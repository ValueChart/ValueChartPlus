import javax.swing.JPanel;

/**
 * Represents a single user interaction. Used for Undo/Redo operations
 * 
 *
 */
public class LastInteraction {

    public static final int NO_UNDO = 0, SLIDE = 1, PUMP = 2, UTIL = 3;
    int type;
    int delY;
    int dragY;
    boolean pump;
    boolean east;
    String elt;
    double weight;
    double knot;
    JPanel pnlUtil;
    String name;
    ValueChart chart;

    LastInteraction(ValueChart ch) {
        type = NO_UNDO;
        chart = ch;
    }

    void setUndoUtil(JPanel p, String e, double k, double wt, String attrName) {
        type = UTIL;
        pnlUtil = p;
        elt = e;
        knot = k;
        weight = wt;
        name = attrName;
    }

    void setUndoSlide(String baseName, int dy, int ry, boolean e) {
        type = SLIDE;
        name = baseName;
        dragY = ry;
        delY = dy;
        east = e;
    }

    void setUndoPump(String baseName, boolean p) {
        type = PUMP;
        name = baseName;
        pump = p;
    }

    void setRedo(LastInteraction last) {
        last.type = type;
        last.dragY = dragY;
        last.delY = -delY;
        last.pump = pump ? false : true;
        last.pnlUtil = pnlUtil;
        last.knot = knot;
        last.elt = elt;
        last.name = name;
        AttributeDomain domain = chart.getDomain(name);
        if (domain != null && type == UTIL) {
            if (domain.getType() == AttributeDomainType.DISCRETE) {
                DiscreteAttributeDomain d = domain.getDiscrete();
                last.weight = d.getWeight(elt);
            } else {
                ContinuousAttributeDomain c = domain.getContinuous();
                last.weight = c.getValue(knot);
            }
        }

    }

    void execute() {
        switch (type) {
        case SLIDE: {
            BaseTableContainer base = chart.getPrimContainer(name);
            if (base == null) return;
            base.dragY = dragY;
            // if (east)
            base.mouseHandler.setEastRollup(base);
            /*
             * else base.mouseHandler.setWestRollup(base);
             */
            base.mouseHandler.eastRollupStretchDrag(delY);
            chart.updateAll();
            break;
        }
        case PUMP: {
            BaseTableContainer base = chart.getPrimContainer(name);
            if (base == null) return;
            base.mouseHandler.pump(base, pump);
            break;
        }
        case UTIL: {
            if (pnlUtil instanceof ContGraph) {
                ContGraph cg = (ContGraph) pnlUtil;
                ContinuousAttributeDomain cdomain = chart.getDomain(cg.attributeName).getContinuous();
                cdomain.setWeight(knot, weight);
                cg.plotPoints();
            } else if (pnlUtil instanceof DiscGraph) {
                DiscGraph dg = (DiscGraph) pnlUtil;
                dg.ddomain.changeWeight(elt, weight);
                dg.plotPoints();
            } else if (pnlUtil instanceof ContinuousUtilityGraph) {
                ContinuousUtilityGraph cug = (ContinuousUtilityGraph) pnlUtil;
                AttributeDomain domain = chart.getDomain(name);
                domain.getContinuous().setWeight(knot, weight);
                if (cug.acell != null && cug.acell.dg != null)
                    cug.acell.cg.plotPoints();
            } else if (pnlUtil instanceof DiscreteUtilityGraph) {
                DiscreteUtilityGraph dug = (DiscreteUtilityGraph) pnlUtil;
                AttributeDomain domain = chart.getDomain(name);
                domain.getDiscrete().changeWeight(elt, weight);
                if (dug.acell != null && dug.acell.dg != null)
                    dug.acell.dg.plotPoints();
            }
            chart.updateAll();
        }
        }
    }

    public ValueChart getChart() {
        return chart;
    }

    public void setChart(ValueChart chart) {
        this.chart = chart;
    }
}