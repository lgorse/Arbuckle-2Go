package arbuckle.app;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ExpandableListView;

public class PermanentValues extends SQLiteOpenHelper {


	private HashMap<String, int[]> pHolderMap;
	ArrayList<String>  pOrderHeaders, pComboSubsets;
	Boolean pOrderSent;
	Context context;
	HashMap<String, double[]> pOrderList;

	//orderSent values
	private static final String ORDERSENT_TABLE = "_orderSent_Table";
	private static final String ORDERSENT_ROWID = "_id";
	private static final String ORDERSENT_VALUE = "_orderSent";

	//validOrder values
	private static final String VALIDORDER_TABLE = "_validOrder_Table";
	private static final String VALIDORDER_ROWID = "_id";
	private static final String VALIDORDER_VALUE = "_validOrder";

	//clickHolder values
	private static final String CLICKHOLDER_TABLE = "_clickHolder_Table";
	private static final String HOLDER_ROWID = "_id";
	private static final String HOLDER_KEY= "_HolderKey";
	private static final String GROUP_COL = "_group";
	private static final String CHILD_COL = "_child";
	private static final String VIEW_COL = "_view";
	private static final String QUANT_COL = "_quantity";
	private static final String ORDERCLICK_COL = "_orderclick";

	//OrderHolder values
	private static final String ORDERHOLDER_TABLE = "_orderHolder_Table";
	private static final String ROWID= "_id";
	private static final String ORDERKEY_COL = "_orderKey";
	private static final String ORDERLIST_PRICE = "_orderPrice";
	private static final String ORDERLIST_QUANT = "_quant";
	private static final String ORDERLIST_GPRICE = "_orderGroupPrice";
	private static final String ORDERLIST_SPICY = "_spicy";

	//UserName values
	private static final String USERNAME_TABLE = "_userName_Table";
	private static final String USERNAME_ROWID = "_id";
	private static final String USERNAME_VALUE = "_userName";

	private static final String PERMANENT_DBASE = "Permanent_values";
	private static final int DATABASE_VERSION = 14;

	private SQLiteDatabase pValueSQLite;

	public static final int GROUP = 0;
	public static final int CHILD = 1;
	public static final int VIEW = 2;
	public static final int QUANTITY = 3;
	public static final int ORDERCLICK = 4;

/*
 * 5 databases store permanent values:
 * the orderholder values
 * the clickholder values
 * the ordersent values
 * the validorder values
 * the username
 */
	public PermanentValues(Context context) {
		super(context, PERMANENT_DBASE, null, DATABASE_VERSION);
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + ORDERHOLDER_TABLE + " (" +
				ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
				ORDERKEY_COL + " TEXT, " +
				ORDERLIST_PRICE + " REAL, " +
				ORDERLIST_QUANT + " REAL, " +
				ORDERLIST_GPRICE + " REAL, " +
				ORDERLIST_SPICY + " REAL);");
		db.execSQL("CREATE TABLE " + CLICKHOLDER_TABLE + " (" +
				HOLDER_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
				HOLDER_KEY + " TEXT, "+
				GROUP_COL + " INTEGER, "+
				CHILD_COL + " INTEGER, "+
				VIEW_COL + " INTEGER, "+
				QUANT_COL + " INTEGER, "+
				ORDERCLICK_COL + " TEXT);");
		db.execSQL("CREATE TABLE "  + ORDERSENT_TABLE + " (" +
				ORDERSENT_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
				ORDERSENT_VALUE + " REAL);");
		db.execSQL("CREATE TABLE " + USERNAME_TABLE + " (" + 
				USERNAME_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
				USERNAME_VALUE + " TEXT);");
		db.execSQL("CREATE TABLE "  + VALIDORDER_TABLE + " (" +
				VALIDORDER_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
				VALIDORDER_VALUE + " REAL);");

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + ORDERHOLDER_TABLE);
		db.execSQL("DROP TABLE IF EXISTS " + CLICKHOLDER_TABLE);
		db.execSQL("DROP TABLE IF EXISTS " + ORDERSENT_TABLE);
		db.execSQL("DROP TABLE IF EXISTS " + USERNAME_TABLE);
		db.execSQL("DROP TABLE IF EXISTS " + VALIDORDER_TABLE);		
		onCreate(db);
	}

	public void setDatabase(){
		pValueSQLite = this.getWritableDatabase();
	}

	/*
	 * Wrapper method that sets all values before the app shuts down
	 */
	public void setPermanentValues(HashMap<String, double[]> thisOrderList, HashMap<String, int[]> thisClickStorage, Boolean orderSent, Boolean validOrder){
		setPermanentOrderList(thisOrderList);	
		setPermanentHolderList(thisClickStorage);
		setPermanentOrderSent(orderSent);
		setPermanentValidOrder(validOrder);
	}

