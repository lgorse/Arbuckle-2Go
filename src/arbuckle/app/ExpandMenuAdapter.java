package arbuckle.app;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SlidingDrawer;
import android.widget.SlidingDrawer.OnDrawerCloseListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import arbuckle.app.SwipeDetector.Action;

public class ExpandMenuAdapter extends BaseExpandableListAdapter {

	private Context context;
	private ArrayList<ExpandOptionsGroup> groupList;
	private ExpandableListView thisExpandableListView;

	private OrderGetterSetter orderHolder;
	private ClickHolder clickStorage;
	
	private FrameLayout mainLayout;
	private SlidingDrawer orderComplete;
	private Button orderCancel, orderConfirm;
	private ListView orderReview;
	private OrderAdapter orderAdapter;
	private TextView totalPrice;
	private TimeStamp timeStamp;
	private int timeValid;


	private String defaultDetail;
	int[] posGroupChild;
	String[] child;

	final SwipeDetector swipeDetector = new SwipeDetector();


	public ExpandMenuAdapter(Context passedContext, ArrayList<ExpandOptionsGroup> passedGroupList, ExpandableListView thisExpandableListView, 
			OrderGetterSetter orderHolder, ClickHolder clickStorage, FrameLayout mainLayout, TimeStamp timeStamp){
		this.context = passedContext;
		this.groupList = passedGroupList;
		this.thisExpandableListView = thisExpandableListView;
		this.orderHolder = orderHolder;
		this.clickStorage = clickStorage;
		this.mainLayout = mainLayout;
		this.timeValid = timeStamp.getTimeStamp();
		this.timeStamp = timeStamp;
		detailXMLTask defaultDetailHandler = new detailXMLTask(context);
		try {
			defaultDetail = defaultDetailHandler.execute().get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		orderMenuSetup();

	}


	public void addItem(String[] child, ExpandOptionsGroup group) {
		if (!groupList.contains(group)) {
			groupList.add(group);
		}
		int index = groupList.indexOf(group);
		ArrayList<String[]> ch = groupList.get(index).getChildren();
		ch.add(child);
		groupList.get(index).setChildren(ch);
	}


	@Override
	public String[] getChild(int groupPosition, int childPosition) {
		ArrayList<String[]> chList = groupList.get(groupPosition).getChildren();
		return chList.get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}


	@Override
	public int getChildrenCount(int groupPosition) {
		ArrayList<String[]> chList = groupList.get(groupPosition).getChildren();
		return chList.size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return groupList.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return groupList.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	/*
	 * The ViewHolder class is the holder of values for the 
	 * items rows
	 */
	public static class ViewHolder {
		//itemRows
		public TextView itemName;
		public TextView itemPrice;
		public TextView itemDetail;
		public TextView itemDetailItem;
		public Spinner itemQuantity;
		public CheckBox itemSpicy;

		public ViewFlipper rowFlipper;
	}


	/*
	 * The optionViewHolder class is the holder of valus for the group rows
	 */
	private static class OptionViewHolder{
		public RelativeLayout chefLayout;
		public RelativeLayout standardOptionBackground;
		public TextView chefName;
		public TextView chefDetail;
		public TextView chefPrice;


		public TextView optionName;
		public TextView optionDetail;
		public TextView optionPrice;

		public TextView chefPointer;
		public TextView specialPointer;
		public ImageView groupExpander;


	}

	/*
	 * Populates the child view with all the relevant information
	 * (non-Javadoc)
	 * @see android.widget.ExpandableListAdapter#getChildView(int, int, boolean, android.view.View, android.view.ViewGroup)
	 */

	@Override
	public View getChildView(final int groupPosition, final int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		posGroupChild = new int[2];
		posGroupChild[0] = groupPosition;
		posGroupChild[1] = childPosition;

		View rowView = null;
		final ViewHolder viewHolder;

		child = getChild(groupPosition, childPosition);
		final String groupName = child[Constant.GROUPNAME];
		final String childName = child[Constant.ITEMNAME];

		final ExpandOptionsGroup thisGroup = (ExpandOptionsGroup) getGroup(groupPosition); 
		final String thisType = thisGroup.getType(); 
		final int typeNum = thisGroup.getTypeNum();

		final String key = orderHolder.getOrderKey(thisType, groupName, childName);
		String groupKey = orderHolder.getOrderKey(thisType, groupName, Constant.combo_signaler);


		if (convertView == null){
			LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
			rowView = infalInflater.inflate(R.layout.item_row, null);

			viewHolder = new ViewHolder();


			viewHolder.itemName = (TextView) rowView.findViewById(R.id.tvItem);
			viewHolder.itemPrice = (TextView) rowView.findViewById(R.id.tvPrice);
			viewHolder.itemDetail = (TextView) rowView.findViewById(R.id.tvDetail);
			viewHolder.itemDetailItem = (TextView) rowView.findViewById(R.id.tvdetailItem);

			viewHolder.rowFlipper = (ViewFlipper) rowView.findViewById(R.id.item_row_flipper);

			viewHolder.itemQuantity = (Spinner) rowView.findViewById(R.id.spQuantity);

			viewHolder.itemSpicy = (CheckBox) rowView.findViewById(R.id.tvSpicy);


			rowView.setTag(viewHolder);
			viewHolder.rowFlipper.setTag(posGroupChild);


		}else{
			rowView = convertView;
			viewHolder = (ViewHolder) rowView.getTag();
			viewHolder.rowFlipper.setTag(posGroupChild);
		}

		final ViewHolder holder = (ViewHolder) rowView.getTag();
		holder.itemSpicy.setVisibility(View.GONE);
		holder.itemSpicy.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				clickStorage.setSpiceValue(isChecked, key);
				orderHolder.setSpicy(isChecked, key);
				Constant.refreshMenuAdapter(thisExpandableListView);
			}
		});
		holder.itemSpicy.setChecked(clickStorage.getSpiceValue(key));

