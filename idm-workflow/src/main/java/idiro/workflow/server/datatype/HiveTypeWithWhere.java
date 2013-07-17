package idiro.workflow.server.datatype;

import idiro.workflow.server.enumeration.FeatureType;

import java.rmi.RemoteException;
import java.util.Map;

/**
 * Hive Output Action Type.
 * Optionally, if the action create a new partition, 
 * the following actions will use only the new partition.
 *  
 * @author etienne
 *
 */
public class HiveTypeWithWhere extends HiveType{

	/**
	 * 
	 */
	private static final long serialVersionUID = 937674007867999596L;

	public static final String key_where = "where";
	
	public HiveTypeWithWhere() throws RemoteException {
		super();
	}
	
	public HiveTypeWithWhere(Map<String, FeatureType> features)
			throws RemoteException {
		super(features);
	}
	

	@Override
	public String getTypeName() throws RemoteException {
		return "HIVE TABLE WITH WHERE STATEMENT";
	}
}