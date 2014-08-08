package com.redsqirl.workflow.server.interaction;


import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.redsqirl.utils.FieldList;
import com.redsqirl.utils.Tree;
import com.redsqirl.utils.TreeNonUnique;
import com.redsqirl.workflow.server.TableInteraction;
import com.redsqirl.workflow.server.action.PigJoin;
import com.redsqirl.workflow.server.action.utils.PigDictionary;
import com.redsqirl.workflow.utils.PigLanguageManager;

/**
 * Specify the relationship between joined relations. The order is important as
 * it will be the same in the Pig Latin query.
 * 
 * @author marcos
 * 
 */
public class PigJoinRelationInteraction extends TableInteraction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7384667815452362352L;
	/**
	 * Join action which interaction is contained n
	 */
	private PigJoin hj;
								/**Relation column title*/
	public static final String table_relation_title = PigLanguageManager.getTextWithoutSpace("pig.join_relationship_interaction.relation_column"),
			/**Feature column tile*/
			table_feat_title = PigLanguageManager.getTextWithoutSpace("pig.join_relationship_interaction.op_column");
	/**
	 * Constructor
	 * @param id
	 * @param name
	 * @param legend
	 * @param column
	 * @param placeInColumn
	 * @param hj
	 * @throws RemoteException
	 */
	public PigJoinRelationInteraction(String id, String name, String legend, int column,
			int placeInColumn, PigJoin hj) throws RemoteException {
		super(id, name, legend, column, placeInColumn);
		this.hj = hj;
		tree.removeAllChildren();
		tree.add(getRootTable());
	}
	/**
	 * Check the interaction for errors
	 * @return Error Message
	 * @throws RemoteException
	 */
	@Override
	public String check() throws RemoteException {
		String msg = super.check();
		if(msg != null){
			return msg;
		}
		
		
		List<Map<String,String>> lRow = getValues();
		Set<String> relations = hj.getAliases().keySet();
		if( relations.size() != lRow.size()){
			msg = PigLanguageManager.getText("pig.join_relationship_interaction.checkrownb");
		}else{
			Set<String> featType = new LinkedHashSet<String>();
			FieldList inFeats = hj.getInFields();
			logger.debug(inFeats.getFieldNames());
			Iterator<Map<String,String>> rows = lRow.iterator();
			int rowNb = 0;
			while (rows.hasNext() && msg == null) {
				++rowNb;
				Map<String,String> row = rows.next();
				try {
					String relation = row.get(table_relation_title);
					String rel = row.get(table_feat_title);
					String type = PigDictionary.getInstance().getReturnType(
							rel, inFeats);

					if (type == null) {
						msg = PigLanguageManager.getText("pig.join_relationship_interaction.checkexpressionnull",new Object[]{rowNb});
					} else {
						featType.add(type);
					}

					Iterator<String> itRelation = relations.iterator();
					while (itRelation.hasNext() && msg == null) {
						String curTab = itRelation.next();
						if (rel.contains(curTab+".") &&
								!curTab.equalsIgnoreCase(relation)) {
							msg = PigLanguageManager.getText("pig.join_relationship_interaction.checktable2times",
									new Object[]{rowNb,curTab,relation});
						}
					}

				} catch (Exception e) {
					msg = e.getMessage();
				}
			}

			if (msg == null && featType.size() != 1) {
				msg = PigLanguageManager.getText("pig.join_relationship_interaction.checksametype");
			}
		}

		return msg;
	}
	/**
	 * Update the interaction
	 * @throws RemoteException
	 */
	public void update() throws RemoteException {
		Set<String> tablesIn = hj.getAliases().keySet();

		// Remove constraint on first column
		updateColumnConstraint(
				table_relation_title, 
				null, 
				1, 
				tablesIn);
		

		updateColumnConstraint(
				table_feat_title, 
				null, 
				null,
				null);
		updateEditor(
				table_feat_title,
				PigDictionary.generateEditor(PigDictionary
				.getInstance().createDefaultSelectHelpMenu(), hj
				.getInFields(), null));
		
		if(getValues().isEmpty()){
			List<Map<String,String>> lrows = new LinkedList<Map<String,String>>();
			Iterator<String> tableIn = tablesIn.iterator();
			while (tableIn.hasNext()) {
				Map<String,String> curMap = new LinkedHashMap<String,String>();
				curMap.put(table_relation_title,tableIn.next());
				curMap.put(table_feat_title,"");
				lrows.add(curMap);
			}
			setValues(lrows);
		}
	}
	/**
	 * Check the expression for error
	 * @param expression
	 * @param modifier
	 * @throws RemoteException
	 */
	public String checkExpression(String expression, String modifier)
			throws RemoteException {
		String error = null;
		try {
			if (PigDictionary.getInstance().getReturnType(
					expression,
					hj.getInFields()
					) == null) {
				error = PigLanguageManager.getText("pig.expressionnull");
			}
		} catch (Exception e) {
			error = PigLanguageManager.getText("pig.expressionexception");
			logger.error(error, e);
		}
		return error;
	}
	/**
	 * Get the root table
	 * @return root table Tree
	 * @throws RemoteException
	 */
	protected Tree<String> getRootTable()
			throws RemoteException {
		// Table
		Tree<String> input = new TreeNonUnique<String>("table");
		Tree<String> columns = new TreeNonUnique<String>("columns");
		input.add(columns);

		// Feature name
		Tree<String> table = new TreeNonUnique<String>("column");
		columns.add(table);
		table.add("title").add(table_relation_title);

		columns.add("column").add("title").add(table_feat_title);

		return input;
	}
	/**
	 * Get the query piece for the join
	 * @return query
	 * @throws RemoteException
	 */
	public String getQueryPiece() throws RemoteException {
		logger.debug("join...");

		String joinType = hj.getJoinTypeInt().getTree().getFirstChild("list")
				.getFirstChild("output").getFirstChild().getHead()
				.replace("JOIN", "");

		Set<String> aliases = hj.getAliases().keySet();
		String join = "";
		Iterator<Map<String,String>> it = getValues().iterator();
		if (it.hasNext()) {
			join += "JOIN";
		}
		while (it.hasNext()) {
			Map<String,String> cur = it.next();
			String expr = cur.get(table_feat_title);
			logger.info(expr);
			
			Iterator<String> namesIt = aliases.iterator();
			String ans = expr;
			while(namesIt.hasNext()){
				ans = ans.replaceAll(Pattern.quote(namesIt.next()+"."), "");
			}

			String relation = cur.get(table_relation_title);

			join += " " + relation + " BY " + ans;
			if (!joinType.isEmpty()) {
				join += " " + joinType;
			}
			if (it.hasNext()) {
				join += ",";
			}
		}

		return join;
	}
}