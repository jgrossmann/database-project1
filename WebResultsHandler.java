import java.util.*;
import java.io.*;

public class WebResultsHandler {
	double precision;
	PrintWriter transcript;
	

	public WebResultsHandler(String results, double precision, PrintWriter transcript) {
		this.precision = precision;
		this.transcript = transcript;
		parseResults(results);
	}


	public void println(String text) {
		System.out.println(text);
		transcript.println(text);
	}

	
	public void print(String text) {
		System.out.print(text);
		transcript.print(text);
	}


	private void parseResults(String results) {
		//parse results from web into a list of WebResult objects
	}

	public boolean relevanceFeedback() {
		//iterate over all results and record each relevance based on user feedback
		//return true if precision is met, false otherwise
		int numRelevant = 0;
		Scanner in = new Scanner(System.in);
		int i = 0;
		for(WebResult result : results) {
			println("Result "+i);
			println("[");
			println(" URL:  "+result.url);
			println(" Title:  "+result.title);
			println(" Summary:  "+result.summary);
			println("]\n");
			
			while(true) {
				print("Relevant (Y/N)?");
				String feedback = in.nextLine()
				transcript.println(feedback);
				if(feedback.equalsIgnoreCase("N")) {
					result.isRelevant(false);
				}else if(feedback.equalsIgnoreCase("Y")) {
					result.isRelevant(true);
					numRelevant++;
				}else {
					println("Please enter only 'Y' or 'N'");
				}	
			}
			i++;
		}
		
		return true;
	}

	public String formNewQuery() {
		//forms new query based on which webresults are relevant and other metrics
		return null;
	}

}
