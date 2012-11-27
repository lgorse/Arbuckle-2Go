package arbuckle.app;

import android.app.Activity;


import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Bitmap;

/*
 * Implements secure loogin of user via Stanford webAuth
 * Calls a webview that opens Stanford webauth
 */
public class SecureAppStarter  extends Activity {

	TextView report;
	WebView input;
	Context thisContext = this;
	String thisValue, url, thisFirstName;
	InternetConnectionChecks checkInternet;

	/*
	 * Calls on PHP script to login user and then pass on user info such as name, e-mail and SunetID to database
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.starter);
		initialize();


		WebViewClient rclient = new WebViewClient(){
			
			@Override
			public boolean shouldOverrideUrlLoading(WebView  view, String  url){
				return false;
			}
						
			@Override
			public void onLoadResource(WebView  view, String  url){	
				input.getSettings().setJavaScriptEnabled(true);
				input.addJavascriptInterface(new CustomJavaScriptInterface(thisContext), "Android");
			}
			
			@Override
			public void onPageFinished(WebView view, String url){
				if (thisValue == null){
					report.setText("Login via Stanford WebAuth. Secure but slow...");
				}else{
					report.setText("Welcome "+thisFirstName);
				}
				if (thisValue!= null){
					Intent passOn = new Intent("arbuckle.app.ArbuckleAppActivity");
					passOn.putExtra("username", thisValue);
					passOn.putExtra("firstName", thisFirstName);
					startActivity(passOn);
					finish();
					Toast.makeText(thisContext, thisValue, Toast.LENGTH_SHORT).show();
					return;
				}
			}
		};
		rclient.onPageFinished(input, url);
		input.setWebViewClient(rclient);
		input.loadUrl(url);
		
	}

/*
 * This javascript is implemented by the php script called on by the user once he is authenticated
 */
	public class CustomJavaScriptInterface {
		Context mContext;

		CustomJavaScriptInterface(Context context) {

			mContext = context;
		}

		public void getValue(String value){
			thisValue = value;
		}
		
		public void getFirstName(String jsFirstName){
			thisFirstName = jsFirstName;
		}
	}

	private void initialize() {
		report = (TextView) findViewById(R.id.tvViewName);
		input = (WebView) findViewById(R.id.wbWebAuth);
		url = "http://www.stanford.edu/group/arbucklecafe/cgi-bin/ArbuckleCafe/webauth.php";
		checkInternet = new InternetConnectionChecks(thisContext);
		Boolean isConnected = checkInternet.isNetworkAvailable();
		if (!isConnected){
			Toast.makeText(this, Constant.noInternet, Toast.LENGTH_SHORT).show();
			finish();
		}
	}


}


