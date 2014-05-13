

public class ColorMap{
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
