package dao;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import authentication.bean.User;
import authentication.defines.Consts;
import common.DefineReport;
import common.DefineReport.DataType;
import common.Defines;
import common.InputChecker;
import common.ItemList;
import common.MessageUtility;
import common.MessageUtility.FieldType;
import common.MessageUtility.Msg;
import common.MessageUtility.MsgKey;
import dao.ReportTG016Dao.TOKSP_NNDT_MySQL_Layout;
import dao.ReportTG016Dao.TOKTG_NNDT_MySQL_Layout;
import dao.ReportTG016Dao.TOK_CMN_MySQL_Layout;
import dao.ReportTG016Dao.TOK_CMN_SHNNNDT_MySQL_Layout;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 */
public class ReportST021Dao extends ItemDao {

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public ReportST021Dao(String JNDIname) {
    super(JNDIname);
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
    String sysdate = (String) request.getSession().getAttribute(Consts.STR_SES_LOGINDT);

    // 更新情報チェック(基本JS側で制御)
    JSONObject option = new JSONObject();
    JSONArray msgList = this.check(map, userInfo, sysdate);

    if (msgList.size() > 0) {
      option.put(MsgKey.E.getKey(), msgList);
      return option;
    }

    // 更新処理
    try {
      option = this.updateData(map, userInfo, sysdate);
      option.put("sysdate", sysdate.substring(2, 4) + "/" + sysdate.substring(4, 6) + "/" + sysdate.substring(6, 8));
      option.put("user", userInfo.getId());
    } catch (Exception e) {
      e.printStackTrace();
      option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
    }
    return option;
  }

  /**
   * 正情報取得処理
   *
   * @throws Exception
   */
  public String createSqlSelTOKNNDT(JSONObject obj) {

    String szMoyskbn = obj.getString("MOYSKBN"); // 催し区分
    String szMoysstdt = obj.getString("MOYSSTDT"); // 催しコード（催し開始日）
    String szMoysrban = obj.getString("MOYSRBAN"); // 催し連番
    String szBmncd = obj.getString("BMNCD"); // 部門コード
    String szKanrino = obj.getString("KANRINO"); // 管理No.
    String szKanrieno = obj.getString("KANRIENO"); // 管理No.枝番
    String szNndt = obj.getString("NNDT"); // 納入日(納入店確認時)


    // パラメータ確認
    // 必須チェック
    if ((szMoyskbn == null) || (szMoysstdt == null) || (szMoysrban == null) || (szBmncd == null) || (szKanrino == null) || (szKanrieno == null) || (szNndt == null)) {
      System.out.println(super.getConditionLog());
      return "";
    }


    // 全店特売アンケート有/無
    boolean isTOKTG = super.isTOKTG(szMoyskbn, szMoysrban);

    String szTableNNDT = "INATK.TOKTG_NNDT"; // 全店特売(アンケート有/無)_納入日
    if (!isTOKTG) {
      szTableNNDT = "INATK.TOKSP_NNDT";
    }

    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append(" select");
    sbSQL.append("  MOYSKBN"); // F1 : 催し区分
    sbSQL.append(" ,MOYSSTDT"); // F2 : 催し開始日
    sbSQL.append(" ,MOYSRBAN"); // F3 : 催し連番
    sbSQL.append(" ,BMNCD"); // F4 : 部門
    sbSQL.append(" ,KANRINO"); // F5 : 管理番号
    sbSQL.append(" ,KANRIENO"); // F6 : 枝番
    sbSQL.append(" ,NNDT"); // F7 : 納入日
    sbSQL.append(" ,TENHTSU_ARR"); // F8 : 店発注数配列
    if (isTOKTG) {
      sbSQL.append(" ,TENCHGFLG_ARR"); // F9 : 店変更フラグ配列
    } else {
      sbSQL.append(" ,null as TENCHGFLG_ARR"); // F9 : 店変更フラグ配列
    }
    sbSQL.append(" ,HTASU"); // F10: 発注総数
    sbSQL.append(" ,PTNNO"); // F11: パターン№
    sbSQL.append(" ,TSEIKBN"); // F12: 訂正区分
    sbSQL.append(" ,TPSU"); // F13: 店舗数
    sbSQL.append(" ,TENKAISU"); // F14: 展開数
    sbSQL.append(" ,ZJSKFLG"); // F15: 前年実績フラグ
    sbSQL.append(" ,WEEKHTDT"); // F16: 週間発注処理日
    sbSQL.append(" ,DATE_FORMAT(UPDDT, '%Y%m%d%H%i%s%f') as HDN_UPDDT");
    sbSQL.append(" from " + szTableNNDT);
    sbSQL.append(" where MOYSKBN  = " + szMoyskbn + "");
    sbSQL.append("   and MOYSSTDT = " + szMoysstdt + "");
    sbSQL.append("   and MOYSRBAN = " + szMoysrban + "");
    sbSQL.append("   and BMNCD    = " + szBmncd + "");
    sbSQL.append("   and KANRINO  = " + szKanrino + "");
    sbSQL.append("   and KANRIENO = " + szKanrieno + "");
    sbSQL.append("   and NNDT     = " + szNndt + "");
    return sbSQL.toString();
  }

