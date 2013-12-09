package idiro.workflow.server.datatype;

import idiro.hadoop.NameNodeVar;
import idiro.hadoop.checker.HdfsFileChecker;
import idiro.utils.FeatureList;
import idiro.utils.OrderedFeatureList;
import idiro.utils.RandomString;
import idiro.workflow.server.DataOutput;
import idiro.workflow.server.OozieManager;
import idiro.workflow.server.WorkflowPrefManager;
import idiro.workflow.server.connect.HDFSInterface;
import idiro.workflow.server.enumeration.DataBrowser;
import idiro.workflow.server.enumeration.FeatureType;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Map-Reduce Text output type. Output given when an algorithm return a text
 * format map-reduce directory.
 * 
 * @author etienne
 * 
 */
public class MapRedTextType extends DataOutput {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8260229620701006942L;

	public final static String key_delimiter = "delimiter";
	public final static String key_header = "header";
	//public final static String key_delimiter_char = "delimiter_char";

	protected static HDFSInterface hdfsInt;

	public MapRedTextType() throws RemoteException {
		super();
		if (hdfsInt == null) {
			hdfsInt = new HDFSInterface();
		}
	}

	public MapRedTextType(FeatureList features) throws RemoteException {
		super(features);
		if (hdfsInt == null) {
			hdfsInt = new HDFSInterface();
		}
	}

	@Override
	public String getTypeName() throws RemoteException {
		return "TEXT MAP-REDUCE DIRECTORY";
	}

	@Override
	public DataBrowser getBrowser() throws RemoteException {
		return DataBrowser.HDFS;
	}

	@Override
	public void generatePath(String userName, String component,
			String outputName) throws RemoteException {
		setPath("/user/" + userName + "/tmp/idm_" + component + "_"
				+ outputName + "_" + RandomString.getRandomName(8));
	}

	@Override
	public String isPathValid() throws RemoteException {
		String error = null;
		HdfsFileChecker hCh = new HdfsFileChecker(getPath());
		if (!hCh.isInitialized() || hCh.isFile()) {
			error = "The map reduce directory is a file";
		} else {
			final FileSystem fs;
			try {
				//if testing uncomment
				NameNodeVar.set(WorkflowPrefManager
						.getSysProperty(WorkflowPrefManager.sys_namenode));
				fs = NameNodeVar.getFS();
				hCh.setPath(new Path(getPath()).getParent());
				if (!hCh.isDirectory()) {
					error = "The parent of the file does not exists";
				}
				FileStatus[] stat = fs.listStatus(new Path(getPath()),
						new PathFilter() {

							@Override
							public boolean accept(Path arg0) {
								return !arg0.getName().startsWith("_");
							}
						});
				for (int i = 0; i < stat.length && error == null; ++i) {
					if (stat[i].isDir()) {
						error = "The path " + getPath()
								+ " is not a map-reduce directory";
					}
				}
				fs.close();
			} catch (IOException e) {
				error = "Unexpected error: " + e.getMessage();
				logger.error(error);
			}

		}
		hCh.close();
		return error;
	}

	@Override
	public boolean isPathAutoGeneratedForUser(String userName,
			String component, String outputName) throws RemoteException {
		return getPath().startsWith(
				"/user/" + userName + "/tmp/idm_" + component + "_"
						+ outputName + "_");
	}

	@Override
	public boolean isPathExists() throws RemoteException {
		boolean ok = false;
		HdfsFileChecker hCh = new HdfsFileChecker(getPath());
		if (hCh.exists()) {
			ok = true;
		}
		hCh.close();
		return ok;
	}

	@Override
	public String remove() throws RemoteException {
		return hdfsInt.delete(getPath());
	}

	@Override
	public boolean oozieRemove(Document oozieDoc, Element action,
			File localDirectory, String pathFromOozieDir,
			String fileNameWithoutExtension) throws RemoteException {
		Element fs = oozieDoc.createElement("fs");
		action.appendChild(fs);

		Element rm = oozieDoc.createElement("delete");
		rm.setAttribute("path", "${" + OozieManager.prop_namenode + "}"
				+ getPath());
		fs.appendChild(rm);

		return true;
	}

	@Override
	public List<String> select(int maxToRead) throws RemoteException {
		List<String> ans = null;
		if (isPathValid() == null ){//&& isPathExists()) {
			try {
				final FileSystem fs = NameNodeVar.getFS();
				FileStatus[] stat = fs.listStatus(new Path(getPath()),
						new PathFilter() {

							@Override
							public boolean accept(Path arg0) {
								return !arg0.getName().startsWith("_");
							}
						});
				ans = new ArrayList<String>(maxToRead);
				for (int i = 0; i < stat.length; ++i) {
					ans.addAll(hdfsInt.select(stat[i].getPath().toString(),
							getChar(getProperty(key_delimiter)),
							(maxToRead / stat.length) + 1));
				}
			} catch (IOException e) {
				String error = "Unexpected error: " + e.getMessage();
				logger.error(error);
				ans = null;
			}
		}else{
			logger.error("ans is null ");
		}
		return ans;
	}

