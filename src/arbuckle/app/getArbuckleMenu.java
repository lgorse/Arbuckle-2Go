package arbuckle.app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;


public class getArbuckleMenu {

	private ArrayList<String> typeArray;
	private ArrayList<String[]> groupArray;
	private ArrayList<String[]> itemArray;

	/*
	 * Sets the list of types, groups and items based on MySQL database
	 */
	public void setSQLMenu(){
		getMenuFromSQL SQLMenu = new getMenuFromSQL();
		try {
			JSONArray passedJSON = SQLMenu.execute().get();
			typeArray = setTypeArray(passedJSON);
			groupArray = setGroupArray(passedJSON);
			itemArray = setItemArray(passedJSON);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


	/*
	 * Sets item List
	 */
	private ArrayList<String[]> setItemArray(JSONArray passedJSON) {
		JSONArray itemJSONArray = new JSONArray();
		try {
			itemJSONArray = passedJSON.getJSONArray(2);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ArrayList<String[]> itemArray = new ArrayList<String[]>();
		for (int i = 0; i<itemJSONArray.length(); i++){
			try {
				JSONArray thisItem = itemJSONArray.getJSONArray(i);
				String[] value = new String[7];
				for (int j = 0; j<thisItem.length(); j++){
					value[j] = "";
					if (!thisItem.getString(j).equals("null")) value[j] = thisItem.getString(j);
				}
				itemArray.add(value);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return itemArray;
	}


	/*
	 * Sets group List
	 */
	private ArrayList<String[]> setGroupArray(JSONArray passedJSON) {
		JSONArray groupJSONArray = new JSONArray();
		try {
			groupJSONArray = passedJSON.getJSONArray(1);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ArrayList<String[]> groupArray = new ArrayList<String[]>();
		for (int i = 0; i<groupJSONArray.length(); i++){
			try {
				JSONArray thisGroup = groupJSONArray.getJSONArray(i);
				String[] value = new String[7];
				if (!thisGroup.getString(Constant.GROUPNAME).equals("Chef's Special")){
					for (int j = 0; j<thisGroup.length(); j++){
						value[j] = "";
						if (!thisGroup.getString(j).equals("null"))value[j] = thisGroup.getString(j);
					}
					groupArray.add(value);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return groupArray;
	}

	/*
	 * Sets type list
	 */
	private ArrayList<String> setTypeArray(JSONArray passedJSON) {
		JSONArray typeJSONArray = new JSONArray();
		try {
			typeJSONArray = passedJSON.getJSONArray(0);
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		ArrayList<String> typeArray = new ArrayList<String>();
		for (int i = 0; i<typeJSONArray.length(); i++){
			try {
				typeArray.add(typeJSONArray.getString(i));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}								
		}
		return typeArray;
	}


	public ArrayList<String> getTypeList(){
		return typeArray;
	}

	public ArrayList<String[]> getGroupList(){
		return groupArray;
	}

	public ArrayList<String[]> getItemList(){
		return itemArray;
	}

	/*
	 * Gets the menu from MySQL
	 */
	private class getMenuFromSQL extends AsyncTask<Void, Void, JSONArray>{
		private ArrayList<String> typeArray;
		private LinkedHashMap<String, String[]> menuElements = new LinkedHashMap<String, String[]>();

		@Override
		protected JSONArray doInBackground(Void... params) {
			JSONArray menuData = new JSONArray();
			HttpParams httpParamscheckServer = new BasicHttpParams();
			HttpClient client = new DefaultHttpClient(httpParamscheckServer);
			String url = "http://www.stanford.edu/group/arbucklecafe/cgi-bin/getArbuckleMenu.php";
			HttpPost request = new HttpPost(url);
			try {
				HttpResponse response = client.execute(request);
				HttpEntity entity = response.getEntity();
				String serverOrderList = EntityUtils.toString(entity);
				menuData = new JSONArray(serverOrderList.toString());
				//typeArray = setTypeArray(typeJSONArray);
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// TODO Auto-generated method stub
			return menuData;
		}
	}
}

