/*
 * 作成日: 2007/11/08
 *
 */
package authentication.defines;

/**
 * 定数クラス
 * システムで使用する定数をまとめて定義
 */
public final class Consts {

	/** セッション名:ユーザー情報 */
	public final static String STR_SES_LOGINUSER = "LoginUser";
	/** セッション名:ユーザー情報 */
	public final static String STR_SES_LOGINPOS = "LoginUserPosition";
	/** セッション名:ユーザー情報 */
	public final static String STR_SES_LOGINATH = "LoginUserAuth";

	/** システム管理者用番号 */
	public final static int SYSTEM_MASTER = -1;

	public final static int SYSTEM_MENTENANCE = -1;

	/** セッション名:メニュー分類 */
	public final static String STR_SES_REPSIDE = "Menu";
	/** セッション名:メニュー分類(マスタ) */
	public final static String STR_SES_MSTSIDE = "Master";

	/** セッション名:メニュー分類(マスタ) */
	public final static String STR_SES_REPORT_SIDE		= "DisplayReportSide";
	/** セッション名:メニュー分類(マスタ) */
	public final static String STR_SES_REPORT_NO			= "DisplayReportNo";
	/** セッション名:メニュー分類(マスタ) */
	public final static String STR_SES_REPORT_SIDE_ARRAY	= "ReportSide_array";
	/** セッション名:メニュー分類(マスタ) */
	public final static String STR_SES_REPORT_NO_ARRAY	= "ReportNo_array";
	/** セッション名:転送パラメータ */
	public final static String STR_SES_REPORT_SEND_PARAM	= "sendParam";
	/** セッション名:転送パラメータ */
	public final static String STR_SES_REPORT_SEND_MODE	= "sendMode";

	/** セッション名:レポート予備情報1 */
	public final static String STR_SES_REPORT_YOBI1 = "Report_yobi1";
	public final static String STR_SES_REPORT_YOBI2 = "Report_yobi2";
	public final static String STR_SES_REPORT_YOBI3 = "Report_yobi3";
	public final static String STR_SES_REPORT_YOBI4 = "Report_yobi4";
	public final static String STR_SES_REPORT_YOBI5 = "Report_yobi5";

	/** セッション名:ログイン日時 */
	public final static String STR_SES_LOGINDT = "LoginDt";

	/** 　テーブル名：　ユーザマスタ カスタムプロパティ */
	public final static String STR_SES_USERS_CUSTOM = "SYS_USERS";
	/** 　テーブル名：　グループマスタ カスタムプロパティ */
	public final static String STR_SES_GORUPS_CUSTOM = "SYS_GROUPS";
	/** 　テーブル名：　ロールマスタ カスタムプロパティ */
	public final static String STR_SES_POSMAST_CUSTOM = "SYS_POSMST";
	/** 　テーブル名：　レポート分類マスタ カスタムプロパティ */
	public final static String STR_SES_SIDE_CUSTOM = "SYS_REPORT_SIDE";
	/** 　テーブル名：レポートマスタ カスタムプロパティ */
	public final static String STR_REPORT_NAME_CUSTOM = "SYS_REPORT_NAME";

	/** 　テーブル名：　品目分類マスタ　 */
	public final static String MST_CLASS = "MST_CLASS";
	/** 　テーブル名：　組織マスタ　 */
	public final static String MST_ORGANIZATION = "MST_ORGANIZATION";
	/** 　テーブル名： 組織体系マスタ */
	public final static String MST_FRAMEWORK = "MST_FRAMEWORK";
	/** 　テーブル名： 勘定科目マスタ */
	public final static String MST_ACCOUNT = "MST_ACCOUNT";
	/** 　テーブル名： 締め日マスタ */
	public final static String MST_CLOSE = "MST_CLOSE";
	/** 　テーブル名： 事業振分マスタ */
	public final static String MST_DEVISION = "MST_DEVISION";

	/***/
	public final static String SHIWAKE_REPORT_KAN = "SHIWAKE_REPORT_KAN";
	public final static String SHIWAKE_REPORT_AITE = "SHIWAKE_REPORT_AITE";
	public final static String SHIWAKE_REPORT_ASHWKB = "SHIWAKE_REPORT_ASHWKB";
	public final static String SHIWAKE_REPORT_ASHWKB_MIN = "SHIWAKE_REPORT_ASHWKB_MIN";

	/** リクエスト名:選択レコード */
	public final static String STR_REQ_REC = "SelectedRecord";
	/** セッション名:グリッドリスト */
	public final static String STR_SES_GRD = "GridData";
	public final static String STR_SES_GRD2 = "GridData2";
	public final static String STR_SES_GRD3 = "GridData3";
	public final static String STR_SES_GRD4 = "GridData4";

	public final static String STR_SES_GRD5 = "GridData5";
	public final static String STR_SES_GRD6 = "GridData6";

	/** アップデート */
	public final static String UPD_DETA_LIST = "UPD_LIST";

	/** レポートメンテナンスのタイトル */
	public final static String STR_SES_TITLE = "Title";

	/** ヘッダー表示タイプ(メニュー、ログオフのリンクボタン表示非表示) */
	public final static String STR_HEAD_TYPE = "headtype";
	/** メニュー、ログオフのリンクボタン表示 */
	public final static String STR_HEAD_TYPE_ON = "on";

	/** システム管理者情報:ID */
	public final static String STR_ADMIN_ID = "SYSADMIN";
	/** システム管理者情報:ﾊﾟｽﾜｰﾄﾞ */
	public final static String STR_ADMIN_PASS = "password";
	/** システム管理者情報:ユーザー名 */
	public final static String STR_ADMIN_NAME = "システム管理者";
	/** システム管理者情報:所属 */
	public final static String STR_ADMIN_AUTH = "ETN";
	/** システム管理者情報:所属名 */
	public final static String STR_ADMIN_AUTH_NAME = "システム";
	/** システム管理者情報:権限 */
	public final static String STR_ADMIN_POSITION = "99";
	/** システム管理者情報:権限名 */
	public final static String STR_ADMIN_POSITION_NAME = "システム管理権限";

	/** URLエンコード・URLデコードする際の文字エンコード */
	public final static String ENCODE = "UTF-8";

	/***/
	public final static String SQL_MESSAGE_IN = "登録";
	/** パスワード変更画面 */
	public final static String SQL_MESSAGE_CHENGE = "変更";

	public final static String SQL_MESSAGE_DELETE = "削除";

	/** アップロード画面 */
	public final static String SQL_MESSAGE_UPLOAD = "アップロード";
	public final static String SQL_MESSAGE_DOWNLOAD = "ダウンロード";

	/** web.xmlパラメータ */
	public final static String PAGE_ERROR = "Page_Error";
	public final static String CLOSE_TIME = "CloseTime";
	public final static String WAIT_TIME = "WaitTime";
	public final static String LOGIN_FORM = "Loginform";
	public final static String FROM_DATA = "FROM_DATA";
	public final static String TO_DATA = "TO_DATA";
	public final static String FROM_DATA_MSG = "FROM_DATA_MSG";
	public final static String TO_DATA_MSG = "TO_DATA_MSG";

	/** お知らせ */
	public final static int INFO_FREE_CD = -99;
	public final static String INFO_FREE_NM = "お知らせ_ログイン画面";

	public final static String INFO_ALWAYS = "1";

}
