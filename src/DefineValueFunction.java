import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;

public class DefineValueFunction extends JPanel implements ActionListener{

	private static final long serialVersionUID = 1L;
	protected static JFrame frame;
    static JComponent newContentPane;

    Vector objs;
    JPanel pnlObjList;
    JPanel ugraph;
    JPanel pnlGraph;
	MouseHandler mouseListener;    
	JObjective obj_sel;
    JLabel lbl_sel;
	
    AttributeDomain domain;
    ValueChart chart;
    String attributeName;
    ContinuousUtilityGraph graph;
    
    JObjective obj;
  
    JPopupMenu popValueFunction;   
    JMenuItem menuRemove;
  
    boolean all_set;
    ConstructionView con;
    
    public DefineValueFunction(ConstructionView c){
    	con = c;
    	mouseListener = new MouseHandler();
    	//create top panel: objective list
		pnlObjList = new JPanel();		
		pnlObjList.setLayout(new BoxLayout(pnlObjList, BoxLayout.Y_AXIS));
		//scrollPane = new JScrollPane(pnlObjList);
        //create the graph panel
		ugraph = new JPanel();
		pnlGraph = new JPanel();
		
		popValueFunction = new JPopupMenu();
		
	    JMenuItem menuItem = new JMenuItem("Default Flat");
	    menuItem.addActionListener(this);
	    popValueFunction.add(menuItem);
	    menuItem = new JMenuItem("Positive Linear");
	    menuItem.addActionListener(this);
	    popValueFunction.add(menuItem);
	    menuItem = new JMenuItem("Negative Linear");
	    menuItem.addActionListener(this);
	    popValueFunction.add(menuItem);
	    menuItem = new JMenuItem("Other");
	    menuItem.addActionListener(this);
	    popValueFunction.add(menuItem);		
	    popValueFunction.addSeparator();
	    menuItem = new JMenuItem("Add Point");
	    menuItem.addActionListener(this);
	    popValueFunction.add(menuItem);	
	    menuRemove = new JMenuItem("Remove Point");
	    menuRemove.addActionListener(this);
	    popValueFunction.add(menuRemove);	
	    menuItem = new JMenuItem("Build Custom...");
	    menuItem.addActionListener(this);
	    //popValueFunction.add(menuItem);	
    		
        // add all information to view
		setBorder(BorderFactory.createEmptyBorder(15, 10, 10, 10));
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(pnlObjList);
        add(pnlGraph);	
        
        addComponentListener(new ResizeHandler());
    }
     
    void repaintDisplay(){    	
		pnlObjList.removeAll();		
		pnlObjList.setPreferredSize(new Dimension(75, con.getHeight()-50));
		pnlObjList.setMaximumSize(new Dimension(75, con.getHeight()-50));
   
		//pnlObjList.setSize(con.constPane.getWidth(), 30);
		// get primitive objective list built in obj tab
		objs = new Vector();
		objs = con.getObjPanel().getPrimitiveObjectives();
		
		for (int i=0; i<objs.size(); i++){
			JObjective lblObj = new JObjective(objs.get(i).toString());
			lblObj.setPreferredSize(new Dimension(75, getHeight()/objs.size()));			
			lblObj.setMaximumSize(new Dimension(75, getHeight()/objs.size()));
			lblObj.setAlignmentY(TOP_ALIGNMENT); 
			lblObj.setHorizontalAlignment(JLabel.CENTER);
			lblObj.addMouseListener(mouseListener);					
			pnlObjList.add(lblObj);
			con.repaint();	
		}		
		
		//set initial status of objectives (0,1)
		for (int i=0; i<objs.size(); i++)
			checkUtility((JObjective)objs.get(i), (JLabel)pnlObjList.getComponent(i));
			obj_sel = (JObjective)objs.get(0);
			lbl_sel = (JLabel)pnlObjList.getComponent(0);
		
	    // set Value Function graph for first objective		
		showGraph();   	 
	}	
  	
    void showGraph(){
		pnlGraph.removeAll();
		ugraph = new JPanel();
		ugraph = obj_sel.getUtilityGraph(this);		
		if (obj_sel.getType()==JObjective.CONTINUOUS){
			((ContinuousUtilityGraph)(ugraph.getComponent(0))).addMouseListener(mouseListener);
			((ContinuousUtilityGraph)(ugraph.getComponent(0))).addMouseMotionListener(mouseListener);
		}
		pnlGraph.add(ugraph);
		lbl_sel.setBackground(new Color(100, 100, 100));
		con.repaint();
		
	}
	
    public void checkUtility(JObjective obj, JLabel lbl){
		boolean has0 = false;
		boolean has1 = false;
		double wt[] = obj.getDomain().getWeights();
		for (int j=0; j<wt.length; j++){
			if (wt[j]==0.0) has0 = true;
			if (wt[j]==1.0) has1 = true;				
		}
		if (!(has0 && has1)){	//if not properly set
			lbl.setForeground(Color.red);
			all_set=false;
		}
		else 
			lbl.setForeground(Color.black);
	
	}
	
    public boolean checkAllUtility(){
		all_set=true;
		if (objs == null){
			repaintDisplay();
		}
		for (int i=0; i<objs.size(); i++)
			checkUtility((JObjective)objs.get(i), (JLabel)pnlObjList.getComponent(i));		
			if (!all_set){
				JOptionPane.showMessageDialog(this,
						"All value functions must have a 0 and a 1.0 value set",
						"Missing information",
						JOptionPane.WARNING_MESSAGE);
			}	
		return all_set;
	}
 	
