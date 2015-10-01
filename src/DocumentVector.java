import java.util.HashMap;
import java.util.StringTokenizer;

public class DocumentVector {
	public HashMap<String,Double> vector;
	public HashMap<String,Integer> termFreq;
	public boolean isRelevent;
	public int wordCount;
	
	public DocumentVector(String doc){
		vector = new HashMap<String,Double>();
		termFreq = new HashMap<String, Integer>();
		getDocumnetTermFrequency(doc);
		
	}
	
	private void getDocumnetTermFrequency(String doc){
		StringTokenizer st = new StringTokenizer(doc, " ,.?\n");
		while(st.hasMoreElements())
		{
			wordCount++;
			String term = (String) st.nextElement();
			if(termFreq.containsKey(term))
			{
				int old = termFreq.get(term);
				termFreq.put(term, old + 1);
			}
			else{
				termFreq.put(term, 1);
			}
		}
		System.out.println(termFreq);
	}
	
	
}

