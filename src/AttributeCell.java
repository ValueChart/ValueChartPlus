
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;
import javax.swing.*;
import javax.swing.event.*;
import java.text.DecimalFormat;
import java.util.*;
import javax.swing.JPanel;
import org.icepdf.core.pobjects.OutlineItem;
import org.icepdf.ri.common.ComponentKeyBinding;
import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.SwingViewBuilder;
import org.icepdf.ri.common.views.DocumentViewControllerImpl;


public class AttributeCell extends JComponent {

    private static final long serialVersionUID = 1L;
    static public final int DEFAULT_HEIGHT = 64;
    private Vector<ChartEntry> entryList;
    private String attributeName;
    private int colWidth = ValueChart.DEFAULT_COL_WIDTH;
    private Color color = Color.red;
    private ValueChart chart;
    private String units;
    private double threshold;
    JPopupMenu domainPopup;
    JPopupMenu entryPopup;
    JMenuItem entryPopupMenuItem;
    ContGraph cg;
    DiscGraph dg;
    JObjective obj;
    //***Part of the new submenu feature
    JPopupMenu attributeMeta = new JPopupMenu(); //***Used for the new popup menu to display images, reports (PDF), etc.
    SwingController[] controller; //Used for each window that controls a pdf document
    JFrame[] window; //the windows created for the attribute, these are used to show the report. They are class variables because we need to toggle visibility from different methods
    JPanel[] viewerComponentPanel; //the panel for which the fram sits on;

    public AttributeCell(ValueChart chart, String attrName) {
        MouseHandler mouseHandler = new MouseHandler();
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
        //addComponentListener(new ResizeHandler());
        this.chart = chart;
        this.attributeName = attrName;
    }

    //temp
    public String getName() {
        return attributeName;
    }

    public void setName(String name) {
        attributeName = name;
    }

    public double getOverallRatio() { // return overallRatio;
        Container container = getParent();
        double ratio = 1.0;
        while (container instanceof BaseTableContainer) {
            ratio *= ((BaseTableContainer) container).getHeightRatio();//-
            container = container.getParent().getParent();
        }
        return ratio;
    }

    public void setOverallRatio(double r) {
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color c) {
        color = c;
        float[] rgb = c.getRGBColorComponents(null);
        new Color(rgb[0] / 3, rgb[1] / 3, rgb[2] / 3);
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String name) {
        units = name;
    }

    //-sets the entry list for the cell
    public void setEntryList(String name, Vector<ChartEntry> list) {
        attributeName = name;
        entryList = list;
        Dimension dim =
                new Dimension(list.size() * colWidth, DEFAULT_HEIGHT);//- & rev xy
        setPreferredSize(dim);
        setSize(dim);
        dim.height = 1000000;//-w->h
        setMaximumSize(dim);
    }

    public void setSize(int w, int h) {
        super.setSize(w, h);
    }

    public int numValues() {
        return entryList.size();
    }

    //used by DisplayPanel to get a list of each entry's value
    public double[] getWeights() {
        double[] weights = new double[entryList.size()];
        int i = 0;
        for (Iterator<ChartEntry> it = entryList.iterator(); it.hasNext();) {
            AttributeValue value = it.next().attributeValue(attributeName);
            if (value != null) {
                weights[i] = value.weight();
            } else {
                weights[i] = 0;
            }
            i++;
        }
        return weights;
    }

    public JPopupMenu getDomainPopup() {
        if (domainPopup == null) {
            makeHelpPopups(getDomain());
        }
        return domainPopup;
    }

    public JPopupMenu getEntryPopup() {
        if (entryPopup == null) {
            makeHelpPopups(getDomain());
        }
        return entryPopup;
    }

