
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.util.*;
import java.io.*;

import acme.misc.*;
import org.icepdf.core.pobjects.OutlineItem;
import org.icepdf.core.pobjects.Outlines;
import org.icepdf.ri.common.ComponentKeyBinding;
import org.icepdf.ri.common.OutlineItemTreeNode;
import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.SwingViewBuilder;
import org.icepdf.ri.common.views.DocumentViewControllerImpl;

public class ValueChart extends JPanel {

    private static final long serialVersionUID = 1L;
//    static public final int DEFAULT_COL_WIDTH = 30;
    static public final int DEFAULT_COL_WIDTH = 50; //SANJANA: Changed default col width to "Large"
    //*%* Changed display height to make it relative to the screen size
    //static public final int DEFAULT_DISPLAY_HEIGHT = 365;
    static public final int DEFAULT_DISPLAY_HEIGHT = (int) (Toolkit.getDefaultToolkit().getScreenSize().height * .48);
    //*%*
    static public final int DEFAULT_DISPLAY = 1,
            SIDE_DISPLAY = 2,
            SEPARATE_DISPLAY = 3;
    static public final Color gridColor = new Color(150, 150, 150); // between lightgray and gray
    
    int headerWidth = 250;
    int graphWidth = 100;
    int displayType = DEFAULT_DISPLAY;
    int displayHeight = DEFAULT_DISPLAY_HEIGHT;
    int colWidth = DEFAULT_COL_WIDTH;
    boolean showAbsoluteRatios = false;
    private JFrame chartFrame = null;
    private String chartTitle;
    TablePane mainPane;
    JPanel mainPaneWithNames;
    EntryNamePanel mainEntryNames;
    DisplayPanel displayPanel;
    EntryNamePanel displayEntryNames;
    JPanel displayWithNames;
    DisplayDialog dialog;
    Reader initReader = null;
    ResizeHandler resizeHandler;
    JPanel pnlDisp;
    String filename;
    Vector<JObjective> objs;
    Vector<HashMap<String,Object>> alts;
    private Vector<BaseTableContainer> prims;
    private HashMap<String, BaseTableContainer> prims_map;
    ConstructionView con;
    boolean pump = false;
    boolean sort = false;
    boolean pump_increase = true;
    boolean show_graph = true;
    int sa_dir = BaseTableContainer.UP;
    SensitivityAnalysisOptions pnlOpt;
    public DomainValues pnlDom;
    OptionsMenu menuOptions;
    boolean isNew = true;
    private Stack<LastInteraction> undo_int;
    private Stack<LastInteraction> redo_int;

    LogUserAction log;
    boolean displayUtilityWeights = false;
    private ChartData chartData;

    
    //***Added so that there is one frame that allows display of pdf reports from value chart. This window is not used for each of the attribute/entry reports.
    //Those are contained within the AttributeCell class. Rather, this is for anywhere else on the interface (it started with a need to have one report window to display the report for the criteria details)
    SwingController controller; //Used for each window that controls a pdf document
    JFrame reportWindow; //the windows created for the attribute, these are used to show the report. They are class variables because we need to toggle visibility from different methods
    JPanel viewerComponentPanel; //the panel for which the frame sits on;
    File reportFile; //location of the report, will be null if there is no report, see doRead()
    Outlines outlines; //The bookmark outline
    OutlineItem item = null; //The items in the bookmark

//CONSTRUCTOR
    
    /*public ValueChart(ValueChart vc) {
        super();
    }*/
    
    public ValueChart(ConstructionView c, String file, LogUserAction l, int type, int colwd, boolean b, boolean g, boolean dispUtil) {
        super();
        show_graph = g;
        con = c;
        isNew = b;
        filename = file;
        setDisplayType(type);
        displayUtilityWeights = dispUtil;
        
        constructFromFile(l, colwd);
    }

    public ValueChart(ConstructionView c, String file, LogUserAction l,
            ChartData data, int type, int colwd,
            boolean b, boolean g, boolean dispUtil) {
        super();
        show_graph = g;
        con = c;
        isNew = b;
        setDisplayType(type);
        displayUtilityWeights = dispUtil;

        if (data == null)
            constructFromFile(l, colwd);
        else {
            filename = file;
            constructFromAttribute(data, l, colwd);
        }
    }
    
    
    private void constructFromFile(LogUserAction l, int colwd) {
        chartData = new ChartData(this);
        
        menuOptions = new OptionsMenu(this);
        menuOptions.createMenu();
        if (!isNew) {
            menuOptions.setVisible(false);
        }
        undo_int = new Stack<LastInteraction>();
        redo_int = new Stack<LastInteraction>();
        setColWidth(colwd);
        showAbsoluteRatios = true;
        try {
            initReader = new FileReader(filename);
            resizeHandler = new ResizeHandler();
            addComponentListener(resizeHandler);
            try {
                read(initReader);
            } catch (IOException e) {
                System.out.println("Error in input: " + e.getMessage());
            }
            //setDisplayHeight(Toolkit.getDefaultToolkit().getScreenSize().height);
            setDisplayHeight(displayHeight);
            setSize(getPreferredWidth(), displayType == SIDE_DISPLAY ? displayHeight * 2 - 50 : displayHeight);
            setVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
        }

        showVC();
        if (l != null) {
            log = l;
        } else {
            log = new LogUserAction(getChartTitle());
            log.setVerbosity(LogUserAction.LOG_ALL);
        }
        if (isNew) {
            menuOptions.setSelectedItems();
            chartFrame.setJMenuBar(menuOptions);
        }
        setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        if (displayType == SEPARATE_DISPLAY) {
            dialog.setLocation(chartFrame.getX(), chartFrame.getHeight());
        }
    }
    
    
    private void constructFromAttribute(ChartData data, LogUserAction l, int colwd) {
        chartData = data;

        menuOptions = new OptionsMenu(this);
        menuOptions.createMenu();
        if (!isNew) {
            menuOptions.setVisible(false);
        }
        undo_int = new Stack<LastInteraction>();
        redo_int = new Stack<LastInteraction>();
        setColWidth(colwd);

        showAbsoluteRatios = true;
        chartData = data;
        resizeHandler = new ResizeHandler();
        addComponentListener(resizeHandler);

        // TODO
        readFromAttribute();

        // setDisplayHeight(Toolkit.getDefaultToolkit().getScreenSize().height);
        setDisplayHeight(displayHeight);
        setSize(getPreferredWidth(),
                displayType == SIDE_DISPLAY ? displayHeight * 2 - 50
                        : displayHeight);
        setVisible(true);

        showVC();
        if (l != null) {
            log = l;
        } else {
            log = new LogUserAction(getChartTitle());
            log.setVerbosity(LogUserAction.LOG_ALL);
        }
        if (isNew) {
            menuOptions.setSelectedItems();
            chartFrame.setJMenuBar(menuOptions);
        }
        setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        if (displayType == SEPARATE_DISPLAY) {
            dialog.setLocation(chartFrame.getX(), chartFrame.getHeight());
        }
    }

//DISPLAY
    public void setDisplayHeight(int h) {
        displayHeight = h;
    }

