import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.event.MouseInputAdapter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class DefineObjectivesPanel extends JPanel implements ActionListener{
	private static final long serialVersionUID = 1L;
	static final int 	OBJ_X = 15, 
						OBJ_Y = 15,
						OBJ_WIDTH = 75,
						OBJ_HEIGHT = 30;
	static final int 	NEW_FILE = 1,
						FROM_DATAFILE = 2;	
	
	int type;
	int counter;
	int colorcount = 0;	
	Vector listed_objs;
	Vector prim_obj;	
	String output = "";	
	boolean del_mode = false;
	boolean dnd_mode = true;
	
    DefaultMutableTreeNode sel_node;
    DefaultMutableTreeNode root_node;	
    DefaultMutableTreeNode found_node;	    
    DefaultTreeModel tree_model; 
	
	JPopupMenu popObjective;
	JPopupMenu popList; 
	JMenuItem menuRemove;
	JMenuItem menuDetails;
	JMenuItem menuDelete;
	JMenu menuAdd;
	JMenu menuRoot;
	JPanel pnlList;	
	
	JObjective lblSel;	
	JObjective lblRoot;
	ConstructionView pnlCon;
	
	MouseHandler mhandler;	
		
//INITIAL METHODS
	
	public DefineObjectivesPanel(int x, ConstructionView c){
		//initial setup
		type = x;
		pnlCon = c;
		
		mhandler = new MouseHandler();				
		addComponentListener(new ResizeHandler());		
	   	setLayout(null);	
		setOpaque(true);

		//create main objective: top of hierarchy
		lblRoot = new JObjective("Problem");
		lblRoot.setWeight("1.0");
		lblRoot.setTransferHandler(new ObjectiveTransferHandler());
		lblRoot.addMouseListener(mhandler);
		
		//set up tree model
		root_node = new DefaultMutableTreeNode (lblRoot);
		tree_model = new DefaultTreeModel(root_node);
		
		//set up panel for objective list
		pnlList = new JPanel();			
		pnlList.setLayout(new BoxLayout(pnlList, BoxLayout.PAGE_AXIS));
		pnlList.setTransferHandler(new ObjectiveTransferHandler());
		pnlList.setBorder(BorderFactory.createEtchedBorder(1));  
		pnlList.addMouseListener(mhandler);
		
		//create the List popup menu
		popList = new JPopupMenu();
		menuAdd = new JMenu("Add to");
	    popList.add(menuAdd);
	    menuRoot = new JMenu(lblRoot.getName());
	    menuAdd.add(menuRoot);
	    createAddMenu(root_node, menuRoot);
	    
		menuDelete = new JMenuItem("Delete");
	    menuDelete.addActionListener(this);
	    popList.add(menuDelete);
	    popList.addSeparator();
		JMenuItem menuItem = new JMenuItem("Rename");
	    menuItem.addActionListener(this);
	    popList.add(menuItem);
		menuItem = new JMenuItem("Details");
	    menuItem.addActionListener(this);
	    popList.add(menuItem);
		
		//create the Tree popup menu
		popObjective = new JPopupMenu();		
	    menuItem = new JMenuItem("Add");
	    menuItem.addActionListener(this);
	    popObjective.add(menuItem);
	    menuRemove = new JMenuItem("Remove");
	    menuRemove.addActionListener(this);
	    popObjective.add(menuRemove);
	    menuItem = new JMenuItem("Delete");
	    menuItem.addActionListener(this);
	    popObjective.add(menuItem);
	    popObjective.addSeparator();
	    menuItem = new JMenuItem("Rename");
	    menuItem.addActionListener(this);
	    popObjective.add(menuItem);
	    menuDetails = new JMenuItem("Details");
	    menuDetails.addActionListener(this);
	    popObjective.add(menuDetails);
	    menuItem = new JMenuItem("test");
	    menuItem.addActionListener(this);
	    popObjective.add(menuItem);

	    //hidden objective: the key for alternative list
	    //this must is added to the listed_objectives vector for later alternative use
		if (type!=FROM_DATAFILE){
			listed_objs = new Vector();
			JObjective name_obj = new JObjective("name");
			name_obj.setType(JObjective.DISCRETE);
			listed_objs.add(name_obj);
		}		
	}		
	
	public void createAddMenu(DefaultMutableTreeNode node, JMenu menu){
		for(int i = 0; i < node.getChildCount(); i++){
			DefaultMutableTreeNode child = (DefaultMutableTreeNode)node.getChildAt(i);
			JObjective obj = (JObjective)child.getUserObject();
			if (child.getChildCount()==0)
				menu.add(new JMenuItem(obj.getName()));
			else {
				JMenu menuChild = new JMenu(obj.getName());
				menu.add(menuChild);
				createAddMenu(child, menuChild);
			}
		}
	}
	
	void setInitialComponents(){
		setSize(pnlCon.constPane.getSize());
		lblRoot.setBounds(10, OBJ_Y, OBJ_WIDTH, getHeight()-50);
		add(lblRoot);
		pnlList.setBounds(getWidth() - OBJ_WIDTH - 10, OBJ_Y, 
				OBJ_WIDTH, getHeight()-50);
		pnlList.setMinimumSize(new Dimension (OBJ_WIDTH, 450));
		pnlList.setPreferredSize(new Dimension (OBJ_WIDTH, 450));
		add(pnlList);	
	}

	void setFileObjectives(Vector obj){		
		listed_objs = obj;	
		repaintList();
	}
		
	void repaintList(){	
		pnlList.removeAll();		
		//display all objectives except for "name"
		for (int i=1; i<listed_objs.size(); i++){
			JObjective lblObj = (JObjective)listed_objs.get(i);
			lblObj.setMaximumSize(new Dimension(OBJ_WIDTH, OBJ_HEIGHT));
			pnlList.add(lblObj);
			lblObj.setTransferHandler(new ObjectiveTransferHandler());			
			lblObj.addMouseListener(mhandler);
		}
		pnlList.setBorder(BorderFactory.createEtchedBorder(1));        		
		pnlList.repaint();
	}

//TREE CONSTRUCTION
	
	private void addObjective(){
        String new_name = (String)JOptionPane.showInputDialog(this, "Objective name:",
                "Add New Objective", JOptionPane.PLAIN_MESSAGE, null, null, "Obj(" + counter + ")");
        
        if (!findDuplicate(new_name)){
		    if ((new_name != null) && (new_name.length() > 0)) {  
		        JObjective lblNew = new JObjective(new_name);	        
				DefaultMutableTreeNode new_node = new DefaultMutableTreeNode(lblNew);			        
				addToTree(new_node, lblSel, true);
				lblNew.setTransferHandler(new ObjectiveTransferHandler());
				lblNew.addMouseListener(mhandler);		
		    }	    
        }
        else
			JOptionPane.showMessageDialog(this, "Objective name \"" + new_name + "\" already exists.  Objective will not be added.", 
					"Duplicate name", JOptionPane.WARNING_MESSAGE);
	}
	
	private boolean findDuplicate(String str){
		for (Enumeration en = root_node.breadthFirstEnumeration(); en.hasMoreElements();)
			if (en.nextElement().toString().equals(str))
				return true;
		for (Iterator it = listed_objs.iterator(); it.hasNext();)
			if (it.next().toString().equals(str))
				return true;
		return false;
	}
        
	public void addFromVC(String par, JObjective ch){
		DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(ch);
		findNode(par);
		JObjective parentobj = (JObjective)found_node.getUserObject();
		addToTree(childNode, parentobj, true);
		ch.setTransferHandler(new ObjectiveTransferHandler());
		ch.addMouseListener(mhandler);	
	}
	
	void addToTree(DefaultMutableTreeNode new_node, JObjective obj2, boolean addToModel){
		//add JObjective to tree		
		counter++;
		if (addToModel)
			tree_model.insertNodeInto(new_node, found_node, found_node.getChildCount());
		JObjective obj1 = (JObjective)new_node.getUserObject();
		//display new JObjective
		int x1 = obj2.getX(),		//should be dropped on obj
		y1 = obj2.getY(),
		ht1 = obj2.getHeight(),
		x2, y2, ht2;
	ht2 = ht1 / found_node.getChildCount();
	y2 = y1 + ((found_node.getChildCount() - 1) * ht2);
	x2 = x1 + OBJ_WIDTH;//??
	obj1.setBounds(x2, y2, OBJ_WIDTH, ht2);
	add(obj1);
	repaint();	
		adjustSiblings(found_node, obj1);
		pnlCon.getAltPanel().updateAllObjs(obj1);		
		pnlCon.getAltPanel().updateTable();
		checkObjectiveCount();
		 //this cascades the children for DnD: repaints the labels but does change model 
		for (int i=0; i<new_node.getChildCount(); i++){
			addToTree((DefaultMutableTreeNode)new_node.getChildAt(i), obj1, false);
		}		
	}
			
	private void adjustSiblings(DefaultMutableTreeNode parent, JObjective lblObj){
	//adjust size/position of siblings		
	JObjective lblChild, lblParent;
	DefaultMutableTreeNode child_node;
	for (int i = 0; (i < parent.getChildCount()); i++){
		child_node = (DefaultMutableTreeNode)parent.getChildAt(i);			
		lblChild = (JObjective)child_node.getUserObject();
		lblParent = (JObjective)parent.getUserObject();
		if (!lblChild.equals(lblObj)){
			int ht2 = lblParent.getHeight() / parent.getChildCount();
			int y2 = lblParent.getY() + (i * ht2);		
			lblChild.setBounds(lblChild.getX(), y2, lblChild.getWidth(), ht2);
		}
			repaint();
			adjustSiblings(child_node, lblObj);
				
		}
	}
	
	private void removeObjective(){		
		//remove from display
		DefaultMutableTreeNode parent_node = (DefaultMutableTreeNode)found_node.getParent();
		for (Enumeration en = found_node.breadthFirstEnumeration(); en.hasMoreElements();){
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) en.nextElement();
			JObjective obj = (JObjective) node.getUserObject();
			remove(obj);
		}
		found_node.removeFromParent();		
		adjustSiblings(parent_node, null);
		pnlCon.getAltPanel().updateTable();		
		checkObjectiveCount();		
	}
	
	private void addBranchToList(){
		for (Enumeration en = found_node.breadthFirstEnumeration(); en.hasMoreElements();){
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)en.nextElement();
			addToList(listed_objs.size(), (JObjective)node.getUserObject());
		}
	}
	
	void addToList(int idx, JObjective obj){
		listed_objs.add(idx, obj);
		repaintList();
	}
	
