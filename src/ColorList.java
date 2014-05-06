import java.util.Vector;

class ColorList extends Vector{  
	private static final long serialVersionUID = 1L;

	ColorList(){
		add(new ColorMap("darkred", 0.5, 0.0, 0.0));
	    add(new ColorMap("gold", 1.0, 0.8, 0.0));
	    add(new ColorMap("orange", 1.0, 0.6, 0.0));
	    add(new ColorMap("earth", 0.72, 0.48, 0.20));
	    add(new ColorMap("brown", 0.5, 0.25, 0.0));  
	    add(new ColorMap("blue", 0.07, 0.07, 0.3));  
	    add(new ColorMap("gray", 0.7, 0.7, 0.7));  
	    add(new ColorMap("green", 0.0, 0.7, 0.0));
	    add(new ColorMap("yellow", 1.0, 1.0, 0.0));
	    add(new ColorMap("red", 0.8, 0.0, 0.0));
	}
	
	String getColorEntry(int index){
		ColorMap col = (ColorMap)this.get(index);
		return new String(col.getColor() + " " + col.getCode());
	}
	
	String getColorCode(int index){
		ColorMap col = (ColorMap)this.get(index);
		return new String(col.getCode());		
	}
	
	String getColorName(int index){
		ColorMap col = (ColorMap)this.get(index);
		return col.getColor();
	}
	
	class ColorMap{
	   	String color;
	   	double red;
	   	double green;
	   	double blue;    	
	   
	   	ColorMap(String c, double r, double g, double b){
	   		color = c;
	   		red = r;
	   		green = g;
				blue = b;
	   	}
	   	
	   	String getColor(){
	   		return color;
	   	}
	   	
	   	String getCode(){
	   		return new String(red + " " + green + " " + blue);
	   	}
	}
}