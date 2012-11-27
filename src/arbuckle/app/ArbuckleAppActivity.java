package arbuckle.app;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.ArrayList;
import java.util.Calendar;

import java.util.HashMap;

import java.util.concurrent.ExecutionException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import android.os.AsyncTask;
import android.os.Bundle;

import android.util.Log;

import android.view.LayoutInflater;

import android.view.View;


import android.widget.ExpandableListView;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;

import android.widget.FrameLayout;

import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;

import android.widget.TabHost.TabSpec;


public class ArbuckleAppActivity extends SherlockActivity {
	/** Called when the activity is first created. */

	// Menu Parsing global variables
	getArbuckleMenu ArbuckleSQLMenu = new getArbuckleMenu();
	private static String userName, firstName;

	// Menu Adapter global Variables
	private ExpandMenuAdapter listAdapter;
	private ExpandableListView thisExpandableListView;

	ArrayList<String[]> itemChildList;
	private TabHost tabHost;
	private FrameLayout mainLayout;
	private OrderGetterSetter orderHolder = new OrderGetterSetter();
	private ClickHolder clickStorage = new ClickHolder();

	
	// Stored value database
	private PermanentValues pValues = new PermanentValues(this);

	// Time global variables
	private TimeStamp thisTimeStamp;
	private int timeValid;


