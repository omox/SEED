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
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 */
public class ReportSA005Dao extends ItemDao {

	/**
	 * インスタンスを生成します。
	 * @param source
	 */
	public ReportSA005Dao(String JNDIname) {
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

		String szMoyskbn	= getMap().get("MOYSKBN");	// 催し区分
		String szMoysstdt	= getMap().get("MOYSSTDT");	// 催しコード（催し開始日）
		String szMoysrban	= getMap().get("MOYSRBAN");	// 催し連番
		String szPushbtnID	= getMap().get("PUSHBTNID");	// ボタンID
		String szQasyukbn	= getMap().get("QASYUKBN");	// 店コード
		String szBmn	= getMap().get("BMN[]");	// 店コード
		String szTengpcd = getMap().get("TENGPCD");	// 店グループコード

		// パラメータ確認
		// 必須チェック
		if ((szMoyskbn == null) || (szMoysstdt == null) || (szMoysrban == null)) {
			System.out.println(super.getConditionLog());
			return "";
		}
		ArrayList<String> paramData = new ArrayList<String>();
		// タイトル情報(任意)設定
		List<String> titleList = new ArrayList<String>();

		// 基本情報取得
		JSONArray array = getTOKMOYCDData(getMap());

		StringBuffer sbSQL = new StringBuffer();
		// 一覧表情報
		sbSQL.append(DefineReport.ID_SQL_CMN_WEEK);
		sbSQL.append("select  ");
		sbSQL.append("    CONCAT(CONCAT(left(TTSH.SHNCD,4),'-'),SUBSTRING(TTSH.SHNCD,5,4)) ");//F1
		sbSQL.append("  , ( case when TTSH.SANCHIKN <> null then CONCAT(CONCAT(TTSH.SANCHIKN, TTSH.POPKN), TTSH.KIKKN) else CONCAT(CONCAT(TTSH.MAKERKN, TTSH.POPKN), TTSH.KIKKN) END ) ");//F2
		sbSQL.append("  , TTSH.CHIRASFLG  ");//F3
		sbSQL.append("  , TTSH.HBSLIDEFLG ");//F4
		sbSQL.append("  , TO_CHAR(TO_DATE(TMYC.HBSTDT, 'YYYYMMDD'), 'YY/MM/DD')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(TO_DATE(TMYC.HBSTDT, 'YYYYMMDD')))");
		sbSQL.append("    ||'～'||");
		sbSQL.append("    TO_CHAR(TO_DATE(TMYC.HBEDDT, 'YYYYMMDD'), 'YY/MM/DD')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(TO_DATE(TMYC.HBEDDT, 'YYYYMMDD'))) ");//F5
		sbSQL.append("  , ( case when TTSH.TKANPLUKBN = 2 then TTSH.GENKAAM_1KG else TTSH.GENKAAM_MAE END ) ");//F6
		sbSQL.append("  , ( case when TTSH.TKANPLUKBN = 2 then 'G' else '' END  ) ");//F7
		sbSQL.append("  , ( case when TTSH.ADDSHUKBN <> 01 then ( case when TTSH.BD1_A_BAIKAAN > 0 then TO_CHAR(TTSH.KO_A_BAIKAAN) else TO_CHAR(TTSH.A_BAIKAAM) END)");
		sbSQL.append(" else  (select MMSH.NMKN from INAMS.MSTMEISHO MMSH where MMSH.MEISHOKBN = 10302 and MMSH.MEISHOCD = char(TTSH.A_WRITUKBN) ) END)  ");//B割引始F8
		sbSQL.append("  , (case when TTSH.BD1_A_BAIKAAN = 0 or TTSH.BD1_A_BAIKAAN is null then '' else CONCAT(TO_CHAR(TTSH.BD1_TENSU),CONCAT('個/', TO_CHAR(TTSH.BD1_A_BAIKAAN))) END ) ");//F9
		sbSQL.append("  , (case when TTSH.BD2_A_BAIKAAN = 0 or TTSH.BD2_A_BAIKAAN is null then '' else CONCAT(TO_CHAR(TTSH.BD2_TENSU),CONCAT('個/', TO_CHAR(TTSH.BD2_A_BAIKAAN))) END ) ");//F10
		sbSQL.append("  , (case when TTSH.ADDSHUKBN <> 01 then (case when TTSH.BD1_B_BAIKAAN > 0 then TO_CHAR(TTSH.KO_B_BAIKAAN) else TO_CHAR(TTSH.B_BAIKAAM) END)");
		sbSQL.append("  else (select MMSH.NMKN from INAMS.MSTMEISHO MMSH where MMSH.MEISHOKBN = 10302 and MMSH.MEISHOCD = char (TTSH.B_WRITUKBN)) END)");//F11
		sbSQL.append("  , (case when TTSH.BD1_B_BAIKAAN = 0 or TTSH.BD1_B_BAIKAAN is null then '' else CONCAT(TO_CHAR(TTSH.BD1_TENSU),CONCAT('個/', TO_CHAR(TTSH.BD1_B_BAIKAAN))) END ) ");//F12
		sbSQL.append("  , (case when TTSH.BD2_B_BAIKAAN = 0 or TTSH.BD2_B_BAIKAAN is null then '' else CONCAT(TO_CHAR(TTSH.BD2_TENSU),CONCAT('個/', TO_CHAR(TTSH.BD2_B_BAIKAAN))) END ) ");//F13
		sbSQL.append("  , (case when TTSH.ADDSHUKBN <> 01 then (case when TTSH.BD1_C_BAIKAAN > 0 then TO_CHAR(TTSH.KO_C_BAIKAAN) else TO_CHAR(TTSH.C_BAIKAAM) END)");
		sbSQL.append("  else (select MMSH.NMKN from INAMS.MSTMEISHO MMSH where MMSH.MEISHOKBN = 10302 and MMSH.MEISHOCD = char (TTSH.C_WRITUKBN)) END)");//F14
		sbSQL.append("  , (case when TTSH.BD1_C_BAIKAAN = 0 or TTSH.BD1_C_BAIKAAN is null then '' else CONCAT(TO_CHAR(TTSH.BD1_TENSU),CONCAT('個/', TO_CHAR(TTSH.BD1_C_BAIKAAN))) END ) ");//F15
		sbSQL.append("  , (case when TTSH.BD2_C_BAIKAAN = 0 or TTSH.BD2_C_BAIKAAN is null then '' else CONCAT(TO_CHAR(TTSH.BD2_TENSU),CONCAT('個/', TO_CHAR(TTSH.BD2_C_BAIKAAN))) END ) ");//F16
		sbSQL.append("  , TTQS.URICHGAM1");//F17希望総売価<br>（1売り）
		sbSQL.append("  , TTQS.URICHGAM2");//F18希望総売価<br>（バンドル１）
		sbSQL.append("  , TTQS.URICHGAM3");//F19希望総売価<br>（バンドル２）
		sbSQL.append("  , TTSH.BD1_A_BAIKAAN  ");//A総売価（バンドル１）F20
		sbSQL.append("  , TTSH.BD2_A_BAIKAAN  ");//A総売価（バンドル２）F21
		sbSQL.append("  , TTQS.BMNCD  ");//F22
		sbSQL.append("  , TTQS.KANRINO  ");//F23
		sbSQL.append("  , TTQS.KANRIENO  ");//F24
		sbSQL.append("  , TTKH.TPNG2FLG  ");//F25
		sbSQL.append("  , TTSH.ADDSHUKBN  ");//F26
		sbSQL.append("  , TTSH.BD1_TENSU  ");//F27
		sbSQL.append("  , TTSH.BD2_TENSU  ");//F28
		sbSQL.append("  , TTSH.B_BAIKAAM  ");//F29
		sbSQL.append("  , TTSH.C_BAIKAAM  ");//F30
		sbSQL.append("  , TTSH.A_WRITUKBN  ");//F30
		sbSQL.append("  , TTSH.B_WRITUKBN  ");//F30
		sbSQL.append("  , TTSH.C_WRITUKBN  ");//F30
		sbSQL.append(" from ");
		sbSQL.append(" INATK.TOKTG_SHN TTSH ");
		sbSQL.append(" left join INATK.TOKTG_KHN TTKH");
		sbSQL.append(" on TTKH.MOYSKBN = ? ");
		paramData.add(szMoyskbn);
		sbSQL.append(" and TTKH.MOYSSTDT = ? ");
		paramData.add(szMoysstdt);
		sbSQL.append(" and TTKH.MOYSRBAN = ? ");
		paramData.add(szMoysrban);
		sbSQL.append(" left join INATK.TOKTG_QAGP TTQG  ");
		sbSQL.append(" on TTQG.MOYSKBN = ? ");
		paramData.add(szMoyskbn);
		sbSQL.append(" and TTQG.MOYSSTDT = ? ");
		paramData.add(szMoysstdt);
		sbSQL.append(" and TTQG.MOYSRBAN = ? ");
		paramData.add(szMoysrban);
		sbSQL.append(" and TTQG.TENGPCD = ? ");
		paramData.add(szTengpcd);
		sbSQL.append(" left join INATK.TOKMOYCD TMYC ");
		sbSQL.append(" on TMYC.MOYSKBN = ? ");
		paramData.add(szMoyskbn);
		sbSQL.append(" and TMYC.MOYSSTDT = ? ");
		paramData.add(szMoysstdt);
		sbSQL.append(" and TMYC.MOYSRBAN = ? ");
		paramData.add(szMoysrban);
		sbSQL.append(" left join INATK.TOKTG_QASHN TTQS ");
		sbSQL.append(" on TTQS.MOYSKBN = ? ");
		paramData.add(szMoyskbn);
		sbSQL.append(" and TTQS.MOYSSTDT = ? ");
		paramData.add(szMoysstdt);
		sbSQL.append(" and TTQS.MOYSRBAN = ? ");
		paramData.add(szMoysrban);
		sbSQL.append(" and TTQS.TENGPCD = ? ");
		paramData.add(szTengpcd);
		sbSQL.append(" and TTQS.KANRINO = TTSH.KANRINO");
		sbSQL.append(" and TTQS.BMNCD = TTSH.BMNCD ");
		sbSQL.append(" and TTQS.KANRIENO = TTSH.KANRIENO ");
		sbSQL.append(" where ");
		sbSQL.append(" TTSH.MOYSKBN = ? ");
		paramData.add(szMoyskbn);
		sbSQL.append(" and TTSH.MOYSSTDT = ? ");
		paramData.add(szMoysstdt);
		sbSQL.append(" and TTSH.MOYSRBAN = ? ");
		paramData.add(szMoysrban);
		sbSQL.append(" and TTKH.MOYSKBN = ? ");
		paramData.add(szMoyskbn);
		sbSQL.append(" and TTKH.MOYSSTDT = ? ");
		paramData.add(szMoysstdt);
		sbSQL.append(" and TTKH.MOYSRBAN = ? ");
		paramData.add(szMoysrban);
		sbSQL.append(" and TTQG.MOYSKBN = ? ");
		paramData.add(szMoyskbn);
		sbSQL.append(" and TTQG.MOYSSTDT = ? ");
		paramData.add(szMoysstdt);
		sbSQL.append(" and TTQG.MOYSRBAN = ? ");
		paramData.add(szMoysrban);
		sbSQL.append(" and TMYC.MOYSKBN = ? ");
		paramData.add(szMoyskbn);
		sbSQL.append(" and TMYC.MOYSSTDT = ? ");
		paramData.add(szMoysstdt);
		sbSQL.append(" and TMYC.MOYSRBAN = ? ");
		paramData.add(szMoysrban);
		sbSQL.append(" and TTQS.MOYSKBN = ? ");
		paramData.add(szMoyskbn);
		sbSQL.append(" and TTQS.MOYSSTDT = ? ");
		paramData.add(szMoysstdt);
		sbSQL.append(" and TTQS.MOYSRBAN = ? ");
		paramData.add(szMoysrban);
		sbSQL.append(" and TTQS.TENGPCD = ? ");
		paramData.add(szTengpcd);
		sbSQL.append(" and TTQS.KANRINO = TTSH.KANRINO");
		sbSQL.append(" and TTQS.BMNCD = TTSH.BMNCD ");
		sbSQL.append(" and TTQS.KANRIENO = TTSH.KANRIENO");
		sbSQL.append(" and TTQS.UPDKBN = 0");
		sbSQL.append(" order by TTQS.CSVSEQNO");

		setParamData(paramData);

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
		String szTENCD	= map.get("TENCD");	// 催し連番
		String szTengpcd = getMap().get("TENGPCD");	// 店グループコード

		ArrayList<String> paramData = new ArrayList<String>();

		StringBuffer sbSQL = new StringBuffer();
		sbSQL.append(DefineReport.ID_SQL_CMN_WEEK);
		sbSQL.append("  select");
		sbSQL.append("    MTTN.TENKN as F1");
		sbSQL.append("  , TTTG.TENGPKN as F2");
		sbSQL.append("  , TTQT.TENCD as F3");
		sbSQL.append("  , TO_CHAR(TO_DATE(TMYC.HBSTDT, 'YYYYMMDD'), 'YY/MM/DD')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(TO_DATE(TMYC.HBSTDT, 'YYYYMMDD')))");
		sbSQL.append("    ||'～'||");
		sbSQL.append("    TO_CHAR(TO_DATE(TMYC.HBEDDT, 'YYYYMMDD'), 'YY/MM/DD')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(TO_DATE(TMYC.HBEDDT, 'YYYYMMDD'))) as F4");
		sbSQL.append("  , TMYC.MOYKN as F5");
		sbSQL.append("  , TTQG.LDSYFLG as F6");
		sbSQL.append("  , TTQG.URIASELKBN as F7");
		sbSQL.append("  , (case when TTQG.ITMANSFLG = 1 and TTQG.SCHANSFLG = 1 then '〇' else '×' END) as F8");
		sbSQL.append("  , TTKH.TPNG2FLG as F9");
		sbSQL.append("  , TTKH.TPNG1FLG as F10");
		sbSQL.append("  , TTKH.TPNG3FLG as F11");
		sbSQL.append("  , TTKH.HBOKUREFLG as F12");
		sbSQL.append("  , TTKH.JLSTCREFLG  as F13");
		sbSQL.append("  , TO_CHAR(TTQG.UPDDT, 'YYYYMMDDHH24MISSNNNNNN') as F14 ");
		sbSQL.append("  from ");
		sbSQL.append("  INATK.TOKTG_QATEN TTQT");
		sbSQL.append("  left join INAMS.MSTTEN MTTN");
		sbSQL.append("  on MTTN.TENCD = ? ");
		paramData.add(szTENCD);
		sbSQL.append("  left  join INATK.TOKTG_TENGP TTTG");
		sbSQL.append("  on TTTG.MOYSKBN = ? ");
		paramData.add(szMoyskbn);
		sbSQL.append("  and TTTG.MOYSSTDT = ? ");
		paramData.add(szMoysstdt);
		sbSQL.append("  and TTTG.MOYSRBAN = ? ");
		paramData.add(szMoysrban);
		sbSQL.append(" and TTTG.TENGPCD = TTQT.TENGPCD");
		sbSQL.append(" left join INATK.TOKMOYCD TMYC");
		sbSQL.append(" on TMYC.MOYSKBN = ? ");
		paramData.add(szMoyskbn);
		sbSQL.append("  and TMYC.MOYSSTDT = ? ");
		paramData.add(szMoysstdt);
		sbSQL.append("  and TMYC.MOYSRBAN = ? ");
		paramData.add(szMoysrban);
		sbSQL.append("  left join INATK.TOKTG_QAGP TTQG");
		sbSQL.append("  on TTQG.MOYSKBN = ? ");
		paramData.add(szMoyskbn);
		sbSQL.append("  and TTQG.MOYSSTDT = ? ");
		paramData.add(szMoysstdt);
		sbSQL.append("  and TTQG.MOYSRBAN = ? ");
		paramData.add(szMoysrban);
		sbSQL.append("  and TTQG.TENGPCD = ? ");
		paramData.add(szTengpcd);
		sbSQL.append("  left join INATK.TOKTG_KHN TTKH");
		sbSQL.append("  on TTKH.MOYSKBN = ? ");
		paramData.add(szMoyskbn);
		sbSQL.append("  and TTKH.MOYSSTDT = ? ");
		paramData.add(szMoysstdt);
		sbSQL.append("  and TTKH.MOYSRBAN = ? ");
		paramData.add(szMoysrban);
		sbSQL.append("  where");
		sbSQL.append("  TTQT.MOYSSTDT = ? ");
		paramData.add(szMoysstdt);
		sbSQL.append("  and TTQT.MOYSRBAN = ? ");
		paramData.add(szMoysrban);
		sbSQL.append("  and TTQT.MOYSKBN = ? ");
		paramData.add(szMoyskbn);
		sbSQL.append("  and TTQT.TENCD = ? ");
		paramData.add(szTENCD);

		ItemList iL = new ItemList();
		@SuppressWarnings("static-access")
		JSONArray array = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

		return array;
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
		JSONObject msgObj = new JSONObject();
		JSONArray msg = this.check(map, userInfo, sysdate);

		if(msg.size() > 0){
			msgObj.put(MsgKey.E.getKey(), msg);
			return msgObj;
		}
		// 更新処理
		try {
			msgObj = this.updateData(map, userInfo, sysdate);
		} catch (Exception e) {
			e.printStackTrace();
			msgObj.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
		}
		//msgObj.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00001.getVal()));
		return msgObj;
	}
	/**
	 * チェック処理
	 *
	 * @throws Exception
	 */
	public JSONArray check(HashMap<String, String> map, User userInfo, String sysdate) {
		// パラメータ確認
		JSONArray dataArray = JSONArray.fromObject(map.get("SHNDATA"));	// 更新情報

		MessageUtility mu = new MessageUtility();

		List<JSONObject> msgList = this.checkData(map,userInfo,sysdate,mu,dataArray);

		JSONArray msgArray = new JSONArray();

		if(msgList.size() > 0){
			msgArray.add(msgList.get(0));
		}
		return msgArray;
	}
	public List<JSONObject> checkData(
			HashMap<String, String> map, User userInfo, String sysdate1, MessageUtility mu,
			JSONArray dataArray			// 対象情報（主要な更新情報）
			) {
		JSONArray msg = new JSONArray();
		JSONObject data = dataArray.optJSONObject(0);
		for(int j=0; j < dataArray.size(); j++){
			// 更新情報
			String URICHGAM1 = data.optString("F1");//希望総売価（1売り）
			String URICHGAM2 = data.optString("F2");//希望総売価　バンドル1
			String URICHGAM3 = data.optString("F3");//希望総売価　バンドル2

			String BD1ABAIKAAN = data.optString("F11");//A総売価（バンドル１）
			String BD2ABAIKAAN = data.optString("F12");//A総売価（バンドル２）
			String ADDSHUKBN = data.optString("F13");//登録種別
			String BD1TENSU = data.optString("F14");//点数_バンドル１
			String BD2TENSU = data.optString("F15");//点数_バンドル２
			String BBAIKAAM = data.optString("F16");//B総売価
			String CBAIKAAM = data.optString("F17");//C総売価
			String TPNG2FLG = data.optString("F18");//売価選択禁止フラグ

			if(!String.valueOf(TPNG2FLG).equals("1")||(BBAIKAAM.length()==0 && CBAIKAAM.length()==0)){
				if(Integer.parseInt(URICHGAM1) < 1){
					JSONObject o = mu.getDbMessageObj("E20467", new String[]{});
					//希望総売価(1売り)は1以上を入力してください。
					msg.add(o);
					return msg;
				}
				if( !String.valueOf(ADDSHUKBN).equals("1") && BD1ABAIKAAN.length()>0 && !String.valueOf(BD1ABAIKAAN).equals("0")
						&& BD2ABAIKAAN.length()>0 && !String.valueOf(BD2ABAIKAAN).equals("0") ){
					if(Integer.parseInt(URICHGAM1) >= Integer.parseInt(URICHGAM2) || Integer.parseInt(URICHGAM2) >= Integer.parseInt(URICHGAM3)){
						JSONObject o = mu.getDbMessageObj("E20097", new String[]{});
						//希望総売価(1売り)＜希望総売価（バンドル１）＜希望総売価（バンドル２）です。
						msg.add(o);
						return msg;
					}
					if(Integer.parseInt(URICHGAM1) < Integer.parseInt(URICHGAM2)/Integer.parseInt(BD1TENSU)){
						JSONObject o = mu.getDbMessageObj("E20603", new String[]{});
						//希望総売価（バンドル１）の平均売価（＝円/個）が希望総売価(1売り)より大きくなっています。
						msg.add(o);
						return msg;
					}else if(Integer.parseInt(URICHGAM2)/Integer.parseInt(BD1TENSU) < Integer.parseInt(URICHGAM3)/Integer.parseInt(BD2TENSU)){
						JSONObject o = mu.getDbMessageObj("E20604", new String[]{});
						//希望総売価（バンドル２）の平均売価≦希望総売価（バンドル１）の平均売価の範囲で入力してください。
						msg.add(o);
						return msg;
					}
				}
				if( !String.valueOf(ADDSHUKBN).equals("1") && BD1ABAIKAAN.length()>0 && !String.valueOf(BD1ABAIKAAN).equals("0") ){
					if(Integer.parseInt(URICHGAM1) >= Integer.parseInt(URICHGAM2)){
						JSONObject o = mu.getDbMessageObj("E20470", new String[]{});
						msg.add(o);
						return msg;
					}
					if(Integer.parseInt(URICHGAM1) < Integer.parseInt(URICHGAM2)/Integer.parseInt(BD1TENSU)){
						JSONObject o = mu.getDbMessageObj("E20603", new String[]{});
						msg.add(o);
						return msg;
					}
				}
			}
		}
		return msg;
	}
	boolean isTest = false;

