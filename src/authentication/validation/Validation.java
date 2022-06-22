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
 * 作成日: 2006/10/26
 */
package authentication.validation;

import java.io.File;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import authentication.defines.Message;

/**
 * エラー制御を行う必要があるイベントのエラーチェック・判定を行い、
 * エラーが存在する際は、各イベントに適切なエラーメッセージを設定する
 */
public class Validation {

	/** エラーメッセージを格納するArrayList */
	protected ArrayList msgs = new ArrayList();

	/** 出力エラーメッセージ:指定したデータがデータリストの中に存在したが、キー項目が重複するデータが既に存在している場合に表示するエラーメッセージ */
	protected static final String MSG_ALR_EQUALKEY = "既に同じデータが登録されています";
	/** 出力エラーメッセージ:指定したデータがデータリストの中に存在しない場合に表示するエラーメッセージ */
	protected static final String MSG_NOT_DELETE = "削除対象が選択されていません";
	/** 出力エラーメッセージ:指定したデータがデータリストの中に存在しない場合に表示するエラーメッセージ */
	protected static final String MSG_NON_EQUALKEY = "対象データが見つかりません";
	/** 出力エラーメッセージ:保持しているデータリストにデータが1件も存在しない場合に表示するエラーメッセージ */
	protected static final String MSG_NON_SAVELIST = "保存するデータが存在しません";
	/** 出力エラーメッセージ:権限が存在しない場合 */
	protected static final String MSG_NON_CHECK = "ユーザーレポート管理に権限が存在しません";
	/** 出力エラーメッセージ：削除しようとしたデータコードが99場合 */
	protected static final String MSG_SYSTEM_MENTENANCE = "システム管理情報のため削除できません";
	/** 出力エラーメッセージ：パスワード変更時に旧パスワードが違う場合 */
	protected static final String MSG_PASS_ERO = "旧パスワードが違います";

	/** 出力エラーメッセージ:ログイン時にパスワード有効期限が切れている場合に表示するエラーメッセージ */
	// private static final String MSG_PWDATE_FALSE =
	// "パスワードの有効期限が切れています。システム管理者に連絡して下さい。";

	/** エラーメッセージ生成用文字:必須入力項目チェック */
	private static final String MSG_REQ = "必須入力項目です。";
	/** エラーメッセージ生成用文字:必須項目チェック */
	private static final String MSG_REQ_LEN = "必須項目です。";
	//
	private static final String MSG_REQ_LEN_FILE = "必ず指定して下さい";
	/** エラーメッセージ生成用文字:桁数チェック */
	private static final String MSG_LEN = "桁まで";
	/** エラーメッセージ生成用文字:半角英数字チェック */
	private static final String MSG_HALFNUMENG = "半角英数字";
	/** エラーメッセージ生成用文字:半角数字チェック */
	private static final String MSG_HALFNUM = "半角数字";
	/** エラーメッセージ生成用文字:日付チェック */
	private static final String MSG_DATE = "日付形式";
	/** エラーメッセージ生成用文字:エラーメッセージ */
	private static final String MSG_END = "で入力してください";

	/** エラーメッセージ生成用文字：ユーザーID */
	protected static final String MSG_USER_ID = "ユーザーID";
	protected static final int LENGTH_USER_ID = 13;
	/** エラーメッセージ生成用文字：ユーザー名 */
	protected static final String MSG_USER_NAME = "ユーザー名";
	/** エラーメッセージ生成用文字：パスワード */
	protected static final String MSG_PASSWORD = "パスワード";
	protected static final int LENGTH_PASSWORD = 20;

	protected static final String OLD_PASSWORD = "旧パスワード";
	protected static final String NEW_PASSWORD = "新パスワード";

	/** エラーメッセージ生成用文字：グループ */
	protected static final String MSG_ATH_ID = "グループ";
	protected static final int LENGTH_CODE = 2;
	/** エラーメッセージ生成用文字:グループ名 */
	protected static final String MSG_ATH_NAME = "グループ名";
	protected static final int LENGTH_ZEN_ATH = 30;
	protected static final int LENGTH_HALF_ATH = 60;

	/** エラーメッセージ生成用文字：ロール */
	protected static final String MSG_POS_ID = "ロール";
	protected static final String MSG_POS_CODE = "ロールコード";
	protected static final int LENGTH_POS_CODE = 2;

