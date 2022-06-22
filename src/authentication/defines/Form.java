/*
 * 作成日: 2007/10/20
 * 更新日: 2018/12/04
 */
package authentication.defines;

/**
 * 画面項目名クラス
 * 各画面にて使用する項目名をまとめて定義
 */
public final class Form {

	/** ログイン画面:ユーザーID */
	public final static String LOGIN_USER = "User";
	/** ログイン画面:パスワード */
	public final static String LOGIN_PASS = "Pass";
	/** ログイン画面：表示	 */
	public final static String LOGIN_VIEW = "view";
	/** ログイン画面:次処理 */
	public final static String ACTION = "ACTION";

	/** ログイン画面:ユーザーID */
	public final static String LOGIN_USER_NAME = "User_name";
	/** ログイン画面:パスワード */
	public final static String LOGIN_PASS_NAME = "Pass_name";

	/** 画面遷移時:URLパラメータ */
	public final static String PRM_MENU_KBN = "MenuKbn";

	/** 画面遷移時:URLパラメータ */
	public final static String PRM_TENPO = "SelTenpo";

	/** ヘッダー画面：DB更新状況フラグ */
	public final static String DBINFO_FLG = "db_update_flg";

	/** 画面出力メッセージ */
	public final static String COMMON_MSG = "formMsg";
	/** 画面出力メッセージタイプ */
	public final static String COMMON_MSGTYPE = "msg";

	/** 画面出力メッセージタイプ 連絡事項 */
	public final static String COMMON_MSGTYPE_SUP = "supmsg";
	/** 画面出力メッセージタイプ エラー */
	public final static String COMMON_MSGTYPE_ERR = "errmsg";

	/** メニュー画面:お知らせ内容 */
	public final static String MENU_INFO_PATH = "filepath";
	/** メニュー画面:お知らせ編集ボタン */
	public final static String MENU_INFO_BTN = "info_edit";
	/** メニュー画面:お知らせ編集(値：保存) */
	public final static String MENU_INFO_SAVE = "save";
	/** メニュー画面:お知らせ編集(値：編集) */
	public final static String MENU_INFO_EDIT = "edit";
	/** メニュー画面:お知らせ内容 */
	public final static String MENU_INFO_VALUE = "info_value";

	/** レポート分類:1分類パラメータ */
	public final static String REPORT_SIDE = "ReportSide";
	/** レポート分類:選択済みレポートNo */
	public final static String REPORT_NO = "ReportNo";
	/** レポート分類:表示レポート名タイプ(普通or短縮) */
	public final static String REPORT_NAMETYPE = "ReportNameType";
	/** レポート管理　表示順 */
	public final static String REPORT_DISP_SIDE = "Rreport_disp_no";

	/** レポート管理　表示行 */
	public final static String REPORT_DISP_ROW_SIDE = "Rreport_disp_row_no";

	/** レポート分類:1分類パラメータ */
	public final static String REPORT_SIDE_ARRAY = "ReportSide_array";

	/** レポート分類:選択済みレポートNo */
	public final static String REPORT_NO_ARRAY = "ReportNo_array";
	/** レポート分類:表示レポート名タイプ(普通or短縮) */

	/** メンテナンスレポート分類：パラメータ */
	public final static String REPORT_NAME = "Report";

	/** データベーステーブル名 */
	public final static String REPORT_PARA_USERS = "SYS_USERS";
	/** データベーステーブル名 */
	public final static String REPORT_PARA_GROUPS = "SYS_GROUPS";
	/** データベーステーブル名 */
	public final static String REPORT_PARA_SYS_POSMST = "SYS_POSMST";
	/** データベーステーブル名 */
	public final static String REPORT_PARA_REPORT_SIDE = "SYS_REPORT_SIDE";
	/** データベーステーブル名 */
	public final static String REPORT_PARA_REPORT_NAME = "SYS_REPORT_NAME";

	/** メニュー画面:メンテナンス分類 */
	public final static String MTN_SIDE = "MaintenanceSide";

	/** メンテナンス画面:リセットボタン */
	public final static String MTN_RSTBTN = "btn_reset";
	/** メンテナンス画面:登録ボタン */
	public final static String MTN_ENTBTN = "btn_entry";
	/** メンテナンス画面:削除ボタン */
	public final static String MTN_DELBTN = "btn_delete";
	/** メンテナンス画面:保存ボタン */
	public final static String MTN_SAVBTN = "btn_save";
	/** メンテナンス画面:選択ボタン */
	public final static String MTN_SELBTN = "btn_select";

	/** メンテナンス画面:入力エリア */
	public final static String MTN_INPAREA = "entry";
	/** メンテナンス画面:選択エリア */
	public final static String MTN_SELAREA = "select";

	/** メンテナンス画面:チェックボックス入力値 */
	public final static String MTN_CHKBOX_ON = "1";

	/** ユーザーメンテナンス画面：入力エリア */

	/** ユーザーメンテナンス画面：入力エリア 選択済データ番号 */
	public final static String MTN_USR_ETY_IDX = "selectedIndex";

	/** ユーザーメンテナンス画面：入力エリア ユーザ-ID */
	public final static String MTN_USR_ETY_CD = "txt_cd";

	/** ユーザーメンテナンス画面：入力エリア ユーザ-ID */
	public final static String MTN_USR_ETY_UID = "txt_id";

	/** パスワード変更画面：入力エリア パスワード */
	public final static String MTN_OLD_PASS = "ord_txt_pass";
	/** パスワード変更画面：入力エリア パスワード */
	public final static String MTN_NEW_PASS = "new_txt_pass";

	/** マスタファイルアップ:アップロードボタン */
	public final static String BTN_UP_LOAD = "up_load_btn";

