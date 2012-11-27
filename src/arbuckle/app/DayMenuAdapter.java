package arbuckle.app;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class DayMenuAdapter extends BaseAdapter {
	
	private Context context;
	private LinkedHashMap<String, String[]> menuMap;
	private ArrayList<String> dayMenuList;
	
	public DayMenuAdapter (Context context, LinkedHashMap<String, String[]> menuMap, ArrayList<String> dayMenuList){
		this.context = context;
		this.menuMap = menuMap;
		this.dayMenuList = dayMenuList;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return dayMenuList.size();
	}

	@Override
	public String getItem(int position) {
		String key = dayMenuList.get(position);
		return key;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater dayMenuInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
		convertView = dayMenuInflater.inflate(R.layout.day_menu_row, null);
		String station = getItem(position);
		String[] value = menuMap.get(station);
		
		TextView tvStation = (TextView) convertView.findViewById(R.id.dayMenuStation);
		TextView tvDesc = (TextView) convertView.findViewById(R.id.dayMenuDesc);
		TextView tvPrice = (TextView) convertView.findViewById(R.id.dayMenuPrice);
		
		tvStation.setText(station);
		tvPrice.setText(value[Constant.menuPRICE]);
		String details = value[Constant.menuTITLE];
		tvDesc.setText(details);
		return convertView;
	}

}
