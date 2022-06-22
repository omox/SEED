package dao;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.JDBCType;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import authentication.bean.User;
import authentication.defines.Consts;
import common.CmnDate;
import common.DefineReport;
import common.DefineReport.DataType;
import common.DefineReport.InfTrankbn;
import common.Defines;
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
public class Reportx251Dao extends Reportx002Dao {

	/**
	 * インスタンスを生成します。
	 * @param source
	 */
	public Reportx251Dao(String JNDIname) {
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

		String szStcdShikakari	= map.get("STATE_S");			// 状態_仕掛商品

		// 更新情報チェック(基本JS側で制御)
		JSONObject option = new JSONObject();

		JSONObject objset = check(map, userInfo, sysdate);
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
				option.put(MsgKey.S.getKey(), msgList.optJSONObject(0));
			}
			if(szStcdShikakari.equals("04")){
				// 承認時は通常通り発行する
				msgList.add(MessageUtility.getDbMessageIdObj("I00002", new String[]{"商品コード"+msgShnCd+" 販売コード"+msgUriCd+"<br>"+"商品マスタ、仕掛商品マスタに"}));
				option.put(MsgKey.S.getKey(), msgList.optJSONObject(0));
			}
		}
		if (DefineReport.ID_DEBUG_MODE)	System.out.println("msgShnCd="+msgShnCd);


		// エラー時
		if(option.containsKey(MsgKey.E.getKey())){
			// 採番実行時にエラーの場合、解除処理
			JSONObject dataOther = objset.optJSONObject("DATA_OTHER");
			if(StringUtils.isNotEmpty(dataOther.optString(MSTSHNLayout.SHNCD.getCol()+"_RENEW"))){
				// 商品コード情報、もしくはエラー情報が返ってくる
				JSONObject result = NumberingUtility.execReleaseNewSHNCD(userInfo, dataOther.optString(MSTSHNLayout.SHNCD.getCol()+"_RENEW"));
			}
			if(StringUtils.isNotEmpty(dataOther.optString(MSTSHNLayout.URICD.getCol()+"_NEW"))){
				// 販売コード情報、もしくはエラー情報が返ってくる
				JSONObject result = NumberingUtility.execReleaseNewURICD(userInfo, dataOther.optString(MSTSHNLayout.URICD.getCol()+"_NEW"));
			}
		}
		return option;
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
		String sysdate = (String)request.getSession().getAttribute(Consts.STR_SES_LOGINDT);

		// 更新情報チェック(基本JS側で制御)
		JSONObject option = new JSONObject();

		String menuKbn = "-1";

		// 本部マスタ
		if ((!StringUtils.isEmpty(userInfo.getYobi7_()) && !userInfo.getYobi7_().equals("-1")) || StringUtils.isEmpty(userInfo.getYobi7_())) {
			menuKbn = "4";
		}

		// 本部マスタ画面の操作でかつ削除権限がない
		if (menuKbn.equals("4") && StringUtils.isEmpty(userInfo.getYobi7_())) {
			String sendBtnid = map.get("SENDBTNID");	// 呼出しボタン
		}

		JSONArray msgList = this.checkDel(map, userInfo, sysdate);
		if(msgList.size() > 0){
			option.put(MsgKey.E.getKey(), msgList.optJSONObject(0));
			return option;
		}

		// 削除処理
		try {
			option = this.deleteData(map, userInfo, sysdate);
		} catch (Exception e) {
			e.printStackTrace();
			option.put(MsgKey.E.getKey(), MessageUtility.getDbMessageIdObj("E30005", new String[]{}));
		}
		return option;
	}

	private String createCommand() {
		// ログインユーザー情報取得
		User userInfo = getUserInfo();
		if(userInfo==null){
			return "";
		}

		String szSelShncd					= getMap().get("SEL_SHNCD");		// 検索商品コード
		String szShncd						= getMap().get("SHNCD");				// 入力商品コード
		szSelShncd							= szShncd;
		String szSeq							= getMap().get("SEQ");					// CSVエラー.SEQ
		String szInputno					= getMap().get("INPUTNO");			// CSVエラー.入力番号
		String szCsvUpdkbn				= getMap().get("CSV_UPDKBN");	// CSVエラー.CSV登録区分
		String szYoyakudt				= getMap().get("YOYAKUDT");		// CSVエラー用.マスタ変更予定日
		String szTenbaikadt				= getMap().get("TENBAIKADT");	// CSVエラー用.店売価実施日
		String sendBtnid					= getMap().get("SENDBTNID");		// 呼出しボタン
		String teianNo						= getMap().get("TEIAN");				// 件名No
		String szStcdShikakari	= getMap().get("STATE_S");			// 状態_仕掛商品
		String szStcdTeian			= getMap().get("STATE_T");			// 状態_提案商品
		String szMode						= getMap().get("MODE");				// モード（提案or仕掛）

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
		boolean isRefer = DefineReport.Button.SEL_REFER.getObj().equals(sendBtnid);

		// 必須チェック
		if ( (isCopyNew && StringUtils.isEmpty(szSelShncd)) || (isChange && StringUtils.isEmpty(szShncd)) ) {
			System.out.println(super.getConditionLog());
			return "";
		}

		String szTableShn = "INAWS.PIMSIT";
		String szTableShina = "INAWS.PIMSISHINAGP";
		String szTableBaika = "INAWS.PIMSIBAIKACTL";
		String szTableSir = "INAWS.PIMSISIRGPSHN";
		String szTableTbmn = "INAMS.MSTSHNTENBMN";
		String szWhereSTable = " and SHNCD like '" + szSelShncd.replace("-", "") + "%'";
		String szWhereYTable = "";
		String szWhereCTable = "";

		if(szMode.equals("Teian")) {
			szTableShn = "INAWS.PIMTIT";
			szTableShina = "INAWS.PIMTISHINAGP";
			szTableBaika = "INAWS.PIMTIBAIKACTL";
			szTableSir = "INAWS.PIMTISIRGPSHN";
			szSelShncd = szSelShncd.replace("-", "");

			szWhereSTable = " and SHNCD like '" + szSelShncd + "%'";
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
			if(isCopyNew){
				sbSQL.append("   null as SHNCD");		// F1
			}else if(szMode.equals("Teian")){
				sbSQL.append("   lpad(SHNCD, 8, '0') as SHNCD");
			}else{
				sbSQL.append("   SHNCD as SHNCD");		// F1
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
				sbSQL.append(" , T1.SENDFLG");

				sbSQL.append(" , T1.OPERATOR");
				sbSQL.append(" , nvl(TO_CHAR(T1.ADDDT, 'YY/MM/DD'),'__/__/__') as ADDDT");
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
			sbSQL.append(" , '' as KETA");			// F125:桁
			sbSQL.append(" , 0 as YOYAKU");	// F126:予約件数	初期値:0
			if(isCopyNew){
				sbSQL.append(" , null as HDN_UPDDT");											// F127:更新日時
			}else{
				sbSQL.append(" , TO_CHAR(T1.UPDDT, 'YYYYMMDDHH24MISSNNNNNN') as HDN_UPDDT");			// F127:更新日時
			}
			sbSQL.append(" , nvl((select max(AREAKBN) from "+szTableShina+" T1 "+ szWhereTable + "),"+DefineReport.ValKbn135.VAL0.getVal()+") as AREAKBN_SHINA");	// F128
			sbSQL.append(" , nvl((select max(AREAKBN) from "+szTableBaika+" T1 "+ szWhereTable + "),"+DefineReport.ValKbn135.VAL1.getVal()+") as AREAKBN_BAIKA");	// F129
			sbSQL.append(" , nvl((select max(AREAKBN) from "+szTableSir+" T1 "+ szWhereTable + "),"+DefineReport.ValKbn135.VAL0.getVal()+") as AREAKBN_SIR");		// F130
			sbSQL.append(" , nvl((select max(AREAKBN) from "+szTableTbmn+" T1 "+ szWhereTable + "),"+DefineReport.ValKbn135.VAL0.getVal()+") as AREAKBN_TBMN");		// F131

			if(szMode.equals("Teian")) {
				sbSQL.append(" , TITKNNO as TEIANNO");		// F132
				sbSQL.append(" , TITSTCD as STCD_SHIKAKARI");		// F133
			}else {
				sbSQL.append(" , SITKNNO as TEIANNO");		// F132
				sbSQL.append(" , SITSTCD as STCD_SHIKAKARI");		// F133
			}
			sbSQL.append(" from "+szTableShn+" T1 ");
			sbSQL.append(" " + szWhereTable + " and nvl(UPDKBN, 0) <> 1 ");
			sbSQL.append( super.getFechSql("1"));
		}

		// オプション情報設定
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

	/** 商品マスタ列数 */
	int mstshn_col_num = MSTSHNLayout.values().length;
	String[] csvshn_add_data = new String[CSVSHNLayout.values().length];

	/**
	 * 更新処理実行
	 * @return
	 *
	 * @throws Exception
	 */
	private JSONObject updateData(HashMap<String, String> map, User userInfo, String sysdate, JSONObject objset) throws Exception {
		// パラメータ確認
		String szSelShncd					= map.get("SEL_SHNCD");	// 検索商品コード
		String szShncd						= map.get("SHNCD");			// 入力商品コード
		szSelShncd 							= szShncd;
		String szSeq							= map.get("SEQ");				// CSVエラー.SEQ
		String szInputno					= map.get("INPUTNO");		// CSVエラー.入力番号
		String szCsvUpdkbn				= map.get("CSV_UPDKBN");	// CSVエラー.CSV登録区分
		String szYoyakudt				= map.get("YOYAKUDT");		// CSVエラー用.マスタ変更予定日
		String szTenbaikadt				= map.get("TENBAIKADT");	// CSVエラー用.店売価実施日
		String sendBtnid					= map.get("SENDBTNID");	// 呼出しボタン
		String teianNo						= map.get("TEIAN");				// 件名No
		String szStcdShikakari			= map.get("STATE_S");			// 状態_仕掛商品
		String szStcdTeian				= map.get("STATE_T");			// 状態_提案商品
		String szShnkn						= map.get("SHNKN");			// 商品名（漢字）
		String szBmncd					= map.get("BMNCD");			// 部門
		String szDaicd						= map.get("DAICD");				// 大分類
		String szChucd						= map.get("CHUCD");			// 中分類

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

		// パラメータ確認
		// 必須チェック
		if ( sendBtnid == null || dataArray.isEmpty() || dataArray.size() != 1 || dataArray.getJSONObject(0).isEmpty() || teianNo == null || teianNo.equals("") ) {
			option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
			return option;
		}

		// ログインユーザー情報取得
		String userId	= userInfo.getId();			// ログインユーザー

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

		// 基本登録情報
		JSONObject data = dataArray.getJSONObject(0);

		// 既存商品の更新の場合
		if (!StringUtils.isEmpty(teianNo) && teianNo.equals("0")) {
			msgShnCd = szShncd;
			msgUriCd = data.optString(MSTSHNLayout.URICD.getId());
		}

		// ①正 .新規
		if(isNew){
			jnlshn_trankbn = InfTrankbn.INS.getVal();
			// 販売コード付番管理 ※検索チェック時に実行
			// String txt_uricd = this.getNewURICD(data.optString(MSTSHNLayout.SHNCD.getId())).optString("VALUE");
			// data.element(MSTSHNLayout.URICD.getId(), txt_uricd);

			// --- 01.商品
			String state_making = "1";		// 作成中
			JSONObject result1 = this.createSqlPIMSIT(userId, sendBtnid, data, TblType.SEI, SqlType.MRG, teianNo, state_making);

			// --- 08.商品コード空き番 ※付番時にDB登録する
			// JSONObject result8 = this.createSqlSYSSHNCD_AKI(userId, sendBtnid, data, TblType.SEI, SqlType.MRG);

			// --- 09.販売コード付番管理
			// JSONObject result9 = this.createSqlSYSURICD_FU(userId, sendBtnid, data, TblType.SEI, SqlType.UPD);

			// --- 10.販売コード空き番※付番時にDB登録する
			// JSONObject result10 = this.createSqlSYSURICD_AKI(userId, sendBtnid, data, TblType.SEI, SqlType.UPD);


		// ②正 .変更
		}else if(isChange){
			jnlshn_trankbn = InfTrankbn.INS.getVal();

			// --- 01.仕掛商品
			JSONObject result1 = this.createSqlPIMSIT(userId, sendBtnid, data, TblType.SEI, SqlType.UPD, teianNo, szStcdShikakari);

		}

		TblType baseTblType = TblType.SEI;

		JSONArray dataArrayDel = new JSONArray();
		JSONArray dataArrayDelTENKABUTSU = new JSONArray();
		dataArrayDel.add(this.createJSONObject(new String[]{"F1"}, new String[]{szShncd}));
		dataArrayDelTENKABUTSU.add(this.createJSONObject(new String[]{"F1"}, new String[]{szShncd}));

		// --- 02.仕入グループ
		JSONObject result2D= this.createSqlMSTSIRGPSHN251(userId, sendBtnid, dataArrayDel, baseTblType, SqlType.DEL);
		if(dataArrayTENGP1.size() > 0){
			JSONObject result2 = this.createSqlMSTSIRGPSHN251(userId, sendBtnid, dataArrayTENGP1, baseTblType, SqlType.INS);
		}

		// --- 03.売価コントロール
		JSONObject result3D= this.createSqlMSTBAIKACTL251(userId, sendBtnid, dataArrayDel, baseTblType, SqlType.DEL);
		if(dataArrayTENGP2.size() > 0){
			JSONObject result3 = this.createSqlMSTBAIKACTL251(userId, sendBtnid, dataArrayTENGP2, baseTblType, SqlType.INS);
		}
		// --- 04.ソースコード管理
		JSONObject result4D= this.createSqlMSTSRCCD251(userId, sendBtnid, dataArrayDel, baseTblType, SqlType.DEL);
		if(dataArraySRCCD.size() > 0){
			JSONObject result4 = this.createSqlMSTSRCCD251(userId, sendBtnid, dataArraySRCCD, baseTblType, SqlType.INS);
		}
		// --- 05.添加物
		JSONObject result5D= this.createSqlMSTTENKABUTSU251(userId, sendBtnid, dataArrayDelTENKABUTSU, baseTblType, SqlType.DEL);
		if(dataArrayTENKABUTSU.size() > 0){
			JSONObject result5 = this.createSqlMSTTENKABUTSU251(userId, sendBtnid, dataArrayTENKABUTSU, baseTblType, SqlType.INS);
		}
		// --- 06.品揃グループ
		JSONObject result6D= this.createSqlMSTSHINAGP251(userId, sendBtnid, dataArrayDel, baseTblType, SqlType.DEL);
		if(dataArrayTENGP3.size() > 0){
			JSONObject result6 = this.createSqlMSTSHINAGP251(userId, sendBtnid, dataArrayTENGP3, baseTblType, SqlType.INS);
		}

		// --- 07.グループ分類名
		JSONObject result7D= this.createSqlMSTGRP251(userId, sendBtnid, dataArrayDel, baseTblType, SqlType.DEL);
		if(dataArrayGROUP.size() > 0){
			// グループ名登録処理+登録情報更新
			this.updateMSTGROUP(userId, sendBtnid, dataArrayGROUP);
			JSONObject result7 = this.createSqlMSTGRP251(userId, sendBtnid, dataArrayGROUP, baseTblType, SqlType.INS);
		}

		// --- 08.自動発注
		JSONObject result8D= this.createSqlMSTAHS251(userId, sendBtnid, dataArrayDel, baseTblType, SqlType.DEL);
		if(dataArrayAHS.size() > 0){
			JSONObject result6 = this.createSqlMSTAHS251(userId, sendBtnid, dataArrayAHS, baseTblType, SqlType.INS);
		}

		// --- 09.店別異部門
		JSONObject result9D= this.createSqlMSTSHNTENBMN251(userId, sendBtnid, dataArrayDel, baseTblType, SqlType.DEL);
		if(dataArrayTENGP4.size() > 0){
			JSONObject result9 = this.createSqlMSTSHNTENBMN251(userId, sendBtnid, dataArrayTENGP4, baseTblType, SqlType.INS);
		}


		// ************ 関連テーブル処理 ***********
		// --- 07.メーカー
		String makercd = data.optString(MSTSHNLayout.MAKERCD.getId());
		if(StringUtils.isNotEmpty(makercd) && dataArraySRCCD.size() > 0){
			// メーカーコード存在チェック
			if(!this.checkMstExist251(DefineReport.InpText.MAKERCD.getObj(), makercd)){
				String jancd = dataArraySRCCD.optJSONObject(0).optString(MSTSRCCDLayout.SRCCD.getId());
				JSONObject dataMAKER = this.createJSONObject(new String[]{"F1", "F4"}, new String[]{makercd, jancd});
				JSONObject result7 = this.createSqlMSTMAKER(userId, sendBtnid, dataMAKER, TblType.SEI, SqlType.INS);
			}
		}

		// 承認ステータスで更新
		if(szStcdShikakari.equals("04")){
			// 商品コードの更新
			// 仕掛商品マスタ
			JSONObject result1_1 = this.updatePIMSIT_SHNCD(userId, sendBtnid, data, TblType.SEI, SqlType.UPD, userInfo, teianNo, szShncd, msgShnCd);

			// 仕掛仕入グループ商品マスタ
			JSONObject result1_2 = this.updatePIMSISIRGPSHN_SHNCD(userId, sendBtnid, data, TblType.SEI, SqlType.UPD, userInfo, teianNo, szShncd, msgShnCd);

			// 仕掛売価コントロールマスタ
			JSONObject result1_3 = this.updatePIMSIBAIKACTL_SHNCD(userId, sendBtnid, data, TblType.SEI, SqlType.UPD, userInfo, teianNo, szShncd, msgShnCd);

			// 仕掛ソースコード管理マスタ
			JSONObject result1_4 = this.updatePIMSISRCCD_SHNCD(userId, sendBtnid, data, TblType.SEI, SqlType.UPD, userInfo, teianNo, szShncd, msgShnCd);

			// 仕掛添加物マスタ
			JSONObject result1_5 = this.updatePIMSITENKABUTSU_SHNCD(userId, sendBtnid, data, TblType.SEI, SqlType.UPD, userInfo, teianNo, szShncd, msgShnCd);

			// 仕掛品揃グループマスタ
			JSONObject result1_6 = this.updatePIMSISHINAGP_SHNCD(userId, sendBtnid, data, TblType.SEI, SqlType.UPD, userInfo, teianNo, szShncd, msgShnCd);

			dataArrayDel = new JSONArray();
			dataArrayDelTENKABUTSU = new JSONArray();
			dataArrayDel.add(this.createJSONObject(new String[]{"F1"}, new String[]{msgShnCd}));
			dataArrayDelTENKABUTSU.add(this.createJSONObject(new String[]{"F1"}, new String[]{msgShnCd}));

			// 本マスタ合流
			// --- 01.商品
			JSONObject result1 = createSqlMSTSHN(userId, sendBtnid, data, TblType.SEI, SqlType.MRG);

			// --- 02.仕入グループ
			JSONObject result2D_2= createSqlMSTSIRGPSHN(userId, sendBtnid, dataArrayDel, baseTblType, SqlType.DEL);
			if(dataArrayTENGP1.size() > 0){
				JSONObject result2 = createSqlMSTSIRGPSHN(userId, sendBtnid, dataArrayTENGP1, baseTblType, SqlType.INS);
			}

			// --- 03.売価コントロール
			JSONObject result3D_2= createSqlMSTBAIKACTL(userId, sendBtnid, dataArrayDel, baseTblType, SqlType.DEL);
			if(dataArrayTENGP2.size() > 0){
				JSONObject result3 = createSqlMSTBAIKACTL(userId, sendBtnid, dataArrayTENGP2, baseTblType, SqlType.INS);
			}
			// --- 04.ソースコード管理
			JSONObject result4D_2= createSqlMSTSRCCD(userId, sendBtnid, dataArrayDel, baseTblType, SqlType.DEL);
			if(dataArraySRCCD.size() > 0){
				JSONObject result4 = createSqlMSTSRCCD(userId, sendBtnid, dataArraySRCCD, baseTblType, SqlType.INS);
			}

			// --- 05.添加物
			JSONObject result5D_2= createSqlMSTTENKABUTSU(userId, sendBtnid, dataArrayDelTENKABUTSU, baseTblType, SqlType.DEL);
			if(dataArrayTENKABUTSU.size() > 0){
				JSONObject result5 = createSqlMSTTENKABUTSU(userId, sendBtnid, dataArrayTENKABUTSU, baseTblType, SqlType.INS);
			}

			// --- 06.品揃グループ
			JSONObject result6D_2= createSqlMSTSHINAGP(userId, sendBtnid, dataArrayDel, baseTblType, SqlType.DEL);
			if(dataArrayTENGP3.size() > 0){
				JSONObject result6 = createSqlMSTSHINAGP(userId, sendBtnid, dataArrayTENGP3, baseTblType, SqlType.INS);
			}

			// --- 07.グループ分類名
			JSONObject result7D_2= createSqlMSTGRP(userId, sendBtnid, dataArrayDel, baseTblType, SqlType.DEL);
			if(dataArrayGROUP.size() > 0){
				// グループ名登録処理+登録情報更新
				this.updateMSTGROUP(userId, sendBtnid, dataArrayGROUP);

				JSONObject result7 = createSqlMSTGRP(userId, sendBtnid, dataArrayGROUP, baseTblType, SqlType.INS);
			}

			// --- 08.自動発注
			JSONObject result8D_2= createSqlMSTAHS(userId, sendBtnid, dataArrayDel, baseTblType, SqlType.DEL);
			if(dataArrayAHS.size() > 0){
				JSONObject result6 = createSqlMSTAHS(userId, sendBtnid, dataArrayAHS, baseTblType, SqlType.INS);
			}

			// --- 09.店別異部門
			JSONObject result9D_2= createSqlMSTSHNTENBMN(userId, sendBtnid, dataArrayDel, baseTblType, SqlType.DEL);
			if(dataArrayTENGP4.size() > 0){
				JSONObject result9 = createSqlMSTSHNTENBMN(userId, sendBtnid, dataArrayTENGP4, baseTblType, SqlType.INS);
			}

			// --- 09.販売コード付番管理
			JSONObject result9 = this.createSqlSYSURICD_FU(userId, sendBtnid, data, TblType.SEI, SqlType.UPD);

			// --- 14.商品更新件数
			JSONObject result14 = this.createSqlSYSSHNCOUNT(userId, sendBtnid, TblType.SEI, SqlType.MRG);

			// 提案商品のステータスを更新
			JSONObject result3_1 = this.updateTeianSTCD(userId, sendBtnid, data, TblType.SEI, SqlType.UPD, userInfo, teianNo, szShncd, msgShnCd, szShnkn, szBmncd, szDaicd, szChucd);
			JSONObject result3_2 = this.updateTeianKenmeiSTCD(userId, sendBtnid, data, TblType.SEI, SqlType.UPD, userInfo, teianNo, szShncd, msgShnCd, szShnkn, szBmncd, szDaicd, szChucd);

			// ******** ジャーナル処理（正・予共通） ********
			// ジャーナル用の情報を取得
			jnlshn_seq = this.getJNLSHN_SEQ();
			jnlshn_tablekbn = DefineReport.ValTablekbn.SEI.getVal();

			// --- 15.ジャーナル_商品
			JSONObject result15 = createSqlPIMSIT(userId, sendBtnid, data, TblType.JNL, SqlType.INS, "0", "0");

			// --- 16.ジャーナル_仕入グループ商品
			if(dataArrayTENGP1.size() > 0){
				JSONObject result16 = createSqlMSTSIRGPSHN(userId, sendBtnid, dataArrayTENGP1, TblType.JNL, SqlType.INS);
			}

			// --- 17.ジャーナル_売価コントロール
			if(dataArrayTENGP2.size() > 0){
				JSONObject result17 = createSqlMSTBAIKACTL(userId, sendBtnid, dataArrayTENGP2, TblType.JNL, SqlType.INS);
			}

			// --- 18.ジャーナル_ソースコード管理
			if(dataArraySRCCD.size() > 0){
				JSONObject result18 = createSqlMSTSRCCD(userId, sendBtnid, dataArraySRCCD, TblType.JNL, SqlType.INS);
			}

			// --- 19.ジャーナル_添加物
			if(dataArrayTENKABUTSU.size() > 0){
				JSONObject result19 = createSqlMSTTENKABUTSU(userId, sendBtnid, dataArrayTENKABUTSU, TblType.JNL, SqlType.INS);
			}

			// --- 20.ジャーナル_品揃グループ
			if(dataArrayTENGP3.size() > 0){
				JSONObject result20 = createSqlMSTSHINAGP(userId, sendBtnid, dataArrayTENGP3, TblType.JNL, SqlType.INS);
			}

			// --- 21.ジャーナル_グループ分類管理マスタ
			if(dataArrayGROUP.size() > 0){
				JSONObject result21 = createSqlMSTGRP(userId, sendBtnid, dataArrayGROUP, TblType.JNL, SqlType.INS);
			}

			// --- 22.ジャーナル_自動発注
			if(dataArrayAHS.size() > 0){
				JSONObject result22 = createSqlMSTAHS(userId, sendBtnid, dataArrayAHS, TblType.JNL, SqlType.INS);
			}

			// --- 23.ジャーナル_店別異部門
			if(dataArrayTENGP4.size() > 0){
				JSONObject result23 = createSqlMSTSHNTENBMN(userId, sendBtnid, dataArrayTENGP4, TblType.JNL, SqlType.INS);
			}
		}

		if(szStcdShikakari.equals("09")){
			JSONObject result3_1 = this.updateTeianSTCD2(userId, sendBtnid, data, TblType.SEI, SqlType.UPD, userInfo, teianNo, szShncd, szShnkn, szBmncd, szDaicd, szChucd);
			JSONObject result3_2 = this.updateTeianKenmeiSTCD(userId, sendBtnid, data, TblType.SEI, SqlType.UPD, userInfo, teianNo, szShncd, msgShnCd, szShnkn, szBmncd, szDaicd, szChucd);
		}

		// 排他チェック実行
		String targetTable = null;
		String targetWhere = "nvl(UPDKBN, 0) <> 1";
		ArrayList<String> targetParam = new ArrayList<String>();

		if(isNew || isChange){
			targetTable = "INAWS.PIMSIT";
			targetWhere += " and SHNCD = ?";
			targetParam.add(szShncd);
		}

 		if(!super.checkExclusion(targetTable, targetWhere, targetParam, data.optString("F127"))){
 			msg.add(MessageUtility.getDbMessageExclusion(FieldType.DEFAULT, new String[]{}));
 			option.put(MsgKey.E.getKey(), msg);
			return option;
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

	public JSONObject createJSONObject(String[] keys, String[] values) {
		JSONObject obj = new JSONObject();
		for (int i = 0; i < keys.length; i++) {
			obj.put(keys[i], values[i]);
		}
		return obj;
	}

	/**
	 * 削除処理実行
	 * @param sysdate
	 * @return
	 *
	 * @throws Exception
	 */
	private JSONObject deleteData(HashMap<String, String> map, User userInfo, String sysdate) throws Exception {
		// パラメータ確認
		String szSelShncd	= map.get("SEL_SHNCD");	// 検索商品コード
		String szShncd		= map.get("SHNCD");		// 入力商品コード
		szSelShncd			= szShncd;
		String szSeq		= map.get("SEQ");			// CSVエラー.SEQ
		String szInputno	= map.get("INPUTNO");		// CSVエラー.入力番号
		String szCsvUpdkbn	= map.get("CSV_UPDKBN");	// CSVエラー.CSV登録区分
		String szYoyakudt	= map.get("YOYAKUDT");		// CSVエラー用.マスタ変更予定日
		String szTenbaikadt	= map.get("TENBAIKADT");	// CSVエラー用.店売価実施日
		String sendBtnid	= map.get("SENDBTNID");	// 呼出しボタン

		JSONArray dataArray = JSONArray.fromObject(map.get("DATA"));	// 対象情報

		JSONArray dataArraySRCCD = JSONArray.fromObject(map.get("DATA_SRCCD"));		// ソースコード
		JSONArray dataArrayTENGP4 = JSONArray.fromObject(map.get("DATA_TENGP4"));	// 店別異部門
		JSONArray dataArrayTENGP3 = JSONArray.fromObject(map.get("DATA_TENGP3"));	// 品揃えグループ
		JSONArray dataArrayTENGP2 = JSONArray.fromObject(map.get("DATA_TENGP2"));	// 売価コントロール
		JSONArray dataArrayTENGP1 = JSONArray.fromObject(map.get("DATA_TENGP1"));	// 仕入グループ
		JSONArray dataArrayTENKABUTSU = JSONArray.fromObject(map.get("DATA_TENKABUTSU"));	// 添加物
		JSONArray dataArrayGROUP = JSONArray.fromObject(map.get("DATA_GROUP"));		// グループ分類
		JSONArray dataArrayAHS = JSONArray.fromObject(map.get("DATA_AHS"));			// 自動発注データ


		JSONObject option = new JSONObject();
		JSONArray msg = new JSONArray();

		// パラメータ確認
		// 必須チェック
		if ( dataArray.isEmpty() || dataArray.size() != 1 || dataArray.getJSONObject(0).isEmpty()) {
			option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
			return option;
		}

		// ログインユーザー情報取得
		String userId	= userInfo.getId();								// ログインユーザー
		boolean isChange = DefineReport.Button.SEL_CHANGE.getObj().equals(sendBtnid)|| DefineReport.Button.SEARCH.getObj().equals(sendBtnid)|| DefineReport.Button.SEI.getObj().equals(sendBtnid);

		// 基本登録情報
		JSONObject data = dataArray.getJSONObject(0);
		// 変更処理（正）
		if(isChange){
			// --- 01.仕掛商品
			JSONObject result1 = this.createSqlMSTSHN_DEL(userId, sendBtnid, data, TblType.SEI, SqlType.DEL);

			// --- 02.仕掛仕入グループ商品マスタ
			JSONObject result2 = this.createSqlPIMSISIRGPSHN_DEL(userId, sendBtnid, data, TblType.SEI, SqlType.DEL, szShncd);

			// --- 03.仕掛売価コントロールマスタ
			JSONObject result3 = this.createSqlPIMSIBAIKACTL_DEL(userId, sendBtnid, data, TblType.SEI, SqlType.DEL, szShncd);

			// --- 04.仕掛ソースコード管理マスタ
			JSONObject result4 = this.createSqlPIMSISRCCD_DEL(userId, sendBtnid, data, TblType.SEI, SqlType.DEL, szShncd);

			// --- 05.仕掛添加物マスタ
			JSONObject result5 = this.createSqlPIMSITENKABUTSU_DEL(userId, sendBtnid, data, TblType.SEI, SqlType.DEL, szShncd);

			// --- 06.仕掛品揃グループマスタ
			JSONObject result6 = this.createSqlPIMSISHINAGP_DEL(userId, sendBtnid, data, TblType.SEI, SqlType.DEL, szShncd);

			// --- 07.仕掛メーカーマスタ
			JSONObject result7 = this.createSqlPIMSIMAKER_DEL(userId, sendBtnid, data, TblType.SEI, SqlType.DEL, szShncd);
		}

		if(isChange){
		}

		// 排他チェック実行
		String targetTable = null;
		String targetWhere = "nvl(UPDKBN, 0) <> 1";
		ArrayList<String> targetParam = new ArrayList<String>();
		// ②正 .変更
		if(isChange){
			targetTable = "INAWS.PIMSIT";
			targetWhere += " and SHNCD = ?";
			targetParam.add(data.optString(MSTSHNLayout.SHNCD.getId()));
		}
 		if(!super.checkExclusion(targetTable, targetWhere, targetParam, data.optString("F127"))){
 			msg.add(MessageUtility.getDbMessageExclusion(FieldType.DEFAULT, new String[]{}));
 			option.put(MsgKey.E.getKey(), msg);
			return option;
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
				option.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00002.getVal()));
			}
		}else{
			option.put(MsgKey.E.getKey(), getMessage());
		}
		return option;
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

		MessageUtility mu = new MessageUtility();

		List<JSONObject> msgList = new ArrayList<JSONObject>();
		msgList = this.checkDataDel(
				isNew, isChange, false,
				map, userInfo, sysdate,mu,
				dataArray, dataArraySRCCD, dataArrayTENGP3, dataArrayTENGP2, dataArrayTENGP1, dataArrayTENKABUTSU);

		JSONArray msgArray = new JSONArray();
		// MessageBoxを出す関係上、1件のみ表示
		if(msgList.size() > 0){
			msgArray.add(msgList.get(0));
		}
		return msgArray;
	}

	public List<JSONObject> checkDataDel(
			boolean isNew, boolean isChange, boolean isCsvUpload,
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
		JSONObject seiJsonObject = new JSONObject();
		if(isChange){
			JSONArray array = getSeiJSONArray(map);
			seiJsonObject = array.optJSONObject(0);
		}

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
			sbSQL.append(" from INAWS.PIMSIT where SHNCD like ? and nvl(UPDKBN, 0) <> 1");

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
	 * 新規商品コード取得処理<br>
	 * 前提：部門必須、桁数と入力商品コードの矛盾チェック済
	 *
	 * @throws Exception
	 */
	public JSONObject getNewSHNCD(String inpshncd, String ketakbn, String bmncd) {
		// 関連情報取得
		ItemList iL = new ItemList();
		// 配列準備
		ArrayList<String> paramData = new ArrayList<String>();
		String sqlcommand = "";

		JSONObject returndata = new JSONObject();

		// 入力無しの場合部門をキーとして
		if(DefineReport.ValKbn143.VAL0.getVal().equals(ketakbn)){
			paramData.add(StringUtils.right("00"+bmncd, 2)+'%');
		}else{
			paramData.add(inpshncd+'%');
		}

		// 手入力の場合、付番管理テーブルは条件に含めず、空き番のみで商品コード取得（使い回し考慮） or 付番済みJAVA再チェックもこちら
		if(DefineReport.ValKbn143.VAL1.getVal().equals(ketakbn)){
			sqlcommand = DefineReport.ID_SQL_MD03100901.replace("@W", "");
			JSONObject data = new JSONObject();
			if(sqlcommand.length() > 0){
				@SuppressWarnings("static-access")
				JSONArray array = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
				if(array.size() > 0){
					data = array.optJSONObject(0);
					if(StringUtils.isNotEmpty(data.optString("VALUE"))){
						returndata =  data;
					}
				}
			}

		}else{
			String searchCommand = DefineReport.ID_SQL_MD03100901.replace("@W", StringUtils.replace(DefineReport.ID_SQL_MD03100901_WHERE_AUTO, "@C", "T1.SHNCD"));
			sqlcommand = "select SHNCD as VALUE from FINAL table ( update INAAD.SYSSHNCD_AKI set USEFLG = '1', UPDDT = current timestamp where USEFLG = 0 and SHNCD = ("+ searchCommand +"))";

			boolean dowhile = true;
			JSONObject data = new JSONObject();
			while(dowhile){
				// 取得したい空き番の取得と登録を同時に行う
				if(sqlcommand.length() > 0){
					data = new JSONObject();
					@SuppressWarnings("static-access")
					JSONArray array = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
					if(array.size() > 0){
						data = array.optJSONObject(0);
						if(StringUtils.isNotEmpty(data.optString("VALUE"))){
							dowhile = false;
						}
					}
				}

				// 空き番を取得出来なかった場合
				if(dowhile){
					@SuppressWarnings("static-access")
					JSONArray array = iL.selectJSONArray(searchCommand, paramData, Defines.STR_JNDI_DS);
					if(array.size() == 0 || StringUtils.isEmpty(array.optJSONObject(0).optString("VALUE"))){
						// 取得できる空き番が存在しない場合は取得処理を終了する。
						dowhile = false;
					}
				}
			}
			returndata =  data;
		}
		return returndata;
	}


	/**
	 * 新規販売コード取得処理
	 *
	 * @throws Exception
	 */
	public JSONObject getNewURICD(String shncd) {
		// 関連情報取得
		ItemList iL = new ItemList();
		// 配列準備
		ArrayList<String> paramData = new ArrayList<String>();
		String sqlcommand = "";

		// 衣料の場合
		if(StringUtils.startsWithAny(shncd, new String[]{"13", "27"})){
			paramData.add(shncd);
			paramData.add(shncd);
//			String chksqlcommand = StringUtils.replace(DefineReport.ID_SQL_MD03100901_EXISTS_AUTO, "@C", "?");	// 20210729 No.158 変更
			String chksqlcommand = StringUtils.replace("select 'X' from INAAD.SYSSHNCD_FU T2 where int(left(@C, 2)) = T2.BMNCD and NOT int(substr(@C, 3, 5)) between T2.STARTNO and T2.ENDNO", "@C", "?");

			@SuppressWarnings("static-access")
			JSONArray array = iL.selectJSONArray(chksqlcommand, paramData, Defines.STR_JNDI_DS);

			paramData = new ArrayList<String>();
			// 衣料使い回しの範囲の場合
			if(array.size() > 0){
				paramData.add(shncd);
				sqlcommand = DefineReport.ID_SQL_MD03100902_USE;
				sqlcommand = "select URICD as value from FINAL table (update INAAD.SYSURICD_AKI set USEFLG = 1, UPDDT = current timestamp where URICD = (select value from ("+sqlcommand+")))";
				@SuppressWarnings("static-access")
				JSONArray array2 = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
				if(array2.size() > 0){
					JSONObject data = array2.optJSONObject(0);
					if(StringUtils.isNotEmpty(data.optString("VALUE"))){
						if (DefineReport.ID_DEBUG_MODE) System.out.println(MessageUtility.getMessage(Msg.S00006.getVal(), new String[]{"取得登録：販売コード空き番管理テーブル", Integer.toString(array.size())}));
						return data;
					}
				}
			}
		}

		// 販売コード付番管理テーブルからを取得
		int STARTNO	 = 0;	// 開始番号
		int ENDNO	 = 0;	// 終了番号
		int SUMINO	 = 0;	// 付番済番号
		boolean useAki = false;

		paramData = new ArrayList<String>();
		sqlcommand = "select STARTNO, ENDNO, SUMINO from INAAD.SYSURICD_FU fetch first 1 rows only";
		if(sqlcommand.length() > 0){
			@SuppressWarnings("static-access")
			JSONArray array = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
			if(array.size() > 0){
				JSONObject data = array.optJSONObject(0);

				STARTNO	 = data.optInt("STARTNO");
				ENDNO	 = data.optInt("ENDNO");
				SUMINO	 = data.optInt("SUMINO");
			}
		}

		if(ENDNO > SUMINO){
			// 終了番号 > 付番済番号
			// 販売コード付番管理テーブルからデータを取得し、登録を行う。
			paramData = new ArrayList<String>();
			sqlcommand = "select SUMINO as value from FINAL table (update INAAD.SYSURICD_FU set SUMINO = SUMINO + 1 where ENDNO > SUMINO)";
			@SuppressWarnings("static-access")
			JSONArray array = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
			if(array.size() > 0){
				JSONObject data = array.optJSONObject(0);
				if(StringUtils.isNotEmpty(data.optString("VALUE"))){
					if (DefineReport.ID_DEBUG_MODE) System.out.println(MessageUtility.getMessage(Msg.S00006.getVal(), new String[]{"取得登録：販売コード付番管理テーブル", Integer.toString(array.size())}));
					return data;
				}else{
					// 取得出来なかった場合(例：上限値999999を複数ユーザーで使用しようとした場合は、取得できないユーザーが出現する)
					useAki = true;
				}
			}
		}

		if(ENDNO == SUMINO || useAki){
			// 終了番号 == 付番済番号
			// 販売コード空き番管理テーブルからデータを取得し、登録を行う。
			paramData = new ArrayList<String>();
			paramData.add(""+STARTNO);
			paramData.add(""+ENDNO);
			sqlcommand = "select URICD as value from FINAL table (update INAAD.SYSURICD_AKI set USEFLG = 1, UPDDT = current timestamp where USEFLG = 0 and URICD = (select MIN(URICD) as URICD from INAAD.SYSURICD_AKI where URICD between ? and ? and NVL(USEFLG, 0) <> 1))";

			String searchCommand = DefineReport.ID_SQL_MD03100902;
			ArrayList<String> searchParamData = new ArrayList<String>();

			boolean dowhile = true;
			JSONObject data = new JSONObject();
			while(dowhile){
				// 取得したい空き番の取得と登録を同時に行う
				if(sqlcommand.length() > 0){
					data = new JSONObject();
					@SuppressWarnings("static-access")
					JSONArray array = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
					if(array.size() > 0){
						data = array.optJSONObject(0);
						if(StringUtils.isNotEmpty(data.optString("VALUE"))){
							if (DefineReport.ID_DEBUG_MODE) System.out.println(MessageUtility.getMessage(Msg.S00006.getVal(), new String[]{"取得登録：販売コード空き番管理テーブル", Integer.toString(array.size())}));
							dowhile = false;
						}
					}
				}

				// 空き番を取得出来なかった場合
				if(dowhile){
					@SuppressWarnings("static-access")
					JSONArray array = iL.selectJSONArray(searchCommand, searchParamData, Defines.STR_JNDI_DS);
					if(array.size() == 0 || StringUtils.isEmpty(array.optJSONObject(0).optString("VALUE"))){
						// 取得できる空き番が存在しない場合は取得処理を終了する。
						dowhile = false;
					}
				}
			}
			return data;
		}
		return new JSONObject();
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
	public boolean checkMstExist251(String outobj, String value) {
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
			tbl="INAWS.PIMSIT";
			col="SHNCD";
			whr = DefineReport.ID_SQL_CMN_WHERE2;
		}
		// メーカーコード
		if (outobj.equals(DefineReport.InpText.MAKERCD.getObj())) {
			tbl="INAMS.MSTMAKER";
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
			tbl="INAWS.PIMSIT";
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

	/**
	 * 仕掛商品マスタINSERT/UPDATE SQL作成処理
	 *
	 * @param userId
	 * @param data
	 * @param tbl
	 *
	 * @throws Exception
	 */
	public JSONObject createSqlPIMSIT(String userId, String btnId, JSONObject data, TblType tbl, SqlType sql, String teianNo, String status) throws Exception{
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

		for (int i = 1; i <= colNum; i++) {
			String col = "F" + i;
			String val = StringUtils.trim(data.optString(col));
			if(i==MSTSHNLayout.SHNCD.getNo()){
				if(TblType.JNL.getVal()==tbl.getVal()){
					val = msgShnCd;
				}
			}else if(i==MSTSHNLayout.SHNKN.getNo()){											// 商品名（漢字）:未入力またはスペースの場合、商品名（カナ）項目を全角変換して設定。
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
			}else if(i==MSTSHNLayout.TEIANNO.getNo()) {		// 件名No
				if(TblType.JNL.getVal()==tbl.getVal()){
					continue;
				}else {
					val =teianNo;
				}
			}else if(i==MSTSHNLayout.STCD_SHIKAKARI.getNo()){		// 状態
				if(TblType.JNL.getVal()==tbl.getVal()){
					continue;
				}else {
					val =status;
				}
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
		sbSQL.append(this.createMergeCmnCommandPIMSIT(tbl, sql, values, names));

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
			if(TblType.JNL.getVal()!=tbl.getVal()){
				sbSQL.append(" SITKNNO");		// 提案No
				sbSQL.append(" ,SITSTCD");		// 商品ステータス
				sbSQL.append(" ,SHNCD");			// F1 : 商品コード
			}else {
				sbSQL.append(" SHNCD");			// F1 : 商品コード
			}
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
			if(TblType.JNL.getVal()!=tbl.getVal()){
				sbSQL.append("  RE.SITKNNO");
				sbSQL.append(" ,RE.SITSTCD");
				sbSQL.append(" ,RE.SHNCD");
			}else {
				sbSQL.append("  RE.SHNCD");
			}
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
			sbSQL.append("  YOYAKUDT=RE.YOYAKUDT");			// マスタ変更予定日
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
			sbSQL.append(" ,SITKNNO=RE.SITKNNO");
			sbSQL.append(" ,SITSTCD=RE.SITSTCD");

		}
		if (DefineReport.ID_DEBUG_MODE)	System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("仕掛商品マスタ" + tbl.getTxt());
		return result;
	}

	/**
	 * 仕掛商品マスタDELETE処理
	 *
	 * @param userId
	 * @param data
	 * @param tbl
	 *
	 * @throws Exception
	 */
	public JSONObject createSqlMSTSHN_DEL(String userId, String btnId, JSONObject data, TblType tbl, SqlType sql){
		JSONObject result = new JSONObject();

		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();
		String values = "", names = "";

		StringBuffer sbSQLKey = new StringBuffer();
		List<String> keyColList = new ArrayList<String>();
		List<String> keyValList = new ArrayList<String>();
		if(TblType.SEI.getVal()==tbl.getVal()){
			sbSQLKey.append("  cast("+MSTSHNLayout.SHNCD.getId()+" as "+MSTSHNLayout.SHNCD.getTyp()+") as "+MSTSHNLayout.SHNCD.getCol());			// 商品コード
			keyColList.add(MSTSHNLayout.SHNCD.getId());
			keyValList.add(data.optString(MSTSHNLayout.SHNCD.getId()));
		}
		if(TblType.CSV.getVal()==tbl.getVal()){
			sbSQLKey.append("  cast("+CSVSHNLayout.SEQ.getId2()+" as "+CSVSHNLayout.SEQ.getTyp()+") as "+CSVSHNLayout.SEQ.getCol());				// F1 : SEQ
			sbSQLKey.append(" ,cast("+CSVSHNLayout.INPUTNO.getId2()+" as "+CSVSHNLayout.INPUTNO.getTyp()+") as "+CSVSHNLayout.INPUTNO.getCol());	// F2 : 入力番号
			keyColList.add(CSVSHNLayout.SEQ.getId2());
			keyColList.add(CSVSHNLayout.INPUTNO.getId2());
			keyValList.add(csvshn_add_data[CSVSHNLayout.SEQ.getNo()-1]);
			keyValList.add(csvshn_add_data[CSVSHNLayout.INPUTNO.getNo()-1]);
		}
		// 共通固定値情報
		MSTSHNLayout[] keys = new MSTSHNLayout[]{MSTSHNLayout.TOROKUMOTO, MSTSHNLayout.UPDKBN, MSTSHNLayout.SENDFLG, MSTSHNLayout.OPERATOR, MSTSHNLayout.ADDDT, MSTSHNLayout.UPDDT};
		for(MSTSHNLayout itm : keys){
			sbSQLKey.append(" ,cast("+itm.getId()+" as "+itm.getTyp()+") as "+itm.getCol());
			keyColList.add(itm.getId());
			String val = "";
			if(itm==MSTSHNLayout.TOROKUMOTO){				// 登録元

				if (data.containsKey(MSTSHNLayout.TOROKUMOTO.getId())) {
					val = data.getString(MSTSHNLayout.TOROKUMOTO.getId());
				}

				if (StringUtils.isEmpty(val) || !val.equals("1")) {
					val = "0";
				}
			}else if(itm==MSTSHNLayout.UPDKBN){				// 更新区分
				val = DefineReport.ValUpdkbn.DEL.getVal();
			}else if(itm==MSTSHNLayout.SENDFLG){			// 送信フラグ
				val = DefineReport.Values.SENDFLG_UN.getVal();
			}else if(itm==MSTSHNLayout.OPERATOR){			// オペレータ
				val = userId;
			}
			keyValList.add(val);
		}
		// data情報取得
		for (int i = 0; i < keyColList.size(); i++) {
			String col = keyColList.get(i);
			String val = keyValList.get(i);
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

		// 条件文など取得
		JSONObject parts = this.getSqlPartsCmnCommandMSTSHN251(tbl, sql);
		String szTable = parts.optString("TABLE");
		String szWhere = parts.optString("WHERE");

		StringBuffer sbSQL;
		sbSQL = new StringBuffer();
		sbSQL.append("merge into "+szTable+" as T");
		sbSQL.append(" using (select");
		sbSQL.append(  sbSQLKey.toString()  );
		sbSQL.append(" from (values"+values+") as T1("+names+")");
		sbSQL.append(" ) as RE on ("+szWhere+") ");
		sbSQL.append(" when matched then ");
		sbSQL.append(" update set");
		sbSQL.append("  UPDKBN=RE.UPDKBN");
		sbSQL.append(" ,TOROKUMOTO=RE.TOROKUMOTO");
		if(TblType.CSV.getVal()!=tbl.getVal()){
			sbSQL.append(" ,SENDFLG=RE.SENDFLG");
		}
		sbSQL.append(" ,OPERATOR=RE.OPERATOR");			// オペレータ
		if(TblType.CSV.getVal()!=tbl.getVal() && DefineReport.Button.CSV_IMPORT_YYK.getObj().equals(btnId)){
			sbSQL.append(" ,ADDDT=current timestamp");				// F114: 登録日
		}
		sbSQL.append(" ,UPDDT=current timestamp");				// 更新日

		if (DefineReport.ID_DEBUG_MODE)	System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("仕掛商品マスタ" + tbl.getTxt());
		return result;
	}

	/**
	 * 仕掛仕入グループ商品マスタDELETE処理
	 *
	 * @param userId
	 * @param data
	 * @param tbl
	 *
	 * @throws Exception
	 */
	public JSONObject createSqlPIMSISIRGPSHN_DEL(String userId, String btnId, JSONObject data, TblType tbl, SqlType sql, String szShncd){
		JSONObject result = new JSONObject();

		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();
		String values = "", names = "";

		StringBuffer sbSQL;
		sbSQL = new StringBuffer();
		sbSQL.append("delete from INAWS.PIMSISIRGPSHN where SHNCD = (select SHNCD from INAWS.PIMSIT where SHNCD = '"+szShncd+"' and SITSTCD <= 2)");

		if (DefineReport.ID_DEBUG_MODE)	System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("仕掛仕入グループ商品マスタ" + tbl.getTxt());
		return result;
	}

	/**
	 * 仕掛売価コントロールマスタDELETE処理
	 *
	 * @param userId
	 * @param data
	 * @param tbl
	 *
	 * @throws Exception
	 */
	public JSONObject createSqlPIMSIBAIKACTL_DEL(String userId, String btnId, JSONObject data, TblType tbl, SqlType sql, String szShncd){
		JSONObject result = new JSONObject();

		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();
		String values = "", names = "";

		StringBuffer sbSQL;
		sbSQL = new StringBuffer();
		sbSQL.append("delete from INAWS.PIMSIBAIKACTL where SHNCD = (select SHNCD from INAWS.PIMSIT where SHNCD = '"+szShncd+"' and SITSTCD <= 2)");

		if (DefineReport.ID_DEBUG_MODE)	System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("仕掛売価コントロールマスタ" + tbl.getTxt());
		return result;
	}

	/**
	 * 仕掛ソースコード管理マスタDELETE処理
	 *
	 * @param userId
	 * @param data
	 * @param tbl
	 *
	 * @throws Exception
	 */
	public JSONObject createSqlPIMSISRCCD_DEL(String userId, String btnId, JSONObject data, TblType tbl, SqlType sql, String szShncd){
		JSONObject result = new JSONObject();

		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();
		String values = "", names = "";

		StringBuffer sbSQL;
		sbSQL = new StringBuffer();
		sbSQL.append("delete from INAWS.PIMSISRCCD where SHNCD = (select SHNCD from INAWS.PIMSIT where SHNCD = '"+szShncd+"' and SITSTCD <= 2)");

		if (DefineReport.ID_DEBUG_MODE)	System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("仕掛ソースコード管理マスタ" + tbl.getTxt());
		return result;
	}

	/**
	 * 仕掛添加物マスタDELETE処理
	 *
	 * @param userId
	 * @param data
	 * @param tbl
	 *
	 * @throws Exception
	 */
	public JSONObject createSqlPIMSITENKABUTSU_DEL(String userId, String btnId, JSONObject data, TblType tbl, SqlType sql, String szShncd){
		JSONObject result = new JSONObject();

		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();
		String values = "", names = "";

		StringBuffer sbSQL;
		sbSQL = new StringBuffer();
		sbSQL.append("delete from INAWS.PIMSITENKABUTSU where SHNCD = (select SHNCD from INAWS.PIMSIT where SHNCD = '"+szShncd+"' and SITSTCD <= 2)");

		if (DefineReport.ID_DEBUG_MODE)	System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("仕掛添加物マスタ" + tbl.getTxt());
		return result;
	}

	/**
	 * 仕掛品揃グループマスタDELETE処理
	 *
	 * @param userId
	 * @param data
	 * @param tbl
	 *
	 * @throws Exception
	 */
	public JSONObject createSqlPIMSISHINAGP_DEL(String userId, String btnId, JSONObject data, TblType tbl, SqlType sql, String szShncd){
		JSONObject result = new JSONObject();

		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();
		String values = "", names = "";

		StringBuffer sbSQL;
		sbSQL = new StringBuffer();
		sbSQL.append("delete from INAWS.PIMSISHINAGP where SHNCD = (select SHNCD from INAWS.PIMSIT where SHNCD = '"+szShncd+"' and SITSTCD <= 2)");

		if (DefineReport.ID_DEBUG_MODE)	System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("仕掛品揃グループマスタ" + tbl.getTxt());
		return result;
	}

	/**
	 * 仕掛メーカーマスタDELETE処理
	 *
	 * @param userId
	 * @param data
	 * @param tbl
	 *
	 * @throws Exception
	 */
	public JSONObject createSqlPIMSIMAKER_DEL(String userId, String btnId, JSONObject data, TblType tbl, SqlType sql, String szShncd){
		JSONObject result = new JSONObject();

		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();
		String values = "", names = "";

		StringBuffer sbSQL;
		sbSQL = new StringBuffer();
		sbSQL.append("delete from INAWS.PIMSIMAKER where MAKERCD = (select MAKERCD from INAWS.PIMSIT where SHNCD = '"+szShncd+"' and SITSTCD <= 2)");

		if (DefineReport.ID_DEBUG_MODE)	System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("仕掛メーカーマスタ" + tbl.getTxt());
		return result;
	}

	public JSONObject getSqlPartsCmnCommandMSTSHN251(TblType tbl, SqlType sql) {
		JSONObject json = new JSONObject();
		String szTable = "INAWS.PIMSIT";
		String szWhere = "T.SHNCD = RE.SHNCD";

		if(TblType.JNL.getVal()==tbl.getVal()){
			szTable = "INAAD.JNLSHN";
			szWhere = "T.SEQ = RE.SEQ";
		}else if(TblType.CSV.getVal()==tbl.getVal()){
			szTable = "INAMS.CSVSHN";
			szWhere = "T.SEQ = RE.SEQ and T.INPUTNO = RE.INPUTNO";
		}

		json.put("TABLE", szTable);
		json.put("WHERE", szWhere);
		return json;
	}

	/**
	 * 仕掛商品マスタMerge共通SQL作成処理
	 *
	 * @param tbl
	 * @param values
	 * @param names
	 * @param userId
	 *
	 * @throws Exception
	 */
	public String createMergeCmnCommandPIMSIT(TblType tbl, SqlType sql, String values, String names) {

		// 条件文など取得
		JSONObject parts = this.getSqlPartsCmnCommandMSTSHN251(tbl, sql);
		String szTable = parts.optString("TABLE");
		String szWhere = parts.optString("WHERE");

		// 基本Merge文
		StringBuffer sbSQL;
		sbSQL = new StringBuffer();
		sbSQL.append("merge into "+szTable+" as T");
		sbSQL.append(" using (select ");
		for(MSTSHNLayout itm :MSTSHNLayout.values()){
			if(itm.equals(MSTSHNLayout.UPDDT)||itm.equals(MSTSHNLayout.ADDDT)){ continue; }
			if(itm.equals(MSTSHNLayout.TEIANNO)||itm.equals(MSTSHNLayout.STCD_SHIKAKARI)) {
				if(TblType.JNL.getVal()==tbl.getVal()){
					continue;
				}
			}
			if(itm.getNo() > 1){ sbSQL.append(" ,"); }
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
	 * 仕掛商品マスタの商品コードを商品マスタ用の商品コードに変更
	 *
	 * @param userId
	 * @param btnId
	 * @param data
	 * @param tbl
	 * @param sql
	 * @param userInfo
	 * @param teianNo
	 * @param szShncd
	 * @param msgShnCd
	 *
	 * @throws Exception
	 */
	public JSONObject updatePIMSIT_SHNCD(String userId, String btnId, JSONObject data, TblType tbl, SqlType sql, User userInfo, String teianNo, String szShncd, String msgShnCd) {
		JSONObject result = new JSONObject();

		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();
		String values = "", names = "";
		int colNum = mstshn_col_num;

		String userID = userInfo.getId();
		CmnDate dateInfo = new CmnDate();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss.SSS");
		String strDateInfo = dateFormat.format(dateInfo);

		StringBuffer sbSQL;
		sbSQL = new StringBuffer();
		sbSQL.append("update INAWS.PIMSIT set SHNCD = '"+msgShnCd+"', OPERATOR = '"+userID+"', UPDDT = '"+strDateInfo+"' where SITKNNO = "+teianNo+" and SITSTCD = 4 and SHNCD like '"+szShncd+"%'");

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("仕掛商品マスタ" + tbl.getTxt());
		return result;
	}
	/**
	 * 仕掛仕入グループ商品マスタの商品コードを商品マスタ用の商品コードに変更
	 *
	 * @param userId
	 * @param btnId
	 * @param data
	 * @param tbl
	 * @param sql
	 * @param userInfo
	 * @param teianNo
	 * @param szShncd
	 * @param msgShnCd
	 *
	 * @throws Exception
	 */
	public JSONObject updatePIMSISIRGPSHN_SHNCD(String userId, String btnId, JSONObject data, TblType tbl, SqlType sql, User userInfo, String teianNo, String szShncd, String msgShnCd) {
		JSONObject result = new JSONObject();

		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();
		String values = "", names = "";
		int colNum = mstshn_col_num;

		String userID = userInfo.getId();
		CmnDate dateInfo = new CmnDate();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss.SSS");
		String strDateInfo = dateFormat.format(dateInfo);

		StringBuffer sbSQL;
		sbSQL = new StringBuffer();
		sbSQL.append("update INAWS.PIMSISIRGPSHN set SHNCD = '"+msgShnCd+"', OPERATOR = '"+userID+"', UPDDT = '"+strDateInfo+"' where SHNCD = '"+szShncd+"'");

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("仕掛商品発注先マスタ" + tbl.getTxt());
		return result;
	}
	/**
	 * 仕掛売価コントロールマスタの商品コードを商品マスタ用の商品コードに変更
	 *
	 * @param userId
	 * @param btnId
	 * @param data
	 * @param tbl
	 * @param sql
	 * @param userInfo
	 * @param teianNo
	 * @param szShncd
	 * @param msgShnCd
	 *
	 * @throws Exception
	 */
	public JSONObject updatePIMSIBAIKACTL_SHNCD(String userId, String btnId, JSONObject data, TblType tbl, SqlType sql, User userInfo, String teianNo, String szShncd, String msgShnCd) {
		JSONObject result = new JSONObject();

		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();
		String values = "", names = "";
		int colNum = mstshn_col_num;

		String userID = userInfo.getId();
		CmnDate dateInfo = new CmnDate();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss.SSS");
		String strDateInfo = dateFormat.format(dateInfo);

		StringBuffer sbSQL;
		sbSQL = new StringBuffer();
		//sbSQL.append("update INAWS.PIMSIT set SHNCD = '"+msgShnCd+"', OPERATOR = '"+userID+"', UPDDT = '"+strDateInfo+"' where SITKNNO = "+teianNo+" and SITSTCD = 4 and SHNCD like '"+szShncd+"%'");
		sbSQL.append("update INAWS.PIMSIBAIKACTL set SHNCD = '"+msgShnCd+"', OPERATOR = '"+userID+"', UPDDT = '"+strDateInfo+"' where SHNCD = '"+szShncd+"'");

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("仕掛売価コントロールマスタ" + tbl.getTxt());
		return result;
	}
	/**
	 * 仕掛ソースコード管理マスタの商品コードを商品マスタ用の商品コードに変更
	 *
	 * @param userId
	 * @param btnId
	 * @param data
	 * @param tbl
	 * @param sql
	 * @param userInfo
	 * @param teianNo
	 * @param szShncd
	 * @param msgShnCd
	 *
	 * @throws Exception
	 */
	public JSONObject updatePIMSISRCCD_SHNCD(String userId, String btnId, JSONObject data, TblType tbl, SqlType sql, User userInfo, String teianNo, String szShncd, String msgShnCd) {
		JSONObject result = new JSONObject();

		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();
		String values = "", names = "";
		int colNum = mstshn_col_num;

		String userID = userInfo.getId();
		CmnDate dateInfo = new CmnDate();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss.SSS");
		String strDateInfo = dateFormat.format(dateInfo);

		StringBuffer sbSQL;
		sbSQL = new StringBuffer();
		sbSQL.append("update INAWS.PIMSISRCCD set SHNCD = '"+msgShnCd+"', OPERATOR = '"+userID+"', UPDDT = '"+strDateInfo+"' where SHNCD = '"+szShncd+"'");

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("仕掛ソースコード管理マスタ" + tbl.getTxt());
		return result;
	}
	/**
	 * 仕掛添加物マスタの商品コードを商品マスタ用の商品コードに変更
	 *
	 * @param userId
	 * @param btnId
	 * @param data
	 * @param tbl
	 * @param sql
	 * @param userInfo
	 * @param teianNo
	 * @param szShncd
	 * @param msgShnCd
	 *
	 * @throws Exception
	 */
	public JSONObject updatePIMSITENKABUTSU_SHNCD(String userId, String btnId, JSONObject data, TblType tbl, SqlType sql, User userInfo, String teianNo, String szShncd, String msgShnCd) {
		JSONObject result = new JSONObject();

		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();
		String values = "", names = "";
		int colNum = mstshn_col_num;

		String userID = userInfo.getId();
		CmnDate dateInfo = new CmnDate();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss.SSS");
		String strDateInfo = dateFormat.format(dateInfo);

		StringBuffer sbSQL;
		sbSQL = new StringBuffer();
		sbSQL.append("update INAWS.PIMSITENKABUTSU set SHNCD = '"+msgShnCd+"', OPERATOR = '"+userID+"', UPDDT = '"+strDateInfo+"' where SHNCD = '"+szShncd+"'");

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("仕掛添加物マスタ" + tbl.getTxt());
		return result;
	}
	/**
	 * 仕掛品揃グループマスタの商品コードを商品マスタ用の商品コードに変更
	 *
	 * @param userId
	 * @param btnId
	 * @param data
	 * @param tbl
	 * @param sql
	 * @param userInfo
	 * @param teianNo
	 * @param szShncd
	 * @param msgShnCd
	 *
	 * @throws Exception
	 */
	public JSONObject updatePIMSISHINAGP_SHNCD(String userId, String btnId, JSONObject data, TblType tbl, SqlType sql, User userInfo, String teianNo, String szShncd, String msgShnCd) {
		JSONObject result = new JSONObject();

		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();
		String values = "", names = "";
		int colNum = mstshn_col_num;

		String userID = userInfo.getId();
		CmnDate dateInfo = new CmnDate();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss.SSS");
		String strDateInfo = dateFormat.format(dateInfo);

		StringBuffer sbSQL;
		sbSQL = new StringBuffer();
		sbSQL.append("update INAWS.PIMSISHINAGP set SHNCD = '"+msgShnCd+"', OPERATOR = '"+userID+"', UPDDT = '"+strDateInfo+"' where SHNCD = '"+szShncd+"'");

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("仕掛品揃グループマスタ" + tbl.getTxt());
		return result;
	}

	/**
	 * 仕掛商品マスタから商品マスタへ登録
	 *
	 * @param userId
	 * @param btnId
	 * @param data
	 * @param tbl
	 * @param sql
	 * @param userInfo
	 * @param teianNo
	 * @param szShncd
	 * @param msgShnCd
	 *
	 * @throws Exception
	 */
	public JSONObject PIMSIT2MSTSHN(String userId, String btnId, JSONObject data, TblType tbl, SqlType sql, User userInfo, String teianNo, String szShncd, String msgShnCd) {
		JSONObject result = new JSONObject();

		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();
		String values = "", names = "";
		int colNum = mstshn_col_num;

		String userID = userInfo.getId();
		CmnDate dateInfo = new CmnDate();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss.SSS");
		String strDateInfo = dateFormat.format(dateInfo);

		StringBuffer sbSQL;
		sbSQL = new StringBuffer();

		sbSQL.append("insert into INAMS.MSTSHN (");
		sbSQL.append("  SHNCD, YOYAKUDT, TENBAIKADT, YOT_BMNCD, YOT_DAICD, YOT_CHUCD, YOT_SHOCD");
		sbSQL.append(" ,URI_BMNCD, URI_DAICD, URI_CHUCD, URI_SHOCD, BMNCD, DAICD, CHUCD, SHOCD, SSHOCD");
		sbSQL.append(" ,ATSUK_STDT, ATSUK_EDDT, TEISHIKBN, SHNAN, SHNKN, PCARDKN, POPKN, RECEIPTAN");
		sbSQL.append(" ,RECEIPTKN, PCKBN, KAKOKBN, ICHIBAKBN, SHNKBN, SANCHIKN, SSIRCD, HSPTN");
		sbSQL.append(" ,RG_ATSUKFLG, RG_GENKAAM, RG_BAIKAAM, RG_IRISU, RG_IDENFLG, RG_WAPNFLG");
		sbSQL.append(" ,HS_ATSUKFLG, HS_GENKAAM, HS_BAIKAAM, HS_IRISU, HS_WAPNFLG, HS_SPOTMINSU");
		sbSQL.append(" ,HP_SWAPNFLG, KIKKN, UP_YORYOSU, UP_TYORYOSU, UP_TANIKBN, SHNYOKOSZ, SHNTATESZ");
		sbSQL.append(" ,SHNOKUSZ, SHNJRYOSZ, PBKBN, KOMONOKBM, TANAOROKBN, TEIKEIKBN, ODS_HARUSU");
		sbSQL.append(" ,ODS_NATSUSU, ODS_AKISU, ODS_FUYUSU, ODS_NYUKASU, ODS_NEBIKISU, PCARD_SHUKBN");
		sbSQL.append(" ,PCARD_IROKBN, ZEIKBN, ZEIRTKBN, ZEIRTKBN_OLD, ZEIRTHENKODT, SEIZOGENNISU, TEIKANKBN");
		sbSQL.append(" ,MAKERCD, IMPORTKBN, SIWAKEKBN, HENPIN_KBN, TAISHONENSU, CALORIESU, ELPFLG");
		sbSQL.append(" ,BELLMARKFLG, RECYCLEFLG, ECOFLG, HZI_YOTO, HZI_ZAISHITU, HZI_RECYCLE, KIKANKBN");
		sbSQL.append(" ,SHUKYUKBN, DOSU, CHINRETUCD, DANTUMICD, KASANARICD, KASANARISZ, ASSHUKURT");
		sbSQL.append(" ,SHUBETUCD, URICD, SALESCOMKN, URABARIKBN, PCARD_OPFLG, PARENTCD, BINKBN");
		sbSQL.append(" ,HAT_MONKBN, HAT_TUEKBN, HAT_WEDKBN, HAT_THUKBN, HAT_FRIKBN, HAT_SATKBN");
		sbSQL.append(" ,HAT_SUNKBN, READTMPTN, SIMEKAISU, IRYOREFLG, TOROKUMOTO, UPDKBN, SENDFLG");
		sbSQL.append(" ,OPERATOR, ADDDT, UPDDT, K_HONKB, K_WAPNFLG_R, K_WAPNFLG_H, K_TORIKB, ITFCD, CENTER_IRISU)");

		sbSQL.append(" select");
		sbSQL.append("  SHNCD");													// 商品コード：
		sbSQL.append(", YOYAKUDT, TENBAIKADT, YOT_BMNCD, YOT_DAICD, YOT_CHUCD, YOT_SHOCD, URI_BMNCD");
		sbSQL.append(", URI_DAICD, URI_CHUCD, URI_SHOCD, BMNCD, DAICD, CHUCD, SHOCD, SSHOCD, ATSUK_STDT");
		sbSQL.append(", ATSUK_EDDT, TEISHIKBN, SHNAN, SHNKN, PCARDKN, POPKN, RECEIPTAN, RECEIPTKN, PCKBN");
		sbSQL.append(", KAKOKBN, ICHIBAKBN, SHNKBN, SANCHIKN, SSIRCD, HSPTN, RG_ATSUKFLG, RG_GENKAAM");
		sbSQL.append(", RG_BAIKAAM, RG_IRISU, RG_IDENFLG, RG_WAPNFLG, HS_ATSUKFLG, HS_GENKAAM, HS_BAIKAAM");
		sbSQL.append(", HS_IRISU, HS_WAPNFLG, HS_SPOTMINSU, HP_SWAPNFLG, KIKKN, UP_YORYOSU, UP_TYORYOSU");
		sbSQL.append(", UP_TANIKBN, SHNYOKOSZ, SHNTATESZ, SHNOKUSZ, SHNJRYOSZ, PBKBN, KOMONOKBM, TANAOROKBN");
		sbSQL.append(", TEIKEIKBN, ODS_HARUSU, ODS_NATSUSU, ODS_AKISU, ODS_FUYUSU, ODS_NYUKASU, ODS_NEBIKISU");
		sbSQL.append(", PCARD_SHUKBN, PCARD_IROKBN, ZEIKBN, ZEIRTKBN, ZEIRTKBN_OLD, ZEIRTHENKODT, SEIZOGENNISU");
		sbSQL.append(", TEIKANKBN, MAKERCD, IMPORTKBN, SIWAKEKBN, HENPIN_KBN, TAISHONENSU, CALORIESU, ELPFLG");
		sbSQL.append(", BELLMARKFLG, RECYCLEFLG, ECOFLG, HZI_YOTO, HZI_ZAISHITU, HZI_RECYCLE, KIKANKBN, SHUKYUKBN");
		sbSQL.append(", DOSU, CHINRETUCD, DANTUMICD, KASANARICD, KASANARISZ, ASSHUKURT, SHUBETUCD, URICD");
		sbSQL.append(", SALESCOMKN, URABARIKBN, PCARD_OPFLG, PARENTCD, BINKBN, HAT_MONKBN, HAT_TUEKBN");
		sbSQL.append(", HAT_WEDKBN, HAT_THUKBN, HAT_FRIKBN, HAT_SATKBN, HAT_SUNKBN, READTMPTN, SIMEKAISU");
		sbSQL.append(", IRYOREFLG, TOROKUMOTO, UPDKBN, SENDFLG");
		sbSQL.append(", '"+userID+"' as OPERATOR");						// オペレータ
		sbSQL.append(", '"+strDateInfo+"' as ADDDT");						// 登録日
		sbSQL.append(", '"+strDateInfo+"' as UPDDT");						// 更新日
		sbSQL.append(", K_HONKB, K_WAPNFLG_R, K_WAPNFLG_H, K_TORIKB, ITFCD, CENTER_IRISU");
		sbSQL.append(" from INAWS.PIMSIT");
		sbSQL.append(" where SITKNNO = '"+teianNo+"' and SITSTCD = 4 and SHNCD like '"+msgShnCd+"%'");

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("商品マスタ" + tbl.getTxt());
		return result;
	}

	/**
	 * 仕掛仕入グループ商品マスタから仕入グループ商品マスタへ登録
	 *
	 * @param userId
	 * @param btnId
	 * @param data
	 * @param tbl
	 * @param sql
	 * @param userInfo
	 * @param teianNo
	 * @param szShncd
	 * @param msgShnCd
	 *
	 * @throws Exception
	 */
	public JSONObject PIMSISIRGPSHN2MSTSIRGPSHN(String userId, String btnId, JSONObject data, TblType tbl, SqlType sql, User userInfo, String teianNo, String szShncd, String msgShnCd) {
		JSONObject result = new JSONObject();

		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();
		String values = "", names = "";
		int colNum = mstshn_col_num;

		String userID = userInfo.getId();
		CmnDate dateInfo = new CmnDate();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss.SSS");
		String strDateInfo = dateFormat.format(dateInfo);

		StringBuffer sbSQL;
		sbSQL = new StringBuffer();

		sbSQL.append("insert into INAMS.MSTSIRGPSHN (");
		sbSQL.append("  SHNCD, TENGPCD, YOYAKUDT, AREAKBN");
		sbSQL.append(", SIRCD, HSPTN, SENDFLG, OPERATOR, ADDDT, UPDDT");

		sbSQL.append(") select");
		sbSQL.append("  SHNCD, TENGPCD, YOYAKUDT, AREAKBN");
		sbSQL.append(", SIRCD, HSPTN, SENDFLG");
		sbSQL.append(", '"+userID+"' as OPERATOR");						// オペレータ
		sbSQL.append(", '"+strDateInfo+"' as ADDDT");					// 登録日
		sbSQL.append(", '"+strDateInfo+"' as UPDDT");						// 更新日
		sbSQL.append(" from INAWS.PIMSISIRGPSHN");
		sbSQL.append(" where SHNCD = (");
		sbSQL.append(" select SHNCD from INAWS.PIMSIT");
		sbSQL.append(" where SHNCD = '"+msgShnCd+"' and SITSTCD = 4");
		sbSQL.append(" )");

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("仕入グループ商品マスタ" + tbl.getTxt());
		return result;
	}
	/**
	 * 仕掛売価コントロールマスタから売価コントロールマスタへ登録
	 *
	 * @param userId
	 * @param btnId
	 * @param data
	 * @param tbl
	 * @param sql
	 * @param userInfo
	 * @param teianNo
	 * @param szShncd
	 * @param msgShnCd
	 *
	 * @throws Exception
	 */
	public JSONObject PIMSIBAIKACTL2MSTBAIKACTL(String userId, String btnId, JSONObject data, TblType tbl, SqlType sql, User userInfo, String teianNo, String szShncd, String msgShnCd) {
		JSONObject result = new JSONObject();

		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();
		String values = "", names = "";
		int colNum = mstshn_col_num;

		String userID = userInfo.getId();
		CmnDate dateInfo = new CmnDate();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss.SSS");
		String strDateInfo = dateFormat.format(dateInfo);

		StringBuffer sbSQL;
		sbSQL = new StringBuffer();

		sbSQL.append("insert into INAMS.MSTBAIKACTL(");
		sbSQL.append("  SHNCD, TENGPCD, YOYAKUDT, AREAKBN, GENKAAM");
		sbSQL.append(", BAIKAAM, IRISU, SENDFLG, OPERATOR, ADDDT, UPDDT");

		sbSQL.append(") select");
		sbSQL.append("  SHNCD, TENGPCD, YOYAKUDT, AREAKBN, GENKAAM");
		sbSQL.append(", BAIKAAM, IRISU, SENDFLG");
		sbSQL.append(", '"+userID+"' as OPERATOR");						// オペレータ
		sbSQL.append(", '"+strDateInfo+"' as ADDDT");					// 登録日
		sbSQL.append(", '"+strDateInfo+"' as UPDDT");						// 更新日
		sbSQL.append(" from INAWS.PIMSIBAIKACTL");
		sbSQL.append(" where SHNCD = (");
		sbSQL.append(" select SHNCD from INAWS.PIMSIT");
		sbSQL.append(" where SHNCD = '"+msgShnCd+"' and SITSTCD = 4");
		sbSQL.append(" )");

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("売価コントロールマスタ" + tbl.getTxt());
		return result;
	}
	/**
	 * 仕掛ソースコード管理マスタからソースコード管理マスタへ登録
	 *
	 * @param userId
	 * @param btnId
	 * @param data
	 * @param tbl
	 * @param sql
	 * @param userInfo
	 * @param teianNo
	 * @param szShncd
	 * @param msgShnCd
	 *
	 * @throws Exception
	 */
	public JSONObject PIMSISRCCD2MSTSRCCD(String userId, String btnId, JSONObject data, TblType tbl, SqlType sql, User userInfo, String teianNo, String szShncd, String msgShnCd) {
		JSONObject result = new JSONObject();

		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();
		String values = "", names = "";
		int colNum = mstshn_col_num;

		String userID = userInfo.getId();
		CmnDate dateInfo = new CmnDate();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss.SSS");
		String strDateInfo = dateFormat.format(dateInfo);

		StringBuffer sbSQL;
		sbSQL = new StringBuffer();

		sbSQL.append("insert into INAMS.MSTSRCCD(");
		sbSQL.append("  SHNCD, SRCCD, YOYAKUDT, SEQNO, SOURCEKBN");
		sbSQL.append(", SENDFLG, OPERATOR, ADDDT, UPDDT, YUKO_STDT, YUKO_EDDT");

		sbSQL.append(") select");
		sbSQL.append("  SHNCD, SRCCD, YOYAKUDT, SEQNO, SOURCEKBN");
		sbSQL.append(", SENDFLG");
		sbSQL.append(", '"+userID+"' as OPERATOR");						// オペレータ
		sbSQL.append(", '"+strDateInfo+"' as ADDDT");					// 登録日
		sbSQL.append(", '"+strDateInfo+"' as UPDDT");						// 更新日
		sbSQL.append(", YUKO_STDT, YUKO_EDDT");
		sbSQL.append(" from INAWS.PIMSISRCCD");
		sbSQL.append(" where SHNCD = (");
		sbSQL.append(" select SHNCD from INAWS.PIMSIT");
		sbSQL.append(" where SHNCD = '"+msgShnCd+"' and SITSTCD = 4");
		sbSQL.append(" )");

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("ソースコード管理マスタ" + tbl.getTxt());
		return result;
	}
	/**
	 * 仕掛添加物マスタから添加物マスタへ登録
	 *
	 * @param userId
	 * @param btnId
	 * @param data
	 * @param tbl
	 * @param sql
	 * @param userInfo
	 * @param teianNo
	 * @param szShncd
	 * @param msgShnCd
	 *
	 * @throws Exception
	 */
	public JSONObject PIMSITENKABUTSU2MSTTENKABUTSU(String userId, String btnId, JSONObject data, TblType tbl, SqlType sql, User userInfo, String teianNo, String szShncd, String msgShnCd) {
		JSONObject result = new JSONObject();

		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();
		String values = "", names = "";
		int colNum = mstshn_col_num;

		String userID = userInfo.getId();
		CmnDate dateInfo = new CmnDate();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss.SSS");
		String strDateInfo = dateFormat.format(dateInfo);

		StringBuffer sbSQL;
		sbSQL = new StringBuffer();

		sbSQL.append("insert into INAMS.MSTTENKABUTSU(");
		sbSQL.append("  SHNCD, TENKABKBN, TENKABCD, YOYAKUDT");
		sbSQL.append(", SENDFLG, OPERATOR, ADDDT, UPDDT");

		sbSQL.append(") select");
		sbSQL.append("  SHNCD, TENKABKBN, TENKABCD, YOYAKUDT");
		sbSQL.append(", SENDFLG");
		sbSQL.append(", '"+userID+"' as OPERATOR");						// オペレータ
		sbSQL.append(", '"+strDateInfo+"' as ADDDT");					// 登録日
		sbSQL.append(", '"+strDateInfo+"' as UPDDT");						// 更新日
		sbSQL.append(" from INAWS.PIMSITENKABUTSU");
		sbSQL.append(" where SHNCD = (");
		sbSQL.append(" select SHNCD from INAWS.PIMSIT");
		sbSQL.append(" where SHNCD = '"+msgShnCd+"' and SITSTCD = 4");
		sbSQL.append(" )");

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("添加物マスタ" + tbl.getTxt());
		return result;
	}
	/**
	 * 仕掛品揃グループマスタから品揃グループマスタへ登録
	 *
	 * @param userId
	 * @param btnId
	 * @param data
	 * @param tbl
	 * @param sql
	 * @param userInfo
	 * @param teianNo
	 * @param szShncd
	 * @param msgShnCd
	 *
	 * @throws Exception
	 */
	public JSONObject PIMSISHINAGP2MSTSHINAGP(String userId, String btnId, JSONObject data, TblType tbl, SqlType sql, User userInfo, String teianNo, String szShncd, String msgShnCd) {
		JSONObject result = new JSONObject();

		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();
		String values = "", names = "";
		int colNum = mstshn_col_num;

		String userID = userInfo.getId();
		CmnDate dateInfo = new CmnDate();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss.SSS");
		String strDateInfo = dateFormat.format(dateInfo);

		StringBuffer sbSQL;
		sbSQL = new StringBuffer();

		sbSQL.append("insert into INAMS.MSTSHINAGP(");
		sbSQL.append("  SHNCD, TENGPCD, YOYAKUDT, AREAKBN");
		sbSQL.append(", ATSUKKBN, SENDFLG, OPERATOR, ADDDT, UPDDT");

		sbSQL.append(") select");
		sbSQL.append("  SHNCD, TENGPCD, YOYAKUDT, AREAKBN");
		sbSQL.append(", ATSUKKBN, SENDFLG");
		sbSQL.append(", '"+userID+"' as OPERATOR");						// オペレータ
		sbSQL.append(", '"+strDateInfo+"' as ADDDT");					// 登録日
		sbSQL.append(", '"+strDateInfo+"' as UPDDT");						// 更新日
		sbSQL.append(" from INAWS.PIMSISHINAGP");
		sbSQL.append(" where SHNCD = (");
		sbSQL.append(" select SHNCD from INAWS.PIMSIT");
		sbSQL.append(" where SHNCD = '"+msgShnCd+"' and SITSTCD = 4");
		sbSQL.append(" )");

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("品揃グループマスタ" + tbl.getTxt());
		return result;
	}
	/**
	 * 仕掛メーカーマスタからメーカーマスタへ登録
	 *
	 * @param userId
	 * @param btnId
	 * @param data
	 * @param tbl
	 * @param sql
	 * @param userInfo
	 * @param teianNo
	 * @param szShncd
	 * @param msgShnCd
	 *
	 * @throws Exception
	 */
	public JSONObject PIMSIMAKER2MSTMAKER(String userId, String btnId, JSONObject data, TblType tbl, SqlType sql, User userInfo, String teianNo, String szShncd, String msgShnCd) {
		JSONObject result = new JSONObject();

		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();
		String values = "", names = "";
		int colNum = mstshn_col_num;

		String userID = userInfo.getId();
		CmnDate dateInfo = new CmnDate();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss.SSS");
		String strDateInfo = dateFormat.format(dateInfo);

		StringBuffer sbSQL;
		sbSQL = new StringBuffer();

		sbSQL.append("insert into INAMS.MSTMAKER(");
		sbSQL.append("  MAKERCD, MAKERAN, MAKERKN, JANCD, DMAKERCD");
		sbSQL.append(", UPDKBN, SENDFLG, OPERATOR, ADDDT, UPDDT");

		sbSQL.append(") select");
		sbSQL.append("  MAKERCD, MAKERAN, MAKERKN, JANCD, DMAKERCD");
		sbSQL.append(", UPDKBN, SENDFLG");
		sbSQL.append(", '"+userID+"' as OPERATOR");						// オペレータ
		sbSQL.append(", '"+strDateInfo+"' as ADDDT");					// 登録日
		sbSQL.append(", '"+strDateInfo+"' as UPDDT");						// 更新日
		sbSQL.append(" from INAWS.PIMSIMAKER");
		sbSQL.append(" where MAKERCD = (");
		sbSQL.append(" select MAKERCD from INAWS.PIMSIT");
		sbSQL.append(" where SITKNNO = '"+teianNo+"' and SHNCD = '"+msgShnCd+"' and SITSTCD = 4");
		sbSQL.append(" )");

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("メーカーマスタ" + tbl.getTxt());
		return result;
	}

	/**
	 * 提案商品の商品ステータスを「完了」に更新
	 *
	 * @param userId
	 * @param btnId
	 * @param data
	 * @param tbl
	 * @param sql
	 * @param userInfo
	 * @param teianNo
	 * @param szShncd
	 * @param msgShnCd
	 * @param szShnkn
	 * @param szBmncd
	 * @param szDaicd
	 * @param szChucd
	 *
	 * @throws Exception
	 */
	public JSONObject updateTeianSTCD(String userId, String btnId, JSONObject data, TblType tbl, SqlType sql, User userInfo, String teianNo, String szShncd, String msgShnCd, String szShnkn, String szBmncd, String szDaicd, String szChucd) {
		JSONObject result = new JSONObject();

		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();
		String values = "", names = "";
		int colNum = mstshn_col_num;

		String userID = userInfo.getId();
		CmnDate dateInfo = new CmnDate();
		String sysDate = dateInfo.getToday();				// 本日日付
		String sysTime = dateInfo.getNowTime();				// 現在時刻
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss.SSS");
		String strDateInfo = dateFormat.format(dateInfo);

		StringBuffer sbSQL;
		sbSQL = new StringBuffer();

		// 提案商品（INAWS.PIMTIT）の商品ステータス（SITSTCD）を完了（4）に更新
		//sbSQL.append("update INAWS.PIMTIT set TITSTCD = 4, OPERATOR = '"+userID+"', UPDDT = '"+strDateInfo+"' where TITKNNO = (select SITKNNO from INAWS.PIMSIT where SITKNNO = "+teianNo+" and SITSTCD = 4) and TITSTCD = 3");
		//sbSQL.append("update INAWS.PIMTIT set TITSTCD = 4, OPERATOR = '"+userID+"', UPDDT = '"+strDateInfo+"' where TITKNNO = (select SITKNNO from INAWS.PIMSIT where SHNCD like '"+szShncd+"%' and SITSTCD = 4) and SHNKN = '"+szShnkn+"' and BMNCD = '"+szBmncd+"' and DAICD = '"+szDaicd+"' and CHUCD = '"+szChucd+"' and TITSTCD = 3;");
		sbSQL.append("update INAWS.PIMTIT set TITSTCD = 4, OPERATOR = '"+userID+"', UPDDT = '"+strDateInfo+"' where TITKNNO = (select SITKNNO from INAWS.PIMSIT where SHNCD = '"+msgShnCd+"' and SITSTCD = 4) and SHNKN = '"+szShnkn+"' and BMNCD = '"+szBmncd+"' and DAICD = '"+szDaicd+"' and CHUCD = '"+szChucd+"' and TITSTCD = 3");

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("提案商品マスタ" + tbl.getTxt());
		return result;
	}

	/**
	 * 提案件名の商品ステータスを「完了」に更新
	 *
	 * @param userId
	 * @param btnId
	 * @param data
	 * @param tbl
	 * @param sql
	 * @param userInfo
	 * @param teianNo
	 * @param szShncd
	 * @param msgShnCd
	 * @param szShnkn
	 * @param szBmncd
	 * @param szDaicd
	 * @param szChucd
	 *
	 * @throws Exception
	 */
	public JSONObject updateTeianKenmeiSTCD(String userId, String btnId, JSONObject data, TblType tbl, SqlType sql, User userInfo, String teianNo, String szShncd, String msgShnCd, String szShnkn, String szBmncd, String szDaicd, String szChucd) {
		JSONObject result = new JSONObject();

		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();
		String values = "", names = "";
		int colNum = mstshn_col_num;

		String userID = userInfo.getId();
		CmnDate dateInfo = new CmnDate();
		String sysDate = dateInfo.getToday();				// 本日日付
		String sysTime = dateInfo.getNowTime();				// 現在時刻
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss.SSS");
		String strDateInfo = dateFormat.format(dateInfo);

		StringBuffer sbSQL;
		sbSQL = new StringBuffer();

		// 提案件名（INAWS.PIMTIK）の件名ステータス（TIKSTCD）を完了（4）に更新
		sbSQL.append("update INAWS.PIMTIK set TIKSTCD = 4, TIKEDDT = current_timestamp, T1UPID = '"+userID+"', T1UPDT = '"+sysDate+"', T1UPTM = '"+sysTime+"' where TIKKNNO = (select SITKNNO from INAWS.PIMSIT where SHNCD = '"+msgShnCd+"' and SITSTCD > 3) and TIKSTCD = 3 and (select COUNT(TITKNNO) from INAWS.PIMTIT where TITKNNO = (select SITKNNO from INAWS.PIMSIT where SHNCD = '"+msgShnCd+"' and SITSTCD > 3) and TITSTCD > 3) = (select COUNT(TITKNNO) from INAWS.PIMTIT where TITKNNO = (select SITKNNO from INAWS.PIMSIT where SHNCD = '"+msgShnCd+"' and SITSTCD > 3))");

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("提案件名マスタ" + tbl.getTxt());
		return result;
	}

	/**
	 * 仕掛、提案商品マスタの商品ステータスを「却下」に更新
	 *
	 * @param userId
	 * @param btnId
	 * @param data
	 * @param tbl
	 * @param sql
	 * @param userInfo
	 * @param teianNo
	 * @param szShncd
	 * @param szShnkn
	 * @param szBmncd
	 * @param szDaicd
	 * @param szChucd
	 *
	 * @throws Exception
	 */
	public JSONObject updateTeianSTCD2(String userId, String btnId, JSONObject data, TblType tbl, SqlType sql, User userInfo, String teianNo, String szShncd, String szShnkn, String szBmncd, String szDaicd, String szChucd) {
		JSONObject result = new JSONObject();

		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();
		String values = "", names = "";
		int colNum = mstshn_col_num;

		String userID = userInfo.getId();
		CmnDate dateInfo = new CmnDate();
		String sysDate = dateInfo.getToday();				// 本日日付
		String sysTime = dateInfo.getNowTime();				// 現在時刻
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss.SSS");
		String strDateInfo = dateFormat.format(dateInfo);

		StringBuffer sbSQL;
		sbSQL = new StringBuffer();

		// 提案商品（INAWS.PIMTIT）の商品ステータス（TITSTCD）を却下（9）に更新
		sbSQL.append("update INAWS.PIMTIT set TITSTCD = 9, OPERATOR = '"+userID+"', UPDDT = '"+strDateInfo+"' where TITKNNO = (select SITKNNO from INAWS.PIMSIT where SHNCD like '"+szShncd+"%' and SITSTCD = 9) and SHNKN = '"+szShnkn+"' and BMNCD = '"+szBmncd+"' and DAICD = '"+szDaicd+"' and CHUCD = '"+szChucd+"' and TITSTCD = 3");

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("提案商品マスタ" + tbl.getTxt());
		return result;
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
	public JSONObject createSqlMSTSIRGPSHN251(String userId, String btnId, JSONArray dataArray, TblType tbl, SqlType sql){
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
				if(i==MSTSIRGPSHNLayout.SHNCD.getNo()){
					if(TblType.JNL.getVal()==tbl.getVal()){
						val = msgShnCd;
					}
				}else if(i==MSTSIRGPSHNLayout.SENDFLG.getNo()){				// 送信フラグ
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
		sbSQL.append(this.createMergeCmnCommandMSTSIRGPSHN251(tbl, sql, rows, names));
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
	public String createMergeCmnCommandMSTSIRGPSHN251(TblType tbl, SqlType sql, String values, String names) {
		String szTable = "INAWS.PIMSISIRGPSHN";
		String szWhere = "T.SHNCD = RE.SHNCD";
		if(SqlType.DEL.getVal()!=sql.getVal()){
			szWhere += " and T.TENGPCD = RE.TENGPCD";
		}
		if(TblType.JNL.getVal()==tbl.getVal()){
			szTable = "INAAD.JNLSIRSHN";
			szWhere = "T.SEQ = RE.SEQ";
		}else if(TblType.CSV.getVal()==tbl.getVal()){
			szTable = "INAMS.CSVSIRSHN";
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
	public JSONObject createSqlMSTBAIKACTL251(String userId, String btnId, JSONArray dataArray, TblType tbl, SqlType sql){
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
				if(i==MSTBAIKACTLLayout.SHNCD.getNo()){
					if(TblType.JNL.getVal()==tbl.getVal()){
						val = msgShnCd;
					}
				}else if(i==MSTBAIKACTLLayout.SENDFLG.getNo()){				// 送信フラグ
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
		sbSQL.append(this.createMergeCmnCommandMSTBAIKACTL251(tbl, sql, rows, names));
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
	public String createMergeCmnCommandMSTBAIKACTL251(TblType tbl, SqlType sql, String values, String names) {
		String szTable = "INAWS.PIMSIBAIKACTL";
		String szWhere = "T.SHNCD = RE.SHNCD";
		if(SqlType.DEL.getVal()!=sql.getVal()){
			szWhere += " and T.TENGPCD = RE.TENGPCD";
		}
		if(TblType.JNL.getVal()==tbl.getVal()){
			szTable = "INAAD.JNLBAIKACTL";
			szWhere = "T.SEQ = RE.SEQ";
		}else if(TblType.CSV.getVal()==tbl.getVal()){
			szTable = "INAMS.CSVBAIKACTL";
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
	public JSONObject createSqlMSTSRCCD251(String userId, String btnId, JSONArray dataArray, TblType tbl, SqlType sql){
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

				if(i==MSTSRCCDLayout.SHNCD.getNo()){
					if(TblType.JNL.getVal()==tbl.getVal()){
						val = msgShnCd;
					}
				}else if(i==MSTSRCCDLayout.SENDFLG.getNo()){		// 送信フラグ
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
		sbSQL.append(this.createMergeCmnCommandMSTSRCCD251(tbl, sql, rows, names));
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
	public String createMergeCmnCommandMSTSRCCD251(TblType tbl, SqlType sql, String values, String names) {
		String szTable = "INAWS.PIMSISRCCD";
		String szWhere = "T.SHNCD = RE.SHNCD";
		if(SqlType.DEL.getVal()!=sql.getVal()){
			szWhere += " and T.SRCCD = RE.SRCCD";
		}
		if(TblType.JNL.getVal()==tbl.getVal()){
			szTable = "INAAD.JNLSRCCD";
			szWhere = "T.SEQ = RE.SEQ";
		}else if(TblType.CSV.getVal()==tbl.getVal()){
			szTable = "INAMS.CSVSRCCD";
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
	public JSONObject createSqlMSTTENKABUTSU251(String userId, String btnId, JSONArray dataArray, TblType tbl, SqlType sql){
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

				if(i==MSTTENKABUTSULayout.SHNCD.getNo()){
					if(TblType.JNL.getVal()==tbl.getVal()){
						val = msgShnCd;
					}
				}else if(i==MSTTENKABUTSULayout.SENDFLG.getNo()){		// 送信フラグ
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
		sbSQL.append(this.createMergeCmnCommandMSTTENKABUTSU251(tbl, sql, rows, names));
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
	public String createMergeCmnCommandMSTTENKABUTSU251(TblType tbl, SqlType sql, String values, String names) {
		String szTable = "INAWS.PIMSITENKABUTSU";
		String szWhere = "T.SHNCD = RE.SHNCD";
		if(SqlType.DEL.getVal()!=sql.getVal()){
			szWhere += " and T.TENKABKBN = RE.TENKABKBN and T.TENKABCD = RE.TENKABCD";
		}
		if(TblType.JNL.getVal()==tbl.getVal()){
			szTable = "INAAD.JNLTENKABUTSU";
			szWhere = "T.SEQ = RE.SEQ";
		}else if(TblType.CSV.getVal()==tbl.getVal()){
			szTable = "INAMS.CSVTENKABUTSU";
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
	public JSONObject createSqlMSTSHINAGP251(String userId, String btnId, JSONArray dataArray, TblType tbl, SqlType sql){
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

				if(i==MSTSHINAGPLayout.SHNCD.getNo()){
					if(TblType.JNL.getVal()==tbl.getVal()){
						val = msgShnCd;
					}
				}else if(i==MSTSHINAGPLayout.SENDFLG.getNo()){			// 送信フラグ
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
		sbSQL.append(this.createMergeCmnCommandMSTSHINAGP251(tbl, sql, rows, names));
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
	public String createMergeCmnCommandMSTSHINAGP251(TblType tbl, SqlType sql, String values, String names) {
		String szTable = "INAWS.PIMSISHINAGP";
		String szWhere = "T.SHNCD = RE.SHNCD";
		if(SqlType.DEL.getVal()!=sql.getVal()){
			szWhere += " and T.TENGPCD = RE.TENGPCD";
		}
		if(TblType.JNL.getVal()==tbl.getVal()){
			szTable = "INAAD.JNLSHINAGP";
			szWhere = "T.SEQ = RE.SEQ";
		}else if(TblType.CSV.getVal()==tbl.getVal()){
			szTable = "INAMS.CSVSHINAGP";
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
	public JSONObject createSqlMSTSHNTENBMN251(String userId, String btnId, JSONArray dataArray, TblType tbl, SqlType sql){
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
		sbSQL.append(this.createMergeCmnCommandMSTSHNTENBMN251(tbl, sql, rows, names));
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
	public String createMergeCmnCommandMSTSHNTENBMN251(TblType tbl, SqlType sql, String values, String names) {

		String szTable = "INAMS.MSTSHNTENBMN";
		String szWhere = "T.SHNCD = RE.SHNCD";
		if(SqlType.DEL.getVal()!=sql.getVal()){
			szWhere += " and T.TENGPCD = RE.TENGPCD";
		}
		if(TblType.JNL.getVal()==tbl.getVal()){
			szTable = "INAAD.JNLMSTSHNTENBMN";
			szWhere = "T.SEQ = RE.SEQ";
		}else if(TblType.CSV.getVal()==tbl.getVal()){
			szTable = "INAMS.CSVMSTSHNTENBMN";
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
	public JSONObject createSqlMSTGRP251(String userId, String btnId, JSONArray dataArray, TblType tbl, SqlType sql){
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
		sbSQL.append(this.createMergeCmnCommandMSTGRP251(tbl, sql, rows, names));
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
	 * グループ分類マスタMergeSQL作成処理
	 *
	 * @param tbl
	 * @param sql
	 * @param values
	 * @param names
	 * @param userId
	 *
	 * @throws Exception
	 */
	public String createMergeCmnCommandMSTGRP251(TblType tbl, SqlType sql, String values, String names) {

		String szTable = "INAMS.MSTGRP";
		String szWhere = "T.SHNCD = RE.SHNCD";
		if(SqlType.DEL.getVal()!=sql.getVal()){
			szWhere += " and T.GRPID = RE.GRPID";
		}
		if(TblType.JNL.getVal()==tbl.getVal()){
			szTable = "INAAD.JNLGRP";
			szWhere = "T.SEQ = RE.SEQ";
		}else if(TblType.CSV.getVal()==tbl.getVal()){
			szTable = "INAMS.CSVGRP";
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
	public JSONObject createSqlMSTAHS251(String userId, String btnId, JSONArray dataArray, TblType tbl, SqlType sql){
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
		sbSQL.append(this.createMergeCmnCommandMSTAHS251(tbl, sql, rows, names));
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
	public String createMergeCmnCommandMSTAHS251(TblType tbl, SqlType sql, String values, String names) {

		String szTable = "INAMS.MSTAHS";
		String szWhere = "T.SHNCD = RE.SHNCD";
		if(SqlType.DEL.getVal()!=sql.getVal()){
			szWhere += " and T.TENCD = RE.TENCD";
		}
		if(TblType.JNL.getVal()==tbl.getVal()){
			szTable = "INAAD.JNLAHS";
			szWhere = "T.SEQ = RE.SEQ";
		}else if(TblType.CSV.getVal()==tbl.getVal()){
			szTable = "INAMS.CSVAHS";
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
		String szTable = "INAWS.PIMSIMAKER";
		String szWhere = "T.MAKERCD = RE.MAKERCD";

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
		CENTER_IRISU(121,"CENTER_IRISU","SMALLINT","センター入数"),
		TEIANNO(122,"SITKNNO","INTEGER","件名No"),
		STCD_SHIKAKARI(123,"SITSTCD","INTEGER","商品ステータス")
		;

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
