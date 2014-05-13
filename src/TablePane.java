
import java.awt.*;

import javax.swing.*;

import java.util.*;

public class TablePane extends JLayeredPane {

    private static final long serialVersionUID = 1L;
    private Vector<BaseTableContainer> rowList = new Vector<BaseTableContainer>(10);

    private int[] computeRowHeights(int totalHeight) {
        int[] heights = new int[rowList.size()];
        int i = 0;
        double rsum = 0; 	//ratiosum
        int wsum = 0;		//weightsum
        for (Iterator<BaseTableContainer> it = rowList.iterator(); it.hasNext();) {
            double r = it.next().getHeightRatio();
            if (r < 0) {
                return null;
            }
            rsum += r;
            heights[i] = (int) Math.round(rsum * totalHeight) - wsum;
            wsum += heights[i++];
        }
        return heights;
    }

    public TablePane() {
        setLayout(null);
    }

    public void setSize(Dimension dim) {
        setSize(dim.width, dim.height);
    }

    public void setExactSize(int w, int h) {
        super.setSize(w, h);
    }

    public void setSize(int w, int h) {
        super.setSize(w, h);
        int[] heights = computeRowHeights(h);
        if (heights != null) {
            int i = 0;
            int y = 0;
            for (Iterator<BaseTableContainer> it = rowList.iterator(); it.hasNext();) {
                BaseTableContainer comp = it.next();
                comp.setSize(w, heights[i]);
                y += heights[i];
                comp.setLocation(comp.getX(), getHeight() - y);
                i++;
            }
        }
    }

    public int getDepth() {
        int maxsubdepth = 0;
        for (Iterator<BaseTableContainer> it = rowList.iterator(); it.hasNext();) {
            BaseTableContainer comp = it.next();
            if (comp.getTable() instanceof TablePane) {
                int subdepth = ((TablePane) comp.getTable()).getDepth();
                if (subdepth > maxsubdepth) {
                    maxsubdepth = subdepth;
                }
            }
        }
        return maxsubdepth + 1;
    }

    //**********S/B getRows()!!!!
    public Iterator<BaseTableContainer> getRows() {
        return rowList.iterator();
    }
    
    public Vector<BaseTableContainer> getRowList() {
        return rowList;
    }

    public BaseTableContainer getRowAt(int i) {
        if (i >= 0 && i < getRowList().size())
            return getRowList().get(i);
        else
            return null;
    }
    
    public BaseTableContainer getRowLast() {
        if (getRowList().isEmpty())
            return null;
        else
            return getRowList().get(getRowList().size()-1);
    }

    public void adjustAttributesForDepth(int d) {
        for (Iterator<BaseTableContainer> it = rowList.iterator(); it.hasNext();) {
            BaseTableContainer comp = it.next();
            if (comp.getTable() instanceof TablePane) {
                ((TablePane) comp.getTable()).adjustAttributesForDepth(d - 1);
            } else {
                comp.adjustHeaderForDepth(d);
            }
        }
    }