    private void makeHelpPopups(AttributeDomain domain) {
        domainPopup = new JPopupMenu();
        String best = "";
        String worst = "";
        DecimalFormat df = obj.decimalFormat;
        if (domain.getType() == AttributeDomainType.DISCRETE) {
            DiscreteAttributeDomain dd = domain.getDiscrete();
            String elt[] = dd.getElements();
            double wt[] = dd.getWeights();
            for (int j = 0; j < wt.length; j++) {
                if (wt[j] == 0.0) {
                    worst = elt[j];
                }
                if (wt[j] == 1.0) {
                    best = elt[j];
                }
            }
        } else {
            ContinuousAttributeDomain cd = domain.getContinuous();
            double kt[] = cd.getKnots();
            double wt[] = cd.getWeights();
            for (int j = 0; j < wt.length; j++) {
                if (wt[j] == 0.0) {
                    worst = df.format(kt[j]) + " " + units;
                }
                if (wt[j] == 1.0) {
                    best = df.format(kt[j]) + " " + units;
                }
            }
        }
        /*        if (domain.getType() == AttributeDomain.DISCRETE){ 
         DiscreteAttributeDomain dd = (DiscreteAttributeDomain)domain;
         String[] elements = dd.getElements();
         String msg = null;
         for (int i=0; i<elements.length; i++){ 
         msg = elements[i] + " " + dd.weight(elements[i]);
         domainPopup.add(msg);
         }
         }
         else{ 
         ContinuousAttributeDomain cd = (ContinuousAttributeDomain)domain;
         String rangeMsg = "[" + cd.getMin() + ", " + cd.getMax() + "]";
         if (units != null)
         rangeMsg += " " + units;
          
         rangeMsg += " -> [" + cd.weight(cd.getMin()) + ", " +
         cd.weight(cd.getMax()) + "]";
         domainPopup.add(rangeMsg);
         }*/
        String msg = "";
        msg = "BEST: " + best;
        domainPopup.add(msg);
        msg = "WORST: " + worst;
        domainPopup.add(msg);
        //domainPopup.setSelectionModel(new NullSelectionModel());

        entryPopup = new JPopupMenu();
        entryPopupMenuItem = new JMenuItem();
        entryPopup.add(entryPopupMenuItem);
        entryPopup.setSelected(entryPopupMenuItem);
        //entryPopup.setSelectionModel(new NullSelectionModel());

    }

    //This is added to display utility graph
    public void getUtility(AttributeDomain domain) {
        if (domain.getType() == AttributeDomainType.DISCRETE) {
            DiscreteAttributeDomain dd = domain.getDiscrete();
            new DiscreteUtilityGraph(chart, true, dd, dd.getElements(), dd.getWeights(), attributeName, null, this);

        } else {
            ContinuousAttributeDomain cd = domain.getContinuous();
            new ContinuousUtilityGraph(chart, true, cd, cd.getKnots(), cd.getWeights(), getUnits(), attributeName, null, this);
        }
    }

    //This will make function call to get utility graph
    public void makeUtility(AttributeDomain domain) {
        if (domain.getType() == AttributeDomainType.DISCRETE) {
            DiscreteAttributeDomain dd = domain.getDiscrete();
            new DiscreteUtilityGraph(chart, true, dd, dd.getElements(), dd.getWeights(), attributeName, null, this);

        } else {
            ContinuousAttributeDomain cd = domain.getContinuous();
            new ContinuousUtilityGraph(chart, true, cd, cd.getKnots(), cd.getWeights(), getUnits(), attributeName, null, this);
        }
    }

    public ContGraph makeContGraph(AttributeDomain domain) {
        ContinuousAttributeDomain cd = domain.getContinuous();
        cg = new ContGraph(chart, cd, cd.getKnots(), cd.getWeights(), getUnits(), attributeName, color, this);
        return cg;
    }

    public DiscGraph makeDiscGraph(AttributeDomain domain) {
        DiscreteAttributeDomain dd = domain.getDiscrete();
        dg = new DiscGraph(chart, dd, dd.getElements(), dd.getWeights(), attributeName, color);
        return dg;
    }

