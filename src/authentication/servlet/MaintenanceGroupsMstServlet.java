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

public class MaintenanceGroupsMstServlet extends HttpServlet {
	/**
	 *
	 */
	private static final long serialVersionUID = 1533490597998418089L;
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

			MaintenanceGroupsMst selection = new MaintenanceGroupsMst();

			/** タイトル取得 */
			if (session.getAttribute(Consts.STR_SES_TITLE) == null) {
				String report_title = request.getParameter(Form.REPORT_NAME) != null ? request.getParameter(Form.REPORT_NAME).toString().trim() : null;
				String name = getReport_title(report_title);
				session.setAttribute(Consts.STR_SES_TITLE, name);
			}

			/** 権限List読込みチェック */
			if (session.getAttribute(Consts.STR_SES_GRD) == null) {
				/** 取得していない場合 */
				ArrayList report_detaList = getGroupMstRecord();
				// リスト設定
				session.setAttribute(Consts.STR_SES_GRD, report_detaList);

			} else {

				/** 取得済みの場合 */
				// 入力文字エスケープ
				Tagescape esc = new Tagescape();

				/** Listデータ読込 */
				ArrayList menuList = (ArrayList) session.getAttribute(Consts.STR_SES_GRD);

				// エラー判定のフラグ
				boolean eraflg = true;

				/** * 処理エリア判別 */
				if (request.getParameter(Form.MTN_INPAREA) != null) {

					/**
					 * 入力エリア処理
					 */
					// イベント取得
					String event = request.getParameter(Form.MTN_INPAREA).toString();

					MaintenanceGroupsMstValidation vld = new MaintenanceGroupsMstValidation();

					if (event.equals(Form.MTN_RSTBTN)) {
						/** リセットボタン */
						// 初期化済の為、処理なし

					} else if (event.equals(Form.MTN_ENTBTN)) {
						/** 登録ボタン */

						/** 入力値取得 */
						HashMap entrys = new HashMap();
						entrys.put(Form.MTN_USR_ETY_ATH_NAME, esc.htmlEscape(request.getParameter(Form.MTN_USR_ETY_ATH_NAME).toString()));
						entrys.put(Form.MTN_USR_ETY_IDX, esc.htmlEscape(request.getParameter(Form.MTN_USR_ETY_IDX).toString()));
						entrys.put(Form.MTN_CUSTOM_VALUE, esc.htmlEscape(request.getParameter(Form.MTN_CUSTOM_VALUE).toString().trim()));

						/* チェック */
						if (vld.entry(entrys, menuList) == false) {
							/* エラーメッセージの設定 */
							request.setAttribute(Form.COMMON_MSGTYPE, Form.COMMON_MSGTYPE_ERR);
							request.setAttribute(Form.COMMON_MSG, vld.OutMsgShort());
							MaintenanceGroupsMst menu = null;

							String group_name = (String) entrys.get(Form.MTN_USR_ETY_ATH_NAME);
							String custom_value = (String) entrys.get(Form.MTN_CUSTOM_VALUE);

							/** 選択ﾎﾞﾀﾝ押下時にエラーが起こった場合・選択ﾚｺｰﾄﾞの行番号を保持する */
							if (entrys.get(Form.MTN_USR_ETY_IDX) != null
									&& Integer.parseInt((String) entrys.get(Form.MTN_USR_ETY_IDX)) >= 0) {
								int idx = Integer.parseInt((String) entrys.get(Form.MTN_USR_ETY_IDX));
								menu = (MaintenanceGroupsMst) menuList.get(idx);
								menu = new MaintenanceGroupsMst(group_name, custom_value, menu.getIndex());

							} else {
								menu = new MaintenanceGroupsMst(group_name, custom_value);
							}
							selection = menu;
							eraflg = false;

						} else {

							/* エラーなし */
							MaintenanceGroupsMst menu = null;
							int idx = 0;
							if (entrys.get(Form.MTN_USR_ETY_IDX) != null && Integer.parseInt((String) entrys.get(Form.MTN_USR_ETY_IDX)) >= 0) {

								// 登録済みデータ更新
								idx = Integer.parseInt((String) entrys.get(Form.MTN_USR_ETY_IDX));
								menu = (MaintenanceGroupsMst) menuList.get(idx);

								// 上書き登録
								menu.setGroupName((String) entrys.get(Form.MTN_USR_ETY_ATH_NAME));
								menu.setCustom_value((String) entrys.get(Form.MTN_CUSTOM_VALUE));
								menu.setUpd();

								// 登録データ反映
								menuList.set(idx, menu);

							} else {

								// 新規登録データ
								idx = menuList.size();
								int group = getSequence();
								String group_name = (String) entrys.get(Form.MTN_USR_ETY_ATH_NAME);
								String custom_value = (String) entrys.get(Form.MTN_CUSTOM_VALUE);

								menu = new MaintenanceGroupsMst(group, group_name, custom_value, idx);
								menu.setUpd();
								// 登録データ追加
								menuList.add(idx, menu);
							}
							// リスト設定
							session.setAttribute(Consts.STR_SES_GRD, menuList);
						}
					} else if (event.equals(Form.MTN_DELBTN)) {
						/** 削除ボタン */
						/** 入力値取得 */
						HashMap entrys = new HashMap();
						entrys.put(Form.MTN_USR_ETY_ATH_NAME, esc
								.htmlEscape(request.getParameter(
										Form.MTN_USR_ETY_ATH_NAME).toString()));
						entrys
								.put(Form.MTN_USR_ETY_IDX, esc
										.htmlEscape(request.getParameter(
												Form.MTN_USR_ETY_IDX)
												.toString()));
						entrys.put(Form.MTN_CUSTOM_VALUE, esc
								.htmlEscape(request.getParameter(
										Form.MTN_CUSTOM_VALUE).toString()
										.trim()));

						/* チェック */
						if (vld.delete(entrys, menuList) == false) {
							/* エラーメッセージの設定 */
							request.setAttribute(Form.COMMON_MSGTYPE,
									Form.COMMON_MSGTYPE_ERR);
							request.setAttribute(Form.COMMON_MSG, vld
									.OutMsgShort());
							MaintenanceGroupsMst menu = null;

							String group_name = (String) entrys
									.get(Form.MTN_USR_ETY_ATH_NAME);
							String custom_value = (String) entrys
									.get(Form.MTN_CUSTOM_VALUE);

							menu = new MaintenanceGroupsMst(group_name,
									custom_value);

							/** 選択ﾎﾞﾀﾝ押下時にエラーが起こった場合・選択ﾚｺｰﾄﾞの行番号を保持する */
							if (entrys.get(Form.MTN_USR_ETY_IDX) != null
									&& Integer.parseInt((String) entrys
											.get(Form.MTN_USR_ETY_IDX)) >= 0) {
								int idx = Integer.parseInt((String) entrys
										.get(Form.MTN_USR_ETY_IDX));
								menu = (MaintenanceGroupsMst) menuList.get(idx);
								menu = new MaintenanceGroupsMst(group_name,
										custom_value, menu.getIndex());
							} else {
								menu = new MaintenanceGroupsMst(group_name,
										custom_value);
							}

							selection = menu;

							eraflg = false;
						} else {
							/* エラーなし */

							// 登録済みデータ更新
							int idx = 0;
							idx = Integer.parseInt((String) entrys
									.get(Form.MTN_USR_ETY_IDX));
							MaintenanceGroupsMst usr = (MaintenanceGroupsMst) menuList
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
							msg = setUserRecord(menuList, loginusr, msg);

							// 再設定
							menuList = getGroupMstRecord();
							ArrayList athList = getGroupRecord();

							// リスト設定
							session.setAttribute(Consts.STR_SES_GRD, menuList);
							session.setAttribute(Consts.STR_SES_GRD2, athList);

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
					selection = (MaintenanceGroupsMst) menuList.get(idx);
				}
			}
			request.setAttribute(Consts.STR_REQ_REC, selection);
			strPage = config.getInitParameter("MaintenanceGroupsMst");
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
	 * グループ情報取得 <br /> DBより権限情報を全て取得し、ArrayList形式で戻す。<br />
	 *
	 * @return 取得したArrayList形式Userデータ <br />
	 * @throws Exception
	 *             例外 <br />
	 */
	protected ArrayList getGroupRecord() throws Exception {
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
	protected ArrayList getGroupMstRecord() throws Exception {
		List list = null;
		try {
			QueryRunner qr = new QueryRunner(DBConnection.getDataSource());
			ResultSetHandler rsh = new MapListHandler();
			list = (List) qr.query(SQL.ATH_SEL_003, rsh);
		} catch (Exception en) {
			System.out
					.println("DBUtils NullPointerException:MaintenanceUser_getGroupMstRecord()");
		}
		Map mu = null;
		ArrayList beanList = null;
		if (list != null) {
			beanList = new ArrayList();
			for (int i = 0; i < list.size(); i++) {
				mu = (Map) list.get(i);
				beanList.add(i, new MaintenanceGroupsMst(Integer.parseInt(mu
						.get("cd_group").toString().trim()), mu.get("nm_group")
						.toString().trim(), mu.get("custom_value").toString()
						.trim(), i));
			}
		}
		return beanList;
	}

	/**
	 * ユーザー情報設定 <br /> 画面にて変更を行ったUserListをDBへ反映させる。 <br />
	 *
	 * @param list
	 *            ArrayList形式Userデータ <br />
	 * @param loginusr
	 *            ログインユーザ情報 <br />
	 * @throws SQLException
	 *             例外 <br />
	 */
	protected String setUserRecord(ArrayList list, User loginusr, String msg)
			throws SQLException {
		Connection con = null; // コネクション

		String save = "";
		try {
			/* コネクションの取得 */

			QueryRunner qr = new QueryRunner(DBConnection.getDataSource());
			con = qr.getDataSource().getConnection();
			con.setAutoCommit(false);

			/* List内データの内、Upd,Delのみ更新対象とする */
			int size = list.size();
			for (int i = 0; i < size; i++) {
				MaintenanceGroupsMst group = (MaintenanceGroupsMst) list.get(i);
				if (group.getState() == MaintenanceGroupsMst.STATE_UPD) {
					/**
					 * UPDATE or INSERTを行うデータ
					 */

					/**
					 * UPDATE
					 */
					int cnt = 0;
					save = Consts.SQL_MESSAGE_IN;
					cnt = qr
							.update(con, SQL.ATH_GROUP_UPD_001, new Object[] {
									new Integer(group.getGroup()),
									group.getGroupName(),
									group.getCustom_value(),
									loginusr.getName(),
									new Integer(group.getGroup()) });
					System.out.println("UPDATE pkey db[SYS_GROUPS]:"
							+ group.getGroup() + " COUNT:" + cnt);

					// 1件もUPDATEできなかった場合はINSERTを行う
					if (cnt == 0) {
						/**
						 * INSERT
						 */
						cnt = qr
								.update(con, SQL.ATH_GROUP_INS_001,
										new Object[] {
												new Integer(group.getGroup()),
												group.getGroupName(),
												group.getCustom_value(),
												loginusr.getName(),
												loginusr.getName() });
						System.out.println("INSERT pkey db[SYS_GROUPS]:"
								+ group.getGroup() + " COUNT:" + cnt);
					}

				} else if (group.getState() == MaintenanceGroupsMst.STATE_DEL) {
					/**
					 * DELETE を行うデータ
					 */
					save = Consts.SQL_MESSAGE_DELETE;
					int cnt = qr.update(con, SQL.ATH_GROUP_DEL_002,
							new Object[] { new Integer(group.getGroup()) });
					System.out.println("DELETE pkey db[SYS_USER_GROUP]:"
							+ group.getGroup() + " COUNT:" + cnt);

					cnt = qr.update(con, SQL.ATH_GROUP_DEL_003,
							new Object[] { new Integer(group.getGroup()) });
					System.out.println("DELETE pkey db[SYS_MENU]:"
							+ group.getGroup() + " COUNT:" + cnt);

					cnt = qr.update(con, SQL.ATH_GROUP_DEL_004,
							new Object[] { new Integer(group.getGroup()) });
					System.out.println("DELETE pkey db[M_INFO_AUTH]:"
							+ group.getGroup() + " COUNT:" + cnt);

					cnt = qr.update(con, SQL.ATH_GROUP_DEL_001,
							new Object[] { new Integer(group.getGroup()) });
					System.out.println("DELETE pkey db[SYS_GROUPS]:"
							+ group.getGroup() + " COUNT:" + cnt);
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

	/**
	 * シーケンス番号を取得する
	 *
	 * @return
	 * @throws Exception
	 */
	protected int getSequence() throws Exception {
		int intsequence = 0;
		try {
			QueryRunner qr = new QueryRunner(DBConnection.getDataSource());
			ResultSetHandler rsh = new MapHandler();
			Map sequence = null;
			sequence = (Map) qr.query(SQL.SEQUENCE_001, rsh);
			intsequence = Integer.parseInt(sequence.get("1").toString().trim());
			// System.out.println(intsequence);
		} catch (Exception en) {
			System.out
					.println("DBUtils NullPointerException:MaintenanceGroupMstServlet:::getSys_menuRecode()");
		}
		return intsequence;
	}

}
