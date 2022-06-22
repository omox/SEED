package authentication.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;

import authentication.bean.MaintenanceInfo;
import authentication.bean.User;
import authentication.connection.DBConnection;
import authentication.defines.Consts;
import authentication.defines.Form;
import authentication.defines.Message;
import authentication.defines.SQL;
import authentication.util.Tagescape;
import authentication.validation.MaintenanceInfoValidation;

public class MaintenanceInfoServlet extends HttpServlet {
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

			MaintenanceInfo selection = new MaintenanceInfo();

			/** タイトル取得 */
			if (session.getAttribute(Consts.STR_SES_TITLE) == null) {
				String report_title = request.getParameter(Form.REPORT_NAME) != null ?
						request.getParameter(Form.REPORT_NAME).toString().trim()
						: null;
				String name = getReport_title(report_title);
				session.setAttribute(Consts.STR_SES_TITLE, name);
			}

			/** お知らせList読込みチェック */
			if (session.getAttribute(Consts.STR_SES_GRD) == null) {
				/** 取得していない場合 */
				ArrayList infoList = getInfoMstRecord();
				// リスト設定
				session.setAttribute(Consts.STR_SES_GRD, infoList);

			} else {
				/** 取得済みの場合 */
				// 入力文字エスケープ
				Tagescape esc = new Tagescape();

				/** Listデータ読込 */
				ArrayList infoList = (ArrayList) session.getAttribute(Consts.STR_SES_GRD);

				// エラー判定のフラグ
				boolean eraflg = true;

				/** * 処理エリア判別 */
				if (request.getParameter(Form.MTN_INPAREA) != null) {

					/**
					 * 入力エリア処理
					 */
					// イベント取得
					String event = request.getParameter(Form.MTN_INPAREA).toString();

					MaintenanceInfoValidation vld = new MaintenanceInfoValidation();

					if (event.equals(Form.MTN_RSTBTN)) {
						/** リセットボタン */
						// 初期化済の為、処理なし

					} else if (event.equals(Form.MTN_ENTBTN)) {
						/** 登録ボタン */

						/** 入力値取得 */
						HashMap entrys = new HashMap();
						entrys.put(Form.MTN_USR_ETY_IDX, esc.htmlEscape(request.getParameter(Form.MTN_USR_ETY_IDX).toString().trim()));
						entrys.put(Form.MTN_INFO_ETY_DT_START, esc.htmlEscape(request.getParameter(Form.MTN_INFO_ETY_DT_START).toString().trim()));
						entrys.put(Form.MTN_INFO_ETY_DT_END, esc.htmlEscape(request.getParameter(Form.MTN_INFO_ETY_DT_END).toString().trim()));
						entrys.put(Form.MTN_INFO_ETY_NO_DISP, esc.htmlEscape(request.getParameter(Form.MTN_INFO_ETY_NO_DISP).toString().trim()));

						// チェックボックス
						entrys.put(Form.MTN_INFO_ETY_FLG_ALWAYS,
								Consts.INFO_ALWAYS.equals(request.getParameter(Form.MTN_INFO_ETY_FLG_ALWAYS)) ?
									Consts.INFO_ALWAYS
									: "0" );

						// 入力文字エスケープなし
						entrys.put(Form.MTN_INFO_ETY_TITLE, request.getParameter(Form.MTN_INFO_ETY_TITLE).toString().trim());
						entrys.put(Form.MTN_INFO_ETY_INFO, request.getParameter(Form.MTN_INFO_ETY_INFO).toString().trim());

						/* チェック */
						if (vld.entry(entrys, infoList) == false) {
							/* エラーメッセージの設定 */
							request.setAttribute(Form.COMMON_MSGTYPE, Form.COMMON_MSGTYPE_ERR);
							request.setAttribute(Form.COMMON_MSG, vld.OutMsgShort());
							MaintenanceInfo info = null;

							String dt_start = (String) entrys.get(Form.MTN_INFO_ETY_DT_START);
							String dt_end = (String) entrys.get(Form.MTN_INFO_ETY_DT_END);
							String flg_disp_always = (String) entrys.get(Form.MTN_INFO_ETY_FLG_ALWAYS);
							String no_disp = (String) entrys.get(Form.MTN_INFO_ETY_NO_DISP);

							// 入力文字エスケープ
							String title = esc.htmlEscape((String) entrys.get(Form.MTN_INFO_ETY_TITLE));
							String information = esc.htmlEscape((String) entrys.get(Form.MTN_INFO_ETY_INFO));

							/** 選択ﾎﾞﾀﾝ押下時にエラーが起こった場合・選択ﾚｺｰﾄﾞの行番号を保持する */
							if (entrys.get(Form.MTN_USR_ETY_IDX) != null
									&& Integer.parseInt((String) entrys.get(Form.MTN_USR_ETY_IDX)) >= 0) {
								int idx = Integer.parseInt((String) entrys.get(Form.MTN_USR_ETY_IDX));
								info = (MaintenanceInfo) infoList.get(idx);
								info = new MaintenanceInfo(dt_start, dt_end,  flg_disp_always, no_disp, title, information, info.getIndex());

							} else {
								info = new MaintenanceInfo(dt_start, dt_end,  flg_disp_always, no_disp, title, information);
							}
							selection = info;
							eraflg = false;

						} else {

							/* エラーなし */
							MaintenanceInfo info = null;
							int idx = 0;
							if (entrys.get(Form.MTN_USR_ETY_IDX) != null && Integer.parseInt((String) entrys.get(Form.MTN_USR_ETY_IDX)) >= 0) {

								// 登録済みデータ更新
								idx = Integer.parseInt((String) entrys.get(Form.MTN_USR_ETY_IDX));
								info = (MaintenanceInfo) infoList.get(idx);

								// 上書き登録
								info.setDt_start((String) entrys.get(Form.MTN_INFO_ETY_DT_START));
								info.setDt_end((String) entrys.get(Form.MTN_INFO_ETY_DT_END));
								info.setFlg_disp_always((String) entrys.get(Form.MTN_INFO_ETY_FLG_ALWAYS));
								info.setNo_disp((String) entrys.get(Form.MTN_INFO_ETY_NO_DISP));
								info.setTitle((String) entrys.get(Form.MTN_INFO_ETY_TITLE));
								info.setInformation((String) entrys.get(Form.MTN_INFO_ETY_INFO));
								info.setUpd();

								// 登録データ反映
								infoList.set(idx, info);

							} else {

								// 新規登録データ
								idx = infoList.size();
								int seq = getSequence();
								String dt_start = (String) entrys.get(Form.MTN_INFO_ETY_DT_START);
								String dt_end = (String) entrys.get(Form.MTN_INFO_ETY_DT_END);
								String flg_disp_always = (String) entrys.get(Form.MTN_INFO_ETY_FLG_ALWAYS);
								String no_disp = (String) entrys.get(Form.MTN_INFO_ETY_NO_DISP);
								String title = (String) entrys.get(Form.MTN_INFO_ETY_TITLE);
								String information = (String) entrys.get(Form.MTN_INFO_ETY_INFO);

								info = new MaintenanceInfo(seq, dt_start, dt_end,  flg_disp_always, no_disp, title, information, idx);
								info.setUpd();
								// 登録データ追加
								infoList.add(idx, info);
							}
							// リスト設定
							session.setAttribute(Consts.STR_SES_GRD, infoList);
						}

					} else if (event.equals(Form.MTN_DELBTN)) {
						/** 削除ボタン */
						/** 入力値取得 */
						HashMap entrys = new HashMap();
						entrys.put(Form.MTN_USR_ETY_IDX, esc.htmlEscape(request.getParameter(Form.MTN_USR_ETY_IDX).toString().trim()));
						entrys.put(Form.MTN_INFO_ETY_DT_START, esc.htmlEscape(request.getParameter(Form.MTN_INFO_ETY_DT_START).toString().trim()));
						entrys.put(Form.MTN_INFO_ETY_DT_END, esc.htmlEscape(request.getParameter(Form.MTN_INFO_ETY_DT_END).toString().trim()));
						entrys.put(Form.MTN_INFO_ETY_NO_DISP, esc.htmlEscape(request.getParameter(Form.MTN_INFO_ETY_NO_DISP).toString().trim()));

						// チェックボックス
						entrys.put(Form.MTN_INFO_ETY_FLG_ALWAYS,
								Consts.INFO_ALWAYS.equals(request.getParameter(Form.MTN_INFO_ETY_FLG_ALWAYS)) ?
									Consts.INFO_ALWAYS
									: "0" );

						// 入力文字エスケープなし
						entrys.put(Form.MTN_INFO_ETY_TITLE, request.getParameter(Form.MTN_INFO_ETY_TITLE).toString().trim());
						entrys.put(Form.MTN_INFO_ETY_INFO, request.getParameter(Form.MTN_INFO_ETY_INFO).toString().trim());

						/* チェック */
						if (vld.delete(entrys, infoList) == false) {
							/* エラーメッセージの設定 */
							request.setAttribute(Form.COMMON_MSGTYPE, Form.COMMON_MSGTYPE_ERR);
							request.setAttribute(Form.COMMON_MSG, vld.OutMsgShort());
							MaintenanceInfo info = null;

							String dt_start = (String) entrys.get(Form.MTN_INFO_ETY_DT_START);
							String dt_end = (String) entrys.get(Form.MTN_INFO_ETY_DT_END);
							String flg_disp_always = (String) entrys.get(Form.MTN_INFO_ETY_FLG_ALWAYS);
							String no_disp = (String) entrys.get(Form.MTN_INFO_ETY_NO_DISP);

							// 入力文字エスケープ
							String title = esc.htmlEscape((String) entrys.get(Form.MTN_INFO_ETY_TITLE));
							String information = esc.htmlEscape((String) entrys.get(Form.MTN_INFO_ETY_INFO));

							/** 選択ﾎﾞﾀﾝ押下時にエラーが起こった場合・選択ﾚｺｰﾄﾞの行番号を保持する */
							if (entrys.get(Form.MTN_USR_ETY_IDX) != null
									&& Integer.parseInt((String) entrys.get(Form.MTN_USR_ETY_IDX)) >= 0) {
								int idx = Integer.parseInt((String) entrys.get(Form.MTN_USR_ETY_IDX));
								info = (MaintenanceInfo) infoList.get(idx);
								info = new MaintenanceInfo(dt_start, dt_end,  flg_disp_always, no_disp, title, information, info.getIndex());

							} else {
								info = new MaintenanceInfo(dt_start, dt_end,  flg_disp_always, no_disp, title, information);
							}

							selection = info;
							eraflg = false;

						} else {
							/* エラーなし */

							// 登録済みデータ更新
							int idx = 0;
							idx = Integer.parseInt((String) entrys.get(Form.MTN_USR_ETY_IDX));
							MaintenanceInfo usr = (MaintenanceInfo) infoList.get(idx);
							usr.setDel();

							// 削除データ反映
							infoList.set(idx, usr);
							// リスト設定
							session.setAttribute(Consts.STR_SES_GRD, infoList);
						}
					}
					if (event.equals(Form.MTN_BTN_CLEAN)) {
						/** 不要データ削除ボタン  */

						String msg = null;
						msg = setInfoMstClean(msg);

						// 再設定
						infoList = getInfoMstRecord();

						// リスト設定
						session.setAttribute(Consts.STR_SES_GRD, infoList);

						request.setAttribute(Form.COMMON_MSGTYPE, Form.COMMON_MSGTYPE_SUP);
						request.setAttribute(Form.COMMON_MSG, msg);

					} else if (eraflg != false
							&& event.equals(Form.MTN_RSTBTN) == false) {
						// else if (event.equals(Form.MTN_SAVBTN)) {
						/** 保存ボタン */

						/* チェック */
						if (vld.save(infoList) == false) {
							/* エラーメッセージの設定 */
							request.setAttribute(Form.COMMON_MSGTYPE, Form.COMMON_MSGTYPE_ERR);
							request.setAttribute(Form.COMMON_MSG, vld.OutMsgShort());

						} else {
							/* エラーなし */
							User loginusr = (User) session.getAttribute(Consts.STR_SES_LOGINUSER);
							String msg = null;
							msg = setInfoMstRecord(infoList, loginusr, msg);

							// 再設定
							infoList = getInfoMstRecord();

							// リスト設定
							session.setAttribute(Consts.STR_SES_GRD, infoList);

							request.setAttribute(Form.COMMON_MSGTYPE, Form.COMMON_MSGTYPE_SUP);
							request.setAttribute(Form.COMMON_MSG, msg);
						}
					}

				} else if (request.getParameter(Form.MTN_SELAREA) != null) {
					/**
					 * 選択エリア処理
					 */
					int idx = Integer.parseInt((String) request .getParameter(Form.MTN_SELAREA));
					selection = (MaintenanceInfo) infoList.get(idx);
				}
			}

