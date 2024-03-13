/**
 *
 */
package common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import authentication.bean.User;
import authentication.defines.Consts;
import common.DefineReport.Values;
import dao.ReportBM015Dao;
import dao.ReportBW002Dao;
import dao.ReportSO003Dao;
import dao.ReportTG016Dao;
import dao.ReportTG020Dao;
import dao.Reportx092Dao;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import servlet.EasyToJSONServlet;

/**
 * @author Eatone
 *
 */
public class GetSqlCommandChange {

  public GetSqlCommandChange() {
    super();
  }

  /**
   *
   * EasyToJSONServletのchange処理を記載
   *
   */
  public static String getSqlcommand(HttpServletRequest request, HashMap<String, String> maps, String datatype, String jndiName) throws Exception {

    /** 画面初期化処理 */
    // セッション情報取得
    User lusr = (User) request.getSession().getAttribute(Consts.STR_SES_LOGINUSER);

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

    // メーカー
    if (outobj.equals(DefineReport.InpText.MAKERCD.getObj()) && !obj.containsKey("KEY")) {
      if (NumberUtils.isNumber(value)) {
        paramData.add(value);
        sqlcommand = DefineReport.ID_SQL_MAKER + " where MAKERCD = ?";
      }
    }

    // 催しコード
    if (outobj.equals(DefineReport.Select.MOYSCD.getObj())) {
      if ("-1".equals(value)) {
        paramData.add(null);
        paramData.add(null);
        paramData.add(null);
        sqlcommand = DefineReport.ID_SQL_MOYO;
      } else {
        String[] moyocd = value.split("-", -1);
        if (moyocd.length >= 3) {
          String moysKbn = moyocd[0];
          String moysStdt = moyocd[1];
          String moysRban = moyocd[2];

          if (StringUtils.isNotEmpty(moysKbn) && StringUtils.isNotEmpty(moysStdt) && StringUtils.isNotEmpty(moysRban)) {

            paramData.add(moysKbn);
            paramData.add(moysStdt);
            paramData.add(moysRban);
            sqlcommand = DefineReport.ID_SQL_MOYO;
          }
        }
      }
    }

    // 店舗名
    if (outobj.equals(DefineReport.InpText.TENCD.getObj()) || outobj.equals(DefineReport.InpText.TENCD.getObj() + "leader") || outobj.equals(DefineReport.InpText.CENTERCD.getObj())
        || outobj.equals(DefineReport.InpText.YCENTERCD.getObj())) {

      if (StringUtils.isEmpty(value)) {
        sqlcommand = DefineReport.ID_SQL_TEN + " and TENCD = null";
      } else {
        sqlcommand = DefineReport.ID_SQL_TEN + " and TENCD = ?";
        paramData.add(value);
      }
    }

    // 仕入先
    if (outobj.equals(DefineReport.InpText.SSIRCD.getObj()) || outobj.equals(DefineReport.InpText.SIRCD.getObj()) || outobj.equals(DefineReport.InpText.DSIRCD.getObj())
        || outobj.equals(DefineReport.InpText.RSIRCD.getObj()) || outobj.equals(DefineReport.InpText.DF_RSIRCD.getObj())) {
      if (NumberUtils.isNumber(value)) {
        paramData.add(value);
        sqlcommand = DefineReport.ID_SQL_SIR + DefineReport.ID_SQL_CMN_WHERE + " and  SIRCD = ?";
      }
    }

    // 配送パターン
    if (outobj.equals(DefineReport.InpText.HSPTN.getObj())) {
      if (NumberUtils.isNumber(value)) {
        paramData.add(value);
        sqlcommand = DefineReport.ID_SQL_HSPTN + " where UPDKBN = " + DefineReport.ValUpdkbn.NML.getVal() + " and HSPTN = ?";
      }
    }

    // 配送グループ
    if (outobj.equals(DefineReport.InpText.HSGPCD.getObj())) {
      String areakbn = obj.optString("AREAKBN");
      if (!StringUtils.isEmpty(areakbn) && NumberUtils.isNumber(value)) {
        sqlcommand = DefineReport.ID_SQL_HSGP + " and HSGPCD = ?";
        paramData.add(areakbn);
        paramData.add(value);
      }
    }

    // リードタイムパターン
    if (outobj.equals(DefineReport.Select.READTMPTN.getObj())) {
      if (NumberUtils.isNumber(value)) {
        paramData.add(value);
        sqlcommand = DefineReport.ID_SQL_READTMPTN2 + DefineReport.ID_SQL_READTMPTN_WHERE;
      }
    }

    // 一括伝票フラグ
    if (outobj.equals(DefineReport.InpText.RG_IDENFLG.getObj())) {
      if (NumberUtils.isNumber(value)) {
        paramData.add(Integer.toString(DefineReport.MeisyoSelect.KBN107.getCd()));
        paramData.add(value);
        sqlcommand = DefineReport.ID_SQL_MEISYO + DefineReport.ID_SQL_MEISYO_CD_WHERE + DefineReport.ID_SQL_MEISYO_FOOTER;
      }
    }

    // 店グループ
    if (StringUtils.startsWith(outobj, DefineReport.InpText.TENGPCD.getObj())) {
      // 配送店グループコード変更時
      if (DefineReport.ID_PAGE_X172.equals(outpage)) {
        String txt_hsgpcd = obj.optString("HSGPCD");
        if (!StringUtils.isEmpty(txt_hsgpcd) && NumberUtils.isNumber(value)) {
          sqlcommand = DefineReport.ID_SQL_TEN_MR002;
          paramData.add(txt_hsgpcd);
          paramData.add(value);
        }
      } else if (DefineReport.ID_PAGE_X152.equals(outpage)) {
        String txt_ehsptn = obj.optString("EHSPTN");
        if (!StringUtils.isEmpty(txt_ehsptn) && NumberUtils.isNumber(value)) {
          sqlcommand = DefineReport.ID_SQL_TEN_SI002;
          paramData.add(txt_ehsptn);
          paramData.add(value);
        }
      } else {
        String gpkbn = obj.optString("GPKBN");
        String areakbn = obj.optString("AREAKBN");
        String bmncd = obj.optString("BMNCD");
        if (StringUtils.isNotEmpty(gpkbn) && NumberUtils.isNumber(areakbn) && NumberUtils.isNumber(bmncd)) {
          // ※店別異部門の店グループ情報は、商品店グループでは、売価グループを参照する
          if (DefineReport.ValGpkbn.TBMN.getVal().equals(gpkbn)) {
            gpkbn = DefineReport.ValGpkbn.BAIKA.getVal();
          }
          paramData.add(value);
          paramData.add(gpkbn);
          paramData.add(areakbn);
          paramData.add(bmncd);
          sqlcommand = DefineReport.ID_SQL_TENGP;
        }
      }
    }

    // 部門
    if (outobj.equals(DefineReport.InpText.BMNCD.getObj()) || outobj.equals(DefineReport.InpText.YOT_BMNCD.getObj()) || outobj.equals(DefineReport.InpText.URI_BMNCD.getObj())) {
      if (NumberUtils.isNumber(value)) {
        paramData.add(value);
        String sqlWhere = DefineReport.ID_SQL_CMN_WHERE + DefineReport.ID_SQL_BMN_BUMON_WHERE;
        sqlcommand = DefineReport.ID_SQL_BUMON_C + sqlWhere + DefineReport.ID_SQL_BUMON_FOOTER_C;
        if (outpage.equals("Out_ReportTM005")) {
          sqlcommand = "select right('0'||BMNCD,2) as F2, right('0'||BMNCD,2)||'-'||rtrim(rtrim(BMNKN), '　') as TEXT, rtrim(rtrim(BMNKN), '　') as F3, * from INAMS.MSTBMN " + sqlWhere + " order by F2";
        }
      }
    }

    // 大分類
    if (outobj.equals(DefineReport.InpText.DAICD.getObj()) || outobj.equals(DefineReport.InpText.YOT_DAICD.getObj()) || outobj.equals(DefineReport.InpText.URI_DAICD.getObj())) {
      String bmncd = obj.optString("bmncd");

      if (NumberUtils.isNumber(value) && NumberUtils.isNumber(bmncd)) {
        paramData.add(bmncd);
        paramData.add(value);
        String sqlWhere = DefineReport.ID_SQL_CMN_WHERE + DefineReport.ID_SQL_BMN_BUMON_WHERE + DefineReport.ID_SQL_BMN_DAI_WHERE;
        if (outobj.equals(DefineReport.InpText.YOT_DAICD.getObj())) {
          sqlcommand = DefineReport.ID_SQL_DAI_BUN_YOT + sqlWhere + DefineReport.ID_SQL_DAI_BUN_FOOTER;
        } else if (outobj.equals(DefineReport.InpText.URI_DAICD.getObj())) {
          sqlcommand = DefineReport.ID_SQL_DAI_BUN_URI + sqlWhere + DefineReport.ID_SQL_DAI_BUN_FOOTER;
        } else {
          sqlcommand = DefineReport.ID_SQL_DAI_BUN + sqlWhere + DefineReport.ID_SQL_DAI_BUN_FOOTER;
        }
      }
    }

    // 中分類
    if (outobj.equals(DefineReport.InpText.CHUCD.getObj()) || outobj.equals(DefineReport.InpText.YOT_CHUCD.getObj()) || outobj.equals(DefineReport.InpText.URI_CHUCD.getObj())) {
      String bmncd = obj.optString("bmncd");
      String daicd = obj.optString("daicd");

      if (NumberUtils.isNumber(value) && NumberUtils.isNumber(bmncd) && NumberUtils.isNumber(daicd)) {
        paramData.add(bmncd);
        paramData.add(daicd);
        paramData.add(value);
        String sqlWhere = DefineReport.ID_SQL_CMN_WHERE + DefineReport.ID_SQL_BMN_BUMON_WHERE + DefineReport.ID_SQL_BMN_DAI_WHERE + DefineReport.ID_SQL_BMN_CHU_WHERE;
        if (outobj.equals(DefineReport.InpText.YOT_CHUCD.getObj())) {
          sqlcommand = DefineReport.ID_SQL_CHU_BUN_YOT + sqlWhere + DefineReport.ID_SQL_CHU_BUN_FOOTER;
        } else if (outobj.equals(DefineReport.InpText.URI_CHUCD.getObj())) {
          sqlcommand = DefineReport.ID_SQL_CHU_BUN_URI + sqlWhere + DefineReport.ID_SQL_CHU_BUN_FOOTER;
        } else {
          sqlcommand = DefineReport.ID_SQL_CHU_BUN + sqlWhere + DefineReport.ID_SQL_CHU_BUN_FOOTER;
        }
      }
    }

    // 小分類
    if (outobj.equals(DefineReport.InpText.SHOCD.getObj()) || outobj.equals(DefineReport.InpText.YOT_SHOCD.getObj()) || outobj.equals(DefineReport.InpText.URI_SHOCD.getObj())) {
      String bmncd = obj.optString("bmncd");
      String daicd = obj.optString("daicd");
      String chucd = obj.optString("chucd");

      if (NumberUtils.isNumber(value) && NumberUtils.isNumber(bmncd) && NumberUtils.isNumber(daicd) && NumberUtils.isNumber(chucd)) {
        paramData.add(bmncd);
        paramData.add(daicd);
        paramData.add(chucd);
        paramData.add(value);
        String sqlWhere =
            DefineReport.ID_SQL_CMN_WHERE + DefineReport.ID_SQL_BMN_BUMON_WHERE + DefineReport.ID_SQL_BMN_DAI_WHERE + DefineReport.ID_SQL_BMN_CHU_WHERE + DefineReport.ID_SQL_BMN_SHO_WHERE;
        if (outobj.equals(DefineReport.InpText.YOT_SHOCD.getObj())) {
          sqlcommand = DefineReport.ID_SQL_SHO_BUN_YOT + sqlWhere + DefineReport.ID_SQL_SHO_BUN_FOOTER;
        } else if (outobj.equals(DefineReport.InpText.URI_SHOCD.getObj())) {
          sqlcommand = DefineReport.ID_SQL_SHO_BUN_URI + sqlWhere + DefineReport.ID_SQL_SHO_BUN_FOOTER;
        } else {
          sqlcommand = DefineReport.ID_SQL_SHO_BUN + sqlWhere + DefineReport.ID_SQL_SHO_BUN_FOOTER;
        }
      }
    }

    // 小小分類
    if (outobj.equals(DefineReport.InpText.SSHOCD.getObj())) {
      String bmncd = obj.optString("bmncd");
      String daicd = obj.optString("daicd");
      String chucd = obj.optString("chucd");
      String shocd = obj.optString("shocd");

      if (NumberUtils.isNumber(value) && NumberUtils.isNumber(bmncd) && NumberUtils.isNumber(daicd) && NumberUtils.isNumber(chucd)) {
        paramData.add(bmncd);
        paramData.add(daicd);
        paramData.add(chucd);
        paramData.add(shocd);
        paramData.add(value);

        String sqlWhere = DefineReport.ID_SQL_CMN_WHERE + DefineReport.ID_SQL_BMN_BUMON_WHERE + DefineReport.ID_SQL_BMN_DAI_WHERE + DefineReport.ID_SQL_BMN_CHU_WHERE
            + DefineReport.ID_SQL_BMN_SHO_WHERE + DefineReport.ID_SQL_BMN_SSHO_WHERE;
        sqlcommand = DefineReport.ID_SQL_SSHO_BUN + sqlWhere + DefineReport.ID_SQL_SSHO_BUN_FOOTER;
      }
    }

    // 検索テキストボックス(添加物絞り込み用)
    if (outobj.equals(DefineReport.InpText.KENSAKU.getObj())) {
      paramData.add(Integer.toString(DefineReport.MeisyoSelect.KBN138.getCd()));
      paramData.add('%' + value + '%');
      sqlcommand = DefineReport.ID_SQL_MEISYO + DefineReport.ID_SQL_MEISYO_CD_WHERE2 + DefineReport.ID_SQL_MEISYO_FOOTER;
    }

    // 催し連番
    if (outobj.equals(DefineReport.InpText.MOYSRBAN.getObj())) {
      // 催し_デフォルト設定.デフォルト_ちらしのみ=1の部門をすべて取得
      sqlcommand = DefineReport.ID_SQL_TOKMOYDEF + " and DCHIRASKBN = " + Values.ON.getVal() + DefineReport.ID_SQL_CMN_FOOTER;
    }


    // 商品コード変更時
    if (StringUtils.startsWith(outobj, DefineReport.InpText.SHNCD.getObj()) && !obj.containsKey("KEY")) {
      if (StringUtils.isNotEmpty(value)) {
        if (DefineReport.ID_PAGE_TG016.equals(outpage)) {
          String szMoysKbn = obj.optString("MOYSKBN");
          String szMoysstdt = obj.optString("MOYSSTDT");
          if (NumberUtils.isNumber(szMoysKbn) && NumberUtils.isNumber(szMoysstdt)) {
            paramData.add(value);
            sqlcommand = new ReportTG016Dao(jndiName).createSqlSelMSTSHN(szMoysKbn, szMoysstdt, value);
          }
        } else if (DefineReport.ID_PAGE_BW002.equals(outpage) || DefineReport.ID_PAGE_BW004.equals(outpage)) {
          paramData.add(obj.optString("HBSTDT"));
          paramData.add(obj.optString("HBSTDT"));
          paramData.add(obj.optString("HBSTDT"));
          paramData.add(obj.optString("HBSTDT"));
          paramData.add(value);
          sqlcommand = DefineReport.ID_SQL_TOKRS;
        } else if (DefineReport.ID_PAGE_SO003.equals(outpage)) {
          paramData.add(value);
          sqlcommand = ReportSO003Dao.ID_SQL_SHNKN_SO003;

        } else if (DefineReport.ID_PAGE_JU032.equals(outpage) || DefineReport.ID_PAGE_JU033.equals(outpage) || DefineReport.ID_PAGE_JU012.equals(outpage)
            || DefineReport.ID_PAGE_JU013.equals(outpage)) {

          String shnkbn = obj.optString("shnkbn");
          String stdt = obj.optString("stdt");

          paramData.add(shnkbn);
          paramData.add(shnkbn);
          paramData.add(stdt);
          paramData.add(stdt);
          paramData.add(stdt);
          paramData.add(stdt);
          paramData.add(value);
          sqlcommand = DefineReport.ID_SQL_SHNKN_TOK;
        } else if (DefineReport.ID_PAGE_BM006.equals(outpage)) {
          paramData.add(value);
          sqlcommand = DefineReport.ID_SQL_SHNKN_TOK2;
        } else if (DefineReport.ID_PAGE_X213.equals(outpage) || DefineReport.ID_PAGE_X214.equals(outpage) || DefineReport.ID_PAGE_X217.equals(outpage) || DefineReport.ID_PAGE_X218.equals(outpage)) {
          paramData.add(value);
          sqlcommand = DefineReport.ID_SQL_SHNKN_PC;
        } else if (DefineReport.ID_PAGE_X092.equals(outpage)) {
          if (outobj.equals(DefineReport.InpText.SHNCD.getObj() + "_M")) {
            if (NumberUtils.isNumber(value)) {
              paramData.add(value);
              sqlcommand = new Reportx092Dao(jndiName).createSqlGenryo(obj);
            }
          } else {
            paramData.add(value);
            sqlcommand = new Reportx092Dao(jndiName).createSqlSelMSTSHN(obj);
          }
        } else if (DefineReport.ID_PAGE_TG020.equals(outpage)) {
          paramData.add(value);
          sqlcommand = new ReportTG020Dao(jndiName).createSqlSelMSTSHN(obj);
        } else {
          paramData.add(value);
          sqlcommand = DefineReport.ID_SQL_SHNKN;
        }
      } else {
        if (DefineReport.ID_PAGE_TG016.equals(outpage)) {
          sqlcommand = new ReportTG016Dao(jndiName).createSqlSelMSTSHN("", "", "");
        }
      }
    }

    // ダミーコード
    if (outobj.equals(DefineReport.InpText.DUMMYCD.getObj())) {
      paramData.add(value);
      sqlcommand = DefineReport.ID_SQL_SHNKN;
    }

    // 売価変更
    if (StringUtils.startsWith(outobj, DefineReport.InpText.BAIKAAM.getObj()) && !obj.containsKey("KEY")) {
      paramData.add(obj.optString("BMNCD"));
      paramData.add(obj.optString("WARIBIKI"));
      paramData.add(obj.optString("SEISI"));
      paramData.add(obj.optString("DUMMYCD"));
      paramData.add(obj.optString("HBSTDT"));
      paramData.add(obj.optString("SHNCD"));
      sqlcommand = new ReportBW002Dao(jndiName).createSqlSelMSTSHN(obj);

    }

    // 添付資料（MD03100901）の商品コード付番機能→仮として付番し、Daoチェック時に仮押さえする
    if (outobj.equals(DefineReport.InpText.SHNCD.getObj()) && obj.containsKey("KEY")) {
      // if(StringUtils.endsWith(obj.optString("KEY"), "_ADD")){
      // 商品コード情報、もしくはエラー情報が返ってくる
      JSONObject result = NumberingUtility.execGetNewSHNCD(lusr, value, obj.optString("KETA"), obj.optString("BMNCD"));
      new EasyToJSONServlet();
      sqlcommand = EasyToJSONServlet.convertJsonobjToSql(result);
      // }else if(StringUtils.endsWith(obj.optString("KEY"), "_DEL")){
      // // 商品コード情報、もしくはエラー情報が返ってくる
      // JSONObject result = NumberingUtility.execReleaseNewSHNCD(lusr, value);
      // sqlcommand = this.convertJsonobjToSql(result);
      // }
    }

    // // 添付資料（MD03100902）の販売コード付番→Daoチェック時に付番、仮押さえする
    // if(outobj.equals(DefineReport.InpText.URICD.getObj())&&obj.containsKey("KEY")){
    // // TODO:IDがなんなのかいまいちわからない
    // // paramData.add(value); // ID
    // sqlcommand = DefineReport.ID_SQL_MD03100902;
    // }


    // 添付資料（MD03112501）のメーカーコードの取得方法
    if (outobj.equals(DefineReport.InpText.MAKERCD.getObj()) && obj.containsKey("KEY")) {
      String kbn = obj.optString("KBN");
      String bmncd = obj.optString("BMNCD");
      paramData.add(value);
      paramData.add(kbn);
      paramData.add(bmncd);
      sqlcommand = DefineReport.ID_SQL_MD03112501;
    }

    // 添付資料（MD03111301）の総額売価計算方法
    if ((outobj.equals(DefineReport.Text.RG_SOUBAIKA.getObj()) && obj.containsKey("KEY")) || (outobj.startsWith(DefineReport.Text.BG_SOUBAIKA.getObj()) && obj.containsKey("KEY"))) {
      String bmncd = obj.optString("BMNCD");
      String zeikbn = obj.optString(DefineReport.MeisyoSelect.KBN120.getObj());
      String zeirtkbn = obj.optString(DefineReport.Select.ZEIRTKBN.getObj());
      String zeirtkbn_old = obj.optString(DefineReport.Select.ZEIRTKBN_OLD.getObj());
      String zeirthenkodt = obj.optString(DefineReport.InpText.ZEIRTHENKODT.getObj());
      String tenbaikadt = obj.optString(DefineReport.InpText.TENBAIKADT.getObj());

      if (NumberUtils.isNumber(value) && NumberUtils.isNumber(zeikbn)) {
        paramData.add(bmncd);
        paramData.add(zeikbn);
        paramData.add(zeirtkbn);
        paramData.add(zeirtkbn_old);
        paramData.add(StringUtils.defaultIfEmpty(zeirthenkodt, "0"));
        paramData.add(StringUtils.defaultIfEmpty(tenbaikadt, "0"));
        paramData.add(value);
        // 予約情報を取得する場合
        if (DefineReport.ValTablekbn.YYK.getVal().equals(obj.optString("TABLEKBN"))) {
          sqlcommand = DefineReport.ID_SQL_MD03111301_RG_Y;
        } else {
          sqlcommand = DefineReport.ID_SQL_MD03111301_RG;
        }
      }
    }
    if (outobj.equals(DefineReport.Text.HS_SOUBAIKA.getObj()) && obj.containsKey("KEY")) {
      String bmncd = obj.optString("BMNCD");
      String zeikbn = obj.optString(DefineReport.MeisyoSelect.KBN120.getObj());
      String zeirtkbn = obj.optString(DefineReport.Select.ZEIRTKBN.getObj());
      String zeirtkbn_old = obj.optString(DefineReport.Select.ZEIRTKBN_OLD.getObj());
      String zeirthenkodt = obj.optString(DefineReport.InpText.ZEIRTHENKODT.getObj());
      String tenbaikadt = obj.optString(DefineReport.InpText.TENBAIKADT.getObj());

      if (NumberUtils.isNumber(value) && NumberUtils.isNumber(zeikbn)) {
        paramData.add(bmncd);
        paramData.add(zeikbn);
        paramData.add(zeirtkbn);
        paramData.add(zeirtkbn_old);
        paramData.add(StringUtils.defaultIfEmpty(zeirthenkodt, "0"));
        paramData.add(StringUtils.defaultIfEmpty(tenbaikadt, "0"));
        paramData.add(value);

        // 予約情報を取得する場合
        if (DefineReport.ValTablekbn.YYK.getVal().equals(obj.optString("TABLEKBN"))) {
          sqlcommand = DefineReport.ID_SQL_MD03111301_HS_Y;
        } else {
          sqlcommand = DefineReport.ID_SQL_MD03111301_HS;
        }
      }
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


    // 催しコード
    if (outobj.equals(DefineReport.InpText.MOYSCD.getObj())) {

      if (!StringUtils.isEmpty(value) && value.length() >= 8) {
        paramData.add(value.substring(0, 1)); // 催し区分
        paramData.add(value.substring(1, 7)); // 催し開始日
        paramData.add(value.substring(7)); // 催し連番

        sqlcommand = DefineReport.ID_SQL_MOYSCD2;
      }
    }

    if (outobj.equals(DefineReport.Text.TOK_SOUBAIKA.getObj())) {
      String shncd = obj.optString("SHNCD");
      String moysstdt = obj.optString("MOYSSTDT");
      String kijundt = common.CmnDate.dateFormat(common.CmnDate.convYYMMDD(moysstdt));
      if (NumberUtils.isNumber(value) && NumberUtils.isNumber(shncd) && NumberUtils.isNumber(kijundt)) {
        paramData.add(shncd);
        paramData.add(value);
        paramData.add(StringUtils.defaultIfEmpty(kijundt, "0"));
        sqlcommand = DefineReport.ID_SQL_TOKBAIKA_SOU.replaceAll("@SHNCD", "T1.SHNCD").replaceAll("@", "");
      }
    }
    if (outobj.equals(DefineReport.Text.TOK_HONBAIKA.getObj())) {
      String shncd = obj.optString("SHNCD");
      String moysstdt = obj.optString("MOYSSTDT");
      String kijundt = common.CmnDate.dateFormat(common.CmnDate.convYYMMDD(moysstdt));
      if (NumberUtils.isNumber(value) && NumberUtils.isNumber(shncd) && NumberUtils.isNumber(kijundt)) {
        paramData.add(shncd);
        paramData.add(value);
        paramData.add(StringUtils.defaultIfEmpty(kijundt, "0"));
        sqlcommand = DefineReport.ID_SQL_TOKBAIKA_HON.replaceAll("@SHNCD", "T1.SHNCD").replaceAll("@", "");
      }
    }

    // 対象店・徐外店ランク№ (部門_1_店配列_0_店配列の形を返却) ※対象ランク№、部門は必須
    if (!StringUtils.isEmpty(obj.optString("ID")) && (obj.optString("ID").equals(DefineReport.InpText.TAISYOTEN.getObj() + "_arr")
        || obj.optString("ID").equals(DefineReport.InpText.JYOGAITEN.getObj() + "_arr") || obj.optString("ID").equals(DefineReport.InpText.BMNCD.getObj() + "_arr"))) {
      // 入力値取得
      String bmnCd = obj.optString("BMNCD");
      String rankNoAdd = obj.optString("RANKNOADD");
      String rankNoDel = obj.optString("RANKNODEL");
      String moyscd = obj.optString("MOYSCD");

      // SQL作成用変数
      String sqlSelect = "";
      String sqlTableAdd = "";
      String sqlTableDel = "";

      // 部門・対象店ランク№は必須
      if (!StringUtils.isEmpty(bmnCd) && !StringUtils.isEmpty(rankNoAdd)) {

        // ランクマスタから店配列を取得
        sqlTableAdd = DefineReport.ID_SQL_RANK_SELECT + DefineReport.ID_SQL_RANK_FROM + DefineReport.ID_SQL_RANK_WHERE;

        // 部門コード
        paramData.add(bmnCd);

        // ランクNo.が900未満の場合参照テーブルを変更
        if (Integer.valueOf(rankNoAdd) >= 900) {

          // 催しコードが正しい形式で入力されていることを確認
          if (!StringUtils.isEmpty(moyscd) && moyscd.length() >= 8) {
            paramData.add(moyscd.substring(0, 1)); // 催し区分
            paramData.add(moyscd.substring(1, 7)); // 催し開始日
            paramData.add(moyscd.substring(7)); // 催し連番

            // 臨時ランクマスタから店配列を取得
            sqlTableAdd = DefineReport.ID_SQL_RANK_SELECT + DefineReport.ID_SQL_RANKEX_FROM + DefineReport.ID_SQL_RANKEX_WHERE;
          }
        }

        // ランクNo.
        paramData.add(rankNoAdd);

        sqlSelect = "SELECT ADD1.BMNCD||'_'||ADD1.TENRANK_ARR ";
        sqlTableAdd = " FROM (" + sqlTableAdd + ")  ADD1 ";

        // 除外店ランク№の入力がある場合は以下のテーブルを追加
        if (!StringUtils.isEmpty(rankNoDel)) {

          // ランクマスタから店配列を取得
          sqlTableDel = DefineReport.ID_SQL_RANK_SELECT + DefineReport.ID_SQL_RANK_FROM + DefineReport.ID_SQL_RANK_WHERE;

          // 部門コード
          paramData.add(bmnCd);

          // ランクNo.が900未満の場合参照テーブルを変更
          if (Integer.valueOf(rankNoDel) >= 900) {

            if (!StringUtils.isEmpty(moyscd) && moyscd.length() >= 8) {
              paramData.add(moyscd.substring(0, 1)); // 催し区分
              paramData.add(moyscd.substring(1, 7)); // 催し開始日
              paramData.add(moyscd.substring(7)); // 催し連番

              // 臨時ランクマスタから店配列を取得
              sqlTableDel = DefineReport.ID_SQL_RANK_SELECT + DefineReport.ID_SQL_RANKEX_FROM + DefineReport.ID_SQL_RANKEX_WHERE;
            }
          }

          // ランクNo.
          paramData.add(rankNoDel);

          sqlSelect += "||'_'||DEL.TENRANK_ARR ";
          sqlTableDel = ",(" + sqlTableDel + ") AS DEL WHERE ADD1.BMNCD=DEL.BMNCD ";
        }
      }

      if (!sqlSelect.equals("")) {
        sqlSelect += "AS " + DefineReport.VAL;
      }

      sqlcommand += sqlSelect + sqlTableAdd + sqlTableDel;
    }

    if (outobj.equals(DefineReport.InpText.RG_BAIKAAM.getObj())) {
      String shncd = obj.optString("shncd");
      String stdt = obj.optString("stdt");
      if (NumberUtils.isNumber(value) && NumberUtils.isNumber(shncd) && NumberUtils.isNumber(stdt)) {
        paramData = new ArrayList<>();
        paramData.add(stdt);
        paramData.add(stdt);
        paramData.add(stdt);
        paramData.add(stdt);
        paramData.add(shncd);
        sqlcommand = DefineReport.ID_SQL_TOKBAIKA_HON2;
      }
    }

    // 再計算ボタン
    if (outobj.equals(DefineReport.Button.CALC.getObj())) {

      Iterator keys = obj.keys();
      HashMap hsmap = new HashMap<String, String>();

      while (keys.hasNext()) {
        String key = (String) keys.next();
        hsmap.put(key, obj.get(key));
      }

      sqlcommand = new ReportBM015Dao(jndiName).getArrMap(hsmap);
    }

    // 発注数取得
    if (outobj.equals(DefineReport.InpText.TENHTSU_ARR.getObj())) {
      sqlcommand = new ReportTG020Dao(jndiName).createSqlGetHatsu(obj);
    }

    // SQL構文の実行（コマンド指定あり）
    if (!"".equals(sqlcommand)) {
      if (DefineReport.ID_DEBUG_MODE) {
        System.out.println(sqlcommand);
      }

      try {
        new EasyToJSONServlet();
        json = EasyToJSONServlet.selectJSON(sqlcommand, paramData, jndiName, datatype);
        // if (DefineReport.ID_DEBUG_MODE)
        // System.out.println("json:" + json);

      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return json;
  }
}