    AttributeDomain getDomain() {
        return getData().getDomain();
    }

    public AttributePrimitiveData getData() {
        return chart.getAttribute(attributeName).getPrimitive();
    }

    public int getColWidth() {
        return colWidth;//-
    }

    public void setColWidth(int w) {
        colWidth = w;
    }
//REPORT AND IMAGE SUBMENU AND WINDOW MANAGEMENT


    //zoom the the location in the report
    public void zoomToReport(JFrame window, SwingController controller, OutlineItem item, int bookmarkIndex, Boolean showAll) {
        //This method navigates to the correct location in the report based on the attribute

        //Since the window is initially hidden, or when the user closes the window is become hidden (not closed), we need to check this first
        //But only show the window if the request has been such
        if (showAll == true && !window.isVisible()) {
            window.setVisible(true);
        }

        //go the the location in the document
        controller.setPageFitMode(org.icepdf.core.views.DocumentViewController.PAGE_FIT_WINDOW_WIDTH, false);
        controller.setPageViewMode(DocumentViewControllerImpl.ONE_COLUMN_VIEW, true);
        controller.followOutlineItem(item.getSubItem(bookmarkIndex));

    }

    public Integer criteriaBookmarkExistsInReport(OutlineItem item, String entryName, String bookmarkTitle) {

        //This method searches through the outline of a report to identify if a bookmark item exists

        if (item != null) {

            //The index of the bookmark that we want to navigate to
            Integer bookMark = 0;
            Integer numDocBookmarks = item.getSubItemCount();
            String bookMarkName; //this is to store the name of the item, from outlines, which are the bookmarks that the computer sees in the PDF document

            //Bookmarks in the PDF tend to have spaces added onto the end of the name, so that is why the replace and trim methods are used
            while (bookMark < numDocBookmarks) {
                bookMarkName = item.getSubItem(bookMark).getTitle().replace("?", "").trim();
                if ((bookMarkName.equals(entryName + ":" + bookmarkTitle.toString()))
                        || (bookMarkName.equals(entryName + ": " + bookmarkTitle.toString()))
                        || (bookMarkName.equals(entryName + ":  " + bookmarkTitle.toString()))
                        || (bookMarkName.equals(entryName + " :  " + bookmarkTitle.toString())
                        || (bookMarkName.equals(entryName + " : " + bookmarkTitle.toString()))
                        || (bookMarkName.equals(entryName + " :" + bookmarkTitle.toString())))
                        || (bookMarkName.equals(entryName + ":" + bookmarkTitle.toString().replace('_', ' ')))
                        || (bookMarkName.equals(entryName + ": " + bookmarkTitle.toString().replace('_', ' ')))
                        || (bookMarkName.equals(entryName + ":  " + bookmarkTitle.toString().replace('_', ' ')))
                        || (bookMarkName.equals(entryName + " :  " + bookmarkTitle.toString().replace('_', ' '))
                        || (bookMarkName.equals(entryName + " : " + bookmarkTitle.toString().replace('_', ' ')))
                        || (bookMarkName.equals(entryName + " :" + bookmarkTitle.toString().replace('_', ' '))))) {
                    break;
                }
                bookMark++;
                //TODO: Return if bookmark not found
            }

            if (bookMark >= numDocBookmarks) {
                //System.out.println("There is no bookmark in the document for the given attribute");
                return -1;
            } else {
                //go to the location
                //controller.followOutlineItem(item.getSubItem(bookMark));
                return bookMark;
            }
        } else {
            return -1;
        }
    }
    
