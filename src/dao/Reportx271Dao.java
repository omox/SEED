package dao;

import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import authentication.bean.User;
import authentication.defines.Consts;
import common.DefineReport;
import common.ItemList;
import common.MessageUtility;
import common.MessageUtility.Msg;
import common.MessageUtility.MsgKey;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 */
public class Reportx271Dao extends ItemDao {

	/** SQLリスト保持用変数 */
	ArrayList<String> sqlList = new ArrayList<String>();
	/** SQLのパラメータリスト保持用変数 */
	ArrayList<ArrayList<String>> prmList = new ArrayList<ArrayList<String>>();
	/** SQLログ用のラベルリスト保持用変数 */
	ArrayList<String> lblList = new ArrayList<String>();

	String copyTenCd = "23";

	/**
	 * インスタンスを生成します。
	 * @param source
	 */
	public Reportx271Dao(String JNDIname) {
		super(JNDIname);
	}

	public JSONObject update(HttpServletRequest request,HttpSession session,  HashMap<String, String> map, User userInfo) {
		String sysdate = (String)request.getSession().getAttribute(Consts.STR_SES_LOGINDT);

		JSONObject option = new JSONObject();

		JSONArray msgList = this.check(map, userInfo, sysdate);

		if(msgList.size() > 0){
			option.put(MsgKey.E.getKey(), msgList);
			return option;
		}

		// 更新処理
		try {
			option = this.updateData(map, userInfo, sysdate);
		} catch (Exception e) {
			e.printStackTrace();
			option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
		}
		return option;
	}

	/**
	 * チェック処理
	 *
	 * @throws Exception
	 */
	public JSONArray check(HashMap<String, String> map, User userInfo, String sysdate) {

		// パラメータ確認
		JSONArray	dataArray	= JSONArray.fromObject(map.get("DATA"));	// 更新情報
		JSONArray		msg		= new JSONArray();
		MessageUtility	mu		= new MessageUtility();

		// DB検索用パラメータ
		String sqlWhere	= "";
		ArrayList<String> paramData = new ArrayList<String>();

		// 格納用変数
		StringBuffer	sbSQL	= new StringBuffer();
		ItemList		iL		= new ItemList();
		JSONArray		dbDatas	= new JSONArray();

		// チェック処理
		// 対象件数チェック
		if(dataArray.size() == 0 || dataArray.getJSONObject(0).isEmpty()){
			msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
			return msg;
		}

		// 入力データのチェック
		JSONObject	data	= dataArray.getJSONObject(0);
		String		de_maxsu	= data.optString("F1");		// デフォルト_登録限度数
		String		maxsu	= data.optString("F2");			// 登録限度数

		return msg;
	}

	/**
	 * 更新処理実行
	 * @return
	 *
	 * @throws Exception
	 */
	private JSONObject updateData(HashMap<String, String> map, User userInfo, String sysdate) throws Exception {
		// パラメータ確認
		JSONObject	option		= new JSONObject();
		JSONArray	dataArray	= JSONArray.fromObject(map.get("DATA"));	// 対象情報（主要な更新情報）

		// パラメータ確認
		// 必須チェック
		if ( dataArray.isEmpty() || dataArray.size() != 1 || dataArray.getJSONObject(0).isEmpty()) {
			option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
			return option;
		}

		// 基本登録情報
		JSONObject data = dataArray.getJSONObject(0);

		// 正規定量_店別数量insert処理
		createSqlSg(data,map,userInfo);

		ArrayList<Integer> countList  = new ArrayList<Integer>();
		if(sqlList.size() > 0){
			countList =  super.executeSQLs(sqlList, prmList);
		}

		if(StringUtils.isEmpty(getMessage())){
			int count = 0;
			for (int i = 0; i < countList.size(); i++) {
				count += countList.get(i);
				if (DefineReport.ID_DEBUG_MODE)	System.out.println(MessageUtility.getMessage(Msg.S00006.getVal(), new String[]{lblList.get(i), Integer.toString(countList.get(i))}));
			}
			if(count==0){
				option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E10000.getVal()));
			}else{
				option.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00006.getVal(), "データ", String.valueOf(count)));
			}
		}else{
			option.put(MsgKey.E.getKey(), getMessage());
		}
		return option;
	}

	/**
	 * 商品登録限度数管理テーブルINSERT/UPDATE処理(実質UPDATEのみ)
	 *
	 * @param data
	 * @param map
	 * @param userInfo
	 */
	public String createSqlSg(JSONObject data, HashMap<String, String> map, User userInfo){

		StringBuffer	sbSQL		= new StringBuffer();

		ArrayList<String> prmData = new ArrayList<String>();
		Object[] valueData = new Object[]{};
		String values = "";

		int maxField = 4;		// Fxxの最大値
		for (int k = 0; k < maxField; k++) {
			String key = "F" + String.valueOf(k);

			if(k == 0){
				values += "0";
			}else if(ArrayUtils.contains(new String[]{"F1", "F2"}, key)){
				String val = data.optString(key);
				values += ", "+val;
			}else{
				values += ", null";
			}
		}

		valueData = ArrayUtils.add(valueData, "("+values+")");

		// 商品登録限度数管理テーブルの登録・更新
		sbSQL.append("UPDATE INAAD.SYSSHNGENDOSU T1 ");
		sbSQL.append(", ( ");
		sbSQL.append("SELECT ");
		sbSQL.append("  ID"); // ID：
        sbSQL.append(", DE_MAXSU"); // デフォルト_登録限度数：
        sbSQL.append(", MAXSU"); // 登録限度数：
        sbSQL.append(", CURRENT_TIMESTAMP AS UPDDT ");
		sbSQL.append(" from (values ROW"+StringUtils.join(valueData, ",")+") as T3( ");
		sbSQL.append("  ID");
        sbSQL.append(", DE_MAXSU"); // F1   : デフォルト_登録限度数
        sbSQL.append(", MAXSU"); // F2   : 登録限度数
        sbSQL.append(", UPDDT");
        sbSQL.append(" )) AS RE  ");
		sbSQL.append("SET ");
		sbSQL.append("  T1.ID = RE.ID ");
        sbSQL.append(", T1.DE_MAXSU = RE.DE_MAXSU ");
        sbSQL.append(", T1.MAXSU = RE.MAXSU ");
        sbSQL.append(", T1.UPDDT = RE.UPDDT ");
		sbSQL.append("WHERE ");
		sbSQL.append(" T1.ID = 0 ");

		if (DefineReport.ID_DEBUG_MODE)	System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("商品登録限度数管理マスタ");

		return sbSQL.toString();
	}
}
