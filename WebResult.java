

public class WebResult {
	String title;
	String description;
	String url;
	private boolean isRelevant;

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
