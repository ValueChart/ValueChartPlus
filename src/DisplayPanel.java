
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.*;
import java.util.*;
import javax.swing.event.MouseInputAdapter;

//This class is the overall ranking panel. The most important function in this
//class is the paint function. All the rankings are done through paint.
//If you are rotating the interface 90 degrees, you need to change the paint function
//of x to y.
//See below comments
public class DisplayPanel extends JComponent {

    private static final long serialVersionUID = 1L;
    boolean score = false;
    boolean ruler = false;
    private TablePane rootPane;
    private int colWidth = ValueChart.DEFAULT_COL_WIDTH;
    private Vector entryList;
    
    JPopupMenu domainMeta = new JPopupMenu();
    

    public DisplayPanel(int col) {
        colWidth = col;
        MouseHandler mouseHandler = new MouseHandler();
        addMouseListener(mouseHandler);
    }

    public int getColWidth() {
        return colWidth;
    }

    /*	public void setColWidth (int w){ 
     colWidth = w;
     }
     */
    public void setRootPane(TablePane pane) {
        rootPane = pane;
    }

    public void setEntries(Vector list) {
        entryList = list;
        Dimension dim = getPreferredSize();

        dim.width = colWidth * entryList.size();
        setPreferredSize(dim);
        setMaximumSize(new Dimension(colWidth * entryList.size() + colWidth * 2, 10000));
    }

    public Dimension getPreferredSize() {
        return new Dimension(colWidth * entryList.size() + colWidth * 2, getHeight());
    }

    public void setScore(boolean s) {
        score = s;
    }

    public void setRuler(boolean r) {
        ruler = r;
    }

    public void paint(Graphics g) {
        int numEntries = entryList.size();
        int totalWidth = getWidth();
        int totalHeight = getHeight();
        g.setColor(Color.white);
        int align_right = 0;//totalWidth - numEntries*colWidth;//
        g.fillRect(align_right + colWidth, 0, numEntries * colWidth, totalHeight);//
        if (rootPane == null) {
            return;
        }

        Vector cellList = new Vector(16);
        rootPane.getAttributeCells(cellList);
        double[] weights;	//array of entry weights (values)
        double[] accumulatedRatios = new double[numEntries];
        int[] ypos = new int[numEntries];	//position of x, starts all at 0
        //for each primobj
        for (Iterator it = cellList.iterator(); it.hasNext();) {
            //for each objective, get the (array)domain value of each alternative
            AttributeCell cell = (AttributeCell) it.next();
            weights = cell.getWeights();
            //absolute weight
            double or = cell.getOverallRatio();
            int h, x = align_right + colWidth;
            //for each entry, paint the said attribute's value
            for (int i = 0; i < numEntries; i++) {
                accumulatedRatios[i] += or * weights[i];
                h = (int) Math.round(accumulatedRatios[i] * totalHeight) - ypos[i];
                ChartEntry entry = (ChartEntry) entryList.get(i);
                if (entry.isMasked()) {
                    g.setColor(Color.lightGray);
                } else {
                    g.setColor(cell.getColor());
                }

                g.fillRect(x, getHeight() - h - ypos[i], colWidth, h);
                g.setColor(Color.lightGray);
                g.drawLine(x, getHeight() - h - ypos[i], x + colWidth - 1, getHeight() - h - ypos[i]);
                x += colWidth;
                ypos[i] += h;
                if (x > totalWidth) {
                    break;
                }
            }
        }
        int y = colWidth + (colWidth / 4);

        //scores
        if (score) {
            for (int j = 0; j < numEntries; j++) {
                g.setColor(Color.darkGray);
                g.drawString(String.valueOf((Math.round(accumulatedRatios[j] * 100))), y, totalHeight - ypos[j] - 5);
                y += colWidth;
            }
        }

        g.setColor(Color.lightGray);
        y = align_right + colWidth;	//temp, was only colWidth
        for (int i = 1; i < numEntries + 1; i++) {
            g.drawLine(y, 0, y, totalHeight - 1);
            y += colWidth;
        }

        //ruler
        if (ruler) {
            g.setColor(Color.gray);
            g.drawRect(1, 0, colWidth - 2, totalHeight - 1);

            for (int i = 1; i < 10; i++) {
                g.drawLine(colWidth - 5, i * totalHeight / 10, colWidth - 2, i * totalHeight / 10);
                g.setFont(new Font("Verdana", Font.PLAIN, 8));
                g.setColor(Color.darkGray);
                g.drawString(String.valueOf((10 - i) * 10), colWidth - 12, i * totalHeight / 10 + 3);
            }
        }
        boolean gridline = false;
        if (gridline) {
            g.setColor(Color.lightGray);
            for (int i = 1; i < 10; i++) {
                g.drawLine(i * totalWidth / 10, colWidth - 2, i * totalWidth / 10, totalHeight);
            }
        }
    }

    private class MouseHandler extends MouseInputAdapter {

        private final int OFF = 0;
        private final int NEARBY = 1;
        private final int MOVING = 2;
        private int dragMode = OFF;
        private int dragY;//-
        private int thresholdPos = 0;
        Cursor movingCursor = new Cursor(Cursor.MOVE_CURSOR);
        Cursor defaultCursor = null;
        boolean nearThreshold = false;

        @Override
        public void mouseExited(MouseEvent e) {
            //do something, like get rid of the popup
            int blah = 1;
        }

        @Override
        public void mousePressed(MouseEvent e) {

            if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) != 0) {
                //If the context menu is up and now the user clicks away from the popup menu, then hide the popup
                /*
                 JPopupMenu popup = getEntryPopup(); //This needs to be a static number in order for the loop to work
                 if (popup.isVisible()) {
                 popup.setVisible(false);

                 while (popup.getComponents().length > 1) {
                 popup.remove(popup.getComponents().length - 1);
                 //remove all the context menu items, except for the default domain value
                 }
                 }*/
            } else if ((e.getModifiers() & MouseEvent.BUTTON3_MASK) != 0) {
                
                domainMeta.removeAll();
                JMenuItem item = new JMenuItem("Test");
                item.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent b){
                            System.out.println("Test Clicked");
                        }
                    });
                //domainMeta.add(item);
                //domainMeta.show(e.getComponent(), e.getX() + 5, e.getY() + 5);
                /*    

                 JMenuItem detailMenuItem = new JMenuItem("Report Details");
                 detailMenuItem.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent b){
                 System.out.print("Report Clicked");
                 }
                 });
                 attributeMeta.add(detailMenuItem);
                 attributeMeta.setSelected(detailMenuItem);
                    
                 detailMenuItem = new JMenuItem("Images");
                 detailMenuItem.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent b){
                 System.out.print("Images Clicked");
                 }
                 });
                 attributeMeta.add(detailMenuItem);
                 Point loc = AttributeCell.this.getLocationOnScreen();
                 attributeMeta.setLocation(loc.x + (index + 1) * colWidth - 5, loc.y + e.getY());
                 attributeMeta.setVisible(true);
                 }
                 * */
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) != 0) {
                //do something                        
            } else if ((e.getModifiers() & MouseEvent.BUTTON3_MASK) != 0) {
                /*                    if (attributeMeta.isVisible()) {
                 attributeMeta.removeAll();
                 }
                 */
            }
        }
    }
}