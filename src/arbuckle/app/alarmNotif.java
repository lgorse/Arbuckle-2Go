package arbuckle.app;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;


public class alarmNotif extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {
		NotificationManager notifManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		String firstName = intent.getStringExtra("name");
		notifTextParser textParse = new notifTextParser(context);
		ArrayList<String> textArray = new ArrayList<String>();
		try {
			textArray = textParse.execute().get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (firstName.equals("null")) firstName = "";
		String title = "Hi, "+firstName+"! ";
		title+=	textArray.get(0);
		String subTitle = textArray.get(1);
		Intent notifIntent = new Intent(context, SecureAppStarter.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notifIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		int id = Calendar.MILLISECOND;
				
		NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(context)
		.setContentTitle(title)
		.setContentText(subTitle)
		.setSmallIcon(R.drawable.ic_launcher)
		.setWhen(System.currentTimeMillis())
		.setContentIntent(pendingIntent)
		.setDefaults(Notification.DEFAULT_VIBRATE)
		.setDefaults(Notification.DEFAULT_LIGHTS)
		.setDefaults(Notification.DEFAULT_SOUND);		
		
		Notification notif = notifBuilder.getNotification();
		notifManager.notify(id, notif);
	}
	
	/*
	 * Parses the menu list from ArbuckleMenu.xml on WWW folder of the CGI-Bin
	 */
	private class notifTextParser extends AsyncTask<Void, Void, ArrayList<String>> {

		private Context context;
		private notifXMLHandler notifTextHandler = new notifXMLHandler();;
		private URL url;

		private notifTextParser(Context context) {
			super();
			this.context = context;
		}

		@Override
		protected ArrayList<String> doInBackground(Void... params) {

			try {
				url = new URL("http://www.stanford.edu/group/arbucklecafe/notification.xml");
			} catch (MalformedURLException e) {
				e.printStackTrace();
				Log.i("Error", "With the URL");
			}

			try {
				SAXParserFactory saxPF = SAXParserFactory.newInstance();
				SAXParser saxP = saxPF.newSAXParser();
				XMLReader xmlR = saxP.getXMLReader();

				xmlR.setContentHandler(notifTextHandler);
				xmlR.parse(new InputSource(url.openStream()));

			} catch (Exception e) {
				//Log.i(e.getCause().toString(), url.toString());
			}
			return notifTextHandler.getText();

		}
	}
	
	private class notifXMLHandler extends DefaultHandler {

		String elementValue = null;
		Boolean elementOn = false;
		ArrayList<String> notifText = new ArrayList<String>();
		String title, subTitle;
				
		
		private void setTitle(String element){
			title = element;
		}
		
		private String getTitle(){
			return title;
		}
		
		private void setSubtitle(String element){
			subTitle = element;
		}
		
		private String getSubtitle(){
			return subTitle;
		}
		
		public ArrayList<String> getText(){
			notifText.add(title);
			notifText.add(subTitle);
			return notifText;
		}


		public void startElement (String uri, String localName, String qName, Attributes attributes) throws SAXException{
			elementOn = true;
		}

		public void endElement (String uri, String localName, String qName) throws SAXException{
			elementOn = false;
			if (localName.equals(XMLTitle)) setTitle(elementValue) ;
			if (localName.equals(XMLSub)) setSubtitle(elementValue);
		}



		public void characters (char[] ch, int start, int length) throws SAXException{
			if (elementOn){
				elementValue = new String(ch, start, length);
				elementOn = false;
			}
		}
		
		private static final String XMLTitle = "title";
		private static final String XMLSub = "subtitle";
	}
}
