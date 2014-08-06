import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Visual component for modifying continuous domain (utility/value/score function) values.
 * 
 * Author: Victor Chung
 * This class itself is "almost" self-contained. Unlike other classes in the program, the function call in this
 * class is not as spiral-like.
 * I personally think the Graphics2D paint function is the most interesting ones. Actually, most of the drawing is done in there for the utility graph 
 * Comments are added below
 */
public class ContinuousUtilityGraph extends JPanel implements MouseListener, MouseMotionListener {
	private static final long serialVersionUID = 1L;
	//The basic idea is I have an array list of points, which has x-axis and y-axis.
    MoveablePoint[] p;
    //Lines will join the points
    Line2D.Float[] lines;
    MoveablePoint moving;
    int xaxis;//This one is needed to keep track of the x-axis. This is used when a point is being dragged, and make sure the dragging only happens along the y-axis.
    ContinuousAttributeDomain cdomain;
    int discrete_elements;
    double[] items;
    double[] weights;
    double[] undo; //This is used for undoing purposes
    String unit;
    int clicki; //This is to keep correct track of what is clicked
    String attributeName;
    ValueChart chart;
    DefineValueFunction dvf;
    AttributeCell acell;
    
    JPanel pnl;
    boolean modified = false;
    boolean fromChart = false; // graph opened by clicking on chart interface
    
    public static int width = 400;
    public static int height = 320;

    public ContinuousUtilityGraph(ValueChart ch, boolean fromCh, ContinuousAttributeDomain dd, double[] it, double[] we, String un, String att, DefineValueFunction d, AttributeCell ac) {
       	
        //Setting variable names
        cdomain = dd;
        chart = ch;
        items = it;
        weights = we;
        unit = un;
        attributeName = att;
        dvf = d;
        acell = ac;
        fromChart = fromCh;

        
        setBackground(Color.white);
        p = new MoveablePoint[items.length];
        lines = new Line2D.Float[items.length - 1];
        plotPoints();
        addMouseListener(this);
        addMouseMotionListener(this);
        if (chart!=null && fromChart) {
            if (acell != null && acell.getData() != null) {
                AttributeData attrData = acell.getData();
                if (attrData != null)
                    chart.setLogOldAttributeData(LogUserAction.getSingleDataOutput(attrData, LogUserAction.OUTPUT_STATE));
            }
            showGraph();
        }
        else
        	setGraph();      
        

    }
    
    void plotPoints(){
        //Creating all the points of utility
        for(int i = 0; i < items.length; i++){
        	p[i] = new MoveablePoint(((int)(((items[i]-items[0])/((items[(items.length)-1])-items[0]))*(width-75)))+50, ((int) ((height-60) - (weights[i] * (height-60)) + 5)));
            //p[i] = new MoveablePoint(((Incre * i) + 50), ((int) (200 - (weights[i] * 200) + 5)));
        }
        
        //Joining all the points into lines
        for(int i = 0;  i < (items.length - 1); i++){
            lines[i] = new Line2D.Float(p[i], p[i+1]);
        }
        repaint();
    }
    
    void setGraph(){
        pnl = new JPanel();
        this.setPreferredSize(new Dimension(width,height));        
        pnl.add(this, BorderLayout.CENTER);
    }
    
    ContinuousUtilityGraph getGraph(){
    	return(this);
    }

    public void mouseClicked(MouseEvent me) { 
        modified = true;
        if((me.getX()< 40) && (me.getX() > 0) && (me.getY() > height-20)){
            for(int i = 0; i < undo.length; i++){
            cdomain.removeKnot(items[i]);
            cdomain.addKnot(items[i], undo[i]);
        }
       
         //Updating the weight
        for(int i = 0; i < items.length; i++){
        	p[i] = new MoveablePoint(((int)(((items[i]-items[0])/((items[(items.length)-1])-items[0]))*(width-75)))+50, ((int) (height-60 - (weights[i] * (height-60)) + 5)));
        }
            
        //Rejoining the lines
        for(int i = 0; i < (items.length - 1); i++){
            lines[i] = new Line2D.Float(p[i], p[i+1]);
        }
        
        repaint();
        if (chart != null)
        	chart.updateAll();
        }
        
        
        if((me.getX()< width) && (me.getX() > width-65) && (me.getY() > height-20)){
            
        for(int i = 0; i < undo.length; i++){
            cdomain.removeKnot(items[i]);
            cdomain.addKnot(items[i], weights[i]);
        }
       
         //Updating the weight
        for(int i = 0; i < items.length; i++){
        	p[i] = new MoveablePoint(((int)(((items[i]-items[0])/((items[(items.length)-1])-items[0]))*(width-75)))+50, ((int) (height-60 - (weights[i] * (height-60)) + 5)));
        }
            
        //Rejoining the lines
        for(int i = 0; i < (items.length - 1); i++){
            lines[i] = new Line2D.Float(p[i], p[i+1]);
        }
        
        repaint();
        if (chart != null)
        	chart.updateAll();
        }
        
    }
    
