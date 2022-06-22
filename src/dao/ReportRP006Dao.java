package dao;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.JDBCType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import authentication.bean.User;
import authentication.defines.Consts;
import common.DefineReport;
import common.DefineReport.DataType;
import common.Defines;
import common.InputChecker;
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
public class ReportRP006Dao extends ItemDao {

	/** SQLリスト保持用変数 */
	ArrayList<String> sqlList = new ArrayList<String>();
	/** SQLのパラメータリスト保持用変数 */
	ArrayList<ArrayList<String>> prmList = new ArrayList<ArrayList<String>>();
	/** SQLログ用のラベルリスト保持用変数 */
	ArrayList<String> lblList = new ArrayList<String>();

	// JQgrid用のSQL通常率パターン
	public final static String ID_SQL_TENBETUBRT_RP006_UPD = ""
			+ "with T1(IDX) as (select 1 from SYSIBM.SYSDUMMY1 union all select IDX + 1 from T1 where IDX < 400)"
			+ ", T3 as (select MAX(TEN.TENCD) as MAXTEN from INAMS.MSTTEN TEN inner join INAMS.MSTTENBMN TBN on TBN.TENCD = TEN.TENCD "
			+ "and TBN.BMNCD = ? and NVL(TEN.UPDKBN, 0) <> 1 and NVL(TBN.UPDKBN, 0) <> 1)"
			+ ", WK as (select TENRT_ARR as ARR, 5 as LEN"
			+ " from INATK.TOKRTPTN"
			+ " where BMNCD = ?"
			+ " and RTPTNNO = ?"
			+ " and UPDKBN = 0), ARRWK(IDX, BPRT, S, ARR, LEN) as (select 1, SUBSTR(ARR, 1, LEN), 1 + LEN, ARR, LEN from WK"
			+ " union all select IDX + 1, SUBSTR(ARR, S, LEN), S + LEN, ARR, LEN from ARRWK where S <= LENGTH(ARR))"
			+ " select"
			+ " right ('000' || T1.IDX, 3) as TENCD"														// F1 : 店番
			+ ", case when NVL(T2.MISEUNYOKBN, 9) = 9 then '' else T2.TENKN end as TENKN"					// F2 : 店舗名
			+ ", case when NVL(T2.MISEUNYOKBN, 9) = 9 then '' else case when TRIM(NVL(T4.BPRT, '')) <> '' then trim(char (int (T4.BPRT))) end end as BUNPAIRT"	// F3 : 分配率(通常率)
			+ ", case when NVL(T2.MISEUNYOKBN, 9) = 9 then 0 else 1 end as EDITFLG "						// EDITFLG : 0:不可 1:可
			+ " from T1,T3"
			+ " left join ARRWK T4 on T4.IDX = T1.IDX"
			+ " left join (select TEN.* from INAMS.MSTTEN TEN inner join INAMS.MSTTENBMN TBN on TBN.TENCD = TEN.TENCD and TBN.BMNCD = ? and NVL(TEN.UPDKBN, 0) <> 1 and NVL(TBN.UPDKBN, 0) <> 1) T2 on T1.IDX = T2.TENCD and NVL(UPDKBN, 0) <> 1"
			+ " where T1.IDX <= T3.MAXTEN"
			+ " order by T1.IDX";

	public final static String ID_SQL_TENBETUBRT_RP006_NEW = ""
			+ "with T1(IDX) as (select 1 from SYSIBM.SYSDUMMY1 union all select IDX + 1 from T1 where IDX < 400)"
			+ ", T3 as (select MAX(TEN.TENCD) as MAXTEN from INAMS.MSTTEN TEN inner join INAMS.MSTTENBMN TBN on TBN.TENCD = TEN.TENCD "
			+ "and TBN.BMNCD = ? and NVL(TEN.UPDKBN, 0) <> 1 and NVL(TBN.UPDKBN, 0) <> 1)"
			+ " select"
			+ " right ('000' || T1.IDX, 3) as TENCD"														// F1 : 店番
			+ ", case when NVL(T2.MISEUNYOKBN, 9) = 9 then '' else T2.TENKN end as TENKN"					// F2 : 店舗名
			+ ", case when NVL(T2.MISEUNYOKBN, 9) = 9 then '' else '0' end as BUNPAIRT"							// F3 : 分配率(通常率)
			+ ", case when NVL(T2.MISEUNYOKBN, 9) = 9 then 0 else 1 end as EDITFLG "						// EDITFLG : 0:不可 1:可
			+ " from T1,T3"
			+ " left join (select TEN.* from INAMS.MSTTEN TEN inner join INAMS.MSTTENBMN TBN on TBN.TENCD = TEN.TENCD and TBN.BMNCD = ? and NVL(TEN.UPDKBN, 0) <> 1 and NVL(TBN.UPDKBN, 0) <> 1) T2 on T1.IDX = T2.TENCD and NVL(UPDKBN, 0) <> 1"
			+ " where T1.IDX <= T3.MAXTEN"
			+ " order by T1.IDX";

	public final static String ID_SQL_TENBETUBRT_RP010 = ""
			/*+ "select"
			+ " right('000'||TENCD,3) as TENCD"																// F1 : 店番
			+ ", (case when MISEUNYOKBN = 9 then NULL when COUNT = 0 then NULL else TENKN end) as TENKN"	// F2 : 店舗名
			+ ", (INT(SUBSTR(TENURI_ARR, ((TENCD -1) * 9 + 1), 9))) as URIAGE"								// F3 : 売上(実績率)
			+ ", (INT(SUBSTR(TENTEN_ARR, ((TENCD -1) * 9 + 1), 9))) as TENSU"								// F4 : 点数(実績率)
			+ " from"
			+ " (select"
			+ " T1.TENCD"
			+ ", T1.MISEUNYOKBN"
			+ ", (select count(T2.TENCD) from INAMS.MSTTENBMN T2 where T2.TENCD=T1.TENCD and T2.BMNCD=?) as COUNT"
			+ ", T1.TENKN"
			+ ", (select T3.TENURI_ARR from INATK.TOKJRTPTN T3 where T3.BMNCD=? and T3.WWMMFLG=? and YYMM=? and DAICD=? and CHUCD=?)"
			+ ", (select T3.TENTEN_ARR from INATK.TOKJRTPTN T3 where T3.BMNCD=? and T3.WWMMFLG=? and YYMM=? and DAICD=? and CHUCD=?)"
			+ " from INAMS.MSTTEN T1"
			+ " where T1.TENCD <= 400"
			+ " and NVL(T1.UPDKBN, 0) = 0"
			+ " )"
			+ " order by TENCD";*/


