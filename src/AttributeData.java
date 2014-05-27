import java.util.HashMap;

public interface AttributeData {

    public double getWeight();

    public boolean isAbstract();

    public String getName();

    public void setName(String name);

    public AttributeData findData(String name);

    public AttributeAbstractData getAbstract();

    public AttributePrimitiveData getPrimitive();

    public String getObjectiveOutput(ColorList color, int level);

    public String getOutputLogXML(boolean isRoot);
    
    public String getOutputWeightXML(boolean isRoot);
    
    public AttributeData getDeepCopy(HashMap<String, AttributeData> attrMap);
}
