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
import authentication.defines.Consts;
import common.DefineReport;
import common.DefineReport.ValKbn10002;
import common.Defines;
import common.ItemList;
import common.JsonArrayData;
import common.MessageUtility;
import common.MessageUtility.FieldType;
import common.MessageUtility.Msg;
import common.MessageUtility.MsgKey;
import dao.ReportTG003Dao.TOKTG_TENGPLayout;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 */
public class ReportTG040Dao extends ItemDao {

	/**
	 * インスタンスを生成します。
	 * @param source
	 */
	public ReportTG040Dao(String JNDIname) {
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
		String sysdate = (String)request.getSession().getAttribute(Consts.STR_SES_LOGINDT);


		// 更新情報チェック(基本JS側で制御)
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
			option.put(MsgKey.E.getKey(), MessageUtility.getDbMessageIdObj("E30007", new String[]{}));
		}
		return option;
	}

	private String createCommand() {
		// ログインユーザー情報取得
		User userInfo = getUserInfo();
		if(userInfo==null){
			return "";
		}

		String szMoyscd = getMap().get("MOYSCD");	// 催しコード
		String szMoyskn = getMap().get("MOYSKN");	// 催し名称
		String szHbstdt = getMap().get("HBSTDT");	// 催し期間From
		String szHbeddt = getMap().get("HBEDDT");	// 催し期間To

		// パラメータ確認
/*		// 必須チェック
		if ((btnId == null)) {
			System.out.println(super.getConditionLog());
			return "";
		}*/


		// タイトル情報(任意)設定
		List<String> titleList = new ArrayList<String>();



		// DB検索用パラメータ
		ArrayList<String> paramData = new ArrayList<String>();

		// 一覧表情報
		StringBuffer sbSQL = new StringBuffer();
		sbSQL.append(DefineReport.ID_SQL_CMN_WEEK);
		sbSQL.append(",WKCD as ( ");
		sbSQL.append("  select T1.MOYSKBN, T1.MOYSSTDT, T1.MOYSRBAN, T1.MOYKN, T1.HBSTDT, T1.HBEDDT ");
		sbSQL.append("  from INATK.TOKMOYCD T1");
		sbSQL.append("  where T1.UPDKBN = 0");
		if(StringUtils.isNotEmpty(szMoyscd)){
			sbSQL.append("  and T1.MOYSKBN = " +StringUtils.left(szMoyscd, 1)+"");
			sbSQL.append("  and T1.MOYSSTDT = "+StringUtils.mid(szMoyscd, 1, 6)+"");
			sbSQL.append("  and T1.MOYSRBAN = "+NumberUtils.toInt(StringUtils.right(szMoyscd, 3))+"");
		}else{
			sbSQL.append("  and T1.MOYSKBN = "+ValKbn10002.VAL1.getVal()+"");
			sbSQL.append("  and T1.MOYSRBAN >= 50");
		}
		if(!StringUtils.isEmpty(szHbstdt)){
			sbSQL.append("  and T1.HBSTDT >= " + szHbstdt);
		}
		if(!StringUtils.isEmpty(szHbeddt)){
			sbSQL.append("  and T1.HBSTDT <= " + szHbeddt);
		}
		sbSQL.append(") ");
		sbSQL.append(" select");
		sbSQL.append("    T1.MOYSKBN || '-' || right (T1.MOYSSTDT, 6) || '-' || right ('000' || T1.MOYSRBAN, 3) as F1");
		sbSQL.append("  , T1.MOYKN as F2");
		sbSQL.append("  , TO_CHAR(TO_DATE(T1.HBSTDT, 'YYYYMMDD'), 'YY/MM/DD') || (select JWEEK from WEEK where CWEEK = DAYOFWEEK(TO_DATE(T1.HBSTDT, 'YYYYMMDD')))");
		sbSQL.append("    ||'～'||");
		sbSQL.append("    TO_CHAR(TO_DATE(T1.HBEDDT, 'YYYYMMDD'), 'YY/MM/DD') || (select JWEEK from WEEK where CWEEK = DAYOFWEEK(TO_DATE(T1.HBEDDT, 'YYYYMMDD'))) as F3");
		sbSQL.append("  , T1.MOYSKBN as F4");
		sbSQL.append("  , T1.MOYSSTDT as F5");
		sbSQL.append("  , T1.MOYSRBAN as F6");
		sbSQL.append("  , T1.HBSTDT as F7");
		sbSQL.append("  , T1.HBEDDT as F8");
		sbSQL.append(" from");
		sbSQL.append("  WKCD T1 ");
		sbSQL.append(" order by T1.MOYSKBN, T1.MOYSSTDT, T1.MOYSRBAN");

		// DB検索用パラメータ設定
		setParamData(paramData);


		// オプション情報（タイトル）設定
		JSONObject option = new JSONObject();
		option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
		setOption(option);

		if (DefineReport.ID_DEBUG_MODE) System.out.println(getClass().getSimpleName()+"[sql]"+sbSQL.toString());
		return sbSQL.toString();
	}

	/**
	 * 情報取得処理
	 *
	 * @throws Exception
	 */
	public String createSqlSelTOKTG_TENGP(HashMap<String, String> map) {
		String szMoyskbn	= map.get("MOYSKBN");	// 催し区分
		String szMoysstdt	= map.get("MOYSSTDT");	// 催しコード（催し開始日）
		String szMoysrban	= map.get("MOYSRBAN");	// 催し連番


		// 一覧表情報
		StringBuffer sbSQL = new StringBuffer();
		sbSQL.append(" select");
		sbSQL.append("   max(T2.KYOSEIFLG) as F1");
		sbSQL.append("  ,right('000'||T2.TENGPCD,3) as F2");
		sbSQL.append("  ,max(T2.TENGPKN) as F3");
		sbSQL.append("  ,max(case when T3.LDTENKBN = 1 then right('000'||T3.TENCD,3) end) as F4");
		sbSQL.append("  ,max(case when T3.LDTENKBN = 1 then M1.TENKN end) as F5");
		sbSQL.append("  ,count(T3.TENCD) as F6");
		sbSQL.append("  ,max(case when T2.QASYUKBN = 1 then 1 else 0 end) as F7");
		sbSQL.append("  ,max(case when T2.QASYUKBN = 2 then 1 else 0 end) as F8");
		sbSQL.append("  ,max(case when T2.QASYUKBN = 3 then 1 else 0 end) as F9");
		sbSQL.append("  ,max(case when T2.QASYUKBN = 4 then 1 else 0 end) as F10");
		sbSQL.append("  ,max(case when T2.QASYUKBN = 5 then 1 else 0 end) as F11");
		sbSQL.append("  ,T1.MOYSKBN");
		sbSQL.append("  ,T1.MOYSSTDT");
		sbSQL.append("  ,T1.MOYSRBAN");
		sbSQL.append("  ,T2.TENGPCD");
		sbSQL.append("  ,max(T2.TENGPKN) as TENGPKN");		// F5 : 店グループ名称
		sbSQL.append("  ,max(T2.KYOSEIFLG) as KYOSEIFLG");	// F6 : 強制グループフラグ
		sbSQL.append("  ,max(T2.QASYUKBN) as QASYUKBN");	// F7 : アンケート種類
		sbSQL.append("  ,max(T2.QACREDT_K) as QACREDT_K");	// F8 : アンケート作成日_強制
		sbSQL.append("  ,max(T2.QARCREDT_K) as QARCREDT_K");// F9 : アンケート再作成日_強制
		sbSQL.append("  ,max(T2.UPDKBN) as UPDKBN");		// F10: 更新区分
		sbSQL.append(" from");
		sbSQL.append("  INATK.TOKMOYCD T1 ");
		sbSQL.append("  inner join INATK.TOKTG_TENGP T2 on T1.MOYSKBN = T2.MOYSKBN and T1.MOYSSTDT = T2.MOYSSTDT and T1.MOYSRBAN = T2.MOYSRBAN");
		sbSQL.append("  and T1.MOYSKBN = "+szMoyskbn+"");
		sbSQL.append("  and T1.MOYSSTDT = "+szMoysstdt+"");
		sbSQL.append("  and T1.MOYSRBAN = "+szMoysrban+"");
		sbSQL.append("  and T1.UPDKBN = 0 and T2.UPDKBN = 0 and T2.KYOSEIFLG <> '1' ");
		sbSQL.append("  left outer join INATK.TOKTG_TEN T3 on T1.MOYSKBN = T3.MOYSKBN and T1.MOYSSTDT = T3.MOYSSTDT and T1.MOYSRBAN = T3.MOYSRBAN and T2.TENGPCD = T3.TENGPCD and T2.KYOSEIFLG = T3.KYOSEIFLG");
		sbSQL.append("  left outer join INAMS.MSTTEN M1 on T3.TENCD = M1.TENCD");
		sbSQL.append(" group by T1.MOYSKBN, T1.MOYSSTDT, T1.MOYSRBAN, T2.TENGPCD");
		sbSQL.append(" order by T1.MOYSKBN, T1.MOYSSTDT, T1.MOYSRBAN, T2.TENGPCD");

		return sbSQL.toString();
	}

	/**
	 * 催しコード初期値取得
	 *
	 * @throws Exception
	 */
	public String getInitMOYSCD(HashMap<String, String> map) {
		String szMoysstdt	= map.get("MOYSSTDT");	// 催しコード（催し開始日）

		// 一覧表情報
		StringBuffer sbSQL = new StringBuffer();

		sbSQL.append(" select T1.MOYSKBN||max(lpad(to_char(T1.MOYSSTDT), 6, '0')||lpad(to_char(T1.MOYSRBAN), 3, '0')) as VALUE");
		sbSQL.append(" from INATK.TOKMOYCD T1 ");
		sbSQL.append(" where T1.UPDKBN = 0");
		sbSQL.append("   and T1.MOYSKBN = "+DefineReport.ValKbn10002.VAL1.getVal()+"");
		sbSQL.append("   and T1.MOYSSTDT < "+szMoysstdt+"");
		sbSQL.append("   and T1.MOYSRBAN >= 50 ");
		sbSQL.append(" group by T1.MOYSKBN");

		ItemList iL = new ItemList();
		@SuppressWarnings("static-access")
		JSONArray array = iL.selectJSONArray(sbSQL.toString(), null, Defines.STR_JNDI_DS);
		String value = "";
		if(array.size() > 0){
			value = array.optJSONObject(0).optString("VALUE");
		}
		return value;
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
	 * 更新処理実行
	 * @return
	 *
	 * @throws Exception
	 */
	private JSONObject updateData(HashMap<String, String> map, User userInfo, String sysdate) throws Exception {
		// パラメータ取得
		String sendBtnid	= map.get("SENDBTNID");						// 呼出しボタン
		JSONArray dataArray = JSONArray.fromObject(map.get("DATA"));	// 対象情報（主要な更新情報）

		JSONObject option = new JSONObject();
		JSONArray msg = new JSONArray();

		// パラメータ確認
		// 必須チェック
		if ( sendBtnid == null || dataArray.isEmpty() || dataArray.size() == 0 || dataArray.getJSONObject(0).isEmpty()) {
			option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
			return option;
		}

		// ログインユーザー情報取得
		String userId	= userInfo.getId();								// ログインユーザー


		// --- 01.全店特売(アンケート有)_店グループ
		JSONObject result1 = this.createSqlTOKTG_TENGP(userId, dataArray, SqlType.INS);

		// --- 02.全店特売(アンケート有)_店
		JSONObject result2 = this.createSqlTOKTG_TEN(userId, dataArray, SqlType.INS);

		// 排他チェック実行
		ArrayList<String> targetParam = new ArrayList<String>();
		if(dataArray.size() > 0){
			String rownum = "";						// エラー行数

			String targetTable = "INATK.TOKTG_TENGP";
			String targetWhere = " MOYSKBN = ? and MOYSSTDT= ? and MOYSRBAN = ? and TENGPCD = ? and UPDKBN = 0";
			for (int i = 0; i < dataArray.size(); i++) {
				JSONObject data = dataArray.getJSONObject(i);
				if(data.isEmpty()){
					continue;
				}

				targetParam = new ArrayList<String>();
				targetParam.add(data.optString(TOKTG_TENGPLayout.MOYSKBN.getId()));
				targetParam.add(data.optString(TOKTG_TENGPLayout.MOYSSTDT.getId()));
				targetParam.add(data.optString(TOKTG_TENGPLayout.MOYSRBAN.getId()));
				targetParam.add(data.optString(TOKTG_TENGPLayout.TENGPCD.getId()));

				if(!super.checkExclusion(targetTable, targetWhere, targetParam, data.optString(TOKTG_TENGPLayout.UPDDT.getId()))){
					rownum = (data.optString("RNO"));
		 			msg.add(MessageUtility.getDbMessageExclusion(FieldType.GRID, new String[]{rownum}));
		 			option.put(MsgKey.E.getKey(), msg);
					return option;
				}
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
	 * チェック処理
	 *
	 * @throws Exception
	 */
	public JSONArray check(HashMap<String, String> map, User userInfo, String sysdate) {
		// パラメータ確認
		String sendBtnid	= map.get("SENDBTNID");						// 呼出しボタン
		JSONArray dataArray = JSONArray.fromObject(map.get("DATA"));	// 対象情報（主要な更新情報）

		// ①正 .新規
		boolean isNew = DefineReport.Button.NEW.getObj().equals(sendBtnid);
		// ②正 .変更
		boolean isChange = DefineReport.Button.SEL_CHANGE.getObj().equals(sendBtnid);


		List<JSONObject> msgList =  new ArrayList<JSONObject>();

		JSONObject data = dataArray.optJSONObject(0);
		String ErrTblNm = "MSTSHN";


//		// 3.催しコード：スポット
//		ErrTblNm = "MSTSIRGPSHN";
//		for (int i = 0; i < dataArrayMOYCD.size(); i++) {
//			JSONObject jo = dataArrayMOYCD.optJSONObject(i);
//			for(MSTSIRGPSHNLayout colinf: MSTSIRGPSHNLayout.values()){
//				String val = StringUtils.trim(jo.optString(colinf.getId()));
//				if(StringUtils.isNotEmpty(val)){
//					DataType dtype = null;
//					int[] digit = null;
//					try {
//						DefineReport.InpText inpsetting = DefineReport.InpText.valueOf(colinf.getCol());
//						dtype = inpsetting.getType();
//						digit = new int[]{inpsetting.getDigit1(), inpsetting.getDigit2()};
//					}catch (IllegalArgumentException e){
//						dtype = colinf.getDataType();
//						digit = colinf.getDigit();
//					}
//					// ①データ型による文字種チェック
//					if(!InputChecker.checkDataType(dtype, val)){
//						JSONObject o = mu.getDbMessageObjDataTypeErr(dtype, new String[]{});
//						msg.add(o);
//						if(!colinf.isText()){
//							jo.element(colinf.getId(), "");		// CSVトラン用に空
//						}
//					}
//					// ②データ桁チェック
//					if(!InputChecker.checkDataLen(dtype, val, digit)){
//						JSONObject o = mu.getDbMessageObjLen(dtype, new String[]{});
//						msg.add(o);
//						jo.element(colinf.getId(), "");		// CSVトラン用に空
//					}
//				}
//			}
//		}

		JSONArray msgArray = new JSONArray();
		// MessageBoxを出す関係上、1件のみ表示
		if(msgList.size() > 0){
			msgArray.add(msgList.get(0));
		}
		return msgArray;
	}

	/**
	 * 項目用 DB問い合わせ件数チェック
	 * @param outobj チェック対象項目
	 * @param obj パラメータ
	 * @throws Exception
	 */
	public JSONObject checkDbCount(String outobj, JSONObject obj) {
		// 関連情報取得
		ItemList iL = new ItemList();
		// 配列準備
		ArrayList<String> paramData = new ArrayList<String>();

		String szMoyskbn	= obj.optString("MOYSKBN");		// 催し区分
		String szMoysstdt	= obj.optString("MOYSSTDT");	// 催しコード（催し開始日）
		String szMoysrban	= obj.optString("MOYSRBAN");	// 催し連番
		String szTengpcd	= obj.optString("TENGPCD");		// 店グループ

		// 2.3.1.1．コピー対象グループに属する全店舗をチェックし、廃店（店舗基本.店運用区分=9）がある場合はワーニングを表示する。
		StringBuffer sbSQL = new StringBuffer();
		sbSQL.append(" select");
		sbSQL.append("  T2.TENCD");
		sbSQL.append(" from");
		sbSQL.append("  INATK.TOKTG_TENGP T1 ");
		sbSQL.append("  inner join INATK.TOKTG_TEN T2 on T1.MOYSKBN = T2.MOYSKBN and T1.MOYSSTDT = T2.MOYSSTDT and T1.MOYSRBAN = T2.MOYSRBAN and T1.TENGPCD = T2.TENGPCD and T1.KYOSEIFLG = T2.KYOSEIFLG");
		sbSQL.append("  and T1.MOYSKBN = "+szMoyskbn+"");
		sbSQL.append("  and T1.MOYSSTDT = "+szMoysstdt+"");
		sbSQL.append("  and T1.MOYSRBAN = "+szMoysrban+"");
		sbSQL.append("  and T1.TENGPCD = "+szTengpcd+"");
		sbSQL.append("  and T1.UPDKBN = 0");
		sbSQL.append("  inner join INAMS.MSTTEN M1 on T2.TENCD = M1.TENCD and MISEUNYOKBN = 9");
		sbSQL.append(" order by T2.TENCD");

		@SuppressWarnings("static-access")
		JSONArray array = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

		// 存在する場合
		if(array.size() > 0){
			String values = "";
			// 値設定
			for(int j=0; j < array.size(); j++){
				values += ","+StringUtils.trim(array.optJSONObject(j).optString("TENCD"));
			}
			values = "<br>廃店：" + StringUtils.removeStart(values, ",");
			// E20259	コピー対象の以下の店舗が廃店となっています。	コピー処理終了後、TG003店舗グループ店情報画面で以下の店舗を削除してください。	0	 	E
			return MessageUtility.getDbMessageIdObj("E20259", new String[]{"", "", values});
		}

		JSONObject data = new JSONObject();
		data.put("RES", "true");
		return data;
	}

	/**
	 * 全店特売(アンケート有)_店グループINSERT/UPDATE SQL作成処理
	 *
	 * @param userId
	 * @param dataArray
	 *
	 * @throws Exception
	 */
	private JSONObject createSqlTOKTG_TENGP(String userId, JSONArray dataArray, SqlType sql) {
		JSONObject result = new JSONObject();

		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();

		// 更新列設定
		TOKTG_TENGPLayout[] notTarget = new TOKTG_TENGPLayout[]{TOKTG_TENGPLayout.UPDKBN, TOKTG_TENGPLayout.SENDFLG, TOKTG_TENGPLayout.OPERATOR,TOKTG_TENGPLayout.ADDDT, TOKTG_TENGPLayout.UPDDT};
		String szWhere = "T.MOYSKBN = RE.MOYSKBN and T.MOYSSTDT= RE.MOYSSTDT and T.MOYSRBAN = RE.MOYSRBAN and T.TENGPCD = RE.TENGPCD";
		String sendflg = DefineReport.Values.SENDFLG_UN.getVal();
		String updkbn = DefineReport.ValUpdkbn.NML.getVal();
		if(SqlType.DEL.getVal() == sql.getVal()){
			updkbn = DefineReport.ValUpdkbn.DEL.getVal();
		}

		String values = "", names = "", rows = "", cols = "";
		// 列別名設定
		for (TOKTG_TENGPLayout itm : TOKTG_TENGPLayout.values()) {
			if(ArrayUtils.contains(notTarget, itm)){ continue;}	// パラメータ不要
			names  += ","+itm.getId();
			cols  += ", cast("+itm.getId()+" as "+itm.getTyp()+") as " + itm.getCol();
		}
		names  = StringUtils.removeStart(names, ",");
		cols   = StringUtils.removeStart(cols, ",");

		int idx = 0;
		// 値設定
		for(int j=0; j < dataArray.size(); j++){
			values = "";
			for (TOKTG_TENGPLayout itm : TOKTG_TENGPLayout.values()) {
				if(ArrayUtils.contains(notTarget, itm)){ continue;}	// パラメータ不要
				String val = StringUtils.trim(dataArray.optJSONObject(j).optString(itm.getId()));
				if(StringUtils.isEmpty(val)){
					values += ", null";
				}else{
					prmData.add(val);
					values += ", cast(? as varchar("+MessageUtility.getDefByteLen(val)+"))";
				}
			}
			rows += ",("+StringUtils.removeStart(values, ",")+")";
		}
		rows = StringUtils.removeStart(rows, ",");


		// 基本Merge文
		StringBuffer sbSQL;
		sbSQL = new StringBuffer();
		sbSQL.append("merge into INATK.TOKTG_TENGP as T");
		sbSQL.append(" using (");
		sbSQL.append("  select "+ cols + " from (values"+rows+") as T1("+names+")");
		sbSQL.append(" ) as RE on ("+szWhere+") ");
		sbSQL.append(" WHEN MATCHED THEN UPDATE SET ");
		sbSQL.append("TENGPKN=RE.TENGPKN");
		sbSQL.append(",KYOSEIFLG=RE.KYOSEIFLG");
		sbSQL.append(",QASYUKBN=RE.QASYUKBN");
		sbSQL.append(",QACREDT_K=RE.QACREDT_K");
		sbSQL.append(",QARCREDT_K=RE.QARCREDT_K");
		sbSQL.append(",UPDKBN="+updkbn);
		sbSQL.append(",SENDFLG="+sendflg);
		sbSQL.append(",OPERATOR='"+userId+"'");
		sbSQL.append(",ADDDT=current timestamp ");
		sbSQL.append(",UPDDT=current timestamp ");
		if(SqlType.INS.getVal() == sql.getVal()){
			sbSQL.append(" when not matched then ");
			sbSQL.append(" insert (");
			sbSQL.append("  MOYSKBN");		// F1 : 催し区分
			sbSQL.append(" ,MOYSSTDT");		// F2 : 催し開始日
			sbSQL.append(" ,MOYSRBAN");		// F3 : 催し連番
			sbSQL.append(" ,TENGPCD");		// F4 : 店グループ
			sbSQL.append(" ,TENGPKN");		// F5 : 店グループ名称
			sbSQL.append(" ,KYOSEIFLG");	// F6 : 強制グループフラグ
			sbSQL.append(" ,QASYUKBN");		// F7 : アンケート種類
			sbSQL.append(" ,QACREDT_K");	// F8 : アンケート作成日_強制
			sbSQL.append(" ,QARCREDT_K");	// F9 : アンケート再作成日_強制
			sbSQL.append(" ,UPDKBN");		// F10: 更新区分
			sbSQL.append(" ,SENDFLG");		// F11: 送信フラグ
			sbSQL.append(" ,OPERATOR");		// F12: オペレータ
			sbSQL.append(" ,ADDDT");		// F13: 登録日
			sbSQL.append(" ,UPDDT");		// F14: 更新日
			sbSQL.append(")values(");
			sbSQL.append("  RE.MOYSKBN");	// F1 : 催し区分
			sbSQL.append(" ,RE.MOYSSTDT");	// F2 : 催し開始日
			sbSQL.append(" ,RE.MOYSRBAN");	// F3 : 催し連番
			sbSQL.append(" ,RE.TENGPCD");	// F4 : 店グループ
			sbSQL.append(" ,RE.TENGPKN");	// F5 : 店グループ名称
			sbSQL.append(" ,RE.KYOSEIFLG");	// F6 : 強制グループフラグ
			sbSQL.append(" ,RE.QASYUKBN");	// F7 : アンケート種類
			sbSQL.append(" ,RE.QACREDT_K");	// F8 : アンケート作成日_強制
			sbSQL.append(" ,RE.QARCREDT_K");	// F9 : アンケート再作成日_強制
			sbSQL.append(" ,"+updkbn);			// F10: 更新区分
			sbSQL.append(" ,"+sendflg);			// F11: 送信フラグ
			sbSQL.append(" ,'"+userId+"'");			// F12: オペレータ
			sbSQL.append(" ,current timestamp");	// F13: 登録日
			sbSQL.append(" ,current timestamp");	// F14: 更新日
			sbSQL.append(" )");
		}

		if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("全店特売(アンケート有)_店グループ");
		return result;
	}

	/**
	 * 関連テーブルINSERT/UPDATE SQL作成処理
	 *
	 * @param userId
	 * @param data
	 *
	 * @throws Exception
	 */
	private JSONObject createSqlTOKTG_TEN(String userId, JSONArray dataArray, SqlType sql) {
		JSONObject result = new JSONObject();

		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();


		// 更新列設定
		TOKTG_TENGPLayout[] keys = new TOKTG_TENGPLayout[]{TOKTG_TENGPLayout.MOYSKBN, TOKTG_TENGPLayout.MOYSSTDT, TOKTG_TENGPLayout.MOYSRBAN, TOKTG_TENGPLayout.TENGPCD};

		String values = "", names = "", rows = "", cols = "";
		// 列別名設定
		String szWhereIn = "";
		for (TOKTG_TENGPLayout itm : keys) {
			names += ","+itm.getId() + ","+itm.getId()+"C";
			szWhereIn += " and T1."+itm.getId()+"C" + "= T2."+itm.getCol();
		}
		names = StringUtils.removeStart(names, ",");
		szWhereIn  = StringUtils.removeStart(szWhereIn, " and");

		int idx = 0;
		// 値設定
		for(int j=0; j < dataArray.size(); j++){
			values = "";
			for (TOKTG_TENGPLayout itm : keys) {
				String val = StringUtils.trim(dataArray.optJSONObject(j).optString(itm.getId()));
				prmData.add(val);
				values += ", cast(? as varchar("+MessageUtility.getDefByteLen(val)+"))";
				String valc = StringUtils.trim(dataArray.optJSONObject(j).optString(itm.getId()+"C"));
				prmData.add(valc);
				values += ", cast(? as varchar("+MessageUtility.getDefByteLen(val)+"))";
			}
			rows += ",("+StringUtils.removeStart(values, ",")+")";
		}
		rows = StringUtils.removeStart(rows, ",");

		String szWhere = "T.MOYSKBN = RE.MOYSKBN and T.MOYSSTDT= RE.MOYSSTDT and T.MOYSRBAN = RE.MOYSRBAN and T.TENCD = RE.TENCD and T.KYOSEIFLG = RE.KYOSEIFLG ";
		String sendflg = DefineReport.Values.SENDFLG_UN.getVal();

		// 基本Merge文
		StringBuffer sbSQL;
		sbSQL = new StringBuffer();
		sbSQL.append("merge into INATK.TOKTG_TEN as T");
		sbSQL.append(" using (");
		sbSQL.append("  select "+ cols + "");
		sbSQL.append("  cast(T1."+TOKTG_TENGPLayout.MOYSKBN.getId()+" as "+TOKTG_TENGPLayout.MOYSKBN.getTyp()+") as " + TOKTG_TENGPLayout.MOYSKBN.getCol());
		sbSQL.append(" ,cast(T1."+TOKTG_TENGPLayout.MOYSSTDT.getId()+" as "+TOKTG_TENGPLayout.MOYSSTDT.getTyp()+") as " + TOKTG_TENGPLayout.MOYSSTDT.getCol());
		sbSQL.append(" ,cast(T1."+TOKTG_TENGPLayout.MOYSRBAN.getId()+" as "+TOKTG_TENGPLayout.MOYSRBAN.getTyp()+") as " + TOKTG_TENGPLayout.MOYSRBAN.getCol());
		sbSQL.append(" ,T2.TENCD");			// F4 : 店コード
		sbSQL.append(" ,T2.KYOSEIFLG");		// F5 : 強制グループフラグ
		sbSQL.append(" ,T2.TENGPCD");		// F6 : 店グループ
		sbSQL.append(" ,T2.LDTENKBN");		// F7 : リーダー店区分
		sbSQL.append(" ,T2.SENDFLG");		// F8 : 送信フラグ
		sbSQL.append("  from (values"+rows+") as T1("+names+")");
		sbSQL.append("  inner join INATK.TOKTG_TEN T2 on " +szWhereIn);
		sbSQL.append(" ) as RE on ("+szWhere+") ");
		sbSQL.append(" WHEN MATCHED THEN UPDATE SET ");
		sbSQL.append("TENGPCD=RE.TENGPCD");
		sbSQL.append(",LDTENKBN=RE.LDTENKBN");
		sbSQL.append(",SENDFLG="+sendflg);
		sbSQL.append(",OPERATOR='"+userId+"'");
		sbSQL.append(",ADDDT=current timestamp ");
		sbSQL.append(",UPDDT=current timestamp ");
		if(SqlType.INS.getVal() == sql.getVal()){
			sbSQL.append(" when not matched then ");
			sbSQL.append(" insert (");
			sbSQL.append("  MOYSKBN");		// F1 : 催し区分
			sbSQL.append(" ,MOYSSTDT");		// F2 : 催し開始日
			sbSQL.append(" ,MOYSRBAN");		// F3 : 催し連番
			sbSQL.append(" ,TENCD");		// F4 : 店コード
			sbSQL.append(" ,KYOSEIFLG");	// F5 : 強制グループフラグ
			sbSQL.append(" ,TENGPCD");		// F6 : 店グループ
			sbSQL.append(" ,LDTENKBN");		// F7 : リーダー店区分
			sbSQL.append(" ,SENDFLG");		// F8 : 送信フラグ
			sbSQL.append(" ,OPERATOR");		// F9 : オペレータ
			sbSQL.append(" ,ADDDT");		// F10: 登録日
			sbSQL.append(" ,UPDDT");		// F11: 更新日
			sbSQL.append(")values(");
			sbSQL.append("  RE.MOYSKBN");	// F1 : 催し区分
			sbSQL.append(" ,RE.MOYSSTDT");	// F2 : 催し開始日
			sbSQL.append(" ,RE.MOYSRBAN");	// F3 : 催し連番
			sbSQL.append(" ,RE.TENCD");		// F4 : 店コード
			sbSQL.append(" ,RE.KYOSEIFLG");	// F5 : 強制グループフラグ
			sbSQL.append(" ,RE.TENGPCD");	// F6 : 店グループ
			sbSQL.append(" ,RE.LDTENKBN");	// F7 : リーダー店区分
			sbSQL.append(" ,"+sendflg);		// F8 : 送信フラグ
			sbSQL.append(" ,'"+userId+"'");		// F9 : オペレータ
			sbSQL.append(" ,current timestamp");	// F10: 登録日
			sbSQL.append(" ,current timestamp");	// F11: 更新日
			sbSQL.append(" )");
		}


		if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("全店特売(アンケート有)_店舗");
		return result;
	}
}
