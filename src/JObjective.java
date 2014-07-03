import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

/**
 * Represents a single objective to be rendered as a label
 * 
 *
 */
public class JObjective extends JLabel{
	private static final long serialVersionUID = 1L;
	
	public static final int CREATED = 1;	
	public static final int FROM_FILE = 2;
	
	JMenuItem menuItem;
	
	private String name;
	int num_obj;
	private String weight;
	//Value Function 
	//Discrete
	int num_points = 6;	
	
	Color color = Color.WHITE;

	// number of alternatives that hold distinct objective values
	// key: objective value, value: count
	private HashMap<Object, Integer> objValuesMap; 
	
	// UI data
	private AttributeDomain domain;	
	private AttributeDomainType domain_type;
	private String unit;

	int origin;        
    boolean init;			//for new objectives, shows whether first value function is created
    AttributeCell acell;
    
	DecimalFormat decimalFormat;
	
	

    public JObjective(String str){
		name = str;
		constructDefault();
	}
    
    private void constructDefault() {
        setFont(new Font("Arial", Font.PLAIN, 11));

        //set objective details
        setContinuous();    // defaults to a positive linear continuous domain
        
        unit = "";
        weight = "*";
        origin = CREATED;   //defaults to newly created objective
        init = false;
        objValuesMap = new HashMap<Object,Integer>();
        
        //set component details
        setText(getName());      
        setBorder(BorderFactory.createEtchedBorder(1));
        setHorizontalAlignment(JLabel.CENTER);
        color = Color.WHITE;
        setOpaque(false);
        setColor(color);
        setToolTipText(getName());
        setDecimalFormat("0.000");
        
        menuItem = new JMenuItem(getName());
    }
	
	public JMenuItem getMenuItem(){
		return menuItem;
	}
	
	public void setColor(Color col){
		color = col;
		setBackground(color);
	}
	
	/**
	 * returns name from associated data where available
	 * returns UI name otherwise
	 */
	public String getName(){
	    return name;
	}
	
	/**
	 * sets UI name label only
	 */
	public void setName(String str){
		name = str;
		setText(getName());
	}
	
	public AttributeDomainType getDomainType(){
	    return domain_type;
	}
	
	public String getUnit(){
	    return unit;
	}
	
	/**
	 * returns name of JObjective, calls getName()
	 */
	public String toString(){
	    return getName();
	}	
	
	public static void setValueFunction(boolean isContinuous){
		// if it is continuous, set default
	}
	
	public String getWeight(){
	    return weight;
	}
	
	public double getWeightNumeric() {
       return Double.parseDouble(weight);
	}
	
	/**
	 * sets weight string label only, does not update underlying
	 * AttributeData weight, if it is not null
	 * IMPORTANT: make sure to modify underlying AttributeData if required
	 * @param wt
	 */
	public void setWeight(String wt){
		weight = wt;
	}
	
	public AttributeDomain getDomain(){
	    return domain;
	}
	
	void updateDetails(AttributeDomainType dt, String nm, String un){
		domain_type = dt;		
		name = nm;
		unit = un;
		setText(getName());
		if (domain_type == AttributeDomainType.CONTINUOUS)
			setContinuous();
		else
			setDiscrete();		
	}
	 
	void setDomainType(AttributeDomainType type){
		domain_type = type;
	}
	
	void setDomain(AttributeDomain ad){
		domain = ad;
	}
	
	//default flat
	// TODO needs update depending on what it's used for
	void setContinuous(){
		Vector<Double> values = new Vector<Double>();
		setCDomain(values);
	}
	
	//linear: positive or negative
	void setContinuous(boolean pos){
		num_points = 5;
		Vector<Double> values = new Vector<Double>();
		if (pos)
			for (int i=0; i<num_points; i++)
				values.add (new Double(1.0/(num_points-1)*(i)));
		else
			for (int i=0; i<num_points; i++)
				values.add (new Double(1.0/(num_points-1)*((num_points-1) - i)));		
			
		setCDomain(values);
	}

	// TODO needs update depending on what it's used for
	void setContinuous(double peak){
		Vector<Double> values = new Vector<Double>();
		
		//seting domain as would in setCDomain
    	domain = null;		
    	Vector<Object> knots = new Vector<Object>();
		domain_type = AttributeDomainType.CONTINUOUS;			

		domain = AttributeDomain.getInfo(knots, values, domain_type);	
	}
	
