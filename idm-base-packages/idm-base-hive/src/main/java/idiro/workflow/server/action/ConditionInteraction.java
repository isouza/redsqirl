package idiro.workflow.server.action;

import idiro.utils.Tree;
import idiro.utils.TreeNonUnique;
import idiro.workflow.server.EditorInteraction;
import idiro.workflow.server.UserInteraction;
import idiro.workflow.server.action.utils.HiveDictionary;
import idiro.workflow.server.datatype.HiveTypeWithWhere;
import idiro.workflow.server.enumeration.DisplayType;
import idiro.workflow.server.interfaces.DFEOutput;
import idiro.workflow.utils.HiveLanguageManager;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Interaction for storing/checking HiveQL conditions.
 * 
 * @author etienne
 * 
 */
public class ConditionInteraction extends EditorInteraction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6688812502383438930L;

	private HiveElement el;

	public ConditionInteraction(int column,
			int placeInColumn, HiveElement el)
			throws RemoteException {
		super(HiveElement.key_condition, HiveLanguageManager
				.getText("hive.filter_interaction.title"), HiveLanguageManager
				.getText("hive.filter_interaction.legend"), column,
				placeInColumn);
		this.el = el;
	}

	@Override
	public String check() {
		String msg = null;
		try {

			String condition = getValue();
			if (condition != null && !condition.isEmpty()) {
				logger.debug("Condition: " + condition
						+ " features list size : "
						+ el.getInFeatures().getSize());
				String type = null;
				Set<String> aggregation = null;
				if(el.groupingInt != null){
					aggregation = el.groupingInt.getAggregationFeatures(el.getDFEInput().get(HiveElement.key_input).get(0));
					logger.info("aggregation set size : "+ aggregation.size());
				}
				type = HiveDictionary.getInstance().getReturnType(
						condition, el.getInFeatures(),aggregation);
				if (!type.equalsIgnoreCase("boolean")) {
					msg = HiveLanguageManager.getText("hive.filter_interaction.checkerror",new String[]{type});
					logger.info(msg);

				}
			}
		} catch (Exception e) {
			msg = HiveLanguageManager.getText("hive.filter_interaction.checkexception");
			logger.error(msg);

		}
		return msg;
	}

	public void update() throws RemoteException {
		try {
			String output = getValue();
			tree.remove("editor");

			Tree<String> base = HiveDictionary.generateEditor(HiveDictionary.getInstance()
					.createConditionHelpMenu(), el.getInFeatures());
			//logger.debug(base);
			tree.add(base.getFirstChild("editor"));
			setValue(output);
		} catch (Exception ec) {
			logger.info("There was an error updating " + getName());
		}
	}

	public String getQueryPiece() throws RemoteException {
		logger.debug("where...");
		String where = "";
		if (getTree().getFirstChild("editor").getFirstChild("output")
				.getSubTreeList().size() > 0) {
			where = getTree().getFirstChild("editor").getFirstChild("output")
					.getFirstChild().getHead();
		}

		String whereIn = getInputWhere();
		if (!where.isEmpty()) {
			if (!whereIn.isEmpty()) {
				where = "(" + where + ") AND (" + whereIn + ")";
			}
			where = " WHERE " + where;
		} else if (!whereIn.isEmpty()) {
			where = "WHERE " + whereIn;
		}
		return where;
	}

	public String getInputWhere() throws RemoteException {
		String where = "";
		List<DFEOutput> out = el.getDFEInput().get(HiveElement.key_input);
		Iterator<DFEOutput> it = out.iterator();
		while (it.hasNext()) {
			DFEOutput cur = it.next();
			String where_loc = cur.getProperty(HiveTypeWithWhere.key_where);
			if (where_loc != null) {
				if (where.isEmpty()) {
					where = where_loc;
				} else {
					where = " AND " + where_loc;
				}
			}
		}
		return where;
	}

	public String getInputWhere(String alias) throws RemoteException {
		String where = "";
		DFEOutput out = el.getAliases().get(alias);
		if (out != null) {
			where = out.getProperty(HiveTypeWithWhere.key_where);
			if (where == null) {
				where = "";
			}
		}

		return where;
	}

}
