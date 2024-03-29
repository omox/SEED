package dao;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import authentication.bean.User;
import common.CmnDate;
import common.CmnDate.DATE_FORMAT;
import common.DefineReport;
import common.DefineReport.DataType;
import common.DefineReport.InfTrankbn;
import common.DefineReport.ValSrccdSeqno;
import common.Defines;
import common.InputChecker;
import common.ItemList;
import common.JsonArrayData;
import common.MessageUtility;
import common.MessageUtility.FieldType;
import common.MessageUtility.Msg;
import common.MessageUtility.MsgKey;
import common.NumberingUtility;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 */
public class Reportx247Dao extends ItemDao {

	/**
	 * インスタンスを生成します。
	 * @param source
	 */
	public Reportx247Dao(String JNDIname) {
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
		// String sysdate = (String)request.getSession().getAttribute(Consts.STR_SES_LOGINDT);
		String sysdate = this.getSHORIDT();

		// 更新情報チェック(基本JS側で制御)
		JSONObject option = new JSONObject();

		JSONObject objset = new Reportx002Dao(JNDIname).check(map, userInfo, sysdate);
		JSONArray msgList = objset.optJSONArray("MSG");
		if(msgList.size() == 0){
			// 更新処理
			try {
				option = this.updateData(map, userInfo, sysdate, objset);
			} catch (Exception e) {
				e.printStackTrace();
				msgList.add(MessageUtility.getDbMessageIdObj("E30007", new String[]{}));
			}
		}

		if(msgList.size() > 0){
			option.put(MsgKey.E.getKey(), msgList.optJSONObject(0));
		} else {
			// 正 .新規
			String	sendBtnid	= map.get("SENDBTNID");		// 呼出しボタン
			boolean	isNew		= DefineReport.Button.NEW.getObj().equals(sendBtnid)||DefineReport.Button.COPY.getObj().equals(sendBtnid)||DefineReport.Button.SEL_COPY.getObj().equals(sendBtnid);
			if (isNew) {
				msgList.add(MessageUtility.getDbMessageIdObj("I00002", new String[] {""}));
				option.put(MsgKey.S.getKey(), msgList.optJSONObject(0));
			}
		}

		return option;
	}


	/**
	 * 削除処理
	 * @param request
	 * @param session
	 * @param map
	 * @param userInfo
	 * @return
	 */
	public JSONObject delete(HttpServletRequest request, HttpSession session, HashMap<String, String> map, User userInfo) {
		// メッセージ格納用
		JSONObject rtn = new JSONObject();
		JSONArray msg = new JSONArray();
		String szSeq = map.get("SEQ");			// CSVエラー.SEQ
		String sendBtnid	= map.get("SENDBTNID");	// 呼出しボタン

		boolean isCsverr = DefineReport.Button.ERR_CHANGE.getObj().equals(sendBtnid);

		// パラメータ確認
		JSONArray dataArray	= JSONArray.fromObject(map.get("DATA"));	// 更新情報

		ArrayList<String> prmData = new ArrayList<String>();
		int upCount = 0;

		// コネクション
		Connection con = null;

		try {
			// コネクションの取得
			con = DBConnection.getConnection(this.JNDIname);
			con.setAutoCommit(false);
			String sqlCommand = "delete from INAWS.PIMTIT where SHNCD = ?";
			if(isCsverr){
				sqlCommand = "delete from INAWS.CSVSHN where SEQ = ?";
				prmData.add(szSeq);
			}else{
				// 基本登録情報
				JSONObject data = dataArray.getJSONObject(0);
				String shncd = data.optString("F1");
				prmData.add(shncd);
			}
			// SQL実行
			upCount += updateBySQL(sqlCommand, prmData, con);
			con.commit();
			con.close();

			rtn.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00004.getVal(), String.valueOf(upCount)));

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
			msg.add(MessageUtility.getMessage(Msg.E00002.getVal()));
			rtn.put(MsgKey.E.getKey(), msg);
		}

		return rtn;
	}



	private String createCommand() {
		// ログインユーザー情報取得
		User userInfo = getUserInfo();
		if(userInfo==null){
			return "";
		}

		String szSelShncd	= getMap().get("SEL_SHNCD");	// 検索商品コード
		String szShncd		= getMap().get("SHNCD");		// 入力商品コード
		String szSeq		= getMap().get("SEQ");			// CSVエラー.SEQ
		String szInputno	= getMap().get("INPUTNO");		// CSVエラー.入力番号
		String szCsvUpdkbn	= getMap().get("CSV_UPDKBN");	// CSVエラー.CSV登録区分
		String szYoyakudt	= getMap().get("YOYAKUDT");		// CSVエラー用.マスタ変更予定日
		String szTenbaikadt	= getMap().get("TENBAIKADT");	// CSVエラー用.店売価実施日
		String sendBtnid	= getMap().get("SENDBTNID");	// 呼出しボタン

		// パラメータ確認
		// 必須チェック
		if (StringUtils.isEmpty(sendBtnid)) {
			System.out.println(super.getConditionLog());
			return "";
		}

		// タイトル情報(任意)設定
		List<String> titleList = new ArrayList<String>();

		StringBuffer sbSQL = new StringBuffer();

		// ①正 .新規
		boolean isNew = DefineReport.Button.NEW.getObj().equals(sendBtnid);
		boolean isCopyNew = DefineReport.Button.COPY.getObj().equals(sendBtnid)||DefineReport.Button.SEL_COPY.getObj().equals(sendBtnid);
		// ②正 .変更
		boolean isChange = DefineReport.Button.SEL_CHANGE.getObj().equals(sendBtnid)|| DefineReport.Button.SEARCH.getObj().equals(sendBtnid)|| DefineReport.Button.SEI.getObj().equals(sendBtnid);
		// ⑨CSVエラー修正
		boolean isCsverr = DefineReport.Button.ERR_CHANGE.getObj().equals(sendBtnid);
		boolean isCsvErrNew = isCsverr && DefineReport.ValCsvUpdkbn.NEW.getVal().equals(szCsvUpdkbn) && StringUtils.equals(szYoyakudt, "0") ;
		// 必須チェック
		if ( (isCopyNew && StringUtils.isEmpty(szSelShncd)) || (isChange && StringUtils.isEmpty(szShncd)) || (isCsverr && StringUtils.isEmpty(szInputno)) ) {
			System.out.println(super.getConditionLog());
			return "";
		}


		// 予約情報取得
		JSONArray yArray = new JSONArray();
		String szYoyaku		= "0";		// 予約件数
		if(!isNew&&!isCopyNew&&!isCsvErrNew){
			// 関連情報取得
			JSONArray array = getYoyakuJSONArray(getMap());
			szYoyaku = Integer.toString(array.size());
			yArray.addAll(array);
		}

		String szTableShn = "INAWS.PIMTIT";
		String szTableShina = "INAWS.PIMTISHINAGP";
		String szTableBaika = "INAWS.PIMTIBAIKACTL";
		String szTableSir = "INAWS.PIMTISIRGPSHN";
		String szTableTbmn = "INAMS.MSTSHNTENBMN";
		String szWhereSTable = " and SHNCD like '" + szSelShncd.replace("-", "") + "%'";
		String szWhereYTable = "";
		String szWhereCTable = "";
		if(isCsverr){
			szTableShn = "INAWS.CSVSHN";
			szTableShina = "INAWS.CSVSHINAGP";
			szTableBaika = "INAWS.CSVBAIKACTL";
			szTableSir = "INAWS.CSVSIRSHN";
			szTableTbmn = "INAWS.CSVMSTSHNTENBMN";
			szWhereSTable = "";
			szWhereCTable = " and T1.SEQ = " + szSeq + " and T1.INPUTNO = " + szInputno;
		}
		String szWhereTable = StringUtils.replaceOnce(szWhereSTable + szWhereYTable + szWhereCTable, " and", " where");

		// 完全新規
		if(isNew){
			sbSQL.append(" select ");
			sbSQL.append("   null as SHNCD");		// F1
			sbSQL.append(" , 0 as YOYAKUDT");
			sbSQL.append(" , 0 as TENBAIKADT");
			sbSQL.append(" , null as YOT_BMNCD");
			sbSQL.append(" , null as YOT_DAICD");
			sbSQL.append(" , null as YOT_CHUCD");
			sbSQL.append(" , null as YOT_SHOCD");
			sbSQL.append(" , null as URI_BMNCD");
			sbSQL.append(" , null as URI_DAICD");
			sbSQL.append(" , null as URI_CHUCD");
			sbSQL.append(" , null as URI_SHOCD");	//F10
			sbSQL.append(" , null as BMNCD");
			sbSQL.append(" , null as DAICD");
			sbSQL.append(" , null as CHUCD");
			sbSQL.append(" , null as SHOCD");
			sbSQL.append(" , null as SSHOCD");
			sbSQL.append(" , null as ATSUK_STDT");
			sbSQL.append(" , null as ATSUK_ETDT");
			sbSQL.append(" , null as TEISHIKBN");
			sbSQL.append(" , null as SHNAN");
			sbSQL.append(" , null as SHNKN");
			sbSQL.append(" , null as PCARDKN");
			sbSQL.append(" , null as POPKN");
			sbSQL.append(" , null as RECEIPTAN");
			sbSQL.append(" , null as RECEIPTKN");
			sbSQL.append(" , null as PCKBN");
			sbSQL.append(" , null as KAKOKBN");
			sbSQL.append(" , 0 as ICHIBAKBN");
			sbSQL.append(" , null as SHNKBN");
			sbSQL.append(" , null as SANCHIKN");
			sbSQL.append(" , null as SSIRCD");
			sbSQL.append(" , null as HSPTN");
			sbSQL.append(" , null as RG_ATSUKFLG");
			sbSQL.append(" , null as RG_GENKAAM");
			sbSQL.append(" , null as RG_BAIKAAM");
			sbSQL.append(" , null as RG_IRISU");
			sbSQL.append(" , null as RG_IDENFLG");
			sbSQL.append(" , null as RG_WAPNFLG");
			sbSQL.append(" , null as HS_ATSUKFLG");
			sbSQL.append(" , null as HS_GENKAAM");
			sbSQL.append(" , null as HS_BAIKAAM");
			sbSQL.append(" , null as HS_IRISU");
			sbSQL.append(" , null as HS_WAPNFLG");
			sbSQL.append(" , null as HS_SPOTMINSU");
			sbSQL.append(" , null as HP_SWAPNFLG");
			sbSQL.append(" , null as KIKKN");
			sbSQL.append(" , null as UP_YORYOSU");
			sbSQL.append(" , null as UP_TYORYOSU");
			sbSQL.append(" , null as UP_TANIKBN");
			sbSQL.append(" , null as SHNYOKOSZ");
			sbSQL.append(" , null as SHNTATESZ");
			sbSQL.append(" , null as SHNOKUSZ");
			sbSQL.append(" , null as SHNJRYOSZ");
			sbSQL.append(" , 0 as PBKBN");
			sbSQL.append(" , '00' as KOMONOKBM");
			sbSQL.append(" , null as TANAOROKBN");
			sbSQL.append(" , null as TEIKEIKBN");
			sbSQL.append(" , null as ODS_HARUSU");
			sbSQL.append(" , null as ODS_NATSUSU");
			sbSQL.append(" , null as ODS_AKISU");
			sbSQL.append(" , null as ODS_FUYUSU");
			sbSQL.append(" , null as ODS_NYUKASU");
			sbSQL.append(" , null as ODS_NEBIKISU");
			sbSQL.append(" , null as PCARD_SHUKBN");
			sbSQL.append(" , null as PCARD_IROKBN");
			sbSQL.append(" , null as ZEIKBN");
			sbSQL.append(" , null as ZEIRTKBN");
			sbSQL.append(" , null as ZEIRTKBN_OLD");
			sbSQL.append(" , null as ZEIRTHENKODT");
			sbSQL.append(" , null as SEIZOGENNISU");
			sbSQL.append(" , null as TEIKANKBN");
			sbSQL.append(" , null as MAKERCD");
			sbSQL.append(" , '00' as IMPORTKBN");
			sbSQL.append(" , null as SIWAKEKBN");
			sbSQL.append(" , 0 as HENPIN_KBN");
			sbSQL.append(" , null as TAISHONENSU");
			sbSQL.append(" , null as CALORIESU");
			sbSQL.append(" , null as ELPFLG");
			sbSQL.append(" , null as BELLMARKFLG");
			sbSQL.append(" , null as RECYCLEFLG");
			sbSQL.append(" , null as ECOFLG");
			sbSQL.append(" , null as HZI_YOTO");
			sbSQL.append(" , null as HZI_ZAISHITU");
			sbSQL.append(" , null as HZI_RECYCLE");
			sbSQL.append(" , null as KIKANKBN");
			sbSQL.append(" , '00' as SHUKYUKBN");
			sbSQL.append(" , null as DOSU");
			sbSQL.append(" , null as CHINRETUCD");
			sbSQL.append(" , null as DANTUMICD");
			sbSQL.append(" , null as KASANARICD");
			sbSQL.append(" , null as KASANARISZ");
			sbSQL.append(" , null as ASSHUKURT");
			sbSQL.append(" , null as SHUBETUCD");
			sbSQL.append(" , null as URICD");
			sbSQL.append(" , null as SALESCOMKN");
			sbSQL.append(" , 0 as URABARIKBN");
			sbSQL.append(" , null as PCARD_OPFLG");
			sbSQL.append(" , '00000000' as PARENTCD");
			sbSQL.append(" , 1 as BINKBN");					// F99:便区分	初期値:1
			sbSQL.append(" , null as HAT_MONKBN");
			sbSQL.append(" , null as HAT_TUEKBN");
			sbSQL.append(" , null as HAT_WEDKBN");
			sbSQL.append(" , null as HAT_THUKBN");
			sbSQL.append(" , null as HAT_FRIKBN");
			sbSQL.append(" , null as HAT_SATKBN");
			sbSQL.append(" , null as HAT_SUNKBN");
			sbSQL.append(" , null as READTMPTN");
			sbSQL.append(" , 1 as SIMEKAISU");				// F108:締め回数	初期値:1
			sbSQL.append(" , null as IRYOREFLG");
			sbSQL.append(" , 0 as TOROKUMOTO");
			sbSQL.append(" , 0 as UPDKBN");
			sbSQL.append(" , 0 as SENDFLG");
			sbSQL.append(" , null as OPERATOR");
			sbSQL.append(" , null as ADDDT");
			sbSQL.append(" , null as UPDDT");
			sbSQL.append(" , null as K_HONKB");				// F116: 保温区分
			sbSQL.append(" , null as K_WAPNFLG_R");			// F117: デリカワッペン区分_レギュラー
			sbSQL.append(" , null as K_WAPNFLG_H");			// F118: デリカワッペン区分_販促
			sbSQL.append(" , null as K_TORIKB");			// F119: 取扱区分
			sbSQL.append(" , null as ITFCD");				// F120: ITFコード
			sbSQL.append(" , null as CENTER_IRISU");		// F121: センター入数

			sbSQL.append(" , null as YOBIDASHICD");			// F122:呼出コード
			sbSQL.append(" , null as RG_AVGPTANKAAM");		// F123:TODO
			sbSQL.append(" , null as HS_AVGPTANKAAM");		// F124:販促平均パック単価(=空白)
			sbSQL.append(" , 0 as KETA");					// F125:桁			初期値:0
			sbSQL.append(" , 0 as YOYAKU");					// F126:予約件数	初期値:0

			sbSQL.append(" , null as HDN_UPDDT");			// F127:更新日時

			sbSQL.append(" , "+DefineReport.ValKbn135.VAL0.getVal()+" as AREAKBN_SHINA");	// F128
			sbSQL.append(" , "+DefineReport.ValKbn135.VAL1.getVal()+" as AREAKBN_BAIKA");	// F129
			sbSQL.append(" , "+DefineReport.ValKbn135.VAL0.getVal()+" as AREAKBN_SIR");		// F130
			sbSQL.append(" , "+DefineReport.ValKbn135.VAL0.getVal()+" as AREAKBN_TBMN");	// F131

			sbSQL.append(" from SYSIBM.SYSDUMMY1 ");

		// 流用新規・変更
		}else{

			// 流用新規の場合、商品コード、ソースコード、ソース区分、定計区分、メーカーコードは元データを参照しない
			sbSQL.append(" select ");
			if(isCopyNew || isCsverr){
				sbSQL.append("   null as SHNCD");		// F1
			}else{
				sbSQL.append(" trim(left(lpad(SHNCD, 8, '0'), 4) || '-' || SUBSTR(lpad(SHNCD, 8, '0'), 5)) as SHNCD"); //F1
			}
			sbSQL.append(" , right(nvl(T1.YOYAKUDT,0), 6) as YOYAKUDT");
			sbSQL.append(" , right(nvl(T1.TENBAIKADT,0), 6) as TENBAIKADT");
			sbSQL.append(" , CASE WHEN T1.YOT_BMNCD = '0' THEN NULL ELSE T1.YOT_BMNCD END AS YOT_BMNCD");
			sbSQL.append(" , CASE WHEN T1.YOT_BMNCD = '0' AND T1.YOT_DAICD = '0' THEN NULL ELSE T1.YOT_DAICD END AS YOT_DAICD");
			sbSQL.append(" , CASE WHEN T1.YOT_BMNCD = '0' AND T1.YOT_CHUCD = '0' THEN NULL ELSE T1.YOT_CHUCD END AS YOT_CHUCD");
			sbSQL.append(" , CASE WHEN T1.YOT_BMNCD = '0' AND T1.YOT_SHOCD = '0' THEN NULL ELSE T1.YOT_SHOCD END AS YOT_SHOCD");
			sbSQL.append(" , CASE WHEN T1.URI_BMNCD = '0' THEN NULL ELSE T1.URI_BMNCD END AS URI_BMNCD");
			sbSQL.append(" , CASE WHEN T1.URI_BMNCD = '0' AND T1.URI_DAICD = '0' THEN NULL ELSE T1.URI_DAICD END AS URI_DAICD");
			sbSQL.append(" , CASE WHEN T1.URI_BMNCD = '0' AND T1.URI_CHUCD = '0' THEN NULL ELSE T1.URI_CHUCD END AS URI_CHUCD");
			sbSQL.append(" , CASE WHEN T1.URI_BMNCD = '0' AND T1.URI_SHOCD = '0' THEN NULL ELSE T1.URI_SHOCD END AS URI_SHOCD");	//F10
			sbSQL.append(" , T1.BMNCD");
			sbSQL.append(" , T1.DAICD");
			sbSQL.append(" , T1.CHUCD");
			sbSQL.append(" , T1.SHOCD");
			sbSQL.append(" , T1.SSHOCD");
			sbSQL.append(" , right(T1.ATSUK_STDT, 6) as ATSUK_STDT");
			sbSQL.append(" , right(T1.ATSUK_EDDT, 6) as ATSUK_ETDT");
			sbSQL.append(" , T1.TEISHIKBN");
			sbSQL.append(" , T1.SHNAN");
			sbSQL.append(" , T1.SHNKN");
			sbSQL.append(" , T1.PCARDKN");
			sbSQL.append(" , T1.POPKN");
			sbSQL.append(" , T1.RECEIPTAN");
			sbSQL.append(" , T1.RECEIPTKN");
			sbSQL.append(" , T1.PCKBN");
			sbSQL.append(" , T1.KAKOKBN");
			sbSQL.append(" , T1.ICHIBAKBN");
			sbSQL.append(" , T1.SHNKBN");
			sbSQL.append(" , T1.SANCHIKN");
			sbSQL.append(" , T1.SSIRCD");
			sbSQL.append(" , T1.HSPTN");
			sbSQL.append(" , T1.RG_ATSUKFLG");
			sbSQL.append(" , T1.RG_GENKAAM");
			sbSQL.append(" , T1.RG_BAIKAAM");
			sbSQL.append(" , T1.RG_IRISU");
			sbSQL.append(" , T1.RG_IDENFLG");
			sbSQL.append(" , T1.RG_WAPNFLG");
			sbSQL.append(" , T1.HS_ATSUKFLG");
			sbSQL.append(" , T1.HS_GENKAAM");
			sbSQL.append(" , T1.HS_BAIKAAM");
			sbSQL.append(" , T1.HS_IRISU");
			sbSQL.append(" , T1.HS_WAPNFLG");
			sbSQL.append(" , T1.HS_SPOTMINSU");
			sbSQL.append(" , T1.HP_SWAPNFLG");
			sbSQL.append(" , T1.KIKKN");
			sbSQL.append(" , T1.UP_YORYOSU");
			sbSQL.append(" , T1.UP_TYORYOSU");
			sbSQL.append(" , right('0'||T1.UP_TANIKBN, 2) as UP_TANIKBN");
			sbSQL.append(" , T1.SHNYOKOSZ");
			sbSQL.append(" , T1.SHNTATESZ");
			sbSQL.append(" , T1.SHNOKUSZ");
			sbSQL.append(" , T1.SHNJRYOSZ");
			sbSQL.append(" , T1.PBKBN");
			sbSQL.append(" , right('0'||T1.KOMONOKBM, 2) as KOMONOKBM");
			sbSQL.append(" , right('0'||T1.TANAOROKBN, 2) as TANAOROKBN");
			if(isCopyNew){
				sbSQL.append(" , null as TEIKEIKBN");
			}else{
				sbSQL.append(" , T1.TEIKEIKBN");
			}
			sbSQL.append(" , T1.ODS_HARUSU");
			sbSQL.append(" , T1.ODS_NATSUSU");
			sbSQL.append(" , T1.ODS_AKISU");
			sbSQL.append(" , T1.ODS_FUYUSU");
			sbSQL.append(" , T1.ODS_NYUKASU");
			sbSQL.append(" , T1.ODS_NEBIKISU");
			sbSQL.append(" , T1.PCARD_SHUKBN");
			sbSQL.append(" , T1.PCARD_IROKBN");
			sbSQL.append(" , T1.ZEIKBN");
			sbSQL.append(" , T1.ZEIRTKBN");
			sbSQL.append(" , T1.ZEIRTKBN_OLD");
			sbSQL.append(" , right(T1.ZEIRTHENKODT, 6) as ZEIRTHENKODT");
			sbSQL.append(" , T1.SEIZOGENNISU");
			sbSQL.append(" , T1.TEIKANKBN");
			if(isCopyNew){
				sbSQL.append(" , null as MAKERCD");
			}else{
				sbSQL.append(" , T1.MAKERCD");
			}
			sbSQL.append(" , right('0'||T1.IMPORTKBN, 2) as IMPORTKBN");
			sbSQL.append(" , T1.SIWAKEKBN");
			sbSQL.append(" , T1.HENPIN_KBN");
			sbSQL.append(" , T1.TAISHONENSU");
			sbSQL.append(" , T1.CALORIESU");
			sbSQL.append(" , T1.ELPFLG");
			sbSQL.append(" , T1.BELLMARKFLG");
			sbSQL.append(" , T1.RECYCLEFLG");
			sbSQL.append(" , T1.ECOFLG");
			sbSQL.append(" , T1.HZI_YOTO");
			sbSQL.append(" , T1.HZI_ZAISHITU");
			sbSQL.append(" , T1.HZI_RECYCLE");
			sbSQL.append(" , T1.KIKANKBN");
			sbSQL.append(" , right('0'||T1.SHUKYUKBN, 2) as SHUKYUKBN");
			sbSQL.append(" , T1.DOSU");
			sbSQL.append(" , T1.CHINRETUCD");
			sbSQL.append(" , trim(T1.DANTUMICD) AS DANTUMICD ");
			sbSQL.append(" , T1.KASANARICD");
			sbSQL.append(" , T1.KASANARISZ");
			sbSQL.append(" , T1.ASSHUKURT");
			sbSQL.append(" , T1.SHUBETUCD");
			if(isCopyNew){
				sbSQL.append(" , null as URICD");
			}else{
				sbSQL.append(" , T1.URICD");
			}
			sbSQL.append(" , T1.SALESCOMKN");
			sbSQL.append(" , T1.URABARIKBN");
			sbSQL.append(" , T1.PCARD_OPFLG");
			sbSQL.append(" , T1.PARENTCD");
			sbSQL.append(" , T1.BINKBN");
			sbSQL.append(" , T1.HAT_MONKBN");
			sbSQL.append(" , T1.HAT_TUEKBN");
			sbSQL.append(" , T1.HAT_WEDKBN");
			sbSQL.append(" , T1.HAT_THUKBN");
			sbSQL.append(" , T1.HAT_FRIKBN");
			sbSQL.append(" , T1.HAT_SATKBN");
			sbSQL.append(" , T1.HAT_SUNKBN");
			sbSQL.append(" , T1.READTMPTN");
			sbSQL.append(" , T1.SIMEKAISU");
			sbSQL.append(" , T1.IRYOREFLG");
			if(isCopyNew){
				sbSQL.append(" , 0 as TOROKUMOTO");
				sbSQL.append(" , 0 as UPDKBN");
				sbSQL.append(" , 0 as SENDFLG");
				sbSQL.append(" , null as OPERATOR");
				sbSQL.append(" , null as ADDDT");
				sbSQL.append(" , null as UPDDT");
			}else{
				sbSQL.append(" , T1.TOROKUMOTO");
				sbSQL.append(" , T1.UPDKBN");
				if(isCsverr){
					sbSQL.append(" , 0 as SENDFLG");
				}else{
					sbSQL.append(" , T1.SENDFLG");
				}
				sbSQL.append(" , T1.OPERATOR");
				if(isCsverr){
					sbSQL.append(" , '__/__/__' as ADDDT");
				}else{
					sbSQL.append(" , nvl(TO_CHAR(T1.ADDDT, 'YY/MM/DD'),'__/__/__') as ADDDT");
				}
				sbSQL.append(" , nvl(TO_CHAR(T1.UPDDT, 'YY/MM/DD'),'__/__/__') as UPDDT");
			}
			sbSQL.append(" , T1.K_HONKB");				// F116: 保温区分
			sbSQL.append(" , T1.K_WAPNFLG_R");			// F117: デリカワッペン区分_レギュラー
			sbSQL.append(" , T1.K_WAPNFLG_H");			// F118: デリカワッペン区分_販促
			sbSQL.append(" , T1.K_TORIKB");				// F119: 取扱区分
			sbSQL.append(" , T1.ITFCD");				// F120: ITFコード
			sbSQL.append(" , T1.CENTER_IRISU");			// F121: センター入数

			sbSQL.append(" , (select YOBIDASHICD from INAMS.MSTKRYO T2 where T1.SHNCD = T2.SHNCD and T1.BMNCD = T2.BMNCD order by T2.UPDDT desc fetch first 1 rows only ) as YOBIDASHICD");		// F122:呼出コード
			sbSQL.append(" , (select AVGPTANKAAM from INAMS.MSTAVGPTANKA T3 where T1.SHNCD = T3.SHNCD) as RG_AVGPTANKAAM");	// F123:TODO
			sbSQL.append(" , '' as HS_AVGPTANKAAM");	// F124:販促平均パック単価(=空白)
			if(isCsverr){
				sbSQL.append(" , KETAKBN as KETA");		// F125:桁
			}else{
				sbSQL.append(" , '' as KETA");			// F125:桁
			}
			sbSQL.append(" , "+szYoyaku+" as YOYAKU");	// F126:予約件数	初期値:0
			if(isCopyNew){
				sbSQL.append(" , null as HDN_UPDDT");											// F127:更新日時
			}else {
				sbSQL.append(" , TO_CHAR(T1.UPDDT, 'YYYYMMDDHH24MISSNNNNNN') as HDN_UPDDT");			// F127:更新日時
			}
			sbSQL.append(" , nvl((select max(AREAKBN) from "+szTableShina+" T1 "+ szWhereTable + "),"+DefineReport.ValKbn135.VAL0.getVal()+") as AREAKBN_SHINA");	// F128
			sbSQL.append(" , nvl((select max(AREAKBN) from "+szTableBaika+" T1 "+ szWhereTable + "),"+DefineReport.ValKbn135.VAL1.getVal()+") as AREAKBN_BAIKA");	// F129
			sbSQL.append(" , nvl((select max(AREAKBN) from "+szTableSir+" T1 "+ szWhereTable + "),"+DefineReport.ValKbn135.VAL0.getVal()+") as AREAKBN_SIR");		// F130
			sbSQL.append(" , nvl((select max(AREAKBN) from "+szTableTbmn+" T1 "+ szWhereTable + "),"+DefineReport.ValKbn135.VAL0.getVal()+") as AREAKBN_TBMN");		// F131
			sbSQL.append(" , T1.TITKNNO");
			sbSQL.append(" , T1.TITSTCD");
			sbSQL.append(" from "+szTableShn+" T1 ");
			sbSQL.append(" " + szWhereTable + " and nvl(UPDKBN, 0) <> 1 ");
			sbSQL.append( super.getFechSql("1"));
		}

		// オプション情報設定
		JSONObject option = new JSONObject();
		option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
		option.put("rows_y", yArray);
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


	boolean isTest = false;

	/** 固定値定義（テーブルタイプ）<br>
	 *   */
	public enum TblType {
		/** 正 */
		SEI(1, "(正)"),
		/** 予約 */
		YYK(2, "(予)"),
		/** ジャーナル */
		JNL(3, "(ジャーナル)"),
		/** CSVトラン */
		CSV(4, "(CSVトラン)"),
		/** 一時作業用テーブル */
		TMP(5, "(一時作業用テーブル)");

		private final Integer val;
		private final String txt;
		/** 初期化 */
		private TblType(Integer val, String txt) {
			this.val = val;
			this.txt = txt;
		}
		/** @return val 値 */
		public Integer getVal() { return val; }
		/** @return txt テキスト */
		public String getTxt() { return txt; }

	}

	/** SQLリスト保持用変数 */
	ArrayList<String> sqlList = new ArrayList<String>();
	/** SQLのパラメータリスト保持用変数 */
	ArrayList<ArrayList<String>> prmList = new ArrayList<ArrayList<String>>();
	/** SQL保持用変数 */
	String sqlCmd = "";
	/** SQLのパラメータ保持用変数 */
	ArrayList<String> cmdParams = new ArrayList<String>();
	/** SQLログ用のラベルリスト保持用変数 */
	ArrayList<String> lblList = new ArrayList<String>();


	/** 商品マスタ列数 */
	int mstshn_col_num = MSTSHNLayout.values().length;

	/** ジャーナル更新のKEY保持用変数 */
	String jnlshn_seq = "";			//
	/** ジャーナル更新のテーブル区分保持用変数 */
	String jnlshn_tablekbn = "";	//
	/** ジャーナル更新の処理区分保持用変数 */
	String jnlshn_trankbn = "";
	/** メッセージ出力用商品・販売コード */
	String msgShnCd = "";
	String msgUriCd = "";

//	/** ジャーナル_商品マスタ特殊情報保持用 */
//	String[] jnlshn_add_data = new String[JNLSHNLayout.values().length];

	/** CSV取込トラン特殊情報保持用 */
	String csvshn_seq = "";
	String[] csvshn_add_data = new String[CSVSHNLayout.values().length];

	/**
	 * 更新処理実行
	 * @return
	 *
	 * @throws Exception
	 */
	private JSONObject updateData(HashMap<String, String> map, User userInfo, String sysdate, JSONObject objset) throws Exception {
		// パラメータ確認
		String szSelShncd	= map.get("SEL_SHNCD");		// 検索商品コード
		String szShncd		= map.get("SHNCD");			// 入力商品コード
		String szSeq		= map.get("SEQ");			// CSVエラー.SEQ
		String szInputno	= map.get("INPUTNO");		// CSVエラー.入力番号
		String szCsvUpdkbn	= map.get("CSV_UPDKBN");	// CSVエラー.CSV登録区分
		String szYoyakudt	= map.get("YOYAKUDT");		// CSVエラー用.マスタ変更予定日
		String szTenbaikadt	= map.get("TENBAIKADT");	// CSVエラー用.店売価実施日
		String sendBtnid	= map.get("SENDBTNID");		// 呼出しボタン
		String teianNo = map.get("TEIAN");
		String status = map.get("STATUS");


		JSONArray dataArray				= objset.optJSONArray("DATA");				// 対象情報（主要な更新情報）
		JSONArray dataArrayAdd			= objset.optJSONArray("DATA_ADD");			// 対象情報（MD03111701:予約同一項目変更用の追加データ）
		JSONArray dataArraySRCCD		= objset.optJSONArray("DATA_SRCCD");		// ソースコード
		JSONArray dataArrayTENGP4		= objset.optJSONArray("DATA_TENGP4");		// 店別異部門
		JSONArray dataArrayTENGP3		= objset.optJSONArray("DATA_TENGP3");		// 品揃えグループ
		JSONArray dataArrayTENGP2		= objset.optJSONArray("DATA_TENGP2");		// 売価コントロール
		JSONArray dataArrayTENGP1		= objset.optJSONArray("DATA_TENGP1");		// 仕入グループ
		JSONArray dataArrayTENKABUTSU	= objset.optJSONArray("DATA_TENKABUTSU");	// 添加物
		JSONArray dataArrayGROUP		= objset.optJSONArray("DATA_GROUP");		// グループ分類
		JSONArray dataArrayAHS			= objset.optJSONArray("DATA_AHS");			// 自動発注データ

		JSONObject option = new JSONObject();
		JSONArray msg = new JSONArray();

		int targetId = 0;

		// パラメータ確認
		// 必須チェック
		if ( sendBtnid == null || dataArray.isEmpty() || dataArray.size() != 1 || dataArray.getJSONObject(0).isEmpty() || teianNo == null || teianNo.equals("") ) {
			option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
			return option;
		}

		// ログインユーザー情報取得
		String userId	= userInfo.getId();			// ログインユーザー
		String szYoyaku		= "0";					// 予約件数


		// SQLパターン
		// ①正 .新規 → 正 ：Insert処理、ジャ：Insert処理
		// ②正 .変更 → 正 ：Update処理、予12：条件付Update処理※1、ジャ：Insert処理	※1.予約商品があり（商品コードが同じ）、かつ同一項目の値が同じ場合は商品－正の値によって更新登録
		// ③正 .削除 → 正 ：Update処理、ジャ：Insert処理								※予1,2がある場合削除不可
		// ④予1.新規 → 予1：Insert処理、ジャ：Insert処理
		// ⑤予1.変更 → 予1：Update処理、予2 ：条件付Update処理※2、ジャ：Insert処理	※2.予約商品があり（商品コードが同じ）、かつ同一項目の値が同じ場合は商品－正の値によって更新登録
		// ⑥予1.削除 → 予1：Update処理、ジャ：Insert処理								※予2がある場合取消不可
		// ⑦予2.新規 → 予2：insert処理、ジャ：Insert処理
		// ⑧予2.変更 → 予2：Update処理、ジャ：Insert処理
		// ⑨予2.削除 → 予2：Update処理、ジャ：Insert処理

		// ①正 .新規
		boolean isNew = DefineReport.Button.NEW.getObj().equals(sendBtnid)||DefineReport.Button.COPY.getObj().equals(sendBtnid)||DefineReport.Button.SEL_COPY.getObj().equals(sendBtnid);
		// ②正 .変更
		boolean isChange = DefineReport.Button.SEL_CHANGE.getObj().equals(sendBtnid)|| DefineReport.Button.SEARCH.getObj().equals(sendBtnid)|| DefineReport.Button.SEI.getObj().equals(sendBtnid);
		// EX.CSVエラー修正
		boolean isCsverr = DefineReport.Button.ERR_CHANGE.getObj().equals(sendBtnid);
		if(isCsverr){
			isNew = DefineReport.ValFileUpdkbn.NEW.getVal().equals(szCsvUpdkbn);
			isChange = DefineReport.ValFileUpdkbn.UPD.getVal().equals(szCsvUpdkbn);
		}

		// 基本登録情報
		JSONObject data = dataArray.getJSONObject(0);

		// 予約情報再取得
		JSONArray yArray = new JSONArray();
		if(!isNew){
			JSONArray array = getYoyakuJSONArray(map);
			szYoyaku = Integer.toString(array.size());
			yArray.addAll(array);
		}

		// ①正 .新規
		if(isNew){
			jnlshn_trankbn = InfTrankbn.INS.getVal();

			// --- 01.商品
			JSONObject result1 = this.createSqlMSTSHN(userId, sendBtnid, data, TblType.SEI, SqlType.MRG, teianNo, status);
			//　実行
			targetId = super.executeSQLReturnId(sqlCmd, cmdParams, "SHNCD", 0, "INAWS.PIMTIT");

		// ②正 .変更
		}else if(isChange || isCsverr){
			jnlshn_trankbn = InfTrankbn.UPD.getVal();
			// --- 01.商品
			JSONObject result1 = this.createSqlMSTSHN(userId, sendBtnid, data, TblType.SEI, SqlType.UPD, teianNo, status);

			// 排他チェック実行
			String targetTable = null;
			String targetWhere = "nvl(UPDKBN, 0) <> 1";
			ArrayList<String> targetParam = new ArrayList<String>();
			// EX.CSVエラー修正
			if(isCsverr){
				targetTable = "INAWS.CSVSHN";
				targetWhere += " and SEQ = ? and INPUTNO = ?";
				targetParam.add(szSeq);
				targetParam.add(szInputno);
			}else{
				// ①正 .新規/②正 .変更
				targetTable = "INAWS.PIMTIT";
				targetWhere += " and SHNCD = ?";
				targetParam.add(data.optString(MSTSHNLayout.SHNCD.getId()));
			}
	 		if(!super.checkExclusion(targetTable, targetWhere, targetParam, data.optString("F127"))){
	 			msg.add(MessageUtility.getDbMessageExclusion(FieldType.DEFAULT, new String[]{}));
	 			option.put(MsgKey.E.getKey(), msg);
				return option;
			}

			//　実行
			targetId = super.executeSQLReturnId(sqlCmd, cmdParams, "SHNCD", Integer.parseInt(szShncd), "INAWS.PIMTIT");

		}



		// ************ 子テーブル処理 ***********
		// 2004/03/05に、子テーブルの同一項目考慮はなくなった模様
		TblType baseTblType = isNew||isChange ? TblType.SEI :TblType.YYK;

		JSONArray dataArrayDel = new JSONArray();
		JSONArray dataArrayDelTENKABUTSU = new JSONArray();
		// 子テーブルは、一度削除してから追加なので、キー項目に注意
		dataArrayDel.add(this.createJSONObject(new String[]{"F1"}, new String[]{szShncd}));
		dataArrayDelTENKABUTSU.add(this.createJSONObject(new String[]{"F1"}, new String[]{szShncd}));

		// --- 02.仕入グループ INAMS.MSTSIRGPSHN
		JSONObject result2D= this.createSqlMSTSIRGPSHN(userId, sendBtnid, dataArrayDel, baseTblType, SqlType.DEL);
		if(dataArrayTENGP1.size() > 0){
			dataArrayTENGP1 = this.addSHNCD(dataArrayTENGP1, isNew, String.format("%08d",targetId));
			JSONObject result2 = this.createSqlMSTSIRGPSHN(userId, sendBtnid, dataArrayTENGP1, baseTblType, SqlType.INS);
		}

		// --- 03.売価コントロール INAMS.MSTBAIKACTL
		JSONObject result3D= this.createSqlMSTBAIKACTL(userId, sendBtnid, dataArrayDel, baseTblType, SqlType.DEL);
		if(dataArrayTENGP2.size() > 0){
			dataArrayTENGP2 = this.addSHNCD(dataArrayTENGP2, isNew, String.format("%08d",targetId));
			JSONObject result3 = this.createSqlMSTBAIKACTL(userId, sendBtnid, dataArrayTENGP2, baseTblType, SqlType.INS);
		}
		// --- 04.ソースコード管理 INAMS.MSTSRCCD
		JSONObject result4D= this.createSqlMSTSRCCD(userId, sendBtnid, dataArrayDel, baseTblType, SqlType.DEL);
		if(dataArraySRCCD.size() > 0){
			dataArraySRCCD = this.addSHNCD(dataArraySRCCD, isNew, String.format("%08d",targetId));
			JSONObject result4 = this.createSqlMSTSRCCD(userId, sendBtnid, dataArraySRCCD, baseTblType, SqlType.INS);
		}

		// --- 05.添加物 INAMS.MSTTENKABUTSU
		JSONObject result5D= this.createSqlMSTTENKABUTSU(userId, sendBtnid, dataArrayDelTENKABUTSU, baseTblType, SqlType.DEL);
		if(dataArrayTENKABUTSU.size() > 0){
			dataArrayTENKABUTSU = this.addSHNCD(dataArrayTENKABUTSU, isNew, String.format("%08d",targetId));
			JSONObject result5 = this.createSqlMSTTENKABUTSU(userId, sendBtnid, dataArrayTENKABUTSU, baseTblType, SqlType.INS);
		}

		// --- 06.品揃グループ INAMS.MSTSHINAGP
		JSONObject result6D= this.createSqlMSTSHINAGP(userId, sendBtnid, dataArrayDel, baseTblType, SqlType.DEL);
		if(dataArrayTENGP3.size() > 0){
			dataArrayTENGP3 = this.addSHNCD(dataArrayTENGP3, isNew, String.format("%08d",targetId));
			JSONObject result6 = this.createSqlMSTSHINAGP(userId, sendBtnid, dataArrayTENGP3, baseTblType, SqlType.INS);
		}

		// --- 07.グループ分類名
		JSONObject result7D= this.createSqlMSTGRP(userId, sendBtnid, dataArrayDel, baseTblType, SqlType.DEL);
		if(dataArrayGROUP.size() > 0){
			dataArrayGROUP = this.addSHNCD(dataArrayGROUP, isNew, String.format("%08d",targetId));
			// グループ名登録処理+登録情報更新
			this.updateMSTGROUP(userId, sendBtnid, dataArrayGROUP);

			JSONObject result7 = this.createSqlMSTGRP(userId, sendBtnid, dataArrayGROUP, baseTblType, SqlType.INS);
		}

//		// --- 08.自動発注
//		JSONObject result8D= this.createSqlMSTAHS(userId, sendBtnid, dataArrayDel, baseTblType, SqlType.DEL);
//		if(dataArrayAHS.size() > 0){
//			dataArrayAHS = this.addSHNCD(dataArrayAHS, isNew, String.valueOf(targetId));
//			JSONObject result6 = this.createSqlMSTAHS(userId, sendBtnid, dataArrayAHS, baseTblType, SqlType.INS);
//		}
//
//		// --- 09.店別異部門
//		JSONObject result9D= this.createSqlMSTSHNTENBMN(userId, sendBtnid, dataArrayDel, baseTblType, SqlType.DEL);
//		if(dataArrayTENGP4.size() > 0){
//			dataArrayTENGP4 = this.addSHNCD(dataArrayTENGP4, isNew, String.valueOf(targetId));
//			JSONObject result9 = this.createSqlMSTSHNTENBMN(userId, sendBtnid, dataArrayTENGP4, baseTblType, SqlType.INS);
//		}

		// ************ 関連テーブル処理 ***********
		// --- 07.メーカー INAMS.MSTMAKER
		String makercd = data.optString(MSTSHNLayout.MAKERCD.getId());
		if(StringUtils.isNotEmpty(makercd) && dataArraySRCCD.size() > 0){
			// メーカーコード存在チェック
			if(!this.checkMstExist(DefineReport.InpText.MAKERCD.getObj(), makercd)){
				String jancd = dataArraySRCCD.optJSONObject(0).optString(MSTSRCCDLayout.SRCCD.getId());
				JSONObject dataMAKER = this.createJSONObject(new String[]{"F1", "F4"}, new String[]{makercd, jancd});
				JSONObject result7 = this.createSqlMSTMAKER(userId, sendBtnid, dataMAKER, TblType.SEI, SqlType.INS);
			}
		}

		// CSV取込ﾄﾗﾝ_商品ﾃｰﾌﾞﾙからﾚｺｰﾄﾞの論理削除
//		if(isCsverr){
//			csvshn_add_data[CSVSHNLayout.SEQ.getNo()-1] = szSeq;
//			csvshn_add_data[CSVSHNLayout.INPUTNO.getNo()-1] = szInputno;
//			// --- 24.CSV取込ﾄﾗﾝ_商品
//			JSONObject result1 = this.createSqlMSTSHN_DEL(userId, sendBtnid, data, TblType.CSV, SqlType.DEL);
//		}


		ArrayList<Integer> countList  = new ArrayList<Integer>();

		if (targetId > 0) {
			countList.add(1);
			if(sqlList.size() > 0){
				countList.addAll(super.executeSQLs(sqlList, prmList));
			}
		} else {
			countList.add(0);
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
	 * update shncd for sub table after new shncd generated
	 *
	 * @return updated array
	 */
	public JSONArray addSHNCD(JSONArray array, boolean isNew, String shncd) {
		JSONArray rs = array;
		if(isNew) {
			for (int i=0; i < rs.size(); i++){
			    JSONObject itemArr = rs.getJSONObject(i);
			    itemArr.put("F1", shncd);
			    rs.set(i, itemArr);
			}
		}
		return rs;
	}

	public JSONObject createJSONObject(String[] keys, String[] values) {
		JSONObject obj = new JSONObject();
		for (int i = 0; i < keys.length; i++) {
			obj.put(keys[i], values[i]);
		}
		return obj;
	}

	/**
	 * チェック処理(削除時)
	 *
	 * @throws Exception
	 */
	public JSONArray checkDel(HashMap<String, String> map, User userInfo, String sysdate) {
		// パラメータ確認
		String szShncd		= map.get("SHNCD");			// 入力商品コード
		String sendBtnid	= map.get("SENDBTNID");	// 呼出しボタン

		JSONArray dataArray = JSONArray.fromObject(map.get("DATA"));				// 対象情報（主要な更新情報）
		JSONArray dataArraySRCCD = JSONArray.fromObject(map.get("DATA_SRCCD"));		// ソースコード
		JSONArray dataArrayTENGP4 = JSONArray.fromObject(map.get("DATA_TENGP4"));	// 店別異部門
		JSONArray dataArrayTENGP3 = JSONArray.fromObject(map.get("DATA_TENGP3"));	// 品揃えグループ
		JSONArray dataArrayTENGP2 = JSONArray.fromObject(map.get("DATA_TENGP2"));	// 売価コントロール
		JSONArray dataArrayTENGP1 = JSONArray.fromObject(map.get("DATA_TENGP1"));	// 仕入グループ
		JSONArray dataArrayTENKABUTSU = JSONArray.fromObject(map.get("DATA_TENKABUTSU"));	// 添加物
		JSONArray dataArrayGROUP = JSONArray.fromObject(map.get("DATA_GROUP"));		// グループ分類
		JSONArray dataArrayAHS = JSONArray.fromObject(map.get("DATA_AHS"));			// 自動発注データ

		// 正 .新規
		boolean isNew = DefineReport.Button.NEW.getObj().equals(sendBtnid)||DefineReport.Button.COPY.getObj().equals(sendBtnid)||DefineReport.Button.SEL_COPY.getObj().equals(sendBtnid);
		// 正 .変更
		boolean isChange = DefineReport.Button.SEL_CHANGE.getObj().equals(sendBtnid)|| DefineReport.Button.SEARCH.getObj().equals(sendBtnid)|| DefineReport.Button.SEI.getObj().equals(sendBtnid);
		// 予約1
		boolean isYoyaku1 = DefineReport.Button.YOYAKU1.getObj().equals(sendBtnid);
		// 予約2
		boolean isYoyaku2 = DefineReport.Button.YOYAKU2.getObj().equals(sendBtnid);
		// EX.CSVエラー修正
		boolean isCsverr = DefineReport.Button.ERR_CHANGE.getObj().equals(sendBtnid);

		MessageUtility mu = new MessageUtility();

		List<JSONObject> msgList = new ArrayList<JSONObject>();
		// CSV情報を削除する場合は無条件チェックなし
		if(!isCsverr){
			msgList = this.checkDataDel(
					isNew, isChange, isYoyaku1, isYoyaku2, false,
					map, userInfo, sysdate,mu,
					dataArray, dataArraySRCCD, dataArrayTENGP3, dataArrayTENGP2, dataArrayTENGP1, dataArrayTENKABUTSU);
		}

		JSONArray msgArray = new JSONArray();
		// MessageBoxを出す関係上、1件のみ表示
		if(msgList.size() > 0){
			msgArray.add(msgList.get(0));
		}
		return msgArray;
	}
	public JSONArray checkData(
			boolean isNew, boolean isChange, boolean isYoyaku1, boolean isYoyaku2, boolean isCsvErr, boolean isCsvUpload,
			HashMap<String, String> map, User userInfo, String sysdate1, MessageUtility mu,
			JSONArray dataArray,			// 対象情報（主要な更新情報）
			JSONArray dataArrayAdd,			// 対象情報（追加更新情報）
			JSONArray dataArraySRCCD,		// ソースコード
			JSONArray dataArrayTENGP4,		// 店部門別異部門
			JSONArray dataArrayTENGP3,		// 品揃えグループ
			JSONArray dataArrayTENGP2,		// 売価コントロール
			JSONArray dataArrayTENGP1,		// 仕入グループ
			JSONArray dataArrayTENKABUTSU,	// 添加物
			JSONArray dataArrayGROUP,		// グループ分類
			JSONArray dataArrayAHS,			// 自動発注データ
			JSONObject dataOther			// その他情報
		) {

		JSONArray msg = new JSONArray();


		// DB最新情報再取得
		JSONArray yArray = new JSONArray();
		if(!isNew){
			JSONArray array = getYoyakuJSONArray(map);
			yArray.addAll(array);
		}
		JSONObject seiJsonObject = new JSONObject();
		if(isChange){
			JSONArray array = getSeiJSONArray(map);
			seiJsonObject = array.optJSONObject(0);
		}

		JSONObject data = dataArray.optJSONObject(0);

		String txt_shncd = StringUtils.strip(data.optString(MSTSHNLayout.SHNCD.getId()));
		String txt_yoyakudt = data.optString(MSTSHNLayout.YOYAKUDT.getId());
		String txt_tenbaikadt = data.optString(MSTSHNLayout.TENBAIKADT.getId());
		String txt_bmncd = data.optString(MSTSHNLayout.BMNCD.getId());

		String login_dt = sysdate1;	// 処理日付
		String sysdate = login_dt;	// 比較用処理日付

		// 基本データチェック:入力値がテーブル定義と矛盾してないか確認
		String[] notTaretCol = new String[]{"UPDDT", "ADDDT"};		// チェック除外項目
		// 1.商品マスタ
		RefTable errTbl = RefTable.MSTSHN;
		for(MSTSHNLayout colinf: MSTSHNLayout.values()){
			if(ArrayUtils.contains(notTaretCol, colinf.getCol())){ continue;}				// チェック除外項目
			String val = StringUtils.trim(data.optString(colinf.getId()));
			if(StringUtils.isNotEmpty(val)){
				DataType dtype = null;
				int[] digit = null;
				try {
					DefineReport.InpText inpsetting = DefineReport.InpText.valueOf(colinf.getCol());
					dtype = inpsetting.getType();
					digit = new int[]{inpsetting.getDigit1(), inpsetting.getDigit2()};

					if(StringUtils.equals(MSTSHNLayout.SANCHIKN.getCol(), colinf.getCol())){
						// POPサイズ項目
						// DefineReportの設定を優先しない。
						dtype = DefineReport.DataType.ZEN;
						digit = colinf.getDigit();

					}

				}catch (IllegalArgumentException e){
					dtype = colinf.getDataType();
					digit = colinf.getDigit();
				}
				// ①データ型による文字種チェック
				if(!InputChecker.checkDataType(dtype, val)){
					JSONObject o = mu.getDbMessageObjDataTypeErr(dtype, new String[]{});
					this.setCsvshnErrinfo(o, errTbl, colinf, val);
					msg.add(o);
					if(!colinf.isText()){
						data.element(colinf.getId(), "");		// CSVトラン用に空
					}
				}
				// ②データ桁チェック
				if(!InputChecker.checkDataLen(dtype, val, digit)){
					JSONObject o = mu.getDbMessageObjLen(dtype, new String[]{});
					this.setCsvshnErrinfo(o, errTbl, colinf, val);
					msg.add(o);
					data.element(colinf.getId(), "");		// CSVトラン用に空
				}

				// ③名称マスタ存在チェックを行う
				String szmeisyoKbn = "";
				if(!InputChecker.checkKbnExist(colinf.getCol(), val, szmeisyoKbn)){
					JSONObject o = mu.getDbMessageObj("E30027", new String[]{"名称コード"});
					this.setCsvshnErrinfo(o, errTbl, colinf, val);
					msg.add(o);
					data.element(colinf.getId(), "");		// CSVトラン用に空
				}
			}
		}
		// 2.ソースマスタ
		errTbl = RefTable.MSTSRCCD;
		for (int i = 0; i < dataArraySRCCD.size(); i++) {
			JSONObject jo = dataArraySRCCD.optJSONObject(i);
			for(MSTSRCCDLayout colinf: MSTSRCCDLayout.values()){
				if(ArrayUtils.contains(notTaretCol, colinf.getCol())){ continue;}				// チェック除外項目
				String val = StringUtils.trim(jo.optString(colinf.getId()));
				if(StringUtils.isNotEmpty(val)){
					DataType dtype = null;
					int[] digit = null;
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
						JSONObject o = mu.getDbMessageObjDataTypeErr(dtype, new String[]{});
						this.setCsvshnErrinfo(o, errTbl, colinf, val);
						msg.add(o);
						if(!colinf.isText()){
							jo.element(colinf.getId(), "");		// CSVトラン用に空
						}
					}
					// ②データ桁チェック
					if(!InputChecker.checkDataLen(dtype, val, digit)){
						JSONObject o = mu.getDbMessageObjLen(dtype, new String[]{});
						this.setCsvshnErrinfo(o, errTbl, colinf, val);
						msg.add(o);
						jo.element(colinf.getId(), "");		// CSVトラン用に空
					}
				}
			}
		}
		// 3.仕入グループ商品マスタ
		errTbl = RefTable.MSTSIRGPSHN;
		for (int i = 0; i < dataArrayTENGP1.size(); i++) {
			JSONObject jo = dataArrayTENGP1.optJSONObject(i);
			for(MSTSIRGPSHNLayout colinf: MSTSIRGPSHNLayout.values()){
				if(ArrayUtils.contains(notTaretCol, colinf.getCol())){ continue;}				// チェック除外項目
				String val = StringUtils.trim(jo.optString(colinf.getId()));
				if(StringUtils.isNotEmpty(val)){
					DataType dtype = null;
					int[] digit = null;
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
						JSONObject o = mu.getDbMessageObjDataTypeErr(dtype, new String[]{});
						this.setCsvshnErrinfo(o, errTbl, colinf, val);
						msg.add(o);
						if(!colinf.isText()){
							jo.element(colinf.getId(), "");		// CSVトラン用に空
						}
					}
					// ②データ桁チェック
					if(!InputChecker.checkDataLen(dtype, val, digit)){
						JSONObject o = mu.getDbMessageObjLen(dtype, new String[]{});
						this.setCsvshnErrinfo(o, errTbl, colinf, val);
						msg.add(o);
						jo.element(colinf.getId(), "");		// CSVトラン用に空
					}
				}
			}
		}
		// 4.売価コントロールマスタ
		errTbl = RefTable.MSTBAIKACTL;
		for (int i = 0; i < dataArrayTENGP2.size(); i++) {
			JSONObject jo = dataArrayTENGP2.optJSONObject(i);
			for(MSTBAIKACTLLayout colinf: MSTBAIKACTLLayout.values()){
				if(ArrayUtils.contains(notTaretCol, colinf.getCol())){ continue;}				// チェック除外項目
				String val = StringUtils.trim(jo.optString(colinf.getId()));
				if(StringUtils.isNotEmpty(val)){
					DataType dtype = null;
					int[] digit = null;
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
						JSONObject o = mu.getDbMessageObjDataTypeErr(dtype, new String[]{});
						this.setCsvshnErrinfo(o, errTbl, colinf, val);
						msg.add(o);
						if(!colinf.isText()){
							jo.element(colinf.getId(), "");		// CSVトラン用に空
						}
					}
					// ②データ桁チェック
					if(!InputChecker.checkDataLen(dtype, val, digit)){
						JSONObject o = mu.getDbMessageObjLen(dtype, new String[]{});
						this.setCsvshnErrinfo(o, errTbl, colinf, val);
						msg.add(o);
						jo.element(colinf.getId(), "");		// CSVトラン用に空
					}
				}
			}
		}
		// 5.品揃グループマスタ
		errTbl=RefTable.MSTSHINAGP;
		for (int i = 0; i < dataArrayTENGP3.size(); i++) {
			JSONObject jo = dataArrayTENGP3.optJSONObject(i);
			for(MSTSHINAGPLayout colinf: MSTSHINAGPLayout.values()){
				if(ArrayUtils.contains(notTaretCol, colinf.getCol())){ continue;}				// チェック除外項目
				String val = StringUtils.trim(jo.optString(colinf.getId()));
				if(StringUtils.isNotEmpty(val)){
					DataType dtype = null;
					int[] digit = null;
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
						JSONObject o = mu.getDbMessageObjDataTypeErr(dtype, new String[]{});
						this.setCsvshnErrinfo(o, errTbl, colinf, val);
						msg.add(o);
						if(!colinf.isText()){
							jo.element(colinf.getId(), "");		// CSVトラン用に空
						}
					}
					// ②データ桁チェック
					if(!InputChecker.checkDataLen(dtype, val, digit)){
						JSONObject o = mu.getDbMessageObjLen(dtype, new String[]{});
						this.setCsvshnErrinfo(o, errTbl, colinf, val);
						msg.add(o);
						jo.element(colinf.getId(), "");		// CSVトラン用に空
					}
				}
			}
		}
		// 6.添加物
		errTbl=RefTable.MSTTENKABUTSU;
		for (int i = 0; i < dataArrayTENKABUTSU.size(); i++) {
			JSONObject jo = dataArrayTENKABUTSU.optJSONObject(i);
			for(MSTTENKABUTSULayout colinf: MSTTENKABUTSULayout.values()){
				if(ArrayUtils.contains(notTaretCol, colinf.getCol())){ continue;}				// チェック除外項目
				String val = StringUtils.trim(jo.optString(colinf.getId()));
				if(StringUtils.isNotEmpty(val)){
					DataType dtype = null;
					int[] digit = null;
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
						JSONObject o = mu.getDbMessageObjDataTypeErr(dtype, new String[]{});
						this.setCsvshnErrinfo(o, errTbl, colinf, val);
						msg.add(o);
						if(!colinf.isText()){
							jo.element(colinf.getId(), "");		// CSVトラン用に空
						}
					}
					// ②データ桁チェック
					if(!InputChecker.checkDataLen(dtype, val, digit)){
						JSONObject o = mu.getDbMessageObjLen(dtype, new String[]{});
						this.setCsvshnErrinfo(o, errTbl, colinf, val);
						msg.add(o);
						jo.element(colinf.getId(), "");		// CSVトラン用に空
					}
				}
			}
		}
		// 7.グループ名
		errTbl=RefTable.MSTGRP;
		for (int i = 0; i < dataArrayGROUP.size(); i++) {
			JSONObject jo = dataArrayGROUP.optJSONObject(i);
			for(MSTGRPLayout colinf: MSTGRPLayout.values()){
				if(ArrayUtils.contains(notTaretCol, colinf.getCol())){ continue;}				// チェック除外項目
				String val = StringUtils.trim(jo.optString(colinf.getId()));
				if(StringUtils.isNotEmpty(val)){
					DataType dtype = null;
					int[] digit = null;
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
						JSONObject o = mu.getDbMessageObjDataTypeErr(dtype, new String[]{});
						this.setCsvshnErrinfo(o, errTbl, colinf, val);
						msg.add(o);
						if(!colinf.isText()){
							jo.element(colinf.getId(), "");		// CSVトラン用に空
						}
					}
					// ②データ桁チェック
					if(!InputChecker.checkDataLen(dtype, val, digit)){
						JSONObject o = mu.getDbMessageObjLen(dtype, new String[]{});
						this.setCsvshnErrinfo(o, errTbl, colinf, val);
						msg.add(o);
						jo.element(colinf.getId(), "");		// CSVトラン用に空
					}
				}
			}
		}
		// 8.自動発注区分
		errTbl=RefTable.MSTAHS;
		for (int i = 0; i < dataArrayAHS.size(); i++) {
			JSONObject jo = dataArrayAHS.optJSONObject(i);
			for(MSTAHSLayout colinf: MSTAHSLayout.values()){
				if(ArrayUtils.contains(notTaretCol, colinf.getCol())){ continue;}				// チェック除外項目
				String val = StringUtils.trim(jo.optString(colinf.getId()));
				if(StringUtils.isNotEmpty(val)){
					DataType dtype = null;
					int[] digit = null;
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
						JSONObject o = mu.getDbMessageObjDataTypeErr(dtype, new String[]{});
						this.setCsvshnErrinfo(o, errTbl, colinf, val);
						msg.add(o);
						if(!colinf.isText()){
							jo.element(colinf.getId(), "");		// CSVトラン用に空
						}
					}
					// ②データ桁チェック
					if(!InputChecker.checkDataLen(dtype, val, digit)){
						JSONObject o = mu.getDbMessageObjLen(dtype, new String[]{});
						this.setCsvshnErrinfo(o, errTbl, colinf, val);
						msg.add(o);
						jo.element(colinf.getId(), "");		// CSVトラン用に空
					}
				}
			}
		}
		// 9.店別異部門
		errTbl=RefTable.MSTSHNTENBMN;
		for (int i = 0; i < dataArrayTENGP4.size(); i++) {
			JSONObject jo = dataArrayTENGP4.optJSONObject(i);
			for(MSTSHNTENBMNLayout colinf: MSTSHNTENBMNLayout.values()){
				if(ArrayUtils.contains(notTaretCol, colinf.getCol())){ continue;}				// チェック除外項目
				String val = StringUtils.trim(jo.optString(colinf.getId()));
				if(StringUtils.isNotEmpty(val)){
					DataType dtype = null;
					int[] digit = null;
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
						JSONObject o = mu.getDbMessageObjDataTypeErr(dtype, new String[]{});
						this.setCsvshnErrinfo(o, errTbl, colinf, val);
						msg.add(o);
						if(!colinf.isText()){
							jo.element(colinf.getId(), "");		// CSVトラン用に空
						}
					}
					// ②データ桁チェック
					if(!InputChecker.checkDataLen(dtype, val, digit)){
						JSONObject o = mu.getDbMessageObjLen(dtype, new String[]{});
						this.setCsvshnErrinfo(o, errTbl, colinf, val);
						msg.add(o);
						jo.element(colinf.getId(), "");		// CSVトラン用に空
					}
				}
			}
		}
		if(msg.size() > 0){
			return msg;
		}

		// 新規(正) 1.1　必須入力項目チェックを行う。
		// 変更(正) 1.1　必須入力項目チェックを行う。
		errTbl = RefTable.MSTSHN;
		MSTSHNLayout[] targetCol = null;
		if(isNew){
			targetCol = new MSTSHNLayout[]{MSTSHNLayout.SHNAN,MSTSHNLayout.PCARD_SHUKBN,MSTSHNLayout.PCARD_IROKBN,MSTSHNLayout.BMNCD
					,MSTSHNLayout.TEIKEIKBN,MSTSHNLayout.TEISHIKBN,MSTSHNLayout.SHNKBN,MSTSHNLayout.PCKBN,MSTSHNLayout.TEIKANKBN,MSTSHNLayout.KAKOKBN,MSTSHNLayout.TANAOROKBN,MSTSHNLayout.ZEIKBN,MSTSHNLayout.SIMEKAISU,MSTSHNLayout.BINKBN,MSTSHNLayout.RG_IDENFLG};
		}else if(isChange){
			targetCol = new MSTSHNLayout[]{MSTSHNLayout.SHNCD,MSTSHNLayout.SHNAN,MSTSHNLayout.PCARD_SHUKBN,MSTSHNLayout.PCARD_IROKBN,MSTSHNLayout.BMNCD
					,MSTSHNLayout.TEIKEIKBN,MSTSHNLayout.TEISHIKBN,MSTSHNLayout.SHNKBN,MSTSHNLayout.PCKBN,MSTSHNLayout.TEIKANKBN,MSTSHNLayout.KAKOKBN,MSTSHNLayout.TANAOROKBN,MSTSHNLayout.ZEIKBN,MSTSHNLayout.SIMEKAISU,MSTSHNLayout.BINKBN,MSTSHNLayout.RG_IDENFLG};
		}else{
			targetCol = new MSTSHNLayout[]{MSTSHNLayout.SHNCD,MSTSHNLayout.SHNAN,MSTSHNLayout.PCARD_SHUKBN,MSTSHNLayout.PCARD_IROKBN,MSTSHNLayout.BMNCD
					,MSTSHNLayout.TEISHIKBN,MSTSHNLayout.SHNKBN,MSTSHNLayout.PCKBN,MSTSHNLayout.TEIKANKBN,MSTSHNLayout.KAKOKBN,MSTSHNLayout.TANAOROKBN,MSTSHNLayout.ZEIKBN,MSTSHNLayout.SIMEKAISU,MSTSHNLayout.BINKBN,MSTSHNLayout.RG_IDENFLG};
		}
		for(MSTSHNLayout colinf: targetCol){
			if(StringUtils.isEmpty(data.optString(colinf.getId()))){
				JSONObject o = mu.getDbMessageObj("E00001", new String[]{});
				this.setCsvshnErrinfo(o, errTbl, colinf, data.optString(colinf.getId()));
				msg.add(o);
				return msg;
			}
		}

		// リードタイムパターン必須入力チェック
		String txt_readtmptn = data.optString(MSTSHNLayout.READTMPTN.getId());

		List<Integer> bmnlist = Arrays.asList(1, 3, 7, 8, 12, 14, 42, 44, 46, 47, 54);	// リードタイムパターンが省略可能な部門リスト
		if(StringUtils.isEmpty(txt_readtmptn)){
			if(StringUtils.isNumeric(txt_bmncd)){
				if(bmnlist.indexOf(Integer.parseInt(txt_bmncd)) == -1){
					JSONObject o = mu.getDbMessageObj("E00001", new String[]{});
					this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.READTMPTN, txt_readtmptn);
					msg.add(o);
					return msg;
				}
			}
		}

		// CSV修正(予約) 1.2　商品_予約ﾃｰﾌﾞﾙに同じ商品ｺｰﾄﾞで異なるﾏｽﾀ変更予定日、店売価実施日の組み合わせを持つﾚｺｰﾄﾞがあれば、ｴﾗｰ。
		errTbl = RefTable.CSVSHN;

		if(!isNew){
			if(!this.checkMstExist(DefineReport.InpText.SHNCD.getObj(), txt_shncd)){
				JSONObject o = mu.getDbMessageObj("E20124", new String[]{});
				this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.SHNCD, txt_shncd);
				msg.add(o);
				return msg;
			}
		}

		String txt_ssircd = data.optString(MSTSHNLayout.SSIRCD.getId());
		String kbn105 = data.optString(MSTSHNLayout.SHNKBN.getId());		// 商品種類

		// 新規(正) 1.4　入力内容相関チェックを行う（入出力データ仕様のチェック内容を参照）。
		// 変更(正) 1.2　画面各項目間の入力内容相関チェックを行う（入出力データ仕様のチェック内容を参照）。



		// 標準-部門コード
		// ①部門マスタに無い場合エラー
		if(!this.checkMstExist(DefineReport.InpText.BMNCD.getObj(), txt_bmncd)){
			JSONObject o = mu.getDbMessageObj("E11044", new String[]{});
			this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.BMNCD, data);
			msg.add(o);
			return msg;
		}
		String txt_daicd = data.optString(MSTSHNLayout.DAICD.getId());
		String txt_chucd = data.optString(MSTSHNLayout.CHUCD.getId());
		String txt_shocd = data.optString(MSTSHNLayout.SHOCD.getId());
		String txt_sshocd= "";
		// 標準-分類：分類マスタに無い場合エラー
		if(msg.size() == 0){
			JSONObject o = checkMstbmnExist(mu, errTbl, txt_bmncd, txt_daicd, txt_chucd, txt_shocd, txt_sshocd, "");
			if(!o.isEmpty()){
				msg.add(o);
				return msg;
			}
		}
		String txt_bmncd_y = data.optString(MSTSHNLayout.YOT_BMNCD.getId());
		String txt_daicd_y = data.optString(MSTSHNLayout.YOT_DAICD.getId());
		String txt_chucd_y = data.optString(MSTSHNLayout.YOT_CHUCD.getId());
		String txt_shocd_y = data.optString(MSTSHNLayout.YOT_SHOCD.getId());
		// 用途-分類：分類マスタに無い場合エラー
		if(msg.size() == 0){
			JSONObject o = checkMstbmnExist(mu, errTbl, txt_bmncd_y, txt_daicd_y, txt_chucd_y, txt_shocd_y, "", "_YOT");
			if(!o.isEmpty()){
				msg.add(o);
				return msg;
			}
		}
		String txt_bmncd_u = data.optString(MSTSHNLayout.URI_BMNCD.getId());
		String txt_daicd_u = data.optString(MSTSHNLayout.URI_DAICD.getId());
		String txt_chucd_u = data.optString(MSTSHNLayout.URI_CHUCD.getId());
		String txt_shocd_u = data.optString(MSTSHNLayout.URI_SHOCD.getId());
		// 用途-分類：分類マスタに無い場合エラー
		if(msg.size() == 0){
			JSONObject o = checkMstbmnExist(mu, errTbl, txt_bmncd_u, txt_daicd_u, txt_chucd_u, txt_shocd_u, "", "_URI");
			if(!o.isEmpty()){
				msg.add(o);
				return msg;
			}
		}

		// ユニットプライス:
		String txt_up_yoryosu = data.optString(MSTSHNLayout.UP_YORYOSU.getId());
		String txt_up_tyoryosu = data.optString(MSTSHNLayout.UP_TYORYOSU.getId());
		String kbn113 = data.optString(MSTSHNLayout.UP_TANIKBN.getId());		// ユニット単位
		boolean isAllInput = (!this.isEmptyVal(txt_up_yoryosu, true)&&!this.isEmptyVal(txt_up_tyoryosu, true)&&!this.isEmptyVal(kbn113, false));
		boolean isAllEmpty = (this.isEmptyVal(txt_up_yoryosu, true)&&this.isEmptyVal(txt_up_tyoryosu, true)&&this.isEmptyVal(kbn113, false));
		if(!isAllInput && !isAllEmpty){
			JSONObject o = mu.getDbMessageObj("E11105", new String[]{});
			this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.UP_YORYOSU, data);
			msg.add(o);
			return msg;
		}

		// ソースコード系
		// ソースコード取得
		ArrayList<JSONObject> srccds= new ArrayList<JSONObject>();
		HashSet<String> srccds_ = new HashSet<String>();
		for (int i=0; i<dataArraySRCCD.size(); i++){
			String val = dataArraySRCCD.optJSONObject(i).optString(MSTSRCCDLayout.SRCCD.getId());
			if(StringUtils.isNotEmpty(val)){
				srccds.add(dataArraySRCCD.optJSONObject(i));
				srccds_.add(val);
			}
		}
		// ①NON-PLUの場合にソースコードに入力がある場合はエラー。
		// ②商品種類6または部門コード02,09,15,04,05,06,20,23,43,08,12,13,26,27の場合にNON-PLUにして良い、それ以外は1を入力してはならない。→削除：共通の商品種類チェックを優先
		String kbn117 = data.optString(MSTSHNLayout.TEIKEIKBN.getId());		// 定計区分
		if(DefineReport.ValKbn117.VAL1.getVal().equals(kbn117)){
			if(srccds.size() > 0){
				JSONObject o = mu.getDbMessageObj("E11106", new String[]{});
				this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.TEIKEIKBN, data);
				msg.add(o);
				return msg;
			}
		}
		errTbl = RefTable.MSTSRCCD;
		// 重複チェック
		if(srccds.size() != srccds_.size()){
			JSONObject o = mu.getDbMessageObj("E11109", new String[]{});
			this.setCsvshnErrinfo(o, errTbl, MSTSRCCDLayout.SRCCD, "");
			msg.add(o);
			return msg;
		}

		String[] allseqnos = new String[]{};
		for(ValSrccdSeqno val : DefineReport.ValSrccdSeqno.values()){
			allseqnos = (String[]) ArrayUtils.add(allseqnos, val.getVal());
		}
		String[] seqnos = new String[]{};
		// ソースコード１、２取得
		JSONObject srccdrow1 = null;
		JSONObject srccdrow2 = null;
		for (int i=0; i<dataArraySRCCD.size(); i++){
			String txt_srccd = dataArraySRCCD.optJSONObject(i).optString(MSTSRCCDLayout.SRCCD.getId());
			String sourcekbn = dataArraySRCCD.optJSONObject(i).optString(MSTSRCCDLayout.SOURCEKBN.getId());
			String seqno = dataArraySRCCD.optJSONObject(i).optString(MSTSRCCDLayout.SEQNO.getId());

			if(this.isEmptyVal(sourcekbn, false)){
				JSONObject o = mu.getDbMessageObj("EX1047", new String[]{"ソース区分"});
				this.setCsvshnErrinfo(o, errTbl, MSTSRCCDLayout.SOURCEKBN, dataArraySRCCD.optJSONObject(i));
				msg.add(o);
				return msg;
			}
			// SEQNOチェック
			if(!ArrayUtils.contains(allseqnos, seqno)){
				JSONObject o = mu.getDbMessageObj("EX1051", new String[]{});
				this.setCsvshnErrinfo(o, errTbl, MSTSRCCDLayout.SEQNO, dataArraySRCCD.optJSONObject(i));
				msg.add(o);
				return msg;
			}
			if(ArrayUtils.contains(new String[]{DefineReport.ValSrccdSeqno.SRC1.getVal(),DefineReport.ValSrccdSeqno.SRC2.getVal()}, seqno)
				&& ArrayUtils.contains(seqnos, seqno)){
				JSONObject o = mu.getDbMessageObj("EX1051", new String[]{});
				this.setCsvshnErrinfo(o, errTbl, MSTSRCCDLayout.SEQNO, dataArraySRCCD.optJSONObject(i));
				msg.add(o);
				return msg;
			}

			// 有効期間チェック
			String txt_yuko_stdt = dataArraySRCCD.optJSONObject(i).optString(MSTSRCCDLayout.YUKO_STDT.getId());
			String txt_yuko_eddt = dataArraySRCCD.optJSONObject(i).optString(MSTSRCCDLayout.YUKO_EDDT.getId());
			if(DefineReport.ValSrccdSeqno.KARI.getVal().equals(seqno)){		// 	一時登録
				if(this.isEmptyVal(txt_yuko_stdt, true)){
					JSONObject o = mu.getDbMessageObj("EX1050", new String[]{});
					this.setCsvshnErrinfo(o, errTbl, MSTSRCCDLayout.YUKO_STDT, dataArraySRCCD.optJSONObject(i));
					msg.add(o);
					return msg;
				}
			}else{													// 	一般
				if(this.isEmptyVal(txt_yuko_stdt, true)){
					JSONObject o = mu.getDbMessageObj("EX1049", new String[]{});
					this.setCsvshnErrinfo(o, errTbl, MSTSRCCDLayout.YUKO_STDT, dataArraySRCCD.optJSONObject(i));
					msg.add(o);
					return msg;
				}
				if(this.isEmptyVal(txt_yuko_eddt, true)){
					JSONObject o = mu.getDbMessageObj("EX1049", new String[]{});
					this.setCsvshnErrinfo(o, errTbl, MSTSRCCDLayout.YUKO_EDDT, dataArraySRCCD.optJSONObject(i));
					msg.add(o);
					return msg;
				}
			}
			// 日付妥当性
			if(!this.isEmptyVal(txt_yuko_stdt, true)&&!this.isEmptyVal(txt_yuko_eddt, true)&& !InputChecker.isFromToNumeric(txt_yuko_stdt, txt_yuko_eddt)){
				JSONObject o = mu.getDbMessageObj("E11020", new String[]{});
				this.setCsvshnErrinfo(o, errTbl, MSTSRCCDLayout.YUKO_EDDT, dataArraySRCCD.optJSONObject(i));
				msg.add(o);
				return msg;
			}

			// ソース1,2整合性
			if(StringUtils.equals(seqno, DefineReport.ValSrccdSeqno.SRC1.getVal())){ srccdrow1 = dataArraySRCCD.optJSONObject(i); }
			if(StringUtils.equals(seqno, DefineReport.ValSrccdSeqno.SRC2.getVal())){ srccdrow2 = dataArraySRCCD.optJSONObject(i); }
			if(srccdrow1!=null && srccdrow2!=null){
				// ①ソース区分2(2行目)が1(JAN13) or 2(JAN8)の場合、ソース区分1(1行目)が3(EAN13), 4(EAN8), 5(UPC-A), 6(UPC-E)はエラー
				String kbn1 = srccdrow1.optString(MSTSRCCDLayout.SOURCEKBN.getId()).split("-")[0];
				String kbn2 = srccdrow2.optString(MSTSRCCDLayout.SOURCEKBN.getId()).split("-")[0];
				if(ArrayUtils.contains(new String[]{"1", "2"}, kbn2)
				 &&ArrayUtils.contains(new String[]{"3", "4", "5", "6"}, kbn1)){
					JSONObject o = mu.getDbMessageObj("E11111", new String[]{});
					this.setCsvshnErrinfo(o, errTbl, MSTSRCCDLayout.SOURCEKBN, "");
					msg.add(o);
					return msg;
				}
			}
			// コード整合性チェック：チェックデジット算出コード取得
			// ソースコードに問題がある場合は、エラー情報が返ってくる（E11165,E11167,E11168,E11169,E11171,E11172,E11224）
			JSONObject result = NumberingUtility.calcCheckdigitSRCCD(userInfo, txt_srccd, sourcekbn);
			if(StringUtils.isNotEmpty(result.optString(mu.ID))){
				JSONObject o = mu.getDbMessageObj(result.optString(mu.ID), new String[]{});
				this.setCsvshnErrinfo(o, errTbl, MSTSRCCDLayout.SRCCD, dataArraySRCCD.optJSONObject(i));
				msg.add(o);
				return msg;
			}
			if(ArrayUtils.contains(new String[]{DefineReport.ValSrccdSeqno.SRC1.getVal(),DefineReport.ValSrccdSeqno.SRC2.getVal()}, seqno)){
				seqnos = (String[]) ArrayUtils.add(seqnos, seqno);
			}
		}
		// ソース登録があるにもかかわらず1指定がない場合エラーとする
		if(srccds.size() > 0 && !ArrayUtils.contains(seqnos, DefineReport.ValSrccdSeqno.SRC1.getVal())){
			JSONObject o = mu.getDbMessageObj("E11110", new String[]{});
			this.setCsvshnErrinfo(o, errTbl, MSTSRCCDLayout.SEQNO, "");
			msg.add(o);
			return msg;
		}


		// 店別異部門
		errTbl=RefTable.MSTSHNTENBMN;
		ArrayList<JSONObject> tengp4s= new ArrayList<JSONObject>();
		TreeSet<String> tengp4s_ = new TreeSet<String>();
		TreeSet<String> tengp4keys= new TreeSet<String>();
		String[] tenshncds = new String[]{};
		String areakbn4 = "";
		for (int i=0; i<dataArrayTENGP4.size(); i++){
			String tengpcd = dataArrayTENGP4.optJSONObject(i).optString(MSTSHNTENBMNLayout.TENGPCD.getId());
			String tenshncd = dataArrayTENGP4.optJSONObject(i).optString(MSTSHNTENBMNLayout.TENSHNCD.getId());
			if(StringUtils.isNotEmpty(tengpcd) || StringUtils.isNotEmpty(tenshncd)){
				// 店グループに入力がある場合、商品コードは必須入力。
				if(StringUtils.isEmpty(tengpcd) || StringUtils.isEmpty(tenshncd)){
					JSONObject o = mu.getDbMessageObj("EX1047", new String[]{"商品コード、店グループ"});
					this.setCsvshnErrinfo(o, errTbl, MSTSHNTENBMNLayout.TENSHNCD, "");
					msg.add(o);
					return msg;
				}

				tengp4s.add(dataArrayTENGP4.optJSONObject(i));
				tengp4s_.add(tengpcd);
				tengp4keys.add(tengpcd+"-"+tenshncd);
				areakbn4 = dataArrayTENGP4.optJSONObject(i).optString(MSTSHNTENBMNLayout.AREAKBN.getId());
				tenshncds = (String[]) ArrayUtils.add(tenshncds, tenshncd);
			}
		}
		// 商品マスタに存在しない場合、エラー。
		if(tenshncds.length > 0){
			if(!this.checkMstExist(DefineReport.InpText.TENSHNCD.getObj(), StringUtils.join(tenshncds, ","))){
				JSONObject o = mu.getDbMessageObj("E11098", new String[]{});
				this.setCsvshnErrinfo(o, errTbl, MSTSHNTENBMNLayout.TENSHNCD, "");
				msg.add(o);
				return msg;
			}
		}
		// 商品店グループに存在しないコードはエラー
		String[] errtengps4 = this.checkMsttgpExist(tengp4s_, DefineReport.ValGpkbn.BAIKA.getVal(), txt_bmncd ,areakbn4);
		if(errtengps4.length > 0){
			JSONObject o = mu.getDbMessageObj("E11140", new String[]{});
			this.setCsvshnErrinfo(o, errTbl, MSTSHNTENBMNLayout.TENGPCD, StringUtils.join(errtengps4, ","));
			msg.add(o);
			return msg;
		}
		for (int i = 0; i < dataArrayTENGP4.size(); i++) {
			// 商品コードが主の商品コードと同じ場合エラー
			if(StringUtils.equals(txt_shncd, dataArrayTENGP4.optJSONObject(i).optString(MSTSHNTENBMNLayout.TENSHNCD.getId()))){
				JSONObject o = mu.getDbMessageObj("EX1047", new String[]{"基本情報と異なる商品コード"});
				this.setCsvshnErrinfo(o, errTbl, MSTSHNTENBMNLayout.TENSHNCD, dataArrayTENGP4.optJSONObject(i));
				msg.add(o);
				return msg;
			}
			// ソースコードが設定しているソースコードの中に無い場合エラー
			String srccd = dataArrayTENGP4.optJSONObject(i).optString(MSTSHNTENBMNLayout.SRCCD.getId());
			if(!srccds_.contains(srccd)){
				JSONObject o = mu.getDbMessageObj("EX1047", new String[]{"ソースコードにあるJANコード"});
				this.setCsvshnErrinfo(o, errTbl, MSTSHNTENBMNLayout.SRCCD, dataArrayTENGP4.optJSONObject(i));
				msg.add(o);
				return msg;
			}
		}
		// 画面に同じ重複がある場合、エラー。
		if(tengp4s.size() != tengp4keys.size()){
			JSONObject o = mu.getDbMessageObj("E11112", new String[]{});
			this.setCsvshnErrinfo(o, errTbl, MSTSHNTENBMNLayout.TENGPCD, "");
			msg.add(o);
			return msg;
		}
		// 店グループを選択したら10番以上で登録しなければならない.
		if(DefineReport.ValKbn135.VAL1.getVal().equals(areakbn4)){	// 店グループ
			if(tengp4s_.size() > 0 && NumberUtils.toInt(tengp4s_.first()) < 10){
				JSONObject o = mu.getDbMessageObj("E11038", new String[]{});
				this.setCsvshnErrinfo(o, errTbl, MSTSHNTENBMNLayout.TENGPCD, tengp4s_.first());
				msg.add(o);
				return msg;
			}
		}

		// 品揃えグループ
		errTbl = RefTable.MSTSHINAGP;
		ArrayList<JSONObject> tengp3s= new ArrayList<JSONObject>();
		TreeSet<String> tengp3s_ = new TreeSet<String>();
		String areakbn3 = "";
		for (int i=0; i<dataArrayTENGP3.size(); i++){
			String val = dataArrayTENGP3.optJSONObject(i).optString(MSTSHINAGPLayout.TENGPCD.getId());
			if(StringUtils.isNotEmpty(val)){
				tengp3s.add(dataArrayTENGP3.optJSONObject(i));
				tengp3s_.add(val);
				areakbn3 = dataArrayTENGP3.optJSONObject(i).optString(MSTSHINAGPLayout.AREAKBN.getId());
			}
		}
		// 店グループ:商品店グループに存在しないコードはエラー。
		// 商品店グループに存在しないコードはエラー
		String[] errtengps = this.checkMsttgpExist(tengp3s_, DefineReport.ValGpkbn.SHINA.getVal(), txt_bmncd ,areakbn3);
		if(errtengps.length > 0){
			JSONObject o = mu.getDbMessageObj("E11140", new String[]{});
			this.setCsvshnErrinfo(o, errTbl, MSTSHINAGPLayout.TENGPCD, StringUtils.join(errtengps, ","));
			msg.add(o);
			return msg;
		}
		// 扱い区分:店グループに入力がある場合、扱い区分未入力はエラー
		for (int i=0; i<tengp3s.size(); i++){
			// 店グループに入力がある場合、扱い区分未入力はエラー
			if(StringUtils.isEmpty(tengp3s.get(i).optString(MSTSHINAGPLayout.ATSUKKBN.getId()))){
				JSONObject o = mu.getDbMessageObj("EX1001", new String[]{});
				this.setCsvshnErrinfo(o, errTbl, MSTSHINAGPLayout.TENGPCD, dataArrayTENGP3.optJSONObject(i));
				msg.add(o);
				return msg;
			}
		}
		// 画面に同じ店グループがある場合、エラー。
		if(tengp3s.size() != tengp3s_.size()){
			JSONObject o = mu.getDbMessageObj("E11112", new String[]{});
			this.setCsvshnErrinfo(o, errTbl, MSTSHINAGPLayout.TENGPCD, "");
			msg.add(o);
			return msg;
		}
		// 店グループを選択したら10番以上で登録しなければならない.
		if(DefineReport.ValKbn135.VAL1.getVal().equals(areakbn3)){	// 店グループ
			if(tengp3s_.size() > 0 && NumberUtils.toInt(tengp3s_.first()) < 10){
				JSONObject o = mu.getDbMessageObj("E11038", new String[]{});
				this.setCsvshnErrinfo(o, errTbl, MSTSHINAGPLayout.TENGPCD, tengp3s_.first());
				msg.add(o);
				return msg;
			}
		}

		errTbl = RefTable.MSTSHN;
		// 取扱期間
		//①取扱開始日0000/00/00　取扱終了日YYYY/MM/DD　NG
		//②取扱開始日YYYY/MM/DD　取扱終了日0000/00/00　NG
		//③取扱開始日YYYY/MM/DD >=　取扱終了日YYYY/MM/DD　NG"
		String txt_atsuk_stdt = data.optString(MSTSHNLayout.ATSUK_STDT.getId());
		String txt_atsuk_eddt = data.optString(MSTSHNLayout.ATSUK_EDDT.getId());
		isAllInput = (!this.isEmptyVal(txt_atsuk_stdt, true)&&!this.isEmptyVal(txt_atsuk_eddt, true));
		isAllEmpty = (this.isEmptyVal(txt_atsuk_stdt, true)&&this.isEmptyVal(txt_atsuk_eddt, true));
		if(!isAllInput&&!isAllEmpty){
			JSONObject o = mu.getDbMessageObj("E11114", new String[]{});
			this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.ATSUK_STDT, data);
			msg.add(o);
			return msg;
		}
		if(isAllInput && !InputChecker.isFromToDate(txt_atsuk_stdt, txt_atsuk_eddt)){
			JSONObject o = mu.getDbMessageObj("E11115", new String[]{});
			this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.ATSUK_STDT, data);
			msg.add(o);
			return msg;
		}


		// 商品種類に基づくチェック
		//  部門:選択可部門
		if(StringUtils.equals(kbn105, DefineReport.ValKbn105.VAL2.getVal())){
			//部門:選択可部門
			//02,09,15,04,05,06,20,23,43部門
			if(!ArrayUtils.contains(new Integer[]{2,9,15,4,5,6,20,23,43}, NumberUtils.toInt(txt_bmncd))){
				JSONObject o = mu.getDbMessageObj("E11143", new String[]{});
				this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.SHNKBN, data);
				msg.add(o);
				return msg;
			}
		}else if(StringUtils.equals(kbn105, DefineReport.ValKbn105.VAL3.getVal())){
			//部門:選択可部門
			//88部門
			if(!ArrayUtils.contains(new Integer[]{88}, NumberUtils.toInt(txt_bmncd))){
				JSONObject o = mu.getDbMessageObj("E11143", new String[]{});
				this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.SHNKBN, data);
				msg.add(o);
				return msg;
			}
		}else if(NumberUtils.toInt(txt_bmncd)==88){
			JSONObject o = mu.getDbMessageObj("E11143", new String[]{});
			this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.SHNKBN, data);
			msg.add(o);
			return msg;
		}

		// 原売価値入ﾁｪｯｸ
		//  ﾚｷﾞｭﾗｰ原売価:値入ﾁｪｯｸ
		String chk_rg_atsukflg= data.optString(MSTSHNLayout.RG_ATSUKFLG.getId());
		String txt_rg_genkaam = data.optString(MSTSHNLayout.RG_GENKAAM.getId());
		String txt_rg_baikaam = data.optString(MSTSHNLayout.RG_BAIKAAM.getId());
		if(ArrayUtils.contains(new String[]{"0", "1"}, kbn105)&& this.calcNeireRit(txt_rg_genkaam, txt_rg_baikaam) >= 98d){
			JSONObject o = mu.getDbMessageObj("E11144", new String[]{});
			this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.RG_GENKAAM, data);
			msg.add(o);
			return msg;
		}
		//  販促原売価:値入ﾁｪｯｸ
		String chk_hs_atsukflg= data.optString(MSTSHNLayout.HS_ATSUKFLG.getId());
		String txt_hs_genkaam = data.optString(MSTSHNLayout.HS_GENKAAM.getId());
		String txt_hs_baikaam = data.optString(MSTSHNLayout.HS_BAIKAAM.getId());
		if(ArrayUtils.contains(new String[]{"0", "1"}, kbn105)&& this.calcNeireRit(txt_hs_genkaam, txt_hs_baikaam) >= 98d){
			JSONObject o = mu.getDbMessageObj("E11144", new String[]{});
			this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.HS_GENKAAM, data);
			msg.add(o);
			return msg;
		}
		//  原売価0チェック:扱い時原価=0and売価=0可（片方だけ0の場合はエラー）
		//  ﾚｷﾞｭﾗｰ原売価:扱い時原価=0and売価=0可（片方だけ0の場合はエラー）
		if(!this.isAbleGenbaika(chk_rg_atsukflg, kbn105, txt_rg_genkaam, txt_rg_baikaam)){
			JSONObject o = mu.getDbMessageObj("E11146", new String[]{});
			this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.HS_GENKAAM, data);
			msg.add(o);
			return msg;
		}
		//  販促原売価:扱い時原価=0and売価=0可（片方だけ0の場合はエラー）
		if(!this.isAbleGenbaika(chk_hs_atsukflg, kbn105, txt_hs_genkaam, txt_hs_baikaam)){
			JSONObject o = mu.getDbMessageObj("E11147", new String[]{});
			this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.HS_GENKAAM, data);
			msg.add(o);
			return msg;
		}

		//  定計区分:”１”を許可するチェック
		//  種類：0		許可部門 02,09,15,04,05,06,20,23,43,08,12,13,26,27部門
		//  種類：0以外	許可部門 全部門
		if(DefineReport.ValKbn117.VAL1.getVal().equals(kbn117)&& DefineReport.ValKbn105.VAL0.getVal().equals(kbn105)
			&&!ArrayUtils.contains(new Integer[]{2,9,15,4,5,6,20,23,43,8,12,13,26,27}, NumberUtils.toInt(txt_bmncd))){
			JSONObject o = mu.getDbMessageObj("E11148", new String[]{});
			this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.TEIKEIKBN, data);
			msg.add(o);
			return msg;
		}

		//  定貫区分:”０”を許可するチェック
		//  種類：0		許可部門 02,09,15,04,05,06,20,23,43,08,12,13,26,27部門
		//  種類：0以外	許可部門 全部門
		String kbn121 = data.optString(MSTSHNLayout.TEIKANKBN.getId());	// 定貫不定貫区分
		if(DefineReport.ValKbn121.VAL0.getVal().equals(kbn121)&& DefineReport.ValKbn105.VAL0.getVal().equals(kbn105)
			&&!ArrayUtils.contains(new Integer[]{2,9,15,4,5,6,20,23,43,8,12,13,26,27}, NumberUtils.toInt(txt_bmncd))){
			JSONObject o = mu.getDbMessageObj("E11149", new String[]{});
			this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.TEIKANKBN, data);
			msg.add(o);
			return msg;
		}
		//POP名称:省略可不可
		//  種類：0		不可
		//  種類：0以外	可
		if(DefineReport.ValKbn105.VAL0.getVal().equals(kbn105) && StringUtils.isEmpty(data.optString(MSTSHNLayout.POPKN.getId()))){
			JSONObject o = mu.getDbMessageObj("E11150", new String[]{});
			this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.POPKN, data);
			msg.add(o);
			return msg;
		}
		//標準分類（大分類以下）:標準分類省略可不可
		//  種類：465		可
		//  種類：上記以外	不可
		if(!ArrayUtils.contains(new String[]{"4","6","5"}, kbn105) && (this.isEmptyVal(txt_daicd, false)||this.isEmptyVal(txt_chucd, false)||this.isEmptyVal(txt_shocd, false))){
			MSTSHNLayout colinfo = MSTSHNLayout.DAICD;
			if(this.isEmptyVal(txt_chucd, false)){
				colinfo = MSTSHNLayout.CHUCD;
			}else if(this.isEmptyVal(txt_shocd, false)){
				colinfo = MSTSHNLayout.SHOCD;
			}
			JSONObject o = mu.getDbMessageObj("E11151", new String[]{});
			this.setCsvshnErrinfo(o, errTbl, colinfo, "");
			msg.add(o);
			return msg;
		}

		// PC区分
		// ①1を指定して良いのは部門コードが04,05,06,43の場合のみ。
		// ②標準仕入先コードから仕入先マスタのデフォルト加工指示を参照し、'1'の場合、PC区分'1'が可能です。
		String kbn102 = data.optString(MSTSHNLayout.PCKBN.getId());
		if(DefineReport.ValKbn102.VAL1.getVal().equals(kbn102)
			&&!ArrayUtils.contains(new Integer[]{4,5,6,43,20,23}, NumberUtils.toInt(txt_bmncd))){
			JSONObject o = mu.getDbMessageObj("E11116", new String[]{});
			this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.PCKBN, data);
			msg.add(o);
			return msg;
		}
		if(DefineReport.ValKbn102.VAL1.getVal().equals(kbn102)
			&& ArrayUtils.contains(new Integer[]{4,5,6,43}, NumberUtils.toInt(txt_bmncd))){
			JSONArray mstsir_rows = this.getMstData(DefineReport.ID_SQL_SIR_, new ArrayList<String>(Arrays.asList(txt_ssircd)));
			if(mstsir_rows.size()!=1 || (mstsir_rows.size()==1 && !StringUtils.equals(kbn102, mstsir_rows.optJSONObject(0).optString("DF_KAKOSJKBN")))){
				JSONObject o = mu.getDbMessageObj("E11326", new String[]{});
				this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.PCKBN, data);
				msg.add(o);
				return msg;
			}
		}
		// レギュラ
		isAllInput = (!this.isEmptyVal(txt_rg_genkaam, true)&&!this.isEmptyVal(txt_rg_baikaam, true)&&!this.isEmptyVal(data.optString(MSTSHNLayout.RG_IRISU.getId()), true));
		if(DefineReport.Values.ON.getVal().equals(chk_rg_atsukflg) && !isAllInput){
			JSONObject o = mu.getDbMessageObj("E11119", new String[]{});
			this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.RG_ATSUKFLG, data);
			msg.add(o);
			return msg;
		}
		// 販促
		isAllInput = (!this.isEmptyVal(txt_hs_genkaam, true)&&!this.isEmptyVal(txt_hs_baikaam, true)&&!this.isEmptyVal(data.optString(MSTSHNLayout.HS_IRISU.getId()), true));
		if(DefineReport.Values.ON.getVal().equals(chk_hs_atsukflg) && !isAllInput){
			JSONObject o = mu.getDbMessageObj("E11119", new String[]{});
			this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.HS_ATSUKFLG, data);
			msg.add(o);
			return msg;
		}

		// 税区分系
		String kbn120 = data.optString(MSTSHNLayout.ZEIKBN.getId());
		// 税率変更日: 税区分が3の場合、設定不可。　TODO:メッセージは２、３
		if(ArrayUtils.contains(new String[]{DefineReport.ValKbn120.VAL2.getVal(), DefineReport.ValKbn120.VAL3.getVal()}, kbn120) && !this.isEmptyVal(data.optString(MSTSHNLayout.ZEIRTHENKODT.getId()), true)){
			JSONObject o = mu.getDbMessageObj("E11121", new String[]{});
			this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.ZEIRTHENKODT, data);
			msg.add(o);
			return msg;
		}
		// 税率区分: 税区分が0,1の場合、税率区分に登録がないとエラー。税区分が2,3の場合、税率区分は設定不可。
		String sel_zeirtkbn = data.optString(MSTSHNLayout.ZEIRTKBN.getId());
		if( ArrayUtils.contains(new String[]{DefineReport.ValKbn120.VAL0.getVal(), DefineReport.ValKbn120.VAL1.getVal()}, kbn120) && this.isEmptyVal(sel_zeirtkbn, false)){
			JSONObject o = mu.getDbMessageObj("E11152", new String[]{});
			this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.ZEIRTKBN, data);
			msg.add(o);
			return msg;
		}else if(ArrayUtils.contains(new String[]{DefineReport.ValKbn120.VAL2.getVal(), DefineReport.ValKbn120.VAL3.getVal()}, kbn120) && !this.isEmptyVal(sel_zeirtkbn, false)){
			JSONObject o = mu.getDbMessageObj("E11153", new String[]{});
			this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.ZEIRTKBN, data);
			msg.add(o);
			return msg;
		}
		// 旧税率区分: 税区分が3の場合、設定不可。2.非課税は選択不可　TODO:メッセージは税率区分と一緒
		String sel_zeirtkbn_old = data.optString("F68");
		if( ArrayUtils.contains(new String[]{DefineReport.ValKbn120.VAL0.getVal(), DefineReport.ValKbn120.VAL1.getVal()}, kbn120) && this.isEmptyVal(sel_zeirtkbn_old, false)){
			JSONObject o = mu.getDbMessageObj("E11185", new String[]{});
			this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.ZEIRTKBN_OLD, data);
			msg.add(o);
			return msg;
		}else if(ArrayUtils.contains(new String[]{DefineReport.ValKbn120.VAL2.getVal(), DefineReport.ValKbn120.VAL3.getVal()}, kbn120) && !this.isEmptyVal(sel_zeirtkbn_old, false)){
			JSONObject o = mu.getDbMessageObj("E11181", new String[]{});
			this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.ZEIRTKBN_OLD, data);
			msg.add(o);
			return msg;
		}

		// ITFコード
		// コード整合性チェック：チェックデジット算出コード取得
		String txt_itfcd =  data.optString(MSTSHNLayout.ITFCD.getId());
		if(!this.isEmptyVal(txt_itfcd, false)){
			JSONObject result = NumberingUtility.calcCheckdigitITFCD(userInfo, txt_itfcd);
			if(StringUtils.isNotEmpty(result.optString(mu.ID))){
				JSONObject o = mu.getDbMessageObj(result.optString(mu.ID), new String[]{"ITFコード"});
				this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.ITFCD, data);
				msg.add(o);
				return msg;
			}
		}

		// 売価グループ
		// レギュラー取扱フラグのチェックがない場合、この売価コントロール部分は設定不可。
		errTbl = RefTable.MSTBAIKACTL;
		ArrayList<JSONObject> tengp2s= new ArrayList<JSONObject>();
		TreeSet<String> tengp2s_ = new TreeSet<String>();
		String areakbn2 = "";
		for (int i=0; i<dataArrayTENGP2.size(); i++){
			String val = dataArrayTENGP2.optJSONObject(i).optString(MSTBAIKACTLLayout.TENGPCD.getId());
			if(StringUtils.isNotEmpty(val)){

				tengp2s.add(dataArrayTENGP2.optJSONObject(i));
				tengp2s_.add(val);
				areakbn2 = dataArrayTENGP2.optJSONObject(i).optString(MSTBAIKACTLLayout.AREAKBN.getId());
			}
		}

		// 店グループ:商品店グループに存在しないコードはエラー。
		// 商品店グループに存在しないコードはエラー
		errtengps = this.checkMsttgpExist(tengp2s_, DefineReport.ValGpkbn.BAIKA.getVal(), txt_bmncd ,areakbn2);
		if(errtengps.length > 0){
			JSONObject o = mu.getDbMessageObj("E11140", new String[]{});
			this.setCsvshnErrinfo(o, errTbl, MSTBAIKACTLLayout.TENGPCD, StringUtils.join(errtengps, ","));
			msg.add(o);
			return msg;
		}
		for (int i=0; i<tengp2s.size(); i++){
			// 店グループに入力がある場合、原価、売価、店入数のすべてが未入力だとエラー。
			isAllEmpty =  this.isEmptyVal(tengp2s.get(i).optString(MSTBAIKACTLLayout.GENKAAM.getId()), true)
					&&this.isEmptyVal(tengp2s.get(i).optString(MSTBAIKACTLLayout.BAIKAAM.getId()), true)
					&&this.isEmptyVal(tengp2s.get(i).optString(MSTBAIKACTLLayout.IRISU.getId()), true);
			if(isAllEmpty){
				JSONObject o = mu.getDbMessageObj("E11122", new String[]{});
				this.setCsvshnErrinfo(o, errTbl, MSTBAIKACTLLayout.TENGPCD, dataArrayTENGP2.optJSONObject(i));
				msg.add(o);
				return msg;
			}
		}
		// 画面に同じ店グループがある場合、エラー。
		if(tengp2s.size() != tengp2s_.size()){
			JSONObject o = mu.getDbMessageObj("E11112", new String[]{});
			this.setCsvshnErrinfo(o, errTbl, MSTBAIKACTLLayout.TENGPCD, "");
			msg.add(o);
			return msg;
		}
		// 店グループを選択したら10番以上で登録しなければならない.
		if(DefineReport.ValKbn135.VAL1.getVal().equals(areakbn2)){	// 店グループ
			if(tengp2s_.size() > 0 && NumberUtils.toInt(tengp2s_.first()) < 10){
				JSONObject o = mu.getDbMessageObj("E11038", new String[]{});
				this.setCsvshnErrinfo(o, errTbl, MSTBAIKACTLLayout.TENGPCD, tengp2s_.first());
				msg.add(o);
				return msg;
			}
		}

		// 仕入グループ
		errTbl = RefTable.MSTSIRGPSHN;
		ArrayList<JSONObject> tengp1s= new ArrayList<JSONObject>();
		TreeSet<String> tengp1s_	= new TreeSet<String>();
		TreeSet<String> tengp1keys	= new TreeSet<String>();
		ArrayList<String> sircdList	= new ArrayList<String>();
		String areakbn1 = "";
		for (int i=0; i<dataArrayTENGP1.size(); i++){
			String val = dataArrayTENGP1.optJSONObject(i).optString(MSTSIRGPSHNLayout.TENGPCD.getId());
			if(StringUtils.isNotEmpty(val)){
				tengp1s.add(dataArrayTENGP1.optJSONObject(i));
				tengp1s_.add(val);
				tengp1keys.add(dataArrayTENGP1.optJSONObject(i).optString(MSTSIRGPSHNLayout.SIRCD.getId())+"-"+dataArrayTENGP1.optJSONObject(i).optString(MSTSIRGPSHNLayout.HSPTN.getId()));
				areakbn1 = dataArrayTENGP1.optJSONObject(i).optString(MSTSIRGPSHNLayout.AREAKBN.getId());
				sircdList.add(dataArrayTENGP1.optJSONObject(i).optString(MSTSIRGPSHNLayout.SIRCD.getId()));
			}
		}
		// 店グループ:商品店グループに存在しないコードはエラー。
		// 商品店グループに存在しないコードはエラー
		errtengps = this.checkMsttgpExist(tengp1s_, DefineReport.ValGpkbn.SIR.getVal(), txt_bmncd ,areakbn1);
		if(errtengps.length > 0){
			JSONObject o = mu.getDbMessageObj("E11140", new String[]{});
			this.setCsvshnErrinfo(o, errTbl, MSTSIRGPSHNLayout.TENGPCD, StringUtils.join(errtengps, ","));
			msg.add(o);
			return msg;
		}
		// 店グループに入力がある場合、仕入先コード、配送パターンは必須入力。
		for (int i=0; i<tengp1s.size(); i++){
			if(StringUtils.isEmpty(tengp1s.get(i).optString(MSTSIRGPSHNLayout.SIRCD.getId()))
				||StringUtils.isEmpty(tengp1s.get(i).optString(MSTSIRGPSHNLayout.HSPTN.getId()))){
				JSONObject o = mu.getDbMessageObj("E11123", new String[]{});
				this.setCsvshnErrinfo(o, errTbl, MSTSIRGPSHNLayout.TENGPCD, tengp1s.get(i));
				msg.add(o);
				return msg;
			}
		}
		// 画面に同じ店グループがある場合、エラー。
		if(tengp1s.size() != tengp1s_.size()){
			JSONObject o = mu.getDbMessageObj("E11112", new String[]{});
			this.setCsvshnErrinfo(o, errTbl, MSTSIRGPSHNLayout.TENGPCD, "");
			msg.add(o);
			return msg;
		}
		if(tengp1s_.size() > 0){
			// 店グループで展開し、店コードが重複する場合はエラー。
			ArrayList<String> paramData = new ArrayList<String>(Arrays.asList(DefineReport.ValGpkbn.SIR.getVal(), txt_bmncd, areakbn1));
			String rep = "";
			for(String cd : tengp1s_){
				rep += ", ?";
				paramData.add(cd);
			}
			rep = StringUtils.removeStart(rep, ",");
			JSONArray msttngp1_rows = this.getMstData(DefineReport.ID_SQL_TENGP_CHK_TEN_CNT.replace("@", rep), paramData);
			if(msttngp1_rows.size() > 1 && !StringUtils.equals(msttngp1_rows.optJSONObject(0).optString("CNT"), "0")){
				JSONObject o = mu.getDbMessageObj("E11141", new String[]{});
				this.setCsvshnErrinfo(o, errTbl, MSTSIRGPSHNLayout.TENGPCD, "");
				msg.add(o);
				return msg;
			}

			// 仕入先コード:仕入先マスタに存在しない場合エラー
			for (int i=0; i<sircdList.size(); i++){
				String sircd = sircdList.get(i);
				if(!this.checkMstExist(DefineReport.InpText.SIRCD.getObj(), sircd)){
					JSONObject o = mu.getDbMessageObj("E11099", new String[]{});
					this.setCsvshnErrinfo(o, errTbl, MSTSIRGPSHNLayout.SIRCD, "");
					msg.add(o);
					return msg;
				}
			}

			// 仕入先コード、配送パターンで配送パターン仕入先マスタの存在チェック
			if(!this.checkMstExist("MSTHSPTNSIR", StringUtils.join(tengp1keys, ","))){
				JSONObject o = mu.getDbMessageObj("E11142", new String[]{});
				this.setCsvshnErrinfo(o, errTbl, MSTSIRGPSHNLayout.TENGPCD, "");
				msg.add(o);
				return msg;
			}
		}
		// 店グループを選択したら10番以上で登録しなければならない.
		if(DefineReport.ValKbn135.VAL1.getVal().equals(areakbn1)){	// 店グループ
			if(tengp1s_.size() > 0 && NumberUtils.toInt(tengp1s_.first()) < 10){
				JSONObject o = mu.getDbMessageObj("E11038", new String[]{});
				this.setCsvshnErrinfo(o, errTbl, MSTSIRGPSHNLayout.TENGPCD, tengp1s_.first());
				msg.add(o);
				return msg;
			}
		}

		errTbl = RefTable.MSTSHN;
		// 仕入先コード:仕入先マスタに存在しない場合エラー
		if(!this.isEmptyVal(txt_ssircd, false)){
			if(!this.checkMstExist(DefineReport.InpText.SIRCD.getObj(), txt_ssircd)){
				JSONObject o = mu.getDbMessageObj("E11099", new String[]{});
				this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.SSIRCD, data);
				msg.add(o);
				return msg;
			}
		}

		// 標準仕入先・配送パターン：仕入先コード、配送パターンで配送パターン仕入先マスタの存在チェック
		String txt_hsptn = data.optString(MSTSHNLayout.HSPTN.getId());
		if(!this.isEmptyVal(txt_ssircd, false) && !this.isEmptyVal(txt_hsptn, false)){
			if(!this.checkMstExist("MSTHSPTNSIR", txt_ssircd + "-" + txt_hsptn)){
				JSONObject o = mu.getDbMessageObj("E11142", new String[]{});
				this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.SSIRCD, data);
				msg.add(o);
				return msg;
			}
		}

		// リードタイムパターン
		String messageid = this.isAbleReadtmptn(data, txt_bmncd);
		if(messageid != null){
			JSONObject o = mu.getDbMessageObj(messageid, new String[]{});
			this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.READTMPTN, data);
			msg.add(o);
			return msg;
		}

		// 締め回数
		//②2を設定して良いのは11,34部門である。
		if(DefineReport.ValKbn134.VAL2.getVal().equals(data.optString(MSTSHNLayout.SIMEKAISU.getId()))
				&& !ArrayUtils.contains(new Integer[]{11,34}, NumberUtils.toInt(txt_bmncd))){
			JSONObject o = mu.getDbMessageObj("E11125", new String[]{});
			this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.READTMPTN, data);
			msg.add(o);
			return msg;
		}
		// 便
		//②2を設定して良いのは02,09,15,04,05,06,20,23,43,10,11,34部門である。
		if(DefineReport.ValKbn132.VAL2.getVal().equals(data.optString(MSTSHNLayout.BINKBN.getId()))
				&& !ArrayUtils.contains(new Integer[]{2,9,15,4,5,6,20,23,43,10,11,34}, NumberUtils.toInt(txt_bmncd))){
			JSONObject o = mu.getDbMessageObj("E11126", new String[]{});
			this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.BINKBN, data);
			msg.add(o);
			return msg;
		}
		// 一括伝票フラグ
		// ②定貫不定貫区分が0の場合、1.センター経由の一括のみ許可する
		// ③PC区分が1の場合、1.センター経由の一括のみ許可する
		String txt_rg_idenflg = data.optString(MSTSHNLayout.RG_IDENFLG.getId());
		if((DefineReport.ValKbn121.VAL0.getVal().equals(kbn121) && txt_rg_idenflg.length() == 0)
		|| (DefineReport.ValKbn102.VAL1.getVal().equals(kbn102) && txt_rg_idenflg.length() == 0)){
			JSONObject o = mu.getDbMessageObj("E11127", new String[]{});
			this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.RG_IDENFLG, data);
			msg.add(o);
			return msg;
		}
		// 酒級:部門コード03,44以外は選択不可。
		String kbn129 = data.optString(MSTSHNLayout.SHUKYUKBN.getId());
		if(!this.isEmptyVal(kbn129, true) && !ArrayUtils.contains(new Integer[]{3, 44}, NumberUtils.toInt(txt_bmncd))){
			JSONObject o = mu.getDbMessageObj("E11129", new String[]{});
			this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.SHUKYUKBN, data);
			msg.add(o);
			return msg;
		}
		// 度数：酒級に登録がある場合、必ず入力。スペース、0は未入力と処理する。　部門コード03,44以外は選択不可
		String txt_dosu = data.optString(MSTSHNLayout.DOSU.getId());
		if(!this.isEmptyVal(kbn129, true) && this.isEmptyVal(txt_dosu, true)){
			JSONObject o = mu.getDbMessageObj("E11130", new String[]{});
			this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.DOSU, data);
			msg.add(o);
			return msg;
		}
		if(!this.isEmptyVal(txt_dosu, true) && !ArrayUtils.contains(new Integer[]{3, 44}, NumberUtils.toInt(txt_bmncd))){
			JSONObject o = mu.getDbMessageObj("E11131", new String[]{});
			this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.DOSU, data);
			msg.add(o);
			return msg;
		}
		// 入荷期限 0は未入力と処理する。入荷期限≧値引期限。0より大きい。
		// 値引期限 0以上を登録可能とする。
		String txt_ods_nyukasu = data.optString(MSTSHNLayout.ODS_NYUKASU.getId());		// 入荷期限:0=未入力扱い
		String txt_ods_nebikisu= data.optString(MSTSHNLayout.ODS_NEBIKISU.getId());		// 値引期限:0=数値
		isAllEmpty = this.isEmptyVal(txt_ods_nyukasu, true) && this.isEmptyVal(txt_ods_nebikisu, false);
		// 入荷期限≧値引期限かチェック
		if(!isAllEmpty && NumberUtils.toInt(txt_ods_nyukasu, 0) < NumberUtils.toInt(txt_ods_nebikisu, 0)){
			JSONObject o = mu.getDbMessageObj("E11132", new String[]{});
			this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.ODS_NYUKASU, data);
			msg.add(o);
			return msg;
		}

		// 添加物
		errTbl = RefTable.MSTTENKABUTSU;
		ArrayList<JSONObject> tenkabcds1= new ArrayList<JSONObject>(), tenkabcds2= new ArrayList<JSONObject>();
		TreeSet<String> tenkabcds1_ = new TreeSet<String>(), tenkabcds2_ = new TreeSet<String>();
		for (int i=0; i<dataArrayTENKABUTSU.size(); i++){
			String kbn = dataArrayTENKABUTSU.optJSONObject(i).optString(MSTTENKABUTSULayout.TENKABKBN.getId());
			String val = dataArrayTENKABUTSU.optJSONObject(i).optString(MSTTENKABUTSULayout.TENKABCD.getId());
			if(StringUtils.isNotEmpty(val)){
				if(DefineReport.ValTenkabkbn.VAL1.getVal().equals(kbn)){
					tenkabcds1.add(dataArrayTENKABUTSU.optJSONObject(i));
					tenkabcds1_.add(val);
				}else if(DefineReport.ValTenkabkbn.VAL2.getVal().equals(kbn)){
					tenkabcds2.add(dataArrayTENKABUTSU.optJSONObject(i));
					tenkabcds2_.add(val);
				}
			}
		}
		// 同じ添加物がある場合、エラー。
		if(tenkabcds1.size() != tenkabcds1_.size()){
			JSONObject o = mu.getDbMessageObj("E11133", new String[]{});
			this.setCsvshnErrinfo(o, errTbl, MSTTENKABUTSULayout.TENKABCD, "");
			msg.add(o);
			return msg;
		}
		if(tenkabcds2.size() != tenkabcds2_.size()){
			JSONObject o = mu.getDbMessageObj("E11133", new String[]{});
			this.setCsvshnErrinfo(o, errTbl, MSTTENKABUTSULayout.TENKABCD, "");
			msg.add(o);
			return msg;
		}

		// グループ分類名
		errTbl = RefTable.MSTGRP;
		ArrayList<JSONObject> mstgrps= new ArrayList<JSONObject>();
		TreeSet<String> mstgrps_ = new TreeSet<String>();
		for (int i=0; i<dataArrayGROUP.size(); i++){
			String val = dataArrayGROUP.optJSONObject(i).optString(MSTGRPLayout.GRPKN.getId());
			if(StringUtils.isNotEmpty(val)){
				mstgrps.add(dataArrayGROUP.optJSONObject(i));
				mstgrps_.add(val);
			}
		}
		// 同じ名称がある場合、エラー。
		if(mstgrps.size() != mstgrps_.size()){
			JSONObject o = mu.getDbMessageObj("EX1012", new String[]{});
			this.setCsvshnErrinfo(o, errTbl, MSTGRPLayout.GRPKN, "");
			msg.add(o);
			return msg;
		}
		// 自動発注
		errTbl = RefTable.MSTAHS;
		ArrayList<JSONObject> mstahss= new ArrayList<JSONObject>();
		TreeSet<String> mstahss_ = new TreeSet<String>();
		for (int i=0; i<dataArrayAHS.size(); i++){
			// 自動発注区分は0,1で登録しなければならない
			if(!ArrayUtils.contains(new String[]{DefineReport.Values.ON.getVal(),DefineReport.Values.OFF.getVal()},dataArrayAHS.optJSONObject(i).optString(MSTAHSLayout.AHSKB.getId()))){
				JSONObject o = mu.getDbMessageObj("E11012", new String[]{"自動発注区分"});
				this.setCsvshnErrinfo(o, errTbl, MSTAHSLayout.AHSKB, dataArrayAHS.optJSONObject(i));
				msg.add(o);
				return msg;
			}
			String val = dataArrayAHS.optJSONObject(i).optString(MSTAHSLayout.TENCD.getId());
			if(StringUtils.isNotEmpty(val)){
				mstahss.add(dataArrayAHS.optJSONObject(i));
				mstahss_.add(val);
			}
		}
		// 同じ名称がある場合、エラー。
		if(mstahss.size() != mstahss_.size()){
			JSONObject o = mu.getDbMessageObj("E11141", new String[]{});
			this.setCsvshnErrinfo(o, errTbl, MSTAHSLayout.TENCD, "");
			msg.add(o);
			return msg;
		}

		errTbl = RefTable.MSTSHN;
		// 変更(正) 1.3　添付資料（MD03112701）の“テーブル削除の整理”と添付資料の（MD03111002）の“商品マスタ登録時　マスタ変更予定日、店売価実施日のチェック”を参照。
		// 新規(予) 1.4　添付資料（MD03111002）の“商品マスタ登録時　マスタ変更予定日、店売価実施日のチェック”を参照。
		// 変更(予) 1.3　添付資料（MD03112701）の“テーブル削除の整理”と添付資料の（MD03111002）の“商品マスタ登録時　マスタ変更予定日、店売価実施日のチェック”を参照。
		if(isYoyaku1||isYoyaku2){
			// *　店売価実施日-４日＜ﾏｽﾀｰ変更日＜店売価実施日＆処理日付＜ﾏｽﾀｰ変更日＆処理日付＜店売価実施日
			// 送信日=店売価実施日-４日
			Integer int_sysdate = NumberUtils.toInt(sysdate);
			Integer int_senddate = NumberUtils.toInt(CmnDate.dateFormat(CmnDate.getDayAddedDate( CmnDate.convDate(txt_tenbaikadt), -4) ,DATE_FORMAT.DEFAULT_DATE));
			Integer int_yoyakudt = NumberUtils.toInt(txt_yoyakudt);
			Integer int_tenbaikadt = NumberUtils.toInt(txt_tenbaikadt);

		}


		// 新規(正) 1.5　商品登録数は商品登録限度数テーブルの登録限度数を超えた場合は、エラー。
		if(isNew){
			JSONArray shnchk_rows = this.getMstData(DefineReport.ID_SQL_SHN_CHK_UPDATECNT, new ArrayList<String>(Arrays.asList(sysdate)));
			if(shnchk_rows.size() < 1){
				JSONObject o = mu.getDbMessageObj("E11241", new String[]{});
				this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.SHNCD, data);
				msg.add(o);
				return msg;
			}
		}

		// 新規(正) 1.6　入力されたメーカーコードがメーカーマスタに存在しない、かつ、ソースコードから取得したメーカーコードとも異なる場合は、エラー。
		// 　　　　　　　空白の場合はソースコードから取得した値を登録する。設定されている場合は、その値を登録する。
		// 変更(正) 1.4　入力されたメーカーコードがメーカーマスタに存在しない、かつ、ソースコードから取得したメーカーコードとも異なる場合は、エラー。
		// 　　　　　　　また、ソースコードが入力されている場合に、メーカーコードが空白の場合、エラー。ただし、衣料使い回しフラグが１の場合はメーカーコードが空白の場合もエラーとしない。（7/14）
		// 新規(予) 1.5　入力されたメーカーコードがメーカーマスタに存在しない、かつ、ソースコードから取得したメーカーコードとも異なる場合は、エラー。
		// 　　　　　　　また、ソースコードが入力されている場合に、メーカーコードが空白の場合、エラー。
		String txt_makercd = data.optString(MSTSHNLayout.MAKERCD.getId());	// 入力されたメーカーコード
		String txt_makercd_new = txt_makercd;
		String chk_iryoreflg = data.optString(MSTSHNLayout.IRYOREFLG.getId());
		if(srccds.size() > 0){
			// ソースコードからメーカーコードの取得
			// 添付資料（MD03112501）のメーカーコードの取得方法
			String value = srccds.get(0).optString(MSTSRCCDLayout.SRCCD.getId());
			String kbn = srccds.get(0).optString(MSTSRCCDLayout.SOURCEKBN.getId());
			ArrayList<String> paramData = new ArrayList<String>(Arrays.asList(value, kbn, txt_bmncd));
			JSONArray makercd_row = this.getMstData(DefineReport.ID_SQL_MD03112501, paramData);				// ソースコードからメーカーコード取得
			String txt_makercd_src = "";
			if(makercd_row.size() > 0){
				txt_makercd_src = makercd_row.optJSONObject(0).optString("VALUE");
			}
			// メーカーコードが入力有の場合
			if(txt_makercd.length() > 0){
				// 入力されたメーカーコードがメーカーマスタに存在しない、かつ、ソースコードから取得したメーカーコードとも異なる場合
				if(!this.checkMstExist(DefineReport.InpText.MAKERCD.getObj(), txt_makercd) && !StringUtils.equals(txt_makercd_src, txt_makercd) ){
					JSONObject o = mu.getDbMessageObj("E11320", new String[]{});
					this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.MAKERCD, data);
					msg.add(o);
					return msg;
				}

			}
			// メーカーコードが空白の場合
			if(txt_makercd.length() == 0){
				if(!isNew){
					if(!DefineReport.Values.ON.getVal().equals(chk_iryoreflg)){
						JSONObject o = mu.getDbMessageObj("E11321", new String[]{});
						this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.MAKERCD, data);
						msg.add(o);
						return msg;
					}
				}else if(isNew){
					txt_makercd_new = txt_makercd_src;
				}
			}

			// ソースコードより取得したメーカーコードを設定。
			if(txt_makercd_src.length() >= 0){
				txt_makercd_new = txt_makercd_src;
			}
		}else {
			// ソースコードの入力がない場合
			if(txt_makercd.length() == 0){
				// メーカーコードが空白の場合はデフォルト値を適用する。
				txt_makercd_new = txt_bmncd + "00001";
			}
		}

		// ①標準仕入先コードが「1」の場合のみ、「保温区分」、「デリカワッペン」、「取扱区分」は選択不可
		// ②標準仕入先コードが「1」以外の場合、「保温区分」、「デリカワッペン」、「取扱区分」は必須入力。
		String sel_k_honkb	 = data.optString(MSTSHNLayout.K_HONKB.getId());		// 保温区分
		String sel_k_wapnflg = data.optString(MSTSHNLayout.K_WAPNFLG_R.getId());	// デリカワッペン
		String sel_k_torikb	 = data.optString(MSTSHNLayout.K_TORIKB.getId());		// 取扱区分

		if(StringUtils.equals("20", txt_bmncd)
				|| StringUtils.equals("23", txt_bmncd)
				|| StringUtils.equals("31", txt_bmncd)){

			if(StringUtils.equals("1", txt_ssircd.trim())){
				MSTSHNLayout errField = null;

				if(StringUtils.isNotEmpty(sel_k_honkb)){
					errField = MSTSHNLayout.K_HONKB;
				}else if(StringUtils.isNotEmpty(sel_k_wapnflg)){
					errField = MSTSHNLayout.K_WAPNFLG_R;
				}else if(StringUtils.isNotEmpty(sel_k_torikb)){
					errField = MSTSHNLayout.K_TORIKB;
				}

				if(errField != null){
					JSONObject o = mu.getDbMessageObj("E30012", new String[]{"標準仕入先コードが「000001」以外の場合、「保温区分」、「デリカワッペン」、「取扱区分」は空欄"});
					this.setCsvshnErrinfo(o, errTbl, errField, data);
					msg.add(o);
					return msg;
				}
			}else{
				MSTSHNLayout errField = null;
				if(StringUtils.isEmpty(sel_k_honkb)){
					errField = MSTSHNLayout.K_HONKB;
				}else if(StringUtils.isEmpty(sel_k_wapnflg)){
					errField = MSTSHNLayout.K_WAPNFLG_R;
				}else if(StringUtils.isEmpty(sel_k_torikb)){
					errField = MSTSHNLayout.K_TORIKB;
				}

				if(errField != null){
					JSONObject o = mu.getDbMessageObj("E30012", new String[]{"標準仕入先コードが「000001」以外の場合、「保温区分」、「デリカワッペン」、「取扱区分」"});
					this.setCsvshnErrinfo(o, errTbl, errField, data);
					msg.add(o);
					return msg;
				}
			}

		}


		// 変更(正) 1.5　部門マスタの評価方法区分が”２”（売価還元法）の部門で、「レギュラー本体売価」の変更があった場合、
		// ①衣料使いまわしフラグ”１”以外の場合、「マスタ更新可」ユーザー（「管理者」ユーザーを含む）だったら、登録ボタン押下時、「レギュラー売価が直接変更されました。本当に変更してよろしいですか？」のようなメッセージと「はい」「いいえ」のボタンをあわせて表示して更新を実行させる。
		//  「いいえ」なら、キャンセル。「はい」なら更新を実施。カーソルのデフォルトは「いいえ」に当てておく。
		//   ※エラー条件の優先度は、他のチェックを終えてから最後に追加。
		// ②．衣料使いまわし区分＝１の時、
		//   全チェックがＯＫだった場合、登録日、変更日を本日日付にする
		// TODO:確認　①②に関しては、登録画面ではJSで実施、CSV取込ではエラー扱いとしてCSVトラン登録
		if(isCsvUpload && isChange){					// 変更(正)
			JSONArray mstbmn_row = this.getMstData(DefineReport.ID_SQL_BUMON_C + DefineReport.ID_SQL_CMN_WHERE + DefineReport.ID_SQL_BMN_BUMON_WHERE +  DefineReport.ID_SQL_BUMON_FOOTER_C, new ArrayList<String>(Arrays.asList(txt_bmncd)));				// 部門マスタから評価方法区分取得
			if(mstbmn_row.size() > 0){
				String txt_rg_baikaam_ = seiJsonObject.optString(MSTSHNLayout.RG_BAIKAAM.getId());
				if( DefineReport.ValKbn504.VAL2.getVal().equals(mstbmn_row.optJSONObject(0).optString("HYOKAKBN"))
					&& NumberUtils.toInt(txt_rg_baikaam) != NumberUtils.toInt(txt_rg_baikaam_)){
					if(!DefineReport.Values.ON.getVal().equals(chk_iryoreflg)){
						JSONObject o = mu.getDbMessageObj("E11328", new String[]{});
						this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.RG_BAIKAAM, data);
						msg.add(o);
						return msg;
						// JSでのチェックの場合は"W20037"を表示
					}else{
						data.element(MSTSHNLayout.ADDDT.getId(), login_dt);
						data.element(MSTSHNLayout.UPDDT.getId(), login_dt);
					}
				}
			}
		}
		// エラーがなかった場合、新規時はデータ加工
		if(isCsvUpload){
			// 取得メーカーコードを設定
			data.element(MSTSHNLayout.MAKERCD.getId(), txt_makercd_new);
			dataArray.element(0, data);
		}

		return msg;
	}

	public List<JSONObject> checkDataDel(
			boolean isNew, boolean isChange, boolean isYoyaku1, boolean isYoyaku2, boolean isCsvUpload,
			HashMap<String, String> map, User userInfo, String sysdate1, MessageUtility mu,
			JSONArray dataArray,			// 対象情報（主要な更新情報）
			JSONArray dataArraySRCCD,		// ソースコード
			JSONArray dataArrayTENGP3,		// 品揃えグループ
			JSONArray dataArrayTENGP2,		// 売価コントロール
			JSONArray dataArrayTENGP1,		// 仕入グループ
			JSONArray dataArrayTENKABUTSU	// 添加物
		) {

		JSONArray msg = new JSONArray();


		// DB最新情報再取得
		String szYoyaku = "0";
		JSONArray yArray = new JSONArray();
		JSONObject seiJsonObject = new JSONObject();
		if(isChange){
			JSONArray array = getSeiJSONArray(map);
			seiJsonObject = array.optJSONObject(0);
		}


		// ④予1.新規
		boolean isNewY1 = isYoyaku1 && NumberUtils.toInt(szYoyaku, 0) == 0;
		// ⑤予1.変更
		boolean isChangeY1 = isYoyaku1 && NumberUtils.toInt(szYoyaku, 0) > 0;
		// ⑦予2.新規
		boolean isNewY2 = isYoyaku2 && NumberUtils.toInt(szYoyaku, 0) == 1;
		// ⑧予2.変更
		boolean isChangeY2 =isYoyaku2 && NumberUtils.toInt(szYoyaku, 0) > 1;


		JSONObject data = dataArray.optJSONObject(0);

		String txt_shncd = data.optString(MSTSHNLayout.SHNCD.getId());
		String txt_yoyakudt = data.optString(MSTSHNLayout.YOYAKUDT.getId());
		String txt_tenbaikadt = data.optString(MSTSHNLayout.TENBAIKADT.getId());
		String txt_bmncd = data.optString(MSTSHNLayout.BMNCD.getId());

		String login_dt = sysdate1;	// 処理日付
		String sysdate = login_dt;				// 比較用処理日付

		RefTable errTbl = RefTable.MSTSHN;
		// 変更(正) 1.3　添付資料（MD03112701）の“テーブル削除の整理”と添付資料の（MD03111002）の“商品マスタ登録時　マスタ変更予定日、店売価実施日のチェック”を参照。
		// 新規(予) 1.4　添付資料（MD03111002）の“商品マスタ登録時　マスタ変更予定日、店売価実施日のチェック”を参照。
		// 変更(予) 1.3　添付資料（MD03112701）の“テーブル削除の整理”と添付資料の（MD03111002）の“商品マスタ登録時　マスタ変更予定日、店売価実施日のチェック”を参照。
		// 前提：CSVエラー修正の場合、エラー情報を削除するので常に削除可
		// 正 .変更
		if(isChange){
			// 予約1がある場合
			if(NumberUtils.toInt(szYoyaku, 0) > 0) {
				// E11179	予約に登録がある場合は削除できません。                                                              	 	0	 	E
				JSONObject o = mu.getDbMessageObj("E11179", new String[]{});
				this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.SHNCD, data);
				msg.add(o);
				return msg;
			}
		// 予1.変更
		}else if(isChangeY1){
			// 予約2がある場合
			if(NumberUtils.toInt(szYoyaku, 0) > 1) {
				// E11231	予約2に登録がある場合は削除できません。 	 	0	 	E
				JSONObject o = mu.getDbMessageObj("E11231", new String[]{});
				this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.SHNCD, data);
				msg.add(o);
				return msg;
			}
		}

		if(isYoyaku1||isYoyaku2){
			// *　店売価実施日-４日＜ﾏｽﾀｰ変更日＜店売価実施日＆処理日付＜ﾏｽﾀｰ変更日＆処理日付＜店売価実施日
			// 送信日=店売価実施日-４日
			Integer int_sysdate = NumberUtils.toInt(sysdate);
			Integer int_senddate = NumberUtils.toInt(CmnDate.dateFormat(CmnDate.getDayAddedDate( CmnDate.convInpDate(txt_tenbaikadt), -4) ,DATE_FORMAT.DEFAULT_DATE));
			Integer int_yoyakudt = NumberUtils.toInt(txt_yoyakudt);
			Integer int_tenbaikadt = NumberUtils.toInt(txt_tenbaikadt);

			// 予約の変更
			if( isChangeY1 || isChangeY2 ){
				// 処理日付＝＜送信日（店売価実施日-4日）　であれば可能
				if(!(int_sysdate <= int_senddate)){
					// E11157	商品予約削除可能期間以外の場合、                                                                    	削除操作は出来ません。	0	 	E
					JSONObject o = mu.getDbMessageObj("E11157", new String[]{});
					this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.TENBAIKADT, data);
					msg.add(o);
					return msg;
				}

				// 変更にもかかわらず、キー項目が変更されていたらエラー
				String txt_yoyakudt_ = "";		// 検索実行時のﾏｽﾀ変更日
				String txt_tenbaikadt_ = "";	// 検索実行時の店売価実施日
				if(isChangeY1 && yArray.size() > 0){
					txt_yoyakudt_ = yArray.optJSONObject(0).optString(MSTSHNLayout.YOYAKUDT.getId());			// 検索実行時のﾏｽﾀ変更日
					txt_tenbaikadt_ = yArray.optJSONObject(0).optString(MSTSHNLayout.TENBAIKADT.getId());		// 検索実行時の店売価実施日

				}else if(isChangeY2 && yArray.size() > 1){
					txt_yoyakudt_ = yArray.optJSONObject(1).optString(MSTSHNLayout.YOYAKUDT.getId());			// 検索実行時のﾏｽﾀ変更日
					txt_tenbaikadt_ = yArray.optJSONObject(1).optString(MSTSHNLayout.TENBAIKADT.getId());		// 検索実行時の店売価実施日
				}
				if(int_yoyakudt != NumberUtils.toInt(txt_yoyakudt_)){
					// E11195	予約のマスタ変更日変更不可                                                                          	 	0	 	E
					JSONObject o = mu.getDbMessageObj("E11195", new String[]{});
					this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.YOYAKUDT, data);
					msg.add(o);
					return msg;
				}
				if(int_tenbaikadt != NumberUtils.toInt(txt_tenbaikadt_)){
					// E11196	予約の店売価実施日変更不可                                                                          	 	0	 	E
					JSONObject o = mu.getDbMessageObj("E11196", new String[]{});
					this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.TENBAIKADT, data);
					msg.add(o);
					return msg;
				}
			}else{
				// EX1095	削除できません。	 	0	 	E
				JSONObject o = mu.getDbMessageObj("EX1095", new String[]{});
				this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.SHNCD, data);
				msg.add(o);
				return msg;
			}
		}
		return msg;
	}


	private String isAbleReadtmptn(JSONObject data, String txt_bmncd) {
		String txt_readtmptn = data.optString(MSTSHNLayout.READTMPTN.getId());
		//  02,09,15,04,05,06,10,11,20,23,34,43,88,13,26,27部門以外は、以下の組合せの場合はエラーとする。
		// CCRが本処理に対応できない為、要望によりMDMでは全ての部門の入力可とする。
//		if(!ArrayUtils.contains(new Integer[]{2,9,15,4,5,6,10,11,20,23,34,43,88,13,26,27}, NumberUtils.toInt(txt_bmncd))
//			&& ArrayUtils.contains(new String[]{"10","20","21","30"}, txt_readtmptn)){
//			return  "EX1005";
//		}
		// それ以外の場合、組合せチェック
		if(StringUtils.isNotEmpty(txt_readtmptn)){
			String msgid = "";
			String chk_hat_frikbn = data.optString(MSTSHNLayout.HAT_FRIKBN.getId());
			String chk_hat_satkbn = data.optString(MSTSHNLayout.HAT_SATKBN.getId());
			String chk_hat_sunkbn = data.optString(MSTSHNLayout.HAT_SUNKBN.getId());
			if(StringUtils.equals(txt_readtmptn, "10") && !DefineReport.Values.ON.getVal().equals(chk_hat_frikbn)){
				return  "E11230";
			}else if(StringUtils.equals(txt_readtmptn, "20") && !DefineReport.Values.ON.getVal().equals(chk_hat_satkbn)){
				return  "E11230";
			}else if(StringUtils.equals(txt_readtmptn, "21") && !DefineReport.Values.ON.getVal().equals(chk_hat_sunkbn)){
				return  "E11230";
			}else if(StringUtils.equals(txt_readtmptn, "30") && !DefineReport.Values.ON.getVal().equals(chk_hat_frikbn)){
				return  "E11230";
			}
		}
		return null;
	}

	private boolean isAbleGenbaika(String atsukflg, String kbn105, String txt_genkaam, String txt_baikaam) {
		//  原売価0チェック:扱い時原価=0and売価=0可（片方だけ0の場合はエラー）
		if(DefineReport.Values.ON.getVal().equals(atsukflg)){
			double genka = NumberUtils.toDouble(txt_genkaam, 0);
			double baika = NumberUtils.toDouble(txt_baikaam, 0);

			if(genka == 0 && baika ==0){					//
				if(!ArrayUtils.contains(new String[]{"4","5","6"}, kbn105)){
					return false;
				}
			}else if(genka != 0 && baika == 0 || genka == 0 && baika !=0){
				return false;
			}
		}
		return true;
	}

	private double calcNeireRit(String txt_genkaam, String txt_baikaam) {
		if(txt_genkaam.length()==0) return 0;
		if(txt_baikaam.length()==0) return 0;

		double genka = NumberUtils.toDouble(txt_genkaam);
		double baika = NumberUtils.toDouble(txt_baikaam);

		// （本体売価－原価）÷本体売価で、小数点以下3位切り捨て, 第2位まで求める。上限98%
		// ただし、商品種別で包材、消耗品、コメント、催事テナントの時はチェックしない。
		return Math.floor((baika-genka)/baika*10000)/100;
	}

	/**
	 * CSVエラー情報セット
	 * @throws Exception
	 */
	public void setCsvshnErrinfo(JSONObject o, String errcd, String errtbl, String errfld, String errvl) {
		o.put(CSVSHNLayout.ERRCD.getCol(), errcd);	// TODO
		o.put(CSVSHNLayout.ERRTBLNM.getCol(), errtbl);
		o.put(CSVSHNLayout.ERRFLD.getCol(), errfld);
		o.put(CSVSHNLayout.ERRVL.getCol(), MessageUtility.leftB(StringUtils.trim(errvl + " " + o.optString(MessageUtility.MSG)), 100));
	}

	/**
	 * CSVエラー情報セット
	 * @throws Exception
	 */
	public void setCsvshnErrinfo(JSONObject o, RefTable errtbl, MSTLayout errfld, String errvl) {
		this.setCsvshnErrinfo(o, "0", errtbl.getTxt(), errfld.getTxt(), errvl);
	}

	/**
	 * CSVエラー情報セット
	 * @throws Exception
	 */
	public void setCsvshnErrinfo(JSONObject o, RefTable errtbl, MSTLayout errfld, JSONObject data) {
		this.setCsvshnErrinfo(o, errtbl, errfld, data.optString(errfld.getId()));
	}


	/**
	 * 正情報取得処理
	 *
	 * @throws Exception
	 */
	public JSONArray getSeiJSONArray(HashMap<String, String> map) {
		String szSelShncd	= map.get("SEL_SHNCD");	// 検索商品コード


		JSONArray array = new JSONArray();
		if(!szSelShncd.isEmpty()){
			ArrayList<String> paramData = new ArrayList<String>();
			paramData.add(szSelShncd.replace("-", "") + "%");

			StringBuffer sbSQL = new StringBuffer();
			sbSQL.append(" select ");
			for(MSTSHNLayout itm :MSTSHNLayout.values()){
				if(itm.getNo() > 1){ sbSQL.append(" ,"); }
				sbSQL.append(itm.getCol() + " as " + itm.getId());
			}
			sbSQL.append(" from INAWS.PIMTIT where SHNCD like ? and nvl(UPDKBN, 0) <> 1");

			ItemList iL = new ItemList();
			@SuppressWarnings("static-access")
			JSONArray array0 = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);
			array.addAll(array0);
		}
		return array;
	}

	/**
	 * 予約情報取得処理
	 *
	 * @throws Exception
	 */
	public JSONArray getYoyakuJSONArray(HashMap<String, String> map) {
		String szSelShncd	= map.get("SEL_SHNCD");	// 検索商品コード

		JSONArray array = new JSONArray();
		if(!szSelShncd.isEmpty()){
			ArrayList<String> paramData = new ArrayList<String>();
			paramData.add(szSelShncd.replace("-", "") + "%");

			StringBuffer sbSQL = new StringBuffer();
			sbSQL.append(" select ");
			for(MSTSHNLayout itm :MSTSHNLayout.values()){
				if(itm.getNo() > 1){ sbSQL.append(" ,"); }
				sbSQL.append(itm.getCol() + " as " + itm.getId());
			}
			sbSQL.append(" from INAMS.MSTSHN_Y where SHNCD like ? and nvl(UPDKBN, 0) <> 1 order by YOYAKUDT");

			ItemList iL = new ItemList();
			@SuppressWarnings("static-access")
			JSONArray array0 = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);
			array.addAll(array0);
		}

		return array;
	}

	/**
	 * SEQ情報取得処理
	 *
	 * @throws Exception
	 */
	public String getJNLSHN_SEQ() {
		// 関連情報取得
		ItemList iL = new ItemList();
		String sqlColCommand = "VALUES NEXTVAL FOR INAMS.SEQ002";
		@SuppressWarnings("static-access")
		JSONArray array = iL.selectJSONArray(sqlColCommand, null, Defines.STR_JNDI_DS);
		String value = "";
		if(array.size() > 0){
			value = array.optJSONObject(0).optString("1");
		}
		return value;
	}

	/**
	 * 新規商品コード取得処理
	 *
	 * @throws Exception
	 */
	public String getMSTSHN_SHNCD_(String inpshncd, String ketakbn, String bmncd) {
		// 関連情報取得
		ItemList iL = new ItemList();
		// 配列準備
		ArrayList<String> paramData = new ArrayList<String>();
		String sqlcommand = "";

		if(((DefineReport.ValKbn143.VAL0.getVal().equals(ketakbn)&&StringUtils.length(inpshncd) == 0)
		 || (DefineReport.ValKbn143.VAL2.getVal().equals(ketakbn)&&StringUtils.length(inpshncd) == 4 && NumberUtils.isNumber(inpshncd)))
			&&NumberUtils.isNumber(bmncd)){
			paramData.add(inpshncd);
			paramData.add(ketakbn);
			paramData.add(bmncd);
			paramData.add(bmncd);
			sqlcommand = DefineReport.ID_SQL_MD03100901;
		}
		String value = "";
		if(sqlcommand.length() > 0){
			@SuppressWarnings("static-access")
			JSONArray array = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
			if(array.size() > 0){
				value = array.optJSONObject(0).optString("VALUE");
			}
		}
		return value;
	}

	/**
	 * マスタ情報取得処理
	 *
	 * @throws Exception
	 */
	public JSONArray getMstData(String sqlcommand, ArrayList<String> paramData) {
		// 関連情報取得
		ItemList iL = new ItemList();
		@SuppressWarnings("static-access")
		JSONArray array = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
		return array;
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
		String dataLength = "";	// 複数データの存在チェックを行う場合に使用

		String tbl = "";
		String col = "";
		String rep = "";
		String whr ="";
		// 商品コード
		if (outobj.equals(DefineReport.InpText.SHNCD.getObj())) {
			tbl="INAWS.PIMTIT";
			col="SHNCD";
			whr = DefineReport.ID_SQL_CMN_WHERE2;
		}
		// メーカーコード
		if (outobj.equals(DefineReport.InpText.MAKERCD.getObj())) {
			tbl="INAWS.PIMTIMAKER";
			col="MAKERCD";
			whr = DefineReport.ID_SQL_CMN_WHERE2;
		}
		// 部門コード
		if (outobj.equals(DefineReport.InpText.BMNCD.getObj())) {
			tbl="INAMS.MSTBMN";
			col="BMNCD";
			whr = DefineReport.ID_SQL_CMN_WHERE2;
		}

		// 仕入先コード
		if (outobj.equals(DefineReport.InpText.SIRCD.getObj())) {
			tbl="INAMS.MSTSIR";
			col="SIRCD";
			whr = DefineReport.ID_SQL_CMN_WHERE2;
		}

		// 配送パターン仕入先
		if (outobj.equals("MSTHSPTNSIR") && value.length() > 1) {
			tbl="INAMS.MSTHSPTNSIR";
			col="right('00000'||SIRCD, 6)||right('00'||HSPTN, 3)";

			String[] vals = StringUtils.split(value, ",");
			for(String val : vals){
				rep += ", ?";
				String cd = StringUtils.leftPad(val.split("-")[0], 6, "0")+StringUtils.leftPad(val.split("-")[1], 3, "0");
				paramData.add(cd);
			}
			rep = StringUtils.removeStart(rep, ",");

			if(paramData.size() > 0){
				dataLength = ""+paramData.size();
			}
		}

		// 店別商品コード
		if (outobj.equals(DefineReport.InpText.TENSHNCD.getObj())) {
			tbl="INAWS.PIMTIT";
			col="SHNCD";
			whr = DefineReport.ID_SQL_CMN_WHERE2;
			String[] vals = StringUtils.split(value, ",");
			for(String val : vals){
				rep += ", ?";
				paramData.add(val);
			}
			rep = StringUtils.removeStart(rep, ",");

			if(paramData.size() > 0){
				dataLength = ""+paramData.size();
			}
		}


		if(tbl.length()>0&&col.length()>0){
			if(paramData.size() > 0 && rep.length() > 0 ){
				sqlcommand = DefineReport.ID_SQL_CHK_TBL.replace("@T", tbl).replaceAll("@C", col).replace("?", rep) + whr;
			}else{
				paramData.add(value);
				sqlcommand = DefineReport.ID_SQL_CHK_TBL.replace("@T", tbl).replaceAll("@C", col) + whr;
			}

			@SuppressWarnings("static-access")
			JSONArray array = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
			if(array.size() > 0 && NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) > 0){
				if(StringUtils.isNotEmpty(dataLength)){
					if(NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) == Integer.parseInt(dataLength)){
						return true;
					}
				}else{
					return true;
				}

			}
		}
		return false;
	}


	/**
	 * マスタ店グループ系情報取得処理
	 * @param tengp3s_
	 *
	 * @throws Exception
	 */
	public String[] checkMsttgpExist(TreeSet<String> tengpcd,String gpkbn, String bmncd, String areakbn) {
		// 関連情報取得
		ItemList iL = new ItemList();
		// 配列準備
		ArrayList<String> paramData = new ArrayList<String>();
		String sqlcommand = "";

		String tbl = "INAMS.MSTSHNTENGP";
		String col = "TENGPCD";
		TreeSet<String> tengpcdNum = new TreeSet<String>();

		String[] errcds = new String[]{};
		if(tengpcd.size()>0){
			String rep = "";
			for(String val : tengpcd){
				rep += ", ?";
				paramData.add(val);
				if(StringUtils.isNumeric(val)){
					tengpcdNum.add("" + Integer.parseInt(val));
				}

			}
			rep = StringUtils.removeStart(rep, ",");
			paramData.add(gpkbn);
			paramData.add(bmncd);
			paramData.add(areakbn);

			sqlcommand = DefineReport.ID_SQL_CHK_TBL_SEL.replace("@T", tbl).replaceAll("@C", col).replace("?", rep)
					+  " and GPKBN = ? and BMNCD = ? and AREAKBN = ?";

			@SuppressWarnings("static-access")
			JSONArray array = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);

			for(int i = 0; i < array.size(); i++){
				String val = array.optJSONObject(i).optString("VALUE");
				if(tengpcdNum.contains(val)){
					tengpcdNum.remove(val);
				}
			}

			Iterator<String> tengpcdNums = tengpcdNum.iterator();
			for (int i = 0; i < tengpcdNum.size(); i++) {
				errcds = (String[]) ArrayUtils.add(errcds,tengpcdNums.next());
			}
		}
		return errcds;
	}

	/**
	 * マスタ部門系情報取得処理
	 *
	 * @throws Exception
	 */
	public JSONObject checkMstbmnExist(MessageUtility mu, RefTable errTbl, String bmncd, String daicd, String chucd, String shocd, String sshocd, String tbl_suffix) {
		// 関連情報取得
		ItemList iL = new ItemList();
		// 配列準備
		ArrayList<String> paramData = new ArrayList<String>();
		String sqlcommand = "";

		paramData.add(StringUtils.defaultIfEmpty(bmncd, "-1"));
		paramData.add(StringUtils.defaultIfEmpty(daicd, "-1"));
		paramData.add(StringUtils.defaultIfEmpty(chucd, "-1"));
		paramData.add(StringUtils.defaultIfEmpty(shocd, "-1"));
		paramData.add(StringUtils.defaultIfEmpty(sshocd, "-1"));
		sqlcommand = StringUtils.replace(DefineReport.ID_SQL_BMN_CHK, "@", tbl_suffix);

		@SuppressWarnings("static-access")
		JSONArray array = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);


		String messageId = "";
		MSTSHNLayout col = null;
		String val = "";
		if(!this.isEmptyVal(bmncd, true)&&StringUtils.isEmpty(array.optJSONObject(0).optString("1"))){
			messageId = "E11044";
			if(StringUtils.equals(tbl_suffix, "_YOT")){
				col = MSTSHNLayout.YOT_BMNCD;
			}else if(StringUtils.equals(tbl_suffix, "_URI")){
				col = MSTSHNLayout.URI_BMNCD;
			}else{
				col = MSTSHNLayout.BMNCD;
			}
			val=bmncd;
		}else if(!this.isEmptyVal(daicd, true)&&StringUtils.isEmpty(array.optJSONObject(0).optString("2"))){
			messageId = "E11135";
			if(StringUtils.equals(tbl_suffix, "_YOT")){
				col = MSTSHNLayout.YOT_DAICD;
			}else if(StringUtils.equals(tbl_suffix, "_URI")){
				col = MSTSHNLayout.URI_DAICD;
			}else{
				col = MSTSHNLayout.DAICD;
			}
			val=daicd;
		}else if(!this.isEmptyVal(chucd, true)&&StringUtils.isEmpty(array.optJSONObject(0).optString("3"))){
			messageId = "E11136";
			if(StringUtils.equals(tbl_suffix, "_YOT")){
				col = MSTSHNLayout.YOT_CHUCD;
			}else if(StringUtils.equals(tbl_suffix, "_URI")){
				col = MSTSHNLayout.URI_CHUCD;
			}else{
				col = MSTSHNLayout.CHUCD;
			}
			val=chucd;
		}else if(!this.isEmptyVal(shocd, true)&&StringUtils.isEmpty(array.optJSONObject(0).optString("4"))){
			messageId = "E11137";
			if(StringUtils.equals(tbl_suffix, "_YOT")){
				col = MSTSHNLayout.YOT_SHOCD;
			}else if(StringUtils.equals(tbl_suffix, "_URI")){
				col = MSTSHNLayout.URI_SHOCD;
			}else{
				col = MSTSHNLayout.SHOCD;
			}
			val=shocd;
		}

		JSONObject o = new JSONObject();
		if(col!=null){
			o = mu.getDbMessageObj(messageId, new String[]{});
			this.setCsvshnErrinfo(o, errTbl, col, val);
		}
		return o;
	}

	public JSONObject createSqlMSTSHN(String userId, String btnId, JSONObject data, TblType tbl, SqlType sql){
		return createSqlMSTSHN(userId, btnId, data, tbl, sql, "", "");
	}

	/**
	 * 商品マスタINSERT/UPDATE SQL作成処理
	 *
	 * @param userId
	 * @param data
	 * @param tbl
	 *
	 * @throws Exception
	 */
	public JSONObject createSqlMSTSHN(String userId, String btnId, JSONObject data, TblType tbl, SqlType sql, String teianNo, String status){

        JSONObject result = new JSONObject();

		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();
		String values = "", names = "";
		int colNum = mstshn_col_num;
		if(TblType.JNL.getVal()==tbl.getVal()){
			colNum += 5;
		}
		if(TblType.CSV.getVal()==tbl.getVal()){
			colNum += csvshn_add_data.length;
		}
		// 特殊変換項目
		String shnkn = StringUtils.trim(data.optString(MSTSHNLayout.SHNKN.getId()));
		if(StringUtils.isEmpty(shnkn)){
			shnkn = MessageUtility.HanToZen(data.optString(MSTSHNLayout.SHNAN.getId()));
		}
		//WEB商品
		//提案NO
		if(StringUtils.isEmpty(teianNo)){
			values += ", null";
		}else{
			if(isTest){
				values += ", '"+teianNo+"'";
			}else{
				prmData.add(teianNo);
				values += ", cast(? as INTEGER)";
			}
		}
		names  += ", TEIANNO";
		//商品ステータス
		if(StringUtils.isEmpty(status)){
			values += ", null";
		}else{
			if(isTest){
				values += ", '"+status+"'";
			}else{
				prmData.add(status);
				values += ", cast(? as INTEGER)";
			}
		}
		names  += ", STATUS";

		for (int i = 1; i <= colNum; i++) {
			String col = "F" + i;
			String val = StringUtils.trim(data.optString(col));
			if(i==MSTSHNLayout.SHNKN.getNo()){											// 商品名（漢字）:未入力またはスペースの場合、商品名（カナ）項目を全角変換して設定。
				val = shnkn;
			}else if(i==MSTSHNLayout.RECEIPTAN.getNo()&&StringUtils.isEmpty(val)){		// レシート名（カナ）:半角大文字。未入力またはスペースの場合、商品名（カナ）項目をコピーする
				val = data.optString(MSTSHNLayout.SHNAN.getId());
			}else if(i==MSTSHNLayout.RECEIPTKN.getNo()&&StringUtils.isEmpty(val)){		// レシート名（漢字）:全角。未入力またはスペースの場合、商品名（漢字）項目をコピーする
				val = shnkn;
			}else if(i==MSTSHNLayout.PCARDKN.getNo()&&StringUtils.isEmpty(val)){		// プライスカード商品名称（漢字）:全角。未入力またはスペースの場合、商品名（漢字）項目をコピーする
				val = shnkn;
			}else if(i==MSTSHNLayout.POPKN.getNo()&&StringUtils.isEmpty(val)){			// POP名称:全角。未入力またはスペースの場合、商品名（漢字）項目をコピーする
				val = shnkn;
			}else if(i==MSTSHNLayout.KASANARICD.getNo()&&StringUtils.isEmpty(val)){		// 重なりコード:画面重なりコードに何も入力がない場合は、半角スペースを登録する。
				val = " ";
			}else if(i==MSTSHNLayout.SHUBETUCD.getNo()&&StringUtils.isEmpty(val)){		// 種別コード:画面種別コードに何も入力がない場合は、半角スペースを登録する。
				val = " ";
			}else if(i==MSTSHNLayout.KIKANKBN.getNo()&&StringUtils.isEmpty(val)){		// 期間にデフォルト値"D"を設定する。
				val = "D";
			}else if(i==MSTSHNLayout.PARENTCD.getNo()&&StringUtils.isEmpty(val)){		// 親商品コードに"00000000"を設定する。
				val = "00000000";
			}else if(i==MSTSHNLayout.IRYOREFLG.getNo()&&StringUtils.isEmpty(val)){		// 衣料使い回しフラグがnullの場合"0"を設定
				val = "0";
			}else if(i==MSTSHNLayout.ELPFLG.getNo()&&StringUtils.isEmpty(val)){			// フラグ情報_ELPがnullの場合"0"を設定
				val = "0";
			}else if(i==MSTSHNLayout.BELLMARKFLG.getNo()&&StringUtils.isEmpty(val)){	// フラグ情報_ベルマークがnullの場合"0"を設定
				val = "0";
			}else if(i==MSTSHNLayout.RECYCLEFLG.getNo()&&StringUtils.isEmpty(val)){		// フラグ情報_リサイクルがnullの場合"0"を設定
				val = "0";
			}else if(i==MSTSHNLayout.ECOFLG.getNo()&&StringUtils.isEmpty(val)){			// フラグ情報_エコマークがnullの場合"0"を設定
				val = "0";
			}else if(i==MSTSHNLayout.HZI_RECYCLE.getNo()&&StringUtils.isEmpty(val)){	// 包材リサイクル対象がnullの場合"0"を設定
				val = "0";
			}else if(i==MSTSHNLayout.PCARD_OPFLG.getNo()&&StringUtils.isEmpty(val)){	// プライスカード出力有無がnullの場合"0"を設定
				val = "0";
			}else if(i==MSTSHNLayout.HAT_MONKBN.getNo()&&StringUtils.isEmpty(val)){		// 発注曜日_月がnullの場合"0"を設定
				val = "0";
			}else if(i==MSTSHNLayout.HAT_TUEKBN.getNo()&&StringUtils.isEmpty(val)){		// 発注曜日_火がnullの場合"0"を設定
				val = "0";
			}else if(i==MSTSHNLayout.HAT_WEDKBN.getNo()&&StringUtils.isEmpty(val)){		// 発注曜日_水がnullの場合"0"を設定
				val = "0";
			}else if(i==MSTSHNLayout.HAT_THUKBN.getNo()&&StringUtils.isEmpty(val)){		// 発注曜日_木がnullの場合"0"を設定
				val = "0";
			}else if(i==MSTSHNLayout.HAT_FRIKBN.getNo()&&StringUtils.isEmpty(val)){		// 発注曜日_金がnullの場合"0"を設定
				val = "0";
			}else if(i==MSTSHNLayout.HAT_SATKBN.getNo()&&StringUtils.isEmpty(val)){		// 発注曜日_土がnullの場合"0"を設定
				val = "0";
			}else if(i==MSTSHNLayout.HAT_SUNKBN.getNo()&&StringUtils.isEmpty(val)){		// 発注曜日_日がnullの場合"0"を設定
				val = "0";
			}else if(i==MSTSHNLayout.TOROKUMOTO.getNo()){		// 登録元
				if (StringUtils.isEmpty(val) || !val.equals("1")) {
					val = "0";
				}
			}else if(i==MSTSHNLayout.UPDKBN.getNo()){			// 更新区分
				val = DefineReport.ValUpdkbn.NML.getVal();
			}else if(i==MSTSHNLayout.SENDFLG.getNo()){			// 送信フラグ
				val = DefineReport.Values.SENDFLG_UN.getVal();
			}else if(i==MSTSHNLayout.OPERATOR.getNo()){		// オペレータ
				val = userId;
			}

			// カナ名:全角。未入力またはスペースの場合、商品名（漢字）項目をコピーする
			// 漢字名:
			// 商品名称:名称マスタからリストを取得する（名称コード区分=118）。初期値は名称マスタの名称コード=0の項目に設定する。
			// POP名称:全角。

			// ジャーナル
			if(TblType.JNL.getVal()==tbl.getVal()){
				if(StringUtils.equals(col, JNLSHNLayout.SEQ.getId2())){						// SEQ(ジャーナル用)
					val = jnlshn_seq;
				}else if(StringUtils.equals(col, JNLSHNLayout.INF_OPERATOR.getId2())){		// 更新情報_オペレータ(ジャーナル用)
					val = userId;
				}else if(StringUtils.equals(col, JNLSHNLayout.INF_TABLEKBN.getId2())){		// 更新情報_テーブル区分(ジャーナル用)
					val = jnlshn_tablekbn;
				}else if(StringUtils.equals(col, JNLSHNLayout.INF_TRANKBN.getId2())){		// 更新情報_処理区分(ジャーナル用)
					val = jnlshn_trankbn;
				}
			}
			// CSV特殊情報設定
			if(TblType.CSV.getVal()==tbl.getVal()){
				if(i > mstshn_col_num){
					val = csvshn_add_data[i - mstshn_col_num - 1];
				}
			}

			if(StringUtils.isEmpty(val)){
				values += ", null";
			}else{
				if(isTest){
					values += ", '"+val+"'";
				}else{
					prmData.add(val);
					values += ", cast(? as varchar("+MessageUtility.getDefByteLen(val)+"))";
				}
			}
			names  += ", "+col;
		}
		values = "("+StringUtils.removeStart(values, ",")+")";
		names  = StringUtils.removeStart(names, ",");

		StringBuffer sbSQL;
		sbSQL = new StringBuffer();
		sbSQL.append(this.createMergeCmnCommandMSTSHN(tbl, sql, values, names));


		if(SqlType.INS.getVal() == sql.getVal()||SqlType.MRG.getVal() == sql.getVal()){
			sbSQL.append(" when not matched then ");
			sbSQL.append(" insert (");
			if(TblType.JNL.getVal()==tbl.getVal()){
				sbSQL.append("  SEQ");			// SEQ
				sbSQL.append(" ,INF_DATE");		// 更新情報_更新日時
				sbSQL.append(" ,INF_OPERATOR");	// 更新情報_オペレータ
				sbSQL.append(" ,INF_TABLEKBN");	// 更新情報_テーブル区分 TODO
				sbSQL.append(" ,INF_TRANKBN");	// 更新情報_処理区分 	 TODO
				sbSQL.append(" ,");
			}
			if(TblType.CSV.getVal()==tbl.getVal()){
				sbSQL.append("  SEQ");			// F1 : SEQ
				sbSQL.append(" ,INPUTNO");		// F2 : 入力番号
				sbSQL.append(" ,ERRCD");		// F3 : エラーコード
				sbSQL.append(" ,ERRFLD");		// F4 : エラー箇所
				sbSQL.append(" ,ERRVL");		// F5 : エラー値
				sbSQL.append(" ,ERRTBLNM");		// F6 : エラーテーブル名
				sbSQL.append(" ,CSV_UPDKBN");	// F7 : CSV登録区分
				sbSQL.append(" ,KETAKBN");		// F8 : 桁指定
				sbSQL.append(" ,");
			}
			sbSQL.append(" TITKNNO");		// 提案No
			sbSQL.append(" ,TITSTCD");		// 商品ステータス
			sbSQL.append(" ,SHNCD");		// F1 : 商品コード
			sbSQL.append(" ,YOYAKUDT");		// F2 : マスタ変更予定日
			sbSQL.append(" ,TENBAIKADT");	// F3 : 店売価実施日
			sbSQL.append(" ,YOT_BMNCD");	// F4 : 用途分類コード_部門
			sbSQL.append(" ,YOT_DAICD");	// F5 : 用途分類コード_大
			sbSQL.append(" ,YOT_CHUCD");	// F6 : 用途分類コード_中
			sbSQL.append(" ,YOT_SHOCD");	// F7 : 用途分類コード_小
			sbSQL.append(" ,URI_BMNCD");	// F8 : 売場分類コード_部門
			sbSQL.append(" ,URI_DAICD");	// F9 : 売場分類コード_大
			sbSQL.append(" ,URI_CHUCD");	// F10: 売場分類コード_中
			sbSQL.append(" ,URI_SHOCD");	// F11: 売場分類コード_小
			sbSQL.append(" ,BMNCD");		// F12: 標準分類コード_部門
			sbSQL.append(" ,DAICD");		// F13: 標準分類コード_大
			sbSQL.append(" ,CHUCD");		// F14: 標準分類コード_中
			sbSQL.append(" ,SHOCD");		// F15: 標準分類コード_小
			sbSQL.append(" ,SSHOCD");		// F16: 標準分類コード_小小
			sbSQL.append(" ,ATSUK_STDT");	// F17: 取扱期間_開始日
			sbSQL.append(" ,ATSUK_EDDT");	// F18: 取扱期間_終了日
			sbSQL.append(" ,TEISHIKBN");	// F19: 取扱停止
			if(TblType.JNL.getVal()==tbl.getVal()){
				sbSQL.append(" ,SHOHINAN");		// F20: 商品名（カナ）
				sbSQL.append(" ,SHOHINKN");		// F21: 商品名（漢字）
			}else{
				sbSQL.append(" ,SHNAN");		// F20: 商品名（カナ）
				sbSQL.append(" ,SHNKN");		// F21: 商品名（漢字）
			}
			sbSQL.append(" ,PCARDKN");		// F22: プライスカード商品名称（漢字）
			sbSQL.append(" ,POPKN");		// F23: POP名称
			sbSQL.append(" ,RECEIPTAN");	// F24: レシート名（カナ）
			sbSQL.append(" ,RECEIPTKN");	// F25: レシート名（漢字）
			sbSQL.append(" ,PCKBN");		// F26: PC区分
			sbSQL.append(" ,KAKOKBN");		// F27: 加工区分
			sbSQL.append(" ,ICHIBAKBN");	// F28: 市場区分
			sbSQL.append(" ,SHNKBN");		// F29: 商品種類
			sbSQL.append(" ,SANCHIKN");		// F30: 産地
			sbSQL.append(" ,SSIRCD");		// F31: 標準仕入先コード
			sbSQL.append(" ,HSPTN");		// F32: 配送パターン
			sbSQL.append(" ,RG_ATSUKFLG");	// F33: レギュラー情報_取扱フラグ
			sbSQL.append(" ,RG_GENKAAM");	// F34: レギュラー情報_原価
			sbSQL.append(" ,RG_BAIKAAM");	// F35: レギュラー情報_売価
			sbSQL.append(" ,RG_IRISU");		// F36: レギュラー情報_店入数
			sbSQL.append(" ,RG_IDENFLG");	// F37: レギュラー情報_一括伝票フラグ
			sbSQL.append(" ,RG_WAPNFLG");	// F38: レギュラー情報_ワッペン
			sbSQL.append(" ,HS_ATSUKFLG");	// F39: 販促情報_取扱フラグ
			sbSQL.append(" ,HS_GENKAAM");	// F40: 販促情報_原価
			sbSQL.append(" ,HS_BAIKAAM");	// F41: 販促情報_売価
			sbSQL.append(" ,HS_IRISU");		// F42: 販促情報_店入数
			sbSQL.append(" ,HS_WAPNFLG");	// F43: 販促情報_ワッペン
			sbSQL.append(" ,HS_SPOTMINSU");	// F44: 販促情報_スポット最低発注数
			sbSQL.append(" ,HP_SWAPNFLG");	// F45: 販促情報_特売ワッペン
			sbSQL.append(" ,KIKKN");		// F46: 規格名称
			sbSQL.append(" ,UP_YORYOSU");	// F47: ユニットプライス_容量
			sbSQL.append(" ,UP_TYORYOSU");	// F48: ユニットプライス_単位容量
			sbSQL.append(" ,UP_TANIKBN");	// F49: ユニットプライス_ユニット単位
			if(TblType.JNL.getVal()==tbl.getVal()){
				sbSQL.append(" ,SHN_YOKOSZ");	// F50: 商品サイズ_横
				sbSQL.append(" ,SHN_TATESZ");	// F51: 商品サイズ_縦
				sbSQL.append(" ,SHN_OKUSZ");	// F52: 商品サイズ_奥行
				sbSQL.append(" ,SHN_JRYOSZ");	// F53: 商品サイズ_重量
			}else{
				sbSQL.append(" ,SHNYOKOSZ");	// F50: 商品サイズ_横
				sbSQL.append(" ,SHNTATESZ");	// F51: 商品サイズ_縦
				sbSQL.append(" ,SHNOKUSZ");		// F52: 商品サイズ_奥行
				sbSQL.append(" ,SHNJRYOSZ");	// F53: 商品サイズ_重量
			}
			sbSQL.append(" ,PBKBN");		// F54: PB区分
			sbSQL.append(" ,KOMONOKBM");	// F55: 小物区分
			sbSQL.append(" ,TANAOROKBN");	// F56: 棚卸区分
			sbSQL.append(" ,TEIKEIKBN");	// F57: 定計区分
			sbSQL.append(" ,ODS_HARUSU");	// F58: ODS_賞味期限_春
			sbSQL.append(" ,ODS_NATSUSU");	// F59: ODS_賞味期限_夏
			sbSQL.append(" ,ODS_AKISU");	// F60: ODS_賞味期限_秋
			sbSQL.append(" ,ODS_FUYUSU");	// F61: ODS_賞味期限_冬
			sbSQL.append(" ,ODS_NYUKASU");	// F62: ODS_入荷期限
			sbSQL.append(" ,ODS_NEBIKISU");	// F63: ODS_値引期限
			sbSQL.append(" ,PCARD_SHUKBN");	// F64: プライスカード_種類
			sbSQL.append(" ,PCARD_IROKBN");	// F65: プライスカード_色
			sbSQL.append(" ,ZEIKBN");		// F66: 税区分
			sbSQL.append(" ,ZEIRTKBN");		// F67: 税率区分
			sbSQL.append(" ,ZEIRTKBN_OLD");	// F68: 旧税率区分
			sbSQL.append(" ,ZEIRTHENKODT");	// F69: 税率変更日
			sbSQL.append(" ,SEIZOGENNISU");	// F70: 製造限度日数
			sbSQL.append(" ,TEIKANKBN");	// F71: 定貫不定貫区分
			sbSQL.append(" ,MAKERCD");		// F72: メーカーコード
			sbSQL.append(" ,IMPORTKBN");	// F73: 輸入区分
			sbSQL.append(" ,SIWAKEKBN");	// F74: 仕分区分
			sbSQL.append(" ,HENPIN_KBN");	// F75: 返品区分
			sbSQL.append(" ,TAISHONENSU");	// F76: 対象年齢
			sbSQL.append(" ,CALORIESU");	// F77: カロリー表示
			sbSQL.append(" ,ELPFLG");		// F78: フラグ情報_ELP
			sbSQL.append(" ,BELLMARKFLG");	// F79: フラグ情報_ベルマーク
			sbSQL.append(" ,RECYCLEFLG");	// F80: フラグ情報_リサイクル
			sbSQL.append(" ,ECOFLG");		// F81: フラグ情報_エコマーク
			sbSQL.append(" ,HZI_YOTO");		// F82: 包材用途
			sbSQL.append(" ,HZI_ZAISHITU");	// F83: 包材材質
			sbSQL.append(" ,HZI_RECYCLE");	// F84: 包材リサイクル対象
			sbSQL.append(" ,KIKANKBN");		// F85: 期間
			sbSQL.append(" ,SHUKYUKBN");	// F86: 酒級
			sbSQL.append(" ,DOSU");			// F87: 度数
			sbSQL.append(" ,CHINRETUCD");	// F88: 陳列形式コード
			sbSQL.append(" ,DANTUMICD");	// F89: 段積み形式コード
			sbSQL.append(" ,KASANARICD");	// F90: 重なりコード
			sbSQL.append(" ,KASANARISZ");	// F91: 重なりサイズ
			sbSQL.append(" ,ASSHUKURT");	// F92: 圧縮率
			sbSQL.append(" ,SHUBETUCD");	// F93: 種別コード
			sbSQL.append(" ,URICD");		// F94: 販売コード
			sbSQL.append(" ,SALESCOMKN");	// F95: 商品コピー・セールスコメント
			sbSQL.append(" ,URABARIKBN");	// F96: 裏貼
			sbSQL.append(" ,PCARD_OPFLG");	// F97: プライスカード出力有無
			sbSQL.append(" ,PARENTCD");		// F98: 親商品コード
			sbSQL.append(" ,BINKBN");		// F99: 便区分
			sbSQL.append(" ,HAT_MONKBN");	// F100: 発注曜日_月
			sbSQL.append(" ,HAT_TUEKBN");	// F101: 発注曜日_火
			sbSQL.append(" ,HAT_WEDKBN");	// F102: 発注曜日_水
			sbSQL.append(" ,HAT_THUKBN");	// F103: 発注曜日_木
			sbSQL.append(" ,HAT_FRIKBN");	// F104: 発注曜日_金
			sbSQL.append(" ,HAT_SATKBN");	// F105: 発注曜日_土
			sbSQL.append(" ,HAT_SUNKBN");	// F106: 発注曜日_日
			sbSQL.append(" ,READTMPTN");	// F107: リードタイムパターン
			sbSQL.append(" ,SIMEKAISU");	// F108: 締め回数
			sbSQL.append(" ,IRYOREFLG");	// F109: 衣料使い回しフラグ
			sbSQL.append(" ,TOROKUMOTO");	// F110: 登録元
			sbSQL.append(" ,UPDKBN");		// F111: 更新区分
			if(TblType.CSV.getVal()!=tbl.getVal()){
				sbSQL.append(" ,SENDFLG");		// F112: 送信フラグ
			}
			sbSQL.append(" ,OPERATOR");		// F113: オペレータ
			if(TblType.CSV.getVal()!=tbl.getVal()){
				sbSQL.append(" ,ADDDT");		// F114: 登録日
			}
			sbSQL.append(" ,UPDDT");		// F115: 更新日
			sbSQL.append(" ,K_HONKB");		// F116: 保温区分
			sbSQL.append(" ,K_WAPNFLG_R");	// F117: デリカワッペン区分_レギュラー
			sbSQL.append(" ,K_WAPNFLG_H");	// F118: デリカワッペン区分_販促
			sbSQL.append(" ,K_TORIKB");		// F119: 取扱区分
			sbSQL.append(" ,ITFCD");		// F120: ITFコード
			sbSQL.append(" ,CENTER_IRISU");	// F121: センター入数

			sbSQL.append(") values(");
			if(TblType.JNL.getVal()==tbl.getVal()){
				sbSQL.append("  RE.SEQ");			// SEQ
				sbSQL.append(" ,current timestamp");		// 更新情報_更新日時
				sbSQL.append(" ,RE.INF_OPERATOR");	// 更新情報_オペレータ
				sbSQL.append(" ,RE.INF_TABLEKBN");	// 更新情報_テーブル区分 TODO
				sbSQL.append(" ,RE.INF_TRANKBN");	// 更新情報_処理区分 	 TODO
				sbSQL.append(" ,");
			}
			if(TblType.CSV.getVal()==tbl.getVal()){
				sbSQL.append("  RE.SEQ");			// F1 : SEQ
				sbSQL.append(" ,RE.INPUTNO");		// F2 : 入力番号
				sbSQL.append(" ,RE.ERRCD");			// F3 : エラーコード
				sbSQL.append(" ,RE.ERRFLD");		// F4 : エラー箇所
				sbSQL.append(" ,RE.ERRVL");			// F5 : エラー値
				sbSQL.append(" ,RE.ERRTBLNM");		// F6 : エラーテーブル名
				sbSQL.append(" ,RE.CSV_UPDKBN");	// F7 : CSV登録区分
				sbSQL.append(" ,RE.KETAKBN");		// F8 : 桁指定
				sbSQL.append(" ,");
			}
			sbSQL.append("  RE.TITKNNO");
			sbSQL.append(" ,RE.TITSTCD");
			sbSQL.append(" ,RE.SHNCD");
			sbSQL.append(" ,RE.YOYAKUDT");
			sbSQL.append(" ,RE.TENBAIKADT");
			sbSQL.append(" ,RE.YOT_BMNCD");
			sbSQL.append(" ,RE.YOT_DAICD");
			sbSQL.append(" ,RE.YOT_CHUCD");
			sbSQL.append(" ,RE.YOT_SHOCD");
			sbSQL.append(" ,RE.URI_BMNCD");
			sbSQL.append(" ,RE.URI_DAICD");
			sbSQL.append(" ,RE.URI_CHUCD");
			sbSQL.append(" ,RE.URI_SHOCD");
			sbSQL.append(" ,RE.BMNCD");
			sbSQL.append(" ,RE.DAICD");
			sbSQL.append(" ,RE.CHUCD");
			sbSQL.append(" ,RE.SHOCD");
			sbSQL.append(" ,RE.SSHOCD");
			sbSQL.append(" ,RE.ATSUK_STDT");
			sbSQL.append(" ,RE.ATSUK_EDDT");
			sbSQL.append(" ,RE.TEISHIKBN");
			sbSQL.append(" ,RE.SHNAN");
			sbSQL.append(" ,RE.SHNKN");
			sbSQL.append(" ,RE.PCARDKN");
			sbSQL.append(" ,RE.POPKN");
			sbSQL.append(" ,RE.RECEIPTAN");
			sbSQL.append(" ,RE.RECEIPTKN");
			sbSQL.append(" ,RE.PCKBN");
			sbSQL.append(" ,RE.KAKOKBN");
			sbSQL.append(" ,RE.ICHIBAKBN");
			sbSQL.append(" ,RE.SHNKBN");
			sbSQL.append(" ,RE.SANCHIKN");
			sbSQL.append(" ,RE.SSIRCD");
			sbSQL.append(" ,RE.HSPTN");
			sbSQL.append(" ,RE.RG_ATSUKFLG");
			sbSQL.append(" ,RE.RG_GENKAAM");
			sbSQL.append(" ,RE.RG_BAIKAAM");
			sbSQL.append(" ,RE.RG_IRISU");
			sbSQL.append(" ,RE.RG_IDENFLG");
			sbSQL.append(" ,RE.RG_WAPNFLG");
			sbSQL.append(" ,RE.HS_ATSUKFLG");
			sbSQL.append(" ,RE.HS_GENKAAM");
			sbSQL.append(" ,RE.HS_BAIKAAM");
			sbSQL.append(" ,RE.HS_IRISU");
			sbSQL.append(" ,RE.HS_WAPNFLG");
			sbSQL.append(" ,RE.HS_SPOTMINSU");
			sbSQL.append(" ,RE.HP_SWAPNFLG");
			sbSQL.append(" ,RE.KIKKN");
			sbSQL.append(" ,RE.UP_YORYOSU");
			sbSQL.append(" ,RE.UP_TYORYOSU");
			sbSQL.append(" ,RE.UP_TANIKBN");
			sbSQL.append(" ,RE.SHNYOKOSZ");
			sbSQL.append(" ,RE.SHNTATESZ");
			sbSQL.append(" ,RE.SHNOKUSZ");
			sbSQL.append(" ,RE.SHNJRYOSZ");
			sbSQL.append(" ,RE.PBKBN");
			sbSQL.append(" ,RE.KOMONOKBM");
			sbSQL.append(" ,RE.TANAOROKBN");
			sbSQL.append(" ,RE.TEIKEIKBN");
			sbSQL.append(" ,RE.ODS_HARUSU");
			sbSQL.append(" ,RE.ODS_NATSUSU");
			sbSQL.append(" ,RE.ODS_AKISU");
			sbSQL.append(" ,RE.ODS_FUYUSU");
			sbSQL.append(" ,RE.ODS_NYUKASU");
			sbSQL.append(" ,RE.ODS_NEBIKISU");
			sbSQL.append(" ,RE.PCARD_SHUKBN");
			sbSQL.append(" ,RE.PCARD_IROKBN");
			sbSQL.append(" ,RE.ZEIKBN");
			sbSQL.append(" ,RE.ZEIRTKBN");
			sbSQL.append(" ,RE.ZEIRTKBN_OLD");
			sbSQL.append(" ,RE.ZEIRTHENKODT");
			sbSQL.append(" ,RE.SEIZOGENNISU");
			sbSQL.append(" ,RE.TEIKANKBN");
			sbSQL.append(" ,RE.MAKERCD");
			sbSQL.append(" ,RE.IMPORTKBN");
			sbSQL.append(" ,RE.SIWAKEKBN");
			sbSQL.append(" ,RE.HENPIN_KBN");
			sbSQL.append(" ,RE.TAISHONENSU");
			sbSQL.append(" ,RE.CALORIESU");
			sbSQL.append(" ,RE.ELPFLG");
			sbSQL.append(" ,RE.BELLMARKFLG");
			sbSQL.append(" ,RE.RECYCLEFLG");
			sbSQL.append(" ,RE.ECOFLG");
			sbSQL.append(" ,RE.HZI_YOTO");
			sbSQL.append(" ,RE.HZI_ZAISHITU");
			sbSQL.append(" ,RE.HZI_RECYCLE");
			sbSQL.append(" ,RE.KIKANKBN");
			sbSQL.append(" ,RE.SHUKYUKBN");
			sbSQL.append(" ,RE.DOSU");
			sbSQL.append(" ,RE.CHINRETUCD");
			sbSQL.append(" ,RE.DANTUMICD");
			sbSQL.append(" ,RE.KASANARICD");
			sbSQL.append(" ,RE.KASANARISZ");
			sbSQL.append(" ,RE.ASSHUKURT");
			sbSQL.append(" ,RE.SHUBETUCD");
			sbSQL.append(" ,RE.URICD");
			sbSQL.append(" ,RE.SALESCOMKN");
			sbSQL.append(" ,RE.URABARIKBN");
			sbSQL.append(" ,RE.PCARD_OPFLG");
			sbSQL.append(" ,RE.PARENTCD");
			sbSQL.append(" ,RE.BINKBN");
			sbSQL.append(" ,RE.HAT_MONKBN");
			sbSQL.append(" ,RE.HAT_TUEKBN");
			sbSQL.append(" ,RE.HAT_WEDKBN");
			sbSQL.append(" ,RE.HAT_THUKBN");
			sbSQL.append(" ,RE.HAT_FRIKBN");
			sbSQL.append(" ,RE.HAT_SATKBN");
			sbSQL.append(" ,RE.HAT_SUNKBN");
			sbSQL.append(" ,RE.READTMPTN");
			sbSQL.append(" ,RE.SIMEKAISU");
			sbSQL.append(" ,RE.IRYOREFLG");
			sbSQL.append(" ,RE.TOROKUMOTO");
			sbSQL.append(" ,RE.UPDKBN");
			if(TblType.CSV.getVal()!=tbl.getVal()){
				sbSQL.append(" ,RE.SENDFLG");	// F112: 送信フラグ
			}
			sbSQL.append(" ,RE.OPERATOR");		// F113: オペレータ
			if(TblType.CSV.getVal()!=tbl.getVal()){
				sbSQL.append(" ,current timestamp");		// F114: 登録日
			}
			sbSQL.append(" ,current timestamp");			// F115: 更新日
			sbSQL.append(" ,RE.K_HONKB");		// F116: 保温区分
			sbSQL.append(" ,RE.K_WAPNFLG_R");	// F117: デリカワッペン区分_レギュラー
			sbSQL.append(" ,RE.K_WAPNFLG_H");	// F118: デリカワッペン区分_販促
			sbSQL.append(" ,RE.K_TORIKB");		// F119: 取扱区分
			sbSQL.append(" ,RE.ITFCD");			// F120: ITFコード
			sbSQL.append(" ,RE.CENTER_IRISU");	// F121: センター入数

			sbSQL.append(" )");
		}
		if(SqlType.UPD.getVal() == sql.getVal()||SqlType.MRG.getVal() == sql.getVal()){
			sbSQL.append(" when matched then ");
			sbSQL.append(" update set");
			sbSQL.append(" TITKNNO=RE.TITKNNO");		// 提案No
			sbSQL.append(" ,TITSTCD=RE.TITSTCD");		// 商品ステータス
			sbSQL.append(" ,YOYAKUDT=RE.YOYAKUDT");			// マスタ変更予定日
			sbSQL.append(" ,TENBAIKADT=RE.TENBAIKADT");		// 店売価実施日
			sbSQL.append(" ,YOT_BMNCD=RE.YOT_BMNCD");		// 用途分類コード_部門
			sbSQL.append(" ,YOT_DAICD=RE.YOT_DAICD");		// 用途分類コード_大
			sbSQL.append(" ,YOT_CHUCD=RE.YOT_CHUCD");		// 用途分類コード_中
			sbSQL.append(" ,YOT_SHOCD=RE.YOT_SHOCD");		// 用途分類コード_小
			sbSQL.append(" ,URI_BMNCD=RE.URI_BMNCD");		// 売場分類コード_部門
			sbSQL.append(" ,URI_DAICD=RE.URI_DAICD");		// 売場分類コード_大
			sbSQL.append(" ,URI_CHUCD=RE.URI_CHUCD");		// 売場分類コード_中
			sbSQL.append(" ,URI_SHOCD=RE.URI_SHOCD");		// 売場分類コード_小
			sbSQL.append(" ,BMNCD=RE.BMNCD");				// 標準分類コード_部門
			sbSQL.append(" ,DAICD=RE.DAICD");				// 標準分類コード_大
			sbSQL.append(" ,CHUCD=RE.CHUCD");				// 標準分類コード_中
			sbSQL.append(" ,SHOCD=RE.SHOCD");				// 標準分類コード_小
			sbSQL.append(" ,SSHOCD=RE.SSHOCD");				// 標準分類コード_小小
			sbSQL.append(" ,ATSUK_STDT=RE.ATSUK_STDT");		// 取扱期間_開始日
			sbSQL.append(" ,ATSUK_EDDT=RE.ATSUK_EDDT");		// 取扱期間_終了日
			sbSQL.append(" ,TEISHIKBN=RE.TEISHIKBN");		// 取扱停止
			sbSQL.append(" ,SHNAN=RE.SHNAN");				// 商品名（カナ）
			sbSQL.append(" ,SHNKN=RE.SHNKN");				// 商品名（漢字）
			sbSQL.append(" ,PCARDKN=RE.PCARDKN");			// プライスカード商品名称（漢字）
			sbSQL.append(" ,POPKN=RE.POPKN");				// POP名称
			sbSQL.append(" ,RECEIPTAN=RE.RECEIPTAN");		// レシート名（カナ）
			sbSQL.append(" ,RECEIPTKN=RE.RECEIPTKN");		// レシート名（漢字）
			sbSQL.append(" ,PCKBN=RE.PCKBN");				// PC区分
			sbSQL.append(" ,KAKOKBN=RE.KAKOKBN");			// 加工区分
			sbSQL.append(" ,ICHIBAKBN=RE.ICHIBAKBN");		// 市場区分
			sbSQL.append(" ,SHNKBN=RE.SHNKBN");				// 商品種類
			sbSQL.append(" ,SANCHIKN=RE.SANCHIKN");			// 産地
			sbSQL.append(" ,SSIRCD=RE.SSIRCD");				// 標準仕入先コード
			sbSQL.append(" ,HSPTN=RE.HSPTN");				// 配送パターン
			sbSQL.append(" ,RG_ATSUKFLG=RE.RG_ATSUKFLG");	// レギュラー情報_取扱フラグ
			sbSQL.append(" ,RG_GENKAAM=RE.RG_GENKAAM");		// レギュラー情報_原価
			sbSQL.append(" ,RG_BAIKAAM=RE.RG_BAIKAAM");		// レギュラー情報_売価
			sbSQL.append(" ,RG_IRISU=RE.RG_IRISU");			// レギュラー情報_店入数
			sbSQL.append(" ,RG_IDENFLG=RE.RG_IDENFLG");		// レギュラー情報_一括伝票フラグ
			sbSQL.append(" ,RG_WAPNFLG=RE.RG_WAPNFLG");		// レギュラー情報_ワッペン
			sbSQL.append(" ,HS_ATSUKFLG=RE.HS_ATSUKFLG");	// 販促情報_取扱フラグ
			sbSQL.append(" ,HS_GENKAAM=RE.HS_GENKAAM");		// 販促情報_原価
			sbSQL.append(" ,HS_BAIKAAM=RE.HS_BAIKAAM");		// 販促情報_売価
			sbSQL.append(" ,HS_IRISU=RE.HS_IRISU");			// 販促情報_店入数
			sbSQL.append(" ,HS_WAPNFLG=RE.HS_WAPNFLG");		// 販促情報_ワッペン
			sbSQL.append(" ,HS_SPOTMINSU=RE.HS_SPOTMINSU");	// 販促情報_スポット最低発注数
			sbSQL.append(" ,HP_SWAPNFLG=RE.HP_SWAPNFLG");	// 販促情報_特売ワッペン
			sbSQL.append(" ,KIKKN=RE.KIKKN");				// 規格名称
			sbSQL.append(" ,UP_YORYOSU=RE.UP_YORYOSU");		// ユニットプライス_容量
			sbSQL.append(" ,UP_TYORYOSU=RE.UP_TYORYOSU");	// ユニットプライス_単位容量
			sbSQL.append(" ,UP_TANIKBN=RE.UP_TANIKBN");		// ユニットプライス_ユニット単位
			sbSQL.append(" ,SHNYOKOSZ=RE.SHNYOKOSZ");		// 商品サイズ_横
			sbSQL.append(" ,SHNTATESZ=RE.SHNTATESZ");		// 商品サイズ_縦
			sbSQL.append(" ,SHNOKUSZ=RE.SHNOKUSZ");			// 商品サイズ_奥行
			sbSQL.append(" ,SHNJRYOSZ=RE.SHNJRYOSZ");		// 商品サイズ_重量
			sbSQL.append(" ,PBKBN=RE.PBKBN");				// PB区分
			sbSQL.append(" ,KOMONOKBM=RE.KOMONOKBM");		// 小物区分
			sbSQL.append(" ,TANAOROKBN=RE.TANAOROKBN");		// 棚卸区分
			sbSQL.append(" ,TEIKEIKBN=RE.TEIKEIKBN");		// 定計区分
			sbSQL.append(" ,ODS_HARUSU=RE.ODS_HARUSU");		// ODS_賞味期限_春
			sbSQL.append(" ,ODS_NATSUSU=RE.ODS_NATSUSU");	// ODS_賞味期限_夏
			sbSQL.append(" ,ODS_AKISU=RE.ODS_AKISU");		// ODS_賞味期限_秋
			sbSQL.append(" ,ODS_FUYUSU=RE.ODS_FUYUSU");		// ODS_賞味期限_冬
			sbSQL.append(" ,ODS_NYUKASU=RE.ODS_NYUKASU");	// ODS_入荷期限
			sbSQL.append(" ,ODS_NEBIKISU=RE.ODS_NEBIKISU");	// ODS_値引期限
			sbSQL.append(" ,PCARD_SHUKBN=RE.PCARD_SHUKBN");	// プライスカード_種類
			sbSQL.append(" ,PCARD_IROKBN=RE.PCARD_IROKBN");	// プライスカード_色
			sbSQL.append(" ,ZEIKBN=RE.ZEIKBN");				// 税区分
			sbSQL.append(" ,ZEIRTKBN=RE.ZEIRTKBN");			// 税率区分
			sbSQL.append(" ,ZEIRTKBN_OLD=RE.ZEIRTKBN_OLD");	// 旧税率区分
			sbSQL.append(" ,ZEIRTHENKODT=RE.ZEIRTHENKODT");	// 税率変更日
			sbSQL.append(" ,SEIZOGENNISU=RE.SEIZOGENNISU");	// 製造限度日数
			sbSQL.append(" ,TEIKANKBN=RE.TEIKANKBN");		// 定貫不定貫区分
			sbSQL.append(" ,MAKERCD=RE.MAKERCD");			// メーカーコード
			sbSQL.append(" ,IMPORTKBN=RE.IMPORTKBN");		// 輸入区分
			sbSQL.append(" ,SIWAKEKBN=RE.SIWAKEKBN");		// 仕分区分
			sbSQL.append(" ,HENPIN_KBN=RE.HENPIN_KBN");		// 返品区分
			sbSQL.append(" ,TAISHONENSU=RE.TAISHONENSU");	// 対象年齢
			sbSQL.append(" ,CALORIESU=RE.CALORIESU");		// カロリー表示
			sbSQL.append(" ,ELPFLG=RE.ELPFLG");				// フラグ情報_ELP
			sbSQL.append(" ,BELLMARKFLG=RE.BELLMARKFLG");	// フラグ情報_ベルマーク
			sbSQL.append(" ,RECYCLEFLG=RE.RECYCLEFLG");		// フラグ情報_リサイクル
			sbSQL.append(" ,ECOFLG=RE.ECOFLG");				// フラグ情報_エコマーク
			sbSQL.append(" ,HZI_YOTO=RE.HZI_YOTO");			// 包材用途
			sbSQL.append(" ,HZI_ZAISHITU=RE.HZI_ZAISHITU");	// 包材材質
			sbSQL.append(" ,HZI_RECYCLE=RE.HZI_RECYCLE");	// 包材リサイクル対象
			sbSQL.append(" ,KIKANKBN=RE.KIKANKBN");			// 期間
			sbSQL.append(" ,SHUKYUKBN=RE.SHUKYUKBN");		// 酒級
			sbSQL.append(" ,DOSU=RE.DOSU");					// 度数
			sbSQL.append(" ,CHINRETUCD=RE.CHINRETUCD");		// 陳列形式コード
			sbSQL.append(" ,DANTUMICD=RE.DANTUMICD");		// 段積み形式コード
			sbSQL.append(" ,KASANARICD=RE.KASANARICD");		// 重なりコード
			sbSQL.append(" ,KASANARISZ=RE.KASANARISZ");		// 重なりサイズ
			sbSQL.append(" ,ASSHUKURT=RE.ASSHUKURT");		// 圧縮率
			sbSQL.append(" ,SHUBETUCD=RE.SHUBETUCD");		// 種別コード
			//sbSQL.append(" ,URICD=RE.URICD");				// 販売コード
			sbSQL.append(" ,SALESCOMKN=RE.SALESCOMKN");		// 商品コピー・セールスコメント
			sbSQL.append(" ,URABARIKBN=RE.URABARIKBN");		// 裏貼
			sbSQL.append(" ,PCARD_OPFLG=RE.PCARD_OPFLG");	// プライスカード出力有無
			sbSQL.append(" ,PARENTCD=RE.PARENTCD");			// 親商品コード
			sbSQL.append(" ,BINKBN=RE.BINKBN");				// 便区分
			sbSQL.append(" ,HAT_MONKBN=RE.HAT_MONKBN");		// 発注曜日_月
			sbSQL.append(" ,HAT_TUEKBN=RE.HAT_TUEKBN");		// 発注曜日_火
			sbSQL.append(" ,HAT_WEDKBN=RE.HAT_WEDKBN");		// 発注曜日_水
			sbSQL.append(" ,HAT_THUKBN=RE.HAT_THUKBN");		// 発注曜日_木
			sbSQL.append(" ,HAT_FRIKBN=RE.HAT_FRIKBN");		// 発注曜日_金
			sbSQL.append(" ,HAT_SATKBN=RE.HAT_SATKBN");		// 発注曜日_土
			sbSQL.append(" ,HAT_SUNKBN=RE.HAT_SUNKBN");		// 発注曜日_日
			sbSQL.append(" ,READTMPTN=RE.READTMPTN");		// リードタイムパターン
			sbSQL.append(" ,SIMEKAISU=RE.SIMEKAISU");		// 締め回数
			sbSQL.append(" ,IRYOREFLG=RE.IRYOREFLG");		// 衣料使い回しフラグ
			sbSQL.append(" ,TOROKUMOTO=RE.TOROKUMOTO");		// F110: 登録元
			sbSQL.append(" ,UPDKBN=RE.UPDKBN");				// F111: 更新区分
			sbSQL.append(" ,SENDFLG=RE.SENDFLG");			// F112: 送信フラグ
			sbSQL.append(" ,OPERATOR=RE.OPERATOR");			// F113: オペレータ
			if(TblType.CSV.getVal()!=tbl.getVal() && DefineReport.Button.CSV_IMPORT_YYK.getObj().equals(btnId)){
				sbSQL.append(" ,ADDDT=current timestamp");				// F114: 登録日
			}
			sbSQL.append(" ,UPDDT=current timestamp");			// F115: 更新日
			sbSQL.append(" ,K_HONKB=RE.K_HONKB");			// F116: 保温区分
			sbSQL.append(" ,K_WAPNFLG_R=RE.K_WAPNFLG_R");	// F117: デリカワッペン区分_レギュラー
			sbSQL.append(" ,K_WAPNFLG_H=RE.K_WAPNFLG_H");	// F118: デリカワッペン区分_販促
			sbSQL.append(" ,K_TORIKB=RE.K_TORIKB");			// F119: 取扱区分
			sbSQL.append(" ,ITFCD=RE.ITFCD");				// F120: ITFコード
			sbSQL.append(" ,CENTER_IRISU=RE.CENTER_IRISU");	// F121: センター入数

		}
		if (DefineReport.ID_DEBUG_MODE)	System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());


		//sqlList.add(sbSQL.toString());
		//prmList.add(prmData);
		sqlCmd = sbSQL.toString();
		cmdParams = prmData;
		lblList.add("商品マスタ" + tbl.getTxt());

		return result;
	}

	/**
	 * SEQ情報取得処理(Web商談)
	 *
	 * @throws Exception
	 */
	public String getSHNCD_SEQ() {
		// 関連情報取得
		ItemList iL = new ItemList();
		String sqlColCommand = "VALUES NEXTVAL FOR INAWS.SEQWS";
		@SuppressWarnings("static-access")
		JSONArray array = iL.selectJSONArray(sqlColCommand, null, Defines.STR_JNDI_DS);
		String value = "";
		if(array.size() > 0){
			value = array.optJSONObject(0).optString("1");
		}
		return value;
	}

	public JSONObject getSqlPartsCmnCommandMSTSHN(TblType tbl, SqlType sql) {
		JSONObject json = new JSONObject();
		//WEB商談
		String szTable = "INAWS.PIMTIT";
		String szWhere = "T.SHNCD = RE.SHNCD";
		if(TblType.YYK.getVal()==tbl.getVal()){
			szTable += "_Y";
			szWhere += " and T.YOYAKUDT = RE.YOYAKUDT ";
		}else if(TblType.JNL.getVal()==tbl.getVal()){
			szTable = "INAAD.JNLSHN";
			szWhere = "T.SEQ = RE.SEQ";
		}else if(TblType.CSV.getVal()==tbl.getVal()){
			szTable = "INAWS.CSVSHN";
			szWhere = "T.SEQ = RE.SEQ and T.INPUTNO = RE.INPUTNO";
		}

		json.put("TABLE", szTable);
		json.put("WHERE", szWhere);
		return json;
	}

	/**
	 * 商品マスタMerge共通SQL作成処理
	 *
	 * @param tbl
	 * @param values
	 * @param names
	 * @param userId
	 *
	 * @throws Exception
	 */
	public String createMergeCmnCommandMSTSHN(TblType tbl, SqlType sql, String values, String names) {
		// 条件文など取得
		JSONObject parts = this.getSqlPartsCmnCommandMSTSHN(tbl, sql);
		String szTable = parts.optString("TABLE");
		String szWhere = parts.optString("WHERE");

		// 基本Merge文
		StringBuffer sbSQL;
		sbSQL = new StringBuffer();
		sbSQL.append("merge into "+szTable+" as T");
		sbSQL.append(" using (select ");
		sbSQL.append("cast(TEIANNO as INTEGER) as TITKNNO");
		sbSQL.append(" ,cast(STATUS as INTEGER) as TITSTCD");
		for(MSTSHNLayout itm :MSTSHNLayout.values()){
			if(itm.equals(MSTSHNLayout.UPDDT)||itm.equals(MSTSHNLayout.ADDDT)){ continue; }
			//if(itm.getNo() > 1){ sbSQL.append(" ,"); }
			sbSQL.append(" ,");
			sbSQL.append("cast("+itm.getId() + " as "+itm.getTyp() + ") as " + itm.getCol());
		}
		if(TblType.JNL.getVal()==tbl.getVal()){
			for(JNLSHNLayout itm :JNLSHNLayout.values()){
				sbSQL.append(" ,cast("+itm.getId2() + " as "+itm.getTyp() + ") as " + itm.getCol());
			}
		}
		if(TblType.CSV.getVal()==tbl.getVal()){
			for(CSVSHNLayout itm :CSVSHNLayout.values()){
				sbSQL.append(" ,cast("+itm.getId2() + " as "+itm.getTyp() + ") as " + itm.getCol());
			}
		}
		sbSQL.append(" from (values"+values+") as T1("+names+")");
		sbSQL.append(" ) as RE on ("+szWhere+") ");
		return sbSQL.toString();
	}


	/**
	 * 仕入グループ商品マスタINSERT/UPDATE SQL作成処理
	 *
	 * @param userId
	 * @param dataArray
	 * @param tbl
	 *
	 * @throws Exception
	 */
	public JSONObject createSqlMSTSIRGPSHN(String userId, String btnId, JSONArray dataArray, TblType tbl, SqlType sql){
		JSONObject result = new JSONObject();
		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();
		String values = "", names = "", rows = "";

		int colNum = MSTSIRGPSHNLayout.values().length;		// テーブル列数
		if(TblType.JNL.getVal()==tbl.getVal()){
			colNum+=JNLCMNLayout.values().length;
		}
		if(TblType.CSV.getVal()==tbl.getVal()){
			colNum+=CSVCMNLayout.values().length;
		}
		for(int j=0; j < dataArray.size(); j++){
			values = "";
			names = "";
			for (int i = 1; i <= colNum; i++) {
				String col = "F" + i;
				String val = dataArray.optJSONObject(j).optString(col);

				if(i==MSTSIRGPSHNLayout.SENDFLG.getNo()){				// 送信フラグ
					val = "0";
				}else if(i==MSTSIRGPSHNLayout.OPERATOR.getNo()){		// オペレータ
					val = userId;
				}
				if(TblType.JNL.getVal()==tbl.getVal()){
					if(i==MSTSIRGPSHNLayout.values().length+1){			// SEQ(ジャーナル用)
						val = jnlshn_seq;
					}else if(i==MSTSIRGPSHNLayout.values().length+2){	// RENNO(ジャーナル用)
						val = (j+1)+"";
					}
				}
				// CSV特殊情報設定
				if(TblType.CSV.getVal()==tbl.getVal()){
					// SEQ以外dataArrayに追加済み前提
					if(i==MSTSIRGPSHNLayout.values().length+1){			// SEQ(CSV用)
						val = csvshn_seq;
					}
				}

				if(StringUtils.isEmpty(val)){
					values += ", null";
				}else{
					if(isTest){
						values += ",'"+val+"'";
					}else{
						prmData.add(val);
						values += ", cast(? as varchar("+MessageUtility.getDefByteLen(val)+"))";
					}
				}
				names  += ","+col;
			}
			rows += ",("+StringUtils.removeStart(values, ",")+")";
		}
		rows = StringUtils.removeStart(rows, ",");
		names  = StringUtils.removeStart(names, ",");

		StringBuffer sbSQL;
		sbSQL = new StringBuffer();
		sbSQL.append(this.createMergeCmnCommandMSTSIRGPSHN(tbl, sql, rows, names));
		if(SqlType.INS.getVal() == sql.getVal()){
			sbSQL.append(" when not matched then ");
			sbSQL.append(" insert (");
			if(TblType.JNL.getVal()==tbl.getVal()){
				sbSQL.append("  SEQ");							// SEQ
				sbSQL.append(" ,RENNO");							// RENNO
				sbSQL.append(" ,");
			}
			if(TblType.CSV.getVal()==tbl.getVal()){
				sbSQL.append("  SEQ");			// F1 : SEQ
				sbSQL.append(" ,INPUTNO");		// F2 : 入力番号
				sbSQL.append(" ,INPUTEDANO");	// F3 : 入力枝番
				sbSQL.append(" ,");
			}
			sbSQL.append("  SHNCD");		// F1 : 商品コード
			sbSQL.append(" ,TENGPCD");		// F2 : 店グループ
			sbSQL.append(" ,YOYAKUDT");		// F3 : マスタ変更予定日
			sbSQL.append(" ,AREAKBN");		// F4 : エリア区分
			sbSQL.append(" ,SIRCD");		// F5 : 仕入先コード
			sbSQL.append(" ,HSPTN");		// F6 : 配送パターン
			sbSQL.append(" ,SENDFLG");		// F7 : 送信フラグ
			sbSQL.append(" ,OPERATOR");		// F8 : オペレータ
			sbSQL.append(" ,ADDDT");		// F9 : 登録日
			sbSQL.append(" ,UPDDT");		// F10: 更新日

			sbSQL.append(") values(");
			if(TblType.JNL.getVal()==tbl.getVal()){
				sbSQL.append("  RE.SEQ");							// SEQ
				sbSQL.append(" ,RE.RENNO");							// RENNO
				sbSQL.append(" ,");
			}
			if(TblType.CSV.getVal()==tbl.getVal()){
				sbSQL.append("  RE.SEQ");			// F1 : SEQ
				sbSQL.append(" ,RE.INPUTNO");		// F2 : 入力番号
				sbSQL.append(" ,RE.INPUTEDANO");	// F3 : 入力枝番
				sbSQL.append(" ,");
			}
			sbSQL.append("  RE.SHNCD");			// F1 : 商品コード
			sbSQL.append(" ,RE.TENGPCD");		// F2 : 店グループ
			sbSQL.append(" ,RE.YOYAKUDT");		// F3 : マスタ変更予定日
			sbSQL.append(" ,RE.AREAKBN");		// F4 : エリア区分
			sbSQL.append(" ,RE.SIRCD");			// F5 : 仕入先コード
			sbSQL.append(" ,RE.HSPTN");			// F6 : 配送パターン
			sbSQL.append(" ,RE.SENDFLG");		// F7 : 送信フラグ
			sbSQL.append(" ,RE.OPERATOR");		// F8 : オペレータ
			sbSQL.append(" ,current timestamp");			// F9 : 登録日
			sbSQL.append(" ,current timestamp");			// F10: 更新日
			sbSQL.append(" )");
		}
		if(SqlType.DEL.getVal() == sql.getVal()){
			sbSQL.append(" when matched then delete");
		}
		if (DefineReport.ID_DEBUG_MODE)	System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("仕入グループ商品マスタ" + tbl.getTxt());
		return result;
	}

	/**
	 * 仕入グループ商品マスタMergeSQL作成処理
	 *
	 * @param tbl
	 * @param sql
	 * @param values
	 * @param names
	 * @param userId
	 *
	 * @throws Exception
	 */
	public String createMergeCmnCommandMSTSIRGPSHN(TblType tbl, SqlType sql, String values, String names) {

		String szTable = "INAWS.PIMTISIRGPSHN";
		String szWhere = "T.SHNCD = RE.SHNCD";
		if(SqlType.DEL.getVal()!=sql.getVal()){
			szWhere += " and T.TENGPCD = RE.TENGPCD";
		}
		if(TblType.YYK.getVal()==tbl.getVal()){
			szTable += "_Y";
			szWhere += " and T.YOYAKUDT = RE.YOYAKUDT ";
		}else if(TblType.JNL.getVal()==tbl.getVal()){
			szTable = "INAAD.JNLSIRSHN";
			szWhere = "T.SEQ = RE.SEQ";
		}else if(TblType.CSV.getVal()==tbl.getVal()){
			szTable = "INAWS.CSVSIRSHN";
			szWhere = "T.SEQ = RE.SEQ and T.INPUTNO = RE.INPUTNO";
		}

		// 基本Merge文
		StringBuffer sbSQL;
		sbSQL = new StringBuffer();
		sbSQL.append("merge into "+szTable+" as T");
		sbSQL.append(" using (select ");
		if(SqlType.DEL.getVal() ==  sql.getVal()){
			sbSQL.append("  cast("+MSTSIRGPSHNLayout.SHNCD.getId()+" as "+MSTSIRGPSHNLayout.SHNCD.getTyp()+") as "+MSTSIRGPSHNLayout.SHNCD.getCol());			// 商品コード
			sbSQL.append(" ,cast("+MSTSIRGPSHNLayout.YOYAKUDT.getId()+" as "+MSTSIRGPSHNLayout.YOYAKUDT.getTyp()+") as "+MSTSIRGPSHNLayout.YOYAKUDT.getCol());	// マスタ変更予定日
		}else{
			int baseCnt = MSTSIRGPSHNLayout.values().length;
			for(MSTSIRGPSHNLayout itm :MSTSIRGPSHNLayout.values()){
				if(itm.getNo() > 1){ sbSQL.append(" ,"); }
				sbSQL.append("cast("+itm.getId() + " as "+itm.getTyp() + ") as " + itm.getCol());
			}
			if(TblType.JNL.getVal()==tbl.getVal()){
				for(JNLCMNLayout itm :JNLCMNLayout.values()){
					sbSQL.append(" ,cast("+itm.getId2(baseCnt) + " as "+itm.getTyp() + ") as " + itm.getCol());
				}
			}
			if(TblType.CSV.getVal()==tbl.getVal()){
				for(CSVCMNLayout itm :CSVCMNLayout.values()){
					sbSQL.append(" ,cast("+itm.getId2(baseCnt) + " as "+itm.getTyp() + ") as " + itm.getCol());
				}
			}
		}
		sbSQL.append(" from (values"+values+") as T1("+names+")");
		sbSQL.append(" ) as RE on ("+szWhere+") ");

		return sbSQL.toString();
	}

	/**
	 * 売価コントロールマスタINSERT/UPDATE SQL作成処理
	 *
	 * @param userId
	 * @param data
	 * @param tbl
	 *
	 * @throws Exception
	 */
	public JSONObject createSqlMSTBAIKACTL(String userId, String btnId, JSONArray dataArray, TblType tbl, SqlType sql){
		JSONObject result = new JSONObject();

		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();
		String values = "", names = "", rows = "";
		int colNum = MSTBAIKACTLLayout.values().length;		// テーブル列数
		if(TblType.JNL.getVal()==tbl.getVal()){
			colNum+=JNLCMNLayout.values().length;
		}
		if(TblType.CSV.getVal()==tbl.getVal()){
			colNum+=CSVCMNLayout.values().length;
		}
		for(int j=0; j < dataArray.size(); j++){
			values = "";
			names = "";
			for (int i = 1; i <= colNum; i++) {
				String col = "F" + i;
				String val = dataArray.optJSONObject(j).optString(col);

				if(i==MSTBAIKACTLLayout.SENDFLG.getNo()){				// 送信フラグ
					val = DefineReport.Values.SENDFLG_UN.getVal();
				}else if(i==MSTBAIKACTLLayout.OPERATOR.getNo()){		// オペレータ
					val = userId;
				}
				if(TblType.JNL.getVal()==tbl.getVal()){
					if(i==MSTBAIKACTLLayout.values().length+1){			// SEQ(ジャーナル用)
						val = jnlshn_seq;
					}else if(i==MSTBAIKACTLLayout.values().length+2){	// RENNO(ジャーナル用)
						val = (j+1)+"";
					}
				}
				// CSV特殊情報設定
				if(TblType.CSV.getVal()==tbl.getVal()){
					// SEQ以外dataArrayに追加済み前提
					if(i==MSTBAIKACTLLayout.values().length+1){			// SEQ(CSV用)
						val = csvshn_seq;
					}
				}


				if(StringUtils.isEmpty(val)){
					values += ", null";
				}else{
					if(isTest){
						values += ", '"+val+"'";
					}else{
						prmData.add(val);
						values += ", cast(? as varchar("+MessageUtility.getDefByteLen(val)+"))";
					}
				}
				names  += ", "+col;
			}
			rows += ",("+StringUtils.removeStart(values, ",")+")";
		}
		rows = StringUtils.removeStart(rows, ",");
		names  = StringUtils.removeStart(names, ",");

		StringBuffer sbSQL;
		sbSQL = new StringBuffer();
		sbSQL.append(this.createMergeCmnCommandMSTBAIKACTL(tbl, sql, rows, names));
		if(SqlType.INS.getVal() == sql.getVal()){
			sbSQL.append(" when not matched then ");
			sbSQL.append(" insert (");
			if(TblType.JNL.getVal()==tbl.getVal()){
				sbSQL.append("  SEQ");			// SEQ
				sbSQL.append(" ,RENNO");		// RENNO
				sbSQL.append(" ,");
			}
			if(TblType.CSV.getVal()==tbl.getVal()){
				sbSQL.append("  SEQ");			// F1 : SEQ
				sbSQL.append(" ,INPUTNO");		// F2 : 入力番号
				sbSQL.append(" ,INPUTEDANO");	// F3 : 入力枝番
				sbSQL.append(" ,");
			}
			sbSQL.append("  SHNCD");		// F1 : 商品コード
			sbSQL.append(" ,TENGPCD");		// F2 : 店グループ
			sbSQL.append(" ,YOYAKUDT");		// F3 : マスタ変更予定日
			sbSQL.append(" ,AREAKBN");		// F4 : エリア区分
			sbSQL.append(" ,GENKAAM");		// F5 : 原価
			sbSQL.append(" ,BAIKAAM");		// F6 : 売価
			sbSQL.append(" ,IRISU");		// F7 : 店入数
			sbSQL.append(" ,SENDFLG");		// F8 : 送信フラグ
			sbSQL.append(" ,OPERATOR");		// F9 : オペレータ
			sbSQL.append(" ,ADDDT");		// F10: 登録日
			sbSQL.append(" ,UPDDT");		// F11: 更新日
			sbSQL.append(")values(");
			if(TblType.JNL.getVal()==tbl.getVal()){
				sbSQL.append("  RE.SEQ");							// SEQ
				sbSQL.append(" ,RE.RENNO");							// RENNO
				sbSQL.append(" ,");
			}
			if(TblType.CSV.getVal()==tbl.getVal()){
				sbSQL.append("  RE.SEQ");			// F1 : SEQ
				sbSQL.append(" ,RE.INPUTNO");		// F2 : 入力番号
				sbSQL.append(" ,RE.INPUTEDANO");	// F3 : 入力枝番
				sbSQL.append(" ,");
			}
			sbSQL.append("  RE.SHNCD");			// F1 : 商品コード
			sbSQL.append(" ,RE.TENGPCD");		// F2 : 店グループ
			sbSQL.append(" ,RE.YOYAKUDT");		// F3 : マスタ変更予定日
			sbSQL.append(" ,RE.AREAKBN");		// F4 : エリア区分
			sbSQL.append(" ,RE.GENKAAM");		// F5 : 原価
			sbSQL.append(" ,RE.BAIKAAM");		// F6 : 売価
			sbSQL.append(" ,RE.IRISU");			// F7 : 店入数
			sbSQL.append(" ,RE.SENDFLG");		// F8 : 送信フラグ
			sbSQL.append(" ,RE.OPERATOR");		// F9 : オペレータ
			sbSQL.append(" ,current timestamp");			// F10: 登録日
			sbSQL.append(" ,current timestamp");			// F11: 更新日

			sbSQL.append(" )");
		}
		if(SqlType.DEL.getVal() == sql.getVal()){
			sbSQL.append(" when matched then delete");
		}
		if (DefineReport.ID_DEBUG_MODE)	System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("売価コントロールマスタ" + tbl.getTxt());
		return result;
	}

	/**
	 * 売価コントロールマスタMergeSQL作成処理
	 *
	 * @param tbl
	 * @param sql
	 * @param values
	 * @param names
	 * @param userId
	 *
	 * @throws Exception
	 */
	public String createMergeCmnCommandMSTBAIKACTL(TblType tbl, SqlType sql, String values, String names) {

		String szTable = "INAWS.PIMTIBAIKACTL";
		String szWhere = "T.SHNCD = RE.SHNCD";
		if(SqlType.DEL.getVal()!=sql.getVal()){
			szWhere += " and T.TENGPCD = RE.TENGPCD";
		}
		if(TblType.YYK.getVal()==tbl.getVal()){
			szTable += "_Y";
			szWhere += " and T.YOYAKUDT = RE.YOYAKUDT ";
		}else if(TblType.JNL.getVal()==tbl.getVal()){
			szTable = "INAAD.JNLBAIKACTL";
			szWhere = "T.SEQ = RE.SEQ";
		}else if(TblType.CSV.getVal()==tbl.getVal()){
			szTable = "INAWS.CSVBAIKACTL";
			szWhere = "T.SEQ = RE.SEQ and T.INPUTNO = RE.INPUTNO and T.INPUTEDANO = RE.INPUTEDANO";
		}

		// 基本Merge文
		StringBuffer sbSQL;
		sbSQL = new StringBuffer();
		sbSQL.append("merge into "+szTable+" as T");
		sbSQL.append(" using (select ");
		if(SqlType.DEL.getVal() ==  sql.getVal()){
			sbSQL.append("  cast("+MSTBAIKACTLLayout.SHNCD.getId()+" as "+MSTBAIKACTLLayout.SHNCD.getTyp()+") as "+MSTBAIKACTLLayout.SHNCD.getCol());			// 商品コード
			sbSQL.append(" ,cast("+MSTBAIKACTLLayout.YOYAKUDT.getId()+" as "+MSTBAIKACTLLayout.YOYAKUDT.getTyp()+") as "+MSTBAIKACTLLayout.YOYAKUDT.getCol());	// マスタ変更予定日
		}else{
			int baseCnt = MSTBAIKACTLLayout.values().length;
			for(MSTBAIKACTLLayout itm :MSTBAIKACTLLayout.values()){
				if(itm.getNo() > 1){ sbSQL.append(" ,"); }
				sbSQL.append("cast("+itm.getId() + " as "+itm.getTyp() + ") as " + itm.getCol());
			}
			if(TblType.JNL.getVal()==tbl.getVal()){
				for(JNLCMNLayout itm :JNLCMNLayout.values()){
					sbSQL.append(" ,cast("+itm.getId2(baseCnt) + " as "+itm.getTyp() + ") as " + itm.getCol());
				}
			}
			if(TblType.CSV.getVal()==tbl.getVal()){
				for(CSVCMNLayout itm :CSVCMNLayout.values()){
					sbSQL.append(" ,cast("+itm.getId2(baseCnt) + " as "+itm.getTyp() + ") as " + itm.getCol());
				}
			}
		}
		sbSQL.append(" from (values"+values+") as T1("+names+")");
		sbSQL.append(" ) as RE on ("+szWhere+") ");
		return sbSQL.toString();
	}


	/**
	 * ソースコード管理マスタINSERT/UPDATE SQL作成処理
	 *
	 * @param userId
	 * @param data
	 * @param tbl
	 *
	 * @throws Exception
	 */
	public JSONObject createSqlMSTSRCCD(String userId, String btnId, JSONArray dataArray, TblType tbl, SqlType sql){
		JSONObject result = new JSONObject();


		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();
		String values = "", names = "", rows = "";
		int colNum = MSTSRCCDLayout.values().length;		// テーブル列数
		if(TblType.JNL.getVal()==tbl.getVal()){
			colNum+=2;
		}
		if(TblType.CSV.getVal()==tbl.getVal()){
			colNum+=CSVCMNLayout.values().length;
		}
		for(int j=0; j < dataArray.size(); j++){
			values = "";
			names = "";
			for (int i = 1; i <= colNum; i++) {
				String col = "F" + i;
				String val = dataArray.optJSONObject(j).optString(col);

				if(i==MSTSRCCDLayout.SENDFLG.getNo()){		// 送信フラグ
					val = "0";
				}else if(i==MSTSRCCDLayout.OPERATOR.getNo()){		// オペレータ
					val = userId;
				}
				if(TblType.JNL.getVal()==tbl.getVal()){
					if(i==MSTSRCCDLayout.values().length+1){			// SEQ(ジャーナル用)
						val = jnlshn_seq;
					}else if(i==MSTSRCCDLayout.values().length+2){		// RENNO(ジャーナル用)
						val = (j+1)+"";
					}
				}
				// CSV特殊情報設定
				if(TblType.CSV.getVal()==tbl.getVal()){
					// SEQ以外dataArrayに追加済み前提
					if(i==MSTSRCCDLayout.values().length+1){			// SEQ(CSV用)
						val = csvshn_seq;
					}
				}

				// TMP特殊情報設定
				if(TblType.TMP.getVal()==tbl.getVal()){
					// dataArrayに追加前提
				}
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

		StringBuffer sbSQL;
		sbSQL = new StringBuffer();
		sbSQL.append(this.createMergeCmnCommandMSTSRCCD(tbl, sql, rows, names));
		if(SqlType.INS.getVal() == sql.getVal()){
			sbSQL.append(" when not matched then ");
			sbSQL.append(" insert (");
			if(TblType.JNL.getVal()==tbl.getVal()){
				sbSQL.append("  SEQ");			// F1 : SEQ
				sbSQL.append(" ,RENNO");		// F2 : RENNO
				sbSQL.append(" ,");
			}
			if(TblType.CSV.getVal()==tbl.getVal()){
				sbSQL.append("  SEQ");			// F1 : SEQ
				sbSQL.append(" ,INPUTNO");		// F2 : 入力番号
				sbSQL.append(" ,INPUTEDANO");	// F3 : 入力枝番
				sbSQL.append(" ,");
			}
			if(TblType.TMP.getVal()==tbl.getVal()){
				sbSQL.append("  SESID");		// F1 : セッションID
				sbSQL.append(" ,");
			}
			sbSQL.append("  SHNCD");		// F1 : 商品コード
			sbSQL.append(" ,SRCCD");		// F2 : ソースコード
			sbSQL.append(" ,YOYAKUDT");		// F3 : マスタ変更予定日
			sbSQL.append(" ,SEQNO");		// F4 : 入力順番
			if(TblType.CSV.getVal()==tbl.getVal()||TblType.JNL.getVal()==tbl.getVal()){
				sbSQL.append(" ,SRCKBN");		// F5 : ソース区分
			}else{
				sbSQL.append(" ,SOURCEKBN");	// F5 : ソース区分
			}
			sbSQL.append(" ,SENDFLG");		// F6 : 送信フラグ
			sbSQL.append(" ,OPERATOR");		// F7 : オペレータ
			sbSQL.append(" ,ADDDT");		// F8 : 登録日
			sbSQL.append(" ,UPDDT");		// F9 : 更新日
			sbSQL.append(" ,YUKO_STDT");	// F10: 有効開始日
			sbSQL.append(" ,YUKO_EDDT");	// F11: 有効終了日

			sbSQL.append(")values(");
			if(TblType.JNL.getVal()==tbl.getVal()){
				sbSQL.append("  RE.SEQ");			// F1 : SEQ
				sbSQL.append(" ,RE.RENNO");			// F2 : RENNO
				sbSQL.append(" ,");
			}
			if(TblType.CSV.getVal()==tbl.getVal()){
				sbSQL.append("  RE.SEQ");			// F1 : SEQ
				sbSQL.append(" ,RE.INPUTNO");		// F2 : 入力番号
				sbSQL.append(" ,RE.INPUTEDANO");	// F3 : 入力枝番
				sbSQL.append(" ,");
			}
			if(TblType.TMP.getVal()==tbl.getVal()){
				sbSQL.append("  RE.SESID");			// F1 : セッションID
				sbSQL.append(" ,");
			}
			sbSQL.append("  RE.SHNCD");		// F1 : 商品コード
			sbSQL.append(" ,RE.SRCCD");		// F2 : ソースコード
			sbSQL.append(" ,RE.YOYAKUDT");	// F3 : マスタ変更予定日
			sbSQL.append(" ,RE.SEQNO");		// F4 : 入力順番
			sbSQL.append(" ,RE.SOURCEKBN");	// F5 : ソース区分
			sbSQL.append(" ,RE.SENDFLG");	// F6 : 送信フラグ
			sbSQL.append(" ,RE.OPERATOR");	// F7 : オペレータ
			sbSQL.append(" ,current timestamp");		// F8 : 登録日
			sbSQL.append(" ,current timestamp");		// F9 : 更新日
			sbSQL.append(" ,RE.YUKO_STDT");	// F10: 有効開始日
			sbSQL.append(" ,RE.YUKO_EDDT");	// F11: 有効終了日

			sbSQL.append(" )");
		}
		if(SqlType.DEL.getVal() == sql.getVal()){
			sbSQL.append(" when matched then delete");
		}
		if (DefineReport.ID_DEBUG_MODE)	System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("ソースコード管理マスタ" + tbl.getTxt());
		return result;
	}

	/**
	 * ソースコード管理マスタMergeSQL作成処理
	 *
	 * @param tbl
	 * @param sql
	 * @param values
	 * @param names
	 * @param userId
	 *
	 * @throws Exception
	 */
	public String createMergeCmnCommandMSTSRCCD(TblType tbl, SqlType sql, String values, String names) {

		String szTable = "INAWS.PIMTISRCCD";
		String szWhere = "T.SHNCD = RE.SHNCD";
		if(SqlType.DEL.getVal()!=sql.getVal()){
			szWhere += " and T.SRCCD = RE.SRCCD";
		}
		if(TblType.YYK.getVal()==tbl.getVal()){
			szTable += "_Y";
			szWhere += " and T.YOYAKUDT = RE.YOYAKUDT ";
		}else if(TblType.JNL.getVal()==tbl.getVal()){
			szTable = "INAAD.JNLSRCCD";
			szWhere = "T.SEQ = RE.SEQ";
		}else if(TblType.CSV.getVal()==tbl.getVal()){
			szTable = "INAWS.CSVSRCCD";
			szWhere = "T.SEQ = RE.SEQ and T.INPUTNO = RE.INPUTNO";
		}

		int baseCnt = MSTSRCCDLayout.values().length;
		// 基本Merge文
		StringBuffer sbSQL;
		sbSQL = new StringBuffer();
		sbSQL.append("merge into "+szTable+" as T");
		sbSQL.append(" using (select ");
		if(SqlType.DEL.getVal() ==  sql.getVal()){
			sbSQL.append("  cast("+MSTSRCCDLayout.SHNCD.getId()+" as "+MSTSRCCDLayout.SHNCD.getTyp()+") as "+MSTSRCCDLayout.SHNCD.getCol());			// 商品コード
			sbSQL.append(" ,cast("+MSTSRCCDLayout.YOYAKUDT.getId()+" as "+MSTSRCCDLayout.YOYAKUDT.getTyp()+") as "+MSTSRCCDLayout.YOYAKUDT.getCol());	// マスタ変更予定日
		}else{
			for(MSTSRCCDLayout itm :MSTSRCCDLayout.values()){
				if(itm.getNo() > 1){ sbSQL.append(" ,"); }
				sbSQL.append("cast("+itm.getId() + " as "+itm.getTyp() + ") as " + itm.getCol());
			}
			if(TblType.JNL.getVal()==tbl.getVal()){
				for(JNLCMNLayout itm :JNLCMNLayout.values()){
					sbSQL.append(" ,cast("+itm.getId2(baseCnt) + " as "+itm.getTyp() + ") as " + itm.getCol());
				}
			}
			if(TblType.CSV.getVal()==tbl.getVal()){
				for(CSVCMNLayout itm :CSVCMNLayout.values()){
					sbSQL.append(" ,cast("+itm.getId2(baseCnt) + " as "+itm.getTyp() + ") as " + itm.getCol());
				}
			}
		}
		sbSQL.append(" from (values"+values+") as T1("+names+")");
		sbSQL.append(" ) as RE on ("+szWhere+") ");
		return sbSQL.toString();
	}



	/**
	 * 添加物マスタINSERT/UPDATE SQL作成処理
	 *
	 * @param userId
	 * @param data
	 * @param tbl
	 *
	 * @throws Exception
	 */
	public JSONObject createSqlMSTTENKABUTSU(String userId, String btnId, JSONArray dataArray, TblType tbl, SqlType sql){
		JSONObject result = new JSONObject();


		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();
		String values = "", names = "", rows = "";
		int colNum = MSTTENKABUTSULayout.values().length;		// テーブル列数
		if(TblType.JNL.getVal()==tbl.getVal()){
			colNum+=JNLCMNLayout.values().length;
		}
		if(TblType.CSV.getVal()==tbl.getVal()){
			colNum+=CSVCMNLayout.values().length;
		}
		for(int j=0; j < dataArray.size(); j++){
			values = "";
			names = "";
			for (int i = 1; i <= colNum; i++) {
				String col = "F" + i;
				String val = dataArray.optJSONObject(j).optString(col);

				if(i==MSTTENKABUTSULayout.SENDFLG.getNo()){		// 送信フラグ
					val = "0";
				}else if(i==MSTTENKABUTSULayout.OPERATOR.getNo()){		// オペレータ
					val = ""+userId;
				}
				if(TblType.JNL.getVal()==tbl.getVal()){
					if(i==MSTTENKABUTSULayout.values().length+1){			// SEQ(ジャーナル用)
						val = jnlshn_seq;
					}else if(i==MSTTENKABUTSULayout.values().length+2){		// RENNO(ジャーナル用)
						val = (j+1)+"";
					}
				}
				// CSV特殊情報設定
				if(TblType.CSV.getVal()==tbl.getVal()){
					// SEQ以外dataArrayに追加済み前提
					if(i==MSTTENKABUTSULayout.values().length+1){			// SEQ(CSV用)
						val = csvshn_seq;
					}
				}
				if(StringUtils.isEmpty(val)){
					values += ", null";
				}else{
					if(isTest){
						values += ", '"+val+"'";
					}else{
						prmData.add(val);
						values += ", cast(? as varchar("+MessageUtility.getDefByteLen(val)+"))";
					}
				}
				names  += ", "+col;
			}
			rows += ",("+StringUtils.removeStart(values, ",")+")";
		}
		rows = StringUtils.removeStart(rows, ",");
		names  = StringUtils.removeStart(names, ",");

		StringBuffer sbSQL;
		sbSQL = new StringBuffer();
		sbSQL.append(this.createMergeCmnCommandMSTTENKABUTSU(tbl, sql, rows, names));
		if(SqlType.INS.getVal() == sql.getVal()){
			sbSQL.append(" when not matched then ");
			sbSQL.append(" insert (");
			if(TblType.JNL.getVal()==tbl.getVal()){
				sbSQL.append("  SEQ");			// SEQ
				sbSQL.append(" ,RENNO");		// RENNO
				sbSQL.append(" ,");
			}
			if(TblType.CSV.getVal()==tbl.getVal()){
				sbSQL.append("  SEQ");			// F1 : SEQ
				sbSQL.append(" ,INPUTNO");		// F2 : 入力番号
				sbSQL.append(" ,INPUTEDANO");	// F3 : 入力枝番
				sbSQL.append(" ,");
			}
			sbSQL.append("  SHNCD");		// F1 : 商品コード
			sbSQL.append(" ,TENKABKBN");	// F2 : 添加物区分
			sbSQL.append(" ,TENKABCD");		// F3 : 添加物コード
			sbSQL.append(" ,YOYAKUDT");		// F4 : マスタ変更予定日
			sbSQL.append(" ,SENDFLG");		// F5 : 送信フラグ
			sbSQL.append(" ,OPERATOR");		// F6 : オペレータ
			sbSQL.append(" ,ADDDT");		// F7 : 登録日
			sbSQL.append(" ,UPDDT");		// F8 : 更新日
			sbSQL.append(")values(");
			if(TblType.JNL.getVal()==tbl.getVal()){
				sbSQL.append("  RE.SEQ");							// SEQ
				sbSQL.append(" ,RE.RENNO");							// RENNO
				sbSQL.append(" ,");
			}
			if(TblType.CSV.getVal()==tbl.getVal()){
				sbSQL.append("  RE.SEQ");			// F1 : SEQ
				sbSQL.append(" ,RE.INPUTNO");		// F2 : 入力番号
				sbSQL.append(" ,RE.INPUTEDANO");	// F3 : 入力枝番
				sbSQL.append(" ,");
			}
			sbSQL.append("  RE.SHNCD");			// F1 : 商品コード
			sbSQL.append(" ,RE.TENKABKBN");		// F2 : 添加物区分
			sbSQL.append(" ,RE.TENKABCD");		// F3 : 添加物コード
			sbSQL.append(" ,RE.YOYAKUDT");		// F4 : マスタ変更予定日
			sbSQL.append(" ,RE.SENDFLG");		// F5 : 送信フラグ
			sbSQL.append(" ,RE.OPERATOR");		// F6 : オペレータ
			sbSQL.append(" ,current timestamp");			// F7 : 登録日
			sbSQL.append(" ,current timestamp");			// F8 : 更新日

			sbSQL.append(" )");
		}
		if(SqlType.DEL.getVal() == sql.getVal()){
			sbSQL.append(" when matched then delete");
		}
		if (DefineReport.ID_DEBUG_MODE)	System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("添加物マスタ" + tbl.getTxt());
		return result;
	}

	/**
	 * 添加物マスタMergeSQL作成処理
	 *
	 * @param tbl
	 * @param sql
	 * @param values
	 * @param names
	 * @param userId
	 *
	 * @throws Exception
	 */
	public String createMergeCmnCommandMSTTENKABUTSU(TblType tbl, SqlType sql, String values, String names) {

		String szTable = "INAWS.PIMTITENKABUTSU";
		String szWhere = "T.SHNCD = RE.SHNCD";
		if(SqlType.DEL.getVal()!=sql.getVal()){
			szWhere += " and T.TENKABKBN = RE.TENKABKBN and T.TENKABCD = RE.TENKABCD";
		}
		if(TblType.YYK.getVal()==tbl.getVal()){
			szTable += "_Y";
			szWhere += " and T.YOYAKUDT = RE.YOYAKUDT ";
		}else if(TblType.JNL.getVal()==tbl.getVal()){
			szTable = "INAAD.JNLTENKABUTSU";
			szWhere = "T.SEQ = RE.SEQ";
		}else if(TblType.CSV.getVal()==tbl.getVal()){
			szTable = "INAWS.CSVTENKABUTSU";
			szWhere = "T.SEQ = RE.SEQ and T.INPUTNO = RE.INPUTNO";
		}

		// 基本Merge文
		StringBuffer sbSQL;
		sbSQL = new StringBuffer();
		sbSQL.append("merge into "+szTable+" as T");
		sbSQL.append(" using (select ");
		if(SqlType.DEL.getVal() ==  sql.getVal()){
			sbSQL.append("  cast("+MSTTENKABUTSULayout.SHNCD.getId()+" as "+MSTTENKABUTSULayout.SHNCD.getTyp()+") as "+MSTTENKABUTSULayout.SHNCD.getCol());			// 商品コード
			sbSQL.append(" ,cast("+MSTTENKABUTSULayout.YOYAKUDT.getId()+" as "+MSTTENKABUTSULayout.YOYAKUDT.getTyp()+") as "+MSTTENKABUTSULayout.YOYAKUDT.getCol());	// マスタ変更予定日
		}else{
			int baseCnt = MSTTENKABUTSULayout.values().length;
			for(MSTTENKABUTSULayout itm :MSTTENKABUTSULayout.values()){
				if(itm.getNo() > 1){ sbSQL.append(" ,"); }
				sbSQL.append("cast("+itm.getId() + " as "+itm.getTyp() + ") as " + itm.getCol());
			}
			if(TblType.JNL.getVal()==tbl.getVal()){
				for(JNLCMNLayout itm :JNLCMNLayout.values()){
					sbSQL.append(" ,cast("+itm.getId2(baseCnt) + " as "+itm.getTyp() + ") as " + itm.getCol());
				}
			}
			if(TblType.CSV.getVal()==tbl.getVal()){
				for(CSVCMNLayout itm :CSVCMNLayout.values()){
					sbSQL.append(" ,cast("+itm.getId2(baseCnt) + " as "+itm.getTyp() + ") as " + itm.getCol());
				}
			}
		}
		sbSQL.append(" from (values"+values+") as T1("+names+")");
		sbSQL.append(" ) as RE on ("+szWhere+") ");
		return sbSQL.toString();
	}


	/**
	 * 品揃グループマスタINSERT/UPDATE SQL作成処理
	 *
	 * @param userId
	 * @param data
	 * @param tbl
	 *
	 * @throws Exception
	 */
	public JSONObject createSqlMSTSHINAGP(String userId, String btnId, JSONArray dataArray, TblType tbl, SqlType sql){
		JSONObject result = new JSONObject();


		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();
		String values = "", names = "", rows = "";
		int colNum = MSTSHINAGPLayout.values().length;		// テーブル列数
		if(TblType.JNL.getVal()==tbl.getVal()){
			colNum+=2;
		}
		if(TblType.CSV.getVal()==tbl.getVal()){
			colNum+=CSVCMNLayout.values().length;
		}
		for(int j=0; j < dataArray.size(); j++){
			values = "";
			names = "";
			for (int i = 1; i <= colNum; i++) {
				String col = "F" + i;
				String val = dataArray.optJSONObject(j).optString(col);

				if(i==MSTSHINAGPLayout.SENDFLG.getNo()){			// 送信フラグ
					val = DefineReport.Values.SENDFLG_UN.getVal();
				}else if(i==MSTSHINAGPLayout.OPERATOR.getNo()){		// オペレータ
					val = ""+userId;
				}
				if(TblType.JNL.getVal()==tbl.getVal()){
					if(i==MSTSHINAGPLayout.values().length+1){			// SEQ(ジャーナル用)
						val = jnlshn_seq;
					}else if(i==MSTSHINAGPLayout.values().length+2){	// RENNO(ジャーナル用)
						val = (j+1)+"";
					}
				}
				// CSV特殊情報設定
				if(TblType.CSV.getVal()==tbl.getVal()){
					// SEQ以外dataArrayに追加済み前提
					if(i==MSTSHINAGPLayout.values().length+1){			// SEQ(CSV用)
						val = csvshn_seq;
					}
				}
				if(StringUtils.isEmpty(val)){
					values += ", null";
				}else{
					if(isTest){
						values += ", '"+val+"'";
					}else{
						prmData.add(val);
						values += ", cast(? as varchar("+MessageUtility.getDefByteLen(val)+"))";
					}
				}
				names  += ", "+col;
			}
			rows += ",("+StringUtils.removeStart(values, ",")+")";
		}
		rows = StringUtils.removeStart(rows, ",");
		names  = StringUtils.removeStart(names, ",");

		StringBuffer sbSQL;
		sbSQL = new StringBuffer();
		sbSQL.append(this.createMergeCmnCommandMSTSHINAGP(tbl, sql, rows, names));
		if(SqlType.INS.getVal() == sql.getVal()){
			sbSQL.append(" when not matched then ");
			sbSQL.append(" insert (");
			if(TblType.JNL.getVal()==tbl.getVal()){
				sbSQL.append("  SEQ");			// SEQ
				sbSQL.append(" ,RENNO");		// RENNO
				sbSQL.append(" ,");
			}
			if(TblType.CSV.getVal()==tbl.getVal()){
				sbSQL.append("  SEQ");			// F1 : SEQ
				sbSQL.append(" ,INPUTNO");		// F2 : 入力番号
				sbSQL.append(" ,INPUTEDANO");	// F3 : 入力枝番
				sbSQL.append(" ,");
			}
			sbSQL.append("  SHNCD");		// F1 : 商品コード
			sbSQL.append(" ,TENGPCD");		// F2 : 店グループ
			sbSQL.append(" ,YOYAKUDT");		// F3 : マスタ変更予定日
			sbSQL.append(" ,AREAKBN");		// F4 : エリア区分
			sbSQL.append(" ,ATSUKKBN");		// F5 : 扱い区分
			sbSQL.append(" ,SENDFLG");		// F6 : 送信フラグ
			sbSQL.append(" ,OPERATOR");		// F7 : オペレータ
			sbSQL.append(" ,ADDDT");		// F8 : 登録日
			sbSQL.append(" ,UPDDT");		// F9 : 更新日

			sbSQL.append(")values(");
			if(TblType.JNL.getVal()==tbl.getVal()){
				sbSQL.append("  RE.SEQ");							// SEQ
				sbSQL.append(" ,RE.RENNO");							// RENNO
				sbSQL.append(" ,");
			}
			if(TblType.CSV.getVal()==tbl.getVal()){
				sbSQL.append("  RE.SEQ");			// F1 : SEQ
				sbSQL.append(" ,RE.INPUTNO");		// F2 : 入力番号
				sbSQL.append(" ,RE.INPUTEDANO");	// F3 : 入力枝番
				sbSQL.append(" ,");
			}
			sbSQL.append("  RE.SHNCD");			// F1 : 商品コード
			sbSQL.append(" ,RE.TENGPCD");		// F2 : 店グループ
			sbSQL.append(" ,RE.YOYAKUDT");		// F3 : マスタ変更予定日
			sbSQL.append(" ,RE.AREAKBN");		// F4 : エリア区分
			sbSQL.append(" ,RE.ATSUKKBN");		// F5 : 扱い区分
			sbSQL.append(" ,RE.SENDFLG");		// F6 : 送信フラグ
			sbSQL.append(" ,RE.OPERATOR");		// F7 : オペレータ
			sbSQL.append(" ,current timestamp");			// F8 : 登録日
			sbSQL.append(" ,current timestamp");			// F9 : 更新日
			sbSQL.append(" )");
		}
		if(SqlType.DEL.getVal() == sql.getVal()){
			sbSQL.append(" when matched then delete");
		}
		if (DefineReport.ID_DEBUG_MODE)	System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("品揃グループマスタ" + tbl.getTxt());
		return result;
	}

	/**
	 * 品揃グループマスタMergeSQL作成処理
	 *
	 * @param tbl
	 * @param sql
	 * @param values
	 * @param names
	 * @param userId
	 *
	 * @throws Exception
	 */
	public String createMergeCmnCommandMSTSHINAGP(TblType tbl, SqlType sql, String values, String names) {

		String szTable = "INAWS.PIMTISHINAGP";
		String szWhere = "T.SHNCD = RE.SHNCD";
		if(SqlType.DEL.getVal()!=sql.getVal()){
			szWhere += " and T.TENGPCD = RE.TENGPCD";
		}
		if(TblType.YYK.getVal()==tbl.getVal()){
			szTable += "_Y";
			szWhere += " and T.YOYAKUDT = RE.YOYAKUDT ";
		}else if(TblType.JNL.getVal()==tbl.getVal()){
			szTable = "INAAD.JNLSHINAGP";
			szWhere = "T.SEQ = RE.SEQ";
		}else if(TblType.CSV.getVal()==tbl.getVal()){
			szTable = "INAWS.CSVSHINAGP";
			szWhere = "T.SEQ = RE.SEQ and T.INPUTNO = RE.INPUTNO";
		}

		// 基本Merge文
		StringBuffer sbSQL;
		sbSQL = new StringBuffer();
		sbSQL.append("merge into "+szTable+" as T");
		sbSQL.append(" using (select ");
		if(SqlType.DEL.getVal() ==  sql.getVal()){
			sbSQL.append("  cast("+MSTSHINAGPLayout.SHNCD.getId()+" as "+MSTSHINAGPLayout.SHNCD.getTyp()+") as "+MSTSHINAGPLayout.SHNCD.getCol());			// 商品コード
			sbSQL.append(" ,cast("+MSTSHINAGPLayout.YOYAKUDT.getId()+" as "+MSTSHINAGPLayout.YOYAKUDT.getTyp()+") as "+MSTSHINAGPLayout.YOYAKUDT.getCol());	// マスタ変更予定日
		}else{
			int baseCnt = MSTSHINAGPLayout.values().length;
			for(MSTSHINAGPLayout itm :MSTSHINAGPLayout.values()){
				if(itm.getNo() > 1){ sbSQL.append(" ,"); }
				sbSQL.append("cast("+itm.getId() + " as "+itm.getTyp() + ") as " + itm.getCol());
			}
			if(TblType.JNL.getVal()==tbl.getVal()){
				for(JNLCMNLayout itm :JNLCMNLayout.values()){
					sbSQL.append(" ,cast("+itm.getId2(baseCnt) + " as "+itm.getTyp() + ") as " + itm.getCol());
				}
			}
			if(TblType.CSV.getVal()==tbl.getVal()){
				for(CSVCMNLayout itm :CSVCMNLayout.values()){
					sbSQL.append(" ,cast("+itm.getId2(baseCnt) + " as "+itm.getTyp() + ") as " + itm.getCol());
				}
			}
		}
		sbSQL.append(" from (values"+values+") as T1("+names+")");
		sbSQL.append(" ) as RE on ("+szWhere+") ");
		return sbSQL.toString();
	}


	/**
	 * 店別異部門INSERT/UPDATE SQL作成処理
	 *
	 * @param userId
	 * @param data
	 * @param tbl
	 *
	 * @throws Exception
	 */
	public JSONObject createSqlMSTSHNTENBMN(String userId, String btnId, JSONArray dataArray, TblType tbl, SqlType sql){
		JSONObject result = new JSONObject();


		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();
		String values = "", names = "", rows = "";
		int colNum = MSTSHNTENBMNLayout.values().length;		// テーブル列数
		if(TblType.JNL.getVal()==tbl.getVal()){
			colNum+=2;
		}
		if(TblType.CSV.getVal()==tbl.getVal()){
			colNum+=CSVCMNLayout.values().length;
		}
		for(int j=0; j < dataArray.size(); j++){
			values = "";
			names = "";
			for (int i = 1; i <= colNum; i++) {
				String col = "F" + i;
				String val = dataArray.optJSONObject(j).optString(col);

				if(i==MSTSHNTENBMNLayout.SENDFLG.getNo()){			// 送信フラグ
					val = DefineReport.Values.SENDFLG_UN.getVal();
				}else if(i==MSTSHNTENBMNLayout.OPERATOR.getNo()){		// オペレータ
					val = ""+userId;
				}
				if(TblType.JNL.getVal()==tbl.getVal()){
					if(i==MSTSHNTENBMNLayout.values().length+1){			// SEQ(ジャーナル用)
						val = jnlshn_seq;
					}else if(i==MSTSHNTENBMNLayout.values().length+2){	// RENNO(ジャーナル用)
						val = (j+1)+"";
					}
				}
				// CSV特殊情報設定
				if(TblType.CSV.getVal()==tbl.getVal()){
					// SEQ以外dataArrayに追加済み前提
					if(i==MSTSHNTENBMNLayout.values().length+1){			// SEQ(CSV用)
						val = csvshn_seq;
					}
				}
				if(StringUtils.isEmpty(val)){
					values += ", null";
				}else{
					if(isTest){
						values += ", '"+val+"'";
					}else{
						prmData.add(val);
						values += ", cast(? as varchar("+MessageUtility.getDefByteLen(val)+"))";
					}
				}
				names  += ", "+col;
			}
			rows += ",("+StringUtils.removeStart(values, ",")+")";
		}
		rows = StringUtils.removeStart(rows, ",");
		names  = StringUtils.removeStart(names, ",");

		StringBuffer sbSQL;
		sbSQL = new StringBuffer();
		sbSQL.append(this.createMergeCmnCommandMSTSHNTENBMN(tbl, sql, rows, names));
		if(SqlType.INS.getVal() == sql.getVal()){
			sbSQL.append(" when not matched then ");
			sbSQL.append(" insert (");
			if(TblType.JNL.getVal()==tbl.getVal()){
				sbSQL.append("  SEQ");			// SEQ
				sbSQL.append(" ,RENNO");		// RENNO
				sbSQL.append(" ,");
			}
			if(TblType.CSV.getVal()==tbl.getVal()){
				sbSQL.append("  SEQ");			// F1 : SEQ
				sbSQL.append(" ,INPUTNO");		// F2 : 入力番号
				sbSQL.append(" ,INPUTEDANO");	// F3 : 入力枝番
				sbSQL.append(" ,");
			}
			sbSQL.append("  SHNCD");		// F1 : 商品コード
			sbSQL.append(" ,TENSHNCD");		// F2 : 店別異部門商品コード
			sbSQL.append(" ,TENGPCD");		// F3 : 店グループ
			sbSQL.append(" ,YOYAKUDT");		// F4 : マスタ変更予定日
			sbSQL.append(" ,AREAKBN");		// F5 : エリア区分
			sbSQL.append(" ,SENDFLG");		// F6 : 送信フラグ
			sbSQL.append(" ,OPERATOR");		// F7 : オペレータ
			sbSQL.append(" ,ADDDT");		// F8 : 登録日
			sbSQL.append(" ,UPDDT");		// F9 : 更新日
			sbSQL.append(" ,SRCCD");		// F10: ソースコード

			sbSQL.append(") values(");
			if(TblType.JNL.getVal()==tbl.getVal()){
				sbSQL.append("  RE.SEQ");							// SEQ
				sbSQL.append(" ,RE.RENNO");							// RENNO
				sbSQL.append(" ,");
			}
			if(TblType.CSV.getVal()==tbl.getVal()){
				sbSQL.append("  RE.SEQ");			// F1 : SEQ
				sbSQL.append(" ,RE.INPUTNO");		// F2 : 入力番号
				sbSQL.append(" ,RE.INPUTEDANO");	// F3 : 入力枝番
				sbSQL.append(" ,");
			}
			sbSQL.append("  RE.SHNCD");		// F1 : 商品コード
			sbSQL.append(" ,RE.TENSHNCD");	// F2 : 店別異部門商品コード
			sbSQL.append(" ,RE.TENGPCD");	// F3 : 店グループ
			sbSQL.append(" ,RE.YOYAKUDT");	// F4 : マスタ変更予定日
			sbSQL.append(" ,RE.AREAKBN");	// F5 : エリア区分
			sbSQL.append(" ,RE.SENDFLG");	// F6 : 送信フラグ
			sbSQL.append(" ,RE.OPERATOR");	// F7 : オペレータ
			sbSQL.append(" ,current timestamp");	// F8 : 登録日
			sbSQL.append(" ,current timestamp");	// F9 : 更新日
			sbSQL.append(" ,RE.SRCCD");		// F10: ソースコード

			sbSQL.append(" )");
		}
		if(SqlType.DEL.getVal() == sql.getVal()){
			sbSQL.append(" when matched then delete");
		}
		if (DefineReport.ID_DEBUG_MODE)	System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("店別異部門" + tbl.getTxt());
		return result;
	}

	/**
	 * 店別異部門MergeSQL作成処理
	 *
	 * @param tbl
	 * @param sql
	 * @param values
	 * @param names
	 * @param userId
	 *
	 * @throws Exception
	 */
	public String createMergeCmnCommandMSTSHNTENBMN(TblType tbl, SqlType sql, String values, String names) {

		String szTable = "INAMS.MSTSHNTENBMN";
		String szWhere = "T.SHNCD = RE.SHNCD";
		if(SqlType.DEL.getVal()!=sql.getVal()){
			szWhere += " and T.TENGPCD = RE.TENGPCD";
		}
		if(TblType.YYK.getVal()==tbl.getVal()){
			szTable += "_Y";
			szWhere += " and T.YOYAKUDT = RE.YOYAKUDT ";
		}else if(TblType.JNL.getVal()==tbl.getVal()){
			szTable = "INAAD.JNLMSTSHNTENBMN";
			szWhere = "T.SEQ = RE.SEQ";
		}else if(TblType.CSV.getVal()==tbl.getVal()){
			szTable = "INAWS.CSVMSTSHNTENBMN";
			szWhere = "T.SEQ = RE.SEQ and T.INPUTNO = RE.INPUTNO";
		}

		// 基本Merge文
		StringBuffer sbSQL;
		sbSQL = new StringBuffer();
		sbSQL.append("merge into "+szTable+" as T");
		sbSQL.append(" using (select ");
		if(SqlType.DEL.getVal() ==  sql.getVal()){
			sbSQL.append("  cast("+MSTSHNTENBMNLayout.SHNCD.getId()+" as "+MSTSHNTENBMNLayout.SHNCD.getTyp()+") as "+MSTSHNTENBMNLayout.SHNCD.getCol());			// 商品コード
			sbSQL.append(" ,cast("+MSTSHNTENBMNLayout.YOYAKUDT.getId()+" as "+MSTSHNTENBMNLayout.YOYAKUDT.getTyp()+") as "+MSTSHNTENBMNLayout.YOYAKUDT.getCol());	// マスタ変更予定日
		}else{
			int baseCnt = MSTSHNTENBMNLayout.values().length;
			for(MSTSHNTENBMNLayout itm :MSTSHNTENBMNLayout.values()){
				if(itm.getNo() > 1){ sbSQL.append(" ,"); }
				sbSQL.append("cast("+itm.getId() + " as "+itm.getTyp() + ") as " + itm.getCol());
			}
			if(TblType.JNL.getVal()==tbl.getVal()){
				for(JNLCMNLayout itm :JNLCMNLayout.values()){
					sbSQL.append(" ,cast("+itm.getId2(baseCnt) + " as "+itm.getTyp() + ") as " + itm.getCol());
				}
			}
			if(TblType.CSV.getVal()==tbl.getVal()){
				for(CSVCMNLayout itm :CSVCMNLayout.values()){
					sbSQL.append(" ,cast("+itm.getId2(baseCnt) + " as "+itm.getTyp() + ") as " + itm.getCol());
				}
			}
		}
		sbSQL.append(" from (values"+values+") as T1("+names+")");
		sbSQL.append(" ) as RE on ("+szWhere+") ");
		return sbSQL.toString();
	}


	/**
	 * グループ分類マスタINSERT/UPDATE処理
	 *
	 * @param userId
	 * @param data
	 * @param tbl
	 *
	 * @throws Exception
	 */
	private void updateMSTGROUP(String userId, String btnId, JSONArray dataArray) throws Exception {


		// 関連情報取得
		ItemList iL = new ItemList();
		// 配列準備
		ArrayList<String> prmData = new ArrayList<String>();

		String values = "", names = "", rows = "";
		for(int j=0; j < dataArray.size(); j++){
			String val = dataArray.optJSONObject(j).optString(MSTGRPLayout.GRPKN.getId());
			values = Integer.toString(j+1);
			if(isTest){
				values += ", '"+val+"'";
			}else{
				prmData.add(val);
				values += ", cast(? as varchar("+MessageUtility.getDefByteLen(val)+"))";
			}
			rows += ",("+values+")";
		}
		rows = StringUtils.removeStart(rows, ",");


		// SQL実行
		StringBuffer sbSQL;
		sbSQL = new StringBuffer();
		sbSQL.append("merge into INAMS.MSTGROUP as T");
		sbSQL.append(" using (select ");
		sbSQL.append(" cast(GRPKN as "+MSTGRPLayout.GRPKN.getTyp() + ") as GRPKN");
		sbSQL.append(" from (values"+rows+") as T1(IDX, GRPKN)");
		sbSQL.append(" ) as RE on (T.GRPKN = RE.GRPKN) ");
		sbSQL.append(" when not matched then ");
		sbSQL.append(" insert (");
		//sbSQL.append("  GRPID,");		// F1 : グループ分類ID
		sbSQL.append("  GRPKN");		// F2 : グループ分類名
		sbSQL.append(" ,UPDKBN");		// F3 : 更新区分
		sbSQL.append(" ,SENDFLG");		// F4 : 送信フラグ
		sbSQL.append(" ,OPERATOR");		// F5 : オペレータ
		sbSQL.append(" ,ADDDT");		// F6 : 登録日
		sbSQL.append(" ,UPDDT");		// F7 : 更新日
		sbSQL.append(")values(");
		//sbSQL.append("  RE.GRPID,");								// F1 : グループ分類ID
		sbSQL.append("  RE.GRPKN");									// F2 : グループ分類名
		sbSQL.append(" ,"+DefineReport.ValUpdkbn.NML.getVal());		// F3 : 更新区分
		sbSQL.append(" ,"+DefineReport.Values.SENDFLG_UN.getVal());	// F4 : 送信フラグ
		sbSQL.append(" ,'"+ userId+ "'");							// F5 : オペレータ
		sbSQL.append(" ,current timestamp");						// F6 : 登録日
		sbSQL.append(" ,current timestamp");						// F7 : 更新日
		sbSQL.append(")");

		boolean result = iL.executeItem(sbSQL.toString(), prmData, Defines.STR_JNDI_DS);


		// 更新処理後、IDを取得する
		String sqlcommand = DefineReport.ID_SQL_MSTGROUP2.replace("@V", rows);
		@SuppressWarnings("static-access")
		JSONArray array = iL.selectJSONArray(sqlcommand, prmData, Defines.STR_JNDI_DS);


		// 更新結果をIDとしてセット
		for(int j=0; j < array.size(); j++){
			String id = array.optJSONObject(j).optString(MSTGRPLayout.GRPID.getCol());
			dataArray.optJSONObject(j).element(MSTGRPLayout.GRPID.getId(), id);
		}

	}

	/**
	 * グループ分類マスタINSERT/UPDATE SQL作成処理
	 *
	 * @param userId
	 * @param data
	 * @param tbl
	 *
	 * @throws Exception
	 */
	public JSONObject createSqlMSTGRP(String userId, String btnId, JSONArray dataArray, TblType tbl, SqlType sql){
		JSONObject result = new JSONObject();


		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();
		String values = "", names = "", rows = "";
		int colNum = MSTGRPLayout.values().length;		// テーブル列数
		if(TblType.JNL.getVal()==tbl.getVal()){
			colNum+=2;
		}
		if(TblType.CSV.getVal()==tbl.getVal()){
			colNum+=CSVCMNLayout.values().length;
		}
		for(int j=0; j < dataArray.size(); j++){
			values = "";
			names = "";
			for (int i = 1; i <= colNum; i++) {
				String col = "F" + i;
				String val = dataArray.optJSONObject(j).optString(col);

				if(i==MSTGRPLayout.SENDFLG.getNo()){			// 送信フラグ
					val = DefineReport.Values.SENDFLG_UN.getVal();
				}else if(i==MSTGRPLayout.OPERATOR.getNo()){		// オペレータ
					val = userId;
				}
				if(TblType.JNL.getVal()==tbl.getVal()){
					if(i==MSTGRPLayout.values().length+1){			// SEQ(ジャーナル用)
						val = jnlshn_seq;
					}else if(i==MSTGRPLayout.values().length+2){	// RENNO(ジャーナル用)
						val = (j+1)+"";
					}
				}
				// CSV特殊情報設定
				if(TblType.CSV.getVal()==tbl.getVal()){
					// SEQ以外dataArrayに追加済み前提
					if(i==MSTGRPLayout.values().length+1){			// SEQ(CSV用)
						val = csvshn_seq;
					}
				}
				if(StringUtils.isEmpty(val)){
					values += ", null";
				}else{
					if(isTest){
						values += ", '"+val+"'";
					}else{
						prmData.add(val);
						values += ", cast(? as varchar("+MessageUtility.getDefByteLen(val)+"))";
					}
				}
				names  += ", "+col;
			}
			rows += ",("+StringUtils.removeStart(values, ",")+")";
		}
		rows = StringUtils.removeStart(rows, ",");
		names  = StringUtils.removeStart(names, ",");

		StringBuffer sbSQL;
		sbSQL = new StringBuffer();
		sbSQL.append(this.createMergeCmnCommandMSTGRP(tbl, sql, rows, names));
		if(SqlType.INS.getVal() == sql.getVal()){
			sbSQL.append(" when not matched then ");
			sbSQL.append(" insert (");
			if(TblType.JNL.getVal()==tbl.getVal()){
				sbSQL.append("  SEQ");			// SEQ
				sbSQL.append(" ,RENNO");		// RENNO
				sbSQL.append(" ,");
			}
			if(TblType.CSV.getVal()==tbl.getVal()){
				sbSQL.append("  SEQ");			// F1 : SEQ
				sbSQL.append(" ,INPUTNO");		// F2 : 入力番号
				sbSQL.append(" ,INPUTEDANO");	// F3 : 入力枝番
				sbSQL.append(" ,");
			}
			sbSQL.append("  SHNCD");		// F1 : 商品コード
			sbSQL.append(" ,GRPID");		// F2 : グループ分類ID
			sbSQL.append(" ,YOYAKUDT");		// F3 : マスタ変更予定日
			sbSQL.append(" ,SEQNO");		// F4 : 入力順番
			sbSQL.append(" ,SENDFLG");		// F5 : 送信フラグ
			sbSQL.append(" ,OPERATOR");		// F6 : オペレータ
			sbSQL.append(" ,ADDDT");		// F7 : 登録日
			sbSQL.append(" ,UPDDT");		// F8 : 更新日
			sbSQL.append(")values(");
			if(TblType.JNL.getVal()==tbl.getVal()){
				sbSQL.append("  RE.SEQ");							// SEQ
				sbSQL.append(" ,RE.RENNO");							// RENNO
				sbSQL.append(" ,");
			}
			if(TblType.CSV.getVal()==tbl.getVal()){
				sbSQL.append("  RE.SEQ");			// F1 : SEQ
				sbSQL.append(" ,RE.INPUTNO");		// F2 : 入力番号
				sbSQL.append(" ,RE.INPUTEDANO");	// F3 : 入力枝番
				sbSQL.append(" ,");
			}
			sbSQL.append("  RE.SHNCD");		// F1 : 商品コード
			sbSQL.append(" ,RE.GRPID");		// F2 : グループ分類ID
			sbSQL.append(" ,RE.YOYAKUDT");	// F3 : マスタ変更予定日
			sbSQL.append(" ,RE.SEQNO");		// F4 : 入力順番
			sbSQL.append(" ,RE.SENDFLG");	// F5 : 送信フラグ
			sbSQL.append(" ,RE.OPERATOR");	// F6 : オペレータ
			sbSQL.append(" ,current timestamp");		// F7 : 登録日
			sbSQL.append(" ,current timestamp");		// F8 : 更新日

			sbSQL.append(" )");
		}
		if(SqlType.DEL.getVal() == sql.getVal()){
			sbSQL.append(" when matched then delete");
		}
		if (DefineReport.ID_DEBUG_MODE)	System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("品揃グループマスタ" + tbl.getTxt());
		return result;
	}

	/**
	 * 品揃グループマスタMergeSQL作成処理
	 *
	 * @param tbl
	 * @param sql
	 * @param values
	 * @param names
	 * @param userId
	 *
	 * @throws Exception
	 */
	public String createMergeCmnCommandMSTGRP(TblType tbl, SqlType sql, String values, String names) {

		String szTable = "INAMS.MSTGRP";
		String szWhere = "T.SHNCD = RE.SHNCD";
		if(SqlType.DEL.getVal()!=sql.getVal()){
			szWhere += " and T.GRPID = RE.GRPID";
		}
		if(TblType.YYK.getVal()==tbl.getVal()){
			szTable += "_Y";
			szWhere += " and T.YOYAKUDT = RE.YOYAKUDT ";
		}else if(TblType.JNL.getVal()==tbl.getVal()){
			szTable = "INAAD.JNLGRP";
			szWhere = "T.SEQ = RE.SEQ";
		}else if(TblType.CSV.getVal()==tbl.getVal()){
			szTable = "INAWS.CSVGRP";
			szWhere = "T.SEQ = RE.SEQ and T.INPUTNO = RE.INPUTNO";
		}

		// 基本Merge文
		StringBuffer sbSQL;
		sbSQL = new StringBuffer();
		sbSQL.append("merge into "+szTable+" as T");
		sbSQL.append(" using (select ");
		if(SqlType.DEL.getVal() ==  sql.getVal()){
			sbSQL.append("  cast("+MSTGRPLayout.SHNCD.getId()+" as "+MSTGRPLayout.SHNCD.getTyp()+") as "+MSTGRPLayout.SHNCD.getCol());			// 商品コード
			sbSQL.append(" ,cast("+MSTGRPLayout.YOYAKUDT.getId()+" as "+MSTGRPLayout.YOYAKUDT.getTyp()+") as "+MSTGRPLayout.YOYAKUDT.getCol());	// マスタ変更予定日
		}else{
			int baseCnt = MSTGRPLayout.values().length;
			for(MSTGRPLayout itm :MSTGRPLayout.values()){
				if(itm.getNo() > 1){ sbSQL.append(" ,"); }
				sbSQL.append("cast("+itm.getId() + " as "+itm.getTyp() + ") as " + itm.getCol());
			}
			if(TblType.JNL.getVal()==tbl.getVal()){
				for(JNLCMNLayout itm :JNLCMNLayout.values()){
					sbSQL.append(" ,cast("+itm.getId2(baseCnt) + " as "+itm.getTyp() + ") as " + itm.getCol());
				}
			}
			if(TblType.CSV.getVal()==tbl.getVal()){
				for(CSVCMNLayout itm :CSVCMNLayout.values()){
					sbSQL.append(" ,cast("+itm.getId2(baseCnt) + " as "+itm.getTyp() + ") as " + itm.getCol());
				}
			}
		}
		sbSQL.append(" from (values"+values+") as T1("+names+")");
		sbSQL.append(" ) as RE on ("+szWhere+") ");
		return sbSQL.toString();
	}


	/**
	 * 自動発注管理マスタINSERT/UPDATE SQL作成処理
	 *
	 * @param userId
	 * @param data
	 * @param tbl
	 *
	 * @throws Exception
	 */
	public JSONObject createSqlMSTAHS(String userId, String btnId, JSONArray dataArray, TblType tbl, SqlType sql){
		JSONObject result = new JSONObject();


		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();
		String values = "", names = "", rows = "";
		int colNum = MSTAHSLayout.values().length;		// テーブル列数
		if(TblType.JNL.getVal()==tbl.getVal()){
			colNum+=2;
		}
		if(TblType.CSV.getVal()==tbl.getVal()){
			colNum+=CSVCMNLayout.values().length;
		}
		for(int j=0; j < dataArray.size(); j++){
			values = "";
			names = "";
			for (int i = 1; i <= colNum; i++) {
				String col = "F" + i;
				String val = dataArray.optJSONObject(j).optString(col);

				if(i==MSTAHSLayout.SENDFLG.getNo()){		// 送信フラグ
					val = "0";
				}else if(i==MSTAHSLayout.OPERATOR.getNo()){		// オペレータ
					val = userId;
				}
				if(TblType.JNL.getVal()==tbl.getVal()){
					if(i==MSTAHSLayout.values().length+1){			// SEQ(ジャーナル用)
						val = jnlshn_seq;
					}else if(i==MSTAHSLayout.values().length+2){		// RENNO(ジャーナル用)
						val = (j+1)+"";
					}
				}
				// CSV特殊情報設定
				if(TblType.CSV.getVal()==tbl.getVal()){
					// SEQ以外dataArrayに追加済み前提
					if(i==MSTAHSLayout.values().length+1){			// SEQ(CSV用)
						val = csvshn_seq;
					}
				}
				if(StringUtils.isEmpty(val)){
					values += ", null";
				}else{
					if(isTest){
						values += ", '"+val+"'";
					}else{
						prmData.add(val);
						values += ", cast(? as varchar("+MessageUtility.getDefByteLen(val)+"))";
					}
				}
				names  += ", "+col;
			}
			rows += ",("+StringUtils.removeStart(values, ",")+")";
		}
		rows = StringUtils.removeStart(rows, ",");
		names  = StringUtils.removeStart(names, ",");

		StringBuffer sbSQL;
		sbSQL = new StringBuffer();
		sbSQL.append(this.createMergeCmnCommandMSTAHS(tbl, sql, rows, names));
		if(SqlType.INS.getVal() == sql.getVal()){
			sbSQL.append(" when not matched then ");
			sbSQL.append(" insert (");
			if(TblType.JNL.getVal()==tbl.getVal()){
				sbSQL.append("  SEQ");			// F1 : SEQ
				sbSQL.append(" ,RENNO");		// F2 : RENNO
				sbSQL.append(" ,");
			}
			if(TblType.CSV.getVal()==tbl.getVal()){
				sbSQL.append("  SEQ");			// F1 : SEQ
				sbSQL.append(" ,INPUTNO");		// F2 : 入力番号
				sbSQL.append(" ,INPUTEDANO");	// F3 : 入力枝番
				sbSQL.append(" ,");
			}
			sbSQL.append("  SHNCD");		// F1 : 商品コード
			sbSQL.append(" ,TENCD");		// F2 : 店コード
			sbSQL.append(" ,YOYAKUDT");		// F3 : マスタ変更予定日
			sbSQL.append(" ,AHSKB");		// F4 : 自動発注区分
			sbSQL.append(" ,SENDFLG");		// F5 : 送信フラグ
			sbSQL.append(" ,OPERATOR");		// F6 : オペレータ
			sbSQL.append(" ,ADDDT");		// F7 : 登録日
			sbSQL.append(" ,UPDDT");		// F8 : 更新日

			sbSQL.append(")values(");
			if(TblType.JNL.getVal()==tbl.getVal()){
				sbSQL.append("  RE.SEQ");			// F1 : SEQ
				sbSQL.append(" ,RE.RENNO");			// F2 : RENNO
				sbSQL.append(" ,");
			}
			if(TblType.CSV.getVal()==tbl.getVal()){
				sbSQL.append("  RE.SEQ");			// F1 : SEQ
				sbSQL.append(" ,RE.INPUTNO");		// F2 : 入力番号
				sbSQL.append(" ,RE.INPUTEDANO");	// F3 : 入力枝番
				sbSQL.append(" ,");
			}
			sbSQL.append("  RE.SHNCD");		// F1 : 商品コード
			sbSQL.append(" ,RE.TENCD");		// F2 : 店コード
			sbSQL.append(" ,RE.YOYAKUDT");	// F3 : マスタ変更予定日
			sbSQL.append(" ,RE.AHSKB");		// F4 : 自動発注区分
			sbSQL.append(" ,RE.SENDFLG");	// F6 : 送信フラグ
			sbSQL.append(" ,RE.OPERATOR");	// F7 : オペレータ
			sbSQL.append(" ,current timestamp");		// F8 : 登録日
			sbSQL.append(" ,current timestamp");		// F9 : 更新日

			sbSQL.append(" )");
		}
		if(SqlType.DEL.getVal() == sql.getVal()){
			sbSQL.append(" when matched then delete");
		}
		if (DefineReport.ID_DEBUG_MODE)	System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("自動発注管理マスタ" + tbl.getTxt());
		return result;
	}

	/**
	 * 自動発注管理マスタMergeSQL作成処理
	 *
	 * @param tbl
	 * @param sql
	 * @param values
	 * @param names
	 * @param userId
	 *
	 * @throws Exception
	 */
	public String createMergeCmnCommandMSTAHS(TblType tbl, SqlType sql, String values, String names) {

		String szTable = "INAMS.MSTAHS";
		String szWhere = "T.SHNCD = RE.SHNCD";
		if(SqlType.DEL.getVal()!=sql.getVal()){
			szWhere += " and T.TENCD = RE.TENCD";
		}
		if(TblType.YYK.getVal()==tbl.getVal()){
			szTable += "_Y";
			szWhere += " and T.YOYAKUDT = RE.YOYAKUDT ";
		}else if(TblType.JNL.getVal()==tbl.getVal()){
			szTable = "INAAD.JNLAHS";
			szWhere = "T.SEQ = RE.SEQ";
		}else if(TblType.CSV.getVal()==tbl.getVal()){
			szTable = "INAWS.CSVAHS";
			szWhere = "T.SEQ = RE.SEQ and T.INPUTNO = RE.INPUTNO";
		}

		// 基本Merge文
		StringBuffer sbSQL;
		sbSQL = new StringBuffer();
		sbSQL.append("merge into "+szTable+" as T");
		sbSQL.append(" using (select ");
		if(SqlType.DEL.getVal() ==  sql.getVal()){
			sbSQL.append("  cast("+MSTAHSLayout.SHNCD.getId()+" as "+MSTAHSLayout.SHNCD.getTyp()+") as "+MSTAHSLayout.SHNCD.getCol());			// 商品コード
			sbSQL.append(" ,cast("+MSTAHSLayout.YOYAKUDT.getId()+" as "+MSTAHSLayout.YOYAKUDT.getTyp()+") as "+MSTAHSLayout.YOYAKUDT.getCol());	// マスタ変更予定日
		}else{
			int baseCnt = MSTAHSLayout.values().length;
			for(MSTAHSLayout itm :MSTAHSLayout.values()){
				if(itm.getNo() > 1){ sbSQL.append(" ,"); }
				sbSQL.append("cast("+itm.getId() + " as "+itm.getTyp() + ") as " + itm.getCol());
			}
			if(TblType.JNL.getVal()==tbl.getVal()){
				for(JNLCMNLayout itm :JNLCMNLayout.values()){
					sbSQL.append(" ,cast("+itm.getId2(baseCnt) + " as "+itm.getTyp() + ") as " + itm.getCol());
				}
			}
			if(TblType.CSV.getVal()==tbl.getVal()){
				for(CSVCMNLayout itm :CSVCMNLayout.values()){
					sbSQL.append(" ,cast("+itm.getId2(baseCnt) + " as "+itm.getTyp() + ") as " + itm.getCol());
				}
			}
		}
		sbSQL.append(" from (values"+values+") as T1("+names+")");
		sbSQL.append(" ) as RE on ("+szWhere+") ");
		return sbSQL.toString();
	}



	/**
	 * メーカーマスタINSERT/UPDATE SQL作成処理
	 *
	 * @param userId
	 * @param data
	 * @param tbl
	 *
	 * @throws Exception
	 */
	public JSONObject createSqlMSTMAKER(String userId, String btnId, JSONObject data, TblType tbl, SqlType sql){
		JSONObject result = new JSONObject();



		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();
		String values = "", names = "";
		int colNum = 10;		// テーブル列数
		for (int i = 1; i <= colNum; i++) {
			String col = "F" + i;
			String val = data.optString(col);;
			if(i==6){				// 更新区分
				val = DefineReport.ValUpdkbn.NML.getVal();
			}else if(i==7){			// 送信フラグ
				val = DefineReport.Values.SENDFLG_UN.getVal();
			}else if(i==8){			// オペレータ
				val = userId;
			}
			if(StringUtils.isEmpty(val)){
				values += ", null";
			}else{
				if(isTest){
					values += ", '"+val+"'";
				}else{
					prmData.add(val);
					values += ", cast(? as varchar("+MessageUtility.getDefByteLen(val)+"))";
				}
			}
			names  += ", "+col;
		}
		values = "("+StringUtils.removeStart(values, ",")+")";
		names  = StringUtils.removeStart(names, ",");

		StringBuffer sbSQL;
		sbSQL = new StringBuffer();
		sbSQL.append(this.createMergeCmnCommandMSTMAKER(tbl, sql, values, names));
		if(SqlType.INS.getVal() == sql.getVal() || SqlType.MRG.getVal() == sql.getVal()){
			sbSQL.append(" when not matched then ");
			sbSQL.append(" insert (");
			sbSQL.append("  MAKERCD");		// F1 : メーカーコード
			sbSQL.append(" ,MAKERAN");		// F2 : メーカー名（カナ）
			sbSQL.append(" ,MAKERKN");		// F3 : メーカー名（漢字）
			sbSQL.append(" ,JANCD");		// F4 : JANコード
			sbSQL.append(" ,DMAKERCD");		// F5 : 代表メーカーコード
			sbSQL.append(" ,UPDKBN");		// F6 : 更新区分
			sbSQL.append(" ,SENDFLG");		// F7 : 送信フラグ
			sbSQL.append(" ,OPERATOR");		// F8 : オペレータ
			sbSQL.append(" ,ADDDT");		// F9 : 登録日
			sbSQL.append(" ,UPDDT");		// F10: 更新日
			sbSQL.append(")values(");
			sbSQL.append("  RE.MAKERCD");	// F1 : メーカーコード
			sbSQL.append(" ,RE.MAKERAN");	// F2 : メーカー名（カナ）
			sbSQL.append(" ,RE.MAKERKN");	// F3 : メーカー名（漢字）
			sbSQL.append(" ,RE.JANCD");		// F4 : JANコード
			sbSQL.append(" ,RE.DMAKERCD");	// F5 : 代表メーカーコード
			sbSQL.append(" ,RE.UPDKBN");	// F6 : 更新区分
			sbSQL.append(" ,RE.SENDFLG");	// F7 : 送信フラグ
			sbSQL.append(" ,RE.OPERATOR");	// F8 : オペレータ
			sbSQL.append(" ,current timestamp");		// F9 : 登録日
			sbSQL.append(" ,current timestamp");		// F10: 更新日
			sbSQL.append(" )");
		}
		if(SqlType.UPD.getVal() == sql.getVal() || SqlType.MRG.getVal() == sql.getVal()){
			sbSQL.append(" when matched then ");
			sbSQL.append(" update set");
			sbSQL.append("  MAKERCD=RE.MAKERCD");		// F1 : メーカーコード
			sbSQL.append(" ,MAKERAN=RE.MAKERAN");		// F2 : メーカー名（カナ）
			sbSQL.append(" ,MAKERKN=RE.MAKERKN");		// F3 : メーカー名（漢字）
			sbSQL.append(" ,JANCD=RE.JANCD");			// F4 : JANコード
			sbSQL.append(" ,DMAKERCD=RE.DMAKERCD");		// F5 : 代表メーカーコード
			sbSQL.append(" ,UPDKBN=RE.UPDKBN");			// F6 : 更新区分
			sbSQL.append(" ,SENDFLG=RE.SENDFLG");		// F7 : 送信フラグ
			sbSQL.append(" ,OPERATOR=RE.OPERATOR");		// F8 : オペレータ
			//sbSQL.append(" ,ADDDT=current timestamp");			// F9 : 登録日
			sbSQL.append(" ,UPDDT=current timestamp");			// F10: 更新日

		}
		if (DefineReport.ID_DEBUG_MODE)	System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("メーカーマスタ" + tbl.getTxt());
		return result;
	}

	/**
	 * メーカーマスタMergeSQL作成処理
	 *
	 * @param tbl
	 * @param sql
	 * @param values
	 * @param names
	 * @param userId
	 *
	 * @throws Exception
	 */
	public String createMergeCmnCommandMSTMAKER(TblType tbl, SqlType sql, String values, String names) {

		String szTable = "INAWS.PIMTIMAKER";
		String szWhere = "T.MAKERCD = RE.MAKERCD";
		if(TblType.YYK.getVal()==tbl.getVal()){
			szTable += "_Y";
			szWhere += " and T.YOYAKUDT = RE.YOYAKUDT ";
		}

		// 基本Merge文
		StringBuffer sbSQL;
		sbSQL = new StringBuffer();
		sbSQL.append("merge into "+szTable+" as T");
		sbSQL.append(" using (select");

		sbSQL.append("  cast(F1 as INTEGER) as MAKERCD");		// F1 : メーカーコード
		sbSQL.append(" ,cast(F2 as VARCHAR(20)) as MAKERAN");	// F2 : メーカー名（カナ）
		sbSQL.append(" ,cast(F3 as VARCHAR(40)) as MAKERKN");	// F3 : メーカー名（漢字）
		sbSQL.append(" ,cast(F4 as CHARACTER(14)) as JANCD");	// F4 : JANコード
		sbSQL.append(" ,cast(F5 as INTEGER) as DMAKERCD");		// F5 : 代表メーカーコード
		sbSQL.append(" ,cast(F6 as SMALLINT) as UPDKBN");		// F6 : 更新区分
		sbSQL.append(" ,cast(F7 as SMALLINT) as SENDFLG");		// F7 : 送信フラグ
		sbSQL.append(" ,cast(F8 as VARCHAR(20)) as OPERATOR");	// F8 : オペレータ
//		sbSQL.append(" ,cast(F9 as DATE) as ADDDT");			// F9 : 登録日
//		sbSQL.append(" ,cast(F10 as DATE) as UPDDT");			// F10: 更新日

		sbSQL.append(" from (values"+values+") as T1("+names+")");
		sbSQL.append(" ) as RE on ("+szWhere+") ");
		return sbSQL.toString();
	}

	/**
	 * 商品コード空き番管理テーブルINSERT/UPDATE SQL作成処理 TODO ※参照している人がいるので残してるだけ
	 *
	 * @param userId
	 * @param data
	 * @param tbl
	 *
	 * @throws Exception
	 */
	public JSONObject createSqlSYSSHNCD_AKI(String userId, String btnId, JSONObject data, TblType typ, SqlType sql){
		JSONObject inf = this.createSqlSYSSHNCD_AKI(userId, data, sql);

		JSONArray prm = inf.optJSONArray("PRM");
		ArrayList<String> prmData = new ArrayList<String>();
		for(int i = 0; i < prm.size(); i++){
			prmData.add(prm.optString(i));
		}

		sqlList.add(inf.optString("SQL"));
		prmList.add(prmData);
		lblList.add("商品コード空き番管理テーブル");
		return inf;
	}

	/**
	 * 商品コード空き番管理テーブルINSERT/UPDATE SQL作成処理
	 *
	 * @param userId
	 * @param data
	 * @param tbl
	 *
	 * @throws Exception
	 */
	public JSONObject createSqlSYSSHNCD_AKI(String userId, JSONObject data, SqlType sql){
		JSONObject result = new JSONObject();

		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();
		String values = "", names = "";
		int colNum = 3;		// テーブル列数
		for (int i = 1; i <= colNum; i++) {
			String col = "F" + i;
			String val = "";
			if(i==1){
				val = data.optString("F1");				// 商品コード
			}else if(i==2){
				val = data.optString("F2", "1");		// 使用済フラグ
			}

			if(StringUtils.isEmpty(val)){
				values += ", null";
			}else{
				prmData.add(val);
				values += ", cast(? as varchar("+MessageUtility.getDefByteLen(val)+"))";
			}

			names  += ", "+col;
		}
		values = "("+StringUtils.removeStart(values, ",")+")";
		names  = StringUtils.removeStart(names, ",");


		String szTable = "INAAD.SYSSHNCD_AKI";
		String szWhere = "T.SHNCD = RE.SHNCD";

		StringBuffer sbSQL;
		sbSQL = new StringBuffer();
		sbSQL.append("merge into "+szTable+" as T");
		sbSQL.append(" using (select");

		sbSQL.append("  cast(F1 as CHARACTER(14)) as SHNCD");	// F1 : 商品コード
		sbSQL.append(" ,cast(F2 as SMALLINT) as USEFLG");		// F2 : 使用済フラグ
		sbSQL.append(" ,current timestamp as UPDDT");			// F3 : 更新日

		sbSQL.append(" from (values"+values+") as T1("+names+")");
		sbSQL.append(" ) as RE on ("+szWhere+") ");

		if(SqlType.INS.getVal() == sql.getVal() || SqlType.MRG.getVal() == sql.getVal()){
			sbSQL.append(" when not matched then ");
			sbSQL.append(" insert (");
			sbSQL.append("  SHNCD");		// F1 : 商品コード
			sbSQL.append(" ,USEFLG");		// F2 : 使用済フラグ
			sbSQL.append(" ,UPDDT");		// F3 : 更新日
			sbSQL.append(" )values(");
			sbSQL.append("  RE.SHNCD");		// F1 : 商品コード
			sbSQL.append(" ,RE.USEFLG");	// F2 : 使用済フラグ
			sbSQL.append(" ,RE.UPDDT");		// F3 : 更新日
			sbSQL.append(" )");
		}
		if(SqlType.UPD.getVal() == sql.getVal() || SqlType.MRG.getVal() == sql.getVal()){
			sbSQL.append(" when matched then ");
			sbSQL.append(" update set");

			sbSQL.append("  SHNCD=RE.SHNCD");		// F1 : 商品コード
			sbSQL.append(" ,USEFLG=RE.USEFLG");		// F2 : 使用済フラグ
			sbSQL.append(" ,UPDDT=RE.UPDDT");		// F3 : 更新日

		}
		if (DefineReport.ID_DEBUG_MODE)	System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		result.put("SQL", sbSQL.toString());
		result.put("PRM", prmData);
		result.put("LBL", "商品コード空き番管理テーブル");
//		sqlList.add(sbSQL.toString());
//		prmList.add(prmData);
//		lblList.add("商品コード空き番管理テーブル");
		return result;
	}


	/**
	 * 販売コード付番管理テーブルINSERT/UPDATE SQL作成処理
	 *
	 * @param userId
	 * @param data
	 * @param tbl
	 *
	 * @throws Exception
	 */
	public JSONObject createSqlSYSURICD_FU(String userId, String btnId, JSONObject data, TblType tbl, SqlType sql){
		JSONObject result = new JSONObject();

		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();

		String szTable = "INAAD.SYSURICD_FU";
		String szWhere = "T.ID = RE.ID";

		StringBuffer sbSQL;
		sbSQL = new StringBuffer();
		sbSQL.append("merge into "+szTable+" as T");
		sbSQL.append(" using (");
		sbSQL.append("  select min(ID) as ID from "+szTable);
		sbSQL.append(" ) as RE on ("+szWhere+") ");
		if(SqlType.UPD.getVal() == sql.getVal()){
			sbSQL.append(" when matched then ");
			sbSQL.append(" update set");
			sbSQL.append("  SUMINO = case when SUMINO+1 <=ENDNO then SUMINO+1 else SUMINO end");		// F4 : 付番済番号
			sbSQL.append(" ,UPDDT= current timestamp");													// F5 : 更新日

		}
		if (DefineReport.ID_DEBUG_MODE)	System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("販売コード付番管理テーブル");
		return result;
	}

	/**
	 * 販売コード空き番管理テーブルINSERT/UPDATE SQL作成処理
	 *
	 * @param userId
	 * @param data
	 * @param tbl
	 *
	 * @throws Exception
	 */
	public JSONObject createSqlSYSURICD_AKI(String userId, String btnId, JSONObject data, TblType tbl, SqlType sql){
		JSONObject inf = this.createSqlSYSURICD_AKI(userId, data, sql);

		JSONArray prm = inf.optJSONArray("PRM");
		ArrayList<String> prmData = new ArrayList<String>();
		for(int i = 0; i < prm.size(); i++){
			prmData.add(prm.optString(i));
		}
		sqlList.add(inf.optString("SQL"));
		prmList.add(prmData);
		lblList.add("販売コード空き番管理テーブル");
		return inf;
	}

	/**
	 * 販売コード空き番管理テーブルINSERT/UPDATE SQL作成処理
	 *
	 * @param userId
	 * @param data
	 * @param tbl
	 *
	 * @throws Exception
	 */
	public JSONObject createSqlSYSURICD_AKI(String userId, JSONObject data, SqlType sql){
		JSONObject result = new JSONObject();

		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();
		String values = "", names = "";
		int colNum = 2;		// テーブル列数
		for (int i = 1; i <= colNum; i++) {
			String col = "F" + i;
			String val = "";
			if(i==1){
				val = data.optString("F1");		// F1 : 販売コード
			}else if(i==2){
				val = data.optString("F2");		// F2 : 使用済フラグ
			}

			if(StringUtils.isEmpty(val)){
				values += ", null";
			}else{
				prmData.add(val);
				values += ", cast(? as varchar("+MessageUtility.getDefByteLen(val)+"))";
			}
			names  += ", "+col;
		}
		values = "("+StringUtils.removeStart(values, ",")+")";
		names  = StringUtils.removeStart(names, ",");


		String szTable = "INAAD.SYSURICD_AKI";
		String szWhere = "T.URICD = RE.URICD";

		StringBuffer sbSQL;
		sbSQL = new StringBuffer();
		sbSQL.append("merge into "+szTable+" as T");
		sbSQL.append(" using (select");

		sbSQL.append("  cast(F1 as INTEGER) as URICD");		// F1 : 販売コード
		sbSQL.append(" ,cast(F2 as SMALLINT) as USEFLG");	// F2 : 使用済フラグ
		sbSQL.append(" ,current timestamp as UPDDT");		// F3 : 更新日

		sbSQL.append(" from (values"+values+") as T1("+names+")");
		sbSQL.append(" ) as RE on ("+szWhere+") ");

		if(SqlType.UPD.getVal() == sql.getVal() || SqlType.MRG.getVal() == sql.getVal()){
			sbSQL.append(" when matched then ");
			sbSQL.append(" update set");

			sbSQL.append("  URICD=RE.URICD");		// F1 : 販売コード
			sbSQL.append(" ,USEFLG=RE.USEFLG");		// F2 : 使用済フラグ
			sbSQL.append(" ,UPDDT=RE.UPDDT");		// F3 : 更新日

		}
		if (DefineReport.ID_DEBUG_MODE)	System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		result.put("SQL", sbSQL.toString());
		result.put("PRM", prmData);
		result.put("LBL", "販売コード空き番管理テーブル");

//		sqlList.add(sbSQL.toString());
//		prmList.add(prmData);
//		lblList.add("販売コード空き番管理テーブル");
		return result;
	}


	/**
	 * 商品更新件数マスタINSERT/UPDATE SQL作成処理
	 *
	 * @param userId
	 * @param data
	 * @param tbl
	 *
	 * @throws Exception
	 */
	public JSONObject createSqlSYSSHNCOUNT(String userId, String btnId, TblType tbl, SqlType sql) {
		JSONObject result = new JSONObject();

		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();

		String szTable = "INAAD.SYSSHNCOUNT";
		String szWhere = "T.UPDATEDT = RE.UPDATEDT";

		StringBuffer sbSQL;
		sbSQL = new StringBuffer();
		sbSQL.append("merge into "+szTable+" as T");
		sbSQL.append(" using (select");
		sbSQL.append("  cast(to_char(current date, 'YYYYMMDD') as integer) as UPDATEDT");	// F1 : 日付
		sbSQL.append(" ,cast(1 as SMALLINT) as UPDATECNT");									// F2 : 件数 insert用
		sbSQL.append(" ,current timestamp as UPDDT");										// F3 : 更新日
		sbSQL.append(" from SYSIBM.SYSDUMMY1");
		sbSQL.append(" ) as RE on ("+szWhere+") ");

		if(SqlType.INS.getVal() == sql.getVal() || SqlType.MRG.getVal() == sql.getVal()){
			sbSQL.append(" when not matched then ");
			sbSQL.append(" insert");
			sbSQL.append(" values(");
			sbSQL.append("  RE.UPDATEDT");				// F1 : 日付
			sbSQL.append(" ,RE.UPDATECNT");				// F2 : 件数
			sbSQL.append(" ,RE.UPDDT");					// F3 : 更新日
			sbSQL.append(" )");
		}
		if(SqlType.UPD.getVal() == sql.getVal() || SqlType.MRG.getVal() == sql.getVal()){
			sbSQL.append(" when matched then ");
			sbSQL.append(" update set");
			sbSQL.append("  UPDATECNT=UPDATECNT+1");	// F2 : 件数
			sbSQL.append(" ,UPDDT=RE.UPDDT");			// F3 : 更新日

		}
		if (DefineReport.ID_DEBUG_MODE)	System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("商品更新件数マスタ");
		return result;
	}

	// 処理日付取得
	public String getSHORIDT() {

		ItemList iL					 = new ItemList();
		StringBuffer sbSQL			 = new StringBuffer();
		ArrayList<String> paramData	 = new ArrayList<String>();

		String  value = "";

		sbSQL.append(DefineReport.ID_SQLSHORIDT);
		@SuppressWarnings("static-access")
		JSONArray array = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);
		if(array.size() > 0){
			value = array.getJSONObject(0).optString("VALUE");
		}
		return value;
	}

	/** マスタレイアウト */
	public interface MSTLayout {
		public Integer getNo();
		public String getCol();
		public String getTyp();
		public String getTxt();
		public String getId();
		public DataType getDataType();
		public boolean isText();
	}

	/**  商品マスタレイアウト() */
	public enum MSTSHNLayout implements MSTLayout{
		/** 商品コード */
		SHNCD(1,"SHNCD","CHARACTER(14)","商品コード"),
		/** マスタ変更予定日 */
		YOYAKUDT(2,"YOYAKUDT","INTEGER","マスタ変更予定日"),
		/** 店売価実施日 */
		TENBAIKADT(3,"TENBAIKADT","INTEGER","店売価実施日"),
		/** 用途分類コード_部門 */
		YOT_BMNCD(4,"YOT_BMNCD","SMALLINT","用途分類コード_部門"),
		/** 用途分類コード_大 */
		YOT_DAICD(5,"YOT_DAICD","SMALLINT","用途分類コード_大"),
		/** 用途分類コード_中 */
		YOT_CHUCD(6,"YOT_CHUCD","SMALLINT","用途分類コード_中"),
		/** 用途分類コード_小 */
		YOT_SHOCD(7,"YOT_SHOCD","SMALLINT","用途分類コード_小"),
		/** 売場分類コード_部門 */
		URI_BMNCD(8,"URI_BMNCD","SMALLINT","売場分類コード_部門"),
		/** 売場分類コード_大 */
		URI_DAICD(9,"URI_DAICD","SMALLINT","売場分類コード_大"),
		/** 売場分類コード_中 */
		URI_CHUCD(10,"URI_CHUCD","SMALLINT","売場分類コード_中"),
		/** 売場分類コード_小 */
		URI_SHOCD(11,"URI_SHOCD","SMALLINT","売場分類コード_小"),
		/** 標準分類コード_部門 */
		BMNCD(12,"BMNCD","SMALLINT","標準分類コード_部門"),
		/** 標準分類コード_大 */
		DAICD(13,"DAICD","SMALLINT","標準分類コード_大"),
		/** 標準分類コード_中 */
		CHUCD(14,"CHUCD","SMALLINT","標準分類コード_中"),
		/** 標準分類コード_小 */
		SHOCD(15,"SHOCD","SMALLINT","標準分類コード_小"),
		/** 標準分類コード_小小 */
		SSHOCD(16,"SSHOCD","SMALLINT","標準分類コード_小小"),
		/** 取扱期間_開始日 */
		ATSUK_STDT(17,"ATSUK_STDT","INTEGER","取扱期間_開始日"),
		/** 取扱期間_終了日 */
		ATSUK_EDDT(18,"ATSUK_EDDT","INTEGER","取扱期間_終了日"),
		/** 取扱停止 */
		TEISHIKBN(19,"TEISHIKBN","SMALLINT","取扱停止"),
		/** 商品名（カナ） */
		SHNAN(20,"SHNAN","VARCHAR(20)","商品名（カナ）"),
		/** 商品名（漢字） */
		SHNKN(21,"SHNKN","VARCHAR(40)","商品名（漢字）"),
		/** プライスカード商品名称（漢字） */
		PCARDKN(22,"PCARDKN","VARCHAR(40)","プライスカード商品名称（漢字）"),
		/** POP名称 */
		POPKN(23,"POPKN","VARCHAR(40)","POP名称"),
		/** レシート名（カナ） */
		RECEIPTAN(24,"RECEIPTAN","VARCHAR(20)","レシート名（カナ）"),
		/** レシート名（漢字） */
		RECEIPTKN(25,"RECEIPTKN","VARCHAR(40)","レシート名（漢字）"),
		/** PC区分 */
		PCKBN(26,"PCKBN","SMALLINT","PC区分"),
		/** 加工区分 */
		KAKOKBN(27,"KAKOKBN","SMALLINT","加工区分"),
		/** 市場区分 */
		ICHIBAKBN(28,"ICHIBAKBN","SMALLINT","市場区分"),
		/** 商品種類 */
		SHNKBN(29,"SHNKBN","SMALLINT","商品種類"),
		/** 産地 */
		SANCHIKN(30,"SANCHIKN","VARCHAR(40)","産地"),
		/** 標準仕入先コード */
		SSIRCD(31,"SSIRCD","INTEGER","標準仕入先コード"),
		/** 配送パターン */
		HSPTN(32,"HSPTN","SMALLINT","配送パターン"),
		/** レギュラー情報_取扱フラグ */
		RG_ATSUKFLG(33,"RG_ATSUKFLG","SMALLINT","レギュラー情報_取扱フラグ"),
		/** レギュラー情報_原価 */
		RG_GENKAAM(34,"RG_GENKAAM","DECIMAL(8,2)","レギュラー情報_原価"),
		/** レギュラー情報_売価 */
		RG_BAIKAAM(35,"RG_BAIKAAM","INTEGER","レギュラー情報_売価"),
		/** レギュラー情報_店入数 */
		RG_IRISU(36,"RG_IRISU","SMALLINT","レギュラー情報_店入数"),
		/** レギュラー情報_一括伝票フラグ */
		RG_IDENFLG(37,"RG_IDENFLG","CHARACTER(1)","レギュラー情報_一括伝票フラグ"),
		/** レギュラー情報_ワッペン */
		RG_WAPNFLG(38,"RG_WAPNFLG","CHARACTER(1)","レギュラー情報_ワッペン"),
		/** 販促情報_取扱フラグ */
		HS_ATSUKFLG(39,"HS_ATSUKFLG","SMALLINT","販促情報_取扱フラグ"),
		/** 販促情報_原価 */
		HS_GENKAAM(40,"HS_GENKAAM","DECIMAL(8,2)","販促情報_原価"),
		/** 販促情報_売価 */
		HS_BAIKAAM(41,"HS_BAIKAAM","INTEGER","販促情報_売価"),
		/** 販促情報_店入数 */
		HS_IRISU(42,"HS_IRISU","SMALLINT","販促情報_店入数"),
		/** 販促情報_ワッペン */
		HS_WAPNFLG(43,"HS_WAPNFLG","CHARACTER(1)","販促情報_ワッペン"),
		/** 販促情報_スポット最低発注数 */
		HS_SPOTMINSU(44,"HS_SPOTMINSU","SMALLINT","販促情報_スポット最低発注数"),
		/** 販促情報_特売ワッペン */
		HP_SWAPNFLG(45,"HP_SWAPNFLG","CHARACTER(1)","販促情報_特売ワッペン"),
		/** 規格名称 */
		KIKKN(46,"KIKKN","VARCHAR(46)","規格名称"),
		/** ユニットプライス_容量 */
		UP_YORYOSU(47,"UP_YORYOSU","INTEGER","ユニットプライス_容量"),
		/** ユニットプライス_単位容量 */
		UP_TYORYOSU(48,"UP_TYORYOSU","SMALLINT","ユニットプライス_単位容量"),
		/** ユニットプライス_ユニット単位 */
		UP_TANIKBN(49,"UP_TANIKBN","SMALLINT","ユニットプライス_ユニット単位"),
		/** 商品サイズ_横 */
		SHNYOKOSZ(50,"SHNYOKOSZ","SMALLINT","商品サイズ_横"),
		/** 商品サイズ_縦 */
		SHNTATESZ(51,"SHNTATESZ","SMALLINT","商品サイズ_縦"),
		/** 商品サイズ_奥行 */
		SHNOKUSZ(52,"SHNOKUSZ","SMALLINT","商品サイズ_奥行"),
		/** 商品サイズ_重量 */
		SHNJRYOSZ(53,"SHNJRYOSZ","DECIMAL(6,1)","商品サイズ_重量"),
		/** PB区分 */
		PBKBN(54,"PBKBN","SMALLINT","PB区分"),
		/** 小物区分 */
		KOMONOKBM(55,"KOMONOKBM","SMALLINT","小物区分"),
		/** 棚卸区分 */
		TANAOROKBN(56,"TANAOROKBN","SMALLINT","棚卸区分"),
		/** 定計区分 */
		TEIKEIKBN(57,"TEIKEIKBN","SMALLINT","定計区分"),
		/** ODS_賞味期限_春 */
		ODS_HARUSU(58,"ODS_HARUSU","SMALLINT","ODS_賞味期限_春"),
		/** ODS_賞味期限_夏 */
		ODS_NATSUSU(59,"ODS_NATSUSU","SMALLINT","ODS_賞味期限_夏"),
		/** ODS_賞味期限_秋 */
		ODS_AKISU(60,"ODS_AKISU","SMALLINT","ODS_賞味期限_秋"),
		/** ODS_賞味期限_冬 */
		ODS_FUYUSU(61,"ODS_FUYUSU","SMALLINT","ODS_賞味期限_冬"),
		/** ODS_入荷期限 */
		ODS_NYUKASU(62,"ODS_NYUKASU","SMALLINT","ODS_入荷期限"),
		/** ODS_値引期限 */
		ODS_NEBIKISU(63,"ODS_NEBIKISU","SMALLINT","ODS_値引期限"),
		/** プライスカード_種類 */
		PCARD_SHUKBN(64,"PCARD_SHUKBN","SMALLINT","プライスカード_種類"),
		/** プライスカード_色 */
		PCARD_IROKBN(65,"PCARD_IROKBN","SMALLINT","プライスカード_色"),
		/** 税区分 */
		ZEIKBN(66,"ZEIKBN","SMALLINT","税区分"),
		/** 税率区分 */
		ZEIRTKBN(67,"ZEIRTKBN","SMALLINT","税率区分"),
		/** 旧税率区分 */
		ZEIRTKBN_OLD(68,"ZEIRTKBN_OLD","SMALLINT","旧税率区分"),
		/** 税率変更日 */
		ZEIRTHENKODT(69,"ZEIRTHENKODT","INTEGER","税率変更日"),
		/** 製造限度日数 */
		SEIZOGENNISU(70,"SEIZOGENNISU","SMALLINT","製造限度日数"),
		/** 定貫不定貫区分 */
		TEIKANKBN(71,"TEIKANKBN","SMALLINT","定貫不定貫区分"),
		/** メーカーコード */
		MAKERCD(72,"MAKERCD","INTEGER","メーカーコード"),
		/** 輸入区分 */
		IMPORTKBN(73,"IMPORTKBN","SMALLINT","輸入区分"),
		/** 仕分区分 */
		SIWAKEKBN(74,"SIWAKEKBN","SMALLINT","仕分区分"),
		/** 返品区分 */
		HENPIN_KBN(75,"HENPIN_KBN","SMALLINT","返品区分"),
		/** 対象年齢 */
		TAISHONENSU(76,"TAISHONENSU","SMALLINT","対象年齢"),
		/** カロリー表示 */
		CALORIESU(77,"CALORIESU","SMALLINT","カロリー表示"),
		/** フラグ情報_ELP */
		ELPFLG(78,"ELPFLG","SMALLINT","フラグ情報_ELP"),
		/** フラグ情報_ベルマーク */
		BELLMARKFLG(79,"BELLMARKFLG","SMALLINT","フラグ情報_ベルマーク"),
		/** フラグ情報_リサイクル */
		RECYCLEFLG(80,"RECYCLEFLG","SMALLINT","フラグ情報_リサイクル"),
		/** フラグ情報_エコマーク */
		ECOFLG(81,"ECOFLG","SMALLINT","フラグ情報_エコマーク"),
		/** 包材用途 */
		HZI_YOTO(82,"HZI_YOTO","SMALLINT","包材用途"),
		/** 包材材質 */
		HZI_ZAISHITU(83,"HZI_ZAISHITU","SMALLINT","包材材質"),
		/** 包材リサイクル対象 */
		HZI_RECYCLE(84,"HZI_RECYCLE","SMALLINT","包材リサイクル対象"),
		/** 期間 */
		KIKANKBN(85,"KIKANKBN","CHARACTER(1)","期間"),
		/** 酒級 */
		SHUKYUKBN(86,"SHUKYUKBN","SMALLINT","酒級"),
		/** 度数 */
		DOSU(87,"DOSU","SMALLINT","度数"),
		/** 陳列形式コード */
		CHINRETUCD(88,"CHINRETUCD","CHARACTER(1)","陳列形式コード"),
		/** 段積み形式コード */
		DANTUMICD(89,"DANTUMICD","CHARACTER(2)","段積み形式コード"),
		/** 重なりコード */
		KASANARICD(90,"KASANARICD","CHARACTER(1)","重なりコード"),
		/** 重なりサイズ */
		KASANARISZ(91,"KASANARISZ","SMALLINT","重なりサイズ"),
		/** 圧縮率 */
		ASSHUKURT(92,"ASSHUKURT","SMALLINT","圧縮率"),
		/** 種別コード */
		SHUBETUCD(93,"SHUBETUCD","CHARACTER(2)","種別コード"),
		/** 販売コード */
		URICD(94,"URICD","INTEGER","販売コード"),
		/** 商品コピー・セールスコメント */
		SALESCOMKN(95,"SALESCOMKN","VARCHAR(60)","商品コピー・セールスコメント"),
		/** 裏貼 */
		URABARIKBN(96,"URABARIKBN","SMALLINT","裏貼"),
		/** プライスカード出力有無 */
		PCARD_OPFLG(97,"PCARD_OPFLG","SMALLINT","プライスカード出力有無"),
		/** 親商品コード */
		PARENTCD(98,"PARENTCD","CHARACTER(14)","親商品コード"),
		/** 便区分 */
		BINKBN(99,"BINKBN","SMALLINT","便区分"),
		/** 発注曜日_月 */
		HAT_MONKBN(100,"HAT_MONKBN","SMALLINT","発注曜日_月"),
		/** 発注曜日_火 */
		HAT_TUEKBN(101,"HAT_TUEKBN","SMALLINT","発注曜日_火"),
		/** 発注曜日_水 */
		HAT_WEDKBN(102,"HAT_WEDKBN","SMALLINT","発注曜日_水"),
		/** 発注曜日_木 */
		HAT_THUKBN(103,"HAT_THUKBN","SMALLINT","発注曜日_木"),
		/** 発注曜日_金 */
		HAT_FRIKBN(104,"HAT_FRIKBN","SMALLINT","発注曜日_金"),
		/** 発注曜日_土 */
		HAT_SATKBN(105,"HAT_SATKBN","SMALLINT","発注曜日_土"),
		/** 発注曜日_日 */
		HAT_SUNKBN(106,"HAT_SUNKBN","SMALLINT","発注曜日_日"),
		/** リードタイムパターン */
		READTMPTN(107,"READTMPTN","SMALLINT","リードタイムパターン"),
		/** 締め回数 */
		SIMEKAISU(108,"SIMEKAISU","SMALLINT","締め回数"),
		/** 衣料使い回しフラグ */
		IRYOREFLG(109,"IRYOREFLG","SMALLINT","衣料使い回しフラグ"),
		/** 登録元 */
		TOROKUMOTO(110,"TOROKUMOTO","SMALLINT","登録元"),
		/** 更新区分 */
		UPDKBN(111,"UPDKBN","SMALLINT","更新区分"),
		/** 送信フラグ */
		SENDFLG(112,"SENDFLG","SMALLINT","送信フラグ"),
		/** オペレータ */
		OPERATOR(113,"OPERATOR","VARCHAR(20)","オペレータ"),
		/** 登録日 */
		ADDDT(114,"ADDDT","TIMESTAMP","登録日"),
		/** 更新日 */
		UPDDT(115,"UPDDT","TIMESTAMP","更新日"),
		/** 保温区分 */
		K_HONKB(116,"K_HONKB","SMALLINT","保温区分"),
		/** デリカワッペン区分_レギュラー */
		K_WAPNFLG_R(117,"K_WAPNFLG_R","SMALLINT","デリカワッペン区分_レギュラー"),
		/** デリカワッペン区分_販促 */
		K_WAPNFLG_H(118,"K_WAPNFLG_H","SMALLINT","デリカワッペン区分_販促"),
		/** 取扱区分 */
		K_TORIKB(119,"K_TORIKB","SMALLINT","取扱区分"),
		/** ITFコード */
		ITFCD(120,"ITFCD","CHARACTER(14)","ITFコード"),
		/** センター入数 */
		CENTER_IRISU(121,"CENTER_IRISU","SMALLINT","センター入数");

		private final Integer no;
		private final String col;
		private final String txt;
		private final String typ;
		/** 初期化 */
		private MSTSHNLayout(Integer no, String col, String typ, String txt) {
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

	/**  ソースコード管理マスタレイアウト() */
	public enum MSTSRCCDLayout implements MSTLayout{
		/** 商品コード */
		SHNCD(1,"SHNCD","CHARACTER(14)","商品コード"),
		/** ソースコード */
		SRCCD(2,"SRCCD","CHARACTER(14)","ソースコード"),
		/** マスタ変更予定日 */
		YOYAKUDT(3,"YOYAKUDT","INTEGER","マスタ変更予定日"),
		/** 入力順番 */
		SEQNO(4,"SEQNO","SMALLINT","入力順番"),
		/** ソース区分 */
		SOURCEKBN(5,"SOURCEKBN","SMALLINT","ソース区分"),
		/** 送信フラグ */
		SENDFLG(6,"SENDFLG","SMALLINT","送信フラグ"),
		/** オペレータ */
		OPERATOR(7,"OPERATOR","VARCHAR(20)","オペレータ"),
		/** 登録日 */
		ADDDT(8,"ADDDT","TIMESTAMP","登録日"),
		/** 更新日 */
		UPDDT(9,"UPDDT","TIMESTAMP","更新日"),
		/** 有効開始日 */
		YUKO_STDT(10,"YUKO_STDT","INTEGER","有効開始日"),
		/** 有効終了日 */
		YUKO_EDDT(11,"YUKO_EDDT","INTEGER","有効終了日");

		private final Integer no;
		private final String col;
		private final String txt;
		private final String typ;
		/** 初期化 */
		private MSTSRCCDLayout(Integer no, String col, String typ, String txt) {
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


	/**  仕入グループ商品マスタレイアウト() */
	public enum MSTSIRGPSHNLayout implements MSTLayout{
		/** 商品コード */
		SHNCD(1,"SHNCD","CHARACTER(14)","商品コード"),
		/** 店グループ */
		TENGPCD(2,"TENGPCD","SMALLINT","店グループ"),
		/** マスタ変更予定日 */
		YOYAKUDT(3,"YOYAKUDT","INTEGER","マスタ変更予定日"),
		/** エリア区分 */
		AREAKBN(4,"AREAKBN","SMALLINT","エリア区分"),
		/** 仕入先コード */
		SIRCD(5,"SIRCD","INTEGER","仕入先コード"),
		/** 配送パターン */
		HSPTN(6,"HSPTN","SMALLINT","配送パターン"),
		/** 送信フラグ */
		SENDFLG(7,"SENDFLG","SMALLINT","送信フラグ"),
		/** オペレータ */
		OPERATOR(8,"OPERATOR","VARCHAR(20)","オペレータ"),
		/** 登録日 */
		ADDDT(9,"ADDDT","TIMESTAMP","登録日"),
		/** 更新日 */
		UPDDT(10,"UPDDT","TIMESTAMP","更新日");

		private final Integer no;
		private final String col;
		private final String txt;
		private final String typ;
		/** 初期化 */
		private MSTSIRGPSHNLayout(Integer no, String col, String typ, String txt) {
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
		/** @return idx 列Index */
		public Integer getIdx() { return no-1; }
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


	/**  売価コントロールマスタレイアウト() */
	public enum MSTBAIKACTLLayout implements MSTLayout{
		/** 商品コード */
		SHNCD(1,"SHNCD","CHARACTER(14)","商品コード"),
		/** 店グループ */
		TENGPCD(2,"TENGPCD","SMALLINT","店グループ"),
		/** マスタ変更予定日 */
		YOYAKUDT(3,"YOYAKUDT","INTEGER","マスタ変更予定日"),
		/** エリア区分 */
		AREAKBN(4,"AREAKBN","SMALLINT","エリア区分"),
		/** 原価 */
		GENKAAM(5,"GENKAAM","DECIMAL(8,2)","原価"),
		/** 売価 */
		BAIKAAM(6,"BAIKAAM","INTEGER","売価"),
		/** 店入数 */
		IRISU(7,"IRISU","SMALLINT","店入数"),
		/** 送信フラグ */
		SENDFLG(8,"SENDFLG","SMALLINT","送信フラグ"),
		/** オペレータ */
		OPERATOR(9,"OPERATOR","VARCHAR(20)","オペレータ"),
		/** 登録日 */
		ADDDT(10,"ADDDT","TIMESTAMP","登録日"),
		/** 更新日 */
		UPDDT(11,"UPDDT","TIMESTAMP","更新日");

		private final Integer no;
		private final String col;
		private final String txt;
		private final String typ;
		/** 初期化 */
		private MSTBAIKACTLLayout(Integer no, String col, String typ, String txt) {
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
		/** @return idx 列Index */
		public Integer getIdx() { return no-1; }
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


	/**  品揃グループマスタレイアウト() */
	public enum MSTSHINAGPLayout implements MSTLayout{
		/** 商品コード */
		SHNCD(1,"SHNCD","CHARACTER(14)","商品コード"),
		/** 店グループ */
		TENGPCD(2,"TENGPCD","SMALLINT","店グループ"),
		/** マスタ変更予定日 */
		YOYAKUDT(3,"YOYAKUDT","INTEGER","マスタ変更予定日"),
		/** エリア区分 */
		AREAKBN(4,"AREAKBN","SMALLINT","エリア区分"),
		/** 扱い区分 */
		ATSUKKBN(5,"ATSUKKBN","SMALLINT","扱い区分"),
		/** 送信フラグ */
		SENDFLG(6,"SENDFLG","SMALLINT","送信フラグ"),
		/** オペレータ */
		OPERATOR(7,"OPERATOR","VARCHAR(20)","オペレータ"),
		/** 登録日 */
		ADDDT(8,"ADDDT","TIMESTAMP","登録日"),
		/** 更新日 */
		UPDDT(9,"UPDDT","TIMESTAMP","更新日");

		private final Integer no;
		private final String col;
		private final String txt;
		private final String typ;
		/** 初期化 */
		private MSTSHINAGPLayout(Integer no, String col, String typ, String txt) {
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

	/**  店別異部門管理レイアウト() */
	public enum MSTSHNTENBMNLayout implements MSTLayout{
		/** 商品コード */
		SHNCD(1,"SHNCD","CHARACTER(14)","商品コード"),
		/** 店別異部門商品コード */
		TENSHNCD(2,"TENSHNCD","CHARACTER(14)","店別異部門商品コード"),
		/** 店グループ */
		TENGPCD(3,"TENGPCD","SMALLINT","店グループ"),
		/** マスタ変更予定日 */
		YOYAKUDT(4,"YOYAKUDT","INTEGER","マスタ変更予定日"),
		/** エリア区分 */
		AREAKBN(5,"AREAKBN","SMALLINT","エリア区分"),
		/** 送信フラグ */
		SENDFLG(6,"SENDFLG","SMALLINT","送信フラグ"),
		/** オペレータ */
		OPERATOR(7,"OPERATOR","VARCHAR(20)","オペレータ"),
		/** 登録日 */
		ADDDT(8,"ADDDT","TIMESTAMP","登録日"),
		/** 更新日 */
		UPDDT(9,"UPDDT","TIMESTAMP","更新日"),
		/** ソースコード */
		SRCCD(10,"SRCCD","CHARACTER(14)","JANコード");

		private final Integer no;
		private final String col;
		private final String txt;
		private final String typ;
		/** 初期化 */
		private MSTSHNTENBMNLayout(Integer no, String col, String typ, String txt) {
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




	/**  添加物マスタレイアウト() */
	public enum MSTTENKABUTSULayout implements MSTLayout{
		/** 商品コード */
		SHNCD(1,"SHNCD","CHARACTER(14)","商品コード"),
		/** 添加物区分 */
		TENKABKBN(2,"TENKABKBN","SMALLINT","添加物区分"),
		/** 添加物コード */
		TENKABCD(3,"TENKABCD","SMALLINT","添加物コード"),
		/** マスタ変更予定日 */
		YOYAKUDT(4,"YOYAKUDT","INTEGER","マスタ変更予定日"),
		/** 送信フラグ */
		SENDFLG(5,"SENDFLG","SMALLINT","送信フラグ"),
		/** オペレータ */
		OPERATOR(6,"OPERATOR","VARCHAR(20)","オペレータ"),
		/** 登録日 */
		ADDDT(7,"ADDDT","TIMESTAMP","登録日"),
		/** 更新日 */
		UPDDT(8,"UPDDT","TIMESTAMP","更新日");

		private final Integer no;
		private final String col;
		private final String txt;
		private final String typ;
		/** 初期化 */
		private MSTTENKABUTSULayout(Integer no, String col, String typ, String txt) {
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

	/**  グループ分類管理マスタレイアウト() */
	public enum MSTGRPLayout implements MSTLayout{
		/** 商品コード */
		SHNCD(1,"SHNCD","CHARACTER(14)","商品コード"),
		/** グループ分類ID */
		GRPID(2,"GRPID","INTEGER","グループ分類ID"),
		/** マスタ変更予定日 */
		YOYAKUDT(3,"YOYAKUDT","INTEGER","マスタ変更予定日"),
		/** 入力順番 */
		SEQNO(4,"SEQNO","SMALLINT","入力順番"),
		/** 送信フラグ */
		SENDFLG(5,"SENDFLG","SMALLINT","送信フラグ"),
		/** オペレータ */
		OPERATOR(6,"OPERATOR","VARCHAR(20)","オペレータ"),
		/** 登録日 */
		ADDDT(7,"ADDDT","TIMESTAMP","登録日"),
		/** 更新日 */
		UPDDT(8,"UPDDT","TIMESTAMP","更新日"),
		/** グループ分類名(別テーブル) */
		GRPKN(9,"GRPKN","VARCHAR(100)", "グループ分類名");

		private final Integer no;
		private final String col;
		private final String txt;
		private final String typ;
		/** 初期化 */
		private MSTGRPLayout(Integer no, String col, String typ, String txt) {
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

	/**  自動発注管理マスタレイアウト() */
	public enum MSTAHSLayout implements MSTLayout{
		/** 商品コード */
		SHNCD(1,"SHNCD","CHARACTER(14)","商品コード"),
		/** 店コード */
		TENCD(2,"TENCD","SMALLINT","店コード"),
		/** マスタ変更予定日 */
		YOYAKUDT(3,"YOYAKUDT","INTEGER","マスタ変更予定日"),
		/** 自動発注区分 */
		AHSKB(4,"AHSKB","CHARACTER(1)","自動発注区分"),
		/** 送信フラグ */
		SENDFLG(5,"SENDFLG","SMALLINT","送信フラグ"),
		/** オペレータ */
		OPERATOR(6,"OPERATOR","VARCHAR(20)","オペレータ"),
		/** 登録日 */
		ADDDT(7,"ADDDT","TIMESTAMP","登録日"),
		/** 更新日 */
		UPDDT(8,"UPDDT","TIMESTAMP","更新日");

		private final Integer no;
		private final String col;
		private final String txt;
		private final String typ;
		/** 初期化 */
		private MSTAHSLayout(Integer no, String col, String typ, String txt) {
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



	/**  ジャーナル_商品マスタレイアウト(正マスタとの差分) */
	public enum JNLSHNLayout implements MSTLayout{
		/** SEQ */
		SEQ(1,"SEQ","INTEGER","SEQ"),
		/** 更新情報_更新日時 */
		INF_DATE(2,"INF_DATE","TIMESTAMP","更新情報_更新日時"),
		/** 更新情報_オペレータ */
		INF_OPERATOR(3,"INF_OPERATOR","VARCHAR(20)","更新情報_オペレータ"),
		/** 更新情報_テーブル区分 */
		INF_TABLEKBN(4,"INF_TABLEKBN","SMALLINT","更新情報_テーブル区分"),
		/** 更新情報_処理区分 */
		INF_TRANKBN(5,"INF_TRANKBN","SMALLINT","更新情報_処理区分");

		private final Integer no;
		private final String col;
		private final String txt;
		private final String typ;
		/** 初期化 */
		private JNLSHNLayout(Integer no, String col, String typ, String txt) {
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
		public String getId() { return "F" + Integer.toString(no) ;}
		/** @return col Id */
		public String getId2() { return "F" + Integer.toString(no+MSTSHNLayout.values().length) ;}
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
	}

	/**  CSV取込トラン_商品マスタレイアウト(正マスタとの差分) */
	public enum CSVSHNLayout implements MSTLayout{
		/** SEQ */
		SEQ(1,"SEQ","INTEGER","SEQ"),
		/** 入力番号 */
		INPUTNO(2,"INPUTNO","INTEGER","入力番号"),
		/** エラーコード */
		ERRCD(3,"ERRCD","SMALLINT","エラーコード"),
		/** エラー箇所 */
		ERRFLD(4,"ERRFLD","VARCHAR(100)","エラー箇所"),
		/** エラー値 */
		ERRVL(5,"ERRVL","VARCHAR(100)","エラー値"),
		/** エラーテーブル名 */
		ERRTBLNM(6,"ERRTBLNM","VARCHAR(100)","エラーテーブル名"),
		/** CSV登録区分 */
		CSV_UPDKBN(7,"CSV_UPDKBN","CHARACTER(1)","CSV登録区分"),
		/** 桁指定 */
		KETAKBN(8,"KETAKBN","SMALLINT","桁指定");

		private final Integer no;
		private final String col;
		private final String txt;
		private final String typ;
		/** 初期化 */
		private CSVSHNLayout(Integer no, String col, String typ, String txt) {
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
		public String getId() { return "F" + Integer.toString(no) ;}
		/** @return col Id */
		public String getId2() { return "F" + Integer.toString(no+MSTSHNLayout.values().length) ;}
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
	}

	/**  ジャーナル_子テーブル共通レイアウト(正マスタとの差分)  */
	public enum JNLCMNLayout implements MSTLayout{
		/** SEQ */
		SEQ(1,"SEQ","INTEGER","SEQ"),
		/** 連番 */
		RENNO(2,"RENNO","SMALLINT","連番");

		private final Integer no;
		private final String col;
		private final String txt;
		private final String typ;
		/** 初期化 */
		private JNLCMNLayout(Integer no, String col, String typ, String txt) {
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
		public String getId() { return "F" + Integer.toString(no) ; }
		/** @return col Id2 */
		public String getId2(Integer idx) { return "F" + Integer.toString(no + idx) ; }
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
	}

	/**  CSV取込トラン_子テーブル共通レイアウト(正マスタとの差分)  */
	public enum CSVCMNLayout implements MSTLayout{
		/** SEQ */
		SEQ(1,"SEQ","INTEGER","SEQ"),
		/** 入力番号 */
		INPUTNO(2,"INPUTNO","INTEGER","入力番号"),
		/** 入力枝番 */
		INPUTEDANO(3,"INPUTEDANO","SMALLINT","入力枝番");

		private final Integer no;
		private final String col;
		private final String txt;
		private final String typ;
		/** 初期化 */
		private CSVCMNLayout(Integer no, String col, String typ, String txt) {
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
		public String getId() { return "F" + Integer.toString(no) ; }
		/** @return col Id2 */
		public String getId2(Integer idx) { return "F" + Integer.toString(no + idx) ; }
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
	}

}
