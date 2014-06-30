package com.redsqirl.workflow.server.action;


import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.redsqirl.utils.FeatureList;
import com.redsqirl.utils.OrderedFeatureList;
import com.redsqirl.workflow.server.DataProperty;
import com.redsqirl.workflow.server.Page;
import com.redsqirl.workflow.server.connect.HiveInterface;
import com.redsqirl.workflow.server.datatype.HiveType;
import com.redsqirl.workflow.server.interfaces.DFEInteraction;
import com.redsqirl.workflow.server.interfaces.DFELinkProperty;
import com.redsqirl.workflow.server.interfaces.DFEOutput;
import com.redsqirl.workflow.utils.HiveLanguageManager;

/**
 * Action to do a union statement in HiveQL.
 * 
 * @author etienne
 * 
 */
public class HiveUnion extends HiveElement {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2971963679008329394L;
	/** Feature Key */
	public static final String key_featureTable = "Features";
	/** Union Condidtion Key */
	public final String key_union_condition = "union_cond";
	/**
	 * key for alias interaction
	 */
	public final String key_alias_interaction = "alias_int";
	/**
	 * Pages
	 */
	private Page page1, page2, page3, page4;
	/**
	 * Union Table Select Interaction
	 */
	private HiveTableUnionInteraction tUnionSelInt;
	/**
	 * Union Condition Interaction
	 */
	private HiveUnionConditions tUnionCond;
	
	/**
	 * Tabel alias interaction
	 */
	private HiveTableAliasInteraction tAliasInt;

	/**
	 * Constructor
	 * 
	 * @throws RemoteException
	 */
	public HiveUnion() throws RemoteException {
		super(2, 1, Integer.MAX_VALUE);
		
		page1 = addPage(
				HiveLanguageManager.getText("hive.union_page1.title"),
				HiveLanguageManager.getText("hive.union_page1.legend"), 1);
		
		tAliasInt = new HiveTableAliasInteraction(
				key_alias_interaction,
				HiveLanguageManager.getText("hive.table_alias_interaction.title"),
				HiveLanguageManager.getText("hive.table_alias_interaction.legend"),
				0, 0, this, 2);

		page1.addInteraction(tAliasInt);

		page2 = addPage(HiveLanguageManager.getText("hive.union_page2.title"),
				HiveLanguageManager.getText("hive.union_page2.legend"), 1);

		tUnionSelInt = new HiveTableUnionInteraction(key_featureTable,
				HiveLanguageManager
						.getText("hive.union_features_interaction.title"),
				HiveLanguageManager
						.getText("hive.union_features_interaction.legend"), 0,
				0, this);

		page2.addInteraction(tUnionSelInt);

		page3 = addPage(HiveLanguageManager.getText("hive.union_page3.title"),
				HiveLanguageManager.getText("hive.union_page3.legend"), 1);
		page3.addInteraction(orderInt);

		page4 = addPage(HiveLanguageManager.getText("hive.union_page4.title"),
				HiveLanguageManager.getText("hive.union_page4.legend"), 1);

		tUnionCond = new HiveUnionConditions(
				key_union_condition,
				HiveLanguageManager.getText("hive.union_cond_interaction.title"),
				HiveLanguageManager
						.getText("hive.union_cond_interaction.legend"), 0, 0,
				this);

		page4.addInteraction(tUnionCond);
		page4.addInteraction(typeOutputInt);

	}

	/**
	 * Initialise the input map
	 * 
	 * @throws RemoteException
	 */
	public void init() throws RemoteException {
		if (input == null) {
			Map<String, DFELinkProperty> in = new LinkedHashMap<String, DFELinkProperty>();
			in.put(key_input, new DataProperty(HiveType.class, 1,
					Integer.MAX_VALUE));
			input = in;
		}
	}

