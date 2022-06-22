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

import authentication.bean.Auth;
import authentication.bean.Info;
import authentication.bean.MaintenanceInfoAuth;
import authentication.bean.Position;
import authentication.bean.User;
import authentication.connection.DBConnection;
import authentication.defines.Consts;
import authentication.defines.Form;
import authentication.defines.Message;
import authentication.defines.SQL;
import authentication.util.Tagescape;
import authentication.validation.MaintenanceInfoAuthValidation;

public class MaintenanceInfoAuthServlet extends HttpServlet {
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

			MaintenanceInfoAuth selection = new MaintenanceInfoAuth();

			/** タイトル取得 */
			if (session.getAttribute(Consts.STR_SES_TITLE) == null) {
				String report_title = request.getParameter(Form.REPORT_NAME) != null ?
						request.getParameter(Form.REPORT_NAME).toString().trim()
						: null;
				String name = getReport_title(report_title);
				session.setAttribute(Consts.STR_SES_TITLE, name);
			}

			/** 権限List読込みチェック */
			if (session.getAttribute(Consts.STR_SES_GRD2) == null) {
				/** 取得していない場合 */
				ArrayList posList = getPosRecord();
				ArrayList athList = getAthRecord();
				ArrayList infoList = getInfoRecord();

				// リスト設定
				session.setAttribute(Consts.STR_SES_GRD2, posList);
				session.setAttribute(Consts.STR_SES_GRD3, athList);
				session.setAttribute(Consts.STR_SES_GRD4, infoList);
			}
			/** お知らせ管理Listデータ読込チェック */
			if (session.getAttribute(Consts.STR_SES_GRD) == null) {
				/** 取得していない場合 */
				ArrayList infoAuthList = getInfoAuthRecord();
				// リスト設定
				session.setAttribute(Consts.STR_SES_GRD, infoAuthList);

				ArrayList updlist_set = updarraylist();
				session.setAttribute(Consts.UPD_DETA_LIST, updlist_set);

			} else {
				/** 取得済みの場合 */
				// 入力文字エスケープ
				Tagescape esc = new Tagescape();

				/** Listデータ読込 */
				ArrayList infoAuthList = (ArrayList) session.getAttribute(Consts.STR_SES_GRD);
				ArrayList updList = (ArrayList) session.getAttribute(Consts.UPD_DETA_LIST);

				/** * 処理エリア判別 */
				if (request.getParameter(Form.MTN_INPAREA) != null) {

					/**
					 * 入力エリア処理
					 */
					// イベント取得
					String event = request.getParameter(Form.MTN_INPAREA).toString();

					MaintenanceInfoAuthValidation vld = new MaintenanceInfoAuthValidation();

					// エラーフラグ。エラー時に保存処理をさせないために。
					boolean eraflg = true;

					if (event.equals(Form.MTN_RSTBTN)) {
						/** リセットボタン */
						// 初期化済の為、処理なし

					} else if (event.equals(Form.MTN_ENTBTN)) {
						/** 登録ボタン */

						/** 入力値取得 */
						HashMap entrys = new HashMap();
						entrys.
								put(Form.MTN_USR_ETY_ATH, esc
										.htmlEscape(request.getParameter(
												Form.MTN_USR_ETY_ATH)
												.toString()));
						entrys
								.put(Form.MTN_USR_ETY_POS, esc
										.htmlEscape(request.getParameter(
												Form.MTN_USR_ETY_POS)
												.toString()));
						entrys
								.put(Form.MTN_INFO_CODE, esc
										.htmlEscape(request.getParameter(
												Form.MTN_INFO_CODE)
												.toString()));
						entrys
								.put(Form.MTN_USR_ETY_IDX, esc
										.htmlEscape(request.getParameter(
												Form.MTN_USR_ETY_IDX)
												.toString()));
						/* チェック */
						if (vld.entry(entrys, infoAuthList) == false) {
							/* エラーメッセージの設定 */
							request.setAttribute(Form.COMMON_MSGTYPE,
									Form.COMMON_MSGTYPE_ERR);
							request.setAttribute(Form.COMMON_MSG, vld
									.OutMsgShort());

							MaintenanceInfoAuth infoAuth = null;

							int group = entrys.get(Form.MTN_USR_ETY_ATH)
									.toString().trim().length() != 0
									&& entrys.get(Form.MTN_USR_ETY_ATH) != null ? Integer
									.parseInt(entrys.get(Form.MTN_USR_ETY_ATH)
											.toString().trim())
									: 0;
							int pos = entrys.get(Form.MTN_USR_ETY_POS)
									.toString().trim().length() != 0
									&& entrys.get(Form.MTN_USR_ETY_POS) != null ? Integer
									.parseInt(entrys.get(Form.MTN_USR_ETY_POS)
											.toString().trim())
									: 0;
							int info = entrys.get(Form.MTN_INFO_CODE)
									.toString().trim().length() != 0
									&& entrys.get(Form.MTN_INFO_CODE) != null ? Integer
									.parseInt(entrys.get(Form.MTN_INFO_CODE)
											.toString().trim())
									: 0;
							// 選択ﾎﾞﾀﾝ押下時値保持
							if (entrys.get(Form.MTN_USR_ETY_IDX) != null
									&& Integer.parseInt((String) entrys
											.get(Form.MTN_USR_ETY_IDX)) >= 0) {
								int idx = Integer.parseInt((String) entrys
										.get(Form.MTN_USR_ETY_IDX));
								selection = (MaintenanceInfoAuth) infoAuthList
										.get(idx);
								infoAuth = new MaintenanceInfoAuth(group, pos,
										info, selection.getIndex());
							} else {
								infoAuth = new MaintenanceInfoAuth(group, pos,
										info);
							}

							selection = infoAuth;
							eraflg = false;

						} else {
							/* エラーなし */
							MaintenanceInfoAuth infoAuth = null;
							int idx = 0;
							int upidx = 0;
							if (entrys.get(Form.MTN_USR_ETY_IDX) != null
									&& Integer.parseInt((String) entrys
											.get(Form.MTN_USR_ETY_IDX)) >= 0) {
								// 登録済みデータ更新

								idx = Integer.parseInt((String) entrys
										.get(Form.MTN_USR_ETY_IDX));

								infoAuth = (MaintenanceInfoAuth) infoAuthList.get(idx);
								// 上書き登録
								upidx = updList.size();
								updList
										.add(upidx, new MaintenanceInfoAuth(
												infoAuth.getCd_group(), infoAuth.getCd_pos(),
												infoAuth.getCd_info(), idx));
								infoAuth.setCd_group(Integer
										.parseInt(entrys.get(
												Form.MTN_USR_ETY_ATH)
												.toString().trim()));
								infoAuth.setCd_pos(Integer
										.parseInt(entrys.get(
												Form.MTN_USR_ETY_POS)
												.toString().trim()));
								infoAuth.setCd_info(Integer
										.parseInt(entrys.get(
												Form.MTN_INFO_CODE)
												.toString().trim()));

								infoAuth.setUpd();
								// 登録データ反映
								infoAuthList.set(idx, infoAuth);

							} else {

								// 新規登録データ
								idx = infoAuthList.size();

								int group = Integer
										.parseInt(entrys.get(
												Form.MTN_USR_ETY_ATH)
												.toString().trim());
								int pos = Integer
										.parseInt(entrys.get(
												Form.MTN_USR_ETY_POS)
												.toString().trim());
								int info = Integer
										.parseInt(entrys.get(
												Form.MTN_INFO_CODE)
												.toString().trim());

								infoAuth = new MaintenanceInfoAuth(group, pos,
										info, idx);
								infoAuth.setUpd();
								// 登録データ追加
								infoAuthList.add(idx, infoAuth);

							}
							// リスト設定
							session.setAttribute(Consts.UPD_DETA_LIST, updList);
							session.setAttribute(Consts.STR_SES_GRD, infoAuthList);
						}
					} else if (event.equals(Form.MTN_DELBTN)) {
						/** 削除ボタン */

						/** 入力値取得 */
						HashMap entrys = new HashMap();
						entrys
								.put(Form.MTN_USR_ETY_ATH, esc
										.htmlEscape(request.getParameter(
												Form.MTN_USR_ETY_ATH)
												.toString()));
						entrys
								.put(Form.MTN_USR_ETY_POS, esc
										.htmlEscape(request.getParameter(
												Form.MTN_USR_ETY_POS)
												.toString()));
						entrys
								.put(Form.MTN_INFO_CODE, esc
										.htmlEscape(request.getParameter(
												Form.MTN_INFO_CODE)
												.toString()));
						entrys
								.put(Form.MTN_USR_ETY_IDX, esc
										.htmlEscape(request.getParameter(
												Form.MTN_USR_ETY_IDX)
												.toString()));

						/* チェック */
						if (vld.delete(entrys, infoAuthList) == false) {
							/* エラーメッセージの設定 */
							request.setAttribute(Form.COMMON_MSGTYPE,
									Form.COMMON_MSGTYPE_ERR);
							request.setAttribute(Form.COMMON_MSG, vld
									.OutMsgShort());
							int group = entrys.get(Form.MTN_USR_ETY_ATH)
									.toString().trim().length() != 0
									&& entrys.get(Form.MTN_USR_ETY_ATH) != null ? Integer
									.parseInt(entrys.get(Form.MTN_USR_ETY_ATH)
											.toString().trim())
									: 0;
							int pos = entrys.get(Form.MTN_USR_ETY_POS)
									.toString().trim().length() != 0
									&& entrys.get(Form.MTN_USR_ETY_POS) != null ? Integer
									.parseInt(entrys.get(Form.MTN_USR_ETY_POS)
											.toString().trim())
									: 0;
							int report_no = entrys.get(Form.MTN_INFO_CODE)
									.toString().trim().length() != 0
									&& entrys.get(Form.MTN_INFO_CODE) != null ? Integer
									.parseInt(entrys.get(Form.MTN_INFO_CODE)
											.toString().trim())
									: 0;
									MaintenanceInfoAuth infoAuth = null;
							// 選択ﾎﾞﾀﾝ押下時値保持
							if (entrys.get(Form.MTN_USR_ETY_IDX) != null
									&& Integer.parseInt((String) entrys
											.get(Form.MTN_USR_ETY_IDX)) >= 0) {
								int idx = Integer.parseInt((String) entrys
										.get(Form.MTN_USR_ETY_IDX));
								selection = (MaintenanceInfoAuth) infoAuthList
										.get(idx);
								infoAuth = new MaintenanceInfoAuth(group, pos,
										report_no, selection.getIndex());
							} else {
								infoAuth = new MaintenanceInfoAuth(group, pos,
										report_no);
							}
							selection = infoAuth;

							eraflg = false;
						} else {
							/* エラーなし */

							// 登録済みデータ更新
							int idx = 0;
							idx = Integer.parseInt((String) entrys
									.get(Form.MTN_USR_ETY_IDX));

							MaintenanceInfoAuth usr = (MaintenanceInfoAuth) infoAuthList
									.get(idx);
							usr.setDel();

							// 削除データ反映
							infoAuthList.set(idx, usr);
							// リスト設定
							session.setAttribute(Consts.STR_SES_GRD, infoAuthList);
						}

					}
					if (eraflg != false
							&& event.equals(Form.MTN_RSTBTN) == false) {
						// else if (event.equals(Form.MTN_SAVBTN)) {
						/** 保存ボタン */

						/* チェック */
						if (vld.save(infoAuthList) == false) {
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
							msg = setUserRecord(infoAuthList, loginusr, updList,
									msg);

							// 再設定
							infoAuthList = getInfoAuthRecord();
							ArrayList posList = getPosRecord();
							ArrayList athList = getAthRecord();
							ArrayList infoList = getInfoRecord();

							// リスト設定
							session.setAttribute(Consts.STR_SES_GRD, infoAuthList);
							session.setAttribute(Consts.STR_SES_GRD2, posList);
							session.setAttribute(Consts.STR_SES_GRD3, athList);
							session.setAttribute(Consts.STR_SES_GRD4, infoList);

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
					selection = (MaintenanceInfoAuth) infoAuthList.get(idx);
				}
			}

			request.setAttribute(Consts.STR_REQ_REC, selection);

			strPage = config.getInitParameter("MaintenanceInfoAuth");

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
	 * ロール情報取得 <br /> DBよりロール情報を全て取得し、ArrayList形式で戻す。<br />
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
					.println("DBUtils NullPointerException:MaintenanceInfoAuth_getPosRecord()");
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
			beanList.add(new Position(Consts.INFO_FREE_CD, Consts.INFO_FREE_NM));
		}
		return beanList;
	}

