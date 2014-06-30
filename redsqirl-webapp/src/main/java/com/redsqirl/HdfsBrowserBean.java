package com.redsqirl;


import java.rmi.RemoteException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.redsqirl.workflow.server.connect.interfaces.DataStore;

/**
 * HdfsBean
 * 
 * Class to screen control of the File System Hadoop
 * 
 * @author Igor.Souza
 */
public class HdfsBrowserBean extends HdfsBean {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2705226125355712008L;

	private static Logger logger = Logger.getLogger(HdfsBrowserBean.class);

	/**
	 * Have the same xhtml page for opening and loading. 'T' for showing save,
	 * 'F' for open.
	 */
	private String showSave;

	public HdfsBrowserBean() {
		List<SelectItem> listExtensions = new LinkedList<SelectItem>();
		listExtensions.add(new SelectItem("*.rs", "*.rs"));
		listExtensions.add(new SelectItem("*", "*"));
		setExtensionsSelected(listExtensions.get(0).getLabel());
		setListExtensions(listExtensions);
	}

	/**
	 * createSaveFolder
	 * 
	 * Method to create a default folder to save
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void createSaveFolder() throws RemoteException {
		logger.info("createSaveFolder");
		
		FacesContext fCtx = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) fCtx.getExternalContext().getSession(false);
		String userName = (String) session.getAttribute("username");
		
		String newPath = "/user/" + userName + "/redsqirl-save";

		logger.info("createSaveFolder newPath " + newPath);
		
		if (getDataStore().goTo(newPath)) {
			logger.info("createSaveFolder path is ok");
			updateTable();
		} else {
			getDataStore().create(newPath, new LinkedHashMap<String, String>());
			if (getDataStore().goTo(newPath)) {
				logger.info("createSaveFolder create new path");
				updateTable();
			} else {
				logger.info("createSaveFolder path error");
				getBundleMessage("error.invalid.path");
			}
		}
	}

	@Override
	public DataStore getRmiHDFS() throws RemoteException {
		return getHDFSBrowser();
	}

	/**
	 * @return the showSave
	 */
	public String getShowSave() {
		return showSave;
	}

	/**
	 * @param showSave
	 *            the showSave to set
	 */
	public void setShowSave(String showSave) {
		this.showSave = showSave;
	}

}