    public boolean hasAttribute(String name) {
        for (Iterator<BaseTableContainer> it = rowList.iterator(); it.hasNext();) {
            BaseTableContainer comp = it.next();
            if (comp.getTable() instanceof TablePane) {
                if (((TablePane) comp.getTable()).hasAttribute(name)) {
                    return true;
                }
            } else {
                if (comp.getName().equals(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    public AttributeCell getAttributeCell(String name) {
        AttributeCell cell;
        for (Iterator<BaseTableContainer> it = rowList.iterator(); it.hasNext();) {
            BaseTableContainer comp = it.next();
            if (comp.getTable() instanceof TablePane) {
                cell = ((TablePane) comp.getTable()).getAttributeCell(name);
                if (cell != null) {
                    return cell;
                }
            } else {
                if (comp.getName().equals(name)) {
                    return (AttributeCell) comp.getTable();
                }
            }
        }
        return null;
    }

    //-finds the primitive objectives to set alternative entries
    public void fillInEntries(Vector entryList) {
        for (Iterator<BaseTableContainer> it = rowList.iterator(); it.hasNext();) {	//-for each objective
            BaseTableContainer comp = it.next();//-iterate through each basetablecontainer
            if (comp.getTable() instanceof TablePane) //-if it is an abstract objective, keep drilling down
            {
                ((TablePane) comp.getTable()).fillInEntries(entryList);
            } else {
                AttributeCell cell = (AttributeCell) comp.getTable();
                cell.setEntryList(comp.getName(), entryList);		//-set values for entries...
            }
        }
    }

    public void repaintEntries() {
        for (Iterator<BaseTableContainer> it = rowList.iterator(); it.hasNext();) {
            BaseTableContainer comp = it.next();
            if (comp.getTable() instanceof TablePane) {
                ((TablePane) comp.getTable()).repaintEntries();
            } else {
                ((AttributeCell) comp.getTable()).repaint();
            }
        }
    }

    public void addRow(BaseTableContainer comp) {
        super.add(comp);
        rowList.add(comp);
    }

    public BaseTableContainer nextRow(BaseTableContainer comp) {
        int idx = rowList.indexOf(comp);
        if (idx > 0) {
            return (BaseTableContainer) rowList.get(idx - 1);
        } else {
            return null;
        }
    }

    public BaseTableContainer prevRow(BaseTableContainer comp) {
        int idx = rowList.indexOf(comp);
        if (idx < rowList.size() - 1) {
            return rowList.get(idx + 1);
        } else {
            return null;
        }
    }

    public void swapRows(BaseTableContainer comp, BaseTableContainer side) {
        int compIdx = rowList.indexOf(comp);
        int sideIdx = rowList.indexOf(side);
        if (compIdx == sideIdx) {
            return;
        }
        Point sideLoc = side.getLocation();
        if (compIdx < sideIdx) {
            sideLoc.y += comp.getHeight();
        } else {
            sideLoc.y -= comp.getHeight();
        }
        side.setLocation(sideLoc);
        rowList.set(sideIdx, comp);
        rowList.set(compIdx, side);
    }

    //*********** NEEDS CHANGE FOR ROLLUP
    public void updateForRollUp(int w) {
        int xpos = 0;
        double accumulatedWidthRatio = 0;
        int height = -1;

        for (Iterator<BaseTableContainer> it = rowList.iterator(); it.hasNext();) {
            BaseTableContainer comp = it.next();
            if (comp.getTable() instanceof TablePane) {
                ((TablePane) comp.getTable()).updateForRollUp(w);
            }
            Dimension dim = comp.getPreferredSize();
            if (height == -1) {
                height = dim.height;
            }
            comp.setLocation(xpos, comp.getY());
            System.out.println("name: " + comp.getName());
            accumulatedWidthRatio += comp.getRollUpRatio();
            System.out.println("rrratio: " + comp.getRollUpRatio());
            System.out.println("cumratio: " + accumulatedWidthRatio);
            int width;
            if (it.hasNext()) {
                width = (int) Math.round(accumulatedWidthRatio * w) - xpos;
            } else {
                width = w - xpos;
            }
            System.out.println(" w: " + w);
            System.out.println("width: " + width);
            comp.setSize(width, getHeight());
            xpos += width;
            comp.updateHeader();
        }
        setPreferredSize(new Dimension(getWidth(), height));
    }

    public void updateSizesAndHeights() {
        double assignedHeight = 0;
        int unassigned = 0;
        double hr = 0.0;
        // for each btc in the tp
        for (Iterator<BaseTableContainer> it = rowList.iterator(); it.hasNext();) {
            BaseTableContainer comp = it.next();
            hr = comp.getHeightRatio();
            //at this point widthScales are accurate for abstract
            if (hr != -1) {
                if (assignedHeight + hr > 1.0) {
                    comp.setHeightRatio(1.0 - assignedHeight);
                    assignedHeight = 1.0;
                } else {
                    assignedHeight += hr;
                }
            } else {  //* entered as weight    
                unassigned++;
            }
        }
        int ypos = 0;
        double accumulatedHeightRatio = 0;
        int width = -1;

        for (Iterator<BaseTableContainer> it = rowList.iterator(); it.hasNext();) {
            BaseTableContainer comp = it.next();
            //if abstract
            if (comp.getTable() instanceof TablePane) {
                ((TablePane) comp.getTable()).updateSizesAndHeights();
            }
            if (comp.getHeightRatio() == -1) {
                comp.setHeightRatio((1.0 - assignedHeight) / unassigned);
            }
            Dimension dim = comp.getPreferredSize();
            if (width == -1) {
                width = dim.width;
            }
            //comp.setLocation(xpos, comp.getY());
            accumulatedHeightRatio += comp.getHeightRatio();
            int height;
            if (it.hasNext()) {
                height = (int) Math.round(accumulatedHeightRatio * getHeight()) - ypos;
            } else {
                height = getHeight() - ypos;
            }
            comp.setSize(getWidth(), height);
            ypos += height;
            comp.setLocation(comp.getX(), getHeight() - ypos);
            comp.updateHeader();

        }
        setPreferredSize(new Dimension(width, getHeight()));
    }

    //used in DisplayPanel to get Vector of attributecells
    public void getAttributeCells(Vector list) {
        for (Iterator<BaseTableContainer> it = rowList.iterator(); it.hasNext();) {
            BaseTableContainer comp = it.next();
            if (comp.getTable() instanceof TablePane) {
                ((TablePane) comp.getTable()).getAttributeCells(list);
            } else {
                list.add(comp.getTable());
            }
        }
    }
}
