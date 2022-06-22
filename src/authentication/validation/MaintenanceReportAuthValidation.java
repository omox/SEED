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

public class MaintenanceReportAuthValidation extends Validation {
	/**
	 * メニューメンテナンス画面：登録ボタンのチェック <br />
	 *
	 * @param entrys
	 *            入力ユーザー
	 * @return エラーなし：true、あり：false
	 */
	public boolean entry(HashMap entrys, ArrayList list) {
		boolean errflg = true;
		/** レポート分類コード必須チェック */
		if (super.chkRequired(entrys.get(Form.REPORT_SIDE), MSG_SIDE_ID) == false) {
			errflg = false;
		}
		/** レポートコード必須チェック */
		if (super.chkRequired(entrys.get(Form.MTN_REPORT_CODE), MSG_REPORT) == false) {
			errflg = false;
		}
		/** レポート分類表示順必須・半角数字チェック */
		if (super.chkRequiredLHANFigure(entrys.get(Form.REPORT_DISP_SIDE),
				LENGTH_DISP_NO, MSG_DISP_NO) == false) {
			errflg = false;
		}
		/**
		 * 重複チェック
		 */

		if (errflg == true) {
			/* 同一キー且つ別INDEXをエラーとする */
			int size = list.size();
			for (int i = 0; i < size; i++) {
				MaintenanceReportAuth side = (MaintenanceReportAuth) list
						.get(i);
				if (side.getReport_no() == Integer.parseInt(entrys.get(
						Form.MTN_REPORT_CODE).toString().trim())
						&& side.getReport_side() == Integer.parseInt(entrys
								.get(Form.REPORT_SIDE).toString().trim())) {

					int idx = Integer.parseInt((String) entrys
							.get(Form.MTN_USR_ETY_IDX));
					if (side.getIndex() != idx) {
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

			MaintenanceReportAuth side = (MaintenanceReportAuth) list.get(i);
			if (side.getIndex() == Integer.parseInt(entrys.get(
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

	public boolean roolchek(boolean check) {
		System.out.println("roolchek");
		boolean checkflg = true;

		if (check == false) {
			String msg = MSG_NON_CHECK;
			msgs.add(msg);
			checkflg = false;
		}

		return checkflg;
	}
}
