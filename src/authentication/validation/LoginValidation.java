/*******************************************************************************
 * Copyright (c) 2001, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * 作成日: 2007/10/26
 */
package authentication.validation;

import java.text.SimpleDateFormat;
import java.util.Date;

import authentication.defines.Consts;
import authentication.defines.Defines;
import authentication.parser.LoginUser;
import common.ChkUsableTime;
import common.CmnDate;

/**
 * ログイン画面でのチェック処理 <br />
 */
public class LoginValidation extends Validation {
	/**
	 * ログイン画面：ログインボタンのチェック
	 * @param usr    入力ユーザー
	 * @param usrs   リスト
	 * @return エラーなし：true、あり：false
	 */
	public boolean entry(String id, String pass) {

		boolean errflg = true;
		/**
		 * 入力チェック
		 */

		/* ID:必須,桁数,半角英数字 */
		if (super.chkRequiredLHAN(id, LENGTH_USER_ID, MSG_USER_ID) == false) {
			errflg = false;
		}

		/* PASS:必須,桁数,半角英数字 */
		if (super.chkRequiredLHAN(pass, LENGTH_PASSWORD, MSG_PASSWORD) == false) {
			errflg = false;
		}

		return errflg;
	}

	/**
	 * システム利用時間の判定
	 * @param fromData
	 * @param toData
	 * @param userid
	 * @return
	 */
	public String closeTime(String fromData, String toData, String userid) {
		String jsppage = Consts.LOGIN_FORM;
		try {
			if (super.chkTime(fromData) != false || super.chkTime(toData) != false) {
				jsppage = Consts.PAGE_ERROR;
			}

			ChkUsableTime sys = new ChkUsableTime(fromData, toData);
			if (sys.isCloseTime(userid)) {
				jsppage = Consts.CLOSE_TIME;
			}

			if (sys.isWaitTime(userid)) {
				jsppage = Consts.WAIT_TIME;
			}

		} catch (Exception e) {
			e.printStackTrace();
			jsppage = Consts.PAGE_ERROR;
		}
		return jsppage;
	}

	/**
	 * パスワードの有効期限確認
	 * @param usr
	 * @return
	 */
	public boolean IsPasswordTrim(LoginUser usr) {
		boolean errflg = true;

		// 管理者確認
		if (usr.hasAdmin() == false) {

			// 日付確認
			if (this.isDate(usr.getPwdate(), Defines.DT_PW_FORMAT_SAVE)) {

				Date now = new Date();
				String snow = new SimpleDateFormat(Defines.DT_PW_FORMAT_SAVE).format(now);

				// 有効期限確認
				if (usr.getPwdate(Defines.DT_PW_FORMAT_SAVE).toString().compareTo(snow) < 0) {
					errflg = false;
				}
			}
		}
		return errflg;
	}

	/**
	 * パスワードの有効期限確認
	 * @param usr
	 * @return
	 */
	public boolean IsPasswordTrimBefore(LoginUser usr) {
		boolean errflg = true;

		// 管理者確認
		if (usr.hasAdmin() == false) {

			// 日付確認
			if (this.isDate(usr.getPwdate(), Defines.DT_PW_FORMAT_SAVE)) {

				String snow = new SimpleDateFormat(Defines.DT_PW_FORMAT_SAVE).format(CmnDate.getDayAddedDate(new Date(),14));

				// 有効期限確認
				if (usr.getPwdate(Defines.DT_PW_FORMAT_SAVE).toString().compareTo(snow) <= 0) {
					errflg = false;
				}
			}
		}
		return errflg;
	}
}