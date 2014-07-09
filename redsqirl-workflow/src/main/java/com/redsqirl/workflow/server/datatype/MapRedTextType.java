package com.redsqirl.workflow.server.datatype;



import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;

import com.idiro.hadoop.NameNodeVar;
import com.idiro.hadoop.checker.HdfsFileChecker;
import com.idiro.utils.RandomString;
import com.redsqirl.utils.FeatureList;
import com.redsqirl.workflow.utils.LanguageManagerWF;

/**
 * Map-Reduce Text output type. Output given when an algorithm return a text
 * format map-reduce directory.
 * 
 * @author etienne
 * 
 */
public class MapRedTextType extends MapRedDir {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8260229620701006942L;
	/** Delimiter Key */
	public final static String key_delimiter = "delimiter";



	/**
	 * Default Constructor
	 * 
	 * @throws RemoteException
	 */
	public MapRedTextType() throws RemoteException {
		super();
	}

	/**
	 * Constructor with FeatureList
	 * 
	 * @param features
	 * @throws RemoteException
	 */
	public MapRedTextType(FeatureList features) throws RemoteException {
		super(features);
	}

	/**
	 * Get the type name
	 * 
	 * @return name
	 * @throws RemoteException
	 */
	@Override
	public String getTypeName() throws RemoteException {
		return "TEXT MAP-REDUCE DIRECTORY";
	}
	
	@Override
	public String[] getExtensions() throws RemoteException {
		return new String[]{"*.mrtxt"};
	}