    public void actionPerformed(ActionEvent ae) {  	
		if ("Default Flat".equals(ae.getActionCommand())){
			obj_sel.setContinuous();
		}
		else if ("Positive Linear".equals(ae.getActionCommand())){
			obj_sel.setContinuous(true);
		}
		else if ("Negative Linear".equals(ae.getActionCommand())){
			obj_sel.setContinuous(false);
		}
		else if ("Other".equals(ae.getActionCommand())){
			inputBestValue();	
		}
		else if ("Add Point".equals(ae.getActionCommand())){
			addPoint();	
		}
		
		else if ("Remove Point".equals(ae.getActionCommand())){
	    	ContinuousAttributeDomain dom = (ContinuousAttributeDomain) obj_sel.domain;
	    	dom.removeKnot(dom.getKnots()[rem_i]);
	    	ugraph.repaint();


		}		

		showGraph();
		checkUtility(obj_sel, lbl_sel);
		con.repaint();
	}

    private class ResizeHandler extends ComponentAdapter{
        public void componentResized(ComponentEvent e){
        	setSize(con.constPane.getSize());
    		pnlObjList.setMaximumSize(new Dimension(75, con.getHeight()-50));
    		pnlObjList.setPreferredSize(new Dimension(75, con.getHeight()-50));
    		for (int i=0; i < pnlObjList.getComponentCount(); i++){
    			JObjective lblObj = (JObjective)pnlObjList.getComponent(i);
    			lblObj.setPreferredSize(new Dimension((getWidth())/objs.size(), 30));			
    			lblObj.setMaximumSize(new Dimension((getWidth())/objs.size(), 30));
    		}		
        	//System.out.println("resize vf " + getWidth());
        }
    }
    
    
    private void inputBestValue(){
		double ansd = 0;
        String ans = null;
    	ans = (String)JOptionPane.showInputDialog(this, "Best Value", "Other", JOptionPane.PLAIN_MESSAGE, 
                null, null, String.valueOf(obj_sel.minC + (obj_sel.maxC - obj_sel.minC)/2));
        		//suggest the median value	 
    	try{    		
	        if ((ans != null) && (ans.length() > 0)) {
	        	ansd = (Double.valueOf(ans)).doubleValue();
	        	if (ansd <= obj_sel.maxC && ansd >= obj_sel.minC)	        		
	        		obj_sel.setContinuous(ansd);
	        	else{
	        		JOptionPane.showMessageDialog(this, "Out of range", "Error", JOptionPane.WARNING_MESSAGE);      
	            	inputBestValue();
	        	}	        		
	        }
    	}catch(NumberFormatException e){                    
    		JOptionPane.showMessageDialog(this, "Invalid Entry", "Error", JOptionPane.WARNING_MESSAGE);      
    		inputBestValue();
        }  
    }
    
    private void addPoint(){
    	double ansd = 0;
		String ans = null;
		ans =  (String)JOptionPane.showInputDialog(this, "New point", "Add New Point", JOptionPane.PLAIN_MESSAGE, null, null, "");		
		if ((ans != null) && (ans.length() > 0)) {
        	ansd = (Double.valueOf(ans)).doubleValue();
        	try{
        		if (ansd <= obj_sel.maxC && ansd >= obj_sel.minC){	 
        			ContinuousAttributeDomain dom = (ContinuousAttributeDomain) obj_sel.domain;
        			dom.addKnot(ansd, 0.0);
        		}
	        	else{
	        		JOptionPane.showMessageDialog(this, "Out of range", "Error", JOptionPane.WARNING_MESSAGE);      
	            	addPoint();
	        	}	
        	}catch(NumberFormatException e){                    
        		JOptionPane.showMessageDialog(this, "Invalid Entry", "Error", JOptionPane.WARNING_MESSAGE);      
        		addPoint();
        	}
		}
    }
    

    int rem_i = -1;
    private class MouseHandler extends MouseInputAdapter{

		public void mousePressed(MouseEvent me){
			        	//If an Objective label is clicked, show the graph
            if(SwingUtilities.isLeftMouseButton(me)){          
            	if("JObjective".equals(me.getComponent().getClass().getName())){
                	lbl_sel.setBackground(Color.LIGHT_GRAY); 	//unmark the last seection
                	lbl_sel = (JLabel)me.getComponent();
                	
                	//set selected objective
            		String str = me.getComponent().toString();     
            		for (int i=0; i<objs.size(); i++){
            			if (str.equals(objs.get(i).toString()))
            				obj_sel = (JObjective)objs.get(i);
            		}            			
            		showGraph();
            		//repaintDisplay();          
            	}
            }
            
            //if the value graph is right-clicked, display the popup menu
            else if(SwingUtilities.isRightMouseButton(me)){
            	if("ContinuousUtilityGraph".equals(me.getComponent().getClass().getName())){
        			int i;
    				ContinuousAttributeDomain cad = (ContinuousAttributeDomain)obj_sel.domain;
    				double kts[] = cad.getKnots();			
    				for (i=0; i < kts.length; i++){
    				int ptx = (int)((kts[i]-kts[0])/((kts[(kts.length)-1])-kts[0])*200)+50;
    				if (me.getX() > (ptx - 5) && me.getX() < (ptx + 5)){
    					rem_i = i;
    					System.out.println("rem_i " + rem_i);
    					break;
    				}
    				else{
    					System.out.println("not");
    					rem_i = -1;
    				}
    					
    			}
            			if (rem_i != -1)
            				menuRemove.setEnabled(true);
            			else
            				menuRemove.setEnabled(false);
            			popValueFunction.show(me.getComponent(), me.getX()+5, me.getY()+5);
            	}
            }
   		}

		public void mouseMoved(MouseEvent me){


		}

    }
   
    boolean point_click = false;
    
    void setPointClick(boolean b){
    	point_click = b;
    }
    
}


