package arbuckle.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import android.R.integer;
import android.util.Log;
import android.widget.Toast;

public class OrderGetterSetter {


	private HashMap<String, double[]> orderList = new HashMap<String, double[]>();

	private Boolean validOrderPresent = false;
	private Boolean orderSent = false;

	/*gets the order key; important to determine type_group_item of order*/
	public String getOrderKey(String type, String group, String item){
		String key = type+Constant.delims+group+Constant.delims+item;
		return key;
	}

	public double[] getOrderValue(String orderKey){
		return orderList.get(orderKey);
	}

	/*sets price according to string value (from child price) and turns it into a double*/
	public double setPrice(String priceString){
		double priceNum = Double.valueOf(priceString) ;
		return priceNum;
	}

	/*public double getPrice(){
		return price;
	}*/

	/*sets quantity based on string of quantity in spinner; turns it into a double*/
	public double setQuantity (int quantityNum){
		double thisQuantity = (double) quantityNum;
		return thisQuantity;		
	}

	/*sets the group price for combos*/
	public double setGroupPrice(String price){
		double groupPrice = Double.valueOf(price);
		return groupPrice;
	}

	/*takes the Boolean spicy value and turns it into a double value for the order array*/
	public void setSpicy (Boolean spice, String key){
		double thisOrder[] = getOrderValue(key);		
		if (thisOrder != null){
			if (spice){
				thisOrder[SPICY] =1;
			}else{
				thisOrder[SPICY] = 0;
			}
			orderList.put(key, thisOrder);
		}
	}

	public double getTotalPrice(){
		double totalPrice = 0;
		Iterator<Entry<String, double[]>> elementEntries = orderList.entrySet().iterator();
		while (elementEntries.hasNext()){
			Entry<String, double[]> thisEntry = (Entry<String, double[]>) elementEntries.next();
			double linePrice;
			if (thisEntry.getKey().contains(Constant.combo_signaler)){
				linePrice = thisEntry.getValue()[GPRICE];
			}else{
				linePrice = thisEntry.getValue()[PRICE] * thisEntry.getValue()[QUANT];
			}
			totalPrice += linePrice;
		}
		return totalPrice;
	}

	/*sets all values (price, quantity, groupprice, spicy) and sets them against
	 * the type_group_item key
	 */
	public double[] setOrder(int thisQuantity, String thisPrice, String groupPrice){

		double thisOrder[] = new double[4];
		if ((thisPrice != null)&&(!thisPrice.isEmpty())){
			thisOrder[PRICE] = setPrice(thisPrice);
		}else{
			thisOrder[PRICE] = 0;
		}
		thisOrder[QUANT] = setQuantity(thisQuantity);
		if ((groupPrice != null)&&(!groupPrice.isEmpty())){
			thisOrder[GPRICE] = setGroupPrice(groupPrice);
		}else{
			thisOrder[GPRICE] = 0;
		}
		return thisOrder;
	}

	/*saves the values and attenant key into the orderList*/
	public void saveOrder(String key, double[] order){
		orderList.put(key, order);
	}
	/*given a key, does the order exist (has this item been ordered?*/
	public Boolean checkOrderExists(String key){
		if (orderList.containsKey(key)){
			return true;
		}else{
			return false;
		}
	}

	/*remove an order (if it is canceled); also check that the list is empty
	 * in which case --> states that there is no valid order in the app.
	 * 
	 */
	public void removeOrder (String key){
		orderList.remove(key);
		if (orderList.isEmpty()){
			setNoOrderValid();
		}
	}


	/*
	 * grabs the orderList for this session.
	 */
	public HashMap<String, double[]> getOrderList(){
		return orderList;
	}

	/*Method that implements order selection with a single command.
	 * NOTE: for combos, only valid items can be changed
	 * so the "removeOrder" indicates that any change from the optimal
	 * quantity selected resets the order (necessarily)
	 * 
	 * Checks for a change in order, and if that change is NOT to 0 adjusts
	 * the order accordingly; IF change is to 0, removes order
	 */
	public void handleItemOrder(String[] child, int pos, int oldPosition){

		String thisType = child[Constant.TYPE];
		String thisGroup = child[Constant.GROUPNAME];
		String thisItem = child[Constant.ITEMNAME];
		String thisPrice = child[Constant.PRICE];
		int thisQuantity = pos; 

		String key = getOrderKey(thisType, thisGroup, thisItem);

		if (pos != oldPosition){
			if (thisQuantity !=0){

				double[] value = setOrder(thisQuantity, thisPrice, null);

				saveOrder(key, value);

				if (thisType.equals(Constant.a_la_carte)) setOrderValid();
			}else{
				if (checkOrderExists(key)) removeOrder(key);
			}
		}
	}

	/*Handles group orders --> only set once quantitysum isChecked
	 */

	public void handleGroupOrder(ExpandOptionsGroup thisGroup, Boolean isChecked) {
		int quant = Constant.groupDefaultQuantity;
		String type = thisGroup.getType();
		String groupName = thisGroup.getName();
		if (type.equals(Constant.chef_Special)) groupName = type;
		String groupPrice = thisGroup.getPrice();

		String key = getOrderKey(type, groupName, Constant.combo_signaler);

		//setOrderKey(type, groupName, groupItem);
		if (isChecked){
			double[] value = setOrder(quant, null, groupPrice);
			saveOrder(key, value);
			setOrderValid();
		}else{
			if (checkOrderExists(key)) removeOrder(key);
			checkGroupSubsetExistence(type, groupName);
		}
	}

	public void checkGroupSubsetExistence(String type, String groupName) {
		if (!type.equals(Constant.a_la_carte)){
			String groupKey ="";
			if (type.equals(Constant.specials)){
				groupKey = type+Constant.delims+groupName+Constant.delims;
			}else{
				groupKey = type+Constant.delims;
			}
			int groupKeyNum = 0;
			Iterator<Entry<String, double[]>> elementEntries = orderList.entrySet().iterator();
			while (elementEntries.hasNext()){
				Entry<String, double[]> thisEntry = (Entry<String, double[]>) elementEntries.next();
				if (thisEntry.getKey().contains(groupKey)) groupKeyNum++;
			} 
			if (groupKeyNum == orderList.size()) setNoOrderValid();		
		}
	}


	/*Is there a valid order in the app? This is used mainly 
	 * for the color coding of the sliding drawer
	 */
	public void setOrderValid(){
		this.validOrderPresent=true;
	}

	public void setNoOrderValid(){
		this.validOrderPresent=false;
	}

	public Boolean isThereAValidOrder(){
		return validOrderPresent;
	}

	/*If the order was sent, all interactions except cancel all will be locked.
	 * Called on by the confirm button
	 */
	public void setOrderSent() {
		// TODO Auto-generated method stub
		orderSent = true;
	}

	public void setOrderCanceled() {
		// TODO Auto-generated method stub
		orderSent = false;
	}

	public boolean isOrderSent() {
		// TODO Auto-generated method stub
		return orderSent;
	}

	public void setOrderList(HashMap<String, double[]> permanentValues) {
		// TODO Auto-generated method stub
		this.orderList = permanentValues;
	}

	public static final int PRICE = 0;
	public static final int QUANT = 1;
	public static final int GPRICE = 2;
	public static final int SPICY = 3;


}
