import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.StreamTokenizer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.*;

public class ConstructionView extends JPanel implements ChangeListener, ActionListener{

	private static final long serialVersionUID = 1L;
	static final int 	NEW_FILE = 1,
						FROM_DATAFILE = 2,
						FROM_VC = 3;
	static final int	DEFAULT_DISPLAY = 1,
						SIDE_DISPLAY = 2,
						SEPARATE_DISPLAY = 3;
	static final String TAB_OBJECTIVES = "Objectives",
	                    TAB_ALTERNATIVES = "Alternatives",
	                    TAB_VALUES = "Values",
	                    TAB_SMARTER = "SMARTER",
	                    TAB_WEIGHTING = "Weighting";

	int type = 1;
	ValueChart chart;

    JDialog frame;
    JTabbedPane constPane;
    JPanel pnlButtons;
    JButton btnOK;
    JButton btnCancel;
    private DefineObjectivesPanel pnlObjectives;
    private DefineAlternativesPanel pnlAlternatives;
    private DefineValueFunction pnlValueFunction;
    private DefineInitialWeights pnlWeighting;
    private WeightingBySMARTER pnlSMARTER;
    JObjective lblRoot;
    JPopupMenu popObjective;

    String filename = "test.vc"; // data file name
    String data; // main data string: all data for data file
    ColorList colors; // vc primitive objective colors
    Vector<JObjective> obj_list; // can represent all possible objectives (columns of table)
    Vector<HashMap<String,Object>> alts; // data (rows of table)
    private String tempUser;

    int display_type = DEFAULT_DISPLAY;
    boolean abs = true;
    int rowht = 16;

    int entry_count;
    boolean vf_flag = true; // flag to determine if value functions are set
    boolean init = true;

    // Root Objective placement/dimensions: Will determine overall tree view
    static final int OBJ_X = 50, OBJ_Y = 15, OBJ_WIDTH = 500, OBJ_HEIGHT = 30;

    public ConstructionView(int i, String user) {
        tempUser = user.replaceAll("[^a-zA-Z0-9]+", "");
        chart = null; // at first there will be no chart attached
        type = i;
        obj_list = new Vector<JObjective>();
        alts = new Vector<HashMap<String,Object>>();

        // Set up the Tabbed Panes
        constPane = new JTabbedPane(JTabbedPane.TOP);
        constPane.addChangeListener(this);
        constPane.setPreferredSize(new Dimension(600, 400));
        pnlObjectives = new DefineObjectivesPanel(type, this);
        constPane.addTab(TAB_OBJECTIVES, pnlObjectives);

        pnlAlternatives = new DefineAlternativesPanel(this);
        constPane.addTab(TAB_ALTERNATIVES, pnlAlternatives);
        pnlValueFunction = new DefineValueFunction(this);
        constPane.addTab(TAB_VALUES, pnlValueFunction);
        pnlWeighting = new DefineInitialWeights(this);
        pnlSMARTER = new WeightingBySMARTER(this, true);
        constPane.addTab(TAB_SMARTER, pnlSMARTER);
        constPane.addTab(TAB_WEIGHTING, pnlWeighting);

        // disable other tabs at first
        constPane.setEnabledAt(1, false);
        constPane.setEnabledAt(2, false);
        constPane.setEnabledAt(3, false);
        constPane.setEnabledAt(4, false);

        // Set up the command buttons
        btnOK = new JButton("Update Chart");
        btnOK.setActionCommand("btnOK");
        btnOK.addActionListener(this);
        btnOK.setEnabled(false);
        btnCancel = new JButton("Cancel");
        btnCancel.setActionCommand("btnCancel");
        btnCancel.addActionListener(this);

        pnlButtons = new JPanel();
        pnlButtons.setLayout(new BoxLayout(pnlButtons, BoxLayout.LINE_AXIS));
        pnlButtons.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        pnlButtons.add(Box.createHorizontalGlue());
        pnlButtons.add(btnOK);
        pnlButtons.add(Box.createRigidArea(new Dimension(10, 0)));
        pnlButtons.add(btnCancel);

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        add(constPane);
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(pnlButtons);

        if (type == FROM_DATAFILE)
            readFile(ValueChartsPlus.datafilename);
        showConstructionView();
        pnlObjectives.setSize(getWidth(), getHeight());
        pnlObjectives.setInitialComponents();
    }

