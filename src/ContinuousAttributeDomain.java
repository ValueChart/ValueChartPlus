import java.util.*;


//This is an important class. In fact, I believe most of the interaction with the continuous domain
//can be done through this class.
//If you are successful using this class, in theory, you can ignore AttributeDomain class
//More comments are added below

public class ContinuousAttributeDomain extends AttributeDomain
{
    ContGraph contGraph;
    
    public Double getValue(double x) {
        return knotMap.get(x);
    }

	// sorted in increasing x value
	TreeMap<Double, Double> knotMap;

        //the constructor carries a Vector list of elements
	public ContinuousAttributeDomain()
	 { super();
	   knotMap = new TreeMap<Double, Double>();
	 }

        //Returns type.
	public AttributeDomainType getType()
	 { return AttributeDomainType.CONTINUOUS;
	 }
	
	private Double interpolate(double x) {
	    Map.Entry<Double, Double> first = knotMap.floorEntry(x);
	    Map.Entry<Double, Double> second = knotMap.ceilingEntry(x);
	    
	    if (first == null)
	        return knotMap.firstEntry().getValue();
	    if (second == null)
	        return knotMap.lastEntry().getValue();
	    if (first.getKey() == x)
	        return first.getValue();
	    
	    return first.getValue() + (second.getValue()-first.getValue())/(second.getKey()-first.getKey())*(x-first.getKey());
	}
	
        //This function will help you find the weight for a particular element x.
        //In continuous domain, the x-axis is always double which maps to another double value on the y-axis
    public Double weight(double x) {
        if (knotMap.size() < 2) {
            throw new IllegalStateException("incomplete knot list");
        }
        
        if (x < knotMap.firstKey() || x > knotMap.lastKey()) {
            throw new IllegalArgumentException("input " + x
                    + " is out of range");
        } 

        return interpolate(x);
    }

        //Returns all the x-axis elements. This is useful if you need to display all the elements in the continuous domain.
    public double[] getKnots() {
        double[] knotvals = new double[knotMap.size()];
        int i = 0;
        for (Iterator<Double> it = knotMap.keySet().iterator(); it.hasNext();) {
            knotvals[i++] = it.next();
        }

        return knotvals;
    }

        //Returns all the y-axis weights. This is useful together with getKnots, to get all the elements in the domain
    public double[] getWeights() {
        double[] weights = new double[knotMap.size()];
        int i = 0;
        for (Map.Entry<Double, Double> entry : knotMap.entrySet()) {
            weights[i++] = entry.getValue();
        }
        return weights;
    }

        //Returns the minimum element on the x-axis
    public double getMin() {
        if (knotMap.size() < 2) {
            throw new IllegalStateException("incomplete knot list");
        }
        return knotMap.firstKey();
    }

        //Returns the maximum element on the x-axis
    public double getMax() {
        if (knotMap.size() < 2) {
            throw new IllegalStateException("incomplete knot list");
        }
        return knotMap.lastKey();
    }

        //This add an element to the Vector List. when the data file is read, this is called repeatedly.
        //In the future, this function will be useful for people to add/remove domain values
    public void addKnot(double ord, double val) {
        double y;
        if (val > 1.0)
            y = 1.0;
        else if (val < 0.0)
            y = 0.0;
        else
            y = val;

        knotMap.put(ord, y);
    }

        //This one is for removing domain values.
    public void removeKnot(double ord) {
        knotMap.remove(ord);
    }
	
    public void changeWeight(double ord, double val) {
        if (val > 1.0)
            knotMap.put(ord, 1.0);
        else if (val < 0.0)
            knotMap.put(ord, 0.0);
        else
            knotMap.put(ord, val);
    }
	
	// scales weights so they fall in range [0, 1]
	public void normalize() {
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        for (Map.Entry<Double, Double> entry : knotMap.entrySet()){
            double currVal = entry.getValue(); 
            if (currVal > max) {
                max = currVal;
            }
            if (currVal < min) {
                min = currVal;
            }
        }
	    if (min == 0 && max == 1) return;
	    
	    double range = max - min;
	    double shift = min;
	    
	    for (Map.Entry<Double, Double> entry : knotMap.entrySet()) {
	        entry.setValue((entry.getValue()-shift)/range); 
	    }
	}
	
	// new range for x-axis
	public boolean updateRange(double min, double max) {
	    if ( (min == getMin() && max == getMax()) ||
	         max <= min) return true;
	    
        // new range larger than current range
	    if (max < knotMap.firstKey()) return false;
	    // new range smaller than current range
	    if (min > knotMap.lastKey()) return false;
	    
	    Double curr;
	    if (min != getMin()) {
	        knotMap.put(min, interpolate(min));
	        curr = knotMap.firstKey();
	        while (curr < min) {
	            knotMap.remove(curr);
	            curr = knotMap.firstKey();
	        }
	    }
	    if (max != getMax()) {
	        knotMap.put(max, interpolate(max));
	        curr = knotMap.lastKey();
	        while (curr > max) {
	            knotMap.remove(curr);
	            curr = knotMap.lastKey();
	        }
	    }
	    
        normalize();
        if (contGraph != null)
            contGraph.plotPoints();
        return true;
    }

    @Override
    public AttributeDomain getDeepCopy() {
        ContinuousAttributeDomain newData = new ContinuousAttributeDomain();
        for (Map.Entry<Double, Double> entry : knotMap.entrySet()) {
            newData.addKnot(entry.getKey(), entry.getValue());
        }
        return newData;
    }

    @Override
    public ContinuousAttributeDomain getContinuous() {
        return this;
    }

    @Override
    public DiscreteAttributeDomain getDiscrete() {
        return null;
    }
	
}
