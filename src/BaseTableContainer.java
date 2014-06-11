
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.text.DecimalFormat;
import java.util.*;


//This class is the draggable panel of objectives. The most interesting method to this class is its mouse handler.
//Pay attention to those few methods, and you can add all kinds of interaction to the interface.

//One of the base containers is the root object, this object is hidden by the width = 0.

public class BaseTableContainer extends Box implements ActionListener {

    private static final long serialVersionUID = 1L;
    public MouseHandler mouseHandler;
    public JComponent table;
    public JLabel header;
    private String name;
    private ValueChart chart;
    private double heightRatio = -1.0;
    private double heightScale = 1.0;
    private double rollUpRatio = 0.0;
    public static final int UP = 1;
    public static final int DOWN = 2;
    private static final int NO_DRAG = 0;
    private static final int EAST_STRETCH_DRAG = 1;
    private static final int WEST_STRETCH_DRAG = 2;
    private static final int WEST_ROLLUP_STRETCH_DRAG = 3;
    private static final int EAST_ROLLUP_STRETCH_DRAG = 4;
    private static final int MOVE_DRAG = 5;
    int dragMode = NO_DRAG;
    private static final int NO_STRETCH = 0;
    private static final int EAST_STRETCH = 1;
    private static final int WEST_STRETCH = 2;
    private static final int WEST_ROLLUP_STRETCH = 3;
    private static final int EAST_ROLLUP_STRETCH = 4;
    private static final int PUMP = 5;		//-added for pump	
    int stretchMode = NO_STRETCH;
    int dragY = 0;
    int dragTopY = 0;
    int componentIndex = -1;
    //-determines position wrt siblings
    BaseTableContainer prevComp = null;
    BaseTableContainer nextComp = null;
    //TablePane par;
    //-Cursor details	
    Cursor eastResizeCursor = new Cursor(Cursor.N_RESIZE_CURSOR);
    Cursor westResizeCursor = new Cursor(Cursor.S_RESIZE_CURSOR);
    Cursor pumpResizeCursor = new Cursor(Cursor.HAND_CURSOR);
    Cursor defaultCursor = null;
    int preferredHeaderWidth = 0;
    String weight;
    JPopupMenu popAttribute;

    public AttributeData getData() {
        return chart.getAttribute(name);
    }
    
    public BaseTableContainer(JComponent table, ValueChart chart) {
        this(table, null, chart, -1);
    }