		holder.itemQuantity.setSelection(clickStorage.getQuantity(key));

		holder.itemQuantity = setSpinner(viewHolder.itemQuantity, typeNum, groupPosition, childPosition, thisType);

		if (clickStorage.getGroupOrderClickStatus(groupName, thisType)){
			holder.rowFlipper.setEnabled(false);
			holder.itemQuantity.setClickable(false);
		}else{
			holder.rowFlipper.setEnabled(true);
			holder.itemQuantity.setClickable(true);
		}


		if (clickStorage.checkQuantitySum(groupPosition, thisType)) {
			clickStorage.setOrderClick(groupKey, true);
			orderHolder.handleGroupOrder(thisGroup, true);
		}else{			
			clickStorage.setOrderClick(groupKey, false);
			orderHolder.handleGroupOrder(thisGroup, false);
		}

		holder.rowFlipper.setOnTouchListener(swipeDetector);	
		holder.rowFlipper.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (swipeDetector.swipeDetected()){
					if (swipeDetector.getAction() == Action.LR) {
						holder.rowFlipper.showPrevious();
					}else if (swipeDetector.getAction() == Action.RL){	
						holder.rowFlipper.showNext();
					}
				}else{
					if (holder.rowFlipper.getDisplayedChild()==VIEW_STANDARD) {
						holder.rowFlipper.setDisplayedChild(VIEW_ORDER);	
					}else {
						holder.rowFlipper.setDisplayedChild(VIEW_STANDARD);
					}
				}

