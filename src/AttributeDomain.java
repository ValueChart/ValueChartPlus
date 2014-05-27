import java.io.*;
//import java.util.HashMap;
//import java.util.Iterator;
import java.util.Vector;

import acme.misc.ScanfReader;



//This class contains the details of a domain. If you are working with a domain, chances are, you will call
//some of the functions in this class to get the weights. 

//NOTE: THIS IS JUST AN ABSTRACT CLASS
public abstract class AttributeDomain
{

	public AttributeDomain()
	 { 
	 }

	public abstract AttributeDomainType getType();

        //getElemnts give you the value in a discrete domain
	public String[] getElements()
	 {
	   return null;
	 }

        //getKnots give you the value in a continuous domain
	public double[] getKnots()
	 {
	   return null;
	 }

        //getWeights will actually give you the utility value
	public double[] getWeights()
	 {
	   return null;
	 }

	//this method added by JLB 
	//does the same thing as read(), but takes from JObjective for construction

/*	
	public static AttributeDomain getInfo(JObjective obj){
		AttributeDomain domain = null;
		Vector vals = obj.getValues();
		
		if (obj.getType() == DISCRETE){	      	
			domain = new DiscreteAttributeDomain();
            for (int i=0; i<vals.size(); i++ ){	      	
            	Double dbl = (Double) obj.getValues().get(i);
            	//System.out.println("vals double " + dbl);
            	((DiscreteAttributeDomain)domain).addElement(obj.getElements().get(i).toString(), dbl.doubleValue());
            	//System.out.println("elts string " + obj.getElements().get(i).toString());
            }
		}	
		else {
	      	domain = new ContinuousAttributeDomain();
            for (int i=0; i<vals.size(); i++ ){	    
	      		Double d =  (Double)obj.getValues().get(i);
	      		//System.out.println("double" + d);
	      		Double k =  (Double)obj.getKnots().get(i);	
	      		//System.out.println("double" + k);		      		
	      		((ContinuousAttributeDomain)domain).addKnot(k.doubleValue(), d.doubleValue());      		
            }
	    }
		return domain;		
	}

	*/
	
	public static AttributeDomain getInfo(Vector<Object> xval, Vector<Double> yval, AttributeDomainType type){
		AttributeDomain domain = null;
		if (type == AttributeDomainType.DISCRETE){	      	
			domain = new DiscreteAttributeDomain();
            for (int i=0; i<yval.size(); i++ ){	      	
            	Double dbl = yval.get(i);
            	domain.getDiscrete().addElement(xval.get(i).toString(), dbl.doubleValue());
            }
		}	
		else {
	      	domain = new ContinuousAttributeDomain();
            for (int i=0; i<yval.size(); i++ ){	    
	      		Double d =  yval.get(i);
	      		Double k =  (Double)xval.get(i);	      		
	      		domain.getContinuous().addKnot(k.doubleValue(), d.doubleValue());      		
            }
	    }
		return domain;		
	}	
	
	public static AttributeDomain read (ScanfReader scanReader)
	   throws IOException
	 {
	   AttributeDomain domain = null;
	   char c;
	   String elementChars = "%S";
	   String numberChars = "0123456789-";
	   String ordstr = null;
	   double val = 0;
	   double ordval = 0;
	   c = scanReader.scanChar(" %c");
	   if (c != '{')
	    { throw new IOException (
"Line " + scanReader.getLineNumber() + ": expected '{'");
	    }
	   while (true)
	    { 
	      ordstr = scanReader.scanString (elementChars);
	      if (ordstr.charAt(0) == '}')
	       { throw new IOException ("Line " + scanReader.getLineNumber() + 
": expected domain entry");
	       }
	      if (numberChars.indexOf(ordstr.charAt(0)) == -1)
	       { // then not a number string
		 if (domain == null)
		  { domain = new DiscreteAttributeDomain();
		  }
		 else if (domain.getType() == AttributeDomainType.CONTINUOUS)
		  { throw new IOException ("Line "+scanReader.getLineNumber()+
": continuous domains cannot have symbolic entries");
		  }
	       }
	      else
	       { if (domain == null)
		  { domain = new ContinuousAttributeDomain();
		  }
		 else if (domain.getType() == AttributeDomainType.DISCRETE)
		  { throw new IOException ("Line "+scanReader.getLineNumber()+
": discrete domains cannot have numeric entries");
		  }
		 try
		  { ordval = Double.parseDouble (ordstr);
		  }
		 catch (NumberFormatException e)
		  { throw new IOException ("Line "+scanReader.getLineNumber()+
": malformed numeric domain entry " + ordstr);
		  }
	       }
	      try 
	       { val = scanReader.scanDouble ();
	       }
	      catch (EOFException e)
	       { throw e;
	       }
	      catch (IOException e)
	       { throw new IOException ("Line " + scanReader.getLineNumber() + 
": expected numeric domain weight");
	       }
	      if (val < 0 || val > 1.0)
	       { throw new IOException ("Line " + scanReader.getLineNumber() + 
": weight " + val + " should be within [0,1]");
	       } 
	      if (domain.getType() == AttributeDomainType.DISCRETE)
	       { domain.getDiscrete().addElement(ordstr, val);
	       }
	      else
	       { domain.getContinuous().addKnot(ordval, val);
	       }
	      c = scanReader.scanChar (" %c");
	      if (c == '}')
	       { break;
	       }
	      else if (c != ',')
	       { throw new IOException (
"Line " + scanReader.getLineNumber() + ": missing ','"); 
	       }
	    }	   
	   return domain;
	 }

    public abstract AttributeDomain getDeepCopy();
    
    public abstract ContinuousAttributeDomain getContinuous();
    
    public abstract DiscreteAttributeDomain getDiscrete();
}
