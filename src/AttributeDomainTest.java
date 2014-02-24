import acme.misc.ScanfReader;
import java.io.*;
//This class does not seems to be related to the Value Charts
class AttributeDomainTest
{
	public static void test (String s, Object res, double[] valcheck)
	 {
	   Exception error = null;
	   AttributeDomain domain = null;
	   try
	    { domain = AttributeDomain.read (
		 new ScanfReader (new StringReader (s)));
	    }
	   catch (Exception e)
	    { error = e;
	    }
	   if (res instanceof EOFException)
	    { if (error == null || !(error instanceof EOFException))
	       { System.out.println ("Error, input " + s + ":");
		 System.out.println ("Expected EOFException");
		 System.exit(1); 
	       }
	    }
	   else if (res instanceof IOException)
	    { if (error == null)
	       { System.out.println ("Error, input " + s + ":");
		 System.out.println ("Expected IOException");
		 System.exit(1); 
	       }
	      else
	       { IOException check = (IOException)res;
		 if (!error.getMessage().equals (check.getMessage()))
		  { System.out.println ("Error, input " + s + ":");
		    System.out.println ("Expected IOException '" +
					check.getMessage() + "', got '" +
					error.getMessage() + "'");
		    System.exit(1);
		  }
	       }
	    }
	   else if (res instanceof String[])
	    { if (error != null)
	       { System.out.println ("Error, input " + s + ":");
		 System.out.println ("Unexpected " +
				     error.getClass().getName() + " " +
				     error.getMessage());
		 System.exit(1); 
	       }
	      if (domain.getType() != AttributeDomain.DISCRETE)
	       { System.out.println ("Error, input " + s + ":");
		 System.out.println ("Expected discrete domain");
		 System.exit(1);
	       }
	      String[] check = (String[])res;
	      String[] elements = domain.getElements();
	      if (check.length != elements.length)
	       { System.out.println ("Error, input " + s + ":");
		 System.out.println ("Expected " + check.length + " elements");
		 System.exit(1); 
	       }
	      for (int i=0; i<check.length; i++)
	       { if (!check[i].equals (elements[i]))
		  { 
		    System.out.println ("Error, input " + s + ":");
		    System.out.println (
"Element " + elements[i] + " at i=" + i + " unexpected");
		    System.exit(1); 
		  }
	       }
	      double[] values = domain.getWeights();
	      for (int i=0; i<values.length; i++)
	       { if (valcheck[i] != values[i])
		  { System.out.println ("Error, input " + s + ":");
		    System.out.println (
"Value " + values[i] + " at i=" + i + " unexpected");
		    System.exit(1); 
		  }
	       }
	    }
	   else if (res instanceof double[])
	    { if (error != null)
	       { System.out.println ("Error, input " + s + ":");
		 System.out.println ("Unexpected " +
				     error.getClass().getName() + " " +
				     error.getMessage());
		 System.exit(1); 
	       }
	      if (domain.getType() != AttributeDomain.CONTINUOUS)
	       { System.out.println ("Error, input " + s + ":");
		 System.out.println ("Expected continuous domain");
		 System.exit(1);
	       }
	      double[] check = (double[])res;
	      double[] knots = domain.getKnots();
	      if (check.length != knots.length)
	       { System.out.println ("Error, input " + s + ":");
		 System.out.println ("Expected " + check.length + " knots");
		 System.exit(1); 
	       }
	      for (int i=0; i<check.length; i++)
	       { if (check[i] != knots[i])
		  { System.out.println ("Error, input " + s + ":");
		    System.out.println (
"Knot " + knots[i] + " at i=" + i + " unexpected");
		    System.exit(1); 
		  }
	       }
	      double[] values = domain.getWeights();
	      for (int i=0; i<values.length; i++)
	       { if (valcheck[i] != values[i])
		  { System.out.println ("Error, input " + s + ":");
		    System.out.println (
"Value " + values[i] + " at i=" + i + " unexpected");
		    System.exit(1); 
		  }
	       }
	    }
	 }

	public static void main (String[] args)
	 {
	   test ("{ foo 0.15 , bar 0.19 }",
		 new String[] {"foo", "bar" },
		 new double[] {0.15, 0.19 });

	   test ("{foo 0.15,bar 0.19}",
		 new String[] {"foo", "bar" },
		 new double[] {0.15, 0.19 });

	   test ("{\" foo\" 0.15,\"bar bat\" .19}",
		 new String[] {" foo", "bar bat" },
		 new double[] {.15, .19 });

	   test ("foo .15,bar -1}",
		 new IOException (
"Line 1: expected '{'"), null);

	   test ("{ }",
		 new IOException (
"Line 1: expected domain entry"), null);

	   test ("{1.0 .15,bar -1}",
		 new IOException (
"Line 1: continuous domains cannot have symbolic entries"), null);

	   test (" {  foo .15,6.7 -1}",
		 new IOException (
"Line 1: discrete domains cannot have numeric entries"), null);

	   test ("{ 1.0 .15, -gg .20 }",
		 new IOException (
"Line 1: malformed numeric domain entry -gg"), null);

	   test ("{foo .15,bar baz}",
		 new IOException (
"Line 1: expected numeric domain value"), null);

	   test ("{foo .15,bar -1}",
		 new IOException (
"Line 1: value should be within [0,1]"), null);

	   test ("{foo .15,bar 1.01}",
		 new IOException (
"Line 1: value should be within [0,1]"), null);

	   test ("{foo .15 bar}",
		 new IOException (
"Line 1: missing ','"), null);

	   test ("{ 100.0 .20, 200.0 .33, 300.0 .50 }",
		 new double[] {100, 200, 300},
		 new double[] {.20, .33, .50});

	   test ("{ 200.0 .33, 100.0 .20, 300.0 .50 }",
		 new double[] {100, 200, 300},
		 new double[] {.20, .33, .50});

	   test ("{ 300 .50, 200.0 .33, 100.0 .20 }",
		 new double[] {100, 200, 300},
		 new double[] {.20, .33, .50});

	   System.out.println ("\nPassed\n");
	 }
}
