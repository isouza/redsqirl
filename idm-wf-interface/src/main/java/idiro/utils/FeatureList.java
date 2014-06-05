package idiro.utils;

import idiro.workflow.server.enumeration.FeatureType;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
/**
 * 
 * Interface that manages the feature list of a dataset.
 *
 */
public interface FeatureList extends Remote {
	/**
	 * Check if feature list contains a feature
	 * @param name feature to check
	 * @return <code>true</code> if feature is in list else <code>false</code>
	 * @throws RemoteException
	 */
	public boolean containsFeature(String name) throws RemoteException;
	/**
	 * Get the {@link idiro.workflow.server.enumeration.FeatureType} of the feature
	 * @param name feature to get type of 
	 * @return type of feature
	 * @throws RemoteException
	 */
	public FeatureType getFeatureType(String name) throws RemoteException;
	
	/**
	 * Add a new feature to the list
	 * @param name Name of the new feature to be added
	 * @param type {@link idiro.workflow.server.enumeration.FeatureType} type to be added
	 * @throws RemoteException
	 */
	public void addFeature(String name, FeatureType type) throws RemoteException;
	/**
	 * Get all features names that are in the list
	 * @return {@link java.util.List<String>}} of feature names
	 * @throws RemoteException
	 */
	public List<String> getFeaturesNames() throws RemoteException;
	/**
	 * Get the size of the list
	 * @return int size of the list
	 * @throws RemoteException
	 */
	public int getSize() throws RemoteException;

	public FeatureList cloneRemote() throws RemoteException;
}
