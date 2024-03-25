/**
 *
 */
package common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang.StringUtils;
import authentication.bean.User;
import authentication.defines.Consts;
import dao.ReportBM015Dao;
import dao.ReportJU012Dao;
import dao.ReportJU032Dao;
import dao.ReportTG001Dao;
import dao.ReportTG003Dao;
import dao.ReportTG040Dao;
import dao.ReportTM002Dao;
import dao.ReportTM004Dao;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import servlet.EasyToJSONServlet;

/**
 * @author Eatone
 *
 */
public class GetSqlCommandCheck {

  public GetSqlCommandCheck() {
    super();
  }

  /**
   *
   * EasyToJSONServletのcheck処理を記載
   *
   */
  public static String getSqlcommand(HttpServletRequest request, HashMap<String, String> maps, String datatype, String jndiName) throws Exception {

    /** 画面初期化処理 */
    // セッションの取得
    HttpSession session = request.getSession();

    User lusr = (User) request.getSession().getAttribute(Consts.STR_SES_LOGINUSER);
    String sysdate = (String) request.getSession().getAttribute(Consts.STR_SES_LOGINDT);

    // パラメータの確認
    String outpage = maps.get(DefineReport.ID_PARAM_PAGE) == null ? "" : maps.get(DefineReport.ID_PARAM_PAGE);
    String outobj = maps.get(DefineReport.ID_PARAM_OBJ) == null ? "" : maps.get(DefineReport.ID_PARAM_OBJ);
    String outjson = maps.get(DefineReport.ID_PARAM_JSON) == null ? "" : maps.get(DefineReport.ID_PARAM_JSON);

    // JSONパラメータの解析
    JSONArray map = new JSONArray();
    if (!"".equals(outjson)) {
      map = (JSONArray) JSONSerializer.toJSON(outjson);
    }

    // 配列準備
    ArrayList<String> paramData = new ArrayList<>();
    String sqlcommand = "";
    String json = "";

    JSONObject obj = (JSONObject) map.get(0);
    String value = obj.optString("value");
    String key = obj.optString("KEY");

    // マスタ存在チェック
    if (StringUtils.equals(key, "MST_CNT")) {
      String tbl = "";
      String col = "";
      String rep = "";
      String szWhere = "";
      // 商品コード
      if (outobj.equals(DefineReport.InpText.SHNCD.getObj())) {
        tbl = "INAMS.MSTSHN";
        col = "SHNCD";
        szWhere = " and COALESCE(UPDKBN, 0) = 0";
      }
      // 部門コード
      if (outobj.equals(DefineReport.InpText.BMNCD.getObj())) {
        tbl = "INAMS.MSTBMN";
        col = "BMNCD";
        szWhere = " and COALESCE(UPDKBN, 0) = 0";
      }
      // 仕入先コード
      if (outobj.equals(DefineReport.InpText.SIRCD.getObj())) {
        tbl = "INAMS.MSTSIR";
        col = "SIRCD";
        szWhere = " and COALESCE(UPDKBN, 0) = 0";
      }
      // 実仕入先コード
      if (outobj.equals(DefineReport.InpText.RSIRCD.getObj())) {
        tbl = "INAMS.MSTFUKUSUSIR_R";
        col = "SIRCD";
        szWhere = " and COALESCE(UPDKBN, 0) = 0";
      }
      // メーカーコード
      if (outobj.equals(DefineReport.InpText.MAKERCD.getObj())) {
        tbl = "INAMS.MSTMAKER";
        col = "MAKERCD";
      }
      // 配送パターン仕入先
      if (outobj.equals("MSTHSPTNSIR") && value.length() > 1) {
        tbl = "INAMS.MSTHSPTNSIR";
        if (DefineReport.ID_PAGE_X172.equals(outpage)) {
          col = "HSPTN";

        } else {
          col = "right('00000'||SIRCD, 6)||right('00'||HSPTN, 3)";

          String[] vals = StringUtils.split(value, ",");
          for (String val : vals) {
            rep += ", ?";
            String cd = StringUtils.leftPad(val.split("-")[0], 6, "0") + StringUtils.leftPad(val.split("-")[1], 3, "0");
            paramData.add(cd);
          }
          rep = StringUtils.removeStart(rep, ",");
        }
      }

      // エリア配送パターン仕入先
      if (outobj.equals("area" + DefineReport.InpText.HSPTN.getObj()) && value.length() > 0) {
        tbl = "INAMS.MSTAREAHSPTNSIR";
        col = "HSPTN";
        String hsptn = value.split(",")[0];
        String tengpcd = value.split(",")[1];
        rep += hsptn;
        szWhere += " and TENGPCD = ?";
        paramData.add(tengpcd);
      }

      // 風袋枝番
      if (outobj.equals(DefineReport.InpText.FUTAIEDABAN.getObj())) {
        tbl = "INAMS.MSTKRYOFTAI";
        col = "FTAIECD";
      }
      // 店舗基本マスタ
      if (outobj.equals(DefineReport.InpText.TENCD.getObj())) {
        tbl = "INAMS.MSTTEN";
        col = "TENCD";
        if (DefineReport.ID_PAGE_RP007.equals(outpage) || DefineReport.ID_PAGE_SK003.equals(outpage) || DefineReport.ID_PAGE_SK006.equals(outpage) || "Out_ReportwinST008".equals(outpage)
            || DefineReport.ID_PAGE_TG003.equals(outpage)) { // ランクパターンマスタ：店コピー
          szWhere += " and MISEUNYOKBN <> 9";
        }

        // 廃店チェック
        if (DefineReport.ID_PAGE_TR016.equals(outpage) // 定量:一括追加・削除
            || DefineReport.ID_PAGE_ST008.equals(outpage) // ランクマスタ 店情報 変更
        ) {
          szWhere += " and MISEUNYOKBN = 9";
        }
        szWhere += " and COALESCE(UPDKBN, 0) = 0";
      }

      // 支払先マスタ
      if (outobj.equals("MSTSIHARAI")) {
        tbl = "INAMS.MSTSIHARAI";
        col = "SIRCD";
      }

      // 店舗部門コード
      if (outobj.equals("MSTTENBMN")) {
        tbl = "INAMS.MSTTENBMN";

        if (DefineReport.ID_PAGE_X192.equals(outpage)) {
          col = "AREACD";
          String areacd = value.split(",")[0];
          String bmncd = value.split(",")[1];
          rep += areacd;
          szWhere += " and BMNCD = ?";
          szWhere += " and COALESCE(UPDKBN, 0) = 0";
          paramData.add(bmncd);

        } else if (DefineReport.ID_PAGE_SK003.equals(outpage)) {
          col = "TENCD";
          String tencd = value.split(",")[0];
          String bmncd = value.split(",")[1];
          rep += tencd;
          szWhere += " and BMNCD = ?";
          szWhere += " and COALESCE(UPDKBN, 0) = 0";
          paramData.add(bmncd);
        } else {
          col = "BMNCD";
        }
      }
      // 値付機マスタのチェック
      if (outobj.equals("MSTNETSUKE")) {
        tbl = "INAMS.MSTNETSUKE";
        String callcd = obj.optString("CALLCD");
        col = "CALLCD";
        rep += "?";
        szWhere += " and COALESCE(UPDKBN, 0) = 0";
        paramData.add(callcd);

      }
      // 催しコード
      if (outobj.equals(DefineReport.InpText.MOYSCD.getObj()) && value.length() > 1) {

        String moyskbn = "";
        String moysstdt = "";
        String moysrban = "";

        if (value.length() >= 8) {
          moyskbn = value.substring(0, 1);
          moysstdt = value.substring(1, 7);
          moysrban = value.substring(7);
        } else if (value.length() >= 7) {
          moyskbn = value.substring(0, 1);
          moysstdt = value.substring(1, 7);
        } else if (value.length() >= 1) {
          moyskbn = value.substring(0, 1);
        }

        tbl = "INATK.TOKMOYCD";
        col = "MOYSKBN";
        rep += moyskbn;
        szWhere += " and MOYSSTDT = ?";
        szWhere += " and MOYSRBAN = ?";
        paramData.add(moysstdt);
        paramData.add(moysrban);

        if (DefineReport.ID_PAGE_BM006.equals(outpage)) {
          szWhere += " and MOYSKBN IN ('1','2','3')";
        }
      }
      // 構成ページ
      if (outobj.equals(DefineReport.InpText.KSPAGE.getObj())) {
        tbl = "INAMS.MSTKSPAGE";
        col = "BMNCD";

        String kspage = value.split(",")[0];
        String tencd = value.split(",")[1];

        rep += StringUtils.substring(kspage, 0, 2);
        szWhere += " and BRUICD = ?";
        szWhere += " and PTN = ?";
        szWhere += " and EDABAN = ?";
        szWhere += " and TENCD = ?";
        paramData.add(StringUtils.substring(kspage, 2, 4));
        paramData.add(StringUtils.substring(kspage, 4, 7));
        paramData.add(StringUtils.substring(kspage, 7, 8));
        paramData.add(tencd);
      }

      // 配送グループコード
      if (outobj.equals(DefineReport.InpText.HSGPCD.getObj()) && value.length() > 0) {
        tbl = "INAMS.MSTHSGP";
        col = "BMNCD";
        String hsgpcd = value.split(",")[0];
        String areakbn = value.split(",")[1];
        rep += hsgpcd;
        szWhere += " and AREAKBN = ?";
        szWhere += " and COALESCE(UPDKBN, 0) = 0";
        paramData.add(areakbn);

      }
      // 大分類コード
      if (outobj.equals(DefineReport.InpText.DAICD.getObj()) && value.length() > 0) {
        tbl = "INAMS.MSTDAIBRUI";
        col = "BMNCD";
        String bmncd = value.split(",")[0];
        String adicd = value.split(",")[1];
        szWhere += " and COALESCE(UPDKBN, 0) = 0";
        rep += bmncd;
        szWhere += " and DAICD = ?";
        paramData.add(adicd);
      }
      // 中分類コード
      if (outobj.equals(DefineReport.InpText.CHUCD.getObj()) && value.length() > 0) {
        tbl = "INAMS.MSTCHUBRUI";
        col = "BMNCD";

        if (DefineReport.ID_PAGE_X033.equals(outpage)) {
          String bmncd = value.split(",")[0];
          String adicd = value.split(",")[1];
          String bunrui = value.split(",")[2];
          rep += bmncd;

          if (StringUtils.equals(DefineReport.OptionBunruiKubun.URIBA.getVal(), bunrui)) {
            tbl = "INAMS.MSTDAIBRUI_URI";
          } else if (StringUtils.equals(DefineReport.OptionBunruiKubun.YOUTO.getVal(), bunrui)) {
            tbl = "INAMS.MSTDAIBRUI_YOT";
          }

          szWhere += " and DAICD = ?";
          szWhere += " and COALESCE(UPDKBN, 0) = 0";
          paramData.add(adicd);

        } else {
          String bmncd = value.split(",")[0];
          String adicd = value.split(",")[1];
          String chucd = value.split(",")[2];
          rep += bmncd;
          szWhere += " and DAICD = ?";
          szWhere += " and CHUCD = ?";
          szWhere += " and COALESCE(UPDKBN, 0) = 0";
          paramData.add(adicd);
          paramData.add(chucd);
        }
      }
      // ランクNo.
      if (outobj.equals(DefineReport.InpText.RANKNO.getObj()) && value.length() > 1) {
        tbl = "INATK.TOKRANK";
        col = "BMNCD";
        String bmncd = value.split(",")[0];
        String rankno = value.split(",")[1];
        rep += bmncd;
        szWhere += " and RANKNO = ?";
        szWhere += " and COALESCE(UPDKBN, 0) = 0";
        paramData.add(rankno);
      }

      // 臨時ランクNo.
      if (outobj.equals(DefineReport.InpText.RANKNO.getObj() + "_EX") && value.length() > 1) {
        tbl = "INATK.TOKRANKEX";
        col = "BMNCD";
        String bmncd = value.split(",")[0];
        String rankno = value.split(",")[1];
        String moyscd = value.split(",")[2];

        rep += bmncd;
        szWhere += " and RANKNO = ? and MOYSKBN = ? and MOYSSTDT = ? and MOYSRBAN = ?";
        szWhere += " and COALESCE(UPDKBN, 0) = 0";

        paramData.add(rankno);
        paramData.add(StringUtils.substring(moyscd, 0, 1));
        paramData.add(StringUtils.substring(moyscd, 1, 7));
        paramData.add(StringUtils.substring(moyscd, 7, 10));
      }

      // 数量パターンNo.
      if (outobj.equals(DefineReport.InpText.SRYPTNNO.getObj()) && value.length() > 1) {

        tbl = "INATK.TOKSRPTN";
        col = "BMNCD";
        String bmncd = value.split(",")[0];
        String sryptnno = value.split(",")[1];
        rep += bmncd;

        if (Integer.valueOf(sryptnno) >= 900) {
          String moyskbn = value.split(",")[2];
          String moysstdt = value.split(",")[3];
          String moysrban = value.split(",")[4];
          tbl = "INATK.TOKSRPTNEX";
          szWhere += " and MOYSKBN = ?";
          szWhere += " and MOYSSTDT = ?";
          szWhere += " and MOYSRBAN = ?";
          paramData.add(moyskbn);
          paramData.add(moysstdt);
          paramData.add(moysrban);
        }

        szWhere += " and SRYPTNNO = ?";
        szWhere += " and COALESCE(UPDKBN, 0) = 0";
        paramData.add(sryptnno);
      }
      // 通常率パターンNo.
      if (outobj.equals(DefineReport.InpText.RTPTNNO.getObj()) && value.length() > 1) {
        tbl = "INATK.TOKRTPTN";
        col = "BMNCD";
        String bmncd = value.split(",")[0];
        String rtptnno = value.split(",")[1];
        rep += bmncd;
        szWhere += " and RTPTNNO = ?";
        szWhere += " and COALESCE(UPDKBN, 0) = 0";
        paramData.add(rtptnno);
      }
      // 実績率パターンNo.
      if (outobj.equals(DefineReport.InpText.JRTPTNNO.getObj()) && value.length() > 1) {
        tbl = "INATK.TOKJRTPTN";
        col = "BMNCD";
        String jrtptnno = value.split(",")[1];
        rep += StringUtils.substring(jrtptnno, 0, 3);
        szWhere += " and WWMMFLG = ?";
        szWhere += " and YYMM = ?";
        szWhere += " and DAICD = ?";
        szWhere += " and CHUCD = ?";
        paramData.add(StringUtils.substring(jrtptnno, 3, 4));
        paramData.add(StringUtils.substring(jrtptnno, 4, 8));
        paramData.add(StringUtils.substring(jrtptnno, 8, 10));
        paramData.add(StringUtils.substring(jrtptnno, 10, 12));
      }
      // チラシのみ部門
      if (outobj.equals("TOKCHIRASBMN")) {
        tbl = "INATK.TOKCHIRASBMN";
        col = "BMNCD";
        szWhere += " and MOYSKBN = " + obj.optString("MOYSKBN");
        szWhere += " and MOYSSTDT= " + obj.optString("MOYSSTDT");
        szWhere += " and MOYSRBAN= " + obj.optString("MOYSRBAN");
      }

      // 所属コード
      if (outobj.equals(DefineReport.InpText.SZKCD.getObj()) && value.length() > 0) {
        tbl = "INAAD.SYSSZK";
        col = "SZKCD";
      }

      // 便区分
      if (outobj.equals(DefineReport.InpText.BINKBN.getObj()) && value.length() > 0) {
        tbl = "INAMS.MSTMEISHO";
        col = "MEISHOCD";
        szWhere += " and MEISHOKBN = " + DefineReport.MeisyoSelect.KBN10665.getCd();
      }
      // ワッペン区分
      if (outobj.equals(DefineReport.InpText.WAPPNKBN.getObj()) && value.length() > 0) {
        tbl = "INAMS.MSTMEISHO";
        col = "MEISHOCD";
        szWhere += " and MEISHOKBN = " + DefineReport.MeisyoSelect.KBN10666.getCd();
      }

      // 子№
      if (outobj.equals(DefineReport.InpText.CHLDNO.getObj()) && value.length() > 0) {

        String moyskbn = value.split(",")[0];
        String moysstdt = value.split(",")[1];
        String moysrban = value.split(",")[2];
        String parno = value.split(",")[3];
        String chldno = value.split(",")[4];
        String toktg = value.split(",")[5];

        col = "CHLDNO";
        rep += chldno;
        szWhere += " and MOYSKBN=?";
        szWhere += " and MOYSSTDT=?";
        szWhere += " and MOYSRBAN=?";
        szWhere += " and PARNO=?";
        paramData.add(moyskbn);
        paramData.add(moysstdt);
        paramData.add(moysrban);
        paramData.add(parno);

        // 更新の場合こちらを追加
        if (value.split(",").length >= 7) {
          String bmncd = value.split(",")[6];
          String kanrino = value.split(",")[7];
          String kanrieno = value.split(",")[8];
          szWhere += " and BMNCD=?";
          szWhere += " and KANRINO=?";
          szWhere += " and KANRIENO=?";
          paramData.add(bmncd);
          paramData.add(kanrino);
          paramData.add(kanrieno);
        }

        if (toktg.equals("1")) {
          tbl = "INATK.TOKTG_SHN";
        } else {
          tbl = "INATK.TOKSP_SHN";
        }
      }

      // 週№
      if (outobj.equals(DefineReport.InpText.SHUNO.getObj())) {
        if (DefineReport.ID_PAGE_TG016.equals(outpage)) {
          tbl = "INAAD.SYSSHUNO";
        } else {
          tbl = "INATK.TOKMOYSYU";
          szWhere = " and COALESCE(UPDKBN, 0) = 0";
        }
        col = "SHUNO";
      }

      if (outobj.equals("txt_pass_old") || outobj.equals("txt_pass_new")) {

        String pass = value;
        String id = lusr.getId();
        int cd = lusr.getCD_user();

        if (DefineReport.ID_PAGE_X242.equals(outpage)) {
          pass = value.split(",")[0];
          id = value.split(",")[1];
          cd = Integer.valueOf(value.split(",")[2]);
        }

        tbl = "KEYSYS.SYS_USERS";
        col = "CD_USER";
        rep += cd;

        // ユーザーID
        szWhere = " and USER_ID=? ";
        paramData.add(id);

        if (outobj.equals("txt_pass_new")) {
          szWhere +=
              " and COALESCE(PASSWORDS,'0')<>? and COALESCE(PASSWORDS_1,'0')<>? and COALESCE(PASSWORDS_2,'0')<>? and COALESCE(PASSWORDS_3,'0')<>? and COALESCE(PASSWORDS_4,'0')<>? and COALESCE(PASSWORDS_5,'0')<>? ";
          paramData.add(pass);
          paramData.add(pass);
          paramData.add(pass);
          paramData.add(pass);
          paramData.add(pass);
          paramData.add(pass);
        } else {
          szWhere += " and PASSWORDS=? ";
          paramData.add(pass);
        }
      }

      if (tbl.length() > 0 && col.length() > 0) {
        if (paramData.size() > 0 && rep.length() > 0) {
          sqlcommand = DefineReport.ID_SQL_CHK_TBL.replace("@T", tbl).replaceAll("@C", col).replace("?", rep) + szWhere;
        } else {
          paramData.add(value);
          sqlcommand = DefineReport.ID_SQL_CHK_TBL.replace("@T", tbl).replaceAll("@C", col) + szWhere;
        }
      }

      if (outobj.equals("ADDSHNCD")) {
        tbl = "INAMS.MSTSHN";
        // 商品コード
        paramData.add(obj.optString("value1"));
        // 部門コード
        paramData.add(obj.optString("value2"));
        sqlcommand = DefineReport.ID_SQL_CHK_TBL_MULTI.replace("@T", tbl);
        sqlcommand += " nvl(UPDKBN, 0) = 0 and shncd = ? and BMNCD = ? and IRYOREFLG = 0 and SHNKBN <> 5 ";
      }
    }
    if (StringUtils.equals(key, "MST_CNT") && outobj.equals(DefineReport.MeisyoSelect.KBN617.getObj())) {
      String tbl = "INAMS.MSTFTAI";
      // 風袋コード
      paramData.add(obj.optString("FUTAIEDABAN"));
      // 風袋種類
      paramData.add(obj.optString("KBN617"));
      sqlcommand = DefineReport.ID_SQL_CHK_TBL_MULTI.replace("@T", tbl);
      sqlcommand += " FTAIECD = ? and FTAISHUKBN = ?";
    }
    if (StringUtils.equals(key, "MST_CNT") && DefineReport.ID_PAGE_ST008.equals(outpage)) {
      paramData = new ArrayList<>();

      paramData.add(obj.optString("value1"));
      paramData.add(obj.optString("value2"));
      sqlcommand = DefineReport.ID_SQL_TENBMN;
    }
    if (StringUtils.equals(key, "MST_CNT") && outobj.equals(DefineReport.InpText.SHNKBN.getObj())) {
      String tbl = "INAMS.MSTSHN";
      // 商品コード
      paramData.add(obj.optString("value"));
      sqlcommand = DefineReport.ID_SQL_CHK_TBL_MULTI.replace("@T", tbl);
      sqlcommand += " SHNCD = ? and COALESCE(UPDKBN, 0) = 0 and SHNKBN <> 5 ";
    }
    if (StringUtils.equals(key, "MST_CNT") && outobj.equals(DefineReport.InpText.SRCCD.getObj())) {
      String tbl = "INAMS.MSTSRCCD";
      sqlcommand = DefineReport.ID_SQL_CHK_TBL_MULTI.replace("@T", tbl);
      sqlcommand += "SRCCD = ?";
      paramData.add(obj.optString("SRCCD"));
    }
    // 件数チェック
    if (StringUtils.equals(key, "CNT")) {
      // 商品コード : 登録限度数
      if (outobj.equals(DefineReport.InpText.SHNCD.getObj())) {
        paramData.add(sysdate);
        sqlcommand = DefineReport.ID_SQL_SHN_CHK_UPDATECNT;
      }

      // 店グループ : 店コード重複チェック
      if (StringUtils.startsWith(outobj, DefineReport.Grid.TENGP.getObj())) {
        String[] tengpcds = StringUtils.split(value, ",");
        if (tengpcds.length > 0) {
          paramData.add(obj.optString("GPKBN"));
          paramData.add(obj.optString("BMNCD"));
          paramData.add(obj.optString("AREAKBN"));
          String rep = "";
          for (String cd : tengpcds) {
            rep += ", ?";
            paramData.add(cd);
          }
          rep = StringUtils.removeStart(rep, ",");
          sqlcommand = DefineReport.ID_SQL_TENGP_CHK_TEN_CNT.replace("@", rep);
        }
      }

      if (outobj.equals(DefineReport.InpText.MOYSSTDT.getObj())) {
        HashMap<String, String> hmap = new HashMap<>();
        hmap.put("KEY", obj.optString("KEY"));
        hmap.put("MOYSKBN", obj.optString("MOYSKBN"));
        hmap.put("MOYSSTDT", obj.optString("MOYSSTDT"));
        hmap.put("REPORT", outpage);
        JSONObject inf = new ReportTM002Dao(jndiName).createSqlSelTOKMOYCD(hmap);
        JSONArray prm = inf.optJSONArray("PRM");
        for (int i = 0; i < prm.size(); i++) {
          paramData.add(prm.optString(i));
        }
        sqlcommand = inf.optString("SQL") + " fetch first 1 rows only";
      }

      // 催しコードPLU配信済みフラグチェック
      if (StringUtils.startsWith(outobj, DefineReport.InpText.MOYSCD.getObj())) {

        if (!StringUtils.isEmpty(value) && value.length() >= 8) {
          paramData.add(value.substring(0, 1)); // 催し区分
          paramData.add(value.substring(1, 7)); // 催し開始日
          paramData.add(value.substring(7)); // 催し連番

          sqlcommand = DefineReport.ID_SQL_TOKMOYCD;
        }
      }

      // 特売共通：全品割引商品登録時のチェック
      if (outobj.equals("TOKRS_KKK")) {
        paramData.add(obj.optString("HBSTDT")); // 販売開始日
        paramData.add(obj.optString("SHNCD")); // 商品コード
        paramData.add(value); // 割引率
        sqlcommand = DefineReport.ID_SQL_TOKRS_KKK_CNT;
      }

      // 「アンケート月度枝番」と「アンケート作成日」チェック
      if (outobj.equals(DefineReport.InpText.QACREDT.getObj())) {
        // ITFコード情報、もしくはエラー情報が返ってくる
        JSONObject result = ReportTG001Dao.checkQACREDTAndQAENO(lusr, obj);
        new EasyToJSONServlet();
        sqlcommand = EasyToJSONServlet.convertJsonobjToSql(result);
      }

      // 「アンケート月度枝番」と「アンケート取込開始日」チェック
      if (outobj.equals(DefineReport.InpText.QADEVSTDT.getObj())) {
        // ITFコード情報、もしくはエラー情報が返ってくる
        JSONObject result = ReportTG001Dao.checkQADEVSTDTAndQAENO(lusr, obj);
        new EasyToJSONServlet();
        sqlcommand = EasyToJSONServlet.convertJsonobjToSql(result);
      }
    }

    // BT006 (分類割引 課題No.12：販売期間_終了日)
    if (StringUtils.equals(key, "HBEDDT")) {
      if (StringUtils.startsWith(outobj, DefineReport.InpText.MOYSCD.getObj())) {

        if (!StringUtils.isEmpty(value) && value.length() >= 1) {
          paramData.add(value); // 企画No
          sqlcommand = DefineReport.ID_SQL_HBEDDT;
        }
      }
    }

    // BT006 (分類割引 課題No.12：処理日付)
    if (StringUtils.equals(key, "SHORIDT")) {
      if (StringUtils.startsWith(outobj, DefineReport.InpText.MOYSCD.getObj())) {
        sqlcommand = DefineReport.ID_SQLSHORIDT;
      }
    }

    // BT006 (分類割引 課題No.12：PLU配信日)
    if (StringUtils.equals(key, "PLUSDDT")) {
      if (StringUtils.startsWith(outobj, DefineReport.InpText.MOYSCD.getObj())) {

        if (!StringUtils.isEmpty(value) && value.length() >= 8) {
          paramData.add(value.substring(0, 1)); // 催し区分
          paramData.add(value.substring(1, 7)); // 催し開始日
          paramData.add(value.substring(7)); // 催し連番

          sqlcommand = DefineReport.ID_SQL_PLUSDDT;
        }
      }
    }

    // 分類割引 新規 重複チェック
    if (StringUtils.equals(key, "NEWDEPLICATECHEACK")) {
      String value2 = obj.optString("value2");
      String value3 = obj.optString("value3");
      String value4 = obj.optString("value4");

      if (StringUtils.startsWith(outobj, DefineReport.InpText.MOYSCD.getObj())) {

        if (!StringUtils.isEmpty(value) && value.length() >= 8) {
          paramData.add(value.substring(0, 1)); // 催し区分
          paramData.add(value.substring(1, 7)); // 催し開始日
        }

        if (!StringUtils.isEmpty(value2)) {
          paramData.add(value2); // 部門
        }

        if (!StringUtils.isEmpty(value3)) {
          paramData.add(value3); // 大分類
        }

        if (!StringUtils.isEmpty(value4)) {
          paramData.add(value4); // 中分類
        }

        if (StringUtils.isEmpty(value4)) {
          sqlcommand = DefineReport.ID_SQL_NEWDEPLICATECHEACK;
        } else {
          sqlcommand = DefineReport.ID_SQL_NEWDEPLICATECHEACK_C;
        }

      }
    }

    // レポート特有のDBチェック
    if (StringUtils.equals(key, "REP_CHK_DB")) {
      // 結果情報、もしくはエラー情報が返ってくる
      JSONObject result = new JSONObject();
      if (DefineReport.ID_PAGE_TG003.equals(outpage)) {
        // 3.1.1.2．全店特売（アンケート有）_店グループに入力店グループがすでに存在すると、エラー。
        // 3.1.1.5．店コードがすでに当催しのある店グループ（当店グループ以外）に属する場合、エラー。
        result = new ReportTG003Dao(jndiName).checkDbCount(outobj, obj);
      }
      if (DefineReport.ID_PAGE_TG040.equals(outpage)) {
        // 2.3.1.1．コピー対象グループに属する全店舗をチェックし、廃店（店舗基本.店運用区分=9）がある場合はワーニングを表示する。
        result = new ReportTG040Dao(jndiName).checkDbCount(outobj, obj);
      }

      if (!result.isEmpty()) {
        new EasyToJSONServlet();
        sqlcommand = EasyToJSONServlet.convertJsonobjToSql(result);
      }
    }

    // チェックデジット妥当性
    if (StringUtils.equals(key, "CHK_DGT")) {

      // 添付資料（MD03100901）の商品コードのチェックデジット計算
      if (outobj.equals(DefineReport.InpText.SHNCD.getObj())) {
        // 商品コード情報、もしくはエラー情報が返ってくる
        JSONObject result = NumberingUtility.calcCheckdigitSHNCD(lusr, value);
        new EasyToJSONServlet();
        sqlcommand = EasyToJSONServlet.convertJsonobjToSql(result);
      }

      // 添付資料（MD03111001）のソースコードのチェックデジット計算
      if (outobj.equals(DefineReport.InpText.SRCCD.getObj())) {
        // ソースコード情報、もしくはエラー情報が返ってくる
        JSONObject result = NumberingUtility.calcCheckdigitSRCCD(lusr, value, obj.optString("SOURCEKBN"));
        new EasyToJSONServlet();
        sqlcommand = EasyToJSONServlet.convertJsonobjToSql(result);
      }

      // ITFコードのチェックデジット計算
      if (outobj.equals(DefineReport.InpText.ITFCD.getObj())) {
        // ITFコード情報、もしくはエラー情報が返ってくる
        JSONObject result = NumberingUtility.calcCheckdigitITFCD(lusr, value);
        new EasyToJSONServlet();
        sqlcommand = EasyToJSONServlet.convertJsonobjToSql(result);
      }
    }


    // チェック用にレコード取得
    if (StringUtils.equals(key, "SEL")) {
      // 商品コード : 登録限度数
      if (outobj.equals(DefineReport.InpText.SSIRCD.getObj())) {
        paramData.add(value);
        sqlcommand = DefineReport.ID_SQL_SIR_;
      }

      // 部門情報 :
      if (outobj.equals(DefineReport.InpText.BMNCD.getObj())) {
        paramData.add(StringUtils.defaultIfEmpty(obj.optString("BMNCD"), "-1"));
        paramData.add(StringUtils.defaultIfEmpty(obj.optString("DAICD"), "-1"));
        paramData.add(StringUtils.defaultIfEmpty(obj.optString("CHUCD"), "-1"));
        paramData.add(StringUtils.defaultIfEmpty(obj.optString("SHOCD"), "-1"));
        paramData.add(StringUtils.defaultIfEmpty(obj.optString("SSHOCD"), "-1"));
        sqlcommand = StringUtils.remove(DefineReport.ID_SQL_BMN_CHK, "@");
      }
      if (outobj.equals(DefineReport.InpText.YOT_BMNCD.getObj())) {
        paramData.add(StringUtils.defaultIfEmpty(obj.optString("BMNCD"), "-1"));
        paramData.add(StringUtils.defaultIfEmpty(obj.optString("DAICD"), "-1"));
        paramData.add(StringUtils.defaultIfEmpty(obj.optString("CHUCD"), "-1"));
        paramData.add(StringUtils.defaultIfEmpty(obj.optString("SHOCD"), "-1"));
        paramData.add(StringUtils.defaultIfEmpty(obj.optString("SSHOCD"), "-1"));
        sqlcommand = StringUtils.replace(DefineReport.ID_SQL_BMN_CHK, "@", "_YOT");
      }
      if (outobj.equals(DefineReport.InpText.URI_BMNCD.getObj())) {
        paramData.add(StringUtils.defaultIfEmpty(obj.optString("BMNCD"), "-1"));
        paramData.add(StringUtils.defaultIfEmpty(obj.optString("DAICD"), "-1"));
        paramData.add(StringUtils.defaultIfEmpty(obj.optString("CHUCD"), "-1"));
        paramData.add(StringUtils.defaultIfEmpty(obj.optString("SHOCD"), "-1"));
        paramData.add(StringUtils.defaultIfEmpty(obj.optString("SSHOCD"), "-1"));
        sqlcommand = StringUtils.replace(DefineReport.ID_SQL_BMN_CHK, "@", "_URI");
      }
      if (outobj.equals(DefineReport.InpText.NEZ_BMNCD.getObj())) {
        paramData.add(StringUtils.defaultIfEmpty(obj.optString("BMNCD"), "-1"));
        paramData.add(StringUtils.defaultIfEmpty(obj.optString("DAICD"), "-1"));
        paramData.add(StringUtils.defaultIfEmpty(obj.optString("CHUCD"), "-1"));
        paramData.add(StringUtils.defaultIfEmpty(obj.optString("SHOCD"), "-1"));
        sqlcommand = StringUtils.replace(DefineReport.ID_SQL_KRYO_CHK, "@", "_NEZ");
      }
    }
    // 店舗休日マスタ用
    if (outobj.equals(DefineReport.InpText.TENKYUDT.getObj()) && DefineReport.ID_PAGE_X142.equals(outpage)) {
      // 店舗コードを取得
      String tbl = "INAMS.MSTTENKYU";
      sqlcommand = DefineReport.ID_SQL_CHK_TBL_MULTI.replace("@T", tbl);
      paramData.add(obj.optString("TENCD"));
      paramData.add(obj.optString("TENKYUDT"));
      sqlcommand += " TENCD = ? and TENKYUDT = ? ";
      sqlcommand += " and COALESCE(UPDKBN, 0) = 0";
    }
    // 店舗部門マスタ用
    if (outobj.equals("MSTTENBMN") && DefineReport.ID_PAGE_X122.equals(outpage)) {
      // 店舗コードを取得
      paramData = new ArrayList<>();
      String tbl = "INAMS.MSTTENBMN";
      sqlcommand = DefineReport.ID_SQL_CHK_TBL_MULTI.replace("@T", tbl);
      paramData.add(obj.optString("TENCD"));
      paramData.add(obj.optString("BMNCD"));
      sqlcommand += " TENCD = ? and BMNCD = ?";
      sqlcommand += " and COALESCE(UPDKBN, 0) = 0";
    }

    // 事前発注_発注明細wk管理
    if (outobj.equals(DefineReport.InpText.LSTNO.getObj()) && DefineReport.ID_PAGE_TJ014.equals(outpage)) {
      String tbl = "INATK.TOKTJ_WK_MNG";
      sqlcommand = DefineReport.ID_SQL_CHK_TBL_MULTI.replace("@T", tbl);
      User userInfo = (User) session.getAttribute(Consts.STR_SES_LOGINUSER);
      String tencd = userInfo.getTenpo();
      paramData.add(tencd);
      paramData.add(obj.optString("BMNCD"));
      sqlcommand += " TENCD = ? and BMNCD = ?";
    }

    // ランク名称取得
    if (outobj.equals(DefineReport.InpText.RANKKN.getObj())) {
      String tbl = "INATK.TOKRANK";
      sqlcommand = DefineReport.ID_SQL_CHK_TBL_MULTI.replace("@T", tbl);
      paramData.add(obj.optString("BMNCD"));
      paramData.add(obj.optString("RANKNO"));
      sqlcommand += " BMNCD = ? and RANKNO = ? and RANKKN IS NOT NULL and RANKKN <> ''";
    }

    // 臨時ランクNo.
    if (outobj.equals(DefineReport.InpText.RANKNO.getObj() + "_ARR") && value.length() > 1) {

      String bmncd = value.split(",")[0];
      String rankno = value.split(",")[1];
      String moyskbn = StringUtils.substring(value.split(",")[2], 0, 1);
      String moysstdt = StringUtils.substring(value.split(",")[2], 1, 7);
      String moysrban = StringUtils.substring(value.split(",")[2], 7, 10);
      String kanrino = value.split(",").length > 3 ? value.split(",")[3] : "";

      sqlcommand = DefineReport.ID_SQL_RANKARR_SELECT;

      if (StringUtils.isEmpty(kanrino)) {
        // 臨時判定
        if (Integer.valueOf(rankno) >= 900) {
          sqlcommand += DefineReport.ID_SQL_RANKEX_FROM;
          paramData.add(bmncd);
          paramData.add(rankno);
          paramData.add(moyskbn);
          paramData.add(moysstdt);
          paramData.add(moysrban);
          sqlcommand += DefineReport.ID_SQL_RANKEX_WHERE + " and COALESCE(UPDKBN, 0) = 0";
        } else {
          sqlcommand += DefineReport.ID_SQL_RANK_FROM;
          paramData.add(bmncd);
          paramData.add(rankno);
          sqlcommand += DefineReport.ID_SQL_RANK_WHERE + " and COALESCE(UPDKBN, 0) = 0";
        }
      } else {
        // アン有 or 無
        if (Integer.valueOf(moysrban) >= 50) {
          sqlcommand += DefineReport.ID_SQL_RANK_FROM_TG;
        } else {
          sqlcommand += DefineReport.ID_SQL_RANK_FROM_SP;
        }
        paramData.add(moyskbn);
        paramData.add(moysstdt);
        paramData.add(moysrban);
        paramData.add(bmncd);
        paramData.add(kanrino);
        sqlcommand += DefineReport.ID_SQL_RANK_WHERE_SHN;
      }
    }

    if (outobj.equals(DefineReport.InpText.BMNCD.getObj()) && DefineReport.ID_PAGE_TM002.equals(outpage)) {
      int count = 0;
      String moyskbn = obj.optString("MOYSKBN");
      String moysstdt = obj.optString("MOYSSTDT");
      String moysrban = obj.optString("MOYSRBAN");


      paramData.add(moyskbn);
      paramData.add(moysstdt);
      paramData.add(moysrban);

      for (int i = 0; i < 20; i++) {
        if (!"".equals(obj.optString("BMNCD_" + i))) {
          paramData.add(obj.optString("BMNCD_" + i));
          count++;
        }
      }
      if (count > 0 && !moyskbn.isEmpty() && !moysstdt.isEmpty() && !moysrban.isEmpty()) {
        sqlcommand = new ReportTM002Dao(jndiName).createCommandcheck(count);
      }
    }

    if (outobj.equals("MOYOKBN") || outobj.equals("MOYOKBN2") || outobj.equals("MOYOKBN3") || outobj.equals("MOYOKBN5") || outobj.equals("MOYOKBN7") || outobj.equals("MOYOKBN8")
        || outobj.equals("MOYOKBN9")) {
      paramData.add(obj.optString("MOYSKBN"));
      paramData.add(obj.optString("MOYSSTDT"));
      paramData.add(obj.optString("MOYSRBAN"));

      sqlcommand = new ReportTM004Dao(jndiName).createCommandcheck(outobj);
    }

    // 事前打ち出し_商品納入日検索
    if (outobj.equals(DefineReport.InpText.SHNCD.getObj()) && StringUtils.equals(key, "SEL")
        && (DefineReport.ID_PAGE_JU012.equals(outpage) || DefineReport.ID_PAGE_JU013.equals(outpage) || DefineReport.ID_PAGE_JU032.equals(outpage) || DefineReport.ID_PAGE_JU033.equals(outpage))) {
      String JNDIname = Defines.STR_JNDI_DS;
      User userInfo = (User) session.getAttribute(Consts.STR_SES_LOGINUSER);
      Iterator keys = obj.keys();
      HashMap hsmap = new HashMap<String, String>();

      while (keys.hasNext()) {
        key = (String) keys.next();
        hsmap.put(key, obj.get(key));
      }

      hsmap.put("callpage", outpage);

      // 発注数量
      sqlcommand = new ReportJU012Dao(JNDIname).createCommandSub(hsmap, userInfo);
    }

    if (outobj.equals(DefineReport.InpText.TAISYOTEN.getObj())) {
      String JNDIname = Defines.STR_JNDI_DS;

      String bmnCd = obj.optString("BMNCD");
      String moysKbn = obj.optString("MOYSKBN");
      String moysStDt = obj.optString("MOYSSTDT");
      String moysRban = obj.optString("MOYSRBAN");
      String rankNoAdd = obj.optString("RANKNOADD");
      String rankNoDel = obj.optString("RANKNODEL");
      JSONArray tenCdAdds = JSONArray.fromObject(obj.get("TENCDADDS"));
      JSONArray tenCdDels = JSONArray.fromObject(obj.get("TENCDDELS"));

      // 対象店を取得
      Set<Integer> tencds = new ReportBM015Dao(JNDIname).getTenCdAdd(bmnCd // 部門コード
          , moysKbn // 催し区分
          , moysStDt // 催し開始日
          , moysRban // 催し連番
          , rankNoAdd // 対象ランク№
          , rankNoDel // 除外ランク№
          , tenCdAdds // 対象店
          , tenCdDels // 除外店
      );

      sqlcommand = "SELECT " + tencds.size() + " AS VALUE FROM (SELECT 1 AS DUMMY) DUMMY";
    }

    if (outobj.equals(DefineReport.InpText.MOYSCD.getObj()) && StringUtils.isEmpty(key)) {
      String JNDIname = Defines.STR_JNDI_DS;
      JSONArray selectCnt = new ReportJU032Dao(JNDIname).getMoysCdChk(obj);

      sqlcommand = "SELECT " + selectCnt.size() + " AS VALUE FROM (SELECT 1 AS DUMMY) DUMMY";
    }

    // 禁止文字が含まれる場合
    if (StringUtils.equals(key, "PROHIBITED") && !StringUtils.isEmpty(value)) {

      int len = value.length();
      String newValue = len != 0 ? "" : value;
      ArrayList<String> dummy = new ArrayList<>();

      for (int i = 0; i < len; i++) {
        sqlcommand = "select * from (values ROW('" + value.substring(i, (i + 1)) + "')) as T1(" + DefineReport.VAL + ")";
        try {
          new EasyToJSONServlet();
          json = EasyToJSONServlet.selectJSON(sqlcommand, dummy, jndiName, datatype);
          String selValue = JSONArray.fromObject(JSONObject.fromObject(json).get("rows").toString()).getJSONObject(0).get("VALUE").toString();

          if (!StringUtils.isEmpty(selValue) && selValue.equals(value.substring(i, (i + 1)))) {
            newValue += selValue;
          }
        } catch (Exception e) {
        }
      }
      sqlcommand = "select * from (values ROW('" + newValue + "')) as T1(" + DefineReport.VAL + ")";
      paramData.clear();
    }

    // SQL構文の実行（コマンド指定あり）
    if (!"".equals(sqlcommand)) {
      try {
        new EasyToJSONServlet();
        json = EasyToJSONServlet.selectJSON(sqlcommand, paramData, jndiName, datatype);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return json;
  }
}
