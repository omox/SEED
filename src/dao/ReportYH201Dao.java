package dao;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;

import authentication.bean.User;
import common.DefineReport;
import common.Defines;
import common.ItemList;
import common.JsonArrayData;
import common.MessageUtility;
import common.MessageUtility.Msg;
import common.MessageUtility.MsgKey;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 */
public class ReportYH201Dao extends ItemDao {

	/**
	 * インスタンスを生成します。
	 * @param source
	 */
	public ReportYH201Dao(String JNDIname) {
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

		String szKkkcd		 = getMap().get("KKKCD");				// 店グループ
		String sendBtnid	 = getMap().get("SENDBTNID");			// 呼出しボタン

		// パラメータ確認
		// 必須チェック
		if ( szKkkcd == null ) {
			System.out.println(super.getConditionLog());
			return "";
		}

		// タイトル情報(任意)設定
		List<String> titleList = new ArrayList<String>();
		StringBuffer sbSQL = new StringBuffer();

		if(!StringUtils.equals(DefineReport.Button.NEW.getObj(), sendBtnid)){
			sbSQL.append(DefineReport.ID_SQL_CMN_WEEK);
			sbSQL.append(" select");
			sbSQL.append(" T1.KKKKM");																	// F1 : 企画名
			sbSQL.append(", case");																		// F2 : 納入期間
			sbSQL.append("  when T1.UKESTDT = T1.UKEEDDT then T1.UKESTDT || W1.JWEEK");
			sbSQL.append("  else T1.UKESTDT || W1.JWEEK || '～' || T1.UKEEDDT || W2.JWEEK end ");
			sbSQL.append(", T1.KKKCD");																	// F3 : 企画No
			sbSQL.append(", T1.NNSTDT");																// F4 : 納入開始日
			sbSQL.append(", T1.NNEDDT");																// F5 : 納入終了日
			sbSQL.append(", T1.OPERATOR");																// F6 : オペレータ
			sbSQL.append(", T1.ADDDT");																	// F7 : 登録日
			sbSQL.append(", T1.UPDDT");																	// F8 : 更新日
			sbSQL.append(", T1.HDN_UPDDT");																// F9 : 排他チェック用：更新日(非表示)
			sbSQL.append(" from (select");
			sbSQL.append(" TO_CHAR(KKK.KKKCD) || ' - ' || KKK.KKKKM as KKKKM");
			sbSQL.append(", KKK.KKKCD");
			sbSQL.append(", TO_CHAR(TO_DATE('20' || right ('0' || KKK.NNSTDT, 6), 'YYYYMMDD'), 'YY/MM/DD') as UKESTDT");
			sbSQL.append(", DAYOFWEEK(TO_DATE('20' || right ('0' || KKK.NNSTDT, 6), 'YYYYMMDD')) as UKESTDT_WNUM");
			sbSQL.append(", TO_CHAR(TO_DATE('20' || right ('0' || KKK.NNEDDT, 6), 'YYYYMMDD'), 'YY/MM/DD') as UKEEDDT");
			sbSQL.append(", DAYOFWEEK(TO_DATE('20' || right ('0' || KKK.NNEDDT, 6), 'YYYYMMDD')) as UKEEDDT_WNUM");
			sbSQL.append(", right (KKK.NNSTDT, 6) as NNSTDT");
			sbSQL.append(", right (KKK.NNEDDT, 6) as NNEDDT");
			sbSQL.append(", KKK.OPERATOR");
			sbSQL.append(", TO_CHAR(KKK.ADDDT, 'yy/mm/dd') as ADDDT");
			sbSQL.append(", TO_CHAR(KKK.UPDDT, 'yy/mm/dd') as UPDDT");
			sbSQL.append(", TO_CHAR(KKK.UPDDT, 'YYYYMMDDHH24MISSNNNNNN') as HDN_UPDDT");
			sbSQL.append(" from INATK.HATYH_KKK KKK");
			sbSQL.append(" where KKK.UPDKBN = "+DefineReport.ValUpdkbn.NML.getVal()+" and KKK.KKKCD = "+szKkkcd+") T1");
			sbSQL.append(" left outer join WEEK W1 on T1.UKESTDT_WNUM = W1.CWEEK");
			sbSQL.append(" left outer join WEEK W2 on T1.UKEEDDT_WNUM = W2.CWEEK");
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

		// 保存用 List (検索情報)作成
		setWhere(new ArrayList<List<String>>());
		List<String> cells = new ArrayList<String>();

		// 共通箇所設定
		createCmnOutput(jad);

	}

	boolean isTest = true;

	/** SQLリスト保持用変数 */
	ArrayList<String> sqlList = new ArrayList<String>();
	/** SQLのパラメータリスト保持用変数 */
	ArrayList<ArrayList<String>> prmList = new ArrayList<ArrayList<String>>();
	/** SQLログ用のラベルリスト保持用変数 */
	ArrayList<String> lblList = new ArrayList<String>();

	/**
	 * 商品店グループINSERT/UPDATE処理
	 *
	 * @param dataArray
	 * @param map
	 * @param userInfo
	 */
	public String createSqlHatyuKKK(JSONArray dataArray, HashMap<String, String> map, User userInfo){
		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();

		String updateRows = "";										// 更新データ

		// ログインユーザー情報取得
		String userId	= userInfo.getId();							// ログインユーザー

		String kkkcd	 = "";										// 企画コード

		for (int i = 0; i < dataArray.size(); i++) {
			JSONObject data = dataArray.getJSONObject(i);
			if(data.isEmpty()){
				continue;
			}
			kkkcd += StringUtils.defaultIfEmpty(data.optString("F3"), "null");
		}

		StringBuffer sbSQL = new StringBuffer();
		sbSQL.append("update INATK.HATYH_KKK");
			sbSQL.append(" set");
			sbSQL.append(" OPERATOR = '"+userId+"'");
			sbSQL.append(", SENDFLG = 0 ");
			sbSQL.append(", UPDDT = current timestamp");
			sbSQL.append(" where KKKCD = ?");
		prmData.add(kkkcd);

		if (DefineReport.ID_DEBUG_MODE)	System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("予約発注企画");

		return sbSQL.toString();
	}

	/**
	 * 予約発注商品INSERT/UPDATE処理
	 *
	 * @param dataArray
	 * @param map
	 * @param userInfo
	 */
	public String createSqlHatyuSHN(JSONArray dataArray, HashMap<String, String> map, User userInfo){
		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();

		String updateRows = "";											// 更新データ

		// ログインユーザー情報取得
		String userId	= userInfo.getId();							// ログインユーザー

		StringBuffer sbSQL = new StringBuffer();


		for (int i = 0; i < dataArray.size(); i++) {
			JSONObject data = dataArray.getJSONObject(i);
			if(data.isEmpty()){
				continue;
			}
			sbSQL = new StringBuffer();
			prmData = new ArrayList<String>();
			sbSQL.append("update INATK.HATYH_SHN");
			sbSQL.append(" set");
			sbSQL.append(" NGFLG = ?");
			sbSQL.append(", SENDFLG = 0 ");
			sbSQL.append(", OPERATOR='" + userId + "'");
			sbSQL.append(", UPDDT=current timestamp ");
			sbSQL.append(" where KKKCD = ?");
			sbSQL.append(" and SHNCD = ?");
			prmData.add(data.optString("F3"));
			prmData.add(data.optString("F1"));
			prmData.add(data.optString("F2"));

			if (DefineReport.ID_DEBUG_MODE)	System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

			sqlList.add(sbSQL.toString());
			prmList.add(prmData);
			lblList.add("予約発注商品");
		}
		return sbSQL.toString();
	}

	/**
	 * 予約発注商品DELETE処理
	 *
	 * @param userId
	 * @param data
	 * @param tbl
	 *
	 * @throws Exception
	 */
	/*public JSONObject createSqlMSTSHN_DEL(JSONArray dataArray, HashMap<String, String> map, User userInfo){
		JSONObject result = new JSONObject();

		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();
		String values = "", names = "";

		StringBuffer sbSQLKey = new StringBuffer();
		List<String> keyColList = new ArrayList<String>();
		List<String> keyValList = new ArrayList<String>();

		// ログインユーザー情報取得
		String userId	= userInfo.getId();							// ログインユーザー

		// 共通固定値情報
		MSTSHNLayout[] keys = new MSTSHNLayout[]{MSTSHNLayout.TOROKUMOTO, MSTSHNLayout.UPDKBN, MSTSHNLayout.SENDFLG, MSTSHNLayout.OPERATOR, MSTSHNLayout.ADDDT, MSTSHNLayout.UPDDT};

		// data情報取得
		for (int i = 0; i < dataArray.size(); i++) {
			String col = keyColList.get(i);
			String val = keyValList.get(i);
			if(StringUtils.isEmpty(val)){
				values += ", null";
			}else{
				if(false){
					values += ", '"+val+"'";
				}else{
					prmData.add(val);
					values += ", ?";
				}
			}
			names  += ", "+col;
		}
		values = "("+StringUtils.removeStart(values, ",")+")";
		names  = StringUtils.removeStart(names, ",");

		// 条件文など取得
		String szTable = "INATK.HATYH_SHN";

		StringBuffer sbSQL;
		sbSQL = new StringBuffer();
		sbSQL.append("merge into "+szTable+" as T");
		sbSQL.append(" using (select");
		sbSQL.append("  cast(T1.KKKCD as SMALLINT) as KKKCD");				// F1 : 企画コード
		sbSQL.append(" ,cast(T1.SHNCD as CHARACTER(14)) as SHNCD");			// F2 : 商品コード
		sbSQL.append(" ,cast(T1.UPDKBN as SMALLINT) as UPDKBN");			// F3: 更新区分
		sbSQL.append(" ,cast(T1.OPERATOR as VARCHAR(20)) as OPERATOR");		// F4: オペレータ
		sbSQL.append(" ,current timestamp as UPDDT");						// F5: 更新日
		sbSQL.append(" from (values"+values+") as T1(");
		sbSQL.append("  KKKCD");
		sbSQL.append(" ,SHNCD");
		sbSQL.append(" ,CATALGNO");
		sbSQL.append(" ,HTDT");
		sbSQL.append(" ,UKESTDT");
		sbSQL.append(" ,UKEEDDT");
		sbSQL.append(" ,TENISTDT");
		sbSQL.append(" ,TENIEDDT");
		sbSQL.append(" ,YOTEISU");
		sbSQL.append(" ,GENDOSU");
		sbSQL.append(" ,NGFLG");
		sbSQL.append(" ,UPDKBN");
		sbSQL.append(" ,SENDFLG");
		sbSQL.append(" ,OPERATOR");
		sbSQL.append(" ,ADDDT");
		sbSQL.append(" ,UPDDT");
		sbSQL.append(" )) as RE on (T.KKKCD = RE.KKKCD and T.SHNCD = RE.SHNCD)");
		sbSQL.append(" when matched then ");
		sbSQL.append(" update set");
		sbSQL.append("  UPDKBN=RE.UPDKBN");
		sbSQL.append(" ,OPERATOR=RE.OPERATOR");					// オペレータ
		sbSQL.append(" ,UPDDT=current timestamp");				// 更新日

		System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("予約発注商品");
		return result;
	}*/

	/**
	 * 更新処理実行
	 * @return
	 *
	 * @throws Exception
	 */
	private JSONObject updateData(HashMap<String, String> map, User userInfo) throws Exception {

		// パラメータ確認
		JSONArray dataArray			 = JSONArray.fromObject(map.get("DATA"));		// 対象情報
		JSONArray dataArrayShn		 = JSONArray.fromObject(map.get("DATA_SHN"));	// 対象情報
		JSONObject option		 = new JSONObject();

		// ログインユーザー情報取得
		String userId	= userInfo.getId();								// ログインユーザー

		// SQL発行：予約発注_企画
		if(dataArray.size() > 0){
			this.createSqlHatyuKKK(dataArray, map, userInfo);

		}
		// SQL発行：予約発注_商品
		if(dataArrayShn.size() > 0){
			this.createSqlHatyuSHN(dataArrayShn, map, userInfo);

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
	private JSONObject deleteData(HashMap<String, String> map, User userInfo) throws Exception {
		// パラメータ確認
		JSONArray dataArray = JSONArray.fromObject(map.get("DATA"));	// 対象情報

		JSONObject msgObj = new JSONObject();
		JSONArray msg = new JSONArray();

		// ログインユーザー情報取得
		String userId	= userInfo.getId();								// ログインユーザー

		// 更新情報
		String values = "";
		for (int i = 0; i < dataArray.size(); i++) {
			JSONObject data = dataArray.getJSONObject(i);
			if(data.isEmpty()){
				continue;
			}

			values += data.optString("F1");
			values += data.optString("F2");

		}
		values = StringUtils.removeStart(values, ",");

		if(values.length()==0){
			msgObj.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00002.getVal()));
			return msgObj;
		}

		String szKkkcd		 = "";							// 企画コード
		String szShncd		 = "";							// 商品コード

		// 基本INSERT/UPDATE文
		StringBuffer sbSQL;
		ArrayList<String> prmData;

		// 商品店グループマスタ
		sbSQL = new StringBuffer();
		prmData = new ArrayList<String>();
		sbSQL.append("update INATK.HATYH_SHN set UPDKBN = "+DefineReport.ValUpdkbn.DEL.getVal()+" where KKKCD = ? and SHNCD = ?");
		prmData.add(szKkkcd);
		prmData.add(szShncd);
		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("予約発注商品");

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
				msgObj.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E10000.getVal()));
			}else{
				msgObj.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00002.getVal()));
			}
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
		JSONArray dataArray = JSONArray.fromObject(map.get("DATA"));		// 更新情報
		String outobj		= map.get(DefineReport.ID_PARAM_OBJ);			// 実行ボタン

		JSONArray msg = new JSONArray();

		// 入力値を取得
		//JSONObject data = dataArray.getJSONObject(0);

		String sendBtnid	= map.get("SENDBTNID");							// 呼出しボタン

		// チェック処理
		// 対象件数チェック

		/*if(dataArray.size() == 0 || dataArray.getJSONObject(0).isEmpty()){
			msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
			return msg;
		}*/


		// 入力制限：税区分が1、0の場合、税率区分の入力必須
		/*if(StringUtils.equals(data.optString("F5"), "0")
		|| StringUtils.equals(data.optString("F5"), "1")){
			if(StringUtils.equals(data.getString("F8"),DefineReport.Values.NONE.getVal())){
				msg.add(MessageUtility.getMsg("税率区分の設定を行ってください。"));
				return msg;
			}
		}*/
		// 各データ行チェック
