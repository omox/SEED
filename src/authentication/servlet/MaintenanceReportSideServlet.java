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

public class MaintenanceReportSideServlet extends HttpServlet {
	/**
	 *
	 */
	private static final long serialVersionUID = 8576335553230649629L;
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
			MaintenanceReportSide selection = new MaintenanceReportSide();
			/** UserListデータ読込チェック */

			/** タイトル取得 */
			if (session.getAttribute(Consts.STR_SES_TITLE) == null) {
				String report_title = request.getParameter(Form.REPORT_NAME) != null ? request.getParameter(Form.REPORT_NAME).toString().trim()	: null;
				String name = getReport_title(report_title);
				session.setAttribute(Consts.STR_SES_TITLE, name);
			}

			if (session.getAttribute(Consts.STR_SES_GRD) == null) {
				/** 取得していない場合 */
				ArrayList side_detaList = getReportSideReport();
				// リスト設定
				session.setAttribute(Consts.STR_SES_GRD, side_detaList);

			} else {

				/** 取得済みの場合 */
				// 入力文字エスケープ
				Tagescape esc = new Tagescape();

				/** Listデータ読込 */
				ArrayList sideList = (ArrayList) session.getAttribute(Consts.STR_SES_GRD);

				/** * 処理エリア判別 */
				if (request.getParameter(Form.MTN_INPAREA) != null) {
					/**
					 * 入力エリア処理
					 */
					// イベント取得
					String event = request.getParameter(Form.MTN_INPAREA).toString();

					MaintenanceReportSideValidation vld = new MaintenanceReportSideValidation();
					boolean eraflg = true;

					if (event.equals(Form.MTN_RSTBTN)) {
						/** リセットボタン */
						// 初期化済の為、処理なし

					} else if (event.equals(Form.MTN_ENTBTN)) {
						/** 登録ボタン */
						/** 入力値取得 */
						HashMap entrys = new HashMap();

						entrys.put(Form.MTN_SIDE_CODE, esc.htmlEscape(request.getParameter(Form.MTN_SIDE_CODE).toString().trim()));
						entrys.put(Form.MTN_NM_REPORT, esc.htmlEscape(request.getParameter(Form.MTN_NM_REPORT).toString().trim()));
						entrys.put(Form.REPORT_DISP_SIDE, esc.htmlEscape(request.getParameter(Form.REPORT_DISP_SIDE).toString().trim()));
						entrys.put(Form.REPORT_DISP_ROW_SIDE, esc.htmlEscape(request.getParameter(Form.REPORT_DISP_ROW_SIDE).toString().trim()));
						entrys.put(Form.MTN_USR_ETY_IDX, esc.htmlEscape(request.getParameter(Form.MTN_USR_ETY_IDX).toString().trim()));
						entrys.put(Form.MTN_CUSTOM_VALUE, esc.htmlEscape(request.getParameter(Form.MTN_CUSTOM_VALUE).toString().trim()));

						/* チェック */
						if (vld.entry(entrys, sideList) == false) {
							/* エラーメッセージの設定 */
							request.setAttribute(Form.COMMON_MSGTYPE, Form.COMMON_MSGTYPE_ERR);
							request.setAttribute(Form.COMMON_MSG, vld.OutMsgShort());
							String side_name = (String) entrys.get(Form.MTN_NM_REPORT);
							String side_disp_no = (String) entrys.get(Form.REPORT_DISP_SIDE);
							String side_disp_row_no = (String) entrys.get(Form.REPORT_DISP_ROW_SIDE);
							String custom_value = (String) entrys.get(Form.MTN_CUSTOM_VALUE);

							MaintenanceReportSide sidereport = null;

							// 選択ﾎﾞﾀﾝ押下時のエラー時選択がはずれないようにするため
							if (entrys.get(Form.MTN_USR_ETY_IDX) != null
									&& Integer.parseInt((String) entrys.get(Form.MTN_USR_ETY_IDX)) >= 0) {
								int idx = Integer.parseInt((String) entrys.get(Form.MTN_USR_ETY_IDX));
								sidereport = (MaintenanceReportSide) sideList.get(idx);

								sidereport = new MaintenanceReportSide(
										sidereport.getSide(), side_name,
										side_disp_no, side_disp_row_no,
										custom_value, sidereport.getIndex());

							} else {
								sidereport = new MaintenanceReportSide(
										side_name, side_disp_no,
										side_disp_row_no, custom_value);

							}
							selection = sidereport;
							eraflg = false;

						} else {

							/* エラーなし */
							MaintenanceReportSide sidereport = null;
							int idx = 0;
							if (entrys.get(Form.MTN_USR_ETY_IDX) != null
									&& Integer.parseInt((String) entrys.get(Form.MTN_USR_ETY_IDX)) >= 0) {
								// 登録済みデータ更新 or 複製
								idx = Integer.parseInt((String) entrys.get(Form.MTN_USR_ETY_IDX));
								sidereport = (MaintenanceReportSide) sideList.get(idx);
								if (Integer.parseInt(entrys.get(
										Form.MTN_SIDE_CODE).toString().trim()) == sidereport.getSide()) {

									// 上書き登録
									sidereport.setSide(Integer.parseInt(entrys.get(Form.MTN_SIDE_CODE).toString().trim()));
									sidereport.setSidename((String) entrys.get(Form.MTN_NM_REPORT));
									sidereport.setDisp_number((String) entrys.get(Form.REPORT_DISP_SIDE));
									sidereport.setDisp_row_number((String) entrys.get(Form.REPORT_DISP_ROW_SIDE));
									sidereport.setCustom_value((String) entrys.get(Form.MTN_CUSTOM_VALUE));

									sidereport.setUpd();
									// 登録データ反映
									sideList.set(idx, sidereport);
								}
							}

							if (sidereport == null) {
								// 新規登録データ
								idx = sideList.size();

								int side = getSequence();
								String side_name = (String) entrys.get(Form.MTN_NM_REPORT);
								String side_disp_no = (String) entrys.get(Form.REPORT_DISP_SIDE);
								String side_disp_row_no = (String) entrys.get(Form.REPORT_DISP_ROW_SIDE);
								String custom_value = (String) entrys.get(Form.MTN_CUSTOM_VALUE);
								sidereport = new MaintenanceReportSide(side, side_name, side_disp_no,
										side_disp_row_no, custom_value, idx);
								sidereport.setUpd();

								// 登録データ追加
								sideList.add(idx, sidereport);
							}

							// リスト設定
							session.setAttribute(Consts.STR_SES_GRD, sideList);
						}

					} else if (event.equals(Form.MTN_DELBTN)) {
						/** 削除ボタン */
						/** 入力値取得 */
						HashMap entrys = new HashMap();
						entrys.put(Form.MTN_SIDE_CODE, esc.htmlEscape(request.getParameter(Form.MTN_SIDE_CODE).toString().trim()));
						entrys.put(Form.MTN_NM_REPORT, esc.htmlEscape(request.getParameter(Form.MTN_NM_REPORT).toString().trim()));
						entrys.put(Form.REPORT_DISP_SIDE, esc.htmlEscape(request.getParameter(Form.REPORT_DISP_SIDE).toString().trim()));
						entrys.put(Form.REPORT_DISP_ROW_SIDE, esc.htmlEscape(request.getParameter(Form.REPORT_DISP_ROW_SIDE).toString().trim()));
						entrys.put(Form.MTN_USR_ETY_IDX, esc.htmlEscape(request.getParameter(Form.MTN_USR_ETY_IDX).toString().trim()));
						entrys.put(Form.MTN_CUSTOM_VALUE, esc.htmlEscape(request.getParameter(Form.MTN_CUSTOM_VALUE).toString().trim()));

						/* チェック */
						if (vld.delete(entrys, sideList) == false) {
							/* エラーメッセージの設定 */
							request.setAttribute(Form.COMMON_MSGTYPE,
									Form.COMMON_MSGTYPE_ERR);
							request.setAttribute(Form.COMMON_MSG, vld
									.OutMsgShort());
							String side_name = (String) entrys
									.get(Form.MTN_NM_REPORT);
							String side_disp_no = (String) entrys
									.get(Form.REPORT_DISP_SIDE);
							String side_disp_row_no = (String) entrys
									.get(Form.REPORT_DISP_ROW_SIDE);
							String custom_value = (String) entrys
									.get(Form.MTN_CUSTOM_VALUE);
							MaintenanceReportSide sidereport = null;

							// 選択ﾎﾞﾀﾝ押下時のエラー時選択がはずれないようにするため
							if (entrys.get(Form.MTN_USR_ETY_IDX) != null
									&& Integer.parseInt((String) entrys
											.get(Form.MTN_USR_ETY_IDX)) >= 0) {
								int idx = Integer.parseInt((String) entrys
										.get(Form.MTN_USR_ETY_IDX));
								sidereport = (MaintenanceReportSide) sideList
										.get(idx);
								sidereport = new MaintenanceReportSide(
										sidereport.getSide(), side_name,
										side_disp_no, side_disp_row_no,
										custom_value, sidereport.getIndex());
							} else {
								sidereport = new MaintenanceReportSide(
										side_name, side_disp_no,
										side_disp_row_no, custom_value);
							}

							selection = sidereport;
							eraflg = false;
						} else {
							/* エラーなし */

							// 登録済みデータ更新
							int idx = 0;
							idx = Integer.parseInt((String) entrys
									.get(Form.MTN_USR_ETY_IDX));

							MaintenanceReportSide usr = (MaintenanceReportSide) sideList
									.get(idx);
							usr.setDel();

							// 削除データ反映
							sideList.set(idx, usr);
							// リスト設定
							session.setAttribute(Consts.STR_SES_GRD, sideList);

						}
					}
					if (eraflg != false
							&& event.equals(Form.MTN_RSTBTN) == false) {
						// else if (event.equals(Form.MTN_SAVBTN)) {
						/** 保存ボタン */

						/* チェック */
						if (vld.save(sideList) == false) {
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
							msg = setUserRecord(sideList, loginusr, msg);

							// 再設定
							sideList = getReportSideReport();

							// リスト設定
							session.setAttribute(Consts.STR_SES_GRD, sideList);

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
					selection = (MaintenanceReportSide) sideList.get(idx);
				}
			}
			request.setAttribute(Consts.STR_REQ_REC, selection);
			strPage = config.getInitParameter("MaintenanceReportSide");
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
	protected ArrayList getReportSideReport() throws Exception {
		List list = null;
		try {
			QueryRunner qr = new QueryRunner(DBConnection.getDataSource());
			ResultSetHandler rsh = new MapListHandler();
			list = (List) qr.query(SQL.MTN_SIDE_SEL_001, rsh);
		} catch (Exception en) {
			System.out
					.println("DBUtils NullPointerException:getReportSideReport()");
		}
		Map mu = null;
		ArrayList beanList = null;

		if (list != null) {
			beanList = new ArrayList();
			for (int i = 0; i < list.size(); i++) {
				mu = (Map) list.get(i);
				beanList.add(i, new MaintenanceReportSide(Integer.parseInt(mu
						.get("CD_REPORT_SIDE").toString().trim()), mu.get(
						"NM_REPORT_SIDE").toString().trim(), mu.get(
						"CD_DISP_NUMBER").toString().trim(), mu.get(
						"CD_DISP_COLUMN").toString().trim(), mu.get(
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
				MaintenanceReportSide side = (MaintenanceReportSide) list
						.get(i);
				if (side.getState() == MaintenanceReportSide.STATE_UPD) {
					/**
					 * UPDATE or INSERTを行うデータ
					 */
					save = Consts.SQL_MESSAGE_IN;
					/**
					 * UPDATE
					 */
					int cnt = 0;
					cnt = qr.update(con, SQL.SIDE_UPD_001, new Object[] {
							new Integer(side.getSide()), side.getSidename(),
							side.getDisp_number(), side.getDisp_Column(),
							side.getCustom_value(), loginusr.getName(),
							new Integer(side.getSide()) });
					System.out.println("UPDATE pkey db[SYS_REPORT_SIDE]:"
							+ side.getSide() + " COUNT:" + cnt);

					// 1件もUPDATEできなかった場合はINSERTを行う
					if (cnt == 0) {
						/**
						 * INSERT
						 */
						cnt = qr.update(con, SQL.SIDE_INS_001, new Object[] {
								new Integer(side.getSide()),
								side.getSidename(), side.getDisp_number(),
								side.getDisp_Column(),
								side.getCustom_value(), loginusr.getName(),
								loginusr.getName() });
						System.out.println("INSERT pkey db[SYS_REPORT_SIDE]:"
								+ side.getSide() + " COUNT:" + cnt);
					}

				} else if (side.getState() == MaintenanceReportSide.STATE_DEL) {
					/**
					 * DELETE を行うデータ
					 */
					save = Consts.SQL_MESSAGE_DELETE;
					int cnt = qr.update(con, SQL.SIDE_DEL_002,
							new Object[] { new Integer(side.getSide()) });
					System.out.println("DELETE pkey db[SYS_REPORT_SIDE]:"
							+ side.getSide() + " COUNT:" + cnt);

					cnt = qr.update(con, SQL.SIDE_DEL_001,
							new Object[] { new Integer(side.getSide()) });
					System.out.println("DELETE pkey db[SYS_REPORT_AUTH]:"
							+ side.getSide() + " COUNT:" + cnt);

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
	 *
	 *レポートマスタよりレポート名称取得 <br />
	 *
	 *
	 */
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
					.println("DBUtils NullPointerException:MaintenanceReportSideServlet:::getSys_menuRecode()");
		}
		return intsequence;
	}

}