	/**
	 * 更新処理実行
	 * @return
	 *
	 * @throws Exception
	 */
	private JSONObject updateData(HashMap<String, String> map, User userInfo, String sysdate) throws Exception {

		String dbsysdate = CmnDate.dbDateFormat(sysdate);

		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();
		String values = "";
		int kryoColNum = 7;		// テーブル列数
		// パラメータ確認
		JSONArray dataArray = JSONArray.fromObject(map.get("DATA"));	// 対象情報
		String TPNG2FLG = dataArray.optJSONObject(0).optString("F9");
		String ADOPT = map.get("ADOPT");								// 採用区分
		String TENGPCD = map.get("TENGPCD");							// 店グループコード
		String MOYSKBN = map.get("MOYSKBN");							// 催し区分
		String MOYSSTDT = map.get("MOYSSTDT");							// 催し開始日
		String MOYSRBAN = map.get("MOYSRBAN");							// 催し連番

		JSONObject msgObj = new JSONObject();
		JSONArray msg = new JSONArray();
		JSONObject option	 = new JSONObject();

		// ログインユーザー情報取得
		String userId	= userInfo.getId();								// ログインユーザー

		String updateRows = "";											// 更新データ

		// 更新情報
		values = "";
		for (int i = 1; i <= kryoColNum; i++) {
			if(isTest){
				if(i==1){
					values += " '"+ADOPT+"'";
					//LDSYFLG
				}else if(i==2){
					// SCHANSFLG
					values += ", '"+1+"'";
				}else if(i==3){
					// ITMANSFLG
					values += ", '"+1+"'";
				}else if(i==4){
					// MOYSKBN
					values += ", '"+MOYSKBN+"'";
				}else if(i==5){
					// MOYSSTDT
					values += ", '"+MOYSSTDT+"'";
				}else if(i==6){
					// MOYSRBAN
					values += ", '"+MOYSRBAN+"'";
				}else if(i==7){
					// TENGPCD
					values += ", '"+TENGPCD+"'";
				}
			}else{

				if(i==1){
					prmData.add(ADOPT);
					//LDSYFLG
				}else if(i==2){
					// SCHANSFLG
					prmData.add("1");
				}else if(i==3){
					// ITMANSFLG
					prmData.add("1");
				}else if(i==4){
					// MOYSKBN
					prmData.add(MOYSKBN);
				}else if(i==5){
					// MOYSSTDT
					prmData.add(MOYSSTDT);
				}else if(i==6){
					// MOYSRBAN
					prmData.add(MOYSRBAN);
				}else if(i==7){
					// TENGPCD
					prmData.add(TENGPCD);
				}
				values += ", ?";
			}
		}
		//ループ抜けた後に先頭のカンマ消しているのおかしい
		updateRows += ",("+StringUtils.removeStart(values, ",")+")";
		updateRows = StringUtils.removeStart(updateRows, ",");

		if(values.length()==0){
			msgObj.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
			return msgObj;
		}
		// 基本INSERT/UPDATE文
		StringBuffer sbSQL = new StringBuffer();;

		sbSQL.append("MERGE INTO INATK.TOKTG_QAGP AS T USING (SELECT ");
		sbSQL.append("  LDSYFLG");													// 催し区分：
		sbSQL.append(", SCHANSFLG");													// 催し連番：
		sbSQL.append(", ITMANSFLG");													// 採用フラグ：
		sbSQL.append(", MOYSKBN");												// 回答フラグ：
		sbSQL.append(", MOYSSTDT");													// エリア区分：
		sbSQL.append(", MOYSRBAN");													// 店コード：
		sbSQL.append(", TENGPCD");												// 強制フラグ：
		sbSQL.append(", "+DefineReport.Values.SENDFLG_UN.getVal()+" AS SENDFLG");		// 送信区分：
		sbSQL.append(", '"+userId+"' AS OPERATOR ");								// オペレーター：
		sbSQL.append(", current timestamp AS ADDDT ");								// 登録日：
		sbSQL.append(", current timestamp AS UPDDT ");								// 更新日：
		sbSQL.append(" FROM (values "+updateRows+") as T1(");
		sbSQL.append(" LDSYFLG ");													// F1	: 配送グループ
		sbSQL.append(" ,SCHANSFLG ");													// F3	: 配送グループ名称（カナ）
		sbSQL.append(" ,ITMANSFLG ");													// F7	: エリア区分
		sbSQL.append(" ,MOYSKBN ");													// F7	: エリア区分
		sbSQL.append(" ,MOYSSTDT ");													// F7	: エリア区分
		sbSQL.append(" ,MOYSRBAN ");													// F7	: エリア区分
		sbSQL.append(" ,TENGPCD ");													// F7	: エリア区分
		sbSQL.append(" )) as RE on (");
		sbSQL.append(" T.MOYSKBN = RE.MOYSKBN and ");
		sbSQL.append(" T.MOYSSTDT = RE.MOYSSTDT and ");
		sbSQL.append(" T.MOYSRBAN = RE.MOYSRBAN and ");
		sbSQL.append(" T.TENGPCD = RE.TENGPCD ");
		sbSQL.append(" ) WHEN MATCHED THEN UPDATE SET ");
		sbSQL.append("  LDSYFLG = RE.LDSYFLG");
		sbSQL.append(", SCHANSFLG = RE.SCHANSFLG");
		sbSQL.append(", ITMANSFLG = RE.ITMANSFLG");
		sbSQL.append(", SENDFLG=RE.SENDFLG ");
		sbSQL.append(", OPERATOR = RE.OPERATOR");
		sbSQL.append(", ADDDT = RE.ADDDT");
		sbSQL.append(", UPDDT = RE.UPDDT");
		if (msg.size() > 0) {
			// 重複チェック_エラー時
			msgObj.put(MsgKey.E.getKey(), msg);
		}else{
			// 更新処理実行
			if (DefineReport.ID_DEBUG_MODE) System.out.println(sbSQL);

			// 排他チェック実行
			String targetTable = null;
			String targetWhere = null;
			ArrayList<String> targetParam = new ArrayList<String>();
			// ⑨CSVエラー修正

			// 排他チェック：大分類グリッド
			if(dataArray.size() > 0){
				String rownum = "";						// エラー行数

				for (int i = 0; i < dataArray.size(); i++) {
					JSONObject data = dataArray.getJSONObject(i);
					if(data.isEmpty()){
						continue;
					}

					targetTable = "INATK.TOKTG_QAGP";
					targetWhere = "MOYSKBN = ? and MOYSSTDT = ? and MOYSRBAN = ? and TENGPCD = ? ";
					targetParam = new ArrayList<String>();
					targetParam.add(MOYSKBN);
					targetParam.add(MOYSSTDT);
					targetParam.add(MOYSRBAN);
					targetParam.add(TENGPCD);

					if(!super.checkExclusion(targetTable, targetWhere, targetParam, data.optString("F14"))){
						//							rownum = data.optString("idx");
						rownum = String.valueOf(i+1);
						msg.add(MessageUtility.getDbMessageExclusion(FieldType.GRID, new String[]{rownum}));
						option.put(MsgKey.E.getKey(), msg);
						return option;
					}
				}
			}

			int count = super.executeSQL(sbSQL.toString(), prmData);
			if(StringUtils.isEmpty(getMessage())) {
				if (DefineReport.ID_DEBUG_MODE) System.out.println("アンケート結果_店グループを " + MessageUtility.getMessage(Msg.S00003.getVal(),
						new String[] { Integer.toString(dataArray.size()), Integer.toString(count) }));
				msgObj.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00001.getVal()));
			}else{
				msgObj.put(MsgKey.E.getKey(), getMessage());
				return msgObj;
			}
			String shnValues = "";
			JSONArray shnDataArray = JSONArray.fromObject(map.get("SHNDATA"));	// 対象情報
			int ColNum = 10;		// テーブル列数
			updateRows = "";
			prmData = new ArrayList<String>();

