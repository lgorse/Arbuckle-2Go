package arbuckle.app;

import java.util.Calendar;

import android.os.Handler;
import android.widget.ExpandableListView;

public class Constant {

	public static final String rootCategory = "Menu";
	public static final String typeCategory = "Type";
	public static final String groupCategory = "Option";
	public static final String itemCategory = "item";


	public static final String attributeType = "type";
	public static final String attributeGroup = "option";
	public static final String attributeName = "name";
	public static final String attributePrice = "price";
	public static final String attributeDetail = "detail";
	public static final String attributeSpice  = "spice";
	public static final String attributeItem = "item";

	public static final String chef_Special = "Chef's Special";
	public static final String specials = "Specials";
	public static final String a_la_carte = "A la Carte";
	public static final String combo_signaler="*";
	public static final String delims ="_";

	public static final int CARTEMAX = 10;
	public static final int SPECIALSASHIMIMAX = 7;
	public static final int CHEFSPECIALNIGIRIMAX = 3;
	public static final int CHEFSPECIALHANDROLLMAX = 1;

	public static final int periodOrderToday = 0;
	public static final int periodLockDown = 1;
	public static final int periodOrderNextDay = 2;
	
	public static final int CATEGORY = 0;
	public static final int TYPE = 1;
	public static final int GROUPNAME = 2;
	public static final int ITEMNAME = 3;
	public static final int PRICE = 4;
	public static final int DETAIL = 5;
	public static final int SPICE = 6;

	
	public static final CharSequence defaultDetail = "Change Lives. Change Organizations. Change the World.";

	//These values refer to time strings in ArbuckleCafetimeStamp PHP file; never alter them without first altering the PHP
	public static final String validTime = "validtime";
	public static final String startTime = "starttime";
	public static final String cutoffTime = "cutoff";
	public static final String endTime = "endtime";
	public static final String nextDay = "nextDay";
	public static int day1 = Calendar.MONDAY;
	public static int day2 = Calendar.THURSDAY;
	
	//These values pertain to Arbuckle's general menu
	public static int menuDESC = 1;
	public static int menuPRICE = 2;
	public static int menuTITLE = 0;
	

	public static void refreshMenuAdapter(final ExpandableListView thisExpandableListView){
		Handler refresh = new Handler();
		refresh.post(new Runnable(){

			@Override
			public void run() {
				thisExpandableListView.invalidateViews();
			}

		});
	}

	//toast messages
	public static final String noInternet = "Sorry, you have no internet connection. Try again later.";
	public static final String generalError = "Sorry! There is a problem with your connection. Try again later.";
	public static final String welcome = "Sushi time!";
	public static final String cancelFailure = "Oops! No internet connection. Get connected fast to cancel your order";
	public static final CharSequence orderPassed = "Combo Ordered";
	public static final String comboDetails = "Combo Details";
	public static final int groupDefaultQuantity = 1;
	public static final CharSequence orderUpdated = "Order updated";
	public static final CharSequence comboAdded = "Combo added";
	public static final CharSequence comboRemoved = "Combo removed";
	public static final int chefSpecialNigiriPos = 0;
	public static final int chefSpecialHandPos = 1;
	public static final CharSequence logout = "Are you sure?";
	public static final CharSequence sendFailure = "Could not send order";
	
	//SlidingDrawer info
	public static final CharSequence preOrder = "Place an order";
	public static final CharSequence confirmOrder = "Review your order";
	public static final CharSequence orderLockOff = "Order Lockoff Period";

	public static String comboRemains(int remainder) {
		String remainMessage;
		if (remainder == 1){
			remainMessage = "Order "+remainder+" more piece";
		}else{
			remainMessage = "Order "+remainder+" more pieces";
		}
		return remainMessage;
	}


	public static CharSequence comboChefSpecial(String string) {
		String message = "Complete this order by selecting your "+string+"."; 
		return message;
	}

	public static String periodLockDownTextOrder(TimeStamp timeStamp) {
		String periodLockDownTextOrder = "You cannot order again today. Pick up your order at Arbuckle after "+timeStamp.getStartTime();
		return periodLockDownTextOrder;
	}


	public static String periodLockDownTextNoOrder(TimeStamp timeStamp) {
		String periodLockDownTextNoOrder = "No more orders today. You can order for "+timeStamp.getNextDay()+" at "+timeStamp.getEndTime();
		return periodLockDownTextNoOrder;
	}


	public static String periodTodayTextOrder(TimeStamp thisTimeStamp) {
		String periodTodayTextOrder = "You placed an order for today. Change it until "+thisTimeStamp.getCutOffTime();
		return periodTodayTextOrder;
	}


	public static String periodTodayTextNoOrder(TimeStamp thisTimeStamp) {
		String periodTodayTextNoOrder = "You can place an order until "+thisTimeStamp.getCutOffTime();
		return periodTodayTextNoOrder;
	}


	public static String periodNextDayOrder(TimeStamp thisTimeStamp) {
		String periodNextDayOrder = "You've placed an order. Change it until "+ thisTimeStamp.getNextDay()+ " at "+thisTimeStamp.getCutOffTime();
		return periodNextDayOrder;
	}


	public static String periodNextDayNoOrder(TimeStamp thisTimeStamp) {
		String periodNextDayNoOrder = "You can place an order until "+thisTimeStamp.getNextDay()+ " at "+thisTimeStamp.getCutOffTime();
		return periodNextDayNoOrder;
	}

}

/*Iterator<Entry<String, double[]>> elementEntries = orderList.entrySet().iterator();
while (elementEntries.hasNext()){
	Entry<String, double[]> thisEntry = (Entry<String, double[]>) elementEntries.next();
	Log.i("the orderList name is"+thisEntry.getKey(), ""+thisEntry.getValue()[1]);

}

for (int i = 0; i <orderHeaders.size(); i++){
	String headerKey = orderHeaders.get(i);
	Log.i("The OrderHeader key is", headerKey);
}

for (int j = 0; j< comboSubsets.size(); j++){
	String comboKey = comboSubsets.get(j);
	Log.i("the ComboSubsets key is", comboKey);
}*/
