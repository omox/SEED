/**
 *
 */
package dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;

import authentication.bean.User;
import authentication.defines.SQL;
import common.CmnDate;
import common.DefineReport;
import common.Defines;
import common.ItemList;
import common.JsonArrayData;
import common.MessageUtility;
import common.MessageUtility.Msg;
import common.MessageUtility.MsgKey;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 */
public class Reportx245Dao extends ItemDao {

	/**
	 * インスタンスを生成します。
	 * @param source
	 */
	public Reportx245Dao(String JNDIname) {
		super(JNDIname);
	}

	/**
	 * 検索実行
	 *
	 * @return
	 */
	public boolean selectBy() {

		// 検索コマンド生成
		String command = createCommand();

		// 出力用検索条件生成
		outputQueryList();

		// 検索実行
		return super.selectBySQL(command);
	}


	private String createCommand() {
		// ログインユーザー情報取得
		User userInfo = getUserInfo();
		if(userInfo==null){
			return "";
		}

		String szTommorow	= (new CmnDate()).getTomorrow();	// 明日日付
		String szTorihiki	= getMap().get("TORIHIKI");			// 取引先
		String szTeianNo	= getMap().get("TEIANNO");			// 件名No
		String szTeian		= getMap().get("TEIAN");			// 提案件名
		String szState		= getMap().get("STATE");			// 状態

		// 検索文（並び替え＆取得範囲制限）
		StringBuffer sbSQL = new StringBuffer();
		sbSQL.append("with WK_FPSSMS as (select SSM.* from INAMS.MSTSIR SSM");
		sbSQL.append(" inner join (select MAX(STARTDT) as STARTDT, SIRCD from INAMS.MSTSIR where STARTDT <= "+szTommorow+" group by SIRCD) TMP on TMP.STARTDT = SSM.STARTDT and TMP.SIRCD = SSM.SIRCD");
		sbSQL.append(")");
		sbSQL.append(" select");
		sbSQL.append(" MAX(TIK.TIKSTCD)");											// F1	：状態
		sbSQL.append(", ''");														// F2	：次画面
		sbSQL.append(", TIK.TIKKNNO");												// F3	：件名No
		sbSQL.append(", MAX(TIK.TIKTTCD)");											// F4	：取引先
		sbSQL.append(", MAX(TIK.TIKKNNM)");											// F5	：提案件名
		sbSQL.append(", COUNT(TIT.SHNCD)");										// F6	：提案数
		sbSQL.append(", COUNT(case when TIT.TITSTCD = 4 then TIT.SHNCD end)");	// F7	：完了数
		sbSQL.append(", COUNT(case when TIT.TITSTCD = 9 then TIT.SHNCD end)");	// F8	：却下数
		sbSQL.append(", COUNT(case when TIT.TITSTCD = 3 then TIT.SHNCD end)");	// F9	：仕掛数
		sbSQL.append(", COUNT(case when TIT.TITSTCD = 2 then TIT.SHNCD end)");	// F10	：確定数
		sbSQL.append(", COUNT(case when TIT.TITSTCD = 1 then TIT.SHNCD end)");	// F11	：作成数
		sbSQL.append(", TO_CHAR(MAX(TIK.TIKEDDT), 'yyyymmdd')");					// F12	：完了日
		sbSQL.append(", MAX(USR.NM_FAMILY) || ' ' || MAX(USR.NM_NAME)");			// F13	：作成者
		sbSQL.append(" from INAWS.PIMTIK TIK");
		if (!szTorihiki.toString().equals(DefineReport.Values.ALL.getVal())) {
			sbSQL.append(" inner join WK_FPSSMS SSM on TIK.TIKTTCD = SSM.SIRCD and TIK.TIKTTCD = '"+szTorihiki+"'");
		} else {
			sbSQL.append(" inner join WK_FPSSMS SSM on TIK.TIKTTCD = SSM.SIRCD");
		}

		if(StringUtils.isNotEmpty(szTeianNo)){		// 条件：件名No
			sbSQL.append(" and TIK.TIKKNNO = "+szTeianNo);
		}
		if(StringUtils.isNotEmpty(szTeian)){		// 条件：提案件名
			sbSQL.append(" and TIK.TIKKNNM like '%"+szTeian.replaceAll("'", "''")+"%'");
		}
		if(StringUtils.isNotEmpty(szState)){		// 条件：状態
			sbSQL.append(" and TIK.TIKSTCD = '"+szState+"'");
		}

		sbSQL.append(" left join INAWS.PIMTIT TIT on TIK.TIKKNNO = TIT.TITKNNO");
		sbSQL.append(" left join " + SQL.system_schema + ".SYS_USERS USR on TIK.T1SKID = USR.USER_ID");
		sbSQL.append(" group by TIK.TIKKNNO");
		sbSQL.append(" order by MAX(TIK.TIKSTCD), TIK.TIKKNNO");

		if (DefineReport.ID_DEBUG_MODE)	System.out.println(sbSQL.toString());
		return sbSQL.toString();
	}

