import java.util.Vector;

public class AttributeAbstractData implements AttributeData {

    Vector<AttributeData> children;
    private String attributeName;

    public AttributeAbstractData() {
        children = new Vector<AttributeData>();
    }

    @Override
    public boolean isAbstract() {
        return true;
    }

    @Override
    public AttributePrimitiveData getPrimitive() {
        return null;
    }

    @Override
    public double getWeight() {
        double total = 0;
        for (AttributeData c : children) {
            total += c.getWeight();
        }
        return total;
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

        for (AttributeData c : children) {
            AttributeData found = c.findData(name);
            if (found != null)
                return found;
        }
        return null;
    }

    @Override
    public AttributeAbstractData getAbstract() {
        return this;
    }

    public Vector<AttributeData> getChildren() {
        return children;
    }

    public void setChildren(Vector<AttributeData> children) {
        this.children = children;
    }

    public void addChild(AttributeData child) {
        children.add(child);
    }

    public void removeAllChildren() {
        children.clear();
    }

    @Override
    public String getObjectiveOutput(ColorList color, int level) {
        String str = "";
        for (int i = 0; i <= level; i++)
            str = str + "\t";

        if (level == 0) // is root
            str = str + "attributes " + getName() + " " + getWeight() + "\n";
        else
            str = str + "attributes " + getName() + " *\n";

        for (int i = 0; i < children.size(); i++) {
            str = str + children.get(i).getObjectiveOutput(color, level + 1);
            if (i == children.size() - 1) {
                for (int j = 0; j <= level; j++)
                    str = str + "\t";
                str = str + "end\n";
            }
        }
        return str;
    }

}
