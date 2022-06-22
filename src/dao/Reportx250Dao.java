/**
 *
 */
package dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;

import authentication.bean.User;
import authentication.defines.Consts;
import common.CmnDate;
import common.DefineReport;
import common.Defines;
import common.ItemList;
import common.JsonArrayData;
import common.MessageUtility;
import common.MessageUtility.Msg;
import common.MessageUtility.MsgKey;
import common.NumberingUtility;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 */
public class Reportx250Dao extends ItemDao {

	/**
	 * インスタンスを生成します。
	 * @param source
	 */
	public Reportx250Dao(String JNDIname) {
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

		String btnId = getMap().get("BTN");			// 実行ボタン

		// パラメータ確認
		// 必須チェック
		if ((btnId == null)) {
			System.out.println(super.getConditionLog());
			return "";
		}

		// タイトル情報(任意)設定
		List<String> titleList = new ArrayList<String>();

		// DB検索用パラメータ
		ArrayList<String> paramData = new ArrayList<String>();

		String szTommorow	= (new CmnDate()).getTomorrow();	// 明日日付
		String szBumon		= getMap().get("BUMON");			// 部門

		if(szBumon.equals("-1")) {
			szBumon = "";
		}

		String szShohin		= getMap().get("SHOHIN");			// 商品名
		String szHatyu		= getMap().get("HATYU");			// 発注先
		String szFromDate		= getMap().get("FROM_DATE");			// 商品登録日FROM
		String szToDate		= getMap().get("TO_DATE");			// 商品登録日TO
		String szLimit		= getMap().get("LIMIT");			// 検索上限数

		// 検索文（並び替え＆取得範囲制限）
		StringBuffer sbSQL = new StringBuffer();

		sbSQL.append(" select");
		sbSQL.append(" case when T1.SITSTCD = '1' then '作成中'");												// F1	：状態
		sbSQL.append(" when T1.SITSTCD = '2' then '確定'");
		sbSQL.append(" when T1.SITSTCD = '4' then '完了'");
		sbSQL.append(" when T1.SITSTCD = '9' then '却下'");
		sbSQL.append(" else '　' end");
		sbSQL.append(" ,T1.SITKNNO");																	// F2	 : 件名No
		sbSQL.append(" ,substr(right('00000000' || rtrim(char(T1.SHNCD)), 8), 1, 4) || '-' || substr(right('00000000' || rtrim(char(t1.shncd)), 8), 5) as SHNCD");	// F3	：商品コード
		sbSQL.append(" ,T3.SRCCD as SRCCD");																	// F4	：ソースコード1
		sbSQL.append(" ,T1.SHNKN");																	// F5	：商品名
		sbSQL.append(" ,T1.RG_ATSUKFLG");																	// F6	：扱区分
		sbSQL.append(" ,T1.RG_GENKAAM");																	// F7	：原価
		sbSQL.append(" ,T1.RG_BAIKAAM");																	// F8	：本体売価
		sbSQL.append(" ,"+DefineReport.ID_SQL_MD03111301_COL_RG);					// F9	：総額売価
		sbSQL.append(" ,T1.RG_IRISU");																	// F10	：店入数
		sbSQL.append(" ,case when trim(T1.PARENTCD) = '00000000' then '' else trim(left(T1.PARENTCD, 4) || '-' || SUBSTR(T1.PARENTCD, 5)) end as PARENTCD");	// F11	: 親の商品コード
		sbSQL.append(" ,T1.RG_WAPNFLG");																	// F12	：ワッペン区分
		sbSQL.append(" ,T1.RG_IDENFLG");																	// F13	：一括区分
		sbSQL.append(" ,T1.SSIRCD");																	// F14	：標準仕入先
		sbSQL.append(" ,right('0'||nvl(T1.BMNCD,0),2)||'-'||right('0'||nvl(T1.DAICD,0),2)||'-'||right('0'||nvl(T1.CHUCD,0),2)||'-'||right('0'||nvl(T1.SHOCD,0),2) as BUNCD");		// F15	：分類コード
		sbSQL.append(" ,nvl(TO_CHAR(T1.UPDDT, 'YY/MM/DD'),'__/__/__')");																	// F16	：更新日
		sbSQL.append(" ,T5.NMKN");																	// F17	：衣料使い回し
		sbSQL.append(" ,T1.URICD");																	// F18	：販売コード

		sbSQL.append(" from INAWS.PIMSIT T1");
		sbSQL.append(" left outer join INAMS.MSTMEISHO T5 on T5.MEISHOKBN = '142' and trim(T5.MEISHOCD) = IRYOREFLG");
		sbSQL.append(" left outer join INAMS.MSTSRCCD T3 on T1.SHNCD = T3.SHNCD and T3.SEQNO = 1");
		sbSQL.append(" "+DefineReport.ID_SQL_MD03111301_JOIN+"");

		sbSQL.append(" left join INAWS.PIMSISIRGPSHN SIH1 on SIH1.SHNCD = T1.SHNCD");
		sbSQL.append(" left join INAMS.MSTSIR SS1 on SS1.SIRCD = SIH1.SIRCD");
		sbSQL.append(" left join INAMS.MSTCHUBRUI CHU on CHU.BMNCD = T1.BMNCD and CHU.DAICD = T1.DAICD and CHU.CHUCD = T1.CHUCD");
		sbSQL.append(" left join INAMS.MSTDAIBRUI DAI on DAI.BMNCD = CHU.BMNCD and DAI.DAICD = CHU.DAICD");
		sbSQL.append(" left join INAMS.MSTBMN BMN on BMN.BMNCD = CHU.BMNCD");
		sbSQL.append(" where nvl(T1.UPDKBN, 0) <> 1");

		if(StringUtils.isNotEmpty(szBumon)){		// 条件：部門
			if( szBumon.substring(0,1).equals("0")) {
				szBumon = szBumon.substring(1);
			}
			sbSQL.append(" and T1.BMNCD like '");
			sbSQL.append(szBumon);

			sbSQL.append("%'");
		}
		if(StringUtils.isNotEmpty(szShohin)){		// 条件：商品名
			sbSQL.append(" and T1.SHNKN like '%");
			sbSQL.append(szShohin);
			sbSQL.append("%'");
		}
		if(StringUtils.isNotEmpty(szHatyu)){		// 条件：発注先1
			sbSQL.append(" and T1.SSIRCD = '"+szHatyu+"'");
		}
		// 商品登録日
		if(szFromDate.length() == 8) {
			szFromDate = szFromDate.substring(0, 4)+"-"+szFromDate.substring(4,6)+"-"+szFromDate.substring(6);
		}
		if(szToDate.length() == 8) {
			szToDate = szToDate.substring(0, 4)+"-"+szToDate.substring(4,6)+"-"+szToDate.substring(6);
		}
		if(StringUtils.isNotEmpty(szFromDate) && StringUtils.isNotEmpty(szToDate)){
			// FROM,TO 指定
			sbSQL.append(" and T1.ADDDT between '"+szFromDate+"' and '"+szToDate+"'");
		} else if (StringUtils.isNotEmpty(szFromDate)) {
			// FROM 指定
			sbSQL.append(" and T1.ADDDT >= '"+szFromDate+"'");
		} else if (StringUtils.isNotEmpty(szToDate)) {
			// TO 指定
			sbSQL.append(" and T1.ADDDT <= '"+szToDate+"'");
		}
		sbSQL.append(" order by T1.SHNCD fetch first "+szLimit+" rows only");

		// オプション情報（タイトル）設定
		JSONObject option = new JSONObject();
		option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
		setOption(option);

		if (DefineReport.ID_DEBUG_MODE)	System.out.println(getClass().getSimpleName()+"[sql]"+sbSQL.toString());
		return sbSQL.toString();
	}

	private void outputQueryList() {

		// 検索条件の加工クラス作成
		JsonArrayData jad = new JsonArrayData();
		jad.setJsonString(getJson());

		// 保存用 List (検索情報)作成
		setWhere(new ArrayList<List<String>>());
		List<String> cells = new ArrayList<String>();

		// 部門
		cells = new ArrayList<String>();
		cells.add( DefineReport.Select.BUMON.getTxt() );
		cells.add( jad.getJSONText(DefineReport.Select.BUMON.getObj()) );
		getWhere().add(cells);

		// 商品名
		cells = new ArrayList<String>();
		cells.add( DefineReport.Text.SHOHIN.getTxt() );
		cells.add( jad.getJSONValue(DefineReport.Text.SHOHIN.getObj()) );
		// 発注先コード
		cells.add( DefineReport.Text.HATYU.getTxt()+"コード" );
		cells.add( jad.getJSONText(DefineReport.Text.HATYU.getObj()) );
		getWhere().add(cells);

		// 商品登録日
		cells = new ArrayList<String>();
		cells.add( "商品登録日" );
		cells.add( jad.getJSONText(DefineReport.InpText.YMD_F.getObj()) );
		cells.add( "～" );
		cells.add( jad.getJSONText(DefineReport.InpText.YMD_T.getObj()) );
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
		String sysdate = (String)request.getSession().getAttribute(Consts.STR_SES_LOGINDT);
		// メッセージ格納用
		JSONObject rtn = new JSONObject();
		JSONArray msg = new JSONArray();

		String mode = map.get("MODE");	// 更新処理モード

		if(StringUtils.equals("2", mode)){
			// 「作成中」→「確定」に更新
			msg = this.check3(map, userInfo, sysdate);
		} else if(StringUtils.equals("3", mode)){
			// 「確定」→「完了」に更新、基本商品マスタに登録
			msg = this.check(map, userInfo, sysdate);

		} else if(StringUtils.equals("9", mode)){
			// 「確定」→「却下」に更新
			msg = this.check2(map, userInfo, sysdate);
		}

		if(msg.size() > 0){
			rtn.put(MsgKey.E.getKey(), msg);
			return rtn;
		}

		// 更新処理
		try {
			if(StringUtils.equals("2", mode)){
				// 「作成中」→「確定」に更新
				rtn = this.updateData3(map, userInfo, sysdate);
			} else if(StringUtils.equals("3", mode)){
				// 「確定」→「完了」に更新、基本商品マスタに登録
				rtn = this.updateData(map, userInfo, sysdate);
			} else if(StringUtils.equals("9", mode)){
				// 「確定」→「却下」に更新
				rtn = this.updateData2(map, userInfo, sysdate);
			}

		} catch (Exception e) {
			e.printStackTrace();
			msg.add(MessageUtility.getMessage(Msg.E00001.getVal()));
			rtn.put(MsgKey.E.getKey(), msg);
		}

		return rtn;
	}

	/**
	 * チェック処理 （「確定」→「完了」）
	 * @param map
	 * @param userInfo
	 * @param sysdate
	 * @return
	 */
	@SuppressWarnings("static-access")
	public JSONArray check(HashMap<String, String> map, User userInfo, String sysdate) {
		JSONArray msg = new JSONArray();

		// パラメータ確認
		JSONArray dataArray	= JSONArray.fromObject(map.get("DATA"));	// 更新情報

		return msg;
	}

