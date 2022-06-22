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

import java.util.*;

import authentication.bean.*;
import authentication.defines.*;

public class MaintenancePosmstValidation extends Validation {
	/**
	 * メニューメンテナンス画面：登録ボタンのチェック <br />
	 *
	 * @param entrys
	 *            入力ユーザー
	 * @return エラーなし：true、あり：false
	 */
	public boolean entry(HashMap entrys, ArrayList list) {
		boolean errflg = true;

		/** グループ名必須チェック */
		if (super.chkRequired(entrys.get(Form.MTN_USR_ETY_POS_NAME),
				MSG_POS_NAME) == false) {
			errflg = false;
		}
		/** グループ名必須チェック */
		if (super.chkNonreqL(entrys.get(Form.MTN_USR_ETY_POS_NAME),
				LENGTH_ZEN_POS, LENGTH_HALF_POS, MSG_POS_NAME) == false) {
			errflg = false;
		}

		/**
		 * 重複チェック
		 */

		if (errflg == true) {
			/* 同一キー且つ別INDEXをエラーとする */
			int size = list.size();
			for (int i = 0; i < size; i++) {
				MaintenancePosmst group = (MaintenancePosmst) list.get(i);
				if (group.getPositionName()
						.equals(
								entrys.get(Form.MTN_USR_ETY_POS_NAME)
										.toString().trim())) {
					int idx = Integer.parseInt((String) entrys
							.get(Form.MTN_USR_ETY_IDX));
					if (group.getIndex() != idx) {
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

		/**
		 * グループコード必須チェック
		 * if(super.chkRequiredLHAN(entrys.get(Form.MTN_USR_ETY_POS
		 * ),LENGTH_POS_CODE, MSG_POS_CODE)== false){ errflg = false; }
		 */

		/** プロパティ・半角数字チェック */
		if (super.chkNonreqL(entrys.get(Form.MTN_CUSTOM_VALUE),
				LENGTH_ZEN_CUSTOM, LENGTH_HALF_CUSTOM, MSG_CUSTOM_VALUE) == false) {
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
		/* 同一キーがリスト内に存在しない場合をエラーとする */
		int size = list.size();
		int i = 0;
		;

		for (i = 0; i < size; i++) {
			MaintenancePosmst pos = (MaintenancePosmst) list.get(i);
			if (pos.getIndex() == Integer.parseInt(entrys.get(
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
			int idx = Integer.parseInt((String) entrys
					.get(Form.MTN_USR_ETY_IDX));
			MaintenancePosmst posmst = (MaintenancePosmst) list.get(idx);
			if (posmst.getPosition() < 0) {
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
		if (size <= 0) {
			String msg = MSG_NON_SAVELIST;
			msgs.add(msg);
			errflg = false;
		}
		return errflg;
	}

}
