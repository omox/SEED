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
import authentication.defines.Consts;
import common.CmnDate;
import common.DefineReport;
import common.Defines;
import common.ItemList;
import common.JsonArrayData;
import common.MessageUtility;
import common.MessageUtility.FieldType;
import common.MessageUtility.Msg;
import common.MessageUtility.MsgKey;
import dao.ReportTG016Dao.TOKTG_SHNLayout;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 */
public class ReportTG008Dao extends ItemDao {

	/**
	 * インスタンスを生成します。
	 * @param source
	 */
	public ReportTG008Dao(String JNDIname) {
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

		String szMoyskbn	= getMap().get("MOYSKBN");		// 催し区分
		String szMoysstdt	= getMap().get("MOYSSTDT");		// 催しコード（催し開始日）
		String szMoysrban	= getMap().get("MOYSRBAN");		// 催し連番
		String pushBtnid	= getMap().get("PUSHBTNID");	// 実行ボタン
		String sendPageid	= getMap().get("PAGEID");		// PAGEID

		JSONArray bumonArray	= JSONArray.fromObject(getMap().get("BUMON"));		// 部門
		JSONArray bumonAllArray	= JSONArray.fromObject(getMap().get("BUMON_DATA"));	// 全部門

		// パラメータ確認
		// 必須チェック
		if ((szMoyskbn == null)||(szMoysstdt == null)||(szMoysrban == null)||(sendPageid == null)) {
			System.out.println(super.getConditionLog());
			return "";
		}

		boolean isTG008 = StringUtils.equals(sendPageid, DefineReport.ID_PAGE_TG008);


		// タイトル情報(任意)設定
		List<String> titleList = new ArrayList<String>();

		// 基本情報取得
		JSONArray array = getTOKMOYCDData(getMap());

		// 一覧表情報
		StringBuffer sbSQL = new StringBuffer();
		if(StringUtils.isNotEmpty(pushBtnid)){
			sbSQL.append(DefineReport.ID_SQL_CMN_WEEK);
			sbSQL.append(" select F1,F2,F3,F4,F5,F6,F7,F8,F9,F10,F11,F12,F13,F14,F15");		// Datagrid表示内容
			sbSQL.append(" ,T1.BMNCD as F16");
			sbSQL.append(" ,T1.KANRINO as F17");
			sbSQL.append(" ,T1.KANRIENO as F18");
			sbSQL.append(" ,T1.ADDSHUKBN as F19");
			sbSQL.append(" ,T1.HDN_UPDDT as F20");
			sbSQL.append(" ,T1.UPDKBN as F21");
			sbSQL.append(" ,0 as F22");		// CHNAGE_IDX
			sbSQL.append(" ,T1.GTSIMECHGKBN as F23");
			sbSQL.append(" from (");
			sbSQL.append("  select");
			sbSQL.append("   T1.PARNO as F1");
			sbSQL.append("  ,RIGHT('00'||T1.CHLDNO,2) as F2");
			sbSQL.append("  ,M1.NMKN as F3");
			sbSQL.append("  ,case when T1.HIGAWRFLG = " + DefineReport.Values.ON.getVal() +" then '" + DefineReport.Values.ON.getVal() + "' end as F4");
			sbSQL.append("  ,trim(left(T1.SHNCD, 4) || '-' || SUBSTR(T1.SHNCD, 5)) as F5");
			sbSQL.append("  ,case when T1.ADDSHUKBN = " + DefineReport.ValAddShuKbn.VAL1.getVal() +" then '" + DefineReport.Values.ON.getVal() + "' end as F6");
			sbSQL.append("  ,case when (T1.SANCHIKN is null or trim(T1.SANCHIKN)='') then T1.MAKERKN else T1.SANCHIKN end as F7");
			sbSQL.append("  ,T1.POPKN as F8");
			sbSQL.append("  ,T1.KIKKN as F9");
			sbSQL.append("  ,CASE WHEN T1.HBSTDT = T1.HBEDDT THEN to_char(to_date(T1.HBSTDT, 'YYYYMMDD'), 'MM/DD')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(TO_DATE(T1.HBSTDT, 'YYYYMMDD')))");
			sbSQL.append("  ELSE to_char(to_date(T1.HBSTDT, 'YYYYMMDD'), 'MM/DD')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(TO_DATE(T1.HBSTDT, 'YYYYMMDD')))");
			sbSQL.append("   ||'～'||");
			sbSQL.append("   to_char(to_date(T1.HBEDDT, 'YYYYMMDD'), 'MM/DD')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(TO_DATE(T1.HBEDDT, 'YYYYMMDD'))) END F10");
			sbSQL.append("  ,case when T1.CHIRASFLG = " + DefineReport.Values.ON.getVal() +" then '" + DefineReport.Values.ON.getVal() + "' end as F11");
			sbSQL.append("  ,case when (T1.SEGN_NINZU is null or trim(T1.SEGN_NINZU)='') and (T1.SEGN_GENTEI is null or trim(T1.SEGN_GENTEI)='') and (T1.SEGN_1KOSU is null or trim(T1.SEGN_1KOSU)='') and (T1.SEGN_1KOSUTNI is null or trim(T1.SEGN_1KOSUTNI)='') then null ");
			sbSQL.append("   else nvl(trim(to_char(T1.SEGN_NINZU, '99,999')), '')||'，'||nvl(T1.SEGN_GENTEI, '')||'，'||nvl(trim(to_char(T1.SEGN_1KOSU, '99,999')), '')||'，'||nvl(T1.SEGN_1KOSUTNI, '') end as F12");
			sbSQL.append("  ,T1.POPCD as F13");
			if(isTG008){
				sbSQL.append("  ,'' as F14");
				sbSQL.append("  ,'' as F15");
			}else{
				sbSQL.append("  ,case when T1.GTSIMEOKFLG = " + DefineReport.Values.ON.getVal() +" then '" + DefineReport.Values.ON.getVal() + "' end as F14");
				sbSQL.append("  ,M2.NMKN as F15");
			}
			sbSQL.append("  ,T1.BMNCD");
			sbSQL.append("  ,T1.KANRINO");
			sbSQL.append("  ,T1.KANRIENO");
			sbSQL.append("  ,T1.ADDSHUKBN");
			sbSQL.append("  ,T1.HBSTDT");
			sbSQL.append("  ,T1.HBEDDT");
			sbSQL.append("  ,T1.GTSIMECHGKBN");
			sbSQL.append("  ,T1.GTSIMEOKFLG");
			sbSQL.append("  ,TO_CHAR(T1.UPDDT, 'YYYYMMDDHH24MISSNNNNNN') as HDN_UPDDT");
			sbSQL.append("  ,T1.UPDKBN");

			sbSQL.append("  ,row_number() over(partition by T1.MOYSKBN,T1.MOYSSTDT,T1.MOYSRBAN,T1.BMNCD,T1.KANRINO order by T1.KANRIENO desc) as RNO");
			sbSQL.append("  from INATK.TOKTG_SHN T1");
			sbSQL.append("  left outer join INAMS.MSTMEISHO M1 on M1.MEISHOKBN = '10660' and T1.MEDAMAKBN = trim(M1.MEISHOCD)");
			if(!isTG008){
				sbSQL.append("  left outer join INAMS.MSTMEISHO M2 on M2.MEISHOKBN = '10672' and T1.GTSIMECHGKBN = trim(M2.MEISHOCD)");
			}
			sbSQL.append("  where T1.UPDKBN = 0");
			sbSQL.append("    and T1.MOYSKBN = "+szMoyskbn+"");
			sbSQL.append("    and T1.MOYSSTDT = "+szMoysstdt+"");
			sbSQL.append("    and T1.MOYSRBAN = "+szMoysrban+"");
			// 部門
			if (bumonArray.optString(0).equals(DefineReport.Values.ALL.getVal())){
				sbSQL.append("    and RIGHT('0'||BMNCD,2) IN ("+StringUtils.removeEnd(StringUtils.replace(bumonAllArray.join(","),"\"","'"),",")+")");
			}else{
				sbSQL.append("    and RIGHT('0'||BMNCD,2) IN ("+StringUtils.removeEnd(StringUtils.replace(bumonArray.join(","),"\"","'"),",")+")");
			}
			sbSQL.append(" ) T1");
			sbSQL.append(" where RNO = 1");			// 全店特売（アンケート有）_商品.枝番がMAX
			sbSQL.append("   and T1.HBSTDT is not null");
			sbSQL.append("   and T1.HBEDDT is not null");
			if(!isTG008){
				sbSQL.append("   and T1.GTSIMECHGKBN <> 0");
				sbSQL.append("   and T1.GTSIMEOKFLG >= 0");
			}
			sbSQL.append(" order by F16,F1,F2,F17");
		}

		// オプション情報（タイトル）設定
		JSONObject option = new JSONObject();
		option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
		option.put("rows_", array);
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
	}