//SUPPORTING	
	
	private void findNode(String str){
		for (Enumeration en = root_node.breadthFirstEnumeration(); en.hasMoreElements();){
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)en.nextElement();
			if (node.getUserObject().toString().equals(str)){
				found_node = node;	
				return;
			}
		}
	}
	boolean ok;//- 
	public void setPrimitiveObjectives(){
	ok = true;//-
		prim_obj = new Vector();
		DefaultMutableTreeNode node = root_node.getFirstLeaf();
		while (node != null){
			JObjective obj = (JObjective)node.getUserObject();
			prim_obj.add(obj);
			if (obj.origin == 1)//-
				ok = false;//-
			node = node.getNextLeaf();
		}
	//if (ok)//-
		//pnlCon.btnOK.setEnabled(true);
	//else
		//pnlCon.btnOK.setEnabled(false);
	}
	
	public Vector getPrimitiveObjectives(){
		setPrimitiveObjectives();		
		return prim_obj;
	}
	
	void renameObjective(){
        String new_name = (String)JOptionPane.showInputDialog(
                this,
                "Objective name:",
                "Rename Objective",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                lblSel.getName());
        //excepton handling needed here		
        String name = lblSel.getName();
		lblSel.setName(new_name);
		pnlCon.getAltPanel().updateObjDetails(lblSel, name);   
		pnlCon.getAltPanel().updateTable();		
	}
    
	protected void showDetails() {
		ObjectiveDetails dlgDet = new ObjectiveDetails(pnlCon); 
        dlgDet.updateFields(lblSel);        
        dlgDet.showFrame(true);
    }

	void checkObjectiveCount(){
		setPrimitiveObjectives();
		if (prim_obj.size()<2)
			pnlCon.constPane.setEnabledAt(1, false);
		else
			pnlCon.constPane.setEnabledAt(1, true);
		double weights=0.0;
		for (Iterator it = prim_obj.iterator(); it.hasNext();){
			JObjective obj = (JObjective)it.next();
			if (!obj.getWeight().equals("*"))
				weights += Double.valueOf(obj.getWeight()).doubleValue();			
		}
		pnlCon.getAltPanel().checkAlternativeCount();
		if (weights <= 0.98 || weights >= 1.02){
			pnlCon.btnOK.setEnabled(false);		
		}
	}
	
