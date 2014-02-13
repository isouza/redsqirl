package idiro.workflow.server.action;

import idiro.utils.FeatureList;
import idiro.utils.OrderedFeatureList;
import idiro.utils.Tree;
import idiro.workflow.server.TableInteraction;
import idiro.workflow.server.action.utils.HiveDictionary;
import idiro.workflow.server.enumeration.FeatureType;
import idiro.workflow.utils.HiveLanguageManager;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * Interaction to choose the field of the join output. The interaction is a
 * table with 3 fields 'Operation', 'Feature name' and 'Type'.
 * 
 * @author etienne
 * 
 */
public class HiveTableJoinInteraction extends TableInteraction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8521366798554741811L;

	private Logger logger = Logger.getLogger(getClass());

	private HiveJoin hj;

	public static final String table_op_title = HiveLanguageManager.getTextWithoutSpace("hive.join_features_interaction.op_column"),
			table_feat_title = HiveLanguageManager.getTextWithoutSpace("hive.join_features_interaction.feat_column"),
			table_type_title = HiveLanguageManager.getTextWithoutSpace("hive.join_features_interaction.type_column");

	public HiveTableJoinInteraction(String id,String name, String legend, int column,
			int placeInColumn, HiveJoin hj) throws RemoteException {
		super(id, name, legend, column, placeInColumn);
		this.hj = hj;
		getRootTable();
	}

	@Override
	public String check() throws RemoteException {
		FeatureList features = hj.getInFeatures();
		int rowNb = 0;
		String msg = null;
		
		List<Map<String,String>> lRow = getValues();

		if(lRow.isEmpty()){
			msg = HiveLanguageManager.getText("hive.join_features_interaction.checkempty");
		}
		
		logger.debug(features.getFeaturesNames());
		Iterator<Map<String,String>> rows = lRow.iterator();
		while(rows.hasNext() && msg == null){
			++rowNb;
			Map<String,String> row = rows.next();
			
				String type = row.get(table_type_title);
				String op = row.get(table_op_title);
				String feature = row.get(table_feat_title);
				if(!HiveDictionary.isVariableName(feature)){
					msg = HiveLanguageManager.getText("hive.join_features_interaction.featureinvalid",new Object[]{rowNb,feature});
				}else{
					try{
						if( ! HiveDictionary.check(
								type, 
								HiveDictionary.getInstance().getReturnType(
										op,
										features
										)
								)){
							msg = HiveLanguageManager.getText("hive.join_features_interaction.typeinvalid",new Object[]{rowNb,feature});
						}
					}catch(Exception e){
						msg = e.getMessage();
					}
				}
			}

		return msg;
	}

	public void update() throws RemoteException {

FeatureList feats = hj.getInFeatures();
		
		updateEditor(table_op_title,
				HiveDictionary.generateEditor(HiveDictionary.getInstance().createDefaultSelectHelpMenu(),feats));
		
		//Set the Generator
		logger.debug("Set the generator...");
		
		//Copy Generator operation
		List<Map<String,String>> copyRows = new LinkedList<Map<String,String>>();
		Iterator<String> featIt = feats.getFeaturesNames().iterator();
		while(featIt.hasNext()){
			Map<String,String> curMap = new LinkedHashMap<String,String>();
			String cur = featIt.next();
			
			curMap.put(table_op_title,cur);
			curMap.put(table_feat_title,cur.replaceAll("\\.","_"));
			curMap.put(table_type_title,
					HiveDictionary.getHiveType(feats.getFeatureType(cur)));
			copyRows.add(curMap);
		}
		updateGenerator("copy", copyRows);
	}

	protected void getRootTable() throws RemoteException {
		
		addColumn(
				table_op_title, 
				null, 
				null,
				null);
		
		addColumn(
				table_feat_title, 
				1, 
				null,
				null);
		
		List<String> typeValues = new LinkedList<String>();
		typeValues.add(FeatureType.BOOLEAN.name());
		typeValues.add(FeatureType.INT.name());
		typeValues.add(FeatureType.DOUBLE.name());
		typeValues.add(FeatureType.FLOAT.name());
		
		addColumn(
				table_type_title,
				null,
				typeValues,
				null);	

	}

	public FeatureList getNewFeatures() throws RemoteException {
		FeatureList new_features = new OrderedFeatureList();
		Iterator<Tree<String>> rowIt = getTree().getFirstChild("table")
				.getChildren("row").iterator();

		while (rowIt.hasNext()) {
			Tree<String> rowCur = rowIt.next();
			String name = rowCur.getFirstChild(table_feat_title)
					.getFirstChild().getHead();
			String type = rowCur.getFirstChild(table_type_title)
					.getFirstChild().getHead();
			new_features.addFeature(name, FeatureType.valueOf(type));
		}
		return new_features;
	}

	public String getQueryPiece() throws RemoteException {
		logger.debug("join interaction...");
		String select = "";
		Iterator<Tree<String>> selIt = getTree().getFirstChild("table")
				.getChildren("row").iterator();
		if (selIt.hasNext()) {
			Tree<String> cur = selIt.next();
			String featName = cur.getFirstChild(table_feat_title)
					.getFirstChild().getHead();
			select = "SELECT "
					+ cur.getFirstChild(table_op_title).getFirstChild()
							.getHead() + " AS " + featName;
		}
		while (selIt.hasNext()) {
			Tree<String> cur = selIt.next();
			String featName = cur.getFirstChild(table_feat_title)
					.getFirstChild().getHead();
			select += ",\n       "
					+ cur.getFirstChild(table_op_title).getFirstChild()
							.getHead() + " AS " + featName;
		}

		return select;
	}

	public String getCreateQueryPiece() throws RemoteException {
		logger.debug("create features...");
		String createSelect = "";
		Iterator<Tree<String>> selIt = getTree().getFirstChild("table")
				.getChildren("row").iterator();
		if (selIt.hasNext()) {
			Tree<String> cur = selIt.next();
			String featName = cur.getFirstChild(table_feat_title)
					.getFirstChild().getHead();
			createSelect = "("
					+ featName
					+ " "
					+ cur.getFirstChild(table_type_title).getFirstChild()
							.getHead();
		}
		while (selIt.hasNext()) {
			Tree<String> cur = selIt.next();
			String featName = cur.getFirstChild(table_feat_title)
					.getFirstChild().getHead();
			createSelect += ","
					+ featName
					+ " "
					+ cur.getFirstChild(table_type_title).getFirstChild()
							.getHead();
		}
		createSelect += ")";

		return createSelect;
	}

	public String checkExpression(String expression, String modifier)
			throws RemoteException {
		String error = null;
		try {
			if (HiveDictionary.getInstance().getReturnType(expression,
					hj.getInFeatures()) == null) {
				error = "Expression does not have a return type";
			}
		} catch (Exception e) {
			error = "Error trying to get expression return type";
			logger.error(error, e);
		}
		return error;
	}
}