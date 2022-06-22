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
import common.MessageUtility.Msg;
import common.MessageUtility.MsgKey;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 */
public class Reportx172Dao extends ItemDao {

	/**
	 * インスタンスを生成します。
	 * @param source
	 */
	public Reportx172Dao(String JNDIname) {
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

		String szHsptn		 = getMap().get("HSPTN");				// 配送パターン
		String szTengpcd	 = getMap().get("TENGPCD");				// 店グループ
		String sendBtnid	 = getMap().get("SENDBTNID");			// 呼出しボタン

		// パラメータ確認
		// 必須チェック
		if (StringUtils.isEmpty(szHsptn)) {
			System.out.println(super.getConditionLog());
			return "";
		}
		ArrayList<String> paramData = new ArrayList<String>();
		// タイトル情報(任意)設定
		List<String> titleList = new ArrayList<String>();

		StringBuffer sbSQL = new StringBuffer();

		sbSQL.append("select");
		sbSQL.append(" HPTN.HSPTN");
		sbSQL.append(", HPTN.HSPTNKN");
		sbSQL.append(", case");
		sbSQL.append("  when HPTN.CENTERCD = 0 then null");
		sbSQL.append("  else HPTN.CENTERCD end as CENTERCD");
		sbSQL.append(", case");
		sbSQL.append("  when HPTN.YCENTERCD = 0 then null");
		sbSQL.append("  else HPTN.YCENTERCD end as YCENTERCD");
		sbSQL.append(", EHPTN.AREAKBN");
		sbSQL.append(", HSGP.HSGPCD");
		sbSQL.append(", HSGP.HSGPKN");
		sbSQL.append(", TO_CHAR(HPTN.ADDDT, 'yy/mm/dd')");
		sbSQL.append(", TO_CHAR(HPTN.UPDDT, 'yy/mm/dd')");
		sbSQL.append(", HPTN.OPERATOR");
		sbSQL.append(", AHSS.HSPTN");
		sbSQL.append(" from INAMS.MSTHSPTN HPTN");
		sbSQL.append(" left join (select * from INAMS.MSTAREAHSPTN where HSPTN = ? fetch first 1 rows only) EHPTN on HPTN.HSPTN = EHPTN.HSPTN");
		paramData.add(szHsptn);
		sbSQL.append(" left join INAMS.MSTHSGP HSGP on HSGP.HSGPCD = EHPTN.HSGPCD and EHPTN.AREAKBN = HSGP.AREAKBN");
		sbSQL.append(" left join (select HSPTN from INAMS.MSTAREAHSPTNSIR group by HSPTN) AHSS on HPTN.HSPTN = AHSS.HSPTN");
		sbSQL.append(" where HPTN.UPDKBN = "+DefineReport.ValUpdkbn.NML.getVal()+" and HPTN.HSPTN = ? ");
		paramData.add(szHsptn);
		sbSQL.append(" order by HPTN.HSPTN");
		setParamData(paramData);
		// オプション情報（タイトル）設定
		JSONObject option = new JSONObject();
		option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
		setOption(option);

		if (DefineReport.ID_DEBUG_MODE)	System.out.println(getClass().getSimpleName()+"[sql]"+sbSQL.toString());
		return sbSQL.toString();
	}