//OUTPUT
	
	void writeOutput(DefaultMutableTreeNode node, Vector color){
		String str = "";
		for (int i=0; i<=node.getLevel(); i++)
			str = str + "\t";
		JObjective obj = (JObjective)node.getUserObject();
		if (!node.isLeaf()){		
			str = str + "attributes " + obj.toString() + " " + obj.getWeight() + "\n";
		}
		
		else {
			str = str + obj.toString() + " " + obj.getWeight() + " { ";			
			//double vals[] = new double[obj.num_points];
			//Vector vals = obj.domain.getWeights();
			double vals[] = obj.domain.getWeights();
			for (int i=0; i<obj.domain.getWeights().length; i++){
				if (obj.getType() == 1){									
					String elts[] = obj.domain.getElements();
					str = str + "\"" + elts[i] + "\" "; 
				}
				else{
					double kts[] = obj.domain.getKnots();
					str = str + kts[i] + " ";
			
				}
				str = str + vals[i];
				if (i != (obj.domain.getWeights().length-1))
					str =str + ",";
				str = str + " ";
					
			}
			str = str + "}\n";
			for (int i=0; i<=node.getLevel()+1; i++)
				str = str + "\t";
			
			//if (colorcount < 10)
			str = str + "color=" +	obj.getName();			
				//str = str + "color=" + colors.getColorName(colorcount);			
			//colorcount++;
			
			if ((obj.getType()==2)&&(obj.getUnit()!="")){
				str = str + " units=" + obj.getUnit();				
			}
			str = str + " end\n";
		}					
		output = output + str;		
		for(int i = 0; i < node.getChildCount(); i++){				
			writeOutput((DefaultMutableTreeNode)node.getChildAt(i), color);
			if (i==node.getChildCount()-1){
			for (int j=0; j<=node.getLevel(); j++)
				output = output + "\t";
			output = output + " end\n";
			}
			
		}
	}
	
	public String getObjectiveOutput(Vector color){
		output = "";
		writeOutput(root_node, color); 
		colorcount=0;
		return output;		
	}
  
