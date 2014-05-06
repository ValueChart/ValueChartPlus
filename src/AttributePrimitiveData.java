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
		if (attributeName.equals(name)) return this;
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

}
