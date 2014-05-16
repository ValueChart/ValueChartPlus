import java.util.Vector;

class ColorList extends Vector<ColorMap>{  
	private static final long serialVersionUID = 1L;

	ColorList(){
		add(new ColorMap("palegreen", 120, 198, 121));
	    add(new ColorMap("yellow", 254, 196, 79));
	    add(new ColorMap("darkorange", 204, 76, 2));
        add(new ColorMap("red", 227, 26, 28));  
	    add(new ColorMap("bluegreen", 1, 102, 94));  
	    add(new ColorMap("green", 102, 166, 30));  
        add(new ColorMap("darkred", 103, 0, 13));
	    add(new ColorMap("purple", 84, 39, 143));
	    add(new ColorMap("paleblue", 103, 169, 207));
	    add(new ColorMap("darkpink", 158, 1, 66));
        add(new ColorMap("brown", 102, 37, 6));
	}
	
	String getColorEntry(int index){
		ColorMap col = (ColorMap)this.get(index);
		return new String(col.getColor() + " " + col.getCodeDec());
	}
	
	String getColorCode(int index){
		ColorMap col = (ColorMap)this.get(index);
		return new String(col.getCodeDec());		
	}
	
	String getColorName(int index){
		ColorMap col = (ColorMap)this.get(index);
		return col.getColor();
	}
	

}