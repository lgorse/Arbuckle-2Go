package arbuckle.app;

import java.util.LinkedHashMap;

public class ArbMenuXMLGetSet {

	LinkedHashMap<String, String[]> elementMap = new LinkedHashMap<String, String[]>();
	String key;
	String title;
	String price;
	String details;
	String date;
	
	
	public void setDate(String date){
		this.date = date;
	}
	
	public String getDate(){
		return date;
	}
	public void setKey(String key){
		this.key = key;	
	}
	
	public void setTitle(String title){
		this.title = title;
	}
	
	public void setPrice(String price){
		this.price = price;
	}
	
	public void setDetails(String details){
		this.details = details;
	}

	public void setElement(){
		String[] value = new String[3];
		value[Constant.menuDESC] = details;
		value[Constant.menuPRICE] = price;
		value[Constant.menuTITLE] = title;
		elementMap.put(key, value);
		reset();
	}
	
	private void reset(){
		key = "";
		title ="";
		price = "";
		details = "";
	}

	public LinkedHashMap<String, String[]> getData(){
		return elementMap;
	}

}