			request.setAttribute(Consts.STR_REQ_REC, selection);

			strPage = config.getInitParameter("MaintenanceInfo");

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
		getServletContext().getRequestDispatcher(strPage).forward(request, response);
	}

	/**
	 * お知らせ情報取得 <br /> DBよりお知らせ情報を全て取得し、ArrayList形式で戻す。<br />
	 *
	 * @return 取得したArrayList形式Userデータ <br />
	 * @throws Exception
	 *             例外 <br />
	 */
	protected ArrayList getInfoMstRecord() throws Exception {
		List list = null;
		try {
			QueryRunner qr = new QueryRunner(DBConnection.getDataSource());
			ResultSetHandler rsh = new MapListHandler();
			list = (List) qr.query(SQL.INFO_SEL_001, rsh);
		} catch (Exception en) {
			System.out
					.println("DBUtils NullPointerException:MaintenanceInfo_getInfoMstRecord()");
		}
		Map mu = null;
		ArrayList beanList = null;
		if (list != null) {
			// 入力文字エスケープ
			Tagescape esc = new Tagescape();
			beanList = new ArrayList();
			for (int i = 0; i < list.size(); i++) {
				mu = (Map) list.get(i);
				beanList.add(i, new MaintenanceInfo(
						Integer.parseInt(mu.get("cd_info").toString().trim()),
						mu.get("dt_start").toString().trim(),
						mu.get("dt_end").toString().trim(),
						mu.get("flg_disp_always").toString().trim(),
						mu.get("no_disp").toString().trim(),
						esc.htmlEscape(mu.get("title").toString().trim()),
						esc.htmlEscape(mu.get("information").toString().trim()),
						i));
			}
		}
		return beanList;
	}

	/**
	 * お知らせ情報設定 <br /> 画面にて変更を行ったinfoListをDBへ反映させる。 <br />
	 *
	 * @param list
	 *            ArrayList形式Userデータ <br />
	 * @param loginusr
	 *            ログインユーザ情報 <br />
	 * @throws SQLException
	 *             例外 <br />
	 */
	protected String setInfoMstRecord(ArrayList list, User loginusr, String msg)
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
				MaintenanceInfo info = (MaintenanceInfo) list.get(i);
				if (info.getState() == MaintenanceInfo.STATE_UPD) {
					/**
					 * UPDATE or INSERTを行うデータ
					 */

					/**
					 * UPDATE
					 */
					int cnt = 0;
					save = Consts.SQL_MESSAGE_IN;
					cnt = qr.update(con, SQL.INFO_UPD_001, new Object[] {
										info.getDt_start(),
										info.getDt_end(),
										info.getFlg_disp_always(),
										new Integer(info.getNo_disp()),
										info.getTitle(),
										info.getInformation(),
										loginusr.getId(),
										new Integer(info.getCd_info()) });
					System.out.println("UPDATE pkey db[M_INFO]:"
							+ info.getCd_info() + " COUNT:" + cnt);

					// 1件もUPDATEできなかった場合はINSERTを行う
					if (cnt == 0) {
						/**
						 * INSERT
						 */
						cnt = qr.update(con, SQL.INFO_INS_001,
										new Object[] {
											new Integer(info.getCd_info()),
											info.getDt_start(),
											info.getDt_end(),
											info.getFlg_disp_always(),
											new Integer(info.getNo_disp()),
											info.getTitle(),
											info.getInformation(),
											loginusr.getId(),
											loginusr.getId() });
						System.out.println("INSERT pkey db[M_INFO]:"
								+ info.getCd_info() + " COUNT:" + cnt);
					}

				} else if (info.getState() == MaintenanceInfo.STATE_DEL) {
					/**
					 * DELETE を行うデータ
					 */
					save = Consts.SQL_MESSAGE_DELETE;
					int cnt = qr.update(con, SQL.INFO_DEL_002,
							new Object[] { new Integer(info.getCd_info()) });
					System.out.println("DELETE pkey db[M_INFO_AUTH]:"
							+ info.getCd_info() + " COUNT:" + cnt);

					cnt = qr.update(con, SQL.INFO_DEL_001,
							new Object[] { new Integer(info.getCd_info()) });
					System.out.println("DELETE pkey db[M_INFO]:"
							+ info.getCd_info() + " COUNT:" + cnt);
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
			e.printStackTrace();
			msg = save + Message.ERR_SAVE_END;
		}
		return msg;
	}

	/**
	 * お知らせ情報設定 <br /> 画面にて変更を行ったinfoListをDBへ反映させる。 <br />
	 *
	 * @param list
	 *            ArrayList形式Userデータ <br />
	 * @param loginusr
	 *            ログインユーザ情報 <br />
	 * @throws SQLException
	 *             例外 <br />
	 */
	protected String setInfoMstClean(String msg)
			throws SQLException {
		Connection con = null; // コネクション

		String save = Consts.SQL_MESSAGE_DELETE;
		try {
			/* コネクションの取得 */

			QueryRunner qr = new QueryRunner(DBConnection.getDataSource());
			con = qr.getDataSource().getConnection();
			con.setAutoCommit(false);

			int cnt = qr.update(con, SQL.INFO_DEL_004);
			System.out.println("DELETE pkey db[M_INFO_AUTH]: COUNT:" + cnt);

			cnt = qr.update(con, SQL.INFO_DEL_003);
			System.out.println("DELETE pkey db[M_INFO]: COUNT:" + cnt);

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
			sequence = (Map) qr.query(SQL.SEQUENCE_002, rsh);
			intsequence = Integer.parseInt(sequence.get("1").toString().trim());
			// System.out.println(intsequence);
		} catch (Exception en) {
			System.out.println("DBUtils NullPointerException:MaintenanceInfo_getSequence()");
		}
		return intsequence;
	}

}
