import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.io.*;
import java.util.*;

import org.apache.commons.codec.binary.Base64;

/*

Program for COMSE6111 Advanced Database Systems
Authors: John Grossmann, 

Usage: ./run.sh <bing account key> <precision@10> <'query terms'>

This program performs relevance feedback on the first 10 of the initial query term 
search results from bing. The goal is to get greater than or equal to the 
precision@10 argument on relevance feedback. If the precision@10 is not met,
the program performs query expansion to choose up to two new query terms to add
to the query. Then, a new query is sent to bing, and the process starts over again.

*/


public class App {

	//account key for bing api
	//private static final String ACCOUNT_KEY = "BU3X9a6Qbmi7UwCgwo3iuHTfOqbU5PWVjuEul/WzOLk";
    private String account_key;
    

	//takes a string with spaces as word delimiters and returns a bingURL
	public static String createUrl(String query) {

		String[] queryWords = query.split("\\s+");
		String bingUrl = "https://api.datamarket.azure.com/Bing/Search/Web?Query=%27"; 
    int i = 0;
    for(; i < queryWords.length-1; i++) {
      bingUrl += queryWords[i] + "%20";
    }
    bingUrl += queryWords[i] + "%27&$top=10&$format=Atom";

		return bingUrl;
	}


	//takes a bingUrl as input and returns the results as a string from bing.
	public static String getResults(String bingUrl) throws IOException {
		byte[] accountKeyBytes = Base64.encodeBase64((account_key + ":" + account_key).getBytes());
		String accountKeyEnc = new String(accountKeyBytes);

		URL url = new URL(bingUrl);
		URLConnection urlConnection = url.openConnection();
		urlConnection.setRequestProperty("Authorization", "Basic " + accountKeyEnc);
				
		InputStream inputStream = (InputStream) urlConnection.getContent();		
		byte[] contentRaw = new byte[urlConnection.getContentLength()];
		inputStream.read(contentRaw);
		String content = new String(contentRaw);
		return content;
	}


	
	public static void main(String[] args) throws IOException {
    if(args.length < 3) {
        System.out.println("Usage: please run ./run.sh <bing account key> <precision> <query>");
	    System.out.println("<query> is your query, a list of words in single quotes (e.g., ‘Milky Way’)");
	    System.out.println("<precision> is the target value for precision@10, a real between 0 and 1");
        return;
    }
        account_key = args[0];
		double precision = 0.0;
    try {
      precision = Double.parseDouble(args[0]);
    }catch(NumberFormatException e) {
      e.printStackTrace();
      return;
    }
	
		File transcriptFile = new File("transcript.txt");
		if(transcriptFile == null) {
			System.out.println("can't create a file in the current directory");
			return;		
		}

		
		PrintWriter transcript = new PrintWriter(new FileOutputStream(transcriptFile, false));
		int i = 1;
		String query = "";
		for(;i<args.length-1; i++) {
		  query += args[i]+" ";
		}
		query += args[i];
    System.out.println("First query: "+query);
    
		String url = createUrl(query);	
		String results = getResults(url);


        List<String> queryWords = new ArrayList<String>();
        for(String word : query.split("\\s+")) {
          queryWords.add(word);
        }
    
		WebResultsHandler resultsHandler = new WebResultsHandler(results, precision, transcript, queryWords);
		boolean precisionMet = false;
		
		//relevance feedback loop
		while((precisionMet = resultsHandler.relevanceFeedback()) == false) {
			query = resultsHandler.formNewQuery();
			url = createUrl(query);
			results = getResults(url);
			queryWords = new ArrayList<String>();
			System.out.print("New query is: [");
			for(String word : query.split("\\s+")) {
                queryWords.add(word);
                System.out.print(word+", ");
            }
            System.out.println("]");
			resultsHandler = new WebResultsHandler(results, precision, transcript, queryWords);
		}

		//the webresultshandler should printout the success or failure of each feedback
		
		transcript.close();
	}

}
