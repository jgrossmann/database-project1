
/*
This class holds each individual web result from bing after
parsing the returned query results
*/
public class WebResult {
	String title;
	String description;
	String url;
	private boolean isRelevant;

	public WebResult(){
		this.title = null;
		this.description = null;
		this.url = null;
	}

	public WebResult(String title, String description, String url) {
		this.title = title;
		this.description = description;
		this.url = url;
	}

	public void isRelevant(boolean relevant) {
		isRelevant = relevant;
	}

	public boolean isRelevant() {
		return isRelevant;
	}

}
