import java.util.*;
import java.io.*;
import java.util.Map.Entry;

public class WebResultsHandler {
	double precision;
	PrintWriter transcript;
	DocParser DocResults;
	List<String> queryWords;
	VectorList docVectors;	
	static List<String> StopWords = Arrays.asList("a", "an", "and", "are", "as", "at", "be", "by", "for", "from",
	"has", "he", "in", "is", "it", "its", "of", "on", "that", "the", "to", "was", "were", "will", "with");

	public WebResultsHandler(String results, double precision, PrintWriter transcript, List<String> queryWords) {
		this.precision = precision;
		this.transcript = transcript;
		this.queryWords = queryWords;
		DocResults = new DocParser();
		DocResults.getEntries(results);
	}


	public void println(String text) {
		System.out.println(text);
		transcript.println(text);
	}

	
	public void print(String text) {
		System.out.print(text);
		transcript.print(text);
	}



	public boolean relevanceFeedback() {
		//iterate over all results and record each relevance based on user feedback
		//return true if precision is met, false otherwise
		int numRelevant = 0;
		Scanner in = new Scanner(System.in);
		int i = 0;
		for(WebResult result : DocResults.entryList) {
			println("Result "+i);
			println("[");
			println(" URL:  "+result.url);
			println(" Title:  "+result.title);
			println(" Summary:  "+result.description);
			println("]\n");
			
			while(true) {
				print("Relevant (Y/N)?");
				String feedback = in.nextLine();
				transcript.println(feedback);
				if(feedback.equalsIgnoreCase("N")) {
					result.isRelevant(false);
					break;
				}else if(feedback.equalsIgnoreCase("Y")) {
					result.isRelevant(true);
					numRelevant++;
					break;
				}else {
					println("Please enter only 'Y' or 'N'");
				}	
			}
			i++;
		}
		
		if((numRelevant / 10.0) >= this.precision) {
		  return true;
		}else if(numRelevant == 0) {
		  println("0 Relevant documents in results. Ending Program...");
		  return true;
		}else {
		  return false;
	  }
	}
	
	public HashMap<String, Double> getQueryVector() {
	  HashMap<String, Double> map = new HashMap<String, Double>();
	  for(String word : queryWords) {
	    map.put(word, 1.0);
	  }
	  return map;
	}
	
	public HashMap<String, Double> sumVectors(List<HashMap<String, Double>> vectors) {
	  HashMap<String, Double> weightedVector = new HashMap<String, Double>();
	  for(HashMap<String, Double> vector: vectors) {
	  
	    for(String key : vector.keySet()) {
	    
	      Double weight = 0.0;
	      if(weightedVector.containsKey(key)) {
	        weight = weightedVector.get(key) + vector.get(key);
	      }else {
	        weight = vector.get(key);
	      }
	      weightedVector.put(key, weight);
	      
	    }
	  }
	  
	  return weightedVector;
	}
	
	public HashMap<String, Double> rocchio(List<DocumentVector> docVectors) {
	  //Double ALPHA = 1.0;
	  Double BETA = .75;
	  Double GAMMA = .25;
	  int numRelevant = 0, numIrrelevant = 0;
	  
	  HashMap<String, Double> queryVector = new HashMap<String, Double>();
	  
	  List<HashMap<String, Double>> relevantVectors = new ArrayList<HashMap<String, Double>>();
	  List<HashMap<String, Double>> irrelevantVectors = new ArrayList<HashMap<String, Double>>();
	  //find num relevant/irrelevant
	  for(DocumentVector vector : docVectors) {
	    if(vector.isRelevant) {
	      numRelevant++;
	      relevantVectors.add(vector.vector);
	    }else {
	      numIrrelevant++;
	      irrelevantVectors.add(vector.vector);
	    }
	  }
	  
	  Double relevantWeight = BETA / numRelevant;
	  Double irrelevantWeight = GAMMA / numIrrelevant;
	  
	  //sum relevant vectors
	  HashMap<String, Double> summedRelevant = sumVectors(relevantVectors);
	 
	  //sum irrelevant vectors
	  HashMap<String, Double> summedIrrelevant = sumVectors(irrelevantVectors);
	  
	  
	  //weight and subtract irrelevant summed vector from weighted relevant summed vector
	  
	  //add relevant summed vector to query vector
	  for(String key : summedRelevant.keySet()) {
	    Double weight = 0.0;
	    if(queryVector.containsKey(key)) {
	      weight = queryVector.get(key) + (summedRelevant.get(key) * relevantWeight);
	    }else {
	      weight = summedRelevant.get(key) * relevantWeight;
	    }
	    queryVector.put(key, weight);
	  }
	  
	  //subtract irrelevant summed vector from query vector
	  for(String key : summedIrrelevant.keySet()) {
	    Double weight = 0.0;
	    if(queryVector.containsKey(key)) {
	      weight = queryVector.get(key) - (summedIrrelevant.get(key) * irrelevantWeight);
	      if(weight < 0.0) {
	        weight = 0.0;
	      }
	    }else {
	      weight = 0.0;
	    }
	    queryVector.put(key, weight);
	  }
	  
	  return queryVector;
	}
	
