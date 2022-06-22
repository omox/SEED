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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.catalina.connector.ClientAbortException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.iq80.snappy.Snappy;

import common.DefineReport;
import common.Defines;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

/**
 * Servlet implementation class ExcelGenerateServlet
 */
public class ExcelGenerateServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ExcelGenerateServlet() {
		super();
	}

	/** 固定値定義（ファイルタイプ）<br>
	 *   */
	public enum FileType {
		/** excel(2007) */
		XLSX("xlsx","Excel"),
		/** csv */
		CSV("csv","CSV");

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
			map.put(name, request.getParameter( name ));
		}

		// セッション情報
		HttpSession session = request.getSession(false);

		if ( map.get(DefineReport.ID_SESSION_HEADER) != null ){

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
			session.setAttribute(DefineReport.ID_SESSION_HEADER, title);

		}

		if ( map.get(DefineReport.ID_SESSION_TABLE) != null ){

			// JSONパラメータの解析
			JSONArray mapArray = (JSONArray) JSONSerializer.toJSON( map.get(DefineReport.ID_SESSION_TABLE) );

			// DB検索結果（0レコード＝タイトル）
			ArrayList<byte[]> table = new ArrayList<byte[]>();

			for(int i=0; i<mapArray.size(); i++){
				// 情報保存
				List<String> row = new ArrayList<String>();
				row = (List<String>) mapArray.get(i);

				StringBuffer cols = new StringBuffer();
				for(int j=0; j<row.size(); j++){
					cols.append(row.get(j)+"\t");
				}
				table.add(Snappy.compress(cols.toString().getBytes("UTF-8")));
			}

			// セッション保持
			session.setAttribute(DefineReport.ID_SESSION_TABLE, table);

		}
		if ( map.get(DefineReport.ID_SESSION_META) != null ){
			// JSONパラメータの解析
			JSONArray metaArray = (JSONArray) JSONSerializer.toJSON( map.get(DefineReport.ID_SESSION_META) );

			// メタ情報
			ArrayList<Integer> meta = new ArrayList<Integer>();

			for(int i=0; i<metaArray.size(); i++){
				// 情報保存
				meta.add((Integer)metaArray.get(i));
			}

			// セッション保持
			session.setAttribute(DefineReport.ID_SESSION_META, meta);

		}

		if ( map.get(DefineReport.ID_SESSION_OPT_TABLE) != null ){
			// JSONパラメータの解析
			JSONArray dataArray = (JSONArray) JSONSerializer.toJSON( map.get(DefineReport.ID_SESSION_OPT_TABLE) );

			// セッション保持
			session.setAttribute(DefineReport.ID_SESSION_OPT_TABLE, dataArray);
		}

		InputStream in = null;
		OutputStream out = null;
		String filePath = null;
		// Excel ワークブック
		SXSSFWorkbook wb = null;
		// テキスト
		StringBuffer sb = null;
		try {

			// Excelボタン押下時処理
			if (	(map.get(DefineReport.ID_SESSION_HEADER) != null)
					||	(map.get(DefineReport.ID_SESSION_TABLE) != null)
					||	(map.get(DefineReport.ID_SESSION_META) != null)){

				//// セッションに一時ファイルのパスが存在する場合（すでに実行済みの場合）何もせず
				//if(session.getAttribute(DefineReport.ID_SESSION_FILE) == null){
				// レポート情報
				String report = map.get("report");
				String kbn = map.get("kbn");
				FileType typ = FileType.XLSX;
				if(map.containsKey("type") && FileType.CSV.val.equals(map.get("type")) ){
					typ = FileType.CSV;
				}

				long start = System.currentTimeMillis();

				if(FileType.XLSX.equals(typ)){
					int frozenCol = 0;						// 何列目の前で固定するか※0始まり
					if ( DefineReport.ID_PAGE_001.equals(report) ) {
						Map<String, JSONObject> format = this.getReportCellFormatMap();
						wb = createReport00(session, format, frozenCol, null, new int[]{0,1});
					} else if ( DefineReport.ID_PAGE_X231.equals(report) ) {
						Map<String, JSONObject> format = this.getReportCellFormatMap2();
						wb = createReport00(session, format, frozenCol, null, new int[]{});
					} else {
						if(kbn != null){
							String[] prms = StringUtils.split(kbn,",");
							if(prms.length > 0){
								frozenCol = NumberUtils.toInt(prms[0], 0);						// 何列目の前で固定するか※0始まり
							}
						}
						Integer keyCol = getKamokuColNo(session);
						if(keyCol == null){
							Map<String, JSONObject> format = this.getReportCellFormatMap();
							wb = createReport00(session, format, frozenCol, null, null);
						}else{
							Map<String, JSONObject> formatR = this.getReportCellFormatMap();
							Map<String, JSONObject> formatC = this.getReportCellFormatMap();
							wb = createReport99(session, formatR, formatC, frozenCol, keyCol, null, null);
						}
					}
				}else if(FileType.CSV.equals(typ)){
					// CSVデータ作成
					boolean disptitle = true;
					sb = createCsv(session, disptitle);

				}
				long stop = System.currentTimeMillis();
				System.out.println(typ.getTxt() + " TIME:" + (stop - start) + " ms");

				// 一時ファイル保存フォルダ存在チェック
				File f = new File(getServletContext().getRealPath(Defines.ID_UPLOAD_FILE_PATH));
				if (!f.exists()) {
					//フォルダ作成実行
					f.mkdirs();
				}

				// 一時ファイル名・パスの指定
				// ファイル名
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
				String upfilename = session.getId() + "_" + sdf.format(new Date()) + "." + typ.getVal();
				String upfilepath = getServletContext().getRealPath( Defines.ID_UPLOAD_FILE_PATH + "/" + upfilename);
				// 一時ファイルのパスをセッション保持
				session.setAttribute(DefineReport.ID_SESSION_FILE, upfilepath);
				System.out.println(typ.getTxt() + " SAVE:" + upfilename);

				// 一時ファイル出力
				out = new FileOutputStream(upfilepath);
				if(FileType.XLSX.equals(typ)){
					wb.write(out);
					wb.dispose();
					wb.close();
					wb = null;
				}else if(FileType.CSV.equals(typ)){
					out.write(sb.toString().getBytes());
				}
				out.close();
				//}

				// JSON データのロード
				response.setContentType("text/html;charset=UTF-8");
				PrintWriter pw = response.getWriter();
				pw.print("");
				pw.close();

			// window.open時処理
			}else{

				// 一時ファイルのパス取得
				filePath = (String) session.getAttribute(DefineReport.ID_SESSION_FILE);
				// ファイルの拡張子からファイルタイプ取得
				FileType typ = FileType.XLSX;
				if(StringUtils.endsWith(filePath, FileType.CSV.val) ){
					typ = FileType.CSV;
				}
				System.out.println(typ.getTxt() + " OPEN:" + filePath);
				in = new FileInputStream(filePath);

				// レポート名取得（検索条件の1行目より）
				ArrayList<List<String>> wh = (ArrayList<List<String>>) session.getAttribute(DefineReport.ID_SESSION_WHERE);
				String filename = wh.get(0).get(0);
				// ファイル名
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
				filename = filename.replaceAll("[ \\\"\'/:;,*?<>|]", "_") + "_" + sdf.format(new Date()) + "." + typ.getVal();

				//送信
				response.setHeader("Content-Disposition", "attachment;filename="+new String(filename.getBytes("Windows-31J"), "ISO-8859-1"));
				if(FileType.XLSX.equals(typ)){
					response.setContentType("application/vnd.ms-excel");
				}else if(FileType.CSV.equals(typ)){
					response.setContentType("text/csv");
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
				session.removeAttribute(DefineReport.ID_SESSION_FILE);
			}

			return;

		} catch (ClientAbortException e) {
			// ダウンロードダイアログでキャンセルされた場合

		} catch (Throwable e) {
			e.printStackTrace();

			// 各情報を破棄
			if(wb != null){
				try {
					wb.dispose();
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
			session.removeAttribute(DefineReport.ID_SESSION_FILE);

			// Excel出力失敗時
			String strPage = Defines.STR_EXCEL_ERROR;
			getServletContext().getRequestDispatcher(strPage).forward(request, response);
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
	private void getSessionResultCsv(HttpSession session, ArrayList<List<String>> al, ArrayList<String> title, boolean dispTitle) throws IOException {

		// 検索結果取得
		int start = 0;

		if (session.getAttribute(DefineReport.ID_SESSION_HEADER) != null) {
			ArrayList<List<String>> al1 = (ArrayList<List<String>>) session.getAttribute(DefineReport.ID_SESSION_HEADER);
			// タイトル情報取得
			title.addAll(al1.get(al1.size() - 1));
			al.addAll(al1);
			start = 1;
		}

		ArrayList<byte[]> al2 = (ArrayList<byte[]>) session.getAttribute(DefineReport.ID_SESSION_TABLE);
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
	private StringBuffer createCsv(HttpSession session, boolean disptitle) throws IOException {
		StringBuffer sb = new StringBuffer();

		// Session 情報格納用
		ArrayList<List<String>> al = new ArrayList<List<String>>();;	// 検索結果
		ArrayList<String> title = new ArrayList<String>();;				// タイトル
		// Session から検索結果取得
		getSessionResultCsv(session, al, title, disptitle);

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
	 * Session から情報取得
	 * @param session
	 * @param wh
	 * @param al
	 * @param title
	 * @param titleJArray
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	private void getSessionResult(HttpSession session, ArrayList<List<String>> wh, ArrayList<List<String>> al,
			ArrayList<String> title, JSONArray titleJArray) throws IOException {
		// 検索条件取得
		if (session.getAttribute(DefineReport.ID_SESSION_WHERE) != null) {
			ArrayList<List<String>> where = (ArrayList<List<String>>) session.getAttribute(DefineReport.ID_SESSION_WHERE);
			wh.addAll(where);
		}

		// 検索結果取得
		int countTitleRow = 1;
		int start = 0;

		if (session.getAttribute(DefineReport.ID_SESSION_HEADER) != null) {
			ArrayList<List<String>> al1 = (ArrayList<List<String>>) session.getAttribute(DefineReport.ID_SESSION_HEADER);
			countTitleRow = al1.size();
			// タイトル情報取得
			title.addAll(al1.get(al1.size() - 1));
			al.addAll(al1);
			start = 1;
		}

		ArrayList<byte[]> al2 = (ArrayList<byte[]>) session.getAttribute(DefineReport.ID_SESSION_TABLE);
		for (int i = start; i < al2.size(); i++) {
			al.add(Arrays.asList(StringUtils.splitPreserveAllTokens(new String( Snappy.uncompress(al2.get(i), 0, al2.get(i).length), "UTF-8"), "\t")));
		}

		if (session.getAttribute(DefineReport.ID_SESSION_HEADER) == null) {
			// タイトル情報取得
			title.addAll(al.get(0));
		}

		// 格納
		for (int i = 0; i < countTitleRow; i++) {
			if(i<al.size()){
				titleJArray.add(al.get(i));
			}
		}

	}

	/** デフォルト列幅設定 */
	private static int DEF_COL_WIDTH = 12;
	/** タイトル行高さ設定 */
	private static float TIT_ROW_HEIGHT = 24;

	/**
	 * 計セル判断用文言
	 */
	private static String KEI_KEYWORD = "月間計";

	/**
	 * フォーマット識別の際に、対象テキストから除外する文言
	 */
	private static String[] REMOVE_KEYWORD = {"\n","累計","_累計"};

	/**
	 * フォーマット識別用文字列の検索方向
	 */
	protected enum KEY_FIND_WAY{
		/** 後方一致検索 */
		AFTER(1),
		/** 前方一致検索 */
		BEFORE(2);
		private final int val;
		/** 初期化 */
		private KEY_FIND_WAY(int val) {
			this.val = val;
		}
		/** @return val 値 */
		public int getVal() { return val; }
	}

	protected Map<String, JSONObject> getReportCellFormatMap() throws IOException {
		return getReportCellFormatMap("#,##0.0;[Red]-#,##0.0", KEY_FIND_WAY.AFTER.getVal());
	}
	protected Map<String, JSONObject> getReportCellFormatMap2() throws IOException {
		return getReportCellFormatMap2("#,##0.0;[Red]-#,##0.0", KEY_FIND_WAY.AFTER.getVal());
	}
	protected Map<String, JSONObject> getReportCellFormatMapGTJ() throws IOException {
		return getReportCellFormatMap("#,##0.00;[Red]-#,##0.00", KEY_FIND_WAY.AFTER.getVal());
	}
	protected Map<String, JSONObject> getReportCellFormatMap(String dotFormat, int keyFindWay) throws IOException {
		Map<String, JSONObject> format= new LinkedHashMap<String, JSONObject>();
		format.put("前年比", this.getReportCellFormat(Cell.CELL_TYPE_NUMERIC, dotFormat, CellStyle.ALIGN_RIGHT, keyFindWay, 8, null));
		format.put("支持率,一人買上点数", this.getReportCellFormat(Cell.CELL_TYPE_NUMERIC, "#,##0.00;[Red]-#,##0.00", CellStyle.ALIGN_RIGHT, keyFindWay, 8, null));
		format.put("率,比,率前年差,比前年差,点数/人,点数/人前年差", this.getReportCellFormat(Cell.CELL_TYPE_NUMERIC, dotFormat, CellStyle.ALIGN_RIGHT, keyFindWay, 8, null));
		format.put("数,数量,額,金,売上,高,費,価,実績,予算,予算案,価計,差異,差,(円),仕入,棚卸,出荷,移動,割戻し,在庫,ロス,（平均）,日商,計画値（売上金額）,計画値（売上点数）,計画値（荒利高）,荒利合計", this.getReportCellFormat(Cell.CELL_TYPE_NUMERIC, "#,##0;[Red]-#,##0", CellStyle.ALIGN_RIGHT, keyFindWay, 10, null));
		format.put("商品名,名称", this.getReportCellFormat(Cell.CELL_TYPE_STRING, "@", CellStyle.ALIGN_LEFT, keyFindWay, 20, null));
		format.put("部,店舗,部門,分類,グループ", this.getReportCellFormat(Cell.CELL_TYPE_STRING, "@", CellStyle.ALIGN_LEFT, keyFindWay, 20, null));
		format.put("項目,科目", this.getReportCellFormat(Cell.CELL_TYPE_STRING, "@", CellStyle.ALIGN_LEFT, keyFindWay, 15, null));
		format.put("前年日付", this.getReportCellFormat(Cell.CELL_TYPE_STRING, "@", CellStyle.ALIGN_LEFT, keyFindWay, 14, null));
		format.put("日付", this.getReportCellFormat(Cell.CELL_TYPE_STRING, "@", CellStyle.ALIGN_LEFT, keyFindWay, 10, null));
		format.put("気温", this.getReportCellFormat(Cell.CELL_TYPE_NUMERIC, "0", CellStyle.ALIGN_RIGHT, keyFindWay, 3, null));
		format.put("天気", this.getReportCellFormat(Cell.CELL_TYPE_STRING, "@", CellStyle.ALIGN_LEFT, keyFindWay, 12, null));
		format.put("商品コード,商品", this.getReportCellFormat(Cell.CELL_TYPE_NUMERIC, "#", CellStyle.ALIGN_RIGHT, keyFindWay, 12, null));
		format.put("順位,№", this.getReportCellFormat(Cell.CELL_TYPE_NUMERIC, "@", CellStyle.ALIGN_RIGHT, keyFindWay, 6, null));
		format.put("要因", this.getReportCellFormat(Cell.CELL_TYPE_STRING, "@", CellStyle.ALIGN_LEFT, KEY_FIND_WAY.BEFORE.getVal(), 30, null));
		return format;
	}
	protected Map<String, JSONObject> getReportCellFormatMap2(String dotFormat, int keyFindWay) throws IOException {
		Map<String, JSONObject> format= new LinkedHashMap<String, JSONObject>();
		format.put("商品コード", this.getReportCellFormat(Cell.CELL_TYPE_STRING, "@", CellStyle.ALIGN_LEFT, keyFindWay, 11, null));
		format.put("商品名称", this.getReportCellFormat(Cell.CELL_TYPE_STRING, "@", CellStyle.ALIGN_LEFT, keyFindWay, 65, null));
		format.put("催し区分", this.getReportCellFormat(Cell.CELL_TYPE_STRING, "@", CellStyle.ALIGN_LEFT, keyFindWay, 20, null));
		format.put("発注数", this.getReportCellFormat(Cell.CELL_TYPE_NUMERIC, "#,##0;[Red]-#,##0", CellStyle.ALIGN_RIGHT, keyFindWay, 10, null));
		format.put("納品日", this.getReportCellFormat(Cell.CELL_TYPE_STRING, "@", CellStyle.ALIGN_CENTER, keyFindWay, 14, null));
		format.put("エラー区分", this.getReportCellFormat(Cell.CELL_TYPE_STRING, "@", CellStyle.ALIGN_LEFT, keyFindWay, 10, null));
		format.put("エラーメッセージ", this.getReportCellFormat(Cell.CELL_TYPE_STRING, "@", CellStyle.ALIGN_LEFT, keyFindWay, 50, null));
		format.put("センターコード", this.getReportCellFormat(Cell.CELL_TYPE_STRING, "@", CellStyle.ALIGN_LEFT, keyFindWay, 10, null));
		format.put("仕入先コード", this.getReportCellFormat(Cell.CELL_TYPE_STRING, "@", CellStyle.ALIGN_LEFT, keyFindWay, 11, null));
		return format;
	}

	/**
	 * createReport用のセル書式作成<br/>
	 * ※デフォルトは文字列形式想定<br/>
	 *
	 * @param cellType	セルの分類
	 * @param format	セルの表示形式
	 * @param align		セルの配置
	 * @param len		値の文字数※幅調整に使用
	 * @param bcColor	セルの背景色
	 * @return SXSSFWorkbook
	 * @throws IOException 例外
	 */
	private JSONObject getReportCellFormat(Integer cellType, String format, Short align, Integer keyFindWay, Integer len, IndexedColors bcColor) throws IOException {
		String cellFormat = "";
		// 必須
		if(cellType == null){
			cellType = Cell.CELL_TYPE_STRING;
		}
		cellFormat += "Type:"+cellType;
		if(StringUtils.isEmpty(format)){
			format = "@";
		}
		cellFormat += ",Format:\""+format+"\"";
		if(align == null){
			align = CellStyle.ALIGN_LEFT;
		}
		cellFormat += ",Align:"+align;

		// オプション
		if(keyFindWay != null){
			cellFormat +=  ",FindWay:"+keyFindWay;
		}
		if(len != null){
			cellFormat += ",Width:" + 256*len;
		}
		if(bcColor != null){
			cellFormat += ",BcColor:"+bcColor.getIndex();
		}
		return JSONObject.fromObject("{"+cellFormat+"}");
	}

	/**
	 * getKamokuColNo<br/>
	 * @param session		セッション
	 * @return SXSSFWorkbook
	 * @throws IOException 例外
	 */
	@SuppressWarnings("unchecked")
	protected Integer getKamokuColNo(HttpSession session) throws IOException {
		if (session.getAttribute(DefineReport.ID_SESSION_HEADER) == null) {
			return null;
		}
		// 検索結果取得
		ArrayList<List<String>> al1 = (ArrayList<List<String>>) session.getAttribute(DefineReport.ID_SESSION_HEADER);
		ArrayList<String> title = new ArrayList<String>(al1.get(al1.size() - 1));
		Integer colNo = null;
		colNo = title.indexOf(DefineReport.ValHyo.VAL14.getTxt());
		if(colNo != -1) return colNo;
		colNo = title.indexOf(DefineReport.ValHyo.VAL14.getTxt()+"名");
		if(colNo != -1) return colNo;
		colNo = title.indexOf(DefineReport.ValHyo.VAL15.getTxt());
		if(colNo != -1) return colNo;
		colNo = title.indexOf(DefineReport.ValHyo.VAL15.getTxt()+"名");
		if(colNo != -1) return colNo;
		return null;
	}

	/**
	 * createReport00<br/>
	 * <br/>
	 * @param session		セッション
	 * @param frozenCol		固定列Index
	 * @param keiCols		合計行判断列Index
	 * @param hideCols		hidden列Index
	 * @return SXSSFWorkbook
	 * @throws IOException 例外
	 */
	public SXSSFWorkbook createReport00(HttpSession session, Map<String, JSONObject> format, int frozenCol, int[] keiCols, int[] hideCols) throws IOException {
		// Session 情報格納用
		ArrayList<List<String>> wh = new ArrayList<List<String>>();		// 条件
		ArrayList<List<String>> al = new ArrayList<List<String>>();;	// 検索結果
		ArrayList<String> title = new ArrayList<String>();;				// タイトル
		JSONArray titleJArray = new JSONArray();						// 検索結果（JSONArray版）
		// Session から検索結果取得
		getSessionResult(session, wh, al, title, titleJArray);

		return createReport00(session, wh, al, title, titleJArray, format, frozenCol, KEY_FIND_WAY.AFTER.getVal(), keiCols, hideCols);
	}

	/**
	 * createReport00<br/>
	 * <br/>
	 * Excel作成処理(シート一枚・フォーマット指定(タイトル参照))<br/>
	 * 取得情報を元に、EXCELファイルを作成。<br/>
	 * 出力シート１枚に対し、全情報記述<br/>
	 * <br/>
	 * フォーマット指定について<br/>
	 * 1.formatにkey値が列名と部分一致したフォーマットがあった場合使用（後方一致/前方一致は指定）<br/>
	 *
	 * 2016.11.29 srcCell参照がXLSXでエラーになるため、処理削除, srcCell前提のlastColor,loopRowCountも廃止<br/>
	 * 2017.01.10 メモリ軽減のため、SXSSF形式に変更。SXSSF形式では作成済みの行の値変更に制限があるため、テンプレートファイル方式廃止<br/>
	 *
	 * @param session		セッション
	 * @param filePath		ファイルパス
	 * @param wh			検索条件
	 * @param al			検索結果
	 * @param title			タイトル部
	 * @param titleJArray	タイトル情報
	 * @param format		フォーマットオプション
	 * @param lastLowColor	最終行色番号
	 * @param loopRowCount	複数行1セットのレイアウトの行数指定
	 * @param frozenCol		固定列Index
	 * @param keyFindWay	後方一致/前方一致
	 * @param keiCols		合計行判断列Index
	 * @param hideCols		hidden列Index
	 * @return SXSSFWorkbook
	 * @throws IOException
	 */
	private SXSSFWorkbook createReport00(HttpSession session, ArrayList<List<String>> wh,
			ArrayList<List<String>> al, ArrayList<String> title, JSONArray titleJArray, Map<String, JSONObject> format,
			int frozenCol, int keyFindWay, int[] keiCols, int[] hideCols) throws IOException {
		System.out.println(getClass().getSimpleName()+".createRepor00t");
//		// Excel ワークブック読み込み
//		InputStream is = new FileInputStream(getServletContext().getRealPath(filePath));
//		XSSFWorkbook wb = new XSSFWorkbook(is);
//		// シート読み込み
//		XSSFSheet sheet = wb.getSheetAt(0);

		// SXSSF形式だとテンプレートはコピーできないため、新規作成
		SXSSFWorkbook wb = new SXSSFWorkbook();
		SXSSFSheet sheet = wb.createSheet();
		PrintSetup ps = sheet.getPrintSetup();
	    ps.setFitWidth((short)1);
	    ps.setFitHeight((short)0);
		ps.setLandscape(true);

		sheet.setDisplayGridlines(false);
		sheet.setDefaultColumnWidth(DEF_COL_WIDTH);

		Font font = wb.createFont();
		font.setFontName("ＭＳ Ｐゴシック");

		// オプションデータ(ヘッダー/フッター追加データ)情報取得
		JSONObject optDataTable = this.getOptionTableData(session);
//		オプションデータを中段(検索条件の下、メイン情報の上)に設定する場合
//		int countOptRows = optDataTable.optInt("count");
//		オプションデータを下段(メイン情報の下)に設定する場合
		int countOptRows = 0;

		// タイトル情報
		int countTitleRow = titleJArray.size();					// タイトル部の行数
		int startTitleRow = wh.size() + countOptRows;			// タイトル部の開始行
		int endTitleRow = startTitleRow + countTitleRow - 1;	// タイトル部の終了行
		int endTitleCol = title.size() - 1;						// タイトル部の終了列
		System.out.println("endTitleCol="+endTitleCol);
		int endDataRow = startTitleRow + al.size() - 1;			// データ部の終了行



		// タイトルスタイル設定用
		CellStyle styleT = wb.createCellStyle();
		Font fontT = wb.createFont();
		fontT.setFontName(font.getFontName());
		styleT.setFont(fontT);										// フォント
		styleT.setBorderTop(CellStyle.BORDER_THIN);					// 枠線上
		styleT.setBorderBottom(CellStyle.BORDER_THIN);				// 枠線下
		styleT.setBorderLeft(CellStyle.BORDER_THIN);				// 枠線左
		styleT.setBorderRight(CellStyle.BORDER_THIN);				// 枠線右
		styleT.setVerticalAlignment(CellStyle.VERTICAL_CENTER);	// 縦位置中央揃え
		styleT.setAlignment(CellStyle.ALIGN_CENTER);				// 中央揃え
		styleT.setFillPattern(CellStyle.SOLID_FOREGROUND);			// 塗りつぶし
		styleT.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());	// 色指定
		styleT.setWrapText(true);									// セル内改行


		// 計セル表示用情報
		String keiKey = KEI_KEYWORD;
		int keiKeyFindWay = KEY_FIND_WAY.AFTER.getVal();
		short keiBcColor = IndexedColors.LIGHT_GREEN.getIndex();

		// スタイル設定用
		Font fontTxt = wb.createFont();
		fontTxt.setFontName(font.getFontName());
		CellStyle styleTxt = wb.createCellStyle();
		styleTxt.setFont(fontTxt);									// フォント
		styleTxt.setBorderTop(CellStyle.BORDER_THIN);				// 枠線上
		styleTxt.setBorderBottom(CellStyle.BORDER_THIN);			// 枠線下
		styleTxt.setBorderLeft(CellStyle.BORDER_THIN);				// 枠線左
		styleTxt.setBorderRight(CellStyle.BORDER_THIN);				// 枠線右
		CellStyle styleTxtK = wb.createCellStyle();
		styleTxtK.setFont(fontTxt);									// フォント
		styleTxtK.setBorderTop(CellStyle.BORDER_THIN);				// 枠線上
		styleTxtK.setBorderBottom(CellStyle.BORDER_THIN);			// 枠線下
		styleTxtK.setBorderLeft(CellStyle.BORDER_THIN);				// 枠線左
		styleTxtK.setBorderRight(CellStyle.BORDER_THIN);			// 枠線右
		styleTxtK.setFillPattern(CellStyle.SOLID_FOREGROUND);		// 塗りつぶし
		styleTxtK.setFillForegroundColor(keiBcColor);		// 色指定

		// タイトル行参照
		Map<String, CellStyle> itemFormatR = new LinkedHashMap<String, CellStyle>();	// 通常セル用スタイル
		Map<String, CellStyle> itemFormatRK = new LinkedHashMap<String, CellStyle>();	// 計セル用スタイル
		for(Map.Entry<String, JSONObject> e : format.entrySet()) {
			// 通常セル
			CellStyle styleTmp = wb.createCellStyle();
			styleTmp.setFont(fontTxt);									// フォント
			styleTmp.setBorderTop(CellStyle.BORDER_THIN);				// 枠線上
			styleTmp.setBorderBottom(CellStyle.BORDER_THIN);			// 枠線下
			styleTmp.setBorderLeft(CellStyle.BORDER_THIN);				// 枠線左
			styleTmp.setBorderRight(CellStyle.BORDER_THIN);				// 枠線右
			if(e.getValue().containsKey("BcColor")){
				styleTmp.setFillPattern(CellStyle.SOLID_FOREGROUND);						// 塗りつぶし
				styleTmp.setFillForegroundColor((short) e.getValue().getInt("BcColor"));	// 色指定
			}
			styleTmp.setAlignment((short) e.getValue().getInt("Align"));
			DataFormat formatTmp = wb.createDataFormat();
			styleTmp.setDataFormat(formatTmp.getFormat(e.getValue().getString("Format")));	// セルのフォーマット
			itemFormatR.put(e.getKey(), styleTmp);
			// 計セル用
			CellStyle styleTmpK = wb.createCellStyle();
			styleTmpK.setFont(fontTxt);									// フォント
			styleTmpK.setBorderTop(CellStyle.BORDER_THIN);				// 枠線上
			styleTmpK.setBorderBottom(CellStyle.BORDER_THIN);			// 枠線下
			styleTmpK.setBorderLeft(CellStyle.BORDER_THIN);				// 枠線左
			styleTmpK.setBorderRight(CellStyle.BORDER_THIN);			// 枠線右
			styleTmpK.setFillPattern(CellStyle.SOLID_FOREGROUND);		// 塗りつぶし
			styleTmpK.setFillForegroundColor(keiBcColor);		// 色指定
			styleTmpK.setAlignment((short) e.getValue().getInt("Align"));
			styleTmpK.setDataFormat(formatTmp.getFormat(e.getValue().getString("Format")));	// セルのフォーマット
			itemFormatRK.put(e.getKey(), styleTmpK);
		}

		// フォーマット識別の際に、除外する文言
		String[] removeTxt = new String[]{};
		removeTxt = (String[]) ArrayUtils.addAll(removeTxt, ExcelGenerateServlet.REMOVE_KEYWORD);
		String[] brankTxt = new String[]{};
		for (int i = 0; i < removeTxt.length; i++) {
			brankTxt = (String[]) ArrayUtils.add(brankTxt, "");
		}

		// 検索条件設定
		int countWhere = -1;
		Iterator<List<String>> itrWhere = wh.iterator();
		while (itrWhere.hasNext()) {
			countWhere++;	// カウンタ

			// 行生成
			SXSSFRow row = sheet.createRow(countWhere);

			// セル情報の取得
			List<String> colList = itrWhere.next();
			Iterator<String> itrCols = colList.iterator();

			int index = -1;
			while (itrCols.hasNext()) {
				index++;

				// セル（列）生成
				SXSSFCell cell = row.createCell(index);
				// 情報設定
				String val = (String) itrCols.next();
				if(StringUtils.isNotEmpty(val)){
					cell.setCellValue(val);
				}
				// レポート名
				if (countWhere == 0) {
					// セルスタイル生成
					Font font1 = wb.createFont();
					font1.setFontName(font.getFontName());
					font1.setFontHeightInPoints((short) 12);

					CellStyle style = wb.createCellStyle();
					if(index==8){
						style.setAlignment(CellStyle.ALIGN_RIGHT);				// 中央揃え
					}else{
						font1.setBoldweight(Font.BOLDWEIGHT_BOLD);
					}
					style.setFont(font1);
					// セルスタイル設定
					cell.setCellStyle(style);
				}
			}
		}

		// 非表示列設定
		if(hideCols!=null){
			for(int colIdx : hideCols){
				sheet.setColumnHidden(colIdx, true);
			}
		}

//		// 中段データ設定
//		this.setOptionTableData(session, wb, sheet, optDataTable, wh.size());

		Iterator<List<String>> itr = al.iterator();
		int count = -1;
		while (itr.hasNext()) {
			count++;	// カウンタ

			// 行生成
			SXSSFRow row = sheet.createRow(count + startTitleRow);
			if (count < countTitleRow) {		// タイトル行
				row.setHeightInPoints(TIT_ROW_HEIGHT);
			}

			// セル情報の取得
			List<String> colList = itr.next();
			Iterator<String> itrCols = colList.iterator();

			// 計行判断処理
			boolean isKeiRow = false;
			if(keiCols!=null){
				if(keiKeyFindWay == KEY_FIND_WAY.BEFORE.getVal()){
					for(int keiCol : keiCols){
						if(colList.size() <= keiCol){
							continue;
						}
						if (StringUtils.startsWithAny(StringUtils.replaceEach(colList.get(keiCol),removeTxt,brankTxt), keiKey.split(","))) {
							isKeiRow = true;
							break;
						}
					}
				}else{
					for(int keiCol : keiCols){
						if(colList.size() <= keiCol){
							continue;
						}
						if (StringUtils.endsWithAny(StringUtils.replaceEach(colList.get(keiCol),removeTxt,brankTxt), keiKey.split(","))) {
							isKeiRow = true;
							break;
						}
					}
				}
			}

			int index = -1;
			while (itrCols.hasNext()) {
				index++;
				String val = (String) itrCols.next();
				val = this.getUnicodeReplace(val);		// ユニコード変換
				// セル（列）生成
				SXSSFCell cell = row.createCell(index);
				// 情報設定
				if (count < countTitleRow) {
					// タイトル部
					cell.setCellValue(val);
					// セルスタイル設定
					cell.setCellStyle(styleT);
					int cntN = StringUtils.countMatches(val, "\n");
					if(cntN > 0){
						row.setHeightInPoints((float) (sheet.getDefaultRowHeightInPoints()*(cntN+1)));
					}
				} else if(index <= endTitleCol){
					int type = Cell.CELL_TYPE_STRING;

					boolean isSet = false;

					boolean isKeiCell = false;
					if(isKeiRow){
						isKeiCell = true;
					}
					// 計列判断処理 ※計判断はタイトル情報の最上段のみで実施
					if(titleJArray.size() > 0 && !isKeiCell){
						if(keiKeyFindWay == KEY_FIND_WAY.BEFORE.getVal()){
							if (StringUtils.startsWithAny(StringUtils.replaceEach(titleJArray.getJSONArray(0).optString(index),removeTxt,brankTxt), keiKey.split(","))) {
								isKeiCell = true;
							}
						}else{
							if (StringUtils.endsWithAny(StringUtils.replaceEach(titleJArray.getJSONArray(0).optString(index),removeTxt,brankTxt), keiKey.split(","))) {
								isKeiCell = true;
							}
						}
					}

					// タイトル情報とセルスタイル情報のキーを比較し、タイトル文字列がキーで終わっていたらセル設定を利用（より下段のタイトルを有効とするため、全タイトルリストを確認）
					Map<String, CellStyle> itemFormat = new LinkedHashMap<String, CellStyle>();
					if(isKeiCell){
						itemFormat = itemFormatRK;
					}else{
						itemFormat = itemFormatR;
					}
					for(Entry<String, CellStyle> e : itemFormat.entrySet()) {
						for (int i = 0; i < titleJArray.size(); i++) {
							if(keyFindWay == KEY_FIND_WAY.BEFORE.getVal()){
								if (StringUtils.startsWithAny( StringUtils.replaceEach(titleJArray.getJSONArray(i).optString(index),removeTxt, brankTxt), e.getKey().split(","))) {
									type = format.get(e.getKey()).getInt("Type");
									cell.setCellStyle(e.getValue());
									isSet = true;
								}
							}else{
								if (StringUtils.endsWithAny(StringUtils.replaceEach(titleJArray.getJSONArray(i).optString(index),removeTxt, brankTxt), e.getKey().split(","))) {
									type = format.get(e.getKey()).getInt("Type");
									cell.setCellStyle(e.getValue());
									isSet = true;
								}
							}
						}
						if(isSet) break;
					}

					// タイトル判断で当てはまらなかった場合、デフォルトスタイル設定
					if(!isSet){
						if(isKeiCell){
							cell.setCellStyle(styleTxtK);
						}else{
							cell.setCellStyle(styleTxt);
						}
					}

					// 値設定
					if(StringUtils.isNotEmpty(val)){
						// データのタイプ判別
						switch(type){
						case Cell.CELL_TYPE_NUMERIC:
							try {
								cell.setCellValue(Double.valueOf(val));
							} catch (Exception e) {
								cell.setCellValue(val);
							}
							break;
						case Cell.CELL_TYPE_STRING:
						default:
							cell.setCellValue(val);
							break;
						}
					}
				}
			}

			// タイトル部分のセル結合
			if (count == countTitleRow -1) {
				// タイトル部のセル結合
				int[] escColIdx = new int[countTitleRow];	// 行単位で作業済みのcolIdxを保持
				int colIdx = 0;		// 列番号
				while (sheet.getRow(startTitleRow) != null && sheet.getRow(startTitleRow).getCell(colIdx) != null && sheet.getRow(startTitleRow).getCell(colIdx).getStringCellValue().length() > 0) {

					int rowIdx = startTitleRow;		// 行番号
					while (rowIdx < endTitleRow) {
						String val = sheet.getRow(rowIdx).getCell(colIdx).getStringCellValue();

						if (colIdx < escColIdx[rowIdx - startTitleRow]) {
							// 作業済みのcolIdxの場合

						} else if (rowIdx > startTitleRow && val.equals(sheet.getRow(rowIdx - 1).getCell(colIdx).getStringCellValue())) {
							// 最上段ではなく、かつ、上のセルと値が同じ場合

						} else {
							int rowspan = 1;
							int colspan = 1;

							while (rowIdx + rowspan <= endTitleRow && val.equals(sheet.getRow(rowIdx + rowspan).getCell(colIdx).getStringCellValue())) {
								// タイトル部内の下のセルと値が同じ場合
								rowspan++;
							}

							while ((rowIdx - startTitleRow == 0 || colIdx + colspan < escColIdx[rowIdx - 1 - startTitleRow])
									&& sheet.getRow(rowIdx).getCell(colIdx + colspan) != null
									&& val.equals(sheet.getRow(rowIdx).getCell(colIdx + colspan).getStringCellValue())) {
								// １つ上の段の結合領域を超えておらず（最上段は考慮しない）、
								// かつ、右のセルと値が同じ場合
								colspan++;
							}

							if (rowspan > 1 || colspan > 1) {
								// セルの結合
								sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx + rowspan - 1, colIdx, colIdx + colspan - 1));

								// 作業済みのcolIdxを保持
								escColIdx[rowIdx - startTitleRow] = colIdx + colspan;
							}
						}
						rowIdx++;
					}
					colIdx++;
				}
			}
		}

		// 下段データ設定
		this.setOptionTableData(session, wb, sheet, optDataTable, endDataRow + 2);

		// ウインドウ枠の固定
		sheet.createFreezePane(frozenCol, endTitleRow + 1);
		// 印刷タイトル設定
//		wb.setRepeatingRowsAndColumns(wb.getSheetIndex(sheet), 0, 0, 0, endTitleRow);
		//5行目までを固定
		sheet.setRepeatingRows(new CellRangeAddress(0, 5, 0, endTitleRow));
		// 印刷範囲の設定
		wb.setPrintArea(wb.getSheetIndex(sheet), 0, endTitleCol, 0, endDataRow);
		sheet.setFitToPage(true);

		// 列幅調整
		for (int i = 0; i <= endTitleCol; i++) {
			//sheet.autoSizeColumn(i);
			boolean isSet = false;
			for(Entry<String, CellStyle> e : itemFormatR.entrySet()) {
				for (int j = 0; j < titleJArray.size(); j++) {
					if ((keyFindWay == KEY_FIND_WAY.BEFORE.getVal()&&StringUtils.startsWithAny(StringUtils.replaceEach(titleJArray.getJSONArray(j).optString(i), removeTxt, brankTxt), e.getKey().split(",")))
							|| (keyFindWay == KEY_FIND_WAY.AFTER.getVal()&&StringUtils.endsWithAny(StringUtils.replaceEach(titleJArray.getJSONArray(j).optString(i), removeTxt, brankTxt), e.getKey().split(",")))) {
						int width = format.get(e.getKey()).getInt("Width");
						sheet.setColumnWidth(i, width);
						isSet = true;
					}
				}
				if(isSet) break;
			}
		}


		return wb;
	}

	/**
	 * createReport99<br/>
	 * @param session		セッション
	 * @param formatR		フォーマットオプション(タイトル行参照)
	 * @param formatC		フォーマットオプション(指定基準列参照)
	 * @param frozenCol		固定列Index
	 * @param keyCol		フォーマット指定基準列
	 * @param keiCols		合計行判断列Index
	 * @param hideCols		hidden列Index
	 * @return SXSSFWorkbook
	 * @throws IOException 例外
	 */
	private SXSSFWorkbook createReport99(HttpSession session, Map<String, JSONObject> formatR,
			Map<String, JSONObject> formatC, int frozenCol, int keyCol, int[] keiCols, int[] hideCols) throws IOException {
		// Session 情報格納用
		ArrayList<List<String>> wh = new ArrayList<List<String>>();		// 条件
		ArrayList<List<String>> al = new ArrayList<List<String>>();;	// 検索結果
		ArrayList<String> title = new ArrayList<String>();;				// タイトル
		JSONArray titleJArray = new JSONArray();						// 検索結果（JSONArray版）
		// Session から検索結果取得
		getSessionResult(session, wh, al, title, titleJArray);

		return createReport99(session, wh, al, title, titleJArray, formatR, formatC, frozenCol, keyCol, keiCols, hideCols);
	}

	/**
	 * createReport99<br/>
	 * <br/>
	 * Excel作成処理(シート一枚・フォーマット指定(タイトル行・基準列参照))<br/>
	 * 取得情報を元に、EXCELファイルを作成。<br/>
	 * 出力シート１枚に対し、全情報記述<br/>
	 * <br/>
	 * フォーマット指定について<br/>
	 * 1.key値がタイトル、または基準列の値と部分一致したフォーマットがあった場合使用<br/>
	 * 2.タイトル行と基準列では基準列によるフォーマットを優先とする<br/>
	 * 3.タイトルが複数行の場合、より下段の一致を優先とする<br/>
	 * 4.基準列によるスタイル設定は、固定列設定時は固定列には使用しない<br>
	 *
	 * createReport00との違い
	 * 1.各レコードの基準列の値でフォーマット判断
	 * 2.各フォーマットでkeyを前方一致/後方一致のどちらで判断するか指定(デフォルト後方一致)
	 *
	 * 2017.01.10 メモリ軽減のため、SXSSF形式に変更。SXSSF形式では作成済みの行の値変更に制限があるため、テンプレートファイル方式廃止<br/>
	 *
	 * @param session		セッション
	 * @param wh			検索条件
	 * @param al			検索結果
	 * @param title			タイトル部
	 * @param titleJArray	タイトル情報
	 * @param formatR		フォーマットオプション(タイトル行参照)
	 * @param formatC		フォーマットオプション(指定基準列参照)
	 * @param frozenCol		固定列Index
	 * @param keyCol		フォーマット指定基準列
	 * @param keiCols		計行判断列index
	 * @param hideCols		hidden列Index
	 * @return SXSSFWorkbook
	 * @throws IOException
	 */
	private SXSSFWorkbook createReport99(HttpSession session, ArrayList<List<String>> wh,
			ArrayList<List<String>> al, ArrayList<String> title, JSONArray titleJArray, Map<String, JSONObject> formatR,
			Map<String, JSONObject> formatC, int frozenCol, int keyCol, int[] keiCols, int[] hideCols) throws IOException {
		System.out.println(getClass().getSimpleName()+".createReport99");
//		// Excel ワークブック読み込み
//		InputStream is = new FileInputStream(getServletContext().getRealPath(filePath));
//		XSSFWorkbook wb = new XSSFWorkbook(is);
//		XSSFSheet sheet = wb.getSheetAt(0);

		// SXSSF形式だとテンプレートはコピーできないため、新規作成
		SXSSFWorkbook wb = new SXSSFWorkbook();
		SXSSFSheet sheet = wb.createSheet();
		sheet.setDisplayGridlines(false);
		sheet.setDefaultColumnWidth(DEF_COL_WIDTH);

		long start = System.currentTimeMillis();

		Font font = wb.createFont();
		font.setFontName("ＭＳ Ｐゴシック");

		// オプションデータ(ヘッダー/フッター追加データ)情報取得
		JSONObject optDataTable = this.getOptionTableData(session);
//		オプションデータを中段(検索条件の下、メイン情報の上)に設定する場合
//		int countOptRows = optDataTable.optInt("count");
//		オプションデータを下段(メイン情報の下)に設定する場合
		int countOptRows = 0;

		// タイトル情報
		int countTitleRow = titleJArray.size();					// タイトル部の行数
		int startTitleRow = wh.size() + countOptRows;			// タイトル部の開始行
		int endTitleRow = startTitleRow + countTitleRow - 1;	// タイトル部の終了行
		int endTitleCol = title.size() - 1;						// タイトル部の終了列
		int endDataRow = startTitleRow + al.size() - 1;			// データ部の終了行

		// タイトルスタイル設定用
		CellStyle styleT = wb.createCellStyle();
		Font fontT = wb.createFont();
		fontT.setFontName(font.getFontName());
		styleT.setFont(fontT);										// フォント
		styleT.setBorderTop(CellStyle.BORDER_THIN);					// 枠線上
		styleT.setBorderBottom(CellStyle.BORDER_THIN);				// 枠線下
		styleT.setBorderLeft(CellStyle.BORDER_THIN);				// 枠線左
		styleT.setBorderRight(CellStyle.BORDER_THIN);				// 枠線右
		styleT.setVerticalAlignment(CellStyle.VERTICAL_CENTER);	// 縦位置中央揃え
		styleT.setAlignment(CellStyle.ALIGN_CENTER);				// 中央揃え
		styleT.setFillPattern(CellStyle.SOLID_FOREGROUND);			// 塗りつぶし
		styleT.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());	// 色指定
		styleT.setWrapText(true);									// セル内改行

		// 計セル表示用情報
		String keiKey = KEI_KEYWORD;
		int keiKeyFindWay = KEY_FIND_WAY.AFTER.getVal();
		short keiBcColor = IndexedColors.LIGHT_GREEN.getIndex();


		// スタイル設定用
		Font fontTxt = wb.createFont();
		fontTxt.setFontName(font.getFontName());
		CellStyle styleTxt = wb.createCellStyle();
		styleTxt.setFont(fontTxt);									// フォント
		styleTxt.setBorderTop(CellStyle.BORDER_THIN);				// 枠線上
		styleTxt.setBorderBottom(CellStyle.BORDER_THIN);			// 枠線下
		styleTxt.setBorderLeft(CellStyle.BORDER_THIN);				// 枠線左
		styleTxt.setBorderRight(CellStyle.BORDER_THIN);				// 枠線右
		CellStyle styleTxtK = wb.createCellStyle();
		styleTxtK.setFont(fontTxt);									// フォント
		styleTxtK.setBorderTop(CellStyle.BORDER_THIN);				// 枠線上
		styleTxtK.setBorderBottom(CellStyle.BORDER_THIN);			// 枠線下
		styleTxtK.setBorderLeft(CellStyle.BORDER_THIN);				// 枠線左
		styleTxtK.setBorderRight(CellStyle.BORDER_THIN);			// 枠線右
		styleTxtK.setFillPattern(CellStyle.SOLID_FOREGROUND);		// 塗りつぶし
		styleTxtK.setFillForegroundColor(keiBcColor);				// 色指定

		// タイトル行参照
		Map<String, CellStyle> itemFormatR = new LinkedHashMap<String, CellStyle>();	// 通常セル用スタイル
		Map<String, CellStyle> itemFormatRK = new LinkedHashMap<String, CellStyle>();	// 計セル用スタイル
		for(Map.Entry<String, JSONObject> e : formatR.entrySet()) {
			CellStyle styleTmp = wb.createCellStyle();
			styleTmp.setFont(fontTxt);									// フォント
			styleTmp.setBorderTop(CellStyle.BORDER_THIN);				// 枠線上
			styleTmp.setBorderBottom(CellStyle.BORDER_THIN);			// 枠線下
			styleTmp.setBorderLeft(CellStyle.BORDER_THIN);				// 枠線左
			styleTmp.setBorderRight(CellStyle.BORDER_THIN);				// 枠線右
			if(e.getValue().containsKey("BcColor")){
				styleTmp.setFillPattern(CellStyle.SOLID_FOREGROUND);						// 塗りつぶし
				styleTmp.setFillForegroundColor((short) e.getValue().getInt("BcColor"));	// 色指定
			}
			styleTmp.setAlignment((short) e.getValue().getInt("Align"));
			DataFormat formatTmp = wb.createDataFormat();
			styleTmp.setDataFormat(formatTmp.getFormat(e.getValue().getString("Format")));	// セルのフォーマット
			itemFormatR.put(e.getKey(), styleTmp);
			// 計セル用
			CellStyle styleTmpK = wb.createCellStyle();
			styleTmpK.setFont(fontTxt);									// フォント
			styleTmpK.setBorderTop(CellStyle.BORDER_THIN);				// 枠線上
			styleTmpK.setBorderBottom(CellStyle.BORDER_THIN);			// 枠線下
			styleTmpK.setBorderLeft(CellStyle.BORDER_THIN);				// 枠線左
			styleTmpK.setBorderRight(CellStyle.BORDER_THIN);			// 枠線右
			styleTmpK.setFillPattern(CellStyle.SOLID_FOREGROUND);		// 塗りつぶし
			styleTmpK.setFillForegroundColor(keiBcColor);		// 色指定
			styleTmpK.setAlignment((short) e.getValue().getInt("Align"));
			styleTmpK.setDataFormat(formatTmp.getFormat(e.getValue().getString("Format")));	// セルのフォーマット
			itemFormatRK.put(e.getKey(), styleTmpK);
		}
		// 基準列参照
		Map<String, CellStyle> itemFormatC = new LinkedHashMap<String, CellStyle>();	// 通常セル用スタイル
		Map<String, CellStyle> itemFormatCK= new LinkedHashMap<String, CellStyle>();	// 計セル用スタイル
		for(Map.Entry<String, JSONObject> e : formatC.entrySet()) {
			CellStyle styleTmp = wb.createCellStyle();
			styleTmp.setFont(fontTxt);									// フォント
			styleTmp.setBorderTop(CellStyle.BORDER_THIN);				// 枠線上
			styleTmp.setBorderBottom(CellStyle.BORDER_THIN);			// 枠線下
			styleTmp.setBorderLeft(CellStyle.BORDER_THIN);				// 枠線左
			styleTmp.setBorderRight(CellStyle.BORDER_THIN);				// 枠線右
			if(e.getValue().containsKey("BcColor")){
				styleTmp.setFillPattern(CellStyle.SOLID_FOREGROUND);						// 塗りつぶし
				styleTmp.setFillForegroundColor((short) e.getValue().getInt("BcColor"));	// 色指定
			}
			styleTmp.setAlignment((short) e.getValue().getInt("Align"));
			DataFormat formatTmp = wb.createDataFormat();
			styleTmp.setDataFormat(formatTmp.getFormat(e.getValue().getString("Format")));	// セルのフォーマット
			itemFormatC.put(e.getKey(), styleTmp);
			// 計セル用
			CellStyle styleTmpK = wb.createCellStyle();
			styleTmpK.setFont(fontTxt);									// フォント
			styleTmpK.setBorderTop(CellStyle.BORDER_THIN);				// 枠線上
			styleTmpK.setBorderBottom(CellStyle.BORDER_THIN);			// 枠線下
			styleTmpK.setBorderLeft(CellStyle.BORDER_THIN);				// 枠線左
			styleTmpK.setBorderRight(CellStyle.BORDER_THIN);			// 枠線右
			styleTmpK.setFillPattern(CellStyle.SOLID_FOREGROUND);		// 塗りつぶし

			styleTmpK.setFillForegroundColor(keiBcColor);		// 色指定
			styleTmpK.setAlignment((short) e.getValue().getInt("Align"));
			styleTmpK.setDataFormat(formatTmp.getFormat(e.getValue().getString("Format")));	// セルのフォーマット
			itemFormatCK.put(e.getKey(), styleTmpK);
		}

		// フォーマット識別の際に、除外する文言
		String[] removeTxt = new String[]{};
		removeTxt = (String[]) ArrayUtils.addAll(removeTxt, ExcelGenerateServlet.REMOVE_KEYWORD);
		String[] brankTxt = new String[]{};
		for (int i = 0; i < removeTxt.length; i++) {
			brankTxt = (String[]) ArrayUtils.add(brankTxt, "");
		}

		long end = System.currentTimeMillis();
		System.out.println("準備処理:" + (end - start)  + "ms");
		start = System.currentTimeMillis();

		// 検索条件設定
		int countWhere = -1;
		Iterator<List<String>> itrWhere = wh.iterator();
		while (itrWhere.hasNext()) {
			countWhere++;	// カウンタ

			// 行生成
			SXSSFRow row = sheet.createRow(countWhere);

			// セル情報の取得
			List<String> colList = itrWhere.next();
			Iterator<String> itrCols = colList.iterator();

			int index = -1;
			while (itrCols.hasNext()) {
				index++;

				// セル（列）生成
				SXSSFCell cell = row.createCell(index);
				// 情報設定
				String val = (String) itrCols.next();
				if(StringUtils.isNotEmpty(val)){
					cell.setCellValue(val);
				}
				// レポート名
				if (countWhere == 0) {
					// セルスタイル生成
					Font font1 = wb.createFont();
					font1.setFontName(font.getFontName());
					font1.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);
					font1.setFontHeightInPoints((short) 12);

					CellStyle style = wb.createCellStyle();
					style.setFont(font1);

					// セルスタイル設定
					cell.setCellStyle(style);
				}
			}
		}

		// 非表示列設定
		if(hideCols!=null){
			for(int colIdx : hideCols){
				sheet.setColumnHidden(colIdx, true);
			}
		}
//
//		// 画面中段データ設定
//		this.setOptionTableData(session, wb, sheet, optDataTable, wh.size());

		Iterator<List<String>> itr = al.iterator();
		int count = -1;
		while (itr.hasNext()) {
			count++;	// カウンタ

			// 行生成
			SXSSFRow row = sheet.createRow(count + startTitleRow);
			if (count < countTitleRow) {		// タイトル行
				row.setHeightInPoints(TIT_ROW_HEIGHT);
			}

			// セル情報の取得
			List<String> colList = itr.next();
			Iterator<String> itrCols = colList.iterator();

			// 計行判断処理
			boolean isKeiRow = false;
			if(keiCols!=null){
				if(keiKeyFindWay == KEY_FIND_WAY.BEFORE.getVal()){
					for(int keiCol : keiCols){
						if(colList.size() <= keiCol){
							continue;
						}
						if (StringUtils.startsWithAny(StringUtils.replaceEach(colList.get(keiCol),removeTxt,brankTxt), keiKey.split(","))) {
							isKeiRow = true;
							break;
						}
					}
				}else{
					for(int keiCol : keiCols){
						if(colList.size() <= keiCol){
							continue;
						}
						if (StringUtils.endsWithAny(StringUtils.replaceEach(colList.get(keiCol),removeTxt,brankTxt), keiKey.split(","))) {
							isKeiRow = true;
							break;
						}
					}
				}
			}

			int index = -1;
			while (itrCols.hasNext()) {
				index++;
				String val = (String) itrCols.next();
				val = this.getUnicodeReplace(val);	// ユニコード変換
				// セル（列）生成
				SXSSFCell cell = row.createCell(index);
				// 情報設定
				if (count < countTitleRow) {
					// タイトル部
					cell.setCellValue(val);
					// セルスタイル設定
					cell.setCellStyle(styleT);
					int cntN = StringUtils.countMatches(val, "\n");
					if(cntN > 0){
						row.setHeightInPoints((float) (sheet.getDefaultRowHeightInPoints()*(cntN+1)));
					}
				} else if(index <= endTitleCol){
					int type = Cell.CELL_TYPE_STRING;

					boolean isSet = false;

					boolean isKeiCell = false;
					if(isKeiRow){
						isKeiCell = true;
					}
					// 計列判断処理 ※計判断はタイトル情報の最上段のみで実施
					if(titleJArray.size() > 0 && !isKeiCell){
						if(keiKeyFindWay == KEY_FIND_WAY.BEFORE.getVal()){
							if (StringUtils.startsWithAny(StringUtils.replaceEach(titleJArray.getJSONArray(0).optString(index),removeTxt,brankTxt), keiKey.split(","))) {
								isKeiCell = true;
							}
						}else{
							if (StringUtils.endsWithAny(StringUtils.replaceEach(titleJArray.getJSONArray(0).optString(index),removeTxt,brankTxt), keiKey.split(","))) {
								isKeiCell = true;
							}
						}
					}

					String key = colList.get(keyCol);
					int keyFindWay = KEY_FIND_WAY.AFTER.getVal();
					if(StringUtils.isNotEmpty(key)
							&& (frozenCol==0 || frozenCol <= index)){		// 科目・項目列の内容によるスタイル設定は、固定列設定時は固定列以外に適応
						Map<String, CellStyle> itemFormat = new LinkedHashMap<String, CellStyle>();
						if(isKeiCell){
							itemFormat = itemFormatCK;
						}else{
							itemFormat = itemFormatC;
						}
						for(Entry<String, CellStyle> e : itemFormat.entrySet()) {
							keyFindWay = formatC.get(e.getKey()).getInt("FindWay");
							// 基準列の値とkeyが部分一致した場合
							if((keyFindWay == KEY_FIND_WAY.BEFORE.getVal() && StringUtils.startsWithAny(StringUtils.replaceEach(key,removeTxt,brankTxt), e.getKey().split(",")))
							|| (keyFindWay != KEY_FIND_WAY.BEFORE.getVal() && StringUtils.endsWithAny(StringUtils.replaceEach(key,removeTxt,brankTxt), e.getKey().split(",")))){
								type = formatC.get(e.getKey()).getInt("Type");
								if(type==Cell.CELL_TYPE_NUMERIC){
									if(NumberUtils.isNumber(val)){
										cell.setCellStyle(e.getValue());
										isSet = true;
									}
								}else{
									cell.setCellStyle(e.getValue());
									isSet = true;
								}
								break;
							}
						}
					}
					// タイトル情報とセルスタイル情報のキーを比較し、タイトル文字列が部分一致したらセル設定を利用（より下段のタイトルを有効とするため、全タイトルリストを確認）
					if(!isSet){
						Map<String, CellStyle> itemFormat = new LinkedHashMap<String, CellStyle>();
						if(isKeiCell){
							itemFormat = itemFormatRK;
						}else{
							itemFormat = itemFormatR;
						}
						for(Entry<String, CellStyle> e : itemFormat.entrySet()) {
							keyFindWay = formatR.get(e.getKey()).getInt("FindWay");
							for (int i = 0; i < titleJArray.size(); i++) {
								if((keyFindWay == KEY_FIND_WAY.BEFORE.getVal()&&StringUtils.startsWithAny(StringUtils.replaceEach(titleJArray.getJSONArray(i).optString(index),removeTxt,brankTxt), e.getKey().split(",")))
								|| (keyFindWay != KEY_FIND_WAY.BEFORE.getVal()&&StringUtils.endsWithAny(StringUtils.replaceEach(titleJArray.getJSONArray(i).optString(index),removeTxt,brankTxt), e.getKey().split(",")))){
									type = formatR.get(e.getKey()).getInt("Type");
									if(type==Cell.CELL_TYPE_NUMERIC){
										if(NumberUtils.isNumber(val)){
											cell.setCellStyle(e.getValue());
											isSet = true;
										}
									}else{
										cell.setCellStyle(e.getValue());
										isSet = true;
									}
								}
							}
							if(isSet) break;
						}
					}

					// タイトル、基準列判断でも当てはまらなかった場合、デフォルトスタイル設定
					if(!isSet){
						type  = Cell.CELL_TYPE_STRING;
						if(isKeiCell){
							cell.setCellStyle(styleTxtK);
						}else{
							cell.setCellStyle(styleTxt);
						}
					}

					// 値設定
					if(StringUtils.isNotEmpty(val)){
						// データのタイプ判別
						switch(type){
						case Cell.CELL_TYPE_NUMERIC:
							try {
								cell.setCellValue(Double.valueOf(val));
							} catch (Exception e) {
								cell.setCellValue(val);
							}
							break;
						case Cell.CELL_TYPE_STRING:
						default:
							cell.setCellValue(val);
							break;
						}
					}
				}
			}

			if (count == countTitleRow -1) {
				// タイトル部のセル結合
				int[] escColIdx = new int[countTitleRow];	// 行単位で作業済みのcolIdxを保持
				int colIdx = 0;		// 列番号
				while (sheet.getRow(startTitleRow) != null && sheet.getRow(startTitleRow).getCell(colIdx) != null && sheet.getRow(startTitleRow).getCell(colIdx).getStringCellValue().length() > 0) {

					int rowIdx = startTitleRow;		// 行番号
					while (rowIdx < endTitleRow) {
						String val = sheet.getRow(rowIdx).getCell(colIdx).getStringCellValue();

						if (colIdx < escColIdx[rowIdx - startTitleRow]) {
							// 作業済みのcolIdxの場合

						} else if (rowIdx > startTitleRow && val.equals(sheet.getRow(rowIdx - 1).getCell(colIdx).getStringCellValue())) {
							// 最上段ではなく、かつ、上のセルと値が同じ場合

						} else {
							int rowspan = 1;
							int colspan = 1;

							while (rowIdx + rowspan <= endTitleRow && val.equals(sheet.getRow(rowIdx + rowspan).getCell(colIdx).getStringCellValue())) {
								// タイトル部内の下のセルと値が同じ場合
								rowspan++;
							}

							while ((rowIdx - startTitleRow == 0 || colIdx + colspan < escColIdx[rowIdx - 1 - startTitleRow])
									&& sheet.getRow(rowIdx).getCell(colIdx + colspan) != null
									&& val.equals(sheet.getRow(rowIdx).getCell(colIdx + colspan).getStringCellValue())) {
								// １つ上の段の結合領域を超えておらず（最上段は考慮しない）、
								// かつ、右のセルと値が同じ場合
								colspan++;
							}

							if (rowspan > 1 || colspan > 1) {
								// セルの結合
								sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx + rowspan - 1, colIdx, colIdx + colspan - 1));

								// 作業済みのcolIdxを保持
								escColIdx[rowIdx - startTitleRow] = colIdx + colspan;
							}
						}
						rowIdx++;
					}
					colIdx++;
				}
			}
		}

		// 下段データ設定
		this.setOptionTableData(session, wb, sheet, optDataTable, endDataRow + 2);

		end = System.currentTimeMillis();
		System.out.println("行生成:" + (end - start)  + "ms");
		start = System.currentTimeMillis();

		// 列幅調整
		for (int i = 0; i <= endTitleCol; i++) {
			//sheet.autoSizeColumn(i);
			boolean isSet = false;
			for(Entry<String, CellStyle> e : itemFormatR.entrySet()) {
				int keyFindWay = formatR.get(e.getKey()).getInt("FindWay");
				for (int j = 0; j < titleJArray.size(); j++) {
					// タイトルの値とkeyが部分一致した場合
					if((keyFindWay == KEY_FIND_WAY.BEFORE.getVal() && StringUtils.startsWithAny(StringUtils.replaceEach(titleJArray.getJSONArray(j).optString(i),removeTxt,brankTxt), e.getKey().split(",")))
							|| (keyFindWay == KEY_FIND_WAY.AFTER.getVal() && StringUtils.endsWithAny(StringUtils.replaceEach(titleJArray.getJSONArray(j).optString(i),removeTxt,brankTxt), e.getKey().split(",")))){
						int width = formatR.get(e.getKey()).getInt("Width");
						sheet.setColumnWidth(i, width);
						isSet = true;
					}
				}
				if(isSet) break;
			}
		}

		// ウインドウ枠の固定
		sheet.createFreezePane(frozenCol, endTitleRow + 1);
		// 印刷タイトル設定