			// 更新情報
			for(int j=0; j < shnDataArray.size(); j++){
				shnValues = "";
				String hanteiValue = "";
				for (int i = 1; i <= ColNum; i++) {
					String col = "F" + i;
					String val = shnDataArray.optJSONObject(j).optString(col);

					if(isTest){
						if(i==1){
							//希望総売価(1売り)
							shnValues += " '"+val+"'";
							if(String.valueOf(TPNG2FLG).equals("1") ){
								shnValues = " '"+shnDataArray.optJSONObject(j).optString("F4")+"'";
							}
						}else if(i==2){
							//希望総売価（バンドル１）
							hanteiValue = val;
							if(String.valueOf(TPNG2FLG).equals("1") ){
								hanteiValue = shnDataArray.optJSONObject(j).optString("F5");
							}
							if(!hanteiValue.isEmpty()){
								shnValues += ", '"+hanteiValue+"'";
							}else{
								shnValues += ", null ";
							}
						}else if(i==3 ){
							// 希望総売価（バンドル２）
							hanteiValue = val;
							if(String.valueOf(TPNG2FLG).equals("1") ){
								hanteiValue = shnDataArray.optJSONObject(j).optString("F6");
							}
							if(!hanteiValue.isEmpty()){
								shnValues += ", '"+hanteiValue+"'";
							}else{
								shnValues += ", null ";
							}
						}else if(i == 4){
							// 催し区分
							shnValues += ", '"+MOYSKBN+"'";
						}else if(i==5){
							// 催し開始日
							shnValues += ", '"+MOYSSTDT+"'";
						}else if(i==6){
							//催し連番
							shnValues += ", '"+MOYSRBAN+"'";
						}else if(i==7){
							//店グループコード
							shnValues += ", '"+TENGPCD+"'";
						}else if(i==8){
							// 部門コード
							shnValues += ", '"+val+"'";
						}else if(i==9){
							// 管理番号
							shnValues += ", '"+val+"'";
						}else if(i==10){
							// 枝番
							shnValues += ", '"+val+"'";
						}
					}else{
						if(i==1){
							//希望総売価(1売り)
							shnValues += ", ?";
							prmData.add(val);
							if(String.valueOf(TPNG2FLG).equals("1") ){
								prmData.add(shnDataArray.optJSONObject(j).optString("F4"));
								shnValues += ", null ";
							}
						}else if(i==2){
							//希望総売価（バンドル１）
							hanteiValue = val;
							if(String.valueOf(TPNG2FLG).equals("1") ){
								hanteiValue = shnDataArray.optJSONObject(j).optString("F5");
							}
							if(!hanteiValue.isEmpty()){
								prmData.add(hanteiValue);
								shnValues += ", ?";
							}else{
								shnValues += ", null ";
							}
						}else if(i==3 ){
							// 希望総売価（バンドル２）
							hanteiValue = val;
							if(String.valueOf(TPNG2FLG).equals("1") ){
								hanteiValue = shnDataArray.optJSONObject(j).optString("F6");
							}
							if(!hanteiValue.isEmpty()){
								shnValues += ", ?";
								prmData.add(hanteiValue);
							}else{
								shnValues += ", null ";
							}
						}else if(i == 4){
							// 催し区分
							shnValues += ", ?";
							prmData.add(MOYSKBN);
						}else if(i==5){
							// 催し開始日
							shnValues += ", ?";
							prmData.add(MOYSSTDT);
						}else if(i==6){
							//催し連番
							shnValues += ", ?";
							prmData.add(MOYSRBAN);
						}else if(i==7){
							//店グループコード
							shnValues += ", ?";
							prmData.add(TENGPCD);
						}else if(i==8){
							// 部門コード
							shnValues += ", ?";
							prmData.add(val);
						}else if(i==9){
							// 管理番号
							shnValues += ", ?";
							prmData.add(val);
						}else if(i==10){
							// 枝番
							shnValues += ", ?";
							prmData.add(val);
						}
					}
				}
				updateRows +=  ",("+StringUtils.removeStart(shnValues, ",")+")";
			}
			updateRows = StringUtils.removeStart(updateRows, ",");

