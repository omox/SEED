/*
 * 作成日: 2008/09/26
 *
 */
package authentication.defines;

/**
 * SQL文クラス DB⇔アプリケーション間のデータ処理に用いられるSQLを定義 一部スキーマ名を固定にしている。294行目以下注意
 */

public final class SQL {

  /** システムスキーマ名 */
  public final static String system_schema = "KEYSYS";
  // 遷移時にパラメータとして取得に変更
  // public final static String kbn_menu = "3";

  public final static String system_schema_02 = "KEYMST";
  public final static String SQL_ORGANIZATION01 = "AAM";
  public final static String SQL_ORGANIZATION02 = "0004";
  public final static String SQL_ORGANIZATION03 = "ASE";
  public final static String SQL_ORGANIZATION04 = "0001";
  public final static String DC = "\"";
  public final static String DC_E = "\"" + ",";
  // ==========================================================================
  // =============
  /** USER */
  /* SELECT ---------------------------------- */
  /** ログイン画面：ログインユーザー情報取得(単) */
  public static final String USER_SEL_001 =
      "SELECT T1.CD_USER,T1.user_id, T1.PASSWORDS, T1.NM_FAMILY || ' ' || T1.NM_NAME AS uname, T1.CD_AUTH, T1.DT_PW_TERM, T5.CD_GROUP , T2.CD_POSITION, COALESCE(T3.NM_POSITION,'') AS NM_POSITION,COALESCE(T5.NM_GROUP,'') AS sosznm,T1.custom_value,case when T1.LOGO is null then '' else T1.LOGO end as LOGO,T3.CUSTOM_VALUE as POS_CUSTOM_VALUE,T1.YOBI_1,T1.YOBI_2,T1.YOBI_3,T1.YOBI_4,T1.YOBI_5,T1.YOBI_6,T1.YOBI_7,T1.YOBI_8,T1.YOBI_9,T1.YOBI_10 FROM "
          + system_schema + ".SYS_USERS T1 INNER JOIN " + system_schema + ".SYS_USER_POS T2 ON ( T2.CD_USER = T1.CD_USER ) LEFT JOIN " + system_schema + ".SYS_POSMST T3 ON ( T3.CD_POSITION = T2.CD_POSITION) INNER JOIN " + system_schema + ".SYS_USER_GROUP T4 ON ( T4.CD_USER = T1.CD_USER ) LEFT JOIN "
          + system_schema + ".SYS_GROUPS T5 ON ( T4.CD_GROUP = T5.CD_GROUP ) WHERE T1.USER_ID = ? AND T1.PASSWORDS = ?";
  /** メンテナンス画面：ユーザー情報取得(複) */
  public static final String USER_SEL_002 = "SELECT t1.CD_USER, t1.PASSWORDS, t1.NM_FAMILY || ' ' || t1.NM_NAME AS uname,t1.NM_FAMILY,t1.NM_NAME,t1.DT_PW_TERM, T3.cd_group, COALESCE(t2.cd_position,'') AS cd_position FROM " + system_schema + ".SYS_USERS t1 LEFT OUTER JOIN " + system_schema
      + ".SYS_USER_POS t2 ON ( t2.CD_USER = t1.CD_USER )  INNER JOIN " + system_schema + ".SYS_USER_GROUP T3 ON ( t3.CD_USER = t1.CD_USER ) ORDER BY t2.cd_position, T3.cd_group, t1.CD_USER ";

  /** ユーザーログイン店舗情報 */
  public static final String USER_TENKN = "SELECT TENKN FROM INAMS.MSTTEN WHERE TENCD=?";

  /** メンテナンス画面：ユーザー情報取得(複) */
  public static final String USER_SEL_003 = "SELECT CD_USER,USER_ID, PASSWORDS, NM_FAMILY || ' ' || NM_NAME AS uname,NM_FAMILY,NM_NAME,CD_AUTH,DT_PW_TERM,CUSTOM_VALUE FROM " + system_schema + ".SYS_USERS ORDER BY CD_USER";

  /* UPDATE ---------------------------------- */
  /** メンテナンス画面:ユーザー情報更新 ユーザマスタ */
  public static final String USER_UPD_001 = "UPDATE " + system_schema + ".sys_users SET cd_user = ?, USER_ID = ?,PASSWORDS = ?, NM_FAMILY = ? , NM_NAME = ?, DT_PW_TERM = ?, CUSTOM_VALUE = ?, nm_update = ?, dt_update = CURRENT TIMESTAMP WHERE cd_user = ? ";

  /** メンテナンス画面：ユーザー情報更新 ユーザーロール管理 */
  public static final String USER_UPD_002 = "UPDATE " + system_schema + ".SYS_USER_POS SET cd_user = ?, cd_position = ?, nm_update = ?, dt_update =  CURRENT TIMESTAMP WHERE cd_user = ? ";

  /** メンテナンス画面：ユーザー情報更新 ユーザーグループ */
  public static final String USER_UPD_003 = "UPDATE " + system_schema + ".SYS_USER_GROUP SET cd_user = ?, cd_group = ?, nm_update = ?, dt_update =  CURRENT TIMESTAMP WHERE cd_user = ? ";

  /* INSERT ---------------------------------- */
  /** メンテナンス画面：ユーザー情報追加 sys_users */
  public static final String USER_INS_001 = "INSERT INTO " + system_schema + ".sys_users (cd_user,user_id, passwords, nm_family, nm_name, dt_pw_term, custom_value,nm_create, dt_create, nm_update, dt_update ) VALUES(?, ?, ?, ?, ?, ?, ?, ?, CURRENT TIMESTAMP, ?, CURRENT TIMESTAMP)";

  /** メンテナンス画面：ユーザー情報追加 SYS_USER_POS */
  public static final String USER_INS_002 = "INSERT INTO " + system_schema + ".SYS_USER_POS (cd_user, cd_position, nm_create, dt_create, nm_update, dt_update ) VALUES(?, ?, ?, CURRENT TIMESTAMP, ?, CURRENT TIMESTAMP) ";

  /** メンテナンス画面：ユーザー情報追加 SYS_USER_GROUP */
  public static final String USER_INS_003 = "INSERT INTO " + system_schema + ".SYS_USER_GROUP (cd_user, cd_group, nm_create, dt_create, nm_update, dt_update ) VALUES(?, ?, ?, CURRENT TIMESTAMP, ?, CURRENT TIMESTAMP) ";

  /* DELETE ---------------------------------- */
  /** メンテナンス画面：ユーザー情報削除 ユーザーマスタ */
  public static final String USER_DEL_001 = "DELETE FROM " + system_schema + ".sys_users WHERE cd_user = ? ";

  /** メンテナンス画面：ユーザー情報削除 ユーザーロール管理 */
  public static final String USER_DEL_002 = "DELETE FROM " + system_schema + ".SYS_USER_POS WHERE cd_user = ? ";

  /** メンテナンス画面：ユーザー情報削除 ユーザーグループ管理 */
  public static final String USER_DEL_003 = "DELETE FROM " + system_schema + ".SYS_USER_GROUP WHERE cd_user = ? ";

  /* SEQUENCE-------------------------------------- */
  public static final String SEQUENCE_001 = "VALUES NEXTVAL FOR " + system_schema + ".GENERATER_ID";

  public static final String SEQUENCE_002 = "VALUES NEXTVAL FOR " + system_schema + ".SEQ_INFO";

  // ==========================================================================
  // =============
  /** MENU */
  /* SELECT ---------------------------------- */
  /** メンテナンス画面：マスターのみレポート分類取得(複) */
  public static final String MENU_SEL_001 = "SELECT * from " + system_schema + ".SYS_MENU GROUP BY DT_CREATE";

  /* INSERT ---------------------------------- */
  /** メンテナンス画面：メニュー情報追加 */
  public static final String MENU_INS_001 = "INSERT INTO " + system_schema + ".SYS_MENU (syzkcd, cd_position, cd_report_side, nm_create, nm_update) VALUES(?, ?, ?, ?, ?) ";

  /* DELETE ---------------------------------- */
  /** メンテナンス画面：メニュー情報削除 */
  public static final String MENU_DEL_001 = "DELETE FROM " + system_schema + ".SYS_MENU WHERE syzkcd = ? AND cd_position = ? ";

  // ==========================================================================
  // =============
  /** REPORT */
  /* SELECT ---------------------------------- */
  /** ログイン画面：レポート分類毎の各レポート情報(複) */
  public static final String REPORT_SEL_001 = "SELECT DISTINCT T2.cd_report_no,T2.cd_report_side,T2.cd_disp_number,T3.nm_report_jsp,T3.nm_report,T3.nm_short,T3.custom_value, T1.ENABLE_MENU, T3.YOBI_1, T3.YOBI_2, T3.YOBI_3, T3.YOBI_4, T3.YOBI_5 FROM " + system_schema + ".SYS_MENU T1 INNER JOIN "
      + system_schema + ".SYS_REPORT_AUTH T2 ON T1.cd_report_no=T2.cd_report_no INNER JOIN " + system_schema + ".SYS_REPORT_NAME T3 ON T1.cd_report_no=T3.cd_report_no WHERE T1.cd_group IN (SELECT T4.cd_group FROM " + system_schema
      + ".SYS_USER_GROUP T4 WHERE T4.cd_user=?) AND T1.cd_position IN (SELECT T5.cd_position FROM " + system_schema + ".SYS_USER_POS T5 WHERE T5.cd_user=?) AND T2.cd_report_side=? ORDER BY T2.CD_DISP_NUMBER ASC";

