package arbuckle.app;


import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/*
 * Parses Arbuckle Menu. Will be deprecated once we implement a class that grabs values from mySQL database
 */
public class ArbMenuXMLHandler extends DefaultHandler {

	String elementValue = null;
	Boolean elementOn = false;
	private static ArbMenuXMLGetSet data = new ArbMenuXMLGetSet();

	public static ArbMenuXMLGetSet getXMLData(){
		return data;
	}

	public  void setXMLData (ArbMenuXMLGetSet data){
		ArbMenuXMLHandler.data = data;
	}

	public void startElement (String uri, String localName, String qName, Attributes attributes) throws SAXException{
		elementOn = true;
		if (localName.equals(root)) data.setDate(attributes.getValue(date));
	}

	public void endElement (String uri, String localName, String qName) throws SAXException{
		elementOn = false;
		if (localName.equals(key)) data.setKey(elementValue);
		if (localName.equals(title)) data.setTitle(elementValue);
		if (localName.equals(details)) data.setDetails(elementValue);
		if (localName.equals(price)) data.setPrice(elementValue);
		if (localName.equalsIgnoreCase(station)) data.setElement();
	}



	public void characters (char[] ch, int start, int length) throws SAXException{
		if (elementOn){
			elementValue = new String(ch, start, length);
			elementOn = false;
		}
	}
	private static final String root = "Document";
	private static final String key = "txtStationDescription";
	private static final String title = "txtTitle";
	private static final String details = "txtDescription";
	private static final String price = "txtPrice";
	private static final String station = "tblStation";
	private static final String date = "menudate";


}