	/**
	 * グループ情報取得 <br /> DBよりグループ情報を全て取得し、ArrayList形式で戻す。<br />
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
					.println("DBUtils NullPointerException:MaintenanceInfoAuth_getAthRecord()");
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
			beanList.add(new Auth(Consts.INFO_FREE_CD, Consts.INFO_FREE_NM));
		}
		return beanList;
	}

	/**
	 * お知らせ情報取得 <br /> DBよりお知らせ情報を全て取得し、ArrayList形式で戻す。<br />
	 *
	 * @return 取得したArrayList形式Userデータ <br />
	 * @throws Exception
	 *             例外 <br />
	 */
	protected ArrayList getInfoRecord() throws Exception {
		List list = null;
		try {
			QueryRunner qr = new QueryRunner(DBConnection.getDataSource());
			ResultSetHandler rsh = new MapListHandler();
			list = (List) qr.query(SQL.INFO_SEL_001, rsh);
		} catch (Exception en) {
			System.out
					.println("DBUtils NullPointerException:MaintenanceInfoAuth_getInfoRecord()");
		}
		Map mu = null;
		ArrayList beanList = null;
		if (list != null) {
			// 入力文字エスケープ
			Tagescape esc = new Tagescape();
			beanList = new ArrayList();
			for (int i = 0; i < list.size(); i++) {
				mu = (Map) list.get(i);
				beanList.add(i, new Info(
						Integer.parseInt(mu.get("cd_info").toString().trim()),
						mu.get("dt_start").toString().trim() + " "
						+ esc.htmlEscape(mu.get("title").toString().trim())));
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
	 * お知らせ管理マスタ <br /> DBよりお知らせ管理情報を全て取得し、ArrayList形式で戻す。<br />
	 *
	 * @return 取得したArrayList形式Userデータ <br />
	 * @throws Exception
	 *             例外 <br />
	 */
	protected ArrayList getInfoAuthRecord() throws Exception {
		List list = null;
		try {
			QueryRunner qr = new QueryRunner(DBConnection.getDataSource());
			ResultSetHandler rsh = new MapListHandler();
			list = (List) qr.query(SQL.ATH_INFO_SEL_001, rsh);
		} catch (Exception en) {
			System.out
					.println("DBUtils NullPointerException:MaintenanceInfoAuth_getInfoAuthRecord()");
		}
		Map mu = null;
		ArrayList beanList = null;
		if (list != null) {
			beanList = new ArrayList();
			for (int i = 0; i < list.size(); i++) {
				mu = (Map) list.get(i);
				beanList.add(i, new MaintenanceInfoAuth(
						Integer.parseInt(mu.get("cd_group").toString().trim()),
						Integer.parseInt(mu.get("cd_position").toString().trim()),
						Integer.parseInt(mu.get("cd_info").toString().trim()),
						i));
			}
		}
		return beanList;
	}

	/**
	 * おしらせ管理に設定 <br /> 画面にて変更を行ったUserListをDBへ反映させる。 <br />
	 *
	 * @param list
	 *            ArrayList形式Userデータ <br />
	 * @param loginusr
	 *            ログインユーザ情報 <br />
	 * @throws SQLException
	 *             例外 <br />
	 */
	protected String setUserRecord(ArrayList list, User loginusr, ArrayList updlist, String msg) throws SQLException {

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
				MaintenanceInfoAuth infoAuth = (MaintenanceInfoAuth) list
						.get(i);
				if (infoAuth.getState() == MaintenanceInfoAuth.STATE_UPD) {
					/**
					 * UPDATE or INSERTを行うデータ
					 */
					save = Consts.SQL_MESSAGE_IN;
					/**
					 * UPDATE
					 */
					int cnt = 0;

					for (int j = 0; j < upsize; j++) {
						MaintenanceInfoAuth updlist_date = (MaintenanceInfoAuth) updlist
								.get(j);
						if (updlist_date.getIndex() == i) {
							cnt = qr
									.update(
											con,
											SQL.ATH_INFO_UPD_001,
											new Object[] {
													new Integer(infoAuth.getCd_group()),
													new Integer(infoAuth.getCd_pos()),
													new Integer(infoAuth.getCd_info()),
													loginusr.getId(),
													new Integer(updlist_date.getCd_group()),
													new Integer(updlist_date.getCd_pos()),
													new Integer(updlist_date.getCd_info()) });
							System.out.println("UPDATE pkey db[M_INFO_AUTH]:"
									+ infoAuth.getCd_group() + " COUNT:" + cnt);
						}
					}

					// 1件もUPDATEできなかった場合はINSERTを行う
					if (cnt == 0) {
						/**
						 * INSERT
						 */
						cnt = qr.update(con, SQL.ATH_INFO_INS_001, new Object[] {
								new Integer(infoAuth.getCd_group()),
								new Integer(infoAuth.getCd_pos()),
								new Integer(infoAuth.getCd_info()),
								loginusr.getId(),
								loginusr.getId() });
						System.out.println("INSERT pkey db[M_INFO_AUTH]:"
								+ infoAuth.getCd_group() + " COUNT:" + cnt);

					}

				} else if (infoAuth.getState() == MaintenanceInfoAuth.STATE_DEL) {
					/**
					 * DELETE を行うデータ
					 */
					save = Consts.SQL_MESSAGE_DELETE;
					int cnt = qr.update(con, SQL.ATH_INFO_DEL_001, new Object[] {
							new Integer(infoAuth.getCd_group()),
							new Integer(infoAuth.getCd_pos()),
							new Integer(infoAuth.getCd_info()) });
					System.out.println("DELETE pkey db[M_INFO_AUTH]:"
							+ infoAuth.getCd_group() + " COUNT:" + cnt);
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
