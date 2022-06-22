package dao;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.JDBCType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import authentication.bean.User;
import common.DefineReport;
import common.DefineReport.DataType;
import common.Defines;
import common.InputChecker;
import common.ItemList;
import common.JsonArrayData;
import common.MessageUtility;
import common.MessageUtility.FieldType;
import common.MessageUtility.Msg;
import common.MessageUtility.MsgKey;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 */
public class ReportJU012Dao extends ItemDao {

	/**
	 * インスタンスを生成します。
	 * @param source
	 */
	public ReportJU012Dao(String JNDIname) {
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

	/**
	 * 他画面からの呼び出し検索実行
	 *
	 * @return
	 */
	public String createCommandSub(HashMap<String, String> map, User userInfo) {

		// ユーザー情報を設定
		super.setUserInfo(userInfo);

		// 検索条件などの情報を設定
		super.setMap(map);

		// 検索コマンド生成
		String command = createCommandArr();

		// 出力用検索条件生成
		outputQueryList();

		// 検索実行
		return command;
	}

	/**
	 * 更新処理
	 * @param userInfo
	 *
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws Exception
	 */
	public JSONObject update(HttpServletRequest request,HttpSession session,  HashMap<String, String> map, User userInfo) {

		// 更新情報チェック(基本JS側で制御)
		JSONObject msgObj = new JSONObject();
		JSONArray msg = this.check(map);

		if(msg.size() > 0){
			msgObj.put(MsgKey.E.getKey(), msg);
			return msgObj;
		}

		// 更新処理
		try {
			msgObj = this.updateData(map, userInfo);
		} catch (Exception e) {
			e.printStackTrace();
			msgObj.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
		}
		return msgObj;
	}

	private String createCommand() {
		StringBuffer sbSQL = new StringBuffer();
		return sbSQL.toString();
	}

	private String createCommandArr() {

		String shncd		= getMap().get("SHNCD");	// 商品コード
		String nndt			= getMap().get("NNDT");		// 納入日
		String htdt			= getMap().get("HTDT");		// 発注日
		String rank			= getMap().get("RANK");		// ランクNo
		String moyscd		= getMap().get("MOYSCD");	// 催しコード
		String callPage		= getMap().get("callpage");
		String kanrino		= getMap().containsKey("KANRINO") ? String.valueOf(Integer.valueOf(getMap().get("KANRINO"))).trim() : "";
		JSONArray dataArrayG = JSONArray.fromObject(getMap().get("DATA_TENHT"));	// 更新情報(数量パターン)

		return getCommand(moyscd,shncd,htdt,nndt,kanrino,rank,callPage,dataArrayG);
	}

	public String getCommand(String moyscd, String shncd, String htdt, String nndt, String kanrino, String rank, String callPage, JSONArray dataArrayG) {

		ItemList	iL		= new ItemList();
		JSONArray	dbDatasAddTen	= new JSONArray();

		// SQL構文
		StringBuffer			sbSQL	= new StringBuffer();
		String sqlcommand = "";

		// DB検索用パラメータ
		String sqlWhere	= "";
		String sqlFrom	= "";
		ArrayList<String> paramData = new ArrayList<String>();

		String[] taisyoTen	= new String[]{};

		String moyskbn	= "";	// 催し区分
		String moysstdt	= "";	// 催し開始日
		String moysrban	= "";	// 催し連番

		if (!StringUtils.isEmpty(moyscd) && moyscd.length() >= 8) {
			moyskbn		= moyscd.substring(0,1);
			moysstdt	= moyscd.substring(1,7);
			moysrban	= moyscd.substring(7);
		}

		JSONArray dbDatas  = getJuShnArr(moyscd,kanrino,shncd,htdt,nndt);

		if (dbDatas.size() == 0) {
			return sqlcommand;
		}

		// 対象店を取得
		if (!StringUtils.isEmpty(rank)) {

			sqlFrom = "INATK.TOKRANK ";

			// 商品コードより部門コードを取得
			if (StringUtils.isEmpty(shncd)) {
				sqlWhere += "BMNCD=null AND ";
			} else {
				if (shncd.length() >= 2) {
					sqlWhere += "BMNCD=? AND ";
					paramData.add(shncd.substring(0,2));
				} else {
					sqlWhere += "BMNCD=null AND ";
				}
			}

			if (Integer.valueOf(rank) >= 900) {

				// 催し区分
				if (StringUtils.isEmpty(moyskbn)) {
					sqlWhere += "MOYSKBN=null AND ";
				} else {
					sqlWhere += "MOYSKBN=? AND ";
					paramData.add(moyskbn);
				}

				// 催し開始日
				if (StringUtils.isEmpty(moysstdt)) {
					sqlWhere += "MOYSSTDT=null AND ";
				} else {
					sqlWhere += "MOYSSTDT=? AND ";
					paramData.add(moysstdt);
				}

				// 催し連番
				if (StringUtils.isEmpty(moysrban)) {
					sqlWhere += "MOYSRBAN=null AND ";
				} else {
					sqlWhere += "MOYSRBAN=? AND ";
					paramData.add(moysrban);
				}

				sqlFrom = "INATK.TOKRANKEX ";
			}

			// ランクNo.
			sqlWhere += "RANKNO=? AND ";
			paramData.add(rank);

			// 一覧表情報
			sbSQL	= new StringBuffer();
			sbSQL.append("SELECT ");
			sbSQL.append("TENRANK_ARR ");
			sbSQL.append("FROM ");
			sbSQL.append(sqlFrom);
			sbSQL.append("WHERE ");
			sbSQL.append(sqlWhere);
			sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal() + " ");

			dbDatasAddTen = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

			// ランク№入力あり
			if (dbDatasAddTen.size() == 1) {
				taisyoTen = dbDatasAddTen.getJSONObject(0).getString("TENRANK_ARR").split("");
			}

		} else {
			// 数量0以外を対象店にする
			for (int j = 0; j < dataArrayG.size(); j++) {
				JSONObject dataG = new JSONObject();
				dataG = dataArrayG.getJSONObject(j);

				if(!dataG.isEmpty() && !StringUtils.isEmpty(dataG.optString("F1"))){
					int tencd = dataG.optInt("F1");
					String suryo = dataG.optString("F2");

					if (!StringUtils.isEmpty(suryo)) {
						if (!suryo.equals("0")) {
							tencds.add(tencd);
						}
					}
				}
			}
		}

		int cnt = 0;
		String tenCd = "";
		JSONObject data = new JSONObject();
		for (int i = 0; i < dbDatas.size(); i++) {

			// 商品、発注日、納入日で取得できた事前発注商品の店発注数配列を取得
			data = dbDatas.getJSONObject(i);
			HashMap<String,String> tenHtsuArrMap = getDigitMap(data.optString("ARR"),5,"");

			// ランク入力がある場合
			if (!StringUtils.isEmpty(rank)) {
				for (int j = 0; j < taisyoTen.length; j++) {
					if(!StringUtils.isEmpty(StringUtils.trim(taisyoTen[j]))) {

						String key = String.valueOf(j+1);

						// 店コード取得用
						if (tenHtsuArrMap.containsKey(key)) {
							if (cnt == 0) {
								tenCd = key;
							}
							cnt++;
						}
					}
				}

			// ランク№入力なし
			} else {

				Iterator<Integer> ten = tencds.iterator();
				for (int j = 0; j < tencds.size(); j++) {

					String key = String.valueOf(ten.next());

					// 店コード取得用
					if (tenHtsuArrMap.containsKey(key)) {
						if (cnt == 0) {
							tenCd = key;
						}
						cnt++;
					}
				}
			}
			if (cnt != 0) {
				break;
			}
		}

		// 重複件数がある場合商品コードに紐づく便区分を取得
		if (cnt != 0) {
			htdt = htdt.substring(4,6) + "月" + htdt.substring(6,8) + "日";
			nndt = nndt.substring(4,6) + "月" + nndt.substring(6,8) + "日";

			moyskbn		= data.getString("MOYSKBN");
			moysstdt	= data.getString("MOYSSTDT");
			moysrban	= data.getString("MOYSRBAN");

			moyscd = moyskbn + "-" + moysstdt + "-" + String.format("%03d",Integer.valueOf(moysrban));

			sqlcommand = "SELECT ";
			sqlcommand += "'商品コード ' || T1.F1 || ' 便区分 ' || T2.BINKBN || '便' || '<br>"
						+ "発注日 ' || T1.F2 || ' 納入日 ' || T1.F3 || ' 催しコード ' || T1.F4 || ' と<br>' || T1.F5 || '号店以下 ' || T1.F6 || '店舗重複しています。<br><br>' AS VALUE ";
			sqlcommand += "FROM (values(";
			sqlcommand += "'" + shncd + "',";
			sqlcommand += "'" + htdt + "',";
			sqlcommand += "'" + nndt + "',";
			sqlcommand += "'" + moyscd + "',";
			sqlcommand += "'" + String.format("%03d",Integer.valueOf(tenCd)) + "',";
			sqlcommand += cnt;
			sqlcommand += ")) AS T1(F1,F2,F3,F4,F5,F6) LEFT JOIN INAMS.MSTSHN T2 ON T1.F1 = T2.SHNCD";
		}

		// オプション情報（タイトル）設定
		if (DefineReport.ID_DEBUG_MODE) System.out.println(getClass().getSimpleName()+"[sql]"+sqlcommand);
		return sqlcommand;

	}

	/**
	 * @param shncd
	 * @param htdt
	 * @param nndt
	 * @return 取得レコード件数
	 */
	public JSONArray getJuShnArr(String moyscd, String kanrino, String shncd, String htdt, String nndt) {

		ItemList	iL		= new ItemList();

		// SQL構文
		StringBuffer	sbSQL	= new StringBuffer();

		// DB検索用パラメータ
		String sqlWhere	= "";
		String sqlWith = "";
		ArrayList<String> paramData = new ArrayList<String>();

		String sqlFrom	= "INATK.TOKJU_SHN T1";

		String moyskbn	= "";	// 催し区分
		String moysstdt	= "";	// 催し開始日
		String moysrban	= "";	// 催し連番


		if (!StringUtils.isEmpty(moyscd) && moyscd.length() >= 8) {
			moyskbn		= moyscd.substring(0,1);
			moysstdt	= moyscd.substring(1,7);
			moysrban	= moyscd.substring(7);

			// アンケート付の場合
			if (moyskbn.equals("8")) {
				sqlFrom	= "INATK.TOKQJU_SHN T1";
			}

			if (!StringUtils.isEmpty(kanrino)) {
				sqlWith = "with WK as (";
				sqlWith += " SELECT MOYSKBN,MOYSSTDT,MOYSRBAN,KANRINO ";
				sqlWith += " FROM "+ sqlFrom;
				sqlWith += " WHERE MOYSKBN=? and MOYSSTDT=? and MOYSRBAN=? and KANRINO=?";
				sqlWith += " AND UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal() + ")";
				paramData.add(moyskbn);
				paramData.add(moysstdt);
				paramData.add(moysrban);
				paramData.add(kanrino);
			}
		}

		// 商品コード
		if (StringUtils.isEmpty(shncd)) {
			sqlWhere += "SHNCD=null AND ";
		} else {
			sqlWhere += "SHNCD=? AND ";
			paramData.add(shncd);
		}

		// 発注日
		if (StringUtils.isEmpty(htdt)) {
			sqlWhere += "HTDT=null AND ";
		} else {
			sqlWhere += "HTDT=? AND ";
			paramData.add(htdt);
		}

		// 納入日
		if (StringUtils.isEmpty(nndt)) {
			sqlWhere += "NNDT=null ";
		} else {
			sqlWhere += "NNDT=? ";
			paramData.add(nndt);
		}

