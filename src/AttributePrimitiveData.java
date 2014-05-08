import java.awt.Color;

public class AttributePrimitiveData implements AttributeData {

    private double attributeWeight;
    private AttributeDomain domain;
    private Color color;
    private String unitsName;
    private String attributeName;

    @Override
    public double getWeight() {
        return attributeWeight;
    }

    public void setWeight(double weight) {
        attributeWeight = weight;
    }

    @Override
    public boolean isAbstract() {
        return false;
    }

    public AttributeDomain getDomain() {
        return domain;
    }

    public void setDomain(AttributeDomain domain) {
        this.domain = domain;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public String getUnitsName() {
        return unitsName;
    }

    public void setUnitsName(String unitsName) {
        this.unitsName = unitsName;
    }

    @Override
    public String getName() {
        return attributeName;
    }

    @Override
    public void setName(String name) {
        attributeName = name;
    }

    @Override
    public AttributeData findData(String name) {
        if (attributeName.equals(name))
            return this;
        return null;
    }

    @Override
    public AttributeAbstractData getAbstract() {
        return null;
    }

    @Override
    public AttributePrimitiveData getPrimitive() {
        return this;
    }

    @Override
    public String getObjectiveOutput(ColorList color, int level) {
        String str = "";
        for (int i = 0; i <= level; i++)
            str = str + "\t";

        str = str + getName() + " " + getWeight() + " { ";

        AttributeDomain d = getDomain();
        double vals[] = d.getWeights();
        for (int i = 0; i < vals.length; i++) {
            if (d.getType() == AttributeDomainType.DISCRETE) {
                String elts[] = d.getElements();
                str = str + "\"" + elts[i] + "\" ";
            } else {
                double kts[] = d.getKnots();
                str = str + kts[i] + " ";
            }
            str = str + vals[i];
            if (i != (vals.length - 1))
                str = str + ",";
            str = str + " ";

        }
        str = str + "}\n";
        for (int i = 0; i <= level + 1; i++)
            str = str + "\t";

        str = str + "color=" + getName();

        if ((d.getType() == AttributeDomainType.CONTINUOUS)
                && (getUnitsName() != "")) {
            str = str + " units=" + getUnitsName();
        }
        str = str + " end\n";

        return str;
    }

}
