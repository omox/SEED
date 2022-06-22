/*
 * 作成日: 2007/10/26
 *
 */
package authentication.defines;

/**
 * メッセージクラス
 * 各画面の上部、又はエラー画面にて出力される文字を定義
 */
public class Message {

	/** ログイン画面:ID、又はパスワードの何れかが一致しない場合に表示されるエラーメッセージ */
	public final static String ERR_AUTH_FAIL = "&nbsp;&nbsp;・ユーザーコード または パスワード が違います";

	/** 共通:セッションがタイムアウトになった際に表示されるエラーメッセージ */
	public final static String ERR_SES_TIMEOUT = "セッションがタイムアウトしました。<br>ウインドウを閉じて、ダッシュボードから再度ログインして下さい。";

	/** 共通:画面を閲覧する権限がない際にひょうじされるエラーメッセージ */
	public final static String ERR_AUTH_INVALID = "閲覧権限がない為、表示できませんでした";

	/** 共通:保存処理を実行し、保存が成功した際に表示される連絡メッセージ */
	public final static String SUP_SAVE_END = "が正常に完了しました";

	/** 共通：保存処理を実行し、保存に失敗した際に表示される連絡メッセージ */
	public final static String ERR_SAVE_END = "に失敗しました";

	/** アップロード画面：1行目の項目が正しくない際に表示されるﾒｯｾｰｼﾞ */
	public final static String UP_EER = "指定されたアップロードファイルの項目名に誤りがあります";

	/** アップロード画面：？行目が失敗した際に表示されるエラーメッセージ */
	public final static String ERR_SAVE_NO = "行目：";

	/** アップロード画面：ファイルに情報が存在しない場合 */
	public final static String UP_FILE_ERR = "アップロードする情報がありません。ファイルをお確かめ下さい";

	public final static String ERR_DOWN_FILE = "ファイルダウンロードが失敗しました";

	public final static String ERR_UP_FILE = "アップロードが失敗しました";

	/** アップロード画面：ファイルに情報が存在しない場合 */
	public final static String FILE_MSG = "　入力ファイル名：";

	/** 共通:保存処理を実行し、保存が成功した際に表示される連絡メッセージ */
	public final static String SUP_SAVE_END_UP = "のアップロードが正常に終了しました";
	public final static String ERR_SAVE_END_UP = "のアップロードに失敗しました";

	/** アップロード画面：ファイルの存在チェック */
	public final static String ERR_FILE_Existence = "指定したアップロードファイル名又はディレクトリが存在しません";

	/** アップロード画面：ファイルの存在チェック */
	public final static String ERR_MASTER_TB = "マスタ管理テーブルにデータが存在しません";

	/** アップロード画面：ファイルの存在チェック */
	public final static String ERR_MASTER_ER_FLG = "マスタ管理テーブルのエラーフラグには空白を0以上を入力ください";
}
