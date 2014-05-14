import java.util.Vector;

class ColorList extends Vector<ColorMap>{  
	private static final long serialVersionUID = 1L;

	ColorList(){
		add(new ColorMap("palegreen", 120/255, 198/255, 121/255));
	    add(new ColorMap("yellow", 254/255, 196/255, 79/255));
	    add(new ColorMap("darkorange", 204/255, 76/255, 2/255));
	    add(new ColorMap("darkred", 103/255, 0, 13/255));
	    add(new ColorMap("bluegreen", 1/255, 102/255, 94/255));  
	    add(new ColorMap("green", 102/255, 166/255, 30/255));  
	    add(new ColorMap("red", 227/255, 26/255, 28/255));  
	    add(new ColorMap("purple", 84/255, 39/255, 143/255));
	    add(new ColorMap("paleblue", 103/255, 169/255, 207/255));
	    add(new ColorMap("darkpink", 158/255, 1/255, 66/255));
        add(new ColorMap("brown", 102/255, 37/255, 6/255));
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
	

}