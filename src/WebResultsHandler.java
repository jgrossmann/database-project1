import java.util.*;
import java.io.*;

public class WebResultsHandler {
	double precision;
	PrintWriter transcript;
	DocParser DocResults;
	VectorList docVectors;	

	public WebResultsHandler(String results, double precision, PrintWriter transcript) {
		this.precision = precision;
		this.transcript = transcript;
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
		
		return true;
	}

	public String formNewQuery() {
		//forms new query based on which webresults are relevant and other metrics
		List<DocumentVector> ld = new ArrayList<DocumentVector>();
		for(WebResult wr : DocResults.entryList)
		{
			DocumentVector dv = new DocumentVector(wr.description);
			dv.isRelevent = wr.isRelevant();
			ld.add(dv);
		}
		docVectors = new VectorList(ld);
		docVectors.getDocumentFrequency();
		docVectors.getTFIDF();
		return null;
	}

}
