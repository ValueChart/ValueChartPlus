import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class DefineInitialWeights extends JPanel implements ActionListener{
	private static final long serialVersionUID = 1L;

	static final int ROW_HEIGHT = 20;
	
	String new_sel;
	Vector objs;
	
	Vector rows, columns;
    Vector rowstemp;
	JTable table;	
	TableHandler tabModel;  
	JScrollPane scrollPane;
	JPanel pnlTable;
	
	int count = 0;
	DecimalFormat df;
	ConstructionView con;
		
	JPopupMenu popWeighting;
	
	DefineInitialWeights(ConstructionView c){
		con = c;
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		pnlTable = new JPanel();
		df = new DecimalFormat("0.000");	
		
		popWeighting = new JPopupMenu();		
	    JMenuItem menuItem = new JMenuItem("SMARTER...");
	    menuItem.addActionListener(this);
	    popWeighting.add(menuItem);
	    menuItem = new JMenuItem("Set equal weights");
	    menuItem.addActionListener(this);
	    popWeighting.add(menuItem);	    
	    menuItem = new JMenuItem("Reset");
	    menuItem.addActionListener(this);
	    popWeighting.add(menuItem);
	    add(Box.createRigidArea(new Dimension(0, 15)));
		obj_map = new HashMap();
	}
	HashMap obj_map;
	void setObjectiveList(){
		pnlTable.removeAll();
		objs = new Vector();
		rowstemp = new Vector();
		
		objs = con.getObjPanel().getPrimitiveObjectives();
		
		//create hashmap for reference
		obj_map.clear();
		for (Iterator it = objs.iterator(); it.hasNext();){
				JObjective obj = (JObjective)it.next();
				obj_map.put(obj.getName(), obj);
		}		
		rows = new Vector();			
		columns = new Vector();	
		
		columns.add("");
		columns.add("Worst");
		columns.add("Best");
		columns.add("Weight");	
	
		updateWeights();
		//set up table model and component
		tabModel = new TableHandler();
		tabModel.setDataVector(rows,columns);		
		table = new JTable(tabModel); 
		table.setRowHeight(ROW_HEIGHT);
        table.setPreferredScrollableViewportSize(new Dimension(400, getHeight() - 100));
        table.setPreferredSize(new Dimension(400, (rows.size()) * ROW_HEIGHT));
        table.setMaximumSize(new Dimension(400, 400));
        table.setAutoCreateColumnsFromModel(true);
        table.setRowSelectionAllowed(false);
        table.addMouseListener(new MouseHandler()); 
        table.setGridColor(Color.white);
        table.setBorder(BorderFactory.createEtchedBorder(1));

        scrollPane = new JScrollPane(table);
        pnlTable.add(scrollPane);
		JTableHeader theader = table.getTableHeader();
		theader.setPreferredSize(new Dimension(table.getWidth(), ROW_HEIGHT));
		theader.setMaximumSize(new Dimension(table.getWidth(), ROW_HEIGHT));
		theader.setBackground(Color.WHITE);
		add(pnlTable); 
	}
	
	void updateWeights(){
		JObjective obj;
		Vector entry;
		boolean allequal = true;
		Double equalWeight = new Double(1.0 / objs.size());
		for (int i=0; i<objs.size(); i++)
			if (!(((JObjective)(objs.get(i))).getWeight().equals("*"))){
				allequal = false;
				break;
			}
		for (int i=0; i<objs.size(); i++){			
			 entry = new Vector();			 
			 obj=(JObjective)objs.get(i);
			 entry.add(obj.getName());
			 entry.add(null);
			 entry.add(null);
			 double wt[] = obj.getDomain().getWeights();			 
			 if (obj.getType()==JObjective.DISCRETE){
			 	String elt[] = obj.getDomain().getElements();
				 for (int j=0; j<wt.length; j++){
					 	if (wt[j]==0.0)
				 		entry.set(1, elt[j]);	
				 	if (wt[j]==1.0)
				 		entry.set(2, elt[j]);	
				 }
			 }
			 else{
			 	double kt[] = obj.getDomain().getKnots();
				 for (int j=0; j<wt.length; j++){	
				 	if (wt[j]==0.0)
				 		entry.set(1, (String.valueOf(obj.decimalFormat.format(kt[j]))) + " " + obj.unit);
				 	if (wt[j]==1.0)
				 		entry.set(2, (String.valueOf(obj.decimalFormat.format(kt[j]))) + " " + obj.unit);				 		
				 }
			 }		 
			 if (allequal){
			 	entry.add(String.valueOf(obj.decimalFormat.format(equalWeight)));
			 	obj.setWeight(String.valueOf(obj.decimalFormat.format(equalWeight)));
			 }
			 else
			 	if(obj.getWeight().equals("*")){
			 		entry.add(String.valueOf("0.00"));
			 		obj.setWeight("0.00");
			 	}
			 	else
			 		entry.add(df.format(Double.valueOf(obj.getWeight()).doubleValue()));
			 rows.add(entry);
		}			

	}
	
    public void addFromSMARTER(Vector data){
	    	Vector entry = data;
			entry.add(String.valueOf(count));
			entry.add("");
			rowstemp.add(entry);
			tabModel.fireTableRowsInserted(rows.size(), rows.size());
		}		    	
    
	public void actionPerformed(ActionEvent e) {
        if ("SMARTER...".equals(e.getActionCommand())) {
        	count = 1;	//reset counter;
        	WeightingBySMARTER wbs = new WeightingBySMARTER();
        	wbs.startWeighting();
        	rowstemp.clear();
        }
        if ("Set equal weights".equals(e.getActionCommand())) {
        	double w = 1.0/(double)rows.size();
        	
			//update display
        	for (int i=0; i<rows.size(); i++){
			Vector v = (Vector)rows.get(i);
			v.set(3, String.valueOf(df.format(w)));
			tabModel.fireTableCellUpdated(i, 4);

			//update objective weight
			JObjective obj = (JObjective)obj_map.get(v.get(0));
			obj.setWeight(String.valueOf(df.format(w)));
			
			con.btnOK.setEnabled(true);
			con.repaint();
        	}

        }
	}

	class MouseHandler extends MouseInputAdapter{
		public void mousePressed(MouseEvent me){
            if(SwingUtilities.isRightMouseButton(me))
            	popWeighting.show(me.getComponent(), me.getX(), me.getY());            	
		}
	}
	
	class WeightingBySMARTER extends JPanel implements ActionListener{		
		private static final long serialVersionUID = 1L;

		JDialog frame;
		
		String q1 = "Imagine the worst possible alternative (i.e. scoring 0 on all objectives), and for some reason you were required to choose it. ";
		String q2 = "If you can choose just one objective to improve from its WORST value to its BEST, which would you choose to improve? ";
		String q3 = "Next, imagine that you are stuck with the worst possible alternative and allowed to improve any objective EXCEPT ";
		String q4 = " from its worst value to its best.  Which would it be?";
		String q5 = " is the last objective that you would choose to improve.  Click OK to complete the SMARTER weighting technique.";
		JButton btnSelect; 
		JButton btnCancel;
		
        JTextArea txtQ;
    	JTable tableWiz;	
    	DefaultTableModel tabModelWiz;  
    	Vector wizRows;
    	Vector wizCols;
		
		String sel;		// string of selected objective
		boolean is_first;	//to determine which string to display
		
		WeightingBySMARTER(){			
		}
		
		void startWeighting(){
		    txtQ = new JTextArea(q1 + q2);	
	        txtQ.setLineWrap(true);
	        txtQ.setWrapStyleWord(true);	
	        txtQ.setRows(5);
	        txtQ.setFont(new Font("Arial", Font.BOLD, 12));
	        
	        wizCols = new Vector();
	        wizCols.addAll(columns.subList(0, 3));
			wizRows = new Vector();
	        
			Vector entry;
			
			for (int i=0; i<objs.size(); i++){
				 Vector e = new Vector();
				 e = (Vector)rows.get(i);
				 entry = new Vector();
				 entry.addAll(e.subList(0, 3));
				 wizRows.add(entry);
			}			

			//set up table model and component
			tabModelWiz = new DefaultTableModel();
			tabModelWiz.setDataVector(wizRows, wizCols);		
			tableWiz = new JTable(tabModelWiz);        
	        tableWiz.setPreferredScrollableViewportSize(new Dimension(350, Math.min(wizRows.size() * 16, 150)));
	        tableWiz.setAutoCreateColumnsFromModel(true);
	        tableWiz.setRowSelectionAllowed(true);      
	        tableWiz.setRowSelectionInterval(0, 0);
	        tableWiz.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	        JScrollPane scrollPane = new JScrollPane(tableWiz);	        

	        btnSelect = new JButton("Select");
	        btnSelect.addActionListener(this);
	        btnSelect.setActionCommand("btnSelect");		
	        
	        btnCancel = new JButton("Cancel");
	        btnCancel.addActionListener(this);
	        btnCancel.setActionCommand("btnCancel");		 
	        
	        JPanel pnlButtons = new JPanel(); 
	        pnlButtons.setLayout(new BoxLayout(pnlButtons, BoxLayout.LINE_AXIS));
	        pnlButtons.add(Box.createHorizontalGlue());
	        pnlButtons.add(btnSelect);
		    pnlButtons.add(Box.createRigidArea(new Dimension(10, 0)));
	        pnlButtons.add(btnCancel);
		    
	        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
	        add(txtQ);
	        Dimension dimarea = new Dimension(0, 20);
	        add(Box.createRigidArea(dimarea));
	        add(scrollPane);
	        add(Box.createRigidArea(dimarea));
	        add(pnlButtons);
	        add(Box.createRigidArea(new Dimension(0, 10)));
	        
	        setBorder(BorderFactory.createEmptyBorder(20,30,0,30));	        
	        showFrame();	
	        is_first = true;
		}
		
	    public void showFrame() {    	
	        //Create and set up the window.
	        frame = new JDialog(con.frame, "SMARTER");
	        frame.setModal(true);
	        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	        frame.getContentPane().add(this, BorderLayout.CENTER);
	        frame.pack();
	        frame.setLocation(con.getX() + con.getWidth()/2 - getWidth()/2, 
	        		con.getY() + con.getHeight()/2 - getHeight()/2);
	        frame.setVisible(true);
	    }
	    
		public void actionPerformed(ActionEvent e) {
			if("btnSelect".equals(e.getActionCommand())){
	            is_first = false;	
	            int sel[] = tableWiz.getSelectedRows();
	            //if no selection
	            if (sel.length ==0)
	            	return;
	            for (int i = 0; i < sel.length; i++){
	            	String str = tableWiz.getValueAt(sel[i], 0).toString();
	            	addFromSMARTER((Vector)wizRows.get(sel[i]));	
	            	wizRows.removeElementAt(sel[i]);
	            	//adjust for removal from wizrows
	            	for (int j=i; j<sel.length; j++)
	            		sel[j]--;
	            	tableWiz.addNotify();
	            	if ((objs.size() - tableWiz.getRowCount())>1 && tableWiz.getRowCount() != 1){
	            		q3 = q3 + " and ";
	            	}
	            	if (tableWiz.getRowCount()==1){
	            		str = tableWiz.getValueAt(0, 0).toString();
	            		txtQ.setText(str.substring(0, 1).toUpperCase() + str.substring(1, str.length()) + q5);
	            		btnSelect.setText("OK");
	            	}			    
	            	else{ 
	            		q3 = q3 + str.substring(0, 1).toUpperCase() + str.substring(1, str.length());		            
	            		txtQ.setText(q3 + " " + q4);
	            	}
	            	repaint();	            
	            	if (tableWiz.getRowCount() == 0) { //none left, disable firing.
	            		frame.dispose();
	            		if (columns.size() < 5)
	            			columns.add(3, "Ranking");
	            		tabModel.fireTableStructureChanged();  
	            		rows.clear();
	            		rows.addAll(rowstemp);
	            		tabModel.fireTableStructureChanged();
	            		setWeights();	
	            		(DefineInitialWeights.this).repaint(); 
	            	}

	            }
	            tableWiz.clearSelection();
	            count++;
			}
			else if("btnCancel".equals(e.getActionCommand()))
				frame.dispose();
		}
		void setWeights(){
			int K=objs.size();
			int k=1;
			//double weight[]=new double[K];
			double x=0;
			Vector weights = new Vector();
			for (int i=0; i<K; i++){				
				//weight computation
				x=0; 
				for (int j=k; j<=K; j++)
					x = x + (1.0 / j);	
				double w = x/K;
				k++;
				weights.add(String.valueOf(df.format(w)));
			}

			for (int i=0; i<(K-1); i++){
				Vector v = (Vector)rows.get(i);
				boolean tied = false;
				Vector next = (Vector)rows.get(i+1);
				if (v.get(3).equals(next.get(3))){
					int tie = Integer.valueOf(String.valueOf(v.get(3))).intValue();//1
					double tot = Double.valueOf(String.valueOf(weights.get(i))).doubleValue();
					tied = true;
					int tiecount = 1;
					Vector nx = next;
					while(tied == true){	
						tied=false;						
						i++;
						tiecount++;
						tot = tot + Double.valueOf(String.valueOf(weights.get(i))).doubleValue();		
						if (i<(K-1)){
							nx = (Vector)rows.get(i+1);
							if (nx.get(3).equals(String.valueOf(tie))){
								tied=true;
							}
						}
					}
					double tieweight = tot/(double)tiecount;
					for (int j=0; j<K; j++){
						Vector vec = (Vector)rows.get(j);
						if(vec.get(3).equals(String.valueOf(tie)))
							weights.set(j, Double.valueOf(tieweight));
					}
				}
			}		
			for (int i=0; i<K; i++){					
				//update display
				Vector v = (Vector)rows.get(i);
				v.set(4, weights.get(i));
				tabModel.fireTableCellUpdated(i, 4);

				//update objective weight
				JObjective obj = (JObjective)obj_map.get(v.get(0));
				obj.setWeight(weights.get(i).toString());
			}
			con.btnOK.setEnabled(true);
		}
	}
	
	class TableHandler extends DefaultTableModel{
		private static final long serialVersionUID = 1L;

		public boolean isCellEditable(int row, int column){
			return false;
		}
	}
	
}