	/**
	 * 正情報取得処理
	 *
	 * @throws Exception
	 */
	public JSONArray getTOKMOYCDData(HashMap<String, String> map) {
		String szMoyskbn	= map.get("MOYSKBN");	// 催し区分
		String szMoysstdt	= map.get("MOYSSTDT");	// 催しコード（催し開始日）
		String szMoysrban	= map.get("MOYSRBAN");	// 催し連番

		ArrayList<String> paramData = new ArrayList<String>();


		StringBuffer sbSQL = new StringBuffer();
		sbSQL.append(DefineReport.ID_SQL_CMN_WEEK);
		sbSQL.append("  select");
		sbSQL.append("    T1.MOYSKBN||'-'||right(T1.MOYSSTDT, 6)||'-'||right('000'||T1.MOYSRBAN, 3) as F1");
		sbSQL.append("  , T1.MOYKN as F2");
		sbSQL.append("  , to_char(to_date(T1.HBSTDT, 'YYYYMMDD'), 'YY/MM/DD')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(TO_DATE(T1.HBSTDT, 'YYYYMMDD')))");
		sbSQL.append("    ||'～'||");
		sbSQL.append("    to_char(to_date(T1.HBEDDT, 'YYYYMMDD'), 'YY/MM/DD')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(TO_DATE(T1.HBEDDT, 'YYYYMMDD'))) as F3");
		sbSQL.append("  , case when T2.HBOKUREFLG = " + DefineReport.Values.ON.getVal() +" then '" + DefineReport.Values.ON.getVal() + "' end as F4");
		sbSQL.append("  , to_char(to_date(T2.GTSIMEDT,'YYYYMMDD'),'YY/MM/DD')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(TO_DATE(T2.GTSIMEDT, 'YYYYMMDD'))) as F5 ");
		sbSQL.append("  , case when T2.GTSIMEFLG = " + DefineReport.Values.ON.getVal() +" then '" + DefineReport.Values.ON.getVal() + "' end as F6");
		sbSQL.append("  , to_char(to_date(T2.LSIMEDT, 'YYYYMMDD'),'YY/MM/DD')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(TO_DATE(T2.LSIMEDT, 'YYYYMMDD'))) as F7");
		sbSQL.append("  , left(right(T2.QAYYYYMM, 4), 2)||'/'||right(T2.QAYYYYMM, 2) as F8");
		sbSQL.append("  , right('00'||nvl(T2.QAENO, 0), 2)  as F9");
		sbSQL.append("  , to_char(to_date(T2.QACREDT, 'YYYYMMDD'),'YYYY/MM/DD')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(TO_DATE(T2.QACREDT, 'YYYYMMDD'))) as F10");
		sbSQL.append("  , case when T2.HNCTLFLG = " + DefineReport.Values.ON.getVal() +" then '" + DefineReport.Values.ON.getVal() + "' end as F11");
		sbSQL.append("  , T1.OPERATOR as F12");
		sbSQL.append("  , nvl(to_char(T1.ADDDT, 'YY/MM/DD'),'__/__/__') as F13");
		sbSQL.append("  , nvl(to_char(T1.UPDDT, 'YY/MM/DD'),'__/__/__') as F14");
		sbSQL.append("  , to_char(T1.UPDDT, 'YYYYMMDDHH24MISSNNNNNN') as F15");
		sbSQL.append("  from INATK.TOKMOYCD T1");
		sbSQL.append("  left join INATK.TOKTG_KHN T2 on T1.MOYSKBN = T2.MOYSKBN and T1.MOYSSTDT = T2.MOYSSTDT and T1.MOYSRBAN = T2.MOYSRBAN and T2.UPDKBN = 0");
		sbSQL.append("  WHERE T1.UPDKBN = 0");
		sbSQL.append("  and T1.MOYSKBN = "+szMoyskbn+"");
		sbSQL.append("  and T1.MOYSSTDT = "+szMoysstdt+"");
		sbSQL.append("  and T1.MOYSRBAN = "+szMoysrban+"");

		ItemList iL = new ItemList();
		@SuppressWarnings("static-access")
		JSONArray array = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

		return array;
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
	protected JSONObject updateData(HashMap<String, String> map, User userInfo, String sysdate) throws Exception {
		JSONArray dataArray = JSONArray.fromObject(map.get("DATA"));					// 催し週

		JSONObject option = new JSONObject();
		JSONArray msg = new JSONArray();

		// パラメータ確認
		// 必須チェック
		if ( dataArray.isEmpty() || dataArray.size() == 0 || dataArray.getJSONObject(0).isEmpty()) {
			option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
			return option;
		}

		// ログインユーザー情報取得
		String userId	= userInfo.getId();								// ログインユーザー

		// --- 01-1.全店特売(アンケート有)_商品登録
		JSONObject result1 = this.createSqlTOKTG_SHN(userId, dataArray, true);

		// --- 01-2.全店特売(アンケート有)_商品 関連情報登録
		JSONObject result2 = this.createSqlTOKTG_SHN(userId, dataArray, false);

		// 排他チェック実行
		ArrayList<String> targetParam = new ArrayList<String>();
		if(dataArray.size() > 0){
			String rownum = "";						// エラー行数

			String targetTable = "INATK.TOKTG_SHN";
			String targetWhere = " MOYSKBN= ? and MOYSSTDT= ? and MOYSRBAN= ? and BMNCD = ? and KANRINO = ? and KANRIENO = ? and UPDKBN = 0";
			for (int i = 0; i < dataArray.size(); i++) {
				JSONObject data = dataArray.getJSONObject(i);
				if(data.isEmpty()){
					continue;
				}

				targetParam = new ArrayList<String>();
				targetParam.add(data.optString(TOKTG_SHNLayout.MOYSKBN.getId()));
				targetParam.add(data.optString(TOKTG_SHNLayout.MOYSSTDT.getId()));
				targetParam.add(data.optString(TOKTG_SHNLayout.MOYSRBAN.getId()));
				targetParam.add(data.optString(TOKTG_SHNLayout.BMNCD.getId()));
				targetParam.add(data.optString(TOKTG_SHNLayout.KANRINO.getId()));
				targetParam.add(data.optString(TOKTG_SHNLayout.KANRIENO.getId()));

				if(!super.checkExclusion(targetTable, targetWhere, targetParam, data.optString(TOKTG_SHNLayout.UPDDT.getId()))){
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
		String szShuno		= map.get("SHUNO");			// 入力商品コード
		String sendBtnid	= map.get("SENDBTNID");	// 呼出しボタン

		JSONArray dataArray = new JSONArray();//JSONArray.fromObject(map.get("DATA"));					// 催し週


		// ①正 .新規
		boolean isNew = DefineReport.Button.NEW.getObj().equals(sendBtnid);
		// ②正 .変更
		boolean isChange = DefineReport.Button.SEL_CHANGE.getObj().equals(sendBtnid);

		MessageUtility mu = new MessageUtility();

		List<JSONObject> msgList = this.checkData(
				isNew, isChange,
				map, userInfo, sysdate,mu, dataArray);


		JSONArray msgArray = new JSONArray();
		// MessageBoxを出す関係上、1件のみ表示
		if(msgList.size() > 0){
			msgArray.add(msgList.get(0));
		}
		return msgArray;
	}

	public List<JSONObject> checkData(
			boolean isNew, boolean isChange,
			HashMap<String, String> map, User userInfo, String sysdate1, MessageUtility mu, JSONArray dataArray			// 催し週
		) {

		JSONArray msg = new JSONArray();


		JSONObject data = dataArray.optJSONObject(0);


		String dbsysdate = CmnDate.dbDateFormat(sysdate1);



		String login_dt = sysdate1;	// 処理日付
		String sysdate = login_dt.substring(2, 6);				// 比較用処理日付

		return msg;
	}

	/**
	 * 関連全店特売(アンケート有)_商品 SQL作成処理
	 *
	 * @param userId
	 * @param data
	 *
	 * @throws Exception
	 */
	protected JSONObject createSqlTOKTG_SHN(String userId, JSONArray dataArray, boolean isBase) {
		JSONObject result = new JSONObject();

		TOKTG_SHNLayout[] target = new TOKTG_SHNLayout[]{TOKTG_SHNLayout.MOYSKBN, TOKTG_SHNLayout.MOYSSTDT,TOKTG_SHNLayout.MOYSRBAN
				, TOKTG_SHNLayout.BMNCD, TOKTG_SHNLayout.KANRINO, TOKTG_SHNLayout.KANRIENO, TOKTG_SHNLayout.GTSIMECHGKBN, TOKTG_SHNLayout.GTSIMEOKFLG};

		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();
		String values = "", names = "", rows = "";
		int colNum = target.length;		// テーブル列数
		for(int j=0; j < dataArray.size(); j++){
			values = "";
			names = "";
			for (TOKTG_SHNLayout itm :  target) {
				String col = itm.getCol();
				String val = StringUtils.trim(dataArray.optJSONObject(j).optString(itm.getId()));
				if(StringUtils.isEmpty(val)){
					values += ", null";
				}else{
					prmData.add(val);
					values += ", cast(? as varchar("+MessageUtility.getDefByteLen(val)+"))";
				}
				names  += ", "+col;
			}
			rows += ",("+StringUtils.removeStart(values, ",")+")";
		}
		rows = StringUtils.removeStart(rows, ",");
		names  = StringUtils.removeStart(names, ",");

		// 基本Merge文
		StringBuffer sbSQL;
		sbSQL = new StringBuffer();
		sbSQL.append("merge into INATK.TOKTG_SHN as T");
		sbSQL.append(" using (select ");
		for (TOKTG_SHNLayout itm :  target) {
			if(itm.getNo() > 1){ sbSQL.append(","); }
			sbSQL.append("cast(T1."+itm.getCol()+" as " + itm.getTyp() + ") as "+itm.getCol());
		}
		sbSQL.append("  from (values"+rows+") as T1("+names+")");
		sbSQL.append(" ) as RE on ( ");
		sbSQL.append(" T.MOYSKBN = RE.MOYSKBN and T.MOYSSTDT = RE.MOYSSTDT and T.MOYSRBAN = RE.MOYSRBAN ");
		sbSQL.append(" and T.BMNCD = RE.BMNCD and T.KANRINO = RE.KANRINO ");
		if(isBase){
			sbSQL.append(" and T.KANRIENO = RE.KANRIENO ");
		}else{
			sbSQL.append(" and not(T.KANRIENO = RE.KANRIENO) and RE.GTSIMECHGKBN = 3 ");
		}
		sbSQL.append(" and T.UPDKBN="+DefineReport.ValUpdkbn.NML.getVal());		// F21: 更新区分
		sbSQL.append(" )");
		sbSQL.append(" when matched then ");
		sbSQL.append(" update set");
		sbSQL.append("  UPDKBN=case when RE.GTSIMECHGKBN = 3 then "+DefineReport.ValUpdkbn.DEL.getVal()+" else "+DefineReport.ValUpdkbn.NML.getVal()+" end");		// 更新区分
		if(isBase){
			sbSQL.append(" ,GTSIMEOKFLG=RE.GTSIMEOKFLG");	// 月締変更許可フラグ
		}
		sbSQL.append(" ,SENDFLG="+DefineReport.Values.SENDFLG_UN.getVal());	// 送信フラグ
		sbSQL.append(" ,OPERATOR='"+userId+"'");		// オペレータ
		//sbSQL.append(" ,ADDDT=RE.ADDDT");				// 登録日
		sbSQL.append(" ,UPDDT=current timestamp");		// 更新日

		if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		String addLbl = "";
		if(!isBase){ addLbl = "関連";}

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("全店特売(アンケート有)_商品" + addLbl);
		return result;
	}
}
