import java.util.*;

/**
 * This class is for comparing weights.
 * 
 *
 */
public class WeightComparator implements Comparator<ChartEntry>
{
	   BaseTableContainer baseTab;

	   WeightComparator (BaseTableContainer tab)
	    { baseTab = tab;
	    }

	   public int compare (ChartEntry o1, ChartEntry o2)
	      throws ClassCastException
	    { 
	      double w1 = baseTab.entryWeight(o1);
	      double w2 = baseTab.entryWeight(o2);
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

	   public boolean equals (ChartEntry o1, ChartEntry o2)
	    {
	      double w1 = baseTab.entryWeight(o1);
	      double w2 = baseTab.entryWeight(o2);
	      return w1 == w2;
	    }
}