	/**
	 * 更新処理実行 （「確定」→「完了」）
	 * @param map
	 * @param userInfo
	 * @return
	 * @throws Exception
	 */
	private JSONObject updateData(HashMap<String, String> map, User userInfo, String sysdate) throws Exception {
		JSONObject rtn = new JSONObject();

		// パラメータ確認
		JSONArray dataArray	= JSONArray.fromObject(map.get("DATA"));	// 更新情報

		String userID = userInfo.getId();
		CmnDate dateInfo = new CmnDate();
		String sysDate = dateInfo.getToday();				// 本日日付
		String sysTime = dateInfo.getNowTime();				// 現在時刻
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss.SSS");
		String strDateInfo = dateFormat.format(dateInfo);

		ArrayList<String> prmData = new ArrayList<String>();
		int upCount = 0;

		// コネクション
		Connection con = null;

		try {
			// コネクションの取得
			con = DBConnection.getConnection(this.JNDIname);
			con.setAutoCommit(false);

			// 仕掛商品（INAWS.PIMSIT）の商品ステータス（SITSTCD）を完了（4）に更新
			String sqlUpdateCommand_1 =
					"update INAWS.PIMSIT set SITSTCD = 4, OPERATOR = ?, UPDDT = ? where SHNCD = ? and SITKNNO = ? and SITSTCD = 2";

			// 仕掛商品（INAWS.PIMSIT）の商品コードを更新
			String sqlUpdateCommand_2 =
					"update INAWS.PIMSIT set SHNCD = ?, URICD = ?, OPERATOR = ?, UPDDT = ? where SHNCD = ? and SITKNNO = ? and SITSTCD = 4";

			// 仕掛仕入グループ商品マスタ（INAWS.PIMSISIRGPSHN）の商品コードを更新
			String sqlUpdateCommand_2_2 =
					//"update INAWS.PIMSISIRGPSHN set SHNCD = ?, OPERATOR = ?, UPDDT = ? where SHNCD = (select SHNCD from INAWS.PIMSIT where SITKNNO = ? and SHNCD = ? and SITSTCD = 4)";
					"update INAWS.PIMSISIRGPSHN set SHNCD = ?, OPERATOR = ?, UPDDT = ? where SHNCD = ?";

			// 仕掛売価コントロールマスタ（INAWS.PIMSIBAIKACTL）の商品コードを更新
			String sqlUpdateCommand_2_3 =
					"update INAWS.PIMSIBAIKACTL set SHNCD = ?, OPERATOR = ?, UPDDT = ? where SHNCD = ?";

			// 仕掛ソースコード管理マスタ（INAWS.PIMSISRCCD）の商品コードを更新
			String sqlUpdateCommand_2_4 =
					"update INAWS.PIMSISRCCD set SHNCD = ?, OPERATOR = ?, UPDDT = ? where SHNCD = ?";

			// 仕掛添加物マスタ（INAWS.PIMSITENKABUTSU）の商品コードを更新
			String sqlUpdateCommand_2_5 =
					"update INAWS.PIMSITENKABUTSU set SHNCD = ?, OPERATOR = ?, UPDDT = ? where SHNCD = ?";

			// 仕掛品揃グループマスタ（INAWS.PIMSISHINAGP）の商品コードを更新
			String sqlUpdateCommand_2_6 =
					"update INAWS.PIMSISHINAGP set SHNCD = ?, OPERATOR = ?, UPDDT = ? where SHNCD = ?";

			// 仕掛メーカーマスタは商品コードがないので、更新不要

			// 提案商品（INAWS.PIMTIT）の商品ステータス（SITSTCD）を完了（4）に更新
			String sqlUpdateCommand_3 =
					//"update INAWS.PIMTIT set TITSTCD = 4, OPERATOR = ?, UPDDT = ? where TITKNNO = (select SITKNNO from INAWS.PIMSIT where SHNCD = ? and SITSTCD = 4) and SHNKN = ? and BMNCD = ? and DAICD = ? and CHUCD = ? and TITSTCD = 3";
					"update INAWS.PIMTIT set TITSTCD = 4, OPERATOR = ?, UPDDT = ? where TITKNNO = (select SITKNNO from INAWS.PIMSIT where SHNCD = ? and SITSTCD = 4) and SHNCD = ? and TITSTCD = 3";

			// 提案件名（INAWS.PIMTIK）の件名ステータス（TIKSTCD）を完了（4）に更新
			String sqlUpdateCommand_4 =
					"update INAWS.PIMTIK set TIKSTCD = 4, TIKEDDT = current_timestamp, T1UPID = ?, T1UPDT = ?, T1UPTM = ? where TIKKNNO = (select SITKNNO from INAWS.PIMSIT where SHNCD = ? and SITSTCD > 3) and TIKSTCD = 3 and (select COUNT(TITKNNO) from INAWS.PIMTIT where TITKNNO = (select SITKNNO from INAWS.PIMSIT where SHNCD = ? and SITSTCD > 3) and TITSTCD > 3) = (select COUNT(TITKNNO) from INAWS.PIMTIT where TITKNNO = (select SITKNNO from INAWS.PIMSIT where SHNCD = ? and SITSTCD > 3))";

			// 商品ユニークコード、	商品コード取得
			String shohinSeq = "";
			String shohinCode = "";

			for (int i = 0; i < dataArray.size(); i++) {
				String code = "";
				String kenno = "";
				String szBunrui = "";
				String szBmncd = "";
				if(!dataArray.getJSONObject(i).isEmpty()){
					code = dataArray.getJSONObject(i).optString("F3");
					code = code.replace("-", "");
					kenno = dataArray.getJSONObject(i).optString("F2");
					szBunrui = dataArray.getJSONObject(i).optString("F15");
					String[] szBunrui_s = szBunrui.split("-", 0);
					szBmncd = szBunrui_s[0].replaceFirst("^0+", "");
				}
				if(code.isEmpty()){
					continue;
				}
				if(kenno.isEmpty()) {
					continue;
				}

				// 仕掛商品の状態更新
				prmData = new ArrayList<String>();
				prmData.add(userID);
				prmData.add(strDateInfo);
				prmData.add(code);
				prmData.add(kenno);
				// SQL実行
				if(updateBySQL(sqlUpdateCommand_1, prmData, con) == 0) {
					continue;
				}

				String kbn143 = "0";
				String txt_shncd_new = "";
				String txt_uricd_new = "";

				if (!StringUtils.isEmpty(kenno) && kenno.equals("0")) {
					txt_shncd_new = code;
					txt_uricd_new = dataArray.getJSONObject(i).optString("F18");
				} else {
					JSONObject result = NumberingUtility.execGetNewSHNCD(userInfo, txt_shncd_new, kbn143, szBmncd);
					txt_shncd_new = result.optString("VALUE");
					// 添付資料（MD03100902）の販売コード付番機能
					JSONObject resUri = NumberingUtility.execHoldNewURICD(userInfo, txt_shncd_new);
					txt_uricd_new = resUri.optString("VALUE");
				}


				// 販売コード付番管理テーブル更新
				prmData = new ArrayList<String>();
				String  sqlUpdateCommand_uri =
						"merge into INAAD.SYSURICD_FU as T" +
						" using (" +
						"  select min(ID) as ID from INAAD.SYSURICD_FU" +
						" ) as RE on (T.ID = RE.ID) " +
						" when matched then " +
						" update set" +
						"  SUMINO = case when SUMINO+1 <=ENDNO then SUMINO+1 else SUMINO end" +			// F4 : 付番済番号
						" ,UPDDT= current timestamp";							// F5 : 更新日

				updateBySQL(sqlUpdateCommand_uri, prmData, con);

				// 商品更新件数+1
				prmData = new ArrayList<String>();
				String  sqlUpdateCommand_SYSSHN =
						"merge into INAAD.SYSSHNCOUNT as T" +
						" using (select" +
						"  cast(to_char(current date, 'YYYYMMDD') as integer) as UPDATEDT" +		// F1 : 日付
						" ,cast(1 as SMALLINT) as UPDATECNT" +													// F2 : 件数 insert用
						" ,current timestamp as UPDDT" +																// F3 : 更新日
						" from SYSIBM.SYSDUMMY1" +
						" ) as RE on ( T.UPDATEDT = RE.UPDATEDT ) " +
						" when not matched then " +
						" insert" +
						" values(" +
						"  RE.UPDATEDT" +			// F1 : 日付
						" ,RE.UPDATECNT" +			// F2 : 件数
						" ,RE.UPDDT" +					// F3 : 更新日
						" )";

				updateBySQL(sqlUpdateCommand_SYSSHN, prmData, con);

				shohinCode = txt_shncd_new;

				// 仕掛商品マスタの商品コード更新
				prmData = new ArrayList<String>();
				prmData.add(shohinCode);
				prmData.add(txt_uricd_new);
				prmData.add(userID);
				prmData.add(strDateInfo);
				prmData.add(code);
				prmData.add(kenno);
				// SQL実行
				upCount += updateBySQL(sqlUpdateCommand_2, prmData, con);

				// 仕掛仕入グループ商品マスタの商品コード更新
				prmData = new ArrayList<String>();
				prmData.add(shohinCode);
				prmData.add(userID);
				prmData.add(strDateInfo);
				prmData.add(code);
				// SQL実行
				updateBySQL(sqlUpdateCommand_2_2, prmData, con);

				// 仕掛売価コントロールマスタの商品コード更新
				prmData = new ArrayList<String>();
				prmData.add(shohinCode);
				prmData.add(userID);
				prmData.add(strDateInfo);
				prmData.add(code);
				// SQL実行
				updateBySQL(sqlUpdateCommand_2_3, prmData, con);

				// 仕掛ソースコード管理マスタの商品コード更新
				prmData = new ArrayList<String>();
				prmData.add(shohinCode);
				prmData.add(userID);
				prmData.add(strDateInfo);
				prmData.add(code);
				// SQL実行
				updateBySQL(sqlUpdateCommand_2_4, prmData, con);

				// 仕掛添加物マスタの商品コード更新
				prmData = new ArrayList<String>();
				prmData.add(shohinCode);
				prmData.add(userID);
				prmData.add(strDateInfo);
				prmData.add(code);
				// SQL実行
				updateBySQL(sqlUpdateCommand_2_5, prmData, con);

				// 仕掛品揃グループマスタの商品コード更新
				prmData = new ArrayList<String>();
				prmData.add(shohinCode);
				prmData.add(userID);
				prmData.add(strDateInfo);
				prmData.add(code);
				// SQL実行
				updateBySQL(sqlUpdateCommand_2_6, prmData, con);

				// 商品マスタへ登録
				if(!this.updateDB(map, userInfo, sysdate, con, code, shohinSeq, shohinCode, userID, strDateInfo, sysDate, sysTime, kenno)){
					continue;
				}

				// ジャーナルへ登録
				this.updateDBJNL(map, userInfo, sysdate, con, code, shohinSeq, shohinCode, userID, strDateInfo, sysDate, sysTime, kenno);

				// 提案商品の状態更新
				prmData = new ArrayList<String>();
				prmData.add(userID);
				prmData.add(strDateInfo);
				prmData.add(shohinCode);
				prmData.add(code);
				// SQL実行（カウントしない）
				updateBySQL(sqlUpdateCommand_3, prmData, con);

				// 提案件名の状態更新
				prmData = new ArrayList<String>();
				prmData.add(userID);
				prmData.add(sysDate);
				prmData.add(sysTime);
				prmData.add(shohinCode);
				prmData.add(shohinCode);
				prmData.add(shohinCode);
				// SQL実行（カウントしない）
				updateBySQL(sqlUpdateCommand_4, prmData, con);

				shohinSeq = "";
				shohinCode = "";
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
	 * 基本商品マスタ、仕入グループ商品マスタなど子テーブルへ登録
	 * @param map
	 * @param userInfo
	 * @param sysdate
	 * @param con
	 * @param code
	 * @param szYmd
	 * @param shohinSeq
	 * @param shohinCode
	 * @param userID
	 * @param sysDate
	 * @param sysTime
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("static-access")
	private boolean updateDB(HashMap<String, String> map, User userInfo, String sysdate, Connection con, String code, String shohinSeq, String shohinCode, String userID, String strDateInfo, String sysDate, String sysTime, String kenno) throws Exception{
		// DB更新SQL
		StringBuffer sbSQL = new StringBuffer();
		sbSQL.append("MERGE INTO INAMS.MSTSHN USING(");
		sbSQL.append(" select");
		sbSQL.append("  SHNCD");													// 商品コード：
		sbSQL.append(", YOYAKUDT, TENBAIKADT, YOT_BMNCD, YOT_DAICD, YOT_CHUCD, YOT_SHOCD, URI_BMNCD");
		sbSQL.append(", URI_DAICD, URI_CHUCD, URI_SHOCD, BMNCD, DAICD, CHUCD, SHOCD, SSHOCD, ATSUK_STDT");
		sbSQL.append(", ATSUK_EDDT, TEISHIKBN, SHNAN, SHNKN, PCARDKN, POPKN, RECEIPTAN, RECEIPTKN, PCKBN");
		sbSQL.append(", KAKOKBN, ICHIBAKBN, SHNKBN, SANCHIKN, SSIRCD, HSPTN, RG_ATSUKFLG, RG_GENKAAM");
		sbSQL.append(", RG_BAIKAAM, RG_IRISU, RG_IDENFLG, RG_WAPNFLG, HS_ATSUKFLG, HS_GENKAAM, HS_BAIKAAM");
		sbSQL.append(", HS_IRISU, HS_WAPNFLG, HS_SPOTMINSU, HP_SWAPNFLG, KIKKN, UP_YORYOSU, UP_TYORYOSU");
		sbSQL.append(", UP_TANIKBN, SHNYOKOSZ, SHNTATESZ, SHNOKUSZ, SHNJRYOSZ, PBKBN, KOMONOKBM, TANAOROKBN");
		sbSQL.append(", TEIKEIKBN, ODS_HARUSU, ODS_NATSUSU, ODS_AKISU, ODS_FUYUSU, ODS_NYUKASU, ODS_NEBIKISU");
		sbSQL.append(", PCARD_SHUKBN, PCARD_IROKBN, ZEIKBN, ZEIRTKBN, ZEIRTKBN_OLD, ZEIRTHENKODT, SEIZOGENNISU");
		sbSQL.append(", TEIKANKBN, MAKERCD, IMPORTKBN, SIWAKEKBN, HENPIN_KBN, TAISHONENSU, CALORIESU, ELPFLG");
		sbSQL.append(", BELLMARKFLG, RECYCLEFLG, ECOFLG, HZI_YOTO, HZI_ZAISHITU, HZI_RECYCLE, KIKANKBN, SHUKYUKBN");
		sbSQL.append(", DOSU, CHINRETUCD, DANTUMICD, KASANARICD, KASANARISZ, ASSHUKURT, SHUBETUCD, URICD");
		sbSQL.append(", SALESCOMKN, URABARIKBN, PCARD_OPFLG, PARENTCD, BINKBN, HAT_MONKBN, HAT_TUEKBN");
		sbSQL.append(", HAT_WEDKBN, HAT_THUKBN, HAT_FRIKBN, HAT_SATKBN, HAT_SUNKBN, READTMPTN, SIMEKAISU");
		sbSQL.append(", IRYOREFLG, TOROKUMOTO, UPDKBN, SENDFLG");
		sbSQL.append(", '"+userID+"' as OPERATOR");						// オペレータ
		sbSQL.append(", '"+strDateInfo+"' as ADDDT");						// 登録日
		sbSQL.append(", '"+strDateInfo+"' as UPDDT");						// 更新日
		sbSQL.append(", K_HONKB, K_WAPNFLG_R, K_WAPNFLG_H, K_TORIKB, ITFCD, CENTER_IRISU");
		sbSQL.append(" from INAWS.PIMSIT RE");
		sbSQL.append(" where SHNCD = '"+shohinCode+"' and SITSTCD = 4) ON (SHNCD=RE.SHNCD)");
		sbSQL.append(" WHEN NOT MATCHED THEN INSERT(");
		sbSQL.append("  SHNCD");		// F1 : 商品コード
		sbSQL.append(" ,YOYAKUDT");		// F2 : マスタ変更予定日
		sbSQL.append(" ,TENBAIKADT");	// F3 : 店売価実施日
		sbSQL.append(" ,YOT_BMNCD");	// F4 : 用途分類コード_部門
		sbSQL.append(" ,YOT_DAICD");	// F5 : 用途分類コード_大
		sbSQL.append(" ,YOT_CHUCD");	// F6 : 用途分類コード_中
		sbSQL.append(" ,YOT_SHOCD");	// F7 : 用途分類コード_小
		sbSQL.append(" ,URI_BMNCD");	// F8 : 売場分類コード_部門
		sbSQL.append(" ,URI_DAICD");	// F9 : 売場分類コード_大
		sbSQL.append(" ,URI_CHUCD");	// F10: 売場分類コード_中
		sbSQL.append(" ,URI_SHOCD");	// F11: 売場分類コード_小
		sbSQL.append(" ,BMNCD");		// F12: 標準分類コード_部門
		sbSQL.append(" ,DAICD");		// F13: 標準分類コード_大
		sbSQL.append(" ,CHUCD");		// F14: 標準分類コード_中
		sbSQL.append(" ,SHOCD");		// F15: 標準分類コード_小
		sbSQL.append(" ,SSHOCD");		// F16: 標準分類コード_小小
		sbSQL.append(" ,ATSUK_STDT");	// F17: 取扱期間_開始日
		sbSQL.append(" ,ATSUK_EDDT");	// F18: 取扱期間_終了日
		sbSQL.append(" ,TEISHIKBN");	// F19: 取扱停止
		sbSQL.append(" ,SHNAN");		// F20: 商品名（カナ）
		sbSQL.append(" ,SHNKN");		// F21: 商品名（漢字）
		sbSQL.append(" ,PCARDKN");		// F22: プライスカード商品名称（漢字）
		sbSQL.append(" ,POPKN");		// F23: POP名称
		sbSQL.append(" ,RECEIPTAN");	// F24: レシート名（カナ）
		sbSQL.append(" ,RECEIPTKN");	// F25: レシート名（漢字）
		sbSQL.append(" ,PCKBN");		// F26: PC区分
		sbSQL.append(" ,KAKOKBN");		// F27: 加工区分
		sbSQL.append(" ,ICHIBAKBN");	// F28: 市場区分
		sbSQL.append(" ,SHNKBN");		// F29: 商品種類
		sbSQL.append(" ,SANCHIKN");		// F30: 産地
		sbSQL.append(" ,SSIRCD");		// F31: 標準仕入先コード
		sbSQL.append(" ,HSPTN");		// F32: 配送パターン
		sbSQL.append(" ,RG_ATSUKFLG");	// F33: レギュラー情報_取扱フラグ
		sbSQL.append(" ,RG_GENKAAM");	// F34: レギュラー情報_原価
		sbSQL.append(" ,RG_BAIKAAM");	// F35: レギュラー情報_売価
		sbSQL.append(" ,RG_IRISU");		// F36: レギュラー情報_店入数
		sbSQL.append(" ,RG_IDENFLG");	// F37: レギュラー情報_一括伝票フラグ
		sbSQL.append(" ,RG_WAPNFLG");	// F38: レギュラー情報_ワッペン
		sbSQL.append(" ,HS_ATSUKFLG");	// F39: 販促情報_取扱フラグ
		sbSQL.append(" ,HS_GENKAAM");	// F40: 販促情報_原価
		sbSQL.append(" ,HS_BAIKAAM");	// F41: 販促情報_売価
		sbSQL.append(" ,HS_IRISU");		// F42: 販促情報_店入数
		sbSQL.append(" ,HS_WAPNFLG");	// F43: 販促情報_ワッペン
		sbSQL.append(" ,HS_SPOTMINSU");	// F44: 販促情報_スポット最低発注数
		sbSQL.append(" ,HP_SWAPNFLG");	// F45: 販促情報_特売ワッペン
		sbSQL.append(" ,KIKKN");		// F46: 規格名称
		sbSQL.append(" ,UP_YORYOSU");	// F47: ユニットプライス_容量
		sbSQL.append(" ,UP_TYORYOSU");	// F48: ユニットプライス_単位容量
		sbSQL.append(" ,UP_TANIKBN");	// F49: ユニットプライス_ユニット単位
		sbSQL.append(" ,SHNYOKOSZ");	// F50: 商品サイズ_横
		sbSQL.append(" ,SHNTATESZ");	// F51: 商品サイズ_縦
		sbSQL.append(" ,SHNOKUSZ");		// F52: 商品サイズ_奥行
		sbSQL.append(" ,SHNJRYOSZ");	// F53: 商品サイズ_重量
		sbSQL.append(" ,PBKBN");		// F54: PB区分
		sbSQL.append(" ,KOMONOKBM");	// F55: 小物区分
		sbSQL.append(" ,TANAOROKBN");	// F56: 棚卸区分
		sbSQL.append(" ,TEIKEIKBN");	// F57: 定計区分
		sbSQL.append(" ,ODS_HARUSU");	// F58: ODS_賞味期限_春
		sbSQL.append(" ,ODS_NATSUSU");	// F59: ODS_賞味期限_夏
		sbSQL.append(" ,ODS_AKISU");	// F60: ODS_賞味期限_秋
		sbSQL.append(" ,ODS_FUYUSU");	// F61: ODS_賞味期限_冬
		sbSQL.append(" ,ODS_NYUKASU");	// F62: ODS_入荷期限
		sbSQL.append(" ,ODS_NEBIKISU");	// F63: ODS_値引期限
		sbSQL.append(" ,PCARD_SHUKBN");	// F64: プライスカード_種類
		sbSQL.append(" ,PCARD_IROKBN");	// F65: プライスカード_色
		sbSQL.append(" ,ZEIKBN");		// F66: 税区分
		sbSQL.append(" ,ZEIRTKBN");		// F67: 税率区分
		sbSQL.append(" ,ZEIRTKBN_OLD");	// F68: 旧税率区分
		sbSQL.append(" ,ZEIRTHENKODT");	// F69: 税率変更日
		sbSQL.append(" ,SEIZOGENNISU");	// F70: 製造限度日数
		sbSQL.append(" ,TEIKANKBN");	// F71: 定貫不定貫区分
		sbSQL.append(" ,MAKERCD");		// F72: メーカーコード
		sbSQL.append(" ,IMPORTKBN");	// F73: 輸入区分
		sbSQL.append(" ,SIWAKEKBN");	// F74: 仕分区分
		sbSQL.append(" ,HENPIN_KBN");	// F75: 返品区分
		sbSQL.append(" ,TAISHONENSU");	// F76: 対象年齢
		sbSQL.append(" ,CALORIESU");	// F77: カロリー表示
		sbSQL.append(" ,ELPFLG");		// F78: フラグ情報_ELP
		sbSQL.append(" ,BELLMARKFLG");	// F79: フラグ情報_ベルマーク
		sbSQL.append(" ,RECYCLEFLG");	// F80: フラグ情報_リサイクル
		sbSQL.append(" ,ECOFLG");		// F81: フラグ情報_エコマーク
		sbSQL.append(" ,HZI_YOTO");		// F82: 包材用途
		sbSQL.append(" ,HZI_ZAISHITU");	// F83: 包材材質
		sbSQL.append(" ,HZI_RECYCLE");	// F84: 包材リサイクル対象
		sbSQL.append(" ,KIKANKBN");		// F85: 期間
		sbSQL.append(" ,SHUKYUKBN");	// F86: 酒級
		sbSQL.append(" ,DOSU");			// F87: 度数
		sbSQL.append(" ,CHINRETUCD");	// F88: 陳列形式コード
		sbSQL.append(" ,DANTUMICD");	// F89: 段積み形式コード
		sbSQL.append(" ,KASANARICD");	// F90: 重なりコード
		sbSQL.append(" ,KASANARISZ");	// F91: 重なりサイズ
		sbSQL.append(" ,ASSHUKURT");	// F92: 圧縮率
		sbSQL.append(" ,SHUBETUCD");	// F93: 種別コード
		sbSQL.append(" ,URICD");		// F94: 販売コード
		sbSQL.append(" ,SALESCOMKN");	// F95: 商品コピー・セールスコメント
		sbSQL.append(" ,URABARIKBN");	// F96: 裏貼
		sbSQL.append(" ,PCARD_OPFLG");	// F97: プライスカード出力有無
		sbSQL.append(" ,PARENTCD");		// F98: 親商品コード
		sbSQL.append(" ,BINKBN");		// F99: 便区分
		sbSQL.append(" ,HAT_MONKBN");	// F100: 発注曜日_月
		sbSQL.append(" ,HAT_TUEKBN");	// F101: 発注曜日_火
		sbSQL.append(" ,HAT_WEDKBN");	// F102: 発注曜日_水
		sbSQL.append(" ,HAT_THUKBN");	// F103: 発注曜日_木
		sbSQL.append(" ,HAT_FRIKBN");	// F104: 発注曜日_金
		sbSQL.append(" ,HAT_SATKBN");	// F105: 発注曜日_土
		sbSQL.append(" ,HAT_SUNKBN");	// F106: 発注曜日_日
		sbSQL.append(" ,READTMPTN");	// F107: リードタイムパターン
		sbSQL.append(" ,SIMEKAISU");	// F108: 締め回数
		sbSQL.append(" ,IRYOREFLG");	// F109: 衣料使い回しフラグ
		sbSQL.append(" ,TOROKUMOTO");	// F110: 登録元
		sbSQL.append(" ,UPDKBN");		// F111: 更新区分
		sbSQL.append(" ,OPERATOR");		// F113: オペレータ
		sbSQL.append(" ,UPDDT");		// F115: 更新日
		sbSQL.append(" ,K_HONKB");		// F116: 保温区分
		sbSQL.append(" ,K_WAPNFLG_R");	// F117: デリカワッペン区分_レギュラー
		sbSQL.append(" ,K_WAPNFLG_H");	// F118: デリカワッペン区分_販促
		sbSQL.append(" ,K_TORIKB");		// F119: 取扱区分
		sbSQL.append(" ,ITFCD");		// F120: ITFコード
		sbSQL.append(" ,CENTER_IRISU");	// F121: センター入数
		sbSQL.append(") values(");
		sbSQL.append("  RE.SHNCD");
		sbSQL.append(" ,RE.YOYAKUDT");
		sbSQL.append(" ,RE.TENBAIKADT");
		sbSQL.append(" ,RE.YOT_BMNCD");
		sbSQL.append(" ,RE.YOT_DAICD");
		sbSQL.append(" ,RE.YOT_CHUCD");
		sbSQL.append(" ,RE.YOT_SHOCD");
		sbSQL.append(" ,RE.URI_BMNCD");
		sbSQL.append(" ,RE.URI_DAICD");
		sbSQL.append(" ,RE.URI_CHUCD");
		sbSQL.append(" ,RE.URI_SHOCD");
		sbSQL.append(" ,RE.BMNCD");
		sbSQL.append(" ,RE.DAICD");
		sbSQL.append(" ,RE.CHUCD");
		sbSQL.append(" ,RE.SHOCD");
		sbSQL.append(" ,RE.SSHOCD");
		sbSQL.append(" ,RE.ATSUK_STDT");
		sbSQL.append(" ,RE.ATSUK_EDDT");
		sbSQL.append(" ,RE.TEISHIKBN");
		sbSQL.append(" ,RE.SHNAN");
		sbSQL.append(" ,RE.SHNKN");
		sbSQL.append(" ,RE.PCARDKN");
		sbSQL.append(" ,RE.POPKN");
		sbSQL.append(" ,RE.RECEIPTAN");
		sbSQL.append(" ,RE.RECEIPTKN");
		sbSQL.append(" ,RE.PCKBN");
		sbSQL.append(" ,RE.KAKOKBN");
		sbSQL.append(" ,RE.ICHIBAKBN");
		sbSQL.append(" ,RE.SHNKBN");
		sbSQL.append(" ,RE.SANCHIKN");
		sbSQL.append(" ,RE.SSIRCD");
		sbSQL.append(" ,RE.HSPTN");
		sbSQL.append(" ,RE.RG_ATSUKFLG");
		sbSQL.append(" ,RE.RG_GENKAAM");
		sbSQL.append(" ,RE.RG_BAIKAAM");
		sbSQL.append(" ,RE.RG_IRISU");
		sbSQL.append(" ,RE.RG_IDENFLG");
		sbSQL.append(" ,RE.RG_WAPNFLG");
		sbSQL.append(" ,RE.HS_ATSUKFLG");
		sbSQL.append(" ,RE.HS_GENKAAM");
		sbSQL.append(" ,RE.HS_BAIKAAM");
		sbSQL.append(" ,RE.HS_IRISU");
		sbSQL.append(" ,RE.HS_WAPNFLG");
		sbSQL.append(" ,RE.HS_SPOTMINSU");
		sbSQL.append(" ,RE.HP_SWAPNFLG");
		sbSQL.append(" ,RE.KIKKN");
		sbSQL.append(" ,RE.UP_YORYOSU");
		sbSQL.append(" ,RE.UP_TYORYOSU");
		sbSQL.append(" ,RE.UP_TANIKBN");
		sbSQL.append(" ,RE.SHNYOKOSZ");
		sbSQL.append(" ,RE.SHNTATESZ");
		sbSQL.append(" ,RE.SHNOKUSZ");
		sbSQL.append(" ,RE.SHNJRYOSZ");
		sbSQL.append(" ,RE.PBKBN");
		sbSQL.append(" ,RE.KOMONOKBM");
		sbSQL.append(" ,RE.TANAOROKBN");
		sbSQL.append(" ,RE.TEIKEIKBN");
		sbSQL.append(" ,RE.ODS_HARUSU");
		sbSQL.append(" ,RE.ODS_NATSUSU");
		sbSQL.append(" ,RE.ODS_AKISU");
		sbSQL.append(" ,RE.ODS_FUYUSU");
		sbSQL.append(" ,RE.ODS_NYUKASU");
		sbSQL.append(" ,RE.ODS_NEBIKISU");
		sbSQL.append(" ,RE.PCARD_SHUKBN");
		sbSQL.append(" ,RE.PCARD_IROKBN");
		sbSQL.append(" ,RE.ZEIKBN");
		sbSQL.append(" ,RE.ZEIRTKBN");
		sbSQL.append(" ,RE.ZEIRTKBN_OLD");
		sbSQL.append(" ,RE.ZEIRTHENKODT");
		sbSQL.append(" ,RE.SEIZOGENNISU");
		sbSQL.append(" ,RE.TEIKANKBN");
		sbSQL.append(" ,RE.MAKERCD");
		sbSQL.append(" ,RE.IMPORTKBN");
		sbSQL.append(" ,RE.SIWAKEKBN");
		sbSQL.append(" ,RE.HENPIN_KBN");
		sbSQL.append(" ,RE.TAISHONENSU");
		sbSQL.append(" ,RE.CALORIESU");
		sbSQL.append(" ,RE.ELPFLG");
		sbSQL.append(" ,RE.BELLMARKFLG");
		sbSQL.append(" ,RE.RECYCLEFLG");
		sbSQL.append(" ,RE.ECOFLG");
		sbSQL.append(" ,RE.HZI_YOTO");
		sbSQL.append(" ,RE.HZI_ZAISHITU");
		sbSQL.append(" ,RE.HZI_RECYCLE");
		sbSQL.append(" ,RE.KIKANKBN");
		sbSQL.append(" ,RE.SHUKYUKBN");
		sbSQL.append(" ,RE.DOSU");
		sbSQL.append(" ,RE.CHINRETUCD");
		sbSQL.append(" ,RE.DANTUMICD");
		sbSQL.append(" ,RE.KASANARICD");
		sbSQL.append(" ,RE.KASANARISZ");
		sbSQL.append(" ,RE.ASSHUKURT");
		sbSQL.append(" ,RE.SHUBETUCD");
		sbSQL.append(" ,RE.URICD");
		sbSQL.append(" ,RE.SALESCOMKN");
		sbSQL.append(" ,RE.URABARIKBN");
		sbSQL.append(" ,RE.PCARD_OPFLG");
		sbSQL.append(" ,RE.PARENTCD");
		sbSQL.append(" ,RE.BINKBN");
		sbSQL.append(" ,RE.HAT_MONKBN");
		sbSQL.append(" ,RE.HAT_TUEKBN");
		sbSQL.append(" ,RE.HAT_WEDKBN");
		sbSQL.append(" ,RE.HAT_THUKBN");
		sbSQL.append(" ,RE.HAT_FRIKBN");
		sbSQL.append(" ,RE.HAT_SATKBN");
		sbSQL.append(" ,RE.HAT_SUNKBN");
		sbSQL.append(" ,RE.READTMPTN");
		sbSQL.append(" ,RE.SIMEKAISU");
		sbSQL.append(" ,RE.IRYOREFLG");
		sbSQL.append(" ,RE.TOROKUMOTO");
		sbSQL.append(" ,RE.UPDKBN");
		sbSQL.append(" ,RE.OPERATOR");		// F113: オペレータ
		sbSQL.append(" ,current timestamp");			// F115: 更新日
		sbSQL.append(" ,RE.K_HONKB");		// F116: 保温区分
		sbSQL.append(" ,RE.K_WAPNFLG_R");	// F117: デリカワッペン区分_レギュラー
		sbSQL.append(" ,RE.K_WAPNFLG_H");	// F118: デリカワッペン区分_販促
		sbSQL.append(" ,RE.K_TORIKB");		// F119: 取扱区分
		sbSQL.append(" ,RE.ITFCD");			// F120: ITFコード
		sbSQL.append(" ,RE.CENTER_IRISU");	// F121: センター入数
		sbSQL.append(" )");
		sbSQL.append(" WHEN MATCHED THEN UPDATE SET ");
		sbSQL.append("  YOYAKUDT=RE.YOYAKUDT");			// マスタ変更予定日
		sbSQL.append(" ,TENBAIKADT=RE.TENBAIKADT");		// 店売価実施日
		sbSQL.append(" ,YOT_BMNCD=RE.YOT_BMNCD");		// 用途分類コード_部門
		sbSQL.append(" ,YOT_DAICD=RE.YOT_DAICD");		// 用途分類コード_大
		sbSQL.append(" ,YOT_CHUCD=RE.YOT_CHUCD");		// 用途分類コード_中
		sbSQL.append(" ,YOT_SHOCD=RE.YOT_SHOCD");		// 用途分類コード_小
		sbSQL.append(" ,URI_BMNCD=RE.URI_BMNCD");		// 売場分類コード_部門
		sbSQL.append(" ,URI_DAICD=RE.URI_DAICD");		// 売場分類コード_大
		sbSQL.append(" ,URI_CHUCD=RE.URI_CHUCD");		// 売場分類コード_中
		sbSQL.append(" ,URI_SHOCD=RE.URI_SHOCD");		// 売場分類コード_小
		sbSQL.append(" ,BMNCD=RE.BMNCD");				// 標準分類コード_部門
		sbSQL.append(" ,DAICD=RE.DAICD");				// 標準分類コード_大
		sbSQL.append(" ,CHUCD=RE.CHUCD");				// 標準分類コード_中
		sbSQL.append(" ,SHOCD=RE.SHOCD");				// 標準分類コード_小
		sbSQL.append(" ,SSHOCD=RE.SSHOCD");				// 標準分類コード_小小
		sbSQL.append(" ,ATSUK_STDT=RE.ATSUK_STDT");		// 取扱期間_開始日
		sbSQL.append(" ,ATSUK_EDDT=RE.ATSUK_EDDT");		// 取扱期間_終了日
		sbSQL.append(" ,TEISHIKBN=RE.TEISHIKBN");		// 取扱停止
		sbSQL.append(" ,SHNAN=RE.SHNAN");				// 商品名（カナ）
		sbSQL.append(" ,SHNKN=RE.SHNKN");				// 商品名（漢字）
		sbSQL.append(" ,PCARDKN=RE.PCARDKN");			// プライスカード商品名称（漢字）
		sbSQL.append(" ,POPKN=RE.POPKN");				// POP名称
		sbSQL.append(" ,RECEIPTAN=RE.RECEIPTAN");		// レシート名（カナ）
		sbSQL.append(" ,RECEIPTKN=RE.RECEIPTKN");		// レシート名（漢字）
		sbSQL.append(" ,PCKBN=RE.PCKBN");				// PC区分
		sbSQL.append(" ,KAKOKBN=RE.KAKOKBN");			// 加工区分
		sbSQL.append(" ,ICHIBAKBN=RE.ICHIBAKBN");		// 市場区分
		sbSQL.append(" ,SHNKBN=RE.SHNKBN");				// 商品種類
		sbSQL.append(" ,SANCHIKN=RE.SANCHIKN");			// 産地
		sbSQL.append(" ,SSIRCD=RE.SSIRCD");				// 標準仕入先コード
		sbSQL.append(" ,HSPTN=RE.HSPTN");				// 配送パターン
		sbSQL.append(" ,RG_ATSUKFLG=RE.RG_ATSUKFLG");	// レギュラー情報_取扱フラグ
		sbSQL.append(" ,RG_GENKAAM=RE.RG_GENKAAM");		// レギュラー情報_原価
		sbSQL.append(" ,RG_BAIKAAM=RE.RG_BAIKAAM");		// レギュラー情報_売価
		sbSQL.append(" ,RG_IRISU=RE.RG_IRISU");			// レギュラー情報_店入数
		sbSQL.append(" ,RG_IDENFLG=RE.RG_IDENFLG");		// レギュラー情報_一括伝票フラグ
		sbSQL.append(" ,RG_WAPNFLG=RE.RG_WAPNFLG");		// レギュラー情報_ワッペン
		sbSQL.append(" ,HS_ATSUKFLG=RE.HS_ATSUKFLG");	// 販促情報_取扱フラグ
		sbSQL.append(" ,HS_GENKAAM=RE.HS_GENKAAM");		// 販促情報_原価
		sbSQL.append(" ,HS_BAIKAAM=RE.HS_BAIKAAM");		// 販促情報_売価
		sbSQL.append(" ,HS_IRISU=RE.HS_IRISU");			// 販促情報_店入数
		sbSQL.append(" ,HS_WAPNFLG=RE.HS_WAPNFLG");		// 販促情報_ワッペン
		sbSQL.append(" ,HS_SPOTMINSU=RE.HS_SPOTMINSU");	// 販促情報_スポット最低発注数
		sbSQL.append(" ,HP_SWAPNFLG=RE.HP_SWAPNFLG");	// 販促情報_特売ワッペン
		sbSQL.append(" ,KIKKN=RE.KIKKN");				// 規格名称
		sbSQL.append(" ,UP_YORYOSU=RE.UP_YORYOSU");		// ユニットプライス_容量
		sbSQL.append(" ,UP_TYORYOSU=RE.UP_TYORYOSU");	// ユニットプライス_単位容量
		sbSQL.append(" ,UP_TANIKBN=RE.UP_TANIKBN");		// ユニットプライス_ユニット単位
		sbSQL.append(" ,SHNYOKOSZ=RE.SHNYOKOSZ");		// 商品サイズ_横
		sbSQL.append(" ,SHNTATESZ=RE.SHNTATESZ");		// 商品サイズ_縦
		sbSQL.append(" ,SHNOKUSZ=RE.SHNOKUSZ");			// 商品サイズ_奥行
		sbSQL.append(" ,SHNJRYOSZ=RE.SHNJRYOSZ");		// 商品サイズ_重量
		sbSQL.append(" ,PBKBN=RE.PBKBN");				// PB区分
		sbSQL.append(" ,KOMONOKBM=RE.KOMONOKBM");		// 小物区分
		sbSQL.append(" ,TANAOROKBN=RE.TANAOROKBN");		// 棚卸区分
		sbSQL.append(" ,TEIKEIKBN=RE.TEIKEIKBN");		// 定計区分
		sbSQL.append(" ,ODS_HARUSU=RE.ODS_HARUSU");		// ODS_賞味期限_春
		sbSQL.append(" ,ODS_NATSUSU=RE.ODS_NATSUSU");	// ODS_賞味期限_夏
		sbSQL.append(" ,ODS_AKISU=RE.ODS_AKISU");		// ODS_賞味期限_秋
		sbSQL.append(" ,ODS_FUYUSU=RE.ODS_FUYUSU");		// ODS_賞味期限_冬
		sbSQL.append(" ,ODS_NYUKASU=RE.ODS_NYUKASU");	// ODS_入荷期限
		sbSQL.append(" ,ODS_NEBIKISU=RE.ODS_NEBIKISU");	// ODS_値引期限
		sbSQL.append(" ,PCARD_SHUKBN=RE.PCARD_SHUKBN");	// プライスカード_種類
		sbSQL.append(" ,PCARD_IROKBN=RE.PCARD_IROKBN");	// プライスカード_色
		sbSQL.append(" ,ZEIKBN=RE.ZEIKBN");				// 税区分
		sbSQL.append(" ,ZEIRTKBN=RE.ZEIRTKBN");			// 税率区分
		sbSQL.append(" ,ZEIRTKBN_OLD=RE.ZEIRTKBN_OLD");	// 旧税率区分
		sbSQL.append(" ,ZEIRTHENKODT=RE.ZEIRTHENKODT");	// 税率変更日
		sbSQL.append(" ,SEIZOGENNISU=RE.SEIZOGENNISU");	// 製造限度日数
		sbSQL.append(" ,TEIKANKBN=RE.TEIKANKBN");		// 定貫不定貫区分
		sbSQL.append(" ,MAKERCD=RE.MAKERCD");			// メーカーコード
		sbSQL.append(" ,IMPORTKBN=RE.IMPORTKBN");		// 輸入区分
		sbSQL.append(" ,SIWAKEKBN=RE.SIWAKEKBN");		// 仕分区分
		sbSQL.append(" ,HENPIN_KBN=RE.HENPIN_KBN");		// 返品区分
		sbSQL.append(" ,TAISHONENSU=RE.TAISHONENSU");	// 対象年齢
		sbSQL.append(" ,CALORIESU=RE.CALORIESU");		// カロリー表示
		sbSQL.append(" ,ELPFLG=RE.ELPFLG");				// フラグ情報_ELP
		sbSQL.append(" ,BELLMARKFLG=RE.BELLMARKFLG");	// フラグ情報_ベルマーク
		sbSQL.append(" ,RECYCLEFLG=RE.RECYCLEFLG");		// フラグ情報_リサイクル
		sbSQL.append(" ,ECOFLG=RE.ECOFLG");				// フラグ情報_エコマーク
		sbSQL.append(" ,HZI_YOTO=RE.HZI_YOTO");			// 包材用途
		sbSQL.append(" ,HZI_ZAISHITU=RE.HZI_ZAISHITU");	// 包材材質
		sbSQL.append(" ,HZI_RECYCLE=RE.HZI_RECYCLE");	// 包材リサイクル対象
		sbSQL.append(" ,KIKANKBN=RE.KIKANKBN");			// 期間
		sbSQL.append(" ,SHUKYUKBN=RE.SHUKYUKBN");		// 酒級
		sbSQL.append(" ,DOSU=RE.DOSU");					// 度数
		sbSQL.append(" ,CHINRETUCD=RE.CHINRETUCD");		// 陳列形式コード
		sbSQL.append(" ,DANTUMICD=RE.DANTUMICD");		// 段積み形式コード
		sbSQL.append(" ,KASANARICD=RE.KASANARICD");		// 重なりコード
		sbSQL.append(" ,KASANARISZ=RE.KASANARISZ");		// 重なりサイズ
		sbSQL.append(" ,ASSHUKURT=RE.ASSHUKURT");		// 圧縮率
		sbSQL.append(" ,SHUBETUCD=RE.SHUBETUCD");		// 種別コード
		sbSQL.append(" ,SALESCOMKN=RE.SALESCOMKN");		// 商品コピー・セールスコメント
		sbSQL.append(" ,URABARIKBN=RE.URABARIKBN");		// 裏貼
		sbSQL.append(" ,PCARD_OPFLG=RE.PCARD_OPFLG");	// プライスカード出力有無
		sbSQL.append(" ,PARENTCD=RE.PARENTCD");			// 親商品コード
		sbSQL.append(" ,BINKBN=RE.BINKBN");				// 便区分
		sbSQL.append(" ,HAT_MONKBN=RE.HAT_MONKBN");		// 発注曜日_月
		sbSQL.append(" ,HAT_TUEKBN=RE.HAT_TUEKBN");		// 発注曜日_火
		sbSQL.append(" ,HAT_WEDKBN=RE.HAT_WEDKBN");		// 発注曜日_水
		sbSQL.append(" ,HAT_THUKBN=RE.HAT_THUKBN");		// 発注曜日_木
		sbSQL.append(" ,HAT_FRIKBN=RE.HAT_FRIKBN");		// 発注曜日_金
		sbSQL.append(" ,HAT_SATKBN=RE.HAT_SATKBN");		// 発注曜日_土
		sbSQL.append(" ,HAT_SUNKBN=RE.HAT_SUNKBN");		// 発注曜日_日
		sbSQL.append(" ,READTMPTN=RE.READTMPTN");		// リードタイムパターン
		sbSQL.append(" ,SIMEKAISU=RE.SIMEKAISU");		// 締め回数
		sbSQL.append(" ,IRYOREFLG=RE.IRYOREFLG");		// 衣料使い回しフラグ
		sbSQL.append(" ,TOROKUMOTO=RE.TOROKUMOTO");		// F110: 登録元
		sbSQL.append(" ,UPDKBN=RE.UPDKBN");				// F111: 更新区分
		sbSQL.append(" ,SENDFLG=RE.SENDFLG");			// F112: 送信フラグ
		sbSQL.append(" ,OPERATOR=RE.OPERATOR");			// F113: オペレータ
		sbSQL.append(" ,UPDDT=current timestamp");			// F115: 更新日
		sbSQL.append(" ,K_HONKB=RE.K_HONKB");			// F116: 保温区分
		sbSQL.append(" ,K_WAPNFLG_R=RE.K_WAPNFLG_R");	// F117: デリカワッペン区分_レギュラー
		sbSQL.append(" ,K_WAPNFLG_H=RE.K_WAPNFLG_H");	// F118: デリカワッペン区分_販促
		sbSQL.append(" ,K_TORIKB=RE.K_TORIKB");			// F119: 取扱区分
		sbSQL.append(" ,ITFCD=RE.ITFCD");				// F120: ITFコード
		sbSQL.append(" ,CENTER_IRISU=RE.CENTER_IRISU");	// F121: センター入数

		// 仕入グループ商品マスタ
		StringBuffer sbSQL2 = new StringBuffer();
		sbSQL2.append("insert into INAMS.MSTSIRGPSHN(");
		sbSQL2.append("  SHNCD, TENGPCD, YOYAKUDT, AREAKBN");
		sbSQL2.append(", SIRCD, HSPTN, SENDFLG, OPERATOR, ADDDT, UPDDT");
		sbSQL2.append(") select");
		sbSQL2.append("  SHNCD, TENGPCD, YOYAKUDT, AREAKBN");
		sbSQL2.append(", SIRCD, HSPTN, SENDFLG");
		sbSQL2.append(", '"+userID+"' as OPERATOR");						// オペレータ
		sbSQL2.append(", '"+strDateInfo+"' as ADDDT");					// 登録日
		sbSQL2.append(", '"+strDateInfo+"' as UPDDT");						// 更新日
		sbSQL2.append(" from INAWS.PIMSISIRGPSHN");
		sbSQL2.append(" where SHNCD = (");
		sbSQL2.append(" select SHNCD from INAWS.PIMSIT");
		sbSQL2.append(" where SHNCD = '"+shohinCode+"' and SITSTCD = 4");
		sbSQL2.append(" )");

		// 売価コントロールマスタ
		StringBuffer sbSQL3 = new StringBuffer();
		sbSQL3.append("insert into INAMS.MSTBAIKACTL(");
		sbSQL3.append("  SHNCD, TENGPCD, YOYAKUDT, AREAKBN, GENKAAM");
		sbSQL3.append(", BAIKAAM, IRISU, SENDFLG, OPERATOR, ADDDT, UPDDT");
		sbSQL3.append(") select");
		sbSQL3.append("  SHNCD, TENGPCD, YOYAKUDT, AREAKBN, GENKAAM");
		sbSQL3.append(", BAIKAAM, IRISU, SENDFLG");
		sbSQL3.append(", '"+userID+"' as OPERATOR");						// オペレータ
		sbSQL3.append(", '"+strDateInfo+"' as ADDDT");					// 登録日
		sbSQL3.append(", '"+strDateInfo+"' as UPDDT");						// 更新日
		sbSQL3.append(" from INAWS.PIMSIBAIKACTL");
		sbSQL3.append(" where SHNCD = (");
		sbSQL3.append(" select SHNCD from INAWS.PIMSIT");
		sbSQL3.append(" where SHNCD = '"+shohinCode+"' and SITSTCD = 4");
		sbSQL3.append(" )");

		// ソースコード管理マスタ
		StringBuffer sbSQL4 = new StringBuffer();
		sbSQL4.append("insert into INAMS.MSTSRCCD(");
		sbSQL4.append("  SHNCD, SRCCD, YOYAKUDT, SEQNO, SOURCEKBN");
		sbSQL4.append(", SENDFLG, OPERATOR, ADDDT, UPDDT, YUKO_STDT, YUKO_EDDT");
		sbSQL4.append(") select");
		sbSQL4.append("  SHNCD, SRCCD, YOYAKUDT, SEQNO, SOURCEKBN");
		sbSQL4.append(", SENDFLG");
		sbSQL4.append(", '"+userID+"' as OPERATOR");						// オペレータ
		sbSQL4.append(", '"+strDateInfo+"' as ADDDT");					// 登録日
		sbSQL4.append(", '"+strDateInfo+"' as UPDDT");						// 更新日
		sbSQL4.append(", YUKO_STDT, YUKO_EDDT");
		sbSQL4.append(" from INAWS.PIMSISRCCD");
		sbSQL4.append(" where SHNCD = (");
		sbSQL4.append(" select SHNCD from INAWS.PIMSIT");
		sbSQL4.append(" where SHNCD = '"+shohinCode+"' and SITSTCD = 4");
		sbSQL4.append(" )");

		// 添加物マスタ
		StringBuffer sbSQL5 = new StringBuffer();
		sbSQL5.append("insert into INAMS.MSTTENKABUTSU(");
		sbSQL5.append("  SHNCD, TENKABKBN, TENKABCD, YOYAKUDT");
		sbSQL5.append(", SENDFLG, OPERATOR, ADDDT, UPDDT");
		sbSQL5.append(") select");
		sbSQL5.append("  SHNCD, TENKABKBN, TENKABCD, YOYAKUDT");
		sbSQL5.append(", SENDFLG");
		sbSQL5.append(", '"+userID+"' as OPERATOR");						// オペレータ
		sbSQL5.append(", '"+strDateInfo+"' as ADDDT");					// 登録日
		sbSQL5.append(", '"+strDateInfo+"' as UPDDT");						// 更新日
		sbSQL5.append(" from INAWS.PIMSITENKABUTSU");
		sbSQL5.append(" where SHNCD = (");
		sbSQL5.append(" select SHNCD from INAWS.PIMSIT");
		sbSQL5.append(" where SHNCD = '"+shohinCode+"' and SITSTCD = 4");
		sbSQL5.append(" )");

		// 品揃グループマスタ
		StringBuffer sbSQL6 = new StringBuffer();
		sbSQL6.append("insert into INAMS.MSTSHINAGP(");
		sbSQL6.append("  SHNCD, TENGPCD, YOYAKUDT, AREAKBN");
		sbSQL6.append(", ATSUKKBN, SENDFLG, OPERATOR, ADDDT, UPDDT");
		sbSQL6.append(") select");
		sbSQL6.append("  SHNCD, TENGPCD, YOYAKUDT, AREAKBN");
		sbSQL6.append(", ATSUKKBN, SENDFLG");
		sbSQL6.append(", '"+userID+"' as OPERATOR");						// オペレータ
		sbSQL6.append(", '"+strDateInfo+"' as ADDDT");					// 登録日
		sbSQL6.append(", '"+strDateInfo+"' as UPDDT");						// 更新日
		sbSQL6.append(" from INAWS.PIMSISHINAGP");
		sbSQL6.append(" where SHNCD = (");
		sbSQL6.append(" select SHNCD from INAWS.PIMSIT");
		sbSQL6.append(" where SHNCD = '"+shohinCode+"' and SITSTCD = 4");
		sbSQL6.append(" )");

		// メーカーマスタ
		StringBuffer sbSQL7 = new StringBuffer();
		sbSQL7.append("insert into INAMS.MSTMAKER(");
		sbSQL7.append("  MAKERCD, MAKERAN, MAKERKN, JANCD, DMAKERCD");
		sbSQL7.append(", UPDKBN, SENDFLG, OPERATOR, ADDDT, UPDDT");
		sbSQL7.append(") select");
		sbSQL7.append("  MAKERCD, MAKERAN, MAKERKN, JANCD, DMAKERCD");
		sbSQL7.append(", UPDKBN, SENDFLG");
		sbSQL7.append(", '"+userID+"' as OPERATOR");						// オペレータ
		sbSQL7.append(", '"+strDateInfo+"' as ADDDT");					// 登録日
		sbSQL7.append(", '"+strDateInfo+"' as UPDDT");						// 更新日
		sbSQL7.append(" from INAWS.PIMSIMAKER");
		sbSQL7.append(" where MAKERCD = (");
		sbSQL7.append(" select MAKERCD from INAWS.PIMSIT");
		sbSQL7.append(" where SITKNNO = '"+kenno+"' and SHNCD = '"+shohinCode+"' and SITSTCD = 4");
		sbSQL7.append(" )");

		int upCount = 0;
		upCount += updateBySQL(sbSQL.toString(), new ArrayList<String>(), con);

		// 更新0件 --> 更新失敗
		if(upCount < 1){
			return false;
		}

		updateBySQL(sbSQL2.toString(), new ArrayList<String>(), con);
		updateBySQL(sbSQL3.toString(), new ArrayList<String>(), con);
		updateBySQL(sbSQL4.toString(), new ArrayList<String>(), con);
		updateBySQL(sbSQL5.toString(), new ArrayList<String>(), con);
		updateBySQL(sbSQL6.toString(), new ArrayList<String>(), con);
		updateBySQL(sbSQL7.toString(), new ArrayList<String>(), con);

		return true;
	}

	/**
	 * 基本商品マスタ、仕入グループ商品マスタなどジャーナルへ登録
	 * @param map
	 * @param userInfo
	 * @param sysdate
	 * @param con
	 * @param code
	 * @param szYmd
	 * @param shohinSeq
	 * @param shohinCode
	 * @param userID
	 * @param sysDate
	 * @param sysTime
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("static-access")
	private boolean updateDBJNL(HashMap<String, String> map, User userInfo, String sysdate, Connection con, String code, String shohinSeq, String shohinCode, String userID, String strDateInfo, String sysDate, String sysTime, String kenno) throws Exception{
		// ジャーナル用変数取得
		/** ジャーナル更新のKEY保持用変数 */
		String jnlshn_seq = "";
		/** ジャーナル更新のテーブル区分保持用変数 */
		String jnlshn_tablekbn = "";
		/** ジャーナル更新の処理区分保持用変数 */
		String jnlshn_trankbn = "";

		// ジャーナル用の情報を取得
		jnlshn_seq = this.getJNLSHN_SEQ_NOW();
		jnlshn_tablekbn = DefineReport.ValTablekbn.SEI.getVal();
		jnlshn_trankbn = DefineReport.InfTrankbn.INS.getVal();

		// DB更新SQL
		StringBuffer sbSQL = new StringBuffer();
		sbSQL.append("insert into INAAD.JNLSHN (");
		sbSQL.append("  SEQ");			// SEQ
		sbSQL.append(" ,INF_DATE");		// 更新情報_更新日時
		sbSQL.append(" ,INF_OPERATOR");	// 更新情報_オペレータ
		sbSQL.append(" ,INF_TABLEKBN");	// 更新情報_テーブル区分
		sbSQL.append(" ,INF_TRANKBN");	// 更新情報_処理区分
		sbSQL.append(" ,");
		sbSQL.append("  SHNCD, YOYAKUDT, TENBAIKADT, YOT_BMNCD, YOT_DAICD, YOT_CHUCD, YOT_SHOCD");
		sbSQL.append(" ,URI_BMNCD, URI_DAICD, URI_CHUCD, URI_SHOCD, BMNCD, DAICD, CHUCD, SHOCD, SSHOCD");
		sbSQL.append(" ,ATSUK_STDT, ATSUK_EDDT, TEISHIKBN, SHOHINAN, SHOHINKN, PCARDKN, POPKN, RECEIPTAN");
		sbSQL.append(" ,RECEIPTKN, PCKBN, KAKOKBN, ICHIBAKBN, SHNKBN, SANCHIKN, SSIRCD, HSPTN");
		sbSQL.append(" ,RG_ATSUKFLG, RG_GENKAAM, RG_BAIKAAM, RG_IRISU, RG_IDENFLG, RG_WAPNFLG");
		sbSQL.append(" ,HS_ATSUKFLG, HS_GENKAAM, HS_BAIKAAM, HS_IRISU, HS_WAPNFLG, HS_SPOTMINSU");
		sbSQL.append(" ,HP_SWAPNFLG, KIKKN, UP_YORYOSU, UP_TYORYOSU, UP_TANIKBN, SHN_YOKOSZ, SHN_TATESZ");
		sbSQL.append(" ,SHN_OKUSZ, SHN_JRYOSZ, PBKBN, KOMONOKBM, TANAOROKBN, TEIKEIKBN, ODS_HARUSU");
		sbSQL.append(" ,ODS_NATSUSU, ODS_AKISU, ODS_FUYUSU, ODS_NYUKASU, ODS_NEBIKISU, PCARD_SHUKBN");
		sbSQL.append(" ,PCARD_IROKBN, ZEIKBN, ZEIRTKBN, ZEIRTKBN_OLD, ZEIRTHENKODT, SEIZOGENNISU, TEIKANKBN");
		sbSQL.append(" ,MAKERCD, IMPORTKBN, SIWAKEKBN, HENPIN_KBN, TAISHONENSU, CALORIESU, ELPFLG");
		sbSQL.append(" ,BELLMARKFLG, RECYCLEFLG, ECOFLG, HZI_YOTO, HZI_ZAISHITU, HZI_RECYCLE, KIKANKBN");
		sbSQL.append(" ,SHUKYUKBN, DOSU, CHINRETUCD, DANTUMICD, KASANARICD, KASANARISZ, ASSHUKURT");
		sbSQL.append(" ,SHUBETUCD, URICD, SALESCOMKN, URABARIKBN, PCARD_OPFLG, PARENTCD, BINKBN");
		sbSQL.append(" ,HAT_MONKBN, HAT_TUEKBN, HAT_WEDKBN, HAT_THUKBN, HAT_FRIKBN, HAT_SATKBN");
		sbSQL.append(" ,HAT_SUNKBN, READTMPTN, SIMEKAISU, IRYOREFLG, TOROKUMOTO, UPDKBN, SENDFLG");
		sbSQL.append(" ,OPERATOR, ADDDT, UPDDT, K_HONKB, K_WAPNFLG_R, K_WAPNFLG_H, K_TORIKB, ITFCD, CENTER_IRISU)");

		sbSQL.append(" select");
		sbSQL.append("  '"+jnlshn_seq+"' as SEQ");			// SEQ
		sbSQL.append(" , '"+strDateInfo+"' as INF_DATE");		// 更新情報_更新日時
		sbSQL.append(" , '"+userID+"' as INF_OPERATOR");	// 更新情報_オペレータ
		sbSQL.append(" , '"+jnlshn_tablekbn+"' as INF_TABLEKBN");	// 更新情報_テーブル区分
		sbSQL.append(" , '"+jnlshn_trankbn+"' as INF_TRANKBN");	// 更新情報_処理区分
		sbSQL.append(" ,");
		sbSQL.append("  SHNCD");													// 商品コード：
		sbSQL.append(", YOYAKUDT, TENBAIKADT, YOT_BMNCD, YOT_DAICD, YOT_CHUCD, YOT_SHOCD, URI_BMNCD");
		sbSQL.append(", URI_DAICD, URI_CHUCD, URI_SHOCD, BMNCD, DAICD, CHUCD, SHOCD, SSHOCD, ATSUK_STDT");
		sbSQL.append(", ATSUK_EDDT, TEISHIKBN, SHNAN, SHNKN, PCARDKN, POPKN, RECEIPTAN, RECEIPTKN, PCKBN");
		sbSQL.append(", KAKOKBN, ICHIBAKBN, SHNKBN, SANCHIKN, SSIRCD, HSPTN, RG_ATSUKFLG, RG_GENKAAM");
		sbSQL.append(", RG_BAIKAAM, RG_IRISU, RG_IDENFLG, RG_WAPNFLG, HS_ATSUKFLG, HS_GENKAAM, HS_BAIKAAM");
		sbSQL.append(", HS_IRISU, HS_WAPNFLG, HS_SPOTMINSU, HP_SWAPNFLG, KIKKN, UP_YORYOSU, UP_TYORYOSU");
		sbSQL.append(", UP_TANIKBN, SHNYOKOSZ, SHNTATESZ, SHNOKUSZ, SHNJRYOSZ, PBKBN, KOMONOKBM, TANAOROKBN");
		sbSQL.append(", TEIKEIKBN, ODS_HARUSU, ODS_NATSUSU, ODS_AKISU, ODS_FUYUSU, ODS_NYUKASU, ODS_NEBIKISU");
		sbSQL.append(", PCARD_SHUKBN, PCARD_IROKBN, ZEIKBN, ZEIRTKBN, ZEIRTKBN_OLD, ZEIRTHENKODT, SEIZOGENNISU");
		sbSQL.append(", TEIKANKBN, MAKERCD, IMPORTKBN, SIWAKEKBN, HENPIN_KBN, TAISHONENSU, CALORIESU, ELPFLG");
		sbSQL.append(", BELLMARKFLG, RECYCLEFLG, ECOFLG, HZI_YOTO, HZI_ZAISHITU, HZI_RECYCLE, KIKANKBN, SHUKYUKBN");
		sbSQL.append(", DOSU, CHINRETUCD, DANTUMICD, KASANARICD, KASANARISZ, ASSHUKURT, SHUBETUCD, URICD");
		sbSQL.append(", SALESCOMKN, URABARIKBN, PCARD_OPFLG, PARENTCD, BINKBN, HAT_MONKBN, HAT_TUEKBN");
		sbSQL.append(", HAT_WEDKBN, HAT_THUKBN, HAT_FRIKBN, HAT_SATKBN, HAT_SUNKBN, READTMPTN, SIMEKAISU");
		sbSQL.append(", IRYOREFLG, TOROKUMOTO, UPDKBN, SENDFLG");
		sbSQL.append(", '"+userID+"' as OPERATOR");						// オペレータ
		sbSQL.append(", '"+strDateInfo+"' as ADDDT");						// 登録日
		sbSQL.append(", '"+strDateInfo+"' as UPDDT");						// 更新日
		sbSQL.append(", K_HONKB, K_WAPNFLG_R, K_WAPNFLG_H, K_TORIKB, ITFCD, CENTER_IRISU");
		sbSQL.append(" from INAWS.PIMSIT");
		sbSQL.append(" where SHNCD = '"+shohinCode+"' and SITSTCD = 4");

		// 仕入グループ商品マスタ
		StringBuffer sbSQL2 = new StringBuffer();
		sbSQL2.append("insert into INAAD.JNLSIRSHN(");
		sbSQL2.append("  SEQ");			// SEQ
		sbSQL2.append(" ,RENNO");		// RENNO
		sbSQL2.append(" ,");
		sbSQL2.append("  SHNCD, TENGPCD, YOYAKUDT, AREAKBN");
		sbSQL2.append(", SIRCD, HSPTN, SENDFLG, OPERATOR, ADDDT, UPDDT");
		sbSQL2.append(") select");
		sbSQL2.append("  '"+jnlshn_seq+"' as SEQ");			// SEQ
		sbSQL2.append(" , '1' as RENNO");		// RENNO
		sbSQL2.append(" ,");
		sbSQL2.append("  SHNCD, TENGPCD, YOYAKUDT, AREAKBN");
		sbSQL2.append(", SIRCD, HSPTN, SENDFLG");
		sbSQL2.append(", '"+userID+"' as OPERATOR");						// オペレータ
		sbSQL2.append(", '"+strDateInfo+"' as ADDDT");					// 登録日
		sbSQL2.append(", '"+strDateInfo+"' as UPDDT");						// 更新日
		sbSQL2.append(" from INAWS.PIMSISIRGPSHN");
		sbSQL2.append(" where SHNCD = (");
		sbSQL2.append(" select SHNCD from INAWS.PIMSIT");
		sbSQL2.append(" where SHNCD = '"+shohinCode+"' and SITSTCD = 4");
		sbSQL2.append(" )");

		// 売価コントロールマスタ
		StringBuffer sbSQL3 = new StringBuffer();
		sbSQL3.append("insert into INAAD.JNLBAIKACTL(");
		sbSQL3.append("  SEQ");			// SEQ
		sbSQL3.append(" ,RENNO");		// RENNO
		sbSQL3.append(" ,");
		sbSQL3.append("  SHNCD, TENGPCD, YOYAKUDT, AREAKBN, GENKAAM");
		sbSQL3.append(", BAIKAAM, IRISU, SENDFLG, OPERATOR, ADDDT, UPDDT");
		sbSQL3.append(") select");
		sbSQL3.append("  '"+jnlshn_seq+"' as SEQ");			// SEQ
		sbSQL3.append(" , '1' as RENNO");		// RENNO
		sbSQL3.append(" ,");
		sbSQL3.append("  SHNCD, TENGPCD, YOYAKUDT, AREAKBN, GENKAAM");
		sbSQL3.append(", BAIKAAM, IRISU, SENDFLG");
		sbSQL3.append(", '"+userID+"' as OPERATOR");						// オペレータ
		sbSQL3.append(", '"+strDateInfo+"' as ADDDT");					// 登録日
		sbSQL3.append(", '"+strDateInfo+"' as UPDDT");						// 更新日
		sbSQL3.append(" from INAWS.PIMSIBAIKACTL");
		sbSQL3.append(" where SHNCD = (");
		sbSQL3.append(" select SHNCD from INAWS.PIMSIT");
		sbSQL3.append(" where SHNCD = '"+shohinCode+"' and SITSTCD = 4");
		sbSQL3.append(" )");

		// ソースコード管理マスタ
		StringBuffer sbSQL4 = new StringBuffer();
		sbSQL4.append("insert into INAAD.JNLSRCCD(");
		sbSQL4.append("  SEQ");			// SEQ
		sbSQL4.append(" ,RENNO");		// RENNO
		sbSQL4.append(" ,");
		sbSQL4.append("  SHNCD, SRCCD, YOYAKUDT, SEQNO, SRCKBN");
		sbSQL4.append(", SENDFLG, OPERATOR, ADDDT, UPDDT, YUKO_STDT, YUKO_EDDT");
		sbSQL4.append(") select");
		sbSQL4.append("  '"+jnlshn_seq+"' as SEQ");			// SEQ
		sbSQL4.append(" , '1' as RENNO");		// RENNO
		sbSQL4.append(" ,");
		sbSQL4.append("  SHNCD, SRCCD, YOYAKUDT, SEQNO, SOURCEKBN");
		sbSQL4.append(", SENDFLG");
		sbSQL4.append(", '"+userID+"' as OPERATOR");						// オペレータ
		sbSQL4.append(", '"+strDateInfo+"' as ADDDT");					// 登録日
		sbSQL4.append(", '"+strDateInfo+"' as UPDDT");						// 更新日
		sbSQL4.append(", YUKO_STDT, YUKO_EDDT");
		sbSQL4.append(" from INAWS.PIMSISRCCD");
		sbSQL4.append(" where SHNCD = (");
		sbSQL4.append(" select SHNCD from INAWS.PIMSIT");
		sbSQL4.append(" where SHNCD = '"+shohinCode+"' and SITSTCD = 4");
		sbSQL4.append(" )");

		// 添加物マスタ
		StringBuffer sbSQL5 = new StringBuffer();
		sbSQL5.append("insert into INAAD.JNLTENKABUTSU(");
		sbSQL5.append("  SEQ");			// SEQ
		sbSQL5.append(" ,RENNO");		// RENNO
		sbSQL5.append(" ,");
		sbSQL5.append("  SHNCD, TENKABKBN, TENKABCD, YOYAKUDT");
		sbSQL5.append(", SENDFLG, OPERATOR, ADDDT, UPDDT");
		sbSQL5.append(") select");
		sbSQL5.append("  '"+jnlshn_seq+"' as SEQ");			// SEQ
		sbSQL5.append(" , '1' as RENNO");		// RENNO
		sbSQL5.append(" ,");
		sbSQL5.append("  SHNCD, TENKABKBN, TENKABCD, YOYAKUDT");
		sbSQL5.append(", SENDFLG");
		sbSQL5.append(", '"+userID+"' as OPERATOR");						// オペレータ
		sbSQL5.append(", '"+strDateInfo+"' as ADDDT");					// 登録日
		sbSQL5.append(", '"+strDateInfo+"' as UPDDT");						// 更新日
		sbSQL5.append(" from INAWS.PIMSITENKABUTSU");
		sbSQL5.append(" where SHNCD = (");
		sbSQL5.append(" select SHNCD from INAWS.PIMSIT");
		sbSQL5.append(" where SHNCD = '"+shohinCode+"' and SITSTCD = 4");
		sbSQL5.append(" )");

		// 品揃グループマスタ
		StringBuffer sbSQL6 = new StringBuffer();
		sbSQL6.append("insert into INAAD.JNLSHINAGP(");
		sbSQL6.append("  SEQ");			// SEQ
		sbSQL6.append(" ,RENNO");		// RENNO
		sbSQL6.append(" ,");
		sbSQL6.append("  SHNCD, TENGPCD, YOYAKUDT, AREAKBN");
		sbSQL6.append(", ATSUKKBN, SENDFLG, OPERATOR, ADDDT, UPDDT");
		sbSQL6.append(") select");
		sbSQL6.append("  '"+jnlshn_seq+"' as SEQ");			// SEQ
		sbSQL6.append(" , '1' as RENNO");		// RENNO
		sbSQL6.append(" ,");
		sbSQL6.append("  SHNCD, TENGPCD, YOYAKUDT, AREAKBN");
		sbSQL6.append(", ATSUKKBN, SENDFLG");
		sbSQL6.append(", '"+userID+"' as OPERATOR");						// オペレータ
		sbSQL6.append(", '"+strDateInfo+"' as ADDDT");					// 登録日
		sbSQL6.append(", '"+strDateInfo+"' as UPDDT");						// 更新日
		sbSQL6.append(" from INAWS.PIMSISHINAGP");
		sbSQL6.append(" where SHNCD = (");
		sbSQL6.append(" select SHNCD from INAWS.PIMSIT");
		sbSQL6.append(" where SHNCD = '"+shohinCode+"' and SITSTCD = 4");
		sbSQL6.append(" )");

		// メーカーマスタはなし

		int upCount = 0;
		upCount += updateBySQL(sbSQL.toString(), new ArrayList<String>(), con);

		// 更新0件 --> 更新失敗
		if(upCount < 1){
			return false;
		}

		updateBySQL(sbSQL2.toString(), new ArrayList<String>(), con);
		updateBySQL(sbSQL3.toString(), new ArrayList<String>(), con);
		updateBySQL(sbSQL4.toString(), new ArrayList<String>(), con);
		updateBySQL(sbSQL5.toString(), new ArrayList<String>(), con);
		updateBySQL(sbSQL6.toString(), new ArrayList<String>(), con);

		return true;
	}


	/**
	 * チェック処理 （「確定」→「却下」）
	 * @param map
	 * @param userInfo
	 * @param sysdate
	 * @return
	 */
	public JSONArray check2(HashMap<String, String> map, User userInfo, String sysdate) {
		JSONArray msg = new JSONArray();

		return msg;
	}

	/**
	 * 更新処理実行 （「確定」→「却下」）
	 * @param map
	 * @param userInfo
	 * @param sysdate
	 * @return
	 * @throws Exception
	 */
	private JSONObject updateData2(HashMap<String, String> map, User userInfo, String sysdate) throws Exception {
		JSONObject rtn = new JSONObject();

		// パラメータ確認
		JSONArray dataArray	= JSONArray.fromObject(map.get("DATA"));	// 更新情報

		String userID = userInfo.getId();
		CmnDate dateInfo = new CmnDate();
		String sysDate = dateInfo.getToday();				// 本日日付
		String sysTime = dateInfo.getNowTime();				// 現在時刻
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss.SSS");
		String strDateInfo = dateFormat.format(dateInfo);

		ArrayList<String> prmData = new ArrayList<String>();
		int upCount = 0;

		// コネクション
		Connection con = null;

		try {
			// コネクションの取得
			con = DBConnection.getConnection(this.JNDIname);
			con.setAutoCommit(false);

			// 仕掛商品（INAWS.PIMSIT）の商品ステータス（SITSTCD）を却下（9）に更新
			String sqlUpdateCommand_1 =
					"update INAWS.PIMSIT set SITSTCD = 9, OPERATOR = ?, UPDDT = ? where SHNCD = ? and SITKNNO = ? and SITSTCD = 2";

			// 提案商品（INAWS.PIMTIT）の商品ステータス（TITSTCD）を却下（9）に更新
			String sqlUpdateCommand_2 =
					"update INAWS.PIMTIT set TITSTCD = 9, OPERATOR = ?, UPDDT = ? where TITKNNO = (select SITKNNO from INAWS.PIMSIT where SHNCD = ? and SITSTCD = 9) and SHNCD = ? and TITSTCD = 3";

			// 提案件名（INAWS.PIMTIK）の件名ステータス（TIKSTCD）を完了（4）に更新
			String sqlUpdateCommand_3 =
					"update INAWS.PIMTIK set TIKSTCD = 4, TIKEDDT = current_timestamp, T1UPID = ?, T1UPDT = ?, T1UPTM = ? where TIKKNNO = (select SITKNNO from INAWS.PIMSIT where SHNCD = ? and SITSTCD > 3) and TIKSTCD = 3 and (select COUNT(TITKNNO) from INAWS.PIMTIT where TITKNNO = (select SITKNNO from INAWS.PIMSIT where SHNCD = ? and SITSTCD > 3) and TITSTCD > 3) = (select COUNT(TITKNNO) from INAWS.PIMTIT where TITKNNO = (select SITKNNO from INAWS.PIMSIT where SHNCD = ? and SITSTCD > 3))";

			for (int i = 0; i < dataArray.size(); i++) {
				String code = "";
				String kenno = "";

				if(!dataArray.getJSONObject(i).isEmpty()){
					code = dataArray.getJSONObject(i).optString("F3");
					code = code.replace("-", "");
					kenno = dataArray.getJSONObject(i).optString("F2");
				}
				if(code.isEmpty()){
					continue;
				}
				if(kenno.isEmpty()) {
					continue;
				}

				// 仕掛商品の状態更新
				prmData = new ArrayList<String>();
				prmData.add(userID);
				prmData.add(strDateInfo);
				prmData.add(code);
				prmData.add(kenno);
				// SQL実行
				upCount += updateBySQL(sqlUpdateCommand_1, prmData, con);

				// 提案商品の状態更新
				prmData = new ArrayList<String>();
				prmData.add(userID);
				prmData.add(strDateInfo);
				prmData.add(code);
				prmData.add(code);
				// SQL実行（カウントしない）
				updateBySQL(sqlUpdateCommand_2, prmData, con);

				// 提案件名の状態更新
				prmData = new ArrayList<String>();
				prmData.add(userID);
				prmData.add(sysDate);
				prmData.add(sysTime);
				prmData.add(code);
				prmData.add(code);
				prmData.add(code);
				// SQL実行（カウントしない）
				updateBySQL(sqlUpdateCommand_3, prmData, con);
			}

			con.commit();
			con.close();

			if (dataArray.size() == upCount) {
				rtn.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00007.getVal(), "対象データ"));
			} else {
				rtn.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00006.getVal(), String.valueOf(upCount), String.valueOf(dataArray.size()  -upCount)));
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
	 * @param map
	 * @param userInfo
	 * @param sysdate
	 * @return
	 */
	@SuppressWarnings("static-access")
	public JSONArray check3(HashMap<String, String> map, User userInfo, String sysdate) {
		JSONArray msg = new JSONArray();

		// パラメータ確認
		JSONArray dataArray	= JSONArray.fromObject(map.get("DATA"));	// 更新情報

		return msg;
	}
	/**
	 * 更新処理実行（「作成中」→「確定」）
	 * @param map
	 * @param userInfo
	 * @param sysdate
	 * @return
	 * @throws Exception
	 */
	private JSONObject updateData3(HashMap<String, String> map, User userInfo, String sysdate) throws Exception {
		JSONObject rtn = new JSONObject();

		// パラメータ確認
		JSONArray dataArray	= JSONArray.fromObject(map.get("DATA"));	// 更新情報
		String szTeian		= "";										// 件名No
		String code			= "";										// 商品コード

		String userID = userInfo.getId();
		CmnDate dateInfo = new CmnDate();
		String sysDate = dateInfo.getToday();				// 本日日付
		String sysTime = dateInfo.getNowTime();				// 現在時刻
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss.SSS");
		String strDateInfo = dateFormat.format(dateInfo);

		ArrayList<String> prmData = new ArrayList<String>();
		int upCount = 0;

		// コネクション
		Connection con = null;

		try {
			// コネクションの取得
			con = DBConnection.getConnection(this.JNDIname);
			con.setAutoCommit(false);

			// 仕掛商品（INAWS.PIMSIT）の商品ステータス（SITSTCD）を確定（2）に更新
			String sqlUpdateCommand_1 =
					"update INAWS.PIMSIT set SITSTCD = 2, OPERATOR = ?, UPDDT = ? where SHNCD = ? and SITKNNO = ? and SITSTCD = 1";

			for (int i = 0; i < dataArray.size(); i++) {
				if(!dataArray.getJSONObject(i).isEmpty()){
					code = dataArray.getJSONObject(i).optString("F3");		// 商品コード
					code = code.replace("-", "");
					szTeian = dataArray.getJSONObject(i).optString("F2");	// 件名No
				}
				if(code.isEmpty()){
					continue;
				}
				if(szTeian.isEmpty()) {
					continue;
				}

				// 仕掛商品の状態更新
				prmData = new ArrayList<String>();
				prmData.add(userID);
				prmData.add(strDateInfo);
				prmData.add(code);
				prmData.add(szTeian);

				// SQL実行
				upCount += updateBySQL(sqlUpdateCommand_1, prmData, con);

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
	 * 削除処理
	 * @param request
	 * @param session
	 * @param map
	 * @param userInfo
	 * @param sysdate
	 * @return
	 */
	public JSONObject delete(HttpServletRequest request, HttpSession session, HashMap<String, String> map, User userInfo) {
		// パラメータ確認
		JSONArray dataArray	= JSONArray.fromObject(map.get("DATA"));	// 更新情報

		ArrayList<String> prmData = new ArrayList<String>();
		int upCount = 0;

		// 削除処理
		JSONObject rtn = new JSONObject();
		JSONArray msg = new JSONArray();

		// コネクション
		Connection con = null;

		try {
			// コネクションの取得
			con = DBConnection.getConnection(this.JNDIname);
			con.setAutoCommit(false);

			// 仕掛商品マスタ
			String sqlCommand_1 = "delete from INAWS.PIMSIT where SHNCD = ? and SITSTCD <= 2";
			// 仕掛仕入グループ商品マスタ
			String sqlCommand_2 = "delete from INAWS.PIMSISIRGPSHN where SHNCD = (select SHNCD from INAWS.PIMSIT where SHNCD = ? and SITSTCD <= 2)";
			// 仕掛売価コントロールマスタ
			String sqlCommand_3 = "delete from INAWS.PIMSIBAIKACTL where SHNCD = (select SHNCD from INAWS.PIMSIT where SHNCD = ? and SITSTCD <= 2)";
			// 仕掛ソースコード管理マスタ
			String sqlCommand_4 = "delete from INAWS.PIMSISRCCD where SHNCD = (select SHNCD from INAWS.PIMSIT where SHNCD = ? and SITSTCD <= 2)";
			// 仕掛添加物マスタ
			String sqlCommand_5 = "delete from INAWS.PIMSITENKABUTSU where SHNCD = (select SHNCD from INAWS.PIMSIT where SHNCD = ? and SITSTCD <= 2)";
			// 仕掛品揃グループマスタ
			String sqlCommand_6 = "delete from INAWS.PIMSISHINAGP where SHNCD = (select SHNCD from INAWS.PIMSIT where SHNCD = ? and SITSTCD <= 2)";
			// 仕掛メーカーマスタ
			String sqlCommand_7 = "delete from INAWS.PIMSIMAKER where MAKERCD = (select MAKERCD from INAWS.PIMSIT where SHNCD = ? and SITSTCD <= 2)";

			for (int i = 0; i < dataArray.size(); i++) {
				String code = "";
				String kenno = "";
				if(!dataArray.getJSONObject(i).isEmpty()){
					code = dataArray.getJSONObject(i).optString("F3");
					code =code.substring(0,4)+code.substring(5);
					kenno = dataArray.getJSONObject(i).optString("F2");
				}
				if(code.isEmpty()){
					continue;
				}
				if(kenno.isEmpty()) {
					continue;
				}

				// 仕掛仕入グループ商品マスタ
				prmData = new ArrayList<String>();
				prmData.add(code);
				// SQL実行
				updateBySQL(sqlCommand_2, prmData, con);

				// 仕掛売価コントロールマスタ
				prmData = new ArrayList<String>();
				prmData.add(code);
				// SQL実行
				updateBySQL(sqlCommand_3, prmData, con);

				// 仕掛ソースコード管理マスタ
				prmData = new ArrayList<String>();
				prmData.add(code);
				// SQL実行
				updateBySQL(sqlCommand_4, prmData, con);

				// 仕掛添加物マスタ
				prmData = new ArrayList<String>();
				prmData.add(code);
				// SQL実行
				updateBySQL(sqlCommand_5, prmData, con);

				// 仕掛品揃グループマスタ
				prmData = new ArrayList<String>();
				prmData.add(code);
				// SQL実行
				updateBySQL(sqlCommand_6, prmData, con);

				// 仕掛メーカーマスタ
				prmData = new ArrayList<String>();
				prmData.add(code);
				// SQL実行
				updateBySQL(sqlCommand_7, prmData, con);

				// 仕掛商品マスタ
				prmData = new ArrayList<String>();
				prmData.add(code);
				upCount += updateBySQL(sqlCommand_1, prmData, con);
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

	/**
	 * SEQ情報取得処理
	 *
	 * @throws Exception
	 */
	public String getJNLSHN_SEQ_NOW() {
		// 関連情報取得
		ItemList iL = new ItemList();
		String sqlColCommand = "VALUES NEXTVAL FOR INAMS.SEQ002";
		@SuppressWarnings("static-access")
		JSONArray array = iL.selectJSONArray(sqlColCommand, null, Defines.STR_JNDI_DS);
		String value = "";
		if(array.size() > 0){
			value = array.optJSONObject(0).optString("1");
		}
		return value;
	}

	/**
	 * ダウンロード用検索実行
	 *
	 * @return
	 */
	public boolean selectForDL() {
		// 検索コマンド生成
		String command = createCommandForDl();

		// 出力用検索条件生成
		outputQueryList();

		// 検索実行
		return super.selectBySQL(command);
	}

	/**
	 * ダウンロード用の検索SQL文作成
	 * @return
	 */
	private String createCommandForDl() {
		// ログインユーザー情報取得
		User userInfo = getUserInfo();
		if(userInfo==null){
			return "";
		}

		String btnId = getMap().get("BTN");			// 実行ボタン

		// パラメータ確認
		// 必須チェック
		if ((btnId == null)) {
			System.out.println(super.getConditionLog());
			return "";
		}

		String szBumon		= getMap().get("BUMON");			// 部門
		if(szBumon.equals("-1")) {
			szBumon = "";
		}

		String szShohin		= getMap().get("SHOHIN");			// 商品名
		String szHatyu			= getMap().get("HATYU");				// 発注先
		String szFromDate	= getMap().get("FROM_DATE");		// 商品登録日FROM
		String szToDate		= getMap().get("TO_DATE");			// 商品登録日TO

		String szWhereCmd = "";

		if(StringUtils.isNotEmpty(szBumon)){		// 条件：部門
			if( szBumon.substring(0,1).equals("0")) {
				szBumon = szBumon.substring(1);
			}
			szWhereCmd += " and T.BMNCD like '";
			szWhereCmd += szBumon;
			szWhereCmd += "%'";
		}
		if(StringUtils.isNotEmpty(szShohin)){		// 条件：商品名
			szWhereCmd += " and T.SHNKN like '%"+szShohin+"%'";
		}
		if(StringUtils.isNotEmpty(szHatyu)){		// 条件：発注先1
			szWhereCmd += " and T.SSIRCD = '"+szHatyu+"'";
		}
		// 商品登録日
		if(szFromDate.length() == 8) {
			szFromDate = szFromDate.substring(0, 4)+"-"+szFromDate.substring(4,6)+"-"+szFromDate.substring(6);
		}
		if(szToDate.length() == 8) {
			szToDate = szToDate.substring(0, 4)+"-"+szToDate.substring(4,6)+"-"+szToDate.substring(6);
		}
		if(StringUtils.isNotEmpty(szFromDate) && StringUtils.isNotEmpty(szToDate)){
			// FROM,TO 指定
			szWhereCmd += " and T.ADDDT between '"+szFromDate+"' and '"+szToDate+"'";
		} else if (StringUtils.isNotEmpty(szFromDate)) {
			// FROM 指定
			szWhereCmd += " and T.ADDDT >= '"+szFromDate+"'";
		} else if (StringUtils.isNotEmpty(szToDate)) {
			// TO 指定
			szWhereCmd += " and T.ADDDT <= '"+szToDate+"'";
		}

		int maxMSTSIRGPSHN =10;
		int maxMSTBAIKACTL = 5;
		int maxMSTSHINAGP = 10;

		int maxMSTTENKABUTSU1 = 10;
		int maxMSTTENKABUTSU2 = 30;

		// 一覧表情報
		StringBuffer sbSQL = new StringBuffer();
		sbSQL.append(" with");
		sbSQL.append("  WKSHN as (select SHNCD from INAWS.PIMSIT T1 where nvl(UPDKBN, 0) <> 1" + szWhereCmd);
		sbSQL.append(" )");
		sbSQL.append(" ,WKIDX(IDX) as (select 1 from SYSIBM.SYSDUMMY1 union all select IDX + 1 from WKIDX where IDX <= "+maxMSTTENKABUTSU1+") ");

		sbSQL.append(" select max(T.SITSTCD)");							// 状態
		sbSQL.append("  ,max(T.SITKNNO)");								// 件名No
		sbSQL.append("  ,''");														// 更新区分			:A
		sbSQL.append("  ,rtrim(T.SHNCD)");												// 商品コード		:
		sbSQL.append("  ,null");														// 商品コード桁数	:0
		sbSQL.append("  ,max(T.TEIKEIKBN)");											// 定計区分			:0
		sbSQL.append("  ,max(case X.IDX when 1 then T1.SOURCEKBN end)");				// ソース区分_1		:1
		sbSQL.append("  ,rtrim(max(case X.IDX when 1 then ''''||T1.SRCCD end))");		// ソースコード_1	:'4901737828775	※シングルクォーテーション
		sbSQL.append("  ,max(T.SSIRCD)");												// 標準仕入先コード	:13324
		sbSQL.append("  ,max(T.TEIKANKBN)");											// 定貫不定貫区分	:1
		sbSQL.append("  ,max(T.BMNCD)");												// 標準部門コード	:010
		sbSQL.append("  ,max(T.DAICD),max(T.CHUCD),max(T.SHOCD),max(right('00'||T.SSHOCD,2))");		// 標準分類コード	:41,05,97
		sbSQL.append("  ,max(T.PCKBN)");												// ＰＣ区分			:
		sbSQL.append("  ,max(T.SHNKBN)");												// 商品種類			:0
		sbSQL.append("  ,rtrim(max(T.PARENTCD))");										// 親商品コード		:0
		sbSQL.append("  ,max('\"'||T.SHNAN||'\"'),max('\"'||T.SHNKN||'\"')");			// 商品名			:"ｽｷﾞﾖ ﾔｻｲﾆｷﾞﾘｱｹﾞ","スギヨ　野菜にぎり揚"
		sbSQL.append("  ,max('\"'||T.RECEIPTAN||'\"'),max('\"'||T.RECEIPTKN||'\"')");	// レシート名		:
		sbSQL.append("  ,max('\"'||T.PCARDKN||'\"')");									// プライスカード商品名称（漢字）
		sbSQL.append("  ,max('\"'||T.SALESCOMKN||'\"')");								// 商品コメント・セールスコピー（漢字）
		sbSQL.append("  ,max('\"'||T.POPKN||'\"')");									// ＰＯＰ名称（漢字）
		sbSQL.append("  ,max('\"'||T.KIKKN||'\"')");									// 規格
		sbSQL.append("  ,max(T.RG_ATSUKFLG),max(RTRIM(TRIM('0' FROM T.RG_GENKAAM),'.')),max(T.RG_BAIKAAM),max(T.RG_IRISU),rtrim(max(T.RG_IDENFLG)),rtrim(max(T.RG_WAPNFLG))");		// レギュラー情報_取扱フラグ、原価、売価、店入数、一括伝票、ワッペン
		sbSQL.append("  ,max(T.HS_ATSUKFLG),max(RTRIM(TRIM('0' FROM T.HS_GENKAAM),'.')),max(T.HS_BAIKAAM),max(T.HS_IRISU),rtrim(max(T.HS_WAPNFLG)),rtrim(max(T.HP_SWAPNFLG))");		// 販促情報_取扱フラグ、原価、売価、店入数、ワッペン、特売ワッペン
		sbSQL.append("  ,max(T.BINKBN)");												// 便区分
		sbSQL.append("  ,max(T.SIMEKAISU)");											// 締め回数
		sbSQL.append("  ,max(right('00'||T.KOMONOKBM,2))");								// 小物区分
		sbSQL.append("  ,max(right('00'||T.SIWAKEKBN,2))");								// 仕分区分
		sbSQL.append("  ,max(right('00'||T.TANAOROKBN,2))");							// 棚卸区分
		sbSQL.append("  ,rtrim(max(T.KIKANKBN))");										// 期間
		sbSQL.append("  ,max(T.ODS_HARUSU),max(T.ODS_NATSUSU),max(T.ODS_AKISU),max(T.ODS_FUYUSU),max(T.ODS_NYUKASU),max(T.ODS_NEBIKISU)");					// ODS_賞味期限_春、夏、秋、冬、入荷期限、値引期限
		sbSQL.append("  ,max(T.HS_SPOTMINSU)");											// 販促情報_スポット最低発注数
		sbSQL.append("  ,max(T.SEIZOGENNISU)");											// 製造限度日数
		sbSQL.append("  ,max(T.READTMPTN)");											// リードタイムパターン
		sbSQL.append("  ,max(T.HAT_MONKBN),max(T.HAT_TUEKBN),max(T.HAT_WEDKBN),max(T.HAT_THUKBN),max(T.HAT_FRIKBN),max(T.HAT_SATKBN),max(T.HAT_SUNKBN)");	// 発注曜日_月、火、水、木、金、土、日
		sbSQL.append("  ,max(T.HSPTN)");												// 配送パターン
		sbSQL.append("  ,max(T.UP_YORYOSU),max(T.UP_TYORYOSU),max(right('00'||T.UP_TANIKBN,2))");		// ユニットプライス_容量、単位容量、ユニット単位
		sbSQL.append("  ,max(T.SHNTATESZ),max(T.SHNYOKOSZ),max(T.SHNOKUSZ),max(T.SHNJRYOSZ)");			// 商品サイズ_縦、横、奥行、重量
		sbSQL.append("  ,max(T.ATSUK_STDT),max(T.ATSUK_EDDT)");							// 取扱期間_開始日、終了日
		sbSQL.append("  ,rtrim(max(T.CHINRETUCD)),rtrim(max(T.DANTUMICD)),rtrim(max(T.KASANARICD)),max(T.KASANARISZ),max(T.ASSHUKURT)");					// 陳列形式コード、段積み形式コード、重なりコード、重なりサイズ、圧縮率
		sbSQL.append("  ,max(T.YOYAKUDT),max(T.TENBAIKADT)");							// マスタ変更予定日、店売価実施日
		sbSQL.append("  ,max(T.YOT_BMNCD),max(T.YOT_DAICD),max(T.YOT_CHUCD),max(T.YOT_SHOCD)");			// 用途分類コード_部門、大分類、中分類、小分類
		sbSQL.append("  ,max(T.URI_BMNCD),max(T.URI_DAICD),max(T.URI_CHUCD),max(T.URI_SHOCD)");			// 売場分類コード_部門、大分類、中分類、小分類
		sbSQL.append("  ,max(T2.AREAKBN)");												// エリア区分
		for (int i=1; i<=maxMSTSIRGPSHN; i++) {
			sbSQL.append("  ,max(case X.IDX when "+i+" then T2.TENGPCD end),max(case X.IDX when "+i+" then T2.SIRCD end),max(case X.IDX when "+i+" then T2.HSPTN end)");		// エリア区分、仕入先コード、配送パターン
		}
		sbSQL.append("  ,max(T3.AREAKBN)");
		for (int i=1; i<=maxMSTBAIKACTL; i++) {
			sbSQL.append(",max(case X.IDX when "+i+" then T3.TENGPCD end)");			// 店グループ（エリア）
			sbSQL.append(",TO_CHAR(max(case X.IDX when "+i+" then (case when MOD(T3.GENKAAM, 1) = 0 then int(T3.GENKAAM) else double(T3.GENKAAM) end) end))");		// 原価
			sbSQL.append(",max(case X.IDX when "+i+" then T3.BAIKAAM end)");			// 売価
			sbSQL.append(",max(case X.IDX when "+i+" then T3.IRISU end)");				// 店入数

		}
		sbSQL.append("  ,max(T4.AREAKBN)");												// エリア区分
		for (int i=1; i<=maxMSTSHINAGP; i++) {
			sbSQL.append("  ,max(case X.IDX when "+i+" then T4.TENGPCD end),max(case X.IDX when "+i+" then T4.ATSUKKBN end)");			// 店グループ（エリア）、扱い区分
		}
		sbSQL.append("  ,max(T5.AVGPTANKAAM)");											// 平均パック単価
		sbSQL.append("  ,max(case X.IDX when 2 then T1.SOURCEKBN end),max(case X.IDX when 2 then ''''||T1.SRCCD end)");					// ソース区分_2、ソースコード_2
		sbSQL.append("  ,max(T.PCARD_OPFLG),max(T.PCARD_SHUKBN),max(T.PCARD_IROKBN)");	// プライスカード出力有無、種類、色
		sbSQL.append("  ,max(T.ZEIKBN),max(T.ZEIRTKBN),max(T.ZEIRTKBN_OLD),max(T.ZEIRTHENKODT)");	// 税区分、税率区分、旧税率区分、税率変更日
		sbSQL.append("  ,max(T.TEISHIKBN),max(T.ICHIBAKBN),max(T.PBKBN),max(T.HENPIN_KBN),max(right('00'||T.IMPORTKBN,2)),max(T.URABARIKBN),max(T.TAISHONENSU),max(T.CALORIESU),max(T.KAKOKBN)");		// 取扱停止、市場区分、ＰＢ区分、返品区分、輸入区分、裏貼、対象年齢、カロリー表示、加工区分
		sbSQL.append("  ,max('\"'||T.SANCHIKN||'\"'),max(right('00'||T.SHUKYUKBN,2)),max(T.DOSU),max(T.HZI_YOTO),max(T.HZI_ZAISHITU),max(T.HZI_RECYCLE)");		// 産地（漢字）、酒級、度数、包材用途、包材材質、包材リサイクル対象
		sbSQL.append("  ,max(T.ELPFLG),max(T.BELLMARKFLG),max(T.RECYCLEFLG),max(T.ECOFLG)");		// フラグ情報_ＥＬＰ、ベルマーク、リサイクル、エコマーク
		sbSQL.append("  ,max(T.MAKERCD),max(T.URICD)");									// メーカーコード、販売コード
		for (int i=1; i<=maxMSTTENKABUTSU1; i++) {
			sbSQL.append("  ,max(case X.IDX when "+i+" then T6.TENKABCD end)");			// 添加物
		}
		for (int i=1; i<=maxMSTTENKABUTSU2; i++) {
			sbSQL.append("  ,max(case X.IDX when "+i+" then T7.TENKABCD end)");			// アレルギー
		}
		sbSQL.append("  ,rtrim(max(T.SHUBETUCD)),max(T.IRYOREFLG),max(T.TOROKUMOTO),max(T.OPERATOR),max(TO_CHAR(T.ADDDT,'YYYYMMDD')),max(TO_CHAR(T.UPDDT,'YYYYMMDD'))");		// 種別コード、衣料使い回しフラグ、登録元、オペレータ、登録日、更新日
		sbSQL.append(" from INAWS.PIMSIT T");
		sbSQL.append(" inner join WKSHN T0 on T.SHNCD = T0.SHNCD");
		sbSQL.append(" inner join WKIDX X on 1 = 1");
		sbSQL.append(" left outer join (");
		// SEQNO＝ソースコード優先順位という認識
		sbSQL.append("   select T1.SHNCD,T1.SRCCD, T1.SOURCEKBN, ROW_NUMBER() over(PARTITION by T1.SHNCD order by SEQNO) as RNO");
		sbSQL.append("   from INAMS.MSTSRCCD T1");
		sbSQL.append("   inner join WKSHN T0 on T0.SHNCD = T1.SHNCD and T1.SEQNO <= 2");
		sbSQL.append(" ) T1 on T.SHNCD = T1.SHNCD and X.IDX = T1.RNO");
		sbSQL.append(" left outer join (");
		sbSQL.append("   select T2.SHNCD,T2.AREAKBN,T2.TENGPCD, T2.SIRCD, T2.HSPTN, ROW_NUMBER() over(PARTITION by T2.SHNCD) as RNO");
		sbSQL.append("   from INAMS.MSTSIRGPSHN T2 ");
		sbSQL.append("   inner join WKSHN T0 on T0.SHNCD = T2.SHNCD");
		sbSQL.append(" ) T2 on T.SHNCD = T2.SHNCD and X.IDX = T2.RNO and T2.RNO <= " + maxMSTSIRGPSHN);
		sbSQL.append(" left outer join (");
		sbSQL.append("   select T3.SHNCD,T3.AREAKBN,T3.TENGPCD, T3.GENKAAM, T3.BAIKAAM, T3.IRISU, ROW_NUMBER() over(PARTITION by T3.SHNCD) as RNO");
		sbSQL.append("   from INAMS.MSTBAIKACTL T3 ");
		sbSQL.append("   inner join WKSHN T0 on T0.SHNCD = T3.SHNCD");
		sbSQL.append(" ) T3 on T.SHNCD = T3.SHNCD and X.IDX = T3.RNO and T3.RNO <= " + maxMSTBAIKACTL);
		sbSQL.append(" left outer join (");
		sbSQL.append("   select T4.SHNCD,T4.AREAKBN,T4.TENGPCD, T4.ATSUKKBN, ROW_NUMBER() over(PARTITION by T4.SHNCD) as RNO");
		sbSQL.append("   from INAMS.MSTSHINAGP T4 ");
		sbSQL.append("   inner join WKSHN T0 on T0.SHNCD = T4.SHNCD");
		sbSQL.append(" ) T4 on T.SHNCD = T4.SHNCD and X.IDX = T4.RNO and T4.RNO <= " + maxMSTSHINAGP);
		sbSQL.append(" left outer join INAMS.MSTAVGPTANKA T5 on T.SHNCD = T5.SHNCD");
		sbSQL.append(" left outer join (");
		sbSQL.append("   select T6.SHNCD, T6.TENKABCD, ROW_NUMBER() over(PARTITION by T6.SHNCD) as RNO");
		sbSQL.append("   from INAMS.MSTTENKABUTSU T6 ");
		sbSQL.append("   inner join WKSHN T0 on T0.SHNCD = T6.SHNCD and T6.TENKABKBN = 2");
		sbSQL.append(" ) T6 on T.SHNCD = T6.SHNCD and X.IDX = T6.RNO and T6.RNO <= " + maxMSTTENKABUTSU1);
		sbSQL.append(" left outer join (");
		sbSQL.append("   select T7.SHNCD, T7.TENKABCD, ROW_NUMBER() over(PARTITION by T7.SHNCD) as RNO");
		sbSQL.append("   from INAMS.MSTTENKABUTSU T7 ");
		sbSQL.append("   inner join WKSHN T0 on T0.SHNCD = T7.SHNCD and T7.TENKABKBN = 1");
		sbSQL.append(" ) T7 on T.SHNCD = T7.SHNCD and X.IDX = T7.RNO and T7.RNO <= " + maxMSTTENKABUTSU1);

		sbSQL.append(" group by T.SHNCD");
		sbSQL.append(" order by T.SHNCD");

		if (DefineReport.ID_DEBUG_MODE)	System.out.println(getClass().getSimpleName()+"[sql]"+sbSQL.toString());
		return sbSQL.toString();
	}

}