    public void mouseEntered(MouseEvent me) { }
    public void mouseExited(MouseEvent me) { }
    public void mouseMoved(MouseEvent me) { 
    }
    
    public void mousePressed(MouseEvent me) {
        for (int i = 0; i < items.length; i++) {
            //This long if statement is only to make the dragging point more sensitive. There is nothing interesting about it.
            if ((p[i].hit(me.getX(), me.getY())) || (p[i].hit(me.getX() + 1, me.getY() + 1)) || (p[i].hit(me.getX() - 1, me.getY() - 1))
                || (p[i].hit(me.getX() + 1, me.getY())) || (p[i].hit(me.getX(), me.getY() + 1)) || (p[i].hit(me.getX(), me.getY() - 1)) 
                || (p[i].hit(me.getX(), me.getY() - 1)) || (p[i].hit(me.getX() + 2, me.getY() + 2)) || (p[i].hit(me.getX() + 2, me.getY()))
                || (p[i].hit(me.getX() - 2, me.getY() - 2)) || (p[i].hit(me.getX(), me.getY() + 2)) || (p[i].hit(me.getX() - 2, me.getY())) 
                || (p[i].hit(me.getX(), me.getY() - 2)) ||
                (p[i].hit(me.getX() + 3, me.getY() + 3)) || (p[i].hit(me.getX() + 3, me.getY()))
                || (p[i].hit(me.getX() - 3, me.getY() - 3)) || (p[i].hit(me.getX(), me.getY() + 3)) || (p[i].hit(me.getX() - 3, me.getY())) 
                || (p[i].hit(me.getX(), me.getY() - 3))) {
                //if (SwingUtilities.isRightMouseButton(me)){
	            //  	dvf.popValueFunction.show(me.getComponent(), me.getX()+5, me.getY()+5);
	            //	setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                //}
                //else{
                	setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
                    xaxis = (int)p[i].x;
                	moving = p[i];
                	clicki = i;
                	if (chart != null && fromChart) {
                        LastInteraction interact = new LastInteraction(chart);
                        interact.setUndoUtil(this, null, items[i], weights[i], attributeName);
                        chart.addInteraction(interact);
                	}
                	movePoint(xaxis, me.getY());  
                //}                
            }
        }
    }
    
    public void mouseReleased(MouseEvent me) {
         
        for (int i = 0; i < items.length; i++) {
            if (p[i].hit(me.getX(), me.getY())) {
                movePoint(xaxis, me.getY());
            }
        }
        
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        moving = null;
    }
    
    public void mouseDragged(MouseEvent me) {
        if(me.getY() < height-55 && me.getY() > 5){
            movePoint(xaxis, me.getY());
        }
        else{
            if(me.getY() > height-55){
                movePoint(xaxis, height-55);
            }
            else if(me.getY() < 5){
                movePoint(xaxis,5);
            }
        }
    }
    
    void movePoint(int x, int y) {
        if (moving == null) return;
        modified = true;
        moving.setLocation(x, y);
        
        //Updating all the lines
        for(int i = 0; i < (items.length - 1); i++){
            lines[i].setLine(p[i].x, p[i].y, p[i+1].x, p[i+1].y);
        }
        repaint();
        
        cdomain.removeKnot(items[clicki]);
        cdomain.addKnot(items[clicki],((float) (height-55 - y) / (height-60)));
        
        if (acell != null && acell.cg != null)
            acell.cg.plotPoints();
        
        if (chart!=null){        	
        	chart.updateAll();
        }
        if (!fromChart) {
        	dvf.con.validateTabs();
        }
        
        //Updating weights
        weights = cdomain.getWeights();
      }
    