	/**
	 * Gernate a path given values
	 * 
	 * @param userName
	 * @param component
	 * @param outputName
	 * @return generated path
	 * @throws RemoteException
	 */
	@Override
	public String generatePathStr(String userName, String component,
			String outputName) throws RemoteException {
		return "/user/" + userName + "/tmp/redsqirl_" + component + "_" + outputName
				+ "_" + RandomString.getRandomName(8)+".mrtxt";
	}

	
	/**
	 * Check if the path is a valid path
	 * 
	 * @return Error Message
	 * @throws RemoteException
	 */
	@Override
	public String isPathValid() throws RemoteException {
		String error = null;
		HdfsFileChecker hCh = new HdfsFileChecker(getPath());
		if (!hCh.isInitialized() || hCh.isFile()) {
			error = LanguageManagerWF.getText("mapredtexttype.dirisfile");
		} else {
			FileSystem fs;
			try {
				fs = NameNodeVar.getFS();
				hCh.setPath(new Path(getPath()).getParent());
				if (!hCh.isDirectory()) {
					error = LanguageManagerWF.getText("mapredtexttype.nodir",new String[]{getPath()});
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
						error = LanguageManagerWF.getText(
								"mapredtexttype.notmrdir",
								new Object[] { getPath() });
					} else {
						try {
							hdfsInt.select(stat[i].getPath().toString(),"", 1);
						} catch (Exception e) {
							error = LanguageManagerWF
									.getText("mapredtexttype.notmrdir");
						}
					}
				}
				try {
					// fs.close();
				} catch (Exception e) {
					logger.error("Fail to close FileSystem: " + e);
				}
			} catch (IOException e) {

				error = LanguageManagerWF.getText("unexpectedexception",
						new Object[] { e.getMessage() });

				logger.error(error);
			}

		}
		// hCh.close();
		return error;
	}

	/**
	 * I
	 */
	@Override
	public boolean isPathAutoGeneratedForUser(String userName,
			String component, String outputName) throws RemoteException {
		return getPath().startsWith(
				"/user/" + userName + "/tmp/redsqirl_" + component + "_"
						+ outputName + "_");
	}

	/**
	 * Select data from the current path
	 * 
	 * @param maxToRead
	 *            limit
	 * @return List of rows returned
	 * @throws RemoteException
	 */
	public List<Map<String,String>> select(int maxToRead) throws RemoteException {
		List<Map<String,String>> ans = new LinkedList<Map<String,String>>();
		Iterator<String> it = selectLine(maxToRead).iterator();
		while(it.hasNext()){
			String l = it.next();
			if(l != null && ! l.isEmpty()){
				String[] line = l.split(
						Pattern.quote(getChar(getProperty(key_delimiter))), -1);
				List<String> featureNames = getFeatures().getFeaturesNames();
				if (featureNames.size() == line.length) {
					Map<String, String> cur = new LinkedHashMap<String, String>();
					for (int i = 0; i < line.length; ++i) {
						cur.put(featureNames.get(i), line[i]);
					}
					ans.add(cur);
				} else {
					logger.error("The line size (" + line.length
							+ ") is not compatible to the number of features ("
							+ featureNames.size() + "). " + "The splitter is '"
							+ getChar(getProperty(key_delimiter)) + "'.");
					logger.error("Error line: " + l);
					ans = null;
					break;
				}
			}
		}
		return ans;
	}


	/**
	 * Set the FeatureList for the data set
	 * 
	 * @param fl
	 * 
	 */
	@Override
	public void setFeatures(FeatureList fl) {
		logger.info("setFeatures :");
		super.setFeatures(fl);
	}


	/**
	 * Get a Default delimiter from text
	 * 
	 * @param text
	 * @return delimiter
	 */
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

	/**
	 * Add a property to the dataset
	 * 
	 * @param key
	 * @param value
	 */
	@Override
	public void addProperty(String key, String value) {

		if (key.equals(key_delimiter) && value.length() == 1) {
			value = "#" + String.valueOf((int) value.charAt(0));
		}
		super.addProperty(key, value);
	}

	/**
	 * Set the path
	 * 
	 * @param path
	 * @throws RemoteException
	 */
	@Override
	public void setPath(String path) throws RemoteException {
		String oldPath = getPath();

		if (path == null) {
			super.setPath(path);
			setFeatures(null);
			return;
		}

		if (!path.equalsIgnoreCase(oldPath)) {

			super.setPath(path);

			logger.info("setPath() " + path);
			if (isPathExists()) {
				List<String> list = selectLine(1);

				if (list != null && !list.isEmpty()) {
					String text = list.get(0);

					if (getProperty(key_delimiter) == null) {
						String delimiter = getDefaultDelimiter(text);

						logger.info("delimiter -> " + delimiter);

						super.addProperty(key_delimiter, delimiter);
					}

				}

				FeatureList fl = generateFeaturesMap(getChar(getProperty(key_delimiter)));
				features = fl;
				
				String error = null;
				String header = getProperty(key_header);
				if (header != null && !header.isEmpty()) {
					logger.info("setFeaturesFromHeader --");
					error = setFeaturesFromHeader();
					if (error != null) {
						throw new RemoteException(error);
					}
				} else {
					if (features != null) {
						String myHeader = fl.mountStringHeader();
						addProperty(key_header, myHeader);
						logger.debug(features.getFeaturesNames());
						logger.debug(fl.getFeaturesNames());
					} else {
						features = fl;
					}
				}

				if (features.getSize() != fl.getSize()) {
					if (header != null && !header.isEmpty()) {
						error = LanguageManagerWF
								.getText("mapredtexttype.setheaders.wronglabels");
					}
					features = fl;
				} else {
					Iterator<String> flIt = fl.getFeaturesNames().iterator();
					Iterator<String> featIt = features.getFeaturesNames()
							.iterator();
					boolean ok = true;
					int i = 1;
					while (flIt.hasNext() && ok) {
						String nf = flIt.next();
						String of = featIt.next();
						logger.info("types feat " + i + ": "
								+ fl.getFeatureType(nf) + " , "
								+ features.getFeatureType(of));
						ok &= canCast(fl.getFeatureType(nf),
								features.getFeatureType(of));
						if (!ok) {
							error = LanguageManagerWF.getText(
									"mapredtexttype.msg_error_cannot_cast",
									new Object[] { fl.getFeatureType(nf),
											features.getFeatureType(of) });
						}
						++i;
					}
					if (!ok) {
						features = fl;
						if (error != null) {
							throw new RemoteException(error);
						}
					}
				}

			}
		}

	}

	/**
	 * Compare the current path , FeatureList , properties to others
	 * 
	 * @param path
	 * @param fl
	 * @param props
	 * @return <code>true</code> if items are equal else <code>false</code>
	 */
	@Override
	public boolean compare(String path, FeatureList fl,
			Map<String, String> props) {
		logger.debug("Comparaison MapRed:");
		logger.debug(this.getPath() + " " + path);
		try {
			logger.debug(features.getFeaturesNames() + " "
					+ fl.getFeaturesNames());
		} catch (Exception e) {
		}
		logger.debug(dataProperty + " " + props);

		String delimNew = props.get(key_delimiter);
		if (delimNew != null && delimNew.length() == 1) {
			delimNew = "#" + String.valueOf((int) delimNew.charAt(0));
		}

		boolean compProps = false;
		if (dataProperty != null) {
			String headOld = dataProperty.get(key_header), headNew = props
					.get(key_header), delimOld = dataProperty
					.get(key_delimiter);
			if (headNew == null) {
				compProps = headOld == null;
			} else {
				compProps = headNew.equals(headOld);
			}
			if (compProps) {
				if (delimNew == null) {
					compProps = delimNew == null;
				} else {
					compProps = delimNew.equals(delimOld);
				}
			}
		} else if (props.isEmpty()) {
			compProps = true;
		}

		return !(this.getPath() == null || features == null) && compProps
				&& (this.getPath().equals(path) && features.equals(fl));
	}


	/**
	 * Get the character from an ascii value
	 * 
	 * @param asciiCode
	 * @return character
	 */
	protected String getChar(String asciiCode) {
		String result = null;
		if(asciiCode == null){
			//default
			result = "|";
		}else if (asciiCode.startsWith("#")
				&& asciiCode.length() > 1) {
			result = String.valueOf(Character.toChars(Integer.valueOf(asciiCode
					.substring(1))));
		} else {
			result = asciiCode;
		}
		return result;
	}

	/**
	 * Get the delimiter in octal format
	 * 
	 * @return delimiter
	 */
	public String getOctalDelimiter() {
		String asciiCode = getProperty(key_delimiter);
		String result = null;
		if (asciiCode != null && asciiCode.startsWith("#")
				&& asciiCode.length() > 1) {
			result = Integer.toOctalString(Integer.valueOf(asciiCode
					.substring(1)));
			if (result.length() == 2) {
				result = "\\0" + result;
			} else {
				result = "\\" + result;
			}
		}
		return result;
	}

	/**
	 * Get the delimiter to be used in Pig format
	 * 
	 * @return delimiter
	 */
	public String getPigDelimiter() {
		String asciiCode = getProperty(key_delimiter);
		Character c = null;
		if (asciiCode == null) {
			c = '|';
		} else if (asciiCode != null && asciiCode.startsWith("#")
				&& asciiCode.length() > 1) {
			int i = Integer.valueOf(asciiCode.substring(1));
			c = new Character((char) i);
		} else if (asciiCode.length() == 1) {
			c = asciiCode.charAt(0);
		}
		
		String result = null;
		
		if (c != null){
			result = String.valueOf(c);
		}
		
		return result;
	}

	/**
	 * Get the delimiter in either octal or decimal notation
	 * 
	 * @return
	 */
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
