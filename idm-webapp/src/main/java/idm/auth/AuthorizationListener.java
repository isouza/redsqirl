package idm.auth;

import javax.faces.application.NavigationHandler;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

/** AuthorizationListener
 * 
 * Class to control the User and application. checks if the User logged in
 * 
 * @author Igor.Souza
 */
public class AuthorizationListener implements PhaseListener {
	
	private static Logger logger = Logger.getLogger(AuthorizationListener.class);

	/** afterPhase
	 * 
	 * Methods to generating screen
	 * 
	 * @return
	 * @author Igor.Souza
	 */
	public void afterPhase(PhaseEvent event) {
		
		FacesContext facesContext = event.getFacesContext();
		String currentPage = facesContext.getViewRoot().getViewId();
		HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
//		ServletContext sc = (ServletContext) facesContext.getExternalContext().getContext();
		
		boolean isLoginPage = (currentPage.lastIndexOf("initial.xhtml") > -1) || (currentPage.lastIndexOf("initial.jsf") > -1);
		HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);

		if(session==null){
			
			System.out.println("session null");
			
//			Map<String, HttpSession> sessionLoginMap = (Map<String, HttpSession>) sc.getAttribute("sessionLoginMap");
//			String userName = (String) sc.getAttribute("userName");
//			sessionLoginMap.remove(userName);
//			sc.removeAttribute("userName");
			
			if (!isLoginPage){
				request.setAttribute("msnLoginError", "msnLoginError");
			}
			
//			logger.info("request signOut: "+request.getAttribute("signOut"));
//			
//			if (request.getAttribute("signOut") == null){
//				DataFlowInterface dataFlowInterface = (DataFlowInterface) sc.getAttribute("wfm");
//				try {
//					logger.info(sc.getAttribute("signOut"));
//					DataFlow wf = dataFlowInterface.getWorkflow("canvas1");
//					String error = wf.cleanProject();
//					if(error != null){
//						MessageUseful.addErrorMessage(error);
//						request.setAttribute("msnError", "msnError");
//					}
//					
//				} catch (RemoteException e) {
//					e.printStackTrace();
//				}
//				request.removeAttribute("signOut");
//			}
			
			NavigationHandler nh = facesContext.getApplication().getNavigationHandler();
			nh.handleNavigation(facesContext, null, "loginPage");
			
		}else{
			Object currentUser = session.getAttribute("username");
			
			if (!isLoginPage && (currentUser == null || currentUser.equals(""))) {
				
				System.out.println(currentPage);
				
				request.setAttribute("msnLoginError", "msnLoginError");
				NavigationHandler nh = facesContext.getApplication().getNavigationHandler();
				nh.handleNavigation(facesContext, null, "loginPage");
			}
		}
	}
	
	/** beforePhase
	 * 
	 */
	public void beforePhase(PhaseEvent event) {

	}

	public PhaseId getPhaseId() {
		return PhaseId.RESTORE_VIEW;
	}
	
}