	private void generateFeaturesMap() throws RemoteException {
		logger.info("Generating features map");
		features = new OrderedFeatureList();
		try {
			List<String> lines = this.select(10);
			if (lines != null) {
				for (String line : lines) {
					if (!line.trim().isEmpty()) {
						int cont = 0;
						for (String s : line.split(Pattern
								.quote(getChar(getProperty(key_delimiter))))) {
							String nameColumn = generateColumnName(cont++);
							FeatureType type = getType(s.trim());
							if (features.containsFeature(nameColumn)) {
								if (!canCast(type,
										features.getFeatureType(nameColumn))) {
									features.addFeature(nameColumn, type);
								}
							} else {
								features.addFeature(nameColumn, type);
							}
						}
					}
				}
			}
		} catch (RemoteException e) {
			logger.error("Could not sekect the first 10 lines");
			e.printStackTrace();
		}
	}

	private String getDefaultDelimiter(String text) {
		if (text.contains("\001")) {
			return "#1";
		} else if (text.contains("\002")) {
			return "#2";
		} else if (text.contains("|")) {
			return "#124";
		}
		return "#1";
	}

	private FeatureType getType(String expr) {

		FeatureType type = null;
		if (expr.equalsIgnoreCase("TRUE") || expr.equalsIgnoreCase("FALSE")) {
			type = FeatureType.BOOLEAN;
		} else {
			try {
				Integer.valueOf(expr);
				type = FeatureType.INT;
			} catch (Exception e) {
			}
			if (type == null) {
				try {
					Long.valueOf(expr);
					type = FeatureType.LONG;
				} catch (Exception e) {
				}
			}
			if (type == null) {
				try {
					Float.valueOf(expr);
					type = FeatureType.FLOAT;
				} catch (Exception e) {
				}
			}
			if (type == null) {
				try {
					Double.valueOf(expr);
					type = FeatureType.DOUBLE;
				} catch (Exception e) {
				}
			}
			if (type == null) {
				type = FeatureType.STRING;
			}
		}
		logger.info("getType: " + expr + " - " + type);
		return type;
	}

	private boolean canCast(FeatureType from, FeatureType to) {
		if (from.equals(to)) {
			return true;
		}

		List<FeatureType> features = new ArrayList<FeatureType>();
		features.add(FeatureType.INT);
		features.add(FeatureType.LONG);
		features.add(FeatureType.FLOAT);
		features.add(FeatureType.DOUBLE);
		features.add(FeatureType.STRING);

		if (from.equals(FeatureType.BOOLEAN)) {
			if (to.equals(FeatureType.STRING)) {
				return true;
			}
			return false;
		} else if (features.indexOf(from) <= features.indexOf(to)) {
			return true;
		}
		return false;
	}

	@Override
	public void addProperty(String key, String value) {

		if (key.equals(key_delimiter) && value.length() == 1) {
			value = "#" + String.valueOf((int) value.charAt(0));
		}

		super.addProperty(key, value);

		if (key.equals(key_delimiter) && getPath() != null) {
			try {
				logger.info("addProperty() ");
				generateFeaturesMap();

			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void setPath(String path) throws RemoteException {
		String oldPath = getPath();

		if (!path.equalsIgnoreCase(oldPath)) {

			super.setPath(path);

			logger.info("setPath() " + path);

			List<String> list = select(1);

			if (list != null && !list.isEmpty()) {
				String text = list.get(0);
				if (getProperty(key_delimiter) == null) {
					String delimiter = getDefaultDelimiter(text);
					super.addProperty(key_delimiter, delimiter);
					super.addProperty(key_header, "");
				}
				else{
					if (!text.contains(getProperty(key_delimiter))){
						String delimiter = getDefaultDelimiter(text);
						super.addProperty(key_delimiter, delimiter);
						super.addProperty(key_header, "");
					}
				}
			}
			generateFeaturesMap();
		}

	}

	private String generateColumnName(int columnIndex) {
		if (columnIndex > 25) {
			return generateColumnName(((columnIndex) / 26) - 1)
					+ generateColumnName(((columnIndex) % 26));
		} else
			return String.valueOf((char) (columnIndex + 65));
	}

	protected String getChar(String asciiCode) {
		String result = null;
		if (asciiCode != null && asciiCode.startsWith("#")
				&& asciiCode.length() > 1) {
			result = String.valueOf(Character.toChars(Integer.valueOf(asciiCode
					.substring(1))));
		}
		return result;
	}

	public String getOctalDelimiter() {
		String asciiCode = getProperty(key_delimiter);
		String result = null;
		if (asciiCode != null && asciiCode.startsWith("#")
				&& asciiCode.length() > 1) {
			result = "\\"
					+ Integer.toOctalString(Integer.valueOf(asciiCode
							.substring(1)));
		}
		return result;
	}

	public String getDelimiterOrOctal() {
		String octal = getOctalDelimiter();
		return octal != null ? octal
				: getProperty(MapRedTextType.key_delimiter);
	}

	@Override
	protected String getDefaultColor() {
		return "Chocolate";
	}

}
