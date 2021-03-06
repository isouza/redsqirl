/** 
 *  Copyright © 2016 Red Sqirl, Ltd. All rights reserved.
 *  Red Sqirl, Clarendon House, 34 Clarendon St., Dublin 2. Ireland
 *
 *  This file is part of Red Sqirl
 *
 *  User agrees that use of this software is governed by: 
 *  (1) the applicable user limitations and specified terms and conditions of 
 *      the license agreement which has been entered into with Red Sqirl; and 
 *  (2) the proprietary and restricted rights notices included in this software.
 *  
 *  WARNING: THE PROPRIETARY INFORMATION OF Red Sqirl IS PROTECTED BY IRISH AND 
 *  INTERNATIONAL LAW.  UNAUTHORISED REPRODUCTION, DISTRIBUTION OR ANY PORTION
 *  OF IT, MAY RESULT IN CIVIL AND/OR CRIMINAL PENALTIES.
 *  
 *  If you have received this software in error please contact Red Sqirl at 
 *  support@redsqirl.com
 */

package com.redsqirl.interaction;


import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.redsqirl.BaseBean;
import com.redsqirl.useful.MessageUseful;

/**
 * Editor selected by the user. This class will control the 
 * object going in and out.
 * @author etienne
 *
 */
public class SelectedEditor extends BaseBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1263101069235275537L;

	private static Logger logger = Logger.getLogger(SelectedEditor.class);

	/**
	 * The editor
	 */
	private EditorFromTree edit;

	/**
	 * Function selected in the menu
	 */
	private String selectedFunction;

	/**
	 * Operation selected in the menu
	 */
	private String selectedOperation;

	/**
	 * The table interaction from which the editor comes from.
	 * If this object is null then editInter is not.
	 */
	private TableInteraction tableInter;

	/**
	 * The editor interaction from which the editor comes from.
	 * If this object is null then tableInter is not.
	 */
	private EditorInteraction editInter;

	/**
	 * "Y" if the validation is a success or "N" if it is a failure.
	 */
	private String confirm;

	/**
	 * The column to edit (null if the object does not derive from a TableInteraction)
	 */
	private String columnEdit;

	/**
	 * The row to edit (null if the object does not derive from a TableInteraction)
	 */
	private int rowEdit;

	/**
	 * The value
	 */
	private String value;

	private List<String> textEditorFunctionMenuString;

	private List<String> textEditorOperationMenuString;

	public List<String[]> listFunctionsCombobox = new ArrayList<String[]>();
	public List<String[]> listOperationCombobox = new ArrayList<String[]>();


	/**
	 * Constructor used for table interaction
	 * @param edit
	 * @param tableInter
	 * @param columnEdit
	 * @param rowEdit
	 */
	public SelectedEditor(TableInteraction tableInter,
			String columnEditTitle, int rowEdit) {
		super();
		logger.info("build selected editor in table inter...");
		this.tableInter = tableInter;
		this.columnEdit = tableInter.getTableGrid().getColumnIds().get(tableInter.getTableGrid().getTitles().indexOf(columnEditTitle));
		this.rowEdit = rowEdit;
		this.edit = tableInter.getTableEditors().get(columnEdit);
		this.value = tableInter.getTableGrid().getValueRow(rowEdit,columnEdit);
		init();
	}

	/**
	 * Constructor used for text editor
	 * @param edit
	 * @param tableInter
	 * @param columnEdit
	 * @param rowEdit
	 */
	public SelectedEditor(EditorInteraction editInter) {
		super();
		logger.info("build selected editor inter...");
		this.editInter = editInter;
		this.edit = editInter.getEdit();
		this.value = edit.getTextEditorValue();
		init();
	}

	private void init(){
		logger.info("init...");
		if(!edit.getTextEditorFunctionMenu().isEmpty()){
			selectedFunction = edit.getTextEditorFunctionMenu().get(0);
			List<String> l = new ArrayList<String>();
			l.add(calcString(edit.getTextEditorFunctionMenu()));
			setTextEditorFunctionMenuString(l);
			logger.info("TextEditorFunctionMenu " + getTextEditorFunctionMenuString());
			mountComboBoxFunctionsListvalue(selectedFunction);
		}
		if(!edit.getTextEditorOperationMenu().isEmpty()){
			selectedOperation = edit.getTextEditorOperationMenu().get(0);
			List<String> l = new ArrayList<String>();
			l.add(calcString(edit.getTextEditorOperationMenu()));
			setTextEditorOperationMenuString(l);
			logger.info("TextEditorOperationMenu " + getTextEditorOperationMenuString());
			mountComboBoxOperationListvalue(selectedOperation);
		}
	}

	/**
	 * Valid the result and copy it in the back-end object
	 */
	public void confirmValueAndCopy() throws RemoteException {
		logger.info("confirmValueAndCopy");
		setConfirm("Y");
		boolean success = checkTextEditor();

		if (success) {
			HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
			request.removeAttribute("msnError");
			if (tableInter != null) {
				tableInter.getTableGrid().setValueRow(rowEdit,columnEdit,getValue());
			} else {
				editInter.setTextEditorValue(getValue());
			}
		}

	}

	/**
	 * Valid the result
	 * 
	 * @return
	 * @author Igor.Souza
	 * @throws RemoteException
	 */
	public boolean checkTextEditor() throws RemoteException {
		logger.info("checkTextEditor");

		boolean result = false;
		String e = null;
		if (tableInter != null) {
			e = tableInter.getInter().checkExpression(value, getColumnEdit());
		}else{
			String oldCommand = editInter.getTextEditorValue();

			logger.info("oldCommand -> " + oldCommand);
			logger.info("newCommand -> " + getValue());

			editInter.getInter().getTree().getFirstChild("editor")
			.getFirstChild("output").removeAllChildren();
			editInter.getInter().getTree().getFirstChild("editor")
			.getFirstChild("output")
			.add(getValue().trim());

			e = editInter.getInter().check();

			editInter.getInter().getTree().getFirstChild("editor")
			.getFirstChild("output").removeAllChildren();
			if (oldCommand != null) {
				editInter.getInter().getTree().getFirstChild("editor")
				.getFirstChild("output").add(oldCommand);
			}
		}

		if (e != null && e.length() > 0) {
			logger.info("error interaction ->  " + e);
			MessageUseful.addErrorMessage(e);
			HttpServletRequest request = (HttpServletRequest) FacesContext
					.getCurrentInstance().getExternalContext()
					.getRequest();
			request.setAttribute("msnError", "msnError");
		} else {
			//if (getConfirm() != null && !getConfirm().equalsIgnoreCase("Y")) {

			MessageUseful.addInfoMessage(getMessageResources("success_message"));
			HttpServletRequest request = (HttpServletRequest) FacesContext
					.getCurrentInstance().getExternalContext().getRequest();
			request.setAttribute("msnError", "msnError");
			//}
			result = true;
		}

		return result;
	}

	public String calcString(List<String> listFields){
		StringBuffer ans = new StringBuffer();
		for (String selectItem : listFields) {
			ans.append(","+selectItem);
		}
		return ans.toString().substring(1);
	}

	public void mountComboBoxFunctionsListvalue(){
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String nameFunction = params.get("nameFunction");
		setSelectedFunction(nameFunction);
		mountComboBoxFunctionsListvalue(nameFunction);
	}
	
	public void mountComboBoxFunctionsListvalue(String value){
		setListFunctionsCombobox(getEdit().getTextEditorFunctions().get(value));
	}

	public void mountComboBoxOperationListvalue(){
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String nameOperation = params.get("nameOperation");
		setSelectedOperation(nameOperation);
		mountComboBoxOperationListvalue(nameOperation);
	}
	
	public void mountComboBoxOperationListvalue(String value){
		
		List<String> l = new ArrayList<String>();
		List<String[]> list = new ArrayList<String[]>();
		for (String[] a : getEdit().getTextEditorOperations().get(value)) {
			if(!l.contains(a[0])){
				l.add(a[0]);
				list.add(a);
			}
		}
		
		setListOperationCombobox(list);
		
	}

	/**
	 * @return the edit
	 */
	public final EditorFromTree getEdit() {
		return edit;
	}

	/**
	 * @param edit the edit to set
	 */
	public final void setEdit(EditorFromTree edit) {
		this.edit = edit;
	}

	/**
	 * @return the confirm
	 */
	public final String getConfirm() {
		return confirm;
	}

	/**
	 * @param confirm the confirm to set
	 */
	public final void setConfirm(String confirm) {
		this.confirm = confirm;
	}

	/**
	 * @return the columnEdit
	 */
	public final String getColumnEdit() {
		return columnEdit;
	}

	/**
	 * @param columnEdit the columnEdit to set
	 */
	public final void setColumnEdit(String columnEdit) {
		this.columnEdit = columnEdit;
	}

	/**
	 * @return the rowEdit
	 */
	public final int getRowEdit() {
		return rowEdit;
	}

	/**
	 * @param rowEdit the rowEdit to set
	 */
	public final void setRowEdit(int rowEdit) {
		this.rowEdit = rowEdit;
	}

	/**
	 * @return the value
	 */
	public final String getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public final void setValue(String value) {
		this.value = value;
	}

	/**
	 * @return the selectedFunction
	 */
	public String getSelectedFunction() {
		return selectedFunction;
	}

	/**
	 * @param selectedFunction the selectedFunction to set
	 */
	public void setSelectedFunction(String selectedFunction) {
		this.selectedFunction = selectedFunction;
	}

	/**
	 * @return the selectedOperation
	 */
	public String getSelectedOperation() {
		return selectedOperation;
	}

	/**
	 * @param selectedOperation the selectedOperation to set
	 */
	public void setSelectedOperation(String selectedOperation) {
		this.selectedOperation = selectedOperation;
	}

	/**
	 * @return
	 * @see com.redsqirl.interaction.EditorFromTree#getTextEditorFunctionMenu()
	 */
	public final List<String> getTextEditorFunctionMenu() {
		return edit.getTextEditorFunctionMenu();
	}

	/**
	 * @return
	 * @see com.redsqirl.interaction.EditorFromTree#getTextEditorOperationMenu()
	 */
	public final List<String> getTextEditorOperationMenu() {
		return edit.getTextEditorOperationMenu();
	}

	public List<String> getTextEditorFunctionMenuString() {
		return textEditorFunctionMenuString;
	}

	public void setTextEditorFunctionMenuString(
			List<String> textEditorFunctionMenuString) {
		this.textEditorFunctionMenuString = textEditorFunctionMenuString;
	}

	public List<String> getTextEditorOperationMenuString() {
		return textEditorOperationMenuString;
	}

	public void setTextEditorOperationMenuString(
			List<String> textEditorOperationMenuString) {
		this.textEditorOperationMenuString = textEditorOperationMenuString;
	}

	public List<String[]> getListFunctionsCombobox() {
		return listFunctionsCombobox;
	}

	public void setListFunctionsCombobox(List<String[]> listFunctionsCombobox) {
		this.listFunctionsCombobox = listFunctionsCombobox;
	}

	public List<String[]> getListOperationCombobox() {
		return listOperationCombobox;
	}

	public void setListOperationCombobox(List<String[]> listOperationCombobox) {
		this.listOperationCombobox = listOperationCombobox;
	}

}