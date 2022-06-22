/*
 * 作成日: 2007/11/08
 * 更新日: 2018/12/05
 *
 */
package authentication.servlet;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import authentication.bean.Auth;
import authentication.bean.Position;
import authentication.bean.Report;
import authentication.bean.Side;
import authentication.bean.User;
import authentication.connection.DBConnection;
import authentication.dbaccess.DBinfo;
import authentication.defines.Consts;
import authentication.defines.Form;
import authentication.defines.Message;
import authentication.defines.SQL;
import authentication.parser.LoginUser;
import authentication.parser.UserParser;
import authentication.util.CmnDate;
import authentication.util.Tagescape;
import authentication.validation.LoginValidation;
import common.ChkUsableTime;


/**
 * ログイン処理制御クラス
 * ログイン・ログオフの処理を制御する。
 */
public class LoginServlet extends HttpServlet {
	/**
	 *
	 */
	private static final long serialVersionUID = 591119765164319961L;
	private ServletConfig config = null;

	/**
	 * 初期化
	 *
	 * @param config 設定
	 * @throws ServletException サーブレットエラー
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

		/* セッションクリア */
		session.removeAttribute(Consts.STR_SES_LOGINUSER);	// LoginUser
		session.removeAttribute(Consts.STR_SES_LOGINPOS);	// LoginUserPosition
		session.removeAttribute(Consts.STR_SES_LOGINATH);	// LoginUserAth
		session.removeAttribute(Consts.STR_SES_REPSIDE);	// Side_main
		session.removeAttribute(Consts.STR_SES_MSTSIDE);	// Side_master

		session.removeAttribute(Consts.STR_SES_LOGINDT);	// LoginDt

		String strPage = null;

		String Parameter = "";
		String User = "";
		String Pass = "";
		String View = "";

		if (request.getParameter(Form.LOGIN_VIEW) != null){
			// 情報保持
			session.setAttribute("_"+Form.LOGIN_USER, request.getParameter(Form.LOGIN_USER));
			session.setAttribute("_"+Form.LOGIN_PASS, request.getParameter(Form.LOGIN_PASS));
			session.setAttribute("_"+Form.LOGIN_VIEW, request.getParameter(Form.LOGIN_VIEW));

			User = request.getParameter(Form.LOGIN_USER);
			Pass = request.getParameter(Form.LOGIN_PASS);
			View = request.getParameter(Form.LOGIN_VIEW);

		} else {

			User = session.getAttribute("_"+Form.LOGIN_USER)==null ? "" : (String)session.getAttribute("_"+Form.LOGIN_USER);
			Pass = session.getAttribute("_"+Form.LOGIN_PASS)==null ? "" : (String)session.getAttribute("_"+Form.LOGIN_PASS);
			View = session.getAttribute("_"+Form.LOGIN_VIEW)==null ? "" : (String)session.getAttribute("_"+Form.LOGIN_VIEW);

		}


		String MenuKbn = "-1";
		if (request.getParameter(Form.PRM_MENU_KBN) != null){
			MenuKbn = request.getParameter(Form.PRM_MENU_KBN);
		}

		if (!"".equals(User)){
			// 失敗時の予備
			session.setAttribute(Consts.STR_SES_LOGINUSER, getTopLoad(User, Pass));
			// ログアウト時に初期化するパラメータ情報
			Parameter = "?"+Form.PRM_MENU_KBN+"="+MenuKbn+"&"+Form.LOGIN_USER+"="+User+"&"+Form.LOGIN_PASS+"="+Pass+"&"+Form.LOGIN_VIEW+"="+View;
		}
		// ログオン失敗時の転送先
		strPage = config.getInitParameter("Login") + Parameter;