    public int getPreferredWidth() {
        int w = ((mainPane.getDepth() -1) + chartData.getEntryList().size()) * colWidth;
        if (displayType == SIDE_DISPLAY) {
            w += (2 + chartData.getEntryList().size()) * colWidth;
        }
        return w;
    }

    public int getColWidth() {
        return colWidth;
    }

    public void setColWidth(int w) {
        colWidth = w;
    }

    public void updateDisplay() {
        displayPanel.repaint();
    }

    public void updateAll() {
        updateHeaders();
        displayPanel.repaint();
        mainPane.repaint();
    }

    public void reorderEntries(BaseTableContainer baseTab) {
        Comparator<ChartEntry> weightComparator = new WeightComparator(baseTab);
        Collections.sort(chartData.getEntryList(), weightComparator);
        mainEntryNames.relabel(chartData.getEntryList());
        displayEntryNames.relabel(chartData.getEntryList());
        displayPanel.repaint();
        mainPane.repaintEntries();
    }

    public void setVisible(boolean b) {
        super.setVisible(b);
        if (dialog != null) {
            dialog.setVisible(b);
        }
    }

//CONNECT CONST/INSP
    void setPrims(TablePane pane) {
        Iterator<BaseTableContainer> it;
        for (it = pane.getRows(); it.hasNext();) {
            BaseTableContainer btc = it.next();
            if (btc.table instanceof AttributeCell) {
                prims.add(btc);
                prims_map.put(btc.getName(), btc);
                btc.adjustHeaderWidth();//added for rotate
            } else {
                setPrims((TablePane) btc.table);
            }
        }
    }

    public void setConnectingFields() {
        for (int i = 0; i < con.getObjPanel().getPrimitiveObjectives().size(); i++) {
            JObjective obj = con.getObjPanel().getPrimitiveObjectives().get(i);
            BaseTableContainer btc = getPrimContainer(obj.getName());
            if (btc != null) {
                AttributeCell ac = (AttributeCell) btc.getTable();
                obj.setDomain(ac.getDomain());
                obj.setUnit(ac.getUnits());
                double pc = btc.getOverallRatio();
                AttributeData data = btc.getData();
                if (data != null && !data.isAbstract()) pc = data.getWeight();
                obj.setWeight(String.valueOf(pc));
                //connect ac and obj
                ac.obj = obj;
                obj.acell = ac;
                if (obj.getDomainType() == AttributeDomainType.CONTINUOUS) {
                    if (obj.getUnit().equals("CAD")) {
                        obj.setDecimalFormat("0.00");
                    }
                    if (obj.maxC > 100) {
                        obj.setDecimalFormat("0");
                    } else {
                        obj.setDecimalFormat("0.0");
                    }
                    if (ac.cg != null) {
                        ac.cg.repaint();
                    }
                }
            }
        }
    }

    void setObjs(TablePane pane, String str) {
        Iterator<BaseTableContainer> it;
        JObjective obj = null;
        for (it = pane.getRows(); it.hasNext();) {
            BaseTableContainer btc = it.next();
            btc.updateHeader();
            if (str.equals("root")) {
                con.getObjPanel().lblRoot.setName(btc.getName());
            } else {
                    obj = new JObjective(btc.getName());
                con.getObjPanel().addFromVC(str, obj);
                objs.add(obj);
                if (btc.table instanceof AttributeCell) {
                    AttributeCell ac = (AttributeCell) btc.getTable();
                    obj.setColor(ac.getColor());
                    obj.origin = JObjective.FROM_FILE;
                }
            }
            if (btc.table instanceof TablePane) {
                setObjs((TablePane) btc.table, btc.getName());
            }
        }
    }