		// 一覧表情報
		sbSQL	= new StringBuffer();
		sbSQL.append(sqlWith + "SELECT * FROM ");
		sbSQL.append("(SELECT TENHTSU_ARR AS ARR ");
		sbSQL.append(",MOYSKBN ");
		sbSQL.append(",MOYSSTDT ");
		sbSQL.append(",MOYSRBAN ");
		sbSQL.append(",SHNCD ");
		sbSQL.append(",HTDT ");
		sbSQL.append(",NNDT ");
		sbSQL.append(",KANRINO ");
		sbSQL.append("FROM INATK.TOKJU_SHN ");
		sbSQL.append(" WHERE ");
		sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal() + " ");
		sbSQL.append("UNION ALL SELECT TENHTSU_ARR AS ARR ");
		sbSQL.append(",MOYSKBN ");
		sbSQL.append(",MOYSSTDT ");
		sbSQL.append(",MOYSRBAN ");
		sbSQL.append(",SHNCD ");
		sbSQL.append(",HTDT ");
		sbSQL.append(",NNDT ");
		sbSQL.append(",KANRINO ");
		sbSQL.append("FROM INATK.TOKQJU_SHN ");
		sbSQL.append(" WHERE ");
		sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal() + ") AS T1 ");
		sbSQL.append(" WHERE ");
		sbSQL.append(sqlWhere);
		if (!StringUtils.isEmpty(sqlWith)) {
			sbSQL.append(" AND NOT EXISTS(");
			sbSQL.append(" SELECT 1 FROM WK T2");
			sbSQL.append(" WHERE T1.MOYSKBN=T2.MOYSKBN AND T1.MOYSSTDT=T2.MOYSSTDT AND T1.MOYSRBAN=T2.MOYSRBAN AND T1.KANRINO=T2.KANRINO)");
		}

		return iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);
	}

	private void outputQueryList() {

		// 検索条件の加工クラス作成
		JsonArrayData jad = new JsonArrayData();
		jad.setJsonString(getJson());

		// 保存用 List (検索情報)作成
		setWhere(new ArrayList<List<String>>());
		List<String> cells = new ArrayList<String>();

		// 共通箇所設定
		createCmnOutput(jad);

	}

	boolean isTest = true;

	/** SQLリスト保持用変数 */
	ArrayList<String> sqlList = new ArrayList<String>();
	/** SQLのパラメータリスト保持用変数 */
	ArrayList<ArrayList<String>> prmList = new ArrayList<ArrayList<String>>();
	/** SQLログ用のラベルリスト保持用変数 */
	ArrayList<String> lblList = new ArrayList<String>();

	// 対象店関連
	String tenht	= "1"; // 1:同一発注数量 2:ランク別発注数量 3:店別発注数量
	String kanrino	= "";
	Set<Integer>			tencds	= new TreeSet<Integer>();
	HashMap<String,String>	tenRank	= new HashMap<String,String>();

	/**
	 * 更新処理実行
	 * @return
	 *
	 * @throws Exception
	 */
	private JSONObject updateData(HashMap<String, String> map, User userInfo) throws Exception {
		// パラメータ確認
		JSONObject	option		= new JSONObject();
		JSONArray	dataArray	= JSONArray.fromObject(map.get("DATA"));		// 更新情報(事前打出し商品)
		JSONArray	dataArrayG	= JSONArray.fromObject(map.get("DATA_TENHT"));	// 更新情報(数量パターン)

		// 排他チェック用
		String				targetTable	= null;
		String				targetWhere	= null;
		ArrayList<String>	targetParam	= new ArrayList<String>();
		JSONArray			msg			= new JSONArray();

		// 基本登録情報
		JSONObject data = dataArray.getJSONObject(0);

		// 事前打出しINSERT/UPDATE処理
		createSqlJu(data,dataArrayG,userInfo);

		// 排他チェック実行
		if(!StringUtils.isEmpty(data.optString("F1"))){

			String moyskbn = "";
			String moysstdt = "";
			String moysrban = "";

			if (data.optString("F1").length() >= 8) {
				moyskbn		= data.optString("F1").substring(0,1);
				moysstdt	= data.optString("F1").substring(1,7);
				moysrban	= data.optString("F1").substring(7);
			}

			// 催し区分
			if (StringUtils.isEmpty(moyskbn)) {
				targetWhere = "MOYSKBN=null AND ";
			} else {
				targetWhere = "MOYSKBN=? AND ";
				targetParam.add(moyskbn);
			}

			// 催し開始日
			if (StringUtils.isEmpty(moysstdt)) {
				targetWhere += "MOYSSTDT=null AND ";
			} else {
				targetWhere += "MOYSSTDT=? AND ";
				targetParam.add(moysstdt);
			}

			// 催し連番
			if (StringUtils.isEmpty(moysrban)) {
				targetWhere += "MOYSRBAN=null AND ";
			} else {
				targetWhere += "MOYSRBAN=? AND ";
				targetParam.add(moysrban);
			}

			// 管理番号
			if (StringUtils.isEmpty(kanrino)) {
				targetWhere += "KANRINO=null ";
			} else {
				targetWhere += "KANRINO=? ";
				targetParam.add(kanrino);
			}

			targetTable = "INATK.TOKJU_SHN";

			if(!super.checkExclusion(targetTable, targetWhere, targetParam, "")){
	 			msg.add(MessageUtility.getDbMessageExclusion(FieldType.DEFAULT, new String[]{}));
	 			option.put(MsgKey.E.getKey(), msg);
				return option;
			}
		}

		ArrayList<Integer> countList  = new ArrayList<Integer>();
		if(sqlList.size() > 0){
			countList =  super.executeSQLs(sqlList, prmList);
		}

		if(StringUtils.isEmpty(getMessage())){
			int count = 0;
			for (int i = 0; i < countList.size(); i++) {
				count += countList.get(i);
				if (DefineReport.ID_DEBUG_MODE) System.out.println(MessageUtility.getMessage(Msg.S00006.getVal(), new String[]{lblList.get(i), Integer.toString(countList.get(i))}));
			}
			if(count==0){
				option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E10000.getVal()));
			}else{
				option.put(MsgKey.S.getKey(), MessageUtility.getDbMessageIdObj("I00002", new String[]{"管理番号"+kanrino+"で"}));
			}
		}else{
			option.put(MsgKey.E.getKey(), getMessage());
		}
		return option;
	}

	/**
	 * チェック処理
	 *
	 * @throws Exception
	 */
	public JSONArray check(HashMap<String, String> map) {
		// パラメータ確認
		JSONArray	dataArray	= JSONArray.fromObject(map.get("DATA"));		// 更新情報(事前打出商品)
		JSONArray	dataArrayG	= JSONArray.fromObject(map.get("DATA_TENHT"));	// 更新情報(数量パターン)
		String		shoriDt		= map.get("SHORIDT");							// 処理日付

		// 格納用変数
		StringBuffer			sbSQL	= new StringBuffer();
		JSONArray				msg		= new JSONArray();
		ItemList				iL		= new ItemList();
		MessageUtility			mu		= new MessageUtility();
		JSONArray				dbDatas	= new JSONArray();

		String moyskbn = "";
		String moysstdt = "";
		String moysrban = "";
		String rankNoAdd = "";
		String sryPtnNo = "";

		String[]				taisyoTen	= new String[]{};

		// DB検索用パラメータ
		String sqlWhere		= "";
		String sqlValues	= "";
		String sqlFrom		= "";
		ArrayList<String> paramData = new ArrayList<String>();

		// チェック処理
		// 対象件数チェック
		if(dataArray.size() == 0 || dataArray.getJSONObject(0).isEmpty()){
			msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
			return msg;
		}

		if(dataArrayG.size() == 0 || dataArrayG.getJSONObject(0).isEmpty()){
			msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
			return msg;
		}

		// 催しコード存在チェック
		JSONObject data = dataArray.getJSONObject(0);

		if (!StringUtils.isEmpty(data.optString("F1")) &&
				data.optString("F1").length() >= 8) {
			moyskbn		= data.optString("F1").substring(0,1);
			moysstdt	= data.optString("F1").substring(1,7);
			moysrban	= data.optString("F1").substring(7);
		} else {
			msg.add(mu.getDbMessageObj("E20005", new String[]{}));
			return msg;
		}

		// 催し区分
		if (StringUtils.isEmpty(moyskbn)) {
			sqlWhere += "MOYSKBN=null AND ";
		} else {
			sqlWhere += "MOYSKBN=? AND ";
			paramData.add(moyskbn);
		}

		// 催し開始日
		if (StringUtils.isEmpty(moysstdt)) {
			sqlWhere += "MOYSSTDT=null AND ";
		} else {
			sqlWhere += "MOYSSTDT=? AND ";
			paramData.add(moysstdt);
		}

		// 催し連番
		if (StringUtils.isEmpty(moysrban)) {
			sqlWhere += "MOYSRBAN=null";
		} else {
			sqlWhere += "MOYSRBAN=?";
			paramData.add(moysrban);
		}

		sbSQL.append("SELECT ");
		sbSQL.append("NNSTDT ");	// 納入開始日
		sbSQL.append(",NNEDDT ");	// 納入終了日
		sbSQL.append("FROM ");
		sbSQL.append("INATK.TOKMOYCD ");
		sbSQL.append("WHERE ");
		sbSQL.append(sqlWhere);		// 入力された催しコード

		dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

		if (dbDatas.size() == 0){
			// 登録のない催しコード
			msg.add(mu.getDbMessageObj("E20005", new String[]{}));
			return msg;
		}

		// 発注日範囲チェック
		if (!StringUtils.isEmpty(data.optString("F13")) && !StringUtils.isEmpty(data.optString("F12")) &&
				data.optInt("F13") <= data.optInt("F12")) {
			// 納入日 <= 発注日はエラー
			msg.add(mu.getDbMessageObj("E20264", new String[]{}));
			return msg;
		}

		// 納入日の範囲チェック
		if (dbDatas.getJSONObject(0).optInt("NNSTDT") > data.optInt("F13") ||
				dbDatas.getJSONObject(0).optInt("NNEDDT") < data.optInt("F13")) {
			// 納入日 <= 発注日はエラー
			msg.add(mu.getDbMessageObj("E20274", new String[]{}));
			return msg;
		}

		// 処理日付 < 発注日 < 納入日以外エラー
		if (!StringUtils.isEmpty(shoriDt)) {
			if (!(Integer.valueOf(shoriDt) < data.optInt("F12"))) {
				// 発注日>処理日付の条件で入力してください。
				msg.add(mu.getDbMessageObj("E20127", new String[]{}));
				return msg;
			} else if (!(data.optInt("F12") < data.optInt("F13"))) {
				// 発注日 ＜ 納入日の条件で入力してください。
				msg.add(mu.getDbMessageObj("E20264", new String[]{}));
				return msg;
			}
		}

		// 同一数量、ランク別発注数量、店別数量発注のいずれか一つのみ入力可能(必須)
		for (int i = 0; i < dataArrayG.size(); i++) {
			JSONObject dataG = new JSONObject();
			dataG = dataArrayG.getJSONObject(i);
			if(!dataG.isEmpty() && !StringUtils.isEmpty(dataG.optString("F2"))){
				tenht = "3";
				break;
			}
		}

		if ((!StringUtils.isEmpty(data.optString("F14")) || !StringUtils.isEmpty(data.optString("F15"))) &&
				(!StringUtils.isEmpty(data.optString("F16")) || !StringUtils.isEmpty(data.optString("F17")))) {
			// 同一数量発注入力の場合、ランク別発注数量入力登録できません。
			msg.add(mu.getDbMessageObj("E20461", new String[]{}));
			return msg;
		}

		if ((!StringUtils.isEmpty(data.optString("F14")) || !StringUtils.isEmpty(data.optString("F15")))) {
			if (tenht.equals("3")) {
				// 同一数量発注入力の場合、店別数量発注入力登録できません。
				msg.add(mu.getDbMessageObj("E20462", new String[]{}));
				return msg;
			}
		}

		if ((!StringUtils.isEmpty(data.optString("F16")) || !StringUtils.isEmpty(data.optString("F17")))) {
			if (tenht.equals("3")) {
				// ランク別発注数量入力の場合、店別数量発注入力登録できません。
				msg.add(mu.getDbMessageObj("E20463", new String[]{}));
				return msg;
			}
			tenht = "2";
		}

		if (StringUtils.isEmpty(data.optString("F14")) && StringUtils.isEmpty(data.optString("F15")) &&
				StringUtils.isEmpty(data.optString("F16")) && StringUtils.isEmpty(data.optString("F17")) && !tenht.equals("3")) {
			// 同一発注数量、ランク別発注数量、店別発注数量のいづれか一つは入力してください。
			msg.add(mu.getDbMessageObj("E20569", new String[]{}));
			return msg;
		}

		if (tenht.equals("1")) {
			if ((!StringUtils.isEmpty(data.optString("F14")) && StringUtils.isEmpty(data.optString("F15")))) {
				msg.add(mu.getDbMessageObj("EX1103", new String[]{"発注数"}));
				return msg;
			}
			if ((StringUtils.isEmpty(data.optString("F14")) && !StringUtils.isEmpty(data.optString("F15")))) {
				msg.add(mu.getDbMessageObj("EX1103", new String[]{"ランク"}));
				return msg;
			}
		}
		if (tenht.equals("2")) {
			if ((!StringUtils.isEmpty(data.optString("F16")) && StringUtils.isEmpty(data.optString("F17")))) {
				msg.add(mu.getDbMessageObj("EX1103", new String[]{"パターン"}));
				return msg;
			}
			if ((StringUtils.isEmpty(data.optString("F16")) && !StringUtils.isEmpty(data.optString("F17")))) {
				msg.add(mu.getDbMessageObj("EX1103", new String[]{"ランク"}));
				return msg;
			}
		}

		// 変数を初期化
		sbSQL	= new StringBuffer();
		iL		= new ItemList();
		dbDatas = new JSONArray();
		sqlWhere	= "";
		paramData	= new ArrayList<String>();

		// 商品存在チェック
		if (StringUtils.isEmpty(data.optString("F2"))) {
			sqlWhere += "SHNCD=null AND ";
		} else {
			sqlWhere += "SHNCD=? AND ";
			paramData.add(data.optString("F2"));
		}

		sbSQL.append("SELECT ");
		sbSQL.append("SHNCD ");	// レコード件数
		sbSQL.append("FROM ");
		sbSQL.append("INAMS.MSTSHN ");
		sbSQL.append("WHERE ");
		sbSQL.append(sqlWhere); // 入力された商品コードで検索
		sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal() + " ");

		dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

		if (dbDatas.size() == 0){
			// マスタに登録のない商品
			msg.add(mu.getDbMessageObj("E40012", new String[]{}));
			return msg;
		}

		// 変数を初期化
		dbDatas = new JSONArray();
		dbDatas = new ReportJU032Dao(JNDIname).getMoysCdChk(data);

		if (dbDatas.size() != 0){
			// 催し区分8で使用されている商品は登録不可
			msg.add(mu.getDbMessageObj("E20162", new String[]{"催し区分=8で"}));
			return msg;
		}

		// 対象店取得
		if (tenht.equals("3")) {
			for (int i = 0; i < dataArrayG.size(); i++) {
				JSONObject dataG = new JSONObject();
				dataG = dataArrayG.getJSONObject(i);

				if(!dataG.isEmpty() && !StringUtils.isEmpty(dataG.optString("F1"))){

					int tencd = dataG.optInt("F1");

					if(!tencds.contains(tencd)) {
						tencds.add(tencd);
					}
				}
			}

		// 同一発注数量 or ランク別発注数量に入力があった場合ランクマスタより対象店を取得
		} else {

			// 変数を初期化
			sbSQL	= new StringBuffer();
			iL		= new ItemList();
			dbDatas = new JSONArray();
			sqlWhere	= "";
			paramData	= new ArrayList<String>();

			rankNoAdd = data.optString("F14");
			if (StringUtils.isEmpty(rankNoAdd)) {
				rankNoAdd	= data.optString("F16");
				sryPtnNo	= data.optString("F17");
			}

			sqlFrom = "INATK.TOKRANK ";

			// 商品コードより部門コードを取得
			if (StringUtils.isEmpty(data.optString("F2"))) {
				sqlWhere += "BMNCD=null AND ";
			} else {
				if (data.optString("F2").length() >= 2) {
					sqlWhere += "BMNCD=? AND ";
					paramData.add(data.optString("F2").substring(0,2));
				} else {
					sqlWhere += "BMNCD=null AND ";
				}
			}

			if (Integer.valueOf(rankNoAdd) >= 900) {

				// 催し区分
				if (StringUtils.isEmpty(moyskbn)) {
					sqlWhere += "MOYSKBN=null AND ";
				} else {
					sqlWhere += "MOYSKBN=? AND ";
					paramData.add(moyskbn);
				}

				// 催し開始日
				if (StringUtils.isEmpty(moysstdt)) {
					sqlWhere += "MOYSSTDT=null AND ";
				} else {
					sqlWhere += "MOYSSTDT=? AND ";
					paramData.add(moysstdt);
				}

				// 催し連番
				if (StringUtils.isEmpty(moysrban)) {
					sqlWhere += "MOYSRBAN=null AND ";
				} else {
					sqlWhere += "MOYSRBAN=? AND ";
					paramData.add(moysrban);
				}

				sqlFrom = "INATK.TOKRANKEX ";
			}

			// ランクNo.
			sqlWhere += "RANKNO=? AND ";
			paramData.add(rankNoAdd);

			// 一覧表情報
			sbSQL	= new StringBuffer();
			sbSQL.append("SELECT ");
			sbSQL.append("TENRANK_ARR ");
			sbSQL.append("FROM ");
			sbSQL.append(sqlFrom);
			sbSQL.append("WHERE ");
			sbSQL.append(sqlWhere);
			sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal() + " ");

			dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

			if (dbDatas.size() == 0){
				// 存在しない対象店ランク№
				if (Integer.valueOf(rankNoAdd) >= 900) {
					msg.add(mu.getDbMessageObj("E20466", new String[]{}));
				} else {
					msg.add(mu.getDbMessageObj("E20057", new String[]{}));
				}
				return msg;
			}

			// 対象店の取得
			taisyoTen = dbDatas.getJSONObject(0).getString("TENRANK_ARR").split("");

			for (int i = 0; i < taisyoTen.length; i++) {
				if(!StringUtils.isEmpty(StringUtils.trim(taisyoTen[i]))) {

					// 店コード取得用
					if (!tencds.contains(i+1)) {
						tencds.add(i+1);
					}

					// 店ランク取得用
					if (!tenRank.containsKey(i+1)) {
						tenRank.put(String.valueOf(i+1),taisyoTen[i]);
					}
				}
			}

			if (!StringUtils.isEmpty(sryPtnNo)) {

				// 変数を初期化
				sbSQL	= new StringBuffer();
				iL		= new ItemList();
				dbDatas = new JSONArray();
				sqlWhere	= "";
				paramData	= new ArrayList<String>();

				sqlFrom = "INATK.TOKSRPTN ";

				// 商品コードより部門コードを取得
				if (StringUtils.isEmpty(data.optString("F2"))) {
					sqlWhere += "BMNCD=null AND ";
				} else {
					if (data.optString("F2").length() >= 2) {
						sqlWhere += "BMNCD=? AND ";
						paramData.add(data.optString("F2").substring(0,2));
					} else {
						sqlWhere += "BMNCD=null AND ";
					}
				}

				if (Integer.valueOf(sryPtnNo) >= 900) {

					// 催し区分
					if (StringUtils.isEmpty(moyskbn)) {
						sqlWhere += "MOYSKBN=null AND ";
					} else {
						sqlWhere += "MOYSKBN=? AND ";
						paramData.add(moyskbn);
					}

					// 催し開始日
					if (StringUtils.isEmpty(moysstdt)) {
						sqlWhere += "MOYSSTDT=null AND ";
					} else {
						sqlWhere += "MOYSSTDT=? AND ";
						paramData.add(moysstdt);
					}

					// 催し連番
					if (StringUtils.isEmpty(moysrban)) {
						sqlWhere += "MOYSRBAN=null AND ";
					} else {
						sqlWhere += "MOYSRBAN=? AND ";
						paramData.add(moysrban);
					}

					sqlFrom = "INATK.TOKSRPTNEX ";
				}

				// ランクNo.
				sqlWhere += "SRYPTNNO=? AND ";
				paramData.add(sryPtnNo);

				// 一覧表情報
				sbSQL	= new StringBuffer();
				sbSQL.append("SELECT ");
				sbSQL.append("SRYPTNNO ");
				sbSQL.append("FROM ");
				sbSQL.append(sqlFrom);
				sbSQL.append("WHERE ");
				sbSQL.append(sqlWhere);
				sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal() + " ");

				dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

				if (dbDatas.size() == 0){
					// 存在しない数量パターン
					msg.add(mu.getDbMessageObj("E20131", new String[]{}));
					return msg;
				}
			}
		}
		return msg;
	}

	/**
	 * チェック処理
	 *
	 * @throws Exception
	 */
	public JSONArray checkData(MessageUtility mu,
			JSONArray		dataArray,		// 事前打出し商品(アン付含む)
			String			shoriDt,
			String			report
	) {

		// 格納用変数
		StringBuffer		sbSQL	= new StringBuffer();
		JSONArray			msg		= new JSONArray();
		ItemList			iL		= new ItemList();
		JSONArray			dbDatas	= new JSONArray();
		ArrayList<String>	paramData = new ArrayList<String>();

		String moyskbn = "";
		String moysstdt = "";
		String moysrban = "";

		// DB検索用パラメータ
		String sqlWhere		= "";
		String sqlValues	= "";
		String sqlFrom		= "";

		// 基本データチェック:入力値がテーブル定義と矛盾してないか確認、
		// 1.正規
		for (int i = 0; i < dataArray.size(); i++) {
			JSONObject jo = dataArray.optJSONObject(i);

			if (jo.size() == 0) {
				continue;
			}

			String reqNo = String.valueOf(i+1) + "行目：";

			for(TOKJU_CKLayout colinf: TOKJU_CKLayout.values()){

				String val = StringUtils.trim(jo.optString(colinf.getId()));
				if(StringUtils.isNotEmpty(val)){
					DataType dtype = null;
					int[] digit = null;

					String txt = colinf.getTxt() + "は";
					if (colinf.getCol().equals(TOKJU_CKLayout.MOYSKBN.getCol()) ||
							colinf.getCol().equals(TOKJU_CKLayout.MOYSSTDT.getCol()) ||
							colinf.getCol().equals(TOKJU_CKLayout.MOYSRBAN.getCol())) {
						txt = "催しコードは";
					}

					try {
						DefineReport.InpText inpsetting = DefineReport.InpText.valueOf(colinf.getCol());
						dtype = inpsetting.getType();
						digit = new int[]{inpsetting.getDigit1(), inpsetting.getDigit2()};
					}catch (IllegalArgumentException e){
						dtype = colinf.getDataType();
						digit = colinf.getDigit();
					}

					if (colinf.getCol().equals(TOKJU_CKLayout.TENHTSU_ARR.getCol())) {
						dtype = TOKJU_CKLayout.TENHTSU_ARR.getDataType();
						digit = new int[]{3, 0};
					}

					// ①データ型による文字種チェック
					if(!InputChecker.checkDataType(dtype, val)){
						// エラー発生箇所を保存
						JSONObject o = mu.getDbMessageObjDataTypeErr(dtype, new String[]{reqNo+txt});
						msg.add(o);
						return msg;
					}
					// ②データ桁チェック
					if(!InputChecker.checkDataLen(dtype, val, digit)){
						// エラー発生箇所を保存
						JSONObject o = mu.getDbMessageObjLen(dtype, new String[]{reqNo+txt});
						msg.add(o);
						return msg;
					}
				}
			}
		}

		// 新規(正) 1.1　必須入力項目チェックを行う。
		// 変更(正) 1.1　必須入力項目チェックを行う。
		TOKJU_CKLayout[] targetCol = null;
		targetCol = new TOKJU_CKLayout[]{TOKJU_CKLayout.MOYSKBN,TOKJU_CKLayout.MOYSSTDT,TOKJU_CKLayout.MOYSRBAN,TOKJU_CKLayout.SHNCD,TOKJU_CKLayout.SHNKBN,
											TOKJU_CKLayout.TSEIKBN,TOKJU_CKLayout.HTDT,TOKJU_CKLayout.NNDT,TOKJU_CKLayout.IRISU,TOKJU_CKLayout.GENKAAM,
											TOKJU_CKLayout.BAIKAAM,TOKJU_CKLayout.TENHTSU_ARR,TOKJU_CKLayout.SURYO};

		for (int i = 0; i < dataArray.size(); i++) {
			JSONObject jo = dataArray.optJSONObject(i);

			if (jo.size() == 0) {
				continue;
			}

			String reqNo = String.valueOf(i+1) + "行目：";
			String shnkbn = "";

			for(TOKJU_CKLayout colinf: targetCol){

				String val = StringUtils.trim(jo.optString(colinf.getId()));

				if (colinf.getCol().equals(TOKJU_CKLayout.SHNKBN.getCol())) {
					shnkbn = val;
				}

				if(StringUtils.isEmpty(val)){

					String txt = colinf.getTxt();
					if (colinf.getCol().equals(TOKJU_CKLayout.MOYSKBN.getCol()) ||
							colinf.getCol().equals(TOKJU_CKLayout.MOYSSTDT.getCol()) ||
							colinf.getCol().equals(TOKJU_CKLayout.MOYSRBAN.getCol())) {
						txt = "催しコード";
					} else if (shnkbn.equals("0") && (colinf.getCol().equals(TOKJU_CKLayout.IRISU.getCol()) ||
							colinf.getCol().equals(TOKJU_CKLayout.GENKAAM.getCol()) ||
							colinf.getCol().equals(TOKJU_CKLayout.BAIKAAM.getCol()))
					) {
						continue;
					}

					// エラー発生箇所を保存
					JSONObject o = mu.getDbMessageObj("EX1047", new String[]{reqNo+txt});
					msg.add(o);
					return msg;
				}
			}
		}

		// チェック済キー格納用変数
		Set<String> moysCd		= new TreeSet<String>();
		Set<String> shnCd		= new TreeSet<String>();
		Set<String> tenCd		= new TreeSet<String>();
		Set<String> juQju		= new TreeSet<String>();
		HashMap<String,String> shnNndtMap	= new HashMap<String,String>();

		for (int i = 0; i < dataArray.size(); i++) {
			// 催しコード存在チェック
			JSONObject data = dataArray.getJSONObject(i);

			if (data.size() == 0) {
				continue;
			}

			String reqNo = String.valueOf(i+1) + "行目：";

			moyskbn		= data.optString("F1");
			moysstdt	= data.optString("F2");
			moysrban	= data.optString("F3");

			String key = moyskbn + moysstdt + moysrban;
			if (!moysCd.contains(key)) {
				// 変数を初期化
				sbSQL	= new StringBuffer();
				iL		= new ItemList();
				dbDatas = new JSONArray();
				sqlWhere	= "";
				paramData	= new ArrayList<String>();

				// 催し区分
				if (StringUtils.isEmpty(moyskbn)) {
					sqlWhere += "T1.MOYSKBN=null AND ";
				} else {
					sqlWhere += "T1.MOYSKBN=? AND ";
					paramData.add(moyskbn);
				}

				// 催し開始日
				if (StringUtils.isEmpty(moysstdt)) {
					sqlWhere += "T1.MOYSSTDT=null AND ";
				} else {
					sqlWhere += "T1.MOYSSTDT=? AND ";
					paramData.add(moysstdt);
				}

				// 催し連番
				if (StringUtils.isEmpty(moysrban)) {
					sqlWhere += "T1.MOYSRBAN=null AND ";
				} else {
					sqlWhere += "T1.MOYSRBAN=? AND ";
					paramData.add(moysrban);
				}

				// 事前打出しじゃない場合
				if (!DefineReport.ID_PAGE_JU037.equals(report)) {
					sqlFrom = "INATK.TOKQJU_MOY T1 LEFT JOIN INATK.TOKMOYCD T2 ON T1.MOYSKBN=T2.MOYSKBN AND T1.MOYSSTDT=T2.MOYSSTDT AND T1.MOYSRBAN=T2.MOYSRBAN ";

					sbSQL.append("SELECT ");
					sbSQL.append("T2.NNSTDT ");		// 納入開始日
					sbSQL.append(",T2.NNEDDT ");	// 納入終了日
				} else {
					sqlFrom = "INATK.TOKMOYCD T1 ";

					sbSQL.append("SELECT ");
					sbSQL.append("NNSTDT ");	// 納入開始日
					sbSQL.append(",NNEDDT ");	// 納入終了日
				}

				sbSQL.append("FROM ");
				sbSQL.append(sqlFrom);
				sbSQL.append("WHERE ");
				sbSQL.append(sqlWhere);		// 入力された催しコード
				sbSQL.append("T1.UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal() + " ");

				dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

				if (dbDatas.size() == 0){
					// 登録のない催しコード
					msg.add(mu.getDbMessageObj("E20005", new String[]{reqNo}));
					return msg;
				}
			}

			// 発注日範囲チェック
			if (!StringUtils.isEmpty(data.optString("F11")) && !StringUtils.isEmpty(data.optString("F10")) &&
					data.optInt("F11") <= data.optInt("F10")) {
				// 納入日 <= 発注日はエラー
				msg.add(mu.getDbMessageObj("E20264", new String[]{reqNo}));
				return msg;
			}

			if (!moysCd.contains(key)) {
				// 納入日の範囲チェック
				if (dbDatas.getJSONObject(0).optInt("NNSTDT") > data.optInt("F11") ||
						dbDatas.getJSONObject(0).optInt("NNEDDT") < data.optInt("F11")) {
					// 納入日 <= 発注日はエラー
					msg.add(mu.getDbMessageObj("E20274", new String[]{reqNo}));
					return msg;
				}
				moysCd.add(key);
			}

			// 処理日付 < 発注日 < 納入日以外エラー
			if (!StringUtils.isEmpty(shoriDt)) {
				if (!(Integer.valueOf(shoriDt) < data.optInt("F10"))) {
					// 発注日>処理日付の条件で入力してください。
					msg.add(mu.getDbMessageObj("E20127", new String[]{reqNo}));
					return msg;
				} else if (!(data.optInt("F10") < data.optInt("F11"))) {
					// 発注日 ＜ 納入日の条件で入力してください。
					msg.add(mu.getDbMessageObj("E20264", new String[]{reqNo}));
					return msg;
				}
			}

			// 訂正区分が2 or 4はエラー
			if (data.optString("F6").equals("2") || data.optString("F6").equals("4")) {
				// 事前区分に2または4を設定することはできません。
				msg.add(mu.getDbMessageObj("EX1118", new String[]{reqNo}));
				return msg;
			}

			if (!data.optString("F6").equals("0") && !data.optString("F6").equals("1") && !data.optString("F6").equals("3")) {
				// 訂正区分は0,1,3のいずれかを入力して下さい。
				msg.add(mu.getDbMessageObj("EX1047", new String[]{reqNo+"訂正区分は0,1,3のいずれか"}));
				return msg;
			}

			if (!data.optString("F5").equals("0") && !data.optString("F5").equals("1") && !data.optString("F5").equals("2")) {
				// 商品区分は0,1,2のいずれかを入力して下さい。
				msg.add(mu.getDbMessageObj("EX1047", new String[]{reqNo+"商品区分は0,1,2のいずれか"}));
				return msg;
			}

			if (!StringUtils.isEmpty(data.optString("F8")) && (data.optInt("F8") < 0 || data.optInt("F8") >= 9)) {
				// 別伝区分は0～8の値を入力してください。
				msg.add(mu.getDbMessageObj("EX1047", new String[]{reqNo+"別伝区分は0～8の値"}));
				return msg;
			}

			if (!data.optString("F9").equals("0") && !data.optString("F9").equals("1") && !data.optString("F9").equals("2")) {
				// ワッペン区分は0,1,2のいずれかを入力して下さい。
				msg.add(mu.getDbMessageObj("EX1047", new String[]{reqNo+"ワッペン区分は0,1,2のいずれか"}));
				return msg;
			}

			if (!StringUtils.isEmpty(data.optString("F12")) && data.optInt("F12") < 0) {
				// 入数 ≧ 0を入力して下さい。
				msg.add(mu.getDbMessageObj("EX1047", new String[]{reqNo+"入数 ≧ 0"}));
				return msg;
			}

			if (!StringUtils.isEmpty(data.optString("F13")) && data.optInt("F13") < 0) {
				// 原価 ≧ 0を入力して下さい。
				msg.add(mu.getDbMessageObj("EX1047", new String[]{reqNo+"原価 ≧ 0"}));
				return msg;
			}

			if (!StringUtils.isEmpty(data.optString("F14")) && data.optInt("F14") < 0) {
				// 総売価 ≧ 0を入力して下さい。
				msg.add(mu.getDbMessageObj("EX1047", new String[]{reqNo+"総売価 ≧ 0"}));
				return msg;
			}

			if (!StringUtils.isEmpty(data.optString("F16")) && data.optInt("F16") < 0) {
				// 数量 ≧ 0を入力して下さい。
				msg.add(mu.getDbMessageObj("EX1047", new String[]{reqNo+"数量 ≧ 0"}));
				return msg;
			}

			key = StringUtils.isEmpty(data.optString("F4")) ? "":data.optString("F4");
			if (!StringUtils.isEmpty(key) && !shnCd.contains(key)) {
				// 変数を初期化
				sbSQL	= new StringBuffer();
				iL		= new ItemList();
				dbDatas = new JSONArray();
				sqlWhere	= "";
				paramData	= new ArrayList<String>();

				// 商品存在チェック
				if (StringUtils.isEmpty(data.optString("F4"))) {
					sqlWhere += "SHNCD=null AND ";
				} else {
					sqlWhere += "SHNCD=? AND ";
					paramData.add(data.optString("F4"));
				}

				sbSQL.append("SELECT ");
				sbSQL.append("SHNCD ");	// レコード件数
				sbSQL.append("FROM ");
				sbSQL.append("INAMS.MSTSHN ");
				sbSQL.append("WHERE ");
				sbSQL.append(sqlWhere); // 入力された商品コードで検索
				sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal() + " ");

				dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

				if (dbDatas.size() == 0){
					// マスタに登録のない商品
					msg.add(mu.getDbMessageObj("E11046", new String[]{reqNo}));
					return msg;
				}
				shnCd.add(key);
			}

			// 商品-納入日-発注日-商品区分-別伝区分-入数-原価-売価の重複チェック
			key = StringUtils.isEmpty(data.optString("F4")) ? "" : data.optString("F4");
			key += StringUtils.isEmpty(data.optString("F11")) ? "-" : "-" + data.optString("F11");
			key += StringUtils.isEmpty(data.optString("F10")) ? "-" : "-" + data.optString("F10");
			key += StringUtils.isEmpty(data.optString("F5")) ? "-" : "-" + data.optString("F5");
			key += StringUtils.isEmpty(data.optString("F6")) ? "-" : "-" + data.optString("F6");
			key += StringUtils.isEmpty(data.optString("F12")) ? "-" : "-" + data.optString("F12");
			key += StringUtils.isEmpty(data.optString("F13")) ? "-" : "-" + data.optString("F13");
			key += StringUtils.isEmpty(data.optString("F14")) ? "-" : "-" + data.optString("F14");

			if (!juQju.contains(key)) {
				// 変数を初期化
				sbSQL	= new StringBuffer();
				iL		= new ItemList();
				dbDatas = new JSONArray();
				sqlValues	= "";
				paramData	= new ArrayList<String>();

				// 商品存在チェック
				if (StringUtils.isEmpty(data.optString("F4"))) {
					sqlValues += "null, ";
				} else {
					sqlValues += "CAST(? AS CHARACTER(14)), ";
					paramData.add(data.optString("F4"));
				}

				// 納入日存在チェック
				if (StringUtils.isEmpty(data.optString("F11"))) {
					sqlValues += "null, ";
				} else {
					sqlValues += "CAST(? AS INTEGER), ";
					paramData.add(data.optString("F11"));
				}

				// 発注日存在チェック
				if (StringUtils.isEmpty(data.optString("F10"))) {
					sqlValues += "null, ";
				} else {
					sqlValues += "CAST(? AS INTEGER), ";
					paramData.add(data.optString("F10"));
				}

				// 商品区分存在チェック
				if (StringUtils.isEmpty(data.optString("F5"))) {
					sqlValues += "null, ";
				} else {
					sqlValues += "CAST(? AS SMALLINT), ";
					paramData.add(data.optString("F5"));
				}

				// 別伝区分存在チェック
				if (StringUtils.isEmpty(data.optString("F6"))) {
					sqlValues += "0, ";
				} else {
					sqlValues += "CAST(? AS SMALLINT), ";
					paramData.add(data.optString("F6"));
				}

				// 入数存在チェック
				if (StringUtils.isEmpty(data.optString("F12"))) {
					sqlValues += "null, ";
				} else {
					sqlValues += "CAST(? AS SMALLINT), ";
					paramData.add(data.optString("F12"));
				}

				// 原価存在チェック
				if (StringUtils.isEmpty(data.optString("F13"))) {
					sqlValues += "null, ";
				} else {
					sqlValues += "CAST(? AS DECIMAL(8,2)), ";
					paramData.add(data.optString("F13"));
				}

				// 売価存在チェック
				if (StringUtils.isEmpty(data.optString("F14"))) {
					sqlValues += "null ";
				} else {
					sqlValues += "CAST(? AS INTEGER) ";
					paramData.add(data.optString("F14"));
				}

				sbSQL.append("SELECT ");
				sbSQL.append("T2.SHNCD ");
				sbSQL.append("FROM ");
				sbSQL.append("( ");
				sbSQL.append("SELECT ");
				sbSQL.append("T1.SHNCD ");
				sbSQL.append(",T2.BINKBN ");
				sbSQL.append(",T1.NNDT ");
				sbSQL.append(",T1.HTDT ");
				sbSQL.append(",T1.SHNKBN ");
				sbSQL.append(",T1.BDENKBN ");
				sbSQL.append(",T2.TEIKANKBN ");
				sbSQL.append(",T1.IRISU ");
				sbSQL.append(",T1.GENKAAM ");
				sbSQL.append(",T1.BAIKAAM  ");
				sbSQL.append("FROM ");
				sbSQL.append("(values (  ");
				sbSQL.append(sqlValues);
				sbSQL.append(")) AS T1(  ");
				sbSQL.append("SHNCD ");
				sbSQL.append(",NNDT ");
				sbSQL.append(",HTDT ");
				sbSQL.append(",SHNKBN ");
				sbSQL.append(",BDENKBN ");
				sbSQL.append(",IRISU ");
				sbSQL.append(",GENKAAM ");
				sbSQL.append(",BAIKAAM ");
				sbSQL.append(") LEFT JOIN INAMS.MSTSHN T2 ON T1.SHNCD=T2.SHNCD ");
				sbSQL.append(") T1 ");
				sbSQL.append(",( ");
				sbSQL.append("SELECT DISTINCT ");
				sbSQL.append("T1.SHNCD,T2.BINKBN,T1.NNDT,T1.HTDT,T1.SHNKBN,T1.BDENKBN,T2.TEIKANKBN,T1.IRISU,T1.GENKAAM,T1.BAIKAAM ");
				sbSQL.append("FROM ");

				// 事前打出しじゃない場合
				sqlFrom = "INATK.TOKQJU_SHN ";
				if (!DefineReport.ID_PAGE_JU037.equals(report)) {
					sqlFrom = "INATK.TOKJU_SHN ";
				}

				sbSQL.append(sqlFrom);
				sbSQL.append(" T1 LEFT JOIN INAMS.MSTSHN T2 ON T1.SHNCD=T2.SHNCD ");
				sbSQL.append("WHERE ");
				sbSQL.append("T1.UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal() + " ");
				sbSQL.append(") T2 ");
				sbSQL.append("WHERE ");
				sbSQL.append("T1.SHNCD=T2.SHNCD AND ");
				sbSQL.append("T1.BINKBN=T2.BINKBN AND ");
				sbSQL.append("T1.NNDT=T2.NNDT AND ");
				sbSQL.append("T1.SHNKBN=T2.SHNKBN AND ");
				sbSQL.append("T1.BDENKBN=T2.BDENKBN AND ");
				sbSQL.append("T1.TEIKANKBN=T2.TEIKANKBN AND ");
				sbSQL.append("T1.IRISU=T2.IRISU AND ");
				sbSQL.append("T1.GENKAAM=T2.GENKAAM AND ");
				sbSQL.append("T1.BAIKAAM=T2.BAIKAAM ");

				dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

				if (dbDatas.size() != 0){
					// 催し区分8で使用されている商品は登録不可
					if (!DefineReport.ID_PAGE_JU037.equals(report)) {
						msg.add(mu.getDbMessageObj("E20162", new String[]{reqNo+"催し区分=9で"}));
					} else {
						msg.add(mu.getDbMessageObj("E20162", new String[]{reqNo+"催し区分=8で"}));
					}
					return msg;
				}
				juQju.add(key);
			}

			HashMap<String,String>	moyCdArrMap = new HashMap<String,String>();
			key = StringUtils.isEmpty(data.optString("F4")) ? "":data.optString("F4");
			key += StringUtils.isEmpty(data.optString("F11")) ? "-":"-"+data.optString("F11");
			if (!StringUtils.isEmpty(key) && !shnNndtMap.containsKey(key)) {
				moyCdArrMap = getArrMap(data.optString("F4"),data.optString("F11"),"1");
				shnNndtMap.put(key, createArr(moyCdArrMap,"1"));
			} else {
				moyCdArrMap = getDigitMap(shnNndtMap.get(key),10,"1");
			}

			String tencd = data.optString("F15");

			key = StringUtils.isEmpty(tencd) ? "":tencd;
			if (!StringUtils.isEmpty(key) && !tenCd.contains(key)) {
				// 変数を初期化
				sbSQL	= new StringBuffer();
				iL		= new ItemList();
				dbDatas = new JSONArray();
				sqlWhere	= "";
				paramData	= new ArrayList<String>();

				// 店存在チェック
				if (StringUtils.isEmpty(data.optString("F15"))) {
					sqlWhere += "TENCD=null AND ";
				} else {

					if (data.optInt("F15") > 400) {
						msg.add(mu.getDbMessageObj("E20110", new String[]{reqNo+"店番"}));
						return msg;
					}

					sqlWhere += "TENCD=? AND ";
					paramData.add(data.optString("F15"));
				}

				sbSQL.append("SELECT ");
				sbSQL.append("TENCD ");	// レコード件数
				sbSQL.append("FROM ");
				sbSQL.append("INAMS.MSTTEN ");
				sbSQL.append("WHERE ");
				sbSQL.append(sqlWhere); // 入力された商品コードで検索
				sbSQL.append("MISEUNYOKBN <> '9' AND UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal() + " ");

				dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

				if (dbDatas.size() == 0){
					// 登録不可の店舗
					msg.add(mu.getDbMessageObj("EX1077", new String[]{reqNo}));
					return msg;
				}
				tenCd.add(key);
			}
		}
		return msg;
	}

	public String checkCsvArr(String moyscd, String shncd,String htdt,String nndt,Set<Integer> addTencds, String callpage) {

		// SQL構文
		String	msg			= "";

		// 格納用変数
		StringBuffer		sbSQL	= new StringBuffer();
		ItemList			iL		= new ItemList();
		JSONArray			dbDatas	= new JSONArray();

		tencds = addTencds;

		String sqlcommand = getCommand(moyscd,shncd,htdt,nndt,"","",callpage,new JSONArray());
		if (!StringUtils.isEmpty(sqlcommand)) {
			sbSQL.append(sqlcommand);
			dbDatas = iL.selectJSONArray(sbSQL.toString(), new ArrayList<String>(), Defines.STR_JNDI_DS);

			if (dbDatas.size() != 0){
				msg = dbDatas.getJSONObject(0).optString("VALUE");
			}
		}

		return msg;
	}

	/**
	 * 事前打出し商品INSERT/UPDATE処理
	 *
	 * @param data
	 * @param dataArrayG
	 * @param userInfo
	 */
	public String createSqlJu(JSONObject data, JSONArray dataArrayG, User userInfo){

		// ログインユーザー情報取得
		String userId = userInfo.getId(); // ログインユーザー
		JSONObject getData	= new JSONObject();

		// DB検索用パラメータ
		ArrayList<String>	paramData	= new ArrayList<String>();
		StringBuffer		sbSQL		= new StringBuffer();
		String				sqlWhere	= "";
		String				sqlFrom		= "";
		Object[]			valueData	= new Object[]{};
		String				values		= "";
		ItemList			iL			= new ItemList();
		JSONArray			dbDatas		= new JSONArray();
		ArrayList<String>	prmData		= new ArrayList<String>();
		HashMap<String,String> tenSuryoMap = new HashMap<String,String>();

		// 入力値格納用変数
		String moyskbn		= "";
		String moysstdt		= "";
		String moysrban		= "";
		String tenHtSu_Arr	= "";
		String moyCd_Arr	= "";
		String kanriNo_Arr	= "";
		String dblCnt_Arr	= "";
		int lastTenCd = 1;

		if (data.optString("F1").length() >= 8) {
			moyskbn		= data.optString("F1").substring(0,1);
			moysstdt	= data.optString("F1").substring(1,7);
			moysrban	= data.optString("F1").substring(7);
		}

		// 管理番号の取得
		sbSQL	= new StringBuffer();
		iL		= new ItemList();
		dbDatas = new JSONArray();
		sqlWhere	= "";
		paramData	= new ArrayList<String>();
		getData = new JSONObject();

		// 催し区分
		if (StringUtils.isEmpty(moyskbn)) {
			sqlWhere += "MOYSKBN=null AND ";
		} else {
			sqlWhere += "MOYSKBN=? AND ";
			paramData.add(moyskbn);
		}

		// 催し開始日
		if (StringUtils.isEmpty(moysstdt)) {
			sqlWhere += "MOYSSTDT=null AND ";
		} else {
			sqlWhere += "MOYSSTDT=? AND ";
			paramData.add(moysstdt);
		}

		// 催し連番
		if (StringUtils.isEmpty(moysrban)) {
			sqlWhere += "MOYSRBAN=null ";
		} else {
			sqlWhere += "MOYSRBAN=? ";
			paramData.add(moysrban);
		}

		// 一覧表情報
		sbSQL.append("SELECT ");
		sbSQL.append("MAX(SUMI_KANRINO) AS SUMI_KANRINO ");
		sbSQL.append("FROM INATK.SYSMOYCD ");
		sbSQL.append("WHERE ");
		sbSQL.append(sqlWhere);

		dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

		if (dbDatas.size() > 0) {
			getData	= dbDatas.getJSONObject(0);
			if (!StringUtils.isEmpty(getData.optString("SUMI_KANRINO"))) {
				kanrino = String.valueOf(getData.optInt("SUMI_KANRINO") + 1);
			} else {
				kanrino = "1";
			}
		} else {
			kanrino = "1";
		}

		if (!tenht.equals("3")) {

			int tenHtsu = data.optInt("F15");
			Iterator<Integer> ten = tencds.iterator();

			for (int i = 0; i < tencds.size(); i++) {

				int tencd = ten.next();

				// ランク別発注の場合店ランクを取得
				if (tenht.equals("2")) {

					// 変数を初期化
					sbSQL	= new StringBuffer();
					iL		= new ItemList();
					dbDatas = new JSONArray();
					sqlWhere	= "";
					paramData	= new ArrayList<String>();

					sqlFrom = "INATK.TOKSRYRANK ";

					// 商品コードより部門コードを取得
					if (StringUtils.isEmpty(data.optString("F2"))) {
						sqlWhere += "BMNCD=null AND ";
					} else {
						if (data.optString("F2").length() >= 2) {
							sqlWhere += "BMNCD=? AND ";
							paramData.add(data.optString("F2").substring(0,2));
						} else {
							sqlWhere += "BMNCD=null AND ";
						}
					}

					if (StringUtils.isEmpty(data.optString("F17")) && data.optInt("F17") >= 900) {

						// 催し区分
						if (StringUtils.isEmpty(moyskbn)) {
							sqlWhere += "MOYSKBN=null AND ";
						} else {
							sqlWhere += "MOYSKBN=? AND ";
							paramData.add(moyskbn);
						}

						// 催し開始日
						if (StringUtils.isEmpty(moysstdt)) {
							sqlWhere += "MOYSSTDT=null AND ";
						} else {
							sqlWhere += "MOYSSTDT=? AND ";
							paramData.add(moysstdt);
						}

						// 催し連番
						if (StringUtils.isEmpty(moysrban)) {
							sqlWhere += "MOYSRBAN=null AND ";
						} else {
							sqlWhere += "MOYSRBAN=? AND ";
							paramData.add(moysrban);
						}

						sqlFrom = "INATK.TOKSRYRANKEX ";
					}

					// ランクNo.
					sqlWhere += "SRYPTNNO=? AND ";
					paramData.add(data.optString("F17"));

					sqlWhere += "TENRANK=? ";
					paramData.add(tenRank.get(String.valueOf(tencd)));

					// 一覧表情報
					sbSQL	= new StringBuffer();
					sbSQL.append("SELECT ");
					sbSQL.append("SURYO ");
					sbSQL.append("FROM ");
					sbSQL.append(sqlFrom);
					sbSQL.append("WHERE ");
					sbSQL.append(sqlWhere);

					dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

					if (dbDatas.size() != 0){
						tenHtsu = dbDatas.getJSONObject(0).optInt("SURYO");
					}
				}

				if ((tencd-lastTenCd) != 0) {
					tenHtSu_Arr += String.format("%"+(tencd-lastTenCd)*5+"s","");
				}

				String suryo = String.valueOf(tenHtsu);

				if (!StringUtils.isEmpty(suryo) && Integer.valueOf(suryo) != 0) {
					tenHtSu_Arr += String.format("%05d", Integer.valueOf(suryo));
				} else {
					tenHtSu_Arr += String.format("%05d", 0);
					suryo = "0";
				}

				if (tenSuryoMap.containsKey(String.valueOf(tencd))) {
					tenSuryoMap.replace(String.valueOf(tencd), suryo);
				} else {
					tenSuryoMap.put(String.valueOf(tencd), suryo);
				}

				lastTenCd = (tencd+1);
			}
		} else {

			for (int i = 0; i < dataArrayG.size(); i++) {
				JSONObject dataG = dataArrayG.getJSONObject(i);

				int tencd = dataG.optInt("F1");

				if ((tencd-lastTenCd) != 0) {
					tenHtSu_Arr += String.format("%"+(tencd-lastTenCd)*5+"s","");
				}

				String suryo = "";
				if (dataG.size() > 1) {
					suryo = dataG.getString("F2");
				}

				if (!StringUtils.isEmpty(suryo)) {
					tenHtSu_Arr += String.format("%05d", Integer.valueOf(suryo));
				} else {
					tenHtSu_Arr += String.format("%5s","");
					suryo = "";
				}

				if (tenSuryoMap.containsKey(String.valueOf(tencd))) {
					tenSuryoMap.replace(String.valueOf(tencd), suryo);
				} else {
					tenSuryoMap.put(String.valueOf(tencd), suryo);
				}

				lastTenCd = (tencd+1);
			}
		}

		// 余白を追加
		tenHtSu_Arr = spaceArr(tenHtSu_Arr,5);

		int maxField = 14;		// Fxxの最大値
		for (int k = 1; k <= maxField; k++) {
			String key = "F" + String.valueOf(k);

			if(k == 1){
				values += String.valueOf(0 + 1);
				// 催しコードを追加
				values += ", ?, ?, ?, ?";
				prmData.add(moyskbn);
				prmData.add(moysstdt);
				prmData.add(moysrban);
				prmData.add(kanrino);
			}

			if(! ArrayUtils.contains(new String[]{"F1","F11"}, key)
			){
				String val = data.optString(key);
				if (key.equals("F14")) {
					values += ", ?";
					prmData.add(tenht);

					if (tenht.equals("1")) {
						values += ", ?, ?, null";
						prmData.add(data.optString("F14"));
						prmData.add(data.optString("F15"));
					} else if (tenht.equals("2")) {
						values += ", ?, null, ?";
						prmData.add(data.optString("F16"));
						prmData.add(data.optString("F17"));
					} else {
						values += ", null, null, null";
					}

					values += ", ?";
					prmData.add(tenHtSu_Arr);
				} else if (key.equals("F6") && StringUtils.isEmpty(val)) {
					values += ", ?";
					prmData.add("0");
				} else if (StringUtils.isEmpty(val)){
					values += ", null";
				} else {
					values += ", ?";
					prmData.add(val);
				}
			}

			if(k == maxField){
				valueData = ArrayUtils.add(valueData, "("+values+")");
				values = "";
			}
		}

		sbSQL = new StringBuffer();
		sbSQL.append("MERGE INTO INATK.TOKJU_SHN AS T USING (SELECT ");
		sbSQL.append(" MOYSKBN");												// 催し区分
		sbSQL.append(",MOYSSTDT");												// 催し開始日
		sbSQL.append(",MOYSRBAN");												// 催し連番
		sbSQL.append(",KANRINO");												// 管理番号
		sbSQL.append(",SHNCD");													// 商品コード
		sbSQL.append(",SHNKBN");												// 商品区分
		sbSQL.append(",TSEIKBN");												// 訂正区分
		sbSQL.append(",JUKBN");													// 事前区分
		sbSQL.append(",BDENKBN");												// 別伝区分
		sbSQL.append(",WAPPNKBN");												// ワッペン区分
		sbSQL.append(",IRISU");													// 入数
		sbSQL.append(",GENKAAM");												// 原価
		sbSQL.append(",BAIKAAM");												// 売価
		sbSQL.append(",HTDT");													// 発注日
		sbSQL.append(",NNDT");													// 納入日
		sbSQL.append(",JUTENKAIKBN");											// 展開方法
		sbSQL.append(",RANKNO_ADD");											// 対象ランク
		sbSQL.append(",HTSU");													// 発注数
		sbSQL.append(",SURYOPTN");												// 数量パターン
		sbSQL.append(",TENHTSU_ARR");											// 店発注数配列
		sbSQL.append(", "+DefineReport.ValUpdkbn.NML.getVal()+" AS UPDKBN");	// 更新区分：
		sbSQL.append(", "+DefineReport.Values.SENDFLG_UN.getVal()+" AS SENDFLG");											// 送信区分：
		sbSQL.append(", '"+userId+"' AS OPERATOR ");							// オペレーター：
		sbSQL.append(", current timestamp AS ADDDT ");							// 登録日：
		sbSQL.append(", current timestamp AS UPDDT ");							// 更新日：
		sbSQL.append(" FROM (values "+StringUtils.join(valueData, ",")+") as T1(NUM");
		sbSQL.append(",MOYSKBN");												// F1	: 催し区分
		sbSQL.append(",MOYSSTDT");												// F1	: 催し開始日
		sbSQL.append(",MOYSRBAN");												// F1	: 催し連番
		sbSQL.append(",KANRINO");												// 特殊	: 管理番号
		sbSQL.append(",SHNCD");													// F2	: 商品コード
		sbSQL.append(",SHNKBN");												// F3	: 商品区分
		sbSQL.append(",TSEIKBN");												// F4	: 訂正区分
		sbSQL.append(",JUKBN");													// F5	: 事前区分
		sbSQL.append(",BDENKBN");												// F6	: 別伝区分
		sbSQL.append(",WAPPNKBN");												// F7	: ワッペン区分
		sbSQL.append(",IRISU");													// F8	: 入数
		sbSQL.append(",GENKAAM");												// F9	: 原価
		sbSQL.append(",BAIKAAM");												// F10	: 売価
		sbSQL.append(",HTDT");													// F11	: 発注日
		sbSQL.append(",NNDT");													// F12	: 納入日
		sbSQL.append(",JUTENKAIKBN");											// 特殊	: 展開方法
		sbSQL.append(",RANKNO_ADD");											// F14,16	: 対象ランク
		sbSQL.append(",HTSU");													// F15	: 発注数
		sbSQL.append(",SURYOPTN");												// F17	: 数量パターン
		sbSQL.append(",TENHTSU_ARR");											// 特殊	: 店発注数配列
		sbSQL.append(")) as RE on (");
		sbSQL.append("T.MOYSKBN=RE.MOYSKBN AND ");
		sbSQL.append("T.MOYSSTDT=RE.MOYSSTDT AND ");
		sbSQL.append("T.MOYSRBAN=RE.MOYSRBAN AND ");
		sbSQL.append("T.KANRINO=RE.KANRINO ");
		sbSQL.append(") WHEN MATCHED THEN UPDATE SET ");
		sbSQL.append("SHNCD=RE.SHNCD");
		sbSQL.append(",SHNKBN=RE.SHNKBN");
		sbSQL.append(",TSEIKBN=RE.TSEIKBN");
		sbSQL.append(",JUKBN=RE.JUKBN");
		sbSQL.append(",BDENKBN=RE.BDENKBN");
		sbSQL.append(",WAPPNKBN=RE.WAPPNKBN");
		sbSQL.append(",IRISU=RE.IRISU");
		sbSQL.append(",GENKAAM=RE.GENKAAM");
		sbSQL.append(",BAIKAAM=RE.BAIKAAM");
		sbSQL.append(",HTDT=RE.HTDT");
		sbSQL.append(",NNDT=RE.NNDT");
		sbSQL.append(",JUTENKAIKBN=RE.JUTENKAIKBN");
		sbSQL.append(",RANKNO_ADD=RE.RANKNO_ADD");
		sbSQL.append(",HTSU=RE.HTSU");
		sbSQL.append(",SURYOPTN=RE.SURYOPTN");
		sbSQL.append(",TENHTSU_ARR=RE.TENHTSU_ARR");
		sbSQL.append(",UPDKBN=RE.UPDKBN ");
		sbSQL.append(",SENDFLG=RE.SENDFLG ");
		sbSQL.append(",OPERATOR=RE.OPERATOR ");
		sbSQL.append(",UPDDT=RE.UPDDT");
		sbSQL.append(" WHEN NOT MATCHED THEN INSERT (");
		sbSQL.append(" MOYSKBN");												// 催し区分
		sbSQL.append(",MOYSSTDT");												// 催し開始日
		sbSQL.append(",MOYSRBAN");												// 催し連番
		sbSQL.append(",KANRINO");												// 管理番号
		sbSQL.append(",SHNCD");													// 商品コード
		sbSQL.append(",SHNKBN");												// 商品区分
		sbSQL.append(",TSEIKBN");												// 訂正区分
		sbSQL.append(",JUKBN");													// 事前区分
		sbSQL.append(",BDENKBN");												// 別伝区分
		sbSQL.append(",WAPPNKBN");												// ワッペン区分
		sbSQL.append(",IRISU");													// 入数
		sbSQL.append(",GENKAAM");												// 原価
		sbSQL.append(",BAIKAAM");												// 売価
		sbSQL.append(",HTDT");													// 発注日
		sbSQL.append(",NNDT");													// 納入日
		sbSQL.append(",JUTENKAIKBN");											// 展開方法
		sbSQL.append(",RANKNO_ADD");											// 対象ランク
		sbSQL.append(",HTSU");													// 発注数
		sbSQL.append(",SURYOPTN");												// 数量パターン
		sbSQL.append(",TENHTSU_ARR");											// 店発注数配列
		sbSQL.append(",UPDKBN");
		sbSQL.append(",SENDFLG");
		sbSQL.append(",OPERATOR");
		sbSQL.append(",ADDDT");
		sbSQL.append(",UPDDT");
		sbSQL.append(") VALUES (");
		sbSQL.append(" RE.MOYSKBN");											// 催し区分
		sbSQL.append(",RE.MOYSSTDT");											// 催し開始日
		sbSQL.append(",RE.MOYSRBAN");											// 催し連番
		sbSQL.append(",RE.KANRINO");											// 管理番号
		sbSQL.append(",RE.SHNCD");												// 商品コード
		sbSQL.append(",RE.SHNKBN");												// 商品区分
		sbSQL.append(",RE.TSEIKBN");											// 訂正区分
		sbSQL.append(",RE.JUKBN");												// 事前区分
		sbSQL.append(",RE.BDENKBN");											// 別伝区分
		sbSQL.append(",RE.WAPPNKBN");											// ワッペン区分
		sbSQL.append(",RE.IRISU");												// 入数
		sbSQL.append(",RE.GENKAAM");											// 原価
		sbSQL.append(",RE.BAIKAAM");											// 売価
		sbSQL.append(",RE.HTDT");												// 発注日
		sbSQL.append(",RE.NNDT");												// 納入日
		sbSQL.append(",RE.JUTENKAIKBN");										// 展開方法
		sbSQL.append(",RE.RANKNO_ADD");											// 対象ランク
		sbSQL.append(",RE.HTSU");												// 発注数
		sbSQL.append(",RE.SURYOPTN");											// 数量パターン
		sbSQL.append(",RE.TENHTSU_ARR");										// 店発注数配列
		sbSQL.append(",RE.UPDKBN");
		sbSQL.append(",RE.SENDFLG");
		sbSQL.append(",RE.OPERATOR");
		sbSQL.append(",RE.ADDDT");
		sbSQL.append(",RE.UPDDT");
		sbSQL.append(")");

		if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("事前打出し_商品");

		// 事前打出し_商品納入日用配列作成
		String moyscd = moyskbn + moysstdt + String.format("%3s",Integer.valueOf(moysrban));

		HashMap<String,String> moyCdMap = getArrMap(data.optString("F2"),data.optString("F13"),"1");
		HashMap<String,String> kanriNoMap = getArrMap(data.optString("F2"),data.optString("F13"),"2");
		HashMap<String,String> cblCntMap = getArrMap(data.optString("F2"),data.optString("F13"),"3");

		for(HashMap.Entry<String, String> getKeyVal : tenSuryoMap.entrySet()) {
			String key = getKeyVal.getKey();
			String val = getKeyVal.getValue();

			String getMoysCd = "";
			String getKanriNo = "";
			int flg = 0;

			// 数量ゼロは対象外
			if (StringUtils.isEmpty(val)) {
				if (moyCdMap.containsKey(key)) {
					getMoysCd = moyCdMap.get(key);
				}

				if (kanriNoMap.containsKey(key)) {
					getKanriNo = kanriNoMap.get(key);
				}

				// 同一のものだった場合
				if (moyscd.equals(getMoysCd) && String.valueOf(Integer.valueOf(kanrino)).equals(getKanriNo)) {

					if (moyCdMap.containsKey(key)) {
						moyCdMap.remove(key);
					}

					if (kanriNoMap.containsKey(key)) {
						kanriNoMap.remove(key);
					}

					if (cblCntMap.containsKey(key)) {

						flg =  Integer.valueOf(cblCntMap.get(key));
						if (flg > 0) {
							cblCntMap.replace(key, String.valueOf(flg-1));
						} else {
							cblCntMap.remove(key);
						}
					}
				}
			} else {
				if (moyCdMap.containsKey(key)) {
					getMoysCd = moyCdMap.get(key);
					moyCdMap.replace(key,moyscd);
				} else {
					moyCdMap.put(key,moyscd);
				}

				if (kanriNoMap.containsKey(key)) {
					getKanriNo = kanriNoMap.get(key);
					kanriNoMap.replace(key,String.valueOf(Integer.valueOf(kanrino)));
				} else {
					kanriNoMap.put(key,String.valueOf(Integer.valueOf(kanrino)));
				}

				// 同一のものじゃなかった場合
				if (!moyscd.equals(getMoysCd) || !String.valueOf(Integer.valueOf(kanrino)).equals(getKanriNo)) {
					if (cblCntMap.containsKey(key)) {
						flg = Integer.valueOf(cblCntMap.get(key)) + 1;
						cblCntMap.replace(key,String.valueOf(flg));
					} else {
						cblCntMap.put(key,"1");
					}
				}
			}
		}

		moyCd_Arr	= createArr(moyCdMap,"1");
		kanriNo_Arr	= createArr(kanriNoMap,"2");
		dblCnt_Arr	= createArr(cblCntMap,"3");

		// 事前打出し_商品納入日更新
		values = "1,";
		prmData = new ArrayList<String>();
		valueData = new Object[]{};

		// 商品コード
		if (StringUtils.isEmpty(data.optString("F2"))) {
			values += "null,";
		} else {
			values += "?,";
			prmData.add(data.optString("F2"));
		}

		// 納入日
		if (StringUtils.isEmpty(data.optString("F13"))) {
			values += "null,";
		} else {
			values += "?,";
			prmData.add(data.optString("F13"));
		}

		// 催し配列
		if (StringUtils.isEmpty(moyCd_Arr)) {
			values += "null,";
		} else {
			values += "?,";
			prmData.add(moyCd_Arr);
		}

		// 管理番号配列
		if (StringUtils.isEmpty(kanriNo_Arr)) {
			values += "null,";
		} else {
			values += "?,";
			prmData.add(kanriNo_Arr);
		}

		// 重複件数配列
		if (StringUtils.isEmpty(dblCnt_Arr)) {
			values += "null ";
		} else {
			values += "? ";
			prmData.add(dblCnt_Arr);
		}

		valueData = ArrayUtils.add(valueData, "("+values+")");

		sbSQL = new StringBuffer();
		sbSQL.append("MERGE INTO INATK.TOKJU_SHNNNDT AS T USING (SELECT ");
		sbSQL.append(" SHNCD");													// 商品コード
		sbSQL.append(",NNDT");													// 納入日
		sbSQL.append(",MOYCD_ARR");												// 催し配列
		sbSQL.append(",KANRINO_ARR");											// 管理番号配列
		sbSQL.append(",DBLCNT_ARR");											// 重複件数配列
		sbSQL.append(", '"+userId+"' AS OPERATOR ");							// オペレーター：
		sbSQL.append(", current timestamp AS ADDDT ");							// 登録日：
		sbSQL.append(", current timestamp AS UPDDT ");							// 更新日：
		sbSQL.append(" FROM (values "+StringUtils.join(valueData, ",")+") as T1(NUM");
		sbSQL.append(",SHNCD");													// F2	: 商品コード
		sbSQL.append(",NNDT");													// F13	: 納入日
		sbSQL.append(",MOYCD_ARR");												// 特殊	: 催し配列
		sbSQL.append(",KANRINO_ARR");											// 特殊	: 管理番号配列
		sbSQL.append(",DBLCNT_ARR");											// 特殊	: 重複件数配列
		sbSQL.append(")) as RE on (");
		sbSQL.append("T.SHNCD=RE.SHNCD AND ");
		sbSQL.append("T.NNDT=RE.NNDT ");
		sbSQL.append(") WHEN MATCHED THEN UPDATE SET ");
		sbSQL.append("MOYCD_ARR=RE.MOYCD_ARR");
		sbSQL.append(",KANRINO_ARR=RE.KANRINO_ARR");
		sbSQL.append(",DBLCNT_ARR=RE.DBLCNT_ARR");
		sbSQL.append(",OPERATOR=RE.OPERATOR ");
		sbSQL.append(",UPDDT=RE.UPDDT");
		sbSQL.append(" WHEN NOT MATCHED THEN INSERT (");
		sbSQL.append(" SHNCD");													// 商品コード
		sbSQL.append(",NNDT");													// 納入日
		sbSQL.append(",MOYCD_ARR");												// 催し配列
		sbSQL.append(",KANRINO_ARR");											// 管理番号配列
		sbSQL.append(",DBLCNT_ARR");											// 重複件数配列
		sbSQL.append(",OPERATOR");
		sbSQL.append(",ADDDT");
		sbSQL.append(",UPDDT");
		sbSQL.append(") VALUES (");
		sbSQL.append(" RE.SHNCD");												// 商品コード
		sbSQL.append(",RE.NNDT");												// 納入日
		sbSQL.append(",RE.MOYCD_ARR");											// 催し配列
		sbSQL.append(",RE.KANRINO_ARR");										// 管理番号配列
		sbSQL.append(",RE.DBLCNT_ARR");											// 重複件数配列
		sbSQL.append(",RE.OPERATOR");
		sbSQL.append(",RE.ADDDT");
		sbSQL.append(",RE.UPDDT");
		sbSQL.append(")");

		if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("事前打出し_商品納入日");

		// 催しコード内部管理更新
		values = "1,";
		prmData = new ArrayList<String>();
		valueData = new Object[]{};

		// 催し区分
		if (StringUtils.isEmpty(moyskbn)) {
			values += "null,";
		} else {
			values += "?,";
			prmData.add(moyskbn);
		}

		// 催し開始日
		if (StringUtils.isEmpty(moysstdt)) {
			values += "null,";
		} else {
			values += "?,";
			prmData.add(moysstdt);
		}

		// 催し連番
		if (StringUtils.isEmpty(moysrban)) {
			values += "null,";
		} else {
			values += "?,";
			prmData.add(moysrban);
		}

		// 管理番号
		if (StringUtils.isEmpty(kanrino)) {
			values += "null";
		} else {
			values += "?";
			prmData.add(kanrino);
		}

		valueData = ArrayUtils.add(valueData, "("+values+")");

		sbSQL = new StringBuffer();
		sbSQL.append("MERGE INTO INATK.SYSMOYCD AS T USING (SELECT ");
		sbSQL.append(" MOYSKBN");												// 催し区分
		sbSQL.append(",MOYSSTDT");												// 催し開始日
		sbSQL.append(",MOYSRBAN");												// 催し連番
		sbSQL.append(",SUMI_KANRINO");											// 管理番号
		sbSQL.append(", '"+userId+"' AS OPERATOR ");							// オペレーター：
		sbSQL.append(", current timestamp AS ADDDT ");							// 登録日：
		sbSQL.append(", current timestamp AS UPDDT ");							// 更新日：
		sbSQL.append(" FROM (values "+StringUtils.join(valueData, ",")+") as T1(NUM");
		sbSQL.append(",MOYSKBN");												// 催し区分
		sbSQL.append(",MOYSSTDT");												// 催し開始日
		sbSQL.append(",MOYSRBAN");												// 催し連番
		sbSQL.append(",SUMI_KANRINO");											// 管理番号
		sbSQL.append(")) as RE on (");
		sbSQL.append("T.MOYSKBN=RE.MOYSKBN AND ");
		sbSQL.append("T.MOYSSTDT=RE.MOYSSTDT AND ");
		sbSQL.append("T.MOYSRBAN=RE.MOYSRBAN ");
		sbSQL.append(") WHEN MATCHED THEN UPDATE SET ");
		sbSQL.append("SUMI_KANRINO=RE.SUMI_KANRINO");
		sbSQL.append(",OPERATOR=RE.OPERATOR ");
		sbSQL.append(",UPDDT=RE.UPDDT");
		sbSQL.append(" WHEN NOT MATCHED THEN INSERT (");
		sbSQL.append(" MOYSKBN");												// 催し区分
		sbSQL.append(",MOYSSTDT");												// 催し開始日
		sbSQL.append(",MOYSRBAN");												// 催し連番
		sbSQL.append(",SUMI_KANRINO");											// 管理番号
		sbSQL.append(",OPERATOR");
		sbSQL.append(",ADDDT");
		sbSQL.append(",UPDDT");
		sbSQL.append(") VALUES (");
		sbSQL.append(" RE.MOYSKBN");											// 催し区分
		sbSQL.append(",RE.MOYSSTDT");											// 催し開始日
		sbSQL.append(",RE.MOYSRBAN");											// 催し連番
		sbSQL.append(",RE.SUMI_KANRINO");										// 管理番号
		sbSQL.append(",RE.OPERATOR");
		sbSQL.append(",RE.ADDDT");
		sbSQL.append(",RE.UPDDT");
		sbSQL.append(")");

		if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("催しコード内部管理更新");

		return sbSQL.toString();
	}

	/**
	 * 事前打出し商品INSERT/UPDATE処理
	 *
	 * @param data
	 * @param dataArrayG
	 * @param userInfo
	 */
	public JSONObject createSqlJuCsv(JSONArray dataArray, Set<Integer> addTen, HashMap<String,String> tenSuryoMap, String report, User userInfo){

		JSONObject result = new JSONObject();

		// ログインユーザー情報取得
		String userId = userInfo.getId(); // ログインユーザー

		// DB検索用パラメータ
		StringBuffer		sbSQL		= new StringBuffer();
		String				sqlFrom		= "INATK.TOKJU_SHN";
		Object[]			valueData	= new Object[]{};
		String				values		= "";
		ArrayList<String>	prmData		= new ArrayList<String>();

		// 入力値格納用変数
		String moyskbn		= "";
		String moysstdt		= "";
		String moysrban		= "";
		String moyCd_Arr	= "";
		String kanriNo_Arr	= "";
		String dblCnt_Arr	= "";
		String jutenkaikbn	= "3";		// CSV取込では展開方法=3で固定値とする。

		if (DefineReport.ID_PAGE_JU038.equals(report)) {
			sqlFrom = "INATK.TOKQJU_SHN";
		}

		for (int i = 0; i < dataArray.size(); i++) {

			JSONObject data = dataArray.getJSONObject(i);

			moyskbn		= data.optString("F1");
			moysstdt	= data.optString("F2");
			moysrban	= data.optString("F3");
			kanrino		= data.optString("F17");

			int maxField = 17;		// Fxxの最大値
			for (int k = 1; k <= maxField; k++) {
				String key = "F" + String.valueOf(k);

				if(k == 1){
					values += String.valueOf(0 + 1);
				}

				if(! ArrayUtils.contains(new String[]{"F16"}, key)
				){
					String val = data.optString(key);
					values += ", ?";
					prmData.add(val);
				}

				if(k == maxField){
					valueData = ArrayUtils.add(valueData, "("+values+")");
					values = "";
				}
			}

			sbSQL = new StringBuffer();
			sbSQL.append("MERGE INTO "+sqlFrom+" AS T USING (SELECT ");
			sbSQL.append(" MOYSKBN");												// 催し区分
			sbSQL.append(",MOYSSTDT");												// 催し開始日
			sbSQL.append(",MOYSRBAN");												// 催し連番
			sbSQL.append(",SHNCD");													// 商品コード
			sbSQL.append(",SHNKBN");												// 商品区分
			sbSQL.append(",TSEIKBN");												// 訂正区分
			sbSQL.append(",JUKBN");													// 事前区分
			sbSQL.append(",BDENKBN");												// 別伝区分
			sbSQL.append(",WAPPNKBN");												// ワッペン区分
			sbSQL.append(",HTDT");													// 発注日
			sbSQL.append(",NNDT");													// 納入日
			sbSQL.append(",IRISU");													// 入数
			sbSQL.append(",GENKAAM");												// 原価
			sbSQL.append(",BAIKAAM");												// 売価
			sbSQL.append(",TENHTSU_ARR");											// 店発注数配列
			sbSQL.append(",KANRINO");												// 管理番号
			sbSQL.append(", "+jutenkaikbn+" AS JUTENKAIKBN");						// 展開方法
			sbSQL.append(", "+DefineReport.ValUpdkbn.NML.getVal()+" AS UPDKBN");	// 更新区分：
			sbSQL.append(", "+DefineReport.Values.SENDFLG_UN.getVal()+" AS SENDFLG");											// 送信区分：
			sbSQL.append(", '"+userId+"' AS OPERATOR ");							// オペレーター：
			sbSQL.append(", current timestamp AS ADDDT ");							// 登録日：
			sbSQL.append(", current timestamp AS UPDDT ");							// 更新日：
			sbSQL.append(" FROM (values "+StringUtils.join(valueData, ",")+") as T1(NUM");
			sbSQL.append(",MOYSKBN");												// F1	: 催し区分
			sbSQL.append(",MOYSSTDT");												// F2	: 催し開始日
			sbSQL.append(",MOYSRBAN");												// F3	: 催し連番
			sbSQL.append(",SHNCD");													// F4	: 商品コード
			sbSQL.append(",SHNKBN");												// F5	: 商品区分
			sbSQL.append(",TSEIKBN");												// F6	: 訂正区分
			sbSQL.append(",JUKBN");													// F7	: 事前区分
			sbSQL.append(",BDENKBN");												// F8	: 別伝区分
			sbSQL.append(",WAPPNKBN");												// F9	: ワッペン区分
			sbSQL.append(",HTDT");													// F10	: 発注日
			sbSQL.append(",NNDT");													// F11	: 納入日
			sbSQL.append(",IRISU");													// F12	: 入数
			sbSQL.append(",GENKAAM");												// F13	: 原価
			sbSQL.append(",BAIKAAM");												// F14	: 売価
			sbSQL.append(",TENHTSU_ARR");											// F15	: 店発注数配列
			sbSQL.append(",KANRINO");												// F17	: 管理番号
			sbSQL.append(")) as RE on (");
			sbSQL.append("T.MOYSKBN=RE.MOYSKBN AND ");
			sbSQL.append("T.MOYSSTDT=RE.MOYSSTDT AND ");
			sbSQL.append("T.MOYSRBAN=RE.MOYSRBAN AND ");
			sbSQL.append("T.KANRINO=RE.KANRINO ");
			sbSQL.append(") WHEN MATCHED THEN UPDATE SET ");
			sbSQL.append("SHNCD=RE.SHNCD");
			sbSQL.append(",SHNKBN=RE.SHNKBN");
			sbSQL.append(",TSEIKBN=RE.TSEIKBN");
			sbSQL.append(",JUKBN=RE.JUKBN");
			sbSQL.append(",BDENKBN=RE.BDENKBN");
			sbSQL.append(",WAPPNKBN=RE.WAPPNKBN");
			sbSQL.append(",IRISU=RE.IRISU");
			sbSQL.append(",GENKAAM=RE.GENKAAM");
			sbSQL.append(",BAIKAAM=RE.BAIKAAM");
			sbSQL.append(",HTDT=RE.HTDT");
			sbSQL.append(",NNDT=RE.NNDT");
			sbSQL.append(",JUTENKAIKBN=RE.JUTENKAIKBN");
			sbSQL.append(",TENHTSU_ARR=RE.TENHTSU_ARR");
			sbSQL.append(",UPDKBN=RE.UPDKBN ");
			sbSQL.append(",SENDFLG=RE.SENDFLG ");
			sbSQL.append(",OPERATOR=RE.OPERATOR ");
			sbSQL.append(",UPDDT=RE.UPDDT");
			sbSQL.append(" WHEN NOT MATCHED THEN INSERT (");
			sbSQL.append(" MOYSKBN");												// 催し区分
			sbSQL.append(",MOYSSTDT");												// 催し開始日
			sbSQL.append(",MOYSRBAN");												// 催し連番
			sbSQL.append(",KANRINO");												// 管理番号
			sbSQL.append(",SHNCD");													// 商品コード
			sbSQL.append(",SHNKBN");												// 商品区分
			sbSQL.append(",TSEIKBN");												// 訂正区分
			sbSQL.append(",JUKBN");													// 事前区分
			sbSQL.append(",BDENKBN");												// 別伝区分
			sbSQL.append(",WAPPNKBN");												// ワッペン区分
			sbSQL.append(",IRISU");													// 入数
			sbSQL.append(",GENKAAM");												// 原価
			sbSQL.append(",BAIKAAM");												// 売価
			sbSQL.append(",HTDT");													// 発注日
			sbSQL.append(",NNDT");													// 納入日
			sbSQL.append(",JUTENKAIKBN");											// 展開方法
			sbSQL.append(",TENHTSU_ARR");											// 店発注数配列
			sbSQL.append(",UPDKBN");
			sbSQL.append(",SENDFLG");
			sbSQL.append(",OPERATOR");
			sbSQL.append(",ADDDT");
			sbSQL.append(",UPDDT");
			sbSQL.append(") VALUES (");
			sbSQL.append(" RE.MOYSKBN");											// 催し区分
			sbSQL.append(",RE.MOYSSTDT");											// 催し開始日
			sbSQL.append(",RE.MOYSRBAN");											// 催し連番
			sbSQL.append(",RE.KANRINO");											// 管理番号
			sbSQL.append(",RE.SHNCD");												// 商品コード
			sbSQL.append(",RE.SHNKBN");												// 商品区分
			sbSQL.append(",RE.TSEIKBN");											// 訂正区分
			sbSQL.append(",RE.JUKBN");												// 事前区分
			sbSQL.append(",RE.BDENKBN");											// 別伝区分
			sbSQL.append(",RE.WAPPNKBN");											// ワッペン区分
			sbSQL.append(",RE.IRISU");												// 入数
			sbSQL.append(",RE.GENKAAM");											// 原価
			sbSQL.append(",RE.BAIKAAM");											// 売価
			sbSQL.append(",RE.HTDT");												// 発注日
			sbSQL.append(",RE.NNDT");												// 納入日
			sbSQL.append(",RE.JUTENKAIKBN");										// 展開方法
			sbSQL.append(",RE.TENHTSU_ARR");										// 店発注数配列
			sbSQL.append(",RE.UPDKBN");
			sbSQL.append(",RE.SENDFLG");
			sbSQL.append(",RE.OPERATOR");
			sbSQL.append(",RE.ADDDT");
			sbSQL.append(",RE.UPDDT");
			sbSQL.append(")");

			if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

			sqlList.add(sbSQL.toString());
			prmList.add(prmData);
			lblList.add("事前打出し_商品");

			// 事前打出し_商品納入日用配列作成
			String moyscd = moyskbn + moysstdt + String.format("%3s",Integer.valueOf(moysrban));

			HashMap<String,String> moyCdMap = getArrMap(data.optString("F4"),data.optString("F11"),"1");
			HashMap<String,String> kanriNoMap = getArrMap(data.optString("F4"),data.optString("F1"),"2");
			HashMap<String,String> cblCntMap = getArrMap(data.optString("F4"),data.optString("F11"),"3");

			for(HashMap.Entry<String, String> getKeyVal : tenSuryoMap.entrySet()) {
				String key = getKeyVal.getKey();
				String val = getKeyVal.getValue();

				String getMoysCd = "";
				String getKanriNo = "";
				int flg = 0;

				// 数量ゼロは対象外
				if (StringUtils.isEmpty(val)) {
					if (moyCdMap.containsKey(key)) {
						getMoysCd = moyCdMap.get(key);
					}

					if (kanriNoMap.containsKey(key)) {
						getKanriNo = kanriNoMap.get(key);
					}

					// 同一のものだった場合
					if (moyscd.equals(getMoysCd) && String.valueOf(Integer.valueOf(kanrino)).equals(getKanriNo)) {

						if (moyCdMap.containsKey(key)) {
							moyCdMap.remove(key);
						}

						if (kanriNoMap.containsKey(key)) {
							kanriNoMap.remove(key);
						}

						if (cblCntMap.containsKey(key)) {

							flg =  Integer.valueOf(cblCntMap.get(key));
							if (flg > 0) {
								cblCntMap.replace(key, String.valueOf(flg-1));
							} else {
								cblCntMap.remove(key);
							}
						}
					}
				} else {
					if (moyCdMap.containsKey(key)) {
						getMoysCd = moyCdMap.get(key);
						moyCdMap.replace(key,moyscd);
					} else {
						moyCdMap.put(key,moyscd);
					}

					if (kanriNoMap.containsKey(key)) {
						getKanriNo = kanriNoMap.get(key);
						kanriNoMap.replace(key,String.valueOf(Integer.valueOf(kanrino)));
					} else {
						kanriNoMap.put(key,String.valueOf(Integer.valueOf(kanrino)));
					}

					// 同一のものじゃなかった場合
					if (!moyscd.equals(getMoysCd) || !String.valueOf(Integer.valueOf(kanrino)).equals(getKanriNo)) {
						if (cblCntMap.containsKey(key)) {
							flg = Integer.valueOf(cblCntMap.get(key)) + 1;
							cblCntMap.replace(key,String.valueOf(flg));
						} else {
							cblCntMap.put(key,"1");
						}
					}
				}
			}

			moyCd_Arr	= createArr(moyCdMap,"1");
			kanriNo_Arr	= createArr(kanriNoMap,"2");
			dblCnt_Arr	= createArr(cblCntMap,"3");

			// 事前打出し_商品納入日更新
			values = "1,";
			prmData = new ArrayList<String>();
			valueData = new Object[]{};

			// 商品コード
			if (StringUtils.isEmpty(data.optString("F4"))) {
				values += "null,";
			} else {
				values += "?,";
				prmData.add(data.optString("F4"));
			}

			// 納入日
			if (StringUtils.isEmpty(data.optString("F11"))) {
				values += "null,";
			} else {
				values += "?,";
				prmData.add(data.optString("F11"));
			}

			// 催し配列
			if (StringUtils.isEmpty(moyCd_Arr)) {
				values += "null,";
			} else {
				values += "?,";
				prmData.add(moyCd_Arr);
			}

			// 管理番号配列
			if (StringUtils.isEmpty(kanriNo_Arr)) {
				values += "null,";
			} else {
				values += "?,";
				prmData.add(kanriNo_Arr);
			}

			// 重複件数配列
			if (StringUtils.isEmpty(dblCnt_Arr)) {
				values += "null ";
			} else {
				values += "? ";
				prmData.add(dblCnt_Arr);
			}

			valueData = ArrayUtils.add(valueData, "("+values+")");

			sbSQL = new StringBuffer();
			sbSQL.append("MERGE INTO INATK.TOKJU_SHNNNDT AS T USING (SELECT ");
			sbSQL.append(" SHNCD");													// 商品コード
			sbSQL.append(",NNDT");													// 納入日
			sbSQL.append(",MOYCD_ARR");												// 催し配列
			sbSQL.append(",KANRINO_ARR");											// 管理番号配列
			sbSQL.append(",DBLCNT_ARR");											// 重複件数配列
			sbSQL.append(", '"+userId+"' AS OPERATOR ");							// オペレーター：
			sbSQL.append(", current timestamp AS ADDDT ");							// 登録日：
			sbSQL.append(", current timestamp AS UPDDT ");							// 更新日：
			sbSQL.append(" FROM (values "+StringUtils.join(valueData, ",")+") as T1(NUM");
			sbSQL.append(",SHNCD");													// F2	: 商品コード
			sbSQL.append(",NNDT");													// F13	: 納入日
			sbSQL.append(",MOYCD_ARR");												// 特殊	: 催し配列
			sbSQL.append(",KANRINO_ARR");											// 特殊	: 管理番号配列
			sbSQL.append(",DBLCNT_ARR");											// 特殊	: 重複件数配列
			sbSQL.append(")) as RE on (");
			sbSQL.append("T.SHNCD=RE.SHNCD AND ");
			sbSQL.append("T.NNDT=RE.NNDT ");
			sbSQL.append(") WHEN MATCHED THEN UPDATE SET ");
			sbSQL.append("MOYCD_ARR=RE.MOYCD_ARR");
			sbSQL.append(",KANRINO_ARR=RE.KANRINO_ARR");
			sbSQL.append(",DBLCNT_ARR=RE.DBLCNT_ARR");
			sbSQL.append(",OPERATOR=RE.OPERATOR ");
			sbSQL.append(",UPDDT=RE.UPDDT");
			sbSQL.append(" WHEN NOT MATCHED THEN INSERT (");
			sbSQL.append(" SHNCD");													// 商品コード
			sbSQL.append(",NNDT");													// 納入日
			sbSQL.append(",MOYCD_ARR");												// 催し配列
			sbSQL.append(",KANRINO_ARR");											// 管理番号配列
			sbSQL.append(",DBLCNT_ARR");											// 重複件数配列
			sbSQL.append(",OPERATOR");
			sbSQL.append(",ADDDT");
			sbSQL.append(",UPDDT");
			sbSQL.append(") VALUES (");
			sbSQL.append(" RE.SHNCD");												// 商品コード
			sbSQL.append(",RE.NNDT");												// 納入日
			sbSQL.append(",RE.MOYCD_ARR");											// 催し配列
			sbSQL.append(",RE.KANRINO_ARR");										// 管理番号配列
			sbSQL.append(",RE.DBLCNT_ARR");											// 重複件数配列
			sbSQL.append(",RE.OPERATOR");
			sbSQL.append(",RE.ADDDT");
			sbSQL.append(",RE.UPDDT");
			sbSQL.append(")");

			if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

			sqlList.add(sbSQL.toString());
			prmList.add(prmData);
			lblList.add("事前打出し_商品納入日");
		}
		return result;
	}

	/**
	 * 配列をkye,valueの形で返却(key:店、value:催しコード)
	 * @param shnCd		商品コード
	 * @param nnDt		納入日
	 * @param getFlg	1:催しコード配列 2:管理番号配列 3:重複件数配列
	 * @return
	 */
	public HashMap<String,String> getArrMap(String shnCd, String nnDt, String getFlg){

		// 格納用変数
		StringBuffer	sbSQL	= new StringBuffer();
		ItemList		iL		= new ItemList();
		JSONArray		dbDatas	= new JSONArray();

		// DB検索用パラメータ
		String sqlWhere	= "";
		String sqlFrom	= "INATK.TOKJU_SHNNNDT ";	// 事前打出し_商品納入日
		String sqlSelect= "MOYCD_ARR AS ARR ";
		ArrayList<String> paramData = new ArrayList<String>();

		HashMap<String,String> arrMap = new HashMap<String,String>();
		int digit = 10;

		// 商品コード
		if (StringUtils.isEmpty(shnCd)) {
			sqlWhere += "SHNCD=null AND ";
		} else {
			sqlWhere += "SHNCD=? AND ";
			paramData.add(shnCd);
		}

		// 販売開始日～終了日
		if (StringUtils.isEmpty(nnDt)) {
			sqlWhere += "NNDT=null ";
		} else {
			sqlWhere += "NNDT=? ";
			paramData.add(nnDt);
		}

		if (getFlg.equals("2")) {
			sqlSelect = "KANRINO_ARR AS ARR ";
			digit = 4;
		} else if (getFlg.equals("3")) {
			sqlSelect = "DBLCNT_ARR AS ARR ";
			digit = 2;
		}

		sbSQL.append("SELECT ");
		sbSQL.append(sqlSelect);	// 配列
		sbSQL.append("FROM ");
		sbSQL.append(sqlFrom);
		sbSQL.append("WHERE ");
		sbSQL.append(sqlWhere);

		dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

		// データが存在する場合、配列の展開を実施
		if (dbDatas.size() != 0){
			arrMap = getDigitMap(dbDatas.getJSONObject(0).optString("ARR"),digit,getFlg);
		}
		return arrMap;
	}

	/**
	 * 配列をkye,valueの形で返却(key:店、value:催しコード)
	 * @param shnCd		商品コード
	 * @param nnDt		納入日
	 * @param getFlg	4:発注数量配列(事前打出し) 5:発注数量配列(店舗アンケート付き)
	 * @return
	 */
	public HashMap<String,String> getHtsuArrMap(String moysCd, String kanrino, String getFlg){

		// 格納用変数
		StringBuffer	sbSQL	= new StringBuffer();
		ItemList		iL		= new ItemList();
		JSONArray		dbDatas	= new JSONArray();

		// DB検索用パラメータ
		String sqlWhere	= "";
		String sqlFrom	= "INATK.TOKJU_SHN ";	// 事前打出し_商品納入日
		ArrayList<String> paramData = new ArrayList<String>();

		HashMap<String,String> arrMap = new HashMap<String,String>();

		String moyskbn	= "";	// 催し区分
		String moysstdt	= "";	// 催し開始日
		String moysrban	= "";	// 催し連番

		if (!StringUtils.isEmpty(moysCd) && moysCd.length() >= 8) {
			moyskbn		= moysCd.substring(0,1);
			moysstdt	= moysCd.substring(1,7);
			moysrban	= moysCd.substring(7);
		}

		if (getFlg.equals("5")) {
			sqlFrom	= "INATK.TOKQJU_SHN ";	// 店舗アンケート付き送り付_商品
		}

		// 催し区分
		if (StringUtils.isEmpty(moyskbn)) {
			sqlWhere += "MOYSKBN=null AND ";
		} else {
			sqlWhere += "MOYSKBN=? AND ";
			paramData.add(moyskbn);
		}

		// 催し開始日
		if (StringUtils.isEmpty(moysstdt)) {
			sqlWhere += "MOYSSTDT=null AND ";
		} else {
			sqlWhere += "MOYSSTDT=? AND ";
			paramData.add(moysstdt);
		}

		// 催し連番
		if (StringUtils.isEmpty(moysrban)) {
			sqlWhere += "MOYSRBAN=null AND ";
		} else {
			sqlWhere += "MOYSRBAN=? AND ";
			paramData.add(moysrban);
		}

		// 管理番号
		if (StringUtils.isEmpty(kanrino)) {
			sqlWhere += "KANRINO=null AND ";
		} else {
			sqlWhere += "KANRINO=? AND ";
			paramData.add(kanrino);
		}

		sbSQL.append("SELECT ");
		sbSQL.append("TENHTSU_ARR AS ARR ");	// レコード件数
		sbSQL.append("FROM ");
		sbSQL.append(sqlFrom);
		sbSQL.append("WHERE ");
		sbSQL.append(sqlWhere); // 入力された商品コードで検索
		sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal() + " ");

		dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

		// データが存在する場合、配列の展開を実施
		if (dbDatas.size() != 0){
			arrMap = getDigitMap(dbDatas.getJSONObject(0).optString("ARR"),5,getFlg);
		}
		return arrMap;
	}


	public HashMap<String,String> getDigitMap(String arr, int digit, String getFlg){

		int st = 0;
		int ed = 0;
		HashMap<String,String> arrMap = new HashMap<String,String>();

		// 配列をdigitで指定された桁ずつで区切りkey、valueを保持(key=店舗、value=取得結果)
		for (int i = 0; i < (arr.length() / digit); i++) {

			ed += digit;

			String val = arr.substring(st,ed);

			// valueがスペースのものは登録しない
			if (!StringUtils.isEmpty(val.trim())) {
				if (!getFlg.equals("1")) {
					arrMap.put(String.valueOf(i+1),String.valueOf(Integer.valueOf(val.trim())));
				} else {
					arrMap.put(String.valueOf(i+1),val);
				}
			}

			st += digit;
		}

		return arrMap;
	}

	public String createArr(HashMap<String,String> arrMap,String getFlg) {

		int space = 10;

		if (getFlg.equals("2")) {
			space = 4;
		} else if (getFlg.equals("3")) {
			space = 2;
		}

		String strArr = "";
		int tenCd = 1;
		int cnt = 0;

		while (arrMap.size() != cnt) {
			if (arrMap.containsKey(String.valueOf(tenCd))) {
				cnt++;
				strArr += String.format("%"+space+"s",arrMap.get(String.valueOf(tenCd)));
			} else {
				strArr += String.format("%"+space+"s", "");
			}
			tenCd++;
		}

		return spaceArr(strArr,space);
	}

	public String spaceArr(String arr, int digit) {
		int len = (digit*400) - arr.length();

		if (len==0) {
			return arr;
		}

		return arr += String.format("%"+len+"s","");
	}

	/** チェック用事前打出し商品(アン付含む) */
	public enum TOKJU_CKLayout implements MSTLayout{

		/** 催し区分 */
		MOYSKBN(1,"MOYSKBN","SMALLINT","催し区分"),
		/** 催し開始日 */
		MOYSSTDT(2,"MOYSSTDT","INTEGER","催し開始日"),
		/** 催し連番 */
		MOYSRBAN(3,"MOYSRBAN","SMALLINT","催し連番"),
		/** 商品コード */
		SHNCD(4,"SHNCD","CHARACTER(14)","商品コード"),
		/** 商品区分 */
		SHNKBN(5,"SHNKBN","SMALLINT","商品区分"),
		/** 訂正区分 */
		TSEIKBN(6,"TSEIKBN","SMALLINT","訂正区分"),
		/** 事前区分 */
		JUKBN(7,"JUKBN","SMALLINT","事前区分"),
		/** 別伝区分 */
		BDENKBN(8,"BDENKBN","SMALLINT","別伝区分"),
		/** ワッペン区分 */
		WAPPNKBN(9,"WAPPNKBN","SMALLINT","ワッペン区分"),
		/** 発注日 */
		HTDT(10,"HTDT","INTEGER","発注日"),
		/** 納入日 */
		NNDT(11,"NNDT","INTEGER","納入日"),
		/** 入数 */
		IRISU(12,"IRISU","SMALLINT","入数"),
		/** 原価 */
		GENKAAM(13,"GENKAAM","DECIMAL(8,2)","原価"),
		/** 売価 */
		BAIKAAM(14,"BAIKAAM","INTEGER","総売価"),
		/** 店番 */
		TENHTSU_ARR(15,"TENHTSU_ARR","SMALLINT","店番"),
		/** 数量 */
		SURYO(16,"SURYO","SMALLINT","数量");

		private final Integer no;
		private final String col;
		private final String typ;
		private final String txt;
		/** 初期化 */
		private TOKJU_CKLayout(Integer no, String col, String typ, String txt) {
			this.no = no;
			this.col = col;
			this.typ = typ;
			this.txt = txt;
		}
		/** @return col 列番号 */
		public Integer getNo() { return no; }
		/** @return tbl 列名 */
		public String getCol() { return col; }
		/** @return tbl 列型 */
		public String getTyp() { return typ; }
		/** @return txt 論理名 */
		public String getTxt() { return txt; }
		/** @return col Id */
		public String getId() { return "F" + Integer.toString(no); }
		/** @return datatype データ型のみ */
		public DataType getDataType() {
			if(typ.indexOf("INT") != -1){ return DefineReport.DataType.INTEGER;}
			if(typ.indexOf("DEC") != -1){	return DefineReport.DataType.DECIMAL;}
			if(typ.indexOf("DATE")!= -1){return DefineReport.DataType.DATE;}
			return DefineReport.DataType.TEXT;
		}
		/** @return boolean */
		public boolean isText() {
			return getDataType() == DefineReport.DataType.TEXT;
		}
		/** @return digit 桁数 */
		public int[] getDigit() {
			int digit1 = 0, digit2 = 0;
 			if(typ.indexOf(",") > 0){
				String[] sDigit = typ.substring(typ.indexOf("(")+1, typ.indexOf(")")).split(",");
				digit1 = NumberUtils.toInt(sDigit[0]);
				digit2 = NumberUtils.toInt(sDigit[1]);
 			}else if(typ.indexOf("(") > 0){
 				digit1 = NumberUtils.toInt(typ.substring(typ.indexOf("(")+1, typ.indexOf(")")));
			}else if(StringUtils.equals(typ, JDBCType.SMALLINT.getName())){
				digit1 = dbNumericTypeInfo.SMALLINT.getDigit();
			}else if(StringUtils.equals(typ, JDBCType.INTEGER.getName())){
				digit1 = dbNumericTypeInfo.INTEGER.getDigit();
			}
			return new int[]{digit1, digit2};
		}
	}

	/** 事前打出し商品(アン付含む) */
	public enum TOKJULayout implements MSTLayout{

		/** 催し区分 */
		MOYSKBN(1,"MOYSKBN","SMALLINT"),
		/** 催し開始日 */
		MOYSSTDT(2,"MOYSSTDT","INTEGER"),
		/** 催し連番 */
		MOYSRBAN(3,"MOYSRBAN","SMALLINT"),
		/** 商品コード */
		SHNCD(4,"SHNCD","CHARACTER(14)"),
		/** 商品区分 */
		SHNKBN(5,"SHNKBN","SMALLINT"),
		/** 訂正区分 */
		TSEIKBN(6,"TSEIKBN","SMALLINT"),
		/** 事前区分 */
		JUKBN(7,"JUKBN","SMALLINT"),
		/** 別伝区分 */
		BDENKBN(8,"BDENKBN","SMALLINT"),
		/** ワッペン区分 */
		WAPPNKBN(9,"WAPPNKBN","SMALLINT"),
		/** 発注日 */
		HTDT(10,"HTDT","INTEGER"),
		/** 納入日 */
		NNDT(11,"NNDT","INTEGER"),
		/** 入数 */
		IRISU(12,"IRISU","SMALLINT"),
		/** 原価 */
		GENKAAM(13,"GENKAAM","DECIMAL(8,2)"),
		/** 売価 */
		BAIKAAM(14,"BAIKAAM","INTEGER"),
		/** 店発注数配列 */
		TENHTSU_ARR(15,"TENHTSU_ARR","VARCHAR(2000)"),
		/** 行番号 */
		GYONO(16,"GYONO","INTEGER"),
		/** 管理番号 */
		KANRINO(17,"KANRINO","SMALLINT");

		private final Integer no;
		private final String col;
		private final String typ;
		/** 初期化 */
		private TOKJULayout(Integer no, String col, String typ) {
			this.no = no;
			this.col = col;
			this.typ = typ;
		}
		/** @return col 列番号 */
		public Integer getNo() { return no; }
		/** @return tbl 列名 */
		public String getCol() { return col; }
		/** @return tbl 列型 */
		public String getTyp() { return typ; }
		/** @return col Id */
		public String getId() { return "F" + Integer.toString(no); }
		/** @return datatype データ型のみ */
		public DataType getDataType() {
			if(typ.indexOf("INT") != -1){ return DefineReport.DataType.INTEGER;}
			if(typ.indexOf("DEC") != -1){	return DefineReport.DataType.DECIMAL;}
			if(typ.indexOf("DATE")!= -1){return DefineReport.DataType.DATE;}
			return DefineReport.DataType.TEXT;
		}
		/** @return boolean */
		public boolean isText() {
			return getDataType() == DefineReport.DataType.TEXT;
		}
		/** @return digit 桁数 */
		public int[] getDigit() {
			int digit1 = 0, digit2 = 0;
 			if(typ.indexOf(",") > 0){
				String[] sDigit = typ.substring(typ.indexOf("(")+1, typ.indexOf(")")).split(",");
				digit1 = NumberUtils.toInt(sDigit[0]);
				digit2 = NumberUtils.toInt(sDigit[1]);
 			}else if(typ.indexOf("(") > 0){
 				digit1 = NumberUtils.toInt(typ.substring(typ.indexOf("(")+1, typ.indexOf(")")));
			}else if(StringUtils.equals(typ, JDBCType.SMALLINT.getName())){
				digit1 = dbNumericTypeInfo.SMALLINT.getDigit();
			}else if(StringUtils.equals(typ, JDBCType.INTEGER.getName())){
				digit1 = dbNumericTypeInfo.INTEGER.getDigit();
			}
			return new int[]{digit1, digit2};
		}
	}
}
