//AttributeValue class seems to give AttributeValue of a domain.
//However, I found out that it seems we can do something very similar
//through ContinuousAttributeDomain and DiscreteAttributeDomain
//Those two classes have a more direct implementation of abstract class AttributeDomain

/**
 * Contains information on a specific value of a domain for a criterion.
 * Gives the weight (y-axis, [0,1]) given its domain value (x-axis) and criterion name.
 *
 */
public class AttributeValue
{
	String str;
	double num;
	String attributeName;
	ValueChart chart;
	boolean isDiscrete;

	//-for discrete attribute, set value and domain
	public AttributeValue (String s, AttributeDomain d, String attrName, ValueChart vc){
		str = s;
		if (d.getType() != AttributeDomainType.DISCRETE)
			throw new IllegalArgumentException ("Symbolic values must be associated with discrete domains");	    
		try{
			d.getDiscrete().getEntryWeight(s);
	    }catch (Exception e){
	    	throw new IllegalArgumentException ("Attribute value " + s + " unknown"); 
	    }
	    attributeName = attrName;
	    chart = vc;
	    isDiscrete = true;
	}
	
	//-for continuous attribute, set value and domain
	public AttributeValue (double n, AttributeDomain d, String attrName, ValueChart vc){
		num = n;
		if (d.getType() != AttributeDomainType.CONTINUOUS)
			throw new IllegalArgumentException ("Numeric values must be associated with continous domains");
		try{
			d.getContinuous().weight(n);
		}catch (Exception e){
			throw new IllegalArgumentException ("Attribute value " + n + " out of range"); 
		}
        attributeName = attrName;
        chart = vc;
        isDiscrete = false;
	}

	public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public ValueChart getChart() {
        return chart;
    }

    public void setChart(ValueChart chart) {
        this.chart = chart;
    }

    public boolean isDiscrete() {
        return isDiscrete;
    }

    public void setDiscrete(boolean isDiscrete) {
        this.isDiscrete = isDiscrete;
    }

    public String symbolicValue()
	 { return str;
	 }

	public double numericValue()
	 { return num;
	 }

	public String stringValue()
	 {
	   if (str != null)
	    { return str;
	    }
	   else
	    { return "" + num;
	    }
	 }

	//returns the weight value of the attribute
    public double weight() {
        AttributeDomain domain = chart.getDomain(attributeName);
        if (isDiscrete) {
            return domain.getDiscrete().getEntryWeight(str);
        } else {
            return domain.getContinuous().weight(num);
        }
    }
}
