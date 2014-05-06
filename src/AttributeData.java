
public interface AttributeData {
	
	public double getWeight();
	
	public boolean isAbstract();
	
	public String getName();
	
	public void setName(String name);
	
	public AttributeData findData(String name);
	
	public AttributeAbstractData getAbstract();
	
	public AttributePrimitiveData getPrimitive();

}
