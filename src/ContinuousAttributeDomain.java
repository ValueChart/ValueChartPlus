import java.util.*;


//This is an important class. In fact, I believe most of the interaction with the continuous domain
//can be done through this class.
//If you are successful using this class, in theory, you can ignore AttributeDomain class
//More comments are added below

public class ContinuousAttributeDomain extends AttributeDomain
{
    ContGraph contGraph;
    
	public class Knot
	 {
	   double ord; // x-axis; alternative objective values
	   double val; // y-axis

	   Knot (double ord, double val)
	    { this.ord = ord;
	      this.val = val;
	    }

	   // interpolates at x between this Knot and next
	   double interpolate (Knot next, double x)
	    {
	      return val + (next.val-val)/(next.ord-ord)*(x-ord);
	    }
	 }
	
	public Knot getKnot (double ord)
	 { for (Iterator<Knot> it=knotList.iterator(); it.hasNext(); ) 
	    { Knot k = it.next();
	      if (k.ord == ord)
	       { return k;
	       }
	    }
	   return null;
	 }

	// sorted in increasing ord value
	Vector<Knot> knotList;

        //the constructor carries a Vector list of elements
	public ContinuousAttributeDomain()
	 { super();
	   knotList = new Vector<Knot>(32);
	 }

        //Returns type.
	public AttributeDomainType getType()
	 { return AttributeDomainType.CONTINUOUS;
	 }
	
        //This function will help you find the weight for a particular element x.
        //In continuous domain, the x-axis is always double which maps to another double value on the y-axis
	public double weight (double x)
	 {
	   Knot k1, k2;
	   if (knotList.size() < 2)
	    { throw new IllegalStateException ("incomplete knot list"); 
	    }
	   Iterator<Knot> it = knotList.iterator();
	   k1 = it.next();
	   k2 = it.next();
	   if (x < k1.ord)
	    { throw new IllegalArgumentException (
"input " + x + " is out of range");
	    }
	   while (x > k2.ord && it.hasNext())
	    { k1 = k2;
	      k2 = it.next();
	    }
	   if (x > k2.ord)
	    { throw new IllegalArgumentException (
"input " + x + " is out of range");
	    }
	   return k1.interpolate (k2, x);
	 }

        //Returns all the x-axis elements. This is useful if you need to display all the elements in the continuous domain.
	public double[] getKnots()
	 {
	   double[] knotvals = new double[knotList.size()];
	   int i = 0;
	   for (Iterator<Knot> it=knotList.iterator(); it.hasNext(); ) 
	    { Knot knot = it.next();
	      knotvals[i++] = knot.ord;
	    }
	   return knotvals;
	 }

        //Returns all the y-axis weights. This is useful together with getKnots, to get all the elements in the domain
	public double[] getWeights()
	 {
	   double[] weights = new double[knotList.size()];
	   int i = 0;
	   for (Iterator<Knot> it=knotList.iterator(); it.hasNext(); ) 
	    { Knot knot = it.next();
	      weights[i++] = knot.val;
	    }
	   return weights;
	 }

        //Returns the minimum element on the x-axis
	public double getMin ()
	 {
	   if (knotList.size() < 2)
	    { throw new IllegalStateException ("incomplete knot list"); 
	    }
	   return knotList.firstElement().ord;
	 }

        //Returns the maximum element on the x-axis
	public double getMax ()
	 {
	   if (knotList.size() < 2)
	    { throw new IllegalStateException ("incomplete knot list"); 
	    }
	   return knotList.lastElement().ord;
	 }

        //This add an element to the Vector List. when the data file is read, this is called repeatedly.
        //In the future, this function will be useful for people to add/remove domain values
    public void addKnot(double ord, double val) {
        Knot knot;
        if (val > 1.0)
            knot = new Knot(ord, 1.0);
        else if (val < 0.0)
            knot = new Knot(ord, 0.0);
        else
            knot = new Knot(ord, val);

        if (knotList.size() == 0) {
            knotList.add(knot);
            return;
        }
        Knot k1;
        Iterator<Knot> it = knotList.iterator();
        k1 = it.next();
        int index = 0;
        while (ord > k1.ord && it.hasNext()) {
            k1 = it.next();
            index++;
        }
        if (ord > k1.ord) {
            knotList.add(index + 1, knot);
        } else if (ord == k1.ord) {
            k1.val = val;
        } else {
            knotList.add(index, knot);
        }
    }

        //This one is for removing domain values.
	public void removeKnot (double ord)
	 {
	   Iterator<Knot> it = knotList.iterator();
	   while (it.hasNext())
	    { if (it.next().ord == ord)
	       { it.remove();
		 break;
	       }
	    }
	 }
	
	public void changeWeight (double ord, double val)
	 {
		Knot k = getKnot(ord);
		if(val > 1.0)
			k.val = Math.min(val, 1.0);
		else if(val < 0.0)
			k.val = Math.max(val, 0.0);
		else
			k.val = val;
//		System.out.println(ord+" "+val+" "+k.val);		
	 }
	
	// scales weights so they fall in range [0, 1]
	public void normalize() {
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        for (int j=0; j<knotList.size(); j++){
            double currVal = knotList.get(j).val; 
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
	    
	    for (int i = 0; i < knotList.size(); i++) {
	        knotList.get(i).val = (knotList.get(i).val-shift)/range; 
	    }
	}
	
	// new range for x-axis
	public boolean updateRange(double min, double max) {
	    if ( (min == getMin() && max == getMax()) ||
	         max <= min) return true;
	    
	    //double currMin = getMin();
	    //double currMax = getMax();
        int k1 = -1; // first knot with ord larger than min
        int k2 = -1; // last knot with ord smaller than max
        for (int i = 0; i < knotList.size(); i++) {
            double currOrd = knotList.get(i).ord;
            if (currOrd > min && k1 < 0) {
                k1 = i;
            } 
            if (currOrd < max) {
                k2 = i;
            }
        }
        
        // new range larger than current range
	    if (k1 < 0) {
	        return false;
	    }
	    // new range smaller than current range
	    else if (k2 < 0) {
	        return false;
	    }
	    // overlap in ranges
	    else {
	        // new min smaller than current range
	        if (k1 == 0) {
	            Knot temp = new Knot(min, knotList.get(k1).val);
	            knotList.add(0, temp);
	            k2++;
	        } else if (min != getMin()){
	            double val = knotList.get(k1-1).interpolate(knotList.get(k1), min);
	            Knot temp = new Knot(min, val);
	            knotList.add(k1, temp);
	            k2++;
	            for (int i = 0; i < k1; i++) {
	                knotList.remove(0);
	                k2--;
	            }
	        }
	        // new max larger than current range
	        if (k2 == knotList.size()-1) {
                Knot temp = new Knot(max, knotList.get(k2).val);
                knotList.add(temp);
	        } else if (max != getMax()){
                double val = knotList.get(k2).interpolate(knotList.get(k2+1), max);
                Knot temp = new Knot(max, val);
                knotList.add(k2+1, temp);
                for (int i = k2+2; i < knotList.size();) {
                    knotList.remove(k2+2);
                }
	        }
	        normalize();
	        if (contGraph != null)
	            contGraph.plotPoints();
	        return true;
	    }
	}
}
