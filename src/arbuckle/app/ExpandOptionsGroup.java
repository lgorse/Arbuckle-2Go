package arbuckle.app;

import java.util.ArrayList;

/*
 * This class sets the values for every group in the menu, including details, etc.
 * IT is a getter setter class
 */
public class ExpandOptionsGroup {

	private String name;
	private String price;
	private String detail;
	private String type;
	private ArrayList<String[]> children;

	public String getName(){
		return name;
	}

	public void setName(String passedName) {
		// TODO Auto-generated method stub
		this.name = passedName;
	}

	public String getPrice(){
		return price;
	}

	public void setPrice(String passedPrice){
		this.price = passedPrice;
	}

	public String getDetail(){
		return detail;
	}

	public void setDetail(String passedDetail){
		this.detail = passedDetail;
	}

	public ArrayList<String[]> getChildren(){
		return children;
	}

	public void setChildren(ArrayList<String[]> itemChildList){
		this.children = itemChildList;
	}

	public void setType(String passedType) {
		this.type = passedType;
	}

	public String getType(){
		return type;
	}

	public int getTypeNum(){
		int numType;
		if (type.equals(Constant.a_la_carte)){
			numType = ALACARTE;
		}else if (type.equals(Constant.specials)){
			numType = SPECIALS;
		}else if (type.equals(Constant.chef_Special)){
			numType  = CHEFSPECIAL;
		}else{
			numType = -1;
		}
		return numType;
	}

	public final static int ALACARTE = 0;
	public final static int SPECIALS = 1;
	public final static int CHEFSPECIAL = 2;

	public ArrayList<ExpandOptionsGroup> setupGroupList(
			ArrayList<String[]> groupList, ArrayList<String[]> itemList, String type) {
		ArrayList<ExpandOptionsGroup> menuList = new ArrayList<ExpandOptionsGroup>();
		ArrayList<String[]> itemCopy = itemList;
		for (int i = 0; i<groupList.size(); i++){
			ExpandOptionsGroup group = new ExpandOptionsGroup();
			String[] thisGroup = groupList.get(i);
			if (thisGroup[Constant.TYPE].equals(type)){
				group.setName(thisGroup[Constant.GROUPNAME]);
				group.setType(thisGroup[Constant.TYPE]);
				group.setDetail(thisGroup[Constant.DETAIL]);
				group.setPrice(thisGroup[Constant.PRICE]);

				ArrayList<ArrayList<String[]>> newChildLists = setChildList(group, itemCopy);
				itemCopy = newChildLists.get(0);
				group.setChildren(newChildLists.get(1));
				menuList.add(group);
			}
		}
		return menuList;
	}

	private ArrayList<ArrayList<String[]>> setChildList(
			ExpandOptionsGroup group, ArrayList<String[]> itemCopy) {
		ArrayList<ArrayList<String[]>> newChildList = new ArrayList<ArrayList<String[]>>();
		ArrayList<String[]> childrenList = new ArrayList<String[]>();
		for (int i = 0; i<itemCopy.size(); i++){
			String[] thisChild = itemCopy.get(i);
			if (thisChild[Constant.GROUPNAME].equals(group.getName())&& thisChild[Constant.TYPE].equals(group.getType())){
				childrenList.add(thisChild);
				itemCopy.remove(i);
				i--;
			}
		}
		newChildList.add(0, itemCopy);
		newChildList.add(1, childrenList);
		return newChildList;
	}

	


}

