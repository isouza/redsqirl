package idiro.workflow.server.action;

import idiro.utils.Tree;
import idiro.workflow.server.Page;
import idiro.workflow.server.UserInteraction;
import idiro.workflow.server.connect.HDFSInterface;
import idiro.workflow.server.enumeration.DisplayType;
import idiro.workflow.server.enumeration.FeatureType;
import idiro.workflow.server.interfaces.DFEInteraction;
import idiro.workflow.server.interfaces.DFEOutput;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Action to join several relations.
 * 
 * @author marcos
 *
 */
public class PigJoin extends PigElement{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3035179016090477413L;

	public static final String key_featureTable = "Features",
			key_joinType = "Join Type",
			key_joinRelation = "Join Relationship";

	private Page page1,
			page2,
			page3;
	
	private PigTableJoinInteraction tJoinInt;
	private PigJoinRelationInteraction jrInt;
	private PigFilterInteraction filterInt;
	private DFEInteraction joinTypeInt;
	
	
	public PigJoin() throws RemoteException {
		super(2,2,Integer.MAX_VALUE);

		page1 = addPage("Select",
				"Select Conditions",
				1);

		joinTypeInt = new UserInteraction(
				key_joinType,
				"Please specify a join type",
				DisplayType.list,
				0,
				0); 


		filterInt = new PigFilterInteraction(key_condition,
				"Please specify the condition of the select",
				0,
				1, 
				this, 
				key_input);

		page1.addInteraction(joinTypeInt);
		page1.addInteraction(filterInt);

		page2 = addPage("Relationship",
				"Join Relationship",
				1);

		jrInt = new PigJoinRelationInteraction(
				key_joinRelation,
				"Please specify the relationship, top to bottom is like left to right", 
				0,
				0,
				this);
		
		page2.addInteraction(jrInt);
		
		page3 = addPage("Operations",
				"Join operations",
				1);
		
		tJoinInt = new PigTableJoinInteraction(
				key_featureTable,
				"Please specify the operations to be executed for each feature",
				0,
				0,
				this);
		
		page3.addInteraction(tJoinInt);
		
		addOutputPage();
		
	}

//	@Override
	public String getName() throws RemoteException {
		return "pig_join";
	}

//	@Override
	public void update(DFEInteraction interaction) throws RemoteException {
		if(interaction == filterInt){
			filterInt.update();
		}else if(interaction == joinTypeInt){
			updateDelimiterOutputInt();
		}else if(interaction == jrInt){
			jrInt.update();
		}else if(interaction == tJoinInt){
			tJoinInt.update();
		}else if(interaction == dataSubtypeInt){
			updateDataSubTypeInt();
		}
	}
	

	public void updateDelimiterOutputInt() throws RemoteException{

		Tree<String> list = null;
		if(joinTypeInt.getTree().getSubTreeList().isEmpty()){
			list = joinTypeInt.getTree().add("list");
			list.add("output").add("");
			Tree<String> value = list.add("value");
			value.add("JOIN");
			value.add("LEFT OUTER JOIN");
			value.add("RIGHT OUTER JOIN");
			value.add("FULL OUTER JOIN");
		}
	}
	
	@Override
	public String getQuery() throws RemoteException{

		HDFSInterface hInt = new HDFSInterface();
		String query = null;
		if(getDFEInput() != null){
			//Output
			DFEOutput out = output.values().iterator().next();
			
			String load = "";
			for (DFEOutput in : getDFEInput().get(key_input)){
				load += hInt.getRelation(in.getPath()) + " = "+getLoadQueryPiece(in) + ";\n";
			}
			load +="\n";
			
			String remove = getRemoveQueryPiece(out.getPath())+"\n\n";
			
			String from = getCurrentName()+" = "+jrInt.getQueryPiece()+";\n\n";

			String select = tJoinInt.getQueryPiece(getCurrentName());
			if (!select.isEmpty()){
				select = getNextName()+" = "+select+";\n\n";
			}
			
			String filter = filterInt.getQueryPiece(getCurrentName());
			if (!filter.isEmpty()){
				filter = getNextName()+" = "+filter+";\n\n";
			}
			
			String store = getStoreQueryPiece(out, getCurrentName());
			
			if(select.isEmpty()){
				logger.debug("Nothing to select");
			}else{
				query = remove;
			
				query += load;
				
				query += from+
						select;
				
				query += filter;
				
				query += store;
			}
		}

		return query;
	}


	public Map<String,FeatureType> getInFeatures() throws RemoteException{
		Map<String,FeatureType> ans = 
				new LinkedHashMap<String,FeatureType>();
		HDFSInterface hInt = new HDFSInterface();
		List<DFEOutput> lOut = getDFEInput().get(PigJoin.key_input);
		Iterator<DFEOutput> it = lOut.iterator();
		while(it.hasNext()){
			DFEOutput out = it.next();
			String relationName = hInt.getRelation(out.getPath());
			Map<String,FeatureType> mapTable = out.getFeatures();
			Iterator<String> itFeat = mapTable.keySet().iterator();
			while(itFeat.hasNext()){
				String cur = itFeat.next();
				ans.put(relationName+"."+cur, mapTable.get(cur));
			}
		}
		return ans; 
	}
	
	/**
	 * @return the tJoinInt
	 */
	public final PigTableJoinInteraction gettJoinInt() {
		return tJoinInt;
	}

	/**
	 * @return the jrInt
	 */
	public final PigJoinRelationInteraction getJrInt() {
		return jrInt;
	}
	
	/**
	 * @return the condInt
	 */
	public final PigFilterInteraction getCondInt() {
		return filterInt;
	}

	/**
	 * @return the joinTypeInt
	 */
	public final DFEInteraction getJoinTypeInt() {
		return joinTypeInt;
	}

	@Override
	public Map<String, FeatureType> getNewFeatures() throws RemoteException {
		return tJoinInt.getNewFeatures();
	}
}