  /** ユーザーレポート管理画面 レポートコードの名前を取得 */
  public static final String REPORT_SEL_002 = "SELECT * FROM " + system_schema + ".SYS_REPORT_NAME ORDER BY cd_report_no ASC";

  /** ユーザーレポート管理画面 SYS_MENUの値を取得 */
  public static final String REPORT_SEL_003 = "SELECT CD_GROUP,CD_POSITION,CD_REPORT_NO,ENABLE_MENU FROM " + system_schema + ".SYS_MENU ORDER BY CD_GROUP ASC";

  /* UPDATE ---------------------------------- */
  /** ユーザーレポート管理メンテナンス画面 ユーザーレポート管理 */
  public static final String REPORT_UPD_001 = "UPDATE " + system_schema + ".SYS_MENU SET cd_group = ?, cd_position = ?, cd_report_no = ? , enable_menu = ?, dt_update = CURRENT TIMESTAMP WHERE cd_group = ? and cd_position = ? AND cd_report_no = ? ";

  /* INSERT ---------------------------------- */
  /** ユーザーレポート管理メンテナンス画面 */
  public static final String REPORT_INS_001 = "INSERT INTO " + system_schema + ".SYS_MENU (cd_group, cd_position, cd_report_no, enable_menu, nm_create, dt_create, nm_update, dt_update ) VALUES(?, ?, ?, ?, ?,CURRENT TIMESTAMP, ?, CURRENT TIMESTAMP)";

  /* DELETE ---------------------------------- */
  /** ユーザーレポート管理メンテナンス画面 */
  public static final String REPORT_DEL_001 = "DELETE FROM " + system_schema + ".SYS_MENU WHERE CD_GROUP = ? AND cd_position = ? AND CD_REPORT_NO = ?";

  // ==========================================================================
  // ===============
  /** REPORT PARAMETER */
  /* SELECT ------------------------------- */
  /** ログイン画面：レポート分類ごとの各レポート情報 */
  public static final String REPORT_PARAMETER_001 = "SELECT T2.cd_report_side,T3.nm_report_side,T2.cd_disp_number,T3.CUSTOM_VALUE  FROM " + system_schema + ".SYS_MENU T1 INNER JOIN " + system_schema + ".SYS_REPORT_AUTH T2 ON ( T2.CD_REPORT_NO = T1.CD_REPORT_NO )LEFT JOIN " + system_schema
      + ".SYS_REPORT_SIDE T3 ON (T2.CD_REPORT_SIDE = T3.CD_REPORT_SIDE) WHERE T1.CD_GROUP = ? AND T1.cd_position = ? AND NOT T2.CD_REPORT_SIDE LIKE 'M%' GROUP BY T2.cd_report_side,T3.nm_report_side,T2.cd_disp_number,T3.CUSTOM_VALUE ORDER BY T2.cd_disp_number ASC ";

  public static final String REPORT_PARAMETER_002 = "SELECT CUSTOM_VALUE FROM " + system_schema + ".SYS_REPORT_SIDE WHERE CD_REPORT_SIDE = ?";

  public static final String REPORT_PARAMETER_003 =
      "SELECT DISTINCT T2.custom_value FROM " + system_schema + ".SYS_MENU T1 INNER JOIN " + system_schema + ".SYS_GROUPS T2 ON T1.cd_group = T2.cd_group INNER JOIN " + system_schema + ".SYS_USER_GROUP T3 ON T3.cd_group = T2.cd_group WHERE T1.cd_report_no = ? AND T3.cd_user = ?";

  public static final String REPORT_PARAMETER_004 =
      "SELECT DISTINCT T2.custom_value FROM " + system_schema + ".SYS_MENU T1 INNER JOIN " + system_schema + ".sys_posmst T2 ON T1.cd_position = T2.cd_position INNER JOIN " + system_schema + ".SYS_USER_POS T3 ON T3.cd_position = T2.cd_position WHERE T1.cd_report_no = ? AND T3.cd_user = ?";

  // ==========================================================================
  // =============
  /** POSITION */
  /* リストボックス ---------------------------------- */
  /** メンテナンス画面：権限管理情報----ロールマスタ(複) */
  public static final String POS_SEL_001 = "SELECT * FROM " + system_schema + ".sys_posmst ORDER BY NM_POSITION";

  /* SELECT ---------------------------------- */
  /** メンテナンス画面：権限情報(複) */
  public static final String POS_SEL_002 = "SELECT t1.cd_user,t2.cd_position, t2.nm_position FROM " + system_schema + ".SYS_USER_POS t1 INNER JOIN " + system_schema + ".SYS_POSMST t2 on (t1.cd_position = t2.cd_position) ";

  // ==========================================================================
  // =============
  /** GROUP */
  /* SELECT ---------------------------------- */
  /** メンテナンス画面：グループ(複) */
  // yofrdt
  public static final String ATH_SEL_001 = "SELECT * FROM " + system_schema + ".SYS_GROUPS ORDER BY NM_GROUP";

  /* SELECT ---------------------------------- */
  /** メンテナンス画面：所属情報(複) */
  //
  public static final String ATH_SEL_002 = "SELECT T1.CD_USER,T2.CD_GROUP,T2.NM_GROUP  FROM " + system_schema + ".SYS_USER_GROUP T1  LEFT JOIN " + system_schema + ".SYS_GROUPS T2 ON (T1.CD_GROUP = T2.CD_GROUP) ORDER BY T1.CD_USER";

  // ==========================================================================
  // =============
  /** SIDE */
  /* SELECT ---------------------------------- */
  /** ログイン画面：マスターを除外したレポート分類取得(複) */
  public static final String SIDE_SEL_001 = "SELECT T2.cd_report_side,T3.nm_report_side,T3.cd_disp_number,T3.cd_disp_column,T3.custom_value FROM " + system_schema + ".SYS_MENU T1 INNER JOIN " + system_schema + ".SYS_REPORT_AUTH T2 ON T2.cd_report_no=T1.cd_report_no INNER JOIN " + system_schema
      + ".SYS_REPORT_SIDE T3 ON T2.cd_report_side=T3.cd_report_side AND T3.KBN_MENU = ? WHERE T3.CUSTOM_VALUE<>'mobile' and T1.cd_group IN (SELECT T4.cd_group FROM " + system_schema + ".SYS_USER_GROUP T4 WHERE T4.cd_user=?) AND T1.cd_position IN (SELECT T5.cd_position FROM " + system_schema
      + ".SYS_USER_POS   T5 WHERE T5.cd_user=?) GROUP BY T2.cd_report_side,T3.nm_report_side,T3.cd_disp_number,T3.cd_disp_column,T3.custom_value ORDER BY T3.cd_disp_column,T3.cd_disp_number ASC";

  /** ログイン画面：マスターのみレポート分類取得(複) */
  public static final String SIDE_SEL_002 = "SELECT  T2.cd_report_side,T3.nm_report_side,T3.cd_disp_number FROM " + system_schema + ".SYS_MENU T1 INNER JOIN " + system_schema + ".SYS_REPORT_AUTH T2 ON ( T2.CD_REPORT_NO = T1.CD_REPORT_NO ) LEFT JOIN " + system_schema
      + ".SYS_REPORT_SIDE T3 ON (T2.CD_REPORT_SIDE = T3.CD_REPORT_SIDE) WHERE T1.cd_group IN (SELECT T4.cd_group FROM " + system_schema + ".SYS_USER_GROUP T4 WHERE T4.cd_user=?) AND T1.cd_position IN (SELECT T5.cd_position FROM " + system_schema
      + ".SYS_USER_POS   T5 WHERE T5.cd_user=?) AND T2.CD_REPORT_SIDE < 0 GROUP BY T2.cd_report_side,T3.nm_report_side,T3.cd_disp_number ORDER BY T3.cd_disp_number ASC ";

  /** メンテナンス画面：メニューに紐付くレポート分類情報(複) */
  public static final String SIDE_SEL_003 =
      "SELECT T1.cd_report_side, T2.nm_report_side FROM " + system_schema + ".SYS_MENU T1 INNER JOIN " + system_schema + ".SYS_REPORT_SIDE T2 ON ( T2.cd_report_side = T1.cd_report_side ) WHERE T1.syzkcd = ? AND T1.cd_position = ? ORDER BY T1.cd_report_side ASC, T2.cd_disp_number ASC ";

  /** メンテナンス画面：レポート分類情報(複) */
  public static final String SIDE_SEL_004 = "SELECT cd_report_side, nm_report_side,cd_disp_column FROM " + system_schema + ".SYS_REPORT_SIDE ORDER BY cd_report_side ASC,cd_disp_number ASC ";

  // ==========================================================================
  // =============
  /** SIDE */
  /* SELECT ---------------------------------- */
  /** 各種画面：SYS_LOGテーブルより更新状況フラグレコード数取得(単) */
  public static final String SYSLOG_SEL_001 = "SELECT count(*) FROM " + system_schema + ".sys_log ";

  /** 各種画面：SYS_LOGテーブルより更新状況フラグを取得(単) */
  public static final String SYSLOG_SEL_002 = "SELECT T1.cd_flag FROM " + system_schema + ".sys_log T1 ";

