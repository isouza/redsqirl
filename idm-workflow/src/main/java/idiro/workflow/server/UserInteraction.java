package idiro.workflow.server;

import idiro.utils.Tree;
import idiro.utils.TreeNonUnique;
import idiro.workflow.server.enumeration.DisplayType;
import idiro.workflow.server.interfaces.DFEInteraction;
import idiro.workflow.server.interfaces.DFEInteractionChecker;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A User Interaction, 
 * is a variable that the user may fill out
 * through a graphical interface.
 * 
 * The programmer has to choose between a fixe
 * list of interface available. The result is 
 * return in a TreeNonUnique object (@see {@link TreeNonUnique} 
 * which can correspond to an xml file loaded in memory.
 * 
 * @author etienne
 *
 */
public class UserInteraction extends UnicastRemoteObject implements DFEInteraction{

	/**
	 * 
	 */
	private static final long serialVersionUID = 539841111561345129L;

	protected static Logger logger = Logger.getLogger(UserInteraction.class);

	/**
	 * The type of display
	 */
	protected DisplayType display;

	/**
	 * The name of the interaction
	 */
	protected String name;
	/**
	 * The legend associated to the interaction
	 */
	protected String legend;

	/**
	 * The column to display the interaction on the page
	 */
	protected int column;

	/**
	 * The place in the column of the interaction
	 */
	protected int placeInColumn;

	/**
	 * The tree where the specific input is stored
	 */
	protected Tree<String> tree;


	protected DFEInteractionChecker checker = null;

	/**
	 * Unique constructor
	 * @param name
	 * @param display
	 * @param column
	 * @param placeInColumn
	 * @throws RemoteException 
	 */
	public UserInteraction(String name, String legend,DisplayType display, int column, int placeInColumn) throws RemoteException{
		super();
		this.name = name;
		this.tree = new TreeNonUnique<String>(name);
		this.legend = legend;
		this.display = display;
		this.column = column;
		this.placeInColumn = placeInColumn;
		logger.debug("Init interaction "+name);
	}


	@Override
	public void writeXml(Document doc, Node n) throws DOMException, RemoteException {
		n.appendChild(writeXml(doc,getTree()));
	}

	protected Node writeXml(Document doc, Tree<String> t) throws RemoteException, DOMException {
		Node elHead = null;
		if(getTree().getSubTreeList().isEmpty()){
			elHead = doc.createTextNode(t.getHead());
		}else{

			elHead = doc.createElement(t.getHead().toString());
			Iterator<Tree<String>> it = t.getSubTreeList().iterator();
			while(it.hasNext()){
				elHead.appendChild(writeXml(doc,it.next()));
			}
		}
		return elHead;
	}


	public void readXml(Node n) throws Exception{
		try{
			this.tree = new TreeNonUnique<String>(name);
			if(n.getNodeType() == Node.ELEMENT_NODE){
				NodeList nl = n.getChildNodes();
				
				for(int i = 0; i < nl.getLength();++i){
					Node cur = nl.item(i);
					
					if(cur.getNodeType() == Node.TEXT_NODE){
						tree.add(cur.getNodeName());
					}else if(cur.getNodeType() == Node.ELEMENT_NODE){
						readXml(cur,tree.add(cur.getNodeName()));
					}
				}
			}
		}catch(Exception e){
			logger.warn("Have to reset the tree...");
			this.tree = new TreeNonUnique<String>(name);
			throw e;
		}

	}

	protected void readXml(Node n,Tree<String> curTree) throws RemoteException, DOMException{
		NodeList nl = n.getChildNodes();
		for(int i = 0; i < nl.getLength();++i){
			Node curNode = nl.item(i);
			if(curNode.getNodeType() == Node.TEXT_NODE){
				curTree.add(curNode.getNodeValue());
			}else if(curNode.getNodeType() == Node.ELEMENT_NODE){
				readXml(curNode,curTree.add(curNode.getNodeName()));
			}
		}
	}


	/**
	 * @return the display
	 */
	public DisplayType getDisplay() {
		return display;
	}


	/**
	 * @return the column
	 */
	public int getColumn() {
		return column;
	}


	/**
	 * @return the placeInColumn
	 */
	public int getPlaceInColumn() {
		return placeInColumn;
	}

	/**
	 * @return the inputToDisplay
	 */
	public final Tree<String> getTree() {
		return tree;
	}


	/**
	 * @param inputToDisplay the inputToDisplay to set
	 */
	public final void setTree(Tree<String> tree) {
		this.tree = tree;
	}

	/**
	 * @return the name
	 */
	public final String getName() {
		return name;
	}


	@Override
	public String check() throws RemoteException {
		String error = null;
		if(getChecker() != null){
			error = getChecker().check(this);
		}
		return error;
	}


	/**
	 * @return the checker
	 */
	public final DFEInteractionChecker getChecker() {
		return checker;
	}


	/**
	 * @param checker the checker to set
	 */
	public final void setChecker(DFEInteractionChecker checker) {
		this.checker = checker;
	}

}