	/** エラーメッセージ生成用文字：ロール名 */
	protected static final String MSG_POS_NAME = "ロール名";
	protected static final int LENGTH_ZEN_POS = 30;
	protected static final int LENGTH_HALF_POS = 60;

	/** エラーメッセージ生成用文字：分類 */
	protected static final String MSG_SIDE_ID = "分類名称";
	protected static final String MSG_SIDE_CODE = "分類コード";
	protected static final int LENGTH_SIDE_CODE = 2;

	/** エラーメッセージ生成用文字：レポート名 */
	protected static final String MSG_REPORT = "レポート名称";
	protected static final String MSG_REPORT_NAME = "レポート名";
	protected static final int LENGTH_HALF_REPORT_NAME = 60;
	protected static final int LENGTH_ZEN_REPORT_NAME = 30;

	/** エラーメッセージ生成用文字：レポート略称 */
	protected static final String MSG_REPORT_SHORT = "レポート略称";
	protected static final int LENGTH_HALF_REPORT_SHORTNAME = 30;
	protected static final int LENGTH_ZEN_REPORT_SHORTNAME = 15;

	/** エラーメッセージ生成用文字：　レポート分類名 */
	protected static final String MSG_REPORT_SIDE = "分類名称";
	protected static final int LENGTH_HALF_SIDE_NAME = 60;
	protected static final int LENGTH_ZEN_SIDE_NAME = 30;

	/** エラーメッセージ生成用文字：レポート分類表示順 */
	protected static final String MSG_DISP_ID = "レポート分類表示順";
	protected static final String MSG_DISP_ROW_NO = "列";
	protected static final String MSG_DISP_NO = "行";
	protected static final int LENGTH_DISP_NO = 5;

	/** エラーメッセージ生成用文字：レポート分類表示順 */
	protected static final String MSG_REPORT_CODE = "レポートコード";
	protected static final int LENGTH_REPORT_CODE = 2;

	/** エラーメッセージ生成用文字：JSP */
	protected static final String MSG_JSP = "レポートJSP";
	protected static final int LENGTH_JSP = 60;

	/** エラーメッセージ生成用文字:姓 */
	protected static final String MSG_USER_NAME_FAMILY = "姓";
	protected static final int LENGTH_HALF_NAME_FAMILY = 60;
	protected static final int LENGTH_ZEN_NAME_FAMILY = 30;
	/** エラーメッセージ生成用文字:名 */
	protected static final String MSG_USER_NM_NAME = "名";
	protected static final int LENGTH_HALF_NM_NAME = 60;
	protected static final int LENGTH_ZEN_NM_NAME = 30;
	/** エラーメッセージ生成用文字：カスタムプロパティ */
	protected static final String MSG_CUSTOM_VALUE = "プロパティ";
	protected static final int LENGTH_HALF_CUSTOM = 300;
	protected static final int LENGTH_ZEN_CUSTOM = 150;

	/** エラーメッセージ生成用文字：アップロード */
	protected static final String MSG_UP_LOAD = "アップロードファイル";

	/** エラーメッセージ生成用文字：アップロード */
	protected static final String MSG_REQ_CSV = "アップロードファイルにはCSVファイルを指定してください";

	/** エラーメッセージ生成用文字ﾚﾎﾟｰﾄマスタのフィールドのみ異なる：カスタムプロパティ */
	protected static final int LENGTH_HALF_CUSTOM_REPORT_NAME = 1000;
	protected static final int LENGTH_ZEN_CUSTOM_REPORT_NAME = 500;

	/** エラーメッセージ生成用文字：表示開始日 */
	protected static final String MSG_DT_START = "表示開始日";

	/** エラーメッセージ生成用文字：表示終了日 */
	protected static final String MSG_DT_END = "表示終了日";

	/** エラーメッセージ生成用文字：入力不可エラーメッセージ */
	protected static final String MSG_CANNOT_INPUT = "は入力できません";

	/** エラーメッセージ生成用文字：表示終了日 */
	protected static final String MSG_FLG_ALWAYS = "常時表示設定";

	/** エラーメッセージ生成用文字：表示順 */
	protected static final String MSG_NO_DISP = "表示順";
	protected static final int LENGTH_NO_DISP = 2;