  // ==========================================================================
  // =============
  /** REPORT_AUTH */
  /** レポート管理メンテナンス画面 */
  public static final String ATH_REPORT_SEL_001 = "SELECT CD_REPORT_NO,CD_REPORT_SIDE,CD_DISP_NUMBER FROM " + system_schema + ".SYS_REPORT_AUTH ORDER BY CD_REPORT_SIDE,CD_DISP_NUMBER";

  /* UPDATE ---------------------------------- */
  /** ユーザーレポート管理メンテナンス画面 */
  public static final String ATH_REPORT_UPD_001 = "UPDATE " + system_schema + ".SYS_REPORT_AUTH SET cd_report_no = ?, cd_report_side = ?, cd_disp_number = ? , dt_update = CURRENT TIMESTAMP WHERE cd_report_no = ? and cd_report_side = ? AND cd_disp_number = ? ";

  /* INSERT ---------------------------------- */
  /** ユーザーレポート管理メンテナンス画面 */
  public static final String ATH_REPORT_INS_001 = "INSERT INTO " + system_schema + ".SYS_REPORT_AUTH (cd_report_no, cd_report_side, cd_disp_number,nm_create, dt_create, nm_update, dt_update ) VALUES(?, ?, ?, ?,CURRENT TIMESTAMP, ?, CURRENT TIMESTAMP)";

  /* DELETE ---------------------------------- */
  /** ユーザーレポート管理メンテナンス画面 */
  public static final String ATH_REPORT_DEL_001 = "DELETE FROM " + system_schema + ".SYS_REPORT_AUTH WHERE CD_REPORT_NO = ? AND cd_REPORT_SIDE = ? AND CD_DISP_NUMBER = ?";

  // ==========================================================================
  // =============
  /** REPORT_AUTH */
  /** GROUP */
  /* SELECT ---------------------------------- */
  /** メンテナンス画面：グループ(複) */
  // yofrdt
  public static final String ATH_SEL_003 = "SELECT * FROM " + system_schema + ".SYS_GROUPS ORDER BY CD_GROUP";

  /* UPDATE ---------------------------------- */
  /** グループマスタメンテナンス画面 */
  public static final String ATH_GROUP_UPD_001 = "UPDATE " + system_schema + ".SYS_GROUPS SET CD_GROUP = ?, NM_GROUP = ?,CUSTOM_VALUE = ?, nm_update = ?, dt_update =  CURRENT TIMESTAMP WHERE cd_GROUP = ? ";

  /* INSERT ---------------------------------- */
  /** グループマスタメンテナンス画面 */
  public static final String ATH_GROUP_INS_001 = "INSERT INTO " + system_schema + ".SYS_GROUPS (CD_GROUP,NM_GROUP, CUSTOM_VALUE,nm_create, dt_create, nm_update, dt_update ) VALUES(?, ?, ?, ?,CURRENT TIMESTAMP, ?, CURRENT TIMESTAMP)";

  /* DELETE ---------------------------------- */
  /** グループマスタメンテナンス画面 グループマスタ */
  public static final String ATH_GROUP_DEL_001 = "DELETE FROM " + system_schema + ".SYS_GROUPS WHERE CD_GROUP = ?";

  /** グループマスタメンテナンス画面 ユーザーグループ管理 */
  public static final String ATH_GROUP_DEL_002 = "DELETE FROM " + system_schema + ".SYS_USER_GROUP WHERE CD_GROUP = ?";

  /** グループマスタメンテナンス画面 ユーザーレポート管理 */
  public static final String ATH_GROUP_DEL_003 = "DELETE FROM " + system_schema + ".SYS_MENU WHERE CD_GROUP = ?";

  /** グループマスタメンテナンス画面 お知らせ管理 */
  public static final String ATH_GROUP_DEL_004 = "DELETE FROM " + system_schema + ".M_INFO_AUTH WHERE CD_GROUP = ?";

  /** グループマスタメンテナンス画面 */
  public static final String ATH_GEOUP_ROOL_CHECK =
      "SELECT  T1.cd_position FROM " + system_schema + ".SYS_MENU T1 WHERE T1.cd_group IN (SELECT T2.cd_group FROM " + system_schema + ".SYS_USER_GROUP T2 WHERE T2.cd_user=?)  AND T1.cd_position IN (SELECT T3.cd_position FROM " + system_schema + ".SYS_USER_POS T3 WHERE T3.cd_user=?) ";

  // ==========================================================================
  // =============
  /** POSMST */
  /* SELECT ---------------------------------- */
  /** メンテナンス画面：グループ(複) */
  // yofrdt
  public static final String POSMST_SEL_001 = "SELECT * FROM " + system_schema + ".SYS_POSMST ORDER BY CD_POSITION";

  /* UPDATE ---------------------------------- */
  /** グループマスタメンテナンス画面 */
  public static final String POSMST_UPD_001 = "UPDATE " + system_schema + ".SYS_POSMST SET CD_POSITION = ?, NM_POSITION = ?,CUSTOM_VALUE = ?, nm_update = ?, dt_update =  CURRENT TIMESTAMP WHERE CD_POSITION = ? ";

  /* INSERT ---------------------------------- */
  /** グループマスタメンテナンス画面 */
  public static final String POSMST_INS_001 = "INSERT INTO " + system_schema + ".SYS_POSMST (CD_POSITION,NM_POSITION, CUSTOM_VALUE,nm_create, dt_create, nm_update, dt_update ) VALUES(?, ?, ?, ?,CURRENT TIMESTAMP, ?, CURRENT TIMESTAMP)";

  /* DELETE ---------------------------------- */
  /** グループマスタメンテナンス画面 ロールマスタ */
  public static final String POSMST_DEL_001 = "DELETE FROM " + system_schema + ".SYS_POSMST WHERE CD_POSITION = ?";

  /** グループマスタメンテナンス画面 ユーザーロール管理 */
  public static final String POSMST_DEL_002 = "DELETE FROM " + system_schema + ".SYS_USER_POS WHERE CD_POSITION = ?";

  /** グループマスタメンテナンス画面 ユーザーレポート管理 */
  public static final String POSMST_DEL_003 = "DELETE FROM " + system_schema + ".SYS_MENU WHERE CD_POSITION = ?";

  /** グループマスタメンテナンス画面 お知らせ管理 */
  public static final String POSMST_DEL_004 = "DELETE FROM " + system_schema + ".M_INFO_AUTH WHERE CD_POSITION = ?";

  // ==========================================================================
  // =============
  /** SYS_REPORT_SIDE */
  /* SELECT ---------------------------------- */
  /** メンテナンス画面：グループ(複) */
  public static final String MTN_SIDE_SEL_001 = "SELECT * FROM " + system_schema + ".SYS_REPORT_SIDE ORDER BY CD_REPORT_SIDE";

  /* UPDATE ---------------------------------- */
  /** ﾚﾎﾟｰﾄ分類マスタ画面 */
  public static final String SIDE_UPD_001 = "UPDATE " + system_schema + ".SYS_REPORT_SIDE SET CD_REPORT_SIDE = ?, NM_REPORT_SIDE = ?,CD_DISP_NUMBER = ? ,CD_DISP_COLUMN = ? , CUSTOM_VALUE = ? ,nm_update = ?, dt_update =  CURRENT TIMESTAMP WHERE CD_REPORT_SIDE = ? ";

  /* INSERT ---------------------------------- */
  /** ﾚﾎﾟｰﾄ分類マスタ画面 */
  public static final String SIDE_INS_001 = "INSERT INTO " + system_schema + ".SYS_REPORT_SIDE (CD_REPORT_SIDE,NM_REPORT_SIDE,CD_DISP_NUMBER,CD_DISP_COLUMN, CUSTOM_VALUE,nm_create, dt_create, nm_update, dt_update ) VALUES(?, ?, ?,?, ?,?,CURRENT TIMESTAMP, ?, CURRENT TIMESTAMP)";

  /* DELETE ---------------------------------- */
  /** ﾚﾎﾟｰﾄ分類マスタ画面 レポート分類マスタ */
  public static final String SIDE_DEL_001 = "DELETE FROM " + system_schema + ".SYS_REPORT_SIDE WHERE CD_REPORT_SIDE = ?";

  /* DELETE ---------------------------------- */
  /** ﾚﾎﾟｰﾄ分類マスタ画面 レポート管理 */
  public static final String SIDE_DEL_002 = "DELETE FROM " + system_schema + ".SYS_REPORT_AUTH WHERE CD_REPORT_SIDE = ?";

  // ==========================================================================
  // =============
  /** SYS_REPORT_NAME */
  /* SELECT ---------------------------------- */
  /** メンテナンス画面：グループ(複) */
  public static final String MTN_REPORT_NAME_SEL_001 = "SELECT * FROM " + system_schema + ".SYS_REPORT_NAME ORDER BY CD_REPORT_NO";

  /* UPDATE ---------------------------------- */
  /** ﾚﾎﾟｰﾄマスタ画面 */
  public static final String REPORT_NAME_UPD_001 = "UPDATE " + system_schema + ".SYS_REPORT_NAME SET CD_REPORT_NO = ?, NM_REPORT = ?,NM_SHORT = ?,NM_REPORT_JSP = ?,CUSTOM_VALUE = ?, nm_update = ?, dt_update =  CURRENT TIMESTAMP WHERE CD_REPORT_NO = ? ";

