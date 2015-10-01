import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

public class VectorList {
	
	public List<DocumentVector> list;
	public HashMap<String, Integer> docFreq;
	
	public VectorList(List<DocumentVector> plist){
		list = plist;
		docFreq = new HashMap<String, Integer>();
	}
	
	public void getDocumentFrequency(){
		for(DocumentVector dv : list)
		{
			Iterator<Entry<String, Integer>> it = dv.termFreq.entrySet().iterator();
			while(it.hasNext()){
				Entry<String, Integer> e = it.next();
				String key = e.getKey();
				int val = e.getValue();
				if(docFreq.containsKey(key)){
					docFreq.put(key, val+1);
				}
				else{
					docFreq.put(key, 1);
				}
			}
		}
	}
	
	public void getTFIDF(){
		for(DocumentVector dv : list)
		{
			Iterator<Entry<String, Integer>> it = dv.termFreq.entrySet().iterator();
			while(it.hasNext()){
				Entry<String, Integer> e = it.next();
				String key = e.getKey();
				int val = e.getValue();
				int df = docFreq.get(key);
				double idf = Math.log10(list.size()/df);
				dv.vector.put(key, val*idf);
			}
		}
	}

}