    Vector<HashMap<String,Object>> setAlts() {
        Iterator<ChartEntry> it;
        Vector<HashMap<String,Object>> alts = new Vector<HashMap<String,Object>>();
        for (it = chartData.getEntryList().iterator(); it.hasNext();) {
            HashMap<String,Object> datamap = new HashMap<String,Object>();
            ChartEntry entry =  it.next();
            datamap.put("name", entry.name);
            datamap.putAll(entry.map);
            for (Iterator<JObjective> it2 = objs.iterator(); it2.hasNext();) {
                JObjective obj = it2.next();
                if (!datamap.containsKey(obj.getName())) {
                    datamap.put(obj.getName(), "0");
                } else {
                    AttributeValue val = (AttributeValue) datamap.get(obj.getName());
                    datamap.put(obj.getName(), val.stringValue());
                    AttributeDomain domain = getDomain(val.attributeName);
                    obj.setDomainType(domain.getType());
                }
            }
            alts.add(datamap);
        }
        return alts;
    }

    public Vector<BaseTableContainer> getPrims() {
        return prims;
    }
    
    public BaseTableContainer getPrimContainer(String name) {
        return prims_map.get(name);
    }

//READS
    private void read(Reader reader)
            throws IOException {
        ScanfReader scanReader = new ScanfReader(reader);
        try {
            doread(scanReader);
        } catch (Exception e) {
            throw new IOException(e.getMessage() + ", line " + scanReader.getLineNumber());
        }
        //bulding the construction view straight from vc
        if (!con.init) {
            setConst();
            con.setInit(true);
        }
        con.setChart(this);
        //set prim obj list for rolling up the absolute tree    	
        prims = new Vector<BaseTableContainer>();
        prims_map = new HashMap<String, BaseTableContainer>();
        setPrims(mainPane);
        for (Iterator<BaseTableContainer> it = prims.iterator(); it.hasNext();) {
            BaseTableContainer btc = it.next();
            btc.setRollUp();
        }
        mainPane.getRowAt(0).setAbstractRatios();
        setConnectingFields();
    }
    
    private void readFromAttribute() {
        if (chartData == null || chartData.getAttrData() == null)
            return;

        mainPane = new TablePane();
        renderMainPaneAttributes();
        mainPane.adjustAttributesForDepth(mainPane.getDepth());
        
        rebuildMainPane();

        // bulding the construction view straight from vc
        if (!con.init) {
            setConst();
            con.setInit(true);
        }
        con.setChart(this);
        // set prim obj list for rolling up the absolute tree
        prims = new Vector<BaseTableContainer>();
        prims_map = new HashMap<String, BaseTableContainer>();
        setPrims(mainPane);
        for (Iterator<BaseTableContainer> it = prims.iterator(); it.hasNext();) {
            BaseTableContainer btc = it.next();
            btc.setRollUp();
        }
        mainPane.getRowAt(0).setAbstractRatios();
        setConnectingFields();
    }

    public void newConst() {
        con = new ConstructionView(ConstructionView.FROM_VC);
        setConst();
    }

    private void setConst() {
        objs = new Vector<JObjective>();
        setObjs(mainPane, "root");
        alts = new Vector<HashMap<String,Object>>();
        alts = setAlts();
        con.getAltPanel().setFileAlternatives(alts);
        con.getAltPanel().updateTable();
        con.setChart(this);
        //for vc w/other data
    }

    private void doread(ScanfReader scanReader)
            throws IOException {

        chartData.setEntryList(new Vector<ChartEntry>(10));
        HashMap<String, Color> colorList = new HashMap<String, Color>(10);
        boolean attributesRead = false;

        colorList.put("red", Color.red);
        colorList.put("green", Color.green);
        colorList.put("blue", Color.blue);
        colorList.put("cyan", Color.cyan);
        colorList.put("magenta", Color.magenta);
        colorList.put("yellow", Color.yellow);

        while (true) {
            String keyword = null;
            try {
                keyword = scanReader.scanString();
            } catch (EOFException e) {
                break;
            }
            if (keyword.equals("attributes")) {
                mainPane = new TablePane();
                chartData.setAttrData(new Vector<AttributeData>());
                readAttributes(scanReader, null, colorList);
                renderMainPaneAttributes();
                mainPane.adjustAttributesForDepth(mainPane.getDepth());
                attributesRead = true;
            } else if (keyword.equals("color")) {
                String name = scanReader.scanString();
                float r = scanReader.scanFloat();
                float g = scanReader.scanFloat();
                float b = scanReader.scanFloat();
                colorList.put(name, new Color(r, g, b));
            } else if (keyword.equals("entry")) {
                if (!attributesRead) {
                    throw new IOException("Entry specified before attributes");
                }
                chartData.addChartEntry(readEntry(scanReader, mainPane));
            } else if (keyword.startsWith("report=")) {
                //Sets the report location for thie ValueCharts parent interface (there is another reportFileLocation, which is specific to the AttributeCell bar charts)
                reportFile = new File(keyword.substring(7).replace("$", " ").replace("\\", "\\\\"));
                if (reportFile.exists()) {
                    createReportController();
                } else {
                    //since the report does not exist, throw a system message letting the user know this, and set the report to null
                    System.out.println("The report for the ValueChart is not valid, the system believes the report is located at: ");
                    System.out.println("  " + reportFile.toString());
                    System.out.println("  If this is an error, please verify that you have correctly substituted all spaces and added two slashes between directories.");
                    reportFile = null;
                }
            } else {
                throw new IOException("Unknown keyword " + keyword);
            }
        }

        rebuildMainPane();
    }

    private double readHeightRatio(ScanfReader scanReader)
            throws IOException {
        String frac = scanReader.scanString();
        //if weight = *, set it at -1
        double hr = -1;
        if (!frac.equals("*")) {
            try {
                hr = Double.parseDouble(frac);
            } catch (NumberFormatException e) {
                throw new IOException("Illegal double value " + frac);
            }
        }
        return hr;
    }

