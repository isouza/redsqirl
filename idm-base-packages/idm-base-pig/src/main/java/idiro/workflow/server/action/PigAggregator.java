package idiro.workflow.server.action;

import idiro.utils.FeatureList;
import idiro.workflow.server.Page;
import idiro.workflow.server.interfaces.DFEInteraction;
import idiro.workflow.server.interfaces.DFEOutput;
import idiro.workflow.utils.PigLanguageManager;

import java.rmi.RemoteException;
import java.util.Iterator;

public class PigAggregator extends PigElement {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4640611831909705304L;

	private Page page1, page2, page3;

	private PigTableSelectInteraction tSelInt;
	private PigFilterInteraction filterInt;
	
	public static final String key_grouping = "grouping";

	public PigAggregator() throws RemoteException {
		super(1, 1,1);
		
		page1 = addPage(
				PigLanguageManager.getText("pig.aggregator_page1.title"), 
				PigLanguageManager.getText("pig.aggregator_page1.legend"), 
				1);

		tSelInt = new PigTableSelectInteraction(
				key_featureTable,
				PigLanguageManager.getText("pig.aggregator_features_interaction.title"),
				PigLanguageManager.getText("pig.aggregator_features_interaction.legend"),
				0, 0, this);

		groupingInt = new PigGroupInteraction(
				key_grouping,
				PigLanguageManager.getText("pig.aggregator_group_interaction.title"),
				PigLanguageManager.getText("pig.aggregator_group_interaction.legend"), 
				0, 1);

		page1.addInteraction(groupingInt);

		page2 = addPage(
				PigLanguageManager.getText("pig.aggregator_page2.title"), 
				PigLanguageManager.getText("pig.aggregator_page2.legend"), 
				1);

		page2.addInteraction(tSelInt);

		page3 = addPage(
				PigLanguageManager.getText("pig.aggregator_page3.title"), 
				PigLanguageManager.getText("pig.aggregator_page3.legend"), 
				1);

		filterInt = new PigFilterInteraction(0, 0, this);

		page3.addInteraction(filterInt);
		page3.addInteraction(delimiterOutputInt);
		page3.addInteraction(savetypeOutputInt);
	}

	public String getName() throws RemoteException {
		return "pig_aggregator";
	}

	@Override
	public String getQuery() throws RemoteException {
		String query = null;
		if (getDFEInput() != null) {
			DFEOutput in = getDFEInput().get(key_input).get(0);
			logger.debug("In and out...");
			// Output
			DFEOutput out = output.values().iterator().next();
			String remove = getRemoveQueryPiece(out.getPath()) + "\n\n";

			String filter = filterInt.getQueryPieceGroup(getCurrentName());

			String loader = "";
			String filterLoader = "";
			Iterator<String> aliases = getAliases().keySet().iterator();

			if (!filter.isEmpty()) {
				
					logger.info("load data by alias");
					loader = getAliases().keySet().iterator().next();
					filter = loader + " = " + filter + ";\n\n";
					filterLoader = loader;
					loader = getCurrentName();
			} else {
				if (aliases.hasNext()) {
					loader = aliases.next();
				}
			}


			String load = loader + " = " + getLoadQueryPiece(in) + ";\n\n";

			if (filterLoader.isEmpty()) {
				filterLoader = loader;
			}

			String groupby = groupingInt.getQueryPiece(filterLoader);
			if (!groupby.isEmpty()) {
				groupby = getNextName() + " = " + groupby + ";\n\n";
			}

			String select = tSelInt.getQueryPiece(out, getCurrentName());
			if (!select.isEmpty()) {
				select = getNextName() + " = " + select + ";\n\n";
			}

			String store = getStoreQueryPiece(out, getCurrentName());

			if (select.isEmpty()) {
				logger.info("Nothing to select");
			} else {
				query = remove;
				query += load;
				query += filter;
				query += groupby;
				query += select;
				query += store;
			}
		}
		return query;
	}

	@Override
	public FeatureList getInFeatures() throws RemoteException {
		return getDFEInput().get(key_input).get(0).getFeatures();
	}

	@Override
	public FeatureList getNewFeatures() throws RemoteException {
		return tSelInt.getNewFeatures();
	}

	@Override
	public void update(DFEInteraction interaction) throws RemoteException {
		
		
		DFEOutput in = getDFEInput().get(key_input).get(0);
		logger.info(in.getFeatures().getFeaturesNames());
		String interId = interaction.getId();
		if (in != null) {
			if (interId.equals(tSelInt.getId())) {
				tSelInt.update(in);
			} else if (interId.equals(key_grouping)) {
				groupingInt.update(in);
			}  else if (interId.equals(key_condition)) {
				filterInt.update();
			}

		}
	}

	public PigTableSelectInteraction gettSelInt() {
		return tSelInt;
	}

	public PigFilterInteraction getFilterInt() {
		return filterInt;
	}

}