			+ " with T1(IDX) as (select 1 from SYSIBM.SYSDUMMY1 union all select IDX + 1 from T1 where IDX < 400)"
			+ ", T3 as (select MAX(TEN.TENCD) as MAXTEN from INAMS.MSTTEN TEN inner join INAMS.MSTTENBMN TBN on TBN.TENCD = TEN.TENCD "
			+ "and TBN.BMNCD = ? and NVL(TEN.UPDKBN, 0) <> 1 and NVL(TBN.UPDKBN, 0) <> 1)"
			+ ", WK as (select"
			+ " TENTEN_ARR as ARR_TEN"
			+ ", TENURI_ARR as ARR_URI"
			+ ", 9 as LEN"
			+ " from INATK.TOKJRTPTN"
			+ " where BMNCD = ?"
			+ " and WWMMFLG = ?"
			+ " and YYMM = ?"
			+ " and DAICD = ?"
			+ " and CHUCD = ?)"
			+ ", ARRWK_TENSU(IDX, TENSU, S, ARR, LEN) as (select 1, SUBSTR(ARR_TEN, 1, LEN), 1 + LEN, ARR_TEN, LEN from WK union all select IDX + 1, SUBSTR(ARR, S, LEN), S + LEN, ARR, LEN from ARRWK_TENSU where S <= LENGTH(ARR))"
			+ ", ARRWK_URI(IDX, URI, S, ARR, LEN) as (select 1, SUBSTR(ARR_URI, 1, LEN), 1 + LEN, ARR_URI, LEN from WK union all select IDX + 1, SUBSTR(ARR, S, LEN), S + LEN, ARR, LEN from ARRWK_URI where S <= LENGTH(ARR))"
			+ " select"
			+ " right ('000' || T1.IDX, 3) as TENCD"													// F1 : 店番
			+ ", M1.TENKN"																				// F2 : 店舗名
			+ ", case when TRIM(T4.TENSU) = '' then null else int (TRIM(T4.TENSU)) end as TENSU"		// F3 : 売上(実績率)
			+ ", case when TRIM(T5.URI) = '' then null else int (TRIM(T5.URI)) end as URIAGE"			// F4 : 点数(実績率)
			+ " from T1,T3 left"
			+ " join ARRWK_TENSU T4 on T4.IDX = T1.IDX"
			+ " left join ARRWK_URI T5 on T5.IDX = T1.IDX"
			+ " left join INAMS.MSTTEN M1 on M1.TENCD = T1.IDX and NVL(M1.UPDKBN, 0) <> 1"
			+ " where T4.TENSU is not null and T1.IDX <= T3.MAXTEN order by T1.IDX";