    public BaseTableContainer(JComponent table, String name, ValueChart chart, int width) {
        super(BoxLayout.X_AXIS);
        header = new JLabel(".00");
        //Checking if BaseTableContainer is the container holding the attribute bar charts or attribute names

        if (table instanceof AttributeCell) { //This container is for the charts (or relative rankings)
            header.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(((AttributeCell) table).getColor()), BorderFactory.createEmptyBorder()));
            //header.setBorder(BorderFactory.createLineBorder(((AttributeCell)table).getColor()));			
        } else { //this is for the names (boxes which control weights)
//            header.setBorder(BorderFactory.createEmptyBorder());
            header.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        }
        //header.setBorder (BorderFactory.createBevelBorder(BevelBorder.RAISED));
        add(header);
        Dimension dimhead = header.getPreferredSize();

        //Needed to move the handler here in order to use it for the attribute names and charts
        mouseHandler = new MouseHandler();

        if (chart.show_graph) {
            if (table instanceof AttributeCell) {
                AttributeCell ac = (AttributeCell) table;
                JPanel graph = null;
                if (ac.getDomain().getType() == AttributeDomainType.DISCRETE) {
                    graph = ac.makeDiscGraph(ac.getDomain());
                } else {
                    graph = ac.makeContGraph(ac.getDomain());
                }
                add(graph);

                //graph.addMouseListener(new MouseHandler());
                //isn't this the mouse handler for the attribute bar charts?
                //It wasn't doing anything, these events would need to be located here as an override or as below...
                //graph.addMouseListener(mouseHandler);
                //***Adds the same mouselistener, so clicking is tracked from this level and not the AttributeCell level
                //(this is important for controlling mounse events, because otherwise each basecontainer (row) has an independent mounse handler


                graph.addMouseListener(new MouseHandler() {
                    public void mousePressed(MouseEvent e) {
                        //*** Modified for right-click	
                        /*
                         if ((e.getModifiers() & MouseEvent.BUTTON3_MASK) != 0) {
                         System.out.print("Right Click");
                         * */
                        if ((e.getModifiers() & MouseEvent.BUTTON3_MASK) != 0) {
                            //do something
                            int blah = 1;
                            blah = blah + 2;
                        }
                    }
                });


            }
        }
        add(table);
        //-set table and header size details
        Dimension dimtab = table.getPreferredSize(); //-match table dimension with header
        dimhead.height = dimtab.height;
        
        if (width != -1) {
            if (chart.getChartTitle().equals(name) || chart.getChartTitle().equals(name.replace('_', ' '))) {
                dimhead.width = 0; //hides the root/main/left/core/chart/title/primary container. 
            } else if (table instanceof AttributeCell){
                dimhead.width = chart.headerWidth;
            } else {
                dimhead.width = chart.headerWidth/2;
            }
        }
        preferredHeaderWidth = dimhead.width;
        header.setMaximumSize(dimhead);
        header.setMinimumSize(dimhead);
        header.setPreferredSize(dimhead);
        //mouseHandler = new MouseHandler();
        header.addMouseListener(mouseHandler);
        header.addMouseMotionListener(mouseHandler);
        header.setOpaque(false);
        header.setHorizontalAlignment(JLabel.LEFT); //Label Alignment
        header.setVerticalAlignment(JLabel.CENTER);

        popAttribute = new JPopupMenu();

        JMenuItem menuItem = new JMenuItem("Value Function SA...");
        menuItem.addActionListener(this);
        popAttribute.add(menuItem);
        //only show this menu item if the item is connected to a report and bookmark
        if (chart.reportExists() && chart.criteriaBookmarkExistsInReport(name) > 0) { //name might not be right
            menuItem = new JMenuItem("Criteria Details in Report");
            menuItem.addActionListener(this);
            popAttribute.add(menuItem);
        }
        popAttribute.addSeparator();
        menuItem = new JMenuItem("Set Color...");
        menuItem.addActionListener(this);
        popAttribute.add(menuItem);
        menuItem = new JMenuItem("Edit");
        menuItem.addActionListener(this);
        popAttribute.add(menuItem);

        add(popAttribute);
        this.table = table;
        this.name = name;
        this.chart = chart;
    }

    public void setExactSize(int w, int h) {
        super.setSize(w, h);
        Dimension dimhead = header.getPreferredSize();
        dimhead.height = h;
        header.setPreferredSize(dimhead);
        header.setMaximumSize(dimhead);
        header.setMinimumSize(dimhead);
        w -= dimhead.width;
        if (table instanceof AttributeCell) {
            Dimension dimcell = table.getPreferredSize();
            h = Math.min(h, dimcell.height);
        }
        if (table instanceof TablePane) {
            ((TablePane) table).setExactSize(w, h);
        } else {
            table.setSize(w, h);
        }
    }

    public void setSize(int w, int h) {
        super.setSize(w, h);
        Dimension dimhead = header.getPreferredSize();
        dimhead.height = h;
        header.setPreferredSize(dimhead);
        header.setMaximumSize(dimhead);
        header.setMinimumSize(dimhead);
        w -= dimhead.width;
        if (table instanceof AttributeCell) {
            Dimension dimcell = table.getPreferredSize();
            h = Math.min(h, dimcell.height);
        }
        table.setSize(w, h);
    }

    public double getHeightRatio() {
        return heightRatio;
    }

    public double getOverallRatio() {
        return heightScale * heightRatio;
    }

    public void setHeightRatio(double ratio) {
        heightRatio = ratio;
        //if prim, set the cell ratio 
        if (table instanceof AttributeCell) {
            ((AttributeCell) table).setOverallRatio(heightScale * ratio);
        } else // if abst, take each child and set their widthScale to  
        {
            propogateHeightScale();
        }
    }

    public void setRollUp() {//+
        double wt = 0.0;
        TablePane pt = (TablePane) getParent();
        if (pt.getDepth() != chart.mainPane.getDepth() - 1) {
            for (Iterator<BaseTableContainer> it = pt.getRows(); it.hasNext();) {
                BaseTableContainer comp = it.next();
                wt += comp.rollUpRatio;
            }
            BaseTableContainer abs = (BaseTableContainer) pt.getParent();
            abs.setRollUpRatio(wt);
            abs.setRollUp();
        }
    }

    public void setAbstractRatios() {//+
        TablePane pt = (TablePane) getParent();
        if (pt != chart.mainPane) {
            BaseTableContainer btc = (BaseTableContainer) pt.getParent();
            setHeightRatio(getRollUpRatio() / btc.getRollUpRatio());
        }
        if (table instanceof TablePane) {
            TablePane tab = (TablePane) table;
            for (Iterator<BaseTableContainer> it = tab.getRows(); it.hasNext();) {
                it.next().setAbstractRatios();
            }
        }
    }

    public void setRollUpRatio(double dbl) {//+
        rollUpRatio = dbl;
    }

    public double getRollUpRatio() {//+
        return rollUpRatio;
    }

    private void propogateHeightScale() {
        if (table instanceof TablePane) {
            for (Iterator<BaseTableContainer> it = ((TablePane) table).getRows(); it.hasNext();) {
                BaseTableContainer comp = it.next();
                comp.setHeightScale(heightScale * heightRatio);
            }
        }
    }

    private void setHeightScale(double s) {
        heightScale = s;
        propogateHeightScale(); 
    }

    public void adjustHeaderForDepth(int depth) {
        Dimension dimhead = header.getPreferredSize();
        dimhead.height = depth * preferredHeaderWidth;
        header.setMaximumSize(dimhead);
        header.setMinimumSize(dimhead);
        header.setPreferredSize(dimhead);
    }

    public JComponent getTable() {
        return table;
    }

    public String getName() {
        return name;
    }

    public void computeHeightRatio() {
        setHeightRatio(getHeight() / (double) getParent().getHeight());
        if (chart.showAbsoluteRatios) {
            updateHeadersRecursively();
        } else {
            updateHeader();
        }
    }

    public void updateHeadersRecursively() {
        updateHeader();
        if (table instanceof TablePane) {
            Iterator<BaseTableContainer> it;
            for (it = ((TablePane) table).getRows(); it.hasNext();) {
                it.next().updateHeadersRecursively();
            }
        }
    }

    public double entryWeight(ChartEntry entry) {
        if (entry.isMasked()) {
            return 0;
        } else if (table instanceof TablePane) {
            double w = 0;
            Iterator<BaseTableContainer> it;
            for (it = ((TablePane) table).getRows(); it.hasNext();) {
                w += it.next().entryWeight(entry);
            }
            return w;
        } else {
            AttributeValue value = entry.attributeValue(name);
            if (value != null) {
                return getOverallRatio() * value.weight();
            } else {
                return 0;
            }
        }
    }

    public void updateHeader() {
        //String s;
        Double s;
        if (chart.showAbsoluteRatios) {
            //s = headerString(heightRatio * heightScale);
            s = heightRatio * heightScale;
        } else {
            //s = headerString(heightRatio);
            s = heightRatio;
        }
        //*********FORMAT TEXT*********
        //header.setText (s);
        //header.setToolTipText (s);

        AttributeData data = getData();
        
        s = data.getWeight();
        //long pct = Math.round(s * 100);
        Double pct_d = s * 100.0;
        
        DecimalFormat oneDForm = new DecimalFormat("#.#");
        Double pct = Double.valueOf(oneDForm.format(pct_d));
        
//        String pct = String.format("%.2f", Double.valueOf(pct_d).toString());
//        System.out.println(name+" "+heightRatio+" "+heightScale);
        
        String range = "";
        if (data != null && !data.isAbstract()) {
            String best = "";
            String worst = "";
            AttributeDomain d = data.getPrimitive().getDomain();
            double[] wts = d.getWeights();
            for (int i = 0; i < wts.length; i++) {
                if (wts[i] == 0 && worst.isEmpty()) {
                    if (d.getType() == AttributeDomainType.DISCRETE) {
                        String elts[] = d.getElements();
                        worst = elts[i];
                    } else {
                        double kts[] = d.getKnots();
                        worst = String.valueOf(kts[i]);
                    }
                }
                if (wts[i] == 1 && best.isEmpty()) {
                    if (d.getType() == AttributeDomainType.DISCRETE) {
                        String elts[] = d.getElements();
                        best = elts[i];
                    } else {
                        double kts[] = d.getKnots();
                        best = String.valueOf(kts[i]);
                    }
                }
            }
            range = "<br><small>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;[" + (worst.isEmpty() ? "" : "WORST: " + worst) 
                    + (best.isEmpty() ? "" : " BEST: " + best) + "]</small>";
        }
        
        if (getHeight() < 40) {
            header.setText("   " + name.replace('_', ' ') + " (" + pct + "%)");
        } else {
            header.setText("<html><left>" + "&nbsp;&nbsp;&nbsp;" + name.replace('_', ' ') + " (" + pct + "%)" + range + "</left></html>");
        }
        //header.setText(s);
        header.setToolTipText("<html><blockquote><left><font size=\"4\">" + name.replace('_', ' ') + " (" + pct + "%)" + range + "</left></blockquote></html>");
        header.setFont(new Font("Verdana", Font.PLAIN, 12));
    }

    void setWeightString(String wt) {//+
        weight = wt;
    }

    String getWeightString() {//+
        return weight;
    }

    String headerString(double ratio) {
        int percent = (int) Math.round(100 * ratio);
        String s = null;
        if (percent >= 100) {
            s = "1.00";
        } else if (percent < 10) {
            s = ".0" + percent;
        } else {
            s = "." + percent;
        }
        weight = s;
        return s;
    }

    void adjustHeaderWidth() {
        if (table instanceof AttributeCell) {
            int d = ((TablePane) getParent()).getDepth();
            if (d != 1) {
                Dimension dim = new Dimension((d + 1) * chart.headerWidth/2, header.getSize().height);
                header.setPreferredSize(dim);
                header.setMaximumSize(dim);
            }
        }
    }

    public class MouseHandler extends MouseInputAdapter {

        TablePane tSel, tNei, tTopSel, tTopNei, tpane = null;
        BaseTableContainer bSel, bNei, bTopSel, bTopNei, base = null;
        BaseTableContainer prevTop;
        BaseTableContainer nextTop;
        int undoY;
        int pressY;
        boolean pump = false;

        public void mouseDragged(MouseEvent e) {
            int newY = e.getY() + getLocation().y;	//-current x (on the panel) when dragging			
            int delY = newY - dragY;				//determines direction wrt starting point
            dragY = newY;							//-(dragX=x when originally pressed)					

            Point pos = getLocation();
            Dimension size = getSize();
            Dimension psize = getParent().getSize();

            switch (dragMode) {
                case MOVE_DRAG: {
                    setCursor(defaultCursor);
                    int newy = Math.max(0, Math.min(psize.height - size.height, pos.y + delY));
                    setLocation(pos.x, newy);
                    if (delY < 0 && prevComp != null) { // moved to the left
                        if ((pos.y - prevComp.getLocation().y) < prevComp.getHeight() / 2) {
                            TablePane parent = (TablePane) getParent();
                            parent.swapRows(BaseTableContainer.this, prevComp);
                            nextComp = prevComp;
                            prevComp = parent.prevRow(BaseTableContainer.this);
                            if (chart != null) {
                                chart.updateDisplay();
                            }
                        }
                    } else if (delY > 0 && nextComp != null) {
                        if ((pos.y + size.height - nextComp.getLocation().y) > nextComp.getHeight() / 2) {
                            TablePane parent = (TablePane) getParent();
                            parent.swapRows(BaseTableContainer.this, nextComp);
                            prevComp = nextComp;
                            nextComp = parent.nextRow(BaseTableContainer.this);
                            if (chart != null) {
                                chart.updateDisplay();
                            }
                        }
                    }
                    break;
                }

                case WEST_STRETCH_DRAG: {
                    westStretchDrag(BaseTableContainer.this, prevComp, delY);
                    break;
                }

                case WEST_ROLLUP_STRETCH_DRAG: {
                    int dragTopY = pressY + bTopSel.getLocation().y;
                    int newTopY = e.getY() + bTopSel.getLocation().y;	//-current x (on the panel) when dragging
                    int delTopY = newTopY - dragTopY;
                    westRollupStretchDrag(delTopY);
                    break;
                }

                case EAST_STRETCH_DRAG: {
                    eastStretchDrag(BaseTableContainer.this, nextComp, delY);
                    break;
                }

                case EAST_ROLLUP_STRETCH_DRAG: {
                    eastRollupStretchDrag(delY);
                    break;
                }
            }
        }

        public void westRollupStretchDrag(int dy) {
            int delY = dy;
            Dimension adjDim = bNei.getSize();
            if (getHeight() - dy < 0) {
                dy = getHeight();
            } else if (adjDim.height + dy < 0) {
                dy = -adjDim.width;
            }

            Component comp = bTopNei.getParent();
            BaseTableContainer base = bNei;
            
            double del = (double)delY/chart.getMainPaneHeight();

            while (comp instanceof TablePane) {
                TablePane tp = (TablePane) comp;
                for (Iterator<BaseTableContainer> it = tp.getRows(); it.hasNext();) {
                    BaseTableContainer b = it.next();
                    if (b.isAncestorOf(base) || (b == base)) {
                        b.setExactSize(b.getWidth(), b.getHeight() + delY);
                        realignPanels(tp, b);
                        b.setRollUpRatio(b.heightRatio * b.heightScale);
                        comp = b.table;
                        if (b == base) {
                            b.getData().getPrimitive().setWeight(base.getData().getWeight() + del);
                        }
                    }
                    realignPanels(tp, b);
                }
            }

            comp = bTopSel.getParent();
            base = bSel;

            while (comp instanceof TablePane) {
                TablePane tp = (TablePane) comp;
                for (Iterator<BaseTableContainer> it = tp.getRows(); it.hasNext();) {
                    BaseTableContainer b = it.next();
                    if (b.isAncestorOf(base) || (b == base)) {
                        b.setExactSize(b.getWidth(), b.getHeight() - delY);
                        realignPanels(tp, b);
                        b.setRollUpRatio(b.heightRatio * b.heightScale);
                        comp = b.table;
                        if (b == base) {
                            b.getData().getPrimitive().setWeight(base.getData().getWeight() - del);
                        }
                    }
                    realignPanels(tp, b);
                }
            }

            realignAll(chart.mainPane);
        }

        public void eastRollupStretchDrag(int dy) {
            int delY = dy;
            Dimension adjDim = bNei.getSize();
            if (getHeight() + delY < 0) {
                delY = -getHeight();
            } else if (adjDim.height - delY < 0) {
                delY = adjDim.height;
            }

            Component comp = bTopSel.getParent();
            BaseTableContainer base = bSel;
            
            double del = (double)delY/chart.getMainPaneHeight();

            while (comp instanceof TablePane) {
                TablePane tp = (TablePane) comp;
                for (Iterator<BaseTableContainer> it = tp.getRows(); it.hasNext();) {
                    BaseTableContainer b = it.next();
                    if (b.isAncestorOf(base) || (b == base)) {
                        b.setExactSize(b.getWidth(), b.getHeight() + delY);
                        realignPanels(tp, b);
                        b.setRollUpRatio(b.heightRatio * b.heightScale);
                        comp = b.table;
                        if (b == base) {
                            b.getData().getPrimitive().setWeight(base.getData().getWeight() + del);
                        }
                    }
                    realignPanels(tp, b);
                }
            }


            comp = bTopNei.getParent();
            base = bNei;

            while (comp instanceof TablePane) {
                TablePane tp = (TablePane) comp;
                for (Iterator<BaseTableContainer> it = tp.getRows(); it.hasNext();) {
                    BaseTableContainer b = it.next();
                    if (b.isAncestorOf(base) || (b == base)) {
                        b.setExactSize(b.getWidth(), b.getHeight() - delY);
                        realignPanels(tp, b);
                        b.setRollUpRatio(b.heightRatio * b.heightScale);
                        comp = b.table;
                        if (b == base) {
                            b.getData().getPrimitive().setWeight(base.getData().getWeight() - del);
                        }
                    }
                    realignPanels(tp, b);
                }
            }
            realignAll(chart.mainPane);
        }

        void realignAll(TablePane tab) {
            for (Iterator<BaseTableContainer> it = tab.getRows(); it.hasNext();) {
                BaseTableContainer b = it.next();
                realignPanels(tab, b);
                if (b.table instanceof TablePane) {
                    realignAll((TablePane) b.table);
                }
            }
        }

        public void realignPanels(TablePane t, BaseTableContainer b) {
            BaseTableContainer p = t.prevRow(b);
            if (p == null) {
                b.setLocation(b.getLocation().x, 0);
            } else {
                b.setLocation(b.getLocation().x, p.getLocation().y + p.getSize().height);
            }
            b.computeHeightRatio();
            b.revalidate();
            if (chart != null) {
                chart.updateDisplay();
            }
        }

        void westStretchDrag(BaseTableContainer base, BaseTableContainer prev, int dy) {
            Dimension adjDim = prev.getSize();
            Dimension selDim = base.getSize();
            Point pos = base.getLocation();
            if (selDim.height - dy < 0) {
                dy = selDim.height;
            } else if (adjDim.height + dy < 0) {
                dy = -adjDim.width;
            }
            base.setSize(selDim.width, selDim.height - dy);
            base.setLocation(pos.x, pos.y + dy);
            prev.setSize(adjDim.width, adjDim.height + dy);
            base.computeHeightRatio();
            prev.computeHeightRatio();
            if (chart != null) {
                double del = dy/chart.mainPane.getHeight();
                base.getData().getPrimitive().setWeight(base.getData().getWeight() - del);
                prev.getData().getPrimitive().setWeight(prev.getData().getWeight() + del);
            }
            base.revalidate();
            prev.revalidate();
            if (chart != null) {
                chart.updateMainPane();
                chart.updateDisplay();
            }
        }

        void eastStretchDrag(BaseTableContainer base, BaseTableContainer next, int dy) {
            Dimension adjDim = next.getSize();
            Dimension selDim = base.getSize();
            if (selDim.height + dy < 0) {
                dy = -selDim.height;
            } else if (adjDim.height - dy < 0) {
                dy = adjDim.height;
            }
            base.setSize(selDim.width, selDim.height + dy);
            Point adjPos = next.getLocation();
            next.setLocation(adjPos.x, adjPos.y + dy);
            next.setSize(adjDim.width, adjDim.height - dy);
            if (chart != null) {
                double del = dy/chart.mainPane.getHeight();
                base.getData().getPrimitive().setWeight(base.getData().getWeight() + del);
                next.getData().getPrimitive().setWeight(next.getData().getWeight() - del);
            }
            base.computeHeightRatio();
            base.revalidate();
            next.computeHeightRatio();
            next.revalidate();
            if (chart != null) {
                chart.updateMainPane();
                chart.updateDisplay();
            }
        }

        public void mouseMoved(MouseEvent e) {
            stretchMode = NO_STRETCH;
            TablePane parent = null;
            if (getParent() instanceof TablePane) {
                parent = (TablePane) getParent();
            }
            prevComp = parent.prevRow(BaseTableContainer.this);
            nextComp = parent.nextRow(BaseTableContainer.this);
            int y = e.getY();
            if (chart.getDirectionSA() == UP) {
                if (defaultCursor == null) {
                    defaultCursor = getCursor();
                }
                if (table instanceof AttributeCell) {
                    //->this bit sets to determine if the first and last edges of the chart
                    tpane = (TablePane) getParent();
                    base = BaseTableContainer.this;
                    boolean isedge2 = true;
                    while (tpane.getDepth() != chart.mainPane.getDepth()) {
                        if ((BaseTableContainer) tpane.prevRow(base) != null) {
                        }
                        if ((BaseTableContainer) tpane.nextRow(base) != null) {
                            isedge2 = false;
                        }
                        base = (BaseTableContainer) tpane.getParent();
                        tpane = (TablePane) base.getParent();
                    }
                    //->					
/*					if (y <= 2){
                     if (!isedge){
                     stretchMode = WEST_ROLLUP_STRETCH;
                     setCursor (westResizeCursor);
                     }						
                     }	
                     else */                    if (y >= getHeight() - 4) {
                        if (!isedge2) {
                            stretchMode = EAST_ROLLUP_STRETCH;
                            setCursor(westResizeCursor);
                        }
                    } else if (chart.isPumpSelected()) {
                        //stretchMode = PUMP;
                        pump = true;
                        setCursor(pumpResizeCursor);
                    } else {
                        stretchMode = NO_STRETCH;
                        setCursor(defaultCursor);
                    }
                    //System.out.println(stretchMode);
                }
            } else {
                //-set the default curser to the system cursor
                if (defaultCursor == null) {
                    defaultCursor = getCursor();
                }
                if (chart.isPumpSelected() && parent != chart.mainPane) {
                    stretchMode = PUMP;
                    setCursor(pumpResizeCursor);
                } //if it is the left side of the objective AND not the (displayed) first in its family 
                else if (y <= 2 && prevComp != null) {
                    stretchMode = WEST_STRETCH;
                    setCursor(westResizeCursor);
                } //if it is on the right side of the objective AND not the (displayed) last in its family
                else if (y >= getHeight() - 4 && nextComp != null) {
                    stretchMode = EAST_STRETCH;
                    setCursor(eastResizeCursor);
                } else {
                    stretchMode = NO_STRETCH;
                    setCursor(defaultCursor);
                }
            }
        }

        //-reset to default when mouse leaves header
        public void mouseExited(MouseEvent e) {
            if (stretchMode != NO_STRETCH) {
                stretchMode = NO_STRETCH;
                setCursor(defaultCursor);
            }
        }

        public void mousePressed(MouseEvent e) {
            if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) != 0) {		//-left-click
                if (dragMode == NO_DRAG) {
                    switch (stretchMode) {
                        case NO_STRETCH: {
                            dragMode = MOVE_DRAG;
                            break;
                        }
                        case EAST_STRETCH: {
                            dragMode = EAST_STRETCH_DRAG;
                            break;
                        }
                        case WEST_STRETCH: {
                            dragMode = WEST_STRETCH_DRAG;
                            break;
                        }
                        case EAST_ROLLUP_STRETCH: {
                            dragMode = EAST_ROLLUP_STRETCH_DRAG;
                            setEastRollup(BaseTableContainer.this);
                            break;
                        }
                        case WEST_ROLLUP_STRETCH: {
                            dragMode = WEST_ROLLUP_STRETCH_DRAG;
                            //e.translatePoint(e.getX() + getWidth()-4, e.getY());
                            setWestRollup(BaseTableContainer.this);
                            pressY = e.getY();
                            break;
                        }
                    }

                    Component parent = getParent();
                    if (parent instanceof JLayeredPane) {
                        ((JLayeredPane) parent).moveToFront(BaseTableContainer.this);
                    }
                    setVisible(true);
                    dragY = e.getY() + getLocation().y;
                    undoY = dragY;
                }
            } else if ((e.getModifiers() & MouseEvent.BUTTON2_MASK) != 0) {	//-centre-click
                //-if it is a primitive obj, show domain in popup
                if (table instanceof AttributeCell) {
                    JPopupMenu popup = ((AttributeCell) table).getDomainPopup();
                    Point loc = header.getLocationOnScreen();
                    popup.setLocation(loc.x + e.getX(), loc.y + e.getY());
                    popup.setVisible(true);
                }
            } else if ((e.getModifiers() & MouseEvent.BUTTON3_MASK) != 0) {	//-right-click
                //-if it is a primitive obj, show menu
                if (table instanceof AttributeCell) {
                    popAttribute.show(e.getComponent(), e.getX() + 5, e.getY() + 5);
                }
            }

        }

        void setWestRollup(BaseTableContainer b) {
            bSel = b;
            tSel = (TablePane) bSel.getParent();
            bTopSel = bSel;	//prepare for rollup
            tTopSel = tSel;
            //get the top movable btc
            while ((tTopSel.getDepth() != chart.mainPane.getDepth() - 1)
                    && tTopSel.prevRow(bTopSel) == null) {
                bTopSel = (BaseTableContainer) tTopSel.getParent();
                tTopSel = (TablePane) bTopSel.getParent();
            }
            //get top's neighbor
            bTopNei = tTopSel.prevRow(bTopSel);
            if (bTopNei != null) {
                tTopNei = (TablePane) bTopNei.getParent();
                //get sel's neighbor
                bNei = bTopNei;	//prepare fot drilldown
                tNei = tTopNei;
                while (!(bNei.table instanceof AttributeCell)) {
                    tNei = (TablePane) bNei.table;
                    bNei = tNei.getRowAt(0);
                }
            }
            
            String str = "";
            if (bSel.getData() != null) {
                str = LogUserAction.getSingleDataOutput(bSel.getData(), LogUserAction.OUTPUT_WEIGHT);
            }
            if (bNei.getData() != null) {
                str += LogUserAction.getSingleDataOutput(bNei.getData(), LogUserAction.OUTPUT_WEIGHT);
            }
            chart.setLogOldAttributeData(str);
        }

        void setEastRollup(BaseTableContainer b) {
            bSel = b;
            tSel = (TablePane) bSel.getParent();
            bTopSel = bSel;	//prepare for rollup
            tTopSel = tSel;
            //get the top movable btc
            while ((tTopSel.getDepth() != chart.mainPane.getDepth() - 1)
                    && tTopSel.nextRow(bTopSel) == null) {
                bTopSel = (BaseTableContainer) tTopSel.getParent();
                tTopSel = (TablePane) bTopSel.getParent();
            }
            //get top's neighbor
            bTopNei = tTopSel.nextRow(bTopSel);
            if (bTopNei != null) {
                tTopNei = (TablePane) bTopNei.getParent();
                //get sel's neighbor
                bNei = bTopNei;	//prepare for drilldown
                tNei = tTopNei;
                while (!(bNei.table instanceof AttributeCell)) {
                    tNei = (TablePane) bNei.table;
                    bNei = tNei.getRowLast();
                }
            }
            
            String str = "";
            if (bSel.getData() != null) {
                str = LogUserAction.getSingleDataOutput(bSel.getData(), LogUserAction.OUTPUT_WEIGHT);
            }
            if (bNei.getData() != null) {
                str += LogUserAction.getSingleDataOutput(bNei.getData(), LogUserAction.OUTPUT_WEIGHT);
            }
            chart.setLogOldAttributeData(str);
        }

        void upSize(BaseTableContainer b) {
            TablePane p = (TablePane) b.getParent();

            if (p.getParent() instanceof BaseTableContainer) {
                BaseTableContainer bp = (BaseTableContainer) p.getParent();
                if (bp.getParent() != chart.mainPane) {
                    int ht = 0;
                    for (Iterator<BaseTableContainer> it = p.getRows(); it.hasNext();) {
                        ht += it.next().getHeight();
                    }

                    bp.setExactSize(bp.getWidth(), ht);
                    upSize(bp);
                }
            }
        }

        public void pump(BaseTableContainer base, boolean up) {
            chart.setLogOldAttributeData(LogUserAction.getDataOutput(chart.getAttrData(), LogUserAction.OUTPUT_WEIGHT));
            Vector<BaseTableContainer> prims = chart.getPrims();
            double othercount = -1;
            for (Iterator<BaseTableContainer> it = prims.iterator(); it.hasNext();) {
                BaseTableContainer b = it.next(); 
                if (b.getData().getWeight() > 0) {
                    othercount = othercount + 1;
                }
            }
            double pump = 0.01;
            if (!up) {
                pump = -pump;
            }
            for (Iterator<BaseTableContainer> it = prims.iterator(); it.hasNext();) {
                BaseTableContainer b = it.next();
                //int bsize = b.getHeight(); //height of primitive at start			
                if (b == base) {
                    b.setRollUpRatio(b.getData().getWeight() + pump);
                    b.getData().getPrimitive().setWeight(b.getData().getWeight() + pump);
                } else {
                    if (b.rollUpRatio < (pump / othercount)) {
                        double d = b.getData().getWeight();
                        if (b.rollUpRatio != 0) {
                            base.setRollUpRatio(base.rollUpRatio + (pump / othercount - d));
                            base.getData().getPrimitive().setWeight(base.getData().getWeight() + (pump / othercount - d));
                        }
                        b.setRollUpRatio(0);
                        b.getData().getPrimitive().setWeight(0);
                    } else {
                        b.setRollUpRatio(b.rollUpRatio - pump / othercount);
                        b.getData().getPrimitive().setWeight(b.getData().getWeight() - pump / othercount);
                    }
                }
            }
            chart.updateMainPane();
            realignAll(chart.mainPane);
            chart.mainPane.updateSizesAndHeights();
            chart.logPump(base.getName(), up);
        }
        //-double-click for sorting by objective: call to reorder	

        public void mouseClicked(MouseEvent e) {
            int newY = e.getY() + getLocation().y;	//-current x when dragging
            dragY = newY;							//-(dragX=x when originally pressed) 
//SANJANA: To pump with multiple clicks so that you don't have to wait for a second to pump
//            if (chart.isPumpSelected() && pump && e.getClickCount() == 1) { 
            
            if (chart.isPumpSelected() && pump) {
                if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) != 0) {
//                    if (e.getClickCount() == 1) {
                        if (chart.getDirectionSA() == UP) {
                            LastInteraction interact = new LastInteraction(chart);
                            interact.setUndoPump(name, chart.pump_increase ? false : true);
                            chart.addInteraction(interact);
                            
                            pump(BaseTableContainer.this, chart.pump_increase);
                        } else {
                            TablePane par = null;
                            if (getParent() instanceof TablePane) {
                                par = (TablePane) getParent();
                            }
                            int sibcount = par.getComponentCount() - 1;
                            int pump = sibcount * 4;
                            if (!chart.pump_increase) {
                                pump = -pump;
                            }
                            setSize(getWidth() + pump, getHeight());

                            for (Iterator<BaseTableContainer> it = par.getRows(); it.hasNext();) {
                                BaseTableContainer b = it.next();
                                if (b != BaseTableContainer.this) {
                                    b.setSize(b.getWidth(), b.getHeight() - pump / sibcount);
                                }
                            }
                            realignAll(chart.mainPane);
                        }
//                    }
                }
            }
            if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) != 0) {
//                if (chart.isSortSelected() && e.getClickCount() == 2) {
            	if (e.getClickCount() == 2) {
            		if(chart.isSortSelected()){
            			chart.reorderEntries(BaseTableContainer.this);
            		}
            		else if (e.getComponent().toString().startsWith("DiscGraph") || e.getComponent().toString().startsWith("ContGraph")) {
                        ((AttributeCell) table).getUtility(((AttributeCell) table).getDomain());
                    } 
//            		else {
//                        chart.reorderEntries(BaseTableContainer.this);
//                    }
                }
            }
        }

        public void mouseReleased(MouseEvent e) {
            int relY = e.getY() + getLocation().y;
            int dy = undoY - relY;
            if (dragMode == EAST_ROLLUP_STRETCH_DRAG) {
                LastInteraction interact = new LastInteraction(chart);
                interact.setUndoSlide(name, dy, relY, true);
                chart.addInteraction(interact);
                
                chart.logDrag(bSel.getData(), bNei.getData());
            }
            /*			if (dragMode == WEST_ROLLUP_STRETCH_DRAG)
             chart.last_int.setUndoSlide(BaseTableContainer.this, -dy, relY, false);	*/
            //-cleanup: reset mode, call to rearrange VC
            if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) != 0) {
                if (dragMode != NO_DRAG) {
                    dragMode = NO_DRAG;
                    if (stretchMode == NO_STRETCH) {
                        snapToLocation();
                    }
                }
            } //-closes the popup
            else if ((e.getModifiers() & MouseEvent.BUTTON2_MASK) != 0) {
                if (table instanceof AttributeCell) {
                    JPopupMenu popup = ((AttributeCell) table).getDomainPopup();
                    popup.setVisible(false);
                }
            }
        }

        public void snapToLocation() {
            if (prevComp != null) {
                Point loc = prevComp.getLocation();
                loc.y += prevComp.getHeight();
                setLocation(loc);
            } else if (nextComp != null) {
                Point loc = nextComp.getLocation();
                loc.y -= getHeight();
                setLocation(loc);
            }
        }
    }

        
    public void actionPerformed(ActionEvent ae) {
        if (("Value Function SA...").equals(ae.getActionCommand())) {
            ((AttributeCell) table).makeUtility(((AttributeCell) table).getDomain());
        } else if (("Set Color...").equals(ae.getActionCommand())) {
            new ColorSelection(table, chart, header);
        } else if (("Edit").equals(ae.getActionCommand())) {
            chart.showConstructionModel();
        } else if (("Criteria Details in Report").equals(ae.getActionCommand())) {
            //go to the valuechart report
            chart.zoomToReport(this.getName());
        }
    }
    
    public void updateSize() {
        if (getData() == null)
            return;
        setExactSize(getWidth(), (int) Math.round(getData().getWeight() * chart.mainPane.getHeight()));
        // abstract
        if (!(table instanceof AttributeCell)) {
            for (Iterator<BaseTableContainer> it = ((TablePane) table).getRows(); it.hasNext();) {
                BaseTableContainer comp = it.next();
                comp.updateSize();
            }
        }
    }
}