    // http://www.splitbrain.org/blog/2008-09/18-calculating_color_contrast_with_php
    public static boolean useBlackForeground(Color background) {
        int black = 0;
        int r = background.getRed();
        int g = background.getGreen();
        int b = background.getBlue();
        
        // brightness
        int bright = (299 * r + 587 * g + 114 * b) / 1000;
        black += (bright >= 125 ? 1 : 0);
        
        // luminosity
        double lumo = 0.2126 * Math.pow(r/255, 2.2) +
                0.7152 * Math.pow(g/255, 2.2) +
                0.0722 * Math.pow(b/255, 2.2);
        black += (lumo >= 5 ? 1 : 0);
        
        return (black >= 1);
    }

    public void paint(Graphics g) {
        int width = getWidth();
        int height = getHeight();
        g.setColor(Color.white);
        g.fillRect(0, 0, width, height);
        g.setColor(Color.gray);
        g.drawLine(0, getHeight() - 1, width, getHeight() - 1);
        int x = 0;
        int thresholdPos = (int) (height - ((height) * threshold));

        for (Iterator<ChartEntry> it = entryList.iterator(); it.hasNext();) {
            ChartEntry entry = it.next();
            AttributeValue value = entry.attributeValue(attributeName);
            int h = 0;
            int hpos = height;
            if (value != null) {
                h = (int) (value.weight() * height);
                hpos = height - h;
            }
            g.setColor(color);
            int wthresh = Math.max(hpos, thresholdPos);
            if (wthresh < height) {
                g.setColor(Color.lightGray);
                g.fillRect(x, wthresh, colWidth, h);
            }
            if (wthresh > hpos) {
                g.setColor(color);
                g.fillRect(x, hpos, colWidth, wthresh - hpos);
            }

            g.setColor(Color.WHITE);
            g.fillRect(x, 0, colWidth, (int) ((1 - value.weight()) * height));

            Font f = new Font("Verdana", Font.BOLD, 10);
            g.setFont(f);

            try {
                if (entry.getShowFlag()) {
                    // completely grey background
                    if (wthresh < height - 10) {
                        g.setColor(Color.darkGray);
                    } else {
                        boolean black = useBlackForeground(getData().getColor());
                        // completely coloured background
                        if (hpos < height - 10){
                            g.setColor((black ? Color.BLACK : Color.WHITE));
                            // partially white, partially grey background
                        } else if ( (wthresh < height - 7) ) {
                            g.setColor(Color.darkGray);
                            // partially white, partially dark coloured background
                        } else if ( ((hpos < height - 7) && !black) ) {
                            g.setColor(Color.darkGray);
                            // mostly white background
                        } else {
                            g.setColor(Color.BLACK);
                        }
                    }
                	g.drawString(value.stringValue(), x+2, height-5);                    
                }
            } catch (java.lang.NullPointerException ne) {
            }


            // vertical grid line
            g.setColor(ValueChart.gridColor);
            x += colWidth;
            g.drawLine(x - 1, 0, x - 1, height - 1);

            if (x > width) {
                break;
            }
        }

        g.setColor(Color.lightGray);
        g.drawLine(0, height - 1, width - 1, height - 1);
        if (thresholdPos > 0) {
            g.setColor(Color.darkGray);
            g.drawLine(0, thresholdPos, width - 1, thresholdPos);
        }
    }
    /* 
     private class NullSelectionModel implements SingleSelectionModel {
     public void addChangeListener(ChangeListener listener) {
     }
        
     //***This is necessary to enable the right-click menu
     public void clearSelection() {
     }
        
     //***This is necessary to enable the right-click menu
     public int getSelectedIndex(){ 
     return -1;
     }
        
     public boolean isSelected(){
     return false;
     }
        
     public void removeChangeListener(ChangeListener listener) {
     }
        
     public void setSelectedIndex(int index) {
     }
     }    
     */

    private class ResizeHandler extends ComponentAdapter {

