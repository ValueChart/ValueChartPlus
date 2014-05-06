import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.text.DecimalFormat;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;


public class JObjective extends JLabel{
	private static final long serialVersionUID = 1L;
	public static final int DISCRETE = 1;		
	public static final int CONTINUOUS = 2;
	
	public static final int CREATED = 1;	
	public static final int FROM_FILE = 2;
	
	JMenuItem menuItem;
	
	private String name;
	int num_obj;
	String weight;
	//Value Function 
	//Discrete
	int num_points = 6;	
	
	Color color;

	double minC = 0;		
	double maxC = 100;
	
	AttributeDomain domain;	
	int domain_type;
	int origin;		
	String unit;	
	boolean init;			//for new objectives, shows whether first value function is created
	ValueChart chart=null;	//needed for utility graphs
    AttributeCell acell;
    
	DecimalFormat decimalFormat;
	
	public JObjective(String str){
		setFont(new Font("Arial", Font.PLAIN, 11));
		//set objective details
		name = str;
		setContinuous();	// defaults to a positive linear continuous domain
	
		unit = "";
		weight = "*";
		origin = CREATED;	//defaults to newly created objective
		init = false;
		
		//set component details
		setText(name);		
        setBorder(BorderFactory.createEtchedBorder(1));
        setHorizontalAlignment(JLabel.CENTER);
        color = Color.WHITE;
        setOpaque(false);
        setColor(color);
        setToolTipText(name);
        setDecimalFormat("0.0");
        
        menuItem = new JMenuItem(name);
	}
	
	public JMenuItem getMenuItem(){
		return menuItem;
	}
	
	public void setColor(Color col){
		color = col;
		setBackground(color);
	}
	
	
	public String getName(){
		return name;
	}
	
	public void setName(String str){
		name = str;
		setText(name);
	}
	
	public int domainType(){
		return domain_type;
	}
	
	public String getUnit(){
		return unit;
	}
	
	public String toString(){
	    String infoString = name;
	    return infoString;
	}	
	
	public static void setValueFunction(boolean isContinuous){
		// if it is continuous, set default
	}

	public int getType(){
		return domain_type;
	}
	
	public String getWeight(){
		return weight;
	}
	
	public void setWeight(String wt){
		weight = wt;
	}
	
	public AttributeDomain getDomain(){
		return domain;
	}
	
	void updateDetails(int dt, String nm, String un){
		domain_type = dt;		
		name = nm;
		unit = un;
		setText(name);
		if (domain_type == CONTINUOUS)
			setContinuous();
		else
			setDiscrete();		
	}
	 
	void setType(int type){
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
		domain_type = CONTINUOUS;		

		knots.add(Double.valueOf(minC));
		knots.add(Double.valueOf(peak));
		knots.add(Double.valueOf(maxC));		

		domain = AttributeDomain.getInfo(knots, values, domain_type);	
	}
	
	void setCDomain(Vector v){
    	domain = null;		
		Vector values = v;
		Vector knots = new Vector();
		domain_type = CONTINUOUS;		
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
		domain_type = DISCRETE;
		domain = AttributeDomain.getInfo(elements, values, domain_type);			
	}
	
	
	void setDiscrete(Vector v){
		Vector values = new Vector();	//reset value vector	
		Vector elements = new Vector();	
    	domain = null;		
		domain_type = DISCRETE;
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
        if (domain_type == DISCRETE) {
        	DiscreteAttributeDomain dd = (DiscreteAttributeDomain)domain;
        	DiscreteUtilityGraph dug = new DiscreteUtilityGraph(chart, dd, dd.getElements(), dd.getWeights(), name, dvf, acell);
            pnl.add(dug);        	
        }
        else {
        	ContinuousAttributeDomain cd = (ContinuousAttributeDomain)domain;
            ContinuousUtilityGraph cug = new ContinuousUtilityGraph(chart, cd, cd.getKnots(), cd.getWeights(), unit, name, dvf, acell);
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
}

