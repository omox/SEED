package servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.iq80.snappy.Snappy;

import common.DefineReport;

/**
 * Servlet implementation class JQEasyUtil
 */
public class JQEasyUtil extends HttpServlet {
	private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public JQEasyUtil() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@SuppressWarnings("unchecked")
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// 文字変換コード設定【重要】
		request.setCharacterEncoding("UTF-8");

		// パラメータ一覧【確認】
		HashMap<String,String> map = new HashMap<String,String>();
		Enumeration<String> enums = request.getParameterNames();
		while( enums.hasMoreElements() ) {
			String name = enums.nextElement();
			//System.out.println(name + "=" + request.getParameter( name ));
			map.put(name, request.getParameter( name ));
		}

		// レポート情報
		String report = request.getParameter("report");

		// パラメータ取得
		int limit = Integer.parseInt((request.getParameter("rows")==null)? "999999" : request.getParameter("rows"));	// ページ辺りの表示レコード数
		String key = request.getParameter("key");	// 項目名
		String val = request.getParameter("val");	// 検索値
		int records = 0;

		// セッション
		HttpSession session = request.getSession(false);

		// レコードカウント
		int count = -1;

		// セルインデックス
		int index = 0;

		// ページ番号
		int page = 0;

		// セッションの保存先
		String sessionTable = DefineReport.ID_SESSION_TABLE;
		if (report != null && DefineReport.ID_PAGE_001.equals(report) && map.get("KBN") != null && !map.get("KBN").isEmpty()) {
			sessionTable = DefineReport.ID_SESSION_TABLE+"2";
		}
		//System.out.println("データ格納先（JQEasyJSON）："+sessionTable);
		if (session.getAttribute(sessionTable) != null) {

			ArrayList<byte[]> al = (ArrayList<byte[]>) session.getAttribute(sessionTable);

			Iterator<byte[]> itr = al.iterator();

			while (itr.hasNext()) {

				count++;

				// セル（列）情報リスト
				byte[] bytes = itr.next();
				String[] columnsList = StringUtils.splitPreserveAllTokens( new String(Snappy.uncompress(bytes, 0, bytes.length), "UTF-8"), "\t");

				index=0;

				for(String col : columnsList){
					index++;
					// セル（列）確認
					if (("F"+String.valueOf(index)).equals(key) && col.equals(val)) {
						records = count;
						break;
					}
				}
				if (records>0)	break;	// 対象レコードあり

			}

			// 対象ページ数の算出
			double target_page = (double)records/(double)limit;
			if( records > 0 ) {
				page = (int) Math.ceil(target_page);
			} else {
				page = 0;
			} // if for some

			//System.out.println("target record : " + records + "\ttarget page : " + page );


		} else {
			System.out.println("table属性がsessionに保持されていません。");
		}

		response.setContentType("text/html;charset=UTF-8");
		response.getWriter().append(String.valueOf(page));
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
