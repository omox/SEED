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

public class MaintenanceReportSideValidation extends Validation {
	/**
	 * メニューメンテナンス画面：登録ボタンのチェック <br />
	 *
	 * @param entrys
	 *            入力ユーザー
	 * @return エラーなし：true、あり：false
	 */
	public boolean entry(HashMap entrys, ArrayList list) {
		boolean errflg = true;

		/** レポート分類名称必須チェック */
		if (super.chkRequired(entrys.get(Form.MTN_NM_REPORT), MSG_REPORT_SIDE) == false) {
			errflg = false;
		}

		/** レポート分類名称桁数チェック */
		if (super.chkNonreqL(entrys.get(Form.MTN_NM_REPORT),
				LENGTH_ZEN_SIDE_NAME, LENGTH_HALF_SIDE_NAME, MSG_REPORT_SIDE) == false) {
			errflg = false;
		}

		/** レポート分類表示順必須・半角数字チェック */
		if (super.chkRequiredLHANFigure(entrys.get(Form.REPORT_DISP_SIDE),
				LENGTH_DISP_NO, MSG_DISP_NO) == false) {
			errflg = false;
		}

		/** レポート列・半角数字チェック */
		if (super.chkRequiredLHANFigure(entrys.get(Form.REPORT_DISP_ROW_SIDE),
				LENGTH_DISP_NO, MSG_DISP_ROW_NO) == false) {
			errflg = false;
		}

		/** カスタムプロパティ桁数チェック */
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
			MaintenanceReportSide side = (MaintenanceReportSide) list.get(i);
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
		} else {
			MaintenanceReportSide side = (MaintenanceReportSide) list.get(i);
			if (side.getSide() < 0) {
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
