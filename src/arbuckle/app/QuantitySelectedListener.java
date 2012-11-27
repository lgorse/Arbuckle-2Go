package arbuckle.app;

import java.util.Iterator;
import java.util.Map.Entry;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.SlidingDrawer;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView;
import android.widget.Toast;
import arbuckle.app.ExpandMenuAdapter.ViewHolder;

public class QuantitySelectedListener implements OnItemSelectedListener{
	private OrderGetterSetter orderHolder;
	private ExpandMenuAdapter thisMenu;
	private ExpandableListView thisExpandableListView;
	private ClickHolder clickStorage;
	private int qGroupPosition;
	private int qChildPosition;
	private Context context;
	private TextView orderHandle;
	private SlidingDrawer orderDrawer;
	
/*
 * Listens for the number of items ordered by the user.
 */
	public QuantitySelectedListener(Context context, OrderGetterSetter orderHolder, ExpandableListView thisExpandableListView, 
			ExpandMenuAdapter thisMenu, ClickHolder clickStorage, int groupPosition, int childPosition, SlidingDrawer orderDrawer) {
		this.orderHolder= orderHolder;
		this.thisMenu = thisMenu;
		this.thisExpandableListView = thisExpandableListView;
		this.clickStorage = clickStorage;
		this.qGroupPosition = groupPosition;
		this.qChildPosition = childPosition;
		this.context = context;
		this.orderDrawer = orderDrawer;
		this.orderHandle = (TextView) orderDrawer.getHandle();

	}

	/* parent = spinner; view = textview; pos = position within Spinner, id =  */
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id){
		ViewHolder thisHolder;
		View thisViewFlipper = (View) view.getParent().getParent().getParent();
		View thisParentView = (View) thisViewFlipper.getParent();
		thisHolder = (ViewHolder) thisParentView.getTag();
		TextView thisText = (TextView) view;


		ExpandOptionsGroup thisGroup = (ExpandOptionsGroup) thisMenu.getGroup(qGroupPosition);
		String[] child = thisMenu.getChild(qGroupPosition, qChildPosition);

		String thisType = thisGroup.getType();
		String groupName = thisGroup.getName();
		String childName = child[Constant.ITEMNAME];

		String key = orderHolder.getOrderKey(thisType, groupName, childName);

		int oldPosition = clickStorage.getQuantity(key);

		orderHolder.handleItemOrder(child, pos, oldPosition);

		if (pos != 0){
			parent.setBackgroundResource(R.drawable.spinner_background_selected);
			thisText.setTextColor(context.getResources().getColor(R.color.red));
			thisViewFlipper.setClickable(false);
			thisHolder.itemName.setTextColor(context.getResources().getColor(R.color.red));
			if (!child[Constant.SPICE].equals("")){
				thisHolder.itemSpicy.setVisibility(View.VISIBLE);
			}else{
				thisHolder.itemSpicy.setVisibility(View.GONE);
			}				
			clickStorage.recordClick(qGroupPosition, qChildPosition, thisHolder.rowFlipper.getDisplayedChild(), key);	
		}else{
			parent.setBackgroundResource(R.drawable.spinner_background);
			thisText.setTextColor(Color.BLACK);
			thisHolder.itemName.setTextColor(context.getResources().getColor(R.color.Red_Translucent));
			thisHolder.itemSpicy.setChecked(false);
			thisHolder.itemSpicy.setVisibility(View.GONE);
			thisViewFlipper.setClickable(true);			
		}

		clickStorage.setQuantity(pos, key);

		if ((oldPosition != pos)){
			if (thisType.equals(Constant.a_la_carte))Toast.makeText(context, Constant.orderUpdated, Toast.LENGTH_SHORT).show();
			ComboToasts(thisType, oldPosition, pos);
			Constant.refreshMenuAdapter(thisExpandableListView);
		}
	}

	/*
	 * Toasts provide information to the user about his orders,
	 * based on the progress he is making inside a combo
	 */
	private void ComboToasts(String thisType, int oldPosition, int pos) {
		Boolean quantitySum = clickStorage.checkQuantitySum(qGroupPosition, thisType);
		int currentSum = clickStorage.getQuantitySum(qGroupPosition, thisType);
		int oldSum = currentSum+(oldPosition-pos);
		if (quantitySum){
			Toast.makeText(context, Constant.comboAdded, Toast.LENGTH_SHORT).show();
		}else {
			int max = clickStorage.getComboMax(thisType, qGroupPosition);
			if (max !=0){
				int remainder = max - currentSum;
				if ((thisType.equals(Constant.chef_Special))&&(currentSum==max)){
					switch (qGroupPosition){
					case Constant.chefSpecialNigiriPos:
						Toast.makeText(context, Constant.comboChefSpecial("handroll"), Toast.LENGTH_SHORT).show(); break;
					case Constant.chefSpecialHandPos:
						Toast.makeText(context, Constant.comboChefSpecial("nigiris"), Toast.LENGTH_SHORT).show(); break;
					}
				}
				if ((remainder > 0)&&(pos>oldPosition)){
					Toast.makeText(context, Constant.comboRemains(remainder), Toast.LENGTH_SHORT).show();
				}else if((oldSum==max)&&(currentSum<oldSum)){
					Toast.makeText(context, Constant.orderUpdated, Toast.LENGTH_SHORT).show();
				}
			}
		}		
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {

	}



}