	/** エラーメッセージ生成用文字：タイトル */
	protected static final String MSG_TITLE = "タイトル";
	protected static final int LENGTH_ZEN_TITLE = 50;
	protected static final int LENGTH_HALF_TITLE = 100;

	/** エラーメッセージ生成用文字：お知らせ */
	protected static final String MSG_INFO = "お知らせ";
	protected static final int LENGTH_ZEN_INFO = 250;
	protected static final int LENGTH_HALF_INFO = 500;

	/**
	 * エラーメッセージ出力 <br /> 各エラーチェックにて生成されたエラーメッセージを <br /> HTMLで出力する為に加筆・編集し、戻す。
	 * <br />
	 *
	 * @return エラー内容
	 */
	public String OutMsg() {
		StringBuffer msg = new StringBuffer();
		int size = msgs.size();
		msg.append("以下の " + size + "つ のエラーがあります。 内容を確認して下さい。<br>");
		for (int i = 0; i < size; i++) {
			msg.append("&nbsp;&nbsp;・" + (String) msgs.get(i) + "<br>");
		}
		return msg.toString();
	}

	/**
	 * エラーメッセージ出力 <br /> 各エラーチェックにて生成されたエラーメッセージのみ <br /> HTMLで出力する為に編集し、戻す。 <br
	 * />
	 *
	 * @return エラー内容
	 */
	public String OutMsgShort() {
		StringBuffer msg = new StringBuffer();
		int size = msgs.size();
		for (int i = 0; i < size; i++) {
			msg.append("&nbsp;&nbsp;" + (String) msgs.get(i) + "<br>");
		}
		return msg.toString();
	}