  /* INSERT ---------------------------------- */
  /** ﾚﾎﾟｰﾄマスタ画面 */
  public static final String REPORT_NAME_INS_001 = "INSERT INTO " + system_schema + ".SYS_REPORT_NAME (CD_REPORT_NO,NM_REPORT,NM_SHORT,NM_REPORT_JSP, CUSTOM_VALUE,nm_create, dt_create, nm_update, dt_update ) VALUES(?, ?, ?, ?,?,?,CURRENT TIMESTAMP, ?, CURRENT TIMESTAMP)";

  /* DELETE ---------------------------------- */
  /** ﾚﾎﾟｰﾄマスタ画面 レポートマスタ */
  public static final String REPORT_NAME_DEL_001 = "DELETE FROM " + system_schema + ".SYS_REPORT_NAME WHERE CD_REPORT_NO = ?";

  /** ﾚﾎﾟｰﾄマスタ画面 レポート管理 */
  public static final String REPORT_NAME_DEL_002 = "DELETE FROM " + system_schema + ".SYS_REPORT_AUTH WHERE CD_REPORT_NO = ?";

  /** ﾚﾎﾟｰﾄマスタ画面 レポート管理 */
  public static final String REPORT_NAME_DEL_003 = "DELETE FROM " + system_schema + ".SYS_MENU WHERE CD_REPORT_NO = ?";

  // ==========================================================================
  // =============
  /** M_INFO */
  /* SELECT ---------------------------------- */
  /** メンテナンス画面：グループ(複) */
  public static final String INFO_SEL_001 = "select " + "  CD_INFO " + "  , NVL(DT_START, '') as DT_START " + "  , NVL(DT_END, '') as DT_END " + "  , FLG_DISP_ALWAYS " + "  , NO_DISP " + "  , TITLE " + "  , INFORMATION  " + "from " + system_schema + ".M_INFO " + "order by "
      + "  case FLG_DISP_ALWAYS  " + "    when '" + Consts.INFO_ALWAYS + "' then 1  " + "    else 2  " + "    end " + "  , DT_START desc " + "  , NO_DISP asc " + "  , CD_INFO asc ";

  /* UPDATE ---------------------------------- */
  /** お知らせマスタ画面 */
  public static final String INFO_UPD_001 =
      "update " + system_schema + ".M_INFO  " + "set " + "  DT_START = ? " + "  , DT_END = ? " + "  , FLG_DISP_ALWAYS = ? " + "  , NO_DISP = ? " + "  , TITLE = ? " + "  , INFORMATION = ? " + "  , NM_UPDATE = ? " + "  , DT_UPDATE = current timestamp  " + "where " + "  CD_INFO = ? ";

  /* INSERT ---------------------------------- */
  /** お知らせマスタ画面 */
  public static final String INFO_INS_001 = "insert  " + "into " + system_schema + ".M_INFO(  " + "  CD_INFO " + "  , DT_START " + "  , DT_END " + "  , FLG_DISP_ALWAYS " + "  , NO_DISP " + "  , TITLE " + "  , INFORMATION " + "  , NM_CREATE " + "  , DT_CREATE " + "  , NM_UPDATE " + "  , DT_UPDATE "
      + ")  " + "values (  " + "  ? , ? , ? , ? , ? , ? , ? , ? , current timestamp , ? , current timestamp ) ";

  /* DELETE ---------------------------------- */
  /** お知らせマスタ画面 お知らせマスタ */
  public static final String INFO_DEL_001 = "delete  " + "from " + system_schema + ".M_INFO  " + "where " + "  CD_INFO = ?";

  /** お知らせマスタ画面 お知らせ管理 */
  public static final String INFO_DEL_002 = "delete  " + "from " + system_schema + ".M_INFO_AUTH  " + "where " + "  CD_INFO = ?";

  /** お知らせマスタ画面 お知らせマスタ */
  public static final String INFO_DEL_003 = "delete  " + "from " + system_schema + ".M_INFO  " + "where " + "  FLG_DISP_ALWAYS <> '" + Consts.INFO_ALWAYS + "'  " + "  and DT_END < TO_CHAR(current date, 'yyyy/mm/dd') ";

  /** お知らせマスタ画面 お知らせ管理 */
  public static final String INFO_DEL_004 = "delete  " + "from " + system_schema + ".M_INFO_AUTH  " + "where " + "  CD_INFO in (  " + "    select " + "      CD_INFO  " + "    from " + system_schema + ".M_INFO  " + "    where " + "      FLG_DISP_ALWAYS <> '" + Consts.INFO_ALWAYS + "'  "
      + "      and DT_END < TO_CHAR(current date, 'yyyy/mm/dd') " + "  ) ";

  // ==========================================================================
  // =============
  /** M_INFO＿AUTH */
  /* SELECT ---------------------------------- */
  /** メンテナンス画面：グループ(複) */
  public static final String ATH_INFO_SEL_001 = "select " + "  T1.CD_GROUP " + "  , T1.CD_POSITION " + "  , T1.CD_INFO  " + "from " + system_schema + ".M_INFO_AUTH T1  " + "  inner join " + system_schema + ".M_INFO T2  " + "    on (T2.CD_INFO = T1.CD_INFO)  " + "order by " + "  T1.CD_GROUP "
      + "  , T1.CD_POSITION " + "  , case T2.FLG_DISP_ALWAYS  " + "    when '" + Consts.INFO_ALWAYS + "' then 1  " + "    else 2  " + "    end " + "  , T2.DT_START desc " + "  , T2.NO_DISP asc " + "  , T2.CD_INFO asc ";

  /* UPDATE ---------------------------------- */
  /** お知らせ管理画面 */
  public static final String ATH_INFO_UPD_001 =
      "update " + system_schema + ".M_INFO_AUTH  " + "set " + "  CD_GROUP = ? " + "  , CD_POSITION = ? " + "  , CD_INFO = ? " + "  , NM_UPDATE = ? " + "  , DT_UPDATE = current timestamp  " + "where " + "  CD_GROUP = ? " + "  and CD_POSITION = ? " + "  and CD_INFO = ? ";

  /* INSERT ---------------------------------- */
  /** お知らせ管理画面 */
  public static final String ATH_INFO_INS_001 =
      "insert  " + "into " + system_schema + ".M_INFO_AUTH(  " + "  CD_GROUP " + "  , CD_POSITION " + "  , CD_INFO " + "  , NM_CREATE " + "  , DT_CREATE " + "  , NM_UPDATE " + "  , DT_UPDATE " + ")  " + "values (  " + "  ? , ? , ? , ? , current timestamp , ? , current timestamp ) ";

  /* DELETE ---------------------------------- */
  /** お知らせ管理画面 レポートマスタ */
  public static final String ATH_INFO_DEL_001 = "delete  " + "from " + system_schema + ".M_INFO_AUTH  " + "where " + "  CD_GROUP = ? " + "  and CD_POSITION = ? " + "  and CD_INFO = ? ";

  // ==========================================================================
  // =============
  /** REPORT_title */
  /** グループマスタメンテナンス画面 */
  public static final String REPORT_NAME_TITLE = "SELECT DISTINCT * FROM " + system_schema + ".SYS_REPORT_NAME WHERE CD_REPORT_NO = ? ORDER BY CD_REPORT_NO";

  // ==========================================================================
  // ==============
  /** SYS_MENU **/
  // *SELECT ------------------------------*/
  public static final String SYS_MENU_001 = "SELECT * FROM " + system_schema + ".SYS_MENU ORDER BY DT_CREATE";

  // ==========================================================================
  // ==============
  /* UPDATE ---------------------------------- */
  /** パスワード変更画面 */
  public static final String PASSCHANGE_UPDATE_001 = "UPDATE " + system_schema + ".sys_users SET PASSWORDS = ?, nm_update = ?, dt_update = CURRENT TIMESTAMP WHERE cd_user = ? ";

  // =====KEYSYS================================================================
  // ===================
  /* UPLOAD ---------------------------------- */
  /** アップロードテーブルの削除 */
  public static final String TABLE_DELETE_KEYSYS = "DELETE FROM " + system_schema + ".";
  /* INSERT ---------------------------------- */
  /** ファイルアップロード グループマスタ */
  public static final String UPDATE_GROUP_INS_001 = "INSERT INTO " + system_schema + ".SYS_GROUPS (CD_GROUP,NM_GROUP, CUSTOM_VALUE,nm_create, dt_create, nm_update, dt_update ) VALUES(";
  /* ファイル ロールマスタ */
  public static final String UPDATE_POS_INS_001 = "INSERT INTO " + system_schema + ".SYS_POSMST (CD_POSITION,NM_POSITION, CUSTOM_VALUE,nm_create, dt_create, nm_update, dt_update ) VALUES(";
  /** ﾚﾎﾟｰﾄ分類マスタ画面 */
  public static final String UPDATE_SIDE_INS_001 = "INSERT INTO " + system_schema + ".SYS_REPORT_SIDE (CD_REPORT_SIDE,NM_REPORT_SIDE,CD_DISP_NUMBER, CUSTOM_VALUE,nm_create, dt_create, nm_update, dt_update ) VALUES(";
  /** ﾚﾎﾟｰﾄマスタ画面 */
  public static final String UPDATE_REPORT_NAME_INS_001 = "INSERT INTO " + system_schema + ".SYS_REPORT_NAME (CD_REPORT_NO,NM_REPORT,NM_SHORT,NM_REPORT_JSP, CUSTOM_VALUE,nm_create, dt_create, nm_update, dt_update ) VALUES(";

