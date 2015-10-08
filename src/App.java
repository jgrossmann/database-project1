import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.io.*;
import java.util.*;

import org.apache.commons.codec.binary.Base64;

public class App {

	//account key for bing api
	private static final String ACCOUNT_KEY = "BU3X9a6Qbmi7UwCgwo3iuHTfOqbU5PWVjuEul/WzOLk";


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
		byte[] accountKeyBytes = Base64.encodeBase64((ACCOUNT_KEY + ":" + ACCOUNT_KEY).getBytes());
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

		//The content string is the xml/json output from Bing.
		//System.out.println(results);

    List<String> queryWords = new ArrayList<String>();
    for(String word : query.split("\\s+")) {
      queryWords.add(word);
    }
    
		WebResultsHandler resultsHandler = new WebResultsHandler(results, precision, transcript, queryWords);
		boolean precisionMet = false;
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
		//here we may need to hanle ending the program in a certain way.
		
		transcript.close();
	}

}
