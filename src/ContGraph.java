import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.text.DecimalFormat;


public class ContGraph extends JPanel implements MouseListener, MouseMotionListener{
	private static final long serialVersionUID = 1L;
	private static final int 	BOTTOM = 7, 
								TOP = 0;
	private static final int MIN_HEIGHT = 20;
    MoveablePoint[] p;
    Line2D.Float[] lines;
    MoveablePoint moving;
    //Line2D.Float line;
    int xaxis;
    ContinuousAttributeDomain cdomain;
    int discrete_elements;
    double[] items;
    double[] weights;
    double[] undo; 
    String unit;
    int clicki; //This is to keep correct track of what is clicked
    String attributeName;
    ValueChart chart;
    Font font;
    Color color;
    AttributeCell acell;
    
    int getBottom(){
    	return getHeight()>MIN_HEIGHT-8 ? 7 : 2;
    }
    
    public ContGraph(ValueChart ch, ContinuousAttributeDomain dd, double[] it, double[] we, String un, String att, Color col, AttributeCell ac) {
        //Setting variable names
        cdomain = dd;
        chart = ch;
        items = it;
        weights = we;
        unit = un;
        attributeName = att;
        color = col;
        acell = ac;
        
    	font = new Font ("Arial", Font.BOLD, 7);
        setBackground(Color.white);
        p = new MoveablePoint[items.length];
        lines = new Line2D.Float[items.length - 1];
        setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        addMouseListener(this);
        addMouseMotionListener(this);
        addComponentListener(new ResizeHandler());
        //showGraph();  
        plotPoints();
        
    }     
    
    void plotPoints(){
    	weights = cdomain.getWeights();
    	items = cdomain.getKnots();
        int ht = getHeight()-getBottom();
        //Creating all the points of utility
        
        for(int i = 0; i < items.length; i++){
            p[i] = new MoveablePoint(((int)(((items[i]-items[0])/((items[(items.length)-1])-items[0]))*(getWidth()-15)))+5, (int) (ht - (weights[i] * ht)) + TOP);
        }
        repaint();
    }    
    
    private class ResizeHandler extends ComponentAdapter {
        public void componentResized(ComponentEvent e){ 
        	plotPoints();
        }
    }

    public void mouseClicked(MouseEvent me) { 
    	//undo code
    }        
    public void mouseEntered(MouseEvent me) { }
    public void mouseExited(MouseEvent me) { }
    public void mouseMoved(MouseEvent me) { }
    public void mousePressed(MouseEvent me) {
        xaxis = me.getX();
        for (int i = 0; i < items.length; i++) {
            if (p[i].hit(me.getX(), me.getY())) {
                undo = cdomain.getWeights();
                moving = p[i];
                clicki = i;
                chart.last_int.setUndoUtil(this, null, items[i], weights[i], cdomain);    
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
        if(me.getY() <= getHeight()-getBottom()+TOP && me.getY() >= TOP){
            movePoint(xaxis, me.getY());
        }
        else{
            if(me.getY() > getHeight()-getBottom()+TOP){
                movePoint(xaxis, getHeight()-getBottom()+TOP);
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
        for(int i = 0; i < (items.length - 1); i++){
            lines[i].setLine(p[i].x, p[i].y, p[i+1].x, p[i+1].y);
        }
        repaint();
        
        //Update Value Chart
        //cdomain.removeKnot(items[clicki]);
        cdomain.changeWeight(items[clicki],(float)((getHeight()-getBottom())-y)  / (getHeight()-getBottom()-TOP));
        chart.updateAll();
        //Updating weights
        weights = cdomain.getWeights();     
        items = cdomain.getKnots();
    }

    public void paintComponent(Graphics gfx) {
        super.paintComponent(gfx);
        Graphics2D g = (Graphics2D) gfx;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
        g.setFont(font);
        
        //g.setColor(Color.gray);
        //g.drawRect (0,0,getWidth(), getHeight());
        
        //Joining all the points into lines
        for(int i = 0; i < (items.length - 1); i++){
            lines[i] = new Line2D.Float(p[i], p[i+1]);
        }
        
        //g.setPaint(Color.blue);     
        //Draw all the lines
        for(int i = 0; i < (items.length - 1); i++){
            g.draw(lines[i]);
        }

        Shape[] s;
        s = new Shape[items.length];
        String temp;
        g.setColor(Color.DARK_GRAY);
        for(int i = 0; i < items.length; i++){
            temp = String.valueOf(weights[i]);
            g.setFont(new Font ("Arial", Font.BOLD, 6));
            if (getHeight() > 20){
            	g.drawString(temp.substring(0,3), (p[i].x + 5), p[i].y < getHeight() - BOTTOM - 3 ? p[i].y + 5 : p[i].y);
            }
            s[i] = p[i].getShape();
        }
        
        g.setColor(color);      
        //g.fill(s0);  g.fill(s1);
        for(int i = 0; i < items.length; i++){
            g.fill(s[i]);
        }
        g.setColor(Color.black);    
        // g.draw(s0);  g.draw(s1);
        //for(int i = 0; i < items.length; i++){
        //    g.draw(s[i]);
        //}
        //Draw the Line axis
        g.drawLine(5, 0, 5, getHeight()-getBottom()); //y-axis
        g.drawLine(5, getHeight()-getBottom(), getWidth() - 5, getHeight()-getBottom()); //x-axis
        
        g.setFont(font);
        //Draw the static labels    
        if (getHeight() > MIN_HEIGHT - 5){
        	String utility_upper_bound = new String("1");
        	g.drawString(utility_upper_bound, 1, 5);
        	String utility_lower_bound = new String("0");
        	g.drawString(utility_lower_bound, 1, getHeight()-getBottom());
        }
        
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
				  RenderingHints.VALUE_TEXT_ANTIALIAS_ON);  
    	
        if (getHeight() > MIN_HEIGHT - 8){
	        for(int i = 0; i < items.length; i++){
	        	DecimalFormat df = acell.obj.decimalFormat;    
	        	String num = df.format(items[i]);
	        	g.drawString(num, ((int)(((items[i]-items[0])/((items[(items.length)-1])-items[0]))*(getWidth()-15)))+5 - (num.length()*2), getHeight()-1);
	        }
        }
    }
  
    class MoveablePoint extends Point2D.Float {
    	int r = getHeight() > MIN_HEIGHT ? 3 : 1;
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