/*
 * Stores the validOrder variable in database
 */
	private void setPermanentValidOrder(Boolean validOrder) {
		int validOrderInt = validOrder? 1:0;
		ContentValues cv = new ContentValues();
		cv.put(VALIDORDER_VALUE, validOrderInt);
		pValueSQLite.insert(VALIDORDER_TABLE, null, cv);

	}

	/*
	 * Stores the orderSent variables in database
	 */
	private void setPermanentOrderSent(Boolean orderSent) {
		int orderInt = orderSent? 1:0;
		ContentValues cv = new ContentValues();
		cv.put(ORDERSENT_VALUE, orderInt);
		pValueSQLite.insert(ORDERSENT_TABLE, null, cv);
	}

	/*
	 * Stores the username in database. 
	 */
	public void setPermanentUserName(String userName){
		pValueSQLite.execSQL("DELETE FROM " + USERNAME_TABLE);
		ContentValues cv = new ContentValues();
		cv.put(USERNAME_VALUE, userName);
		pValueSQLite.insert(USERNAME_TABLE, null, cv);

	}

/*
 * Stores the clickstorage variable in database
 */
	private void setPermanentHolderList(HashMap<String, int[]> thisClickStorage) {
		Iterator <Entry<String, int[]>> holderEntries = thisClickStorage.entrySet().iterator();
		while (holderEntries.hasNext()){
			Entry<String, int[]> holderEntry = (Entry <String, int[]>) holderEntries.next();
			int[] value = holderEntry.getValue();
			String key = holderEntry.getKey();
			ContentValues cv = new ContentValues();
			cv.put(HOLDER_KEY, key);
			cv.put(GROUP_COL, value[0]);
			cv.put(CHILD_COL, value[1]);
			cv.put(VIEW_COL, value[2]);
			cv.put(QUANT_COL, value[3]);
			cv.put(ORDERCLICK_COL, value[4]);
			pValueSQLite.insert(CLICKHOLDER_TABLE, null, cv);
		}

	}

	/*
	 * Stores the orderHolder variable in database
	 */
	private void setPermanentOrderList(HashMap <String, double[]> thisOrderList) {
		Iterator<Entry<String, double[]>> orderListEntries = thisOrderList.entrySet().iterator();
		while (orderListEntries.hasNext()){
			Entry<String, double[]> orderEntry = (Entry<String, double[]>) orderListEntries.next();
			double[] value = orderEntry.getValue();
			String key = orderEntry.getKey();
			ContentValues cv = new ContentValues();
			cv.put(ORDERKEY_COL, key);
			cv.put(ORDERLIST_PRICE, value[0]);
			cv.put(ORDERLIST_QUANT, value[1]);
			cv.put(ORDERLIST_GPRICE, value[2]);
			cv.put(ORDERLIST_SPICY, value[3]);
			pValueSQLite.insert(ORDERHOLDER_TABLE, null, cv);
		}		
	}

	/*
	 * Wrapper class gets all values from the database
	 */
	public void getPermanentValues(OrderGetterSetter orderHolder, ClickHolder clickStorage){
		orderHolder.setOrderList(getOrderHolderValue());
		clickStorage.setHolderList(getClickHolderValue());
		int orderSent = getOrderSentValue();
		int isOrderValid = getValidOrderValue();
		if (orderSent == 1){
			orderHolder.setOrderSent();
		}else{
			orderHolder.setOrderCanceled();
		}
		if (isOrderValid==1){
			orderHolder.setOrderValid();
		}else{
			orderHolder.setNoOrderValid();
		}
	}

	private int getOrderSentValue() {
		int isOrderSent = 0;
		String[] columns = new String[]{ORDERSENT_VALUE};
		Cursor c = pValueSQLite.query(ORDERSENT_TABLE, columns, null, null, null, null, null);
		int colValue = c.getColumnIndex(ORDERSENT_VALUE);
		for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()){
			isOrderSent = c.getInt(colValue);
		}
		pValueSQLite.delete(ORDERSENT_TABLE, null, null);
		return isOrderSent;
	}

	private int getValidOrderValue() {
		int isOrderValid = 0;
		String[] columns = new String[]{VALIDORDER_VALUE};
		Cursor c = pValueSQLite.query(VALIDORDER_TABLE, columns, null, null, null, null, null);
		int colValue = c.getColumnIndex(VALIDORDER_VALUE);
		for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()){
			isOrderValid = c.getInt(colValue);
		}
		pValueSQLite.delete(VALIDORDER_TABLE, null, null);
		Log.i("Value of permanentGetValidOrderValue", ""+isOrderValid);
		return isOrderValid;
	}

	public String getPermanentUserName(){

		String userName = "";
		String[] columns = new String[]{USERNAME_VALUE};
		Cursor c = pValueSQLite.query(USERNAME_TABLE, columns, null, null, null, null, null);
		int colValue = c.getColumnIndex(USERNAME_VALUE);
		for (c.moveToFirst();!c.isAfterLast();c.moveToNext()){
			userName = c.getString(colValue);
			Log.i("Here is the userName", userName);
		}
		return userName;
	}

	/*
	 * Resets the user name to null.
	 * Usually precedes the call for a new username when the user logs out
	 */
	public void resetUserName(){	
		pValueSQLite.delete(USERNAME_TABLE, null, null);
	}

	private HashMap<String, int[]> getClickHolderValue() {
		HashMap<String, int[]> newClickHolderList = new HashMap<String, int[]>();
		String[] columns = new String[]{HOLDER_KEY, GROUP_COL, CHILD_COL, VIEW_COL, QUANT_COL, ORDERCLICK_COL};
		Cursor c = pValueSQLite.query(CLICKHOLDER_TABLE, columns, null, null, null, null, null);
		int colKey = c.getColumnIndex(HOLDER_KEY);
		int colGroup = c.getColumnIndex(GROUP_COL);
		int colChild = c.getColumnIndex(CHILD_COL);
		int colView = c.getColumnIndex(VIEW_COL);
		int colQuant = c.getColumnIndex(QUANT_COL);
		int colOrderClick = c.getColumnIndex(ORDERCLICK_COL);
		for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()){
			int[] valueArray = new int[6];
			int group = c.getInt(colGroup);
			int child = c.getInt(colChild);
			int view = c.getInt(colView);
			int quant = c.getInt(colQuant);
			int orderClick = c.getInt(colOrderClick);
			String key = c.getString(colKey);
			valueArray[0] = group;
			valueArray[1] = child;
			valueArray[2] = view;
			valueArray[3] = quant;
			valueArray[4] = orderClick;
			newClickHolderList.put(key, valueArray);
		}
		pValueSQLite.delete(CLICKHOLDER_TABLE, null, null);
		return newClickHolderList;
	}

	private HashMap<String, double[]> getOrderHolderValue() {
		HashMap<String, double[]> newOrderList = new HashMap<String, double[]>();
		String[] columns = new String[]{ORDERKEY_COL, ORDERLIST_PRICE, ORDERLIST_QUANT, ORDERLIST_GPRICE, ORDERLIST_SPICY};
		Cursor c = pValueSQLite.query(ORDERHOLDER_TABLE, columns, null, null, null, null, null);
		int colKey = c.getColumnIndex(ORDERKEY_COL);
		int colPrice = c.getColumnIndex(ORDERLIST_PRICE);
		int colQuant = c.getColumnIndex(ORDERLIST_QUANT);
		int colGprice = c.getColumnIndex(ORDERLIST_GPRICE);
		int colSpicy = c.getColumnIndex(ORDERLIST_SPICY);
		for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()){
			double[] valueArray = new double[4];
			double price = c.getDouble(colPrice);
			double quant = c.getDouble(colQuant);
			double gPrice = c.getDouble(colGprice);
			double spicy = c.getDouble(colSpicy);
			valueArray[0] = price;
			valueArray[1] = quant;
			valueArray[2] = gPrice;
			valueArray[3] = spicy;
			String key = c.getString(colKey);
			newOrderList.put(key, valueArray);
		}
		pValueSQLite.delete(ORDERHOLDER_TABLE, null, null);
		return newOrderList;
	}

	public void closePermanentValues(){
		pValueSQLite.close();
	}

	/*
	 * Resets all tables so that they are clear when a user logs out 
	 */
	public void resetTables() {
		pValueSQLite = this.getWritableDatabase();
		pValueSQLite.execSQL("DELETE FROM " + ORDERHOLDER_TABLE);
		pValueSQLite.execSQL("DELETE FROM " + CLICKHOLDER_TABLE);
		pValueSQLite.execSQL("DELETE FROM " + ORDERSENT_TABLE);
	}

	/*
	 * On open, gets the values of the permanent variables from the server
	 * This is called when a user logs in again: the order is has been cleared from
	 * the database when he logged out, only his effective order is stored in the remote database
	 */
	public void getServerValues(Context context, String userName, OrderGetterSetter orderHolder, int timeValue) {
		GetServerValues getServerValues = new GetServerValues(context, userName, orderHolder, timeValue);		
		try {
			HashMap<String, double[]> orderMap = getServerValues.execute().get();
			if ((orderMap!= null)&&(!orderMap.isEmpty())){
				orderHolder.setOrderSent();
				orderHolder.setOrderList(orderMap);	
				if (orderHolder.getOrderList().isEmpty()){
					orderHolder.setNoOrderValid();
				}else{
					orderHolder.setOrderValid();
				}
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private class GetServerValues extends AsyncTask<Void, Void, HashMap<String, double[]>>{

		Context context;
		String userName;
		int timeValue;
		private OrderGetterSetter orderHolder;
		private ProgressDialog progress;

		private GetServerValues(Context context, String userName, OrderGetterSetter orderHolder, int timeValue){
			this.context = context;
			this.userName = userName;
			this.timeValue = timeValue;
			this.orderHolder = orderHolder;
		}

		@Override
		protected HashMap<String, double[]> doInBackground(Void... params) {
			HashMap<String, double[]> orderList = new HashMap<String, double[]>();
			JSONObject checkUserName = new JSONObject();
			try {
				checkUserName.put("userName", userName);			
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			HttpParams httpParamscheckServer = new BasicHttpParams();
			int timeoutConnection = 3000;
			int timeoutSocket = 5000;
			HttpConnectionParams.setConnectionTimeout(httpParamscheckServer, timeoutConnection);
			HttpConnectionParams.setConnectionTimeout(httpParamscheckServer, timeoutSocket);
			HttpClient client = new DefaultHttpClient(httpParamscheckServer);
			String url = "http://www.stanford.edu/group/arbucklecafe/cgi-bin/ArbuckleCafeCheckOrderInServer.php";

			List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(2);
			nameValuePair.add(new BasicNameValuePair("userName", checkUserName.toString()));
			HttpPost request = new HttpPost(url);
			try {
				request.setEntity(new UrlEncodedFormEntity(nameValuePair));
			} catch (UnsupportedEncodingException e) {

				e.printStackTrace();
			}
			try {
				HttpResponse response = client.execute(request);
				HttpEntity entity = response.getEntity();
				String serverOrderList = EntityUtils.toString(entity);

				try {
					if ((serverOrderList!=null)&&(!serverOrderList.isEmpty())){
						if (serverOrderList.equals("{\"-1\":null}")){	
							orderList.put("-1", null);
							//getServerValues(context, userName, orderHolder, timeValue);
						}else{
							JSONObject jsonObject = new JSONObject(serverOrderList.toString());
							Iterator keys = jsonObject.keys();
							while(keys.hasNext()){
								String key = (String) keys.next();
								JSONArray JSONValues = jsonObject.getJSONArray(key);
								double[] values =  new double[4];
								//values = (double[]) jsonObject.get(key);
								values[0] = JSONValues.getDouble(0);
								values[1] = JSONValues.getDouble(1);
								values[2] = JSONValues.getDouble(2);
								values[3] = JSONValues.getDouble(3);
								orderList.put(key, values);
							}
						}
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} catch (ClientProtocolException e) {		

				e.printStackTrace();
			} catch (IOException e) {

				e.printStackTrace();
			}
			return orderList;
		}
	}

}



