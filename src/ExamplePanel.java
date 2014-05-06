//gui
import java.awt.*;
import java.awt.geom.*;

import javax.swing.*;
import java.util.*;

public class ExamplePanel extends JPanel{
	private static final long serialVersionUID = 1L;


	public ExamplePanel() {
	setBackground(Color.lightGray);
	labels.add("House1");
	labels.add("House2");
	labels.add("House3");
	labels.add("House4");
	labels.add("House5");
	labels.add("House6");
	labels.add("House1");
	labels.add("House2");
	labels.add("House3");
	labels.add("House4");
	labels.add("House5");
	labels.add("House6");
    }
    public static Font LABELFONT = new Font("Arial", Font.PLAIN, 11);

    public static final AffineTransform NINETY = 
	new AffineTransform(0.0, -1.0, 1.0, 0.0, 0.0, 0.0);

    /// actually not 45 degrees, 180/3 = 60
    public static final AffineTransform FORTYFIVE = 
	AffineTransform.getRotateInstance(-Math.PI / 4.0);

    /// derive a slanted font from the affine transform
    public static Font VLABELFONT = LABELFONT.deriveFont(FORTYFIVE);

    public static Vector labels = new Vector();

    
    public static void main(String argv[]) {
    	JFrame jf = new JFrame("font test");
    	jf.getContentPane().add(new ExamplePanel());
    	jf.pack();
    	jf.setLocation(32, 32);
    	jf.setVisible(true);
    }

    public void paintComponent(Graphics g) {
    	((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
    	        RenderingHints.VALUE_ANTIALIAS_ON);
    	g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());	
        g.setColor(Color.BLACK);
        g.setFont(VLABELFONT);
        for(int i = 0; i < labels.size(); ++i) {        	
        	g.drawString(labels.get(i).toString(), (i*30) + 20, 40);
        	g.drawLine((i*30), 40, (i*30) + 38, 0);
        }
        g.drawLine((labels.size()*30), 40, (labels.size()*30) + 37, 0);
        g.drawLine(38, 0, (labels.size()*30) + 38, 0);
        g.drawLine(0, 40, (labels.size()*30), 40);
    }    

    public Dimension getPreferredSize() {
	return new Dimension(labels.size()*30 + 38, 42);
    }

}
