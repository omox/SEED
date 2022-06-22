package authentication.servlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import authentication.bean.Info;
import authentication.defines.Consts;
import authentication.defines.Form;

public class InfoViewServlet extends HttpServlet{
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

		Info info = new Info();

		try {
			info.setTitle(request.getParameter(Form.MTN_INFO_VIEW_TITLE).toString().trim());
			info.setInformation(request.getParameter(Form.MTN_INFO_VIEW_INFO).toString().trim());

			request.setAttribute(Consts.STR_REQ_REC, info);

		} catch (Exception e) {
			e.printStackTrace();

			// 表示内容を空文字に設定
			info.setTitle("");
			info.setInformation("");
		}

		request.setAttribute(Consts.STR_REQ_REC, info);

		/**
		 * ページ遷移
		 */
		String strPage = config.getInitParameter("InfoView");
		getServletContext().getRequestDispatcher(strPage).forward(request, response);
	}
}
