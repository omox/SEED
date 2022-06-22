/*
 * 作成日: 2006/08/30
 *
 */
package servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import authentication.bean.User;
import authentication.defines.Consts;
import authentication.defines.Message;
import common.CmnDate;
import common.DefineReport;
import common.Defines;
import common.ItemList;
import dao.DBConnection;
import dao.ReportTG040Dao;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * レポート画面表示制御クラス <br />
 * レポート画面に表示するレポートを制御するクラス <br />
 */
public class ReportServlet extends HttpServlet {

	/**
	 *
	 */
	private static final long serialVersionUID = -5961333752928159562L;


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
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.doPost(request, response);
	}

	/**
	 * リクエスト時処理 <br />
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		/**
		 * 処理準備
		 */
		request.setCharacterEncoding("UTF-8");
		HttpSession session = request.getSession(); // セッションの取得
		String strPage = new String(); // ページ遷移先

		try {
			if (session.getAttribute(Defines.ID_REQUEST_USER_ID) == null) {
				/* エラーメッセージの設定 */
				request.setAttribute(Defines.STR_FRM_MSGTYPE, Defines.STR_VAL_MSGTYPE_ERR);
				request.setAttribute(Defines.STR_FRM_MSG, Message.ERR_SES_TIMEOUT);
				// ページ遷移先の設定
				strPage = Defines.STR_JSP_LOGIN;

			} else {
				/**
				 * レポート表示処理
				 */
				// セッション・クリア
				session.removeAttribute(DefineReport.ID_SESSION_STORAGE);
				session.removeAttribute(DefineReport.ID_SESSION_TABLE);
				session.removeAttribute(DefineReport.ID_SESSION_TABLE+"2");
				session.removeAttribute(DefineReport.ID_SESSION_WHERE);
				session.removeAttribute(DefineReport.ID_SESSION_META);
				session.removeAttribute(DefineReport.ID_SESSION_HEADER);
				session.removeAttribute(DefineReport.ID_SESSION_OPTION);
				session.removeAttribute(DefineReport.ID_SESSION_OPT_TABLE);
				session.removeAttribute(DefineReport.ID_SESSION_FILE);

				String inclurepname = (String)request.getSession().getAttribute(Defines.ID_REQUEST_JSP_REPORT);

				// カスタムプロパティの確認
				String[] inclurepnames = request.getSession().getAttribute(Defines.ID_REQUEST_CUSTOM_REPORT).toString().split(",");
				if (ArrayUtils.isNotEmpty(inclurepnames) && !"".equals(inclurepnames[0])) {
					// カスタムプロパティで複数指定の場合、書き換え
					inclurepname = inclurepnames[0];
					request.getSession().setAttribute(Defines.ID_REQUEST_JSP_REPORT,inclurepname);
				}

				String SendParamBef = (String)session.getAttribute(Defines.ID_REQUEST_SEND_PARAM);
				//System.out.println(Defines.ID_REQUEST_SEND_PARAM + ":" + SendParam);
				String SendParam = "";
				if(StringUtils.isNotEmpty(SendParamBef)){
					SendParam = SendParamBef.replaceAll("'", "&#39;");
				}

				// authentication.servlet.ReportServlet にて null チェック済
				String SendMode = (String)session.getAttribute(Defines.ID_REQUEST_SEND_MODE);
				//System.out.println(Defines.ID_REQUEST_SEND_MODE + ":" + SendMode);


				// 初回検索条件の取得
				JSONArray initParam = getInitSearchParam(inclurepname, SendParam);
				JSONArray initDtParam = getSendParams(inclurepname);

				// ユーザ情報.店舗コード（予備２）を店舗の初期値とする
				User lusr = (User)request.getSession().getAttribute(Consts.STR_SES_LOGINUSER);
				JSONArray initTenpoParam = getInitTenpoParams(inclurepname, lusr.getYobi2_());
				initParam.addAll(initTenpoParam);	// 店舗

				if (SendMode.length() == 0 ) {
					String Data="";

					// TODO:いなげや特処理:メニューから遷移してきた場合は、情報引き継がない（ボタンIDが設定されている場合のみ実行）
					// TODO:いなげや特処理:戻る系遷移用に、前回条件にかかわらない一部情報引継
					if(StringUtils.isNotEmpty(SendParam) && StringUtils.contains(SendParam, "sendBtnid")){
						/** 前回検索条件の取得 */
						ArrayList<String> results =  getBeforeSearchParam(request);
						/** 最初回検索条件のパラメータ設定 */
						if (results.size() == 0 && initParam.size() > 0) {
							results.add(initParam.toString());
						}
						if (results.size() > 0) {
							// 戻り値あり
							Data = results.toString().substring(1, results.toString().length()-1);//results.get(0);
							Data = "," + Data.substring(1, Data.length()-1);
						}

						// 戻る系遷移時の引継ぎ対象
						String[] targetId = new String[]{ "sendBtnid", "callpage", "reportYobi1" };
						// 遷移引数から引継ぎ対象条件を取得
						JSONArray paramArray = JSONArray.fromObject(SendParam);
						for (Iterator it = paramArray.iterator(); it.hasNext();) {
							JSONObject paramJson = (JSONObject) it.next();
							if ( ArrayUtils.contains(targetId, paramJson.getString("id")) ) {
								Data += "," + paramJson.toString();
 							}
						}
					}

					/** 検索条件のパラメータ設定（日付） */
					JSONArray param = initDtParam;

					/** 前回検索条件＋日付条件の合成 */
					session.setAttribute(Defines.ID_REQUEST_SEND_PARAM,
						"[" + param.toString().substring(1, param.toString().length()-1) + Data + "]" );

				} else if(StringUtils.equals(SendMode, Defines.SendMode.TAB.getVal())){

					// タブ遷移時の引継ぎ対象条件
					String[] targetId = new String[]{
							Defines.ID_REQUEST_SEND_MODE, DefineReport.Select.KIKAN.getObj()
							, DefineReport.Select.KIKAN_F.getObj(), DefineReport.Select.KIKAN_T.getObj()
							, DefineReport.Select.YM_F.getObj(), DefineReport.Select.YM_T.getObj()
							, DefineReport.Select.YEAR_F.getObj(), DefineReport.Select.YEAR_T.getObj()
							, DefineReport.Select.WEEK_F.getObj(), DefineReport.Select.WEEK_T.getObj()
							, DefineReport.Select.YMD_F.getObj(), DefineReport.Select.YMD_T.getObj()
						};


					/** 前回検索条件の取得 */
					ArrayList<String> results =  getBeforeSearchParam(request);


					/** 前回検索条件＋タブ遷移条件の合成 */
					JSONArray param = new JSONArray();

					// タブ遷移引数から引継ぎ対象条件を取得
					JSONArray paramArray = JSONArray.fromObject(SendParam);
					for (Iterator it = paramArray.iterator(); it.hasNext();) {
						JSONObject paramJson = (JSONObject) it.next();
						if ( ArrayUtils.contains(targetId, paramJson.getString("id")) ) {
							param.add(paramJson);
						}
					}
					// 前回検索条件から引継ぎ対象外条件を取得
					if (results.size() > 0) {	// 前回検索条件有
						JSONArray befArray = JSONArray.fromObject(results.get(0));
						for (Iterator it = befArray.iterator(); it.hasNext();) {
							JSONObject paramJson = (JSONObject) it.next();
							if ( !ArrayUtils.contains(targetId, paramJson.getString("id")) ) {
								param.add(paramJson);
							}
						}
					}

					/** 帳票中からの画面遷移 */
					session.setAttribute(Defines.ID_REQUEST_SEND_PARAM, "[" + param.toString().substring(1, param.toString().length()-1) + "]");

				} else if(StringUtils.equals(SendMode, Defines.SendMode.BACK.getVal())){

					// BACK時
					// 遷移引数から引継ぎ対象
					JSONArray paramArray = JSONArray.fromObject(SendParam);

					JSONObject repinfo = new JSONObject();
					for (Iterator it = paramArray.iterator(); it.hasNext();) {
						JSONObject paramJson = (JSONObject) it.next();
						if (StringUtils.equals("repinfo", paramJson.getString("id"))) {
							JSONArray paramArray2 = paramJson.optJSONArray("value");
							for (Iterator it2 = paramArray2.iterator(); it2.hasNext();) {
								JSONObject paramJson2 = (JSONObject) it2.next();
								if (StringUtils.equals(inclurepname, paramJson2.getString("id"))) {
									repinfo = paramJson2;
									break;
								}
							}
							break;
						}
					}
					JSONArray param = new JSONArray();
					if(repinfo.containsKey("value")){
						JSONObject states = repinfo.optJSONObject("value");
						JSONArray srccondArray = states.optJSONArray("SRCCOND");
						JSONArray tmpcondArray = states.optJSONArray("TMPCOND");
						if(srccondArray.size() > 0){
							param.addAll(srccondArray);
							// 戻る系遷移時の引継ぎ対象
							String[] targetId = new String[]{ "sendBtnid", "callpage", "reportYobi1"};
							// 遷移引数から引継ぎ対象条件を取得
							for (Iterator it = paramArray.iterator(); it.hasNext();) {
								JSONObject paramJson = (JSONObject) it.next();
								if ( ArrayUtils.contains(targetId, paramJson.getString("id")) ) {
									param.add(paramJson);
	 							}
							}
						}

						if(tmpcondArray.size() > 0){
							String[] targetId = new String[]{ "scrollToIndex"};
							// 遷移引数から引継ぎ対象条件を取得
							for (Iterator it = tmpcondArray.iterator(); it.hasNext();) {
								JSONObject paramJson = (JSONObject) it.next();
								for (int i = 0; i < targetId.length; i++) {
									if (paramJson.getString("id").contains(targetId[i])) {
										param.add(paramJson);
									}
								}
							}
						}
					}
					// 必須引継ぎ情報
					String[] targetId = new String[]{ "repinfo" };
					// 遷移引数から引継ぎ対象条件を取得
					for (Iterator it = paramArray.iterator(); it.hasNext();) {
						JSONObject paramJson = (JSONObject) it.next();
						if ( ArrayUtils.contains(targetId, paramJson.getString("id")) ) {
							param.add(paramJson);
						}
					}

					/** 帳票中からの画面遷移 */
					session.setAttribute(Defines.ID_REQUEST_SEND_PARAM, param.toString());

				} else {
					/** 帳票中からの画面遷移 */
					session.setAttribute(Defines.ID_REQUEST_SEND_PARAM, SendParam);

					// WEB商談
					if (ArrayUtils.contains(new String[]{DefineReport.ID_PAGE_X246, DefineReport.ID_PAGE_X247}, inclurepname)) {
						// 「件名No」をセッションで保持
						JSONArray paramArray = JSONArray.fromObject(SendParam);
						for (Iterator<?> it = paramArray.iterator(); it.hasNext();) {
							JSONObject paramJson = (JSONObject) it.next();
							if ( StringUtils.equals(DefineReport.Hidden.NO_TEIAN.getObj(), paramJson.optString("id")) ) {
								session.setAttribute(DefineReport.Hidden.NO_TEIAN.getObj(), paramJson.optString("value"));
							}
							if ( StringUtils.equals(DefineReport.Hidden.TORIHIKI.getObj(), paramJson.optString("id")) ) {
								session.setAttribute(DefineReport.Hidden.TORIHIKI.getObj(), paramJson.optString("value"));
							}
							if ( StringUtils.equals(DefineReport.Hidden.STCD_TEIAN.getObj(), paramJson.optString("id")) ) {
								session.setAttribute(DefineReport.Hidden.STCD_TEIAN.getObj(), paramJson.optString("value"));
							}
							// 「ページ」をセッションで保持
							if ( StringUtils.equals("callpage", paramJson.optString("id")) ) {
								session.setAttribute("frompage", paramJson.optString("value"));
							}
						}
					}else if (ArrayUtils.contains(new String[]{DefineReport.ID_PAGE_X249, DefineReport.ID_PAGE_X250, DefineReport.ID_PAGE_X251}, inclurepname)) {
						JSONArray paramArray = JSONArray.fromObject(SendParam);
						for (Iterator<?> it = paramArray.iterator(); it.hasNext();) {
							JSONObject paramJson = (JSONObject) it.next();
							// 「件名No」をセッションで保持
							if ( StringUtils.equals(DefineReport.Hidden.NO_TEIAN.getObj(), paramJson.optString("id")) ) {
								session.setAttribute(DefineReport.Hidden.NO_TEIAN.getObj(), paramJson.optString("value"));
							}
							// 「状態」をセッションで保持
							if ( StringUtils.equals(DefineReport.Hidden.STCD_SHIKAKARI.getObj(), paramJson.optString("id")) ) {
								session.setAttribute(DefineReport.Hidden.STCD_SHIKAKARI.getObj(), paramJson.optString("value"));
							}
							// 「ページ」をセッションで保持
							if ( StringUtils.equals("callpage", paramJson.optString("id")) ) {
								session.setAttribute("frompage", paramJson.optString("value"));
							}
						}
					}
				}

				initParam.addAll(initDtParam);		// 期間
				session.setAttribute(Defines.ID_REQUEST_INIT_PARAM, initParam.toString());

				//System.out.println(Defines.ID_REQUEST_SEND_PARAM + "(LAST):" + session.getAttribute(Defines.ID_REQUEST_SEND_PARAM));

				/** 遷移先設定 */
				strPage = "/jsp/" + inclurepname + ".jsp";

			}

		} catch (Exception e) {

			/**
			 * 例外処理
			 */
			// スタックトレースの出力
			e.printStackTrace();
			// エラーメッセージの設定
			request.setAttribute(Defines.STR_FRM_MSGTYPE, Defines.STR_VAL_MSGTYPE_ERR);
			request.setAttribute(Defines.STR_FRM_MSG, e.getMessage());
			// ページ遷移先の設定
			strPage = Defines.STR_JSP_ERROR;

		}

		/**
		 * ページ遷移
		 */
		getServletContext().getRequestDispatcher(strPage).forward(request, response);

	}

	/**
	 * 前回検索条件の取得
	 *
	 * @param report
	 * @return String
	 */
	private ArrayList<String> getBeforeSearchParam(HttpServletRequest request) {
		/** 前回検索条件の取得 */
		/** SQL構文 */
		String sqlcommand = "SELECT SNAPSHOT FROM KEYSYS.SYS_SNAPSHOT WHERE CD_USER=? and CD_REPORT=?";

		/** 配列準備 */
		ArrayList<Object> paramData = new ArrayList<Object>();
		paramData.add(0, (String)request.getSession().getAttribute(Defines.ID_REQUEST_CUSTOM_USER));
		paramData.add(1, (String)request.getSession().getAttribute(Defines.ID_REQUEST_REPORT_NO));

		/** SQL実行 */
		ArrayList<String> results = selectQuery(sqlcommand, paramData);

		return results;
	}


	/**
	 * 初回検索条件の取得
	 *
	 * @param report
	 * @param sendParam
	 * @return String
	 */
	private JSONArray getInitSearchParam(String report, String sendParam) {
		JSONArray array = new JSONArray();
		JSONObject obj = new JSONObject();
		DefineReport.OptionKikanYMWD kikan = null;

		if (DefineReport.ID_PAGE_001.equals(report)) {
			kikan = DefineReport.OptionKikanYMWD.DAY;

		}else if (DefineReport.ID_PAGE_TG040.equals(report)) {

			JSONArray paramArray = JSONArray.fromObject(sendParam);
			HashMap<String, String> hmap = new HashMap<String, String>();
			for (int i=0; i < paramArray.size(); i++) {
				JSONObject o = paramArray.optJSONObject(i);
				String id = o.optString("id");
				if(StringUtils.equals(DefineReport.InpText.MOYSSTDT.getObj(), id)){
					hmap.put("MOYSSTDT", o.optString("value"));
					break;
				}
			}
			String moyscd = new ReportTG040Dao(Defines.STR_JNDI_DS).getInitMOYSCD(hmap);

			// 表示項目
			obj = new JSONObject();
			obj.put("id", DefineReport.InpText.MOYSCD.getObj());
			obj.put("value", moyscd);
			obj.put("text", "");
			array.add(obj);
		}

		if(kikan!=null){
			obj = new JSONObject();
			obj.put("id", DefineReport.Select.KIKAN.getObj());
			obj.put("value", kikan.getVal());
			obj.put("text", kikan.getTxt());
			array.add(obj);
		}
		return array;
	}


	/**
	 * 検索条件の店舗パラメータ設定
	 * @param report	レポートコード
	 * @param tenpocd	店舗コード
	 * @return JSONArray
	 */
	private JSONArray getInitTenpoParams(String report, String tenpocd) {
		JSONArray array = new JSONArray();
		try {
			if (tenpocd.isEmpty()) {
				// 本部ユーザの初回店舗ID
				ItemList iL = new ItemList();
				ArrayList<List<String>> results = iL.selectArray(DefineReport.ID_SQL_TENPO_MIN_CD,
						new ArrayList<String>(), Defines.STR_JNDI_DS);
				if (results.size() > 0) {
					// 戻り値あり
					tenpocd = results.get(1).get(0);
				}
			}
			// 店舗コードの設定
			if (!tenpocd.isEmpty()) {
				JSONObject obj = new JSONObject();
				obj.put("id", DefineReport.Select.TENPO.getObj());
				obj.put("value", "['"+tenpocd+"']");
				obj.put("text", tenpocd);
				array.add(obj);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return array;
	}

	/**
	 * 検索条件のパラメータを取得（TODO:日付条件のみで）
	 *
	 * @param report
	 * @return JSONArray
	 */
	private JSONArray getSendParams(String report){
		JSONArray array = new JSONArray();

		try {
			CmnDate cd = new CmnDate();

			// 日付初期値
			String dt = cd.getYesterday();
			String dtfrom = dt;

			String sqlCommand = "";
			ItemList iL = new ItemList();

			// 年月日
//			if (DefineReport.ID_PAGE_004.equals(report)){
//				sqlCommand = DefineReport.ID_SQL_KIKAN_DAY_INIT_MY;
//			}
			if (!sqlCommand.isEmpty()) {
				ArrayList<List<String>> results = iL.selectArray(sqlCommand, new ArrayList<String>(), Defines.STR_JNDI_DS);
				if (results.size() > 1) {
					// 戻り値あり
					dt = results.get(1).get(0);
					dtfrom = results.get(1).get(1);
				}
			}

			JSONObject obj = new JSONObject();
			obj.put("id", DefineReport.Select.YMD_F.getObj());
			obj.put("value", dt);
			obj.put("text", dt);
			array.add(obj);

			obj = new JSONObject();
			obj.put("id", DefineReport.Select.YMD_T.getObj());
			obj.put("value", dtfrom);
			obj.put("text", dtfrom);
			array.add(obj);

			ArrayList<String> param = new ArrayList<String>();
			if (DefineReport.ID_PAGE_001.equals(report)){
				param.add(CmnDate.dateFormatYM(CmnDate.getMonthAddedDate(new Date(), +1)));
				param.add(CmnDate.dateFormatYM(new Date()));
				sqlCommand = DefineReport.ID_SQL_KIKAN_MONTH_INIT;
			}
			if (!sqlCommand.isEmpty()) {
				ArrayList<List<String>> results2 = iL.selectArray(sqlCommand, param, Defines.STR_JNDI_DS);
				obj = new JSONObject();
				obj.put("id", DefineReport.Select.YM_F.getObj());
				obj.put("value", results2.get(1).get(0));
				array.add(obj);
				obj = new JSONObject();
				obj.put("id", DefineReport.Select.YM_T.getObj());
				obj.put("value", results2.get(1).get(1));
				array.add(obj);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return array;
	}

	/**
	 * 検索実行
	 *
	 * @param sqlCommand
	 * @param paramData
	 * @return
	 */
	private ArrayList<String> selectQuery(String sqlCommand, ArrayList<Object> paramData) {

		ArrayList<String> results = new ArrayList<String>();
		// 検索
		Connection con = null; // コネクション
		try {
			// コネクションの取得
			con = DBConnection.getConnection(Defines.STR_JNDI_DS);

			// 実行SQL設定
			PreparedStatement stmt = con.prepareStatement(sqlCommand);

			// パラメータ設定
			for (int i=0; i < paramData.size(); i++) {
				stmt.setString((i+1), (String)paramData.get(i));
			}
			// SQL実行
			ResultSet rs = stmt.executeQuery();

			// カラム数
			ResultSetMetaData rsmd = rs.getMetaData();
			int sizeColumn = rsmd.getColumnCount();

			// 結果の取得
			while (rs.next()) {
				StringBuffer sb = new StringBuffer();
				for ( int i=1; i <= sizeColumn; i++ ) {
					sb.append(rs.getString(i));
				}
				results.add(sb.toString());
			}

			con.commit();
			con.close();

		} catch (Exception e) {
			/* 接続解除 */
			if (con != null){
				try {
					con.rollback();
					con.close();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}
			e.printStackTrace();
		}

		return results;
	}

}
