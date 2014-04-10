package idm;

import idiro.utils.LocalFileSystem;
import idiro.workflow.server.WorkflowPrefManager;
import idiro.workflow.server.connect.interfaces.DataFlowInterface;
import idiro.workflow.server.enumeration.SavingState;
import idiro.workflow.server.interfaces.DFELinkProperty;
import idiro.workflow.server.interfaces.DFEOutput;
import idiro.workflow.server.interfaces.DataFlow;
import idiro.workflow.server.interfaces.DataFlowElement;
import idiro.workflow.server.interfaces.JobManager;
import idm.auth.UserInfoBean;
import idm.useful.MessageUseful;

import java.io.File;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.WordUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CanvasBean extends BaseBean implements Serializable {

	private static Logger logger = Logger.getLogger(CanvasBean.class);

	private List<SelectItem> linkPossibilities = new ArrayList<SelectItem>();
	private String selectedLink;
	private int nbLinkPossibilities = 0;
	private String nameWorkflow;
	private DataFlow df;
	private String paramOutId;
	private String paramInId;
	private String paramNameLink;
	private String[] result;
	private String idElement;
	private String idGroup;
	private String nameOutput;
	private String linkLabel;
	private Map<String, Map<String, String>> idMap;
	private UserInfoBean userInfoBean;
	private String path;
	private String workflowElementUrl;

	private Map<String, DataFlow> workflowMap;

	private String errorTableState = new String();


	/**
	 * Don't open the modal window after changing the ID.
	 */
	private boolean changeElementOnly = false;

	public void doNew() {

		logger.info("doNew");

	}

	public void doOpen() {

		logger.info("doOpen");

	}

	/**
	 * openCanvas
	 * 
	 * Methods to mount the first canvas
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public CanvasBean() {

	}

	//@PostConstruct
	public void init(){


		logger.info("openCanvas");

		FacesContext context = FacesContext.getCurrentInstance();
		userInfoBean = (UserInfoBean) context.getApplication()
				.evaluateExpressionGet(context, "#{userInfoBean}",
						UserInfoBean.class);

		userInfoBean.setCurrentValue(Long.valueOf(78));

		workflowMap = new HashMap<String, DataFlow>();
		setNameWorkflow("canvas-1");

		setIdMap(new HashMap<String, Map<String, String>>());
		getIdMap().put(getNameWorkflow(), new HashMap<String, String>());

		DataFlowInterface dfi;
		try {

			dfi = getworkFlowInterface();
			if (dfi.getWorkflow(getNameWorkflow()) == null) {
				dfi.addWorkflow(getNameWorkflow());
			} else {
				dfi.removeWorkflow(getNameWorkflow());
				dfi.addWorkflow(getNameWorkflow());
			}
			logger.info("add new Workflow " + getNameWorkflow());

			if (userInfoBean.getCurrentValue() < 98) {
				userInfoBean
				.setCurrentValue(userInfoBean.getCurrentValue() + 3);
			}

			setDf(dfi.getWorkflow(getNameWorkflow()));
			getDf().getAllWANameWithClassName();

			workflowMap.put(getNameWorkflow(), getDf());

		} catch (RemoteException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}

	}

	public List<String[]> getHelpItens() throws Exception {
		if (getworkFlowInterface().getWorkflow("canvas-1") == null) {
			getworkFlowInterface().addWorkflow("canvas-1");
		}

		String relPath = ((HttpServletRequest) FacesContext
				.getCurrentInstance().getExternalContext().getRequest())
				.getRequestURI();

		logger.info(relPath);
		File f = null;
		Map<String, List<String[]>> newList = null;
		logger.info(relPath);
		DataFlow wf = getworkFlowInterface().getWorkflow("canvas-1");

		List<Integer> pos = new ArrayList<Integer>();
		for (int i = 0; i < relPath.length(); i++) {

			if (relPath.charAt(i) == '/') {
				pos.add(i);
			}
		}
		relPath = relPath.substring(0, pos.get(pos.size() - 1));
		logger.info(relPath);
		try {
			f = new File(
					WorkflowPrefManager
					.getSysProperty(WorkflowPrefManager.sys_tomcat_path)
					+ relPath);
			if (!f.exists()) {
				logger.info(relPath.substring(pos.get(1)));
				f = new File(
						WorkflowPrefManager
						.getSysProperty(WorkflowPrefManager.sys_tomcat_path)
						+ relPath.substring(pos.get(1)));
			}
			newList = wf.loadMenu(f);
			logger.info(f.getAbsolutePath());
		} catch (Exception e) {
			logger.info("E");
		}
		Iterator<String> it = newList.keySet().iterator();
		while (it.hasNext()) {
			List<String[]> tab = newList.get(it.next());
			for (String[] s : tab) {
				logger.info(s[0] + " , " + s[1] + " , " + s[2]);
			}
		}

		Iterator<String> keysIt = newList.keySet().iterator();

		List<String[]> helpList = new ArrayList<String[]>();

		while (keysIt.hasNext()) {
			String key = keysIt.next();
			List<String[]> menuItem = newList.get(key);
			for (String[] e : menuItem) {
				String name = WordUtils.capitalizeFully(e[0].replace("_", " "));
				helpList.add(new String[] { name, e[2],e[0] });
			}
		}
		return helpList;
	}

	/**
	 * addElement
	 * 
	 * Method for add Element on canvas. set the new idElement on the element
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void addElement() {

		logger.info("addElement");
		logger.info("numWorkflows: " + getWorkflowMap().size());
		logger.info("numIdMap: " + getIdMap().size());

		Map<String, String> params = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap();

		String nameElement = params.get("paramNameElement");
		String paramGroupID = params.get("paramGroupID");

		logger.info("nameElement " + nameElement);
		logger.info("paramGroupID " + paramGroupID);

		try {
			DataFlow df = getDf();

			if (nameElement != null && paramGroupID != null) {
				String idElement = df.addElement(nameElement);
				if (idElement != null) {
					getIdMap().get(getNameWorkflow()).put(paramGroupID,
							idElement);
				} else {
					MessageUseful.addErrorMessage("NULL POINTER"); // FIXME
					HttpServletRequest request = (HttpServletRequest) FacesContext
							.getCurrentInstance().getExternalContext()
							.getRequest();
					request.setAttribute("msnError", "msnError");
				}
			}

		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		logger.info("finish add element");

	}

	/**
	 * removeElement
	 * 
	 * Method to remove Element on canvas.
	 * 
	 * @return
	 * @author Marcos.Freitas
	 */
	public void removeElement() {

		Map<String, String> params = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap();

		String paramGroupID = params.get("paramGroupID");

		try {

			DataFlow df = getDf();
			df.removeElement(getIdMap().get(getNameWorkflow())
					.get(paramGroupID));
			getIdMap().remove(paramGroupID);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * updatePosition
	 * 
	 * Method for update the position of an Element
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void updatePosition(String paramGroupID, String posX, String posY) {
		updatePosition(getNameWorkflow(), paramGroupID, posX, posY);
	}

	/**
	 * Update Position
	 * 
	 * @param workflowName
	 * @param paramGroupID
	 * @param posX
	 * @param posY
	 */
	public void updatePosition(String workflowName, String paramGroupID,
			String posX, String posY) {

		logger.info("updatePosition");
		logger.info("canvas Name: " + getIdMap().keySet());

		logger.info("getIdMap1 :" + getIdMap());
		logger.info("getIdMap2 :" + getIdMap().get(workflowName));
		logger.info("getIdMap3 :" + paramGroupID);
		if (getIdMap().get(workflowName) != null) {
			logger.info("getIdMap4 :"
					+ getIdMap().get(workflowName).get(paramGroupID));
			if (getIdMap().get(workflowName).get(paramGroupID) != null) {
				try {
					DataFlow df = getDf();
					df.getElement(
							getIdMap().get(workflowName).get(paramGroupID))
							.setPosition(Double.valueOf(posX).intValue(),
									Double.valueOf(posY).intValue());
					logger.info(workflowName + " - "
							+ getIdMap().get(workflowName).get(paramGroupID)
							+ " - " + Double.valueOf(posX).intValue() + " - "
							+ Double.valueOf(posY).intValue());

				} catch (RemoteException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * addLink
	 * 
	 * Method for add Link for two elements
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void addLink() {
		logger.info("addLink");
		String idElementA = getIdMap().get(getNameWorkflow()).get(
				getParamOutId());
		String idElementB = getIdMap().get(getNameWorkflow()).get(
				getParamInId());

		String nameElementA = getSelectedLink().split(" -> ")[0];
		String nameElementB = getSelectedLink().split(" -> ")[1];

		try {

			DataFlow df = getDf();

			DataFlowElement dfeObjA = df.getElement(idElementA);
			DataFlowElement dfeObjB = df.getElement(idElementB);

			df.addLink(nameElementA, dfeObjA.getComponentId(), nameElementB,
					dfeObjB.getComponentId());

			setResult(new String[] { getParamNameLink(), nameElementA,
					nameElementB });

			setNameOutput(nameElementA);

			// generate the label to put in the arrow
			String label = "";

			if (dfeObjA.getDFEOutput().entrySet().size() > 1
					|| dfeObjB.getInput().entrySet().size() > 1) {
				if (dfeObjA.getDFEOutput().entrySet().size() > 1) {
					label += nameElementA;
				}
				label += " -> ";
				if (dfeObjB.getInput().entrySet().size() > 1) {
					label += nameElementB;
				}
			}
			setLinkLabel(label);

		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void updateLinkPossibilities() {

		logger.info("updateLinkPossibilities");

		Map<String, String> params = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap();
		String idElementA = getIdMap().get(getNameWorkflow()).get(
				params.get("paramOutId"));
		String idElementB = getIdMap().get(getNameWorkflow()).get(
				params.get("paramInId"));

		logger.info("idElementA " + idElementA);
		logger.info("idElementB " + idElementB);

		try {
			linkPossibilities = new ArrayList<SelectItem>();
			nbLinkPossibilities = 0;

			DataFlow df = getDf();

			DataFlowElement dfeObjA = df.getElement(idElementA);
			DataFlowElement dfeObjB = df.getElement(idElementB);

			for (Map.Entry<String, DFELinkProperty> entryInput : dfeObjB.getInput().entrySet()){
				for (Map.Entry<String, DFEOutput> entryOutput : dfeObjA.getDFEOutput().entrySet()){



					logger.info("entryInput " + entryInput);
					logger.info("entryOutput " + entryOutput);

					if (df.check(entryOutput.getKey(), dfeObjA.getComponentId(), entryInput.getKey(), dfeObjB.getComponentId())){
						linkPossibilities.add(new SelectItem(entryOutput.getKey()+" -> "+entryInput.getKey()));


					}
				}
			}

			if (!linkPossibilities.isEmpty()) {
				setSelectedLink(linkPossibilities.get(0).getValue().toString());
				nbLinkPossibilities = linkPossibilities.size();
			}

		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * removeLink
	 * 
	 * Method for remove Link for two elements
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void removeLink() {
		logger.info("Remove link");

		try {
			Map<String, String> params = FacesContext.getCurrentInstance()
					.getExternalContext().getRequestParameterMap();
			String idElementA = getIdMap().get(getNameWorkflow()).get(
					params.get("paramOutId"));
			String idElementB = getIdMap().get(getNameWorkflow()).get(
					params.get("paramInId"));
			String nameElementA = params.get("paramOutName");
			String nameElementB = params.get("paramInName");

			getDf().removeLink(nameElementA, idElementA, nameElementB,
					idElementB);

		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * load
	 * 
	 * Method to load a workflow
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void load() {

		String path = getPath();

		logger.info("load " + path);

		DataFlowInterface dfi;
		try {
			dfi = getworkFlowInterface();

			String newWfName = generateWorkflowName(path);
			String error = dfi.addWorkflow(newWfName);

			if (error != null) {
				MessageUseful.addErrorMessage(error);
				HttpServletRequest request = (HttpServletRequest) FacesContext
						.getCurrentInstance().getExternalContext().getRequest();
				request.setAttribute("msnError", "msnError");
				return;
			}
			if (!getWorkflowMap().containsKey(newWfName)) {
				logger.error("A key is not available on the back-end but on the front-end!");
			}

			DataFlow df = dfi.getWorkflow(newWfName);
			error = df.read(path);
			logger.info("load error " + error);

			if (error != null) {
				dfi.removeWorkflow(newWfName);
				MessageUseful.addErrorMessage(error);
				HttpServletRequest request = (HttpServletRequest) FacesContext
						.getCurrentInstance().getExternalContext().getRequest();
				request.setAttribute("msnError", "msnError");
			} else {
				setNameWorkflow(newWfName);
				setDf(df);
				workflowMap.put(getNameWorkflow(), df);
				getIdMap().put(getNameWorkflow(), new HashMap<String, String>());

				for (DataFlowElement e : df.getElement()){
					getIdMap().get(getNameWorkflow()).put(e.getComponentId(), e.getComponentId());


				}
			}

		} catch (Exception e) {
			logger.info("Error loading workflow");
			e.printStackTrace();
		}
	}

	/**
	 * Push the object position on the backend
	 */
	protected void updatePosition() {

		logger.info("updatePosition");

		String positions = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap().get("positions");
		try {
			JSONObject positionsArray = new JSONObject(positions);
			Iterator it = positionsArray.keys();
			while (it.hasNext()) {
				String groupId = (String) it.next();
				Object objc = positionsArray.get(groupId);

				JSONArray elementArray = new JSONArray(objc.toString());

				if (!groupId.equalsIgnoreCase("legend")) {
					updatePosition(groupId, elementArray.get(0).toString(),
							elementArray.get(1).toString());
				}

			}
		} catch (JSONException e) {
			logger.info("Error updating positions");
			e.printStackTrace();
		}
	}

	/**
	 * Push the object position on the backend for all workflows
	 */
	public void updateAllPosition() {
		String allPositions = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap()
				.get("allpositions");
		logger.info(allPositions);
		logger.info(workflowMap.keySet());
		try {
			JSONObject allPositionsArray = new JSONObject(allPositions);
			Iterator itWorkflow = allPositionsArray.keys();
			while (itWorkflow.hasNext()) {
				String workflowId = (String) itWorkflow.next();
				JSONObject positionsArray = new JSONObject(allPositionsArray
						.get(workflowId).toString());
				Iterator it = positionsArray.keys();
				while (it.hasNext()) {
					String groupId = (String) it.next();
					Object objc = positionsArray.get(groupId);

					JSONArray elementArray = new JSONArray(objc.toString());
					logger.info("Update :" + workflowId + " " + groupId + " "
							+ elementArray.get(0).toString() + " "
							+ elementArray.get(1).toString());

					if (!groupId.equalsIgnoreCase("legend")) {
						updatePosition(workflowId, groupId, elementArray.get(0)
								.toString(), elementArray.get(1).toString());
					}

				}
			}
		} catch (JSONException e) {
			logger.info("Error updating positions");
			e.printStackTrace();
		}
	}

	public void backupAll() {
		logger.info("backupAll");
		updateAllPosition();
		try {
			getworkFlowInterface().backupAll();

		} catch (RemoteException e) {
			logger.info("Error backing up all workflows");
			e.printStackTrace();
			;
		}
	}

	/**
	 * save
	 * 
	 * Method to save the workflow
	 * 
	 * @return
	 * @author Igor.Souza
	 * @throws JSONException
	 */
	@SuppressWarnings("rawtypes")
	public void save() {

		logger.info("save");
		String msg = null;
		//Set path
		path = FacesContext.getCurrentInstance().getExternalContext().
				getRequestParameterMap().get("pathFile");

		if(!path.contains(".")){


			path += ".rs";
		}
		// Update the object positions
		updatePosition();
		{
			String nameWorkflowSwp = generateWorkflowName(path);
			try {
				msg = getworkFlowInterface().renameWorkflow(nameWorkflow,
						nameWorkflowSwp);
			} catch (RemoteException e) {
				msg = "Error when renaming workflow";
				logger.error("Error when renaming workflow: " + e);
			}
			if (msg == null && !nameWorkflowSwp.equals(nameWorkflow)) {
				workflowMap.put(nameWorkflowSwp, workflowMap.get(nameWorkflow));
				workflowMap.remove(nameWorkflow);
				// idMap.put(nameWorkflowSwp, idMap.get(nameWorkflow));
				idMap.put(nameWorkflowSwp, new HashMap<String, String>());
				idMap.remove(nameWorkflow);
				nameWorkflow = nameWorkflowSwp;
			}
		}
		if (msg == null) {
			try {
				logger.info("save workflow " + nameWorkflow + " in " + path);
				DataFlow df = getWorkflowMap().get(nameWorkflow);
				setDf(df);
				msg = df.save(path);
				logger.info("save msg :" + msg);
			} catch (Exception e) {
				logger.info("Error saving workflow");
				e.printStackTrace();
			}
		}
		if (msg != null) {
			MessageUseful.addErrorMessage(msg);
			HttpServletRequest request = (HttpServletRequest) FacesContext
					.getCurrentInstance().getExternalContext().getRequest();
			request.setAttribute("msnError", "msnError");
		}
	}

	/**
	 * closeWorkflow
	 * 
	 * Method to close a workflow
	 * 
	 * @return
	 * @author Igor.Souza
	 * @throws RemoteException
	 */
	public void closeWorkflow() {
		String workflow = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap().get("workflow");
		closeWorkflow(workflow);
	}

	protected void closeWorkflow(String workflowName) {
		logger.info("closeWorkflow:" + workflowName);

		try {
			DataFlow dfCur = workflowMap.get(workflowName);
			if (dfCur != null) {
				dfCur.close();
				getworkFlowInterface().removeWorkflow(workflowName);
				workflowMap.remove(workflowName);
				idMap.remove(workflowName);
				if (getDf() != null) {
					if (dfCur.getName() != null
							&& dfCur.getName().equals(getDf().getName())) {
						setDf(null);
					}
				}
			}

		} catch (RemoteException e) {
			logger.error("Fail closing " + workflowName);
			e.printStackTrace();
		}
	}

	public void runWorkflow() throws Exception {
		logger.info("runWorkflow");

		getDf().setName(getNameWorkflow());

		// Back up the project
		try {
			updatePosition();
			df.backup();
		} catch (RemoteException e) {
			e.printStackTrace();
		}

		String select = FacesContext.getCurrentInstance().getExternalContext()
				.getRequestParameterMap().get("select");
		logger.info("Select: " + select);

		String error = null;
		if (select == null || select.isEmpty() || select.equals("undefined")) {
			logger.info("Run a complete workflow");
			error = getDf().run();
			logger.info("Run error:" + error);
		} else {
			List<String> elements = new LinkedList<String>();
			String[] groupIds = select.split(",");
			for (String groupId : groupIds) {
				elements.add(idMap.get(getNameWorkflow()).get(groupId));
			}
			logger.info("Run workflow for: " + elements);
			if (elements.contains(null)) {
				error = "Dev - Error front-end, list contains null values.";
			} else {
				error = getDf().run(elements);
				logger.info("Run elements error:" + error);
			}
		}
		if (error != null) {
			logger.error(error);
			MessageUseful.addErrorMessage(error);
			HttpServletRequest request = (HttpServletRequest) FacesContext
					.getCurrentInstance().getExternalContext().getRequest();
			request.setAttribute("msnError", "msnError");
		}

		FacesContext context = FacesContext.getCurrentInstance();
		userInfoBean = (UserInfoBean) context.getApplication()
				.evaluateExpressionGet(context, "#{userInfoBean}",
						UserInfoBean.class);

		if (userInfoBean.getCurrentValue() < 98) {
			userInfoBean.setCurrentValue(userInfoBean.getCurrentValue() + 3);
		}
	}

	public void blockRunningWorkflow() throws Exception {
		if (getDf() != null) {
			while (getDf().isrunning()) {
				Thread.sleep(500);
			}
		}
	}

	public String getWorkflowUrl() {

		logger.info("getWorkflowUrl");
		String url = null;
		try {
			DataFlow df = getDf();
			if (df != null) {
				if (df.getOozieJobId() != null) {
					try {
						JobManager jm = getOozie();
						jm.getUrl();
						url = jm.getConsoleUrl(df);
					} catch (Exception e) {
						logger.error("error " + e.getMessage());
					}
				} else {
					url = null;
				}
			}
		} catch (Exception e) {
			logger.error("error get df: " + e.getMessage());
		}

		if (url == null) {
			try {
				url = getOozie().getUrl();
			} catch (RemoteException e) {
				logger.error("error getting Oozie url : " + e.getMessage());
			}
		}

		FacesContext context = FacesContext.getCurrentInstance();
		userInfoBean = (UserInfoBean) context.getApplication()
				.evaluateExpressionGet(context, "#{userInfoBean}",
						UserInfoBean.class);

		userInfoBean.setCurrentValue(Long.valueOf(100));

		return url;
	}

	public void calcWorkflowElementUrl(){
		String id = FacesContext.getCurrentInstance().getExternalContext().
				getRequestParameterMap().get("groupId");
		logger.info("element gp url: "+id);
		String url = null;
		try {
			DataFlow df = getDf();
			if (df != null && id != null && df.getOozieJobId() != null) {
				try {
					JobManager jm = getOozie();
					logger.info("element url: "+df.getElement(getIdMap().get(getNameWorkflow()).get(id)));
					url = jm.getConsoleElementUrl(df, df.getElement(getIdMap().get(getNameWorkflow()).get(id)));
				} catch (Exception e) {
					logger.error("error " + e.getMessage());
				}
			}
		} catch (Exception e) {
			logger.error("error get df: " + e.getMessage());
		}

		if (url == null) {
			try {
				url = getOozie().getUrl();
			} catch (RemoteException e) {
				logger.error("error getting Oozie url : " + e.getMessage());
			}
		}
		setWorkflowElementUrl(url);
	}

	public void updateIdObj() {
		String groupId = FacesContext.getCurrentInstance().getExternalContext()
				.getRequestParameterMap().get("idGroup");

		String id = FacesContext.getCurrentInstance().getExternalContext()
				.getRequestParameterMap().get("id");
		getIdMap().get(getNameWorkflow()).put(groupId, id);

	}

	public void reinitialize() throws RemoteException {
		logger.info("invalidate session");

		for (java.util.Map.Entry<String, DataFlow> e : getWorkflowMap()
				.entrySet()) {
			if (getworkFlowInterface().getWorkflow(e.getKey()) != null) {
				logger.info("removing workflow");
				getworkFlowInterface().removeWorkflow(e.getKey());

			}
		}

		getworkFlowInterface().addWorkflow("canvas-1");
		setDf(getworkFlowInterface().getWorkflow("canvas-1"));

		getWorkflowMap().clear();
		getWorkflowMap().put(getNameWorkflow(), getDf());

		getIdMap().clear();
		getIdMap().put(getNameWorkflow(), new HashMap<String, String>());

	}

	/**
	 * openCanvas
	 * 
	 * Methods to clean the outputs from all canvas
	 * 
	 * @return
	 * @author Igor.Souza
	 * @throws RemoteException
	 */
	public void cleanCanvasProject() throws RemoteException {

		DataFlow wf = getworkFlowInterface().getWorkflow(getNameWorkflow());
		String error = wf.cleanProject();
		if (error != null) {
			MessageUseful.addErrorMessage(error);
			HttpServletRequest request = (HttpServletRequest) FacesContext
					.getCurrentInstance().getExternalContext().getRequest();
			request.setAttribute("msnError", "msnError");
		}

	}

	public void regeneratePathsProjectCopy() throws RemoteException {
		logger.info("regenerate paths project copy");
		regeneratePathsProject(true);
	}

	public void regeneratePathsProjectMove() throws RemoteException {
		logger.info("regenerate paths project move");
		regeneratePathsProject(false);
	}

	/**
	 * 
	 * Methods to regenerate paths of the current workflow
	 * 
	 * @return
	 * @author Igor.Souza
	 * @throws RemoteException
	 */
	public void regeneratePathsProject(boolean copy) throws RemoteException {

		DataFlow wf = getworkFlowInterface().getWorkflow(getNameWorkflow());
		String error = wf.regeneratePaths(copy);
		if (error != null) {
			MessageUseful.addErrorMessage(error);
			HttpServletRequest request = (HttpServletRequest) FacesContext
					.getCurrentInstance().getExternalContext().getRequest();
			request.setAttribute("msnError", "msnError");
		}

	}

	public void openChangeIdModal() throws RemoteException {
		logger.info("openChangeIdModal");
		setIdElement(getIdMap().get(getNameWorkflow()).get(getIdGroup()));
	}

	public void changeIdElement() throws RemoteException {
		logger.info("id new -> " + getIdElement());
		String oldId = getIdMap().get(getNameWorkflow()).get(getIdGroup());
		getIdMap().get(getNameWorkflow()).put(getIdGroup(), getIdElement());
		getDf().changeElementId(oldId, getIdElement());
	}

	/**
	 * initial
	 * 
	 * Methods to drive to the main screen
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public String initial() {

		logger.info("initial");

		return "initial";
	}

	private String generateWorkflowName(String path) {
		String name;
		int index = path.lastIndexOf("/");
		if (index + 1 < path.length()) {
			name = path.substring(index + 1);
		} else {
			name = path;
		}
		return name.replace(".rs", "");
	}

	public void changeWorkflow() throws RemoteException {

		logger.info(getNameWorkflow());
		setDf(getWorkflowMap().get(getNameWorkflow()));
	}

	public void addWorkflow() throws RemoteException {

		Map<String, String> params = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap();
		String name = params.get("name");

		logger.info("addWorkflow: " + name);

		DataFlowInterface dfi = getworkFlowInterface();

		if (!getWorkflowMap().containsKey(name)) {
			dfi.addWorkflow(name);
			workflowMap.put(name, dfi.getWorkflow(name));
			getIdMap().put(name, new HashMap<String, String>());
		}

	}

	public void closeAll() {
		logger.info("closeAll");
		Iterator<String> workflowIt = workflowMap.keySet().iterator();
		while (workflowIt.hasNext()) {
			closeWorkflow(workflowIt.next());
		}
		setDf(null);
	}

	public String[][] getAllOutputStatus() throws Exception {

		logger.info("getAllOutputStatus");

		logger.info("getAllOutputStatus nameWorkflow " + getNameWorkflow());

		String[][] result = new String[getIdMap().get(getNameWorkflow()).size()][];

		int i = 0;
		for (Entry<String, String> el : getIdMap().get(getNameWorkflow())
				.entrySet()) {
			DataFlowElement df = getDf().getElement(el.getValue());
			result[i++] = getOutputStatus(df, el.getKey());

		}

		return result;
	}

	private String[] getOutputStatus(DataFlowElement dfe, String groupId)
			throws RemoteException {
		String state = null;
		String pathExistsStr = null;

		if (dfe != null && dfe.getDFEOutput() != null) {
			boolean pathExists = false;
			for (Entry<String, DFEOutput> e : dfe.getDFEOutput().entrySet()) {

				String stateCur = e.getValue().getSavingState().toString();

				logger.info("path: " + e.getValue().getPath());

				pathExists |= e.getValue().isPathExists();
				if (stateCur != null) {
					if (state == null) {
						state = stateCur;
					} else if (state.equalsIgnoreCase(SavingState.BUFFERED
							.toString())
							&& stateCur.equalsIgnoreCase(SavingState.RECORDED
									.toString())) {
						state = stateCur;
					} else if (state.equalsIgnoreCase(SavingState.TEMPORARY
							.toString())
							&& (stateCur.equalsIgnoreCase(SavingState.RECORDED
									.toString()) || stateCur
									.equalsIgnoreCase(SavingState.BUFFERED
											.toString()))) {
						state = stateCur;
					}
				}

			}
			if (!dfe.getDFEOutput().isEmpty()) {
				pathExistsStr = String.valueOf(pathExists);
			}

			logger.info("element " + dfe.getComponentId());
			logger.info("state " + state);
			logger.info("pathExists " + String.valueOf(pathExistsStr));
		}
		logger.info("output status result " + groupId + " - " + state + " - "
				+ pathExistsStr);
		return new String[] { groupId, state, pathExistsStr };
	}

	/**
	 * Recursive function to get the output status of all the elements after the
	 * one specified
	 * 
	 * @param dfe
	 *            The data flow element
	 * @param status
	 *            the list to append the result
	 * @throws RemoteException
	 */
	private void getOutputStatus(DataFlowElement dfe, List<String[]> status)
			throws RemoteException {

		if (dfe != null && dfe.getDFEOutput() != null) {
			String compId = dfe.getComponentId();
			if (compId == null) {
				logger.info("Error component id cannot be null");
			} else {
				String groupId = null;
				Map<String, String> mapIdW = getIdMap().get(getNameWorkflow());
				for (Entry<String, String> e : mapIdW.entrySet()) {
					if (compId.equals(e.getValue())) {
						groupId = e.getKey();
					}
				}
				if (groupId == null) {
					logger.info("Error getting status: " + compId);
				} else {
					status.add(getOutputStatus(dfe, groupId));

					for (DataFlowElement cur : dfe.getAllOutputComponent()) {
						getOutputStatus(cur, status);
					}
				}
			}
		}

	}

	public String[][] getOutputStatus() throws Exception {

		logger.info("getOutputStatus");

		Map<String, String> params = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap();
		String groupId = params.get("groupId");

		DataFlowElement df = getDf().getElement(getIdElement(groupId));

		List<String[]> status = new LinkedList<String[]>();
		getOutputStatus(df, status);
		String[][] ans = new String[status.size()][];
		int i = 0;
		for (String[] elStat : status) {
			ans[i++] = elStat;
		}

		return ans;
	}

	public String[][] getRunningStatus() throws Exception {

		logger.info("getRunningStatus");

		String[][] result = new String[getIdMap().get(getNameWorkflow()).size()][];

		int i = 0;
		for (Entry<String, String> e : getIdMap().get(getNameWorkflow())
				.entrySet()) {

			DataFlowElement cur = getDf().getElement(e.getValue());
			if (cur == null) {
				String msg = "Element " + e.getValue() + " does not exist.";
				logger.warn(msg);
				MessageUseful.addErrorMessage(msg);
			} else {
				String status = getOozie().getElementStatus(getDf(), cur);

				logger.info(e.getKey() + " - " + status);

				String pathExistsStr = null;
				if (cur != null) {
					boolean pathExists = false;
					for (Entry<String, DFEOutput> e2 : cur.getDFEOutput()
							.entrySet()) {

						logger.info("path: " + e2.getValue().getPath());

						pathExists |= e2.getValue().isPathExists();

					}
					if (!cur.getDFEOutput().isEmpty()) {
						pathExistsStr = String.valueOf(pathExists);
					}
				}

				result[i++] = new String[] { e.getKey(), status, pathExistsStr };
			}
		}

		return result;
	}

	public String[] getArrowType(String groupOutId, String groupInId,
			String outputName) throws Exception {

		DataFlowElement df = getDf().getElement(
				getIdMap().get(getNameWorkflow()).get(groupOutId));

		String color = null;
		String typeName = null;
		for (Entry<String, DFEOutput> e : df.getDFEOutput().entrySet()) {
			if (e.getKey().equals(outputName)) {
				color = e.getValue().getColour();
				typeName = e.getValue().getTypeName();
				logger.info(e.getKey() + " - " + color);
				break;
			}
		}

		logger.info("getArrowType " + color + " " + typeName);

		return new String[] { groupOutId, groupInId, color, typeName };
	}

	public String[] getArrowType() throws Exception {

		logger.info("getArrowType");

		Map<String, String> params = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap();
		String groupOutId = params.get("groupOutId");
		String groupInId = params.get("groupInId");
		String outputName = params.get("outputName");

		logger.info("getArrowType " + groupOutId + " " + groupInId + " "
				+ outputName);

		return getArrowType(groupOutId, groupInId, outputName);
	}

	public String[] getAllArrowType() throws Exception {

		logger.info("getAllArrowType");

		Map<String, String> params = FacesContext.getCurrentInstance()
				.getExternalContext().getRequestParameterMap();
		String groupOutId = params.get("groupOutId");
		String groupInId = params.get("groupInId");
		String outputName = "";
		try{
			DataFlowElement df = getDf().getElement(getIdMap().get(getNameWorkflow()).get(groupOutId));
			for (String value : df.getOutputComponent().keySet()) {
				outputName = value;
				break;
			}
		}catch(Exception e){
			MessageUseful.addErrorMessage("Error when updating link colors: NULL POINTER"); //FIXME
			HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
			request.setAttribute("msnError", "msnError");


		}

		logger.info("getAllArrowType " + groupOutId + " " + groupInId + " "
				+ outputName);

		return getArrowType(groupOutId, groupInId, outputName);
	}

	public String getIdElement(String idGroup) {
		return getIdMap().get(getNameWorkflow()).get(idGroup);
	}

	public DataFlow getDf() {
		return df;
	}

	public void setDf(DataFlow df) {
		this.df = df;
	}

	public String getNameWorkflow() {
		return nameWorkflow;
	}

	public void setNameWorkflow(String nameWorkflow) {
		if (nameWorkflow != null && nameWorkflow.equals("undefined")) {
			return;
		}
		this.nameWorkflow = nameWorkflow;
	}

	public String getParamOutId() {
		return paramOutId;
	}

	public void setParamOutId(String paramOutId) {
		this.paramOutId = paramOutId;
	}

	public String getParamInId() {
		return paramInId;
	}

	public void setParamInId(String paramInId) {
		this.paramInId = paramInId;
	}

	public String getParamNameLink() {
		return paramNameLink;
	}

	public void setParamNameLink(String paramNameLink) {
		this.paramNameLink = paramNameLink;
	}

	public String[] getResult() {
		return result;
	}

	public void setResult(String[] result) {
		this.result = result;
	}

	public List<SelectItem> getLinkPossibilities() {
		return linkPossibilities;
	}

	public void setLinkPossibilities(List<SelectItem> linkPossibilities) {
		this.linkPossibilities = linkPossibilities;
	}

	public String getSelectedLink() {
		return selectedLink;
	}

	public void setSelectedLink(String selectedLink) {
		this.selectedLink = selectedLink;
	}

	public Map<String, Map<String, String>> getIdMap() {
		return idMap;
	}

	public void setIdMap(Map<String, Map<String, String>> idMap) {
		this.idMap = idMap;
	}

	public String getIdElement() {
		return idElement;
	}

	public void setIdElement(String idElement) {
		this.idElement = idElement;
	}

	public String getIdGroup() {
		return idGroup;
	}

	public void setIdGroup(String idGroup) {
		this.idGroup = idGroup;
	}

	public Map<String, DataFlow> getWorkflowMap() {
		return workflowMap;
	}

	public void setWorkflowMap(Map<String, DataFlow> workflowMap) {
		this.workflowMap = workflowMap;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String[] getPositions() throws Exception {

		logger.info("getPositions");

		JSONArray jsonElements = new JSONArray();
		for (DataFlowElement e : getDf().getElement()) {

			jsonElements.put(new Object[] { e.getComponentId(), e.getName(),
					LocalFileSystem.relativize(getCurrentPage(),e.getImage()), e.getX(), e.getY() });

		}

		JSONArray jsonLinks = new JSONArray();
		for (DataFlowElement e : getDf().getElement()) {
			for (Map.Entry<String, List<DataFlowElement>> entry : e
					.getInputComponent().entrySet()) {
				for (DataFlowElement dfe : entry.getValue()) {
					jsonLinks.put(new Object[] { dfe.getComponentId(),
							e.getComponentId() });
				}
			}
		}

		logger.info("getPositions getNameWorkflow " + getNameWorkflow());
		logger.info("getPositions getPath " + getPath());
		logger.info("getPositions jsonElements.toString "
				+ jsonElements.toString());
		logger.info("getPositions jsonLinks.toString " + jsonLinks.toString());

		return new String[] { getNameWorkflow(), getPath(),
				jsonElements.toString(), jsonLinks.toString() };
	}

	public UserInfoBean getUserInfoBean() {
		return userInfoBean;
	}

	public void setUserInfoBean(UserInfoBean userInfoBean) {
		this.userInfoBean = userInfoBean;
	}

	/**
	 * @return the nbLinkPossibilities
	 */
	public int getNbLinkPossibilities() {
		return nbLinkPossibilities;
	}

	/**
	 * @param nbLinkPossibilities
	 *            the nbLinkPossibilities to set
	 */
	public void setNbLinkPossibilities(int nbLinkPossibilities) {
		this.nbLinkPossibilities = nbLinkPossibilities;
	}

	public String getNameOutput() {
		return nameOutput;
	}

	public void setNameOutput(String nameOutput) {
		this.nameOutput = nameOutput;
	}

	public String getLinkLabel() {
		return linkLabel;
	}

	public void setLinkLabel(String nameLink) {
		this.linkLabel = nameLink;
	}

	public String getErrorTableState() {
		return errorTableState;
	}

	public void setErrorTableState(String errorTableState) {
		this.errorTableState = errorTableState;
	}

	/**
	 * @return the changeElementOnly
	 */
	public boolean isChangeElementOnly() {
		return changeElementOnly;
	}

	/**
	 * @param changeElementOnly the changeElementOnly to set
	 */
	public void setChangeElementOnly(boolean changeElementOnly) {
		this.changeElementOnly = changeElementOnly;
	}

	/**
	 * @return the workflowElementUrl
	 */
	public String getWorkflowElementUrl() {
		return workflowElementUrl;
	}

	/**
	 * @param workflowElementUrl the workflowElementUrl to set
	 */
	public void setWorkflowElementUrl(String workflowElementUrl) {
		this.workflowElementUrl = workflowElementUrl;
	}

}
