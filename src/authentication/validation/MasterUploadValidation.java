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

import java.text.*;
import java.util.*;

import org.apache.commons.dbutils.*;
import org.apache.commons.dbutils.handlers.*;

import authentication.connection.*;
import authentication.defines.*;

public class MasterUploadValidation extends Validation {
	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	String today = sdf.format(new java.util.Date());

	public static java.lang.String trimNeedlessChara2(java.lang.String getData) {
		return getData;
	}

	/**
	 * メニューメンテナンス画面：登録ボタンのチェック <br />
	 *
	 * @param entrys
	 *            入力ユーザー
	 * @return エラーなし：true、あり：false
	 */
	public boolean entry(String csv_file, long filesize, boolean hander) {
		boolean errflg = true;
		if (hander == true) {
			if (super.chk_csvfile(csv_file, MSG_UP_LOAD) == false) {
				errflg = false;
			} else if (super.chk_file_size(filesize) == false) {
				errflg = false;
			}
		} else {
			errflg = false;
		}
		return errflg;
	}

	public boolean entry(String csv_file) {
		boolean errflg = true;
		if (super.chk_csvfile(csv_file, MSG_UP_LOAD) == false) {
			errflg = false;
		}

		return errflg;
	}

	/**
	 * csvファイルアップロードファイル1行目チェック 列数と項目名チェック
	 *
	 * @param acconut
	 *            csvファイル項目
	 * @param getTablename
	 *            　テーブル名
	 * @return false:エラー true：OK
	 * @throws Exception
	 */
	public boolean csvCheck(Map acconut, String getTablename, int sort) {
		List list = null;
		try {
			QueryRunner qr = new QueryRunner(DBConnection.getDataSource());
			ResultSetHandler rsh = new MapListHandler();
			list = (List) qr.query(SQL.MASTER_TABLE_SEL + "'"
					+ getTablename.toString() + "'"
					+ SQL.MASTER_TABLE_SEL_WHERE, rsh);
		} catch (Exception e) {
			System.out.println("DBUtils NullPointerException:getMstMaster");
		}
		Map mu = null;
		int a = 0;
		// 項目名チェック
		if (list != null) {
			for (int i = 0; i < acconut.size(); i++) {
				a++;
				mu = (Map) list.get(i);
				if (!(acconut.get(mu.get("NM_VIEW_NAME").toString()).toString()
						.replaceAll(",", "").equals(mu.get("NM_VIEW_NAME")
						.toString()))) {
					msgs.add(Message.UP_EER);
					return false;
				}
			}
		} else {
			super.msgs.add(Message.UP_EER);
			return false;
		}
		// 列数チェック
		if (a != sort) {
			super.msgs.add(Message.UP_EER);
			return false;
		}
		return true;
	}

	/**
	 * csvファイルアップロードファイル1行目チェック 列数と項目名チェック
	 *
	 * @param acconut
	 *            csvファイル項目
	 * @param getTablename
	 *            　テーブル名
	 * @return false:エラー true：OK
	 * @throws Exception
	 */
	public boolean csvCheck(String[] acconut, String getTablename, int sort) {
		List list = null;
		try {
			QueryRunner qr = new QueryRunner(DBConnection.getDataSource());
			ResultSetHandler rsh = new MapListHandler();
			list = (List) qr.query(SQL.MASTER_TABLE_SEL + "'"
					+ getTablename.toString() + "'"
					+ SQL.MASTER_TABLE_SEL_WHERE, rsh);
		} catch (Exception e) {
			System.out.println("DBUtils NullPointerException:getMstMaster");
		}
		Map mu = null;
		int a = 0;
		// 項目名チェック
		if (list != null) {
			for (int i = 0; i < acconut.length; i++) {
				a++;
				if (a > sort) {
					a--;
					break;
				} else {
					mu = (Map) list.get(i);
					if (!(acconut[i].toString().replaceAll(",", "").replaceAll(
							"^[\\s　]*", "").replaceAll("[\\s　]*$", "")
							.equals(mu.get("NM_VIEW_NAME").toString()))) {
						msgs.add(Message.UP_EER);
						return false;
					}
				}
			}
		} else {
			System.out.println("MST_Master_list_null:csvCheck");
			super.msgs.add(Message.UP_EER);
			return false;
		}
		// 列数チェック
		if (a != sort) {
			System.out.println("list_csv_num:" + a);
			System.out.println("list_sort_num:" + sort);
			super.msgs.add(Message.UP_EER);
			return false;
		}
		return true;
	}

