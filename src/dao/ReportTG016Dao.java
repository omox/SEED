package dao;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
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
import common.Defines;
import common.InputChecker;
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
public class ReportTG016Dao extends ItemDao {

  // 画面モード
  boolean isModeA = false; // TG008更新
  boolean isModeB = false; // TG008参照
  boolean isModeC = false; // TG009(更新のみ)
  boolean isModeD = false; // ST016の新規・新規（全品割引）
  boolean isModeE = false; // ST016の選択（販売・納品情報）とMM001の選択
  boolean isModeF = false; // ST016の月締後新規・新規（全品割引） →機能廃止
  boolean isModeG = false; // ST016の月締後今の内容を修正 →機能廃止
  boolean isModeH = false; // ST019の選択（確定）
  boolean isModeI = false; // 参照モード

  // 処理タイプ
  boolean isNew = false; // 新規
  boolean isCopyNew = false; // コピー新規
  boolean isChange = false; // 更新
  boolean isRefer = false; // 参照

  boolean isCopy = false; // コピー表示

  // 催し種類
  boolean isToktg = false; // アンケート有
  boolean isToksp = false; // アンケート無
  boolean isToktg_t = false; // アンケート有(チラシのみ)
  boolean isToktg_h = false; // アンケート有(販売・納入)

  // 登録種別
  boolean isFrm1 = false; // ドライ
  boolean isFrm2 = false; // 精肉
  boolean isFrm3 = false; // 鮮魚
  boolean isFrm4 = false; // 青果
  boolean isFrm5 = false; // 全品割引

  boolean st = false;
  boolean tg = false;

  boolean csv = false;

  String reqNo = "";

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public ReportTG016Dao(String JNDIname) {
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
    String sysdate = (String) request.getSession().getAttribute(Consts.STR_SES_LOGINDT);


    // 更新情報チェック(基本JS側で制御)
    JSONObject option = new JSONObject();

    String sendBtnid = map.get("SENDBTNID"); // 呼出しボタン
    String sendPageid = map.get("PAGEID"); // 呼出しPAGEID

    // 画面モード情報設定
    this.setModeInfo(sendPageid, sendBtnid);

    JSONObject objset = this.check(map, userInfo, sysdate);
    JSONArray msgList = objset.optJSONArray("MSG");
    System.out.print("msglist : " + msgList + "\n");
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
    }

    // エラー時
    if (option.containsKey(MsgKey.E.getKey())) {
      // 採番実行時にエラーの場合、解除処理
      // JSONObject dataOther = objset.optJSONObject("DATA_OTHER");
      // if(StringUtils.isNotEmpty(dataOther.optString(MSTSHNLayout.SHNCD.getCol()+"_RENEW"))){
      // // 商品コード情報、もしくはエラー情報が返ってくる
      // JSONObject result = NumberingUtility.execReleaseNewSHNCD(userInfo,
      // dataOther.optString(MSTSHNLayout.SHNCD.getCol()+"_RENEW"));
      // }
    }
    return option;
  }


  /**
   * 更新処理(許可)
   *
   * @param userInfo
   *
   * @return
   * @throws IOException
   * @throws FileNotFoundException
   * @throws Exception
   */
  public JSONObject update2(HttpServletRequest request, HttpSession session, HashMap<String, String> map, User userInfo) {
    String sysdate = (String) request.getSession().getAttribute(Consts.STR_SES_LOGINDT);

    ReportTG008Dao dao = new ReportTG008Dao(super.JNDIname);

    // 更新情報チェック(基本JS側で制御)
    JSONObject option = new JSONObject();
    JSONArray msgList = dao.check(map, userInfo, sysdate);

    if (msgList.size() > 0) {
      option.put(MsgKey.E.getKey(), msgList);
      return option;
    }

    // 更新処理
    try {
      option = dao.updateData(map, userInfo, sysdate);
    } catch (Exception e) {
      e.printStackTrace();
      option.put(MsgKey.E.getKey(), MessageUtility.getDbMessageIdObj("E30007", new String[] {}));
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


  /**
   * 画面モード情報判断
   */
  private void setModeInfo(String sendPageid, String sendBtnid) {
    // 画面モード取得
    isModeA = (DefineReport.ID_PAGE_TG008.equals(sendPageid) && (StringUtils.equals(sendBtnid, DefineReport.Button.SEL_CHANGE.getObj()))); // TG008更新
    isModeB = (DefineReport.ID_PAGE_TG008.equals(sendPageid) && (StringUtils.equals(sendBtnid, DefineReport.Button.SEL_REFER.getObj()))); // TG008参照
    isModeC = (DefineReport.ID_PAGE_TG009.equals(sendPageid) && (StringUtils.equals(sendBtnid, DefineReport.Button.SEL_CHANGE.getObj()))); // TG009(更新のみ)
    isModeD = (DefineReport.ID_PAGE_ST016.equals(sendPageid) && (StringUtils.startsWith(sendBtnid, DefineReport.Button.NEW.getObj()))); // ST016の新規・新規（全品割引）
    isModeE = (DefineReport.ID_PAGE_ST016.equals(sendPageid) && (StringUtils.equals(sendBtnid, DefineReport.Button.SEL_CHANGE.getObj())))
        || (DefineReport.ID_PAGE_MM001.equals(sendPageid) && (StringUtils.equals(sendBtnid, DefineReport.Button.SELECT.getObj()))); // ST016の選択（販売・納品情報）とMM001の選択
    isModeF = (DefineReport.ID_PAGE_ST016.equals(sendPageid) && (false)); // ST016の月締後新規・新規（全品割引） →機能廃止
    isModeG = (DefineReport.ID_PAGE_ST016.equals(sendPageid) && (false)); // ST016の月締後今の内容を修正 →機能廃止
    isModeH = (DefineReport.ID_PAGE_ST019.equals(sendPageid) && (StringUtils.equals(sendBtnid, DefineReport.Button.SEL_KAKUTEI.getObj()))); // ST019の選択（確定）
    isModeI = (!isModeA && !isModeB && !isModeC && !isModeD && !isModeE && !isModeF && !isModeG && !isModeH); // 参照モード

    // 作業モード
    isNew = isModeD;
    isCopyNew = isModeH;
    isChange = isModeA || isModeC || isModeE;
    isRefer = isModeB || isModeI;

    isCopy = DefineReport.ID_PAGE_ST019.equals(sendPageid);
  }

  /**
   * 催し種別情報判断
   */
  public void setMoycdInfo(String szMoyskbn, String szMoysstdt, String szMoysrban, String szBmncd) {
    // 催し種類
    isToktg = super.isTOKTG(szMoyskbn, szMoysrban); // アンケート有
    isToksp = !isToktg; // アンケート無
    if (isToktg) {
      // チラシのみ部門判断
      JSONObject obj = new JSONObject();
      obj.put("MOYSKBN", szMoyskbn); // 催し区分
      obj.put("MOYSSTDT", szMoysstdt); // 催しコード（催し開始日）
      obj.put("MOYSRBAN", szMoysrban); // 催し連番
      obj.put("BMNCD", szBmncd); // 部門コード
      boolean isExist = this.checkMstExist("TOKCHIRASBMN", szBmncd, obj);
      isToktg_t = isExist; // アンケート有(チラシのみ)
      isToktg_h = !isExist; // アンケート有(販売・納入)
    }
  }

  public String getUpdFlg(String szMoyskbn, String szMoysstdt, String szMoysrban) {

    // 関連情報取得
    StringBuffer sbSQL = new StringBuffer();
    JSONArray dbDatas = new JSONArray();
    ItemList iL = new ItemList();
    ArrayList<String> prmData = new ArrayList<String>();

    prmData.add(szMoyskbn);
    prmData.add(szMoysstdt);
    prmData.add(szMoysrban);

    sbSQL.append("WITH SHUNO AS ( SELECT ");
    sbSQL.append("SHUNO ");
    sbSQL.append(",CAST(DATE_FORMAT(STARTDT + INTERVAL 3 DAY, '%Y%m%d') AS SIGNED) AS STDT_T");
    sbSQL.append(" , STARTDT");
    sbSQL.append(" , ENDDT ");
    sbSQL.append(" FROM INAAD.SYSSHUNO");
    sbSQL.append(" ) ");
    sbSQL.append(" , SHORIDT as (SELECT SHORIDT FROM INAAD.SYSSHORIDT) ");
    sbSQL.append(" SELECT");
    sbSQL.append(" CASE ");
    sbSQL.append(" WHEN T1.NENMATKBN = 0 ");
    sbSQL.append(" AND T2.STDT_T <= T3.SHORIDT THEN '1' ");
    sbSQL.append(" WHEN T1.NENMATKBN = 1 AND ");
    sbSQL.append(" CAST(DATE_FORMAT(T1.NNSTDT_TGF - INTERVAL 52 DAY ,'%Y%m%d') AS SIGNED) <= T3.SHORIDT THEN '1' ");
    sbSQL.append(" ELSE '' END AS FLG");
    sbSQL.append(" FROM INATK.TOKMOYCD T1");
    sbSQL.append(" , SHUNO T2");
    sbSQL.append(" , SHORIDT T3 ");
    sbSQL.append(" WHERE");
    sbSQL.append(" T1.MOYSKBN = ? ");
    sbSQL.append(" and T1.MOYSSTDT = ? ");
    sbSQL.append(" and T1.MOYSRBAN = ? ");
    sbSQL.append(" and T2.SHUNO = ( ");
    sbSQL.append(" SELECT shuno ");
    sbSQL.append(" FROM INAAD.SYSSHUNO T3 ");
    sbSQL.append(" WHERE");
    sbSQL.append(" CAST(DATE_FORMAT(T1.NNSTDT_TGF - INTERVAL 14 DAY,'%Y%m%d') AS SIGNED) >= T3.STARTDT ");
    sbSQL.append(" AND CAST(DATE_FORMAT(T1.NNSTDT_TGF - INTERVAL 14 DAY,'%Y%m%d') AS SIGNED ) <= T3.ENDDT)");

    dbDatas = iL.selectJSONArray(sbSQL.toString(), prmData, Defines.STR_JNDI_DS);

    if (dbDatas.size() != 0) {
      return dbDatas.getJSONObject(0).optString("FLG");
    }

    return "";
  }

  /**
   * 画面種別情報判断
   */
  public void setFrmInfo(String szAddshukbn) {
    if (StringUtils.isEmpty(szAddshukbn)) {
      isFrm1 = false;
      isFrm2 = false;
      isFrm3 = false;
      isFrm4 = false;
      isFrm5 = false;
    } else {
      if (DefineReport.ValAddShuKbn.VAL1.getVal().equals(szAddshukbn)) {
        isFrm5 = true;
      } else if (DefineReport.ValAddShuKbn.VAL2.getVal().equals(szAddshukbn)) {
        isFrm1 = true;
      } else if (DefineReport.ValAddShuKbn.VAL3.getVal().equals(szAddshukbn)) {
        isFrm4 = true;
      } else if (DefineReport.ValAddShuKbn.VAL4.getVal().equals(szAddshukbn)) {
        isFrm3 = true;
      } else if (DefineReport.ValAddShuKbn.VAL5.getVal().equals(szAddshukbn)) {
        isFrm2 = true;
      }
    }
  }

  private String createCommand() {
    // ログインユーザー情報取得
    User userInfo = getUserInfo();
    if (userInfo == null) {
      return "";
    }

    String szMoyskbn = getMap().get("MOYSKBN"); // 催し区分
    String szMoysstdt = getMap().get("MOYSSTDT"); // 催しコード（催し開始日）
    String szMoysrban = getMap().get("MOYSRBAN"); // 催し連番
    String szBmncd = getMap().get("BMNCD"); // 部門コード
    String szKanrino = getMap().get("KANRINO"); // 管理No.
    String szKanrieno = getMap().get("KANRIENO"); // 管理No.枝番 ※月間チラシ・特売スポット遷移時キー
    String szShncd = getMap().get("SHNCD"); // 商品コード ※催し送信遷移時検索キー
    String szAddshukbn = getMap().get("ADDSHUKBN"); // 登録種別
    String szMoyskbnC = getMap().get("MOYSKBN_C"); // 催し区分 ※コピー画面の場合
    String szMoysstdtC = getMap().get("MOYSSTDT_C"); // 催しコード（催し開始日） ※コピー画面の場合
    String szMoysrbanC = getMap().get("MOYSRBAN_C"); // 催し連番 ※コピー画面の場合
    String szBmncdC = getMap().get("BMNCD_C"); // 部門コード ※コピー画面の場合
    String szKanrinoC = getMap().get("KANRINO_C"); // 管理No. ※コピー画面の場合
    String szKanrienoC = getMap().get("KANRIENO_C"); // 管理No.枝番 ※コピー画面の場合

    String sendBtnid = getMap().get("SENDBTNID"); // 呼出しボタン
    String sendPageid = getMap().get("PAGEID"); // 呼出しPAGEID


    // パラメータ確認
    // 必須チェック
    if ((szMoyskbn == null) || (szMoysstdt == null) || (szMoysrban == null) || (szBmncd == null) || (sendBtnid == null)) {
      System.out.println(super.getConditionLog());
      return "";
    }


    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<String>();

    StringBuffer sbSQL = new StringBuffer();

    // 画面モード情報設定
    this.setModeInfo(sendPageid, sendBtnid);
    // 催し種類情報設定
    this.setMoycdInfo(szMoyskbn, szMoysstdt, szMoysrban, szBmncd);
    // フォーム情報設定
    this.setFrmInfo(szAddshukbn);

    String moysFlg = this.getUpdFlg(szMoyskbn, szMoysstdt, szMoysrban);

    // 2.2．ST019の【特売・スポット計画 コピー元商品選択】画面の「選択（確定）」よりの場合：
    // 2.2.1．コピー元商品の情報をコピーし、新規画面に表示する
    // 2.2.1.2.2．催しコードが異なる場合：納入情報タブ、販売期間、納入期間をコピーしない。
    boolean isCopyNotNN = isCopy && !(StringUtils.equals(szMoyskbn, szMoyskbnC) && StringUtils.equals(szMoysstdt, szMoysstdtC) && StringUtils.equals(szMoysrban, szMoysrbanC));


    String szTableSHN = "INATK.TOKTG_SHN"; // 全店特売(アンケート有/無)_商品
    String szTableTJTEN = "INATK.TOKTG_TJTEN"; // 全店特売(アンケート有/無)_対象除外店
    if (!isToktg) {
      szTableSHN = "INATK.TOKSP_SHN";
      szTableTJTEN = "INATK.TOKSP_TJTEN";
    }

    // 催し開始日を日付形式に加工
    String szMoysstdt2 = CmnDate.dateFormat(CmnDate.convYYMMDD(szMoysstdt));
    if (StringUtils.isEmpty(szKanrino)) {
      szKanrino = "-1";
    }

    sbSQL.append(DefineReport.ID_SQL_CMN_WEEK);
    /*** 対象除外店 ***/
    sbSQL.append(", JTEN as (");
    sbSQL.append(" select T1.MOYSKBN, T1.MOYSSTDT, T1.MOYSRBAN, T1.BMNCD,T1.KANRINO, T1.KANRIENO, T1.TJFLG");
    for (int i = 1; i <= 10; i++) {
      sbSQL.append(" ,max(case T1.RNO when " + i + " then T1.TENCD end) as TENCD_" + i);
      sbSQL.append(" ,max(case T1.RNO when " + i + " then T1.TENRANK end) as TENRANK_" + i);
    }
    sbSQL.append(" from (");
    sbSQL.append("  select T1.MOYSKBN, T1.MOYSSTDT, T1.MOYSRBAN, T1.BMNCD,T1.KANRINO, T1.KANRIENO, T1.TENCD,T1.TJFLG,T1.TENRANK");
    sbSQL.append("  ,row_number() over(partition by T1.MOYSKBN, T1.MOYSSTDT, T1.MOYSRBAN, T1.BMNCD,T1.KANRINO, T1.KANRIENO,T1.TJFLG order by T1.TENCD) as RNO");
    sbSQL.append("  from " + szTableTJTEN + " T1");
    if (isCopy) {
      sbSQL.append("  where T1.MOYSKBN= " + szMoyskbnC + "");
      sbSQL.append("  and T1.MOYSSTDT = " + szMoysstdtC + "");
      sbSQL.append("  and T1.MOYSRBAN = " + szMoysrbanC + "");
      sbSQL.append("  and T1.BMNCD    = " + szBmncdC + "");
      sbSQL.append("  and T1.KANRINO  = " + szKanrinoC + "");
      sbSQL.append("  and T1.KANRIENO = " + szKanrienoC + "");
    } else {
      sbSQL.append("  where T1.MOYSKBN= " + szMoyskbn + "");
      sbSQL.append("  and T1.MOYSSTDT = " + szMoysstdt + "");
      sbSQL.append("  and T1.MOYSRBAN = " + szMoysrban + "");
      sbSQL.append("  and T1.BMNCD    = " + szBmncd + "");
      sbSQL.append("  and T1.KANRINO  = " + szKanrino + "");
      if (StringUtils.isNotEmpty(szKanrieno)) {
        sbSQL.append("  and T1.KANRIENO = " + szKanrieno + "");
      }
    }
    sbSQL.append(" ) T1");
    sbSQL.append(" group by T1.MOYSKBN, T1.MOYSSTDT, T1.MOYSRBAN, T1.BMNCD,T1.KANRINO, T1.KANRIENO, T1.TJFLG");
    sbSQL.append(")");

    sbSQL.append(" select ");
    if (isModeC || isModeF || isModeG) {
      sbSQL.append("  '月締後変更処理'"); // F1 : 月締後変更処理 TODO
    } else {
      sbSQL.append("  ''"); // F1 : 月締後変更処理 TODO
    }
    sbSQL.append(" ,T1.SHUNO"); // F2 : 週№
    sbSQL.append(" ,T1.MOYSKBN||'-'||right('00'||T1.MOYSSTDT, 6)||'-'||right('000'||T1.MOYSRBAN, 3)"); // F3 : 催しコード
    sbSQL.append(" ,T1.MOYKN"); // F4 : 催し名称（漢字）
    if (isToktg) {
      sbSQL.append(" ,T2.HBOKUREFLG||'-'||case when T2.HBOKUREFLG = 1 then '有' else '無' end"); // F5：一日遅パタン
      sbSQL.append(" ,T3.HBSLIDEFLG"); // F6：一日遅スライドしない-販売
      sbSQL.append(" ,T3.NHSLIDEFLG"); // F7：一日遅スライドしない-納入
    } else {
      sbSQL.append(" ,null"); // F5：一日遅パタン
      sbSQL.append(" ,null"); // F6：一日遅スライドしない-販売
      sbSQL.append(" ,null"); // F7：一日遅スライドしない-納入
    }
    if (isCopy) {
      sbSQL.append(" ,right('00'||'" + szBmncdC + "', 2) as BMNCD"); // F8：部門
    } else {
      sbSQL.append(" ,right('00'||'" + szBmncd + "', 2) as BMNCD"); // F8：部門
    }
    sbSQL.append(" ,CASE WHEN T3.BYCD='0' THEN right('00'||'" + szBmncd + "', 2) || '00000' ELSE right('0000000'||T3.BYCD,7) END as BYCD"); // F9：BY

    sbSQL.append(" ,T3.SHNCD"); // F10：商品コード
    sbSQL.append(" ,M0.SHNKN"); // F11：商品マスタ名称

    sbSQL.append(" ,null"); // F12：仮JAN TODO:仮JANのなごり、不要

    sbSQL.append(" ,T3.PARNO"); // F13：グループNo.
    sbSQL.append(" ,T3.CHLDNO"); // F14：子No.
    sbSQL.append(" ,T3.HIGAWRFLG"); // F15：日替

    if (isCopyNotNN) {
      sbSQL.append(" ,null"); // F16：販売期間From
      sbSQL.append(" ,null"); // F17：販売期間To
      sbSQL.append(" ,null"); // F18：納入期間From
      sbSQL.append(" ,null"); // F19：納入期間To
    } else {
      sbSQL.append(" ,right(T3.HBSTDT,6)"); // F16：販売期間From
      sbSQL.append(" ,right(T3.HBEDDT,6)"); // F17：販売期間To
      sbSQL.append(" ,right(T3.NNSTDT,6)"); // F18：納入期間From
      sbSQL.append(" ,right(T3.NNEDDT,6)"); // F19：納入期間To
    }

    if (isToktg || (isToksp && !isNew)) {
      sbSQL.append(" ,T3.CHIRASFLG"); // F20：チラシ未掲載
    } else {
      sbSQL.append(" ," + DefineReport.Values.ON.getVal()); // F20：チラシ未掲載
    }

    if (isToktg) {
      sbSQL.append(" ,T3.RANKNO_ADD"); // F21：対象店
      sbSQL.append(" ,null"); // F22：除外店
    } else {
      sbSQL.append(" ,T3.RANKNO_ADD_A"); // F21：対象店
      sbSQL.append(" ,T3.RANKNO_DEL"); // F22：除外店
    }
    /** 追加・ランク・除外 */
    for (int i = 1; i <= 10; i++) {
      sbSQL.append(" ,T4.TENCD_" + i); // F23：追加（1～10）
    }
    for (int i = 1; i <= 10; i++) {
      sbSQL.append(" ,T4.TENRANK_" + i); // F33：ランク（1～10）
    }
    for (int i = 1; i <= 10; i++) {
      sbSQL.append(" ,T5.TENCD_" + i); // F43：除外（1～10）
    }
    /** 予定数 */
    sbSQL.append(" ,T3.HBYOTEISU"); // F53：予定数
    sbSQL.append(" ,null"); // F54：仕入額 TODO※画面で実行
    sbSQL.append(" ,null"); // F55：販売額 TODO※画面で実行
    sbSQL.append(" ,null"); // F56：荒利額 TODO※画面で実行

    sbSQL.append(" ,T3.TKANPLUKBN"); // F57：PLU商品・定貫商品／不定貫商品
    /** PLU商品・定貫商品 */
    // レギュラー
    sbSQL.append(" ,M0.RG_GENKAAM"); // F58：原価
    sbSQL.append(" ," + DefineReport.ID_SQL_TOKBAIKA_COL_SOU.replaceAll("@DT", szMoysstdt2).replaceAll("@BAIKA", "M0.RG_BAIKAAM")); // F59：A総売価
    sbSQL.append(" ,null"); // F60：本体売価 TODO※画面で実行
    sbSQL.append(" ,M0.RG_IRISU"); // F61：入数
    sbSQL.append(" ,null"); // F62：値入 TODO※画面で実行
    // 特売事前行
    sbSQL.append(" ,CASE WHEN CAST(T3.GENKAAM_MAE AS SIGNED)=0 THEN null ELSE T3.GENKAAM_MAE END GENKAAM_MAE "); // F63：原価
    sbSQL.append(" ,CASE WHEN IFNULL(T3.TKANPLUKBN,'1') <> '2' THEN T3.A_BAIKAAM ELSE NULL END A_BAIKAAM "); // F64：A総売価
    sbSQL.append(" ,null"); // F65：本体売価 TODO※画面で実行
    sbSQL.append(" ,CASE WHEN IFNULL(T3.TKANPLUKBN,'1') <> '2' THEN T3.IRISU ELSE NULL END IRISU "); // F66：入数
    sbSQL.append(" ,null"); // F67：値入 TODO※画面で実行
    // 特売追加行
    sbSQL.append(" ,T3.GENKAAM_ATO"); // F68：原価
    sbSQL.append(" ,null"); // F69：A総売価 表示・入力不可
    sbSQL.append(" ,null"); // F70：本体売価 表示・入力不可
    sbSQL.append(" ,null"); // F71：入数 表示・入力不可
    sbSQL.append(" ,null"); // F72：値入 TODO※画面で実行
    // B部分
    sbSQL.append(" ,CASE WHEN IFNULL(T3.TKANPLUKBN,'1') <> '2' THEN T3.B_BAIKAAM ELSE NULL END B_BAIKAAM "); // F73：B総売価
    sbSQL.append(" ,null"); // F74：本体売価 TODO※画面で実行
    sbSQL.append(" ,null"); // F75：値入 TODO※画面で実行
    if (isToktg) {
      sbSQL.append(" ,null"); // F76：B売店 編集不可(列無し)
    } else {
      sbSQL.append(" ,T3.RANKNO_ADD_B"); // F76：B売店
    }
    // C部分
    sbSQL.append(" ,CASE WHEN IFNULL(T3.TKANPLUKBN,'1') <> '2' THEN T3.C_BAIKAAM ELSE NULL END C_BAIKAAM "); // F77：C総売価
    sbSQL.append(" ,null"); // F78：本体売価 TODO※画面で実行
    sbSQL.append(" ,null"); // F79：値入 TODO※画面で実行
    if (isToktg) {
      sbSQL.append(" ,null"); // F80：C売店 編集不可(列無し)
    } else {
      sbSQL.append(" ,T3.RANKNO_ADD_C"); // F80：C売店
    }
    /** 不定貫商品 */
    // A総売価行
    sbSQL.append(" ,CASE WHEN T3.TKANPLUKBN = '2' THEN T3.A_BAIKAAM ELSE NULL END A_BAIKAAM "); // F81：100g総売価
    sbSQL.append(" ,T3.GENKAAM_1KG"); // F82：1Kg原価
    sbSQL.append(" ,T3.A_GENKAAM_1KG"); // F83：1Kg総売価
    sbSQL.append(" ,T3.GENKAAM_PACK"); // F84：P原価
    sbSQL.append(" ,T3.A_BAIKAAM_PACK"); // F85：P総売価
    sbSQL.append(" ,CASE WHEN T3.TKANPLUKBN = '2' THEN T3.IRISU ELSE NULL END IRISU "); // F86：入数
    // B総売価行
    sbSQL.append(" ,CASE WHEN T3.TKANPLUKBN = '2' THEN T3.B_BAIKAAM ELSE NULL END B_BAIKAAM "); // F87：100g総売価
    sbSQL.append(" ,null"); // F88：1Kg原価 表示・入力不可
    sbSQL.append(" ,T3.B_GENKAAM_1KG"); // F89：1Kg総売価
    sbSQL.append(" ,null"); // F90：P原価 表示・入力不可
    sbSQL.append(" ,T3.B_BAIKAAM_PACK"); // F91：P総売価
    sbSQL.append(" ,null"); // F92：入数 表示・入力不可
    // C総売価行
    sbSQL.append(" ,CASE WHEN T3.TKANPLUKBN = '2' THEN T3.C_BAIKAAM ELSE NULL END C_BAIKAAM "); // F93：100g総売価
    sbSQL.append(" ,null"); // F94：1Kg原価
    sbSQL.append(" ,T3.C_GENKAAM_1KG"); // F95：1Kg総売価
    sbSQL.append(" ,null"); // F96：P原価
    sbSQL.append(" ,T3.C_BAIKAAM_PACK"); // F97：P総売価
    sbSQL.append(" ,null"); // F98：入数

    // 発注原売価適用しない
    int def_htgenbaikaflg = 0;
    if (isNew) {
      if (isToktg_t) { // ① 新規初期値は1：適用しないに設置する。
        def_htgenbaikaflg = 1;
      } else {
        if (StringUtils.equals(szBmncd, "11")) { // ① 部門=11の時、新規初期値は0：適用するに設置し、変更不可。
          def_htgenbaikaflg = 0;
        } else { // ② 部門=11以外は、新規初期値は1：適用しないに設置し、変更可。
          def_htgenbaikaflg = 1;
        }
      }
      sbSQL.append(" ," + def_htgenbaikaflg); // F99：発注原売価適用しない(16_5)
    } else {
      sbSQL.append(" ,T3.HTGENBAIKAFLG"); // F99：発注原売価適用しない(16_5)
    }

    // 総売価部分
    sbSQL.append(" ,T3.A_WRITUKBN"); // F100：A総売価 ※16_4の場合F81
    sbSQL.append(" ,T3.B_WRITUKBN"); // F101：B総売価 ※16_4の場合F87
    sbSQL.append(" ,T3.C_WRITUKBN"); // F102：C総売価 ※16_4の場合F93

    // 【販売情報部分】
    sbSQL.append(" ,T3.SANCHIKN"); // F103：産地
    sbSQL.append(" ,T3.MAKERKN"); // F104：メーカー名
    sbSQL.append(" ,T3.POPKN"); // F105：POP名称
    sbSQL.append(" ,T3.KIKKN"); // F106：規格
    // 制限部分
    sbSQL.append(" ,T3.SEGN_NINZU"); // F107：先着人数
    sbSQL.append(" ,T3.SEGN_GENTEI"); // F108：限定表現
    sbSQL.append(" ,T3.SEGN_1KOSU"); // F109：一人
    sbSQL.append(" ,T3.SEGN_1KOSUTNI"); // F110：単位
    if (isNew) {
      if (isFrm1 && isToktg_t) {
        // ちらしのみ：チェック状態に設置し、編集不可。
        sbSQL.append(" ," + DefineReport.Values.ON.getVal()); // F111：PLU配信しない
      } else if (isFrm4) {
        // 画面上項目はないがDFで1(しない)をセット
        sbSQL.append(" ," + DefineReport.Values.ON.getVal()); // F111：PLU配信しない
      } else if (isFrm5 && isToktg_t && def_htgenbaikaflg == 1) {
        // ① 【画面】.「発注原売価適用しない」のチェックが無い場合、新規値は非チェック状態に設置する。
        // ② 【画面】.「発注原売価適用しない」がチェック状態の場合、新規値はチェック状態に設置し、編集不可。
        sbSQL.append(" ," + DefineReport.Values.ON.getVal()); // F111：PLU配信しない
      } else {
        sbSQL.append(" ,null"); // F111：PLU配信しない
      }
    } else {
      sbSQL.append(" ,T3.PLUSNDFLG"); // F111：PLU配信しない
    }
    if (isNew) {
      sbSQL.append(" ,null"); // F112：よりどり
    } else {
      sbSQL.append(" ,T3.YORIFLG"); // F112：よりどり
    }
    // 一個売り部分
    sbSQL.append(" ,T3.KO_A_BAIKAAN"); // F113：総売価A
    sbSQL.append(" ,T3.KO_B_BAIKAAN"); // F114：総売価B
    sbSQL.append(" ,T3.KO_C_BAIKAAN"); // F115：総売価C

    // バンドル1部分
    sbSQL.append(" ,T3.BD1_TENSU"); // F116：点数1
    sbSQL.append(" ,T3.BD1_A_BAIKAAN"); // F117：総売価1A
    sbSQL.append(" ,T3.BD1_B_BAIKAAN"); // F118：総売価1B
    sbSQL.append(" ,T3.BD1_C_BAIKAAN"); // F119：総売価1C

    // バンドル2部分
    sbSQL.append(" ,T3.BD2_TENSU"); // F120：点数2
    sbSQL.append(" ,T3.BD2_A_BAIKAAN"); // F121：総売価２A
    sbSQL.append(" ,T3.BD2_B_BAIKAAN"); // F122：総売価２B
    sbSQL.append(" ,T3.BD2_C_BAIKAAN"); // F123：総売価２C

    // 100g相当部分
    sbSQL.append(" ,T3.A_BAIKAAM_100G"); // F124：A総売価
    sbSQL.append(" ,T3.B_BAIKAAM_100G"); // F125：B総売価
    sbSQL.append(" ,T3.C_BAIKAAM_100G"); // F126：C総売価
    // 生食・加熱/解凍/養殖
    if (isNew) {
      sbSQL.append(" ,null"); // F127：生食・加熱-生食
      sbSQL.append(" ,null"); // F128：生食・加熱-加熱
      sbSQL.append(" ,null"); // F129：解凍
      sbSQL.append(" ,null"); // F130：養殖
    } else {
      sbSQL.append(" ,CASE WHEN T3.NAMANETUKBN='1' THEN '1' ELSE '0' END NAMANETUKBN "); // F127：生食・加熱-生食
      sbSQL.append(" ,CASE WHEN T3.NAMANETUKBN='2' THEN '1' ELSE '0' END NAMANETUKBN "); // F128：生食・加熱-加熱
      sbSQL.append(" ,T3.KAITOFLG"); // F129：解凍
      sbSQL.append(" ,T3.YOSHOKUFLG"); // F130：養殖
    }
    // チラシ・POP情報
    sbSQL.append(" ,T3.MEDAMAKBN"); // F131：目玉情報
    sbSQL.append(" ,T3.POPCD"); // F132：POPコード
    sbSQL.append(" ,T3.POPSZ"); // F133：POPサイズ
    sbSQL.append(" ,T3.POPSU"); // F134：枚数
    sbSQL.append(" ,T3.SHNSIZE"); // F135：商品サイズ
    sbSQL.append(" ,T3.SHNCOLOR"); // F136：商品色
    sbSQL.append(" ,T3.COMMENT_HGW"); // F137：その他日替コメント
    sbSQL.append(" ,T3.COMMENT_POP"); // F138：POPコメント

    // 【納入情報部分】TG016_1,TG016_2,TG016_3
    if (isNew || isCopyNotNN) {
      sbSQL.append(" ,null"); // F139：事前打出(チェック)
      sbSQL.append(" ,null"); // F140：事前打出(日付)
      sbSQL.append(" ,null"); // F141：特売コメント
      sbSQL.append(" ,null"); // F142：カット店展開しない
      sbSQL.append(" ,null"); // F143：便区分
      sbSQL.append(" ,null"); // F144：別伝区分
      sbSQL.append(" ,null"); // F145：ワッペン区分
      sbSQL.append(" ,null"); // F146：週次伝送flg
    } else {
      sbSQL.append(" ,CASE WHEN T3.JUFLG = '2' THEN '1' ELSE '0' END AS JUFLG "); // F139：事前打出(チェック)
      sbSQL.append(" ,right(T3.JUHTDT,6)"); // F140：事前打出(日付)
      sbSQL.append(" ,T3.COMMENT_TB"); // F141：特売コメント
      sbSQL.append(" ,T3.CUTTENFLG"); // F142：カット店展開しない
      sbSQL.append(" ,T3.BINKBN"); // F143：便区分
      sbSQL.append(" ,T3.BDENKBN"); // F144：別伝区分
      sbSQL.append(" ,T3.WAPPNKBN"); // F145：ワッペン区分
      sbSQL.append(" ,T3.SHUDENFLG"); // F146：週次伝送flg
    }
    sbSQL.append(" ,RTRIM(MEI.NMKN)"); // F147：PC区分 テキスト

    sbSQL.append(" ,T3.UPDKBN as UPDKBN"); // F148: 更新区分
    sbSQL.append(" ,T3.SENDFLG as SENDFLG"); // F149: 送信フラグ
    if (isCopy) {
      sbSQL.append(" ,null as OPERATOR"); // F150: オペレータ
      sbSQL.append(" ,'__/__/__' as ADDDT"); // F151: 登録日
      sbSQL.append(" ,'__/__/__' as UPDDT"); // F152: 更新日
      sbSQL.append(" ,null as HDN_UPDDT"); // F153: 更新日時
    } else {
      sbSQL.append(" ,T3.OPERATOR as OPERATOR"); // F150: オペレータ
      sbSQL.append(" ,IFNULL(DATE_FORMAT(T3.ADDDT, '%y/%m/%d'),'__/__/__') as ADDDT"); // F151: 登録日
      sbSQL.append(" ,IFNULL(DATE_FORMAT(T3.UPDDT, '%y/%m/%d'),'__/__/__') as UPDDT"); // F152: 更新日
      sbSQL.append(" ,DATE_FORMAT(T3.UPDDT, '%Y%m%d%H%i%s%f') as HDN_UPDDT"); // F153: 更新日時
    }
    if (isNew || isCopyNotNN) {
      sbSQL.append(" ,DF.DSUEXKBN"); // F154: 展開方法
    } else {
      sbSQL.append(" ,T3.TENKAIKBN"); // F154: 展開方法
    }
    if (isToktg) {
      sbSQL.append(" ,MT1.TENKN"); // F155：対象店
      sbSQL.append(" ,null"); // F156：除外店
    } else {
      sbSQL.append(" ,MT1.TENKN"); // F155：対象店
      sbSQL.append(" ,MT2.TENKN"); // F156：除外店
    }

    if (isNew || isCopyNotNN) {
      sbSQL.append(" ,DF.DRTEXKBN"); // F157：実績率パタン数値
      sbSQL.append(" ,DF.DDNENDSKBN"); // F158：実績率パタン前年同月
      sbSQL.append(" ,DF.DZNENDSKBN"); // F159：実績率パタン前年同週
    } else {
      sbSQL.append(" ,T3.JSKPTNSYUKBN"); // F157：実績率パタン数値
      sbSQL.append(" ,T3.JSKPTNZNENMKBN"); // F158：実績率パタン前年同月
      sbSQL.append(" ,T3.JSKPTNZNENWKBN"); // F159：実績率パタン前年同週
    }
    sbSQL.append(" ,M0.DAICD"); // F160：大分類
    sbSQL.append(" ,M0.CHUCD"); // F161：中分類
    if (isCopy) {
      sbSQL.append(" ,null"); // F162：店ランク配列
      sbSQL.append(" ,null"); // F163：事前発注リスト出力日
      sbSQL.append(" ,null"); // F164：事前発注数量取込日
      sbSQL.append(" ,null"); // F165：週間発注処理日
    } else {
      sbSQL.append(" ,T3.TENRANK_ARR"); // F162：店ランク配列
      sbSQL.append(" ,CASE WHEN T3.JLSTCREDT=0 THEN null ELSE T3.JLSTCREDT END "); // F163：事前発注リスト出力日
      sbSQL.append(" ,CASE WHEN T3.JHTSUINDT=0 THEN null ELSE T3.JHTSUINDT END "); // F164：事前発注数量取込日
      sbSQL.append(" ,CASE WHEN T3.WEEKHTDT=0 THEN null ELSE T3.WEEKHTDT END "); // F165：週間発注処理日
    }
    if (isCopy || isNew) {
      sbSQL.append("  ,T1.HBSTDT AS MYOSHBSTDT"); // F166：催し販売開始日
      sbSQL.append("  ,T1.HBEDDT AS MYOSHBEDDT"); // F167：催し販売終了日
      // 5.3.2.2.1．生鮮以外（催し_デフォルト設定.デフォルト_部門属性=0:ドライ）:
      // 納入開始日 = 催しコード.納入開始日 / 納入終了日 = 催しコード.納入終了日
      // 5.3.2.2.2．生鮮（催し_デフォルト設定.デフォルト_部門属性=1:生鮮）:
      // 納入開始日 = 催しコード.納入開始日_全特生鮮 / 納入終了日 = 催しコード.納入終了日_全特生鮮
      sbSQL.append("  ,case when DF.DBMNATRKBN = 1 then T1.NNSTDT_TGF else T1.NNSTDT end as MYOSNNSTDT"); // F168：催し納入開始日
      sbSQL.append("  ,case when DF.DBMNATRKBN = 1 then T1.NNEDDT_TGF else T1.NNEDDT end as MYOSNNEDDT"); // F169：催し納入終了日
    } else {
      sbSQL.append(" ,T3.MYOSHBSTDT"); // F166：催し販売開始日
      sbSQL.append(" ,T3.MYOSHBEDDT"); // F167：催し販売終了日
      sbSQL.append(" ,T3.MYOSNNSTDT"); // F168：催し納入開始日
      sbSQL.append(" ,T3.MYOSNNEDDT"); // F169：催し納入終了日
    }
    if (isToktg) {
      sbSQL.append(" ,T3.GTSIMECHGKBN"); // F170：月締変更理由
      sbSQL.append(" ,T3.GTSIMEOKFLG"); // F171：月締変更許可フラグ
    } else {
      sbSQL.append(" ,null"); // F170：月締変更理由
      sbSQL.append(" ,null"); // F171：月締変更許可フラグ
    }

    sbSQL.append(" ,M0.SHUBETUCD"); // F172：種別コード
    sbSQL.append(" ,M0.PCKBN"); // F173：PC区分
    sbSQL.append(" ,M0.SHNKBN"); // F174：商品種類
    sbSQL.append(" ,M0.SHOCD"); // F175：小分類
    sbSQL.append(" ,CASE WHEN M0.TEIKANKBN='0' THEN '2' WHEN M0.TEIKANKBN='1' THEN M0.TEIKANKBN ELSE '1' END AS TEIKANKBN "); // F176：定貫区分
    sbSQL.append(" , '" + moysFlg + "'"); // F177 // F176：定貫区分
    sbSQL.append(" from (");
    sbSQL.append("  select * from INATK.TOKMOYCD T1");
    sbSQL.append("  where IFNULL(T1.UPDKBN, 0) <> 1");
    sbSQL.append("  and T1.MOYSKBN  = " + szMoyskbn + "");
    sbSQL.append("  and T1.MOYSSTDT = " + szMoysstdt + "");
    sbSQL.append("  and T1.MOYSRBAN = " + szMoysrban + "");
    sbSQL.append(" ) T1 ");
    sbSQL.append(" left join INATK.TOKTG_KHN T2 on T1.MOYSKBN = T2.MOYSKBN and T1.MOYSSTDT = T2.MOYSSTDT and T1.MOYSRBAN = T2.MOYSRBAN and IFNULL(T2.UPDKBN, 0) <> 1 ");
    sbSQL.append(" left join " + szTableSHN + " T3 on IFNULL(T3.UPDKBN, 0) <> 1 ");
    if (isCopy) {
      sbSQL.append("  and T3.MOYSKBN  = " + szMoyskbnC + "");
      sbSQL.append("  and T3.MOYSSTDT = " + szMoysstdtC + "");
      sbSQL.append("  and T3.MOYSRBAN = " + szMoysrbanC + "");
      sbSQL.append("  and T3.BMNCD    = " + szBmncdC + "");
      sbSQL.append("  and T3.KANRINO  = " + szKanrinoC + "");
      sbSQL.append("  and T3.KANRIENO = " + szKanrienoC + "");
    } else {
      sbSQL.append("  and T1.MOYSKBN = T3.MOYSKBN and T1.MOYSSTDT = T3.MOYSSTDT and T1.MOYSRBAN = T3.MOYSRBAN");
      sbSQL.append("  and T3.BMNCD    = " + szBmncd + "");
      sbSQL.append("  and T3.KANRINO  = " + szKanrino + "");
      if (StringUtils.isNotEmpty(szKanrieno)) {
        sbSQL.append("  and T3.KANRIENO = " + szKanrieno + "");
      }
    }
    if (StringUtils.isNotEmpty(szShncd)) {
      sbSQL.append("  and T3.SHNCD = '" + szShncd + "'");
    }
    if (isCopy) {
      sbSQL.append(" left join INATK.TOKMOYDEF DF on T3.BMNCD = DF.BMNCD and IFNULL(DF.UPDKBN, 0) <> 1");
    } else if (isNew) {
      sbSQL.append(" left join INATK.TOKMOYDEF DF on DF.BMNCD = " + szBmncd + " and IFNULL(DF.UPDKBN, 0) <> 1");
    }
    sbSQL.append(
        " left join JTEN T4 on T3.MOYSKBN = T4.MOYSKBN and T3.MOYSSTDT = T4.MOYSSTDT and T3.MOYSRBAN = T4.MOYSRBAN and T3.BMNCD = T4.BMNCD and T3.KANRINO = T4.KANRINO and T3.KANRIENO = T4.KANRIENO and T4.TJFLG = 1");
    sbSQL.append(
        " left join JTEN T5 on T3.MOYSKBN = T5.MOYSKBN and T3.MOYSSTDT = T5.MOYSSTDT and T3.MOYSRBAN = T5.MOYSRBAN and T3.BMNCD = T5.BMNCD and T3.KANRINO = T5.KANRINO and T3.KANRIENO = T5.KANRIENO and T5.TJFLG = 2");
    sbSQL.append(DefineReport.ID_SQL_TOKBAIKA_JOIN.replace("@", "T3."));
    if (isToktg) {
      sbSQL.append(" left join INAMS.MSTTEN MT1 on T3.RANKNO_ADD = MT1.TENCD and IFNULL(MT1.UPDKBN, 0) <> 1 ");
    } else {
      sbSQL.append(" left join INAMS.MSTTEN MT1 on T3.RANKNO_ADD_A = MT1.TENCD and IFNULL(MT1.UPDKBN, 0) <> 1 ");
      sbSQL.append(" left join INAMS.MSTTEN MT2 on T3.RANKNO_DEL = MT2.TENCD and IFNULL(MT1.UPDKBN, 0) <> 1 ");
    }
    sbSQL.append(" left join INAMS.MSTMEISHO MEI on MEI.MEISHOKBN = '" + DefineReport.MeisyoSelect.KBN102.getCd() + "' and MEI.MEISHOCD = M0.PCKBN ");
    sbSQL.append(" left join INAMS.MSTMAKER MA on MA.MAKERCD = M0.MAKERCD and IFNULL(MA.UPDKBN,0) <> 1 ");

    // 基本情報取得
    JSONArray array1 = getTOKMOYCDData(getMap());

    // 納入日情報取得
    JSONArray array2 = getNNDTData(getMap());

    // 販売日情報取得
    JSONArray array3 = getHBDTData(getMap());

    // オプション情報（タイトル）設定
    JSONObject option = new JSONObject();
    option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
    option.put("rows_", array1);
    option.put("rows_nndt", array2);
    option.put("rows_hbdt", array3);
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

  /**
   * ベース情報取得処理
   *
   * @throws Exception
   */
  public JSONArray getTOKMOYCDData(HashMap<String, String> map) {
    String szMoyskbn = map.get("MOYSKBN"); // 催し区分
    String szMoysstdt = map.get("MOYSSTDT"); // 催しコード（催し開始日）
    String szMoysrban = map.get("MOYSRBAN"); // 催し連番
    String szBmncd = map.get("BMNCD"); // 部門コード

    ArrayList<String> paramData = new ArrayList<String>();

    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append(" select");
    sbSQL.append("  T1.MOYSKBN"); // F1 : 催し区分
    sbSQL.append(" ,T1.MOYSSTDT"); // F2 : 催し開始日
    sbSQL.append(" ,T1.MOYSRBAN"); // F3 : 催し連番
    sbSQL.append(" ,T1.SHUNO"); // F4 : 週№
    sbSQL.append(" ,T1.MOYKN"); // F5 : 催し名称（漢字）
    sbSQL.append(" ,T1.MOYAN"); // F6 : 催し名称（カナ）
    sbSQL.append(" ,T1.NENMATKBN"); // F7 : 年末区分
    sbSQL.append(" ,T1.HBSTDT"); // F8 : 販売開始日
    sbSQL.append(" ,T1.HBEDDT"); // F9 : 販売終了日
    sbSQL.append(" ,T1.NNSTDT"); // F10: 納入開始日
    sbSQL.append(" ,T1.NNEDDT"); // F11: 納入終了日
    sbSQL.append(" ,T1.NNSTDT_TGF"); // F12: 納入開始日_全特生鮮
    sbSQL.append(" ,T1.NNEDDT_TGF"); // F13: 納入終了日_全特生鮮
    sbSQL.append(" ,T1.PLUSDDT"); // F14: PLU配信日
    sbSQL.append(" ,T1.PLUSFLG"); // F15: PLU配信済フラグ
    sbSQL.append(" ,T2.HBOKUREFLG"); // F4 : 販売日1日遅許可フラグ
    sbSQL.append(" ,T2.GTSIMEDT"); // F5 : 月締日
    sbSQL.append(" ,T2.GTSIMEFLG"); // F6 : 月締フラグ
    sbSQL.append(" ,T2.LSIMEDT"); // F7 : 最終締日
    sbSQL.append(" ,T2.QAYYYYMM"); // F8 : アンケート月度
    sbSQL.append(" ,T2.QAENO"); // F9 : アンケート月度枝番
    sbSQL.append(" ,T2.QACREDT"); // F10: アンケート作成日
    sbSQL.append(" ,T2.QARCREDT"); // F11: アンケート再作成日
    sbSQL.append(" ,T2.JLSTCREFLG"); // F12: 事前発注リスト作成済フラグ
    sbSQL.append(" ,T2.HNCTLFLG"); // F13: 本部コントロールフラグ
    sbSQL.append(" ,T2.TPNG1FLG"); // F14: 店不採用禁止フラグ
    sbSQL.append(" ,T2.TPNG2FLG"); // F15: 店売価選択禁止フラグ
    sbSQL.append(" ,T2.TPNG3FLG"); // F16: 店商品選択禁止フラグ
    sbSQL.append(" ,T2.SIMEFLG1_LD"); // F17: 仮締フラグ_リーダー店
    sbSQL.append(" ,T2.SIMEFLG2_LD"); // F18: 本締フラグ_リーダー店
    sbSQL.append(" ,T2.SIMEFLG_MB"); // F19: 本締フラグ_各店
    sbSQL.append(" ,T2.QADEVSTDT"); // F20: アンケート取込開始日
    sbSQL.append(" ,CASE WHEN ");
    sbSQL.append(" (SELECT DBMNATRKBN FROM INATK.TOKMOYDEF WHERE BMNCD = " + szBmncd + " and IFNULL(UPDKBN, 0) <> 1) = '1' THEN T1.NNSTDT_TGF ");
    sbSQL.append(" ELSE T1.NNSTDT END AS CHK_NNSTDT "); // F21:チェック用納入開始日
    sbSQL.append(" ,CASE WHEN ");
    sbSQL.append(" (SELECT DBMNATRKBN FROM INATK.TOKMOYDEF WHERE BMNCD = " + szBmncd + " and IFNULL(UPDKBN, 0) <> 1) = '1' THEN T1.NNEDDT_TGF ");
    sbSQL.append(" ELSE T1.NNEDDT END AS CHK_NNEDDT "); // F22:チェック用納入終了日
    sbSQL.append(" from INATK.TOKMOYCD T1");
    sbSQL.append(" left join INATK.TOKTG_KHN T2 on T1.MOYSKBN = T2.MOYSKBN and T1.MOYSSTDT = T2.MOYSSTDT and T1.MOYSRBAN = T2.MOYSRBAN and T2.UPDKBN = 0");
    sbSQL.append(" where T1.UPDKBN = 0");
    sbSQL.append("   and T1.MOYSKBN = " + szMoyskbn + "");
    sbSQL.append("   and T1.MOYSSTDT = " + szMoysstdt + "");
    sbSQL.append("   and T1.MOYSRBAN = " + szMoysrban + "");

    ItemList iL = new ItemList();
    @SuppressWarnings("static-access")
    JSONArray array = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    return array;
  }

  /**
   * BYCD取得
   *
   * @throws Exception
   */
  public JSONArray getByCd(String byCd) {

    ArrayList<String> paramData = new ArrayList<String>();
    String sqlWhere = "";

    if (StringUtils.isEmpty(byCd)) {
      sqlWhere += "SHAINCD=null ";
    } else {
      sqlWhere += "SHAINCD=? ";
      paramData.add(byCd);
    }

    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append(" SELECT");
    sbSQL.append("  COUNT(SHAINCD) COUNT");
    sbSQL.append(" from INAAD.SYSLOGIN");
    sbSQL.append(" where " + sqlWhere);

    ItemList iL = new ItemList();
    @SuppressWarnings("static-access")
    JSONArray array = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    return array;
  }

  /**
   * 納入日情報取得処理
   *
   * @throws Exception
   */
  public JSONArray getNNDTData(HashMap<String, String> map) {
    String szMoyskbn = map.get("MOYSKBN"); // 催し区分
    String szMoysstdt = map.get("MOYSSTDT"); // 催しコード（催し開始日）
    String szMoysrban = map.get("MOYSRBAN"); // 催し連番
    String szBmncd = map.get("BMNCD"); // 部門コード
    String szKanrino = map.get("KANRINO"); // 管理No.
    String szKanrieno = map.get("KANRIENO"); // 管理No.枝番 ※月間チラシ・特売スポット遷移時キー
    String szShncd = map.get("SHNCD"); // 商品コード ※催し送信遷移時検索キー
    map.get("ADDSHUKBN");
    String szMoyskbnC = map.get("MOYSKBN_C"); // 催し区分 ※コピー画面の場合
    String szMoysstdtC = map.get("MOYSSTDT_C");// 催しコード（催し開始日） ※コピー画面の場合
    String szMoysrbanC = map.get("MOYSRBAN_C");// 催し連番 ※コピー画面の場合
    String szBmncdC = map.get("BMNCD_C"); // 部門コード ※コピー画面の場合
    String szKanrinoC = map.get("KANRINO_C"); // 管理No. ※コピー画面の場合
    String szKanrienoC = map.get("KANRIENO_C");// 管理No.枝番 ※コピー画面の場合

    String sendBtnid = map.get("SENDBTNID"); // 呼出しボタン
    String sendPageid = map.get("PAGEID"); // 呼出しPAGEID

    String szTenkaikbn = map.get("TENKAIKBN"); // 展開方法
    String szJskptnsyukbn = map.get("JSKPTNSYUKBN"); // 実績率パタン数値
    String szJskptnznenmkbn = map.get("JSKPTNZNENMKBN"); // 実績率パタン前年同月
    String szJskptnznenwkbn = map.get("JSKPTNZNENWKBN"); // 実績率パタン前年同週

    // 全店特売アンケート有/無
    boolean isTOKTG = super.isTOKTG(szMoyskbn, szMoysrban);
    // 画面モード情報設定
    this.setModeInfo(sendPageid, sendBtnid);

    // 2.2．ST019の【特売・スポット計画 コピー元商品選択】画面の「選択（確定）」よりの場合：
    // 2.2.1．コピー元商品の情報をコピーし、新規画面に表示する
    // 2.2.1.2.2．催しコードが異なる場合：納入情報タブ、販売期間、納入期間をコピーしない。
    boolean isCopyNN = isCopy && (StringUtils.equals(szMoyskbn, szMoyskbnC) && StringUtils.equals(szMoysstdt, szMoysstdtC) && StringUtils.equals(szMoysrban, szMoysrbanC));

    String szTableSHN = "INATK.TOKTG_SHN"; // 全店特売(アンケート有/無)_商品
    String szTableNNDT = "INATK.TOKTG_NNDT"; // 全店特売(アンケート有/無)_納入日
    if (!isTOKTG) {
      szTableSHN = "INATK.TOKSP_SHN";
      szTableNNDT = "INATK.TOKSP_NNDT";
    }
    CmnDate.dateFormat(CmnDate.convYYMMDD(szMoysstdt));
    if (StringUtils.isEmpty(szKanrino)) {
      szKanrino = "-1";
    }


    ArrayList<String> paramData = new ArrayList<String>();

    // 2.1.4．【画面】.「納入情報」タブ上の「パターンNo.」と「パターン年月表示」の制御：
    // 2.1.4.1．前提：
    // 2.1.4.1.1．全店特売（アンケート有/無）_商品.展開方法 = 3：実績率パターンの場合、「パターン年月表示」には”YYMM月” OR “YYNN週”の4桁を入力する。
    // 2.1.4.1.2．数値展開方法に基き、その商品の部門OR標準大分類OR標準中分類を使う。展開に用いた分類は、全店特売（アンケート有/無）の大分類、中分類に保持する。

    // 2.1.4.2．TG016の新規画面OPEN時（CSVデータ取込時の実績率パターンNo.決定ロジックにおいては取込日（処理日付）を登録日として以下のロジックを実行する。）：
    // 2.1.4.2.1．全店特売（アンケート有/無）_商品.展開方法 = 3：実績率パターンの場合：
    // ①【画面】.「納入情報」タブのスプレッドにおいて、NULLを「パターンNo.」列にデフォルト表示し、登録日より求めた前年同週（or前年同月）を「パターン年月表示」行に表示する。表示列数：10列。
    // ②数値展開方法より「パターン年月表示」行の表示内容を取得する：
    // ・実績率pt数値 = 1：売上の場合、”売”を表示する。実績率pt数値 = 2：点数の場合、”数”を表示する。
    // ・実績率pt前年同週/同月の場合、”YYNN週” / “YYMM月”を表示する。
    // ・実績率pt前年同週/同月の部門実績場合、”部”を表示する。大実績の場合、”大”を表示する。中実績の場合、”中”を表示する。
    // ③説明：入力9999は前年同月OR 前年同週の意味。DB上は全店特売(アンケート有/無)_納入日.パターンNo.へは展開に使う実際の値を保持する。
    // 例：04年11月30日から始まる催し（041130～041205）の登録を04年8月31日に行った場合、"0308月（売中）"と「パターン年月表示」行へ10日間分表示（常に固定で10日間表示）。

    // 2.1.4.2.2．全店特売（アンケート有/無）_商品.展開方法 = 3：実績率パターン以外の場合、
    // ・「パターン年月表示」に"通常数pt" OR "通常率pt"を表示。
    // ・「パターンNo.」には何も表示しない。

    // 2.1.4.2.3．催し_デフォルト設定.一日遅スライド_販売、一日遅スライド_納入、カット店展開を参照し、画面に初期表示を行う。ただし、以下の点に注意。
    // ・  画面初期表示がDISABLEの場合は初期値設定を行わない
    // ・  一日遅れスライドしない（販売、納入）はアンケート有りで、その催しが販売日一日遅れ許可されている場合のみ初期値設定を行う。

    // 2.1.4.3．TG016の修正画面OPEN時：
    // 2.1.4.3.1．新規・更新登録時の全店特売（アンケート有/無）_納入日.パターンNo.を「パターンNo.」列にデフォルト表示する。
    // 2.1.4.3.2．数値展開方法のデフォルト値が実績率ptの場合：
    // ①「パターン年月表示」列の表示において、新規を参照（2.1.4.2.1.）する。ただし年月・年週は更新時の処理日付より算出する。
    // ②説明：前回画面上で9999を入力した場合も、次回の初期表示では9999とは表示せず、実際に展開に使ったパターンNo.を表示する。
    // 例：04年11月30日から始まる催し（041130～041205）の登録を04年8月31日に行い、そのデータの更新を04年9月1日に行った場合：当日の日付より"0309月（売中）"と「パターン年月表示」行へ10日分表示（常に固定で10日間）。


    // 詳細部分
    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append("WITH RECURSIVE ");
    sbSQL.append(DefineReport.ID_SQL_CMN_WEEK.substring(5));
    sbSQL.append(",WK as (");
    sbSQL.append(" select * ");
    sbSQL.append(" ,case T1.JSKPTNSYUKBN " + " when " + DefineReport.ValKbn10008.VAL1.getVal() + " then '売'" + " when " + DefineReport.ValKbn10008.VAL2.getVal() + " then '点'"
        + " else '' end as JSKPTNSYUKBN_NM"); // 実績率パタン数値txt
    sbSQL.append(" ,case T1.JSKPTNZNENMKBN " + " when " + DefineReport.ValKbn10009.VAL1.getVal() + " then '" + DefineReport.ValKbn10009.VAL1.getTxt() + "'" + " when "
        + DefineReport.ValKbn10009.VAL2.getVal() + " then '" + DefineReport.ValKbn10009.VAL2.getTxt() + "'" + " when " + DefineReport.ValKbn10009.VAL3.getVal() + " then '"
        + DefineReport.ValKbn10009.VAL3.getTxt() + "'" + " else '' end as JSKPTNZNENMKBN_NM"); // 実績率パタン前年同月txt
    sbSQL.append(" ,case T1.JSKPTNZNENWKBN " + " when " + DefineReport.ValKbn10009.VAL1.getVal() + " then '" + DefineReport.ValKbn10009.VAL1.getTxt() + "'" + " when "
        + DefineReport.ValKbn10009.VAL2.getVal() + " then '" + DefineReport.ValKbn10009.VAL2.getTxt() + "'" + " when " + DefineReport.ValKbn10009.VAL3.getVal() + " then '"
        + DefineReport.ValKbn10009.VAL3.getTxt() + "'" + " else '' end as JSKPTNZNENWKBN_NM"); // 実績率パタン前年同週txt
    sbSQL.append(" ,S1.SHUNO||'週' as ZNENW"); // 実績率パタン前年週 TODO
    sbSQL.append(" ,DATE_FORMAT(T1.ZNENDT, '%y%m')||'月' as ZNENM"); // 実績率パタン前年月 TODO
    sbSQL.append(" from (");
    sbSQL.append("  select ");
    sbSQL.append("  T1.MOYSKBN, T1.MOYSSTDT, T1.MOYSRBAN, T3.BMNCD, T3.KANRINO, T3.KANRIENO");
    // 納入情報比較用：5.3．販売期間範囲、納入期間範囲の定義
    if (isNew || (isCopy && !isCopyNN)) {
      sbSQL.append("  ,T1.HBSTDT"); // 販売開始日 = 催しコード.販売開始日
      sbSQL.append("  ,T1.HBEDDT"); // 販売終了日 = 催しコード.販売終了日
      // 5.3.2.2.1．生鮮以外（催し_デフォルト設定.デフォルト_部門属性=0:ドライ）:
      // 納入開始日 = 催しコード.納入開始日 / 納入終了日 = 催しコード.納入終了日
      // 5.3.2.2.2．生鮮（催し_デフォルト設定.デフォルト_部門属性=1:生鮮）:
      // 納入開始日 = 催しコード.納入開始日_全特生鮮 / 納入終了日 = 催しコード.納入終了日_全特生鮮
      sbSQL.append("  ,case when DF.DBMNATRKBN = 1 then T1.NNSTDT_TGF else T1.NNSTDT end as NNSTDT");
      sbSQL.append("  ,case when DF.DBMNATRKBN = 1 then T1.NNEDDT_TGF else T1.NNEDDT end as NNEDDT");
      sbSQL.append("  ,case when DF.DBMNATRKBN = 1 ");
      sbSQL.append("   then (case when T1.NNEDDT_TGF > T1.HBEDDT then T1.NNEDDT_TGF else T1.HBEDDT end)");
      sbSQL.append("   else (case when T1.NNEDDT > T1.HBEDDT then T1.NNEDDT else T1.HBEDDT end) end as COMPDT");
    } else {
      sbSQL.append("  ,T3.HBSTDT");
      sbSQL.append("  ,T3.HBEDDT");
      sbSQL.append("  ,CASE WHEN T3.MYOSNNSTDT IS NULL THEN T3.NNSTDT ELSE T3.MYOSNNSTDT END NNSTDT");
      sbSQL.append("  ,CASE WHEN T3.MYOSNNEDDT IS NULL THEN T3.NNEDDT ELSE T3.MYOSNNEDDT END NNEDDT");
      sbSQL.append("  ,case when T3.MYOSNNEDDT IS NOT NULL AND T3.MYOSHBEDDT IS NOT NULL THEN ");
      sbSQL.append("  case when T3.MYOSNNEDDT > T3.MYOSHBEDDT then T3.MYOSNNEDDT else T3.MYOSHBEDDT end ");
      sbSQL.append("  when T3.MYOSNNEDDT IS NULL AND T3.MYOSHBEDDT IS NOT NULL THEN  ");
      sbSQL.append("  case when T3.NNEDDT > T3.MYOSHBEDDT then T3.NNEDDT else T3.MYOSHBEDDT end ");
      sbSQL.append("  else case when T3.NNEDDT > T3.HBEDDT then T3.NNEDDT else T3.HBEDDT end end COMPDT");
    }
    if (StringUtils.isNotEmpty(szTenkaikbn)) {
      sbSQL.append("  ,'" + szTenkaikbn + "' as TENKAIKBN"); // 展開方法
      sbSQL.append("  ,'" + szJskptnsyukbn + "' as JSKPTNSYUKBN"); // 実績率パタン数値
      sbSQL.append("  ,'" + szJskptnznenmkbn + "' as JSKPTNZNENMKBN"); // 実績率パタン前年同月
      sbSQL.append("  ,'" + szJskptnznenwkbn + "' as JSKPTNZNENWKBN"); // 実績率パタン前年同週
    } else if (isNew || (isCopy && !isCopyNN)) {
      sbSQL.append("  ,DF.DSUEXKBN as TENKAIKBN"); // 展開方法
      sbSQL.append("  ,DF.DRTEXKBN as JSKPTNSYUKBN"); // 実績率パタン数値
      sbSQL.append("  ,DF.DZNENDSKBN as JSKPTNZNENMKBN"); // 実績率パタン前年同月
      sbSQL.append("  ,DF.DDNENDSKBN as JSKPTNZNENWKBN"); // 実績率パタン前年同週
    } else {
      sbSQL.append("  ,T3.TENKAIKBN"); // 展開方法
      sbSQL.append("  ,T3.JSKPTNSYUKBN"); // 実績率パタン数値
      sbSQL.append("  ,T3.JSKPTNZNENMKBN"); // 実績率パタン前年同月
      sbSQL.append("  ,T3.JSKPTNZNENWKBN"); // 実績率パタン前年同週
    }
    if (isNew || (isCopy && !isCopyNN)) {
      sbSQL.append("  ,T1.ADDDT - INTERVAL 1 year as ZNENDT"); // 実績率パタン前年日
    } else {
      sbSQL.append("  ,T1.UPDDT - INTERVAL 1 year as ZNENDT"); // 実績率パタン前年日
    }
    sbSQL.append("  from (");
    sbSQL.append("   select * from INATK.TOKMOYCD T1");
    sbSQL.append("   where IFNULL(T1.UPDKBN, 0) <> 1");
    if (isCopyNN) {
      sbSQL.append("   and T1.MOYSKBN  = " + szMoyskbnC + "");
      sbSQL.append("   and T1.MOYSSTDT = " + szMoysstdtC + "");
      sbSQL.append("   and T1.MOYSRBAN = " + szMoysrbanC + "");
    } else {
      sbSQL.append("   and T1.MOYSKBN  = " + szMoyskbn + "");
      sbSQL.append("   and T1.MOYSSTDT = " + szMoysstdt + "");
      sbSQL.append("   and T1.MOYSRBAN = " + szMoysrban + "");
    }
    sbSQL.append("  ) T1 ");
    sbSQL.append("  left join " + szTableSHN + " T3 on IFNULL(T3.UPDKBN, 0) <> 1 ");
    sbSQL.append("  and T1.MOYSKBN = T3.MOYSKBN and T1.MOYSSTDT = T3.MOYSSTDT and T1.MOYSRBAN = T3.MOYSRBAN");
    if (isCopyNN) {
      sbSQL.append("  and T3.BMNCD    = " + szBmncdC + "");
      sbSQL.append("  and T3.KANRINO  = " + szKanrinoC + "");
      sbSQL.append("  and T3.KANRIENO = " + szKanrienoC + "");
    } else {
      sbSQL.append("  and T3.BMNCD    = " + szBmncd + "");
      sbSQL.append("  and T3.KANRINO  = " + szKanrino + "");
      if (StringUtils.isNotEmpty(szKanrieno)) {
        sbSQL.append("  and T3.KANRIENO = " + szKanrieno + "");
      }
    }
    if (StringUtils.isNotEmpty(szShncd)) {
      sbSQL.append("  and T3.SHNCD = '" + szShncd + "'");
    }
    sbSQL.append("  left join INATK.TOKMOYDEF DF on DF.BMNCD = " + szBmncd + " and IFNULL(DF.UPDKBN, 0) <> 1");
    sbSQL.append(" ) T1");
    sbSQL.append(" left join INAAD.SYSSHUNO S1 on DATE_FORMAT(T1.ZNENDT, '%Y%m%d') between S1.STARTDT and S1.ENDDT");
    sbSQL.append(")");

    sbSQL.append(",CAL(IDX, DT) as (");
    sbSQL.append(" select 1 , NNSTDT  from WK");
    sbSQL.append(" union all ");
    sbSQL.append(" select IDX + 1 , DATE_FORMAT(DT + INTERVAL 1 day,'%Y%m%d')  from CAL where IDX < 10 ");
    sbSQL.append(" ) ");

    sbSQL.append(" select ");
    sbSQL.append("  IFNULL(DATE_FORMAT(M1.DT, '%m/%d'), '') as N1"); // F148：日付（1～10）
    sbSQL.append(" ,max(JWEEK2) as N2"); // F158：曜日（1～10）
    if (isNew || (isCopy && !isCopyNN)) {
      sbSQL.append(" ," + DefineReport.Values.OFF.getVal() + " as N3"); // F168：販売日（1～10）
      sbSQL.append(" ," + DefineReport.Values.OFF.getVal() + " as N4"); // F178：納入日（1～10）
      sbSQL.append(" ,null as N5"); // F188：発注総数（1～10）+合計：
    } else {
      sbSQL.append(" ,max(case when CAST(M1.DT AS SIGNED) between T1.HBSTDT and T1.HBEDDT then " + DefineReport.Values.ON.getVal() + " else " + DefineReport.Values.OFF.getVal() + " end) as N3"); // F168：販売日（1～10）
      sbSQL.append(" ,max(case when T6.NNDT IS NOT NULL AND T6.NNDT = CAST(M1.DT AS SIGNED) then " + DefineReport.Values.ON.getVal() + " else " + DefineReport.Values.OFF.getVal() + " end) as N4"); // F178：納入日（1～10）
      sbSQL.append(" ,sum(case when T1.TENKAIKBN='2' then null else T6.HTASU end) as N5"); // F188：発注総数（1～10）+合計：
    }
    sbSQL.append(" ,max(case T1.TENKAIKBN " + " when " + DefineReport.ValKbn10007.VAL1.getVal() + " then '通常率pt'" + " when " + DefineReport.ValKbn10007.VAL2.getVal() + " then '通常数pt'" + " when "
        + DefineReport.ValKbn10007.VAL3.getVal() + " then case T1.JSKPTNZNENMKBN when " + DefineReport.ValKbn10009.VAL0.getVal() + " then ZNENW||'('||JSKPTNZNENWKBN_NM||JSKPTNSYUKBN_NM||')'"
        + " else ZNENM||'('||JSKPTNZNENMKBN_NM||JSKPTNSYUKBN_NM||')' end" + " else null end) as N6"); // F
    if (isNew || (isCopy && !isCopyNN)) {
      sbSQL.append(" ,null as N7"); // F199：パターンNo.（1～10）
      sbSQL.append(" ,null as N8"); // F209：訂正区分（1～10）
      sbSQL.append(" ,null as N9"); // F219：店舗数（1～10） ：全店特売（アンケート有/無）_納入日.店舗数
      sbSQL.append(" ,null as N10"); // F229：展開数（1～10）+合計：全店特売（アンケート有/無）_納入日.展開数
      sbSQL.append(" ,null as N11"); // F230：数量差（1～10）+合計：「発注総数」－「展開数」
      sbSQL.append(" ,null as N12"); // F241：原価計（1～10）+合計：※画面にて実行
      sbSQL.append(" ,null as N13"); // F252：本売価計(1～10)+合計：※画面にて実行
      sbSQL.append(" ,null as N14"); // F263：荒利額（1～10）+合計：※画面にて実行
      sbSQL.append(" ,null as N50"); // データ確認用
    } else {
      sbSQL.append(" ,max(T6.PTNNO) as N7"); // F199：パターンNo.（1～10）
      sbSQL.append(" ,max(T6.TSEIKBN) as N8"); // F209：訂正区分（1～10）
      sbSQL.append(" ,sum(T6.TPSU) as N9"); // F219：店舗数（1～10） ：全店特売（アンケート有/無）_納入日.店舗数
      sbSQL.append(" ,sum(T6.TENKAISU) as N10"); // F229：展開数（1～10）+合計：全店特売（アンケート有/無）_納入日.展開数
      sbSQL.append(" ,sum(T6.HTASU-T6.TENKAISU) as N11"); // F230：数量差（1～10）+合計：「発注総数」－「展開数」
      sbSQL.append(" ,null as N12"); // F241：原価計（1～10）+合計：※画面にて実行
      sbSQL.append(" ,null as N13"); // F252：本売価計(1～10)+合計：※画面にて実行
      sbSQL.append(" ,null as N14"); // F263：荒利額（1～10）+合計：※画面にて実行
      sbSQL.append(" ,max(DATE_FORMAT(T6.ADDDT, '%Y%m%d%H%i%s%f')) as N50"); // データ確認用
    }
    sbSQL.append(" ,DT as N90"); // 参照用
    sbSQL.append(" ,IFNULL(DATE_FORMAT(M1.DT, '%y/%m/%d') || max(JWEEK), '') as N91"); // 参照用
    sbSQL.append(" ,max(ZNENW) as N92,max(ZNENM) as N93"); // 参照用

    // 参照用基本情報
    if (isNew || (isCopy && !isCopyNN)) {
      sbSQL.append(" ,null as TENHTSU_ARR"); // 店発注数配列
      sbSQL.append(" ,null as TENCHGFLG_ARR"); // 店変更フラグ配列
      sbSQL.append(" ,null as HTASU"); // 発注総数
      sbSQL.append(" ,null as PTNNO"); // パターン№
      sbSQL.append(" ,null as TSEIKBN"); // 訂正区分
      sbSQL.append(" ,null as TPSU"); // 店舗数
      sbSQL.append(" ,null as TENKAISU"); // 展開数
      sbSQL.append(" ,null as ZJSKFLG"); // 前年実績フラグ
      sbSQL.append(" ,null as WEEKHTDT"); // 週間発注処理日
    } else {
      sbSQL.append(" ,max(T6.TENHTSU_ARR) as TENHTSU_ARR"); // 店発注数配列
      if (isTOKTG) {
        sbSQL.append(" ,max(T6.TENCHGFLG_ARR) as TENCHGFLG_ARR"); // 店変更フラグ配列
      } else {
        sbSQL.append(" ,null as TENCHGFLG_ARR"); // 店変更フラグ配列
      }
      sbSQL.append(" ,sum(case when T1.TENKAIKBN='2' then null else T6.HTASU end) as HTASU"); // 発注総数
      sbSQL.append(" ,max(T6.PTNNO) as PTNNO"); // パターン№
      sbSQL.append(" ,max(T6.TSEIKBN) as TSEIKBN"); // 訂正区分
      sbSQL.append(" ,sum(T6.TPSU) as TPSU"); // 店舗数
      sbSQL.append(" ,sum(T6.TENKAISU) as TENKAISU"); // 展開数
      sbSQL.append(" ,max(T6.ZJSKFLG) as ZJSKFLG"); // 前年実績フラグ
      sbSQL.append(" ,max(T6.WEEKHTDT) as WEEKHTDT"); // 週間発注処理日
    }
    sbSQL.append(" from CAL M1");
    sbSQL.append(" inner join WEEK M2 on CWEEK = DAYOFWEEK(DATE_FORMAT(M1.DT, '%Y%m%d'))");
    sbSQL.append(" inner join WK T1 on 1=1");
    sbSQL.append(" left join " + szTableNNDT
        + " T6 on T1.MOYSKBN = T6.MOYSKBN and T1.MOYSSTDT = T6.MOYSSTDT and T1.MOYSRBAN = T6.MOYSRBAN and T1.BMNCD = T6.BMNCD and T1.KANRINO = T6.KANRINO and T1.KANRIENO = T6.KANRIENO and M1.DT = T6.NNDT");

    sbSQL.append(" group by M1.DT WITH ROLLUP ");
    sbSQL.append(" order by IFNULL(M1.DT, 99999999)");

    ItemList iL = new ItemList();
    @SuppressWarnings("static-access")
    JSONArray array = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    return array;
  }

  /**
   * 販売日情報取得処理
   *
   * @throws Exception
   */
  public JSONArray getHBDTData(HashMap<String, String> map) {
    String szMoyskbn = map.get("MOYSKBN"); // 催し区分
    String szMoysstdt = map.get("MOYSSTDT"); // 催しコード（催し開始日）
    String szMoysrban = map.get("MOYSRBAN"); // 催し連番
    String szBmncd = map.get("BMNCD"); // 部門コード
    String szKanrino = map.get("KANRINO"); // 管理No.
    String szKanrieno = map.get("KANRIENO"); // 管理No.枝番 ※月間チラシ・特売スポット遷移時キー
    String szMoyskbnC = map.get("MOYSKBN_C"); // 催し区分 ※コピー画面の場合
    String szMoysstdtC = map.get("MOYSSTDT_C");// 催しコード（催し開始日） ※コピー画面の場合
    String szMoysrbanC = map.get("MOYSRBAN_C");// 催し連番 ※コピー画面の場合
    String szBmncdC = map.get("BMNCD_C"); // 部門コード ※コピー画面の場合
    String szKanrinoC = map.get("KANRINO_C"); // 管理No. ※コピー画面の場合
    String szKanrienoC = map.get("KANRIENO_C");// 管理No.枝番 ※コピー画面の場合
    String sendBtnid = map.get("SENDBTNID"); // 呼出しボタン
    String sendPageid = map.get("PAGEID"); // 呼出しPAGEID

    // 全店特売アンケート有/無
    boolean isTOKTG = super.isTOKTG(szMoyskbn, szMoysrban);
    // 画面モード情報設定
    this.setModeInfo(sendPageid, sendBtnid);

    // 2.2．ST019の【特売・スポット計画 コピー元商品選択】画面の「選択（確定）」よりの場合：
    // 2.2.1．コピー元商品の情報をコピーし、新規画面に表示する
    // 2.2.1.2.2．催しコードが異なる場合：納入情報タブ、販売期間、納入期間をコピーしない。
    boolean isCopyNN = isCopy && (StringUtils.equals(szMoyskbn, szMoyskbnC) && StringUtils.equals(szMoysstdt, szMoysstdtC) && StringUtils.equals(szMoysrban, szMoysrbanC));

    String szTableSHN = "INATK.TOKTG_SHN"; // 全店特売(アンケート有/無)_商品
    if (!isTOKTG) {
      szTableSHN = "INATK.TOKSP_SHN";
    }

    ArrayList<String> paramData = new ArrayList<String>();
    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append(" SELECT ");
    sbSQL.append(" T1.MOYSKBN ");
    sbSQL.append(" ,T1.MOYSSTDT ");
    sbSQL.append(" ,T1.MOYSRBAN ");
    sbSQL.append(" ,T1.BMNCD ");
    sbSQL.append(" ,T1.KANRINO ");
    sbSQL.append(" ,T2.TENATSUK_ARR ");
    sbSQL.append(" FROM ");
    sbSQL.append(szTableSHN + " T1 LEFT JOIN INATK.TOKSP_HB T2 ON ");
    sbSQL.append(" T1.MOYSKBN=T2.MOYSKBN AND ");
    sbSQL.append(" T1.MOYSSTDT=T2.MOYSSTDT AND ");
    sbSQL.append(" T1.MOYSRBAN=T2.MOYSRBAN AND ");
    sbSQL.append(" T1.BMNCD=T2.BMNCD AND ");
    sbSQL.append(" T1.KANRINO=T2.KANRINO AND ");
    sbSQL.append(" T1.KANRIENO=T2.KANRIENO ");
    sbSQL.append(" WHERE ");
    if (isCopyNN) {
      sbSQL.append(" T1.MOYSKBN  = " + szMoyskbnC + "");
      sbSQL.append(" and T1.MOYSSTDT = " + szMoysstdtC + "");
      sbSQL.append(" and T1.MOYSRBAN = " + szMoysrbanC + "");
      sbSQL.append(" and T1.BMNCD    = " + szBmncdC + "");
      sbSQL.append(" and T1.KANRINO  = " + szKanrinoC + "");
      sbSQL.append(" and T1.KANRIENO = " + szKanrienoC + "");
    } else {
      sbSQL.append(" T1.MOYSKBN  = " + szMoyskbn + "");
      sbSQL.append(" and T1.MOYSSTDT = " + szMoysstdt + "");
      sbSQL.append(" and T1.MOYSRBAN = " + szMoysrban + "");
      sbSQL.append(" and T1.BMNCD    = " + szBmncd + "");
      sbSQL.append(" and T1.KANRINO  = " + szKanrino + "");
      if (StringUtils.isNotEmpty(szKanrieno)) {
        sbSQL.append("  and T1.KANRIENO = " + szKanrieno + "");
      }
    }
    sbSQL.append(" and IFNULL(T1.UPDKBN, 0) <> 1 ");

    ItemList iL = new ItemList();
    @SuppressWarnings("static-access")
    JSONArray array = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    return array;
  }

  /**
   * 商品情報取得処理
   *
   * @throws Exception
   */
  public String createSqlSelMSTSHN(String szMoysKbn, String szMoysstdt, String shncd) {
    StringBuffer sbSQL = new StringBuffer();

    if (StringUtils.isEmpty(szMoysstdt)) {
      sbSQL.append(" select ");
      sbSQL.append("  '' as F11"); // F11：商品マスタ名称
      /** PLU商品・定貫商品 */
      // レギュラー
      sbSQL.append(" ,'' as F58"); // F58：原価
      sbSQL.append(" ,'' as F59"); // F59：A総売価
      sbSQL.append(" ,'' as F61"); // F61：入数
      // 特売事前
      sbSQL.append(" ,'' as F63"); // F63：原価
      sbSQL.append(" ,'' as F64"); // F64：A総売価
      sbSQL.append(" ,'' as F65"); // F64：本売価
      sbSQL.append(" ,'' as F66"); // F66：入数
      // 特売追加
      sbSQL.append(" ,'' as F68"); // F68：原価
      // 【販売情報部分】
      sbSQL.append(" ,'' as F104"); // F104：メーカー名
      sbSQL.append(" ,'' as F105"); // F105：POP名称
      sbSQL.append(" ,'' as F106"); // F106：規格
      // 【納入情報部分】
      sbSQL.append(" ,'' as F147"); // F147：PC区分
      // 情報保持
      sbSQL.append(" ,'' as F172"); // F172：種別コード
      sbSQL.append(" ,'' as F173"); // F173：PC区分
      sbSQL.append(" ,'' as F174"); // F174：商品種類
      sbSQL.append(" ,'' as F160"); // F160：大分類
      sbSQL.append(" ,'' as F161"); // F161：中分類
      sbSQL.append(" ,'' as F175"); // F175：小分類
      sbSQL.append(" ,'' as F176"); // F176：定貫区分
      sbSQL.append(" from (SELECT 1 AS DUMMY) DUMMY ");
      return sbSQL.toString();
    }

    sbSQL = new StringBuffer();

    String szMoysstdt2 = CmnDate.dateFormat(CmnDate.convYYMMDD(szMoysstdt));
    sbSQL.append(" select ");
    sbSQL.append("  M0.SHNKN as F11"); // F11：商品マスタ名称
    sbSQL.append(" ,M0.RG_GENKAAM as F58"); // F58：原価
    sbSQL.append(" ," + DefineReport.ID_SQL_TOKBAIKA_COL_SOU.replaceAll("@DT", szMoysstdt2).replaceAll("@BAIKA", "M0.RG_BAIKAAM") + " as F59"); // F59：A総売価
    sbSQL.append(" ,M0.RG_IRISU as F61"); // F61：入数

    if (szMoysKbn.equals("1") || szMoysKbn.equals("3")) {
      sbSQL.append(" ,'' as F63"); // F63：原価
      sbSQL.append(" ,'' as F64"); // F64：A総売価
      sbSQL.append(" ,M0.IRISU as F66"); // F66：入数
      sbSQL.append(" ,'' as F68"); // F68：原価
    } else {
      sbSQL.append(" ,M0.GENKAAM as F63"); // F58：原価
      sbSQL.append(" ,CASE WHEN M0.BAIKAAM IS NULL THEN M0.BAIKAAM ELSE " + DefineReport.ID_SQL_TOKBAIKA_COL_SOU.replaceAll("@DT", szMoysstdt2).replaceAll("@BAIKA", "M0.BAIKAAM") + " END as F64"); // F59：A総売価
      sbSQL.append(" ,M0.IRISU as F66"); // F61：入数
      sbSQL.append(" ,M0.GENKAAM as F68"); // F68：原価
    }

    // 【販売情報部分】
    sbSQL.append(" ,SUBSTRING(M0.SANCHIKN,0,15) as F104"); // F104：メーカー名
    sbSQL.append(" ,M0.POPKN as F105"); // F105：POP名称
    sbSQL.append(" ,M0.KIKKN as F106"); // F106：規格
    // 【納入情報部分】
    sbSQL.append(" ,RTRIM(MEI.NMKN) as F147"); // F147：PC区分
    // 情報保持
    sbSQL.append(" ,M0.SHUBETUCD as F172"); // F172：種別コード
    sbSQL.append(" ,M0.PCKBN as F173"); // F173：PC区分
    sbSQL.append(" ,M0.SHNKBN as F174"); // F174：商品種類
    sbSQL.append(" ,M0.DAICD as F160"); // F160：大分類
    sbSQL.append(" ,M0.CHUCD as F161"); // F161：中分類
    sbSQL.append(" ,M0.SHOCD as F175"); // F175：小分類
    sbSQL.append(" ,M0.TEIKANKBN as F176"); // F176：定貫区分
    sbSQL.append(" from ");
    sbSQL.append(" (SELECT" + "  SHNCD " + " ,SHNKN " + " ,DAICD " + " ,CHUCD " + " ,SHOCD " + " ,CASE WHEN TEIKANKBN='0' THEN '2' WHEN TEIKANKBN='1' THEN TEIKANKBN ELSE '1' END AS TEIKANKBN "
        + " ,POPKN " + " ,KIKKN " + " ,PCKBN " + " ,SHNKBN " + " ,BMNCD " + " ,SHUBETUCD " + " ,SANCHIKN " + " ,ZEIKBN " + " ,ZEIRTHENKODT " + " ,ZEIRTKBN " + " ,ZEIRTKBN_OLD " + " ,UPDKBN "
        + " ,RG_GENKAAM  " + " ,RG_BAIKAAM  " + " ,RG_IRISU  ");
    if (szMoysKbn.equals("0")) {
      sbSQL.append(" ,RG_GENKAAM GENKAAM " + " ,RG_BAIKAAM BAIKAAM " + " ,RG_IRISU IRISU ");
    } else if (szMoysKbn.equals("1") || szMoysKbn.equals("3")) {
      sbSQL.append(" ,RG_GENKAAM GENKAAM " + " ,RG_BAIKAAM BAIKAAM " + " ,CASE WHEN (HS_GENKAAM = 0 AND HS_BAIKAAM = 0 AND HS_IRISU = 0) OR "
          + "  (HS_GENKAAM IS NULL AND HS_BAIKAAM IS NULL AND HS_IRISU IS NULL) THEN RG_IRISU ELSE HS_IRISU END IRISU ");
    } else {
      sbSQL.append(" ,CASE WHEN BMNCD IN ('2','4','5','6','9','15') THEN NULL ELSE " + " CASE WHEN (HS_GENKAAM = 0 AND HS_BAIKAAM = 0 AND HS_IRISU = 0) OR "
          + "  (HS_GENKAAM IS NULL AND HS_BAIKAAM IS NULL AND HS_IRISU IS NULL) THEN RG_GENKAAM ELSE HS_GENKAAM END END GENKAAM " + " ,CASE WHEN BMNCD IN ('2','4','5','6','9','15') THEN NULL ELSE "
          + " CASE WHEN (HS_GENKAAM = 0 AND HS_BAIKAAM = 0 AND HS_IRISU = 0) OR "
          + "  (HS_GENKAAM IS NULL AND HS_BAIKAAM IS NULL AND HS_IRISU IS NULL) THEN RG_BAIKAAM ELSE HS_BAIKAAM END END BAIKAAM "
          + " ,CASE WHEN (HS_GENKAAM = 0 AND HS_BAIKAAM = 0 AND HS_IRISU = 0) OR " + "  (HS_GENKAAM IS NULL AND HS_BAIKAAM IS NULL AND HS_IRISU IS NULL) THEN RG_IRISU ELSE HS_IRISU END IRISU ");
    }
    sbSQL.append(" FROM INAMS.MSTSHN) M0 ");
    sbSQL.append(
        " left outer join INAMS.MSTBMN M1 on M1.BMNCD = M0.BMNCD and IFNULL(M1.UPDKBN, 0) <> 1" + " left outer join INAMS.MSTZEIRT M2 on M2.ZEIRTKBN = M0.ZEIRTKBN and IFNULL(M2.UPDKBN, 0) <> 1"
            + " left outer join INAMS.MSTZEIRT M3 on M3.ZEIRTKBN = M0.ZEIRTKBN_OLD and IFNULL(M3.UPDKBN, 0) <> 1"
            + " left outer join INAMS.MSTZEIRT M4 on M4.ZEIRTKBN = M1.ZEIRTKBN and IFNULL(M4.UPDKBN, 0) <> 1"
            + " left outer join INAMS.MSTZEIRT M5 on M5.ZEIRTKBN = M1.ZEIRTKBN_OLD and IFNULL(M5.UPDKBN, 0) <> 1");
    sbSQL.append(" left join INAMS.MSTMEISHO MEI on MEI.MEISHOKBN = '" + DefineReport.MeisyoSelect.KBN102.getCd() + "' and MEI.MEISHOCD = M0.PCKBN ");
    sbSQL.append(" where M0.SHNCD = ? and IFNULL(M0.UPDKBN, 0) <> 1 ");
    return sbSQL.toString();
  }

  /**
   * 前複写（【画面】.「販売期間」、【画面】.「納入期間」部分）:前回商品情報取得
   *
   */
  public String createSqlSelTOK_SHN_BEF1(JSONObject map, User userInfo) {
    String szMoyskbn = map.optString("MOYSKBN"); // 催し区分
    String szMoysstdt = map.optString("MOYSSTDT"); // 催しコード（催し開始日）
    String szMoysrban = map.optString("MOYSRBAN"); // 催し連番
    String szBmncd = map.optString("BMNCD"); // 部門コード
    String szAddshukbn = map.optString("ADDSHUKBN"); // 登録種別

    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー

    // 全店特売アンケート有/無
    boolean isTOKTG = super.isTOKTG(szMoyskbn, szMoysrban);
    String szTableSHN = "INATK.TOKTG_SHN"; // 全店特売(アンケート有/無)_商品
    if (!isTOKTG) {
      szTableSHN = "INATK.TOKSP_SHN";
    }

    new ArrayList<String>();

    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append(" select ");
    sbSQL.append("  right(HBSTDT,6) as F16"); // F16：販売期間From
    sbSQL.append(" ,right(HBEDDT,6) as F17"); // F17：販売期間To
    sbSQL.append(" ,right(NNSTDT,6) as F18"); // F18：納入期間From
    sbSQL.append(" ,right(NNEDDT,6) as F19"); // F19：納入期間To
    sbSQL.append(" from " + szTableSHN);
    sbSQL.append(" where MOYSKBN  = " + szMoyskbn + "");
    sbSQL.append("   and MOYSSTDT = " + szMoysstdt + "");
    sbSQL.append("   and MOYSRBAN = " + szMoysrban + "");
    sbSQL.append("   and ADDSHUKBN= " + szAddshukbn + "");
    sbSQL.append("   and BMNCD    = " + szBmncd + "");
    sbSQL.append("   and OPERATOR = '" + userId + "'");
    sbSQL.append(" order by MOYSKBN, MOYSSTDT, MOYSRBAN, BMNCD, UPDDT desc");
    sbSQL.append(" LIMIT 1 ");

    return sbSQL.toString();
  }

  /**
   * 前複写（【画面】.「対象店」、【画面】.「除外店」部分）:前回商品情報取得
   *
   */
  public String createSqlSelTOK_SHN_BEF2(JSONObject map, User userInfo) {
    String szMoyskbn = map.optString("MOYSKBN"); // 催し区分
    String szMoysstdt = map.optString("MOYSSTDT"); // 催しコード（催し開始日）
    String szMoysrban = map.optString("MOYSRBAN"); // 催し連番
    String szBmncd = map.optString("BMNCD"); // 部門コード
    String szAddshukbn = map.optString("ADDSHUKBN"); // 登録種別

    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー

    // 全店特売アンケート有/無
    boolean isTOKTG = super.isTOKTG(szMoyskbn, szMoysrban);
    String szTableSHN = "INATK.TOKTG_SHN"; // 全店特売(アンケート有/無)_商品
    String szTableTJTEN = "INATK.TOKTG_TJTEN"; // 全店特売(アンケート有/無)_対象除外店
    if (!isTOKTG) {
      szTableSHN = "INATK.TOKSP_SHN";
      szTableTJTEN = "INATK.TOKSP_TJTEN";
    }
    // コピー対象
    // 全店特売（アンケート有）_商品.対象店ランク OR 全店特売（アンケート無）_商品.対象店ランク_A売価、
    // 全店特売（アンケート無）_商品.除外店ランク
    // 全店特売（アンケート有/無）_商品.定貫PLU・不定貫区分
    // 全店特売（アンケート有/無）_商品.原価_特売事前 GENKAAM_MAE
    // 全店特売（アンケート有/無）_商品.A売価(100ｇ売価) A_BAIKAAM
    // 全店特売（アンケート有/無）_商品.入数（特売事前入数、不定貫入数） IRISU
    // 全店特売（アンケート有/無）_商品.原価_特売追加 GENKAAM_ATO
    // 全店特売（アンケート有/無）_商品.B売価(100ｇ売価) B_BAIKAAM
    // 全店特売（アンケート有/無）_商品.C売価(100ｇ売価) C_BAIKAAM
    // 全店特売（アンケート無）_商品.対象店ランク_B売価 RANKNO_ADD_B
    // 全店特売（アンケート無）_商品.対象店ランク_C売価 RANKNO_ADD_C
    // 全店特売（アンケート有/無）_商品.A売価_100g相当 A_BAIKAAM_100G
    // 全店特売（アンケート有/無）_商品.B売価_100g相当 B_BAIKAAM_100G
    // 全店特売（アンケート有/無）_商品.C売価_100g相当 C_BAIKAAM_100G
    // 全店特売（アンケート有/無）_商品.原価_1Kg GENKAAM_1KG
    // 全店特売（アンケート有/無）_商品.A売価_1Kg A_GENKAAM_1KG
    // 全店特売（アンケート有/無）_商品.B売価_1Kg B_GENKAAM_1KG
    // 全店特売（アンケート有/無）_商品.C売価_1Kg C_GENKAAM_1KG
    // 全店特売（アンケート有/無）_商品.パック原価 GENKAAM_PACK
    // 全店特売（アンケート有/無）_商品.A売価_パック A_BAIKAAM_PACK
    // 全店特売（アンケート有/無）_商品.B売価_パック B_BAIKAAM_PACK
    // 全店特売（アンケート有/無）_商品.C売価_パック C_BAIKAAM_PACK

    new ArrayList<String>();

    StringBuffer sbSQL = new StringBuffer();

    sbSQL.append("with SHN as (");
    sbSQL.append(" select MOYSKBN,MOYSSTDT,MOYSRBAN,BMNCD,KANRINO,KANRIENO");
    if (isTOKTG) {
      sbSQL.append(" ,T3.RANKNO_ADD as F21"); // F21：対象店
      sbSQL.append(" ,null as F22"); // F22：除外店
    } else {
      sbSQL.append(" ,T3.RANKNO_ADD_A as F21"); // F21：対象店
      sbSQL.append(" ,T3.RANKNO_DEL as F22"); // F22：除外店
    }
    sbSQL.append(" ,T3.TKANPLUKBN as F57"); // F57：PLU商品・定貫商品／不定貫商品
    // 特売事前行
    sbSQL.append(" ,T3.GENKAAM_MAE as F63"); // F63：原価
    sbSQL.append(" ,T3.A_BAIKAAM as F64"); // F64：A総売価
    sbSQL.append(" ,T3.IRISU as F66"); // F66：入数
    // 特売追加行
    sbSQL.append(" ,T3.GENKAAM_ATO as F68"); // F68：原価
    // B部分
    sbSQL.append(" ,T3.B_BAIKAAM as F73"); // F73：B総売価
    if (isTOKTG) {
      sbSQL.append(" ,null as F76"); // F76：B売店 編集不可(列無し)
    } else {
      sbSQL.append(" ,T3.RANKNO_ADD_B as F76"); // F76：B売店
    }
    // C部分
    sbSQL.append(" ,T3.C_BAIKAAM as F77"); // F77：C総売価
    if (isTOKTG) {
      sbSQL.append(" ,null as F80"); // F80：C売店 編集不可(列無し)
    } else {
      sbSQL.append(" ,T3.RANKNO_ADD_C as F80"); // F80：C売店
    }
    // A総売価行
    sbSQL.append(" ,T3.A_BAIKAAM as F81"); // F81：100g総売価
    sbSQL.append(" ,T3.GENKAAM_1KG as F82"); // F82：1Kg原価
    sbSQL.append(" ,T3.A_GENKAAM_1KG as F83"); // F83：1Kg総売価
    sbSQL.append(" ,T3.GENKAAM_PACK as F84"); // F84：P原価
    sbSQL.append(" ,T3.A_BAIKAAM_PACK as F85"); // F85：P総売価
    sbSQL.append(" ,T3.IRISU as F86"); // F86：入数
    // B総売価行
    sbSQL.append(" ,T3.B_BAIKAAM as F87"); // F87：100g総売価
    sbSQL.append(" ,T3.B_GENKAAM_1KG as F89"); // F89：1Kg総売価
    sbSQL.append(" ,T3.B_BAIKAAM_PACK as F91"); // F91：P総売価
    // C総売価行
    sbSQL.append(" ,T3.C_BAIKAAM as F93"); // F93：100g総売価
    sbSQL.append(" ,T3.C_GENKAAM_1KG as F95"); // F95：1Kg総売価
    sbSQL.append(" ,T3.C_BAIKAAM_PACK as F97"); // F97：P総売価
    // 100g相当部分
    sbSQL.append(" ,T3.A_BAIKAAM_100G as F124"); // F124：A総売価
    sbSQL.append(" ,T3.B_BAIKAAM_100G as F125"); // F125：B総売価
    sbSQL.append(" ,T3.C_BAIKAAM_100G as F126"); // F126：C総売価
    sbSQL.append(" from " + szTableSHN + " T3");
    sbSQL.append(" where MOYSKBN  = " + szMoyskbn + "");
    sbSQL.append("   and MOYSSTDT = " + szMoysstdt + "");
    sbSQL.append("   and MOYSRBAN = " + szMoysrban + "");
    sbSQL.append("   and ADDSHUKBN= " + szAddshukbn + "");
    sbSQL.append("   and BMNCD    = " + szBmncd + "");
    sbSQL.append("   and OPERATOR = '" + userId + "'");
    sbSQL.append(" order by MOYSKBN, MOYSSTDT, MOYSRBAN, BMNCD, UPDDT desc");
    sbSQL.append(" LIMIT 1 ");
    sbSQL.append(")");
    /*** 対象除外店 ***/
    sbSQL.append(", JTEN as (");
    sbSQL.append(" select T1.MOYSKBN, T1.MOYSSTDT, T1.MOYSRBAN, T1.BMNCD,T1.KANRINO, T1.KANRIENO, T1.TJFLG");
    for (int i = 1; i <= 10; i++) {
      sbSQL.append(" ,max(case T1.RNO when " + i + " then T1.TENCD end) as TENCD_" + i);
      sbSQL.append(" ,max(case T1.RNO when " + i + " then T1.TENRANK end) as TENRANK_" + i);
    }
    sbSQL.append(" from (");
    sbSQL.append("  select T1.MOYSKBN, T1.MOYSSTDT, T1.MOYSRBAN, T1.BMNCD,T1.KANRINO, T1.KANRIENO, T1.TENCD,T1.TJFLG,T1.TENRANK");
    sbSQL.append("  ,row_number() over(partition by T1.MOYSKBN, T1.MOYSSTDT, T1.MOYSRBAN, T1.BMNCD,T1.KANRINO, T1.KANRIENO,T1.TJFLG order by T1.TENCD) as RNO");
    sbSQL.append("  from " + szTableTJTEN + " T1");
    sbSQL.append("  inner join SHN T2");
    sbSQL.append("  on T1.MOYSKBN=T2.MOYSKBN and T1.MOYSSTDT=T2.MOYSSTDT and T1.MOYSRBAN=T2.MOYSRBAN and T1.BMNCD=T2.BMNCD and T1.KANRINO=T2.KANRINO and T1.KANRIENO=T2.KANRIENO");
    sbSQL.append(" ) T1");
    sbSQL.append(" group by T1.MOYSKBN, T1.MOYSSTDT, T1.MOYSRBAN, T1.BMNCD,T1.KANRINO, T1.KANRIENO, T1.TJFLG");
    sbSQL.append(")");

    sbSQL.append(" select T3.*");
    /** 追加・ランク・除外 */
    for (int i = 1; i <= 10; i++) {
      sbSQL.append(" ,T4.TENCD_" + i + " as F" + (22 + i)); // F23：追加（1～10）
    }
    for (int i = 1; i <= 10; i++) {
      sbSQL.append(" ,T4.TENRANK_" + i + " as F" + (32 + i)); // F33：ランク（1～10）
    }
    for (int i = 1; i <= 10; i++) {
      sbSQL.append(" ,T5.TENCD_" + i + " as F" + (42 + i)); // F43：除外（1～10）
    }
    sbSQL.append(" from SHN T3");
    sbSQL.append(
        " left join JTEN T4 on T3.MOYSKBN = T4.MOYSKBN and T3.MOYSSTDT = T4.MOYSSTDT and T3.MOYSRBAN = T4.MOYSRBAN and T3.BMNCD = T4.BMNCD and T3.KANRINO = T4.KANRINO and T3.KANRIENO = T4.KANRIENO and T4.TJFLG = 1");
    sbSQL.append(
        " left join JTEN T5 on T3.MOYSKBN = T5.MOYSKBN and T3.MOYSSTDT = T5.MOYSSTDT and T3.MOYSRBAN = T5.MOYSRBAN and T3.BMNCD = T5.BMNCD and T3.KANRINO = T5.KANRINO and T3.KANRIENO = T5.KANRIENO and T5.TJFLG = 2");
    return sbSQL.toString();
  }

  /**
   * 前複写（【画面】.「納入情報」タブ部分）:前回商品情報取得
   *
   */
  public String createSqlSelTOK_SHN_BEF3(JSONObject map, User userInfo) {
    String szMoyskbn = map.optString("MOYSKBN"); // 催し区分
    String szMoysstdt = map.optString("MOYSSTDT"); // 催しコード（催し開始日）
    String szMoysrban = map.optString("MOYSRBAN"); // 催し連番
    String szBmncd = map.optString("BMNCD"); // 部門コード
    String szAddshukbn = map.optString("ADDSHUKBN"); // 登録種別

    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー

    // 全店特売アンケート有/無
    boolean isTOKTG = super.isTOKTG(szMoyskbn, szMoysrban);
    String szTableSHN = "INATK.TOKTG_SHN"; // 全店特売(アンケート有/無)_商品
    if (!isTOKTG) {
      szTableSHN = "INATK.TOKSP_SHN";
    }
    // コピー対象
    // 全店特売（アンケート有/無）_商品.事前打出フラグ
    // 全店特売（アンケート有/無）_商品.事前打出日付
    // 全店特売（アンケート有/無）_商品.特売コメント
    // 全店特売（アンケート有/無）_商品.カット店展開フラグ
    // 全店特売（アンケート有/無）_商品.便区分
    // 全店特売（アンケート有/無）_商品.別伝区分
    // 全店特売（アンケート有/無）_商品.ワッペン区分
    // 全店特売（アンケート有/無）_商品.週次仕入先転送フラグ
    // 全店特売（アンケート有/無）_商品.展開方法
    // 全店特売（アンケート有/無）_商品.実績率パターン数値
    // 全店特売（アンケート有/無）_商品.実績率パターン前年同月
    // 全店特売（アンケート有/無）_商品.実績率パターン前年同週


    new ArrayList<String>();

    StringBuffer sbSQL = new StringBuffer();

    sbSQL.append(" select MOYSKBN,MOYSSTDT,MOYSRBAN,BMNCD,KANRINO,KANRIENO");
    // 【納入情報部分】TG016_1,TG016_2,TG016_3
    sbSQL.append(" ,T3.JUFLG as F139"); // F139：事前打出(チェック)
    sbSQL.append(" ,T3.JUHTDT as F140"); // F140：事前打出(日付)
    sbSQL.append(" ,T3.COMMENT_TB as F141"); // F141：特売コメント
    sbSQL.append(" ,T3.CUTTENFLG as F142"); // F142：カット店展開しない
    sbSQL.append(" ,T3.BINKBN as F143"); // F143：便区分
    sbSQL.append(" ,T3.BDENKBN as F144"); // F144：別伝区分
    sbSQL.append(" ,T3.WAPPNKBN as F145"); // F145：ワッペン区分
    sbSQL.append(" ,T3.SHUDENFLG as F146"); // F146：週次伝送flg
    sbSQL.append(" ,T3.TENKAIKBN as F154"); // F154: 展開方法
    sbSQL.append(" ,T3.JSKPTNSYUKBN as F157"); // F157：実績率パタン数値
    sbSQL.append(" ,T3.JSKPTNZNENMKBN as F158"); // F158：実績率パタン前年同月
    sbSQL.append(" ,T3.JSKPTNZNENWKBN as F159"); // F159：実績率パタン前年同週
    sbSQL.append(" from " + szTableSHN + " T3");
    sbSQL.append(" where MOYSKBN  = " + szMoyskbn + "");
    sbSQL.append("   and MOYSSTDT = " + szMoysstdt + "");
    sbSQL.append("   and MOYSRBAN = " + szMoysrban + "");
    sbSQL.append("   and ADDSHUKBN= " + szAddshukbn + "");
    sbSQL.append("   and BMNCD    = " + szBmncd + "");
    sbSQL.append("   and OPERATOR = '" + userId + "'");
    sbSQL.append(" order by MOYSKBN, MOYSSTDT, MOYSRBAN, BMNCD, UPDDT desc");
    sbSQL.append(" LIMIT 1 ");
    return sbSQL.toString();
  }

  /**
   * 前複写（【画面】.「納入情報」タブ部分）:前回納入日情報取得
   *
   */
  public String createSqlSelTOK_NNDT_BEF3(JSONObject map, User userInfo) {
    String szMoyskbn = map.optString("MOYSKBN"); // 催し区分
    String szMoysstdt = map.optString("MOYSSTDT"); // 催しコード（催し開始日）
    String szMoysrban = map.optString("MOYSRBAN"); // 催し連番
    String szBmncd = map.optString("BMNCD"); // 部門コード
    String szAddshukbn = map.optString("ADDSHUKBN"); // 登録種別

    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー

    // 全店特売アンケート有/無
    boolean isTOKTG = super.isTOKTG(szMoyskbn, szMoysrban);
    String szTableSHN = "INATK.TOKTG_SHN"; // 全店特売(アンケート有/無)_商品
    String szTableNNDT = "INATK.TOKTG_NNDT"; // 全店特売(アンケート有/無)_納入日
    if (!isTOKTG) {
      szTableSHN = "INATK.TOKSP_SHN";
      szTableNNDT = "INATK.TOKSP_NNDT";
    }
    // コピー対象
    // 全店特売(アンケート有/無)_納入日.納入日
    // 全店特売(アンケート有/無)_納入日.発注総数
    // 全店特売(アンケート有/無)_納入日.パターンNo.
    // 全店特売(アンケート有/無)_納入日.訂正区分
    // 全店特売(アンケート有/無)_納入日.前年実績フラグ


    new ArrayList<String>();

    // 詳細部分
    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append(DefineReport.ID_SQL_CMN_WEEK);
    sbSQL.append(",WK as (");
    sbSQL.append(" select * ");
    sbSQL.append(" ,case T1.JSKPTNSYUKBN " + " when " + DefineReport.ValKbn10008.VAL1.getVal() + " then '売'" + " when " + DefineReport.ValKbn10008.VAL2.getVal() + " then '点'"
        + " else '' end as JSKPTNSYUKBN_NM"); // 実績率パタン数値txt
    sbSQL.append(" ,case T1.JSKPTNZNENMKBN " + " when " + DefineReport.ValKbn10009.VAL1.getVal() + " then '" + DefineReport.ValKbn10009.VAL1.getTxt() + "'" + " when "
        + DefineReport.ValKbn10009.VAL2.getVal() + " then '" + DefineReport.ValKbn10009.VAL2.getTxt() + "'" + " when " + DefineReport.ValKbn10009.VAL3.getVal() + " then '"
        + DefineReport.ValKbn10009.VAL3.getTxt() + "'" + " else '' end as JSKPTNZNENMKBN_NM"); // 実績率パタン前年同月txt
    sbSQL.append(" ,case T1.JSKPTNZNENWKBN " + " when " + DefineReport.ValKbn10009.VAL1.getVal() + " then '" + DefineReport.ValKbn10009.VAL1.getTxt() + "'" + " when "
        + DefineReport.ValKbn10009.VAL2.getVal() + " then '" + DefineReport.ValKbn10009.VAL2.getTxt() + "'" + " when " + DefineReport.ValKbn10009.VAL3.getVal() + " then '"
        + DefineReport.ValKbn10009.VAL3.getTxt() + "'" + " else '' end as JSKPTNZNENWKBN_NM"); // 実績率パタン前年同週txt
    sbSQL.append(" ,S1.SHUNO||'週' as ZNENW"); // 実績率パタン前年週 TODO
    sbSQL.append(" ,DATE_FORMAT(T1.ZNENDT, '%y%m')||'月' as ZNENM"); // 実績率パタン前年月 TODO
    sbSQL.append(" from (");
    sbSQL.append("  select ");
    sbSQL.append("  T1.MOYSKBN, T1.MOYSSTDT, T1.MOYSRBAN, T3.BMNCD, T3.KANRINO, T3.KANRIENO");
    // 納入情報比較用：5.3．販売期間範囲、納入期間範囲の定義
    sbSQL.append("  ,T3.HBSTDT");
    sbSQL.append("  ,T3.HBEDDT");
    sbSQL.append("  ,T3.NNSTDT");
    sbSQL.append("  ,T3.NNEDDT");
    sbSQL.append("  ,case when T3.NNEDDT > T3.HBEDDT then T3.NNEDDT else T3.HBEDDT end as COMPDT");
    sbSQL.append("  ,T3.TENKAIKBN"); // 展開方法
    sbSQL.append("  ,T3.JSKPTNSYUKBN"); // 実績率パタン数値
    sbSQL.append("  ,T3.JSKPTNZNENMKBN"); // 実績率パタン前年同月
    sbSQL.append("  ,T3.JSKPTNZNENWKBN"); // 実績率パタン前年同週
    sbSQL.append("  ,T1.UPDDT - INTERVAL 1 year as ZNENDT"); // 実績率パタン前年日
    sbSQL.append("  from INATK.TOKMOYCD T1 ");
    sbSQL.append("  inner join " + szTableSHN + " T3 on IFNULL(T1.UPDKBN, 0) <> 1 and IFNULL(T3.UPDKBN, 0) <> 1 ");
    sbSQL.append("    and T1.MOYSKBN  = T3.MOYSKBN and T1.MOYSSTDT = T3.MOYSSTDT and T1.MOYSRBAN = T3.MOYSRBAN");
    sbSQL.append("    and T3.MOYSKBN  = " + szMoyskbn + "");
    sbSQL.append("    and T3.MOYSSTDT = " + szMoysstdt + "");
    sbSQL.append("    and T3.MOYSRBAN = " + szMoysrban + "");
    sbSQL.append("    and T3.ADDSHUKBN= " + szAddshukbn + "");
    sbSQL.append("    and T3.BMNCD    = " + szBmncd + "");
    sbSQL.append("    and T3.OPERATOR = '" + userId + "'");
    sbSQL.append(" ) T1");
    sbSQL.append(" left join INAAD.SYSSHUNO S1 on DATE_FORMAT(T1.ZNENDT, '%Y%m%d') between S1.STARTDT and S1.ENDDT");
    sbSQL.append(")");
    sbSQL.append(",CAL(IDX, DT) as (");
    sbSQL.append(" select 1 as IDX, NNSTDT as DT from WK");
    sbSQL.append(" union all ");
    sbSQL.append(" select IDX+1 as IDX, DATE_FORMAT(DT + INTERVAL 1 day,'%Y%m%d') as DT from CAL where IDX < 10");
    sbSQL.append(")");
    sbSQL.append(" select ");
    sbSQL.append("  IFNULL(DATE_FORMAT(M1.DT, '%m/%d'), '') as N1"); // F148：日付（1～10）
    sbSQL.append(" ,max(JWEEK2) as N2"); // F158：曜日（1～10）
    // F168：販売日（1～10）
    sbSQL.append(" ,max(case when int(M1.DT) between T1.NNSTDT and T1.NNEDDT then " + DefineReport.Values.ON.getVal() + " else " + DefineReport.Values.OFF.getVal() + " end) as N4"); // F178：納入日（1～10）
    sbSQL.append(" ,sum(T6.HTASU) as N5"); // F188：発注総数（1～10）+合計：
    sbSQL.append(" ,max(T6.PTNNO) as N7"); // F199：パターンNo.（1～10）
    sbSQL.append(" ,max(T6.TSEIKBN) as N8"); // F209：訂正区分（1～10）
    sbSQL.append(" ,DT as N90"); // 参照用
    sbSQL.append(" ,IFNULL(DATE_FOTMAT(M1.DT, '%y/%m/%d') || max(JWEEK), '') as N91"); // 参照用
    sbSQL.append(" ,max(ZNENW) as N92,max(ZNENM) as N93"); // 参照用
    // 参照用基本情報
    sbSQL.append(" ,max(T6.TENHTSU_ARR) as TENHTSU_ARR"); // 店発注数配列
    if (isTOKTG) {
      sbSQL.append(" ,max(T6.TENCHGFLG_ARR) as TENCHGFLG_ARR"); // 店変更フラグ配列
    } else {
      sbSQL.append(" ,null as TENCHGFLG_ARR"); // 店変更フラグ配列
    }
    sbSQL.append(" ,sum(T6.HTASU) as HTASU"); // 発注総数
    sbSQL.append(" ,max(T6.PTNNO) as PTNNO"); // パターン№
    sbSQL.append(" ,max(T6.TSEIKBN) as TSEIKBN"); // 訂正区分
    sbSQL.append(" ,max(T6.ZJSKFLG) as ZJSKFLG"); // 前年実績フラグ
    sbSQL.append(" from CAL M1");
    sbSQL.append(" inner join WEEK M2 on CWEEK = DAYOFWEEK(DATE_FORMAT(M1.DT, '%Y%m%d'))");
    sbSQL.append(" inner join WK T1 on 1=1");
    sbSQL.append(" left join " + szTableNNDT
        + " T6 on T1.MOYSKBN = T6.MOYSKBN and T1.MOYSSTDT = T6.MOYSSTDT and T1.MOYSRBAN = T6.MOYSRBAN and T1.BMNCD = T6.BMNCD and T1.KANRINO = T6.KANRINO and T1.KANRIENO = T6.KANRIENO and M1.DT = T6.NNDT");

    sbSQL.append(" group by grouping sets ((),(M1.DT))");
    sbSQL.append(" order by IFNULL(M1.DT, 99999999)");
    return sbSQL.toString();
  }

  /**
   * 初期表示時、前複写
   *
   */
  public String createSqlSelTOK_SHN_BEF4(JSONObject map, User userInfo) {
    String szMoyskbn = map.optString("MOYSKBN"); // 催し区分
    String szMoysstdt = map.optString("MOYSSTDT"); // 催しコード（催し開始日）
    String szMoysrban = map.optString("MOYSRBAN"); // 催し連番
    String szBmncd = map.optString("BMNCD"); // 部門コード
    String szAddshukbn = map.optString("ADDSHUKBN"); // 登録種別

    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー

    // 全店特売アンケート有/無
    boolean isTOKTG = super.isTOKTG(szMoyskbn, szMoysrban);
    String szTableSHN = "INATK.TOKTG_SHN"; // 全店特売(アンケート有/無)_商品
    String szTableTJTEN = "INATK.TOKTG_TJTEN"; // 全店特売(アンケート有/無)_対象除外店
    if (!isTOKTG) {
      szTableSHN = "INATK.TOKSP_SHN";
      szTableTJTEN = "INATK.TOKSP_TJTEN";
    }

    new ArrayList<String>();

    StringBuffer sbSQL = new StringBuffer();

    sbSQL.append("with SHN as (");
    sbSQL.append(" select MOYSKBN,MOYSSTDT,MOYSRBAN,BMNCD,KANRINO,KANRIENO");
    sbSQL.append(" ,CASE WHEN BYCD='0' THEN right('00'||'" + szBmncd + "', 2) || '0000000' ELSE right('0000000'||BYCD,7) END as F9"); // F9：BY
    sbSQL.append(" ,right(HBSTDT,6) as F16"); // F16：販売期間From
    sbSQL.append(" ,right(HBEDDT,6) as F17"); // F17：販売期間To
    sbSQL.append(" ,right(NNSTDT,6) as F18"); // F18：納入期間From
    sbSQL.append(" ,right(NNEDDT,6) as F19"); // F19：納入期間To
    sbSQL.append(" ,T3.COMMENT_TB as F141"); // F141：特売コメント
    sbSQL.append(" ,T3.PLUSNDFLG AS F111"); // F111：PLU配信しない
    sbSQL.append(" ,T3.COMMENT_HGW AS  F137"); // F137：その他日替コメント
    sbSQL.append(" ,T3.COMMENT_POP AS F138"); // F138：POPコメント
    if (isTOKTG) {
      sbSQL.append(" ,T3.RANKNO_ADD as F21"); // F21：対象店
      sbSQL.append(" ,null as F22"); // F22：除外店
    } else {
      sbSQL.append(" ,T3.RANKNO_ADD_A as F21"); // F21：対象店
      sbSQL.append(" ,T3.RANKNO_DEL as F22"); // F22：除外店
    }
    sbSQL.append(" from " + szTableSHN + " T3");
    sbSQL.append(" where MOYSKBN  = " + szMoyskbn + "");
    sbSQL.append("   and MOYSSTDT = " + szMoysstdt + "");
    sbSQL.append("   and MOYSRBAN = " + szMoysrban + "");
    sbSQL.append("   and ADDSHUKBN= " + szAddshukbn + "");
    sbSQL.append("   and BMNCD    = " + szBmncd + "");
    sbSQL.append("   and OPERATOR = '" + userId + "'");
    sbSQL.append(" order by MOYSKBN, MOYSSTDT, MOYSRBAN, BMNCD, UPDDT desc");
    sbSQL.append(" LIMIT 1 ");
    sbSQL.append(")");
    /*** 対象除外店 ***/
    sbSQL.append(", JTEN as (");
    sbSQL.append(" select T1.MOYSKBN, T1.MOYSSTDT, T1.MOYSRBAN, T1.BMNCD,T1.KANRINO, T1.KANRIENO, T1.TJFLG");
    for (int i = 1; i <= 10; i++) {
      sbSQL.append(" ,max(case T1.RNO when " + i + " then T1.TENCD end) as TENCD_" + i);
      sbSQL.append(" ,max(case T1.RNO when " + i + " then T1.TENRANK end) as TENRANK_" + i);
    }
    sbSQL.append(" from (");
    sbSQL.append("  select T1.MOYSKBN, T1.MOYSSTDT, T1.MOYSRBAN, T1.BMNCD,T1.KANRINO, T1.KANRIENO, T1.TENCD,T1.TJFLG,T1.TENRANK");
    sbSQL.append("  ,row_number() over(partition by T1.MOYSKBN, T1.MOYSSTDT, T1.MOYSRBAN, T1.BMNCD,T1.KANRINO, T1.KANRIENO,T1.TJFLG order by T1.TENCD) as RNO");
    sbSQL.append("  from " + szTableTJTEN + " T1");
    sbSQL.append("  inner join SHN T2");
    sbSQL.append("  on T1.MOYSKBN=T2.MOYSKBN and T1.MOYSSTDT=T2.MOYSSTDT and T1.MOYSRBAN=T2.MOYSRBAN and T1.BMNCD=T2.BMNCD and T1.KANRINO=T2.KANRINO and T1.KANRIENO=T2.KANRIENO");
    sbSQL.append(" ) T1");
    sbSQL.append(" group by T1.MOYSKBN, T1.MOYSSTDT, T1.MOYSRBAN, T1.BMNCD,T1.KANRINO, T1.KANRIENO, T1.TJFLG");
    sbSQL.append(")");

    sbSQL.append(" select T3.*");
    /** 追加・ランク・除外 */
    for (int i = 1; i <= 10; i++) {
      sbSQL.append(" ,T4.TENCD_" + i + " as F" + (22 + i)); // F23：追加（1～10）
    }
    for (int i = 1; i <= 10; i++) {
      sbSQL.append(" ,T4.TENRANK_" + i + " as F" + (32 + i)); // F33：ランク（1～10）
    }
    for (int i = 1; i <= 10; i++) {
      sbSQL.append(" ,T5.TENCD_" + i + " as F" + (42 + i)); // F43：除外（1～10）
    }
    sbSQL.append(" from SHN T3");
    sbSQL.append(
        " left join JTEN T4 on T3.MOYSKBN = T4.MOYSKBN and T3.MOYSSTDT = T4.MOYSSTDT and T3.MOYSRBAN = T4.MOYSRBAN and T3.BMNCD = T4.BMNCD and T3.KANRINO = T4.KANRINO and T3.KANRIENO = T4.KANRIENO and T4.TJFLG = 1");
    sbSQL.append(
        " left join JTEN T5 on T3.MOYSKBN = T5.MOYSKBN and T3.MOYSSTDT = T5.MOYSSTDT and T3.MOYSRBAN = T5.MOYSRBAN and T3.BMNCD = T5.BMNCD and T3.KANRINO = T5.KANRINO and T3.KANRIENO = T5.KANRIENO and T5.TJFLG = 2");
    return sbSQL.toString();
  }

  /** SQLリスト保持用変数 */
  ArrayList<String> sqlList = new ArrayList<String>();
  /** SQLのパラメータリスト保持用変数 */
  ArrayList<ArrayList<String>> prmList = new ArrayList<ArrayList<String>>();
  /** SQLログ用のラベルリスト保持用変数 */
  ArrayList<String> lblList = new ArrayList<String>();

  /** SQLリスト保持用変数(付番管理用) */
  ArrayList<String> sqlList0 = new ArrayList<String>();

  /**
   * 更新処理実行
   *
   * @return
   *
   * @throws Exception
   */
  private JSONObject updateData(HashMap<String, String> map, User userInfo, String sysdate, JSONObject objset) throws Exception {
    // パラメータ取得
    String sendBtnid = map.get("SENDBTNID"); // 呼出しボタン
    String sendPageid = map.get("PAGEID"); // 呼出しPAGEID
    String szAddshukbn = map.get("ADDSHUKBN"); // 登録種別

    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 対象情報（主要な更新情報）
    JSONArray dataArrayTJTEN = JSONArray.fromObject(map.get("DATA_TJTEN")); // 対象情報（主要な更新情報）
    JSONArray dataArrayNNDT = JSONArray.fromObject(map.get("DATA_NNDT")); // 対象情報（主要な更新情報）
    JSONArray dataArrayHB = JSONArray.fromObject(map.get("DATA_HB")); // 対象情報（主要な更新情報）

    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー

    return updateExec(sendBtnid, sendPageid, szAddshukbn, userId, dataArray, dataArrayTJTEN, dataArrayNNDT, dataArrayHB);
  }

  public JSONObject updateExec(String sendBtnid, String sendPageid, String szAddshukbn, String userId, JSONArray dataArray, JSONArray dataArrayTJTEN, JSONArray dataArrayNNDT, JSONArray dataArrayHB) {

    JSONObject option = new JSONObject();
    JSONArray msg = new JSONArray();

    // パラメータ確認
    // 必須チェック
    if (sendBtnid == null || sendPageid == null || dataArray.isEmpty() || dataArray.size() != 1 || dataArray.getJSONObject(0).isEmpty()) {
      option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
      return option;
    }


    // 基本登録情報
    JSONObject data = dataArray.getJSONObject(0);

    String szMoyskbn = data.optString(TOK_CMNLayout.MOYSKBN.getId()); // 催し区分
    String szMoysstdt = data.optString(TOK_CMNLayout.MOYSSTDT.getId()); // 催し開始日
    String szMoysrban = data.optString(TOK_CMNLayout.MOYSRBAN.getId()); // 催し連番
    String szBmncd = data.optString(TOK_CMNLayout.BMNCD.getId()); // 部門

    // 全店特売アンケート有/無
    boolean isTOKTG = super.isTOKTG(szMoyskbn, szMoysrban);

    // 3.13．「登録」ボタン機能説明：
    // 3.13.1．適用対象：ST016の新規・新規（全品割引）より、ST019の選択（確定）より、ST016の月締後新規・新規（全品割引）より、ST016の月締後今の内容を修正より（商品コードを変更した場合）。
    // ※ST016の月締後新規・新規（全品割引）、月締後今の内容を修正ボタンは廃止
    boolean isNew = isModeD || isModeH;

    // 3.13.3.6．管理番号と枝番の発番：
    // 3.13.3.6.1．管理番号と枝番の取得規則：
    // ① ST016の新規・新規（全品割引）より、ST019の選択（確定）より、ST016の月締後新規・新規（全品割引）よりの場合：管理番号：新規付番 / 枝番：0
    boolean isNewKanrino = isModeD || isModeH;
    // ② ST016の月締後今の内容を修正より（１項目でも変更を行った場合）：管理番号：変更なし / 枝番：現枝番+１（「削除」の場合は現枝番+1しない）→ 機能廃止
    boolean isNewKanrieno = false;

    // ③ ST016の選択（販売・納入情報）よりの場合：管理番号：変更無し / 枝番：変更無し

    // ランクNo展開配列作成機能
    ReportBM015Dao dao = new ReportBM015Dao(JNDIname);
    ReportJU012Dao daoJu = new ReportJU012Dao(JNDIname);

    String ranknoAdd = "";
    String ranknoDel = "";
    String saveTenrankArr = "";
    if (isTOKTG) {
      ranknoAdd = data.optString(TOKTG_SHNLayout.RANKNO_ADD.getId());
      ranknoDel = "";
      saveTenrankArr = StringUtils.isEmpty(data.optString(TOKTG_SHNLayout.TENRANK_ARR.getId()).trim()) ? "" : data.optString(TOKTG_SHNLayout.TENRANK_ARR.getId());
    } else {
      ranknoAdd = data.optString(TOKSP_SHNLayout.RANKNO_ADD_A.getId());
      ranknoDel = data.optString(TOKSP_SHNLayout.RANKNO_DEL.getId());
      saveTenrankArr = StringUtils.isEmpty(data.optString(TOKSP_SHNLayout.TENRANK_ARR.getId()).trim()) ? "" : data.optString(TOKSP_SHNLayout.TENRANK_ARR.getId());
    }

    JSONArray tencdAdds = new JSONArray();
    JSONArray rankAdds = new JSONArray();
    JSONArray tencdDels = new JSONArray();

    ArrayList<String> tenranks = dao.getTenrankArray(szBmncd, szMoyskbn, szMoysstdt, szMoysrban, ranknoAdd, ranknoDel, tencdAdds, rankAdds, tencdDels, saveTenrankArr);

    String arr = "";
    for (String rank : tenranks) {
      if (StringUtils.isEmpty(rank)) {
        arr += String.format("%1s", "");
      } else {
        arr += rank;
      }
    }

    for (int i = 0; i < dataArrayTJTEN.size(); i++) {

      JSONObject dataTj = dataArrayTJTEN.optJSONObject(i);
      if (dataTj.isEmpty()) {
        continue;
      }

      // 1:対象 2:除外
      if (!dataTj.optString(TOK_CMN_TJTENLayout.SENDFLG.getId()).equals("D")) {
        if (dataTj.optString(TOK_CMN_TJTENLayout.TJFLG.getId()).equals("1")) {
          tencdAdds.add(tencdAdds.size(), dataTj.optString(TOK_CMN_TJTENLayout.TENCD.getId()));
          rankAdds.add(rankAdds.size(), dataTj.optString(TOK_CMN_TJTENLayout.TENRANK.getId()));
        } else if (dataTj.optString(TOK_CMN_TJTENLayout.TJFLG.getId()).equals("2")) {
          tencdDels.add(tencdDels.size(), dataTj.optString(TOK_CMN_TJTENLayout.TENCD.getId()));
        }
      }
    }
    tenranks = dao.getTenrankArray(szBmncd, szMoyskbn, szMoysstdt, szMoysrban, ranknoAdd, ranknoDel, tencdAdds, rankAdds, tencdDels, saveTenrankArr);

    String bycd = "";
    if (isTOKTG) {
      bycd = data.optString(TOKTG_SHNLayout.BYCD.getId());
    } else {
      bycd = data.optString(TOKSP_SHNLayout.BYCD.getId());
    }

    JSONArray byCd = getByCd(bycd);

    if (byCd.getJSONObject(0).optInt("COUNT") == 0) {
      bycd = "0";
    }

    if (isTOKTG) {
      data.replace(TOKTG_SHNLayout.TENRANK_ARR.getId(), arr);
      data.replace(TOKTG_SHNLayout.BYCD.getId(), bycd);
    } else {
      data.replace(TOKSP_SHNLayout.TENRANK_ARR.getId(), arr);
      data.replace(TOKSP_SHNLayout.BYCD.getId(), bycd);
    }

    // 3.13.3．新規登録時処理：
    if (isNew) {
      // 3.13.3.7．DB登録処理（アンケート有）：
      if (isTOKTG) {
        this.createSqlTOK_CMN_BMN(userId, data, SqlType.MRG, isTOKTG);
        this.createSqlTOKTG_SHN(userId, data, SqlType.MRG);
        // 全店特売（アンケート有）_対象除外店
        if (dataArrayTJTEN.size() > 0) {
          this.createSqlTOK_CMN_TJTEN(userId, dataArrayTJTEN, SqlType.MRG, isTOKTG);
        }
        // 全店特売（アンケート有）_納入日（店変更フラグ配列に400店分1:更新をセット）
        if (dataArrayNNDT.size() > 0) {
          this.createSqlTOKTG_NNDT(userId, dataArrayNNDT, data, SqlType.MRG);
        }
        // 注意：全店特売（アンケート有）_販売は、アンケート有では登録しない。

        // 3.13.3.7.2．管理テーブル更新
        // 催し部門内部管理、管理番号内部管理：※後述
        // 3.13.3.8．DB登録処理（アンケート無）：
      } else {
        this.createSqlTOK_CMN_BMN(userId, data, SqlType.MRG, isTOKTG);
        this.createSqlTOKSP_SHN(userId, data, SqlType.MRG);
        // 全店特売（アンケート無）_対象除外店
        if (dataArrayTJTEN.size() > 0) {
          this.createSqlTOK_CMN_TJTEN(userId, dataArrayTJTEN, SqlType.MRG, isTOKTG);
        }
        // 全店特売（アンケート無）_納入日
        if (dataArrayNNDT.size() > 0) {
          this.createSqlTOKSP_NNDT(userId, dataArrayNNDT, data, SqlType.MRG);
        }
        // 全店特売（アンケート無）_販売
        if (dataArrayHB.size() > 0) {

          String tenRankArr = "";
          String tenAtsukArr = "";

          if (dataArrayHB.getJSONObject(0).containsKey(TOKSP_HBLayout.TENATSUK_ARR.getId())) {
            tenAtsukArr = dataArrayHB.getJSONObject(0).optString(TOKSP_HBLayout.TENATSUK_ARR.getId());
            tenRankArr = data.optString(TOKSP_SHNLayout.TENRANK_ARR.getId());
          }

          String ranknoAddB = data.optString(TOKSP_SHNLayout.RANKNO_ADD_B.getId());
          String ranknoAddC = data.optString(TOKSP_SHNLayout.RANKNO_ADD_C.getId());

          HashMap<Integer, String> tenAtsuk = getTencds(szBmncd // 部門コード
              , szMoyskbn // 催し区分
              , szMoysstdt // 催し開始日
              , szMoysrban // 催し連番
              , ranknoAdd // 対象ランク№
              , ranknoDel // 除外ランク№
              , ranknoAddB // 対象ランク№B
              , ranknoAddC // 対象ランク№C
              , tencdAdds // 対象店
              , tencdDels // 除外店
              , tenAtsukArr, tenRankArr);

          this.createSqlTOKSP_HB(userId, dataArrayHB, tenAtsuk, SqlType.MRG);
        }


        // 3.13.3.8.2．管理テーブル更新
        // ① 全特_商品販売日：
        JSONObject obj = new JSONObject();
        JSONArray array = new JSONArray();

        String hbStDt = data.optString(TOKSP_SHNLayout.HBSTDT.getId());
        String hbEdDt = data.optString(TOKSP_SHNLayout.HBEDDT.getId());
        String shnCd = data.optString(TOKSP_SHNLayout.SHNCD.getId());
        String binKbn = data.optString(TOKSP_SHNLayout.BINKBN.getId());

        int amount = 0;
        String stdt = "";
        String eddt = StringUtils.isEmpty(hbEdDt) ? "" : CmnDate.dateFormat(CmnDate.getDayAddedDate(CmnDate.convDate(hbEdDt), amount));

        String pluSndFlg = StringUtils.isEmpty(data.optString(TOKSP_SHNLayout.PLUSNDFLG.getId())) ? "0" : data.optString(TOKSP_SHNLayout.PLUSNDFLG.getId());
        String moyscd = szMoyskbn + szMoysstdt + String.format("%3s", szMoysrban);

        while (!stdt.equals(eddt) && !pluSndFlg.equals("1")) {
          stdt = StringUtils.isEmpty(hbStDt) ? "" : CmnDate.dateFormat(CmnDate.getDayAddedDate(CmnDate.convDate(hbStDt), amount));
          amount++;

          // 商品販売日重複チェック
          HashMap<String, String> map = getArrMap(shnCd, "", stdt, szMoyskbn, "1", "1");
          HashMap<String, String> mapKanri = getArrMap(shnCd, "", stdt, szMoyskbn, "1", "2");

          int tenCd = 1;
          for (String rank : tenranks) {
            if (!StringUtils.isEmpty(rank) && !map.containsKey(String.valueOf(tenCd))) {
              map.put(String.valueOf(tenCd), moyscd);
            }
            if (!StringUtils.isEmpty(rank) && !mapKanri.containsKey(String.valueOf(tenCd))) {
              mapKanri.put(String.valueOf(tenCd), "reno");
            }
            tenCd++;
          }

          String moysArr = daoJu.createArr(map, "1");
          String kanriArr = daoJu.createArr(mapKanri, "2");

          obj.put(TOK_CMN_SHNHBDTLayout.SHNCD.getId(), shnCd);
          obj.put(TOK_CMN_SHNHBDTLayout.HBDT.getId(), stdt);
          obj.put(TOK_CMN_SHNHBDTLayout.MOYCD_ARR.getId(), moysArr);
          obj.put(TOK_CMN_SHNHBDTLayout.KANRINO_ARR.getId(), kanriArr);
          array.add(obj);
          obj = new JSONObject();
        }
        if (array.size() != 0) {

          String table = "INATK.TOKHTK_SHNHBDT ";
          if (!szMoyskbn.equals("3")) {
            table = "INATK.TOKSP_SHNHBDT ";
          }

          this.createSqlTOKSP_SHNHBDT(userId, array, SqlType.MRG, table);
        }

        // ② 全特（ア無）_商品納入日：
        array = new JSONArray();
        for (int i = 0; i < dataArrayNNDT.size(); i++) {

          JSONObject dataN = dataArrayNNDT.getJSONObject(i);
          if (dataN.isEmpty()) {
            continue;
          }

          // 新規登録時は納入日にチェックの入ってるもののみ登録
          if (dataN.optString(TOKSP_NNDTLayout.SENDFLG.getId()).equals("1")) {

            String nndt = dataN.optString(TOKSP_NNDTLayout.NNDT.getId());

            // 商品納入日重複チェック
            HashMap<String, String> map = getArrMap(shnCd, binKbn, nndt, szMoyskbn, "2", "1");
            HashMap<String, String> mapKanri = getArrMap(shnCd, binKbn, nndt, szMoyskbn, "2", "2");

            arr = dataN.optString(TOKSP_NNDTLayout.TENHTSU_ARR.getId());
            HashMap<String, String> mapHtsu = daoJu.getDigitMap(arr, 5, "1");

            for (HashMap.Entry<String, String> htsu : mapHtsu.entrySet()) {

              String val = htsu.getValue();
              String key = htsu.getKey();

              if (!val.equals("0") && !map.containsKey(key)) {
                map.put(key, moyscd);
              }

              if (!val.equals("0") && !mapKanri.containsKey(key)) {
                mapKanri.put(key, "reno");
              }
            }

            String moysArr = daoJu.createArr(map, "1");
            String kanriArr = daoJu.createArr(mapKanri, "2");

            obj.put(TOK_CMN_SHNNNDTLayout.SHNCD.getId(), shnCd);
            obj.put(TOK_CMN_SHNNNDTLayout.BINKBN.getId(), binKbn);
            obj.put(TOK_CMN_SHNNNDTLayout.NNDT.getId(), nndt);
            obj.put(TOK_CMN_SHNNNDTLayout.MOYCD_ARR.getId(), moysArr);
            obj.put(TOK_CMN_SHNNNDTLayout.KANRINO_ARR.getId(), kanriArr);
            array.add(obj);
            obj = new JSONObject();
          }
        }
        if (array.size() != 0) {

          String table = "INATK.TOKHTK_SHNNNDT ";
          if (!szMoyskbn.equals("3")) {
            table = "INATK.TOKSP_SHNNNDT ";
          }

          this.createSqlTOKSP_SHNNNDT(userId, array, SqlType.MRG, table);
        }

        // ③ 催し部門内部管理、管理番号内部管理：※後述
      }

      // 3.13.3.6．管理番号と枝番の発番： ※トランザクション時にselect for updateし、ロックする
      // 催し部門内部管理
      if (isNewKanrino) {
        this.createSqlSYSMOYBMN_SEL(data);
        this.createSqlSYSMOYBMN(userId, data, SqlType.MRG);
      }
      // 管理番号内部管理
      if (isNewKanrieno) {
        this.createSqlSYSMOYKANRIENO_SEL(data);
        this.createSqlSYSMOYKANRIENO(userId, data, SqlType.MRG);
      }
      // 3.14.3．更新時処理：
    } else {

      // 3.14.3.7．DB更新処理（全特アンケート有）
      if (isTOKTG) {
        this.createSqlTOK_CMN_BMN(userId, data, SqlType.MRG, isTOKTG);
        this.createSqlTOKTG_SHN(userId, data, SqlType.MRG);
        // 全店特売（アンケート有）_対象除外店
        if (dataArrayTJTEN.size() > 0) {
          this.createSqlTOK_CMN_DELINS(dataArrayTJTEN, "INATK.TOKTG_TJTEN");
          this.createSqlTOK_CMN_TJTEN(userId, dataArrayTJTEN, SqlType.MRG, isTOKTG);
        }
        // 全店特売（アンケート有）_納入日（店変更フラグ配列に400店分1:更新をセット）
        if (dataArrayNNDT.size() > 0) {
          this.createSqlTOKTG_NNDT(userId, dataArrayNNDT, data, SqlType.MRG);
        }
        // 注意：全店特売（アンケート有）_販売は、アンケート有では登録しない。
        // 重複チェックテーブルを用いない為、管理テーブルの処理はない。

        // 3.14.3.8．DB更新処理（全特アンケート無）
      } else {
        this.createSqlTOK_CMN_BMN(userId, data, SqlType.MRG, isTOKTG);
        this.createSqlTOKSP_SHN(userId, data, SqlType.MRG);
        // 全店特売（アンケート無）_対象除外店
        if (dataArrayTJTEN.size() > 0) {
          this.createSqlTOK_CMN_DELINS(dataArrayTJTEN, "INATK.TOKSP_TJTEN");
          this.createSqlTOK_CMN_TJTEN(userId, dataArrayTJTEN, SqlType.MRG, isTOKTG);
        }
        // 全店特売（アンケート無）_納入日
        if (dataArrayNNDT.size() > 0) {
          this.createSqlTOKSP_NNDT(userId, dataArrayNNDT, data, SqlType.MRG);
        }
        // 全店特売（アンケート無）_販売
        if (dataArrayHB.size() > 0) {

          String tenRankArr = "";
          String tenAtsukArr = "";

          if (dataArrayHB.getJSONObject(0).containsKey(TOKSP_HBLayout.TENATSUK_ARR.getId())) {
            tenAtsukArr = dataArrayHB.getJSONObject(0).optString(TOKSP_HBLayout.TENATSUK_ARR.getId());
            tenRankArr = data.optString(TOKSP_SHNLayout.TENRANK_ARR.getId());
          }

          String ranknoAddB = data.optString(TOKSP_SHNLayout.RANKNO_ADD_B.getId());
          String ranknoAddC = data.optString(TOKSP_SHNLayout.RANKNO_ADD_C.getId());

          HashMap<Integer, String> tenAtsuk = getTencds(szBmncd // 部門コード
              , szMoyskbn // 催し区分
              , szMoysstdt // 催し開始日
              , szMoysrban // 催し連番
              , ranknoAdd // 対象ランク№
              , ranknoDel // 除外ランク№
              , ranknoAddB // 対象ランク№B
              , ranknoAddC // 対象ランク№C
              , tencdAdds // 対象店
              , tencdDels // 除外店
              , tenAtsukArr, tenRankArr);

          this.createSqlTOKSP_HB(userId, dataArrayHB, tenAtsuk, SqlType.MRG);
        }

        // ① 全特_商品販売日：
        JSONObject obj = new JSONObject();
        JSONArray array = new JSONArray();

        String hbStDt = data.optString(TOKSP_SHNLayout.HBSTDT.getId());
        String hbEdDt = data.optString(TOKSP_SHNLayout.HBEDDT.getId());
        String shnCd = data.optString(TOKSP_SHNLayout.SHNCD.getId());
        String binKbn = data.optString(TOKSP_SHNLayout.BINKBN.getId());
        String kanriNo = data.optString(TOKSP_SHNLayout.KANRINO.getId());

        int amount = 0;
        String stdt = "";
        String eddt = StringUtils.isEmpty(hbEdDt) ? "" : CmnDate.dateFormat(CmnDate.getDayAddedDate(CmnDate.convDate(hbEdDt), amount));

        String pluSndFlg = StringUtils.isEmpty(data.optString(TOKSP_SHNLayout.PLUSNDFLG.getId())) ? "0" : data.optString(TOKSP_SHNLayout.PLUSNDFLG.getId());
        String moyscd = szMoyskbn + szMoysstdt + String.format("%3s", szMoysrban);

        while (!stdt.equals(eddt)) {
          stdt = StringUtils.isEmpty(hbStDt) ? "" : CmnDate.dateFormat(CmnDate.getDayAddedDate(CmnDate.convDate(hbStDt), amount));
          amount++;

          // 商品販売日重複チェック
          HashMap<String, String> map = getArrMap(shnCd, "", stdt, szMoyskbn, "1", "1");
          HashMap<String, String> mapKanri = getArrMap(shnCd, "", stdt, szMoyskbn, "1", "2");
          Set<String> tencds = getRmTenCds(moyscd, kanriNo, map, mapKanri);

          Iterator<String> ten = tencds.iterator();
          for (int j = 0; j < tencds.size(); j++) {

            String tenCd = ten.next();
            map.remove(tenCd);
            mapKanri.remove(tenCd);
          }

          // データが存在していてPLU配信しないフラグがたっている場合
          if (pluSndFlg.equals("1") && map.size() != 0) {

            Set<String> keys = new TreeSet<String>();

            for (HashMap.Entry<String, String> delMap : map.entrySet()) {
              String val = delMap.getValue();
              String key = delMap.getKey();

              if (val.equals(moyscd) && mapKanri.containsKey(key) && kanriNo.equals(mapKanri.get(key))) {
                keys.add(key);
              }
            }

            Iterator<String> key = keys.iterator();
            for (int j = 0; j < keys.size(); j++) {

              String getKey = key.next();
              map.remove(getKey);
              mapKanri.remove(getKey);
            }
          } else {
            int tenCd = 1;
            for (String rank : tenranks) {
              if (!StringUtils.isEmpty(rank) && !map.containsKey(String.valueOf(tenCd))) {
                map.put(String.valueOf(tenCd), moyscd);
              }
              if (!StringUtils.isEmpty(rank) && !mapKanri.containsKey(String.valueOf(tenCd))) {
                mapKanri.put(String.valueOf(tenCd), "reno");
              }
              tenCd++;
            }

          }

          String moysArr = daoJu.createArr(map, "1");
          String kanriArr = daoJu.createArr(mapKanri, "2");
          obj.put(TOK_CMN_SHNHBDTLayout.SHNCD.getId(), shnCd);
          obj.put(TOK_CMN_SHNHBDTLayout.HBDT.getId(), stdt);
          obj.put(TOK_CMN_SHNHBDTLayout.MOYCD_ARR.getId(), moysArr);
          obj.put(TOK_CMN_SHNHBDTLayout.KANRINO_ARR.getId(), kanriArr);
          array.add(obj);
          obj = new JSONObject();
        }

        if (array.size() != 0) {
          String table = "INATK.TOKHTK_SHNHBDT ";
          if (!szMoyskbn.equals("3")) {
            table = "INATK.TOKSP_SHNHBDT ";
          }
          this.createSqlTOKSP_SHNHBDT(userId, array, SqlType.MRG, table);
        }

        // ② 全特（ア無）_商品納入日：
        array = new JSONArray();
        for (int i = 0; i < dataArrayNNDT.size(); i++) {

          JSONObject dataN = dataArrayNNDT.getJSONObject(i);
          if (dataN.isEmpty()) {
            continue;
          }

          // 更新時はupdate or delete or insertの判断が必要
          StringBuffer sbSQL = new StringBuffer();
          ItemList iL = new ItemList();
          JSONArray dbDatas = new JSONArray();
          String sqlWhere = "";
          ArrayList<String> paramData = new ArrayList<String>();

          String kanrino = dataN.optString(TOKSP_NNDTLayout.KANRINO.getId());
          String kanrieno = dataN.optString(TOKSP_NNDTLayout.KANRIENO.getId());
          String nndt = dataN.optString(TOKSP_NNDTLayout.NNDT.getId());
          String chk = dataN.optString(TOKSP_NNDTLayout.SENDFLG.getId());

          if (StringUtils.isEmpty(szMoyskbn)) {
            sqlWhere += "MOYSKBN=null AND ";
          } else {
            sqlWhere += "MOYSKBN=? AND ";
            paramData.add(szMoyskbn);
          }

          if (StringUtils.isEmpty(szMoysstdt)) {
            sqlWhere += "MOYSSTDT=null AND ";
          } else {
            sqlWhere += "MOYSSTDT=? AND ";
            paramData.add(szMoysstdt);
          }

          if (StringUtils.isEmpty(szMoysrban)) {
            sqlWhere += "MOYSRBAN=null AND ";
          } else {
            sqlWhere += "MOYSRBAN=? AND ";
            paramData.add(szMoysrban);
          }

          if (StringUtils.isEmpty(szBmncd)) {
            sqlWhere += "BMNCD=null AND ";
          } else {
            sqlWhere += "BMNCD=? AND ";
            paramData.add(szBmncd);
          }

          if (StringUtils.isEmpty(kanrino)) {
            sqlWhere += "KANRINO=null AND ";
          } else {
            sqlWhere += "KANRINO=? AND ";
            paramData.add(kanrino);
          }

          if (StringUtils.isEmpty(kanrieno)) {
            sqlWhere += "KANRIENO=null AND ";
          } else {
            sqlWhere += "KANRIENO=? AND ";
            paramData.add(kanrieno);
          }

          if (StringUtils.isEmpty(nndt)) {
            sqlWhere += "NNDT=null ";
          } else {
            sqlWhere += "NNDT=? ";
            paramData.add(nndt);
          }

          sbSQL.append("SELECT ");
          sbSQL.append("MOYSKBN "); // レコード件数
          sbSQL.append("FROM ");
          sbSQL.append("INATK.TOKSP_NNDT ");
          sbSQL.append("WHERE ");
          sbSQL.append(sqlWhere); // 入力された商品コードで検索

          dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

          // レコードが存在していてチェックのある場合はupdate。チェックの無い場合はdelete
          boolean del = false;
          if (dbDatas.size() >= 1) {
            if (chk.equals("0")) {
              del = true;
            }
            // レコードが存在せずチェックのある場合はinsert。チェックの無い場合は処理なし
          } else {
            if (chk.equals("0")) {
              continue;
            }
          }

          // 商品納入日重複チェック
          HashMap<String, String> map = getArrMap(shnCd, binKbn, nndt, szMoyskbn, "2", "1");
          HashMap<String, String> mapKanri = getArrMap(shnCd, binKbn, nndt, szMoyskbn, "2", "2");
          Set<String> tencds = getRmTenCds(moyscd, kanriNo, map, mapKanri);

          Iterator<String> ten = tencds.iterator();
          for (int j = 0; j < tencds.size(); j++) {

            String tenCd = ten.next();
            map.remove(tenCd);
            mapKanri.remove(tenCd);
          }

          arr = dataN.optString(TOKSP_NNDTLayout.TENHTSU_ARR.getId());
          HashMap<String, String> mapHtsu = daoJu.getDigitMap(arr, 5, "1");

          Set<String> keys = new TreeSet<String>();

          if (mapHtsu.size() == 0 && del) {
            // 配列が作成されていない場合該当の催しコード+管理番号は削除

            for (HashMap.Entry<String, String> delMap : map.entrySet()) {
              String val = delMap.getValue();
              String key = delMap.getKey();

              if (val.equals(moyscd) && mapKanri.containsKey(key) && kanrino.equals(mapKanri.get(key))) {
                keys.add(key);
              }
            }

            Iterator<String> key = keys.iterator();
            for (int j = 0; j < keys.size(); j++) {

              String getKey = key.next();
              map.remove(getKey);
              mapKanri.remove(getKey);
            }
          }

          for (HashMap.Entry<String, String> htsu : mapHtsu.entrySet()) {

            String val = htsu.getValue();
            String key = htsu.getKey();

            if (del) {
              if (!val.equals("0") && map.containsKey(key)) {
                map.remove(key);
              }

              if (!val.equals("0") && mapKanri.containsKey(key)) {
                mapKanri.remove(key);
              }
            } else {
              if (!val.equals("0") && !map.containsKey(key)) {
                map.put(key, moyscd);
              }

              if (!val.equals("0") && !mapKanri.containsKey(key)) {
                mapKanri.put(key, "reno");
              }
            }
          }

          String moysArr = daoJu.createArr(map, "1");
          String kanriArr = daoJu.createArr(mapKanri, "2");

          obj.put(TOK_CMN_SHNNNDTLayout.SHNCD.getId(), shnCd);
          obj.put(TOK_CMN_SHNNNDTLayout.BINKBN.getId(), binKbn);
          obj.put(TOK_CMN_SHNNNDTLayout.NNDT.getId(), nndt);
          obj.put(TOK_CMN_SHNNNDTLayout.MOYCD_ARR.getId(), moysArr);
          obj.put(TOK_CMN_SHNNNDTLayout.KANRINO_ARR.getId(), kanriArr);
          array.add(obj);
          obj = new JSONObject();
        }
        if (array.size() != 0) {
          String table = "INATK.TOKHTK_SHNNNDT ";
          if (!szMoyskbn.equals("3")) {
            table = "INATK.TOKSP_SHNNNDT ";
          }
          this.createSqlTOKSP_SHNNNDT(userId, array, SqlType.MRG, table);
        }
        /*
         * // 3.15.2.2.2．管理テーブル更新 if(!DefineReport.ValKbn10002.VAL3.getVal().equals(szMoyskbn)){ //
         * ①全特_商品販売日 催し区分 <> 3の場合、該当催しコード、管理番号をクリア（UPDATFE）する JSONObject result6 =
         * this.createSqlTOKSP_SHNHBDT(userId, dataArrayHB, SqlType.MRG); // ②全特（ア無）_商品納入日 催し区分 <>
         * 3の場合、該当催しコード、管理番号をクリア（UPDATFE）する JSONObject result7 = result7 =
         * this.createSqlTOKSP_SHNNNDT(userId, dataArrayNNDT, SqlType.MRG); }else{ // ③本部個特_商品販売日
         * 催し区分=3の場合、該当催しコード、管理番号をクリア（UPDATFE）する JSONObject result6 =
         * this.createSqlTOK_CMN_SHNHBDT_DEL(userId, dataArrayHB, SqlType.MRG, "INATK.TOKHTK_SHNHBDT"); //
         * ④本部個特_商品納入日 催し区分=3の場合、該当催しコード、管理番号をクリア（UPDATFE）する JSONObject result7 =
         * this.createSqlTOK_CMN_SHNNNDT_DEL(userId, dataArrayNNDT, SqlType.MRG, "INATK.TOKHTK_SHNNNDT"); }
         */

        // ③ 催し部門内部管理、管理番号内部管理：※但し、催し部門内部管理の更新は行わない。※後述
      } // test

      // 3.13.3.6．管理番号と枝番の発番： ※トランザクション時にselect for updateし、ロックする
      // 管理番号内部管理
      if (isNewKanrieno) {
        this.createSqlSYSMOYKANRIENO_SEL(data);
        this.createSqlSYSMOYKANRIENO(userId, data, SqlType.MRG);
      }
    }

    boolean isError = false;
    ArrayList<Integer> countList = new ArrayList<Integer>();
    if (sqlList.size() > 0) {
      ArrayList<String> commands = sqlList;
      ArrayList<ArrayList<String>> paramDatas = prmList;

      Connection con = null;
      PreparedStatement statement = null;
      long startTime, stop, diff;
      try {
        // コネクションの取得
        con = DBConnection.getConnection(this.JNDIname);
        con.setAutoCommit(false);

        // 3.13.3.6．管理番号と枝番の発番
        if (sqlList0.size() > 0) {
          // 実行SQL設定
          statement = con.prepareStatement(sqlList0.get(0));

          // SQL実行
          ResultSet rs = statement.executeQuery();

          // 結果の取得
          JSONArray json = this.createJSONArray(rs);

          if (json.size() > 0) {
            JSONObject obj = json.optJSONObject(0);
            data.element(TOK_CMNLayout.KANRINO.getId(), obj.optString(TOK_CMNLayout.KANRINO.getCol()));
            data.element(TOK_CMNLayout.KANRIENO.getId(), obj.optString(TOK_CMNLayout.KANRIENO.getCol()));
          } else {
            // データがない場合完全新規
            if (isNewKanrino) {
              data.element(TOK_CMNLayout.KANRINO.getId(), "1");
              data.element(TOK_CMNLayout.KANRIENO.getId(), "0");
            }
            if (isNewKanrieno) {
              data.element(TOK_CMNLayout.KANRIENO.getId(), "1");
            }

          }
        }

        if (DefineReport.ID_DEBUG_MODE) {
          String log = "[cmnprm]";
          for (TOK_CMNLayout itm : TOK_CMNLayout.values()) {
            log += data.optString(itm.getId()) + ",";
          }
          System.out.println(StringUtils.removeEnd(log, ","));
        }

        // 排他チェック
        if (isError) {
          // 排他チェック実行用
          String targetTable = isTOKTG ? "INATK.TOKTG_SHN" : "INATK.TOKSP_SHN";
          String targetWhere = " MOYSKBN = ? and MOYSSTDT= ? and MOYSRBAN = ? and BMNCD = ? and KANRINO = ? and KANRIENO = ? and UPDKBN = 0";
          String targetValue = isTOKTG ? data.optString(TOKTG_SHNLayout.UPDDT.getId()) : data.optString(TOKSP_SHNLayout.UPDDT.getId());
          String sqlcommand = "select count(*) as VALUE from " + targetTable + " where " + targetWhere;
          // 新規以外の場合
          if (!StringUtils.isEmpty(targetValue)) {
            sqlcommand = sqlcommand + " and DATE_FORMAT(UPDDT, '%Y%m%d%H%i%s%f') = ? ";
          }
          // 実行SQL設定
          statement = con.prepareStatement(sqlcommand);
          // パラメータ設定
          for (TOK_CMNLayout itm : TOK_CMNLayout.values()) {
            statement.setString(itm.getNo(), data.optString(itm.getId()));
          }
          if (!StringUtils.isEmpty(targetValue)) {
            statement.setString((TOK_CMNLayout.values().length), targetValue);
          }

          // SQL実行
          ResultSet rs = statement.executeQuery();
          // 結果の取得
          JSONArray json = this.createJSONArray(rs);
          if (!super.checkExclusion(json, targetValue)) {
            msg.add(MessageUtility.getDbMessageExclusion(FieldType.DEFAULT, new String[] {}));
            option.put(MsgKey.E.getKey(), msg);
            isError = true;
          }
        }

        // 登録処理
        for (int index = 0; index < commands.size(); index++) {
          String command = commands.get(index);
          ArrayList<String> paramData = paramDatas.get(index);

          // 実行SQL設定
          statement = con.prepareStatement(command);

          String kanrino = "";

          if (DefineReport.ID_DEBUG_MODE)
            System.out.println("/* [sql] */" + command + "/* [prm] " + (paramData == null ? "" : StringUtils.join(paramData.toArray(), ",")) + " */");
          System.out.print("/*[paramDatasize]  " + paramData.size() + " */\n");

          // パラメータ設定
          // 共通キー情報をセット
          for (TOK_CMNLayout itm : TOK_CMNLayout.values()) {

            if (itm.getId().equals(TOK_CMNLayout.KANRINO.getId())) {
              kanrino = data.optString(itm.getId());
            }

            statement.setString(itm.getNo(), data.optString(itm.getId()));
          }


          // 各種パラメータをセット
          for (int i = 0; i < paramData.size(); i++) {

            String val = paramData.get(i);
            // 商品納入日 or 商品販売日テーブルだった場合
            if (command.contains("_SHNHBDT") && (i + 1) % TOK_CMN_SHNHBDTLayout.KANRINO_ARR.getNo() == 0) {
              val = val.replace("reno", String.format("%4s", kanrino));
            } else if (command.contains("_SHNNNDT") && (i + 1) % TOK_CMN_SHNNNDTLayout.KANRINO_ARR.getNo() == 0) {
              val = val.replace("reno", String.format("%4s", kanrino));
            }

            statement.setString((i + TOK_CMNLayout.values().length) + 1, val);
          }

          startTime = System.currentTimeMillis();

          // SQL実行
          if (DefineReport.ID_DEBUG_MODE)
            System.out.println("[sql]" + command + "[prm]" + (paramData == null ? "" : StringUtils.join(paramData.toArray(), ",")));
          System.out.print("/*[paramDatasize]  " + paramData.size() + " */\n");
          // SQL実行

          // SQL実行
          int count = statement.executeUpdate();
          countList.add(count);

          stop = System.currentTimeMillis();
          diff = stop - startTime;
          if (DefineReport.ID_DEBUG_MODE)
            System.out.println("TIME:" + diff + " ms" + " COUNT:" + count);
        }

        con.commit();
      } catch (SQLException e) {
        countList = new ArrayList<Integer>();
        rollback(con);
        e.printStackTrace();
        if (DefineReport.ID_SQLSTATE_CONNECTION_RESET.equals(e.getSQLState())) {
          // 通信切断
          setMessage(DefineReport.ID_MSG_CONNECTION_REST + "(" + e.getSQLState() + ")");
        } else {
          // その他SQLエラー
          setMessage(DefineReport.ID_MSG_SQL_EXCEPTION + e.getMessage());
        }

      } catch (Exception e) {
        countList = new ArrayList<Integer>();
        e.printStackTrace();

      } finally {
        close(statement);
        close(con);
      }
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

  public JSONArray createJSONArray(ResultSet rs) throws SQLException, Exception {
    // カラム数
    ResultSetMetaData rsmd = rs.getMetaData();
    int sizeColumn = rsmd.getColumnCount();

    JSONArray json = new JSONArray();
    // 結果の取得
    while (rs.next()) {
      JSONObject obj = new JSONObject();
      for (int i = 1; i <= sizeColumn; i++) {
        obj.put(rsmd.getColumnName(i), rs.getString(i));
      }
      // 行データ格納
      json.add(obj);
    }
    return json;
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
    // パラメータ取得
    String sendBtnid = map.get("SENDBTNID"); // 呼出しボタン
    String sendPageid = map.get("PAGEID"); // 呼出しPAGEID
    map.get("ADDSHUKBN");

    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 対象情報（主要な更新情報）
    JSONArray dataArrayTJTEN = JSONArray.fromObject(map.get("DATA_TJTEN")); // 対象情報（主要な更新情報）
    JSONArray dataArrayNNDT = JSONArray.fromObject(map.get("DATA_NNDT")); // 対象情報（主要な更新情報）
    JSONArray.fromObject(map.get("DATA_HB"));


    JSONObject option = new JSONObject();
    JSONArray msg = new JSONArray();

    // パラメータ確認
    // 必須チェック
    if (sendBtnid == null || sendPageid == null || dataArray.isEmpty() || dataArray.size() != 1 || dataArray.getJSONObject(0).isEmpty()) {
      option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
      return option;
    }

    // 画面モード情報設定
    this.setModeInfo(sendPageid, sendBtnid);

    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー
    // 基本登録情報
    JSONObject data = dataArray.getJSONObject(0);

    String szMoyskbn = data.optString(TOK_CMNLayout.MOYSKBN.getId()); // 催し区分
    String szMoysstdt = data.optString(TOK_CMNLayout.MOYSSTDT.getId()); // 催しコード（催し開始日）
    String szMoysrban = data.optString(TOK_CMNLayout.MOYSRBAN.getId()); // 催し連番
    String szBmncd = data.optString(TOK_CMNLayout.BMNCD.getId()); // 部門コード
    data.optString(TOK_CMNLayout.KANRINO.getId());
    data.optString(TOK_CMNLayout.KANRIENO.getId());


    // 全店特売アンケート有/無
    boolean isTOKTG = super.isTOKTG(szMoyskbn, szMoysrban);
    // 画面モード情報設定
    this.setModeInfo(sendPageid, sendBtnid);

    // ランクNo展開配列作成機能
    ReportBM015Dao dao = new ReportBM015Dao(JNDIname);
    ReportJU012Dao daoJu = new ReportJU012Dao(JNDIname);

    String ranknoAdd = "";
    String ranknoDel = "";
    String saveTenrankArr = "";
    if (isTOKTG) {
      ranknoAdd = data.optString(TOKTG_SHNLayout.RANKNO_ADD.getId());
      ranknoDel = "";
      saveTenrankArr = StringUtils.isEmpty(data.optString(TOKTG_SHNLayout.TENRANK_ARR.getId()).trim()) ? "" : data.optString(TOKTG_SHNLayout.TENRANK_ARR.getId());
    } else {
      ranknoAdd = data.optString(TOKSP_SHNLayout.RANKNO_ADD_A.getId());
      ranknoDel = data.optString(TOKSP_SHNLayout.RANKNO_DEL.getId());
      saveTenrankArr = StringUtils.isEmpty(data.optString(TOKSP_SHNLayout.TENRANK_ARR.getId()).trim()) ? "" : data.optString(TOKSP_SHNLayout.TENRANK_ARR.getId());
    }

    JSONArray tencdAdds = new JSONArray();
    JSONArray rankAdds = new JSONArray();
    JSONArray tencdDels = new JSONArray();

    for (int i = 0; i < dataArrayTJTEN.size(); i++) {

      JSONObject dataTj = dataArrayTJTEN.optJSONObject(i);
      if (dataTj.isEmpty()) {
        continue;
      }

      // 1:対象 2:除外
      if (dataTj.optString(TOK_CMN_TJTENLayout.TJFLG.getId()).equals("1")) {
        tencdAdds.add(tencdAdds.size(), dataTj.optString(TOK_CMN_TJTENLayout.TENCD.getId()));
        rankAdds.add(rankAdds.size(), dataTj.optString(TOK_CMN_TJTENLayout.TENRANK.getId()));
      } else if (dataTj.optString(TOK_CMN_TJTENLayout.TJFLG.getId()).equals("2")) {
        tencdDels.add(tencdDels.size(), dataTj.optString(TOK_CMN_TJTENLayout.TENCD.getId()));
      }
    }

    ArrayList<String> tenranks = dao.getTenrankArray(szBmncd, szMoyskbn, szMoysstdt, szMoysrban, ranknoAdd, ranknoDel, tencdAdds, rankAdds, tencdDels, saveTenrankArr);

    String arr = "";
    for (String rank : tenranks) {
      if (StringUtils.isEmpty(rank)) {
        arr += String.format("%1s", "");
      } else {
        arr += rank;
      }
    }

    if (isTOKTG) {
      data.replace(TOKTG_SHNLayout.TENRANK_ARR.getId(), arr);
    } else {
      data.replace(TOKSP_SHNLayout.TENRANK_ARR.getId(), arr);
    }

    // 3.15.2.1．DB登録処理（全特アンケート有）
    if (isTOKTG) {
      this.createSqlTOK_CMN_BMN_DEL(userId, data, SqlType.DEL, isTOKTG);
      this.createSqlTOK_CMN_SHN_DEL(userId, data, SqlType.DEL, isTOKTG);
      this.createSqlTOK_CMN_DEL(userId, data, SqlType.DEL, "INATK.TOKTG_TJTEN");
      this.createSqlTOK_CMN_DEL(userId, data, SqlType.DEL, "INATK.TOKTG_NNDT");
      this.createSqlTOK_CMN_DEL(userId, data, SqlType.DEL, "INATK.TOKTG_HB");
      this.createSqlSYS_CMN_DEL(userId, data, SqlType.DEL, "INATK.SYSMOYKANRIENO", isTOKTG);

      // ② ST016の月締後今の内容を修正よりの場合：Gモード→廃止
      // 3.15.2.2．DB登録処理（全特アンケート無）：
    } else {
      this.createSqlTOK_CMN_BMN_DEL(userId, data, SqlType.DEL, isTOKTG);
      this.createSqlTOK_CMN_SHN_DEL(userId, data, SqlType.DEL, isTOKTG);
      this.createSqlTOK_CMN_DEL(userId, data, SqlType.DEL, "INATK.TOKSP_TJTEN");
      this.createSqlTOK_CMN_DEL(userId, data, SqlType.DEL, "INATK.TOKSP_NNDT");
      this.createSqlTOK_CMN_DEL(userId, data, SqlType.DEL, "INATK.TOKSP_HB");

      // ① 全特_商品販売日：
      JSONObject obj = new JSONObject();
      JSONArray array = new JSONArray();

      String hbStDt = data.optString(TOKSP_SHNLayout.HBSTDT.getId());
      String hbEdDt = data.optString(TOKSP_SHNLayout.HBEDDT.getId());
      String shnCd = data.optString(TOKSP_SHNLayout.SHNCD.getId());
      String binKbn = data.optString(TOKSP_SHNLayout.BINKBN.getId());

      int amount = 0;
      String stdt = "";
      String eddt = StringUtils.isEmpty(hbEdDt) ? "" : CmnDate.dateFormat(CmnDate.getDayAddedDate(CmnDate.convDate(hbEdDt), amount));

      String moyscd = szMoyskbn + szMoysstdt + String.format("%3s", szMoysrban);

      while (!stdt.equals(eddt)) {
        stdt = CmnDate.dateFormat(CmnDate.getDayAddedDate(CmnDate.convDate(hbStDt), amount));
        amount++;

        // 商品販売日重複チェック
        HashMap<String, String> mapMoy = getArrMap(shnCd, "", stdt, szMoyskbn, "1", "1");
        HashMap<String, String> mapKanri = getArrMap(shnCd, "", stdt, szMoyskbn, "1", "2");

        int tenCd = 1;
        for (String rank : tenranks) {
          if (!StringUtils.isEmpty(rank) && mapMoy.containsKey(String.valueOf(tenCd))) {
            mapMoy.remove(String.valueOf(tenCd));
          }
          if (!StringUtils.isEmpty(rank) && mapKanri.containsKey(String.valueOf(tenCd))) {
            mapKanri.remove(String.valueOf(tenCd));
          }
          tenCd++;
        }

        String moysArr = daoJu.createArr(mapMoy, "1");
        String kanriArr = daoJu.createArr(mapKanri, "2");

        obj.put(TOK_CMN_SHNHBDTLayout.SHNCD.getId(), shnCd);
        obj.put(TOK_CMN_SHNHBDTLayout.HBDT.getId(), stdt);
        obj.put(TOK_CMN_SHNHBDTLayout.MOYCD_ARR.getId(), moysArr);
        obj.put(TOK_CMN_SHNHBDTLayout.KANRINO_ARR.getId(), kanriArr);
        array.add(obj);
        obj = new JSONObject();
      }

      if (array.size() != 0) {

        String table = "INATK.TOKHTK_SHNHBDT ";
        if (!szMoyskbn.equals("3")) {
          table = "INATK.TOKSP_SHNHBDT ";
        }

        this.createSqlTOK_CMN_SHNHBDT_DEL(userId, array, SqlType.MRG, table);
      }

      // ② 全特（ア無）_商品納入日：
      array = new JSONArray();
      for (int i = 0; i < dataArrayNNDT.size(); i++) {

        JSONObject dataN = dataArrayNNDT.getJSONObject(i);
        if (dataN.isEmpty()) {
          continue;
        }

        // 存在しているレコードのみ削除
        StringBuffer sbSQL = new StringBuffer();
        ItemList iL = new ItemList();
        JSONArray dbDatas = new JSONArray();
        String sqlWhere = "";
        ArrayList<String> paramData = new ArrayList<String>();

        String kanrino = dataN.optString(TOKSP_NNDTLayout.KANRINO.getId());
        String kanrieno = dataN.optString(TOKSP_NNDTLayout.KANRIENO.getId());
        String nndt = dataN.optString(TOKSP_NNDTLayout.NNDT.getId());

        if (StringUtils.isEmpty(szMoyskbn)) {
          sqlWhere += "MOYSKBN=null AND ";
        } else {
          sqlWhere += "MOYSKBN=? AND ";
          paramData.add(szMoyskbn);
        }

        if (StringUtils.isEmpty(szMoysstdt)) {
          sqlWhere += "MOYSSTDT=null AND ";
        } else {
          sqlWhere += "MOYSSTDT=? AND ";
          paramData.add(szMoysstdt);
        }

        if (StringUtils.isEmpty(szMoysrban)) {
          sqlWhere += "MOYSRBAN=null AND ";
        } else {
          sqlWhere += "MOYSRBAN=? AND ";
          paramData.add(szMoysrban);
        }

        if (StringUtils.isEmpty(szBmncd)) {
          sqlWhere += "BMNCD=null AND ";
        } else {
          sqlWhere += "BMNCD=? AND ";
          paramData.add(szBmncd);
        }

        if (StringUtils.isEmpty(kanrino)) {
          sqlWhere += "KANRINO=null AND ";
        } else {
          sqlWhere += "KANRINO=? AND ";
          paramData.add(kanrino);
        }

        if (StringUtils.isEmpty(kanrieno)) {
          sqlWhere += "KANRIENO=null AND ";
        } else {
          sqlWhere += "KANRIENO=? AND ";
          paramData.add(kanrieno);
        }

        if (StringUtils.isEmpty(nndt)) {
          sqlWhere += "NNDT=null ";
        } else {
          sqlWhere += "NNDT=? ";
          paramData.add(nndt);
        }

        sbSQL.append("SELECT ");
        sbSQL.append("MOYSKBN "); // レコード件数
        sbSQL.append("FROM ");
        sbSQL.append("INATK.TOKSP_NNDT ");
        sbSQL.append("WHERE ");
        sbSQL.append(sqlWhere); // 入力された商品コードで検索

        dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

        // レコードが存在していてチェックのある場合はupdate。チェックの無い場合はdelete
        if (dbDatas.size() >= 1) {

          // 商品納入日重複チェック
          HashMap<String, String> mapMoy = getArrMap(shnCd, binKbn, nndt, szMoyskbn, "2", "1");
          HashMap<String, String> mapKanri = getArrMap(shnCd, binKbn, nndt, szMoyskbn, "2", "2");

          arr = dataN.optString(TOKSP_NNDTLayout.TENHTSU_ARR.getId());
          HashMap<String, String> mapHtsu = daoJu.getDigitMap(arr, 5, "1");

          Set<String> keys = new TreeSet<String>();

          if (mapHtsu.size() == 0) {
            // 配列が作成されていない場合該当の催しコード+管理番号は削除

            for (HashMap.Entry<String, String> delMap : mapMoy.entrySet()) {
              String val = delMap.getValue();
              String key = delMap.getKey();

              if (val.equals(moyscd) && mapKanri.containsKey(key) && kanrino.equals(mapKanri.get(key))) {
                keys.add(key);
              }
            }

            Iterator<String> key = keys.iterator();
            for (int j = 0; j < keys.size(); j++) {

              String getKey = key.next();
              mapMoy.remove(getKey);
              mapKanri.remove(getKey);
            }
          }

          for (HashMap.Entry<String, String> htsu : mapHtsu.entrySet()) {

            String val = htsu.getValue();
            String key = htsu.getKey();

            if (!val.equals("0") && mapMoy.containsKey(key)) {
              mapMoy.remove(key);
            }

            if (!val.equals("0") && mapKanri.containsKey(key)) {
              mapKanri.remove(key);
            }
          }

          String moysArr = daoJu.createArr(mapMoy, "1");
          String kanriArr = daoJu.createArr(mapKanri, "2");

          obj.put(TOK_CMN_SHNNNDTLayout.SHNCD.getId(), shnCd);
          obj.put(TOK_CMN_SHNNNDTLayout.BINKBN.getId(), binKbn);
          obj.put(TOK_CMN_SHNNNDTLayout.NNDT.getId(), nndt);
          obj.put(TOK_CMN_SHNNNDTLayout.MOYCD_ARR.getId(), moysArr);
          obj.put(TOK_CMN_SHNNNDTLayout.KANRINO_ARR.getId(), kanriArr);
          array.add(obj);
          obj = new JSONObject();

          // レコードが存在せずチェックのある場合はinsert。チェックの無い場合は処理なし
        } else {
          continue;
        }
      }

      if (array.size() != 0) {
        String table = "INATK.TOKHTK_SHNNNDT ";
        if (!szMoyskbn.equals("3")) {
          table = "INATK.TOKSP_SHNNNDT ";
        }
        this.createSqlTOK_CMN_SHNNNDT_DEL(userId, array, SqlType.MRG, table);
      }

      /*
       * // 3.15.2.2.2．管理テーブル更新 if(!DefineReport.ValKbn10002.VAL3.getVal().equals(szMoyskbn)){ //
       * ①全特_商品販売日 催し区分 <> 3の場合、該当催しコード、管理番号をクリア（UPDATFE）する JSONObject result6 =
       * this.createSqlTOK_CMN_SHNHBDT_DEL(userId, dataArrayHB, SqlType.MRG, "INATK.TOKSP_SHNHBDT"); //
       * ②全特（ア無）_商品納入日 催し区分 <> 3の場合、該当催しコード、管理番号をクリア（UPDATFE）する JSONObject result7 =
       * this.createSqlTOK_CMN_SHNNNDT_DEL(userId, dataArrayNNDT, SqlType.MRG, "INATK.TOKSP_SHNNNDT");
       * }else{ // ③本部個特_商品販売日 催し区分=3の場合、該当催しコード、管理番号をクリア（UPDATFE）する JSONObject result6 =
       * this.createSqlTOK_CMN_SHNHBDT_DEL(userId, dataArrayHB, SqlType.MRG, "INATK.TOKHTK_SHNHBDT"); //
       * ④本部個特_商品納入日 催し区分=3の場合、該当催しコード、管理番号をクリア（UPDATFE）する JSONObject result7 =
       * this.createSqlTOK_CMN_SHNNNDT_DEL(userId, dataArrayNNDT, SqlType.MRG, "INATK.TOKHTK_SHNNNDT"); }
       */

      this.createSqlSYS_CMN_DEL(userId, data, SqlType.DEL, "INATK.SYSMOYBMN", isTOKTG);
      this.createSqlSYS_CMN_DEL(userId, data, SqlType.DEL, "INATK.SYSMOYKANRIENO", isTOKTG);
    }

    // 排他チェック実行用
    String targetTable = isTOKTG ? "INATK.TOKTG_SHN" : "INATK.TOKSP_SHN";
    String targetWhere = " MOYSKBN = ? and MOYSSTDT= ? and MOYSRBAN = ? and BMNCD = ? and KANRINO = ? and KANRIENO = ? and UPDKBN = 0";
    String targetValue = isTOKTG ? data.optString(TOKTG_SHNLayout.UPDDT.getId()) : data.optString(TOKSP_SHNLayout.UPDDT.getId());
    ArrayList<String> targetParam = new ArrayList<String>();
    targetParam.add(data.optString(TOK_CMNLayout.MOYSKBN.getId()));
    targetParam.add(data.optString(TOK_CMNLayout.MOYSSTDT.getId()));
    targetParam.add(data.optString(TOK_CMNLayout.MOYSRBAN.getId()));
    targetParam.add(data.optString(TOK_CMNLayout.BMNCD.getId()));
    targetParam.add(data.optString(TOK_CMNLayout.KANRINO.getId()));
    targetParam.add(data.optString(TOK_CMNLayout.KANRIENO.getId()));
    if (!super.checkExclusion(targetTable, targetWhere, targetParam, targetValue)) {
      msg.add(MessageUtility.getDbMessageExclusion(FieldType.DEFAULT, new String[] {}));
      option.put(MsgKey.E.getKey(), msg);
      return option;
    }

    ArrayList<Integer> countList = new ArrayList<Integer>();
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

    JSONArray dataArrayOther = JSONArray.fromObject(map.get("DATA_OTHER")); // 対象情報（補足更新情報）
    JSONObject dataOther = dataArrayOther.optJSONObject(0);


    // 正 .新規
    boolean isNew = DefineReport.Button.NEW.getObj().equals(sendBtnid) || DefineReport.Button.COPY.getObj().equals(sendBtnid) || DefineReport.Button.SEL_COPY.getObj().equals(sendBtnid);
    // 正 .変更
    boolean isChange = DefineReport.Button.SEL_CHANGE.getObj().equals(sendBtnid) || DefineReport.Button.SEARCH.getObj().equals(sendBtnid) || DefineReport.Button.SEI.getObj().equals(sendBtnid);
    // EX.CSVエラー修正
    boolean isCsverr = DefineReport.Button.ERR_CHANGE.getObj().equals(sendBtnid);
    if (isCsverr) {
      isNew = DefineReport.ValFileUpdkbn.NEW.getVal().equals(szCsvUpdkbn);
      isChange = DefineReport.ValFileUpdkbn.UPD.getVal().equals(szCsvUpdkbn);
    }

    MessageUtility mu = new MessageUtility();

    JSONArray msgList = this.checkData(isNew, isChange, isCsverr, false, map, userInfo, mu, dataOther);

    JSONArray msgArray = new JSONArray();
    // MessageBoxを出す関係上、1件のみ表示
    if (msgList.size() > 0) {
      msgArray.add(msgList.get(0));
    }

    JSONObject objset = new JSONObject();
    objset.put("MSG", msgArray);
    objset.put("DATA", dataArray); // 対象情報（主要な更新情報）
    objset.put("DATA_ADD", dataArrayAdd); // 対象情報（MD03111701:予約同一項目変更用の追加データ）
    objset.put("DATA_OTHER", dataOther);

    return objset;
  }

  /**
   * チェック処理(削除時)
   *
   * @throws Exception
   */
  public JSONArray checkDel(HashMap<String, String> map, User userInfo, String sysdate) {
    // パラメータ確認
    String sendBtnid = map.get("SENDBTNID"); // 呼出しボタン
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 対象情報（主要な更新情報）

    // 正 .新規
    boolean isNew = DefineReport.Button.NEW.getObj().equals(sendBtnid) || DefineReport.Button.COPY.getObj().equals(sendBtnid) || DefineReport.Button.SEL_COPY.getObj().equals(sendBtnid);
    // 正 .変更
    boolean isChange = DefineReport.Button.SEL_CHANGE.getObj().equals(sendBtnid) || DefineReport.Button.SEARCH.getObj().equals(sendBtnid) || DefineReport.Button.SEI.getObj().equals(sendBtnid);
    // EX.CSVエラー修正
    boolean isCsverr = DefineReport.Button.ERR_CHANGE.getObj().equals(sendBtnid);

    MessageUtility mu = new MessageUtility();

    List<JSONObject> msgList = new ArrayList<JSONObject>();
    // CSV情報を削除する場合は無条件チェックなし
    if (!isCsverr) {
      msgList = this.checkDataDel(isNew, isChange, false, map, userInfo, sysdate, mu, dataArray, dataArray);
    }

    JSONArray msgArray = new JSONArray();
    // MessageBoxを出す関係上、1件のみ表示
    if (msgList.size() > 0) {
      msgArray.add(msgList.get(0));
    }
    return msgArray;
  }

  public JSONArray checkData(boolean isNew, boolean isChange, boolean isCsvErr, boolean isCsvUpload, HashMap<String, String> map, User userInfo, MessageUtility mu, JSONObject dataOther // その他情報
  ) {

    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 対象情報（主要な更新情報）
    JSONArray dataArrayTJTEN = JSONArray.fromObject(map.get("DATA_TJTEN")); // 更新対象補足情報
    JSONArray dataArrayNNDT = JSONArray.fromObject(map.get("DATA_NNDT")); // 更新対象補足情報
    String hobokure = map.get("HBOKUREFLG");
    if (Boolean.getBoolean(map.get("TGFLG"))) {
      tg = true;
    } else {
      st = true;
    }

    String moyskbn = dataArray.getJSONObject(0).optString(TOK_CMNLayout.MOYSKBN.getId());
    String moysstdt = dataArray.getJSONObject(0).optString(TOK_CMNLayout.MOYSSTDT.getId());
    String moysrban = dataArray.getJSONObject(0).optString(TOK_CMNLayout.MOYSRBAN.getId());
    String bmncd = dataArray.getJSONObject(0).optString(TOK_CMNLayout.BMNCD.getId());

    map.put("MOYSKBN", moyskbn);
    map.put("MOYSSTDT", moysstdt);
    map.put("MOYSRBAN", moysrban);
    map.put("BMNCD", bmncd);

    JSONArray moycdData = getTOKMOYCDData(map); // 催しコード情報

    return getCheckResult(dataArray, dataArrayTJTEN, dataArrayNNDT, moycdData, hobokure, mu);
  }

  public JSONArray getCheckResult(JSONArray dataArray, JSONArray dataArrayTJTEN, JSONArray dataArrayNNDT, JSONArray moycdData, String hobokure, MessageUtility mu) {

    // 基本登録情報
    JSONObject data = dataArray.getJSONObject(0);

    // 格納用変数
    StringBuffer sbSQL = new StringBuffer();
    ItemList iL = new ItemList();
    JSONArray dbDatas = new JSONArray();

    // DB検索用パラメータ
    String sqlWhere = "";
    ArrayList<String> paramData = new ArrayList<String>();

    JSONArray msg = new JSONArray();

    String szMoyskbn = data.optString(TOK_CMNLayout.MOYSKBN.getId()); // 催し区分
    String szMoysstdt = data.optString(TOK_CMNLayout.MOYSSTDT.getId()); // 催しコード（催し開始日）
    String szMoysrban = data.optString(TOK_CMNLayout.MOYSRBAN.getId()); // 催し連番
    String szBmncd = data.optString(TOK_CMNLayout.BMNCD.getId()); // 部門コード

    // 催し種類情報設定
    this.setMoycdInfo(szMoyskbn, szMoysstdt, szMoysrban, szBmncd);
    // フォーム情報設定
    setFrmInfo(data.optString(TOKTG_SHNLayout.ADDSHUKBN.getId()));

    String shnCd = data.optString(TOKTG_SHNLayout.SHNCD.getId());
    if (isToktg) {
      shnCd = data.optString(TOKTG_SHNLayout.SHNCD.getId());
    } else {
      shnCd = data.optString(TOKSP_SHNLayout.SHNCD.getId());
    }

    String shnKbn = "";
    String shubetuCd = "";
    String pcKbn = "";
    String aSouBaika = "";

    sbSQL.append(createSqlSelMSTSHN(szMoyskbn, szMoysstdt, shnCd));
    paramData.add(shnCd);

    dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    if (dbDatas.size() != 0) {
      shubetuCd = dbDatas.getJSONObject(0).optString("F172");
      pcKbn = dbDatas.getJSONObject(0).optString("F173");
      shnKbn = dbDatas.getJSONObject(0).optString("F174");

      if (!StringUtils.isEmpty(dbDatas.getJSONObject(0).optString("F59")) && !isFrm4 && !isFrm5) {
        aSouBaika = String.valueOf((int) Double.parseDouble(dbDatas.getJSONObject(0).optString("F59")));
      }
    }

    // パラメータ確認
    // 必須チェック
    if (dataArray.isEmpty() || dataArray.size() == 0 || dataArray.getJSONObject(0).isEmpty()) {
      msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
      return msg;
    }

    // 1.月締後変更処理 月締後変更処理と固定表示する。
    // 2.週No. 前の画面から。
    // 3.催しコード 前の画面から。
    // 4.催し名称
    // 5.一日遅パタン アンケート無の場合、NULLを表示。
    // 6.一日遅スライドしない-販売
    // ① 【画面】.「一日遅れパタン有り」でないと、チェック不可能。 check
    // ② 【画面】.「販売期間From」 = 【画面】.「販売期間To」 AND 【画面】.「販売期間From」 = 当催しの催し開始日の場合のみチェック可能。 check
    // ③ 新規の場合、初期値は非チェック状態に設置する。 java

    // ①E20293 「一日遅れパタン有り」がチェックされていないと、一日遅スライドしない-販売はチェックできません。 0 E
    String hbSlideFlg = "";
    String hbStDt = "";
    String hbEdDt = "";
    if (isToktg) {
      hbSlideFlg = data.optString(TOKTG_SHNLayout.HBSLIDEFLG.getId());
      hbStDt = data.optString(TOKTG_SHNLayout.HBSTDT.getId());
      hbEdDt = data.optString(TOKTG_SHNLayout.HBEDDT.getId());
    } else {
      hbStDt = data.optString(TOKSP_SHNLayout.HBSTDT.getId());
      hbEdDt = data.optString(TOKSP_SHNLayout.HBEDDT.getId());
    }

    if (!StringUtils.isEmpty(hbSlideFlg) && hbSlideFlg.equals("1")) {
      if (!hobokure.equals("1")) {
        // ①E20293 「一日遅れパタン有り」がチェックされていないと、一日遅スライドしない-販売はチェックできません。 0 E
        JSONObject o = mu.getDbMessageObj("E20293", new String[] {reqNo});
        msg.add(o);
        return msg;
      }

      // ②E20387 「販売期間From」 = 「販売期間To」かつ「販売期間From」 = 当催しの催し開始日の場合のみチェックできます 0 E
      String stdt = hbStDt.substring(2);
      String eddt = hbEdDt.substring(2);
      if (!(!StringUtils.isEmpty(stdt) && !StringUtils.isEmpty(eddt) && stdt.equals(eddt) && szMoysstdt.equals(stdt))) {
        JSONObject o = mu.getDbMessageObj("E20387", new String[] {reqNo});
        msg.add(o);
        return msg;
      }
    }

    // 7.一日遅スライドしない-納入
    // ① 【画面】.「一日遅れパタン」有りでないと、チェック不可能。 check
    // ② 【画面】.「販売期間From」 = 【画面】.「販売期間To」 AND 【画面】.「販売期間From」 = 当催しの催し開始日の場合のみチェック可能。 check
    // ③ 【画面】.「一日遅スライドしない-販売」がチェックでないとチェック不可。 check
    // ④ 新規の場合、初期値は非チェック状態に設置する。 java
    // 16_5 【画面】.「発注原売価適用」非チェックの場合のみチェック可。
    String nhSlideFlg = "";
    String htGenBaikaFlg = "";
    if (isToktg) {
      nhSlideFlg = data.optString(TOKTG_SHNLayout.NHSLIDEFLG.getId());
      htGenBaikaFlg = data.optString(TOKTG_SHNLayout.HTGENBAIKAFLG.getId());
    } else {
      htGenBaikaFlg = data.optString(TOKSP_SHNLayout.HTGENBAIKAFLG.getId());
    }

    if (!StringUtils.isEmpty(nhSlideFlg) && nhSlideFlg.equals("1")) {
      // ①E20294 「一日遅れパタン有り」がチェックされていないと、一日遅スライドしない-納入はチェックできません。 0 E
      if (!hobokure.equals("1")) {
        JSONObject o = mu.getDbMessageObj("E20294", new String[] {reqNo});
        msg.add(o);
        return msg;

      }

      // ②E20387 「販売期間From」 = 「販売期間To」かつ「販売期間From」 = 当催しの催し開始日の場合のみチェックできます 0 E
      String stdt = hbStDt.substring(2);
      String eddt = hbEdDt.substring(2);
      if (!(!StringUtils.isEmpty(stdt) && !StringUtils.isEmpty(eddt) && stdt.equals(eddt) && szMoysstdt.equals(stdt))) {
        JSONObject o = mu.getDbMessageObj("E20387", new String[] {reqNo});
        msg.add(o);
        return msg;
      }

      // ③E20295 「一日遅スライドしない-販売」がチェックされていないと、一日遅スライドしない-納入はチェックできません 0 E
      if (hbSlideFlg.equals("0")) {
        JSONObject o = mu.getDbMessageObj("E20295", new String[] {reqNo});
        msg.add(o);
        return msg;
      }

      if (isFrm5 && st) {
        // E20426 「発注原売価適用」のチェックがある場合、納入期間は入力不可。 0 E
        if (htGenBaikaFlg.equals("1")) {
          JSONObject o = mu.getDbMessageObj("E20426", new String[] {reqNo});
          msg.add(o);
          return msg;
        }
      }
    }

    // 販売日重複チェック

    String pluSndFlg = "";

    if (isToktg) {
      pluSndFlg = data.optString(TOKTG_SHNLayout.PLUSNDFLG.getId());
    } else {
      pluSndFlg = data.optString(TOKSP_SHNLayout.PLUSNDFLG.getId());
    }

    if (isToktg && !StringUtils.isEmpty(hbStDt) && !StringUtils.isEmpty(hbEdDt) && !StringUtils.isEmpty(pluSndFlg) && !pluSndFlg.equals("1")) {

      // 変数を初期化
      sbSQL = new StringBuffer();
      iL = new ItemList();
      dbDatas = new JSONArray();
      sqlWhere = "";
      paramData = new ArrayList<String>();

      if (StringUtils.isEmpty(shnCd)) {
        sqlWhere += "SHNCD=null AND ";
      } else {
        sqlWhere += "SHNCD=? AND ";
        paramData.add(shnCd);
      }

      sqlWhere += "HBSTDT <= ? AND HBEDDT >= ? AND ";
      paramData.add(hbEdDt);
      paramData.add(hbStDt);

      sbSQL.append("SELECT ");
      sbSQL.append("MOYSKBN "); // レコード件数
      sbSQL.append(",MOYSRBAN "); // レコード件数
      sbSQL.append(",MOYSSTDT "); // レコード件数
      sbSQL.append(",BMNCD "); // レコード件数
      sbSQL.append(",KANRINO "); // レコード件数
      sbSQL.append(",KANRIENO "); // レコード件数
      sbSQL.append("FROM ");
      sbSQL.append("INATK.TOKTG_SHN ");
      sbSQL.append("WHERE ");
      sbSQL.append(sqlWhere); // 入力された商品コードで検索
      sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal() + " AND ");
      sbSQL.append("PLUSNDFLG<>1");

      dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

      if (dbDatas.size() >= 1) {

        if (isNew) {
          // 同一販売日の重複チェックエラー
          JSONObject o = mu.getDbMessageObj("E20449", new String[] {reqNo});
          msg.add(o);
          return msg;
        } else {

          String kanrino = data.optString(TOKTG_SHNLayout.KANRINO.getId());
          String kanrieno = data.optString(TOKTG_SHNLayout.KANRIENO.getId());
          String bmncd = !StringUtils.isEmpty(szBmncd) ? String.valueOf(Integer.valueOf(szBmncd)) : szBmncd;

          if (!szMoyskbn.equals(dbDatas.getJSONObject(0).optString("MOYSKBN")) || !szMoysstdt.equals(dbDatas.getJSONObject(0).optString("MOYSSTDT"))
              || !szMoysrban.equals(dbDatas.getJSONObject(0).optString("MOYSRBAN")) || !bmncd.equals(dbDatas.getJSONObject(0).optString("BMNCD"))
              || !kanrino.equals(dbDatas.getJSONObject(0).optString("KANRINO")) || !kanrieno.equals(dbDatas.getJSONObject(0).optString("KANRIENO"))) {
            // 同一販売日の重複チェックエラー
            JSONObject o = mu.getDbMessageObj("E20449", new String[] {reqNo});
            msg.add(o);
            return msg;
          }
        }
      }
    }

    // 8.部門 前の画面から。
    // 9.BY
    // ① 【画面】.「部門」で催し_デフォルト設定テーブルから所属コードを取得する。 java
    // ② 所属コードでログイン管理テーブルから社員レコードを取得。職員氏名順に連番を振り、"XX社員名漢字"を【画面】.「BY」リストに保存する。（XX：連番）java
    // ③ 新規の場合、初期値は空白行に設置する。 java
    // ④ 更新の場合、全店特売（アンケート有/無）_商品のBYコードは②のりストに存在しなければ、 BYコード（コードと表示名を同じにする）を【画面】.「BY」リストに加え、画面に表示する。
    // ⑤ 手入力可でロストフォーカス時に連番と一致するリストを表示する。 js
    // ⑥フォントサイズを9とする js
    // 10.商品コード
    // ① フォーカスアウト時のチェック：先頭2桁が【画面】.「部門」項目と一致する。 check
    // ② フォーカスアウト時のチェック：商品マスタの存在チェック（商品マスタ.更新区分=0 AND 【画面】.「部門」=商品マスタ.商品コードの頭2桁 だけ）。TODO：頭二けただけ？？ check
    // ③ 新規の場合、初期値はNULLに設置する。 java

    String tkanPluKbn = "";

    if (isToktg) {
      tkanPluKbn = data.optString(TOKTG_SHNLayout.TKANPLUKBN.getId());
    } else {
      tkanPluKbn = data.optString(TOKSP_SHNLayout.TKANPLUKBN.getId());
    }

    if (!StringUtils.isEmpty(shnCd)) {
      // ① E20240 部門に属さない商品コードです。 0 E
      if (!StringUtils.isEmpty(szBmncd) && !String.format("%02d", Integer.valueOf(szBmncd)).equals(shnCd.substring(0, 2))) {
        JSONObject o = mu.getDbMessageObj("E20240", new String[] {reqNo});
        msg.add(o);
        return msg;
      }
      // ② E20257 商品マスタに存在しません。 0 E

      // 変数を初期化
      sbSQL = new StringBuffer();
      iL = new ItemList();
      dbDatas = new JSONArray();
      sqlWhere = "";
      paramData = new ArrayList<String>();

      if (StringUtils.isEmpty(shnCd)) {
        sqlWhere += "SHNCD=null";
      } else {
        sqlWhere += "SHNCD=?";
        paramData.add(shnCd);
      }

      sbSQL.append("SELECT ");
      sbSQL.append("SHNCD "); // レコード件数
      sbSQL.append(",CASE WHEN TEIKANKBN='0' THEN '2' WHEN TEIKANKBN='1' THEN TEIKANKBN ELSE '1' END AS TEIKANKBN ");
      sbSQL.append("FROM ");
      sbSQL.append("INAMS.MSTSHN ");
      sbSQL.append("WHERE ");
      sbSQL.append(sqlWhere); // 入力された商品コードで検索

      dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

      if (dbDatas.size() == 0) {
        // マスタに登録のない商品
        JSONObject o = mu.getDbMessageObj("E20257", new String[] {reqNo});
        msg.add(o);
        return msg;
      }

      // 16_5 ① 商品マスタ.商品種類=5のみ。
      if (isFrm5 && st) {
        // E20488 ダミーコード（商品種類5）以外は入力できません。 0 E
        if (!shnKbn.equals("5")) {
          JSONObject o = mu.getDbMessageObj("E20488", new String[] {reqNo});
          msg.add(o);
          return msg;
        }
      }

      if (!csv) {
        String teikankbn = dbDatas.getJSONObject(0).optString("TEIKANKBN");
        if (!teikankbn.equals(tkanPluKbn) && (isFrm2 || isFrm3)) {
          JSONObject o = mu.getDbMessageObj("E20605", new String[] {reqNo});
          msg.add(o);
          return msg;
        }
      }
    }
    // 11.商品マスタ名称
    // 12.グループNo.
    // ① 半角。 easyui
    // ② 新規の場合、初期値はNULLに設置する。 java
    // 13.子No.
    // ① 入力範囲00～99。 easyui
    // ② 同一催し内の同じ「グループNo.」中でユニーク。 check
    // ③ 新規の場合、初期値はNULLに設置する。 java

    String moyskbn = "";
    String moysstdt = "";
    String moysrban = "";
    String parno = "";
    String chldNo = "";
    String tbl = "";

    if (isToktg) {
      moyskbn = data.optString(TOKTG_SHNLayout.MOYSKBN.getId());
      moysstdt = data.optString(TOKTG_SHNLayout.MOYSSTDT.getId());
      moysrban = data.optString(TOKTG_SHNLayout.MOYSRBAN.getId());
      parno = data.optString(TOKTG_SHNLayout.PARNO.getId()).trim();
      chldNo = data.optString(TOKTG_SHNLayout.CHLDNO.getId()).trim();
      tbl = "INATK.TOKTG_SHN";
    } else {
      moyskbn = data.optString(TOKSP_SHNLayout.MOYSKBN.getId());
      moysstdt = data.optString(TOKSP_SHNLayout.MOYSSTDT.getId());
      moysrban = data.optString(TOKSP_SHNLayout.MOYSRBAN.getId());
      parno = data.optString(TOKSP_SHNLayout.PARNO.getId()).trim();
      chldNo = data.optString(TOKSP_SHNLayout.CHLDNO.getId()).trim();
      tbl = "INATK.TOKSP_SHN";
    }
    if (!StringUtils.isEmpty(chldNo)) {

      // 変数を初期化
      sbSQL = new StringBuffer();
      iL = new ItemList();
      dbDatas = new JSONArray();
      sqlWhere = "";
      paramData = new ArrayList<String>();

      if (StringUtils.isEmpty(moyskbn)) {
        sqlWhere += " and MOYSKBN=null";
      } else {
        sqlWhere += " and MOYSKBN=?";
        paramData.add(moyskbn);
      }

      if (StringUtils.isEmpty(moysstdt)) {
        sqlWhere += " and MOYSSTDT=null";
      } else {
        sqlWhere += " and MOYSSTDT=?";
        paramData.add(moysstdt);
      }

      if (StringUtils.isEmpty(moysrban)) {
        sqlWhere += " and MOYSRBAN=null";
      } else {
        sqlWhere += " and MOYSRBAN=?";
        paramData.add(moysrban);
      }

      if (StringUtils.isEmpty(parno)) {
        sqlWhere += " and PARNO=null";
      } else {
        sqlWhere += " and PARNO=?";
        paramData.add(parno);
      }

      sbSQL.append(DefineReport.ID_SQL_CHK_TBL.replace("@T", tbl).replaceAll("@C", "CHLDNO").replace("?", chldNo));
      sbSQL.append(sqlWhere);

      dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

      if (dbDatas.size() != 0 && dbDatas.getJSONObject(0).optInt("VALUE") != 0) {

        if (isNew) {
          // ②E20484 子No.は同じグループNo.中でユニークでなければいけません。 0 E
          JSONObject o = mu.getDbMessageObj("E20484", new String[] {reqNo});
          msg.add(o);
          return msg;
        } else {
          String bmncd = "";
          String kanrino = "";
          String kanrieno = "";

          if (isToktg) {
            bmncd = data.optString(TOKTG_SHNLayout.BMNCD.getId());
            kanrino = data.optString(TOKTG_SHNLayout.KANRINO.getId());
            kanrieno = data.optString(TOKTG_SHNLayout.KANRIENO.getId());
          } else {
            bmncd = data.optString(TOKSP_SHNLayout.BMNCD.getId());
            kanrino = data.optString(TOKSP_SHNLayout.KANRINO.getId());
            kanrieno = data.optString(TOKSP_SHNLayout.KANRIENO.getId());
          }

          if (StringUtils.isEmpty(bmncd)) {
            sqlWhere = " and MOYSKBN=null";
          } else {
            sqlWhere = " and MOYSKBN=?";
            paramData.add(bmncd);
          }

          if (StringUtils.isEmpty(kanrino)) {
            sqlWhere += " and MOYSKBN=null";
          } else {
            sqlWhere += " and MOYSKBN=?";
            paramData.add(kanrino);
          }

          if (StringUtils.isEmpty(kanrieno)) {
            sqlWhere += " and MOYSKBN=null";
          } else {
            sqlWhere += " and MOYSKBN=?";
            paramData.add(kanrieno);
          }
          sbSQL.append(sqlWhere);
          dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

          if (dbDatas.size() != 1) {
            // ②E20484 子No.は同じグループNo.中でユニークでなければいけません。 0 E
            JSONObject o = mu.getDbMessageObj("E20484", new String[] {reqNo});
            msg.add(o);
            return msg;
          }
        }
      }
    }
    // 14.日替 新規の場合、初期値は非チェック状態に設置する。 java
    // 15.販売期間From/16.販売期間To
    // ① 入力範囲：2003/01/01～9999/12/31。 check
    // ② 【画面】.「販売期間From」≧当催しの販売開始日 AND 【画面】.「販売期間To」≦当催しの販売終了日。 check
    // ③ 【画面】.「販売期間From」と【画面】.「販売期間To」の両方入力 OR 両方NULL
    // ④ 【画面】.「販売期間From」≦【画面】.「販売期間To」
    // ⑤ 催し区分=0：レギュラーの場合、【画面】.「販売期間」編集不可。
    // ⑥ 更新の時は編集不可。
    // ⑦ 新規の場合、初期値はNULLに設置する。
    // ⑧
    // 【画面】.「販売期間From」と【画面】.「販売期間To」が全て入力されたら、販売情報タブを編集可能にする。クリアされた場合は販売情報タブを編集不可にし、値をクリアする。編集不可状態の販売情報タブの値はDBへ保存しないが、メーカー名、POP名、規格は編集不可でもDBへ保存する。
    if (!StringUtils.isEmpty(hbStDt) || !StringUtils.isEmpty(hbEdDt)) {

      int stdt = Integer.valueOf(hbStDt);
      int eddt = Integer.valueOf(hbEdDt);

      // ①E20296 販売期間の入力可能範囲は2003/01/01から9999/12/31です。 0 E
      if (stdt < 20030101 && eddt > 99991231) {
        JSONObject o = mu.getDbMessageObj("E20387", new String[] {reqNo});
        msg.add(o);
        return msg;
      }

      // ②E20481 「販売期間From」と「販売期間To」は当催しの販売期間内で入力してください。 0 E
      if (!(stdt >= moycdData.getJSONObject(0).optInt("HBSTDT") && eddt <= moycdData.getJSONObject(0).optInt("HBEDDT"))) {
        JSONObject o = mu.getDbMessageObj("E20481", new String[] {reqNo});
        msg.add(o);
        return msg;
      }
      // ④E20298 販売期間From ≦ 販売期間Toの条件で入力してください。 0 E
      if (!(stdt <= eddt)) {
        JSONObject o = mu.getDbMessageObj("E20298", new String[] {reqNo});
        msg.add(o);
        return msg;
      }
    }

    // ③E20297 販売期間Fromと販売期間Toの両方入力または両方未入力としてください。 0 E
    if ((!StringUtils.isEmpty(hbStDt) && StringUtils.isEmpty(hbEdDt)) || (StringUtils.isEmpty(hbStDt) && !StringUtils.isEmpty(hbEdDt))) {
      JSONObject o = mu.getDbMessageObj("E20297", new String[] {reqNo});
      msg.add(o);
      return msg;
    }

    // 17.納入期間From/18.納入期間To
    // ① 入力範囲：2003/01/01～9999/12/31。
    // ② 【画面】.「納入期間From」≧当催しの納入開始日 AND 【画面】.「納入期間To」≦当催しの納入終了日。
    // ③ 【画面】.「納入期間From」と【画面】.「納入期間To」の両方入力 OR 両方NULL。
    // ④ 【画面】.「納入期間From」≦【画面】.「納入期間To」
    // ⑤ 更新の時は編集不可。
    // ⑥ 新規の場合、初期値はNULLに設置する。
    // ⑦
    // 【画面】.「納入期間From」と【画面】.「納入期間To」が全て入力されたら、納入情報タブを編集可能にする。また、入力された納入期間に応じて納入情報タブの納入対象にチェックをつける。クリアされた場合は納入情報タブを編集不可にし、値をクリアする。
    String nnStDt = "";
    String nnEdDt = "";

    if (isToktg) {
      nnStDt = data.optString(TOKTG_SHNLayout.NNSTDT.getId());
      nnEdDt = data.optString(TOKTG_SHNLayout.NNEDDT.getId());
    } else {
      nnStDt = data.optString(TOKSP_SHNLayout.NNSTDT.getId());
      nnEdDt = data.optString(TOKSP_SHNLayout.NNEDDT.getId());
    }
    if (!StringUtils.isEmpty(nnStDt) || !StringUtils.isEmpty(nnEdDt)) {

      String binkbn = "";
      String bdenkbn = "";
      String wappnkbn = "";
      String shudenflg = "";

      if (isToktg) {
        binkbn = data.optString(TOKTG_SHNLayout.BINKBN.getId());
        bdenkbn = data.optString(TOKTG_SHNLayout.BDENKBN.getId());
        wappnkbn = data.optString(TOKTG_SHNLayout.WAPPNKBN.getId());
        shudenflg = data.optString(TOKTG_SHNLayout.SHUDENFLG.getId());
      } else {
        binkbn = data.optString(TOKSP_SHNLayout.BINKBN.getId());
        bdenkbn = data.optString(TOKSP_SHNLayout.BDENKBN.getId());
        wappnkbn = data.optString(TOKSP_SHNLayout.WAPPNKBN.getId());
        shudenflg = data.optString(TOKSP_SHNLayout.SHUDENFLG.getId());
      }

      // 納入情報非表示の画面はチェック不要
      if (!(st || isToktg_t || isFrm4 || isFrm5)) {

        // 便区分を入力してください
        if (StringUtils.isEmpty(binkbn)) {
          JSONObject o = mu.getDbMessageObj("E30012", new String[] {reqNo + "便区分"});
          msg.add(o);
          return msg;
        }

        // 別伝区分を入力してください
        if (StringUtils.isEmpty(bdenkbn)) {
          JSONObject o = mu.getDbMessageObj("E30012", new String[] {reqNo + "別伝区分"});
          msg.add(o);
          return msg;
        }

        // ワッペン区分を入力してください
        if (StringUtils.isEmpty(wappnkbn)) {
          JSONObject o = mu.getDbMessageObj("E30012", new String[] {reqNo + "ワッペン区分"});
          msg.add(o);
          return msg;
        }

        // 週次仕入先伝送フラグを入力してください
        if (StringUtils.isEmpty(shudenflg)) {
          JSONObject o = mu.getDbMessageObj("E30012", new String[] {reqNo + "週次仕入先伝送フラグ"});
          msg.add(o);
          return msg;
        }
      }

      int stdt = Integer.valueOf(nnStDt);
      int eddt = Integer.valueOf(nnEdDt);
      // ①E20299 納入期間の入力可能範囲は2003/01/01から9999/12/31です。 0 E
      if (stdt < 20030101 && eddt > 99991231) {
        JSONObject o = mu.getDbMessageObj("E20299", new String[] {reqNo});
        msg.add(o);
        return msg;
      }
      // ②E20482 「納入期間From」と「納入期間To」は当催しの納入期間内で入力してください。 0 E
      if (!(stdt >= moycdData.getJSONObject(0).optInt("CHK_NNSTDT") && eddt <= moycdData.getJSONObject(0).optInt("CHK_NNEDDT"))) {
        JSONObject o = mu.getDbMessageObj("E20482", new String[] {reqNo});
        msg.add(o);
        return msg;
      }
      // ④E20301 納入期間From ≦ 納入期間Toの条件で入力してください。 0 E
      if (!(stdt <= eddt)) {
        JSONObject o = mu.getDbMessageObj("E20301", new String[] {reqNo});
        msg.add(o);
        return msg;
      }

      // ③E20300 納入期間Fromと納入期間Toの両方入力または両方未入力としてください。 0 E
      if ((nnStDt.length() > 0 && nnEdDt.length() == 0) || (nnStDt.length() == 0 && nnEdDt.length() > 0)) {
        JSONObject o = mu.getDbMessageObj("E20300", new String[] {reqNo});
        msg.add(o);
        return msg;
      }

      // 16_5 【画面】.「発注原売価適用」非チェックの場合必須。それ以外は入力不可。
      if (isFrm5 && st) {
        // E20426 「発注原売価適用」のチェックがある場合、納入期間は入力不可。 0 E
        // E20427 「発注原売価適用」非チェックの場合、納入期間必須。 0 E

        if (htGenBaikaFlg.equals("1")) {
          JSONObject o = mu.getDbMessageObj("E20426", new String[] {reqNo});
          msg.add(o);
          return msg;
        } else if (htGenBaikaFlg.equals("0") && (StringUtils.isEmpty(nnStDt) || StringUtils.isEmpty(nnEdDt))) {
          JSONObject o = mu.getDbMessageObj("E20427", new String[] {reqNo});
          msg.add(o);
          return msg;
        }
      }
    }
    // 20.チラシ未掲載
    // ① 商品マスタ.商品種別=1（原材料）の場合、チェックしないとエラー。
    // ② アンケート無の場合、デフォルトでチェックを付け、編集不可。
    // ③ 新規の場合、上記の①と②以外のデフォルトはチェックしない状態にする。
    // ①E20454 商品種別=1（原材料）の場合、チェックが必須です。 0 E
    String chirasFlg = "";
    if (isToktg) {
      chirasFlg = data.optString(TOKTG_SHNLayout.CHIRASFLG.getId());
    } else {
      chirasFlg = data.optString(TOKSP_SHNLayout.CHIRASFLG.getId());
    }
    if (shubetuCd.equals("1") && (StringUtils.isEmpty(chirasFlg) || (!StringUtils.isEmpty(chirasFlg) && chirasFlg.equals("0")))) {
      JSONObject o = mu.getDbMessageObj("E20454", new String[] {reqNo});
      msg.add(o);
      return msg;
    }

    // 21.対象店
    // ① 入力範囲：001～999。
    // ② 更新の時は編集不可。
    // ③ 新規の場合、初期値はNULLに設置する。
    String ranknoAdd = "";
    if (isToktg) {
      ranknoAdd = data.optString(TOKTG_SHNLayout.RANKNO_ADD.getId());
    }
    // 16_5 【画面】.「発注原売価適用」非チェックの場合必須。それ以外は入力不可。
    if (isFrm5 && st && isToktg_t) {
      // E20428 「発注原売価適用」のチェックがある場合は対象店が入力不可。 0 E
      // E20429 「発注原売価適用」のチェックが無い場合対象店必須。 0 E
      if (htGenBaikaFlg.equals("1") && !StringUtils.isEmpty(ranknoAdd)) {
        JSONObject o = mu.getDbMessageObj("E20428", new String[] {reqNo});
        msg.add(o);
        return msg;
      } else if (htGenBaikaFlg.equals("0") && StringUtils.isEmpty(ranknoAdd)) {
        JSONObject o = mu.getDbMessageObj("E20429", new String[] {reqNo});
        msg.add(o);
        return msg;
      }
    }

    // *** 予定数 ***
    // 29.予定数
    // ① 入力範囲：0～999,999。
    // ② 新規の場合、初期値はNULLに設置する。
    // 30.仕入額
    // ① 全特（ア有/無）_商品.定貫PLU不定貫区分=1:定貫・PLUの場合、【画面】.「予定数」×【画面】.特売事前行の「原価」×【画面】.「特売事前入数」。
    // ② 全特（ア有/無）_商品.定貫PLU不定貫区分=2:不定貫の場合、【画面】.「予定数」×【画面】.A総売価行「P原価」×【画面】.「A総売価行入数」。
    // ③ 結果は小数切り捨てで表示する。
    // 31.販売額
    // ① 全特（ア有/無）_商品.定貫PLU不定貫区分=1:定貫・PLUの場合、【画面】.「予定数」×【画面】.特売事前行の「本体売価」×【画面】.「特売事前入数」。
    // ②
    // 全特（ア有/無）_商品.定貫PLU不定貫区分=2:不定貫の場合、【画面】.「予定数」×（【画面】.A総売価行の「P総売価」-税額）×【画面】.「A総売価行入数」。税額の計算は、『特売共通仕様書
    // 総額売価計算方法』部分参照。
    // 32.荒利額 【画面】.「販売額」-【画面】.「仕入額」。

    // *** レギュラー行 ***
    // ① 【画面】.「商品コード」を入力したタイミングで表示。
    // ② 【画面】.「商品コード」をクリアしたタイミングでクリア。
    // 36.原価
    // 37.A総売価 商品マスタ.レギュラー情報_売価より税込み計算。『特売共通仕様書 総額売価計算方法』部分参照。
    // 38.本体売価 【画面】.レギュラー行の「A総売価」より税抜き計算。『特売共通仕様書 本体売価算出方法』部分参照。
    // 39.入数
    // 40.値入
    // ① （【画面】.レギュラー行の「本体売価」－商品マスタ.レギュラー情報_原価）／【画面】.レギュラー行の「本体売価」*100。
    // ② 小数点以下3位切り捨て, 第2位まで求める。

    // *** 特売事前行 ***
    // 42.原価
    // ① 入力範囲：1～999,999.99。
    // ② 『特売共通仕様書 原価・売価・入数のデフォルト表示』部分参照。
    // ③ 通常は【画面】.特売追加行の「原価」 = 【画面】.特売事前行の「原価」でないとエラー。
    // ④ 但し、ドライの場合は【画面】.特売追加行の「原価」 >= 【画面】.特売事前行の「原価」を許す。また、ドライ以外で【画面】.納入情報タブの「週次伝送flg」 = 1 and
    // 【画面】.納入情報タブの「PC区分」 = 0なら、【画面】.特売追加行の「原価」 >= 【画面】.特売事前行の「原価」を許す。
    String genkaamMae = "";
    String genkaamAto = "";
    String shudenFlg = "";

    if (isToktg) {
      genkaamMae = data.optString(TOKTG_SHNLayout.GENKAAM_MAE.getId());
      genkaamAto = data.optString(TOKTG_SHNLayout.GENKAAM_ATO.getId());
      shudenFlg = data.optString(TOKTG_SHNLayout.SHUDENFLG.getId());
    } else {
      genkaamMae = data.optString(TOKSP_SHNLayout.GENKAAM_MAE.getId());
      genkaamAto = data.optString(TOKSP_SHNLayout.GENKAAM_ATO.getId());
      shudenFlg = data.optString(TOKSP_SHNLayout.SHUDENFLG.getId());
    }

    double genkaam_mae = 0;
    double genkaam_ato = 0;

    if (!StringUtils.isEmpty(genkaamMae)) {
      genkaam_mae = Double.parseDouble(genkaamMae);
    }
    if (!StringUtils.isEmpty(genkaamAto)) {
      genkaam_ato = Double.parseDouble(genkaamAto);
    }

    if (!(isFrm1 || (!isFrm1 && !StringUtils.isEmpty(shudenFlg) && shudenFlg.equals("1") && pcKbn.equals("0")))) {
      if (!tkanPluKbn.equals("2")) {
        // ③E20540 通常は特売追加行の「原価」= 特売事前行の「原価」の条件で入力してください。 0 E
        if (genkaam_mae != genkaam_ato) {
          JSONObject o = mu.getDbMessageObj("E20540", new String[] {reqNo});
          msg.add(o);
          return msg;
        }
      }
    }
    if (isFrm1 || isFrm2 || isFrm3) {
      // 16_1,16_2,16_3
      // ① 【画面】.特売事前行の「原価」と【画面】.特売追加行の「原価」どちらか必須。入力がない場合、【画面】.特売事前行の「原価」をコピーする。
      // ② 新規初期値：『特売共通仕様書 原価・売価・入数のデフォルト表示』部分参照。
      // E20304 特売事前行の「原価」と 特売追加行の「原価」のいずれかを入力してください。 0 E
      if (StringUtils.isEmpty(genkaamMae) && StringUtils.isEmpty(genkaamAto) && (isFrm1 || ((isFrm2 || isFrm3) && tkanPluKbn.equals("1")))) {
        JSONObject o = mu.getDbMessageObj("E20304", new String[] {reqNo});
        msg.add(o);
        return msg;

        // 特売追加行の「A総売価」 と 特売事前行の「A総売価」の大小関係が不正です
      } else if (!StringUtils.isEmpty(genkaamMae) && !StringUtils.isEmpty(genkaamAto) && (isFrm1 || ((isFrm2 || isFrm3) && tkanPluKbn.equals("1"))) && (genkaam_mae > genkaam_ato)) {
        JSONObject o = mu.getDbMessageObj("E20388", new String[] {reqNo});
        msg.add(o);
        return msg;
      }
    }

    // 43.A総売価
    // ① 入力範囲：1～999,999。
    // ②
    // 【画面】.レギュラー行の「A総売価」>=【画面】.特売事前行の「A総売価」。但し、【画面】.レギュラー行の「A総売価」=0orNULLの場合と催し区分=0の場合、あるいは精肉、鮮魚の場合は本チェックを行わない。
    // ③ 【画面】.特売事前行の「A総売価」>=【画面】.B部分の「B総売価」>=【画面】.C部分の「C総売価」。
    // ④ 【画面】.特売事前行の「A総売価」=【画面】.B部分の「B総売価」=【画面】.C部分の「C総売価」は不可。
    // ⑤ 『特売共通仕様書 原価・売価・入数のデフォルト表示』部分参照。
    String aBaikaam = "";
    String bBaikaam = "";
    String cBaikaam = "";

    String yoriFlg = "";
    String bd1aBaikaan = "";
    String bd1bBaikaan = "";
    String bd1cBaikaan = "";
    String bd2aBaikaan = "";
    String bd2bBaikaan = "";
    String bd2cBaikaan = "";

    if (isToktg) {
      yoriFlg = data.optString(TOKTG_SHNLayout.YORIFLG.getId());
    } else {
      yoriFlg = data.optString(TOKSP_SHNLayout.YORIFLG.getId());
    }

    if (isToktg) {
      bd1aBaikaan = data.optString(TOKTG_SHNLayout.BD1_A_BAIKAAN.getId());
      bd1bBaikaan = data.optString(TOKTG_SHNLayout.BD1_B_BAIKAAN.getId());
      bd1cBaikaan = data.optString(TOKTG_SHNLayout.BD1_C_BAIKAAN.getId());
      bd2aBaikaan = data.optString(TOKTG_SHNLayout.BD2_A_BAIKAAN.getId());
      bd2bBaikaan = data.optString(TOKTG_SHNLayout.BD2_B_BAIKAAN.getId());
      bd2cBaikaan = data.optString(TOKTG_SHNLayout.BD2_C_BAIKAAN.getId());
      aBaikaam = data.optString(TOKTG_SHNLayout.A_BAIKAAM.getId());
      bBaikaam = data.optString(TOKTG_SHNLayout.B_BAIKAAM.getId());
      cBaikaam = data.optString(TOKTG_SHNLayout.C_BAIKAAM.getId());
    } else {
      bd1aBaikaan = data.optString(TOKSP_SHNLayout.BD1_A_BAIKAAN.getId());
      bd1bBaikaan = data.optString(TOKSP_SHNLayout.BD1_B_BAIKAAN.getId());
      bd1cBaikaan = data.optString(TOKSP_SHNLayout.BD1_C_BAIKAAN.getId());
      bd2aBaikaan = data.optString(TOKSP_SHNLayout.BD2_A_BAIKAAN.getId());
      bd2bBaikaan = data.optString(TOKSP_SHNLayout.BD2_B_BAIKAAN.getId());
      bd2cBaikaan = data.optString(TOKSP_SHNLayout.BD2_C_BAIKAAN.getId());
      aBaikaam = data.optString(TOKSP_SHNLayout.A_BAIKAAM.getId());
      bBaikaam = data.optString(TOKSP_SHNLayout.B_BAIKAAM.getId());
      cBaikaam = data.optString(TOKSP_SHNLayout.C_BAIKAAM.getId());
    }

    int a_baikaam = 0;
    int b_baikaam = 0;
    int c_baikaam = 0;
    int r_baikaam = 0;
    if (!StringUtils.isEmpty(aBaikaam)) {
      a_baikaam = Integer.valueOf(aBaikaam);
    }
    if (!StringUtils.isEmpty(bBaikaam)) {
      b_baikaam = Integer.valueOf(bBaikaam);
    }
    if (!StringUtils.isEmpty(cBaikaam)) {
      c_baikaam = Integer.valueOf(cBaikaam);
    }
    if (!StringUtils.isEmpty(aSouBaika)) {
      r_baikaam = Integer.valueOf(aSouBaika);
    }

    if (a_baikaam != 0) {
      if (!tkanPluKbn.equals("2")) {
        // ②E20305 レギュラー行の「A総売価」 ≧ 特売事前行の「A総売価」の条件で入力してください。 0 E
        if (!StringUtils.isEmpty(aSouBaika) && r_baikaam != 0 && !szMoyskbn.equals("0") && !isFrm2 && !isFrm3) {
          if (!(r_baikaam >= a_baikaam)) {
            JSONObject o = mu.getDbMessageObj("E20305", new String[] {reqNo});
            msg.add(o);
            return msg;
          }
        }
        // ③E20306 特売事前行の「A総売価」 ≧ B部分の「B総売価」 ≧ C部分の「C総売価」の条件で入力してください。 0 E
        if (!(a_baikaam >= b_baikaam && b_baikaam >= c_baikaam)) {
          JSONObject o = mu.getDbMessageObj("E20306", new String[] {reqNo});
          msg.add(o);
          return msg;
        }
        // ④E20307 特売事前行の「A総売価」= B部分の「B総売価」= C部分の「C総売価」はエラーです。 0 E
        if (a_baikaam == b_baikaam && b_baikaam == c_baikaam) {
          JSONObject o = mu.getDbMessageObj("E20307", new String[] {reqNo});
          msg.add(o);
          return msg;
        }
      }
    }
    // 44.本体売価 【画面】.特売事前行の「A総売価」より税抜き計算。『特売共通仕様書 本体売価算出方法』部分参照。
    // 45.入数
    // ① 入力範囲：1～999。
    // ② 『特売共通仕様書 原価・売価・入数のデフォルト表示』部分参照。
    // 46.値入
    // ① （【画面】.特売事前行の「本体売価」－【画面】.特売事前行の「原価」）／【画面】.特売事前行の「本体売価」*100。
    // ② 小数点以下3位切り捨て, 第2位まで求める。

    // *** 特売追加行 ***
    // 48.原価
    // ① 入力範囲：1～999,999.99。
    // ② 『特売共通仕様書 原価・売価・入数のデフォルト表示』部分参照。
    // 49.A総売価 表示・入力不可
    // 50.本体売価 表示・入力不可
    // 51.入数 表示・入力不可
    // 52.値入
    // ① （【画面】.特売事前行の「本体売価」－【画面】.特売追加行の「原価」）／【画面】.特売事前行の「本体売価」*100。
    // ② 小数点以下3位切り捨て, 第2位まで求める。

    // *** B部分 ***
    // 54.B総売価
    // ① 入力範囲：1～999,999。
    // ② 新規の場合、初期値はNULLに設置する。
    // ③ 【画面】.特売事前行の「A総売価」>=【画面】.B部分の「B総売価」>=【画面】.C部分の「C総売価」
    // ④ 【画面】.特売事前行の「A総売価」=【画面】.B部分の「B総売価」=【画面】.C部分の「C総売価」は不可
    if (!StringUtils.isEmpty(bBaikaam)) {
      if (!tkanPluKbn.equals("2")) {
        // ③E20306 特売事前行の「A総売価」 ≧ B部分の「B総売価」 ≧ C部分の「C総売価」の条件で入力してください。 0 E
        if (!(a_baikaam >= b_baikaam && b_baikaam >= c_baikaam)) {
          JSONObject o = mu.getDbMessageObj("E20306", new String[] {reqNo});
          msg.add(o);
          return msg;
        }
        // ④E20307 特売事前行の「A総売価」= B部分の「B総売価」= C部分の「C総売価」はエラーです。 0 E
        if (a_baikaam == b_baikaam && b_baikaam == c_baikaam) {
          JSONObject o = mu.getDbMessageObj("E20307", new String[] {reqNo});
          msg.add(o);
          return msg;
        }
      }
    } else {
      // 「総売価1A」「総売価1B」に入力があって「B総売価」に入力が無い場合エラー
      if (!StringUtils.isEmpty(bd1aBaikaan) && !StringUtils.isEmpty(bd1bBaikaan)) {
        JSONObject o = mu.getDbMessageObj("E20396", new String[] {reqNo});
        msg.add(o);
        return msg;
      }
    }
    // 55.本体売価 【画面】.B部分の「B総売価」より税抜き計算。『特売共通仕様書 本体売価算出方法』部分参照。
    // 56.値入
    // ① （【画面】.B部分の「本体売価」－【画面】.特売事前行の「原価」）／【画面】.B部分の「本体売価」*100。
    // ② 小数点以下3位切り捨て, 第2位まで求める。
    // 57.B売店
    // ① 入力範囲：001～999。
    // ② 新規の場合、初期値はNULLに設置する。
    // ③ アンケート有の場合、編集不可。
    String rankAddB = "";
    if (isToksp) {
      rankAddB = data.optString(TOKSP_SHNLayout.RANKNO_ADD_B.getId());
    }

    if ((isFrm1 || isFrm4) && st && isToksp && !tkanPluKbn.equals("2")) {
      // 16_1 【画面】B部分の「B総売価」が入力されたら、必須となる。入力がなかったら、編集不可。
      // E20389 B部分の「B総売価」の入力がない場合、B売店入カはできません。 0 E
      // E20390 B部分の「B総売価」が入力された場合、Ｂ売店は必須です。 0 E
      if (StringUtils.isEmpty(bBaikaam) && !StringUtils.isEmpty(rankAddB)) {
        JSONObject o = mu.getDbMessageObj("E20389", new String[] {reqNo});
        msg.add(o);
        return msg;
      } else if (!StringUtils.isEmpty(bBaikaam) && StringUtils.isEmpty(rankAddB)) {
        JSONObject o = mu.getDbMessageObj("E20390", new String[] {reqNo});
        msg.add(o);
        return msg;
      }
    }

    if ((isFrm2 || isFrm3) && st && isToksp) {
      // 16_2,16_3【画面】B部分の「B総売価」 OR 【画面】B総売価行の「100ｇ総売価」の入力がある場合必須。全てに入力がなかったら、編集不可。
      // E20309 B部分の「B総売価」あるいはB総売価行の「100ｇ総売価」に入力がなかったら B売店は入力できません。 0 E
      // E20310 B部分の「B総売価」あるいはB総売価行の「100ｇ総売価」の入力がある場合はＢ売店が必須です。 0 E
      if ((StringUtils.isEmpty(bBaikaam)) && !StringUtils.isEmpty(rankAddB)) {
        JSONObject o = mu.getDbMessageObj("E20309", new String[] {reqNo});
        msg.add(o);
        return msg;
      } else if ((!StringUtils.isEmpty(bBaikaam)) && StringUtils.isEmpty(rankAddB)) {
        JSONObject o = mu.getDbMessageObj("E20310", new String[] {reqNo});
        msg.add(o);
        return msg;
      }
    }
    String aWrituKbn = "";
    String bWrituKbn = "";
    String cWrituKbn = "";
    if (isFrm5) {
      if (isToktg) {
        aWrituKbn = data.optString(TOKTG_SHNLayout.A_WRITUKBN.getId());
        bWrituKbn = data.optString(TOKTG_SHNLayout.B_WRITUKBN.getId());
        cWrituKbn = data.optString(TOKTG_SHNLayout.C_WRITUKBN.getId());
      } else {
        aWrituKbn = data.optString(TOKSP_SHNLayout.A_WRITUKBN.getId());
        bWrituKbn = data.optString(TOKSP_SHNLayout.B_WRITUKBN.getId());
        cWrituKbn = data.optString(TOKSP_SHNLayout.C_WRITUKBN.getId());
      }
    } else if (isFrm4) {
      if (isToktg) {
        aWrituKbn = data.optString(TOKTG_SHNLayout.A_BAIKAAM.getId());
        bWrituKbn = data.optString(TOKTG_SHNLayout.B_BAIKAAM.getId());
        cWrituKbn = data.optString(TOKTG_SHNLayout.C_BAIKAAM.getId());
      } else {
        aWrituKbn = data.optString(TOKSP_SHNLayout.A_BAIKAAM.getId());
        bWrituKbn = data.optString(TOKSP_SHNLayout.B_BAIKAAM.getId());
        cWrituKbn = data.optString(TOKSP_SHNLayout.C_BAIKAAM.getId());
      }
    }
    if (isFrm5 && st && isToksp) {
      // 16_5 【画面】B部分の「B総売価」が入力されたら、必須となる。入力がなかったら、編集不可。
      String baika = bWrituKbn;
      if (baika.equals("-1")) {
        baika = "";
      }

      // E20389 B部分の「B総売価」の入力がない場合、B売店入カはできません。 0 E
      // E20390 B部分の「B総売価」が入力された場合、Ｂ売店は必須です。 0 E
      if (!tkanPluKbn.equals("2")) {
        if (StringUtils.isEmpty(baika) && !StringUtils.isEmpty(rankAddB)) {
          JSONObject o = mu.getDbMessageObj("E20389", new String[] {reqNo});
          msg.add(o);
          return msg;
        } else if (!StringUtils.isEmpty(baika) && StringUtils.isEmpty(rankAddB)) {
          JSONObject o = mu.getDbMessageObj("E20390", new String[] {reqNo});
          msg.add(o);
          return msg;
        }
      }
    }

    // *** C部分 ***
    // 60.C総売価
    // ① 入力範囲：1～999,999。
    // ② 新規の場合、初期値はNULLに設置する。
    // ③ 【画面】.特売事前行の「A総売価」>=【画面】.B部分の「B総売価」>=【画面】.C部分の「C総売価」
    // ④ 【画面】.特売事前行の「A総売価」=【画面】.B部分の「B総売価」=【画面】.C部分の「C総売価」は不可
    // ⑤ 【画面】.B部分の[B総売価]が入力されいている場合のみ入力可
    if (!StringUtils.isEmpty(cBaikaam)) {
      if (!tkanPluKbn.equals("2")) {
        // ③E20306 特売事前行の「A総売価」 ≧ B部分の「B総売価」 ≧ C部分の「C総売価」の条件で入力してください。 0 E
        if (!(a_baikaam >= b_baikaam && b_baikaam >= c_baikaam)) {
          JSONObject o = mu.getDbMessageObj("E20306", new String[] {reqNo});
          msg.add(o);
          return msg;
        }
        // ④E20307 特売事前行の「A総売価」= B部分の「B総売価」= C部分の「C総売価」はエラーです。 0 E
        if (a_baikaam == b_baikaam && b_baikaam == c_baikaam) {
          JSONObject o = mu.getDbMessageObj("E20307", new String[] {reqNo});
          msg.add(o);
          return msg;
        }
        // ⑤E20544 総売価部分の「B総売価」が入力されいている場合のみ「C総売価」は入力できます。 0 E
        if (StringUtils.isEmpty(bBaikaam) && !StringUtils.isEmpty(cBaikaam)) {
          JSONObject o = mu.getDbMessageObj("E20544", new String[] {reqNo});
          msg.add(o);
          return msg;
        }
      }
    }

    // 61.本体売価 【画面】.C部分の「C総売価」より税抜き計算。『特売共通仕様書 本体売価算出方法』部分参照。
    // 62.値入
    // ① （【画面】.C部分の「本体売価」－【画面】.特売事前行の「原価」）／【画面】.C部分の「本体売価」*100。
    // ② 小数点以下3位切り捨て, 第2位まで求める。
    // 63.C売店
    // ① 入力範囲：001～999。
    // ② 新規の場合、初期値はNULLに設置する。
    // ③ アンケート有の場合、編集不可。
    String rankAddC = "";
    if (isToksp) {
      rankAddC = data.optString(TOKSP_SHNLayout.RANKNO_ADD_C.getId());
    }

    if ((isFrm1 || isFrm4) && st && isToksp) {
      // 16_1 【画面】C部分の「C総売価」が入力されたら、必須となる。入力がなっかたら、編集不可。
      // E20391 C部分の「C総売価」の入力がない場合、C売店は入カできません。 0 E
      // E20392 C部分の「C総売価」が入力された場合、Ｃ売店は必須です。 0 E
      if (!tkanPluKbn.equals("2")) {
        if (StringUtils.isEmpty(cBaikaam) && !StringUtils.isEmpty(rankAddC)) {
          JSONObject o = mu.getDbMessageObj("E20391", new String[] {reqNo});
          msg.add(o);
          return msg;
        } else if (!StringUtils.isEmpty(cBaikaam) && StringUtils.isEmpty(rankAddC)) {
          JSONObject o = mu.getDbMessageObj("E20392", new String[] {reqNo});
          msg.add(o);
          return msg;
        }
      }
    }

    if ((isFrm2 || isFrm3) && st && isToksp) {
      if (tkanPluKbn.equals("2")) {
        // 16_2,16_3 【画面】C部分の「C総売価」 OR 【画面】C総売価行の「100ｇ総売価」の入力がある場合必須。全てに入力がなかったら、編集不可。
        // E20311 C部分の「C総売価」あるいはC総売価行の「100ｇ総売価」に入力がなかったら C売店は入力できません。 0 E
        // E20312 C部分の「C総売価」あるいはC総売価行の「100ｇ総売価」の入力がある場合は、Ｃ売店が必須です。 0 E
        if (StringUtils.isEmpty(cBaikaam) && !StringUtils.isEmpty(rankAddC)) {
          JSONObject o = mu.getDbMessageObj("E20311", new String[] {reqNo});
          msg.add(o);
          return msg;
        } else if (!StringUtils.isEmpty(cBaikaam) && StringUtils.isEmpty(rankAddC)) {
          JSONObject o = mu.getDbMessageObj("E20312", new String[] {reqNo});
          msg.add(o);
          return msg;
        }
      }
    }

    if (isFrm5 && st && isToksp) {
      // 16_5 【画面】C部分の「C総売価」が入力されたら、必須となる。入力がなっかたら、編集不可。
      String baika = cWrituKbn;
      if (baika.equals("-1")) {
        baika = "";
      }

      if (!tkanPluKbn.equals("2")) {
        // E20391 C部分の「C総売価」の入力がない場合、C売店は入カできません。 0 E
        // E20392 C部分の「C総売価」が入力された場合、Ｃ売店は必須です。 0 E
        if (StringUtils.isEmpty(baika) && !StringUtils.isEmpty(rankAddC)) {
          JSONObject o = mu.getDbMessageObj("E20391", new String[] {reqNo});
          msg.add(o);
          return msg;
        } else if (!StringUtils.isEmpty(baika) && StringUtils.isEmpty(rankAddC)) {
          JSONObject o = mu.getDbMessageObj("E20392", new String[] {reqNo});
          msg.add(o);
          return msg;
        }
      }
    }

    // 65.発注原売価適用しない
    // 66.PLU商品・定貫商品 ／ 不定貫商品
    // PLU商品・定貫商品をクリック時：
    // ① レギュラー行、特売事前行、特売追加行部分の項目をデフォルト表示する。
    // ② 不定貫部分のA総売価、B総売価、C総売価部分の項目をクリアする。
    // 不定貫商品をクリック時：
    // ① レギュラー行、特売事前行、特売追加行部分の項目をクリアする。
    // ② デフォルト表示なし。

    // *** A総売価行 ***
    // 98.100g総売価
    // ① 入力範囲：1～999,999。
    // ② 新規の場合、初期値はNULLに設置する。
    // ③ 【画面】.A総売価行の「100g総売価」>=【画面】.B総売価行の「100g総売価」>=【画面】.C総売価行の「100g総売価」。
    // ④ 【画面】.A総売価行の「100g総売価」=【画面】.B総売価行の「100g総売価」=【画面】.C総売価行の「100g総売価」は不可。
    /*
     * TODO: jsからCOPYしてきた処理だがjava側に渡された時点で1,2の判断は不要の為、保留 if(allcheck && id===$.id_inp.txt_a_baikaam+2 &&
     * !$.isEmptyVal(newValue)){ var a_baikaam = id===$.id_inp.txt_a_baikaam+2 ? newValue:
     * $.getInputboxValue($('#'+$.id_inp.txt_a_baikaam+2)); var b_baikaam =
     * id===$.id_inp.txt_b_baikaam+2 ? newValue: $.getInputboxValue($('#'+$.id_inp.txt_b_baikaam+2));
     * var c_baikaam = id===$.id_inp.txt_c_baikaam+2 ? newValue:
     * $.getInputboxValue($('#'+$.id_inp.txt_c_baikaam+2)); // ③E20546 A総売価行の「100g総売価」 ≧ B総売価行の「100g総売価」
     * ≧ C総売価行の「100g総売価」 の条件で入力してください。 0 E if(!(a_baikaam*1 >= b_baikaam*1 && b_baikaam*1 >=
     * c_baikaam*1)){ return "E20546"; } // ④E20547 A総売価行の「100g総売価」 = B総売価行の「100g総売価」 =
     * C総売価行の「100g総売価」は入力できません。 0 E if(a_baikaam*1 == b_baikaam*1 && b_baikaam*1 == c_baikaam*1){ return
     * "E20547"; } }
     */

    // 99.1Kg原価
    // ① 入力範囲：1～999,999.99。
    // ② 新規の場合、初期値はNULLに設置する。
    // 100.1Kg総売価
    // ① 入力範囲：1～999,999.99。
    // ② 新規の場合、初期値はNULLに設置する。
    // ③ 【画面】.A総売価行の「1kg総売価」>=【画面】.B総売価行の「1kg総売価」>=【画面】.C総売価行の「1kg総売価」。
    // ④ 【画面】.A総売価行の「1kg総売価」=【画面】.B総売価行の「1kg総売価」=【画面】.C総売価行の「1kg総売価」は不可。
    String aGenkaam1kg = "";
    String bGenkaam1kg = "";
    String cGenkaam1kg = "";
    if (isToktg) {
      aGenkaam1kg = data.optString(TOKTG_SHNLayout.A_GENKAAM_1KG.getId());
      bGenkaam1kg = data.optString(TOKTG_SHNLayout.B_GENKAAM_1KG.getId());
      cGenkaam1kg = data.optString(TOKTG_SHNLayout.C_GENKAAM_1KG.getId());
    } else {
      aGenkaam1kg = data.optString(TOKSP_SHNLayout.A_GENKAAM_1KG.getId());
      bGenkaam1kg = data.optString(TOKSP_SHNLayout.B_GENKAAM_1KG.getId());
      cGenkaam1kg = data.optString(TOKSP_SHNLayout.C_GENKAAM_1KG.getId());
    }

    int a_genkaam1kg = 0;
    int b_genkaam1kg = 0;
    int c_genkaam1kg = 0;
    if (!StringUtils.isEmpty(aGenkaam1kg)) {
      a_genkaam1kg = Integer.valueOf(aGenkaam1kg);
    }
    if (!StringUtils.isEmpty(bGenkaam1kg)) {
      b_genkaam1kg = Integer.valueOf(bGenkaam1kg);
    }
    if (!StringUtils.isEmpty(cGenkaam1kg)) {
      c_genkaam1kg = Integer.valueOf(cGenkaam1kg);
    }

    if (tkanPluKbn.equals("2")) {
      // ③E20556 A総売価行の「1Kg総価」 ≧ B総売価行の「1Kg総価」 ≧ C総売価行の「1Kg総価」 の条件で入力してください。 0 E
      if (!(a_genkaam1kg >= b_genkaam1kg && b_genkaam1kg >= c_genkaam1kg)) {
        JSONObject o = mu.getDbMessageObj("E20556", new String[] {reqNo});
        msg.add(o);
        return msg;
      }
      // ④E20557 A総売価行の「1Kg総価」 = B総売価行の「1Kg総価」 = C総売価行の「1Kg総価」は入力できません。 0 E
      if (!(StringUtils.isEmpty(aGenkaam1kg) && StringUtils.isEmpty(bGenkaam1kg) && StringUtils.isEmpty(cGenkaam1kg)) && a_genkaam1kg == b_genkaam1kg && b_genkaam1kg == c_genkaam1kg) {
        JSONObject o = mu.getDbMessageObj("E20557", new String[] {reqNo});
        msg.add(o);
        return msg;
      }
    }

    // 101.P原価
    // ① 入力範囲：1～999,999.99。
    // ② 新規の場合、初期値はNULLに設置する。
    String genkaamP = "";
    if (isToktg) {
      genkaamP = data.optString(TOKTG_SHNLayout.GENKAAM_PACK.getId());
    } else {
      genkaamP = data.optString(TOKSP_SHNLayout.GENKAAM_PACK.getId());
    }

    if ((isFrm2 || isFrm3) && st && (isToktg_h || isToksp)) {
      if (tkanPluKbn.equals("2")) {
        // 16_2,16_3【画面】.「納入期間」入力時、必須。
        // E20313 「納入期間」入力時、P原価は必須です。 0 E
        if ((!StringUtils.isEmpty(nnStDt) && !StringUtils.isEmpty(nnEdDt)) && StringUtils.isEmpty(genkaamP)) {
          JSONObject o = mu.getDbMessageObj("E20313", new String[] {reqNo});
          msg.add(o);
          return msg;
        }
      }
    }

    // 102.P総売価
    // ① 入力範囲：1～999,999。
    // ② 新規の場合、初期値はNULLに設置する。
    // ③ 【画面】.A総売価行の「P総売価」>=【画面】.B総売価行の「P総売価」>=【画面】.C総売価行の「P総売価」。
    // ④ 【画面】.A総売価行の「P総売価」=【画面】.B総売価行の「P総売価」=【画面】.C総売価行の「P総売価」は不可。
    String aBaikaamP = "";
    String bBaikaamP = "";
    String cBaikaamP = "";
    if (isToktg) {
      aBaikaamP = data.optString(TOKTG_SHNLayout.A_BAIKAAM_PACK.getId());
      bBaikaamP = data.optString(TOKTG_SHNLayout.B_BAIKAAM_PACK.getId());
      cBaikaamP = data.optString(TOKTG_SHNLayout.C_BAIKAAM_PACK.getId());
    } else {
      aBaikaamP = data.optString(TOKSP_SHNLayout.A_BAIKAAM_PACK.getId());
      bBaikaamP = data.optString(TOKSP_SHNLayout.B_BAIKAAM_PACK.getId());
      cBaikaamP = data.optString(TOKSP_SHNLayout.C_BAIKAAM_PACK.getId());
    }

    int a_baikaamP = 0;
    int b_baikaamP = 0;
    int c_baikaamP = 0;
    if (!StringUtils.isEmpty(aBaikaamP)) {
      a_baikaamP = Integer.valueOf(aBaikaamP);
    }
    if (!StringUtils.isEmpty(bBaikaamP)) {
      b_baikaamP = Integer.valueOf(bBaikaamP);
    }
    if (!StringUtils.isEmpty(cBaikaamP)) {
      c_baikaamP = Integer.valueOf(cBaikaamP);
    }

    if (tkanPluKbn.equals("2")) {
      // ③E20559 A総売価行の「P総価」 ≧ B総売価行の「P総価」 ≧ C総売価行の「P総価」 の条件で入力してください。 0 E
      if (!(a_baikaamP >= b_baikaamP && b_baikaamP >= c_baikaamP)) {
        JSONObject o = mu.getDbMessageObj("E20559", new String[] {reqNo});
        msg.add(o);
        return msg;
      }
      // ④E20560 A総売価行の「P総価」 = B総売価行の「P総価」 = C総売価行の「P総価」は入力できません。 0 E
      if (!(StringUtils.isEmpty(aBaikaamP) && StringUtils.isEmpty(bBaikaamP) && StringUtils.isEmpty(cBaikaamP)) && a_baikaamP == b_baikaamP && b_baikaamP == c_baikaamP) {
        JSONObject o = mu.getDbMessageObj("E20560", new String[] {reqNo});
        msg.add(o);
        return msg;
      }
      if ((isFrm2 || isFrm3) && st && (isToktg_h || isToksp)) {
        // 16_2,16_3 【画面】.「納入期間」入力時、必須。
        // E20314 「納入期間」入力時、P総売価は必須です。 0 E
        if ((!StringUtils.isEmpty(nnStDt) && !StringUtils.isEmpty(nnEdDt)) && StringUtils.isEmpty(aBaikaamP)) {
          JSONObject o = mu.getDbMessageObj("E20314", new String[] {reqNo});
          msg.add(o);
          return msg;
        }
      }
    }

    // 103.入数
    // ① 入力範囲：1～999。
    // ② 新規の場合、初期値はNULLに設置する。
    String irisu = "";
    if (isToktg) {
      irisu = data.optString(TOKTG_SHNLayout.IRISU.getId());
    } else {
      irisu = data.optString(TOKSP_SHNLayout.IRISU.getId());
    }
    if ((isFrm2 || isFrm3) && st && (isToktg_h || isToksp) && tkanPluKbn.equals("2")) {
      // 16_2,16_3 【画面】.「納入期間」入力時、必須。
      // E20315 「納入期間」入力時、入数は必須です。 0 E
      if ((!StringUtils.isEmpty(nnStDt) && !StringUtils.isEmpty(nnEdDt)) && StringUtils.isEmpty(irisu)) {
        JSONObject o = mu.getDbMessageObj("E20315", new String[] {reqNo});
        msg.add(o);
        return msg;
      }
    }

    // *** B総売価行 ***
    // 105.100g総売価
    // ① 入力範囲：1～999,999。
    // ② 新規の場合、初期値はNULLに設置する。
    // ③ 【画面】.A総売価行の「100g総売価」>=【画面】.B総売価行の「100g総売価」>=【画面】.C総売価行の「100g総売価」。
    // ④ 【画面】.A総売価行の「100g総売価」=【画面】.B総売価行の「100g総売価」=【画面】.C総売価行の「100g総売価」は不可。
    /*
     * TODO: jsからCOPYしてきた処理だがjava側に渡された時点で1,2の判断は不要の為、保留 if(allcheck && id===$.id_inp.txt_b_baikaam+2 &&
     * !$.isEmptyVal(newValue)){ var a_baikaam = id===$.id_inp.txt_a_baikaam+2 ? newValue:
     * $.getInputboxValue($('#'+$.id_inp.txt_a_baikaam+2)); var b_baikaam =
     * id===$.id_inp.txt_b_baikaam+2 ? newValue: $.getInputboxValue($('#'+$.id_inp.txt_b_baikaam+2));
     * var c_baikaam = id===$.id_inp.txt_c_baikaam+2 ? newValue:
     * $.getInputboxValue($('#'+$.id_inp.txt_c_baikaam+2)); // ③E20546 A総売価行の「100g総売価」 ≧ B総売価行の「100g総売価」
     * ≧ C総売価行の「100g総売価」 の条件で入力してください。 0 E if(!(a_baikaam*1 >= b_baikaam*1 && b_baikaam*1 >=
     * c_baikaam*1)){ return "E20546"; } // ④E20547 A総売価行の「100g総売価」 = B総売価行の「100g総売価」 =
     * C総売価行の「100g総売価」は入力できません。 0 E if(a_baikaam*1 == b_baikaam*1 && b_baikaam*1 == c_baikaam*1){ return
     * "E20547"; } }
     */
    // 106.1Kg原価 表示・入力不可
    // 107.1Kg総売価
    // ① 入力範囲：1～999,999.99。
    // ② 新規の場合、初期値はNULLに設置する。
    // ③ 【画面】.A総売価行の「1kg総売価」>=【画面】.B総売価行の「1kg総売価」>=【画面】.C総売価行の「1kg総売価」。
    // ④ 【画面】.A総売価行の「1kg総売価」=【画面】.B総売価行の「1kg総売価」=【画面】.C総売価行の「1kg総売価」は不可。
    if ((isFrm2 || isFrm3) && st && (isToktg_h || isToksp)) {
      if (tkanPluKbn.equals("2")) {
        // 16_2,16_3 ① 【画面】.「納入期間」入力時で、【画面】.B総売価行の「100g総売価」に入力がある場合、必須。② それ以外入力不可。
        if ((!StringUtils.isEmpty(nnStDt) && !StringUtils.isEmpty(nnEdDt)) && !StringUtils.isEmpty(bBaikaam)) {
          // E20320 「納入期間」入力時、B総売価行の「100g総売価」に入力がある場合、1Kg総売価は必須です。 0 E
          if (StringUtils.isEmpty(bGenkaam1kg)) {
            JSONObject o = mu.getDbMessageObj("E20320", new String[] {reqNo});
            msg.add(o);
            return msg;
          }
        } else {
          // E20316 「納入期間」入力時、B総売価行の「100g総売価」に入力時以外1Kg総売価は入力できません。 0 E
          if (!StringUtils.isEmpty(bGenkaam1kg)) {
            JSONObject o = mu.getDbMessageObj("E20316", new String[] {reqNo});
            msg.add(o);
            return msg;
          }
        }
      }
    }

    // 108.P原価 表示・入力不可
    // 109.P総売価
    // ① 入力範囲：1～999,999。
    // ② 新規の場合、初期値はNULLに設置する。
    // ③ 【画面】.A総売価行の「P総売価」>=【画面】.B総売価行の「P総売価」>=【画面】.C総売価行の「P総売価」。
    // ④ 【画面】.A総売価行の「P総売価」=【画面】.B総売価行の「P総売価」=【画面】.C総売価行の「P総売価」は不可。
    if ((isFrm2 || isFrm3) && st && (isToktg_h || isToksp)) {
      if (tkanPluKbn.equals("2")) {
        // 16_2,16_3 ① 【画面】.「納入期間」入力時で、【画面】.B総売価行の「100g総売価」に入力がある場合、必須。② それ以外入力不可。
        if ((!StringUtils.isEmpty(nnStDt) && !StringUtils.isEmpty(nnEdDt)) && !StringUtils.isEmpty(bBaikaam)) {
          // E20321 「納入期間」入力時、B総売価行の「100g総売価」に入力がある場合、P総売価は必須です。 0 E
          if (StringUtils.isEmpty(bBaikaamP)) {
            JSONObject o = mu.getDbMessageObj("E20321", new String[] {reqNo});
            msg.add(o);
            return msg;
          }
        } else {
          // E20317 「納入期間」入力時、B総売価行の「100g総売価」に入力時以外P総売価は入力できません。 0 E
          if (!StringUtils.isEmpty(bBaikaamP)) {
            JSONObject o = mu.getDbMessageObj("E20317", new String[] {reqNo});
            msg.add(o);
            return msg;
          }
        }
      }
    }

    // 110.入数 表示・入力不可

    // *** C総売価行 ***
    // 112.100g総売価
    // ① 入力範囲：1～999,999。
    // ② 新規の場合、初期値はNULLに設置する。
    // ③ 【画面】.A総売価行の「100g総売価」>=【画面】.B総売価行の「100g総売価」>=【画面】.C総売価行の「100g総売価」。
    // ④ 【画面】.A総売価行の「100g総売価」=【画面】.B総売価行の「100g総売価」=【画面】.C総売価行の「100g総売価」は不可。
    // ⑤ 【画面】.B総売価行の「100g総売価」を入力しないと【画面】.C総売価行の「100g総売価」を入力できない。
    /*
     * TODO: jsからCOPYしてきた処理だがjava側に渡された時点で1,2の判断は不要の為、保留 if(allcheck && id===$.id_inp.txt_c_baikaam+2 &&
     * !$.isEmptyVal(newValue)){ var a_baikaam = id===$.id_inp.txt_a_baikaam+2 ? newValue:
     * $.getInputboxValue($('#'+$.id_inp.txt_a_baikaam+2)); var b_baikaam =
     * id===$.id_inp.txt_b_baikaam+2 ? newValue: $.getInputboxValue($('#'+$.id_inp.txt_b_baikaam+2));
     * var c_baikaam = id===$.id_inp.txt_c_baikaam+2 ? newValue:
     * $.getInputboxValue($('#'+$.id_inp.txt_c_baikaam+2)); // ③E20546 A総売価行の「100g総売価」 ≧ B総売価行の「100g総売価」
     * ≧ C総売価行の「100g総売価」 の条件で入力してください。 0 E if(!(a_baikaam*1 >= b_baikaam*1 && b_baikaam*1 >=
     * c_baikaam*1)){ return "E20546"; } // ④E20547 A総売価行の「100g総売価」 = B総売価行の「100g総売価」 =
     * C総売価行の「100g総売価」は入力できません。 0 E if(a_baikaam*1 == b_baikaam*1 && b_baikaam*1 == c_baikaam*1){ return
     * "E20547"; } }
     */
    // 113.1Kg原価 表示・入力不可
    // 114.1Kg総売価
    // ① 入力範囲：1～999,999.99。
    // ② 新規の場合、初期値はNULLに設置する。
    // ③ 【画面】.A総売価行の「1kg総売価」>=【画面】.B総売価行の「1kg総売価」>=【画面】.C総売価行の「1kg総売価」
    // ④ 【画面】.A総売価行の「1kg総売価」=【画面】.B総売価行の「1kg総売価」=【画面】.C総売価行の「1kg総売価」は不可
    // ⑤ 【画面】.B総売価行の「1kg総売価」を入力しないと【画面】.C総売価行の「1kg総売価」を入力できない。
    if ((isFrm2 || isFrm3) && st && (isToktg_h || isToksp)) {
      if (tkanPluKbn.equals("2")) {
        // 16_2,16_3 ① 【① 【画面】.「納入期間」入力時で、【画面】.C総売価行の「100g総売価」に入力がある場合、必須。② それ以外入力不可。
        if ((!StringUtils.isEmpty(nnStDt) && !StringUtils.isEmpty(nnEdDt)) && !StringUtils.isEmpty(cBaikaam)) {
          // E20322 「納入期間」入力時、C総売価行の「100g総売価」に入力がある場合、1Kg総売価は必須です。 0 E
          if (StringUtils.isEmpty(cGenkaam1kg)) {
            JSONObject o = mu.getDbMessageObj("E20322", new String[] {reqNo});
            msg.add(o);
            return msg;
          }
        } else {
          // E20318 「納入期間」入力時、C総売価行の「100g総売価」に入力時以外1Kg総売価は入力できません。 0 E
          if (!StringUtils.isEmpty(cGenkaam1kg)) {
            JSONObject o = mu.getDbMessageObj("E20318", new String[] {reqNo});
            msg.add(o);
            return msg;
          }
        }
      }
    }
    // 115.P原価 表示・入力不可
    // 116.P総売価
    // ① 入力範囲：1～999,999。
    // ② 新規の場合、初期値はNULLに設置する。
    // ③ 【画面】.A総売価行の「P総売価」>=【画面】.B総売価行の「P総売価」>=【画面】.C総売価行の「P総売価」
    // ④ 【画面】.A総売価行の「P総売価」=【画面】.B総売価行の「P総売価」=【画面】.C総売価行の「P総売価」は不可
    // ⑤ 【画面】.B総売価行の「P総売価」を入力しないと【画面】.C総売価行の「P総売価」を入力できない。
    if ((isFrm2 || isFrm3) && st && (isToktg_h || isToksp)) {
      if (tkanPluKbn.equals("2")) {
        // 16_2,16_3 ① 【画面】.「納入期間」入力時で、【画面】.C総売価行の「100g総売価」に入力がある場合、必須。② それ以外入力不可。
        if ((!StringUtils.isEmpty(nnStDt) && !StringUtils.isEmpty(nnEdDt)) && !StringUtils.isEmpty(cBaikaam)) {
          // E20323 「納入期間」入力時、【画面】.C総売価行の「100g総売価」に入力がある場合、P総売価は必須です。 0 E
          if (StringUtils.isEmpty(cBaikaamP)) {
            JSONObject o = mu.getDbMessageObj("E20323", new String[] {reqNo});
            msg.add(o);
            return msg;
          }
        } else {
          // E20319 「納入期間」入力時、C総売価行の「100g総売価」に入力時以外C総売価は入力できません。 0 E
          if (!StringUtils.isEmpty(cBaikaamP)) {
            JSONObject o = mu.getDbMessageObj("E20319", new String[] {reqNo});
            msg.add(o);
            return msg;
          }
        }
      }
    }
    // 117.入数 表示・入力不可

    // *** 総売価部分 ***
    // 16_5
    // ① リスト内容：名称マスタの割引率区分（名称コード区分＝10656）を取得する。
    // ② 新規の場合、初期値は空白行に設置する。
    // ③ 商品コード＋販売開始日で冷凍食品をチェック。『特売共通仕様書 全品割引商品登録時のチェック』部分参照。

    // 119.A総売価
    // ① 入力範囲：1～999,999。
    // ②TG016_4：【画面】.総売価部分の「A総売価」>=「B総売価」>=「C総売価」 → A総売価1で実施
    // TG016_5：【画面】.総売価部分の「A総売価」<=「B総売価」<=「C総売価」
    // ③ 【画面】.総売価部分の「A総売価」=【画面】.総売価部分の「B総売価」=【画面】.総売価部分の「C総売価」は不可。
    // ④ 【画面】.総売価部分の[B総売価]が入力されいている場合のみ入力可
    // ④ 新規初期値：NULL。
    // ⑤ TG016_5の場合は名称マスタより名称コード区分=10656でリストを作成する。

    int a_writuKbn = 0;
    int b_writuKbn = 0;
    int c_writuKbn = 0;
    if (!StringUtils.isEmpty(aWrituKbn) && !aWrituKbn.equals("-1")) {
      a_writuKbn = Integer.valueOf(aWrituKbn);
    }
    if (!StringUtils.isEmpty(bWrituKbn) && !bWrituKbn.equals("-1")) {
      b_writuKbn = Integer.valueOf(bWrituKbn);
    }
    if (!StringUtils.isEmpty(cWrituKbn) && !cWrituKbn.equals("-1")) {
      c_writuKbn = Integer.valueOf(cWrituKbn);
    }

    if (isFrm4 || isFrm5) {

      if (!StringUtils.isEmpty(aWrituKbn) && !aWrituKbn.equals("-1") && !StringUtils.isEmpty(bWrituKbn) && !bWrituKbn.equals("-1") && !StringUtils.isEmpty(cWrituKbn) && !cWrituKbn.equals("-1")) {

        // ②E20542 総売価部分の「A総売価」 ≦ 総売価部分の「B総売価」 ≦ 総売価部分の「C総売価」 の条件で入力してください。 0 E
        if (isFrm5 && !(a_writuKbn <= b_writuKbn && b_writuKbn <= c_writuKbn)) {
          JSONObject o = mu.getDbMessageObj("E20542", new String[] {reqNo});
          msg.add(o);
          return msg;
        }

        if (isFrm4 && !(c_writuKbn <= b_writuKbn && b_writuKbn <= a_writuKbn)) {
          JSONObject o = mu.getDbMessageObj("E20050", new String[] {reqNo});
          msg.add(o);
          return msg;
        }

        // ③E20053 A総売価＝B総売価＝C総売価は不可です。 0 E
        if (a_writuKbn == b_writuKbn && b_writuKbn == c_writuKbn) {
          JSONObject o = mu.getDbMessageObj("E20053", new String[] {reqNo});
          msg.add(o);
          return msg;
        }
      }
      // ④E20544 総売価部分の「B総売価」が入力されいている場合のみ「C総売価」は入力できます。 0 E
      if ((StringUtils.isEmpty(bWrituKbn) || bWrituKbn.equals("-1")) && (!StringUtils.isEmpty(cWrituKbn) && !cWrituKbn.equals("-1"))) {
        JSONObject o = mu.getDbMessageObj("E20544", new String[] {reqNo});
        msg.add(o);
        return msg;
      }

      // 総売価部分 16_5 ③
      if (isFrm5) {
        // ③E20220 冷凍食品企画に登録されていません。 0 E
        // 変数を初期化
        sbSQL = new StringBuffer();
        iL = new ItemList();
        dbDatas = new JSONArray();
        sqlWhere = "";
        paramData = new ArrayList<String>();

        if (!StringUtils.isEmpty(hbStDt) && !StringUtils.isEmpty(shnCd)) {
          if (!StringUtils.isEmpty(aWrituKbn) && !aWrituKbn.equals("-1")) {
            sbSQL.append(DefineReport.ID_SQL_TOKRS_KKK_CNT);
            paramData.add(hbStDt); // 販売開始日
            paramData.add(shnCd); // 商品コード
            paramData.add(aWrituKbn); // 割引率

            dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

            if (dbDatas.size() == 0) {
              // マスタに登録のない商品
              msg.add(mu.getDbMessageObj("E20220", new String[] {reqNo}));
              return msg;
            }
          }

          // 変数を初期化
          sbSQL = new StringBuffer();
          iL = new ItemList();
          dbDatas = new JSONArray();
          sqlWhere = "";
          paramData = new ArrayList<String>();

          if (!StringUtils.isEmpty(bWrituKbn) && !bWrituKbn.equals("-1")) {
            sbSQL.append(DefineReport.ID_SQL_TOKRS_KKK_CNT);
            paramData.add(hbStDt); // 販売開始日
            paramData.add(shnCd); // 商品コード
            paramData.add(bWrituKbn); // 割引率

            dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);
            if (dbDatas.size() == 0) {
              // マスタに登録のない商品
              msg.add(mu.getDbMessageObj("E20220", new String[] {reqNo}));
              return msg;
            }
          }

          // 変数を初期化
          sbSQL = new StringBuffer();
          iL = new ItemList();
          dbDatas = new JSONArray();
          sqlWhere = "";
          paramData = new ArrayList<String>();
          if (!StringUtils.isEmpty(cWrituKbn) && !cWrituKbn.equals("-1")) {
            sbSQL.append(DefineReport.ID_SQL_TOKRS_KKK_CNT);
            paramData.add(hbStDt); // 販売開始日
            paramData.add(shnCd); // 商品コード
            paramData.add(cWrituKbn); // 割引率

            dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

            if (dbDatas.size() == 0) {
              // マスタに登録のない商品
              msg.add(mu.getDbMessageObj("E20220", new String[] {reqNo}));
              return msg;
            }
          }
        }
      }
    }

    // 【販売情報部分】
    // 123.産地
    // ① 全角20文字。
    // ② 新規の場合、初期値はNULLに設置する。
    // 124.メーカー名
    // ① 新規の初期値：商品マスタ.メーカーコードでメーカーマスタ.メーカー名（漢字）を取得する。メーカー名（漢字）の前28桁を使う。
    // ② 全角14文字。
    // ③ 【画面】.「商品コード」を入力したタイミングで上書き表示。
    // 125.POP名称
    // ① 新規の初期値：商品マスタ.POP名称を取得する。
    // ② 全角20文字。
    // ③ 【画面】.「商品コード」を入力したタイミングで上書き表示。
    String popKn = "";
    if (isToktg) {
      popKn = data.optString(TOKTG_SHNLayout.POPKN.getId());
    } else {
      popKn = data.optString(TOKSP_SHNLayout.POPKN.getId());
    }

    if ((isFrm2 || isFrm3) && st && (isToktg_h || isToksp)) {

      // 16_2,16_3【画面】.「販売期間」入力があれば、必須。
      // E20324 「販売期間」入力時、ＰＯＰ名称は必須です。 0 E
      if ((!StringUtils.isEmpty(hbStDt) && !StringUtils.isEmpty(hbEdDt)) && StringUtils.isEmpty(popKn)) {
        msg.add(mu.getDbMessageObj("E20324", new String[] {reqNo}));
        return msg;

      }
    }
    // 126.規格
    // ① 新規の初期値：商品マスタ.規格を取得する。
    // ② 全角23文字。
    // ③ 【画面】.「商品コード」を入力したタイミングで上書き表示。
    // 127.制限部分
    // 128.先着人数
    // ① 入力範囲：1～99,999。
    // ② 新規の場合、初期値はNULLに設置する。
    // 129.限定表現
    // ① 全角10文字。
    // ② 新規画面：
    // 名称マスタ（名称コード区分=10670）よりリストを作成。初期値は空白行に設置
    // リスト選択＋手入力も可能。DBへは画面上選択したテキストを保持
    // ③ 変更画面：
    // 名称マスタ（名称コード区分=10670）と全店特売（アンケート有/無）_商品.制限_限定表現よりリストを作成。初期値はDB内容に設置する
    // リスト選択＋手入力も可能。DBへは画面上選択したテキストを保持
    String segnGentei = "";
    if (isToktg) {
      segnGentei = data.optString(TOKTG_SHNLayout.SEGN_GENTEI.getId());
    } else {
      segnGentei = data.optString(TOKSP_SHNLayout.SEGN_GENTEI.getId());
    }

    if (!(segnGentei.length() <= 10 && InputChecker.checkDataType(DataType.ZEN, segnGentei))) {
      // ①E20479 限定表現のは全角10文字以内で入力してください。 0 E
      // ※直接入力時チェック
      msg.add(mu.getDbMessageObj("E20479", new String[] {reqNo}));
      return msg;
    }

    // 130.一人
    // ① 入力範囲：1～999。
    // ② 新規の場合、初期値はNULLに設置する。
    // 131.単位
    // ① 全角5文字。
    // ② 【画面】.制限部分の「一人」が入力された場合は必須。入力がなかったら入力不可。
    // ③ 新規画面：
    // 名称マスタ（名称コード区分=10671）よりリストを作成。初期値は空白行に設置
    // リスト選択＋手入力も可能。DBへは画面上選択したテキストを保持
    // ④ 変更画面：
    // 名称マスタ（名称コード区分=10671）と全店特売（アンケート有/無）_商品.制限_一人当り個数単位よりリストを作成。初期値はDB内容に設置する
    // リスト選択＋手入力も可能。DBへは画面上選択したテキストを保持
    String segn1kosuTni = "";
    String segn1Kosu = "";
    if (isToktg) {
      segn1kosuTni = data.optString(TOKTG_SHNLayout.SEGN_1KOSUTNI.getId());
      segn1Kosu = data.optString(TOKTG_SHNLayout.SEGN_1KOSU.getId());
    } else {
      segn1kosuTni = data.optString(TOKSP_SHNLayout.SEGN_1KOSUTNI.getId());
      segn1Kosu = data.optString(TOKSP_SHNLayout.SEGN_1KOSU.getId());
    }

    if (!(segn1kosuTni.length() <= 5 && InputChecker.checkDataType(DataType.ZEN, segn1kosuTni))) {
      // ①E20480 単位は全角5文字以内で入力してください。 0 E
      // ※直接入力時チェック
      msg.add(mu.getDbMessageObj("E20480", new String[] {reqNo}));
      return msg;
    }

    // ② E20483 「一人」が入力された場合は「単位」は必須です。 0 E
    // ② E20349 制限部分の「一人」が入力がなかったら単位入力できません。 0 E
    if ((StringUtils.isEmpty(segn1kosuTni) || segn1kosuTni.equals("-1")) && !StringUtils.isEmpty(segn1Kosu)) {
      msg.add(mu.getDbMessageObj("E20483", new String[] {reqNo}));
      return msg;
    } else if ((!StringUtils.isEmpty(segn1kosuTni) && !segn1kosuTni.equals("-1")) && StringUtils.isEmpty(segn1Kosu)) {
      msg.add(mu.getDbMessageObj("E20349", new String[] {reqNo}));
      return msg;
    }

    // 132.PLU配信しない
    // 【画面】.「販売期間」に入力があれば、デフォルト未チェック状態に設置する。入力がなかったら、デフォルトチェック状態に設置し、編集不可。TODO:???
    String binKbn = "";

    if (isToktg) {
      binKbn = data.optString(TOKTG_SHNLayout.BINKBN.getId());
    } else {
      binKbn = data.optString(TOKSP_SHNLayout.BINKBN.getId());
    }

    if (pluSndFlg.equals("0") && binKbn.equals("2")) {
      msg.add(mu.getDbMessageObj("E20449", new String[] {reqNo}));
      return msg;
    }

    int bd1aBaikaani = 0;
    int bd1bBaikaani = 0;
    int bd1cBaikaani = 0;
    if (!StringUtils.isEmpty(bd1aBaikaan)) {
      bd1aBaikaani = Integer.valueOf(bd1aBaikaan);
    }
    if (!StringUtils.isEmpty(bd1bBaikaan)) {
      bd1bBaikaani = Integer.valueOf(bd1bBaikaan);
    }
    if (!StringUtils.isEmpty(bd1cBaikaan)) {
      bd1cBaikaani = Integer.valueOf(bd1cBaikaan);
    }

    // 133.よりどり 新規の場合、初期値は非チェック状態に設置する。
    if ((isFrm1 || isFrm2 || isFrm3) && st) {
      // 【画面】.バンドル1部分の「総売価1A」の入力がある場合のみチェック可。
      String baika1 = bd1aBaikaan;

      if (StringUtils.isEmpty(baika1) && (!StringUtils.isEmpty(yoriFlg) && yoriFlg.equals("1"))) {
        // E20325 バンドル1部分の「総売価1A」の入力がある場合のみ、よりどりがチェック可能です。 0 E
        msg.add(mu.getDbMessageObj("E20325", new String[] {reqNo}));
        return msg;
      }
      if ((isFrm2 || isFrm3) && st && (isToksp)) {
        // 【画面】.「販売期間」入力時のみ入力可能。
        // E20397 「販売期間」に入力がある場合のみ、入力できます。 0 E
        if ((StringUtils.isEmpty(hbStDt) && StringUtils.isEmpty(hbEdDt)) && !StringUtils.isEmpty(yoriFlg) && yoriFlg.equals("1")) {
          msg.add(mu.getDbMessageObj("E20397", new String[] {reqNo}));
          return msg;
        }
      }
    }

    // *** 一個売り部分 ***
    // 135.総売価A
    // ① 入力範囲：1～999,999。
    // ② 商品マスタ.レギュラー情報_売価より税込み計算>=【画面】.一個売り部分の「総売価A」(『特売共通仕様書 総額売価計算方法』部分参照。)
    // 但し、【画面】.レギュラー行の「A総売価」=0orNULLの場合と催し区分=0の場合、あるいは精肉、鮮魚の場合はは本チェックを行わない。
    // ③ 新規の場合、初期値はNULLに設置する。
    String koaBaikaan = "";
    String kobBaikaan = "";
    String kocBaikaan = "";

    if (isToktg) {
      koaBaikaan = data.optString(TOKTG_SHNLayout.KO_A_BAIKAAN.getId());
      kobBaikaan = data.optString(TOKTG_SHNLayout.KO_B_BAIKAAN.getId());
      kocBaikaan = data.optString(TOKTG_SHNLayout.KO_C_BAIKAAN.getId());
    } else {
      koaBaikaan = data.optString(TOKSP_SHNLayout.KO_A_BAIKAAN.getId());
      kobBaikaan = data.optString(TOKSP_SHNLayout.KO_B_BAIKAAN.getId());
      kocBaikaan = data.optString(TOKSP_SHNLayout.KO_C_BAIKAAN.getId());
    }

    int koaBaikaani = 0;
    int kobBaikaani = 0;
    int kocBaikaani = 0;
    if (!StringUtils.isEmpty(koaBaikaan)) {
      koaBaikaani = Integer.valueOf(koaBaikaan);
    }
    if (!StringUtils.isEmpty(kobBaikaan)) {
      kobBaikaani = Integer.valueOf(kobBaikaan);
    }
    if (!StringUtils.isEmpty(kocBaikaan)) {
      kocBaikaani = Integer.valueOf(kocBaikaan);
    }

    // ②E20489 一個売り部分の「総売価A」 ≦ レギュラー行の「A売価」の条件で入力してください。 0 E
    if (!StringUtils.isEmpty(koaBaikaan) && !(StringUtils.isEmpty(aSouBaika) || szMoyskbn.equals("0") || isFrm2 || isFrm3)) {

      int baika = Integer.valueOf(aSouBaika);
      int koaBaikaanI = koaBaikaani;
      if (!(baika >= koaBaikaanI)) {
        msg.add(mu.getDbMessageObj("E20489", new String[] {reqNo}));
        return msg;
      }
    }
    if ((isFrm1) && st) {
      // 16_1【画面】.バンドル1部分の「総売価1A」の入力がある場合のみ必須。入力がなかったら入力不可。
      String baika = bd1aBaikaan;
      // E20410 バンドル1部分の「総売価1A」の入力がある場合のみ総売価A入力可。
      // E20393 バンドル1部分の「総売価1A」の入力がある場合は、一個売り部分の総売価Aが必須です。 0 E
      if (StringUtils.isEmpty(baika) && !StringUtils.isEmpty(koaBaikaan)) {
        msg.add(mu.getDbMessageObj("E20410", new String[] {reqNo}));
        return msg;
      } else if (!StringUtils.isEmpty(baika) && StringUtils.isEmpty(koaBaikaan)) {
        msg.add(mu.getDbMessageObj("E20393", new String[] {reqNo}));
        return msg;
      }
    }

    if ((isFrm2 || isFrm3) && st) {
      // 16_2,16_3 ①【画面】.「PLU商品・定貫商品」選択時で、【画面】.バンドル1部分の「総売価1A」の入力がある場合のみ必須。② 上記以外入力不可。
      String baika = bd1aBaikaan;
      if (tkanPluKbn.equals("1") && !StringUtils.isEmpty(baika)) {
        // E20332 「PLU商品・定貫商品」選択時で、バンドル1部分の「総売価1A」の入力がある場合のみ、総売価Aが必須です。 0 E
        if (StringUtils.isEmpty(koaBaikaan)) {
          msg.add(mu.getDbMessageObj("E20332", new String[] {reqNo}));
          return msg;
        }
      } else {
        // E20326 「PLU商品・定貫商品」選択時で、バンドル1部分の「総売価1A」の入力時以外、総売価Aは入力できません。 0 E
        if (!StringUtils.isEmpty(koaBaikaan)) {
          msg.add(mu.getDbMessageObj("E20326", new String[] {reqNo}));
          return msg;
        }
      }
      if (isToksp) {
        // 【画面】.「販売期間」入力時のみ入力可能。
        // E20397 「販売期間」に入力がある場合のみ、入力できます。 0 E
        if ((StringUtils.isEmpty(hbStDt) && StringUtils.isEmpty(hbEdDt)) && !StringUtils.isEmpty(koaBaikaan)) {
          msg.add(mu.getDbMessageObj("E20397", new String[] {reqNo}));
          return msg;
        }
      }
    }

    // 136.総売価B
    // ① 入力範囲：1～999,999。
    // ② 新規の場合、初期値はNULLに設置する。
    if ((isFrm1) && st) {
      // 【画面】.バンドル1部分の「総売価1B」の入力がある場合のみ必須。入力がなかったら入力不可。
      String baika = bd1bBaikaan;
      // E20411 バンドル1部分の「総売価1B」の入力がある場合のみ総売価B入力可。
      // E20394 バンドル1部分の「総売価1B」の入力がある場合は、一個売り部分の総売価Bが必須です。 0 E
      if (StringUtils.isEmpty(baika) && !StringUtils.isEmpty(kobBaikaan)) {
        msg.add(mu.getDbMessageObj("E20411", new String[] {reqNo}));
        return msg;
      } else if (!StringUtils.isEmpty(baika) && StringUtils.isEmpty(kobBaikaan)) {
        msg.add(mu.getDbMessageObj("E20394", new String[] {reqNo}));
        return msg;
      }
    }
    if ((isFrm2 || isFrm3) && st) {
      // 16_2,16_3 ① 【画面】.「PLU商品・定貫商品」選択時で、【画面】.バンドル1部分の「総売価１B」の入力がある場合のみ必須。② 上記以外入力不可。
      String baika = bd1bBaikaan;
      if (tkanPluKbn.equals("1") && !StringUtils.isEmpty(baika)) {
        // E20333 「PLU商品・定貫商品」選択時で、バンドル1部分の「総売価１B」の入力がある場合のみ、総売価Bが必須です。 0 E
        if (StringUtils.isEmpty(kobBaikaan)) {
          msg.add(mu.getDbMessageObj("E20333", new String[] {reqNo}));
          return msg;
        }
      } else {
        // E20328 「PLU商品・定貫商品」選択時で、バンドル1部分の「総売価1B」の入力以外、総売価Bは入力できません。 0 E
        if (!StringUtils.isEmpty(kobBaikaan)) {
          msg.add(mu.getDbMessageObj("E20328", new String[] {reqNo}));
          return msg;
        }
      }
      if (isToksp) {
        // 【画面】.「販売期間」入力時のみ入力可能。
        // E20397 「販売期間」に入力がある場合のみ、入力できます。 0 E
        if ((StringUtils.isEmpty(hbStDt) && StringUtils.isEmpty(hbEdDt)) && !StringUtils.isEmpty(kobBaikaan)) {
          msg.add(mu.getDbMessageObj("E20397", new String[] {reqNo}));
          return msg;
        }
      }
    }
    // 137.総売価C
    // ① 入力範囲：1～999,999。
    // ② 新規の場合、初期値はNULLに設置する。
    if ((isFrm1) && st) {
      // 【画面】.バンドル1部分の「総売価1C」の入力がある場合のみ必須。入力がなかったら入力不可。
      String baika = bd1cBaikaan;
      // E20412 バンドル1部分の「総売価1C」の入力がある場合のみ総売価C入力可。
      // E20395 バンドル1部分の「総売価1C」の入力がある場合は、一個売り部分の総売価Cが必須です。
      if (StringUtils.isEmpty(baika) && !StringUtils.isEmpty(kocBaikaan)) {
        msg.add(mu.getDbMessageObj("E20412", new String[] {reqNo}));
        return msg;
      } else if (!StringUtils.isEmpty(baika) && StringUtils.isEmpty(kocBaikaan)) {
        msg.add(mu.getDbMessageObj("E20395", new String[] {reqNo}));
        return msg;
      }
    }
    if ((isFrm2 || isFrm3) && st) {
      // 16_2,16_3 ① 【画面】.「PLU商品・定貫商品」選択時で、【画面】.バンドル1部分の「総売価１C」の入力がある場合のみ必須。② 上記以外入力不可。
      String baika = bd1cBaikaan;
      if (tkanPluKbn.equals("1") && !StringUtils.isEmpty(baika)) {
        // E20334 「PLU商品・定貫商品」選択時で、バンドル1部分の「総売価１C」の入力がある場合のみ、総売価Cが必須です。 0 E
        if (StringUtils.isEmpty(kocBaikaan)) {
          msg.add(mu.getDbMessageObj("E20334", new String[] {reqNo}));
          return msg;
        }
      } else {
        // E20330 「PLU商品・定貫商品」選択時で、バンドル1部分の「総売価1C」の入力以外、総売価Cは入力できません。 0 E
        if (!StringUtils.isEmpty(kocBaikaan)) {
          msg.add(mu.getDbMessageObj("E20330", new String[] {reqNo}));
          return msg;
        }
      }
      if (isToksp) {
        // 【画面】.「販売期間」入力時のみ入力可能。
        // E20397 「販売期間」に入力がある場合のみ、入力できます。 0 E
        if ((StringUtils.isEmpty(hbStDt) && StringUtils.isEmpty(hbEdDt)) && !StringUtils.isEmpty(kocBaikaan)) {
          msg.add(mu.getDbMessageObj("E20397", new String[] {reqNo}));
          return msg;
        }
      }
    }

    // *** バンドル1部分 ***
    // 139.点数1
    // ① 入力範囲：1～999。
    // ② 新規の場合、初期値はNULLに設置する。

    String bd1tensu = "";
    String bd2tensu = "";

    if (isToktg) {
      bd1tensu = data.optString(TOKTG_SHNLayout.BD1_TENSU.getId());
      bd2tensu = data.optString(TOKTG_SHNLayout.BD2_TENSU.getId());
    } else {
      bd1tensu = data.optString(TOKSP_SHNLayout.BD1_TENSU.getId());
      bd2tensu = data.optString(TOKSP_SHNLayout.BD2_TENSU.getId());
    }

    int bd1tensui = 0;
    int bd2tensui = 0;
    if (!StringUtils.isEmpty(bd1tensu)) {
      bd1tensui = Integer.valueOf(bd1tensu);
    }
    if (!StringUtils.isEmpty(bd2tensu)) {
      bd2tensui = Integer.valueOf(bd2tensu);
    }

    // 16_1【画面】.バンドル1部分の「総売価1A」の入力がある場合2以上が可。
    // 16_4【画面】.バンドル1部分の「総売価1A」の入力がある場合2以上が可。
    if ((isFrm1 || isFrm4) && st) {
      String baika = bd1aBaikaan;
      int nVal = bd1tensui;

      // 16_1【画面】.バンドル1部分の「総売価1A」の入力がある場合のみ必須。入力がなかったら入力不可。
      if (StringUtils.isEmpty(baika) && !StringUtils.isEmpty(bd1tensu)) {
        msg.add(mu.getDbMessageObj("E20413", new String[] {reqNo}));
        return msg;
      }

      // E20419 バンドル1部分の「総売価1A」の入力がある場合、点数1に2以上を入力してください。 0 E
      if (!StringUtils.isEmpty(bd1tensu) && !StringUtils.isEmpty(baika) && !(nVal * 1 >= 2)) {
        msg.add(mu.getDbMessageObj("E20419", new String[] {reqNo}));
        return msg;
      }
    }

    if ((isFrm2 || isFrm3) && st) {
      // 16_2,16_3 【画面】.「PLU商品・定貫商品」選択時で、【画面】.バンドル1部分の「総売価1A」の入力がある場合2以上可。
      String baika = bd1aBaikaan;
      int nVal = bd1tensui;

      if (tkanPluKbn.equals("1") && !StringUtils.isEmpty(bd1tensu)) {

        if (StringUtils.isEmpty(baika)) {
          msg.add(mu.getDbMessageObj("E20413", new String[] {reqNo}));
          return msg;
        }

        // E20336 「PLU商品・定貫商品」選択時で、バンドル1部分の「総売価1A」の入力がある場合点数1が2以上可。 0 E
        if (!(nVal * 1 >= 2)) {
          msg.add(mu.getDbMessageObj("E20336", new String[] {reqNo}));
          return msg;
        }
      }
      if (isToksp) {
        // 【画面】.「販売期間」入力時のみ入力可能。
        // E20397 「販売期間」に入力がある場合のみ、入力できます。 0 E
        if ((StringUtils.isEmpty(hbStDt) && StringUtils.isEmpty(hbEdDt)) && !StringUtils.isEmpty(bd1tensu)) {
          msg.add(mu.getDbMessageObj("E20397", new String[] {reqNo}));
          return msg;
        }
      }
    }

    // 140.総売価1A
    // ① 入力範囲：1～999,999。
    // ② 新規の場合、初期値はNULLに設置する。
    // ③ 1個売り部分「総売価A」 ＜ バンドル1部分「総売価1A」
    // ④ 1個売り部分「総売価A」 ≧ バンドル1部分「総売価1A」÷点数1
    if (!StringUtils.isEmpty(bd1aBaikaan)) {
      int ko_baika = koaBaikaani;
      if (isFrm4) {
        ko_baika = a_baikaam;
      }
      int nVal = bd1aBaikaani;
      String tensu = bd1tensu;

      if (StringUtils.isEmpty(tensu)) {
        tensu = "1";
      }

      if (!StringUtils.isEmpty(koaBaikaan)) {
        // ③E20584 バンドル総売価１Ａに1個売り総売価Ａ以下の値が入力されています。 0 E
        if (!(ko_baika < nVal)) {
          msg.add(mu.getDbMessageObj("E20584", new String[] {reqNo}));
          return msg;
        }
        // ④E20585 バンドル総売価１Ａの平均売価（＝円/個）が1個売り総売価Ａより大きくなっています。 0 E
        if ((!(ko_baika >= Integer.valueOf(calcAvg(bd1aBaikaan, tensu))))) {
          msg.add(mu.getDbMessageObj("E20585", new String[] {reqNo}));
          return msg;
        }
      }

      if ((isFrm1 || isFrm4) && st) {
        // 16_1,16_4【画面】.「販売期間」に入力がある場合のみ、入力可。
        // E20397 「販売期間」に入力がある場合のみ、入力できます。 0 E
        if ((StringUtils.isEmpty(hbStDt) && StringUtils.isEmpty(hbEdDt)) && !StringUtils.isEmpty(bd1aBaikaan)) {
          msg.add(mu.getDbMessageObj("E20397", new String[] {reqNo}));
          return msg;
        }
      }

      if ((isFrm2 || isFrm3) && st) {
        // 16_2,16_3【画面】.「販売期間」入力時のみで【画面】.「PLU商品・定貫商品」選択時入力可。
        if (!(!StringUtils.isEmpty(hbStDt) && !StringUtils.isEmpty(hbEdDt) && tkanPluKbn.equals("1"))) {
          // E20337 「販売期間」入力、かつ「PLU商品・定貫商品」選択時総売価1Aが入力できます。 0 E
          if (!StringUtils.isEmpty(bd1aBaikaan)) {
            msg.add(mu.getDbMessageObj("E20337", new String[] {reqNo}));
            return msg;
          }
        }
      }
    }
    // 141.総売価1B
    // ① 入力範囲：1～999,999。
    // ② 新規の場合、初期値はNULLに設置する。
    // ③ 1個売り部分「総売価B」 ＜ バンドル1部分「総売価1B」
    // ④ 1個売り部分「総売価B」 ≧ バンドル1部分「総売価1B」÷点数1
    int ko_b_baika = kobBaikaani;
    if (isFrm4) {
      ko_b_baika = b_baikaam;
    }

    int nBVal = bd1bBaikaani;
    // ③E20588 バンドル総売価１Ｂに1個売り総売価Ｂ以下の値が入力されています。 0 E
    if (!StringUtils.isEmpty(kobBaikaan) && !StringUtils.isEmpty(bd1bBaikaan)) {
      if (ko_b_baika >= nBVal) {
        msg.add(mu.getDbMessageObj("E20588", new String[] {reqNo}));
        return msg;
      }
    }
    // ④E20589 バンドル総売価１Ｂの平均売価（＝円/個）が1個売り総売価Ｂより大きくなっています。 0 E
    if (!StringUtils.isEmpty(kobBaikaan) && !StringUtils.isEmpty(bd1bBaikaan)) {

      String tensu = bd1tensu;

      if (StringUtils.isEmpty(tensu)) {
        tensu = "1";
      }

      if ((!(ko_b_baika >= Integer.valueOf(calcAvg(bd1bBaikaan, tensu))))) {
        msg.add(mu.getDbMessageObj("E20589", new String[] {reqNo}));
        return msg;
      }
    }

    if ((isFrm1 || isFrm2 || isFrm3 || isFrm4) && st) {

      // 16_1,16_2,16_3,16_4【画面】.バンドル1部分の「総売価1A」の入力があり、【画面】.B部分の「B総売価」に入力がある場合のみ必須。それ以外は入力不可。
      if (!StringUtils.isEmpty(bd1aBaikaan) && !StringUtils.isEmpty(bBaikaam)) {
        // E20396 バンドル1部分の「総売価1A」の入力があり、B部分の「B総売価」に入力がある場合は総売価1Bが必須。
        if (StringUtils.isEmpty(bd1bBaikaan)) {
          msg.add(mu.getDbMessageObj("E20396", new String[] {reqNo}));
          return msg;
        }
      } else {
        // E20338 バンドル1部分の「総売価1A」の入力があり、B部分の「B総売価」に入力時以外は総売価1Bは入力できません。 0 E
        if (!StringUtils.isEmpty(bd1bBaikaan)) {
          msg.add(mu.getDbMessageObj("E20338", new String[] {reqNo}));
          return msg;
        }
      }
      if ((isFrm2 || isFrm3) && isToksp) {

        // 【画面】.「販売期間」入力時のみ入力可能。
        // E20397 「販売期間」に入力がある場合のみ、入力できます。 0 E
        if ((StringUtils.isEmpty(hbStDt) && StringUtils.isEmpty(hbEdDt)) && !StringUtils.isEmpty(bd1bBaikaan)) {
          msg.add(mu.getDbMessageObj("E20397", new String[] {reqNo}));
          return msg;
        }
      }
    }

    // 142.総売価1C
    // ① 入力範囲：1～999,999。
    // ② 新規の場合、初期値はNULLに設置する。
    // ③ 1個売り部分「総売価C」 ＜ バンドル1部分「総売価1C」
    // ④ 1個売り部分「総売価C」 ≧ バンドル1部分「総売価1C」÷点数1
    int ko_c_baika = kocBaikaani;
    if (isFrm4) {
      ko_c_baika = c_baikaam;
    }

    int nCVal = bd1cBaikaani;
    // ③E20592 バンドル総売価１Ｃに1個売り総売価Ｃ以下の値が入力されています。 0 E
    if (!StringUtils.isEmpty(bd1cBaikaan) && !StringUtils.isEmpty(kocBaikaan)) {
      if (!(ko_c_baika < nCVal)) {
        msg.add(mu.getDbMessageObj("E20592", new String[] {reqNo}));
        return msg;
      }
    }

    String tensu = bd1tensu;

    if (StringUtils.isEmpty(tensu)) {
      tensu = "1";
    }

    // ④E20593 バンドル総売価１Ｃの平均売価（＝円/個）が1個売り総売価Ｃより大きくなっています。 0 E
    if (!StringUtils.isEmpty(bd1cBaikaan) && (!(ko_c_baika >= Integer.valueOf(calcAvg(bd1cBaikaan, tensu))))) {
      msg.add(mu.getDbMessageObj("E20593", new String[] {reqNo}));
      return msg;
    }

    if ((isFrm1 || isFrm2 || isFrm3 || isFrm4) && st) {

      // 16_1,16_2,16_3,16_4【画面】.バンドル1部分の「総売価1A」の入力があり、【画面】.B部分の「B総売価」に入力がある場合のみ必須。それ以外は入力不可。
      if (!StringUtils.isEmpty(bd1aBaikaan) && !StringUtils.isEmpty(cBaikaam)) {
        // E20399 バンドル1部分の「総売価1B」の入力があり、C部分の「C総売価」に入力がある場合は総売価1Cが必須。 0 E
        if (StringUtils.isEmpty(bd1cBaikaan)) {
          msg.add(mu.getDbMessageObj("E20399", new String[] {reqNo}));
          return msg;
        }
      } else {
        // E20340 バンドル1部分の「総売価1B」の入力があり、C部分の「C総売価」に入力時以外は総売価1Cは入力できません｡
        if (!StringUtils.isEmpty(bd1cBaikaan)) {
          msg.add(mu.getDbMessageObj("E20340", new String[] {reqNo}));
          return msg;
        }
      }
      if ((isFrm2 || isFrm3) && isToksp) {

        // 【画面】.「販売期間」入力時のみ入力可能。
        // E20397 「販売期間」に入力がある場合のみ、入力できます。 0 E
        if ((StringUtils.isEmpty(hbStDt) && StringUtils.isEmpty(hbEdDt)) && !StringUtils.isEmpty(bd1cBaikaan)) {
          msg.add(mu.getDbMessageObj("E20397", new String[] {reqNo}));
          return msg;
        }
      }
    }

    // *** バンドル2部分 ***
    // 144.点数2
    // ① 入力範囲：1～999。
    // ② 新規の場合、初期値はNULLに設置する。
    if ((isFrm1 || isFrm4) && st) {
      // 16_1,16_4【画面】.バンドル2部分の「総売価２A」の入力がある場合、 【画面】.バンドル1部分の「点数1」より大きい。
      String baika = bd2aBaikaan;
      // E20342 バンドル2部分の「総売価２A」の入力がある場合、 バンドル2部分の「点数2」がバンドル1部分の「点数1」より大きい。
      if (!StringUtils.isEmpty(bd2aBaikaan) && !StringUtils.isEmpty(baika) && bd1tensui >= bd2tensui) {
        msg.add(mu.getDbMessageObj("E20342", new String[] {reqNo}));
        return msg;
      }
    }
    if ((isFrm2 || isFrm3) && st) {
      // 16_2,16_3【画面】.「PLU商品・定貫商品」選択時で、【画面】.バンドル2部分の「総売価2A」の入力がある場合、 【画面】.バンドル1部分の「点数1」より大きい値入力可能。
      String baika = bd2aBaikaan;
      if (!(tkanPluKbn.equals("1") && !StringUtils.isEmpty(baika))) {
        // E20441 「PLU商品・定貫商品」選択時で、バンドル1部分の「総売価1B」の入力以外総売価Bが入力不可。 0 E
        if (!StringUtils.isEmpty(bd2tensu)) {
          msg.add(mu.getDbMessageObj("E20441", new String[] {reqNo}));
          return msg;
        }
      }

      if (isToksp) {
        // 【画面】.「販売期間」入力時のみ入力可能。
        if ((StringUtils.isEmpty(hbStDt) && StringUtils.isEmpty(hbEdDt)) && !StringUtils.isEmpty(bd2tensu)) {
          msg.add(mu.getDbMessageObj("E20397", new String[] {reqNo}));
          return msg;
        }
      }
    }

    // 145.総売価２A
    // ① 入力範囲：1～999,999。
    // ② 新規の場合、初期値はNULLに設置する。
    // ③ バンドル1部分「総売価1A」 ＜ バンドル2部分「総売価2A」
    // ④ バンドル1部分「総売価1A」 ≧ バンドル2部分「総売価2A」÷点数2q
    if (!StringUtils.isEmpty(bd2aBaikaan)) {
      int baika = bd1aBaikaani;
      int nVal = Integer.valueOf(bd2aBaikaan);
      // ③E20586 バンドル総売価２Ａにバンドル総売価１Ａ以下の値が入力されています。 0 E
      if (!(baika < nVal)) {
        msg.add(mu.getDbMessageObj("E20586", new String[] {reqNo}));
        return msg;

      }

      String tensu1 = bd1tensu;
      String tensu2 = bd2tensu;

      if (StringUtils.isEmpty(tensu1)) {
        tensu1 = "1";
      }

      if (StringUtils.isEmpty(tensu2)) {
        tensu2 = "1";
      }

      // ④E20587 バンドル総売価２Ａの平均売価≦バンドル総売価１Ａの平均売価の範囲で入力してください。 0 E
      if (!StringUtils.isEmpty(bd1aBaikaan) && !(Integer.valueOf(calcAvg(bd1aBaikaan, tensu1)) >= Integer.valueOf(calcAvg(bd2aBaikaan, tensu2)))) {
        msg.add(mu.getDbMessageObj("E20587", new String[] {reqNo}));
        return msg;
      }
      if ((isFrm1 || isFrm2 || isFrm3 || isFrm4) && st) {
        // 16_1,16_2,16_3,16_4【画面】.バンドル1部分の「総売価1A」に入力がある場合のみ入力可。
        // E20410 バンドル1部分の「総売価1A」の入力がある場合のみ総売価A入力可。 0 E
        if (StringUtils.isEmpty(bd1aBaikaan) && !StringUtils.isEmpty(bd2aBaikaan)) {
          msg.add(mu.getDbMessageObj("E20410", new String[] {reqNo}));
          return msg;
        }
        if ((isFrm2 || isFrm3) && isToksp) {

          // 【画面】.「販売期間」入力時のみ入力可能。
          // E20397 「販売期間」に入力がある場合のみ、入力できます。 0 E
          if ((StringUtils.isEmpty(hbStDt) && StringUtils.isEmpty(hbEdDt)) && !StringUtils.isEmpty(bd2aBaikaan)) {
            msg.add(mu.getDbMessageObj("E20397", new String[] {reqNo}));
            return msg;
          }
        }
      }
    }

    // 146.総売価２B
    // ① 入力範囲：1～999,999。
    // ② 新規の場合、初期値はNULLに設置する。
    // ③ バンドル1部分「総売価1B」 ＜ バンドル2部分「総売価2B」
    // ④ バンドル1部分「総売価1B」 ≧ バンドル2部分「総売価2B」÷点数2
    int baika = bd1bBaikaani;
    int nVal = 0;

    if (!StringUtils.isEmpty(bd2bBaikaan)) {
      nVal = Integer.valueOf(bd2bBaikaan);
    }

    // ③ E20590 バンドル総売価２Ｂにバンドル総売価１Ｂ以下の値が入力されています。 0 E
    if (!StringUtils.isEmpty(bd2bBaikaan) && !(baika < nVal)) {
      msg.add(mu.getDbMessageObj("E20590", new String[] {reqNo}));
      return msg;
    }
    // ④ E20591 バンドル総売価２Ｂの平均売価≦バンドル総売価１Ｂの平均売価の範囲で入力してください。 0 E
    String tensu1 = bd1tensu;
    String tensu2 = bd2tensu;

    if (StringUtils.isEmpty(tensu1)) {
      tensu1 = "1";
    }

    if (StringUtils.isEmpty(tensu2)) {
      tensu2 = "1";
    }

    if (!StringUtils.isEmpty(bd2bBaikaan) && !StringUtils.isEmpty(bd1bBaikaan) && !(Integer.valueOf(calcAvg(bd1bBaikaan, tensu1)) >= Integer.valueOf(calcAvg(bd2bBaikaan, tensu2)))) {
      msg.add(mu.getDbMessageObj("E20591", new String[] {reqNo}));
      return msg;
    }

    if ((isFrm1 || isFrm2 || isFrm3 || isFrm4) && st) {

      // 16_1,16_2,16_3,16_4【画面】.バンドル2部分の「総売価2A」の入力があり、【画面】.B部分の「B総売価」に入力がある場合のみ必須。それ以外は入力不可。
      if (!StringUtils.isEmpty(bd2aBaikaan) && !StringUtils.isEmpty(bBaikaam)) {
        // E20402 バンドル2部分の「総売価2A」の入力があり、B部分の「B総売価」に入力が ある場合は総売価2Bが必須。 0 E
        if (StringUtils.isEmpty(bd2bBaikaan)) {
          msg.add(mu.getDbMessageObj("E20402", new String[] {reqNo}));
          return msg;
        }
      } else {
        // E20346 バンドル2部分の「総売価2A」の入力があり、B部分の「B総売価」に入力時以外は総売価２Bは入力できません。 0 E
        if (!StringUtils.isEmpty(bd2bBaikaan)) {
          msg.add(mu.getDbMessageObj("E20346", new String[] {reqNo}));
          return msg;
        }
      }

      if ((isFrm2 || isFrm3) && isToksp) {
        // 【画面】.「販売期間」入力時のみ入力可能。
        if ((StringUtils.isEmpty(hbStDt) && StringUtils.isEmpty(hbEdDt)) && !StringUtils.isEmpty(bd2bBaikaan)) {
          msg.add(mu.getDbMessageObj("E20397", new String[] {reqNo}));
          return msg;
        }
      }
    }

    // 147.総売価２C
    // ① 入力範囲：1～999,999。
    // ② 新規の場合、初期値はNULLに設置する。
    // ③ バンドル1部分「総売価1C」 ＜ バンドル2部分「総売価2C」
    // ④ バンドル1部分「総売価1C」 ≧ バンドル2部分「総売価2C」÷点数2
    baika = bd1cBaikaani;
    nVal = 0;

    if (!StringUtils.isEmpty(bd2cBaikaan)) {
      nVal = Integer.valueOf(bd2cBaikaan);
    }

    // ③ E20594 バンドル総売価２Ｃにバンドル総売価１Ｃ以下の値が入力されています。 0 E
    if (!StringUtils.isEmpty(bd2cBaikaan) && !(baika < nVal)) {
      msg.add(mu.getDbMessageObj("E20594", new String[] {reqNo}));
      return msg;
    }
    // ④ E20595 バンドル総売価２Ｃの平均売価≦バンドル総売価１Ｃの平均売価の範囲で入力してください。 0 E
    tensu1 = bd1tensu;
    tensu2 = bd2tensu;

    if (StringUtils.isEmpty(tensu1)) {
      tensu1 = "1";
    }

    if (StringUtils.isEmpty(tensu2)) {
      tensu2 = "1";
    }

    if (!StringUtils.isEmpty(bd2cBaikaan) && !StringUtils.isEmpty(bd1cBaikaan) && !(Integer.valueOf(calcAvg(bd1cBaikaan, tensu1)) >= Integer.valueOf(calcAvg(bd2cBaikaan, tensu2)))) {
      msg.add(mu.getDbMessageObj("E20595", new String[] {reqNo}));
      return msg;
    }

    if ((isFrm1 || isFrm2 || isFrm3 || isFrm4) && st) {
      // 16_1,16_2,16_3,16_4【画面】.バンドル2部分の「総売価2B」の入力があり、【画面】.C部分の「C総売価」に入力がある場合のみ必須。それ以外は入力不可。
      if (!StringUtils.isEmpty(bd2bBaikaan) && !StringUtils.isEmpty(cBaikaam)) {
        // E20403 バンドル2部分の「総売価2B」の入力があり、C部分の「C総売価」に入力が ある場合は総売価2Cが必須。 0 E
        if (StringUtils.isEmpty(bd2cBaikaan)) {
          msg.add(mu.getDbMessageObj("E20403", new String[] {reqNo}));
          return msg;
        }
      } else {
        // E20507 バンドル2部分の「総売価2B」の入力があり、C部分の「C総売価」に入力がある場合以外は、 総売価２Cは入力できません。 0 E
        if (!StringUtils.isEmpty(bd2cBaikaan)) {
          msg.add(mu.getDbMessageObj("E20507", new String[] {reqNo}));
          return msg;
        }
      }

      if ((isFrm2 || isFrm3) && isToksp) {
        // 【画面】.「販売期間」入力時のみ入力可能。
        // E20397 「販売期間」に入力がある場合のみ、入力できます。 0 E
        if ((StringUtils.isEmpty(hbStDt) && StringUtils.isEmpty(hbEdDt)) && !StringUtils.isEmpty(bd2cBaikaan)) {
          msg.add(mu.getDbMessageObj("E20397", new String[] {reqNo}));
          return msg;
        }
      }
    }

    // *** 100g相当部分 ***
    // 149.A総売価
    // ① 入力範囲：1～999,999。
    // ② 新規の場合、初期値はNULLに設置する。
    // ③ 【画面】.100g相当部分の「A総売価」>=【画面】.100g相当部分の「B総売価」>=【画面】.100g相当部分の「C総売価」
    // ④ 【画面】.100g相当部分の「A総売価」=【画面】.100g相当部分の「B総売価」=【画面】.100g相当部分の「A総売価」は不可
    // ⑤ 【画面】.不定貫商品が選択されている場合に入力されていたらエラー

    String aBaikaam100g = "";
    String bBaikaam100g = "";
    String cBaikaam100g = "";

    if (isToktg) {
      aBaikaam100g = data.optString(TOKTG_SHNLayout.A_BAIKAAM_100G.getId());
      bBaikaam100g = data.optString(TOKTG_SHNLayout.B_BAIKAAM_100G.getId());
      cBaikaam100g = data.optString(TOKTG_SHNLayout.C_BAIKAAM_100G.getId());
    } else {
      aBaikaam100g = data.optString(TOKSP_SHNLayout.A_BAIKAAM_100G.getId());
      bBaikaam100g = data.optString(TOKSP_SHNLayout.B_BAIKAAM_100G.getId());
      cBaikaam100g = data.optString(TOKSP_SHNLayout.C_BAIKAAM_100G.getId());
    }

    int a_baikaam100g = 0;
    int b_baikaam100g = 0;
    int c_baikaam100g = 0;

    if (!StringUtils.isEmpty(aBaikaam100g)) {
      a_baikaam100g = Integer.valueOf(aBaikaam100g);
    }
    if (!StringUtils.isEmpty(bBaikaam100g)) {
      b_baikaam100g = Integer.valueOf(bBaikaam100g);
    }
    if (!StringUtils.isEmpty(cBaikaam100g)) {
      c_baikaam100g = Integer.valueOf(cBaikaam100g);
    }

    // ③E20562 販売情報でA総売価行の「100g相当」 ≧ B総売価行の「100g相当」 ≧ C総売価行の「100g相当」 の条件で入力してください。 0 E
    if (!(a_baikaam100g >= b_baikaam100g && b_baikaam100g >= c_baikaam100g)) {
      msg.add(mu.getDbMessageObj("E20562", new String[] {reqNo}));
      return msg;
    }
    // ④E20563 販売情報でA総売価行の「100g相当」 = B総売価行の「100g相当」 = C総売価行の「100g相当」 は入力できません。 0 E
    if (!(StringUtils.isEmpty(aBaikaam100g) && StringUtils.isEmpty(bBaikaam100g) && StringUtils.isEmpty(cBaikaam100g)) && a_baikaam100g == b_baikaam100g && b_baikaam100g == c_baikaam100g) {
      msg.add(mu.getDbMessageObj("E20563", new String[] {reqNo}));
      return msg;
    }
    // ⑤E20600 「不定貫商品」選択時にA総売価「100g相当」は入力できません。 0 E
    if (!StringUtils.isEmpty(aBaikaam100g) && tkanPluKbn.equals("2")) {
      msg.add(mu.getDbMessageObj("E20600", new String[] {reqNo}));
      return msg;
    }

    // 150.B総売価
    // ① 入力範囲：1～999,999。
    // ② 新規の場合、初期値はNULLに設置する。
    // ③ 【画面】.100g相当部分の「A総売価」>=【画面】.100g相当部分の「B総売価」>=【画面】.100g相当部分の「C総売価」
    // ④ 【画面】.100g相当部分の「A総売価」=【画面】.100g相当部分の「B総売価」=【画面】.100g相当部分の「A総売価」は不可
    // ⑤ 【画面】.不定貫商品が選択されている場合に入力されていたらエラー
    if (!StringUtils.isEmpty(bBaikaam100g)) {
      if (isFrm2 && st) {
        // 16_2 定貫時：【画面】.B部分の「B総売価」に入力がある場合のみ入力可能。不定貫時：【画面】.B総売価行の「100g総売価」に入力がある場合のみ入力可能。
        // E20447 B部分の「B総売価」に入力がある場合のみB総売価が入力可能。
        if (tkanPluKbn.equals("1") && StringUtils.isEmpty(bBaikaam)) {
          msg.add(mu.getDbMessageObj("E20447", new String[] {reqNo}));
          return msg;
        }
        // E20505 B総売価行の「100g総売価」に入力がある場合のみB総売価が入力できます。 0 E
        if (tkanPluKbn.equals("2") && StringUtils.isEmpty(bBaikaam)) {
          msg.add(mu.getDbMessageObj("E20505", new String[] {reqNo}));
          return msg;
        }
      } else {
        // ⑤E20601 「不定貫商品」選択時にB総売価「100g相当」は入力できません。 0 E
        if (tkanPluKbn.equals("2")) {
          msg.add(mu.getDbMessageObj("E20601", new String[] {reqNo}));
          return msg;
        }
      }
    }
    // 151.C総売価
    // ① 入力範囲：1～999,999。
    // ② 新規の場合、初期値はNULLに設置する。
    // ③ 【画面】.100g相当部分の「A総売価」>=【画面】.100g相当部分の「B総売価」>=【画面】.100g相当部分の「C総売価」
    // ④ 【画面】.100g相当部分の「A総売価」=【画面】.100g相当部分の「B総売価」=【画面】.100g相当部分の「A総売価」は不可
    // ⑤ 【画面】.100g相当部分の「B総売価」が入力されていないと入力不可
    // ⑥ 【画面】.不定貫商品が選択されている場合に入力されていたらエラー
    if (!StringUtils.isEmpty(cBaikaam100g)) {
      if (isFrm2 && st) {
        // 16_2 定貫時：【画面】.C部分の「C総売価」に入力がある場合のみ入力可能。不定貫時：【画面】.C総売価行の「100g総売価」に入力がある場合のみ入力可能。
        // E20448 C部分の「C総売価」に入力がある場合のみC総売価が入力可能。 0 E
        if (tkanPluKbn.equals("1") && StringUtils.isEmpty(cBaikaam)) {
          msg.add(mu.getDbMessageObj("E20448", new String[] {reqNo}));
          return msg;
        }
        // E20506 C総売価行の「100g総売価」に入力がある場合のみC総売価が入力できます。 0 E
        if (tkanPluKbn.equals("2") && StringUtils.isEmpty(cBaikaam)) {
          msg.add(mu.getDbMessageObj("E20506", new String[] {reqNo}));
          return msg;
        }
      } else {
        // ⑤E20602 「不定貫商品」選択時にC総売価「100g相当」は入力できません。 0 E
        if (tkanPluKbn.equals("2")) {
          msg.add(mu.getDbMessageObj("E20602", new String[] {reqNo}));
          return msg;
        }
      }
    }

    // *** 生食・加熱/解凍/養殖 ***
    // 153.生食・加熱
    // ① 生食、加熱はどちらか一方にしかチェックをつけれない。 ※
    // ② 新規の場合、初期値は非チェックに設置する。
    // 154.解凍 新規の場合、初期値は非チェックに設置する。
    // 155.養殖 新規の場合、初期値は非チェックに設置する。

    // *** チラシ・ＰＯＰ情報 ***
    // 156.目玉情報
    // ① リスト内容：名称コード区分=10660で名称マスタから取得する。
    // ② 新規の場合、初期値は空白行に設置する。
    // 157.POPコード
    // ① 入力範囲：1～9999999999。
    // ② 新規の場合、初期値はNULLに設置する。
    // 158.POPサイズ 新規の場合、初期値はNULLに設置する。
    // 159.枚数
    // ① 入力範囲：1～99。
    // ② 新規の場合、初期値はNULLに設置する。
    // ③ 【画面】.「POPコード」または【画面】.「POPサイズ」が入力されたら１以上必須。それ以外入力不可。

    String popcd = "";
    String popsz = "";
    String popsu = "";

    if (isToktg) {
      popcd = data.optString(TOKTG_SHNLayout.POPCD.getId());
      popsz = data.optString(TOKTG_SHNLayout.POPSZ.getId());
      popsu = data.optString(TOKTG_SHNLayout.POPSU.getId());
    } else {
      popcd = data.optString(TOKSP_SHNLayout.POPCD.getId());
      popsz = data.optString(TOKSP_SHNLayout.POPSZ.getId());
      popsu = data.optString(TOKSP_SHNLayout.POPSU.getId());
    }
    // ③E20531 「POPコード」または「POPサイズ」が入力されたら、 「枚数」は1以上を入力してください。 0 E
    if (StringUtils.isEmpty(popsu) && (!StringUtils.isEmpty(popcd) || !StringUtils.isEmpty(popsz))) {
      msg.add(mu.getDbMessageObj("E20531", new String[] {reqNo}));
      return msg;
    }

    // 160.商品サイズ
    // ① 全角20文字。
    // ② 新規の場合、初期値はNULLに設置する。
    // 161.商品色
    // ① 全角10文字。
    // ② 新規の場合、初期値はNULLに設置する。
    // 162.その他日替コメント
    // ① 全角50文字。
    // ② 新規の場合、初期値はNULLに設置する。
    // 163.POPコメント
    // ① 全角50文字。
    // ② 新規の場合、初期値はNULLに設置する。


    // 【納入情報部分】
    // 170.事前打出(チェック) 新規の場合、初期値は非チェック状態に設置する。

    String juflg = "";

    if (isToktg) {
      juflg = data.optString(TOKTG_SHNLayout.JUFLG.getId());
    } else {
      juflg = data.optString(TOKSP_SHNLayout.JUFLG.getId());
    }

    if ((isFrm1 || isFrm2 || isFrm3) && st && (isToktg_h || isToksp)) {

      // 16_1,16_2,16_3
      // ① 【画面】.「納入期間」入力がある時のみ入力可。 TODO:
      // ② 商品マスタ.PC区分=1の場合、チェック不可。
      // E20350 PC商品の場合、事前打出(チェック)チェックはできません。 0 E
      if (pcKbn.equals("1") && !StringUtils.isEmpty(juflg) && juflg.equals("1")) {
        msg.add(mu.getDbMessageObj("E20350", new String[] {reqNo}));
        return msg;
      }
    }

    // 171.事前打出(日付)
    // ① 入力範囲：2003/01/01～9999/12/31
    // ② 【画面】.納入情報部分の「事前打出（チェック）」がＯＮの時のみ入力可。それ以外は入力不可。
    // ③ 処理日付が「事前打出（日付）」の前日まで修正可能。
    // ④ 処理日付けの翌日以降の日付を入力可能。
    // ⑤ 新規の場合、初期値はNULLに設置する。
    String juhtdt = "";

    if (isToktg) {
      juhtdt = data.optString(TOKTG_SHNLayout.JUHTDT.getId());
    } else {
      juhtdt = data.optString(TOKSP_SHNLayout.JUHTDT.getId());
    }

    int iJuhtdt = 0;
    if (!StringUtils.isEmpty(juhtdt)) {
      iJuhtdt = Integer.valueOf(juhtdt);
    }


    // ②E20352 納入情報部分の「事前打出（チェック）」がＯＮの時のみ事前打出(日付)が入力できます。 0 E
    if (juflg.equals("0") && !StringUtils.isEmpty(juhtdt)) {
      msg.add(mu.getDbMessageObj("E20352", new String[] {reqNo}));
      return msg;
    }
    // ③

    // ④E20530 事前打出日付は明日以降の日付しか入力できません。 0 E
    if (!StringUtils.isEmpty(juhtdt)) {
      // 変数を初期化
      sbSQL = new StringBuffer();
      iL = new ItemList();
      dbDatas = new JSONArray();
      sbSQL.append("select 1 from INAAD.SYSSHORIDT where SHORIDT >= " + iJuhtdt);

      dbDatas = iL.selectJSONArray(sbSQL.toString(), new ArrayList<String>(), Defines.STR_JNDI_DS);

      if (dbDatas.size() != 0) {
        msg.add(mu.getDbMessageObj("E20530", new String[] {reqNo}));
        return msg;
      }
    }

    if ((isFrm1 || isFrm2 || isFrm3) && st && (isToktg_h || isToksp)) {

      // 16_1,16_2,16_3【画面】.「事前打出（チェック）」がチェックされた時のみ必須。
      // E20353 「事前打出（チェック）」がチェックされた時のみ事前打出(日付)が必須です。 0 E
      if (juflg.equals("1") && StringUtils.isEmpty(juhtdt)) {
        msg.add(mu.getDbMessageObj("E20353", new String[] {reqNo}));
        return msg;
      }
    }

    // 172.特売コメント
    // ① 全角30文字。
    // ② 新規の場合、初期値はNULLに設置する。
    // 173.カット店展開しない
    // ① アンケート有の場合、
    // 新規画面では、常に登録ボタンを押すまでは変更可能（前複写の場合も）。
    // 変更画面では、全店特売（アンケート有）_基本.アンケート取込開始日<=処理日付の時、DISABLE。
    // ② 新規の場合、初期値は非チェック状態に設置する。
    // 175.便区分
    // ① 新規の初期値は1に設置する。
    // ② DB登録・更新時に、名称コード区分=10665で名称マスタとの整合チェックを行う。
    if (!StringUtils.isEmpty(binKbn)) {

      // 変数を初期化
      sbSQL = new StringBuffer();
      iL = new ItemList();
      dbDatas = new JSONArray();
      sqlWhere = "";
      paramData = new ArrayList<String>();

      sbSQL.append(DefineReport.ID_SQL_CHK_TBL.replace("@T", "INAMS.MSTMEISHO").replaceAll("@C", "MEISHOCD"));
      sbSQL.append(" and MEISHOKBN = ");
      sbSQL.append(DefineReport.MeisyoSelect.KBN10665.getCd());
      paramData.add(binKbn);

      dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

      if (dbDatas.size() != 0 && dbDatas.getJSONObject(0).getString("VALUE").equals("0")) {
        // E20455 [便区分]整合チェックエラー 0 E
        msg.add(mu.getDbMessageObj("E20455", new String[] {reqNo}));
        return msg;
      }

      // 16_1 ① 必須。② 1を設置し、変更不可。

      if ((isFrm1 || isFrm2 || isFrm3) && st && (isToktg_h || isToksp)) {

        // 16_2,16_3 ① 必須。② 【画面】.納入情報部分の「PC区分」=０の場合、2は選べない。 「PC区分」=０通常商品 便区分 １：１便、２：2便
        // E20355 PC商品でない場合、2は選択できません。
        if (pcKbn.equals("0") && binKbn.equals("2")) {
          msg.add(mu.getDbMessageObj("E20355", new String[] {reqNo}));
          return msg;
        }
      }
    }
    // 176.別伝区分
    // ① 入力範囲：0～8
    // ② 新規の初期値は0である。

    String bdenKbn = "";

    if (isToktg) {
      bdenKbn = data.optString(TOKTG_SHNLayout.BDENKBN.getId());
    } else {
      bdenKbn = data.optString(TOKSP_SHNLayout.BDENKBN.getId());
    }

    if (!StringUtils.isEmpty(bdenKbn)) {
      // ①E20356 0 ≦ 別伝区分の入力範囲 ≦ 8の条件で入力してください。 0 E
      nVal = Integer.valueOf(bdenKbn);
      if (!(0 <= nVal && nVal <= 8)) {
        msg.add(mu.getDbMessageObj("E20356", new String[] {reqNo}));
        return msg;
      }
    }
    // 177.ワッペン区分
    // ① 新規の初期値は0である。
    // ② DB登録・更新時に、名称コード区分=10666で名称マスタとの整合チェックを行う。

    String wappnKbn = "";

    if (isToktg) {
      wappnKbn = data.optString(TOKTG_SHNLayout.WAPPNKBN.getId());
    } else {
      wappnKbn = data.optString(TOKSP_SHNLayout.WAPPNKBN.getId());
    }

    if (!StringUtils.isEmpty(wappnKbn)) {

      // 変数を初期化
      sbSQL = new StringBuffer();
      iL = new ItemList();
      dbDatas = new JSONArray();
      sqlWhere = "";
      paramData = new ArrayList<String>();

      sbSQL.append(DefineReport.ID_SQL_CHK_TBL.replace("@T", "INAMS.MSTMEISHO").replaceAll("@C", "MEISHOCD"));
      sbSQL.append(" and MEISHOKBN = ");
      sbSQL.append(DefineReport.MeisyoSelect.KBN10666.getCd());
      paramData.add(wappnKbn);

      dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

      if (dbDatas.size() == 0) {
        // ②E20456 [ワッペン区分]整合チェックエラー 0 E
        msg.add(mu.getDbMessageObj("E20456", new String[] {reqNo}));
        return msg;
      }
    }
    // 178.週次伝送flg
    // ① 新規の場合、初期値は非チェック状態に設置する。
    // ② PC区分=1の場合、チェック状態で編集不可とする。

    if (!StringUtils.isEmpty(shudenFlg) && shudenFlg.equals("1")) {
      // E20351 PC商品の場合、週次伝送flgはチェックできません。 0 E
    }

    // 179.PC区分
    // ① 【画面】.「商品コード」を入力したタイミングで表示。
    // ② 【画面】.「商品コード」をクリアしたタイミングでクリア。

    // *** 詳細部分 ***
    // 182.日付
    // ① 開始日：納入開始日。
    // ② 終了日：納入終了日 OR 販売終了日の大きい方。ただし、開始日～終了日が11日以上となる場合は11日以降をカットする（最大10日間）。
    // ③ 納入開始/終了日と販売開始/終了日の定義は、『機能概要説明書』の補足処理部分を参照(5.)する。
    // ④ 表示フォーマット：MM/DD。
    // 183.曜日 同列【画面】.「日付」の曜日
    // 184.販売日
    // 185.納入日 新規の場合、初期値は非チェック状態に設置する。
    /*
     * TODO: 不要？ if(allcheck && id===$.id.chk_nndt){
     * if((that.judgeRepType.frm1||that.judgeRepType.frm2||that.judgeRepType.frm3) &&
     * that.judgeRepType.st && (that.judgeRepType.toktg_h||that.judgeRepType.toksp)){ // 16_1,16_2,16_3
     * // ① 【画面】.「納入期間」入力がある時のみ入力可。 // ② 1日はチェックが必要。 // ③ 【画面】.「納入期間」の範囲内 // E20363 「納入日」に1日はチェックが必要です。
     * 0 E if($("[id^="+$.id.chk_nndt+"]:checked").length === 0){ return "E20363"; } } }
     */

    // ランクNo展開配列作成機能
    ReportBM015Dao dao = new ReportBM015Dao(JNDIname);
    ReportJU012Dao daoJu = new ReportJU012Dao(JNDIname);

    String tanknoAdd = "";
    String tanknoDel = "";
    String saveTenrankArr = "";
    if (isToktg) {
      tanknoAdd = data.optString(TOKTG_SHNLayout.RANKNO_ADD.getId());
      tanknoDel = "";
      saveTenrankArr = StringUtils.isEmpty(data.optString(TOKTG_SHNLayout.TENRANK_ARR.getId()).trim()) ? "" : data.optString(TOKTG_SHNLayout.TENRANK_ARR.getId());
    } else {
      tanknoAdd = data.optString(TOKSP_SHNLayout.RANKNO_ADD_A.getId());
      tanknoDel = data.optString(TOKSP_SHNLayout.RANKNO_DEL.getId());
      saveTenrankArr = StringUtils.isEmpty(data.optString(TOKSP_SHNLayout.TENRANK_ARR.getId()).trim()) ? "" : data.optString(TOKSP_SHNLayout.TENRANK_ARR.getId());
    }

    JSONArray tencdAdds = new JSONArray();
    JSONArray rankAdds = new JSONArray();
    JSONArray tencdDels = new JSONArray();
    Set<String> tencds = new TreeSet<String>();

    // 22.除外店
    // ① 入力範囲：001～999。
    // ② 更新の時は編集不可。
    // ③ 新規の場合、初期値はNULLに設置する。
    // ④ アンケート有の場合、編集不可。
    // 23.追加（1～10）
    // 33.ランク（1～10）
    // ① 全店特売（アンケート有/無）_対象除外店.対象除外フラグ=1：対象で保存する。
    // ② 入力範囲：店番号001～400、ランクA～Z。
    // ③ 【画面】.「追加」に店番を入力したら、当列の【画面】.「ランク」は必須とする。
    // ④ 新規の場合、初期値はNULLに設置する。
    // 43.除外（1～10）
    // ① 全店特売（アンケート有/無）_対象除外店.対象除外フラグ=2：除外で保存する。
    // ② 入力範囲：001～400。
    // ③ 新規の場合、初期値はNULLに設置する。
    for (int i = 0; i < dataArrayTJTEN.size(); i++) {
      JSONObject dataJ = dataArrayTJTEN.getJSONObject(i);
      if (dataJ.isEmpty()) {
        continue;
      } else if (dataJ.optString(TOK_CMN_TJTENLayout.SENDFLG.getId()).equals("D")) {
        continue;
      }

      String tjFlg = dataJ.optString(TOK_CMN_TJTENLayout.TJFLG.getId());
      if (tjFlg.equals("1")) {
        String add = dataJ.optString(TOK_CMN_TJTENLayout.TENCD.getId());
        if (tencds.contains(add)) {
          JSONObject o = mu.getDbMessageObj("E20024", new String[] {reqNo});
          msg.add(o);
          return msg;
        } else if (!StringUtils.isEmpty(add)) {
          tencds.add(add);
        }
        String rnk = dataJ.optString(TOK_CMN_TJTENLayout.TENRANK.getId());

        tencdAdds.add(tencdAdds.size(), add);
        rankAdds.add(rankAdds.size(), rnk);

        // ③E20453 「追加」に店番を入力した場合、対応する「ランク」は必須です。 0 E
        if (!StringUtils.isEmpty(add) && StringUtils.isEmpty(rnk)) {
          JSONObject o = mu.getDbMessageObj("E20453", new String[] {reqNo});
          msg.add(o);
          return msg;
        }

        if (isFrm5 && st && isToktg_t) {
          // 16_5 ① 【画面】.「発注原売価適用」のチェックが無い場合任意。② それ以外は入力不可。
          if (htGenBaikaFlg.equals("1")) {
            if (!StringUtils.isEmpty(add)) {
              // E20430 「発注原売価適用」のチェックがある場合は追加が入力不可。 0 E
              JSONObject o = mu.getDbMessageObj("E20430", new String[] {reqNo});
              msg.add(o);
              return msg;
            }
            if (!StringUtils.isEmpty(rnk)) {
              // E20431 「発注原売価適用」のチェックがある場合はランクが入力不可。 0 E
              JSONObject o = mu.getDbMessageObj("E20431", new String[] {reqNo});
              msg.add(o);
              return msg;
            }
          }
        }
      } else if (tjFlg.equals("2")) {
        String del = dataJ.optString(TOK_CMN_TJTENLayout.TENCD.getId());
        if (tencds.contains(del)) {
          JSONObject o = mu.getDbMessageObj("E20024", new String[] {reqNo});
          msg.add(o);
          return msg;
        } else if (!StringUtils.isEmpty(del)) {
          tencds.add(del);
        }

        tencdDels.add(tencdDels.size(), del);

        if (isFrm5 && st && isToktg_t) {
          if (!StringUtils.isEmpty(del)) {
            // E20432 「発注原売価適用」のチェックがある場合は除外が入力不可。 0 E
            JSONObject o = mu.getDbMessageObj("E20432", new String[] {reqNo});
            msg.add(o);
            return msg;
          }
        }

      }
    }

    // 対象店を取得
    if (!StringUtils.isEmpty(tanknoAdd)) {
      if (StringUtils.isEmpty(saveTenrankArr)) {
        String checkTencd = dao.checkTenCdAdd(szBmncd // 部門コード
            , moyskbn // 催し区分
            , moysstdt // 催し開始日
            , moysrban // 催し連番
            , tanknoAdd // 対象ランク№
            , tanknoDel // 除外ランク№
            , tencdAdds // 対象店
            , tencdDels // 除外店
        );
        if (!StringUtils.isEmpty(checkTencd)) {
          msg.add(mu.getDbMessageObj(checkTencd, new String[] {reqNo}));
          return msg;
        }
      } else {
        Set<Integer> tencdsChk = new TreeSet<Integer>();
        for (int i = 0; i < saveTenrankArr.split("").length; i++) {
          if (!StringUtils.isEmpty(saveTenrankArr.split("")[i].trim())) {
            tencdsChk.add(i + 1);
          }
        }

        String msgCd = "";
        // 対象店を追加
        for (int i = 0; i < tencdAdds.size(); i++) {
          if (!StringUtils.isEmpty(tencdAdds.optString(i)) && tencdsChk.contains(tencdAdds.getInt(i))) {
            boolean err = true;
            for (int j = 0; j < tencdDels.size(); j++) {
              if (!StringUtils.isEmpty(tencdDels.optString(j)) && tencdDels.optString(j).equals(tencdAdds.optString(i))) {
                err = false;
                break;
              }
            }

            if (err) {
              msgCd = "E20025";
            }
          } else if (!StringUtils.isEmpty(tencdAdds.optString(i))) {
            tencdsChk.add(tencdAdds.getInt(i));
          }
        }

        // 除外店を削除
        for (int i = 0; i < tencdDels.size(); i++) {
          if (!StringUtils.isEmpty(tencdDels.optString(i)) && !tencdsChk.contains(tencdDels.getInt(i))) {
            msgCd = "E20026";
          } else if (!StringUtils.isEmpty(tencdDels.optString(i))) {
            tencdsChk.remove(tencdDels.getInt(i));
          }
        }

        if (tencdsChk.size() == 0) {
          msgCd = "E20027";
        }

        if (!StringUtils.isEmpty(msgCd)) {
          msg.add(mu.getDbMessageObj(msgCd, new String[] {reqNo}));
          return msg;
        }
      }
    }

    ArrayList<String> tenranks = dao.getTenrankArray(szBmncd, szMoyskbn, szMoysstdt, szMoysrban, tanknoAdd, tanknoDel, tencdAdds, rankAdds, tencdDels, saveTenrankArr);

    // 重複チェック
    String errMsg = "";
    if (!isToktg && !StringUtils.isEmpty(pluSndFlg) && !pluSndFlg.equals("1")) {
      int amount = 0;
      String stdt = "";
      String eddt = StringUtils.isEmpty(hbEdDt) ? "" : CmnDate.dateFormat(CmnDate.getDayAddedDate(CmnDate.convDate(hbEdDt), amount));

      String moyscd = szMoyskbn + szMoysstdt + String.format("%3s", szMoysrban);
      int tenSu = 0;

      while (!stdt.equals(eddt)) {
        stdt = StringUtils.isEmpty(hbStDt) ? "" : CmnDate.dateFormat(CmnDate.getDayAddedDate(CmnDate.convDate(hbStDt), amount));
        amount++;

        // 商品販売日重複チェック
        HashMap<String, String> map = getArrMap(shnCd, "", stdt, szMoyskbn, "1", "1");

        int tenCd = 1;
        for (String rank : tenranks) {
          if (!StringUtils.isEmpty(rank) && map.containsKey(String.valueOf(tenCd))) {
            if ((isChange && !map.get(String.valueOf(tenCd)).equals(moyscd)) || !isChange) {
              if (tenSu == 0) {
                String moyscdMsg = map.get(String.valueOf(tenCd)).substring(0, 1) + "-" + map.get(String.valueOf(tenCd)).substring(1, 7) + "-"
                    + String.format("%03d", Integer.valueOf(map.get(String.valueOf(tenCd)).substring(8).trim()));
                errMsg += "販売日 " + stdt.substring(4, 6) + "月" + stdt.substring(6, 8) + "日 催しコード " + moyscdMsg + "と " + tenCd + "号店以下";
              }
              tenSu++;
            }
          }
          tenCd++;
        }
        if (tenSu != 0) {
          errMsg += tenSu + "店舗<br>";
          tenSu = 0;
        }
      }
      if (!StringUtils.isEmpty(errMsg)) {
        msg.add(mu.getDbMessageObj("E30025", new String[] {reqNo + "商品コード " + shnCd + "<br>" + errMsg + "<br>"}));
        return msg;
      }
    }

    // 186.発注総数
    // ① 入力範囲：0～99,999。
    // ② 【画面】.「予定数」とのチェック無し。
    // ③ 新規の場合、初期値はNULLに設置する。
    // 187.数量計 【画面】.納入情報部分の「発注総数」の期間計。
    // 188.パターン年月表示 『機能概要説明』の「納入情報タブ上のパターンNOと年月表示の制御」（2.1.4.）を参照する。
    // 189.パターンNo.
    // 190.訂正区分
    // ① 入力範囲：0～4。
    // ② 新規の場合、初期値は0に設置する。
    String tenkaiKbn = "";

    if (isToktg) {
      tenkaiKbn = data.optString(TOKTG_SHNLayout.TENKAIKBN.getId());
    } else {
      tenkaiKbn = data.optString(TOKSP_SHNLayout.TENKAIKBN.getId());
    }

    errMsg = "";
    for (int i = 0; i < dataArrayNNDT.size(); i++) {

      JSONObject dataN = dataArrayNNDT.getJSONObject(i);
      if (dataN.isEmpty()) {
        continue;
      }

      String htasu = "";
      String ptnNo = "";
      String tseiKbn = "";
      String chk = "";

      if (isToktg) {
        htasu = dataN.optString(TOKTG_NNDTLayout.HTASU.getId());
        ptnNo = dataN.optString(TOKTG_NNDTLayout.PTNNO.getId());
        tseiKbn = dataN.optString(TOKTG_NNDTLayout.TSEIKBN.getId());
        chk = dataN.optString("F17");
      } else {
        htasu = dataN.optString(TOKSP_NNDTLayout.HTASU.getId());
        ptnNo = dataN.optString(TOKSP_NNDTLayout.PTNNO.getId());
        tseiKbn = dataN.optString(TOKSP_NNDTLayout.TSEIKBN.getId());
        chk = dataN.optString("F16");
      }

      if ((isFrm1 || isFrm2 || isFrm3) && st && (isToksp || isToktg_h)) {
        if (chk.equals("1") && (tenkaiKbn.equals("1") || tenkaiKbn.equals("3"))) {
          // E20360 「納入日」がチェックされ、かつ 数値展開方法が通常率or実績率パタンの時、必須です。 0 E
          if (StringUtils.isEmpty(htasu)) {
            msg.add(mu.getDbMessageObj("E20360", new String[] {reqNo}));
            return msg;
          }
        }

        // E20361 「納入日」がチェックされた場合、パターンNo.は必須です。 0 E
        if (chk.equals("1") && StringUtils.isEmpty(ptnNo) && !tenkaiKbn.equals("3")) {
          msg.add(mu.getDbMessageObj("E20361", new String[] {reqNo}));
          return msg;
        }

        // E20362 「納入日」がチェックされた場合、訂正区分は必須です。 0 E
        if (chk.equals("1") && StringUtils.isEmpty(tseiKbn)) {
          msg.add(mu.getDbMessageObj("E20362", new String[] {reqNo}));
          return msg;
        }
      }

      String nndt = dataN.optString(TOKSP_NNDTLayout.NNDT.getId());

      // 重複チェック
      if (chk.equals("1")) {

        String ptnErr = "";
        if (tenkaiKbn.equals("1") || tenkaiKbn.equals("3")) {
          String syukbn = "";
          String jskptnznenmkbn = "";
          String daicd = ""; // 大分類
          String chucd = ""; // 中分類

          if (isToktg) {
            syukbn = data.optString(TOKTG_SHNLayout.JSKPTNSYUKBN.getId());
            jskptnznenmkbn = data.optString(TOKTG_SHNLayout.JSKPTNZNENMKBN.getId());
            daicd = data.optString(TOKTG_SHNLayout.DAICD.getId()); // 大分類
            chucd = data.optString(TOKTG_SHNLayout.CHUCD.getId()); // 中分類
          } else {
            syukbn = data.optString(TOKSP_SHNLayout.JSKPTNSYUKBN.getId());
            jskptnznenmkbn = data.optString(TOKSP_SHNLayout.JSKPTNZNENMKBN.getId());
            daicd = data.optString(TOKSP_SHNLayout.DAICD.getId()); // 大分類
            chucd = data.optString(TOKSP_SHNLayout.CHUCD.getId()); // 中分類
          }

          String wwmm = "1"; // 週月フラグ 1:週 2:月
          if (jskptnznenmkbn.equals("1") || jskptnznenmkbn.equals("2") || jskptnznenmkbn.equals("3")) {
            wwmm = "2";
          }

          ptnErr = dao.chkRtPt(szBmncd, ptnNo, wwmm, syukbn, daicd, chucd, tenkaiKbn);
        } else {
          ptnErr = dao.chkSuryoPtn(szBmncd, moyskbn, moysstdt, moysrban, ptnNo);
        }

        if (!StringUtils.isEmpty(ptnErr)) {
          msg.add(mu.getDbMessageObj(ptnErr, new String[] {reqNo}));
          return msg;
        }

        if (!isToktg) {
          if (dataN.optString(TOKSP_NNDTLayout.SENDFLG.getId()).equals("1")) {
            String moyscd = szMoyskbn + szMoysstdt + String.format("%3s", szMoysrban);
            int tenSu = 0;

            // 商品納入日重複チェック
            HashMap<String, String> map = getArrMap(shnCd, binKbn, nndt, szMoyskbn, "2", "1");
            HashMap<String, String> mapKanri = getArrMap(shnCd, binKbn, nndt, szMoyskbn, "2", "2");
            String arr = dataN.optString(TOKSP_NNDTLayout.TENHTSU_ARR.getId());
            HashMap<String, String> mapHtsu = daoJu.getDigitMap(arr, 5, "1");
            String kanriNo = data.optString(TOKSP_SHNLayout.KANRINO.getId());

            for (HashMap.Entry<String, String> shnnn : map.entrySet()) {
              if (mapHtsu.containsKey(shnnn.getKey()) && Integer.valueOf(mapHtsu.get(shnnn.getKey())) >= 0) {

                // 更新：便区分が変更されている or 既に別の催しが存在している 登録：ここまできたら無条件にエラー
                if ((isChange && (!binKbn.equals(dataN.optString(TOKSP_NNDTLayout.OPERATOR.getId())) || !shnnn.getValue().equals(moyscd)
                    || (shnnn.getValue().equals(moyscd) && !mapKanri.get(shnnn.getKey()).equals(kanriNo)))) || !isChange) {
                  if (tenSu == 0) {
                    String moyscdMsg = shnnn.getValue().substring(0, 1) + "-" + shnnn.getValue().substring(1, 7) + "-" + String.format("%03d", Integer.valueOf(shnnn.getValue().substring(8).trim()));
                    errMsg += "納入日 " + nndt.substring(4, 6) + "月" + nndt.substring(6, 8) + "日 催しコード " + moyscdMsg + "と " + shnnn.getKey() + "号店以下";
                  }
                  tenSu++;
                }
              }
            }

            if (tenSu != 0) {
              errMsg += tenSu + "店舗<br>";
              tenSu = 0;
            }
          }
        } else {
          // 変数を初期化
          sbSQL = new StringBuffer();
          iL = new ItemList();
          dbDatas = new JSONArray();
          sqlWhere = "";
          paramData = new ArrayList<String>();

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
          paramData.add(nndt);

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
            if (isNew) {
              // 同一納入日の重複チェックエラー
              JSONObject o = mu.getDbMessageObj("E20450", new String[] {reqNo});
              msg.add(o);
              return msg;
            } else {

              String kanrino = data.optString(TOKTG_SHNLayout.KANRINO.getId());

              if (!szMoyskbn.equals(dbDatas.getJSONObject(0).optString("MOYSKBN")) || !szMoysstdt.equals(dbDatas.getJSONObject(0).optString("MOYSSTDT"))
                  || !szMoysrban.equals(dbDatas.getJSONObject(0).optString("MOYSRBAN")) || !kanrino.equals(dbDatas.getJSONObject(0).optString("KANRINO"))) {
                // 同一納入日の重複チェックエラー
                JSONObject o = mu.getDbMessageObj("E20450", new String[] {reqNo});
                msg.add(o);
                return msg;
              }
            }
          }
        }
      }
    }

    if (!StringUtils.isEmpty(errMsg)) {
      msg.add(mu.getDbMessageObj("E30025", new String[] {reqNo + "商品コード " + shnCd + " 便区分 " + binKbn + "便<br>" + errMsg + "<br>"}));
      return msg;
    }

    if (isChange) {
      String msgCd = checkNnDt(data, dataArrayNNDT);
      if (!StringUtils.isEmpty(msgCd)) {
        JSONObject o = mu.getDbMessageObj(msgCd, new String[] {reqNo});
        msg.add(o);
        return msg;
      }
    }

    return msg;
  }

  /**
   * 配列をkye,valueの形で返却(key:店、value:催しコード)
   *
   * @param shnCd 商品コード
   * @param nnDt 納入日
   * @param getTblFlg 1:販売日 2:納入日
   * @param getColFlg 1:催しコード配列 2:管理番号配列 3:重複件数配列
   * @return
   */
  public HashMap<String, String> getArrMap(String shnCd, String binkbn, String dt, String moyskbn, String getTblFlg, String getColFlg) {

    ReportJU012Dao dao = new ReportJU012Dao(JNDIname);

    // 格納用変数
    StringBuffer sbSQL = new StringBuffer();
    ItemList iL = new ItemList();
    JSONArray dbDatas = new JSONArray();

    // DB検索用パラメータ
    String sqlWhere = "";
    String sqlFrom = "INATK.TOKSP_SHNHBDT ";

    String getFlg = "1";

    if (!moyskbn.equals("3")) {
      if (getTblFlg.equals("2")) {
        sqlFrom = "INATK.TOKSP_SHNNNDT ";
      }
    } else {
      if (getTblFlg.equals("1")) {
        sqlFrom = "INATK.TOKHTK_SHNHBDT ";
      } else if (getTblFlg.equals("2")) {
        sqlFrom = "INATK.TOKHTK_SHNNNDT ";
      }
    }

    String sqlSelect = "MOYCD_ARR AS ARR ";
    ArrayList<String> paramData = new ArrayList<String>();

    HashMap<String, String> arrMap = new HashMap<String, String>();
    int digit = 10;

    // 商品コード
    if (StringUtils.isEmpty(shnCd)) {
      sqlWhere += "SHNCD=null AND ";
    } else {
      sqlWhere += "SHNCD=? AND ";
      paramData.add(shnCd);
    }

    if (getTblFlg.equals("1")) {
      // 販売日
      if (StringUtils.isEmpty(dt)) {
        sqlWhere += "HBDT=null ";
      } else {
        sqlWhere += "HBDT=? ";
        paramData.add(dt);
      }
    } else if (getTblFlg.equals("2")) {

      // 便区分
      if (StringUtils.isEmpty(binkbn)) {
        sqlWhere += "BINKBN=null AND ";
      } else {
        sqlWhere += "BINKBN=? AND ";
        paramData.add(binkbn);
      }

      // 納入日
      if (StringUtils.isEmpty(dt)) {
        sqlWhere += "NNDT=null ";
      } else {
        sqlWhere += "NNDT=? ";
        paramData.add(dt);
      }
    }

    if (getColFlg.equals("2")) {
      sqlSelect = "KANRINO_ARR AS ARR ";
      getFlg = "";
      digit = 4;
    }

    sbSQL.append("SELECT ");
    sbSQL.append(sqlSelect); // 配列
    sbSQL.append("FROM ");
    sbSQL.append(sqlFrom);
    sbSQL.append("WHERE ");
    sbSQL.append(sqlWhere);

    dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    // データが存在する場合、配列の展開を実施
    if (dbDatas.size() != 0) {
      arrMap = dao.getDigitMap(dbDatas.getJSONObject(0).optString("ARR"), digit, getFlg);
    }
    return arrMap;
  }

  // 対象店取得処理
  public Set<String> getRmTenCds(String moyscd, String kanrino, HashMap<String, String> mapMoysCd, HashMap<String, String> mapKanriNo) {

    Set<String> tencds = new TreeSet<String>();

    for (HashMap.Entry<String, String> getMoysCd : mapMoysCd.entrySet()) {

      String key = getMoysCd.getKey();
      String val = getMoysCd.getValue();

      if (val.equals(moyscd)) {
        if (mapKanriNo.containsKey(key) && mapKanriNo.get(key).equals(kanrino)) {
          tencds.add(key);
        }
      }
    }
    return tencds;
  }

  public List<JSONObject> checkDataDel(boolean isNew, boolean isChange, boolean isCsvUpload, HashMap<String, String> map, User userInfo, String sysdate1, MessageUtility mu, JSONArray dataArray, // 対象情報（主要な更新情報）
      JSONArray dataArraySRCCD // ソースコード
  ) {

    JSONArray msg = new JSONArray();


    // DB最新情報再取得


    dataArray.optJSONObject(0);

    String login_dt = sysdate1; // 処理日付
    login_dt.substring(2, 6);

    return msg;
  }

  public String checkNnDt(JSONObject data, JSONArray nnDtArray) {

    // 格納用変数
    StringBuffer sbSQL = new StringBuffer();
    ItemList iL = new ItemList();
    JSONArray dbDatas = new JSONArray();

    // DB検索用パラメータ
    String sqlWhere = "";
    String sqlFrom = "INATK.TOKSP_SHN T1, INATK.TOKSP_NNDT T2 ";
    ArrayList<String> paramData = new ArrayList<String>();

    String moyskbn = "";
    String moysstdt = "";
    String moysrban = "";
    String bmncd = "";
    String kanrino = "";
    String kanrieno = "";
    String jhtsuindt = "";
    String weekhtdt = "";
    String shudenflg = "";
    String wappnkbn = "";
    String bdenkbn = "";

    if (isToktg) {
      moyskbn = data.optString(TOKTG_SHNLayout.MOYSKBN.getId());
      moysstdt = data.optString(TOKTG_SHNLayout.MOYSSTDT.getId());
      moysrban = data.optString(TOKTG_SHNLayout.MOYSRBAN.getId());
      bmncd = data.optString(TOKTG_SHNLayout.BMNCD.getId());
      kanrino = data.optString(TOKTG_SHNLayout.KANRINO.getId());
      kanrieno = data.optString(TOKTG_SHNLayout.KANRIENO.getId());
      jhtsuindt = data.optString(TOKTG_SHNLayout.JHTSUINDT.getId());
      weekhtdt = data.optString(TOKTG_SHNLayout.WEEKHTDT.getId());
      shudenflg = data.optString(TOKTG_SHNLayout.SHUDENFLG.getId());
      wappnkbn = data.optString(TOKTG_SHNLayout.WAPPNKBN.getId());
      bdenkbn = data.optString(TOKTG_SHNLayout.BDENKBN.getId());
      sqlFrom = "INATK.TOKTG_SHN T1, INATK.TOKTG_NNDT T2 ";
    } else {
      moyskbn = data.optString(TOKSP_SHNLayout.MOYSKBN.getId());
      moysstdt = data.optString(TOKSP_SHNLayout.MOYSSTDT.getId());
      moysrban = data.optString(TOKSP_SHNLayout.MOYSRBAN.getId());
      bmncd = data.optString(TOKSP_SHNLayout.BMNCD.getId());
      kanrino = data.optString(TOKSP_SHNLayout.KANRINO.getId());
      kanrieno = data.optString(TOKSP_SHNLayout.KANRIENO.getId());
      jhtsuindt = data.optString(TOKSP_SHNLayout.JHTSUINDT.getId());
      weekhtdt = data.optString(TOKSP_SHNLayout.WEEKHTDT.getId());
      shudenflg = data.optString(TOKSP_SHNLayout.SHUDENFLG.getId());
      wappnkbn = data.optString(TOKSP_SHNLayout.WAPPNKBN.getId());
      bdenkbn = data.optString(TOKSP_SHNLayout.BDENKBN.getId());
    }


    if ((StringUtils.isEmpty(jhtsuindt) || (!StringUtils.isEmpty(jhtsuindt) && jhtsuindt.equals("0"))) && (StringUtils.isEmpty(weekhtdt) || (!StringUtils.isEmpty(weekhtdt) && weekhtdt.equals("0")))) {
      return "";
    } else {

      // 催し区分
      if (StringUtils.isEmpty(moyskbn)) {
        sqlWhere += "T1.MOYSKBN=null AND ";
      } else {
        sqlWhere += "T1.MOYSKBN=? AND ";
        paramData.add(moyskbn);
      }

      // 催し開始日
      if (StringUtils.isEmpty(moysstdt)) {
        sqlWhere += "T1.MOYSSTDT=null AND ";
      } else {
        sqlWhere += "T1.MOYSSTDT=? AND ";
        paramData.add(moysstdt);
      }

      // 催し連番
      if (StringUtils.isEmpty(moysrban)) {
        sqlWhere += "T1.MOYSRBAN=null AND ";
      } else {
        sqlWhere += "T1.MOYSRBAN=? AND ";
        paramData.add(moysrban);
      }

      // 部門コード
      if (StringUtils.isEmpty(bmncd)) {
        sqlWhere += "T1.BMNCD=null AND ";
      } else {
        sqlWhere += "T1.BMNCD=? AND ";
        paramData.add(bmncd);
      }

      // 管理番号
      if (StringUtils.isEmpty(kanrino)) {
        sqlWhere += "T1.KANRINO=null AND ";
      } else {
        sqlWhere += "T1.KANRINO=? AND ";
        paramData.add(kanrino);
      }

      // 枝番
      if (StringUtils.isEmpty(kanrieno)) {
        sqlWhere += "T1.KANRIENO=null AND ";
      } else {
        sqlWhere += "T1.KANRIENO=? AND ";
        paramData.add(kanrieno);
      }

      sbSQL.append("SELECT ");
      sbSQL.append(" T2.NNDT ");
      sbSQL.append(",T2.HTASU ");
      sbSQL.append(",T2.PTNNO ");
      sbSQL.append(",T2.TENHTSU_ARR ");
      sbSQL.append(",T1.BDENKBN ");
      sbSQL.append(",T1.WAPPNKBN ");
      sbSQL.append(",T1.TENKAIKBN ");
      sbSQL.append("FROM " + sqlFrom);
      sbSQL.append("WHERE " + sqlWhere + "T1.UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal());
      sbSQL.append(" AND T1.MOYSKBN=T2.MOYSKBN ");
      sbSQL.append(" AND T1.MOYSSTDT=T2.MOYSSTDT ");
      sbSQL.append(" AND T1.MOYSRBAN=T2.MOYSRBAN ");
      sbSQL.append(" AND T1.BMNCD=T2.BMNCD ");
      sbSQL.append(" AND T1.KANRINO=T2.KANRINO ");
      sbSQL.append(" AND T1.KANRIENO=T2.KANRIENO ");
      sbSQL.append(" AND T1.KANRIENO=T2.KANRIENO ");

      dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

      boolean E20465 = false;
      boolean E20464 = false;

      for (int i = 0; i < dbDatas.size(); i++) {
        for (int j = 0; j < nnDtArray.size(); j++) {

          String dbNnDt = dbDatas.optJSONObject(i).getString("NNDT");
          String tenkaikbn = dbDatas.optJSONObject(i).getString("TENKAIKBN");
          String dbhatAsu = tenkaikbn.equals("2") ? "" : dbDatas.optJSONObject(i).getString("HTASU");
          String dbPtnNo = dbDatas.optJSONObject(i).getString("PTNNO");
          String dbArr = dbDatas.optJSONObject(i).getString("TENHTSU_ARR");
          String dbBdenKbn = dbDatas.optJSONObject(i).getString("BDENKBN");
          String dbWappnKbn = dbDatas.optJSONObject(i).getString("WAPPNKBN");
          String nndt = "";
          String htasu = "";
          String ptnno = "";
          String arr = "";

          if (isToktg) {
            nndt = nnDtArray.optJSONObject(j).optString(TOKTG_NNDTLayout.NNDT.getId());
            htasu = nnDtArray.optJSONObject(j).optString(TOKTG_NNDTLayout.HTASU.getId());
            ptnno = nnDtArray.optJSONObject(j).optString(TOKTG_NNDTLayout.PTNNO.getId());
            arr = nnDtArray.optJSONObject(j).optString(TOKTG_NNDTLayout.TENHTSU_ARR.getId());
          } else {
            nndt = nnDtArray.optJSONObject(j).optString(TOKSP_NNDTLayout.NNDT.getId());
            htasu = nnDtArray.optJSONObject(j).optString(TOKSP_NNDTLayout.HTASU.getId());
            ptnno = nnDtArray.optJSONObject(j).optString(TOKSP_NNDTLayout.PTNNO.getId());
            arr = nnDtArray.optJSONObject(j).optString(TOKSP_NNDTLayout.TENHTSU_ARR.getId());
          }

          if (dbNnDt.equals(nndt)) {
            // 週間発注処理日に設定がありかつ週次伝送フラグ=1の場合
            if (!StringUtils.isEmpty(weekhtdt) && !weekhtdt.equals("0") && shudenflg.equals("1")) {
              if (!dbBdenKbn.equals(bdenkbn) || !dbWappnKbn.equals(wappnkbn) || !dbArr.equals(arr)) {
                E20465 = true;
              }
            }

            if (!dbhatAsu.equals(htasu) || !dbPtnNo.equals(ptnno)) {
              // 週間発注処理日に設定があった場合
              if (!StringUtils.isEmpty(weekhtdt) && !weekhtdt.equals("0")) {
                E20465 = true;

                // 事前発注数量取込日に設定があった場合
              } else if (!StringUtils.isEmpty(jhtsuindt) && !jhtsuindt.equals("0")) {
                E20464 = true;
              }
            }
          }
        }
      }

      if (E20465) {
        return "E20465";
      } else if (E20464) {
        return "E20464";
      }
    }
    return "";
  }

  public List<JSONObject> checkCsvNndt(MessageUtility mu, JSONArray dataArray, // メイン
      JSONArray dataArrayShn // 商品
  ) {

    // 格納用変数
    StringBuffer sbSQL = new StringBuffer();
    JSONArray msg = new JSONArray();
    ItemList iL = new ItemList();
    JSONArray dbDatas = new JSONArray();
    String moyskbn = "";
    String moysstdt = "";
    String moysrban = "";

    // DB検索用パラメータ
    String sqlWhere = "";
    String sqlFrom = "";
    ArrayList<String> paramData = new ArrayList<String>();

    String JNDIname = Defines.STR_JNDI_DS;

    isChange = true;

    // 基本データチェック:入力値がテーブル定義と矛盾してないか確認、
    // 1.メイン
    for (int i = 0; i < dataArray.size(); i++) {
      JSONObject jo = dataArray.optJSONObject(i);

      String reqNo = String.valueOf(i + 1) + "行目：";

      for (TOKSP_NNDTLayout colinf : TOKSP_NNDTLayout.values()) {
        String val = StringUtils.trim(jo.optString(colinf.getId()));
        if (StringUtils.isNotEmpty(val)) {
          DataType dtype = null;
          int[] digit = null;

          String txt = colinf.getTxt() + "は";
          if (colinf.getCol().equals(TOKSP_NNDTLayout.MOYSKBN.getCol()) || colinf.getCol().equals(TOKSP_NNDTLayout.MOYSSTDT.getCol()) || colinf.getCol().equals(TOKSP_NNDTLayout.MOYSRBAN.getCol())) {
            txt = "催しコードは";
          }

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
            // エラー発生箇所を保存
            JSONObject o = mu.getDbMessageObjDataTypeErr(dtype, new String[] {reqNo + txt});
            msg.add(o);
            return msg;
          }
          // ②データ桁チェック
          if (!InputChecker.checkDataLen(dtype, val, digit)) {
            // エラー発生箇所を保存
            JSONObject o = mu.getDbMessageObjLen(dtype, new String[] {reqNo + txt});
            msg.add(o);
            return msg;
          }
        }
      }
    }

    // 2:商品コード
    for (int i = 0; i < dataArrayShn.size(); i++) {
      JSONObject jo = dataArrayShn.optJSONObject(i);

      String reqNo = String.valueOf(i + 1) + "行目：";

      for (TOK_CMN_SHNNNDTLayout colinf : TOK_CMN_SHNNNDTLayout.values()) {
        String val = StringUtils.trim(jo.optString(colinf.getId()));
        if (StringUtils.isNotEmpty(val)) {
          DataType dtype = null;
          int[] digit = null;

          String txt = colinf.getTxt() + "は";
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
            // エラー発生箇所を保存
            JSONObject o = mu.getDbMessageObjDataTypeErr(dtype, new String[] {reqNo + txt});
            msg.add(o);
            return msg;
          }
          // ②データ桁チェック
          if (!InputChecker.checkDataLen(dtype, val, digit)) {
            // エラー発生箇所を保存
            JSONObject o = mu.getDbMessageObjLen(dtype, new String[] {reqNo + txt});
            msg.add(o);
            return msg;
          }
        }
      }
    }

    if (msg.size() > 0) {
      return msg;
    }

    TOKSP_NNDTLayout[] targetCol = null;
    targetCol = new TOKSP_NNDTLayout[] {TOKSP_NNDTLayout.MOYSKBN, TOKSP_NNDTLayout.MOYSSTDT, TOKSP_NNDTLayout.MOYSRBAN, TOKSP_NNDTLayout.BMNCD, TOKSP_NNDTLayout.KANRINO, TOKSP_NNDTLayout.NNDT};
    for (int i = 0; i < dataArray.size(); i++) {
      JSONObject jo = dataArray.optJSONObject(i);

      String reqNo = String.valueOf(i + 1) + "行目：";

      for (TOKSP_NNDTLayout colinf : targetCol) {

        String val = StringUtils.trim(jo.optString(colinf.getId()));
        if (StringUtils.isEmpty(val)) {

          String txt = colinf.getTxt();
          if (colinf.getCol().equals(TOKSP_NNDTLayout.MOYSKBN.getCol()) || colinf.getCol().equals(TOKSP_NNDTLayout.MOYSSTDT.getCol()) || colinf.getCol().equals(TOKSP_NNDTLayout.MOYSRBAN.getCol())) {
            txt = "催しコード";
          }

          // エラー発生箇所を保存
          JSONObject o = mu.getDbMessageObj("EX1047", new String[] {reqNo + txt});
          msg.add(o);
          return msg;
        }
      }
    }


    // 存在チェック
    for (int i = 0; i < dataArray.size(); i++) {

      JSONObject data = dataArray.getJSONObject(i);
      if (data.isEmpty()) {
        continue;
      }

      String reqNo = String.valueOf(i + 1) + "行目：";

      moyskbn = data.optString(TOKSP_NNDTLayout.MOYSKBN.getId());
      moysstdt = data.optString(TOKSP_NNDTLayout.MOYSSTDT.getId());
      moysrban = data.optString(TOKSP_NNDTLayout.MOYSRBAN.getId());
      String bmncd = data.optString(TOKSP_NNDTLayout.BMNCD.getId());
      String kanrino = data.optString(TOKSP_NNDTLayout.KANRINO.getId());
      String nndt = data.optString(TOKSP_NNDTLayout.NNDT.getId());
      String htsuArr = data.optString(TOKSP_NNDTLayout.TENHTSU_ARR.getId()).split("-")[0];
      String flg = data.optString(TOKSP_NNDTLayout.TENHTSU_ARR.getId()).split("-")[1];

      isToktg = super.isTOKTG(moyskbn, moysrban); // アンケート有

      // 催しコード存在確認
      // 変数を初期化
      sbSQL = new StringBuffer();
      iL = new ItemList();
      dbDatas = new JSONArray();
      sqlWhere = "";
      paramData = new ArrayList<String>();

      // 催し区分
      if (StringUtils.isEmpty(moyskbn)) {
        sqlWhere += "MOYSKBN=null AND ";
      } else {
        sqlWhere += "MOYSKBN=? AND ";
        paramData.add(moyskbn);
      }

      // 催し開始日
      if (StringUtils.isEmpty(moysstdt)) {
        sqlWhere += "MOYSSTDT=null AND ";
      } else {
        sqlWhere += "MOYSSTDT=? AND ";
        paramData.add(moysstdt);
      }

      // 催し連番
      if (StringUtils.isEmpty(moysrban)) {
        sqlWhere += "MOYSRBAN=null AND ";
      } else {
        sqlWhere += "MOYSRBAN=? AND ";
        paramData.add(moysrban);
      }

      sbSQL.append("SELECT ");
      sbSQL.append("MOYSKBN ");
      sbSQL.append("FROM ");
      sbSQL.append("INATK.TOKMOYCD ");
      sbSQL.append("WHERE ");
      sbSQL.append(sqlWhere + "UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal()); // 入力された催しコード

      dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

      if (dbDatas.size() == 0) {
        // エラー発生箇所を保存
        JSONObject o = mu.getDbMessageObj("E20100", new String[] {reqNo});
        msg.add(o);
        return msg;
      }

      // 部門コード存在確認
      // 変数を初期化
      sbSQL = new StringBuffer();
      iL = new ItemList();
      dbDatas = new JSONArray();
      sqlWhere = "";
      paramData = new ArrayList<String>();

      // 部門コード
      if (StringUtils.isEmpty(bmncd)) {
        sqlWhere += "BMNCD=null AND ";
      } else {
        sqlWhere += "BMNCD=? AND ";
        paramData.add(bmncd);
      }

      sbSQL.append("SELECT ");
      sbSQL.append("BMNCD ");
      sbSQL.append("FROM ");
      sbSQL.append("INAMS.MSTBMN ");
      sbSQL.append("WHERE ");
      sbSQL.append(sqlWhere + "UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal()); // 入力された部門コード

      dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

      if (dbDatas.size() == 0) {
        // エラー発生箇所を保存
        JSONObject o = mu.getDbMessageObj("E11044", new String[] {reqNo});
        msg.add(o);
        return msg;
      }

      // 変数を初期化
      sbSQL = new StringBuffer();
      iL = new ItemList();
      dbDatas = new JSONArray();
      sqlWhere = "";
      paramData = new ArrayList<String>();

      if (isToktg) {
        sqlFrom = "INATK.TOKTG_SHN ";
      } else {
        sqlFrom = "INATK.TOKSP_SHN ";
      }

      // 催し区分
      if (StringUtils.isEmpty(moyskbn)) {
        sqlWhere += "MOYSKBN=null AND ";
      } else {
        sqlWhere += "MOYSKBN=? AND ";
        paramData.add(moyskbn);
      }

      // 催し開始日
      if (StringUtils.isEmpty(moysstdt)) {
        sqlWhere += "MOYSSTDT=null AND ";
      } else {
        sqlWhere += "MOYSSTDT=? AND ";
        paramData.add(moysstdt);
      }

      // 催し連番
      if (StringUtils.isEmpty(moysrban)) {
        sqlWhere += "MOYSRBAN=null AND ";
      } else {
        sqlWhere += "MOYSRBAN=? AND ";
        paramData.add(moysrban);
      }

      // 部門コード
      if (StringUtils.isEmpty(bmncd)) {
        sqlWhere += "BMNCD=null AND ";
      } else {
        sqlWhere += "BMNCD=? AND ";
        paramData.add(bmncd);
      }

      // 管理番号
      if (StringUtils.isEmpty(kanrino)) {
        sqlWhere += "KANRINO=null ";
      } else {
        sqlWhere += "KANRINO=? ";
        paramData.add(kanrino);
      }

      sbSQL.append("SELECT DISTINCT ");
      sbSQL.append("SHUDENFLG "); // 週次フラグ
      sbSQL.append(",CASE WHEN WEEKHTDT IS NULL THEN 0 ELSE WEEKHTDT END WEEKHTDT "); // 週間発注処理日
      if (isToktg) {
        sbSQL.append(",GTSIMECHGKBN "); // 月締変更理由
        sbSQL.append(",GTSIMEOKFLG "); // 月締変更許可フラグ
      }
      sbSQL.append("FROM ");
      sbSQL.append(sqlFrom);
      sbSQL.append("WHERE ");
      sbSQL.append(sqlWhere); // 入力された催しコード

      dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

      if (dbDatas.size() == 0) {
        // エラー発生箇所を保存
        JSONObject o = mu.getDbMessageObj("E20005", new String[] {reqNo});
        msg.add(o);
        return msg;
      }

      String gtsimechgkbn = "";
      String gtsimeokflg = "";

      if (isToktg) {
        gtsimechgkbn = dbDatas.optJSONObject(0).containsKey("GTSIMECHGKBN") ? dbDatas.optJSONObject(0).getString("GTSIMECHGKBN") : "";
        gtsimeokflg = dbDatas.optJSONObject(0).containsKey("GTSIMEOKFLG") ? dbDatas.optJSONObject(0).getString("GTSIMEOKFLG") : "";
      }

      // 変数を初期化
      sbSQL = new StringBuffer();
      iL = new ItemList();
      dbDatas = new JSONArray();
      sqlWhere = "";
      paramData = new ArrayList<String>();

      if (isToktg && (gtsimechgkbn.equals("0") || StringUtils.isEmpty(gtsimechgkbn)) && !gtsimeokflg.equals("0")) {
        sqlFrom = "INATK.TOKTG_KHN ";

        // 催し区分
        if (StringUtils.isEmpty(moyskbn)) {
          sqlWhere += "MOYSKBN=null AND ";
        } else {
          sqlWhere += "MOYSKBN=? AND ";
          paramData.add(moyskbn);
        }

        // 催し開始日
        if (StringUtils.isEmpty(moysstdt)) {
          sqlWhere += "MOYSSTDT=null AND ";
        } else {
          sqlWhere += "MOYSSTDT=? AND ";
          paramData.add(moysstdt);
        }

        // 催し連番
        if (StringUtils.isEmpty(moysrban)) {
          sqlWhere += "MOYSRBAN=null AND ";
        } else {
          sqlWhere += "MOYSRBAN=? AND ";
          paramData.add(moysrban);
        }

        sbSQL.append("SELECT ");
        sbSQL.append("MOYSKBN "); // 件数
        sbSQL.append("FROM ");
        sbSQL.append(sqlFrom);
        sbSQL.append("WHERE ");
        sbSQL.append(sqlWhere); // 入力された催しコード
        sbSQL.append("GTSIMEFLG = '1' ");

        dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

        if (dbDatas.size() != 0) {
          // エラー発生箇所を保存
          JSONObject o = mu.getDbMessageObj("E40093", new String[] {reqNo});
          msg.add(o);
          return msg;
        }
      }

      // 変数を初期化
      sbSQL = new StringBuffer();
      iL = new ItemList();
      dbDatas = new JSONArray();
      sqlWhere = "";
      paramData = new ArrayList<String>();

      if (isToktg) {
        sqlFrom = "INATK.TOKTG_NNDT ";
      } else {
        sqlFrom = "INATK.TOKSP_NNDT ";
      }

      // 催し区分
      if (StringUtils.isEmpty(moyskbn)) {
        sqlWhere += "MOYSKBN=null AND ";
      } else {
        sqlWhere += "MOYSKBN=? AND ";
        paramData.add(moyskbn);
      }

      // 催し開始日
      if (StringUtils.isEmpty(moysstdt)) {
        sqlWhere += "MOYSSTDT=null AND ";
      } else {
        sqlWhere += "MOYSSTDT=? AND ";
        paramData.add(moysstdt);
      }

      // 催し連番
      if (StringUtils.isEmpty(moysrban)) {
        sqlWhere += "MOYSRBAN=null AND ";
      } else {
        sqlWhere += "MOYSRBAN=? AND ";
        paramData.add(moysrban);
      }

      // 部門コード
      if (StringUtils.isEmpty(bmncd)) {
        sqlWhere += "BMNCD=null AND ";
      } else {
        sqlWhere += "BMNCD=? AND ";
        paramData.add(bmncd);
      }

      // 管理番号
      if (StringUtils.isEmpty(kanrino)) {
        sqlWhere += "KANRINO=null AND ";
      } else {
        sqlWhere += "KANRINO=? AND ";
        paramData.add(kanrino);
      }

      // 納入日
      if (StringUtils.isEmpty(nndt)) {
        sqlWhere += "NNDT=null ";
      } else {
        sqlWhere += "NNDT=? ";
        paramData.add(nndt);
      }

      sbSQL.append("SELECT DISTINCT ");
      sbSQL.append("MOYSKBN "); // 件数
      sbSQL.append(",TENHTSU_ARR "); // 件数
      sbSQL.append("FROM ");
      sbSQL.append(sqlFrom);
      sbSQL.append("WHERE ");
      sbSQL.append(sqlWhere); // 入力された催しコード

      dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

      if (dbDatas.size() == 0 || (dbDatas.size() != 0 && !dbDatas.getJSONObject(0).containsKey("MOYSKBN"))) {
        // エラー発生箇所を保存
        JSONObject o = mu.getDbMessageObj("E30027", new String[] {reqNo + "納入日"});
        msg.add(o);
        return msg;
      }

      // 数値の設定がない店舗は更新対象外
      String tenhtsuArr = dbDatas.getJSONObject(0).containsKey("TENHTSU_ARR") ? dbDatas.getJSONObject(0).getString("TENHTSU_ARR") : "";
      HashMap<String, String> arrMapNn = new ReportJU012Dao(JNDIname).getDigitMap(tenhtsuArr, 5, "0");
      HashMap<String, String> arrMapCsv = new ReportJU012Dao(JNDIname).getDigitMap(htsuArr, 5, "0");

      for (HashMap.Entry<String, String> csv : arrMapCsv.entrySet()) {

        String key = flg.equals("1") ? String.valueOf((Integer.valueOf(csv.getKey()) + 200)) : csv.getKey();
        String val = csv.getValue();

        // 店舗存在チェック

        // 変数を初期化
        sbSQL = new StringBuffer();
        iL = new ItemList();
        dbDatas = new JSONArray();
        sqlWhere = "";
        paramData = new ArrayList<String>();
        sqlFrom = "INAMS.MSTTEN ";

        // 店コード
        if (StringUtils.isEmpty(key)) {
          sqlWhere += "TENCD=null AND ";
        } else {
          sqlWhere += "TENCD=? AND ";
          paramData.add(key);
        }

        sbSQL.append("SELECT ");
        sbSQL.append("TENCD "); // 件数
        sbSQL.append("FROM ");
        sbSQL.append(sqlFrom);
        sbSQL.append("WHERE ");
        sbSQL.append(sqlWhere); // 入力された催しコード
        sbSQL.append("MISEUNYOKBN = 9 AND ");
        sbSQL.append("IFNULL(UPDKBN, 0)=" + DefineReport.ValUpdkbn.NML.getVal());

        dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

        if (dbDatas.size() != 0) {
          // エラー発生箇所を保存
          JSONObject o = mu.getDbMessageObj("E20229", new String[] {reqNo});
          msg.add(o);
          return msg;
        }

        boolean err = false;

        // 登録のない店舗が設定されている場合エラー
        if (arrMapNn.containsKey(key)) {
          if (StringUtils.isEmpty(arrMapNn.get(key)) && !StringUtils.isEmpty(val)) {
            err = true;
          }
        } else {
          err = true;
        }

        if (err) {
          // エラー発生箇所を保存
          JSONObject o = mu.getDbMessageObj("E30012", new String[] {reqNo + "発注数の設定されている店舗"});
          msg.add(o);
          return msg;
        }
      }

      if (flg.equals("0")) {
        htsuArr += tenhtsuArr.substring(1000);
      } else {
        htsuArr = tenhtsuArr.substring(0, 1000) + htsuArr;
      }

      data.replace(TOKSP_NNDTLayout.TENHTSU_ARR.getId(), htsuArr);
      dataArray.set(i, data);
    }

    // 存在チェック
    for (int i = 0; i < dataArrayShn.size(); i++) {

      JSONObject data = dataArrayShn.getJSONObject(i);
      if (data.isEmpty()) {
        continue;
      }

      String shncd = data.optString(TOK_CMN_SHNNNDTLayout.SHNCD.getId());
      String binkbn = data.optString(TOK_CMN_SHNNNDTLayout.BINKBN.getId());
      String nndt = data.optString(TOK_CMN_SHNNNDTLayout.NNDT.getId());

      // 変数を初期化
      sbSQL = new StringBuffer();
      iL = new ItemList();
      dbDatas = new JSONArray();
      sqlWhere = "";
      paramData = new ArrayList<String>();

      // 商品コード
      if (StringUtils.isEmpty(shncd)) {
        sqlWhere += "SHNCD=null AND ";
      } else {
        sqlWhere += "SHNCD=? AND ";
        paramData.add(shncd);
      }

      sbSQL.append("SELECT ");
      sbSQL.append("SHNCD "); // 件数
      sbSQL.append("FROM ");
      sbSQL.append("INAMS.MSTSHN ");
      sbSQL.append("WHERE ");
      sbSQL.append(sqlWhere + "UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal());

      dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

      if (dbDatas.size() == 0) {
        // エラー発生箇所を保存
        JSONObject o = mu.getDbMessageObj("E11046", new String[] {reqNo});
        msg.add(o);
        return msg;
      }

      if (!isToktg) {

        // 変数を初期化
        sbSQL = new StringBuffer();
        iL = new ItemList();
        dbDatas = new JSONArray();
        sqlWhere = "";
        paramData = new ArrayList<String>();

        if (moyskbn.equals("3")) {
          sqlFrom = "INATK.TOKHTK_SHNNNDT ";
        } else {
          sqlFrom = "INATK.TOKSP_SHNNNDT ";
        }

        // 商品コード
        if (StringUtils.isEmpty(shncd)) {
          sqlWhere += "SHNCD=null AND ";
        } else {
          sqlWhere += "SHNCD=? AND ";
          paramData.add(shncd);
        }

        // 便区分
        if (StringUtils.isEmpty(binkbn)) {
          sqlWhere += "BINKBN=null AND ";
        } else {
          sqlWhere += "BINKBN=? AND ";
          paramData.add(binkbn);
        }

        // 納入日
        if (StringUtils.isEmpty(nndt)) {
          sqlWhere += "NNDT=null ";
        } else {
          sqlWhere += "NNDT=? ";
          paramData.add(nndt);
        }

        sbSQL.append("SELECT ");
        sbSQL.append("SHNCD "); // 件数
        sbSQL.append("FROM ");
        sbSQL.append(sqlFrom);
        sbSQL.append("WHERE ");
        sbSQL.append(sqlWhere);

        dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

        if (dbDatas.size() == 0) {
          // エラー発生箇所を保存
          JSONObject o = mu.getDbMessageObj("E40076", new String[] {reqNo});
          msg.add(o);
          return msg;
        }
      }
    }
    return msg;
  }


  // チェック済キー格納用変数
  Set<String> shnCd = new TreeSet<String>();
  Set<String> tenCdChk = new TreeSet<String>();
  Set<String> tokKhn = new TreeSet<String>();
  HashMap<String, String> tokShnMap = new HashMap<String, String>();

  public List<JSONObject> checkCsvNndt2(MessageUtility mu, JSONArray dataArray, // メイン
      JSONArray dataArrayShn, // 商品
      String reqNo) {

    // 格納用変数
    StringBuffer sbSQL = new StringBuffer();
    JSONArray msg = new JSONArray();
    ItemList iL = new ItemList();
    JSONArray dbDatas = new JSONArray();
    String moyskbn = "";
    String moysstdt = "";
    String moysrban = "";

    // DB検索用パラメータ
    String sqlWhere = "";
    String sqlFrom = "";
    ArrayList<String> paramData = new ArrayList<String>();

    String JNDIname = Defines.STR_JNDI_DS;

    isChange = true;

    // 基本データチェック:入力値がテーブル定義と矛盾してないか確認、
    // 1.メイン
    for (int i = 0; i < dataArray.size(); i++) {
      JSONObject jo = dataArray.optJSONObject(i);
      for (TOKSP_NNDTLayout colinf : TOKSP_NNDTLayout.values()) {
        String val = StringUtils.trim(jo.optString(colinf.getId()));
        if (StringUtils.isNotEmpty(val)) {
          DataType dtype = null;
          int[] digit = null;

          String txt = colinf.getTxt() + "は";
          if (colinf.getCol().equals(TOKSP_NNDTLayout.MOYSKBN.getCol()) || colinf.getCol().equals(TOKSP_NNDTLayout.MOYSSTDT.getCol()) || colinf.getCol().equals(TOKSP_NNDTLayout.MOYSRBAN.getCol())) {
            txt = "催しコードは";
          }

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
            // エラー発生箇所を保存
            JSONObject o = mu.getDbMessageObjDataTypeErr(dtype, new String[] {reqNo + txt});
            msg.add(o);
            return msg;
          }
          // ②データ桁チェック
          if (!InputChecker.checkDataLen(dtype, val, digit)) {
            // エラー発生箇所を保存
            JSONObject o = mu.getDbMessageObjLen(dtype, new String[] {reqNo + txt});
            msg.add(o);
            return msg;
          }
        }
      }
    }

    // 2:商品コード
    for (int i = 0; i < dataArrayShn.size(); i++) {
      JSONObject jo = dataArrayShn.optJSONObject(i);
      if (isToktg) {
        for (TOKTG_SHNLayout colinf : TOKTG_SHNLayout.values()) {
          String val = StringUtils.trim(jo.optString(colinf.getId()));
          if (StringUtils.isNotEmpty(val)) {
            DataType dtype = null;
            int[] digit = null;

            String txt = colinf.getTxt() + "は";
            if (colinf.getCol().equals(TOKTG_SHNLayout.MOYSKBN.getCol()) || colinf.getCol().equals(TOKTG_SHNLayout.MOYSSTDT.getCol()) || colinf.getCol().equals(TOKTG_SHNLayout.MOYSRBAN.getCol())) {
              txt = "催しコードは";
            }

            try {
              DefineReport.InpText inpsetting = DefineReport.InpText.valueOf(colinf.getCol());
              dtype = inpsetting.getType();
              digit = new int[] {inpsetting.getDigit1(), inpsetting.getDigit2()};
            } catch (IllegalArgumentException e) {
              dtype = colinf.getDataType();
              digit = colinf.getDigit();
            }

            if (colinf.getCol().equals(TOKTG_SHNLayout.MAKERKN.getCol())) {
              dtype = DefineReport.DataType.TEXT;
              digit = new int[] {28, 0};
            } else if (colinf.getCol().equals(TOKTG_SHNLayout.KANRIENO.getCol())) {
              dtype = DefineReport.DataType.SUUJI;
              digit = new int[] {4, 0};
            }

            // ①データ型による文字種チェック
            if (!InputChecker.checkDataType(dtype, val)) {
              // エラー発生箇所を保存
              JSONObject o = mu.getDbMessageObjDataTypeErr(dtype, new String[] {reqNo + txt});
              msg.add(o);
              return msg;
            }
            // ②データ桁チェック
            if (!InputChecker.checkDataLen(dtype, val, digit)) {
              // エラー発生箇所を保存
              JSONObject o = mu.getDbMessageObjLen(dtype, new String[] {reqNo + txt});
              msg.add(o);
              return msg;
            }
          }
        }
      } else {
        for (TOKSP_SHNLayout colinf : TOKSP_SHNLayout.values()) {
          String val = StringUtils.trim(jo.optString(colinf.getId()));
          if (StringUtils.isNotEmpty(val)) {
            DataType dtype = null;
            int[] digit = null;

            String txt = colinf.getTxt() + "は";
            if (colinf.getCol().equals(TOKSP_SHNLayout.MOYSKBN.getCol()) || colinf.getCol().equals(TOKSP_SHNLayout.MOYSSTDT.getCol()) || colinf.getCol().equals(TOKSP_SHNLayout.MOYSRBAN.getCol())) {
              txt = "催しコードは";
            }

            try {
              DefineReport.InpText inpsetting = DefineReport.InpText.valueOf(colinf.getCol());
              dtype = inpsetting.getType();
              digit = new int[] {inpsetting.getDigit1(), inpsetting.getDigit2()};
            } catch (IllegalArgumentException e) {
              dtype = colinf.getDataType();
              digit = colinf.getDigit();
            }

            if (colinf.getCol().equals(TOKSP_SHNLayout.MAKERKN.getCol())) {
              dtype = DefineReport.DataType.TEXT;
              digit = new int[] {28, 0};
            } else if (colinf.getCol().equals(TOKSP_SHNLayout.KANRIENO.getCol())) {
              dtype = DefineReport.DataType.SUUJI;
              digit = new int[] {4, 0};
            }

            // ①データ型による文字種チェック
            if (!InputChecker.checkDataType(dtype, val)) {
              // エラー発生箇所を保存
              JSONObject o = mu.getDbMessageObjDataTypeErr(dtype, new String[] {reqNo + txt});
              msg.add(o);
              return msg;
            }
            // ②データ桁チェック
            if (!InputChecker.checkDataLen(dtype, val, digit)) {
              // エラー発生箇所を保存
              JSONObject o = mu.getDbMessageObjLen(dtype, new String[] {reqNo + txt});
              msg.add(o);
              return msg;
            }
          }
        }
      }
    }

    if (msg.size() > 0) {
      return msg;
    }

    TOKSP_NNDTLayout[] targetCol = null;
    targetCol = new TOKSP_NNDTLayout[] {TOKSP_NNDTLayout.MOYSKBN, TOKSP_NNDTLayout.MOYSSTDT, TOKSP_NNDTLayout.MOYSRBAN, TOKSP_NNDTLayout.BMNCD, TOKSP_NNDTLayout.NNDT};
    for (int i = 0; i < dataArray.size(); i++) {
      JSONObject jo = dataArray.optJSONObject(i);
      for (TOKSP_NNDTLayout colinf : targetCol) {

        String val = StringUtils.trim(jo.optString(colinf.getId()));
        if (StringUtils.isEmpty(val)) {

          String txt = colinf.getTxt();
          if (colinf.getCol().equals(TOKSP_NNDTLayout.MOYSKBN.getCol()) || colinf.getCol().equals(TOKSP_NNDTLayout.MOYSSTDT.getCol()) || colinf.getCol().equals(TOKSP_NNDTLayout.MOYSRBAN.getCol())) {
            txt = "催しコード";
          }

          // エラー発生箇所を保存
          JSONObject o = mu.getDbMessageObj("EX1047", new String[] {reqNo + txt});
          msg.add(o);
          return msg;
        }
      }
    }

    // 存在チェック
    for (int i = 0; i < dataArray.size(); i++) {

      JSONObject data = dataArray.getJSONObject(i);
      if (data.isEmpty()) {
        continue;
      }

      moyskbn = data.optString(TOKSP_NNDTLayout.MOYSKBN.getId());
      moysstdt = data.optString(TOKSP_NNDTLayout.MOYSSTDT.getId());
      moysrban = data.optString(TOKSP_NNDTLayout.MOYSRBAN.getId());
      String bmncd = data.optString(TOKSP_NNDTLayout.BMNCD.getId());
      String kanrino = data.optString(TOKSP_NNDTLayout.KANRINO.getId());
      data.optString(TOKSP_NNDTLayout.NNDT.getId());
      String htsuArr = data.optString(TOKSP_NNDTLayout.TENHTSU_ARR.getId());
      String shncd = "";
      isToktg = super.isTOKTG(moyskbn, moysrban); // アンケート有

      JSONObject dataShn = dataArrayShn.getJSONObject(0);
      if (isToktg) {
        shncd = dataShn.optString(TOKTG_SHNLayout.SHNCD.getId());
        dataShn.optString(TOKTG_SHNLayout.BINKBN.getId());
      } else {
        shncd = dataShn.optString(TOKSP_SHNLayout.SHNCD.getId());
        dataShn.optString(TOKSP_SHNLayout.BINKBN.getId());
      }

      // 変数を初期化
      // 商品コード
      if (!StringUtils.isEmpty(shncd)) {
        if (!shnCd.contains(shncd)) {
          sbSQL = new StringBuffer();
          iL = new ItemList();
          dbDatas = new JSONArray();
          sqlWhere = "";
          paramData = new ArrayList<String>();

          sqlWhere += "SHNCD=? AND ";
          paramData.add(shncd);

          sbSQL.append("SELECT ");
          sbSQL.append("SHNCD "); // 件数
          sbSQL.append("FROM ");
          sbSQL.append("INAMS.MSTSHN ");
          sbSQL.append("WHERE ");
          sbSQL.append(sqlWhere + "UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal());

          dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

          if (dbDatas.size() == 0) {
            // エラー発生箇所を保存
            JSONObject o = mu.getDbMessageObj("E11046", new String[] {reqNo});
            msg.add(o);
            return msg;
          }
          shnCd.add(shncd);
        }
      } else if (StringUtils.isEmpty(kanrino)) {
        // エラー発生箇所を保存
        JSONObject o = mu.getDbMessageObj("EX1047", new String[] {reqNo + "管理番号または商品コード"});
        msg.add(o);
        return msg;
      }

      String shudenflg = "";
      String weekhtdt = "";
      kanrino = "";
      String gtsimechgkbn = "";
      String gtsimeokflg = "";

      String shnKey = moyskbn + "-" + moysstdt + "-" + moysrban + "-" + bmncd + "-";
      if (StringUtils.isEmpty(kanrino) && !StringUtils.isEmpty(shncd)) {
        shnKey += shncd;
      } else {
        shnKey += kanrino;
      }

      if (tokShnMap.containsKey(shnKey)) {
        String getVal = tokShnMap.get(shnKey);
        shudenflg = getVal.split("-")[0];
        weekhtdt = getVal.split("-")[1];
        kanrino = getVal.split("-")[2];
        if (isToktg) {
          if (getVal.split("-").length >= 4) {
            gtsimechgkbn = getVal.split("-")[3];
          }
          if (getVal.split("-").length >= 5) {
            gtsimeokflg = getVal.split("-")[4];
          }
        }
      } else {
        // 変数を初期化
        sbSQL = new StringBuffer();
        iL = new ItemList();
        dbDatas = new JSONArray();
        sqlWhere = "";
        paramData = new ArrayList<String>();

        if (isToktg) {
          sqlFrom = "INATK.TOKTG_SHN ";
        } else {
          sqlFrom = "INATK.TOKSP_SHN ";
        }

        // 催し区分
        if (StringUtils.isEmpty(moyskbn)) {
          sqlWhere += "MOYSKBN=null AND ";
        } else {
          sqlWhere += "MOYSKBN=? AND ";
          paramData.add(moyskbn);
        }

        // 催し開始日
        if (StringUtils.isEmpty(moysstdt)) {
          sqlWhere += "MOYSSTDT=null AND ";
        } else {
          sqlWhere += "MOYSSTDT=? AND ";
          paramData.add(moysstdt);
        }

        // 催し連番
        if (StringUtils.isEmpty(moysrban)) {
          sqlWhere += "MOYSRBAN=null AND ";
        } else {
          sqlWhere += "MOYSRBAN=? AND ";
          paramData.add(moysrban);
        }

        // 部門コード
        if (StringUtils.isEmpty(bmncd)) {
          sqlWhere += "BMNCD=null AND ";
        } else {
          sqlWhere += "BMNCD=? AND ";
          paramData.add(bmncd);
        }

        // 管理番号 or 商品
        if (StringUtils.isEmpty(kanrino) && !StringUtils.isEmpty(shncd)) {
          sqlWhere += "SHNCD=? ";
          paramData.add(shncd);
        } else {
          sqlWhere += "KANRINO=? ";
          paramData.add(kanrino);
        }

        sbSQL.append("SELECT DISTINCT ");
        sbSQL.append("SHUDENFLG "); // 週次フラグ
        sbSQL.append(",KANRINO "); // 管理番号
        sbSQL.append(",CASE WHEN WEEKHTDT IS NULL THEN 0 ELSE WEEKHTDT END WEEKHTDT "); // 週間発注処理日
        if (isToktg) {
          sbSQL.append(",GTSIMECHGKBN "); // 月締変更理由
          sbSQL.append(",GTSIMEOKFLG "); // 月締変更許可フラグ
        }
        sbSQL.append("FROM ");
        sbSQL.append(sqlFrom);
        sbSQL.append("WHERE ");
        sbSQL.append(sqlWhere); // 入力された催しコード

        dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

        if (dbDatas.size() == 0) {
          // エラー発生箇所を保存
          JSONObject o = mu.getDbMessageObj("E20005", new String[] {reqNo});
          msg.add(o);
          return msg;
        }

        shudenflg = dbDatas.optJSONObject(0).getString("SHUDENFLG");
        weekhtdt = dbDatas.optJSONObject(0).getString("WEEKHTDT");
        kanrino = dbDatas.optJSONObject(0).getString("KANRINO");
        gtsimechgkbn = "";
        gtsimeokflg = "";

        String setVal = shudenflg + "-" + weekhtdt + "-" + kanrino;

        if (isToktg) {

          if (dbDatas.optJSONObject(0).containsKey("GTSIMECHGKBN")) {
            gtsimechgkbn = dbDatas.optJSONObject(0).getString("GTSIMECHGKBN");
          }

          if (dbDatas.optJSONObject(0).containsKey("GTSIMEOKFLG")) {
            gtsimeokflg = dbDatas.optJSONObject(0).getString("GTSIMEOKFLG");
          }
          setVal += "-" + gtsimechgkbn + "-" + gtsimeokflg;
        }
        tokShnMap.put(shnKey, setVal);
      }

      // 変数を初期化
      sbSQL = new StringBuffer();
      iL = new ItemList();
      dbDatas = new JSONArray();
      sqlWhere = "";
      paramData = new ArrayList<String>();

      if (isToktg && (gtsimechgkbn.equals("0") || StringUtils.isEmpty(gtsimechgkbn)) && !gtsimeokflg.equals("0")) {

        String khnKey = moyskbn + "-" + moysstdt + "-" + moysrban;
        if (!tokKhn.contains(khnKey)) {
          sqlFrom = "INATK.TOKTG_KHN ";

          // 催し区分
          if (StringUtils.isEmpty(moyskbn)) {
            sqlWhere += "MOYSKBN=null AND ";
          } else {
            sqlWhere += "MOYSKBN=? AND ";
            paramData.add(moyskbn);
          }

          // 催し開始日
          if (StringUtils.isEmpty(moysstdt)) {
            sqlWhere += "MOYSSTDT=null AND ";
          } else {
            sqlWhere += "MOYSSTDT=? AND ";
            paramData.add(moysstdt);
          }

          // 催し連番
          if (StringUtils.isEmpty(moysrban)) {
            sqlWhere += "MOYSRBAN=null AND ";
          } else {
            sqlWhere += "MOYSRBAN=? AND ";
            paramData.add(moysrban);
          }

          sbSQL.append("SELECT ");
          sbSQL.append("MOYSKBN "); // 件数
          sbSQL.append("FROM ");
          sbSQL.append(sqlFrom);
          sbSQL.append("WHERE ");
          sbSQL.append(sqlWhere); // 入力された催しコード
          sbSQL.append("GTSIMEFLG = '1' ");

          dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

          if (dbDatas.size() != 0) {
            // エラー発生箇所を保存
            JSONObject o = mu.getDbMessageObj("E40093", new String[] {reqNo});
            msg.add(o);
            return msg;
          }
          tokKhn.add(khnKey);
        }
      }

      // 数値の設定がない店舗は更新対象外
      String tenhtsuArr = htsuArr.split("-")[0];
      HashMap<String, String> arrMapNn = new ReportJU012Dao(JNDIname).getDigitMap(tenhtsuArr, 5, "0");
      HashMap<String, String> arrMapCsv = new HashMap<String, String>();
      arrMapCsv.put(htsuArr.split("-")[1], htsuArr.split("-")[2]);

      for (HashMap.Entry<String, String> csv : arrMapCsv.entrySet()) {

        String key = csv.getKey();
        String val = csv.getValue();

        if (!tenCdChk.contains(key)) {
          // 店舗存在チェック
          // 変数を初期化
          sbSQL = new StringBuffer();
          iL = new ItemList();
          dbDatas = new JSONArray();
          sqlWhere = "";
          paramData = new ArrayList<String>();
          sqlFrom = "INAMS.MSTTEN ";

          // 店コード
          if (StringUtils.isEmpty(key)) {
            sqlWhere += "TENCD=null AND ";
          } else {
            sqlWhere += "TENCD=? AND ";
            paramData.add(key);
          }

          sbSQL.append("SELECT ");
          sbSQL.append("TENCD "); // 件数
          sbSQL.append("FROM ");
          sbSQL.append(sqlFrom);
          sbSQL.append("WHERE ");
          sbSQL.append(sqlWhere); // 入力された催しコード
          sbSQL.append("MISEUNYOKBN = 9 AND ");
          sbSQL.append("IFNULL(UPDKBN, 0)=" + DefineReport.ValUpdkbn.NML.getVal());

          dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

          if (dbDatas.size() != 0) {
            // エラー発生箇所を保存
            JSONObject o = mu.getDbMessageObj("E20229", new String[] {reqNo});
            msg.add(o);
            return msg;
          }
          tenCdChk.add(key);
        }

        boolean err = false;

        // 登録のない店舗が設定されている場合エラー
        if (arrMapNn.containsKey(key)) {
          if (StringUtils.isEmpty(arrMapNn.get(key)) && !StringUtils.isEmpty(val)) {
            err = true;
          } else {
            arrMapNn.replace(key, val);
          }
        } else {
          err = true;
        }

        if (err) {
          // エラー発生箇所を保存
          JSONObject o = mu.getDbMessageObj("E30012", new String[] {reqNo + "発注数の設定されている店舗"});
          msg.add(o);
          return msg;
        }
      }

      htsuArr = "";
      int tenCd = 1;
      int cnt = 0;

      while (arrMapNn.size() != cnt) {
        if (arrMapNn.containsKey(String.valueOf(tenCd))) {
          cnt++;
          htsuArr += String.format("%05d", Integer.valueOf(arrMapNn.get(String.valueOf(tenCd))));
        } else {
          htsuArr += String.format("%5s", "");
        }
        tenCd++;
      }

      htsuArr = new ReportJU012Dao(JNDIname).spaceArr(htsuArr, 5);
      data.replace(TOKSP_NNDTLayout.TENHTSU_ARR.getId(), htsuArr);
      dataArray.set(i, data);
    }
    return msg;
  }

  public List<JSONObject> checkCsvShn(MessageUtility mu, JSONArray dataArray, // メイン
      JSONArray dataArrayTJTEN, // 対象除外店
      JSONArray dataArrayNNDT, // 納入日
      String hobokure, String callpage, String updKbn, String gyoNo) {

    new StringBuffer();
    JSONArray msg = new JSONArray();
    new ItemList();
    new JSONArray();
    String moyskbn = "";
    String moysstdt = "";
    String moysrban = "";
    String bmncd = "";

    new ArrayList<String>();

    reqNo = gyoNo + "行目：";
    csv = true;

    if (!updKbn.equals("A")) {
      isChange = true;
    } else {
      isChange = false;
    }

    HashMap<String, String> map = new HashMap<String, String>();
    moyskbn = dataArray.getJSONObject(0).optString(TOK_CMNLayout.MOYSKBN.getId());
    moysstdt = dataArray.getJSONObject(0).optString(TOK_CMNLayout.MOYSSTDT.getId());
    moysrban = dataArray.getJSONObject(0).optString(TOK_CMNLayout.MOYSRBAN.getId());
    bmncd = dataArray.getJSONObject(0).optString(TOK_CMNLayout.BMNCD.getId());

    map.put("MOYSKBN", moyskbn);
    map.put("MOYSSTDT", moysstdt);
    map.put("MOYSRBAN", moysrban);
    map.put("BMNCD", bmncd);

    if (!callpage.contains("Out_ReportST")) {
      tg = true;
    } else {
      st = true;
    }

    // 催し種類情報設定
    this.setMoycdInfo(moyskbn, moysstdt, moysrban, bmncd);
    // フォーム情報設定
    setFrmInfo(dataArray.getJSONObject(0).optString(TOKTG_SHNLayout.ADDSHUKBN.getId()));

    // 基本データチェック:入力値がテーブル定義と矛盾してないか確認、
    // 1.メイン
    for (int i = 0; i < dataArray.size(); i++) {
      JSONObject jo = dataArray.optJSONObject(i);

      if (isToktg) {
        for (TOKTG_SHNLayout colinf : TOKTG_SHNLayout.values()) {
          String val = StringUtils.trim(jo.optString(colinf.getId()));
          if (StringUtils.isNotEmpty(val)) {
            DataType dtype = null;
            int[] digit = null;

            String txt = colinf.getTxt() + "は";
            if (colinf.getCol().equals(TOKTG_SHNLayout.MOYSKBN.getCol()) || colinf.getCol().equals(TOKTG_SHNLayout.MOYSSTDT.getCol()) || colinf.getCol().equals(TOKTG_SHNLayout.MOYSRBAN.getCol())) {
              txt = "催しコードは";
            }

            try {
              DefineReport.InpText inpsetting = DefineReport.InpText.valueOf(colinf.getCol());
              dtype = inpsetting.getType();
              digit = new int[] {inpsetting.getDigit1(), inpsetting.getDigit2()};
            } catch (IllegalArgumentException e) {
              dtype = colinf.getDataType();
              digit = colinf.getDigit();
            }

            if (colinf.getCol().equals(TOKTG_SHNLayout.MAKERKN.getCol())) {
              dtype = DefineReport.DataType.TEXT;
              digit = new int[] {28, 0};
            } else if (colinf.getCol().equals(TOKTG_SHNLayout.SANCHIKN.getCol())) {
              dtype = DefineReport.DataType.ZEN;
              digit = new int[] {40, 0};
            } else if (colinf.getCol().equals(TOKTG_SHNLayout.NAMANETUKBN.getCol()) || colinf.getCol().equals(TOKTG_SHNLayout.KAITOFLG.getCol())
                || colinf.getCol().equals(TOKTG_SHNLayout.YOSHOKUFLG.getCol()) || colinf.getCol().equals(TOKTG_SHNLayout.HIGAWRFLG.getCol())
                || colinf.getCol().equals(TOKTG_SHNLayout.CHIRASFLG.getCol()) || colinf.getCol().equals(TOKTG_SHNLayout.HBSLIDEFLG.getCol())
                || colinf.getCol().equals(TOKTG_SHNLayout.NHSLIDEFLG.getCol()) || colinf.getCol().equals(TOKTG_SHNLayout.HTGENBAIKAFLG.getCol())
                || colinf.getCol().equals(TOKTG_SHNLayout.TKANPLUKBN.getCol()) || colinf.getCol().equals(TOKTG_SHNLayout.PLUSNDFLG.getCol())
                || colinf.getCol().equals(TOKTG_SHNLayout.TENKAIKBN.getCol()) || colinf.getCol().equals(TOKTG_SHNLayout.JSKPTNSYUKBN.getCol())
                || colinf.getCol().equals(TOKTG_SHNLayout.JSKPTNZNENMKBN.getCol()) || colinf.getCol().equals(TOKTG_SHNLayout.JSKPTNZNENWKBN.getCol())
                || colinf.getCol().equals(TOKTG_SHNLayout.YORIFLG.getCol()) || colinf.getCol().equals(TOKTG_SHNLayout.MEDAMAKBN.getCol()) || colinf.getCol().equals(TOKTG_SHNLayout.JUFLG.getCol())
                || colinf.getCol().equals(TOKTG_SHNLayout.CUTTENFLG.getCol()) || colinf.getCol().equals(TOKTG_SHNLayout.SHUDENFLG.getCol())
                || colinf.getCol().equals(TOKTG_SHNLayout.A_WRITUKBN.getCol()) || colinf.getCol().equals(TOKTG_SHNLayout.B_WRITUKBN.getCol())
                || colinf.getCol().equals(TOKTG_SHNLayout.C_WRITUKBN.getCol()) || colinf.getCol().equals(TOKTG_SHNLayout.KANRIENO.getCol())) {
              dtype = DefineReport.DataType.SUUJI;
              digit = new int[] {1, 0};
            } else if (colinf.getCol().equals(TOKTG_SHNLayout.BYCD.getCol())) {
              dtype = DefineReport.DataType.SUUJI;
              digit = new int[] {7, 0};
            }

            // ①データ型による文字種チェック
            if (!InputChecker.checkDataType(dtype, val)) {
              // エラー発生箇所を保存
              JSONObject o = mu.getDbMessageObjDataTypeErr(dtype, new String[] {reqNo + txt});
              msg.add(o);
              return msg;
            }
            // ②データ桁チェック
            if (!InputChecker.checkDataLen(dtype, val, digit)) {
              // エラー発生箇所を保存
              JSONObject o = mu.getDbMessageObjLen(dtype, new String[] {reqNo + txt});
              msg.add(o);
              return msg;
            }
          }
        }
      } else {
        for (TOKSP_SHNLayout colinf : TOKSP_SHNLayout.values()) {
          String val = StringUtils.trim(jo.optString(colinf.getId()));
          if (StringUtils.isNotEmpty(val)) {
            DataType dtype = null;
            int[] digit = null;

            String txt = colinf.getTxt() + "は";
            if (colinf.getCol().equals(TOKSP_SHNLayout.MOYSKBN.getCol()) || colinf.getCol().equals(TOKSP_SHNLayout.MOYSSTDT.getCol()) || colinf.getCol().equals(TOKSP_SHNLayout.MOYSRBAN.getCol())) {
              txt = "催しコードは";
            }

            try {
              DefineReport.InpText inpsetting = DefineReport.InpText.valueOf(colinf.getCol());
              dtype = inpsetting.getType();
              digit = new int[] {inpsetting.getDigit1(), inpsetting.getDigit2()};
            } catch (IllegalArgumentException e) {
              dtype = colinf.getDataType();
              digit = colinf.getDigit();
            }

            if (colinf.getCol().equals(TOKSP_SHNLayout.MAKERKN.getCol())) {
              dtype = DefineReport.DataType.TEXT;
              digit = new int[] {28, 0};
            } else if (colinf.getCol().equals(TOKSP_SHNLayout.SANCHIKN.getCol())) {
              dtype = DefineReport.DataType.ZEN;
              digit = new int[] {40, 0};
            } else if (colinf.getCol().equals(TOKSP_SHNLayout.NAMANETUKBN.getCol()) || colinf.getCol().equals(TOKSP_SHNLayout.KAITOFLG.getCol())
                || colinf.getCol().equals(TOKSP_SHNLayout.YOSHOKUFLG.getCol()) || colinf.getCol().equals(TOKSP_SHNLayout.HIGAWRFLG.getCol())
                || colinf.getCol().equals(TOKSP_SHNLayout.CHIRASFLG.getCol()) || colinf.getCol().equals(TOKSP_SHNLayout.HTGENBAIKAFLG.getCol())
                || colinf.getCol().equals(TOKSP_SHNLayout.TKANPLUKBN.getCol()) || colinf.getCol().equals(TOKSP_SHNLayout.PLUSNDFLG.getCol())
                || colinf.getCol().equals(TOKSP_SHNLayout.TENKAIKBN.getCol()) || colinf.getCol().equals(TOKSP_SHNLayout.JSKPTNSYUKBN.getCol())
                || colinf.getCol().equals(TOKSP_SHNLayout.JSKPTNZNENMKBN.getCol()) || colinf.getCol().equals(TOKSP_SHNLayout.JSKPTNZNENWKBN.getCol())
                || colinf.getCol().equals(TOKSP_SHNLayout.YORIFLG.getCol()) || colinf.getCol().equals(TOKSP_SHNLayout.MEDAMAKBN.getCol()) || colinf.getCol().equals(TOKSP_SHNLayout.JUFLG.getCol())
                || colinf.getCol().equals(TOKSP_SHNLayout.CUTTENFLG.getCol()) || colinf.getCol().equals(TOKSP_SHNLayout.SHUDENFLG.getCol())
                || colinf.getCol().equals(TOKSP_SHNLayout.A_WRITUKBN.getCol()) || colinf.getCol().equals(TOKSP_SHNLayout.B_WRITUKBN.getCol())
                || colinf.getCol().equals(TOKSP_SHNLayout.C_WRITUKBN.getCol()) || colinf.getCol().equals(TOKSP_SHNLayout.KANRIENO.getCol())) {
              dtype = DefineReport.DataType.SUUJI;
              digit = new int[] {1, 0};
            } else if (colinf.getCol().equals(TOKSP_SHNLayout.BYCD.getCol())) {
              dtype = DefineReport.DataType.SUUJI;
              digit = new int[] {7, 0};
            }

            // ①データ型による文字種チェック
            if (!InputChecker.checkDataType(dtype, val)) {
              // エラー発生箇所を保存
              JSONObject o = mu.getDbMessageObjDataTypeErr(dtype, new String[] {reqNo + txt});
              msg.add(o);
              return msg;
            }
            // ②データ桁チェック
            if (!InputChecker.checkDataLen(dtype, val, digit)) {
              // エラー発生箇所を保存
              JSONObject o = mu.getDbMessageObjLen(dtype, new String[] {reqNo + txt});
              msg.add(o);
              return msg;
            }
          }
        }
      }
    }

    // 2:対象除外店
    for (int i = 0; i < dataArrayTJTEN.size(); i++) {
      JSONObject jo = dataArrayTJTEN.optJSONObject(i);
      for (TOK_CMN_TJTENLayout colinf : TOK_CMN_TJTENLayout.values()) {
        String val = StringUtils.trim(jo.optString(colinf.getId()));
        if (StringUtils.isNotEmpty(val)) {
          DataType dtype = null;
          int[] digit = null;

          String txt = colinf.getTxt() + "は";
          if (colinf.getCol().equals(TOK_CMN_TJTENLayout.MOYSKBN.getCol()) || colinf.getCol().equals(TOK_CMN_TJTENLayout.MOYSSTDT.getCol())
              || colinf.getCol().equals(TOK_CMN_TJTENLayout.MOYSRBAN.getCol())) {
            txt = "催しコードは";
          }

          try {
            DefineReport.InpText inpsetting = DefineReport.InpText.valueOf(colinf.getCol());
            dtype = inpsetting.getType();
            digit = new int[] {inpsetting.getDigit1(), inpsetting.getDigit2()};
          } catch (IllegalArgumentException e) {
            dtype = colinf.getDataType();
            digit = colinf.getDigit();
          }

          // 店ランクの場合はA-Zのみ入力可能
          if (colinf.getCol().equals(TOK_CMN_TJTENLayout.TENRANK.getCol())) {
            if (!val.matches("^[A-Z]")) {
              // エラー発生箇所を保存
              JSONObject o = mu.getDbMessageObj("E11302", new String[] {reqNo + colinf.getTxt(), "", "(A～Zで入力してください)"});
              msg.add(o);
              return msg;
            }
          } else {
            // ①データ型による文字種チェック
            if (!InputChecker.checkDataType(dtype, val)) {
              // エラー発生箇所を保存
              JSONObject o = mu.getDbMessageObjDataTypeErr(dtype, new String[] {reqNo + txt});
              msg.add(o);
              return msg;
            }
          }

          // ②データ桁チェック
          if (!InputChecker.checkDataLen(dtype, val, digit)) {
            // エラー発生箇所を保存
            JSONObject o = mu.getDbMessageObjLen(dtype, new String[] {reqNo + txt});
            msg.add(o);
            return msg;
          }
        }
      }
    }

    // 2:納入情報
    for (int i = 0; i < dataArrayNNDT.size(); i++) {
      JSONObject jo = dataArrayNNDT.optJSONObject(i);
      if (isToktg) {
        for (TOKTG_NNDTLayout colinf : TOKTG_NNDTLayout.values()) {

          if (colinf.getCol().equals(TOKTG_NNDTLayout.PTNNO.getCol())) {
            continue;
          }

          String val = StringUtils.trim(jo.optString(colinf.getId()));
          if (StringUtils.isNotEmpty(val)) {
            DataType dtype = null;
            int[] digit = null;

            String txt = colinf.getTxt() + "は";
            if (colinf.getCol().equals(TOKTG_NNDTLayout.MOYSKBN.getCol()) || colinf.getCol().equals(TOKTG_NNDTLayout.MOYSSTDT.getCol()) || colinf.getCol().equals(TOKTG_NNDTLayout.MOYSRBAN.getCol())) {
              txt = "催しコードは";
            }

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
              // エラー発生箇所を保存
              JSONObject o = mu.getDbMessageObjDataTypeErr(dtype, new String[] {reqNo + txt});
              msg.add(o);
              return msg;
            }
            // ②データ桁チェック
            if (!InputChecker.checkDataLen(dtype, val, digit)) {
              // エラー発生箇所を保存
              JSONObject o = mu.getDbMessageObjLen(dtype, new String[] {reqNo + txt});
              msg.add(o);
              return msg;
            }
          }
        }
      } else {
        for (TOKSP_NNDTLayout colinf : TOKSP_NNDTLayout.values()) {

          if (colinf.getCol().equals(TOKSP_NNDTLayout.PTNNO.getCol())) {
            continue;
          }

          String val = StringUtils.trim(jo.optString(colinf.getId()));
          if (StringUtils.isNotEmpty(val)) {
            DataType dtype = null;
            int[] digit = null;

            String txt = colinf.getTxt() + "は";
            if (colinf.getCol().equals(TOKSP_NNDTLayout.MOYSKBN.getCol()) || colinf.getCol().equals(TOKSP_NNDTLayout.MOYSSTDT.getCol()) || colinf.getCol().equals(TOKSP_NNDTLayout.MOYSRBAN.getCol())) {
              txt = "催しコードは";
            }

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
              // エラー発生箇所を保存
              JSONObject o = mu.getDbMessageObjDataTypeErr(dtype, new String[] {reqNo + txt});
              msg.add(o);
              return msg;
            }
            // ②データ桁チェック
            if (!InputChecker.checkDataLen(dtype, val, digit)) {
              // エラー発生箇所を保存
              JSONObject o = mu.getDbMessageObjLen(dtype, new String[] {reqNo + txt});
              msg.add(o);
              return msg;
            }
          }
        }
      }
    }

    if (msg.size() > 0) {
      return msg;
    }

    if (isToktg) {
      TOKTG_SHNLayout[] targetCol = null;
      targetCol = new TOKTG_SHNLayout[] {TOKTG_SHNLayout.MOYSKBN, TOKTG_SHNLayout.MOYSSTDT, TOKTG_SHNLayout.MOYSRBAN, TOKTG_SHNLayout.SHNCD, TOKTG_SHNLayout.BYCD, TOKTG_SHNLayout.HBSTDT,
          TOKTG_SHNLayout.HBEDDT, TOKTG_SHNLayout.RANKNO_ADD, TOKTG_SHNLayout.BDENKBN, TOKTG_SHNLayout.WAPPNKBN, TOKTG_SHNLayout.SHUDENFLG, TOKTG_SHNLayout.POPKN};

      for (int i = 0; i < dataArray.size(); i++) {
        JSONObject jo = dataArray.optJSONObject(i);
        for (TOKTG_SHNLayout colinf : targetCol) {

          boolean conFlg = false;

          if ((colinf.getCol().equals(TOKTG_SHNLayout.HBSTDT.getCol()) || colinf.getCol().equals(TOKTG_SHNLayout.HBEDDT.getCol())) && !isToktg) {
            conFlg = true;
          }

          if (isFrm1) {
            if (colinf.getCol().equals(TOKTG_SHNLayout.RANKNO_ADD.getCol()) && !isToktg_h) {
              conFlg = true;
            }
          } else if (isFrm2) {
            if (colinf.getCol().equals(TOKTG_SHNLayout.RANKNO_ADD.getCol()) && !isToktg_h) {
              conFlg = true;
            }

            if (colinf.getCol().equals(TOKTG_SHNLayout.POPKN.getCol())) {
              String val = StringUtils.trim(jo.optString(TOKTG_SHNLayout.HBSTDT.getCol()));
              if (StringUtils.isEmpty(val)) {
                conFlg = true;
              }
              val = StringUtils.trim(jo.optString(TOKTG_SHNLayout.HBEDDT.getCol()));
              if (StringUtils.isEmpty(val)) {
                conFlg = true;
              }
            }


          } else if (isFrm3) {
            if (colinf.getCol().equals(TOKTG_SHNLayout.RANKNO_ADD.getCol()) && !isToktg_h) {
              conFlg = true;
            }
            if (colinf.getCol().equals(TOKTG_SHNLayout.POPKN.getCol())) {
              String val = StringUtils.trim(jo.optString(TOKTG_SHNLayout.HBSTDT.getCol()));
              if (StringUtils.isEmpty(val)) {
                conFlg = true;
              }
              val = StringUtils.trim(jo.optString(TOKTG_SHNLayout.HBEDDT.getCol()));
              if (StringUtils.isEmpty(val)) {
                conFlg = true;
              }
            }
          } else if (isFrm4) {
            if (colinf.getCol().equals(TOKTG_SHNLayout.RANKNO_ADD.getCol())) {
              conFlg = true;
            }
            if (colinf.getCol().equals(TOKTG_SHNLayout.WAPPNKBN.getCol()) || colinf.getCol().equals(TOKTG_SHNLayout.SHUDENFLG.getCol()) || colinf.getCol().equals(TOKTG_SHNLayout.BDENKBN.getCol())) {
              conFlg = true;
            }
          } else {
            if (colinf.getCol().equals(TOKTG_SHNLayout.RANKNO_ADD.getCol())) {
              String val = StringUtils.trim(jo.optString(TOKTG_SHNLayout.HTGENBAIKAFLG.getCol()));
              if (val.equals("0")) {
                conFlg = true;
              }
            }
            if (colinf.getCol().equals(TOKTG_SHNLayout.WAPPNKBN.getCol()) || colinf.getCol().equals(TOKTG_SHNLayout.SHUDENFLG.getCol()) || colinf.getCol().equals(TOKTG_SHNLayout.BDENKBN.getCol())) {
              conFlg = true;
            }

          }

          if (conFlg) {
            continue;
          }

          String val = StringUtils.trim(jo.optString(colinf.getId()));
          if (StringUtils.isEmpty(val)) {

            String txt = colinf.getTxt();
            if (colinf.getCol().equals(TOKTG_SHNLayout.MOYSKBN.getCol()) || colinf.getCol().equals(TOKTG_SHNLayout.MOYSSTDT.getCol()) || colinf.getCol().equals(TOKTG_SHNLayout.MOYSRBAN.getCol())) {
              txt = "催しコード";
            }

            // エラー発生箇所を保存
            JSONObject o = mu.getDbMessageObj("EX1047", new String[] {reqNo + txt});
            msg.add(o);
            return msg;
          }
        }
      }
    } else {
      TOKSP_SHNLayout[] targetCol = null;
      targetCol = new TOKSP_SHNLayout[] {TOKSP_SHNLayout.MOYSKBN, TOKSP_SHNLayout.MOYSSTDT, TOKSP_SHNLayout.MOYSRBAN, TOKSP_SHNLayout.SHNCD, TOKSP_SHNLayout.BYCD, TOKSP_SHNLayout.HBSTDT,
          TOKSP_SHNLayout.HBEDDT, TOKSP_SHNLayout.BDENKBN, TOKSP_SHNLayout.WAPPNKBN, TOKSP_SHNLayout.SHUDENFLG, TOKSP_SHNLayout.POPKN};

      for (int i = 0; i < dataArray.size(); i++) {
        JSONObject jo = dataArray.optJSONObject(i);
        for (TOKSP_SHNLayout colinf : targetCol) {

          boolean conFlg = false;

          if ((colinf.getCol().equals(TOKSP_SHNLayout.HBSTDT.getCol()) || colinf.getCol().equals(TOKSP_SHNLayout.HBEDDT.getCol())) && !isToktg) {
            conFlg = true;
          }

          if (isFrm2) {

            if (colinf.getCol().equals(TOKSP_SHNLayout.POPKN.getCol())) {
              String val = StringUtils.trim(jo.optString(TOKSP_SHNLayout.HBSTDT.getCol()));
              if (StringUtils.isEmpty(val)) {
                conFlg = true;
              }
              val = StringUtils.trim(jo.optString(TOKSP_SHNLayout.HBEDDT.getCol()));
              if (StringUtils.isEmpty(val)) {
                conFlg = true;
              }
            }

          } else if (isFrm3) {
            if (colinf.getCol().equals(TOKTG_SHNLayout.RANKNO_ADD.getCol()) && !isToktg_h) {
              conFlg = true;
            }
            if (colinf.getCol().equals(TOKTG_SHNLayout.POPKN.getCol())) {
              String val = StringUtils.trim(jo.optString(TOKTG_SHNLayout.HBSTDT.getCol()));
              if (StringUtils.isEmpty(val)) {
                conFlg = true;
              }
              val = StringUtils.trim(jo.optString(TOKTG_SHNLayout.HBEDDT.getCol()));
              if (StringUtils.isEmpty(val)) {
                conFlg = true;
              }
            }
          } else if (isFrm4) {
            if (colinf.getCol().equals(TOKTG_SHNLayout.RANKNO_ADD.getCol())) {
              conFlg = true;
            }
            if (colinf.getCol().equals(TOKTG_SHNLayout.WAPPNKBN.getCol()) || colinf.getCol().equals(TOKTG_SHNLayout.SHUDENFLG.getCol()) || colinf.getCol().equals(TOKTG_SHNLayout.BDENKBN.getCol())) {
              conFlg = true;
            }
          } else {
            if (colinf.getCol().equals(TOKTG_SHNLayout.RANKNO_ADD.getCol())) {
              String val = StringUtils.trim(jo.optString(TOKTG_SHNLayout.HTGENBAIKAFLG.getCol()));
              if (val.equals("0")) {
                conFlg = true;
              }
            }
            if (colinf.getCol().equals(TOKTG_SHNLayout.WAPPNKBN.getCol()) || colinf.getCol().equals(TOKTG_SHNLayout.SHUDENFLG.getCol()) || colinf.getCol().equals(TOKTG_SHNLayout.BDENKBN.getCol())) {
              conFlg = true;
            }

          }

          if (conFlg) {
            continue;
          }

          String val = StringUtils.trim(jo.optString(colinf.getId()));
          if (StringUtils.isEmpty(val)) {

            String txt = colinf.getTxt();
            if (colinf.getCol().equals(TOKTG_SHNLayout.MOYSKBN.getCol()) || colinf.getCol().equals(TOKTG_SHNLayout.MOYSSTDT.getCol()) || colinf.getCol().equals(TOKTG_SHNLayout.MOYSRBAN.getCol())) {
              txt = "催しコード";
            }

            // エラー発生箇所を保存
            JSONObject o = mu.getDbMessageObj("EX1047", new String[] {reqNo + txt});
            msg.add(o);
            return msg;
          }
        }
      }
    }

    JSONArray moycdData = getTOKMOYCDData(map); // 催しコード情報

    if (moycdData.size() == 0) {
      // エラー発生箇所を保存
      JSONObject o = mu.getDbMessageObj("E40011", new String[] {});
      msg.add(o);
      return msg;

    }

    return getCheckResult(dataArray, dataArrayTJTEN, dataArrayNNDT, moycdData, hobokure, mu);
  }

  public JSONObject createSqlShn(JSONArray dataArray, JSONArray dataArrayTJTEN, JSONArray dataArrayNNDT, JSONArray dataArrayHB, User userInfo, String updKbn) {

    String userId = userInfo.getId(); // ログインユーザー
    String sendBtnid = DefineReport.Button.NEW.getObj();

    if (!updKbn.equals("A")) {
      sendBtnid = DefineReport.Button.SEL_CHANGE.getObj();
    }

    // 画面モード情報設定
    this.setModeInfo(DefineReport.ID_PAGE_ST016, sendBtnid);

    return updateExec(sendBtnid, DefineReport.ID_PAGE_ST016, dataArray.getJSONObject(0).optString(TOKTG_SHNLayout.ADDSHUKBN.getId()), userId, dataArray, dataArrayTJTEN, dataArrayNNDT, dataArrayHB);
  }

  public JSONObject createSqlNnDtCsv(JSONArray dataArray, JSONArray dataArrayShn, boolean insTen, User userInfo) {
    JSONObject result = new JSONObject();

    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー
    new JSONObject();

    // DB検索用パラメータ
    ArrayList<String> paramData = new ArrayList<String>();
    StringBuffer sbSQL = new StringBuffer();
    String sqlWhere = "";
    Object[] valueData = new Object[] {};
    String values = "";
    new ItemList();
    new JSONArray();
    ArrayList<String> prmData = new ArrayList<String>();
    new ArrayList<String>();
    new ArrayList<String>();

    new JSONArray();
    new JSONArray();
    JSONArray updKeys = new JSONArray();

    for (int i = 0; i < dataArray.size(); i++) {

      JSONObject data = dataArray.getJSONObject(i);
      if (data.isEmpty()) {
        continue;
      }

      String moyskbn = data.optString(TOKSP_NNDTLayout.MOYSKBN.getId());
      String moysstdt = data.optString(TOKSP_NNDTLayout.MOYSSTDT.getId());
      String moysrban = data.optString(TOKSP_NNDTLayout.MOYSRBAN.getId());
      String bmncd = data.optString(TOKSP_NNDTLayout.BMNCD.getId());
      String kanrino = data.optString(TOKSP_NNDTLayout.KANRINO.getId());
      String nndt = data.optString(TOKSP_NNDTLayout.NNDT.getId());
      data.optString(TOKSP_NNDTLayout.TENHTSU_ARR.getId());

      isToktg = super.isTOKTG(moyskbn, moysrban); // アンケート有

      moyskbn = data.optString(TOKSP_NNDTLayout.MOYSKBN.getId());
      moysstdt = data.optString(TOKSP_NNDTLayout.MOYSSTDT.getId());
      moysrban = data.optString(TOKSP_NNDTLayout.MOYSRBAN.getId());
      bmncd = data.optString(TOKSP_NNDTLayout.BMNCD.getId());
      kanrino = data.optString(TOKSP_NNDTLayout.KANRINO.getId());
      nndt = data.optString(TOKSP_NNDTLayout.NNDT.getId());
      data.optString(TOKSP_NNDTLayout.TENHTSU_ARR.getId());

      values += String.valueOf(0 + 1);
      values += ",? ,? ,? ,? ,? ,0 ,?";
      prmData.add(moyskbn);
      prmData.add(moysstdt);
      prmData.add(moysrban);
      prmData.add(bmncd);
      prmData.add(kanrino);
      prmData.add(nndt);

      // 配列項目の作成
      String arr = getHtsuArr(data, new JSONObject(), insTen);

      int len = arr.split(":").length;

      if (len == 0) {
        values += ",null ,null ,null ";
      } else if (len == 3 && !isToktg) {
        len = 2;
      }

      for (int j = 0; j < len; j++) {
        String val = arr.split(":")[j];
        values += ",? ";
        prmData.add(val);

        if (len == 1) {
          values += ",null ";
          if (isToktg) {
            values += ",null ";
          }
          break;
        } else if (len == 2 && j == 1 && isToktg) {
          values += ",null ";
          break;
        }
      }

      valueData = ArrayUtils.add(valueData, values);
      values = "";

      if (valueData.length >= 100 || (i + 1 == dataArray.size() && valueData.length > 0)) {
        sbSQL = new StringBuffer();
        if (isToktg) {
          sbSQL.append("REPLACE INTO INATK.TOKTG_NNDT  ( ");
        } else {
          sbSQL.append("REPLACE INTO INATK.TOKSP_NNDT  ( ");
        }
        sbSQL.append(" MOYSKBN"); // 催し区分
        sbSQL.append(",MOYSSTDT"); // 催し開始日
        sbSQL.append(",MOYSRBAN"); // 催し連番
        sbSQL.append(",BMNCD"); // 部門コード
        sbSQL.append(",KANRINO"); // 管理番号
        sbSQL.append(",KANRIENO"); // 枝番
        sbSQL.append(",NNDT"); // 納入日
        sbSQL.append(",TENKAISU"); // 展開数
        sbSQL.append(",TENHTSU_ARR"); // 店発注数配列
        if (isToktg) {
          sbSQL.append(",TENCHGFLG_ARR"); // 店変更フラグ配列
        }
        sbSQL.append(", SENDFLG");// 送信区分：
        sbSQL.append(", OPERATOR "); // オペレーター：
        sbSQL.append(", ADDDT "); // 登録日：
        sbSQL.append(", UPDDT "); // 更新日：
        sbSQL.append(" ) values (" + StringUtils.join(valueData, ",") + " ");
        sbSQL.append(", " + DefineReport.Values.SENDFLG_UN.getVal() + " ");// 送信区分：
        sbSQL.append(", '" + userId + "' "); // オペレーター：
        sbSQL.append(", current_timestamp  "); // 登録日：
        sbSQL.append(", current_timestamp  "); // 更新日：
        sbSQL.append(")");

        if (DefineReport.ID_DEBUG_MODE)
          System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

        sqlList.add(sbSQL.toString());
        prmList.add(prmData);
        lblList.add("全店特売（アンケート有/無）_納入日");

        // クリア
        prmData = new ArrayList<String>();
        valueData = new Object[] {};
        values = "";
      }

      String key = moyskbn + "," + moysstdt + "," + moysrban + "," + bmncd + "," + kanrino;
      if (!updKeys.contains(key)) {
        updKeys.add(key);
      }

      if (updKeys.size() >= 100 || (i + 1 == dataArray.size() && updKeys.size() > 0)) {
        createSqlNnDtSub(updKeys, userId);
        updKeys = new JSONArray();
      }
    }

    for (int i = 0; i < dataArrayShn.size(); i++) {

      JSONObject data = dataArrayShn.getJSONObject(i);
      String popkn = data.optString(TOKSP_SHNLayout.POPKN.getId());

      if (data.isEmpty()) {
        continue;
      } else if (StringUtils.isEmpty(popkn)) {
        continue;
      }

      String moyskbn = data.optString(TOKSP_SHNLayout.MOYSKBN.getId());
      String moysstdt = data.optString(TOKSP_SHNLayout.MOYSSTDT.getId());
      String moysrban = data.optString(TOKSP_SHNLayout.MOYSRBAN.getId());
      String bmncd = data.optString(TOKSP_SHNLayout.BMNCD.getId());
      String kanrino = data.optString(TOKSP_SHNLayout.KANRINO.getId());

      // 変数を初期化
      sbSQL = new StringBuffer();
      new ItemList();
      new JSONArray();
      sqlWhere = "";
      paramData = new ArrayList<String>();

      String set = "POPKN=? ";
      paramData.add(popkn);

      // 催し区分
      if (StringUtils.isEmpty(moyskbn)) {
        sqlWhere += "MOYSKBN=null AND ";
      } else {
        sqlWhere += "MOYSKBN=? AND ";
        paramData.add(moyskbn);
      }

      // 催し開始日
      if (StringUtils.isEmpty(moysstdt)) {
        sqlWhere += "MOYSSTDT=null AND ";
      } else {
        sqlWhere += "MOYSSTDT=? AND ";
        paramData.add(moysstdt);
      }

      // 催し連番
      if (StringUtils.isEmpty(moysrban)) {
        sqlWhere += "MOYSRBAN=null AND ";
      } else {
        sqlWhere += "MOYSRBAN=? AND ";
        paramData.add(moysrban);
      }

      // 部門コード
      if (StringUtils.isEmpty(bmncd)) {
        sqlWhere += "BMNCD=null AND ";
      } else {
        sqlWhere += "BMNCD=? AND ";
        paramData.add(bmncd);
      }

      // 管理番号
      if (StringUtils.isEmpty(kanrino)) {
        sqlWhere += "KANRINO=null ";
      } else {
        sqlWhere += "KANRINO=? ";
        paramData.add(kanrino);
      }

      if (isToktg) {
        sbSQL.append("UPDATE INATK.TOKTG_SHN ");
      } else {
        sbSQL.append("UPDATE INATK.TOKSP_SHN ");
      }
      sbSQL.append("SET ");
      sbSQL.append(set);
      sbSQL.append(",SENDFLG=" + DefineReport.Values.SENDFLG_UN.getVal());
      sbSQL.append(",OPERATOR='" + userId + "'");
      sbSQL.append(",UPDDT=current timestamp ");
      sbSQL.append("WHERE ");
      sbSQL.append(sqlWhere);

      if (DefineReport.ID_DEBUG_MODE)
        System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

      sqlList.add(sbSQL.toString());
      prmList.add(paramData);
      lblList.add("全店特売（アンケート有/無）_商品");
    }

    return result;
  }

  public void createSqlNnDtSub(JSONArray updKeys, String userId) {

    // DB検索用パラメータ
    ArrayList<String> paramData = new ArrayList<String>();
    StringBuffer sbSQL = new StringBuffer();
    String sqlWhere = "";
    Object[] valueDataOt = new Object[] {};
    String values = "";
    ArrayList<String> prmDataOt = new ArrayList<String>();

    for (int j = 0; j < updKeys.size(); j++) {
      values += String.valueOf(0 + 1);
      values += ",? ,? ,? ,? ,? ,0";
      prmDataOt.add(updKeys.getString(j).split(",")[0]);
      prmDataOt.add(updKeys.getString(j).split(",")[1]);
      prmDataOt.add(updKeys.getString(j).split(",")[2]);
      prmDataOt.add(updKeys.getString(j).split(",")[3]);
      prmDataOt.add(updKeys.getString(j).split(",")[4]);
      valueDataOt = ArrayUtils.add(valueDataOt, values);
      values = "";
    }

    sbSQL = new StringBuffer();
    if (isToktg) {
      sbSQL.append("REPLACE INTO INATK.TOKTG_SHN ( ");
    } else {
      sbSQL.append("REPLACE INTO INATK.TOKSP_SHN ( ");
    }
    sbSQL.append(" MOYSKBN"); // 催し区分
    sbSQL.append(",MOYSSTDT"); // 催し開始日
    sbSQL.append(",MOYSRBAN"); // 催し連番
    sbSQL.append(",BMNCD"); // 部門コード
    sbSQL.append(",KANRINO"); // 管理番号
    sbSQL.append(",KANRIENO"); // 枝番
    sbSQL.append(",SENDFLG");// 送信区分：
    sbSQL.append(",OPERATOR "); // オペレーター：
    sbSQL.append(",ADDDT "); // 登録日：
    sbSQL.append(",UPDDT "); // 更新日：
    sbSQL.append(" ) values( " + StringUtils.join(valueDataOt, ",") + "");
    sbSQL.append(", " + DefineReport.Values.SENDFLG_UN.getVal() + " ");// 送信区分：
    sbSQL.append(", '" + userId + "' "); // オペレーター：
    sbSQL.append(", current_timestamp  "); // 登録日：
    sbSQL.append(", current_timestamp "); // 更新日：
    sbSQL.append(")");

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(prmDataOt);
    lblList.add("全店特売（アンケート有/無）_商品");

    // クリア
    prmDataOt = new ArrayList<String>();
    valueDataOt = new Object[] {};
    values = "";

    for (int j = 0; j < updKeys.size(); j++) {

      String moyskbn = updKeys.getString(j).split(",")[0];
      String moysstdt = updKeys.getString(j).split(",")[1];
      String moysrban = updKeys.getString(j).split(",")[2];
      String bmncd = updKeys.getString(j).split(",")[3];
      String kanrino = updKeys.getString(j).split(",")[4];

      // 変数を初期化
      sbSQL = new StringBuffer();
      sqlWhere = "";
      paramData = new ArrayList<String>();

      // 催し区分
      if (StringUtils.isEmpty(moyskbn)) {
        sqlWhere += "MOYSKBN=null AND ";
      } else {
        sqlWhere += "MOYSKBN=? AND ";
        paramData.add(moyskbn);
      }

      // 催し開始日
      if (StringUtils.isEmpty(moysstdt)) {
        sqlWhere += "MOYSSTDT=null AND ";
      } else {
        sqlWhere += "MOYSSTDT=? AND ";
        paramData.add(moysstdt);
      }

      // 催し連番
      if (StringUtils.isEmpty(moysrban)) {
        sqlWhere += "MOYSRBAN=null AND ";
      } else {
        sqlWhere += "MOYSRBAN=? AND ";
        paramData.add(moysrban);
      }

      // 部門コード
      if (StringUtils.isEmpty(bmncd)) {
        sqlWhere += "BMNCD=null AND ";
      } else {
        sqlWhere += "BMNCD=? AND ";
        paramData.add(bmncd);
      }

      // 管理番号
      if (StringUtils.isEmpty(kanrino)) {
        sqlWhere += "KANRINO=null ";
      } else {
        sqlWhere += "KANRINO=? ";
        paramData.add(kanrino);
      }

      if (isToktg) {
        sbSQL.append("UPDATE INATK.TOKTG_HB ");
      } else {
        sbSQL.append("UPDATE INATK.TOKSP_HB ");
      }
      sbSQL.append("SET ");
      sbSQL.append(" SENDFLG=" + DefineReport.Values.SENDFLG_UN.getVal());
      sbSQL.append(",OPERATOR='" + userId + "'");
      sbSQL.append(",UPDDT=current timestamp ");
      sbSQL.append("WHERE ");
      sbSQL.append(sqlWhere);

      if (DefineReport.ID_DEBUG_MODE)
        System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

      sqlList.add(sbSQL.toString());
      prmList.add(paramData);
      lblList.add("全店特売（アンケート有/無）_販売");

      // 変数を初期化
      sbSQL = new StringBuffer();
      if (isToktg) {
        sbSQL.append("UPDATE INATK.TOKTG_TJTEN ");
      } else {
        sbSQL.append("UPDATE INATK.TOKSP_TJTEN ");
      }
      sbSQL.append("SET ");
      sbSQL.append(" SENDFLG=" + DefineReport.Values.SENDFLG_UN.getVal());
      sbSQL.append(",OPERATOR='" + userId + "'");
      sbSQL.append(",UPDDT=current timestamp ");
      sbSQL.append("WHERE ");
      sbSQL.append(sqlWhere);

      if (DefineReport.ID_DEBUG_MODE)
        System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

      sqlList.add(sbSQL.toString());
      prmList.add(paramData);
      lblList.add("全店特売（アンケート有/無）_対象除外店");

      // 変数を初期化
      sbSQL = new StringBuffer();
      if (isToktg) {
        sbSQL.append("UPDATE INATK.TOKTG_NNDT ");
      } else {
        sbSQL.append("UPDATE INATK.TOKSP_NNDT ");
      }
      sbSQL.append("SET ");
      sbSQL.append(" SENDFLG=" + DefineReport.Values.SENDFLG_UN.getVal());
      sbSQL.append(",OPERATOR='" + userId + "'");
      sbSQL.append(",UPDDT=current timestamp ");
      sbSQL.append("WHERE ");
      sbSQL.append(sqlWhere);

      if (DefineReport.ID_DEBUG_MODE)
        System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

      sqlList.add(sbSQL.toString());
      prmList.add(paramData);
      lblList.add("全店特売（アンケート有/無）_納入日");
    }
  }

  /**
   *
   * 配列または展開数を返却
   *
   * @param moyskbn
   * @param moysstdt
   * @param moysrban
   * @param bmncd
   * @param kanrino
   * @param nndt
   * @param htsuArr
   * @return 展開数：発注数配列：店変更フラグ配列の形で返却
   */
  public String getHtsuArr(JSONObject data, JSONObject dataShn, boolean insTen) {

    String moyskbn = data.optString(TOKSP_NNDTLayout.MOYSKBN.getId());
    String moysstdt = data.optString(TOKSP_NNDTLayout.MOYSSTDT.getId());
    String moysrban = data.optString(TOKSP_NNDTLayout.MOYSRBAN.getId());
    String bmncd = data.optString(TOKSP_NNDTLayout.BMNCD.getId());
    String kanrino = data.optString(TOKSP_NNDTLayout.KANRINO.getId());
    String nndt = data.optString(TOKSP_NNDTLayout.NNDT.getId());
    String htsuArr = data.optString(TOKSP_NNDTLayout.TENHTSU_ARR.getId());

    // 格納用変数
    StringBuffer sbSQL = new StringBuffer();
    ItemList iL = new ItemList();
    JSONArray dbDatas = new JSONArray();

    // DB検索用パラメータ
    String sqlWhere = "";
    String sqlWhereNndt = "";
    String sqlFrom = "";
    ArrayList<String> paramData = new ArrayList<String>();

    String arrChg = "";

    String JNDIname = Defines.STR_JNDI_DS;

    if (isToktg) {
      sqlFrom = "INATK.TOKTG_SHN T1,INATK.TOKTG_NNDT T2 ";
    } else {
      sqlFrom = "INATK.TOKSP_SHN T1,INATK.TOKSP_NNDT T2 ";
    }

    // 催し区分
    if (StringUtils.isEmpty(moyskbn)) {
      sqlWhere += "T1.MOYSKBN=null AND ";
    } else {
      sqlWhere += "T1.MOYSKBN=? AND ";
      paramData.add(moyskbn);
    }

    // 催し開始日
    if (StringUtils.isEmpty(moysstdt)) {
      sqlWhere += "T1.MOYSSTDT=null AND ";
    } else {
      sqlWhere += "T1.MOYSSTDT=? AND ";
      paramData.add(moysstdt);
    }

    // 催し連番
    if (StringUtils.isEmpty(moysrban)) {
      sqlWhere += "T1.MOYSRBAN=null AND ";
    } else {
      sqlWhere += "T1.MOYSRBAN=? AND ";
      paramData.add(moysrban);
    }

    // 部門コード
    if (StringUtils.isEmpty(bmncd)) {
      sqlWhere += "T1.BMNCD=null AND ";
    } else {
      sqlWhere += "T1.BMNCD=? AND ";
      paramData.add(bmncd);
    }

    // 管理番号
    if (StringUtils.isEmpty(kanrino)) {
      sqlWhere += "T1.KANRINO=null AND ";
    } else {
      sqlWhere += "T1.KANRINO=? AND ";
      paramData.add(kanrino);
    }

    // 納入日
    if (StringUtils.isEmpty(nndt)) {
      sqlWhereNndt += " AND T2.NNDT=null ";
    } else {
      sqlWhereNndt += " AND T2.NNDT=? ";
      paramData.add(nndt);
    }

    sbSQL.append("SELECT ");
    sbSQL.append("T2.MOYSKBN "); // 件数
    sbSQL.append(",T2.TENHTSU_ARR "); // 件数
    if (isToktg) {
      sbSQL.append(",T2.TENCHGFLG_ARR ");
    }
    sbSQL.append("FROM ");
    sbSQL.append(sqlFrom);
    sbSQL.append("WHERE " + sqlWhere + "T1.UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal());
    sbSQL.append(" AND T1.MOYSKBN=T2.MOYSKBN ");
    sbSQL.append(" AND T1.MOYSSTDT=T2.MOYSSTDT ");
    sbSQL.append(" AND T1.MOYSRBAN=T2.MOYSRBAN ");
    sbSQL.append(" AND T1.BMNCD=T2.BMNCD ");
    sbSQL.append(" AND T1.KANRINO=T2.KANRINO ");
    sbSQL.append(" AND T1.KANRIENO=T2.KANRIENO ");
    sbSQL.append(sqlWhereNndt); // 入力された催しコード

    dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

    // 数値の設定がない店舗は更新対象外
    HashMap<String, String> arrMapNn = new HashMap<String, String>();
    HashMap<String, String> arrMapNnChg = new HashMap<String, String>();
    String getArr = "";
    if (dbDatas.size() != 0 && dbDatas.getJSONObject(0).containsKey("MOYSKBN")) {

      getArr = dbDatas.getJSONObject(0).containsKey("TENHTSU_ARR") ? dbDatas.getJSONObject(0).getString("TENHTSU_ARR") : "";

      arrMapNn = new ReportJU012Dao(JNDIname).getDigitMap(getArr, 5, "0");
      if (isToktg) {
        getArr = dbDatas.getJSONObject(0).containsKey("TENCHGFLG_ARR") ? dbDatas.getJSONObject(0).getString("TENCHGFLG_ARR") : "";
        arrMapNnChg = new ReportJU012Dao(JNDIname).getDigitMap(getArr, 1, "0");
      }
    }

    HashMap<String, String> arrMapCsv = new ReportJU012Dao(JNDIname).getDigitMap(htsuArr, 5, "0");

    if (isToktg) {

      int ten = 0;

      if (StringUtils.isEmpty(getArr) && dbDatas.size() == 0) {
        ten = 400;
      } else if (data.size() != 0) {
        // 再展開の必要有無

        String kanrieno = data.optString(TOKTG_NNDTLayout.KANRIENO.getId());
        String ptnno = data.optString(TOKTG_NNDTLayout.PTNNO.getId());
        String htasu = data.optString(TOKTG_NNDTLayout.HTASU.getId());
        String tenkaikbn = dataShn.optString(TOKTG_SHNLayout.TENKAIKBN.getId());
        String syukbn = dataShn.optString(TOKTG_SHNLayout.JSKPTNSYUKBN.getId());
        String jskptnznenmkbn = dataShn.optString(TOKTG_SHNLayout.JSKPTNZNENMKBN.getId());
        String jskptnznenwkbn = dataShn.optString(TOKTG_SHNLayout.JSKPTNZNENWKBN.getId());
        String arr = dataShn.optString(TOKTG_SHNLayout.TENRANK_ARR.getId());

        dbDatas = new ReportBM015Dao(JNDIname).getST021UpdChk(moyskbn, moysstdt, moysrban, bmncd, kanrino, kanrieno, arr, tenkaikbn, syukbn, jskptnznenmkbn, jskptnznenwkbn, nndt, ptnno, htasu);

        if (dbDatas.size() == 0) {
          ten = 400;
        }
      }

      for (int i = 1; i <= ten; i++) {
        arrChg += "1";
      }
    }

    for (HashMap.Entry<String, String> csv : arrMapCsv.entrySet()) {

      String key = csv.getKey();
      String val = csv.getValue();

      // 値の入力がない場合次店舗へ
      if (StringUtils.isEmpty(val)) {
        continue;
      }

      if (isToktg && Integer.valueOf(val) >= 0) {
        if (arrMapNnChg.containsKey(key)) {
          arrMapNnChg.replace(key, "1");
        } else if (insTen) {
          arrMapNnChg.put(key, "1");
        }
      }

      if (arrMapNn.containsKey(key)) {
        arrMapNn.replace(key, val);

        // 更新時の追加の店舗を認めるか
      } else if (insTen && !arrMapNn.containsKey(key)) {
        arrMapNn.put(key, val);
      }
    }

    // 今回対象外となった店舗があった場合
    ArrayList<String> delTen = new ArrayList<String>();
    for (HashMap.Entry<String, String> del : arrMapNn.entrySet()) {
      String key = del.getKey();
      if (!arrMapCsv.containsKey(key)) {
        delTen.add(key);
      }
    }

    for (int i = 0; i < delTen.size(); i++) {
      String key = delTen.get(i);
      arrMapNn.remove(key);
      if (isToktg) {
        if (arrMapNnChg.containsKey(key)) {
          arrMapNnChg.replace(key, "1");
        } else {
          arrMapNnChg.put(key, "1");
        }
      }
    }

    String arr = "";
    int tenkaisu = 0;
    int tencd = 0;

    for (int i = 1; i <= 400; i++) {

      String key = String.valueOf(i);
      String val = "";

      if (arrMapCsv.containsKey(key)) {
        val = arrMapCsv.get(key);

        if (!StringUtils.isEmpty(val)) {
          tenkaisu += Integer.valueOf(val);
        }

        for (int j = tencd + 1; j < Integer.valueOf(key); j++) {
          arr += String.format("%5s", "");
        }
        arr += String.format("%05d", Integer.valueOf(val));
        tencd = Integer.valueOf(key);

      }
    }
    arr = new ReportJU012Dao(JNDIname).spaceArr(arr, 5);

    if (StringUtils.isEmpty(arrChg)) {
      tencd = 0;
      for (int i = 1; i <= 400; i++) {

        String key = String.valueOf(i);
        if (arrMapNnChg.containsKey(key)) {
          arrMapNnChg.get(key);

          for (int j = tencd + 1; j < Integer.valueOf(key); j++) {
            arrChg += String.format("%1s", "");
          }
          arrChg += "1";
          tencd = Integer.valueOf(key);
        }
      }
      arrChg = new ReportJU012Dao(JNDIname).spaceArr(arrChg, 1);
    }

    return String.valueOf(tenkaisu) + ":" + arr + ":" + arrChg;
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
  public boolean checkMstExist(String outobj, String value, JSONObject obj) {
    // 関連情報取得
    ItemList iL = new ItemList();
    // 配列準備
    ArrayList<String> paramData = new ArrayList<String>();
    String sqlcommand = "";

    String tbl = "";
    String col = "";
    String rep = "";
    String szWhere = "";
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

    // チラシのみ部門
    if (outobj.equals("TOKCHIRASBMN")) {
      tbl = "INATK.TOKCHIRASBMN";
      col = "BMNCD";
      szWhere += " and MOYSKBN = " + obj.optString("MOYSKBN");
      szWhere += " and MOYSSTDT= " + obj.optString("MOYSSTDT");
      szWhere += " and MOYSRBAN= " + obj.optString("MOYSRBAN");
    }

    if (tbl.length() > 0 && col.length() > 0) {
      if (paramData.size() > 0 && rep.length() > 0) {
        sqlcommand = DefineReport.ID_SQL_CHK_TBL.replace("@T", tbl).replaceAll("@C", col).replace("?", rep) + szWhere;
      } else {
        paramData.add(value);
        sqlcommand = DefineReport.ID_SQL_CHK_TBL.replace("@T", tbl).replaceAll("@C", col) + szWhere;
      }

      @SuppressWarnings("static-access")
      JSONArray array = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
      if (array.size() > 0 && NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) > 0) {
        return true;
      }
    }
    return false;
  }

  /**
   * 全店特売(アンケート有/無)_部門 SQL作成処理
   *
   * @param userId
   * @param data
   *
   * @throws Exception
   */
  private JSONObject createSqlTOK_CMN_BMN(String userId, JSONObject data, SqlType sql, boolean isTOKTG) {
    JSONObject result = new JSONObject();

    String[] notTarget =
        new String[] {TOK_CMN_BMNLayout.UPDKBN.getId(), TOK_CMN_BMNLayout.SENDFLG.getId(), TOK_CMN_BMNLayout.OPERATOR.getId(), TOK_CMN_BMNLayout.ADDDT.getId(), TOK_CMN_BMNLayout.UPDDT.getId()};
    this.getIds(TOK_CMNLayout.values());

    // 更新情報
    ArrayList<String> prmData = new ArrayList<String>();
    // 基本Merge文
    StringBuffer sbSQL;
    sbSQL = new StringBuffer();
    if (isTOKTG) {
      sbSQL.append("INSERT into INATK.TOKTG_BMN ");
    } else {
      sbSQL.append("INSERT into INATK.TOKSP_BMN ");
    }
    sbSQL.append("(  ");
    for (TOK_CMN_BMNLayout itm : TOK_CMN_BMNLayout.values()) {
      if (itm.getNo() > 1) {
        sbSQL.append(",");
      }
      sbSQL.append(itm.getCol());
    }
    sbSQL.append(") SELECT ");
    for (TOK_CMN_BMNLayout itm : TOK_CMN_BMNLayout.values()) {
      if (itm.getNo() > 1) {
        sbSQL.append(",");
      }
      sbSQL.append(itm.getCol());
    }
    sbSQL.append(" FROM ( SELECT ");
    // キー情報はロックのため後で追加する
    for (TOK_CMNLayout itm : TOK_CMNLayout.values()) {
      if (itm.getNo() > 1) {
        sbSQL.append(",");
      }
      sbSQL.append("cast(? as " + itm.getTyp() + ") as " + itm.getCol());

    }
    sbSQL.append(" ," + DefineReport.ValUpdkbn.NML.getVal() + " AS UPDKBN "); // 更新区分
    sbSQL.append(" ," + DefineReport.Values.SENDFLG_UN.getVal() + " AS SENDFLG"); // 送信フラグ
    sbSQL.append(" ,'" + userId + "' AS OPERATOR "); // オペレータ
    sbSQL.append(" ,CURRENT_TIMESTAMP AS ADDDT "); // 登録日
    sbSQL.append(" ,CURRENT_TIMESTAMP AS UPDDT "); // 更新日
    sbSQL.append("FROM (SELECT 1 DUMMY) AS DUMMY ");
    sbSQL.append(") AS DUMMY ON DUPLICATE KEY UPDATE ");
    for (TOK_CMN_BMNLayout itm : TOK_CMN_BMNLayout.values()) {
      if (itm.getNo() > 1) {
        sbSQL.append(",");
      }
      sbSQL.append(itm.getCol() + "=VALUES(" + itm.getCol() + ") ");
    }

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println("/* " + this.getClass().getName() + "*/ " + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("全店特売(アンケート有/無)_部門");
    return result;
  }

  /**
   * 全店特売(アンケート有/無)_部門 SQL作成処理(DELETE)
   *
   * @param userId
   * @param data
   *
   * @throws Exception
   */
  private JSONObject createSqlTOK_CMN_BMN_DEL(String userId, JSONObject data, SqlType sql, boolean isTOKTG) {
    JSONObject result = new JSONObject();

    String szTableBmn = isTOKTG ? "INATK.TOKTG_BMN" : "INATK.TOKSP_BMN";
    String szTableShn = isTOKTG ? "INATK.TOKTG_SHN" : "INATK.TOKSP_SHN";

    // 更新情報
    ArrayList<String> prmData = new ArrayList<String>();
    // 基本Merge文
    StringBuffer sbSQL;
    sbSQL = new StringBuffer();
    sbSQL.append("with VAL as (  ");
    sbSQL.append("select ");
    sbSQL.append("MOYSKBN,MOYSSTDT,MOYSRBAN,BMNCD ");
    sbSQL.append(",sum(case when KANRINO = " + data.optString(TOK_CMNLayout.KANRINO.getId()) + " then 0 else 1 end) as CNT ");
    sbSQL.append("from " + szTableShn + "  where ");
    sbSQL.append("MOYSKBN  = " + data.optString(TOK_CMNLayout.MOYSKBN.getId()) + " ");
    sbSQL.append("and MOYSSTDT = " + data.optString(TOK_CMNLayout.MOYSSTDT.getId()) + " ");
    sbSQL.append("and MOYSRBAN = " + data.optString(TOK_CMNLayout.MOYSRBAN.getId()) + " ");
    sbSQL.append("and BMNCD    = " + data.optString(TOK_CMNLayout.BMNCD.getId()) + " ");
    sbSQL.append("group by MOYSKBN,MOYSSTDT,MOYSRBAN,BMNCD ) ");

    sbSQL.append("update " + szTableBmn + " as MT ");
    sbSQL.append(",VAL as T1 ");
    sbSQL.append("set ");
    sbSQL.append("UPDKBN = " + DefineReport.ValUpdkbn.DEL.getVal() + " "); // 更新区分
    sbSQL.append(",SENDFLG = " + DefineReport.Values.SENDFLG_UN.getVal() + " "); // 送信フラグ
    sbSQL.append(",OPERATOR= '" + userId + "' "); // オペレータ
    sbSQL.append(",UPDDT = current_timestamp "); // 更新日
    sbSQL.append("where ");
    sbSQL.append("MT.MOYSKBN = T1.MOYSKBN ");
    sbSQL.append("and MT.MOYSSTDT = T1.MOYSSTDT ");
    sbSQL.append("and MT.MOYSRBAN = T1.MOYSRBAN ");
    sbSQL.append("and MT.BMNCD = T1.BMNCD ");
    sbSQL.append("and T1.CNT = 0 ");

    if (DefineReport.ID_DEBUG_MODE) {
      System.out.println("/* " + this.getClass().getName() + "*/ " + sbSQL.toString());
    }
    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("全店特売(アンケート有/無)_部門");
    return result;
  }

  /**
   * 全店特売(アンケート有)_商品 SQL作成処理
   *
   * @param userId
   * @param data
   *
   * @throws Exception
   */
  private JSONObject createSqlTOKTG_SHN(String userId, JSONObject data, SqlType sql) {
    JSONObject result = new JSONObject();

    String[] notTarget = new String[] {TOKTG_SHNLayout.UPDKBN.getId(), TOKTG_SHNLayout.SENDFLG.getId(), TOKTG_SHNLayout.OPERATOR.getId(), TOKTG_SHNLayout.ADDDT.getId(), TOKTG_SHNLayout.UPDDT.getId()};
    String[] keys = this.getIds(TOK_CMNLayout.values());

    // 更新情報
    ArrayList<String> prmData = new ArrayList<String>();

    // 基本Merge文
    StringBuffer sbSQL;
    sbSQL = new StringBuffer();
    sbSQL.append("replace into INATK.TOKTG_SHN ( ");
    for (TOKTG_SHNLayout itm : TOKTG_SHNLayout.values()) {
      if (ArrayUtils.contains(notTarget, itm.getId())) {
        continue;
      } // パラメータ不要
      if (itm.getNo() > 1) {
        sbSQL.append(",");
      }
      sbSQL.append(itm.getCol());
    }
    sbSQL.append(",UPDKBN ");
    sbSQL.append(",SENDFLG ");
    sbSQL.append(",OPERATOR ");
    sbSQL.append(",ADDDT ");
    sbSQL.append(",UPDDT ");
    sbSQL.append(")values(");
    // キー情報はロックのため後で追加する
    for (TOK_CMNLayout itm : TOK_CMNLayout.values()) {
      String value = StringUtils.strip(data.optString(itm.getId()));
      if (itm.getNo() > 1) {
        sbSQL.append(",");
      }
      if (StringUtils.isNotEmpty(value)) {
        prmData.add(value);
        sbSQL.append("cast(? as " + itm.getTyp() + ") ");
      } else if ((itm.getNo() == 5 || itm.getNo() == 6) && !StringUtils.isNotEmpty(value)) {
        sbSQL.append("0 ");
      } else {
        sbSQL.append("null ");
      }
    }
    for (TOKTG_SHNLayout itm : TOKTG_SHNLayout.values()) {
      if (ArrayUtils.contains(notTarget, itm.getId())) {
        continue;
      } // パラメータ不要
      if (ArrayUtils.contains(keys, itm.getId())) {
        continue;
      } // 上記で実施
      if (data.containsKey(itm.getId())) {
        String value = StringUtils.strip(data.optString(itm.getId()));
        // 配列系項目はスペースを取り除かない
        if (itm.getCol().equals(TOKTG_SHNLayout.TENRANK_ARR.getCol())) {
          value = new ReportJU012Dao(JNDIname).spaceArr(data.optString(itm.getId()), 1);
        } else if (itm.getCol().equals(TOKTG_SHNLayout.GENKAAM_MAE.getCol()) && data.containsKey(TOKTG_SHNLayout.TKANPLUKBN.getId())) {
          String tkanplukbn = StringUtils.strip(data.optString(TOKTG_SHNLayout.TKANPLUKBN.getId()));
          if (tkanplukbn.equals("2") && StringUtils.isEmpty(value)) {
            value = "0";
          }
        }
        if (StringUtils.isNotEmpty(value)) {
          prmData.add(value);
          sbSQL.append(",cast(? as " + itm.getTyp() + ") ");
        } else {
          sbSQL.append(",null ");
        }
      } else {
        sbSQL.append(",null ");
      }
    }
    sbSQL.append(" ," + DefineReport.ValUpdkbn.NML.getVal()); // 更新区分
    sbSQL.append(" ," + DefineReport.Values.SENDFLG_UN.getVal()); // 送信フラグ
    sbSQL.append(" ,'" + userId + "'"); // オペレータ
    sbSQL.append(" ,current_timestamp"); // 登録日
    sbSQL.append(" ,current_timestamp"); // 更新日
    sbSQL.append(")");



    if (DefineReport.ID_DEBUG_MODE) {
      System.out.println("/* " + this.getClass().getName() + "*/ " + sbSQL.toString());
    }
    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("全店特売(アンケート有)_商品");
    return result;
  }

  /**
   * 全店特売(アンケート無)_商品 SQL作成処理
   *
   * @param userId
   * @param data
   *
   * @throws Exception
   */
  private JSONObject createSqlTOKSP_SHN(String userId, JSONObject data, SqlType sql) {
    JSONObject result = new JSONObject();

    String[] notTarget = new String[] {TOKSP_SHNLayout.UPDKBN.getId(), TOKSP_SHNLayout.SENDFLG.getId(), TOKSP_SHNLayout.OPERATOR.getId(), TOKSP_SHNLayout.ADDDT.getId(), TOKSP_SHNLayout.UPDDT.getId()};
    String[] keys = this.getIds(TOK_CMNLayout.values());

    // 更新情報
    ArrayList<String> prmData = new ArrayList<String>();

    // 基本Merge文
    StringBuffer sbSQL;
    sbSQL = new StringBuffer();
    sbSQL.append("INSERT INTO INATK.TOKSP_SHN ");
    sbSQL.append("( ");
    for (TOKSP_SHNLayout itm : TOKSP_SHNLayout.values()) {
      if (itm.getNo() > 1) {
        sbSQL.append(",");
      }
      sbSQL.append(itm.getCol());
    }
    sbSQL.append(") SELECT ");
    for (TOKSP_SHNLayout itm : TOKSP_SHNLayout.values()) {
      if (itm.getNo() > 1) {
        sbSQL.append(",");
      }
      sbSQL.append(itm.getCol());
    }
    sbSQL.append(" FROM ( SELECT ");

    // キー情報はロックのため後で追加する
    for (TOK_CMNLayout itm : TOK_CMNLayout.values()) {
      if (itm.getNo() > 1) {
        sbSQL.append(",");
      }
      sbSQL.append("cast(? as " + itm.getTyp() + ") as " + itm.getCol());
    }
    for (TOKSP_SHNLayout itm : TOKSP_SHNLayout.values()) {
      if (ArrayUtils.contains(notTarget, itm.getId())) {
        continue;
      } // パラメータ不要
      if (ArrayUtils.contains(keys, itm.getId())) {
        continue;
      } // 上記で実施
      if (data.containsKey(itm.getId())) {
        String value = StringUtils.strip(data.optString(itm.getId()));

        // 配列系項目はスペースを取り除かない
        if (itm.getCol().equals(TOKSP_SHNLayout.TENRANK_ARR.getCol())) {
          value = new ReportJU012Dao(JNDIname).spaceArr(data.optString(itm.getId()), 1);
        } else if (itm.getCol().equals(TOKSP_SHNLayout.GENKAAM_MAE.getCol()) && data.containsKey(TOKSP_SHNLayout.TKANPLUKBN.getId())) {
          String tkanplukbn = StringUtils.strip(data.optString(TOKSP_SHNLayout.TKANPLUKBN.getId()));
          if (tkanplukbn.equals("2") && StringUtils.isEmpty(value)) {
            value = "0";
          }
        }

        if (StringUtils.isNotEmpty(value)) {
          prmData.add(value);
          sbSQL.append(",cast(? as " + itm.getTyp() + ") as " + itm.getCol());
        } else {
          sbSQL.append(",null as " + itm.getCol());
        }
      } else {
        sbSQL.append(",null as " + itm.getCol());
      }
    }
    sbSQL.append(" ," + DefineReport.ValUpdkbn.NML.getVal() + " AS UPDKBN "); // F99: 更新区分
    sbSQL.append(" ," + DefineReport.Values.SENDFLG_UN.getVal() + " AS SENDFLG "); // F100: 送信フラグ
    sbSQL.append(" ,'" + userId + "' AS OPERATOR"); // F101: オペレータ
    sbSQL.append(" ,CURRENT_TIMESTAMP AS ADDDT "); // F102: 登録日
    sbSQL.append(" ,CURRENT_TIMESTAMP AS UPDDT "); // F103: 更新日
    sbSQL.append("FROM (SELECT 1 DUMMY) AS DUMMY ");
    sbSQL.append(") AS DUMMY ON DUPLICATE KEY UPDATE ");
    for (TOKSP_SHNLayout itm : TOKSP_SHNLayout.values()) {
      if (itm.getNo() > 1) {
        sbSQL.append(",");
      }
      sbSQL.append(itm.getCol() + " = VALUES(" + itm.getCol() + ") ");
    }


    if (DefineReport.ID_DEBUG_MODE) {
      System.out.println("/* " + this.getClass().getName() + "*/ " + sbSQL.toString());
    }
    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("全店特売(アンケート無)_商品");
    return result;
  }

  /**
   * 全店特売(アンケート有/無)_商品 SQL作成処理(DELETE)
   *
   * @param userId
   * @param data
   *
   * @throws Exception
   */
  private JSONObject createSqlTOK_CMN_SHN_DEL(String userId, JSONObject data, SqlType sql, boolean isTOKTG) {
    JSONObject result = new JSONObject();

    // 更新情報
    ArrayList<String> prmData = new ArrayList<String>();

    String szTableShn = isTOKTG ? "INATK.TOKTG_SHN" : "INATK.TOKSP_SHN";

    // 基本Merge文
    StringBuffer sbSQL;
    sbSQL = new StringBuffer();
    sbSQL.append("with VAL as ( ");
    sbSQL.append("select ");
    for (TOK_CMNLayout itm : TOK_CMNLayout.values()) {
      if (itm.getNo() > 1) {
        sbSQL.append(",");
      }
      sbSQL.append("cast(" + data.optString(itm.getId()) + " as " + itm.getTyp() + ") as " + itm.getCol() + " ");
    }
    sbSQL.append(") ");
    sbSQL.append("update " + szTableShn + " as MT ");
    sbSQL.append(",VAL as T1 ");
    sbSQL.append("set ");
    if (SqlType.DEL.getVal() == sql.getVal()) {
      sbSQL.append("UPDKBN = " + DefineReport.ValUpdkbn.DEL.getVal() + " "); // 更新区分
      sbSQL.append(",SENDFLG = " + DefineReport.Values.SENDFLG_UN.getVal() + " "); // 送信フラグ
      sbSQL.append(",OPERATOR = '" + userId + "' "); // オペレータ
      sbSQL.append(",UPDDT = current_timestamp "); // 更新日
    }
    sbSQL.append("where ");
    sbSQL.append("MT.MOYSKBN = T1.MOYSKBN ");
    sbSQL.append("and MT.MOYSSTDT = T1.MOYSSTDT ");
    sbSQL.append("and MT.MOYSRBAN = T1.MOYSRBAN ");
    sbSQL.append("and MT.BMNCD = T1.BMNCD ");
    sbSQL.append("and MT.KANRINO = T1.KANRINO ");

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("全店特売(アンケート有/無)_商品");
    return result;
  }

  /**
   * 全店特売(アンケート有/無)_除外店 SQL作成処理
   *
   * @param userId
   * @param data
   *
   * @throws Exception
   */
  private JSONObject createSqlTOK_CMN_TJTEN(String userId, JSONArray data, SqlType sql, boolean isTOKTG) {// ks_point
    JSONObject result = new JSONObject();
    String[] notTarget = new String[] {TOK_CMN_TJTENLayout.SENDFLG.getId(), TOK_CMN_TJTENLayout.OPERATOR.getId(), TOK_CMN_TJTENLayout.ADDDT.getId(), TOK_CMN_TJTENLayout.UPDDT.getId()};


    // 更新情報
    ArrayList<String> prmData = new ArrayList<String>();
    String table = "", colList = "", rows = "", values = "";
    System.out.print("data > " + data + "\n");


    if (isTOKTG) {
      table = "INATK.TOKTG_TJTEN";
    } else {
      table = "INATK.TOKSP_TJTEN";
    }

    for (TOK_CMN_TJTENLayout itm : TOK_CMN_TJTENLayout.values()) {
      colList += "," + itm.getCol();
    }
    colList = StringUtils.removeStart(colList, ",");

    for (int j = 0; j < data.size(); j++) {
      values = "";
      for (TOK_CMN_TJTENLayout itm : TOK_CMN_TJTENLayout.values()) {
        if (ArrayUtils.contains(notTarget, itm.getId())) {
          continue;
        } // パラメータ不要
        String val = data.optJSONObject(j).optString(itm.getId());
        if (StringUtils.isEmpty(val.trim()) && j <= 7) {
          values += ",0";
        } else if (StringUtils.isEmpty(val.trim()) && j >= 8) {
          values += ",null";
        } else {
          values += ",cast(? as " + itm.getTyp() + " ) ";
          prmData.add(val);
        }
      }

      for (TOK_CMN_TJTENLayout itm : TOK_CMN_TJTENLayout.values()) {
        if (!ArrayUtils.contains(notTarget, itm.getId())) {
          continue;
        } // パラメータ不要
        if (itm.getId().equals(TOK_CMN_TJTENLayout.SENDFLG.getId())) {
          values += "," + DefineReport.Values.SENDFLG_UN.getVal() + " ";
        }
        if (itm.getId().equals(TOK_CMN_TJTENLayout.OPERATOR.getId())) {
          values += ",'" + userId + "' ";
        }
        if (itm.getId().equals(TOK_CMN_TJTENLayout.ADDDT.getId())) {
          values += ",(select * from(select case when count(*) = 0 or ifnull(ADDDT,0) = 0 then current_timestamp ";
          values += "else ADDDT end ADDDT from " + table + " ";
          for (TOK_CMNLayout qur : TOK_CMNLayout.values()) {
            String val = data.optJSONObject(j).optString(qur.getId());
            if (qur.getNo() == 1) {
              values += "where ";
            } else {
              values += "and ";
            }
            if (StringUtils.isEmpty(val)) {
              values += qur.getCol() + " = null ";
            } else {
              values += qur.getCol() + " = ? ";
              prmData.add(val);
            }
          }
          values += ") as T1 ) ";

        }
        if (itm.getId().equals(TOK_CMN_TJTENLayout.UPDDT.getId())) {
          values += ",current_timestamp";
        }

      }
      rows += ",( " + StringUtils.removeStart(values, ",") + " ) ";
    }
    rows = StringUtils.removeStart(rows, ",");

    // 対象データが存在しない場合
    if (StringUtils.isEmpty(rows)) {
      return result;
    }

    // 基本Merge文
    StringBuffer sbSQL;
    sbSQL = new StringBuffer();

    sbSQL.append("replace into " + table + "( ");
    sbSQL.append(colList + " ) ");
    sbSQL.append("values " + rows);


    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("全店特売(アンケート有/無)_除外店");
    return result;
  }

  /**
   * 全店特売(アンケート有)_納入日 SQL作成処理
   *
   * @param userId
   * @param data
   *
   * @throws Exception
   */
  protected JSONObject createSqlTOKTG_NNDT(String userId, JSONArray dataArray, JSONObject data, SqlType sql) {
    JSONObject result = new JSONObject();

    // まったく使用しない項目
    String[] notUse = new String[] {}; // new String[]{TOKTG_NNDTLayout.bat_ctlflg01.getId()};
    // 直接値設定する項目
    String[] notTarget = new String[] {TOKTG_NNDTLayout.OPERATOR.getId(), TOKTG_NNDTLayout.ADDDT.getId(), TOKTG_NNDTLayout.UPDDT.getId()};
    String[] keys = this.getIds(TOK_CMNLayout.values());

    // 更新情報
    ArrayList<String> prmData = new ArrayList<String>();
    String values = "", names = "", rows = "";

    for (int j = 0; j < dataArray.size(); j++) {

      String arr = "";
      String tenkaisu = "";
      String tenchg = "";

      values = "";
      names = "";
      for (TOKTG_NNDTLayout itm : TOKTG_NNDTLayout.values()) {
        if (ArrayUtils.contains(notUse, itm.getId())) {
          continue;
        } // 不要
        if (ArrayUtils.contains(notTarget, itm.getId())) {
          continue;
        } // パラメータ不要
        if (ArrayUtils.contains(keys, itm.getId())) {
          continue;
        } // 共通で実施

        String col = itm.getCol();
        String val = StringUtils.trim(dataArray.optJSONObject(j).optString(itm.getId()));

        if (itm.getId().equals(TOKTG_NNDTLayout.TENHTSU_ARR.getId())) {
          val = getHtsuArr(dataArray.optJSONObject(j), data, true);

          if (!StringUtils.isEmpty(val)) {
            for (int jj = 0; jj < val.split(":").length; jj++) {
              if (jj == 0) {
                tenkaisu = val.split(":")[jj];
              } else if (jj == 1 && !StringUtils.isEmpty(val.split(":")[jj])) {
                arr = val.split(":")[jj];
              } else if (jj == 2 && !StringUtils.isEmpty(val.split(":")[jj])) {
                tenchg = val.split(":")[jj];
              }
            }
          }
          val = arr;
        } else if (itm.getId().equals(TOKTG_NNDTLayout.TENKAISU.getId())) {
          val = tenkaisu;
        } else if (itm.getId().equals(TOKTG_NNDTLayout.TENCHGFLG_ARR.getId())) {
          val = tenchg;
        }

        if (StringUtils.isEmpty(val)) {
          values += ", null";
        } else {
          prmData.add(val);
          values += ", cast(? as char(" + MessageUtility.getDefByteLen(val) + "))";
        }
        names += ", " + col;
      }
      rows += ",(" + StringUtils.removeStart(values, ",") + ")";
    }
    rows = StringUtils.removeStart(rows, ",");
    names = StringUtils.removeStart(names, ",");

    // 基本Merge文
    StringBuffer sbSQL;
    sbSQL = new StringBuffer();
    sbSQL.append("replace into INATK.TOKTG_NNDT ( ");

    for (TOKTG_NNDTLayout itm : TOKTG_NNDTLayout.values()) {
      if (ArrayUtils.contains(notUse, itm.getId())) {
        continue;
      } // 不要
      if (itm.getNo() > 1) {
        sbSQL.append(",");
      }
      sbSQL.append(itm.getCol());
    }
    sbSQL.append(")values(");
    for (TOK_CMNLayout itm : TOK_CMNLayout.values()) {
      if (itm.getNo() > 1) {
        sbSQL.append(",");
      }
      sbSQL.append("cast(? as " + itm.getTyp() + ") ");
    }
    sbSQL.append(" ," + DefineReport.Values.SENDFLG_UN.getVal()); // 送信フラグ
    sbSQL.append(" ,'" + userId + "'"); // オペレータ
    sbSQL.append(" ,current_timestamp"); // 登録日
    sbSQL.append(" ,current_timestamp"); // 更新日
    sbSQL.append(")");
    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("全店特売(アンケート有)_納入日");
    return result;
  }

  /**
   * 全店特売(アンケート無)_納入日 SQL作成処理
   *
   * @param userId
   * @param data
   *
   * @throws Exception
   */
  protected JSONObject createSqlTOKSP_NNDT(String userId, JSONArray dataArray, JSONObject data, SqlType sql) {
    JSONObject result = new JSONObject();

    String[] notTarget = new String[] {TOKSP_NNDTLayout.OPERATOR.getId(), TOKSP_NNDTLayout.ADDDT.getId(), TOKSP_NNDTLayout.UPDDT.getId()};
    String[] keys = this.getIds(TOK_CMNLayout.values());

    // 更新情報
    ArrayList<String> prmData = new ArrayList<String>();
    String values = "", names = "", rows = "";

    for (int j = 0; j < dataArray.size(); j++) {

      String arr = "";
      String tenkaisu = "";

      values = "";
      names = "";
      for (TOKSP_NNDTLayout itm : TOKSP_NNDTLayout.values()) {
        if (ArrayUtils.contains(notTarget, itm.getId())) {
          continue;
        } // パラメータ不要
        if (ArrayUtils.contains(keys, itm.getId())) {
          continue;
        } // 共通で実施

        String col = itm.getCol();
        String val = StringUtils.trim(dataArray.optJSONObject(j).optString(itm.getId()));

        if (itm.getId().equals(TOKSP_NNDTLayout.TENHTSU_ARR.getId())) {
          val = getHtsuArr(dataArray.optJSONObject(j), data, true);

          if (!StringUtils.isEmpty(val)) {
            for (int jj = 0; jj < val.split(":").length; jj++) {
              if (jj == 0) {
                tenkaisu = val.split(":")[jj];
              } else if (jj == 1 && !StringUtils.isEmpty(val.split(":")[jj])) {
                arr = val.split(":")[jj];
              }
            }
          }
          val = arr;
        } else if (itm.getId().equals(TOKSP_NNDTLayout.TENKAISU.getId())) {
          val = tenkaisu;
        }

        if (StringUtils.isEmpty(val)) {
          values += ", null";
        } else {
          prmData.add(val);
          values += ", cast(? as varchar(" + MessageUtility.getDefByteLen(val) + "))";
        }
        names += ", " + col;
      }
      rows += ",(" + StringUtils.removeStart(values, ",") + ")";
    }
    rows = StringUtils.removeStart(rows, ",");
    names = StringUtils.removeStart(names, ",");

    // 基本Merge文
    StringBuffer sbSQL;
    sbSQL = new StringBuffer();
    sbSQL.append("merge into INATK.TOKSP_NNDT as T");
    sbSQL.append(" using (select ");
    // キー情報はロックのため後で追加する
    for (TOK_CMNLayout itm : TOK_CMNLayout.values()) {
      if (itm.getNo() > 1) {
        sbSQL.append(",");
      }
      sbSQL.append("cast(? as " + itm.getTyp() + ") as " + itm.getCol());
    }
    for (TOKSP_NNDTLayout itm : TOKSP_NNDTLayout.values()) {
      if (ArrayUtils.contains(notTarget, itm.getId())) {
        continue;
      } // パラメータ不要
      if (ArrayUtils.contains(keys, itm.getId())) {
        continue;
      } // 上記で実施
      sbSQL.append(",cast(T1." + itm.getCol() + " as " + itm.getTyp() + ") as " + itm.getCol());
    }
    sbSQL.append("  from (values" + rows + ") as T1(" + names + ")");
    sbSQL.append(" ) as RE on (");
    sbSQL.append(" T.MOYSKBN = RE.MOYSKBN and T.MOYSSTDT = RE.MOYSSTDT and T.MOYSRBAN = RE.MOYSRBAN ");
    sbSQL.append(" and T.BMNCD = RE.BMNCD and T.KANRINO = RE.KANRINO and T.KANRIENO = RE.KANRIENO ");
    sbSQL.append(" and T.NNDT = RE.NNDT ");
    sbSQL.append(" )");
    if (SqlType.INS.getVal() == sql.getVal() || SqlType.MRG.getVal() == sql.getVal()) {
      sbSQL.append(" when not matched AND RE.SENDFLG = '1' then ");
      sbSQL.append(" insert (");
      for (TOKSP_NNDTLayout itm : TOKSP_NNDTLayout.values()) {
        if (itm.getNo() > 1) {
          sbSQL.append(",");
        }
        sbSQL.append(itm.getCol());
      }
      sbSQL.append(")values(");
      for (TOKSP_NNDTLayout itm : TOKSP_NNDTLayout.values()) {
        if (ArrayUtils.contains(notTarget, itm.getId())) {
          continue;
        } // パラメータ不要
        if (itm.getId().equals(TOKSP_NNDTLayout.SENDFLG.getId())) {
          continue;
        } // パラメータ不要
        if (itm.getNo() > 1) {
          sbSQL.append(",");
        }
        sbSQL.append("RE." + itm.getCol());
      }
      sbSQL.append(" ," + DefineReport.Values.SENDFLG_UN.getVal()); // 送信フラグ
      sbSQL.append(" ,'" + userId + "'"); // オペレータ
      sbSQL.append(" ,current timestamp"); // 登録日
      sbSQL.append(" ,current timestamp"); // 更新日
      sbSQL.append(")");
    }
    if (SqlType.UPD.getVal() == sql.getVal() || SqlType.MRG.getVal() == sql.getVal()) {
      sbSQL.append(" when matched AND RE.SENDFLG = '1' then ");
      sbSQL.append(" update set");
      sbSQL.append("  TENHTSU_ARR=RE.TENHTSU_ARR"); // F8 : 店発注数配列
      sbSQL.append(" ,HTASU=RE.HTASU"); // F9 : 発注総数
      sbSQL.append(" ,PTNNO=RE.PTNNO"); // F10: パターン№
      sbSQL.append(" ,TSEIKBN=RE.TSEIKBN"); // F11: 訂正区分
      sbSQL.append(" ,TPSU=RE.TPSU"); // F12: 店舗数
      sbSQL.append(" ,TENKAISU=RE.TENKAISU"); // F13: 展開数
      sbSQL.append(" ,ZJSKFLG=RE.ZJSKFLG"); // F14: 前年実績フラグ
      sbSQL.append(" ,WEEKHTDT=RE.WEEKHTDT"); // F15: 週間発注処理日
      sbSQL.append(" ,SENDFLG=" + DefineReport.Values.SENDFLG_UN.getVal()); // 送信フラグ
      sbSQL.append(" ,OPERATOR='" + userId + "'"); // オペレータ
      // sbSQL.append(" ,ADDDT=RE.ADDDT"); // 登録日
      sbSQL.append(" ,UPDDT=current timestamp"); // 更新日
      // sbSQL.append(" ,bat_ctlflg01=RE.bat_ctlflg01"); // F20: batch用制御フラグ01
      sbSQL.append(" when matched AND RE.SENDFLG <> '1' then DELETE");
    }
    if (DefineReport.ID_DEBUG_MODE) {
      System.out.println("/* " + this.getClass().getName() + "*/ " + sbSQL.toString());
    }
    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("全店特売(アンケート無)_納入日");
    return result;
  }

  public HashMap<Integer, String> getTencds(String bmnCd, String moysKbn, String moysStDt, String moysRban, String rankNoAdd, String rankNoDel, String rankNoAddB, String rankNoAddC,
      JSONArray tenCdAdds, JSONArray tenCdDels, String tenAtsukArr, String tenRankArr) {

    Set<Integer> tencds = new TreeSet<Integer>();
    Set<Integer> bTencds = new TreeSet<Integer>();
    Set<Integer> cTencds = new TreeSet<Integer>();

    if (!StringUtils.isEmpty(tenAtsukArr)) {

      HashMap<String, String> tenAtsukUpMap = new ReportJU012Dao(JNDIname).getDigitMap(tenAtsukArr, 1, "1");

      String tenAtsukArr_b = "";
      String tenAtsukArr_c = "";

      // ②(店の分配率/対象店の合計分配率) * 発注総数(小数点切り捨て) を実施
      for (HashMap.Entry<String, String> atsuk : tenAtsukUpMap.entrySet()) {
        String key = atsuk.getKey();
        String val = atsuk.getValue();

        int space = 0;

        if (val.equals("B")) {
          space = Integer.valueOf(key) - tenAtsukArr_b.length() - 1;
          tenAtsukArr_b = tenAtsukArr_b + String.format("%" + space + "s", "") + "B";
        } else if (val.equals("C")) {
          space = Integer.valueOf(key) - tenAtsukArr_c.length() - 1;
          tenAtsukArr_c = tenAtsukArr_c + String.format("%" + space + "s", "") + "C";
        }
      }

      tencds = new ReportBM015Dao(JNDIname).getTenCdAddArr(bmnCd + "_" + tenRankArr, tenCdAdds, tenCdDels);
      bTencds = new ReportBM015Dao(JNDIname).getTenCdAddArr(bmnCd + "_" + tenAtsukArr_b, new JSONArray(), new JSONArray());
      cTencds = new ReportBM015Dao(JNDIname).getTenCdAddArr(bmnCd + "_" + tenAtsukArr_c, new JSONArray(), new JSONArray());
    } else {
      // 対象店を取得
      tencds = new ReportBM015Dao(JNDIname).getTenCdAdd(bmnCd // 部門コード
          , moysKbn // 催し区分
          , moysStDt // 催し開始日
          , moysRban // 催し連番
          , rankNoAdd // 対象ランク№
          , rankNoDel // 除外ランク№
          , tenCdAdds // 対象店
          , tenCdDels // 除外店
      );

      bTencds = new TreeSet<Integer>();
      if (!StringUtils.isEmpty(rankNoAddB)) {
        rankNoDel = "";
        tenCdAdds = new JSONArray();
        tenCdDels = new JSONArray();

        bTencds = new ReportBM015Dao(JNDIname).getTenCdAdd(bmnCd // 部門コード
            , moysKbn // 催し区分
            , moysStDt // 催し開始日
            , moysRban // 催し連番
            , rankNoAddB // 対象ランク№
            , rankNoDel // 除外ランク№
            , tenCdAdds // 対象店
            , tenCdDels // 除外店
        );
      }

      cTencds = new TreeSet<Integer>();
      if (!StringUtils.isEmpty(rankNoAddC)) {
        rankNoDel = "";
        tenCdAdds = new JSONArray();
        tenCdDels = new JSONArray();

        cTencds = new ReportBM015Dao(JNDIname).getTenCdAdd(bmnCd // 部門コード
            , moysKbn // 催し区分
            , moysStDt // 催し開始日
            , moysRban // 催し連番
            , rankNoAddC // 対象ランク№
            , rankNoDel // 除外ランク№
            , tenCdAdds // 対象店
            , tenCdDels // 除外店
        );
      }
    }

    return tenAtsukMerge(tencds, bTencds, cTencds);
  }

  public HashMap<Integer, String> tenAtsukMerge(Set<Integer> tencds, Set<Integer> bTencds, Set<Integer> cTencds) {

    HashMap<Integer, String> tenAtsuk = new HashMap<Integer, String>();

    for (int i = 1; i <= 400; i++) {

      int tenCd = i;

      if (cTencds.contains(tenCd)) {
        if (tenAtsuk.containsKey(tenCd)) {
          tenAtsuk.replace(tenCd, "C");
        } else {
          tenAtsuk.put(tenCd, "C");
        }
        cTencds.remove(tenCd);
      } else if (bTencds.contains(tenCd)) {
        if (tenAtsuk.containsKey(tenCd)) {
          tenAtsuk.replace(tenCd, "B");
        } else {
          tenAtsuk.put(tenCd, "B");
        }
        bTencds.remove(tenCd);
      } else if (tencds.contains(tenCd)) {
        if (tenAtsuk.containsKey(tenCd)) {
          tenAtsuk.replace(tenCd, "A");
        } else {
          tenAtsuk.put(tenCd, "A");
        }
        tencds.remove(tenCd);
      }

      if (tencds.size() == 0 && bTencds.size() == 0 && cTencds.size() == 0) {
        break;
      }
    }
    return tenAtsuk;
  }

  /**
   * 全店特売(アンケート無)_販売 SQL作成処理
   *
   * @param userId
   * @param data
   *
   * @throws Exception
   */
  private JSONObject createSqlTOKSP_HB(String userId, JSONArray dataArray, HashMap<Integer, String> tenAtsuk, SqlType sql) {
    JSONObject result = new JSONObject();
    String[] notTarget = new String[] {TOKSP_HBLayout.SENDFLG.getId(), TOKSP_HBLayout.OPERATOR.getId(), TOKSP_HBLayout.ADDDT.getId(), TOKSP_HBLayout.UPDDT.getId()};


    // 更新情報
    ArrayList<String> prmData = new ArrayList<String>();
    String values = "", rows = "";
    for (int j = 0; j < dataArray.size(); j++) {
      values = "";

      for (TOKSP_HBLayout itm : TOKSP_HBLayout.values()) {
        if (ArrayUtils.contains(notTarget, itm.getId())) {
          continue;
        } // パラメータ不要
        String arr = "", val = "";
        if (itm.getId() == TOKSP_HBLayout.TENATSUK_ARR.getId()) {
          for (int i = 1; i <= 400; i++) {
            if (tenAtsuk.containsKey(i)) {
              arr += tenAtsuk.get(i);
            } else {
              arr += String.format("%1s", "");
            }
          }
          val = new ReportJU012Dao(JNDIname).spaceArr(arr, 1);
        } else {
          val = dataArray.optJSONObject(j).optString(itm.getId());
        }



        if (StringUtils.isEmpty(val)) {
          values += ", null";
        } else {
          prmData.add(val);
          values += ", cast(? as char(" + MessageUtility.getDefByteLen(val) + "))";
        }

      }
      values += " ," + DefineReport.Values.SENDFLG_UN.getVal();
      values += " ,'" + userId + "'";
      values += " ,(select * from(select case when count(*) = 0 or ifnull(ADDDT,0) = 0 ";
      values += "then current_timestamp else ADDDT end as ADDDT ";
      values += "from INATK.TOKSP_HB ";
      values += "where MOYSKBN = " + dataArray.optJSONObject(j).optString("F1") + " ";
      values += "and MOYSSTDT = " + dataArray.optJSONObject(j).optString("F2") + " ";
      values += "and MOYSRBAN = " + dataArray.optJSONObject(j).optString("F3") + " ";
      values += "and BMNCD = " + dataArray.optJSONObject(j).optString("F4") + " ";
      values += "and KANRINO = " + dataArray.optJSONObject(j).optString("F5") + " ";
      values += "and KANRIENO = " + dataArray.optJSONObject(j).optString("F6") + " ";
      values += ") as T1 ) ";
      values += " ,current_timestamp";
      rows += ",(" + StringUtils.removeStart(values, ",") + ")";
    }
    rows = StringUtils.removeStart(rows, ",");

    // 基本Merge文
    StringBuffer sbSQL;
    sbSQL = new StringBuffer();
    sbSQL.append("replace into INATK.TOKSP_HB ( ");
    for (TOKSP_HBLayout itm : TOKSP_HBLayout.values()) {
      if (itm.getNo() > 1) {
        sbSQL.append(",");
      }
      sbSQL.append(itm.getCol());
    }
    sbSQL.append(") values ");
    sbSQL.append(rows);


    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("全店特売(アンケート無)_販売");
    return result;
  }

  /**
   * 全店特売(アンケート有/無)_関連テーブル SQL作成処理(DELETE)
   *
   * @param userId
   * @param data
   *
   * @throws Exception
   */
  private JSONObject createSqlTOK_CMN_DELINS(JSONArray data, String szTable) {
    JSONObject result = new JSONObject();
    String values = "", rows = "";

    // 更新情報
    ArrayList<String> prmData = new ArrayList<String>();

    for (TOK_CMNLayout itm : TOK_CMNLayout.values()) {
      String val = data.optJSONObject(0).optString(itm.getId());
      if (StringUtils.isEmpty(val)) {
        values += ",null";
      } else {
        values += ",cast( ? as SIGNED) as " + itm.getCol();
        prmData.add(val);
      }
    }
    rows = StringUtils.removeStart(values, ",");

    // 基本Merge文
    StringBuffer sbSQL;
    sbSQL = new StringBuffer();
    sbSQL.append("with VAL as ( ");
    sbSQL.append("select " + rows + " ) ");
    sbSQL.append("delete MT ");
    sbSQL.append("from " + szTable + " as MT ");
    sbSQL.append("left join VAL as T1 ");
    sbSQL.append("on MT.MOYSKBN = T1.MOYSKBN ");
    sbSQL.append("and MT.MOYSSTDT = T1.MOYSSTDT ");
    sbSQL.append("and MT.MOYSRBAN = T1.MOYSRBAN ");
    sbSQL.append("and MT.BMNCD = T1.BMNCD ");
    sbSQL.append("and MT.KANRINO = T1.KANRINO ");
    sbSQL.append("where ");
    sbSQL.append("MT.MOYSKBN = T1.MOYSKBN ");
    sbSQL.append("and MT.MOYSSTDT = T1.MOYSSTDT ");
    sbSQL.append("and MT.MOYSRBAN = T1.MOYSRBAN ");
    sbSQL.append("and MT.BMNCD = T1.BMNCD ");
    sbSQL.append("and MT.KANRINO = T1.KANRINO ");


    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add(szTable);
    return result;
  }

  /**
   * 全店特売(アンケート有/無)_関連テーブル SQL作成処理(DELETE)
   *
   * @param userId
   * @param data
   *
   * @throws Exception
   */
  private JSONObject createSqlTOK_CMN_DEL(String userId, JSONObject data, SqlType sql, String szTable) {
    JSONObject result = new JSONObject();

    // 更新情報
    ArrayList<String> prmData = new ArrayList<String>();

    // 基本Merge文
    StringBuffer sbSQL;
    sbSQL = new StringBuffer();
    sbSQL.append("with VAL as ( ");
    sbSQL.append("select ");
    for (TOK_CMNLayout itm : TOK_CMNLayout.values()) {
      if (itm.getNo() > 1) {
        sbSQL.append(",");
      }
      sbSQL.append("cast( " + data.optString(itm.getId()) + " as " + itm.getTyp() + ") as " + itm.getCol() + " ");
    }
    sbSQL.append(") ");

    sbSQL.append("delete MT from " + szTable + " as MT ");
    sbSQL.append("left join VAL as T1  ");
    sbSQL.append("on ");
    sbSQL.append("MT.MOYSKBN = T1.MOYSKBN ");
    sbSQL.append("and MT.MOYSSTDT = T1.MOYSSTDT ");
    sbSQL.append("and MT.MOYSRBAN = T1.MOYSRBAN ");
    sbSQL.append("and MT.BMNCD = T1.BMNCD ");
    sbSQL.append("and MT.KANRINO = T1.KANRINO ");
    sbSQL.append("where ");
    sbSQL.append("MT.MOYSKBN = T1.MOYSKBN ");
    sbSQL.append("and MT.MOYSSTDT = T1.MOYSSTDT ");
    sbSQL.append("and MT.MOYSRBAN = T1.MOYSRBAN ");
    sbSQL.append("and MT.BMNCD = T1.BMNCD ");
    sbSQL.append("and MT.KANRINO = T1.KANRINO ");

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add(szTable);
    return result;
  }

  /**
   * 全特_商品販売日 SQL作成処理
   *
   * @param userId
   * @param data
   *
   * @throws Exception
   */
  private JSONObject createSqlTOKSP_SHNHBDT(String userId, JSONArray dataArray, SqlType sql, String table) {
    JSONObject result = new JSONObject();

    String[] notTarget = new String[] {TOK_CMN_SHNHBDTLayout.OPERATOR.getId(), TOK_CMN_SHNHBDTLayout.ADDDT.getId(), TOK_CMN_SHNHBDTLayout.UPDDT.getId()};

    // 更新情報
    ArrayList<String> prmData = new ArrayList<String>();
    String values = "", names = "", rows = "";
    for (int j = 0; j < dataArray.size(); j++) {
      values = "";
      names = "";
      for (TOK_CMN_SHNHBDTLayout itm : TOK_CMN_SHNHBDTLayout.values()) {
        if (ArrayUtils.contains(notTarget, itm.getId())) {
          continue;
        } // パラメータ不要

        String col = itm.getCol();
        String val = StringUtils.trim(dataArray.optJSONObject(j).optString(itm.getId()));
        if (itm.getId().equals(TOK_CMN_SHNHBDTLayout.MOYCD_ARR.getId()) || itm.getId().equals(TOK_CMN_SHNHBDTLayout.KANRINO_ARR.getId())) {
          val = StringUtils.isEmpty(dataArray.optJSONObject(j).optString(itm.getId()).trim()) ? "" : dataArray.optJSONObject(j).optString(itm.getId());
        }
        if (StringUtils.isEmpty(val)) {
          values += ", null";
        } else {
          prmData.add(val);
          values += ", cast(? as char(" + MessageUtility.getDefByteLen(val) + "))";
        }
        names += ", " + col;
      }
      values += " ,'" + userId + "'"; // オペレータ
      values += " ,current_timestamp"; // 登録日
      values += " ,current_timestamp"; // 更新日
      rows += ",(" + StringUtils.removeStart(values, ",") + ")";
    }
    rows = StringUtils.removeStart(rows, ",");
    names = StringUtils.removeStart(names, ",");

    // 基本Merge文
    StringBuffer sbSQL;
    sbSQL = new StringBuffer();
    sbSQL.append("replace into " + table + " ( ");
    // キー情報はロックのため後で追加する
    for (TOK_CMN_SHNHBDTLayout itm : TOK_CMN_SHNHBDTLayout.values()) {
      if (itm.getNo() > 1) {
        sbSQL.append(",");
      }
      sbSQL.append(itm.getCol());
    }
    sbSQL.append("  )values" + rows + " ");


    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("全特_商品販売日");
    return result;
  }

  /**
   * 全特/個特_商品販売日 SQL作成処理
   *
   * @param userId
   * @param data
   *
   * @throws Exception
   */
  private JSONObject createSqlTOK_CMN_SHNHBDT_DEL(String userId, JSONArray dataArray, SqlType sql, String szTable) {
    JSONObject result = new JSONObject();

    String[] notTarget = new String[] {TOK_CMN_SHNHBDTLayout.OPERATOR.getId(), TOK_CMN_SHNHBDTLayout.ADDDT.getId(), TOK_CMN_SHNHBDTLayout.UPDDT.getId()};

    // 更新情報
    ArrayList<String> prmData = new ArrayList<String>();
    String values = "", names = "", rows = "";
    for (int j = 0; j < dataArray.size(); j++) {
      values = "";
      names = "";
      for (TOK_CMN_SHNHBDTLayout itm : TOK_CMN_SHNHBDTLayout.values()) {
        if (ArrayUtils.contains(notTarget, itm.getId())) {
          continue;
        } // パラメータ不要

        String col = itm.getCol();
        String val = StringUtils.trim(dataArray.optJSONObject(j).optString(itm.getId()));
        if (itm.getId().equals(TOK_CMN_SHNHBDTLayout.MOYCD_ARR.getId()) || itm.getId().equals(TOK_CMN_SHNHBDTLayout.KANRINO_ARR.getId())) {
          val = StringUtils.isEmpty(dataArray.optJSONObject(j).optString(itm.getId()).trim()) ? "" : dataArray.optJSONObject(j).optString(itm.getId());
        }
        if (StringUtils.isEmpty(val)) {
          values += ", null";
        } else {
          prmData.add(val);
          values += ", cast(? as char(" + MessageUtility.getDefByteLen(val) + "))";
        }
        names += ", " + col;
      }
      rows += ",(" + StringUtils.removeStart(values, ",") + ")";
    }
    rows = StringUtils.removeStart(rows, ",");
    names = StringUtils.removeStart(names, ",");

    // 基本Merge文
    StringBuffer sbSQL;
    sbSQL = new StringBuffer();
    sbSQL.append("REPLACE INTO " + szTable + " ( ");
    for (TOK_CMN_SHNHBDTLayout itm : TOK_CMN_SHNHBDTLayout.values()) {
      if (ArrayUtils.contains(notTarget, itm.getId())) {
        continue;
      } // パラメータ不要

      if (itm.getNo() == 1) {
        sbSQL.append(itm.getCol());
      } else {
        sbSQL.append("," + itm.getCol());
      }
    }
    sbSQL.append("   )values (" + rows + "");

    sbSQL.append(" ,'" + userId + "'"); // オペレータ
    sbSQL.append(" ,current_timestamp"); // 登録日
    sbSQL.append(" ,current_timestamp"); // 更新日
    sbSQL.append(")");
    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("全特_商品販売日");
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
  protected JSONObject createSqlTOKSP_SHNNNDT(String userId, JSONArray dataArray, SqlType sql, String table) {
    JSONObject result = new JSONObject();

    String[] notTarget = new String[] {TOK_CMN_SHNNNDTLayout.OPERATOR.getId(), TOK_CMN_SHNNNDTLayout.ADDDT.getId(), TOK_CMN_SHNNNDTLayout.UPDDT.getId()};

    // 更新情報
    ArrayList<String> prmData = new ArrayList<String>();
    String values = "", rows = "";
    for (int j = 0; j < dataArray.size(); j++) {
      values = "";
      for (TOK_CMN_SHNNNDTLayout itm : TOK_CMN_SHNNNDTLayout.values()) {
        if (ArrayUtils.contains(notTarget, itm.getId())) {
          continue;
        } // パラメータ不要

        itm.getCol();
        String val = StringUtils.trim(dataArray.optJSONObject(j).optString(itm.getId()));
        if (itm.getId().equals(TOK_CMN_SHNNNDTLayout.MOYCD_ARR.getId()) || itm.getId().equals(TOK_CMN_SHNNNDTLayout.KANRINO_ARR.getId())) {
          val = dataArray.optJSONObject(j).optString(itm.getId());
        }
        if (StringUtils.isEmpty(val)) {
          values += ", null";
        } else {
          prmData.add(val);
          values += ", cast(? as char(" + MessageUtility.getDefByteLen(val) + "))";
        }

      }
      values += " ,'" + userId + "'";
      values += " ,(select * from(select case when count(*) = 0  ";
      values += "then current_timestamp else ADDDT end as ADDDT ";
      values += "from " + table + " ";
      values += "where SHNCD = " + dataArray.optJSONObject(j).optString("F1") + " ";
      values += "and BINKBN = " + dataArray.optJSONObject(j).optString("F2") + " ";
      values += "and NNDT = " + dataArray.optJSONObject(j).optString("F3") + " ";
      values += ") as T1 ) ";
      values += " ,current_timestamp  ";

      rows += ",(" + StringUtils.removeStart(values, ",") + ")";
    }
    rows = StringUtils.removeStart(rows, ",");


    // 基本Merge文
    StringBuffer sbSQL;
    sbSQL = new StringBuffer();
    sbSQL.append("replace into " + table + " (");
    for (TOK_CMN_SHNNNDTLayout itm : TOK_CMN_SHNNNDTLayout.values()) {
      if (itm.getNo() > 1) {
        sbSQL.append(",");
      }
      sbSQL.append(itm.getCol());
    }

    sbSQL.append(") values" + rows);

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("全特(アンケート無)_商品納入日");
    return result;
  }

  /**
   * 全特/個特_商品納入日 SQL作成処理
   *
   * @param userId
   * @param data
   *
   * @throws Exception
   */
  private JSONObject createSqlTOK_CMN_SHNNNDT_DEL(String userId, JSONArray dataArray, SqlType sql, String szTable) {
    JSONObject result = new JSONObject();

    String[] notTarget = new String[] {TOK_CMN_SHNNNDTLayout.OPERATOR.getId(), TOK_CMN_SHNNNDTLayout.ADDDT.getId(), TOK_CMN_SHNNNDTLayout.UPDDT.getId()};
    String[] notwhere = new String[] {TOK_CMN_SHNNNDTLayout.MOYCD_ARR.getId(), TOK_CMN_SHNNNDTLayout.KANRINO_ARR.getId(), TOK_CMN_SHNNNDTLayout.OPERATOR.getId(), TOK_CMN_SHNNNDTLayout.ADDDT.getId(),
        TOK_CMN_SHNNNDTLayout.UPDDT.getId()};


    // 更新情報
    ArrayList<String> prmData = new ArrayList<String>();
    String values = "", rows = "";
    for (int j = 0; j < dataArray.size(); j++) {
      values = "";
      for (TOK_CMN_SHNNNDTLayout itm : TOK_CMN_SHNNNDTLayout.values()) {
        if (ArrayUtils.contains(notTarget, itm.getId())) {
          continue;
        } // パラメータ不要

        itm.getCol();
        String val = StringUtils.trim(dataArray.optJSONObject(j).optString(itm.getId()));
        if (itm.getId().equals(TOK_CMN_SHNNNDTLayout.MOYCD_ARR.getId()) || itm.getId().equals(TOK_CMN_SHNNNDTLayout.KANRINO_ARR.getId())) {
          val = dataArray.optJSONObject(j).optString(itm.getId());
        }
        if (StringUtils.isEmpty(val)) {
          values += ", null";
        } else {
          prmData.add(val);
          values += ", cast(? as char(" + MessageUtility.getDefByteLen(val) + "))";
        }
      }
      values += ",'" + userId + "' ";
      values += ",(select * from (select case when count(*) = 0 or ifnull(ADDDT,'0') = 0 then current_timestamp ";
      values += "else ADDDT end as ADDDT ";
      values += "from " + szTable + " ";

      for (TOK_CMN_SHNNNDTLayout itm : TOK_CMN_SHNNNDTLayout.values()) {
        if (ArrayUtils.contains(notwhere, itm.getId())) {
          continue;
        }
        String val = StringUtils.trim(dataArray.optJSONObject(j).optString(itm.getId()));
        if (itm.getNo() == 1) {
          values += "where ";
        } else {
          values += "and ";
        }
        values += itm.getCol() + " = ? ";
        prmData.add(val);
      }

      values += ") as T1 ) ";
      values += ",current_timestamp ";

      rows += ",(" + StringUtils.removeStart(values, ",") + ")";
    }
    rows = StringUtils.removeStart(rows, ",");

    // 基本Merge文
    StringBuffer sbSQL;
    sbSQL = new StringBuffer();
    sbSQL.append("replace into " + szTable + " ( ");
    for (TOK_CMN_SHNNNDTLayout itm : TOK_CMN_SHNNNDTLayout.values()) {
      if (itm.getNo() > 1) {
        sbSQL.append(",");
      }
      sbSQL.append(itm.getCol() + " ");
    }
    sbSQL.append(") values" + rows + " ");

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());
    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("全特(アンケート無)_商品納入日");
    return result;
  }

  /**
   * 催し部門内部管理 SQL作成処理(SELECT_FOR_UPDATE)
   *
   * @throws Exception
   */
  public JSONObject createSqlSYSMOYBMN_SEL(JSONObject data) {
    JSONObject result = new JSONObject();

    String szMoyskbn = data.optString(TOK_CMNLayout.MOYSKBN.getId()); // 催し区分
    String szMoysstdt = data.optString(TOK_CMNLayout.MOYSSTDT.getId()); // 催しコード（催し開始日）
    String szMoysrban = data.optString(TOK_CMNLayout.MOYSRBAN.getId()); // 催し連番
    String szBmncd = data.optString(TOK_CMNLayout.BMNCD.getId()); // 部門コード

    new ArrayList<String>();

    StringBuffer sbSQL = new StringBuffer();

    sbSQL.append("  select");
    sbSQL.append("  T1.MOYSKBN"); // F1 : 催し区分
    sbSQL.append(" ,T1.MOYSSTDT"); // F2 : 催し開始日
    sbSQL.append(" ,T1.MOYSRBAN"); // F3 : 催し連番
    sbSQL.append(" ,T1.BMNCD"); // F4 : 部門
    sbSQL.append(" ,T1.SUMI_KANRINO+1 as KANRINO"); // F5 : 付番済管理番号→管理番号
    sbSQL.append(" ,0 as KANRIENO"); // F6 : 枝番
    sbSQL.append(" from INATK.SYSMOYBMN T1");
    sbSQL.append(" where T1.MOYSKBN  = " + szMoyskbn + "");
    sbSQL.append("   and T1.MOYSSTDT = " + szMoysstdt + "");
    sbSQL.append("   and T1.MOYSRBAN = " + szMoysrban + "");
    sbSQL.append("   and T1.BMNCD    = " + szBmncd + "");
    sbSQL.append(" LIMIT 1 ");
    sbSQL.append(" for update ");

    sqlList0.add(sbSQL.toString());
    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());
    return result;
  }

  /**
   * 催し部門内部管理 SQL作成処理(INSERT/UPDATE)
   *
   * @param userId
   * @param data
   *
   * @throws Exception
   */
  private JSONObject createSqlSYSMOYBMN(String userId, JSONObject data, SqlType sql) {
    JSONObject result = new JSONObject();

    new ArrayList<String>();
    // 基本Merge文
    StringBuffer sbSQL;
    sbSQL = new StringBuffer();
    sbSQL.append("replace into INATK.SYSMOYBMN ( ");
    String szMoyskbn = data.optString(TOK_CMNLayout.MOYSKBN.getId()); // 催し区分
    String szMoysstdt = data.optString(TOK_CMNLayout.MOYSSTDT.getId()); // 催しコード（催し開始日）
    String szMoysrban = data.optString(TOK_CMNLayout.MOYSRBAN.getId()); // 催し連番
    String szBmncd = data.optString(TOK_CMNLayout.BMNCD.getId()); // 部門コード
    for (SYSMOYBMNLayout itm : SYSMOYBMNLayout.values()) {
      if (itm.getNo() > 1) {
        sbSQL.append(",");
      }
      sbSQL.append(itm.getCol());
    }
    sbSQL.append(")values(");
    for (TOK_CMNLayout itm : TOK_CMNLayout.values()) {
      if (itm.getNo() > 1) {
        sbSQL.append(",");
      }
      switch (itm.getNo()) {
        case 1:
          sbSQL.append("cast(" + szMoyskbn + " as " + itm.getTyp() + ") ");
          break;
        case 2:
          sbSQL.append("cast(" + szMoysstdt + " as " + itm.getTyp() + ") ");
          break;
        case 3:
          sbSQL.append("cast(" + szMoysrban + " as " + itm.getTyp() + ") ");
          break;
        case 4:
          sbSQL.append("cast(" + szBmncd + " as " + itm.getTyp() + ") ");
          break;
        case 5:
          sbSQL.append("cast(");
          sbSQL.append("(SELECT SUMI_KANRINO+1 FROM INATK.SYSMOYBMN AS MT ");
          sbSQL.append(" where MOYSKBN  = " + szMoyskbn + "");
          sbSQL.append("   and MOYSSTDT = " + szMoysstdt + "");
          sbSQL.append("   and MOYSRBAN = " + szMoysrban + "");
          sbSQL.append("   and BMNCD    = " + szBmncd + "");
          sbSQL.append(" ) ");
          sbSQL.append(" as " + itm.getTyp() + ") ");
          break;
        case 6:
          sbSQL.append(" null"); // F6 : 付番済表示順番
          break;
      }

    }

    sbSQL.append(" ,'" + userId + "'"); // オペレータ
    sbSQL.append(" ,current_timestamp"); // 登録日
    sbSQL.append(" ,current_timestamp"); // 更新日
    sbSQL.append(")");
    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    // prmList.add(prmData);
    lblList.add("催し部門内部管理");
    return result;
  }

  /**
   * 催し部門内部管理 SQL作成処理(SELECT_FOR_UPDATE)
   *
   * @throws Exception
   */
  public JSONObject createSqlSYSMOYKANRIENO_SEL(JSONObject data) {
    JSONObject result = new JSONObject();

    String szMoyskbn = data.optString(TOK_CMNLayout.MOYSKBN.getId()); // 催し区分
    String szMoysstdt = data.optString(TOK_CMNLayout.MOYSSTDT.getId()); // 催しコード（催し開始日）
    String szMoysrban = data.optString(TOK_CMNLayout.MOYSRBAN.getId()); // 催し連番
    String szBmncd = data.optString(TOK_CMNLayout.BMNCD.getId()); // 部門コード
    String szKanrino = data.optString(TOK_CMNLayout.KANRINO.getId()); // 管理番号

    new ArrayList<String>();

    // 詳細部分
    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append(" select");
    sbSQL.append("  T1.MOYSKBN"); // F1 : 催し区分
    sbSQL.append(" ,T1.MOYSSTDT"); // F2 : 催し開始日
    sbSQL.append(" ,T1.MOYSRBAN"); // F3 : 催し連番
    sbSQL.append(" ,T1.BMNCD"); // F4 : 部門
    sbSQL.append(" ,T1.KANRINO"); // F5 : 管理番号
    sbSQL.append(" ,IFNULL(T1.SUMI_KANRIENO+1, 0) as KANRIENO"); // F6 : 付番済枝番→枝番
    sbSQL.append(" from INATK.SYSMOYKANRIENO T1");
    sbSQL.append(" where T1.MOYSKBN  = " + szMoyskbn + "");
    sbSQL.append("   and T1.MOYSSTDT = " + szMoysstdt + "");
    sbSQL.append("   and T1.MOYSRBAN = " + szMoysrban + "");
    sbSQL.append("   and T1.BMNCD    = " + szBmncd + "");
    sbSQL.append("   and T1.KANRINO  = " + szKanrino + "");
    sbSQL.append(" LIMIT 1 ");
    sbSQL.append(" for update ");

    sqlList0.add(sbSQL.toString());
    return result;
  }

  /**
   * 催し管理枝番内部管理 SQL作成処理
   *
   * @param userId
   * @param data
   *
   * @throws Exception
   */
  private JSONObject createSqlSYSMOYKANRIENO(String userId, JSONObject data, SqlType sql) {
    JSONObject result = new JSONObject();

    // 更新情報
    ArrayList<String> prmData = new ArrayList<String>();
    // 基本Merge文
    StringBuffer sbSQL;
    sbSQL = new StringBuffer();
    sbSQL.append("replace into INATK.SYSMOYKANRIENO ( ");
    for (SYSMOYKANRIENOLayout itm : SYSMOYKANRIENOLayout.values()) {
      if (itm.getNo() > 1) {
        sbSQL.append(",");
      }
      sbSQL.append(itm.getCol());
    }
    sbSQL.append(")values(");
    for (TOK_CMNLayout itm : TOK_CMNLayout.values()) {
      if (itm.getNo() > 1) {
        sbSQL.append(",");
      }
      sbSQL.append("cast(? as " + itm.getTyp() + ") ");
    }
    sbSQL.append(" ,'" + userId + "'"); // オペレータ
    sbSQL.append(" ,current_timestamp"); // 登録日
    sbSQL.append(" ,current_timestamp"); // 更新日
    sbSQL.append(")");
    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("催し管理枝番内部管理");
    return result;
  }


  /**
   * 全店特売(アンケート有/無)_関連テーブル SQL作成処理(DELETE)
   *
   * @param userId
   * @param data
   *
   * @throws Exception
   */
  private JSONObject createSqlSYS_CMN_DEL(String userId, JSONObject data, SqlType sql, String szTable, boolean isTOKTG) {
    JSONObject result = new JSONObject();

    // 更新情報
    ArrayList<String> prmData = new ArrayList<String>();

    String[] szTableshn = new String[] {"INATK.TOKTG_SHN", "INATK.TOKSP_SHN"};

    // 基本Merge文
    StringBuffer sbSQL;
    sbSQL = new StringBuffer();
    sbSQL.append("with VAL as  (");
    sbSQL.append("  select T1.MOYSKBN,T1.MOYSSTDT,T1.MOYSRBAN,T1.BMNCD,sum(T1.CNT) as CNT");
    sbSQL.append("  from (");
    for (int j = 0; j < 2; j++) {
      if (j == 1) {
        sbSQL.append("  union all");
      }
      sbSQL.append("  select T1.MOYSKBN,T1.MOYSSTDT,T1.MOYSRBAN,T1.BMNCD");
      if ((isTOKTG && j == 0) || (!isTOKTG && j == 1)) {
        sbSQL.append("  ,sum(case when T1.KANRINO = " + data.optString(TOK_CMNLayout.KANRINO.getId()) + " then 0 else 1 end) as CNT");
      } else {
        sbSQL.append("  ,sum(1) as CNT");
      }
      sbSQL.append("  from " + szTableshn[j] + " T1");
      sbSQL.append("  where T1.UPDKBN = 0 ");
      sbSQL.append("    and T1.MOYSKBN  = " + data.optString(TOK_CMNLayout.MOYSKBN.getId()) + "");
      sbSQL.append("    and T1.MOYSSTDT = " + data.optString(TOK_CMNLayout.MOYSSTDT.getId()) + "");
      sbSQL.append("    and T1.MOYSRBAN = " + data.optString(TOK_CMNLayout.MOYSRBAN.getId()) + "");
      sbSQL.append("    and T1.BMNCD    = " + data.optString(TOK_CMNLayout.BMNCD.getId()) + "");
      sbSQL.append("  group by T1.MOYSKBN,T1.MOYSSTDT,T1.MOYSRBAN,T1.BMNCD");
    }
    sbSQL.append("  ) T1");
    sbSQL.append("  group by T1.MOYSKBN,T1.MOYSSTDT,T1.MOYSRBAN,T1.BMNCD");
    sbSQL.append(" ) ");
    sbSQL.append("delete MT from " + szTable + " as MT ");
    sbSQL.append("left join VAL as T1 ");
    sbSQL.append("on ");
    sbSQL.append("MT.MOYSKBN = T1.MOYSKBN ");
    sbSQL.append("and MT.MOYSSTDT = T1.MOYSSTDT ");
    sbSQL.append("and MT.MOYSRBAN = T1.MOYSRBAN ");
    sbSQL.append("and MT.BMNCD = T1.BMNCD ");
    sbSQL.append("and T1.CNT = 0 ");
    sbSQL.append("where ");
    sbSQL.append("MT.MOYSKBN = T1.MOYSKBN ");
    sbSQL.append("and MT.MOYSSTDT = T1.MOYSSTDT ");
    sbSQL.append("and MT.MOYSRBAN = T1.MOYSRBAN ");
    sbSQL.append("and MT.BMNCD = T1.BMNCD ");
    sbSQL.append("and T1.CNT = 0 ");

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());


    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add(szTable);
    return result;
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

    public int[] getDigit();
  }

  /** @return boolean */
  public String[] getIds(MSTLayout[] layouts) {
    String[] ids = new String[] {};
    for (MSTLayout itm : layouts) {
      ids = (String[]) ArrayUtils.add(ids, itm.getId());
    }
    return ids;
  }

  /** 全店特売_共通レイアウト() */
  public enum TOK_CMNLayout implements MSTLayout {
    /** 催し区分 */
    MOYSKBN(1, "MOYSKBN", "SIGNED", "催し区分"),
    /** 催し開始日 */
    MOYSSTDT(2, "MOYSSTDT", "SIGNED", "催し開始日"),
    /** 催し連番 */
    MOYSRBAN(3, "MOYSRBAN", "SIGNED", "催し連番"),
    /** 部門 */
    BMNCD(4, "BMNCD", "SIGNED", "部門"),

    /** 管理番号 */
    KANRINO(5, "KANRINO", "SIGNED", "管理番号"),
    /** 枝番 */
    KANRIENO(6, "KANRIENO", "SIGNED", "枝番");

    private final Integer no;
    private final String col;
    private final String txt;
    private final String typ;

    /** 初期化 */
    private TOK_CMNLayout(Integer no, String col, String typ, String txt) {
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
      if (typ.indexOf("DATE") != -1 || typ.indexOf("TIMSTAMP") != -1) {
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
    @Override
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

  /** 全店特売(アンケート有/無)_部門レイアウト() */
  public enum TOK_CMN_BMNLayout implements MSTLayout {
    /** 催し区分 */
    MOYSKBN(1, "MOYSKBN", "SMALLINT", "催し区分"),
    /** 催し開始日 */
    MOYSSTDT(2, "MOYSSTDT", "INTEGER", "催し開始日"),
    /** 催し連番 */
    MOYSRBAN(3, "MOYSRBAN", "SMALLINT", "催し連番"),
    /** 部門 */
    BMNCD(4, "BMNCD", "SMALLINT", "部門"),
    /** 更新区分 */
    UPDKBN(5, "UPDKBN", "SMALLINT", "更新区分"),
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
    private TOK_CMN_BMNLayout(Integer no, String col, String typ, String txt) {
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
      if (typ.indexOf("DATE") != -1 || typ.indexOf("TIMSTAMP") != -1) {
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
    @Override
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


  /** 全店特売(アンケート有)_商品レイアウト() */
  public enum TOKTG_SHNLayout implements MSTLayout {
    /** 催し区分 */
    MOYSKBN(1, "MOYSKBN", "SIGNED", "催し区分"),
    /** 催し開始日 */
    MOYSSTDT(2, "MOYSSTDT", "SIGNED", "催し開始日"),
    /** 催し連番 */
    MOYSRBAN(3, "MOYSRBAN", "SIGNED", "催し連番"),
    /** 部門 */
    BMNCD(4, "BMNCD", "SIGNED", "部門"),
    /** 管理番号 */
    KANRINO(5, "KANRINO", "SIGNED", "管理番号"),
    /** 枝番 */
    KANRIENO(6, "KANRIENO", "SIGNED", "枝番"),
    /** 登録種別 */
    ADDSHUKBN(7, "ADDSHUKBN", "SIGNED", "登録種別"),
    /** 1日遅スライド_販売 */
    HBSLIDEFLG(8, "HBSLIDEFLG", "SIGNED", "1日遅スライド_販売"),
    /** 1日遅スライド_納品 */
    NHSLIDEFLG(9, "NHSLIDEFLG", "SIGNED", "1日遅スライド_納品"),
    /** BYコード */
    BYCD(10, "BYCD", "SIGNED", "BYコード"),
    /** 商品コード */
    SHNCD(11, "SHNCD", "CHARACTER(14)", "商品コード"),
    /** 親No */
    PARNO(12, "PARNO", "CHARACTER(3)", "親No"),
    /** 子No */
    CHLDNO(13, "CHLDNO", "SIGNED", "子No"),
    /** 日替フラグ */
    HIGAWRFLG(14, "HIGAWRFLG", "SIGNED", "日替フラグ"),
    /** 販売期間_開始日 */
    HBSTDT(15, "HBSTDT", "SIGNED", "販売期間_開始日"),
    /** 販売期間_終了日 */
    HBEDDT(16, "HBEDDT", "SIGNED", "販売期間_終了日"),
    /** 納入期間_開始日 */
    NNSTDT(17, "NNSTDT", "SIGNED", "納入期間_開始日"),
    /** 納入期間_終了日 */
    NNEDDT(18, "NNEDDT", "SIGNED", "納入期間_終了日"),
    /** チラシ未掲載 */
    CHIRASFLG(19, "CHIRASFLG", "SIGNED", "チラシ未掲載"),
    /** 対象店ランク */
    RANKNO_ADD(20, "RANKNO_ADD", "SIGNED", "対象店ランク"),
    /** 販売予定数 */
    HBYOTEISU(21, "HBYOTEISU", "SIGNED", "販売予定数"),
    /** 原価_特売事前 */
    GENKAAM_MAE(22, "GENKAAM_MAE", "DECIMAL(8,2)", "原価_特売事前"),
    /** 原価_特売追加 */
    GENKAAM_ATO(23, "GENKAAM_ATO", "DECIMAL(8,2)", "原価_特売追加"),
    /** A売価（100ｇ） */
    A_BAIKAAM(24, "A_BAIKAAM", "SIGNED", "A売価（100ｇ）"),
    /** B売価（100ｇ） */
    B_BAIKAAM(25, "B_BAIKAAM", "SIGNED", "B売価（100ｇ）"),
    /** C売価（100ｇ） */
    C_BAIKAAM(26, "C_BAIKAAM", "SIGNED", "C売価（100ｇ）"),
    /** 入数 */
    IRISU(27, "IRISU", "SIGNED", "入数"),
    /** 発注原売価適用フラグ */
    HTGENBAIKAFLG(28, "HTGENBAIKAFLG", "SIGNED", "発注原売価適用フラグ"),
    /** A売価_割引率区分 */
    A_WRITUKBN(29, "A_WRITUKBN", "SIGNED", "A売価_割引率区分"),
    /** B売価_割引率区分 */
    B_WRITUKBN(30, "B_WRITUKBN", "SIGNED", "B売価_割引率区分"),
    /** C売価_割引率区分 */
    C_WRITUKBN(31, "C_WRITUKBN", "SIGNED", "C売価_割引率区分"),
    /** 定貫PLU・不定貫区分 */
    TKANPLUKBN(32, "TKANPLUKBN", "SIGNED", "定貫PLU・不定貫区分"),
    /** A売価_1㎏ */
    A_GENKAAM_1KG(33, "A_GENKAAM_1KG", "SIGNED", "A売価_1㎏"),
    /** B売価_1㎏ */
    B_GENKAAM_1KG(34, "B_GENKAAM_1KG", "SIGNED", "B売価_1㎏"),
    /** C売価_1㎏ */
    C_GENKAAM_1KG(35, "C_GENKAAM_1KG", "SIGNED", "C売価_1㎏"),
    /** パック原価 */
    GENKAAM_PACK(36, "GENKAAM_PACK", "DECIMAL(8,2)", "パック原価"),
    /** A売価_パック */
    A_BAIKAAM_PACK(37, "A_BAIKAAM_PACK", "SIGNED", "A売価_パック"),
    /** B売価_パック */
    B_BAIKAAM_PACK(38, "B_BAIKAAM_PACK", "SIGNED", "B売価_パック"),
    /** C売価_パック */
    C_BAIKAAM_PACK(39, "C_BAIKAAM_PACK", "SIGNED", "C売価_パック"),
    /** A売価_100ｇ相当 */
    A_BAIKAAM_100G(40, "A_BAIKAAM_100G", "SIGNED", "A売価_100ｇ相当"),
    /** B売価_100ｇ相当 */
    B_BAIKAAM_100G(41, "B_BAIKAAM_100G", "SIGNED", "B売価_100ｇ相当"),
    /** C売価_100ｇ相当 */
    C_BAIKAAM_100G(42, "C_BAIKAAM_100G", "SIGNED", "C売価_100ｇ相当"),
    /** 原価_1㎏ */
    GENKAAM_1KG(43, "GENKAAM_1KG", "DECIMAL(8,2)", "原価_1㎏"),
    /** PLU配信フラグ */
    PLUSNDFLG(44, "PLUSNDFLG", "SIGNED", "PLU配信フラグ"),
    /** 展開方法 */
    TENKAIKBN(45, "TENKAIKBN", "SIGNED", "展開方法"),
    /** 実績率パタン数値 */
    JSKPTNSYUKBN(46, "JSKPTNSYUKBN", "SIGNED", "実績率パタン数値"),
    /** 実績率パタン前年同月 */
    JSKPTNZNENMKBN(47, "JSKPTNZNENMKBN", "SIGNED", "実績率パタン前年同月"),
    /** 実績率パタン前年同週 */
    JSKPTNZNENWKBN(48, "JSKPTNZNENWKBN", "SIGNED", "実績率パタン前年同週"),
    /** 大分類 */
    DAICD(49, "DAICD", "SIGNED", "大分類"),
    /** 中分類 */
    CHUCD(50, "CHUCD", "SIGNED", "中分類"),
    /** 産地 */
    SANCHIKN(51, "SANCHIKN", "CHAR", "産地"),
    /** メーカー名 */
    MAKERKN(52, "MAKERKN", "CHAR", "メーカー名"),
    /** POP名称 */
    POPKN(53, "POPKN", "CHAR", "POP名称"),
    /** 規格名称 */
    KIKKN(54, "KIKKN", "CHAR", "規格名称"),
    /** 制限_先着人数 */
    SEGN_NINZU(55, "SEGN_NINZU", "SIGNED", "制限_先着人数"),
    /** 制限_限定表現 */
    SEGN_GENTEI(56, "SEGN_GENTEI", "CHAR", "制限_限定表現"),
    /** 制限_一人当たり個数 */
    SEGN_1KOSU(57, "SEGN_1KOSU", "SIGNED", "制限_一人当たり個数"),
    /** 制限_一人当たり個数単位 */
    SEGN_1KOSUTNI(58, "SEGN_1KOSUTNI", "CHAR", "制限_一人当たり個数単位"),
    /** よりどりフラグ */
    YORIFLG(59, "YORIFLG", "SIGNED", "よりどりフラグ"),
    /** 点数_バンドル1 */
    BD1_TENSU(60, "BD1_TENSU", "SIGNED", "点数_バンドル1"),
    /** 点数_バンドル2 */
    BD2_TENSU(61, "BD2_TENSU", "SIGNED", "点数_バンドル2"),
    /** A売価_1個売り */
    KO_A_BAIKAAN(62, "KO_A_BAIKAAN", "SIGNED", "A売価_1個売り"),
    /** A売価_バンドル1 */
    BD1_A_BAIKAAN(63, "BD1_A_BAIKAAN", "SIGNED", "A売価_バンドル1"),
    /** A売価_バンドル2 */
    BD2_A_BAIKAAN(64, "BD2_A_BAIKAAN", "SIGNED", "A売価_バンドル2"),
    /** B売価_1個売り */
    KO_B_BAIKAAN(65, "KO_B_BAIKAAN", "SIGNED", "B売価_1個売り"),
    /** B売価_バンドル1 */
    BD1_B_BAIKAAN(66, "BD1_B_BAIKAAN", "SIGNED", "B売価_バンドル1"),
    /** B売価_バンドル2 */
    BD2_B_BAIKAAN(67, "BD2_B_BAIKAAN", "SIGNED", "B売価_バンドル2"),
    /** C売価_1個売り */
    KO_C_BAIKAAN(68, "KO_C_BAIKAAN", "SIGNED", "C売価_1個売り"),
    /** C売価_バンドル1 */
    BD1_C_BAIKAAN(69, "BD1_C_BAIKAAN", "SIGNED", "C売価_バンドル1"),
    /** C売価_バンドル2 */
    BD2_C_BAIKAAN(70, "BD2_C_BAIKAAN", "SIGNED", "C売価_バンドル2"),
    /** 目玉区分 */
    MEDAMAKBN(71, "MEDAMAKBN", "SIGNED", "目玉区分"),
    /** POPコード */
    POPCD(72, "POPCD", "SIGNED", "POPコード"),
    /** POPサイズ */
    POPSZ(73, "POPSZ", "CHAR", "POPサイズ"),
    /** POP枚数 */
    POPSU(74, "POPSU", "SIGNED", "POP枚数"),
    /** 商品サイズ */
    SHNSIZE(75, "SHNSIZE", "CHAR", "商品サイズ"),
    /** 商品色 */
    SHNCOLOR(76, "SHNCOLOR", "CHAR", "商品色"),
    /** その他日替わりコメント */
    COMMENT_HGW(77, "COMMENT_HGW", "CHAR", "その他日替わりコメント"),
    /** POPコメント */
    COMMENT_POP(78, "COMMENT_POP", "CHAR", "POPコメント"),
    /** 生食加熱区分 */
    NAMANETUKBN(79, "NAMANETUKBN", "SIGNED", "生食加熱区分"),
    /** 解凍フラグ */
    KAITOFLG(80, "KAITOFLG", "SIGNED", "解凍フラグ"),
    /** 養殖フラグ */
    YOSHOKUFLG(81, "YOSHOKUFLG", "SIGNED", "養殖フラグ"),
    /** 事前打出フラグ */
    JUFLG(82, "JUFLG", "SIGNED", "事前打出フラグ"),
    /** 事前打出日付 */
    JUHTDT(83, "JUHTDT", "INTEGER", "事前打出日付"),
    /** 特売コメント */
    COMMENT_TB(84, "COMMENT_TB", "CHAR", "特売コメント"),
    /** カット店展開フラグ */
    CUTTENFLG(85, "CUTTENFLG", "SIGNED", "カット店展開フラグ"),
    /** 便区分 */
    BINKBN(86, "BINKBN", "SIGNED", "便区分"),
    /** 別伝区分 */
    BDENKBN(87, "BDENKBN", "SIGNED", "別伝区分"),
    /** ワッペン区分 */
    WAPPNKBN(88, "WAPPNKBN", "SIGNED", "ワッペン区分"),
    /** 週次仕入先伝送フラグ */
    SHUDENFLG(89, "SHUDENFLG", "SIGNED", "週次仕入先伝送フラグ"),
    /** 店ランク配列 */
    TENRANK_ARR(90, "TENRANK_ARR", "CHAR", "店ランク配列"),
    /** 月締変更理由 */
    GTSIMECHGKBN(91, "GTSIMECHGKBN", "SIGNED", "月締変更理由"),
    /** 月締変更許可フラグ */
    GTSIMEOKFLG(92, "GTSIMEOKFLG", "SIGNED", "月締変更許可フラグ"),
    /** 事前発注リスト出力日 */
    JLSTCREDT(93, "JLSTCREDT", "SIGNED", "事前発注リスト出力日"),
    /** 事前発注数量取込日 */
    JHTSUINDT(94, "JHTSUINDT", "SIGNED", "事前発注数量取込日"),
    /** 週間発注処理日 */
    WEEKHTDT(95, "WEEKHTDT", "SIGNED", "週間発注処理日"),
    /** 催し販売開始日 */
    MYOSHBSTDT(96, "MYOSHBSTDT", "SIGNED", "催し販売開始日"),
    /** 催し販売終了日 */
    MYOSHBEDDT(97, "MYOSHBEDDT", "SIGNED", "催し販売終了日"),
    /** 催し納入開始日 */
    MYOSNNSTDT(98, "MYOSNNSTDT", "SIGNED", "催し納入開始日"),
    /** 催し納入終了日 */
    MYOSNNEDDT(99, "MYOSNNEDDT", "SIGNED", "催し納入終了日"),
    /** 小分類 */
    SHOBUNCD(105, "SHOBUNCD", "SIGNED", "小分類"),
    /** 更新区分 */
    UPDKBN(100, "UPDKBN", "SIGNED", "更新区分"),
    /** 送信フラグ */
    SENDFLG(101, "SENDFLG", "SIGNED", "送信フラグ"),
    /** オペレータ */
    OPERATOR(102, "OPERATOR", "CHAR", "オペレータ"),
    /** 登録日 */
    ADDDT(103, "ADDDT", "TIMESTAMP", "登録日"),
    /** 更新日 */
    UPDDT(104, "UPDDT", "TIMESTAMP", "更新日");

    private final Integer no;
    private final String col;
    private final String txt;
    private final String typ;

    /** 初期化 */
    private TOKTG_SHNLayout(Integer no, String col, String typ, String txt) {
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
      if (typ.indexOf("DATE") != -1 || typ.indexOf("TIMSTAMP") != -1) {
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
    @Override
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

  /** 全店特売(アンケート無)_商品レイアウト() */
  public enum TOKSP_SHNLayout implements MSTLayout {
    /** 催し区分 */
    MOYSKBN(1, "MOYSKBN", "SIGNED", "催し区分"),
    /** 催し開始日 */
    MOYSSTDT(2, "MOYSSTDT", "SIGNED", "催し開始日"),
    /** 催し連番 */
    MOYSRBAN(3, "MOYSRBAN", "SIGNED", "催し連番"),
    /** 部門 */
    BMNCD(4, "BMNCD", "SIGNED", "部門"),
    /** 管理番号 */
    KANRINO(5, "KANRINO", "SIGNED", "管理番号"),
    /** 枝番 */
    KANRIENO(6, "KANRIENO", "SIGNED", "枝番"),
    /** 登録種別 */
    ADDSHUKBN(7, "ADDSHUKBN", "SIGNED", "登録種別"),
    /** BYコード */
    BYCD(8, "BYCD", "SIGNED", "BYコード"),
    /** 商品コード */
    SHNCD(9, "SHNCD", "CHAR(14)", "商品コード"),
    /** 親No */
    PARNO(10, "PARNO", "CHAR(3)", "親No"),
    /** 子No */
    CHLDNO(11, "CHLDNO", "SIGNED", "子No"),
    /** 日替フラグ */
    HIGAWRFLG(12, "HIGAWRFLG", "SIGNED", "日替フラグ"),
    /** 販売期間_開始日 */
    HBSTDT(13, "HBSTDT", "SIGNED", "販売期間_開始日"),
    /** 販売期間_終了日 */
    HBEDDT(14, "HBEDDT", "SIGNED", "販売期間_終了日"),
    /** 納入期間_開始日 */
    NNSTDT(15, "NNSTDT", "SIGNED", "納入期間_開始日"),
    /** 納入期間_終了日 */
    NNEDDT(16, "NNEDDT", "SIGNED", "納入期間_終了日"),
    /** チラシ未掲載 */
    CHIRASFLG(17, "CHIRASFLG", "SIGNED", "チラシ未掲載"),
    /** 対象店ランク_A売価 */
    RANKNO_ADD_A(18, "RANKNO_ADD_A", "SIGNED", "対象店ランク_A売価"),
    /** 対象店ランク_B売価 */
    RANKNO_ADD_B(19, "RANKNO_ADD_B", "SIGNED", "対象店ランク_B売価"),
    /** 対象店ランク_C売価 */
    RANKNO_ADD_C(20, "RANKNO_ADD_C", "SIGNED", "対象店ランク_C売価"),
    /** 除外店ランク */
    RANKNO_DEL(21, "RANKNO_DEL", "SIGNED", "除外店ランク"),
    /** 販売予定数 */
    HBYOTEISU(22, "HBYOTEISU", "SIGNED", "販売予定数"),
    /** 原価_特売事前 */
    GENKAAM_MAE(23, "GENKAAM_MAE", "DECIMAL(8,2)", "原価_特売事前"),
    /** 原価_特売追加 */
    GENKAAM_ATO(24, "GENKAAM_ATO", "DECIMAL(8,2)", "原価_特売追加"),
    /** A売価（100ｇ） */
    A_BAIKAAM(25, "A_BAIKAAM", "SIGNED", "A売価（100ｇ）"),
    /** B売価（100ｇ） */
    B_BAIKAAM(26, "B_BAIKAAM", "SIGNED", "B売価（100ｇ）"),
    /** C売価（100ｇ） */
    C_BAIKAAM(27, "C_BAIKAAM", "SIGNED", "C売価（100ｇ）"),
    /** 入数 */
    IRISU(28, "IRISU", "SIGNED", "入数"),
    /** 発注原売価適用フラグ */
    HTGENBAIKAFLG(29, "HTGENBAIKAFLG", "SIGNED", "発注原売価適用フラグ"),
    /** A売価_割引率区分 */
    A_WRITUKBN(30, "A_WRITUKBN", "SIGNED", "A売価_割引率区分"),
    /** B売価_割引率区分 */
    B_WRITUKBN(31, "B_WRITUKBN", "SIGNED", "B売価_割引率区分"),
    /** C売価_割引率区分 */
    C_WRITUKBN(32, "C_WRITUKBN", "SIGNED", "C売価_割引率区分"),
    /** 定貫PLU・不定貫区分 */
    TKANPLUKBN(33, "TKANPLUKBN", "SIGNED", "定貫PLU・不定貫区分"),
    /** A売価_1㎏ */
    A_GENKAAM_1KG(34, "A_GENKAAM_1KG", "SIGNED", "A売価_1㎏"),
    /** B売価_1㎏ */
    B_GENKAAM_1KG(35, "B_GENKAAM_1KG", "SIGNED", "B売価_1㎏"),
    /** C売価_1㎏ */
    C_GENKAAM_1KG(36, "C_GENKAAM_1KG", "SIGNED", "C売価_1㎏"),
    /** パック原価 */
    GENKAAM_PACK(37, "GENKAAM_PACK", "DECIMAL(8,2)", "パック原価"),
    /** A売価_パック */
    A_BAIKAAM_PACK(38, "A_BAIKAAM_PACK", "SIGNED", "A売価_パック"),
    /** B売価_パック */
    B_BAIKAAM_PACK(39, "B_BAIKAAM_PACK", "SIGNED", "B売価_パック"),
    /** C売価_パック */
    C_BAIKAAM_PACK(40, "C_BAIKAAM_PACK", "SIGNED", "C売価_パック"),
    /** A売価_100ｇ相当 */
    A_BAIKAAM_100G(41, "A_BAIKAAM_100G", "SIGNED", "A売価_100ｇ相当"),
    /** B売価_100ｇ相当 */
    B_BAIKAAM_100G(42, "B_BAIKAAM_100G", "SIGNED", "B売価_100ｇ相当"),
    /** C売価_100ｇ相当 */
    C_BAIKAAM_100G(43, "C_BAIKAAM_100G", "SIGNED", "C売価_100ｇ相当"),
    /** 原価_1㎏ */
    GENKAAM_1KG(44, "GENKAAM_1KG", "DECIMAL(8,2)", "原価_1㎏"),
    /** PLU配信フラグ */
    PLUSNDFLG(45, "PLUSNDFLG", "SIGNED", "PLU配信フラグ"),
    /** 展開方法 */
    TENKAIKBN(46, "TENKAIKBN", "SIGNED", "展開方法"),
    /** 実績率パタン数値 */
    JSKPTNSYUKBN(47, "JSKPTNSYUKBN", "SIGNED", "実績率パタン数値"),
    /** 実績率パタン前年同月 */
    JSKPTNZNENMKBN(48, "JSKPTNZNENMKBN", "SIGNED", "実績率パタン前年同月"),
    /** 実績率パタン前年同週 */
    JSKPTNZNENWKBN(49, "JSKPTNZNENWKBN", "SIGNED", "実績率パタン前年同週"),
    /** 大分類 */
    DAICD(50, "DAICD", "SIGNED", "大分類"),
    /** 中分類 */
    CHUCD(51, "CHUCD", "SIGNED", "中分類"),
    /** 産地 */
    SANCHIKN(52, "SANCHIKN", "CHAR(40)", "産地"),
    /** メーカー名 */
    MAKERKN(53, "MAKERKN", "CHAR(28)", "メーカー名"),
    /** POP名称 */
    POPKN(54, "POPKN", "CHAR(40)", "POP名称"),
    /** 規格名称 */
    KIKKN(55, "KIKKN", "CHAR(46)", "規格名称"),
    /** 制限_先着人数 */
    SEGN_NINZU(56, "SEGN_NINZU", "SIGNED", "制限_先着人数"),
    /** 制限_限定表現 */
    SEGN_GENTEI(57, "SEGN_GENTEI", "CHAR(20)", "制限_限定表現"),
    /** 制限_一人当たり個数 */
    SEGN_1KOSU(58, "SEGN_1KOSU", "SIGNED", "制限_一人当たり個数"),
    /** 制限_一人当たり個数単位 */
    SEGN_1KOSUTNI(59, "SEGN_1KOSUTNI", "CHAR(10)", "制限_一人当たり個数単位"),
    /** よりどりフラグ */
    YORIFLG(60, "YORIFLG", "SIGNED", "よりどりフラグ"),
    /** 点数_バンドル1 */
    BD1_TENSU(61, "BD1_TENSU", "SIGNED", "点数_バンドル1"),
    /** 点数_バンドル2 */
    BD2_TENSU(62, "BD2_TENSU", "SIGNED", "点数_バンドル2"),
    /** A売価_1個売り */
    KO_A_BAIKAAN(63, "KO_A_BAIKAAN", "SIGNED", "A売価_1個売り"),
    /** A売価_バンドル1 */
    BD1_A_BAIKAAN(64, "BD1_A_BAIKAAN", "SIGNED", "A売価_バンドル1"),
    /** A売価_バンドル2 */
    BD2_A_BAIKAAN(65, "BD2_A_BAIKAAN", "SIGNED", "A売価_バンドル2"),
    /** B売価_1個売り */
    KO_B_BAIKAAN(66, "KO_B_BAIKAAN", "SIGNED", "B売価_1個売り"),
    /** B売価_バンドル1 */
    BD1_B_BAIKAAN(67, "BD1_B_BAIKAAN", "SIGNED", "B売価_バンドル1"),
    /** B売価_バンドル2 */
    BD2_B_BAIKAAN(68, "BD2_B_BAIKAAN", "SIGNED", "B売価_バンドル2"),
    /** C売価_1個売り */
    KO_C_BAIKAAN(69, "KO_C_BAIKAAN", "SIGNED", "C売価_1個売り"),
    /** C売価_バンドル1 */
    BD1_C_BAIKAAN(70, "BD1_C_BAIKAAN", "SIGNED", "C売価_バンドル1"),
    /** C売価_バンドル2 */
    BD2_C_BAIKAAN(71, "BD2_C_BAIKAAN", "SIGNED", "C売価_バンドル2"),
    /** 目玉区分 */
    MEDAMAKBN(72, "MEDAMAKBN", "SIGNED", "目玉区分"),
    /** POPコード */
    POPCD(73, "POPCD", "SIGNED", "POPコード"),
    /** POPサイズ */
    POPSZ(74, "POPSZ", "CHAR(3)", "POPサイズ"),
    /** POP枚数 */
    POPSU(75, "POPSU", "SIGNED", "POP枚数"),
    /** 商品サイズ */
    SHNSIZE(76, "SHNSIZE", "CHAR(40)", "商品サイズ"),
    /** 商品色 */
    SHNCOLOR(77, "SHNCOLOR", "CHAR(20)", "商品色"),
    /** その他日替わりコメント */
    COMMENT_HGW(78, "COMMENT_HGW", "CHAR(100)", "その他日替わりコメント"),
    /** POPコメント */
    COMMENT_POP(79, "COMMENT_POP", "CHAR(100)", "POPコメント"),
    /** 生食加熱区分 */
    NAMANETUKBN(80, "NAMANETUKBN", "SIGNED", "生食加熱区分"),
    /** 解凍フラグ */
    KAITOFLG(81, "KAITOFLG", "SIGNED", "解凍フラグ"),
    /** 養殖フラグ */
    YOSHOKUFLG(82, "YOSHOKUFLG", "SIGNED", "養殖フラグ"),
    /** 事前打出フラグ */
    JUFLG(83, "JUFLG", "SIGNED", "事前打出フラグ"),
    /** 事前打出日付 */
    JUHTDT(84, "JUHTDT", "SIGNED", "事前打出日付"),
    /** 特売コメント */
    COMMENT_TB(85, "COMMENT_TB", "CHAR(60)", "特売コメント"),
    /** カット店展開フラグ */
    CUTTENFLG(86, "CUTTENFLG", "SIGNED", "カット店展開フラグ"),
    /** 便区分 */
    BINKBN(87, "BINKBN", "SIGNED", "便区分"),
    /** 別伝区分 */
    BDENKBN(88, "BDENKBN", "SIGNED", "別伝区分"),
    /** ワッペン区分 */
    WAPPNKBN(89, "WAPPNKBN", "SIGNED", "ワッペン区分"),
    /** 週次仕入先伝送フラグ */
    SHUDENFLG(90, "SHUDENFLG", "SIGNED", "週次仕入先伝送フラグ"),
    /** 店ランク配列 */
    TENRANK_ARR(91, "TENRANK_ARR", "CHAR(400)", "店ランク配列"),
    /** 事前発注リスト出力日 */
    JLSTCREDT(92, "JLSTCREDT", "SIGNED", "事前発注リスト出力日"),
    /** 事前発注数量取込日 */
    JHTSUINDT(93, "JHTSUINDT", "SIGNED", "事前発注数量取込日"),
    /** 週間発注処理日 */
    WEEKHTDT(94, "WEEKHTDT", "SIGNED", "週間発注処理日"),
    /** 催し販売開始日 */
    MYOSHBSTDT(95, "MYOSHBSTDT", "SIGNED", "催し販売開始日"),
    /** 催し販売終了日 */
    MYOSHBEDDT(96, "MYOSHBEDDT", "SIGNED", "催し販売終了日"),
    /** 催し納入開始日 */
    MYOSNNSTDT(97, "MYOSNNSTDT", "SIGNED", "催し納入開始日"),
    /** 催し納入終了日 */
    MYOSNNEDDT(98, "MYOSNNEDDT", "SIGNED", "催し納入終了日"),
    /** 更新区分 */
    UPDKBN(99, "UPDKBN", "SIGNED", "更新区分"),
    /** 送信フラグ */
    SENDFLG(100, "SENDFLG", "SIGNED", "送信フラグ"),
    /** オペレータ */
    OPERATOR(101, "OPERATOR", "CHAR(20)", "オペレータ"),
    /** 登録日 */
    ADDDT(102, "ADDDT", "TIMESTAMP", "登録日"),
    /** 更新日 */
    UPDDT(103, "UPDDT", "TIMESTAMP", "更新日");

    private final Integer no;
    private final String col;
    private final String txt;
    private final String typ;

    /** 初期化 */
    private TOKSP_SHNLayout(Integer no, String col, String typ, String txt) {
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
      if (typ.indexOf("DATE") != -1 || typ.indexOf("TIMSTAMP") != -1) {
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
    @Override
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

  /** 全店特売(アンケート有/無)_対象除外店レイアウト() */
  public enum TOK_CMN_TJTENLayout implements MSTLayout {
    /** 催し区分 */
    MOYSKBN(1, "MOYSKBN", "SIGNED", "催し区分"),
    /** 催し開始日 */
    MOYSSTDT(2, "MOYSSTDT", "SIGNED", "催し開始日"),
    /** 催し連番 */
    MOYSRBAN(3, "MOYSRBAN", "SIGNED", "催し連番"),
    /** 部門 */
    BMNCD(4, "BMNCD", "SIGNED", "部門"),
    /** 管理番号 */
    KANRINO(5, "KANRINO", "SIGNED", "管理番号"),
    /** 枝番 */
    KANRIENO(6, "KANRIENO", "SIGNED", "枝番"),
    /** 店コード */
    TENCD(7, "TENCD", "SIGNED", "店コード"),
    /** 対象除外フラグ */
    TJFLG(8, "TJFLG", "SIGNED", "対象除外フラグ"),
    /** 店ランク */
    TENRANK(9, "TENRANK", "CHAR", "店ランク"),
    /** 送信フラグ */
    SENDFLG(10, "SENDFLG", "SIGNED", "送信フラグ"),
    /** オペレータ */
    OPERATOR(11, "OPERATOR", "CHAR", "オペレータ"),
    /** 登録日 */
    ADDDT(12, "ADDDT", "TIMESTAMP", "登録日"),
    /** 更新日 */
    UPDDT(13, "UPDDT", "TIMESTAMP", "更新日");

    private final Integer no;
    private final String col;
    private final String txt;
    private final String typ;

    /** 初期化 */
    private TOK_CMN_TJTENLayout(Integer no, String col, String typ, String txt) {
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
      if (typ.indexOf("DATE") != -1 || typ.indexOf("TIMSTAMP") != -1) {
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
    @Override
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

  /** 全店特売(アンケート有)_納入日レイアウト() */
  public enum TOKTG_NNDTLayout implements MSTLayout {
    /** 催し区分 */
    MOYSKBN(1, "MOYSKBN", "SMALLINT", "催し区分"),
    /** 催し開始日 */
    MOYSSTDT(2, "MOYSSTDT", "INTEGER", "催し開始日"),
    /** 催し連番 */
    MOYSRBAN(3, "MOYSRBAN", "SMALLINT", "催し連番"),
    /** 部門 */
    BMNCD(4, "BMNCD", "SMALLINT", "部門"),
    /** 管理番号 */
    KANRINO(5, "KANRINO", "SMALLINT", "管理番号"),
    /** 枝番 */
    KANRIENO(6, "KANRIENO", "SMALLINT", "枝番"),
    /** 納入日 */
    NNDT(7, "NNDT", "INTEGER", "納入日"),
    /** 店発注数配列 */
    TENHTSU_ARR(8, "TENHTSU_ARR", "VARCHAR(2000)", "店発注数配列"),
    /** 店変更フラグ配列 */
    TENCHGFLG_ARR(9, "TENCHGFLG_ARR", "VARCHAR(400)", "店変更フラグ配列"),
    /** 発注総数 */
    HTASU(10, "HTASU", "INTEGER", "発注総数"),
    /** パターン№ */
    PTNNO(11, "PTNNO", "INTEGER", "パターン№"),
    /** 訂正区分 */
    TSEIKBN(12, "TSEIKBN", "SMALLINT", "訂正区分"),
    /** 店舗数 */
    TPSU(13, "TPSU", "SMALLINT", "店舗数"),
    /** 展開数 */
    TENKAISU(14, "TENKAISU", "INTEGER", "展開数"),
    /** 前年実績フラグ */
    ZJSKFLG(15, "ZJSKFLG", "SMALLINT", "前年実績フラグ"),
    /** 週間発注処理日 */
    WEEKHTDT(16, "WEEKHTDT", "INTEGER", "週間発注処理日"),
    /** 送信フラグ */
    SENDFLG(17, "SENDFLG", "SMALLINT", "送信フラグ"),
    /** オペレータ */
    OPERATOR(18, "OPERATOR", "VARCHAR(20)", "オペレータ"),
    /** 登録日 */
    ADDDT(19, "ADDDT", "TIMESTAMP", "登録日"),
    /** 更新日 */
    UPDDT(20, "UPDDT", "TIMESTAMP", "更新日")

    /** batch用制御フラグ01 */
    // ,bat_ctlflg01(21,"bat_ctlflg01","SMALLINT","batch用制御フラグ01")
    ;

    private final Integer no;
    private final String col;
    private final String txt;
    private final String typ;

    /** 初期化 */
    private TOKTG_NNDTLayout(Integer no, String col, String typ, String txt) {
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
      if (typ.indexOf("DATE") != -1 || typ.indexOf("TIMSTAMP") != -1) {
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
    @Override
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

  /** 全店特売(アンケート無)_納入日レイアウト() */
  public enum TOKSP_NNDTLayout implements MSTLayout {
    /** 催し区分 */
    MOYSKBN(1, "MOYSKBN", "SMALLINT", "催し区分"),
    /** 催し開始日 */
    MOYSSTDT(2, "MOYSSTDT", "INTEGER", "催し開始日"),
    /** 催し連番 */
    MOYSRBAN(3, "MOYSRBAN", "SMALLINT", "催し連番"),
    /** 部門 */
    BMNCD(4, "BMNCD", "SMALLINT", "部門"),
    /** 管理番号 */
    KANRINO(5, "KANRINO", "SMALLINT", "管理番号"),
    /** 枝番 */
    KANRIENO(6, "KANRIENO", "SMALLINT", "枝番"),
    /** 納入日 */
    NNDT(7, "NNDT", "INTEGER", "納入日"),
    /** 店発注数配列 */
    TENHTSU_ARR(8, "TENHTSU_ARR", "VARCHAR(2000)", "店発注数配列"),
    /** 発注総数 */
    HTASU(9, "HTASU", "INTEGER", "発注総数"),
    /** パターン№ */
    PTNNO(10, "PTNNO", "INTEGER", "パターン№"),
    /** 訂正区分 */
    TSEIKBN(11, "TSEIKBN", "SMALLINT", "訂正区分"),
    /** 店舗数 */
    TPSU(12, "TPSU", "SMALLINT", "店舗数"),
    /** 展開数 */
    TENKAISU(13, "TENKAISU", "INTEGER", "展開数"),
    /** 前年実績フラグ */
    ZJSKFLG(14, "ZJSKFLG", "SMALLINT", "前年実績フラグ"),
    /** 週間発注処理日 */
    WEEKHTDT(15, "WEEKHTDT", "INTEGER", "週間発注処理日"),
    /** 送信フラグ */
    SENDFLG(16, "SENDFLG", "SMALLINT", "送信フラグ"),
    /** オペレータ */
    OPERATOR(17, "OPERATOR", "VARCHAR(20)", "オペレータ"),
    /** 登録日 */
    ADDDT(18, "ADDDT", "TIMESTAMP", "登録日"),
    /** 更新日 */
    UPDDT(19, "UPDDT", "TIMESTAMP", "更新日")

    /** batch用制御フラグ01 */
    // ,bat_ctlflg01(20,"bat_ctlflg01","SMALLINT","batch用制御フラグ01")
    ;

    private final Integer no;
    private final String col;
    private final String txt;
    private final String typ;

    /** 初期化 */
    private TOKSP_NNDTLayout(Integer no, String col, String typ, String txt) {
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
      if (typ.indexOf("DATE") != -1 || typ.indexOf("TIMSTAMP") != -1) {
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
    @Override
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

  /** 全店特売(アンケート無)_販売レイアウト() */
  public enum TOKSP_HBLayout implements MSTLayout {
    /** 催し区分 */
    MOYSKBN(1, "MOYSKBN", "SMALLINT", "催し区分"),
    /** 催し開始日 */
    MOYSSTDT(2, "MOYSSTDT", "INTEGER", "催し開始日"),
    /** 催し連番 */
    MOYSRBAN(3, "MOYSRBAN", "SMALLINT", "催し連番"),
    /** 部門 */
    BMNCD(4, "BMNCD", "SMALLINT", "部門"),
    /** 管理番号 */
    KANRINO(5, "KANRINO", "SMALLINT", "管理番号"),
    /** 枝番 */
    KANRIENO(6, "KANRIENO", "SMALLINT", "枝番"),
    /** 店扱いフラグ配列 */
    TENATSUK_ARR(7, "TENATSUK_ARR", "VARCHAR(400)", "店扱いフラグ配列"),
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
    private TOKSP_HBLayout(Integer no, String col, String typ, String txt) {
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
      if (typ.indexOf("DATE") != -1 || typ.indexOf("TIMSTAMP") != -1) {
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
    @Override
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


  /** 全特/個特_商品販売日レイアウト() */
  public enum TOK_CMN_SHNHBDTLayout implements MSTLayout {
    /** 商品コード */
    SHNCD(1, "SHNCD", "CHARACTER(14)", "商品コード"),
    /** 販売日 */
    HBDT(2, "HBDT", "INTEGER", "販売日"),
    /** 催しコード配列 */
    MOYCD_ARR(3, "MOYCD_ARR", "VARCHAR(4000)", "催しコード配列"),
    /** 管理番号配列 */
    KANRINO_ARR(4, "KANRINO_ARR", "VARCHAR(1600)", "管理番号配列"),
    /** オペレータ */
    OPERATOR(5, "OPERATOR", "VARCHAR(20)", "オペレータ"),
    /** 登録日 */
    ADDDT(6, "ADDDT", "TIMESTAMP", "登録日"),
    /** 更新日 */
    UPDDT(7, "UPDDT", "TIMESTAMP", "更新日");

    private final Integer no;
    private final String col;
    private final String txt;
    private final String typ;

    /** 初期化 */
    private TOK_CMN_SHNHBDTLayout(Integer no, String col, String typ, String txt) {
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
      if (typ.indexOf("DATE") != -1 || typ.indexOf("TIMSTAMP") != -1) {
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
    @Override
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

  /** 全特(アンケート無)/個特_商品納入日レイアウト() */
  public enum TOK_CMN_SHNNNDTLayout implements MSTLayout {
    /** 商品コード */
    SHNCD(1, "SHNCD", "CHARACTER(14)", "商品コード"),
    /** 便区分 */
    BINKBN(2, "BINKBN", "SMALLINT", "便区分"),
    /** 納入日 */
    NNDT(3, "NNDT", "INTEGER", "納入日"),
    /** 催しコード配列 */
    MOYCD_ARR(4, "MOYCD_ARR", "VARCHAR(4000)", "催しコード配列"),
    /** 管理番号配列 */
    KANRINO_ARR(5, "KANRINO_ARR", "VARCHAR(1600)", "管理番号配列"),
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
    private TOK_CMN_SHNNNDTLayout(Integer no, String col, String typ, String txt) {
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
      if (typ.indexOf("DATE") != -1 || typ.indexOf("TIMSTAMP") != -1) {
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
    @Override
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

  /** 催し部門内部管理レイアウト() */
  public enum SYSMOYBMNLayout implements MSTLayout {
    /** 催し区分 */
    MOYSKBN(1, "MOYSKBN", "SMALLINT", "催し区分"),
    /** 催し開始日 */
    MOYSSTDT(2, "MOYSSTDT", "INTEGER", "催し開始日"),
    /** 催し連番 */
    MOYSRBAN(3, "MOYSRBAN", "SMALLINT", "催し連番"),
    /** 部門 */
    BMNCD(4, "BMNCD", "SMALLINT", "部門"),
    /** 付番済管理番号 */
    SUMI_KANRINO(5, "SUMI_KANRINO", "SMALLINT", "付番済管理番号"),
    /** 付番済表示順番 */
    SUMI_HYOSEQNO(6, "SUMI_HYOSEQNO", "SMALLINT", "付番済表示順番"),
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
    private SYSMOYBMNLayout(Integer no, String col, String typ, String txt) {
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
      if (typ.indexOf("DATE") != -1 || typ.indexOf("TIMSTAMP") != -1) {
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
    @Override
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

  /** 催し管理枝番内部管理レイアウト() */
  public enum SYSMOYKANRIENOLayout implements MSTLayout {
    /** 催し区分 */
    MOYSKBN(1, "MOYSKBN", "SMALLINT", "催し区分"),
    /** 催し開始日 */
    MOYSSTDT(2, "MOYSSTDT", "INTEGER", "催し開始日"),
    /** 催し連番 */
    MOYSRBAN(3, "MOYSRBAN", "SMALLINT", "催し連番"),
    /** 部門 */
    BMNCD(4, "BMNCD", "SMALLINT", "部門"),
    /** 管理番号 */
    KANRINO(5, "KANRINO", "SMALLINT", "管理番号"),
    /** 付番済枝番 */
    SUMI_KANRIENO(6, "SUMI_KANRIENO", "SMALLINT", "付番済枝番"),
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
    private SYSMOYKANRIENOLayout(Integer no, String col, String typ, String txt) {
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
      if (typ.indexOf("DATE") != -1 || typ.indexOf("TIMSTAMP") != -1) {
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
    @Override
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

  /**
   *
   * 小数点以下を切り捨てた計算結果を返却
   *
   * @param baika
   * @param tensu
   * @return String
   */
  public String calcAvg(String baika, String tensu) {
    if (baika.length() == 0)
      return "";
    if (tensu.length() == 0)
      return "";

    double d1 = Double.parseDouble(baika);
    double d2 = Double.parseDouble(tensu);

    return String.valueOf((int) Math.floor(d1 / d2));
  }
}
