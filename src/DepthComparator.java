import java.util.*;

//This class is for comparing weights.

public class DepthComparator implements Comparator<BaseTableContainer>
{

	   DepthComparator ()
	    {}

	   public int compare (BaseTableContainer o1, BaseTableContainer o2)
	      throws ClassCastException
	    { 
	   	
	   	int i1 = ((TablePane)o1.getParent()).getDepth();
	   	int i2 = ((TablePane)o2.getParent()).getDepth();

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

	   public boolean equals (BaseTableContainer o1, BaseTableContainer o2)
	    {
	   	
	   	int i1 = ((TablePane)o1.getParent()).getDepth();
	   	int i2 = ((TablePane)o2.getParent()).getDepth();

	      return i1 == i2;
	    }
}