import java.awt.Color;
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

public class DefineAlternativesPanel extends JPanel implements ActionListener, TableModelListener{

	private static final long serialVersionUID = 1L;

	//all possible objective and alternative information from data file and/or objective construction
	Vector 	all_objs,			//model of all objectives
			alts,				//model of all alternative info				
	//for the table model: these are selected in objective construction				
			rows, 				//subset of all_objs
			columns;			//subset of alts

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
	Vector objs;
	
	DefineAlternativesPanel(ConstructionView c){
		con = c;
		
		//initialize data vectors
		all_objs = new Vector();	
		alts = new Vector();				
		rows = new Vector();		
		columns = new Vector();		
				
		//set up table model and component
		tabModel = new DefaultTableModel();
		tabModel.setDataVector(rows,columns);		
		table = new JTable(tabModel);        
		table.getModel().addTableModelListener(this);		
        table.setAutoCreateColumnsFromModel(true);
        table.setRowSelectionAllowed(true);   
        
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
	public void setFileAlternatives(Vector obj, Vector alt){
		if (obj != null)
			all_objs.addAll(obj);
		alts.addAll(alt);
		num_alts = alts.size();
		updateObjectiveFields();
	}
	
	//resets alternative constuction view to reflect any changes in objectives
	public void updateTable(){
		//add columns
		columns = new Vector();
		columns.addElement("Alternatives");		
		objs = con.getObjPanel().getPrimitiveObjectives();	//do we really need this temp holder?
		for(int i=0; i<objs.size(); i++){
			columns.add(objs.get(i).toString());
		}  		
		
		//add data
		rows = new Vector();
		for (int j=0; j<alts.size(); j++){
			Vector data = new Vector();
			HashMap temp = (HashMap)alts.get(j);
			data.add(temp.get("name"));
			for (int i=0; i<objs.size(); i++){	//for each primitive objective, find each alternative's value
				data.add(temp.get(objs.get(i).toString()));			
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
			JObjective obj = (JObjective)objs.get(i);
			if (obj.getType() == JObjective.DISCRETE){
				JComboBox cboCell;
				if (obj.domain.getElements() != null)
					cboCell = new JComboBox(obj.domain.getElements());
				else
					cboCell = new JComboBox();
				col.setCellEditor(new DefaultCellEditor(cboCell));
		        cboCell.setEditable(true);
			} 		
			//set formatted text field if not
			else {
				DecimalFormat df = new DecimalFormat("0.0"); 
				JFormattedTextField txtCell = new JFormattedTextField(df);
				col.setCellEditor(new DefaultCellEditor(txtCell));
		        txtCell.setColumns(10);
			}
				
		}		
		
		checkAlternativeCount();
	}
	
	public void updateAllObjs(JObjective obj){
		all_objs.add(obj);		
		for (int i=0; i<alts.size(); i++){
			HashMap temp = new HashMap();
			temp = (HashMap)alts.get(i);
			if (!temp.containsKey(obj.getName()))	//new objective			
				temp.put(obj.getName(), null);
		}
	}
	
	public void updateObjDetails(JObjective obj, String name){
		//if there is a change in objective name
		if(!obj.getName().equals(name))
			for (int i=0; i<alts.size(); i++){			
					HashMap temp = (HashMap)alts.get(i);
					temp.put(obj.getName(), temp.get(name));
					temp.remove(name);
			}		
		//changes in objective details
		for (int i=0; i<all_objs.size(); i++){
			if ((all_objs.get(i).toString()).equals(obj.getName()) ||
					(all_objs.get(i).toString()).equals(name)){
				all_objs.remove(i);
				all_objs.add(obj);
				return;
			}				
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
	     checkAlternativeCount();
	   }
	
    public void checkAlternativeCount(){
    	if ((num_alts<2) || ((num_alts>=2)&&(!con.constPane.isEnabledAt(1))) || !con.getObjPanel().ok){//- last part
    		con.constPane.setEnabledAt(2, false);
    		con.constPane.setEnabledAt(3, false); 
    		con.btnOK.setEnabled(false);
    	}
    	else{
    		con.constPane.setEnabledAt(2, true);
    		con.constPane.setEnabledAt(3, true);
    		con.btnOK.setEnabled(true);
    	}
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
	    	Vector data = new Vector();
	        data.addElement(new_name);	 
	        for (int i=0; i<objs.size(); i++){
	        		JObjective obj = (JObjective)objs.get(i);
	        		if (obj.getType()==JObjective.DISCRETE){
	        			data.addElement("");
	        		}
	        		else
	        			data.addElement("");
	        }
	        rows.add(data);
	        tabModel.fireTableRowsInserted(rows.size(), rows.size());   
   
	        //adding to hashmap to update main model
	        HashMap datamap = new HashMap();
	        datamap.put("name", new_name);
	        
	        //add new blank field for all objs
	        for (int i=0; i<all_objs.size(); i++){
        		JObjective obj = (JObjective)all_objs.get(i);
        		if (!obj.getName().equals("name")){
	        		if (obj.getType()==JObjective.DISCRETE){
	        			datamap.put(obj.getName(), "");
	        		}
	        		else
	        			datamap.put(obj.getName(), Double.valueOf(obj.minC));
        		}
	        	
	        }
	        alts.add(datamap);
	        counter++;     	
	        num_alts++;
	        checkAlternativeCount();
        }	        
    }
    
    //this method creates initial value funtions from a read file 
    //need to check if we need the flag:  for now only one method callng it with "true"
    //delete comments staring with //-->
    void updateObjectiveFields(){ //-->boolean from_const){     	
    	Vector v = new Vector();
    	double min = 0, max = 0;
    	for(int i=0; i< all_objs.size(); i++){
    		JObjective obj = (JObjective)all_objs.get(i);
    		//create a vector with unique discrete values
    		if (obj.domain_type==(JObjective.DISCRETE)){
    			//-->if (from_const){
	    			v = new Vector();
		    		for (int j=0; j<alts.size(); j++){
		    			HashMap temp = (HashMap)alts.get(j);
	    				if(!v.contains(temp.get(all_objs.get(i).toString())))
                        	v.add(temp.get(all_objs.get(i).toString()));         
		    		}
    			obj.setDiscrete(v);	        	
    			//-->}
    		}
    			
    		else {    		   //has not been initially set
    			if (!obj.init){//objective domain is CONTINUOUS
    				for (int j=0; j<alts.size(); j++){	    			
			    			HashMap temp = (HashMap)alts.get(j);
			    			Double d = Double.valueOf(temp.get(all_objs.get(i).toString()).toString());
			    			if(j==0)
			    				min=max=d.doubleValue();
			    			if (d.doubleValue() > max)
			    				max = d.doubleValue();
			    			else if (d.doubleValue() < min)
								min = d.doubleValue();	
			    		}
			        	if (!obj.init){
			        		obj.maxC = max;
			        		obj.minC = min;
			        		obj.setContinuous();		
			        		obj.init=true;
			        	}    				
	    			//->}   
			        	//determine decimalFormat
			        	if (obj.getUnit().equals("CAD"))
			        		obj.setDecimalFormat("0.00");
			        	else if (obj.maxC > 100)
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
    			HashMap hm = (HashMap)alts.get(i);
    			str = str + "\"" + hm.get("name") + "\"" + "\n";
    			for (int j=1; j<columns.size(); j++){
    				str = str + "\t" + columns.get(j).toString() + " ";
    				for (int k=0; k<objs.size(); k++){
    					JObjective obj = (JObjective)objs.get(k);
    					if ((obj.getName()).equals(columns.get(j).toString()))
    						if (obj.getType() == JObjective.DISCRETE)
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
    	for (Iterator it=rows.iterator(); it.hasNext();){
    		Vector data = (Vector)it.next();
    		for (Iterator it2=data.iterator(); it2.hasNext();)
    			if ((it2.next().toString()).equals("")){
                    JOptionPane.showMessageDialog(this,
                            "All data values must be input before proceeding",
                            "Missing data",
                            JOptionPane.WARNING_MESSAGE);                    		
                    		return false;
                    }    				
    	}    		
    	return true;
    }
    
    public void actionPerformed(ActionEvent e){ 
    	//PopupMenu listener    
	    	if ("Add".equals(e.getActionCommand()))
	    		addAlternative();    	
	    	else if ("Remove".equals(e.getActionCommand()))
	    		removeAlternative(table.getSelectedRow());
	    	else if ("Rename".equals(e.getActionCommand()))
	    		System.out.println("This is to change the alternative name"); 
	    		
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
	            		menuRemove.setEnabled(true);
	            	}
            	}
         	}
            else if (SwingUtilities.isLeftMouseButton(me)){
            	try{
	            	JTable tab = (JTable)(me.getSource());
	            	if (tab.isEditing()){
	            		last_value = (tab.getValueAt(tab.getSelectedRow(), tab.getSelectedColumn())).toString();
	            		System.out.println("last_value: " + last_value);
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
		        		HashMap temp = (HashMap)alts.get(table.getSelectedRow());
		        		//last_value = (temp.get((columns.get(table.getSelectedColumn()).toString()))).toString();
	
		        		temp.put((columns.get(table.getSelectedColumn()).toString()), entered);
		        		alts.set(table.getSelectedRow(), temp); 
		        		//check if data is valid:
		        		//1. get the objective	        		
		        		JObjective obj = null;
//		        		JObjective prim = null; 
		        		boolean found = false;
		        		for (int i=0; i<all_objs.size(); i++){
		        			if (all_objs.get(i).toString().equals((columns.get(table.getSelectedColumn()).toString()))){
		        				obj = (JObjective)all_objs.get(i);	        				
		        				for (int j=0; j<con.getObjPanel().prim_obj.size(); j++){
		        					if (obj.getName()==con.getObjPanel().prim_obj.get(j).toString()){
//		        						prim = (JObjective)con.getObjPanel().prim_obj.get(j);
		        					}
		        				}	        				
		        				break;	        				
		        			}
		        		}	
		        		
		        		//2. discrete items
		        		//a) check if in list
		        		if (obj!=null && obj.domain_type==JObjective.DISCRETE){
		        			String elts[] = obj.domain.getElements();
		        			try{
			        			for (int i=0; i<elts.length; i++){
			        				if (entered.equals(elts[i])){ 
			        						found = true; 
			        						break;
				        			}
			        			}	
		        			}catch(NullPointerException ne){}
		        			//b) if new, prompt for confirmation 
		        			if (!found || elts.length == 0){	        	
		                        int n = JOptionPane.showConfirmDialog(
		                                this, "This is not currently a value on the list.  Would you like to add it?",
		                                "New discrete value",
		                                JOptionPane.YES_NO_CANCEL_OPTION);
		                        //c)add the new discrete value 
		                        if (n == JOptionPane.YES_OPTION) {
		                        	DiscreteAttributeDomain dom = (DiscreteAttributeDomain)obj.domain;
		                        	dom.addElement(entered, 0.5); 		                        	
		                        	updateTable(); //d. update for new combobox item
		                        }
		                        else{ //reset for cancel or no
		        	        		temp.put((columns.get(table.getSelectedColumn()).toString()), last_value);
		        	        		alts.set(table.getSelectedRow(), temp); 
		        	        		table.setValueAt(last_value, table.getSelectedRow(), table.getSelectedColumn());
		                        }
		        			}
		        		}	        		
		        		//3. continuous items
		        		else
		        			//a) check if within range
			        		if((obj.minC>Double.valueOf(entered).doubleValue())||(obj.maxC<Double.valueOf(entered).doubleValue())){
			        			//b)if not, prompt for confirmation
		                        int n = JOptionPane.showConfirmDialog(
		                                this, "The value you entered is not within the current range.  Are you sure you would like to enter it?",
		                                "Value out of range",
		                                JOptionPane.YES_NO_CANCEL_OPTION);
		                        //c)update the range information
		                        if (n == JOptionPane.YES_OPTION){
		                            JOptionPane.showMessageDialog(this,		//inform the user that value function is affected
		                                    "The value function for this objective must be reset.",
		                                    "Reminder",
		                                    JOptionPane.INFORMATION_MESSAGE);
		                         	if (obj.minC>Double.valueOf(entered).doubleValue()){
		                        		obj.minC = Double.valueOf(entered).doubleValue();
		                        	}
		                        	else
		                        		obj.maxC = Double.valueOf(entered).doubleValue();                        	
		                        	obj.setContinuous();	//reset to default value function
		                        }
		                        else{ //reset for cancel or no
		        	        		temp.put((columns.get(table.getSelectedColumn()).toString()), last_value);
		        	        		alts.set(table.getSelectedRow(), temp); 
		        	        		table.setValueAt(last_value, table.getSelectedRow(), table.getSelectedColumn());
		                        }                        	
			        		}       	
		        		}
	        		else if (!last_value.equals(""))
	        				table.setValueAt(last_value, table.getEditingRow(), table.getEditingColumn());
	        		break;}
	        		catch (ArrayIndexOutOfBoundsException ex){}  
	        		catch (NullPointerException ne){}         		
	    }
	}
}