	/** マスタファイルアップ:アップロードボタン */
	public final static String BTN_DOW_LOAD = "dow_load_btn";

	/** マスタファイルアップ:アップロードボタン */
	public final static String BTN_DOW_CLEA = "clea_load_btn";

	/** マスタファイルアップロード画面：選択 エリア */
	public final static String TALBE_CHECK = "check";

	/** マスタファイルアップ:参照ﾃｷｽﾄ */
	public final static String UP_LOAD_TXT = "fileName";

	/** マスタファイルアップ:参照ﾃｷｽﾄ Subject テーブル名 */
	public final static String MTN_UP_DOWN_BTN = "subject";

	public final static String UP_DOWN_MSG = "up_down_msg";

	/** ユーザーメンテナンス画面：入力エリア ユーザーパスワード */
	public final static String MTN_USR_ETY_PAS = "txt_pass";
	/**
	 * ユーザーメンテナンス画面[入力エリア],管理画面[選択エリア]： ロールコード　権限
	 */
	public final static String MTN_USR_ETY_POS = "cmb_pos";

	/**
	 * ユーザーメンテナンス画面[入力エリア] ：管理画面[選択エリア]:グループコード 所属
	 */
	public final static String MTN_USR_ETY_ATH = "cmb_atx";

	/**
	 * ユーザーメンテナンス画面[入力エリア] ：管理画面[選択エリア]:グループコード 所属
	 */
	public final static String MTN_USR_ETY_ATH_NAME = "cmd_atx_name";

	/**
	 * ユーザーメンテナンス画面[入力エリア] ：管理画面[選択エリア]:権限
	 */
	public final static String MTN_USR_ETY_POS_NAME = "txt_pos_name";

	/** ユーザーメンテナンス画面：入力エリア 姓 */
	public final static String MTN_USR_ETY_NM_FAMILY = "txt_nm_family";
	/** ユーザーメンテナンス画面：入力エリア 名 */
	public final static String MTN_USR_ETY_NM_NAME = "txt_nm_name";

	/** メニューメンテナンス画面：入力エリア レポート名 */
	public final static String MTN_NM_REPORT = "txt_nm_report";
	/** ユーザーメンテナンス画面：入力エリア 選択済データ番号 */
	public final static String MTN_MENU_ETY_IDX = "selectedIndex";

	/** メニューメンテナンス画面：入力エリア 所属部門 */
	public final static String MTN_MENU_ETY_ATH = "cmb_atx";
	/** メニューメンテナンス画面：入力エリア 権限 */
	public final static String MTN_MENU_ETY_POS = "cmb_pos";
	/** メニューメンテナンス画面：選択エリア レポート分類 */
	public final static String MTN_MENU_ETY_SIDE = "lst_side";
	/** メニューメンテナンス画面：入力エリア　カスタムプロパティ */
	public final static String MTN_CUSTOM_VALUE = "cmd_custom";

	/** 管理画面 :選択エリア　レポートコード　所属 */
	public final static String MTN_REPORT_CODE = "cmd_report";
	/** 管理画面 :選択エリア　レポートコード　所属 */
	public final static String MTN_REPORT_NM_SHORT = "txt_report_short";
	/** 管理画面 :選択エリア　レポートコード　所属 */
	public final static String MTN_REPORT_JSP = "txt_report_jsp";

	/** 管理画面 :選択エリア　グループコード　レポート分類コード */
	public final static String MTN_SIDE_CODE = "txt_report_side";
	/** 管理画面 :入力エリア　メニュー　有効無効 */
	public final static String MTN_ENABLE_MENU = "txt_enable_menu";

	/** お知らせ管理画面 :選択エリア　お知らせコード */
	public final static String MTN_INFO_CODE = "cmb_info";

	/** お知らせメンテナンス画面：入力エリア 表示開始日 */
	public final static String MTN_INFO_ETY_DT_START = "txt_dt_start";
	/** お知らせメンテナンス画面：入力エリア 表示終了日 */
	public final static String MTN_INFO_ETY_DT_END = "txt_dt_end";
	/** お知らせメンテナンス画面：入力エリア 常時表示設定 */
	public final static String MTN_INFO_ETY_FLG_ALWAYS = "txt_flg_always";
	/** お知らせメンテナンス画面：入力エリア 表示順 */
	public final static String MTN_INFO_ETY_NO_DISP = "txt_no_disp";
	/** お知らせメンテナンス画面：入力エリア タイトル */
	public final static String MTN_INFO_ETY_TITLE = "txt_title";
	/** お知らせメンテナンス画面：入力エリア お知らせ */
	public final static String MTN_INFO_ETY_INFO = "txt_info";

	/** お知らせメンテナンス画面：不要データ削除ボタン */
	public final static String MTN_BTN_CLEAN = "btn_clean";

	/** お知らせメンテナンス画面：プレビュー用 タイトル */
	public final static String MTN_INFO_VIEW_TITLE = "infoView_title";
	/** お知らせメンテナンス画面：プレビュー用 お知らせ */
	public final static String MTN_INFO_VIEW_INFO = "infoView_info";


	/* 仕訳レポート管理画面 form */
	public final static String CMB_DATE_FROM_ID = "CMB_DATE_FROM_ID";
	public final static String CMB_DATE_TO_ID = "CMB_DATE_TO_ID";
	public final static String TXT_DENPYO_ID = "TXT_DENPYO_ID";
	public final static String CMB_KAMOKU_ID = "CMB_KAMOKU_ID";
	public final static String TXT_BUMON_ID = "TXT_BUMON_ID";
	public final static String TXT_PROJECT_ID = "TXT_PROJECT_ID";
	public final static String CMB_AITE_ID = "CMB_AITE_ID";
	public final static String BTN_SERCH_ID = "BTN_SERCH_ID";
}
