import javax.swing.JPanel;

public class LastInteraction {

    public static final int NO_UNDO = 0, SLIDE = 1, PUMP = 2, UTIL = 3;
    int type;
    int delY;
    int dragY;
    BaseTableContainer base;
    boolean pump;
    boolean east;
    String elt;
    double weight;
    double knot;
    JPanel pnlUtil;
    AttributeDomain domain;
    ValueChart chart;

    LastInteraction(ValueChart ch) {
        type = NO_UNDO;
        chart = ch;
    }

    void setUndoUtil(JPanel p, String e, double k, double wt, AttributeDomain ad) {
        type = UTIL;
        pnlUtil = p;
        elt = e;
        knot = k;
        weight = wt;
        domain = ad;
    }

    void setUndoSlide(BaseTableContainer b, int dy, int ry, boolean e) {
        type = SLIDE;
        base = b;
        dragY = ry;
        delY = dy;
        east = e;
    }

    void setUndoPump(BaseTableContainer b, boolean p) {
        type = PUMP;
        base = b;
        pump = p;
    }

    void setRedo(LastInteraction last) {
        last.type = type;
        last.dragY = dragY;
        last.delY = -delY;
        last.pump = pump ? false : true;
        last.base = base;
        last.pnlUtil = pnlUtil;
        last.knot = knot;
        last.elt = elt;
        last.domain = domain;
        if (domain != null) {
            if (domain.getType() == AttributeDomainType.DISCRETE) {
                DiscreteAttributeDomain d = ((DiscreteAttributeDomain) domain);
                last.weight = d.getEntryWeight(elt);
            } else {
                ContinuousAttributeDomain c = ((ContinuousAttributeDomain) domain);
                last.weight = c.getValue(knot);
            }
        }

    }

    void execute() {
        switch (type) {
        case SLIDE: {
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
            base.mouseHandler.pump(base, pump);
            break;
        }
        case UTIL: {
            if (pnlUtil instanceof ContGraph) {
                ContGraph cg = (ContGraph) pnlUtil;
                cg.cdomain.changeWeight(knot, weight);
                cg.plotPoints();
            } else if (pnlUtil instanceof DiscGraph) {
                DiscGraph dg = (DiscGraph) pnlUtil;
                dg.ddomain.changeWeight(elt, weight);
                dg.plotPoints();
            } else if (pnlUtil instanceof ContinuousUtilityGraph) {
                ContinuousUtilityGraph cug = (ContinuousUtilityGraph) pnlUtil;
                ((ContinuousAttributeDomain) domain).changeWeight(knot, weight);
                cug.acell.cg.plotPoints();
            } else if (pnlUtil instanceof DiscreteUtilityGraph) {
                DiscreteUtilityGraph dug = (DiscreteUtilityGraph) pnlUtil;
                ((DiscreteAttributeDomain) domain).changeWeight(elt, weight);
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