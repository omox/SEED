package dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;

import authentication.bean.User;
import authentication.defines.Consts;
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
public class Reportx204Dao extends ItemDao {

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
	public Reportx204Dao(String JNDIname) {
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

		JSONObject	option = new JSONObject();
		JSONArray	msgList = this.check(map, userInfo, sysdate);

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
		JSONArray	dataArray	= JSONArray.fromObject(map.get("DATA"));		// 更新情報(配送・店グループ)
		JSONArray	dataArrayT	= JSONArray.fromObject(map.get("DATA_HSGPT"));	// 更新情報(配送グループ店)

		// 格納用変数
		StringBuffer			sbSQL	= new StringBuffer();
		JSONArray				msg		= new JSONArray();
		ItemList				iL		= new ItemList();
		MessageUtility			mu		= new MessageUtility();
		JSONArray				dbDatas	= new JSONArray();
		ArrayList<JSONObject>	tencds	= new ArrayList<JSONObject>();
		HashSet<String>			tencds_	= new HashSet<String>();

		// DB検索用パラメータ
		String sqlWhere = "";
		ArrayList<String> paramData = new ArrayList<String>();

		// チェック処理
		// 対象件数チェック
		if(dataArray.size() == 0 || dataArray.getJSONObject(0).isEmpty()){
			msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
			return msg;
		}

		if(dataArrayT.size() == 0 || dataArrayT.getJSONObject(0).isEmpty()){
			msg.add(mu.getDbMessageObj("EX1047", new String[]{"店番"}));
			return msg;
		}

		// 入力値の重複チェック
		for (int i=0; i<dataArrayT.size(); i++){
			String val = dataArrayT.optJSONObject(i).optString("F1");
			if(StringUtils.isNotEmpty(val)){
				tencds.add(dataArrayT.optJSONObject(i));
				tencds_.add(val);
			}
		}

		if(tencds.size() != tencds_.size()){
			msg.add(mu.getDbMessageObj("E11141", new String[]{}));
			return msg;
		}

		// 店舗基本マスタ存在チェック
		for (int i = 0; i < dataArrayT.size(); i++) {
			JSONObject data = dataArrayT.getJSONObject(i);
			if(data.isEmpty()){
				continue;
			}

			// 変数を初期化
			sbSQL	= new StringBuffer();
			iL		= new ItemList();
			dbDatas = new JSONArray();
			sqlWhere	= "";
			paramData	= new ArrayList<String>();

			if (StringUtils.isEmpty(data.optString("F1"))) {
				sqlWhere += "TENCD=null";
			} else {
				sqlWhere += "TENCD=?";
				paramData.add(data.optString("F1"));
				tencds_.add(data.optString("F1"));
			}

			sbSQL.append("SELECT ");
			sbSQL.append("TENCD ");	// レコード件数
			sbSQL.append("FROM ");
			sbSQL.append("INAMS.MSTTEN ");
			sbSQL.append("WHERE ");
			sbSQL.append(sqlWhere); // 入力された店コードで検索

			dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

			if (dbDatas.size() == 0){
				// マスタに登録のない店舗
				msg.add(mu.getDbMessageObj("E11096", new String[]{}));
				return msg;
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

		// 格納用変数
		JSONArray msg = new JSONArray();

		// チェック処理
		// 対象件数チェック
		if(dataArray.size() == 0 || dataArray.getJSONObject(0).isEmpty()){
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
		JSONArray	dataArray	= JSONArray.fromObject(map.get("DATA"));		// 対象情報（主要な更新情報）
		JSONArray	dataArrayT	= JSONArray.fromObject(map.get("DATA_HSGPT"));	// 対象情報（主要な更新情報）

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
		createSqlHs(data,dataArrayT,userInfo);

		// 排他チェック実行
		targetTable = "INAMS.MSTHSTENGP";
		targetWhere = " HSGPCD=? AND TENGPCD=? ";
		targetParam.add(data.optString("F1"));
		targetParam.add(data.optString("F3"));

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
		JSONArray	dataArray	= JSONArray.fromObject(map.get("DATA"));		// 対象情報（主要な更新情報）

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
		targetTable = "INAMS.MSTHSTENGP";
		targetWhere = " HSGPCD=? AND TENGPCD=? ";
		targetParam.add(data.optString("F1"));
		targetParam.add(data.optString("F3"));

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
	 * 配送グループ店舗マスタDELETE(倫理削除)処理
	 *
	 * @param data
	 * @param map
	 * @param userInfo
	 */
	public String createDelSqlHs(JSONObject data, HashMap<String, String> map, User userInfo){

		// 格納用変数
		StringBuffer	sbSQL	= new StringBuffer();

		// ログインユーザー情報取得
		String userId	= userInfo.getId(); // ログインユーザー

		// DB検索用パラメータ
		String sqlWhere = "";
		ArrayList<String> paramData = new ArrayList<String>();

		if(StringUtils.isEmpty(data.optString("F1"))){
			sqlWhere += "HSGPCD=null AND ";
		}else{
			sqlWhere += "HSGPCD=? AND ";
			paramData.add(data.optString("F1"));
		}

		if(StringUtils.isEmpty(data.optString("F3"))){
			sqlWhere += "TENGPCD=null";
		}else{
			sqlWhere += "TENGPCD=?";
			paramData.add(data.optString("F3"));
		}

		// 配送グループ店舗マスタの論理削除
		sbSQL = new StringBuffer();
		sbSQL.append("UPDATE INAMS.MSTHSGPTEN ");
		sbSQL.append("SET ");
		sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.DEL.getVal());
		sbSQL.append(",OPERATOR='" + userId + "'");
		sbSQL.append(",UPDDT=current timestamp ");
		sbSQL.append("WHERE ");
		sbSQL.append(sqlWhere);

		if (DefineReport.ID_DEBUG_MODE)	System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(paramData);
		lblList.add("配送グループ店舗マスタ");

		return sbSQL.toString();
	}

	/**
	 * 配送グループ店舗マスタINSERT/UPDATE処理(DELETE→INSERT)
	 *
	 * @param data
	 * @param dataArrayT
	 * @param userInfo
	 */
	public String createSqlHs(JSONObject data, JSONArray dataArrayT, User userInfo){

		// ログインユーザー情報取得
		String userId = userInfo.getId(); // ログインユーザー

		// DB検索用パラメータ
		ArrayList<String>	paramData	= new ArrayList<String>();
		StringBuffer		sbSQL		= new StringBuffer();
		String				sqlWhere	= "";

		if(StringUtils.isEmpty(data.optString("F1"))){
			sqlWhere += "HSGPCD=null AND ";
		}else{
			sqlWhere += "HSGPCD=? AND ";
			paramData.add(data.optString("F1"));
		}

		if(StringUtils.isEmpty(data.optString("F3"))){
			sqlWhere += "TENGPCD=null";
		}else{
			sqlWhere += "TENGPCD=?";
			paramData.add(data.optString("F3"));
		}

		// 配送店グループマスタの論理削除
		sbSQL.append("UPDATE INAMS.MSTHSTENGP ");
		sbSQL.append("SET ");
		sbSQL.append("OPERATOR='" + userId + "'");
		sbSQL.append(",UPDDT=current timestamp ");
		sbSQL.append("WHERE ");
		sbSQL.append(sqlWhere);

		if (DefineReport.ID_DEBUG_MODE)	System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(paramData);
		lblList.add("配送店グループマスタ");

		// 配送店グループマスタの件数チェック
		sbSQL = new StringBuffer();
		sbSQL.append("DELETE FROM INAMS.MSTHSGPTEN ");
		sbSQL.append("WHERE ");
		sbSQL.append(sqlWhere);

		if (DefineReport.ID_DEBUG_MODE)	System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(paramData);
		lblList.add("配送グループ店舗マスタ");

		for (int i = 0; i < dataArrayT.size(); i++) {
			JSONObject dataT = dataArrayT.getJSONObject(i);
			if(dataT.isEmpty()){
				continue;
			}

			// クリア
			sqlWhere = "";
			paramData = new ArrayList<String>();

			// 配送グループコード
			if(StringUtils.isEmpty(data.optString("F1"))){
				sqlWhere += "null,";
			}else{
				sqlWhere += "?,";
				paramData.add(data.optString("F1"));
			}

			// 配送店グループコード
			if(StringUtils.isEmpty(data.optString("F3"))){
				sqlWhere += "null,";
			}else{
				sqlWhere += "?,";
				paramData.add(data.optString("F3"));
			}

			// 配送グループ店コード
			if(StringUtils.isEmpty(dataT.optString("F1"))){
				sqlWhere += "null";
			}else{
				sqlWhere += "?";
				paramData.add(dataT.optString("F1"));
			}

			sbSQL = new StringBuffer();

			sbSQL.append("INSERT INTO INAMS.MSTHSGPTEN( ");
			sbSQL.append("HSGPCD ");
			sbSQL.append(",TENGPCD ");
			sbSQL.append(",TENCD ");
			sbSQL.append(",UPDKBN ");
			sbSQL.append(",SENDFLG ");
			sbSQL.append(",OPERATOR ");
			sbSQL.append(",ADDDT ");
			sbSQL.append(",UPDDT ");
			sbSQL.append(") VALUES ( ");
			sbSQL.append(sqlWhere);
			sbSQL.append("," + DefineReport.ValUpdkbn.NML.getVal());
			sbSQL.append("," + DefineReport.Values.SENDFLG_UN.getVal());
			sbSQL.append(",'" + userId + "'");
			sbSQL.append(",current timestamp ");
			sbSQL.append(",current timestamp ");
			sbSQL.append(") ");

			if (DefineReport.ID_DEBUG_MODE)	System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

			sqlList.add(sbSQL.toString());
			prmList.add(paramData);
			lblList.add("配送グループ店舗マスタ");
		}
		return sbSQL.toString();
	}

	/**
	 * 排他チェック
	 *
	 * @param data
	 * @param dataArrayT
	 * @return msg
	 */
	public JSONArray checkUpdd(JSONObject data, JSONArray dataArrayT) {

		// 排他チェック用
		String				targetTable	= null;
		String				targetWhere	= null;
		ArrayList<String>	targetParam	= new ArrayList<String>();
		JSONArray			msg			= new JSONArray();

		// 該当テーブル
		targetTable = "INAMS.MSTHSTENGP";

		// 排他チェック実行
		for (int i = 0; i < dataArrayT.size(); i++) {
			JSONObject dataT = dataArrayT.getJSONObject(i);
			if(dataT.isEmpty()){
				continue;
			}

			// パラーメーター設定
			targetParam = new ArrayList<String>();
			targetParam.add(data.optString("F1"));	// 配送グループコード
			targetParam.add(data.optString("F3"));	// 配送店グループコード

			if(StringUtils.isEmpty(dataT.optString("F3"))){
				targetWhere = " HSGPCD=? AND TENGPCD=? AND UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal() +" ";
			} else {
				targetWhere = " HSGPCD=? AND TENGPCD=? AND TENCD=? ";
				targetParam.add(dataT.optString("F1"));	// 配送グループ店コード
			}

			if(!super.checkExclusion(targetTable, targetWhere, targetParam, dataT.optString("F3"))){
	 			msg.add(MessageUtility.getDbMessageExclusion(FieldType.DEFAULT, new String[]{}));
	 			return msg;
			}
		}
		return msg;
	}

	private String createCommand() {
		// ログインユーザー情報取得
		User userInfo = getUserInfo();
		if(userInfo==null){
			return "";
		}

		String szHsgpcd		= getMap().get("HSGPCD");	// 配送グループコード
		String szHsTengpcd	= getMap().get("HSTENGPCD");	// 配送グループ店コード

		// DB検索用パラメータ
		ArrayList<String> paramData = new ArrayList<String>();
		String sqlWhere = "";

		if(StringUtils.isEmpty(szHsgpcd)){
			sqlWhere += "HSG.HSGPCD=null AND HSG.HSGPCD=HST.HSGPCD AND ";
		}else{
			sqlWhere += "HSG.HSGPCD=? AND HSG.HSGPCD=HST.HSGPCD AND ";
			paramData.add(szHsgpcd);
		}

		if(StringUtils.isEmpty(szHsTengpcd)){
			sqlWhere += "HST.TENGPCD=null ";
		}else{
			sqlWhere += "HST.TENGPCD=? ";
			paramData.add(szHsTengpcd);
		}

		// タイトル情報(任意)設定
		List<String> titleList = new ArrayList<String>();

		// 一覧表情報
		StringBuffer sbSQL = new StringBuffer();

		sbSQL.append("SELECT ");
		sbSQL.append("HSG.HSGPCD");														// F1	: 配送グループ
		sbSQL.append(",HSG.HSGPKN");													// F2	: 配送グループ名称（漢字）
		sbSQL.append(",HST.TENGPCD AS HSTENGPCD ");										// F3	: 配送店グループ
		sbSQL.append(",HST.TENGPKN ");													// F4	: 配送店グループ名称（漢字）
		sbSQL.append(",HSG.OPERATOR ");													// F5	: オペレーター
		sbSQL.append(",TO_CHAR(HSG.ADDDT,'YY/MM/DD') AS ADDDT ");						// F6	: 登録日
		sbSQL.append(",TO_CHAR(HSG.UPDDT,'YY/MM/DD') AS UPDDT ");						// F7	: 更新日
		sbSQL.append(",TO_CHAR(HST.UPDDT,'YYYYMMDDHH24MISSNNNNNN') as HDN_UPDDT ");		// F8	: 更新日時（配送店グループ）
		sbSQL.append("FROM ");
		sbSQL.append("INAMS.MSTHSGP AS HSG");
		sbSQL.append(",INAMS.MSTHSTENGP AS HST ");
		sbSQL.append("WHERE ");
		sbSQL.append(sqlWhere);
		sbSQL.append("ORDER BY ");
		sbSQL.append("HSG.HSGPCD");
		sbSQL.append(",HST.TENGPCD");

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