  /* DOWNLOAD------------------------^-^ */
  /** エクスポート処理 */
  public static final String TABLE_DOWN_LOAD = "export to ";
  public static final String TABLE_DOWN_LOAD2 = " of del select * from " + system_schema + ".";
  public static final String TABLE_DOWN_LOAD3 = "SELECT * FROM " + system_schema + ".";

  // ====KEYMST================================================================
  // ====================
  /* UPLOAD ---------------------------------- */
  /** アップロードテーブルの削除 */
  public static final String DEL_KEYMST_TABLE = "DELETE FROM " + system_schema_02 + ".";

  /** アップロードテーブルの削除(締め日マスタ) */
  public static final String TABLE_DELETE_2_CLOSE = "DELETE FROM " + system_schema_02 + ".MST_CLOSE WHERE CD_COMPANY=? AND NO_YEAR=? AND NO_MONTH=?";

  public static final String TABLE_DELETE_YM_DATE = "CREATE TABLE KEYMST.FM_DATE(DT_APPLY	CHAR(6) NOT NULL WITH DEFAULT)";

  // 組織マスタ
  public static final String TABLE_DELETE_ORGANIZATION_IMPORT =
      "CREATE TABLE KEYMST.FM_DATE_IMPORT( 	DT_APPLY	CHAR(6) NOT NULL WITH DEFAULT,CD_COMPANY	CHAR(3) NOT NULL WITH DEFAULT,CD_PLACE	CHAR(5) NOT NULL WITH DEFAULT,SALES	DECIMAL(11,2),CONSTRUCTION	DECIMAL(11,2),PEOPLE	DECIMAL(12,2),DT_UPDATE	TIMESTAMP,NM_UPDATE	VARCHAR(60), 	PRIMARY KEY ( DT_APPLY,CD_COMPANY,CD_PLACE))";
  public static final String TABLE_DELETE_ORGANIZATION = "DELETE FROM " + system_schema_02 + ".MST_ORGANIZATION WHERE DT_APPLY=?";
  public static final String TABLE_DROP = "DROP TABLE KEYMST.FM_DATE_IMPORT";

  /* INSERT ---------------------------------- */
  public static final String UPDATE_MST_CLASS = "INSERT INTO " + system_schema_02 + ".MST_CLASS (CD_CLASS_1,CD_CLASS_2,CD_CLASS_3,CD_CLASS_4,CD_CLASS_5,NM_CLASS_1,NM_CLASS_2,NM_CLASS_3,NM_CLASS_4,NM_CLASS_5,DT_UPDATE,NM_UPDATE ) VALUES(?,?,?,?,?,?,?,?,?,?,CURRENT TIMESTAMP,?)";
  public static final String UPDATE_MST_ACCOUNT =
      "INSERT INTO KEYMST.MST_ACCOUNT (CD_ACCOUNT ,CD_ACCOUNT_SUB ,F101 ,F102 ,F103 ,F104 ,F105 ,F106 ,F107 ,F108 ,F109 ,F110 ,F111 ,F201 ,F202 ,F203 ,F204 ,F205 ,F206 ,F207 ,F208 ,F209 ,F210 ,F211 ,F212 ,F213 ,F214 ,F215 ,F216 ,F217 ,F218 ,F219 ,F220 ,F221 ,F222 ,F223 ,F224 ,F225,F226 ,F301 ,F302 ,F303 ,F304 ,F305 ,F306 ,F307 ,F308 ,F309 ,F310 ,F311 ,F401 ,F402 ,F403 ,F404 ,F405 ,F406 ,F407 ,F408 ,F409 ,F410 ,F411 ,F412 ,F413 ,F414 ,F415 ,F416 ,F417 ,F418 ,F419,F420 ,DT_UPDATE ,NM_UPDATE) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,CURRENT TIMESTAMP,?)";
  public static final String UPDATE_MST_FRAMEWORK = "INSERT INTO " + system_schema_02 + ".MST_FRAMEWORK (CD_COMPANY,CD_SECTION,FG_TAKING,DT_UPDATE,NM_UPDATE) VALUES(?,?,?,CURRENT TIMESTAMP,?)";
  public static final String UPDATE_MST_DEVISION = "INSERT INTO " + system_schema_02 + ".MST_DEVISION (CD_DIVISION,CD_CLASS_1,CD_CLASS_2,CD_CLASS_3,CD_CLASS_4,CD_CLASS_5,DT_UPDATE,NM_UPDATE ) VALUES(?,?,?,?,?,?,CURRENT TIMESTAMP,?)";
  public static final String INSERT_MST_CLOSE = "INSERT INTO " + system_schema_02 + ".MST_CLOSE (CD_COMPANY,NO_YEAR,NO_MONTH,DT_CLOSE,FG_END,DT_UPDATE,NM_UPDATE) VALUES(?,?,?,?,?,CURRENT TIMESTAMP,?)";

  public static final String UPDATE_MST_CLOSE = "UPDATE " + system_schema_02 + ".MST_CLOSE SET CD_COMPANY = ?, NO_YEAR = ?,NO_MONTH = ?, DT_CLOSE = ?, FG_END = ?, DT_UPDATE =  CURRENT TIMESTAMP,NM_UPDATE=? WHERE  CD_COMPANY = ? AND  NO_YEAR = ? AND NO_MONTH = ? ";

  // 削除用テーブルにインサート
  public static final String UPDATE_MST_ORGANIZATION = "INSERT INTO " + system_schema_02 + ".FM_DATE_IMPORT (DT_APPLY,CD_COMPANY,CD_PLACE,SALES,CONSTRUCTION,PEOPLE,DT_UPDATE,NM_UPDATE) VALUES(?,?,?,?,?,?,CURRENT TIMESTAMP,?)";

  public static final String UPDATE_MST_ORGANIZATION_2 = "INSERT INTO " + system_schema_02
      + ".MST_ORGANIZATION(DT_APPLY	,CD_COMPANY	,CD_PLACE	,SALES		,CONSTRUCTION	,PEOPLE		,DT_UPDATE	,NM_UPDATE	) SELECT DT_APPLY	,CD_COMPANY	,CD_PLACE	,SALES		,CONSTRUCTION	,PEOPLE		,DT_UPDATE	,NM_UPDATE	FROM " + system_schema_02 + ".FM_DATE_IMPORT";

  // 削除用データ取得
  public static final String COUNT_DATE = "SELECT DISTINCT DT_APPLY FROM " + system_schema_02 + ".FM_DATE_IMPORT";

  /* DOWNLOAD------------------------^ */
  public static final String TABLE_DOWN_LOAD4 = "SELECT * FROM " + system_schema_02 + ".";

  // マスターマスタてーぶるから項目取得
  public static final String MASTER_TABLE_SEL = "SELECT NM_TABLE,NM_COLUMN,NM_VIEW_NAME,CD_FORMAT,ORD_SORT,REMARK FROM " + system_schema_02 + ".MST_MASTER WHERE NM_TABLE=";

  public static final String MASTER_TABLE_SEL_WHERE_AND = " AND NM_COLUMN NOT LIKE '--' ";

  public static final String MASTER_TABLE_SEL_WHERE = " ORDER BY ORD_SORT";

  public static final String MASTER_TABLE_SEL_MAX_SORT_UP = "SELECT MAX(ORD_SORT) AS ORD_SORT FROM " + system_schema_02 + ".MST_MASTER WHERE NM_TABLE=? AND NM_COLUMN NOT LIKE '--'";

  public static final String MASTER_TABLE_NAME = "SELECT MIN(REMARK) AS REMARK FROM " + system_schema_02 + ".MST_MASTER WHERE NM_TABLE=?";

  public static final String MASTER_TABLE_SEL_MAX_SORT = "SELECT MAX(ORD_SORT) AS ORD_SORT FROM " + system_schema_02 + ".MST_MASTER WHERE NM_TABLE=?";