	/**
	 * 半角数値のみで構成されているかを判定し、結果を戻す。 <br />
	 *
	 * @param str
	 *            対象文字列
	 * @return 数値のみ:true
	 */
	protected boolean isNumeric(String str) {
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if ((c < '0') || (c > '9')) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 半角英字のみで構成されているかを判定し、結果を戻す。 <br /> 記号は対象外(ｴﾗｰ)とする
	 *
	 * @param c
	 *            対象文字列
	 * @return 半角英字のみ:true
	 */
	protected boolean isHalfchar(String str) {
		for (int i = 0; i < str.length(); i++) {

			char c = str.charAt(i);
			if (((c < 'A') || (c > 'Z')) && ((c < 'a') || (c > 'z'))) {
				return false;
			}
		}
		return true;

	}

	/**
	 * 半角英数字のみで構成されているかを判定し、結果を戻す。 <br /> 記号は対象外(ｴﾗｰ)とする
	 *
	 * @param c 対象文字列
	 * @return 半角英数字のみ + "_" :true
	 */
	protected boolean isHalfcharAndNumeric(String str) {
		for (int i = 0; i < str.length(); i++) {

			char c = str.charAt(i);
			if (((c < 'A') || (c > 'Z')) && ((c < 'a') || (c > 'z'))
					&& ((c < '0') || (c > '9')) && (c != '_' ))  {
				return false;
			}
		}
		return true;

	}

	/**
	 * 全角文字のバイト桁数を取得し、その桁数を返す。
	 *
	 * @param src
	 *            対象文字
	 * @return バイト桁数
	 */
	protected int isByteLenOver(String src) {
		int dstlen = 0;
		for (int i = 0; i < src.length(); i++) {
			dstlen += (src.charAt(i) <= 0xff ? 1 : 2);
		}
		return dstlen;
	}

	/**
	 * 日付か判定し、結果を戻す。 <br /> 記号は対象外(ｴﾗｰ)とする
	 *
	 * @param strDate
	 *            対象日付
	 * @param format
	 *            日付パターン
	 *
	 * @return 日付
	 */
	protected boolean isDate(String strDate, String format) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			Date dt = sdf.parse(strDate);

			if (sdf.format(dt).toString().equals(strDate)) {
				return true;
			} else {
				return false;
			}
		} catch (ParseException e) {
			if (format.equals(format.replaceAll("/", "")) == false) {
				if (isDate(strDate, format.replaceAll("/", "")) == true) {
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		}
	}

	/**
	 * 日付前後のチェックを行い、結果を戻す。 <br />
	 *
	 * @param obj1
	 *            対象日付(前)
	 * @param obj2
	 *            対象日付(後)
	 * @param format
	 *            対象日付フォーマット
	 * @return obj1 < obj2 ：true、その他：false
	 */
	protected boolean isDateBefore(String obj1, String obj2, String format) {

		try {
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			Date dt1 = sdf.parse(obj1);
			Date dt2 = sdf.parse(obj2);

			return dt1.before(dt2);

		} catch (Exception e) {
		}

		return false;
	}


	/**
	 * 必須,桁数,半角英数字のチェックを行い、結果を戻す。 <br />
	 *
	 * @param obj
	 *            チェック対象
	 * @param len
	 *            対象桁数
	 * @param objMSG
	 *            対象の表示文字
	 * @return エラーあり:true エラーなし:false
	 */
	protected boolean chkRequiredLHAN(String obj, int len, String objMSG) {
		boolean errflg = true;

		/* 必須,桁数,半角英数字 */
		if (obj.trim().length() == 0) {
			String msg = objMSG + "は" + MSG_REQ;
			msgs.add(msg);
			errflg = false;

		} else if (obj.trim().length() > len || this.isByteLenOver(obj) > len) {
			String msg = objMSG + "は" + len + MSG_LEN + "の" + MSG_HALFNUMENG + MSG_END;
			msgs.add(msg);
			errflg = false;

		// 2009/12/01 コメントアウト（ユーザーIDとパスワードはホストより取得する）
//		} else if (this.isHalfcharAndNumeric(obj) == false) {
//			String msg = objMSG + "は" + len + MSG_LEN + "の" + MSG_HALFNUMENG
//					+ MSG_END;
//			msgs.add(msg);
//			errflg = false;
		}
		return errflg;
	}

	/**
	 * 必須・半角数字文字数チェック
	 *
	 * @param obj
	 * @param len
	 * @param objMSG
	 * @return
	 */
	protected boolean chkRequiredLHAN(Object obj, int len, String objMSG) {
		if (obj == null || obj.toString().trim().length() == 0) {
			String msg = objMSG + "は" + MSG_REQ;
			msgs.add(msg);
			return false;
		}
		return chkLHAN((String) obj, len, objMSG);
	}

	/**
	 * 必須・半角英数字文字数チェック
	 *
	 * @param obj
	 * @param len
	 * @param objMSG
	 * @return
	 */
	protected boolean chkRequiredLHANFigure(Object obj, int len, String objMSG) {
		if (obj == null) {
			return false;
		} else if (obj.toString().trim().length() == 0) {
			String msg = objMSG + "は" + MSG_REQ;
			msgs.add(msg);
			return false;
		} else {
			return chkNonreqLN((String) obj, len, objMSG);
		}
	}

	protected boolean chkRequiredLHAN(Object obj, int len, int half,
			String objMSG) {
		if (obj == null) {
			return false;
		} else if (obj.toString().trim().length() == 0) {
			String msg = objMSG + "は" + MSG_REQ;
			msgs.add(msg);
			return false;
		} else {
			return chkNonreqL((String) obj, len, half, objMSG);
		}
	}

	/**
	 * 桁数,半角英数字のチェックを行い、結果を戻す。 <br />
	 *
	 * @param obj
	 *            チェック対象
	 * @param len
	 *            対象桁数
	 * @param objMSG
	 *            対象の表示文字
	 * @return エラーあり:true エラーなし:false
	 */
	protected boolean chkLHAN(String obj, int len, String objMSG) {
		boolean errflg = true;

		/* 桁数,半角英数字 */
		if (obj.trim().length() > len || this.isByteLenOver(obj) > len) {
			String msg = objMSG + "は" + len + MSG_LEN + "の" + MSG_HALFNUMENG
					+ MSG_END;
			msgs.add(msg);
			errflg = false;
		} else if (this.isHalfcharAndNumeric(obj) == false) {
			String msg = objMSG + "は" + len + MSG_LEN + "の" + MSG_HALFNUMENG
					+ MSG_END;
			msgs.add(msg);
			errflg = false;
		}
		return errflg;
	}

	/**
	 * 桁数,半角数字のチェックを行い、結果を戻す。 <br />
	 *
	 * @param obj
	 *            チェック対象
	 * @param len
	 *            対象桁数
	 * @param objMSG
	 *            対象の表示文字
	 * @return エラーあり:true エラーなし:false
	 */
	protected boolean chkNonreqLN(String obj, int len, String objMSG) {
		boolean errflg = true;

		/* 桁数,半角数字 */
		if (obj.toString().trim().length() != 0
				|| this.isByteLenOver(obj) > len) {
			if (obj.trim().length() > len) {
				String msg = objMSG + "は" + len + MSG_LEN + "の" + MSG_HALFNUM
						+ MSG_END;
				msgs.add(msg);
				errflg = false;
			} else if (this.isNumeric(obj) == false) {
				String msg = objMSG + "は" + len + MSG_LEN + "の" + MSG_HALFNUM
						+ MSG_END;
				msgs.add(msg);
				errflg = false;
			}
		}
		return errflg;
	}

	/**
	 * 桁数,半角数字のチェックを行い、結果を戻す。 <br />
	 *
	 * @param obj
	 *            チェック対象
	 * @param
	 * @param objMSG
	 *            対象の表示文字
	 * @return エラーあり:true エラーなし:false
	 */
	protected boolean chkNonreqLN(String obj, String objMSG) {
		boolean errflg = true;
		/* 桁数,半角数字 */
		if (obj.trim().length() != 0) {
			if (this.isNumeric(obj) == false) {
				String msg = objMSG + "は" + MSG_HALFNUM + MSG_END;
				msgs.add(msg);
				errflg = false;
			}
		}
		return errflg;
	}

	/**
	 * 桁数のチェックを行い、結果を戻す。 <br />
	 *
	 * @param obj
	 *            チェック対象
	 * @param len
	 *            対象桁数
	 * @param objMSG
	 *            対象の表示文字
	 * @return エラーあり:true エラーなし:false
	 */
	protected boolean chkNonreqL(String obj, int len, String objMSG) {
		boolean errflg = true;

		/* 桁数 */
		if (obj.trim().length() != 0) {
			if (obj.trim().length() > len || this.isByteLenOver(obj) > len) {
				String msg = objMSG + "は" + len + MSG_LEN + MSG_END;
				msgs.add(msg);
				errflg = false;
			}
		}

		return errflg;
	}

	protected boolean chkNonreqL(Object obj, int len, String objMSG) {
		boolean errflg = true;

		/* 桁数 */
		if (obj.toString().trim().length() != 0) {
			if (obj.toString().trim().length() > len
					|| this.isByteLenOver(obj.toString().trim()) > len) {
				String msg = objMSG + "は" + len + MSG_LEN + MSG_END;
				msgs.add(msg);
				errflg = false;
			}
		}
		return errflg;
	}

	protected boolean chkNonreqL(Object obj, int zenlen, int halflen,
			String objMSG) {
		boolean errflg = true;

		/* 桁数 */
		if (obj.toString().trim().length() != 0) {
			if (obj.toString().trim().length() > halflen
					|| this.isByteLenOver(obj.toString().trim()) > halflen) {
				String msg = objMSG + "は全角文字で" + zenlen + MSG_LEN + ",半角で"
						+ halflen + MSG_LEN + MSG_END;
				msgs.add(msg);
				errflg = false;
			}
		}
		return errflg;
	}

	/**
	 * 日付のチェックを行い、結果を戻す。 <br />
	 *
	 * @param obj
	 *            チェック対象
	 * @param format
	 *            対象日付フォーマット
	 * @param objMSG
	 *            対象の表示文字
	 * @return エラーあり:true エラーなし:false
	 */
	protected boolean chkNonreqDt(String obj, String format, String objMSG) {
		boolean errflg = true;

		/* 日付 */
		if (obj.trim().length() != 0) {
			if (this.isDate(obj, format) == false) {
				String msg = objMSG + "は (" + format + ") の" + MSG_DATE
						+ MSG_END;
				msgs.add(msg);
				errflg = false;
			}
		}

		return errflg;
	}

	/**
	 * 日付のチェックを行い、結果を戻す。 <br />
	 *
	 * @param obj
	 *            チェック対象
	 * @param format
	 *            対象日付フォーマット
	 * @param objMSG
	 *            対象の表示文字
	 * @return エラーなし：true、あり：false
	 */
	protected boolean chkNonreqDtFormat(String obj, String format, String objMSG) {

		/* 日付 */
		if (obj.trim().length() != 0) {
			try {
				SimpleDateFormat sdf = new SimpleDateFormat(format);
				Date dt = sdf.parse(obj);

				if (sdf.format(dt).toString().equals(obj)) {
					return true;
				}

			} catch (Exception e) {
			}

			String msg = objMSG + "は (" + format.toLowerCase() + ") の" + MSG_DATE + MSG_END;
			msgs.add(msg);

			return false;
		}

		return true;
	}


	/**
	 * 桁数のチェックを行い、結果を戻す。 <br />
	 *
	 * @param obj
	 *            チェック対象
	 * @param len
	 *            対象桁数
	 * @param objMSG
	 *            対象の表示文字
	 * @return エラーあり:true エラーなし:false MSG_REQ 必須入力項目です。
	 */
	protected boolean chkRequired(String obj, String objMSG) {
		boolean errflg = true;

		/* 桁数 */
		if (obj == null || obj.trim().length() == 0) {
			String msg = objMSG + "は" + MSG_REQ;
			msgs.add(msg);
			errflg = false;
		}

		return errflg;
	}

	protected boolean chkRequired(Object obj, String objMSG) {
		boolean errflg = true;

		/* 桁数 */
		if (obj == null || obj.toString().trim().length() == 0) {
			String msg = objMSG + "は" + MSG_REQ;
			msgs.add(msg);
			errflg = false;
		}
		return errflg;
	}

	/**
	 * @param obj
	 * @param objMSG
	 * @return エラーメッセージ：必須項目
	 */
	protected boolean chkRequired_notinput(Object obj, String objMSG) {
		boolean errflg = true;
		/* 桁数 */
		if (obj == null || obj.toString().trim().length() == 0) {
			String msg = objMSG + "は" + MSG_REQ_LEN;
			msgs.add(msg);
			errflg = false;
		}
		return errflg;
	}

	/**
	 * CSVファイルかどうかをチェックする。
	 *
	 * @param obj
	 * @param objMSG
	 * @return エラーメッセージ：必須項目
	 */
	protected boolean chk_csvfile(String csv_file, String objMSG) {
		boolean errflg = true;
		int i_period = 0;
		String csv_file_Type = null;
		if (csv_file == null || csv_file.equals("")) {
			// ファイルが添付されていない
			String msg = objMSG + "は" + MSG_REQ_LEN_FILE;
			msgs.add(msg);
			errflg = false;
		} else {
			i_period = csv_file.lastIndexOf(".");
			csv_file_Type = csv_file.substring(i_period + 1);
			if (!csv_file_Type.equals("csv") && !csv_file_Type.equals("CSV")
					&& !csv_file_Type.equals("Csv")
					&& !csv_file_Type.equals("cSv")
					&& !csv_file_Type.equals("csV")
					&& !csv_file_Type.equals("CSv")
					&& !csv_file_Type.equals("cSV")
					&& !csv_file_Type.equals("CsV") && i_period != 0) {
				// CSVファイルでない
				// ファイルが添付されていない
				String msg = MSG_REQ_CSV;
				msgs.add(msg);
				errflg = false;
			}
		}
		return errflg;
	}

	/**
	 * ファイルの存在をチェックする
	 *
	 * @param csv_file
	 * @param objMSG
	 * @return true ファイルが存在した場合 false ファイルが存在しない
	 */

	protected boolean chk_file(String csv_file) {
		if ((new File(csv_file)).exists()) {
			return true;
		} else {
			return false;
		}
	}

	protected boolean chk_file_size(long filesize) {
		if (filesize == 0) {
			msgs.add(Message.ERR_FILE_Existence);
			return false;
		}
		return true;
	}

	/**
	 * 日付の妥当性チェックを行います。 指定した日付文字列（yyyy/MM/dd or yyyy-MM-dd）が
	 * カレンダーに存在するかどうかを返します。
	 *
	 * @param strDate
	 *            チェック対象の文字列
	 * @return 存在する日付の場合true
	 */
	public static boolean checkDate(String strDate) {
		if (strDate == null || strDate.length() != 10) {
			return false;
			// throw new IllegalArgumentException(
			// "引数の文字列["+ strDate +"]" +
			// "は不正です。");
		}
		strDate = strDate.replace('-', '/');
		try {
			// format.parse(strDate);
			return true;
		} catch (Exception e) {
			System.out.println("checkDate");
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 日付の妥当性チェックを行います。 カレンダーに存在するかどうかを返します。
	 *
	 * @param strDate
	 *            チェック対象の文字列
	 * @return 存在する日付の場合true
	 */
	protected boolean checkDate_6(String strDate) {
		if (strDate == null || strDate.length() != 6) {
			return false;
			// throw new IllegalArgumentException(
			// "引数の文字列["+ strDate +"]" +
			// "は不正です。");
		}
		if (isNumeric(strDate) == false) {
			return false;
		}

		int iMonth = 0;
		try {
			iMonth = Integer.parseInt(strDate.substring(4, 6));
		} catch (Exception e) {
			return false;
		}
		if (iMonth <= 0 || iMonth >= 13) {
			return false;
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyymm");
		sdf.setLenient(false);
		try {
			Date date = sdf.parse(strDate);
		} catch (ParseException e) {
			// 不正な日付のときはこのブロックに到達
			return false;
		} catch (Exception s) {
			return false;

		}
		return true;
	}

	/**
	 * -チェック
	 *
	 * @param number
	 * @return
	 */
	public static boolean isMinus(String number) {
		// double
		try {
			double i = Double.parseDouble(number);
			if (i < 0) {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * 小数点チェック
	 *
	 */
	public static boolean isDecimal(String number) {
		// ^(([1-9]\\d{0,4})|0)(\\.\\d)?$
		try {
			// System.out.println("元データ:"+number);
			// double型にならずに文字があった場合Exceptionアウト！！
			// 数値をbigdecimalに変えて掛ける100をしている
			BigDecimal i = new BigDecimal(number).multiply(new BigDecimal(
					"100.0"));
			// System.out.println("BigDecimal_i:"+i);
			int ans1 = i.intValue();// int型に変換し、小数点以下を切り捨て
			// System.out.println("Int型に変換した値:："+ans1);
			BigDecimal answer = i
					.add(new BigDecimal("-" + String.valueOf(ans1)));

			// System.out.println("answer:"+answer);

			if (answer.doubleValue() > 0) {// 0ではないのでエラーになる。
				return false;
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * 0,1,null　空以外は以外はエラー
	 *
	 * @param number
	 * @return
	 */
	public static boolean isZeroOneNull(String number) {
		if (number.equals("0") || number.equals("1")) {
			return true;
		}
		return false;
	}

	/**
	 * 必須・半角数字文字数チェック 月日6文字
	 *
	 * @param obj
	 * @param len
	 * @param objMSG
	 * @return
	 */
	public boolean chkRequiredLHAN(String obj) {
		if (obj == null || obj.toString().trim().length() == 0) {
			return false;
		}
		if (obj.toString().trim().length() == 6) {
			return true;
		}

		return false;
	}

	/**
	 * 必須チェック
	 *
	 * @param obj
	 * @return
	 */
	protected boolean chknotinput(String str) {
		boolean errflg = true;
		/* 桁数 */
		if (str == null || str.toString().trim().length() == 0) {
			errflg = false;
		}
		return errflg;
	}

	protected boolean isNum(String str) {
		try {
			double judgment = Double.parseDouble(str);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Nullﾁｪｯｸ 文字数がある場合 falseを返す
	 *
	 * @param str
	 * @return
	 */
	protected boolean isNull(String str) {
		if (str.length() != 0) {
			return false;
		}
		return true;
	}

	/**
	 * 時間のcheck HHMM
	 *
	 * @return
	 */
	protected boolean chkTime(String time) {
		try {
			if (Integer.parseInt(time.substring(0, 2)) > 23
					|| Integer.parseInt(time.substring(0, 2)) < 0) {
				return true;
			}

			if (Integer.parseInt(time.substring(2, 4)) > 59
					|| Integer.parseInt(time.substring(2, 4)) < 0) {
				return true;
			}
		} catch (Exception e) {
			System.out.println("chkTime Exception");
			e.printStackTrace();
			return true;
		}
		return false;
	}

}
