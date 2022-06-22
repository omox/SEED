package servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.json.JSONObject;

import common.DefineReport;
import dto.JQEasyModel;

/**
 * Servlet implementation class History
 */
public class History extends HttpServlet {
	private static final long serialVersionUID = -5961333752928159594L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public History() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@SuppressWarnings({ "unchecked" })
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// 文字変換コード設定【重要】
		request.setCharacterEncoding("UTF-8");

		try {
			// パラメータ一覧【確認】
			HashMap<String,String> maps = new HashMap<String,String>();
			Enumeration<String> enums = request.getParameterNames();
			while( enums.hasMoreElements() ) {
				String name = enums.nextElement();
				//System.out.println(name + "=" + request.getParameter( name ));
				maps.put(name, request.getParameter( name ));
			}
			String action	= maps.get(DefineReport.ID_PARAM_ACTION)==null	? DefineReport.ID_PARAM_ACTION_STATUS : maps.get(DefineReport.ID_PARAM_ACTION);
			String url		= maps.get(DefineReport.ID_PARAM_URL)==null	? "" : maps.get(DefineReport.ID_PARAM_URL);

			// セッション
			HttpSession session = request.getSession(false);

			ArrayList<String> urlData = new ArrayList<String>();
			urlData = (ArrayList<String>) session.getAttribute("histroyLists");
			if ( urlData == null) {
				urlData = new ArrayList<String>();
			}
			int historyPosition = session.getAttribute("historyPosition") == null ? 0 :  new Integer((Integer)session.getAttribute("historyPosition")).intValue();
			int historyChange = session.getAttribute("historyChange") == null ? 0 :  new Integer((Integer)session.getAttribute("historyChange")).intValue();

			// 戻り値
			response.setContentType("text/html;charset=UTF-8");
			PrintWriter pw = response.getWriter();

			if (DefineReport.ID_PARAM_ACTION_STATUS.equals(action) ) {
				if (historyChange == 0) {

					// 状況確認urlData.size()
					if (url.indexOf("Report.do") != -1) {

						if (urlData.size() > 1){
							ArrayList<String> urlRec = (ArrayList<String>) urlData.clone();
							urlData.clear();

							int startUrl = 1;
							int lastUrl = urlRec.size();
							if (historyPosition<4){
								startUrl = 0;
								lastUrl = historyPosition;
							}
							for (int i=startUrl; i<lastUrl; i++){
								urlData.add(urlRec.get(i));
							}
						}

						// レポートurl の保存(初回及び前回と同一出ない場合）
						if (urlData.size() == 0 || !url.equals(urlData.get(urlData.size()-1)) ){
							urlData.add(url);
						}
						historyPosition = urlData.size();
						session.setAttribute("histroyLists", urlData);
						session.setAttribute("historyPosition", historyPosition);

						// ボタン判定
						switch(urlData.size()){
						case 1: pw.print(0); break;
						case 2: pw.print(4); break;
						case 3: pw.print(4); break;
						case 4: pw.print(historyPosition); break;
						}

					} else {

						// レポート以外表示
						session.setAttribute("histroyLists", null);
						session.setAttribute("historyPosition", 0);
						pw.print(0);

					}
				} else {
					// ボタン判定
					switch(urlData.size()){
					case 1: pw.print(0); break;
					case 2:
						if(historyPosition == 2) {
							pw.print(4);
						} else {
							pw.print(historyPosition);
						}
						break;
					case 3:
						if(historyPosition == 3) {
							pw.print(4);
						} else {
							pw.print(historyPosition);
						}
						break;
					case 4: pw.print(historyPosition); break;
					}
				}
				session.setAttribute("historyChange", 0);

			} else if (DefineReport.ID_PARAM_ACTION_BACK.equals(action)) {
				// 戻るクリック
				if (historyPosition-1 > 0 && urlData.size() >= historyPosition-1) {
					url = (String)urlData.get(historyPosition-2);
					session.setAttribute("historyPosition", historyPosition-1);
					session.setAttribute("historyChange", 1);
					pw.print(url);
				}

			} else if (DefineReport.ID_PARAM_ACTION_FOWRWARD.equals(action)) {
				// 進むクリック
				if (historyPosition+1 <= 4 && urlData.size() >= historyPosition+1) {
					url = (String)urlData.get(historyPosition);
					session.setAttribute("historyPosition", historyPosition+1);
					session.setAttribute("historyChange", 1);
					pw.print(url);
				}

			}

			pw.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
