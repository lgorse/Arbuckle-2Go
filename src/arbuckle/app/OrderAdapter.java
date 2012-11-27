package arbuckle.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;


public class OrderAdapter extends BaseAdapter {

	Context context;
	HashMap<String, double[]> orderList;
	ArrayList<String> orderHeaders;
	ArrayList<String> comboSubsets;
	OrderGetterSetter orderHolder;
	ClickHolder clickStorage;
	ExpandableListView thisExpandableListView;
	ExpandMenuAdapter thisExpandMenuAdapter;
	Button allCancel, allConfirm;
	ListView orderReview;


	public OrderAdapter (Context passedContext, OrderGetterSetter orderHolder, 
			ClickHolder clickStorage, ExpandableListView thisExpandableListView, Button allCancel, Button allConfirm, ListView orderReview, ExpandMenuAdapter thisExpandMenuAdapter){
		this.context = passedContext;
		this.orderHolder=orderHolder;
		this.orderList = orderHolder.getOrderList();
		orderHeaders = getKeyArray();
		this.orderHolder = orderHolder;
		this.clickStorage = clickStorage;
		this.thisExpandableListView = thisExpandableListView;
		this.thisExpandMenuAdapter = thisExpandMenuAdapter;
		this.allCancel = allCancel;
		this.allConfirm = allConfirm;
		this.orderReview = orderReview;
	}

	/*
	 * This method separates the combo items (which are subsets of a combo order)
	 * from the a la carte orders.
	 * If a key is neither a la carte nor contains a combo signaler, it means it
	 * is an item under the Specials menu. It goes into the combosubsets list
	 * Otherwise the key goes into the orderHeader list, which is for 
	 * actual registered orders associated with a price
	 */
	private ArrayList<String> getKeyArray() {
		comboSubsets = new ArrayList<String>();
		ArrayList<String> thisKeyArray = new ArrayList<String>();
		Iterator<Entry<String, double[]>> orderEntries = orderList.entrySet().iterator();
		while (orderEntries.hasNext()){
			Entry<String, double[]> orderEntry = (Entry<String, double[]>) orderEntries.next();
			String key = orderEntry.getKey();
			if (!key.contains(Constant.a_la_carte)&&(!key.contains(Constant.combo_signaler))){
				comboSubsets.add(key);
			}else{
				thisKeyArray.add(key);	
			}
		}
		return thisKeyArray;
	}

	@Override
	public int getCount() {
		return orderHeaders.size();
	}

	@Override
	public String getItem(int position) {
		return orderHeaders.get(position);
	}

	@Override
	public long getItemId(int position) {	
		return position;
	}
	/*For a combo order: matches the subset entries to a wantedKey (the group key)
	 * and returns all orders within that group
	 */
	public String getSubset(String wantedKey){
		String thisKey = null;
		Iterator<String> subsetEntries = comboSubsets.iterator();
		while (subsetEntries.hasNext()){
			thisKey = (String) subsetEntries.next();
			if (thisKey.equals(wantedKey))break;
		}
		return thisKey;
	}

	/*Removes order from the header*/
	public void removeHeader(String key){
		orderHeaders.remove(orderHeaders.indexOf(key));
	}

	/*removes subsets under a group (when a group is canceled)
	 * 
	 */
	public void removeSubset(String key){
		comboSubsets.remove(key);
	}



	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		LayoutInflater orderInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
		convertView = orderInflater.inflate(R.layout.order_row, null);

		TextView name = (TextView) convertView.findViewById(R.id.tvOrderKey);
		TextView price = (TextView) convertView.findViewById(R.id.tvOrderPrice);
		TextView spice = (TextView) convertView.findViewById(R.id.tvOrderSpicy);

		Button singleCancel = (Button) convertView.findViewById(R.id.bCancel);

		if (orderHolder.isOrderSent()) singleCancel.setEnabled(false);

		String orderKey = (String) getItem(position);	

		if (orderKey != null){
			String[]headerTokens = parseKey(orderKey);
			String headerType = headerTokens[TYPE];
			String headerGroup = headerTokens[GROUP];
			String headerItem = headerTokens[ITEM];

			double[] values = orderList.get(orderKey);
			double thisPriceNum; 
			String groupSubset = "";
			String groupQuantities = "";
			String headerName = "";
			int thisQuantity = (int) values[OrderGetterSetter.QUANT];

			if (!headerItem.contains(Constant.combo_signaler)){
				if (!headerItem.equals(headerGroup)){
					headerName = headerItem+" "+headerGroup;
				}else{
					headerName = headerItem;
				}
				int thisSpicy = (int) values[OrderGetterSetter.SPICY];
				thisPriceNum= values[OrderGetterSetter.PRICE];
				TextView quantity = (TextView) convertView.findViewById(R.id.tvOrderQuantity);
				quantity.setText("Pieces: "+thisQuantity);
				if (thisSpicy != 0) spice.setText("Spicy");
			}else{
				TextView comboItems = (TextView) convertView.findViewById(R.id.tvComboSubsetNames);
				TextView comboQuantities = (TextView) convertView.findViewById(R.id.tvComboSubsetQuant);
				thisPriceNum = values[OrderGetterSetter.GPRICE];
				if (headerType.equals(Constant.chef_Special)){
					headerName = headerType;
					for (String key:comboSubsets){
						if ((key.contains(headerType))){
							String[]subsetTokens = parseKey(key);
							String subsetItem = subsetTokens[ITEM];
							double[] subsetValues = orderList.get(key);
							int subsetQuantity = (int) subsetValues[OrderGetterSetter.QUANT];
							groupSubset += subsetItem +"\n";
							groupQuantities += "Pieces: "+subsetQuantity +"\n";
						}
					}
				}else{
					headerName = headerGroup;
					for (String key:comboSubsets){
						if ((key.contains(headerType))&&(key.contains(headerGroup))){
							String[]subsetTokens = parseKey(key);
							String subsetItem = subsetTokens[ITEM];
							double[] subsetValues = orderList.get(key);
							int subsetQuantity = (int) subsetValues[OrderGetterSetter.QUANT];
							groupSubset += subsetItem +"\n";
							groupQuantities += "Pieces: "+subsetQuantity +"\n";
						}
					}
				}

				comboItems.setText(groupSubset);
				comboQuantities.setText(""+ groupQuantities);
			}

			name.setText(headerName);

			double linePrice = thisPriceNum*thisQuantity;
			price.setText(String.format("$ %.2f", linePrice));
		}
		singleCancel.setOnClickListener(new FinalOrderListener(context, clickStorage, orderHolder, thisExpandableListView, this, position, thisExpandMenuAdapter));

		allCancel.setOnClickListener(new FinalOrderListener(context, clickStorage, orderHolder, thisExpandableListView, this, position, thisExpandMenuAdapter));

		allConfirm.setOnClickListener (new FinalOrderListener(context, clickStorage, orderHolder, thisExpandableListView, this, position, thisExpandMenuAdapter));
		return convertView;
	}


	private String[] parseKey(String orderKey) {
		// TODO Auto-generated method stub
		String[]tokens = orderKey.split(Constant.delims);
		return tokens;
	}

	public ArrayList<String> getOrderHeader() {
		// TODO Auto-generated method stub
		return orderHeaders;
	}

	public ArrayList<String> getComboSubset() {

		return comboSubsets;
	}

	public OrderAdapter getOrderAdapter(){
		return this;
	}



	private static final int TYPE = 0;
	private static final int GROUP = 1;
	private static final int ITEM = 2;




}
