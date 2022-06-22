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
package authentication.validation;

import authentication.bean.*;

public class PasswordChangeValidation extends Validation {
	/**
	 * メニューメンテナンス画面：登録ボタンのチェック <br />
	 *
	 * @param entrys
	 *            入力ユーザー
	 * @return エラーなし：true、あり：false
	 */
	public boolean entry(String old_pass, String new_pass, User loginur,
			String er_flg) {
		boolean errflg = true;
		/**
		 * 入力チェック
		 */
		/* ID:必須,桁数,半角英数字 */
		/* PASS:必須,桁数,半角英数字 */
		if (super.chkRequiredLHAN(old_pass, LENGTH_PASSWORD, OLD_PASSWORD) == false) {
			errflg = false;
		}
		else if (er_flg.equals("false")) {
			String msg = MSG_PASS_ERO;
			msgs.add(msg);
			errflg = false;
		}

		/* PASS:必須,桁数,半角英数字 */
		if (super.chkRequiredLHAN(new_pass, LENGTH_PASSWORD, NEW_PASSWORD) == false) {
			errflg = false;
		}

		return errflg;
	}

}
