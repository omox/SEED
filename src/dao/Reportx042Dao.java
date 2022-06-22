package dao;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import authentication.bean.User;
import common.DefineReport;
import common.Defines;
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
public class Reportx042Dao extends ItemDao {

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
	public Reportx042Dao(String JNDIname) {
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

	/**
	 * 更新処理
	 * @param userInfo
	 *
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws Exception
	 */
	public JSONObject update(HttpServletRequest request,HttpSession session,  HashMap<String, String> map, User userInfo) {

		// 更新情報チェック(基本JS側で制御)
		JSONObject msgObj = new JSONObject();
		JSONArray msg = this.check(map);

		if(msg.size() > 0){
			msgObj.put(MsgKey.E.getKey(), msg);
			return msgObj;
		}

		// 更新処理
		try {
			msgObj = this.updateData(map, userInfo);
		} catch (Exception e) {
			e.printStackTrace();
			msgObj.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
		}
		return msgObj;
	}

	/**
	 * 削除処理
	 * @param userInfo
	 *
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws Exception
	 */
	public JSONObject delete(HttpServletRequest request,HttpSession session,  HashMap<String, String> map, User userInfo) {

		// 更新情報チェック(基本JS側で制御)
		JSONObject msgObj = new JSONObject();
		JSONArray msg = this.checkDel(map);

		if(msg.size() > 0){
			msgObj.put(MsgKey.E.getKey(), msg);
			return msgObj;
		}

		// 削除処理
		try {
			msgObj = this.deleteData(map, userInfo);
		} catch (Exception e) {
			e.printStackTrace();
			msgObj.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00002.getVal()));
		}
		return msgObj;
	}

	private String createCommand() {
		// ログインユーザー情報取得
		User userInfo = getUserInfo();
		if(userInfo==null){
			return "";
		}
		ArrayList<String> paramData = new ArrayList<String>();
		String szRTPattern	 = getMap().get("READTMPTN");			// 選択リードタイムパターン
		String sendBtnid	 = getMap().get("SENDBTNID");			// 呼出しボタン

		// パラメータ確認
		// 必須チェック
		if ( szRTPattern == null || sendBtnid == null) {
			System.out.println(super.getConditionLog());
			return "";
		}

		// タイトル情報(任意)設定
		List<String> titleList = new ArrayList<String>();

		StringBuffer sbSQL = new StringBuffer();

		if(DefineReport.Button.SEL_CHANGE.getObj().equals(sendBtnid)){

			sbSQL.append(" select");
			sbSQL.append(" right('00'||READTMPTN,3)");												// F1	：リードタイムパターン
			sbSQL.append(", READTMPTNKN");															// F2	：リードタイム名称
			sbSQL.append(", READTM_MON");															// F3	：リードタイム_月
			sbSQL.append(", READTM_TUE");															// F4	：リードタイム_火
			sbSQL.append(", READTM_WED");															// F5	：リードタイム_水
			sbSQL.append(", READTM_THU");															// F6	：リードタイム_木
			sbSQL.append(", READTM_FRI");															// F7	：リードタイム_金
			sbSQL.append(", READTM_SAT");															// F8	：リードタイム_土
			sbSQL.append(", READTM_SUN");															// F9	：リードタイム_日
			sbSQL.append(", OPERATOR");																// F10	：オペレータ
			sbSQL.append(", TO_CHAR(ADDDT, 'yy/mm/dd')");											// F11	：登録日
			sbSQL.append(", TO_CHAR(UPDDT, 'yy/mm/dd')");											// F12	：更新日
			sbSQL.append(", TO_CHAR(UPDDT,'YYYYMMDDHH24MISSNNNNNN') as HDN_UPDDT ");				// F13	: 排他チェック用：更新日(非表示)
			sbSQL.append(" from INAMS.MSTREADTM");
			sbSQL.append(" where NVL(UPDKBN, 0) = 0");
			sbSQL.append(" and READTMPTN = ? ");
			paramData.add(szRTPattern);
			sbSQL.append(" order by READTMPTN");
			setParamData(paramData);
		}

		// オプション情報（タイトル）設定
		JSONObject option = new JSONObject();
		option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
		setOption(option);

		if (DefineReport.ID_DEBUG_MODE) System.out.println(getClass().getSimpleName()+"[sql]"+sbSQL.toString());
		return sbSQL.toString();
	}

	private void outputQueryList() {

		// 検索条件の加工クラス作成
		JsonArrayData jad = new JsonArrayData();
		jad.setJsonString(getJson());

		// 保存用 List (検索情報)作成
		setWhere(new ArrayList<List<String>>());
		List<String> cells = new ArrayList<String>();

		// 共通箇所設定
		createCmnOutput(jad);

	}

	/**
	 * 更新処理実行
	 * @return
	 *
	 * @throws Exception
	 */
	private JSONObject updateData(HashMap<String, String> map, User userInfo) throws Exception {
		// パラメータ確認
		JSONObject	option		= new JSONObject();
		JSONArray	dataArray	= JSONArray.fromObject(map.get("DATA"));	// 対象情報（主要な更新情報）

		// 排他チェック用
		JSONArray			msg			= new JSONArray();

		// パラメータ確認
		// 必須チェック
		if ( dataArray.isEmpty() || dataArray.size() != 1 || dataArray.getJSONObject(0).isEmpty()) {
			option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
			return option;
		}

		// 基本登録情報
		JSONObject data = dataArray.getJSONObject(0);

		// リードタイムパターンマスタINSERT/UPDATE処理
		this.createSqlTREADTM(data,map,userInfo);

		// 排他チェック実行
		String targetTable = null;
		String targetWhere = null;
		ArrayList<String> targetParam = new ArrayList<String>();
		if(dataArray.size() > 0){
			targetTable = "INAMS.MSTREADTM";
			targetWhere = "NVL(UPDKBN, 0) <> 1 and READTMPTN = ?";
			targetParam.add(data.optString("F1"));
			if(! super.checkExclusion(targetTable, targetWhere, targetParam, data.optString("F13"))){
	 			msg.add(MessageUtility.getDbMessageExclusion(FieldType.DEFAULT, new String[]{}));
	 			option.put(MsgKey.E.getKey(), msg);
				return option;
			}
		}

		ArrayList<Integer> countList  = new ArrayList<Integer>();
		if(sqlList.size() > 0){
			countList =  super.executeSQLs(sqlList, prmList);
		}

		if(StringUtils.isEmpty(getMessage())){
			int count = 0;
			for (int i = 0; i < countList.size(); i++) {
				count += countList.get(i);
				if (DefineReport.ID_DEBUG_MODE) System.out.println(MessageUtility.getMessage(Msg.S00006.getVal(), new String[]{lblList.get(i), Integer.toString(countList.get(i))}));
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
	 * 更新処理実行
	 * @return
	 *
	 * @throws Exception
	 *//*
	@SuppressWarnings("static-access")
	private JSONObject updateData(HashMap<String, String> map, User userInfo) throws Exception {
		// パラメータ確認
		JSONArray dataArray = JSONArray.fromObject(map.get("DATA"));	// 対象情報
		JSONObject msgObj = new JSONObject();
		JSONArray msg = new JSONArray();

		// ログインユーザー情報取得
		String userId	= userInfo.getId();									// ログインユーザー

		String sendBtnid = map.get("SENDBTNID");							// 呼出しボタン
		String szRTPattern = "";											// 入力リードタイムパターン
		String addSQL = "";

		// 更新情報
		String values = "";
		for (int i = 0; i < dataArray.size(); i++) {
			JSONObject data = dataArray.getJSONObject(i);
			if(data.isEmpty()){
				continue;
			}

			szRTPattern = data.optString("F1");
			values += szRTPattern +",";
			values += StringUtils.defaultIfEmpty("'"+data.optString("F2")+"'", "null")+",";
			values += StringUtils.defaultIfEmpty(data.optString("F3"), "null")+",";
			values += StringUtils.defaultIfEmpty(data.optString("F4"), "null")+",";
			values += StringUtils.defaultIfEmpty(data.optString("F5"), "null")+",";
			values += StringUtils.defaultIfEmpty(data.optString("F6"), "null")+",";
			values += StringUtils.defaultIfEmpty(data.optString("F7"), "null")+",";
			values += StringUtils.defaultIfEmpty(data.optString("F8"), "null")+",";
			values += StringUtils.defaultIfEmpty(data.optString("F9"), "null")+",";
			values += "null,";
			values += "null,";
			values += "null,";
			values += "null,";
			values += "null)";

		}

		if(values.length()==0){
			msgObj.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
			return msgObj;
		}

		// 基本INSERT/UPDATE文
		StringBuffer sbSQL;
		ItemList iL = new ItemList();
		JSONArray dbDatas = new JSONArray();
		ArrayList<String> prmData = new ArrayList<String>();

		if(DefineReport.Button.NEW.getObj().equals(sendBtnid)){
			// 重複チェック：部門コード
			sbSQL = new StringBuffer();
			sbSQL.append("select * from INAMS.MSTREADTM RTM where NVL(RTM.UPDKBN, 0) = 0 and RTM.READTMPTN ="+szRTPattern);
			dbDatas = iL.selectJSONArray(sbSQL.toString(), null, Defines.STR_JNDI_DS);
			if(dbDatas.size() > 0 ){
				msg.add(MessageUtility.getMsg("リードタイムパターンが重複しています。"));
			}

			// 論理削除データ有の場合SQL文を追加
			sbSQL = new StringBuffer();
			sbSQL.append("select * from INAMS.MSTREADTM RTM where NVL(RTM.UPDKBN, 0) = 1 and RTM.READTMPTN ="+szRTPattern);
			dbDatas = iL.selectJSONArray(sbSQL.toString(), null, Defines.STR_JNDI_DS);
			if(dbDatas.size() > 0 ){
				addSQL = ", ADDDT = RE.ADDDT";

			}
		}

		// 更新SQL
		sbSQL = new StringBuffer();
		sbSQL.append(" merge into INAMS.MSTREADTM as T using (select");
		sbSQL.append(" T1.READTMPTN");
		sbSQL.append(", T1.READTMPTNKN");
		sbSQL.append(", T1.READTM_MON");
		sbSQL.append(", T1.READTM_TUE");
		sbSQL.append(", T1.READTM_WED");
		sbSQL.append(", T1.READTM_THU");
		sbSQL.append(", T1.READTM_FRI");
		sbSQL.append(", T1.READTM_SAT");
		sbSQL.append(", T1.READTM_SUN");
		sbSQL.append(", "+DefineReport.ValUpdkbn.NML.getVal()+" as UPDKBN");
		sbSQL.append(", T1.SENDFLG");
		sbSQL.append(", '"+userId+"' as OPERATOR");
		sbSQL.append(", current timestamp as ADDDT");
		sbSQL.append(", current timestamp as UPDDT");
		sbSQL.append(" from (values ("+values+") as T1(");
		sbSQL.append(" READTMPTN");
		sbSQL.append(", READTMPTNKN");
		sbSQL.append(", READTM_MON");
		sbSQL.append(", READTM_TUE");
		sbSQL.append(", READTM_WED");
		sbSQL.append(", READTM_THU");
		sbSQL.append(", READTM_FRI");
		sbSQL.append(", READTM_SAT");
		sbSQL.append(", READTM_SUN");
		sbSQL.append(", UPDKBN");
		sbSQL.append(", SENDFLG");
		sbSQL.append(", OPERATOR");
		sbSQL.append(", ADDDT");
		sbSQL.append(", UPDDT))");
		sbSQL.append(" as RE on (T.READTMPTN = RE.READTMPTN)");
		sbSQL.append(" when matched then update set");
		sbSQL.append(" READTMPTN = RE.READTMPTN");
		sbSQL.append(", READTMPTNKN = RE.READTMPTNKN");
		sbSQL.append(", READTM_MON = RE.READTM_MON");
		sbSQL.append(", READTM_TUE = RE.READTM_TUE");
		sbSQL.append(", READTM_WED = RE.READTM_WED");
		sbSQL.append(", READTM_THU = RE.READTM_THU");
		sbSQL.append(", READTM_FRI = RE.READTM_FRI");
		sbSQL.append(", READTM_SAT = RE.READTM_SAT");
		sbSQL.append(", READTM_SUN = RE.READTM_SUN");
		sbSQL.append(   addSQL);
		sbSQL.append(", UPDKBN = RE.UPDKBN");
		sbSQL.append(", SENDFLG = RE.SENDFLG");
		sbSQL.append(", OPERATOR = RE.OPERATOR");
		sbSQL.append(", UPDDT = RE.UPDDT");
		sbSQL.append(" when not matched then insert values (");
		sbSQL.append(" RE.READTMPTN");
		sbSQL.append(", RE.READTMPTNKN");
		sbSQL.append(", RE.READTM_MON");
		sbSQL.append(", RE.READTM_TUE");
		sbSQL.append(", RE.READTM_WED");
		sbSQL.append(", RE.READTM_THU");
		sbSQL.append(", RE.READTM_FRI");
		sbSQL.append(", RE.READTM_SAT");
		sbSQL.append(", RE.READTM_SUN");
		sbSQL.append(", RE.UPDKBN");
		sbSQL.append(", RE.SENDFLG");
		sbSQL.append(", RE.OPERATOR");
		sbSQL.append(", RE.ADDDT");
		sbSQL.append(", RE.UPDDT)");

		if (msg.size() > 0) {
			// 重複チェック_エラー時
			msgObj.put(MsgKey.E.getKey(), msg);

		}else{
			// 更新処理実行
			System.out.println(sbSQL);
			int count = super.executeSQL(sbSQL.toString(), prmData);
			if(StringUtils.isEmpty(getMessage())) {
				System.out.println("部門を " + MessageUtility.getMessage(Msg.S00003.getVal(),
						new String[] { Integer.toString(dataArray.size()), Integer.toString(count) }));
				msgObj.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00001.getVal()));
			}else{
				msgObj.put(MsgKey.E.getKey(), getMessage());
			}
		}
		return msgObj;
	}*/

	/**
	 * リードタイムパターンマスタINSERT/UPDATE処理
	 *
	 * @param data
	 * @param map
	 * @param userInfo
	 */
	public String createSqlTREADTM(JSONObject data, HashMap<String, String> map, User userInfo){

		StringBuffer	sbSQL		= new StringBuffer();

		// ログインユーザー情報取得
		String userId		= userInfo.getId();					// ログインユーザー
		String sendBtnid	= map.get("SENDBTNID");				// 呼出しボタン

		ArrayList<String> prmData = new ArrayList<String>();
		Object[] valueData = new Object[]{};
		String values = "";

		int maxField = 9;		// Fxxの最大値
		for (int k = 1; k <= maxField; k++) {
			String key = "F" + String.valueOf(k);

			if(k == 1){
				values += String.valueOf(0 + 1);

			}

			if(! ArrayUtils.contains(new String[]{""}, key)){
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

		// リードタイムパターンマスタの登録・更新
		sbSQL = new StringBuffer();
		sbSQL.append(" merge into INAMS.MSTREADTM as T using (select");
		sbSQL.append(" READTMPTN");													// リードタイムパターン
		sbSQL.append(", READTMPTNKN");												// リードタイム名称
		sbSQL.append(", READTM_MON");												// リードタイム_月
		sbSQL.append(", READTM_TUE");												// リードタイム_火
		sbSQL.append(", READTM_WED");												// リードタイム_水
		sbSQL.append(", READTM_THU");												// リードタイム_木
		sbSQL.append(", READTM_FRI");												// リードタイム_金
		sbSQL.append(", READTM_SAT");												// リードタイム_土
		sbSQL.append(", READTM_SUN");												// リードタイム_日
		sbSQL.append(", "+DefineReport.ValUpdkbn.NML.getVal()+" AS UPDKBN");		// 更新区分：
		sbSQL.append(", 0 as SENDFLG");												// 送信フラグ
		sbSQL.append(", '"+userId+"' AS OPERATOR ");								// オペレーター：
		sbSQL.append(", current timestamp AS ADDDT ");								// 登録日：
		sbSQL.append(", current timestamp AS UPDDT ");								// 更新日：
		sbSQL.append(" from (values "+StringUtils.join(valueData, ",")+") as T1(NUM, ");
		sbSQL.append(" READTMPTN");
		sbSQL.append(", READTMPTNKN");
		sbSQL.append(", READTM_MON");
		sbSQL.append(", READTM_TUE");
		sbSQL.append(", READTM_WED");
		sbSQL.append(", READTM_THU");
		sbSQL.append(", READTM_FRI");
		sbSQL.append(", READTM_SAT");
		sbSQL.append(", READTM_SUN");
		sbSQL.append(" ))as RE on (T.READTMPTN = RE.READTMPTN)");
		sbSQL.append(" when matched then update set");
		sbSQL.append(" READTMPTN=RE.READTMPTN");
		sbSQL.append(", READTMPTNKN=RE.READTMPTNKN");
		sbSQL.append(", READTM_MON=RE.READTM_MON");
		sbSQL.append(", READTM_TUE=RE.READTM_TUE");
		sbSQL.append(", READTM_WED=RE.READTM_WED");
		sbSQL.append(", READTM_THU=RE.READTM_THU");
		sbSQL.append(", READTM_FRI=RE.READTM_FRI");
		sbSQL.append(", READTM_SAT=RE.READTM_SAT");
		sbSQL.append(", READTM_SUN=RE.READTM_SUN");
		sbSQL.append(", UPDKBN=RE.UPDKBN");
		sbSQL.append(", SENDFLG=RE.SENDFLG");
		sbSQL.append(", OPERATOR=RE.OPERATOR");
		sbSQL.append(", ADDDT = case when NVL(UPDKBN, 0) = 1 then RE.ADDDT else ADDDT end");
		sbSQL.append(", UPDDT=RE.UPDDT");
		sbSQL.append(" when not matched then insert (");
		sbSQL.append(" READTMPTN");
		sbSQL.append(", READTMPTNKN");
		sbSQL.append(", READTM_MON");
		sbSQL.append(", READTM_TUE");
		sbSQL.append(", READTM_WED");
		sbSQL.append(", READTM_THU");
		sbSQL.append(", READTM_FRI");
		sbSQL.append(", READTM_SAT");
		sbSQL.append(", READTM_SUN");
		sbSQL.append(", UPDKBN");
		sbSQL.append(", SENDFLG");
		sbSQL.append(", OPERATOR");
		sbSQL.append(", ADDDT");
		sbSQL.append(", UPDDT");
		sbSQL.append(") values (");
		sbSQL.append(" RE.READTMPTN");
		sbSQL.append(", RE.READTMPTNKN");
		sbSQL.append(", RE.READTM_MON");
		sbSQL.append(", RE.READTM_TUE");
		sbSQL.append(", RE.READTM_WED");
		sbSQL.append(", RE.READTM_THU");
		sbSQL.append(", RE.READTM_FRI");
		sbSQL.append(", RE.READTM_SAT");
		sbSQL.append(", RE.READTM_SUN");
		sbSQL.append(", RE.UPDKBN");
		sbSQL.append(", RE.SENDFLG");
		sbSQL.append(", RE.OPERATOR");
		sbSQL.append(", RE.ADDDT");
		sbSQL.append(", RE.UPDDT");
		sbSQL.append(")");

		if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("リードタイムパターンマスタ");

		// クリア
		prmData = new ArrayList<String>();
		valueData = new Object[]{};
		values = "";

		return sbSQL.toString();
	}



	/**
	 * 削除処理実行
	 * @return
	 *
	 * @throws Exception
	 */
	@SuppressWarnings("static-access")
	private JSONObject deleteData(HashMap<String, String> map, User userInfo) throws Exception {
		// パラメータ確認
		JSONArray dataArray = JSONArray.fromObject(map.get("DATA"));	// 対象情報

		JSONObject msgObj = new JSONObject();
		JSONArray msg = new JSONArray();

		// ログインユーザー情報取得
		String userId	= userInfo.getId();								// ログインユーザー

		String szRTPattern = map.get("READTMPTN");

		if(szRTPattern.length()==0){
			msgObj.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00002.getVal()));
			return msgObj;
		}

		// 基本INSERT/UPDATE文
		ItemList iL = new ItemList();
		JSONArray dbDatas = new JSONArray();
		StringBuffer sbSQL;
		ArrayList<String> prmData = new ArrayList<String>();

		// 削除処理：更新区分に"1"（削除）を登録
		sbSQL = new StringBuffer();
		sbSQL.append("UPDATE INAMS.MSTREADTM ");
		sbSQL.append("SET ");
		sbSQL.append(" SENDFLG = 0");
		sbSQL.append(",UPDKBN =" + DefineReport.ValUpdkbn.DEL.getVal());
		sbSQL.append(",OPERATOR ='" + userId + "'");
		sbSQL.append(",UPDDT = current timestamp ");
		sbSQL.append(" WHERE READTMPTN = ? ");
		prmData.add(szRTPattern);

		int count = super.executeSQL(sbSQL.toString(), prmData);
		if(StringUtils.isEmpty(getMessage())){
			if (DefineReport.ID_DEBUG_MODE) System.out.println("リードタイムマスタを "+MessageUtility.getMessage(Msg.S00004.getVal(), new String[]{Integer.toString(count)}));
			msgObj.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00002.getVal()));
		}else{
			msgObj.put(MsgKey.E.getKey(), getMessage());
		}
		return msgObj;
	}

	/**
	 * チェック処理
	 *
	 * @throws Exception
	 */
	public JSONArray check(HashMap<String, String> map) {
		// パラメータ確認
		JSONArray dataArray = JSONArray.fromObject(map.get("DATA"));	// 更新情報
		String outobj		= map.get(DefineReport.ID_PARAM_OBJ);		// 実行ボタン

		JSONArray msg		 = new JSONArray();
		MessageUtility mu	 = new MessageUtility();
		String sqlcommand	 = "";
		ArrayList<String> paramData	 = new ArrayList<String>();

		String sendBtnid	= map.get("SENDBTNID");			// 呼出しボタン

		// 入力値を取得
		JSONObject data = dataArray.getJSONObject(0);
		// 関連情報取得
		ItemList iL = new ItemList();

		// チェック処理
		// 対象件数チェック
		if(dataArray.size() == 0 || dataArray.getJSONObject(0).isEmpty()){
			msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
			return msg;
		}

		// 重複チェック：リードタイムパターン

		if(StringUtils.equals(DefineReport.Button.NEW.getObj(), sendBtnid)){
			paramData  = new ArrayList<String>();
			paramData.add(data.getString("F1"));
			sqlcommand = "select count(*) as VALUE from INAMS.MSTREADTM where NVL(UPDKBN, 0) <> 1 and READTMPTN = ?";

			@SuppressWarnings("static-access")
			JSONArray array = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
			if(array.size() > 0 && NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) > 0){
				JSONObject o = mu.getDbMessageObj("E00004", "リードタイムパターン");
				msg.add(o);
				return msg;
			}
		}
		return msg;
	}

	/**
	 * チェック処理(削除)
	 *
	 * @throws Exception
	 */
	@SuppressWarnings("static-access")
	public JSONArray checkDel(HashMap<String, String> map) {
		// 関連情報取得
		ItemList iL = new ItemList();
		// パラメータ確認
		JSONArray dataArray = JSONArray.fromObject(map.get("DATA"));	// 更新情報

		StringBuffer sbSQL = new StringBuffer();
		ArrayList<String> prmData	 = new ArrayList<String>();
		JSONArray msg				 = new JSONArray();
		JSONArray dbDatas 			 = new JSONArray();
		MessageUtility mu			 = new MessageUtility();

		// 入力値を取得
		JSONObject data = dataArray.getJSONObject(0);

		String szRTPattern	= map.get("READTMPTN");			// リードタイムパターン

		// 対象件数チェック
		if(dataArray.size() == 0 || dataArray.getJSONObject(0).isEmpty()){
			msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
			return msg;
		}

		// チェック処理
		// 部門紐付チェック:商品マスタ
		sbSQL = new StringBuffer();
		prmData	 = new ArrayList<String>();
		sbSQL.append("select READTMPTN from INAMS.MSTSHN where READTMPTN = ? and NVL(UPDKBN, 0) <> 1 fetch first 1 rows only");
		prmData.add(szRTPattern);
		dbDatas = iL.selectJSONArray(sbSQL.toString(), prmData, Defines.STR_JNDI_DS);
		if(dbDatas.size() > 0 ){
			msg.add(mu.getDbMessageObj("E00006", new String[]{}));
			return msg;
		}

		// 部門紐付チェック店舗部門マスタ
		sbSQL = new StringBuffer();
		prmData	 = new ArrayList<String>();
		sbSQL.append("select READTMPTN from INAMS.MSTTENBMN where READTMPTN = ? and NVL(UPDKBN, 0) <> 1 fetch first 1 rows only");
		prmData.add(szRTPattern);
		dbDatas = iL.selectJSONArray(sbSQL.toString(), prmData, Defines.STR_JNDI_DS);
		if(dbDatas.size() > 0 ){
			msg.add(mu.getDbMessageObj("E00006", new String[]{}));
			return msg;
		}
		return msg;
	}
}