        public void componentResized(ComponentEvent e) {
            getHeight();
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

        public void mouseDragged(MouseEvent e) {
            int newY = e.getY() + getLocation().y;
            int delY = newY - dragY;
            dragY = newY;
            int height = getHeight();

            if (dragMode == MOVING && height >= 1) {
                thresholdPos += delY;
                if (thresholdPos <= 0) {
                    thresholdPos = 0;
                } else if (thresholdPos > height) {
                    thresholdPos = height;
                }
                threshold = (height - thresholdPos) / (double) (height);//-
                boolean redisplay = false;
                for (Iterator<ChartEntry> it = entryList.iterator(); it.hasNext();) {
                    ChartEntry entry = it.next();
                    AttributeValue value = entry.attributeValue(attributeName);
                    double h = (value != null ? value.weight() : 0);
                    if (threshold <= 0) {
                        if (threshold >= h && !entry.isMasked() && threshold > 0.02) {//added to fix bug0 
                            if (entry.addMaskingAttribute(AttributeCell.this)) {
                                redisplay = true;
                            }
                        } else if (threshold < h && entry.isMasked() || threshold <= 0.02) {//added to fix bug0, in 2013 changed < to <=
                            if (entry.removeMaskingAttribute(AttributeCell.this)) {
                                redisplay = true;
                            }
                        }
                    } else {
                        if (threshold >= h && !entry.isMasked() && threshold >= 1.0 / ((double) height)) {//added 2013 because bug fix above wasn't working for domain values = 0. Because height-1 might be zero, added condition 
                            if (entry.addMaskingAttribute(AttributeCell.this)) {
                                redisplay = true;
                            }
                        } else if (threshold < h && entry.isMasked() || threshold < 1.0 / ((double) height)) {//added 2013 because bug fix above wasn't working for domain values = 0. Because height-1 might be zero, added condition  
                            if (entry.removeMaskingAttribute(AttributeCell.this)) {
                                redisplay = true;
                            }
                        }
                    }
                }
                if (redisplay) {
                    chart.updateDisplay();
                }
                repaint();
            }
        }

        public void mouseMoved(MouseEvent e) {
            int y = e.getY();
            if (defaultCursor == null) {
                defaultCursor = getCursor();
            }
            thresholdPos = (int) (getHeight() - (threshold * (getHeight() - 1)));//-
            if (y >= thresholdPos - 5 && y <= thresholdPos) {
                if (dragMode == OFF) {
                    dragMode = NEARBY;
                    setCursor(movingCursor);
                }
            } else {
                if (dragMode == NEARBY) {
                    dragMode = OFF;
                    setCursor(defaultCursor);
                }
            }
        }

        public void mouseExited(MouseEvent e) {
            if (dragMode == NEARBY) {
                dragMode = OFF;
                setCursor(defaultCursor);
            }
        }

        public void mousePressed(MouseEvent e) {
            if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) != 0) {
                if (dragMode == NEARBY) {
                    dragMode = MOVING;
                }
                dragY = e.getY() + getLocation().y;

            } else if ((e.getModifiers() & MouseEvent.BUTTON3_MASK) != 0) {
                final int index = e.getX() / colWidth;
                if (index >= 0 && index < entryList.size()) {
                    ChartEntry entry =  entryList.get(index);
                    AttributeValue value = entry.attributeValue(attributeName);

                    //***added to retreive the report location from the hash map (data structure)
                    //final File reportFileLocation = (File) entry.map.get("report"); //"report" can be found in ValueChart.java in the method readEntry(...);
                    //final String entryName = entry.name.toString(); //this is the name that will be used as the title for the PDF window

                    String msg = "undefined";
                    if (value != null) {
                        msg = value.stringValue();
                        if (units != null) {
                            msg += " " + units;
                        }
                    }

                    attributeMeta.removeAll();

                    //Add the attribute value to the popup

                    JMenuItem detailMenuItem = new JMenuItem("Value: " + msg);
                    attributeMeta.add(detailMenuItem);

                    //Now check if this attribute exists in the report, if not then don't show the menu items to zoom to details
                    final int bookmarkIndex = criteriaBookmarkExistsInReport(entry.getOutlineItem(), entry.name.toString(), attributeName);

                    //This menu item opens one pdf report specific for this entry for the given attribute
                    if (bookmarkIndex > 0) {
                        attributeMeta.addSeparator();
                        detailMenuItem = new JMenuItem("Report Details");
                        detailMenuItem.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent b) {
                                System.out.print("Report Clicked");
                                ChartEntry tempentry =  entryList.get(index); //get the current entry
                                if (tempentry.getReportFrame() != null) { //check if the entry has an associated report
                                    //The JFram and SwingController inputs were created in ValueChart.java when the initial data was loaded
                                    zoomToReport(tempentry.getReportFrame(), tempentry.getReportController(), tempentry.getOutlineItem(), bookmarkIndex, true); //Go the the bookmark in the PDF (bookmark names should be assocaited with attribute)
                                } else {
                                    System.out.println("There is no associated report for scenario/entry #" + (index + 1));
                                }
                            }
                        });
                        attributeMeta.add(detailMenuItem);

