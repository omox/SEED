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

import java.util.ArrayList;
import java.util.HashMap;

import authentication.bean.MaintenanceInfo;
import authentication.defines.Consts;
import authentication.defines.Defines;
import authentication.defines.Form;

public class MaintenanceInfoValidation extends Validation {

	/**
	 * お知らせマスタ：登録ボタンのチェック
	 *
	 * @param entrys
	 * @param list
	 *
	 * @return エラーなし：true、あり：false
	 */
	public boolean entry(HashMap entrys, ArrayList list) {

		boolean errflg = true;

		/**
		 * 常時表示設定、表示開始日、表示終了日チェック
		 */
		String dt_start = entrys.get(Form.MTN_INFO_ETY_DT_START).toString();
		String dt_end = entrys.get(Form.MTN_INFO_ETY_DT_END).toString();

		if (Consts.INFO_ALWAYS.equals(entrys.get(Form.MTN_INFO_ETY_FLG_ALWAYS).toString())){
			/* 常時表示をチェックした場合、表示開始日、表示終了日は設定できない */
			if (dt_start.length() > 0 || dt_end.length() > 0){
				String msg = MSG_FLG_ALWAYS + "をチェックした場合、" + MSG_DT_START + "、" + MSG_DT_END + MSG_CANNOT_INPUT;
				msgs.add(msg);
				errflg = false;
			}

		} else {
			boolean errflg_dt = true;

			/* 表示開始日:必須,yyyy/mm/ddの日付形式 */
			if (super.chkRequired(dt_start, MSG_DT_START) == false) {
				errflg = false;
				errflg_dt = false;

			} else if (super.chkNonreqDtFormat(dt_start, Defines.DT_PW_FORMAT_DISP, MSG_DT_START) == false) {
				errflg = false;
				errflg_dt = false;
			}

			/* 表示終了日:必須,yyyy/mm/ddの日付形式 */
			if (super.chkRequired(dt_end, MSG_DT_END) == false) {
				errflg = false;
				errflg_dt = false;

			} else if (super.chkNonreqDtFormat(dt_end, Defines.DT_PW_FORMAT_DISP, MSG_DT_END) == false) {
				errflg = false;
				errflg_dt = false;
			}

			if (errflg_dt = true) {
				if (super.isDateBefore(dt_end, dt_start, Defines.DT_PW_FORMAT_DISP) == true) {
					String msg = MSG_DT_END + "は、" + MSG_DT_START + "以降で入力してください";
					msgs.add(msg);
					errflg = false;
				}
			}
		}

		/* 表示順:必須,桁数,半角英数字 */
		if (super.chkRequiredLHANFigure(entrys.get(Form.MTN_INFO_ETY_NO_DISP), LENGTH_NO_DISP, MSG_NO_DISP) == false) {
			errflg = false;
		}

		/* タイトル:必須,桁数 */
		if (super.chkRequiredLHAN(entrys.get(Form.MTN_INFO_ETY_TITLE),
				LENGTH_ZEN_TITLE, LENGTH_HALF_TITLE, MSG_TITLE) == false) {
			errflg = false;
		}

		/* お知らせ:必須,桁数 */
		if (super.chkRequiredLHAN(entrys.get(Form.MTN_INFO_ETY_INFO),
				LENGTH_ZEN_INFO, LENGTH_HALF_INFO, MSG_INFO) == false) {
			errflg = false;
		}

		return errflg;

	}

	/**
	 * お知らせマスタ画面：削除ボタンのチェック <br />
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
			MaintenanceInfo listdata = (MaintenanceInfo) list.get(i);
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
	 * お知らせマスタ画面：保存ボタンのチェック <br />
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
