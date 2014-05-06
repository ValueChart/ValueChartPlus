import java.util.*;

//This class is for comparing weights.

public class WeightComparator implements Comparator
{
	   BaseTableContainer baseTab;

	   WeightComparator (BaseTableContainer tab)
	    { baseTab = tab;
	    }

	   public int compare (Object o1, Object o2)
	      throws ClassCastException
	    { 
	      double w1 = baseTab.entryWeight((ChartEntry)o1);
	      double w2 = baseTab.entryWeight((ChartEntry)o2);
	      if (w1 > w2)
	       { return -1;
	       }
	      else if (w1 == w2)
	       { return 0;
	       }
	      else
	       { return 1; 
	       }
	    }

	   public boolean equals (Object o1, Object o2)
	    {
	      double w1 = baseTab.entryWeight((ChartEntry)o1);
	      double w2 = baseTab.entryWeight((ChartEntry)o2);
	      return w1 == w2;
	    }
}
