/*
 * 作成日: 2007/10/26
 *
 */
package authentication.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import authentication.bean.User;
import authentication.defines.Consts;
import authentication.defines.Form;
import authentication.defines.Message;

import common.ChkUsableTime;

/**
 * セッション状態をチェックし、システムへログインしているかをチェックする
 * セッションが切れている場合は、ログイン画面へ強制的に遷移させ、
 * セッションが継続している場合は処理を継続させる。
 */
public class LoginFilter implements Filter {
	private FilterConfig config = null;

	/**
	 * 初期化
	 */
	public void init(FilterConfig config) throws ServletException {
		this.config = config;
	}

	/**
	 * 破棄
	 */
	public void destroy() {
	}

	/**
	 * リクエスト時フィルター処理
	 */
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		HttpSession session = httpRequest.getSession(false);

		// 遷移先
		String strPage = "Page_Error";

		/* セッションチェック */
		if (session != null) {
			// ユーザーID
			String userid = "";
			if (session.getAttribute(Consts.STR_SES_LOGINUSER) != null) {
				User user = (User) session.getAttribute(Consts.STR_SES_LOGINUSER);
				userid = user.getId();
			}

			// web.xmlからサイト利用時間を取得
			String fromData	= config.getServletContext().getInitParameter(Consts.FROM_DATA);
			String toData	= config.getServletContext().getInitParameter(Consts.TO_DATA);
			/* 利用可能時間チェック */
			ChkUsableTime sys = new ChkUsableTime(fromData, toData);
			if ( sys.isCloseTime(userid) || sys.isWaitTime(userid) ){
				// 利用時間外
				strPage = "Login";

			} else if (session.getAttribute(Consts.STR_SES_LOGINUSER) != null) {
				chain.doFilter(httpRequest, httpResponse);
				return;
			}
		}

		// セッションなし：ログイン画面へ⇒別ウインドウ方式なので、エラー画面に遷移
		/* エラーメッセージの設定 */
		httpRequest.setAttribute(Form.COMMON_MSGTYPE, Form.COMMON_MSGTYPE_ERR);
		httpRequest.setAttribute(Form.COMMON_MSG, Message.ERR_SES_TIMEOUT);
		httpRequest.getRequestDispatcher(config.getInitParameter(strPage)).forward(request, response);
	}
}