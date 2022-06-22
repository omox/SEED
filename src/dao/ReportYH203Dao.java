package dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

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
public class ReportYH203Dao extends ItemDao {

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
	public ReportYH203Dao(String JNDIname) {
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

	private String createCommand() {
		// ログインユーザー情報取得
		User userInfo = getUserInfo();
		if(userInfo==null){
			return "";
		}

		String szKkkcd		 = getMap().get("KKKCD"); 					// 企画No
		String szShncd		 = getMap().get("SHNCD");					// 商品No
		String szNndt		 = getMap().get("NNDT");					// 納品日

		// パラメータ確認
		// 必須チェック
		if ( szKkkcd == null || szShncd == null ) {
			System.out.println(super.getConditionLog());
			return "";
		}

		// タイトル情報(任意)設定
		List<String> titleList = new ArrayList<String>();

		// 一覧表情報
		StringBuffer sbSQL = new StringBuffer();

		if(!StringUtils.equals("合計", szNndt)){
			sbSQL.append(DefineReport.ID_SQL_CMN_WEEK);
			sbSQL.append(" select");
			sbSQL.append(" T1.KKKKM");
			sbSQL.append(", T1.SHNKN");
			sbSQL.append(", T1.KKKCD");
			sbSQL.append(", TRIM(T1.SHNCD)");
			sbSQL.append(", T1.NNSTDT_DSP || W1.JWEEK as TEXT");
			sbSQL.append(", T1.NNDT");
			sbSQL.append(", T1.HDN_UPDDT");
			sbSQL.append(", T1.ADDDT");
			sbSQL.append(", T1.UPDDT");
			sbSQL.append(", T1.OPERATOR");
			sbSQL.append(", T1.YOTEISU");
			sbSQL.append(", T1.GENDOSU");
			sbSQL.append(", T1.HTDT");
			sbSQL.append(" from (select");
			sbSQL.append(" TO_CHAR(HKKK.KKKCD) || ' - ' || NVL(HKKK.KKKKM, '') as KKKKM");
			sbSQL.append(", left (TRIM(HSHN.SHNCD), 4) || '-' || right (TRIM(HSHN.SHNCD), 4) || ' ' || NVL(SHN.SHNKN, '') as SHNKN");
			sbSQL.append(", HKKK.KKKCD");
			sbSQL.append(", HSHN.SHNCD");
			sbSQL.append(", TO_CHAR(NNDT.UPDDT,'YYYYMMDDHH24MISSNNNNNN') as HDN_UPDDT ");
			sbSQL.append(", TO_CHAR(TO_DATE('20' || right ('0' || NNDT.NNDT, 6), 'YYYYMMDD'), 'YY/MM/DD') as NNSTDT_DSP");
			sbSQL.append(", DAYOFWEEK(TO_DATE('20' || right ('0' || NNDT.NNDT, 6), 'YYYYMMDD')) as STARTDTW, right (NNDT.NNDT, 6) as NNDT");
			sbSQL.append(", TO_CHAR(HKKK.ADDDT, 'yy/mm/dd') as ADDDT");
			sbSQL.append(", TO_CHAR(HKKK.UPDDT, 'yy/mm/dd') as UPDDT");
			sbSQL.append(", HKKK.OPERATOR as OPERATOR");
			sbSQL.append(", NNDT.YOTEISU");
			sbSQL.append(", NNDT.GENDOSU");
			sbSQL.append(", HSHN.HTDT");
			sbSQL.append(" from INATK.HATYH_KKK HKKK");
			sbSQL.append(" left join INATK.HATYH_SHN HSHN on HSHN.KKKCD = HKKK.KKKCD");
			sbSQL.append(" left join INATK.HATYH_NNDT NNDT on NNDT.KKKCD = HKKK.KKKCD and NNDT.SHNCD = HSHN.SHNCD");
			sbSQL.append(" left join INAMS.MSTSHN SHN on SHN.SHNCD = HSHN.SHNCD");
			sbSQL.append(" where HKKK.UPDKBN = "+DefineReport.ValUpdkbn.NML.getVal()+" and HKKK.KKKCD = "+szKkkcd+" and HSHN.SHNCD = '"+szShncd+"' and NNDT.NNDT = "+szNndt+") T1");
			sbSQL.append(" left outer join WEEK W1 on T1.STARTDTW = W1.CWEEK");

		}else{
			sbSQL.append(DefineReport.ID_SQL_CMN_WEEK);
			sbSQL.append(" select");
			sbSQL.append(" T1.KKKKM");
			sbSQL.append(", T1.SHNKN");
			sbSQL.append(", T1.KKKCD");
			sbSQL.append(", TRIM(T1.SHNCD)");
			sbSQL.append(", T1.NNSTDT_DSP || W1.JWEEK || '～' || T1.NNEDDT_DSP || W2.JWEEK as TEXT");
			sbSQL.append(", null");
			sbSQL.append(", T1.HDN_UPDDT");
			sbSQL.append(", T1.ADDDT");
			sbSQL.append(", T1.UPDDT");
			sbSQL.append(", T1.OPERATOR");
			sbSQL.append(", T1.YOTEISU");
			sbSQL.append(", T1.GENDOSU");
			sbSQL.append(", T1.HTDT");
			sbSQL.append(" from (select");
			sbSQL.append(" TO_CHAR(HKKK.KKKCD) || ' - ' || NVL(MAX(HKKK.KKKKM), '') as KKKKM");
			sbSQL.append(", left(TRIM(HSHN.SHNCD), 4) || '-' ||  right(TRIM(HSHN.SHNCD), 4) || ' ' || NVL(MAX(SHN.SHNKN), '') as SHNKN");
			sbSQL.append(", HKKK.KKKCD");
			sbSQL.append(", HSHN.SHNCD");
			sbSQL.append(", TO_CHAR(TO_DATE(MIN(NNDT.NNDT), 'YYYYMMDD'), 'YY/MM/DD') as NNSTDT_DSP");
			sbSQL.append(", DAYOFWEEK(TO_DATE(MIN(NNDT.NNDT), 'YYYYMMDD')) as STARTDTW");
			sbSQL.append(", TO_CHAR(TO_DATE(MAX(NNDT.NNDT), 'YYYYMMDD'), 'YY/MM/DD') as NNEDDT_DSP");
			sbSQL.append(", DAYOFWEEK(TO_DATE(MAX(NNDT.NNDT), 'YYYYMMDD')) as ENDDTW");
			sbSQL.append(", TO_CHAR(MAX(HKKK.ADDDT), 'yy/mm/dd') as ADDDT");
			sbSQL.append(", TO_CHAR(MAX(HKKK.UPDDT), 'yy/mm/dd') as UPDDT");
			sbSQL.append(", MAX(HKKK.OPERATOR) as OPERATOR");
			sbSQL.append(", TO_CHAR(MAX(NNDT.UPDDT),'YYYYMMDDHH24MISSNNNNNN') as HDN_UPDDT");
			sbSQL.append(", MAX(NNDT.YOTEISU) as YOTEISU");
			sbSQL.append(", MAX(NNDT.GENDOSU) as GENDOSU");
			sbSQL.append(", MAX(HSHN.HTDT) as HTDT");
			sbSQL.append(" from INATK.HATYH_KKK HKKK");
			sbSQL.append(" left join INATK.HATYH_SHN HSHN on HSHN.KKKCD = HKKK.KKKCD");
			sbSQL.append(" left join INATK.HATYH_NNDT NNDT on NNDT.KKKCD = HKKK.KKKCD");
			sbSQL.append(" left join INAMS.MSTSHN SHN on SHN.SHNCD = HSHN.SHNCD");
			sbSQL.append(" where HKKK.UPDKBN = 0 and HKKK.KKKCD = "+szKkkcd+" and HSHN.SHNCD = '"+szShncd+"'");
			sbSQL.append(" group by HKKK.KKKCD, HSHN.SHNCD) T1");
			sbSQL.append(" left outer join WEEK W1 on T1.STARTDTW = W1.CWEEK");
			sbSQL.append(" left outer join WEEK W2 on T1.ENDDTW = W2.CWEEK");

		}

		// オプション情報（タイトル）設定
		JSONObject option = new JSONObject();
		option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
		setOption(option);

		if (DefineReport.ID_DEBUG_MODE)	System.out.println(getClass().getSimpleName()+"[sql]"+sbSQL.toString());
		return sbSQL.toString();
	}

	private void outputQueryList() {

		// 検索条件の加工クラス作成
		JsonArrayData jad = new JsonArrayData();
		jad.setJsonString(getJson());

	}

	/**
	 * チェック処理
	 *
	 * @throws Exception
	 */
	public JSONArray check(HashMap<String, String> map, User userInfo, String sysdate) {

		// パラメータ確認
		JSONArray	dataArray		= JSONArray.fromObject(map.get("DATA"));		// 更新情報(配送グループ)
		JSONArray	dataArrayTEN	= JSONArray.fromObject(map.get("DATA_TEN"));	// 更新情報(配送店グループ)
		String		sendBtnid		= map.get("SENDBTNID");							// 呼出しボタン

		// 格納用変数
		StringBuffer		sbSQL	= new StringBuffer();
		JSONArray			msg		= new JSONArray();
		ItemList			iL		= new ItemList();
		MessageUtility		mu		= new MessageUtility();
		JSONArray			dbDatas = new JSONArray();

		// チェック処理
		// 対象件数チェック
		if(dataArray.size() == 0 || dataArray.getJSONObject(0).isEmpty()){
			msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
			return msg;
		}

		// 基本登録情報
		JSONObject data = dataArray.getJSONObject(0);

		// 標準-商品コード
		// ①商品マスタに無い場合エラー
		String shncd = data.optString("F2");
		if(!this.checkMstExist(DefineReport.InpText.SHNCD.getObj(), shncd)){
			JSONObject o = mu.getDbMessageObj("E11046", new String[]{});
			msg.add(o);
			return msg;
		}

		// グリッドエラーチェック：店舗一覧
		ArrayList<JSONObject> record = new ArrayList<JSONObject>();
		HashSet<String> parameter_ = new HashSet<String>();
		for (int i = 0; i < dataArrayTEN.size(); i++) {
			data = dataArrayTEN.getJSONObject(i);

			// 店舗取得
			String val = data.optString("F4");
			if(StringUtils.isNotEmpty(val)){
				record.add(data);							// レコード数
				parameter_.add(val);						// 店コードード(HashSetにより重複なし状態にする。)
			}

			// 標準-店舗コード
			// ①基本マスタに無い場合エラー
			String tencd = data.optString("F4");
			if(!this.checkMstExist(DefineReport.InpText.TENCD.getObj(), tencd)){
				JSONObject o = mu.getDbMessageObj("E11046", new String[]{});
				msg.add(o);
				return msg;
			}
		}
		// 重複チェック
		if(record.size() != parameter_.size()){
			JSONObject o = MessageUtility.getDbMessageIdObj("E11141", new String[]{});
			msg.add(o);
			return msg;
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
		String		tengpcd		= map.get("TENGPCD");						// 配送店グループコード

		// 格納用変数
		StringBuffer		sbSQL	= new StringBuffer();
		JSONArray			msg		= new JSONArray();
		ItemList			iL		= new ItemList();
		JSONArray			dbDatas = new JSONArray();
		MessageUtility		mu		= new MessageUtility();

		// チェック処理
		// 対象件数チェック
		if(dataArray.size() == 0 || dataArray.getJSONObject(0).isEmpty()){
			msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
			return msg;
		}

		if(tengpcd.isEmpty() || tengpcd == null){
			msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
			return msg;
		}

		// 基本登録情報
		JSONObject data = dataArray.getJSONObject(0);

		// エリア別配送パターンテーブルにレコードが存在する場合エラー
		sbSQL.append("SELECT ");
		sbSQL.append("	 HSGPCD ");	// レコード件数
		sbSQL.append("FROM ");
		sbSQL.append("	INAMS.MSTAREAHSPTN ");
		sbSQL.append("WHERE ");
		sbSQL.append("	HSGPCD	= '" + data.optString("F1") + "' AND ");	// 入力された配送グループで検索
		sbSQL.append("	TENGPCD	= '" + tengpcd + "' ");	// 入力された配送店グループで検索

		dbDatas = iL.selectJSONArray(sbSQL.toString(), null, Defines.STR_JNDI_DS);

		if (dbDatas.size() >= 1){
			// エリア別配送パターンに存在している
			msg.add(mu.getDbMessageObj("E11036", new String[]{}));
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
		JSONArray			msg			= new JSONArray();

		// パラメータ確認
		// 必須チェック
		if ( dataArray.isEmpty() || dataArray.size() != 1 || dataArray.getJSONObject(0).isEmpty()) {
			option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
			return option;
		}

		// 基本登録情報
		JSONObject data = dataArray.getJSONObject(0);

		// 予約発注_商品、予約発注_納品日INSERT/UPDATE処理
		this.createSqlTEN(data,map,userInfo);

		// 排他チェック実行
		String targetTable = null;
		String targetWhere = null;
		ArrayList<String> targetParam = new ArrayList<String>();
		if(dataArray.size() > 0){
			targetTable = "INATK.HATYH_NNDT";
			targetWhere = "KKKCD = ? and SHNCD = ? and NNDT = ?";
			targetParam.add(data.optString("F1"));
			targetParam.add(data.optString("F2"));
			targetParam.add(data.optString("F3"));
			if(! super.checkExclusion(targetTable, targetWhere, targetParam, data.optString("F4"))){
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
		JSONArray	dataArray	= JSONArray.fromObject(map.get("DATA"));	// 対象情報（主要な更新情報）

		// パラメータ確認
		// 必須チェック
		if ( dataArray.isEmpty() || dataArray.size() != 1 || dataArray.getJSONObject(0).isEmpty()) {
			option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
			return option;
		}

		// 基本登録情報
		JSONObject data = dataArray.getJSONObject(0);

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
	 * 予約発注_納品日、予約発注_店舗INSERT/UPDATE処理
	 *
	 * @param data
	 * @param map
	 * @param userInfo
	 */
	public String createSqlTEN(JSONObject data, HashMap<String, String> map, User userInfo){

		StringBuffer	sbSQL		= new StringBuffer();
		JSONArray		dataArrayT	= JSONArray.fromObject(map.get("DATA_TEN"));	// 更新情報(予約発注_納品日)

		// ログインユーザー情報取得
		String userId	= userInfo.getId();							// ログインユーザー

		ArrayList<String> prmData = new ArrayList<String>();
		Object[] valueData = new Object[]{};
		String values = "";

		int maxField = 9;		// Fxxの最大値
		for (int k = 1; k <= maxField; k++) {
			String key = "F" + String.valueOf(k);

			if(k == 1){
				values += String.valueOf(0 + 1);

			}

			if(! ArrayUtils.contains(new String[]{"F4", "F5", "F6", "F7", "F8", "F9", "F10"}, key)){
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

		// 予約発注_納入日の登録・更新
		sbSQL = new StringBuffer();
		sbSQL.append("merge into INATK.HATYH_NNDT as T");
		sbSQL.append(" using (select");
		sbSQL.append(" KKKCD");															// F1	: 企画コード
		sbSQL.append(", SHNCD");														// F2	: 商品コード
		sbSQL.append(", NNDT");															// F3	: 納入日
		sbSQL.append(", 0 as SENDFLG");													// F4	: 送信フラグ
		sbSQL.append(", '"+userId+"' AS OPERATOR ");									// F4	: オペレーター
		sbSQL.append(", current timestamp AS ADDDT ");									// F5	: 登録日
		sbSQL.append(", current timestamp AS UPDDT ");									// F6	: 更新日
		sbSQL.append(" from (values"+StringUtils.join(valueData, ",")+") as T1(NUM");
		sbSQL.append(", KKKCD");
		sbSQL.append(", SHNCD");
		sbSQL.append(", NNDT");
		sbSQL.append(" )) as RE on (T.KKKCD = RE.KKKCD and T.SHNCD = RE.SHNCD and T.NNDT = RE.NNDT)");
		sbSQL.append(" when matched then ");
		sbSQL.append(" update set");
		sbSQL.append("  KKKCD=RE.KKKCD");
		sbSQL.append(", SHNCD=RE.SHNCD");
		sbSQL.append(", NNDT=RE.NNDT");
		sbSQL.append(", OPERATOR=RE.OPERATOR");
		sbSQL.append(", SENDFLG=RE.SENDFLG");
		sbSQL.append(", ADDDT=RE.ADDDT");
		sbSQL.append(", UPDDT=RE.UPDDT");
		sbSQL.append(" when not matched then insert (");
		sbSQL.append("  KKKCD");
		sbSQL.append(", SHNCD");
		sbSQL.append(", NNDT");
		sbSQL.append(", SENDFLG");
		sbSQL.append(", OPERATOR");
		sbSQL.append(", ADDDT");
		sbSQL.append(", UPDDT");
		sbSQL.append(") values (");
		sbSQL.append("  RE.KKKCD");
		sbSQL.append(", RE.SHNCD");
		sbSQL.append(", RE.NNDT");
		sbSQL.append(", RE.SENDFLG");
		sbSQL.append(", RE.OPERATOR");
		sbSQL.append(", RE.ADDDT");
		sbSQL.append(", RE.UPDDT");
		sbSQL.append(")");

		if (DefineReport.ID_DEBUG_MODE)	System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("予約発注_納入日");

		// クリア
		prmData = new ArrayList<String>();
		valueData = new Object[]{};
		values = "";

		maxField = 10;		// Fxxの最大値
		int len = dataArrayT.size();

		if(len > 0){
			// 店舗情報削除
			this.createDeleteSqlHATYH_TEN(data, map);
		}

		for (int i = 0; i < len; i++) {
			JSONObject dataT = dataArrayT.getJSONObject(i);
			if(! dataT.isEmpty()){
				for (int k = 1; k <= maxField; k++) {
					String key = "F" + String.valueOf(k);

					if(k == 1){
						values += String.valueOf(0 + 1);

					}

					if(! ArrayUtils.contains(new String[]{"F7", "F8", "F9", "F10"}, key)){
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

				// 予約発注_店舗の登録・更新
				sbSQL = new StringBuffer();
				sbSQL.append("merge into INATK.HATYH_TEN as T");
				sbSQL.append(" using (select");
				sbSQL.append("  KKKCD");													// F1	: 企画コード
				sbSQL.append(" ,SHNCD");													// F2	: 商品コード
				sbSQL.append(" ,NNDT");														// F3	: 納入日
				sbSQL.append(" ,TENCD");													// F4	: 店舗コード
				sbSQL.append(" ,INPUTDT");													// F5	: 入力日
				sbSQL.append(" ,HTSU");														// F6	: 発注数
				sbSQL.append(", "+DefineReport.ValUpdkbn.NML.getVal()+" AS UPDKBN");		// F7	: 更新区分
				sbSQL.append(", 0 as SENDFLG");												// F8	: 送信フラグ
				sbSQL.append(", '"+userId+"' AS OPERATOR ");								// F9	: オペレーター
				sbSQL.append(", current timestamp AS ADDDT ");								// F10	: 登録日
				sbSQL.append(", current timestamp AS UPDDT ");								// F11	: 更新日
				sbSQL.append(" from (values "+StringUtils.join(valueData, ",")+") as T1(NUM");
				sbSQL.append(", KKKCD");
				sbSQL.append(", SHNCD");
				sbSQL.append(", NNDT");
				sbSQL.append(", TENCD");
				sbSQL.append(", INPUTDT");
				sbSQL.append(", HTSU");
				sbSQL.append(" ))as RE on (T.KKKCD = RE.KKKCD and T.SHNCD = RE.SHNCD and T.NNDT = RE.NNDT and T.TENCD = RE.TENCD and T.INPUTDT = RE.INPUTDT)");
				sbSQL.append(" when matched then update set");
				sbSQL.append("  KKKCD=RE.KKKCD");
				sbSQL.append(", SHNCD=RE.SHNCD");
				sbSQL.append(", NNDT=RE.NNDT");
				sbSQL.append(", INPUTDT=RE.INPUTDT");
				sbSQL.append(", TENCD=RE.TENCD");
				sbSQL.append(", HTSU=RE.HTSU");
				sbSQL.append(",  SENDFLG=RE.SENDFLG");
				sbSQL.append(", OPERATOR=RE.OPERATOR");
				sbSQL.append(", ADDDT=RE.ADDDT");
				sbSQL.append(", UPDDT=RE.UPDDT");
				sbSQL.append(" when not matched then insert (");
				sbSQL.append("  KKKCD");
				sbSQL.append(", SHNCD");
				sbSQL.append(", NNDT");
				sbSQL.append(", INPUTDT");
				sbSQL.append(", TENCD");
				sbSQL.append(", HTSU");
				sbSQL.append(", SENDFLG");
				sbSQL.append(", OPERATOR");
				sbSQL.append(", ADDDT");
				sbSQL.append(", UPDDT");
				sbSQL.append(") values (");
				sbSQL.append("  RE.KKKCD");
				sbSQL.append(", RE.SHNCD");
				sbSQL.append(", RE.NNDT");
				sbSQL.append(", RE.INPUTDT");
				sbSQL.append(", RE.TENCD");
				sbSQL.append(", RE.HTSU");
				sbSQL.append(", RE.SENDFLG");
				sbSQL.append(", RE.OPERATOR");
				sbSQL.append(", RE.ADDDT");
				sbSQL.append(", RE.UPDDT");
				sbSQL.append(")");

				if (DefineReport.ID_DEBUG_MODE)	System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

				sqlList.add(sbSQL.toString());
				prmList.add(prmData);
				lblList.add("予約発注_店舗");

				// クリア
				prmData = new ArrayList<String>();
				valueData = new Object[]{};
				values = "";
			}
		}

		return sbSQL.toString();
	}

	/**
	 * 予約発注_店舗DELETE処理
	 *
	 * @param dataArray
	 * @param map
	 * @param userInfo
	 */
	public String createDeleteSqlHATYH_TEN(JSONObject data,HashMap<String, String> map){
		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();

		String kkkcd	 = data.optString("F1");
		String shncd	 = data.optString("F2");
		String nndt		 = data.optString("F3");
		String shoridt	 = map.get("SHORIDT");

		StringBuffer sbSQL;

		sbSQL = new StringBuffer();
		sbSQL.append("delete from INATK.HATYH_TEN where KKKCD = ? and SHNCD = ? and NNDT = ? and INPUTDT < ? ");
		prmData.add(kkkcd);
		prmData.add(shncd);
		prmData.add(nndt);
		prmData.add(shoridt);

		if (DefineReport.ID_DEBUG_MODE)	System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("予約発注_店舗");

		return sbSQL.toString();
	}

	/**
	 * マスタ情報取得処理
	 *
	 * @throws Exception
	 */
	public boolean checkMstExist(String outobj, String value) {
		// 関連情報取得
		ItemList iL = new ItemList();
		// 配列準備
		ArrayList<String> paramData = new ArrayList<String>();
		String sqlcommand = "";

		String tbl = "";
		String col = "";
		String rep = "";
		// 商品コード
		if (outobj.equals(DefineReport.InpText.SHNCD.getObj())){
			tbl="INAMS.MSTSHN";
			col="SHNCD";
		}

		// 店コード
		if (outobj.equals(DefineReport.InpText.TENCD.getObj())){
			tbl="INAMS.MSTTEN";
			col="TENCD";
		}

		if(tbl.length()>0&&col.length()>0){
			if(paramData.size() > 0 && rep.length() > 0 ){
				sqlcommand = DefineReport.ID_SQL_CHK_TBL.replace("@T", tbl).replaceAll("@C", col).replace("?", rep);
			}else{
				paramData.add(value);
				sqlcommand = DefineReport.ID_SQL_CHK_TBL.replace("@T", tbl).replaceAll("@C", col);
			}

			@SuppressWarnings("static-access")
			JSONArray array = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
			if(array.size() > 0 && NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) > 0){
				return true;
			}
		}
		return false;
	}


}
