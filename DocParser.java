import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class DocParser {
	
	Document dom;
	List<WebResult> entryList;
	
	public DocParser(){
		dom = null;
		entryList = new ArrayList<WebResult>();
	}
	
	private void getDocument(String s){
	
		//String ns = s.substring(s.indexOf("<entry>"), s.lastIndexOf("</feed>"));
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		try{
			DocumentBuilder db = dbf.newDocumentBuilder();
			dom = db.parse(new InputSource(new ByteArrayInputStream(s.getBytes("utf-8"))));
			
		}catch(ParserConfigurationException x){
			System.out.print("Parse error");
			x.printStackTrace();
		}catch(SAXException se){
			se.printStackTrace();
		}catch(IOException io){
			io.printStackTrace();
		}
	}
	
	private void parseDocument(){
		//get the root element
		Element docEle = dom.getDocumentElement();

		//get a nodelist of elements
		NodeList nl = docEle.getElementsByTagName("entry");
		if(nl != null && nl.getLength() > 0) {
			for(int i = 0 ; i < nl.getLength();i++) {

				//get the employee element
				Element el = (Element)nl.item(i);

				//get the Employee object
				WebResult e = getWebResult(el);

				//add it to list
				entryList.add(e);
			}
		}
	}
	
	private WebResult getWebResult(Element e){
		WebResult wr = new WebResult();
		wr.title = e.getElementsByTagName("d:Title").item(0).getTextContent();
		wr.url = e.getElementsByTagName("d:Url").item(0).getTextContent();
		wr.description = e.getElementsByTagName("d:Description").item(0).getTextContent();
		return wr;
	}
	
	public void getEntries(String s)
	{
		getDocument(s);
		parseDocument();
	}
	
	
}