	public String[] getTopWords(HashMap<String, Double> vector) {
	  class WordWeight {
	    Double weight;
	    String word;
	    public WordWeight(String word, Double weight) {
	      this.weight = weight;
	      this.word = word;
	    }
	  }
	  class WordWeightComparator implements Comparator<WordWeight> {
        public int compare(WordWeight a, WordWeight b) {
            if(a.weight > b.weight) {
                return -1;
            }else if(a.weight < b.weight) {
                return 1;
            }else {
                return 0;
            }
        }
        
        public boolean equals(Object a, Object b) {
            WordWeight A = (WordWeight)a;
            WordWeight B = (WordWeight)b;
            if(A.word.equalsIgnoreCase(B.word)) {
                if(A.weight == B.weight) {
                    return true;
                }
            }
            return false;
        }
      }
	  List<String> query = new ArrayList<String>();
	  for(String word : queryWords) {
	    query.add(word.toLowerCase());
	  }
	  queryWords = new ArrayList<String>(query);
	  List<WordWeight> queryWeight = new ArrayList<WordWeight>();
	  
	  Comparator<WordWeight> comp = new WordWeightComparator();
	  PriorityQueue<WordWeight> queue = new PriorityQueue<WordWeight>(2, comp);
	  
	  for(Map.Entry<String, Double> entry : vector.entrySet()) {
	    if(queryWords.contains(entry.getKey().toLowerCase())) {
	        queryWeight.add(new WordWeight(entry.getKey().toLowerCase(), entry.getValue()));
	        //queryWords.remove(entry.getKey().toLowerCase());
	        continue;
	    }
	    if(StopWords.contains(entry.getKey())) {
	        continue;
	    }
	    if(entry.getValue() > .2) {
	      System.out.println(entry.getKey() + " "+ entry.getValue());
	    }
	    queue.offer(new WordWeight(entry.getKey(), entry.getValue()));
	  }
	  
	  boolean in;
	  for(String word : new ArrayList<String>(query)) {
	    in = false;
	    for(WordWeight entry : new ArrayList<WordWeight>(queryWeight)) {
	        if(entry.word.equalsIgnoreCase(word)) {
	            in = true;
	        }
	    }
	    if(!in) {
	        queryWeight.add(new WordWeight(word.toLowerCase(), 0.0));
	    }
	  }
	  
	  
	  String[] words = new String[2+queryWeight.size()];
	  PriorityQueue<WordWeight> newQueue = new PriorityQueue<WordWeight>(2+queryWeight.size(), comp);
	  newQueue.offer(queue.poll());
	  newQueue.offer(queue.poll());
	  
	  for(WordWeight entry : queryWeight) {
	    newQueue.offer(entry);
	  }
	  
	  int size = newQueue.size();
	  for(int i=0; i<size; i++) {
	    WordWeight entry = newQueue.poll();
	    words[i] = entry.word;
	    System.out.println(entry.word+" "+entry.weight);
	  }
	  
	  return words;
	}

	public String formNewQuery() {
	  //List<DocumentVector> docVectors = ...
	  
	  
		List<DocumentVector> ld = new ArrayList<DocumentVector>();
		for(WebResult wr : DocResults.entryList)
		{
			DocumentVector dv = new DocumentVector(wr.description, queryWords);
			dv.isRelevant = wr.isRelevant();
			ld.add(dv);
		}
		docVectors = new VectorList(ld);
		docVectors.getDocumentFrequency();
		docVectors.getTFIDF();

    HashMap<String, Double> optimizedQueryVector = rocchio(docVectors.list);
      String[] newQueryWords = new String[2+queryWords.size()];
	  newQueryWords = getTopWords(optimizedQueryVector);
	  
	  String query = "";
	  int i = 0;
	  for(; i<newQueryWords.length - 1; i++) {
	    String word = newQueryWords[i];
	    query += word+" ";
	  }
	  query += newQueryWords[i];
	  
	  return query;
	}


}
