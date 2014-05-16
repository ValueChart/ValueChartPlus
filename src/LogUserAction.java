import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Vector;


public class LogUserAction {

    public static final int LOG_NONE = 1;
    public static final int LOG_MSG = 2; // message head and passed strings only
    public static final int LOG_CHANGE = 3; // log changes only
    public static final int LOG_ALL = 4; // log changes and attribute values
    
    public static final int OUTPUT_STATE = 1; // output attribute weight and utility
    public static final int OUTPUT_WEIGHT = 2; // output attribute weight only
    //public static final int OUTPUT_UTILITY = 3; // output utility values only
    
    private String filename;
    private DateFormat df;
    private int verbosity;
    
    private String oldAttrData = "";
    
    public LogUserAction(String vcName) {
        df = new SimpleDateFormat("ddMMMyyyy_HHmmss");
        
        filename = "vc_" + vcName + "_" + df.format(Calendar.getInstance().getTime()) + ".xml";
        
        df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        setVerbosity(LOG_ALL);
    }

    public String getFilename() {
        return filename;
    }
    
    public int getVerbosity() {
        return verbosity;
    }

    public void setVerbosity(int verbosity) {
        this.verbosity = verbosity;
    }
    
    public String getOldAttrData() {
        return oldAttrData;
    }

    public void setOldAttrData(String oldAttrData) {
        this.oldAttrData = oldAttrData;
    }

    public static String getDataOutput(Vector<AttributeData> attrData, int outputType) {
        String data = "";
        if (outputType == OUTPUT_STATE) {
            for (AttributeData a : attrData) {
                data = data + a.getOutputLogXML(true);
            }
        }
        else if (outputType == OUTPUT_WEIGHT) {
            for (AttributeData a : attrData) {
                data = data + a.getOutputWeightXML(true);
            }
        }
        return data;
    }
    
    public static String getSingleDataOutput(AttributeData attrData, int outputType) {
        String data = "";
        if (outputType == OUTPUT_STATE) {
            data = attrData.getOutputLogXML(true);
        }
        else if (outputType == OUTPUT_WEIGHT) {
            data = attrData.getOutputWeightXML(true);
        }

        return data;
    }
    
    public boolean logConstruction(Vector<AttributeData> newAttr) {
        if (verbosity < LOG_MSG) return true;
        try {
            FileWriter fw = new FileWriter(filename, true);
            fw.write("<Log ");
            fw.write("timestamp=\"" + df.format(Calendar.getInstance().getTime()) + "\">\n");
            
            fw.write("<Action type=\"construction\"/>\n");
            
            if (verbosity >= LOG_ALL) {
                fw.write("<State type=\"original\">\n");
                fw.write(oldAttrData);
                fw.write("</State>\n");
            }
            if (verbosity >= LOG_CHANGE) {
                fw.write("<State type=\"new\">\n");
                fw.write(getDataOutput(newAttr, OUTPUT_STATE));
                fw.write("</State>\n");
            }
            
            fw.write("</Log>\n");
            fw.close();
            return true;
        } catch (Exception ex) {
            System.out.println("Exception: " + ex);
        }
        return false;
    }
    
    public boolean logPump(Vector<AttributeData> newAttr, String pumpAttr, boolean isIncrease) {
        if (verbosity < LOG_MSG) return true;
        try {
            FileWriter fw = new FileWriter(filename, true);
            fw.write("<Log ");
            fw.write("timestamp=\"" + df.format(Calendar.getInstance().getTime()) + "\">\n");
            
            fw.write("<Action type=\"pump\">\n");
            fw.write("<Pump attribute=\"" + pumpAttr + "\" increase=\"" + (isIncrease ? "true" : "false") + "\"/>\n");
            fw.write("</Action>\n");
            
            if (verbosity >= LOG_ALL) {
                fw.write("<State type=\"original\">\n");
                fw.write(oldAttrData);
                fw.write("</State>\n");
            }
            if (verbosity >= LOG_CHANGE) {
                fw.write("<State type=\"new\">\n");
                fw.write(getDataOutput(newAttr, OUTPUT_WEIGHT));
                fw.write("</State>\n");
            }
            
            fw.write("</Log>\n");
            fw.close();
            return true;
        } catch (Exception ex) {
            System.out.println("Exception: " + ex);
        }
        return false;
    }
    
