/*
 * 作成日: 2007/10/26
 *
 */
package authentication.servlet;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import authentication.bean.Report;
import authentication.bean.Side;
import authentication.bean.User;
import authentication.defines.Consts;
import authentication.defines.Form;
import common.Defines;

/**
 * メニュー処理制御クラス
 * メニュー画面を表示する際の処理を制御する
 */
public class MenuServlet extends HttpServlet {
	/**
	 *
	 */
	private static final long serialVersionUID = 426123602076827384L;
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
		/* セッションクリア */
		/* メンテナンス画面のListクリア */
		session.removeAttribute(Consts.STR_SES_GRD);
		session.removeAttribute(Consts.STR_SES_GRD2);
		session.removeAttribute(Consts.STR_SES_GRD3);
		session.removeAttribute(Consts.STR_SES_GRD4);
		session.removeAttribute(Consts.STR_SES_GRD5);
		session.removeAttribute(Consts.STR_SES_GRD6);
		session.removeAttribute(Consts.STR_SES_TITLE);

		// Report画面情報クリア(必須:レポート分類選択時の初期表示指定に利用)
		session.removeAttribute(Consts.STR_SES_REPORT_SIDE);
		session.removeAttribute(Consts.STR_SES_REPORT_NO);
		session.removeAttribute(Consts.STR_SES_REPORT_SIDE_ARRAY);
		session.removeAttribute(Consts.STR_SES_REPORT_NO_ARRAY);

		String strPage = null;

		try {
			/**
			 * メニュー表示処理
			 */
			String FilePath = config.getInitParameter("InfoFile");
			request.setAttribute(Form.MENU_INFO_PATH, FilePath);

			/* info.txt 編集 */
			if (request.getParameter(Form.MENU_INFO_BTN) == null) {
				// 表示

			} else {
				String info = request.getParameter(Form.MENU_INFO_BTN).toString();

				if (info.trim().length() != 0) {
					/* 編集・保存 */
					if (info.equals(Form.MENU_INFO_EDIT)) {
						// 編集
						request.setAttribute(Form.MENU_INFO_BTN,Form.MENU_INFO_EDIT);

					} else if (info.equals(Form.MENU_INFO_SAVE)) {
						// 保存
					}
				}

			}

			// ユーザ情報取得※CD_AUTHの値を追加（nullの場合は、通常メニュー）
			User lusr = (User)session.getAttribute(Consts.STR_SES_LOGINUSER);
			String initParamName = "Menu";
			if (lusr.getCd_auth_() != null) {
				initParamName += lusr.getCd_auth_();
			}

			/**
			 * 別ページ遷移設定 : Menuへ
			 */
			strPage = config.getInitParameter(initParamName);

			// ACTION 専用処理
			String Action = session.getAttribute(Form.ACTION)==null ? "" : (String)session.getAttribute(Form.ACTION);

			ArrayList<?> menu = (ArrayList<?>)request.getSession().getAttribute(Consts.STR_SES_REPSIDE);
			ArrayList<?> rowdisp = (ArrayList<?>)request.getSession().getAttribute("DISP");

			if (Action.length()!=0){
				String ReportSideArray="0";
				String ReportNoArray="1";
				String ReportSide="2002";
				String ReportNo="2010";
				String SendParam = session.getAttribute(Consts.STR_SES_REPORT_SEND_PARAM)==null ? "" : (String)session.getAttribute(Consts.STR_SES_REPORT_SEND_PARAM);

				if (Action.equals("2069")){	// 全店売上

					// メニュー情報から取得
					for(int i = 0;i<rowdisp.size();i++){
						Side m = (Side)menu.get(i);
						if("全体売上".equals(m.getSidename())){
							ReportSideArray=Integer.toString(i);

							ArrayList<?> report = m.getReport();
							for(int k = 0; k< report.size() ;k++){
								Report rep = (Report)report.get(k);
								if ("全店売上".equals(rep.getReport_name())){
									ReportNoArray=Integer.toString(k);
									ReportSide=Integer.toString(rep.getReport_side());
									ReportNo=Integer.toString(rep.getReport_no());
								}
							}
							break;
						}
					}
				}else{	// 売上実績
					// メニュー情報から取得
					for(int i = 0;i<rowdisp.size();i++){
						Side m = (Side)menu.get(i);
						if("実績照会メニュー".equals(m.getSidename())){
							ReportSideArray=Integer.toString(i);

							ArrayList<?> report = m.getReport();
							for(int k = 0; k< report.size() ;k++){
								Report rep = (Report)report.get(k);
								if ("売上実績".equals(rep.getReport_name())){
									ReportNoArray=Integer.toString(k);
									ReportSide=Integer.toString(rep.getReport_side());
									ReportNo=Integer.toString(rep.getReport_no());
								}
							}
							break;
						}
					}
				}

				// 遷移先レポート情報
				session.setAttribute(Consts.STR_SES_REPORT_SIDE_ARRAY,	ReportSideArray);
				session.setAttribute(Consts.STR_SES_REPORT_NO_ARRAY,	ReportNoArray);
				session.setAttribute(Consts.STR_SES_REPORT_SIDE,		ReportSide);
				session.setAttribute(Consts.STR_SES_REPORT_NO,			ReportNo);
				session.setAttribute(Consts.STR_SES_REPORT_SEND_PARAM,	SendParam);
				session.setAttribute(Consts.STR_SES_REPORT_SEND_MODE,	Defines.SendMode.OTHER.toString());
				getServletContext().getRequestDispatcher("/Servlet/Report.do").forward(request,response);
				return;
			}

		} catch (Exception e) {
			/**
			 * 例外処理
			 */
			// スタックトレースの出力
			e.printStackTrace();
			// エラーメッセージの設定
			request.setAttribute(Form.COMMON_MSGTYPE, Form.COMMON_MSGTYPE_ERR);
			// ページ遷移先の設定
			strPage = this.getServletContext().getInitParameter("Page_Error");

		}

		/**
		 * ページ遷移
		 */
		getServletContext().getRequestDispatcher(strPage).forward(request, response);

	}
}
