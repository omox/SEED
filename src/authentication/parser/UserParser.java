/**
 * User クラスのパーサー
 */
package authentication.parser;

import authentication.bean.User;

/**
 * @author Omoto_Yuki
 *
 */
public class UserParser {


	/**
	 * コンストラクター
	 */
	public UserParser () {

	}

	/**
	 * @throws Exception
	 *
	 */
	public LoginUser UserParse( User userInfo ) throws Exception {
		// 戻り値の初期化
		LoginUser convertUser = new LoginUser();

		try {
			// 定義ありの場合
			if (userInfo != null) {
				// ユーザーIDの設定
				convertUser.setId(userInfo.getId());
				// パスワードの設定
				convertUser.setPass(userInfo.getPass());
				// 名前（性）の設定
				convertUser.setName(userInfo.getName());
				// 権限の設定
				convertUser.setAuth(userInfo.getCd_auth_());
				// パスワード有効期限(仮)
				convertUser.setPwdate(userInfo.getDt_pw_term_());
				// ロゴパスの設定
				convertUser.setLogo_(userInfo.getLogo_());
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		// 戻り値
		return convertUser;
	}
}