  public static final String DL_ORGANIZATION = "SELECT DISTINCT " + "T3.DT_APPLY                  AS  " + "\"" + "DT_APPLY" + "\"" + "" + "    ," + "T3.CD_COMPANY                AS " + "\"" + "CD_COMPANY" + "\"" + "" + "," + "T3.CD_PLACE                  AS " + "\"" + "CD_PLACE" + "\"" + "" + ","
      + "LTRIM(RTRIM(T4.SOK_NAME_S))  AS " + "\"" + "-" + "\"" + "" + "," + "T3.SALES                     AS " + "\"" + "SALES" + "\"" + "" + "," + "T3.CONSTRUCTION              AS " + "\"" + "CONSTRUCTION" + "\"" + "" + "," + "T3.PEOPLE                    AS " + "\"" + "PEOPLE" + "\"" + "" + ","
      + "''                           AS " + "\"" + "--" + "\"" + "" + "" + "FROM   KEYMST.MST_ORGANIZATION      AS T3 " + "LEFT OUTER JOIN AAM.CMSOKMST AS T4 " + "ON     T3.CD_PLACE||'0' = T4.SOK_CODE " + "   AND " + "(" + "    T4.SOK_KAI_CODE = '" + SQL_ORGANIZATION01 + "' " + // 'AAM'
      "AND T4.SOK_BUN_CODE = '" + SQL_ORGANIZATION02 + "' " + // '0004'
      " OR T4.SOK_KAI_CODE =  '" + SQL_ORGANIZATION03 + "' " + // 'ASE'
      " AND T4.SOK_BUN_CODE = '" + SQL_ORGANIZATION04 + "' " + // '0001'
      ") ";
  public static final String DL_ORGANIZATION_START = "AND  (T4.SOK_START_DATE<=CURRENT DATE ";
  public static final String DL_ORGANIZATION_END = " AND T4.SOK_END_DATE  >=CURRENT DATE )";
  public static final String DL_ORGANIZATION_UNION = "UNION ALL SELECT DISTINCT " + " ''                              AS " + "\"" + "DT_APPLY" + "\"" + "" + "," + " T1.SOK_KAI_CODE                 AS " + "\"" + "CD_COMPANY" + "\"" + "" + "," + " SUBSTR(T1.SOK_CODE,1,5)         AS " + "\""
      + "CD_PLACE" + "\"" + "" + "," + " LTRIM(RTRIM(T1.SOK_NAME_S))     AS " + "\"" + "-" + "\"" + "" + "," + " 0                               AS " + "\"" + "SALES" + "\"" + "" + "," + " 0                               AS " + "\"" + "CONSTRUCTION" + "\"" + "" + ","
      + " 0                               AS " + "\"" + "PEOPLE" + "\"" + "" + "," + " '追加'                           AS " + "\"" + "--" + "\"" + "" + "" + "FROM  AAM.CMSOKMST AS T1 INNER JOIN KEYMST.MST_FRAMEWORK AS T2 ON            " + "  T1.SOK_CODE  =SUBSTR(T2.CD_SECTION,1,5)||'0' "
      + " AND T2.FG_TAKING ='1' AND ( " + "    T1.SOK_KAI_CODE =  '" + SQL_ORGANIZATION01 + "' " + // 'AAM'
      "AND T1.SOK_BUN_CODE = '" + SQL_ORGANIZATION02 + "' " + // '0004'
      " OR T1.SOK_KAI_CODE =  '" + SQL_ORGANIZATION03 + "' " + // 'ASE'
      " AND T1.SOK_BUN_CODE =  '" + SQL_ORGANIZATION04 + "' " + // '0001'
      " ) ";
  public static final String DL_ORGANIZATION_START_2 = "AND  (T1.SOK_START_DATE<= CURRENT DATE ";
  public static final String DL_ORGANIZATION_END_2 = " AND T1.SOK_END_DATE  >=CURRENT DATE )";
  public static final String DL_ORGANIZATION_WHERE =
      "WHERE NOT EXISTS (SELECT * FROM   KEYMST.MST_ORGANIZATION T7 WHERE  SUBSTR(T1.SOK_CODE,1,5) = T7.CD_PLACE )" + "ORDER BY " + "\"" + "DT_APPLY" + "\"" + "" + "    ," + "\"" + "CD_COMPANY" + "\"" + "" + "    ," + "\"" + "CD_PLACE" + "\"" + "" + "    ";

  public static final String DL_MST_DEVISION = "SELECT  T1.CD_DIVISION  AS " + "\"" + "CD_DIVISION" + "\"" + "" + "," + "LTRIM(RTRIM(T2.SOK_NAME_S))   AS " + "\"" + "-" + "\"" + "" + "," + "LTRIM(RTRIM(T1.CD_CLASS_1))   AS " + "\"" + "CD_CLASS_1" + "\"" + "" + ","
      + "LTRIM(RTRIM(T1.CD_CLASS_2))   AS " + "\"" + "CD_CLASS_2" + "\"" + "" + "," + "LTRIM(RTRIM(T1.CD_CLASS_3))   AS " + "\"" + "CD_CLASS_3" + "\"" + "" + "," + "LTRIM(RTRIM(T1.CD_CLASS_4))   AS " + "\"" + "CD_CLASS_4" + "\"" + "" + "," + "LTRIM(RTRIM(T1.CD_CLASS_5))   AS " + "\"" + "CD_CLASS_5"
      + "\"" + ", " + "''   AS " + "\"" + "--" + "\"" + " " + "FROM    KEYMST.MST_DEVISION  AS T1 " + "LEFT OUTER JOIN AAM.CMSOKMST AS T2 " + "ON (T1.CD_DIVISION||'0000' = T2.SOK_CODE) " + "AND  (T2.SOK_KAI_CODE = 'AAM' AND T2.SOK_BUN_CODE = '0004')";
  public static final String DL_MST_DEVISION_START = "AND  (T2.SOK_START_DATE<= CURRENT DATE ";
  public static final String DL_MST_DEVISION_END = " AND T2.SOK_END_DATE  >=CURRENT DATE )";
  public static final String DL_MST_DEVISION_2 = " UNION ALL " + "SELECT T9.CD_DIVISION AS " + DC + "CD_DIVISION" + DC_E + " DI_NA.SOK_NAME_S AS " + DC + "-" + DC_E + "'' AS " + DC + "CD_CLASS_1" + DC_E + "'' AS " + DC + "CD_CLASS_2" + DC_E + "'' AS " + DC + "CD_CLASS_3" + DC_E + "'' AS " + DC
      + "CD_CLASS_4" + DC_E + "'' AS " + DC + "CD_CLASS_5" + DC_E + "'追加' AS " + DC + "--" + DC + " FROM ( " + "SELECT DISTINCT SUBSTR(T3.SOK_CODE,1,2)       AS " + DC + "CD_DIVISION" + DC_E + " T3.SOK_KAI_CODE AS " + DC + "SOK_KAI_CODE" + DC_E + " T3.SOK_BUN_CODE AS " + DC + "SOK_BUN_CODE" + DC
      + " FROM  AAM.CMSOKMST   AS T3 " + " INNER JOIN KEYMST.MST_FRAMEWORK AS T4 " + " ON SUBSTR(T4.CD_SECTION,1,6) = SUBSTR(T3.SOK_CODE,1,6) AND (T4.CD_COMPANY=T3.SOK_KAI_CODE) " + " AND T4.FG_TAKING ='1'" + " AND ( T3.SOK_KAI_CODE = 'AAM' AND T3.SOK_BUN_CODE = '0004' )";
  public static final String DL_MST_DEVISION_START_2 = " AND  (T3.SOK_START_DATE<= CURRENT DATE ";
  public static final String DL_MST_DEVISION_END_2 = " AND T3.SOK_END_DATE  >=CURRENT DATE )";
  public static final String DL_MST_DEVISION_2_WHERE = " WHERE NOT EXISTS " + "(SELECT * FROM   KEYMST.MST_DEVISION AS T5 WHERE  " + "T5.CD_DIVISION = SUBSTR(T3.SOK_CODE,1,2) ) " + ") AS T9 " + " INNER JOIN  AAM.CMSOKMST AS DI_NA " + " ON DI_NA.SOK_CODE=T9.CD_DIVISION||'0000' AND"
      + " (DI_NA.SOK_KAI_CODE='AAM' AND DI_NA.SOK_BUN_CODE='0004') " + "ORDER BY " + DC + "CD_DIVISION" + DC;

