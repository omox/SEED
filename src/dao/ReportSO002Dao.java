package dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

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
public class ReportSO002Dao extends ItemDao {

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
	public ReportSO002Dao(String JNDIname) {
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

	private String createCommand() {
		// ログインユーザー情報取得
		User userInfo = getUserInfo();
		if(userInfo==null){
			return "";
		}

		String moyskbn	 = getMap().get("MOYSKBN"); 				// 催し区分
		String moysstdt	 = getMap().get("MOYSSTDT"); 				// 催し開始日
		String moysrban	 = getMap().get("MOYSRBAN"); 				// 催し連番

		// タイトル情報(任意)設定
		List<String> titleList = new ArrayList<String>();

		// 基本情報取得
		JSONArray array = getTOKMOYCDData(getMap());

		// 一覧表情報
		StringBuffer sbSQL = new StringBuffer();
		sbSQL.append(" select");
		sbSQL.append(" right ('00' || BMN.BMNCD, 2)");	// F1 : 部門
		sbSQL.append(" , BMN.BMNKN");					// F2 : 部門名
		sbSQL.append(" , BMN.COUNT");					// F3 : 登録件数
		sbSQL.append(" from");
		sbSQL.append(" INATK.TOKMOYCD MYCD");
		sbSQL.append(" ,(");
		sbSQL.append(" SELECT");
		sbSQL.append(" T1.*");
		sbSQL.append(" ,T2.BMNKN");
		sbSQL.append(" FROM");
		sbSQL.append(" (");
		sbSQL.append(" SELECT");
		sbSQL.append(" T1.MOYSKBN");
		sbSQL.append(" ,T1.MOYSSTDT");
		sbSQL.append(" ,T1.MOYSRBAN");
		sbSQL.append(" ,T1.BMNCD");
		sbSQL.append(" ,SUM(CASE WHEN T2.SHNCD IS NULL THEN 0 ELSE 1 END) COUNT");
		sbSQL.append(" FROM");
		sbSQL.append(" INATK.TOKSO_BMN T1 LEFT JOIN INATK.TOKSO_SHN T2 ON");
		sbSQL.append(" T1.MOYSKBN=T2.MOYSKBN");
		sbSQL.append(" AND T1.MOYSSTDT=T2.MOYSSTDT");
		sbSQL.append(" AND T1.MOYSRBAN=T2.MOYSRBAN");
		sbSQL.append(" AND T1.BMNCD=T2.BMNCD");
		sbSQL.append(" AND T1.UPDKBN <> 1 AND T1.UPDKBN <> 1 ");
		sbSQL.append(" GROUP BY");
		sbSQL.append(" T1.MOYSKBN");
		sbSQL.append(" ,T1.MOYSSTDT");
		sbSQL.append(" ,T1.MOYSRBAN");
		sbSQL.append(" ,T1.BMNCD");
		sbSQL.append(" ) T1 LEFT JOIN INAMS.MSTBMN T2 ON");
		sbSQL.append(" T1.BMNCD = T2.BMNCD");
		sbSQL.append(" ) BMN");
		sbSQL.append(" where");
		sbSQL.append(" NVL(MYCD.UPDKBN, 0) <> 1 ");
		sbSQL.append(" and MYCD.MOYSKBN = "+moyskbn+" and MYCD.MOYSSTDT = "+moysstdt+" and MYCD.MOYSRBAN = "+moysrban);
		sbSQL.append(" and BMN.MOYSKBN = MYCD.MOYSKBN ");
		sbSQL.append(" and BMN.MOYSSTDT = MYCD.MOYSSTDT ");
		sbSQL.append(" and BMN.MOYSRBAN = MYCD.MOYSRBAN order by BMN.BMNCD");

		// オプション情報（タイトル）設定
		JSONObject option = new JSONObject();
		option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
		option.put("rows_", array);
		setOption(option);

		if (DefineReport.ID_DEBUG_MODE) System.out.println(getClass().getSimpleName()+"[sql]"+sbSQL.toString());
		return sbSQL.toString();
	}

	/**
	 * 正情報取得処理
	 *
	 * @throws Exception
	 */
	public JSONArray getTOKMOYCDData(HashMap<String, String> map) {
		String szMoyskbn	= map.get("MOYSKBN");	// 催し区分
		String szMoysstdt	= map.get("MOYSSTDT");	// 催しコード（催し開始日）
		String szMoysrban	= map.get("MOYSRBAN");	// 催し連番

		ArrayList<String> paramData = new ArrayList<String>();


		StringBuffer sbSQL = new StringBuffer();
		sbSQL.append(DefineReport.ID_SQL_CMN_WEEK);
		sbSQL.append("  select");
		sbSQL.append("    T1.MOYSKBN||'-'||right(T1.MOYSSTDT, 6)||'-'||right('000'||T1.MOYSRBAN, 3) as F1");
		sbSQL.append("  , T1.MOYKN as F2");
		sbSQL.append("  , to_char(to_date(T1.HBSTDT, 'YYYYMMDD'), 'YY/MM/DD')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(TO_DATE(T1.HBSTDT, 'YYYYMMDD')))");
		sbSQL.append("    ||'～'||");
		sbSQL.append("    to_char(to_date(T1.HBEDDT, 'YYYYMMDD'), 'YY/MM/DD')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(TO_DATE(T1.HBEDDT, 'YYYYMMDD'))) as F3");
		sbSQL.append("  from INATK.TOKMOYCD T1");
		sbSQL.append("  where T1.UPDKBN = 0");
		sbSQL.append("  and T1.MOYSKBN = "+szMoyskbn+"");
		sbSQL.append("  and T1.MOYSSTDT = "+szMoysstdt+"");
		sbSQL.append("  and T1.MOYSRBAN = "+szMoysrban+"");

		ItemList iL = new ItemList();
		@SuppressWarnings("static-access")
		JSONArray array = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

		return array;
	}

	private void outputQueryList() {

		// 検索条件の加工クラス作成
		JsonArrayData jad = new JsonArrayData();
		jad.setJsonString(getJson());

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
		JSONArray	dataArray	= JSONArray.fromObject(map.get("DATA"));	// 対象情報（主要な更新情報）

		// 排他チェック用
		JSONArray			msg			= new JSONArray();

		// パラメータ確認
		// 必須チェック
		if ( dataArray.isEmpty() || dataArray.size() != 1 || dataArray.getJSONObject(0).isEmpty()) {
			option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
			return option;
		}

		// 基本登録情報
		JSONObject data = dataArray.getJSONObject(0);

		// 新店改装店発注、新店改装店発注＿商品INSERT/UPDATE処理
		this.createSqlSTKTHAT(data,map,userInfo);

		// 排他チェック実行
		String targetTable = null;
		String targetWhere = null;
		ArrayList<String> targetParam = new ArrayList<String>();
		if(dataArray.size() > 0){
			targetTable = "INATK.HATSK";
			targetWhere = "INPUTNO = ?";

			if(StringUtils.isNotEmpty(data.optString("F1"))){
				targetParam.add(data.optString("F1"));
			}
			if(! super.checkExclusion(targetTable, targetWhere, targetParam, data.optString("F8"))){
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
				option.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00001.getVal()));
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
	public JSONArray check(HashMap<String, String> map, User userInfo, String sysdate) {

		// パラメータ確認
		JSONArray	dataArray		= JSONArray.fromObject(map.get("DATA"));		// 更新情報(配送グループ)
		JSONArray	dataArrayTEN	= JSONArray.fromObject(map.get("DATA_TEN"));	// 更新情報(配送店グループ)
		String		sendBtnid		= map.get("SENDBTNID");							// 呼出しボタン

		// 格納用変数
		StringBuffer		sbSQL	= new StringBuffer();
		JSONArray			msg		= new JSONArray();
		ItemList			iL		= new ItemList();
		MessageUtility		mu		= new MessageUtility();
		JSONArray			dbDatas = new JSONArray();

		// チェック処理
		// 対象件数チェック
		if(dataArray.size() == 0 || dataArray.getJSONObject(0).isEmpty()){
			msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
			return msg;
		}

		// 基本登録情報
		JSONObject data = dataArray.getJSONObject(0);

		/*// 標準-商品コード
		// ①商品マスタに無い場合エラー
		String shncd = data.optString("F2");
		if(!this.checkMstExist(DefineReport.InpText.SHNCD.getObj(), shncd)){
			JSONObject o = mu.getDbMessageObj("E11046", new String[]{});
			msg.add(o);
			return msg;
		}*/

		// グリッドエラーチェック：店舗一覧
		/*ArrayList<JSONObject> record = new ArrayList<JSONObject>();
		HashSet<String> parameter_ = new HashSet<String>();
		for (int i = 0; i < dataArrayTEN.size(); i++) {
			data = dataArray.getJSONObject(0);

			// 店舗取得
			String val = data.optString("F1");
			if(StringUtils.isNotEmpty(val)){
				record.add(data);							// レコード数
				parameter_.add(val);						// 店コードード(HashSetにより重複なし状態にする。)
			}

			// 標準-店舗コード
			// ①店舗基本マスタに無い場合エラー
			String tencd = data.optString("F1");
			if(!this.checkMstExist(DefineReport.InpText.TENCD.getObj(), tencd)){
				JSONObject o = mu.getDbMessageObj("E11046", new String[]{});
				msg.add(o);
				return msg;
			}
		}*/
		// 重複チェック
		/*if(record.size() != parameter_.size()){
			JSONObject o = MessageUtility.getDbMessageIdObj("E11141", new String[]{});
			msg.add(o);
			return msg;
		}*/

		return msg;
	}

	/**
	 * 新店改装店発注_商品INSERT/UPDATE処理
	 *
	 * @param data
	 * @param map
	 * @param userInfo
	 */
	public String createSqlSTKTHAT(JSONObject data, HashMap<String, String> map, User userInfo){

		StringBuffer	sbSQL		= new StringBuffer();
		JSONArray		dataArrayT		= JSONArray.fromObject(map.get("DATA_SHN"));		// 更新情報(予約発注_納品日)
		JSONArray		dataArrayTDel	= JSONArray.fromObject(map.get("DATA_SHN_DEL"));	// 対象情報（主要な更新情報）


		// ログインユーザー情報取得
		String userId		 = userInfo.getId();					// ログインユーザー
		String sendBtnid	 = map.get("SENDBTNID");				// 呼出しボタン
		String allCheck		 = map.get("ALLCHECK");					// チェック数確認フラグ
		String inputNo		 = "";									// 入力番号


		ArrayList<String> prmData = new ArrayList<String>();
		Object[] valueData = new Object[]{};
		String values = "";

		int maxField = 5;		// Fxxの最大値
		for (int k = 1; k <= maxField; k++) {
			String key = "F" + String.valueOf(k);

			if(k == 1){
				values += String.valueOf(0 + 1);

			}

			if(! ArrayUtils.contains(new String[]{"F5"}, key)){
				String val = data.optString(key);
				if(StringUtils.isEmpty(val)){
					values += ", null";
				}else{
					values += ", ?";
					prmData.add(val);
				}
			}

			if(k == maxField){
				valueData = ArrayUtils.add(valueData, "("+values+")");
				values = "";
			}
		}

		// 生活応援_部門の登録・更新
		sbSQL = new StringBuffer();
		sbSQL.append(" merge into INATK.TOKSO_BMN as T using (select");
		sbSQL.append(" MOYSKBN");													// 催し区分
		sbSQL.append(", MOYSSTDT");													// 催し開始日
		sbSQL.append(", MOYSRBAN");													// 催し連番
		sbSQL.append(", BMNCD");													// 部門
		sbSQL.append(", "+DefineReport.ValUpdkbn.NML.getVal()+" AS UPDKBN");		// 更新区分：
		sbSQL.append(", null as SENDFLG");											// 送信フラグ
		sbSQL.append(", '"+userId+"' AS OPERATOR ");								// オペレーター：
		sbSQL.append(", current timestamp AS ADDDT ");								// 登録日：
		sbSQL.append(", current timestamp AS UPDDT ");								// 更新日：
		sbSQL.append(" from (values "+StringUtils.join(valueData, ",")+") as T1(NUM, ");
		sbSQL.append(" MOYSKBN");
		sbSQL.append(", MOYSSTDT");
		sbSQL.append(", MOYSRBAN");
		sbSQL.append(", BMNCD");
		sbSQL.append(" ))as RE on (T.MOYSKBN = RE.MOYSKBN and T.MOYSSTDT = RE.MOYSSTDT and T.MOYSRBAN = RE.MOYSRBAN and T.BMNCD = RE.BMNCD)");
		sbSQL.append(" when matched then update set");
		sbSQL.append(" MOYSKBN=RE.MOYSKBN");
		sbSQL.append(", MOYSSTDT=RE.MOYSSTDT");
		sbSQL.append(", MOYSRBAN=RE.MOYSRBAN");
		sbSQL.append(", BMNCD=RE.BMNCD");
		sbSQL.append(", UPDKBN=RE.UPDKBN");
		sbSQL.append(", SENDFLG=RE.SENDFLG");
		sbSQL.append(", OPERATOR=RE.OPERATOR");
		sbSQL.append(", UPDDT=RE.UPDDT");
		sbSQL.append(" when not matched then insert (");
		sbSQL.append(" MOYSKBN");
		sbSQL.append(", MOYSSTDT");
		sbSQL.append(", MOYSRBAN");
		sbSQL.append(", BMNCD");
		sbSQL.append(", UPDKBN");
		sbSQL.append(", SENDFLG");
		sbSQL.append(", OPERATOR");
		sbSQL.append(", ADDDT");
		sbSQL.append(", UPDDT");
		sbSQL.append(") values (");
		sbSQL.append(" RE.MOYSKBN");
		sbSQL.append(", RE.MOYSSTDT");
		sbSQL.append(", RE.MOYSRBAN");
		sbSQL.append(", RE.BMNCD");
		sbSQL.append(", RE.UPDKBN");
		sbSQL.append(", RE.SENDFLG");
		sbSQL.append(", RE.OPERATOR");
		sbSQL.append(", RE.ADDDT");
		sbSQL.append(", RE.UPDDT");
		sbSQL.append(")");

		if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("生活応援_部門");

		// クリア
		prmData = new ArrayList<String>();
		valueData = new Object[]{};
		values = "";

		return sbSQL.toString();
	}
}
