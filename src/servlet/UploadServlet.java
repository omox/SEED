/*
 * 作成日: 2008/08/01
 *
 */
package servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.StringUtils;

import authentication.bean.User;
import authentication.defines.Consts;
import common.ChkUsableTime;
import common.DefineReport;
import common.Defines;
import common.MessageUtility;
import common.MessageUtility.MsgKey;
import dao.ReportBM007Dao;
import dao.ReportBW000Dao;
import dao.ReportIT031Dao;
import dao.ReportIT032Dao;
import dao.ReportJU037Dao;
import dao.ReportRP011Dao;
import dao.ReportSK000Dao;
import dao.ReportSO004Dao;
import dao.ReportST024Dao;
import dao.ReportTR006Dao;
import dao.Reportx004Dao;
import dao.Reportx051Dao;
import dao.Reportx248Dao;
import dto.JQEasyModel;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

public class UploadServlet extends HttpServlet {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	final static String FILE_NAME_STRING = "filename=";
	final static String CONTENT_TYPE_HEADER="Content-Type: ";

	/**
	 * 初期化 <br />
	 */
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	/**
	 * 破棄 <br />
	 */
	public void destroy() {
		super.destroy();
	}

	/**
	 * リクエスト時処理 <br />
	 *
	 * @param request リクエスト
	 * @param response レスポンス
	 * @throws ServletException サーブレットエラー
	 * @throws IOException 入出力エラー
	 */
	@SuppressWarnings("unchecked")
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// 文字変換コード設定【重要】
		request.setCharacterEncoding("UTF-8");

		// セッション情報
		HttpSession session = request.getSession(false);
		String session_prefix = DefineReport.ID_SESSION_PREFIX_TEMP + getClass().getSimpleName();

		// レコード情報の格納先(JSONObject)作成
		JSONObject jsonOB = new JSONObject();

		// jqEasy 用 JSON モデル作成
		JQEasyModel json = new JQEasyModel();

		// 利用時間外の確認
		String reportNo = "";
		if(session.getAttribute(Consts.STR_SES_REPORT_NO) != null){
			reportNo = session.getAttribute(Consts.STR_SES_REPORT_NO).toString();
		}
		String fromData		= getServletContext().getInitParameter(Consts.FROM_DATA);
		String toData		= getServletContext().getInitParameter(Consts.TO_DATA);
		ChkUsableTime sys	= new ChkUsableTime(fromData, toData);
		boolean execut		= true;

		if(sys.isTimeOut(reportNo)){
			json.setMessage(DefineReport.ID_MSG_TIMEOUT_EXCEPTION);
			JSONObject option = new JSONObject();
			JSONArray msg = new JSONArray();
			msg.add(MessageUtility.getMsg(DefineReport.ID_MSG_TIMEOUT_EXCEPTION));
			option.put(MsgKey.E.getKey(), msg);
			json.setOpts(option);
			execut = false;
		}

		// セッション情報取得
		User userInfo = (User)request.getSession().getAttribute(Consts.STR_SES_LOGINUSER);


		String menuKbn = "-1";

		// 本部マスタ
		if ((!StringUtils.isEmpty(userInfo.getYobi6_()) && !userInfo.getYobi6_().equals("-1")) || StringUtils.isEmpty(userInfo.getYobi6_())) {
			menuKbn = "4";

		// 本部特売
		} else if ((!StringUtils.isEmpty(userInfo.getYobi8_()) && !userInfo.getYobi8_().equals("-1")) || StringUtils.isEmpty(userInfo.getYobi8_())) {
			menuKbn = "5";
		}

		// 本部マスタ画面の操作でかつ更新権限がない or 特売マスタ画面の操作でかつ更新権限がない
		if ((menuKbn.equals("4") && StringUtils.isEmpty(userInfo.getYobi6_())) ||
				(menuKbn.equals("5") && StringUtils.isEmpty(userInfo.getYobi8_()))) {

			json.setMessage(DefineReport.ID_MSG_TIMEOUT_EXCEPTION);
			JSONObject option = new JSONObject();
			option.put(MsgKey.E.getKey(), MessageUtility.getDbMessageIdObj("E00012", new String[]{}));
			json.setOpts(option);
			execut = false;
		}

