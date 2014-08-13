import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringEscapeUtils;

import org.w3c.dom.*;
import javax.xml.parsers.*;

/**
 * Contains helper methods to parse XML file data.
 * 
 *
 */
public class XMLParser {
   
    public static final String XML_CRITERIA = "Criteria",
                                XML_CRITERION = "Criterion",
                                XML_ALTERNATIVES = "Alternatives",
                                XML_ALTERNATIVE = "Alternative",
                                XML_COLORS = "Colors",
                                XML_COLOR = "Color",
                                XML_ROOT = "ValueCharts",
                                XML_REPORT = "Report",
                                XML_DESCRIPTION = "Description",
                                XML_DOMAIN = "Domain",
                                XML_ALT_VAL = "AlternativeValue",
                                XML_CONT_VAL = "ContinuousValue",
                                XML_DISC_VAL = "DiscreteValue";
        
    public static void readDescriptions(ValueChart chart) {
        // read description file
        try {
            String descFile = chart.filename.substring(0,chart.filename.length()-3) + ".xml";
            File xmlFile = new File(descFile);
            if (xmlFile.exists() && !xmlFile.isDirectory()) {
//                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
//                Document doc = dBuilder.parse(xmlFile);
                
                Document doc = PositionalXMLReader.readXML(descFile);
                
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
            UserDialog.showError("Error in line " + e.getMessage(), "File Error", chart.getFrame());
        }
    }
    
    public static ChartData parseSaveFile(ValueChart vc, String filename) {
        
        File xmlFile = new File(filename);
        if (!xmlFile.exists() || xmlFile.isDirectory())
            return null;
        
        ChartData chartData = new ChartData(vc);
        
        try {
//            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
//            Document doc = dBuilder.parse(xmlFile);
            Document doc = PositionalXMLReader.readXML(filename);
    
            doc.getDocumentElement().normalize();
            
            // get the report file
            if (doc.getElementsByTagName(XML_REPORT).getLength() > 0) {
                String report = ((Element)doc.getElementsByTagName(XML_REPORT).item(0)).getAttribute("file");
                vc.reportFile = new File(report.replace("$", " ").replace("\\", "\\\\"));
                if (vc.reportFile.exists()) {
                    vc.createReportController();
                } else {
                    //since the report does not exist, throw a system message letting the user know this, and set the report to null
                    String msg = "The report for the ValueChart is not valid, the system believes the report is located at: ";
                    msg += "  " + vc.reportFile.toString();
                    msg += "  If this is an error, please verify that you have correctly substituted all spaces and added two slashes between directories.";
                    System.out.println(msg);
                    UserDialog.showError(msg, "Report File Error", vc.getFrame());
                    report = null;
                }
            }
            
            // parse the colors
            NodeList colors = ((Element)doc.getElementsByTagName(XML_COLORS).item(0)).getElementsByTagName(XML_COLOR);
            HashMap<String, Color> colorMap = new HashMap<String, Color>();
            for (int i = 0; i < colors.getLength(); i++) {
                Node node = colors.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    parseColor((Element) node, colorMap);
                }
            }
            
            // parse the criteria
            NodeList crits = (doc.getElementsByTagName(XML_CRITERIA).item(0)).getChildNodes();
            for (int i = 0; i < crits.getLength(); i++) {
                Node node = crits.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    parseCriteria((Element) node, chartData, null, colorMap);
                }
            }
            
            // parse the alternatives
            NodeList alts = (doc.getElementsByTagName(XML_ALTERNATIVES).item(0)).getChildNodes();
            for (int i = 0; i < alts.getLength(); i++) {
                Node node = alts.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    parseAlternative((Element) node, chartData, vc);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            UserDialog.showError("Error in line " + ex.getMessage(), "File Error", vc.getFrame());
        }
        
        return chartData;

    }

