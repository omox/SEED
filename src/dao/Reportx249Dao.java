package dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
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
import common.JsonArrayData;
import common.MessageUtility;
import common.MessageUtility.Msg;
import common.MessageUtility.MsgKey;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 */
public class Reportx249Dao extends ItemDao {

	/**
	 * インスタンスを生成します。
	 * @param source
	 */
	public Reportx249Dao(String JNDIname) {
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
	 * 検索実行
	 *
	 * @return
	 */
	private String createCommand() {
		// ログインユーザー情報取得
		User userInfo = getUserInfo();
		if(userInfo==null){
			return "";
		}

		String btnId = getMap().get("BTN");			// 実行ボタン

		// パラメータ確認
		// 必須チェック
		if ((btnId == null)) {
			System.out.println(super.getConditionLog());
			return "";
		}

		// タイトル情報(任意)設定
		List<String> titleList = new ArrayList<String>();

		// DB検索用パラメータ
		ArrayList<String> paramData = new ArrayList<String>();

		String szTommorow	= (new CmnDate()).getTomorrow();	// 明日日付
		String szTorihiki		= getMap().get("TORIHIKI");				// 取引先
		String szTeianNo		= getMap().get("TEIANNO");				// 件名No
		String szTeian			= getMap().get("TEIAN");					// 提案件名
		String szState			= getMap().get("STATE");					// 状態
		String szBumon		= getMap().get("BUMON");				// 部門
		if(szBumon.equals("-1")) {
			szBumon = "";
		}
		String szShohin		= getMap().get("SHOHIN");				// 商品名
		String szFromDate	= getMap().get("FROM_DATE");			// 商品登録日FROM
		String szToDate		= getMap().get("TO_DATE");				// 商品登録日TO
		String szLimit			= getMap().get("LIMIT");					// 検索上限数

		// 検索文（並び替え＆取得範囲制限）
		StringBuffer sbSQL = new StringBuffer();

		sbSQL.append(" select");
		sbSQL.append(" case when T1.TITSTCD = '1' then '作成中'");				// F1	：状態
		sbSQL.append(" when T1.TITSTCD = '2' then '確定'");
		sbSQL.append(" when T1.TITSTCD = '3' then '仕掛'");
		sbSQL.append(" when T1.TITSTCD = '4' then '完了'");
		sbSQL.append(" when T1.TITSTCD = '9' then '却下'");
		sbSQL.append(" else '　' end");
		sbSQL.append(" ,T1.TITKNNO");																	// F2	 : 件名No
		sbSQL.append(" ,TIK.TIKKNNM");																	// F3	 : 提案件名
		sbSQL.append(" ,trim(left(lpad(T1.SHNCD, 8, '0'), 4) || '-' || SUBSTR(lpad(T1.SHNCD, 8, '0'), 5)) as SHNCD");	// F4	：商品コード
		sbSQL.append(" ,T3.SRCCD as SRCCD");																	// F5	：ソースコード1
		sbSQL.append(" ,T1.SHNKN");																	// F6	：商品名
		sbSQL.append(" ,T1.RG_ATSUKFLG");																	// F7	：扱区分
		sbSQL.append(" ,T1.RG_GENKAAM");																	// F8	：原価
		sbSQL.append(" ,T1.RG_BAIKAAM");																	// F9	：本体売価
		sbSQL.append(" ,"+DefineReport.ID_SQL_MD03111301_COL_RG);					// F10	：総額売価
		sbSQL.append(" ,T1.RG_IRISU");																	// F11	：店入数
		sbSQL.append(" ,case when trim(T1.PARENTCD) = '00000000' then '' else trim(left(T1.PARENTCD, 4) || '-' || SUBSTR(T1.PARENTCD, 5)) end as PARENTCD");	// F12	: 親の商品コード
		sbSQL.append(" ,T1.RG_WAPNFLG");																	// F13	：ワッペン区分
		sbSQL.append(" ,T1.RG_IDENFLG");																	// F14	：一括区分
		sbSQL.append(" ,T1.SSIRCD");																	// F15	：標準仕入先
		sbSQL.append(" ,right('0'||nvl(T1.BMNCD,0),2)||'-'||right('0'||nvl(T1.DAICD,0),2)||'-'||right('0'||nvl(T1.CHUCD,0),2)||'-'||right('0'||nvl(T1.SHOCD,0),2) as BUNCD");		// F16	：分類コード
		sbSQL.append(" ,nvl(TO_CHAR(T1.UPDDT, 'YY/MM/DD'),'__/__/__')");																	// F17	：更新日
		sbSQL.append(" ,T5.NMKN");																	// F18	：衣料使い回し

		sbSQL.append(" from INAWS.PIMTIT T1");
		sbSQL.append(" left outer join INAMS.MSTMEISHO T5 on T5.MEISHOKBN = '142' and trim(T5.MEISHOCD) = IRYOREFLG");
		sbSQL.append(" left outer join INAMS.MSTSRCCD T3 on T1.SHNCD = T3.SHNCD and T3.SEQNO = 1");
		sbSQL.append(" "+DefineReport.ID_SQL_MD03111301_JOIN+"");
		sbSQL.append(" inner join INAWS.PIMTIK TIK on T1.TITKNNO = TIK.TIKKNNO");

		sbSQL.append(" left join INAMS.MSTCHUBRUI CHU on CHU.BMNCD = T1.BMNCD and CHU.DAICD = T1.DAICD and CHU.CHUCD = T1.CHUCD");
		sbSQL.append(" left join INAMS.MSTDAIBRUI DAI on DAI.BMNCD = CHU.BMNCD and DAI.DAICD = CHU.DAICD");
		sbSQL.append(" left join INAMS.MSTBMN BMN on BMN.BMNCD = CHU.BMNCD");

		sbSQL.append(" left join INAMS.MSTSIR SIR on SIR.SIRCD = TIK.TIKTTCD");
		sbSQL.append(" left join INAMS.MSTSIR SIR2 on SIR2.SIRCD = T1.SSIRCD");
		sbSQL.append(" where nvl(T1.UPDKBN, 0) <> 1");

		if(StringUtils.isNotEmpty(szTorihiki)){		// 条件：取引先
			sbSQL.append(" and TIK.TIKTTCD = '"+szTorihiki+"'");
		}
		if(StringUtils.isNotEmpty(szTeianNo)){		// 条件：件名No
			sbSQL.append(" and TIK.TIKKNNO = "+szTeianNo.replaceAll("'", "''"));
		}
		if(StringUtils.isNotEmpty(szTeian)){		// 条件：提案件名
			sbSQL.append(" and TIK.TIKKNNM like '%"+szTeian.replaceAll("'", "''")+"%'");
		}
		if((StringUtils.isNotEmpty(szState)) && !(szState.equals("-1"))){		// 条件：状態
			sbSQL.append(" and T1.TITSTCD = '"+szState.substring(1)+"'");
		}
		if(StringUtils.isNotEmpty(szBumon)){		// 条件：部門
			if( szBumon.substring(0,1).equals("0")) {
				szBumon = szBumon.substring(1);
			}
			sbSQL.append(" and T1.BMNCD like '");
			sbSQL.append(szBumon);

			sbSQL.append("%'");
		}
		if(StringUtils.isNotEmpty(szShohin)){		// 条件：商品名
			sbSQL.append(" and T1.SHNKN like '%");
			sbSQL.append(szShohin);
			sbSQL.append("%'");
		}
		// 商品登録日
		if(szFromDate.length() == 8) {
			szFromDate = szFromDate.substring(0, 4)+"-"+szFromDate.substring(4,6)+"-"+szFromDate.substring(6);
		}
		if(szToDate.length() == 8) {
			szToDate = szToDate.substring(0, 4)+"-"+szToDate.substring(4,6)+"-"+szToDate.substring(6);
		}
		if(StringUtils.isNotEmpty(szFromDate) && StringUtils.isNotEmpty(szToDate)){
			// FROM,TO 指定
			sbSQL.append(" and T1.ADDDT between '"+szFromDate+"' and '"+szToDate+"'");
		} else if (StringUtils.isNotEmpty(szFromDate)) {
			// FROM 指定
			sbSQL.append(" and T1.ADDDT >= '"+szFromDate+"'");
		} else if (StringUtils.isNotEmpty(szToDate)) {
			// TO 指定
			sbSQL.append(" and T1.ADDDT <= '"+szToDate+"'");
		}
		sbSQL.append(" order by T1.SHNCD, T1.TITKNNO fetch first "+szLimit+" rows only");

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

		// 取引先
		cells = new ArrayList<String>();
		cells.add( DefineReport.Text.TORIHIKI.getTxt() );
		cells.add( jad.getJSONText(DefineReport.Text.TORIHIKI.getObj()) );
		// 件名No
		cells.add( DefineReport.InpText.TEIANNO.getTxt() );
		cells.add( jad.getJSONValue(DefineReport.InpText.TEIANNO.getObj()) );
		// 提案件名
		cells.add( DefineReport.InpText.TEIAN.getTxt() );
		cells.add( jad.getJSONValue(DefineReport.InpText.TEIAN.getObj()) );
		// 状態
		cells.add( DefineReport.Select.STCD_TEIAN.getTxt() );
		cells.add( jad.getJSONText(DefineReport.Select.STCD_TEIAN.getObj()) );
		getWhere().add(cells);

		// 部門
		cells = new ArrayList<String>();
		cells.add( DefineReport.Select.BUMON.getTxt() );
		cells.add( jad.getJSONText(DefineReport.Select.BUMON.getObj()) );
		// 商品名
		cells.add( DefineReport.Text.SHOHIN.getTxt() );
		cells.add( jad.getJSONValue(DefineReport.Text.SHOHIN.getObj()) );
		getWhere().add(cells);

		// 商品登録日
		cells = new ArrayList<String>();
		cells.add( "商品登録日" );
		cells.add( jad.getJSONText(DefineReport.InpText.YMD_F.getObj()) );
		cells.add( "～" );
		cells.add( jad.getJSONText(DefineReport.InpText.YMD_T.getObj()) );
		getWhere().add(cells);

		// 共通箇所設定
		createCmnOutput(jad);
	}

	/**
	 * 更新処理
	 * @param request
	 * @param session
	 * @param map
	 * @param userInfo
	 * @return
	 */
	public JSONObject update(HttpServletRequest request, HttpSession session, HashMap<String, String> map, User userInfo) {
		String sysdate = (String)request.getSession().getAttribute(Consts.STR_SES_LOGINDT);
		// メッセージ格納用
		JSONObject rtn = new JSONObject();
		JSONArray msg = new JSONArray();

		String mode = map.get("MODE");	// 更新処理モード

		if(StringUtils.equals("1", mode)){
			// 仕掛商品に登録
			msg = this.check(map, userInfo, sysdate);
		}

		if(msg.size() > 0){
			rtn.put(MsgKey.E.getKey(), msg);
			return rtn;
		}

		// 更新処理
		try {
			if(StringUtils.equals("1", mode)){
				// 仕掛商品に登録
				rtn = this.updateData(map, userInfo, sysdate);
			}

		} catch (Exception e) {
			e.printStackTrace();
			msg.add(MessageUtility.getMessage(Msg.E00001.getVal()));
			rtn.put(MsgKey.E.getKey(), msg);
		}

		return rtn;
	}

	/**
	 * チェック処理
	 * @param map
	 * @param userInfo
	 * @param sysdate
	 * @return
	 */
	@SuppressWarnings("static-access")
	public JSONArray check(HashMap<String, String> map, User userInfo, String sysdate) {
		JSONArray msg = new JSONArray();

		// パラメータ確認
		JSONArray dataArray	= JSONArray.fromObject(map.get("DATA"));	// 更新情報

		return msg;
	}

	/**
	 * 更新処理実行
	 * @param map
	 * @param userInfo
	 * @param sysdate
	 * @return
	 * @throws Exception
	 */
	private JSONObject updateData(HashMap<String, String> map, User userInfo, String sysdate) throws Exception {
		JSONObject rtn = new JSONObject();

		// パラメータ確認
		JSONArray dataArray	= JSONArray.fromObject(map.get("DATA"));	// 更新情報
		String szTeian		= "";										// 件名No
		String code			= "";										// 商品コード

		String userID = userInfo.getId();
		CmnDate dateInfo = new CmnDate();
		String sysDate = dateInfo.getToday();				// 本日日付
		String sysTime = dateInfo.getNowTime();				// 現在時刻
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss.SSS");
		String strDateInfo = dateFormat.format(dateInfo);

		ArrayList<String> prmData = new ArrayList<String>();
		int upCount = 0;

		// コネクション
		Connection con = null;

		try {
			// コネクションの取得
			con = DBConnection.getConnection(this.JNDIname);
			con.setAutoCommit(false);

			// 仕掛商品（INAWS.PIMSIT）の商品ステータス（SITSTCD）を作成中（1）にて追加
			String sqlInsertCommand_1 =
					"insert into INAWS.PIMSIT(" +
					"  SITKNNO, SITSTCD, SHNCD, YOYAKUDT, TENBAIKADT, YOT_BMNCD, YOT_DAICD, YOT_CHUCD" +
					", YOT_SHOCD, URI_BMNCD, URI_DAICD, URI_CHUCD, URI_SHOCD, BMNCD, DAICD, CHUCD, SHOCD" +
					", SSHOCD, ATSUK_STDT, ATSUK_EDDT, TEISHIKBN, SHNAN, SHNKN, PCARDKN, POPKN, RECEIPTAN" +
					", RECEIPTKN, PCKBN, KAKOKBN, ICHIBAKBN, SHNKBN, SANCHIKN, SSIRCD, HSPTN, RG_ATSUKFLG" +
					", RG_GENKAAM, RG_BAIKAAM, RG_IRISU, RG_IDENFLG, RG_WAPNFLG, HS_ATSUKFLG, HS_GENKAAM" +
					", HS_BAIKAAM, HS_IRISU, HS_WAPNFLG, HS_SPOTMINSU, HP_SWAPNFLG, KIKKN, UP_YORYOSU" +
					", UP_TYORYOSU, UP_TANIKBN, SHNYOKOSZ, SHNTATESZ, SHNOKUSZ, SHNJRYOSZ, PBKBN, KOMONOKBM" +
					", TANAOROKBN, TEIKEIKBN, ODS_HARUSU, ODS_NATSUSU, ODS_AKISU, ODS_FUYUSU, ODS_NYUKASU" +
					", ODS_NEBIKISU, PCARD_SHUKBN, PCARD_IROKBN, ZEIKBN, ZEIRTKBN, ZEIRTKBN_OLD, ZEIRTHENKODT" +
					", SEIZOGENNISU, TEIKANKBN, MAKERCD, IMPORTKBN, SIWAKEKBN, HENPIN_KBN, TAISHONENSU, CALORIESU" +
					", ELPFLG, BELLMARKFLG, RECYCLEFLG, ECOFLG, HZI_YOTO, HZI_ZAISHITU, HZI_RECYCLE, KIKANKBN" +
					", SHUKYUKBN, DOSU, CHINRETUCD, DANTUMICD, KASANARICD, KASANARISZ, ASSHUKURT, SHUBETUCD" +
					", URICD, SALESCOMKN, URABARIKBN, PCARD_OPFLG, PARENTCD, BINKBN, HAT_MONKBN, HAT_TUEKBN" +
					", HAT_WEDKBN, HAT_THUKBN, HAT_FRIKBN, HAT_SATKBN, HAT_SUNKBN, READTMPTN, SIMEKAISU, IRYOREFLG" +
					", TOROKUMOTO, UPDKBN, SENDFLG, OPERATOR, ADDDT, UPDDT, K_HONKB, K_WAPNFLG_R, K_WAPNFLG_H" +
					", K_TORIKB, ITFCD, CENTER_IRISU" +
					") select" +
					"  TITKNNO, 1, lpad(SHNCD, 8, '0') as SHNCD, YOYAKUDT, TENBAIKADT, YOT_BMNCD, YOT_DAICD, YOT_CHUCD" +
					", YOT_SHOCD, URI_BMNCD, URI_DAICD, URI_CHUCD, URI_SHOCD, BMNCD, DAICD, CHUCD, SHOCD" +
					", SSHOCD, ATSUK_STDT, ATSUK_EDDT, TEISHIKBN, SHNAN, SHNKN, PCARDKN, POPKN, RECEIPTAN" +
					", RECEIPTKN, PCKBN, KAKOKBN, ICHIBAKBN, SHNKBN, SANCHIKN, SSIRCD, HSPTN, RG_ATSUKFLG" +
					", RG_GENKAAM, RG_BAIKAAM, RG_IRISU, RG_IDENFLG, RG_WAPNFLG, HS_ATSUKFLG, HS_GENKAAM" +
					", HS_BAIKAAM, HS_IRISU, HS_WAPNFLG, HS_SPOTMINSU, HP_SWAPNFLG, KIKKN, UP_YORYOSU" +
					", UP_TYORYOSU, UP_TANIKBN, SHNYOKOSZ, SHNTATESZ, SHNOKUSZ, SHNJRYOSZ, PBKBN, KOMONOKBM" +
					", TANAOROKBN, TEIKEIKBN, ODS_HARUSU, ODS_NATSUSU, ODS_AKISU, ODS_FUYUSU, ODS_NYUKASU" +
					", ODS_NEBIKISU, PCARD_SHUKBN, PCARD_IROKBN, ZEIKBN, ZEIRTKBN, ZEIRTKBN_OLD, ZEIRTHENKODT" +
					", SEIZOGENNISU, TEIKANKBN, MAKERCD, IMPORTKBN, SIWAKEKBN, HENPIN_KBN, TAISHONENSU, CALORIESU" +
					", ELPFLG, BELLMARKFLG, RECYCLEFLG, ECOFLG, HZI_YOTO, HZI_ZAISHITU, HZI_RECYCLE, KIKANKBN" +
					", SHUKYUKBN, DOSU, CHINRETUCD, DANTUMICD, KASANARICD, KASANARISZ, ASSHUKURT, SHUBETUCD" +
					", URICD, SALESCOMKN, URABARIKBN, PCARD_OPFLG, PARENTCD, BINKBN, HAT_MONKBN, HAT_TUEKBN" +
					", HAT_WEDKBN, HAT_THUKBN, HAT_FRIKBN, HAT_SATKBN, HAT_SUNKBN, READTMPTN, SIMEKAISU, IRYOREFLG" +
					", TOROKUMOTO, UPDKBN, SENDFLG" +
					", ?, ?, ?" +
					", K_HONKB, K_WAPNFLG_R, K_WAPNFLG_H, K_TORIKB, ITFCD, CENTER_IRISU" +
					" from INAWS.PIMTIT" +
					" where SHNCD = ? and TITKNNO = ? and TITSTCD = 2";

			// 仕掛仕入グループ商品マスタ（INAWS.PIMSISIRGPSHN）に追加
			String sqlInsertCommand_2 =
					"insert into INAWS.PIMSISIRGPSHN(" +
					"  SHNCD, TENGPCD, YOYAKUDT, AREAKBN" +
					", SIRCD, HSPTN, SENDFLG, OPERATOR, ADDDT, UPDDT" +
					") select" +
					"  lpad(SHNCD, 8, '0') as SHNCD, TENGPCD, YOYAKUDT, AREAKBN" +
					", SIRCD, HSPTN, SENDFLG, ?, ?, ?" +
					" from INAWS.PIMTISIRGPSHN" +
					" where SHNCD = (" +
					" select SHNCD from INAWS.PIMTIT" +
					" where TITKNNO = ? and SHNCD = ? and TITSTCD = 2" +
					" )";

			// 仕掛売価コントロールマスタ（INAWS.PIMSIBAIKACTL）に追加
			String sqlInsertCommand_3 =
					"insert into INAWS.PIMSIBAIKACTL(" +
					"  SHNCD, TENGPCD, YOYAKUDT, AREAKBN, GENKAAM" +
					", BAIKAAM, IRISU, SENDFLG, OPERATOR, ADDDT, UPDDT" +
					") select" +
					"  lpad(SHNCD, 8, '0') as SHNCD, TENGPCD, YOYAKUDT, AREAKBN, GENKAAM" +
					", BAIKAAM, IRISU, SENDFLG, ?, ?, ?" +
					" from INAWS.PIMTIBAIKACTL" +
					" where SHNCD = (" +
					" select SHNCD from INAWS.PIMTIT" +
					" where TITKNNO = ? and SHNCD = ? and TITSTCD = 2" +
					" )";

			// 仕掛ソースコード管理マスタ（INAWS.PIMSISRCCD）に追加
			String sqlInsertCommand_4 =
					"insert into INAWS.PIMSISRCCD(" +
					"  SHNCD, SRCCD, YOYAKUDT, SEQNO, SOURCEKBN" +
					", SENDFLG, OPERATOR, ADDDT, UPDDT, YUKO_STDT, YUKO_EDDT" +
					") select" +
					"  lpad(SHNCD, 8, '0') as SHNCD, SRCCD, YOYAKUDT, SEQNO, SOURCEKBN" +
					", SENDFLG, ?, ?, ?, YUKO_STDT, YUKO_EDDT" +
					" from INAWS.PIMTISRCCD" +
					" where SHNCD = (" +
					" select SHNCD from INAWS.PIMTIT" +
					" where TITKNNO = ? and SHNCD = ? and TITSTCD = 2" +
					" )";

			// 仕掛添加物マスタ（INAWS.PIMSITENKABUTSU）に追加
			String sqlInsertCommand_5 =
					"insert into INAWS.PIMSITENKABUTSU(" +
					"  SHNCD, TENKABKBN, TENKABCD, YOYAKUDT" +
					", SENDFLG, OPERATOR, ADDDT, UPDDT" +
					") select" +
					"  lpad(SHNCD, 8, '0') as SHNCD, TENKABKBN, TENKABCD, YOYAKUDT" +
					", SENDFLG, ?, ?, ?" +
					" from INAWS.PIMTITENKABUTSU" +
					" where SHNCD = (" +
					" select SHNCD from INAWS.PIMTIT" +
					" where TITKNNO = ? and SHNCD = ? and TITSTCD = 2" +
					" )";

			// 仕掛品揃グループマスタ（INAWS.PIMSISHINAGP）に追加
			String sqlInsertCommand_6 =
					"insert into INAWS.PIMSISHINAGP(" +
					"  SHNCD, TENGPCD, YOYAKUDT, AREAKBN" +
					", ATSUKKBN, SENDFLG, OPERATOR, ADDDT, UPDDT" +
					") select" +
					"  lpad(SHNCD, 8, '0') as SHNCD, TENGPCD, YOYAKUDT, AREAKBN" +
					", ATSUKKBN, SENDFLG, ?, ?, ?" +
					" from INAWS.PIMTISHINAGP" +
					" where SHNCD = (" +
					" select SHNCD from INAWS.PIMTIT" +
					" where TITKNNO = ? and SHNCD = ? and TITSTCD = 2" +
					" )";

			// 仕掛メーカーマスタ（INAWS.PIMSIMAKER）に追加
			String sqlInsertCommand_7 =
					"insert into INAWS.PIMSIMAKER(" +
					"  MAKERCD, MAKERAN, MAKERKN, JANCD, DMAKERCD" +
					", UPDKBN, SENDFLG, OPERATOR, ADDDT, UPDDT" +
					") select" +
					"  MAKERCD, MAKERAN, MAKERKN, JANCD, DMAKERCD" +
					", UPDKBN, SENDFLG, ?, ?, ?" +
					" from INAWS.PIMTIMAKER" +
					" where MAKERCD = (" +
					" select MAKERCD from INAWS.PIMTIT" +
					" where TITKNNO = ? and SHNCD = ? and TITSTCD = 2" +
					" )";

			// 提案商品（INAWS.PIMTIT）の商品ステータス（TITSTCD）を仕掛（3）に更新
			String sqlUpdateCommand_1 =
					"update INAWS.PIMTIT set TITSTCD = 3, OPERATOR = ?, UPDDT = ? where SHNCD = ? and TITKNNO = ? and TITSTCD = 2";

			// 提案件名（INAWS.PIMTIK）の件名ステータス（TIKSTCD）を仕掛（3）に更新
			String sqlUpdateCommand_2 =
					"update INAWS.PIMTIK set TIKSTCD = 3, T1UPID = ?, T1UPDT = ?, T1UPTM = ? where TIKKNNO = ? and TIKSTCD = 2 and (select COUNT(TITKNNO) from INAWS.PIMTIT where TITKNNO = ? and TITSTCD > 2) > 0";

			for (int i = 0; i < dataArray.size(); i++) {
				if(!dataArray.getJSONObject(i).isEmpty()){
					code = dataArray.getJSONObject(i).optString("F4");		// 商品コード
					code = code.replace("-", "");
					szTeian = dataArray.getJSONObject(i).optString("F2");	// 件名No
				}
				if(code.isEmpty()){
					continue;
				}
				if(szTeian.isEmpty()) {
					continue;
				}

				// 仕掛商品に登録
				prmData = new ArrayList<String>();
				prmData.add(userID);
				prmData.add(strDateInfo);
				prmData.add(strDateInfo);
				prmData.add(code);
				prmData.add(szTeian);

				// SQL実行
				if (updateBySQL(sqlInsertCommand_1, prmData, con) == 0) {
					continue;
				}

				// 仕掛仕入グループ商品マスタに登録
				prmData = new ArrayList<String>();
				prmData.add(userID);
				prmData.add(strDateInfo);
				prmData.add(strDateInfo);
				prmData.add(szTeian);
				prmData.add(code);
				// SQL実行（カウントしない）
				updateBySQL(sqlInsertCommand_2, prmData, con);

				// 仕掛売価コントロールマスタに登録
				prmData = new ArrayList<String>();
				prmData.add(userID);
				prmData.add(strDateInfo);
				prmData.add(strDateInfo);
				prmData.add(szTeian);
				prmData.add(code);
				// SQL実行（カウントしない）
				updateBySQL(sqlInsertCommand_3, prmData, con);

				// 仕掛ソースコード管理マスタに登録
				prmData = new ArrayList<String>();
				prmData.add(userID);
				prmData.add(strDateInfo);
				prmData.add(strDateInfo);
				prmData.add(szTeian);
				prmData.add(code);
				// SQL実行（カウントしない）
				updateBySQL(sqlInsertCommand_4, prmData, con);

				// 仕掛添加物マスタに登録
				prmData = new ArrayList<String>();
				prmData.add(userID);
				prmData.add(strDateInfo);
				prmData.add(strDateInfo);
				prmData.add(szTeian);
				prmData.add(code);
				// SQL実行（カウントしない）
				updateBySQL(sqlInsertCommand_5, prmData, con);

				// 仕掛品揃グループマスタに登録
				prmData = new ArrayList<String>();
				prmData.add(userID);
				prmData.add(strDateInfo);
				prmData.add(strDateInfo);
				prmData.add(szTeian);
				prmData.add(code);
				// SQL実行（カウントしない）
				updateBySQL(sqlInsertCommand_6, prmData, con);

				// 仕掛メーカーマスタに登録
				prmData = new ArrayList<String>();
				prmData.add(userID);
				prmData.add(strDateInfo);
				prmData.add(strDateInfo);
				prmData.add(szTeian);
				prmData.add(code);
				// SQL実行（カウントしない）
				updateBySQL(sqlInsertCommand_7, prmData, con);

				// 提案商品の状態更新
				prmData = new ArrayList<String>();
				prmData.add(userID);
				prmData.add(strDateInfo);
				prmData.add(code);
				prmData.add(szTeian);
				// SQL実行
				upCount += updateBySQL(sqlUpdateCommand_1, prmData, con);

				// 提案件名の状態更新
				prmData = new ArrayList<String>();
				prmData.add(userID);
				prmData.add(sysDate);
				prmData.add(sysTime);
				prmData.add(szTeian);
				prmData.add(szTeian);
				// SQL実行（カウントしない）
				updateBySQL(sqlUpdateCommand_2, prmData, con);
			}

			con.commit();
			con.close();

			if (dataArray.size() == upCount) {
				rtn.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00007.getVal(), "対象データ"));
			} else {
				rtn.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00006.getVal(), String.valueOf(upCount), String.valueOf(dataArray.size() - upCount)));
			}

		} catch (Exception e) {
			/* 接続解除 */
			if (con != null){
				try {
					con.rollback();
					con.close();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}
			e.printStackTrace();
			throw e;
		}

		return rtn;
	}
}
