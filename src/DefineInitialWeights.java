import java.awt.Color;
import java.awt.Dimension;
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
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/**
 * Contained in ConstructionView. Has overview of all primitive criteria weights.
 * 
 *
 */
public class DefineInitialWeights extends JPanel implements ActionListener{
	private static final long serialVersionUID = 1L;

	static final int ROW_HEIGHT = 20;
	
	String new_sel;
	Vector<JObjective> objs;
	
	Vector<Vector<String>> rows;
	Vector<String> columns;
	JTable table;	
	TableHandler tabModel;  
	JScrollPane scrollPane;
	JPanel pnlTable;
	
	DecimalFormat df;
	ConstructionView con;
	
	JDialog SMARTERframe;
	boolean completedSMARTER = false;
		
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
		obj_map = new HashMap<String,JObjective>();
	}
	HashMap<String,JObjective> obj_map;
	void setObjectiveList(){
	    if (completedSMARTER) {
	        completedSMARTER = false;
	        return;
	    }
	    
		pnlTable.removeAll();
		objs = new Vector<JObjective>();
		
		objs = con.getObjPanel().getPrimitiveObjectives();
		
		//create hashmap for reference
		obj_map.clear();
		for (Iterator<JObjective> it = objs.iterator(); it.hasNext();){
				JObjective obj = it.next();
				obj_map.put(obj.getName(), obj);
		}		
		rows = new Vector<Vector<String>>();			
		columns = new Vector<String>();	
		
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
		Vector<String> entry;
		boolean allequal = true;
		Double equalWeight = new Double(1.0 / objs.size());
		for (int i=0; i<objs.size(); i++)
			if (!(objs.get(i).getWeight().equals("*"))){
				allequal = false;
				break;
			}
		for (int i=0; i<objs.size(); i++){			
			 entry = new Vector<String>();			 
			 obj=objs.get(i);
			 entry.add(obj.getName());
			 entry.add(null);
			 entry.add(null);
			 double wt[] = obj.getDomain().getWeights();			 
			 if (obj.getDomainType()==AttributeDomainType.DISCRETE){
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
				 		entry.set(1, (String.valueOf(obj.decimalFormat.format(kt[j]))) + " " + obj.getUnit());
				 	if (wt[j]==1.0)
				 		entry.set(2, (String.valueOf(obj.decimalFormat.format(kt[j]))) + " " + obj.getUnit());				 		
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
			 		entry.add(df.format(obj.getWeightNumeric()));
			 rows.add(entry);
		}			

	}
	
    public void addFromSMARTER(Vector<Vector<String>> rowstemp){
        if (columns.size() < 5)
            columns.add(3, "Ranking");
        tabModel.fireTableStructureChanged();  
        rows.clear();
        rows.addAll(rowstemp);
        tabModel.fireTableStructureChanged();
        setWeights();   
        repaint(); 
	}		    	
    
	public void actionPerformed(ActionEvent e) {
        if ("SMARTER...".equals(e.getActionCommand())) {
        	WeightingBySMARTER wbs = new WeightingBySMARTER(con, false);
        	wbs.startWeighting();
        	wbs.showFrame();
        }
        if ("Set equal weights".equals(e.getActionCommand())) {
        	double w = 1.0/(double)rows.size();
        	
			//update display
        	for (int i=0; i<rows.size(); i++){
			Vector<String> v = rows.get(i);
			v.set(3, String.valueOf(df.format(w)));
			tabModel.fireTableCellUpdated(i, 4);

			//update objective weight
			JObjective obj = obj_map.get(v.get(0));
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
	
    void setWeights(){
        int K=objs.size();
        int k=1;
        //double weight[]=new double[K];
        double x=0;
        Vector<Double> weights = new Vector<Double>();
        for (int i=0; i<K; i++){                
            //weight computation
            x=0; 
            for (int j=k; j<=K; j++)
                x = x + (1.0 / j);  
            double w = x/K;
            k++;
            weights.add(w);
        }

        for (int i=0; i<(K-1); i++){
            Vector<String> v = rows.get(i);
            boolean tied = false;
            Vector<String> next = rows.get(i+1);
            if (v.get(3).equals(next.get(3))){
                int tie = Integer.valueOf(String.valueOf(v.get(3))).intValue();//1
                double tot = weights.get(i);
                tied = true;
                int tiecount = 1;
                Vector<String> nx = next;
                while(tied == true){    
                    tied=false;                     
                    i++;
                    tiecount++;
                    tot = tot + weights.get(i);     
                    if (i<(K-1)){
                        nx = rows.get(i+1);
                        if (nx.get(3).equals(String.valueOf(tie))){
                            tied=true;
                        }
                    }
                }
                double tieweight = tot/(double)tiecount;
                for (int j=0; j<K; j++){
                    Vector<String> vec = rows.get(j);
                    if(vec.get(3).equals(String.valueOf(tie)))
                        weights.set(j, tieweight);
                }
            }
        }       
        for (int i=0; i<K; i++){                    
            //update display
            Vector<String> v = rows.get(i);
            v.set(4, df.format(weights.get(i)));
            tabModel.fireTableCellUpdated(i, 4);

            //update objective weight
            JObjective obj = obj_map.get(v.get(0));
            obj.setWeight(df.format(weights.get(i)));
        }
        con.btnOK.setEnabled(true);
    }
    
	
	class TableHandler extends DefaultTableModel{
		private static final long serialVersionUID = 1L;

		public boolean isCellEditable(int row, int column){
			return false;
		}
	}
	
}