import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.*;
import javax.xml.parsers.*;

public class XMLParser {
    
    public static void readDescriptions(ValueChart chart) {
        // read description file
        try {
            String descFile = chart.filename.substring(0,chart.filename.length()-3) + ".xml";
            File xmlFile = new File(descFile);
            if (xmlFile.exists() && !xmlFile.isDirectory()) {
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(xmlFile);

                doc.getDocumentElement().normalize();
                
                NodeList nList = doc.getElementsByTagName("criterion");
                for (int temp = 0; temp < nList.getLength(); temp++) {
                    Node nNode = nList.item(temp);
                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        
                        Element eElement = (Element) nNode;
                        
                        // TODO error checking
                        String attrName = eElement.getAttribute("name");
                        AttributeData attrData = chart.getAttribute(attrName);
                        String htmlText = eElement.getElementsByTagName("htmlData").item(0).getTextContent();
                        
                        Pattern pat = Pattern.compile("<html>");
                        Matcher mat = pat.matcher(htmlText);
                        int start = -1;
                        if(mat.find())
                            start = mat.start();
                        
                        pat = Pattern.compile("</html>");
                        mat = pat.matcher(htmlText);
                        int end = -1;
                        if(mat.find())
                            end = mat.start();
                        
                        htmlText = htmlText.substring(start, end + 7);
                        
                        if (attrData != null && !attrData.isAbstract()) {
                            attrData.getPrimitive().setDescription(htmlText);
                        }
                    }
                }
                
                nList = doc.getElementsByTagName("alternative");
                for (int temp = 0; temp < nList.getLength(); temp++) {
                    Node nNode = nList.item(temp);
                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        
                        Element eElement = (Element) nNode;
                        
                        // TODO error checking
                        String entryName = eElement.getAttribute("name");
                        ChartEntry entry = chart.getEntry(entryName);
                        String text = eElement.getElementsByTagName("textData").item(0).getTextContent();
                        
                        if (entry != null) {
                            entry.setDescription(text);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
