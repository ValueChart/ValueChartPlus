import java.util.HashMap;
import java.util.Vector;


public class ChartData {
    ValueChart chart;
    
    private Vector<AttributeData> attrData;
    private Vector<ChartEntry> entryList;
    
    // locate AttributeData by criteria name
    private HashMap<String, AttributeData> attrMap;
    // locate ChartEntry by alternative name
    private HashMap<String, ChartEntry> entryMap;
    

    private Vector<AttributeData> oldAttrData;
    private Vector<ChartEntry> oldChartEntry;
    private HashMap<String, AttributeData> oldAttrMap;
    private HashMap<String, ChartEntry> oldEntryMap;
    
    public ChartData(ValueChart vc) {
        chart = vc;
        
        attrData = new Vector<AttributeData>();
        entryList = new Vector<ChartEntry>();
        
        attrMap = new HashMap<String, AttributeData>();
        entryMap = new HashMap<String, ChartEntry>();
        
        oldAttrData = null;
        oldChartEntry = null;
        oldAttrMap = null;
        oldEntryMap = null;
    }
    
    public Vector<AttributeData> getAttrData() {
        return attrData;
    }

    public void setAttrData(Vector<AttributeData> attrData) {
        this.attrData = attrData;
    }

    public Vector<ChartEntry> getEntryList() {
        return entryList;
    }

    public void setEntryList(Vector<ChartEntry> entryList) {
        this.entryList = entryList;
    }

    public HashMap<String, AttributeData> getAttrMap() {
        return attrMap;
    }

    public void setAttrMap(HashMap<String, AttributeData> attrMap) {
        this.attrMap = attrMap;
    }

    public HashMap<String, ChartEntry> getEntryMap() {
        return entryMap;
    }

    public void setEntryMap(HashMap<String, ChartEntry> entryMap) {
        this.entryMap = entryMap;
    }

    public Vector<AttributeData> getOldAttrData() {
        return oldAttrData;
    }

    public void setOldAttrData(Vector<AttributeData> oldAttrData) {
        this.oldAttrData = oldAttrData;
    }

    public Vector<ChartEntry> getOldChartEntry() {
        return oldChartEntry;
    }

    public void setOldChartEntry(Vector<ChartEntry> oldChartEntry) {
        this.oldChartEntry = oldChartEntry;
    }

    public HashMap<String, AttributeData> getOldAttrMap() {
        return oldAttrMap;
    }

    public void setOldAttrMap(HashMap<String, AttributeData> oldAttrMap) {
        this.oldAttrMap = oldAttrMap;
    }

    public HashMap<String, ChartEntry> getOldEntryMap() {
        return oldEntryMap;
    }

    public void setOldEntryMap(HashMap<String, ChartEntry> oldEntryMap) {
        this.oldEntryMap = oldEntryMap;
    }

    public void keepCurrentState(ValueChart vc) {
        oldAttrData = new Vector<AttributeData>(attrData.size());
        oldChartEntry = new Vector<ChartEntry>(entryList.size());
        oldAttrMap = new HashMap<String, AttributeData>();
        oldEntryMap = new HashMap<String, ChartEntry>();
        
        for (AttributeData attr : attrData) {
            AttributeData newAttr = attr.getDeepCopy(oldAttrMap); 
            oldAttrData.add(newAttr);
            oldAttrMap.put(attr.getName(), newAttr);
        }
        for (ChartEntry entry : entryList) {
            ChartEntry newEntry = entry.getDeepCopy(oldAttrData, chart);
            oldChartEntry.add(newEntry);
            oldEntryMap.put(entry.name, newEntry);
        }
        
    }
    
    public void restoreState() {
        if (oldAttrData == null || oldChartEntry == null) return;
        
        attrData = oldAttrData;
        entryList = oldChartEntry;
        attrMap = oldAttrMap;
        entryMap = oldEntryMap;
    }
    
    public void addAttributeData(AttributeData parent, AttributeData newData) {
        if (newData == null) return;
        
        if (parent != null && parent.isAbstract()) {
            parent.getAbstract().addChild(newData);
            attrMap.put(newData.getName(), newData);
        } else {
            attrData.add(newData);
            attrMap.put(newData.getName(), newData);
        }
    }
    
    public void addChartEntry(ChartEntry newEntry) {
        if (newEntry == null) return;
        
        entryList.add(newEntry);
        entryMap.put(newEntry.name, newEntry);
    }

    public AttributeData findAttribute(String name) {
        return attrMap.get(name);
    }
    
    
}
