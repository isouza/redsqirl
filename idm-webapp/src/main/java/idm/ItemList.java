package idm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.richfaces.model.Ordering;


/** ItemList
 * 
 * Class to for control of items in a grid. used in screens file system.
 * 
 * @author Igor.Souza
 */
public class ItemList implements Serializable{
	
	private String name;
	private boolean selected;
	private String stringSelectedDestination;
	private String idSelected;
	private String property;
	private String value;
	
	private Map<String, String> typeTableInteraction = new HashMap<String, String>();
	private Map<String, String> nameValue = new HashMap<String, String>();
	private Map<String, Ordering> sortingOrder = new HashMap<String, Ordering>();
	private Map<String, Object> filterValue = new HashMap<String, Object>();
	private Map<String, String> nameValueEdit = new HashMap<String, String>();
	private Map<String, Boolean> nameIsConst = new HashMap<String, Boolean>();
	private Map<String, Boolean> valueHasLineBreak = new HashMap<String, Boolean>();
	private Map<String, String> nameValueGrid = new HashMap<String, String>();
	
	/*
	 *
	 * @author Igor.Souza
	 */
	public ItemList() {
		super();
	}
	
	public ItemList(String name) {
		super();
		this.name = name;
	}
	
	public ItemList(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public List<String> getKeyAsListNameValue(){
		return new ArrayList<String>(nameValue.keySet());
	}
	
	public List<String> getKeyAsListNameValueEdit(){
		return new ArrayList<String>(nameValueEdit.keySet());
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public void setStringSelectedDestination(String selectedDestination) {
		this.stringSelectedDestination = selectedDestination;
	}
	
	public String getStringSelectedDestination() {
		return stringSelectedDestination;
	}
	
	public boolean isSelectedDestination() {
		return ("true").equals(stringSelectedDestination);
	}

	public void setSelectedDestination(boolean selectedDestination) {
		this.stringSelectedDestination = String.valueOf(selectedDestination);
	}

	public Map<String, String> getNameValue() {
		return nameValue;
	}

	public void setNameValue(Map<String, String> nameValue) {
		this.nameValue = nameValue;
	}
	
	public Map<String, Ordering> getSortingOrder() {
		return sortingOrder;
	}

	public void setSortingOrder(Map<String, Ordering> sortingOrder) {
		this.sortingOrder = sortingOrder;
	}
	
	public Map<String, Object> getFilterValue() {
		return filterValue;
	}

	public void setFilterValue(Map<String, Object> filterValue) {
		this.filterValue = filterValue;
	}

	public Map<String, String> getNameValueEdit() {
		return nameValueEdit;
	}

	public void setNameValueEdit(Map<String, String> nameValueEdit) {
		this.nameValueEdit = nameValueEdit;
	}
	
	public Map<String, Boolean> getNameIsConst() {
		return nameIsConst;
	}

	public void setNameIsConst(Map<String, Boolean> nameIsConst) {
		this.nameIsConst = nameIsConst;
	}

	public String getIdSelected() {
		return idSelected;
	}

	public void setIdSelected(String idSelected) {
		this.idSelected = idSelected;
	}

	public Map<String, String> getTypeTableInteraction() {
		return typeTableInteraction;
	}

	public void setTypeTableInteraction(Map<String, String> typeTableInteraction) {
		this.typeTableInteraction = typeTableInteraction;
	}
	
	public Map<String, Boolean> getValueHasLineBreak() {
		return valueHasLineBreak;
	}

	public void setValueHasLineBreak(Map<String, Boolean> valueHasLineBreak) {
		this.valueHasLineBreak = valueHasLineBreak;
	}

	public Map<String, String> getNameValueGrid() {
		return nameValueGrid;
	}

	public void setNameValueGrid(Map<String, String> nameValueGrid) {
		this.nameValueGrid = nameValueGrid;
	}

	public String getProperty() {
		return property;
	}

	public String getValue() {
		return value;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
}