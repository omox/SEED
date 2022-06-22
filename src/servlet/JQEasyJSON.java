package servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.iq80.snappy.Snappy;

import common.DefineReport;
import common.RowsLengthLimitUtility;
import dto.JQEasyModel;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

/**
 * Servlet implementation class JQeasyJSON
 */
public class JQEasyJSON extends HttpServlet {
	private static final long serialVersionUID = -5961333752928159592L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public JQEasyJSON() {
		super();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@SuppressWarnings("unchecked")
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// 文字変換コード設定【重要】
		request.setCharacterEncoding("UTF-8");

		// パラメータ一覧【確認】
		HashMap<String, String> map = new HashMap<String, String>();
		Enumeration<String> enums = request.getParameterNames();
		while (enums.hasMoreElements()) {
			String name = enums.nextElement();
			if (DefineReport.ID_DEBUG_MODE)
				System.out.println(name + "=" + request.getParameter(name));
			map.put(name, request.getParameter(name));
		}

		// レポート情報
		String report = request.getParameter("report");
		if ("".equals(report)) {
			// 対象外
			// return;
		}

		// ページングが実装されている画面IDの定義
		ArrayList<String> pagingRep = new ArrayList<String>();
		pagingRep.add(DefineReport.ID_PAGE_TJ001);
		pagingRep.add(DefineReport.ID_PAGE_TJ005);
		pagingRep.add(DefineReport.ID_PAGE_TJ009);
		pagingRep.add(DefineReport.ID_PAGE_TJ011);
		pagingRep.add(DefineReport.ID_PAGE_X241);
		pagingRep.add(DefineReport.ID_PAGE_X243);
		pagingRep.add(DefineReport.ID_PAGE_X244);
		pagingRep.add(DefineReport.ID_PAGE_X245);
		pagingRep.add(DefineReport.ID_PAGE_X246);
		pagingRep.add(DefineReport.ID_PAGE_X249);
		pagingRep.add(DefineReport.ID_PAGE_X250);

		// パラメータ取得
		int page = Integer.parseInt((request.getParameter("page") == null) ? "1" : request.getParameter("page")); // ページ番号
		int limit = Integer.parseInt((request.getParameter("rows") == null) ? "999999" : request.getParameter("rows")); // ページ辺りの表示レコード数
		String sidx = request.getParameter("sort"); // ソート項目
		// String sord = request.getParameter("order"); // ソート順番(asc or desc)
		if (sidx == null)
			sidx = "1";

		// レコード数
		String preRecords = request.getParameter("records");
		if (preRecords == null)
			preRecords = "0";
		int records = Integer.parseInt(preRecords);

		// セッション
		HttpSession session = request.getSession(false);

		// レコード情報の格納先(JSONObject)作成
		JSONObject jsonOB = new JSONObject();

		// jqEasy 用 JSON モデル作成
		JQEasyModel json = new JQEasyModel();

		// 項目単位の情報格納
		List<JSONObject> lineData = new ArrayList<JSONObject>();

		// オプション設定情報
		JSONObject option = (JSONObject) session.getAttribute(DefineReport.ID_SESSION_OPTION);

		if (!((DefineReport.ID_PAGE_X241.equals(report) || DefineReport.ID_PAGE_X243.equals(report))
				&& request.getParameter("rows") != null)) {
			limit = Integer.parseInt(option.getString("rows") == null
					|| ArrayUtils.contains(RowsLengthLimitUtility.defaultValues, option.getInt("rows")) ? "999999"
							: option.getString("rows"));
		}

		// レコードカウント
		int count = -1;

		// セルインデックス
		int index = 0;

		// セッションの保存先
		String sessionTable = DefineReport.ID_SESSION_TABLE;
		if (report != null && DefineReport.ID_PAGE_001.equals(report) && map.get("KBN") != null
				&& !map.get("KBN").isEmpty()) {
			sessionTable = DefineReport.ID_SESSION_TABLE + "2";
		}

		if (session.getAttribute(sessionTable) != null) {

			ArrayList<byte[]> al = (ArrayList<byte[]>) session.getAttribute(sessionTable);
			records = al.size();
			if (records > 0)
				records--; // タイトル行の除外

			// 総レコード数の設定
			if (records <= limit || pagingRep.contains(report)) {
				json.setTotal(records);
			} else {
				// 総レコード数が上限値を超える場合は、トータル値に上限値を設定する。
				if (!pagingRep.contains(report)) {
					json.setTotal(limit);
				}
			}

			// 総ページ数の算出
			int total_pages = 0;
			double ii = (double) records / (double) limit;
			if (records > 0) {
				total_pages = (int) Math.ceil(ii);
			} else {
				total_pages = 0;
			} // if for some

			if (page > total_pages) {
				page = total_pages; // calculate the starting position of the
									// rows
			}

			int start = limit * page - limit; // if for some reasons start
												// position

			if (start < 0) {
				start = 0;
			}

			int state = 0;
			boolean stateFlag = false;
			String states = "";

			Iterator<byte[]> itr = al.iterator();
			// Iterator itr = al.iterator();
			// if (itr.hasNext()) itr.next(); // タイトル情報スキップ

			// 項目単位の情報格納
			lineData = new ArrayList<JSONObject>();

			while (itr.hasNext()) {

				count++;

				// 表示範囲の情報取得
				if (((start + 1) <= count) && (count <= (start + limit))) {

					// jqGrid 用レコード情報準備
					JSONObject n1 = new JSONObject();

					// セル（列）情報リスト
					byte[] bytes = itr.next();
					String[] columnsList = StringUtils.splitPreserveAllTokens(
							new String(Snappy.uncompress(bytes, 0, bytes.length), "UTF-8"), "\t");

					index = 0;

					for (String col : columnsList) {

						index++;
						if (state == index) {
							// easyui.treegrid state : closed
							states = col;
							n1.put("state", states);
							if (!"".equals(states) && states != null) {
								n1.put("iconCls", "icon-ok");
							}
							// itrCols.next(); // 次のセルへ移動
						} else {
							// セル（列）生成
							n1.put("F" + String.valueOf(index), col);
						}

					}

					// 行情報へセル情報を追加
					lineData.add(n1);

				} else if (count == 0) {

					// タイトル
					// セル（列）情報リスト
					byte[] bytes = itr.next();
					String[] columnsList = StringUtils.splitPreserveAllTokens(
							new String(Snappy.uncompress(bytes, 0, bytes.length), "UTF-8"), "\t");

					for (String col : columnsList) {
						state++;
						if ("STATE".equals(col)) {
							stateFlag = true;
							break;
						}
					}
					// state カラムがない場合は、初期化
					if (!stateFlag)
						state = 0;

				} else if (count > (start + limit)) {
					// 取得範囲外
					break;
				} else {
					// 次レコード移動
					itr.next();
				}

			}

			if (DefineReport.ID_DEBUG_MODE)
				System.out.println("flotData size : " + lineData.size());

		} else {
			// 事前検索結果がsessionに保持されていません。
			System.out.println("table属性がsessionに保持されていません。");
		}

		// レコード情報の格納（JSON形式変換用）
		json.setRows(lineData);

		// メッセージの設定
		json.setMessage((String) (session.getAttribute(DefineReport.ID_SESSION_MSG) == null ? ""
				: session.getAttribute(DefineReport.ID_SESSION_MSG)));
		// session.removeAttribute(DefineReport.ID_SESSION_MSG);

		// JSON 形式へ変換
		jsonOB = JSONObject.fromObject(JSONSerializer.toJSON(json));
		if (DefineReport.ID_DEBUG_MODE)
			System.out.println(jsonOB.toString());

		// JSON データのロード
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter pw = response.getWriter();
		pw.print(jsonOB);

	}

}
