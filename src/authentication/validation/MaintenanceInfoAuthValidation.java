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

import java.util.ArrayList;
import java.util.HashMap;

import authentication.bean.MaintenanceInfoAuth;
import authentication.defines.Form;

public class MaintenanceInfoAuthValidation extends Validation {
	/**
	 * お知らせ管理画面：登録ボタンのチェック <br />
	 *
	 * @param entrys
	 * @param list
	 *
	 * @return エラーなし：true、あり：false
	 */
	public boolean entry(HashMap entrys, ArrayList list) {
		boolean errflg = true;

		/** グループコード必須チェック */
		if (super.chkRequired(entrys.get(Form.MTN_USR_ETY_ATH), MSG_ATH_NAME) == false) {
			errflg = false;
		}
		/** ロールコード必須チェック */
		if (super.chkRequired(entrys.get(Form.MTN_USR_ETY_POS), MSG_POS_NAME) == false) {
			errflg = false;
		}
		/** お知らせコード必須チェック */
		if (super.chkRequired(entrys.get(Form.MTN_INFO_CODE), MSG_INFO) == false) {
			errflg = false;
		}

		if (errflg == true) {
			/* 同一キー且つ別INDEXをエラーとする */
			int size = list.size();
			for (int i = 0; i < size; i++) {
				MaintenanceInfoAuth listdata = (MaintenanceInfoAuth) list.get(i);
				if (listdata.getCd_group() == Integer.parseInt((String) entrys.get(Form.MTN_USR_ETY_ATH))
						&& listdata.getCd_pos() == Integer.parseInt((String) entrys.get(Form.MTN_USR_ETY_POS))
						&& listdata.getCd_info() == Integer.parseInt((String) entrys.get(Form.MTN_INFO_CODE))
					) {
					int idx = Integer.parseInt((String) entrys.get(Form.MTN_USR_ETY_IDX));
					if (listdata.getIndex() != idx) {
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

		return errflg;
	}

	/**
	 * お知らせ管理画面：削除ボタンのチェック <br />
	 *
	 * @param entrys
	 * @param list
	 *
	 * @return エラーなし：true、あり：false
	 */
	public boolean delete(HashMap entrys, ArrayList list) {
		boolean errflg = true;

		/**
		 * 入力以外の特殊エラーチェック
		 */
		/* 同一キーがリスト内に存在しない場合をエラーとする */

		int size = list.size();
		int i = 0;

		for (i = 0; i < size; i++) {
			MaintenanceInfoAuth listdata = (MaintenanceInfoAuth) list.get(i);
			if (listdata.getIndex() == Integer.parseInt(entrys.get(
					Form.MTN_USR_ETY_IDX).toString().trim())) {
				break;
			}
		}

		// }
		if (i >= size) {
			String msg = MSG_NOT_DELETE;
			msgs.add(msg);
			errflg = false;
		}
		return errflg;
	}

	/**
	 * お知らせ管理画面：保存ボタンのチェック <br />
	 *
	 * @param list
	 *
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
