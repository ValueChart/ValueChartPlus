import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;

import javax.swing.JFormattedTextField;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import javax.swing.event.MouseInputAdapter;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

/**
 * Contained in ConstructionView. Table for defining and setting alternative values
 * 
 *
 */
public class DefineAlternativesPanel extends JPanel implements ActionListener, TableModelListener{

	private static final long serialVersionUID = 1L;

	//all possible objective and alternative information from data file and/or objective construction
	Vector<HashMap<String,Object>> alts;				//model of all alternative info				
	//for the table model: these are selected in objective construction				
	Vector<Vector<String>> rows; 				//subset of all_objs
	Vector<String> columns;			//subset of alts

	//gui table components
	static JTable table;	
	static DefaultTableModel tabModel;
	JScrollPane scrollPane;
	TableColumn colNames;
	
	MouseHandler mouseListener;
	
	//popup menu components
	JPopupMenu popAlternative;	
	JMenuItem menuAdd;
	JMenuItem menuRemove;
	JMenuItem menuRename;	
	String sel_alt;
	
	//counters
	int counter = 0;
	int num_alts;
	ConstructionView con;
	String last_value;
	// may want to remove this and use getPrimitiveObjectives from ObjectivesPanel if data doesn't sync between panels
	Vector<JObjective> objs; 
	
	DefineAlternativesPanel(ConstructionView c){
		con = c;
		
		//initialize data vectors
		alts = new Vector<HashMap<String,Object>>();				
		rows = new Vector<Vector<String>>();		
		columns = new Vector<String>();		
				
		//set up table model and component
		tabModel = new DefaultTableModel() {
            private static final long serialVersionUID = 1L;

            @Override 
            public boolean isCellEditable(int row, int column) {
                // alternative name column
		        if (column == 0) return false;
		        return true;
		    }
		};
		tabModel.setDataVector(rows,columns);		
		table = new JTable(tabModel);        
		table.getModel().addTableModelListener(this);		
        table.setAutoCreateColumnsFromModel(true);
        table.setRowSelectionAllowed(true);   
        table.getTableHeader().setReorderingAllowed(false);
        
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane);   
        addComponentListener(new ResizeHandler());
    	table.setSize(new Dimension(getWidth()- 20, getHeight() - 75));
        table.setPreferredScrollableViewportSize(table.getSize());
        
        JTableHeader th = table.getTableHeader();
        th.setPreferredSize(new Dimension (50, 30));
        th.setMaximumSize(new Dimension (50, 30));
        Font font = new Font("Arial", Font.BOLD, 12);
        th.setFont(font);
        table.setRowHeight(20);
        
        //add popup menu for right-click
		popAlternative = new JPopupMenu();		
	    menuAdd = new JMenuItem("Add");
	    menuAdd.addActionListener(this);
	    popAlternative.add(menuAdd);
	    menuRemove = new JMenuItem("Remove");
	    menuRemove.addActionListener(this);
	    popAlternative.add(menuRemove);
	    popAlternative.addSeparator();
	    menuRename = new JMenuItem("Rename");
	    menuRename.addActionListener(this);
	    popAlternative.add(menuRename);	    
	    
