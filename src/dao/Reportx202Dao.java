package dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import authentication.bean.User;
import authentication.defines.Consts;
import common.DefineReport;
import common.Defines;
import common.InputChecker;
import common.ItemList;
import common.JsonArrayData;
import common.MessageUtility;
import common.MessageUtility.FieldType;
import common.MessageUtility.Msg;
import common.MessageUtility.MsgKey;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 */
public class Reportx202Dao extends ItemDao {

	/** SQLリスト保持用変数 */
	ArrayList<String> sqlList = new ArrayList<String>();
	/** SQLのパラメータリスト保持用変数 */
	ArrayList<ArrayList<String>> prmList = new ArrayList<ArrayList<String>>();
	/** SQLログ用のラベルリスト保持用変数 */
	ArrayList<String> lblList = new ArrayList<String>();

	/**
	 * インスタンスを生成します。
	 * @param source
	 */
	public Reportx202Dao(String JNDIname) {
		super(JNDIname);
	}

	/**
	 * 検索実行
	 *
	 * @return
	 */
	public boolean selectBy() {

		// 検索コマンド生成
		String command = createCommand();

		// 出力用検索条件生成
		outputQueryList();

		// 検索実行
		return super.selectBySQL(command);
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

	// 削除処理
	public JSONObject delete(HttpServletRequest request,HttpSession session,  HashMap<String, String> map, User userInfo) {
		String sysdate = (String)request.getSession().getAttribute(Consts.STR_SES_LOGINDT);

		JSONObject option = new JSONObject();
		JSONArray msgList = this.checkDel(map, userInfo, sysdate);

		if(msgList.size() > 0){
			option.put(MsgKey.E.getKey(), msgList);
			return option;
		}

		// 更新処理
		try {
			option = this.deleteData(map, userInfo, sysdate);
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
		JSONArray	dataArray	= JSONArray.fromObject(map.get("DATA"));		// 更新情報(配送グループ)
		JSONArray	dataArrayT	= JSONArray.fromObject(map.get("DATA_HSTGP"));	// 更新情報(配送店グループ)
		String		area		= map.get("AREA");								// エリア区分
		String		sendBtnid	= map.get("SENDBTNID");							// 呼出しボタン

		// 格納用変数
		StringBuffer		sbSQL	= new StringBuffer();
		JSONArray			msg		= new JSONArray();
		ItemList			iL		= new ItemList();
		MessageUtility		mu		= new MessageUtility();
		JSONArray			dbDatas = new JSONArray();
		ArrayList<JSONObject>	tengps	= new ArrayList<JSONObject>();
		HashSet<String>			tengps_	= new HashSet<String>();

		// チェック処理
		// 対象件数チェック
		if(dataArray.size() == 0 || dataArray.getJSONObject(0).isEmpty()){
			msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
			return msg;
		}

		if(dataArrayT.size() == 0 || dataArrayT.getJSONObject(0).isEmpty()){
			msg.add(mu.getDbMessageObj("EX1047", new String[]{"店グループ"}));
			return msg;
		}

		// 入力データのチェック
		JSONObject	data	= dataArray.getJSONObject(0);
		String		hsgpCd	= data.optString("F1");			// 配送グループコード

		if (!StringUtils.isEmpty(data.optString("F3"))) {

			DefineReport.InpText inpsetting = DefineReport.InpText.valueOf("HSGPAN");

			// ①データ型による文字種チェック
			if (!InputChecker.checkDataType(inpsetting.getType(), data.optString("F3"))) {
				JSONObject o = mu.getDbMessageObjDataTypeErr(inpsetting.getType(), new String[] {});
				msg.add(o);
				return msg;
			}
		}

		// エリア区分に選択がなかった場合エラー
		if (area == null) {
			msg.add(mu.getDbMessageObj("E20030", new String[]{"エリア区分"}));
			return msg;
		}

		// DB検索用パラメータ
		String sqlWhere = "";
		ArrayList<String> paramData = new ArrayList<String>();

		if(StringUtils.isEmpty(hsgpCd)){
			sqlWhere += "null";
		}else{
			sqlWhere += "?";
			paramData.add(hsgpCd);
		}

		if(DefineReport.Button.NEW.getObj().equals(sendBtnid)) {
			sbSQL.append("SELECT ");
			sbSQL.append("HSGPCD ");	// レコード件数
			sbSQL.append("FROM ");
			sbSQL.append("INAMS.MSTHSGP ");
			sbSQL.append("WHERE ");
			sbSQL.append("HSGPCD = " + sqlWhere +" AND ");						// 入力された配送グループで検索
			sbSQL.append("UPDKBN = " + DefineReport.ValUpdkbn.NML.getVal());	// 有効なレコードが対象

			dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

			if (dbDatas.size() >= 1){
				// 既に存在している配送グループ
				msg.add(mu.getDbMessageObj("EX1015", new String[]{}));
				return msg;
			}
		}

		// 入力値の重複チェック
		for (int i=0; i<dataArrayT.size(); i++){
			String val = dataArrayT.optJSONObject(i).optString("F1");
			if(StringUtils.isNotEmpty(val)){
				tengps.add(dataArrayT.optJSONObject(i));
				tengps_.add(val);
			}
		}

		if(tengps.size() != tengps_.size()){
			msg.add(MessageUtility.getDbMessageIdObj("E11040", new String[]{"店グループ"}));
			return msg;
		}

		// 配送グループマスタ存在チェック(新規のみ)
		for (int i = 0; i < dataArrayT.size(); i++) {
			JSONObject dataT = dataArrayT.getJSONObject(i);
			if(dataT.isEmpty()){
				continue;
			}

			if (!StringUtils.isEmpty(dataT.optString("F3"))) {

				DefineReport.InpText inpsetting = DefineReport.InpText.valueOf("HSGPAN");

				// ①データ型による文字種チェック
				if (!InputChecker.checkDataType(inpsetting.getType(), dataT.optString("F3"))) {
					JSONObject o = mu.getDbMessageObjDataTypeErr(inpsetting.getType(), new String[] {});
					msg.add(o);
					return msg;
				}
			}

			// 配送店グループマスタ存在チェック(新規のみ)
			if(DefineReport.Button.NEW.getObj().equals(sendBtnid)) {
				// 変数を初期化
				sbSQL	= new StringBuffer();
				iL		= new ItemList();
				dbDatas = new JSONArray();
				sqlWhere	= "";
				paramData	= new ArrayList<String>();

				if(StringUtils.isEmpty(hsgpCd)){
					sqlWhere += "HSGPCD=null AND ";
				}else{
					sqlWhere += "HSGPCD=? AND ";
					paramData.add(hsgpCd);
				}

				if (StringUtils.isEmpty(dataT.optString("F1"))) {
					sqlWhere += "TENGPCD=null AND ";
				} else {
					sqlWhere += "TENGPCD=? AND ";
					paramData.add(dataT.optString("F1"));
				}

				sbSQL.append("SELECT ");
				sbSQL.append("HSGPCD ");	// レコード件数
				sbSQL.append("FROM ");
				sbSQL.append("INAMS.MSTHSTENGP ");
				sbSQL.append("WHERE ");
				sbSQL.append(sqlWhere);
				sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal());	// 有効なレコードが対象

				dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

				if (dbDatas.size() >= 1){
					// 既に存在している配送店グループ
					msg.add(mu.getDbMessageObj("EX1016", new String[]{}));
					return msg;
				}
			}

			// 店舗部門マスタチェック(エリア選択時のみ)
			if (area.equals("0")) {
				sbSQL	= new StringBuffer();
				iL		= new ItemList();
				dbDatas = new JSONArray();
				sqlWhere	= "";
				paramData	= new ArrayList<String>();

				if (StringUtils.isEmpty(dataT.optString("F1"))) {
					sqlWhere += "null";
				} else {
					sqlWhere += "?";
					paramData.add(dataT.optString("F1"));
				}

				sbSQL.append("SELECT ");
				sbSQL.append("AREACD ");
				sbSQL.append("FROM ");
				sbSQL.append("INAMS.MSTTENBMN ");
				sbSQL.append("WHERE ");
				sbSQL.append("AREACD=" + sqlWhere);

				dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

				if (dbDatas.size() == 0){
					// 店舗部門マスタに存在しないエリア
					msg.add(mu.getDbMessageObj("E11030", new String[]{}));
					return msg;
				}

			// 店グループ選択時のみ
			} else {
				if (dataT.optInt("F1") < 10) {
					// 10番以上を入力
					msg.add(mu.getDbMessageObj("E30012", new String[]{"店グループは10番以上"}));
					return msg;
				}
			}
		}

		return msg;
	}

	/**
	 * チェック処理
	 *
	 * @throws Exception
	 */
	public JSONArray checkDel(HashMap<String, String> map, User userInfo, String sysdate) {

		// 削除データ検索用コード
		JSONArray	dataArray	= JSONArray.fromObject(map.get("DATA"));	// 対象情報（主要な更新情報）
		String		hstengpcd	= map.get("HSTENGPCD");						// 配送店グループコード

		// 格納用変数
		JSONArray msg = new JSONArray();

		// チェック処理
		// 対象件数チェック
		if(dataArray.size() == 0 || dataArray.getJSONObject(0).isEmpty()){
			msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
			return msg;
		}

		if(hstengpcd.isEmpty() || hstengpcd == null){
			msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
			return msg;
		}

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

		// 排他チェック用
		String				targetTable	= null;
		String				targetWhere	= null;
		ArrayList<String>	targetParam	= new ArrayList<String>();
		JSONArray			msg			= new JSONArray();

		// パラメータ確認
		// 必須チェック
		if ( dataArray.isEmpty() || dataArray.size() != 1 || dataArray.getJSONObject(0).isEmpty()) {
			option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
			return option;
		}

		// 基本登録情報
		JSONObject data = dataArray.getJSONObject(0);

		// 配送グループマスタ、配送店グループマスタINSERT/UPDATE処理
		createSqlHs(data,map,userInfo);

		// 排他チェック実行
		targetTable = "INAMS.MSTHSGP";
		targetWhere = " HSGPCD=? AND UPDKBN=?";
		targetParam.add(data.optString("F1"));
		targetParam.add(DefineReport.ValUpdkbn.NML.getVal());

		if(!super.checkExclusion(targetTable, targetWhere, targetParam, data.optString("F8"))){
 			msg.add(MessageUtility.getDbMessageExclusion(FieldType.DEFAULT, new String[]{}));
 			option.put(MsgKey.E.getKey(), msg);
			return option;
		}

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
				option.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00001.getVal()));
			}
		}else{
			option.put(MsgKey.E.getKey(), getMessage());
		}
		return option;
	}

	/**
	 * 削除処理実行
	 * @return
	 *
	 * @throws Exception
	 */
	private JSONObject deleteData(HashMap<String, String> map, User userInfo, String sysdate) throws Exception {
		// パラメータ確認
		JSONObject	option		= new JSONObject();
		JSONArray	dataArray	= JSONArray.fromObject(map.get("DATA"));	// 対象情報（主要な更新情報）

		// 排他チェック用
		String				targetTable	= null;
		String				targetWhere	= null;
		ArrayList<String>	targetParam	= new ArrayList<String>();
		JSONArray			msg			= new JSONArray();

		// パラメータ確認
		// 必須チェック
		if ( dataArray.isEmpty() || dataArray.size() != 1 || dataArray.getJSONObject(0).isEmpty()) {
			option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
			return option;
		}

		// 基本登録情報
		JSONObject data = dataArray.getJSONObject(0);

		// 配送グループマスタ、配送店グループマスタINSERT/UPDATE処理
		createDelSqlHs(data,map,userInfo);

		// 排他チェック実行
		targetTable = "INAMS.MSTHSGP";
		targetWhere = " HSGPCD=? AND UPDKBN=?";
		targetParam.add(data.optString("F1"));
		targetParam.add(DefineReport.ValUpdkbn.NML.getVal());

		if(!super.checkExclusion(targetTable, targetWhere, targetParam, data.optString("F8"))){
 			msg.add(MessageUtility.getDbMessageExclusion(FieldType.DEFAULT, new String[]{}));
 			option.put(MsgKey.E.getKey(), msg);
			return option;
		}

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
				option.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00001.getVal()));
			}
		}else{
			option.put(MsgKey.E.getKey(), getMessage());
		}
		return option;
	}
	/**
	 * 配送グループマスタ、配送店グループマスタDELETE(倫理削除)処理
	 *
	 * @param data
	 * @param map
	 * @param userInfo
	 */
	public String createDelSqlHs(JSONObject data, HashMap<String, String> map, User userInfo){

		// 削除データ検索用コード
		String	hstengpcd	= map.get("HSTENGPCD");	// 配送店グループコード

		// 格納用変数
		StringBuffer	sbSQL	= new StringBuffer();
		ItemList		iL		= new ItemList();
		JSONArray		dbDatas = new JSONArray();

		// ログインユーザー情報取得
		String userId	= userInfo.getId(); // ログインユーザー

		// DB検索用パラメータ
		String sqlWhere = "";
		ArrayList<String> paramData = new ArrayList<String>();

		if(StringUtils.isEmpty(data.optString("F1"))){
			sqlWhere += "null";
		}else{
			sqlWhere += "?";
			paramData.add(data.optString("F1"));
		}

		// 配送店グループマスタの件数チェック
		sbSQL = new StringBuffer();
		sbSQL.append("SELECT ");
		sbSQL.append(" HSGPCD ");
		sbSQL.append("FROM ");
		sbSQL.append("INAMS.MSTHSTENGP ");
		sbSQL.append("WHERE ");
		sbSQL.append("HSGPCD=" + sqlWhere + " AND ");
		sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal());

		dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

		sbSQL = new StringBuffer();
		sbSQL.append("UPDATE ");
		sbSQL.append("INAMS.MSTHSGP ");
		sbSQL.append("SET ");
		// 1件のみの場合は対象の配送グループを論理削除
		if (dbDatas.size() == 1){
			sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.DEL.getVal() + ",");
		}
		sbSQL.append("OPERATOR='" + userId + "'");
		sbSQL.append(",UPDDT=current timestamp ");
		sbSQL.append("WHERE ");
		sbSQL.append("HSGPCD = " + sqlWhere);

		if (DefineReport.ID_DEBUG_MODE)	System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(paramData);
		lblList.add("配送グループマスタ");

		sqlWhere	= "";
		paramData	= new ArrayList<String>();

		if(StringUtils.isEmpty(data.optString("F1"))){
			sqlWhere += "HSGPCD=null AND ";
		}else{
			sqlWhere += "HSGPCD=? AND ";
			paramData.add(data.optString("F1"));
		}

		if(StringUtils.isEmpty(hstengpcd)){
			sqlWhere += "TENGPCD=null";
		}else{
			sqlWhere += "TENGPCD=?";
			paramData.add(hstengpcd);
		}

		sbSQL = new StringBuffer();
		sbSQL.append("UPDATE ");
		sbSQL.append("INAMS.MSTHSTENGP ");
		sbSQL.append("SET ");
		sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.DEL.getVal());
		sbSQL.append(",OPERATOR='" + userId + "'");
		sbSQL.append(",UPDDT=current timestamp ");
		sbSQL.append("WHERE ");
		sbSQL.append(sqlWhere);

		if (DefineReport.ID_DEBUG_MODE)	System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(paramData);
		lblList.add("配送店グループマスタ");

		sbSQL = new StringBuffer();
		sbSQL.append("UPDATE ");
		sbSQL.append("INAMS.MSTHSGPTEN ");
		sbSQL.append("SET ");
		sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.DEL.getVal());
		sbSQL.append(",OPERATOR='" + userId + "'");
		sbSQL.append(",UPDDT=current timestamp ");
		sbSQL.append("WHERE ");
		sbSQL.append(sqlWhere);

		if (DefineReport.ID_DEBUG_MODE)	System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(paramData);
		lblList.add("配送グループ店マスタ");

		return sbSQL.toString();
	}

	/**
	 * 配送グループマスタ、配送店グループマスタINSERT/UPDATE処理
	 *
	 * @param data
	 * @param map
	 * @param userInfo
	 */
	public String createSqlHs(JSONObject data, HashMap<String, String> map, User userInfo){

		StringBuffer	sbSQL		= new StringBuffer();
		JSONArray		dataArrayT	= JSONArray.fromObject(map.get("DATA_HSTGP"));	// 更新情報(配送店グループ)

		// ログインユーザー情報取得
		String userId	= userInfo.getId();							// ログインユーザー

		ArrayList<String> prmData = new ArrayList<String>();
		Object[] valueData = new Object[]{};
		String values = "";

		int maxField = 8;		// Fxxの最大値
		for (int k = 1; k <= maxField; k++) {
			String key = "F" + String.valueOf(k);

			if(k == 1){
				values += String.valueOf(0 + 1);
			}

			if(! ArrayUtils.contains(new String[]{"F4", "F5", "F6", "F8"}, key)){
				String val = data.optString(key);
				if(StringUtils.isEmpty(val)){
					values += ", null";
				}else{
					values += ", ?";
					prmData.add(val);
				}
			}

			if(k == maxField){
				valueData = ArrayUtils.add(valueData, "("+values+")");
				values = "";
			}
		}

		// 配送グループマスタの登録・更新
		sbSQL.append("MERGE INTO INAMS.MSTHSGP AS T USING (SELECT ");
		sbSQL.append("  HSGPCD");													// 配送グループ：
		sbSQL.append(", HSGPAN");													// 配送グループ名称（カナ）：
		sbSQL.append(", HSGPKN");													// 配送グループ名称（漢字）：
		sbSQL.append(", AREAKBN");													// エリア区分：
		sbSQL.append(", "+DefineReport.ValUpdkbn.NML.getVal()+" AS UPDKBN");		// 更新区分：
		sbSQL.append(", "+DefineReport.Values.SENDFLG_UN.getVal()+" AS SENDFLG");// 送信区分：
		sbSQL.append(", '"+userId+"' AS OPERATOR ");								// オペレーター：
		sbSQL.append(", current timestamp AS ADDDT ");								// 登録日：
		sbSQL.append(", current timestamp AS UPDDT ");								// 更新日：
		sbSQL.append(" FROM (values "+StringUtils.join(valueData, ",")+") as T1(NUM");
		sbSQL.append(",HSGPCD ");													// F1	: 配送グループ
		sbSQL.append(",HSGPKN ");													// F2	: 配送グループ名称
		sbSQL.append(",HSGPAN ");													// F3	: 配送グループ名称（カナ）
		sbSQL.append(",AREAKBN ");													// F7	: エリア区分
		sbSQL.append(")) as RE on (");
		sbSQL.append("T.HSGPCD = RE.HSGPCD ");
		sbSQL.append(") WHEN MATCHED THEN UPDATE SET ");
		sbSQL.append("HSGPCD=RE.HSGPCD");
		sbSQL.append(",HSGPKN=RE.HSGPKN");
		sbSQL.append(",HSGPAN=RE.HSGPAN");
		sbSQL.append(",AREAKBN=RE.AREAKBN");
		sbSQL.append(",UPDKBN=RE.UPDKBN");
		sbSQL.append(",SENDFLG=RE.SENDFLG ");
		sbSQL.append(",OPERATOR=RE.OPERATOR");
		sbSQL.append(",ADDDT= CASE WHEN NVL(UPDKBN,0)=1 THEN RE.ADDDT ELSE ADDDT END ");
		sbSQL.append(",UPDDT=RE.UPDDT ");
		sbSQL.append("WHEN NOT MATCHED THEN INSERT(");
		sbSQL.append("HSGPCD");
		sbSQL.append(",HSGPAN");
		sbSQL.append(",HSGPKN");
		sbSQL.append(",AREAKBN");
		sbSQL.append(",UPDKBN");
		sbSQL.append(",SENDFLG");
		sbSQL.append(",OPERATOR");
		sbSQL.append(",ADDDT");
		sbSQL.append(",UPDDT");
		sbSQL.append(") VALUES (");
		sbSQL.append("RE.HSGPCD");
		sbSQL.append(",RE.HSGPAN");
		sbSQL.append(",RE.HSGPKN");
		sbSQL.append(",RE.AREAKBN");
		sbSQL.append(",RE.UPDKBN");
		sbSQL.append(",RE.SENDFLG");
		sbSQL.append(",RE.OPERATOR");
		sbSQL.append(",RE.ADDDT");
		sbSQL.append(",RE.UPDDT");
		sbSQL.append(")");

		if (DefineReport.ID_DEBUG_MODE)	System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("配送グループマスタ");

		// クリア
		prmData = new ArrayList<String>();
		valueData = new Object[]{};
		values = "";

		maxField = 3;		// Fxxの最大値
		int len = dataArrayT.size();
		for (int i = 0; i < len; i++) {
			JSONObject dataT = dataArrayT.getJSONObject(i);
			if(! dataT.isEmpty()){
				for (int k = 1; k <= maxField; k++) {
					String key = "F" + String.valueOf(k);

					if(k == 1){
						values += String.valueOf(i + 1);
						// 配送グループコードを追加
						values += ", ?";
						prmData.add(data.optString(key));
					}

					if(! ArrayUtils.contains(new String[]{""}, key)){
						String val = dataT.optString(key);
						if(StringUtils.isEmpty(val)){
							values += ", null";
						}else{
							values += ", ?";
							prmData.add(val);
						}
					}

					if(k == maxField){
						valueData = ArrayUtils.add(valueData, "("+values+")");
						values = "";
					}
				}
			}

			if(valueData.length >= 100 || (i+1 == len && valueData.length > 0)){
				sbSQL = new StringBuffer();
				sbSQL.append("MERGE INTO INAMS.MSTHSTENGP AS T USING (SELECT ");
				sbSQL.append("HSGPCD");														// 配送グループ：
				sbSQL.append(",TENGPCD");													// 配送店グループ：
				sbSQL.append(",TENGPAN");													// 配送店グループ名称（カナ）：
				sbSQL.append(",TENGPKN");													// 配送店グループ名称（漢字）：
				sbSQL.append(", "+DefineReport.ValUpdkbn.NML.getVal()+" AS UPDKBN");		// 更新区分：
				sbSQL.append(", "+DefineReport.Values.SENDFLG_UN.getVal()+" AS SENDFLG");	// 送信区分：
				sbSQL.append(", '"+userId+"' AS OPERATOR ");								// オペレーター：
				sbSQL.append(", current timestamp AS ADDDT ");								// 登録日：
				sbSQL.append(", current timestamp AS UPDDT ");								// 更新日：
				sbSQL.append(" FROM (values "+StringUtils.join(valueData, ",")+") as T1(NUM");
				sbSQL.append(",HSGPCD");													// 特殊	: 配送グループ：
				sbSQL.append(",TENGPCD");													// F1	: 配送店グループ：
				sbSQL.append(",TENGPKN");													// F2	: 配送店グループ名称（漢字）：
				sbSQL.append(",TENGPAN");													// F3	: 配送店グループ名称（カナ）：
				sbSQL.append(")) as RE on (");
				sbSQL.append("T.HSGPCD=RE.HSGPCD AND ");
				sbSQL.append("T.TENGPCD=RE.TENGPCD ");
				sbSQL.append(") WHEN MATCHED THEN UPDATE SET ");
				sbSQL.append("TENGPCD=RE.TENGPCD ");
				sbSQL.append(",TENGPKN=RE.TENGPKN ");
				sbSQL.append(",TENGPAN=RE.TENGPAN ");
				sbSQL.append(",UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal() + " ");
				sbSQL.append(",SENDFLG=RE.SENDFLG ");
				sbSQL.append(",OPERATOR=RE.OPERATOR ");
				sbSQL.append(",ADDDT= CASE WHEN NVL(UPDKBN,0)=1 THEN RE.ADDDT ELSE ADDDT END ");
				sbSQL.append(",UPDDT=RE.UPDDT");
				sbSQL.append(" WHEN NOT MATCHED THEN INSERT (");
				sbSQL.append("HSGPCD ");
				sbSQL.append(",TENGPCD");
				sbSQL.append(",TENGPAN");
				sbSQL.append(",TENGPKN");
				sbSQL.append(",UPDKBN");
				sbSQL.append(",SENDFLG");
				sbSQL.append(",OPERATOR");
				sbSQL.append(",ADDDT");
				sbSQL.append(",UPDDT");
				sbSQL.append(") VALUES (");
				sbSQL.append("RE.HSGPCD ");
				sbSQL.append(",RE.TENGPCD");
				sbSQL.append(",RE.TENGPAN");
				sbSQL.append(",RE.TENGPKN");
				sbSQL.append(",RE.UPDKBN");
				sbSQL.append(",RE.SENDFLG");
				sbSQL.append(",RE.OPERATOR");
				sbSQL.append(",RE.ADDDT");
				sbSQL.append(",RE.UPDDT");
				sbSQL.append(")");

				if (DefineReport.ID_DEBUG_MODE)	System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

				sqlList.add(sbSQL.toString());
				prmList.add(prmData);
				lblList.add("配送店グループマスタ");

				// クリア
				prmData = new ArrayList<String>();
				valueData = new Object[]{};
				values = "";
			}
		}

		return sbSQL.toString();
	}

	private String createCommand() {
		// ログインユーザー情報取得
		User userInfo = getUserInfo();
		if(userInfo==null){
			return "";
		}

		String szHsgpcd = getMap().get("HSGPCD"); // 配送グループコード
		// DB検索用パラメータ
		ArrayList<String> paramData = new ArrayList<String>();
		String sqlWhere = "";

		if(StringUtils.isEmpty(szHsgpcd)){
			sqlWhere += "null";
		}else{
			sqlWhere += "?";
			paramData.add(szHsgpcd);
		}

		// タイトル情報(任意)設定
		List<String> titleList = new ArrayList<String>();

		// 一覧表情報
		StringBuffer sbSQL = new StringBuffer();
		sbSQL.append("SELECT ");
		sbSQL.append("HSGPCD ");													// F1	: 配送グループ
		sbSQL.append(",HSGPKN ");													// F2	: 配送グループ名称
		sbSQL.append(",HSGPAN ");													// F3	: 配送グループ名称（カナ）
		sbSQL.append(",OPERATOR ");													// F4	: オペレーター
		sbSQL.append(",TO_CHAR(ADDDT,'YY/MM/DD') AS ADDDT ");						// F5	: 登録日
		sbSQL.append(",TO_CHAR(UPDDT,'YY/MM/DD') AS UPDDT ");						// F6	: 更新日
		sbSQL.append(",AREAKBN ");													// F7	: エリア区分
		sbSQL.append(",TO_CHAR(UPDDT,'YYYYMMDDHH24MISSNNNNNN') as HDN_UPDDT ");		// F8	: 更新日時
		sbSQL.append("FROM ");
		sbSQL.append("INAMS.MSTHSGP ");
		sbSQL.append("WHERE ");
		sbSQL.append("HSGPCD = " + sqlWhere +" AND ");
		sbSQL.append("UPDKBN = " + DefineReport.ValUpdkbn.NML.getVal() + " ");
		sbSQL.append("ORDER BY HSGPCD");

		// オプション情報（タイトル）設定
		JSONObject option = new JSONObject();
		option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
		setOption(option);

		// DB検索用パラメータ設定
		setParamData(paramData);

		if (DefineReport.ID_DEBUG_MODE)	System.out.println(getClass().getSimpleName()+"[sql]"+sbSQL.toString());
		return sbSQL.toString();
	}

	private void outputQueryList() {

		// 検索条件の加工クラス作成
		JsonArrayData jad = new JsonArrayData();
		jad.setJsonString(getJson());

	}
}