  public static final String DL_MST_FRAMEWORK = "SELECT DISTINCT " + "LTRIM(RTRIM(T1.CD_COMPANY)) AS " + DC + "CD_COMPANY" + DC_E + "SUBSTR(LTRIM(RTRIM(T1.CD_SECTION)),1,6) AS " + DC + "CD_SECTION" + DC_E + "LTRIM(RTRIM(T2.SOK_NAME_S)) AS " + DC + "-" + DC_E + "T1.FG_TAKING AS" + DC + "FG_TAKING"
      + DC_E + "'' AS " + DC + "--" + DC + " FROM	KEYMST.MST_FRAMEWORK AS T1 " + "LEFT OUTER JOIN AAM.CMSOKMST AS T2 " + "ON SUBSTR(T1.CD_SECTION,1,6) = SUBSTR(T2.SOK_CODE,1,6)  AND (T1.CD_COMPANY=T2.SOK_KAI_CODE)" + "AND (	" + " T2.SOK_KAI_CODE = '" + SQL_ORGANIZATION01 + "'"
      + " AND T2.SOK_BUN_CODE = '" + SQL_ORGANIZATION02 + "'" + "  OR T2.SOK_KAI_CODE = '" + SQL_ORGANIZATION03 + "' " + " AND T2.SOK_BUN_CODE = '" + SQL_ORGANIZATION04 + "'" + ") ";
  public static final String DL_MST_FRAMEWORK_START = "AND  (T2.SOK_START_DATE<= CURRENT DATE ";
  public static final String DL_MST_FRAMEWORK_END = " AND T2.SOK_END_DATE  >=CURRENT DATE )";
  public static final String DL_MST_FRAMEWORK_UNION = "UNION ALL " + "SELECT DISTINCT LTRIM(RTRIM(T3.SOK_KAI_CODE)) AS " + DC + "CD_COMPANY" + DC_E + "SUBSTR(LTRIM(RTRIM(T3.SOK_CODE)),1,6) AS " + DC + "CD_SECTION" + DC_E + "LTRIM(RTRIM(T3.SOK_NAME_S)) AS " + DC + "-" + DC_E + "'' AS " + DC
      + "FG_TAKING" + DC_E + "'追加' AS " + DC + "--" + DC + " FROM AAM.CMSOKMST AS T3 " + "WHERE(	" + " T3.SOK_KAI_CODE = 'AAM' " + "     AND T3.SOK_BUN_CODE = '0004' " + "      OR T3.SOK_KAI_CODE = 'ASE' " + "     AND T3.SOK_BUN_CODE = '0001' " + ") ";
  public static final String DL_MST_FRAMEWORK_START_2 = "AND  (T3.SOK_START_DATE<= CURRENT DATE ";
  public static final String DL_MST_FRAMEWORK_END_2 = " AND T3.SOK_END_DATE  >=CURRENT DATE )";
  public static final String DL_MST_FRAMEWORK_WHERE =
      // "INNER JOIN KEYMST.MST_FRAMEWORK AS T4 "+
      // "ON SUBSTR(T4.CD_SECTION,1,6) = SUBSTR(T3.SOK_CODE,1,6) "+
      // "AND ( " +
      // " T3.SOK_KAI_CODE = 'AAM' "+
      // " AND T3.SOK_BUN_CODE = '0004' "+
      // " OR T3.SOK_KAI_CODE = 'ASE' "+
      // " AND T3.SOK_BUN_CODE = '0001' "+") "+
      "AND 	NOT EXISTS (" + "SELECT * " + "FROM KEYMST.MST_FRAMEWORK AS T5 " + "WHERE SUBSTR(T5.CD_SECTION,1,6) = SUBSTR(T3.SOK_CODE,1,6)  AND (T5.CD_COMPANY=T3.SOK_KAI_CODE) " + ")" + "ORDER BY " + DC + "CD_COMPANY" + DC_E + DC + "CD_SECTION" + DC;
  public static final String DL_ACCOUNT = "SELECT DISTINCT " + "LTRIM(RTRIM(T1.CD_ACCOUNT)) AS " + DC + "CD_ACCOUNT" + DC_E + "LTRIM(RTRIM(T1.CD_ACCOUNT_SUB)) AS " + DC + "CD_ACCOUNT_SUB" + DC_E + "LTRIM(RTRIM(T2.KMK_NAME)) AS " + DC + "-" + DC_E + "LTRIM(RTRIM(T3.HKM_NAME))    AS " + DC + "---"
      + DC_E + "LTRIM(RTRIM(T1.F101)) AS " + DC + "F101" + DC_E + "LTRIM(RTRIM(T1.F102)) AS " + DC + "F102" + DC_E + "LTRIM(RTRIM(T1.F103)) AS " + DC + "F103" + DC_E + "LTRIM(RTRIM(T1.F104)) AS " + DC + "F104" + DC_E + "LTRIM(RTRIM(T1.F105)) AS " + DC + "F105" + DC_E + "LTRIM(RTRIM(T1.F106)) AS "
      + DC + "F106" + DC_E + "LTRIM(RTRIM(T1.F107)) AS " + DC + "F107" + DC_E + "LTRIM(RTRIM(T1.F108)) AS " + DC + "F108" + DC_E + "LTRIM(RTRIM(T1.F109)) AS " + DC + "F109" + DC_E + "LTRIM(RTRIM(T1.F110)) AS " + DC + "F110" + DC_E + "LTRIM(RTRIM(T1.F111)) AS " + DC + "F111" + DC_E
      + "LTRIM(RTRIM(T1.F201)) AS " + DC + "F201" + DC_E + "LTRIM(RTRIM(T1.F202)) AS " + DC + "F202" + DC_E + "LTRIM(RTRIM(T1.F203)) AS " + DC + "F203" + DC_E + "LTRIM(RTRIM(T1.F204)) AS " + DC + "F204" + DC_E + "LTRIM(RTRIM(T1.F205)) AS " + DC + "F205" + DC_E + "LTRIM(RTRIM(T1.F206)) AS " + DC
      + "F206" + DC_E + "LTRIM(RTRIM(T1.F207)) AS " + DC + "F207" + DC_E + "LTRIM(RTRIM(T1.F208)) AS " + DC + "F208" + DC_E + "LTRIM(RTRIM(T1.F209)) AS " + DC + "F209" + DC_E + "LTRIM(RTRIM(T1.F210)) AS " + DC + "F210" + DC_E + "LTRIM(RTRIM(T1.F211)) AS " + DC + "F211" + DC_E
      + "LTRIM(RTRIM(T1.F212)) AS " + DC + "F212" + DC_E + "LTRIM(RTRIM(T1.F213)) AS " + DC + "F213" + DC_E + "LTRIM(RTRIM(T1.F214)) AS " + DC + "F214" + DC_E + "LTRIM(RTRIM(T1.F215)) AS " + DC + "F215" + DC_E + "LTRIM(RTRIM(T1.F216)) AS " + DC + "F216" + DC_E + "LTRIM(RTRIM(T1.F217)) AS " + DC
      + "F217" + DC_E + "LTRIM(RTRIM(T1.F218)) AS " + DC + "F218" + DC_E + "LTRIM(RTRIM(T1.F219)) AS " + DC + "F219" + DC_E + "LTRIM(RTRIM(T1.F220)) AS " + DC + "F220" + DC_E + "LTRIM(RTRIM(T1.F221)) AS " + DC + "F221" + DC_E + "LTRIM(RTRIM(T1.F222)) AS " + DC + "F222" + DC_E
      + "LTRIM(RTRIM(T1.F223)) AS " + DC + "F223" + DC_E + "LTRIM(RTRIM(T1.F224)) AS " + DC + "F224" + DC_E + "LTRIM(RTRIM(T1.F225)) AS " + DC + "F225" + DC_E + "LTRIM(RTRIM(T1.F226)) AS " + DC + "F226" + DC_E + "LTRIM(RTRIM(T1.F301)) AS " + DC + "F301" + DC_E + "LTRIM(RTRIM(T1.F302)) AS " + DC
      + "F302" + DC_E + "LTRIM(RTRIM(T1.F303)) AS " + DC + "F303" + DC_E + "LTRIM(RTRIM(T1.F304)) AS " + DC + "F304" + DC_E + "LTRIM(RTRIM(T1.F305)) AS " + DC + "F305" + DC_E + "LTRIM(RTRIM(T1.F306)) AS " + DC + "F306" + DC_E + "LTRIM(RTRIM(T1.F307)) AS " + DC + "F307" + DC_E
      + "LTRIM(RTRIM(T1.F308)) AS " + DC + "F308" + DC_E + "LTRIM(RTRIM(T1.F309)) AS " + DC + "F309" + DC_E + "LTRIM(RTRIM(T1.F310)) AS " + DC + "F310" + DC_E + "LTRIM(RTRIM(T1.F311)) AS " + DC + "F311" + DC_E + "LTRIM(RTRIM(T1.F401)) AS " + DC + "F401" + DC_E + "LTRIM(RTRIM(T1.F402)) AS " + DC
      + "F402" + DC_E + "LTRIM(RTRIM(T1.F403)) AS " + DC + "F403" + DC_E + "LTRIM(RTRIM(T1.F404)) AS " + DC + "F404" + DC_E + "LTRIM(RTRIM(T1.F405)) AS " + DC + "F405" + DC_E + "LTRIM(RTRIM(T1.F406)) AS " + DC + "F406" + DC_E + "LTRIM(RTRIM(T1.F407)) AS " + DC + "F407" + DC_E
      + "LTRIM(RTRIM(T1.F408)) AS " + DC + "F408" + DC_E + "LTRIM(RTRIM(T1.F409)) AS " + DC + "F409" + DC_E + "LTRIM(RTRIM(T1.F410)) AS " + DC + "F410" + DC_E + "LTRIM(RTRIM(T1.F411)) AS " + DC + "F411" + DC_E + "LTRIM(RTRIM(T1.F412)) AS " + DC + "F412" + DC_E + "LTRIM(RTRIM(T1.F413)) AS " + DC
      + "F413" + DC_E + "LTRIM(RTRIM(T1.F414)) AS " + DC + "F414" + DC_E + "LTRIM(RTRIM(T1.F415)) AS " + DC + "F415" + DC_E + "LTRIM(RTRIM(T1.F416)) AS " + DC + "F416" + DC_E + "LTRIM(RTRIM(T1.F417)) AS " + DC + "F417" + DC_E + "LTRIM(RTRIM(T1.F418)) AS " + DC + "F418" + DC_E
      + "LTRIM(RTRIM(T1.F419)) AS " + DC + "F419" + DC_E + "LTRIM(RTRIM(T1.F420)) AS " + DC + "F420" + DC_E + "'' AS " + DC + "--" + DC + " FROM	KEYMST.MST_ACCOUNT AS T1 " + " LEFT	JOIN AAM.CMKMKMST AS T2 " + "ON T1.CD_ACCOUNT =T2.KMK_CODE " + " AND T2.KMK_KAI_CODE='AAM' "
      + " LEFT OUTER JOIN AAM.CMHKMMST AS T3 " + " ON (T1.CD_ACCOUNT = T3.HKM_KMK_CODE AND T1.CD_ACCOUNT_SUB = T3.HKM_CODE) AND " + " T3.HKM_KAI_CODE='AAM' AND T2.KMK_HKM_KBN='1' " +