		try {
			// 利用時間の時間取得
			String fromData	= getServletContext().getInitParameter(Consts.FROM_DATA);
			String toData	= getServletContext().getInitParameter(Consts.TO_DATA);
			/* 利用可能時間チェック */
			ChkUsableTime sys = new ChkUsableTime(fromData, toData);
			if ( sys.isCloseTime(User) || sys.isWaitTime(User) ){
				getServletContext().getRequestDispatcher(strPage).forward(request,response);
				return;
			}

			if (request.getParameter(Form.LOGIN_USER) == null || request.getParameter(Form.LOGIN_PASS) == null) {
				// フォーム値未入力の場合
				session.invalidate();
				// 別ページ遷移設定 : Loginへ
				getServletContext().getRequestDispatcher(strPage).forward(request,response);
				return;
			}

			// フォーム値入力済みの場合
			Tagescape esc = new Tagescape();
			LoginValidation vld = new LoginValidation();

			// 入力された値の取得
			String strUserID = esc.htmlEscape(request.getParameter(Form.LOGIN_USER).toString().trim());
			String strPass = esc.htmlEscape(request.getParameter(Form.LOGIN_PASS).toString().trim());

			if (!vld.entry(strUserID, strPass)) {
				// 入力チェックでエラー項目が検出された場合
				request.setAttribute(Form.COMMON_MSGTYPE, Form.COMMON_MSGTYPE_ERR);
				request.setAttribute(Form.COMMON_MSG, vld.OutMsgShort());

				// 別ページ遷移設定 : Loginへ
				getServletContext().getRequestDispatcher(strPage).forward(request,response);
				return;
			}

			// 入力チェックでエラーなしの場合
			User loginusr = null;
			Position position = null;
			Auth auth = null;


			String strMenuKbn = esc.htmlEscape(MenuKbn.trim());


			/** 管理者以外の場合 */
			String sql = SQL.USER_SEL_001;
			Object[] param = { strUserID, strPass };
			List list = null;
			Map mu = null;

			try {
				QueryRunner qr = new QueryRunner(DBConnection.getDataSource());
				ResultSetHandler rsh = new MapHandler();
				mu = (Map) qr.query(sql, param, rsh);

			} catch (Exception en) {
				System.out.println("DBUtils NullPointerException:LoginServlet_User");
			}

			if (mu == null) {

				// DBに対象データが存在しない場合
				request.setAttribute(Form.COMMON_MSGTYPE, Form.COMMON_MSGTYPE_ERR);
				request.setAttribute(Form.COMMON_MSG, Message.ERR_AUTH_FAIL);

				// 別ページ遷移設定 : Loginへ
				getServletContext().getRequestDispatcher(strPage).forward(request,response);
				return;

			}

			// ユーザーのデータを保持
			User usr = new User(
					Integer.parseInt(mu.get("CD_USER").toString().trim()),
					mu.get("USER_ID").toString().trim(),
					mu.get("PASSWORDS").toString().trim(),
					mu.get("UNAME").toString().trim(),
					mu.get("CD_AUTH").toString().trim(),
					mu.get("DT_PW_TERM").toString().trim(),
					Integer.parseInt(mu.get("CD_GROUP").toString().trim()),
					Integer.parseInt(mu.get("CD_POSITION").toString().trim()),
					mu.get("CUSTOM_VALUE").toString().trim(),
					mu.get("LOGO").toString().trim(),
					mu.get("YOBI_1").toString().trim(),
					mu.get("YOBI_2").toString().trim(),
					mu.get("YOBI_3").toString().trim(),
					mu.get("YOBI_4").toString().trim(),
					mu.get("YOBI_5").toString().trim(),
					mu.get("YOBI_6").toString().trim(),
					mu.get("YOBI_7").toString().trim(),
					mu.get("YOBI_8").toString().trim(),
					mu.get("YOBI_9").toString().trim(),
					mu.get("YOBI_10").toString().trim()
					);

			loginusr = usr;

			// ユーザー情報の変換
			UserParser convert = new UserParser();
			LoginUser lUser = convert.UserParse(loginusr);

			// ロールを保持
			position = new Position(loginusr.getPos(), mu.get("nm_position").toString().trim());

			// グループを保持
			auth = new Auth(loginusr.getGroup(), mu.get("sosznm").toString().trim());

			// 担当店舗を取得
			String tantoTen	= loginusr.getYobi2_();
			if(StringUtils.isNotEmpty(tantoTen) && StringUtils.isNumeric(tantoTen)){
				if(StringUtils.equals("-1", strMenuKbn)){
					// URLに区分が含まれなかった場合に、区分を設定する
					strMenuKbn = "6";
				}
			}

			// メニュー区分=6(店舗特売画面)の場合
			if(StringUtils.equals("6", strMenuKbn)){
				loginusr.setYobi6_("-1");
				loginusr.setYobi7_("-1");
				loginusr.setYobi8_("-1");

			// メニュー区分=4(本部マスタ)の場合(開発ユーザー用ルート)
			}else if(StringUtils.equals("4", strMenuKbn) ){
				loginusr.setYobi8_("-1");

			// メニュー区分=5(特売)の場合(開発ユーザー用ルート)
			}else if(StringUtils.equals("5", strMenuKbn)){ // メニュー区分=4,5(本部マスタ、特売)の場合(開発ユーザー用ルート)
				loginusr.setYobi6_("-1");
				loginusr.setYobi7_("-1");
			}

			// ロールを全て取得
			try {
				QueryRunner qr = new QueryRunner(DBConnection.getDataSource());
				ResultSetHandler rsh = new MapListHandler();
				list = (List) qr.query(SQL.USER_SEL_001, new Object[] {	strUserID, strPass }, rsh);
			} catch (Exception en) {
				System.out.println("DBUtils NullPointerException:権限判断エラー");
				throw en;
			}
			mu = null;
			List<String> posList = new ArrayList<String>();
			List<String> posCustomValueList = new ArrayList<String>();
			for (int i = 0; i < list.size(); i++) {
				mu = (Map) list.get(i);
				posList.add(ObjectUtils.toString(mu.get("CD_POSITION")));
				posCustomValueList.add(ObjectUtils.toString(mu.get("POS_CUSTOM_VALUE")));
			}
			loginusr.setPoslist(posList.toArray(new String[posList.size()]));
			loginusr.setPos_custom_value_list(posCustomValueList.toArray(new String[posCustomValueList.size()]));
			// TODO:いなげや特処理
			loginusr.setPos_custom_value_();

//			// マスタメニューは、ポータル側のみ
//			/** レポート分類メニューでマスタが表示できる権限を持っているかを調べている */
//			try {
//				QueryRunner qr = new QueryRunner(DBConnection.getDataSource());
//				ResultSetHandler rsh = new MapListHandler();
//				list = (List) qr.query(SQL.USER_SEL_001, new Object[] {	strUserID, strPass }, rsh);
//			} catch (Exception en) {
//				System.out.println("DBUtils NullPointerException:権限判断エラー");
//				throw en;
//			}
//
//			int hander = 0;
//			mu = null;
//
//			for (int i = 0; i < list.size(); i++) {
//				mu = (Map) list.get(i);
//				if (Integer.parseInt(mu.get("cd_position").toString().trim()) == (Consts.SYSTEM_MASTER)) {
//					hander = Integer.parseInt(mu.get("cd_position").toString().trim());
//					break;
//				} else {
//					hander = 0;
//				}
//			}

			// DBに対象データが存在した場合
			/**
			 * レポート分類メニュー設定(マスタ以外)
			 */
			list = null;
			try {
				QueryRunner qr = new QueryRunner(DBConnection.getDataSource());
				ResultSetHandler rsh = new MapListHandler();

				list = (List) qr.query(SQL.SIDE_SEL_001,
						new Object[] {
							new Long(strMenuKbn),
							new Long(loginusr.getCD_user()),
							new Long(loginusr.getCD_user()) },
						rsh);
			} catch (Exception en) {
				System.out.println("DBUtils NullPointerException:LoginServlet_ReportMenu");
				throw en;
			}

			mu = null;
			ArrayList menu = null;
			if (list != null) {
				menu = new ArrayList();

				for (int i = 0; i < list.size(); i++) {
					mu = (Map) list.get(i);
					menu.add(i,
							new Side(Integer.parseInt(mu.get("cd_report_side").toString().trim()),
							mu.get("NM_REPORT_SIDE").toString().trim(),
							mu.get("cd_disp_column").toString().trim()));

				}
			}

// マスタメニューは、ポータル側のみ
			/**
			 * レポート分類メニュー設定(マスタ)
			 */
			list = null;
//			if (hander == Consts.SYSTEM_MASTER) {
//				try {
//					QueryRunner qr = new QueryRunner(DBConnection.getDataSource());
//					ResultSetHandler rsh = new MapListHandler();
//					list = (List) qr.query(SQL.SIDE_SEL_002,
//							new Object[] {
//									new Long(loginusr.getCD_user()),
//									new Long(loginusr.getCD_user()) },
//							rsh);
//				} catch (Exception en) {
//					System.out.println("DBUtils NullPointerException:LoginServlet_ReportMstr");
//					throw en;
//				}
//			}

			mu = null;
			ArrayList mst = null;
//			if (list != null) {
//				mst = new ArrayList();
//				for (int i = 0; i < list.size(); i++) {
//					mu = (Map) list.get(i);
//					mst.add(i, new Side(Integer.parseInt(mu.get("cd_report_side").toString().trim()),
//							mu.get("NM_REPORT_SIDE").toString().trim(),
//							mu.get("cd_disp_number").toString().trim(),
//							1));
//				}
//			}
//
//			/**
//			 * レポート設定(マスタ)
//			 */
//			if (list != null) {
//				for (int i = 0; i < list.size(); i++) {
//					Side m = (Side) mst.get(i);
//					List listmst = null;
//
//					try {
//						QueryRunner qr = new QueryRunner(DBConnection.getDataSource());
//						ResultSetHandler rsh = new MapListHandler();
//						listmst = (List) qr.query(
//										SQL.REPORT_SEL_001,
//										new Object[] {
//												new Long(loginusr.getCD_user()),
//												new Long(loginusr.getCD_user()),
//												new Long(m.getSide())
//										},
//										rsh);
//					} catch (Exception en) {
//						System.out.println("DBUtils NullPointerException:LoginServlet_ReportMstr");
//						throw en;
//					}
//
//					mu = null;
//					ArrayList rep = null;
//					if (listmst != null) {
//						rep = new ArrayList();
//						for (int j = 0; j < listmst.size(); j++) {
//							mu = (Map) listmst.get(j);
//							rep.add(j,
//									new Report(Integer.parseInt(mu.get("cd_report_side").toString().trim()),
//									Integer.parseInt(mu.get("cd_report_no").toString().trim()),
//									mu.get("NM_REPORT").toString().trim(),
//									getUrlencoder(mu.get("NM_REPORT").toString().trim()),
//									mu.get("cd_disp_number").toString().trim(),
//									"report"));
//						}
//						m.setReportMst(rep);
//						mst.set(i, m);
//					}
//				}
//			}

			/**
			 * レポート設定
			 */
			List list1 = null;
			List list2 = null;
			List list3 = null;
			ArrayList list4 = new ArrayList();

			if (menu != null) {
				for (int i = 0; i < menu.size(); i++) {
					Side m = (Side) menu.get(i);

					list = null;
					try {
						QueryRunner qr = new QueryRunner(DBConnection
								.getDataSource());
						ResultSetHandler rsh = new MapListHandler();
						list = (List) qr.query(
										SQL.REPORT_SEL_001,
										new Object[] {
												new Long(loginusr.getCD_user()),
												new Long(loginusr.getCD_user()),
												new Long(m.getSide()) },
										rsh);

					} catch (Exception en) {
						System.out.println("DBUtils NullPointerException:LoginServlet_Report");
						throw en;
					}

					mu = null;
					ArrayList rep = null;
					String users_custom = getUrlencoder(usr.getUsers_custom());

					if (list != null) {
						rep = new ArrayList();
						for (int j = 0; j < list.size(); j++) {
							QueryRunner qr = new QueryRunner(DBConnection.getDataSource());
							ResultSetHandler rsh = new MapListHandler();
							mu = (Map) list.get(j);

//							// レポート分類マスタ
//							list1 = (List) qr.query(
//									SQL.REPORT_PARAMETER_002,
//									new Object[] {
//											new Long(mu.get("CD_REPORT_SIDE").toString().trim()) },
//									rsh);
//
//							// グループマスタ
//							list2 = (List) qr.query(
//											SQL.REPORT_PARAMETER_003,
//											new Object[] {
//													new Long(mu.get("CD_REPORT_NO").toString().trim()),
//													new Long(usr.getCD_user()) },
//											rsh);
//							// ロールマスタ
//							list3 = (List) qr.query(
//											SQL.REPORT_PARAMETER_004,
//											new Object[] {
//													new Long(mu.get("CD_REPORT_NO").toString().trim()),
//													new Long(usr.getCD_user()) },
//											rsh);

							rep.add(j, new Report(
									Integer.parseInt(mu.get("CD_REPORT_SIDE").toString().trim()),
									Integer.parseInt(mu.get("CD_REPORT_NO").toString().trim()),
									mu.get("NM_REPORT").toString().trim(),
									mu.get("NM_SHORT").toString().trim(),
									mu.get("NM_REPORT_JSP").toString().trim(),
									mu.get("CD_DISP_NUMBER").toString().trim(),
									mu.get("ENABLE_MENU").toString().trim(),
									mu.get("custom_value").toString().trim().toString().trim(),
									users_custom, getParameter(list1),
									getParameter(list2),
									getParameter(list3),
									mu.get("YOBI_1").toString().trim(),
									mu.get("YOBI_2").toString().trim(),
									mu.get("YOBI_3").toString().trim(),
									mu.get("YOBI_4").toString().trim(),
									mu.get("YOBI_5").toString().trim()));

						}
						m.setReport(rep);
						menu.set(i, m);
						list4.add(i, m.getDisp_Column());
					}
				}
			}

			// ------------------------------
			// DB更新状況取得
			// ------------------------------
			DBinfo dbinfo = new DBinfo();
			dbinfo.setLogMenu(loginusr.getId());

			// セッションの設定
			session.setAttribute(Consts.STR_SES_LOGINUSER, loginusr);	// LoginUser
			session.setAttribute(Consts.STR_SES_LOGINPOS, position);	// LoginUserPosition
			session.setAttribute(Consts.STR_SES_LOGINATH, auth);		// LoginUserAth
			session.setAttribute(Consts.STR_SES_REPSIDE, menu);			// Side_main
			session.setAttribute("DISP", list4);						// Side_main
			session.setAttribute(Consts.STR_SES_MSTSIDE, mst);			// Side_Master
			session.setAttribute(Consts.STR_HEAD_TYPE, Consts.STR_HEAD_TYPE_ON);	// ヘッダリンク表示ON

			CmnDate cdate = new CmnDate();
			session.setAttribute(Consts.STR_SES_LOGINDT, cdate.getToday());	// ログイン日時

			if (request.getParameter(Form.LOGIN_VIEW) != null){
				// 別ページ遷移設定 : Loginへ
				strPage = config.getInitParameter(request.getParameter(Form.LOGIN_VIEW));
				getServletContext().getRequestDispatcher(strPage).forward(request,response);
				return;
			}

			// ACTION 専用処理
			String Action = request.getParameter(Form.ACTION)==null ? "" : (String)request.getParameter(Form.ACTION);
			if (Action.length()!=0){
				session.setAttribute(Form.ACTION, Action);	// セッション保持
			}
			// SendMode 専用処理
			String SendMode = request.getParameter(Consts.STR_SES_REPORT_SEND_MODE)==null ? "" : (String)request.getParameter(Consts.STR_SES_REPORT_SEND_MODE);
			if (Action.length()!=0){
				session.setAttribute(Consts.STR_SES_REPORT_SEND_MODE, SendMode);	// セッション保持
			}
			// SendParam 専用処理
			String SendParam = request.getParameter(Consts.STR_SES_REPORT_SEND_PARAM)==null ? "" : (String)request.getParameter(Consts.STR_SES_REPORT_SEND_PARAM);
			if (Action.length()!=0){
				session.setAttribute(Consts.STR_SES_REPORT_SEND_PARAM, SendParam);	// セッション保持
			}

			/**
			 * 別ページ遷移設定
			 * ①本部ユーザー　マスタ/特売	：Login2へ
			 * ②本部ユーザー　店舗特売		：TenSelectへ
			 * ③店舗ユーザー				：Menuへ
			 */

			// 店舗ユーザーではない場合
			String yobi9 = loginusr.getYobi9_();
			if(StringUtils.isNotEmpty(tantoTen) && StringUtils.isNumeric(tantoTen)){
				if (StringUtils.isEmpty(yobi9)) {
					strPage = config.getInitParameter("Menu");
				} else {
					strPage = config.getInitParameter("TenSelect");
				}
			}else{
				if(StringUtils.equals("6", strMenuKbn)){	// メニュー区分=6(店舗特売画面)の場合
					if (list4.size() == 0) {
						strPage = config.getInitParameter("Menu");	// 権限なし
					} else {
						strPage = config.getInitParameter("TenSelect");
					}
				}else if(StringUtils.equals("4", strMenuKbn) || StringUtils.equals("5", strMenuKbn)){
					strPage = config.getInitParameter("Menu");	// メニュー区分=4,5(本部マスタ、特売)の場合(開発ユーザー用ルート)
				}else{
					strPage = config.getInitParameter("Login2");
				}
				// パスワード有効期限確認
				if(!vld.IsPasswordTrimBefore(lUser)) {
					strPage = config.getInitParameter("Pass");
				}
			}

		} catch (Exception e) {
			// スタックトレースの出力
			e.printStackTrace();
			// エラーメッセージの設定
			request.setAttribute(Form.COMMON_MSGTYPE,Form.COMMON_MSGTYPE_ERR);
			// ページ遷移先の設定
			strPage = this.getServletContext().getInitParameter("Page_Error");
		}

