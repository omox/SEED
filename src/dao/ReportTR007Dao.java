package dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import authentication.bean.User;
import authentication.defines.Consts;
import common.DefineReport;
import common.Defines;
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
public class ReportTR007Dao extends ItemDao {

	/** SQLリスト保持用変数 */
	ArrayList<String> sqlList = new ArrayList<String>();
	/** SQLのパラメータリスト保持用変数 */
	ArrayList<ArrayList<String>> prmList = new ArrayList<ArrayList<String>>();
	/** SQLログ用のラベルリスト保持用変数 */
	ArrayList<String> lblList = new ArrayList<String>();

	/**
	 * インスタンスを生成します。
	 * @param source
	 */
	public ReportTR007Dao(String JNDIname) {
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

	public JSONObject update(HttpServletRequest request,HttpSession session,  HashMap<String, String> map, User userInfo) {
		String sysdate = (String)request.getSession().getAttribute(Consts.STR_SES_LOGINDT);

		JSONObject option = new JSONObject();

		JSONArray msgList = this.check(map, userInfo, sysdate);

		if(msgList.size() > 0){
			option.put(MsgKey.E.getKey(), msgList);
			return option;
		}

		// 更新処理
		try {
			option = this.updateData(map, userInfo, sysdate);
		} catch (Exception e) {
			e.printStackTrace();
			option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
		}
		return option;
	}

	/**
	 * チェック処理
	 *
	 * @throws Exception
	 */
	public JSONArray check(HashMap<String, String> map, User userInfo, String sysdate) {

		// パラメータ確認
		JSONArray	dataArrayT	= JSONArray.fromObject(map.get("DATA_HATSTR"));	// 正規定量_店別数量(次週も含む)

		// 格納用変数
		JSONArray			msg		= new JSONArray();

		// チェック処理
		if(dataArrayT.size() == 0 || dataArrayT.getJSONObject(0).isEmpty()){
			msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
			return msg;
		}
		return msg;
	}

	/**
	 * チェック処理
	 *
	 * @throws Exception
	 */
	public JSONArray checkDel(HashMap<String, String> map, User userInfo, String sysdate) {

		// 削除データ検索用コード
		JSONArray	dataArray	= JSONArray.fromObject(map.get("DATA"));	// 対象情報（主要な更新情報）
		String		hstengpcd	= map.get("HSTENGPCD");						// 配送店グループコード

		// 格納用変数
		StringBuffer		sbSQL	= new StringBuffer();
		JSONArray			msg		= new JSONArray();
		ItemList			iL		= new ItemList();
		JSONArray			dbDatas = new JSONArray();
		MessageUtility		mu		= new MessageUtility();

		// チェック処理
		// 対象件数チェック
		if(dataArray.size() == 0 || dataArray.getJSONObject(0).isEmpty()){
			msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
			return msg;
		}

		if(hstengpcd.isEmpty() || hstengpcd == null){
			msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
			return msg;
		}

		// 基本登録情報
		JSONObject data = dataArray.getJSONObject(0);

		// DB検索用パラメータ
		String sqlWhere = "";
		ArrayList<String> paramData = new ArrayList<String>();

		if(StringUtils.isEmpty(data.optString("F1"))){
			sqlWhere += "HSGPCD=null AND ";
		}else{
			sqlWhere += "HSGPCD=? AND ";
			paramData.add(data.optString("F1"));
		}

		if(StringUtils.isEmpty(hstengpcd)){
			sqlWhere += "TENGPCD=null";
		}else{
			sqlWhere += "TENGPCD=?";
			paramData.add(hstengpcd);
		}

		// エリア別配送パターンテーブルにレコードが存在する場合エラー
		sbSQL.append("SELECT ");
		sbSQL.append("HSGPCD ");	// レコード件数
		sbSQL.append("FROM ");
		sbSQL.append("INAMS.MSTAREAHSPTN ");
		sbSQL.append("WHERE ");
		sbSQL.append(sqlWhere);

		dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

		if (dbDatas.size() >= 1){
			// エリア別配送パターンに存在している
			msg.add(mu.getDbMessageObj("E11036", new String[]{}));
			return msg;
		}

		return msg;
	}

	/**
	 * 更新処理実行
	 * @return
	 *
	 * @throws Exception
	 */
	private JSONObject updateData(HashMap<String, String> map, User userInfo, String sysdate) throws Exception {
		// パラメータ確認
		JSONObject	option		= new JSONObject();

		// 排他チェック用
		JSONArray			msg			= new JSONArray();

		// 正規定量_店別数量(次週を含む)UPDATE/DELETE処理
		createSqlHatTr_CSV(map,userInfo);

		// 排他チェック実行
		if(!checkExclusion(map, map.get("HDN_UPDDT"))){
 			msg.add(MessageUtility.getDbMessageExclusion(FieldType.DEFAULT, new String[]{}));
 			option.put(MsgKey.E.getKey(), msg);
			return option;
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
				option.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00001.getVal()));
			}
		}else{
			option.put(MsgKey.E.getKey(), getMessage());
		}
		return option;
	}

	/**
	 * 配送グループマスタ、配送店グループマスタDELETE(倫理削除)処理
	 *
	 * @param data
	 * @param map
	 * @param userInfo
	 */
	public String createDelSqlHs(JSONObject data, HashMap<String, String> map, User userInfo){

		// 削除データ検索用コード
		String	hstengpcd	= map.get("HSTENGPCD");	// 配送店グループコード

		// 格納用変数
		StringBuffer	sbSQL	= new StringBuffer();
		ItemList		iL		= new ItemList();
		JSONArray		dbDatas = new JSONArray();

		// ログインユーザー情報取得
		String userId	= userInfo.getId(); // ログインユーザー

		// DB検索用パラメータ
		String sqlWhere = "";
		ArrayList<String> paramData = new ArrayList<String>();

		if(StringUtils.isEmpty(data.optString("F1"))){
			sqlWhere += "null";
		}else{
			sqlWhere += "?";
			paramData.add(data.optString("F1"));
		}

		// 配送店グループマスタの件数チェック
		sbSQL = new StringBuffer();
		sbSQL.append("SELECT ");
		sbSQL.append(" HSGPCD ");
		sbSQL.append("FROM ");
		sbSQL.append("INAMS.MSTHSTENGP ");
		sbSQL.append("WHERE ");
		sbSQL.append("HSGPCD=" + sqlWhere + " AND ");
		sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal());

		dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

		sbSQL = new StringBuffer();
		sbSQL.append("UPDATE ");
		sbSQL.append("INAMS.MSTHSGP ");
		sbSQL.append("SET ");
		// 1件のみの場合は対象の配送グループを論理削除
		if (dbDatas.size() == 1){
			sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.DEL.getVal() + ",");
		}
		sbSQL.append("OPERATOR='" + userId + "'");
		sbSQL.append(",UPDDT=current timestamp ");
		sbSQL.append("WHERE ");
		sbSQL.append("HSGPCD = " + sqlWhere);

		if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(paramData);
		lblList.add("配送グループマスタ");

		sqlWhere	= "";
		paramData	= new ArrayList<String>();

		if(StringUtils.isEmpty(data.optString("F1"))){
			sqlWhere += "HSGPCD=null AND ";
		}else{
			sqlWhere += "HSGPCD=? AND ";
			paramData.add(data.optString("F1"));
		}

		if(StringUtils.isEmpty(hstengpcd)){
			sqlWhere += "TENGPCD=null";
		}else{
			sqlWhere += "TENGPCD=?";
			paramData.add(hstengpcd);
		}

		sbSQL = new StringBuffer();
		sbSQL.append("UPDATE ");
		sbSQL.append("INAMS.MSTHSTENGP ");
		sbSQL.append("SET ");
		sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.DEL.getVal());
		sbSQL.append(",OPERATOR='" + userId + "'");
		sbSQL.append(",UPDDT=current timestamp ");
		sbSQL.append("WHERE ");
		sbSQL.append(sqlWhere);

		if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(paramData);
		lblList.add("配送店グループマスタ");

		sbSQL = new StringBuffer();
		sbSQL.append("UPDATE ");
		sbSQL.append("INAMS.MSTHSGPTEN ");
		sbSQL.append("SET ");
		sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.DEL.getVal());
		sbSQL.append(",OPERATOR='" + userId + "'");
		sbSQL.append(",UPDDT=current timestamp ");
		sbSQL.append("WHERE ");
		sbSQL.append(sqlWhere);

		if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(paramData);
		lblList.add("配送グループ店マスタ");

		return sbSQL.toString();
	}

	/**
	 * 店別数量_CSV INSERT/UPDATE処理
	 *
	 * @param data
	 * @param map
	 * @param userInfo
	 */
	public String createSqlHatTr_CSV(HashMap<String, String> map, User userInfo){

		StringBuffer	sbSQL		= new StringBuffer();
		JSONArray		dataArrayT	= JSONArray.fromObject(map.get("DATA_HATSTR"));	// 正規定量_店別数量(次週も含む)
		String			tenFlg		= map.get("TENFLG");							// 店番が指定されているかどうか

		ArrayList<String>	paramData	= new ArrayList<String>();
		String				sqlWhere	= "";

		// ログインユーザー情報取得
		String userId	= userInfo.getId();							// ログインユーザー

		ArrayList<String> prmData = new ArrayList<String>();
		Object[] valueData = new Object[]{};
		String values = "";

		// 親論理削除時のチェック用マップ
		Map<String,String> shn = new HashMap<String,String>();

		int maxField = 12;		// Fxxの最大値
		int len = dataArrayT.size();
		for (int i = 0; i < len; i++) {
			JSONObject dataT = dataArrayT.getJSONObject(i);
			String updKbn	 = "0";

			if (!StringUtils.isEmpty(dataT.optString("F1"))) {
				updKbn = dataT.optString("F1");			// 更新区分
			}

			String[] delKey = new String[]{"F1"};		// パラメータの取り込みを行わないキーを指定する。

			if(! dataT.isEmpty()){
				for (int k = 1; k <= maxField; k++) {
					String key = "F" + String.valueOf(k);

					if(k == 1){
						values += String.valueOf(i + 1);
					}

					if(! ArrayUtils.contains(delKey, key)){
						String val = dataT.optString(key);
						if(StringUtils.isEmpty(val)){
							values += ", null";
						}else{
							values += ", ?";
							prmData.add(val);
						}
					}

					if(k == maxField){
						// 最後に更新区分を追加する
						values += ", ?";
						prmData.add(updKbn);

						valueData = ArrayUtils.add(valueData, "("+values+")");
						values = "";
					}
				}
			}

			if(valueData.length >= 100 || (i+1 == len && valueData.length > 0)){

				sbSQL = new StringBuffer();
				sbSQL.append("MERGE INTO INATK.HATTR_CSV AS T USING (SELECT ");
				sbSQL.append("SHUNO");														// 週№
				sbSQL.append(",SHNCD");														// 商品コード
				sbSQL.append(",TENCD");														// 店コード
				sbSQL.append(",JSEIKBN");													// 次正区分
				sbSQL.append(",SURYO_MON");													// 店別数量_月
				sbSQL.append(",SURYO_TUE");													// 店別数量_火
				sbSQL.append(",SURYO_WED");													// 店別数量_水
				sbSQL.append(",SURYO_THU");													// 店別数量_木
				sbSQL.append(",SURYO_FRI");													// 店別数量_金
				sbSQL.append(",SURYO_SAT");													// 店別数量_土
				sbSQL.append(",SURYO_SUN");													// 店別数量_日
				sbSQL.append(",UPDKBN");													// 更新区分
				sbSQL.append(", 0 AS SENDFLG ");											// 送信区分：未送信
				sbSQL.append(", '"+userId+"' AS OPERATOR ");								// オペレーター
				sbSQL.append(", current timestamp AS ADDDT ");								// 登録日
				sbSQL.append(", current timestamp AS UPDDT ");								// 更新日
				sbSQL.append(" FROM (values "+StringUtils.join(valueData, ",")+") as T1(NUM");
				sbSQL.append(",JSEIKBN");													// F1	 次正区分
				sbSQL.append(",SHUNO");														// F2	 週№
				sbSQL.append(",SHNCD");														// F3	 商品コード
				sbSQL.append(",TENCD");														// F4	 店コード
				sbSQL.append(",SURYO_MON");													// F5	 店別数量_月
				sbSQL.append(",SURYO_TUE");													// F6	 店別数量_火
				sbSQL.append(",SURYO_WED");													// F7	 店別数量_水
				sbSQL.append(",SURYO_THU");													// F8	 店別数量_木
				sbSQL.append(",SURYO_FRI");													// F9	 店別数量_金
				sbSQL.append(",SURYO_SAT");													// F10	 店別数量_土
				sbSQL.append(",SURYO_SUN");													// F11	 店別数量_日
				sbSQL.append(",UPDKBN");													// F12	 更新区分
				sbSQL.append(")) as RE on (");
				sbSQL.append("T.TENCD = RE.TENCD AND ");
				sbSQL.append("T.SHNCD = RE.SHNCD AND ");
				sbSQL.append("T.JSEIKBN = RE.JSEIKBN AND ");
				sbSQL.append("T.SHUNO = RE.SHUNO ");
				sbSQL.append(") WHEN MATCHED THEN UPDATE SET ");
				sbSQL.append("SURYO_MON=RE.SURYO_MON");
				sbSQL.append(",SURYO_TUE=RE.SURYO_TUE");
				sbSQL.append(",SURYO_WED=RE.SURYO_WED");
				sbSQL.append(",SURYO_THU=RE.SURYO_THU");
				sbSQL.append(",SURYO_FRI=RE.SURYO_FRI");
				sbSQL.append(",SURYO_SAT=RE.SURYO_SAT");
				sbSQL.append(",SURYO_SUN=RE.SURYO_SUN");
				sbSQL.append(",UPDKBN=RE.UPDKBN ");
				sbSQL.append(",SENDFLG=RE.SENDFLG");
				sbSQL.append(",OPERATOR=RE.OPERATOR ");
				sbSQL.append(",UPDDT=RE.UPDDT");
				sbSQL.append(" WHEN NOT MATCHED THEN INSERT (");
				sbSQL.append("SHUNO");														// 週№
				sbSQL.append(",SHNCD");														// 商品コード
				sbSQL.append(",TENCD");														// 店コード
				sbSQL.append(",JSEIKBN");													// 次正区分
				sbSQL.append(",SURYO_MON");													// 店別数量_月
				sbSQL.append(",SURYO_TUE");													// 店別数量_火
				sbSQL.append(",SURYO_WED");													// 店別数量_水
				sbSQL.append(",SURYO_THU");													// 店別数量_木
				sbSQL.append(",SURYO_FRI");													// 店別数量_金
				sbSQL.append(",SURYO_SAT");													// 店別数量_土
				sbSQL.append(",SURYO_SUN");													// 店別数量_日
				sbSQL.append(",UPDKBN");
				sbSQL.append(",SENDFLG");
				sbSQL.append(",OPERATOR");
				sbSQL.append(",ADDDT");
				sbSQL.append(",UPDDT");
				sbSQL.append(") VALUES (");
				sbSQL.append("RE.SHUNO");													// 週№
				sbSQL.append(",RE.SHNCD");													// 商品コード
				sbSQL.append(",RE.TENCD");													// 店コード
				sbSQL.append(",RE.JSEIKBN");												// 次正区分
				sbSQL.append(",RE.SURYO_MON");												// 店別数量_月
				sbSQL.append(",RE.SURYO_TUE");												// 店別数量_火
				sbSQL.append(",RE.SURYO_WED");												// 店別数量_水
				sbSQL.append(",RE.SURYO_THU");												// 店別数量_木
				sbSQL.append(",RE.SURYO_FRI");												// 店別数量_金
				sbSQL.append(",RE.SURYO_SAT");												// 店別数量_土
				sbSQL.append(",RE.SURYO_SUN");												// 店別数量_日
				sbSQL.append(",RE.UPDKBN");
				sbSQL.append(",RE.SENDFLG");
				sbSQL.append(",RE.OPERATOR");
				sbSQL.append(",RE.ADDDT");
				sbSQL.append(",RE.UPDDT");
				sbSQL.append(")");

				if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

				sqlList.add(sbSQL.toString());
				prmList.add(prmData);
				lblList.add("店別数量_CSV");

				// クリア
				prmData = new ArrayList<String>();
				valueData = new Object[]{};
				values = "";
			}
		}
		return sbSQL.toString();
	}

	private String createCommand() {
		// ログインユーザー情報取得
		User userInfo = getUserInfo();
		if(userInfo==null){
			return "";
		}

		String szShncd	= getMap().get("SHNCD");	// 商品コード
		String szBumon	= getMap().get("BUMON");	// 部門
		String szDaibun	= getMap().get("DAIBUN");	// 大分類
		String szChubun	= getMap().get("CHUBUN");	// 中分類
		String szTencd	= getMap().get("TENCD");	// 店コード
		String szShuno	= getMap().get("SHUNO");	// 週No.
		String szSeiki	= getMap().get("SEIKI");	// 正規
		String szJisyu	= getMap().get("JISYU");	// 次週

		// 配送グループ名称（漢字）に入力があった場合部分一致検索を行う
		// DB検索用パラメータ
		String sqlFrom = "";
		String sqlWhere = "";
		String sqlWith = "";
		ArrayList<String> paramData = new ArrayList<String>();

		// タイトル情報(任意)設定
		List<String> titleList = new ArrayList<String>();
		// 一覧表情報
		StringBuffer sbSQL = new StringBuffer();

		sqlWith = "WITH MST AS(SELECT SHNCD,SHNKN FROM INAMS.MSTSHN WHERE ";
		// 商品コードが未入力の場合は部門で検索
		if(StringUtils.isEmpty(szShncd)){
			if(StringUtils.isEmpty(szBumon) || szBumon.equals("-1")){
				sqlWith += "BMNCD=null AND ";
			}else{
				sqlWith += "SUBSTR(SHNCD,1,2)=? AND BMNCD=? AND ";
				paramData.add(szBumon);
				paramData.add(szBumon);
				//paramData.add(String.valueOf(Integer.valueOf(szBumon.substring(0, 2))));
			}

			if(StringUtils.isEmpty(szDaibun) || szDaibun.equals("-1")){
				sqlWith += "DAICD=null ";
			}else{
				sqlWith += "DAICD=? ";
				paramData.add(szDaibun);
				//paramData.add(String.valueOf(Integer.valueOf(szDaibun.substring(2, 4))));
			}

			if(StringUtils.isEmpty(szChubun) || szChubun.equals("-1")){
				sqlWith += "";
			}else{
				sqlWith += " AND CHUCD=? ";
				paramData.add(szChubun);
				//paramData.add(String.valueOf(Integer.valueOf(szChubun.substring(4, 6))));
			}
		}else{

			sqlWith += "SHNCD=? ";
			paramData.add(szShncd);
		}
		sqlWith += ")";

		sbSQL.append(sqlWith + "SELECT * FROM (");
		// 正規が選択されていた場合
		//sqlFrom += "INATK.HATSTR_TEN TK LEFT JOIN INAMS.MSTTEN TEN ON TK.TENCD=TEN.TENCD LEFT JOIN MST AS M1 ON TK.SHNCD=M1.SHNCD ";
		sqlFrom += "INATK.HATTR_CSV TK LEFT JOIN INAMS.MSTTEN TEN ON TK.TENCD=TEN.TENCD LEFT JOIN MST AS M1 ON TK.SHNCD=M1.SHNCD ";
		sqlFrom += ",MST AS M2 ";

		sqlWhere = "WHERE TK.SHNCD=M2.SHNCD ";

		// 次正区分
		if(szSeiki.equals("1") && szJisyu.equals("0")){
			// 正規の場合
			sqlWhere += " and JSEIKBN = 0 ";

		}else if(szSeiki.equals("0") && szJisyu.equals("1")){
			// 次週の場合
			sqlWhere += " and JSEIKBN = 1";
			if(StringUtils.isNotEmpty(szShuno) && !szShuno.equals("-1")){
				// 週番号を設定
				sqlWhere += " and SHUNO = ? ";
				paramData.add(szShuno);
			}
		}else if(szSeiki.equals("1") && szJisyu.equals("1")){
			// 正規、次週の場合
			if(StringUtils.isNotEmpty(szShuno) && !szShuno.equals("-1")){
				// 週番号を設定
				sqlWhere += " and (JSEIKBN = 0 or (JSEIKBN = 1 and SHUNO = ?))";
				paramData.add(szShuno);
			}
		}

		if(StringUtils.isEmpty(szTencd)){
			sqlWhere += "";
		} else {
			sqlWhere += " AND TK.TENCD=? ";
			paramData.add(szTencd);
		}

		sbSQL.append("SELECT  ");
		sbSQL.append("TK.JSEIKBN");			// F1:  次正区分
		sbSQL.append(",SHUNO");				// F2:  週No.
		sbSQL.append(",TK.SHNCD ");			// F3:  商品コード
		sbSQL.append(",M1.SHNKN ");			// F4:  商品名
		sbSQL.append(",TK.TENCD ");			// F5:  店コード
		sbSQL.append(",TEN.TENKN ");		// F6:  店舗名
		sbSQL.append(",TK.SURYO_MON ");		// F7:  数量＿月
		sbSQL.append(",TK.SURYO_TUE ");		// F8:  数量＿火
		sbSQL.append(",TK.SURYO_WED ");		// F9:  数量＿水
		sbSQL.append(",TK.SURYO_THU ");		// F10: 数量＿木
		sbSQL.append(",TK.SURYO_FRI ");		// F11: 数量＿金
		sbSQL.append(",TK.SURYO_SAT ");		// F12: 数量＿土
		sbSQL.append(",TK.SURYO_SUN ");		// F13: 数量＿日
		//sbSQL.append(",TK.BINKBN ");		// F14: 便区分
		sbSQL.append(",null ");				// F14: 便区分
		sbSQL.append(",TK.OPERATOR ");												// F14	: オペレーター
		sbSQL.append(",TO_CHAR(TK.ADDDT,'YY/MM/DD') AS ADDDT ");					// F15	: 登録日
		sbSQL.append(",TO_CHAR(TK.UPDDT,'YY/MM/DD') AS UPDDT ");					// F16	: 更新日
		sbSQL.append(",TO_CHAR(TK.UPDDT,'YYYYMMDDHH24MISSNNNNNN') as HDN_UPDDT ");	// F17	: 更新日時
		sbSQL.append(",TK.SENDFLG ");												// F18	: 送信フラグ(入力制御用)
		sbSQL.append("FROM ");
		sbSQL.append(sqlFrom);
		sbSQL.append(sqlWhere);
		sbSQL.append(" AND TK.UPDKBN="+DefineReport.ValUpdkbn.NML.getVal());
		sbSQL.append(")ORDER BY JSEIKBN,SHUNO,SHNCD,TENCD");

		// オプション情報（タイトル）設定
		JSONObject option = new JSONObject();
		option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
		setOption(option);

		// DB検索用パラメータ設定
		setParamData(paramData);

		if (DefineReport.ID_DEBUG_MODE) System.out.println(getClass().getSimpleName()+"[sql]"+sbSQL.toString());
		return sbSQL.toString();
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

	// 最小週Noを取得する
	public String getMinShuNo() {

		// 格納用変数
		StringBuffer		sbSQL	= new StringBuffer();
		ArrayList<String> paramData	= new ArrayList<String>();
		String				shuNo	= "";
		ItemList			iL		= new ItemList();
		JSONArray			dbDatas = new JSONArray();

		sbSQL.append(DefineReport.ID_SQL_CMN_WEEK);
		sbSQL.append(", SHORIDT as (select");
		sbSQL.append(" right (YYYY, 2) || right ('00' || (case when WEEK_NO in (2, 3) then SHUNO else SHUNO + 1 end), 2) as SHUNO");
		sbSQL.append(" from (select");
		sbSQL.append(" SHORIDT as value");
		sbSQL.append(", TO_CHAR(TO_DATE(SHORIDT, 'YYYYMMDD'), 'YYYY') as YYYY");
		sbSQL.append(", WEEK_ISO(TO_DATE(SHORIDT, 'YYYYMMDD')) as SHUNO");
		sbSQL.append(", DAYOFWEEK(TO_DATE(SHORIDT, 'YYYYMMDD')) as WEEK_NO");
		sbSQL.append(" from INAAD.SYSSHORIDT where NVL(UPDKBN, 0) <> 1");
		sbSQL.append(" order by ID desc fetch first 1 rows only))");
		sbSQL.append(" select *");
		sbSQL.append(" from (select");
		sbSQL.append(" right ('0000' || MIN(T1.SHUNO), 4) as value");
		sbSQL.append(" from INAAD.SYSSHUNO T1");
		sbSQL.append(" inner join (select distinct SHUNO from INATK.HATJTR_TEN) T2 on T1.SHUNO = T2.SHUNO, SHORIDT");
		sbSQL.append(" where SHORIDT.SHUNO < T1.SHUNO) T order by replace (value, '-1', '9999') desc");

		@SuppressWarnings("static-access")
		JSONArray array  = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

		if (dbDatas.size() >= 0){
			shuNo =  array.optJSONObject(0).optString("VALUE");
		}
		return shuNo;
	}

	/**
	 * 排他判断<br>
	 * 更新日時が変わっていないことを判断する<br>
	 * @param targetTable 対象テーブル
	 * @param targetWhere テーブルのキー条件
	 * @param targetParam テーブルのキー
	 * @param inp_upddt 画面入力開始時の更新日時
	 * @return true:一致/false:不一致(排他発生)
	 */
	public boolean checkExclusion(HashMap<String, String> map, String inp_upddt) {

		ItemList iL = new ItemList();

		String szShncd	= map.get("SHNCD");		// 商品コード
		String szBumon	= map.get("BUMON");		// 部門
		String szDaibun	= map.get("DAIBUN");	// 大分類
		String szChubun	= map.get("CHUBUN");	// 中分類
		String szTencd	= map.get("TENCD");		// 店コード
		String szShuno	= map.get("SHUNO");		// 週No.
		String szSeiki	= map.get("SEIKI");		// 正規
		String szJisyu	= map.get("JISYU");		// 次週

		// 配送グループ名称（漢字）に入力があった場合部分一致検索を行う
		// DB検索用パラメータ
		String sqlFrom = "";
		String sqlWhere = "";
		String sqlWith = "";
		ArrayList<String> paramData = new ArrayList<String>();

		// 一覧表情報
		StringBuffer sbSQL = new StringBuffer();

		JSONArray		dataArrayT	= JSONArray.fromObject(map.get("DATA_HATSTR"));	// 正規定量_店別数量(次週も含む)
		int len = dataArrayT.size();
		for (int i = 0; i < len; i++) {
			JSONObject dataT = dataArrayT.getJSONObject(i);

			String UPDDT	 = dataT.optString("F13");
			String JSEIKBN	 = dataT.optString("F2");
			String SHUNO	 = dataT.optString("F3");
			String SHNCD	 = dataT.optString("F4");
			String TENCD	 = dataT.optString("F5");

			paramData.add(UPDDT);
			paramData.add(JSEIKBN);
			paramData.add(SHUNO);
			paramData.add(SHNCD);
			paramData.add(TENCD);

			sbSQL.append(" select");
			sbSQL.append(" COUNT(UPDDT) as value");
			sbSQL.append(" from (select * from (select");
			sbSQL.append(" TK.JSEIKBN");
			sbSQL.append(", TK.SHUNO");
			sbSQL.append(", TK.SHNCD");
			sbSQL.append(", TK.TENCD");
			sbSQL.append(", TO_CHAR(TK.UPDDT, 'YYYYMMDDHH24MISSNNNNNN') as UPDDT");
			sbSQL.append(" from INATK.HATTR_CSV TK");
			sbSQL.append(" where TK.UPDKBN = 0))");
			sbSQL.append(" where UPDDT = ?");
			sbSQL.append(" and JSEIKBN = ?");
			sbSQL.append(" and SHUNO = ?");
			sbSQL.append(" and SHNCD = ?");
			sbSQL.append(" and TENCD = ?");

			@SuppressWarnings("static-access")
			JSONArray array = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);
			if(array.size() > 0 && NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) == 0){
				return false;
			}

		}
		return true;
	}
}
