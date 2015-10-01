import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.*;

public class DocumentVector {
	public HashMap<String,Double> vector;
	public HashMap<String,Integer> termFreq;
	public boolean isRelevant;
	public int wordCount;
	public List<String> queryWords;
	
	public DocumentVector(String doc, List<String> words){
		vector = new HashMap<String,Double>();
		termFreq = new HashMap<String, Integer>();
		queryWords = new ArrayList<String>();
		for(String word : words) {
		  queryWords.add(word.toLowerCase());
		}
		getDocumentTermFrequency(doc);
		
	}
	
	private void getDocumentTermFrequency(String doc){
		StringTokenizer st = new StringTokenizer(doc, " ,.?\n");
		while(st.hasMoreElements())
		{
			wordCount++;
			String term = (String) st.nextElement();
			if(!queryWords.contains(term.toLowerCase())) {
			  
			  if(termFreq.containsKey(term))
			  {
				  int old = termFreq.get(term);
				  termFreq.put(term, old + 1);
			  }
			  else{
				  termFreq.put(term, 1);
			  }
			}
		}
		//System.out.println(termFreq);
	}
	
	
}

