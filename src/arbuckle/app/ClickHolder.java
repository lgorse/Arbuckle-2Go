package arbuckle.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import android.text.style.QuoteSpan;
import android.util.Log;
import android.widget.ExpandableListView;
import android.widget.Toast;

public class ClickHolder {

	private HashMap<String, int[]> holderMap = new HashMap<String, int[]>();
	
	public int setSpicy(Boolean spice){
		int spiceInt;
		if (spice){
			spiceInt = 1;
		}else{
			spiceInt = 0;
		}
		return spiceInt;
	}

	/*ID's the position of the view in the viewFlipper: order or description
	 * Checks if the view has already been clicked and then alters it.
	 * If it has never been clicked, adds that position to the list of clicked items
	 * NOTE: for now this list is cumulative and the items are never removed once clicked
	 * (chance of a memory leak!)*/
	public void recordClick (int groupPosition, int childPosition, int view, String key){
		int[] holder = setHolder (groupPosition, childPosition, view);
		if (checkViewClicked (key)){
			setViewClicked (view, key);
			setOrderClick (key, getOrderClickStatus(key));
		}else{
			addHolder(key, holder);
		}
	}


	/*Was this view clicked in the past?*/
	public boolean checkViewClicked(String key){
		boolean containChecked = false;
		if (holderMap.containsKey(key))containChecked = true;
		return containChecked;
	}

	/*Adds view value to holderList --> list of views that have been clicked*/
	public void setViewClicked(int view, String key){
		if (holderMap.containsKey(key)){
			int[] containedHolder = holderMap.get(key);
			containedHolder[VIEW] = view;
			holderMap.put(key, containedHolder);
		}
	}

	/*Get the last view clicked*/
	public int getViewClicked(String key){
		int newView = -1;
		if (holderMap.containsKey(key)){
			int[] containedHolder= holderMap.get(key);
			newView = containedHolder[VIEW];
		}
		return newView;
	}

	/*Set the order button as clicked*/
	public void setOrderClick (String key, Boolean clickValue){
		if (holderMap.containsKey(key)){
			int[] containedHolder = holderMap.get(key);
			if (clickValue){
				containedHolder[ORDERCLICK] = 1;
			}else{
				containedHolder[ORDERCLICK] = 0;
			}

		}
	}

	/*Determine whether the order button was clicked*/
	public Boolean getOrderClickStatus(String key){
		Boolean isClicked = false;
		if (holderMap.containsKey(key)){
			int[] containedHolder= holderMap.get(key);
			int clickStatus = containedHolder[ORDERCLICK];
			switch (clickStatus){
			case 1: isClicked = true; break;
			}
		}	
		return isClicked;
	}

	/*Was the combo button clicked or not?*/
	public Boolean getGroupOrderClickStatus(String group, String type){
		Boolean isClicked = false;
		String key = setKey(group, Constant.combo_signaler, type);
		if (holderMap.containsKey(key)){
			int[] containedHolder= holderMap.get(key);
			int clickStatus = containedHolder[ORDERCLICK];
			switch (clickStatus){
			case 1: isClicked = true; break;
			}
		}	
		return isClicked;
	}
	
	public void setSpiceValue(boolean isChecked, String key) {
		if (holderMap.containsKey(key)){
			int[] containedHolder = holderMap.get(key);
			containedHolder[SPICY] = setSpicy(isChecked);
			holderMap.put(key, containedHolder);
		}
	}

	public Boolean getSpiceValue(String key){
		Boolean spicyBoolean = false;
		if (holderMap.containsKey(key)){
			int[] containedHolder= holderMap.get(key);
			if (containedHolder[SPICY]==1){
				spicyBoolean = true;
			}else{
				spicyBoolean = false;
			}
		}
		return spicyBoolean;
	}	

	/*Set the quantity at this item*/
	public void setQuantity(int quantity, String key){
		if (holderMap.containsKey(key)){
			int[] containedHolder = holderMap.get(key);
			containedHolder[QUANTITY] = quantity;
			holderMap.put(key, containedHolder);
		}
	}


