import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.StreamTokenizer;
import java.util.HashMap;
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
    JObjective lblRoot;
    JPopupMenu popObjective;

    String filename = "test.vc"; // data file name
    String data; // main data string: all data for data file
    Vector colors; // vc primitive objective colors
    Vector obj_list; // can represent all possible objectives (columns of table)
    Vector alts; // data (rows of table)

    int display_type = DEFAULT_DISPLAY;
    boolean abs = true;
    int rowht = 16;

    int entry_count;
    boolean vf_flag = true; // flag to determine if value functions are set
    boolean init = true;

    // Root Objective placement/dimensions: Will determine overall tree view
    static final int OBJ_X = 50, OBJ_Y = 15, OBJ_WIDTH = 500, OBJ_HEIGHT = 30;

    public ConstructionView(int i) {
        chart = null; // at first there will be no chart attached
        type = i;
        obj_list = new Vector();
        alts = new Vector();

        // Set up the Tabbed Panes
        constPane = new JTabbedPane(JTabbedPane.TOP);
        constPane.addChangeListener(this);
        constPane.setPreferredSize(new Dimension(600, 400));
        pnlObjectives = new DefineObjectivesPanel(type, this);
        constPane.addTab("Objectives", pnlObjectives);

        pnlAlternatives = new DefineAlternativesPanel(this);
        constPane.addTab("Alternatives", pnlAlternatives);
        pnlValueFunction = new DefineValueFunction(this);
        constPane.addTab("Values", pnlValueFunction);
        pnlWeighting = new DefineInitialWeights(this);
        constPane.addTab("Weighting", pnlWeighting);

        // disable other tabs at first
        constPane.setEnabledAt(1, false);
        constPane.setEnabledAt(2, false);
        constPane.setEnabledAt(3, false);

        // Set up the command buttons
        btnOK = new JButton("    OK    ");
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

    public void stateChanged(ChangeEvent ce) {
        JTabbedPane pane = (JTabbedPane) ce.getSource();
        int sel = pane.getSelectedIndex();
        switch (sel) {
        case 0:
            // Nothing will really need to be done b/c objs drive the data
        case 1: {
            repaint();
            break;
        }
        case 2: {
            if (pnlAlternatives.checkFields()) {
                if (vf_flag)
                    pnlValueFunction.repaintDisplay();
            } else
                pane.setSelectedIndex(1);
            break;
        }
        case 3: {
            if (pnlValueFunction.checkAllUtility())
                pnlWeighting.setObjectiveList();
            else {
                vf_flag = false;
                pane.setSelectedIndex(2);
                vf_flag = true;
            }
            break;
        }
        }

    }

    public void actionPerformed(ActionEvent e) {
        if ("btnCancel".equals(e.getActionCommand()))
            if (chart == null) // construction
                System.exit(0);
            else {
                frame.dispose(); // edit view
                chart.newConst();
            }
        else if ("btnOK".equals(e.getActionCommand())) {
            frame.setVisible(false);
            createDataFile("test.vc");
            if (pnlAlternatives.alts.size() > 10)
                display_type = SIDE_DISPLAY;
            showChart(true);
        }
    }

    public void showChart(boolean fromCon) {
        if (fromCon)
            if (chart != null)
                chart.closeChart();
        chart = new ValueChart(this, filename, ValueChart.DEFAULT_DISPLAY,
                ValueChart.DEFAULT_COL_WIDTH, true, true); // TODO
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
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
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

    public void createDataFile(String fname) {
        data = ""; // initialize new data string
        ColorList colors = new ColorList();
        int countblank = 0;
        for (int i = 0; i < pnlObjectives.prim_obj.size(); i++) {
            JObjective obj = (JObjective) pnlObjectives.prim_obj.get(i);
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
        // data = data + pnlObjectives.getObjectiveOutput(colors);
        if (chart != null) {
            for (AttributeData a : chart.attrData) {
                data = data + a.getObjectiveOutput(colors, 0);
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
            HashMap alt_data = null;
            while (st.nextToken() != StreamTokenizer.TT_EOF) {
                // read first line into objective list
                if (st.lineno() == 1) {

                    if (st.ttype == StreamTokenizer.TT_WORD) {
                        // if = unit set flag to true then have anotherit that
                        // processes i flag is true then set flag to false
                        // can also determine domain here
                        if (unit) {
                            obj.setUnit(st.sval);
                            obj.setType(JObjective.CONTINUOUS);
                            unit = false;
                        } else if ((st.sval).equals("unit")) {
                            unit = true;
                        } else {
                            obj = new JObjective(st.sval);
                            obj.setType(JObjective.DISCRETE); // default to
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
                        alt_data = new HashMap();
                        count2 = 0;
                        last_line = st.lineno();
                    }
                    JObjective temp = (JObjective) obj_list.get(count2);
                    if (temp.getType() == 1)
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
        pnlAlternatives.setFileAlternatives(obj_list, alts);

    }
}
