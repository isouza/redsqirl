package com.redsqirl.workflow.server.datatype;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;

import com.idiro.hadoop.checker.HdfsFileChecker;
import com.idiro.utils.RandomString;
import com.redsqirl.utils.FieldList;
import com.redsqirl.workflow.utils.LanguageManagerWF;

public class MapRedModelType extends MapRedDir {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2256801373086895177L;

	private static Logger logger = Logger.getLogger(MapRedModelType.class);
	
	private String delimiter = new String(new char[] { '\001' });

	public MapRedModelType() throws RemoteException {
		super();
	}

	public MapRedModelType(FieldList fields) throws RemoteException {
		super(fields);
	}

	@Override
	public String getTypeName() throws RemoteException {
		return "MODEL DETAILS DIRECTORY";
	}

	@Override
	public String[] getExtensions() throws RemoteException {
		return new String[] { "*.rsmodel" };
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
		return "/user/" + userName + "/tmp/redsqirl_" + component + "_"
				+ outputName + "_" + RandomString.getRandomName(8) + ".rsmodel";
	}

	@Override
	public String isPathValid() throws RemoteException {
		String error = null;
		HdfsFileChecker hCh = new HdfsFileChecker(getPath());
		if (!hCh.isInitialized() || hCh.isFile()) {
			error = LanguageManagerWF.getText("mapredtexttype.dirisfile");
		} else {
			hCh.setPath(new Path(getPath()).getParent());
			if (!hCh.isDirectory()) {
				error = LanguageManagerWF.getText("mapredtexttype.nodir",new String[]{getPath()});
			}
			if(isPathExists()){
				hCh.setPath(new Path(getPath(), "output"));
				if (!hCh.isFile()) {
					error = LanguageManagerWF.getText("mapredmodeltype.nooutput");
				}
				hCh.setPath(new Path(getPath(), "_output"));
				if (!hCh.isFile()) {
					error = LanguageManagerWF.getText("mapredmodeltype.no_output");
				}
			}

		}
		// hCh.close();
		return error;
	}

	@Override
	public boolean isPathAutoGeneratedForUser(String userName,
			String component, String outputName) throws RemoteException {
		return getPath().startsWith(
				"/user/" + userName + "/tmp/redsqirl_" + component + "_"
						+ outputName + "_");
	}

	@Override
	public List<Map<String, String>> select(int maxToRead)
			throws RemoteException {
		List<Map<String, String>> ans = new LinkedList<Map<String, String>>();
		String patternStr = Pattern.quote(delimiter);
		Iterator<String> it = selectLine(maxToRead).iterator();
		while (it.hasNext()) {
			String l = it.next();
			String[] line = l.split(patternStr, -1);
			List<String> fieldNames = getFields().getFieldNames();
			if (fieldNames.size() == line.length) {
				Map<String, String> cur = new LinkedHashMap<String, String>();
				for (int i = 0; i < line.length; ++i) {
					cur.put(fieldNames.get(i), line[i]);
				}
				ans.add(cur);
			} else {
				logger.error("The line size (" + line.length
						+ ") is not compatible to the number of fields ("
						+ fieldNames.size() + ").");
				logger.error("Error line: " + l);
				ans = null;
				break;
			}
		}
		return ans;
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
			setFields(null);
			return;
		}

		if (!path.equalsIgnoreCase(oldPath)) {

			super.setPath(path);

			logger.info("setPath() " + path);
			if (isPathExists()) {

				FieldList fl = generateFieldsMap(delimiter);
				
				String error = checkCompatibility(fl,fields);
				logger.debug(fields.getFieldNames());
				logger.debug(fl.getFieldNames());
				if(error != null){
					fields = fl;
					throw new RemoteException(error);
				}

			}
		}

	}

	@Override
	protected String getDefaultColor() {
		return "Aqua";
	}

}