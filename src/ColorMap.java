

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
    
    String getColor(){
        return color;
    }
    
    String getCodeInt(){
        return new String(red + " " + green + " " + blue);
    }
    
    String getCodeDec() {
        return new String(((double)red)/255 + " " + ((double)green)/255 + " " + ((double)blue)/255);
    }
}
