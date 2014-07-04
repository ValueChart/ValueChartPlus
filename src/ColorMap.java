import java.awt.Color;



/**
 * Contains information for a colour associated with a name.
 * 
 *
 */
public class ColorMap{
    String color;
    int red;
    int green;
    int blue;        
   
    ColorMap(String c, int r, int g, int b){
        color = c;
        red = r;
        green = g;
        blue = b;
    }
    
    String getColorString(){
        return color;
    }
    
    Color getColor() {
        return new Color(red, green, blue);
    }
    
    String getCodeInt(){
        return new String(red + " " + green + " " + blue);
    }
    
    String getCodeDec() {
        return new String(((double)red)/255 + " " + ((double)green)/255 + " " + ((double)blue)/255);
    }
}
