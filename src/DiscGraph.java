import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;


public class DiscGraph extends JPanel implements MouseListener, MouseMotionListener {
	private static final long serialVersionUID = 1L;
	private static final int MIN_HEIGHT = 20;
	private static final int 	TOP = 0;
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
    int Incre; //This specifies how far each point is apart from each other
    boolean undone;
    Font font;
    Color color;
    
    BufferedImage bi;
    Image buffer;
    Graphics2D bg;
    
    int getBottom(){
    	return getHeight()>MIN_HEIGHT-8 ? 7 : 2;
    }
    
    public DiscGraph(ValueChart ch, DiscreteAttributeDomain dd, String[] it, double[] we, String att, Color col) {
    	
        //Setting variable names
        ddomain = dd;
        chart = ch;
        items = it;
        weights = we;
        attributeName = att;
        undo = new DiscreteAttributeDomain();
        redo = new DiscreteAttributeDomain();
        color = col;
        
        for(int i = 0; i < items.length; i++){
            undo.addElement(items[i], ddomain.weight(items[i]));
            redo.addElement(items[i], ddomain.weight(items[i]));
        }
     
    	font = new Font ("Arial", Font.BOLD, 7);
        setBackground(Color.white);
        p = new MoveablePoint[items.length];
        base = new MoveablePoint[items.length];
        lines = new Line2D.Float[items.length];   
        setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));       
        addMouseListener(this);
        addMouseMotionListener(this);
        addComponentListener(new ResizeHandler());
        //showGraph();
        plotPoints();
    }
    
    int getSpacing(int i){
    	int sp = 0;
    	if (items.length > 2)
    		sp = getWidth()/10;
    	else if (items.length == 2 && i ==0)
    		sp = 30;
    	else if (items.length == 2 && i == 1)
    		sp = 0;
    	return sp;
    }
    
    void plotPoints(){
    	weights = ddomain.getWeights();    
    	int Incre = (getWidth()-(getWidth()/4)) / (items.length-1);
    	int ht = getHeight()-getBottom();
	    //Creating all the points of utility
	    for(int i = 0; i < items.length; i++){
	        p[i] = new MoveablePoint(((Incre * i) + getSpacing(i)), ((int) (ht - (weights[i] * ht)))+ TOP);
	    }
	    
	    //Making the basepoints
	    for(int i = 0; i < items.length; i++){
	        base[i] = new MoveablePoint(((Incre * i) + getSpacing(i)), getHeight()-getBottom());
	    }
	    
	    //Joining all the points into lines
	    for(int i = 0; i < items.length; i++){
	        lines[i] = new Line2D.Float(p[i], base[i]);
	    }
	    repaint();
    }
    
    private class ResizeHandler extends ComponentAdapter {
        public void componentResized(ComponentEvent e){ 
        	plotPoints();
        }
    }

    
    public void mouseClicked(MouseEvent me) {
        //was undo code
    }
    public void mouseEntered(MouseEvent me) { }
    public void mouseExited(MouseEvent me) { }
    public void mouseMoved(MouseEvent me) { }
    public void mousePressed(MouseEvent me) {
        
        for (int i = 0; i < items.length; i++) {
            if (p[i].hit(me.getX(), me.getY())) {
                xaxis = (int)base[i].x;
                moving = p[i];
                clicki = i;
                chart.last_int.setUndoUtil(this, items[i], 0, weights[i], ddomain);         
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
        moving = null;
    }
    public void mouseDragged(MouseEvent me) {
        if(me.getY() <= getHeight()-getBottom() && me.getY() >= TOP){
            movePoint(xaxis, me.getY());
        }
        else{
            if(me.getY() > getHeight()-getBottom()){
                movePoint(xaxis, getHeight()-getBottom());
            }
            else if(me.getY() < TOP){
                movePoint(xaxis, TOP);
            }        
        }
    }
    
    void movePoint(int x, int y) {
        if (moving == null) return;
        moving.setLocation(x, y);
        //Updating all the lines
        for(int i = 0; i < (items.length); i++){
            lines[i].setLine(p[i], base[i]);
        }
        repaint();        
        //Update Value Chart
        //ddomain.removeElement(items[clicki]);
        ddomain.changeWeight(items[clicki], (float)((getHeight()-getBottom())-y)  / (getHeight()-getBottom()-TOP));        
        chart.updateAll();        
    }
    
    public void paintComponent(Graphics gfx) {
        super.paintComponent(gfx);      
        Graphics2D g = (Graphics2D) gfx;    
       
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
        g.setFont(font);
        
        //g.setColor(Color.gray);
        //g.drawRect (0,0,getWidth(), getHeight());
        
        g.setPaint(color);
        //Draw all the lines
        
        int px, py;
        for(int i = 0; i < (items.length); i++){
        	px = (int)p[i].x;
        	py = (int)p[i].y;
            //g.draw(lines[i]);
            //To make the lines thicker
            //g.drawLine((int)p[i].x - 1,  (int)p[i].y, (int)base[i].x - 1, (int)base[i].y);
            //g.drawLine((int)p[i].x - 2,  (int)p[i].y, (int)base[i].x - 2, (int)base[i].y);
            g.drawLine(px - 3, getHeight() - getBottom(), px - 3, py);
            //g.drawLine((int)p[i].x + 1,  (int)p[i].y, (int)base[i].x + 1, (int)base[i].y);
            g.drawLine( px + 2, getHeight() - getBottom(), px + 2, py );
        }
        
        Shape[] s;
        s = new Shape[items.length];
        String temp;
        g.setPaint(Color.DARK_GRAY);
        weights = ddomain.getWeights();
        for(int i = 0; i < items.length; i++){
            //Float test = new Float(((getHeight()-getBottom()) - p[i].y) / (getHeight()-15));
            temp = String.valueOf(weights[i]);
            if (getHeight() > MIN_HEIGHT)
            	g.drawString(temp.substring(0,3), (p[i].x + 5),p[i].y < 5 ? p[i].y + 6 : p[i].y);
            s[i] = p[i].getShape();
        }        
        g.setColor(color);
        for(int i = 0; i < items.length; i++){
            g.fill(s[i]);
        }
        g.setColor(Color.black);
        //for(int i = 0; i < items.length; i++){
        //    g.draw(s[i]);
        //}
        //Draw the Line axis
        g.drawLine(5, 0, 5, getHeight()-getBottom()); //y-axis
        g.drawLine(5, getHeight()-getBottom(), getWidth() - 5, getHeight()-getBottom()); //x-axis
        
        //Draw the static labels  
        g.setFont(font);
        if (getHeight() > MIN_HEIGHT - 5){
        	String utility_upper_bound = new String("1");
        	g.drawString(utility_upper_bound, 1, 5);
        	String utility_lower_bound = new String("0");
        	g.drawString(utility_lower_bound, 1, getHeight()-getBottom());
        }
        
        //Drawing the labels from variables passed
        //g.drawString(attributeName, 90 ,240);
        int Incre = (getWidth()-(getWidth()/4)) / (items.length-1); // This is the variable that calculate how far each point should be.
        //Labelling different utilities
    	((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
				  RenderingHints.VALUE_TEXT_ANTIALIAS_ON);  
    	if (getHeight() > MIN_HEIGHT - 8){
	        for(int i = 0; i < items.length; i++){
	            g.drawString(items[i], ((Incre * i) - items[i].length()*2) + getSpacing(i), getHeight()-1);
	        }       
    	}

    }
       
    class MoveablePoint extends Point2D.Float {
        int r = 3;
        Shape shape;
        public MoveablePoint(int x, int y) {
            super(x, y);
            setLocation(x, y);
        }
        
        void setLocation(int x, int y) {
            super.setLocation(x, y);
            if (getHeight() < MIN_HEIGHT)
            	shape = new Rectangle(x-r, y, 2*r, 1);
            else
            	shape = new Rectangle(x-r, y-r, 2*r, 2*r);
        }
        
        public boolean hit(int x, int y) {
            return shape.contains(x, y);
        }
        
        public Shape getShape() {
            return shape;
        }
    }
    
}