	// Dialog variables
	Dialog quitDialog, statusDialog, infoDialog, menuDialog;
	private DialogSet dialogs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			userName = extras.getString("username");
			firstName = extras.getString("firstName");
		}
		setTimeValues();
		setPermanentValuesOnStart();
		notificationAlarm();
		setContentView(R.layout.main);
		parseSushiMenu();
		initialize();
		TabSetup();
		initializeDialog();
	}


	/*
	 * Class the alarm manager class that will send user notifications
	 */
	private void notificationAlarm() {
		notifyDay(Constant.day1, 9, 00);
		notifyDay(Constant.day2, 9, 00);	
	}

	private void notifyDay(int day, int hour, int minute){
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_WEEK, day);
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, minute);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		long interval = 604800000;

		Intent alarmIntent = new Intent(this, alarmNotif.class);
		alarmIntent.putExtra("name", firstName);
		PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager notifAlarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		notifAlarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), interval, alarmPendingIntent);

	}

	/*
	 * Gets the following permanent values when app starts: orderHolder(what was
	 * the previous order, is there avalid order, is the order sent);
	 * clickStorage(what was previously clicked)
	 */
	private void setPermanentValuesOnStart() {
		pValues.getServerValues(this, userName, orderHolder, timeValid);
		if ((!orderHolder.getOrderList().isEmpty()) && (orderHolder != null)) {
			pValues.setDatabase();
			pValues.resetTables();
			pValues.resetUserName();
			pValues.closePermanentValues();
			if (orderHolder.getOrderList().containsKey("-1")) {
				orderHolder.getOrderList().clear();
				orderHolder.setNoOrderValid();
				orderHolder.setOrderCanceled();
			}
		} else {
			pValues.setDatabase();
			String oldUserName = pValues.getPermanentUserName();
			if ((userName.equals(oldUserName))) {
				pValues.getPermanentValues(orderHolder, clickStorage);
			} else {
				pValues.setDatabase();
				pValues.resetTables();
				pValues.resetUserName();
			}
			pValues.closePermanentValues();
		}
	}

	/*
	 * Builds menu from SQL database
	 */
	private void parseSushiMenu() {
		ArbuckleSQLMenu.setSQLMenu();
	}

	/*
	 * Sets up the tabs over the adapter. Also calls the menu adapter that lists
	 * the product.
	 */
	private void TabSetup() {
		for (final String type : ArbuckleSQLMenu.getTypeList()) {
			View tabView = createTabView(tabHost.getContext(), type);
			TabSpec ourSpec = tabHost.newTabSpec(type).setIndicator(tabView)
			.setContent(new TabHost.TabContentFactory() {

				@Override
				public View createTabContent(String tag) {
					ExpandOptionsGroup thisTypeGroup = new ExpandOptionsGroup();
					ArrayList<ExpandOptionsGroup> optionsGroup = thisTypeGroup.setupGroupList(ArbuckleSQLMenu.getGroupList(), ArbuckleSQLMenu.getItemList(), type);
					display(optionsGroup);
					return thisExpandableListView;
				}
			});
			tabHost.addTab(ourSpec);
			tabHost.setCurrentTabByTag(type);
		}
		tabHost.setCurrentTab(0);
		tabHost.setOnTabChangedListener(new OnTabChangeListener() {

			@Override
			public void onTabChanged(String tabId) {
				ExpandableListView thisView = (ExpandableListView) tabHost
				.getCurrentView();
				thisView.invalidateViews();
			}
		});

	}

	/*
	 * Populates each tab view
	 */
	private static View createTabView(final Context context,
			final String passedType) {
		View view = LayoutInflater.from(context).inflate(R.layout.tabs, null);
		final TextView tv = (TextView) view.findViewById(R.id.tabsText);
		tv.setText(passedType);
		return view;
	}

	/*
	 * inflates the menu adapter
	 */
	private void display(ArrayList<ExpandOptionsGroup> optionsGroup) {
		thisExpandableListView = new ExpandableListView(
				ArbuckleAppActivity.this);
		thisExpandableListView.setGroupIndicator(null);
		thisExpandableListView.setCacheColorHint(0);
		thisExpandableListView.setSelector(R.drawable.blankselector);
		thisExpandableListView.setPadding(0, 0, 0,
				mainLayout.findViewById(R.id.handle)
				.getHeight());

		listAdapter = new ExpandMenuAdapter(ArbuckleAppActivity.this,
				optionsGroup, thisExpandableListView, orderHolder,
				clickStorage, mainLayout, thisTimeStamp);
		thisExpandableListView.setAdapter(listAdapter);
	}

	/*
	 * Initializes app interface, including tabhost, grabbing elements, and
	 * layout and actionbar.
	 */
	private void initialize() {
		tabHost = (TabHost) findViewById(R.id.tabhost);
		tabHost.setup();
		mainLayout = (FrameLayout) findViewById(R.id.mainlayout);
		ActionBar appActionBar = getSupportActionBar();
		appActionBar.setDisplayHomeAsUpEnabled(true);
	}

	private void initializeDialog() {
		dialogs = new DialogSet(ArbuckleAppActivity.this);
	}

	/*
	 * Sets the time stamps of the app based on PHP script
	 */
	private void setTimeValues() {
		thisTimeStamp = new TimeStamp();
		try {
			thisTimeStamp.setTimeStamp();
			timeValid = thisTimeStamp.getTimeStamp();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setToastValues();
	}

	/*
	 * Based on time values, sets values of toasts that inform user on his
	 * activities.
	 */
	private void setToastValues() {
		switch (timeValid) {
		case Constant.periodLockDown:
			if (orderHolder.isOrderSent()) {
				Toast.makeText(this,
						Constant.periodLockDownTextOrder(thisTimeStamp),
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(this,
						Constant.periodLockDownTextNoOrder(thisTimeStamp),
						Toast.LENGTH_SHORT).show();
			}
			break;
		case Constant.periodOrderToday:
			if (orderHolder.isOrderSent()) {
				Toast.makeText(this,
						Constant.periodTodayTextOrder(thisTimeStamp),
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(this,
						Constant.periodTodayTextNoOrder(thisTimeStamp),
						Toast.LENGTH_SHORT).show();
			}
		case Constant.periodOrderNextDay:
			if (orderHolder.isOrderSent()) {
				Toast.makeText(this,
						Constant.periodNextDayOrder(thisTimeStamp),
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(this,
						Constant.periodNextDayNoOrder(thisTimeStamp),
						Toast.LENGTH_SHORT).show();
			}

		}

	}

	public static String getUserName() {
		return userName;
	}


	/*
	 * Mainly ensures that the dialogs are dismissed so that the app can pause
	 * correctly.
	 */
	@Override
	protected void onPause() {
		super.onPause();
		if (quitDialog != null)
			quitDialog.dismiss();
		if (statusDialog != null)
			statusDialog.dismiss();
		if (menuDialog != null)
			menuDialog.dismiss();
		if (infoDialog != null)
			infoDialog.dismiss();
	}

	/*
	 * Saves permanent values where relevant, so that they are not lost when
	 * user closes or switches perspectives on the phone
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		OrderGetterSetter thisOrderHolder = listAdapter.getOrderHolder();
		ClickHolder thisClickHolder = listAdapter.getClickHolder();
		HashMap<String, double[]> thisOrderList = thisOrderHolder
		.getOrderList();
		HashMap<String, int[]> thisClickStorage = thisClickHolder
		.getHolderList();
		Boolean orderSent = thisOrderHolder.isOrderSent();
		Boolean validOrder = thisOrderHolder.isThereAValidOrder();
		pValues.setDatabase();
		pValues.setPermanentValues(thisOrderList, thisClickStorage, orderSent,
				validOrder);
		pValues.setPermanentUserName(userName);
		pValues.closePermanentValues();
	}

	/*
	 * Ensures onbackpressed simply removes the order slidingdrawer
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		SlidingDrawer thisDrawer = (SlidingDrawer) mainLayout
		.findViewById(R.id.sdOrder);
		thisDrawer.close();
	}

	/*
	 * Creates the actionbar menu (non-Javadoc)
	 * 
	 * @see
	 * com.actionbarsherlock.app.SherlockActivity#onCreateOptionsMenu(android
	 * .view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = new MenuInflater(this);
		inflater.inflate(R.menu.menu_options, menu);
		return true;
	}

	/*
	 * Listens for optionsmenu selection (non-Javadoc)
	 * 
	 * @see
	 * com.actionbarsherlock.app.SherlockActivity#onOptionsItemSelected(android
	 * .view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.dayMenu:
			menuDialog = dialogs.setMenuDialog(ArbuckleAppActivity.this);
			menuDialog.show();
			break;
		case R.id.quit_app:
			quitDialog = dialogs.setQuitDialog(ArbuckleAppActivity.this, firstName);
			quitDialog.show();
			break;
		case R.id.spInfo:
			infoDialog = dialogs.setInfoDialog(ArbuckleAppActivity.this, thisTimeStamp);
			infoDialog.show();
			break;
		case android.R.id.home:
			statusDialog = dialogs.setStatusDialog(ArbuckleAppActivity.this, listAdapter, thisTimeStamp);
			statusDialog.show();
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return super.onOptionsItemSelected(item);
	}

	public static final String TYPE = "type";
	public static final String GROUP = "group";
	public static final String ITEM = "item";

}