	/**
	 * Get the name of the action
	 */
	public String getName() throws RemoteException {
		return "hive_union";
	}
	/**
	 * Update the interaction that are in the action 
	 * @param interaction
	 * @throws RemoteException
	 */
	public void update(DFEInteraction interaction) throws RemoteException {

		List<DFEOutput> in = getDFEInput().get(key_input);
		String interId = interaction.getId();
		logger.info("Hive Union interaction: " + interId+", "+interaction.getName());

		if (in != null && in.size() > 0) {
			if (interId.equals(tUnionSelInt.getId())) {
				logger.info("update tunuion interaction");
				tUnionSelInt.update(in);
			} else if (interId.equals(tUnionCond.getId())) {
				tUnionCond.update(in);
			} else if(interId.equals(tAliasInt.getId())){
				tAliasInt.update();
			}
			else if (interaction.getName().equals(orderInt.getName())) {
				orderInt.update();
			}
		}
	}
	/**
	 * Get the query to run the union action
	 * @return query
	 * @throws RemoteException
	 */
	public String getQuery() throws RemoteException {

		HiveInterface hInt = new HiveInterface();
		String query = null;
		if (getDFEInput() != null) {
			// Output
			DFEOutput out = output.values().iterator().next();
			String tableOut = hInt.getTableAndPartitions(out.getPath())[0];

			String insert = "INSERT OVERWRITE TABLE " + tableOut;
			String create = "CREATE TABLE IF NOT EXISTS " + tableOut;

			String select = tUnionSelInt.getQueryPiece(out,
					tUnionCond.getCondition());
			String createSelect = tUnionSelInt.getCreateQueryPiece(out);

			String order = orderInt.getQueryPiece();

			if (select.isEmpty()) {
				logger.debug("Nothing to select");
			} else {
				query = create + "\n" + createSelect + ";\n\n";

				query += insert + "\n" + select + "\n" + order + ";";
			}
		}

		return query;
	}
	/**
	 * Get the features that are in the input
	 * @return input FeatureList
	 * @throws RemoteException
	 */
	public FeatureList getInFeatures() throws RemoteException {
		FeatureList ans = new OrderedFeatureList();
		Map<String, DFEOutput> aliases = getAliases();

		Iterator<String> it = aliases.keySet().iterator();
		while (it.hasNext()) {
			String alias = it.next();
			FeatureList mapTable = aliases.get(alias).getFeatures();
			Iterator<String> itFeat = mapTable.getFeaturesNames().iterator();
			while (itFeat.hasNext()) {
				String cur = itFeat.next();
				ans.addFeature(alias + "." + cur, mapTable.getFeatureType(cur));
			}
		}
		return ans;
	}

	/**
	 * Get the input features with the alias
	 * @param alias
	 * @return FeatureList
	 * @throws RemoteException
	 */
	public FeatureList getInFeatures(String alias) throws RemoteException {
		FeatureList ans = null;
		Map<String, DFEOutput> aliases = getAliases();
		if(aliases.get(alias) != null){
			ans = new OrderedFeatureList();
			FeatureList mapTable = aliases.get(alias).getFeatures();
			Iterator<String> itFeat = mapTable.getFeaturesNames().iterator();
			while (itFeat.hasNext()) {
				String cur = itFeat.next();
				ans.addFeature(alias + "." + cur, mapTable.getFeatureType(cur));
			}
		}
		return ans;
	}
	/**
	 * Get the feature list that are generated from this action
	 * @return new FeatureList
	 * @throws RemoteException
	 */
	@Override
	public FeatureList getNewFeatures() throws RemoteException {
		return tUnionSelInt.getNewFeatures();
	}

	/**
	 * Get the Union Select Interaction
	 * @return tUnionSelInt
	 */
	public final HiveTableUnionInteraction gettUnionSelInt() {
		return tUnionSelInt;
	}

	/**
	 * Get the Union Condition Interaction
	 * @return tUnionCond
	 */
	public final HiveUnionConditions gettUnionCond() {
		return tUnionCond;
	}
	
	/**
	 * Get the table Alias Interaction
	 * @return tUnionSelInt
	 */
	public final HiveTableAliasInteraction gettAliasInt() {
		return tAliasInt;
	}
	
	@Override
	public Map<String, DFEOutput> getAliases() throws RemoteException {
		
		Map<String, DFEOutput> aliases = tAliasInt.getAliases();
		
		if (aliases.isEmpty()){
			aliases = super.getAliases();
		}
		
		return aliases;
	}

}