//		wb.setRepeatingRowsAndColumns(wb.getSheetIndex(sheet), 0, 0, 0, endTitleRow);
		sheet.setRepeatingRows(new CellRangeAddress(0, 0, 0, endTitleRow));
		// 印刷範囲の設定
		wb.setPrintArea(wb.getSheetIndex(sheet), 0, endTitleCol, 0, endDataRow);

		end = System.currentTimeMillis();
		System.out.println("99終了まで:" + (end - start)  + "ms");
		start = System.currentTimeMillis();

		return wb;
	}



	/**
	 * createReport00<br/>
	 * <br/>
	 * @param session		セッション
	 * @param frozenCol		固定列Index
	 * @param keiCols		合計行判断列Index
	 * @param hideCols		hidden列Index
	 * @return SXSSFWorkbook
	 * @throws IOException 例外
	 */
	private SXSSFWorkbook createReportCSV(HttpSession session) throws IOException {
		// Session 情報格納用
		ArrayList<List<String>> wh = new ArrayList<List<String>>();		// 条件
		ArrayList<List<String>> al = new ArrayList<List<String>>();;	// 検索結果
		ArrayList<String> title = new ArrayList<String>();;				// タイトル
		JSONArray titleJArray = new JSONArray();						// 検索結果（JSONArray版）
		// Session から検索結果取得
		getSessionResult(session, wh, al, title, titleJArray);

		return createReportCSV(session,  al);
	}

	/**
	 * createReport00<br/>
	 * <br/>
	 * Excel作成処理(シート一枚・フォーマット指定(タイトル参照))<br/>
	 * 取得情報を元に、EXCELファイルを作成。<br/>
	 * 出力シート１枚に対し、全情報記述<br/>
	 * <br/>
	 * フォーマット指定について<br/>
	 * 1.formatにkey値が列名と部分一致したフォーマットがあった場合使用（後方一致/前方一致は指定）<br/>
	 *
	 * 2016.11.29 srcCell参照がXLSXでエラーになるため、処理削除, srcCell前提のlastColor,loopRowCountも廃止<br/>
	 * 2017.01.10 メモリ軽減のため、SXSSF形式に変更。SXSSF形式では作成済みの行の値変更に制限があるため、テンプレートファイル方式廃止<br/>
	 *
	 * @param session		セッション
	 * @param filePath		ファイルパス
	 * @param wh			検索条件
	 * @param al			検索結果
	 * @param title			タイトル部
	 * @param titleJArray	タイトル情報
	 * @param format		フォーマットオプション
	 * @param lastLowColor	最終行色番号
	 * @param loopRowCount	複数行1セットのレイアウトの行数指定
	 * @param frozenCol		固定列Index
	 * @param keyFindWay	後方一致/前方一致
	 * @param keiCols		合計行判断列Index
	 * @param hideCols		hidden列Index
	 * @return SXSSFWorkbook
	 * @throws IOException
	 */
	private SXSSFWorkbook createReportCSV(HttpSession session, ArrayList<List<String>> al) throws IOException {
		System.out.println(getClass().getSimpleName()+".createRepor00t");

		// SXSSF形式だとテンプレートはコピーできないため、新規作成
		SXSSFWorkbook wb = new SXSSFWorkbook();
		SXSSFSheet sheet = wb.createSheet();

		// タイトル情報
		int startTitleRow = 0;									// タイトル部の開始行

		Iterator<List<String>> itr = al.iterator();
		int count = -1;
		while (itr.hasNext()) {
			count++;	// カウンタ

			// 行生成
			SXSSFRow row = sheet.createRow(count + startTitleRow);

			// セル情報の取得
			List<String> colList = itr.next();
			Iterator<String> itrCols = colList.iterator();

			String vals = "";
			while (itrCols.hasNext()) {
				String val = (String) itrCols.next();
				val = this.getUnicodeReplace(val);		// ユニコード変換
				vals += "," + val;
			}
			// セル（列）生成
			SXSSFCell cell = row.createCell(0);
			cell.setCellValue(StringUtils.removeStart(vals, ","));
		}

		return wb;
	}

	private JSONObject getOptionTableData(HttpSession session) {
		JSONArray array = (JSONArray) session.getAttribute(DefineReport.ID_SESSION_OPT_TABLE);
		JSONObject datas = new JSONObject();
		JSONArray dataArray = new JSONArray();
		int rowCount = 0;
		if(array!=null){
			for (int i = 0; i < array.size(); i++) {
				rowCount += array.getJSONArray(i).size();
				dataArray.addAll(array.getJSONArray(i));
			}
			// 余白行
			rowCount++;
		}
		datas.put("count", rowCount);
		datas.put("data", dataArray);
		return datas;
	}

	private int setOptionTableData(HttpSession session, SXSSFWorkbook wb, SXSSFSheet sheet, JSONObject optDataTable, int startRowIdx) {
		if(optDataTable.isEmpty()){
			return 0;
		}

		Font font = wb.createFont();
		font.setFontName("ＭＳ Ｐゴシック");

		// タイトルスタイル設定用
		CellStyle styleTit = wb.createCellStyle();
		styleTit.setFont(font);										// フォント
		styleTit.setBorderTop(CellStyle.BORDER_THIN);				// 枠線上
		styleTit.setBorderBottom(CellStyle.BORDER_THIN);			// 枠線下
		styleTit.setBorderLeft(CellStyle.BORDER_THIN);				// 枠線左
		styleTit.setBorderRight(CellStyle.BORDER_THIN);				// 枠線右
		styleTit.setFillPattern(CellStyle.SOLID_FOREGROUND);		// 塗りつぶし
		styleTit.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());	// 色指定
		CellStyle styleTxt = wb.createCellStyle();
		styleTxt.setFont(font);										// フォント
		styleTxt.setBorderTop(CellStyle.BORDER_THIN);				// 枠線上
		styleTxt.setBorderBottom(CellStyle.BORDER_THIN);			// 枠線下
		styleTxt.setBorderLeft(CellStyle.BORDER_THIN);				// 枠線左
		styleTxt.setBorderRight(CellStyle.BORDER_THIN);				// 枠線右

		Set<String> sFormat = new HashSet<String>();
		for (int i = 0; i < optDataTable.optInt("count") -1; i++) {
			JSONArray colArray = optDataTable.optJSONArray("data").optJSONObject(i).optJSONArray("data");
			for (int j = 0; j < colArray.size(); j++) {
				sFormat.add(colArray.optJSONObject(j).optString("format"));
			}
		}
		sFormat.remove("");

		Map<String, CellStyle> itemFormat = new LinkedHashMap<String, CellStyle>();
		for(String format : sFormat){
			CellStyle styleTmp = wb.createCellStyle();
			styleTmp.setFont(font);										// フォント
			styleTmp.setBorderTop(CellStyle.BORDER_THIN);				// 枠線上
			styleTmp.setBorderBottom(CellStyle.BORDER_THIN);			// 枠線下
			styleTmp.setBorderLeft(CellStyle.BORDER_THIN);				// 枠線左
			styleTmp.setBorderRight(CellStyle.BORDER_THIN);				// 枠線右
			DataFormat formatTmp = wb.createDataFormat();
			if(StringUtils.indexOf(format, "0") > 0){
				styleTmp.setAlignment(CellStyle.ALIGN_RIGHT);
				styleTmp.setDataFormat(formatTmp.getFormat(format+";[Red]-"+format));
			}else if(StringUtils.indexOf(format, "\n") > 0){
				styleTmp.setDataFormat(formatTmp.getFormat(StringUtils.replace(format,"\n","")));
				styleTmp.setWrapText(true);
				styleTmp.setVerticalAlignment(VerticalAlignment.TOP);
			}else{
				styleTmp.setDataFormat(formatTmp.getFormat(format));
			}
			itemFormat.put(format, styleTmp);
		}

		LinkedHashMap<Integer, LinkedHashMap<Integer, CellRangeAddress>> mergedRegionMap = new LinkedHashMap<Integer, LinkedHashMap<Integer, CellRangeAddress>>();
		ArrayList<CellRangeAddress> mergedInfo = new ArrayList<CellRangeAddress>();
		for (int i = 0; i < optDataTable.optInt("count") -1; i++) {
			JSONArray colArray = optDataTable.optJSONArray("data").optJSONObject(i).optJSONArray("data");
			int rowIdx = startRowIdx + i;
			int colIdx = 0;
			// 行生成
			SXSSFRow row = sheet.createRow(rowIdx);
			for (int j = 0; j < colArray.size(); j++) {
				// セル（列）生成
				SXSSFCell cell = row.createCell(colIdx);

				// 非表示列はとばす
				while (sheet.isColumnHidden(colIdx)){
					// セル（列）生成
					colIdx++;
					cell = row.createCell(colIdx);
				}

				JSONObject colData = colArray.optJSONObject(j);
				String val = "", format = "";
				format = colData.optString("format");
				val = colData.optString("value");
				if(StringUtils.equals(colData.optString("type"),"title")){
					cell.setCellStyle(styleTit);
				}else if(StringUtils.equals(colData.optString("type"),"data")){
					cell.setCellStyle(itemFormat.get(format));
				}
				// 情報設定
				if(StringUtils.isNotEmpty(val)){
					if(StringUtils.indexOf(format, "0") > 0){
						try {
							cell.setCellValue(Double.valueOf(val.replace(",", "")));
						} catch (Exception e) {
							cell.setCellValue(val);
						}
					}else{
						cell.setCellValue(val);
					}
				}
				// セルの結合情報設定
				int rowspan = colData.optInt("rowspan", 1);
				int colspan = colData.optInt("colspan", 1);
				if (rowspan > 1 || colspan > 1) {
					int mStrtR = rowIdx;
					int mLastR = rowIdx + rowspan - 1;
					int mStrtC = colIdx;
					int mLastC = colIdx + colspan - 1;
					if(!mergedRegionMap.containsKey(mLastR)){
						mergedRegionMap.put(mLastR, new LinkedHashMap<Integer, CellRangeAddress>());
					}
					if(!mergedRegionMap.get(mLastR).containsKey(mLastC)){
						mergedRegionMap.get(mLastR).put(mLastC, new CellRangeAddress(mStrtR, mLastR, mStrtC, mLastC));
						// 結合されたセルを作成するため
						mergedInfo.add(new CellRangeAddress(mStrtR, mLastR, mStrtC, mLastC));
					}
				}
				colIdx++;

				// 結合されているセルを作成
				for (CellRangeAddress ca : mergedInfo) {
					if(rowIdx >= ca.getFirstRow() && rowIdx <= ca.getLastRow()
							&& colIdx >= ca.getFirstColumn() && colIdx <= ca.getLastColumn()){
						for (int k = ca.getFirstColumn(); k <= ca.getLastColumn(); k++) {
							if(!(rowIdx == ca.getFirstRow() && k == ca.getFirstColumn())){
								SXSSFCell addCell = row.createCell(colIdx);
								addCell.setCellStyle(styleTxt);
								colIdx++;
							}
						}
					}
				}
			}
			// 結合実施
			if(mergedRegionMap.containsKey(rowIdx)){
				for(Integer key :mergedRegionMap.get(rowIdx).keySet()){
					sheet.addMergedRegion(mergedRegionMap.get(rowIdx).get(key));
				}
			}
		}
		return 0;
	}
	/**
	 * ユニコード変換
	 * @param val
	 * @return
	 */
	private String getUnicodeReplace(String val) {
		return val
				.replace("&#9728;", "☀")
				.replace("&#9729;", "☁")
				.replace("&#9730;", "☂")
				.replace("&#9731;", "☃");
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