  /**
   * 正情報取得処理
   *
   * @throws Exception
   */
  public String createSqlSelTOKNNDTSplit(JSONObject obj) {

    String szMoyskbn = obj.getString("MOYSKBN"); // 催し区分
    String szMoysstdt = obj.getString("MOYSSTDT"); // 催しコード（催し開始日）
    String szMoysrban = obj.getString("MOYSRBAN"); // 催し連番
    String szBmncd = obj.getString("BMNCD"); // 部門コード
    String szKanrino = obj.getString("KANRINO"); // 管理No.
    String szKanrieno = obj.getString("KANRIENO"); // 管理No.枝番
    String szNndt = obj.getString("NNDT"); // 納入日(納入店確認時)


    // パラメータ確認
    // 必須チェック
    if ((szMoyskbn == null) || (szMoysstdt == null) || (szMoysrban == null) || (szBmncd == null) || (szKanrino == null) || (szKanrieno == null) || (szNndt == null)) {
      System.out.println(super.getConditionLog());
      return "";
    }


    // 全店特売アンケート有/無
    boolean isTOKTG = super.isTOKTG(szMoyskbn, szMoysrban);

    String szTableNNDT = "INATK.TOKTG_NNDT"; // 全店特売(アンケート有/無)_納入日
    if (!isTOKTG) {
      szTableNNDT = "INATK.TOKSP_NNDT";
    }

    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append("with RECURSIVE WK as (");
    sbSQL.append(" select TENHTSU_ARR as ARR, 5 as LEN");
    sbSQL.append(" from " + szTableNNDT);
    sbSQL.append(" where MOYSKBN  = " + szMoyskbn + "");
    sbSQL.append("   and MOYSSTDT = " + szMoysstdt + "");
    sbSQL.append("   and MOYSRBAN = " + szMoysrban + "");
    sbSQL.append("   and BMNCD    = " + szBmncd + "");
    sbSQL.append("   and KANRINO  = " + szKanrino + "");
    sbSQL.append("   and KANRIENO = " + szKanrieno + "");
    sbSQL.append("   and NNDT     = " + szNndt + "");
    sbSQL.append(")");
    sbSQL.append(",TENWK(COLNO, TENNO) as (");
    sbSQL.append(" select 0, 1 from (SELECT 1 AS DUMMY)DUMMY");
    sbSQL.append(" union all");
    sbSQL.append(" select MOD(COLNO + 1, 15),TENNO + 1 from TENWK where TENNO < 400");
    sbSQL.append(")");
    sbSQL.append(DefineReport.ID_SQL_ARR_CMN);
    sbSQL.append(", WK1 as (");
    sbSQL.append(" select");
    sbSQL.append("  ROW_NUMBER() over(PARTITION BY COLNO  order by TENNO) as ROWNO");
    sbSQL.append(" ,T1.COLNO");
    sbSQL.append(" ,T1.TENNO");
    sbSQL.append(" ,case when trim(T2.RNK) = '' then null else cast(trim(T2.RNK) as signed) end as RNK");
    sbSQL.append(" ,case when M1.TENCD is null or M1.MISEUNYOKBN = 9 then 1 else 0 end as NULLFLG");
    sbSQL.append(" from TENWK T1");
    sbSQL.append(" left outer join ARRWK T2 on T1.TENNO = T2.IDX");
    sbSQL.append(" left outer join INAMS.MSTTEN M1 on T1.TENNO = M1.TENCD and M1.UPDKBN = 0");
    sbSQL.append(")");

    sbSQL.append(" select ROWNO, 1 as IDX");
    for (int i = 0; i < 15; i++) {
      sbSQL.append(" ,max(case when COLNO = " + i + " then TENNO end) as HTASU_" + i);
      sbSQL.append(" ,null as FLG_" + i);
    }
    sbSQL.append(" from WK1 group by ROWNO");
    sbSQL.append(" union all ");
    sbSQL.append(" select ROWNO, 2 as IDX");
    for (int i = 0; i < 15; i++) {
      sbSQL.append(" ,max(case when COLNO = " + i + " then RNK end) as HTASU_" + i);
      sbSQL.append(" ,max(case when COLNO = " + i + " then NULLFLG end) as FLG_" + i);
    }
    sbSQL.append(" from WK1 group by ROWNO");
    sbSQL.append(" order by ROWNO, IDX");

    return sbSQL.toString();
  }

