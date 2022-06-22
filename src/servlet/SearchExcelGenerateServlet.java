package servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.catalina.connector.ClientAbortException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.iq80.snappy.Snappy;

import authentication.bean.User;
import authentication.defines.Consts;
import common.DefineReport;
import common.Defines;
import dao.ItemInterface;
import dao.ReportBW002Dao;
import dao.ReportSO001Dao;
import dao.ReportSO003Dao;
import dao.ReportST016Dao;
import dao.ReportTG001Dao;
import dao.ReportTG017Dao;
import dao.Reportx001Dao;
import dao.Reportx250Dao;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
/**
 * Servlet implementation class ExcelGenerateServlet
 */
public class SearchExcelGenerateServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public SearchExcelGenerateServlet() {
		super();
	}

	/** 固定値定義（ファイルタイプ）<br>
	 *   */
	public enum FileType {
		/** excel(2007) */
		XLSX("xlsx","Excel"),
		/** csv */
		CSV("csv","CSV"),
		/** 固定長 */
		FIX("fix","FIX");

		private final String val;
		private final String txt;
		/** 初期化 */
		private FileType(String val, String txt) {
			this.val = val;
			this.txt = txt;
		}
		/** @return val 値 */
		public String getVal() { return val; }
		/** @return txt 表示名称 */
		public String getTxt() { return txt; }
	}

	/**
	 * @see HttpServlet#service(HttpServletRequest request, HttpServletResponse response)
	 */
	@SuppressWarnings("unchecked")
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		// 文字変換コード設定【重要】
		request.setCharacterEncoding("UTF-8");

		// パラメータ一覧【確認】
		HashMap<String,String> map = new HashMap<String,String>();
		Enumeration<String> enums = request.getParameterNames();
		while( enums.hasMoreElements() ) {
			String name = enums.nextElement();
			if (DefineReport.ID_DEBUG_MODE) System.out.println(name + "=" + request.getParameter( name ));
			map.put(name, request.getParameter( name ));
		}

		// レポート情報
		String report = request.getParameter("report");
		String kbn = request.getParameter("kbn");

		// セッション情報
		HttpSession session = request.getSession(false);
		String session_prefix = DefineReport.ID_SESSION_PREFIX_TEMP + getClass().getSimpleName();

		InputStream in = null;
		OutputStream out = null;
		String filePath = null;
		// Excel ワークブック
		SXSSFWorkbook wb = null;
		// テキスト
		StringBuffer sb = null;
		try {
			// Excelボタン押下時処理
			if ((session.getAttribute(DefineReport.ID_SESSION_FILE+session_prefix) == null)){

				FileType typ = FileType.CSV;
				// CSVデータ作成
				boolean disptitle = false;
				if(map.containsKey("type") && FileType.FIX.val.equals(map.get("type")) ){
					typ = FileType.FIX;
				}
				if(map.containsKey("type") && FileType.XLSX.val.equals(map.get("type")) ){
					typ = FileType.XLSX;
				}

				int start = 0;
				int limit = 0;
				if (request.getParameter("rows") != null) {
					limit = Integer.parseInt(request.getParameter("rows"));	// ページ辺りの表示レコード数;
				}

				long startWK = System.currentTimeMillis();

				// 検索実行
				// コネクションの取得
				String JNDIname = Defines.STR_JNDI_DS;
				// 検索クラス作成
				if (DefineReport.ID_PAGE_X001.equals(report)) {
					disptitle = true;
					convertItem(new Reportx001Dao(JNDIname), request, map, limit, session, start, session_prefix);
				}else if (DefineReport.ID_PAGE_TG001.equals(report)) {
					convertItem(new ReportTG001Dao(JNDIname), request, map, limit, session, start, session_prefix);

				}else if (DefineReport.ID_PAGE_TG017.equals(report)) {
					convertItem(new ReportTG017Dao(JNDIname), request, map, limit, session, start, session_prefix);

				}else if (DefineReport.ID_PAGE_BW002.equals(report)){
					convertItem(new ReportBW002Dao(JNDIname), request, map, limit, session, start, session_prefix);

				}else if (DefineReport.ID_PAGE_ST016.equals(report)){
					disptitle = true;
					convertItem(new ReportST016Dao(JNDIname), request, map, limit, session, start, session_prefix);

				}else if (DefineReport.ID_PAGE_SO001.equals(report)) {
					convertItem(new ReportSO001Dao(JNDIname), request, map, limit, session, start, session_prefix);

				}else if (DefineReport.ID_PAGE_SO003.equals(report)) {
					convertItem(new ReportSO003Dao(JNDIname), request, map, limit, session, start, session_prefix);
				}else if (DefineReport.ID_PAGE_X250.equals(report)) {
					disptitle = true;
					convertItem(new Reportx250Dao(JNDIname), request, map, limit, session, start, session_prefix);
				}


				String suffix = "";
				if(FileType.XLSX.equals(typ)){
					int frozenCol = 0;						// 何列目の前で固定するか※0始まり
					ExcelGenerateServlet egs = new ExcelGenerateServlet();
					Map<String, JSONObject> format = egs.getReportCellFormatMap();
					wb = egs.createReport00(session, format, frozenCol, null, null);
				}else if(FileType.CSV.equals(typ)){
					sb = createCsv(session, session_prefix, disptitle);
					suffix = "." + typ.getVal();
				}else if(FileType.FIX.equals(typ)){
					sb = createFix(session, session_prefix);
				}

				// 一時ファイル保存フォルダ存在チェック
				File f = new File(getServletContext().getRealPath(Defines.ID_UPLOAD_FILE_PATH));
				if (!f.exists()) {
					//フォルダ作成実行
					f.mkdirs();
				}

				// 一時ファイル名・パスの指定
				// ファイル名
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
				String upfilename = session.getId() + "_" + sdf.format(new Date()) + suffix;
				String upfilepath = getServletContext().getRealPath( Defines.ID_UPLOAD_FILE_PATH + "/" + upfilename);
				// 一時ファイルのパスをセッション保持
				session.setAttribute(DefineReport.ID_SESSION_FILE+session_prefix, upfilepath);
				System.out.println(typ.getTxt() + " SAVE:" + upfilename);

				// 一時ファイル出力
				out = new FileOutputStream(upfilepath);
				if(FileType.XLSX.equals(typ)){
					wb.write(out);
					wb.close();
					wb = null;
				}else if(FileType.CSV.equals(typ)){
					out.write(sb.toString().getBytes("MS932"));
				}else if(FileType.FIX.equals(typ)){
					out.write(sb.toString().getBytes("MS932"));
				}
				out.close();

				long stopWK = System.currentTimeMillis();
				System.out.println(typ.getTxt() + " TIME:" + (stopWK - startWK) + " ms");

				// JSON データのロード
				response.setContentType("text/html;charset=UTF-8");
				PrintWriter pw = response.getWriter();
				pw.print("");
				pw.close();

			// window.open時処理
			}else{

				// 一時ファイルのパス取得
				filePath = (String) session.getAttribute(DefineReport.ID_SESSION_FILE+session_prefix);
				// ファイルの拡張子からファイルタイプ取得
				String suffix = "";
				FileType typ = FileType.FIX;
				if(StringUtils.endsWith(filePath, FileType.CSV.getVal())){
					typ = FileType.CSV;
					suffix = "." +typ.getVal();
				}
				System.out.println(typ.getTxt() + " OPEN:" + filePath);
				in = new FileInputStream(filePath);

				// ファイル名
				String filename = "";
				if ( DefineReport.ID_PAGE_X001.equals(report) ) {
					filename = DefineReport.Download.PAGE_X001.getTxt();

				}else if(DefineReport.ID_PAGE_ST016.equals(report)){
					JSONObject option = (JSONObject) session.getAttribute(DefineReport.ID_SESSION_OPTION + session_prefix);
					filename = option.optString("FILE_NAME");

				}else if (DefineReport.ID_PAGE_TG001.equals(report)) {
					JSONObject option = (JSONObject) session.getAttribute(DefineReport.ID_SESSION_OPTION + session_prefix);
					filename = option.optString("FILE_NAME");

				}else if (DefineReport.ID_PAGE_TG017.equals(report)) {
					JSONObject option = (JSONObject) session.getAttribute(DefineReport.ID_SESSION_OPTION + session_prefix);
					filename = option.optString("FILE_NAME");

				}else if (DefineReport.ID_PAGE_X250.equals(report)) {
					filename = DefineReport.Download.PAGE_X250.getTxt();

				}
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
				filename = filename.replaceAll("[ \\\"\'/:;,*?<>|]", "_") + "_" + sdf.format(new Date()) + suffix;

				//送信
				response.setHeader("Content-Disposition", "attachment;filename="+new String(filename.getBytes("Windows-31J"), "ISO-8859-1"));
				if(FileType.XLSX.equals(typ)){
					response.setContentType("application/vnd.ms-excel");
				}else if(FileType.CSV.equals(typ)){
					response.setContentType("text/csv");
				}else if(FileType.FIX.equals(typ)){
					response.setContentType("text/plain");
				}
				out = response.getOutputStream();
				byte[] buff = new byte[1024];
				int len = 0;
				while ((len = in.read(buff, 0, buff.length)) != -1) {
					out.write(buff, 0, len);
				}

				// 各情報を破棄
				out.close();
				in.close();
				this.deleteFile(filePath);
				// セッションクリア
				this.crearSession(session, session_prefix);
			}

			return;

		} catch (ClientAbortException e) {
			// ダウンロードダイアログでキャンセルされた場合
			// セッションクリア
			this.crearSession(session, session_prefix);

		} catch (Throwable e) {
			e.printStackTrace();

			// 各情報を破棄
			if(wb != null){
				try {
					wb.close();
				} catch (Exception e2) {
				}
			}
			if(out != null){
				IOUtils.closeQuietly(out);
			}
			if(in != null){
				IOUtils.closeQuietly(in);
			}
			if(filePath!=null){
				this.deleteFile(filePath);
			}
			// セッションクリア
			this.crearSession(session, session_prefix);

			// Excel出力失敗時
			String strPage = Defines.STR_EXCEL_ERROR;
			getServletContext().getRequestDispatcher(strPage).forward(request, response);
		}
	}

	/**
	 * セッションクリア
	 * @param session
	 * @param session_prefix
	 * @return
	 */
	private void crearSession(HttpSession session, String session_prefix) {
		session.removeAttribute(DefineReport.ID_SESSION_TABLE + session_prefix);
		session.removeAttribute(DefineReport.ID_SESSION_WHERE + session_prefix);
		session.removeAttribute(DefineReport.ID_SESSION_META + session_prefix);
		session.removeAttribute(DefineReport.ID_SESSION_HEADER + session_prefix);
		session.removeAttribute(DefineReport.ID_SESSION_OPTION + session_prefix);
		session.removeAttribute(DefineReport.ID_SESSION_FILE + session_prefix);

	}

	/**
	 * 分析
	 * @param ItemInterface
	 * @param request
	 * @param map
	 * @param limit
	 * @param session
	 * @param start
	 * @param con
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private void convertItem(ItemInterface shopItem, HttpServletRequest request,
			HashMap<String, String> map, int limit, HttpSession session, int start,String session_prefix) {

		try {
			// セッション情報取得
			User userInfo = (User)request.getSession().getAttribute(Consts.STR_SES_LOGINUSER);

			// ログインユーザー情報セット
			shopItem.setUserInfo(userInfo);
			// 条件セット
			shopItem.setMap(map);
			// 検索条件（Excel出力用）
			shopItem.setJson((String) session.getAttribute(DefineReport.ID_SESSION_STORAGE));
			// 検索開始位置取得
			shopItem.setStart(start);
			// 検索取得数
			shopItem.setLimit(limit);
			// SQL 実行
			shopItem.selectForDL();

			// セッション保持
			session.setAttribute(DefineReport.ID_SESSION_TABLE + session_prefix, shopItem.getTable());
			session.setAttribute(DefineReport.ID_SESSION_WHERE + session_prefix, shopItem.getWhere());
			session.setAttribute(DefineReport.ID_SESSION_META + session_prefix,  shopItem.getMeta());
			session.setAttribute(DefineReport.ID_SESSION_OPTION + session_prefix,  shopItem.getOption());
			session.setAttribute(DefineReport.ID_SESSION_MSG + session_prefix,  shopItem.getMessage());

			if (map.get(DefineReport.ID_SESSION_HEADER) != null) {
				// JSONパラメータの解析
				JSONArray titleArray = (JSONArray) JSONSerializer.toJSON( map.get(DefineReport.ID_SESSION_HEADER) );

				// タイトル情報
				ArrayList<List<String>> title = new ArrayList<List<String>>();

				for(int i=0; i<titleArray.size(); i++){
					// 情報保存
					List<String> row = new ArrayList<String>();
					row = (List<String>) titleArray.get(i);

					List<String> cols = new ArrayList<String>();
					for(int j=0; j<row.size(); j++){
						cols.add(row.get(j));
					}
					title.add(cols);
				}

				// セッション保持
				session.setAttribute(DefineReport.ID_SESSION_HEADER + session_prefix, title);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Session から情報取得(CSV)
	 * @param session
	 * @param al
	 * @param title
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	private void getSessionResultCsv(HttpSession session, String session_prefix, ArrayList<List<String>> al, ArrayList<String> title, boolean dispTitle) throws IOException {

		// 検索結果取得
		int start = 1;

		if (session.getAttribute(DefineReport.ID_SESSION_HEADER + session_prefix) != null) {
			ArrayList<List<String>> al1 = (ArrayList<List<String>>) session.getAttribute(DefineReport.ID_SESSION_HEADER + session_prefix);
			// タイトル情報取得
			title.addAll(al1.get(al1.size() - 1));
			if(dispTitle){
				al.addAll(al1);
			}
		}

		ArrayList<byte[]> al2 = (ArrayList<byte[]>) session.getAttribute(DefineReport.ID_SESSION_TABLE + session_prefix);

		if(title.isEmpty() && al2.size() > 0){
			int i = 0;
			title.addAll(Arrays.asList(StringUtils.splitPreserveAllTokens(new String( Snappy.uncompress(al2.get(i), 0, al2.get(i).length), "UTF-8"), "\t")));
			if(dispTitle){
				start = 0;
			}
		}

		// 本文取得
		for (int i = start; i < al2.size(); i++) {
			al.add(Arrays.asList(StringUtils.splitPreserveAllTokens(new String( Snappy.uncompress(al2.get(i), 0, al2.get(i).length), "UTF-8"), "\t")));
		}
	}

	/**
	 * createCsv<br/>
	 * <br/>
	 * @param session		セッション
	 * @param disptitle		タイトル行表示・非表示
	 * @return StringBuffer
	 * @throws IOException 例外
	 */
	private StringBuffer createCsv(HttpSession session, String session_prefix, boolean disptitle) throws IOException {
		StringBuffer sb = new StringBuffer();

		// Session 情報格納用
		ArrayList<List<String>> al = new ArrayList<List<String>>();;	// 検索結果
		ArrayList<String> title = new ArrayList<String>();;				// タイトル
		// Session から検索結果取得
		getSessionResultCsv(session, session_prefix, al, title, disptitle);

		int endTitleCol = title.size() - 1;						// タイトル部の終了列

		Iterator<List<String>> itr = al.iterator();
		while (itr.hasNext()) {
			List<String> colList = itr.next();
			List<String> valList = new ArrayList<>();
			Iterator<String> itrCols = colList.iterator();
			int index = -1;
			while (itrCols.hasNext()) {
				index++;
				String val = (String) itrCols.next();
				if(index <= endTitleCol){
					valList.add(StringUtils.remove(val, "\n"));
				}
			}
			sb.append(StringUtils.join(valList.toArray(new Object[valList.size()]), ","));
			sb.append(System.getProperty("line.separator"));
		}
		return sb;
	}

	/**
	 * Session から情報取得(FIX)<br>
	 * SQL作成の際にそのまま出力できるようにデータが用意されている前提
	 * @param session
	 * @param al
	 * @param title
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	private void getSessionResultFix(HttpSession session, String session_prefix, ArrayList<List<String>> al) throws IOException {

		// 検索結果取得
		int start = 0;

		ArrayList<byte[]> al2 = (ArrayList<byte[]>) session.getAttribute(DefineReport.ID_SESSION_TABLE + session_prefix);

		// 本文取得
		for (int i = start; i < al2.size(); i++) {
			al.add(Arrays.asList(StringUtils.splitPreserveAllTokens(new String( Snappy.uncompress(al2.get(i), 0, al2.get(i).length), "UTF-8"), "\t")));
		}
	}

	/**
	 * createFix<br/>
	 * SQL作成の際にそのまま出力できるようにデータが用意されている前提
	 * <br/>
	 * @param session		セッション
	 * @param disptitle		タイトル行表示・非表示
	 * @return StringBuffer
	 * @throws IOException 例外
	 */
	private StringBuffer createFix(HttpSession session, String session_prefix) throws IOException {
		StringBuffer sb = new StringBuffer();

		// Session 情報格納用
		ArrayList<List<String>> al = new ArrayList<List<String>>();;	// 検索結果
		// Session から検索結果取得
		getSessionResultFix(session, session_prefix, al);

		int endTitleCol = 1;						// タイトル部の終了列

		Iterator<List<String>> itr = al.iterator();
		while (itr.hasNext()) {
			List<String> colList = itr.next();
			Iterator<String> itrCols = colList.iterator();
			int index = -1;
			while (itrCols.hasNext()) {
				index++;
				String val = (String) itrCols.next();
				if(index <= endTitleCol){
					sb.append(val);
				}
			}
			sb.append(System.getProperty("line.separator", "\r\n"));
		}
		return sb;
	}
	private void deleteFile(String filepath) {
		//削除対象ファイル
		File file = new File(filepath);
		if(file.delete()){
			//ファイル削除成功
			System.out.println("ファイル削除成功:"+filepath);
		}else{
			//ファイル削除失敗
			System.out.println("ファイル削除失敗:"+filepath);
		}
	}
}
