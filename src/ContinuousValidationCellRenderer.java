import java.awt.Color;
import java.awt.Component;
import java.util.HashSet;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

    public class ContinuousValidationCellRenderer extends DefaultTableCellRenderer implements ValidationCellRenderer {
        private static final long serialVersionUID = 1L;
        public HashSet<Integer> invalidValue = new HashSet<Integer>();

        public ContinuousValidationCellRenderer() {
            super();
        }
        
        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            
            Component rendererComp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
                    row, column);
            
            if (value.toString().isEmpty() || !DefineAlternativesPanel.isNumeric(value.toString())) {
                // selection 
                if (isSelected) {
                    rendererComp.setBackground(new Color(103,0,13));
                } else {
                    rendererComp.setBackground(new Color(222,45,38));
                }
                rendererComp.setForeground(Color.white);
                invalidValue.add(row);
            } else {
                if (isSelected) {
                    rendererComp.setBackground(table.getSelectionBackground());
                    rendererComp.setForeground(table.getSelectionForeground());
                } else {
                    rendererComp.setBackground(table.getBackground());
                    rendererComp.setForeground(table.getForeground());
                }
                invalidValue.remove(row);
            }
            
            return this;
        }

        @Override
        public boolean valuesValid() {
            return invalidValue.isEmpty();
        }
    }