  /**
   * チェック処理
   *
   * @throws Exception
   */
  public JSONArray check(HashMap<String, String> map, User userInfo, String sysdate) {
    JSONArray msgArray = new JSONArray();
    MessageUtility mu = new MessageUtility();

    JSONArray dataArray = JSONArray.fromObject(map.get("DATA"));

    // パラメータ確認
    String szMoyskbn = dataArray.optJSONObject(0).optString(TOKTG_NNDT_MySQL_Layout.MOYSKBN.getId()); // 催し区分
    String szMoysstdt = dataArray.optJSONObject(0).optString(TOKTG_NNDT_MySQL_Layout.MOYSSTDT.getId()); // 催しコード（催し開始日）
    String szMoysrban = dataArray.optJSONObject(0).optString(TOKTG_NNDT_MySQL_Layout.MOYSRBAN.getId()); // 催し連番
    dataArray.optJSONObject(0).optString(TOKTG_NNDT_MySQL_Layout.BMNCD.getId());
    String szKanrino = dataArray.optJSONObject(0).optString(TOKTG_NNDT_MySQL_Layout.KANRINO.getId()); // 管理No.
    dataArray.optJSONObject(0).optString(TOKTG_NNDT_MySQL_Layout.KANRIENO.getId());
    String szNndt = dataArray.optJSONObject(0).optString(TOKTG_NNDT_MySQL_Layout.NNDT.getId()); // 納入日(納入店確認時)

    ReportTG016Dao dao = new ReportTG016Dao(super.JNDIname);
    ReportJU012Dao daoJu = new ReportJU012Dao(JNDIname);

    String errMsg = "";

    // 全店特売アンケート有/無
    boolean isTOKTG = super.isTOKTG(szMoyskbn, szMoysrban);

    dao.ReportTG016Dao.MSTLayout[] layouts;
    String[] target;
    String[] notTarget;
    if (isTOKTG) {
      layouts = TOKTG_NNDT_MySQL_Layout.values();
      target = new String[] {TOKTG_NNDT_MySQL_Layout.TENHTSU_ARR.getId(), TOKTG_NNDT_MySQL_Layout.TENCHGFLG_ARR.getId()};
      notTarget = new String[] {TOKTG_NNDT_MySQL_Layout.SENDFLG.getId(), TOKTG_NNDT_MySQL_Layout.OPERATOR.getId(), TOKTG_NNDT_MySQL_Layout.ADDDT.getId(), TOKTG_NNDT_MySQL_Layout.UPDDT.getId()};
    } else {
      layouts = TOKSP_NNDT_MySQL_Layout.values();
      target = new String[] {TOKSP_NNDT_MySQL_Layout.TENHTSU_ARR.getId()};
      notTarget = new String[] {TOKSP_NNDT_MySQL_Layout.SENDFLG.getId(), TOKSP_NNDT_MySQL_Layout.OPERATOR.getId(), TOKSP_NNDT_MySQL_Layout.ADDDT.getId(), TOKSP_NNDT_MySQL_Layout.UPDDT.getId()};
    }

    // 基本データチェック:入力値がテーブル定義と矛盾してないか確認、
    for (int i = 0; i < dataArray.size(); i++) {
      JSONObject jo = dataArray.optJSONObject(i);
      for (dao.ReportTG016Dao.MSTLayout colinf : layouts) {
        if (ArrayUtils.contains(notTarget, colinf.getId())) {
          continue;
        } // チェック不要
        if (!ArrayUtils.contains(target, colinf.getId())) {
          continue;
        } // チェック不要

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
            msgArray.add(o);
            return msgArray;
          }
          // ②データ桁チェック
          if (!InputChecker.checkDataLen(dtype, val, digit)) {
            JSONObject o = mu.getDbMessageObjLen(dtype, new String[] {});
            msgArray.add(o);
            return msgArray;
          }
        }
      }

