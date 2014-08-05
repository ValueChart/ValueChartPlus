import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Visual component for modifying discrete domain (utility/value/score function) values.
 * 
 * This class is a variation of the ContinuousUtilityGraph class. the only difference is that
 * the function call is to DiscreteAttributeDomain
 * Also, the paint function has function class that draw bars instead of lines.
*/
public class DiscreteUtilityGraph extends JPanel implements MouseListener, MouseMotionListener {
	private static final long serialVersionUID = 1L;
	MoveablePoint[] p;
    MoveablePoint[] base;
    Line2D.Float[] lines;
    MoveablePoint moving;
    //Line2D.Float line;
    int xaxis;
    DiscreteAttributeDomain ddomain;
    DiscreteAttributeDomain undo;
    DiscreteAttributeDomain redo;
    double[] weights; 
    int discrete_elements;
    String[] items;
    int clicki; //This is to keep correct track of what is clicked
    String attributeName;
    ValueChart chart;
    DefineValueFunction dvf;
    int Incre; //This specifies how far each point is apart from each other
    boolean undone;
    AttributeCell acell;
    JPanel pnl;    
    boolean modified = false;
    boolean fromChart = false; // graph opened by clicking on chart interface
    
    public static Font LABELFONT = new Font("Verdana", Font.PLAIN, 10);
    public static Font LABELFONT_BOLD = new Font("Verdana", Font.BOLD, 10);
    public static final AffineTransform ANGLEFONT =
            AffineTransform.getRotateInstance(-Math.PI / 6.0);
    public static Font VLABELFONT = LABELFONT.deriveFont(ANGLEFONT);
    public static Font VLABELFONT_BOLD = LABELFONT_BOLD.deriveFont(ANGLEFONT);
    
    int width = 400;
    int height = 320;
    
    int getSpacing(int i){
    	int sp = 0;
    	if (items.length > 2)
    		sp = 70;
    	else if (items.length == 2 && i == 0)
    		sp = 120;
    	else if (items.length == 2 && i == 1)
    		sp = 80;
    	return sp;
    }
    
    public DiscreteUtilityGraph(ValueChart ch, boolean fromCh, DiscreteAttributeDomain dd, String[] it, double[] we, String att, DefineValueFunction d, AttributeCell ac) {
        
        //Setting variable names
        ddomain = dd;
        chart = ch;
        items = it;
        weights = we;
        attributeName = att;
        undo = new DiscreteAttributeDomain();
        redo = new DiscreteAttributeDomain();
        dvf = d;
        acell = ac;
        fromChart = fromCh;
        
       for(int i = 0; i < items.length; i++){
            undo.addElement(items[i], ddomain.getEntryWeight(items[i]));
            redo.addElement(items[i], ddomain.getEntryWeight(items[i]));
        }
     
        setBackground(Color.white);
        p = new MoveablePoint[items.length];
        base = new MoveablePoint[items.length];
        lines = new Line2D.Float[items.length];
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
        if (items.length == 1) return;
        Incre = (width - 120) / (items.length-1); // This is the variable that calculate how far each point should be.
        //Creating all the points of utility
        for(int i = 0; i < items.length; i++){
            p[i] = new MoveablePoint(((Incre * i) + getSpacing(i)), ((int) (height - 60 - (weights[i] * (height-60)) + 5)));
        }
        
        //Making the basepoints
        for(int i = 0; i < items.length; i++){
            base[i] = new MoveablePoint(((Incre * i) + getSpacing(i)), height - 55);
        }
        
        //Joining all the points into lines
        for(int i = 0; i < items.length; i++){
            lines[i] = new Line2D.Float(p[i], base[i]);
        }        
    }
    
    void setGraph(){
        pnl = new JPanel();
        this.setPreferredSize(new Dimension(width,height));
        pnl.add(this, BorderLayout.CENTER);
    }

    DiscreteUtilityGraph getGraph(){
    	return(this);
    }
    