	    //add listeners
		mouseListener = new MouseHandler();		
		table.addMouseListener(mouseListener);	
		table.getTableHeader().addMouseListener(mouseListener);
	}
	
	//this method sets the initial values of the column headers and data
	public void setFileAlternatives(Vector<HashMap<String,Object>> alt){
		alts.addAll(alt);
		num_alts = alts.size();
	}
	
	public static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
        } catch (NumberFormatException ex) {
            return false;
        }
        return true;
	}
	
	//resets alternative constuction view to reflect any changes in objectives
	public void updateTable(){
		//add columns
		columns.clear();
		columns.addElement("Alternatives");		
		objs = con.getObjPanel().getPrimitiveObjectives();	//do we really need this temp holder?
		for(int i=0; i<objs.size(); i++){
			columns.add(objs.get(i).toString());
		}  		
		
		//add data
		rows.clear();
		for (int j=0; j<alts.size(); j++){
			Vector<String> data = new Vector<String>();
			HashMap<String,Object> temp = alts.get(j);
			data.add(temp.get("name").toString());
			for (int i=0; i<objs.size(); i++){	//for each primitive objective, find each alternative's value
                JObjective obj = objs.get(i);
		        if (j == 0)
		            obj.clearObjValueMap();

			    Object o = temp.get(objs.get(i).toString());
			    if (o != null) {
			        boolean numeric = isNumeric(o.toString());
			        if (obj.getDomainType() == AttributeDomainType.CONTINUOUS) { 
			            if(!numeric) {
			                data.add("");
			            } else {
			                data.add(o.toString());
			                double val = Double.valueOf(o.toString()).doubleValue();
			                obj.addToObjValueMap(val);
			            }
			        } else if (obj.getDomainType() == AttributeDomainType.DISCRETE) {
			            if (numeric) {
			                data.add("");
			            } else {
			                data.add(o.toString());
			                obj.addToObjValueMap(o.toString());
			            }
			        }
			    }
			    else
			        data.add("");
			}		
		rows.add(data);
		}
		
		//apply it to the JTable view
		tabModel.setDataVector(rows,columns);		
		tabModel.fireTableStructureChanged();
		
		colNames = table.getColumnModel().getColumn(0);
		
		DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();
		dtcr.setBackground(new Color(238, 238, 238));
		dtcr.setFont(new Font("Arial", Font.BOLD, 9));
		colNames.setCellRenderer(dtcr);
		colNames.setMaxWidth(100);
		colNames.setPreferredWidth(100);
		colNames.setResizable(false);
		dtcr.setBorder(null);
		
		//determine data that can be entered by combo box
		//JComboBox[] cbox= new JComboBox[columns.size()];
		for(int i=0; i<objs.size(); i++){	
			//set combobox if discrete
			TableColumn col = table.getColumnModel().getColumn(i+1);
			JObjective obj = objs.get(i);
			if (obj.getDomainType() == AttributeDomainType.DISCRETE){
			    col.setCellRenderer(new DiscreteValidationCellRenderer());
				JComboBox<String> cboCell;
				if (obj.getDomain().getElements() != null)
					cboCell = new JComboBox<String>(obj.getDomain().getElements());
				else
					cboCell = new JComboBox<String>();
				col.setCellEditor(new DefaultCellEditor(cboCell));
		        cboCell.setEditable(true);
			} 		
			//set formatted text field if not
			else {
	            col.setCellRenderer(new ContinuousValidationCellRenderer());
				DecimalFormat df = new DecimalFormat("0.0"); 
				JFormattedTextField txtCell = new JFormattedTextField(df);
				col.setCellEditor(new DefaultCellEditor(txtCell));
		        txtCell.setColumns(10);
			}
				
		}		
		
        if (con != null)
            con.validateTabs();      
    }
	
	public void updateObjDetails(JObjective obj, String name){
		//if there is a change in objective name
		if(!obj.getName().equals(name))
			for (int i=0; i<alts.size(); i++){			
					HashMap<String,Object> temp = alts.get(i);
					temp.put(obj.getName(), temp.get(name));
					temp.remove(name);
			}		
	}
	void removeAlternative(int index) 
	   {
	     if(index!=-1)//At least one Row in Table
	      { 
	        rows.removeElementAt(index);        
	        table.addNotify();
	        alts.removeElementAt(index);
	       }
	     num_alts--;
	     if (con != null)
	            con.validateTabs(); 
	   }

    
    void renameAlternative(int index){
        String old_name = rows.get(index).get(0);
        String new_name = (String)JOptionPane.showInputDialog(
                this,
                "Alternative name:",
                "Rename Alternative",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                old_name);
        // no change, return
        if (new_name == null || new_name.isEmpty() 
             || new_name.equals(old_name)) 
            return;
        
        if (hasDuplicate(new_name)) {
            JOptionPane.showMessageDialog(this, "Alternative name " + new_name + " already exists");
        } else {
            HashMap<String,Object> val = alts.get(index);
            val.put("name", new_name);
            rows.get(index).set(0, new_name);
            table.addNotify();
        }
    }
    
    private boolean hasDuplicate(String str){
        for (Vector<String> row : rows) {
            if (row.get(0).equals(str))
                return true;
        }
        return false;
    }
    
    void addAlternative(){
        String new_name = (String)JOptionPane.showInputDialog(
                this,
                "Alternative name:",
                "Add New Alternative",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                "Alt(" + counter + ")");
        if ((new_name != null) && (new_name.length() > 0)) {
	        //adding to data to update the current model
	    	Vector<String> data = new Vector<String>();
	        data.addElement(new_name);	 
	        for (int i=0; i<objs.size(); i++){
	        		JObjective obj = objs.get(i);
	        		if (obj.getDomainType()==AttributeDomainType.DISCRETE){
	        			data.addElement("");
	        		}
	        		else {
	        		    double minC = obj.getDomain().getContinuous().getMin();
	        		    double maxC = obj.getDomain().getContinuous().getMax();
	        		    if (minC > maxC) {
	        		        data.addElement("");
	        		    } else {
	        		        data.addElement(Double.valueOf(minC).toString());
	        		    }
	        		}
	        }
	        rows.add(data);
	        tabModel.fireTableRowsInserted(rows.size(), rows.size());   
   
	        //adding to hashmap to update main model
	        HashMap<String,Object> datamap = new HashMap<String,Object>();
	        datamap.put("name", new_name);
	        
	        //add new blank field for all objs
	        
	        Vector<JObjective> all_objs = con.getObjPanel().getPrimitiveObjectives();
	        for (int i=0; i<all_objs.size(); i++){
        		JObjective obj = all_objs.get(i);
        		if (!obj.getName().equals("name")){
	        		if (obj.getDomainType()==AttributeDomainType.DISCRETE){
	        			datamap.put(obj.getName(), "");
	        		}
	        		else {
	        		    double minC = obj.getDomain().getContinuous().getMin();
                        double maxC = obj.getDomain().getContinuous().getMax();
	        		    if (minC > maxC) {
	        		        datamap.put(obj.getName(), "");
                        } else {
                            datamap.put(obj.getName(), Double.valueOf(minC));
                        }
	        		}
        		}
	        	
	        }
	        alts.add(datamap);
	        counter++;     	
	        num_alts++;
	        if (con != null)
	            con.validateTabs(); 
        }	        
    }
    
    //this method creates initial value funtions from a read file 
    //need to check if we need the flag:  for now only one method callng it with "true"
    //delete comments staring with //-->
    void updateObjectiveFields(){ //-->boolean from_const){     	
    	Vector<Object> v = new Vector<Object>();
    	double min = 0, max = 0;
    	Vector<JObjective> all_objs = con.getObjPanel().getPrimitiveObjectives();
    	for(int i=0; i< all_objs.size(); i++){
    		JObjective obj = all_objs.get(i);
    		//create a vector with unique discrete values
    		if (obj.getDomainType()==(AttributeDomainType.DISCRETE)){
    			//-->if (from_const){
	    			v = new Vector<Object>();
		    		for (int j=0; j<alts.size(); j++){
		    			HashMap<String,Object> temp = alts.get(j);
	    				if(!v.contains(temp.get(all_objs.get(i).toString())))
                        	v.add(temp.get(all_objs.get(i).toString()));         
		    		}
    			obj.setDiscrete(v);	        	
    			//-->}
    		}
    			
    		else {    		   //has not been initially set
    			if (!obj.init){//objective domain is CONTINUOUS
    				for (int j=0; j<alts.size(); j++){	    			
			    			HashMap<String,Object> temp = alts.get(j);
			    			Double d = Double.valueOf(temp.get(all_objs.get(i).toString()).toString());
			    			if(j==0)
			    				min=max=d.doubleValue();
			    			if (d.doubleValue() > max)
			    				max = d.doubleValue();
			    			else if (d.doubleValue() < min)
								min = d.doubleValue();	
			    		}
			        	if (!obj.init){
			        		obj.setContinuous();		
			        		obj.init=true;
			        	}    				
	    			//->}   
			        	//determine decimalFormat
			        	if (obj.getUnit().equals("CAD"))
			        		obj.setDecimalFormat("0.00");
			        	else if (obj.getDomain().getContinuous().getMax() > 100)
			        		obj.setDecimalFormat("0");
			        	else
			        		obj.setDecimalFormat("0.0");
	    		}    
    		}
    	}
    }

    public String getAlternativeOutput(){
    	String str = "";
    		
		for (int i=0; i<alts.size(); i++){
			str = str + "\nentry ";    			
			HashMap<String,Object> hm = alts.get(i);
			str = str + "\"" + hm.get("name") + "\"" + "\n";
			for (int j=1; j<columns.size(); j++){
				str = str + "\t" + columns.get(j).toString() + " ";
				for (int k=0; k<objs.size(); k++){
					JObjective obj = objs.get(k);
					if ((obj.getName()).equals(columns.get(j).toString()))
						if (obj.getDomainType() == AttributeDomainType.DISCRETE)
							str = str + "\"" + hm.get(columns.get(j).toString()) + "\"" + "\n";
						else 
							str = str + hm.get(columns.get(j).toString()) + "\n";
				}
			}
			str = str + "end\n";
		}
    		
		return str;
    
    }
    
    public boolean checkFields(){    
    	for (Iterator<Vector<String>> it=rows.iterator(); it.hasNext();){
    		Vector<String> data = it.next();
    		for (Iterator<String> it2=data.iterator(); it2.hasNext();)
    			if (it2.next().equals(""))
    			    return false;
    	}    		
    	Vector<JObjective> all_objs = con.getObjPanel().getPrimitiveObjectives();
    	for (JObjective obj : all_objs) {
    	    if (obj.getObjValueMapCount() < 2) return false;
    	}
    	return true;
    }
    
    public void actionPerformed(ActionEvent e){ 
    	//PopupMenu listener    
	    	if ("Add".equals(e.getActionCommand()))
	    		addAlternative();    	
	    	else if ("Remove".equals(e.getActionCommand()))
	    		removeAlternative(table.getSelectedRow());
	    	else if ("Rename".equals(e.getActionCommand())) {
	    		renameAlternative(table.getSelectedRow());
	    	}
	    		
    }	
    
    private class ResizeHandler extends ComponentAdapter{
        public void componentResized(ComponentEvent e){        	
        	setSize(con.constPane.getSize());
        	//System.out.println("resize alt " + getWidth());
        	table.setSize(new Dimension(getWidth()- 20, getHeight() - 75));
            table.setPreferredScrollableViewportSize(table.getSize());
        }
    }
	
	class MouseHandler extends MouseInputAdapter{
		public void mousePressed(MouseEvent me){
            if(SwingUtilities.isRightMouseButton(me)){
            	if (table.columnAtPoint(me.getPoint()) == 0){
	             	int index=table.getSelectedRow();
	     			popAlternative.show(me.getComponent(), me.getX()+5, me.getY()+5);            	
	            	if(index==-1){
						menuRemove.setEnabled(false);
						menuRename.setEnabled(false);
	            	}
	            	else{
	            		menuRemove.setEnabled(true);
	            		menuRename.setEnabled(true);
	            	}
            	}
         	}
            else if (SwingUtilities.isLeftMouseButton(me) && (me.getSource() instanceof JTable)){
            	try{
	            	JTable tab = (JTable)(me.getSource());
	            	if (tab.isEditing()){
	            	    if (tab.getValueAt(tab.getSelectedRow(), tab.getSelectedColumn()) != null) {
    	            		last_value = (tab.getValueAt(tab.getSelectedRow(), tab.getSelectedColumn())).toString();
    	            		System.out.println("last_value: " + last_value);
	            	    }
	            	}
            	}	catch (java.lang.ClassCastException cce){}
        			catch (java.lang.NumberFormatException nfe){}
        			catch (java.lang.NullPointerException npe){
        				last_value = null;
        			}
            }         	
		}
	}
	
	//Updating the tableModel should also update the main data model
	/* (non-Javadoc)
	 * @see javax.swing.event.TableModelListener#tableChanged(javax.swing.event.TableModelEvent)
	 */
	/* (non-Javadoc)
	 * @see javax.swing.event.TableModelListener#tableChanged(javax.swing.event.TableModelEvent)
	 */
	public void tableChanged(TableModelEvent e) {
        switch (e.getType()){     		
	        case TableModelEvent.UPDATE:
	        	try{   	  
	        		//update the new data	
	        		String entered = table.getValueAt(table.getSelectedRow(),table.getSelectedColumn()).toString();
	        		if (!entered.equals("")){
		        		HashMap<String,Object> temp = alts.get(table.getSelectedRow());
		        		//last_value = (temp.get((columns.get(table.getSelectedColumn()).toString()))).toString();
		        		String objName = (columns.get(table.getSelectedColumn()).toString());
		        		temp.put(objName, entered);
		        		alts.set(table.getSelectedRow(), temp); 
		        		
		        		//check if data is valid:
		        		//1. get the objective	        		
		        		JObjective obj = null;
		        		boolean found = false;
		        		Vector<JObjective> all_objs = con.getObjPanel().getPrimitiveObjectives();
		        		for (int i=0; i<all_objs.size(); i++){
		        			if (all_objs.get(i).toString().equals((columns.get(table.getSelectedColumn()).toString()))){
		        				obj = all_objs.get(i);	        				        				
		        				break;	        				
		        			}
		        		}	
		        		
		        		// ---------------------------------------------------------------
		        		//2. discrete items
		        		//a) check if in list
		        		if (obj!=null && obj.getDomainType()==AttributeDomainType.DISCRETE){
		        		    
		        		    if (!isNumeric(entered)) {
    		        		    found = obj.containsKey(entered);
                                obj.replaceInObjValueMap(last_value, entered);
    
    		        			//b) if new, prompt for confirmation 
    		        			if (!found){	        	
    		                        int n = JOptionPane.showConfirmDialog(
    		                                this, "This is not currently a value on the list.  Would you like to add it?",
    		                                "New discrete value",
    		                                JOptionPane.YES_NO_CANCEL_OPTION);
    		                        //c)add the new discrete value 
    		                        if (n == JOptionPane.YES_OPTION) {
    		                        	DiscreteAttributeDomain dom = obj.getDomain().getDiscrete();
    		                        	dom.addElement(entered, 0.5); 		                        	
    		                            String name = temp.get("name").toString();
    		                            if (con.chart != null) {
    		                                ChartEntry entry = con.chart.getEntry(name);
    		                                if (entry != null) {
    		                                    AttributeValue value = entry.attributeValue(objName);
                                                if (value != null) {
                                                    value.str = entered;
                                                }
    		                                }
    		                            }
    		                        	updateTable(); //d. update for new combobox item
    		                        }
    		                        else{ //reset for cancel or no
    		        	        		temp.put((columns.get(table.getSelectedColumn()).toString()), last_value);
    		        	        		alts.set(table.getSelectedRow(), temp); 
    		        	        		table.setValueAt(last_value, table.getSelectedRow(), table.getSelectedColumn());
    		                            obj.replaceInObjValueMap(entered, last_value);
    		                        }
    		        			}
    		        			// already in list
    		        			else {                                  
                                    String name = temp.get("name").toString();
                                    if (con.chart != null) {
                                        ChartEntry entry = con.chart.getEntry(name);
                                        if (entry != null) {
                                            AttributeValue value = entry.attributeValue(objName);
                                            if (value != null) {
                                                value.str = entered;
                                            }
                                        }
                                    }
    		        			}
		        		    } else {
                                temp.put((columns.get(table.getSelectedColumn()).toString()), last_value);
                                alts.set(table.getSelectedRow(), temp); 
                                table.setValueAt(last_value, table.getSelectedRow(), table.getSelectedColumn());
                                entered = last_value;
		        		    }
		        		}	        		
		        		// ---------------------------------------------------------------
                        //3. continuous items
		        		else if (obj!=null && obj.getDomainType()==AttributeDomainType.CONTINUOUS){
                            AttributeValue value = null;
                            double minC = obj.getDomain().getContinuous().getMin();
                            double maxC = obj.getDomain().getContinuous().getMax();
                            
		        			//a) check if within range
		        		    try {
		        		        double enteredVal = Double.valueOf(entered).doubleValue();
		        		        double lastVal = 0;
		        		        if (last_value != null && !last_value.isEmpty()) {
		        		            lastVal = Double.valueOf(last_value).doubleValue();
		        		            obj.replaceInObjValueMap(lastVal, enteredVal);
		        		        } else {
		        		            obj.addToObjValueMap(enteredVal);
		        		        }
		        		        
                                String name = temp.get("name").toString();
                                if (con.chart != null) {
                                    ChartEntry entry = con.chart.getEntry(name);
                                    if (entry != null) {
                                        value = entry.attributeValue(objName);
                                        if (value != null) {
                                            value.num = enteredVal;
                                        }
                                    }
                                }
			        		if((minC>enteredVal)||(maxC<enteredVal)){
			        			//b)if not, prompt for confirmation
		                        int n = JOptionPane.showConfirmDialog(
		                                this, "The value you entered is not within the current range.  Are you sure you would like to enter it?",
		                                "Value out of range",
		                                JOptionPane.YES_NO_CANCEL_OPTION);
		                        //c)update the range information
		                        if (n == JOptionPane.YES_OPTION){
		                            double newMin = (minC < enteredVal ? minC : enteredVal);
		                            double newMax = (maxC > enteredVal ? maxC : enteredVal);
		                            
		                            boolean updated = obj.getDomain().getContinuous().updateRange(newMin, newMax);
                                    
		                            if (!updated) {
		                                JOptionPane.showMessageDialog(this,		//inform the user that value function is affected
		                                    "The value function for this objective must be reset.",
		                                    "Reminder",
		                                    JOptionPane.INFORMATION_MESSAGE);

		                                obj.resetWeights();	//reset to default value function
		                            }
		                        }
		                        else{ //reset for cancel or no
		        	        		temp.put((columns.get(table.getSelectedColumn()).toString()), last_value);
		        	        		alts.set(table.getSelectedRow(), temp); 
		        	        		table.setValueAt(last_value, table.getSelectedRow(), table.getSelectedColumn());
		        	        		if (!last_value.isEmpty()) {
    	                                obj.replaceInObjValueMap(enteredVal, lastVal);
                                        if (value != null) {
                                            value.num = lastVal;
                                        }
		        	        		} else {
		        	        		    obj.decreaseInObjValueMap(enteredVal);
		        	        		}
                                    entered = last_value;
		                        }                        	
			        		}   
			        		// a) check if range is smaller
			        		else if (  (minC==lastVal && !obj.containsKey(lastVal)) 
			        		        || (maxC==lastVal && !obj.containsKey(lastVal)) ) {
	                               //b)if yes, prompt for confirmation
                                int n = JOptionPane.showConfirmDialog(
                                        this, "The value you entered requires updating the current range.  Are you sure you would like to enter it?",
                                        "Value out of range",
                                        JOptionPane.YES_NO_CANCEL_OPTION);
                                //c)update the range information
                                if (n == JOptionPane.YES_OPTION){
                                    Iterator<Object> it = obj.getKeySet().iterator();
                                    double newMax = Double.MIN_VALUE;
                                    double newMin = Double.MAX_VALUE;
                                    while (it.hasNext()) {
                                        Double curr = (Double)it.next();
                                        if (curr > newMax)
                                            newMax = curr;
                                        if (curr < newMin)
                                            newMin = curr;
                                    }
                                    
                                    boolean updated = obj.getDomain().getContinuous().updateRange(newMin, newMax);
                                    
                                    if (!updated) {
                                        JOptionPane.showMessageDialog(this,     //inform the user that value function is affected
                                            "The value function for this objective must be reset.",
                                            "Reminder",
                                            JOptionPane.INFORMATION_MESSAGE);

                                        obj.resetWeights();    //reset to default value function
                                    }

                                }
                                else{ //reset for cancel or no
                                    temp.put((columns.get(table.getSelectedColumn()).toString()), last_value);
                                    alts.set(table.getSelectedRow(), temp); 
                                    table.setValueAt(last_value, table.getSelectedRow(), table.getSelectedColumn());
                                    obj.replaceInObjValueMap(enteredVal, lastVal);
                                    if (value != null) {
                                        value.num = lastVal;
                                    }
                                    entered = last_value;
                                } 
			        		    
			        		}
		        		    } catch (NumberFormatException ex) {
                                temp.put((columns.get(table.getSelectedColumn()).toString()), last_value);
                                alts.set(table.getSelectedRow(), temp); 
                                table.setValueAt(last_value, table.getSelectedRow(), table.getSelectedColumn());
                                entered = last_value;
		        		    }
		        		}
	        		}
	        		else if (!last_value.equals(""))
	        				table.setValueAt(last_value, table.getEditingRow(), table.getEditingColumn());
	        		
	        		last_value = entered;
	        		
	        		if (con != null) {
	                    con.validateTabs();
	                    if (con.chart != null)
	                        con.chart.updateAll();
	        		}	        		    
	        		break;}
	        		catch (ArrayIndexOutOfBoundsException ex){}  
	        		catch (NullPointerException ne){}         		
	    }
	}
	
    public class ContinuousValidationCellRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        public ContinuousValidationCellRenderer() {
            super();
        }
        
        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            
            Component rendererComp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
                    row, column);
            
            if (value.toString().isEmpty() || !isNumeric(value.toString())) {
                // selection 
                if (isSelected) {
                    rendererComp.setBackground(new Color(103,0,13));
                } else {
                    rendererComp.setBackground(new Color(222,45,38));
                }
                rendererComp.setForeground(Color.white);
            } else {
                if (isSelected) {
                    rendererComp.setBackground(table.getSelectionBackground());
                    rendererComp.setForeground(table.getSelectionForeground());
                } else {
                    rendererComp.setBackground(table.getBackground());
                    rendererComp.setForeground(table.getForeground());
                }
            }
            
            return this;
        }
    }
    
    public class DiscreteValidationCellRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        public DiscreteValidationCellRenderer() {
            super();
        }
        
        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            
            Component rendererComp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
                    row, column);
            
            if (value.toString().isEmpty() || isNumeric(value.toString())) {
                // selection 
                if (isSelected) {
                    rendererComp.setBackground(new Color(103,0,13));
                } else {
                    rendererComp.setBackground(new Color(222,45,38));
                }
                rendererComp.setForeground(Color.white);
            } else {
                if (isSelected) {
                    rendererComp.setBackground(table.getSelectionBackground());
                    rendererComp.setForeground(table.getSelectionForeground());
                } else {
                    rendererComp.setBackground(table.getBackground());
                    rendererComp.setForeground(table.getForeground());
                }
            }
            
            return this;
        }
    }
}