	public void Strsql(String sql, int ercount) {
		msgs.add("　" + ercount + Message.ERR_SAVE_NO + sql);
	}

	public void setMsg(String massage) {
		msgs.add(massage);

	}

	public boolean dowCheck(List list, List masterlist) {

		Map mst = null;
		if (list != null) {
			for (int i = 0; i < list.size(); i++) {
				// 1行目のヘッダー作成
				if (i == 0) {
					for (int k = 0; k < masterlist.size(); k++) {
						mst = (Map) masterlist.get(k);
						if (mst.get("NM_VIEW_NAME").toString() == null) {
							return false;
						}
					}
				}
			}
		}
		return true;
	}

	/**
	 * 日付チェック
	 *
	 * @param strDate
	 * @return true 可 false 否
	 */
	public boolean checkDatestr(String strDate, String table) {
		if (super.checkDate(strDate) != true) {
			return false;
		}
		return true;
	}

	/**
	 *
	 * @param csvinfo
	 * @param erhander
	 * @param table
	 * @return
	 *
	 */
	public boolean InfoTrap(String[] csvinfo, ArrayList erhander, String table) {

		for (int i = 0; i < csvinfo.length; i++) {
			// System.out.println("*****< 項目チェック start >*****");

			String Binary = Integer.toBinaryString(Integer.parseInt(erhander
					.get(i).toString()));
			for (int hander = Binary.length(); hander > 0; hander--) {
				// System.out.println("--"+s+"@@"+hander);
				char c = Binary.charAt(hander - 1);
				if (c == '1') {
					switch (Binary.length() - hander + 1) {
					case 1:
						// 必須チェック
						// System.out.println("chknotinput");
						if (super.chknotinput(csvinfo[i].toString().trim()) == false) {
							return false;
						}
						break;
					case 2:
						// 数値チェック
						// System.out.println("isNumeric");
						if (super.isNull(csvinfo[i].toString().trim()) == false) {
							if (super.isNum(csvinfo[i].toString().trim()) == false) {
								return false;
							}
						}
						break;
					case 3:
						// -チェック
						// System.out.println("isMinus:" );
						if (super.isNull(csvinfo[i].toString().trim()) == false) {
							if (super.isMinus(csvinfo[i].toString().trim()) == false) {
								return false;
							}
						}
						break;
					case 4:
						// 少数第二までのチェック
						// System.out.println("isDecimal：");
						if (super.isNull(csvinfo[i].toString().trim()) == false) {
							if (super.isDecimal(csvinfo[i].toString().trim()) == false) {
								return false;
							}
						}
						break;
					// 0,1エラー
					case 5:
						// System.out.println("isZeroOneNull");
						if (super.isNull(csvinfo[i].toString().trim()) == false) {
							if (super.isZeroOneNull(csvinfo[i]) == false) {
								return false;
							}
						}
						break;
					// 日付YYYY-MM-DD
					case 6:
						// System.out.println("checkDatestr");
						if (super.isNull(csvinfo[i].toString().trim()) == false) {
							if (checkDatestr(csvinfo[i].toString().trim(),
									table) == false) {
								// System.out.println("err");
								return false;
							}
						}
						break;
					// 日付YYYYMM
					case 7:
						// System.out.println("適用年月チェックYYYYMM");
						if (super.isNull(csvinfo[i].toString().trim()) == false) {
							if (super.checkDate_6(csvinfo[i].toString().trim()) == false) {
								return false;
							}
						}
						break;
					default:
						break;
					}
				}
			}
			// System.out.println("*****< 項目チェック  end  >*****");
		}
		return true;
	}
}