    public void mouseClicked(MouseEvent me) {
        modified = true;
        if((me.getX()< 40) && (me.getX() > 0) && (me.getY() > height-20)){
            undone = true;
            int Incre = (width-35) / items.length; // This is the variable that calculate how far each point should be.
            for(int i = 0; i < items.length; i++){
                redo.removeElement(items[i]);
                redo.addElement(items[i],ddomain.getEntryWeight(items[i]));
            }
            for(int i = 0; i < items.length; i++){
                ddomain.removeElement(items[i]);
                ddomain.addElement(items[i],undo.getEntryWeight(items[i]));
            }
            
            
            //Updating the weight
            for(int i = 0; i < items.length; i++){
                p[i] = new MoveablePoint(((Incre * i) + getSpacing(i)), ((int) (height-60 - (undo.getEntryWeight(items[i]) * (height-60)) + 5)));
            }
            
            for(int i = 0; i < items.length; i++){
                lines[i] = new Line2D.Float(p[i], base[i]);
            }
            
            repaint();
            if (chart != null)
            	chart.updateAll();
        }
        
        if((me.getX()< width-25) && (me.getX() > width-65) && (me.getY() > height-20) && (undone == true)){
            int Incre = (width-35) / items.length; // This is the variable that calculate how far each point should be.
            undone = false;
            for(int i = 0; i < items.length; i++){
                ddomain.removeElement(items[i]);
                ddomain.addElement(items[i], redo.getEntryWeight(items[i]));
            }
            
            for(int i = 0; i < items.length; i++){
                p[i] = new MoveablePoint(((Incre * i) + getSpacing(i)), ((int) (height-60 - (redo.getEntryWeight(items[i]) * (height-60)) + 5)));
            }
            
            //Rejoining the lines
            for(int i = 0; i < items.length; i++){
                lines[i] = new Line2D.Float(p[i], base[i]);
            }
            
            repaint();
            if (chart != null)
            	chart.updateAll();
        }
    }
    public void mouseEntered(MouseEvent me) { }
    public void mouseExited(MouseEvent me) { }
    public void mouseMoved(MouseEvent me) { }
    