    public DefineObjectivesPanel getObjPanel() {
        return pnlObjectives;
    }

    public DefineValueFunction getValPanel() {
        return pnlValueFunction;
    }

    public DefineAlternativesPanel getAltPanel() {
        return pnlAlternatives;
    }

    public DefineInitialWeights getWeightPanel() {
        return pnlWeighting;
    }

    public void stateChanged(ChangeEvent ce) {
        JTabbedPane pane = (JTabbedPane) ce.getSource();
        int sel = pane.getSelectedIndex();
        if (btnOK != null)
            btnOK.setVisible(true);
        if (btnCancel != null)
            btnCancel.setVisible(true);

        validateTabs();
        
        // initial construction
        if (pane.getTabCount() > 3) {
            switch (sel) {
            case 1: {
                if (pnlAlternatives != null)
                    pnlAlternatives.updateTable();
                break;
            }
            case 2: {
                pnlValueFunction.repaintDisplay();
                break;
            }
            case 3: {
                if (btnOK != null)
                    btnOK.setVisible(false);
                if (btnCancel != null)
                    btnCancel.setVisible(false);
                
                if (pnlValueFunction.checkAllUtility(true)) {
                    pnlWeighting.setObjectiveList();
                    String problem  = "";
                    if (chart != null) problem = chart.getChartTitle();
                    pnlSMARTER.startWeighting(problem);
                }
                break;
            }
            case 4: {
                if (pnlValueFunction.checkAllUtility(true))
                    pnlWeighting.setObjectiveList();
                break;
            }
            }
        }
        // preference model
        else {
           String title = pane.getTitleAt(sel);
            if (title.equals(TAB_VALUES)) {
                pnlValueFunction.repaintDisplay();
            } else if (title.equals(TAB_SMARTER)) {
                if (btnOK != null)
                    btnOK.setVisible(false);
                if (btnCancel != null)
                    btnCancel.setVisible(false);
                
                pnlWeighting.setObjectiveList();
                String problem  = "";
                if (chart != null) problem = chart.getChartTitle();
                pnlSMARTER.startWeighting(problem);
            } else if (title.equals(TAB_WEIGHTING)) {
                pnlWeighting.setObjectiveList();
            }
        }

    }

    public void actionPerformed(ActionEvent e) {
        if ("btnCancel".equals(e.getActionCommand()))
            if (chart == null) // construction
                System.exit(0);
            else {
                frame.dispose(); // edit view
                if (chart != null) {
                    chart.restoreState(true);
                }
            }
        else if ("btnOK".equals(e.getActionCommand())) {
            frame.setVisible(false);
            createDataFile("test.vc", false);
            if (pnlAlternatives.alts.size() > 10)
                display_type = SIDE_DISPLAY;
            showChart();
            chart.logConstruction();
        }
    }

    public void showChart() {
        if (chart != null)
            chart.closeChart();
        LogUserAction log = null;
        boolean dispUtil = false;
        if (chart != null) {
            log = chart.getLog();
            dispUtil = chart.displayUtilityWeights;
        }
        chart = new ValueChart(this, filename, log, tempUser, ValueChart.DEFAULT_DISPLAY,
                ValueChart.DEFAULT_COL_WIDTH, true, true, dispUtil); // TODO
        chart.newConst();
    }

    public void setRowHeight(int ht) {
        rowht = ht;
    }

    public void setChart(ValueChart ch) {
        chart = ch;
    }

    public void setDisplayType(int type) {
        display_type = type;
    }

