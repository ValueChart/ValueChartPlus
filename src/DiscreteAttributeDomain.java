import java.util.*;

//Just like ContinuousAttributeDomain, this is the other class which can control most of the interaction
//you will need to do with the domain.
//Some more comments are attempted below

public class DiscreteAttributeDomain extends AttributeDomain
{
    //Again, the basis data management is done through a Vector List. I personally do not think this is a good idea.
    //However, I am guessing this is not going to change until the next rewrite.
    //I personally would create a object class and go from there.
	Vector set;

	public class Entry
	 {
             //In disrete domain, the x-axis value is a string, and y-axis value is a double.
	   String name;
	   double weight;

	   Entry (String name, double weight)
	    { this.name = name;
	      this.weight = weight;
	    }
	 }

        //The rest of the functions are self-explanatory.
	DiscreteAttributeDomain()
	 { super();
	   set = new Vector(32);
	 }

	public AttributeDomainType getType()
	 { return AttributeDomainType.DISCRETE;
	 }
	
	public Entry getEntry (String elem)
	 { for (Iterator it=set.iterator(); it.hasNext(); ) 
	    { Entry e = (Entry)it.next();
	      if (e.name.equals(elem))
	       { return e;
	       }
	    }
	   return null;
	 }

	public double weight (String elem)
	 {
	   Entry e = getEntry(elem);
	   if (e == null)
	    { throw new IllegalArgumentException (
"element " + elem + " not found");
	    }
	   return e.weight;
	 }

	public String[] getElements ()
	 {
	   String[] elems = new String[set.size()];
	   int index = 0;
	   for (Iterator it=set.iterator(); it.hasNext(); )
	    { elems[index++] = ((Entry)it.next()).name;
	    }
	   return elems;
	 }

	public double[] getWeights ()
	 {
	   double[] weights = new double[set.size()];
	   int index = 0;
	   for (Iterator it=set.iterator(); it.hasNext(); )
	    { weights[index++] = ((Entry)it.next()).weight;
	    }
	   return weights;
	 }

	public void addElement (String elem, double weight)
	 {
	   set.add (new Entry (elem, weight));
	 }

	public void removeElement (String elem)
	 {
	   Entry e = getEntry(elem);
	   if (e != null)
	    { set.remove (e);
	    }
	 }

	//added for utility graph: so position does not change
	public void changeWeight(String elem, double wt){
		Entry e = getEntry(elem);		
		if(wt > 1.0)
			e.weight = Math.min(wt, 1.0);
		else if(wt < 0.0)
			e.weight = Math.max(wt, 0.0);
		else
			e.weight = wt;
//		System.out.println(elem+" "+wt+" "+e.weight);
	}
}