		/**
		 * ページ遷移
		 */
		getServletContext().getRequestDispatcher(strPage).forward(request,response);
	}

	/**
	 * レポート分類への情報のCUSTOM_VALUEを加工するためのクラス
	 *
	 * @param list list
	 * @return Strparameter
	 * @throws Exception 例外
	 */
	protected String getParameter(List list) throws Exception {
		String Strparameter = "";
		Map Mapparameter = null;

		if (list!=null) {
			if (list.size() != 0) {
				for (int k = 0; k < list.size(); k++) {
					Mapparameter = (Map) list.get(k);
					if (k != 0) {
						Strparameter = Strparameter	+ ";" + Mapparameter.get("custom_value").toString().trim();
					} else {
						Strparameter = Mapparameter.get("custom_value").toString().trim();
					}
				}

			} else {
				Mapparameter = (Map) list.get(0);
				Strparameter = Mapparameter.get("custom_vlaue").toString().trim();
			}
		}
		return Strparameter;
	}

	/**
	 * パラメータをURLエンコードするためのクラス
	 *
	 * @param url_encoder パラメータ
	 * @return url_encode
	 * @throws Exception 例外
	 */
	protected String getUrlencoder(String url_encoder) throws Exception {
		String url_encode = null;
		url_encode = URLEncoder.encode(url_encoder, Consts.ENCODE);
		return url_encode;
	}

	/**
	 * @param strUserID ユーザーID
	 * @param strPass パスワード
	 *
	 * @return User
	 */
	protected User getTopLoad (String strUserID, String strPass){
		User usr = null;
		try {
			String sql = SQL.USER_SEL_001;
			Object[] param = { strUserID, strPass };
			Map mu = null;

			try {
				QueryRunner qr = new QueryRunner(DBConnection.getDataSource());
				ResultSetHandler rsh = new MapHandler();
				mu = (Map) qr.query(sql, param, rsh);

			} catch (Exception e) {
				// スタックトレースの出力
				System.out.println("DBUtils NullPointerException:LoginServlet_User");
				e.printStackTrace();
			}

			if (mu != null) {
				// ユーザーのデータを保持
				usr = new User(Integer.parseInt(
						mu.get("CD_USER").toString().trim()),
						mu.get("USER_ID").toString().trim(),
						mu.get("PASSWORDS").toString().trim(),
						mu.get("uname").toString().trim(),
						mu.get("CD_AUTH").toString().trim(),
						mu.get("DT_PW_TERM").toString().trim(),
						Integer.parseInt(mu.get("CD_GROUP").toString().trim()),
						Integer.parseInt(mu.get("cd_position").toString().trim()),
						mu.get("custom_value").toString().trim(),
						mu.get("LOGO").toString().trim(),
						mu.get("YOBI_1").toString().trim(),
						mu.get("YOBI_2").toString().trim(),
						mu.get("YOBI_3").toString().trim(),
						mu.get("YOBI_4").toString().trim(),
						mu.get("YOBI_5").toString().trim()
						);
			}

		} catch (Exception e) {
			// スタックトレースの出力
			e.printStackTrace();
		}
		return usr;
	}

}