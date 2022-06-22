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
public class Reportx216Dao extends ItemDao {

	/** SQLリスト保持用変数 */
	ArrayList<String> sqlList = new ArrayList<String>();
	/** SQLのパラメータリスト保持用変数 */
	ArrayList<ArrayList<String>> prmList = new ArrayList<ArrayList<String>>();
	/** SQLログ用のラベルリスト保持用変数 */
	ArrayList<String> lblList = new ArrayList<String>();

	/** 入力No保持用変数 */
	String input_seq = "";			//

	/**
	 * インスタンスを生成します。
	 * @param source
	 */
	public Reportx216Dao(String JNDIname) {
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
		JSONArray	dataArray	= JSONArray.fromObject(map.get("DATA"));		// 更新情報(プライスカード発行トラン)
		JSONArray	dataArrayG	= JSONArray.fromObject(map.get("DATA_PCSU"));	// 更新情報(プライスカード発行枚数トラン)

		// 格納用変数
		StringBuffer			sbSQL		= new StringBuffer();
		JSONArray				msg			= new JSONArray();
		ItemList				iL			= new ItemList();
		MessageUtility			mu			= new MessageUtility();
		JSONArray				dbDatas		= new JSONArray();
		ArrayList<JSONObject>	tenBmncds	= new ArrayList<JSONObject>();
		HashSet<String>			tenBmncds_	= new HashSet<String>();

		// DB検索用パラメータ
		String sqlWhere = "";
		ArrayList<String> paramData = new ArrayList<String>();

		// チェック処理
		// 対象件数チェック
		if(dataArray.size() == 0 || dataArray.getJSONObject(0).isEmpty()){
			msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
			return msg;
		}

		if(dataArrayG.size() == 0 || dataArrayG.getJSONObject(0).isEmpty()){
			msg.add(mu.getDbMessageObj("EX1047", new String[]{"店コード"}));
			return msg;
		}

		/*
		// 入力値の重複チェック
		for (int i=0; i<dataArrayG.size(); i++){
			String val = dataArrayG.optJSONObject(i).optString("F2") + dataArrayG.optJSONObject(i).optString("F4");
			if(StringUtils.isNotEmpty(val)){
				tenBmncds.add(dataArrayG.optJSONObject(i));
				tenBmncds_.add(val);
			}
		}

		if(tenBmncds.size() != tenBmncds_.size()){
			msg.add(mu.getDbMessageObj("EX1023", new String[]{}));
			return msg;
		}
		*/

		// 店舗基本・部門マスタ存在チェック
		for (int i = 0; i < dataArrayG.size(); i++) {
			JSONObject data = dataArrayG.getJSONObject(i);
			if(data.isEmpty()){
				continue;
			}

			// 店・部門・枚数は同時入力
			if (StringUtils.isEmpty(data.optString("F2")) || StringUtils.isEmpty(data.optString("F4")) || StringUtils.isEmpty(data.optString("F6"))) {
				msg.add(mu.getDbMessageObj("E11049", new String[]{"店コードと部門コードと枚数を"}));
				return msg;
			}

			// 枚数のみに入力が存在する場合
			if ((StringUtils.isEmpty(data.optString("F2")) || StringUtils.isEmpty(data.optString("F4"))) &&
					(!StringUtils.isEmpty(data.optString("F6")))) {
				msg.add(mu.getDbMessageObj("EX1040", new String[]{}));
				return msg;
			}

			// 枚数にマイナス値の入力は
			if (data.optInt("F6") < 0) {
				msg.add(mu.getDbMessageObj("EX1047", new String[]{"枚数 ≧ 0の値"}));
				return msg;
			}

			// 変数を初期化
			sbSQL	= new StringBuffer();
			iL		= new ItemList();
			dbDatas = new JSONArray();
			sqlWhere	= "";
			paramData	= new ArrayList<String>();

			if (StringUtils.isEmpty(data.optString("F2"))) {
				sqlWhere += "TENCD=null";
			} else {
				sqlWhere += "TENCD=?";
				paramData.add(data.optString("F2"));
			}

			sbSQL.append("SELECT ");
			sbSQL.append("TENCD ");	// レコード件数
			sbSQL.append("FROM ");
			sbSQL.append("INAMS.MSTTEN ");
			sbSQL.append("WHERE ");
			sbSQL.append(sqlWhere); // 入力された店コードで検索

			dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

			if (dbDatas.size() == 0){
				// マスタに登録のない店
				msg.add(mu.getDbMessageObj("E11096", new String[]{}));
				return msg;
			}

			sbSQL	= new StringBuffer();
			iL		= new ItemList();
			dbDatas = new JSONArray();
			sqlWhere	= "";
			paramData	= new ArrayList<String>();

			if (StringUtils.isEmpty(data.optString("F4"))) {
				sqlWhere += "BMNCD=null";
			} else {
				sqlWhere += "BMNCD=?";
				paramData.add(data.optString("F4"));
			}

			sbSQL.append("SELECT ");
			sbSQL.append("BMNCD ");	// レコード件数
			sbSQL.append("FROM ");
			sbSQL.append("INAMS.MSTBMN ");
			sbSQL.append("WHERE ");
			sbSQL.append(sqlWhere); // 入力された店コードで検索

			dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

			if (dbDatas.size() == 0){
				// マスタに登録のない部門
				msg.add(mu.getDbMessageObj("E11044", new String[]{}));
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
		JSONArray	dataArray	= JSONArray.fromObject(map.get("DATA"));		// 更新情報(プライスカード発行トラン)

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
		JSONArray	dataArray	= JSONArray.fromObject(map.get("DATA"));		// 更新情報(プライスカード発行トラン)
		JSONArray	dataArrayG	= JSONArray.fromObject(map.get("DATA_PCSU"));	// 更新情報(プライスカード発行枚数トラン)

		// 排他チェック用
		String				targetTable	= null;
		String				targetWhere	= null;
		ArrayList<String>	targetParam	= new ArrayList<String>();
		JSONArray			msg			= new JSONArray();

		// 基本登録情報
		JSONObject data = dataArray.getJSONObject(0);

		// プライスカード発行・枚数トランINSERT/UPDATE処理
		createSqlPc(data,dataArrayG,userInfo);

		// 排他チェック実行
		if(!StringUtils.isEmpty(data.optString("F4"))){
			targetTable = "INAMS.TRNPCARD";
			targetWhere = " INPUTNO=? ";
			targetParam.add(data.optString("F4"));

			if(!super.checkExclusion(targetTable, targetWhere, targetParam, data.optString("F8"))){
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
		JSONArray	dataArray	= JSONArray.fromObject(map.get("DATA"));		// 更新情報(プライスカード発行トラン)

		// 排他チェック用
		String				targetTable	= null;
		String				targetWhere	= null;
		ArrayList<String>	targetParam	= new ArrayList<String>();
		JSONArray			msg			= new JSONArray();

		// 基本登録情報
		JSONObject data = dataArray.getJSONObject(0);

		// 配送グループマスタ、配送店グループマスタINSERT/UPDATE処理
		createDelSqlHs(data,map,userInfo);

		// 排他チェック実行
		if(!StringUtils.isEmpty(data.optString("F4"))){
			targetTable = "INAMS.TRNPCARD";
			targetWhere = " INPUTNO=? ";
			targetParam.add(data.optString("F4"));

			if(!super.checkExclusion(targetTable, targetWhere, targetParam, data.optString("F8"))){
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
	 * プライスカード発行・枚数トランINSERT/UPDATE処理
	 *
	 * @param data
	 * @param dataArrayG
	 * @param userInfo
	 */
	public String createSqlPc(JSONObject data, JSONArray dataArrayG, User userInfo){

		// ログインユーザー情報取得
		String userId = userInfo.getId(); // ログインユーザー

		// DB検索用パラメータ
		ArrayList<String>	paramData	= new ArrayList<String>();
		StringBuffer		sbSQL		= new StringBuffer();
		String				sqlWhere	= "";
		Object[]			valueData	= new Object[]{};
		String				values		= "";

		// 新規登録時のみ番号を発行
		if(StringUtils.isEmpty(data.optString("F4"))){

			// SEQ001から新しい番号を発行
			input_seq = getInput_SEQ();

			// コメント
			if(StringUtils.isEmpty(data.optString("F1"))){
				sqlWhere += "null,";
			}else{
				sqlWhere += "?,";
				paramData.add(data.optString("F1"));
			}

			// マスタ予約日付
			if(StringUtils.isEmpty(data.optString("F2"))){
				sqlWhere += "null,";
			}else{
				sqlWhere += "?,";
				paramData.add(data.optString("F2"));
			}

			// プライスカードサイズ
			if(StringUtils.isEmpty(data.optString("F3"))){
				sqlWhere += "null";
			}else{
				sqlWhere += "?";
				paramData.add(data.optString("F3"));
			}

			sbSQL.append("INSERT INTO INAMS.TRNPCARD( ");
			sbSQL.append("COMAN ");
			sbSQL.append(",MST_YOYAKUDT ");
			sbSQL.append(",PCARDSZ ");
			sbSQL.append(",INPUTNO ");
			sbSQL.append(",SAKUBAIKAKB ");	// 固定
			sbSQL.append(",MAISUHOHOKB ");	// 固定
			sbSQL.append(",UPDKBN ");
			sbSQL.append(",SENDFLG ");
			sbSQL.append(",OPERATOR ");
			sbSQL.append(",ADDDT ");
			sbSQL.append(",UPDDT ");
			sbSQL.append(") VALUES ( ");
			sbSQL.append(sqlWhere);
			sbSQL.append("," + input_seq);
			sbSQL.append("," + DefineReport.SakuBaikakbn.TEN.getVal());
			sbSQL.append("," + DefineReport.MaisuHohokbn.BMNMAISU.getVal());
			sbSQL.append("," + DefineReport.ValUpdkbn.NML.getVal());
			sbSQL.append("," + DefineReport.Values.SENDFLG_UN.getVal());
			sbSQL.append(",'" + userId + "'");
			sbSQL.append(",current timestamp ");
			sbSQL.append(",current timestamp ");
			sbSQL.append(") ");

			if (DefineReport.ID_DEBUG_MODE)	System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

			sqlList.add(sbSQL.toString());
			prmList.add(paramData);
			lblList.add("プライスカード発行トラン");
		} else {

			// 更新データの場合は既存の入力Noを設定
			input_seq = data.optString("F4");
		}

		// クリア
		paramData = new ArrayList<String>();
		valueData = new Object[]{};
		values = "";

		int maxField = 7;		// Fxxの最大値
		int len = dataArrayG.size();
		int delcnt = 0;
		for (int i = 0; i < len; i++) {
			JSONObject dataG = dataArrayG.getJSONObject(i);
			if(! dataG.isEmpty()){
				for (int k = 1; k <= maxField; k++) {
					String key = "F" + String.valueOf(k);

					if(k == 1){
						values += String.valueOf(i + 1);
						// 配送グループコードを追加
						values += ", ?";
						paramData.add(input_seq);
					}

					if(! ArrayUtils.contains(new String[]{"F3","F5"}, key)){
						String val = dataG.optString(key);

						if(StringUtils.isEmpty(val)){

							// 新規登録の場合更新区分は有効
							if (key.equals("F1")) {
								values += "," + DefineReport.ValUpdkbn.NML.getVal();
								values += "," + DefineReport.Values.SENDFLG_UN.getVal();

							// 新規登録の場合SEQ001から新しい番号を発行
							} else if (key.equals("F7")) {
								values += ", ?";
								paramData.add(String.valueOf(i+1));
							} else {
								values += ", null";
							}
						}else{
							values += ", ?";
							paramData.add(val);

							// 削除対象行の件数をチェック
							if (key.equals("F1")) {
								if (val.equals("1")) {
									delcnt++;
								}
								// 送信フラグを追加
								values += "," + DefineReport.Values.SENDFLG_UN.getVal();
							}
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
				sbSQL.append("MERGE INTO INAMS.TRNPCARDSU AS T USING (SELECT ");
				sbSQL.append("  INPUTNO");											// 入力No：
				sbSQL.append(", UPDKBN");											// 更新区分：
				sbSQL.append(", SENDFLG");											// 送信区分：
				sbSQL.append(", TENCD");											// 店コード：
				sbSQL.append(", BMNCD");											// 部門コード：
				sbSQL.append(", MAISU");											// 枚数：
				sbSQL.append(", SEQ");												// SEQ：
				sbSQL.append(", '"+userId+"' AS OPERATOR ");						// オペレーター：
				sbSQL.append(", current timestamp AS ADDDT ");						// 登録日：
				sbSQL.append(", current timestamp AS UPDDT ");						// 更新日：
				sbSQL.append(" FROM (values "+StringUtils.join(valueData, ",")+") as T1(NUM");
				sbSQL.append(",INPUTNO");											// 特殊	: 入力No：
				sbSQL.append(",UPDKBN");											// F1	: 更新区分：
				sbSQL.append(",SENDFLG");											// F1	: 送信区分：
				sbSQL.append(",TENCD");												// F2	: 店コード：
				sbSQL.append(",BMNCD");												// F3	: 部門コード：
				sbSQL.append(",MAISU");												// F4	: 枚数：
				sbSQL.append(",SEQ");												// F5	: SEQ：
				sbSQL.append(")) as RE on (");
				sbSQL.append("T.INPUTNO=RE.INPUTNO AND ");
				sbSQL.append("T.SEQ=RE.SEQ ");
				sbSQL.append(") WHEN MATCHED THEN UPDATE SET ");
				sbSQL.append("TENCD=RE.TENCD ");
				sbSQL.append(",BMNCD=RE.BMNCD ");
				sbSQL.append(",MAISU=RE.MAISU ");
				sbSQL.append(",UPDKBN=RE.UPDKBN ");
				sbSQL.append(",SENDFLG=RE.SENDFLG ");
				sbSQL.append(",OPERATOR=RE.OPERATOR ");
				sbSQL.append(",UPDDT=RE.UPDDT");
				sbSQL.append(" WHEN NOT MATCHED THEN INSERT (");
				sbSQL.append("INPUTNO ");
				sbSQL.append(",SEQ");
				sbSQL.append(",BMNCD");
				sbSQL.append(",MAISU");
				sbSQL.append(",TENCD");
				sbSQL.append(",UPDKBN");
				sbSQL.append(",SENDFLG");
				sbSQL.append(",OPERATOR");
				sbSQL.append(",ADDDT");
				sbSQL.append(",UPDDT");
				sbSQL.append(") VALUES (");
				sbSQL.append("RE.INPUTNO ");
				sbSQL.append(",RE.SEQ");
				sbSQL.append(",RE.BMNCD");
				sbSQL.append(",RE.MAISU");
				sbSQL.append(",RE.TENCD");
				sbSQL.append(",RE.UPDKBN");
				sbSQL.append(",RE.SENDFLG");
				sbSQL.append(",RE.OPERATOR");
				sbSQL.append(",RE.ADDDT");
				sbSQL.append(",RE.UPDDT");
				sbSQL.append(")");

				if (DefineReport.ID_DEBUG_MODE)	System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

				sqlList.add(sbSQL.toString());
				prmList.add(paramData);
				lblList.add("プライスカード発行枚数トラン");

				// クリア
				paramData = new ArrayList<String>();
				valueData = new Object[]{};
				values = "";
			}
		}
		// 新規登録時のみ番号を発行
		if(!StringUtils.isEmpty(data.optString("F4"))){
			paramData	= new ArrayList<String>();
			sqlWhere	= "";

			// 入力No
			sqlWhere += "INPUTNO=?";
			paramData.add(input_seq);

			sbSQL = new StringBuffer();
			sbSQL.append("UPDATE ");
			sbSQL.append("INAMS.TRNPCARD ");
			sbSQL.append("SET ");
			sbSQL.append(" OPERATOR='" + userId + "'");
			sbSQL.append(",UPDDT=current timestamp ");
			if (delcnt == len){
				sbSQL.append(",UPDKBN=" + DefineReport.ValUpdkbn.DEL.getVal());
			}
			sbSQL.append(",SENDFLG=" + DefineReport.Values.SENDFLG_UN.getVal());
			sbSQL.append(" WHERE ");
			sbSQL.append(sqlWhere);

			if (DefineReport.ID_DEBUG_MODE)	System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

			sqlList.add(sbSQL.toString());
			prmList.add(paramData);
			lblList.add("プライスカード発行トラン");
		}

		return sbSQL.toString();
	}

	/**
	 * プライスカード発行・枚数トランDELETE(倫理削除)処理
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

		if(StringUtils.isEmpty(data.optString("F4"))){
			sqlWhere += "INPUTNO=null";
		}else{
			sqlWhere += "INPUTNO=?";
			paramData.add(data.optString("F4"));
		}

		// プライスカード発行トランの論理削除
		sbSQL.append("UPDATE INAMS.TRNPCARD ");
		sbSQL.append("SET ");
		sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.DEL.getVal());
		sbSQL.append(",SENDFLG=" + DefineReport.Values.SENDFLG_UN.getVal());
		sbSQL.append(",OPERATOR='" + userId + "'");
		sbSQL.append(",UPDDT=current timestamp ");
		sbSQL.append("WHERE ");
		sbSQL.append(sqlWhere);

		if (DefineReport.ID_DEBUG_MODE)	System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(paramData);
		lblList.add("プライスカード発行トラン");

		// プライスカード発行枚数トランの論理削除
		sbSQL = new StringBuffer();
		sbSQL.append("UPDATE INAMS.TRNPCARDSU ");
		sbSQL.append("SET ");
		sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.DEL.getVal());
		sbSQL.append(",SENDFLG=" + DefineReport.Values.SENDFLG_UN.getVal());
		sbSQL.append(",OPERATOR='" + userId + "'");
		sbSQL.append(",UPDDT=current timestamp ");
		sbSQL.append("WHERE ");
		sbSQL.append(sqlWhere);

		if (DefineReport.ID_DEBUG_MODE)	System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(paramData);
		lblList.add("プライスカード発行枚数トラン");

		return sbSQL.toString();
	}

	/**
	 * SEQ情報取得処理(No)
	 *
	 * @throws Exception
	 */
	public String getInput_SEQ() {
		// 関連情報取得
		ItemList iL = new ItemList();
		String sqlColCommand = "VALUES NEXTVAL FOR INAMS.SEQ001";
		@SuppressWarnings("static-access")
		JSONArray array = iL.selectJSONArray(sqlColCommand, null, Defines.STR_JNDI_DS);
		String value = "";
		if(array.size() > 0){
			value = array.optJSONObject(0).optString("1");
		}
		return value;
	}

	private String createCommand() {
		// ログインユーザー情報取得
		User userInfo = getUserInfo();
		if(userInfo==null){
			return "";
		}

		String szInputno		= getMap().get("INPUTNO");		// 入力No

		// DB検索用パラメータ
		ArrayList<String> paramData = new ArrayList<String>();
		String sqlWhere = "";

		if(StringUtils.isEmpty(szInputno)){
			sqlWhere += "INPUTNO=null AND ";
		}else{
			sqlWhere += "INPUTNO=? AND ";
			paramData.add(szInputno);
		}

		// タイトル情報(任意)設定
		List<String> titleList = new ArrayList<String>();

		// 一覧表情報
		StringBuffer sbSQL = new StringBuffer();
		sbSQL.append("SELECT ");
		sbSQL.append("COMAN ");														// F1	: コメント
		sbSQL.append(",SUBSTR(MST_YOYAKUDT,3) AS MST_YOYAKUDT ");					// F2	: マスタ予約日付
		sbSQL.append(",PCARDSZ ");													// F3	: プライスカードサイズ
		sbSQL.append(",INPUTNO ");													// F4	: 入力No
		sbSQL.append(",OPERATOR ");													// F5	: オペレーター
		sbSQL.append(",TO_CHAR(ADDDT,'YY/MM/DD') AS ADDDT ");						// F6	: 登録日
		sbSQL.append(",TO_CHAR(UPDDT,'YY/MM/DD') AS UPDDT ");						// F7	: 更新日
		sbSQL.append(",TO_CHAR(UPDDT,'YYYYMMDDHH24MISSNNNNNN') as HDN_UPDDT ");		// F8	: 更新日時
		sbSQL.append("FROM ");
		sbSQL.append("INAMS.TRNPCARD ");
		sbSQL.append("WHERE ");
		sbSQL.append(sqlWhere);
		sbSQL.append("UPDKBN = " + DefineReport.ValUpdkbn.NML.getVal() + " ");

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