			if(shnValues.length()==0){
				msgObj.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
				return msgObj;
			}
			// 基本INSERT/UPDATE文
			StringBuffer sbSQL2 = new StringBuffer();;

			sbSQL2.append("MERGE INTO INATK.TOKTG_QASHN AS T USING (SELECT ");
			sbSQL2.append("  URICHGAM1");													// 催し区分：
			sbSQL2.append(", URICHGAM2");													// 催し開始日：
			sbSQL2.append(", URICHGAM3");													// 催し連番：
			sbSQL2.append(", MOYSKBN");													// 採用フラグ：
			sbSQL2.append(", MOYSSTDT");												// 回答フラグ：
			sbSQL2.append(", MOYSRBAN");													// エリア区分：
			sbSQL2.append(", TENGPCD");													// エリア区分：
			sbSQL2.append(", BMNCD");													// エリア区分：
			sbSQL2.append(", KANRINO");													// 店コード：
			sbSQL2.append(", KANRIENO");												// 強制フラグ：
			sbSQL2.append(", "+DefineReport.ValUpdkbn.NML.getVal()+" AS UPDKBN");		// 更新区分：
			sbSQL2.append(", "+DefineReport.Values.SENDFLG_UN.getVal()+" AS SENDFLG");		// 送信区分：
			sbSQL2.append(", '"+userId+"' AS OPERATOR ");								// オペレーター：
			sbSQL2.append(", current timestamp AS ADDDT ");								// 登録日：
			sbSQL2.append(", current timestamp AS UPDDT ");								// 更新日：
			sbSQL2.append(" FROM (values "+updateRows+") as T1(");
			sbSQL2.append("  URICHGAM1");													// 催し区分：
			sbSQL2.append(", URICHGAM2");													// 催し開始日：
			sbSQL2.append(", URICHGAM3");													// 催し連番：
			sbSQL2.append(", MOYSKBN");													// 採用フラグ：
			sbSQL2.append(", MOYSSTDT");												// 回答フラグ：
			sbSQL2.append(", MOYSRBAN");													// エリア区分：
			sbSQL2.append(", TENGPCD");													// エリア区分：
			sbSQL2.append(", BMNCD");													// エリア区分：
			sbSQL2.append(", KANRINO");													// 店コード：
			sbSQL2.append(", KANRIENO");
			sbSQL2.append(")) as RE on (");
			sbSQL2.append("T.MOYSKBN = RE.MOYSKBN and ");
			sbSQL2.append("T.MOYSSTDT = RE.MOYSSTDT and ");
			sbSQL2.append("T.MOYSRBAN = RE.MOYSRBAN and ");
			sbSQL2.append("T.TENGPCD = RE.TENGPCD and ");
			sbSQL2.append("T.BMNCD = RE.BMNCD and ");
			sbSQL2.append("T.KANRINO = RE.KANRINO and ");
			sbSQL2.append("T.KANRIENO = RE.KANRIENO ");
			sbSQL2.append(") WHEN MATCHED THEN UPDATE SET ");
			sbSQL2.append("  URICHGAM1 = RE.URICHGAM1");
			sbSQL2.append(", URICHGAM2 = RE.URICHGAM2");
			sbSQL2.append(", URICHGAM3 = RE.URICHGAM3");
			sbSQL2.append(", UPDKBN=RE.UPDKBN ");
			sbSQL2.append(", SENDFLG=RE.SENDFLG ");
			sbSQL2.append(", OPERATOR = RE.OPERATOR");
			sbSQL2.append(", ADDDT = case when NVL(UPDKBN, 0) = 1 then RE.ADDDT else ADDDT end");
			sbSQL2.append(", UPDDT=RE.UPDDT ");
			int count2 = super.executeSQL(sbSQL2.toString(), prmData);
			if(StringUtils.isEmpty(getMessage())) {
				if (DefineReport.ID_DEBUG_MODE) System.out.println("アンケート結果_商品を " + MessageUtility.getMessage(Msg.S00003.getVal(),
						new String[] { Integer.toString(dataArray.size()), Integer.toString(count) }));
				msgObj.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00001.getVal()));
			}else{
				msgObj.put(MsgKey.E.getKey(), getMessage());
			}


		}
		return msgObj;
	}
}