	/**
	 * 正情報取得処理
	 *
	 * @throws Exception
	 */
	public JSONArray getHSPTNData(HashMap<String, String> map) {
		String szHsptn		 = map.get("HSPTN");				// 配送パターン
		String szTengpcd	 = map.get("TENGPCD");				// 店グループ
		String sendBtnid	 = map.get("SENDBTNID");			// 呼出しボタン

		ArrayList<String> paramData = new ArrayList<String>();

		StringBuffer sbSQL = new StringBuffer();

		if(DefineReport.Button.SEL_CHANGE.getObj().equals(sendBtnid)){

			if(StringUtils.isNotEmpty(szTengpcd)){
				sbSQL.append("select");
				sbSQL.append(" HPTN.HSPTN as F1");
				sbSQL.append(", HPTN.HSPTNKN as F2");
				sbSQL.append(", case");
				sbSQL.append("  when HPTN.CENTERCD = 0 then null");
				sbSQL.append("  else HPTN.CENTERCD end as F3");
				sbSQL.append(", case");
				sbSQL.append("  when HPTN.YCENTERCD = 0 then null");
				sbSQL.append("  else HPTN.YCENTERCD end as F4");
				sbSQL.append(", EHPTN.AREAKBN as F5");
				sbSQL.append(", HSGP.HSGPCD as F6");
				sbSQL.append(", HSGP.HSGPKN as F7");
				sbSQL.append(", TO_CHAR(HPTN.ADDDT, 'yy/mm/dd') as F8");
				sbSQL.append(", TO_CHAR(HPTN.UPDDT, 'yy/mm/dd') as F9");
				sbSQL.append(", HPTN.OPERATOR as F10");
				sbSQL.append(" from INAMS.MSTHSPTN HPTN");
				sbSQL.append(" left join (select * from INAMS.MSTAREAHSPTN where TENGPCD = "+szTengpcd+") EHPTN on HPTN.HSPTN = EHPTN.HSPTN");
				sbSQL.append(" left join INAMS.MSTHSGP HSGP on HSGP.HSGPCD = EHPTN.HSGPCD");
				sbSQL.append(" where HPTN.UPDKBN = "+DefineReport.ValUpdkbn.NML.getVal()+" and HPTN.HSPTN = " + szHsptn);
				sbSQL.append(" order by HPTN.HSPTN");

			}else {
				sbSQL.append("select");
				sbSQL.append(" HPTN.HSPTN as F1");
				sbSQL.append(", HPTN.HSPTNKN as F2");
				sbSQL.append(", case");
				sbSQL.append("  when HPTN.CENTERCD = 0 then null");
				sbSQL.append("  else HPTN.CENTERCD end as F3");
				sbSQL.append(", case");
				sbSQL.append("  when HPTN.YCENTERCD = 0 then null");
				sbSQL.append("  else HPTN.YCENTERCD end as F4");
				sbSQL.append(", null as F5");
				sbSQL.append(", null as F6");
				sbSQL.append(", null as F7");
				sbSQL.append(", TO_CHAR(HPTN.ADDDT, 'yy/mm/dd') as F8");
				sbSQL.append(", TO_CHAR(HPTN.UPDDT, 'yy/mm/dd') as F9");
				sbSQL.append(", HPTN.OPERATOR as F10");
				sbSQL.append(" from INAMS.MSTHSPTN HPTN");
				sbSQL.append(" where HPTN.UPDKBN = "+DefineReport.ValUpdkbn.NML.getVal()+" and HPTN.HSPTN = " + szHsptn);
				sbSQL.append(" order by HPTN.HSPTN");
			}
		}

		ItemList iL = new ItemList();
		@SuppressWarnings("static-access")
		JSONArray array = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

		return array;
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
	 * 配送パターンマスタ、エリア別配送パターンマスタINSERT/UPDATE処理
	 *
	 * @param data
	 * @param map
	 * @param userInfo
	 */
	public String createSqlHSPTN(JSONObject data, HashMap<String, String> map, User userInfo){

		StringBuffer sbSQL			= new StringBuffer();
		//JSONArray dataArrayHSPTN	= JSONArray.fromObject(map.get("DATA_HSPTN"));		// 更新情報(予約発注_納品日)
		JSONArray dataArrayEHSPTN	= JSONArray.fromObject(map.get("DATA_AHSPTN"));		// 更新情報(予約発注_納品日)

		// ログインユーザー情報取得
		String userId	= userInfo.getId();							// ログインユーザー

		ArrayList<String> prmData = new ArrayList<String>();
		Object[] valueData = new Object[]{};
		String values = "";

		int maxField = 4;		// Fxxの最大値
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

		// 生活応援_部門の登録・更新
		sbSQL = new StringBuffer();

		sbSQL.append(" merge into INAMS.MSTHSPTN as T using (select");
		sbSQL.append(" HSPTN");														// 配送パターン
		sbSQL.append(", HSPTNKN");													// 配送パターン名称
		sbSQL.append(", CENTERCD");													// センターコード
		sbSQL.append(", YCENTERCD");												// 横持先センターコード
		sbSQL.append(", "+DefineReport.ValUpdkbn.NML.getVal()+" AS UPDKBN");		// 更新区分
		sbSQL.append(", 0 as SENDFLG");												// 送信フラグ
		sbSQL.append(", '"+userId+"' AS OPERATOR ");								// オペレーター
		sbSQL.append(", current timestamp AS ADDDT ");								// 登録日
		sbSQL.append(", current timestamp AS UPDDT ");								// 更新日
		sbSQL.append(" from (values "+StringUtils.join(valueData, ",")+") as T1(NUM, ");
		sbSQL.append(" HSPTN");
		sbSQL.append(", HSPTNKN");
		sbSQL.append(", CENTERCD");
		sbSQL.append(", YCENTERCD");
		sbSQL.append(" ))as RE on (T.HSPTN = RE.HSPTN)");
		sbSQL.append(" when matched then update set");
		sbSQL.append(" HSPTN=RE.HSPTN");
		sbSQL.append(", HSPTNKN=RE.HSPTNKN");
		sbSQL.append(", CENTERCD=RE.CENTERCD");
		sbSQL.append(", YCENTERCD=RE.YCENTERCD");
		sbSQL.append(", UPDKBN=RE.UPDKBN");
		sbSQL.append(", SENDFLG=RE.SENDFLG");
		sbSQL.append(", OPERATOR=RE.OPERATOR");
		sbSQL.append(", ADDDT = case when NVL(UPDKBN, 0) = 1 then RE.ADDDT else ADDDT end");
		sbSQL.append(", UPDDT=RE.UPDDT");
		sbSQL.append(" when not matched then insert (");
		sbSQL.append(" HSPTN");
		sbSQL.append(", HSPTNKN");
		sbSQL.append(", CENTERCD");
		sbSQL.append(", YCENTERCD");
		sbSQL.append(", UPDKBN");
		sbSQL.append(", SENDFLG");
		sbSQL.append(", OPERATOR");
		sbSQL.append(", ADDDT");
		sbSQL.append(", UPDDT");
		sbSQL.append(") values (");
		sbSQL.append(" RE.HSPTN");
		sbSQL.append(", RE.HSPTNKN");
		sbSQL.append(", RE.CENTERCD");
		sbSQL.append(", RE.YCENTERCD");
		sbSQL.append(", RE.UPDKBN");
		sbSQL.append(", RE.SENDFLG");
		sbSQL.append(", RE.OPERATOR");
		sbSQL.append(", RE.ADDDT");
		sbSQL.append(", RE.UPDDT");
		sbSQL.append(")");

		if (DefineReport.ID_DEBUG_MODE)	System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("配送パターン");

		// クリア
		prmData = new ArrayList<String>();
		valueData = new Object[]{};
		values = "";

		maxField = 6;		// Fxxの最大値
		int len = dataArrayEHSPTN.size();
		for (int i = 0; i < len; i++) {
			JSONObject dataT = dataArrayEHSPTN.getJSONObject(i);
			if(! dataT.isEmpty()){
				for (int k = 1; k <= maxField; k++) {
					String key = "F" + String.valueOf(k);

					if(k == 1){
						values += String.valueOf(0 + 1);

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

				// エリア配送パターンの登録・更新
				sbSQL = new StringBuffer();
				sbSQL.append(" merge into INAMS.MSTAREAHSPTN as T using (select");
				sbSQL.append(" HSPTN");														// 配送パターン
				sbSQL.append(", TENGPCD");													// 店グループ
				sbSQL.append(", HSGPCD");													// 配送グループ
				sbSQL.append(", AREAKBN");													// エリア区分
				sbSQL.append(", CENTERCD");													// センターコード
				sbSQL.append(", YCENTERCD");												// 横持先センターコード
				sbSQL.append(", 0 as SENDFLG");												// 送信フラグ
				sbSQL.append(", '"+userId+"' AS OPERATOR ");								// オペレーター
				sbSQL.append(", current timestamp AS ADDDT ");								// 登録日
				sbSQL.append(", current timestamp AS UPDDT ");								// 更新日
				sbSQL.append(" from (values "+StringUtils.join(valueData, ",")+") as T1(NUM, ");
				sbSQL.append(" HSPTN");
				sbSQL.append(", TENGPCD");
				sbSQL.append(", HSGPCD");
				sbSQL.append(", AREAKBN");
				sbSQL.append(", CENTERCD");
				sbSQL.append(", YCENTERCD");
				sbSQL.append(" ))as RE on (T.HSPTN = RE.HSPTN and T.TENGPCD = RE.TENGPCD)");
				sbSQL.append(" when matched then update set");
				sbSQL.append(" HSPTN=RE.HSPTN");
				sbSQL.append(", TENGPCD=RE.TENGPCD");
				sbSQL.append(", HSGPCD=RE.HSGPCD");
				sbSQL.append(", AREAKBN=RE.AREAKBN");
				sbSQL.append(", CENTERCD=RE.CENTERCD");
				sbSQL.append(", YCENTERCD=RE.YCENTERCD");
				sbSQL.append(", SENDFLG=RE.SENDFLG");
				sbSQL.append(", OPERATOR=RE.OPERATOR");
				sbSQL.append(", ADDDT=RE.ADDDT");
				sbSQL.append(", UPDDT=RE.UPDDT");
				sbSQL.append(" when not matched then insert (");
				sbSQL.append(" HSPTN");
				sbSQL.append(", TENGPCD");
				sbSQL.append(", HSGPCD");
				sbSQL.append(", AREAKBN");
				sbSQL.append(", CENTERCD");
				sbSQL.append(", YCENTERCD");
				sbSQL.append(", SENDFLG");
				sbSQL.append(", OPERATOR");
				sbSQL.append(", ADDDT");
				sbSQL.append(", UPDDT");
				sbSQL.append(") values (");
				sbSQL.append(" RE.HSPTN");
				sbSQL.append(", RE.TENGPCD");
				sbSQL.append(", RE.HSGPCD");
				sbSQL.append(", RE.AREAKBN");
				sbSQL.append(", RE.CENTERCD");
				sbSQL.append(", RE.YCENTERCD");
				sbSQL.append(", RE.SENDFLG");
				sbSQL.append(", RE.OPERATOR");
				sbSQL.append(", RE.ADDDT");
				sbSQL.append(", RE.UPDDT");
				sbSQL.append(")");

				if (DefineReport.ID_DEBUG_MODE)	System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

				sqlList.add(sbSQL.toString());
				prmList.add(prmData);
				lblList.add("エリア別配送パターン");

				// クリア
				prmData = new ArrayList<String>();
				valueData = new Object[]{};
				values = "";
			}
		}
		return sbSQL.toString();
	}

	/**
	 * 配送パターンINSERT/UPDATE処理
	 *
	 * @param dataArray
	 * @param map
	 * @param userInfo
	 */
	@SuppressWarnings("static-access")
	public String createSqlHsptn(JSONArray dataArray, HashMap<String, String> map, User userInfo){
		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();

		String updateRows = "";										// 更新データ

		// ログインユーザー情報取得
		String userId	= userInfo.getId();							// ログインユーザー

		String hsptn	 = "";
		String adddt	 = "";							// 登録日(削除時仕様)

		StringBuffer sbSQL;
		ItemList iL			 = new ItemList();
		JSONArray dbDatas	 = new JSONArray();

		for (int i = 0; i < dataArray.size(); i++) {
			JSONObject data = dataArray.getJSONObject(i);
			if(data.isEmpty()){
				continue;
			}

			String values = "";

			if(isTest){

				// 登録設定
				/* 新規登録の場合							：timestamp
				 * 変更登録の場合							：ADDDT(検索値)
				 * PK重複の論理削除されたデータがある場合	：timestamp
				 * */
				hsptn =data.optString("F1");
				sbSQL = new StringBuffer();
				sbSQL.append("select");
				sbSQL.append(" case");
				sbSQL.append(" when UPDKBN = "+DefineReport.ValUpdkbn.DEL.getVal()+" then 'current timestamp'");
				sbSQL.append(" when UPDKBN = "+DefineReport.ValUpdkbn.NML.getVal()+" then TO_CHAR(ADDDT, '''yyyy-mm-dd''') end");
				sbSQL.append(" from INAMS.MSTHSPTN");
				sbSQL.append(" where HSPTN = "+hsptn);
				dbDatas = iL.selectJSONArray(sbSQL.toString(), null, Defines.STR_JNDI_DS);
				if(dbDatas.size() > 0 ){
					adddt = dbDatas.getJSONObject(0).getString("1")+",";
				}else{
					adddt = "current timestamp,";
				}

				values += StringUtils.defaultIfEmpty(data.optString("F1"), "null")+",";
				values += StringUtils.defaultIfEmpty("'"+data.optString("F2")+"'", "null")+",";
				values += StringUtils.defaultIfEmpty(data.optString("F3"), "null")+",";
				values += StringUtils.defaultIfEmpty(data.optString("F4"), "null")+",";
				values += DefineReport.ValUpdkbn.NML.getVal()+"," ;
				values += "null,";
				values += "'"+userId+"',";
				values += adddt;
				values += "current timestamp";

			}else{
				for (int j = 0; j < data.size(); j++) {
					values += ", ?";
				}
				values = StringUtils.removeStart(values, ",");

				for (String prm: values.split(",", 0)){
					prmData.add(prm);
				}
			}
			updateRows += ",("+values+")";
		}
		updateRows = StringUtils.removeStart(updateRows, ",");

		sbSQL = new StringBuffer();
		sbSQL.append(" merge into INAMS.MSTHSPTN as T using (select");
		sbSQL.append("  cast(T1.HSPTN as SMALLINT) as HSPTN");							// F1 : 配送パターン
		sbSQL.append(" ,cast(T1.HSPTNKN as VARCHAR(40)) as HSPTNKN");					// F2 : 配送パターン名称
		sbSQL.append(" ,cast(T1.CENTERCD as SMALLINT) as CENTERCD");					// F3 : センターコード
		sbSQL.append(" ,cast(T1.YCENTERCD as SMALLINT) as YCENTERCD");					// F4 : 横持先センターコード
		sbSQL.append(" ,cast(T1.UPDKBN as SMALLINT) as UPDKBN");						// F5 : 更新区分
		sbSQL.append(" ,cast(T1.SENDFLG as SMALLINT) as SENDFLG");						// F6 : 送信フラグ
		sbSQL.append(" ,cast(T1.OPERATOR as VARCHAR(20)) as OPERATOR");					// F7 : オペレータ
		sbSQL.append(" ,cast(T1.ADDDT as DATE) as ADDDT");								// F8 : 登録日
		sbSQL.append(" ,cast(T1.UPDDT as DATE) as UPDDT");								// F9 : 更新日
		sbSQL.append(" from (values "+updateRows+") as T1(");
		sbSQL.append("  HSPTN");
		sbSQL.append(" ,HSPTNKN");
		sbSQL.append(" ,CENTERCD");
		sbSQL.append(" ,YCENTERCD");
		sbSQL.append(" ,UPDKBN");
		sbSQL.append(" ,SENDFLG");
		sbSQL.append(" ,OPERATOR");
		sbSQL.append(" ,ADDDT");
		sbSQL.append(" ,UPDDT");
		sbSQL.append(" ))as RE on (T.HSPTN = RE.HSPTN)");
		sbSQL.append(" when matched then update set");
		sbSQL.append("  HSPTN=RE.HSPTN");
		sbSQL.append(" ,HSPTNKN=RE.HSPTNKN");
		sbSQL.append(" ,CENTERCD=RE.CENTERCD");
		sbSQL.append(" ,YCENTERCD=RE.YCENTERCD");
		sbSQL.append(" ,UPDKBN=RE.UPDKBN");
		sbSQL.append(" ,SENDFLG=RE.SENDFLG");
		sbSQL.append(" ,OPERATOR=RE.OPERATOR");
		sbSQL.append(" ,ADDDT=RE.ADDDT");
		sbSQL.append(" ,UPDDT=RE.UPDDT");
		sbSQL.append(" when not matched then insert values (");
		sbSQL.append("  RE.HSPTN");
		sbSQL.append(" ,RE.HSPTNKN");
		sbSQL.append(" ,RE.CENTERCD");
		sbSQL.append(" ,RE.YCENTERCD");
		sbSQL.append(" ,RE.UPDKBN");
		sbSQL.append(" ,RE.SENDFLG");
		sbSQL.append(" ,RE.OPERATOR");
		sbSQL.append(" ,RE.ADDDT");
		sbSQL.append(" ,RE.UPDDT");
		sbSQL.append(")");

		if (DefineReport.ID_DEBUG_MODE)	System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("配送パターンマスタ");

		return sbSQL.toString();
	}

	/**
	 * エリア別配送パターンINSERT/UPDATE処理
	 *
	 * @param dataArray
	 * @param map
	 * @param userInfo
	 */
	public String createSqlAreahsptn(JSONArray dataArray, HashMap<String, String> map, User userInfo){
		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();

		String updateRows = "";											// 更新データ

		// ログインユーザー情報取得
		String userId	= userInfo.getId();							// ログインユーザー

		String adddt	 = "";											// 登録日

		StringBuffer sbSQL;

		for (int i = 0; i < dataArray.size(); i++) {
			JSONObject data = dataArray.getJSONObject(i);
			if(data.isEmpty()){
				continue;
			}

			String values = "";

			if(isTest){

				sbSQL = new StringBuffer();
				values += StringUtils.defaultIfEmpty(data.optString("F1"), "null")+",";
				values += StringUtils.defaultIfEmpty(data.optString("F2"), "null")+",";
				values += StringUtils.defaultIfEmpty(data.optString("F3"), "null")+",";
				values += StringUtils.defaultIfEmpty(data.optString("F4"), "null")+",";
				values += StringUtils.defaultIfEmpty(data.optString("F5"), "null")+",";
				values += StringUtils.defaultIfEmpty("'"+data.optString("F6")+"'", "null")+",";
				values += "null,";
				values += "'"+userId+"',";
				values += "current timestamp,";
				values += "current timestamp";

			}else{
				for (int j = 0; j < data.size(); j++) {
					values += ", ?";
				}
				values = StringUtils.removeStart(values, ",");

				for (String prm: values.split(",", 0)){
					prmData.add(prm);
				}
			}
			updateRows += ",("+values+")";
		}
		updateRows = StringUtils.removeStart(updateRows, ",");

		sbSQL = new StringBuffer();
		sbSQL.append(" merge into INAMS.MSTAREAHSPTN as T using (select");
		sbSQL.append("  cast(T1.HSPTN as SMALLINT) as HSPTN");							// F1 : 配送パターン
		sbSQL.append(" ,cast(T1.TENGPCD as SMALLINT) as TENGPCD");						// F2 : 店グループ
		sbSQL.append(" ,cast(T1.HSGPCD as SMALLINT) as HSGPCD");						// F3 : 配送グループ
		sbSQL.append(" ,cast(T1.AREAKBN as SMALLINT) as AREAKBN");						// F4 : エリア区分
		sbSQL.append(" ,cast(T1.CENTERCD as SMALLINT) as CENTERCD");					// F5 : センターコード
		sbSQL.append(" ,cast(T1.YCENTERCD as CHARACTER(3)) as YCENTERCD");				// F6 : 横持先センターコード
		sbSQL.append(" ,cast(T1.SENDFLG as SMALLINT) as SENDFLG");						// F7 : 送信フラグ
		sbSQL.append(" ,cast(T1.OPERATOR as VARCHAR(20)) as OPERATOR");					// F8 : オペレータ
		sbSQL.append(" ,cast(T1.ADDDT as DATE) as ADDDT");								// F9 : 登録日
		sbSQL.append(" ,cast(T1.UPDDT as DATE) as UPDDT");								// F10: 更新日
		sbSQL.append(" from (values "+updateRows+") as T1(");
		sbSQL.append("  HSPTN");
		sbSQL.append(" ,TENGPCD");
		sbSQL.append(" ,HSGPCD");
		sbSQL.append(" ,AREAKBN");
		sbSQL.append(" ,CENTERCD");
		sbSQL.append(" ,YCENTERCD");
		sbSQL.append(" ,SENDFLG");
		sbSQL.append(" ,OPERATOR");
		sbSQL.append(" ,ADDDT");
		sbSQL.append(" ,UPDDT");
		sbSQL.append(" ))as RE on (T.HSPTN = RE.HSPTN and T.TENGPCD = RE.TENGPCD)");
		sbSQL.append(" when matched then update set");
		sbSQL.append("  HSPTN=RE.HSPTN");
		sbSQL.append(" ,TENGPCD=RE.TENGPCD");
		sbSQL.append(" ,HSGPCD=RE.HSGPCD");
		sbSQL.append(" ,AREAKBN=RE.AREAKBN");
		sbSQL.append(" ,CENTERCD=RE.CENTERCD");
		sbSQL.append(" ,YCENTERCD=RE.YCENTERCD");
		sbSQL.append(" ,SENDFLG=RE.SENDFLG");
		sbSQL.append(" ,OPERATOR=RE.OPERATOR");
		sbSQL.append(" ,UPDDT=RE.UPDDT");
		sbSQL.append(" when not matched then insert values (");
		sbSQL.append("  RE.HSPTN");
		sbSQL.append(" ,RE.TENGPCD");
		sbSQL.append(" ,RE.HSGPCD");
		sbSQL.append(" ,RE.AREAKBN");
		sbSQL.append(" ,RE.CENTERCD");
		sbSQL.append(" ,RE.YCENTERCD");
		sbSQL.append(" ,RE.SENDFLG");
		sbSQL.append(" ,RE.OPERATOR");
		sbSQL.append(" ,RE.ADDDT");
		sbSQL.append(" ,RE.UPDDT");
		sbSQL.append(")");

		if (DefineReport.ID_DEBUG_MODE)	System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("エリア別配送パターンマスタ");

		return sbSQL.toString();
	}

	/**
	 * エリア別配送パターンINSERT/UPDATE処理
	 *
	 * @param dataArray
	 * @param map
	 * @param userInfo
	 */
	public String createDeleteSql(HashMap<String, String> map, User userInfo){
		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();

		String hsptn	 = map.get("HSPTN");							// 配送パターン

		StringBuffer sbSQL;

		sbSQL = new StringBuffer();
		sbSQL.append("delete from INAMS.MSTAREAHSPTN where HSPTN = ?");
		prmData.add(hsptn);

		if (DefineReport.ID_DEBUG_MODE)	System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("エリア別配送パターンマスタ");

		return sbSQL.toString();
	}

	/**
	 * 更新処理実行
	 * @return
	 *
	 * @throws Exception
	 */
	private JSONObject updateData(HashMap<String, String> map, User userInfo) throws Exception {

		// パラメータ確認
		JSONArray dataArrayHsptn	 = JSONArray.fromObject(map.get("DATA_HSPTN"));		// 対象情報
		JSONArray dataArrayAhsptn	 = JSONArray.fromObject(map.get("DATA_AHSPTN"));	// 対象情報
		JSONObject option			 = new JSONObject();

		// ログインユーザー情報取得
		String userId	= userInfo.getId();								// ログインユーザー

		//String bmoncd	 = map.get("BUMON");								// 入力部門コード
		/*String daicd	 = "";												// 入力大分類コード
		String adddt	 = "";												// 登録日
		String updflg	 = "";												// 更新フラグ
		String supdate	 = "";												// 新規登録フラグ
*/
		//String sendBtnid	 = getMap().get("SENDBTNID");					// 呼出しボタン


		// パラメータ確認
		// 必須チェック
		if (dataArrayHsptn.isEmpty() && dataArrayAhsptn.isEmpty()) {
			option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
			return option;
		}

		// 基本登録情報
		JSONObject data = dataArrayHsptn.getJSONObject(0);

		// エリア別配送パターン仕入先マスタ 削除
		this.createDeleteSql(map, userInfo);

		// 配送パターンマスタ、エリア別配送パターンマスタINSERT/UPDATE処理
		this.createSqlHSPTN(data, map, userInfo);

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

		String szHsptn = map.get("HSPTN");									// 配送パターン

		if(szHsptn.length()==0){
			msgObj.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00002.getVal()));
			return msgObj;
		}

		// 基本INSERT/UPDATE文
		StringBuffer sbSQL;
		ArrayList<String> prmData;

		// 配送パターン削除
		sbSQL = new StringBuffer();
		prmData = new ArrayList<String>();
		sbSQL.append(" UPDATE INAMS.MSTHSPTN SET ");
		sbSQL.append(" UPDKBN = " + DefineReport.ValUpdkbn.DEL.getVal());
		sbSQL.append(", SENDFLG = 0");
		sbSQL.append(", OPERATOR ='" + userId + "'");
		sbSQL.append(", UPDDT = current timestamp ");
		sbSQL.append(" WHERE HSPTN = ? ");

		prmData.add(szHsptn);
		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("配送パターンマスタ");

		//子要素の削除
		sbSQL = new StringBuffer();
		prmData = new ArrayList<String>();
		sbSQL.append("delete from INAMS.MSTAREAHSPTN where HSPTN = ?");
		prmData.add(szHsptn);
		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("エリア別配送パターンマスタ");

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
		// 関連情報取得
		ItemList iL = new ItemList();
		// パラメータ確認
		JSONArray dataArray			 = JSONArray.fromObject(map.get("DATA_HSPTN"));			// 更新情報
		JSONArray dataArray_AHSP_DEL = JSONArray.fromObject(map.get("DATA_AHSPTN_DEL"));	// 更新情報
		ArrayList<String> paramData	 = new ArrayList<String>();
		String sqlcommand			 = "";
		String outobj				 = map.get(DefineReport.ID_PARAM_OBJ);					// 実行ボタン

		JSONArray	msg		 = new JSONArray();
		MessageUtility	mu	 = new MessageUtility();

		// 入力値を取得
		JSONObject data = dataArray.getJSONObject(0);

		String sendBtnid	= map.get("SENDBTNID");			// 呼出しボタン

		// チェック処理
		// 新規登録重複チェック
		if(StringUtils.equals(DefineReport.Button.NEW.getObj(), sendBtnid)){
			if(dataArray.size() > 0){
				data = dataArray.getJSONObject(0);

				paramData	 = new ArrayList<String>();
				paramData.add(data.getString("F1"));
				sqlcommand = "select count(HSPTN) as VALUE from INAMS.MSTHSPTN where UPDKBN = "+DefineReport.ValUpdkbn.NML.getVal()+" and HSPTN = ?";

				@SuppressWarnings("static-access")
				JSONArray array = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
				if(array.size() > 0 && NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) > 0){
					JSONObject o = mu.getDbMessageObj("E00004", "配送パターン");
					msg.add(o);
					return msg;
				}
			}
		}

		// エリア別配送パターン削除時チェック
		for (int i = 0; i < dataArray_AHSP_DEL.size(); i++) {
			data = dataArray_AHSP_DEL.getJSONObject(i);
			if(data.isEmpty()){
				continue;
			}

			paramData	 = new ArrayList<String>();
			paramData.add(data.getString("F1"));
			paramData.add(data.getString("F2"));
			sqlcommand = "select count(*) as VALUE from INAMS.MSTAREAHSPTNSIR where HSPTN = ? and TENGPCD = ?";

			@SuppressWarnings("static-access")
			JSONArray array = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
			if(array.size() > 0 && NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) > 0){
				JSONObject o = mu.getDbMessageObj("EX1042", new String[]{});
				msg.add(o);
				return msg;
			}
		}
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

		String szHsptn = map.get("HSPTN");									// 配送パターン

		// 基本INSERT/UPDATE文
		StringBuffer		sbSQL	= new StringBuffer();
		ArrayList<String> prmData	 = new ArrayList<String>();
		JSONArray			msg		= new JSONArray();
		ItemList			iL		= new ItemList();
		JSONArray			dbDatas = new JSONArray();
		MessageUtility		mu		= new MessageUtility();

		// 基本登録情報
		//JSONObject data = dataArray.getJSONObject(0);

		// 配送パターン紐付チェック:配送パターン仕入先マスタ
		sbSQL = new StringBuffer();
		sbSQL.append("select SIRCD from INAMS.MSTHSPTNSIR where HSPTN = ? fetch first 1 rows only");
		prmData.add(szHsptn);
		dbDatas = iL.selectJSONArray(sbSQL.toString(), prmData, Defines.STR_JNDI_DS);
		if(dbDatas.size() > 0 ){
			msg.add(mu.getDbMessageObj("E00006", new String[]{}));
			return msg;
		}
		return msg;
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
