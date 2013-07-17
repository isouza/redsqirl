package idiro.workflow.server.connect;

import idiro.hadoop.NameNodeVar;
import idiro.workflow.server.OozieManager;
import idiro.workflow.server.WorkflowPrefManager;
import idiro.workflow.server.connect.interfaces.DataFlowInterface;
import idiro.workflow.server.connect.interfaces.DataStore;
import idiro.workflow.server.connect.interfaces.DataStoreArray;
import idiro.workflow.server.interfaces.JobManager;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMISocketFactory;

import org.apache.log4j.Logger;

/**
 * Class to start up the server.
 * 
 * @author etienne
 * 
 * The server aims to resolve permissions and authentication
 * issues. In this instance it is the client that create server
 * instances independent of each other. The server takes as argument
 * a port.
 * 
 *
 */
public class ServerMain {

	private static Logger logger = Logger.getLogger(ServerMain.class);

	private static Registry registry;

	public static void main(String[] arg) throws RemoteException{

		try {

			logger.info(WorkflowPrefManager.sysPackageLibPath);
			logger.info(WorkflowPrefManager.userPackageLibPath);

			//Update classpath with packages
			updateClassPath(WorkflowPrefManager.sysPackageLibPath);
			updateClassPath(WorkflowPrefManager.userPackageLibPath);

		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}

		int port = 2001;
		
		
		
		// Initialise logs and jar
		WorkflowPrefManager runner = WorkflowPrefManager.getInstance();
		if(runner.isInit()){
			
			logger = Logger.getLogger(ServerMain.class);
			NameNodeVar.set(WorkflowPrefManager.getSysProperty(WorkflowPrefManager.sys_namenode));
			logger.info("sys_namenode Path: " + NameNodeVar.get());

			try {
				
				logger.info("start server main");

				String nameWorkflow = System.getProperty("user.name")+"@wfm";
				String nameHive = System.getProperty("user.name")+"@hive";
				String nameSshArray = System.getProperty("user.name")+"@ssharray";
				String nameOozie = System.getProperty("user.name")+"@oozie";
				String nameHDFS = System.getProperty("user.name")+"@hdfs";

				registry = LocateRegistry.getRegistry(
						"127.0.0.1",
						port,
						RMISocketFactory.getDefaultSocketFactory()
						//new ClientRMIRegistry()	
						);

				registry.rebind(
						nameWorkflow,
						(DataFlowInterface) WorkflowInterface.getInstance()
						);

				logger.info("nameWorkflow");

				registry.rebind(
						nameHive,
						(DataStore) new HiveInterface()
						);

				logger.info("nameHive");

				registry.rebind(
						nameOozie,
						(JobManager) OozieManager.getInstance()
						);

				logger.info("nameOozie");

				registry.rebind(
						nameSshArray,
						(DataStoreArray) new SSHInterfaceArray()
						);

				logger.info("nameSshArray");

				registry.rebind(
						nameHDFS,
						(DataStore) new HDFSInterface()
						);

				logger.info("nameHDFS");


				logger.info("end server main");
				
			} catch (IOException e) {
				logger.error(e.getMessage());
				System.exit(1);
			}
		}
	}

	public static void updateClassPath(String path) throws MalformedURLException{

		URL url = new URL("file:"+path);
		ClassLoader contextCL = Thread.currentThread().getContextClassLoader();
		ClassLoader urlCL = URLClassLoader.newInstance(new URL[] { url }, contextCL);
		Thread.currentThread().setContextClassLoader(urlCL);

	}

}