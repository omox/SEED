package dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import authentication.bean.User;
import net.sf.json.JSONObject;

public interface ItemInterface {

	public abstract HashMap<String, String> getMap();

	public abstract void setMap(HashMap<String, String> map);

	public abstract String getJson();

	public abstract void setJson(String json);

	public abstract ArrayList<List<String>> getWhere();

	public abstract void setWhere(ArrayList<List<String>> where);

	public abstract ArrayList<Integer> getMeta();

	public abstract void setMeta(ArrayList<Integer> meta);

	public abstract int getStart();

	public abstract void setStart(int start);

	public abstract int getLimit();

	public abstract void setLimit(int limit);

	public abstract ArrayList<String> getParamData();

	public abstract void setParamData(ArrayList<String> paramData);

	public abstract ArrayList<byte[]> getTable();

	public abstract void setTable(ArrayList<byte[]> table);

	public abstract boolean selectBy();

	public abstract boolean selectForDL();

	public abstract boolean selectBySQL(String Command);

	public abstract String getSelectCommand();

	public abstract String getReader(int columnNumber);

	public abstract JSONObject getOption();

	public abstract void setOption(JSONObject option);

	public abstract User getUserInfo();

	public abstract void setUserInfo(User userInfo);

	public abstract String getMessage();

	public abstract void setMessage(String message);
}