    private void readAttributes(ScanfReader scanReader,
            AttributeData attr, HashMap<String, Color> colorList)
            throws IOException {
        String name = null;
        while (!(name = scanReader.scanString()).equals("end")) {
            AttributeData a;
            double hr;
            if (name.equals("attributes")) { // if keyword=attibutes, then read
                                             // abstract name and ratio
                a = new AttributeAbstractData();
                name = scanReader.scanString();
                a.setName(name);
                hr = readHeightRatio(scanReader);
                readAttributes(scanReader, a, colorList);

            } else { // if no keyword is primitive
                a = new AttributePrimitiveData();
                a.setName(name);
                hr = readHeightRatio(scanReader);
                a.getPrimitive().setWeight(hr);
                AttributeDomain domain = AttributeDomain.read(scanReader);
                a.getPrimitive().setDomain(domain);
                String option = null;

                // This is where the program reads through the valuecharts file
                // (.vc) and collects the properties for each of the attributes
                while (!(option = scanReader.scanString()).equals("end")) {
                    if (option.startsWith("color=")) {
                        String colorName = option.substring(6);
                        Color color = (Color) colorList.get(colorName);
                        if (color == null) {
                            throw new IOException("Unknown color '" + colorName
                                    + "'");
                        }
                        a.getPrimitive().setColor(color);
                    } else if (option.startsWith("units=")) {
                        String unitsName = option.substring(6);
                        a.getPrimitive().setUnitsName(unitsName);
                    } else {
                        throw new IOException("Unknown option '" + option + "'");
                    }
                }
            }

            chartData.addAttributeData(attr, a);
        }
    }

    private ChartEntry readEntry(ScanfReader scanReader, TablePane pane)
            throws IOException {
        String entryName = scanReader.scanString("%S");
        ChartEntry entry = new ChartEntry(entryName);
        String attributeName;
        while (!(attributeName = scanReader.scanString()).equals("end")) {

            //***
            //Here we have added a special case for the entry, called "report=", this is the PDF file location of the report associated with this entry 
            //PROBLEM: the acme scanReader reads the line in the file, but if the line has any spaces is becomes the next attribute.
            //         so, in the valuecharts file, the user needs to specify the entire line, including the path as one single line
            //         We suggest using "$" to replace spaces
            //         This method also adds a double slash when the user enters a single (necessary for java opening the file)
            if (attributeName.startsWith("report=")) {
                File reportFileLocation = new File(attributeName.substring(7).replace("$", " ").replace("\\", "\\\\"));
                entry.setReport(reportFileLocation);

                //***Now create the windows necessary to hold the pdf in view
                //build a controller
                SwingController controller = new SwingController(); //Used for each window that controls a pdf document
                SwingViewBuilder factory = new SwingViewBuilder(controller);

                // Build a SwingViewFactory configured with the controller
                JPanel viewerComponentPanel = new JPanel(); //the panel for which the frame sits on;
                viewerComponentPanel = factory.buildViewerPanel();
                // add copy keyboard command
                ComponentKeyBinding.install(controller, viewerComponentPanel);
                // add interactive mouse link annotation support via callback
                controller.getDocumentViewController().setAnnotationCallback(new org.icepdf.ri.common.MyAnnotationCallback(controller.getDocumentViewController()));
                //Use the factory to build a JPanel that is pre-configured
                //with a complete, active Viewer UI.
                // Open a PDF document (frame is no yet visible, but the document is opened and ready for interaction)
                controller.openDocument(reportFileLocation.toString());
                //Make it continuous view
                controller.setPageFitMode(org.icepdf.core.views.DocumentViewController.PAGE_FIT_WINDOW_WIDTH, false);
                controller.setPageViewMode(DocumentViewControllerImpl.ONE_COLUMN_VIEW, true);

                // Create a JFrame to display the panel in
                JFrame window = new JFrame("Report for " + entryName); //the windows created for the attribute, these are used to show the report. They are class variables because we need to toggle visibility from different methods
                window.getContentPane().add(viewerComponentPanel);
                window.pack();
                window.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE); //we only want to hide the window, not close it.
                window.setSize(new Dimension(800,600));
                
                //The Frame and controller are the only aspects of the report frame that need to be modified from elsewhere in the program (particularly AttributeCell, so they become part of the hashmap
                entry.setReportFrame(window);
                entry.setReportController(controller);

                //Now add the outline and bookmark items to the hash map
                OutlineItem entryItem = null;
                Outlines entryOutlines = controller.getDocument().getCatalog().getOutlines();
                if (entryOutlines != null) {
                    entryItem = entryOutlines.getRootOutlineItem();
                }
                if (entryItem != null) {
                    OutlineItemTreeNode outlineItemTreeNode = new OutlineItemTreeNode(entryItem);
                    outlineItemTreeNode.getChildCount();  // Added this line
                    Enumeration depthFirst = outlineItemTreeNode.depthFirstEnumeration();
                    // find the node you need
                }
                
                entry.setOutlineItem(entryItem);
                //entry.map.put("Outlines", entryOutlines); //this doesn't need to be added, it is only the items that are needed for locating
                
                
            } else {
                AttributeCell attributeCell = pane.getAttributeCell(attributeName);
                if (attributeCell == null) {
                    throw new IOException("attribute " + attributeName + " not found");
                }
                AttributeDomain domain = attributeCell.getDomain();
                AttributeValue value;
                if (domain.getType() == AttributeDomainType.DISCRETE) {
                    String name = scanReader.scanString("%S");
                    value = new AttributeValue(name, domain, attributeName, this);
                } else { // type == AttributeDomain.CONTINUOUS
                    double x = scanReader.scanDouble();
                    value = new AttributeValue(x, domain, attributeName, this);
                }
                entry.map.put(attributeName, value);
            }
        }
        return entry;
    }

