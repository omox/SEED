/*
 * 作成日: 2006/08/04
 *
 */
package authentication.defines;

/**
 * 定数クラス <br />
 * システムで使用する定数をまとめて定義。 <br />
 */
public final class Defines {

	/** クロスコンテキストに利用するリクエスト情報
	 *
	 */
	// ユーザーID
	public final static String ID_REQUEST_USER_ID			=	"uid";
	// ユーザーカスタムプロパティ
	public final static String ID_REQUEST_CUSTOM_USER		=	"cu";
	// グループカスタムプロパティ
	public final static String ID_REQUEST_CUSTOM_GROUP	=	"cg";
	// ロールカスタムプロパティ
	public final static String ID_REQUEST_CUSTOM_POS		=	"cp";
	// 分類カスタムプロパティ
	public final static String ID_REQUEST_CUSTOM_SIDE		=	"cs";
	// レポートカスタムプロパティ
	public final static String ID_REQUEST_CUSTOM_REPORT	=	"cr";
	// レポートJSP
	public final static String ID_REQUEST_JSP_REPORT		=	"jsp";
	// レポートタイトル
	public final static String ID_REQUEST_TITLE_REPORT	=	"title";


	/** SQL-PREFIX:加盟店ロール値(LIVINS) */
	public final static String VALUE_LIVINS_ROLL	=	"0001";

	/** JSPファイル名:エラー画面 */
	public final static String STR_JSP_ERROR = "/frame/jsp/Error.jsp";

	/** JSPファイルパス:ダッシュボード画面 */
	public final static String STR_JSP_DB_PATH = "/JSP/dashboard";

	/** JSPファイル名:時間外画面 */
	public final static String STR_JSP_WORK = "/frame/jsp/CloseTime.jsp";

	/** セッション名:パスワード有効期限 */
	public final static String STR_SES_PASS_TERM_MSG = "passwordTermMsg";

	/** フォーム名:画面メッセージ */
	public final static String STR_FRM_MSG = "formMsg";
	/** フォーム名:画面メッセージタイプ */
	public final static String STR_FRM_MSGTYPE = "msg";

	/** セッション名:ダッシュボード画面 ReportName */
	public final static String SES_DB_REP_NAME = "Ses_Report_Name";
	/** セッション名:ダッシュボード画面 YM */
	public final static String SES_DB_YM = "Ses_YM";
	/** セッション名:ダッシュボード画面 REPORT_TYPE */
	public final static String SES_DB_REP_TYPE = "Ses_Report_Type";

	/** フォームの値:画面メッセージ表示タイプ エラー */
	public final static String STR_VAL_MSGTYPE_ERR = "errmsg";

	/** システム管理者情報:ID */
	public final static String STR_ADMIN_ID = "sysadmin";
	/** システム管理者情報:ﾊﾟｽﾜｰﾄﾞ */
	public final static String STR_ADMIN_PASS = "atlaspass";
	/** システム管理者情報:ユーザー名 */
	public final static String STR_ADMIN_NAME = "システム管理者";
	/** システム管理者情報:メニュー表示用権限 */
	public final static String STR_ADMIN_AUTH = "99";

	/** TermDateFormat */
	public final static String DT_PW_FORMAT_DISP = "yyyy/MM/dd";
	/** TermDateFormat */
	public final static String DT_PW_FORMAT_SAVE = "yyyyMMdd";

	/** パスワードの有効期限が切れている場合のメッセージ */
	public final static String MSG_LUSER_PASSOUT = "パスワードの有効期限が切れています。";

}
