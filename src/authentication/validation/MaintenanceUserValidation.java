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
 * 作成日: 2007/11/09
 */
package authentication.validation;

import java.util.*;

import authentication.bean.*;
import authentication.defines.*;

/**
 * ユーザーマスターメンテナンス画面でのチェック処理
 */
public class MaintenanceUserValidation extends Validation {

	/**
	 * ユーザーマスター画面：登録ボタンのチェック
	 *
	 * @param entrys 入力ユーザー
	 * @return エラーなし：true、あり：false
	 */
	public boolean entry(HashMap entrys, ArrayList list) {

		boolean errflg = true;

		/**
		 * 入力チェック
		 */
		/* ID:必須,桁数,半角英数字 */
		if (super.chkRequiredLHAN(entrys.get(Form.MTN_USR_ETY_UID), LENGTH_USER_ID, MSG_USER_ID) == false) {
			errflg = false;
		}

		/**
		 * 入力以外の特殊エラーチェック
		 */
		if (errflg == true) {
			/* 同一キー且つ別INDEXをエラーとする */
			int size = list.size();
			for (int i = 0; i < size; i++) {
				MaintenanceUser usr = (MaintenanceUser) list.get(i);
				if (usr.getId().equals(entrys.get(Form.MTN_USR_ETY_UID))) {
					int idx = Integer.parseInt((String) entrys
							.get(Form.MTN_USR_ETY_IDX));
					if (usr.getIndex() != idx) {
						String msg = MSG_ALR_EQUALKEY;
						msgs.add(msg);
						errflg = false;
						break;
					} else {
						break;
					}
				}
			}
		}

		/* password:必須,桁数,半角英数字 */
		if (super.chkRequiredLHAN(entrys.get(Form.MTN_USR_ETY_PAS),
				LENGTH_PASSWORD, MSG_PASSWORD) == false) {
			errflg = false;
		}

		/* 姓:桁数・必須 */
// ATLAS用に必須確認OFF
//		if (super.chkRequired(entrys.get(Form.MTN_USR_ETY_NM_FAMILY), MSG_USER_NAME_FAMILY) == false) {
//			errflg = false;
//		}
		if (super.chkNonreqL(entrys.get(Form.MTN_USR_ETY_NM_FAMILY), LENGTH_ZEN_NAME_FAMILY, LENGTH_HALF_NAME_FAMILY, MSG_USER_NAME_FAMILY) == false) {
			errflg = false;
		}

		/* 名:桁数・必須 */
// ATLAS用に必須確認OFF
//		if (super.chkRequired(entrys.get(Form.MTN_USR_ETY_NM_NAME), MSG_USER_NM_NAME) == false) {
//			errflg = false;
//		}
		if (super.chkNonreqL(entrys.get(Form.MTN_USR_ETY_NM_NAME),	LENGTH_ZEN_NM_NAME, LENGTH_HALF_NAME_FAMILY, MSG_USER_NM_NAME) == false) {
			errflg = false;
		}

		/* ロール:必須 */
		String[] kk = (String[]) entrys.get(Form.MTN_USR_ETY_POS);
		if (super.chkRequired_notinput(kk, MSG_POS_NAME) == false) {
			errflg = false;
		}
		/* グループ:必須 */
		kk = (String[]) entrys.get(Form.MTN_USR_ETY_ATH);
		if (super.chkRequired_notinput(kk, MSG_ATH_NAME) == false) {
			errflg = false;
		}
		return errflg;

	}

	/**
	 * ユーザーマスター画面：削除ボタンのチェック <br />
	 *
	 * @param usr
	 *            入力ユーザー
	 * @param usrs
	 *            リスト
	 * @return エラーなし：true、あり：false
	 */
	public boolean delete(HashMap entrys, ArrayList list) {
		boolean errflg = true;

		/**
		 * 入力以外の特殊エラーチェック
		 */
		int size = list.size();
		int i = 0;
		for (i = 0; i < size; i++) {
			MaintenanceUser usr = (MaintenanceUser) list.get(i);
			if (usr.getIndex() == Integer.parseInt(entrys.get(
					Form.MTN_USR_ETY_IDX).toString().trim())) {
				break;
			}
		}
		// }
		if (i >= size) {
			String msg = MSG_NOT_DELETE;
			msgs.add(msg);
			errflg = false;
		} else {
			MaintenanceUser usr = (MaintenanceUser) list.get(i);
			if (usr.getCD_user() < 0) {
				String msg = MSG_SYSTEM_MENTENANCE;
				msgs.add(msg);
				errflg = false;
			}

		}
		return errflg;
	}

	/**
	 * ユーザーマスター画面：保存ボタンのチェック <br />
	 *
	 * @param usrs
	 *            リスト
	 * @return エラーなし：true、あり：false
	 */
	public boolean save(ArrayList list) {
		boolean errflg = true;
		/**
		 * 入力以外の特殊エラーチェック
		 */
		/* データがない場合エラーとする */

		int size = list.size();
		if (size <= 1) {
			String msg = MSG_NON_SAVELIST;
			msgs.add(msg);
			errflg = false;
		}
		return errflg;
	}
}
