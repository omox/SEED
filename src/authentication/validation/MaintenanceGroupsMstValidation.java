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

public class MaintenanceGroupsMstValidation extends Validation {
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
		if (super.chkRequired(entrys.get(Form.MTN_USR_ETY_ATH_NAME),
				MSG_ATH_NAME) == false) {
			errflg = false;
		}
		/** グループ名桁数チェック */
		if (super.chkNonreqL(entrys.get(Form.MTN_USR_ETY_ATH_NAME),
				LENGTH_ZEN_ATH, LENGTH_HALF_ATH, MSG_ATH_NAME) == false) {
			errflg = false;
		}

		/**
		 * 重複チェック
		 */
		if (errflg == true) {
			// 同一キー且つ別INDEXをエラーとする
			int size = list.size();
			for (int i = 0; i < size; i++) {
				MaintenanceGroupsMst group = (MaintenanceGroupsMst) list.get(i);
				if (group.getGroupName().equals(
						(entrys.get(Form.MTN_USR_ETY_ATH_NAME).toString()
								.trim()))) {
					int idx = Integer.parseInt(entrys.get(Form.MTN_USR_ETY_IDX)
							.toString());
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
		 * グループコード必須チェック System.out.println(entrys.get(Form.MTN_USR_ETY_ATH));
		 * if(super.chkRequiredLHAN(entrys.get(Form.MTN_USR_ETY_ATH),
		 * LENGTH_CODE,MSG_ATH_ID)== false){ errflg = false; }
		 */

		// /カスタムプロパティ桁数チェック
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
			MaintenanceGroupsMst group = (MaintenanceGroupsMst) list.get(i);
			if (group.getIndex() == Integer.parseInt(entrys.get(
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
			int idx = Integer.parseInt(entrys.get(Form.MTN_USR_ETY_IDX)
					.toString().trim());
			MaintenanceGroupsMst group = (MaintenanceGroupsMst) list.get(idx);
			if (group.getGroup() < 0) {
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
