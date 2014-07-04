import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class XMLWriter {

    

    public static void saveFile(ValueChart chart, String filename) {
        if (chart == null || filename == null) return;
        Document dom;
        Element e = null;

        // instance of a DocumentBuilderFactory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            // use factory to get an instance of document builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            // create instance of DOM
            dom = db.newDocument();

            // create the root element
            Element rootEle = dom.createElement(XMLParser.XML_ROOT);
            rootEle.setAttribute("problem", chart.getChartTitle());

            if (chart.reportExists()) {
                e = dom.createElement(XMLParser.XML_REPORT);
                e.setAttribute("file", chart.reportFile.toString());
                rootEle.appendChild(e);
            }
            
            Vector<AttributeData> attrData = chart.getAttrData();
            Vector<ChartEntry> entryList = chart.getEntryList();
            
            Element colors = dom.createElement(XMLParser.XML_COLORS);
            ColorList colorList = new ColorList();
            for (AttributeData data : attrData) {
                saveColors(dom, colors, data, colorList);
            }
            rootEle.appendChild(colors);
            
            Element crits = dom.createElement(XMLParser.XML_CRITERIA);
            for (AttributeData data : attrData) {
                saveCriteria(dom, crits, data);
            }
            rootEle.appendChild(crits);
            
            Element alts = dom.createElement(XMLParser.XML_ALTERNATIVES);
            for (ChartEntry entry : entryList) {
                saveAlternative(dom, alts, entry);
            }
            rootEle.appendChild(alts);


            dom.appendChild(rootEle);

            try {
                Transformer tr = TransformerFactory.newInstance().newTransformer();
                tr.setOutputProperty(OutputKeys.INDENT, "yes");
                tr.setOutputProperty(OutputKeys.METHOD, "xml");
                tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

                // send DOM to file
                tr.transform(new DOMSource(dom), 
                                     new StreamResult(new FileOutputStream(filename)));

            } catch (TransformerException te) {
                System.out.println(te.getMessage());
            } catch (IOException ioe) {
                System.out.println(ioe.getMessage());
            }
        } catch (ParserConfigurationException pce) {
            System.out.println("Error trying to instantiate DocumentBuilder " + pce);
        }
    }

    private static void saveColors(Document dom, Element colorNode, AttributeData data, ColorList colorList) {
        if (data.isAbstract()){
            for (AttributeData d : data.getAbstract().getChildren()) {
                saveColors(dom, colorNode, d, colorList);
            }
        } else {
            Element e = dom.createElement(XMLParser.XML_COLOR);
            e.setAttribute("name", data.getName());
            
            Color c = data.getPrimitive().getColor();
            // if the obj already has a color assigned to it...
            if (c != Color.WHITE) {
                e.setAttribute("r", Integer.toString(c.getRed()));
                e.setAttribute("g", Integer.toString(c.getGreen()));
                e.setAttribute("b", Integer.toString(c.getBlue()));
            }
            // if not, assign one
            else {
                ColorMap cm = colorList.get(0); 
                c = cm.getColor();
                e.setAttribute("r", Integer.toString(c.getRed()));
                e.setAttribute("g", Integer.toString(c.getGreen()));
                e.setAttribute("b", Integer.toString(c.getBlue()));
                colorList.remove(0);
                colorList.add(cm);
            }
            colorNode.appendChild(e);
        }
    }

    private static void saveCriteria(Document dom, Element parentNode, AttributeData data) {
        if (dom == null || parentNode == null || data == null) return;
        
        Element elem = dom.createElement(XMLParser.XML_CRITERION);
        elem.setAttribute("name", data.getName());
        if (data.isAbstract()) {
            elem.setAttribute("type", "abstract");
            for (AttributeData child : data.getAbstract().getChildren()) {
                saveCriteria(dom, elem, child);
            }
        } else {
            elem.setAttribute("type", "primitive");
            AttributePrimitiveData prim = data.getPrimitive();
            elem.setAttribute("weight", Double.toString(prim.getWeight()));
            
            Element domainElem = dom.createElement(XMLParser.XML_DOMAIN);
            if (prim.getDomain().getType() == AttributeDomainType.CONTINUOUS) {
                domainElem.setAttribute("type", "continuous");
                ContinuousAttributeDomain domain = prim.getDomain().getContinuous();
                domainElem.setAttribute("unit", prim.getUnitsName());
                for (Map.Entry<Double, Double> val : domain.getKnotMap().entrySet()) {
                    Element v = dom.createElement(XMLParser.XML_CONT_VAL);
                    v.setAttribute("x", Double.toString(val.getKey()));
                    v.setAttribute("y", Double.toString(val.getValue()));
                    domainElem.appendChild(v);
                }
            } else {
                domainElem.setAttribute("type", "discrete");
                DiscreteAttributeDomain domain = prim.getDomain().getDiscrete();
                for (Map.Entry<String, Double> val : domain.getEntryMap().entrySet()) {
                    Element v = dom.createElement(XMLParser.XML_DISC_VAL);
                    v.setAttribute("x", val.getKey());
                    v.setAttribute("y", Double.toString(val.getValue()));
                    domainElem.appendChild(v);
                }
            }
            elem.appendChild(domainElem);
            if (data.hasDescription()) {
                Element d = dom.createElement(XMLParser.XML_DESCRIPTION);
                d.setAttribute("name", data.getName());
                d.setTextContent("<![CDATA[" + data.getPrimitive().getDescription() + "]]>");
                elem.appendChild(d);
            }
        }
        
        parentNode.appendChild(elem);
    }
    
    
    private static void saveAlternative(Document dom, Element parentNode, ChartEntry entry) {
        if (dom == null || parentNode == null || entry == null) return;
        
        Element elem = dom.createElement(XMLParser.XML_ALTERNATIVE);
        elem.setAttribute("name", entry.name);
        for (Map.Entry<String, AttributeValue> val : entry.map.entrySet()) {
            Element v = dom.createElement(XMLParser.XML_ALT_VAL);
            v.setAttribute("criterion", val.getKey());
            v.setAttribute("value", val.getValue().stringValue());
            elem.appendChild(v);
        }
        if (entry.hasDescription()) {
            Element d = dom.createElement(XMLParser.XML_DESCRIPTION);
            d.setAttribute("name", entry.name);
            d.setTextContent(entry.getDescription());
            elem.appendChild(d);
        }
        
        parentNode.appendChild(elem);
    }
}
