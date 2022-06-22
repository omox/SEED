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

public class MaintenancePosmstServlet extends HttpServlet {
	/**
	 *
	 */
	private static final long serialVersionUID = -2314524888220405847L;
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
			MaintenancePosmst selection = new MaintenancePosmst();
			/** タイトル取得 */
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
				ArrayList report_detaList = getPosmstRecod();
				// リスト設定
				session.setAttribute(Consts.STR_SES_GRD, report_detaList);
			} else {
				/** 取得済みの場合 */
				// 入力文字エスケープ
				Tagescape esc = new Tagescape();

				/** Listデータ読込 */
				ArrayList posList = (ArrayList) session
						.getAttribute(Consts.STR_SES_GRD);
				/** * 処理エリア判別 */
				if (request.getParameter(Form.MTN_INPAREA) != null) {
					/**
					 * 入力エリア処理
					 */
					// イベント取得
					String event = request.getParameter(Form.MTN_INPAREA)
							.toString();

					MaintenancePosmstValidation vld = new MaintenancePosmstValidation();
					boolean eraflg = true;
					if (event.equals(Form.MTN_RSTBTN)) {
						/** リセットボタン */
						// 初期化済の為、処理なし
					} else if (event.equals(Form.MTN_ENTBTN)) {
						/** 登録ボタン */
						/** 入力値取得 */
						HashMap entrys = new HashMap();
						entrys.put(Form.MTN_USR_ETY_POS_NAME, esc
								.htmlEscape(request.getParameter(
										Form.MTN_USR_ETY_POS_NAME).toString()
										.trim()));
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
						if (vld.entry(entrys, posList) == false) {
							/* エラーメッセージの設定 */
							request.setAttribute(Form.COMMON_MSGTYPE,
									Form.COMMON_MSGTYPE_ERR);
							request.setAttribute(Form.COMMON_MSG, vld
									.OutMsgShort());
							String pos_name = (String) entrys
									.get(Form.MTN_USR_ETY_POS_NAME);
							String custom_value = (String) entrys
									.get(Form.MTN_CUSTOM_VALUE);

							MaintenancePosmst posmst = null;

							// 選択ﾎﾞﾀﾝの行番号を解除させない
							if (entrys.get(Form.MTN_USR_ETY_IDX) != null
									&& Integer.parseInt((String) entrys
											.get(Form.MTN_USR_ETY_IDX)) >= 0) {
								int idx = Integer.parseInt((String) entrys
										.get(Form.MTN_USR_ETY_IDX));
								posmst = (MaintenancePosmst) posList.get(idx);
								posmst = new MaintenancePosmst(pos_name,
										custom_value, posmst.getIndex());
							} else {
								posmst = new MaintenancePosmst(pos_name,
										custom_value);
							}
							selection = posmst;

							eraflg = false;
						} else {
							/* エラーなし */
							MaintenancePosmst posmst = null;
							int idx = 0;
							if (entrys.get(Form.MTN_USR_ETY_IDX) != null
									&& Integer.parseInt((String) entrys
											.get(Form.MTN_USR_ETY_IDX)) >= 0) {
								// 登録済みデータ更新
								idx = Integer.parseInt((String) entrys
										.get(Form.MTN_USR_ETY_IDX));

								posmst = (MaintenancePosmst) posList.get(idx);
								// 上書き登録
								posmst.setPositionName((String) entrys
										.get(Form.MTN_USR_ETY_POS_NAME));
								posmst.setCustom_vlaue((String) entrys
										.get(Form.MTN_CUSTOM_VALUE));
								posmst.setUpd();
								// 登録データ反映
								posList.set(idx, posmst);
							} else {
								// 新規登録データ
								idx = posList.size();

								int position = getSequence();
								String pos_name = (String) entrys
										.get(Form.MTN_USR_ETY_POS_NAME);
								String custom_value = (String) entrys
										.get(Form.MTN_CUSTOM_VALUE);

								posmst = new MaintenancePosmst(position,
										pos_name, custom_value, idx);
								posmst.setUpd();
								// 登録データ追加
								posList.add(idx, posmst);
							}
							// リスト設定
							session.setAttribute(Consts.STR_SES_GRD, posList);
						}
					} else if (event.equals(Form.MTN_DELBTN)) {
						/** 削除ボタン */
						/** 入力値取得 */
						HashMap entrys = new HashMap();
						entrys.put(Form.MTN_USR_ETY_POS_NAME, esc
								.htmlEscape(request.getParameter(
										Form.MTN_USR_ETY_POS_NAME).toString()
										.trim()));
						entrys.put(Form.MTN_USR_ETY_IDX, esc.htmlEscape(request
								.getParameter(Form.MTN_USR_ETY_IDX).toString()
								.trim()));
						/* チェック */
						if (vld.delete(entrys, posList) == false) {
							/* エラーメッセージの設定 */
							request.setAttribute(Form.COMMON_MSGTYPE,
									Form.COMMON_MSGTYPE_ERR);
							request.setAttribute(Form.COMMON_MSG, vld
									.OutMsgShort());

							String pos_name = (String) entrys
									.get(Form.MTN_USR_ETY_POS_NAME);
							String custom_value = (String) entrys
									.get(Form.MTN_CUSTOM_VALUE);

							MaintenancePosmst posmst = new MaintenancePosmst(
									pos_name, custom_value);
							// 選択ﾎﾞﾀﾝの行番号を解除させない
							if (entrys.get(Form.MTN_USR_ETY_IDX) != null
									&& Integer.parseInt((String) entrys
											.get(Form.MTN_USR_ETY_IDX)) >= 0) {
								int idx = Integer.parseInt((String) entrys
										.get(Form.MTN_USR_ETY_IDX));
								posmst = (MaintenancePosmst) posList.get(idx);
								posmst = new MaintenancePosmst(pos_name,
										custom_value, posmst.getIndex());
							} else {
								posmst = new MaintenancePosmst(pos_name,
										custom_value);
							}

							selection = posmst;

							eraflg = false;
						} else {
							/* エラーなし */

							// 登録済みデータ更新
							int idx = 0;
							idx = Integer.parseInt((String) entrys
									.get(Form.MTN_USR_ETY_IDX));

							MaintenancePosmst usr = (MaintenancePosmst) posList
									.get(idx);
							usr.setDel();
							// 削除データ反映
							posList.set(idx, usr);
							// リスト設定
							session.setAttribute(Consts.STR_SES_GRD, posList);
						}
					}
					if (eraflg != false
							&& event.equals(Form.MTN_RSTBTN) == false) {
						// else if (event.equals(Form.MTN_SAVBTN)) {
						/** 保存ボタン */

						/* チェック */
						if (vld.save(posList) == false) {
							/* エラーメッセージの設定 */
							request.setAttribute(Form.COMMON_MSGTYPE,
									Form.COMMON_MSGTYPE_ERR);
							request.setAttribute(Form.COMMON_MSG, vld
									.OutMsgShort());
							// 再設定
							posList = getPosmstRecod();
							// リスト設定
							session.setAttribute(Consts.STR_SES_GRD, posList);
						} else {
							/* エラーなし */
							User loginusr = (User) session
									.getAttribute(Consts.STR_SES_LOGINUSER);
							String msg = null;
							msg = setUserRecord(posList, loginusr, msg);

							// 再設定
							posList = getPosmstRecod();

							// リスト設定
							session.setAttribute(Consts.STR_SES_GRD, posList);

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
					selection = (MaintenancePosmst) posList.get(idx);
				}
			}

			request.setAttribute(Consts.STR_REQ_REC, selection);
			strPage = config.getInitParameter("MaintenancePosmst");
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
	 * ユーザー情報取得 <br />レポートコード DBよりユーザー情報を全て取得し、ArrayList形式で戻す。<br />
	 *
	 * @return 取得したArrayList形式Userデータ <br />
	 * @throws Exception
	 *             例外 <br />
	 */
	protected ArrayList getPosmstRecod() throws Exception {
		List list = null;
		try {
			QueryRunner qr = new QueryRunner(DBConnection.getDataSource());
			ResultSetHandler rsh = new MapListHandler();
			list = (List) qr.query(SQL.POSMST_SEL_001, rsh);
		} catch (Exception en) {
			System.out
					.println("DBUtils NullPointerException:MaintenancegetPosmstRecod()");
		}
		Map mu = null;
		ArrayList beanList = null;

		if (list != null) {
			beanList = new ArrayList();
			for (int i = 0; i < list.size(); i++) {
				mu = (Map) list.get(i);
				beanList.add(i, new MaintenancePosmst(Integer.parseInt(mu.get(
						"CD_POSITION").toString().trim()), mu
						.get("NM_POSITION").toString().trim(), mu.get(
						"CUSTOM_VALUE").toString().trim(), i));
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
		String save = null;// 保存･削除メッセージ
		try {
			/* コネクションの取得 */

			QueryRunner qr = new QueryRunner(DBConnection.getDataSource());
			con = qr.getDataSource().getConnection();
			con.setAutoCommit(false);

			/* List内データの内、Upd,Delのみ更新対象とする */
			int size = list.size();
			for (int i = 0; i < size; i++) {
				MaintenancePosmst posmst = (MaintenancePosmst) list.get(i);
				if (posmst.getState() == MaintenancePosmst.STATE_UPD) {
					/**
					 * UPDATE or INSERTを行うデータ
					 */

					/**
					 * UPDATE
					 */
					save = Consts.SQL_MESSAGE_IN;
					int cnt = 0;
					cnt = qr.update(con, SQL.POSMST_UPD_001, new Object[] {
							new Integer(posmst.getPosition()),
							posmst.getPositionName(), posmst.getCustom_value(),
							loginusr.getName(),
							new Integer(posmst.getPosition()) });
					System.out.println("UPDATE pkey db[SYS_POSMST]:"
							+ posmst.getPosition() + " COUNT:" + cnt);

					// 1件もUPDATEできなかった場合はINSERTを行う
					if (cnt == 0) {
						/**
						 * INSERT
						 */
						cnt = qr.update(con, SQL.POSMST_INS_001, new Object[] {
								new Integer(posmst.getPosition()),
								posmst.getPositionName(),
								posmst.getCustom_value(), loginusr.getName(),
								loginusr.getName() });
						System.out.println("INSERT pkey db[SYS_POSMST]:"
								+ posmst.getPosition() + " COUNT:" + cnt);
					}

				} else if (posmst.getState() == MaintenanceGroupsMst.STATE_DEL) {
					/**
					 * DELETE を行うデータ
					 */
					save = Consts.SQL_MESSAGE_DELETE;
					int cnt = qr.update(con, SQL.POSMST_DEL_002,
							new Object[] { new Integer(posmst.getPosition()) });
					System.out.println("DELETE pkey db[SYS_USER_POS]:"
							+ posmst.getPosition() + " COUNT:" + cnt);

					cnt = qr.update(con, SQL.POSMST_DEL_003,
							new Object[] { new Integer(posmst.getPosition()) });
					System.out.println("DELETE pkey db[SYS_MENU]:"
							+ posmst.getPosition() + " COUNT:" + cnt);

					cnt = qr.update(con, SQL.POSMST_DEL_004,
							new Object[] { new Integer(posmst.getPosition()) });
					System.out.println("DELETE pkey db[M_INFO_AUTH]:"
							+ posmst.getPosition() + " COUNT:" + cnt);

					cnt = qr.update(con, SQL.POSMST_DEL_001,
							new Object[] { new Integer(posmst.getPosition()) });
					System.out.println("DELETE pkey db[SYS_POSMST]:"
							+ posmst.getPosition() + " COUNT:" + cnt);
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
			System.out.println(intsequence);
		} catch (Exception en) {
			System.out
					.println("DBUtils NullPointerException:MaintenanceGroupMstServlet:::getSys_menuRecode()");
		}
		return intsequence;
	}

}
