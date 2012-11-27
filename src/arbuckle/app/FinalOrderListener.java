package arbuckle.app;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

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
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SlidingDrawer;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

/*
 * This implements the behavior of the app when a final order has been placed.
 * The user action can be cancel all, cancel only one menu item orr confirm.
 */
public class FinalOrderListener implements OnClickListener {
	ClickHolder clickStorage;
	OrderGetterSetter orderHolder;
	HashMap<String, double[]> orderList;
	ExpandableListView thisExpandableListView ;
	ExpandMenuAdapter thisExpandMenuAdapter;
	OrderAdapter orderAdapter;
	ListView orderReview;
	ArrayList <String> orderHeaders, comboSubsets;
	int position;
	SlidingDrawer thisSlidingDrawer;
	TextView orderCompleteHandle;
	String userName = ArbuckleAppActivity.getUserName();
	Context context;
	InternetConnectionChecks checkInternet;
	
	public FinalOrderListener (Context context, ClickHolder clickStorage, OrderGetterSetter orderHolder, ExpandableListView thisExpandableListView, 
			OrderAdapter orderAdapter, int position, ExpandMenuAdapter thisExpandMenuAdapter){
		this.clickStorage = clickStorage;
		this.orderList = orderHolder.getOrderList();
		this.orderHolder = orderHolder;
		this.thisExpandableListView = thisExpandableListView;
		this.thisExpandMenuAdapter = thisExpandMenuAdapter;
		this.orderAdapter = orderAdapter;
		this.orderReview = orderAdapter.orderReview;
		thisSlidingDrawer = (SlidingDrawer) orderAdapter.orderReview.getParent().getParent();
		this.orderCompleteHandle = (TextView) thisSlidingDrawer.getHandle();
		this.orderHeaders = orderAdapter.orderHeaders;
		this.comboSubsets = orderAdapter.comboSubsets;
		this.position = position;
		this.context = context;
		checkInternet = new InternetConnectionChecks(this.context);
	}
	@Override
	public void onClick(View v) {

		switch (v.getId()){
		case R.id.bCancelAll:
			cancelAll();
			break;
		case R.id.bCancel:
			cancelSingle();
			break;
		case R.id.bConfirm:
			confirmOrder(v);
		}
	}
/*
 * Order confirmation sends the order to the database.
 * After this, the user can only cancle the entire order and that only before the lockoff period on sushi day.
 */
	private void confirmOrder(View v) {	
		SendOrder sendOrder = new SendOrder(context);
		sendOrder.execute();
		thisSlidingDrawer.lock();
		orderHolder.setOrderSent();
		refreshView();
		thisExpandableListView.invalidateViews();

	}

/*
 * Passes the order as a JSON to the database
 */
	private JSONObject createOrderJSON() {
		JSONObject thisOrderJSON = new JSONObject();
		JSONArray headerJSON = new JSONArray();
		JSONArray comboSubsetJSON = new JSONArray();
		JSONObject orderListJSON = new JSONObject();

		try {
			Iterator<Entry<String, double[]>> elementEntries = orderList.entrySet().iterator();
			while (elementEntries.hasNext()){
				Entry<String, double[]> thisEntry = (Entry<String, double[]>) elementEntries.next();
				JSONArray orderListValueArray = new JSONArray();
				int entryLength = thisEntry.getValue().length;
				for (int u = 0; u< entryLength; u++){
					orderListValueArray.put(thisEntry.getValue()[u]);
				}
				orderListJSON.put(thisEntry.getKey(), orderListValueArray);
			}
		}
		catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (int i = 0; i < orderHeaders.size(); i++){
			headerJSON.put(orderHeaders.get(i));
		}

		for (int j = 0; j < comboSubsets.size(); j++){
			comboSubsetJSON.put(comboSubsets.get(j));
		}	


		try {
			thisOrderJSON.put("userName", userName);
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			thisOrderJSON.put("orderHeaders", headerJSON);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			thisOrderJSON.put("comboSubsets", comboSubsetJSON);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			thisOrderJSON.put("orderList", orderListJSON);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return thisOrderJSON;
	}

	/*
	 * While the order has not been confirmed yet,
	 * cancels single menu items
	 */
	private void cancelSingle() {
		OrderAdapter thisAdapter = (OrderAdapter) orderReview.getAdapter(); 
		String key = thisAdapter.getItem(position);
		String[] tokens = key.split(Constant.delims);
		removeFromClickStorage(tokens, key);
		orderHolder.removeOrder(key);
		orderAdapter.removeHeader(key);
		orderAdapter.removeSubset(key);
		if (orderList.isEmpty()) thisSlidingDrawer.close();
		refreshView();
	}

/*
 * Ensures that, if a single order has been canceled, the user will
 * not see that order in clickstorage
 */
	private void removeFromClickStorage(String[] tokens, String key) {
		String type = tokens[TYPE];
		String group = tokens[GROUP];
		String item = tokens[ITEM];		
		if (!type.equals(Constant.chef_Special)){
			if (item.equals(Constant.combo_signaler)){
				String groupHeader = type+Constant.delims+group+Constant.delims;
				for (String subKey:comboSubsets){
					if (subKey.contains(groupHeader)){
						clickStorage.getHolderList().remove(subKey);
						orderHolder.removeOrder(subKey);
					}
				}
			}
			clickStorage.getHolderList().remove(key);
		}else{
			ArrayList<String> toRemove = new ArrayList<String>();
			Iterator<Entry<String, int[]>> clickEntries = clickStorage.getHolderList().entrySet().iterator();
			while (clickEntries.hasNext()){
				Entry<String, int[]> thisEntry = (Entry<String, int[]>) clickEntries.next();
				String entryKey = thisEntry.getKey();
				if (entryKey.contains(type)) toRemove.add(entryKey);
			}
			for (String entrykeys:toRemove){
				clickStorage.getHolderList().remove(entrykeys);
				orderHolder.removeOrder(entrykeys);
				orderAdapter.removeSubset(entrykeys);
			}
		}
	}

	/*
	 * Cancel all menu items ordered.
	 * This is the only option once an order has been confirmed,
	 * until the lockoff period on sushi day
	 */
	private void cancelAll() {
		TimeStamp thisTimeStamp = new TimeStamp();
		int timeValid;
		try {
			thisTimeStamp.setTimeStamp();
			timeValid = thisTimeStamp.getTimeStamp();
			if ((timeValid != Constant.periodLockDown) &&(orderHolder.isOrderSent())){
				Boolean isConnected = checkInternet.isNetworkAvailable();
				if(isConnected){
					SendCancel sendCancel = new SendCancel(context);
					sendCancel.execute();
					implementCancel();
				}else{
					Toast.makeText(context, Constant.cancelFailure, Toast.LENGTH_SHORT).show();
				}
			}else{
				implementCancel();
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * This cancellation implementation takes care of all the graphics
	 * and updates the holder variables (orderholder, clickstorage, etc.)
	 * once cancel has been clicked.
	 */
	private void implementCancel() {
		thisSlidingDrawer.unlock();
		thisSlidingDrawer.close();
		orderHolder.setOrderCanceled();
		clickStorage.getHolderList().clear();
		orderList.clear();
		orderHolder.setNoOrderValid();
		orderHolder.setOrderCanceled();
		orderReview.setVisibility(View.GONE);
		refreshView();
		thisExpandableListView.invalidateViews();
		Toast.makeText(context, "Order canceled", Toast.LENGTH_SHORT).show();
	}
	
	/*
	 * Sets up a parallel threat to update the views inside the
	 * slidingdrawer.
	 */
	private void refreshView(){
		Handler refresh = new Handler();
		refresh.post(new Runnable(){

			@Override
			public void run() {
				orderCompleteHandle.invalidate();
				orderReview.invalidateViews();
			}
		});	
	}

	/*
	 * SendCancel sends the cancellation information to the database, to clear it of the user's order
	 */
	private class SendCancel extends AsyncTask<Void, Void, Void>{

		Context context;
		private ProgressDialog progress;

		private SendCancel(Context context){
			this.context = context;
		}

		@Override
		protected void onPreExecute(){
			super.onPreExecute();
			progress = ProgressDialog.show(context, "Cancelling Order", "Cancelling", true);
		}

		@Override
		protected Void doInBackground(Void... params) {
			JSONObject cancelJSON = new JSONObject();
			try {
				cancelJSON.put("userName", userName);			
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			HttpParams httpParamsCancel = new BasicHttpParams();
			int timeoutConnection = 3000;
			int timeoutSocket = 5000;
			HttpConnectionParams.setConnectionTimeout(httpParamsCancel, timeoutConnection);
			HttpConnectionParams.setConnectionTimeout(httpParamsCancel, timeoutSocket);
			HttpClient client = new DefaultHttpClient(httpParamsCancel);
			String url = "http://www.stanford.edu/group/arbucklecafe/cgi-bin/ArbuckleCafeOrderCancel.php";

			List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(2);
			nameValuePair.add(new BasicNameValuePair("cancel", cancelJSON.toString()));
			HttpPost request = new HttpPost(url);
			try {
				request.setEntity(new UrlEncodedFormEntity(nameValuePair));
			} catch (UnsupportedEncodingException e) {

				e.printStackTrace();
			}
			try {
				HttpResponse response = client.execute(request);
				HttpEntity entity = response.getEntity();
				String cancelValue = EntityUtils.toString(entity);
			} catch (ClientProtocolException e) {		

				e.printStackTrace();
			} catch (IOException e) {

				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void nothing) {
			progress.dismiss();
		}

	}

/*
 * Sends the order to the database. Done via asynctask.
 */
	private class SendOrder extends AsyncTask<Void, Void, Void>{

		Context context;
		private ProgressDialog progress;
		private int sendTimeValid;
		private String sendStartTime;
		private String sendEndtime;
		private String sendCutOffTime;
		private String nextDay;

		private SendOrder(Context context){
			this.context = context;
			TimeStamp sendTimeStamp = new TimeStamp();
			try {
				sendTimeStamp.setTimeStamp();
				this.sendTimeValid = sendTimeStamp.getTimeStamp();
				this.sendStartTime = sendTimeStamp.getStartTime();
				this.sendEndtime = sendTimeStamp.getEndTime();
				this.sendCutOffTime = sendTimeStamp.getCutOffTime();
				this.nextDay = sendTimeStamp.getNextDay();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {

				e.printStackTrace();
			}
		}

		@Override
		protected void onPreExecute(){
			super.onPreExecute();
			progress = ProgressDialog.show(context, "Sending Order", "Sending", true);
		}

		@Override
		protected Void doInBackground(Void... params) {
			Boolean isConnected = checkInternet.isNetworkAvailable();
			if (!isConnected){
				Toast.makeText(context, Constant.noInternet, Toast.LENGTH_SHORT).show();
			}else{
				JSONObject orderJSON = createOrderJSON();
				HttpParams httpParamsSend = new BasicHttpParams();
				int timeoutConnection = 3000;
				int timeoutSocket = 5000;
				HttpConnectionParams.setConnectionTimeout(httpParamsSend, timeoutConnection);
				HttpConnectionParams.setConnectionTimeout(httpParamsSend, timeoutSocket);
				HttpClient client = new DefaultHttpClient(httpParamsSend);
				String url = "http://www.stanford.edu/group/arbucklecafe/cgi-bin/ArbuckleCafeOrderSubmit.php";

				List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>(2);
				nameValuePair.add(new BasicNameValuePair("Order", orderJSON.toString()));
				HttpPost request = new HttpPost(url);
				try {
					request.setEntity(new UrlEncodedFormEntity(nameValuePair));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
					Toast.makeText(context, Constant.generalError, Toast.LENGTH_SHORT).show();
					return null;
				}

				try {
					HttpResponse response = client.execute(request);
					HttpEntity entity = response.getEntity();
					String orderValue = EntityUtils.toString(entity);
					Log.i("Hello, world", orderValue);
				} catch (ClientProtocolException e) {		


					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
					Toast.makeText(context, Constant.noInternet, Toast.LENGTH_SHORT).show();
					return null;
				}
			}
			return null;					
		}

		@Override
		protected void onPostExecute(Void nothing) {
			progress.dismiss();
			if(sendTimeValid == Constant.periodOrderToday){
				Toast.makeText(context, "Order sent. Pick up today from "+sendStartTime, Toast.LENGTH_SHORT).show();
			}else if(sendTimeValid == Constant.periodOrderNextDay){
				Toast.makeText(context, "Order sent. Pick up next "+nextDay+ " at " +sendStartTime, Toast.LENGTH_SHORT).show();
			}
		}
	}

	private static final int TYPE = 0;
	private static final int GROUP = 1;
	private static final int ITEM = 2;


}