      // 重複チェック
      String shnCd = "";
      if (!isTOKTG) {
        String moyscd = szMoyskbn + szMoysstdt + String.format("%3s", szMoysrban);
        int tenSu = 0;

        String arr = jo.optString(TOKSP_NNDT_MySQL_Layout.TENHTSU_ARR.getId());
        shnCd = jo.optString("F20");
        String binKbn = jo.optString("F21");

        // 商品納入日重複チェック
        HashMap<String, String> map2 = dao.getArrMap(shnCd, binKbn, szNndt, szMoyskbn, "2", "1");
        HashMap<String, String> mapKanri = dao.getArrMap(shnCd, binKbn, szNndt, szMoyskbn, "2", "2");
        HashMap<String, String> mapHtsu = daoJu.getDigitMap(arr, 5, "1");

        for (HashMap.Entry<String, String> shnnn : map2.entrySet()) {
          if (mapHtsu.containsKey(shnnn.getKey()) && Integer.valueOf(mapHtsu.get(shnnn.getKey())) >= 0) {
            if (!shnnn.getValue().equals(moyscd) || (shnnn.getValue().equals(moyscd) && !mapKanri.get(shnnn.getKey()).equals(szKanrino))) {
              if (tenSu == 0) {
                String moyscdMsg = shnnn.getValue().substring(0, 1) + "-" + shnnn.getValue().substring(1, 7) + "-" + String.format("%03d", Integer.valueOf(shnnn.getValue().substring(8).trim()));
                errMsg += "納入日 " + szNndt.substring(4, 6) + "月" + szNndt.substring(6, 8) + "日 催しコード " + moyscdMsg + "と " + shnnn.getKey() + "号店以下";
              }
              tenSu++;
            }
          }
        }

        if (tenSu != 0) {
          errMsg += tenSu + "店舗<br>";
          tenSu = 0;
        }

        if (!StringUtils.isEmpty(errMsg)) {
          msgArray.add(mu.getDbMessageObj("E30025", new String[] {"商品コード " + shnCd + " 便区分 " + binKbn + "便<br>" + errMsg + "<br>"}));
          return msgArray;
        }
      } else {
        // 変数を初期化
        // 格納用変数
        StringBuffer sbSQL = new StringBuffer();
        ItemList iL = new ItemList();
        JSONArray dbDatas = new JSONArray();

        shnCd = jo.optString("F21");

        // DB検索用パラメータ
        String sqlWhere = "";
        ArrayList<String> paramData = new ArrayList<String>();

        if (StringUtils.isEmpty(shnCd)) {
          sqlWhere += "T1.SHNCD=null AND ";
        } else {
          sqlWhere += "T1.SHNCD=? AND ";
          paramData.add(shnCd);
        }

        sqlWhere += "T1.MOYSKBN=T2.MOYSKBN AND ";
        sqlWhere += "T1.MOYSSTDT=T2.MOYSSTDT AND ";
        sqlWhere += "T1.MOYSRBAN=T2.MOYSRBAN AND ";
        sqlWhere += "T1.BMNCD=T2.BMNCD AND ";
        sqlWhere += "T1.KANRINO=T2.KANRINO AND ";
        sqlWhere += "T1.KANRIENO=T2.KANRIENO AND ";
        sqlWhere += "T2.NNDT=? ";
        paramData.add(szNndt);

        sbSQL.append("SELECT ");
        sbSQL.append("T1.MOYSKBN "); // レコード件数
        sbSQL.append(",T1.MOYSSTDT "); // レコード件数
        sbSQL.append(",T1.MOYSRBAN "); // レコード件数
        sbSQL.append(",T1.KANRINO "); // レコード件数
        sbSQL.append("FROM ");
        sbSQL.append("INATK.TOKTG_SHN T1 ");
        sbSQL.append(",INATK.TOKTG_NNDT T2 ");
        sbSQL.append("WHERE ");
        sbSQL.append(sqlWhere); // 入力された商品コードで検索

        dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

        if (dbDatas.size() >= 1) {


          if (!szMoyskbn.equals(dbDatas.getJSONObject(0).optString("MOYSKBN")) || !szMoysstdt.equals(dbDatas.getJSONObject(0).optString("MOYSSTDT"))
              || !szMoysrban.equals(dbDatas.getJSONObject(0).optString("MOYSRBAN")) || !szKanrino.equals(dbDatas.getJSONObject(0).optString("KANRINO"))) {
            // 同一納入日の重複チェックエラー
            JSONObject o = mu.getDbMessageObj("E20450", new String[] {});
            msgArray.add(o);
            return msgArray;
          }
        }
      }
    }

    return msgArray;
  }

  boolean isTest = false;

  /**
   * 更新処理実行
   *
   * @return
   *
   * @throws Exception
   */
  private JSONObject updateData(HashMap<String, String> map, User userInfo, String sysdate) throws Exception {
    JSONObject option = new JSONObject();
    JSONArray msg = new JSONArray();
    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー

    // パラメータ確認
    JSONArray dataArrayNNDT = JSONArray.fromObject(map.get("DATA"));

    ReportTG016Dao dao = new ReportTG016Dao(super.JNDIname);
    ReportJU012Dao daoJu = new ReportJU012Dao(JNDIname);

    // 基本登録情報
    JSONObject data = dataArrayNNDT.getJSONObject(0);
    String szMoyskbn = data.optString(TOKTG_NNDT_MySQL_Layout.MOYSKBN.getId()); // 催し区分
    String szMoysstdt = data.optString(TOKTG_NNDT_MySQL_Layout.MOYSSTDT.getId()); // 催しコード（催し開始日）
    String szMoysrban = data.optString(TOKTG_NNDT_MySQL_Layout.MOYSRBAN.getId()); // 催し連番
    String szBmncd = data.optString(TOKTG_NNDT_MySQL_Layout.BMNCD.getId()); // 部門コード
    String szKanrino = data.optString(TOKTG_NNDT_MySQL_Layout.KANRINO.getId()); // 管理No.
    String szKanrieno = data.optString(TOKTG_NNDT_MySQL_Layout.KANRIENO.getId()); // 管理No.枝番
    String szNndt = data.optString(TOKTG_NNDT_MySQL_Layout.NNDT.getId()); // 納入日(納入店確認時)
    JSONArray updKeys = new JSONArray();

    // 全店特売アンケート有/無
    boolean isTOKTG = super.isTOKTG(szMoyskbn, szMoysrban);

    // 3.14.3.7．DB更新処理（全特アンケート有）
    if (isTOKTG) {
      // 全店特売（アンケート有）_納入日
      if (dataArrayNNDT.size() > 0) {
        this.createSqlTOK_NNDT(userId, data, SqlType.UPD, isTOKTG, dao);
      }
      // 注意：全店特売（アンケート有）_販売は、アンケート有では登録しない。
      // 重複チェックテーブルを用いない為、管理テーブルの処理はない。

      // 3.14.3.8．DB更新処理（全特アンケート無）
    } else {
      // 全店特売（アンケート無）_納入日
      if (dataArrayNNDT.size() > 0) {
        this.createSqlTOK_NNDT(userId, data, SqlType.UPD, isTOKTG, dao);
      }

      // ② 全特（ア無）_商品納入日：
      JSONArray array = new JSONArray();
      JSONObject obj = new JSONObject();
      String moyscd = szMoyskbn + szMoysstdt + String.format("%3s", szMoysrban);
      for (int i = 0; i < dataArrayNNDT.size(); i++) {

        JSONObject dataN = dataArrayNNDT.getJSONObject(i);
        if (dataN.isEmpty()) {
          continue;
        }

        String shnCd = data.optString("F20");
        String binKbn = data.optString("F21");

        // 商品納入日重複チェック
        HashMap<String, String> map2 = new HashMap<String, String>();
        HashMap<String, String> mapKanri = dao.getArrMap(shnCd, binKbn, szNndt, szMoyskbn, "2", "2");

        for (HashMap.Entry<String, String> shnNnDt : dao.getArrMap(shnCd, binKbn, szNndt, szMoyskbn, "2", "1").entrySet()) {

          String key = shnNnDt.getKey();
          String val = shnNnDt.getValue();
          String kanrino = mapKanri.containsKey(key) ? mapKanri.get(key).trim() : "";

          if (val.equals(moyscd) && !StringUtils.isEmpty(kanrino) && kanrino.equals(szKanrino)) {
            mapKanri.remove(key);
          } else {
            map2.put(key, val);
          }
        }

        String arr = dataN.optString(TOKSP_NNDT_MySQL_Layout.TENHTSU_ARR.getId());
        HashMap<String, String> mapHtsu = daoJu.getDigitMap(arr, 5, "1");

        for (HashMap.Entry<String, String> htsu : mapHtsu.entrySet()) {

          String val = htsu.getValue();
          String key = htsu.getKey();

          if (!val.equals("0") && !map2.containsKey(key)) {
            map2.put(key, moyscd);
          }

          if (!val.equals("0") && !mapKanri.containsKey(key)) {
            mapKanri.put(key, szKanrino);
          }
        }

        String moysArr = daoJu.createArr(map2, "1");
        String kanriArr = daoJu.createArr(mapKanri, "2");

        obj.put(TOK_CMN_SHNNNDT_MySQL_Layout.SHNCD.getId(), shnCd);
        obj.put(TOK_CMN_SHNNNDT_MySQL_Layout.BINKBN.getId(), binKbn);
        obj.put(TOK_CMN_SHNNNDT_MySQL_Layout.NNDT.getId(), szNndt);
        obj.put(TOK_CMN_SHNNNDT_MySQL_Layout.MOYCD_ARR.getId(), moysArr);
        obj.put(TOK_CMN_SHNNNDT_MySQL_Layout.KANRINO_ARR.getId(), kanriArr);
        array.add(obj);
        obj = new JSONObject();
      }
      if (array.size() != 0) {

        String table = "INATK.TOKHTK_SHNNNDT ";
        if (!szMoyskbn.equals("3")) {
          table = "INATK.TOKSP_SHNNNDT ";
        }

        createSqlTOKSP_SHNNNDT2(userId, array, data, SqlType.MRG, table, dao);
      }
    }

    // 関連テーブルの更新対象項目をUPDATE
    dao.setMoycdInfo(szMoyskbn, szMoysstdt, szMoysrban, szBmncd);
    String key = szMoyskbn + "," + szMoysstdt + "," + szMoysrban + "," + szBmncd + "," + szKanrino;
    if (!updKeys.contains(key)) {
      updKeys.add(key);
      dao.createSqlNnDtSub(updKeys, userId);
    }

    // 排他チェック実行
    String targetTable = isTOKTG ? "INATK.TOKTG_NNDT" : "INATK.TOKSP_NNDT";
    String targetWhere = " MOYSKBN = ? and MOYSSTDT= ? and MOYSRBAN = ? and BMNCD = ? and KANRINO = ? and KANRIENO = ? and NNDT = ?";
    String targetValue = isTOKTG ? data.optString(TOKTG_NNDT_MySQL_Layout.UPDDT.getId()) : data.optString(TOKSP_NNDT_MySQL_Layout.UPDDT.getId());
    ArrayList<String> targetParam = new ArrayList<String>();
    targetParam.add(szMoyskbn);
    targetParam.add(szMoysstdt);
    targetParam.add(szMoysrban);
    targetParam.add(szBmncd);
    targetParam.add(szKanrino);
    targetParam.add(szKanrieno);
    targetParam.add(szNndt);
    if (!super.checkExclusion(targetTable, targetWhere, targetParam, targetValue)) {
      msg.add(MessageUtility.getDbMessageExclusion(FieldType.DEFAULT, new String[] {}));
      option.put(MsgKey.E.getKey(), msg);
      return option;
    }

    ArrayList<Integer> countList = new ArrayList<Integer>();
    if (dao.sqlList.size() > 0) {
      countList = super.executeSQLs(dao.sqlList, dao.prmList);
    }

    if (StringUtils.isEmpty(getMessage())) {
      int count = 0;
      for (int i = 0; i < countList.size(); i++) {
        count += countList.get(i);
        if (DefineReport.ID_DEBUG_MODE)
          System.out.println(MessageUtility.getMessage(Msg.S00006.getVal(), new String[] {dao.lblList.get(i), Integer.toString(countList.get(i))}));
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


  /**
   * 全店特売(アンケート有/無)_納入日 SQL作成処理
   *
   * @param userId
   * @param data
   *
   * @throws Exception
   */
  protected JSONObject createSqlTOK_NNDT(String userId, JSONObject data, SqlType sql, boolean isTOKTG, ReportTG016Dao dao) {
    JSONObject result = new JSONObject();

    dao.ReportTG016Dao.MSTLayout[] layouts;
    String[] target;
    String table = "";
    if (isTOKTG) {
      layouts = TOKTG_NNDT_MySQL_Layout.values();
      target = new String[] {TOKTG_NNDT_MySQL_Layout.NNDT.getId(), TOKTG_NNDT_MySQL_Layout.TENHTSU_ARR.getId(), TOKTG_NNDT_MySQL_Layout.TENCHGFLG_ARR.getId()};
      table = "INATK.TOKTG_NNDT";
    } else {
      layouts = TOKSP_NNDT_MySQL_Layout.values();
      target = new String[] {TOKSP_NNDT_MySQL_Layout.NNDT.getId(), TOKSP_NNDT_MySQL_Layout.TENHTSU_ARR.getId()};
      table = "INATK.TOKSP_NNDT";
    }

    // 更新情報
    ArrayList<String> prmData = new ArrayList<String>();
    // 基本Merge文
    StringBuffer sbSQL;
    sbSQL = new StringBuffer();
    sbSQL.append("INSERT INTO " + table);
    sbSQL.append(" ( ");
    sbSQL.append(" MOYSKBN ,MOYSSTDT ,MOYSRBAN ");
    sbSQL.append(" ,BMNCD ,KANRINO ,KANRIENO ");
    sbSQL.append(" ,NNDT ,TENHTSU_ARR,SENDFLG,OPERATOR ,ADDDT,UPDDT ");
    sbSQL.append(") SELECT ");
    for (TOK_CMN_MySQL_Layout itm : TOK_CMN_MySQL_Layout.values()) {
      if (itm.getNo() > 1) {
        sbSQL.append(",");
      }
      String value = StringUtils.strip(data.optString(itm.getId()));
      if (isTest) {

        sbSQL.append("cast(" + value + " as " + itm.getTyp() + ") as " + itm.getCol());

      } else {
        prmData.add(value);
        sbSQL.append("cast(? as " + itm.getTyp() + ") as " + itm.getCol());

      }

    }
    for (dao.ReportTG016Dao.MSTLayout itm : layouts) {
      if (!ArrayUtils.contains(target, itm.getId())) {
        continue;
      } // パラメータ不要

      if (data.containsKey(itm.getId())) {
        String value = data.optString(itm.getId());
        if (isTest) {
          sbSQL.append(",cast('" + value + "' as " + itm.getTyp() + ") as " + itm.getCol());

        } else {
          if (StringUtils.isNotEmpty(value)) {
            prmData.add(value);
            sbSQL.append(",cast(? as " + itm.getTyp() + ") as " + itm.getCol());
          } else {
            sbSQL.append(",null as " + itm.getCol());
          }
        }
      } else {
        sbSQL.append(",null as " + itm.getCol());
      }
    }
    sbSQL.append("," + DefineReport.Values.SENDFLG_UN.getVal() + " as SENDFLG");
    sbSQL.append(", '" + userId + "' as OPERATOR ");
    sbSQL.append(",CURRENT_TIMESTAMP as ADDDT ");
    sbSQL.append(",CURRENT_TIMESTAMP as UPDDT ");
    sbSQL.append("from (SELECT 1 DUMMY) as DUMMY ");
    sbSQL.append("ON DUPLICATE KEY UPDATE ");
    sbSQL.append("  TENHTSU_ARR=VALUES(TENHTSU_ARR) "); // F8 : 店発注数配列
    if (isTOKTG) {
      sbSQL.append(" ,TENCHGFLG_ARR=VALUES(TENCHGFLG_ARR) "); // F9 : 店変更フラグ配列
    }
    sbSQL.append(",SENDFLG = VALUES(SENDFLG) ");
    sbSQL.append(",OPERATOR = VALUES(OPERATOR) ");
    sbSQL.append(",UPDDT = VALUES(UPDDT) ");

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    dao.sqlList.add(sbSQL.toString());
    dao.prmList.add(prmData);
    dao.lblList.add("全店特売(アンケート有/無)_納入日");
    return result;
  }

  /**
   * 全特(アンケート無)_商品納入日 SQL作成処理
   *
   * @param userId
   * @param data
   *
   * @throws Exception
   */
  protected JSONObject createSqlTOKSP_SHNNNDT2(String userId, JSONArray dataArray, JSONObject data, SqlType sql, String table, ReportTG016Dao dao) {
    JSONObject result = new JSONObject();

    String[] notTarget = new String[] {TOK_CMN_SHNNNDT_MySQL_Layout.OPERATOR.getId(), TOK_CMN_SHNNNDT_MySQL_Layout.ADDDT.getId(), TOK_CMN_SHNNNDT_MySQL_Layout.UPDDT.getId()};

    // 更新情報
    ArrayList<String> prmData = new ArrayList<String>();
    String values = "", names = "", rows = "";

    for (TOK_CMN_MySQL_Layout itm : TOK_CMN_MySQL_Layout.values()) {
      String value = StringUtils.strip(data.optString(itm.getId()));
      prmData.add(value);
    }

    for (int j = 0; j < dataArray.size(); j++) {
      values = "";
      names = "";
      for (TOK_CMN_SHNNNDT_MySQL_Layout itm : TOK_CMN_SHNNNDT_MySQL_Layout.values()) {
        if (ArrayUtils.contains(notTarget, itm.getId())) {
          continue;
        } // パラメータ不要

        String col = itm.getCol();
        String val = StringUtils.trim(dataArray.optJSONObject(j).optString(itm.getId()));
        if (itm.getId().equals(TOK_CMN_SHNNNDT_MySQL_Layout.MOYCD_ARR.getId()) || itm.getId().equals(TOK_CMN_SHNNNDT_MySQL_Layout.KANRINO_ARR.getId())) {
          val = dataArray.optJSONObject(j).optString(itm.getId());
        }
        if (StringUtils.isEmpty(val)) {
          values += ", null";
        } else {
          prmData.add(val);
          values += ", cast(? as CHAR(" + MessageUtility.getDefByteLen(val) + "))";
        }
        names += ", " + col;
      }
      rows += ",ROW(" + StringUtils.removeStart(values, ",") + ")";
    }
    rows = StringUtils.removeStart(rows, ",");
    names = StringUtils.removeStart(names, ",");

    // 基本Merge文
    StringBuffer sbSQL;
    sbSQL = new StringBuffer();
    sbSQL.append("INSERT INTO " + table);
    sbSQL.append(" ( ");
    for (TOK_CMN_SHNNNDT_MySQL_Layout itm : TOK_CMN_SHNNNDT_MySQL_Layout.values()) {
      if (itm.getNo() > 1) {
        sbSQL.append(",");
      }
      sbSQL.append(itm.getCol());
    }
    sbSQL.append(") SELECT ");
    for (TOK_CMN_SHNNNDT_MySQL_Layout itm : TOK_CMN_SHNNNDT_MySQL_Layout.values()) {
      if (itm.getNo() > 1) {
        sbSQL.append(",");
      }
      sbSQL.append(itm.getCol());
    }
    sbSQL.append(" FROM ( SELECT ");

    for (TOK_CMN_MySQL_Layout itm : TOK_CMN_MySQL_Layout.values()) {
      if (itm.getNo() > 1) {
        sbSQL.append(",");
      }
      String value = StringUtils.strip(data.optString(itm.getId()));
      if (isTest) {
        sbSQL.append("cast(" + value + " as " + itm.getTyp() + ") as " + itm.getCol());
      } else {
        sbSQL.append("cast(? as " + itm.getTyp() + ") as " + itm.getCol());
      }
    }
    for (TOK_CMN_SHNNNDT_MySQL_Layout itm : TOK_CMN_SHNNNDT_MySQL_Layout.values()) {
      if (ArrayUtils.contains(notTarget, itm.getId())) {
        continue;
      } // パラメータ不要
      sbSQL.append(",cast(T1." + itm.getCol() + " as " + itm.getTyp() + ") as " + itm.getCol());
    }
    sbSQL.append(" ,'" + userId + "' AS OPERATOR"); // オペレータ
    sbSQL.append(" ,CURRENT_TIMESTAMP AS ADDDT "); // 登録日
    sbSQL.append(" ,CURRENT_TIMESTAMP AS UPDDT "); // 更新日
    sbSQL.append("  FROM (values " + rows + ") as T1(" + names + ")");
    sbSQL.append(" ) as T1 ON DUPLICATE KEY UPDATE ");
    sbSQL.append(" MOYCD_ARR = VALUES(MOYCD_ARR) "); // F4 : 催しコード配列
    sbSQL.append(" ,KANRINO_ARR = VALUES(KANRINO_ARR) "); // F5 : 管理番号配列
    sbSQL.append(" ,OPERATOR='" + userId + "' "); // オペレータ
    // sbSQL.append(" ,ADDDT=RE.ADDDT"); // 登録日
    sbSQL.append(" ,UPDDT=CURRENT_TIMESTAMP "); // 更新日

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    dao.sqlList.add(sbSQL.toString());
    dao.prmList.add(prmData);
    dao.lblList.add("全特(アンケート無)_商品納入日");
    return result;
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

    String tbl = "";
    String col = "";
    String rep = "";
    // 商品コード
    if (outobj.equals(DefineReport.Select.SHUNO.getObj())) {
      tbl = "INAMS.MSTSHN";
      col = "SHUNO";
    }
    // メーカーコード
    if (outobj.equals(DefineReport.InpText.MAKERCD.getObj())) {
      tbl = "INAMS.MSTMAKER";
      col = "MAKERCD";
    }
    // 部門コード
    if (outobj.equals(DefineReport.InpText.BMNCD.getObj())) {
      tbl = "INAMS.MSTBMN";
      col = "BMNCD";
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
    }

    if (tbl.length() > 0 && col.length() > 0) {
      if (paramData.size() > 0 && rep.length() > 0) {
        sqlcommand = DefineReport.ID_SQL_CHK_TBL.replace("@T", tbl).replaceAll("@C", col).replace("?", rep);
      } else {
        paramData.add(value);
        sqlcommand = DefineReport.ID_SQL_CHK_TBL.replace("@T", tbl).replaceAll("@C", col);
      }

      @SuppressWarnings("static-access")
      JSONArray array = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
      if (array.size() > 0 && NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) > 0) {
        return true;
      }
    }
    return false;
  }
}