    public void showConstructionView() {
        // Create and set up the window.
        frame = new JDialog(ValueChartsPlus.frame,
                "ValueCharts - Model Construction");
        frame.setModal(true);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        WindowListener exitListener = new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                if (chart != null) {
                    chart.restoreState(true);
                }
            }
        };
        frame.addWindowListener(exitListener);
        
        frame.getContentPane().add(this, BorderLayout.CENTER);
        frame.pack();
        // ValueChart.createMenu();
        // frame.setJMenuBar(ValueChart.menubar);
        if (type == FROM_VC)
            frame.setVisible(false);
        else 
            frame.setVisible(true);
    }

    public void setInit(boolean i) {
        init = i;
    }

    public void createDataFile(String fname, boolean save) {
        data = ""; // initialize new data string
        colors = new ColorList();
        int countblank = 0;
        for (int i = 0; i < pnlObjectives.prim_obj.size(); i++) {
            JObjective obj = pnlObjectives.prim_obj.get(i);
            data = data + "color " + obj.getName() + " ";
            // if the obj already has a color assigned to it...
            if (obj.color != Color.WHITE) {
                data = data + (float) obj.color.getRed() / 255 + " "
                        + (float) obj.color.getGreen() / 255 + " "
                        + (float) obj.color.getBlue() / 255 + "\n";
            }
            // if not, assign one
            else {
                data = data + colors.getColorCode(countblank);
                countblank++;
            }

            // else if (countblank < 10){
            // colors.getColorEntry(i);
            // data = data + "color " + colors.getColorEntry(i) + "\n";
            // }
            // if (i<10){
            // colors.getColorEntry(i);
            // data = data + "color " + colors.getColorEntry(i) + "\n";
            // }
        }

        data = data + "\n\nattributes\n";
        if (!save) {
            data = data + pnlObjectives.getObjectiveOutput(colors);
        } else {
            if (chart != null) {
                data = data + chart.outputAttributes(colors);
            }
        }
        data = data + "end\n";

        data = data + pnlAlternatives.getAlternativeOutput();

        try {
            FileWriter fw = new FileWriter(fname);
            fw.write(data);
            fw.close();
        } catch (Exception ex) {
            System.out.println("Exception: " + ex);
        }
    }

    public void setFileName(String f) {
        filename = f;
    }

    public void readFile(String str) {
        JObjective obj = null;
        boolean unit = false;
        try {
            FileReader fr = new FileReader(str);
            BufferedReader br = new BufferedReader(fr);
            StreamTokenizer st = new StreamTokenizer(br);
            st.whitespaceChars(44, 44);
            int count2 = 0; // running count to add alternative data
            int last_line = 1;
            HashMap<String,Object> alt_data = null;
            while (st.nextToken() != StreamTokenizer.TT_EOF) {
                // read first line into objective list
                if (st.lineno() == 1) {

                    if (st.ttype == StreamTokenizer.TT_WORD) {
                        // if = unit set flag to true then have anotherit that
                        // processes i flag is true then set flag to false
                        // can also determine domain here
                        if (unit) {
                            obj.setUnit(st.sval);
                            obj.setDomainType(AttributeDomainType.CONTINUOUS);
                            unit = false;
                        } else if ((st.sval).equals("unit")) {
                            unit = true;
                        } else {
                            obj = new JObjective(st.sval);
                            obj.setDomainType(AttributeDomainType.DISCRETE); // default to
                                                              // discrete,
                                                              // change to
                                                              // continuous if
                                                              // unit found
                            obj.origin = JObjective.FROM_FILE;
                            obj_list.add(obj);
                        }
                    }
                }

                // read second line to determine domain type
                else {
                    if (last_line != st.lineno()) {
                        if (st.lineno() != 2)
                            alts.add(alt_data);
                        alt_data = new HashMap<String,Object>();
                        count2 = 0;
                        last_line = st.lineno();
                    }
                    JObjective temp = obj_list.get(count2);
                    if (temp.getDomainType() == AttributeDomainType.DISCRETE)
                        alt_data.put(temp.getName(), st.sval);
                    else {
                        alt_data.put(temp.getName(), Double.valueOf(st.nval));
                    }

                    count2++;
                }
            }
            alts.add(alt_data); // add last alternative
            // for debugging
            // for (int i=0; i<alts.size(); i++)
            // System.out.println("alts HERE" + alts.get(i));
            fr.close();
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }
        pnlObjectives.setFileObjectives(obj_list);
        pnlAlternatives.setFileAlternatives(alts);

    }
    
    
    // returns true if Objectives tab valid
    public boolean checkObjectiveValid(){
        pnlObjectives.setPrimitiveObjectives();
        Vector<JObjective> prim_obj = pnlObjectives.getPrimitiveObjectives();
        
        if (prim_obj.size()<2 || !pnlObjectives.ok)
            return false;

        return true;
    }
    
    // returns true if Alternative panel valid
    public boolean checkAlternativeValid(){
        int num_alts = pnlAlternatives.num_alts;
        if ((num_alts<2) || !pnlAlternatives.checkFields()){//- last part
            return false;
        }

        return true;
    }
    
    // returns true if weights all defined properly
    public boolean checkWeightsValid() {
        Vector<JObjective> prim_obj = pnlObjectives.getPrimitiveObjectives();

        double weights=0.0;
        for (Iterator<JObjective> it = prim_obj.iterator(); it.hasNext();){
            JObjective obj = it.next();
            if (!obj.getWeight().equals("*"))
                weights += obj.getWeightNumeric();          
        }
        if (weights <= 0.98 || weights >= 1.02){
            return false;
        }
        return true;
    }
    
    // TODO possible rename?
    public void setConstructionModel() {
        if (constPane == null || pnlObjectives == null || pnlAlternatives == null)
            return;
        
        // keep old data so cancel will restore previous settings
        if (chart != null)
            chart.keepCurrentState();
        
        constPane.removeAll();
        
        constPane.addTab(TAB_OBJECTIVES, pnlObjectives);
        constPane.addTab(TAB_ALTERNATIVES, pnlAlternatives);
        constPane.addTab(TAB_VALUES, pnlValueFunction);
        constPane.addTab(TAB_SMARTER, pnlSMARTER);
        constPane.addTab(TAB_WEIGHTING, pnlWeighting);
        
        validateTabs();
        constPane.setSelectedIndex(0);
    }
    
    public void setPreferenceModel() {
        if (constPane == null || pnlObjectives == null || pnlAlternatives == null)
            return;
        
        // keep old data so cancel will restore previous settings
        if (chart != null)
            chart.keepCurrentState();
        
        constPane.removeAll();        
        
        constPane.addTab(TAB_VALUES, pnlValueFunction);
        constPane.addTab(TAB_SMARTER, pnlSMARTER);
        constPane.addTab(TAB_WEIGHTING, pnlWeighting);
        
        validateTabs();
        constPane.setSelectedIndex(0);
    }
    
    public boolean allowPreferenceModel() {
        return (checkObjectiveValid() && checkAlternativeValid());
    }
    
    public void gotoWeighting() {
        constPane.setSelectedIndex(constPane.getTabCount()-1);
    }
    
    public void validateTabs() {
        if (constPane == null || frame == null || !frame.isVisible()) 
            return;
        
        // construction
        if (constPane.getTabCount() > 3) {
            constPane.setEnabledAt(1, false);
            constPane.setEnabledAt(2, false);
            constPane.setEnabledAt(3, false);
            constPane.setEnabledAt(4, false);
            
            if (checkObjectiveValid()) {
                constPane.setEnabledAt(1, true);
                if (checkAlternativeValid()) {
                    constPane.setEnabledAt(2, true);
                    if (pnlValueFunction.checkAllUtility(false)) {
                        constPane.setEnabledAt(3, true);
                        constPane.setEnabledAt(4, true);
                        if (checkWeightsValid()) {
                            btnOK.setEnabled(true);
                            return;
                        }
                    }
                }
            }
            btnOK.setEnabled(false);
        }
        // preference model
        else {
            if (constPane.getTabCount() < 3) return;

            constPane.setEnabledAt(0, false);
            constPane.setEnabledAt(1, false);
            constPane.setEnabledAt(2, false);
            
            if (checkAlternativeValid()){
                constPane.setEnabledAt(0, true);
                if (pnlValueFunction.checkAllUtility(false)) {
                   constPane.setEnabledAt(1, true);
                   constPane.setEnabledAt(2, true);
                   if (checkWeightsValid()) {
                       btnOK.setEnabled(true);
                       return;
                   }
                }
                    
            }
            btnOK.setEnabled(false);
        }
    }
}