	void setCDomain(Vector<Double> v){
		Vector<Double> values = v;
		Vector<Object> knots = new Vector<Object>();
		domain_type = AttributeDomainType.CONTINUOUS;
		double minC = Double.MAX_VALUE;
		double maxC = Double.MIN_VALUE;
		if (domain != null) {
		    minC = domain.getContinuous().getMin();
		    maxC = domain.getContinuous().getMax();
		}
		if (minC < maxC) {
    		for (int i=0; i<num_points; i++){
    			Double knt = new Double(minC+((maxC-minC)/(num_points-1)*(i)));
    			knots.add(knt);
    		}	
		}
		domain = AttributeDomain.getInfo(knots, values, domain_type);		
	}
	
	void setDiscrete(){
		Vector<Double> values = new Vector<Double>();	//reset value vector	
		Vector<Object> elements = new Vector<Object>();	
    	domain = null;		
		domain_type = AttributeDomainType.DISCRETE;
		domain = AttributeDomain.getInfo(elements, values, domain_type);			
	}
	
	
	void setDiscrete(Vector<Object> v){
		Vector<Double> values = new Vector<Double>();	//reset value vector	
		Vector<Object> elements = new Vector<Object>();	
    	domain = null;		
		domain_type = AttributeDomainType.DISCRETE;
		elements = v;
		for (int i=0; i<elements.size(); i++){			
			values.add(Double.valueOf(0.5));
		}
		num_points = values.size();
		domain = AttributeDomain.getInfo(elements, values, domain_type);			
	}
	
    public JPanel getUtilityGraph(DefineValueFunction d){
    	DefineValueFunction dvf = d;
    	JPanel pnl = new JPanel();
    	pnl.setLayout(new FlowLayout());
        ValueChart chart = null;
        if (dvf != null && dvf.con != null) chart = dvf.con.chart;
        if (getDomainType() == AttributeDomainType.DISCRETE) {
        	DiscreteAttributeDomain dd = getDomain().getDiscrete();
        	DiscreteUtilityGraph dug = new DiscreteUtilityGraph(chart, false, dd, dd.getElements(), dd.getWeights(), getName(), dvf, acell);
            pnl.add(dug);        	
        }
        else {
        	ContinuousAttributeDomain cd = getDomain().getContinuous();
            ContinuousUtilityGraph cug = new ContinuousUtilityGraph(chart, false, cd, cd.getKnots(), cd.getWeights(), getUnit(), getName(), dvf, acell);
            pnl.add(cug);            
        }
        return pnl;	
    }
    
    public void setUnit(String u){
    	unit = u;
    }
    
    public void setDecimalFormat(String df){
    	decimalFormat = new DecimalFormat(df);
    }
    
    public void replaceInObjValueMap(Object oldVal, Object newVal) {
        if (!objValuesMap.containsKey(oldVal)) 
            return;
        
        if (objValuesMap.get(oldVal) == 1) {
            objValuesMap.remove(oldVal);
        } else {
            objValuesMap.put(oldVal, objValuesMap.get(oldVal)-1);
        }
        if (objValuesMap.containsKey(newVal)) {
            objValuesMap.put(newVal, objValuesMap.get(newVal)+1);
        } else {
            objValuesMap.put(newVal, 1);
        }
    }
    
    public void addToObjValueMap(Object val) {
        if (objValuesMap.containsKey(val)) {
            objValuesMap.put(val, objValuesMap.get(val)+1);
        } else {
            objValuesMap.put(val, 1);
        }
    }
    
    public void decreaseInObjValueMap(Object val) {
        if (objValuesMap.containsKey(val)) {
            if (objValuesMap.get(val) == 1) {
                objValuesMap.remove(val);
            } else {
                objValuesMap.put(val, objValuesMap.get(val)-1);
            }
        }
    }
    
    public int getObjValueMapCount() {
        return objValuesMap.size();
    }
    
    public void clearObjValueMap() {
        objValuesMap.clear();
    }
    
    public boolean containsKey(Object key) {
        return objValuesMap.containsKey(key);
    }
    
    public Set<Object> getKeySet() {
        return objValuesMap.keySet();
    }

    public void resetWeights() {
        getDomain().resetWeights();
    }
}

