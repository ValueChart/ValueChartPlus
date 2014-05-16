import java.util.*;

//This class is the basis of ChartEntry. To my knowledge, this class has little to do with he interface itself. One thing to note though.
//is that HashMap is the datatype for a Chart Entry. I am not sure if this is a good idea rather than using an object. Since HashMap has certain
//problems to maintaining consistency and maybe hard to organize for interface presentation
public class ChartEntry
{
	String name;
	HashMap<String, Object> map;
	HashSet maskingAttributes;
	/**/private boolean showFlag;
	/**/private boolean isMarked;

	public ChartEntry (String name){
		this.name = name;
		map = new HashMap<String, Object>();
		maskingAttributes = new HashSet();
		/**/showFlag=false;
	}

	public boolean isMasked(){
		return !maskingAttributes.isEmpty();
	}

	public boolean addMaskingAttribute (AttributeCell attr){
		return maskingAttributes.add (attr);
	}

	public boolean removeMaskingAttribute (AttributeCell attr){
	   return maskingAttributes.remove (attr);
	}

	public AttributeValue attributeValue (String name){
	   return (AttributeValue)map.get(name);
	}

	/**/
	public void setShowFlag(boolean b){
		showFlag = b;		
	}
	
	public boolean getShowFlag(){
		return showFlag;		
	}

	public void setIsMarked(boolean b){
		isMarked = b;		
	}
	
	public boolean getIsMarked(){
		return isMarked;		
	}

	
}
