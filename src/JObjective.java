import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.text.DecimalFormat;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

/**
 * Represents a single objective to be rendered as a label
 * TODO: redundant information stored for UI and data handling, should remove UI data where possible
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
	
	Color color;

	double minC = 0;		
	double maxC = 100;
	
	// UI data
	private AttributeDomain domain;	
	private AttributeDomainType domain_type;
	private String unit;
	/**
	 * If JObjective is associated with some data (ie. not just generated for display)
	 * then data should be instantiated
	 */
	private AttributeData data = null;

	int origin;        
    boolean init;			//for new objectives, shows whether first value function is created
	ValueChart chart=null;	//needed for utility graphs
    AttributeCell acell;
    
	DecimalFormat decimalFormat;
	
	

    public JObjective(String str){
		name = str;
		constructDefault();
	}
    
    /**
     * Construct based on information from data
     * @param d associated data handle
     */
    public JObjective(AttributeData d){
        data = d;
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
	    if (data != null)
            return data.getName();
	    else
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
	    if (data != null && !data.isAbstract())
	        return data.getPrimitive().getDomain().getType();
	    else
	        return domain_type;
	}
	
	public String getUnit(){
	    if (data != null && !data.isAbstract())
	        return data.getPrimitive().getUnitsName();
	    else 
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
	    if (data != null && !data.isAbstract() && decimalFormat != null)
            return decimalFormat.format(data.getWeight());
	    else 
	        return weight;
	}
	
	public double getWeightNumeric() {
       if (data != null)
           return data.getWeight();
       else
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
	    if (data != null && !data.isAbstract())
	        return data.getPrimitive().getDomain();
	    else 
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
	void setContinuous(){
		num_points = 6;
		Vector values = new Vector();
		for (int i=0; i<num_points; i++){
			values.add(Double.valueOf(0.5));
		}	
		setCDomain(values);
	}
	
	//linear: positive or negative
	void setContinuous(boolean pos){
		num_points = 5;
		Vector values = new Vector();
		if (pos)
			for (int i=0; i<num_points; i++)
				values.add (new Double(1.0/(num_points-1)*(i)));
		else
			for (int i=0; i<num_points; i++)
				values.add (new Double(1.0/(num_points-1)*((num_points-1) - i)));		
			
		setCDomain(values);
	}

	void setContinuous(double peak){
		num_points = 5;
		Vector values = new Vector();

		values.add(Double.valueOf(0));
		values.add(Double.valueOf(1));
		values.add(Double.valueOf(0));
		
		//seting domain as would in setCDomain
    	domain = null;		
    	Vector knots = new Vector();
		domain_type = AttributeDomainType.CONTINUOUS;		

		knots.add(Double.valueOf(minC));
		knots.add(Double.valueOf(peak));
		knots.add(Double.valueOf(maxC));		

		domain = AttributeDomain.getInfo(knots, values, domain_type);	
	}
	
	void setCDomain(Vector v){
    	domain = null;		
		Vector values = v;
		Vector knots = new Vector();
		domain_type = AttributeDomainType.CONTINUOUS;		
		for (int i=0; i<num_points; i++){
			Double knt = new Double(minC+((maxC-minC)/(num_points-1)*(i)));
			knots.add(knt);
		}	
		domain = AttributeDomain.getInfo(knots, values, domain_type);		
	}
	
	void setDiscrete(){
		Vector values = new Vector();	//reset value vector	
		Vector elements = new Vector();	
    	domain = null;		
		domain_type = AttributeDomainType.DISCRETE;
		domain = AttributeDomain.getInfo(elements, values, domain_type);			
	}
	
	
	void setDiscrete(Vector v){
		Vector values = new Vector();	//reset value vector	
		Vector elements = new Vector();	
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
        if (getDomainType() == AttributeDomainType.DISCRETE) {
        	DiscreteAttributeDomain dd = (DiscreteAttributeDomain)getDomain();
        	DiscreteUtilityGraph dug = new DiscreteUtilityGraph(chart, dd, dd.getElements(), dd.getWeights(), getName(), dvf, acell);
            pnl.add(dug);        	
        }
        else {
        	ContinuousAttributeDomain cd = (ContinuousAttributeDomain)getDomain();
            ContinuousUtilityGraph cug = new ContinuousUtilityGraph(chart, cd, cd.getKnots(), cd.getWeights(), getUnit(), getName(), dvf, acell);
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
    
    
    public AttributeData getData() {
        return data;
    }

    public void setData(AttributeData data) {
        this.data = data;
    }
}