	private void outputQueryList() {

		// 検索条件の加工クラス作成
		JsonArrayData jad = new JsonArrayData();
		jad.setJsonString(getJson());

		// 保存用 List (検索情報)作成
		setWhere(new ArrayList<List<String>>());
		List<String> cells = new ArrayList<String>();

		// 取引先
		cells = new ArrayList<String>();
		cells.add( DefineReport.Select.TORIHIKI.getTxt() );
		cells.add( jad.getJSONText(DefineReport.Select.TORIHIKI.getObj()) );
		getWhere().add(cells);

		// 共通箇所設定
		createCmnOutput(jad);

	}

	/**
	 * 更新処理
	 * @param request
	 * @param session
	 * @param map
	 * @param userInfo
	 * @return
	 */
	public JSONObject update(HttpServletRequest request, HttpSession session, HashMap<String, String> map, User userInfo) {
		// メッセージ格納用
		JSONObject rtn = new JSONObject();
		JSONArray msg = new JSONArray();

		String mode = map.get("MODE");	// 更新処理モード

		if(StringUtils.equals("1", mode)){
			// 新規登録
			msg = check(userInfo, map);

		} else if(StringUtils.equals("2", mode)){
			// 「確定」→「作成中」に更新
			msg = check2(userInfo, map);

		} else if(StringUtils.equals("3", mode)){
			// 「作成中」→「確定」に更新
			msg = check3(userInfo, map);
		}

		if(msg.size() > 0){
			rtn.put(MsgKey.E.getKey(), msg);
			return rtn;
		}

		// 更新処理
		try {
			if(StringUtils.equals("1", mode)){
				// 新規登録
				rtn = updateData(userInfo, map);

			} else if(StringUtils.equals("2", mode)){
				// 「確定」→「作成中」に更新
				rtn = updateData2(userInfo, map);

			} else if(StringUtils.equals("3", mode)){
				// 「作成中」→「確定」に更新
				rtn = updateData3(userInfo, map);
			}

		} catch (Exception e) {
			e.printStackTrace();
			msg.add(MessageUtility.getMessage(Msg.E00001.getVal()));
			rtn.put(MsgKey.E.getKey(), msg);
		}

		return rtn;
	}

	/**
	 * チェック処理 （新規登録）
	 * @param userInfo
	 * @param map
	 * @return
	 */
	@SuppressWarnings("static-access")
	public JSONArray check(User userInfo, HashMap<String, String> map) {
		JSONArray msg = new JSONArray();

		// パラメータ確認
		String szTorihiki	= map.get("TORIHIKI");	// 取引先
		String szTeian		= map.get("TEIAN");		// 提案件名

		// 提案件名の重複チェック
		ItemList iL = new ItemList();
		String sqlCommand = "select COUNT(TIKKNNO) as CNT from INAWS.PIMTIK where TIKTTCD = ? and TIKKNNM = ?";
		ArrayList<String> prmData = new ArrayList<String>();
		prmData.add(szTorihiki);
		prmData.add(szTeian);
		JSONArray result = iL.selectJSONArray(sqlCommand, prmData, Defines.STR_JNDI_DS);

		if (result.size() > 0
			&& ! result.getJSONObject(0).isEmpty()
			&& result.getJSONObject(0).optLong("CNT") > 0
		){
			msg.add(MessageUtility.getMsg("この" + DefineReport.InpText.TEIAN.getTxt() + "は既に登録されています。"));
		}

		return msg;
	}