    public void mousePressed(MouseEvent me) {
        
        for (int i = 0; i < items.length; i++) {
            //This long conditional statement only makes the dot more sensitive. Nothing too interesting happening here
            if (((p[i].hit(me.getX(), me.getY())) || (p[i].hit(me.getX() + 1, me.getY() + 1)) || (p[i].hit(me.getX() - 1, me.getY() - 1))
                || (p[i].hit(me.getX() + 1, me.getY())) || (p[i].hit(me.getX(), me.getY() + 1)) || (p[i].hit(me.getX(), me.getY() - 1)) 
                || (p[i].hit(me.getX(), me.getY() - 1)) || (p[i].hit(me.getX() + 2, me.getY() + 2)) || (p[i].hit(me.getX() + 2, me.getY()))
                || (p[i].hit(me.getX() - 2, me.getY() - 2)) || (p[i].hit(me.getX(), me.getY() + 2)) || (p[i].hit(me.getX() - 2, me.getY())) 
                || (p[i].hit(me.getX(), me.getY() - 2)) ||
                (p[i].hit(me.getX() + 3, me.getY() + 3)) || (p[i].hit(me.getX() + 3, me.getY()))
                || (p[i].hit(me.getX() - 3, me.getY() - 3)) || (p[i].hit(me.getX(), me.getY() + 3)) || (p[i].hit(me.getX() - 3, me.getY())) 
                || (p[i].hit(me.getX(), me.getY() - 3)))) {
                setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                xaxis = (int)base[i].x;
                moving = p[i];
                clicki = i;
                if (chart != null && fromChart) {
                     LastInteraction interact = new LastInteraction(chart);
                     interact.setUndoUtil(this, items[i], 0, weights[i], attributeName);
                     chart.addInteraction(interact);                
                }
                
            for(int e = 0; e < items.length; e++){
                undo.removeElement(items[e]);
                
                undo.addElement(items[e],ddomain.getEntryWeight(items[e]));
            }
                
                undo.removeElement(items[clicki]);
                undo.addElement(items[clicki],((float)(height-55 - me.getY())) / (height-60));
                movePoint(xaxis, me.getY());
                return;
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
        for(int i = 0; i < (items.length); i++){
            lines[i].setLine(p[i], base[i]);
        }
        repaint();
         
        //Update domain
	    //ddomain.removeElement(items[clicki]);
	    //ddomain.addElement(items[clicki],((float) (205 - y) / 200));
        
	    ddomain.changeWeight(items[clicki],((float) (height-55 - y) / (height-60)));
	    if (acell != null && acell.dg != null)
	        acell.dg.plotPoints();
	    //Update Value Chart
	    if (chart!=null) {  
	        chart.updateAll();
	    }
        if (!fromChart) {
            dvf.con.validateTabs();
        }
        
    }
    
    public void paintComponent(Graphics gfx) {
        super.paintComponent(gfx);
        Graphics2D g = (Graphics2D) gfx;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
        g.setPaint(Color.blue);
        //Draw all the lines
        for(int i = 0; i < (items.length); i++){
            g.draw(lines[i]);
            //To make the lines thicker
            g.drawLine((int)p[i].x - 1,  (int)p[i].y, (int)base[i].x - 1, (int)base[i].y);
            g.drawLine((int)p[i].x - 2,  (int)p[i].y, (int)base[i].x - 2, (int)base[i].y);
            g.drawLine((int)p[i].x + 1,  (int)p[i].y, (int)base[i].x + 1, (int)base[i].y);
            g.drawLine((int)p[i].x + 2,  (int)p[i].y, (int)base[i].x + 2, (int)base[i].y);
            g.drawLine((int)p[i].x - 3,  (int)p[i].y, (int)base[i].x - 3, (int)base[i].y);
            g.drawLine((int)p[i].x + 3,  (int)p[i].y, (int)base[i].x + 3, (int)base[i].y);
            g.drawLine((int)p[i].x - 4,  (int)p[i].y, (int)base[i].x - 4, (int)base[i].y);
            g.drawLine((int)p[i].x + 4,  (int)p[i].y, (int)base[i].x + 4, (int)base[i].y);
        }
        
        Shape[] s;
        s = new Shape[items.length];
        String temp;
        g.setColor(Color.DARK_GRAY);
        for(int i = 0; i < items.length; i++){
            if (chart != null && chart.displayUtilityWeights) {
                Float test = new Float((float) (height-55 - p[i].y) / (height-60)*100); //converted domain values
                DecimalFormat df = new DecimalFormat("#.#");
                temp = df.format(test);
                g.drawString(temp, (p[i].x + 5),p[i].y);
            }
            s[i] = p[i].getShape();
        }
        
        g.setColor(Color.red);
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
        for(int i = 0; i < items.length; i++){
            g.draw(s[i]);
        }
        //Draw the Line axis
        g.drawLine(50, 5, 50, height-55); //y-axis
        g.drawLine(50, height-55, width-15, height-55); //x-axis
        
        //Draw the static labels
        g.setFont(new Font(null, Font.BOLD, 12));
        String utility_upper_bound = new String("Best");
        g.drawString(utility_upper_bound, 10, 15);
        String utility_lower_bound = new String("Worst");
        g.drawString(utility_lower_bound, 10, height-55);
        
        //Drawing the labels from variables passed
        g.setFont(new Font(null, Font.BOLD, 13));
        g.drawString(attributeName, width/2 - 3*attributeName.length() ,height-15);
        Incre = (width - 120) / (items.length-1); // This is the variable that calculate how far each point should be.
        //Labelling different utilities
        weights = ddomain.getWeights();
        for(int i = 0; i < items.length; i++){
            if((weights[i] == 0.0) || (weights[i] == 1.0)){
                g.setFont(new Font(null, Font.BOLD, 10));
            }
            else{
                g.setFont(new Font(null, Font.PLAIN, 10));
            }
            String text = items[i];
            if(items[i].length()>12){
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
                    g.drawString(text,(((Incre * i) + getSpacing(i) - 3 * (text.length()))),height-40);
                } else {
                    g.drawString(text.substring(0, idx), (((Incre * i) + getSpacing(i) - 3 * (idx))),height-45);
                    g.drawString(text.substring(idx, text.length()),(((Incre * i) + getSpacing(i) - 3 * (text.length()-idx))),height-35);
                }
            } else {
                g.drawString(text,(((Incre * i) + getSpacing(i) - 3 * (text.length()))),height-40);
            }
        }
        
        //Drawing the Undo and Redo button
        g.setFont(new Font(null, Font.PLAIN, 12));
        g.setColor(Color.RED);
        g.drawString("UNDO", 2, height-5);
        g.drawString("REDO", width-40, height-5);        
    }
    
    
    private void showGraph(){
        JFrame frame = new JFrame(attributeName + " utility graph");
        
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
        this.setPreferredSize(new Dimension(width, height));
        
        frame.getContentPane().add(this, BorderLayout.NORTH);
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
            shape = new Rectangle(x-r, y-r, 2*r, 2*r);
            //new Ellipse2D.Float(x - r, y - r, 2*r, 2*r);
        }
        
        public boolean hit(int x, int y) {
            return shape.contains(x, y);
        }
        
        public Shape getShape() {
            return shape;
        }
    }
    
}




