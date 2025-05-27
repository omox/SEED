package dao;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import authentication.bean.User;
import authentication.defines.Consts;
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
public class Reportx242Dao extends ItemDao {

  /** SQLリスト保持用変数 */
  public ArrayList<String> sqlList = new ArrayList<>();
  /** SQLのパラメータリスト保持用変数 */
  public ArrayList<ArrayList<String>> prmList = new ArrayList<>();
  /** SQLログ用のラベルリスト保持用変数 */
  public ArrayList<String> lblList = new ArrayList<>();

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public Reportx242Dao(String JNDIname) {
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
    JSONObject msgObj = new JSONObject();
    JSONArray msg = this.check(map);

    if (msg.size() > 0) {
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
    return msgObj;
  }

  private String createCommand() {
    // ログインユーザー情報取得
    User userInfo = getUserInfo();
    if (userInfo == null) {
      return "";
    }
    ArrayList<String> paramData = new ArrayList<>();
    String szUserCd = getMap().get("USERCD"); // 選択ユーザーコード

    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<>();

    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append(" with WKUSER as ( ");
    sbSQL.append(" SELECT");
    sbSQL.append(" T1.*");
    sbSQL.append(" , T2.TENCD");
    sbSQL.append(" , T2.TENKN ");
    sbSQL.append(" FROM");
    sbSQL.append(" ( select");
    sbSQL.append(" T1.USER_ID");
    sbSQL.append(" , T1.PASSWORDS");
    sbSQL.append(" , T1.NM_FAMILY");
    sbSQL.append(" , T1.NM_NAME");
    sbSQL.append(" , DATE_FORMAT( ");
    sbSQL.append(" DATE_FORMAT(T1.DT_PW_TERM, '%Y%m%d')");
    sbSQL.append(" , '%Y/%m/%d'");
    sbSQL.append(" ) as DT_PW_TERM");
    sbSQL.append(" , case ");
    sbSQL.append(" when T1.YOBI_2 is null ");
    sbSQL.append(" or T1.YOBI_2 = '' ");
    sbSQL.append(" then null ");
    sbSQL.append(" else cast(T1.YOBI_2 as SIGNED) ");
    sbSQL.append(" end YOBI_2");
    sbSQL.append(" , T1.YOBI_6");
    sbSQL.append(" , T1.YOBI_7");
    sbSQL.append(" , T1.YOBI_8");
    sbSQL.append(" , T1.YOBI_9");
    sbSQL.append(" , T1.NM_UPDATE || '　' || T2.NM_FAMILY || T2.NM_NAME AS UPDUSER");
    sbSQL.append(" , DATE_FORMAT(T1.DT_UPDATE, '%Y/%m/%d %H:%i') AS DT_UPDATE");
    sbSQL.append(" , DATE_FORMAT(T1.DT_UPDATE, '%Y%m%d%H%i%S%f') as HDN_UPDDT ");
    sbSQL.append(" , T1.CD_USER ");
    sbSQL.append(" from KEYSYS.SYS_USERS T1 ");
    sbSQL.append(" left join KEYSYS.SYS_USERS T2 ");
    sbSQL.append(" on T1.NM_UPDATE = T2.USER_ID");
    sbSQL.append(" where T1.CD_USER=? ");
    paramData.add(szUserCd);
    sbSQL.append(" ) T1 ");
    sbSQL.append(" left join INAMS.MSTTEN T2 ");
    sbSQL.append(" on T1.YOBI_2 = T2.TENCD) ");
    sbSQL.append(" , WKMS as ( ");
    sbSQL.append(" SELECT");
    sbSQL.append(" CD_USER AS CD_USER_WK ");
    sbSQL.append(" FROM");
    sbSQL.append(" KEYSYS.SYS_USER_POS ");
    sbSQL.append(" WHERE");
    sbSQL.append(" CD_POSITION = 24321) ");
    sbSQL.append(" , WKTK as ( ");
    sbSQL.append(" SELECT");
    sbSQL.append(" CD_USER AS CD_USER_WK ");
    sbSQL.append(" FROM");
    sbSQL.append(" KEYSYS.SYS_USER_POS ");
    sbSQL.append(" WHERE");
    sbSQL.append(" CD_POSITION = 24320) ");
    sbSQL.append(" , WKTN as ( ");
    sbSQL.append(" SELECT");
    sbSQL.append(" CD_USER AS CD_USER_WK ");
    sbSQL.append(" FROM");
    sbSQL.append(" KEYSYS.SYS_USER_POS ");
    sbSQL.append(" WHERE");
    sbSQL.append(" CD_POSITION = 24322) ");
    sbSQL.append(" SELECT ");
    sbSQL.append(" USER_ID ");
    sbSQL.append(" , PASSWORDS ");
    sbSQL.append(" , NM_FAMILY ");
    sbSQL.append(" , NM_NAME ");
    sbSQL.append(" , case ");
    sbSQL.append(" when TENCD is null ");
    sbSQL.append(" then '本部' ");
    sbSQL.append(" when YOBI_9 <> ''");
    sbSQL.append(" then YOBI_9 ");
    sbSQL.append(" else RIGHT('000' || YOBI_2, 3) end ");
    sbSQL.append(" , DT_PW_TERM ");
    sbSQL.append(" , case ");
    sbSQL.append(" when NOT EXISTS ( ");
    sbSQL.append(" SELECT 1 FROM");
    sbSQL.append(" WKMS T1 ");
    sbSQL.append(" WHERE CD_USER = T1.CD_USER_WK) ");
    sbSQL.append(" then '1' ");
    sbSQL.append(" when YOBI_6 = '1' ");
    sbSQL.append(" and YOBI_7 = '1' ");
    sbSQL.append(" then '2' ");
    sbSQL.append(" when YOBI_6 = '1' ");
    sbSQL.append(" then '3' ");
    sbSQL.append(" else '4' end ");
    sbSQL.append(" , case ");
    sbSQL.append(" when NOT EXISTS ( ");
    sbSQL.append(" SELECT 1 FROM ");
    sbSQL.append(" WKTK T1 ");
    sbSQL.append(" WHERE CD_USER = T1.CD_USER_WK) ");
    sbSQL.append(" then '1' ");
    sbSQL.append(" when YOBI_8 = '1' ");
    sbSQL.append(" then '2' ");
    sbSQL.append(" else '4' end ");
    sbSQL.append(" , case ");
    sbSQL.append(" when NOT EXISTS ( ");
    sbSQL.append(" SELECT 1 FROM ");
    sbSQL.append(" WKTN T1 ");
    sbSQL.append(" WHERE CD_USER = T1.CD_USER_WK) ");
    sbSQL.append(" then '1' ");
    sbSQL.append(" else '2' end ");
    sbSQL.append(" , UPDUSER ");
    sbSQL.append(" , DT_UPDATE ");
    sbSQL.append(" , HDN_UPDDT ");
    sbSQL.append(" FROM WKUSER ");
    setParamData(paramData);

    // オプション情報（タイトル）設定
    JSONObject option = new JSONObject();
    option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
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
   * 更新処理実行
   *
   * @return
   *
   * @throws Exception
   */
  private JSONObject updateData(HashMap<String, String> map, User userInfo, String sysdate) throws Exception {
    // パラメータ確認
    JSONObject option = new JSONObject();
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 対象情報（主要な更新情報）

    // 排他チェック用
    JSONArray msg = new JSONArray();

    // パラメータ確認
    // 必須チェック
    if (dataArray.isEmpty() || dataArray.size() != 1 || dataArray.getJSONObject(0).isEmpty()) {
      option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
      return option;
    }

    // 部門マスタINSERT/UPDATE処理
    this.createSqlUser(map, userInfo);

    // 排他チェック実行
    String targetTable = null;
    String targetWhere = null;

    String hdn_upddt = map.get("HDN_UPDDT");
    String userCd = map.get("USERCD");
    ArrayList<String> targetParam = new ArrayList<>();
    targetTable = "(SELECT CD_USER,DT_UPDATE AS UPDDT FROM KEYSYS.SYS_USERS)";
    targetWhere = " CD_USER = ? ";
    targetParam.add(userCd);
    if (!super.checkExclusion(targetTable, targetWhere, targetParam, hdn_upddt)) {
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

  /**
   * 部門マスタINSERT/UPDATE処理
   *
   * @param map
   * @param userInfo
   */
  public String createSqlUser(HashMap<String, String> map, User userInfo) {

    StringBuffer sbSQL = new StringBuffer();

    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー

    ArrayList<String> prmData = new ArrayList<>();
    Object[] valueData = new Object[] {};
    String values = "";

    // ページ情報の取得
    String outpage = map.get(DefineReport.ID_PARAM_PAGE) == null ? "" : map.get(DefineReport.ID_PARAM_PAGE);

    String userCd = map.get("USERCD");
    String pass = map.get("PASS");
    String passOld = map.get("PASSOLD");
    String yobi6 = map.get("YOBI6");
    String yobi7 = map.get("YOBI7");
    String yobi8 = map.get("YOBI8");
    String yobi9 = map.get("YOBI9");

    // 権限
    String authMs = map.get("AUTHMS");
    String authTk = map.get("AUTHTK");
    String authTn = map.get("AUTHTN");

    // 有効期限
    String dtPwTerm = map.containsKey("DTPWTERM") ? map.get("DTPWTERM") : "19000101";

    // テーブル区分
    String infTbleKbn = !StringUtils.isEmpty(outpage) && outpage.equals("Pass") ? "0" : map.get("INFTBLEKBN");

    boolean passChg = true;
    if (pass.equals(passOld)) {
      passChg = false;
    }

    values = String.valueOf(0 + 1);
    values += ", ?";
    prmData.add(userCd);
    values += ", ?";
    prmData.add(pass);
    values += ", ?";
    prmData.add(yobi6);
    values += ", ?";
    prmData.add(yobi7);
    values += ", ?";
    prmData.add(yobi8);
    values += ", ?";
    prmData.add(yobi9);
    values += ", ?";
    prmData.add(dtPwTerm);
    valueData = ArrayUtils.add(valueData, "(" + values + ")");

    // ユーザーマスタの登録・更新
    sbSQL = new StringBuffer();

    sbSQL.append(" INSERT into KEYSYS.SYS_USERS  (");
    sbSQL.append(" CD_USER"); // ユーザーコード
    sbSQL.append(", PASSWORDS"); // パスワード
    if (passChg) {
    sbSQL.append(", PASSWORDS_1");//パスワード1
    sbSQL.append(", PASSWORDS_2");//パスワード2
    sbSQL.append(", PASSWORDS_3");//パスワード3
    sbSQL.append(", PASSWORDS_4");//パスワード4
    sbSQL.append(", PASSWORDS_5");//パスワード5
    }
    sbSQL.append(", YOBI_6"); // 予備6
    sbSQL.append(", YOBI_7"); // 予備7
    sbSQL.append(", YOBI_8"); // 予備8
    sbSQL.append(", YOBI_9"); // 予備9
    sbSQL.append(", DT_PW_TERM "); // 有効期限
    sbSQL.append(", NM_CREATE");
    sbSQL.append(", DT_CREATE");
    sbSQL.append(", NM_UPDATE "); // オペレーター
    sbSQL.append(", DT_UPDATE "); // 更新日
    sbSQL.append(" )select ");
    sbSQL.append(" T1.CD_USER"); // ユーザーコード
    sbSQL.append(", T1.PASSWORDS"); // パスワード
    if (passChg) {
    sbSQL.append(", USERS.PASSWORDS AS PASSWORDS_1 ");//パスワード1
    sbSQL.append(", USERS.PASSWORDS_1 AS PASSWORDS_2 ");//パスワード2
    sbSQL.append(", USERS.PASSWORDS_2 AS PASSWORDS_3 ");//パスワード3
    sbSQL.append(", USERS.PASSWORDS_3 AS PASSWORDS_4 ");//パスワード4
    sbSQL.append(", USERS.PASSWORDS_4 AS PASSWORDS_5 ");//パスワード5
    }
    sbSQL.append(", T1.YOBI_6"); // 予備6
    sbSQL.append(", T1.YOBI_7"); // 予備7
    sbSQL.append(", T1.YOBI_8"); // 予備8
    sbSQL.append(", T1.YOBI_9"); // 予備9
    sbSQL.append(", T1.DT_PW_TERM "); // 有効期限
    sbSQL.append(", '" + userId + "' AS  NM_CREATE ");
    sbSQL.append(", CURRENT_TIMESTAMP AS DT_CREATE ");
    sbSQL.append(", '" + userId + "' AS NM_UPDATE "); // オペレーター
    sbSQL.append(", CURRENT_TIMESTAMP AS DT_UPDATE "); // 更新日
    sbSQL.append(" FROM (VALUES ROW ");
    sbSQL.append(StringUtils.join(valueData, ",") + ") as T1(NUM , ");
    sbSQL.append(" CD_USER");
    sbSQL.append(", PASSWORDS");
    sbSQL.append(", YOBI_6");
    sbSQL.append(", YOBI_7");
    sbSQL.append(", YOBI_8");
    sbSQL.append(", YOBI_9");
    sbSQL.append(", DT_PW_TERM) ");
    if (passChg) {
    sbSQL.append("LEFT OUTER JOIN KEYSYS.SYS_USERS AS USERS ");
    sbSQL.append("ON T1.CD_USER = USERS.CD_USER ");
    }
    sbSQL.append("ON DUPLICATE KEY UPDATE ");
    sbSQL.append("  PASSWORDS=VALUES(PASSWORDS)");
    if (passChg) {
      sbSQL.append(" ,PASSWORDS_1=VALUES(PASSWORDS_1)");
      sbSQL.append(" ,PASSWORDS_2=VALUES(PASSWORDS_2)");
      sbSQL.append(" ,PASSWORDS_3=VALUES(PASSWORDS_3)");
      sbSQL.append(" ,PASSWORDS_4=VALUES(PASSWORDS_4)");
      sbSQL.append(" ,PASSWORDS_5=VALUES(PASSWORDS_5)");
      sbSQL.append(" ,DT_PW_TERM=VALUES(DT_PW_TERM)");
    }
    sbSQL.append(", YOBI_6=VALUES(YOBI_6)");
    sbSQL.append(", YOBI_7=VALUES(YOBI_7)");
    sbSQL.append(", YOBI_8=VALUES(YOBI_8)");
    sbSQL.append(", YOBI_9=VALUES(YOBI_9)");
    sbSQL.append(", NM_UPDATE=VALUES(NM_UPDATE)");
    sbSQL.append(", DT_UPDATE=VALUES(DT_UPDATE)");

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("ユーザーマスタ");

    // SYS_USERS_JNLの挿入
    String jnlSeq = getCSVTOK_SEQ();
    sbSQL = new StringBuffer();
    prmData = new ArrayList<>();
    sbSQL.append(" INSERT INTO KEYSYS.SYS_USERS_JNL ");
    sbSQL.append(" SELECT");
    sbSQL.append(" ? AS SEQ");
    prmData.add(jnlSeq);
    sbSQL.append(" , CURRENT_TIMESTAMP AS INF_DATE");
    sbSQL.append(" , ? AS INF_OPERATOR");
    prmData.add(userId);
    sbSQL.append(" ,? AS INF_TABLEKBN");
    prmData.add(infTbleKbn);
    sbSQL.append(" ,'1' AS INF_TRNKBN");
    sbSQL.append(" , ? AS CD_USER");
    prmData.add(userCd);
    sbSQL.append(" ,USER_ID");
    sbSQL.append(" , ? AS PASSWORDS");
    prmData.add(pass);
    sbSQL.append(" ,NM_FAMILY");
    sbSQL.append(" ,NM_NAME");
    sbSQL.append(" ,CUSTOM_VALUE");
    sbSQL.append(" ,NM_CREATE");
    sbSQL.append(" ,DT_CREATE");
    sbSQL.append(" ,NM_UPDATE");
    sbSQL.append(" ,DT_UPDATE");
    sbSQL.append(" ,CD_AUTH");
    sbSQL.append(" ,DT_PW_TERM");
    sbSQL.append(" ,LOGO");
    sbSQL.append(" ,YOBI_1");
    sbSQL.append(" ,YOBI_2");
    sbSQL.append(" ,YOBI_3");
    sbSQL.append(" ,YOBI_4");
    sbSQL.append(" ,YOBI_5");
    sbSQL.append(" ,? AS YOBI_6");
    sbSQL.append(" ,? AS YOBI_7");
    sbSQL.append(" ,? AS YOBI_8");
    sbSQL.append(" ,? AS YOBI_9");
    prmData.add(yobi6);
    prmData.add(yobi7);
    prmData.add(yobi8);
    prmData.add(yobi9);
    sbSQL.append(" ,YOBI_10");
    if (passChg) {
      sbSQL.append(" ,? AS PASSWORDS_1");
      sbSQL.append(" ,PASSWORDS_1 AS PASSWORDS_2");
      sbSQL.append(" ,PASSWORDS_2 AS PASSWORDS_3");
      sbSQL.append(" ,PASSWORDS_3 AS PASSWORDS_4");
      sbSQL.append(" ,PASSWORDS_4 AS PASSWORDS_5");
      prmData.add(pass);
    } else {
      sbSQL.append(" ,PASSWORDS_1");
      sbSQL.append(" ,PASSWORDS_2");
      sbSQL.append(" ,PASSWORDS_3");
      sbSQL.append(" ,PASSWORDS_4");
      sbSQL.append(" ,PASSWORDS_5");
    }
    sbSQL.append(" FROM");
    sbSQL.append(" KEYSYS.SYS_USERS");
    sbSQL.append(" WHERE");
    sbSQL.append(" CD_USER=?");
    prmData.add(userCd);

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

    sqlList.add(sbSQL.toString());
    prmList.add(prmData);
    lblList.add("SYS_USERS_JNL");

    // その他テーブル更新
    createSqlUserPos(userId, userCd, authMs, "24321", jnlSeq, infTbleKbn);
    createSqlUserPos(userId, userCd, authTk, "24320", jnlSeq, infTbleKbn);
    createSqlUserPos(userId, userCd, authTn, "24322", jnlSeq, infTbleKbn);

    return sbSQL.toString();
  }

  /**
   * 本部マスタ用
   *
   * @param map
   * @param userInfo
   */
  public String createSqlUserPos(String userId, String userCd, String auth, String cdPositinon, String jnlSeq, String infTbleKbn) {

    StringBuffer sbSQL = new StringBuffer();
    ArrayList<String> prmData = new ArrayList<>();
    Object[] valueData = new Object[] {};
    String values = "";

    sbSQL = new StringBuffer();
    prmData = new ArrayList<>();
    if (auth.equals("1")) {
      // SYS_USER_POSの削除
      sbSQL.append(" DELETE FROM KEYSYS.SYS_USER_POS WHERE CD_USER=? AND CD_POSITION=?");
      prmData.add(userCd);
      prmData.add(cdPositinon);

      if (DefineReport.ID_DEBUG_MODE)
        System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

      sqlList.add(sbSQL.toString());
      prmList.add(prmData);
      lblList.add("SYS_USER_POS");

      sbSQL = new StringBuffer();
      prmData = new ArrayList<>();

      // SYS_USER_POS_JNLの挿入
      sbSQL = new StringBuffer();
      prmData = new ArrayList<>();
      sbSQL.append(" INSERT INTO KEYSYS.SYS_USER_POS_JNL ");
      sbSQL.append(" SELECT");
      sbSQL.append(" ? AS SEQ");
      prmData.add(jnlSeq);
      sbSQL.append(" , CURRENT_TIMESTAMP AS INF_DATE");
      sbSQL.append(" , ? AS INF_OPERATOR");
      prmData.add(userId);
      sbSQL.append(" ,? AS INF_TABLEKBN");
      prmData.add(infTbleKbn);
      sbSQL.append(" ,'9' AS INF_TRNKBN");
      sbSQL.append(" , ? AS CD_USER");
      prmData.add(userCd);
      sbSQL.append(" , " + cdPositinon + " AS CD_POSITION");
      sbSQL.append(" ,? AS NM_CREATE");
      prmData.add(userId);
      sbSQL.append(" , CURRENT_TIMESTAMP AS DT_CREATE");
      sbSQL.append(" ,? AS NM_UPDATE");
      prmData.add(userId);
      sbSQL.append(" , CURRENT_TIMESTAMP AS DT_UPDATE");
      sbSQL.append(" FROM (SELECT 1 AS DUMMY) DUMMY ");

      if (DefineReport.ID_DEBUG_MODE)
        System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

      sqlList.add(sbSQL.toString());
      prmList.add(prmData);
      lblList.add("SYS_USER_POS_JNL");
    } else {
      // SYS_USER_POSの登録・更新
      values = String.valueOf(0 + 1);
      values += ", ?";
      prmData.add(userCd);
      values += ", " + cdPositinon;
      valueData = ArrayUtils.add(valueData, "(" + values + ")");

      sbSQL.append(" INSERT into KEYSYS.SYS_USER_POS  ( ");
      sbSQL.append(" CD_USER"); // ユーザーコード
      sbSQL.append(", CD_POSITION");
      sbSQL.append(", NM_CREATE");
      sbSQL.append(", DT_CREATE");
      sbSQL.append(", NM_UPDATE "); // オペレーター
      sbSQL.append(", DT_UPDATE "); // 更新日
      sbSQL.append(")");
      sbSQL.append("select ");
      sbSQL.append(" CD_USER"); // ユーザーコード
      sbSQL.append(", CD_POSITION");
      sbSQL.append(", '" + userId + "' AS NM_CREATE "); // オペレーター
      sbSQL.append(", CURRENT_TIMESTAMP AS DT_CREATE "); // 更新日
      sbSQL.append(", '" + userId + "' AS NM_UPDATE "); // オペレーター
      sbSQL.append(", CURRENT_TIMESTAMP AS DT_UPDATE "); // 更新日
      sbSQL.append(" FROM (VALUES ROW ");
      sbSQL.append(StringUtils.join(valueData, ",") + ") as T1( NUM ");
      sbSQL.append(", CD_USER");
      sbSQL.append(", CD_POSITION");
      sbSQL.append(")");
      sbSQL.append(" ON DUPLICATE KEY UPDATE ");// 重複した時のUPDATE処理
      sbSQL.append(" NM_UPDATE=VALUES(NM_UPDATE)");
      sbSQL.append(", DT_UPDATE=VALUES(DT_UPDATE)");

      if (DefineReport.ID_DEBUG_MODE)
        System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

      sqlList.add(sbSQL.toString());
      prmList.add(prmData);
      lblList.add("SYS_USER_POS");

      sbSQL = new StringBuffer();
      prmData = new ArrayList<>();

      // SYS_USER_POS_JNLの挿入
      sbSQL = new StringBuffer();
      prmData = new ArrayList<>();
      sbSQL.append(" INSERT INTO KEYSYS.SYS_USER_POS_JNL ");
      sbSQL.append(" SELECT");
      sbSQL.append(" ? AS SEQ");
      prmData.add(jnlSeq);
      sbSQL.append(" , CURRENT_TIMESTAMP AS INF_DATE");
      sbSQL.append(" , ? AS INF_OPERATOR");
      prmData.add(userId);
      sbSQL.append(" ,? AS INF_TABLEKBN");
      prmData.add(infTbleKbn);
      sbSQL.append(" ,'1' AS INF_TRNKBN");
      sbSQL.append(" , ? AS CD_USER");
      prmData.add(userCd);
      sbSQL.append(" , " + cdPositinon + " AS CD_POSITION");
      sbSQL.append(" ,? AS NM_CREATE");
      prmData.add(userId);
      sbSQL.append(" , CURRENT_TIMESTAMP AS DT_CREATE");
      sbSQL.append(" ,? AS NM_UPDATE");
      prmData.add(userId);
      sbSQL.append(" , CURRENT_TIMESTAMP AS DT_UPDATE");
      sbSQL.append(" FROM  (SELECT 1 AS DUMMY) DUMMY");

      if (DefineReport.ID_DEBUG_MODE)
        System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

      sqlList.add(sbSQL.toString());
      prmList.add(prmData);
      lblList.add("SYS_USER_POS_JNL");
    }

    return sbSQL.toString();
  }

  /**
   * SEQ情報取得処理
   *
   * @throws Exception
   */
  public String getCSVTOK_SEQ() {
    new ItemList();
    String sqlColCommand = "SELECT KEYSYS.nextval('SEQ_USER') AS \"1\"";
    @SuppressWarnings("static-access")
    JSONArray array = ItemList.selectJSONArray(sqlColCommand, null, Defines.STR_JNDI_DS);
    String value = "";
    if (array.size() > 0) {
      value = array.optJSONObject(0).optString("1");
    }
    return value;
  }

  /**
   * チェック処理
   *
   * @throws Exception
   */
  public JSONArray check(HashMap<String, String> map) {
    // パラメータ確認
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 更新情報
    JSONArray msg = new JSONArray();

    // 対象件数チェック
    if (dataArray.size() == 0 || dataArray.getJSONObject(0).isEmpty()) {
      msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
      return msg;
    }
    return msg;
  }
}
