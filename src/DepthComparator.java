import java.util.*;

//This class is for comparing weights.

public class DepthComparator implements Comparator
{

	   DepthComparator ()
	    {}

	   public int compare (Object o1, Object o2)
	      throws ClassCastException
	    { 
	   	
	   	int i1 = ((TablePane)((BaseTableContainer)o1).getParent()).getDepth();
	   	int i2 = ((TablePane)((BaseTableContainer)o2).getParent()).getDepth();

	      if (i1 > i2)
	       { return -1;
	       }
	      else if (i1 == i2)
	       { return 0;
	       }
	      else
	       { return 1; 
	       }
	    }

	   public boolean equals (Object o1, Object o2)
	    {
	   	
	   	int i1 = ((TablePane)((BaseTableContainer)o1).getParent()).getDepth();
	   	int i2 = ((TablePane)((BaseTableContainer)o2).getParent()).getDepth();

	      return i1 == i2;
	    }
}