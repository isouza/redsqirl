package idiro.workflow.server.action;

import idiro.utils.Tree;
import idiro.workflow.server.Page;
import idiro.workflow.server.UserInteraction;
import idiro.workflow.server.connect.HiveInterface;
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
 * Action to join several tables.
 * 
 * @author etienne
 *
 */
public class HiveJoin extends HiveElement{

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
	
	private TableJoinInteraction tJoinInt;
	private JoinRelationInteraction jrInt;
	private DFEInteraction joinTypeInt;
	
	
	public HiveJoin() throws RemoteException {
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


		partInt = new PartitionInteraction(
				key_partitions,
				"Please specify the partitions",
				0,
				1);
		
		condInt = new ConditionInteraction(key_condition,
				"Please specify the condition of the select",
				0,
				2, 
				this, 
				key_input);

		page1.addInteraction(joinTypeInt);
		page1.addInteraction(partInt);
		page1.addInteraction(condInt);

		page2 = addPage("Relationship",
				"Join Relationship",
				1);

		jrInt = new JoinRelationInteraction(
				key_joinRelation,
				"Please specify the relationship, top to bottom is like left to right", 
				0,
				0,
				this);
		
		page2.addInteraction(jrInt);
		
		page3 = addPage("Operations",
				"Join operations",
				1);
		
		tJoinInt = new TableJoinInteraction(
				key_featureTable,
				"Please specify the operations to be executed for each feature",
				0,
				0,
				this);
		
		page3.addInteraction(tJoinInt);
		
	}

	@Override
	public String getName() throws RemoteException {
		return "hive_join";
	}

	@Override
	public void update(DFEInteraction interaction) throws RemoteException {
		if(interaction == condInt){
			condInt.update();
		}else if(interaction == partInt){
			partInt.update();
		}else if(interaction == joinTypeInt){
			updateJoinType();
		}else if(interaction == jrInt){
			jrInt.update();
		}else if(interaction == tJoinInt){
			addOrRemoveOutPage();
			tJoinInt.update();
		}else if(typeOutputInt != null && interaction == typeOutputInt){
			updateType();
		}
	}
	

	public void updateJoinType() throws RemoteException{

		Tree<String> list = null;
		if(joinTypeInt.getTree().getSubTreeList().isEmpty()){
			list = joinTypeInt.getTree().add("list");
			list.add("output").add("JOIN");
			Tree<String> value = list.add("value");
			value.add("JOIN");
			value.add("LEFT OUTER JOIN");
			value.add("RIGHT OUTER JOIN");
			value.add("FULL OUTER JOIN");
		}
	}
	
	@Override
	public String getQuery() throws RemoteException{

		HiveInterface hInt = new HiveInterface();
		String query = null;
		if(getDFEInput() != null){
			//Output
			DFEOutput out = output.values().iterator().next();
			String tableOut = hInt.getTableAndPartitions(out.getPath())[0];
			
			String insert = "INSERT OVERWRITE TABLE "+tableOut+partInt.getQueryPiece(out);
			String from = " FROM "+jrInt.getQueryPiece()+" ";
			String create = "CREATE TABLE IF NOT EXISTS "+tableOut;
			String createPartition = partInt.getCreateQueryPiece(out);
			String where = condInt.getQueryPiece();

			String select = tJoinInt.getQueryPiece();
			String createSelect = tJoinInt.getCreateQueryPiece();
			
			
			if(select.isEmpty()){
				logger.debug("Nothing to select");
			}else{
				query = create+"\n"+
						createSelect+"\n"+
						createPartition+";\n\n";
				
				query += insert+"\n"+
						select+"\n"+
						from+"\n"+
						where+";";
			}
		}

		return query;
	}


	public Map<String,FeatureType> getInFeatures() throws RemoteException{
		Map<String,FeatureType> ans = 
				new LinkedHashMap<String,FeatureType>();
		HiveInterface hInt = new HiveInterface();
		List<DFEOutput> lOut = getDFEInput().get(HiveJoin.key_input);
		Iterator<DFEOutput> it = lOut.iterator();
		while(it.hasNext()){
			DFEOutput out = it.next();
			String tableName = hInt.getTableAndPartitions(out.getPath())[0];
			Map<String,FeatureType> mapTable = out.getFeatures();
			Iterator<String> itFeat = mapTable.keySet().iterator();
			while(itFeat.hasNext()){
				String cur = itFeat.next();
				ans.put(tableName+"."+cur, mapTable.get(cur));
			}
		}
		return ans; 
	}
	
	/**
	 * @return the tJoinInt
	 */
	public final TableJoinInteraction gettJoinInt() {
		return tJoinInt;
	}

	/**
	 * @return the jrInt
	 */
	public final JoinRelationInteraction getJrInt() {
		return jrInt;
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