import java.util.*;
import java.io.*;

public class WebResultsHandler {
	List<WebResult> results;
	double precision;
	PrintWriter transcript;

	public WebResultsHandler(String results, double precision, PrintWriter transcript) {
		this.precision = precision;
		this.transcript = transcript;
		parseResults(results);
	}


	private void parseResults(String results) {
		//parse results from web into a list of WebResult objects
	}

	public boolean relevanceFeedback() {
		//iterate over all results and record each relevance based on user feedback
		//return true if precision is met, false otherwise
		return true;
	}

	public String formNewQuery() {
		//forms new query based on which webresults are relevant and other metrics
		return null;
	}

}