    public void paintComponent(Graphics gfx) {
        super.paintComponent(gfx);
        Graphics2D g = (Graphics2D) gfx;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
        g.setPaint(Color.blue);     
      //Draw all the lines
                for(int i = 0; i < (items.length - 1); i++){
            g.draw(lines[i]);
        }
        
        Shape[] s;
        s = new Shape[items.length];
        String temp;
        g.setColor(Color.DARK_GRAY);
        
        for(int i = 0; i < items.length; i++){
            if (chart != null && chart.displayUtilityWeights) {
                Float test = new Float((float) (height-55 - p[i].y) / (height-60)*100);
                DecimalFormat df = new DecimalFormat("#.#");
                temp = df.format(test);
                g.drawString(temp, (p[i].x + 5),p[i].y);
            }
            s[i] = p[i].getShape();
        }
        
        g.setColor(Color.red);      
        //g.fill(s0);  g.fill(s1);
        for(int i = 0; i < items.length; i++){
            if ((weights[i] == 0.0) || (weights[i] == 1.0)) {
                g.setColor(Color.yellow);
                g.fill(s[i]);
                g.setColor(Color.red);
            } else {
                g.fill(s[i]);
            }
        }
        g.setColor(Color.black);    
       // g.draw(s0);  g.draw(s1);
        for(int i = 0; i < items.length; i++){
            g.draw(s[i]);
        }
        //Draw the Line axis
        g.drawLine(50, 5, 50, height-55); //y-axis
        g.drawLine(50, height-55, width-15, height-55); //x-axis
        //Draw the static labels
        g.setFont(new Font(null, Font.BOLD, 12));
//        String utility_upper_bound = new String("1");
//        g.drawString(utility_upper_bound, 35, 15);
//        String utility_lower_bound = new String("0");
//        g.drawString(utility_lower_bound, 35, 205);
        String utility_upper_bound = new String("Best");
        g.drawString(utility_upper_bound, 10, 15);
        String utility_lower_bound = new String("Worst");
        g.drawString(utility_lower_bound, 10, height-55);
        
        //Drawing the labels from variables passed
         g.setFont(new Font(null, Font.BOLD, 12));
         int len = (attributeName + " (" + unit + ")").length();
         g.setFont(new Font(null, Font.BOLD, 13));
        g.drawString((attributeName + " (" + unit + ")"), width/2 - 3 * len ,height-15);
        //Labelling different utilities
        for(int i = 0; i < items.length; i++){
           if((weights[i] == 0.0) || (weights[i] == 1.0)){
                g.setFont(new Font(null, Font.BOLD, 12));
            }
            else{
                g.setFont(new Font(null, Font.PLAIN, 12));
            }    
           
           String text = "";
           if (acell != null) {
               DecimalFormat df = acell.obj.decimalFormat;
               text = (df.format(items[i]));
           } else {
               text = Double.valueOf(items[i]).toString();
           }
           
           if(text.length()>12){
               Pattern pattern = Pattern.compile("[^A-Za-z0-9]");
               Matcher matcher = pattern.matcher(text);
               int mid = text.length()/2;
               // find idx closes to middle
               int idx = -1;
               while (matcher.find()) {
                   idx = matcher.start();
                   if (idx >= mid) break;
                   matcher.group();
               }
               if (idx < 0) {
                   g.drawString(text,((int)(((items[i]-items[0])/((items[(items.length)-1])-items[0]))*(width-75)))+40 ,height-40);
               } else {
                   g.drawString(text.substring(0, idx), ((int)(((items[i]-items[0])/((items[(items.length)-1])-items[0]))*(width-75)))+40 ,height-45);
                   g.drawString(text.substring(idx, text.length()),((int)(((items[i]-items[0])/((items[(items.length)-1])-items[0]))*(width-75)))+40 ,height-35);
               }
           } else {
               g.drawString(text,((int)(((items[i]-items[0])/((items[(items.length)-1])-items[0]))*(width-75)))+40 ,height-40);
           }
               
        }	
        
        //Drawing the Undo and Redo button
        g.setColor(Color.RED);
        g.setFont(new Font(null, Font.PLAIN, 12));
        g.drawString("UNDO", 2, height-5);
        g.drawString("REDO", width-40, height-5);
        
    }
        
    private void showGraph(){
        JDialog frame = new JDialog(chart.getFrame(), attributeName + " utility graph");
        frame.setModal(true);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (modified && chart!=null && fromChart && acell != null && acell.getData() != null) {
                    chart.logUtility(acell.getData());
                }
            }
        });
        //ContinuousUtilityGraph moving = new ContinuousUtilityGraph();
        //moving
        this.setPreferredSize(new Dimension(width,height));
//        this.setPreferredSize(new Dimension(500,500));
        
        frame.getContentPane().add(this, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
        
        
    }
        
    class MoveablePoint extends Point2D.Float {
        private static final long serialVersionUID = 1L;
        
        int r = 4;
        Shape shape;
        public MoveablePoint(int x, int y) {
            super(x, y);
            setLocation(x, y);
        }
        
        void setLocation(int x, int y) {
            super.setLocation(x, y);
            shape = new Ellipse2D.Float(x - r, y - r, 2*r, 2*r);
        }
        
        public boolean hit(int x, int y) {
            return shape.contains(x, y);
        }
        
        public Shape getShape() {
            return shape;
        }
    }
    
}




