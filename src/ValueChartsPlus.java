import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.StreamTokenizer;
import java.text.Collator;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;

public class ValueChartsPlus extends JPanel
                             implements ActionListener, PropertyChangeListener{
	private static final long serialVersionUID = 1L;
	static String strNew = "Create a new ValueChart";
    static String strData = "Open a data file";
    static String strVC = "Open an existing ValueChart";
    JRadioButton optNew;
    JRadioButton optData;
    JRadioButton optVC;
    ButtonGroup grpOptions;    
    JPanel pnlOptions;
    
    protected JLabel lblFileList;
    protected JTextField txtName; 
    protected static JFrame frame;
    
    JList lstFiles;    
    DefaultListModel listModel;
    JScrollPane scrList;     
    JButton btnOpenCreate;
    JButton btnCancel;    
    JPanel pnlButtons;
    JLabel lblName;
    JCheckBox chkAbs;
    
    ConstructionView con;
    static JComponent newContentPane;
    public static String datafilename;
    
	JPopupMenu popList; 
	JMenuItem menuRemove;
    
    Vector vc_files;
    Vector csv_files;
    
    String filename;
    static ValueChart chart;

    public ValueChartsPlus(){   
    	
        //Create the radio buttons (options)
        optNew = new JRadioButton(strNew);
        optNew.setActionCommand("optNew");
        optNew.setSelected(true);	//New File is selected: default

        optData = new JRadioButton(strData);
        optData.setActionCommand("optData");
        optVC = new JRadioButton(strVC);
        optVC.setActionCommand("optVC");
    
        grpOptions = new ButtonGroup();
        grpOptions.add(optNew);
        grpOptions.add(optData);
        grpOptions.add(optVC);        

        optNew.addActionListener(this);
        optData.addActionListener(this);
        optVC.addActionListener(this);

        pnlOptions = new JPanel(new GridLayout(0, 1));
        pnlOptions.setBorder(BorderFactory.createEtchedBorder(1));        
        pnlOptions.add(optNew);
        pnlOptions.add(optData);
        pnlOptions.add(optVC);
        
        //Set up the File List
        listModel = new DefaultListModel(); 
        lstFiles = new JList(listModel);
        lstFiles.addMouseListener(new MouseHandler());
        lstFiles.setEnabled(false);
        lstFiles.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lstFiles.setVisibleRowCount(10);       
    
        scrList = new JScrollPane(lstFiles);
        lstFiles.setPreferredSize(new Dimension(200,500));
      
        scrList.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        //Set up file name text field    
        lblName = new JLabel("ValueChart name:");       
        txtName = new JTextField(20);   
        txtName.addPropertyChangeListener(this);
        JPanel pnlFileName = new JPanel(new FlowLayout());
        pnlFileName.add(lblName);
        pnlFileName.add(txtName);       

        chkAbs = new JCheckBox("abs");
        chkAbs.setSelected(true);
        //Set up file Open/Create and Cancel command buttons 
        btnOpenCreate = new JButton("Create");
        btnOpenCreate.addActionListener(this);
        btnOpenCreate.setActionCommand("btnOpenCreate");
        btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(this);
        btnCancel.setActionCommand("btnCancel");
        
    	pnlButtons = new JPanel();
    	pnlButtons.setLayout(new BoxLayout(pnlButtons, BoxLayout.LINE_AXIS));
    	pnlButtons.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
    	pnlButtons.add(chkAbs);    	
    	pnlButtons.add(Box.createHorizontalGlue());
    	pnlButtons.add(btnOpenCreate);
    	pnlButtons.add(Box.createRigidArea(new Dimension(10, 0)));
    	pnlButtons.add(btnCancel);        
 
    	//Layout all panels
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        add(pnlOptions);
        add(Box.createRigidArea(new Dimension(0,5)));
        add(scrList);
        add(Box.createRigidArea(new Dimension(0,5)));
        add(pnlFileName);     
        add(Box.createRigidArea(new Dimension(0,5)));
        add(pnlButtons);

        setBorder(BorderFactory.createEmptyBorder(10,10,0,10));
        
		popList = new JPopupMenu();
		menuRemove = new JMenuItem("Remove");
	    menuRemove.addActionListener(this);
	    popList.add(menuRemove);
 
    }
    
    public void actionPerformed(ActionEvent e) {    	
    	if ("btnOpenCreate".equals(e.getActionCommand())) {
        	frame.setVisible(false);
        	if (chart != null){
        		chart.closeChart();
        		chart = null;
        	}
        	if (optData.isSelected()){
        		datafilename = lstFiles.getSelectedValue().toString();        	
        		con = new ConstructionView(ConstructionView.FROM_DATAFILE);
        		//con.readFile(lstFiles.getSelectedValue().toString());        	
            }
        	else if (optVC.isSelected()){ 
        		con = new ConstructionView(ConstructionView.FROM_VC);
        		con.filename = lstFiles.getSelectedValue().toString();
        		con.setInit(false);
        		filename = lstFiles.getSelectedValue().toString();
        		if (countEntries(filename) > 10)
        			con.setDisplayType(ConstructionView.SIDE_DISPLAY);
        		con.showChart(true);
        		con.filename = "test.vc";	//temp holder for a new filename
        	}
            else if (optNew.isSelected()){
            	con = new ConstructionView(ConstructionView.NEW_FILE);
            	//create new file with specified name
            }        	
    	}
        else if ("btnCancel".equals(e.getActionCommand())) 
        	System.exit(0);
        
    	//what happens when an option is selected
        else if ("optNew".equals(e.getActionCommand())){
        	listModel.clear();
        	lstFiles.setEnabled(false);
        	btnOpenCreate.setText("Create");
        	txtName.setEnabled(true);
            scrList.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            lblName.setForeground(Color.BLACK);            
        }
        else if ("optData".equals(e.getActionCommand())){
        	prepLists();
        	setList(csv_files);
        	txtName.setEnabled(true);      	
            scrList.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            lblName.setForeground(Color.BLACK);            
        }
        else if ("optVC".equals(e.getActionCommand())){
        	prepLists();
        	setList(vc_files);
        	txtName.setEnabled(false);      	
            scrList.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            lblName.setForeground(Color.GRAY);
        }
        else if ("Remove".equals(e.getActionCommand())){
        	if (lstFiles.getSelectedIndex() != -1){
	        	String delstring = lstFiles.getSelectedValue().toString();
	    		int ans = JOptionPane.showConfirmDialog(
	                    this, "Are you sure you want to delete " + delstring + "?",
	                    "Confirm Delete",
	                    JOptionPane.YES_NO_OPTION);	
	    		if (ans == JOptionPane.YES_OPTION){
		    		boolean del = new File(delstring).delete();
		        	if (del){
		        		prepLists();
		        		if (optData.isSelected())
		        			setList(csv_files);
		        		else if (optVC.isSelected())
		        			setList(vc_files);        			
		        		lstFiles.revalidate();
		        	}        	
	    		}
        	}
        	else
        		System.out.println("nothing selected");
        }
    }
    
    //List all the vc files
    void prepLists(){
    	vc_files = new Vector();
    	csv_files = new Vector();
        //String[] filenames;
        File f = new File(".");
        String files[] = f.list();
        for (int i=0; i<files.length; i++){
        	if (files[i].endsWith(".vc"))
        		vc_files.add(files[i]);
    		else if (files[i].endsWith(".csv"))
    			csv_files.add(files[i]);
        }    	
    }
    
    void setList(Vector file_list){
		lstFiles.setEnabled(true);  
		listModel.clear();
		//sort the list
		for (int i=0; i<file_list.size(); i++)
			listModel.addElement(file_list.elementAt(i).toString());
		int numItems = listModel.getSize();
		   String[] a = new String[numItems];
		   for (int i=0;i<numItems;i++)
		   {
		     a[i] = (String)listModel.getElementAt(i);
		   }
		   sortArray(a);
		   lstFiles.setListData(a);
		   lstFiles.revalidate();
		
		lstFiles.setSelectedIndex(0);
		btnOpenCreate.setText("  Open ");
    }
  
    public Vector getFileNames(String name, Vector list){
    	try{
    		FileReader fr = new FileReader(name);
    		BufferedReader br = new BufferedReader(fr);
    		StreamTokenizer st = new StreamTokenizer(br);
    		st.whitespaceChars(',', ',');
    		while(st.nextToken() != StreamTokenizer.TT_EOF) {
    			if(st.ttype==StreamTokenizer.TT_WORD)
    		    	list.add(st.sval);
    		}
    		fr.close();    		
    	}catch(Exception e) {
    		System.out.println("Exception: " + e);
    	}
    	return list;
    }     
    
    public int countEntries(String name){
    	int count=0;
    	try{
    		FileReader fr = new FileReader(name);
    		BufferedReader br = new BufferedReader(fr);
    		StreamTokenizer st = new StreamTokenizer(br);
    		st.whitespaceChars(',', ',');
    		while(st.nextToken() != StreamTokenizer.TT_EOF) {
    			if(st.ttype==StreamTokenizer.TT_WORD)
    				if(st.sval.equals("entry")){
    					count++;
    				}
    		}
    		fr.close();    		
    	}catch(Exception e) {
    		System.out.println("Exception: " + e);
    	}
    	return count;    	
    }
    
    public static void showStartView() {
        //Create and set up the window.
        frame = new JFrame("ValueCharts Plus");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        newContentPane = new ValueChartsPlus();
        frame.setContentPane(newContentPane);      
        frame.pack();
        frame.setVisible(true);
        frame.setResizable(false);
    }
    
    //sorts an array for the listview
    void sortArray(String[] strArray)
    {
      if (strArray.length == 1)    // no need to sort one item
        return;
      Collator collator = Collator.getInstance();
      String strTemp;
      for (int i=0;i<strArray.length;i++)
      {
        for (int j=i+1;j<strArray.length;j++)
        {
          if (collator.compare(strArray[i], strArray[j]) > 0)
          {
            strTemp = strArray[i];
            strArray[i] = strArray[j];
            strArray[j] = strTemp;
          }
        }
      }
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                showStartView();
            }
        });
    }

	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent pce) {
		if (!txtName.getText().endsWith(".vc")){
			txtName.setText(txtName.getText() + ".vc");
			repaint();
		}		
	}
	
	private class MouseHandler extends MouseInputAdapter{

		public void mousePressed(MouseEvent me){
            if(SwingUtilities.isRightMouseButton(me)){
				popList.show(me.getComponent(), me.getX()+5, me.getY()+5);
            }
		}
	}
}


