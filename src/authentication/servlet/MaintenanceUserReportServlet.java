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

public class MaintenanceUserReportServlet extends HttpServlet {
	/**
	 *
	 */
	private static final long serialVersionUID = -9216325742873498972L;
	private ServletConfig config = null;

	/**
	 * 初期化 <br />
	 */
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		this.config = config;
	}

	/**
	 * 破棄 <br />
	 */
	public void destroy() {
		super.destroy();
	}

	/**
	 * リクエスト時処理 <br />
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		this.doPost(request, response);
	}

	/**
	 * リクエスト時処理 <br />
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		HttpSession session = request.getSession();

		String strPage = null;
		try {
			MaintenanceUserReport selection = new MaintenanceUserReport();
			/** タイトル取得 */
			if (session.getAttribute(Consts.STR_SES_TITLE) == null) {
				String report_title = request.getParameter(Form.REPORT_NAME) != null ? request
						.getParameter(Form.REPORT_NAME).toString().trim()
						: null;
				String name = getReport_title(report_title);
				session.setAttribute(Consts.STR_SES_TITLE, name);
			}

			/** 権限List読込みチェック */
			if (session.getAttribute(Consts.STR_SES_GRD2) == null) {
				/** 取得していない場合 */
				ArrayList posList = getPosRecord();
				ArrayList athList = getAthRecord();
				ArrayList reportList = getReportNameRecord();

				// リスト設定
				session.setAttribute(Consts.STR_SES_GRD2, posList);
				session.setAttribute(Consts.STR_SES_GRD3, athList);
				session.setAttribute(Consts.STR_SES_GRD4, reportList);
			}
			/** UserListデータ読込チェック */
			if (session.getAttribute(Consts.STR_SES_GRD) == null) {
				/** 取得していない場合 */
				ArrayList report_detaList = getReportRecord();
				// リスト設定
				session.setAttribute(Consts.STR_SES_GRD, report_detaList);

				ArrayList updlist_set = updarraylist();
				session.setAttribute(Consts.UPD_DETA_LIST, updlist_set);

			} else {
				/** 取得済みの場合 */
				// 入力文字エスケープ
				Tagescape esc = new Tagescape();

				/** Listデータ読込 */
				ArrayList menuList = (ArrayList) session.getAttribute(Consts.STR_SES_GRD);
				ArrayList updList = (ArrayList) session.getAttribute(Consts.UPD_DETA_LIST);
				/** * 処理エリア判別 */
				if (request.getParameter(Form.MTN_INPAREA) != null) {

					/**
					 * 入力エリア処理
					 */
					// イベント取得
					String event = request.getParameter(Form.MTN_INPAREA).toString();

					MaintenanceUserReportValidation vld = new MaintenanceUserReportValidation();
					// エラーフラグ。エラー時に保存処理をさせないために。
					boolean eraflg = true;
					if (event.equals(Form.MTN_RSTBTN)) {
						/** リセットボタン */
						// 初期化済の為、処理なし

					} else if (event.equals(Form.MTN_ENTBTN)) {
						/** 登録ボタン */

						/** 入力値取得 */
						HashMap entrys = new HashMap();
						entrys.put(Form.MTN_USR_ETY_ATH, esc.htmlEscape(request.getParameter(Form.MTN_USR_ETY_ATH).toString()));
						entrys.put(Form.MTN_USR_ETY_POS, esc.htmlEscape(request.getParameter(Form.MTN_USR_ETY_POS).toString()));
						entrys.put(Form.MTN_REPORT_CODE, esc.htmlEscape(request.getParameter(Form.MTN_REPORT_CODE).toString()));
						entrys.put(Form.MTN_USR_ETY_IDX, esc.htmlEscape(request.getParameter(Form.MTN_USR_ETY_IDX).toString()));
						entrys.put(Form.MTN_ENABLE_MENU, esc.htmlEscape(request.getParameter(Form.MTN_ENABLE_MENU).toString()));

						/* チェック */
						if (vld.entry(entrys, menuList) == false) {
							/* エラーメッセージの設定 */
							request.setAttribute(Form.COMMON_MSGTYPE, Form.COMMON_MSGTYPE_ERR);
							request.setAttribute(Form.COMMON_MSG, vld.OutMsgShort());
							MaintenanceUserReport menu = null;

							int group = entrys.get(Form.MTN_USR_ETY_ATH).toString().trim().length() != 0
									&& entrys.get(Form.MTN_USR_ETY_ATH) != null
								? Integer.parseInt(entrys.get(Form.MTN_USR_ETY_ATH).toString().trim())
								: 0;
							int pos = entrys.get(Form.MTN_USR_ETY_POS).toString().trim().length() != 0
									&& entrys.get(Form.MTN_USR_ETY_POS) != null
								? Integer.parseInt(entrys.get(Form.MTN_USR_ETY_POS).toString().trim())
								: 0;
							int report_no = entrys.get(Form.MTN_REPORT_CODE).toString().trim().length() != 0
									&& entrys.get(Form.MTN_REPORT_CODE) != null
								? Integer.parseInt(entrys.get(Form.MTN_REPORT_CODE).toString().trim())
								: 0;

							String enableMenu = entrys.get(Form.MTN_ENABLE_MENU).toString().trim().length() != 0
									&& entrys.get(Form.MTN_ENABLE_MENU) != null
								? entrys.get(Form.MTN_ENABLE_MENU).toString().trim()
								: "0";

							// 選択ﾎﾞﾀﾝ押下時値保持
							if (entrys.get(Form.MTN_USR_ETY_IDX) != null && Integer.parseInt((String) entrys.get(Form.MTN_USR_ETY_IDX)) >= 0) {
								int idx = Integer.parseInt((String) entrys.get(Form.MTN_USR_ETY_IDX));
								selection = (MaintenanceUserReport) menuList.get(idx);
								menu = new MaintenanceUserReport(group, pos, report_no, selection.getIndex(), enableMenu);
							} else {
								menu = new MaintenanceUserReport(group, pos, report_no, enableMenu);
							}
							System.out.println(enableMenu);

							selection = menu;
							eraflg = false;

						} else {
							/* エラーなし */
							MaintenanceUserReport menu = null;
							int idx = 0;
							int upidx = 0;
							if (entrys.get(Form.MTN_USR_ETY_IDX) != null && Integer.parseInt((String) entrys.get(Form.MTN_USR_ETY_IDX)) >= 0) {
								// 登録済みデータ更新

								idx = Integer.parseInt((String) entrys.get(Form.MTN_USR_ETY_IDX));
								menu = (MaintenanceUserReport) menuList.get(idx);

								// 上書き登録
								upidx = updList.size();
								updList.add(upidx, new MaintenanceUserReport(
										menu.getGroup(),
										menu.getPos(),
										menu.getReport_no(),
										menu.getEnableMenu(),
										"update",
										idx));

								menu.setGroup(Integer.parseInt(entrys.get(Form.MTN_USR_ETY_ATH).toString().trim()));
								menu.setPos(Integer.parseInt(entrys.get(Form.MTN_USR_ETY_POS).toString().trim()));
								menu.setReport_no(Integer.parseInt(entrys.get(Form.MTN_REPORT_CODE).toString().trim()));
								menu.setEnableMenu(entrys.get(Form.MTN_ENABLE_MENU).toString().trim());

								menu.setUpd();
								// 登録データ反映
								menuList.set(idx, menu);

							} else {

								// 新規登録データ
								idx = menuList.size();

								int group = Integer.parseInt(entrys.get(Form.MTN_USR_ETY_ATH).toString().trim());
								int pos = Integer.parseInt(entrys.get(Form.MTN_USR_ETY_POS).toString().trim());
								int report_no = Integer.parseInt(entrys.get(Form.MTN_REPORT_CODE).toString().trim());
								String enableMenu = entrys.get(Form.MTN_ENABLE_MENU).toString().trim().length() != 0
										&& entrys.get(Form.MTN_ENABLE_MENU) != null
									? entrys.get(Form.MTN_ENABLE_MENU).toString().trim()
									: "0";

								menu = new MaintenanceUserReport(group, pos, report_no, idx, enableMenu);
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
						entrys.put(Form.MTN_USR_ETY_ATH, esc.htmlEscape(request.getParameter(Form.MTN_USR_ETY_ATH).toString()));
						entrys.put(Form.MTN_USR_ETY_POS, esc.htmlEscape(request.getParameter(Form.MTN_USR_ETY_POS).toString()));
						entrys.put(Form.MTN_REPORT_CODE, esc.htmlEscape(request.getParameter(Form.MTN_REPORT_CODE).toString()));
						entrys.put(Form.MTN_USR_ETY_IDX, esc.htmlEscape(request.getParameter(Form.MTN_USR_ETY_IDX).toString()));

						/* チェック */
						if (vld.delete(entrys, menuList) == false) {
							/* エラーメッセージの設定 */
							request.setAttribute(Form.COMMON_MSGTYPE, Form.COMMON_MSGTYPE_ERR);
							request.setAttribute(Form.COMMON_MSG, vld.OutMsgShort());
							int group = entrys.get(Form.MTN_USR_ETY_ATH).toString().trim().length() != 0
									&& entrys.get(Form.MTN_USR_ETY_ATH) != null
								? Integer.parseInt(entrys.get(Form.MTN_USR_ETY_ATH).toString().trim())
								: 0;
							int pos = entrys.get(Form.MTN_USR_ETY_POS)
									.toString().trim().length() != 0
									&& entrys.get(Form.MTN_USR_ETY_POS) != null ? Integer
									.parseInt(entrys.get(Form.MTN_USR_ETY_POS)
											.toString().trim())
									: 0;
							int report_no = entrys.get(Form.MTN_REPORT_CODE)
									.toString().trim().length() != 0
									&& entrys.get(Form.MTN_REPORT_CODE) != null ? Integer
									.parseInt(entrys.get(Form.MTN_REPORT_CODE)
											.toString().trim())
									: 0;
							MaintenanceUserReport menu = null;
							// 選択ﾎﾞﾀﾝ押下時値保持
							if (entrys.get(Form.MTN_USR_ETY_IDX) != null
									&& Integer.parseInt((String) entrys
											.get(Form.MTN_USR_ETY_IDX)) >= 0) {
								int idx = Integer.parseInt((String) entrys
										.get(Form.MTN_USR_ETY_IDX));
								selection = (MaintenanceUserReport) menuList
										.get(idx);
								menu = new MaintenanceUserReport(group, pos, report_no, selection.getIndex());
							} else {
								menu = new MaintenanceUserReport(group, pos, report_no);
							}
							selection = menu;

							eraflg = false;
						} else {
							/* エラーなし */

							// 登録済みデータ更新
							int idx = 0;
							idx = Integer.parseInt((String) entrys
									.get(Form.MTN_USR_ETY_IDX));

							MaintenanceUserReport usr = (MaintenanceUserReport) menuList
									.get(idx);
							usr.setDel();

							// 削除データ反映
							menuList.set(idx, usr);
							// リスト設定
							session.setAttribute(Consts.STR_SES_GRD, menuList);
						}

					}
					if (eraflg != false && event.equals(Form.MTN_RSTBTN) == false) {
						// else if (event.equals(Form.MTN_SAVBTN)) {
						/** 保存ボタン */

						/* チェック */
						if (vld.save(menuList) == false) {
							/* エラーメッセージの設定 */
							request.setAttribute(Form.COMMON_MSGTYPE, Form.COMMON_MSGTYPE_ERR);
							request.setAttribute(Form.COMMON_MSG, vld.OutMsgShort());
						} else {
							/* エラーなし */
							User loginusr = (User) session.getAttribute(Consts.STR_SES_LOGINUSER);
							String msg = null;
							msg = setUserRecord(menuList, loginusr, updList, msg);

							// 再設定
							menuList = getReportRecord();
							ArrayList posList = getPosRecord();
							ArrayList athList = getAthRecord();
							ArrayList reportList = getReportNameRecord();

							// リスト設定
							session.setAttribute(Consts.STR_SES_GRD, menuList);
							session.setAttribute(Consts.STR_SES_GRD2, posList);
							session.setAttribute(Consts.STR_SES_GRD3, athList);
							session.setAttribute(Consts.STR_SES_GRD4, reportList);

							request.setAttribute(Form.COMMON_MSGTYPE, Form.COMMON_MSGTYPE_SUP);
							request.setAttribute(Form.COMMON_MSG, msg);
						}

					}

				} else if (request.getParameter(Form.MTN_SELAREA) != null) {
					/**
					 * 選択エリア処理
					 */
					int idx = Integer.parseInt((String) request.getParameter(Form.MTN_SELAREA));
					selection = (MaintenanceUserReport) menuList.get(idx);
				}
			}

			request.setAttribute(Consts.STR_REQ_REC, selection);

			strPage = config.getInitParameter("MaintenanceUserReport");

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
	 * ロール情報取得 <br /> DBより権限情報を全て取得し、ArrayList形式で戻す。<br />
	 *
	 * @return 取得したArrayList形式Userデータ <br />
	 * @throws Exception
	 *             例外 <br />
	 */
	protected ArrayList getPosRecord() throws Exception {
		List list = null;
		try {
			QueryRunner qr = new QueryRunner(DBConnection.getDataSource());
			ResultSetHandler rsh = new MapListHandler();
			list = (List) qr.query(SQL.POS_SEL_001, rsh);

		} catch (Exception en) {
			System.out
					.println("DBUtils NullPointerException:MaintenanceUser_getPosRecord()");
		}
		Map mu = null;
		ArrayList beanList = null;
		if (list != null) {
			beanList = new ArrayList();
			for (int i = 0; i < list.size(); i++) {
				mu = (Map) list.get(i);
				beanList.add(i, new Position(Integer.parseInt(mu.get(
						"cd_position").toString().trim()), mu
						.get("nm_position").toString().trim()));
			}
		}
		return beanList;
	}

	/**
	 * グループ情報取得 <br /> DBより権限情報を全て取得し、ArrayList形式で戻す。<br />
	 *
	 * @return 取得したArrayList形式Userデータ <br />
	 * @throws Exception
	 *             例外 <br />
	 */
	protected ArrayList getAthRecord() throws Exception {
		List list = null;
		try {
			QueryRunner qr = new QueryRunner(DBConnection.getDataSource());
			ResultSetHandler rsh = new MapListHandler();
			list = (List) qr.query(SQL.ATH_SEL_001, rsh);
		} catch (Exception en) {
			System.out
					.println("DBUtils NullPointerException:MaintenanceMenu_getAthRecord()");
		}
		Map mu = null;
		ArrayList beanList = null;
		if (list != null) {
			beanList = new ArrayList();
			for (int i = 0; i < list.size(); i++) {
				mu = (Map) list.get(i);
				beanList.add(i, new Auth(Integer.parseInt(mu.get("cd_group")
						.toString().trim()), mu.get("nm_group").toString()
						.trim()));
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
	protected ArrayList getReportRecord() throws Exception {
		List list = null;
		try {
			QueryRunner qr = new QueryRunner(DBConnection.getDataSource());
			ResultSetHandler rsh = new MapListHandler();
			list = (List) qr.query(SQL.REPORT_SEL_003, rsh);
		} catch (Exception en) {
			System.out.println("DBUtils NullPointerException:MaintenanceUser_getUserRecord()");
		}
		Map mu = null;
		ArrayList beanList = null;
		if (list != null) {
			beanList = new ArrayList();
			for (int i = 0; i < list.size(); i++) {
				mu = (Map) list.get(i);
				beanList.add(i, new MaintenanceUserReport(
						Integer.parseInt(mu.get("cd_group").toString().trim()),
						Integer.parseInt(mu.get("cd_position").toString().trim()),
						Integer.parseInt(mu.get("cd_report_no").toString().trim()),
						i,
						mu.get("ENABLE_MENU").toString().trim()	));
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
			System.out
					.println("DBUtils NullPointerException:MaintenanceUser_getReportNameRecord()");
		}

		Map mu = null;
		ArrayList beanList = null;
		if (list != null) {
			beanList = new ArrayList();
			for (int i = 0; i < list.size(); i++) {
				mu = (Map) list.get(i);
				beanList.add(i, new Report(Integer.parseInt(mu.get(
						"cd_report_no").toString().trim()), mu.get("nm_report")
						.toString().trim()));
			}
		}

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
				MaintenanceUserReport report = (MaintenanceUserReport) list
						.get(i);
				if (report.getState() == MaintenanceUser.STATE_UPD) {
					/**
					 * UPDATE or INSERTを行うデータ
					 */
					save = Consts.SQL_MESSAGE_IN;
					/**
					 * UPDATE
					 */
					int cnt = 0;

					for (int j = 0; j < upsize; j++) {
						MaintenanceUserReport updlist_date = (MaintenanceUserReport) updlist
								.get(j);
						if (updlist_date.getIndex() == i) {
							cnt = qr
									.update(
											con,
											SQL.REPORT_UPD_001,
											new Object[] {
													new Integer(report.getGroup()),
													new Integer(report.getPos()),
													new Integer(report.getReport_no()),
													report.getEnableMenu(),
													new Integer(updlist_date.getGroup()),
													new Integer(updlist_date.getPos()),
													new Integer(updlist_date.getReport_no())
												});
							System.out.println("UPDATE pkey db[sys_menu]:" + report.getGroup() + " COUNT:" + cnt);
						}
					}

					// 1件もUPDATEできなかった場合はINSERTを行う
					if (cnt == 0) {
						/**
						 * INSERT
						 */
						cnt = qr.update(con, SQL.REPORT_INS_001, new Object[] {
								new Integer(report.getGroup()),
								new Integer(report.getPos()),
								new Integer(report.getReport_no()),
								report.getEnableMenu(),
								loginusr.getId(), loginusr.getId() });
						System.out.println("INSERT pkey db[sys_menu]:"
								+ report.getGroup() + " COUNT:" + cnt);

					}

				} else if (report.getState() == MaintenanceUser.STATE_DEL) {
					/**
					 * DELETE を行うデータ
					 */
					save = Consts.SQL_MESSAGE_DELETE;
					int cnt = qr.update(con, SQL.REPORT_DEL_001, new Object[] {
							new Integer(report.getGroup()),
							new Integer(report.getPos()),
							new Integer(report.getReport_no()) });
					System.out.println("DELETE pkey db[sys_menu]:"
							+ report.getGroup() + " COUNT:" + cnt);
				}
			}
			DbUtils.commitAndCloseQuietly(con);

			msg = save + Message.SUP_SAVE_END;
			/**
			 * 例外処理
			 */
		} catch (SQLException e) {
			try {
				DbUtils.rollback(con);
			} catch (SQLException e2) {
			}
			DbUtils.closeQuietly(con);
			msg = save + Message.ERR_SAVE_END;
		}
		return msg;
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
