package com.redsqirl;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.redsqirl.auth.AuthorizationListener;
import com.redsqirl.auth.UserInfoBean;
import com.redsqirl.dynamictable.SelectHeaderType;
import com.redsqirl.dynamictable.SelectableRow;
import com.redsqirl.dynamictable.SelectableRowFooter;
import com.redsqirl.dynamictable.SelectableTable;
import com.redsqirl.workflow.server.interfaces.DataFlow;
import com.redsqirl.workflow.utils.ModelInt;
import com.redsqirl.workflow.utils.PackageManager;

public class ConfigureTabsBean extends BaseBean implements Serializable {


	private static final long serialVersionUID = 4626482566525824607L;

	private static Logger logger = Logger.getLogger(ConfigureTabsBean.class);

	private String workflowNameTmp = "wf-footer-123";
	private Map<String, List<String[]>> tabsMap;
	private SelectableTable tableGrid = new SelectableTable();
	private List<String> tabs;
	private List<SelectItem> listPackages;
	private List<SelectHeaderType> listActions;
	private String selectedPackage;
	private Integer selectedTab;
	private Map<String,String> mapActionPackage;


	/** openCanvasConfigureTabsBean
	 * 
	 * Methods Used in AuthorizationListener.java to open the canvas.xhtml
	 * 
	 * @see AuthorizationListener.java
	 * @author Igor.Souza
	 */
	public void openCanvasConfigureTabsBean() {

		try{

			if (getworkFlowInterface().getWorkflow(workflowNameTmp) == null) {
				getworkFlowInterface().addWorkflow(workflowNameTmp);
			}

			DataFlow wf = getworkFlowInterface().getWorkflow(workflowNameTmp);
			wf.loadMenu();
			tabsMap = wf.getRelativeMenu(getCurrentPage());

			setTabs(new LinkedList<String>(getTabsMap().keySet()));

			HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
			String user = (String) session.getAttribute("username");
			PackageManager pckManager = new PackageManager();
			mapActionPackage = pckManager.getPackageOfActions(user);

		} catch (Exception e) {
			logger.error("Error openConfigureTabsBean " + e,e);
		}

	}

	/** openConfigureTabsBean
	 * 
	 * Methods Used called to open the configure footer popup
	 * 
	 * @see canvas.xhtml
	 * @author Igor.Souza
	 * @throws RemoteException 
	 */
	public void openConfigureTabsBean() throws RemoteException {

		setTableGrid(new SelectableTable());
		for (String name : getTabsMap().keySet()) {

			List<SelectHeaderType> ans = new ArrayList<SelectHeaderType>();
			List<String[]> l = getTabsMap().get(name);
			for (String[] value : l) {
				SelectHeaderType sht = new SelectHeaderType(mapActionPackage.get(value[0]) , value[0]);
				ans.add(sht);
			}
			SelectableRowFooter str = new SelectableRowFooter(ans);
			str.setNameTab(name);
			getTableGrid().getRows().add(str);
		}

	}

	/** deleteTab
	 * 
	 * Methods to remove one line in the table. Remove one tab in the footer
	 * 
	 * @author Igor.Souza
	 */
	public void deleteTab() {
		for (Iterator<SelectableRow> iterator = getTableGrid().getRows().iterator(); iterator.hasNext();) {
			SelectableRow selectableRow = (SelectableRow) iterator.next();
			if(selectableRow.isSelected()){
				iterator.remove();
				getTabsMap().remove(selectableRow.getNameTab());
			}
		}
	}

	/** createTab
	 * 
	 * Methods to add one line in the table. this line represent on tab in the footer
	 * 
	 * @author Igor.Souza
	 */
	public void createTab() throws RemoteException, Exception {
		getTableGrid().getRows().add(new SelectableRowFooter(new ArrayList<SelectHeaderType>()));
	}

	/** saveTabs
	 * 
	 * Methods to save the list of tabs names
	 * 
	 * @author Igor.Souza
	 * @throws RemoteException 
	 */
	public void saveTabs() throws RemoteException {

		Map<String,List<String>> mapMenu = new LinkedHashMap<String,List<String>>();

		for (Iterator iterator = tableGrid.getRows().iterator(); iterator.hasNext();) {
			SelectableRowFooter s = (SelectableRowFooter) iterator.next();
			List<String> temp = new ArrayList<String>();
			for (int i = 0; i < s.getSelectedActions().size(); ++i) {
				temp.add(s.getSelectedActions().get(i).getType());
			}
			mapMenu.put(s.getNameTab() , temp);
		}

		DataFlow wf = getworkFlowInterface().getWorkflow(workflowNameTmp);
		wf.loadMenu(mapMenu);
		wf.saveMenu();
		tabsMap = wf.getRelativeMenu(getCurrentPage());
		setTabs(new LinkedList<String>(getTabsMap().keySet()));
	}

