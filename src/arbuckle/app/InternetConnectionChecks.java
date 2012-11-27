package arbuckle.app;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class InternetConnectionChecks {
	
	Context context;
	static ConnectivityManager connectivityManager;

	public InternetConnectionChecks(Context context) {
		this.context =context; 
		connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);		
	}

		public boolean isNetworkAvailable(){
			NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
			if ((activeNetworkInfo != null)&&(activeNetworkInfo.isConnected())){
				return true;
			}else{
				return false;
			}
		
	}

}
