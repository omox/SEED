/*
 * 作成日: 2007/10/26
 *
 */
package authentication.filter;

import java.io.*;
import javax.servlet.*;

/**
 * システム全体で使用する文字コードを設定する
 */
public class StringFilter implements Filter {
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
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws ServletException, IOException {

		request.setCharacterEncoding(config.getInitParameter("default_encofing"));
		chain.doFilter(request, response);

	}
}