		if(execut){
			HashMap<String, String> map = new HashMap<String, String>();
			String upfilepath = null;
			String upfilename = null;
			String objPrefix = "";

			try {
				if (ServletFileUpload.isMultipartContent(request)) {
					// 一時フォルダ存在チェック
					//Fileオブジェクトを生成する
					File f = new File(getServletContext().getRealPath(Defines.ID_UPLOAD_FILE_PATH));
					if (!f.exists()) {
						//フォルダ作成実行
						f.mkdirs();
					}

					// ファクトリー生成
					DiskFileItemFactory factory = new DiskFileItemFactory();
					factory.setSizeThreshold(1426);
					factory.setRepository(f);				//一時的に保存する際のディレクトリ
					ServletFileUpload upload = new ServletFileUpload(factory);
					upload.setSizeMax(-1);
					upload.setFileSizeMax(-1);

					List<FileItem> items = upload.parseRequest(request);

					// 全フィールドに対するループ
					FileItem fItem = null;
					for (FileItem item : items) {
						String nm = null;
						if (item.isFormField()) {
							// type="file"以外のフィールド
							nm = item.getString("UTF-8");
						} else {
							// type="file"のフィールド
							fItem = item;
							nm = item.getName();
						}
						map.put(item.getFieldName(), nm);
					}

					String report = map.get("report");

					// ファイル名の指定（拡張子無し前提）がある場合はそちらを採用する
					SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
					upfilename = sdf.format(new Date()) + "_" + session.getId() + "." + StringUtils.substringAfterLast((new File(fItem.getName())).getName(),".");
					upfilepath = getServletContext().getRealPath( Defines.ID_UPLOAD_FILE_PATH + "/" + upfilename);

					// ファイルを作業フォルダに保存
					fItem.write(new File(upfilepath));

					String JNDIname = Defines.STR_JNDI_DS;

					if (DefineReport.ID_PAGE_X004.equals(report)) {			// 外部データ取込
						json = new Reportx004Dao(JNDIname).upload(session, userInfo, map, upfilepath);
					}

					if (DefineReport.ID_PAGE_BM007.equals(report)) {		// 外部データ取込
						json = new ReportBM007Dao(JNDIname).upload(session, userInfo, map, upfilepath);
					}

					if (DefineReport.ID_PAGE_TR006.equals(report)) {		// 外部データ取込
						json = new ReportTR006Dao(JNDIname).upload(session, userInfo, map, upfilepath);
					}

					if (DefineReport.ID_PAGE_SK000.equals(report)) {		// 外部データ取込
						json = new ReportSK000Dao(JNDIname).upload(session, userInfo, map, upfilepath);
					}

					if (DefineReport.ID_PAGE_IT031.equals(StringUtils.remove(report, "win"))) {		// 外部データ取込
						json = new ReportIT031Dao(JNDIname).convert(session, userInfo, map, upfilepath);
					}
					if (DefineReport.ID_PAGE_IT032.equals(StringUtils.remove(report, "win"))) {		// 外部データ取込
						json = new ReportIT032Dao(JNDIname).convert(session, userInfo, map, upfilepath);
					}

					if (DefineReport.ID_PAGE_RP011.equals(report)) {		// 外部データ取込
						json = new ReportRP011Dao(JNDIname).upload(session, userInfo, map, upfilepath);
					}

					if (DefineReport.ID_PAGE_SO004.equals(report)) {		// 外部データ取込
						json = new ReportSO004Dao(JNDIname).upload(session, userInfo, map, upfilepath);
					}
					if (DefineReport.ID_PAGE_BW000.equals(report)) {		// 外部データ取込
							json = new ReportBW000Dao(JNDIname).upload(session, userInfo, map, upfilepath);
					}
					if (DefineReport.ID_PAGE_X051.equals(report)) {		// 外部データ取込
						json = new Reportx051Dao(JNDIname).upload(session, userInfo, map, upfilepath);
					}

					if (DefineReport.ID_PAGE_JU037.equals(report)) {		// 外部データ取込
						json = new ReportJU037Dao(JNDIname).upload(session, userInfo, map, upfilepath);
					}

					if (DefineReport.ID_PAGE_JU038.equals(report)) {		// 外部データ取込
						json = new ReportJU037Dao(JNDIname).upload(session, userInfo, map, upfilepath);
					}

					if (DefineReport.ID_PAGE_ST024.equals(report)) {		// 外部データ取込
						json = new ReportST024Dao(JNDIname).upload(session, userInfo, map, upfilepath);
					}

					if (DefineReport.ID_PAGE_X248.equals(report)) {			// 外部データ取込
						json = new Reportx248Dao(JNDIname).upload(session, userInfo, map, upfilepath);
					}

					// 表示するメッセージがない場合、エラーメッセージをセット
					if(StringUtils.isEmpty(json.getMessage())){
						//json.setMessage(MessageUtility.getMsgText(Msg.E00003.getVal(), ""));
					}
				}

			} catch (OutOfMemoryError e) {
				e.printStackTrace();
				//json.setMessage(MessageUtility.getMsgText(Msg.E00003.getVal(), "\n" + e.getMessage()));
			} catch (Throwable e) {
				e.printStackTrace();
				//json.setMessage(MessageUtility.getMsgText(Msg.E00003.getVal(), "\n" + e.getMessage()));
			}

			try{
				this.deleteFile(upfilepath);
			} catch (Throwable e) {}
		}

		// JSON 形式へ変換
		jsonOB = JSONObject.fromObject(JSONSerializer.toJSON(json));
		if (DefineReport.ID_DEBUG_MODE) System.out.println(jsonOB.toString());

		// JSON データのロード
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter pw = response.getWriter();
		pw.print(jsonOB);
	}

	private void deleteFile(String upfilepath) {
		//削除対象ファイル
		File file = new File(upfilepath);
		if(file.delete()){
			//ファイル削除成功
			System.out.println("ファイル削除成功:"+upfilepath);
		}else{
			//ファイル削除失敗
			System.out.println("ファイル削除失敗:"+upfilepath);
		}
	}
}