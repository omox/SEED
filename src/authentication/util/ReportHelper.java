/**
 *
 */
package authentication.util;

import java.util.*;

import javax.servlet.http.*;

import authentication.bean.*;
import authentication.defines.*;

/**
 * @author Omoto_Yuki
 *
 */
public class ReportHelper {

	public ReportHelper() {
		super();
	}

	/**
	 * レポートのオブジェクト取得
	 * @param request
	 * @return レポートオブジェクト
	 */
	public Report getReportObject( HttpServletRequest request ) {

		// セッションの取得
		HttpSession session = request.getSession();

		// メニューの取得
		ArrayList menu = (ArrayList)session.getAttribute(Consts.STR_SES_REPSIDE);

		// レポート分類番号の取得
		int repside = Integer.parseInt((String) session.getAttribute(Consts.STR_SES_REPORT_SIDE));

		// レポート分類番号が一致するレポート配列の取得
		ArrayList report = null;
		for(int i = 0; i < menu.size(); i++){
			Side m = (Side) menu.get(i);
			if ( m.getSide() == repside) {
				report = m.getReport();
				break;
			}
		}

		// レポート番号の取得
		String current = ((String) session.getAttribute(Consts.STR_SES_REPORT_NO));

		// レポート番号が一致するレポート情報の取得
		Report rep = null;
		if (current == null) {
			// レポート番号が未指定の場合
			rep = (Report)report.get(0);
		} else {
			// レポート番号が指定済の場合
			for( int i = 0; i< report.size(); i++ ){
				Report r = (Report)report.get(i);

				if (r.getReport_boolean() && current.equals(Long.toString(r.getReport_no())) ) {
					rep = r;
					break;
				}
			}
		}

		return rep;

	}

}
