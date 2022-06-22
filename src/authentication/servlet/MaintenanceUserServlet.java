/*
 * 作成日: 2007/11/08
 *
 */
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

/**
 * ユーザーメンテナンス画面処理制御クラス
 * ユーザーメンテナンス画面の処理を制御する。
 */
public class MaintenanceUserServlet extends HttpServlet {
	/**
	 *
	 */
	private static final long serialVersionUID = 1686961626733058293L;
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
	 * リクエスト時処理 <br />
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		HttpSession session = request.getSession();
		String strPage = null;
		try {
			MaintenanceUser selection = new MaintenanceUser();

			/** 権限List読込みチェック */
			if (session.getAttribute(Consts.STR_SES_GRD2) == null) {
				/** 取得していない場合 */
				ArrayList posList = getPosRecord();
				ArrayList athList = getAthRecord();
				// リスト設定
				session.setAttribute(Consts.STR_SES_GRD2, posList);
				session.setAttribute(Consts.STR_SES_GRD3, athList);
				/** 表のグループに使用 */
				ArrayList groupList = getGroupRecord();
				ArrayList posList_user = getPosRecord_user();
				session.setAttribute(Consts.STR_SES_GRD5, groupList);
				session.setAttribute(Consts.STR_SES_GRD6, posList_user);
			}

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
				ArrayList userList = getUserRecord();
				// リスト設定
				session.setAttribute(Consts.STR_SES_GRD, userList);

			} else {
				/** 取得済みの場合 */
				// 入力文字エスケープ
				Tagescape esc = new Tagescape();

				/** Listデータ読込 */
				ArrayList userList = (ArrayList) session
						.getAttribute(Consts.STR_SES_GRD);

				/** * 処理エリア判別 */
				if (request.getParameter(Form.MTN_INPAREA) != null) {
					/**
					 * 入力エリア処理
					 */
					// イベント取得
					String event = request.getParameter(Form.MTN_INPAREA)
							.toString();

					MaintenanceUserValidation vld = new MaintenanceUserValidation();
					boolean eraflg = true;
					if (event.equals(Form.MTN_RSTBTN)) {
						/** リセットボタン */
						// 初期化済の為、処理なし
					} else if (event.equals(Form.MTN_ENTBTN)) {
						/** 登録ボタン */
						/** 入力値取得 */

						HashMap entrys = new HashMap();
						entrys.put(Form.MTN_USR_ETY_CD, esc.htmlEscape(request.getParameter(Form.MTN_USR_ETY_CD).toString().trim()));
						// ユーザーID
						entrys.put(Form.MTN_USR_ETY_UID, esc.htmlEscape(request.getParameter(Form.MTN_USR_ETY_UID).toString().trim()));
						// パスワード
						entrys.put(Form.MTN_USR_ETY_PAS, esc.htmlEscape(request.getParameter(Form.MTN_USR_ETY_PAS).toString().trim()));
						// ロールコード 配列をHashMpに入れた
						String[] selected_pos = request.getParameterValues(Form.MTN_USR_ETY_POS);
						entrys.put(Form.MTN_USR_ETY_POS, selected_pos);

						// グループコード　配列をHashMpに入れた
						String[] selected_group = request.getParameterValues(Form.MTN_USR_ETY_ATH);
						entrys.put(Form.MTN_USR_ETY_ATH, selected_group);

						// 姓
						entrys.put(Form.MTN_USR_ETY_NM_FAMILY, esc.htmlEscape(request.getParameter(Form.MTN_USR_ETY_NM_FAMILY).toString().trim()));
						// 名
						entrys.put(Form.MTN_USR_ETY_NM_NAME, esc.htmlEscape(request.getParameter(Form.MTN_USR_ETY_NM_NAME).toString().trim()));
						entrys.put(Form.MTN_USR_ETY_IDX, esc.htmlEscape(request.getParameter(Form.MTN_USR_ETY_IDX).toString().trim()));

						/* チェック */
						if (vld.entry(entrys, userList) == false) {
							/* エラーメッセージの設定 */
							request.setAttribute(Form.COMMON_MSGTYPE,Form.COMMON_MSGTYPE_ERR);
							request.setAttribute(Form.COMMON_MSG, vld.OutMsgShort());
							eraflg = false;
							/** エラー後　入力値保持 */
							MaintenanceUser usr = null;
							String uid = (String) entrys.get(Form.MTN_USR_ETY_UID).toString().trim();
							String pas = (String) entrys.get(Form.MTN_USR_ETY_PAS);
							String nme = (String) entrys.get(Form.MTN_USR_ETY_NM_FAMILY) + (String) entrys.get(Form.MTN_USR_ETY_NM_NAME);
							String nm_family = (String) entrys.get(Form.MTN_USR_ETY_NM_FAMILY);
							String nm_name = (String) entrys.get(Form.MTN_USR_ETY_NM_NAME);

							String[] group = (String[]) entrys.get(Form.MTN_USR_ETY_ATH);
							String[] pos = (String[]) entrys.get(Form.MTN_USR_ETY_POS);

							// 選択ﾎﾞﾀﾝ押下時値保持
							if (entrys.get(Form.MTN_USR_ETY_IDX) != null
									&& Integer.parseInt((String) entrys
											.get(Form.MTN_USR_ETY_IDX)) >= 0) {
								int idx = Integer.parseInt((String) entrys
										.get(Form.MTN_USR_ETY_IDX));
								selection = (MaintenanceUser) userList.get(idx);
								usr = new MaintenanceUser(selection
										.getCD_user(), uid, pas, nme,
										nm_family, nm_name, group, pos,
										selection.getIndex());
							} else {
								usr = new MaintenanceUser(uid, pas, nme,
										nm_family, nm_name, group, pos);
							}

							selection = usr;

						} else {
							/* エラーなし */
							MaintenanceUser usr = null;
							int idx = 0;

							/**
							 * 更新処理概要 同一index 且つ 同一Key ：更新<br>
							 * 同一index 且つ 別key ：追加<br>
							 * 別 index 且つ 別Key ：追加<br>
							 * 別 index 且つ 同一Key ：エラー(ここまで到達不可)
							 */

							if (entrys.get(Form.MTN_USR_ETY_IDX) != null
									&& Integer.parseInt((String) entrys
											.get(Form.MTN_USR_ETY_IDX)) >= 0) {
								// 登録済みデータ更新 or 複製
								idx = Integer.parseInt((String) entrys
										.get(Form.MTN_USR_ETY_IDX));
								usr = (MaintenanceUser) userList.get(idx);
								// 更新
								// ユーザーコード
								usr.setId((String) entrys
										.get(Form.MTN_USR_ETY_UID));
								// パスワード
								usr.setPass((String) entrys
										.get(Form.MTN_USR_ETY_PAS));
								// ロールコード
								usr.setPoslist((String[]) entrys
										.get(Form.MTN_USR_ETY_POS));
								// グループコード
								usr.setGrouplist((String[]) entrys
										.get(Form.MTN_USR_ETY_ATH));
								// 姓
								usr.setNm_family((String) entrys
										.get(Form.MTN_USR_ETY_NM_FAMILY));
								// 名
								usr.setNm_name((String) entrys
										.get(Form.MTN_USR_ETY_NM_NAME));

								usr.setName((String) entrys
										.get(Form.MTN_USR_ETY_NM_FAMILY)
										+ (String) entrys
												.get(Form.MTN_USR_ETY_NM_NAME));

								usr.setUpd();
								// 登録データ反映
								userList.set(idx, usr);
							}
							if (usr == null) {
								// 新規登録 or 複製時
								idx = userList.size();
								int cd_user = getSequence();
								String uid = (String) entrys
										.get(Form.MTN_USR_ETY_UID);
								String pas = (String) entrys
										.get(Form.MTN_USR_ETY_PAS);
								String nme = (String) entrys
										.get(Form.MTN_USR_ETY_NM_FAMILY)
										+ (String) entrys
												.get(Form.MTN_USR_ETY_NM_NAME);
								String nm_family = (String) entrys
										.get(Form.MTN_USR_ETY_NM_FAMILY);

								String nm_name = (String) entrys
										.get(Form.MTN_USR_ETY_NM_NAME);
								String[] group = (String[]) entrys
										.get(Form.MTN_USR_ETY_ATH);

								String[] pos = (String[]) entrys
										.get(Form.MTN_USR_ETY_POS);
								usr = new MaintenanceUser(cd_user, uid, pas,
										nme, nm_family, nm_name, group, pos,
										idx);
								usr.setUpd();
								// 登録データ追加
								userList.add(idx, usr);
							}
							// リスト設定
							session.setAttribute(Consts.STR_SES_GRD, userList);
						}

					} else if (event.equals(Form.MTN_DELBTN)) {
						/** 削除ボタン */

						/** 入力値取得 */
						HashMap entrys = new HashMap();
						entrys.put(Form.MTN_USR_ETY_CD, esc.htmlEscape(request
								.getParameter(Form.MTN_USR_ETY_CD).toString()
								.trim()));
						// ユーザーID
						entrys.put(Form.MTN_USR_ETY_UID, esc.htmlEscape(request
								.getParameter(Form.MTN_USR_ETY_UID).toString()
								.trim()));
						// パスワード
						entrys.put(Form.MTN_USR_ETY_PAS, esc.htmlEscape(request
								.getParameter(Form.MTN_USR_ETY_PAS).toString()
								.trim()));
						// ロールコード 配列をHashMpに入れた

						String[] selected_pos = request
								.getParameterValues(Form.MTN_USR_ETY_POS);
						entrys.put(Form.MTN_USR_ETY_POS, selected_pos);

						// グループコード　配列をHashMpに入れた
						String[] selected_group = request
								.getParameterValues(Form.MTN_USR_ETY_ATH);
						entrys.put(Form.MTN_USR_ETY_ATH, selected_group);

						// 姓
						entrys.put(Form.MTN_USR_ETY_NM_FAMILY, esc
								.htmlEscape(request.getParameter(
										Form.MTN_USR_ETY_NM_FAMILY).toString()
										.trim()));
						// 名
						entrys.put(Form.MTN_USR_ETY_NM_NAME, esc
								.htmlEscape(request.getParameter(
										Form.MTN_USR_ETY_NM_NAME).toString()
										.trim()));
						entrys.put(Form.MTN_USR_ETY_IDX, esc.htmlEscape(request
								.getParameter(Form.MTN_USR_ETY_IDX).toString()
								.trim()));
						/* チェック */
						if (vld.delete(entrys, userList) == false) {
							/* エラーメッセージの設定 */
							/* エラーメッセージの設定 */
							request.setAttribute(Form.COMMON_MSGTYPE,
									Form.COMMON_MSGTYPE_ERR);
							request.setAttribute(Form.COMMON_MSG, vld
									.OutMsgShort());
							eraflg = false;
							/** エラー後　入力値保持 */
							MaintenanceUser usr = null;
							String uid = (String) entrys.get(
									Form.MTN_USR_ETY_UID).toString().trim();
							String pas = (String) entrys
									.get(Form.MTN_USR_ETY_PAS);
							String nme = (String) entrys
									.get(Form.MTN_USR_ETY_NM_FAMILY)
									+ (String) entrys
											.get(Form.MTN_USR_ETY_NM_NAME);
							String nm_family = (String) entrys
									.get(Form.MTN_USR_ETY_NM_FAMILY);
							String nm_name = (String) entrys
									.get(Form.MTN_USR_ETY_NM_NAME);

							String[] group = (String[]) entrys
									.get(Form.MTN_USR_ETY_ATH);
							String[] pos = (String[]) entrys
									.get(Form.MTN_USR_ETY_POS);
							// 選択ﾎﾞﾀﾝ押下時値保持
							if (entrys.get(Form.MTN_USR_ETY_IDX) != null
									&& Integer.parseInt((String) entrys
											.get(Form.MTN_USR_ETY_IDX)) >= 0) {
								int idx = Integer.parseInt((String) entrys
										.get(Form.MTN_USR_ETY_IDX));
								selection = (MaintenanceUser) userList.get(idx);
								usr = new MaintenanceUser(selection
										.getCD_user(), uid, pas, nme,
										nm_family, nm_name, group, pos,
										selection.getIndex());
							} else {
								usr = new MaintenanceUser(uid, pas, nme,
										nm_family, nm_name, group, pos);
							}

							selection = usr;

						} else {
							/* エラーなし */
							// 登録済みデータ更新
							int idx = 0;
							idx = Integer.parseInt((String) entrys
									.get(Form.MTN_USR_ETY_IDX));
							MaintenanceUser usr = (MaintenanceUser) userList
									.get(idx);
							usr.setDel();

							// 削除データ反映
							userList.set(idx, usr);
							// リスト設定
							session.setAttribute(Consts.STR_SES_GRD, userList);
						}
					}
					if (eraflg != false
							&& event.equals(Form.MTN_RSTBTN) == false) {
						// else if (event.equals(Form.MTN_SAVBTN)) {

						/** 保存ボタン */
						/* チェック */
						if (vld.save(userList) == false) {
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
							msg = setUserRecord(userList, loginusr, msg);
							// 再設定
							userList = getUserRecord();
							ArrayList posList = getPosRecord();
							ArrayList athList = getAthRecord();

							// リスト設定
							session.setAttribute(Consts.STR_SES_GRD, userList);
							session.setAttribute(Consts.STR_SES_GRD2, posList);
							session.setAttribute(Consts.STR_SES_GRD3, athList);

							/** 表のグループに使用 */
							ArrayList groupList = getGroupRecord();
							ArrayList posList_user = getPosRecord_user();

							session
									.setAttribute(Consts.STR_SES_GRD5,
											groupList);
							session.setAttribute(Consts.STR_SES_GRD6,
									posList_user);

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
					selection = (MaintenanceUser) userList.get(idx);
				}
			}
			request.setAttribute(Consts.STR_REQ_REC, selection);

			strPage = config.getInitParameter("MaintenanceUser");

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
	 * ロール情報取得 <br /> DBより権限情報を全て取得し、ArrayList形式で戻す。<br />
	 *
	 * @return 取得したArrayList形式Userデータ <br />
	 * @throws Exception
	 *             例外 <br />
	 */
	protected ArrayList getPosRecord_user() throws Exception {
		List list = null;
		try {
			QueryRunner qr = new QueryRunner(DBConnection.getDataSource());
			ResultSetHandler rsh = new MapListHandler();
			list = (List) qr.query(SQL.POS_SEL_002, rsh);
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
				beanList.add(i, new Position(Integer.parseInt(mu.get("cd_user")
						.toString().trim()), Integer.parseInt(mu.get(
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
			list = (List) qr.query(SQL.ATH_SEL_002, rsh);
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
				beanList.add(i, new Auth(Integer.parseInt(mu.get("cd_user")
						.toString().trim()), Integer.parseInt(mu
						.get("cd_group").toString().trim()), mu.get("nm_group")
						.toString().trim()));
			}
		}
		return beanList;
	}

	/**
	 * ユーザー情報取得 <br /> DBよりユーザー情報を全て取得し、ArrayList形式で戻す。<br />
	 *
	 * @return 取得したArrayList形式Userデータ <br />
	 * @throws Exception
	 *             例外 <br />
	 */
	protected ArrayList getUserRecord() throws Exception {
		List list = null;

		try {
			QueryRunner qr = new QueryRunner(DBConnection.getDataSource());
			ResultSetHandler rsh = new MapListHandler();
			list = (List) qr.query(SQL.USER_SEL_003, rsh);

		} catch (Exception en) {
			System.out.println("DBUtils NullPointerException:MaintenanceUser_getUserRecord()");
		}

		Map mu = null;
		ArrayList beanList = null;
		if (list != null) {
			beanList = new ArrayList();
			for (int i = 0; i < list.size(); i++) {
				mu = (Map) list.get(i);
				beanList.add(i, new MaintenanceUser(
						Integer.parseInt(mu.get("cd_user").toString().trim()),
						mu.get("user_id").toString().trim(),
						mu.get("passwords").toString().trim(),
						mu.get("uname").toString().trim(),
						mu.get("nm_family").toString().trim(),
						mu.get("nm_name").toString().trim(),
						mu.get("cd_auth").toString().trim(),
						mu.get("dt_pw_term").toString().trim(),
						"flg",
						i));
			}
		}
		return beanList;
	}

	/**
	 * ユーザー情報設定
	 * 画面にて変更を行ったUserListをDBへ反映
	 *
	 * @param list   ArrayList形式Userデータ
	 * @param loginusr      ログインユーザ情報
	 * @throws SQLException    例外
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
				MaintenanceUser usr = (MaintenanceUser) list.get(i);
				if (usr.getState() == MaintenanceUser.STATE_UPD) {

					/**
					 * UPDATE or INSERTを行うデータ
					 */
					save = Consts.SQL_MESSAGE_IN;

					// 特殊追加
					if (usr.getPoslist() == null) {
						// 権限が未場合の場合は削除する。
						/** sys_users */
						int cnt = qr.update(con, SQL.USER_DEL_001, new String[] { usr.getId() });
						System.out.println("DELETE pkey db[sys_users]:" + usr.getId() + " COUNT:" + cnt);

						/** sys_user_pos */
						cnt = qr.update(con, SQL.USER_DEL_002, new String[] { usr.getId() });
						System.out.println("DELETE pkey db[sys_user_pos]:" + usr.getId() + " COUNT:" + cnt);

						/** sys_user_group */
						cnt = qr.update(con, SQL.USER_DEL_003, new String[] { usr.getId() });
						System.out.println("DELETE pkey db[sys_group]:" + usr.getId() + " COUNT:" + cnt);

					} else {

						int cnt = 0;
						/**
						 * UPDATE
						 */

						/** sys_users */
						/** シーケンスの取得 */
						int cnt_id = qr.update(con, SQL.USER_UPD_001,
								new Object[] {
										new Integer(usr.getCD_user()),
										usr.getId(),
										usr.getPass(),
										usr.getNm_family(),
										usr.getNm_name(),
										usr.getDt_pw_term_(),
										new Integer(usr.getCD_user()),
										loginusr.getId(),
										new Integer(usr.getCD_user())
								});
						System.out.println("UPDATE pkey db[sys_users]:" + usr.getId() + " COUNT:" + cnt_id);

						if (cnt_id != 0) {
							/** sys_user_pos */
							String[] k = usr.getPoslist();
							for (int len = 0; len < k.length; len++) {
								if (len == 0) {
									// 複製登録の際に削除しないとエラーになるため
									/** sys_user_group */

									cnt = qr.update(con, SQL.USER_DEL_002, new Object[] { new Integer(usr.getCD_user()) });
									System.out.println("DELETE pkey db[sys_user_pos]:" + usr.getId() + " COUNT:" + cnt);
								}
								cnt = qr.update(con, SQL.USER_INS_002,
												new Object[] {
														new Integer(usr.getCD_user()),
														new Integer(k[len]),
														loginusr.getId(),
														usr.getId()
												});
								System.out.println("INSERT pkey db[sys_user_pos]:" + usr.getId() + " COUNT:" + cnt);
							}

							k = null;
							k = usr.getGrouplist();
							for (int len = 0; len < k.length; len++) {
								/** sys_user_group */
								if (len == 0) {
									// 複製登録の際に削除しないとエラーになるため
									/** sys_user_group */
									cnt = qr.update(con, SQL.USER_DEL_003, new Object[] { new Integer(usr.getCD_user()) });
									System.out.println("DELETE pkey db[sys_user_group]:" + usr.getId() + " COUNT:" + cnt);
								}
								cnt = qr
										.update(con, SQL.USER_INS_003,
												new Object[] {
														new Integer(usr.getCD_user()),
														new Integer(k[len]),
														loginusr.getId(),
														usr.getId()
												});
								System.out.println("INSERT pkey db[sys_user_group]:" + usr.getId() + " COUNT:" + cnt);
							}
						}

						// 1件もUPDATEできなかった場合はINSERTを行う
						if (cnt_id == 0) {
							/**
							 * INSERT
							 */
							/**
							 * sys_users
							 */
							cnt = qr.update(con, SQL.USER_INS_001,
											new Object[] {
													new Integer(usr.getCD_user()),
													usr.getId(),
													usr.getPass(),
													usr.getNm_family(),
													usr.getNm_name(),
													usr.getDt_pw_term_(),
													new Integer(usr.getCD_user()),
													loginusr.getId(),
													loginusr.getId()
											});
							System.out.println("INSERT pkey db[sys_users]:" + usr.getId() + " COUNT:" + cnt);

							/**
							 * sys_user_pos
							 */
							String[] k = usr.getPoslist();

							for (int len = 0; len < k.length; len++) {
								cnt = qr.update(con, SQL.USER_INS_002,
										new Object[] {
												new Integer(usr.getCD_user()),
												new Integer(k[len]),
												loginusr.getId(),
												loginusr.getId() });
								System.out
										.println("INSERT pkey db[sys_user_pos]:"
												+ usr.getId() + " COUNT:" + cnt);
							}
							/**
							 * sys_user_group
							 */
							k = null;
							k = usr.getGrouplist();
							for (int len = 0; len < k.length; len++) {
								cnt = qr
										.update(con, SQL.USER_INS_003,
												new Object[] {
														new Integer(usr
																.getCD_user()),
														new Integer(k[len]),
														loginusr.getId(),
														usr.getId() });
								System.out
										.println("INSERT pkey db[sys_user_group]:"
												+ usr.getId() + " COUNT:" + cnt);
							}
						}
					}
				} else if (usr.getState() == MaintenanceUser.STATE_DEL) {
					/**
					 * DELETE を行うデータ
					 */
					save = Consts.SQL_MESSAGE_DELETE;
					// sys_usersから
					int cnt = qr.update(con, SQL.USER_DEL_001,
							new Object[] { new Integer(usr.getCD_user()) });
					System.out.println("DELETE pkey db[sys_users]:"
							+ usr.getId() + " COUNT:" + cnt);

					// ss_user_posから
					cnt = qr.update(con, SQL.USER_DEL_002,
							new Object[] { new Integer(usr.getCD_user()) });
					System.out.println("DELETE pkey db[sys_user_pos]:"
							+ usr.getId() + " COUNT:" + cnt);

					// sys_user_groupから
					cnt = qr.update(con, SQL.USER_DEL_003,
							new Object[] { new Integer(usr.getCD_user()) });
					System.out.println("DELETE pkey db[sys_group]:"
							+ usr.getId() + " COUNT:" + cnt);
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
		} catch (Exception en) {
			System.out
					.println("DBUtils NullPointerException:MaintenanceReportSideServlet:getSys_menuRecode()");
		}
		return intsequence;
	}

}