	/*Get the last quantity of this spinner*/
	public int getQuantity(String key){
		int quantityOrdered = 0;
		if (holderMap.containsKey(key)){
			int[] containedHolder= holderMap.get(key);
			quantityOrdered = containedHolder[QUANTITY];
		}
		return quantityOrdered;
	}

	/*Get the value of the sum of selected items inside a group*/
	public int getQuantitySum(int groupPosition, String type){
		Iterator<Entry<String, int[]>> clickEntries = holderMap.entrySet().iterator();
		ArrayList <Integer> numList = new ArrayList<Integer>();
		while (clickEntries.hasNext()){
			Entry<String, int[]> thisEntry = (Entry<String, int[]>) clickEntries.next();	
			String entryKey = thisEntry.getKey();
			String[]keyParts = entryKey.split(Constant.delims);
			String thisType = keyParts[GROUP];
			int[] value = thisEntry.getValue();
			if ((value[GROUP]== groupPosition)&& (thisType.equals(type))) numList.add(value[QUANTITY]);
		}

		Integer sum = 0;
		for (Integer i:numList){
			sum +=i;
		}
		return sum;	
	}

	/*Checks if the quantity currently ordered fulfills a quantity limit
	 * For item rows, this will return false*/
	public Boolean checkQuantitySum (int groupPosition, String type){
		Boolean sumCheck = false;
		int thisMax = -1;
		int otherMax = 0;
		int otherValue = 0;
		int thisSum = getQuantitySum (groupPosition, type);

		if (type.equals(Constant.specials)&& (groupPosition==0))thisMax = Constant.SPECIALSASHIMIMAX;
		if (type.equals(Constant.chef_Special)){
			if(groupPosition==Constant.chefSpecialNigiriPos){
				thisMax = Constant.CHEFSPECIALNIGIRIMAX;
				otherMax = Constant.CHEFSPECIALHANDROLLMAX;
				otherValue = getQuantitySum(1, type);
			}else if (groupPosition==Constant.chefSpecialHandPos){
				thisMax = Constant.CHEFSPECIALHANDROLLMAX;
				otherMax = Constant.CHEFSPECIALNIGIRIMAX;
				otherValue = getQuantitySum(0, type);
			}
			if ((otherValue == otherMax) && (thisSum == thisMax)) sumCheck = true;
		}else{
			if (thisSum !=0){
				if (thisSum == thisMax){
					sumCheck = true;
				}else{
					sumCheck = false;
				}
			}
		}
		return sumCheck;
	}
	
	public int getComboMax(String type, int groupPosition){
		int max = 0;
		if (type.equals(Constant.specials)&& (groupPosition==0))max = Constant.SPECIALSASHIMIMAX;
		if (type.equals(Constant.chef_Special)){
			if(groupPosition==0){
				max = Constant.CHEFSPECIALNIGIRIMAX;
			}else if (groupPosition==1){
				max = Constant.CHEFSPECIALHANDROLLMAX;
			}
		}
		return max;
		
	}

	/*Sets the holder that will contain values such as group, child and view for each item in the holderlist*/
	public int[] setHolder(int group, int child, int view){
		int[] holder = new int[6];
		holder[GROUP] = group;
		holder[CHILD] = child;
		holder[VIEW] = view;
		return holder;
	}

	/*Sets the key for each item that was interacted with*/
	public String setKey(String group, String child, String type){
		String groupString = "_"+group;
		String childString = "_"+child;
		String key = type.concat(groupString.concat(childString));
		return key;		
	}

	public void addHolder(String key, int[] holder){
		holderMap.put(key, holder);
	}

	public void removeHolder(String key){
		holderMap.remove(key);

	}
	public HashMap<String, int[]> getHolderList(){
		return holderMap;
	}

	public void setHolderList(HashMap<String, int[]> pHolderMap){
		this.holderMap = pHolderMap;
	}


	public static final int GROUP = 0;
	public static final int CHILD = 1;
	public static final int VIEW = 2;
	public static final int QUANTITY = 3;
	public static final int ORDERCLICK = 4;
	public static final int SPICY = 5;

	private static final int TYPEKEY = 0;
	private static final int GROUPKEY = 1;
	private static final int ITEMKEY = 2;
	
	

	public static final int GROUPVIEW = -1;
	
}



