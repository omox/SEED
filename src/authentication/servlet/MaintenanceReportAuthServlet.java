package authentication.servlet;

import java.io.*;
import java.sql.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.commons.dbutils.*;
import org.apache.commons.dbutils.handlers.*;

import authentication.bean.*;
import authentication.connection.*;
import authentication.defines.*;
import authentication.util.*;
import authentication.validation.*;

public class MaintenanceReportAuthServlet extends HttpServlet {

	/**
	 *
	 */
	private static final long serialVersionUID = 147341193683962344L;
	private ServletConfig config = null;

	/**
	 * 初期化
	 */
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		this.config = config;
	}

	/**
	 * 破棄
	 */
	public void destroy() {
		super.destroy();
	}

	/**
	 * リクエスト時処理
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		this.doPost(request, response);
	}

	/**
	 * リクエスト時処理
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		HttpSession session = request.getSession();

		String strPage = null;

		try {

			MaintenanceReportAuth selection = new MaintenanceReportAuth();

			/** 権限List読込みチェック */
			if (session.getAttribute(Consts.STR_SES_GRD4) == null) {
				/** 取得していない場合 */

				ArrayList reportclassList = getReportCalss();
				ArrayList reportList = getReportNameRecord();

				// リスト設定
				session.setAttribute(Consts.STR_SES_GRD2, reportclassList);
				session.setAttribute(Consts.STR_SES_GRD4, reportList);

			}
			if (session.getAttribute(Consts.STR_SES_TITLE) == null) {
				String report_title = request.getParameter(Form.REPORT_NAME) != null ? request
						.getParameter(Form.REPORT_NAME).toString().trim()
						: null;
				String name = getReport_title(report_title);
				session.setAttribute(Consts.STR_SES_TITLE, name);
			}
			/** UserListデータ読込チェック */
			if (session.getAttribute(Consts.STR_SES_GRD) == null) {
				/** 取得していない場合 */
				ArrayList report_detaList = getReportAuthRecord();
				// リスト設定
				session.setAttribute(Consts.STR_SES_GRD, report_detaList);
				/** 上書き保存ArrayList生成 */
				ArrayList updlist_set = updarraylist();
				session.setAttribute(Consts.UPD_DETA_LIST, updlist_set);

			} else {
				/** 取得済みの場合 */
				// 入力文字エスケープ
				Tagescape esc = new Tagescape();

				/** Listデータ読込 */
				ArrayList menuList = (ArrayList) session
						.getAttribute(Consts.STR_SES_GRD);
				ArrayList updList = (ArrayList) session
						.getAttribute(Consts.UPD_DETA_LIST);

				/** * 処理エリア判別 */
				if (request.getParameter(Form.MTN_INPAREA) != null) {
					/**
					 * 入力エリア処理
					 */
					// イベント取得
					String event = request.getParameter(Form.MTN_INPAREA)
							.toString();
					MaintenanceReportAuthValidation vld = new MaintenanceReportAuthValidation();
					boolean eraflg = true;
					if (event.equals(Form.MTN_RSTBTN)) {
						/** リセットボタン */
						// 初期化済の為、処理なし
					} else if (event.equals(Form.MTN_ENTBTN)) {
						/** 登録ボタン */
						/** 入力値取得 */
						HashMap entrys = new HashMap();
						entrys
								.put(Form.MTN_REPORT_CODE, esc
										.htmlEscape(request.getParameter(
												Form.MTN_REPORT_CODE)
												.toString()));
						entrys.put(Form.REPORT_SIDE, esc.htmlEscape(request
								.getParameter(Form.REPORT_SIDE).toString()));
						entrys.put(Form.REPORT_DISP_SIDE, esc
								.htmlEscape(request.getParameter(
										Form.REPORT_DISP_SIDE).toString()
										.trim()));
						entrys
								.put(Form.MTN_USR_ETY_IDX, esc
										.htmlEscape(request.getParameter(
												Form.MTN_USR_ETY_IDX)
												.toString()));
						/* チェック */
						if (vld.entry(entrys, menuList) == false) {// ||
																	// vld.roolchek
																	// (
																	// hander)==
																	// false) {
																	// 権限を厳しくするために使用
							/* エラーメッセージの設定 */

							request.setAttribute(Form.COMMON_MSGTYPE,
									Form.COMMON_MSGTYPE_ERR);
							request.setAttribute(Form.COMMON_MSG, vld
									.OutMsgShort());

							MaintenanceReportAuth menu = null;
							int report_no = entrys.get(Form.MTN_REPORT_CODE)
									.toString().trim().length() != 0
									&& entrys.get(Form.MTN_REPORT_CODE) != null ? Integer
									.parseInt(entrys.get(Form.MTN_REPORT_CODE)
											.toString().trim())
									: 0;
							int report_side = entrys.get(Form.REPORT_SIDE)
									.toString().trim().length() != 0
									&& entrys.get(Form.REPORT_SIDE) != null ? Integer
									.parseInt(entrys.get(Form.REPORT_SIDE)
											.toString().trim())
									: 0;
							String disp_number = (String) entrys
									.get(Form.REPORT_DISP_SIDE);
							if (entrys.get(Form.MTN_USR_ETY_IDX) != null
									&& Integer.parseInt((String) entrys
											.get(Form.MTN_USR_ETY_IDX)) >= 0) {
								int idx = Integer.parseInt((String) entrys
										.get(Form.MTN_USR_ETY_IDX));
								menu = (MaintenanceReportAuth) menuList
										.get(idx);
								menu = new MaintenanceReportAuth(report_no,
										report_side, disp_number, menu
												.getIndex());
							} else {
								menu = new MaintenanceReportAuth(report_no,
										report_side, disp_number);
							}
							selection = menu;
							eraflg = false;
						} else {

							/* エラーなし */
							MaintenanceReportAuth menu = null;
							int idx = 0;
							int upidx = 0;
							if (entrys.get(Form.MTN_USR_ETY_IDX) != null
									&& Integer.parseInt((String) entrys
											.get(Form.MTN_USR_ETY_IDX)) >= 0) {
								// 登録済みデータ更新

								idx = Integer.parseInt((String) entrys
										.get(Form.MTN_USR_ETY_IDX));
								menu = (MaintenanceReportAuth) menuList
										.get(idx);
								// 上書き登録
								upidx = updList.size();
								updList
										.add(upidx, new MaintenanceReportAuth(
												menu.getReport_no(), menu
														.getReport_side(), menu
														.getDispNumber(),
												"update", idx));
								menu.setReport_no(Integer
										.parseInt(entrys.get(
												Form.MTN_REPORT_CODE)
												.toString().trim()));
								menu.setReport_side(Integer.parseInt(entrys
										.get(Form.REPORT_SIDE).toString()
										.trim()));
								menu.setDispNumber(((String) entrys
										.get(Form.REPORT_DISP_SIDE)));

								menu.setUpd();
								// 登録データ反映
								menuList.set(idx, menu);
							} else {
								// 新規登録データ
								idx = menuList.size();

								int report_no = Integer
										.parseInt(entrys.get(
												Form.MTN_REPORT_CODE)
												.toString().trim());
								int report_side = Integer.parseInt(entrys.get(
										Form.REPORT_SIDE).toString().trim());
								String disp_number = (String) entrys
										.get(Form.REPORT_DISP_SIDE);

								menu = new MaintenanceReportAuth(report_no,
										report_side, disp_number, idx);
								menu.setUpd();
								// 登録データ追加
								menuList.add(idx, menu);

							}
							// リスト設定
							session.setAttribute(Consts.UPD_DETA_LIST, updList);
							session.setAttribute(Consts.STR_SES_GRD, menuList);
						}
					} else if (event.equals(Form.MTN_DELBTN)) {
						/** 削除ボタン */

						/** 入力値取得 */
						HashMap entrys = new HashMap();
						entrys
								.put(Form.MTN_REPORT_CODE, esc
										.htmlEscape(request.getParameter(
												Form.MTN_REPORT_CODE)
												.toString()));
						entrys.put(Form.REPORT_SIDE, esc.htmlEscape(request
								.getParameter(Form.REPORT_SIDE).toString()));
						entrys.put(Form.REPORT_DISP_SIDE, esc
								.htmlEscape(request.getParameter(
										Form.REPORT_DISP_SIDE).toString()
										.trim()));
						entrys
								.put(Form.MTN_USR_ETY_IDX, esc
										.htmlEscape(request.getParameter(
												Form.MTN_USR_ETY_IDX)
												.toString()));

						/* チェック */
						if (vld.delete(entrys, menuList) == false) {
							/* エラーメッセージの設定 */
							request.setAttribute(Form.COMMON_MSGTYPE,
									Form.COMMON_MSGTYPE_ERR);
							request.setAttribute(Form.COMMON_MSG, vld
									.OutMsgShort());
							MaintenanceReportAuth menu = null;

							int report_no = entrys.get(Form.MTN_REPORT_CODE)
									.toString().trim().length() != 0
									&& entrys.get(Form.MTN_REPORT_CODE) != null ? Integer
									.parseInt(entrys.get(Form.MTN_REPORT_CODE)
											.toString().trim())
									: 0;
							int report_side = entrys.get(Form.REPORT_SIDE)
									.toString().trim().length() != 0
									&& entrys.get(Form.REPORT_SIDE) != null ? Integer
									.parseInt(entrys.get(Form.REPORT_SIDE)
											.toString().trim())
									: 0;
							String disp_number = (String) entrys
									.get(Form.REPORT_DISP_SIDE);
							/** 削除後　行番号保持 */
							if (entrys.get(Form.MTN_USR_ETY_IDX) != null
									&& Integer.parseInt((String) entrys
											.get(Form.MTN_USR_ETY_IDX)) >= 0) {
								int idx = Integer.parseInt((String) entrys
										.get(Form.MTN_USR_ETY_IDX));
								menu = (MaintenanceReportAuth) menuList
										.get(idx);
								menu = new MaintenanceReportAuth(report_no,
										report_side, disp_number, menu
												.getIndex());
							} else {
								menu = new MaintenanceReportAuth(report_no,
										report_side, disp_number);
							}

							selection = menu;
							eraflg = false;
						} else {
							/* エラーなし */

							// 登録済みデータ更新
							int idx = 0;
							idx = Integer.parseInt((String) entrys
									.get(Form.MTN_USR_ETY_IDX));

							MaintenanceReportAuth usr = (MaintenanceReportAuth) menuList
									.get(idx);
							usr.setDel();

							// 削除データ反映
							menuList.set(idx, usr);
							// リスト設定
							session.setAttribute(Consts.STR_SES_GRD, menuList);
						}
					}
					if (eraflg != false
							&& event.equals(Form.MTN_RSTBTN) == false) {
						// else if (event.equals(Form.MTN_SAVBTN)) {
						/** 保存ボタン */

						/* チェック */
						if (vld.save(menuList) == false) {
							/* エラーメッセージの設定 */
							request.setAttribute(Form.COMMON_MSGTYPE,
									Form.COMMON_MSGTYPE_ERR);
							request.setAttribute(Form.COMMON_MSG, vld
									.OutMsgShort());
						} else {
							/* エラーなし */
							User loginusr = (User) session
									.getAttribute(Consts.STR_SES_LOGINUSER);
							String msg = null;
							msg = setUserRecord(menuList, loginusr, updList,
									msg);

							// 再設定
							menuList = getReportAuthRecord();
							ArrayList reportclassList = getReportCalss();
							ArrayList reportList = getReportNameRecord();

							// リスト設定
							session.setAttribute(Consts.STR_SES_GRD, menuList);
							session.setAttribute(Consts.STR_SES_GRD2,
									reportclassList);
							session.setAttribute(Consts.STR_SES_GRD4,
									reportList);

							request.setAttribute(Form.COMMON_MSGTYPE,
									Form.COMMON_MSGTYPE_SUP);
							request.setAttribute(Form.COMMON_MSG, msg);
						}
					}
				} else if (request.getParameter(Form.MTN_SELAREA) != null) {
					/**
					 * 選択エリア処理
					 */
					int idx = Integer.parseInt((String) request
							.getParameter(Form.MTN_SELAREA));
					selection = (MaintenanceReportAuth) menuList.get(idx);
				}
			}

			request.setAttribute(Consts.STR_REQ_REC, selection);
			strPage = config.getInitParameter("MaintenanceReportAuth");
			/**
			 * 例外処理
			 */
		} catch (Exception e) {
			e.printStackTrace();
			// エラーメッセージの設定
			request.setAttribute(Form.COMMON_MSGTYPE, Form.COMMON_MSGTYPE_ERR);
			request.setAttribute(Form.COMMON_MSG, e.getMessage());
			// ページ遷移先の設定
			strPage = this.getServletContext().getInitParameter("Page_Error");
		}

		/**
		 * ページ遷移
		 */
		getServletContext().getRequestDispatcher(strPage).forward(request,
				response);
	}

	/**
	 *レポートマスタ <br /> DBより権限情報を全て取得し、ArrayList形式で戻す。<br />
	 *
	 * @return 取得したArrayList形式Userデータ <br />
	 * @throws Exception
	 *             例外 <br />
	 */
	protected ArrayList getReportNameRecord() throws Exception {
		List list = null;
		try {
			QueryRunner qr = new QueryRunner(DBConnection.getDataSource());
			ResultSetHandler rsh = new MapListHandler();
			list = (List) qr.query(SQL.REPORT_SEL_002, rsh);
		} catch (Exception en) {
			System.out.println("DBUtils NullPointerException:MaintenanceUser_getReportNameRecord()");
		}

		Map mu = null;
		ArrayList beanList = null;
		if (list != null) {
			beanList = new ArrayList();
			for (int i = 0; i < list.size(); i++) {
				mu = (Map) list.get(i);
				beanList.add(i, new Report(Integer.parseInt(mu.get(	"cd_report_no").toString().trim()),
						mu.get("nm_report").toString().trim()));
			}
		}

		return beanList;
	}

	/**
	 *レポート分類マスタよりレポート分類名称取得 <br /> DBより権限情報を全て取得し、ArrayList形式で戻す。<br />
	 *
	 * @return 取得したArrayList形式Userデータ <br />
	 * @throws Exception
	 *             例外 <br />
	 */
	protected ArrayList getReportCalss() throws Exception {
		List list = null;
		try {
			QueryRunner qr = new QueryRunner(DBConnection.getDataSource());
			ResultSetHandler rsh = new MapListHandler();
			list = (List) qr.query(SQL.SIDE_SEL_004, rsh);

		} catch (Exception en) {
			System.out.println("DBUtils NullPointerException:MaintenanceUser_getReportClass()");
		}

		Map mu = null;
		ArrayList beanList = null;

		if (list != null) {
			beanList = new ArrayList();
			for (int i = 0; i < list.size(); i++) {
				mu = (Map) list.get(i);
				beanList.add(i, new Side(Integer.parseInt(mu.get("cd_report_side").toString().trim()),
						mu.get("nm_report_side").toString().trim(),
						mu.get("cd_disp_column").toString().trim(),
						1));
			}
		}

		return beanList;
	}

	/**
	 * ユーザー情報取得 <br />レポートコード DBよりユーザー情報を全て取得し、ArrayList形式で戻す。<br />
	 *
	 * @return 取得したArrayList形式Userデータ <br />
	 * @throws Exception
	 *             例外 <br />
	 */
	protected ArrayList getReportAuthRecord() throws Exception {
		List list = null;
		try {
			QueryRunner qr = new QueryRunner(DBConnection.getDataSource());
			ResultSetHandler rsh = new MapListHandler();
			list = (List) qr.query(SQL.ATH_REPORT_SEL_001, rsh);
		} catch (Exception en) {
			System.out
					.println("DBUtils NullPointerException:MaintenanceUser_getReportAuthRecord()");
		}
		Map mu = null;
		ArrayList beanList = null;
		if (list != null) {
			beanList = new ArrayList();
			for (int i = 0; i < list.size(); i++) {
				mu = (Map) list.get(i);
				beanList.add(i, new MaintenanceReportAuth(Integer.parseInt(mu
						.get("CD_REPORT_NO").toString().trim()), Integer
						.parseInt(mu.get("CD_REPORT_SIDE").toString().trim()),
						mu.get("CD_DISP_NUMBER").toString().trim(), i));
			}
		}
		return beanList;
	}

	/**
	 *
	 * @return
	 * @throws Exception
	 */
	protected ArrayList updarraylist() throws Exception {

		ArrayList beanList = null;

		beanList = new ArrayList();

		return beanList;
	}

	/**
	 * ユーザーレポート管理に権限設定 <br /> 画面にて変更を行ったUserListをDBへ反映させる。 <br />
	 *
	 * @param list
	 *            ArrayList形式Userデータ <br />
	 * @param loginusr
	 *            ログインユーザ情報 <br />
	 * @throws SQLException
	 *             例外 <br />
	 */
	protected String setUserRecord(ArrayList list, User loginusr,
			ArrayList updlist, String msg) throws SQLException {

		Connection con = null; // コネクション
		String save = null;// 保存･削除メッセージ
		try {
			/* コネクションの取得 */

			QueryRunner qr = new QueryRunner(DBConnection.getDataSource());
			con = qr.getDataSource().getConnection();
			con.setAutoCommit(false);

			/* List内データの内、Upd,Delのみ更新対象とする */
			int size = list.size();
			int upsize = updlist.size();
			for (int i = 0; i < size; i++) {
				MaintenanceReportAuth side = (MaintenanceReportAuth) list
						.get(i);
				if (side.getState() == MaintenanceUser.STATE_UPD) {
					/**
					 * UPDATE or INSERTを行うデータ
					 */

					/**
					 * UPDATE
					 */
					int cnt = 0;
					save = Consts.SQL_MESSAGE_IN;
					for (int j = 0; j < upsize; j++) {
						MaintenanceReportAuth updlist_date = (MaintenanceReportAuth) updlist
								.get(j);
						if (updlist_date.getIndex() == i) {
							cnt = qr.update(con, SQL.ATH_REPORT_UPD_001,
									new Object[] {
											new Integer(side.getReport_no()),
											new Integer(side.getReport_side()),
											side.getDispNumber(),
											new Integer(updlist_date
													.getReport_no()),
											new Integer(updlist_date
													.getReport_side()),
											updlist_date.getDispNumber() });
							System.out
									.println("UPDATE pkey db[sys_report_auth]:"
											+ side.getReport_no() + " COUNT:"
											+ cnt);
						}
					}

					// 1件もUPDATEできなかった場合はINSERTを行う
					if (cnt == 0) {
						/**
						 * INSERT
						 */
						cnt = qr.update(con, SQL.ATH_REPORT_INS_001,
								new Object[] {
										new Integer(side.getReport_no()),
										new Integer(side.getReport_side()),
										side.getDispNumber(), loginusr.getId(),
										loginusr.getId() });
						System.out.println("INSERT pkey db[sys_report_auth]:"
								+ side.getReport_no() + " COUNT:" + cnt);
					}

				} else if (side.getState() == MaintenanceReportAuth.STATE_DEL) {
					/**
					 * DELETE を行うデータ
					 */
					save = Consts.SQL_MESSAGE_DELETE;
					int cnt = qr.update(con, SQL.ATH_REPORT_DEL_001,
							new Object[] { new Integer(side.getReport_no()),
									new Integer(side.getReport_side()),
									side.getDispNumber() });
					System.out.println("DELETE pkey db[sys_report_auth]:"
							+ side.getReport_no() + " COUNT:" + cnt);
				}
			}
			DbUtils.commitAndCloseQuietly(con);
			msg = save + Message.SUP_SAVE_END;
			/**
			 * 例外処理
			 */
		} catch (SQLException e) {
			try {
				System.out.println("sqlError");
				DbUtils.rollback(con);
			} catch (SQLException e2) {
			}
			DbUtils.closeQuietly(con);
			msg = save + Message.ERR_SAVE_END;
		}
		return msg;
	}

	/**
	 * レポート管理権限チェック
	 */
	protected boolean roolhander(HashMap entrys, User loginusr)
			throws SQLException {
		List sqllist = null;
		try {
			QueryRunner qr = new QueryRunner(DBConnection.getDataSource());
			ResultSetHandler rsh = new MapListHandler();
			sqllist = (List) qr.query(SQL.ATH_GEOUP_ROOL_CHECK, new Object[] {
					loginusr.getId(), loginusr.getId() }, rsh);
			/**
			 * 例外処理
			 */
		} catch (Exception en) {
			System.out.println("DBUtils NullPointerException:権限エラー");
		}
		boolean hander = false;
		Map mu = null;
		if (sqllist != null) {
			for (int i = 0; i < sqllist.size(); i++) {
				mu = (Map) sqllist.get(i);
				if (Integer.parseInt(mu.get("cd_position").toString().trim()) == Consts.SYSTEM_MASTER) {
					hander = true;
					break;
				} else {
					hander = false;
				}
			}
		}
		return hander;
	}

	/**
	 *レポートマスタよりレポート名称取得 <br />
	 * */
	protected String getReport_title(String report_no) throws Exception {
		String title = null;
		Map mu = null;
		try {
			QueryRunner qr = new QueryRunner(DBConnection.getDataSource());
			ResultSetHandler rsh = new MapHandler();
			mu = (Map) qr.query(SQL.REPORT_NAME_TITLE, report_no, rsh);
		} catch (Exception en) {
			System.out
					.println("DBUtils NullPointerException:getReport_title DB :sys_report_name");
		}

		if (mu == null) {
			title = "no_title";
		} else {
			title = mu.get("NM_REPORT").toString().trim();
		}

		return title;
	}

}
