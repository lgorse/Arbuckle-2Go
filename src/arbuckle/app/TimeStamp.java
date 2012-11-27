package arbuckle.app;



import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;


import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

/*
 * Gets the time values from the php database (so all times are centralized around Stanford times
 * startTime: when pickup can start
 * endTime: when pickup must end
 * cutofftime: when orders must stop
 * nextDay: defined as the next sushi day (currently either Monday or Thursday)
 */
public class TimeStamp {

	int timeStamp;
	String startTime;
	String cutOffTime;
	String endTime;
	String nextDay;

	
	public void setTimeStamp() throws InterruptedException, ExecutionException{
		GetTimeData getTimes = new GetTimeData();
		JSONObject thisTimeJSON = getTimes.execute().get();
		try {
			timeStamp = Integer.parseInt(thisTimeJSON.getString(Constant.validTime));
			startTime = thisTimeJSON.getString(Constant.startTime);
			cutOffTime = thisTimeJSON.getString(Constant.cutoffTime);
			endTime =  thisTimeJSON.getString(Constant.endTime);
			nextDay = thisTimeJSON.getString(Constant.nextDay);
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	public String getStartTime(){
		return startTime;
	}

	public String getCutOffTime(){
		return cutOffTime;
	}

	public String getEndTime(){
		return endTime;
	}
	
	public int getTimeStamp() {
		return timeStamp;	
	}
	
	public String getNextDay(){
		return nextDay;
	}
	

/*
 * Gets time values from php script
 */
	private class GetTimeData extends AsyncTask<Void, Void, JSONObject>{
		
		public GetTimeData(){
			super();
		}

		
		
		@Override
		protected JSONObject doInBackground(Void... params) {
			JSONObject timeJSON = null;
			HttpParams httpParams = new BasicHttpParams();
			HttpClient client = new DefaultHttpClient(httpParams);
			String url = "http://www.stanford.edu/group/arbucklecafe/cgi-bin/ArbuckleCafeTimeStampPrint.php";

			HttpPost request = new HttpPost(url);

			HttpResponse timeResponse;
			try {
				timeResponse = client.execute(request);
				HttpEntity timeEntity = timeResponse.getEntity();
				String timeString = EntityUtils.toString(timeEntity);
				try {
					timeJSON = new JSONObject(timeString.toString());
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} catch (ClientProtocolException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return timeJSON;
		}	
	}


}





