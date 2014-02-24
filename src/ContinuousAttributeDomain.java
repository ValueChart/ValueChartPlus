import java.util.*;


//This is an important class. In fact, I believe most of the interaction with the continuous domain
//can be done through this class.
//If you are successful using this class, in theory, you can ignore AttributeDomain class
//More comments are added below

public class ContinuousAttributeDomain extends AttributeDomain
{
	public class Knot
	 {
	   double ord;
	   double val;

	   Knot (double ord, double val)
	    { this.ord = ord;
	      this.val = val;
	    }

	   double interpolate (Knot next, double x)
	    {
	      return val + (next.val-val)/(next.ord-ord)*(x-ord);
	    }
	 }
	
	public Knot getKnot (double ord)
	 { for (Iterator it=knotList.iterator(); it.hasNext(); ) 
	    { Knot k = (Knot)it.next();
	      if (k.ord == ord)
	       { return k;
	       }
	    }
	   return null;
	 }

	Vector knotList;

        //the constructor carries a Vector list of elements
	public ContinuousAttributeDomain()
	 { super();
	   knotList = new Vector(32);
	 }

        //Returns type.
	public int getType()
	 { return CONTINUOUS;
	 }
	
        //This function will help you find the weight for a particular element x.
        //In continuous domain, the x-axis is always double which maps to another double value on the y-axis
	public double weight (double x)
	 {
	   Knot k1, k2;
	   if (knotList.size() < 2)
	    { throw new IllegalStateException ("incomplete knot list"); 
	    }
	   Iterator it = knotList.iterator();
	   k1 = (Knot)it.next();
	   k2 = (Knot)it.next();
	   if (x < k1.ord)
	    { throw new IllegalArgumentException (
"input " + x + " is out of range");
	    }
	   while (x > k2.ord && it.hasNext())
	    { k1 = k2;
	      k2 = (Knot)it.next();
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
	   for (Iterator it=knotList.iterator(); it.hasNext(); ) 
	    { Knot knot = (Knot)it.next();
	      knotvals[i++] = knot.ord;
	    }
	   return knotvals;
	 }

        //Returns all the y-axis weights. This is useful together with getKnots, to get all the elements in the domain
	public double[] getWeights()
	 {
	   double[] weights = new double[knotList.size()];
	   int i = 0;
	   for (Iterator it=knotList.iterator(); it.hasNext(); ) 
	    { Knot knot = (Knot)it.next();
	      weights[i++] = knot.val;
	    }
	   return weights;
	 }

        //Returns the minmum element on the x-axis
	public double getMin ()
	 {
	   if (knotList.size() < 2)
	    { throw new IllegalStateException ("incomplete knot list"); 
	    }
	   return ((Knot)knotList.firstElement()).ord;
	 }

        //Returns the maximum element on the y-axis
	public double getMax ()
	 {
	   if (knotList.size() < 2)
	    { throw new IllegalStateException ("incomplete knot list"); 
	    }
	   return ((Knot)knotList.lastElement()).ord;
	 }

        //This add an element to the Vector List. when the data file is read, this is called repeatedly.
        //In the future, this function will be useful for people to add/remove domain values
	public void addKnot (double ord, double val)
	 {
	   Knot knot = new Knot (ord, val);
	   if (knotList.size() == 0)
	    { knotList.add (knot);
	      return;
	    }
	   Knot k1;
	   Iterator it = knotList.iterator();
	   k1 = (Knot)it.next();
	   int index = 0;
	   while (ord > k1.ord && it.hasNext())
	    { 
			k1 = (Knot)it.next();
			index++;
	    }
	   if (ord > k1.ord)
	    {
		   knotList.add (index+1, knot);
	    }
	   else if (ord == k1.ord)
	    { 
		   k1.val = val;
	    }
	   else
	    { 
		   knotList.add (index, knot);
	    }
	 }

        //This one is for removing domain values.
	public void removeKnot (double ord)
	 {
	   Iterator it = knotList.iterator();
	   while (it.hasNext())
	    { if (((Knot)it.next()).ord == ord)
	       { it.remove();
		 break;
	       }
	    }
	 }
	
	public void changeWeight (double ord, double val)
	 {
		Knot k = getKnot(ord);
		k.val = val;
	 }
}
