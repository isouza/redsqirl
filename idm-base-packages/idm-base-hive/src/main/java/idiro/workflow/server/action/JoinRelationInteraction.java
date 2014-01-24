package idiro.workflow.server.action;

import idiro.utils.FeatureList;
import idiro.utils.Tree;
import idiro.utils.TreeNonUnique;
import idiro.workflow.server.TableInteraction;
import idiro.workflow.server.UserInteraction;
import idiro.workflow.server.action.utils.HiveDictionary;
import idiro.workflow.server.connect.HiveInterface;
import idiro.workflow.server.enumeration.DisplayType;
import idiro.workflow.server.interfaces.DFEOutput;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Specify the relationship between joined tables.
 * The order is important as it will be the same 
 * in the SQL query.
 * 
 * @author etienne
 *
 */
public class JoinRelationInteraction extends TableInteraction{


	/**
	 * 
	 */
	private static final long serialVersionUID = 7384667815452362352L;

	private HiveJoin hj;

	public static final String table_table_title = "Table",
			table_feat_title = "Joining-condition";

	public JoinRelationInteraction(String name, String legend,
			int column, int placeInColumn, HiveJoin hj)
					throws RemoteException {
		super("",name, legend, column, placeInColumn);//FIXME 
		this.hj = hj;
	}

	@Override
	public String check() throws RemoteException{
		String msg = null;
		List<Tree<String>> lRow;
		Iterator<Tree<String>> rows;
		try{
			lRow = getTree()
					.getFirstChild("table").getChildren("row"); 
			rows = lRow.iterator();
		}catch(Exception e){
			msg = "The table needs to have one and only one row for each entry";
			logger.error(msg);
			return msg;
		}
		Set<String> tables = hj.getAliases().keySet();
		if(tables.size() != lRow.size()){
			msg = "The table needs to have one and only one row for each entry";
		}else{

			Set<String> featType = new LinkedHashSet<String>();
			FeatureList inFeats = hj.getInFeatures();
			int rowNb = 0;


			while(rows.hasNext() && msg == null){
				++rowNb;
				Tree<String> row = rows.next();
				try{
					String table = row.getFirstChild(table_table_title).getFirstChild().getHead();
					String rel = row.getFirstChild(table_feat_title).getFirstChild().getHead(); 
					String type = HiveDictionary.getInstance().getReturnType(
							rel,
							inFeats
							);
					
					if(type == null){
						msg = "row "+rowNb+": SQL code not correct";
					}else{
						featType.add(type);
					}
					

					Iterator<String> itTable = tables.iterator();
					while(itTable.hasNext() && msg == null){
						String curTab = itTable.next();
						if(rel.contains(curTab+".") &&
								!curTab.equalsIgnoreCase(table)){
							msg = "row "+rowNb+": Cannot have an operation with several table here";
						}
					}

				}catch(Exception e){
					msg = e.getMessage();
				}
			}

			if(msg == null && 
					featType.size() != 1){
				msg = "The features need to be all of same type";
			}
		}

		return msg;
	}


	public void update() throws RemoteException{

		Set<String> tablesIn = hj.getAliases().keySet();
		if(tree.getSubTreeList().isEmpty()){
			tree.add(getRootTable(tablesIn));		
		}else{
			//Remove constraint on first column
			tree.getFirstChild("table").getFirstChild("columns")
			.findFirstChild(table_table_title).getParent().remove("constraint");
			
			//Remove Editor of operation
			tree.getFirstChild("table").remove("generator");
			Tree<String> operation = tree.getFirstChild("table")
					.getFirstChild("columns").findFirstChild(table_feat_title).getParent();
			operation.remove("editor");
		}
		
		//Set the constraint on first column
		Tree<String> table = tree.getFirstChild("table")
				.getFirstChild("columns").findFirstChild(table_table_title).getParent().getParent();
		
		Tree<String> constraintTable = table.add("constraint");
		
		constraintTable.add("count").add("1");

		Tree<String> valsTable = constraintTable.add("values");

		Iterator<String> itTable = tablesIn.iterator();
		while(itTable.hasNext()){
			valsTable.add("value").add(itTable.next());
		}
		
		//Generate Editor
		Tree<String> featEdit = HiveDictionary.generateEditor(
				HiveDictionary.getInstance().createDefaultSelectHelpMenu(),hj.getInFeatures());

		//Set the Editor of operation
		Tree<String> operation = tree.getFirstChild("table")
				.getFirstChild("columns").findFirstChild(table_feat_title);
		operation.getParent().getParent().add(featEdit);
	}


	protected Tree<String> getRootTable(Set<String> tablesIn) throws RemoteException{
		//Table
		Tree<String> input = new TreeNonUnique<String>("table");
		Tree<String> columns = new TreeNonUnique<String>("columns");
		input.add(columns);

		//Feature name
		Tree<String> table = new TreeNonUnique<String>("column");
		columns.add(table);
		table.add("title").add(table_table_title);

		columns.add("column").add("title").add(table_feat_title);
		
		//Add rows
		Iterator<String> tableIn = tablesIn.iterator();
		while(tableIn.hasNext()){
			input.add("row").add(table_table_title).add(tableIn.next());
		}

		return input;
	}

	public String getQueryPiece() throws RemoteException{
		logger.debug("join...");
		
		String joinType = hj.getJoinTypeInt().getTree()
				.getFirstChild("list").getFirstChild("output")
				.getFirstChild().getHead();
		
		String join = "";
		String prev = "";
		Map<String,DFEOutput> aliases = hj.getAliases();
		HiveInterface hi = new HiveInterface();
		Iterator<Tree<String>> it = getTree().getFirstChild("table")
				.getChildren("row").iterator();
		if(it.hasNext()){
			Tree<String> cur = it.next();
			String curAlias = cur.getFirstChild(table_table_title).getFirstChild().getHead();
			prev = cur.getFirstChild(table_feat_title).getFirstChild().getHead();
			join = hi.getTableAndPartitions(aliases.get(curAlias).getPath())[0]
					+" "
					+curAlias;
		}
		while(it.hasNext()){
			Tree<String> cur = it.next();
			String curFeat =  cur.getFirstChild(table_feat_title).getFirstChild().getHead();
			String curAlias = cur.getFirstChild(table_table_title).getFirstChild().getHead();
			join += " "+joinType+" "
					+hi.getTableAndPartitions(aliases.get(curAlias).getPath())[0]
					+" "
					+curAlias+" ON ("+prev+" = "+curFeat+")";
			prev = curFeat;
		}

		return join;
	}
	
	public String checkExpression(String expression, String modifier) throws RemoteException{
		String error = null;
		try{
			if (HiveDictionary.getInstance().getReturnType(expression, hj.getInFeatures()) == null){
				error = "Expression does not have a return type";
			}
		}
		catch (Exception e){
			error = "Error trying to get expression return type";
			logger.error(error, e);
		}
		return error;
	}

}
