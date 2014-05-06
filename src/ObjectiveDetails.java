import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


public class ObjectiveDetails extends JPanel implements ActionListener{
	private static final long serialVersionUID = 1L;
	JRadioButton optCont;
    JRadioButton optDisc;
    ButtonGroup grpOptions;    
    JPanel pnlDomain;
    
    protected JTextField txtName;
    protected JTextField txtUnit;
    
    protected static JDialog frame;

    JButton btnOK;
    JButton btnCancel;    
    JPanel pnlButtons;
    JPanel pnlOptions;
    JObjective objective;
    JLabel lblUnit;

    static JComponent newContentPane;
    ConstructionView con;

    ObjectiveDetails(ConstructionView c){ 
    	con=c;
    	
        //Create the radio buttons (options)
        optCont = new JRadioButton("Continuous");
        optCont.setActionCommand("optCont");
        optDisc = new JRadioButton("Discrete");
        optDisc.setActionCommand("optDisc");        
     
        grpOptions = new ButtonGroup();
        grpOptions.add(optCont);
        grpOptions.add(optDisc);

        optCont.addActionListener(this);
        optDisc.addActionListener(this);

        pnlOptions = new JPanel(new GridLayout(0, 1));
        pnlOptions.setBorder(BorderFactory.createEtchedBorder(1));       
        pnlOptions.add(optCont);
        pnlOptions.add(optDisc);
        
        //Set up the objective name text field    
        JLabel lblName = new JLabel("Objective Name:");       
        txtName = new JTextField(20); 
        JPanel pnlName = new JPanel(new FlowLayout());
        pnlName.add(lblName);
        pnlName.add(txtName);     

        //Set up the unit text field
        lblUnit = new JLabel("unit");
        txtUnit = new JTextField(20);        
        JPanel pnlUnit = new JPanel(new FlowLayout());
        pnlUnit.add(lblUnit);
        pnlUnit.add(txtUnit);

        //Set up file Open/Create and Cancel command buttons 
        btnOK = new JButton("OK");
        btnOK.addActionListener(this);
        btnOK.setActionCommand("btnOK");
        btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(this);
        btnCancel.setActionCommand("btnCancel");
        
    	pnlButtons = new JPanel();
    	pnlButtons.setLayout(new BoxLayout(pnlButtons, BoxLayout.LINE_AXIS));
    	pnlButtons.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
    	pnlButtons.add(Box.createHorizontalGlue());
    	pnlButtons.add(btnOK);
    	pnlButtons.add(Box.createRigidArea(new Dimension(10, 0)));
    	pnlButtons.add(btnCancel);        
 
    	//Layout all panels
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        add(pnlName);
        add(Box.createRigidArea(new Dimension(0,5)));
        add(pnlOptions);
        add(Box.createRigidArea(new Dimension(0,5)));
        add(pnlUnit);
        add(Box.createRigidArea(new Dimension(0,5)));
        add(pnlButtons);
        setBorder(BorderFactory.createEmptyBorder(10,10,0,10));
        
        showDetailsView();
    }
    public void updateFields(JObjective obj){
    	objective = obj;
        if (objective.getType()==2){
        	optCont.setSelected(true);
        	txtUnit.setEnabled(true);
        	lblUnit.setForeground(Color.BLACK);
        }        	
        else{
        	optDisc.setSelected(true); 
        	txtUnit.setEnabled(true);
        	lblUnit.setForeground(Color.LIGHT_GRAY);
        }
        txtName.setText(objective.getName());
        txtUnit.setText(objective.getUnit());        
    }
    
    public void actionPerformed(ActionEvent e) {
    	if ("btnOK".equals(e.getActionCommand())) {
    		int type;
    		if (optCont.isSelected())
    			type = 2;
    		else
    			type = 1;    		
    		String name = objective.getName();
    		objective.updateDetails(type, txtName.getText(), txtUnit.getText());
    		con.getAltPanel().updateObjDetails(objective, name);    		
            con.getAltPanel().updateTable();
        	frame.setVisible(false);
            } 
        else if ("btnCancel".equals(e.getActionCommand())) 
        	frame.setVisible(false);
        else if ("optCont".equals(e.getActionCommand())){ 
        	txtUnit.setEnabled(true);
        	lblUnit.setForeground(Color.BLACK);  	
        }
        else if ("optDisc".equals(e.getActionCommand())){
        	txtUnit.setEnabled(false);
        	lblUnit.setForeground(Color.LIGHT_GRAY);        
        }
        else JOptionPane.showMessageDialog(frame, "Error!");
    }
   
    public void showDetailsView() {
        //Create and set up the modal dialog window.
        frame = new JDialog(con.frame, "Details");
        frame.setModal(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(this, BorderLayout.CENTER);      
        frame.pack();
        frame.setResizable(false);
        //place at centre of view
        frame.setLocation(con.getX() + con.getWidth()/2 - getWidth()/2, 
        		con.getY() + con.getHeight()/2 - getHeight()/2);		
    }
    
    public void showFrame(boolean bool){
    	frame.setVisible(bool);    	
    }

}


