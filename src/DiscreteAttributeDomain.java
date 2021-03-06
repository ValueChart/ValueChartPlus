import java.util.*;

/**
 * Contains a LinkedHashMap that maps x-axis values to y-axis values [0,1] for discrete value/utility/score functions 
 * 
 * Just like ContinuousAttributeDomain, this is the other class which can control most of the interaction
 * you will need to do with the domain.
 * Some more comments are attempted below
*/
public class DiscreteAttributeDomain extends AttributeDomain
{

	LinkedHashMap<String, Double> entryMap;

        //The rest of the functions are self-explanatory.
	DiscreteAttributeDomain()
	 { super();
	   entryMap = new LinkedHashMap<String, Double>();
	 }

	public AttributeDomainType getType()
	 { return AttributeDomainType.DISCRETE;
	 }
	
	public Double getWeight (String elem) { 
	    Double e =  entryMap.get(elem);
        if (e == null) {
            throw new IllegalArgumentException("element " + elem + " not found");
        }
        return e;
	 }
	
	public boolean setWeight(String elem, Double weight) {
	    if (!entryMap.containsKey(elem)) return false;
	    entryMap.put(elem, weight);
	    return true;
	}

    public String[] getElements() {
        String[] elems = new String[entryMap.size()];
        int index = 0;
        for (Iterator<String> it = entryMap.keySet().iterator(); it.hasNext();) {
            elems[index++] = it.next();
        }
        return elems;
    }

    public double[] getWeights() {
        double[] weights = new double[entryMap.size()];
        int index = 0;
        for (Map.Entry<String, Double> entry : entryMap.entrySet()) {
            weights[index++] = entry.getValue();
        }
        return weights;
    }

    public void addElement(String elem, double weight) {
        entryMap.put(elem, weight);
    }

    public void removeElement(String elem) {
        entryMap.remove(elem);
    }

	//added for utility graph: so position does not change
	public void changeWeight(String elem, double wt){
		if(wt > 1.0)
		    setWeight(elem, Math.min(wt, 1.0));
		else if(wt < 0.0)
		    setWeight(elem, Math.max(wt, 0.0));
		else
		    setWeight(elem, wt);
	}

    @Override
    public AttributeDomain getDeepCopy() {
        DiscreteAttributeDomain newData = new DiscreteAttributeDomain();
        for (Map.Entry<String, Double> entry : entryMap.entrySet()) {
            newData.addElement(entry.getKey(), entry.getValue());
        }
        return newData;
    }

    @Override
    public ContinuousAttributeDomain getContinuous() {
        return null;
    }

    @Override
    public DiscreteAttributeDomain getDiscrete() {
        return this;
    }

    @Override
    public void resetWeights() {
        for (Map.Entry<String, Double> entry : entryMap.entrySet()) {
            entry.setValue(0.5);
        }
    }
	
    public LinkedHashMap<String, Double> getEntryMap() {
        return entryMap;
    }
}
