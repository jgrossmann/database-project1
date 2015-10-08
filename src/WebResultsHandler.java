import java.util.*;
import java.io.*;
import java.util.Map.Entry;

/*

This class handles the raw web results from bing, the relevance feedback,
and the query expansion. 

*/

public class WebResultsHandler {
	double precision;
	PrintWriter transcript;
	DocParser DocResults;
	List<String> queryWords;
	VectorList docVectors;	
	
	//list of basic stopwords to remove from document vectors.
	static List<String> StopWords = Arrays.asList("a", "an", "and", "are", "as", "at", "be", "by", "for", "from",
	"has", "he", "in", "is", "it", "its", "of", "on", "that", "the", "to", "was", "were", "will", "with");

	public WebResultsHandler(String results, double precision, PrintWriter transcript, List<String> queryWords) {
		this.precision = precision;
		this.transcript = transcript;
		this.queryWords = new ArrayList<String>();
		for(String word : queryWords) {
		    this.queryWords.add(word.toLowerCase());
		}
		DocResults = new DocParser();
		DocResults.getEntries(results);
		println("Total no of results : "+DocResults.entryList.size());
		if(DocResults.entryList.size() < 10) {
		    println("There are less than 10 results from Bing. Exiting now...");
		    transcript.close();
		    System.exit(0);
		}
	}


    //function to print to screen as well as to transcript with newline
	public void println(String text) {
		System.out.println(text);
		transcript.println(text);
	}

	
	//function to print to screen as well as to transcript
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
		println("Bing Search Results:");
		println("======================");
		//feedback loop
		for(WebResult result : DocResults.entryList) {
			println("Result "+i);
			println("[");
			println(" URL:  "+result.url);
			println(" Title:  "+result.title);
			println(" Summary:  "+result.description);
			println("]\n");
			
			while(true) {
			    System.out.println("Relevant (Y/N)? ");
				String feedback = in.nextLine();
				transcript.println(feedback);
				if(feedback.equalsIgnoreCase("N")) {
				    transcript.println("Relevant: NO");
					result.isRelevant(false);
					break;
				}else if(feedback.equalsIgnoreCase("Y")) {
				    transcript.println("Relevant: YES");
					result.isRelevant(true);
					numRelevant++;
					break;
				}else {
					println("Please enter only 'Y' or 'N'");
				}	
			}
			i++;
		}
		
		println("======================");
		println("FEEDBACK SUMMARY");
		print("Query");
		for(String word : queryWords) {
		    print(" "+word);
		}
		println("");
		
		double precision = numRelevant / 10.0;
		println("Precision "+precision);
		
		if(precision >= this.precision) {
		  println("Desired precision reached, done");
		  return true;
		}else if(numRelevant == 0) {
		  println("0 Relevant documents in results. Ending Program...");
		  return true;
		}else {
		  println("Still below the desired precision of 0.9");
		  return false;
	  }
	}
	
	//returns the query words as a query vector
	public HashMap<String, Double> getQueryVector() {
	  HashMap<String, Double> map = new HashMap<String, Double>();
	  for(String word : queryWords) {
	    map.put(word, 1.0);
	  }
	  return map;
	}
	
	//function which sums the weights of each word across a list of document vectors
	//returns a single vector containing all the summed weights of each word.
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
	
	
	//function which performs rocchio algorithm on a list of document vectors
	//and a query vector (class variable)
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
	
	
	//this function gets the top two highest weighted words from the
	//document vectors, then reorders the query with the new words
	//according to their weights in the document vectors. 
	public String[] getTopWords(HashMap<String, Double> vector) {
	
	  List<WordWeight> queryWeight = new ArrayList<WordWeight>();
	  
	  Comparator<WordWeight> comp = new WordWeightComparator();
	  PriorityQueue<WordWeight> queue = new PriorityQueue<WordWeight>(2, comp);
	  
	  //create priority queue for each word in the document vectors
	  for(Map.Entry<String, Double> entry : vector.entrySet()) {
	  
	    //add queryword to separate queryWeight list
	    if(queryWords.contains(entry.getKey().toLowerCase())) {
	        queryWeight.add(new WordWeight(entry.getKey().toLowerCase(), entry.getValue()));
	        continue;
	    }
	    
	    if(StopWords.contains(entry.getKey())) {
	        continue;
	    }
	    
	    queue.offer(new WordWeight(entry.getKey(), entry.getValue()));
	  }
	  
	  boolean in;
	  //if a queryword was not found in the document vectors, 
	  //add it with a weight of 0
	  for(String word : new ArrayList<String>(queryWords)) {
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
	  
	  //return the reordered query including new top words
	  return reorderQuery(queryWeight, queue.poll(), queue.poll());
	}
	
	
	//This function takes the query words as well as the 2 new top words 
	//from the rocchio algorithm. It returns the reordered query ordered
	//from highest weighted word, to lowest.
	public String[] reorderQuery(List<WordWeight> query, WordWeight word1, WordWeight word2) {
	    println("Augmenting by  "+word1.word+" "+word2.word);
        String[] words = new String[2+query.size()];
        Comparator<WordWeight> comp = new WordWeightComparator();
        PriorityQueue<WordWeight> newQueue = new PriorityQueue<WordWeight>(2+query.size(), comp);
        newQueue.offer(word1);
        newQueue.offer(word2);
      
        for(WordWeight entry : query) {
            newQueue.offer(entry);
        }
      
        int size = newQueue.size();
        for(int i=0; i<size; i++) {
            WordWeight entry = newQueue.poll();
            words[i] = entry.word;
        }
        return words;
	}
	

    //This is the handler function which handles the creation
    //of a new query from the relevance feedback on the most
    //recent results. Returns a string of query terms separated
    //by spaces.
	public String formNewQuery() {
	    println("Indexing results ... ");
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