      "UNION ALL " + "SELECT DISTINCT " + "LTRIM(RTRIM(T4.KMK_CODE)) AS " + DC + "CD_ACCOUNT" + DC_E + "'__' AS " + DC + "CD_ACCOUNT_SUB" + DC_E + "LTRIM(RTRIM(T4.KMK_NAME)) AS " + DC + "-" + DC_E + "''		AS " + DC + "---" + DC_E + "'' AS " + DC + "F101" + DC_E + "'' AS " + DC + "F102" + DC_E
      + "'' AS " + DC + "F103" + DC_E + "'' AS " + DC + "F104" + DC_E + "'' AS " + DC + "F105" + DC_E + "'' AS " + DC + "F106" + DC_E + "'' AS " + DC + "F107" + DC_E + "'' AS " + DC + "F108" + DC_E + "'' AS " + DC + "F109" + DC_E + "'' AS " + DC + "F110" + DC_E + "'' AS " + DC + "F111" + DC_E
      + "'' AS " + DC + "F201" + DC_E + "'' AS " + DC + "F202" + DC_E + "'' AS " + DC + "F203" + DC_E + "'' AS " + DC + "F204" + DC_E + "'' AS " + DC + "F205" + DC_E + "'' AS " + DC + "F206" + DC_E + "'' AS " + DC + "F207" + DC_E + "'' AS " + DC + "F208" + DC_E + "'' AS " + DC + "F209" + DC_E
      + "'' AS " + DC + "F210" + DC_E + "'' AS " + DC + "F211" + DC_E + "'' AS " + DC + "F212" + DC_E + "'' AS " + DC + "F213" + DC_E + "'' AS " + DC + "F214" + DC_E + "'' AS " + DC + "F215" + DC_E + "'' AS " + DC + "F216" + DC_E + "'' AS " + DC + "F217" + DC_E + "'' AS " + DC + "F218" + DC_E
      + "'' AS " + DC + "F219" + DC_E + "'' AS " + DC + "F220" + DC_E + "'' AS " + DC + "F221" + DC_E + "'' AS " + DC + "F222" + DC_E + "'' AS " + DC + "F223" + DC_E + "'' AS " + DC + "F224" + DC_E + "'' AS " + DC + "F225" + DC_E + "'' AS " + DC + "F226" + DC_E + "'' AS " + DC + "F301" + DC_E
      + "'' AS " + DC + "F302" + DC_E + "'' AS " + DC + "F303" + DC_E + "'' AS " + DC + "F304" + DC_E + "'' AS " + DC + "F305" + DC_E + "'' AS " + DC + "F306" + DC_E + "'' AS " + DC + "F307" + DC_E + "'' AS " + DC + "F308" + DC_E + "'' AS " + DC + "F309" + DC_E + "'' AS " + DC + "F310" + DC_E
      + "'' AS " + DC + "F311" + DC_E + "'' AS " + DC + "F401" + DC_E + "'' AS " + DC + "F402" + DC_E + "'' AS " + DC + "F403" + DC_E + "'' AS " + DC + "F404" + DC_E + "'' AS " + DC + "F405" + DC_E + "'' AS " + DC + "F406" + DC_E + "'' AS " + DC + "F407" + DC_E + "'' AS " + DC + "F408" + DC_E
      + "'' AS " + DC + "F409" + DC_E + "'' AS " + DC + "F410" + DC_E + "'' AS " + DC + "F411" + DC_E + "'' AS " + DC + "F412" + DC_E + "'' AS " + DC + "F413" + DC_E + "'' AS " + DC + "F414" + DC_E + "'' AS " + DC + "F415" + DC_E + "'' AS " + DC + "F416" + DC_E + "'' AS " + DC + "F417" + DC_E
      + "'' AS " + DC + "F418" + DC_E + "'' AS " + DC + "F419" + DC_E + "'' AS " + DC + "F420" + DC_E + "'追加' AS " + DC + "--" + DC + " FROM	AAM.CMKMKMST AS T4 " +
      // " FROM KEYMST.MST_ACCOUNT AS T3 "+
      // "INNER JOIN AAM.CMKMKMST AS T4 "+
      // "ON T3.CD_ACCOUNT =T4.KMK_CODE "+
      " WHERE	NOT EXISTS(" + "SELECT * " + "FROM KEYMST.MST_ACCOUNT AS T5 " + "WHERE T5.CD_ACCOUNT =T4.KMK_CODE " + ")" + " and T4.KMK_KAI_CODE='AAM' " + "ORDER BY " + DC + "CD_ACCOUNT" + DC;

  public static final String DL_MST_CLASS = "SELECT	DISTINCT " + "T1.CD_CLASS_1 AS " + DC + "CD_CLASS_1" + DC_E + "T1.CD_CLASS_2 AS " + DC + "CD_CLASS_2" + DC_E + "T1.CD_CLASS_3 AS " + DC + "CD_CLASS_3" + DC_E + "T1.CD_CLASS_4 AS " + DC + "CD_CLASS_4" + DC_E + "T1.CD_CLASS_5 AS " + DC + "CD_CLASS_5"
      + DC_E + "T1.NM_CLASS_1 AS " + DC + "NM_CLASS_1" + DC_E + "T1.NM_CLASS_2 AS " + DC + "NM_CLASS_2" + DC_E + "T1.NM_CLASS_3 AS " + DC + "NM_CLASS_3" + DC_E + "T1.NM_CLASS_4 AS " + DC + "NM_CLASS_4" + DC_E + "T1.NM_CLASS_5 AS " + DC + "NM_CLASS_5" + DC_E + "'' 	   AS " + DC + "--" + DC
      + " FROM KEYMST.MST_CLASS AS T1 " + " UNION ALL " + " SELECT	DISTINCT T2.B1BUN1 AS " + DC + "CD_CLASS_1" + DC_E + " T3.B2BUN2 AS " + DC + "CD_CLASS_2" + DC_E + "COALESCE(T4.B3BUN3,'ZZ')  AS " + DC + "CD_CLASS_3" + DC_E + "COALESCE(T5.B4BUN4,'ZZ') AS " + DC + "CD_CLASS_4" + DC_E
      + "COALESCE(T6.B5BUN5,'ZZ') AS " + DC + "CD_CLASS_5" + DC_E + "LTRIM(RTRIM(T2.B1BNM1)) AS " + DC + "NM_CLASS_1" + DC_E + "LTRIM(RTRIM(T3.B2BNM2)) AS " + DC + "NM_CLASS_2" + DC_E + "LTRIM(RTRIM(T4.B3BNM3)) AS " + DC + "NM_CLASS_3" + DC_E + "LTRIM(RTRIM(T5.B4BNM4)) AS " + DC + "NM_CLASS_4"
      + DC_E + "LTRIM(RTRIM(T6.B5BNM5)) AS " + DC + "NM_CLASS_5" + DC_E + "'追加'  AS " + DC + "--" + DC + " FROM AAM.HB1MFP AS T2 " + " INNER JOIN AAM.HB2MFP AS T3 " + " ON(T2.B1BUN1 = T3.B2BUN1)  " + " LEFT OUTER JOIN AAM.HB3MFP AS T4 " + " ON(T2.B1BUN1 = T4.B3BUN1 AND T3.B2BUN2 = T4.B3BUN2 )  "
      + " LEFT OUTER JOIN AAM.HB4MFP AS T5 " + " ON(T2.B1BUN1 = T5.B4BUN1 AND T3.B2BUN2 = T5.B4BUN2  AND T4.B3BUN3 = T5.B4BUN3 )  " + " LEFT OUTER JOIN AAM.HB5MFP AS T6 " + " ON(T2.B1BUN1 = T6.B5BUN1 AND T3.B2BUN2 = T6.B5BUN2  AND T4.B3BUN3 = T6.B5BUN3  AND T5.B4BUN4 = T6.B5BUN4 )"
      + " WHERE	NOT EXISTS(" + "	SELECT * " + "	FROM KEYMST.MST_CLASS AS T9 " + "	WHERE T9.CD_CLASS_1 =COALESCE(T2.B1BUN1,'ZZ') AND " + "              COALESCE(LTRIM(RTRIM(T9.CD_CLASS_2)),'ZZ') =COALESCE(LTRIM(RTRIM(T3.B2BUN2)),'ZZ') "
      + "		   AND COALESCE(LTRIM(RTRIM(T9.CD_CLASS_3)),'ZZ') =COALESCE(LTRIM(RTRIM(T4.B3BUN3)),'ZZ') " + "          AND COALESCE(LTRIM(RTRIM(T9.CD_CLASS_4)),'ZZ') =COALESCE(LTRIM(RTRIM(T5.B4BUN4)),'ZZ') "
      + "	       AND COALESCE(LTRIM(RTRIM(T9.CD_CLASS_5)),'ZZ') =COALESCE(LTRIM(RTRIM(T6.B5BUN5)),'ZZ') " + "	)" + "ORDER BY " + DC + "CD_CLASS_1" + DC_E + "	 " + DC + "CD_CLASS_2" + DC_E + "	 " + DC + "CD_CLASS_3" + DC_E + "	 " + DC + "CD_CLASS_4" + DC_E + "	 " + DC + "CD_CLASS_5" + DC;

  public static final String DL_CLOSE = "SELECT DISTINCT " + "CD_COMPANY AS " + DC + "CD_COMPANY" + DC_E + "NO_YEAR AS " + DC + "NO_YEAR" + DC_E + "NO_MONTH AS " + DC + "NO_MONTH" + DC_E + "DT_CLOSE AS " + DC + "DT_CLOSE" + DC_E + "FG_END AS " + DC + "FG_END" + DC + " FROM " + system_schema_02
      + ".MST_CLOSE " + " ORDER BY " + DC + "CD_COMPANY" + DC_E + "" + DC + "NO_YEAR" + DC_E + "" + DC + "NO_MONTH" + DC;

}