	/**
	 * インスタンスを生成します。
	 * @param source
	 */
	public ReportRP006Dao(String JNDIname) {
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

	private String createCommand() {
		// ログインユーザー情報取得
		User userInfo = getUserInfo();
		if(userInfo==null){
			return "";
		}

		String szBmncd		= getMap().get("BMNCD");			// 部門
		String szRtptnno	= getMap().get("RTPTNNO");			// 通常率パターンNo.
		String szWwmmflg	= getMap().get("WWMMFLG");			// 週月フラグ
		String szYymm		= getMap().get("YYMM");				// 年月(週No.)
		String szDaicd		= getMap().get("DAICD");			// 大分類
		String szChucd		= getMap().get("CHUCD");			// 中分類
		String szJissekibun	= getMap().get("JISSEKIBUN");		// 実績分類
		String sendBtnid	= getMap().get("SENDBTNID");		// 呼出しボタン

		// パラメータ確認
		// 必須チェック
		if ( szBmncd == null || sendBtnid == null) {
			System.out.println(super.getConditionLog());
			return "";
		}

		StringBuffer sbSQL = new StringBuffer();

		if (DefineReport.Button.SEL_TENBETUBRT.getObj().equals(sendBtnid)) {
			// 実績率の場合
			sbSQL.append("select");
			sbSQL.append("  NULL as F1");																																// F1 : 部門(通常率パターン)
			sbSQL.append(", NULL as F2");																																// F2 : 通常率パターンNo.
			sbSQL.append(", NULL as F3");																																// F3 : 通常率パターン名称
			sbSQL.append(", right('00'||BMNCD, 2) as F4");																												// F4 : 部門(実績率パターン)
			sbSQL.append(", (right('000'||BMNCD, 3) || right('0'||WWMMFLG, 1) || right('0000'||YYMM, 4) || right('00'||DAICD, 2) || right('00'||CHUCD, 2)) as F5");		// F5 : 実績率パターンNo.
			sbSQL.append(",(case when "+szJissekibun+"=1 then BMNKN when "+szJissekibun+"=2 then DAIBRUIKN when "+szJissekibun+"=3 then CHUBRUIKN end) as F6");			// F6 : 実績率パターン名称
			sbSQL.append(" from");
			sbSQL.append(" (select");
			sbSQL.append(" T1.BMNCD");
			sbSQL.append(", T1.WWMMFLG");
			sbSQL.append(", T1.YYMM");
			sbSQL.append(", T1.DAICD");
			sbSQL.append(", T1.CHUCD");
			sbSQL.append(", (select T2.BMNKN from INAMS.MSTBMN T2 where T2.BMNCD = "+szBmncd+")");
			sbSQL.append(", (select T3.DAIBRUIKN from INAMS.MSTDAIBRUI T3 where T3.BMNCD = "+szBmncd+" and T3.DAICD ="+szDaicd+")");
			sbSQL.append(", (select T4.CHUBRUIKN from INAMS.MSTCHUBRUI T4 where T4.BMNCD = "+szBmncd+" and T4.DAICD ="+szDaicd+" and T4.CHUCD ="+szChucd+")");
			sbSQL.append("  from INATK.TOKJRTPTN T1");
			sbSQL.append("  where T1.BMNCD =" +szBmncd+"");
			sbSQL.append("  and T1.WWMMFLG =" +szWwmmflg+"");
			sbSQL.append("  and T1.YYMM =" +szYymm+"");
			sbSQL.append("  and T1.DAICD =" +szDaicd+"");
			sbSQL.append("  and T1.CHUCD =" +szChucd+"");
			sbSQL.append(" )");

		} else if (DefineReport.Button.NEW.getObj().equals(sendBtnid)) {
			// 通常率(新規)の場合
			sbSQL.append("select");
			sbSQL.append("  right('00'||BMNCD, 2) as F1");		// F1 : 部門(通常率パターン)
			sbSQL.append(", NULL as F2");						// F2 : 通常率パターンNo.
			sbSQL.append(", NULL as F3");						// F3 : 通常率パターン名称
			sbSQL.append(", NULL as F4");						// F4 : 部門(実績率パターン)
			sbSQL.append(", NULL as F5");						// F5 : 実績率パターンNo.
			sbSQL.append(", NULL as F6");						// F6 : 実績率パターン名称
			sbSQL.append("  from INATK.TOKRTPTN T1");
			sbSQL.append("  where T1.BMNCD =" +szBmncd+"");

		} else {
			// 通常率(変更・参照)の場合
			if ( szRtptnno == null) {
				System.out.println(super.getConditionLog());
				return "";
			}
			sbSQL.append("select");
			sbSQL.append("  right('00'||BMNCD, 2) as F1");		// F1 : 部門(通常率パターン)
			sbSQL.append(", right('000'||T1.RTPTNNO,3) as F2");	// F2 : 通常率パターンNo.
			sbSQL.append(", T1.RTPTNKN as F3");					// F3 : 通常率パターン名称
			sbSQL.append(", NULL as F4");						// F4 : 部門(実績率パターン)
			sbSQL.append(", NULL as F4");						// F5 : 実績率パターンNo.
			sbSQL.append(", NULL as F5");						// F6 : 実績率パターン名称
			sbSQL.append(" from INATK.TOKRTPTN T1");
			sbSQL.append(" where T1.BMNCD =" +szBmncd+"");
			sbSQL.append(" and T1.RTPTNNO =" +szRtptnno+"");

		}

		if (DefineReport.ID_DEBUG_MODE) System.out.println(getClass().getSimpleName()+"[sql]"+sbSQL.toString());
		return sbSQL.toString();
	}

	private void outputQueryList() {

		// 検索条件の加工クラス作成
		JsonArrayData jad = new JsonArrayData();
		jad.setJsonString(getJson());

		// 保存用 List (検索情報)作成
		setWhere(new ArrayList<List<String>>());

		// 共通箇所設定
		createCmnOutput(jad);

	}


	/**
	 * 更新処理
	 *
	 * @param request
	 * @param session
	 * @param map
	 * @param userInfo
	 * @return
	 */
	public JSONObject update(HttpServletRequest request,HttpSession session, HashMap<String, String> map, User userInfo) {

		String sysdate = (String)request.getSession().getAttribute(Consts.STR_SES_LOGINDT);

		// 更新情報チェック(基本JS側で制御)
		JSONObject msgObj = new JSONObject();
		JSONArray msg = this.check(map);

		// パラメータ確認
		JSONArray dataArray				= JSONArray.fromObject(map.get("DATA"));	// 更新情報
		JSONArray dataArrayRtptn	= JSONArray.fromObject(map.get("DATA_RTPTN"));	// 更新情報

		if(msg.size() > 0){
			msgObj.put(MsgKey.E.getKey(), msg);
			return msgObj;
		}

		// 更新処理
		try {
			msgObj = this.updateData(dataArray, dataArrayRtptn, userInfo, sysdate);
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
		JSONArray msg = this.check(map);

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


	boolean isTest = true;

	/**
	 * 更新処理実行
	 *
	 * @param map
	 * @param userInfo
	 * @return
	 * @throws Exception
	 */
	public JSONObject updateData(JSONArray dataArray, JSONArray dataArrayRtptn, User userInfo, String sysdate) throws Exception {
		JSONObject option = new JSONObject();
		JSONArray msg = new JSONArray();

//
//		// 必須チェック
//		if (dataArray.isEmpty() && dataArrayRtptn.isEmpty()) {
//			option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
//			return option;
//		}

		// 基本登録情報
		JSONObject data = dataArray.getJSONObject(0);

		// SQL発行：通常率パターン(登録)
		this.createSqlRtptn(data, dataArrayRtptn, userInfo);

		ArrayList<Integer> countList = new ArrayList<Integer>();
		if(sqlList.size() > 0){
			// 更新処理実行
			countList = super.executeSQLs(sqlList, prmList);
		}

		if(StringUtils.isEmpty(super.getMessage())){
			int count = 0;
			for (int i = 0; i < countList.size(); i++) {
				count += countList.get(i);
				if (DefineReport.ID_DEBUG_MODE) System.out.println(MessageUtility.getMessage(Msg.S00006.getVal(), new String[]{lblList.get(i), Integer.toString(countList.get(i))}));
			}
			if(count == 0){
				option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E10000.getVal()));
			}else{
				option.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00001.getVal()));
			}
		}else{
			option.put(MsgKey.E.getKey(), super.getMessage());
		}
		return option;
	}

	/**
	 * 通常率パターン INSERT/UPDATE処理のSQL作成
	 *
	 * @param dataArray
	 * @param map
	 * @param userInfo
	 * @return
	 */
	/*public String createSqlRtptn(JSONArray dataArray, JSONArray dataArrayRtptn, HashMap<String, String> map, User userInfo, String sysdate){

		String dbsysdate = CmnDate.dbDateFormat(sysdate);

		JSONObject option	 = new JSONObject();
		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();
		String values = "";
		String updateRows = "";											// 更新データ

		String obj = map.get(DefineReport.ID_PARAM_OBJ);
		JSONObject msgObj = new JSONObject();
		JSONArray msg = new JSONArray();

		// パラメータ確認
		int kryoColNum = 9;		// テーブル列数
		// ログインユーザー情報取得
		int userId	= userInfo.getCD_user();						// ログインユーザー
		values = "";
		// 更新情報
		for (int i = 1; i <= kryoColNum; i++) {
			String col = "F" + i;
			String val = dataArray.optJSONObject(0).optString(col);
			if (i==4) {												// F4 : 店分配率配列：
				val = "";
				for(int j=0; j < dataArrayRtptn.size(); j++){
					val += StringUtils.leftPad(dataArrayRtptn.optJSONObject(j).optString("F3"), 5, "0");
				}
			} else if (i==5) {										// F5 : 更新区分：
				val = "0";
			} else if (i==6) {										// F6 : 送信フラグ：
				val = "0";
			} else if (i==7) {										// F7 : オペレーター：
				val = ""+userId;
			} else if (i==8) {										// F8 : 登録日：
				val = dbsysdate;
			} else if (i==9) {										// F9 : 更新日：
				val = dbsysdate;
			}
			if (isTest) {
				if (i == 1) {
					values += "( '" + val + "'";					// F1 : 部門：
				} else if (i == 9) {
					values += ", '" + val + "')";					// F9 : 更新日：
				} else {
					values += ", '" + val + "'";					// F2 : 率パターンNo.：, F3 : 率パターン名称：
				}
			} else {
				prmData.add(val);
				values += ", ?";
			}
		}
		// 基本INSERT/UPDATE文
		StringBuffer sbSQL;
		// 更新SQL
		sbSQL = new StringBuffer();
				sbSQL = new StringBuffer();
				sbSQL.append("merge into INATK.TOKRTPTN as T using (select");
				sbSQL.append("  BMNCD");													// F1 : 部門：
				sbSQL.append(", RTPTNNO");													// F2 : 率パターンNo.：
				sbSQL.append(", RTPTNKN");													// F3 : 率パターン名称：
				sbSQL.append(", TENRT_ARR");												// F4 : 店分配率配列：
				sbSQL.append(", UPDKBN");													// F5 : 更新区分
				sbSQL.append(", SENDFLG");													// F6 : 送信フラグ
				sbSQL.append(", OPERATOR");													// F7 : オペレータ
				sbSQL.append(", ADDDT");													// F8 : 登録日
				sbSQL.append(", UPDDT");													// F9 : 更新日
				sbSQL.append(" from (values "+values+" ) as T1(");
				sbSQL.append("  BMNCD");													// F1 : 部門：
				sbSQL.append(", RTPTNNO");													// F2 : 率パターンNo.：
				sbSQL.append(", RTPTNKN");													// F3 : 率パターン名称：
				sbSQL.append(", TENRT_ARR");												// F4 : 店分配率配列：
				sbSQL.append(", UPDKBN");													// F5 : 更新区分
				sbSQL.append(", SENDFLG");													// F6 : 送信フラグ
				sbSQL.append(", OPERATOR");													// F7 : オペレータ
				sbSQL.append(", ADDDT");													// F8 : 登録日
				sbSQL.append(", UPDDT");													// F9 : 更新日
				sbSQL.append("))as RE on (T.BMNCD = RE.BMNCD and T.RTPTNNO = RE.RTPTNNO)");
				sbSQL.append(" when matched then update set");
				sbSQL.append("  BMNCD = RE.BMNCD");											// F1 : 部門：
				sbSQL.append(", RTPTNNO = RE.RTPTNNO");										// F2 : 率パターンNo.：
				sbSQL.append(", RTPTNKN = RE.RTPTNKN");										// F3 : 率パターン名称：
				sbSQL.append(", TENRT_ARR = RE.TENRT_ARR");									// F4 : 店分配率配列：
				sbSQL.append(", UPDKBN = RE.UPDKBN");										// F5 : 更新区分：
				sbSQL.append(", SENDFLG = RE.SENDFLG");										// F6 : 送信フラグ：
				sbSQL.append(", OPERATOR = RE.OPERATOR");									// F7 : オペレーター：
				sbSQL.append(", UPDDT = RE.UPDDT");											// F9 : 更新日：
				sbSQL.append(" when not matched then insert(");
				sbSQL.append("  BMNCD");													// F1 : 部門：
				sbSQL.append(", RTPTNNO");													// F2 : 率パターンNo.：
				sbSQL.append(", RTPTNKN");													// F3 : 率パターン名称：
				sbSQL.append(", TENRT_ARR");												// F4 : 店分配率配列：
				sbSQL.append(", UPDKBN");													// F5 : 更新区分：
				sbSQL.append(", SENDFLG");													// F6 : 送信フラグ：
				sbSQL.append(", OPERATOR");													// F7 : オペレーター：
				sbSQL.append(", ADDDT");													// F8 : 登録日：
				sbSQL.append(", UPDDT");													// F9 : 更新日：
				sbSQL.append(") values (");
				sbSQL.append("  RE.BMNCD");													// F1 : 部門：
				sbSQL.append(", RE.RTPTNNO");												// F2 : 率パターンNo.：
				sbSQL.append(", RE.RTPTNKN");												// F3 : 率パターン名称：
				sbSQL.append(", RE.TENRT_ARR");												// F4 : 店分配率配列：
				sbSQL.append(", RE.UPDKBN");												// F5 : 更新区分：
				sbSQL.append(", RE.SENDFLG");												// F6 : 送信フラグ：
				sbSQL.append(", RE.OPERATOR");												// F7 : オペレーター：
				sbSQL.append(", RE.ADDDT");													// F8 : 登録日：
				sbSQL.append(", RE.UPDDT");													// F9 : 更新日：
				sbSQL.append(")");

				System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

				sqlList.add(sbSQL.toString());
				prmList.add(prmData);
				lblList.add("通常率パターン");

//				// クリア
//				prmData = new ArrayList<String>();
//				valueData = new Object[]{};
//				values = "";

		return sbSQL.toString();


	}*/

	/**
	 * ランクマスタINSERT/UPDATE処理
	 *
	 * @param data
	 * @param map
	 * @param userInfo
	 */
	public String createSqlRtptn(JSONObject data, JSONArray dataArrayRtptn, User userInfo){
		StringBuffer sbSQL		= new StringBuffer();

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

				if(StringUtils.equals("F4", key)){
					val = "";
					for(int j=0; j < dataArrayRtptn.size(); j++){
						val += StringUtils.leftPad(dataArrayRtptn.optJSONObject(j).optString("F3"), 5, "0");
					}

					int len = (5*400) - val.length();
					if (len!=0) {
						val += String.format("%"+len+"s","").replace(" ", "0");
					}
				}
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

		// 通常率パターンの登録・更新
		sbSQL = new StringBuffer();
		sbSQL.append(" merge into INATK.TOKRTPTN as T using (select");
		sbSQL.append("  cast(T1.BMNCD as SMALLINT) as BMNCD");						// F1 : 部門
		sbSQL.append(" ,cast(T1.RTPTNNO as SMALLINT) as RTPTNNO");					// F2 : 率パターン№
		sbSQL.append(" ,cast(T1.RTPTNKN as VARCHAR(40)) as RTPTNKN");				// F3 : 率パターン名称
		sbSQL.append(" ,cast(T1.TENRT_ARR as VARCHAR(2000)) as TENRT_ARR");			// F4 : 店分配率配列
		sbSQL.append(", "+DefineReport.ValUpdkbn.NML.getVal()+" AS UPDKBN");		// 更新区分
		sbSQL.append(", 0 as SENDFLG");												// 送信フラグ
		sbSQL.append(", '"+userId+"' AS OPERATOR ");								// オペレーター
		sbSQL.append(", current timestamp AS ADDDT ");								// 登録日
		sbSQL.append(", current timestamp AS UPDDT ");								// 更新日
		sbSQL.append(" from (values "+StringUtils.join(valueData, ",")+") as T1(NUM, ");
		sbSQL.append("  BMNCD");
		sbSQL.append(" ,RTPTNNO");
		sbSQL.append(" ,RTPTNKN");
		sbSQL.append(" ,TENRT_ARR");
		sbSQL.append(" ))as RE on (T.BMNCD = RE.BMNCD and T.RTPTNNO = RE.RTPTNNO)");
		sbSQL.append(" when matched then update set");
		sbSQL.append("  BMNCD=RE.BMNCD");
		sbSQL.append(" ,RTPTNNO=RE.RTPTNNO");
		sbSQL.append(" ,RTPTNKN=RE.RTPTNKN");
		sbSQL.append(" ,TENRT_ARR=RE.TENRT_ARR");
		sbSQL.append(" ,UPDKBN=RE.UPDKBN");
		sbSQL.append(" ,SENDFLG=RE.SENDFLG");
		sbSQL.append(" ,OPERATOR=RE.OPERATOR");
		sbSQL.append(", ADDDT = case when NVL(UPDKBN, 0) = 1 then RE.ADDDT else ADDDT end");
		sbSQL.append(" ,UPDDT=RE.UPDDT");
		sbSQL.append(" when not matched then insert (");
		sbSQL.append("  BMNCD");
		sbSQL.append(" ,RTPTNNO");
		sbSQL.append(" ,RTPTNKN");
		sbSQL.append(" ,TENRT_ARR");
		sbSQL.append(" ,UPDKBN");
		sbSQL.append(" ,SENDFLG");
		sbSQL.append(" ,OPERATOR");
		sbSQL.append(" ,ADDDT");
		sbSQL.append(" ,UPDDT");
		sbSQL.append(") values (");
		sbSQL.append("  RE.BMNCD");
		sbSQL.append(" ,RE.RTPTNNO");
		sbSQL.append(" ,RE.RTPTNKN");
		sbSQL.append(" ,RE.TENRT_ARR");
		sbSQL.append(" ,RE.UPDKBN");
		sbSQL.append(" ,RE.SENDFLG");
		sbSQL.append(" ,RE.OPERATOR");
		sbSQL.append(" ,RE.ADDDT");
		sbSQL.append(" ,RE.UPDDT");
		sbSQL.append(")");

		if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("通常率パターン");

		return sbSQL.toString();
	}

	/**
	 * 削除処理実行
	 * @return
	 *
	 * @throws Exception
	 */
	private JSONObject deleteData(HashMap<String, String> map, User userInfo) throws Exception {
		// パラメータ確認
		JSONObject	option		= new JSONObject();
		JSONArray	dataArray	= JSONArray.fromObject(map.get("DATA"));		// 更新情報(通常率パターン)

		// 排他チェック用
		String				targetTable	= null;
		String				targetWhere	= null;
		ArrayList<String>	targetParam	= new ArrayList<String>();
		JSONArray			msg			= new JSONArray();

		// 基本登録情報
		JSONObject data = dataArray.getJSONObject(0);

		// 通常率パターンINSERT/UPDATE処理
		createDelSqlRtptn(data,map,userInfo);

//		// 排他チェック実行
//		if(!StringUtils.isEmpty(data.optString("F1")) && !StringUtils.isEmpty(data.optString("F2"))){
//			targetTable = "INATK.TOKRTPTN";
//			targetWhere = " BMNCD=? and RTPTNNO=?";
//			targetParam.add(data.optString("F1"));
//			targetParam.add(data.optString("F2"));
//			if(!super.checkExclusion(targetTable, targetWhere, targetParam, data.optString("F5"))){
//	 			msg.add(MessageUtility.getDbMessageExclusion(FieldType.DEFAULT, new String[]{}));
//	 			option.put(MsgKey.E.getKey(), msg);
//				return option;
//			}
//		}

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
	 * 通常率パターンDELETE(論理削除)処理
	 *
	 * @param data
	 * @param map
	 * @param userInfo
	 */
	public String createDelSqlRtptn(JSONObject data, HashMap<String, String> map, User userInfo){

		// 格納用変数
		StringBuffer	sbSQL	= new StringBuffer();

		// ログインユーザー情報取得
		String userId	= userInfo.getId(); // ログインユーザー

		// DB検索用パラメータ
		String sqlWhere = "";
		ArrayList<String> paramData = new ArrayList<String>();


		sqlWhere += " where BMNCD=?";
		sqlWhere += " and RTPTNNO=?";
		paramData.add(data.optString("F1"));	// 部門
		paramData.add(data.optString("F2"));	// 通常率パターンNo.

		// 通常率パターンの論理削除
		sbSQL.append("update INATK.TOKRTPTN");
		sbSQL.append(" set");
		sbSQL.append(" UPDKBN=" + DefineReport.ValUpdkbn.DEL.getVal());
		sbSQL.append(", SENDFLG = 0");
		sbSQL.append(", OPERATOR='" + userId + "'");
		sbSQL.append(", UPDDT=current timestamp ");
		sbSQL.append(sqlWhere);

		if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(paramData);
		lblList.add("通常率パターン");

		return sbSQL.toString();
	}

	/**
	 * チェック処理
	 *
	 * @param map
	 * @return
	 */
	@SuppressWarnings("static-access")
	public JSONArray check(HashMap<String, String> map) {
		// パラメータ確認
		String sendBtnid		= map.get("SENDBTNID");						// 呼出しボタン
		JSONArray dataArray		= JSONArray.fromObject(map.get("DATA"));	// 更新情報
		JSONArray dataArrayRtptn	= JSONArray.fromObject(map.get("DATA_RTPTN"));	// 更新情報

		JSONArray msg = new JSONArray();
		MessageUtility mu = new MessageUtility();

		// チェック処理
		// 対象件数チェック
		if(dataArray.size() == 0 || dataArray.getJSONObject(0).isEmpty()){
			msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
			return msg;
		}

		// チェック処理
		msg = this.checkData(mu,dataArray,dataArrayRtptn,sendBtnid);

		return msg;
	}

	/**
	 * チェック処理
	 *
	 * @throws Exception
	 */
	public JSONArray checkData(MessageUtility mu,
			JSONArray	dataArray,			// キー項目
			JSONArray	dataArrayRtptn,
			String		sendBtnid
	) {

		// 格納用変数
		StringBuffer		sbSQL	= new StringBuffer();
		ItemList			iL		= new ItemList();
		JSONArray			dbDatas	= new JSONArray();
		ArrayList<String>	paramData = new ArrayList<String>();

		// DB検索用パラメータ
		String sqlWhere		= "";
		String sqlValues	= "";
		String sqlFrom		= "";

		JSONArray msg = new JSONArray();

		if (DefineReport.Button.UPLOAD.getObj().equals(sendBtnid)) {
			// 基本データチェック:入力値がテーブル定義と矛盾してないか確認、
			// 1.正規
			for (int i = 0; i < dataArray.size(); i++) {
				JSONObject jo = dataArray.optJSONObject(i);

				if (jo.size() == 0) {
					continue;
				}

				String reqNo = String.valueOf(i+1) + "行目：";

				for(TOKRTPTNLayout colinf: TOKRTPTNLayout.values()){

					String val = StringUtils.trim(jo.optString(colinf.getId()));
					if(StringUtils.isNotEmpty(val)){
						DataType dtype = null;
						int[] digit = null;

						String txt = colinf.getTxt() + "は";

						try {
							DefineReport.InpText inpsetting = DefineReport.InpText.valueOf(colinf.getCol());
							dtype = inpsetting.getType();
							digit = new int[]{inpsetting.getDigit1(), inpsetting.getDigit2()};
						}catch (IllegalArgumentException e){
							dtype = colinf.getDataType();
							digit = colinf.getDigit();
						}

						// ①データ型による文字種チェック
						if(!InputChecker.checkDataType(dtype, val)){
							// エラー発生箇所を保存
							JSONObject o = mu.getDbMessageObjDataTypeErr(dtype, new String[]{reqNo+txt});
							msg.add(o);
							return msg;
						}
						// ②データ桁チェック
						if(!InputChecker.checkDataLen(dtype, val, digit)){
							// エラー発生箇所を保存
							JSONObject o = mu.getDbMessageObjLen(dtype, new String[]{reqNo+txt});
							msg.add(o);
							return msg;
						}
					}
				}
			}

			// 基本データチェック:入力値がテーブル定義と矛盾してないか確認、
			// 1.正規
			for (int i = 0; i < dataArrayRtptn.size(); i++) {
				JSONObject jo = dataArrayRtptn.optJSONObject(i);

				if (jo.size() == 0) {
					continue;
				}

				String reqNo = String.valueOf(i+1) + "行目：";

				for(BUNPAIRTLayout colinf: BUNPAIRTLayout.values()){

					String val = StringUtils.trim(jo.optString(colinf.getId()));
					if(StringUtils.isNotEmpty(val)){
						DataType dtype = null;
						int[] digit = null;

						String txt = colinf.getTxt() + "は";

						try {
							DefineReport.InpText inpsetting = DefineReport.InpText.valueOf(colinf.getCol());
							dtype = inpsetting.getType();
							digit = new int[]{inpsetting.getDigit1(), inpsetting.getDigit2()};
						}catch (IllegalArgumentException e){
							dtype = colinf.getDataType();
							digit = colinf.getDigit();
						}

						// ①データ型による文字種チェック
						if(!InputChecker.checkDataType(dtype, val)){
							// エラー発生箇所を保存
							JSONObject o = mu.getDbMessageObjDataTypeErr(dtype, new String[]{reqNo+txt});
							msg.add(o);
							return msg;
						}
						// ②データ桁チェック
						if(!InputChecker.checkDataLen(dtype, val, digit)){
							// エラー発生箇所を保存
							JSONObject o = mu.getDbMessageObjLen(dtype, new String[]{reqNo+txt});
							msg.add(o);
							return msg;
						}
					}
				}
			}

			// 新規(正) 1.1　必須入力項目チェックを行う。
			// 変更(正) 1.1　必須入力項目チェックを行う。
			TOKRTPTNLayout[] tokRtPtnCol = null;
			tokRtPtnCol = new TOKRTPTNLayout[]{TOKRTPTNLayout.BMNCD,TOKRTPTNLayout.RTPTNNO,TOKRTPTNLayout.RTPTNKN};
			for (int i = 0; i < dataArray.size(); i++) {
				JSONObject jo = dataArray.optJSONObject(i);

				if (jo.size() == 0) {
					continue;
				}

				String reqNo = String.valueOf(i+1) + "行目：";

				for(TOKRTPTNLayout colinf: tokRtPtnCol){

					String val = StringUtils.trim(jo.optString(colinf.getId()));
					if(StringUtils.isEmpty(val)){

						String txt = colinf.getTxt();

						// エラー発生箇所を保存
						JSONObject o = mu.getDbMessageObj("EX1047", new String[]{reqNo+txt});
						msg.add(o);
						return msg;
					}
				}
			}

			// 新規(正) 1.1　必須入力項目チェックを行う。
			// 変更(正) 1.1　必須入力項目チェックを行う。
			BUNPAIRTLayout[] bunpaiRt = null;
			bunpaiRt = new BUNPAIRTLayout[]{BUNPAIRTLayout.BUNPAIRT,BUNPAIRTLayout.TENCD};
			for (int i = 0; i < dataArrayRtptn.size(); i++) {
				JSONObject jo = dataArrayRtptn.optJSONObject(i);

				if (jo.size() == 0) {
					continue;
				}

				String reqNo = String.valueOf(i+1) + "行目：";

				for(BUNPAIRTLayout colinf: bunpaiRt){

					String val = StringUtils.trim(jo.optString(colinf.getId()));
					if(StringUtils.isEmpty(val)){

						String txt = colinf.getTxt();

						// エラー発生箇所を保存
						JSONObject o = mu.getDbMessageObj("EX1047", new String[]{reqNo+txt});
						msg.add(o);
						return msg;
					}
				}
			}
		}

		// 入力値を取得
		String reqNo = "";
		for (int i = 0; i < dataArray.size(); i++) {
			JSONObject data = dataArray.getJSONObject(i);

			if (DefineReport.Button.UPLOAD.getObj().equals(sendBtnid)) {
				reqNo = String.valueOf(i+1) + "行目：";
			}

			if(DefineReport.Button.NEW.getObj().equals(sendBtnid) || DefineReport.Button.UPLOAD.getObj().equals(sendBtnid)){
				// 重複チェック：部門コード、率パターンNo.
				sbSQL = new StringBuffer();
				sbSQL.append("select * from INATK.TOKRTPTN RTPTN where RTPTN.BMNCD = "+data.optString(TOKRTPTNLayout.BMNCD.getId())+" and RTPTN.RTPTNNO = "+data.optString(TOKRTPTNLayout.RTPTNNO.getId())+"  and NVL(RTPTN.UPDKBN, 0) = 0");
				dbDatas = iL.selectJSONArray(sbSQL.toString(), null, Defines.STR_JNDI_DS);
				if(dbDatas.size() > 0){
					msg.add(MessageUtility.getDbMessageIdObj("E11040", new String[]{reqNo+"部門コード,率パターンNo."}));
					return msg;
				}
			}

			// パターン№チェック
			if (data.optInt(TOKRTPTNLayout.RTPTNNO.getId()) < 0 || data.optInt(TOKRTPTNLayout.RTPTNNO.getId()) > 999) {
				msg.add(MessageUtility.getDbMessageIdObj("E30012", new String[]{reqNo+"0 ≦ パタンーン№ ≦ 999"}));
				return msg;
			}

			// 部門コード存在チェック
			// 変数を初期化
			sbSQL	= new StringBuffer();
			iL		= new ItemList();
			dbDatas = new JSONArray();
			sqlWhere	= "";
			paramData	= new ArrayList<String>();

			if (StringUtils.isEmpty(data.optString(TOKRTPTNLayout.BMNCD.getId()))) {
				sqlWhere += "BMNCD=null AND ";
			} else {
				sqlWhere += "BMNCD=? AND ";
				paramData.add(data.optString(TOKRTPTNLayout.BMNCD.getId()));
			}

			sbSQL.append("SELECT ");
			sbSQL.append("BMNCD ");	// レコード件数
			sbSQL.append("FROM ");
			sbSQL.append("INAMS.MSTBMN ");
			sbSQL.append("WHERE ");
			sbSQL.append(sqlWhere); // 入力された商品コードで検索
			sbSQL.append("UPDKBN="+DefineReport.ValUpdkbn.NML.getVal());

			dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

			if (dbDatas.size() == 0){
				msg.add(MessageUtility.getDbMessageIdObj("E11044", new String[]{reqNo}));
				return msg;
			}
		}

		for (int i = 0; i < dataArrayRtptn.size(); i++) {
			// 入力値を取得
			JSONObject dataR = dataArrayRtptn.getJSONObject(i);

			if (DefineReport.Button.UPLOAD.getObj().equals(sendBtnid)) {
				reqNo = String.valueOf(i+1) + "行目：";
			}

			if (dataR.isEmpty()) {
				continue;
			}

			// パターン№チェック
			if (dataR.optInt(BUNPAIRTLayout.BUNPAIRT.getId()) < 0 || dataR.optInt(BUNPAIRTLayout.BUNPAIRT.getId()) > 99999) {
				msg.add(MessageUtility.getDbMessageIdObj("E30012", new String[]{reqNo+"0 ≦ 分配率 ≦ 99999"}));
				return msg;
			}

			String tencd = dataR.optString(BUNPAIRTLayout.TENCD.getId());
			if (Integer.valueOf(tencd) > 400 || Integer.valueOf(tencd) < 1) {
				msg.add(mu.getDbMessageObj("E20110", new String[]{reqNo+"店コード"}));
				return msg;
			}

			// 店が存在するか
			// 変数を初期化
			if (DefineReport.Button.UPLOAD.getObj().equals(sendBtnid)) {
				sbSQL	= new StringBuffer();
				iL		= new ItemList();
				dbDatas = new JSONArray();
				sqlWhere	= "";
				paramData	= new ArrayList<String>();

				// 店存在チェック
				if (StringUtils.isEmpty(tencd)) {
					sqlWhere += "TENCD=null AND ";
				} else {
					sqlWhere += "TENCD=? AND ";
					paramData.add(tencd);
				}

				sbSQL.append("SELECT ");
				sbSQL.append("TENCD ");	// レコード件数
				sbSQL.append("FROM ");
				sbSQL.append("INAMS.MSTTEN ");
				sbSQL.append("WHERE ");
				sbSQL.append(sqlWhere); // 入力された商品コードで検索
				sbSQL.append("MISEUNYOKBN <> '9' AND UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal() + " ");

				dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

				if (dbDatas.size() == 0){
					// 登録不可の店舗
					msg.add(mu.getDbMessageObj("EX1077", new String[]{reqNo}));
					return msg;
				}
			}
		}
		return msg;
	}

	/**  通常率パターンレイアウト */
	public enum TOKRTPTNLayout implements MSTLayout{

		/** 部門コード */
		BMNCD(1,"BMNCD","SMALLINT","部門コード"),
		/** 率パターン№ */
		RTPTNNO(2,"RTPTNNO","SMALLINT","率パターン№"),
		/** 率パターン名称 */
		RTPTNKN(3,"RTPTNKN","VARCHAR(40)","率パターン名称"),
		/** 店分配率配列 */
		TENRT_ARR(4,"TENRT_ARR","VARCHAR(2000)","店分配率配列");

		private final Integer no;
		private final String col;
		private final String txt;
		private final String typ;
		/** 初期化 */
		private TOKRTPTNLayout(Integer no, String col, String typ, String txt) {
			this.no = no;
			this.col = col;
			this.typ = typ;
			this.txt = txt;
		}
		/** @return col 列番号 */
		public Integer getNo() { return no; }
		/** @return tbl 列名 */
		public String getCol() { return col; }
		/** @return typ 列型 */
		public String getTyp() { return typ; }
		/** @return txt 論理名 */
		public String getTxt() { return txt; }
		/** @return col Id */
		public String getId() { return "F" + Integer.toString(no); }

		/** @return datatype データ型のみ */
		public DataType getDataType() {
			if(typ.indexOf("INT") != -1){ return DefineReport.DataType.INTEGER;}
			if(typ.indexOf("DEC") != -1){ return DefineReport.DataType.DECIMAL;}
			if(typ.indexOf("DATE")!= -1||typ.indexOf("TIMESTAMP")!= -1){ return DefineReport.DataType.DATE;}
			return DefineReport.DataType.TEXT;
		}
		/** @return boolean */
		public boolean isText() {
			return getDataType() == DefineReport.DataType.TEXT;
		}
		/** @return digit 桁数 */
		public int[] getDigit() {
			int digit1 = 0, digit2 = 0;
 			if(typ.indexOf(",") > 0){
				String[] sDigit = typ.substring(typ.indexOf("(")+1, typ.indexOf(")")).split(",");
				digit1 = NumberUtils.toInt(sDigit[0]);
				digit2 = NumberUtils.toInt(sDigit[1]);
 			}else if(typ.indexOf("(") > 0){
 				digit1 = NumberUtils.toInt(typ.substring(typ.indexOf("(")+1, typ.indexOf(")")));
			}else if(StringUtils.equals(typ, JDBCType.SMALLINT.getName())){
				digit1 = dbNumericTypeInfo.SMALLINT.getDigit();
			}else if(StringUtils.equals(typ, JDBCType.INTEGER.getName())){
				digit1 = dbNumericTypeInfo.INTEGER.getDigit();
			}
			return new int[]{digit1, digit2};
		}
	}

	/**  分配率レイアウト */
	public enum BUNPAIRTLayout implements MSTLayout{

		/** 店コード */
		TENCD(1,"TENCD","SMALLINT","店コード"),
		/** 店名称 */
		TENKN(2,"TENKN","VARCHAR(40)","店名称"),
		/** 分配率 */
		BUNPAIRT(3,"BUNPAIRT","VARCHAR(5)","分配率");

		private final Integer no;
		private final String col;
		private final String txt;
		private final String typ;
		/** 初期化 */
		private BUNPAIRTLayout(Integer no, String col, String typ, String txt) {
			this.no = no;
			this.col = col;
			this.typ = typ;
			this.txt = txt;
		}
		/** @return col 列番号 */
		public Integer getNo() { return no; }
		/** @return tbl 列名 */
		public String getCol() { return col; }
		/** @return typ 列型 */
		public String getTyp() { return typ; }
		/** @return txt 論理名 */
		public String getTxt() { return txt; }
		/** @return col Id */
		public String getId() { return "F" + Integer.toString(no); }

		/** @return datatype データ型のみ */
		public DataType getDataType() {
			if(typ.indexOf("INT") != -1){ return DefineReport.DataType.INTEGER;}
			if(typ.indexOf("DEC") != -1){ return DefineReport.DataType.DECIMAL;}
			if(typ.indexOf("DATE")!= -1||typ.indexOf("TIMESTAMP")!= -1){ return DefineReport.DataType.DATE;}
			return DefineReport.DataType.TEXT;
		}
		/** @return boolean */
		public boolean isText() {
			return getDataType() == DefineReport.DataType.TEXT;
		}
		/** @return digit 桁数 */
		public int[] getDigit() {
			int digit1 = 0, digit2 = 0;
 			if(typ.indexOf(",") > 0){
				String[] sDigit = typ.substring(typ.indexOf("(")+1, typ.indexOf(")")).split(",");
				digit1 = NumberUtils.toInt(sDigit[0]);
				digit2 = NumberUtils.toInt(sDigit[1]);
 			}else if(typ.indexOf("(") > 0){
 				digit1 = NumberUtils.toInt(typ.substring(typ.indexOf("(")+1, typ.indexOf(")")));
			}else if(StringUtils.equals(typ, JDBCType.SMALLINT.getName())){
				digit1 = dbNumericTypeInfo.SMALLINT.getDigit();
			}else if(StringUtils.equals(typ, JDBCType.INTEGER.getName())){
				digit1 = dbNumericTypeInfo.INTEGER.getDigit();
			}
			return new int[]{digit1, digit2};
		}
	}

}