				clickStorage.recordClick(groupPosition, childPosition, holder.rowFlipper.getDisplayedChild(), key);
			}
		});

		checkRowStatus(holder.rowFlipper, holder.itemQuantity, groupPosition, groupName, childName, thisType);

		holder.itemName.setText(child[Constant.ITEMNAME]);
		holder.itemDetailItem.setText(child[Constant.ITEMNAME]);

		if (!child[Constant.DETAIL].isEmpty()){
			holder.itemDetail.setText(child[Constant.DETAIL]);
		}else{
			holder.itemDetail.setText(defaultDetail);
		}
		if(!child[Constant.PRICE].isEmpty()){
			holder.itemPrice.setText("$"+child[Constant.PRICE]);
		}else{
			holder.itemPrice.setText("");
		}
		orderMenuSetup();
		return rowView;
	}

	/*
	 * Populates the group row
	 * Pays attention to whether the group is a la carte, chef's special or simply specials
	 * (non-Javadoc)
	 * @see android.widget.ExpandableListAdapter#getGroupView(int, boolean, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {

		View optionView = null;
		final OptionViewHolder optionViewHolder;

		ExpandOptionsGroup thisGroup = (ExpandOptionsGroup) getGroup(groupPosition);
		final String thisType = thisGroup.getType();
		final int thisTypeNum = thisGroup.getTypeNum();

		final String thisPrice = thisGroup.getPrice();

		int[] thisGroupPos = new int[2];
		thisGroupPos[ClickHolder.GROUP] = groupPosition;
		thisGroupPos[ClickHolder.CHILD] = ClickHolder.GROUPVIEW;

		if (convertView == null){
			LayoutInflater inf = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
			optionView = inf.inflate(R.layout.option_row, null);

			optionViewHolder = new OptionViewHolder();

			optionViewHolder.chefLayout = (RelativeLayout) optionView.findViewById(R.id.rlChefs_Special_Order);
			optionViewHolder.chefLayout.setVisibility(View.GONE);

			if ((thisTypeNum == ExpandOptionsGroup.CHEFSPECIAL)){
				optionViewHolder.chefLayout.setVisibility(View.VISIBLE);

				optionViewHolder.chefName = (TextView) optionView.findViewById(R.id.tvChef_Special_Name);


				optionViewHolder.chefDetail = (TextView) optionView.findViewById(R.id.tvChef_Special_Detail);



				optionViewHolder.chefPointer = (TextView) optionView.findViewById(R.id.tvChef_Special_Confirm);


				optionViewHolder.chefPrice = (TextView) optionView.findViewById(R.id.tvChef_Special_Price);

				optionViewHolder.chefLayout.setTag(thisGroupPos);

			}

			optionViewHolder.standardOptionBackground = (RelativeLayout) optionView.findViewById(R.id.standardOptionLayout);

			optionViewHolder.optionName = (TextView) optionView.findViewById(R.id.tvOption);


			optionViewHolder.optionDetail = (TextView) optionView.findViewById(R.id.tvOptionDetail);

			optionViewHolder.optionPrice = (TextView) optionView.findViewById(R.id.tvOptionPrice);


			optionViewHolder.specialPointer = (TextView) optionView.findViewById(R.id.tvSpecialOK);

			optionViewHolder.groupExpander = (ImageView) optionView.findViewById(R.id.ivGroupIndicator);


			optionView.setTag(optionViewHolder);
			optionViewHolder.standardOptionBackground.setTag(thisGroupPos);


		}else{
			optionView = convertView;
			optionViewHolder = (OptionViewHolder) optionView.getTag();
			;
		}

		final OptionViewHolder thisOptionViewHolder = (OptionViewHolder) optionView.getTag();
		TextView comboTextView;

		if ((thisTypeNum == ExpandOptionsGroup.CHEFSPECIAL)&&(groupPosition == 0)){
			optionViewHolder.chefLayout.setVisibility(View.VISIBLE);
			thisOptionViewHolder.chefName.setText(thisType);
			thisOptionViewHolder.chefPrice.setText("$"+thisPrice);
			comboTextView = thisOptionViewHolder.chefPointer;
			optionViewHolder.optionPrice.setVisibility(View.GONE);
		}else{
			if (thisTypeNum==ExpandOptionsGroup.CHEFSPECIAL) optionViewHolder.optionPrice.setVisibility(View.GONE);
			optionViewHolder.chefLayout.setVisibility(View.GONE);			
			comboTextView = thisOptionViewHolder.specialPointer;
		}

		if (thisTypeNum == ExpandOptionsGroup.SPECIALS) optionViewHolder.groupExpander.setVisibility(View.GONE);
		if (isExpanded){
			optionViewHolder.groupExpander.setImageResource(R.drawable.group_collapser);
		}else{
			optionViewHolder.groupExpander.setImageResource(R.drawable.group_expander);
		}

		if (clickStorage.checkQuantitySum(groupPosition, thisType)) {
			comboTextView.setText(Constant.orderPassed);
			comboTextView.setSelected(true);
		}else{			
			comboTextView.setText(Constant.comboDetails);
			comboTextView.setSelected(false);
		}
		thisOptionViewHolder.optionName.setText(thisGroup.getName());
		thisOptionViewHolder.optionDetail.setText(thisGroup.getDetail());

		if (!thisGroup.getPrice().isEmpty()){
			thisOptionViewHolder.optionPrice.setText("$"+thisGroup.getPrice());
		}else{
			thisOptionViewHolder.optionPrice.setText("");
		}

		if (thisTypeNum != ExpandOptionsGroup.SPECIALS){
			thisOptionViewHolder.specialPointer.setVisibility(View.GONE);
			thisOptionViewHolder.groupExpander.setVisibility(View.VISIBLE);
		}
		orderMenuSetup();
		return optionView;
	}

	/*
	 * Identifies all graphical elements of each child
	 * based on its status in the UI: clicked, whether the quantitySum is fulfilled,
	 * etc.
	 */
	private void checkRowStatus(ViewFlipper rowFlipper, Spinner itemQuantity,
			int groupPosition, String groupName, String childName, String thisType) {

		String key = orderHolder.getOrderKey(thisType, groupName, childName);

		if (clickStorage.checkViewClicked(key)){		
			rowFlipper.setDisplayedChild(clickStorage.getViewClicked(key));
			itemQuantity.setSelection(clickStorage.getQuantity(key));	
			if ((clickStorage.checkQuantitySum(groupPosition, thisType))&&(clickStorage.getQuantity(key))==0){
				rowFlipper.setEnabled(false);
				itemQuantity.setEnabled(false);
				rowFlipper.getChildAt(VIEW_STANDARD).setEnabled(false);
			}else{
				rowFlipper.setEnabled(true);
				itemQuantity.setEnabled(true);	
				rowFlipper.getChildAt(VIEW_STANDARD).setEnabled(true);
			}
		}else{
			rowFlipper.setDisplayedChild(VIEW_STANDARD);
			itemQuantity.setSelection(0);
			if (clickStorage.checkQuantitySum(groupPosition, thisType)){
				rowFlipper.setEnabled(false);
				itemQuantity.setEnabled(false);	
				rowFlipper.getChildAt(VIEW_STANDARD).setEnabled(false);
			}else{
				rowFlipper.setEnabled(true);
				itemQuantity.setEnabled(true);	
				rowFlipper.getChildAt(VIEW_STANDARD).setEnabled(true);
			}
		}
		rowFlipper.getChildAt(VIEW_STANDARD).setBackgroundResource(R.drawable.itembackground);

	}

	/*
	 * Sets the spinner for the quantity ordered values
	 */

	private Spinner setSpinner(Spinner itemQuantity, int spinnerType, int groupPosition, int childPosition, String thisType) {

		String[] child = getChild(groupPosition, childPosition);

		String key = orderHolder.getOrderKey(thisType, child[Constant.GROUPNAME], child[Constant.ITEMNAME]);
		int thisQuantity = clickStorage.getQuantity(key);
		int quantityMax = Constant.CARTEMAX+1;;

		switch(spinnerType){
		case ExpandOptionsGroup.ALACARTE: quantityMax = Constant.CARTEMAX+1; break;
		case ExpandOptionsGroup.SPECIALS: 
			quantityMax = Constant.SPECIALSASHIMIMAX+1 - clickStorage.getQuantitySum(groupPosition, thisType);
			if (thisQuantity != 0)	quantityMax +=  thisQuantity;
			break;
		case ExpandOptionsGroup.CHEFSPECIAL: 
			switch (groupPosition){
			case 0: 
				quantityMax = Constant.CHEFSPECIALNIGIRIMAX+1 - clickStorage.getQuantitySum(groupPosition, thisType);
				if (thisQuantity != 0)	quantityMax +=  thisQuantity;
				break;
			case 1: 
				quantityMax = Constant.CHEFSPECIALHANDROLLMAX+1 - clickStorage.getQuantitySum(groupPosition, thisType);
				if (thisQuantity != 0)	quantityMax +=  thisQuantity;
				break;
			}
		}
		String[] quantityList = new String[quantityMax];
		int quantListLenght = quantityList.length;
		for (int v = 0; v<quantListLenght;v++){
			int num = v;
			quantityList[v] = ""+num;
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, R.layout.quantityspinner, quantityList);
		adapter.setDropDownViewResource(R.layout.quantityspinner);
		itemQuantity.setAdapter(adapter);

		itemQuantity.setOnItemSelectedListener(new QuantitySelectedListener(context, orderHolder, thisExpandableListView, this, clickStorage, groupPosition, childPosition, orderComplete));
		return itemQuantity;
	}

	/*
	 * Sets up the order sliding drawer and its contents
	 */

	private void orderMenuSetup() {

		orderCancel = (Button) mainLayout.findViewById(R.id.bCancelAll);
		orderConfirm = (Button) mainLayout.findViewById(R.id.bConfirm);

		orderReview = (ListView) mainLayout.findViewById(R.id.tvOrder);

		orderComplete = (SlidingDrawer) mainLayout.findViewById(R.id.sdOrder);
		TextView orderCompleteHandle = (TextView) orderComplete.getHandle();
		totalPrice = (TextView) mainLayout.findViewById(R.id.tvTotalPrice);

		orderAdapter = new OrderAdapter(context,orderHolder, clickStorage, thisExpandableListView, orderCancel, orderConfirm, orderReview, ExpandMenuAdapter.this);
		orderReview.setAdapter(orderAdapter);
		if (orderHolder.isThereAValidOrder()) orderReview.setVisibility(View.VISIBLE);

		if (timeValid != Constant.periodLockDown){
			orderCancel.setEnabled(true);
			orderConfirm.setEnabled(true);
			orderComplete.unlock();
			if (orderHolder.isThereAValidOrder()){
				totalPrice.setVisibility(View.VISIBLE);
				totalPrice.setText(String.format("Total Price: $ %.2f", orderHolder.getTotalPrice()));
				if (!orderHolder.isOrderSent()){
					orderCompleteHandle.setBackgroundResource(R.drawable.handlerfull);
					orderCompleteHandle.setText(Constant.confirmOrder);
					orderCompleteHandle.setTextColor(context.getResources().getColor(R.color.white));
					orderCancel.setEnabled(true);
					orderConfirm.setEnabled(true);
					orderComplete.unlock();
				}else if (orderHolder.isOrderSent()){
					orderCompleteHandle.setBackgroundResource(R.drawable.handlersent);
					orderCompleteHandle.setTextColor(context.getResources().getColor(R.color.green));
					if (timeValid == Constant.periodOrderNextDay){
						orderCompleteHandle.setText("Order Sent - pick up next "+timeStamp.getNextDay());
					}else{	
						orderCompleteHandle.setText("Order Sent - pick up today");
					}
					orderConfirm.setEnabled(false);
					orderComplete.open();
					orderComplete.lock();
				}
			}else {
				totalPrice.setVisibility(View.GONE);
				orderCompleteHandle.setBackgroundResource(R.drawable.handlerempty);
				orderCompleteHandle.setText(Constant.preOrder);
				orderCompleteHandle.setTextColor(context.getResources().getColor(R.color.white));
				orderCancel.setEnabled(false);
				orderConfirm.setEnabled(false);			
			}
		}else{
			orderCancel.setEnabled(false);
			orderConfirm.setEnabled(false);
			orderCompleteHandle.setText(Constant.orderLockOff);
			orderComplete.open();
			orderComplete.lock();
		}


		orderComplete.setOnDrawerCloseListener(new OnDrawerCloseListener(){

			@Override
			public void onDrawerClosed() {
				// TODO Auto-generated method stub
				Constant.refreshMenuAdapter(thisExpandableListView);
			}
		});

	}

	/*
	 * Adapter for the order list inside the slidingdrawer
	 */

	public OrderAdapter getOrderAdapter() {
		OrderAdapter thisAdapter = (OrderAdapter) orderReview.getAdapter(); 
		return thisAdapter;
	}

	/*
	 * This public method enables other activities and classes
	 * to call the orderHOlder as it is inside the expandmenuAdapter
	 */
	public OrderGetterSetter getOrderHolder(){
		return orderHolder;

	}

	/*
	 * This public method enables other activities and classes
	 * to call the clickHolder as it is inside the expandmenuAdapter
	 */
	public ClickHolder getClickHolder(){
		return clickStorage;
	}

	/*
	 * This public method enables other activities and classes
	 * to call the getOrderSent as it is inside the expandmenuAdapter
	 */
	public Boolean getOrderSent(){
		return orderHolder.isOrderSent();
	}
	
	/*
	 * Parses the menu list from ArbuckleMenu.xml on WWW folder of the CGI-Bin
	 */
	private class detailXMLTask extends AsyncTask<Void, Void, String> {

		private Context context;
		private detailXMLHandler detailTextHandler = new detailXMLHandler();
		private URL url;

		private detailXMLTask(Context context) {
			super();
			this.context = context;
		}

		@Override
		protected String doInBackground(Void... params) {

			try {
				url = new URL("http://www.stanford.edu/group/arbucklecafe/detaildefault.xml");
			} catch (MalformedURLException e) {
				e.printStackTrace();
				Log.i("Error", "With the URL");
			}

			try {
				SAXParserFactory saxPF = SAXParserFactory.newInstance();
				SAXParser saxP = saxPF.newSAXParser();
				XMLReader xmlR = saxP.getXMLReader();

				xmlR.setContentHandler(detailTextHandler);
				xmlR.parse(new InputSource(url.openStream()));

			} catch (Exception e) {
				//Log.i(e.getCause().toString(), url.toString());
			}
			return detailTextHandler.getDefaultDetail();
		}
	}
	
	private class detailXMLHandler extends DefaultHandler {

		String elementValue = null;
		Boolean elementOn = false;
		String defaultText = new String();
		
		public String getDefaultDetail(){
			return defaultText;
		}
				
		public void startElement (String uri, String localName, String qName, Attributes attributes) throws SAXException{
			elementOn = true;
		}

		public void endElement (String uri, String localName, String qName) throws SAXException{
			elementOn = false;
			if (localName.equals(XMLDetail)) defaultText = elementValue;
		}



		public void characters (char[] ch, int start, int length) throws SAXException{
			if (elementOn){
				elementValue = new String(ch, start, length);
				elementOn = false;
			}
		}
		
		private static final String XMLDetail = "detail";
	}
	
	public static final int VIEW_DETAILS = 0;
	public static final int VIEW_STANDARD = 1;
	public static final int VIEW_ORDER = 2;



}
