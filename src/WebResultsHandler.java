import java.util.*;
import java.io.*;
import java.util.Map.Entry;

public class WebResultsHandler {
	double precision;
	PrintWriter transcript;
	DocParser DocResults;
	List<String> queryWords;
	VectorList docVectors;	

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
	
	public String[] getTopTwoWords(HashMap<String, Double> vector) {
	  class WordWeight {
	    Double weight;
	    String word;
	    public WordWeight(String word, Double weight) {
	      this.weight = weight;
	      this.word = word;
	    }
	  }
	  
	  WordWeight[] topwords = new WordWeight[2];
	  for(Map.Entry<String, Double> entry : vector.entrySet()) {
	    if(entry.getValue() > .2) {
	      System.out.println(entry.getKey() + " "+ entry.getValue());
	    }
	    if(topwords[0] == null) {
	      topwords[0] = new WordWeight(entry.getKey(), entry.getValue());
	    }else if(topwords[1] == null) {
	      topwords[1] = new WordWeight(entry.getKey(), entry.getValue());
	    }else {
	      if(entry.getValue() > topwords[1].weight) {
	        if(entry.getValue() > topwords[0].weight) {
	          topwords[1] = topwords[0];
	          topwords[0] = new WordWeight(entry.getKey(), entry.getValue());
	        }else {
	          topwords[1] = new WordWeight(entry.getKey(), entry.getValue());
	        }
	      }
	    }
	  }
	  
	  String[] words = {topwords[0].word, topwords[1].word};
	  System.out.println(topwords[0].word+" "+topwords[0].weight);
	  System.out.println(topwords[1].word+" "+topwords[1].weight);
	  
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
	  for(String newWord : getTopTwoWords(optimizedQueryVector)) {
	    queryWords.add(newWord);
	  }
	  
	  String query = "";
	  int i = 0;
	  for(; i<queryWords.size() - 1; i++) {
	    String word = queryWords.get(i);
	    query += word+" ";
	  }
	  query += queryWords.get(i);
	  
	  return query;
	}


}