	/**
	 * 更新処理実行 （新規登録）
	 * @param userInfo
	 * @param map
	 * @return
	 * @throws Exception
	 */
	private JSONObject updateData(User userInfo, HashMap<String, String> map) throws Exception {
		JSONObject rtn = new JSONObject();

		// パラメータ確認
		String szTorihiki	= map.get("TORIHIKI");	// 取引先
		String szTeian		= map.get("TEIAN");		// 提案件名

		String userID = userInfo.getId();
		CmnDate dateInfo = new CmnDate();
		String sysDate = dateInfo.getToday();				// 本日日付
		String sysTime = dateInfo.getNowTime();				// 現在時刻

		ArrayList<String> prmData = new ArrayList<String>();
		int upCount = 0;

		// コネクション
		Connection con = null;

		try {
			// コネクションの取得
			con = DBConnection.getConnection(this.JNDIname);
			con.setAutoCommit(false);

			String sqlCommand = "insert into INAWS.PIMTIK (TIKTTCD, TIKKNNM, TIKSTCD, T1SKID, T1SKDT, T1SKTM, T1UPID, T1UPDT, T1UPTM) values(?, ?, ?, ?, ?, ?, ?, ?, ?)";
			prmData.add(szTorihiki);
			prmData.add(szTeian);
			prmData.add("1");
			prmData.add(userID);
			prmData.add(sysDate);
			prmData.add(sysTime);
			prmData.add(userID);
			prmData.add(sysDate);
			prmData.add(sysTime);

			// SQL実行
			upCount += updateBySQL(sqlCommand, prmData, con);

			// 更新0件 --> 登録失敗
			if(upCount < 1){
				throw new NullPointerException();
			}

			con.commit();
			con.close();

			rtn.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00001.getVal()));

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
			throw e;
		}

		return rtn;
	}


	/**
	 * チェック処理 （「確定」→「作成中」）
	 * @param userInfo
	 * @param map
	 * @return
	 */
	public JSONArray check2(User userInfo, HashMap<String, String> map) {
		JSONArray msg = new JSONArray();

		return msg;
	}

	/**
	 * 更新処理実行 （「確定」→「作成中」）
	 * @param userInfo
	 * @param map
	 * @return
	 * @throws Exception
	 */
	private JSONObject updateData2(User userInfo, HashMap<String, String> map) throws Exception {
		JSONObject rtn = new JSONObject();

		// パラメータ確認
		JSONArray dataArray	= JSONArray.fromObject(map.get("DATA"));	// 更新情報

		String userID = userInfo.getId();
		CmnDate dateInfo = new CmnDate();
		String sysDate = dateInfo.getToday();				// 本日日付
		String sysTime = dateInfo.getNowTime();				// 現在時刻

		ArrayList<String> prmData = new ArrayList<String>();
		int upCount = 0;

		// コネクション
		Connection con = null;

		try {
			// コネクションの取得
			con = DBConnection.getConnection(this.JNDIname);
			con.setAutoCommit(false);
			String sqlCommand = "update INAWS.PIMTIK set TIKSTCD = 1, TIKKTDT = null, T1UPID = ?, T1UPDT = ?, T1UPTM = ? where TIKKNNO = ? and TIKSTCD = 2";

			for (int i = 0; i < dataArray.size(); i++) {
				String code = "";
				if(!dataArray.getJSONObject(i).isEmpty()){
					code = dataArray.getJSONObject(i).optString("F3");
				}
				if(code.isEmpty()){
					continue;
				}

				prmData = new ArrayList<String>();
				prmData.add(userID);
				prmData.add(sysDate);
				prmData.add(sysTime);
				prmData.add(code);

				// SQL実行
				upCount += updateBySQL(sqlCommand, prmData, con);
			}

			con.commit();
			con.close();

			if (dataArray.size() == upCount) {
				rtn.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00007.getVal(), "対象データ"));
			} else {
				rtn.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00006.getVal(), String.valueOf(upCount), String.valueOf(dataArray.size() - upCount)));
			}

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
			throw e;
		}

		return rtn;
	}


	/**
	 * チェック処理 （「作成中」→「確定」）
	 * @param userInfo
	 * @param map
	 * @return
	 */
	public JSONArray check3(User userInfo, HashMap<String, String> map) {
		JSONArray msg = new JSONArray();

		return msg;
	}

	/**
	 * 更新処理実行 （「作成中」→「確定」）
	 * @param userInfo
	 * @param map
	 * @return
	 * @throws Exception
	 */
	private JSONObject updateData3(User userInfo, HashMap<String, String> map) throws Exception {
		JSONObject rtn = new JSONObject();

		// パラメータ確認
		JSONArray dataArray	= JSONArray.fromObject(map.get("DATA"));	// 更新情報

		String userID = userInfo.getId();
		CmnDate dateInfo = new CmnDate();
		String sysDate = dateInfo.getToday();				// 本日日付
		String sysTime = dateInfo.getNowTime();				// 現在時刻

		ArrayList<String> prmData = new ArrayList<String>();
		int upCount = 0;

		// コネクション
		Connection con = null;

		try {
			// コネクションの取得
			con = DBConnection.getConnection(this.JNDIname);
			con.setAutoCommit(false);

			String sqlCommand =
				"update INAWS.PIMTIK set TIKSTCD = 2, TIKKTDT = current_timestamp, T1UPID = ?, T1UPDT = ?, T1UPTM = ? where TIKKNNO = ? and TIKSTCD = 1";
//				" and (select COUNT(TITSTCD) from INAWS.PIMTIT where TITKNNO = ? and TITSTCD <> 2) = 0";

			for (int i = 0; i < dataArray.size(); i++) {
				String code = "";
				if(!dataArray.getJSONObject(i).isEmpty()){
					code = dataArray.getJSONObject(i).optString("F3");
				}
				if(code.isEmpty()){
					continue;
				}

				prmData = new ArrayList<String>();
				prmData.add(userID);
				prmData.add(sysDate);
				prmData.add(sysTime);
				prmData.add(code);
//				prmData.add(code);

				// 関連する提案商品の状態変更 「作成中」→「確定」に更新
				updateData4(userInfo, dataArray.getJSONObject(i));

				// SQL実行
				upCount += updateBySQL(sqlCommand, prmData, con);
			}

			con.commit();
			con.close();

			if (dataArray.size() == upCount) {
				rtn.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00007.getVal(), "対象データ"));
			} else {
				rtn.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00006.getVal(), String.valueOf(upCount), String.valueOf(dataArray.size() - upCount)));
			}

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
			throw e;
		}

		return rtn;
	}

	/**
	 * 更新処理実行 （提案商品の状態変更 「作成中」→「確定」）
	 * @param userInfo
	 * @param map
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unused")
	private JSONObject updateData4(User userInfo, JSONObject map) throws Exception {
		JSONObject rtn = new JSONObject();

		// パラメータ確認
		String szTeian		= map.optString("F3");		// 件名No

		String userID = userInfo.getId();
		CmnDate dateInfo = new CmnDate();
		String sysDate = dateInfo.getToday();				// 本日日付
		String sysTime = dateInfo.getNowTime();				// 現在時刻

		ArrayList<String> prmData = new ArrayList<String>();
		int upCount = 0;

		// コネクション
		Connection con = null;

		try {
			// コネクションの取得
			con = DBConnection.getConnection(this.JNDIname);
			con.setAutoCommit(false);

			String sqlCommand = "update INAWS.PIMTIT set TITSTCD = 2 where TITKNNO = ? and TITSTCD = 1";

			prmData = new ArrayList<String>();
			prmData.add(szTeian);

			// SQL実行
			upCount += updateBySQL(sqlCommand, prmData, con);

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
			throw e;
		}

		return rtn;
	}


	/**
	 * 削除処理
	 * @param request
	 * @param session
	 * @param map
	 * @param userInfo
	 * @return
	 */
	public JSONObject delete(HttpServletRequest request, HttpSession session, HashMap<String, String> map, User userInfo) {
		// メッセージ格納用
		JSONObject rtn = new JSONObject();
		JSONArray msg = new JSONArray();

		// パラメータ確認
		JSONArray dataArray	= JSONArray.fromObject(map.get("DATA"));	// 更新情報

		ArrayList<String> prmData = new ArrayList<String>();
		int upCount = 0;

		// コネクション
		Connection con = null;

		try {
			// コネクションの取得
			con = DBConnection.getConnection(this.JNDIname);
			con.setAutoCommit(false);

			String sqlCommand1 = "delete from INAWS.PIMTIT where TITKNNO = ? and (select TIKSTCD from INAWS.PIMTIK where TIKKNNO = ?) in (1, 2)";
			String sqlCommand2 = "delete from INAWS.PIMTIK where TIKKNNO = ? and TIKSTCD in (1, 2)";

			for (int i = 0; i < dataArray.size(); i++) {
				String code = "";
				if(!dataArray.getJSONObject(i).isEmpty()){
					code = dataArray.getJSONObject(i).optString("F3");
				}
				if(code.isEmpty()){
					continue;
				}

				prmData = new ArrayList<String>();
				prmData.add(code);
				prmData.add(code);

				// SQL実行（カウントしない）
				updateBySQL(sqlCommand1, prmData, con);

				prmData = new ArrayList<String>();
				prmData.add(code);

				// SQL実行
				upCount += updateBySQL(sqlCommand2, prmData, con);
			}

			con.commit();
			con.close();

			rtn.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00004.getVal(), String.valueOf(upCount)));

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
			msg.add(MessageUtility.getMessage(Msg.E00002.getVal()));
			rtn.put(MsgKey.E.getKey(), msg);
		}

		return rtn;
	}

}

