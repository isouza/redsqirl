package com.redsqirl.workflow.server.connect.jdbc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.redsqirl.workflow.server.WorkflowPrefManager;
import com.redsqirl.workflow.server.enumeration.FieldType;

public class JdbcTypeManager {

	private static Logger logger = Logger.getLogger(JdbcTypeManager.class);
	public static final String dbTypeFileName = "db_type.txt";
	public static final String rsTypeFileName = "rs_type.txt";
	private static File userHome = new File(WorkflowPrefManager.getPathuserpref());
	protected static Map<String,Map<String,String>> dbTypes = new LinkedHashMap<String,Map<String,String>>();
	protected static Map<String,Map<String,String>> rsTypes = new LinkedHashMap<String,Map<String,String>>();
	
	protected File getDbTypeFile(String dicName){
		File dicFolder = new File(userHome,dicName);
		File dbTypeFile = new File(dicFolder,dbTypeFileName);
		logger.info("DB file for "+dicName+": "+dbTypeFile.getAbsolutePath());
		return dbTypeFile;
	}
	
	protected File getRsTypeFile(String dicName){
		File dicFolder = new File(userHome,dicName);
		File rsTypeFile = new File(dicFolder,rsTypeFileName);
		logger.info("RS file for "+dicName+": "+rsTypeFile.getAbsolutePath());
		return rsTypeFile;
	}

	
	protected Map<String,String> readTypeFile(File file) throws IOException{
		Map<String,String> ans = new LinkedHashMap<String,String>();
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line = null;
		while( (line = br.readLine()) != null){
			try{
				String[] lineArr = line.split(":");
				ans.put(lineArr[0].trim(), lineArr[1].trim());
			}catch(Exception e){}
		}
		br.close();
		return ans;
	}
	
	protected Map<String,String> readDbTypeFile(String dicName) throws IOException{
		return readTypeFile(getDbTypeFile(dicName));
	}
	
	protected Map<String,String> readRsTypeFile(String dicName) throws IOException{
		return readTypeFile(getRsTypeFile(dicName));
	}
	
	protected Map<String,String>  getDbType(String dicName){
		if(dbTypes.get(dicName) == null){
			try{
				dbTypes.put(dicName, readDbTypeFile(dicName));
			}catch(Exception e){}
		}
		return dbTypes.get(dicName);
	}
	
	protected Map<String,String>  getRsType(String dicName){
		if(rsTypes.get(dicName) == null){
			try{
				rsTypes.put(dicName, readRsTypeFile(dicName));
			}catch(Exception e){}
		}
		return rsTypes.get(dicName);
	}
	
	
	public String getDbType(String dicName, FieldType rsType){
		Map<String,String> map = getDbType(dicName);
		if(map == null){
			logger.error("No DB map for "+dicName);
		}
		return map != null? map.get(rsType.toString()):null;
	}
	
	public FieldType getRsType(String dicName, String fieldType){
		Map<String,String> map = getRsType(dicName);
		FieldType ans = null;
		if(map == null){
			logger.error("No RS map for "+dicName);
		}else{
			Iterator<String> it = map.keySet().iterator();
			while(it.hasNext() && ans == null){
				String key = it.next();
				logger.info(fieldType+" match "+key+" ?");
				if(fieldType.matches(key)){
					try{
						ans = FieldType.valueOf(map.get(key));
					}catch(Exception e){
						logger.warn(e,e);
					}
				}
			}
		}
		return ans;
	}
}