                        //This menu item opens the report details for this attribute, for ALL the entries simultaneously
                        detailMenuItem = new JMenuItem("Compare Open Report Details");
                        detailMenuItem.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent b) {
                                System.out.print("Report Open Clicked");
                                //loop through each entry
                                for (int i = 0; i < entryList.size(); i++) {
                                    ChartEntry tempentry =  entryList.get(i); //get the entry
                                    if (tempentry.getReportFrame() != null) { //check if there is an associated report
                                        //The JFrame and SwingController inputs were created in ValueChart.java when the initial data was loaded
                                        int tempbookmarkIndex = criteriaBookmarkExistsInReport(tempentry.getOutlineItem(), tempentry.name.toString(), attributeName);
                                        if (tempbookmarkIndex >= 0) {
                                        zoomToReport(tempentry.getReportFrame(), tempentry.getReportController(), tempentry.getOutlineItem(), tempbookmarkIndex, false); //Go the the bookmark in the PDF (bookmark names should be assocaited with attribute)
                                        }
                                    } else {
                                        System.out.println("There is no associated report for scenario/entry #" + (index + 1));
                                    }
                                }
                            }
                        });
                        attributeMeta.add(detailMenuItem);

                        //This menu item opens the report details for this attribute, for ALL the entries simultaneously
                        detailMenuItem = new JMenuItem("Compare All Report Details");
                        detailMenuItem.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent b) {
                                System.out.print("Report All Clicked");
                                //loop through each entry
                                for (int i = 0; i < entryList.size(); i++) {
                                    ChartEntry tempentry =  entryList.get(i); //get the entry
                                    if (tempentry.getReportFrame() != null) { //check if there is an associated report
                                        //The JFram and SwingController inputs were created in ValueChart.java when the initial data was loaded
                                        int tempbookmarkIndex = criteriaBookmarkExistsInReport(tempentry.getOutlineItem(), tempentry.name.toString(), attributeName);
                                        if (tempbookmarkIndex >= 0) {
                                            zoomToReport(tempentry.getReportFrame(), tempentry.getReportController(), tempentry.getOutlineItem(), tempbookmarkIndex, true); //Go the the bookmark in the PDF (bookmark names should be assocaited with attribute)
                                        }
                                    } else {
                                        System.out.println("There is no associated report for scenario/entry #" + (index + 1));
                                    }
                                }
                            }
                        });
                        attributeMeta.add(detailMenuItem);
                    }

                    //show the popup menu
                    attributeMeta.show(e.getComponent(), e.getX() + 5, e.getY() + 5);

                }
            }

        }

        public void mouseReleased(MouseEvent e) {
            if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) != 0) {
                if (dragMode == MOVING) {
                    dragMode = OFF;
                    setCursor(defaultCursor);
                }


            }
        }
    }
}
