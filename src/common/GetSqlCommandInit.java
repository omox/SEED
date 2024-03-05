/**
 *
 */
package common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import authentication.bean.User;
import authentication.defines.Consts;
import common.DefineReport.MeisyoSelect;
import dao.ReportBM015Dao;
import dao.ReportJU017Dao;
import dao.ReportJU033Dao;
import dao.ReportRP006Dao;
import dao.ReportST007Dao;
import dao.ReportST008Dao;
import dao.ReportST010Dao;
import dao.ReportST011Dao;
import dao.ReportST021Dao;
import dao.ReportTG003Dao;
import dao.ReportTG016Dao;
import dao.ReportTG040Dao;
import dao.ReportTJ002Dao;
import dao.ReportTJ003Dao;
import dao.ReportTJ006Dao;
import dao.ReportTJ007Dao;
import dao.ReportTM002Dao;
import dao.Reportx092Dao;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import servlet.EasyToJSONServlet;

/**
 * @author Eatone
 *
 */
public class GetSqlCommandInit {

  public GetSqlCommandInit() {
    super();
  }

  /**
   *
   * EasyToJSONServletのinit処理を記載
   *
   */
  public static String getSqlcommand(HttpServletRequest request, HashMap<String, String> maps, String datatype, String jndiName) throws Exception {

    /** 画面初期化処理 */
    // セッションの取得
    HttpSession session = request.getSession();

    // セッション情報取得
    User lusr = (User) request.getSession().getAttribute(Consts.STR_SES_LOGINUSER);

    // パラメータの確認
    String outpage = maps.get(DefineReport.ID_PARAM_PAGE) == null ? "" : maps.get(DefineReport.ID_PARAM_PAGE);
    String outobj = maps.get(DefineReport.ID_PARAM_OBJ) == null ? "" : maps.get(DefineReport.ID_PARAM_OBJ);
    String outjson = maps.get(DefineReport.ID_PARAM_JSON) == null ? "" : maps.get(DefineReport.ID_PARAM_JSON);

    // 検索キー
    String nameWith = maps.get(DefineReport.ID_SEARCHJSON_PARAM_NAMEWITH) == null ? "" : maps.get(DefineReport.ID_SEARCHJSON_PARAM_NAMEWITH);

    // JSONパラメータの解析
    JSONArray map = new JSONArray();
    if (!"".equals(outjson)) {
      map = (JSONArray) JSONSerializer.toJSON(outjson);
    }

    // 配列準備
    ArrayList<String> paramData = new ArrayList<String>();
    String sqlcommand = "";
    String json = "";

    // メッセージ一覧
    if (outobj.equals(DefineReport.Select.MSG_LIST.getObj())) {
      sqlcommand = DefineReport.ID_SQL_MSG;

      if (DefineReport.ID_PAGE_X002.equals(outpage)) {
        paramData.add("E%");
        paramData.add("I%");
        paramData.add("W%");
        sqlcommand += DefineReport.ID_SQL_MSG_WHERE2;
      } /*
         * else if (DefineReport.ID_PAGE_X251.equals(outpage)) { paramData.add("E%"); paramData.add("I%");
         * paramData.add("W%"); sqlcommand += DefineReport.ID_SQL_MSG_WHERE2; }
         */
    }

    // 禁止文字一覧
    if (outobj.equals(DefineReport.Select.PROHIBITED_LIST.getObj())) {
      sqlcommand = DefineReport.ID_SQL_MEISYO_PROHIBITED;
    }

    // 年月FROM
    if (outobj.equals(DefineReport.Select.YM_F.getObj()) || outobj.equals(DefineReport.Select.YM_F2.getObj())) {
      sqlcommand = DefineReport.ID_SQL_KIKAN_YM_FROM;
    }

    // 年月TO
    if (outobj.equals(DefineReport.Select.YM_T.getObj()) || outobj.equals(DefineReport.Select.YM_T2.getObj())) {
      sqlcommand = DefineReport.ID_SQL_KIKAN_YM_TO;
    }

    // 催しコード
    if (outobj.equals(DefineReport.Select.MOYSCD.getObj())) {
      JSONObject obj = (JSONObject) map.get(0);
      String moyoosi = obj.optString("DUMMY");
      sqlcommand = DefineReport.ID_SQL_MOYSCD;
      if (!StringUtils.isEmpty(moyoosi)) {
        sqlcommand = DefineReport.ID_SQL_MOYSCD;
        String[] moyocd = moyoosi.split("-", -1);
        sqlcommand += " where T1.MOYSKBN = ? and T1.MOYSSTDT = ? and T1.MOYSRBAN = ? AND T1.UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal();
        paramData.add(moyocd[0]);
        paramData.add(moyocd[1]);
        paramData.add(moyocd[2]);
      } else {
        sqlcommand = "(" + DefineReport.ID_SQL_MOYSCD;
        sqlcommand += " where T1.MOYSKBN = 8 AND T1.UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal()
            + " and NOT EXISTS(SELECT T2.MOYSKBN FROM INATK.TOKQJU_MOY T2 WHERE T1.MOYSKBN=T2.MOYSKBN AND T1.MOYSSTDT=T2.MOYSSTDT AND T1.MOYSRBAN=T2.MOYSRBAN AND T2.UPDKBN="
            + DefineReport.ValUpdkbn.NML.getVal() + ") order by T1.MOYSKBN , T1.MOYSSTDT , T1.MOYSSTDT )";
        sqlcommand += DefineReport.ID_SQL_MOYSCD_HEAD;
      }
    }

    // 週№
    if (outobj.equals(DefineReport.Select.SHUNO.getObj())) {
      JSONObject obj = (JSONObject) map.get(0);

      String sqlHead = DefineReport.ID_SQL_SHUNO_HEAD;

      String prmC = "", prmT = "";
      if (DefineReport.ID_PAGE_TM002.equals(outpage)) {
        // prmC = ", right('0000' ||NVL(T1.SHUNO, T2.SHUNO), 4) as ID , nvl(T2.SHUNO||'', '') as FLG";
        prmT = " , SHORI T2 where T1.STARTDT >= T2.VALUE2 AND T1.ENDDT <= T2.VALUE3 ";
      }

      if (DefineReport.ID_PAGE_TG017.equals(outpage)) {
        sqlcommand = DefineReport.ID_SQL_CMN_WEEK + DefineReport.ID_SQL_TOKSHUNO + DefineReport.ID_SQL_CMN_FOOTER;
      } else if (DefineReport.ID_PAGE_TM002.equals(outpage)) {
        sqlcommand = DefineReport.ID_SQL_CMN_WEEK + ", SHORI as (" + DefineReport.ID_SQLSHORIDT3 + ") " + DefineReport.ID_SQL_SHUNO + DefineReport.ID_SQL_CMN_FOOTER;
      } else {
        sqlcommand = DefineReport.ID_SQL_CMN_WEEK + DefineReport.ID_SQL_SHUNO + DefineReport.ID_SQL_CMN_FOOTER + " desc";
      }

      if (!obj.containsKey("REQUIRED")) {
        if (DefineReport.ID_PAGE_TG017.equals(outpage)) {
          sqlcommand = DefineReport.ID_SQL_CMN_WEEK + "select * from (" + sqlHead + DefineReport.ID_SQL_TOKSHUNO + ") T order by VALUE";
        } else if (DefineReport.ID_PAGE_TM002.equals(outpage)) {
          sqlHead = DefineReport.ID_SQL_SHUNO_HEAD2;
          sqlcommand = DefineReport.ID_SQL_CMN_WEEK + ", SHORI as (" + DefineReport.ID_SQLSHORIDT3 + ") select * from (" + sqlHead + DefineReport.ID_SQL_SHUNO + ") T order by VALUE ";
        } else {
          sqlcommand = DefineReport.ID_SQL_CMN_WEEK + "select * from (" + sqlHead + DefineReport.ID_SQL_SHUNO + ") T order by replace(VALUE, '-1', '9999') desc";
        }
      }
      sqlcommand = sqlcommand.replace("@C", prmC).replace("@T", prmT);
    }

    // 週№(処理日付を基準日とした週、翌週、翌々週のデータ)
    if (outobj.equals(DefineReport.Select.SHUNOPERIOD.getObj())) {
      if (DefineReport.ID_PAGE_TR007.equals(outpage)) {
        sqlcommand = DefineReport.ID_SQL_SHUN_TR007;
      } else {
        sqlcommand = DefineReport.ID_SQL_SHUNO2;
      }
    }

    // 店舗
    if (outobj.equals(DefineReport.Select.TENPO.getObj())) {
      JSONObject obj = (JSONObject) map.get(0);
      if ("TenSelect".equals(outpage) && lusr != null && !StringUtils.isEmpty(lusr.getYobi9_())) {
        sqlcommand = "select right ('000' || T2.TENCD, 3) as value, right ('000' || T2.TENCD, 3) || '" + DefineReport.SEPARATOR
            + "' || NVL(RTRIM(RTRIM(T2.TENKN), '　'), '') as TEXT, RTRIM(RTRIM(T2.TENKN), '　') as TEXT2 FROM (values";
        for (int i = 0; i < lusr.getYobi9_().split(",").length; i++) {
          sqlcommand += "(?)";
          if (i + 1 != lusr.getYobi9_().split(",").length) {
            sqlcommand += ",";
          }
          paramData.add(lusr.getYobi9_().split(",")[i]);
        }
        sqlcommand += ") AS T1(TENCD) LEFT JOIN INAMS.MSTTEN T2 ON T1.TENCD=T2.TENCD ORDER BY VALUE";
      } else {
        String sqlWhere = "";
        if (!DefineReport.isPastPage(outpage)) {
          sqlWhere += DefineReport.ID_SQL_TEN_EXIST;
        }
        sqlWhere = StringUtils.replaceOnce(sqlWhere, "and", "where");

        sqlcommand += DefineReport.ID_SQL_TENPO_MDM;
      }

      if (!obj.containsKey("REQUIRED")) {
        sqlcommand = DefineReport.ID_SQL_TENPO_MDM_HEAD + sqlcommand;
      }
    }

    // 部門
    if (StringUtils.startsWith(outobj, DefineReport.Select.BUMON.getObj())) {
      JSONObject obj = (JSONObject) map.get(0);
      sqlcommand = DefineReport.ID_SQL_BUMON + DefineReport.ID_SQL_CMN_WHERE + DefineReport.ID_SQL_BUMON_FOOTER;
      if (!obj.containsKey("REQUIRED")) {
        if (DefineReport.ID_PAGE_TG008.equals(outpage)) {
          sqlcommand = DefineReport.ID_SQL_BUMON_HEAD3 + sqlcommand;
        } else if (!DefineReport.ID_PAGE_SA003.equals(outpage)) {
          sqlcommand = DefineReport.ID_SQL_BUMON_HEAD2 + sqlcommand;
        }
      } else if (DefineReport.ID_PAGE_X001.equals(outpage) || DefineReport.ID_PAGE_X231.equals(outpage) || DefineReport.ID_PAGE_X244.equals(outpage) || DefineReport.ID_PAGE_X246.equals(outpage)
          || DefineReport.ID_PAGE_X249.equals(outpage) || DefineReport.ID_PAGE_X250.equals(outpage) || DefineReport.ID_PAGE_X261.equals(outpage) || DefineReport.ID_PAGE_X280.equals(outpage)) {
        sqlcommand = DefineReport.ID_SQL_BUMON_HEAD + sqlcommand;
      }
      if (DefineReport.ID_PAGE_TJ014.equals(outpage)) {
        User userInfo = (User) session.getAttribute(Consts.STR_SES_LOGINUSER);
        String tencd = userInfo.getTenpo();
        paramData.add(tencd);
        paramData.add(obj.optString("LSTNO"));
        paramData.add(tencd);
        sqlcommand = DefineReport.ID_SQL_BUMON_ZIZEN;
      }
    }

    // 大分類
    if (StringUtils.startsWith(outobj, DefineReport.Select.DAI_BUN.getObj())) {
      JSONObject obj = (JSONObject) map.get(0);
      JSONArray array1 = obj.optJSONArray("BUMON");
      String sqlWhere = DefineReport.ID_SQL_CMN_WHERE;
      if (array1 != null) {
        if (!DefineReport.Values.NONE.getVal().equals(obj.optString("BUMON")) && array1.size() > 0) {
          sqlWhere += DefineReport.ID_SQL_BMN_BUMONS_WHERE.replace("@", array1.join(",").replaceAll("\"", "'"));
        }

        if (!DefineReport.ID_PAGE_X001.equals(outpage) && !DefineReport.ID_PAGE_HT002.equals(outpage) && !DefineReport.ID_PAGE_HT004.equals(outpage) && !DefineReport.ID_PAGE_HT007.equals(outpage)
            && !DefineReport.ID_PAGE_HT009.equals(outpage) && !DefineReport.ID_PAGE_X231.equals(outpage) && !DefineReport.ID_PAGE_X244.equals(outpage) && !DefineReport.ID_PAGE_X261.equals(outpage)
            && !DefineReport.ID_PAGE_X280.equals(outpage)) {
          sqlcommand = DefineReport.ID_SQL_DAI_BUN + sqlWhere + DefineReport.ID_SQL_DAI_BUN_FOOTER;
        } else if (DefineReport.ID_PAGE_X001.equals(outpage) || DefineReport.ID_PAGE_X244.equals(outpage) || DefineReport.ID_PAGE_X261.equals(outpage) || DefineReport.ID_PAGE_X280.equals(outpage)) {
          sqlcommand = DefineReport.ID_SQL_DAI_BUN_S3 + sqlWhere + DefineReport.ID_SQL_DAI_BUN_FOOTER;
        } else {
          sqlcommand = DefineReport.ID_SQL_DAI_BUN_S2 + sqlWhere + DefineReport.ID_SQL_DAI_BUN_FOOTER;
        }
      } else {
        String bumon = obj.optString("BUMON");
        if (StringUtils.isNotEmpty(bumon)) {
          paramData.add(bumon);
          sqlWhere += DefineReport.ID_SQL_BMN_BUMON_WHERE;
          sqlcommand = DefineReport.ID_SQL_DAI_BUN_S + sqlWhere + DefineReport.ID_SQL_DAI_BUN_FOOTER;
        }
      }
      if (!obj.containsKey("REQUIRED")) {
        if (StringUtils.isEmpty(sqlcommand)) {
          sqlcommand = StringUtils.removeEnd(DefineReport.ID_SQL_BUMON_HEAD2, " union all ");
        } else {
          sqlcommand = DefineReport.ID_SQL_BUMON_HEAD2 + sqlcommand;
        }
      } else if (DefineReport.ID_PAGE_X001.equals(outpage) || DefineReport.ID_PAGE_X231.equals(outpage) || DefineReport.ID_PAGE_X244.equals(outpage) || DefineReport.ID_PAGE_X261.equals(outpage)
          || DefineReport.ID_PAGE_X280.equals(outpage)) {
        sqlcommand = DefineReport.ID_SQL_BUMON_HEAD + sqlcommand;
      }
    }

    // 中分類
    if (StringUtils.startsWith(outobj, DefineReport.Select.CHU_BUN.getObj())) {
      JSONObject obj = (JSONObject) map.get(0);
      JSONArray array1 = obj.optJSONArray("DAI_BUN");
      JSONArray array2 = obj.optJSONArray("BUMON");
      String sqlWhere = DefineReport.ID_SQL_CMN_WHERE;
      if (array1 != null) {
        if (!DefineReport.Values.NONE.getVal().equals(obj.optString("BUMON")) && array2.size() > 0) {
          sqlWhere += DefineReport.ID_SQL_BMN_BUMONS_WHERE.replace("@", array2.join(",").replaceAll("\"", "'"));
        }
        if (!DefineReport.Values.NONE.getVal().equals(obj.optString("DAI_BUN")) && array1.size() > 0) {
          sqlWhere += DefineReport.ID_SQL_BMN_DAIS_WHERE.replace("@", array1.join(",").replaceAll("\"", "'"));
        }

        if (!DefineReport.ID_PAGE_X001.equals(outpage) && !DefineReport.ID_PAGE_HT002.equals(outpage) && !DefineReport.ID_PAGE_HT004.equals(outpage) && !DefineReport.ID_PAGE_HT007.equals(outpage)
            && !DefineReport.ID_PAGE_HT009.equals(outpage) && !DefineReport.ID_PAGE_X231.equals(outpage) && !DefineReport.ID_PAGE_X244.equals(outpage) && !DefineReport.ID_PAGE_X261.equals(outpage)
            && !DefineReport.ID_PAGE_X280.equals(outpage)) {
          sqlcommand = DefineReport.ID_SQL_CHU_BUN + sqlWhere + DefineReport.ID_SQL_CHU_BUN_FOOTER;
        } else if (DefineReport.ID_PAGE_X001.equals(outpage) || DefineReport.ID_PAGE_X244.equals(outpage) || DefineReport.ID_PAGE_X261.equals(outpage) || DefineReport.ID_PAGE_X280.equals(outpage)) {
          sqlcommand = DefineReport.ID_SQL_CHU_BUN_S3 + sqlWhere + DefineReport.ID_SQL_CHU_BUN_FOOTER;
        } else {
          sqlcommand = DefineReport.ID_SQL_CHU_BUN_S2 + sqlWhere + DefineReport.ID_SQL_CHU_BUN_FOOTER;
        }
      } else {
        String bumon = obj.optString("BUMON");
        String dai_bumon = obj.optString("DAI_BUN");
        if (StringUtils.isNotEmpty(bumon) && StringUtils.isNotEmpty(dai_bumon)) {
          paramData.add(bumon);
          paramData.add(dai_bumon);
          sqlWhere += DefineReport.ID_SQL_BMN_BUMON_WHERE;
          sqlWhere += DefineReport.ID_SQL_BMN_DAI_WHERE;
          sqlcommand = DefineReport.ID_SQL_CHU_BUN_S + sqlWhere + DefineReport.ID_SQL_CHU_BUN_FOOTER;
        }
      }

      if (!obj.containsKey("REQUIRED")) {
        if (StringUtils.isEmpty(sqlcommand)) {
          sqlcommand = StringUtils.removeEnd(DefineReport.ID_SQL_BUMON_HEAD2, " union all ");
        } else {
          sqlcommand = DefineReport.ID_SQL_BUMON_HEAD2 + sqlcommand;
        }
      } else if (DefineReport.ID_PAGE_X001.equals(outpage) || DefineReport.ID_PAGE_X231.equals(outpage) || DefineReport.ID_PAGE_X244.equals(outpage) || DefineReport.ID_PAGE_X261.equals(outpage)
          || DefineReport.ID_PAGE_X280.equals(outpage)) {
        sqlcommand = DefineReport.ID_SQL_BUMON_HEAD + sqlcommand;
      }
    }

    // リードタイムパターン
    if (outobj.equals(DefineReport.Select.READTMPTN.getObj())) {
      JSONObject obj = (JSONObject) map.get(0);
      sqlcommand = DefineReport.ID_SQL_READTMPTN + DefineReport.ID_SQL_CMN_FOOTER;
      if (obj.containsKey("TOPBLANK")) {
        sqlcommand = DefineReport.ID_SQL_READTMPTN_HEAD + sqlcommand;
      }
    }


    // グループ分類名(絞り込み用)
    if (outobj.equals(DefineReport.InpText.GRPKN.getObj())) {
      paramData.add(nameWith);
      sqlcommand = DefineReport.ID_SQL_MSTGROUP;
      // 最大行数
      String max_row = DefineReport.SubGridRowNumber.DEF.getVal();
      sqlcommand = sqlcommand.replaceAll("@M", max_row);
    }

    // BYCD
    if (outobj.equals(DefineReport.Select.BYCD.getObj())) {
      JSONObject obj = (JSONObject) map.get(0);
      String sqlWhere = "";
      if (DefineReport.ID_PAGE_TG016.equals(outpage) || DefineReport.ID_PAGE_ST016.equals(outpage)) {
        String bumon = obj.optString("BUMON");
        if (NumberUtils.isNumber(bumon)) {
          paramData.add(bumon);
          sqlWhere += DefineReport.ID_SQL_SYSLOGIN_WHERE_MOY;
        }
      }
      if (StringUtils.isNotEmpty(nameWith)) {
        paramData.add(nameWith);
        sqlWhere += DefineReport.ID_SQL_SYSLOGIN_WHERE_LIKE;
      }

      if (DefineReport.ID_PAGE_ST016.equals(outpage)) {
        sqlcommand = DefineReport.ID_SQL_SYSLOGIN2 + sqlWhere + " LIMIT 100 ";
      } else {
        sqlcommand = DefineReport.ID_SQL_SYSLOGIN + sqlWhere + " LIMIT 100 ";
      }

      if (obj.containsKey("TOPBLANK")) {
        sqlcommand = DefineReport.ID_SQL_BLANK4 + sqlcommand;
      }
      sqlcommand = "select * from (" + sqlcommand + ") T " + DefineReport.ID_SQL_SYSLOGIN_FOOTER;
    }

    // センター
    if (outobj.equals(DefineReport.Select.CENTER_ORR.getObj())) {
      JSONObject obj = (JSONObject) map.get(0);
      String shoriDt = "";
      if (obj.containsKey("SHORIDT")) {
        shoriDt = obj.getString("SHORIDT");
      }

      sqlcommand = DefineReport.ID_SQL_CENTER;
      paramData.add(shoriDt);
      paramData.add(shoriDt);

      if (!obj.containsKey("REQUIRED")) {
        sqlcommand = DefineReport.ID_SQL_CENTER_HEAD + sqlcommand;
        paramData.add("1");
      }
    }

    // 商品区分
    if (outobj.equals(DefineReport.Select.SHNKBN.getObj())) {
      // JSONObject obj = (JSONObject)map.get(0);
      sqlcommand = DefineReport.ID_SQL_SHNKBN;

    }

    // 便コード
    if (outobj.equals(DefineReport.Select.SUPPLYNO_ORR.getObj())) {
      JSONObject obj = (JSONObject) map.get(0);
      String center = "";
      String shoriDt = "";
      if (obj.containsKey("CENTER")) {
        center = obj.getString("CENTER");
      }
      if (obj.containsKey("SHORIDT")) {
        shoriDt = obj.getString("SHORIDT");
      }

      String sqlWhere = "";

      if (StringUtils.isNotEmpty(center)) {
        sqlcommand = DefineReport.ID_SQL_SUPPLYNO;
        sqlWhere = "WHERE CORPORATIONCODE='0001' AND CENTERCD=? AND ";
        sqlWhere += "DATE_FORMAT(EFFECTIVESTARTDATE,'%Y%m%d') <= ? AND DATE_FORMAT(EFFECTIVEENDDATE,'%Y%m%d') >= ? AND ";
        sqlWhere += "LOGICALDELFLG = " + DefineReport.ValUpdkbn.NML.getVal() + " ";
        paramData.add(center);
        paramData.add(shoriDt);
        paramData.add(shoriDt);

        if (!obj.containsKey("REQUIRED")) {
          sqlcommand = DefineReport.ID_SQL_SUPPLYNO_HEAD + sqlcommand;
        }
        sqlcommand = " SELECT * FROM (" + sqlcommand + sqlWhere + ") AS T2 ORDER BY VALUE";
      }
    }

    // 権限
    if (StringUtils.startsWith(outobj, DefineReport.Select.AUTH.getObj())) {
      String values = "('1','参照不可')";
      values += ",('2','登録・削除可')";
      if (outobj.equals(DefineReport.Select.AUTH.getObj() + "_ms")) {
        values += ",('3','登録可')";
      }
      if (!outobj.equals(DefineReport.Select.AUTH.getObj() + "_tn")) {
        values += ",('4','参照可')";
      }
      sqlcommand = "select VALUE, TEXT from (values " + values + ") as X(value, TEXT) ORDER BY VALUE";
    }

    // 表示する・しない
    if (outobj.equals(DefineReport.Select.DISPLAY.getObj())) {
      String values = "('0','表示しない')";
      values += ",('1','表示する')";
      sqlcommand = "select VALUE, TEXT from (values " + values + ") as X(value, TEXT) ORDER BY VALUE";
    }

    // 名称マスタ
    if (ArrayUtils.contains(DefineReport.getItemObjs(MeisyoSelect.values()), outobj)
        || ArrayUtils.contains(DefineReport.getItemObjs(MeisyoSelect.values()), outobj.replaceFirst("_[0-9a-z]{1}$", ""))) {
      JSONObject obj = (JSONObject) map.get(0);
      String id = outobj.replaceFirst("_[0-9a-z]{1}$", "");
      if (!ArrayUtils.contains(DefineReport.getItemObjs(MeisyoSelect.values()), id)) {
        id = outobj;
      }
      MeisyoSelect item = (MeisyoSelect) DefineReport.getItemData(MeisyoSelect.values(), id);
      String sqlWhere = "";
      paramData.add(Integer.toString(item.getCd()));

      // 催し区分の場合
      if (DefineReport.MeisyoSelect.KBN10002.getObj().equals(id)) {
        if (DefineReport.ID_PAGE_BM003.equals(outpage) || DefineReport.ID_PAGE_GM001.equals(outpage) || DefineReport.ID_PAGE_BT001.equals(outpage)) {
          sqlWhere = " AND MEISHOCD IN ('" + DefineReport.ValKbn10002.VAL1.getVal() + "','" + DefineReport.ValKbn10002.VAL2.getVal() + "','" + DefineReport.ValKbn10002.VAL3.getVal() + "') ";
        }
        if (DefineReport.ID_PAGE_TM003.equals(outpage)) {
          sqlWhere = " AND MEISHOCD NOT IN ('" + DefineReport.ValKbn10002.VAL7.getVal() + "') ";
        }
        if (DefineReport.ID_PAGE_TG017.equals(outpage)) {
          sqlWhere = " AND MEISHOCD IN ('" + DefineReport.ValKbn10002.VAL0.getVal() + "','" + DefineReport.ValKbn10002.VAL1.getVal() + "','" + DefineReport.ValKbn10002.VAL2.getVal() + "','"
              + DefineReport.ValKbn10002.VAL3.getVal() + "') ";
        }
        if (DefineReport.ID_PAGE_X231.equals(outpage)) {
          sqlWhere = " AND MEISHOCD IN ('" + DefineReport.ValKbn10002.VAL0.getVal() + "','" + DefineReport.ValKbn10002.VAL1.getVal() + "','" + DefineReport.ValKbn10002.VAL2.getVal() + "') ";
        }
      }
      sqlcommand = DefineReport.ID_SQL_MEISYO + sqlWhere + DefineReport.ID_SQL_MEISYO_FOOTER;

      if (obj.containsKey("TOPBLANK") && !DefineReport.ID_PAGE_BM003.equals(outpage) && !DefineReport.MeisyoSelect.KBN910009.getObj().equals(id)
          && (!DefineReport.ID_PAGE_TR001.equals(outpage) || !DefineReport.MeisyoSelect.KBN105014.getObj().equals(id)) && !DefineReport.ID_PAGE_GM001.equals(outpage)) {
        sqlcommand = DefineReport.ID_SQL_MEISYO_HEAD + sqlcommand;
      } else if (DefineReport.ID_PAGE_BM003.equals(outpage) || DefineReport.ID_PAGE_GM001.equals(outpage) || DefineReport.ID_PAGE_MM001.equals(outpage)) {
        sqlcommand = DefineReport.ID_SQL_MEISYO_HEAD2 + sqlcommand;
      } else if (DefineReport.MeisyoSelect.KBN910009.getObj().equals(id)) {
        sqlcommand = DefineReport.ID_SQL_MEISYO_HEAD3 + DefineReport.ID_SQL_MEISYO2 + sqlWhere + DefineReport.ID_SQL_MEISYO_FOOTER2;
      }

      // X001 衣料使い回しフラグの場合
      if ((DefineReport.ID_PAGE_X001.equals(outpage) || DefineReport.ID_PAGE_X280.equals(outpage)) && DefineReport.MeisyoSelect.KBN142.getObj().equals(id)) {
        sqlcommand = "select KBN,VALUE,'' as " + DefineReport.KTXT + ",VALUE||'" + DefineReport.SEPARATOR + "'||TEXT as TEXT,TEXT as TEXT2,'' as " + DefineReport.STXT + ",'' as " + DefineReport.KANA
            + " from (values ROW('" + DefineReport.MeisyoSelect.KBN142.getCd() + "','2','両方')) as X(KBN,VALUE,TEXT) union all " + sqlcommand;
      }
    }

    // 処理日付
    if (outobj.equals(DefineReport.Text.SHORIDT.getObj())) {
      if (!DefineReport.ID_PAGE_X221.equals(outpage)) {
        sqlcommand = DefineReport.ID_SQLSHORIDT;
      } else {

        // YYMMDD型
        sqlcommand = DefineReport.ID_SQLSHORIDT2;
      }
    }

    // 処理日付曜日
    if (outobj.equals(DefineReport.Text.SHORIDTWEEK.getObj())) {
      sqlcommand = DefineReport.ID_SQLSHORIDTWEEK;
    }

    // 処理日付を基準日とした週№を取得
    if (outobj.equals(DefineReport.Text.SHUNOPERIOD.getObj())) {
      sqlcommand = DefineReport.ID_SQLSHUNOPERIOD;
    }

    // 処理日付を基準日とした発注日を取得
    if (outobj.equals(DefineReport.InpText.HTDT.getObj()) && DefineReport.ID_PAGE_X231.equals(outpage)) {
      // YYMMDD型
      sqlcommand = DefineReport.ID_SQLSHORIDT2;
    }

    // Web商談 取引先
    if (StringUtils.startsWith(outobj, DefineReport.Select.TORIHIKI.getObj()) || (StringUtils.startsWith(outobj, DefineReport.InpText.SSIRCD.getObj()) && DefineReport.ID_PAGE_X280.equals(outpage))) {
      User userInfo = (User) session.getAttribute(Consts.STR_SES_LOGINUSER);
      if (ArrayUtils.contains(new String[] {DefineReport.ID_PAGE_X245, DefineReport.ID_PAGE_X246, DefineReport.ID_PAGE_X247, DefineReport.ID_PAGE_X280}, outpage)) {
        if (userInfo.getYobi1_() == null || userInfo.getYobi1_().isEmpty()) {
          sqlcommand = DefineReport.ID_SQL_TORIHIKI_NO_YOBI_x245;
          paramData.add((new CmnDate()).getTomorrow());
        } else {
          sqlcommand = DefineReport.ID_SQL_TORIHIKI_x245;
          paramData.add(userInfo.getYobi1_());
        }
      } else if (StringUtils.isEmpty(nameWith)) {
        sqlcommand = DefineReport.ID_SQL_BLANK;
      }
    }

    // Web商談 提案件名
    if (StringUtils.startsWith(outobj, DefineReport.Select.TEIAN.getObj())) {
      if (ArrayUtils.contains(new String[] {DefineReport.ID_PAGE_X246}, outpage)) {
        User userInfo = (User) session.getAttribute(Consts.STR_SES_LOGINUSER);

        if (userInfo.getYobi1_() == null || userInfo.getYobi1_().isEmpty()) {
          sqlcommand = DefineReport.ID_SQL_TEIAN_NO_YOBI_x246;
        } else {
          sqlcommand = DefineReport.ID_SQL_TEIAN_x246;
          paramData.add(userInfo.getYobi1_());
        }

      }
    }

    // grid系
    if (StringUtils.startsWith(outobj, DefineReport.Grid.MAKER.getObj())) {
      JSONObject obj = (JSONObject) map.get(0);
      String txt_makerkn = obj.optString("MAKERKN").trim();
      String chk_dmakercd = obj.optString("DMAKERCD");
      String chk_makercd = obj.optString("MAKERCD");
      String sqlWhere = DefineReport.ID_SQL_CMN_WHERE;
      if (!StringUtils.isEmpty(txt_makerkn)) {
        paramData.add("%" + txt_makerkn + "%");
        sqlWhere += " and MAKERKN like ?";
      }
      String sqlWhereDmakercd = "(DMAKERCD = MAKERCD or DMAKERCD is null)"; // （代表コード=メーカーコード）OR（代表コード=NULL）
      String sqlWhereMakercd = "DMAKERCD <> MAKERCD"; // （代表コード<>メーカーコード）
      if (DefineReport.Values.ON.getVal().equals(chk_dmakercd) && DefineReport.Values.ON.getVal().equals(chk_makercd)) {
        sqlWhere += " and (" + sqlWhereDmakercd + " or " + sqlWhereMakercd + ")";
      } else if (DefineReport.Values.ON.getVal().equals(chk_dmakercd)) {
        sqlWhere += " and " + sqlWhereDmakercd;
      } else if (DefineReport.Values.ON.getVal().equals(chk_makercd)) {
        sqlWhere += " and " + sqlWhereMakercd;
      }
      sqlcommand = DefineReport.ID_SQL_MAKER + sqlWhere + DefineReport.ID_SQL_MAKER_FOOTER;

      String FromPage = (String) request.getSession().getAttribute("frompage");
      if (ArrayUtils.contains(new String[] {DefineReport.ID_PAGE_X247}, outpage)) {
        if (StringUtils.isEmpty(FromPage) || !FromPage.equals(DefineReport.ID_PAGE_X280)) {
          sqlcommand = DefineReport.ID_SQL_MAKER_TEIAN;
        }
      } else if (ArrayUtils.contains(new String[] {DefineReport.ID_PAGE_X251}, outpage) && FromPage.equals(DefineReport.ID_PAGE_X249)) {
        sqlcommand = DefineReport.ID_SQL_MAKER_TEIAN;
      } else if (ArrayUtils.contains(new String[] {DefineReport.ID_PAGE_X251}, outpage) && FromPage.equals(DefineReport.ID_PAGE_X250)) {
        sqlcommand = DefineReport.ID_SQL_MAKER_SHIKAKARI;
      }

    } else if (StringUtils.startsWith(outobj, DefineReport.Grid.SIR.getObj())) {
      JSONObject obj = (JSONObject) map.get(0);
      String sqlWhere = DefineReport.ID_SQL_CMN_WHERE;
      paramData.add("%" + obj.optString("SIRKN").trim() + "%");
      sqlWhere += " and SIRKN like ?";
      sqlcommand = DefineReport.ID_SQL_SIR + sqlWhere + DefineReport.ID_SQL_SIR_FOOTER;

      // 実仕入先一覧
    } else if (StringUtils.startsWith(outobj, DefineReport.Grid.ZITSIR.getObj())) {
      JSONObject obj = (JSONObject) map.get(0);
      String txt_sircd = obj.optString("SIRCD");
      // 空行用
      sqlcommand = DefineReport.ID_SQL_GRD_EMPTY;
      // 最大行数
      String max_row = DefineReport.SubGridRowNumber.ZITSIR.getVal();
      sqlcommand = sqlcommand.replaceAll("@M", max_row);

      if (!StringUtils.isEmpty(txt_sircd)) {
        // 仕入先コード
        paramData.add(txt_sircd);
        sqlcommand = DefineReport.ID_SQL_ZITSIR;
        sqlcommand = sqlcommand.replaceAll("@M", max_row);
      }

      // 複数仕入先店舗一覧
    } else if (StringUtils.startsWith(outobj, DefineReport.Grid.FSTENPO.getObj())) {
      JSONObject obj = (JSONObject) map.get(0);
      String txt_sircd = obj.optString("SIRCD");

      if (!StringUtils.isEmpty(txt_sircd)) {
        paramData.add(txt_sircd);
        sqlcommand = DefineReport.ID_SQL_FSTNEPO;
      } else {
        sqlcommand = "select TEN.TENCD, TEN.TENKN, null from INAMS.MSTTEN TEN where TEN.UPDKBN = " + DefineReport.ValUpdkbn.NML.getVal() + " order by TEN.TENCD";
      }

    } else if (StringUtils.startsWith(outobj, DefineReport.Grid.MSTUGENRYO.getObj())) {

      JSONObject obj = (JSONObject) map.get(0);
      String txt_bmncd = obj.optString("BMNCD");
      String txt_callcd = obj.optString("CALLCD");
      if (StringUtils.isNotEmpty(txt_bmncd) && StringUtils.isNotEmpty(txt_callcd)) {
        paramData.add(txt_bmncd);
        paramData.add(txt_callcd);
        sqlcommand = new Reportx092Dao(jndiName).getTOKMOYCDData(obj);
      }
      // 配送パターン
    } else if (StringUtils.startsWith(outobj, DefineReport.Grid.HSPTN.getObj())) {
      JSONObject obj = (JSONObject) map.get(0);
      String txt_sircd = obj.optString("SIRCD");
      String txt_hsptn = obj.optString("HSPTN");
      String txt_hsptnkn = obj.optString("HSPTNKN");
      String sqlWhere = "";

      if (StringUtils.equals(outobj, DefineReport.Grid.HSPTN.getObj() + "_list")) {

        if (StringUtils.isNotEmpty(txt_sircd)) {
          sqlcommand = DefineReport.ID_SQL_HSPTN_LIST;
          paramData.add(txt_sircd);

        } else {
          // 空行用
          sqlcommand = DefineReport.ID_SQL_GRD_EMPTY;
        }

        // 最大行数
        String max_row = DefineReport.SubGridRowNumber.HSPTN.getVal();
        sqlcommand = sqlcommand.replaceAll("@M", max_row);

      } else if (StringUtils.equals(outobj, DefineReport.Grid.HSPTN.getObj() + "_win004")) {
        sqlWhere = DefineReport.ID_SQL_CMN_WHERE;
        if (!StringUtils.isEmpty(txt_hsptn)) {
          // 配送パターン
          paramData.add(txt_hsptn);
          sqlWhere += " and HSPTN >= ?";
        }
        if (!StringUtils.isEmpty(txt_hsptnkn)) {
          // 配送パターン名称
          paramData.add("%" + txt_hsptnkn + "%");
          sqlWhere += " and HSPTNKN like ?";
        }
        sqlcommand = DefineReport.ID_SQL_HSPTN + sqlWhere + DefineReport.ID_SQL_HSPTN_FOOTER;
      }

      // エリア別配送パターン
    } else if (StringUtils.startsWith(outobj, DefineReport.Grid.EHSPTN.getObj())) {
      JSONObject obj = (JSONObject) map.get(0);
      if (StringUtils.equals(outobj, DefineReport.Grid.EHSPTN.getObj() + "_list")) {
        String txt_sircd = obj.optString("SIRCD");

        if (StringUtils.isNotEmpty(txt_sircd)) {
          sqlcommand = DefineReport.ID_SQL_EHSPTN_LIST;
          paramData.add(txt_sircd);

        } else {
          // 空行用
          sqlcommand = DefineReport.ID_SQL_GRD_EMPTY;
        }
        // 最大行数
        String max_row = DefineReport.SubGridRowNumber.HSPTN.getVal();
        sqlcommand = sqlcommand.replaceAll("@M", max_row);

      } else if (StringUtils.equals(outobj, DefineReport.Grid.EHSPTN.getObj() + "_hp012")) {
        String txt_areakbn = obj.optString("AREAKBN");
        String txt_hsgpcd = obj.optString("HSGPCD");
        String txt_hsptn = obj.optString("HSPTN");
        String txt_nmlSearch = obj.optString("NMLSEARCH");

        // 空行用
        sqlcommand = DefineReport.ID_SQL_GRD_EMPTY;
        // 最大行数
        String max_row = DefineReport.SubGridRowNumber.TENPO.getVal();

        if (StringUtils.isNotEmpty(txt_areakbn) && StringUtils.isNotEmpty(txt_hsgpcd)) {
          if (StringUtils.isNotEmpty(txt_hsptn)) {
            if (StringUtils.equals("1", txt_nmlSearch)) {
              paramData.add(txt_hsptn);
              paramData.add(txt_areakbn);
              paramData.add(txt_hsgpcd);
              sqlcommand = DefineReport.ID_SQL_EHSPTN_HP012_UPD;
            }
          } else {
            paramData.add(txt_areakbn);
            paramData.add(txt_hsgpcd);
            sqlcommand = DefineReport.ID_SQL_EHSPTN_HP012_INS;
          }
        }
        sqlcommand = sqlcommand.replaceAll("@M", max_row);
      } else {
        sqlcommand = DefineReport.ID_SQL_EHSPTN;
      }

      // 配送グループ
    } else if (StringUtils.startsWith(outobj, DefineReport.Grid.HSGP.getObj())) {
      JSONObject obj = (JSONObject) map.get(0);
      String txt_areakbn = obj.optString("AREAKBN");

      if (!StringUtils.isEmpty(txt_areakbn)) {
        paramData.add(txt_areakbn);
        sqlcommand = DefineReport.ID_SQL_HSGP + DefineReport.ID_SQL_HSGP_FOOTER;

      }
      // 配送店グループ
    } else if (StringUtils.startsWith(outobj, DefineReport.Grid.HSTNGP.getObj())) {
      JSONObject obj = (JSONObject) map.get(0);
      String txt_hsgpcd = obj.optString("HSGPCD");

      // 空行用
      sqlcommand = DefineReport.ID_SQL_GRD_EMPTY;
      // 最大行数
      String max_row = DefineReport.SubGridRowNumber.DEF.getVal();
      sqlcommand = sqlcommand.replaceAll("@M", max_row);

      if (!StringUtils.isEmpty(txt_hsgpcd)) {
        paramData.add(txt_hsgpcd);
        sqlcommand = DefineReport.ID_SQL_HSTENGP;
        sqlcommand = sqlcommand.replaceAll("@M", max_row);

      }
      // 店舗休日
    } else if (StringUtils.startsWith(outobj, DefineReport.Grid.TENPO_M.getObj())) {

      // 空行用
      sqlcommand = DefineReport.ID_SQL_GRD_EMPTY;
      // 最大行数
      String max_row = DefineReport.SubGridRowNumber.TEMPM.getVal();
      sqlcommand = sqlcommand.replaceAll("@M", max_row);

      // 店舗一覧
    } else if (StringUtils.equals(outobj, DefineReport.Grid.TENPO.getObj())) {
      JSONObject obj = (JSONObject) map.get(0);

      // 空行用
      sqlcommand = DefineReport.ID_SQL_GRD_EMPTY;
      // 最大行数
      String max_row = DefineReport.SubGridRowNumber.DEF.getVal();

      if (DefineReport.ID_PAGE_TG003.equals(outpage)) {
        sqlcommand = new ReportTG003Dao(jndiName).createSqlSelTOKTG_TEN(obj, true);

      } else {
        String txt_tengpcd = obj.optString("TENGPCD");
        String gpkbn = obj.optString("GPKBN");
        String SelBumon = obj.optString("BMNCD");
        if (!DefineReport.ID_PAGE_X192.equals(outpage)) {
          max_row = DefineReport.SubGridRowNumber.TENPO.getVal();
        } else {
          max_row = "400";
        }
        if (!StringUtils.isEmpty(txt_tengpcd) && !StringUtils.isEmpty(gpkbn) && !StringUtils.isEmpty(SelBumon) && !StringUtils.equals(DefineReport.Values.NONE.getVal(), gpkbn)
            && !StringUtils.equals(DefineReport.Values.NONE.getVal(), SelBumon)) {
          paramData.add(gpkbn);
          paramData.add(SelBumon);
          paramData.add(txt_tengpcd);

          sqlcommand = DefineReport.ID_SQL_TENPO_STGT;
          sqlcommand = sqlcommand.replaceAll("@M", max_row);

        }
      }
      sqlcommand = sqlcommand.replaceAll("@M", max_row);

      // 店舗一覧(win005)
    } else if (StringUtils.equals(outobj, DefineReport.Grid.TENPO.getObj() + "_win005")) {
      JSONObject obj = (JSONObject) map.get(0);
      String txt_hsgpcd = obj.optString("HSGPCD");
      String txt_tengpcd = obj.optString("TENGPCD");
      String ariakbn = obj.optString("AREAKBN");

      if (StringUtils.isNotEmpty(ariakbn)) {
        if (StringUtils.equals("0", ariakbn)) {
          if (StringUtils.isNotEmpty(txt_tengpcd)) {
            // 店舗グループコード
            paramData.add(txt_tengpcd);
            sqlcommand = DefineReport.ID_SQL_TENPO_TB;
          }
        } else if (StringUtils.equals("1", ariakbn)) {
          if (StringUtils.isNotEmpty(txt_hsgpcd) && StringUtils.isNotEmpty(txt_tengpcd)) {
            paramData.add(txt_hsgpcd);
            paramData.add(txt_tengpcd);
            sqlcommand = DefineReport.ID_SQL_TENPO_HG;
          }
        }
      }

      // 店舗一覧(予約発注)
    } else if (StringUtils.equals(outobj, DefineReport.Grid.TENPO＿YH.getObj())) {
      JSONObject obj = (JSONObject) map.get(0);

      String szWhere = " where TEN.TENCD = ?";

      String txt_kkkcd = obj.optString("KKKCD");
      String txt_shncd = obj.optString("SHNCD");
      String txt_nndt = obj.optString("NNDT");
      String txt_shoridt = obj.optString("SHORIDT");

      // 空行用
      sqlcommand = DefineReport.ID_SQL_GRD_EMPTY;
      // 最大行数
      String max_row = DefineReport.SubGridRowNumber.TENPO_YH.getVal();
      sqlcommand = sqlcommand.replaceAll("@M", max_row);

      if (!StringUtils.isEmpty(txt_kkkcd) || !StringUtils.isEmpty(txt_shncd) || !StringUtils.isEmpty(txt_nndt)) {

        paramData.add(txt_shoridt); // case文にて使用(F2:前日までの発注数)
        paramData.add(txt_shoridt); // case文にて使用(F3:当日発注数)
        paramData.add(txt_kkkcd);
        paramData.add(txt_shncd);

        szWhere = "where HTEN.KKKCD = ? and HTEN.SHNCD = ?";

        if (!StringUtils.equals("合計", txt_nndt)) {
          szWhere += "and HTEN.NNDT = ?";
          paramData.add(txt_nndt);
        }

        sqlcommand = DefineReport.ID_SQL_TENPO_HTS;
        sqlcommand = sqlcommand.replaceAll("@M", max_row);
        sqlcommand = sqlcommand.replaceAll("@W", szWhere);
      }
      // 店舗一覧(新店改装発注)
    } else if (StringUtils.equals(outobj, DefineReport.Grid.TENPO＿SK.getObj())) {
      JSONObject obj = (JSONObject) map.get(0);

      String txt_inputNo = obj.optString("INPUTNO");
      String txt_tencd = obj.optString("TENCD");
      String txt_kspage = obj.optString("KSPAGE");
      String txt_dispModo = obj.optString("DISPMODO");

      // 空行用
      sqlcommand = DefineReport.ID_SQL_GRD_EMPTY;
      // 最大行数
      String max_row = DefineReport.SubGridRowNumber.TENPO_SK.getVal();
      sqlcommand = sqlcommand.replaceAll("@M", max_row);

      if (!StringUtils.isEmpty(txt_tencd) && !StringUtils.isEmpty(txt_kspage)) {

        String szWhere = " where (COALESCE(SHN.UPDKBN, 0) <> 1 or SHN.SHNCD = '99999994') and TEN.TENCD = ?";
        paramData.add(txt_tencd);

        if (!StringUtils.isEmpty(txt_kspage.substring(0, 2))) {
          szWhere += "and KSPG.BMNCD = ?";
          paramData.add(txt_kspage.substring(0, 2));
        }

        if (!StringUtils.isEmpty(txt_kspage.substring(2, 4))) {
          szWhere += "and KSPG.BRUICD = ?";
          paramData.add(txt_kspage.substring(2, 4));

        }

        if (!StringUtils.isEmpty(txt_kspage.substring(4, 7))) {
          szWhere += "and KSPG.PTN = ?";
          paramData.add(txt_kspage.substring(4, 7));

        }

        if (!StringUtils.isEmpty(txt_kspage.substring(7, 8))) {
          szWhere += "and KSPG.EDABAN = ?";
          paramData.add(txt_kspage.substring(7, 8));

        }

        sqlcommand = DefineReport.ID_SQL_TENPO_SK_K;
        sqlcommand = sqlcommand.replaceAll("@M", max_row).replace("@W", szWhere);

      } else if (!StringUtils.isEmpty(txt_inputNo)) {

        paramData.add(txt_inputNo); // case文にて使用(F2:前日までの発注数)
        sqlcommand = DefineReport.ID_SQL_TENPO_SK;
        sqlcommand = sqlcommand.replaceAll("@M", max_row);

        String sqlWhere = "";
        if (StringUtils.equals("TypeF", txt_dispModo) || StringUtils.equals("TypeD", txt_dispModo)) {
          sqlWhere += "where T2.KANRINO is not null";
        }
        sqlcommand = sqlcommand.replaceAll("@W", sqlWhere);

      }

      // 商品一覧(予約発注)
    } else if (StringUtils.equals(outobj, DefineReport.Grid.SHOHIN.getObj())) {
      JSONObject obj = (JSONObject) map.get(0);

      String txt_kkkcd = obj.optString("KKKCD");
      String txt_shoridt = obj.optString("SHORIDT");

      if (!StringUtils.isEmpty(txt_kkkcd) && !StringUtils.isEmpty(txt_shoridt)) {
        paramData.add(txt_shoridt);
        paramData.add(txt_shoridt);
        paramData.add(txt_kkkcd);

        sqlcommand = DefineReport.ID_SQL_SHOHIN_HTS;

      } else if (!StringUtils.isEmpty(txt_kkkcd)) {
        paramData.add(txt_kkkcd);

        sqlcommand = DefineReport.ID_SQL_SHOHIN;

      }
      // 店確認
    } else if (StringUtils.equals(outobj, DefineReport.Grid.ADTEN.getObj())) {
      JSONObject obj = (JSONObject) map.get(0);

      String txt_kkkno = obj.optString("KKKNO");

      // 空行用
      sqlcommand = DefineReport.ID_SQL_GRD_EMPTY;
      // 最大行数
      String max_row = DefineReport.SubGridRowNumber.ADTEN.getVal();
      sqlcommand = sqlcommand.replaceAll("@M", max_row);

      if (!StringUtils.isEmpty(txt_kkkno)) {
        paramData.add(txt_kkkno);
        paramData.add(txt_kkkno);
        sqlcommand = DefineReport.ID_SQL_ADTEN;
        sqlcommand = sqlcommand.replaceAll("@M", max_row);

      }
      // 納入日一覧(予約発注)
    } else if (StringUtils.equals(outobj, DefineReport.Grid.NOHIN.getObj())) {
      JSONObject obj = (JSONObject) map.get(0);

      String txt_kkkcd = obj.optString("KKKCD");
      String txt_shncd = obj.optString("SHNCD");
      String txt_shoridt = obj.optString("SHORIDT");

      if (!StringUtils.isEmpty(txt_shoridt)) {

        paramData.add(txt_shoridt); // case文にて使用(F2:前日までの発注数)
        paramData.add(txt_shoridt); // case文にて使用(F3:当日発注数)
        paramData.add(txt_kkkcd);
        paramData.add(txt_shncd);
        sqlcommand = DefineReport.ID_SQL_NOHIN_HTS;

      } else {
        // 空行用
        sqlcommand = DefineReport.ID_SQL_GRD_EMPTY;
        // 最大行数
        String max_row = DefineReport.SubGridRowNumber.DEF.getVal();
        sqlcommand = sqlcommand.replaceAll("@M", max_row);

        if (!StringUtils.isEmpty(txt_kkkcd) || !StringUtils.isEmpty(txt_shncd)) {
          paramData.add(txt_kkkcd);
          paramData.add(txt_shncd);

          sqlcommand = DefineReport.ID_SQL_NOHIN;
          sqlcommand = sqlcommand.replaceAll("@M", max_row);

        }
      }
    } else if (StringUtils.startsWith(outobj, DefineReport.Grid.EHSPTN.getObj())) {
      sqlcommand = DefineReport.ID_SQL_EHSPTN;

    } else if (StringUtils.startsWith(outobj, DefineReport.Grid.SRCCD.getObj())) {
      JSONObject obj = (JSONObject) map.get(0);
      if (DefineReport.ValTablekbn.CSV.getVal().equals(obj.optString("TABLEKBN"))) {
        String seq = obj.optString("SEQ");
        String inputno = obj.optString("INPUTNO");

        if (StringUtils.isNotEmpty(seq) && StringUtils.isNotEmpty(inputno)) {
          String sztable = "INAMS.CSVSRCCD T where T.SEQ = " + seq + " and T.INPUTNO = " + inputno;
          sqlcommand = DefineReport.ID_SQL_SRCCD1.replace("@T", sztable).replaceAll("T.SOURCEKBN", "T.SRCKBN");
        }
      } else {
        String sel_shocd = obj.optString("SHNCD");
        if (obj.containsKey("SHNCD") && StringUtils.isNotEmpty(sel_shocd)) {
          paramData.add(sel_shocd + "%");
          String sztable = "INAMS.MSTSRCCD T where T.SHNCD like ? ";
          String yoyakudt = obj.optString("YOYAKUDT");
          if (DefineReport.ValTablekbn.YYK.getVal().equals(obj.optString("TABLEKBN")) && StringUtils.isNotEmpty(yoyakudt)) {
            paramData.add(yoyakudt);
            sztable = "INAMS.MSTSRCCD_Y T where T.SHNCD like ? and T.YOYAKUDT = ?";
          }

          String FromPage = (String) request.getSession().getAttribute("frompage");
          if (DefineReport.ID_PAGE_X247.equals(outpage)
              || (StringUtils.equals(outobj, DefineReport.Grid.SRCCD.getObj() + "_winIT031") && DefineReport.ID_PAGE_X247.equals(obj.optString("callpage")))) {
            if (StringUtils.isEmpty(FromPage) || !FromPage.equals(DefineReport.ID_PAGE_X280)) {
              sztable = "INAWS.PIMTISRCCD T where T.SHNCD like ? ";
            }
          } else if ((DefineReport.ID_PAGE_X251.equals(outpage) && FromPage.equals(DefineReport.ID_PAGE_X249)) || (StringUtils.equals(outobj, DefineReport.Grid.SRCCD.getObj() + "_winIT031")
              && (DefineReport.ID_PAGE_X251.equals(obj.optString("callpage")) && FromPage.equals(DefineReport.ID_PAGE_X249)))) {
            sztable = "INAWS.PIMTISRCCD T where T.SHNCD like ? ";
          } else if ((DefineReport.ID_PAGE_X251.equals(outpage) && FromPage.equals(DefineReport.ID_PAGE_X250)) || (StringUtils.equals(outobj, DefineReport.Grid.SRCCD.getObj() + "_winIT031")
              && (DefineReport.ID_PAGE_X251.equals(obj.optString("callpage")) && FromPage.equals(DefineReport.ID_PAGE_X250)))) {
            sztable = "INAWS.PIMSISRCCD T where T.SHNCD like ? ";
          }
          sqlcommand = DefineReport.ID_SQL_SRCCD1.replace("@T", sztable);

          if (DefineReport.ID_PAGE_X002.equals(outpage) || StringUtils.equals(outobj, DefineReport.Grid.SRCCD.getObj() + "_winIT031")) {
            sqlcommand = "SELECT * FROM(" + sqlcommand
                + ") AS MT order by case when SEQNO = '1' then '1' when SEQNO = '2' then '2' else '9' end, case when DATE_FORMAT(SYSDATE(),'%y%m%d') <= YUKO_STDT then 1 else 9 end, YUKO_STDT, SRCCD";
          }
          /*
           * if (DefineReport.ID_PAGE_X251.equals(outpage) || StringUtils.equals(outobj,
           * DefineReport.Grid.SRCCD.getObj()+"_winIT031")) { sqlcommand = "SELECT * FROM(" + sqlcommand +
           * ") order by case when SEQNO = '1' then '1' when SEQNO = '2' then '2' else '9' end, case when to_char(sysdate,'yymmdd') <= YUKO_STDT then 1 else 9 end, YUKO_STDT, SRCCD"
           * ; }
           */
        }
      }
      // 出力行数指定
      String rownum = obj.optString("ROWNUM");
      if (StringUtils.isNotEmpty(sqlcommand) && StringUtils.isNotEmpty(rownum) && NumberUtils.isNumber(rownum)) {
        sqlcommand += " LIMIT " + rownum + " ";
      }
    } else if (StringUtils.equals(outobj, DefineReport.Grid.TENGP.getObj())) {
      JSONObject obj = (JSONObject) map.get(0);
      if (DefineReport.ID_PAGE_TG040.equals(outpage)) {
        HashMap<String, String> hmap = new HashMap<String, String>();
        hmap.put("MOYSKBN", obj.optString("MOYSKBN"));
        hmap.put("MOYSSTDT", obj.optString("MOYSSTDT"));
        hmap.put("MOYSRBAN", obj.optString("MOYSRBAN"));
        sqlcommand = new ReportTG040Dao(jndiName).createSqlSelTOKTG_TENGP(hmap);
      }

    } else if (StringUtils.startsWith(outobj, DefineReport.Grid.TENGP.getObj() + "_")) {
      JSONObject obj = (JSONObject) map.get(0);
      String gpkbn = obj.optString("GPKBN");
      String areakbn = obj.optString("AREAKBN");
      String bmncd = obj.optString("BMNCD");
      String tengpkn = StringUtils.trim(obj.optString("TENGPKN"));

      if (StringUtils.isNotEmpty(gpkbn) || StringUtils.isNotEmpty(areakbn) || StringUtils.isNotEmpty(bmncd)) {
        if (NumberUtils.isNumber(gpkbn) && NumberUtils.isNumber(areakbn) && NumberUtils.isNumber(bmncd)) {
          //
          paramData.add(gpkbn);
          paramData.add(bmncd);
          paramData.add(areakbn);
          paramData.add("%" + tengpkn + "%");
          sqlcommand = DefineReport.ID_SQL_TENGP2;
        }
      }
    } else if (StringUtils.startsWith(outobj, DefineReport.Grid.TENGP.getObj() + DefineReport.ValGpkbn.SIR.getVal())
        || StringUtils.startsWith(outobj, DefineReport.Grid.TENGP.getObj() + DefineReport.ValGpkbn.BAIKA.getVal())
        || StringUtils.startsWith(outobj, DefineReport.Grid.TENGP.getObj() + DefineReport.ValGpkbn.SHINA.getVal())
        || StringUtils.startsWith(outobj, DefineReport.Grid.TENGP.getObj() + DefineReport.ValGpkbn.TBMN.getVal())) {
      JSONObject obj = (JSONObject) map.get(0);
      String gpkbn = obj.optString("GPKBN");
      String bmncd = obj.optString("BMNCD");

      // 空行用
      sqlcommand = DefineReport.ID_SQL_GRD_EMPTY;
      if (DefineReport.ValTablekbn.CSV.getVal().equals(obj.optString("TABLEKBN"))) {
        String seq = obj.optString("SEQ");
        String inputno = obj.optString("INPUTNO");

        if (StringUtils.isNotEmpty(seq) && StringUtils.isNotEmpty(inputno)) {
          paramData.add(seq);
          paramData.add(inputno);
          paramData.add(StringUtils.defaultIfEmpty(bmncd, "-1"));

          if (DefineReport.ValGpkbn.SIR.getVal().equals(gpkbn)) {
            sqlcommand = DefineReport.ID_SQL_TENGP_SIR_C;
          } else if (DefineReport.ValGpkbn.BAIKA.getVal().equals(gpkbn)) {
            sqlcommand = DefineReport.ID_SQL_TENGP_BAIKA_C;
          } else if (DefineReport.ValGpkbn.SHINA.getVal().equals(gpkbn)) {
            sqlcommand = DefineReport.ID_SQL_TENGP_SHINA_C;
          } else if (DefineReport.ValGpkbn.TBMN.getVal().equals(gpkbn)) {
            sqlcommand = DefineReport.ID_SQL_TENGP_TBMN_C; // TODO:テーブル定義変更？今エラーおきる
          }
        }
      } else {
        String sel_shocd = obj.optString("SEL_SHNCD");
        String areakbn = obj.optString("AREAKBN");

        if (StringUtils.isNotEmpty(sel_shocd) && StringUtils.isNotEmpty(gpkbn)) {
          if (NumberUtils.isNumber(sel_shocd) && NumberUtils.isNumber(areakbn) && NumberUtils.isNumber(bmncd)) {
            // 予約情報を取得する場合
            if (DefineReport.ValTablekbn.YYK.getVal().equals(obj.optString("TABLEKBN"))) {
              String yoyakudt = obj.optString("YOYAKUDT");
              if (StringUtils.isNotEmpty(yoyakudt)) {
                paramData.add(sel_shocd + "%");
                paramData.add(yoyakudt);
                paramData.add(areakbn);
                paramData.add(bmncd);
                if (DefineReport.ValGpkbn.SIR.getVal().equals(gpkbn)) {
                  sqlcommand = DefineReport.ID_SQL_TENGP_SIR_Y;
                } else if (DefineReport.ValGpkbn.BAIKA.getVal().equals(gpkbn)) {
                  sqlcommand = DefineReport.ID_SQL_TENGP_BAIKA_Y;
                } else if (DefineReport.ValGpkbn.SHINA.getVal().equals(gpkbn)) {
                  sqlcommand = DefineReport.ID_SQL_TENGP_SHINA_Y;
                } else if (DefineReport.ValGpkbn.TBMN.getVal().equals(gpkbn)) {
                  sqlcommand = DefineReport.ID_SQL_TENGP_TBMN_Y;
                }
              }
            } else {
              paramData.add(sel_shocd + "%");
              paramData.add(areakbn);
              paramData.add(bmncd);

              String FromPage = (String) request.getSession().getAttribute("frompage");
              if (DefineReport.ValGpkbn.SIR.getVal().equals(gpkbn)) {
                if (ArrayUtils.contains(new String[] {DefineReport.ID_PAGE_X247}, outpage)) {
                  if (StringUtils.isEmpty(FromPage) || !FromPage.equals(DefineReport.ID_PAGE_X280)) {
                    sqlcommand = DefineReport.ID_SQL_TENGP_SIR_TEIAN;
                  } else {
                    sqlcommand = DefineReport.ID_SQL_TENGP_SIR;
                  }
                } else if (ArrayUtils.contains(new String[] {DefineReport.ID_PAGE_X251}, outpage) && FromPage.equals(DefineReport.ID_PAGE_X249)) {
                  sqlcommand = DefineReport.ID_SQL_TENGP_SIR_TEIAN;
                } else if (ArrayUtils.contains(new String[] {DefineReport.ID_PAGE_X251}, outpage) && FromPage.equals(DefineReport.ID_PAGE_X250)) {
                  sqlcommand = DefineReport.ID_SQL_TENGP_SIR_SHIKAKARI;
                } else {
                  sqlcommand = DefineReport.ID_SQL_TENGP_SIR;
                }

              } else if (DefineReport.ValGpkbn.BAIKA.getVal().equals(gpkbn)) {
                if (ArrayUtils.contains(new String[] {DefineReport.ID_PAGE_X247}, outpage)) {
                  if (StringUtils.isEmpty(FromPage) || !FromPage.equals(DefineReport.ID_PAGE_X280)) {
                    sqlcommand = DefineReport.ID_SQL_TENGP_BAIKA_TEIAN;
                  } else {
                    sqlcommand = DefineReport.ID_SQL_TENGP_BAIKA;
                  }
                } else if (ArrayUtils.contains(new String[] {DefineReport.ID_PAGE_X251}, outpage) && FromPage.equals(DefineReport.ID_PAGE_X249)) {
                  sqlcommand = DefineReport.ID_SQL_TENGP_BAIKA_TEIAN;
                } else if (ArrayUtils.contains(new String[] {DefineReport.ID_PAGE_X251}, outpage) && FromPage.equals(DefineReport.ID_PAGE_X250)) {
                  sqlcommand = DefineReport.ID_SQL_TENGP_BAIKA_SHIKAKARI;
                } else {
                  sqlcommand = DefineReport.ID_SQL_TENGP_BAIKA;
                }
              } else if (DefineReport.ValGpkbn.SHINA.getVal().equals(gpkbn)) {
                if (ArrayUtils.contains(new String[] {DefineReport.ID_PAGE_X247}, outpage)) {
                  if (StringUtils.isEmpty(FromPage) || !FromPage.equals(DefineReport.ID_PAGE_X280)) {
                    sqlcommand = DefineReport.ID_SQL_TENGP_SHINA_TEIAN;
                  } else {
                    sqlcommand = DefineReport.ID_SQL_TENGP_SHINA;
                  }
                } else if (ArrayUtils.contains(new String[] {DefineReport.ID_PAGE_X251}, outpage) && FromPage.equals(DefineReport.ID_PAGE_X249)) {
                  sqlcommand = DefineReport.ID_SQL_TENGP_SHINA_TEIAN;
                } else if (ArrayUtils.contains(new String[] {DefineReport.ID_PAGE_X251}, outpage) && FromPage.equals(DefineReport.ID_PAGE_X250)) {
                  sqlcommand = DefineReport.ID_SQL_TENGP_SHINA_SHIKAKARI;
                } else {
                  sqlcommand = DefineReport.ID_SQL_TENGP_SHINA;
                }
              } else if (DefineReport.ValGpkbn.TBMN.getVal().equals(gpkbn)) {
                sqlcommand = DefineReport.ID_SQL_TENGP_TBMN;
              }
            }
          }
        }
      }
      // 最大行数
      String max_row = DefineReport.SubGridRowNumber.DEF.getVal();
      if (DefineReport.ValGpkbn.SIR.getVal().equals(gpkbn)) {
        // MAX20行
        max_row = DefineReport.SubGridRowNumber.TENGP_SIR.getVal();
      } else if (DefineReport.ValGpkbn.BAIKA.getVal().equals(gpkbn)) {
        // TODO:MAX?行→とりあえず仕入に合わせる
        max_row = DefineReport.SubGridRowNumber.TENGP_BAIKA.getVal();
      } else if (DefineReport.ValGpkbn.SHINA.getVal().equals(gpkbn)) {
        // MAX10行
        max_row = DefineReport.SubGridRowNumber.TENGP_SHINA.getVal();
      } else if (DefineReport.ValGpkbn.TBMN.getVal().equals(gpkbn)) {
        // MAX10行
        max_row = DefineReport.SubGridRowNumber.TENGP_TBMN.getVal();
      }
      sqlcommand = sqlcommand.replaceAll("@M", max_row);

    } else if (StringUtils.startsWith(outobj, DefineReport.Grid.ALLERGY.getObj())) {
      JSONObject obj = (JSONObject) map.get(0);

      if (DefineReport.ValTablekbn.CSV.getVal().equals(obj.optString("TABLEKBN"))) {
        String seq = obj.optString("SEQ");
        String inputno = obj.optString("INPUTNO");
        if (StringUtils.isNotEmpty(seq) && StringUtils.isNotEmpty(inputno)) {
          paramData.add(Integer.toString(DefineReport.MeisyoSelect.KBN146.getCd()));
          paramData.add(seq);
          paramData.add(inputno);
          sqlcommand = DefineReport.ID_SQL_ALLERGY2.replace("@T", "INAMS.CSVTENKABUTSU").replace("@W", " and SEQ = ? and INPUTNO = ?");
        }
      } else {
        String sel_shocd = obj.optString("SEL_SHNCD");
        if (StringUtils.isNotEmpty(sel_shocd) && NumberUtils.isNumber(sel_shocd)) {
          paramData.add(Integer.toString(DefineReport.MeisyoSelect.KBN146.getCd()));
          paramData.add(sel_shocd + "%");
          paramData.add(DefineReport.ValTenkabkbn.VAL2.getVal());

          String szTable = "INAMS.MSTTENKABUTSU";

          String FromPage = (String) request.getSession().getAttribute("frompage");
          if (ArrayUtils.contains(new String[] {DefineReport.ID_PAGE_X247}, outpage)) {
            if (StringUtils.isEmpty(FromPage) || !FromPage.equals(DefineReport.ID_PAGE_X280)) {
              szTable = "INAWS.PIMTITENKABUTSU";
            }
          } else if (ArrayUtils.contains(new String[] {DefineReport.ID_PAGE_X251}, outpage) && FromPage.equals(DefineReport.ID_PAGE_X249)) {
            szTable = "INAWS.PIMTITENKABUTSU";
          } else if (ArrayUtils.contains(new String[] {DefineReport.ID_PAGE_X251}, outpage) && FromPage.equals(DefineReport.ID_PAGE_X250)) {
            szTable = "INAWS.PIMSITENKABUTSU";
          }

          String szWhere = " and T2.SHNCD like ? and T2.TENKABKBN = ?";
          if (DefineReport.ValTablekbn.YYK.getVal().equals(obj.optString("TABLEKBN"))) {
            paramData.add(obj.optString("YOYAKUDT"));
            szTable += "_Y";
            szWhere += " and T2.YOYAKUDT = ?";
          }
          sqlcommand = DefineReport.ID_SQL_ALLERGY2.replace("@T", szTable).replace("@W", szWhere);
        }
      }
      if (StringUtils.isEmpty(sqlcommand)) {
        paramData.add(Integer.toString(DefineReport.MeisyoSelect.KBN146.getCd()));
        sqlcommand = "select VALUE, TEXT, '' as SEL from (" + DefineReport.ID_SQL_MEISYO + ") T order by VALUE";
      }

    } else if (StringUtils.startsWith(outobj, DefineReport.Grid.TENKABUTSU.getObj())) {
      JSONObject obj = (JSONObject) map.get(0);
      // 空行用
      sqlcommand = DefineReport.ID_SQL_GRD_EMPTY;
      if (DefineReport.ValTablekbn.CSV.getVal().equals(obj.optString("TABLEKBN"))) {
        String seq = obj.optString("SEQ");
        String inputno = obj.optString("INPUTNO");
        if (StringUtils.isNotEmpty(seq) && StringUtils.isNotEmpty(inputno)) {
          paramData.add(DefineReport.ValTenkabkbn.VAL1.getVal());
          paramData.add(seq);
          paramData.add(inputno);

          sqlcommand = DefineReport.ID_SQL_TENKABUTSU2.replace("@T", "INAMS.CSVTENKABUTSU").replace("@W", " and SEQ = ? and INPUTNO = ?");
        }
      } else {
        String sel_shocd = obj.optString("SEL_SHNCD");
        if (StringUtils.isNotEmpty(sel_shocd) && NumberUtils.isNumber(sel_shocd)) {
          paramData.add(DefineReport.ValTenkabkbn.VAL1.getVal());
          paramData.add(sel_shocd + "%");
          String szTable = "INAMS.MSTTENKABUTSU";

          String FromPage = (String) request.getSession().getAttribute("frompage");
          if (ArrayUtils.contains(new String[] {DefineReport.ID_PAGE_X247}, outpage)) {
            if (StringUtils.isEmpty(FromPage) || !FromPage.equals(DefineReport.ID_PAGE_X280)) {
              szTable = "INAWS.PIMTITENKABUTSU";
            }
          } else if (ArrayUtils.contains(new String[] {DefineReport.ID_PAGE_X251}, outpage) && FromPage.equals(DefineReport.ID_PAGE_X249)) {
            szTable = "INAWS.PIMTITENKABUTSU";
          } else if (ArrayUtils.contains(new String[] {DefineReport.ID_PAGE_X251}, outpage) && FromPage.equals(DefineReport.ID_PAGE_X250)) {
            szTable = "INAWS.PIMSITENKABUTSU";
          }
          String szWhere = " and SHNCD like ?";
          if (DefineReport.ValTablekbn.YYK.getVal().equals(obj.optString("TABLEKBN"))) {
            paramData.add(obj.optString("YOYAKUDT"));
            szTable += "_Y";
            szWhere += " and YOYAKUDT = ?";
          }
          sqlcommand = DefineReport.ID_SQL_TENKABUTSU2.replace("@T", szTable).replace("@W", szWhere);
        }
      }
      sqlcommand = sqlcommand.replaceAll("@M", DefineReport.SubGridRowNumber.TENKABUTSU.getVal());
    } else if (StringUtils.startsWith(outobj, DefineReport.Grid.FUTAI.getObj())) {
      JSONObject obj = (JSONObject) map.get(0);
      String sel_futai = obj.optString("FUTAI");
      // 空行用
      // sqlcommand = DefineReport.ID_SQL_GRD_EMPTY;
      if (StringUtils.isNotEmpty(sel_futai) && !("-1".equals(sel_futai))) {
        if (NumberUtils.isNumber(sel_futai)) {
          sqlcommand = DefineReport.ID_SQL_FUTAI + "'" + sel_futai + "'" + DefineReport.ID_SQL_FUTAI_TAIL;
        }
      }
      sqlcommand = sqlcommand.replaceAll("@M", DefineReport.SubGridRowNumber.TENKABUTSU.getVal());
    } else if (StringUtils.startsWith(outobj, DefineReport.Grid.KRYOFUTAI.getObj())) {
      JSONObject obj = (JSONObject) map.get(0);
      String txt_yobidashicd = obj.optString("YOBIDASHICD");
      String txt_bmncd = obj.optString("BMNCD");
      if (StringUtils.isNotEmpty(txt_yobidashicd) && StringUtils.isNotEmpty(txt_bmncd)) {
        sqlcommand = DefineReport.ID_SQL_KRYOFUTAI + "'" + txt_yobidashicd + "' and BMNCD = '" + txt_bmncd + "'";
        sqlcommand = sqlcommand.replaceAll("@M", DefineReport.SubGridRowNumber.TENKABUTSU.getVal());
        sqlcommand += DefineReport.ID_SQL_KRYOFUTAI_TAIL;
      }
    } else if (StringUtils.startsWith(outobj, DefineReport.Grid.SHNCD.getObj())) {
      JSONObject obj = (JSONObject) map.get(0);
      sqlcommand = "";
      String sqlWhere = "";
      String shncd = obj.optString("SHNCD");
      String shnkn = obj.optString("SHNKN");
      String ssircd = obj.optString("SSIRCD");
      String maker = obj.optString("MAKER");
      String kbn121 = StringUtils.equals(obj.optString("KBN121"), DefineReport.Values.NONE.getVal()) ? "" : obj.optString("KBN121");// obj.optString("KBN121");
      String kbn117 = StringUtils.equals(obj.optString("KBN117"), DefineReport.Values.NONE.getVal()) ? "" : obj.optString("KBN117");// obj.optString("KBN117");
      String kbn105 = StringUtils.equals(obj.optString("KBN105"), DefineReport.Values.NONE.getVal()) ? "" : obj.optString("KBN105");// obj.optString("KBN105");
      String bumon = StringUtils.equals(obj.optString("BUMON"), DefineReport.Values.NONE.getVal()) ? "" : obj.optString("BUMON");
      String daibun = StringUtils.equals(obj.optString("DAI_BUN"), DefineReport.Values.NONE.getVal()) ? "" : obj.optString("DAI_BUN");
      String chubun = StringUtils.equals(obj.optString("CHU_BUN"), DefineReport.Values.NONE.getVal()) ? "" : obj.optString("CHU_BUN");// obj.optString("KBN105");
      sqlcommand = DefineReport.ID_SQL_SHN_SHNCD;

      sqlWhere = DefineReport.ID_SQL_CMN_WHERE;
      if (StringUtils.isNotEmpty(shncd)) {
        sqlWhere += " and SHNCD = '" + shncd + "'";
      }
      if (StringUtils.isNotEmpty(bumon)) {
        sqlWhere += " and BMNCD = '" + bumon + "'";
      }
      if (StringUtils.isNotEmpty(daibun)) {
        sqlWhere += " and DAICD = '" + daibun + "'";
      }
      if (StringUtils.isNotEmpty(chubun)) {
        sqlWhere += " and CHUCD = '" + chubun + "'";
      }
      if (StringUtils.isNotEmpty(shnkn)) {
        sqlWhere += " and SHNKN like '%" + shnkn + "%'";
      }
      if (StringUtils.isNotEmpty(ssircd)) {
        sqlWhere += " and SSIRCD = '" + ssircd + "'";
      }
      if (StringUtils.isNotEmpty(maker)) {
        sqlWhere += " and MAKERCD = '" + maker + "'";
      }
      if (StringUtils.isNotEmpty(kbn121)) {
        sqlWhere += " and TEIKANKBN = '" + kbn121 + "'";
      }
      if (StringUtils.isNotEmpty(kbn117)) {
        sqlWhere += " and TEIKEIKBN = '" + kbn117 + "'";
      }
      if (StringUtils.isNotEmpty(kbn105)) {
        sqlWhere += " and SHNKBN = '" + kbn105 + "'";
      }
      sqlcommand += sqlWhere + DefineReport.ID_SQL_SHN_SHNCD_TAIL;
    } else if (StringUtils.startsWith(outobj, DefineReport.InpText.DE_MAXSU.getObj())) {
      sqlcommand = DefineReport.ID_SQL_DE_MAXSU;
    } else if (StringUtils.startsWith(outobj, DefineReport.InpText.MAXSU.getObj())) {
      sqlcommand = DefineReport.ID_SQL_MAXSU;
    } else if (StringUtils.startsWith(outobj, DefineReport.Grid.GROUP.getObj())) {
      JSONObject obj = (JSONObject) map.get(0);
      String sel_shocd = obj.optString("SEL_SHNCD");
      // 空行用
      sqlcommand = DefineReport.ID_SQL_GRD_EMPTY;
      if (StringUtils.isNotEmpty(sel_shocd)) {
        // 予約情報を取得する場合
        if (DefineReport.ValTablekbn.YYK.getVal().equals(obj.optString("TABLEKBN"))) {
          String yoyakudt = obj.optString("YOYAKUDT");
          if (StringUtils.isNotEmpty(yoyakudt)) {
            paramData.add(sel_shocd + "%");
            paramData.add(yoyakudt);
            sqlcommand = DefineReport.ID_SQL_MSTGRP_Y;
          }
        } else {
          paramData.add(sel_shocd + "%");
          sqlcommand = DefineReport.ID_SQL_MSTGRP;
        }
      }
      // 最大行数
      String max_row = DefineReport.SubGridRowNumber.MSTGRP.getVal();
      sqlcommand = sqlcommand.replaceAll("@M", max_row);
      // 配送店グループ
    } else if (StringUtils.startsWith(outobj, DefineReport.Grid.HSTGP.getObj())) {

      JSONObject obj = (JSONObject) map.get(0);
      String txt_hsgpcd = obj.optString("HSGPCD");
      String flg = obj.optString("FLG"); // 更新:0 参照:1

      // 空行用
      sqlcommand = DefineReport.ID_SQL_GRD_EMPTY;
      // 最大行数
      String max_row = DefineReport.SubGridRowNumber.HSTGP.getVal();
      sqlcommand = sqlcommand.replaceAll("@M", max_row);

      if (!StringUtils.isEmpty(txt_hsgpcd)) {
        if (flg.equals("0")) {
          paramData.add(txt_hsgpcd);
          paramData.add(DefineReport.ValUpdkbn.NML.getVal());
          sqlcommand = DefineReport.ID_SQL_HSTENGP2;
        } else {
          paramData.add(DefineReport.ValUpdkbn.NML.getVal());
          paramData.add(txt_hsgpcd);
          paramData.add(DefineReport.ValUpdkbn.NML.getVal());
          paramData.add(DefineReport.ValUpdkbn.NML.getVal());
          sqlcommand = DefineReport.ID_SQL_HSTENGP3;
        }
        sqlcommand = sqlcommand.replaceAll("@M", max_row);
      }

      // 配送グループ店
    } else if (StringUtils.startsWith(outobj, DefineReport.Grid.HSGPT.getObj())) {

      JSONObject obj = (JSONObject) map.get(0);
      String txt_hsgpcd = obj.optString("HSGPCD");
      String txt_tengpcd = obj.optString("HSTENGPCD");

      // 空行用
      sqlcommand = DefineReport.ID_SQL_GRD_EMPTY;
      // 最大行数
      String max_row = DefineReport.SubGridRowNumber.HSGPT.getVal();
      sqlcommand = sqlcommand.replaceAll("@M", max_row);

      if (!StringUtils.isEmpty(txt_hsgpcd) && !StringUtils.isEmpty(txt_tengpcd)) {
        paramData.add(txt_hsgpcd);
        paramData.add(txt_tengpcd);
        sqlcommand = DefineReport.ID_SQL_TENPO_HG2;
        sqlcommand = sqlcommand.replaceAll("@M", max_row);
      }

      // プライスカード発行枚数
    } else if (StringUtils.startsWith(outobj, DefineReport.Grid.PCARDSU.getObj())) {
      JSONObject obj = (JSONObject) map.get(0);
      String inputno = obj.optString("INPUTNO");

      // 空行用
      sqlcommand = DefineReport.ID_SQL_GRD_EMPTY;
      // 最大行数
      String max_row = DefineReport.SubGridRowNumber.PCARDMAISU.getVal();
      sqlcommand = sqlcommand.replaceAll("@M", max_row);

      if (!StringUtils.isEmpty(inputno) && !StringUtils.isEmpty(inputno)) {
        paramData.add(inputno);
        paramData.add(DefineReport.ValUpdkbn.NML.getVal());
        sqlcommand = DefineReport.ID_SQL_TRNPCARDMAISU;
        sqlcommand = sqlcommand.replaceAll("@M", max_row);
      }
      // B/M番号一覧
    } else if (StringUtils.startsWith(outobj, DefineReport.Grid.BMNNO.getObj())) {

      JSONObject obj = (JSONObject) map.get(0);
      String txt_moyskbn = obj.optString("MOYSKBN");
      String txt_moysstdt = obj.optString("MOYSSTDT");
      String txt_moysrban = obj.optString("MOYSRBAN");

      if (!StringUtils.isEmpty(txt_moyskbn) && !StringUtils.isEmpty(txt_moysstdt) && !StringUtils.isEmpty(txt_moysrban)) {
        paramData.add(txt_moyskbn);
        paramData.add(txt_moysstdt.substring(2));
        paramData.add(txt_moysrban);
        sqlcommand = DefineReport.ID_SQL_BMNNO;
      }
      // セット番号一覧
    } else if (StringUtils.startsWith(outobj, DefineReport.Grid.SETNO.getObj())) {

      JSONObject obj = (JSONObject) map.get(0);
      String txt_moyskbn = obj.optString("MOYSKBN");
      String txt_moysstdt = obj.optString("MOYSSTDT");
      String txt_moysrban = obj.optString("MOYSRBAN");

      // 空行用
      sqlcommand = DefineReport.ID_SQL_GRD_EMPTY;
      // 最大行数
      String max_row = DefineReport.SubGridRowNumber.DEF.getVal();
      sqlcommand = sqlcommand.replaceAll("@M", max_row);
      if (!StringUtils.isEmpty(txt_moyskbn) && !StringUtils.isEmpty(txt_moysstdt) && !StringUtils.isEmpty(txt_moysrban)) {
        paramData.add(txt_moyskbn);
        paramData.add(txt_moysstdt.substring(2));
        paramData.add(txt_moysrban);
        sqlcommand = DefineReport.ID_SQL_SETNO;
        sqlcommand = sqlcommand.replaceAll("@M", max_row);
      }

      // B/M商品一覧
    } else if (StringUtils.startsWith(outobj, DefineReport.Grid.BMSHN.getObj())) {

      JSONObject obj = (JSONObject) map.get(0);
      String txt_moyskbn = obj.optString("MOYSKBN");
      String txt_moysstdt = obj.optString("MOYSSTDT");
      String txt_moysrban = obj.optString("MOYSRBAN");
      String txt_bmnno = obj.optString("BMNNO");

      // 空行用
      sqlcommand = DefineReport.ID_SQL_GRD_EMPTY;
      // 最大行数
      String max_row = DefineReport.SubGridRowNumber.BMSHN.getVal();
      sqlcommand = sqlcommand.replaceAll("@M", max_row);

      if (!StringUtils.isEmpty(txt_moyskbn) && !StringUtils.isEmpty(txt_moyskbn) && !StringUtils.isEmpty(txt_moysstdt) && !StringUtils.isEmpty(txt_moysstdt) && !StringUtils.isEmpty(txt_moysrban)
          && !StringUtils.isEmpty(txt_moysrban) && !StringUtils.isEmpty(txt_bmnno) && !StringUtils.isEmpty(txt_bmnno)) {
        paramData.add(txt_moyskbn);
        paramData.add(txt_moysstdt.substring(2));
        paramData.add(txt_moysrban);
        paramData.add(txt_bmnno);
        sqlcommand = DefineReport.ID_SQL_BMSHN;
        sqlcommand = sqlcommand.replaceAll("@M", max_row);
      }
    } else if (StringUtils.startsWith(outobj, DefineReport.Grid.TENHTSU_ARR.getObj())) {
      JSONObject obj = (JSONObject) map.get(0);

      if (StringUtils.equals(obj.optString("callpage"), DefineReport.ID_PAGE_JU013) || StringUtils.equals(obj.optString("callpage"), DefineReport.ID_PAGE_JU033)) {

        String JNDIname = Defines.STR_JNDI_DS;
        User userInfo = (User) session.getAttribute(Consts.STR_SES_LOGINUSER);
        Iterator keys = obj.keys();
        HashMap hsmap = new HashMap<String, String>();

        while (keys.hasNext()) {
          String key = (String) keys.next();
          hsmap.put(key, obj.get(key));
        }

        // 発注数量
        sqlcommand = new ReportJU033Dao(JNDIname).createCommandSub(hsmap, userInfo);
      } else if (StringUtils.equals(obj.optString("callpage"), DefineReport.ID_PAGE_JU012)) {
        sqlcommand =
            "select TEN.TENCD, TEN.TENKN, null from INAMS.MSTTEN TEN where TEN.UPDKBN = " + DefineReport.ValUpdkbn.NML.getVal() + " and TEN.TENCD <= 400 and TEN.MISEUNYOKBN <> 9 order by TEN.TENCD";
      } else if (StringUtils.equals(obj.optString("callpage"), DefineReport.ID_PAGE_JU032)) {
        sqlcommand =
            "select TEN.TENCD, TEN.TENKN, null from INAMS.MSTTEN TEN where TEN.UPDKBN = " + DefineReport.ValUpdkbn.NML.getVal() + " and TEN.TENCD < 800 and TEN.MISEUNYOKBN <> 9 order by TEN.TENCD";
      }

      // 正規定量商品店一覧
    } else if (StringUtils.startsWith(outobj, DefineReport.Grid.HATSTRSHNTEN.getObj())) {

      // 最大行数
      String max_row = DefineReport.SubGridRowNumber.TENPO.getVal();
      if (DefineReport.ID_PAGE_TR001.equals(outpage)) {
        sqlcommand = DefineReport.ID_SQL_HATSTRSHNTEN;
      } else {
        JSONObject obj = (JSONObject) map.get(0);
        String txt_shncd = obj.optString("SHNCD");
        String txt_binkbn = obj.optString("BINKBN");
        if (!StringUtils.isEmpty(txt_shncd) && !StringUtils.isEmpty(txt_binkbn)) {
          paramData.add(txt_shncd);
          paramData.add(txt_binkbn);
          sqlcommand = DefineReport.ID_SQL_HATSTRSHNTEN2;
        }
      }
      sqlcommand = sqlcommand.replaceAll("@M", max_row);

      // 店情報
    } else if (StringUtils.startsWith(outobj, DefineReport.Grid.TENINFO.getObj())) {

      JSONObject obj = (JSONObject) map.get(0);

      String JNDIname = Defines.STR_JNDI_DS;
      User userInfo = (User) session.getAttribute(Consts.STR_SES_LOGINUSER);
      Iterator keys = obj.keys();
      HashMap hsmap = new HashMap<String, String>();

      while (keys.hasNext()) {
        String key = (String) keys.next();
        hsmap.put(key, obj.get(key));
      }
      sqlcommand = new ReportST008Dao(JNDIname).createCommandSub(hsmap, userInfo);

      // 店番一括入力
    } else if (StringUtils.startsWith(outobj, DefineReport.Grid.TENCDIINPUT.getObj())) {

      // 空行用
      sqlcommand = DefineReport.ID_SQL_GRD_EMPTY;
      // 最大行数
      String max_row = DefineReport.SubGridRowNumber.HSTGP.getVal();
      sqlcommand = sqlcommand.replaceAll("@M", max_row);
      sqlcommand = DefineReport.ID_SQL_TENCDIINPUT;
      sqlcommand = sqlcommand.replaceAll("@M", max_row);

      // 率パターン店別分配率
    } else if (StringUtils.startsWith(outobj, DefineReport.Grid.RTPTNTENBETUBRT.getObj())) {
      JSONObject obj = (JSONObject) map.get(0);
      String sendBtnid = obj.optString("SENDBTNID");
      String txt_bmncd = obj.optString("BMNCD");
      String rad_wwmmflg = obj.optString("WWMMFLG");
      String txt_yymm = obj.optString("YYMM");
      String txt_daicd = obj.optString("DAICD");
      String txt_chucd = obj.optString("CHUCD");
      String txt_rtptnno = obj.optString("RTPTNNO");
      // 空行用
      sqlcommand = DefineReport.ID_SQL_GRD_EMPTY;
      // 最大行数
      String max_row = DefineReport.SubGridRowNumber.HSTGP.getVal();
      sqlcommand = sqlcommand.replaceAll("@M", max_row);

      if (!StringUtils.isEmpty(sendBtnid)) {
        if (DefineReport.Button.SEL_TENBETUBRT.getObj().equals(sendBtnid)) {

          paramData.add(txt_bmncd);
          paramData.add(txt_bmncd);
          paramData.add(rad_wwmmflg);
          paramData.add(txt_yymm);
          paramData.add(txt_daicd);
          paramData.add(txt_chucd);

          sqlcommand = ReportRP006Dao.ID_SQL_TENBETUBRT_RP010;
        } else if (DefineReport.Button.NEW.getObj().equals(sendBtnid)) {
          // 通常率 新規の場合
          paramData.add(txt_bmncd);
          paramData.add(txt_bmncd);
          sqlcommand = ReportRP006Dao.ID_SQL_TENBETUBRT_RP006_NEW;
        } else {
          // 通常率 変更・参照の場合
          paramData.add(txt_bmncd);
          paramData.add(txt_bmncd);
          paramData.add(txt_rtptnno);
          paramData.add(txt_bmncd);
          sqlcommand = ReportRP006Dao.ID_SQL_TENBETUBRT_RP006_UPD;
        }
      }
      sqlcommand = sqlcommand.replaceAll("@M", max_row);


      // ランク別数量
    } else if (StringUtils.startsWith(outobj, DefineReport.Grid.RANKSURYO.getObj())) {

      JSONObject obj = (JSONObject) map.get(0);
      String sendBtnid = obj.optString("SENDBTNID");
      String txt_bmncd = obj.optString("BMNCD");
      String chk_rinji = obj.optString("RINJI");
      String txt_moyscd = obj.optString("MOYSCD");
      String txt_sryptnno = obj.optString("SRYPTNNO");

      // 空行用
      sqlcommand = DefineReport.ID_SQL_GRD_EMPTY;
      // 最大行数
      String max_row = DefineReport.SubGridRowNumber.HSTGP.getVal();
      sqlcommand = sqlcommand.replaceAll("@M", max_row);

      if (!StringUtils.isEmpty(sendBtnid)) {
        if (StringUtils.equals(DefineReport.Button.NEW.getObj(), sendBtnid)) {
          // 新規の場合
          sqlcommand = DefineReport.ID_SQL_RANKSURYO_NEW;

        } else if (StringUtils.equals(DefineReport.Button.SEL_CHANGE.getObj(), sendBtnid) || StringUtils.equals(DefineReport.Button.SEL_REFER.getObj(), sendBtnid)) {
          // 変更・参照の場合
          if (!StringUtils.isEmpty(chk_rinji)) {
            if (StringUtils.equals("1", chk_rinji)) {
              // 臨時チェックありの場合
              paramData.add(txt_bmncd); // 部門
              paramData.add(txt_sryptnno); // 数量パターンNo.
              paramData.add(StringUtils.substring(txt_moyscd, 0, 1));
              paramData.add(StringUtils.substring(txt_moyscd, 1, 7));
              paramData.add(StringUtils.substring(txt_moyscd, 7, 10));
              sqlcommand = DefineReport.ID_SQL_RANKSURYO_CHANGE_EX;

            } else {
              // 臨時チェックなしの場合
              paramData.add(txt_bmncd); // 部門
              paramData.add(txt_sryptnno); // 数量パターンNo.
              sqlcommand = DefineReport.ID_SQL_RANKSURYO_CHANGE;
            }
          }
        }
      }
      sqlcommand = sqlcommand.replaceAll("@M", max_row);
      // ランク別数量
    } else if (StringUtils.equals(outobj, DefineReport.Grid.SET.getObj() + "_list")) {
      JSONObject obj = (JSONObject) map.get(0);
      // 空行用

      // 最大行数
      String txt_moyskbn = obj.optString("MOYSKBN");
      String txt_moysstdt = obj.optString("MOYSSTDT");
      String txt_moysrban = obj.optString("MOYSRBAN");
      String txt_stno = obj.optString("SETNO");
      String max_row = DefineReport.SubGridRowNumber.SET.getVal();

      if (StringUtils.isEmpty(txt_stno)) {
        sqlcommand = DefineReport.ID_SQL_GRD_EMPTY;
      } else {
        sqlcommand = DefineReport.ID_SQL_SETNOLIST;
        paramData.add(txt_moyskbn); // 催し区分
        paramData.add(txt_moysstdt); // 催し開始日
        paramData.add(txt_moysrban); // 催し連番
        paramData.add(txt_stno); // セット番号
        paramData.add("1"); // 割引グループID
      }
      sqlcommand = sqlcommand.replaceAll("@M", max_row);
    } else if (StringUtils.equals(outobj, DefineReport.Grid.SET2.getObj() + "_list")) {
      JSONObject obj = (JSONObject) map.get(0);
      // 空行用

      // 最大行数
      String txt_moyskbn = obj.optString("MOYSKBN");
      String txt_moysstdt = obj.optString("MOYSSTDT");
      String txt_moysrban = obj.optString("MOYSRBAN");
      String txt_stno = obj.optString("SETNO");
      String max_row = DefineReport.SubGridRowNumber.SET.getVal();

      if (StringUtils.isEmpty(txt_stno)) {
        sqlcommand = DefineReport.ID_SQL_GRD_EMPTY;
      } else {
        sqlcommand = DefineReport.ID_SQL_SETNOLIST;
        paramData.add(txt_moyskbn); // 催し区分
        paramData.add(txt_moysstdt); // 催し開始日
        paramData.add(txt_moysrban); // 催し連番
        paramData.add(txt_stno); // セット番号
        paramData.add("2"); // 割引グループID
      }
      sqlcommand = sqlcommand.replaceAll("@M", max_row);
    } else if (StringUtils.startsWith(outobj, DefineReport.Grid.BUMONYOSAN.getObj())) {
      sqlcommand = DefineReport.ID_SQL_BUMONYOSAN;

    } else if (StringUtils.startsWith(outobj, DefineReport.Grid.SEL_VIEW.getObj() + "_main")) {
      JSONObject obj = (JSONObject) map.get(0);
      String listNo = obj.optString("LISTNO");
      String bmnCd = StringUtils.isEmpty(obj.optString("BMNCD")) ? "" : obj.optString("BMNCD").substring(0, 2);
      String outRowIndex = obj.containsKey("OUTROWINDEX") ? obj.optString("OUTROWINDEX") : "";
      JSONArray arr = StringUtils.isEmpty(obj.optString("INPDAYARR")) ? new JSONArray() : obj.optJSONArray("INPDAYARR");

      String JNDIname = Defines.STR_JNDI_DS;
      User userInfo = (User) session.getAttribute(Consts.STR_SES_LOGINUSER);
      HashMap hsmap = new HashMap<String, String>();

      hsmap.put("LSTNO", listNo);
      hsmap.put("BMNCD", bmnCd);
      hsmap.put("OUTROWINDEX", outRowIndex);
      hsmap.put("INPDAYARR", arr);

      // 呼出ウィンドウによって実行するSQLを変更
      if (DefineReport.ID_PAGE_TJ003.equals(outpage)) {
        ReportTJ003Dao dao = new ReportTJ003Dao(JNDIname);
        sqlcommand = dao.createCommandSub2(hsmap, userInfo);
        paramData = dao.getParamData();
      } else {
        ReportTJ007Dao dao = new ReportTJ007Dao(JNDIname);
        sqlcommand = dao.createCommandSub(hsmap, userInfo);
        paramData = dao.getParamData();
      }
    } else if (StringUtils.startsWith(outobj, DefineReport.Grid.KOUSEIHI.getObj() + "_main")) {
      JSONObject obj = (JSONObject) map.get(0);
      String listNo = obj.optString("LSTNO");
      String bmnCd = StringUtils.isEmpty(obj.optString("BMNCD")) ? "" : obj.optString("BMNCD").substring(0, 2);

      String JNDIname = Defines.STR_JNDI_DS;
      User userInfo = (User) session.getAttribute(Consts.STR_SES_LOGINUSER);
      HashMap hsmap = new HashMap<String, String>();

      hsmap.put("LSTNO", listNo);
      hsmap.put("BMNCD", bmnCd);

      // 呼出ウィンドウによって実行するSQLを変更
      if (DefineReport.ID_PAGE_TJ002.equals(outpage)) {
        ReportTJ002Dao dao = new ReportTJ002Dao(JNDIname);
        sqlcommand = dao.createCommandSub(hsmap, userInfo);
        paramData = dao.getParamData();
      } else {
        ReportTJ006Dao dao = new ReportTJ006Dao(JNDIname);
        sqlcommand = dao.createCommandSub2(hsmap, userInfo);
        paramData = dao.getParamData();
      }
    }


    // サブ画面(内容の異なる複数サブ画面が一つになっている場合や、使い回ししないサブ画面などに利用)
    if (StringUtils.startsWith(outobj, DefineReport.Grid.SUB.getObj())) {
      // 販売店/納入店確認
      if (StringUtils.startsWith(outobj, DefineReport.Grid.SUB.getObj() + "_winTG018")) {
        JSONObject obj = (JSONObject) map.get(0);
        String txt_moyskbn = obj.optString("MOYSKBN");
        String txt_moysstdt = obj.optString("MOYSSTDT");
        String txt_moysrban = obj.optString("MOYSRBAN");
        String txt_bmncd = obj.optString("BMNCD");
        String txt_kanrino = obj.optString("KANRINO"); // 管理No.
        String txt_kanrieno = obj.optString("KANRIENO"); // 管理No.枝番
        String txt_nndt = obj.optString("NNDT"); // 納入日(納入店確認時)
        String btnid = obj.optString("BTNID"); // ボタンID(販売店/納入店)

        if (StringUtils.isNotEmpty(txt_moyskbn) && StringUtils.isNotEmpty(txt_moysstdt) && StringUtils.isNotEmpty(txt_moysrban) && StringUtils.isNotEmpty(txt_bmncd)
            && StringUtils.isNotEmpty(txt_kanrino) && StringUtils.isNotEmpty(txt_kanrieno)
            && (StringUtils.endsWith(btnid, "_h") || (StringUtils.endsWith(btnid, "_n") && StringUtils.isNotEmpty(txt_nndt)))) {
          paramData.add(txt_moyskbn);
          paramData.add(txt_moysstdt);
          paramData.add(txt_moysrban);
          paramData.add(txt_bmncd);
          paramData.add(txt_kanrino);
          paramData.add(txt_kanrieno);
          String sqlWhere = " where MOYSKBN = ? and MOYSSTDT = ? and MOYSRBAN = ? and BMNCD = ? and KANRINO = ? and KANRIENO = ?";
          ReportTG016Dao dao = new ReportTG016Dao(jndiName);
          if (StringUtils.endsWith(btnid, "_h")) { // 販売店確認
            if (dao.isTOKTG(txt_moyskbn, txt_moysrban)) { // 全店特売アンケート有無
              sqlcommand = "with WK as (select TENATSUK_ARR as ARR, 1 as LEN from INATK.TOKTG_HB" + sqlWhere + ")";
            } else {
              sqlcommand = "with WK as (select TENATSUK_ARR as ARR, 1 as LEN from INATK.TOKSP_HB" + sqlWhere + ")";
            }
          } else if (StringUtils.endsWith(btnid, "_n")) { // 納入店確認
            paramData.add(txt_nndt);
            sqlWhere += " and NNDT=? ";

            if (dao.isTOKTG(txt_moyskbn, txt_moysrban)) { // 全店特売アンケート有無
              sqlcommand = "with WK as (select TENHTSU_ARR as ARR, 5 as LEN from INATK.TOKTG_NNDT" + sqlWhere + ")";
            } else {
              sqlcommand = "with WK as (select TENHTSU_ARR as ARR, 5 as LEN from INATK.TOKSP_NNDT" + sqlWhere + ")";
            }
          }
          sqlcommand += DefineReport.ID_SQL_ARR_CMN;
          sqlcommand += "select T1.IDX as F1, T2.TENKN as F2, RNK as F3 from ARRWK T1 inner join INAMS.MSTTEN T2 on T1.IDX = T2.TENCD and length(trim(T1.RNK)) > 0";
        }
      }

      // 通常率パターン 選択
      if (StringUtils.startsWith(outobj, DefineReport.Grid.SUB.getObj() + "_winST012")) {
        JSONObject obj = (JSONObject) map.get(0);
        String kbn = obj.optString("KBN");
        String txt_bmncd = obj.optString("BMNCD");
        if (DefineReport.ValKbn10007.VAL1.getVal().equals(kbn)) {
          if (StringUtils.isNotEmpty(txt_bmncd)) {
            paramData.add(txt_bmncd);
            sqlcommand = DefineReport.ID_SQL_RTPTN;
          }
        } else if (DefineReport.ValKbn10007.VAL2.getVal().equals(kbn)) {
          String chk_rinji = obj.optString("RINJI");
          if (StringUtils.isNotEmpty(txt_bmncd)) {
            String txt_moyskbn = obj.optString("MOYSKBN");
            String txt_moysstdt = obj.optString("MOYSSTDT");
            String txt_moysrban = obj.optString("MOYSRBAN");
            if (DefineReport.Values.ON.getVal().equals(chk_rinji)) {
              ;
              if (StringUtils.isNotEmpty(txt_moyskbn) && StringUtils.isNotEmpty(txt_moysstdt) && StringUtils.isNotEmpty(txt_moysrban)) {
                paramData.add(txt_bmncd);
                paramData.add(txt_moyskbn);
                paramData.add(txt_moysstdt);
                paramData.add(txt_moysrban);
                sqlcommand = DefineReport.ID_SQL_SURYO2EX;
              }
            } else {
              paramData.add(txt_bmncd);
              sqlcommand = DefineReport.ID_SQL_SURYO2;
            }
            if (sqlcommand.length() > 0) {
              String rankno_add = obj.optString("RANKNO_ADD");
              String rankno_del = obj.optString("RANKNO_DEL");
              JSONArray tencdAdds = obj.optJSONArray("TENCD_ADDS");
              JSONArray rankAdds = obj.optJSONArray("TENRANK_ADDS");
              JSONArray tencdDels = obj.optJSONArray("TENCD_DELS");
              String saveTenrankArr = obj.optString("TENRANK_ARR");
              // ランクNo展開配列作成機能
              ReportBM015Dao dao = new ReportBM015Dao(jndiName);
              ArrayList<String> tenranks = dao.getTenrankArray(txt_bmncd, txt_moyskbn, txt_moysstdt, txt_moysrban, rankno_add, rankno_del, tencdAdds, rankAdds, tencdDels, saveTenrankArr);
              TreeMap<String, Integer> rankcnts = dao.getTenrankCount(tenranks);

              String[] titles = new String[] {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
              String colsql = "", colsql2 = "";
              int colidx = 4;
              for (String title : titles) {
                colsql += ",sum(case T2.TENRANK when '" + title + "' then T2.SURYO end) as F" + colidx;
                colsql2 += "," + rankcnts.get(title);
                colidx++;
              }
              int sum = rankcnts.getOrDefault("SUM", 0);
              String addrowsql = " union all select null as F1, '店舗数' as F2, " + sum + colsql2 + ", -1 from sysibm.sysdummy1";
              sqlcommand = StringUtils.replace(sqlcommand, "@C", colsql) + addrowsql + " order by IDX";
            }
          }
        }
      }
      if (StringUtils.startsWith(outobj, DefineReport.Grid.SUB.getObj() + "_winHT005")) {
        JSONObject obj = (JSONObject) map.get(0);
        String ten = obj.optString("TEN");
        String shuno = obj.optString("SHUNO");
        paramData.add(ten);

        if (StringUtils.isEmpty(shuno)) {
          sqlcommand = DefineReport.ID_SQL_HATSTRTEN;
        } else {
          sqlcommand = DefineReport.ID_SQL_HATJTRTEN;
          paramData.add(shuno);
        }
      }

      // 自動発注
      if (StringUtils.equals(outobj, DefineReport.Grid.SUB.getObj() + "_winIT032")) {
        JSONObject obj = (JSONObject) map.get(0);
        String sztable = "INAMS.MSTAHS";
        String szWhere = "";
        if (DefineReport.ValTablekbn.CSV.getVal().equals(obj.optString("TABLEKBN"))) {
          sztable = "INAMS.CSVMSTAHS";
          paramData.add(obj.optString("SEQ"));
          paramData.add(obj.optString("INPUTNO"));
          szWhere += " and SEQ = ? and INPUTNO = ?";
        } else {
          String shncd = obj.optString("SHNCD");
          szWhere = " and SHNCD like ? ";
          if (StringUtils.isNotEmpty(shncd)) {
            paramData.add(shncd + "%");
          } else {
            paramData.add("");
          }
          if (DefineReport.ValTablekbn.YYK.getVal().equals(obj.optString("TABLEKBN"))) {
            sztable += "_Y";
            String yoyakudt = obj.optString("YOYAKUDT");
            if (StringUtils.isNotEmpty(yoyakudt)) {
              szWhere += " and YOYAKUDT = ? ";
              paramData.add(yoyakudt);
            } else {
              szWhere += " and YOYAKUDT = -1 ";
            }
          }
        }
        sqlcommand = DefineReport.ID_SQL_AHS.replace("@T", sztable).replace("@W", szWhere);
      }
    }

    // 週間発注計画_計画計表示
    if (StringUtils.equals(outobj, DefineReport.Grid.KEIKAKU.getObj() + "_winTJ010")) {
      JSONObject obj = (JSONObject) map.get(0);

      User userInfo = (User) session.getAttribute(Consts.STR_SES_LOGINUSER);
      String listno = obj.optString("LSTNO");
      String bmncd = obj.optString("BMNCD");
      String tencd = userInfo.getTenpo();

      sqlcommand = DefineReport.ID_SQL_KKH_TJ009;

      paramData.add(listno);
      paramData.add(bmncd);
      paramData.add(tencd);
    }

    // サブ画面(通常画面をサブ画面として使う場合)
    if (StringUtils.contains(outobj, DefineReport.Grid.SUBWINDOW.getObj())) {
      JSONObject obj = (JSONObject) map.get(0);

      String JNDIname = Defines.STR_JNDI_DS;
      User userInfo = (User) session.getAttribute(Consts.STR_SES_LOGINUSER);
      Iterator keys = obj.keys();
      HashMap hsmap = new HashMap<String, String>();


      while (keys.hasNext()) {
        String key = (String) keys.next();
        hsmap.put(key, obj.get(key));
      }

      if (StringUtils.equals(outobj, DefineReport.Grid.TENKAKUNIN.getObj())) {
        // 対象店確認
        sqlcommand = new ReportBM015Dao(JNDIname).createCommandSub(hsmap, userInfo);

      } else if (StringUtils.equals(outobj, DefineReport.Grid.RANKTENINFO.getObj())) {
        // ランクNo確認
        sqlcommand = new ReportST007Dao(JNDIname).createCommandSub(hsmap, userInfo);

      } else if (StringUtils.equals(outobj, DefineReport.Grid.SUBWINDOWTENIFO.getObj())) {
        // 店情報
        // sqlcommand = new ReportST007Dao(JNDIname).createCommandSub(hsmap, userInfo);
        sqlcommand = new ReportST008Dao(JNDIname).createCommandSub(hsmap, userInfo);

      } else if (StringUtils.equals(outobj, DefineReport.Grid.ZITREF.getObj())) {

        // 実績参照
        ReportST011Dao dao = new ReportST011Dao(JNDIname);
        sqlcommand = dao.createCommandSub(hsmap, userInfo);
        paramData = dao.getParamData();

      } else if (StringUtils.equals(outobj, DefineReport.Grid.RINZIRANKNO.getObj())) {
        sqlcommand = new ReportST010Dao(JNDIname).createCommandSub(hsmap, userInfo);

      } else if (StringUtils.equals(outobj, DefineReport.Grid.SURYO.getObj())) {
        sqlcommand = new ReportJU017Dao(JNDIname).createCommandSub(hsmap, userInfo);

      } else if (StringUtils.equals(outobj, DefineReport.Grid.SEL_VIEW.getObj() + "_winTJ003") || StringUtils.equals(outobj, DefineReport.Grid.SEL_VIEW.getObj() + "_winTJ007")) {
        // 対象店確認
        ReportTJ003Dao dao = new ReportTJ003Dao(JNDIname);
        sqlcommand = dao.createCommandSub(hsmap, userInfo);
        paramData = dao.getParamData();

      } else if (StringUtils.equals(outobj, DefineReport.Grid.KOUSEIHI.getObj() + "_winTJ002") || StringUtils.equals(outobj, DefineReport.Grid.KOUSEIHI.getObj() + "_winTJ006")) {

        ReportTJ006Dao dao = new ReportTJ006Dao(JNDIname);
        sqlcommand = dao.createCommandSub(hsmap, userInfo);
        paramData = dao.getParamData();

      }
    }


    /* 販促 */
    if (StringUtils.startsWith(outobj, DefineReport.Grid.MOYCD_R.getObj()) || StringUtils.startsWith(outobj, DefineReport.Grid.MOYCD_S.getObj())
        || StringUtils.startsWith(outobj, DefineReport.Grid.MOYCD_T.getObj())) {
      JSONObject obj = (JSONObject) map.get(0);
      String sel_shuno = obj.optString("SEL_SHUNO");
      String moyskbn = obj.optString("MOYSKBN");
      // 共通
      sqlcommand += DefineReport.ID_SQL_GRD_CMN0.replace("@M", DefineReport.SubGridRowNumber.MOYCD.getVal() + " ");
      if (StringUtils.isNotEmpty(sel_shuno)) {
        HashMap<String, String> hmap = new HashMap<String, String>();
        hmap.put("SEL_SHUNO", sel_shuno);
        hmap.put("MOYSKBN", moyskbn);
        JSONObject inf = new ReportTM002Dao(jndiName).createSqlSelTOKMOYCD(hmap);
        JSONArray prm = inf.optJSONArray("PRM");
        for (int i = 0; i < prm.size(); i++) {
          paramData.add(prm.optString(i));
        }
        sqlcommand += "select T2.* from T1 left outer join (" + inf.optString("SQL") + ") T2 on T1.IDX = T2.IDX order by T2.MOYSKBN,T2.MOYSSTDT,T2.MOYSSTDT";
      } else {
        sqlcommand += " select IDX from T1";
      }
    }
    // ランクNo.
    if (StringUtils.startsWith(outobj, DefineReport.Grid.RANKNO.getObj())) {
      JSONObject obj = (JSONObject) map.get(0);
      String txt_bmncd = obj.optString("BMNCD");
      String chk_rinji = obj.optString("RINJI");
      if (StringUtils.isNotEmpty(txt_bmncd)) {
        if (DefineReport.Values.ON.getVal().equals(chk_rinji)) {
          String txt_moyskbn = obj.optString("MOYSKBN");
          String txt_moysstdt = obj.optString("MOYSSTDT");
          String txt_moysrban = obj.optString("MOYSRBAN");
          paramData.add(txt_bmncd);
          paramData.add(txt_moyskbn);
          paramData.add(txt_moysstdt);
          paramData.add(txt_moysrban);
          sqlcommand = DefineReport.ID_SQL_RANKEX;
        } else {
          paramData.add(txt_bmncd);
          sqlcommand = DefineReport.ID_SQL_RANK;
        }
      }
    }
    // 数値展開
    if (StringUtils.startsWith(outobj, DefineReport.Grid.TENKAI.getObj())) {
      JSONObject obj = (JSONObject) map.get(0);
      String txt_bmncd = obj.optString("BMNCD");
      if (StringUtils.isNotEmpty(txt_bmncd)) {
        paramData.add(txt_bmncd);
        sqlcommand = DefineReport.ID_SQL_TOKMOYDEF2 + " and BMNCD = ? ";
      }
    }
    // 店別数量
    if (StringUtils.startsWith(outobj, DefineReport.Grid.TENBETUSU.getObj())) {
      JSONObject obj = (JSONObject) map.get(0);
      if (obj.containsKey("KEY")) {
        sqlcommand = new ReportST021Dao(jndiName).createSqlSelTOKNNDT((JSONObject) map.get(0));
      } else {
        sqlcommand = new ReportST021Dao(jndiName).createSqlSelTOKNNDTSplit((JSONObject) map.get(0));
      }
    }

    // 税率区分
    if (outobj.equals(DefineReport.Select.ZEIRTKBN.getObj()) || outobj.equals(DefineReport.Select.ZEIRTKBN_OLD.getObj())) {
      map.get(0);
      sqlcommand = DefineReport.ID_SQL_ZEIRT;
    }

    // 分類区分
    if (outobj.equals(DefineReport.Select.BNNRUIKBN.getObj())) {
      map.get(0);
      sqlcommand = DefineReport.ID_SQL_BUNRUI;
    }
    // 分類区分
    if (outobj.equals(DefineReport.Select.TENKN.getObj())) {
      JSONObject obj = (JSONObject) map.get(0);
      if (DefineReport.ID_PAGE_SA003.equals(outpage) || DefineReport.ID_PAGE_SA004.equals(outpage) || DefineReport.ID_PAGE_SA005.equals(outpage) || DefineReport.ID_PAGE_SA008.equals(outpage)) {
        String txt_moyskbn = obj.optString("MOYSKBN");
        String txt_moysstdt = obj.optString("MOYSSTDT");
        String txt_moysrban = obj.optString("MOYSRBAN");
        String txt_kyoseiflg = obj.optString("KYOSEIFLG");
        String txt_tengpcd = obj.optString("TENGPCD");
        paramData.add(txt_moyskbn);
        paramData.add(txt_moysstdt);
        paramData.add(txt_moysrban);
        paramData.add(txt_kyoseiflg);
        paramData.add(txt_tengpcd);
        sqlcommand = DefineReport.ID_SQL_TENKN_HEAD2 + sqlcommand;
      } else if (DefineReport.ID_PAGE_X231.equals(outpage)) {
        User userInfo = (User) session.getAttribute(Consts.STR_SES_LOGINUSER);
        String tencd = userInfo.getTenpo();
        paramData.add(tencd);
        sqlcommand = DefineReport.ID_SQL_TENKN2;
      } else {
        sqlcommand = DefineReport.ID_SQL_TENKN;
      }
    }
    if (StringUtils.startsWith(outobj, DefineReport.InpText.TENGPCD.getObj())) {
      JSONObject obj = (JSONObject) map.get(0);
      String txt_moyskbn = obj.optString("MOYSKBN");
      String txt_moysstdt = obj.optString("MOYSSTDT");
      String txt_moysrban = obj.optString("MOYSRBAN");
      String txt_tencd = obj.optString("TENCD");
      String txt_kyoseiflg = obj.optString("KYOSEIFLG");

      paramData.add(txt_moyskbn);
      paramData.add(txt_moysstdt);
      paramData.add(txt_moysrban);
      paramData.add(txt_tencd);
      paramData.add(txt_kyoseiflg);

      sqlcommand = DefineReport.ID_SQL_TENGPCD;
    }
    if (StringUtils.startsWith(outobj, "SUMI_KANRINO")) {
      JSONObject obj = (JSONObject) map.get(0);
      String txt_hbstdt = obj.optString("HBSTDT");
      String txt_bmncd = obj.optString("BMNCD");
      String txt_waribiki2 = obj.optString("WRITUKBN");
      String txt_seisi2 = obj.optString("SEICUTKBN");
      String txt_dummycd = obj.optString("DUMMYCD");

      paramData.add(txt_hbstdt);
      paramData.add(txt_bmncd);
      paramData.add(txt_waribiki2);
      paramData.add(txt_seisi2);
      paramData.add(txt_dummycd);

      sqlcommand = DefineReport.SUMI_KANRINO;
    }
    if (outobj.equals("BAIKACOUNT")) {
      JSONObject obj = (JSONObject) map.get(0);
      String txt_moyskbn = obj.optString("MOYSKBN");
      String txt_moysstdt = obj.optString("MOYSSTDT");
      String txt_moysrban = obj.optString("MOYSRBAN");
      String txt_tengpcd = obj.optString("TENGPCD");

      sqlcommand += "  select ";
      sqlcommand += "  sum(case when (case when TTSH.ADDSHUKBN <> 01 then (case when TTSH.BD1_B_BAIKAAN > 0 then TO_CHAR(TTSH.KO_B_BAIKAAN) else TO_CHAR(TTSH.B_BAIKAAM) END) ";
      sqlcommand += "  else (select MMSH.NMKN from INAMS.MSTMEISHO MMSH where MMSH.MEISHOKBN = 10302 and MMSH.MEISHOCD = char (TTSH.B_WRITUKBN)) END) is null then 0 else 1 end) as VALUE1 ";
      sqlcommand += " ,sum(case when (case when TTSH.ADDSHUKBN <> 01 then (case when TTSH.BD1_C_BAIKAAN > 0 then TO_CHAR(TTSH.KO_C_BAIKAAN) else TO_CHAR(TTSH.C_BAIKAAM) END)";
      sqlcommand += "  else (select MMSH.NMKN from INAMS.MSTMEISHO MMSH where MMSH.MEISHOKBN = 10302 and MMSH.MEISHOCD = char (TTSH.C_WRITUKBN)) END) is null then 0 else 1 end) as VALUE2 ";
      sqlcommand += "  from INATK.TOKTG_SHN TTSH left join INATK.TOKTG_KHN TTKH on TTKH.MOYSKBN =" + txt_moyskbn + " and TTKH.MOYSSTDT = " + txt_moysstdt + " and TTKH.MOYSRBAN =" + txt_moysrban;
      sqlcommand +=
          "  left join INATK.TOKTG_QAGP TTQG on TTQG.MOYSKBN = " + txt_moyskbn + " and TTQG.MOYSSTDT = " + txt_moysstdt + " and TTQG.MOYSRBAN = " + txt_moysrban + " and TTQG.TENGPCD = " + txt_tengpcd;
      sqlcommand += "  left join INATK.TOKMOYCD TMYC on TMYC.MOYSKBN = " + txt_moyskbn + " and TMYC.MOYSSTDT = " + txt_moysstdt + " and TMYC.MOYSRBAN = " + txt_moysrban;
      sqlcommand += "  left join INATK.TOKTG_QASHN TTQS on TTQS.MOYSKBN = " + txt_moyskbn + " and TTQS.MOYSSTDT = " + txt_moysstdt + " and TTQS.MOYSRBAN = " + txt_moysrban + " and TTQS.TENGPCD = "
          + txt_tengpcd;
      sqlcommand += "  and TTQS.KANRINO = TTSH.KANRINO and TTQS.BMNCD = TTSH.BMNCD and TTQS.KANRIENO = TTSH.KANRIENO";
      sqlcommand += "  where TTSH.MOYSKBN = " + txt_moyskbn + " and TTSH.MOYSSTDT = " + txt_moysstdt + " and TTSH.MOYSRBAN = " + txt_moysrban + "";
      sqlcommand += "  and   TTKH.MOYSKBN = " + txt_moyskbn + " and TTKH.MOYSSTDT = " + txt_moysstdt + " and TTKH.MOYSRBAN = " + txt_moysrban + "";
      sqlcommand += "  and   TTQG.MOYSKBN = " + txt_moyskbn + " and TTQG.MOYSSTDT = " + txt_moysstdt + " and TTQG.MOYSRBAN = " + txt_moysrban + "";
      sqlcommand += "  and   TMYC.MOYSKBN = " + txt_moyskbn + " and TMYC.MOYSSTDT = " + txt_moysstdt + " and TMYC.MOYSRBAN = " + txt_moysrban + "";
      sqlcommand += "  and   TTQS.MOYSKBN = " + txt_moyskbn + " and TTQS.MOYSSTDT = " + txt_moysstdt + " and TTQS.MOYSRBAN = " + txt_moysrban + " and TTQS.TENGPCD =" + txt_tengpcd;
      sqlcommand += "  and TTQS.KANRINO = TTSH.KANRINO and TTQS.BMNCD = TTSH.BMNCD and TTQS.KANRIENO = TTSH.KANRIENO ";

    }

    // 前複写ボタン
    if (StringUtils.startsWith(outobj, DefineReport.Button.COPY.getObj())) {
      JSONObject obj = (JSONObject) map.get(0);
      if (DefineReport.ID_PAGE_TG016.equals(outpage)) {
        if (outobj.equals(DefineReport.Button.COPY.getObj() + "1")) {
          sqlcommand = new ReportTG016Dao(jndiName).createSqlSelTOK_SHN_BEF1(obj, lusr);
        }
        if (outobj.equals(DefineReport.Button.COPY.getObj() + "2")) {
          sqlcommand = new ReportTG016Dao(jndiName).createSqlSelTOK_SHN_BEF2(obj, lusr);
        }
        if (outobj.equals(DefineReport.Button.COPY.getObj() + "3_1")) {
          sqlcommand = new ReportTG016Dao(jndiName).createSqlSelTOK_SHN_BEF3(obj, lusr);
        }
        if (outobj.equals(DefineReport.Button.COPY.getObj() + "3_2")) {
          sqlcommand = new ReportTG016Dao(jndiName).createSqlSelTOK_NNDT_BEF3(obj, lusr);
        }
        if (outobj.equals(DefineReport.Button.COPY.getObj() + "1_2")) {
          sqlcommand = new ReportTG016Dao(jndiName).createSqlSelTOK_SHN_BEF4(obj, lusr);
        }
      }
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

    // 禁止文字一覧取得時の文字化けを回避
    if (outobj.equals(DefineReport.Select.PROHIBITED_LIST.getObj())) {
      json = new String(json.getBytes("Cp943C"), "MS932");
    }

    return json;
  }
}
