/*
 * 作成日: 2006/08/29
 *
 */
package authentication.parser;

import authentication.defines.Defines;
import authentication.parser.User;

/**
 * ログインユーザーを表すクラス <br />
 * 各権限判定やログイン処理で使用される <br />
 * Userクラスを継承している
 */
public class LoginUser extends User {

	/** メンテナンス画面での入力項目表示判定:権限 */
	private boolean visible_auth = false;
	/** メンテナンス画面での入力項目表示判定:利用者区分 */
	private boolean visible_divi = false;

	/**
	 * コンストラクタ <br />
	 */
	public LoginUser() {}

	/**
	 * コンストラクタ <br />
	 */
	public LoginUser(String id, String pass) {
		super.setId(id);
		super.setPass(pass);
	}

	/**
	 * コンストラクタ <br />
	 *
	 * @param id user_id
	 * @param pass user_password
	 * @param name user_name
	 * @param auth user_auth
	 * @param divide user_divide
	 */
	public LoginUser(String id, String pass, String name, String auth,String dtpw) {
		super.setId(id);
		super.setPass(pass);
		super.setName(name);
		super.setAuth(auth);
		super.setPwdate(dtpw);
	}

	/**
	 * システム管理者判定 <br />
	 * ログインユーザーがシステム管理者かどうかを戻す。 <br />
	 *
	 * @return 管理者の場合：true 管理者以外の場合：false <br />
	 */
	public boolean hasAdmin() {
		if (Defines.STR_ADMIN_ID.equals(super.getId())) {
				return true;
		}
		return false;
	}

	/**
	 * システム管理者設定 <br />
	 * システム管理者としての各情報(ID,ﾊﾟｽﾜｰﾄﾞ,名前,権限,利用者区分)を与える <br />
	 */
	public void setAdmin() {
		super.setId(Defines.STR_ADMIN_ID); //ユーザID
		super.setPass(Defines.STR_ADMIN_PASS); //パスワード
		super.setName(Defines.STR_ADMIN_NAME); //ユーザー名
		super.setAuth(Defines.STR_ADMIN_AUTH); //権限
	}

	/**
	 * ユーザー一致判定 <br />
	 *
	 * @param usr ユーザー
	 * @return 一致した場合はtrue,それ以外はfalseを戻す。
	 */
	public boolean equal(LoginUser usr) {
		/* 比較するユーザーを空白チェック */
		if (usr.hasBlank() == true) return false;

		if (usr.getId().equals(super.getId())) {
			if (usr.getPass().equals(super.getPass())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return visible_auth を戻す。 <br />
	 *
	 */
	public boolean getVisible_auth() {
		return visible_auth;
	}

	/**
	 * @param visible_auth visible_auth を設定。 <br />
	 *
	 */
	public void setVisible_auth(boolean visible_auth) {
		this.visible_auth = visible_auth;
	}

	/**
	 * @return visible_divi を戻す。 <br />
	 *
	 */
	public boolean getVisible_divi() {
		return visible_divi;
	}

	/**
	 * @param visible_divi visible_divi を設定。 <br />
	 *
	 */
	public void setVisible_divi(boolean visible_divi) {
		this.visible_divi = visible_divi;
	}

	/**
	 * ID,Pass空白チェック
	 *
	 * @return id,passの何れかにnull,又は空白以外が入っている場合はtrue,それ以外はfalseを戻す。
	 */
	private boolean hasBlank() {

		String id = super.getId();
		if (id == null) return true;
		if (id.equals("")) return true;
		return false;
	}

	public boolean hasSystemAdmin(){
		if (this.hasAdmin()) return true;
		if (this.getAuth().equals(Defines.STR_ADMIN_AUTH)) return true;
		return false;
	}
}
