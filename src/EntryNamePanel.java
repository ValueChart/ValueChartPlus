
import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.util.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import org.icepdf.core.pobjects.OutlineItem;
import org.icepdf.core.pobjects.Outlines;
import org.icepdf.ri.common.OutlineItemTreeNode;
import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.views.DocumentViewControllerImpl;

//This class deals with displaying name of entry's.
//Like it will display House 1, House 2, House 3 etc..
public class EntryNamePanel extends JPanel implements ActionListener {

    private static final long serialVersionUID = 1L;
    public static Font LABELFONT = new Font("Verdana", Font.BOLD, 16);
    private static final int ANG_WD = 50;
    static final int ANG_HT = 50;
    Vector<String> labelList;
    int panelHeight = 0;
    int colWidth = 0;
    int numTopBlanks = 0;
    boolean main = false;
    MouseHandler mh = new MouseHandler();
    JPopupMenu popEntry;
    ValueChart chart;
    JLabel selected_entry;

    public EntryNamePanel(Vector<ChartEntry> entryList, int colWidth, int numTopBlanks, boolean main, ValueChart chart) {
        labelList = new Vector<String>();

        int maxheight = 0;
        addMouseListener(mh);
        addMouseMotionListener(mh);

        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(Box.createHorizontalGlue());

        for (Iterator<ChartEntry> it = entryList.iterator(); it.hasNext();) {
            ChartEntry entry = it.next();
            labelList.add(entry.name);
        }

        setBorder(BorderFactory.createLineBorder(getBackground()));

        panelHeight = maxheight;
        this.setPreferredSize(new Dimension(labelList.size() * colWidth + ANG_WD, ANG_HT + 3));
        this.setMaximumSize(new Dimension(labelList.size() * colWidth + ANG_WD, ANG_HT + 3));
        this.setMinimumSize(new Dimension(labelList.size() * colWidth + ANG_WD, ANG_HT + 3));
        this.colWidth = colWidth;
        //setAlignmentY (JComponent.TOP_ALIGNMENT);
        this.numTopBlanks = numTopBlanks;
        this.main = main;
        this.chart = chart;

        createPopup();

    }
    int mousedOver = -1;