	/** openActionsPanel
	 * 
	 * open the settings configure the actions
	 * 
	 * @param selected - id of tabsMap came from .xhtml
	 * @see configureFooterTabs.xhtml
	 * @author Igor.Souza
	 * @throws RemoteException 
	 */
	public void openActionsPanel() throws RemoteException {

		logger.info("openActionsPanel");
		String selectedTab = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("selected");
		if(selectedTab != null){
			setSelectedTab(Integer.parseInt(selectedTab));

			listPackages = new ArrayList<SelectItem>();
			listPackages.add(new SelectItem("all", "all"));
			listPackages.add(new SelectItem("core", "core"));

			HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
			String user = (String) session.getAttribute("username");

			PackageManager pckManager = new PackageManager();
			for (String name : pckManager.getPackageNames(user)) {
				SelectItem s = new SelectItem(name, name);
				listPackages.add(s);
			}
			for (String name : pckManager.getPackageNames(null)) {
				SelectItem s = new SelectItem(name, name);
				listPackages.add(s);
			}
			if(listPackages != null && !listPackages.isEmpty()){
				setSelectedPackage(listPackages.get(0).getLabel());
				retrieveActions(getSelectedPackage());
			}
			
			
			for (ModelInt modelInt : getModelManager().getAvailableModels(user)) {
				SelectItem s = new SelectItem(modelInt.getName(), modelInt.getName());
				listPackages.add(s);
			}

			calculateListSelectedActions();

		}

	}

	/** calculateListSelectedActions
	 * 
	 * Method to create the list of selected actions for each tab
	 * 
	 * @author Igor.Souza
	 * @throws RemoteException 
	 */
	public void calculateListSelectedActions() throws RemoteException {

		SelectableRowFooter s = (SelectableRowFooter) tableGrid.getRows().get(getSelectedTab());
		for (SelectHeaderType selectHeaderType : s.getSelectedActions()) {
			for (Iterator<SelectHeaderType> iterator = listActions.iterator(); iterator.hasNext();) {
				SelectHeaderType actions = (SelectHeaderType) iterator.next();
				if(actions.getType().equals(selectHeaderType.getType())){
					iterator.remove();
				}
			}
		}

	}

	public void retrieveActions() throws RemoteException {
		String selectedPackage = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("selectedPackage");
		retrieveActions(selectedPackage);
		calculateListSelectedActions();
	}

	/** retrieveActions
	 * 
	 * Method to create the list of actions for each package
	 * 
	 * @param selectedPackage
	 * @author Igor.Souza
	 * @throws RemoteException 
	 */
	public void retrieveActions(String selectedPackage) throws RemoteException {

		if(selectedPackage != null){
			setSelectedPackage(selectedPackage);

			HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
			String user = (String) session.getAttribute("username");

			PackageManager pckManager = new PackageManager();
			listActions = new ArrayList<SelectHeaderType>();

			if(selectedPackage.equals("all")){
				for (String name : pckManager.getCoreActions()) {
					SelectHeaderType s = new SelectHeaderType("core", name);
					listActions.add(s);
				}
				Map<String,List<String>> map = pckManager.getActionsPerPackage(user);
				for (String key : map.keySet()) {
					List<String> ansList = map.get(key);
					if(ansList != null && !ansList.isEmpty()){
						for (String action : ansList) {
							SelectHeaderType selectHeaderType = new SelectHeaderType(key, action);
							listActions.add(selectHeaderType);
						}
					}
				}
			}else if(selectedPackage.equals("core")){
				for (String name : pckManager.getCoreActions()) {
					SelectHeaderType s = new SelectHeaderType("core", name);
					listActions.add(s);
				}
			}else{
				
				Map<String,List<String>> map = pckManager.getActionsPerPackage(user);
				List<String> ansList = map.get(selectedPackage);
				if(ansList != null && !ansList.isEmpty()){
					for (String action : ansList) {
						SelectHeaderType selectHeaderType = new SelectHeaderType(selectedPackage, action);
						listActions.add(selectHeaderType);
					}
				}else{
					
					for (ModelInt modelInt : getModelManager().getAvailableModels(user)) {
						if(modelInt.getName().equals(selectedPackage)){
							for (String superAction : modelInt.getSubWorkflowNames()) {
								SelectHeaderType selectHeaderType = new SelectHeaderType(selectedPackage, superAction);
								listActions.add(selectHeaderType);
							}
						}
					}
					
				}
				
			}

		}

	}

	/** cancelActions
	 * 
	 * re calculate the list of selected actions for the selected tab 
	 * 
	 * @author Igor.Souza
	 */
	public void cancelActions() throws RemoteException {
		SelectableRowFooter s = (SelectableRowFooter) tableGrid.getRows().get(getSelectedTab());
		List<String[]> l = getTabsMap().get(s.getNameTab());
		List<SelectHeaderType> ans = new ArrayList<SelectHeaderType>();
		for (String[] value : l) {
			SelectHeaderType sht = new SelectHeaderType(mapActionPackage.get(value[0]) , value[0]);
			ans.add(sht);
		}
		s.setSelectedActions(ans);
		setSelectedTab(null);
	}