//HANDLERS/LISTENERS
	
    public void actionPerformed(ActionEvent e){
    	//PopupMenu listener    
	    	if ("Add".equals(e.getActionCommand()))
	    		addObjective();    	
	    	else if ("Remove".equals(e.getActionCommand())){
	    		removeObjective();
	    		addBranchToList();
	    	}
	    	else if ("Delete".equals(e.getActionCommand())){
	    		if (listed_objs.contains(lblSel)){
	    			listed_objs.remove(listed_objs.indexOf(lblSel));
	    			repaintList();
	    		}	    			
	    		else
	    			removeObjective();
	    	}
	    	else if ("Rename".equals(e.getActionCommand()))
	    		renameObjective();
	    	else if ("Details".equals(e.getActionCommand()))
	    		showDetails();  	    	
	    	else if ("test".equals(e.getActionCommand())){
	    		for (Enumeration en = root_node.breadthFirstEnumeration(); en.hasMoreElements(); ){
	    		}
	    			
	    	}
    }	
    
    private class ResizeHandler extends ComponentAdapter{
        public void componentResized(ComponentEvent e){         	
        	setInitialComponents();   
        	try{
        		adjustSiblings(root_node, null);
        	}catch(ArrayIndexOutOfBoundsException ex){}
        	repaint();
        }        
    }

	private class MouseHandler extends MouseInputAdapter{

		public void mousePressed(MouseEvent me){
			JComponent c = (JComponent)me.getSource();
			TransferHandler handler = c.getTransferHandler();
			handler.exportAsDrag(c, me, TransferHandler.COPY);		
			
			if("JObjective".equals(me.getComponent().getClass().getName())){				
				lblSel = (JObjective)me.getComponent();
				found_node = null;	//reinitialize; if node is not in hierarchy it will remain null					
				findNode(me.getComponent().toString());
				//If an Objective label is right-clicked, present the popup menu
	            if(SwingUtilities.isRightMouseButton(me)){
					if (found_node!=null){
						if (found_node.isRoot()){
							menuRemove.setEnabled(false);
							menuDelete.setEnabled(false);
						}
						else{
							menuRemove.setEnabled(true);
							menuDelete.setEnabled(true);
						}
						if (found_node.isLeaf())
							menuDetails.setEnabled(true);
						else
							menuDetails.setEnabled(false);
						popObjective.show(me.getComponent(), me.getX()+5, me.getY()+5);
					}
					else{
						menuAdd.removeAll();
						JObjective root = (JObjective)root_node.getUserObject();
						menuRoot= new JMenu(root.getName());
						menuAdd.add(menuRoot);
						createAddMenu(root_node, menuRoot);
						popList.show(me.getComponent(), me.getX()+5, me.getY()+5);
					}
	            }
	            me = null;
			}		
		}
	}
	
	class ObjectiveTransferHandler extends TransferHandler{
		private static final long serialVersionUID = 1L;
		JObjective obj1;	//export		
		JObjective obj2;	//import
		String str2;
		
		public String exportString(JComponent c){
			try{
				obj1 = (JObjective)c;
				return obj1.getName();				
			}catch (ClassCastException cce){
				return null;}
		}
		
		public void importString(JComponent c, String s){	
			boolean found1 = false;
			boolean found2 = false;
			boolean is_root = false;
			if(!(c.toString().startsWith("javax.swing.JPanel")))
				obj2 = (JObjective)c;
			else obj2 = new JObjective("jpanel");	//temp: to add to end of list (need to find a bettr way)
		
			if (!(s.equals(obj2.getName())) && !s.equals(null)){	//prevents accidental DnD on itself
			//check if objs are in list, 2 different iterations required
				
				//process export from list or tree
				for (int i=0; i<listed_objs.size(); i++){
					if (s.equals(listed_objs.get(i).toString())){
						obj1 = (JObjective)listed_objs.get(i);
						listed_objs.remove(i); 
						repaintList();
						found1 = true; break;
					}
				}	
				
				if (!found1){	//if obj1 is from tree, remove
					findNode(s);
					if (found_node.isRoot())
						is_root=true;
					obj1 = (JObjective)found_node.getUserObject();
					//note: this will always add the objectives to the end of the list
					if (!is_root)
						removeObjective();
				}
				
				if (!is_root){	//prevents any movement of root node
				
					//if dragged to panel, add to end of list
					//note: this is applicable for list->list dnd for reordering
					if(obj2.getName().equals("jpanel")){
						if (found1)
							addToList(listed_objs.size(), obj1);
						else
							addBranchToList();
					}
					
					//process import to list or tree
					else{
						for (int i=0; i<listed_objs.size(); i++){
							if (obj2==(JObjective)listed_objs.get(i)){
								found2 = true;
								if (found1)
									addToList(i + 1, obj1);
								else
									addBranchToList();
								break;					
							}
						}
						if (!found2){
							DefaultMutableTreeNode node1; 
							if (found1)
								node1 = new DefaultMutableTreeNode(obj1);
							else
								node1 = found_node;
							findNode(obj2.getName());
							addToTree(node1, (JObjective)found_node.getUserObject(), true);						
						}
					}
				}
			}					
		}
		
		public void cleanup(JComponent c, boolean remove){
			pnlCon.repaint();				
		}
		
		//-------------------------------------------------------
		//-------------------------------------------------------
		
	    protected Transferable createTransferable(JComponent c) {
	        return new StringSelection(exportString(c));
	    }
  
	    public int getSourceActions(JComponent c) {
	        return COPY_OR_MOVE;
	    }
	    
	    public boolean importData(JComponent c, Transferable t) {
	        if (canImport(c, t.getTransferDataFlavors())) {
	            try {
	                String str = (String)t.getTransferData(DataFlavor.stringFlavor);
	                importString(c, str);
	                return true;
	            } catch (UnsupportedFlavorException ufe) {
	            } catch (IOException ioe) {
	            }
	        }

	        return false;
	    }
  
	    protected void exportDone(JComponent c, Transferable data, int action) {
	        cleanup(c, action == MOVE);
	    }
  
	    public boolean canImport(JComponent c, DataFlavor[] flavors) {
	        for (int i = 0; i < flavors.length; i++) {
	            if (DataFlavor.stringFlavor.equals(flavors[i])) {
	                return true;
	            }
	        }
	        return false;		
	    }
	}
}




	