//BOOLS
    
    public void showConstructionModel() {
        setConnectingFields();
        log.setOldAttrData(LogUserAction.getDataOutput(chartData.getAttrData(), LogUserAction.OUTPUT_STATE));
        con.setConstructionModel();
        con.frame.setVisible(true);
    }
    
    public void showPreferenceModel() {
        setConnectingFields();
        log.setOldAttrData(LogUserAction.getDataOutput(chartData.getAttrData(), LogUserAction.OUTPUT_STATE));
        con.setPreferenceModel();
        con.frame.setVisible(true);
    }
    
    public boolean allowPreferenceModel() {
        if (con == null) return false;
        
        return con.allowPreferenceModel();
    }
    
    public boolean isPumpSelected() {
        return pump;
    }
    
    public boolean isSortSelected() {
    	return sort;
    }

    public int getDirectionSA() {
        return sa_dir;
    }
    
    public String getChartTitle(){
        if (chartTitle == null){
            return "__no title given__";
        } else {
            return chartTitle;
            
        }
    }

//SHOWS/FRAMES
    
    public boolean createReportController() {
        //entries is the number of entries/scenarios/alternatives

        //get the report location if associated with the entries

        controller = new SwingController();
        //From: http://greenxgene.blogspot.ca/2012/10/how-to-open-pdf-file-from-your-java.html

        //build a controller
        //SwingController controller = new SwingController(); //this is now a class variable
        //Above is now done at class level

        // Build a SwingViewFactory configured with the controller
        SwingViewBuilder factory = new SwingViewBuilder(controller);
        viewerComponentPanel = factory.buildViewerPanel();

        // add copy keyboard command
        ComponentKeyBinding.install(controller, viewerComponentPanel);

        // add interactive mouse link annotation support via callback
        controller.getDocumentViewController().setAnnotationCallback(
                new org.icepdf.ri.common.MyAnnotationCallback(
                controller.getDocumentViewController()));

        //Use the factory to build a JPanel that is pre-configured
        //with a complete, active Viewer UI.
        // Open a PDF document to view
        controller.openDocument(reportFile.toString());

        //Make if continuous view
        controller.setPageFitMode(org.icepdf.core.views.DocumentViewController.PAGE_FIT_WINDOW_WIDTH, false);
        controller.setPageViewMode(DocumentViewControllerImpl.ONE_COLUMN_VIEW, true);
        
        //Set the outline variable so its contents can be quickly searched
        outlines = controller.getDocument().getCatalog().getOutlines();

        //Now create the window
        reportWindow = new JFrame("Details on Criteria Used in Report"); //the windows created for the attribute, these are used to show the report. They are class variables because we need to toggle visibility from different methods
        reportWindow.getContentPane().add(viewerComponentPanel);
        reportWindow.pack();
        reportWindow.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE); //we only want to hide the window, not close it.
        reportWindow.setSize(new Dimension(800,600));
        //If it the swingworkes weren't already created, they are now!
        return true;
    }

    //if the bookmarkTitle exists in the report, then it will show the window, zoom to the location and return a value >= 0
    public Integer criteriaBookmarkExistsInReport(String bookmarkTitle) {

        //This method finds the associated bookmark
        if (controller != null) {
            //From: http://www.icesoft.org/JForum/posts/list/13433.page
            //Get the bookmarks (icepdf calls them outline items)


            if (outlines != null) {
                item = outlines.getRootOutlineItem();
            }
            if (item != null) {
                OutlineItemTreeNode outlineItemTreeNode = new OutlineItemTreeNode(item);
                outlineItemTreeNode.getChildCount();  // Added this line
                Enumeration depthFirst = outlineItemTreeNode.depthFirstEnumeration();
                // find the node you need


                //Loop through the bookmarks looking for a matching title
                //The index of the bookmark that we want to navigate to
                Integer bookMark = 0;
                Integer numDocBookmarks = item.getSubItemCount();
                String bookMarkName; //this is to store the name of the item, from outlines, which are the bookmarks that the computer sees in the PDF document

                //Bookmarks in the PDF tend to have spaces added onto the end of the name, so that is why the replace and trim methods are used
                while (bookMark < numDocBookmarks) {
                    bookMarkName = item.getSubItem(bookMark).getTitle().replace("?", "").trim();
                    //System.out.println("|" + bookMarkName + "/" + bookmarkTitle.toString() + "|");
                    if (bookMarkName.equals(bookmarkTitle.toString()) || bookMarkName.equals(bookmarkTitle.toString() + " ") || bookMarkName.equals(bookmarkTitle.toString().replace('_', ' ')) || bookMarkName.equals(bookmarkTitle.toString().replace('_', ' ') + " ")) {
                        break;
                    }
                    bookMark++;
                    //TODO: Return if bookmark not found
                }

                if (bookMark >= numDocBookmarks) {
                    //System.out.println("There is no bookmark in the document for the given attribute");
                    return -1;
                } else {
                    return bookMark;
                }
            } else {
                //the report does not exist anyway, so no controller was created, return -1
                return -1;
            }
        } else {
            //the report does not exist anyway, so no controller was created, return -1
            return -1;
        }
    }          
    
    //zoom the the location in the report
    public void zoomToReport(String bookmarkTitle) {
        //go to the location
        if (!reportWindow.isVisible()) {
            reportWindow.setVisible(true);
        }
        //go the the location in the document
        controller.followOutlineItem(item.getSubItem(criteriaBookmarkExistsInReport(bookmarkTitle)));
    }
    
    //allows other classes to verify if the report does exist
    public boolean reportExists() {
        if (reportFile == null) {
            return false;
        } else if (reportFile.exists()) {
            return true;
        } else {
            return false;
        }

    }
                    
    public void gotoBookmark(Integer bookMark) {
        //Since the window is initially hidden, or when the user closes the window is become hidden (not closed), we need to check this first
        //But only show the window if the request has been such

    }
    
    public ConstructionView getCon() {
        return con;
    }

    public DisplayPanel getDisplayPanel() {
        return displayPanel;
    }

    public void resetDisplay(int type, int colwd, boolean close, boolean graph) {
        ValueChart ch = new ValueChart(con, filename, log, chartData, type, colwd, true, graph, displayUtilityWeights);
        ch.showAbsoluteRatios = this.showAbsoluteRatios;
        ch.pump = pump;
        ch.sort = sort;
        ch.pump_increase = pump_increase;
        ch.sa_dir = sa_dir;
        ch.getDisplayPanel().setScore(getDisplayPanel().score);
        ch.getDisplayPanel().setRuler(getDisplayPanel().ruler);
        Vector<ChartEntry> entryList = chartData.getEntryList();
        for (int j = 0; j < entryList.size(); j++) {
            if (( entryList.get(j)).getShowFlag()) {
                ( (ch.chartData.getEntryList().get(j))).setShowFlag(true);
                ch.updateAll();
            }
        }
        closeChart();
    }

    public void compareDisplay(int type, int colwd) {
        ValueChart ch = new ValueChart(con, filename, log, type, colwd, false, show_graph, displayUtilityWeights); // TODO
        ch.showAbsoluteRatios = this.showAbsoluteRatios;
        ch.pump = pump;
        ch.sort = sort;
        ch.pump_increase = pump_increase;
        ch.sa_dir = sa_dir;
        ch.getDisplayPanel().setScore(getDisplayPanel().score);
        ch.getDisplayPanel().setRuler(getDisplayPanel().ruler);
        Vector<ChartEntry> entryList = chartData.getEntryList();
        for (int j = 0; j < entryList.size(); j++) {
            if (( entryList.get(j)).getShowFlag()) {
                ( (ch.chartData.getEntryList().get(j))).setShowFlag(true);
                ch.updateAll();
            }
        }
    }

    public void setDisplayType(int type) {
        displayType = type;
    }

    public void closeChart() {
        if (displayType == SEPARATE_DISPLAY) {
            dialog.dispose();
        }
        chartFrame.dispose();
    }

    void showVC() {
        chartFrame = new JFrame("ValueChart for " + chartTitle);
        chartFrame.getContentPane().setLayout(new BoxLayout(chartFrame.getContentPane(), BoxLayout.Y_AXIS));
        if (displayType == SIDE_DISPLAY) {
            pnlOpt = new SensitivityAnalysisOptions(this);
            chartFrame.getContentPane().add(pnlOpt);
        }
        chartFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        chartFrame.getContentPane().add(this);

        chartFrame.pack();
        chartFrame.setVisible(true);

    }

    private class DisplayDialog extends JDialog {

        private static final long serialVersionUID = 1L;

        public DisplayDialog(Frame aFrame, String title, JPanel panel) {
            super(aFrame, false);
            setTitle(title);
            setModal(false);
            setContentPane(panel);
            setDefaultCloseOperation(HIDE_ON_CLOSE);
            pack();
        }
    }

    private class ResizeHandler extends ComponentAdapter {

        public void componentResized(ComponentEvent e) {
            if (mainPane != null) {
                mainPane.updateSizesAndHeights();
                mainPane.revalidate();
                if (displayPanel != null) {
                    alignDisplayPanel();
                    displayPanel.revalidate();
                }
            }
        }
    }

    public JFrame getFrame() {
        return chartFrame;
    }

    public void showDomainVals(int i) {
        ChartEntry c =  chartData.getEntryList().get(i);
        pnlDom.showData(c);
    }

    
    void addInteraction(LastInteraction last) {
        undo_int.push(last);
        redo_int.clear();
        updateUndoRedoMenu();
    }
    
    void updateUndoRedoMenu() {
        if (!undo_int.isEmpty()) 
            menuOptions.menuUndo.setEnabled(true);
        else
            menuOptions.menuUndo.setEnabled(false);
        
        if (!redo_int.isEmpty())
            menuOptions.menuRedo.setEnabled(true);
        else
            menuOptions.menuRedo.setEnabled(false);
    }
    
    void undo() {
        LastInteraction undo = undo_int.pop();
        LastInteraction redo = new LastInteraction(this);
        undo.setRedo(redo);
        undo.execute();
        redo_int.push(redo);
        updateUndoRedoMenu();
    }
    
    void redo() {
        LastInteraction redo = redo_int.pop();
        LastInteraction undo = new LastInteraction(this);
        redo.setRedo(undo);
        redo.execute();
        undo_int.push(undo);
        updateUndoRedoMenu();
    }
    
    void renderAttribute(TablePane pane, AttributeData a) {
        BaseTableContainer container;
        double hr = a.getWeight();
        String name = a.getName();

        if (a.isAbstract()) {
            TablePane subpane = new TablePane();
            if (chartTitle == null) {
                // this is the first attribute level, so set the title of the
                // window to the main title of the ValueChart
                chartTitle = name.replace('_', ' ');
            }
            for (AttributeData c : a.getAbstract().getChildren()) {
                renderAttribute(subpane, c);
            }
            container = new BaseTableContainer(subpane, name, this, colWidth);
        } else {
            AttributePrimitiveData prim = a.getPrimitive();

            AttributeCell cell = new AttributeCell(this, a.getName());
            cell.setColWidth(colWidth);
            cell.setColor(prim.getColor());
            cell.setUnits(prim.getUnitsName());

            container = new BaseTableContainer(cell, name, this, colWidth);
        }

        pane.addRow(container);
        container.setHeightRatio(hr);
        container.setRollUpRatio(hr);
        container.setName(name);
    }

    void renderMainPaneAttributes() {
        for (AttributeData a : chartData.getAttrData()) {
            renderAttribute(mainPane, a);
        }
    }

    void updateMainPane() {
        for (BaseTableContainer b : mainPane.getRowList()) {
            b.updateSize();
        }
    }

    // TODO: to be adjusted, not currently accurate
    double getMainPaneHeight() {
        return mainPane.getHeight();
    }

    void rebuildMainPane() {
        mainPane.fillInEntries(chartData.getEntryList());

        mainPaneWithNames = new JPanel();
        mainPaneWithNames.setLayout(new BoxLayout(mainPaneWithNames, BoxLayout.Y_AXIS));

        mainEntryNames = new EntryNamePanel(chartData.getEntryList(), colWidth, mainPane.getDepth(), true, this);
        JPanel pnlBottom = new JPanel();
        pnlBottom.setLayout(new BoxLayout(pnlBottom, BoxLayout.X_AXIS));
        pnlBottom.add(Box.createHorizontalGlue());
        pnlBottom.add(mainEntryNames);

        mainPaneWithNames.add(mainPane);
        mainPaneWithNames.add(pnlBottom);

        removeAll();

        /*      if (displayType == SIDE_DISPLAY){ 
         setLayout (new BoxLayout (this, BoxLayout.X_AXIS));
         add(mainPaneWithNames);
         }
         else{ 
         setLayout (new BoxLayout (this, BoxLayout.Y_AXIS));
         add(mainPaneWithNames);
         if (displayType != SEPARATE_DISPLAY){ 
         add (Box.createVerticalStrut(colWidth));
         add (Box.createGlue());
         }
         }*/

        displayPanel = new DisplayPanel(colWidth);
        displayPanel.setRootPane(mainPane);
        displayPanel.setEntries(chartData.getEntryList());


        pnlDisp = new JPanel();
        pnlDisp.setLayout(new BoxLayout(pnlDisp, BoxLayout.X_AXIS));
        pnlDom = new DomainValues(this);
        if (displayType == SIDE_DISPLAY) {
            pnlDisp.add(Box.createHorizontalGlue());
        } else {
            pnlOpt = new SensitivityAnalysisOptions(this);
            pnlOpt.setAlignmentX(Component.LEFT_ALIGNMENT);
            pnlDisp.add(pnlOpt);
            //Note: subtracted 1 from mainPane depth, this might cause a problem if there is only one level.
            pnlOpt.setPreferredSize(new Dimension((mainPane.getDepth() - 1) * headerWidth - colWidth + (show_graph ? graphWidth : 0), displayHeight));
            pnlOpt.setMaximumSize(new Dimension((mainPane.getDepth() - 1) * headerWidth - colWidth, displayHeight));
        }
        pnlDisp.add(Box.createHorizontalGlue());
        pnlDisp.add(displayPanel);

        displayWithNames = new JPanel();
        displayWithNames.setLayout(new BoxLayout(displayWithNames, BoxLayout.Y_AXIS));
        displayEntryNames = new EntryNamePanel(chartData.getEntryList(), colWidth, 0, false, this);
        JPanel pnlNames = new JPanel();
        pnlNames.setLayout(new BoxLayout(pnlNames, BoxLayout.X_AXIS));
        pnlNames.add(Box.createHorizontalGlue());
        //pnlNames.add(displayEntryNames);
        pnlNames.add(Box.createRigidArea(new Dimension(colWidth, 0)));
        displayWithNames.add(Box.createVerticalGlue());
        displayWithNames.add(pnlDisp);
        displayWithNames.add(pnlNames);

        //int mainWidth = (entryList.size()+mainPane.getDepth())*colWidth;
        int mainWidth = chartData.getEntryList().size() * colWidth;
///**/  int dispWidth = entryList.size()*colWidth + colWidth;
        int dispWidth = chartData.getEntryList().size() * colWidth + colWidth;

        if (displayType == SIDE_DISPLAY) {
            displayPanel.setMaximumSize(new Dimension(dispWidth, 10000));
            displayPanel.setMinimumSize(new Dimension(dispWidth, 0));
            //mainPaneWithNames.setAlignmentY(0.0f);
        }

        graphWidth = (show_graph ? graphWidth : 0);

        mainPaneWithNames.setMaximumSize(new Dimension(mainWidth + ((mainPane.getDepth() -1) * headerWidth) + graphWidth, 10000));
        mainPaneWithNames.setMinimumSize(new Dimension(mainWidth + ((mainPane.getDepth() -1) * headerWidth) + graphWidth, 0));
        mainPaneWithNames.setPreferredSize(
                new Dimension(mainWidth + ((mainPane.getDepth() -1) * headerWidth) + graphWidth, displayType == SIDE_DISPLAY ? displayHeight * 2 - 50 : displayHeight));

        int w = ((displayType == SIDE_DISPLAY) ? mainWidth + 40 : dispWidth) + colWidth;//no real reason why it's 40

        if (displayType == SIDE_DISPLAY) {
            displayWithNames.setMaximumSize(new Dimension(w, 10000));
            displayWithNames.setMinimumSize(new Dimension(w, 0));
            displayWithNames.setPreferredSize(new Dimension(w, displayHeight * 2 - 50));
        } else {
            displayWithNames.setMaximumSize(new Dimension(w + (mainPane.getDepth() -1) * headerWidth + graphWidth, 10000));//- and rev x, y
            displayWithNames.setMinimumSize(new Dimension(w + (mainPane.getDepth() -1) * headerWidth + graphWidth, 0));//- and rev x, y
            displayWithNames.setPreferredSize(new Dimension(w + (mainPane.getDepth() -1) * headerWidth + graphWidth, displayHeight-50));//- and rev x, y
            // seems to work with -50 to get rid of unnecessary padding. TODO resize is weird
        }
        mainPane.updateSizesAndHeights();
        alignDisplayPanel();
        
        if (displayType == SEPARATE_DISPLAY) {
            dialog = new DisplayDialog(chartFrame, "Total Scores", displayWithNames);
        }
        if (displayType == DEFAULT_DISPLAY) {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            add(displayWithNames);
            add(Box.createRigidArea(new Dimension(0, 5)));
        }
        if (displayType == SIDE_DISPLAY) {
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            add(mainPaneWithNames);
            add(Box.createHorizontalStrut(40));//-
            add(Box.createGlue());
            add(displayWithNames);
        } else {
            add(mainPaneWithNames);
            if (displayType != SEPARATE_DISPLAY) {
                //add (Box.createVerticalStrut(40));
                //add (Box.createGlue());
            }
            add(Box.createVerticalStrut(10));
        }
    }
    

    
    public boolean logAttribute(int outputType, String msg) {
        if (outputType == LogUserAction.OUTPUT_WEIGHT) {
            return log.logAttributeWeight(chartData.getAttrData(), msg);
        }
        else if (outputType == LogUserAction.OUTPUT_STATE) {
            return log.logAttributeState(chartData.getAttrData(), msg);
        }

        return false;
    }
    
    public boolean logState() {
        int temp = log.getVerbosity();
        log.setVerbosity(LogUserAction.LOG_ALL);
        boolean res = log.logAttributeState(chartData.getAttrData(), "");
        log.setVerbosity(temp);
        return res;
    }
    
    public boolean logConstruction() {
        return log.logConstruction(chartData.getAttrData());
    }
    
    public boolean logUtility(AttributeData data) {
        return log.logUtility(data);
    }
    
    public boolean logPump(String pumpAttr, boolean isInc) {
        return log.logPump(chartData.getAttrData(), pumpAttr, isInc);
    }
    
    public boolean logDrag(AttributeData attr1, AttributeData attr2) {
        return log.logDrag(attr1, attr2);
    }
    
    public String outputAttributes(ColorList colors) {
        String data = "";
        for (AttributeData a : chartData.getAttrData()) {
            data = data + a.getObjectiveOutput(colors, 0);
        }
        return data;
    }
    
    public boolean logString(String str, String msg) {
        return log.log(str, msg);
    }
    
    public void setLogOldAttributeData(String str) {
        log.setOldAttrData(str);
    }
    
    public void setLogVerbosity(int v) {
        log.setVerbosity(v);
    }

    public LogUserAction getLog() {
        return log;
    }

    public void setLog(LogUserAction log) {
        this.log = log;
    }
    
    public void updateHeaders() {
        if (mainPane == null) return;
        Iterator<BaseTableContainer> it;
        for (it = mainPane.getRows(); it.hasNext();) {
            BaseTableContainer btc = it.next();
            btc.updateHeadersRecursively();
        }
    }
    
    public void alignDisplayPanel() {
        if (displayPanel == null) return;
        displayPanel.setPrefHeight(mainPane.getHeight());
        displayPanel.setMaximumSize(displayPanel.getPreferredSize());
        displayPanel.setMinimumSize(displayPanel.getPreferredSize());
        pnlDisp.setPreferredSize(mainPane.getSize());
        pnlDisp.setMaximumSize(mainPane.getSize());
    }
    

    
    public void restoreState(boolean refreshDisp) {     
        chartData.restoreState();
        if (refreshDisp)
            updateChartState();
    }
    
    public void updateChartState() {
        readFromAttribute();
        setSize(getPreferredWidth(),
                displayType == SIDE_DISPLAY ? displayHeight * 2 - 50
                        : displayHeight);
        setVisible(true);
        if (mainPane != null) {
            mainPane.updateSizesAndHeights();
            mainPane.revalidate();
            if (displayPanel != null) {
                alignDisplayPanel();
                displayPanel.revalidate();
            }
        }
        newConst();
        repaint();
    }
    
    public Vector<AttributeData> getAttrData() {
        return chartData.getAttrData();
    }
    
    public Vector<ChartEntry> getEntryList() {
        return chartData.getEntryList();
    }
    
    public AttributeData getAttribute(String name) {
        return chartData.findAttribute(name);
    }
    
    public ChartEntry getEntry(String name) {
        return chartData.findEntry(name);
    }
    
    public AttributeDomain getDomain(String name) {
        AttributeData data = getAttribute(name);
        if (data != null && !data.isAbstract()) {
            return data.getPrimitive().getDomain();
        }
        return null;
    }

    public void keepCurrentState() {
        chartData.keepCurrentState(this);
    }
}