//		if(!outobj.equals(DefineReport.Button.DELETE.getObj())) {
//			FieldType fieldType = FieldType.GRID;
//			for (int i = 0; i < dataArray.size(); i++) {
//				JSONObject data = dataArray.getJSONObject(i);
//				String[] addParam = {Integer.toString(i+1)};
//				// 比較対象店舗必須チェック
//				if(!InputChecker.isNotNull(data.optString("F2"))){
//					msg.add(MessageUtility.getCheckNullMessage(DefineReport.Select.M_TENPO.getTxt(), fieldType, addParam));
//				}
//			}
//		}
		return msg;
	}
	/**
	 * チェック処理(削除時)
	 *
	 * @throws Exception
	 */
	@SuppressWarnings("static-access")
	public JSONArray checkDel(HashMap<String, String> map) {
		// パラメータ確認
		JSONArray dataArray = JSONArray.fromObject(map.get("DATA"));				// 対象情報（主要な更新情報）

		JSONArray msg		 = new JSONArray();

		String szTengpcd	 = map.get("TENGPCD");							// 店グループ
		String szGpkbn		 = map.get("GPKBN");							// グループ区分
		String szAreakbn	 = map.get("AREAKBN");							// エリア区分


		// 基本INSERT/UPDATE文
		ItemList iL					 = new ItemList();
		JSONArray dbDatas			 = new JSONArray();
		StringBuffer sbSQL;
		ArrayList<String> prmData	 = new ArrayList<String>();


		if(StringUtils.equals("1", szGpkbn)){
			// 部門紐付チェック:仕入グループ商品
			sbSQL = new StringBuffer();
			sbSQL.append("select SHNCD from INAMS.MSTSIRGPSHN where TENGPCD = "+szTengpcd+" and AREAKBN = "+szAreakbn+" fetch first 1 rows only");
			dbDatas = iL.selectJSONArray(sbSQL.toString(), null, Defines.STR_JNDI_DS);

			if(dbDatas.size() > 0 ){
				msg.add(MessageUtility.getMsg("仕入グループ商品で使用している為削除できません。"));
			}

			// 部門紐付チェック:仕入グループ商品_予約
			sbSQL = new StringBuffer();
			sbSQL.append("select SHNCD from INAMS.MSTSIRGPSHN_Y where TENGPCD = "+szTengpcd+" and AREAKBN = "+szAreakbn+" fetch first 1 rows only");
			dbDatas = iL.selectJSONArray(sbSQL.toString(), null, Defines.STR_JNDI_DS);
			if(dbDatas.size() > 0 ){
				msg.add(MessageUtility.getMsg("仕入グループ商品_予約で使用している為削除できません。"));
			}

		}else if(StringUtils.equals("2", szGpkbn)){
			// 部門紐付チェック:売価コントロール
			sbSQL = new StringBuffer();
			sbSQL.append("select SHNCD from INAMS.MSTBAIKACTL where TENGPCD = "+szTengpcd+" and AREAKBN = "+szAreakbn+" fetch first 1 rows only");
			dbDatas = iL.selectJSONArray(sbSQL.toString(), null, Defines.STR_JNDI_DS);
			if(dbDatas.size() > 0 ){
				msg.add(MessageUtility.getMsg("売価コントロールで使用している為削除できません。"));
			}

			// 部門紐付チェック:売価コントロール_予約
			sbSQL = new StringBuffer();
			sbSQL.append("select SHNCD from INAMS.MSTBAIKACTL_Y where TENGPCD = "+szTengpcd+" and AREAKBN = "+szAreakbn+" fetch first 1 rows only");
			dbDatas = iL.selectJSONArray(sbSQL.toString(), null, Defines.STR_JNDI_DS);
			if(dbDatas.size() > 0 ){
				msg.add(MessageUtility.getMsg("売価コントロール_予約で使用している為削除できません。"));
			}

		}else if(StringUtils.equals("3", szGpkbn)){
			// 部門紐付チェック:品揃えグループ
			sbSQL = new StringBuffer();
			sbSQL.append("select SHNCD from INAMS.MSTSHINAGP where TENGPCD = "+szTengpcd+" and AREAKBN = ? fetch first 1 rows only");
			dbDatas = iL.selectJSONArray(sbSQL.toString(), null, Defines.STR_JNDI_DS);
			if(dbDatas.size() > 0 ){
				msg.add(MessageUtility.getMsg("品揃グループで使用している為削除できません。"));
			}

			// 部門紐付チェック:品揃えグループ_予約
			sbSQL = new StringBuffer();
			sbSQL.append("select SHNCD from INAMS.MSTSHINAGP_Y where TENGPCD = "+szTengpcd+" and AREAKBN = ? fetch first 1 rows only");
			dbDatas = iL.selectJSONArray(sbSQL.toString(), null, Defines.STR_JNDI_DS);
			if(dbDatas.size() > 0 ){
				msg.add(MessageUtility.getMsg("品揃グループ_予約で使用している為削除できません。"));
			}
		}

		return msg;
	}
	/**
	 * SEQ情報取得処理(企画No)
	 *
	 * @throws Exception
	 */
	public String getKKK_SEQ() {
		// 関連情報取得
		ItemList iL = new ItemList();
		String sqlColCommand = "VALUES NEXTVAL FOR INAMS.SEQ006";
		@SuppressWarnings("static-access")
		JSONArray array = iL.selectJSONArray(sqlColCommand, null, Defines.STR_JNDI_DS);
		String value = "";
		if(array.size() > 0){
			value = array.optJSONObject(0).optString("1");
		}
		return value;
	}
}
