import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

class WeightingBySMARTER extends JPanel implements ActionListener{      
    private static final long serialVersionUID = 1L;

    JDialog frame;
    
    String q1a = "Imagine the worst possible ";
    String q1b = " (i.e. scoring worst on all criteria) ";
    String q1c = "and you were given the opportunity to improve one criteria from its WORST value to its BEST. Which would you choose? ";
    String q2 = "Next, imagine again the worst possible alternative and you are allowed to improve one of the following criteria from its WORST value to its BEST. Which would you choose?";
    String q3 = " is the last criteria that you would choose to improve.  Click Finish to complete the SMARTER weighting technique.";
    JButton btnSelect; 
    JButton btnCancel;
    
    JTextArea txtQ;
    JTable tableWiz;    
    DefaultTableModel tabModelWiz;  
    Vector<Vector<String>> wizRows;
    Vector<String> wizCols;
    private Vector<Vector<String>> rowstemp;

    String sel;     // string of selected objective
    boolean is_first;   //to determine which string to display
    ConstructionView con;
    private boolean fromConstruction = true;
    private int count = 0;
    private String problem = "";

    WeightingBySMARTER(ConstructionView c, boolean fromCon){
        con = c;
        fromConstruction = fromCon;
        rowstemp = new Vector<Vector<String>>();
    }
    
    void startWeighting(String problem){        
        removeAll();
        rowstemp.clear();
        this.problem = problem;
        count = 1;  //reset counter;
        txtQ = new JTextArea(q1a + (problem == null || problem.isEmpty() ? "alternative" : problem) + q1b + q1c);   
        txtQ.setLineWrap(true);
        txtQ.setWrapStyleWord(true);    
        txtQ.setRows(5);
        txtQ.setFont(new Font("Arial", Font.BOLD, 12));
        
        wizCols = new Vector<String>();
        wizCols.addAll(con.getWeightPanel().columns.subList(0, 3));
        wizRows = new Vector<Vector<String>>();
        
        Vector<String> entry;
        
        for (int i=0; i<con.getWeightPanel().objs.size(); i++){
             Vector<String> e;
             e = con.getWeightPanel().rows.get(i);
             entry = new Vector<String>();
             entry.addAll(e.subList(0, 3));
             wizRows.add(entry);
        }           

        //set up table model and component
        tabModelWiz = new DefaultTableModel() {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
               //all cells false
               return false;
            }
        };
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
        
        btnCancel = new JButton((fromConstruction ? "Reset": "Cancel"));
        btnCancel.addActionListener(this);
        btnCancel.setActionCommand("btnCancelSMARTER");  
        if (fromConstruction) btnCancel.setEnabled(false);
        
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
        is_first = true;
    }
    
    public void showFrame() {       
        //Create and set up the window.
        frame = new JDialog(con.getWeightPanel().con.frame, "SMARTER");
        frame.setModal(true);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().add(this, BorderLayout.CENTER);
        frame.pack();
        frame.setLocation(con.getWeightPanel().con.getX() + con.getWeightPanel().con.getWidth()/2 - getWidth()/2, 
                con.getWeightPanel().con.getY() + con.getWeightPanel().con.getHeight()/2 - getHeight()/2);
        frame.setVisible(true);
        rowstemp.clear();
    }
    
    public void addSelection(Vector<String> data){
        Vector<String> entry = data;
        entry.add(String.valueOf(count));
        entry.add("");
        rowstemp.add(entry);
    }       
    
    public void actionPerformed(ActionEvent e) {
        if("btnSelect".equals(e.getActionCommand())){
            is_first = false;   
            int sel[] = tableWiz.getSelectedRows();
            //if no selection
            if (sel.length ==0)
                return;
            else
                btnCancel.setEnabled(true);
            
            for (int i = 0; i < sel.length; i++){
                String str = tableWiz.getValueAt(sel[i], 0).toString();
                addSelection(wizRows.get(sel[i]));    
                wizRows.removeElementAt(sel[i]);
                //adjust for removal from wizrows
                for (int j=i; j<sel.length; j++)
                    sel[j]--;
                tableWiz.addNotify();
                if (tableWiz.getRowCount()==1){
                    str = tableWiz.getValueAt(0, 0).toString();
                    txtQ.setText(str.substring(0, 1).toUpperCase() + str.substring(1, str.length()) + q3);
                    btnSelect.setText("Finish");
                }               
                else{ 
                    txtQ.setText(q2);
                }
                repaint();              
                if (tableWiz.getRowCount() == 0) { //none left, disable firing.

                    con.getWeightPanel().addFromSMARTER(rowstemp);
                    if (frame != null) {
                        frame.dispose();
                    } 
                    if (fromConstruction) { // from ConstructionView tab
                        con.getWeightPanel().completedSMARTER = true;
                        count = 0; 
                        con.constPane.setSelectedIndex(4);
                    }
                }

            }
            tableWiz.clearSelection();
            if (count > 0) count++;
        }
        else if("btnCancelSMARTER".equals(e.getActionCommand())) {
            if (frame != null) frame.dispose();
            if (fromConstruction) {
                count = 0;
                startWeighting(problem);
            }
        }
    }
}