    public void paintComponent(Graphics g) {
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.BLACK);
        g.setFont(LABELFONT);
        for (int i = 0; i < labelList.size(); ++i) {
            if (i == mousedOver) {
                g.setColor(Color.GRAY);
            } else {
                g.setColor(Color.BLACK);
            }
            g.drawString("(" + (i+1) + ")", ((i+1) * colWidth) + ((int) (colWidth) / 2) - LABELFONT.getSize(), LABELFONT.getSize());
            g.setColor(Color.GRAY);
            if (i != 0)
                g.drawLine(((i+1) * colWidth)-1, LABELFONT.getSize(), ((i+1) * colWidth)-1, 0);
            if (chart.getEntryList().get(i).getIsMarked()) {
                g.setColor(Color.blue);
                //code a polygon or something
                g.drawString(labelList.get(i).toString(), (i * colWidth) + 21, ANG_HT - 1);
            }
        }
        g.setColor(Color.BLACK);
        g.drawString("Alternatives", (labelList.size() * colWidth)/2, ANG_HT-10);
    }

    void createPopup() {
        popEntry = new JPopupMenu();
        JMenuItem menuItem = new JMenuItem("Mark");
        menuItem.addActionListener(this);
        popEntry.add(menuItem);
        menuItem = new JMenuItem("Edit");
        menuItem.addActionListener(this);
        popEntry.add(menuItem);
        popEntry.addSeparator();
        menuItem = new JMenuItem("Show Details");
        menuItem.addActionListener(this);
        popEntry.add(menuItem);
        //***This adds access to the report
        menuItem = new JMenuItem("Show Report");
        menuItem.addActionListener(this);
        popEntry.add(menuItem);

    }

    public void relabel(Vector<ChartEntry> entryList) {
        labelList.clear();

        for (Iterator<ChartEntry> ei = entryList.iterator(); ei.hasNext();) {
            ChartEntry entry = ei.next();
            labelList.add(entry.name);
        }

        repaint();
    }

    public int getDesiredHeight() {
        return panelHeight;
    }

    void updateBoth() {
        chart.mainEntryNames.repaint();
        chart.displayEntryNames.repaint();
    }
    
    public void zoomToReport(JFrame window, SwingController controller, String entryName) {

        //This method navigates to the correct location in the report based on the attribute

        //Since the window is initially hidden, or when the user closes the window is become hidden (not closed), we need to check this first
        //But only show the window if the request has been such
        if (!window.isVisible()) {
            window.setVisible(true);
        }

        //From: http://www.icesoft.org/JForum/posts/list/13433.page
        //Get the bookmarks (icepdf calls them outline items)
        OutlineItem item = null;
        Outlines outlines = controller.getDocument().getCatalog().getOutlines();
        if (outlines != null) {
            item = outlines.getRootOutlineItem();
        }
        if (item != null) {
            OutlineItemTreeNode outlineItemTreeNode = new OutlineItemTreeNode(item);
            outlineItemTreeNode.getChildCount();  // Added this line
            Enumeration depthFirst = outlineItemTreeNode.depthFirstEnumeration();
            // find the node you need
        }

        //Loop through the bookmarks looking for a matching title

        //The index of the bookmark that we want to navigate to
        Integer bookMark = 0;
        Integer numDocBookmarks = item.getSubItemCount();
        String bookMarkName; //this is to store the name of the item, from outlines, which are the bookmarks that the computer sees in the PDF document

        //Bookmarks in the PDF tend to have spaces added onto the end of the name, so that is why the replace and trim methods are used
        while (bookMark < numDocBookmarks) {

            bookMarkName = item.getSubItem(bookMark).getTitle().replace("?", "").trim();

            System.out.println("|" + bookMarkName + "/" + entryName + "|");
            if (
                    (bookMarkName.equals(entryName)) ||
                    (bookMarkName.equals(entryName + " ")) ||
                    (bookMarkName.equals(entryName + "  ")) ||
                    (bookMarkName.equals(" " + entryName)) ||
                    (bookMarkName.equals(" " + entryName + " ")) ||
                    (bookMarkName.equals(" " + entryName + "  "))
                    ){
                break;
            }
            bookMark++;
            //TODO: Return if bookmark not found
        }
        
        //Set continuous view
        controller.setPageFitMode(org.icepdf.core.views.DocumentViewController.PAGE_FIT_WINDOW_WIDTH, false);
        controller.setPageViewMode(DocumentViewControllerImpl.ONE_COLUMN_VIEW, true);
        
        if (bookMark >= numDocBookmarks) {
            //System.out.println("There is no bookmark in the document for the given attribute");
            controller.showPage(0); //zoom to the opening page in the PDF
        } else {
            //go to the location
            controller.followOutlineItem(item.getSubItem(bookMark));
        }

    }
    
    public void actionPerformed(ActionEvent ae) {
        //listener for popup menu
        if ("Mark".equals(ae.getActionCommand()) || "Unmark".equals(ae.getActionCommand())) {
            if ("Mark".equals(ae.getActionCommand())) {
                chart.getEntryList().get(rightClicked).setIsMarked(true);
            } else {
                chart.getEntryList().get(rightClicked).setIsMarked(false);
            }
            updateBoth();
            /*			if (chart.mainEntryNames.isAncestorOf(selected_entry))
             markMatchingLabel(chart.displayEntryNames, col);
             else			
             markMatchingLabel(chart.mainEntryNames, col);		*/
        } else if ("Show Details".equals(ae.getActionCommand()) || "Hide Details".equals(ae.getActionCommand())) {
            if ("Show Details".equals(ae.getActionCommand())) {
                toggleDetails(true);
            } else {
                toggleDetails(false);
            }
        } else if ("Edit".equals(ae.getActionCommand())) {
            chart.showConstructionModel();
        } else if ("Show Report".equals(ae.getActionCommand())) {
            //This menu item opens one pdf report specific for this entry for the given attribute
            System.out.println("Report Clicked");
            
            //Get the current entry
            ChartEntry tempentry = chart.getEntryList().get(rightClicked); //get the current entry
            
            if (tempentry.getReportFrame() != null) { //check if the entry has an associated report
                //The JFrame and SwingController inputs were created in ValueChart.java when the initial data was loaded
                //Since the window is initially hidden, or when the user closes the window is become hidden (not closed), we need to check this first
                //But only show the window if the request has been such
                JFrame window = tempentry.getReportFrame(); //get the window associated with the chart
                SwingController controller = tempentry.getReportController(); //get the controller associated with the window
                
                zoomToReport(window, controller, tempentry.name.toString()); //if there is a bookmark with the name of this entry, go to it, otherwise it will take you to the first page of the report
            } else {
                System.out.println("There is no associated report for scenario/entry ");
            }
        }
    }
    /**/

    void markMatchingLabel(EntryNamePanel enp, Color color) {
        for (int i = 0; i < enp.getComponentCount(); i++) {
            JLabel labi = (JLabel) enp.getComponent(i);
            if (labi.getText().equals(selected_entry.getText())) {
                JLabel lab = (JLabel) enp.getComponent(i);
                lab.setBackground(color);
                break;
            }

        }
    }

    void toggleDetails(boolean show) {
        //offset fom extra label added for ruler
        chart.getEntryList().get(rightClicked).setShowFlag(show);
        chart.updateAll();
    }

    /**/
    public Dimension getPreferredSize() {
        return new Dimension(labelList.size() * colWidth + ANG_WD, ANG_HT + 3);
    }
    
    int rightClicked = -1;
    int idx = -1;

    class MouseHandler extends MouseInputAdapter {
        public void mousePressed(MouseEvent me) {
            //(chart.entryList.get(idx)).setShowFlag(true);	
            if (idx != -1) {
                chart.showDomainVals(idx);
            } else {
                chart.showAlternativeLegend();
            }
            //System.out.println("IDX___________ " + idx);
            chart.updateAll();
            if (SwingUtilities.isRightMouseButton(me)) {
                if (chart.getEntryList().get(idx).getIsMarked()) {
                    ((JMenuItem) popEntry.getComponent(0)).setText("Unmark");
                } else {
                    ((JMenuItem) popEntry.getComponent(0)).setText("Mark");
                }
                if (idx != -1) {
                    if (chart.getEntryList().get(idx).getShowFlag()) {
                        ((JMenuItem) popEntry.getComponent(3)).setText("Hide Details");
                    } else {
                        ((JMenuItem) popEntry.getComponent(3)).setText("Show Details");
                    }
                }
                rightClicked = idx;
                popEntry.show(me.getComponent(), me.getX() - 1, me.getY() - 1);

            }
        }

        public void mouseMoved(MouseEvent me) {
            int inc = ANG_WD / 4;
            int y = getHeight() / 4;
            for (int i = 0; i < labelList.size(); i++) {
                if (me.getX() > ((i+1)*colWidth) && me.getX() < ((i+1)*colWidth) + colWidth) {
                    idx = i;
                    
                    EntryNamePanel.this.setToolTipText("<html><blockquote><left><font size=\"4\">" + labelList.get(i).toString().replace('_', ' ') + "</left></blockquote></html>");
                    
                    break;
                }
                //no sel
                idx = -1;
            }
            mousedOver = idx;

            if (me.getY() < 3 || me.getY() > getHeight() - 3) {
                mousedOver = -1;
            }
            chart.selectAlternativeLegend(mousedOver);

            repaint();
        }

        public void mouseExited(MouseEvent me) {
            mousedOver = -1;
            idx = -1;
            repaint();
        }
    }
}
