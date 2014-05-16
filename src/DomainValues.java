import java.awt.Color;
import java.awt.Dimension;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

	class DomainValues extends JPanel{		
		private static final long serialVersionUID = 1L;
		JLabel lblAlt;
    	JTable table;	
    	DefaultTableModel tabModel;  
    	Vector<Vector<String>> rows;
    	Vector<String> cols;
		Vector<String> data;
		String sel;		// string of selected objective
		ValueChart chart;
		
		DomainValues(ValueChart chart){			
			this.chart = chart;
	        cols = new Vector<String>();
	        cols.add(" ");
	        cols.add(" ");
			rows = new Vector<Vector<String>>();
			data = new Vector<String>();

			lblAlt= new JLabel(" ");
			//lblAlt.setBorder(BorderFactory.createLineBorder(Color.red));
			//set up table model and component			
			
			tabModel = new DefaultTableModel();
			tabModel.setDataVector(rows, cols);		
			table = new JTable(tabModel);        
	        table.setPreferredScrollableViewportSize(new Dimension(190, 200));

	        table.setAutoCreateColumnsFromModel(true);
	        table.setRowSelectionAllowed(false);      
	        table.setGridColor(Color.WHITE);
	        (table.getTableHeader()).setVisible(false);
	        JScrollPane scrollPane = new JScrollPane(table);	        
   
	        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
	        lblAlt.setPreferredSize(new Dimension(getWidth(), lblAlt.getHeight()));
	        add(lblAlt);
	        //Dimension dimarea = new Dimension(0, 20);
	        //add(Box.createRigidArea(dimarea));
	        add(scrollPane);
	        scrollPane.setPreferredSize(new Dimension(250, 250));
	        scrollPane.setMaximumSize(new Dimension(250, 250));

		}
		
		public void showData(ChartEntry entry){

			rows = new Vector<Vector<String>>();
			Vector<BaseTableContainer> prims = chart.getPrims();
			//lblAlt.setText(entry.name);
			//lblAlt.setLocation(0, lblAlt.getLocation().y);
			//lblAlt.setPreferredSize(new Dimension(getWidth(), lblAlt.getHeight()));
			//lblAlt.setMaximumSize(new Dimension(getWidth(), lblAlt.getHeight()));
			
	        setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(0,0,0,0), entry.name, 0, 0));
			for (Iterator<BaseTableContainer> it = prims.iterator(); it.hasNext();){
				data = new Vector<String>();
				BaseTableContainer base = it.next();
				String s = (base.getName());				
				AttributeValue val = (AttributeValue)entry.map.get(s);				
				data.add(s);
				String u = ((AttributeCell)base.table).getUnits();
				data.add(val.stringValue() + (u==null || u.equals("") ? " " : " " + u));
				rows.add(data);
			}
			tabModel.setDataVector(rows,cols);	
			tabModel.fireTableStructureChanged();
			repaint();
			
		}
		
		public Dimension getPreferredSize(){
			 	return (new Dimension(200, 200));
		}
}