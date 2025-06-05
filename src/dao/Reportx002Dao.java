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
import authentication.defines.Consts;
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
public class Reportx002Dao extends ItemDao {

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public Reportx002Dao(String JNDIname) {
    super(JNDIname);
  }

  /**
   * 検索実行
   *
   * @return
   */
  @Override
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
   *
   * @param userInfo
   *
   * @return
   * @throws IOException
   * @throws FileNotFoundException
   * @throws Exception
   */
  public JSONObject update(HttpServletRequest request, HttpSession session, HashMap<String, String> map, User userInfo) {
    // String sysdate = (String)request.getSession().getAttribute(Consts.STR_SES_LOGINDT);
    String sysdate = this.getSHORIDT();

    // 更新情報チェック(基本JS側で制御)
    JSONObject option = new JSONObject();

    JSONObject objset = this.check(map, userInfo, sysdate);
    JSONArray msgList = objset.optJSONArray("MSG");
    if (msgList.size() == 0) {
      // 更新処理
      try {
        option = this.updateData(map, userInfo, sysdate, objset);
      } catch (Exception e) {
        e.printStackTrace();
        msgList.add(MessageUtility.getDbMessageIdObj("E30007", new String[] {}));
      }
    }

    if (msgList.size() > 0) {
      option.put(MsgKey.E.getKey(), msgList.optJSONObject(0));
    } else {
      // 正 .新規
      String sendBtnid = map.get("SENDBTNID"); // 呼出しボタン
      boolean isNew = DefineReport.Button.NEW.getObj().equals(sendBtnid) || DefineReport.Button.COPY.getObj().equals(sendBtnid) || DefineReport.Button.SEL_COPY.getObj().equals(sendBtnid);
      if (isNew) {
        msgList.add(MessageUtility.getDbMessageIdObj("I00002", new String[] {"商品コード" + msgShnCd + " 販売コード" + msgUriCd + "<br>"}));
        option.put(MsgKey.S.getKey(), msgList.optJSONObject(0));
      }
    }

    // エラー時
    if (option.containsKey(MsgKey.E.getKey())) {
      // 採番実行時にエラーの場合、解除処理
      JSONObject dataOther = objset.optJSONObject("DATA_OTHER");
      if (StringUtils.isNotEmpty(dataOther.optString(MSTSHNLayout.SHNCD.getCol() + "_RENEW"))) {
        NumberingUtility.execReleaseNewSHNCD(userInfo, dataOther.optString(MSTSHNLayout.SHNCD.getCol() + "_RENEW"));
      }
      if (StringUtils.isNotEmpty(dataOther.optString(MSTSHNLayout.URICD.getCol() + "_NEW"))) {
        NumberingUtility.execReleaseNewURICD(userInfo, dataOther.optString(MSTSHNLayout.URICD.getCol() + "_NEW"));
      }
    }
    return option;
  }

  /**
   * 削除処理
   *
   * @param userInfo
   *
   * @return
   * @throws IOException
   * @throws FileNotFoundException
   * @throws Exception
   */
  public JSONObject delete(HttpServletRequest request, HttpSession session, HashMap<String, String> map, User userInfo) {
    String sysdate = (String) request.getSession().getAttribute(Consts.STR_SES_LOGINDT);

    // 更新情報チェック(基本JS側で制御)
    JSONObject option = new JSONObject();

    String menuKbn = "-1";

    // 本部マスタ
    if ((!StringUtils.isEmpty(userInfo.getYobi7_()) && !userInfo.getYobi7_().equals("-1")) || StringUtils.isEmpty(userInfo.getYobi7_())) {
      menuKbn = "4";
    }

    // 本部マスタ画面の操作でかつ削除権限がない
    if (menuKbn.equals("4") && StringUtils.isEmpty(userInfo.getYobi7_())) {

      String sendBtnid = map.get("SENDBTNID"); // 呼出しボタン
      boolean isYoyaku = DefineReport.Button.YOYAKU1.getObj().equals(sendBtnid) || DefineReport.Button.YOYAKU2.getObj().equals(sendBtnid);

      // 削除権限のないユーザーの場合でも予約削除の操作は可能
      if (!isYoyaku) {
        option.put(MsgKey.E.getKey(), MessageUtility.getDbMessageIdObj("E00012", new String[] {}));
        return option;
      }
    }

    JSONArray msgList = this.checkDel(map, userInfo, sysdate);
    if (msgList.size() > 0) {
      option.put(MsgKey.E.getKey(), msgList.optJSONObject(0));
      return option;
    }

    // 削除処理
    try {
      option = this.deleteData(map, userInfo, sysdate);
    } catch (Exception e) {
      e.printStackTrace();
      option.put(MsgKey.E.getKey(), MessageUtility.getDbMessageIdObj("E30005", new String[] {}));
    }
    return option;
  }

  private String createCommand() {
    // ログインユーザー情報取得
    User userInfo = getUserInfo();
    if (userInfo == null) {
      return "";
    }

    String szSelShncd = getMap().get("SEL_SHNCD"); // 検索商品コード
    String szShncd = getMap().get("SHNCD"); // 入力商品コード
    String szSeq = getMap().get("SEQ"); // CSVエラー.SEQ
    String szInputno = getMap().get("INPUTNO"); // CSVエラー.入力番号
    String szCsvUpdkbn = getMap().get("CSV_UPDKBN"); // CSVエラー.CSV登録区分
    String szYoyakudt = getMap().get("YOYAKUDT"); // CSVエラー用.マスタ変更予定日
    getMap().get("TENBAIKADT");
    String sendBtnid = getMap().get("SENDBTNID"); // 呼出しボタン

    // パラメータ確認
    // 必須チェック
    if (StringUtils.isEmpty(sendBtnid)) {
      System.out.println(super.getConditionLog());
      return "";
    }

    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<>();

    StringBuffer sbSQL = new StringBuffer();

    // ①正 .新規
    boolean isNew = DefineReport.Button.NEW.getObj().equals(sendBtnid);
    boolean isCopyNew = DefineReport.Button.COPY.getObj().equals(sendBtnid) || DefineReport.Button.SEL_COPY.getObj().equals(sendBtnid);
    // ②正 .変更
    boolean isChange = DefineReport.Button.SEL_CHANGE.getObj().equals(sendBtnid) || DefineReport.Button.SEARCH.getObj().equals(sendBtnid) || DefineReport.Button.SEI.getObj().equals(sendBtnid);
    // ⑨CSVエラー修正
    boolean isCsverr = DefineReport.Button.ERR_CHANGE.getObj().equals(sendBtnid);
    boolean isCsvErrNew = isCsverr && DefineReport.ValCsvUpdkbn.NEW.getVal().equals(szCsvUpdkbn) && StringUtils.equals(szYoyakudt, "0");
    // 必須チェック
    if ((isCopyNew && StringUtils.isEmpty(szSelShncd)) || (isChange && StringUtils.isEmpty(szShncd)) || (isCsverr && StringUtils.isEmpty(szInputno))) {
      System.out.println(super.getConditionLog());
      return "";
    }


    // 予約情報取得
    JSONArray yArray = new JSONArray();
    String szYoyaku = "0"; // 予約件数
    if (!isNew && !isCopyNew && !isCsvErrNew) {
      // 関連情報取得
      JSONArray array = getYoyakuJSONArray(getMap());
      szYoyaku = Integer.toString(array.size());
      yArray.addAll(array);
    }
    DefineReport.Button.YOYAKU1.getObj().equals(sendBtnid);
    DefineReport.Button.YOYAKU2.getObj().equals(sendBtnid);
    // ④予1.新規
    boolean isNewY1 = DefineReport.Button.YOYAKU1.getObj().equals(sendBtnid) && NumberUtils.toInt(szYoyaku, 0) == 0;
    // ⑤予1.変更
    boolean isChangeY1 = DefineReport.Button.YOYAKU1.getObj().equals(sendBtnid) && NumberUtils.toInt(szYoyaku, 0) > 0;
    // ⑦予2.新規
    boolean isNewY2 = DefineReport.Button.YOYAKU2.getObj().equals(sendBtnid) && NumberUtils.toInt(szYoyaku, 0) == 1;
    // ⑧予2.変更
    boolean isChangeY2 = DefineReport.Button.YOYAKU2.getObj().equals(sendBtnid) && NumberUtils.toInt(szYoyaku, 0) > 1;

    String szTableShn = "INAMS.MSTSHN";
    String szTableShina = "INAMS.MSTSHINAGP";
    String szTableBaika = "INAMS.MSTBAIKACTL";
    String szTableSir = "INAMS.MSTSIRGPSHN";
    String szTableTbmn = "INAMS.MSTSHNTENBMN";
    String szWhereSTable = " and SHNCD like '" + szSelShncd.replace("-", "") + "%'";
    String szWhereYTable = "";
    String szWhereCTable = "";
    if (DefineReport.Button.YOYAKU1.getObj().equals(sendBtnid)) {
      if (isChangeY1) {
        szTableShn += "_Y";
        szTableShina += "_Y";
        szTableBaika += "_Y";
        szTableSir += "_Y";
        szTableTbmn += "_Y";
        szWhereYTable = " and YOYAKUDT = " + yArray.optJSONObject(0).optString("F2");
      }
    }
    if (DefineReport.Button.YOYAKU2.getObj().equals(sendBtnid)) {
      if (isChangeY2) {
        szTableShn += "_Y";
        szTableShina += "_Y";
        szTableBaika += "_Y";
        szTableSir += "_Y";
        szTableTbmn += "_Y";
        szWhereYTable = " and YOYAKUDT = " + yArray.optJSONObject(1).optString("F2");
      } else if (isNewY2) {
        szTableShn += "_Y";
        szTableShina += "_Y";
        szTableBaika += "_Y";
        szTableSir += "_Y";
        szTableTbmn += "_Y";
        szWhereYTable = " and YOYAKUDT = " + yArray.optJSONObject(0).optString("F2");
      }
    }
    if (isCsverr) {
      szTableShn = "INAMS.CSVSHN";
      szTableShina = "INAMS.CSVSHINAGP";
      szTableBaika = "INAMS.CSVBAIKACTL";
      szTableSir = "INAMS.CSVSIRSHN";
      szTableTbmn = "INAMS.CSVMSTSHNTENBMN";
      szWhereSTable = "";
      szWhereCTable = " and T1.SEQ = " + szSeq + " and T1.INPUTNO = " + szInputno;
    }
    String szWhereTable = StringUtils.replaceOnce(szWhereSTable + szWhereYTable + szWhereCTable, " and", " where");

    // 完全新規
    if (isNew) {
      sbSQL.append(" select ");
      sbSQL.append("   null as SHNCD"); // F1
      sbSQL.append(" , 0 as YOYAKUDT");
      sbSQL.append(" , 0 as TENBAIKADT");
      sbSQL.append(" , null as YOT_BMNCD");
      sbSQL.append(" , null as YOT_DAICD");
      sbSQL.append(" , null as YOT_CHUCD");
      sbSQL.append(" , null as YOT_SHOCD");
      sbSQL.append(" , null as URI_BMNCD");
      sbSQL.append(" , null as URI_DAICD");
      sbSQL.append(" , null as URI_CHUCD");
      sbSQL.append(" , null as URI_SHOCD"); // F10
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
      sbSQL.append(" , 1 as BINKBN"); // F99:便区分 初期値:1
      sbSQL.append(" , null as HAT_MONKBN");
      sbSQL.append(" , null as HAT_TUEKBN");
      sbSQL.append(" , null as HAT_WEDKBN");
      sbSQL.append(" , null as HAT_THUKBN");
      sbSQL.append(" , null as HAT_FRIKBN");
      sbSQL.append(" , null as HAT_SATKBN");
      sbSQL.append(" , null as HAT_SUNKBN");
      sbSQL.append(" , null as READTMPTN");
      sbSQL.append(" , 1 as SIMEKAISU"); // F108:締め回数 初期値:1
      sbSQL.append(" , null as IRYOREFLG");
      sbSQL.append(" , 0 as TOROKUMOTO");
      sbSQL.append(" , 0 as UPDKBN");
      sbSQL.append(" , 0 as SENDFLG");
      sbSQL.append(" , null as OPERATOR");
      sbSQL.append(" , null as ADDDT");
      sbSQL.append(" , null as UPDDT");
      sbSQL.append(" , null as K_HONKB"); // F116: 保温区分
      sbSQL.append(" , null as K_WAPNFLG_R"); // F117: デリカワッペン区分_レギュラー
      sbSQL.append(" , null as K_WAPNFLG_H"); // F118: デリカワッペン区分_販促
      sbSQL.append(" , null as K_TORIKB"); // F119: 取扱区分
      sbSQL.append(" , null as ITFCD"); // F120: ITFコード
      sbSQL.append(" , null as CENTER_IRISU"); // F121: センター入数

      sbSQL.append(" , null as YOBIDASHICD"); // F122:呼出コード
      sbSQL.append(" , null as RG_AVGPTANKAAM"); // F123:TODO
      sbSQL.append(" , null as HS_AVGPTANKAAM"); // F124:販促平均パック単価(=空白)
      sbSQL.append(" , 0 as KETA"); // F125:桁 初期値:0
      sbSQL.append(" , 0 as YOYAKU"); // F126:予約件数 初期値:0

      sbSQL.append(" , null as HDN_UPDDT"); // F127:更新日時

      sbSQL.append(" , " + DefineReport.ValKbn135.VAL0.getVal() + " as AREAKBN_SHINA"); // F128
      sbSQL.append(" , " + DefineReport.ValKbn135.VAL1.getVal() + " as AREAKBN_BAIKA"); // F129
      sbSQL.append(" , " + DefineReport.ValKbn135.VAL0.getVal() + " as AREAKBN_SIR"); // F130
      sbSQL.append(" , " + DefineReport.ValKbn135.VAL0.getVal() + " as AREAKBN_TBMN"); // F131
      sbSQL.append(" from (SELECT 1 AS DUMMY) DUMMY ");

      // 流用新規・変更
    } else {

      // 流用新規の場合、商品コード、ソースコード、ソース区分、定計区分、メーカーコードは元データを参照しない
      sbSQL.append(" select ");
      if (isCopyNew) {
        sbSQL.append("   null as SHNCD"); // F1
      } else {
        sbSQL.append("   SHNCD as SHNCD"); // F1
      }
      if (!isNewY1) {
        sbSQL.append(" , right(COALESCE(T1.YOYAKUDT,0), 6) as YOYAKUDT");
        sbSQL.append(" , right(COALESCE(T1.TENBAIKADT,0), 6) as TENBAIKADT");
      } else {
        sbSQL.append(" , '0' as YOYAKUDT");
        sbSQL.append(" , '0' as TENBAIKADT");
      }
      sbSQL.append(" , CASE WHEN T1.YOT_BMNCD = '0' THEN NULL ELSE T1.YOT_BMNCD END AS YOT_BMNCD");
      sbSQL.append(" , CASE WHEN T1.YOT_BMNCD = '0' AND T1.YOT_DAICD = '0' THEN NULL ELSE T1.YOT_DAICD END AS YOT_DAICD");
      sbSQL.append(" , CASE WHEN T1.YOT_BMNCD = '0' AND T1.YOT_CHUCD = '0' THEN NULL ELSE T1.YOT_CHUCD END AS YOT_CHUCD");
      sbSQL.append(" , CASE WHEN T1.YOT_BMNCD = '0' AND T1.YOT_SHOCD = '0' THEN NULL ELSE T1.YOT_SHOCD END AS YOT_SHOCD");
      sbSQL.append(" , CASE WHEN T1.URI_BMNCD = '0' THEN NULL ELSE T1.URI_BMNCD END AS URI_BMNCD");
      sbSQL.append(" , CASE WHEN T1.URI_BMNCD = '0' AND T1.URI_DAICD = '0' THEN NULL ELSE T1.URI_DAICD END AS URI_DAICD");
      sbSQL.append(" , CASE WHEN T1.URI_BMNCD = '0' AND T1.URI_CHUCD = '0' THEN NULL ELSE T1.URI_CHUCD END AS URI_CHUCD");
      sbSQL.append(" , CASE WHEN T1.URI_BMNCD = '0' AND T1.URI_SHOCD = '0' THEN NULL ELSE T1.URI_SHOCD END AS URI_SHOCD"); // F10
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
      if (isCopyNew) {
        sbSQL.append(" , null as TEIKEIKBN");
      } else {
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
      if (isCopyNew) {
        sbSQL.append(" , null as MAKERCD");
      } else {
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
      if (isCopyNew) {
        sbSQL.append(" , null as URICD");
      } else {
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
      if (isCopyNew) {
        sbSQL.append(" , 0 as TOROKUMOTO");
        sbSQL.append(" , 0 as UPDKBN");
        sbSQL.append(" , 0 as SENDFLG");
        sbSQL.append(" , null as OPERATOR");
        sbSQL.append(" , null as ADDDT");
        sbSQL.append(" , null as UPDDT");
      } else {
        sbSQL.append(" , T1.TOROKUMOTO");
        sbSQL.append(" , T1.UPDKBN");
        if (isCsverr) {
          sbSQL.append(" , 0 as SENDFLG");
        } else {
          sbSQL.append(" , T1.SENDFLG");
        }
        sbSQL.append(" , T1.OPERATOR");
        if (isCsverr) {
          sbSQL.append(" , '__/__/__' as ADDDT");
        } else {
          sbSQL.append(" , COALESCE(DATE_FORMAT(T1.ADDDT, '%y/%m/%d'),'__/__/__') as ADDDT");
        }
        sbSQL.append(" , COALESCE(DATE_FORMAT(T1.UPDDT, '%y/%m/%d'),'__/__/__') as UPDDT");
      }
      sbSQL.append(" , T1.K_HONKB"); // F116: 保温区分
      sbSQL.append(" , T1.K_WAPNFLG_R"); // F117: デリカワッペン区分_レギュラー
      sbSQL.append(" , T1.K_WAPNFLG_H"); // F118: デリカワッペン区分_販促
      sbSQL.append(" , T1.K_TORIKB"); // F119: 取扱区分
      sbSQL.append(" , T1.ITFCD"); // F120: ITFコード
      sbSQL.append(" , T1.CENTER_IRISU"); // F121: センター入数

      sbSQL.append(" , (select YOBIDASHICD from INAMS.MSTKRYO T2 where T1.SHNCD = T2.SHNCD and T1.BMNCD = T2.BMNCD order by T2.UPDDT desc LIMIT 1 ) as YOBIDASHICD"); // F122:呼出コード
      sbSQL.append(" , (select AVGPTANKAAM from INAMS.MSTAVGPTANKA T3 where T1.SHNCD = T3.SHNCD) as RG_AVGPTANKAAM"); // F123:TODO
      sbSQL.append(" , '' as HS_AVGPTANKAAM"); // F124:販促平均パック単価(=空白)
      if (isCsverr) {
        sbSQL.append(" , KETAKBN as KETA"); // F125:桁
      } else {
        sbSQL.append(" , '' as KETA"); // F125:桁
      }
      sbSQL.append(" , " + szYoyaku + " as YOYAKU"); // F126:予約件数 初期値:0
      if (isCopyNew) {
        sbSQL.append(" , null as HDN_UPDDT"); // F127:更新日時
      } else if (isNewY2) {
        sbSQL.append(" , (select DATE_FORMAT(UPDDT, '%Y%m%d%H%i%s%f') from INAMS.MSTSHN where SHNCD like '" + szSelShncd.replace("-", "") + "%' and COALESCE(UPDKBN, 0) <> 1 " + super.getFechSql("1")
            + ") as HDN_UPDDT"); // F127:更新日時
      } else {
        sbSQL.append(" , DATE_FORMAT(T1.UPDDT, '%Y%m%d%H%i%s%f') as HDN_UPDDT"); // F127:更新日時
      }
      sbSQL.append(" , COALESCE((select max(AREAKBN) from " + szTableShina + " T1 " + szWhereTable + ")," + DefineReport.ValKbn135.VAL0.getVal() + ") as AREAKBN_SHINA"); // F128
      sbSQL.append(" , COALESCE((select max(AREAKBN) from " + szTableBaika + " T1 " + szWhereTable + ")," + DefineReport.ValKbn135.VAL1.getVal() + ") as AREAKBN_BAIKA"); // F129
      sbSQL.append(" , COALESCE((select max(AREAKBN) from " + szTableSir + " T1 " + szWhereTable + ")," + DefineReport.ValKbn135.VAL0.getVal() + ") as AREAKBN_SIR"); // F130
      sbSQL.append(" , COALESCE((select max(AREAKBN) from " + szTableTbmn + " T1 " + szWhereTable + ")," + DefineReport.ValKbn135.VAL0.getVal() + ") as AREAKBN_TBMN"); // F131
      sbSQL.append(" from " + szTableShn + " T1 ");
      sbSQL.append(" " + szWhereTable + " and COALESCE(UPDKBN, 0) <> 1 ");
      sbSQL.append(super.getFechSql("1"));
    }

    // オプション情報設定
    JSONObject option = new JSONObject();
    option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
    option.put("rows_y", yArray);
    setOption(option);

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(getClass().getSimpleName() + "[sql]" + sbSQL.toString());
    return sbSQL.toString();
  }



  private void outputQueryList() {

    // 検索条件の加工クラス作成
    JsonArrayData jad = new JsonArrayData();
    jad.setJsonString(getJson());

    // 保存用 List (検索情報)作成
    setWhere(new ArrayList<List<String>>());
    new ArrayList<String>();


    // 共通箇所設定
    createCmnOutput(jad);

  }


  boolean isTest = false;

  /**
   * 固定値定義（テーブルタイプ）<br>
   */
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
    public Integer getVal() {
      return val;
    }

    /** @return txt テキスト */
    public String getTxt() {
      return txt;
    }

  }

  /** SQLリスト保持用変数 */
  ArrayList<String> sqlList = new ArrayList<>();
  /** SQLのパラメータリスト保持用変数 */
  ArrayList<ArrayList<String>> prmList = new ArrayList<>();
  /** SQLログ用のラベルリスト保持用変数 */
  ArrayList<String> lblList = new ArrayList<>();


  /** 商品マスタ列数 */
  int mstshn_col_num = MSTSHNLayout.values().length;

  /** ジャーナル更新のKEY保持用変数 */
  String jnlshn_seq = ""; //
  /** ジャーナル更新のテーブル区分保持用変数 */
  String jnlshn_tablekbn = ""; //
  /** ジャーナル更新の処理区分保持用変数 */
  String jnlshn_trankbn = "";
  /** メッセージ出力用商品・販売コード */
  String msgShnCd = "";
  String msgUriCd = "";

  // /** ジャーナル_商品マスタ特殊情報保持用 */
  // String[] jnlshn_add_data = new String[JNLSHNLayout.values().length];

  /** CSV取込トラン特殊情報保持用 */
  String csvshn_seq = "";
  String[] csvshn_add_data = new String[CSVSHNLayout.values().length];

  /** 仕入グループ商品マスタ,売価コントロールマスタ,ソースコード管理マスタ登録時の商品コード */
  static String SHCD = "";

  /**
   * 更新処理実行
   *
   * @return
   *
   * @throws Exception
   */
  private JSONObject updateData(HashMap<String, String> map, User userInfo, String sysdate, JSONObject objset) throws Exception {
    map.get("SEL_SHNCD");
    String szShncd = map.get("SHNCD"); // 入力商品コード
    String szSeq = map.get("SEQ"); // CSVエラー.SEQ
    String szInputno = map.get("INPUTNO"); // CSVエラー.入力番号
    String szCsvUpdkbn = map.get("CSV_UPDKBN"); // CSVエラー.CSV登録区分
    String szYoyakudt = map.get("YOYAKUDT"); // CSVエラー用.マスタ変更予定日
    map.get("TENBAIKADT");
    String sendBtnid = map.get("SENDBTNID"); // 呼出しボタン
    JSONArray dataArray = objset.optJSONArray("DATA"); // 対象情報（主要な更新情報）
    JSONArray dataArrayAdd = objset.optJSONArray("DATA_ADD"); // 対象情報（MD03111701:予約同一項目変更用の追加データ）
    JSONArray dataArraySRCCD = objset.optJSONArray("DATA_SRCCD"); // ソースコード
    JSONArray dataArrayTENGP4 = objset.optJSONArray("DATA_TENGP4"); // 店別異部門
    JSONArray dataArrayTENGP3 = objset.optJSONArray("DATA_TENGP3"); // 品揃えグループ
    JSONArray dataArrayTENGP2 = objset.optJSONArray("DATA_TENGP2"); // 売価コントロール
    JSONArray dataArrayTENGP1 = objset.optJSONArray("DATA_TENGP1"); // 仕入グループ
    JSONArray dataArrayTENKABUTSU = objset.optJSONArray("DATA_TENKABUTSU"); // 添加物
    JSONArray dataArrayGROUP = objset.optJSONArray("DATA_GROUP"); // グループ分類
    JSONArray dataArrayAHS = objset.optJSONArray("DATA_AHS"); // 自動発注データ

    JSONObject option = new JSONObject();
    JSONArray msg = new JSONArray();

    // パラメータ確認
    // 必須チェック
    if (sendBtnid == null || dataArray.isEmpty() || dataArray.size() != 1 || dataArray.getJSONObject(0).isEmpty()) {
      option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
      return option;
    }

    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー
    String szYoyaku = "0"; // 予約件数


    // SQLパターン
    // ①正 .新規 → 正 ：Insert処理、ジャ：Insert処理
    // ②正 .変更 → 正 ：Update処理、予12：条件付Update処理※1、ジャ：Insert処理
    // ※1.予約商品があり（商品コードが同じ）、かつ同一項目の値が同じ場合は商品－正の値によって更新登録
    // ③正 .削除 → 正 ：Update処理、ジャ：Insert処理 ※予1,2がある場合削除不可
    // ④予1.新規 → 予1：Insert処理、ジャ：Insert処理
    // ⑤予1.変更 → 予1：Update処理、予2 ：条件付Update処理※2、ジャ：Insert処理
    // ※2.予約商品があり（商品コードが同じ）、かつ同一項目の値が同じ場合は商品－正の値によって更新登録
    // ⑥予1.削除 → 予1：Update処理、ジャ：Insert処理 ※予2がある場合取消不可
    // ⑦予2.新規 → 予2：insert処理、ジャ：Insert処理
    // ⑧予2.変更 → 予2：Update処理、ジャ：Insert処理
    // ⑨予2.削除 → 予2：Update処理、ジャ：Insert処理

    // ①正 .新規
    boolean isNew = DefineReport.Button.NEW.getObj().equals(sendBtnid) || DefineReport.Button.COPY.getObj().equals(sendBtnid) || DefineReport.Button.SEL_COPY.getObj().equals(sendBtnid);
    // ②正 .変更
    boolean isChange = DefineReport.Button.SEL_CHANGE.getObj().equals(sendBtnid) || DefineReport.Button.SEARCH.getObj().equals(sendBtnid) || DefineReport.Button.SEI.getObj().equals(sendBtnid);
    // 予約1
    boolean isYoyaku1 = DefineReport.Button.YOYAKU1.getObj().equals(sendBtnid);
    // 予約2
    boolean isYoyaku2 = DefineReport.Button.YOYAKU2.getObj().equals(sendBtnid);
    // EX.CSVエラー修正
    boolean isCsverr = DefineReport.Button.ERR_CHANGE.getObj().equals(sendBtnid);
    if (isCsverr) {
      isNew = DefineReport.ValFileUpdkbn.NEW.getVal().equals(szCsvUpdkbn);
      isChange = DefineReport.ValFileUpdkbn.UPD.getVal().equals(szCsvUpdkbn);
      isYoyaku1 = DefineReport.ValFileUpdkbn.YYK.getVal().equals(szCsvUpdkbn);
    }

    // 基本登録情報
    JSONObject data = dataArray.getJSONObject(0);

    // 予約情報再取得
    JSONArray yArray = new JSONArray();
    if (!isNew) {
      JSONArray array = getYoyakuJSONArray(map);
      szYoyaku = Integer.toString(array.size());
      yArray.addAll(array);
    }
    // ④予1.新規
    boolean isNewY1 = isYoyaku1 && NumberUtils.toInt(szYoyaku, 0) == 0;
    // ⑤予1.変更
    boolean isChangeY1 = isYoyaku1 && NumberUtils.toInt(szYoyaku, 0) > 0;
    // ⑦予2.新規
    boolean isNewY2 = isYoyaku2 && NumberUtils.toInt(szYoyaku, 0) == 1;
    // ⑧予2.変更
    boolean isChangeY2 = isYoyaku2 && NumberUtils.toInt(szYoyaku, 0) > 1;

    // ①正 .新規
    if (isNew) {
      jnlshn_trankbn = InfTrankbn.INS.getVal();
      // 販売コード付番管理 ※検索チェック時に実行
      // String txt_uricd =
      // this.getNewURICD(data.optString(MSTSHNLayout.SHNCD.getId())).optString("VALUE");
      // data.element(MSTSHNLayout.URICD.getId(), txt_uricd);

      this.createSqlMSTSHN(userId, sendBtnid, data, TblType.SEI, SqlType.MRG);

      // --- 08.商品コード空き番 ※付番時にDB登録する
      // JSONObject result8 = this.createSqlSYSSHNCD_AKI(userId, sendBtnid, data, TblType.SEI,
      // SqlType.MRG);

      // --- 09.販売コード付番管理
      // JSONObject result9 = this.createSqlSYSURICD_FU(userId, sendBtnid, data, TblType.SEI,
      // SqlType.UPD);

      // --- 10.販売コード空き番※付番時にDB登録する
      // JSONObject result10 = this.createSqlSYSURICD_AKI(userId, sendBtnid, data, TblType.SEI,
      // SqlType.UPD);


      // ②正 .変更
    } else if (isChange) {
      jnlshn_trankbn = InfTrankbn.UPD.getVal();

      this.createSqlMSTSHN(userId, sendBtnid, data, TblType.SEI, SqlType.UPD);

      // MD03111701:予約同一項目更新(予1,予2)
      if (NumberUtils.toInt(szYoyaku, 0) > 0 && dataArrayAdd.size() > 0) {
        for (int i = 0; i < dataArrayAdd.size(); i++) {
          this.createSqlMSTSHN(userId, sendBtnid, dataArrayAdd.getJSONObject(i), TblType.YYK, SqlType.UPD);
        }
      }

      // ④予1.新規
    } else if (isNewY1) {
      jnlshn_trankbn = InfTrankbn.INS.getVal();
      this.createSqlMSTSHN(userId, sendBtnid, data, TblType.YYK, SqlType.MRG);

      // ⑤予1.変更
    } else if (isChangeY1) {
      jnlshn_trankbn = InfTrankbn.UPD.getVal();

      this.createSqlMSTSHN(userId, sendBtnid, data, TblType.YYK, SqlType.UPD);

      // MD03111701:予約同一項目更新(この場合予2のみ)
      if (NumberUtils.toInt(szYoyaku, 0) > 1 && dataArrayAdd.size() == 1) {
        this.createSqlMSTSHN(userId, sendBtnid, dataArrayAdd.getJSONObject(0), TblType.YYK, SqlType.UPD);
      }

      // ⑦予2.新規
    } else if (isNewY2) {
      jnlshn_trankbn = InfTrankbn.INS.getVal();

      this.createSqlMSTSHN(userId, sendBtnid, data, TblType.YYK, SqlType.MRG);

      // ⑧予2.変更
    } else if (DefineReport.Button.YOYAKU2.getObj().equals(sendBtnid)) {
      jnlshn_trankbn = InfTrankbn.UPD.getVal();

      this.createSqlMSTSHN(userId, sendBtnid, data, TblType.YYK, SqlType.UPD);

    }

    // ************ 子テーブル処理 ***********
    // 2004/03/05に、子テーブルの同一項目考慮はなくなった模様
    TblType baseTblType = isNew || isChange ? TblType.SEI : TblType.YYK;

    JSONArray dataArrayDel = new JSONArray();
    JSONArray dataArrayDelTENKABUTSU = new JSONArray();
    // 子テーブルは、一度削除してから追加なので、キー項目に注意
    if (isChangeY1 || isNewY2 || isChangeY2) {
      szYoyakudt = dataArray.optJSONObject(0).optString("F2");
      dataArrayDel.add(this.createJSONObject(new String[] {"F1", "F3"}, new String[] {szShncd, szYoyakudt}));
      dataArrayDelTENKABUTSU.add(this.createJSONObject(new String[] {"F1", "F4"}, new String[] {szShncd, szYoyakudt}));
    } else {
      dataArrayDel.add(this.createJSONObject(new String[] {"F1"}, new String[] {szShncd}));
      dataArrayDelTENKABUTSU.add(this.createJSONObject(new String[] {"F1"}, new String[] {szShncd}));
    }

    this.createSqlMSTSIRGPSHN(userId, sendBtnid, dataArrayDel, baseTblType, SqlType.DEL);
    if (dataArrayTENGP1.size() > 0) {
      this.createSqlMSTSIRGPSHN(userId, sendBtnid, dataArrayTENGP1, baseTblType, SqlType.INS);
    }

    this.createSqlMSTBAIKACTL(userId, sendBtnid, dataArrayDel, baseTblType, SqlType.DEL);
    if (dataArrayTENGP2.size() > 0) {
      this.createSqlMSTBAIKACTL(userId, sendBtnid, dataArrayTENGP2, baseTblType, SqlType.INS);
    }
    this.createSqlMSTSRCCD(userId, sendBtnid, dataArrayDel, baseTblType, SqlType.DEL);
    if (dataArraySRCCD.size() > 0) {
      this.createSqlMSTSRCCD(userId, sendBtnid, dataArraySRCCD, baseTblType, SqlType.INS);
    }
    this.createSqlMSTTENKABUTSU(userId, sendBtnid, dataArrayDelTENKABUTSU, baseTblType, SqlType.DEL);
    if (dataArrayTENKABUTSU.size() > 0) {
      this.createSqlMSTTENKABUTSU(userId, sendBtnid, dataArrayTENKABUTSU, baseTblType, SqlType.INS);
    }

    this.createSqlMSTSHINAGP(userId, sendBtnid, dataArrayDel, baseTblType, SqlType.DEL);
    if (dataArrayTENGP3.size() > 0) {
      this.createSqlMSTSHINAGP(userId, sendBtnid, dataArrayTENGP3, baseTblType, SqlType.INS);
    }

    this.createSqlMSTGRP(userId, sendBtnid, dataArrayDel, baseTblType, SqlType.DEL);
    if (dataArrayGROUP.size() > 0) {
      // グループ名登録処理+登録情報更新
      this.updateMSTGROUP(userId, sendBtnid, dataArrayGROUP);

      this.createSqlMSTGRP(userId, sendBtnid, dataArrayGROUP, baseTblType, SqlType.INS);
    }

    this.createSqlMSTAHS(userId, sendBtnid, dataArrayDel, baseTblType, SqlType.DEL);
    if (dataArrayAHS.size() > 0) {
      this.createSqlMSTAHS(userId, sendBtnid, dataArrayAHS, baseTblType, SqlType.INS);
    }

    this.createSqlMSTSHNTENBMN(userId, sendBtnid, dataArrayDel, baseTblType, SqlType.DEL);
    if (dataArrayTENGP4.size() > 0) {
      this.createSqlMSTSHNTENBMN(userId, sendBtnid, dataArrayTENGP4, baseTblType, SqlType.INS);
    }


    // ************ 関連テーブル処理 ***********
    // --- 07.メーカー
    String makercd = data.optString(MSTSHNLayout.MAKERCD.getId());
    if (StringUtils.isNotEmpty(makercd) && dataArraySRCCD.size() > 0) {
      // メーカーコード存在チェック
      if (!this.checkMstExist(DefineReport.InpText.MAKERCD.getObj(), makercd)) {
        String jancd = dataArraySRCCD.optJSONObject(0).optString(MSTSRCCDLayout.SRCCD.getId());
        JSONObject dataMAKER = this.createJSONObject(new String[] {"F1", "F4"}, new String[] {makercd, jancd});
        this.createSqlMSTMAKER(userId, sendBtnid, dataMAKER, TblType.SEI, SqlType.INS);
      }
    }
    this.createSqlSYSSHNCOUNT(userId, sendBtnid, TblType.SEI, SqlType.MRG);

    // ******** ジャーナル処理（正・予共通） ********
    // ジャーナル用の情報を取得
    jnlshn_seq = this.getJNLSHN_SEQ();
    jnlshn_tablekbn = isNew || isChange ? DefineReport.ValTablekbn.SEI.getVal() : DefineReport.ValTablekbn.YYK.getVal(); // 0：正、1：予約

    this.createSqlMSTSHN(userId, sendBtnid, data, TblType.JNL, SqlType.INS);

    // --- 16.ジャーナル_仕入グループ商品
    if (dataArrayTENGP1.size() > 0) {
      this.createSqlMSTSIRGPSHN(userId, sendBtnid, dataArrayTENGP1, TblType.JNL, SqlType.INS);
    }

    // --- 17.ジャーナル_売価コントロール
    if (dataArrayTENGP2.size() > 0) {
      this.createSqlMSTBAIKACTL(userId, sendBtnid, dataArrayTENGP2, TblType.JNL, SqlType.INS);
    }

    // --- 18.ジャーナル_ソースコード管理
    if (dataArraySRCCD.size() > 0) {
      this.createSqlMSTSRCCD(userId, sendBtnid, dataArraySRCCD, TblType.JNL, SqlType.INS);
    }

    // --- 19.ジャーナル_添加物
    if (dataArrayTENKABUTSU.size() > 0) {
      this.createSqlMSTTENKABUTSU(userId, sendBtnid, dataArrayTENKABUTSU, TblType.JNL, SqlType.INS);
    }

    // --- 20.ジャーナル_品揃グループ
    if (dataArrayTENGP3.size() > 0) {
      this.createSqlMSTSHINAGP(userId, sendBtnid, dataArrayTENGP3, TblType.JNL, SqlType.INS);
    }

    // --- 21.ジャーナル_グループ分類管理マスタ
    if (dataArrayGROUP.size() > 0) {
      this.createSqlMSTGRP(userId, sendBtnid, dataArrayGROUP, TblType.JNL, SqlType.INS);
    }

    // --- 22.ジャーナル_自動発注
    if (dataArrayAHS.size() > 0) {
      this.createSqlMSTAHS(userId, sendBtnid, dataArrayAHS, TblType.JNL, SqlType.INS);
    }

    // --- 23.ジャーナル_店別異部門
    if (dataArrayTENGP4.size() > 0) {
      this.createSqlMSTSHNTENBMN(userId, sendBtnid, dataArrayTENGP4, TblType.JNL, SqlType.INS);
    }

    // CSV取込ﾄﾗﾝ_商品ﾃｰﾌﾞﾙからﾚｺｰﾄﾞの論理削除
    if (isCsverr) {
      csvshn_add_data[CSVSHNLayout.SEQ.getNo() - 1] = szSeq;
      csvshn_add_data[CSVSHNLayout.INPUTNO.getNo() - 1] = szInputno;
      this.createSqlMSTSHN_DEL(userId, sendBtnid, data, TblType.CSV, SqlType.DEL);
    }


    // 排他チェック実行
    String targetTable = null;
    String targetWhere = "COALESCE(UPDKBN, 0) <> 1";
    ArrayList<String> targetParam = new ArrayList<>();
    // EX.CSVエラー修正
    if (isCsverr) {
      targetTable = "INAMS.CSVSHN";
      targetWhere += " and SEQ = ? and INPUTNO = ?";
      targetParam.add(szSeq);
      targetParam.add(szInputno);
    } else {
      // ①正 .新規/②正 .変更/④予1.新規/⑦予2.新規
      if (isNew || isChange || isNewY1 || isNewY2) {
        targetTable = "INAMS.MSTSHN";
        targetWhere += " and SHNCD = ?";
        targetParam.add(data.optString(MSTSHNLayout.SHNCD.getId()));

        // ⑤予1.変更/⑧予2.変更
      } else if (isChangeY1 || isChangeY2) {
        targetTable = "INAMS.MSTSHN_Y";
        targetWhere += " and SHNCD = ? and YOYAKUDT = ?";
        targetParam.add(data.optString(MSTSHNLayout.SHNCD.getId()));
        targetParam.add(data.optString(MSTSHNLayout.YOYAKUDT.getId()));
      }
    }
    if (!super.checkExclusion(targetTable, targetWhere, targetParam, data.optString("F127"))) {
      msg.add(MessageUtility.getDbMessageExclusion(FieldType.DEFAULT, new String[] {}));
      option.put(MsgKey.E.getKey(), msg);
      return option;
    }

    ArrayList<Integer> countList = new ArrayList<>();
    if (sqlList.size() > 0) {
      countList = super.executeSQLs(sqlList, prmList);
    }

    if (StringUtils.isEmpty(getMessage())) {
      int count = 0;
      for (int i = 0; i < countList.size(); i++) {
        count += countList.get(i);
        if (DefineReport.ID_DEBUG_MODE)
          System.out.println(MessageUtility.getMessage(Msg.S00006.getVal(), new String[] {lblList.get(i), Integer.toString(countList.get(i))}));
      }
      if (count == 0) {
        option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E10000.getVal()));
      } else {
        option.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00001.getVal()));
      }
    } else {
      option.put(MsgKey.E.getKey(), getMessage());
    }

    return option;
  }

  @Override
  public JSONObject createJSONObject(String[] keys, String[] values) {
    JSONObject obj = new JSONObject();
    for (int i = 0; i < keys.length; i++) {
      obj.put(keys[i], values[i]);
    }
    return obj;
  }

  /**
   * 削除処理実行
   *
   * @param sysdate
   * @return
   *
   * @throws Exception
   */
  private JSONObject deleteData(HashMap<String, String> map, User userInfo, String sysdate) throws Exception {
    map.get("SEL_SHNCD");
    String szShncd = map.get("SHNCD"); // 入力商品コード
    String szSeq = map.get("SEQ"); // CSVエラー.SEQ
    String szInputno = map.get("INPUTNO"); // CSVエラー.入力番号
    map.get("CSV_UPDKBN");
    String szYoyakudt = map.get("YOYAKUDT"); // CSVエラー用.マスタ変更予定日
    map.get("TENBAIKADT");
    String sendBtnid = map.get("SENDBTNID"); // 呼出しボタン

    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 対象情報

    JSONArray dataArraySRCCD = JSONArray.fromObject(map.get("DATA_SRCCD")); // ソースコード
    JSONArray dataArrayTENGP4 = JSONArray.fromObject(map.get("DATA_TENGP4")); // 店別異部門
    JSONArray dataArrayTENGP3 = JSONArray.fromObject(map.get("DATA_TENGP3")); // 品揃えグループ
    JSONArray dataArrayTENGP2 = JSONArray.fromObject(map.get("DATA_TENGP2")); // 売価コントロール
    JSONArray dataArrayTENGP1 = JSONArray.fromObject(map.get("DATA_TENGP1")); // 仕入グループ
    JSONArray dataArrayTENKABUTSU = JSONArray.fromObject(map.get("DATA_TENKABUTSU")); // 添加物
    JSONArray dataArrayGROUP = JSONArray.fromObject(map.get("DATA_GROUP")); // グループ分類
    JSONArray dataArrayAHS = JSONArray.fromObject(map.get("DATA_AHS")); // 自動発注データ


    JSONObject option = new JSONObject();
    JSONArray msg = new JSONArray();

    // パラメータ確認
    // 必須チェック
    if (dataArray.isEmpty() || dataArray.size() != 1 || dataArray.getJSONObject(0).isEmpty()) {
      option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
      return option;
    }

    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー
    boolean isChange = DefineReport.Button.SEL_CHANGE.getObj().equals(sendBtnid) || DefineReport.Button.SEARCH.getObj().equals(sendBtnid) || DefineReport.Button.SEI.getObj().equals(sendBtnid);
    boolean isYoyaku = DefineReport.Button.YOYAKU1.getObj().equals(sendBtnid) || DefineReport.Button.YOYAKU2.getObj().equals(sendBtnid);
    // EX.CSVエラー修正
    boolean isCsverr = DefineReport.Button.ERR_CHANGE.getObj().equals(sendBtnid);


    // 基本登録情報
    JSONObject data = dataArray.getJSONObject(0);
    // 変更処理（正）
    if (isChange) {
      this.createSqlMSTSHN_DEL(userId, sendBtnid, data, TblType.SEI, SqlType.DEL);

      // 変更処理（予）
    } else if (isYoyaku) {
      this.createSqlMSTSHN_DEL(userId, sendBtnid, data, TblType.YYK, SqlType.DEL);

      JSONArray dataArrayDel = new JSONArray();
      JSONArray dataArrayDelTENKABUTSU = new JSONArray();
      // 子テーブルは、一度削除してから追加なので、キー項目に注意
      szYoyakudt = data.optString("F2");
      dataArrayDel.add(this.createJSONObject(new String[] {"F1", "F3"}, new String[] {szShncd, szYoyakudt}));
      dataArrayDelTENKABUTSU.add(this.createJSONObject(new String[] {"F1", "F4"}, new String[] {szShncd, szYoyakudt}));

      this.createSqlMSTSIRGPSHN(userId, sendBtnid, dataArrayDel, TblType.YYK, SqlType.DEL);

      this.createSqlMSTBAIKACTL(userId, sendBtnid, dataArrayDel, TblType.YYK, SqlType.DEL);

      this.createSqlMSTSRCCD(userId, sendBtnid, dataArrayDel, TblType.YYK, SqlType.DEL);

      this.createSqlMSTTENKABUTSU(userId, sendBtnid, dataArrayDelTENKABUTSU, TblType.YYK, SqlType.DEL);

      this.createSqlMSTSHINAGP(userId, sendBtnid, dataArrayDel, TblType.YYK, SqlType.DEL);

    } else if (isCsverr) {
      csvshn_add_data[CSVSHNLayout.SEQ.getNo() - 1] = szSeq;
      csvshn_add_data[CSVSHNLayout.INPUTNO.getNo() - 1] = szInputno;
      this.createSqlMSTSHN_DEL(userId, sendBtnid, data, TblType.CSV, SqlType.DEL);
    }

    if (isChange || isYoyaku) {
      this.createSqlSYSSHNCOUNT(userId, sendBtnid, TblType.SEI, SqlType.MRG);


      // ******** ジャーナル処理（正・予共通） ********
      // ジャーナル用の情報を取得
      jnlshn_seq = this.getJNLSHN_SEQ();
      jnlshn_tablekbn = isYoyaku ? DefineReport.ValTablekbn.YYK.getVal() : DefineReport.ValTablekbn.SEI.getVal(); // 0：正、1：予約
      jnlshn_trankbn = InfTrankbn.DEL.getVal();

      this.createSqlMSTSHN(userId, sendBtnid, data, TblType.JNL, SqlType.INS);

      // --- 16.ジャーナル_仕入グループ商品
      if (dataArrayTENGP1.size() > 0) {
        this.createSqlMSTSIRGPSHN(userId, sendBtnid, dataArrayTENGP1, TblType.JNL, SqlType.INS);
      }

      // --- 17.ジャーナル_売価コントロール
      if (dataArrayTENGP2.size() > 0) {
        this.createSqlMSTBAIKACTL(userId, sendBtnid, dataArrayTENGP2, TblType.JNL, SqlType.INS);
      }

      // --- 18.ジャーナル_ソースコード管理
      if (dataArraySRCCD.size() > 0) {
        this.createSqlMSTSRCCD(userId, sendBtnid, dataArraySRCCD, TblType.JNL, SqlType.INS);
      }

      // --- 19.ジャーナル_添加物
      if (dataArrayTENKABUTSU.size() > 0) {
        this.createSqlMSTTENKABUTSU(userId, sendBtnid, dataArrayTENKABUTSU, TblType.JNL, SqlType.INS);
      }

      // --- 20.ジャーナル_品揃グループ
      if (dataArrayTENGP3.size() > 0) {
        this.createSqlMSTSHINAGP(userId, sendBtnid, dataArrayTENGP3, TblType.JNL, SqlType.INS);
      }

      // --- 21.ジャーナル_グループ分類管理マスタ
      if (dataArrayGROUP.size() > 0) {
        this.createSqlMSTGRP(userId, sendBtnid, dataArrayGROUP, TblType.JNL, SqlType.INS);
      }

      // --- 22.ジャーナル_自動発注
      if (dataArrayAHS.size() > 0) {
        this.createSqlMSTAHS(userId, sendBtnid, dataArrayAHS, TblType.JNL, SqlType.INS);
      }

      // --- 23.ジャーナル_店別異部門
      if (dataArrayTENGP4.size() > 0) {
        this.createSqlMSTSHNTENBMN(userId, sendBtnid, dataArrayTENGP4, TblType.JNL, SqlType.INS);
      }
    }

    // 排他チェック実行
    String targetTable = null;
    String targetWhere = "COALESCE(UPDKBN, 0) <> 1";
    ArrayList<String> targetParam = new ArrayList<>();
    // EX.CSVエラー修正
    if (isCsverr) {
      targetTable = "INAMS.CSVSHN";
      targetWhere += " and SEQ = ? and INPUTNO = ?";
      targetParam.add(szSeq);
      targetParam.add(szInputno);
    } else {
      // ②正 .変更
      if (isChange) {
        targetTable = "INAMS.MSTSHN";
        targetWhere += " and SHNCD = ?";
        targetParam.add(data.optString(MSTSHNLayout.SHNCD.getId()));

        // ⑤予1.変更/⑧予2.変更
      } else if (isYoyaku) {
        targetTable = "INAMS.MSTSHN_Y";
        targetWhere += " and SHNCD = ? and YOYAKUDT = ?";
        targetParam.add(data.optString(MSTSHNLayout.SHNCD.getId()));
        targetParam.add(data.optString(MSTSHNLayout.YOYAKUDT.getId()));
      }
    }
    if (!super.checkExclusion(targetTable, targetWhere, targetParam, data.optString("F127"))) {
      msg.add(MessageUtility.getDbMessageExclusion(FieldType.DEFAULT, new String[] {}));
      option.put(MsgKey.E.getKey(), msg);
      return option;
    }


    ArrayList<Integer> countList = new ArrayList<>();
    if (sqlList.size() > 0) {
      countList = super.executeSQLs(sqlList, prmList);
    }

    if (StringUtils.isEmpty(getMessage())) {
      int count = 0;
      for (int i = 0; i < countList.size(); i++) {
        count += countList.get(i);
        if (DefineReport.ID_DEBUG_MODE)
          System.out.println(MessageUtility.getMessage(Msg.S00006.getVal(), new String[] {lblList.get(i), Integer.toString(countList.get(i))}));
      }
      if (count == 0) {
        option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E10000.getVal()));
      } else {
        option.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00002.getVal()));
      }
    } else {
      option.put(MsgKey.E.getKey(), getMessage());
    }
    return option;
  }

  /**
   * チェック処理
   *
   * @throws Exception
   */
  public JSONObject check(HashMap<String, String> map, User userInfo, String sysdate) {
    // パラメータ確認
    String szCsvUpdkbn = map.get("CSV_UPDKBN"); // CSVエラー.CSV登録区分
    String sendBtnid = map.get("SENDBTNID"); // 呼出しボタン

    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 対象情報（主要な更新情報）
    JSONArray dataArrayAdd = JSONArray.fromObject(map.get("DATA_ADD")); // 対象情報（MD03111701:予約同一項目変更用の追加データ）

    JSONArray dataArraySRCCD = JSONArray.fromObject(map.get("DATA_SRCCD")); // ソースコード
    JSONArray dataArrayTENGP4 = JSONArray.fromObject(map.get("DATA_TENGP4")); // 店別異部門
    JSONArray dataArrayTENGP3 = JSONArray.fromObject(map.get("DATA_TENGP3")); // 品揃えグループ
    JSONArray dataArrayTENGP2 = JSONArray.fromObject(map.get("DATA_TENGP2")); // 売価コントロール
    JSONArray dataArrayTENGP1 = JSONArray.fromObject(map.get("DATA_TENGP1")); // 仕入グループ
    JSONArray dataArrayTENKABUTSU = JSONArray.fromObject(map.get("DATA_TENKABUTSU")); // 添加物
    JSONArray dataArrayGROUP = JSONArray.fromObject(map.get("DATA_GROUP")); // グループ分類
    JSONArray dataArrayAHS = JSONArray.fromObject(map.get("DATA_AHS")); // 自動発注データ

    JSONArray dataArrayOther = JSONArray.fromObject(map.get("DATA_OTHER")); // 対象情報（補足更新情報）
    JSONObject dataOther = dataArrayOther.optJSONObject(0);


    // 正 .新規
    boolean isNew = DefineReport.Button.NEW.getObj().equals(sendBtnid) || DefineReport.Button.COPY.getObj().equals(sendBtnid) || DefineReport.Button.SEL_COPY.getObj().equals(sendBtnid);
    // 正 .変更
    boolean isChange = DefineReport.Button.SEL_CHANGE.getObj().equals(sendBtnid) || DefineReport.Button.SEARCH.getObj().equals(sendBtnid) || DefineReport.Button.SEI.getObj().equals(sendBtnid);
    // 予約1
    boolean isYoyaku1 = DefineReport.Button.YOYAKU1.getObj().equals(sendBtnid);
    // 予約2
    boolean isYoyaku2 = DefineReport.Button.YOYAKU2.getObj().equals(sendBtnid);
    // EX.CSVエラー修正
    boolean isCsverr = DefineReport.Button.ERR_CHANGE.getObj().equals(sendBtnid);
    if (isCsverr) {
      isNew = DefineReport.ValFileUpdkbn.NEW.getVal().equals(szCsvUpdkbn);
      isChange = DefineReport.ValFileUpdkbn.UPD.getVal().equals(szCsvUpdkbn);
      isYoyaku1 = DefineReport.ValFileUpdkbn.YYK.getVal().equals(szCsvUpdkbn);
    }

    MessageUtility mu = new MessageUtility();

    JSONArray msgList = this.checkData(isNew, isChange, isYoyaku1, isYoyaku2, isCsverr, false, map, userInfo, sysdate, mu, dataArray, dataArrayAdd, dataArraySRCCD, dataArrayTENGP4, dataArrayTENGP3,
        dataArrayTENGP2, dataArrayTENGP1, dataArrayTENKABUTSU, dataArrayGROUP, dataArrayAHS, dataOther);

    JSONArray msgArray = new JSONArray();
    // MessageBoxを出す関係上、1件のみ表示
    if (msgList.size() > 0) {
      msgArray.add(msgList.get(0));
    }

    JSONObject objset = new JSONObject();
    objset.put("MSG", msgArray);
    objset.put("DATA", dataArray); // 対象情報（主要な更新情報）
    objset.put("DATA_ADD", dataArrayAdd); // 対象情報（MD03111701:予約同一項目変更用の追加データ）
    objset.put("DATA_SRCCD", dataArraySRCCD); // ソースコード
    objset.put("DATA_TENGP3", dataArrayTENGP3); // 品揃えグループ
    objset.put("DATA_TENGP2", dataArrayTENGP2); // 売価コントロール
    objset.put("DATA_TENGP1", dataArrayTENGP1); // 仕入グループ
    objset.put("DATA_TENKABUTSU", dataArrayTENKABUTSU); // 添加物
    objset.put("DATA_GROUP", dataArrayGROUP); // グループ名
    objset.put("DATA_AHS", dataArrayAHS); // 自動発注
    objset.put("DATA_TENGP4", dataArrayTENGP4); // 店別異部門
    objset.put("DATA_OTHER", dataOther);

    return objset;
  }

  /**
   * チェック処理(削除時)
   *
   * @throws Exception
   */
  public JSONArray checkDel(HashMap<String, String> map, User userInfo, String sysdate) {
    map.get("SHNCD");
    String sendBtnid = map.get("SENDBTNID"); // 呼出しボタン

    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 対象情報（主要な更新情報）
    JSONArray dataArraySRCCD = JSONArray.fromObject(map.get("DATA_SRCCD")); // ソースコード
    JSONArray.fromObject(map.get("DATA_TENGP4"));
    JSONArray dataArrayTENGP3 = JSONArray.fromObject(map.get("DATA_TENGP3")); // 品揃えグループ
    JSONArray dataArrayTENGP2 = JSONArray.fromObject(map.get("DATA_TENGP2")); // 売価コントロール
    JSONArray dataArrayTENGP1 = JSONArray.fromObject(map.get("DATA_TENGP1")); // 仕入グループ
    JSONArray dataArrayTENKABUTSU = JSONArray.fromObject(map.get("DATA_TENKABUTSU")); // 添加物
    JSONArray.fromObject(map.get("DATA_GROUP"));
    JSONArray.fromObject(map.get("DATA_AHS"));

    // 正 .新規
    boolean isNew = DefineReport.Button.NEW.getObj().equals(sendBtnid) || DefineReport.Button.COPY.getObj().equals(sendBtnid) || DefineReport.Button.SEL_COPY.getObj().equals(sendBtnid);
    // 正 .変更
    boolean isChange = DefineReport.Button.SEL_CHANGE.getObj().equals(sendBtnid) || DefineReport.Button.SEARCH.getObj().equals(sendBtnid) || DefineReport.Button.SEI.getObj().equals(sendBtnid);
    // 予約1
    boolean isYoyaku1 = DefineReport.Button.YOYAKU1.getObj().equals(sendBtnid);
    // 予約2
    boolean isYoyaku2 = DefineReport.Button.YOYAKU2.getObj().equals(sendBtnid);
    // EX.CSVエラー修正
    boolean isCsverr = DefineReport.Button.ERR_CHANGE.getObj().equals(sendBtnid);

    MessageUtility mu = new MessageUtility();

    List<JSONObject> msgList = new ArrayList<>();
    // CSV情報を削除する場合は無条件チェックなし
    if (!isCsverr) {
      msgList = this.checkDataDel(isNew, isChange, isYoyaku1, isYoyaku2, false, map, userInfo, sysdate, mu, dataArray, dataArraySRCCD, dataArrayTENGP3, dataArrayTENGP2, dataArrayTENGP1,
          dataArrayTENKABUTSU);
    }

    JSONArray msgArray = new JSONArray();
    // MessageBoxを出す関係上、1件のみ表示
    if (msgList.size() > 0) {
      msgArray.add(msgList.get(0));
    }
    return msgArray;
  }

  public JSONArray checkData(boolean isNew, boolean isChange, boolean isYoyaku1, boolean isYoyaku2, boolean isCsvErr, boolean isCsvUpload, HashMap<String, String> map, User userInfo, String sysdate1,
      MessageUtility mu, JSONArray dataArray, // 対象情報（主要な更新情報）
      JSONArray dataArrayAdd, // 対象情報（追加更新情報）
      JSONArray dataArraySRCCD, // ソースコード
      JSONArray dataArrayTENGP4, // 店部門別異部門
      JSONArray dataArrayTENGP3, // 品揃えグループ
      JSONArray dataArrayTENGP2, // 売価コントロール
      JSONArray dataArrayTENGP1, // 仕入グループ
      JSONArray dataArrayTENKABUTSU, // 添加物
      JSONArray dataArrayGROUP, // グループ分類
      JSONArray dataArrayAHS, // 自動発注データ
      JSONObject dataOther // その他情報
  ) {

    JSONArray msg = new JSONArray();

    // Web商談対応
    String szStcdKenmei = map.containsKey("STATUS") && !StringUtils.isEmpty(map.get("STATUS")) ? map.get("STATUS") : ""; // 状態_提案件名
    String szStcdShikakari = map.containsKey("STATE_S") && !StringUtils.isEmpty(map.get("STATE_S")) ? map.get("STATE_S") : ""; // 状態_仕掛商品

    // DB最新情報再取得
    String szYoyaku = "0";
    JSONArray yArray = new JSONArray();
    if (!isNew) {
      JSONArray array = getYoyakuJSONArray(map);
      szYoyaku = Integer.toString(array.size());
      yArray.addAll(array);
    }
    JSONObject seiJsonObject = new JSONObject();
    if (isChange) {
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
    boolean isChangeY2 = isYoyaku2 && NumberUtils.toInt(szYoyaku, 0) > 1;


    JSONObject data = dataArray.optJSONObject(0);

    String txt_shncd = StringUtils.strip(data.optString(MSTSHNLayout.SHNCD.getId()));
    String txt_yoyakudt = data.optString(MSTSHNLayout.YOYAKUDT.getId());
    String txt_tenbaikadt = data.optString(MSTSHNLayout.TENBAIKADT.getId());
    String txt_bmncd = data.optString(MSTSHNLayout.BMNCD.getId());

    String login_dt = sysdate1; // 処理日付
    String sysdate = login_dt; // 比較用処理日付

    // 基本データチェック:入力値がテーブル定義と矛盾してないか確認
    String[] notTaretCol = new String[] {"UPDDT", "ADDDT"}; // チェック除外項目
    String[] notMstChkTaretCol = new String[] {"UP_YORYOSU", "UP_TYORYOSU", "UP_TANIKBN", "ZEIRTKBN", "ZEIRTKBN_OLD", "MAKERCD"}; // チェック除外項目
    // 1.商品マスタ
    RefTable errTbl = RefTable.MSTSHN;
    for (MSTSHNLayout colinf : MSTSHNLayout.values()) {
      if (ArrayUtils.contains(notTaretCol, colinf.getCol())) {
        continue;
      } // チェック除外項目
      String val = StringUtils.trim(data.optString(colinf.getId()));
      if (StringUtils.isNotEmpty(val)) {
        DataType dtype = null;
        int[] digit = null;
        try {
          DefineReport.InpText inpsetting = DefineReport.InpText.valueOf(colinf.getCol());
          dtype = inpsetting.getType();
          digit = new int[] {inpsetting.getDigit1(), inpsetting.getDigit2()};

          if (StringUtils.equals(MSTSHNLayout.SANCHIKN.getCol(), colinf.getCol())) {
            // POPサイズ項目
            // DefineReportの設定を優先しない。
            dtype = DefineReport.DataType.ZEN;
            digit = colinf.getDigit();

          }

        } catch (IllegalArgumentException e) {
          dtype = colinf.getDataType();
          digit = colinf.getDigit();
        }
        // ①データ型による文字種チェック
        if (!InputChecker.checkDataType(dtype, val)) {
          JSONObject o = mu.getDbMessageObjDataTypeErr(dtype, new String[] {});
          this.setCsvshnErrinfo(o, errTbl, colinf, val);
          msg.add(o);
          if (!colinf.isText()) {
            data.element(colinf.getId(), ""); // CSVトラン用に空
          }
        }
        // ②データ桁チェック
        if (!InputChecker.checkDataLen(dtype, val, digit)) {
          JSONObject o = mu.getDbMessageObjLen(dtype, new String[] {});
          this.setCsvshnErrinfo(o, errTbl, colinf, val);
          msg.add(o);
          data.element(colinf.getId(), ""); // CSVトラン用に空
        }

        // チェック除外項目
        if (isCsvUpload && ArrayUtils.contains(notMstChkTaretCol, colinf.getCol())) {
          try {
            if (!StringUtils.isEmpty(val) && Integer.valueOf(val) == 0) {
              continue;
            }
          } catch (NumberFormatException e) {
            continue;
          }
        }

        // ③名称マスタ存在チェックを行う
        String szmeisyoKbn = "";
        if (!InputChecker.checkKbnExist(colinf.getCol(), val, szmeisyoKbn)) {
          JSONObject o = mu.getDbMessageObj("E30027", new String[] {"名称コード"});
          this.setCsvshnErrinfo(o, errTbl, colinf, val);
          msg.add(o);
          data.element(colinf.getId(), ""); // CSVトラン用に空
        }
      }
    }
    // 2.ソースマスタ
    errTbl = RefTable.MSTSRCCD;
    for (int i = 0; i < dataArraySRCCD.size(); i++) {
      JSONObject jo = dataArraySRCCD.optJSONObject(i);
      for (MSTSRCCDLayout colinf : MSTSRCCDLayout.values()) {
        if (ArrayUtils.contains(notTaretCol, colinf.getCol())) {
          continue;
        } // チェック除外項目
        String val = StringUtils.trim(jo.optString(colinf.getId()));
        if (StringUtils.isNotEmpty(val)) {
          DataType dtype = null;
          int[] digit = null;
          try {
            DefineReport.InpText inpsetting = DefineReport.InpText.valueOf(colinf.getCol());
            dtype = inpsetting.getType();
            digit = new int[] {inpsetting.getDigit1(), inpsetting.getDigit2()};
          } catch (IllegalArgumentException e) {
            dtype = colinf.getDataType();
            digit = colinf.getDigit();
          }
          System.out.println(txt_bmncd);
          // ①データ型による文字種チェック
          if (!InputChecker.checkDataType(dtype, val)) {
            JSONObject o = mu.getDbMessageObjDataTypeErr(dtype, new String[] {});
            this.setCsvshnErrinfo(o, errTbl, colinf, val);
            msg.add(o);
            if (!colinf.isText()) {
              jo.element(colinf.getId(), ""); // CSVトラン用に空
            }
          }
          // ②データ桁チェック
          if (!InputChecker.checkDataLen(dtype, val, digit)) {
            JSONObject o = mu.getDbMessageObjLen(dtype, new String[] {});
            this.setCsvshnErrinfo(o, errTbl, colinf, val);
            msg.add(o);
            jo.element(colinf.getId(), ""); // CSVトラン用に空
          }
        }
      }
    }
    // 3.仕入グループ商品マスタ
    errTbl = RefTable.MSTSIRGPSHN;
    for (int i = 0; i < dataArrayTENGP1.size(); i++) {
      JSONObject jo = dataArrayTENGP1.optJSONObject(i);
      for (MSTSIRGPSHNLayout colinf : MSTSIRGPSHNLayout.values()) {
        if (ArrayUtils.contains(notTaretCol, colinf.getCol())) {
          continue;
        } // チェック除外項目
        String val = StringUtils.trim(jo.optString(colinf.getId()));
        if (StringUtils.isNotEmpty(val)) {
          DataType dtype = null;
          int[] digit = null;
          try {
            DefineReport.InpText inpsetting = DefineReport.InpText.valueOf(colinf.getCol());
            dtype = inpsetting.getType();
            digit = new int[] {inpsetting.getDigit1(), inpsetting.getDigit2()};
          } catch (IllegalArgumentException e) {
            dtype = colinf.getDataType();
            digit = colinf.getDigit();
          }
          // ①データ型による文字種チェック
          if (!InputChecker.checkDataType(dtype, val)) {
            JSONObject o = mu.getDbMessageObjDataTypeErr(dtype, new String[] {});
            this.setCsvshnErrinfo(o, errTbl, colinf, val);
            msg.add(o);
            if (!colinf.isText()) {
              jo.element(colinf.getId(), ""); // CSVトラン用に空
            }
          }
          // ②データ桁チェック
          if (!InputChecker.checkDataLen(dtype, val, digit)) {
            JSONObject o = mu.getDbMessageObjLen(dtype, new String[] {});
            this.setCsvshnErrinfo(o, errTbl, colinf, val);
            msg.add(o);
            jo.element(colinf.getId(), ""); // CSVトラン用に空
          }
        }
      }
    }
    // 4.売価コントロールマスタ
    errTbl = RefTable.MSTBAIKACTL;
    for (int i = 0; i < dataArrayTENGP2.size(); i++) {
      JSONObject jo = dataArrayTENGP2.optJSONObject(i);
      for (MSTBAIKACTLLayout colinf : MSTBAIKACTLLayout.values()) {
        if (ArrayUtils.contains(notTaretCol, colinf.getCol())) {
          continue;
        } // チェック除外項目
        String val = StringUtils.trim(jo.optString(colinf.getId()));
        if (StringUtils.isNotEmpty(val)) {
          DataType dtype = null;
          int[] digit = null;
          try {
            DefineReport.InpText inpsetting = DefineReport.InpText.valueOf(colinf.getCol());
            dtype = inpsetting.getType();
            digit = new int[] {inpsetting.getDigit1(), inpsetting.getDigit2()};
          } catch (IllegalArgumentException e) {
            dtype = colinf.getDataType();
            digit = colinf.getDigit();
          }
          // ①データ型による文字種チェック
          if (!InputChecker.checkDataType(dtype, val)) {
            JSONObject o = mu.getDbMessageObjDataTypeErr(dtype, new String[] {});
            this.setCsvshnErrinfo(o, errTbl, colinf, val);
            msg.add(o);
            if (!colinf.isText()) {
              jo.element(colinf.getId(), ""); // CSVトラン用に空
            }
          }
          // ②データ桁チェック
          if (!InputChecker.checkDataLen(dtype, val, digit)) {
            JSONObject o = mu.getDbMessageObjLen(dtype, new String[] {});
            this.setCsvshnErrinfo(o, errTbl, colinf, val);
            msg.add(o);
            jo.element(colinf.getId(), ""); // CSVトラン用に空
          }
        }
      }
    }
    // 5.品揃グループマスタ
    errTbl = RefTable.MSTSHINAGP;
    for (int i = 0; i < dataArrayTENGP3.size(); i++) {
      JSONObject jo = dataArrayTENGP3.optJSONObject(i);
      for (MSTSHINAGPLayout colinf : MSTSHINAGPLayout.values()) {
        if (ArrayUtils.contains(notTaretCol, colinf.getCol())) {
          continue;
        } // チェック除外項目
        String val = StringUtils.trim(jo.optString(colinf.getId()));
        if (StringUtils.isNotEmpty(val)) {
          DataType dtype = null;
          int[] digit = null;
          try {
            DefineReport.InpText inpsetting = DefineReport.InpText.valueOf(colinf.getCol());
            dtype = inpsetting.getType();
            digit = new int[] {inpsetting.getDigit1(), inpsetting.getDigit2()};
          } catch (IllegalArgumentException e) {
            dtype = colinf.getDataType();
            digit = colinf.getDigit();
          }
          // ①データ型による文字種チェック
          if (!InputChecker.checkDataType(dtype, val)) {
            JSONObject o = mu.getDbMessageObjDataTypeErr(dtype, new String[] {});
            this.setCsvshnErrinfo(o, errTbl, colinf, val);
            msg.add(o);
            if (!colinf.isText()) {
              jo.element(colinf.getId(), ""); // CSVトラン用に空
            }
          }
          // ②データ桁チェック
          if (!InputChecker.checkDataLen(dtype, val, digit)) {
            JSONObject o = mu.getDbMessageObjLen(dtype, new String[] {});
            this.setCsvshnErrinfo(o, errTbl, colinf, val);
            msg.add(o);
            jo.element(colinf.getId(), ""); // CSVトラン用に空
          }
        }
      }
    }
    // 6.添加物
    errTbl = RefTable.MSTTENKABUTSU;
    for (int i = 0; i < dataArrayTENKABUTSU.size(); i++) {
      JSONObject jo = dataArrayTENKABUTSU.optJSONObject(i);
      for (MSTTENKABUTSULayout colinf : MSTTENKABUTSULayout.values()) {
        if (ArrayUtils.contains(notTaretCol, colinf.getCol())) {
          continue;
        } // チェック除外項目
        String val = StringUtils.trim(jo.optString(colinf.getId()));
        if (StringUtils.isNotEmpty(val)) {
          DataType dtype = null;
          int[] digit = null;
          try {
            DefineReport.InpText inpsetting = DefineReport.InpText.valueOf(colinf.getCol());
            dtype = inpsetting.getType();
            digit = new int[] {inpsetting.getDigit1(), inpsetting.getDigit2()};
          } catch (IllegalArgumentException e) {
            dtype = colinf.getDataType();
            digit = colinf.getDigit();
          }
          // ①データ型による文字種チェック
          if (!InputChecker.checkDataType(dtype, val)) {
            JSONObject o = mu.getDbMessageObjDataTypeErr(dtype, new String[] {});
            this.setCsvshnErrinfo(o, errTbl, colinf, val);
            msg.add(o);
            if (!colinf.isText()) {
              jo.element(colinf.getId(), ""); // CSVトラン用に空
            }
          }
          // ②データ桁チェック
          if (!InputChecker.checkDataLen(dtype, val, digit)) {
            JSONObject o = mu.getDbMessageObjLen(dtype, new String[] {});
            this.setCsvshnErrinfo(o, errTbl, colinf, val);
            msg.add(o);
            jo.element(colinf.getId(), ""); // CSVトラン用に空
          }
        }
      }
    }
    // 7.グループ名
    errTbl = RefTable.MSTGRP;
    for (int i = 0; i < dataArrayGROUP.size(); i++) {
      JSONObject jo = dataArrayGROUP.optJSONObject(i);
      for (MSTGRPLayout colinf : MSTGRPLayout.values()) {
        if (ArrayUtils.contains(notTaretCol, colinf.getCol())) {
          continue;
        } // チェック除外項目
        String val = StringUtils.trim(jo.optString(colinf.getId()));
        if (StringUtils.isNotEmpty(val)) {
          DataType dtype = null;
          int[] digit = null;
          try {
            DefineReport.InpText inpsetting = DefineReport.InpText.valueOf(colinf.getCol());
            dtype = inpsetting.getType();
            digit = new int[] {inpsetting.getDigit1(), inpsetting.getDigit2()};
          } catch (IllegalArgumentException e) {
            dtype = colinf.getDataType();
            digit = colinf.getDigit();
          }
          // ①データ型による文字種チェック
          if (!InputChecker.checkDataType(dtype, val)) {
            JSONObject o = mu.getDbMessageObjDataTypeErr(dtype, new String[] {});
            this.setCsvshnErrinfo(o, errTbl, colinf, val);
            msg.add(o);
            if (!colinf.isText()) {
              jo.element(colinf.getId(), ""); // CSVトラン用に空
            }
          }
          // ②データ桁チェック
          if (!InputChecker.checkDataLen(dtype, val, digit)) {
            JSONObject o = mu.getDbMessageObjLen(dtype, new String[] {});
            this.setCsvshnErrinfo(o, errTbl, colinf, val);
            msg.add(o);
            jo.element(colinf.getId(), ""); // CSVトラン用に空
          }
        }
      }
    }
    // 8.自動発注区分
    errTbl = RefTable.MSTAHS;
    for (int i = 0; i < dataArrayAHS.size(); i++) {
      JSONObject jo = dataArrayAHS.optJSONObject(i);
      for (MSTAHSLayout colinf : MSTAHSLayout.values()) {
        if (ArrayUtils.contains(notTaretCol, colinf.getCol())) {
          continue;
        } // チェック除外項目
        String val = StringUtils.trim(jo.optString(colinf.getId()));
        if (StringUtils.isNotEmpty(val)) {
          DataType dtype = null;
          int[] digit = null;
          try {
            DefineReport.InpText inpsetting = DefineReport.InpText.valueOf(colinf.getCol());
            dtype = inpsetting.getType();
            digit = new int[] {inpsetting.getDigit1(), inpsetting.getDigit2()};
          } catch (IllegalArgumentException e) {
            dtype = colinf.getDataType();
            digit = colinf.getDigit();
          }
          // ①データ型による文字種チェック
          if (!InputChecker.checkDataType(dtype, val)) {
            JSONObject o = mu.getDbMessageObjDataTypeErr(dtype, new String[] {});
            this.setCsvshnErrinfo(o, errTbl, colinf, val);
            msg.add(o);
            if (!colinf.isText()) {
              jo.element(colinf.getId(), ""); // CSVトラン用に空
            }
          }
          // ②データ桁チェック
          if (!InputChecker.checkDataLen(dtype, val, digit)) {
            JSONObject o = mu.getDbMessageObjLen(dtype, new String[] {});
            this.setCsvshnErrinfo(o, errTbl, colinf, val);
            msg.add(o);
            jo.element(colinf.getId(), ""); // CSVトラン用に空
          }
        }
      }
    }
    // 9.店別異部門
    errTbl = RefTable.MSTSHNTENBMN;
    for (int i = 0; i < dataArrayTENGP4.size(); i++) {
      JSONObject jo = dataArrayTENGP4.optJSONObject(i);
      for (MSTSHNTENBMNLayout colinf : MSTSHNTENBMNLayout.values()) {
        if (ArrayUtils.contains(notTaretCol, colinf.getCol())) {
          continue;
        } // チェック除外項目
        String val = StringUtils.trim(jo.optString(colinf.getId()));
        if (StringUtils.isNotEmpty(val)) {
          DataType dtype = null;
          int[] digit = null;
          try {
            DefineReport.InpText inpsetting = DefineReport.InpText.valueOf(colinf.getCol());
            dtype = inpsetting.getType();
            digit = new int[] {inpsetting.getDigit1(), inpsetting.getDigit2()};
          } catch (IllegalArgumentException e) {
            dtype = colinf.getDataType();
            digit = colinf.getDigit();
          }
          // ①データ型による文字種チェック
          if (!InputChecker.checkDataType(dtype, val)) {
            JSONObject o = mu.getDbMessageObjDataTypeErr(dtype, new String[] {});
            this.setCsvshnErrinfo(o, errTbl, colinf, val);
            msg.add(o);
            if (!colinf.isText()) {
              jo.element(colinf.getId(), ""); // CSVトラン用に空
            }
          }
          // ②データ桁チェック
          if (!InputChecker.checkDataLen(dtype, val, digit)) {
            JSONObject o = mu.getDbMessageObjLen(dtype, new String[] {});
            this.setCsvshnErrinfo(o, errTbl, colinf, val);
            msg.add(o);
            jo.element(colinf.getId(), ""); // CSVトラン用に空
          }
        }
      }
    }
    if (msg.size() > 0) {
      return msg;
    }

    // 新規(正) 1.1 必須入力項目チェックを行う。
    // 変更(正) 1.1 必須入力項目チェックを行う。
    errTbl = RefTable.MSTSHN;
    MSTSHNLayout[] targetCol = null;
    if (isNew) {
      targetCol = new MSTSHNLayout[] {MSTSHNLayout.SHNAN, MSTSHNLayout.PCARD_SHUKBN, MSTSHNLayout.PCARD_IROKBN, MSTSHNLayout.BMNCD, MSTSHNLayout.TEIKEIKBN, MSTSHNLayout.TEISHIKBN, MSTSHNLayout.SHNKBN,
          MSTSHNLayout.PCKBN, MSTSHNLayout.TEIKANKBN, MSTSHNLayout.KAKOKBN, MSTSHNLayout.TANAOROKBN, MSTSHNLayout.ZEIKBN, MSTSHNLayout.SIMEKAISU, MSTSHNLayout.BINKBN, MSTSHNLayout.RG_IDENFLG,
          MSTSHNLayout.HSPTN};
    } else if (isChange) {
      targetCol = new MSTSHNLayout[] {MSTSHNLayout.SHNCD, MSTSHNLayout.SHNAN, MSTSHNLayout.PCARD_SHUKBN, MSTSHNLayout.PCARD_IROKBN, MSTSHNLayout.BMNCD, MSTSHNLayout.TEIKEIKBN, MSTSHNLayout.TEISHIKBN,
          MSTSHNLayout.SHNKBN, MSTSHNLayout.PCKBN, MSTSHNLayout.TEIKANKBN, MSTSHNLayout.KAKOKBN, MSTSHNLayout.TANAOROKBN, MSTSHNLayout.ZEIKBN, MSTSHNLayout.SIMEKAISU, MSTSHNLayout.BINKBN,
          MSTSHNLayout.RG_IDENFLG, MSTSHNLayout.HSPTN};
    } else {
      targetCol = new MSTSHNLayout[] {MSTSHNLayout.SHNCD, MSTSHNLayout.SHNAN, MSTSHNLayout.PCARD_SHUKBN, MSTSHNLayout.PCARD_IROKBN, MSTSHNLayout.BMNCD, MSTSHNLayout.TEISHIKBN, MSTSHNLayout.SHNKBN,
          MSTSHNLayout.PCKBN, MSTSHNLayout.TEIKANKBN, MSTSHNLayout.KAKOKBN, MSTSHNLayout.TANAOROKBN, MSTSHNLayout.ZEIKBN, MSTSHNLayout.SIMEKAISU, MSTSHNLayout.BINKBN, MSTSHNLayout.RG_IDENFLG,
          MSTSHNLayout.HSPTN};
    }
    for (MSTSHNLayout colinf : targetCol) {
      if (StringUtils.isEmpty(data.optString(colinf.getId()))) {
        JSONObject o = mu.getDbMessageObj("E00001", new String[] {});
        this.setCsvshnErrinfo(o, errTbl, colinf, data.optString(colinf.getId()));
        msg.add(o);
        return msg;
      }
    }

    // リードタイムパターン必須入力チェック
    String txt_readtmptn = data.optString(MSTSHNLayout.READTMPTN.getId());

    List<Integer> bmnlist = Arrays.asList(1, 3, 7, 8, 12, 14, 42, 44, 46, 47, 54); // リードタイムパターンが省略可能な部門リスト
    if (StringUtils.isEmpty(txt_readtmptn)) {
      if (StringUtils.isNumeric(txt_bmncd)) {
        if (bmnlist.indexOf(Integer.parseInt(txt_bmncd)) == -1) {
          JSONObject o = mu.getDbMessageObj("E00001", new String[] {});
          this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.READTMPTN, txt_readtmptn);
          msg.add(o);
          return msg;
        }
      }
    }

    // // 0未入力扱い項目のチェック
    // if(isNew){
    // targetCol = new
    // MSTSHNLayout[]{MSTSHNLayout.BMNCD,MSTSHNLayout.DAICD,MSTSHNLayout.CHUCD,MSTSHNLayout.SHOCD};
    // }else if(isChange){
    // targetCol = new MSTSHNLayout[]{MSTSHNLayout.BMNCD};
    // }else{
    // targetCol = new MSTSHNLayout[]{MSTSHNLayout.BMNCD};
    // }
    // for(MSTSHNLayout colinf: targetCol){
    // if(StringUtils.equals(data.optString(colinf.getId()), "0")){
    // JSONObject o = mu.getDbMessageObj("E00001", new String[]{});
    // this.setCsvshnErrinfo(o, errTbl, colinf, data.optString(colinf.getId()));
    // msg.add(o);
    // return msg;
    // }
    // }


    // CSV修正(予約) 1.2 商品_予約ﾃｰﾌﾞﾙに同じ商品ｺｰﾄﾞで異なるﾏｽﾀ変更予定日、店売価実施日の組み合わせを持つﾚｺｰﾄﾞがあれば、ｴﾗｰ。
    errTbl = RefTable.CSVSHN;
    if (isCsvErr && isChangeY1) {
      String szCsvUpdkbn = map.get("CSV_UPDKBN"); // CSVエラー.CSV登録区分
      if (yArray.size() > 1) {
        JSONObject o = mu.getDbMessageObj("EX1010", new String[] {});
        this.setCsvshnErrinfo(o, errTbl, CSVSHNLayout.CSV_UPDKBN, szCsvUpdkbn);
        msg.add(o);
        return msg;
      }
      JSONObject y1o = yArray.optJSONObject(0);
      if (!(y1o.optString(MSTSHNLayout.YOYAKUDT.getId()).equals(txt_yoyakudt) && y1o.optString(MSTSHNLayout.TENBAIKADT.getId()).equals(txt_tenbaikadt))) {
        JSONObject o = mu.getDbMessageObj("EX1010", new String[] {});
        this.setCsvshnErrinfo(o, errTbl, CSVSHNLayout.CSV_UPDKBN, szCsvUpdkbn);
        msg.add(o);
        return msg;
      }
    }

    String txt_shncd_new = txt_shncd;
    if (!StringUtils.isEmpty(szStcdKenmei) && szStcdKenmei.equals("1")) {
      txt_shncd_new = new Reportx247Dao(JNDIname).getSHNCD_SEQ();
    }

    String txt_uricd_new = data.optString(MSTSHNLayout.URICD.getId());
    if ((isNew && StringUtils.isEmpty(szStcdKenmei)) || (!StringUtils.isEmpty(szStcdShikakari) && szStcdShikakari.equals("04"))) {
      String kbn143 = dataOther.optString(CSVSHNLayout.KETAKBN.getCol());
      // 新規(正) 1.2 添付資料（MD03100901）の商品コード付番規則によって、入力された商品コードを処理し、8桁の商品コード番号を取得する。
      // F1-商品コード：入力内容の桁数と桁指定項目の選択内容と合わない場合、エラー。
      // 画面上は付番済み前提
      if (szStcdShikakari.equals("04")) {
        kbn143 = "0";
        txt_shncd = "";
        txt_shncd_new = "";
      }
      if (StringUtils.isEmpty(dataOther.optString(MSTSHNLayout.SHNCD.getCol() + "_NEW"))) {
        if ((DefineReport.ValKbn143.VAL0.getVal().equals(kbn143) && txt_shncd.length() != 0) || (DefineReport.ValKbn143.VAL1.getVal().equals(kbn143) && txt_shncd.length() != 8)
            || (DefineReport.ValKbn143.VAL2.getVal().equals(kbn143) && txt_shncd.length() != 4)) {
          JSONObject o = mu.getDbMessageObj("E11154", new String[] {});
          this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.SHNCD, txt_shncd);
          msg.add(o);
          return msg;
        }
      }

      // コード整合性チェック：チェックデジット算出コード取得
      if (txt_shncd_new.length() == 8) {
        JSONObject resShn2 = NumberingUtility.calcCheckdigitSHNCD(userInfo, txt_shncd_new);
        if (StringUtils.isNotEmpty(resShn2.optString(MessageUtility.ID))) {
          JSONObject o = mu.getDbMessageObj(resShn2.optString(MessageUtility.ID), new String[] {});
          this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.SHNCD, txt_shncd_new);
          msg.add(o);
          return msg;
        }
      }

      // 添付資料（MD03100901）の商品コード付番機能
      // 画面上は付番済み前提
      if (isCsvUpload && StringUtils.isNotEmpty(txt_shncd_new)) {
        // CSVアップロード時には、画面新規登録時に設定していた項目が設定されない為、付番処理を行う前に設定を行う。
        dataOther.element(MSTSHNLayout.SHNCD.getCol() + "_NEW", txt_shncd_new);
      }

      if (StringUtils.isEmpty(dataOther.optString(MSTSHNLayout.SHNCD.getCol() + "_NEW")) || DefineReport.ValKbn143.VAL2.getVal().equals(kbn143)) {
        JSONObject result = NumberingUtility.execGetNewSHNCD(userInfo, txt_shncd_new, kbn143, txt_bmncd);
        if (StringUtils.isNotEmpty(result.optString(MessageUtility.ID))) {
          JSONObject o = mu.getDbMessageObj(result.optString(MessageUtility.ID), new String[] {});
          this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.SHNCD, txt_shncd_new);
          msg.add(o);
          return msg;
        }

        txt_shncd_new = result.optString("VALUE");
        // 取得商品コードを設定
        dataOther.element(MSTSHNLayout.SHNCD.getCol() + "_NEW", txt_shncd_new);
      } else {
        txt_shncd_new = dataOther.optString(MSTSHNLayout.SHNCD.getCol() + "_NEW");
        if (this.checkMstExist(DefineReport.InpText.SHNCD.getObj(), txt_shncd_new)) {
          JSONObject o = mu.getDbMessageObj("E20162", new String[] {});
          this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.SHNCD, txt_shncd_new);
          msg.add(o);
          return msg;
        }

        // 付番済商品コードを仮押さえ
        JSONObject resShn = NumberingUtility.execHoldNewSHNCD(userInfo, txt_shncd_new);
        if (StringUtils.isNotEmpty(resShn.optString(MessageUtility.ID))) {
          JSONObject o = mu.getDbMessageObj(resShn.optString(MessageUtility.ID), new String[] {});
          this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.SHNCD, txt_shncd_new);
          msg.add(o);
          return msg;
        }
      }
      // エラー時用取得商品コードを設定
      dataOther.element(MSTSHNLayout.SHNCD.getCol() + "_RENEW", txt_shncd_new);

      // 添付資料（MD03100902）の販売コード付番機能
      JSONObject resUri = NumberingUtility.execHoldNewURICD(userInfo, txt_shncd_new);
      if (StringUtils.isNotEmpty(resUri.optString(MessageUtility.ID))) {
        JSONObject o = mu.getDbMessageObj(resUri.optString(MessageUtility.ID), new String[] {});
        this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.URICD, txt_shncd_new);
        msg.add(o);
        return msg;
      }
      txt_uricd_new = resUri.optString("VALUE");
      // 取得コードを設定
      dataOther.element(MSTSHNLayout.URICD.getCol() + "_NEW", txt_uricd_new);
    } else {

      if (!isNew || StringUtils.isEmpty(szStcdKenmei)) {
        if (txt_shncd.length() != 8) {
          JSONObject o = mu.getDbMessageObj("E11089", new String[] {});
          this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.SHNCD, txt_shncd);
          msg.add(o);
          return msg;
        }
      }

      if (StringUtils.isEmpty(szStcdShikakari) && StringUtils.isEmpty(szStcdKenmei)) {
        if (!this.checkMstExist(DefineReport.InpText.SHNCD.getObj(), txt_shncd)) {
          JSONObject o = mu.getDbMessageObj("E20124", new String[] {});
          this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.SHNCD, txt_shncd);
          msg.add(o);
          return msg;
        }
      } else if (!isNew && !StringUtils.isEmpty(szStcdKenmei)) {
        if (!new Reportx247Dao(JNDIname).checkMstExist(DefineReport.InpText.SHNCD.getObj(), txt_shncd)) {
          JSONObject o = mu.getDbMessageObj("E20124", new String[] {});
          this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.SHNCD, txt_shncd);
          msg.add(o);
          return msg;
        }
      }
    }

    String txt_ssircd = data.optString(MSTSHNLayout.SSIRCD.getId());
    String kbn105 = data.optString(MSTSHNLayout.SHNKBN.getId()); // 商品種類

    // 新規(正) 1.4 入力内容相関チェックを行う（入出力データ仕様のチェック内容を参照）。
    // 変更(正) 1.2 画面各項目間の入力内容相関チェックを行う（入出力データ仕様のチェック内容を参照）。

    // 親商品コード
    // ①商品コードと同じ場合、エラー。商品マスタに存在しない場合、エラー。
    String txt_parentcd = data.optString(MSTSHNLayout.PARENTCD.getId());
    if (isCsvUpload && StringUtils.isEmpty(szStcdKenmei)) {
      if (StringUtils.equals(txt_parentcd, txt_shncd_new)) {
        JSONObject o = mu.getDbMessageObj("E11102", new String[] {});
        this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.PARENTCD, data);
        msg.add(o);
        return msg;
      }
      // ②商品マスタに存在しない場合、エラー。
      if (!this.isEmptyVal(txt_parentcd, true)) {
        if (!this.checkMstExist(DefineReport.InpText.SHNCD.getObj(), txt_parentcd)) {
          JSONObject o = mu.getDbMessageObj("E11134", new String[] {});
          this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.PARENTCD, data);
          msg.add(o);
          return msg;
        }
      }
    }

    // 標準-部門コード
    // ①部門マスタに無い場合エラー
    if (!this.checkMstExist(DefineReport.InpText.BMNCD.getObj(), txt_bmncd)) {
      JSONObject o = mu.getDbMessageObj("E11044", new String[] {});
      this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.BMNCD, data);
      msg.add(o);
      return msg;
    }
    // ②頭2桁は商品コードと一致しないと、エラー。
    if (StringUtils.isEmpty(szStcdShikakari) && StringUtils.isEmpty(szStcdKenmei) && NumberUtils.toInt(txt_shncd_new.substring(0, 2)) != NumberUtils.toInt(txt_bmncd)) {
      String messageId = "E11205";
      if (isNew) {
        messageId = "E11162";
      }
      JSONObject o = mu.getDbMessageObj(messageId, new String[] {});
      this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.BMNCD, data);
      msg.add(o);
      return msg;

    }
    String txt_daicd = data.optString(MSTSHNLayout.DAICD.getId());
    String txt_chucd = data.optString(MSTSHNLayout.CHUCD.getId());
    String txt_shocd = data.optString(MSTSHNLayout.SHOCD.getId());
    String txt_sshocd = "";
    // 標準-分類：分類マスタに無い場合エラー
    if (msg.size() == 0) {
      JSONObject o = checkMstbmnExist(mu, errTbl, txt_bmncd, txt_daicd, txt_chucd, txt_shocd, txt_sshocd, "");
      if (!o.isEmpty()) {
        msg.add(o);
        return msg;
      }
    }
    String txt_bmncd_y = data.optString(MSTSHNLayout.YOT_BMNCD.getId());
    String txt_daicd_y = data.optString(MSTSHNLayout.YOT_DAICD.getId());
    String txt_chucd_y = data.optString(MSTSHNLayout.YOT_CHUCD.getId());
    String txt_shocd_y = data.optString(MSTSHNLayout.YOT_SHOCD.getId());
    // 用途-分類：分類マスタに無い場合エラー
    if (msg.size() == 0) {
      JSONObject o = checkMstbmnExist(mu, errTbl, txt_bmncd_y, txt_daicd_y, txt_chucd_y, txt_shocd_y, "", "_YOT");
      if (!o.isEmpty()) {
        msg.add(o);
        return msg;
      }
    }
    String txt_bmncd_u = data.optString(MSTSHNLayout.URI_BMNCD.getId());
    String txt_daicd_u = data.optString(MSTSHNLayout.URI_DAICD.getId());
    String txt_chucd_u = data.optString(MSTSHNLayout.URI_CHUCD.getId());
    String txt_shocd_u = data.optString(MSTSHNLayout.URI_SHOCD.getId());
    // 用途-分類：分類マスタに無い場合エラー
    if (msg.size() == 0) {
      JSONObject o = checkMstbmnExist(mu, errTbl, txt_bmncd_u, txt_daicd_u, txt_chucd_u, txt_shocd_u, "", "_URI");
      if (!o.isEmpty()) {
        msg.add(o);
        return msg;
      }
    }

    // ユニットプライス:
    String txt_up_yoryosu = data.optString(MSTSHNLayout.UP_YORYOSU.getId());
    String txt_up_tyoryosu = data.optString(MSTSHNLayout.UP_TYORYOSU.getId());
    String kbn113 = data.optString(MSTSHNLayout.UP_TANIKBN.getId()); // ユニット単位

    if (isCsvUpload) {
      if (!StringUtils.isEmpty(txt_up_yoryosu) && Integer.valueOf(txt_up_yoryosu) == 0) {
        txt_up_yoryosu = "";
      }
      if (!StringUtils.isEmpty(txt_up_tyoryosu) && Integer.valueOf(txt_up_tyoryosu) == 0) {
        txt_up_tyoryosu = "";
      }
      if (!StringUtils.isEmpty(kbn113) && Integer.valueOf(kbn113) == 0) {
        kbn113 = "";
      }
    }

    boolean isAllInput = (!this.isEmptyVal(txt_up_yoryosu, true) && !this.isEmptyVal(txt_up_tyoryosu, true) && !this.isEmptyVal(kbn113, false));
    boolean isAllEmpty = (this.isEmptyVal(txt_up_yoryosu, true) && this.isEmptyVal(txt_up_tyoryosu, true) && this.isEmptyVal(kbn113, false));
    if (!isAllInput && !isAllEmpty) {
      JSONObject o = mu.getDbMessageObj("E11105", new String[] {});
      this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.UP_YORYOSU, data);
      msg.add(o);
      return msg;
    }

    // ソースコード系
    // ソースコード取得
    ArrayList<JSONObject> srccds = new ArrayList<>();
    HashSet<String> srccds_ = new HashSet<>();
    for (int i = 0; i < dataArraySRCCD.size(); i++) {
      String val = dataArraySRCCD.optJSONObject(i).optString(MSTSRCCDLayout.SRCCD.getId());
      if (StringUtils.isNotEmpty(val)) {
        srccds.add(dataArraySRCCD.optJSONObject(i));
        srccds_.add(val);
      }

      if (!isNew) {
        if (this.checkMstExist(DefineReport.InpText.SRCCD.getObj() + "_UPD", txt_shncd + "," + val)) {
          continue;
        }
      }
      if (StringUtils.isEmpty(szStcdKenmei) && StringUtils.isEmpty(szStcdShikakari)) {
        if (this.checkMstExist(DefineReport.InpText.SRCCD.getObj(), val)) {
          errTbl = RefTable.MSTSRCCD;
          JSONObject o = mu.getDbMessageObj("E11139", new String[] {});
          this.setCsvshnErrinfo(o, errTbl, MSTSRCCDLayout.SRCCD, "");
          msg.add(o);
          return msg;
        }
      }
    }
    // ①NON-PLUの場合にソースコードに入力がある場合はエラー。
    // ②商品種類6または部門コード02,09,15,04,05,06,20,23,43,08,12,13,26,27の場合にNON-PLUにして良い、それ以外は1を入力してはならない。→削除：共通の商品種類チェックを優先
    String kbn117 = data.optString(MSTSHNLayout.TEIKEIKBN.getId()); // 定計区分
    if (DefineReport.ValKbn117.VAL1.getVal().equals(kbn117)) {
      if (srccds.size() > 0) {
        JSONObject o = mu.getDbMessageObj("E11106", new String[] {});
        this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.TEIKEIKBN, data);
        msg.add(o);
        return msg;
      }
      // Integer[] canNonPluBmn = new Integer[]{2,9,15,4,5,6,20,23,43,8,12,13,26,27};
      // if(!DefineReport.ValKbn105.VAL6.equals(kbn105)||!ArrayUtils.contains(canNonPluBmn,
      // NumberUtils.toInt(txt_bmncd))){
      // if(!DefineReport.ValKbn105.VAL6.equals(kbn105)){
      // JSONObject o = mu.getDbMessageObj("E11107", new String[]{});
      // this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.TEIKEIKBN, data);
      // msg.add(o);
      // return msg;
      // }
      // if(!ArrayUtils.contains(canNonPluBmn, NumberUtils.toInt(txt_bmncd))){
      // JSONObject o = mu.getDbMessageObj("E11108", new String[]{});
      // this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.TEIKEIKBN, data);
      // msg.add(o);
      // return msg;
      // }
      // }
    }
    errTbl = RefTable.MSTSRCCD;
    // 重複チェック
    if (srccds.size() != srccds_.size()) {
      JSONObject o = mu.getDbMessageObj("E11109", new String[] {});
      this.setCsvshnErrinfo(o, errTbl, MSTSRCCDLayout.SRCCD, "");
      msg.add(o);
      return msg;
    }

    String[] allseqnos = new String[] {};
    for (ValSrccdSeqno val : DefineReport.ValSrccdSeqno.values()) {
      allseqnos = (String[]) ArrayUtils.add(allseqnos, val.getVal());
    }
    String[] seqnos = new String[] {};
    // ソースコード１、２取得
    JSONObject srccdrow1 = null;
    JSONObject srccdrow2 = null;
    for (int i = 0; i < dataArraySRCCD.size(); i++) {
      String txt_srccd = dataArraySRCCD.optJSONObject(i).optString(MSTSRCCDLayout.SRCCD.getId());
      String sourcekbn = dataArraySRCCD.optJSONObject(i).optString(MSTSRCCDLayout.SOURCEKBN.getId());
      String seqno = dataArraySRCCD.optJSONObject(i).optString(MSTSRCCDLayout.SEQNO.getId());

      if (this.isEmptyVal(sourcekbn, false)) {
        JSONObject o = mu.getDbMessageObj("EX1047", new String[] {"ソース区分"});
        this.setCsvshnErrinfo(o, errTbl, MSTSRCCDLayout.SOURCEKBN, dataArraySRCCD.optJSONObject(i));
        msg.add(o);
        return msg;
      } else {

        if (StringUtils.isEmpty(txt_srccd) && !sourcekbn.equals("0")) {
          JSONObject o = mu.getDbMessageObj("E00001", new String[] {"ソースコード"});
          this.setCsvshnErrinfo(o, errTbl, MSTSRCCDLayout.SOURCEKBN, dataArraySRCCD.optJSONObject(i));
          msg.add(o);
          return msg;
        }

        if (sourcekbn.equals("0")) {
          JSONObject o = mu.getDbMessageObj("E11223");
          this.setCsvshnErrinfo(o, errTbl, MSTSRCCDLayout.SOURCEKBN, dataArraySRCCD.optJSONObject(i));
          msg.add(o);
          return msg;
        } else if ((sourcekbn.equals("1") || sourcekbn.equals("3")) && txt_srccd.length() != 13) {
          JSONObject o = mu.getDbMessageObj("E11302", new String[] {"ソースコードの桁数", "。ソース区分が" + sourcekbn + "の場合は13桁で入力してください。"});
          this.setCsvshnErrinfo(o, errTbl, MSTSRCCDLayout.SOURCEKBN, dataArraySRCCD.optJSONObject(i));
          msg.add(o);
          return msg;
        } else if ((sourcekbn.equals("2") || sourcekbn.equals("4")) && txt_srccd.length() != 8) {
          JSONObject o = mu.getDbMessageObj("E11302", new String[] {"ソースコードの桁数", "。ソース区分が" + sourcekbn + "の場合は8桁で入力してください。"});
          this.setCsvshnErrinfo(o, errTbl, MSTSRCCDLayout.SOURCEKBN, dataArraySRCCD.optJSONObject(i));
          msg.add(o);
          return msg;
        }
      }
      if (!this.checkMstExist(DefineReport.MeisyoSelect.KBN136.getObj(), sourcekbn)) {
        JSONObject o = mu.getDbMessageObj("E30027", new String[] {"名称コード"});
        this.setCsvshnErrinfo(o, errTbl, MSTSRCCDLayout.SOURCEKBN, dataArraySRCCD.optJSONObject(i));
        msg.add(o);
        return msg;
      }

      // SEQNOチェック
      if (!ArrayUtils.contains(allseqnos, seqno)
          || (ArrayUtils.contains(new String[] {DefineReport.ValSrccdSeqno.SRC1.getVal(), DefineReport.ValSrccdSeqno.SRC2.getVal()}, seqno) && ArrayUtils.contains(seqnos, seqno))) {
        JSONObject o = mu.getDbMessageObj("EX1051", new String[] {"ソースコードの順位は、"});
        this.setCsvshnErrinfo(o, errTbl, MSTSRCCDLayout.SEQNO, dataArraySRCCD.optJSONObject(i));
        msg.add(o);
        return msg;
      }

      // 有効期間チェック
      String txt_yuko_stdt = dataArraySRCCD.optJSONObject(i).optString(MSTSRCCDLayout.YUKO_STDT.getId());
      String txt_yuko_eddt = dataArraySRCCD.optJSONObject(i).optString(MSTSRCCDLayout.YUKO_EDDT.getId());
      if (DefineReport.ValSrccdSeqno.KARI.getVal().equals(seqno)) { // 一時登録
        if (this.isEmptyVal(txt_yuko_stdt, true)) {
          JSONObject o = mu.getDbMessageObj("EX1050", new String[] {});
          this.setCsvshnErrinfo(o, errTbl, MSTSRCCDLayout.YUKO_STDT, dataArraySRCCD.optJSONObject(i));
          msg.add(o);
          return msg;
        }
      } else { // 一般
        if (this.isEmptyVal(txt_yuko_stdt, true)) {
          JSONObject o = mu.getDbMessageObj("EX1049", new String[] {});
          this.setCsvshnErrinfo(o, errTbl, MSTSRCCDLayout.YUKO_STDT, dataArraySRCCD.optJSONObject(i));
          msg.add(o);
          return msg;
        }
        if (this.isEmptyVal(txt_yuko_eddt, true)) {
          JSONObject o = mu.getDbMessageObj("EX1049", new String[] {});
          this.setCsvshnErrinfo(o, errTbl, MSTSRCCDLayout.YUKO_EDDT, dataArraySRCCD.optJSONObject(i));
          msg.add(o);
          return msg;
        }
      }
      // 日付妥当性
      if (!this.isEmptyVal(txt_yuko_stdt, true) && !this.isEmptyVal(txt_yuko_eddt, true) && !InputChecker.isFromToNumeric(txt_yuko_stdt, txt_yuko_eddt)) {
        JSONObject o = mu.getDbMessageObj("E11020", new String[] {});
        this.setCsvshnErrinfo(o, errTbl, MSTSRCCDLayout.YUKO_EDDT, dataArraySRCCD.optJSONObject(i));
        msg.add(o);
        return msg;
      }

      // ソース1,2整合性
      if (StringUtils.equals(seqno, DefineReport.ValSrccdSeqno.SRC1.getVal())) {
        srccdrow1 = dataArraySRCCD.optJSONObject(i);
      }
      if (StringUtils.equals(seqno, DefineReport.ValSrccdSeqno.SRC2.getVal())) {
        srccdrow2 = dataArraySRCCD.optJSONObject(i);
      }
      if (srccdrow1 != null && srccdrow2 != null) {
        // ①ソース区分2(2行目)が1(JAN13) or 2(JAN8)の場合、ソース区分1(1行目)が3(EAN13), 4(EAN8), 5(UPC-A), 6(UPC-E)はエラー
        String kbn1 = srccdrow1.optString(MSTSRCCDLayout.SOURCEKBN.getId()).split("-")[0];
        String kbn2 = srccdrow2.optString(MSTSRCCDLayout.SOURCEKBN.getId()).split("-")[0];
        if (ArrayUtils.contains(new String[] {"1", "2"}, kbn2) && ArrayUtils.contains(new String[] {"3", "4", "5", "6"}, kbn1)) {
          JSONObject o = mu.getDbMessageObj("E11111", new String[] {});
          this.setCsvshnErrinfo(o, errTbl, MSTSRCCDLayout.SOURCEKBN, "");
          msg.add(o);
          return msg;
        }
      }
      // コード整合性チェック：チェックデジット算出コード取得
      // ソースコードに問題がある場合は、エラー情報が返ってくる（E11165,E11167,E11168,E11169,E11171,E11172,E11224）
      JSONObject result = NumberingUtility.calcCheckdigitSRCCD(userInfo, txt_srccd, sourcekbn);
      if (StringUtils.isNotEmpty(result.optString(MessageUtility.ID))) {
        JSONObject o = mu.getDbMessageObj(result.optString(MessageUtility.ID), new String[] {});
        this.setCsvshnErrinfo(o, errTbl, MSTSRCCDLayout.SRCCD, dataArraySRCCD.optJSONObject(i));
        msg.add(o);
        return msg;
      }
      if (ArrayUtils.contains(new String[] {DefineReport.ValSrccdSeqno.SRC1.getVal(), DefineReport.ValSrccdSeqno.SRC2.getVal()}, seqno)) {
        seqnos = (String[]) ArrayUtils.add(seqnos, seqno);
      }
    }
    // ソース登録があるにもかかわらず1指定がない場合エラーとする
    if (srccds.size() > 0 && !ArrayUtils.contains(seqnos, DefineReport.ValSrccdSeqno.SRC1.getVal())) {
      JSONObject o = mu.getDbMessageObj("E11110", new String[] {});
      this.setCsvshnErrinfo(o, errTbl, MSTSRCCDLayout.SEQNO, "");
      msg.add(o);
      return msg;
    }


    // 店別異部門
    errTbl = RefTable.MSTSHNTENBMN;
    ArrayList<JSONObject> tengp4s = new ArrayList<>();
    TreeSet<String> tengp4s_ = new TreeSet<>();
    TreeSet<String> tengp4keys = new TreeSet<>();
    String[] tenshncds = new String[] {};
    String areakbn4 = "";
    for (int i = 0; i < dataArrayTENGP4.size(); i++) {
      String tengpcd = dataArrayTENGP4.optJSONObject(i).optString(MSTSHNTENBMNLayout.TENGPCD.getId());
      String tenshncd = dataArrayTENGP4.optJSONObject(i).optString(MSTSHNTENBMNLayout.TENSHNCD.getId());
      if (StringUtils.isNotEmpty(tengpcd) || StringUtils.isNotEmpty(tenshncd)) {
        // 店グループに入力がある場合、商品コードは必須入力。
        if (StringUtils.isEmpty(tengpcd) || StringUtils.isEmpty(tenshncd)) {
          JSONObject o = mu.getDbMessageObj("EX1047", new String[] {"商品コード、店グループ"});
          this.setCsvshnErrinfo(o, errTbl, MSTSHNTENBMNLayout.TENSHNCD, "");
          msg.add(o);
          return msg;
        }

        tengp4s.add(dataArrayTENGP4.optJSONObject(i));
        tengp4s_.add(tengpcd);
        tengp4keys.add(tengpcd + "-" + tenshncd);
        areakbn4 = dataArrayTENGP4.optJSONObject(i).optString(MSTSHNTENBMNLayout.AREAKBN.getId());
        tenshncds = (String[]) ArrayUtils.add(tenshncds, tenshncd);
      }
    }
    // 商品マスタに存在しない場合、エラー。
    // 店別異部門商品コードが0の場合エラーを出さない(CSV取込エラーの回避)
    if (tenshncds.length > 0 && !(StringUtils.join(tenshncds, ",").equals("0"))) {
      if (!this.checkMstExist(DefineReport.InpText.TENSHNCD.getObj(), StringUtils.join(tenshncds, ","))) {
        JSONObject o = mu.getDbMessageObj("E11098", new String[] {});
        this.setCsvshnErrinfo(o, errTbl, MSTSHNTENBMNLayout.TENSHNCD, "");
        msg.add(o);
        return msg;
      }
    }
    // 商品店グループに存在しないコードはエラー
    // エリア区分がないor店グループが0ならエラーを出さない(CSV取込エラーの回避)
    String[] errtengps4 = this.checkMsttgpExist(tengp4s_, DefineReport.ValGpkbn.BAIKA.getVal(), txt_bmncd, areakbn4);
    if (errtengps4.length > 0 && areakbn4.length() > 0 && !StringUtils.join(tengp4s_, ",").equals("0")) {
      JSONObject o = mu.getDbMessageObj("E11140", new String[] {});
      this.setCsvshnErrinfo(o, errTbl, MSTSHNTENBMNLayout.TENGPCD, StringUtils.join(errtengps4, ","));
      msg.add(o);
      return msg;
    }
    for (int i = 0; i < dataArrayTENGP4.size(); i++) {
      // 商品コードが主の商品コードと同じ場合エラー
      if (StringUtils.equals(txt_shncd, dataArrayTENGP4.optJSONObject(i).optString(MSTSHNTENBMNLayout.TENSHNCD.getId()))) {
        JSONObject o = mu.getDbMessageObj("EX1047", new String[] {"基本情報と異なる商品コード"});
        this.setCsvshnErrinfo(o, errTbl, MSTSHNTENBMNLayout.TENSHNCD, dataArrayTENGP4.optJSONObject(i));
        msg.add(o);
        return msg;
      }
      // ソースコードが設定しているソースコードの中に無い場合エラー
      // ソースコードの文字列長が0ならエラーを出さない(CSV取込エラーの回避)
      String srccd = dataArrayTENGP4.optJSONObject(i).optString(MSTSHNTENBMNLayout.SRCCD.getId());
      if (!srccds_.contains(srccd) && srccd.length() > 0) {
        JSONObject o = mu.getDbMessageObj("EX1047", new String[] {"ソースコードにあるJANコード"});
        this.setCsvshnErrinfo(o, errTbl, MSTSHNTENBMNLayout.SRCCD, dataArrayTENGP4.optJSONObject(i));
        msg.add(o);
        return msg;
      }
    }
    // 画面に同じ重複がある場合、エラー。
    if (tengp4s.size() != tengp4keys.size()) {
      JSONObject o = mu.getDbMessageObj("E11112", new String[] {});
      this.setCsvshnErrinfo(o, errTbl, MSTSHNTENBMNLayout.TENGPCD, "");
      msg.add(o);
      return msg;
    }
    // 店グループを選択したら10番以上で登録しなければならない.
    if (DefineReport.ValKbn135.VAL1.getVal().equals(areakbn4)) { // 店グループ
      if (tengp4s_.size() > 0 && NumberUtils.toInt(tengp4s_.first()) < 10) {
        JSONObject o = mu.getDbMessageObj("E11038", new String[] {});
        this.setCsvshnErrinfo(o, errTbl, MSTSHNTENBMNLayout.TENGPCD, tengp4s_.first());
        msg.add(o);
        return msg;
      }
    }

    // 品揃えグループ
    errTbl = RefTable.MSTSHINAGP;
    ArrayList<JSONObject> tengp3s = new ArrayList<>();
    TreeSet<String> tengp3s_ = new TreeSet<>();
    String areakbn3 = "";
    for (int i = 0; i < dataArrayTENGP3.size(); i++) {
      String val = dataArrayTENGP3.optJSONObject(i).optString(MSTSHINAGPLayout.TENGPCD.getId());
      if (StringUtils.isNotEmpty(val)) {
        tengp3s.add(dataArrayTENGP3.optJSONObject(i));
        tengp3s_.add(val);
        areakbn3 = dataArrayTENGP3.optJSONObject(i).optString(MSTSHINAGPLayout.AREAKBN.getId());
      }
    }
    // 店グループ:商品店グループに存在しないコードはエラー。
    // 商品店グループに存在しないコードはエラー
    // エリア区分がないor店グループが0の場合はエラーを出さない(CSV取込エラーの回避)
    String[] errtengps = this.checkMsttgpExist(tengp3s_, DefineReport.ValGpkbn.SHINA.getVal(), txt_bmncd, areakbn3);
    if (errtengps.length > 0 && areakbn3.length() > 0 && !StringUtils.join(tengp3s_, ",").equals("0")) {
      JSONObject o = mu.getDbMessageObj("E11140", new String[] {});
      this.setCsvshnErrinfo(o, errTbl, MSTSHINAGPLayout.TENGPCD, StringUtils.join(errtengps, ","));
      msg.add(o);
      return msg;
    }
    // 扱い区分:店グループに入力がある場合、扱い区分未入力はエラー
    for (int i = 0; i < tengp3s.size(); i++) {
      // 店グループに入力がある場合、扱い区分未入力はエラー
      if (!StringUtils.join(tengp3s_, ",").equals("0") && StringUtils.isEmpty(tengp3s.get(i).optString(MSTSHINAGPLayout.ATSUKKBN.getId()))) {
        JSONObject o = mu.getDbMessageObj("EX1001", new String[] {});
        this.setCsvshnErrinfo(o, errTbl, MSTSHINAGPLayout.TENGPCD, dataArrayTENGP3.optJSONObject(i));
        msg.add(o);
        return msg;
      }
    }
    // 画面に同じ店グループがある場合、エラー。
    if (tengp3s.size() != tengp3s_.size()) {
      JSONObject o = mu.getDbMessageObj("E11112", new String[] {});
      this.setCsvshnErrinfo(o, errTbl, MSTSHINAGPLayout.TENGPCD, "");
      msg.add(o);
      return msg;
    }
    // 店グループを選択したら10番以上で登録しなければならない.
    if (DefineReport.ValKbn135.VAL1.getVal().equals(areakbn3)) { // 店グループ
      if (tengp3s_.size() > 0 && NumberUtils.toInt(tengp3s_.first()) < 10) {
        JSONObject o = mu.getDbMessageObj("E11038", new String[] {});
        this.setCsvshnErrinfo(o, errTbl, MSTSHINAGPLayout.TENGPCD, tengp3s_.first());
        msg.add(o);
        return msg;
      }
    }

    errTbl = RefTable.MSTSHN;
    // 取扱期間
    // ①取扱開始日0000/00/00 取扱終了日YYYY/MM/DD NG
    // ②取扱開始日YYYY/MM/DD 取扱終了日0000/00/00 NG
    // ③取扱開始日YYYY/MM/DD >= 取扱終了日YYYY/MM/DD NG"
    String txt_atsuk_stdt = data.optString(MSTSHNLayout.ATSUK_STDT.getId());
    String txt_atsuk_eddt = data.optString(MSTSHNLayout.ATSUK_EDDT.getId());
    isAllInput = (!this.isEmptyVal(txt_atsuk_stdt, true) && !this.isEmptyVal(txt_atsuk_eddt, true));
    isAllEmpty = (this.isEmptyVal(txt_atsuk_stdt, true) && this.isEmptyVal(txt_atsuk_eddt, true));
    if (!isAllInput && !isAllEmpty) {
      JSONObject o = mu.getDbMessageObj("E11114", new String[] {});
      this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.ATSUK_STDT, data);
      msg.add(o);
      return msg;
    }
    if (isAllInput && !InputChecker.isFromToDate(txt_atsuk_stdt, txt_atsuk_eddt)) {
      JSONObject o = mu.getDbMessageObj("E11115", new String[] {});
      this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.ATSUK_STDT, data);
      msg.add(o);
      return msg;
    }


    // 商品種類に基づくチェック
    // 部門:選択可部門
    if (StringUtils.equals(kbn105, DefineReport.ValKbn105.VAL2.getVal())) {
      // 部門:選択可部門
      // 02,09,15,04,05,06,20,23,43部門
      if (!ArrayUtils.contains(new Integer[] {2, 9, 15, 4, 5, 6, 20, 23, 43, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79}, NumberUtils.toInt(txt_bmncd))) {
        JSONObject o = mu.getDbMessageObj("E11143", new String[] {});
        this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.SHNKBN, data);
        msg.add(o);
        return msg;
      }
    } else if (StringUtils.equals(kbn105, DefineReport.ValKbn105.VAL3.getVal())) {
      // 部門:選択可部門
      // 88部門
      if (!ArrayUtils.contains(new Integer[] {88}, NumberUtils.toInt(txt_bmncd))) {
        JSONObject o = mu.getDbMessageObj("E11143", new String[] {});
        this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.SHNKBN, data);
        msg.add(o);
        return msg;
      }
    } else if (NumberUtils.toInt(txt_bmncd) == 88) {
      JSONObject o = mu.getDbMessageObj("E11143", new String[] {});
      this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.SHNKBN, data);
      msg.add(o);
      return msg;
    }

    // 原売価値入ﾁｪｯｸ
    // ﾚｷﾞｭﾗｰ原売価:値入ﾁｪｯｸ
    String chk_rg_atsukflg = data.optString(MSTSHNLayout.RG_ATSUKFLG.getId());
    String txt_rg_genkaam = data.optString(MSTSHNLayout.RG_GENKAAM.getId());
    String txt_rg_baikaam = data.optString(MSTSHNLayout.RG_BAIKAAM.getId());
    if (ArrayUtils.contains(new String[] {"0", "1"}, kbn105) && this.calcNeireRit(txt_rg_genkaam, txt_rg_baikaam) >= 98d) {
      JSONObject o = mu.getDbMessageObj("E11144", new String[] {});
      this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.RG_GENKAAM, data);
      msg.add(o);
      return msg;
    }
    // 販促原売価:値入ﾁｪｯｸ
    String chk_hs_atsukflg = data.optString(MSTSHNLayout.HS_ATSUKFLG.getId());
    String txt_hs_genkaam = data.optString(MSTSHNLayout.HS_GENKAAM.getId());
    String txt_hs_baikaam = data.optString(MSTSHNLayout.HS_BAIKAAM.getId());
    if (ArrayUtils.contains(new String[] {"0", "1"}, kbn105) && this.calcNeireRit(txt_hs_genkaam, txt_hs_baikaam) >= 98d) {
      JSONObject o = mu.getDbMessageObj("E11144", new String[] {});
      this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.HS_GENKAAM, data);
      msg.add(o);
      return msg;
    }

    // 定計区分:”１”を許可するチェック
    // 種類：0 許可部門 02,09,15,04,05,06,20,23,43,08,12,13,26,27部門
    // 種類：0以外 許可部門 全部門
    if (DefineReport.ValKbn117.VAL1.getVal().equals(kbn117) && DefineReport.ValKbn105.VAL0.getVal().equals(kbn105)
        && !ArrayUtils.contains(new Integer[] {2, 9, 15, 4, 5, 6, 20, 23, 43, 8, 12, 13, 26, 27, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79}, NumberUtils.toInt(txt_bmncd))) {
      JSONObject o = mu.getDbMessageObj("E11148", new String[] {});
      this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.TEIKEIKBN, data);
      msg.add(o);
      return msg;
    }

    // 定貫区分:”０”を許可するチェック
    // 種類：0 許可部門 02,09,15,04,05,06,20,23,43,08,12,13,26,27部門
    // 種類：0以外 許可部門 全部門
    String kbn121 = data.optString(MSTSHNLayout.TEIKANKBN.getId()); // 定貫不定貫区分
    if (DefineReport.ValKbn121.VAL0.getVal().equals(kbn121) && DefineReport.ValKbn105.VAL0.getVal().equals(kbn105)
        && !ArrayUtils.contains(new Integer[] {2, 9, 15, 4, 5, 6, 20, 23, 43, 8, 12, 13, 26, 27, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79}, NumberUtils.toInt(txt_bmncd))) {
      JSONObject o = mu.getDbMessageObj("E11149", new String[] {});
      this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.TEIKANKBN, data);
      msg.add(o);
      return msg;
    }
    // POP名称:省略可不可
    // 種類：0 不可
    // 種類：0以外 可
    if (DefineReport.ValKbn105.VAL0.getVal().equals(kbn105) && StringUtils.isEmpty(data.optString(MSTSHNLayout.POPKN.getId()))) {
      JSONObject o = mu.getDbMessageObj("E11150", new String[] {});
      this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.POPKN, data);
      msg.add(o);
      return msg;
    }
    // 標準分類（大分類以下）:標準分類省略可不可
    // 種類：465 可
    // 種類：上記以外 不可
    if (!ArrayUtils.contains(new String[] {"4", "6", "5"}, kbn105) && (this.isEmptyVal(txt_daicd, false) || this.isEmptyVal(txt_chucd, false) || this.isEmptyVal(txt_shocd, false))) {
      MSTSHNLayout colinfo = MSTSHNLayout.DAICD;
      if (this.isEmptyVal(txt_chucd, false)) {
        colinfo = MSTSHNLayout.CHUCD;
      } else if (this.isEmptyVal(txt_shocd, false)) {
        colinfo = MSTSHNLayout.SHOCD;
      }
      JSONObject o = mu.getDbMessageObj("E11151", new String[] {});
      this.setCsvshnErrinfo(o, errTbl, colinfo, "");
      msg.add(o);
      return msg;
    }

    // PC区分
    // ①1を指定して良いのは部門コードが04,05,06,43の場合のみ。
    // ②標準仕入先コードから仕入先マスタのデフォルト加工指示を参照し、'1'の場合、PC区分'1'が可能です。
    String kbn102 = data.optString(MSTSHNLayout.PCKBN.getId());
    if (DefineReport.ValKbn102.VAL1.getVal().equals(kbn102) && !ArrayUtils.contains(new Integer[] {4, 5, 6, 43, 20, 23, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79}, NumberUtils.toInt(txt_bmncd))) {
      JSONObject o = mu.getDbMessageObj("E11116", new String[] {});
      this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.PCKBN, data);
      msg.add(o);
      return msg;
    }
    if (ArrayUtils.contains(new Integer[] {4, 5, 6, 43}, NumberUtils.toInt(txt_bmncd))) {
      JSONArray mstsir_rows = this.getMstData(DefineReport.ID_SQL_SIR_, new ArrayList<>(Arrays.asList(txt_ssircd)));

      boolean err = false;
      if (mstsir_rows.size() != 1) {
        err = true;
      } else {
        String dfKakoSjKbn = mstsir_rows.optJSONObject(0).optString("DF_KAKOSJKBN");

        if (!dfKakoSjKbn.equals("0") && !dfKakoSjKbn.equals("1")) {
          err = true;
        } else if (!kbn102.equals("0") && !kbn102.equals("1")) {
          err = true;
        } else if (!dfKakoSjKbn.equals(kbn102)) {
          err = true;
        }
      }

      if (err) {
        JSONObject o = mu.getDbMessageObj("E11326", new String[] {});
        this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.PCKBN, data);
        msg.add(o);
        return msg;
      }
    }
    // レギュラー
    isAllInput = (!this.isEmptyVal(txt_rg_genkaam, true) && !this.isEmptyVal(txt_rg_baikaam, true) && !this.isEmptyVal(data.optString(MSTSHNLayout.RG_IRISU.getId()), true));
    isAllEmpty = (this.isEmptyVal(txt_rg_genkaam, true) && this.isEmptyVal(txt_rg_baikaam, true) && this.isEmptyVal(data.optString(MSTSHNLayout.RG_IRISU.getId()), true));
    if ((!isAllInput && !isAllEmpty) || (DefineReport.Values.ON.getVal().equals(chk_rg_atsukflg) && isAllEmpty)) {
      JSONObject o = mu.getDbMessageObj("E11119", new String[] {});
      this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.RG_ATSUKFLG, data);
      msg.add(o);
      return msg;
    }
    // 販促
    isAllInput = (!this.isEmptyVal(txt_hs_genkaam, true) && !this.isEmptyVal(txt_hs_baikaam, true) && !this.isEmptyVal(data.optString(MSTSHNLayout.HS_IRISU.getId()), true));
    isAllEmpty = (this.isEmptyVal(txt_hs_genkaam, true) && this.isEmptyVal(txt_hs_baikaam, true) && this.isEmptyVal(data.optString(MSTSHNLayout.HS_IRISU.getId()), true));
    if ((!isAllInput && !isAllEmpty) || (DefineReport.Values.ON.getVal().equals(chk_hs_atsukflg) && isAllEmpty)) {
      JSONObject o = mu.getDbMessageObj("E11119", new String[] {});
      this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.HS_ATSUKFLG, data);
      msg.add(o);
      return msg;
    }

    // 税区分系
    String kbn120 = data.optString(MSTSHNLayout.ZEIKBN.getId());
    // 税率変更日: 税区分が3の場合、設定不可。 TODO:メッセージは２、３
    if (ArrayUtils.contains(new String[] {DefineReport.ValKbn120.VAL2.getVal(), DefineReport.ValKbn120.VAL3.getVal()}, kbn120)
        && !this.isEmptyVal(data.optString(MSTSHNLayout.ZEIRTHENKODT.getId()), true)) {
      JSONObject o = mu.getDbMessageObj("E11121", new String[] {});
      this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.ZEIRTHENKODT, data);
      msg.add(o);
      return msg;
    }
    // 税率区分: 税区分が0,1の場合、税率区分に登録がないとエラー。税区分が2,3の場合、税率区分は設定不可。
    String sel_zeirtkbn = data.optString(MSTSHNLayout.ZEIRTKBN.getId());
    if (isCsvUpload) {
      if (!StringUtils.isEmpty(sel_zeirtkbn) && Integer.valueOf(sel_zeirtkbn) == 0) {
        sel_zeirtkbn = "";
      }
    }
    if (ArrayUtils.contains(new String[] {DefineReport.ValKbn120.VAL0.getVal(), DefineReport.ValKbn120.VAL1.getVal()}, kbn120) && this.isEmptyVal(sel_zeirtkbn, false)) {
      JSONObject o = mu.getDbMessageObj("E11152", new String[] {});
      this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.ZEIRTKBN, data);
      msg.add(o);
      return msg;
    } else if (ArrayUtils.contains(new String[] {DefineReport.ValKbn120.VAL2.getVal(), DefineReport.ValKbn120.VAL3.getVal()}, kbn120) && !this.isEmptyVal(sel_zeirtkbn, false)) {
      JSONObject o = mu.getDbMessageObj("E11153", new String[] {});
      this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.ZEIRTKBN, data);
      msg.add(o);
      return msg;
    }
    // 旧税率区分: 税区分が3の場合、設定不可。2.非課税は選択不可 TODO:メッセージは税率区分と一緒
    String sel_zeirtkbn_old = data.optString("F68");
    if (isCsvUpload) {
      if (!StringUtils.isEmpty(sel_zeirtkbn_old) && Integer.valueOf(sel_zeirtkbn_old) == 0) {
        sel_zeirtkbn_old = "";
      }
    }
    if (ArrayUtils.contains(new String[] {DefineReport.ValKbn120.VAL0.getVal(), DefineReport.ValKbn120.VAL1.getVal()}, kbn120) && this.isEmptyVal(sel_zeirtkbn_old, false)) {
      JSONObject o = mu.getDbMessageObj("E11185", new String[] {});
      this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.ZEIRTKBN_OLD, data);
      msg.add(o);
      return msg;
    } else if (ArrayUtils.contains(new String[] {DefineReport.ValKbn120.VAL2.getVal(), DefineReport.ValKbn120.VAL3.getVal()}, kbn120) && !this.isEmptyVal(sel_zeirtkbn_old, false)) {
      JSONObject o = mu.getDbMessageObj("E11181", new String[] {});
      this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.ZEIRTKBN_OLD, data);
      msg.add(o);
      return msg;
    }

    // ITFコード
    // コード整合性チェック：チェックデジット算出コード取得
    String txt_itfcd = data.optString(MSTSHNLayout.ITFCD.getId());
    if (!this.isEmptyVal(txt_itfcd, false)) {
      JSONObject result = NumberingUtility.calcCheckdigitITFCD(userInfo, txt_itfcd);
      if (StringUtils.isNotEmpty(result.optString(MessageUtility.ID))) {
        JSONObject o = mu.getDbMessageObj(result.optString(MessageUtility.ID), new String[] {"ITFコード"});
        this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.ITFCD, data);
        msg.add(o);
        return msg;
      }
    }

    // 売価グループ
    // レギュラー取扱フラグのチェックがない場合、この売価コントロール部分は設定不可。
    errTbl = RefTable.MSTBAIKACTL;
    ArrayList<JSONObject> tengp2s = new ArrayList<>();
    TreeSet<String> tengp2s_ = new TreeSet<>();
    String areakbn2 = "";
    for (int i = 0; i < dataArrayTENGP2.size(); i++) {
      String val = dataArrayTENGP2.optJSONObject(i).optString(MSTBAIKACTLLayout.TENGPCD.getId());
      if (StringUtils.isNotEmpty(val)) {

        tengp2s.add(dataArrayTENGP2.optJSONObject(i));
        tengp2s_.add(val);
        areakbn2 = dataArrayTENGP2.optJSONObject(i).optString(MSTBAIKACTLLayout.AREAKBN.getId());
      }
    }

    // 店グループ:商品店グループに存在しないコードはエラー。
    // 商品店グループに存在しないコードはエラー
    // エリア区分がないor店グループが0の場合はエラーを出さない(CSV取込)
    errtengps = this.checkMsttgpExist(tengp2s_, DefineReport.ValGpkbn.BAIKA.getVal(), txt_bmncd, areakbn2);
    if (errtengps.length > 0 && areakbn2.length() > 0 && !StringUtils.join(tengp2s_, ",").equals("0")) {
      JSONObject o = mu.getDbMessageObj("E11140", new String[] {});
      this.setCsvshnErrinfo(o, errTbl, MSTBAIKACTLLayout.TENGPCD, StringUtils.join(errtengps, ","));
      msg.add(o);
      return msg;
    }
    for (int i = 0; i < tengp2s.size(); i++) {
      // 店グループに入力がある場合、原価、売価、店入数のすべてが未入力だとエラー。
      isAllEmpty = this.isEmptyVal(tengp2s.get(i).optString(MSTBAIKACTLLayout.GENKAAM.getId()), true) && this.isEmptyVal(tengp2s.get(i).optString(MSTBAIKACTLLayout.BAIKAAM.getId()), true)
          && this.isEmptyVal(tengp2s.get(i).optString(MSTBAIKACTLLayout.IRISU.getId()), true);
      if (!StringUtils.join(tengp2s_, ",").equals("0") && isAllEmpty) {
        JSONObject o = mu.getDbMessageObj("E11122", new String[] {});
        this.setCsvshnErrinfo(o, errTbl, MSTBAIKACTLLayout.TENGPCD, dataArrayTENGP2.optJSONObject(i));
        msg.add(o);
        return msg;
      }
    }
    // 画面に同じ店グループがある場合、エラー。
    if (tengp2s.size() != tengp2s_.size()) {
      JSONObject o = mu.getDbMessageObj("E11112", new String[] {});
      this.setCsvshnErrinfo(o, errTbl, MSTBAIKACTLLayout.TENGPCD, "");
      msg.add(o);
      return msg;
    }
    // 店グループを選択したら10番以上で登録しなければならない.
    if (DefineReport.ValKbn135.VAL1.getVal().equals(areakbn2)) { // 店グループ
      if (tengp2s_.size() > 0 && NumberUtils.toInt(tengp2s_.first()) < 10) {
        JSONObject o = mu.getDbMessageObj("E11038", new String[] {});
        this.setCsvshnErrinfo(o, errTbl, MSTBAIKACTLLayout.TENGPCD, tengp2s_.first());
        msg.add(o);
        return msg;
      }
    }

    // 仕入グループ
    errTbl = RefTable.MSTSIRGPSHN;
    ArrayList<JSONObject> tengp1s = new ArrayList<>();
    TreeSet<String> tengp1s_ = new TreeSet<>();
    TreeSet<String> tengp1keys = new TreeSet<>();
    ArrayList<String> sircdList = new ArrayList<>();
    String areakbn1 = "";
    for (int i = 0; i < dataArrayTENGP1.size(); i++) {
      String val = dataArrayTENGP1.optJSONObject(i).optString(MSTSIRGPSHNLayout.TENGPCD.getId());
      if (StringUtils.isNotEmpty(val)) {
        tengp1s.add(dataArrayTENGP1.optJSONObject(i));
        tengp1s_.add(val);
        tengp1keys.add(dataArrayTENGP1.optJSONObject(i).optString(MSTSIRGPSHNLayout.SIRCD.getId()) + "-" + dataArrayTENGP1.optJSONObject(i).optString(MSTSIRGPSHNLayout.HSPTN.getId()));
        areakbn1 = dataArrayTENGP1.optJSONObject(i).optString(MSTSIRGPSHNLayout.AREAKBN.getId());
        sircdList.add(dataArrayTENGP1.optJSONObject(i).optString(MSTSIRGPSHNLayout.SIRCD.getId()));
      }
    }
    // 店グループ:商品店グループに存在しないコードはエラー。
    // 商品店グループに存在しないコードはエラー
    // エリア区分がないor店グループが0の場合はエラーを出さない(CSV取込エラーの回避)
    errtengps = this.checkMsttgpExist(tengp1s_, DefineReport.ValGpkbn.SIR.getVal(), txt_bmncd, areakbn1);
    if (errtengps.length > 0 && areakbn1.length() > 0 && !StringUtils.join(tengp1s_, ",").equals("0")) {
      JSONObject o = mu.getDbMessageObj("E11140", new String[] {});
      this.setCsvshnErrinfo(o, errTbl, MSTSIRGPSHNLayout.TENGPCD, StringUtils.join(errtengps, ","));
      msg.add(o);
      return msg;
    }
    // 店グループに入力がある場合、仕入先コード、配送パターンは必須入力。
    for (JSONObject tengp1 : tengp1s) {
      if (!StringUtils.join(tengp1s_, ",").equals("0")
          && (StringUtils.isEmpty(tengp1.optString(MSTSIRGPSHNLayout.SIRCD.getId())) || StringUtils.isEmpty(tengp1.optString(MSTSIRGPSHNLayout.HSPTN.getId())))) {
        JSONObject o = mu.getDbMessageObj("E11123", new String[] {});
        this.setCsvshnErrinfo(o, errTbl, MSTSIRGPSHNLayout.TENGPCD, tengp1);
        msg.add(o);
        return msg;
      }
    }
    // 画面に同じ店グループがある場合、エラー。
    if (tengp1s.size() != tengp1s_.size()) {
      JSONObject o = mu.getDbMessageObj("E11112", new String[] {});
      this.setCsvshnErrinfo(o, errTbl, MSTSIRGPSHNLayout.TENGPCD, "");
      msg.add(o);
      return msg;
    }
    if (tengp1s_.size() > 0) {
      // 店グループで展開し、店コードが重複する場合はエラー。
      ArrayList<String> paramData = new ArrayList<>(Arrays.asList(DefineReport.ValGpkbn.SIR.getVal(), txt_bmncd, areakbn1));
      String rep = "";
      for (String cd : tengp1s_) {
        rep += ", ?";
        paramData.add(cd);
      }
      rep = StringUtils.removeStart(rep, ",");
      JSONArray msttngp1_rows = this.getMstData(DefineReport.ID_SQL_TENGP_CHK_TEN_CNT.replace("@", rep), paramData);
      if (msttngp1_rows.size() > 1 && !StringUtils.equals(msttngp1_rows.optJSONObject(0).optString("CNT"), "0")) {
        JSONObject o = mu.getDbMessageObj("E11141", new String[] {});
        this.setCsvshnErrinfo(o, errTbl, MSTSIRGPSHNLayout.TENGPCD, "");
        msg.add(o);
        return msg;
      }

      // 仕入先コード:仕入先マスタに存在しない場合エラー
      // 仕入先コードがない場合はエラーを出さない(CSV取込エラーの回避)
      for (String sircd : sircdList) {
        if (sircd.length() > 0 && !this.checkMstExist(DefineReport.InpText.SIRCD.getObj(), sircd)) {
          JSONObject o = mu.getDbMessageObj("E11099", new String[] {});
          this.setCsvshnErrinfo(o, errTbl, MSTSIRGPSHNLayout.SIRCD, "");
          msg.add(o);
          return msg;
        }
      }

      // 仕入先コード、配送パターンで配送パターン仕入先マスタの存在チェック
      // 仕入先コード、配送パターンがどちらもない場合はエラーを出さない(CSV取込エラーの回避)
      if (!StringUtils.join(tengp1keys, ",").equals("-") && !this.checkMstExist("MSTHSPTNSIR", StringUtils.join(tengp1keys, ","))) {
        JSONObject o = mu.getDbMessageObj("E11142", new String[] {});
        this.setCsvshnErrinfo(o, errTbl, MSTSIRGPSHNLayout.TENGPCD, "");
        msg.add(o);
        return msg;
      }
    }
    // 店グループを選択したら10番以上で登録しなければならない.
    if (DefineReport.ValKbn135.VAL1.getVal().equals(areakbn1)) { // 店グループ
      if (tengp1s_.size() > 0 && NumberUtils.toInt(tengp1s_.first()) < 10) {
        JSONObject o = mu.getDbMessageObj("E11038", new String[] {});
        this.setCsvshnErrinfo(o, errTbl, MSTSIRGPSHNLayout.TENGPCD, tengp1s_.first());
        msg.add(o);
        return msg;
      }
    }

    errTbl = RefTable.MSTSHN;
    // 仕入先コード:仕入先マスタに存在しない場合エラー
    if (!this.isEmptyVal(txt_ssircd, false)) {
      if (!this.checkMstExist(DefineReport.InpText.SIRCD.getObj(), txt_ssircd)) {
        JSONObject o = mu.getDbMessageObj("E11099", new String[] {});
        this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.SSIRCD, data);
        msg.add(o);
        return msg;
      }
    }

    // 標準仕入先・配送パターン：仕入先コード、配送パターンで配送パターン仕入先マスタの存在チェック
    String txt_hsptn = data.optString(MSTSHNLayout.HSPTN.getId());
    if (!this.isEmptyVal(txt_ssircd, false) && !this.isEmptyVal(txt_hsptn, false)) {
      if (!this.checkMstExist("MSTHSPTNSIR", txt_ssircd + "-" + txt_hsptn)) {
        JSONObject o = mu.getDbMessageObj("E11142", new String[] {});
        this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.SSIRCD, data);
        msg.add(o);
        return msg;
      }
    }

    // リードタイムパターン
    String messageid = this.isAbleReadtmptn(data, txt_bmncd);
    if (messageid != null) {
      JSONObject o = mu.getDbMessageObj(messageid, new String[] {});
      this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.READTMPTN, data);
      msg.add(o);
      return msg;
    }

    // 締め回数
    // ②2を設定して良いのは11,34部門である。
    if (DefineReport.ValKbn134.VAL2.getVal().equals(data.optString(MSTSHNLayout.SIMEKAISU.getId())) && !ArrayUtils.contains(new Integer[] {11, 34}, NumberUtils.toInt(txt_bmncd))) {
      JSONObject o = mu.getDbMessageObj("E11125", new String[] {});
      this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.READTMPTN, data);
      msg.add(o);
      return msg;
    }
    // 便
    // ②2を設定して良いのは02,09,15,04,05,06,20,23,43,10,11,34部門である。
    if (DefineReport.ValKbn132.VAL2.getVal().equals(data.optString(MSTSHNLayout.BINKBN.getId()))
        && !ArrayUtils.contains(new Integer[] {2, 9, 15, 4, 5, 6, 20, 23, 43, 10, 11, 34}, NumberUtils.toInt(txt_bmncd))) {
      JSONObject o = mu.getDbMessageObj("E11126", new String[] {});
      this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.BINKBN, data);
      msg.add(o);
      return msg;
    }
    // 一括伝票フラグ
    // ②定貫不定貫区分が0の場合、1.センター経由の一括のみ許可する
    // ③PC区分が1の場合、1.センター経由の一括のみ許可する
    String txt_rg_idenflg = data.optString(MSTSHNLayout.RG_IDENFLG.getId());
    if ((DefineReport.ValKbn121.VAL0.getVal().equals(kbn121) && txt_rg_idenflg.length() == 0) || (DefineReport.ValKbn102.VAL1.getVal().equals(kbn102) && txt_rg_idenflg.length() == 0)) {
      JSONObject o = mu.getDbMessageObj("E11127", new String[] {});
      this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.RG_IDENFLG, data);
      msg.add(o);
      return msg;
    }
    // 酒級:部門コード03,44以外は選択不可。
    String kbn129 = data.optString(MSTSHNLayout.SHUKYUKBN.getId());
    if (!this.isEmptyVal(kbn129, true) && !ArrayUtils.contains(new Integer[] {3, 44}, NumberUtils.toInt(txt_bmncd))) {
      JSONObject o = mu.getDbMessageObj("E11129", new String[] {});
      this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.SHUKYUKBN, data);
      msg.add(o);
      return msg;
    }
    // 度数：酒級に登録がある場合、必ず入力。スペース、0は未入力と処理する。 部門コード03,44以外は選択不可
    String txt_dosu = data.optString(MSTSHNLayout.DOSU.getId());
    if (!this.isEmptyVal(kbn129, true) && this.isEmptyVal(txt_dosu, true)) {
      JSONObject o = mu.getDbMessageObj("E11130", new String[] {});
      this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.DOSU, data);
      msg.add(o);
      return msg;
    }
    if (!this.isEmptyVal(txt_dosu, true) && !ArrayUtils.contains(new Integer[] {3, 44}, NumberUtils.toInt(txt_bmncd))) {
      JSONObject o = mu.getDbMessageObj("E11131", new String[] {});
      this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.DOSU, data);
      msg.add(o);
      return msg;
    }
    // 入荷期限 0は未入力と処理する。入荷期限≧値引期限。0より大きい。
    // 値引期限 0以上を登録可能とする。
    String txt_ods_nyukasu = data.optString(MSTSHNLayout.ODS_NYUKASU.getId()); // 入荷期限:0=未入力扱い
    String txt_ods_nebikisu = data.optString(MSTSHNLayout.ODS_NEBIKISU.getId()); // 値引期限:0=数値
    isAllEmpty = this.isEmptyVal(txt_ods_nyukasu, true) && this.isEmptyVal(txt_ods_nebikisu, false);
    // 入荷期限≧値引期限かチェック
    if (!isAllEmpty && NumberUtils.toInt(txt_ods_nyukasu, 0) < NumberUtils.toInt(txt_ods_nebikisu, 0)) {
      JSONObject o = mu.getDbMessageObj("E11132", new String[] {});
      this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.ODS_NYUKASU, data);
      msg.add(o);
      return msg;
    }

    // 添加物
    errTbl = RefTable.MSTTENKABUTSU;
    ArrayList<JSONObject> tenkabcds1 = new ArrayList<>(), tenkabcds2 = new ArrayList<>();
    TreeSet<String> tenkabcds1_ = new TreeSet<>(), tenkabcds2_ = new TreeSet<>();
    for (int i = 0; i < dataArrayTENKABUTSU.size(); i++) {
      String kbn = dataArrayTENKABUTSU.optJSONObject(i).optString(MSTTENKABUTSULayout.TENKABKBN.getId());
      String val = dataArrayTENKABUTSU.optJSONObject(i).optString(MSTTENKABUTSULayout.TENKABCD.getId());
      if (StringUtils.isNotEmpty(val)) {
        if (DefineReport.ValTenkabkbn.VAL1.getVal().equals(kbn)) {
          tenkabcds1.add(dataArrayTENKABUTSU.optJSONObject(i));
          tenkabcds1_.add(val);
        } else if (DefineReport.ValTenkabkbn.VAL2.getVal().equals(kbn)) {
          tenkabcds2.add(dataArrayTENKABUTSU.optJSONObject(i));
          tenkabcds2_.add(val);
        }
      }
    }
    // 同じ添加物がある場合、エラー。
    if ((tenkabcds1.size() != tenkabcds1_.size()) || (tenkabcds2.size() != tenkabcds2_.size())) {
      JSONObject o = mu.getDbMessageObj("E11133", new String[] {});
      this.setCsvshnErrinfo(o, errTbl, MSTTENKABUTSULayout.TENKABCD, "");
      msg.add(o);
      return msg;
    }

    // グループ分類名
    errTbl = RefTable.MSTGRP;
    ArrayList<JSONObject> mstgrps = new ArrayList<>();
    TreeSet<String> mstgrps_ = new TreeSet<>();
    for (int i = 0; i < dataArrayGROUP.size(); i++) {
      String val = dataArrayGROUP.optJSONObject(i).optString(MSTGRPLayout.GRPKN.getId());
      if (StringUtils.isNotEmpty(val)) {
        mstgrps.add(dataArrayGROUP.optJSONObject(i));
        mstgrps_.add(val);
      }
    }
    // 同じ名称がある場合、エラー。
    if (mstgrps.size() != mstgrps_.size()) {
      JSONObject o = mu.getDbMessageObj("EX1012", new String[] {});
      this.setCsvshnErrinfo(o, errTbl, MSTGRPLayout.GRPKN, "");
      msg.add(o);
      return msg;
    }
    // 自動発注
    errTbl = RefTable.MSTAHS;
    ArrayList<JSONObject> mstahss = new ArrayList<>();
    TreeSet<String> mstahss_ = new TreeSet<>();
    for (int i = 0; i < dataArrayAHS.size(); i++) {
      // 自動発注区分は0,1で登録しなければならない
      if (!ArrayUtils.contains(new String[] {DefineReport.Values.ON.getVal(), DefineReport.Values.OFF.getVal()}, dataArrayAHS.optJSONObject(i).optString(MSTAHSLayout.AHSKB.getId()))) {
        JSONObject o = mu.getDbMessageObj("E11012", new String[] {"自動発注区分"});
        this.setCsvshnErrinfo(o, errTbl, MSTAHSLayout.AHSKB, dataArrayAHS.optJSONObject(i));
        msg.add(o);
        return msg;
      }
      String val = dataArrayAHS.optJSONObject(i).optString(MSTAHSLayout.TENCD.getId());
      if (StringUtils.isNotEmpty(val)) {
        mstahss.add(dataArrayAHS.optJSONObject(i));
        mstahss_.add(val);
      }
    }
    // 同じ名称がある場合、エラー。
    if (mstahss.size() != mstahss_.size()) {
      JSONObject o = mu.getDbMessageObj("E11141", new String[] {});
      this.setCsvshnErrinfo(o, errTbl, MSTAHSLayout.TENCD, "");
      msg.add(o);
      return msg;
    }

    errTbl = RefTable.MSTSHN;
    // 変更(正) 1.3 添付資料（MD03112701）の“テーブル削除の整理”と添付資料の（MD03111002）の“商品マスタ登録時 マスタ変更予定日、店売価実施日のチェック”を参照。
    // 新規(予) 1.4 添付資料（MD03111002）の“商品マスタ登録時 マスタ変更予定日、店売価実施日のチェック”を参照。
    // 変更(予) 1.3 添付資料（MD03112701）の“テーブル削除の整理”と添付資料の（MD03111002）の“商品マスタ登録時 マスタ変更予定日、店売価実施日のチェック”を参照。
    if (isYoyaku1 || isYoyaku2) {
      // * 店売価実施日-４日＜ﾏｽﾀｰ変更日＜店売価実施日＆処理日付＜ﾏｽﾀｰ変更日＆処理日付＜店売価実施日
      // 送信日=店売価実施日-４日
      Integer int_sysdate = NumberUtils.toInt(sysdate);
      Integer int_senddate = NumberUtils.toInt(CmnDate.dateFormat(CmnDate.getDayAddedDate(CmnDate.convDate(txt_tenbaikadt), -4), DATE_FORMAT.DEFAULT_DATE));
      Integer int_yoyakudt = NumberUtils.toInt(txt_yoyakudt);
      Integer int_tenbaikadt = NumberUtils.toInt(txt_tenbaikadt);

      // ④予1.新規の場合
      if (isNewY1) {
        // マスタ変更予定日:処理日付＜ﾏｽﾀｰ変更日
        if (!(int_sysdate < int_yoyakudt)) {
          JSONObject o = mu.getDbMessageObj("E11190", new String[] {});
          this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.YOYAKUDT, data);
          msg.add(o);
          return msg;
        }
        // 店売価実施日:処理日付＜店売価実施日
        if (!(int_sysdate < int_tenbaikadt)) {
          JSONObject o = mu.getDbMessageObj("E11191", new String[] {});
          this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.TENBAIKADT, data);
          msg.add(o);
          return msg;

        }
        // 店売価実施日-４日＜ﾏｽﾀｰ変更日＜店売価実施日
        if (!(int_senddate < int_yoyakudt && int_yoyakudt < int_tenbaikadt)) {
          JSONObject o = mu.getDbMessageObj("E11192", new String[] {});
          this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.YOYAKUDT, data);
          msg.add(o);
          return msg;
        }

        // 店売価実施日:処理日付＜店売価実施日-４日
        if (!(int_sysdate <= int_senddate)) {
          JSONObject o = mu.getDbMessageObj("E11191", new String[] {});
          this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.TENBAIKADT, data);
          msg.add(o);
          return msg;

        }
        // ⑦予2.新規の場合
      } else if (isNewY2) {
        // マスタ変更予定日:処理日付＜ﾏｽﾀｰ変更日
        if (!(int_sysdate < int_yoyakudt)) {
          JSONObject o = mu.getDbMessageObj("E11190", new String[] {});
          this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.YOYAKUDT, data);
          msg.add(o);
          return msg;
        }
        // 店売価実施日:処理日付＜店売価実施日
        if (!(int_sysdate < int_tenbaikadt)) {
          JSONObject o = mu.getDbMessageObj("E11191", new String[] {});
          this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.TENBAIKADT, data);
          msg.add(o);
          return msg;
        }
        // 店売価実施日-４日＜ﾏｽﾀｰ変更日＜店売価実施日
        if (!(int_senddate < int_yoyakudt && int_yoyakudt < int_tenbaikadt)) {
          JSONObject o = mu.getDbMessageObj("E11192", new String[] {});
          this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.YOYAKUDT, data);
          msg.add(o);
          return msg;
        }


        // マスタ変更予定日:予約2のﾏｽﾀ変更日 >予約1のﾏｽﾀ変更日+4日
        Integer int_yoyakudt_1 = 0;
        if (yArray.size() > 0) {
          // 予約1のﾏｽﾀ変更日
          String txt_yoyakudt_1 = yArray.optJSONObject(0).optString(MSTSHNLayout.YOYAKUDT.getId());
          int_yoyakudt_1 = NumberUtils.toInt(CmnDate.dateFormat(CmnDate.getDayAddedDate(CmnDate.convDate(20 + txt_yoyakudt_1), 4), DATE_FORMAT.INP_YMD));
        }
        if (!(int_yoyakudt > int_yoyakudt_1)) {
          JSONObject o = mu.getDbMessageObj("E11193", new String[] {});
          this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.YOYAKUDT, data);
          msg.add(o);
          return msg;
        }

        // 予約の変更
      } else if (isChangeY1 || isChangeY2) {
        String txt_yoyakudt_ = ""; // 検索実行時のﾏｽﾀ変更日
        String txt_tenbaikadt_ = ""; // 検索実行時の店売価実施日
        if (isChangeY1 && yArray.size() > 0) {
          txt_yoyakudt_ = yArray.optJSONObject(0).optString(MSTSHNLayout.YOYAKUDT.getId()); // 検索実行時のﾏｽﾀ変更日
          txt_tenbaikadt_ = yArray.optJSONObject(0).optString(MSTSHNLayout.TENBAIKADT.getId()); // 検索実行時の店売価実施日

        } else if (isChangeY2 && yArray.size() > 1) {
          txt_yoyakudt_ = yArray.optJSONObject(1).optString(MSTSHNLayout.YOYAKUDT.getId()); // 検索実行時のﾏｽﾀ変更日
          txt_tenbaikadt_ = yArray.optJSONObject(1).optString(MSTSHNLayout.TENBAIKADT.getId()); // 検索実行時の店売価実施日
        }
        if (int_yoyakudt != NumberUtils.toInt(txt_yoyakudt_)) {
          JSONObject o = mu.getDbMessageObj("E11195", new String[] {});
          this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.YOYAKUDT, data);
          msg.add(o);
          return msg;
        }
        if (int_tenbaikadt != NumberUtils.toInt(txt_tenbaikadt_)) {
          JSONObject o = mu.getDbMessageObj("E11196", new String[] {});
          this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.TENBAIKADT, data);
          msg.add(o);
          return msg;
        }
      }
    }


    // 新規(正) 1.5 商品登録数は商品登録限度数テーブルの登録限度数を超えた場合は、エラー。
    if (isNew) {
      JSONArray shnchk_rows = this.getMstData(DefineReport.ID_SQL_SHN_CHK_UPDATECNT, new ArrayList<>(Arrays.asList(sysdate)));
      if (shnchk_rows.size() < 1) {
        JSONObject o = mu.getDbMessageObj("E11241", new String[] {});
        this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.SHNCD, data);
        msg.add(o);
        return msg;
      }
    }

    // 新規(正) 1.6 入力されたメーカーコードがメーカーマスタに存在しない、かつ、ソースコードから取得したメーカーコードとも異なる場合は、エラー。
    // 空白の場合はソースコードから取得した値を登録する。設定されている場合は、その値を登録する。
    // 変更(正) 1.4 入力されたメーカーコードがメーカーマスタに存在しない、かつ、ソースコードから取得したメーカーコードとも異なる場合は、エラー。
    // また、ソースコードが入力されている場合に、メーカーコードが空白の場合、エラー。ただし、衣料使い回しフラグが１の場合はメーカーコードが空白の場合もエラーとしない。（7/14）
    // 新規(予) 1.5 入力されたメーカーコードがメーカーマスタに存在しない、かつ、ソースコードから取得したメーカーコードとも異なる場合は、エラー。
    // また、ソースコードが入力されている場合に、メーカーコードが空白の場合、エラー。
    String txt_makercd = data.optString(MSTSHNLayout.MAKERCD.getId()); // 入力されたメーカーコード
    if (isCsvUpload) {
      if (!StringUtils.isEmpty(txt_makercd) && Integer.valueOf(txt_makercd) == 0) {
        txt_makercd = "";
      }
    }
    String txt_makercd_new = txt_makercd;
    String chk_iryoreflg = data.optString(MSTSHNLayout.IRYOREFLG.getId());
    if (srccds.size() > 0) {
      // ソースコードからメーカーコードの取得
      // 添付資料（MD03112501）のメーカーコードの取得方法
      String value = srccds.get(0).optString(MSTSRCCDLayout.SRCCD.getId());
      String kbn = srccds.get(0).optString(MSTSRCCDLayout.SOURCEKBN.getId());
      ArrayList<String> paramData = new ArrayList<>(Arrays.asList(value, kbn, txt_bmncd));
      JSONArray makercd_row = this.getMstData(DefineReport.ID_SQL_MD03112501, paramData); // ソースコードからメーカーコード取得
      String txt_makercd_src = "";
      if (makercd_row.size() > 0) {
        txt_makercd_src = makercd_row.optJSONObject(0).optString("VALUE");
      }
      // メーカーコードが入力有の場合
      if (txt_makercd.length() > 0) {
        // 入力されたメーカーコードがメーカーマスタに存在しない、かつ、ソースコードから取得したメーカーコードとも異なる場合
        if (!this.checkMstExist(DefineReport.InpText.MAKERCD.getObj(), txt_makercd) && !StringUtils.equals(txt_makercd_src, txt_makercd)) {
          JSONObject o = mu.getDbMessageObj("E11320", new String[] {});
          this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.MAKERCD, data);
          msg.add(o);
          return msg;
        }

      }
      // ソースコードより取得したメーカーコードを設定。
      if (txt_makercd_src.length() >= 0) {
        txt_makercd_new = txt_makercd_src;
      }
    } else {
      // ソースコードの入力がない場合
      if (txt_makercd.length() == 0) {
        // メーカーコードが空白の場合はデフォルト値を適用する。
        txt_makercd_new = txt_bmncd + "00001";
      }
    }

    // ①標準仕入先コードが「1」の場合のみ、「保温区分」、「デリカワッペン」、「取扱区分」は選択不可
    // ②標準仕入先コードが「1」以外の場合、「保温区分」、「デリカワッペン」、「取扱区分」は必須入力。
    String sel_k_honkb = data.optString(MSTSHNLayout.K_HONKB.getId()); // 保温区分
    String sel_k_wapnflg = data.optString(MSTSHNLayout.K_WAPNFLG_R.getId()); // デリカワッペン
    String sel_k_torikb = data.optString(MSTSHNLayout.K_TORIKB.getId()); // 取扱区分

    if (StringUtils.equals("20", txt_bmncd) || StringUtils.equals("23", txt_bmncd) || StringUtils.equals("31", txt_bmncd) || StringUtils.equals("70", txt_bmncd)
        || StringUtils.equals("73", txt_bmncd)) {

      if (StringUtils.equals("1", txt_ssircd.trim())) {
        MSTSHNLayout errField = null;

        if (StringUtils.isNotEmpty(sel_k_honkb)) {
          errField = MSTSHNLayout.K_HONKB;
        } else if (StringUtils.isNotEmpty(sel_k_wapnflg)) {
          errField = MSTSHNLayout.K_WAPNFLG_R;
        } else if (StringUtils.isNotEmpty(sel_k_torikb)) {
          errField = MSTSHNLayout.K_TORIKB;
        }

        if (errField != null) {
          JSONObject o = mu.getDbMessageObj("E30012", new String[] {"標準仕入先コードが「000001」以外の場合、「保温区分」、「デリカワッペン」、「取扱区分」は空欄"});
          this.setCsvshnErrinfo(o, errTbl, errField, data);
          msg.add(o);
          return msg;
        }
      } else {
        MSTSHNLayout errField = null;
        if (StringUtils.isEmpty(sel_k_honkb)) {
          errField = MSTSHNLayout.K_HONKB;
        } else if (StringUtils.isEmpty(sel_k_wapnflg)) {
          errField = MSTSHNLayout.K_WAPNFLG_R;
        } else if (StringUtils.isEmpty(sel_k_torikb)) {
          errField = MSTSHNLayout.K_TORIKB;
        }

        if (errField != null) {
          JSONObject o = mu.getDbMessageObj("E30012", new String[] {"標準仕入先コードが「000001」以外の場合、「保温区分」、「デリカワッペン」、「取扱区分」"});
          this.setCsvshnErrinfo(o, errTbl, errField, data);
          msg.add(o);
          return msg;
        }
      }

    }


    // 変更(正) 1.5 部門マスタの評価方法区分が”２”（売価還元法）の部門で、「レギュラー本体売価」の変更があった場合、
    // ①衣料使いまわしフラグ”１”以外の場合、「マスタ更新可」ユーザー（「管理者」ユーザーを含む）だったら、登録ボタン押下時、「レギュラー売価が直接変更されました。本当に変更してよろしいですか？」のようなメッセージと「はい」「いいえ」のボタンをあわせて表示して更新を実行させる。
    // 「いいえ」なら、キャンセル。「はい」なら更新を実施。カーソルのデフォルトは「いいえ」に当てておく。
    // ※エラー条件の優先度は、他のチェックを終えてから最後に追加。
    // ②．衣料使いまわし区分＝１の時、
    // 全チェックがＯＫだった場合、登録日、変更日を本日日付にする
    // TODO:確認 ①②に関しては、登録画面ではJSで実施、CSV取込ではエラー扱いとしてCSVトラン登録
    if (isCsvUpload && isChange) { // 変更(正)
      JSONArray mstbmn_row = this.getMstData(DefineReport.ID_SQL_BUMON_C + DefineReport.ID_SQL_CMN_WHERE + DefineReport.ID_SQL_BMN_BUMON_WHERE + DefineReport.ID_SQL_BUMON_FOOTER_C,
          new ArrayList<>(Arrays.asList(txt_bmncd))); // 部門マスタから評価方法区分取得
      if (mstbmn_row.size() > 0) {
        String txt_rg_baikaam_ = seiJsonObject.optString(MSTSHNLayout.RG_BAIKAAM.getId());
        if (DefineReport.ValKbn504.VAL2.getVal().equals(mstbmn_row.optJSONObject(0).optString("HYOKAKBN")) && NumberUtils.toInt(txt_rg_baikaam) != NumberUtils.toInt(txt_rg_baikaam_)) {
          if (!DefineReport.Values.ON.getVal().equals(chk_iryoreflg)) {
            JSONObject o = mu.getDbMessageObj("E11328", new String[] {});
            this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.RG_BAIKAAM, data);
            msg.add(o);
            return msg;
            // JSでのチェックの場合は"W20037"を表示
          } else {
            data.element(MSTSHNLayout.ADDDT.getId(), login_dt);
            data.element(MSTSHNLayout.UPDDT.getId(), login_dt);
          }
        }
      }
    }
    // エラーがなかった場合、新規時はデータ加工
    if (isCsvUpload) {
      // ユニットプライスを設定
      data.element(MSTSHNLayout.UP_YORYOSU.getId(), txt_up_yoryosu);
      data.element(MSTSHNLayout.UP_TYORYOSU.getId(), txt_up_tyoryosu);
      data.element(MSTSHNLayout.UP_TANIKBN.getId(), kbn113);

      // 税率区分
      data.element(MSTSHNLayout.ZEIRTKBN.getId(), sel_zeirtkbn);
      data.element(MSTSHNLayout.ZEIRTKBN_OLD.getId(), sel_zeirtkbn_old);

      // 取得メーカーコードを設定
      data.element(MSTSHNLayout.MAKERCD.getId(), txt_makercd_new);
      dataArray.element(0, data);
    }
    if (isNew || (!StringUtils.isEmpty(szStcdShikakari) && szStcdShikakari.equals("04"))) {
      // 画面orCSVの商品コード付番による新規登録の場合
      if (StringUtils.isEmpty(txt_shncd) || !txt_shncd.equals(txt_shncd_new)) {
        // 取得商品コードを設定
        data.element(MSTSHNLayout.SHNCD.getId(), txt_shncd_new);

        // 2.ソースマスタ
        for (int i = 0; i < dataArraySRCCD.size(); i++) {
          dataArraySRCCD.getJSONObject(i).element(MSTSRCCDLayout.SHNCD.getId(), txt_shncd_new);
        }
        // 3.仕入グループ商品マスタ
        for (int i = 0; i < dataArrayTENGP1.size(); i++) {
          dataArrayTENGP1.getJSONObject(i).element(MSTSIRGPSHNLayout.SHNCD.getId(), txt_shncd_new);
        }
        // 4.売価コントロールマスタ
        for (int i = 0; i < dataArrayTENGP2.size(); i++) {
          dataArrayTENGP2.getJSONObject(i).element(MSTBAIKACTLLayout.SHNCD.getId(), txt_shncd_new);
        }
        // 5.品揃グループマスタ
        for (int i = 0; i < dataArrayTENGP3.size(); i++) {
          dataArrayTENGP3.getJSONObject(i).element(MSTSHINAGPLayout.SHNCD.getId(), txt_shncd_new);
        }
        // 6.添加物
        for (int i = 0; i < dataArrayTENKABUTSU.size(); i++) {
          dataArrayTENKABUTSU.getJSONObject(i).element(MSTTENKABUTSULayout.SHNCD.getId(), txt_shncd_new);
        }
      }

      // 取得販売コードを設定
      data.element(MSTSHNLayout.URICD.getId(), txt_uricd_new);
      dataArray.element(0, data);
    }

    // 登録処理正常終了後出力用メッセージ作成
    msgShnCd = txt_shncd_new;
    msgUriCd = txt_uricd_new;

    return msg;
  }

  public List<JSONObject> checkDataDel(boolean isNew, boolean isChange, boolean isYoyaku1, boolean isYoyaku2, boolean isCsvUpload, HashMap<String, String> map, User userInfo, String sysdate1,
      MessageUtility mu, JSONArray dataArray, // 対象情報（主要な更新情報）
      JSONArray dataArraySRCCD, // ソースコード
      JSONArray dataArrayTENGP3, // 品揃えグループ
      JSONArray dataArrayTENGP2, // 売価コントロール
      JSONArray dataArrayTENGP1, // 仕入グループ
      JSONArray dataArrayTENKABUTSU // 添加物
  ) {

    JSONArray msg = new JSONArray();


    // DB最新情報再取得
    String szYoyaku = "0";
    JSONArray yArray = new JSONArray();
    if (!isNew) {
      JSONArray array = getYoyakuJSONArray(map);
      szYoyaku = Integer.toString(array.size());
      yArray.addAll(array);
    }
    new JSONObject();
    if (isChange) {
      JSONArray array = getSeiJSONArray(map);
      array.optJSONObject(0);
    }


    NumberUtils.toInt(szYoyaku, 0);
    // ⑤予1.変更
    boolean isChangeY1 = isYoyaku1 && NumberUtils.toInt(szYoyaku, 0) > 0;
    NumberUtils.toInt(szYoyaku, 0);
    // ⑧予2.変更
    boolean isChangeY2 = isYoyaku2 && NumberUtils.toInt(szYoyaku, 0) > 1;


    JSONObject data = dataArray.optJSONObject(0);

    data.optString(MSTSHNLayout.SHNCD.getId());
    String txt_yoyakudt = data.optString(MSTSHNLayout.YOYAKUDT.getId());
    String txt_tenbaikadt = data.optString(MSTSHNLayout.TENBAIKADT.getId());
    data.optString(MSTSHNLayout.BMNCD.getId());

    String login_dt = sysdate1; // 処理日付
    String sysdate = login_dt; // 比較用処理日付

    RefTable errTbl = RefTable.MSTSHN;
    // 変更(正) 1.3 添付資料（MD03112701）の“テーブル削除の整理”と添付資料の（MD03111002）の“商品マスタ登録時 マスタ変更予定日、店売価実施日のチェック”を参照。
    // 新規(予) 1.4 添付資料（MD03111002）の“商品マスタ登録時 マスタ変更予定日、店売価実施日のチェック”を参照。
    // 変更(予) 1.3 添付資料（MD03112701）の“テーブル削除の整理”と添付資料の（MD03111002）の“商品マスタ登録時 マスタ変更予定日、店売価実施日のチェック”を参照。
    // 前提：CSVエラー修正の場合、エラー情報を削除するので常に削除可
    // 正 .変更
    if (isChange) {
      // 予約1がある場合
      if (NumberUtils.toInt(szYoyaku, 0) > 0) {
        // E11179 予約に登録がある場合は削除できません。 0 E
        JSONObject o = mu.getDbMessageObj("E11179", new String[] {});
        this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.SHNCD, data);
        msg.add(o);
        return msg;
      }
      // 予1.変更
    } else if (isChangeY1) {
      // 予約2がある場合
      if (NumberUtils.toInt(szYoyaku, 0) > 1) {
        // E11231 予約2に登録がある場合は削除できません。 0 E
        JSONObject o = mu.getDbMessageObj("E11231", new String[] {});
        this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.SHNCD, data);
        msg.add(o);
        return msg;
      }
    }

    if (isYoyaku1 || isYoyaku2) {
      // * 店売価実施日-４日＜ﾏｽﾀｰ変更日＜店売価実施日＆処理日付＜ﾏｽﾀｰ変更日＆処理日付＜店売価実施日
      // 送信日=店売価実施日-４日
      Integer int_sysdate = NumberUtils.toInt(sysdate);
      Integer int_senddate = NumberUtils.toInt(CmnDate.dateFormat(CmnDate.getDayAddedDate(CmnDate.convInpDate(txt_tenbaikadt), -4), DATE_FORMAT.DEFAULT_DATE));
      Integer int_yoyakudt = NumberUtils.toInt(txt_yoyakudt);
      Integer int_tenbaikadt = NumberUtils.toInt(txt_tenbaikadt);

      // 予約の変更
      if (isChangeY1 || isChangeY2) {
        // 処理日付＝＜送信日（店売価実施日-4日） であれば可能
        if (!(int_sysdate <= int_senddate)) {
          // E11157 商品予約削除可能期間以外の場合、 削除操作は出来ません。 0 E
          JSONObject o = mu.getDbMessageObj("E11157", new String[] {});
          this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.TENBAIKADT, data);
          msg.add(o);
          return msg;
        }

        // 変更にもかかわらず、キー項目が変更されていたらエラー
        String txt_yoyakudt_ = ""; // 検索実行時のﾏｽﾀ変更日
        String txt_tenbaikadt_ = ""; // 検索実行時の店売価実施日
        if (isChangeY1 && yArray.size() > 0) {
          txt_yoyakudt_ = yArray.optJSONObject(0).optString(MSTSHNLayout.YOYAKUDT.getId()); // 検索実行時のﾏｽﾀ変更日
          txt_tenbaikadt_ = yArray.optJSONObject(0).optString(MSTSHNLayout.TENBAIKADT.getId()); // 検索実行時の店売価実施日

        } else if (isChangeY2 && yArray.size() > 1) {
          txt_yoyakudt_ = yArray.optJSONObject(1).optString(MSTSHNLayout.YOYAKUDT.getId()); // 検索実行時のﾏｽﾀ変更日
          txt_tenbaikadt_ = yArray.optJSONObject(1).optString(MSTSHNLayout.TENBAIKADT.getId()); // 検索実行時の店売価実施日
        }
        if (int_yoyakudt != NumberUtils.toInt(txt_yoyakudt_)) {
          // E11195 予約のマスタ変更日変更不可 0 E
          JSONObject o = mu.getDbMessageObj("E11195", new String[] {});
          this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.YOYAKUDT, data);
          msg.add(o);
          return msg;
        }
        if (int_tenbaikadt != NumberUtils.toInt(txt_tenbaikadt_)) {
          // E11196 予約の店売価実施日変更不可 0 E
          JSONObject o = mu.getDbMessageObj("E11196", new String[] {});
          this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.TENBAIKADT, data);
          msg.add(o);
          return msg;
        }
      } else {
        // EX1095 削除できません。 0 E
        JSONObject o = mu.getDbMessageObj("EX1095", new String[] {});
        this.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.SHNCD, data);
        msg.add(o);
        return msg;
      }
    }
    return msg;
  }


  private String isAbleReadtmptn(JSONObject data, String txt_bmncd) {
    String txt_readtmptn = data.optString(MSTSHNLayout.READTMPTN.getId());
    // 02,09,15,04,05,06,10,11,20,23,34,43,88,13,26,27部門以外は、以下の組合せの場合はエラーとする。
    // CCRが本処理に対応できない為、要望によりMDMでは全ての部門の入力可とする。
    // if(!ArrayUtils.contains(new Integer[]{2,9,15,4,5,6,10,11,20,23,34,43,88,13,26,27},
    // NumberUtils.toInt(txt_bmncd))
    // && ArrayUtils.contains(new String[]{"10","20","21","30"}, txt_readtmptn)){
    // return "EX1005";
    // }
    // それ以外の場合、組合せチェック
    if (StringUtils.isNotEmpty(txt_readtmptn)) {
      String chk_hat_frikbn = data.optString(MSTSHNLayout.HAT_FRIKBN.getId());
      String chk_hat_satkbn = data.optString(MSTSHNLayout.HAT_SATKBN.getId());
      String chk_hat_sunkbn = data.optString(MSTSHNLayout.HAT_SUNKBN.getId());
      if (StringUtils.equals(txt_readtmptn, "10") && !DefineReport.Values.ON.getVal().equals(chk_hat_sunkbn)) {
        return "E11230";
      } else if (StringUtils.equals(txt_readtmptn, "20") && !DefineReport.Values.ON.getVal().equals(chk_hat_satkbn)) {
        return "E11230";
      } else if (StringUtils.equals(txt_readtmptn, "21") && !DefineReport.Values.ON.getVal().equals(chk_hat_sunkbn)) {
        return "E11230";
      } else if (StringUtils.equals(txt_readtmptn, "30") && !DefineReport.Values.ON.getVal().equals(chk_hat_frikbn)) {
        return "E11230";
      }
    }
    return null;
  }

  private double calcNeireRit(String txt_genkaam, String txt_baikaam) {
    if ((txt_genkaam.length() == 0) || (txt_baikaam.length() == 0))
      return 0;

    double genka = NumberUtils.toDouble(txt_genkaam);
    double baika = NumberUtils.toDouble(txt_baikaam);

    // （本体売価－原価）÷本体売価で、小数点以下3位切り捨て, 第2位まで求める。上限98%
    // ただし、商品種別で包材、消耗品、コメント、催事テナントの時はチェックしない。
    return Math.floor((baika - genka) / baika * 10000) / 100;
  }

  /**
   * CSVエラー情報セット
   *
   * @throws Exception
   */
  public void setCsvshnErrinfo(JSONObject o, String errcd, String errtbl, String errfld, String errvl) {
    o.put(CSVSHNLayout.ERRCD.getCol(), errcd); // TODO
    o.put(CSVSHNLayout.ERRTBLNM.getCol(), errtbl);
    o.put(CSVSHNLayout.ERRFLD.getCol(), errfld);
    o.put(CSVSHNLayout.ERRVL.getCol(), MessageUtility.leftB(StringUtils.trim(errvl + " " + o.optString(MessageUtility.MSG)), 100));
  }

  /**
   * CSVエラー情報セット
   *
   * @throws Exception
   */
  public void setCsvshnErrinfo(JSONObject o, RefTable errtbl, MSTLayout errfld, String errvl) {
    this.setCsvshnErrinfo(o, "0", errtbl.getTxt(), errfld.getTxt(), errvl);
  }

  /**
   * CSVエラー情報セット
   *
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
    String szSelShncd = map.get("SEL_SHNCD"); // 検索商品コード


    JSONArray array = new JSONArray();
    if (!szSelShncd.isEmpty()) {
      ArrayList<String> paramData = new ArrayList<>();
      paramData.add(szSelShncd.replace("-", "") + "%");

      StringBuffer sbSQL = new StringBuffer();
      sbSQL.append(" select ");
      for (MSTSHNLayout itm : MSTSHNLayout.values()) {
        if (itm.getNo() > 1) {
          sbSQL.append(" ,");
        }
        sbSQL.append(itm.getCol() + " as " + itm.getId());
      }
      sbSQL.append(" from INAMS.MSTSHN where SHNCD like ? and COALESCE(UPDKBN, 0) <> 1");

      new ItemList();
      @SuppressWarnings("static-access")
      JSONArray array0 = ItemList.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);
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
    String szSelShncd = map.get("SEL_SHNCD"); // 検索商品コード

    JSONArray array = new JSONArray();
    if (!szSelShncd.isEmpty()) {
      ArrayList<String> paramData = new ArrayList<>();
      paramData.add(szSelShncd.replace("-", "") + "%");

      StringBuffer sbSQL = new StringBuffer();
      sbSQL.append(" select ");
      for (MSTSHNLayout itm : MSTSHNLayout.values()) {
        if (itm.getNo() > 1) {
          sbSQL.append(" ,");
        }
        sbSQL.append(itm.getCol() + " as " + itm.getId());
      }
      sbSQL.append(" from INAMS.MSTSHN_Y where SHNCD like ? and COALESCE(UPDKBN, 0) <> 1 order by YOYAKUDT");

      new ItemList();
      @SuppressWarnings("static-access")
      JSONArray array0 = ItemList.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);
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
    new ItemList();
    String sqlColCommand = "SELECT INAMS.nextval('SEQ002') AS \"1\"";
    @SuppressWarnings("static-access")
    JSONArray array = ItemList.selectJSONArray(sqlColCommand, null, Defines.STR_JNDI_DS);
    String value = "";
    if (array.size() > 0) {
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
    new ItemList();
    // 配列準備
    ArrayList<String> paramData = new ArrayList<>();
    String sqlcommand = "";

    if (((DefineReport.ValKbn143.VAL0.getVal().equals(ketakbn) && StringUtils.length(inpshncd) == 0)
        || (DefineReport.ValKbn143.VAL2.getVal().equals(ketakbn) && StringUtils.length(inpshncd) == 4 && NumberUtils.isNumber(inpshncd))) && NumberUtils.isNumber(bmncd)) {
      paramData.add(inpshncd);
      paramData.add(ketakbn);
      paramData.add(bmncd);
      paramData.add(bmncd);
      sqlcommand = DefineReport.ID_SQL_MD03100901;
    }
    String value = "";
    if (sqlcommand.length() > 0) {
      @SuppressWarnings("static-access")
      JSONArray array = ItemList.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
      if (array.size() > 0) {
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
    new ItemList();
    // 配列準備
    ArrayList<String> paramData = new ArrayList<>();
    String sqlcommand = "";
    String sqlcommand2 = "";
    JSONObject returndata = new JSONObject();
    new JSONObject();

    // 入力無しの場合部門をキーとして
    if (DefineReport.ValKbn143.VAL0.getVal().equals(ketakbn)) {
      paramData.add(StringUtils.right("00" + bmncd, 2) + '%');
    } else {
      paramData.add(inpshncd + '%');
    }

    // 手入力の場合、付番管理テーブルは条件に含めず、空き番のみで商品コード取得（使い回し考慮） or 付番済みJAVA再チェックもこちら
    if (DefineReport.ValKbn143.VAL1.getVal().equals(ketakbn)) {
      sqlcommand = DefineReport.ID_SQL_MD03100901.replace("@W", "");
      JSONObject data = new JSONObject();
      if (sqlcommand.length() > 0) {
        @SuppressWarnings("static-access")
        JSONArray array = ItemList.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
        if (array.size() > 0) {
          data = array.optJSONObject(0);
          if (StringUtils.isNotEmpty(data.optString("VALUE"))) {
            SHCD = data.optString("VALUE");
            returndata = data;
          }
        }
      }

    } else {
      String searchCommand = DefineReport.ID_SQL_MD03100901.replace("@W", StringUtils.replace(DefineReport.ID_SQL_MD03100901_WHERE_AUTO, "@C", "T1.SHNCD"));

      sqlcommand2 = "update INAAD.SYSSHNCD_AKI set USEFLG = '1', UPDDT = current_timestamp where USEFLG = 0 and SHNCD = (" + searchCommand + ")";
      sqlcommand = "" + searchCommand + "";

      boolean dowhile = true;
      JSONObject data = new JSONObject();
      int upCount = 0;

      // コネクション
      Connection con = null;
      while (dowhile) {
        // 取得したい空き番の取得と登録を同時に行う
        if (sqlcommand.length() > 0) {
          data = new JSONObject();
          @SuppressWarnings("static-access")
          JSONArray array = ItemList.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
          if (array.size() > 0) {
            data = array.optJSONObject(0);
            if (StringUtils.isNotEmpty(data.optString("VALUE"))) {
              SHCD = data.optString("VALUE");
              try {
                // コネクションの取得
                con = DBConnection.getConnection(this.JNDIname);
                con.setAutoCommit(false);
                upCount += updateBySQL(sqlcommand2, paramData, con);

                // 更新0件 --> 登録失敗
                if (upCount < 1) {
                  throw new NullPointerException();

                }
                con.commit();
                con.close();
              } catch (Exception e) {
                /* 接続解除 */
                if (con != null) {
                  try {
                    con.rollback();
                    con.close();
                  } catch (SQLException e1) {
                    e1.printStackTrace();
                  }
                }
                e.printStackTrace();
              }
              dowhile = false;
            }
          }
        }

        // 空き番を取得出来なかった場合
        if (dowhile) {
          @SuppressWarnings("static-access")
          JSONArray array = ItemList.selectJSONArray(searchCommand, paramData, Defines.STR_JNDI_DS);
          if (array.size() == 0 || StringUtils.isEmpty(array.optJSONObject(0).optString("VALUE"))) {
            // 取得できる空き番が存在しない場合は取得処理を終了する。
            dowhile = false;
          }
        }
      }
      returndata = data;
    }
    return returndata;
  }


  /**
   * 新規販売コード取得処理
   *
   * @throws Exception
   */
  public JSONObject getNewURICD(String shncd) {
    new ItemList();
    // 配列準備
    ArrayList<String> paramData = new ArrayList<>();
    String sqlcommand = "";
    String sqlcommand2 = "";
    int upCount = 0;
    int Count = 0;

    // コネクション
    Connection con = null;

    // 衣料の場合
    if (StringUtils.startsWithAny(shncd, new String[] {"13", "27"})) {
      paramData.add(shncd);
      paramData.add(shncd);
      // String chksqlcommand = StringUtils.replace(DefineReport.ID_SQL_MD03100901_EXISTS_AUTO, "@C",
      // "?"); // 20210729 No.158 変更
      String chksqlcommand = StringUtils
          .replace("select 'X' from INAAD.SYSSHNCD_FU T2 where CAST(left(@C, 2) AS SIGNED ) = T2.BMNCD and NOT CAST(substr(@C, 3, 5) AS SIGNED ) between T2.STARTNO and T2.ENDNO", "@C", "?");

      @SuppressWarnings("static-access")
      JSONArray array = ItemList.selectJSONArray(chksqlcommand, paramData, Defines.STR_JNDI_DS);

      paramData = new ArrayList<>();
      // 衣料使い回しの範囲の場合
      if (array.size() > 0) {
        paramData.add(shncd);
        sqlcommand = DefineReport.ID_SQL_MD03100902_USE;
        sqlcommand2 = "update INAAD.SYSURICD_AKI set USEFLG = 1, UPDDT = current_timestamp where URICD = (select value from (" + sqlcommand + "))";
        sqlcommand = "" + sqlcommand + "";

        @SuppressWarnings("static-access")
        JSONArray array2 = ItemList.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);

        if (array2.size() > 0) {
          JSONObject data = array2.optJSONObject(0);
          if (StringUtils.isNotEmpty(data.optString("VALUE"))) {
            try {
              // コネクションの取得
              con = DBConnection.getConnection(this.JNDIname);
              con.setAutoCommit(false);
              upCount += updateBySQL(sqlcommand2, paramData, con);

              // 更新0件 --> 登録失敗
              if (upCount < 1) {
                throw new NullPointerException();
              }
              con.commit();
              con.close();
            } catch (Exception e) {
              /* 接続解除 */
              if (con != null) {
                try {
                  con.rollback();
                  con.close();
                } catch (SQLException e1) {
                  e1.printStackTrace();
                }
              }
              e.printStackTrace();
            }
            if (DefineReport.ID_DEBUG_MODE)
              System.out.println(MessageUtility.getMessage(Msg.S00006.getVal(), new String[] {"取得登録：販売コード空き番管理テーブル", Integer.toString(array.size())}));
            return data;
          }
        }
      }
    }

    // 販売コード付番管理テーブルからを取得
    int STARTNO = 0; // 開始番号
    int ENDNO = 0; // 終了番号
    int SUMINO = 0; // 付番済番号
    boolean useAki = false;

    paramData = new ArrayList<>();
    sqlcommand = "select STARTNO, ENDNO, SUMINO from INAAD.SYSURICD_FU LIMIT 1";
    if (sqlcommand.length() > 0) {
      @SuppressWarnings("static-access")
      JSONArray array = ItemList.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
      if (array.size() > 0) {
        JSONObject data = array.optJSONObject(0);

        STARTNO = data.optInt("STARTNO");
        ENDNO = data.optInt("ENDNO");
        SUMINO = data.optInt("SUMINO");
      }
    }

    if (ENDNO > SUMINO) {
      // 終了番号 > 付番済番号
      // 販売コード付番管理テーブルからデータを取得し、登録を行う。
      paramData = new ArrayList<>();
      sqlcommand2 = "update INAAD.SYSURICD_FU set SUMINO = SUMINO + 1 where ENDNO > SUMINO";
      sqlcommand = "select SUMINO as VALUE from INAAD.SYSURICD_FU  where ENDNO > SUMINO)";

      @SuppressWarnings("static-access")
      JSONArray array = ItemList.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);

      if (array.size() > 0) {
        JSONObject data = array.optJSONObject(0);
        if (StringUtils.isNotEmpty(data.optString("VALUE"))) {
          try {
            // コネクションの取得
            con = DBConnection.getConnection(this.JNDIname);
            con.setAutoCommit(false);
            upCount += updateBySQL(sqlcommand2, paramData, con);

            // 更新0件 --> 登録失敗
            if (upCount < 1) {
              throw new NullPointerException();
            }
            con.commit();
            con.close();
          } catch (Exception e) {
            /* 接続解除 */
            if (con != null) {
              try {
                con.rollback();
                con.close();
              } catch (SQLException e1) {
                e1.printStackTrace();
              }
            }
            e.printStackTrace();
          }
          if (DefineReport.ID_DEBUG_MODE)
            System.out.println(MessageUtility.getMessage(Msg.S00006.getVal(), new String[] {"取得登録：販売コード付番管理テーブル", Integer.toString(array.size())}));
          return data;
        } else {
          // 取得出来なかった場合(例：上限値999999を複数ユーザーで使用しようとした場合は、取得できないユーザーが出現する)
          useAki = true;
        }
      }
    }

    if (ENDNO == SUMINO || useAki) {
      // 終了番号 == 付番済番号
      // 販売コード空き番管理テーブルからデータを取得し、登録を行う。
      paramData = new ArrayList<>();
      paramData.add("" + STARTNO);
      paramData.add("" + ENDNO);
      sqlcommand2 =
          "update INAAD.SYSURICD_AKI set USEFLG = 1, UPDDT = current_timestamp where USEFLG = 0 and URICD = (select MIN(URICD) as URICD from (SELECT * FROM INAAD.SYSURICD_AKI) T1 where URICD between ? and ? and COALESCE(USEFLG, 0) <> 1)";
      sqlcommand = "select MIN(URICD) as VALUE from INAAD.SYSURICD_AKI where URICD between ? and ? and COALESCE(USEFLG, 0) <> 1";
      String searchCommand = DefineReport.ID_SQL_MD03100902;
      ArrayList<String> searchParamData = new ArrayList<>();

      boolean dowhile = true;
      JSONObject data = new JSONObject();
      while (dowhile) {
        // 取得したい空き番の取得と登録を同時に行う
        if (sqlcommand.length() > 0) {
          data = new JSONObject();
          @SuppressWarnings("static-access")
          JSONArray array = ItemList.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);

          if (array.size() > 0) {
            data = array.optJSONObject(0);
            if (StringUtils.isNotEmpty(data.optString("VALUE"))) {
              try {
                // コネクションの取得
                con = DBConnection.getConnection(this.JNDIname);
                con.setAutoCommit(false);
                upCount += updateBySQL(sqlcommand2, paramData, con);
                Count++;

                // 更新0件 --> 登録失敗
                if (upCount < 1 || Count > 1) {
                  throw new NullPointerException();
                }
                con.commit();
                con.close();
              } catch (Exception e) {
                /* 接続解除 */
                if (con != null) {
                  try {
                    con.rollback();
                    con.close();
                  } catch (SQLException e1) {
                    e1.printStackTrace();
                  }
                }
                e.printStackTrace();
              }
              if (DefineReport.ID_DEBUG_MODE)
                System.out.println(MessageUtility.getMessage(Msg.S00006.getVal(), new String[] {"取得登録：販売コード空き番管理テーブル", Integer.toString(array.size())}));
              dowhile = false;
            }
          }
        }

        // 空き番を取得出来なかった場合
        if (dowhile) {
          @SuppressWarnings("static-access")
          JSONArray array = ItemList.selectJSONArray(searchCommand, searchParamData, Defines.STR_JNDI_DS);
          if (array.size() == 0 || StringUtils.isEmpty(array.optJSONObject(0).optString("VALUE"))) {
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
    new ItemList();
    @SuppressWarnings("static-access")
    JSONArray array = ItemList.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
    return array;
  }

  /**
   * マスタ情報取得処理
   *
   * @throws Exception
   */
  public boolean checkMstExist(String outobj, String value) {
    new ItemList();
    // 配列準備
    ArrayList<String> paramData = new ArrayList<>();
    String sqlcommand = "";
    String dataLength = ""; // 複数データの存在チェックを行う場合に使用

    String tbl = "";
    String col = "";
    String rep = "";
    String whr = "";
    // 商品コード
    if (outobj.equals(DefineReport.InpText.SHNCD.getObj())) {
      tbl = "INAMS.MSTSHN";
      col = "SHNCD";
      whr = DefineReport.ID_SQL_CMN_WHERE2;
    }
    // メーカーコード
    if (outobj.equals(DefineReport.InpText.MAKERCD.getObj())) {
      tbl = "INAMS.MSTMAKER";
      col = "MAKERCD";
      whr = DefineReport.ID_SQL_CMN_WHERE2;
    }
    // 部門コード
    if (outobj.equals(DefineReport.InpText.BMNCD.getObj())) {
      tbl = "INAMS.MSTBMN";
      col = "BMNCD";
      whr = DefineReport.ID_SQL_CMN_WHERE2;
    }

    // 仕入先コード
    if (outobj.equals(DefineReport.InpText.SIRCD.getObj())) {
      tbl = "INAMS.MSTSIR";
      col = "SIRCD";
      whr = DefineReport.ID_SQL_CMN_WHERE2;
    }

    // 配送パターン仕入先
    if (outobj.equals("MSTHSPTNSIR") && value.length() > 1) {
      tbl = "INAMS.MSTHSPTNSIR";
      col = "right('00000'||SIRCD, 6)||right('00'||HSPTN, 3)";

      String[] vals = StringUtils.split(value, ",");
      for (String val : vals) {
        rep += ", ?";
        String cd = StringUtils.leftPad(val.split("-")[0], 6, "0") + StringUtils.leftPad(val.split("-")[1], 3, "0");
        paramData.add(cd);
      }
      rep = StringUtils.removeStart(rep, ",");

      if (paramData.size() > 0) {
        dataLength = "" + paramData.size();
      }
    }

    // 店別商品コード
    if (outobj.equals(DefineReport.InpText.TENSHNCD.getObj())) {
      tbl = "INAMS.MSTSHN";
      col = "SHNCD";
      whr = DefineReport.ID_SQL_CMN_WHERE2;
      String[] vals = StringUtils.split(value, ",");
      for (String val : vals) {
        rep += ", ?";
        paramData.add(val);
      }
      rep = StringUtils.removeStart(rep, ",");

      if (paramData.size() > 0) {
        dataLength = "" + paramData.size();
      }
    }

    // ソースコード
    if (outobj.equals(DefineReport.InpText.SRCCD.getObj())) {
      tbl = "INAMS.MSTSRCCD";
      col = "SRCCD";
    } else if (outobj.equals(DefineReport.InpText.SRCCD.getObj() + "_UPD")) {
      tbl = "INAMS.MSTSRCCD";
      col = "SHNCD";
      paramData.add(value.split(",")[0]);
      value = value.split(",")[1];
      whr = " AND SRCCD=?";
    }

    // ソース区分
    if (outobj.equals(DefineReport.MeisyoSelect.KBN136.getObj())) {
      tbl = "INAMS.MSTMEISHO";
      col = "MEISHOKBN";
      paramData.add(String.valueOf(DefineReport.MeisyoSelect.KBN136.getCd()));
      whr = " AND MEISHOCD=?";
    }

    if (tbl.length() > 0 && col.length() > 0) {
      if (paramData.size() > 0 && rep.length() > 0) {
        sqlcommand = DefineReport.ID_SQL_CHK_TBL.replace("@T", tbl).replaceAll("@C", col).replace("?", rep) + whr;
      } else {
        paramData.add(value);
        sqlcommand = DefineReport.ID_SQL_CHK_TBL.replace("@T", tbl).replaceAll("@C", col) + whr;
      }

      @SuppressWarnings("static-access")
      JSONArray array = ItemList.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
      if (array.size() > 0 && NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) > 0) {
        if (StringUtils.isNotEmpty(dataLength)) {
          if (NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) == Integer.parseInt(dataLength)) {
            return true;
          }
        } else {
          return true;
        }

      }
    }
    return false;
  }


  /**
   * マスタ店グループ系情報取得処理
   *
   * @param tengp3s_
   *
   * @throws Exception
   */
  // 要修正
  public String[] checkMsttgpExist(TreeSet<String> tengpcd, String gpkbn, String bmncd, String areakbn) {
    new ItemList();
    // 配列準備
    ArrayList<String> paramData = new ArrayList<>();
    String sqlcommand = "";

    String tbl = "INAMS.MSTSHNTENGP";
    String col = "TENGPCD";
    TreeSet<String> tengpcdNum = new TreeSet<>();

    String[] errcds = new String[] {};
    if (tengpcd.size() > 0) {
      String rep = "";
      for (String val : tengpcd) {
        rep += ", ?";
        paramData.add(val);
        if (StringUtils.isNumeric(val)) {
          tengpcdNum.add("" + Integer.parseInt(val));
        }

      }
      rep = StringUtils.removeStart(rep, ",");
      paramData.add(gpkbn);
      paramData.add(bmncd);
      paramData.add(areakbn);

      sqlcommand = DefineReport.ID_SQL_CHK_TBL_SEL.replace("@T", tbl).replaceAll("@C", col).replace("?", rep) + " and GPKBN = ? and BMNCD = ? and AREAKBN = ?";

      @SuppressWarnings("static-access")
      JSONArray array = ItemList.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);

      for (int i = 0; i < array.size(); i++) {
        String val = array.optJSONObject(i).optString("VALUE");
        if (tengpcdNum.contains(val)) {
          tengpcdNum.remove(val);
        }
      }

      Iterator<String> tengpcdNums = tengpcdNum.iterator();
      for (int i = 0; i < tengpcdNum.size(); i++) {
        errcds = (String[]) ArrayUtils.add(errcds, tengpcdNums.next());
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
    new ItemList();
    // 配列準備
    ArrayList<String> paramData = new ArrayList<>();
    String sqlcommand = "";

    paramData.add(StringUtils.defaultIfEmpty(bmncd, "-1"));
    paramData.add(StringUtils.defaultIfEmpty(daicd, "-1"));
    paramData.add(StringUtils.defaultIfEmpty(chucd, "-1"));
    paramData.add(StringUtils.defaultIfEmpty(shocd, "-1"));
    paramData.add(StringUtils.defaultIfEmpty(sshocd, "-1"));
    sqlcommand = StringUtils.replace(DefineReport.ID_SQL_BMN_CHK, "@", tbl_suffix);

    @SuppressWarnings("static-access")
    JSONArray array = ItemList.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);


    String messageId = "";
    MSTSHNLayout col = null;
    String val = "";
    if (!this.isEmptyVal(bmncd, true) && StringUtils.isEmpty(array.optJSONObject(0).optString("1"))) {
      messageId = "E11044";
      if (StringUtils.equals(tbl_suffix, "_YOT")) {
        col = MSTSHNLayout.YOT_BMNCD;
      } else if (StringUtils.equals(tbl_suffix, "_URI")) {
        col = MSTSHNLayout.URI_BMNCD;
      } else {
        col = MSTSHNLayout.BMNCD;
      }
      val = bmncd;
    } else if (!this.isEmptyVal(daicd, true) && StringUtils.isEmpty(array.optJSONObject(0).optString("2"))) {
      messageId = "E11135";
      if (StringUtils.equals(tbl_suffix, "_YOT")) {
        col = MSTSHNLayout.YOT_DAICD;
      } else if (StringUtils.equals(tbl_suffix, "_URI")) {
        col = MSTSHNLayout.URI_DAICD;
      } else {
        col = MSTSHNLayout.DAICD;
      }
      val = daicd;
    } else if (!this.isEmptyVal(chucd, true) && StringUtils.isEmpty(array.optJSONObject(0).optString("3"))) {
      messageId = "E11136";
      if (StringUtils.equals(tbl_suffix, "_YOT")) {
        col = MSTSHNLayout.YOT_CHUCD;
      } else if (StringUtils.equals(tbl_suffix, "_URI")) {
        col = MSTSHNLayout.URI_CHUCD;
      } else {
        col = MSTSHNLayout.CHUCD;
      }
      val = chucd;
    } else if (!this.isEmptyVal(shocd, true) && StringUtils.isEmpty(array.optJSONObject(0).optString("4"))) {
      messageId = "E11137";
      if (StringUtils.equals(tbl_suffix, "_YOT")) {
        col = MSTSHNLayout.YOT_SHOCD;
      } else if (StringUtils.equals(tbl_suffix, "_URI")) {
        col = MSTSHNLayout.URI_SHOCD;
      } else {
        col = MSTSHNLayout.SHOCD;
      }
      val = shocd;
    }

    JSONObject o = new JSONObject();
    if (col != null) {
      o = mu.getDbMessageObj(messageId, new String[] {});
      this.setCsvshnErrinfo(o, errTbl, col, val);
    }
    return o;
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
  public JSONObject createSqlMSTSHN(String userId, String btnId, JSONObject data, TblType tbl, SqlType sql) {
    JSONObject result = new JSONObject();

    // 更新情報
    ArrayList<String> prmData = new ArrayList<>();
    String values = "", names = "";
    int colNum = mstshn_col_num;
    if (TblType.JNL.getVal() == tbl.getVal()) {
      colNum += 5;
    }
    if (TblType.CSV.getVal() == tbl.getVal()) {
      colNum += csvshn_add_data.length;
    }
    // 特殊変換項目
    String shnkn = StringUtils.trim(data.optString(MSTSHNLayout.SHNKN.getId()));
    if (StringUtils.isEmpty(shnkn)) {
      shnkn = MessageUtility.HanToZen(data.optString(MSTSHNLayout.SHNAN.getId()));
    }
    for (int i = 1; i <= colNum; i++) {
      String col = "F" + i;
      String val = StringUtils.trim(data.optString(col));
      if (i == MSTSHNLayout.SHNKN.getNo()) { // 商品名（漢字）:未入力またはスペースの場合、商品名（カナ）項目を全角変換して設定。
        val = shnkn;
      } else if (i == MSTSHNLayout.RECEIPTAN.getNo() && StringUtils.isEmpty(val)) { // レシート名（カナ）:半角大文字。未入力またはスペースの場合、商品名（カナ）項目をコピーする
        val = data.optString(MSTSHNLayout.SHNAN.getId());
      } else if (i == MSTSHNLayout.RECEIPTKN.getNo() && StringUtils.isEmpty(val)) { // レシート名（漢字）:全角。未入力またはスペースの場合、商品名（漢字）項目をコピーする
        val = shnkn;
      } else if (i == MSTSHNLayout.PCARDKN.getNo() && StringUtils.isEmpty(val)) { // プライスカード商品名称（漢字）:全角。未入力またはスペースの場合、商品名（漢字）項目をコピーする
        val = shnkn;
      } else if (i == MSTSHNLayout.POPKN.getNo() && StringUtils.isEmpty(val)) { // POP名称:全角。未入力またはスペースの場合、商品名（漢字）項目をコピーする
        val = shnkn;
      } else if (i == MSTSHNLayout.KASANARICD.getNo() && StringUtils.isEmpty(val)) { // 重なりコード:画面重なりコードに何も入力がない場合は、半角スペースを登録する。
        val = " ";
      } else if (i == MSTSHNLayout.SHUBETUCD.getNo() && StringUtils.isEmpty(val)) { // 種別コード:画面種別コードに何も入力がない場合は、半角スペースを登録する。
        val = " ";
      } else if (i == MSTSHNLayout.KIKANKBN.getNo() && StringUtils.isEmpty(val)) { // 期間にデフォルト値"D"を設定する。
        val = "D";
      } else if (i == MSTSHNLayout.PARENTCD.getNo() && StringUtils.isEmpty(val)) { // 親商品コードに"00000000"を設定する。
        val = "00000000";
      } else if (i == MSTSHNLayout.IRYOREFLG.getNo()) { // 衣料使い回しフラグの場合"0"を設定
        val = "0";
      } else if (i == MSTSHNLayout.ELPFLG.getNo() && StringUtils.isEmpty(val)) { // フラグ情報_ELPがnullの場合"0"を設定
        val = "0";
      } else if (i == MSTSHNLayout.BELLMARKFLG.getNo() && StringUtils.isEmpty(val)) { // フラグ情報_ベルマークがnullの場合"0"を設定
        val = "0";
      } else if (i == MSTSHNLayout.RECYCLEFLG.getNo() && StringUtils.isEmpty(val)) { // フラグ情報_リサイクルがnullの場合"0"を設定
        val = "0";
      } else if (i == MSTSHNLayout.ECOFLG.getNo() && StringUtils.isEmpty(val)) { // フラグ情報_エコマークがnullの場合"0"を設定
        val = "0";
      } else if (i == MSTSHNLayout.HZI_RECYCLE.getNo() && StringUtils.isEmpty(val)) { // 包材リサイクル対象がnullの場合"0"を設定
        val = "0";
      } else if (i == MSTSHNLayout.PCARD_OPFLG.getNo() && StringUtils.isEmpty(val)) { // プライスカード出力有無がnullの場合"0"を設定
        val = "0";
      } else if (i == MSTSHNLayout.HAT_MONKBN.getNo() && StringUtils.isEmpty(val)) { // 発注曜日_月がnullの場合"0"を設定
        val = "0";
      } else if (i == MSTSHNLayout.HAT_TUEKBN.getNo() && StringUtils.isEmpty(val)) { // 発注曜日_火がnullの場合"0"を設定
        val = "0";
      } else if (i == MSTSHNLayout.HAT_WEDKBN.getNo() && StringUtils.isEmpty(val)) { // 発注曜日_水がnullの場合"0"を設定
        val = "0";
      } else if (i == MSTSHNLayout.HAT_THUKBN.getNo() && StringUtils.isEmpty(val)) { // 発注曜日_木がnullの場合"0"を設定
        val = "0";
      } else if (i == MSTSHNLayout.HAT_FRIKBN.getNo() && StringUtils.isEmpty(val)) { // 発注曜日_金がnullの場合"0"を設定
        val = "0";
      } else if (i == MSTSHNLayout.HAT_SATKBN.getNo() && StringUtils.isEmpty(val)) { // 発注曜日_土がnullの場合"0"を設定
        val = "0";
      } else if (i == MSTSHNLayout.HAT_SUNKBN.getNo() && StringUtils.isEmpty(val)) { // 発注曜日_日がnullの場合"0"を設定
        val = "0";
      } else if (i == MSTSHNLayout.TOROKUMOTO.getNo()) { // 登録元
        if (DefineReport.Button.ERR_CHANGE.getObj().equals(btnId)) {
          val = "1";
        } else if (StringUtils.isEmpty(val) || !val.equals("1")) {
          val = "0";
        }
      } else if (i == MSTSHNLayout.UPDKBN.getNo()) { // 更新区分
        val = DefineReport.ValUpdkbn.NML.getVal();
      } else if (i == MSTSHNLayout.SENDFLG.getNo()) { // 送信フラグ
        val = DefineReport.Values.SENDFLG_UN.getVal();
      } else if (i == MSTSHNLayout.OPERATOR.getNo()) { // オペレータ
        val = userId;
      }

      // カナ名:全角。未入力またはスペースの場合、商品名（漢字）項目をコピーする
      // 漢字名:
      // 商品名称:名称マスタからリストを取得する（名称コード区分=118）。初期値は名称マスタの名称コード=0の項目に設定する。
      // POP名称:全角。

      // ジャーナル
      if (TblType.JNL.getVal() == tbl.getVal()) {
        if (StringUtils.equals(col, JNLSHNLayout.SEQ.getId2())) { // SEQ(ジャーナル用)
          val = jnlshn_seq;
        } else if (StringUtils.equals(col, JNLSHNLayout.INF_OPERATOR.getId2())) { // 更新情報_オペレータ(ジャーナル用)
          val = userId;
        } else if (StringUtils.equals(col, JNLSHNLayout.INF_TABLEKBN.getId2())) { // 更新情報_テーブル区分(ジャーナル用)
          val = jnlshn_tablekbn;
        } else if (StringUtils.equals(col, JNLSHNLayout.INF_TRANKBN.getId2())) { // 更新情報_処理区分(ジャーナル用)
          val = jnlshn_trankbn;
        }
      }
      // CSV特殊情報設定
      if (TblType.CSV.getVal() == tbl.getVal()) {
        if (i > mstshn_col_num) {
          val = csvshn_add_data[i - mstshn_col_num - 1];
        }
      }
      if (TblType.CSV.getVal() == tbl.getVal() && (i == 120) && StringUtils.isEmpty(val)) {
        values += ", COALESCE(null,0)";
      } else if (TblType.JNL.getVal() == tbl.getVal() && (i == 122) && StringUtils.isEmpty(val)) {
        values += ", " + this.getJNLSHN_SEQ() + "";
      } else if (TblType.CSV.getVal() == tbl.getVal() && (i == 113)) {
        values += ", current_timestamp";
      } else if (TblType.CSV.getVal() != tbl.getVal() && i == 114) {
        values += ", current_timestamp";
      } else if (TblType.CSV.getVal() != tbl.getVal() && (i == 123 || i == 115)) {
        values += ", current_timestamp";
      } else if (TblType.CSV.getVal() == tbl.getVal() && i == 114) {
      } else if (TblType.CSV.getVal() == tbl.getVal() && i == 115) {
      } else if (StringUtils.isEmpty(val)) {
        values += ", null";
      } else {
        prmData.add(val);
        values += ", ?";
      }
      names += ", " + col;
    }
    values = StringUtils.removeStart(values, ",");
    names = StringUtils.removeStart(names, ",");

    StringBuffer sbSQL;
    sbSQL = new StringBuffer();
    sbSQL.append(this.createMergeCmnCommandMSTSHN(tbl, sql, values, names));
    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("商品マスタ" + tbl.getTxt());
    return result;
  }

  /**
   * 商品マスタDELETE処理
   *
   * @param userId
   * @param data
   * @param tbl
   *
   * @throws Exception
   */
  public JSONObject createSqlMSTSHN_DEL(String userId, String btnId, JSONObject data, TblType tbl, SqlType sql) {
    JSONObject result = new JSONObject();

    // 更新情報
    ArrayList<String> prmData = new ArrayList<>();
    String values = "", names = "";

    new StringBuffer();
    List<String> keyColList = new ArrayList<>();
    List<String> keyValList = new ArrayList<>();
    if (TblType.SEI.getVal() == tbl.getVal()) {
      keyColList.add(MSTSHNLayout.SHNCD.getId());
      keyValList.add(data.optString(MSTSHNLayout.SHNCD.getId()));
    }
    if (TblType.YYK.getVal() == tbl.getVal()) {
      keyColList.add(MSTSHNLayout.SHNCD.getId());
      keyColList.add(MSTSHNLayout.YOYAKUDT.getId());
      keyValList.add(data.optString(MSTSHNLayout.SHNCD.getId()));
      keyValList.add(data.optString(MSTSHNLayout.YOYAKUDT.getId()));
    }
    if (TblType.CSV.getVal() == tbl.getVal()) {
      keyColList.add(CSVSHNLayout.SEQ.getId2());
      keyColList.add(CSVSHNLayout.INPUTNO.getId2());
      keyValList.add(csvshn_add_data[CSVSHNLayout.SEQ.getNo() - 1]);
      keyValList.add(csvshn_add_data[CSVSHNLayout.INPUTNO.getNo() - 1]);
    }
    // 共通固定値情報
    MSTSHNLayout[] keys = new MSTSHNLayout[] {MSTSHNLayout.UPDKBN, MSTSHNLayout.TOROKUMOTO, MSTSHNLayout.SENDFLG, MSTSHNLayout.OPERATOR, MSTSHNLayout.ADDDT, MSTSHNLayout.UPDDT};
    for (MSTSHNLayout itm : keys) {
      keyColList.add(itm.getId());
      String val = "";
      if (itm == MSTSHNLayout.UPDKBN) { // 更新区分
        val = DefineReport.ValUpdkbn.DEL.getVal();
      }

      if (itm == MSTSHNLayout.TOROKUMOTO) { // 登録元

        if (data.containsKey(MSTSHNLayout.TOROKUMOTO.getId())) {
          val = data.getString(MSTSHNLayout.TOROKUMOTO.getId());
        }

        if (StringUtils.isEmpty(val) || !val.equals("1")) {
          val = "0";
        }
      }
      if (TblType.CSV.getVal() != tbl.getVal()) {
        if (itm == MSTSHNLayout.SENDFLG) { // 送信フラグ
          val = DefineReport.Values.SENDFLG_UN.getVal();
        }
      }
      if (itm == MSTSHNLayout.OPERATOR) { // オペレータ
        val = userId;
      }
      keyValList.add(val);
    }
    // data情報取得
    for (int i = 0; i < keyColList.size() - 2; i++) {
      String col = keyColList.get(i);
      String val = keyValList.get(i);
      if (StringUtils.isEmpty(val)) {
        values += ", null";
      } else {
        if (isTest) {
          values += ", '" + val + "'";
        } else {
          prmData.add(val);
          values += ", ? ";
        }
      }
      names += ", " + col;
    }
    values = StringUtils.removeStart(values, ",");
    names = StringUtils.removeStart(names, ",");

    // 条件文など取得
    JSONObject parts = this.getSqlPartsCmnCommandMSTSHN(tbl, sql);
    String szTable = parts.optString("TABLE");

    StringBuffer sbSQL;
    sbSQL = new StringBuffer();
    sbSQL.append("INSERT INTO " + szTable + " ( ");
    if (TblType.CSV.getVal() == tbl.getVal()) {
      sbSQL.append("SEQ ");
      sbSQL.append(",INPUTNO ");
      sbSQL.append(",TOROKUMOTO ");
      sbSQL.append(",UPDKBN ");
      sbSQL.append(",OPERATOR ");
      sbSQL.append(",UPDDT ");
    }else {
    sbSQL.append("SHNCD ");
    if (tbl.getVal() == 2) {
      sbSQL.append(",YOYAKUDT ");
    }
    sbSQL.append(",UPDKBN ");
    sbSQL.append(",TOROKUMOTO ");
    if (TblType.CSV.getVal() != tbl.getVal()) {
      sbSQL.append(" ,SENDFLG ");
    }
    sbSQL.append(",OPERATOR ");
    if (TblType.CSV.getVal() != tbl.getVal() && DefineReport.Button.CSV_IMPORT_YYK.getObj().equals(btnId)) {
      sbSQL.append(" ,ADDDT "); // F114: 登録日
    }
    sbSQL.append(",UPDDT ");
    }
    if (TblType.CSV.getVal() == tbl.getVal()) {
      sbSQL.append(")SELECT ");
      sbSQL.append("SEQ ");
      sbSQL.append(",INPUTNO ");
      sbSQL.append(",TOROKUMOTO ");
      sbSQL.append(",UPDKBN ");
      sbSQL.append(",OPERATOR ");
      sbSQL.append(",UPDDT ");
      sbSQL.append("FROM( VALUES ROW( ");
    }else {
      sbSQL.append(")SELECT * FROM( VALUES ROW( ");
    }
    sbSQL.append(" " + values + " ");
    if (TblType.CSV.getVal() != tbl.getVal() && DefineReport.Button.CSV_IMPORT_YYK.getObj().equals(btnId)) {
    sbSQL.append(" ,CURRENT_TIMESTAMP"); // F114: 登録日
    }
    sbSQL.append(" ,CURRENT_TIMESTAMP "); // 更新日
    sbSQL.append(")) AS NEW ");
    sbSQL.append("( ");
    if (TblType.CSV.getVal() == tbl.getVal()) {
      sbSQL.append("SEQ ");
      sbSQL.append(",INPUTNO ");
      sbSQL.append(",TOROKUMOTO ");
      sbSQL.append(",UPDKBN ");
      sbSQL.append(",SENDFLG ");
      sbSQL.append(",OPERATOR ");
      sbSQL.append(",UPDDT ");
    }else {
    sbSQL.append("SHNCD ");
    if (tbl.getVal() == 2) {
      sbSQL.append(",YOYAKUDT ");
    }
    sbSQL.append(",UPDKBN ");
    sbSQL.append(",TOROKUMOTO ");
    if (TblType.CSV.getVal() != tbl.getVal()) {
      sbSQL.append(" ,SENDFLG ");
    }
    sbSQL.append(",OPERATOR ");
    if (TblType.CSV.getVal() != tbl.getVal() && DefineReport.Button.CSV_IMPORT_YYK.getObj().equals(btnId)) {
      sbSQL.append(" ,ADDDT "); // F114: 登録日
    }
    sbSQL.append(",UPDDT ");
    }
    sbSQL.append(") ");
    sbSQL.append("ON DUPLICATE KEY UPDATE ");
    if (TblType.CSV.getVal() == tbl.getVal()) {
      sbSQL.append("UPDKBN = VALUES(UPDKBN)");
      sbSQL.append(",OPERATOR = VALUES(OPERATOR) ");
      sbSQL.append(",UPDDT = VALUES(UPDDT) ");
    }else {
    sbSQL.append("SHNCD = VALUES(SHNCD)  ");
    if (tbl.getVal() == 2) {
      sbSQL.append(",YOYAKUDT = VALUES(YOYAKUDT) ");
    }
    sbSQL.append(",UPDKBN = VALUES(UPDKBN)  ");
    sbSQL.append(",TOROKUMOTO = VALUES(TOROKUMOTO)  ");

    if (TblType.CSV.getVal() != tbl.getVal()) {
      sbSQL.append(",SENDFLG = VALUES(SENDFLG) ");
    }
    sbSQL.append(",OPERATOR = VALUES(OPERATOR)  ");
    if (TblType.CSV.getVal() != tbl.getVal() && DefineReport.Button.CSV_IMPORT_YYK.getObj().equals(btnId)) {
      sbSQL.append(",ADDDT = VALUES(ADDDT) "); // F114: 登録日
    }

    sbSQL.append(",UPDDT = VALUES(UPDDT) ");
    }

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("商品マスタ" + tbl.getTxt());
    return result;
  }

  public JSONObject getSqlPartsCmnCommandMSTSHN(TblType tbl, SqlType sql) {
    JSONObject json = new JSONObject();

    String szTable = "INAMS.MSTSHN";
    String szWhere = "T.SHNCD = RE.SHNCD";
    if (TblType.YYK.getVal() == tbl.getVal()) {
      szTable += "_Y";
      szWhere += " and T.YOYAKUDT = RE.YOYAKUDT ";
    } else if (TblType.JNL.getVal() == tbl.getVal()) {
      szTable = "INAAD.JNLSHN";
      szWhere = "T.SEQ = RE.SEQ";
    } else if (TblType.CSV.getVal() == tbl.getVal()) {
      szTable = "INAMS.CSVSHN";
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
    parts.optString("WHERE");

    // 基本Merge文
    StringBuffer sbSQL;
    sbSQL = new StringBuffer();
    sbSQL.append("INSERT INTO " + szTable + " ( ");
    sbSQL.append("  SHNCD"); // F1 : 商品コード
    sbSQL.append(" ,YOYAKUDT"); // F2 : マスタ変更予定日
    sbSQL.append(" ,TENBAIKADT"); // F3 : 店売価実施日
    sbSQL.append(" ,YOT_BMNCD"); // F4 : 用途分類コード_部門
    sbSQL.append(" ,YOT_DAICD"); // F5 : 用途分類コード_大
    sbSQL.append(" ,YOT_CHUCD"); // F6 : 用途分類コード_中
    sbSQL.append(" ,YOT_SHOCD"); // F7 : 用途分類コード_小
    sbSQL.append(" ,URI_BMNCD"); // F8 : 売場分類コード_部門
    sbSQL.append(" ,URI_DAICD"); // F9 : 売場分類コード_大
    sbSQL.append(" ,URI_CHUCD"); // F10: 売場分類コード_中
    sbSQL.append(" ,URI_SHOCD"); // F11: 売場分類コード_小
    sbSQL.append(" ,BMNCD"); // F12: 標準分類コード_部門
    sbSQL.append(" ,DAICD"); // F13: 標準分類コード_大
    sbSQL.append(" ,CHUCD"); // F14: 標準分類コード_中
    sbSQL.append(" ,SHOCD"); // F15: 標準分類コード_小
    sbSQL.append(" ,SSHOCD"); // F16: 標準分類コード_小小
    sbSQL.append(" ,ATSUK_STDT"); // F17: 取扱期間_開始日
    sbSQL.append(" ,ATSUK_EDDT"); // F18: 取扱期間_終了日
    sbSQL.append(" ,TEISHIKBN"); // F19: 取扱停止
    if (TblType.JNL.getVal() == tbl.getVal()) {
      sbSQL.append(" ,SHOHINAN"); // F20: 商品名（カナ）
      sbSQL.append(" ,SHOHINKN"); // F21: 商品名（漢字）
    } else {
      sbSQL.append(" ,SHNAN"); // F20: 商品名（カナ）
      sbSQL.append(" ,SHNKN"); // F21: 商品名（漢字）
    }
    sbSQL.append(" ,PCARDKN"); // F22: プライスカード商品名称（漢字）
    sbSQL.append(" ,POPKN"); // F23: POP名称
    sbSQL.append(" ,RECEIPTAN"); // F24: レシート名（カナ）
    sbSQL.append(" ,RECEIPTKN"); // F25: レシート名（漢字）
    sbSQL.append(" ,PCKBN"); // F26: PC区分
    sbSQL.append(" ,KAKOKBN"); // F27: 加工区分
    sbSQL.append(" ,ICHIBAKBN"); // F28: 市場区分
    sbSQL.append(" ,SHNKBN"); // F29: 商品種類
    sbSQL.append(" ,SANCHIKN"); // F30: 産地
    sbSQL.append(" ,SSIRCD"); // F31: 標準仕入先コード
    sbSQL.append(" ,HSPTN"); // F32: 配送パターン
    sbSQL.append(" ,RG_ATSUKFLG"); // F33: レギュラー情報_取扱フラグ
    sbSQL.append(" ,RG_GENKAAM"); // F34: レギュラー情報_原価
    sbSQL.append(" ,RG_BAIKAAM"); // F35: レギュラー情報_売価
    sbSQL.append(" ,RG_IRISU"); // F36: レギュラー情報_店入数
    sbSQL.append(" ,RG_IDENFLG"); // F37: レギュラー情報_一括伝票フラグ
    sbSQL.append(" ,RG_WAPNFLG"); // F38: レギュラー情報_ワッペン
    sbSQL.append(" ,HS_ATSUKFLG"); // F39: 販促情報_取扱フラグ
    sbSQL.append(" ,HS_GENKAAM"); // F40: 販促情報_原価
    sbSQL.append(" ,HS_BAIKAAM"); // F41: 販促情報_売価
    sbSQL.append(" ,HS_IRISU"); // F42: 販促情報_店入数
    sbSQL.append(" ,HS_WAPNFLG"); // F43: 販促情報_ワッペン
    sbSQL.append(" ,HS_SPOTMINSU"); // F44: 販促情報_スポット最低発注数
    sbSQL.append(" ,HP_SWAPNFLG"); // F45: 販促情報_特売ワッペン
    sbSQL.append(" ,KIKKN"); // F46: 規格名称
    sbSQL.append(" ,UP_YORYOSU"); // F47: ユニットプライス_容量
    sbSQL.append(" ,UP_TYORYOSU"); // F48: ユニットプライス_単位容量
    sbSQL.append(" ,UP_TANIKBN"); // F49: ユニットプライス_ユニット単位
    if (TblType.JNL.getVal() == tbl.getVal()) {
      sbSQL.append(" ,SHN_YOKOSZ"); // F50: 商品サイズ_横
      sbSQL.append(" ,SHN_TATESZ"); // F51: 商品サイズ_縦
      sbSQL.append(" ,SHN_OKUSZ"); // F52: 商品サイズ_奥行
      sbSQL.append(" ,SHN_JRYOSZ"); // F53: 商品サイズ_重量
    } else {
      sbSQL.append(" ,SHNYOKOSZ"); // F50: 商品サイズ_横
      sbSQL.append(" ,SHNTATESZ"); // F51: 商品サイズ_縦
      sbSQL.append(" ,SHNOKUSZ"); // F52: 商品サイズ_奥行
      sbSQL.append(" ,SHNJRYOSZ"); // F53: 商品サイズ_重量
    }
    sbSQL.append(" ,PBKBN"); // F54: PB区分
    sbSQL.append(" ,KOMONOKBM"); // F55: 小物区分
    sbSQL.append(" ,TANAOROKBN"); // F56: 棚卸区分
    sbSQL.append(" ,TEIKEIKBN"); // F57: 定計区分
    sbSQL.append(" ,ODS_HARUSU"); // F58: ODS_賞味期限_春
    sbSQL.append(" ,ODS_NATSUSU"); // F59: ODS_賞味期限_夏
    sbSQL.append(" ,ODS_AKISU"); // F60: ODS_賞味期限_秋
    sbSQL.append(" ,ODS_FUYUSU"); // F61: ODS_賞味期限_冬
    sbSQL.append(" ,ODS_NYUKASU"); // F62: ODS_入荷期限
    sbSQL.append(" ,ODS_NEBIKISU"); // F63: ODS_値引期限
    sbSQL.append(" ,PCARD_SHUKBN"); // F64: プライスカード_種類
    sbSQL.append(" ,PCARD_IROKBN"); // F65: プライスカード_色
    sbSQL.append(" ,ZEIKBN"); // F66: 税区分
    sbSQL.append(" ,ZEIRTKBN"); // F67: 税率区分
    sbSQL.append(" ,ZEIRTKBN_OLD"); // F68: 旧税率区分
    sbSQL.append(" ,ZEIRTHENKODT"); // F69: 税率変更日
    sbSQL.append(" ,SEIZOGENNISU"); // F70: 製造限度日数
    sbSQL.append(" ,TEIKANKBN"); // F71: 定貫不定貫区分
    sbSQL.append(" ,MAKERCD"); // F72: メーカーコード
    sbSQL.append(" ,IMPORTKBN"); // F73: 輸入区分
    sbSQL.append(" ,SIWAKEKBN"); // F74: 仕分区分
    sbSQL.append(" ,HENPIN_KBN"); // F75: 返品区分
    sbSQL.append(" ,TAISHONENSU"); // F76: 対象年齢
    sbSQL.append(" ,CALORIESU"); // F77: カロリー表示
    sbSQL.append(" ,ELPFLG"); // F78: フラグ情報_ELP
    sbSQL.append(" ,BELLMARKFLG"); // F79: フラグ情報_ベルマーク
    sbSQL.append(" ,RECYCLEFLG"); // F80: フラグ情報_リサイクル
    sbSQL.append(" ,ECOFLG"); // F81: フラグ情報_エコマーク
    sbSQL.append(" ,HZI_YOTO"); // F82: 包材用途
    sbSQL.append(" ,HZI_ZAISHITU"); // F83: 包材材質
    sbSQL.append(" ,HZI_RECYCLE"); // F84: 包材リサイクル対象
    sbSQL.append(" ,KIKANKBN"); // F85: 期間
    sbSQL.append(" ,SHUKYUKBN"); // F86: 酒級
    sbSQL.append(" ,DOSU"); // F87: 度数
    sbSQL.append(" ,CHINRETUCD"); // F88: 陳列形式コード
    sbSQL.append(" ,DANTUMICD"); // F89: 段積み形式コード
    sbSQL.append(" ,KASANARICD"); // F90: 重なりコード
    sbSQL.append(" ,KASANARISZ"); // F91: 重なりサイズ
    sbSQL.append(" ,ASSHUKURT"); // F92: 圧縮率
    sbSQL.append(" ,SHUBETUCD"); // F93: 種別コード
    sbSQL.append(" ,URICD"); // F94: 販売コード
    sbSQL.append(" ,SALESCOMKN"); // F95: 商品コピー・セールスコメント
    sbSQL.append(" ,URABARIKBN"); // F96: 裏貼
    sbSQL.append(" ,PCARD_OPFLG"); // F97: プライスカード出力有無
    sbSQL.append(" ,PARENTCD"); // F98: 親商品コード
    sbSQL.append(" ,BINKBN"); // F99: 便区分
    sbSQL.append(" ,HAT_MONKBN"); // F100: 発注曜日_月
    sbSQL.append(" ,HAT_TUEKBN"); // F101: 発注曜日_火
    sbSQL.append(" ,HAT_WEDKBN"); // F102: 発注曜日_水
    sbSQL.append(" ,HAT_THUKBN"); // F103: 発注曜日_木
    sbSQL.append(" ,HAT_FRIKBN"); // F104: 発注曜日_金
    sbSQL.append(" ,HAT_SATKBN"); // F105: 発注曜日_土
    sbSQL.append(" ,HAT_SUNKBN"); // F106: 発注曜日_日
    sbSQL.append(" ,READTMPTN"); // F107: リードタイムパターン
    sbSQL.append(" ,SIMEKAISU"); // F108: 締め回数
    sbSQL.append(" ,IRYOREFLG"); // F109: 衣料使い回しフラグ
    sbSQL.append(" ,TOROKUMOTO"); // F110: 登録元
    sbSQL.append(" ,UPDKBN"); // F111: 更新区分
    if (TblType.CSV.getVal() != tbl.getVal()) {
      sbSQL.append(" ,SENDFLG"); // F112: 送信フラグ
    }
    sbSQL.append(" ,OPERATOR"); // F113: オペレータ
    if (TblType.CSV.getVal() != tbl.getVal()) {
      sbSQL.append(" ,ADDDT"); // F114: 登録日
    }
    sbSQL.append(" ,UPDDT"); // F115: 更新日
    sbSQL.append(" ,K_HONKB"); // F116: 保温区分
    sbSQL.append(" ,K_WAPNFLG_R"); // F117: デリカワッペン区分_レギュラー
    sbSQL.append(" ,K_WAPNFLG_H"); // F118: デリカワッペン区分_販促
    sbSQL.append(" ,K_TORIKB"); // F119: 取扱区分
    sbSQL.append(" ,ITFCD"); // F120: ITFコード
    sbSQL.append(" ,CENTER_IRISU"); // F121: センター入数
    if (TblType.JNL.getVal() == tbl.getVal()) {
      sbSQL.append(" ,SEQ"); // SEQ
      sbSQL.append(" ,INF_DATE"); // 更新情報_更新日時
      sbSQL.append(" ,INF_OPERATOR"); // 更新情報_オペレータ
      sbSQL.append(" ,INF_TABLEKBN"); // 更新情報_テーブル区分 TODO
      sbSQL.append(" ,INF_TRANKBN"); // 更新情報_処理区分 TODO
    }
    if (TblType.CSV.getVal() == tbl.getVal()) {
      sbSQL.append(" ,SEQ"); // F1 : SEQ
      sbSQL.append(" ,INPUTNO"); // F2 : 入力番号
      sbSQL.append(" ,ERRCD"); // F3 : エラーコード
      sbSQL.append(" ,ERRFLD"); // F4 : エラー箇所
      sbSQL.append(" ,ERRVL"); // F5 : エラー値
      sbSQL.append(" ,ERRTBLNM"); // F6 : エラーテーブル名
      sbSQL.append(" ,CSV_UPDKBN"); // F7 : CSV登録区分
      sbSQL.append(" ,KETAKBN"); // F8 : 桁指定
    }
    sbSQL.append(" ) SELECT ");
    sbSQL.append("  SHNCD"); // F1 : 商品コード
    sbSQL.append(" ,YOYAKUDT"); // F2 : マスタ変更予定日
    sbSQL.append(" ,TENBAIKADT"); // F3 : 店売価実施日
    sbSQL.append(" ,YOT_BMNCD"); // F4 : 用途分類コード_部門
    sbSQL.append(" ,YOT_DAICD"); // F5 : 用途分類コード_大
    sbSQL.append(" ,YOT_CHUCD"); // F6 : 用途分類コード_中
    sbSQL.append(" ,YOT_SHOCD"); // F7 : 用途分類コード_小
    sbSQL.append(" ,URI_BMNCD"); // F8 : 売場分類コード_部門
    sbSQL.append(" ,URI_DAICD"); // F9 : 売場分類コード_大
    sbSQL.append(" ,URI_CHUCD"); // F10: 売場分類コード_中
    sbSQL.append(" ,URI_SHOCD"); // F11: 売場分類コード_小
    sbSQL.append(" ,BMNCD"); // F12: 標準分類コード_部門
    sbSQL.append(" ,DAICD"); // F13: 標準分類コード_大
    sbSQL.append(" ,CHUCD"); // F14: 標準分類コード_中
    sbSQL.append(" ,SHOCD"); // F15: 標準分類コード_小
    sbSQL.append(" ,SSHOCD"); // F16: 標準分類コード_小小
    sbSQL.append(" ,ATSUK_STDT"); // F17: 取扱期間_開始日
    sbSQL.append(" ,ATSUK_EDDT"); // F18: 取扱期間_終了日
    sbSQL.append(" ,TEISHIKBN"); // F19: 取扱停止
    if (TblType.JNL.getVal() == tbl.getVal()) {
      sbSQL.append(" ,SHOHINAN"); // F20: 商品名（カナ）
      sbSQL.append(" ,SHOHINKN"); // F21: 商品名（漢字）
    } else {
      sbSQL.append(" ,SHNAN"); // F20: 商品名（カナ）
      sbSQL.append(" ,SHNKN"); // F21: 商品名（漢字）
    }
    sbSQL.append(" ,PCARDKN"); // F22: プライスカード商品名称（漢字）
    sbSQL.append(" ,POPKN"); // F23: POP名称
    sbSQL.append(" ,RECEIPTAN"); // F24: レシート名（カナ）
    sbSQL.append(" ,RECEIPTKN"); // F25: レシート名（漢字）
    sbSQL.append(" ,PCKBN"); // F26: PC区分
    sbSQL.append(" ,KAKOKBN"); // F27: 加工区分
    sbSQL.append(" ,ICHIBAKBN"); // F28: 市場区分
    sbSQL.append(" ,SHNKBN"); // F29: 商品種類
    sbSQL.append(" ,SANCHIKN"); // F30: 産地
    sbSQL.append(" ,SSIRCD"); // F31: 標準仕入先コード
    sbSQL.append(" ,HSPTN"); // F32: 配送パターン
    sbSQL.append(" ,RG_ATSUKFLG"); // F33: レギュラー情報_取扱フラグ
    sbSQL.append(" ,RG_GENKAAM"); // F34: レギュラー情報_原価
    sbSQL.append(" ,RG_BAIKAAM"); // F35: レギュラー情報_売価
    sbSQL.append(" ,RG_IRISU"); // F36: レギュラー情報_店入数
    sbSQL.append(" ,RG_IDENFLG"); // F37: レギュラー情報_一括伝票フラグ
    sbSQL.append(" ,RG_WAPNFLG"); // F38: レギュラー情報_ワッペン
    sbSQL.append(" ,HS_ATSUKFLG"); // F39: 販促情報_取扱フラグ
    sbSQL.append(" ,HS_GENKAAM"); // F40: 販促情報_原価
    sbSQL.append(" ,HS_BAIKAAM"); // F41: 販促情報_売価
    sbSQL.append(" ,HS_IRISU"); // F42: 販促情報_店入数
    sbSQL.append(" ,HS_WAPNFLG"); // F43: 販促情報_ワッペン
    sbSQL.append(" ,HS_SPOTMINSU"); // F44: 販促情報_スポット最低発注数
    sbSQL.append(" ,HP_SWAPNFLG"); // F45: 販促情報_特売ワッペン
    sbSQL.append(" ,KIKKN"); // F46: 規格名称
    sbSQL.append(" ,UP_YORYOSU"); // F47: ユニットプライス_容量
    sbSQL.append(" ,UP_TYORYOSU"); // F48: ユニットプライス_単位容量
    sbSQL.append(" ,UP_TANIKBN"); // F49: ユニットプライス_ユニット単位
    if (TblType.JNL.getVal() == tbl.getVal()) {
      sbSQL.append(" ,SHN_YOKOSZ"); // F50: 商品サイズ_横
      sbSQL.append(" ,SHN_TATESZ"); // F51: 商品サイズ_縦
      sbSQL.append(" ,SHN_OKUSZ"); // F52: 商品サイズ_奥行
      sbSQL.append(" ,SHN_JRYOSZ"); // F53: 商品サイズ_重量
    } else {
      sbSQL.append(" ,SHNYOKOSZ"); // F50: 商品サイズ_横
      sbSQL.append(" ,SHNTATESZ"); // F51: 商品サイズ_縦
      sbSQL.append(" ,SHNOKUSZ"); // F52: 商品サイズ_奥行
      sbSQL.append(" ,SHNJRYOSZ"); // F53: 商品サイズ_重量
    }
    sbSQL.append(" ,PBKBN"); // F54: PB区分
    sbSQL.append(" ,KOMONOKBM"); // F55: 小物区分
    sbSQL.append(" ,TANAOROKBN"); // F56: 棚卸区分
    sbSQL.append(" ,TEIKEIKBN"); // F57: 定計区分
    sbSQL.append(" ,ODS_HARUSU"); // F58: ODS_賞味期限_春
    sbSQL.append(" ,ODS_NATSUSU"); // F59: ODS_賞味期限_夏
    sbSQL.append(" ,ODS_AKISU"); // F60: ODS_賞味期限_秋
    sbSQL.append(" ,ODS_FUYUSU"); // F61: ODS_賞味期限_冬
    sbSQL.append(" ,ODS_NYUKASU"); // F62: ODS_入荷期限
    sbSQL.append(" ,ODS_NEBIKISU"); // F63: ODS_値引期限
    sbSQL.append(" ,PCARD_SHUKBN"); // F64: プライスカード_種類
    sbSQL.append(" ,PCARD_IROKBN"); // F65: プライスカード_色
    sbSQL.append(" ,ZEIKBN"); // F66: 税区分
    sbSQL.append(" ,ZEIRTKBN"); // F67: 税率区分
    sbSQL.append(" ,ZEIRTKBN_OLD"); // F68: 旧税率区分
    sbSQL.append(" ,ZEIRTHENKODT"); // F69: 税率変更日
    sbSQL.append(" ,SEIZOGENNISU"); // F70: 製造限度日数
    sbSQL.append(" ,TEIKANKBN"); // F71: 定貫不定貫区分
    sbSQL.append(" ,MAKERCD"); // F72: メーカーコード
    sbSQL.append(" ,IMPORTKBN"); // F73: 輸入区分
    sbSQL.append(" ,SIWAKEKBN"); // F74: 仕分区分
    sbSQL.append(" ,HENPIN_KBN"); // F75: 返品区分
    sbSQL.append(" ,TAISHONENSU"); // F76: 対象年齢
    sbSQL.append(" ,CALORIESU"); // F77: カロリー表示
    sbSQL.append(" ,ELPFLG"); // F78: フラグ情報_ELP
    sbSQL.append(" ,BELLMARKFLG"); // F79: フラグ情報_ベルマーク
    sbSQL.append(" ,RECYCLEFLG"); // F80: フラグ情報_リサイクル
    sbSQL.append(" ,ECOFLG"); // F81: フラグ情報_エコマーク
    sbSQL.append(" ,HZI_YOTO"); // F82: 包材用途
    sbSQL.append(" ,HZI_ZAISHITU"); // F83: 包材材質
    sbSQL.append(" ,HZI_RECYCLE"); // F84: 包材リサイクル対象
    sbSQL.append(" ,KIKANKBN"); // F85: 期間
    sbSQL.append(" ,SHUKYUKBN"); // F86: 酒級
    sbSQL.append(" ,DOSU"); // F87: 度数
    sbSQL.append(" ,CHINRETUCD"); // F88: 陳列形式コード
    sbSQL.append(" ,LPAD(DANTUMICD,2,0)"); // F89: 段積み形式コード
    sbSQL.append(" ,KASANARICD"); // F90: 重なりコード
    sbSQL.append(" ,KASANARISZ"); // F91: 重なりサイズ
    sbSQL.append(" ,ASSHUKURT"); // F92: 圧縮率
    sbSQL.append(" ,SHUBETUCD"); // F93: 種別コード
    sbSQL.append(" ,URICD"); // F94: 販売コード
    sbSQL.append(" ,SALESCOMKN"); // F95: 商品コピー・セールスコメント
    sbSQL.append(" ,URABARIKBN"); // F96: 裏貼
    sbSQL.append(" ,PCARD_OPFLG"); // F97: プライスカード出力有無
    sbSQL.append(" ,PARENTCD"); // F98: 親商品コード
    sbSQL.append(" ,BINKBN"); // F99: 便区分
    sbSQL.append(" ,HAT_MONKBN"); // F100: 発注曜日_月
    sbSQL.append(" ,HAT_TUEKBN"); // F101: 発注曜日_火
    sbSQL.append(" ,HAT_WEDKBN"); // F102: 発注曜日_水
    sbSQL.append(" ,HAT_THUKBN"); // F103: 発注曜日_木
    sbSQL.append(" ,HAT_FRIKBN"); // F104: 発注曜日_金
    sbSQL.append(" ,HAT_SATKBN"); // F105: 発注曜日_土
    sbSQL.append(" ,HAT_SUNKBN"); // F106: 発注曜日_日
    sbSQL.append(" ,READTMPTN"); // F107: リードタイムパターン
    sbSQL.append(" ,SIMEKAISU"); // F108: 締め回数
    sbSQL.append(" ,IRYOREFLG"); // F109: 衣料使い回しフラグ
    sbSQL.append(" ,TOROKUMOTO"); // F110: 登録元
    sbSQL.append(" ,UPDKBN"); // F111: 更新区分
    if (TblType.CSV.getVal() != tbl.getVal()) {
      sbSQL.append(" ,SENDFLG"); // F112: 送信フラグ
    }
    sbSQL.append(" ,OPERATOR"); // F113: オペレータ
    if (TblType.CSV.getVal() != tbl.getVal()) {
      sbSQL.append(" ,ADDDT"); // F114: 登録日
    }
    sbSQL.append(" ,UPDDT"); // F115: 更新日
    sbSQL.append(" ,K_HONKB"); // F116: 保温区分
    sbSQL.append(" ,K_WAPNFLG_R"); // F117: デリカワッペン区分_レギュラー
    sbSQL.append(" ,K_WAPNFLG_H"); // F118: デリカワッペン区分_販促
    sbSQL.append(" ,K_TORIKB"); // F119: 取扱区分
    sbSQL.append(" ,ITFCD"); // F120: ITFコード
    sbSQL.append(" ,CENTER_IRISU"); // F121: センター入数
    if (TblType.JNL.getVal() == tbl.getVal()) {
      sbSQL.append(" ,SEQ"); // SEQ
      sbSQL.append(" ,INF_DATE"); // 更新情報_更新日時
      sbSQL.append(" ,INF_OPERATOR"); // 更新情報_オペレータ
      sbSQL.append(" ,INF_TABLEKBN"); // 更新情報_テーブル区分 TODO
      sbSQL.append(" ,INF_TRANKBN"); // 更新情報_処理区分 TODO
    }
    if (TblType.CSV.getVal() == tbl.getVal()) {
      sbSQL.append(" ,SEQ"); // F1 : SEQ
      sbSQL.append(" ,INPUTNO"); // F2 : 入力番号
      sbSQL.append(" ,ERRCD"); // F3 : エラーコード
      sbSQL.append(" ,ERRFLD"); // F4 : エラー箇所
      sbSQL.append(" ,ERRVL"); // F5 : エラー値
      sbSQL.append(" ,ERRTBLNM"); // F6 : エラーテーブル名
      sbSQL.append(" ,CSV_UPDKBN"); // F7 : CSV登録区分
      sbSQL.append(" ,KETAKBN"); // F8 : 桁指定
    }
    sbSQL.append(" FROM( VALUES ROW (" + values + ") ) ");
    sbSQL.append("AS TMP( ");
    sbSQL.append("  SHNCD"); // F1 : 商品コード
    sbSQL.append(" ,YOYAKUDT"); // F2 : マスタ変更予定日
    sbSQL.append(" ,TENBAIKADT"); // F3 : 店売価実施日
    sbSQL.append(" ,YOT_BMNCD"); // F4 : 用途分類コード_部門
    sbSQL.append(" ,YOT_DAICD"); // F5 : 用途分類コード_大
    sbSQL.append(" ,YOT_CHUCD"); // F6 : 用途分類コード_中
    sbSQL.append(" ,YOT_SHOCD"); // F7 : 用途分類コード_小
    sbSQL.append(" ,URI_BMNCD"); // F8 : 売場分類コード_部門
    sbSQL.append(" ,URI_DAICD"); // F9 : 売場分類コード_大
    sbSQL.append(" ,URI_CHUCD"); // F10: 売場分類コード_中
    sbSQL.append(" ,URI_SHOCD"); // F11: 売場分類コード_小
    sbSQL.append(" ,BMNCD"); // F12: 標準分類コード_部門
    sbSQL.append(" ,DAICD"); // F13: 標準分類コード_大
    sbSQL.append(" ,CHUCD"); // F14: 標準分類コード_中
    sbSQL.append(" ,SHOCD"); // F15: 標準分類コード_小
    sbSQL.append(" ,SSHOCD"); // F16: 標準分類コード_小小
    sbSQL.append(" ,ATSUK_STDT"); // F17: 取扱期間_開始日
    sbSQL.append(" ,ATSUK_EDDT"); // F18: 取扱期間_終了日
    sbSQL.append(" ,TEISHIKBN"); // F19: 取扱停止
    if (TblType.JNL.getVal() == tbl.getVal()) {
      sbSQL.append(" ,SHOHINAN"); // F20: 商品名（カナ）
      sbSQL.append(" ,SHOHINKN"); // F21: 商品名（漢字）
    } else {
      sbSQL.append(" ,SHNAN"); // F20: 商品名（カナ）
      sbSQL.append(" ,SHNKN"); // F21: 商品名（漢字）
    }
    sbSQL.append(" ,PCARDKN"); // F22: プライスカード商品名称（漢字）
    sbSQL.append(" ,POPKN"); // F23: POP名称
    sbSQL.append(" ,RECEIPTAN"); // F24: レシート名（カナ）
    sbSQL.append(" ,RECEIPTKN"); // F25: レシート名（漢字）
    sbSQL.append(" ,PCKBN"); // F26: PC区分
    sbSQL.append(" ,KAKOKBN"); // F27: 加工区分
    sbSQL.append(" ,ICHIBAKBN"); // F28: 市場区分
    sbSQL.append(" ,SHNKBN"); // F29: 商品種類
    sbSQL.append(" ,SANCHIKN"); // F30: 産地
    sbSQL.append(" ,SSIRCD"); // F31: 標準仕入先コード
    sbSQL.append(" ,HSPTN"); // F32: 配送パターン
    sbSQL.append(" ,RG_ATSUKFLG"); // F33: レギュラー情報_取扱フラグ
    sbSQL.append(" ,RG_GENKAAM"); // F34: レギュラー情報_原価
    sbSQL.append(" ,RG_BAIKAAM"); // F35: レギュラー情報_売価
    sbSQL.append(" ,RG_IRISU"); // F36: レギュラー情報_店入数
    sbSQL.append(" ,RG_IDENFLG"); // F37: レギュラー情報_一括伝票フラグ
    sbSQL.append(" ,RG_WAPNFLG"); // F38: レギュラー情報_ワッペン
    sbSQL.append(" ,HS_ATSUKFLG"); // F39: 販促情報_取扱フラグ
    sbSQL.append(" ,HS_GENKAAM"); // F40: 販促情報_原価
    sbSQL.append(" ,HS_BAIKAAM"); // F41: 販促情報_売価
    sbSQL.append(" ,HS_IRISU"); // F42: 販促情報_店入数
    sbSQL.append(" ,HS_WAPNFLG"); // F43: 販促情報_ワッペン
    sbSQL.append(" ,HS_SPOTMINSU"); // F44: 販促情報_スポット最低発注数
    sbSQL.append(" ,HP_SWAPNFLG"); // F45: 販促情報_特売ワッペン
    sbSQL.append(" ,KIKKN"); // F46: 規格名称
    sbSQL.append(" ,UP_YORYOSU"); // F47: ユニットプライス_容量
    sbSQL.append(" ,UP_TYORYOSU"); // F48: ユニットプライス_単位容量
    sbSQL.append(" ,UP_TANIKBN"); // F49: ユニットプライス_ユニット単位
    if (TblType.JNL.getVal() == tbl.getVal()) {
      sbSQL.append(" ,SHN_YOKOSZ"); // F50: 商品サイズ_横
      sbSQL.append(" ,SHN_TATESZ"); // F51: 商品サイズ_縦
      sbSQL.append(" ,SHN_OKUSZ"); // F52: 商品サイズ_奥行
      sbSQL.append(" ,SHN_JRYOSZ"); // F53: 商品サイズ_重量
    } else {
      sbSQL.append(" ,SHNYOKOSZ"); // F50: 商品サイズ_横
      sbSQL.append(" ,SHNTATESZ"); // F51: 商品サイズ_縦
      sbSQL.append(" ,SHNOKUSZ"); // F52: 商品サイズ_奥行
      sbSQL.append(" ,SHNJRYOSZ"); // F53: 商品サイズ_重量
    }
    sbSQL.append(" ,PBKBN"); // F54: PB区分
    sbSQL.append(" ,KOMONOKBM"); // F55: 小物区分
    sbSQL.append(" ,TANAOROKBN"); // F56: 棚卸区分
    sbSQL.append(" ,TEIKEIKBN"); // F57: 定計区分
    sbSQL.append(" ,ODS_HARUSU"); // F58: ODS_賞味期限_春
    sbSQL.append(" ,ODS_NATSUSU"); // F59: ODS_賞味期限_夏
    sbSQL.append(" ,ODS_AKISU"); // F60: ODS_賞味期限_秋
    sbSQL.append(" ,ODS_FUYUSU"); // F61: ODS_賞味期限_冬
    sbSQL.append(" ,ODS_NYUKASU"); // F62: ODS_入荷期限
    sbSQL.append(" ,ODS_NEBIKISU"); // F63: ODS_値引期限
    sbSQL.append(" ,PCARD_SHUKBN"); // F64: プライスカード_種類
    sbSQL.append(" ,PCARD_IROKBN"); // F65: プライスカード_色
    sbSQL.append(" ,ZEIKBN"); // F66: 税区分
    sbSQL.append(" ,ZEIRTKBN"); // F67: 税率区分
    sbSQL.append(" ,ZEIRTKBN_OLD"); // F68: 旧税率区分
    sbSQL.append(" ,ZEIRTHENKODT"); // F69: 税率変更日
    sbSQL.append(" ,SEIZOGENNISU"); // F70: 製造限度日数
    sbSQL.append(" ,TEIKANKBN"); // F71: 定貫不定貫区分
    sbSQL.append(" ,MAKERCD"); // F72: メーカーコード
    sbSQL.append(" ,IMPORTKBN"); // F73: 輸入区分
    sbSQL.append(" ,SIWAKEKBN"); // F74: 仕分区分
    sbSQL.append(" ,HENPIN_KBN"); // F75: 返品区分
    sbSQL.append(" ,TAISHONENSU"); // F76: 対象年齢
    sbSQL.append(" ,CALORIESU"); // F77: カロリー表示
    sbSQL.append(" ,ELPFLG"); // F78: フラグ情報_ELP
    sbSQL.append(" ,BELLMARKFLG"); // F79: フラグ情報_ベルマーク
    sbSQL.append(" ,RECYCLEFLG"); // F80: フラグ情報_リサイクル
    sbSQL.append(" ,ECOFLG"); // F81: フラグ情報_エコマーク
    sbSQL.append(" ,HZI_YOTO"); // F82: 包材用途
    sbSQL.append(" ,HZI_ZAISHITU"); // F83: 包材材質
    sbSQL.append(" ,HZI_RECYCLE"); // F84: 包材リサイクル対象
    sbSQL.append(" ,KIKANKBN"); // F85: 期間
    sbSQL.append(" ,SHUKYUKBN"); // F86: 酒級
    sbSQL.append(" ,DOSU"); // F87: 度数
    sbSQL.append(" ,CHINRETUCD"); // F88: 陳列形式コード
    sbSQL.append(" ,DANTUMICD"); // F89: 段積み形式コード
    sbSQL.append(" ,KASANARICD"); // F90: 重なりコード
    sbSQL.append(" ,KASANARISZ"); // F91: 重なりサイズ
    sbSQL.append(" ,ASSHUKURT"); // F92: 圧縮率
    sbSQL.append(" ,SHUBETUCD"); // F93: 種別コード
    sbSQL.append(" ,URICD"); // F94: 販売コード
    sbSQL.append(" ,SALESCOMKN"); // F95: 商品コピー・セールスコメント
    sbSQL.append(" ,URABARIKBN"); // F96: 裏貼
    sbSQL.append(" ,PCARD_OPFLG"); // F97: プライスカード出力有無
    sbSQL.append(" ,PARENTCD"); // F98: 親商品コード
    sbSQL.append(" ,BINKBN"); // F99: 便区分
    sbSQL.append(" ,HAT_MONKBN"); // F100: 発注曜日_月
    sbSQL.append(" ,HAT_TUEKBN"); // F101: 発注曜日_火
    sbSQL.append(" ,HAT_WEDKBN"); // F102: 発注曜日_水
    sbSQL.append(" ,HAT_THUKBN"); // F103: 発注曜日_木
    sbSQL.append(" ,HAT_FRIKBN"); // F104: 発注曜日_金
    sbSQL.append(" ,HAT_SATKBN"); // F105: 発注曜日_土
    sbSQL.append(" ,HAT_SUNKBN"); // F106: 発注曜日_日
    sbSQL.append(" ,READTMPTN"); // F107: リードタイムパターン
    sbSQL.append(" ,SIMEKAISU"); // F108: 締め回数
    sbSQL.append(" ,IRYOREFLG"); // F109: 衣料使い回しフラグ
    sbSQL.append(" ,TOROKUMOTO"); // F110: 登録元
    sbSQL.append(" ,UPDKBN"); // F111: 更新区分
    if (TblType.CSV.getVal() != tbl.getVal()) {
      sbSQL.append(" ,SENDFLG"); // F112: 送信フラグ
    }
    sbSQL.append(" ,OPERATOR"); // F113: オペレータ
    if (TblType.CSV.getVal() != tbl.getVal()) {
      sbSQL.append(" ,ADDDT"); // F114: 登録日
    }
    sbSQL.append(" ,UPDDT"); // F115: 更新日
    sbSQL.append(" ,K_HONKB"); // F116: 保温区分
    sbSQL.append(" ,K_WAPNFLG_R"); // F117: デリカワッペン区分_レギュラー
    sbSQL.append(" ,K_WAPNFLG_H"); // F118: デリカワッペン区分_販促
    sbSQL.append(" ,K_TORIKB"); // F119: 取扱区分
    sbSQL.append(" ,ITFCD"); // F120: ITFコード
    sbSQL.append(" ,CENTER_IRISU"); // F121: センター入数
    if (TblType.JNL.getVal() == tbl.getVal()) {
      sbSQL.append(" ,SEQ"); // SEQ
      sbSQL.append(" ,INF_DATE"); // 更新情報_更新日時
      sbSQL.append(" ,INF_OPERATOR"); // 更新情報_オペレータ
      sbSQL.append(" ,INF_TABLEKBN"); // 更新情報_テーブル区分 TODO
      sbSQL.append(" ,INF_TRANKBN"); // 更新情報_処理区分 TODO
    }
    if (TblType.CSV.getVal() == tbl.getVal()) {
      sbSQL.append(" ,SEQ"); // F1 : SEQ
      sbSQL.append(" ,INPUTNO"); // F2 : 入力番号
      sbSQL.append(" ,ERRCD"); // F3 : エラーコード
      sbSQL.append(" ,ERRFLD"); // F4 : エラー箇所
      sbSQL.append(" ,ERRVL"); // F5 : エラー値
      sbSQL.append(" ,ERRTBLNM"); // F6 : エラーテーブル名
      sbSQL.append(" ,CSV_UPDKBN"); // F7 : CSV登録区分
      sbSQL.append(" ,KETAKBN"); // F8 : 桁指定
    }
    sbSQL.append(") ON DUPLICATE KEY UPDATE ");

    sbSQL.append("  SHNCD = VALUES(SHNCD) "); // F1 : 商品コード
    sbSQL.append(" ,YOYAKUDT = VALUES(YOYAKUDT) "); // F2 : マスタ変更予定日
    sbSQL.append(" ,TENBAIKADT = VALUES(TENBAIKADT) "); // F3 : 店売価実施日
    sbSQL.append(" ,YOT_BMNCD = VALUES(YOT_BMNCD) "); // F4 : 用途分類コード_部門
    sbSQL.append(" ,YOT_DAICD = VALUES(YOT_DAICD) "); // F5 : 用途分類コード_大
    sbSQL.append(" ,YOT_CHUCD = VALUES(YOT_CHUCD) "); // F6 : 用途分類コード_中
    sbSQL.append(" ,YOT_SHOCD = VALUES(YOT_SHOCD) "); // F7 : 用途分類コード_小
    sbSQL.append(" ,URI_BMNCD = VALUES(URI_BMNCD) "); // F8 : 売場分類コード_部門
    sbSQL.append(" ,URI_DAICD = VALUES(URI_DAICD) "); // F9 : 売場分類コード_大
    sbSQL.append(" ,URI_CHUCD = VALUES(URI_CHUCD) "); // F10: 売場分類コード_中
    sbSQL.append(" ,URI_SHOCD = VALUES(URI_SHOCD) "); // F11: 売場分類コード_小
    sbSQL.append(" ,BMNCD = VALUES(BMNCD) "); // F12: 標準分類コード_部門
    sbSQL.append(" ,DAICD = VALUES(DAICD) "); // F13: 標準分類コード_大
    sbSQL.append(" ,CHUCD = VALUES(CHUCD) "); // F14: 標準分類コード_中
    sbSQL.append(" ,SHOCD = VALUES(SHOCD) "); // F15: 標準分類コード_小
    sbSQL.append(" ,SSHOCD = VALUES(SSHOCD) "); // F16: 標準分類コード_小小
    sbSQL.append(" ,ATSUK_STDT = VALUES(ATSUK_STDT) "); // F17: 取扱期間_開始日
    sbSQL.append(" ,ATSUK_EDDT = VALUES(ATSUK_EDDT) "); // F18: 取扱期間_終了日
    sbSQL.append(" ,TEISHIKBN = VALUES(TEISHIKBN) "); // F19: 取扱停止
    if (TblType.JNL.getVal() == tbl.getVal()) {
      sbSQL.append(" ,SHOHINAN = VALUES(SHOHINAN) "); // F20: 商品名（カナ）
      sbSQL.append(" ,SHOHINKN = VALUES(SHOHINKN) "); // F21: 商品名（漢字）
    } else {
      sbSQL.append(" ,SHNAN = VALUES(SHNAN) "); // F20: 商品名（カナ）
      sbSQL.append(" ,SHNKN = VALUES(SHNKN) "); // F21: 商品名（漢字）
    }
    sbSQL.append(" ,PCARDKN = VALUES(PCARDKN) "); // F22: プライスカード商品名称（漢字）
    sbSQL.append(" ,POPKN = VALUES(POPKN) "); // F23: POP名称
    sbSQL.append(" ,RECEIPTAN = VALUES(RECEIPTAN) "); // F24: レシート名（カナ）
    sbSQL.append(" ,RECEIPTKN = VALUES(RECEIPTKN) "); // F25: レシート名（漢字）
    sbSQL.append(" ,PCKBN = VALUES(PCKBN) "); // F26: PC区分
    sbSQL.append(" ,KAKOKBN = VALUES(KAKOKBN) "); // F27: 加工区分
    sbSQL.append(" ,ICHIBAKBN = VALUES(ICHIBAKBN) "); // F28: 市場区分
    sbSQL.append(" ,SHNKBN = VALUES(SHNKBN) "); // F29: 商品種類
    sbSQL.append(" ,SANCHIKN = VALUES(SANCHIKN) "); // F30: 産地
    sbSQL.append(" ,SSIRCD = VALUES(SSIRCD) "); // F31: 標準仕入先コード
    sbSQL.append(" ,HSPTN = VALUES(HSPTN) "); // F32: 配送パターン
    sbSQL.append(" ,RG_ATSUKFLG = VALUES(RG_ATSUKFLG) "); // F33: レギュラー情報_取扱フラグ
    sbSQL.append(" ,RG_GENKAAM = VALUES(RG_GENKAAM) "); // F34: レギュラー情報_原価
    sbSQL.append(" ,RG_BAIKAAM = VALUES(RG_BAIKAAM) "); // F35: レギュラー情報_売価
    sbSQL.append(" ,RG_IRISU = VALUES(RG_IRISU) "); // F36: レギュラー情報_店入数
    sbSQL.append(" ,RG_IDENFLG = VALUES(RG_IDENFLG) "); // F37: レギュラー情報_一括伝票フラグ
    sbSQL.append(" ,RG_WAPNFLG = VALUES(RG_WAPNFLG) "); // F38: レギュラー情報_ワッペン
    sbSQL.append(" ,HS_ATSUKFLG = VALUES(HS_ATSUKFLG) "); // F39: 販促情報_取扱フラグ
    sbSQL.append(" ,HS_GENKAAM = VALUES(HS_GENKAAM) "); // F40: 販促情報_原価
    sbSQL.append(" ,HS_BAIKAAM = VALUES(HS_BAIKAAM) "); // F41: 販促情報_売価
    sbSQL.append(" ,HS_IRISU = VALUES(HS_IRISU) "); // F42: 販促情報_店入数
    sbSQL.append(" ,HS_WAPNFLG = VALUES(HS_WAPNFLG) "); // F43: 販促情報_ワッペン
    sbSQL.append(" ,HS_SPOTMINSU = VALUES(HS_SPOTMINSU) "); // F44: 販促情報_スポット最低発注数
    sbSQL.append(" ,HP_SWAPNFLG = VALUES(HP_SWAPNFLG) "); // F45: 販促情報_特売ワッペン
    sbSQL.append(" ,KIKKN = VALUES(KIKKN) "); // F46: 規格名称
    sbSQL.append(" ,UP_YORYOSU = VALUES(UP_YORYOSU) "); // F47: ユニットプライス_容量
    sbSQL.append(" ,UP_TYORYOSU = VALUES(UP_TYORYOSU) "); // F48: ユニットプライス_単位容量
    sbSQL.append(" ,UP_TANIKBN = VALUES(UP_TANIKBN) "); // F49: ユニットプライス_ユニット単位
    if (TblType.JNL.getVal() == tbl.getVal()) {
      sbSQL.append(" ,SHN_YOKOSZ = VALUES(SHN_YOKOSZ) "); // F50: 商品サイズ_横
      sbSQL.append(" ,SHN_TATESZ = VALUES(SHN_TATESZ) "); // F51: 商品サイズ_縦
      sbSQL.append(" ,SHN_OKUSZ = VALUES(SHN_OKUSZ) "); // F52: 商品サイズ_奥行
      sbSQL.append(" ,SHN_JRYOSZ = VALUES(SHN_JRYOSZ) "); // F53: 商品サイズ_重量
    } else {
      sbSQL.append(" ,SHNYOKOSZ = VALUES(SHNYOKOSZ) "); // F50: 商品サイズ_横
      sbSQL.append(" ,SHNTATESZ = VALUES(SHNTATESZ) "); // F51: 商品サイズ_縦
      sbSQL.append(" ,SHNOKUSZ = VALUES(SHNOKUSZ) "); // F52: 商品サイズ_奥行
      sbSQL.append(" ,SHNJRYOSZ = VALUES(SHNJRYOSZ) "); // F53: 商品サイズ_重量
    }
    sbSQL.append(" ,PBKBN = VALUES(PBKBN) "); // F54: PB区分
    sbSQL.append(" ,KOMONOKBM = VALUES(KOMONOKBM) "); // F55: 小物区分
    sbSQL.append(" ,TANAOROKBN = VALUES(TANAOROKBN) "); // F56: 棚卸区分
    sbSQL.append(" ,TEIKEIKBN = VALUES(TEIKEIKBN) "); // F57: 定計区分
    sbSQL.append(" ,ODS_HARUSU = VALUES(ODS_HARUSU) "); // F58: ODS_賞味期限_春
    sbSQL.append(" ,ODS_NATSUSU = VALUES(ODS_NATSUSU) "); // F59: ODS_賞味期限_夏
    sbSQL.append(" ,ODS_AKISU = VALUES(ODS_AKISU) "); // F60: ODS_賞味期限_秋
    sbSQL.append(" ,ODS_FUYUSU = VALUES(ODS_FUYUSU) "); // F61: ODS_賞味期限_冬
    sbSQL.append(" ,ODS_NYUKASU = VALUES(ODS_NYUKASU) "); // F62: ODS_入荷期限
    sbSQL.append(" ,ODS_NEBIKISU = VALUES(ODS_NEBIKISU) "); // F63: ODS_値引期限
    sbSQL.append(" ,PCARD_SHUKBN = VALUES(PCARD_SHUKBN) "); // F64: プライスカード_種類
    sbSQL.append(" ,PCARD_IROKBN = VALUES(PCARD_IROKBN) "); // F65: プライスカード_色
    sbSQL.append(" ,ZEIKBN = VALUES(ZEIKBN) "); // F66: 税区分
    sbSQL.append(" ,ZEIRTKBN = VALUES(ZEIRTKBN) "); // F67: 税率区分
    sbSQL.append(" ,ZEIRTKBN_OLD = VALUES(ZEIRTKBN_OLD) "); // F68: 旧税率区分
    sbSQL.append(" ,ZEIRTHENKODT = VALUES(ZEIRTHENKODT) "); // F69: 税率変更日
    sbSQL.append(" ,SEIZOGENNISU = VALUES(SEIZOGENNISU) "); // F70: 製造限度日数
    sbSQL.append(" ,TEIKANKBN = VALUES(TEIKANKBN) "); // F71: 定貫不定貫区分
    sbSQL.append(" ,MAKERCD = VALUES(MAKERCD) "); // F72: メーカーコード
    sbSQL.append(" ,IMPORTKBN = VALUES(IMPORTKBN) "); // F73: 輸入区分
    sbSQL.append(" ,SIWAKEKBN = VALUES(SIWAKEKBN) "); // F74: 仕分区分
    sbSQL.append(" ,HENPIN_KBN = VALUES(HENPIN_KBN) "); // F75: 返品区分
    sbSQL.append(" ,TAISHONENSU = VALUES(TAISHONENSU) "); // F76: 対象年齢
    sbSQL.append(" ,CALORIESU = VALUES(CALORIESU) "); // F77: カロリー表示
    sbSQL.append(" ,ELPFLG = VALUES(ELPFLG) "); // F78: フラグ情報_ELP
    sbSQL.append(" ,BELLMARKFLG = VALUES(BELLMARKFLG) "); // F79: フラグ情報_ベルマーク
    sbSQL.append(" ,RECYCLEFLG = VALUES(RECYCLEFLG) "); // F80: フラグ情報_リサイクル
    sbSQL.append(" ,ECOFLG = VALUES(ECOFLG) "); // F81: フラグ情報_エコマーク
    sbSQL.append(" ,HZI_YOTO = VALUES(HZI_YOTO) "); // F82: 包材用途
    sbSQL.append(" ,HZI_ZAISHITU = VALUES(HZI_ZAISHITU) "); // F83: 包材材質
    sbSQL.append(" ,HZI_RECYCLE = VALUES(HZI_RECYCLE) "); // F84: 包材リサイクル対象
    sbSQL.append(" ,KIKANKBN = VALUES(KIKANKBN) "); // F85: 期間
    sbSQL.append(" ,SHUKYUKBN = VALUES(SHUKYUKBN) "); // F86: 酒級
    sbSQL.append(" ,DOSU = VALUES(DOSU) "); // F87: 度数
    sbSQL.append(" ,CHINRETUCD = VALUES(CHINRETUCD) "); // F88: 陳列形式コード
    sbSQL.append(" ,DANTUMICD = VALUES(DANTUMICD) "); // F89: 段積み形式コード
    sbSQL.append(" ,KASANARICD = VALUES(KASANARICD) "); // F90: 重なりコード
    sbSQL.append(" ,KASANARISZ = VALUES(KASANARISZ) "); // F91: 重なりサイズ
    sbSQL.append(" ,ASSHUKURT = VALUES(ASSHUKURT) "); // F92: 圧縮率
    sbSQL.append(" ,SHUBETUCD = VALUES(SHUBETUCD) "); // F93: 種別コード
    sbSQL.append(" ,URICD = VALUES(URICD) "); // F94: 販売コード
    sbSQL.append(" ,SALESCOMKN = VALUES(SALESCOMKN) "); // F95: 商品コピー・セールスコメント
    sbSQL.append(" ,URABARIKBN = VALUES(URABARIKBN) "); // F96: 裏貼
    sbSQL.append(" ,PCARD_OPFLG = VALUES(PCARD_OPFLG) "); // F97: プライスカード出力有無
    sbSQL.append(" ,PARENTCD = VALUES(PARENTCD) "); // F98: 親商品コード
    sbSQL.append(" ,BINKBN = VALUES(BINKBN) "); // F99: 便区分
    sbSQL.append(" ,HAT_MONKBN = VALUES(HAT_MONKBN) "); // F100: 発注曜日_月
    sbSQL.append(" ,HAT_TUEKBN = VALUES(HAT_TUEKBN) "); // F101: 発注曜日_火
    sbSQL.append(" ,HAT_WEDKBN = VALUES(HAT_WEDKBN) "); // F102: 発注曜日_水
    sbSQL.append(" ,HAT_THUKBN = VALUES(HAT_THUKBN) "); // F103: 発注曜日_木
    sbSQL.append(" ,HAT_FRIKBN = VALUES(HAT_FRIKBN) "); // F104: 発注曜日_金
    sbSQL.append(" ,HAT_SATKBN = VALUES(HAT_SATKBN) "); // F105: 発注曜日_土
    sbSQL.append(" ,HAT_SUNKBN = VALUES(HAT_SUNKBN) "); // F106: 発注曜日_日
    sbSQL.append(" ,READTMPTN = VALUES(READTMPTN) "); // F107: リードタイムパターン
    sbSQL.append(" ,SIMEKAISU = VALUES(SIMEKAISU) "); // F108: 締め回数
    sbSQL.append(" ,IRYOREFLG = VALUES(IRYOREFLG) "); // F109: 衣料使い回しフラグ
    sbSQL.append(" ,TOROKUMOTO = VALUES(TOROKUMOTO) "); // F110: 登録元
    sbSQL.append(" ,UPDKBN = VALUES(UPDKBN) "); // F111: 更新区分
    if (TblType.CSV.getVal() != tbl.getVal()) {
      sbSQL.append(" ,SENDFLG = VALUES(SENDFLG) "); // F112: 送信フラグ
    }
    sbSQL.append(" ,OPERATOR = VALUES(OPERATOR) "); // F113: オペレータ
    if (TblType.CSV.getVal() != tbl.getVal()) {
      // sbSQL.append(" ,ADDDT = VALUES(ADDDT) "); // F114: 登録日
    }
    sbSQL.append(" ,UPDDT = VALUES(UPDDT) "); // F115: 更新日
    sbSQL.append(" ,K_HONKB = VALUES(K_HONKB) "); // F116: 保温区分
    sbSQL.append(" ,K_WAPNFLG_R = VALUES(K_WAPNFLG_R) "); // F117: デリカワッペン区分_レギュラー
    sbSQL.append(" ,K_WAPNFLG_H = VALUES(K_WAPNFLG_H) "); // F118: デリカワッペン区分_販促
    sbSQL.append(" ,K_TORIKB = VALUES(K_TORIKB) "); // F119: 取扱区分
    sbSQL.append(" ,ITFCD = VALUES(ITFCD) "); // F120: ITFコード
    sbSQL.append(" ,CENTER_IRISU = VALUES(CENTER_IRISU) "); // F121: センター入数
    if (TblType.JNL.getVal() == tbl.getVal()) {
      sbSQL.append(" ,SEQ = VALUES(SEQ) "); // SEQ
      sbSQL.append(" ,INF_DATE = VALUES(INF_DATE) "); // 更新情報_更新日時
      sbSQL.append(" ,INF_OPERATOR = VALUES(INF_OPERATOR) "); // 更新情報_オペレータ
      sbSQL.append(" ,INF_TABLEKBN = VALUES(INF_TABLEKBN) "); // 更新情報_テーブル区分 TODO
      sbSQL.append(" ,INF_TRANKBN = VALUES(INF_TRANKBN) "); // 更新情報_処理区分 TODO
    }
    if (TblType.CSV.getVal() == tbl.getVal()) {
      sbSQL.append(" ,SEQ = VALUES(SEQ) "); // F1 : SEQ
      sbSQL.append(" ,INPUTNO = VALUES(INPUTNO) "); // F2 : 入力番号
      sbSQL.append(" ,ERRCD = VALUES(ERRCD) "); // F3 : エラーコード
      sbSQL.append(" ,ERRFLD = VALUES(ERRFLD) "); // F4 : エラー箇所
      sbSQL.append(" ,ERRVL = VALUES(ERRVL) "); // F5 : エラー値
      sbSQL.append(" ,ERRTBLNM = VALUES(ERRTBLNM) "); // F6 : エラーテーブル名
      sbSQL.append(" ,CSV_UPDKBN = VALUES(CSV_UPDKBN) "); // F7 : CSV登録区分
      sbSQL.append(" ,KETAKBN = VALUES(KETAKBN) "); // F8 : 桁指定
    }


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
  public JSONObject createSqlMSTSIRGPSHN(String userId, String btnId, JSONArray dataArray, TblType tbl, SqlType sql) {
    JSONObject result = new JSONObject();
    // 更新情報
    ArrayList<String> prmData = new ArrayList<>();
    String values = "", names = "", rows = "", set = "";

    int colNum = MSTSIRGPSHNLayout.values().length; // テーブル列数
    if (TblType.JNL.getVal() == tbl.getVal()) {
      colNum += JNLCMNLayout.values().length;
    }
    if (TblType.CSV.getVal() == tbl.getVal()) {
      colNum += CSVCMNLayout.values().length;
    }
    for (int j = 0; j < dataArray.size(); j++) {
      values = "";
      names = "";
      if (j == 1) {
        values += "ROW( ";
      } else {
        values += ",ROW( ";

      }
      for (int i = 1; i <= colNum; i++) {
        String col = "F" + i;
        String val = dataArray.optJSONObject(j).optString(col);

        if (i == MSTSIRGPSHNLayout.SENDFLG.getNo()) { // 送信フラグ
          val = "0";
        } else if (i == MSTSIRGPSHNLayout.OPERATOR.getNo()) { // オペレータ
          val = userId;
        }
        if (TblType.JNL.getVal() == tbl.getVal()) {
          if (i == MSTSIRGPSHNLayout.values().length + 1) { // SEQ(ジャーナル用)
            val = jnlshn_seq;
          } else if (i == MSTSIRGPSHNLayout.values().length + 2) { // RENNO(ジャーナル用)
            val = (j + 1) + "";
          }
        }
        // CSV特殊情報設定
        if (TblType.CSV.getVal() == tbl.getVal()) {
          // SEQ以外dataArrayに追加済み前提
          if (i == MSTSIRGPSHNLayout.values().length + 1) { // SEQ(CSV用)
            val = csvshn_seq;
          }
        }
        if (i != 1) {
          values += " ,";
          names += " ,";
        }if (TblType.CSV.getVal() == tbl.getVal() && (i == 1) && StringUtils.isEmpty(val)) {
          values += " null";
        }else if ((i == 1) && StringUtils.isEmpty(val)) {
          values += " " + SHCD + " ";
        } else if ((i == 2) && (StringUtils.isEmpty(val) || val.equals("0"))) {
          set = "1";
          values += " null";
        } else if ((i == 9 || i == 10)) {
          values += " current_timestamp";
        } else if (StringUtils.isEmpty(val)) {
          values += " null";
        } else {
          if (isTest) {
            values += " '" + val + "'";
          } else {
            prmData.add(val);
            values += "  ? ";
          }
        }
        names += " " + col;
      }
      values += ") ";
      rows += "," + "" + StringUtils.removeStart(values, ",") + "";
    }
    rows = StringUtils.removeStart(rows, ",");
    names = StringUtils.removeStart(names, ",");

    StringBuffer sbSQL;
    sbSQL = new StringBuffer();
    sbSQL.append(this.createMergeCmnCommandMSTSIRGPSHN(tbl, sql, rows, names, set));

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

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
  public String createMergeCmnCommandMSTSIRGPSHN(TblType tbl, SqlType sql, String values, String names, String set) {

    String szTable = "INAMS.MSTSIRGPSHN";
    if (SqlType.DEL.getVal() != sql.getVal()) {
    }
    if (TblType.YYK.getVal() == tbl.getVal()) {
      szTable += "_Y";
    } else if (TblType.JNL.getVal() == tbl.getVal()) {
      szTable = "INAAD.JNLSIRSHN";
    } else if (TblType.CSV.getVal() == tbl.getVal()) {
      szTable = "INAMS.CSVSIRSHN";
    }

    // 基本Merge文
    StringBuffer sbSQL;
    sbSQL = new StringBuffer();
    if (set.equals("1")) {
      sbSQL.append(" WITH T1 AS (");
      sbSQL.append(" select");
      sbSQL.append(" cast(" + MSTSIRGPSHNLayout.SHNCD.getId() + " as CHAR(14)) as " + MSTSIRGPSHNLayout.SHNCD.getCol() + " ");
      sbSQL.append(" ,cast(" + MSTSIRGPSHNLayout.YOYAKUDT.getId() + " as SIGNED) as " + MSTSIRGPSHNLayout.YOYAKUDT.getCol() + " ");
      sbSQL.append(" from(VALUES " + values + ") as RE(" + names + ")");
      sbSQL.append(" ) ");
      sbSQL.append(" DELETE FROM " + szTable + " as T ");
      sbSQL.append(" where ");
      sbSQL.append(" T.SHNCD IN " + " (select T1." + MSTSIRGPSHNLayout.SHNCD.getCol() + " from T1) ");
      if (SqlType.DEL.getVal() != sql.getVal()) {
        sbSQL.append(" AND T.TENGPCD IN " + " (select T1." + MSTSIRGPSHNLayout.YOYAKUDT.getCol() + " from T1) ");
      }
    } else {
      sbSQL.append("INSERT INTO " + szTable + " (");
      sbSQL.append("  SHNCD"); // F1 : 商品コード
      sbSQL.append(" ,TENGPCD"); // F2 : 店グループ
      sbSQL.append(" ,YOYAKUDT"); // F3 : マスタ変更予定日
      sbSQL.append(" ,AREAKBN"); // F4 : エリア区分
      sbSQL.append(" ,SIRCD"); // F5 : 仕入先コード
      sbSQL.append(" ,HSPTN"); // F6 : 配送パターン
      sbSQL.append(" ,SENDFLG"); // F7 : 送信フラグ
      sbSQL.append(" ,OPERATOR"); // F8 : オペレータ
      sbSQL.append(" ,ADDDT"); // F9 : 登録日
      sbSQL.append(" ,UPDDT"); // F10: 更新日
      if (TblType.JNL.getVal() == tbl.getVal()) {
        sbSQL.append(" ,SEQ"); // SEQ
        sbSQL.append(" ,RENNO"); // RENNO
      }
      if (TblType.CSV.getVal() == tbl.getVal()) {
        sbSQL.append(" ,SEQ"); // F1 : SEQ
        sbSQL.append(" ,INPUTNO"); // F2 : 入力番号
        sbSQL.append(" ,INPUTEDANO"); // F3 : 入力枝番
      }
      sbSQL.append(" )VALUES " + values + "");
      sbSQL.append("ON DUPLICATE KEY UPDATE ");
      sbSQL.append("  SHNCD=VALUES(SHNCD)"); // F1 : 商品コード
      sbSQL.append(" ,TENGPCD=VALUES(TENGPCD)"); // F2 : 店グループ
      sbSQL.append(" ,YOYAKUDT=VALUES(YOYAKUDT)"); // F3 : マスタ変更予定日
      sbSQL.append(" ,AREAKBN=VALUES(AREAKBN)"); // F4 : エリア区分
      sbSQL.append(" ,SIRCD=VALUES(SIRCD)"); // F5 : 仕入先コード
      sbSQL.append(" ,HSPTN=VALUES(HSPTN)"); // F6 : 配送パターン
      sbSQL.append(" ,SENDFLG=VALUES(SENDFLG)"); // F7 : 送信フラグ
      sbSQL.append(" ,OPERATOR=VALUES(OPERATOR)"); // F8 : オペレータ
      // sbSQL.append(" ,ADDDT=VALUES(ADDDT)"); // F9 : 登録日
      sbSQL.append(" ,UPDDT=VALUES(UPDDT)"); // F10: 更新日
      if (TblType.JNL.getVal() == tbl.getVal()) {
        sbSQL.append(" ,SEQ=VALUES(SEQ)"); // SEQ
        sbSQL.append(" ,RENNO=VALUES(RENNO)"); // RENNO
      }
      if (TblType.CSV.getVal() == tbl.getVal()) {
        sbSQL.append(" ,SEQ=VALUES(SEQ)"); // F1 : SEQ
        sbSQL.append(" ,INPUTNO=VALUES(INPUTNO)"); // F2 : 入力番号
        sbSQL.append(" ,INPUTEDANO=VALUES(INPUTEDANO)"); // F3 : 入力枝番
      }
    }
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
  public JSONObject createSqlMSTBAIKACTL(String userId, String btnId, JSONArray dataArray, TblType tbl, SqlType sql) {
    JSONObject result = new JSONObject();
    String szTable = "INAMS.MSTBAIKACTL";
    String szWhere = "T.SHNCD = RE.SHNCD";

    if (TblType.YYK.getVal() == tbl.getVal()) {
      szTable += "_Y";

    } else if (TblType.JNL.getVal() == tbl.getVal()) {
      szTable = "INAAD.JNLBAIKACTL";

    } else if (TblType.CSV.getVal() == tbl.getVal()) {
      szTable = "INAMS.CSVBAIKACTL";

    }

    // 更新情報
    ArrayList<String> prmData = new ArrayList<String>();
    String values = "", names = "", rows = "";
    int colNum = MSTBAIKACTLLayout.values().length; // テーブル列数
    if (TblType.JNL.getVal() == tbl.getVal()) {
      colNum += JNLCMNLayout.values().length;
    }
    if (TblType.CSV.getVal() == tbl.getVal()) {
      colNum += CSVCMNLayout.values().length;
    }
    for (int j = 0; j < dataArray.size(); j++) {
      values = "";
      names = "";
      for (int i = 1; i <= colNum; i++) {
        String col = "F" + i;
        String val = dataArray.optJSONObject(j).optString(col);

        if (i == MSTBAIKACTLLayout.SENDFLG.getNo()) { // 送信フラグ
          val = DefineReport.Values.SENDFLG_UN.getVal();
        } else if (i == MSTBAIKACTLLayout.OPERATOR.getNo()) { // オペレータ
          val = userId;
        }
        if (TblType.JNL.getVal() == tbl.getVal()) {
          if (i == MSTBAIKACTLLayout.values().length + 1) { // SEQ(ジャーナル用)
            val = jnlshn_seq;
          } else if (i == MSTBAIKACTLLayout.values().length + 2) { // RENNO(ジャーナル用)
            val = (j + 1) + "";
          }
        }
        // CSV特殊情報設定
        if (TblType.CSV.getVal() == tbl.getVal()) {
          // SEQ以外dataArrayに追加済み前提
          if (i == MSTBAIKACTLLayout.values().length + 1) { // SEQ(CSV用)
            val = csvshn_seq;
          }
        }


        if (StringUtils.isEmpty(val)) {
          values += ", null";
        } else {
          if (isTest) {
            values += ", '" + val + "'";
          } else {
            prmData.add(val);
            values += ", ? ";
          }
        }
        names += ", " + col;
      }
      rows += ",(" + StringUtils.removeStart(values, ",") + ")";
    }
    rows = StringUtils.removeStart(rows, ",");
    names = StringUtils.removeStart(names, ",");

    StringBuffer sbSQL;
    sbSQL = new StringBuffer();
    if (SqlType.DEL.getVal() == sql.getVal()) {
      sbSQL.append("DELETE FROM " + szTable + " WHERE SHNCD = (");
      sbSQL.append("SELECT SHNCD FROM ( ");
      sbSQL.append(this.createMergeCmnCommandMSTBAIKACTL(tbl, sql, rows, names));
      sbSQL.append(") ");
    } else {
      sbSQL.append("INSERT INTO " + szTable + "( ");
      sbSQL.append("  SHNCD"); // F1 : 商品コード
      sbSQL.append(" ,TENGPCD"); // F2 : 店グループ
      sbSQL.append(" ,YOYAKUDT"); // F3 : マスタ変更予定日
      sbSQL.append(" ,AREAKBN"); // F4 : エリア区分
      sbSQL.append(" ,GENKAAM"); // F5 : 原価
      sbSQL.append(" ,BAIKAAM"); // F6 : 売価
      sbSQL.append(" ,IRISU"); // F7 : 店入数
      sbSQL.append(" ,SENDFLG"); // F8 : 送信フラグ
      sbSQL.append(" ,OPERATOR"); // F9 : オペレータ
      sbSQL.append(" ,ADDDT"); // F10: 登録日
      sbSQL.append(" ,UPDDT"); // F11: 更新日

      if (TblType.JNL.getVal() == tbl.getVal()) {
        sbSQL.append(" ,SEQ"); // SEQ
        sbSQL.append(" ,RENNO"); // RENNO

      }
      if (TblType.CSV.getVal() == tbl.getVal()) {
        sbSQL.append(" ,SEQ"); // F1 : SEQ
        sbSQL.append(" ,INPUTNO"); // F2 : 入力番号
        sbSQL.append(" ,INPUTEDANO"); // F3 : 入力枝番
      }

      sbSQL.append(") ");
      sbSQL.append("SELECT * FROM ( ");

      sbSQL.append(this.createMergeCmnCommandMSTBAIKACTL(tbl, sql, rows, names));
      sbSQL.append("ON DUPLICATE KEY UPDATE ");

      if (TblType.JNL.getVal() == tbl.getVal()) {
        sbSQL.append("  SEQ = VALUES(SEQ) "); // SEQ
        sbSQL.append(" ,RENNO = VALUES(RENNO) "); // RENNO
        sbSQL.append(" ,");
      }
      if (TblType.CSV.getVal() == tbl.getVal()) {
        sbSQL.append("  SEQ = VALUES(SEQ) "); // F1 : SEQ
        sbSQL.append(" ,INPUTNO = VALUES(INPUTNO) "); // F2 : 入力番号
        sbSQL.append(" ,INPUTEDANO = VALUES(INPUTEDANO) "); // F3 : 入力枝番
        sbSQL.append(" ,");
      }
      sbSQL.append("  SHNCD = VALUES(SHNCD) "); // F1 : 商品コード
      sbSQL.append(" ,TENGPCD = VALUES(TENGPCD) "); // F2 : 店グループ
      sbSQL.append(" ,YOYAKUDT = VALUES(YOYAKUDT) "); // F3 : マスタ変更予定日
      sbSQL.append(" ,AREAKBN = VALUES(AREAKBN) "); // F4 : エリア区分
      sbSQL.append(" ,GENKAAM = VALUES(GENKAAM) "); // F5 : 原価
      sbSQL.append(" ,BAIKAAM = VALUES(BAIKAAM) "); // F6 : 売価
      sbSQL.append(" ,IRISU = VALUES(IRISU) "); // F7 : 店入数
      sbSQL.append(" ,UPDDT = CURRENT_TIMESTAMP "); // F11: 更新日
    }

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

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

    String szTable = "INAMS.MSTBAIKACTL";
    String szWhere = "T.SHNCD = RE.SHNCD";
    String szROW = "";
    if (SqlType.DEL.getVal() != sql.getVal()) {
      szWhere += " and T.TENGPCD = RE.TENGPCD";

    }
    if (TblType.YYK.getVal() == tbl.getVal()) {
      szTable += "_Y";
      szWhere += " and T.YOYAKUDT = RE.YOYAKUDT ";
    } else if (TblType.JNL.getVal() == tbl.getVal()) {
      szTable = "INAAD.JNLBAIKACTL";
      szWhere = "T.SEQ = RE.SEQ";
    } else if (TblType.CSV.getVal() == tbl.getVal()) {
      szTable = "INAMS.CSVBAIKACTL";
      szWhere = "T.SEQ = RE.SEQ and T.INPUTNO = RE.INPUTNO and T.INPUTEDANO = RE.INPUTEDANO";
    }

    // 基本Merge文
    StringBuffer sbSQL;
    sbSQL = new StringBuffer();

    sbSQL.append("SELECT ");
    if (SqlType.DEL.getVal() == sql.getVal()) {
      sbSQL.append(MSTBAIKACTLLayout.SHNCD.getId() + " as " + MSTBAIKACTLLayout.SHNCD.getCol()); // 商品コード
      sbSQL.append(" ," + MSTBAIKACTLLayout.YOYAKUDT.getId() + " as " + MSTBAIKACTLLayout.YOYAKUDT.getCol()); // マスタ変更予定日
    } else {
      int baseCnt = MSTBAIKACTLLayout.values().length;
      for (MSTBAIKACTLLayout itm : MSTBAIKACTLLayout.values()) {
        if (itm.getNo() > 1) {
          sbSQL.append(" ,");
        }
        if (itm.getId().equals(MSTBAIKACTLLayout.ADDDT.getId())) {
          sbSQL.append("CURRENT_TIMESTAMP as ADDDT");
        }else if (itm.getId().equals(MSTBAIKACTLLayout.UPDDT.getId())) {
          sbSQL.append("CURRENT_TIMESTAMP as UPDDT");
        }else {
        sbSQL.append(itm.getId() + " as " + itm.getCol());
        }
      }
      if (TblType.JNL.getVal() == tbl.getVal()) {
        for (JNLCMNLayout itm : JNLCMNLayout.values()) {

          sbSQL.append(" ," + itm.getId2(baseCnt) + " as " + itm.getCol());
        }
      }
      if (TblType.CSV.getVal() == tbl.getVal()) {
        for (CSVCMNLayout itm : CSVCMNLayout.values()) {
          sbSQL.append(" ," + itm.getId2(baseCnt) + " as " + itm.getCol());
        }
      }
    }
    szROW = " ROW "+ values.replaceAll("\\)\\s*,\\s*\\(", "), ROW (");
    sbSQL.append(" from (values " + szROW + ") as T1(" + names + ") )AS T1 ");
    
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
  public JSONObject createSqlMSTSRCCD(String userId, String btnId, JSONArray dataArray, TblType tbl, SqlType sql) {
    JSONObject result = new JSONObject();


    // 更新情報
    ArrayList<String> prmData = new ArrayList<>();
    String values = "", names = "", rows = "", set = "";
    int colNum = MSTSRCCDLayout.values().length; // テーブル列数
    if (TblType.JNL.getVal() == tbl.getVal()) {
      colNum += 2;
    }
    if (TblType.CSV.getVal() == tbl.getVal()) {
      colNum += CSVCMNLayout.values().length;
    }
    for (int j = 0; j < dataArray.size(); j++) {
      values = "";
      names = "";
      if (j == 1) {
        values += "ROW( ";
      } else {
        values += ",ROW( ";

      }
      for (int i = 1; i <= colNum; i++) {
        String col = "F" + i;
        String val = dataArray.optJSONObject(j).optString(col);

        if (i == MSTSRCCDLayout.SENDFLG.getNo()) { // 送信フラグ
          val = "0";
        } else if (i == MSTSRCCDLayout.OPERATOR.getNo()) { // オペレータ
          val = userId;
        }
        if (TblType.JNL.getVal() == tbl.getVal()) {
          if (i == MSTSRCCDLayout.values().length + 1) { // SEQ(ジャーナル用)
            val = jnlshn_seq;
          } else if (i == MSTSRCCDLayout.values().length + 2) { // RENNO(ジャーナル用)
            val = (j + 1) + "";
          }
        }
        // CSV特殊情報設定
        if (TblType.CSV.getVal() == tbl.getVal()) {
          // SEQ以外dataArrayに追加済み前提
          if (i == MSTSRCCDLayout.values().length + 1) { // SEQ(CSV用)
            val = csvshn_seq;
          }
        }

        // TMP特殊情報設定
        if (TblType.TMP.getVal() == tbl.getVal()) {
          // dataArrayに追加前提
        }
        if (i != 1) {
          values += " ,";
          names += " ,";
        }
        if (TblType.CSV.getVal() == tbl.getVal() && (i == 1) && StringUtils.isEmpty(val)) {
          values += " null";
        }else if ((i == 1) && StringUtils.isEmpty(val)) {
          values += " " + SHCD + " ";
        } else if ((i == 2) && StringUtils.isEmpty(val)) {
          set = "1";
          values += " null";
        } else if ((i == 8 || i == 9)) {
          values += " current_timestamp";
        } else if (StringUtils.isEmpty(val)) {
          values += " null";
        } else {
          prmData.add(val);
          values += "? ";
        }
        names += " " + col;
      }
      values += ") ";
      rows += "," + StringUtils.removeStart(values, ",") + "";

    }
    rows = StringUtils.removeStart(rows, ",");
    names = StringUtils.removeStart(names, ",");

    StringBuffer sbSQL;
    sbSQL = new StringBuffer();
    sbSQL.append(this.createMergeCmnCommandMSTSRCCD(tbl, sql, rows, names, set));
    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

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
  public String createMergeCmnCommandMSTSRCCD(TblType tbl, SqlType sql, String values, String names, String set) {

    String szTable = "INAMS.MSTSRCCD";
    if (SqlType.DEL.getVal() != sql.getVal()) {
    }
    if (TblType.YYK.getVal() == tbl.getVal()) {
      szTable += "_Y";
    } else if (TblType.JNL.getVal() == tbl.getVal()) {
      szTable = "INAAD.JNLSRCCD";
    } else if (TblType.CSV.getVal() == tbl.getVal()) {
      szTable = "INAMS.CSVSRCCD";
    }

    MSTSRCCDLayout.values();
    // 基本Merge文
    StringBuffer sbSQL;
    sbSQL = new StringBuffer();

    if (set.equals("1")) {
      sbSQL.append(" WITH T1 AS (");
      sbSQL.append(" select");
      sbSQL.append(" cast(" + MSTSRCCDLayout.SHNCD.getId() + " as CHAR(14)) as " + MSTSRCCDLayout.SHNCD.getCol() + " ");
      sbSQL.append(" ,cast(" + MSTSRCCDLayout.YOYAKUDT.getId() + " as SIGNED) as " + MSTSRCCDLayout.YOYAKUDT.getCol() + " ");
      sbSQL.append(" from(VALUES " + values + ") as RE(" + names + ")");
      sbSQL.append(" ) ");
      sbSQL.append(" DELETE FROM " + szTable + " as T ");
      sbSQL.append(" where ");
      sbSQL.append(" T.SHNCD IN " + " (select T1." + MSTSRCCDLayout.SHNCD.getCol() + " from T1) ");
      if (SqlType.DEL.getVal() != sql.getVal()) {
        sbSQL.append(" AND T.TENGPCD IN " + " (select T1." + MSTSRCCDLayout.YOYAKUDT.getCol() + " from T1) ");
      }
    } else {

      sbSQL.append("INSERT INTO " + szTable + " ( ");
      sbSQL.append("  SHNCD"); // F1 : 商品コード
      sbSQL.append(" ,SRCCD"); // F2 : ソースコード
      sbSQL.append(" ,YOYAKUDT"); // F3 : マスタ変更予定日
      sbSQL.append(" ,SEQNO"); // F4 : 入力順番
      if (TblType.CSV.getVal() == tbl.getVal() || TblType.JNL.getVal() == tbl.getVal()) {
        sbSQL.append(" ,SRCKBN"); // F5 : ソース区分
      } else {
        sbSQL.append(" ,SOURCEKBN"); // F5 : ソース区分
      }
      sbSQL.append(" ,SENDFLG"); // F6 : 送信フラグ
      sbSQL.append(" ,OPERATOR"); // F7 : オペレータ
      sbSQL.append(" ,ADDDT"); // F8 : 登録日
      sbSQL.append(" ,UPDDT"); // F9 : 更新日
      sbSQL.append(" ,YUKO_STDT"); // F10: 有効開始日
      sbSQL.append(" ,YUKO_EDDT"); // F11: 有効終了日
      if (TblType.JNL.getVal() == tbl.getVal()) {
        sbSQL.append(" ,SEQ"); // F1 : SEQ
        sbSQL.append(" ,RENNO"); // F2 : RENNO
      }
      if (TblType.CSV.getVal() == tbl.getVal()) {
        sbSQL.append(" ,SEQ"); // F1 : SEQ
        sbSQL.append(" ,INPUTNO"); // F2 : 入力番号
        sbSQL.append(" ,INPUTEDANO"); // F3 : 入力枝番
      }
      if (TblType.TMP.getVal() == tbl.getVal()) {
        sbSQL.append(" ,SESID"); // F1 : セッションID
      }
      sbSQL.append(" ) SELECT * FROM (VALUES " + values + ") ");
      sbSQL.append("AS TMP( ");
      sbSQL.append("  SHNCD"); // F1 : 商品コード
      sbSQL.append(" ,SRCCD"); // F2 : ソースコード
      sbSQL.append(" ,YOYAKUDT"); // F3 : マスタ変更予定日
      sbSQL.append(" ,SEQNO"); // F4 : 入力順番
      if (TblType.CSV.getVal() == tbl.getVal() || TblType.JNL.getVal() == tbl.getVal()) {
        sbSQL.append(" ,SRCKBN"); // F5 : ソース区分
      } else {
        sbSQL.append(" ,SOURCEKBN"); // F5 : ソース区分
      }
      sbSQL.append(" ,SENDFLG"); // F6 : 送信フラグ
      sbSQL.append(" ,OPERATOR"); // F7 : オペレータ
      sbSQL.append(" ,ADDDT"); // F8 : 登録日
      sbSQL.append(" ,UPDDT"); // F9 : 更新日
      sbSQL.append(" ,YUKO_STDT"); // F10: 有効開始日
      sbSQL.append(" ,YUKO_EDDT"); // F11: 有効終了日
      if (TblType.JNL.getVal() == tbl.getVal()) {
        sbSQL.append(" ,SEQ"); // F1 : SEQ
        sbSQL.append(" ,RENNO"); // F2 : RENNO
      }
      if (TblType.CSV.getVal() == tbl.getVal()) {
        sbSQL.append(" ,SEQ"); // F1 : SEQ
        sbSQL.append(" ,INPUTNO"); // F2 : 入力番号
        sbSQL.append(" ,INPUTEDANO"); // F3 : 入力枝番
      }
      if (TblType.TMP.getVal() == tbl.getVal()) {
        sbSQL.append(" ,SESID"); // F1 : セッションID
      }
      sbSQL.append(") ON DUPLICATE KEY UPDATE "); // F1 : セッションID
      sbSQL.append("  SHNCD=VALUES(SHNCD) "); // F1 : 商品コード
      sbSQL.append(" ,SRCCD=VALUES(SRCCD) "); // F2 : ソースコード
      sbSQL.append(" ,YOYAKUDT=VALUES(YOYAKUDT) "); // F3 : マスタ変更予定日
      sbSQL.append(" ,SEQNO=VALUES(SEQNO) "); // F4 : 入力順番
      if (TblType.CSV.getVal() == tbl.getVal() || TblType.JNL.getVal() == tbl.getVal()) {
        sbSQL.append(" ,SRCKBN=VALUES(SRCKBN) "); // F5 : ソース区分
      } else {
        sbSQL.append(" ,SOURCEKBN=VALUES(SOURCEKBN) "); // F5 : ソース区分
      }
      sbSQL.append(" ,SENDFLG=VALUES(SENDFLG) "); // F6 : 送信フラグ
      sbSQL.append(" ,OPERATOR=VALUES(OPERATOR) "); // F7 : オペレータ
      // sbSQL.append(" ,ADDDT=VALUES(ADDDT) "); // F8 : 登録日
      sbSQL.append(" ,UPDDT=VALUES(UPDDT) "); // F9 : 更新日
      sbSQL.append(" ,YUKO_STDT=VALUES(YUKO_STDT) "); // F10: 有効開始日
      sbSQL.append(" ,YUKO_EDDT=VALUES(YUKO_EDDT) "); // F11: 有効終了日
      if (TblType.JNL.getVal() == tbl.getVal()) {
        sbSQL.append(" ,SEQ=VALUES(SEQ) "); // F1 : SEQ
        sbSQL.append(" ,RENNO=VALUES(RENNO) "); // F2 : RENNO
      }
      if (TblType.CSV.getVal() == tbl.getVal()) {
        sbSQL.append(" ,SEQ=VALUES(SEQ) "); // F1 : SEQ
        sbSQL.append(" ,INPUTNO=VALUES(INPUTNO) "); // F2 : 入力番号
        sbSQL.append(" ,INPUTEDANO=VALUES(INPUTEDANO) "); // F3 : 入力枝番
      }
      if (TblType.TMP.getVal() == tbl.getVal()) {
        sbSQL.append(" ,SESID=VALUES(SESID) "); // F1 : セッションID
      }
    }
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
  public JSONObject createSqlMSTTENKABUTSU(String userId, String btnId, JSONArray dataArray, TblType tbl, SqlType sql) {
    JSONObject result = new JSONObject();


    // 更新情報
    ArrayList<String> prmData = new ArrayList<>();
    String values = "", names = "", rows = "", set = "";
    int colNum = MSTTENKABUTSULayout.values().length; // テーブル列数
    if (TblType.JNL.getVal() == tbl.getVal()) {
      colNum += JNLCMNLayout.values().length;
    }
    if (TblType.CSV.getVal() == tbl.getVal()) {
      colNum += CSVCMNLayout.values().length;
    }
    for (int j = 0; j < dataArray.size(); j++) {
      values = "";
      names = "";
      for (int i = 1; i <= colNum; i++) {
        String col = "F" + i;
        String val = dataArray.optJSONObject(j).optString(col);

        if (i == MSTTENKABUTSULayout.SENDFLG.getNo()) { // 送信フラグ
          val = "0";
        } else if (i == MSTTENKABUTSULayout.OPERATOR.getNo()) { // オペレータ
          val = "" + userId;
        }
        if (TblType.JNL.getVal() == tbl.getVal()) {
          if (i == MSTTENKABUTSULayout.values().length + 1) { // SEQ(ジャーナル用)
            val = jnlshn_seq;
          } else if (i == MSTTENKABUTSULayout.values().length + 2) { // RENNO(ジャーナル用)
            val = (j + 1) + "";
          }
        }
        // CSV特殊情報設定
        if (TblType.CSV.getVal() == tbl.getVal()) {
          // SEQ以外dataArrayに追加済み前提
          if (i == MSTTENKABUTSULayout.values().length + 1) { // SEQ(CSV用)
            val = csvshn_seq;
          }
        }
        if (i != 1) {
          values += " ,";
          names += " ,";
        }
        if (((i == 2) || (i == 3)) && StringUtils.isEmpty(val) || val.equals("0")) {
          set = "1";
          values += " null";
        } else if ((i == 7 || i == 8)) {
          values += " current_timestamp";
        } else if (StringUtils.isEmpty(val)) {
          values += " null";
        } else {
          if (isTest) {
            values += " '" + val + "'";
          } else {
            prmData.add(val);
            values += "? ";
          }
        }
        names += " " + col;
      }
      if (SqlType.DEL.getVal() == sql.getVal()) {
        rows += "," + StringUtils.removeStart(values, ",") + "";
      }else {
      rows += ",(" + StringUtils.removeStart(values, ",") + ")";
      }
    }
    rows = StringUtils.removeStart(rows, ",");
    names = StringUtils.removeStart(names, ",");

    StringBuffer sbSQL;
    sbSQL = new StringBuffer();
    sbSQL.append(this.createMergeCmnCommandMSTTENKABUTSU(tbl, sql, rows, names, set));
    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

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
  public String createMergeCmnCommandMSTTENKABUTSU(TblType tbl, SqlType sql, String values, String names, String set) {

    String szTable = "INAMS.MSTTENKABUTSU";
    if (SqlType.DEL.getVal() != sql.getVal()) {
    }
    if (TblType.YYK.getVal() == tbl.getVal()) {
      szTable += "_Y";
    } else if (TblType.JNL.getVal() == tbl.getVal()) {
      szTable = "INAAD.JNLTENKABUTSU";
    } else if (TblType.CSV.getVal() == tbl.getVal()) {
      szTable = "INAMS.CSVTENKABUTSU";
    }

    // 基本Merge文
    StringBuffer sbSQL;
    sbSQL = new StringBuffer();
    if (SqlType.DEL.getVal() == sql.getVal()) {
      sbSQL.append(" WITH T1 AS (");
      sbSQL.append(" select");
      sbSQL.append(" cast(" + MSTTENKABUTSULayout.SHNCD.getId() + " as CHAR(14)) as " + MSTTENKABUTSULayout.SHNCD.getCol() + " ");
      sbSQL.append(" ,cast(" + MSTTENKABUTSULayout.YOYAKUDT.getId() + " as SIGNED) as " + MSTTENKABUTSULayout.YOYAKUDT.getCol() + " ");
      sbSQL.append(" from(VALUES ROW(" + values + ")) as RE(" + names + ")");
      sbSQL.append(" ) ");
      sbSQL.append(" DELETE FROM " + szTable + " as T ");
      sbSQL.append(" where ");
      sbSQL.append(" T.SHNCD IN " + " (select T1." + MSTTENKABUTSULayout.SHNCD.getCol() + " from T1) ");
    } else {
      sbSQL.append("INSERT INTO " + szTable + " ( ");
      sbSQL.append("  SHNCD"); // F1 : 商品コード
      sbSQL.append(" ,TENKABKBN"); // F2 : 添加物区分
      sbSQL.append(" ,TENKABCD"); // F3 : 添加物コード
      sbSQL.append(" ,YOYAKUDT"); // F4 : マスタ変更予定日
      sbSQL.append(" ,SENDFLG"); // F5 : 送信フラグ
      sbSQL.append(" ,OPERATOR"); // F6 : オペレータ
      sbSQL.append(" ,ADDDT"); // F7 : 登録日
      sbSQL.append(" ,UPDDT"); // F8 : 更新日
      if (TblType.JNL.getVal() == tbl.getVal()) {
        sbSQL.append(" ,SEQ"); // SEQ
        sbSQL.append(" ,RENNO"); // RENNO
      }
      if (TblType.CSV.getVal() == tbl.getVal()) {
        sbSQL.append(" ,SEQ"); // F1 : SEQ
        sbSQL.append(" ,INPUTNO"); // F2 : 入力番号
        sbSQL.append(" ,INPUTEDANO"); // F3 : 入力枝番
      }
      sbSQL.append(" )VALUES " + values + "");      
      sbSQL.append(" ON DUPLICATE KEY UPDATE ");
      sbSQL.append("  SHNCD=VALUES(SHNCD)"); // F1 : 商品コード
      sbSQL.append(" ,TENKABKBN=VALUES(TENKABKBN)"); // F2 : 添加物区分
      sbSQL.append(" ,TENKABCD=VALUES(TENKABCD)"); // F3 : 添加物コード
      sbSQL.append(" ,YOYAKUDT=VALUES(YOYAKUDT)"); // F4 : マスタ変更予定日
      sbSQL.append(" ,SENDFLG=VALUES(SENDFLG)"); // F5 : 送信フラグ
      sbSQL.append(" ,OPERATOR=VALUES(OPERATOR)"); // F6 : オペレータ
      // sbSQL.append(" ,ADDDT=VALUES(ADDDT)"); // F7 : 登録日
      sbSQL.append(" ,UPDDT=VALUES(UPDDT)"); // F8 : 更新日
      if (TblType.JNL.getVal() == tbl.getVal()) {
        sbSQL.append(" ,SEQ=VALUES(SEQ)"); // SEQ
        sbSQL.append(" ,RENNO=VALUES(RENNO)"); // RENNO
      }
      if (TblType.CSV.getVal() == tbl.getVal()) {
        sbSQL.append(" ,SEQ=VALUES(SEQ)"); // F1 : SEQ
        sbSQL.append(" ,INPUTNO=VALUES(INPUTNO)"); // F2 : 入力番号
        sbSQL.append(" ,INPUTEDANO=VALUES(INPUTEDANO)"); // F3 : 入力枝番
      }
    }
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
  public JSONObject createSqlMSTSHINAGP(String userId, String btnId, JSONArray dataArray, TblType tbl, SqlType sql) {
    JSONObject result = new JSONObject();


    // 更新情報
    ArrayList<String> prmData = new ArrayList<>();
    String values = "", names = "", rows = "", set = "";
    int colNum = MSTSHINAGPLayout.values().length; // テーブル列数
    if (TblType.JNL.getVal() == tbl.getVal()) {
      colNum += 2;
    }
    if (TblType.CSV.getVal() == tbl.getVal()) {
      colNum += CSVCMNLayout.values().length;
    }
    for (int j = 0; j < dataArray.size(); j++) {
      values = "";
      names = "";
      for (int i = 1; i <= colNum; i++) {
        String col = "F" + i;
        String val = dataArray.optJSONObject(j).optString(col);

        if (i == MSTSHINAGPLayout.SENDFLG.getNo()) { // 送信フラグ
          val = DefineReport.Values.SENDFLG_UN.getVal();
        } else if (i == MSTSHINAGPLayout.OPERATOR.getNo()) { // オペレータ
          val = "" + userId;
        }
        if (TblType.JNL.getVal() == tbl.getVal()) {
          if (i == MSTSHINAGPLayout.values().length + 1) { // SEQ(ジャーナル用)
            val = jnlshn_seq;
          } else if (i == MSTSHINAGPLayout.values().length + 2) { // RENNO(ジャーナル用)
            val = (j + 1) + "";
          }
        }
        // CSV特殊情報設定
        if (TblType.CSV.getVal() == tbl.getVal()) {
          // SEQ以外dataArrayに追加済み前提
          if (i == MSTSHINAGPLayout.values().length + 1) { // SEQ(CSV用)
            val = csvshn_seq;
          }
        }
        if (i != 1) {
          values += " ,";
          names += " ,";
        }
        if ((i == 2) && StringUtils.isEmpty(val) || val.equals("0")) {
          set = "1";
          values += " null";
        } else if ((i == 8 || i == 9)) {
          values += " current_timestamp";
        } else if (StringUtils.isEmpty(val)) {
          values += " null";
        } else {
          if (isTest) {
            values += " '" + val + "'";
          } else {
            prmData.add(val);
            values += "? ";
          }
        }
        names += " " + col;
      }
      rows += "," + StringUtils.removeStart(values, ",") + "";
    }
    rows = StringUtils.removeStart(rows, ",");
    names = StringUtils.removeStart(names, ",");

    StringBuffer sbSQL;
    sbSQL = new StringBuffer();
    sbSQL.append(this.createMergeCmnCommandMSTSHINAGP(tbl, sql, rows, names, set));
    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

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
  public String createMergeCmnCommandMSTSHINAGP(TblType tbl, SqlType sql, String values, String names, String set) {

    String szTable = "INAMS.MSTSHINAGP";
    if (SqlType.DEL.getVal() != sql.getVal()) {
    }
    if (TblType.YYK.getVal() == tbl.getVal()) {
      szTable += "_Y";
    } else if (TblType.JNL.getVal() == tbl.getVal()) {
      szTable = "INAAD.JNLSHINAGP";
    } else if (TblType.CSV.getVal() == tbl.getVal()) {
      szTable = "INAMS.CSVSHINAGP";
    }

    // 基本Merge文
    StringBuffer sbSQL;
    sbSQL = new StringBuffer();
    if (set.equals("1")) {
      sbSQL.append(" WITH T1 AS (");
      sbSQL.append(" select");
      sbSQL.append("  cast(" + MSTSHINAGPLayout.SHNCD.getId() + " as CHAR(14)) as " + MSTSHINAGPLayout.SHNCD.getCol() + " ");
      sbSQL.append(" ,cast(" + MSTSHINAGPLayout.YOYAKUDT.getId() + " as SIGNED) as " + MSTSHINAGPLayout.YOYAKUDT.getCol() + " ");
      sbSQL.append(" from(VALUES ROW(" + values + ")) as RE(" + names + ")");
      sbSQL.append(" ) ");
      sbSQL.append(" DELETE FROM " + szTable + " as T ");
      sbSQL.append(" where ");
      sbSQL.append(" T.SHNCD IN " + " (select T1." + MSTSHINAGPLayout.SHNCD.getCol() + " from T1) ");
      if (SqlType.DEL.getVal() != sql.getVal()) {
        sbSQL.append(" AND T.TENGPCD IN " + " (select T1." + MSTSHINAGPLayout.YOYAKUDT.getCol() + " from T1) ");
      }
    } else {
      sbSQL.append("INSERT INTO " + szTable + " ( ");
      sbSQL.append("  SHNCD"); // F1 : 商品コード
      sbSQL.append(" ,TENGPCD"); // F2 : 店グループ
      sbSQL.append(" ,YOYAKUDT"); // F3 : マスタ変更予定日
      sbSQL.append(" ,AREAKBN"); // F4 : エリア区分
      sbSQL.append(" ,ATSUKKBN"); // F5 : 扱い区分
      sbSQL.append(" ,SENDFLG"); // F6 : 送信フラグ
      sbSQL.append(" ,OPERATOR"); // F7 : オペレータ
      sbSQL.append(" ,ADDDT"); // F8 : 登録日
      sbSQL.append(" ,UPDDT"); // F9 : 更新日
      if (TblType.JNL.getVal() == tbl.getVal()) {
        sbSQL.append(" ,SEQ"); // SEQ
        sbSQL.append(" ,RENNO"); // RENNO
      }
      if (TblType.CSV.getVal() == tbl.getVal()) {
        sbSQL.append(" ,SEQ"); // F1 : SEQ
        sbSQL.append(" ,INPUTNO"); // F2 : 入力番号
        sbSQL.append(" ,INPUTEDANO"); // F3 : 入力枝番
      }
      sbSQL.append(" )VALUES (" + values + ")");
      sbSQL.append("ON DUPLICATE KEY UPDATE ");
      sbSQL.append("  SHNCD=VALUES(SHNCD)"); // F1 : 商品コード
      sbSQL.append(" ,TENGPCD=VALUES(TENGPCD)"); // F2 : 店グループ
      sbSQL.append(" ,YOYAKUDT=VALUES(YOYAKUDT)"); // F3 : マスタ変更予定日
      sbSQL.append(" ,AREAKBN=VALUES(AREAKBN)"); // F4 : エリア区分
      sbSQL.append(" ,ATSUKKBN=VALUES(ATSUKKBN)"); // F5 : 扱い区分
      sbSQL.append(" ,SENDFLG=VALUES(SENDFLG)"); // F6 : 送信フラグ
      sbSQL.append(" ,OPERATOR=VALUES(OPERATOR)"); // F7 : オペレータ
      // sbSQL.append(" ,ADDDT=VALUES(ADDDT)"); // F8 : 登録日
      sbSQL.append(" ,UPDDT=VALUES(UPDDT)"); // F9 : 更新日
      if (TblType.JNL.getVal() == tbl.getVal()) {
        sbSQL.append(" ,SEQ=VALUES(SEQ)"); // SEQ
        sbSQL.append(" ,RENNO=VALUES(RENNO)"); // RENNO
      }
      if (TblType.CSV.getVal() == tbl.getVal()) {
        sbSQL.append(" ,SEQ=VALUES(SEQ)"); // F1 : SEQ
        sbSQL.append(" ,INPUTNO=VALUES(INPUTNO)"); // F2 : 入力番号
        sbSQL.append(" ,INPUTEDANO=VALUES(INPUTEDANO)"); // F3 : 入力枝番
      }
    }
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
  public JSONObject createSqlMSTSHNTENBMN(String userId, String btnId, JSONArray dataArray, TblType tbl, SqlType sql) {
    JSONObject result = new JSONObject();


    // 更新情報
    ArrayList<String> prmData = new ArrayList<>();
    String values = "", names = "", rows = "", set = "";
    int colNum = MSTSHNTENBMNLayout.values().length; // テーブル列数
    if (TblType.JNL.getVal() == tbl.getVal()) {
      colNum += 2;
    }
    if (TblType.CSV.getVal() == tbl.getVal()) {
      colNum += CSVCMNLayout.values().length;
    }
    for (int j = 0; j < dataArray.size(); j++) {
      values = "";
      names = "";

      if (SqlType.DEL.getVal() == sql.getVal()) {
        if (j == 1) {
          values += " row( ";
        } else {
          values += ", row( ";
        }
      } else {
        if (j == 1) {
          values += "row( ";
        } else {
          values += ",row( ";
        }
      }
      for (int i = 1; i <= colNum; i++) {
        String col = "F" + i;
        String val = dataArray.optJSONObject(j).optString(col);

        if (i == MSTSHNTENBMNLayout.SENDFLG.getNo()) { // 送信フラグ
          val = DefineReport.Values.SENDFLG_UN.getVal();
        } else if (i == MSTSHNTENBMNLayout.OPERATOR.getNo()) { // オペレータ
          val = "" + userId;
        }
        if (TblType.JNL.getVal() == tbl.getVal()) {
          if (i == MSTSHNTENBMNLayout.values().length + 1) { // SEQ(ジャーナル用)
            val = jnlshn_seq;
          } else if (i == MSTSHNTENBMNLayout.values().length + 2) { // RENNO(ジャーナル用)
            val = (j + 1) + "";
          }
        }
        // CSV特殊情報設定
        if (TblType.CSV.getVal() == tbl.getVal()) {
          // SEQ以外dataArrayに追加済み前提
          if (i == MSTSHNTENBMNLayout.values().length + 1) { // SEQ(CSV用)
            val = csvshn_seq;
          }
        }
        if (i != 1) {
          values += " ,";
          names += " ,";
        }
        if (((i == 2) || (i == 3)) && StringUtils.isEmpty(val) || val.equals("0")) {
          set = "1";
          values += " null";
        } else if ((i == 8 || i == 9)) {
          values += " current_timestamp";
        } else if (StringUtils.isEmpty(val)) {
          values += " null";
        } else {
          if (isTest) {
            values += " '" + val + "'";
          } else {
            prmData.add(val);
            values += "? ";
          }
        }
        names += " " + col;
      }
      values += ") ";
      rows += "," + StringUtils.removeStart(values, ",") + "";
    }
    rows = StringUtils.removeStart(rows, ",");
    names = StringUtils.removeStart(names, ",");

    StringBuffer sbSQL;
    sbSQL = new StringBuffer();
    sbSQL.append(this.createMergeCmnCommandMSTSHNTENBMN(tbl, sql, rows, names, set));
    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

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
  public String createMergeCmnCommandMSTSHNTENBMN(TblType tbl, SqlType sql, String values, String names, String set) {

    String szTable = "INAMS.MSTSHNTENBMN";
    if (SqlType.DEL.getVal() != sql.getVal()) {
    }
    if (TblType.YYK.getVal() == tbl.getVal()) {
      szTable += "_Y";
    } else if (TblType.JNL.getVal() == tbl.getVal()) {
      szTable = "INAAD.JNLMSTSHNTENBMN";
    } else if (TblType.CSV.getVal() == tbl.getVal()) {
      szTable = "INAMS.CSVMSTSHNTENBMN";
    }

    // 基本Merge文
    StringBuffer sbSQL;
    sbSQL = new StringBuffer();

    if (set.equals("1")) {
      sbSQL.append(" WITH T1 AS (");
      sbSQL.append(" select");
      sbSQL.append(" cast(" + MSTGRPLayout.SHNCD.getId() + " as CHAR(14)) as " + MSTGRPLayout.SHNCD.getCol() + " ");
      sbSQL.append(" ,cast(F3 as SIGNED) as " + MSTGRPLayout.YOYAKUDT.getCol() + " ");
      sbSQL.append(" from(values " + values + ") as RE(" + names + ")");
      sbSQL.append(" ) ");
      sbSQL.append(" DELETE FROM " + szTable + " as T ");
      sbSQL.append(" where ");
      sbSQL.append(" T.SHNCD IN " + " (select T1." + MSTGRPLayout.SHNCD.getCol() + " from T1) ");
      if (SqlType.DEL.getVal() != sql.getVal()) {
        sbSQL.append(" AND T.TENGPCD IN " + " (select T1." + MSTGRPLayout.YOYAKUDT.getCol() + " from T1) ");
      }
    } else {
      sbSQL.append("INSERT INTO " + szTable + " ( ");
      sbSQL.append("  SHNCD"); // F1 : 商品コード
      sbSQL.append(" ,TENSHNCD"); // F2 : 店別異部門商品コード
      sbSQL.append(" ,TENGPCD"); // F3 : 店グループ
      sbSQL.append(" ,YOYAKUDT"); // F4 : マスタ変更予定日
      sbSQL.append(" ,AREAKBN"); // F5 : エリア区分
      sbSQL.append(" ,SENDFLG"); // F6 : 送信フラグ
      sbSQL.append(" ,OPERATOR"); // F7 : オペレータ
      sbSQL.append(" ,ADDDT"); // F8 : 登録日
      sbSQL.append(" ,UPDDT"); // F9 : 更新日
      sbSQL.append(" ,SRCCD"); // F10: ソースコード
      if (TblType.JNL.getVal() == tbl.getVal()) {
        sbSQL.append(" ,SEQ"); // SEQ
        sbSQL.append(" ,RENNO"); // RENNO
      }
      if (TblType.CSV.getVal() == tbl.getVal()) {
        sbSQL.append(" ,SEQ"); // F1 : SEQ
        sbSQL.append(" ,INPUTNO"); // F2 : 入力番号
        sbSQL.append(" ,INPUTEDANO"); // F3 : 入力枝番
      }
      sbSQL.append(" )VALUES " + values + "");
      sbSQL.append("ON DUPLICATE KEY UPDATE ");
      sbSQL.append("  SHNCD=VALUES(SHNCD)"); // F1 : 商品コード
      sbSQL.append(" ,TENSHNCD=VALUES(TENSHNCD)"); // F2 : 店別異部門商品コード
      sbSQL.append(" ,TENGPCD=VALUES(TENGPCD)"); // F3 : 店グループ
      sbSQL.append(" ,YOYAKUDT=VALUES(YOYAKUDT)"); // F4 : マスタ変更予定日
      sbSQL.append(" ,AREAKBN=VALUES(AREAKBN)"); // F5 : エリア区分
      sbSQL.append(" ,SENDFLG=VALUES(SENDFLG)"); // F6 : 送信フラグ
      sbSQL.append(" ,OPERATOR=VALUES(OPERATOR)"); // F7 : オペレータ
      // sbSQL.append(" ,ADDDT=VALUES(ADDDT)"); // F8 : 登録日
      sbSQL.append(" ,UPDDT=VALUES(UPDDT)"); // F9 : 更新日
      sbSQL.append(" ,SRCCD=VALUES(SRCCD)"); // F10: ソースコード
      if (TblType.JNL.getVal() == tbl.getVal()) {
        sbSQL.append(" ,SEQ=VALUES(SEQ)"); // SEQ
        sbSQL.append(" ,RENNO=VALUES(RENNO)"); // RENNO
      }
      if (TblType.CSV.getVal() == tbl.getVal()) {
        sbSQL.append(" ,SEQ=VALUES(SEQ)"); // F1 : SEQ
        sbSQL.append(" ,INPUTNO=VALUES(INPUTNO)"); // F2 : 入力番号
        sbSQL.append(" ,INPUTEDANO=VALUES(INPUTEDANO)"); // F3 : 入力枝番
      }
    }
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
    ArrayList<String> prmData = new ArrayList<>();

    String values = "", rows = "";
    for (int j = 0; j < dataArray.size(); j++) {
      String val = dataArray.optJSONObject(j).optString(MSTGRPLayout.GRPKN.getId());
      values = Integer.toString(j + 1);
      if (isTest) {
        values += ", '" + val + "'";
      } else {
        prmData.add(val);
        values += ", cast(? as char(" + MessageUtility.getDefByteLen(val) + "))";
      }
      values += "," + DefineReport.ValUpdkbn.NML.getVal() + " ";
      values += "," + DefineReport.Values.SENDFLG_UN.getVal() + " ";
      values += ",'" + userId + "' ";
      values += ",current_timestamp ";
      values += ",current_timestamp ";
      rows += ",ROW(" + values + ")";
    }
    rows = StringUtils.removeStart(rows, ",");


    // SQL実行
    StringBuffer sbSQL;
    sbSQL = new StringBuffer();
    sbSQL.append("INSERT INTO INAMS.MSTGROUP ( ");
    sbSQL.append("  GRPID"); // F2 : グループ分類ID
    sbSQL.append(" ,GRPKN"); // F2 : グループ分類名
    sbSQL.append(" ,UPDKBN"); // F3 : 更新区分
    sbSQL.append(" ,SENDFLG"); // F4 : 送信フラグ
    sbSQL.append(" ,OPERATOR"); // F5 : オペレータ
    sbSQL.append(" ,ADDDT"); // F6 : 登録日
    sbSQL.append(" ,UPDDT"); // F7 : 更新日
    sbSQL.append(" )VALUES " + rows + "");
    sbSQL.append("ON DUPLICATE KEY UPDATE ");
    sbSQL.append("  GRPID=VALUES(GRPID)"); // F2 : グループ分類ID
    sbSQL.append(" ,GRPKN=VALUES(GRPKN)"); // F2 : グループ分類名
    sbSQL.append(" ,UPDKBN=VALUES(UPDKBN)"); // F3 : 更新区分
    sbSQL.append(" ,SENDFLG=VALUES(SENDFLG)"); // F4 : 送信フラグ
    sbSQL.append(" ,OPERATOR=VALUES(OPERATOR)"); // F5 : オペレータ
    // sbSQL.append(" ,ADDDT=VALUES(ADDDT)"); // F6 : 登録日
    sbSQL.append(" ,UPDDT=VALUES(UPDDT)"); // F7 : 更新日

    iL.executeItem(sbSQL.toString(), prmData, Defines.STR_JNDI_DS);


    // 更新処理後、IDを取得する
    String sqlcommand = DefineReport.ID_SQL_MSTGROUP2.replace("@V", rows);
    @SuppressWarnings("static-access")
    JSONArray array = ItemList.selectJSONArray(sqlcommand, prmData, Defines.STR_JNDI_DS);


    // 更新結果をIDとしてセット
    for (int j = 0; j < array.size(); j++) {
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
  public JSONObject createSqlMSTGRP(String userId, String btnId, JSONArray dataArray, TblType tbl, SqlType sql) {
    JSONObject result = new JSONObject();


    // 更新情報
    ArrayList<String> prmData = new ArrayList<>();
    String values = "", names = "", rows = "", set = "";
    int colNum = 8; // テーブル列数
    if (TblType.JNL.getVal() == tbl.getVal()) {
      colNum += 2;
    }
    if (TblType.CSV.getVal() == tbl.getVal()) {
      colNum += CSVCMNLayout.values().length;
    }
    for (int j = 0; j < dataArray.size(); j++) {
      values = "";
      names = "";
      for (int i = 1; i <= colNum; i++) {
        String col = "F" + i;
        String val = dataArray.optJSONObject(j).optString(col);

        if (i == MSTGRPLayout.SENDFLG.getNo()) { // 送信フラグ
          val = DefineReport.Values.SENDFLG_UN.getVal();
        } else if (i == MSTGRPLayout.OPERATOR.getNo()) { // オペレータ
          val = userId;
        }
        if (TblType.JNL.getVal() == tbl.getVal()) {
          if (i == MSTGRPLayout.values().length + 1) { // SEQ(ジャーナル用)
            val = jnlshn_seq;
          } else if (i == MSTGRPLayout.values().length + 2) { // RENNO(ジャーナル用)
            val = (j + 1) + "";
          }
        }
        // CSV特殊情報設定
        if (TblType.CSV.getVal() == tbl.getVal()) {
          // SEQ以外dataArrayに追加済み前提
          if (i == MSTGRPLayout.values().length + 1) { // SEQ(CSV用)
            val = csvshn_seq;
          }
        }
        if (i != 1) {
          values += " ,";
          names += " ,";
        }
        if ((i == 2) && StringUtils.isEmpty(val)) {
          set = "1";
          values += " null";
        } else if ((i == 7 || i == 8)) {
          values += " current_timestamp";
        } else if (StringUtils.isEmpty(val)) {
          values += " null";
        } else {
          if (isTest) {
            values += " '" + val + "'";
          } else {
            prmData.add(val);
            values += "? ";
          }
        }
        names += " " + col;
      }
      rows += "," + StringUtils.removeStart(values, ",") + "";
    }
    rows = StringUtils.removeStart(rows, ",");
    names = StringUtils.removeStart(names, ",");

    StringBuffer sbSQL;
    sbSQL = new StringBuffer();
    sbSQL.append(this.createMergeCmnCommandMSTGRP(tbl, sql, rows, names, set));
    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

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
  public String createMergeCmnCommandMSTGRP(TblType tbl, SqlType sql, String values, String names, String set) {

    if (SqlType.DEL.getVal() != sql.getVal()) {
    }
    if (TblType.YYK.getVal() == tbl.getVal()) {
    } else if (TblType.JNL.getVal() == tbl.getVal()) {
    } else if (TblType.CSV.getVal() == tbl.getVal()) {
    }

    // 基本Merge文
    StringBuffer sbSQL;
    sbSQL = new StringBuffer();
    if (SqlType.DEL.getVal() == sql.getVal() && !set.equals("1")) {
      sbSQL.append("INSERT INTO INAMS.MSTGRP ( ");
      sbSQL.append("  SHNCD"); // F1 : 商品コード
      sbSQL.append(" ,GRPID"); // F2 : グループ分類ID
      sbSQL.append(" ,YOYAKUDT"); // F3 : マスタ変更予定日
      sbSQL.append(" ,SEQNO"); // F4 : 入力順番
      sbSQL.append(" ,SENDFLG"); // F5 : 送信フラグ
      sbSQL.append(" ,OPERATOR"); // F6 : オペレータ
      sbSQL.append(" ,ADDDT"); // F7 : 登録日
      sbSQL.append(" ,UPDDT"); // F8 : 更新日
      if (TblType.JNL.getVal() == tbl.getVal()) {
        sbSQL.append(" ,SEQ"); // SEQ
        sbSQL.append(" ,RENNO"); // RENNO
      }
      if (TblType.CSV.getVal() == tbl.getVal()) {
        sbSQL.append(" ,SEQ"); // F1 : SEQ
        sbSQL.append(" ,INPUTNO"); // F2 : 入力番号
        sbSQL.append(" ,INPUTEDANO"); // F3 : 入力枝番
      }
      sbSQL.append(" )VALUES (" + values + ")");
      sbSQL.append("ON DUPLICATE KEY UPDATE ");
      sbSQL.append("  SHNCD=VALUES(SHNCD)"); // F1 : 商品コード
      sbSQL.append(" ,GRPID=VALUES(GRPID)"); // F2 : グループ分類ID
      sbSQL.append(" ,YOYAKUDT=VALUES(YOYAKUDT)"); // F3 : マスタ変更予定日
      sbSQL.append(" ,SEQNO=VALUES(SEQNO)"); // F4 : 入力順番
      sbSQL.append(" ,SENDFLG=VALUES(SENDFLG)"); // F5 : 送信フラグ
      sbSQL.append(" ,OPERATOR=VALUES(OPERATOR)"); // F6 : オペレータ
      // sbSQL.append(" ,ADDDT=VALUES(ADDDT)"); // F7 : 登録日
      sbSQL.append(" ,UPDDT=VALUES(UPDDT)"); // F8 : 更新日
      if (TblType.JNL.getVal() == tbl.getVal()) {
        sbSQL.append(" ,SEQ=VALUES(SEQ)"); // SEQ
        sbSQL.append(" ,RENNO=VALUES(RENNO)"); // RENNO
      }
      if (TblType.CSV.getVal() == tbl.getVal()) {
        sbSQL.append(" ,SEQ=VALUES(SEQ)"); // F1 : SEQ
        sbSQL.append(" ,INPUTNO=VALUES(INPUTNO)"); // F2 : 入力番号
        sbSQL.append(" ,INPUTEDANO=VALUES(INPUTEDANO)"); // F3 : 入力枝番
      }
    } else {
      sbSQL.append(" WITH T1 AS (");
      sbSQL.append(" select");
      sbSQL.append(" cast(" + MSTGRPLayout.SHNCD.getId() + " as CHAR(14)) as " + MSTGRPLayout.SHNCD.getCol() + " ");
      sbSQL.append(" ,cast(F3 as SIGNED) as " + MSTGRPLayout.YOYAKUDT.getCol() + " ");
      sbSQL.append(" from(VALUES ROW(" + values + ")) as RE(" + names + ")");
      sbSQL.append(" ) ");
      sbSQL.append(" DELETE FROM INAMS.MSTGRP as T ");
      sbSQL.append(" where ");
      sbSQL.append(" T.SHNCD IN " + " (select T1." + MSTGRPLayout.SHNCD.getCol() + " from T1) ");

    }
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
  public JSONObject createSqlMSTAHS(String userId, String btnId, JSONArray dataArray, TblType tbl, SqlType sql) {
    JSONObject result = new JSONObject();


    // 更新情報
    ArrayList<String> prmData = new ArrayList<>();
    String values = "", names = "", rows = "";
    int colNum = MSTAHSLayout.values().length; // テーブル列数
    if (TblType.JNL.getVal() == tbl.getVal()) {
      colNum += 2;
    }
    if (TblType.CSV.getVal() == tbl.getVal()) {
      colNum += CSVCMNLayout.values().length;
    }
    for (int j = 0; j < dataArray.size(); j++) {
      values = "";
      names = "";
      for (int i = 1; i <= colNum; i++) {
        String col = "F" + i;
        String val = dataArray.optJSONObject(j).optString(col);

        if (i == MSTAHSLayout.SENDFLG.getNo()) { // 送信フラグ
          val = "0";
        } else if (i == MSTAHSLayout.OPERATOR.getNo()) { // オペレータ
          val = userId;
        }
        if (TblType.JNL.getVal() == tbl.getVal()) {
          if (i == MSTAHSLayout.values().length + 1) { // SEQ(ジャーナル用)
            val = jnlshn_seq;
          } else if (i == MSTAHSLayout.values().length + 2) { // RENNO(ジャーナル用)
            val = (j + 1) + "";
          }
        }
        // CSV特殊情報設定
        if (TblType.CSV.getVal() == tbl.getVal()) {
          // SEQ以外dataArrayに追加済み前提
          if (i == MSTAHSLayout.values().length + 1) { // SEQ(CSV用)
            val = csvshn_seq;
          }
        }
        if (i != 1) {
          values += " ,";
          names += " ,";
        }
        if ((i == 7 || i == 8)) {
          values += " current_timestamp";
        } else if (StringUtils.isEmpty(val)) {
          values += " null";
        } else {
          if (isTest) {
            values += " '" + val + "'";
          } else {
            prmData.add(val);
            values += "? ";
          }
        }
        names += " " + col;
      }
      rows += "," + StringUtils.removeStart(values, ",") + "";
    }
    rows = StringUtils.removeStart(rows, ",");
    names = StringUtils.removeStart(names, ",");

    StringBuffer sbSQL;
    sbSQL = new StringBuffer();
    sbSQL.append(this.createMergeCmnCommandMSTAHS(tbl, sql, rows, names));
    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

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
    if (SqlType.DEL.getVal() != sql.getVal()) {
    }
    if (TblType.YYK.getVal() == tbl.getVal()) {
      szTable += "_Y";
    } else if (TblType.JNL.getVal() == tbl.getVal()) {
      szTable = "INAAD.JNLAHS";
    } else if (TblType.CSV.getVal() == tbl.getVal()) {
      szTable = "INAMS.CSVAHS";
    }

    // 基本Merge文
    StringBuffer sbSQL;
    sbSQL = new StringBuffer();
    if (SqlType.DEL.getVal() == sql.getVal()) {
      sbSQL.append(" WITH T1 AS (");
      sbSQL.append(" select");
      sbSQL.append(" cast(" + MSTAHSLayout.SHNCD.getId() + " as CHAR(14)) as " + MSTAHSLayout.SHNCD.getCol() + " ");
      sbSQL.append(" ,cast(" + MSTAHSLayout.YOYAKUDT.getId() + " as SIGNED) as " + MSTAHSLayout.YOYAKUDT.getCol() + " ");
      sbSQL.append(" from(VALUES ROW(" + values + ")) as RE(" + names + ")");
      sbSQL.append(" ) ");
      sbSQL.append(" DELETE FROM " + szTable + " as T ");
      sbSQL.append(" where ");
      sbSQL.append(" T.SHNCD IN " + " (select T1." + MSTAHSLayout.SHNCD.getCol() + " from T1) ");
      if (SqlType.DEL.getVal() != sql.getVal()) {
        sbSQL.append(" AND T.TENGPCD IN " + " (select T1." + MSTAHSLayout.YOYAKUDT.getCol() + " from T1) ");
      }
    } else {
      sbSQL.append("INSERT INTO " + szTable + " ( ");
      sbSQL.append("  SHNCD"); // F1 : 商品コード
      sbSQL.append(" ,TENCD"); // F2 : 店コード
      sbSQL.append(" ,YOYAKUDT"); // F3 : マスタ変更予定日
      sbSQL.append(" ,AHSKB"); // F4 : 自動発注区分
      sbSQL.append(" ,SENDFLG"); // F5 : 送信フラグ
      sbSQL.append(" ,OPERATOR"); // F6 : オペレータ
      sbSQL.append(" ,ADDDT"); // F7 : 登録日
      sbSQL.append(" ,UPDDT"); // F8 : 更新日
      if (TblType.JNL.getVal() == tbl.getVal()) {
        sbSQL.append(" ,SEQ"); // F1 : SEQ
        sbSQL.append(" ,RENNO"); // F2 : RENNO
      }
      if (TblType.CSV.getVal() == tbl.getVal()) {
        sbSQL.append(" ,SEQ"); // F1 : SEQ
        sbSQL.append(" ,INPUTNO"); // F2 : 入力番号
        sbSQL.append(" ,INPUTEDANO"); // F3 : 入力枝番
      }
      sbSQL.append(" )VALUES (" + values + ")");
      sbSQL.append("ON DUPLICATE KEY UPDATE ");
      sbSQL.append("  SHNCD=VALUES(SHNCD)"); // F1 : 商品コード
      sbSQL.append(" ,TENCD=VALUES(TENCD)"); // F2 : 店コード
      sbSQL.append(" ,YOYAKUDT=VALUES(YOYAKUDT)"); // F3 : マスタ変更予定日
      sbSQL.append(" ,AHSKB=VALUES(AHSKB)"); // F4 : 自動発注区分
      sbSQL.append(" ,SENDFLG=VALUES(SENDFLG)"); // F5 : 送信フラグ
      sbSQL.append(" ,OPERATOR=VALUES(OPERATOR)"); // F6 : オペレータ
      // sbSQL.append(" ,ADDDT=VALUES(ADDDT)"); // F7 : 登録日
      sbSQL.append(" ,UPDDT=VALUES(UPDDT)"); // F8 : 更新日
      if (TblType.JNL.getVal() == tbl.getVal()) {
        sbSQL.append(" ,SEQ=VALUES(SEQ)"); // F1 : SEQ
        sbSQL.append(" ,RENNO=VALUES(RENNO)"); // F2 : RENNO
      }
      if (TblType.CSV.getVal() == tbl.getVal()) {
        sbSQL.append(" ,SEQ=VALUES(SEQ)"); // F1 : SEQ
        sbSQL.append(" ,INPUTNO=VALUES(INPUTNO)"); // F2 : 入力番号
        sbSQL.append(" ,INPUTEDANO=VALUES(INPUTEDANO)"); // F3 : 入力枝番
      }
    }

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
  public JSONObject createSqlMSTMAKER(String userId, String btnId, JSONObject data, TblType tbl, SqlType sql) {
    JSONObject result = new JSONObject();



    // 更新情報
    ArrayList<String> prmData = new ArrayList<>();
    String values = "", names = "";
    int colNum = 10; // テーブル列数
    for (int i = 1; i <= colNum; i++) {
      String col = "F" + i;
      String val = data.optString(col);;
      if (i == 6) { // 更新区分
        val = DefineReport.ValUpdkbn.NML.getVal();
      } else if (i == 7) { // 送信フラグ
        val = DefineReport.Values.SENDFLG_UN.getVal();
      } else if (i == 8) { // オペレータ
        val = userId;
      }
      if ((i == 9 || i == 10)) {
        values += ", current_timestamp";
      } else if (StringUtils.isEmpty(val)) {
        values += ", null";
      } else {
        if (isTest) {
          values += ", '" + val + "'";
        } else {
          prmData.add(val);
          values += ",? ";
        }
      }
      names += ", " + col;
    }
    values = "(" + StringUtils.removeStart(values, ",") + ")";
    names = StringUtils.removeStart(names, ",");

    StringBuffer sbSQL;
    sbSQL = new StringBuffer();
    sbSQL.append(this.createMergeCmnCommandMSTMAKER(tbl, sql, values, names));
    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

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

    String szTable = "INAMS.MSTMAKER";
    if (TblType.YYK.getVal() == tbl.getVal()) {
      szTable += "_Y";
    }

    // 基本Merge文
    StringBuffer sbSQL;
    sbSQL = new StringBuffer();
    sbSQL.append("REPLACE INTO " + szTable + " ( ");
    sbSQL.append("  MAKERCD"); // F1 : メーカーコード
    sbSQL.append(" ,MAKERAN"); // F2 : メーカー名（カナ）
    sbSQL.append(" ,MAKERKN"); // F3 : メーカー名（漢字）
    sbSQL.append(" ,JANCD"); // F4 : JANコード
    sbSQL.append(" ,DMAKERCD"); // F5 : 代表メーカーコード
    sbSQL.append(" ,UPDKBN"); // F6 : 更新区分
    sbSQL.append(" ,SENDFLG"); // F7 : 送信フラグ
    sbSQL.append(" ,OPERATOR"); // F8 : オペレータ
    sbSQL.append(" ,ADDDT"); // F9 : 登録日
    sbSQL.append(" ,UPDDT"); // F10: 更新日
    sbSQL.append(" )VALUES " + values + "");
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
  public JSONObject createSqlSYSSHNCD_AKI(String userId, String btnId, JSONObject data, TblType typ, SqlType sql) {
    JSONObject inf = this.createSqlSYSSHNCD_AKI(userId, data, sql);

    JSONArray prm = inf.optJSONArray("PRM");
    ArrayList<String> prmData = new ArrayList<>();
    for (int i = 0; i < prm.size(); i++) {
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
  public JSONObject createSqlSYSSHNCD_AKI(String userId, JSONObject data, SqlType sql) {
    JSONObject result = new JSONObject();

    // 更新情報
    ArrayList<String> prmData = new ArrayList<>();
    String values = "", names = "";
    int colNum = 3; // テーブル列数
    for (int i = 1; i <= colNum; i++) {
      String col = "F" + i;
      String val = "";
      if (i == 1) {
        val = data.optString("F1"); // 商品コード
      } else if (i == 2) {
        val = data.optString("F2", "1"); // 使用済フラグ
      }

      if (i == 3) {
        values += ", current_timestamp";
      } else if (StringUtils.isEmpty(val)) {
        values += ", null";
      } else {
        prmData.add(val);
        values += ", ? ";
      }

      names += ", " + col;
    }
    values = "" + StringUtils.removeStart(values, ",") + "";
    names = StringUtils.removeStart(names, ",");


    String szTable = "INAAD.SYSSHNCD_AKI";
    StringBuffer sbSQL;
    sbSQL = new StringBuffer();
    sbSQL.append("REPLACE INTO " + szTable + " ( ");

    sbSQL.append("  SHNCD"); // F1 : 商品コード
    sbSQL.append(" ,USEFLG"); // F2 : 使用済フラグ
    sbSQL.append(" ,UPDDT"); // F3 : 更新日

    sbSQL.append(" )VALUES (" + values + ")");
    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    result.put("SQL", sbSQL.toString());
    result.put("PRM", prmData);
    result.put("LBL", "商品コード空き番管理テーブル");
    // sqlList.add(sbSQL.toString());
    // prmList.add(prmData);
    // lblList.add("商品コード空き番管理テーブル");
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
  public JSONObject createSqlSYSURICD_FU(String userId, String btnId, JSONObject data, TblType tbl, SqlType sql) {
    JSONObject result = new JSONObject();

    // 更新情報
    ArrayList<String> prmData = new ArrayList<>();

    String szTable = "INAAD.SYSURICD_FU";
    StringBuffer sbSQL;
    sbSQL = new StringBuffer();
    sbSQL.append("REPLACE INTO " + szTable + " (");
    sbSQL.append("  SUMINO,UPDDT ");
    sbSQL.append(" ) VALUE ( ");
    sbSQL.append("  case when SUMINO+1 <=ENDNO then SUMINO+1 else SUMINO end"); // F4 : 付番済番号
    sbSQL.append(" , current_timestamp"); // F5 : 更新日
    sbSQL.append(") ");


    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

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
  public JSONObject createSqlSYSURICD_AKI(String userId, String btnId, JSONObject data, TblType tbl, SqlType sql) {
    JSONObject inf = this.createSqlSYSURICD_AKI(userId, data, sql);

    JSONArray prm = inf.optJSONArray("PRM");
    ArrayList<String> prmData = new ArrayList<>();
    for (int i = 0; i < prm.size(); i++) {
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
  public JSONObject createSqlSYSURICD_AKI(String userId, JSONObject data, SqlType sql) {
    JSONObject result = new JSONObject();

    // 更新情報
    ArrayList<String> prmData = new ArrayList<>();
    String values = "", names = "";
    int colNum = 2; // テーブル列数
    for (int i = 1; i <= colNum; i++) {
      String col = "F" + i;
      String val = "";
      if (i == 1) {
        val = data.optString("F1"); // F1 : 販売コード
      } else if (i == 2) {
        val = data.optString("F2"); // F2 : 使用済フラグ
      }
      if (i == 3) {
        values += ", current_timestamp";
      } else if (StringUtils.isEmpty(val)) {
        values += ", null";
      } else {
        prmData.add(val);
        values += ", ? ";
      }
      names += ", " + col;
    }
    values = "" + StringUtils.removeStart(values, ",") + "";
    names = StringUtils.removeStart(names, ",");


    String szTable = "INAAD.SYSURICD_AKI";
    StringBuffer sbSQL;
    sbSQL = new StringBuffer();
    sbSQL.append("REPLACE INTO " + szTable + " (");

    sbSQL.append("   URICD"); // F1 : 販売コード
    sbSQL.append(" , USEFLG"); // F2 : 使用済フラグ

    sbSQL.append(" )VALUES (" + values + ")");


    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    result.put("SQL", sbSQL.toString());
    result.put("PRM", prmData);
    result.put("LBL", "販売コード空き番管理テーブル");

    // sqlList.add(sbSQL.toString());
    // prmList.add(prmData);
    // lblList.add("販売コード空き番管理テーブル");
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
    ArrayList<String> prmData = new ArrayList<>();

    String sysdate = this.getSHORIDT();

    String szTable = "INAAD.SYSSHNCOUNT";
    StringBuffer sbSQL;
    sbSQL = new StringBuffer();
    sbSQL.append("INSERT INTO " + szTable + " (");
    sbSQL.append(" UPDATEDT ");
    sbSQL.append(",UPDATECNT ");
    sbSQL.append(",UPDDT ");
    sbSQL.append(") VALUES ( ");

    sbSQL.append(" " + sysdate + " ");
    sbSQL.append(", 1 ");
    sbSQL.append(",CURRENT_TIMESTAMP ");
    sbSQL.append(") ");
    sbSQL.append("ON DUPLICATE KEY UPDATE ");
    sbSQL.append("UPDATEDT = VALUES(UPDATEDT) ");
    sbSQL.append(",UPDATECNT = UPDATECNT + 1 ");
    sbSQL.append(",UPDDT= VALUES(UPDDT) ");


    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("商品更新件数マスタ");
    return result;
  }

  // 処理日付取得
  public String getSHORIDT() {

    new ItemList();
    StringBuffer sbSQL = new StringBuffer();
    ArrayList<String> paramData = new ArrayList<>();

    String value = "";

    sbSQL.append(DefineReport.ID_SQLSHORIDT);
    @SuppressWarnings("static-access")
    JSONArray array = ItemList.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);
    if (array.size() > 0) {
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

  /** 商品マスタレイアウト() */
  public enum MSTSHNLayout implements MSTLayout {
    /** 商品コード */
    SHNCD(1, "SHNCD", "CHARACTER(14)", "商品コード"),
    /** マスタ変更予定日 */
    YOYAKUDT(2, "YOYAKUDT", "INTEGER", "マスタ変更予定日"),
    /** 店売価実施日 */
    TENBAIKADT(3, "TENBAIKADT", "INTEGER", "店売価実施日"),
    /** 用途分類コード_部門 */
    YOT_BMNCD(4, "YOT_BMNCD", "SMALLINT", "用途分類コード_部門"),
    /** 用途分類コード_大 */
    YOT_DAICD(5, "YOT_DAICD", "SMALLINT", "用途分類コード_大"),
    /** 用途分類コード_中 */
    YOT_CHUCD(6, "YOT_CHUCD", "SMALLINT", "用途分類コード_中"),
    /** 用途分類コード_小 */
    YOT_SHOCD(7, "YOT_SHOCD", "SMALLINT", "用途分類コード_小"),
    /** 売場分類コード_部門 */
    URI_BMNCD(8, "URI_BMNCD", "SMALLINT", "売場分類コード_部門"),
    /** 売場分類コード_大 */
    URI_DAICD(9, "URI_DAICD", "SMALLINT", "売場分類コード_大"),
    /** 売場分類コード_中 */
    URI_CHUCD(10, "URI_CHUCD", "SMALLINT", "売場分類コード_中"),
    /** 売場分類コード_小 */
    URI_SHOCD(11, "URI_SHOCD", "SMALLINT", "売場分類コード_小"),
    /** 標準分類コード_部門 */
    BMNCD(12, "BMNCD", "SMALLINT", "標準分類コード_部門"),
    /** 標準分類コード_大 */
    DAICD(13, "DAICD", "SMALLINT", "標準分類コード_大"),
    /** 標準分類コード_中 */
    CHUCD(14, "CHUCD", "SMALLINT", "標準分類コード_中"),
    /** 標準分類コード_小 */
    SHOCD(15, "SHOCD", "SMALLINT", "標準分類コード_小"),
    /** 標準分類コード_小小 */
    SSHOCD(16, "SSHOCD", "SMALLINT", "標準分類コード_小小"),
    /** 取扱期間_開始日 */
    ATSUK_STDT(17, "ATSUK_STDT", "INTEGER", "取扱期間_開始日"),
    /** 取扱期間_終了日 */
    ATSUK_EDDT(18, "ATSUK_EDDT", "INTEGER", "取扱期間_終了日"),
    /** 取扱停止 */
    TEISHIKBN(19, "TEISHIKBN", "SMALLINT", "取扱停止"),
    /** 商品名（カナ） */
    SHNAN(20, "SHNAN", "VARCHAR(20)", "商品名（カナ）"),
    /** 商品名（漢字） */
    SHNKN(21, "SHNKN", "VARCHAR(40)", "商品名（漢字）"),
    /** プライスカード商品名称（漢字） */
    PCARDKN(22, "PCARDKN", "VARCHAR(40)", "プライスカード商品名称（漢字）"),
    /** POP名称 */
    POPKN(23, "POPKN", "VARCHAR(40)", "POP名称"),
    /** レシート名（カナ） */
    RECEIPTAN(24, "RECEIPTAN", "VARCHAR(20)", "レシート名（カナ）"),
    /** レシート名（漢字） */
    RECEIPTKN(25, "RECEIPTKN", "VARCHAR(40)", "レシート名（漢字）"),
    /** PC区分 */
    PCKBN(26, "PCKBN", "SMALLINT", "PC区分"),
    /** 加工区分 */
    KAKOKBN(27, "KAKOKBN", "SMALLINT", "加工区分"),
    /** 市場区分 */
    ICHIBAKBN(28, "ICHIBAKBN", "SMALLINT", "市場区分"),
    /** 商品種類 */
    SHNKBN(29, "SHNKBN", "SMALLINT", "商品種類"),
    /** 産地 */
    SANCHIKN(30, "SANCHIKN", "VARCHAR(40)", "産地"),
    /** 標準仕入先コード */
    SSIRCD(31, "SSIRCD", "INTEGER", "標準仕入先コード"),
    /** 配送パターン */
    HSPTN(32, "HSPTN", "SMALLINT", "配送パターン"),
    /** レギュラー情報_取扱フラグ */
    RG_ATSUKFLG(33, "RG_ATSUKFLG", "SMALLINT", "レギュラー情報_取扱フラグ"),
    /** レギュラー情報_原価 */
    RG_GENKAAM(34, "RG_GENKAAM", "DECIMAL(8,2)", "レギュラー情報_原価"),
    /** レギュラー情報_売価 */
    RG_BAIKAAM(35, "RG_BAIKAAM", "INTEGER", "レギュラー情報_売価"),
    /** レギュラー情報_店入数 */
    RG_IRISU(36, "RG_IRISU", "SMALLINT", "レギュラー情報_店入数"),
    /** レギュラー情報_一括伝票フラグ */
    RG_IDENFLG(37, "RG_IDENFLG", "CHARACTER(1)", "レギュラー情報_一括伝票フラグ"),
    /** レギュラー情報_ワッペン */
    RG_WAPNFLG(38, "RG_WAPNFLG", "CHARACTER(1)", "レギュラー情報_ワッペン"),
    /** 販促情報_取扱フラグ */
    HS_ATSUKFLG(39, "HS_ATSUKFLG", "SMALLINT", "販促情報_取扱フラグ"),
    /** 販促情報_原価 */
    HS_GENKAAM(40, "HS_GENKAAM", "DECIMAL(8,2)", "販促情報_原価"),
    /** 販促情報_売価 */
    HS_BAIKAAM(41, "HS_BAIKAAM", "INTEGER", "販促情報_売価"),
    /** 販促情報_店入数 */
    HS_IRISU(42, "HS_IRISU", "SMALLINT", "販促情報_店入数"),
    /** 販促情報_ワッペン */
    HS_WAPNFLG(43, "HS_WAPNFLG", "CHARACTER(1)", "販促情報_ワッペン"),
    /** 販促情報_スポット最低発注数 */
    HS_SPOTMINSU(44, "HS_SPOTMINSU", "SMALLINT", "販促情報_スポット最低発注数"),
    /** 販促情報_特売ワッペン */
    HP_SWAPNFLG(45, "HP_SWAPNFLG", "CHARACTER(1)", "販促情報_特売ワッペン"),
    /** 規格名称 */
    KIKKN(46, "KIKKN", "VARCHAR(46)", "規格名称"),
    /** ユニットプライス_容量 */
    UP_YORYOSU(47, "UP_YORYOSU", "INTEGER", "ユニットプライス_容量"),
    /** ユニットプライス_単位容量 */
    UP_TYORYOSU(48, "UP_TYORYOSU", "SMALLINT", "ユニットプライス_単位容量"),
    /** ユニットプライス_ユニット単位 */
    UP_TANIKBN(49, "UP_TANIKBN", "SMALLINT", "ユニットプライス_ユニット単位"),
    /** 商品サイズ_横 */
    SHNYOKOSZ(50, "SHNYOKOSZ", "SMALLINT", "商品サイズ_横"),
    /** 商品サイズ_縦 */
    SHNTATESZ(51, "SHNTATESZ", "SMALLINT", "商品サイズ_縦"),
    /** 商品サイズ_奥行 */
    SHNOKUSZ(52, "SHNOKUSZ", "SMALLINT", "商品サイズ_奥行"),
    /** 商品サイズ_重量 */
    SHNJRYOSZ(53, "SHNJRYOSZ", "DECIMAL(6,1)", "商品サイズ_重量"),
    /** PB区分 */
    PBKBN(54, "PBKBN", "SMALLINT", "PB区分"),
    /** 小物区分 */
    KOMONOKBM(55, "KOMONOKBM", "SMALLINT", "小物区分"),
    /** 棚卸区分 */
    TANAOROKBN(56, "TANAOROKBN", "SMALLINT", "棚卸区分"),
    /** 定計区分 */
    TEIKEIKBN(57, "TEIKEIKBN", "SMALLINT", "定計区分"),
    /** ODS_賞味期限_春 */
    ODS_HARUSU(58, "ODS_HARUSU", "SMALLINT", "ODS_賞味期限_春"),
    /** ODS_賞味期限_夏 */
    ODS_NATSUSU(59, "ODS_NATSUSU", "SMALLINT", "ODS_賞味期限_夏"),
    /** ODS_賞味期限_秋 */
    ODS_AKISU(60, "ODS_AKISU", "SMALLINT", "ODS_賞味期限_秋"),
    /** ODS_賞味期限_冬 */
    ODS_FUYUSU(61, "ODS_FUYUSU", "SMALLINT", "ODS_賞味期限_冬"),
    /** ODS_入荷期限 */
    ODS_NYUKASU(62, "ODS_NYUKASU", "SMALLINT", "ODS_入荷期限"),
    /** ODS_値引期限 */
    ODS_NEBIKISU(63, "ODS_NEBIKISU", "SMALLINT", "ODS_値引期限"),
    /** プライスカード_種類 */
    PCARD_SHUKBN(64, "PCARD_SHUKBN", "SMALLINT", "プライスカード_種類"),
    /** プライスカード_色 */
    PCARD_IROKBN(65, "PCARD_IROKBN", "SMALLINT", "プライスカード_色"),
    /** 税区分 */
    ZEIKBN(66, "ZEIKBN", "SMALLINT", "税区分"),
    /** 税率区分 */
    ZEIRTKBN(67, "ZEIRTKBN", "SMALLINT", "税率区分"),
    /** 旧税率区分 */
    ZEIRTKBN_OLD(68, "ZEIRTKBN_OLD", "SMALLINT", "旧税率区分"),
    /** 税率変更日 */
    ZEIRTHENKODT(69, "ZEIRTHENKODT", "INTEGER", "税率変更日"),
    /** 製造限度日数 */
    SEIZOGENNISU(70, "SEIZOGENNISU", "SMALLINT", "製造限度日数"),
    /** 定貫不定貫区分 */
    TEIKANKBN(71, "TEIKANKBN", "SMALLINT", "定貫不定貫区分"),
    /** メーカーコード */
    MAKERCD(72, "MAKERCD", "INTEGER", "メーカーコード"),
    /** 輸入区分 */
    IMPORTKBN(73, "IMPORTKBN", "SMALLINT", "輸入区分"),
    /** 仕分区分 */
    SIWAKEKBN(74, "SIWAKEKBN", "SMALLINT", "仕分区分"),
    /** 返品区分 */
    HENPIN_KBN(75, "HENPIN_KBN", "SMALLINT", "返品区分"),
    /** 対象年齢 */
    TAISHONENSU(76, "TAISHONENSU", "SMALLINT", "対象年齢"),
    /** カロリー表示 */
    CALORIESU(77, "CALORIESU", "SMALLINT", "カロリー表示"),
    /** フラグ情報_ELP */
    ELPFLG(78, "ELPFLG", "SMALLINT", "フラグ情報_ELP"),
    /** フラグ情報_ベルマーク */
    BELLMARKFLG(79, "BELLMARKFLG", "SMALLINT", "フラグ情報_ベルマーク"),
    /** フラグ情報_リサイクル */
    RECYCLEFLG(80, "RECYCLEFLG", "SMALLINT", "フラグ情報_リサイクル"),
    /** フラグ情報_エコマーク */
    ECOFLG(81, "ECOFLG", "SMALLINT", "フラグ情報_エコマーク"),
    /** 包材用途 */
    HZI_YOTO(82, "HZI_YOTO", "SMALLINT", "包材用途"),
    /** 包材材質 */
    HZI_ZAISHITU(83, "HZI_ZAISHITU", "SMALLINT", "包材材質"),
    /** 包材リサイクル対象 */
    HZI_RECYCLE(84, "HZI_RECYCLE", "SMALLINT", "包材リサイクル対象"),
    /** 期間 */
    KIKANKBN(85, "KIKANKBN", "CHARACTER(1)", "期間"),
    /** 酒級 */
    SHUKYUKBN(86, "SHUKYUKBN", "SMALLINT", "酒級"),
    /** 度数 */
    DOSU(87, "DOSU", "SMALLINT", "度数"),
    /** 陳列形式コード */
    CHINRETUCD(88, "CHINRETUCD", "CHARACTER(1)", "陳列形式コード"),
    /** 段積み形式コード */
    DANTUMICD(89, "DANTUMICD", "CHARACTER(2)", "段積み形式コード"),
    /** 重なりコード */
    KASANARICD(90, "KASANARICD", "CHARACTER(1)", "重なりコード"),
    /** 重なりサイズ */
    KASANARISZ(91, "KASANARISZ", "SMALLINT", "重なりサイズ"),
    /** 圧縮率 */
    ASSHUKURT(92, "ASSHUKURT", "SMALLINT", "圧縮率"),
    /** 種別コード */
    SHUBETUCD(93, "SHUBETUCD", "CHARACTER(2)", "種別コード"),
    /** 販売コード */
    URICD(94, "URICD", "INTEGER", "販売コード"),
    /** 商品コピー・セールスコメント */
    SALESCOMKN(95, "SALESCOMKN", "VARCHAR(60)", "商品コピー・セールスコメント"),
    /** 裏貼 */
    URABARIKBN(96, "URABARIKBN", "SMALLINT", "裏貼"),
    /** プライスカード出力有無 */
    PCARD_OPFLG(97, "PCARD_OPFLG", "SMALLINT", "プライスカード出力有無"),
    /** 親商品コード */
    PARENTCD(98, "PARENTCD", "CHARACTER(14)", "親商品コード"),
    /** 便区分 */
    BINKBN(99, "BINKBN", "SMALLINT", "便区分"),
    /** 発注曜日_月 */
    HAT_MONKBN(100, "HAT_MONKBN", "SMALLINT", "発注曜日_月"),
    /** 発注曜日_火 */
    HAT_TUEKBN(101, "HAT_TUEKBN", "SMALLINT", "発注曜日_火"),
    /** 発注曜日_水 */
    HAT_WEDKBN(102, "HAT_WEDKBN", "SMALLINT", "発注曜日_水"),
    /** 発注曜日_木 */
    HAT_THUKBN(103, "HAT_THUKBN", "SMALLINT", "発注曜日_木"),
    /** 発注曜日_金 */
    HAT_FRIKBN(104, "HAT_FRIKBN", "SMALLINT", "発注曜日_金"),
    /** 発注曜日_土 */
    HAT_SATKBN(105, "HAT_SATKBN", "SMALLINT", "発注曜日_土"),
    /** 発注曜日_日 */
    HAT_SUNKBN(106, "HAT_SUNKBN", "SMALLINT", "発注曜日_日"),
    /** リードタイムパターン */
    READTMPTN(107, "READTMPTN", "SMALLINT", "リードタイムパターン"),
    /** 締め回数 */
    SIMEKAISU(108, "SIMEKAISU", "SMALLINT", "締め回数"),
    /** 衣料使い回しフラグ */
    IRYOREFLG(109, "IRYOREFLG", "SMALLINT", "衣料使い回しフラグ"),
    /** 登録元 */
    TOROKUMOTO(110, "TOROKUMOTO", "SMALLINT", "登録元"),
    /** 更新区分 */
    UPDKBN(111, "UPDKBN", "SMALLINT", "更新区分"),
    /** 送信フラグ */
    SENDFLG(112, "SENDFLG", "SMALLINT", "送信フラグ"),
    /** オペレータ */
    OPERATOR(113, "OPERATOR", "VARCHAR(20)", "オペレータ"),
    /** 登録日 */
    ADDDT(114, "ADDDT", "TIMESTAMP", "登録日"),
    /** 更新日 */
    UPDDT(115, "UPDDT", "TIMESTAMP", "更新日"),
    /** 保温区分 */
    K_HONKB(116, "K_HONKB", "SMALLINT", "保温区分"),
    /** デリカワッペン区分_レギュラー */
    K_WAPNFLG_R(117, "K_WAPNFLG_R", "SMALLINT", "デリカワッペン区分_レギュラー"),
    /** デリカワッペン区分_販促 */
    K_WAPNFLG_H(118, "K_WAPNFLG_H", "SMALLINT", "デリカワッペン区分_販促"),
    /** 取扱区分 */
    K_TORIKB(119, "K_TORIKB", "SMALLINT", "取扱区分"),
    /** ITFコード */
    ITFCD(120, "ITFCD", "CHARACTER(14)", "ITFコード"),
    /** センター入数 */
    CENTER_IRISU(121, "CENTER_IRISU", "SMALLINT", "センター入数");

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
    @Override
    public Integer getNo() {
      return no;
    }

    /** @return tbl 列名 */
    @Override
    public String getCol() {
      return col;
    }

    /** @return typ 列型 */
    @Override
    public String getTyp() {
      return typ;
    }

    /** @return txt 論理名 */
    @Override
    public String getTxt() {
      return txt;
    }

    /** @return col Id */
    @Override
    public String getId() {
      return "F" + Integer.toString(no);
    }

    /** @return datatype データ型のみ */
    @Override
    public DataType getDataType() {
      if (typ.indexOf("INT") != -1) {
        return DefineReport.DataType.INTEGER;
      }
      if (typ.indexOf("DEC") != -1) {
        return DefineReport.DataType.DECIMAL;
      }
      if (typ.indexOf("DATE") != -1 || typ.indexOf("TIMESTAMP") != -1) {
        return DefineReport.DataType.DATE;
      }
      return DefineReport.DataType.TEXT;
    }

    /** @return boolean */
    @Override
    public boolean isText() {
      return getDataType() == DefineReport.DataType.TEXT;
    }

    /** @return digit 桁数 */
    public int[] getDigit() {
      int digit1 = 0, digit2 = 0;
      if (typ.indexOf(",") > 0) {
        String[] sDigit = typ.substring(typ.indexOf("(") + 1, typ.indexOf(")")).split(",");
        digit1 = NumberUtils.toInt(sDigit[0]);
        digit2 = NumberUtils.toInt(sDigit[1]);
      } else if (typ.indexOf("(") > 0) {
        digit1 = NumberUtils.toInt(typ.substring(typ.indexOf("(") + 1, typ.indexOf(")")));
      } else if (StringUtils.equals(typ, JDBCType.SMALLINT.getName())) {
        digit1 = dbNumericTypeInfo.SMALLINT.getDigit();
      } else if (StringUtils.equals(typ, JDBCType.INTEGER.getName())) {
        digit1 = dbNumericTypeInfo.INTEGER.getDigit();
      }
      return new int[] {digit1, digit2};
    }
  }

  /** ソースコード管理マスタレイアウト() */
  public enum MSTSRCCDLayout implements MSTLayout {
    /** 商品コード */
    SHNCD(1, "SHNCD", "CHARACTER(14)", "商品コード"),
    /** ソースコード */
    SRCCD(2, "SRCCD", "CHARACTER(14)", "ソースコード"),
    /** マスタ変更予定日 */
    YOYAKUDT(3, "YOYAKUDT", "INTEGER", "マスタ変更予定日"),
    /** 入力順番 */
    SEQNO(4, "SEQNO", "SMALLINT", "入力順番"),
    /** ソース区分 */
    SOURCEKBN(5, "SOURCEKBN", "SMALLINT", "ソース区分"),
    /** 送信フラグ */
    SENDFLG(6, "SENDFLG", "SMALLINT", "送信フラグ"),
    /** オペレータ */
    OPERATOR(7, "OPERATOR", "VARCHAR(20)", "オペレータ"),
    /** 登録日 */
    ADDDT(8, "ADDDT", "TIMESTAMP", "登録日"),
    /** 更新日 */
    UPDDT(9, "UPDDT", "TIMESTAMP", "更新日"),
    /** 有効開始日 */
    YUKO_STDT(10, "YUKO_STDT", "INTEGER", "有効開始日"),
    /** 有効終了日 */
    YUKO_EDDT(11, "YUKO_EDDT", "INTEGER", "有効終了日");

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
    @Override
    public Integer getNo() {
      return no;
    }

    /** @return tbl 列名 */
    @Override
    public String getCol() {
      return col;
    }

    /** @return typ 列型 */
    @Override
    public String getTyp() {
      return typ;
    }

    /** @return txt 論理名 */
    @Override
    public String getTxt() {
      return txt;
    }

    /** @return col Id */
    @Override
    public String getId() {
      return "F" + Integer.toString(no);
    }

    /** @return datatype データ型のみ */
    @Override
    public DataType getDataType() {
      if (typ.indexOf("INT") != -1) {
        return DefineReport.DataType.INTEGER;
      }
      if (typ.indexOf("DEC") != -1) {
        return DefineReport.DataType.DECIMAL;
      }
      if (typ.indexOf("DATE") != -1 || typ.indexOf("TIMESTAMP") != -1) {
        return DefineReport.DataType.DATE;
      }
      return DefineReport.DataType.TEXT;
    }

    /** @return boolean */
    @Override
    public boolean isText() {
      return getDataType() == DefineReport.DataType.TEXT;
    }

    /** @return digit 桁数 */
    public int[] getDigit() {
      int digit1 = 0, digit2 = 0;
      if (typ.indexOf(",") > 0) {
        String[] sDigit = typ.substring(typ.indexOf("(") + 1, typ.indexOf(")")).split(",");
        digit1 = NumberUtils.toInt(sDigit[0]);
        digit2 = NumberUtils.toInt(sDigit[1]);
      } else if (typ.indexOf("(") > 0) {
        digit1 = NumberUtils.toInt(typ.substring(typ.indexOf("(") + 1, typ.indexOf(")")));
      } else if (StringUtils.equals(typ, JDBCType.SMALLINT.getName())) {
        digit1 = dbNumericTypeInfo.SMALLINT.getDigit();
      } else if (StringUtils.equals(typ, JDBCType.INTEGER.getName())) {
        digit1 = dbNumericTypeInfo.INTEGER.getDigit();
      }
      return new int[] {digit1, digit2};
    }
  }


  /** 仕入グループ商品マスタレイアウト() */
  public enum MSTSIRGPSHNLayout implements MSTLayout {
    /** 商品コード */
    SHNCD(1, "SHNCD", "CHARACTER(14)", "商品コード"),
    /** 店グループ */
    TENGPCD(2, "TENGPCD", "SMALLINT", "店グループ"),
    /** マスタ変更予定日 */
    YOYAKUDT(3, "YOYAKUDT", "INTEGER", "マスタ変更予定日"),
    /** エリア区分 */
    AREAKBN(4, "AREAKBN", "SMALLINT", "エリア区分"),
    /** 仕入先コード */
    SIRCD(5, "SIRCD", "INTEGER", "仕入先コード"),
    /** 配送パターン */
    HSPTN(6, "HSPTN", "SMALLINT", "配送パターン"),
    /** 送信フラグ */
    SENDFLG(7, "SENDFLG", "SMALLINT", "送信フラグ"),
    /** オペレータ */
    OPERATOR(8, "OPERATOR", "VARCHAR(20)", "オペレータ"),
    /** 登録日 */
    ADDDT(9, "ADDDT", "TIMESTAMP", "登録日"),
    /** 更新日 */
    UPDDT(10, "UPDDT", "TIMESTAMP", "更新日");

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
    @Override
    public Integer getNo() {
      return no;
    }

    /** @return tbl 列名 */
    @Override
    public String getCol() {
      return col;
    }

    /** @return typ 列型 */
    @Override
    public String getTyp() {
      return typ;
    }

    /** @return txt 論理名 */
    @Override
    public String getTxt() {
      return txt;
    }

    /** @return idx 列Index */
    public Integer getIdx() {
      return no - 1;
    }

    /** @return col Id */
    @Override
    public String getId() {
      return "F" + Integer.toString(no);
    }

    /** @return datatype データ型のみ */
    @Override
    public DataType getDataType() {
      if (typ.indexOf("INT") != -1) {
        return DefineReport.DataType.INTEGER;
      }
      if (typ.indexOf("DEC") != -1) {
        return DefineReport.DataType.DECIMAL;
      }
      if (typ.indexOf("DATE") != -1 || typ.indexOf("TIMESTAMP") != -1) {
        return DefineReport.DataType.DATE;
      }
      return DefineReport.DataType.TEXT;
    }

    /** @return boolean */
    @Override
    public boolean isText() {
      return getDataType() == DefineReport.DataType.TEXT;
    }

    /** @return digit 桁数 */
    public int[] getDigit() {
      int digit1 = 0, digit2 = 0;
      if (typ.indexOf(",") > 0) {
        String[] sDigit = typ.substring(typ.indexOf("(") + 1, typ.indexOf(")")).split(",");
        digit1 = NumberUtils.toInt(sDigit[0]);
        digit2 = NumberUtils.toInt(sDigit[1]);
      } else if (typ.indexOf("(") > 0) {
        digit1 = NumberUtils.toInt(typ.substring(typ.indexOf("(") + 1, typ.indexOf(")")));
      } else if (StringUtils.equals(typ, JDBCType.SMALLINT.getName())) {
        digit1 = dbNumericTypeInfo.SMALLINT.getDigit();
      } else if (StringUtils.equals(typ, JDBCType.INTEGER.getName())) {
        digit1 = dbNumericTypeInfo.INTEGER.getDigit();
      }
      return new int[] {digit1, digit2};
    }
  }


  /** 売価コントロールマスタレイアウト() */
  public enum MSTBAIKACTLLayout implements MSTLayout {
    /** 商品コード */
    SHNCD(1, "SHNCD", "CHARACTER(14)", "商品コード"),
    /** 店グループ */
    TENGPCD(2, "TENGPCD", "SMALLINT", "店グループ"),
    /** マスタ変更予定日 */
    YOYAKUDT(3, "YOYAKUDT", "INTEGER", "マスタ変更予定日"),
    /** エリア区分 */
    AREAKBN(4, "AREAKBN", "SMALLINT", "エリア区分"),
    /** 原価 */
    GENKAAM(5, "GENKAAM", "DECIMAL(8,2)", "原価"),
    /** 売価 */
    BAIKAAM(6, "BAIKAAM", "INTEGER", "売価"),
    /** 店入数 */
    IRISU(7, "IRISU", "SMALLINT", "店入数"),
    /** 送信フラグ */
    SENDFLG(8, "SENDFLG", "SMALLINT", "送信フラグ"),
    /** オペレータ */
    OPERATOR(9, "OPERATOR", "VARCHAR(20)", "オペレータ"),
    /** 登録日 */
    ADDDT(10, "ADDDT", "TIMESTAMP", "登録日"),
    /** 更新日 */
    UPDDT(11, "UPDDT", "TIMESTAMP", "更新日");

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
    @Override
    public Integer getNo() {
      return no;
    }

    /** @return tbl 列名 */
    @Override
    public String getCol() {
      return col;
    }

    /** @return typ 列型 */
    @Override
    public String getTyp() {
      return typ;
    }

    /** @return txt 論理名 */
    @Override
    public String getTxt() {
      return txt;
    }

    /** @return idx 列Index */
    public Integer getIdx() {
      return no - 1;
    }

    /** @return col Id */
    @Override
    public String getId() {
      return "F" + Integer.toString(no);
    }

    /** @return datatype データ型のみ */
    @Override
    public DataType getDataType() {
      if (typ.indexOf("INT") != -1) {
        return DefineReport.DataType.INTEGER;
      }
      if (typ.indexOf("DEC") != -1) {
        return DefineReport.DataType.DECIMAL;
      }
      if (typ.indexOf("DATE") != -1 || typ.indexOf("TIMESTAMP") != -1) {
        return DefineReport.DataType.DATE;
      }
      return DefineReport.DataType.TEXT;
    }

    /** @return boolean */
    @Override
    public boolean isText() {
      return getDataType() == DefineReport.DataType.TEXT;
    }

    /** @return digit 桁数 */
    public int[] getDigit() {
      int digit1 = 0, digit2 = 0;
      if (typ.indexOf(",") > 0) {
        String[] sDigit = typ.substring(typ.indexOf("(") + 1, typ.indexOf(")")).split(",");
        digit1 = NumberUtils.toInt(sDigit[0]);
        digit2 = NumberUtils.toInt(sDigit[1]);
      } else if (typ.indexOf("(") > 0) {
        digit1 = NumberUtils.toInt(typ.substring(typ.indexOf("(") + 1, typ.indexOf(")")));
      } else if (StringUtils.equals(typ, JDBCType.SMALLINT.getName())) {
        digit1 = dbNumericTypeInfo.SMALLINT.getDigit();
      } else if (StringUtils.equals(typ, JDBCType.INTEGER.getName())) {
        digit1 = dbNumericTypeInfo.INTEGER.getDigit();
      }
      return new int[] {digit1, digit2};
    }
  }


  /** 品揃グループマスタレイアウト() */
  public enum MSTSHINAGPLayout implements MSTLayout {
    /** 商品コード */
    SHNCD(1, "SHNCD", "CHARACTER(14)", "商品コード"),
    /** 店グループ */
    TENGPCD(2, "TENGPCD", "SMALLINT", "店グループ"),
    /** マスタ変更予定日 */
    YOYAKUDT(3, "YOYAKUDT", "INTEGER", "マスタ変更予定日"),
    /** エリア区分 */
    AREAKBN(4, "AREAKBN", "SMALLINT", "エリア区分"),
    /** 扱い区分 */
    ATSUKKBN(5, "ATSUKKBN", "SMALLINT", "扱い区分"),
    /** 送信フラグ */
    SENDFLG(6, "SENDFLG", "SMALLINT", "送信フラグ"),
    /** オペレータ */
    OPERATOR(7, "OPERATOR", "VARCHAR(20)", "オペレータ"),
    /** 登録日 */
    ADDDT(8, "ADDDT", "TIMESTAMP", "登録日"),
    /** 更新日 */
    UPDDT(9, "UPDDT", "TIMESTAMP", "更新日");

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
    @Override
    public Integer getNo() {
      return no;
    }

    /** @return tbl 列名 */
    @Override
    public String getCol() {
      return col;
    }

    /** @return typ 列型 */
    @Override
    public String getTyp() {
      return typ;
    }

    /** @return txt 論理名 */
    @Override
    public String getTxt() {
      return txt;
    }

    /** @return col Id */
    @Override
    public String getId() {
      return "F" + Integer.toString(no);
    }

    /** @return datatype データ型のみ */
    @Override
    public DataType getDataType() {
      if (typ.indexOf("INT") != -1) {
        return DefineReport.DataType.INTEGER;
      }
      if (typ.indexOf("DEC") != -1) {
        return DefineReport.DataType.DECIMAL;
      }
      if (typ.indexOf("DATE") != -1 || typ.indexOf("TIMESTAMP") != -1) {
        return DefineReport.DataType.DATE;
      }
      return DefineReport.DataType.TEXT;
    }

    /** @return boolean */
    @Override
    public boolean isText() {
      return getDataType() == DefineReport.DataType.TEXT;
    }

    /** @return digit 桁数 */
    public int[] getDigit() {
      int digit1 = 0, digit2 = 0;
      if (typ.indexOf(",") > 0) {
        String[] sDigit = typ.substring(typ.indexOf("(") + 1, typ.indexOf(")")).split(",");
        digit1 = NumberUtils.toInt(sDigit[0]);
        digit2 = NumberUtils.toInt(sDigit[1]);
      } else if (typ.indexOf("(") > 0) {
        digit1 = NumberUtils.toInt(typ.substring(typ.indexOf("(") + 1, typ.indexOf(")")));
      } else if (StringUtils.equals(typ, JDBCType.SMALLINT.getName())) {
        digit1 = dbNumericTypeInfo.SMALLINT.getDigit();
      } else if (StringUtils.equals(typ, JDBCType.INTEGER.getName())) {
        digit1 = dbNumericTypeInfo.INTEGER.getDigit();
      }
      return new int[] {digit1, digit2};
    }
  }

  /** 店別異部門管理レイアウト() */
  public enum MSTSHNTENBMNLayout implements MSTLayout {
    /** 商品コード */
    SHNCD(1, "SHNCD", "CHARACTER(14)", "商品コード"),
    /** 店別異部門商品コード */
    TENSHNCD(2, "TENSHNCD", "CHARACTER(14)", "店別異部門商品コード"),
    /** 店グループ */
    TENGPCD(3, "TENGPCD", "SMALLINT", "店グループ"),
    /** マスタ変更予定日 */
    YOYAKUDT(4, "YOYAKUDT", "INTEGER", "マスタ変更予定日"),
    /** エリア区分 */
    AREAKBN(5, "AREAKBN", "SMALLINT", "エリア区分"),
    /** 送信フラグ */
    SENDFLG(6, "SENDFLG", "SMALLINT", "送信フラグ"),
    /** オペレータ */
    OPERATOR(7, "OPERATOR", "VARCHAR(20)", "オペレータ"),
    /** 登録日 */
    ADDDT(8, "ADDDT", "TIMESTAMP", "登録日"),
    /** 更新日 */
    UPDDT(9, "UPDDT", "TIMESTAMP", "更新日"),
    /** ソースコード */
    SRCCD(10, "SRCCD", "CHARACTER(14)", "JANコード");

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
    @Override
    public Integer getNo() {
      return no;
    }

    /** @return tbl 列名 */
    @Override
    public String getCol() {
      return col;
    }

    /** @return typ 列型 */
    @Override
    public String getTyp() {
      return typ;
    }

    /** @return txt 論理名 */
    @Override
    public String getTxt() {
      return txt;
    }

    /** @return col Id */
    @Override
    public String getId() {
      return "F" + Integer.toString(no);
    }

    /** @return datatype データ型のみ */
    @Override
    public DataType getDataType() {
      if (typ.indexOf("INT") != -1) {
        return DefineReport.DataType.INTEGER;
      }
      if (typ.indexOf("DEC") != -1) {
        return DefineReport.DataType.DECIMAL;
      }
      if (typ.indexOf("DATE") != -1 || typ.indexOf("TIMESTAMP") != -1) {
        return DefineReport.DataType.DATE;
      }
      return DefineReport.DataType.TEXT;
    }

    /** @return boolean */
    @Override
    public boolean isText() {
      return getDataType() == DefineReport.DataType.TEXT;
    }

    /** @return digit 桁数 */
    public int[] getDigit() {
      int digit1 = 0, digit2 = 0;
      if (typ.indexOf(",") > 0) {
        String[] sDigit = typ.substring(typ.indexOf("(") + 1, typ.indexOf(")")).split(",");
        digit1 = NumberUtils.toInt(sDigit[0]);
        digit2 = NumberUtils.toInt(sDigit[1]);
      } else if (typ.indexOf("(") > 0) {
        digit1 = NumberUtils.toInt(typ.substring(typ.indexOf("(") + 1, typ.indexOf(")")));
      } else if (StringUtils.equals(typ, JDBCType.SMALLINT.getName())) {
        digit1 = dbNumericTypeInfo.SMALLINT.getDigit();
      } else if (StringUtils.equals(typ, JDBCType.INTEGER.getName())) {
        digit1 = dbNumericTypeInfo.INTEGER.getDigit();
      }
      return new int[] {digit1, digit2};
    }
  }



  /** 添加物マスタレイアウト() */
  public enum MSTTENKABUTSULayout implements MSTLayout {
    /** 商品コード */
    SHNCD(1, "SHNCD", "CHARACTER(14)", "商品コード"),
    /** 添加物区分 */
    TENKABKBN(2, "TENKABKBN", "SMALLINT", "添加物区分"),
    /** 添加物コード */
    TENKABCD(3, "TENKABCD", "SMALLINT", "添加物コード"),
    /** マスタ変更予定日 */
    YOYAKUDT(4, "YOYAKUDT", "INTEGER", "マスタ変更予定日"),
    /** 送信フラグ */
    SENDFLG(5, "SENDFLG", "SMALLINT", "送信フラグ"),
    /** オペレータ */
    OPERATOR(6, "OPERATOR", "VARCHAR(20)", "オペレータ"),
    /** 登録日 */
    ADDDT(7, "ADDDT", "TIMESTAMP", "登録日"),
    /** 更新日 */
    UPDDT(8, "UPDDT", "TIMESTAMP", "更新日");

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
    @Override
    public Integer getNo() {
      return no;
    }

    /** @return tbl 列名 */
    @Override
    public String getCol() {
      return col;
    }

    /** @return typ 列型 */
    @Override
    public String getTyp() {
      return typ;
    }

    /** @return txt 論理名 */
    @Override
    public String getTxt() {
      return txt;
    }

    /** @return col Id */
    @Override
    public String getId() {
      return "F" + Integer.toString(no);
    }

    /** @return datatype データ型のみ */
    @Override
    public DataType getDataType() {
      if (typ.indexOf("INT") != -1) {
        return DefineReport.DataType.INTEGER;
      }
      if (typ.indexOf("DEC") != -1) {
        return DefineReport.DataType.DECIMAL;
      }
      if (typ.indexOf("DATE") != -1 || typ.indexOf("TIMESTAMP") != -1) {
        return DefineReport.DataType.DATE;
      }
      return DefineReport.DataType.TEXT;
    }

    /** @return boolean */
    @Override
    public boolean isText() {
      return getDataType() == DefineReport.DataType.TEXT;
    }

    /** @return digit 桁数 */
    public int[] getDigit() {
      int digit1 = 0, digit2 = 0;
      if (typ.indexOf(",") > 0) {
        String[] sDigit = typ.substring(typ.indexOf("(") + 1, typ.indexOf(")")).split(",");
        digit1 = NumberUtils.toInt(sDigit[0]);
        digit2 = NumberUtils.toInt(sDigit[1]);
      } else if (typ.indexOf("(") > 0) {
        digit1 = NumberUtils.toInt(typ.substring(typ.indexOf("(") + 1, typ.indexOf(")")));
      } else if (StringUtils.equals(typ, JDBCType.SMALLINT.getName())) {
        digit1 = dbNumericTypeInfo.SMALLINT.getDigit();
      } else if (StringUtils.equals(typ, JDBCType.INTEGER.getName())) {
        digit1 = dbNumericTypeInfo.INTEGER.getDigit();
      }
      return new int[] {digit1, digit2};
    }
  }

  /** グループ分類管理マスタレイアウト() */
  public enum MSTGRPLayout implements MSTLayout {
    /** 商品コード */
    SHNCD(1, "SHNCD", "CHARACTER(14)", "商品コード"),
    /** グループ分類ID */
    GRPID(2, "GRPID", "INTEGER", "グループ分類ID"),
    /** マスタ変更予定日 */
    YOYAKUDT(3, "YOYAKUDT", "INTEGER", "マスタ変更予定日"),
    /** 入力順番 */
    SEQNO(4, "SEQNO", "SMALLINT", "入力順番"),
    /** 送信フラグ */
    SENDFLG(5, "SENDFLG", "SMALLINT", "送信フラグ"),
    /** オペレータ */
    OPERATOR(6, "OPERATOR", "VARCHAR(20)", "オペレータ"),
    /** 登録日 */
    ADDDT(7, "ADDDT", "TIMESTAMP", "登録日"),
    /** 更新日 */
    UPDDT(8, "UPDDT", "TIMESTAMP", "更新日"),
    /** グループ分類名(別テーブル) */
    GRPKN(9, "GRPKN", "VARCHAR(100)", "グループ分類名");

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
    @Override
    public Integer getNo() {
      return no;
    }

    /** @return tbl 列名 */
    @Override
    public String getCol() {
      return col;
    }

    /** @return typ 列型 */
    @Override
    public String getTyp() {
      return typ;
    }

    /** @return txt 論理名 */
    @Override
    public String getTxt() {
      return txt;
    }

    /** @return col Id */
    @Override
    public String getId() {
      return "F" + Integer.toString(no);
    }

    /** @return datatype データ型のみ */
    @Override
    public DataType getDataType() {
      if (typ.indexOf("INT") != -1) {
        return DefineReport.DataType.INTEGER;
      }
      if (typ.indexOf("DEC") != -1) {
        return DefineReport.DataType.DECIMAL;
      }
      if (typ.indexOf("DATE") != -1 || typ.indexOf("TIMESTAMP") != -1) {
        return DefineReport.DataType.DATE;
      }
      return DefineReport.DataType.TEXT;
    }

    /** @return boolean */
    @Override
    public boolean isText() {
      return getDataType() == DefineReport.DataType.TEXT;
    }

    /** @return digit 桁数 */
    public int[] getDigit() {
      int digit1 = 0, digit2 = 0;
      if (typ.indexOf(",") > 0) {
        String[] sDigit = typ.substring(typ.indexOf("(") + 1, typ.indexOf(")")).split(",");
        digit1 = NumberUtils.toInt(sDigit[0]);
        digit2 = NumberUtils.toInt(sDigit[1]);
      } else if (typ.indexOf("(") > 0) {
        digit1 = NumberUtils.toInt(typ.substring(typ.indexOf("(") + 1, typ.indexOf(")")));
      } else if (StringUtils.equals(typ, JDBCType.SMALLINT.getName())) {
        digit1 = dbNumericTypeInfo.SMALLINT.getDigit();
      } else if (StringUtils.equals(typ, JDBCType.INTEGER.getName())) {
        digit1 = dbNumericTypeInfo.INTEGER.getDigit();
      }
      return new int[] {digit1, digit2};
    }
  }

  /** 自動発注管理マスタレイアウト() */
  public enum MSTAHSLayout implements MSTLayout {
    /** 商品コード */
    SHNCD(1, "SHNCD", "CHARACTER(14)", "商品コード"),
    /** 店コード */
    TENCD(2, "TENCD", "SMALLINT", "店コード"),
    /** マスタ変更予定日 */
    YOYAKUDT(3, "YOYAKUDT", "INTEGER", "マスタ変更予定日"),
    /** 自動発注区分 */
    AHSKB(4, "AHSKB", "CHARACTER(1)", "自動発注区分"),
    /** 送信フラグ */
    SENDFLG(5, "SENDFLG", "SMALLINT", "送信フラグ"),
    /** オペレータ */
    OPERATOR(6, "OPERATOR", "VARCHAR(20)", "オペレータ"),
    /** 登録日 */
    ADDDT(7, "ADDDT", "TIMESTAMP", "登録日"),
    /** 更新日 */
    UPDDT(8, "UPDDT", "TIMESTAMP", "更新日");

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
    @Override
    public Integer getNo() {
      return no;
    }

    /** @return tbl 列名 */
    @Override
    public String getCol() {
      return col;
    }

    /** @return typ 列型 */
    @Override
    public String getTyp() {
      return typ;
    }

    /** @return txt 論理名 */
    @Override
    public String getTxt() {
      return txt;
    }

    /** @return col Id */
    @Override
    public String getId() {
      return "F" + Integer.toString(no);
    }

    /** @return datatype データ型のみ */
    @Override
    public DataType getDataType() {
      if (typ.indexOf("INT") != -1) {
        return DefineReport.DataType.INTEGER;
      }
      if (typ.indexOf("DEC") != -1) {
        return DefineReport.DataType.DECIMAL;
      }
      if (typ.indexOf("DATE") != -1 || typ.indexOf("TIMESTAMP") != -1) {
        return DefineReport.DataType.DATE;
      }
      return DefineReport.DataType.TEXT;
    }

    /** @return boolean */
    @Override
    public boolean isText() {
      return getDataType() == DefineReport.DataType.TEXT;
    }

    /** @return digit 桁数 */
    public int[] getDigit() {
      int digit1 = 0, digit2 = 0;
      if (typ.indexOf(",") > 0) {
        String[] sDigit = typ.substring(typ.indexOf("(") + 1, typ.indexOf(")")).split(",");
        digit1 = NumberUtils.toInt(sDigit[0]);
        digit2 = NumberUtils.toInt(sDigit[1]);
      } else if (typ.indexOf("(") > 0) {
        digit1 = NumberUtils.toInt(typ.substring(typ.indexOf("(") + 1, typ.indexOf(")")));
      } else if (StringUtils.equals(typ, JDBCType.SMALLINT.getName())) {
        digit1 = dbNumericTypeInfo.SMALLINT.getDigit();
      } else if (StringUtils.equals(typ, JDBCType.INTEGER.getName())) {
        digit1 = dbNumericTypeInfo.INTEGER.getDigit();
      }
      return new int[] {digit1, digit2};
    }
  }



  /** ジャーナル_商品マスタレイアウト(正マスタとの差分) */
  public enum JNLSHNLayout implements MSTLayout {
    /** SEQ */
    SEQ(1, "SEQ", "INTEGER", "SEQ"),
    /** 更新情報_更新日時 */
    INF_DATE(2, "INF_DATE", "TIMESTAMP", "更新情報_更新日時"),
    /** 更新情報_オペレータ */
    INF_OPERATOR(3, "INF_OPERATOR", "VARCHAR(20)", "更新情報_オペレータ"),
    /** 更新情報_テーブル区分 */
    INF_TABLEKBN(4, "INF_TABLEKBN", "SMALLINT", "更新情報_テーブル区分"),
    /** 更新情報_処理区分 */
    INF_TRANKBN(5, "INF_TRANKBN", "SMALLINT", "更新情報_処理区分");

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
    @Override
    public Integer getNo() {
      return no;
    }

    /** @return tbl 列名 */
    @Override
    public String getCol() {
      return col;
    }

    /** @return typ 列型 */
    @Override
    public String getTyp() {
      return typ;
    }

    /** @return txt 論理名 */
    @Override
    public String getTxt() {
      return txt;
    }

    /** @return col Id */
    @Override
    public String getId() {
      return "F" + Integer.toString(no);
    }

    /** @return col Id */
    public String getId2() {
      return "F" + Integer.toString(no + MSTSHNLayout.values().length);
    }

    /** @return datatype データ型のみ */
    @Override
    public DataType getDataType() {
      if (typ.indexOf("INT") != -1) {
        return DefineReport.DataType.INTEGER;
      }
      if (typ.indexOf("DEC") != -1) {
        return DefineReport.DataType.DECIMAL;
      }
      if (typ.indexOf("DATE") != -1 || typ.indexOf("TIMESTAMP") != -1) {
        return DefineReport.DataType.DATE;
      }
      return DefineReport.DataType.TEXT;
    }

    /** @return boolean */
    @Override
    public boolean isText() {
      return getDataType() == DefineReport.DataType.TEXT;
    }
  }

  /** CSV取込トラン_商品マスタレイアウト(正マスタとの差分) */
  public enum CSVSHNLayout implements MSTLayout {
    /** SEQ */
    SEQ(1, "SEQ", "INTEGER", "SEQ"),
    /** 入力番号 */
    INPUTNO(2, "INPUTNO", "INTEGER", "入力番号"),
    /** エラーコード */
    ERRCD(3, "ERRCD", "SMALLINT", "エラーコード"),
    /** エラー箇所 */
    ERRFLD(4, "ERRFLD", "VARCHAR(100)", "エラー箇所"),
    /** エラー値 */
    ERRVL(5, "ERRVL", "VARCHAR(100)", "エラー値"),
    /** エラーテーブル名 */
    ERRTBLNM(6, "ERRTBLNM", "VARCHAR(100)", "エラーテーブル名"),
    /** CSV登録区分 */
    CSV_UPDKBN(7, "CSV_UPDKBN", "CHARACTER(1)", "CSV登録区分"),
    /** 桁指定 */
    KETAKBN(8, "KETAKBN", "SMALLINT", "桁指定");

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
    @Override
    public Integer getNo() {
      return no;
    }

    /** @return tbl 列名 */
    @Override
    public String getCol() {
      return col;
    }

    /** @return typ 列型 */
    @Override
    public String getTyp() {
      return typ;
    }

    /** @return txt 論理名 */
    @Override
    public String getTxt() {
      return txt;
    }

    /** @return col Id */
    @Override
    public String getId() {
      return "F" + Integer.toString(no);
    }

    /** @return col Id */
    public String getId2() {
      return "F" + Integer.toString(no + MSTSHNLayout.values().length);
    }

    /** @return datatype データ型のみ */
    @Override
    public DataType getDataType() {
      if (typ.indexOf("INT") != -1) {
        return DefineReport.DataType.INTEGER;
      }
      if (typ.indexOf("DEC") != -1) {
        return DefineReport.DataType.DECIMAL;
      }
      if (typ.indexOf("DATE") != -1 || typ.indexOf("TIMESTAMP") != -1) {
        return DefineReport.DataType.DATE;
      }
      return DefineReport.DataType.TEXT;
    }

    /** @return boolean */
    @Override
    public boolean isText() {
      return getDataType() == DefineReport.DataType.TEXT;
    }
  }

  /** ジャーナル_子テーブル共通レイアウト(正マスタとの差分) */
  public enum JNLCMNLayout implements MSTLayout {
    /** SEQ */
    SEQ(1, "SEQ", "INTEGER", "SEQ"),
    /** 連番 */
    RENNO(2, "RENNO", "SMALLINT", "連番");

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
    @Override
    public Integer getNo() {
      return no;
    }

    /** @return tbl 列名 */
    @Override
    public String getCol() {
      return col;
    }

    /** @return typ 列型 */
    @Override
    public String getTyp() {
      return typ;
    }

    /** @return txt 論理名 */
    @Override
    public String getTxt() {
      return txt;
    }

    /** @return col Id */
    @Override
    public String getId() {
      return "F" + Integer.toString(no);
    }

    /** @return col Id2 */
    public String getId2(Integer idx) {
      return "F" + Integer.toString(no + idx);
    }

    /** @return datatype データ型のみ */
    @Override
    public DataType getDataType() {
      if (typ.indexOf("INT") != -1) {
        return DefineReport.DataType.INTEGER;
      }
      if (typ.indexOf("DEC") != -1) {
        return DefineReport.DataType.DECIMAL;
      }
      if (typ.indexOf("DATE") != -1 || typ.indexOf("TIMESTAMP") != -1) {
        return DefineReport.DataType.DATE;
      }
      return DefineReport.DataType.TEXT;
    }

    /** @return boolean */
    @Override
    public boolean isText() {
      return getDataType() == DefineReport.DataType.TEXT;
    }
  }

  /** CSV取込トラン_子テーブル共通レイアウト(正マスタとの差分) */
  public enum CSVCMNLayout implements MSTLayout {
    /** SEQ */
    SEQ(1, "SEQ", "INTEGER", "SEQ"),
    /** 入力番号 */
    INPUTNO(2, "INPUTNO", "INTEGER", "入力番号"),
    /** 入力枝番 */
    INPUTEDANO(3, "INPUTEDANO", "SMALLINT", "入力枝番");

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
    @Override
    public Integer getNo() {
      return no;
    }

    /** @return tbl 列名 */
    @Override
    public String getCol() {
      return col;
    }

    /** @return typ 列型 */
    @Override
    public String getTyp() {
      return typ;
    }

    /** @return txt 論理名 */
    @Override
    public String getTxt() {
      return txt;
    }

    /** @return col Id */
    @Override
    public String getId() {
      return "F" + Integer.toString(no);
    }

    /** @return col Id2 */
    public String getId2(Integer idx) {
      return "F" + Integer.toString(no + idx);
    }

    /** @return datatype データ型のみ */
    @Override
    public DataType getDataType() {
      if (typ.indexOf("INT") != -1) {
        return DefineReport.DataType.INTEGER;
      }
      if (typ.indexOf("DEC") != -1) {
        return DefineReport.DataType.DECIMAL;
      }
      if (typ.indexOf("DATE") != -1 || typ.indexOf("TIMESTAMP") != -1) {
        return DefineReport.DataType.DATE;
      }
      return DefineReport.DataType.TEXT;
    }

    /** @return boolean */
    @Override
    public boolean isText() {
      return getDataType() == DefineReport.DataType.TEXT;
    }
  }

}
