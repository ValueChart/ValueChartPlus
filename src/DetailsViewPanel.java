import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

	class DetailsViewPanel extends JPanel{		
		private static final long serialVersionUID = 1L;
		JLabel lblAlt;
    	JTable table;	
    	DefaultTableModel tabModel;  
    	Vector<Vector<String>> rows;
    	Vector<String> colsTwo;
    	Vector<String> colsOne;
		Vector<String> data;
		String sel;		// string of selected objective
		ValueChart chart;
		
		int width = 400;
		int height = 200;
		Font entryFont = new Font("Arial", Font.PLAIN, 12);
		Font altFont = new Font("Verdana", Font.PLAIN, 13);
		boolean isAlternative = true;
		
		DetailsViewPanel(ValueChart chart){			
			this.chart = chart;
	        colsTwo = new Vector<String>();
	        colsTwo.add(" ");
	        colsTwo.add(" ");
	        colsOne = new Vector<String>();
	        colsOne.add(" ");
			rows = new Vector<Vector<String>>();
			data = new Vector<String>();

			lblAlt= new JLabel(" ");
			//lblAlt.setBorder(BorderFactory.createLineBorder(Color.red));
			//set up table model and component			
			
			tabModel = new DefaultTableModel() {
	            private static final long serialVersionUID = 1L;

	            @Override
	            public boolean isCellEditable(int row, int column) {
	               //all cells false
	               return false;
	            }
	        };
			tabModel.setDataVector(rows, colsOne);		
			table = new JTable(tabModel);        
	        table.setPreferredScrollableViewportSize(new Dimension(width-10, height));
	        
	        table.setAutoCreateColumnsFromModel(true);
	        table.setRowSelectionAllowed(true);      
	        table.setGridColor(Color.WHITE);
	        (table.getTableHeader()).setVisible(false);
	        JScrollPane scrollPane = new JScrollPane(table);	        
   
	        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
	        lblAlt.setPreferredSize(new Dimension(getWidth(), lblAlt.getHeight()));
	        add(lblAlt);

	        add(scrollPane);
	        scrollPane.setPreferredSize(new Dimension(width+50, height+50));
	        scrollPane.setMaximumSize(new Dimension(width+50, height+50));
	        
	        showAlternativeLegend();
		}
		
		// display single alternative information
		public void showData(ChartEntry entry){
		    isAlternative = false;
		    table.setFont(entryFont);
		    
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
				AttributeValue val = entry.attributeValue(s);				
				data.add(s);
				String u = ((AttributeCell)base.table).getUnits();
				data.add(val.stringValue() + (u==null || u.equals("") ? " " : " " + u));
				rows.add(data);
			}
			tabModel.setDataVector(rows,colsTwo);	
			tabModel.fireTableStructureChanged();
			repaint();
			
		}
		
		public void showAlternativeLegend() {
		    isAlternative = true;
		    table.setFont(altFont);
		    
		    rows = new Vector<Vector<String>>();
		    Vector<ChartEntry> entryList = chart.getEntryList();
		    setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(0,0,0,0), "Alternatives", 0, 0));
		    if (entryList == null) return;
		    for (int i = 0; i < entryList.size(); i++) {
		        data = new Vector<String>();
		        data.add("   (" + (i+1) + ") -- " + entryList.get(i).name);
		        rows.add(data);
		    }
            tabModel.setDataVector(rows, colsOne);
            tabModel.fireTableStructureChanged();
            repaint();
		}
		
		public void highLightAlternative(int idx) {
		    if (!isAlternative || idx < 0) return;
		    table.clearSelection();
		    table.getSelectionModel().setSelectionInterval(idx, idx);
		}
		
		public Dimension getPreferredSize(){
			 	return (new Dimension(width, height));
		}
}