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

public class MaintenanceReportNameValidation extends Validation {
	/**
	 * メニューメンテナンス画面：登録ボタンのチェック <br />
	 *
	 * @param entrys
	 *            入力ユーザー
	 * @return エラーなし：true、あり：false
	 */
	public boolean entry(HashMap entrys, ArrayList list) {

		boolean errflg = true;

		/** レポート名称桁数チェック 桁数必須 */
		if (super.chkRequiredLHAN(entrys.get(Form.MTN_NM_REPORT),
				LENGTH_ZEN_REPORT_NAME, LENGTH_HALF_REPORT_NAME, MSG_REPORT) == false) {
			errflg = false;
		}

		/** レポート略称チェック 桁数必須 */
		if (super.chkRequiredLHAN(entrys.get(Form.MTN_REPORT_NM_SHORT),
				LENGTH_ZEN_REPORT_SHORTNAME, LENGTH_HALF_REPORT_SHORTNAME,
				MSG_REPORT_SHORT) == false) {
			errflg = false;
		}

		// 行番号を取得している場合はコードがシステムメンテナンスコードではないかを判断
		if (entrys.get(Form.MTN_USR_ETY_IDX) != null
				&& Integer.parseInt((String) entrys.get(Form.MTN_USR_ETY_IDX)) >= 0) {

			if (Integer.parseInt(entrys.get(Form.MTN_REPORT_CODE).toString().trim()) > 0) {

				/** JSPチェック 桁数半角英数字 */
				if (super.chkRequiredLHAN(entrys.get(Form.MTN_REPORT_JSP).toString().trim(), LENGTH_JSP, MSG_JSP) == false) {
					errflg = false;
				}
			}
			// システムメンテナンスコードではない場合はJSPチェック
		} else {
			/** JSPチェック 桁数半角英数字 */
			if (super.chkRequiredLHAN(entrys.get(Form.MTN_REPORT_JSP)
					.toString().trim(), LENGTH_JSP, MSG_JSP) == false) {
				errflg = false;
			}
		}

		/** カスタムプロパティ桁数チェック 桁数 */
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
			MaintenanceReportName report_name = (MaintenanceReportName) list
					.get(i);
			if (report_name.getIndex() == Integer.parseInt(entrys.get(
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
			MaintenanceReportName report_name = (MaintenanceReportName) list
					.get(i);
			if (report_name.getReport_no() < 0) {
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