    private static void parseColor(Element node, HashMap<String, Color> colorMap) throws IOException {
        try {
            Color c = new Color(Integer.parseInt(node.getAttribute("r")), 
                                Integer.parseInt(node.getAttribute("g")), 
                                Integer.parseInt(node.getAttribute("b")));
            colorMap.put(node.getAttribute("name"), c);
        } catch (Exception e) {
            throw new IOException(node.getUserData(PositionalXMLReader.LINE_NUMBER).toString());
        }
    }

    
    private static void parseCriteria(Element attrElem, ChartData chartData, AttributeData parentAttr, HashMap<String, Color> colorMap) 
                throws IOException {
        try {
            if (attrElem.getAttribute("type").equals("abstract")) {
                AttributeAbstractData attr = new AttributeAbstractData();
                attr.setName(attrElem.getAttribute("name"));
                NodeList nList = attrElem.getChildNodes();
                for (int i = 0; i < nList.getLength(); i++) {
                    Node node = nList.item(i);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        parseCriteria((Element)node, chartData, attr, colorMap);
                    }
                }
                chartData.addAttributeData(parentAttr, attr);
            } 
            else {
                AttributePrimitiveData attr = new AttributePrimitiveData();
                attr.setName(attrElem.getAttribute("name"));
                attr.setWeight(Double.parseDouble(attrElem.getAttribute("weight")));
                attr.setColor(colorMap.get(attr.getName()));
                if (attrElem.getElementsByTagName(XML_DESCRIPTION).getLength() > 0) {
                    String htmlText = StringEscapeUtils.unescapeJava(
                            attrElem.getElementsByTagName(XML_DESCRIPTION).item(0).getTextContent());
                    
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
                    
                    attr.setDescription(htmlText);
                }
                
                Element domainElem = (Element) attrElem.getElementsByTagName(XML_DOMAIN).item(0);
                if (domainElem.getAttribute("type").equals("continuous"))
                    attr.setUnitsName(domainElem.getAttribute("unit"));
                attr.setDomain(parseDomain(domainElem));
                chartData.addAttributeData(parentAttr, attr);
            }
        } catch (IOException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IOException(attrElem.getUserData(PositionalXMLReader.LINE_NUMBER).toString());
        }
    }
    
    private static AttributeDomain parseDomain(Element domainElem) throws IOException {
        if (domainElem == null) return null;
        try {
            if (domainElem.getAttribute("type").equals("continuous")) {
                ContinuousAttributeDomain domain = new ContinuousAttributeDomain();
                NodeList nList = domainElem.getElementsByTagName(XML_CONT_VAL);
                for (int i = 0; i < nList.getLength(); i++) {
                    Node node = nList.item(i);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        Element e = (Element) node;
                        domain.addKnot(Double.parseDouble(e.getAttribute("x")), 
                                       Double.parseDouble(e.getAttribute("y")));
                    }
                }
                return domain;
            } else {
                DiscreteAttributeDomain domain = new DiscreteAttributeDomain();
                NodeList nList = domainElem.getElementsByTagName(XML_DISC_VAL);
                for (int i = 0; i < nList.getLength(); i++) {
                    Node node = nList.item(i);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        Element e = (Element) node;
                        domain.addElement(e.getAttribute("x"), 
                                       Double.parseDouble(e.getAttribute("y")));
                    }
                }
                return domain;
            }
        } catch (Exception ex) {
            throw new IOException(domainElem.getUserData(PositionalXMLReader.LINE_NUMBER).toString());
        }
    }
    
    private static void parseAlternative(Element altNode, ChartData chartData, ValueChart vc) throws IOException {
        if (altNode == null || chartData == null) return;
        
        ChartEntry entry = new ChartEntry(altNode.getAttribute("name"));
        try {
            NodeList nList = altNode.getElementsByTagName(XML_ALT_VAL);
            for (int i = 0; i < nList.getLength(); i++) {
                Node node = nList.item(i);
                try {
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        Element e = (Element) node;
                        String critName = e.getAttribute("criterion"); 
                        AttributeData data = chartData.findAttribute(critName);
                        if (data != null) {
                            AttributeDomain domain = data.getPrimitive().getDomain();
                            if (domain.getType() == AttributeDomainType.CONTINUOUS) {
                                AttributeValue attrVal = new AttributeValue(Double.parseDouble(e.getAttribute("value")),
                                                                            domain, critName, vc);
                                entry.map.put(critName, attrVal);
                            } else {
                                AttributeValue attrVal = new AttributeValue(e.getAttribute("value"),
                                                                            domain, critName, vc);
                                entry.map.put(critName, attrVal);
                            }
                        }
                    }
                } catch (Exception ex) {
                    throw new IOException(node.getUserData(PositionalXMLReader.LINE_NUMBER).toString());
                }
            }
            if (altNode.getElementsByTagName(XML_DESCRIPTION).getLength() > 0) {
                entry.setDescription(StringEscapeUtils.unescapeJava(
                        altNode.getElementsByTagName(XML_DESCRIPTION).item(0).getTextContent()));
            }
        } catch (Exception ex) {
            throw new IOException(altNode.getUserData(PositionalXMLReader.LINE_NUMBER).toString());
        }
        
        entry.setReport(vc.reportFile);
        entry.setReportFrame(vc.reportWindow);
        entry.setReportController(vc.controller);
        entry.setOutlineItem(vc.item);
        
        chartData.addChartEntry(entry);
    }

}
