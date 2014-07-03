import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

/**
 * Table of alternative information. By default, display indexed legend of all alternative names.
 * If alternative clicked, displays summary details for alternative.
 * 
 * <p><img src="doc-files/DetailsViewPanel.png" /></p>
 */
class DetailsViewPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    JLabel lblAlt;
    JTable table;
    DefaultTableModel tabModel;
    Vector<Vector<String>> rows;
    Vector<String> colsTwo;
    Vector<String> colsOne;
    Vector<String> data;
    String sel; // string of selected objective
    ValueChart chart;

    int width = 400;
    int height = 200;
    Font entryFont = new Font("Arial", Font.PLAIN, 12);
    Font altFont = new Font("Verdana", Font.PLAIN, 13);
    boolean isAlternative = true;

    DetailsViewPanel(ValueChart chart) {
        this.chart = chart;
        colsTwo = new Vector<String>();
        colsTwo.add(" ");
        colsTwo.add(" ");
        colsOne = new Vector<String>();
        colsOne.add(" ");
        rows = new Vector<Vector<String>>();
        data = new Vector<String>();

        lblAlt = new JLabel(" ");
        // set up table model and component
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        lblAlt.setPreferredSize(new Dimension(getWidth(), lblAlt.getHeight()));
        add(lblAlt);
        
        tabModel = new DefaultTableModel() {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                // all cells false
                return false;
            }
        };
        tabModel.setDataVector(rows, colsTwo);
        table = new JTable(tabModel) {
            @Override
            public Class<?> getColumnClass(int column) {
                return String.class;
            }
        };
        table.setPreferredScrollableViewportSize(new Dimension(width - 10, height));
        table.setRowSelectionAllowed(true);
        table.setGridColor(Color.WHITE);
        
        table.setTableHeader(null);
        table.setAutoCreateColumnsFromModel(false);
        TableColumnModel columns = table.getColumnModel();
        columns.getColumn(1).setCellRenderer(new WrapCellRenderer());

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane);
        scrollPane.setPreferredSize(new Dimension(width + 50, height + 50));
        scrollPane.setMaximumSize(new Dimension(width + 50, height + 50));

        showAlternativeLegend();
    }

    // display single alternative information
    public void showData(ChartEntry entry) {
        isAlternative = false;
        table.setFont(entryFont);

        TableColumnModel cModel = table.getColumnModel();
        cModel.getColumn(0).setPreferredWidth(120);
        cModel.getColumn(1).setPreferredWidth(width - 120);

        rows.clear();
        Vector<BaseTableContainer> prims = chart.getPrims();
        setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0), entry.name, 0, 0));
        for (Iterator<BaseTableContainer> it = prims.iterator(); it.hasNext();) {
            data = new Vector<String>();
            BaseTableContainer base = it.next();
            String s = (base.getName());
            AttributeValue val = entry.attributeValue(s);
            data.add(s);
            String u = ((AttributeCell) base.table).getUnits();
            data.add(val.stringValue() + (u == null || u.equals("") ? " " : " " + u));
            rows.add(data);
        }
        if (entry.hasDescription()) {
            data = new Vector<String>();
            data.add("Description");
            data.add(entry.getDescription());
            rows.add(data);
        }

        tabModel.fireTableStructureChanged();
        repaint();

    }

    public void showAlternativeLegend() {
        isAlternative = true;
        table.setFont(altFont);

        TableColumnModel cModel = table.getColumnModel();
        cModel.getColumn(0).setPreferredWidth(40);
        cModel.getColumn(1).setPreferredWidth(width - 40);

        rows.clear();
        Vector<ChartEntry> entryList = chart.getEntryList();
        setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0), "Alternatives", 0,0));
        if (entryList == null)
            return;
        for (int i = 0; i < entryList.size(); i++) {
            data = new Vector<String>();
            data.add("(" + (i + 1) + ")");
            data.add(entryList.get(i).name);
            rows.add(data);
        }

        tabModel.fireTableStructureChanged();
        repaint();
    }

    public void highLightAlternative(int idx) {
        if (!isAlternative || idx < 0)
            return;
        table.clearSelection();
        table.getSelectionModel().setSelectionInterval(idx, idx);
    }

    public Dimension getPreferredSize() {
        return (new Dimension(width, height));
    }

    public class WrapCellRenderer extends JTextArea implements TableCellRenderer {
        private static final long serialVersionUID = 1L;

        public WrapCellRenderer() {
            setLineWrap(true);
            setWrapStyleWord(true);
        }
        
        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            // wrap text 
            setText(value.toString());
            setSize(table.getColumnModel().getColumn(column).getWidth(), getPreferredSize().height);
            if (table.getRowHeight(row) != getPreferredSize().height) {
                table.setRowHeight(row, getPreferredSize().height);
            }

            // selection 
            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                setBackground(table.getBackground());
                setForeground(table.getForeground());
            }
            
            // add padding
            setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
            return this;
        }
    }
}