    public boolean logUtility(AttributeData newAttr) {
        if (verbosity < LOG_MSG) return true;
        try {
            FileWriter fw = new FileWriter(filename, true);
            fw.write("<Log ");
            fw.write("timestamp=\"" + df.format(Calendar.getInstance().getTime()) + "\">\n");
            
            fw.write("<Action type=\"utility\">\n");
            fw.write("<Utility attribute=\"" + newAttr.getName() + "\"/>\n");
            fw.write("</Action>\n");
            
            if (verbosity >= LOG_ALL) {
                fw.write("<State type=\"originalPartial\">\n");
                fw.write(oldAttrData);
                fw.write("</State>\n");
            }
            if (verbosity >= LOG_CHANGE) {
                fw.write("<State type=\"newPartial\">\n");
                fw.write(getSingleDataOutput(newAttr, OUTPUT_STATE));
                fw.write("</State>\n");
            }
            
            fw.write("</Log>\n");
            fw.close();
            return true;
        } catch (Exception ex) {
            System.out.println("Exception: " + ex);
        }
        return false;
    }

    public boolean log(String s, String msg) {
        if (verbosity < LOG_MSG) return true;
        if (msg.isEmpty() && verbosity < LOG_ALL) return true;
       try {
            FileWriter fw = new FileWriter(filename, true);
            fw.write("<Log ");
            fw.write("timestamp=\"" + df.format(Calendar.getInstance().getTime()) + "\">\n");
            
            if (!msg.isEmpty()) {
                fw.write("<Message>\n");
                fw.write(msg);
                fw.write("</Message>\n");
            }
            
            if (verbosity >= LOG_ALL) {
                fw.write(s);
            }
            
            fw.write("</Log>\n");
            fw.close();
            return true;
        } catch (Exception ex) {
            System.out.println("Exception: " + ex);
        }
        return false;
    }

    public boolean logAttributeState(Vector<AttributeData> attrData, String msg) {
        if (verbosity < LOG_MSG) return true;
        if (msg.isEmpty() && verbosity < LOG_ALL) return true;
       try {
            FileWriter fw = new FileWriter(filename, true);
            fw.write("<Log ");
            fw.write("timestamp=\"" + df.format(Calendar.getInstance().getTime()) + "\">\n");
            
            if (!msg.isEmpty()) {
                fw.write("<Message>\n");
                fw.write(msg);
                fw.write("</Message>\n");
            }
            
            if (verbosity >= LOG_ALL) {
                fw.write("<State type=\"current\">\n");
                fw.write(getDataOutput(attrData, OUTPUT_WEIGHT));
                fw.write("</State>\n");
            }
            
            fw.write("</Log>\n");
            fw.close();
            return true;
        } catch (Exception ex) {
            System.out.println("Exception: " + ex);
        }
        return false;
    }
    
    public boolean logAttributeWeight(Vector<AttributeData> attrData, String msg) {        
        if (verbosity < LOG_MSG) return true;
        if (msg.isEmpty() && verbosity < LOG_ALL) return true;
       try {
            FileWriter fw = new FileWriter(filename, true);
            fw.write("<Log ");
            fw.write("timestamp=\"" + df.format(Calendar.getInstance().getTime()) + "\">\n");
            
            if (!msg.isEmpty()) {
                fw.write("<Message>\n");
                fw.write(msg);
                fw.write("</Message>\n");
            }
            
            if (verbosity >= LOG_ALL) {
                fw.write("<State type=\"current\">\n");
                fw.write(getDataOutput(attrData, OUTPUT_WEIGHT));
                fw.write("</State>\n");
            }
            
            fw.write("</Log>\n");
            fw.close();
            return true;
        } catch (Exception ex) {
            System.out.println("Exception: " + ex);
        }
        return false;
    }
    
    public boolean logDrag(AttributeData newAttr1, AttributeData newAttr2) {        
        if (verbosity < LOG_MSG) return true;
        try {
            FileWriter fw = new FileWriter(filename, true);
            fw.write("<Log ");
            fw.write("timestamp=\"" + df.format(Calendar.getInstance().getTime()) + "\">\n");
            
            fw.write("<Action type=\"drag\">\n");
            fw.write("<Drag attribute1=\"" + newAttr1.getName() 
                    + "\" attribute2=\"" + newAttr2.getName() + "\"/>\n");
            fw.write("</Action>\n");
            
            if (verbosity >= LOG_ALL) {
                fw.write("<State type=\"originalPartial\">\n");
                fw.write(oldAttrData);
                fw.write("</State>\n");
            }
            if (verbosity >= LOG_CHANGE) {
                fw.write("<State type=\"newPartial\">\n");
                fw.write(getSingleDataOutput(newAttr1, OUTPUT_WEIGHT));
                fw.write(getSingleDataOutput(newAttr2, OUTPUT_WEIGHT));
                fw.write("</State>\n");
            }
            
            fw.write("</Log>\n");
            fw.close();
            return true;
        } catch (Exception ex) {
            System.out.println("Exception: " + ex);
        }
        return false;
    }
}