	public void selectAll(){

		SelectableRowFooter s = (SelectableRowFooter) tableGrid.getRows().get(getSelectedTab());

		for (Iterator<SelectHeaderType> iterator = listActions.iterator(); iterator.hasNext();) {
			SelectHeaderType actions = (SelectHeaderType) iterator.next();
			logger.info(actions.isSelected());
			s.getSelectedActions().add(actions);
			iterator.remove();
		}

	}

	public void select(){

		SelectableRowFooter s = (SelectableRowFooter) tableGrid.getRows().get(getSelectedTab());

		for (Iterator<SelectHeaderType> iterator = listActions.iterator(); iterator.hasNext();) {
			SelectHeaderType actions = (SelectHeaderType) iterator.next();
			logger.info(actions.isSelected());
			if(actions.isSelected()){
				s.getSelectedActions().add(actions);
				iterator.remove();
			}
		}

	}

	public void unselect(){

		SelectableRowFooter s = (SelectableRowFooter) tableGrid.getRows().get(getSelectedTab());

		for (Iterator<SelectHeaderType> iterator = s.getSelectedActions().iterator(); iterator.hasNext();) {
			SelectHeaderType actions = (SelectHeaderType) iterator.next();
			logger.info(actions.isSelected());
			if(actions.isSelected()){
				if(actions.getName().equals(getSelectedPackage()) || getSelectedPackage().equals("all")){
					listActions.add(actions);
					iterator.remove();
				}else{
					iterator.remove();
				}
			}
		}

	}

	public void unselectAll(){

		SelectableRowFooter s = (SelectableRowFooter) tableGrid.getRows().get(getSelectedTab());

		for (Iterator<SelectHeaderType> iterator = s.getSelectedActions().iterator(); iterator.hasNext();) {
			SelectHeaderType actions = (SelectHeaderType) iterator.next();
			logger.info(actions.isSelected());
			if(actions.getName().equals(getSelectedPackage()) || getSelectedPackage().equals("all")){
				listActions.add(actions);
				iterator.remove();
			}else{
				iterator.remove();
			}
		}

	}

	/** getTabSelectedActions
	 * 
	 * Methods to return the list of selected actions. Used in configureFooterActionsTabs.xhtml to Iterator the tabs 
	 * 
	 * @see configureFooterActionsTabs.xhtml
	 * @return List<SelectHeaderType>
	 * @author Igor.Souza
	 */
	public List<SelectHeaderType> getTabSelectedActions(){
		if(getSelectedTab() != null){
			SelectableRowFooter s = (SelectableRowFooter) tableGrid.getRows().get(getSelectedTab());
			return s.getSelectedActions();
		}
		return new ArrayList<SelectHeaderType>();
	}

	/** getMenuWAList
	 * 
	 * Methods to return the list of tabs. Used in canvas.xhtml to Iterator the tabs of the footer 
	 * 
	 * @see canvas.xhtml
	 * @return List<Entry<String, List<String[]>>>
	 * @author Igor.Souza
	 */
	public List<Entry<String, List<String[]>>> getTabsMapList(){
		List<Entry<String, List<String[]>>> list = new ArrayList<Entry<String, List<String[]>>>();
		for (Entry<String, List<String[]>> e : getTabsMap().entrySet()) {
			list.add(e);
		}
		return list;
	}


	public Map<String, List<String[]>> getTabsMap() {
		return tabsMap;
	}

	public void setTabsMap(Map<String, List<String[]>> tabsMap) {
		this.tabsMap = tabsMap;
	}

	public SelectableTable getTableGrid() {
		return tableGrid;
	}

	public void setTableGrid(SelectableTable tableGrid) {
		this.tableGrid = tableGrid;
	}

	public List<String> getTabs() {
		return tabs;
	}

	public void setTabs(List<String> tabs) {
		this.tabs = tabs;
	}

	public List<SelectItem> getListPackages() {
		return listPackages;
	}

	public void setListPackages(List<SelectItem> listPackages) {
		this.listPackages = listPackages;
	}

	public String getSelectedPackage() {
		return selectedPackage;
	}

	public void setSelectedPackage(String selectedPackage) {
		this.selectedPackage = selectedPackage;
	}

	public List<SelectHeaderType> getListActions() {
		return listActions;
	}

	public void setListActions(List<SelectHeaderType> listActions) {
		this.listActions = listActions;
	}

	public Integer getSelectedTab() {
		return selectedTab;
	}

	public void setSelectedTab(Integer selectedTab) {
		this.selectedTab = selectedTab;
	}

	public Map<String, String> getMapActionPackage() {
		return mapActionPackage;
	}

	public void setMapActionPackage(Map<String, String> mapActionPackage) {
		this.mapActionPackage = mapActionPackage;
	}

}