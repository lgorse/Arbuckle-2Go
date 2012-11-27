package arbuckle.app;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

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
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class DialogSet {

	//Context context;
	//String firstName;
	private ArbMenuXMLGetSet menuData;

	public DialogSet(Context context){
		//this.context = context;
		//this.firstName = firstName;
		parseArbuckleMenu(context);
	}
	
	
	public Dialog setQuitDialog(Context context, String firstName) {
		Dialog quitDialog = new Dialog(context, R.style.dialogStyle);
		quitDialog.setContentView(R.layout.exit_dialog);
		quitDialog.setTitle(Constant.logout);
		Button no = (Button) quitDialog.findViewById(R.id.bNo);
		Button yes = (Button) quitDialog.findViewById(R.id.bYes);
		yes.setOnClickListener(new exitListener(quitDialog, context, firstName));
		no.setOnClickListener(new exitListener(quitDialog, context, firstName));
		return quitDialog;
	}

	
	/*
	 * Handles the info dialog on the app from howtoarray in values folder
	 */
	public Dialog setInfoDialog(Context context, TimeStamp timeStamp) {
		Dialog infoDialog = new Dialog(context, R.style.dialogStyle);
		infoDialog.setContentView(R.layout.howitworks_dialog);
		infoDialog.setCanceledOnTouchOutside(true);
		ListView infoListView = (ListView) infoDialog
		.findViewById(R.id.lvHowItWorks);
		infoDialog.setTitle("Here's how it works:");
		ArrayList<String> thisArray = setInfoArray(timeStamp);
		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(context,
				R.layout.howitworks_row, R.id.infoTextView, thisArray);
		infoListView.setAdapter(arrayAdapter);
		return infoDialog;
	}
	
	/*
	 * Status dialog gives the user the latest information about his activity
	 * and his order
	 */
	public Dialog setStatusDialog(Context context, ExpandMenuAdapter listAdapter, TimeStamp thisTimeStamp) {
		Dialog statusDialog = new Dialog(context, R.style.dialogStyle);
		statusDialog.setContentView(R.layout.info_dialog);
		statusDialog.setCanceledOnTouchOutside(true);
		TextView user_info1 = (TextView) statusDialog
		.findViewById(R.id.tvUser_info1);
		TextView user_info2 = (TextView) statusDialog
		.findViewById(R.id.tvUser_info2);

		if (listAdapter.getOrderSent()) {
			user_info1.setText("You sent an order");
		} else {
			user_info1.setText("You haven't sent an order yet");
		}
		setStatusText(statusDialog, thisTimeStamp, listAdapter, user_info2);		
		return statusDialog;
	}
	
	/*
	 * Sets the dialog for Arbuckle's entire menu, based on feed provided by Arbuckle web manager
	 */
	public Dialog setMenuDialog(Context context) {
		String dialogTitle = "Arbuckle menu";
		ArrayList<String> dayMenuList = dayMenuArrayList(menuData);
		Dialog menuDialog = new Dialog(context, R.style.dialogStyle);
		menuDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		menuDialog.setCanceledOnTouchOutside(true);
		menuDialog.setContentView(R.layout.day_menu_dialog);
		TextView tvDate = (TextView) menuDialog.findViewById(R.id.menuDate);
		// TextView tvTitle = (TextView)
		// menuDialog.findViewById(R.id.menuTitle);
		tvDate.setText(setDate(menuData.getDate()));
		LinearLayout dayLayout = (LinearLayout) menuDialog
		.findViewById(R.id.llDayMenuDialog);
		ListView dayList = (ListView) dayLayout.findViewById(R.id.dayMenuList);
		DayMenuAdapter dayMenuAdapter = new DayMenuAdapter(context,
				menuData.getData(), dayMenuList);
		dayList.setAdapter(dayMenuAdapter);
		return menuDialog;
	}

	/*
	 * Sets the array that describes how the user should use Arbuckle 2Go
	 */
	private ArrayList<String> setInfoArray(TimeStamp timeStamp) {
		ArrayList<String> infoArray = new ArrayList<String>();
		InfoMenuGet getInfoMenu = new InfoMenuGet();
		try {
			infoArray = getInfoMenu.execute().get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return infoArray;
	}

	private class InfoMenuGet extends AsyncTask<Void, Void, ArrayList<String>>{
		
		@Override
		protected ArrayList<String> doInBackground(Void... params) {
			ArrayList<String> infoMenuArray = new ArrayList<String>();
			JSONArray infoMenu = new JSONArray();
			HttpParams httpParamscheckServer = new BasicHttpParams();
			HttpClient client = new DefaultHttpClient(httpParamscheckServer);
			String url = "http://www.stanford.edu/group/arbucklecafe/cgi-bin/ArbuckleInstructions.php";
			HttpPost request = new HttpPost(url);
			try {
				HttpResponse response = client.execute(request);
				HttpEntity entity = response.getEntity();
				String serverOrderList = EntityUtils.toString(entity);
				infoMenu = new JSONArray(serverOrderList.toString());
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
			for (int i = 0; i<infoMenu.length(); i++){
				try {
					infoMenuArray.add(infoMenu.getString(i));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return infoMenuArray;
		}
	}
	
/*
 * Sets the status text for the status dialog, so the user is aware of the time and status of his order
 */
	private void setStatusText(Dialog statusDialog, TimeStamp thisTimeStamp, ExpandMenuAdapter listAdapter, TextView user_info2) {
		switch (thisTimeStamp.getTimeStamp()) {
		case Constant.periodOrderNextDay:
			statusDialog.setTitle("Arbuckle is now closed");
			user_info2.setText("You can place an order today and pick up on "
					+ thisTimeStamp.getNextDay());
			break;
		case Constant.periodOrderToday:
			statusDialog.setTitle("Order until " + thisTimeStamp.getCutOffTime());
			user_info2.setText("Pick up today between " + thisTimeStamp.getStartTime() + " and "
					+ thisTimeStamp.getEndTime() + " at Arbuckle Cafe");
			break;
		case Constant.periodLockDown:
			if (listAdapter.getOrderSent()) {
				statusDialog.setTitle("No more orders today!");
				user_info2.setText("Pick up your order between " + thisTimeStamp.getStartTime()
						+ " and " + thisTimeStamp.getEndTime() + " at Arbuckle Cafe");
			} else {
				statusDialog.setTitle("No more orders today!");
				user_info2.setText("You can place an order for next "
						+ thisTimeStamp.getNextDay() + " starting at "
						+ thisTimeStamp.getEndTime() + " today.");
			}
			break;
		default:
			break;
		}
		
	}

		/*
		 * Creates the arrayList of the menu, which then populates the menu adapter
		 */
	private ArrayList<String> dayMenuArrayList(ArbMenuXMLGetSet menuData) {
		ArrayList<String> dayArrayList = new ArrayList<String>();
		LinkedHashMap<String, String[]> elementList = menuData.getData();
		Iterator<Entry<String, String[]>> elementEntries = elementList
		.entrySet().iterator();
		while (elementEntries.hasNext()) {
			Entry<String, String[]> thisEntry = (Entry<String, String[]>) elementEntries
			.next();
			String key = thisEntry.getKey();
			dayArrayList.add(key);
		}
		return dayArrayList;
	}
	
	/*
	 * Sets the date for the title of the Arbuckle menu dialog
	 */
	String setDate(String XMLDate) {
		String title = "";
		Calendar today = Calendar.getInstance();
		SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
		try {
			Date menuDate = format.parse(XMLDate);
			Calendar menuCal = Calendar.getInstance();
			menuCal.setTime(menuDate);
			if (menuCal.get(Calendar.DAY_OF_YEAR) == today
					.get(Calendar.DAY_OF_YEAR)) {
				title += "Today " + XMLDate.substring(0, 4);
			} else if (menuCal.get(Calendar.DAY_OF_YEAR) > today
					.get(Calendar.DAY_OF_YEAR)) {
				title += "Tomorrow " + XMLDate.substring(0, 4);
			} else {
				title += "On " + XMLDate;
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return title;
	}
	
	private void parseArbuckleMenu(Context context) {
		// TODO Auto-generated method stub
		ArbMenuXMLHandler myMenuHandler = new ArbMenuXMLHandler();
		ArbParserTask arbParseTask = new ArbParserTask(context,
				myMenuHandler);
		try {
			menuData = arbParseTask.execute().get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * Parses the menu list from the XML feed of Cafe bon Appetit
	 */
	private class ArbParserTask extends AsyncTask<Void, Void, ArbMenuXMLGetSet> {

		private Context context;
		private ArbMenuXMLHandler myMenuHandler;
		private URL url;

		private ArbParserTask(Context context, ArbMenuXMLHandler myMenuHandler) {
			super();
			this.context = context;
			this.myMenuHandler = myMenuHandler;
		}

		@Override
		protected ArbMenuXMLGetSet doInBackground(Void... params) {

			try {
				url = new URL("http://www.cafebonappetit.com/feeds/daily/269");
			} catch (MalformedURLException e) {
				e.printStackTrace();
				Log.i("Error", "With the URL");
			}

			try {
				SAXParserFactory saxPF = SAXParserFactory.newInstance();
				SAXParser saxP = saxPF.newSAXParser();
				XMLReader xmlR = saxP.getXMLReader();

				xmlR.setContentHandler(myMenuHandler);
				xmlR.parse(new InputSource(url.openStream()));

			} catch (Exception e) {
				Toast.makeText(context, Constant.generalError,
						Toast.LENGTH_SHORT).show();
				System.out.println(e);
				((Activity) context).finish();
			}
			return ArbMenuXMLHandler.getXMLData();

		}
	}
	
	/*
	 * Handles the click on the "are you sure" dialog that pops up when the user
	 * quits
	 */
	private class exitListener implements View.OnClickListener {

		Dialog dialog;
		Context context;
		String firstName;

		private exitListener(Dialog dialog, Context context, String firstName) {
			this.dialog = dialog;
			this.context = context;
			this.firstName = firstName;
		}

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.bNo:
				dialog.cancel();
				break;
			case R.id.bYes:
				Toast.makeText(context,
						"Come back soon " + firstName + "!", Toast.LENGTH_SHORT)
						.show();
				CookieSyncManager.createInstance(context);
				CookieManager cookieManager = CookieManager.getInstance();
				cookieManager.removeAllCookie();

				((Activity) context).finish();
				((Activity) context).moveTaskToBack(true);
				break;
			}

		}
	}
}
