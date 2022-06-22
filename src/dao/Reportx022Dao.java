package dao;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
public class Reportx022Dao extends ItemDao {

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
	public Reportx022Dao(String JNDIname) {
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
	 * 更新処理
	 * @param userInfo
	 *
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws Exception
	 */
	public JSONObject update(HttpServletRequest request,HttpSession session,  HashMap<String, String> map, User userInfo) {
		String sysdate = (String)request.getSession().getAttribute(Consts.STR_SES_LOGINDT);

		// 更新情報チェック(基本JS側で制御)
		JSONObject msgObj = new JSONObject();
		JSONArray msg = this.check(map);

		if(msg.size() > 0){
			msgObj.put(MsgKey.E.getKey(), msg);
			return msgObj;
		}

		// 更新処理
		try {
			msgObj = this.updateData(map, userInfo, sysdate);
		} catch (Exception e) {
			e.printStackTrace();
			msgObj.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
		}
		return msgObj;
	}

	/**
	 * 削除処理
	 * @param userInfo
	 *
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws Exception
	 */
	public JSONObject delete(HttpServletRequest request,HttpSession session,  HashMap<String, String> map, User userInfo) {

		// 更新情報チェック(基本JS側で制御)
		JSONObject msgObj = new JSONObject();
		JSONArray msg = this.checkDel(map);

		if(msg.size() > 0){
			msgObj.put(MsgKey.E.getKey(), msg);
			return msgObj;
		}

		// 削除処理
		try {
			msgObj = this.deleteData(map, userInfo);
		} catch (Exception e) {
			e.printStackTrace();
			msgObj.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00002.getVal()));
		}
		return msgObj;
	}

	private String createCommand() {
		// ログインユーザー情報取得
		User userInfo = getUserInfo();
		if(userInfo==null){
			return "";
		}
		ArrayList<String> paramData = new ArrayList<String>();
		String szSelBmoncd = getMap().get("SEL_BMONCD");		// 選択部門コード
		String sendBtnid = getMap().get("SENDBTNID");			// 呼出しボタン

		// パラメータ確認
		// 必須チェック
		if ( szSelBmoncd == null || sendBtnid == null) {
			System.out.println(super.getConditionLog());
			return "";
		}

		// タイトル情報(任意)設定
		List<String> titleList = new ArrayList<String>();


		StringBuffer sbSQL = new StringBuffer();

		if(DefineReport.Button.SEL_CHANGE.getObj().equals(sendBtnid)){

			sbSQL.append(" select");
			sbSQL.append(" BMN.BMNCD");																// F1	：部門
			sbSQL.append(", BMN.BMNKBN");															// F2	：部門区分
			sbSQL.append(", BMN.BMNKN");															// F3	：部門名称（漢字）
			sbSQL.append(", BMN.BMNAN");															// F4	：部門名称（カナ）
			sbSQL.append(", BMN.ZEIKBN");															// F5	：税区分
			sbSQL.append(", TO_CHAR(TO_DATE(BMN.ZEIRTHENKODT, 'yyyymmdd'), 'yy/mm/dd')");			// F6	：税率変更日
			sbSQL.append(", BMN.ZEIRTKBN_OLD");														// F7	：旧税率区分
			sbSQL.append(", BMN.ZEIRTKBN");															// F8	：税率区分
			sbSQL.append(", BMN.ODBOOKKBN");														// F9	：オーダーブック出力区分
			sbSQL.append(", BMN.HYOKAKBN");															// F10	：評価方法区分
			sbSQL.append(", BMN.GENKART");															// F11	：原価率
			sbSQL.append(", BMN.CORPBMNCD");														// F12	：会社部門
			sbSQL.append(", BMN.URIBMNCD");															// F13	：売上計上部門
			sbSQL.append(", BMN.HOGANBMNCD");														// F14	：包含部門
			sbSQL.append(", BMN.JYOGENAM");															// F15	：上限金額
			sbSQL.append(", BMN.JYOGENSU");															// F16	：上限数量
			sbSQL.append(", BMN.TANAOROTIMKB");														// F17	：棚卸タイミング
			sbSQL.append(", BMN.POSBAIHENKBN");														// F18	：POS売変対象区分
			sbSQL.append(", BMN.KEIHIKBN");															// F19	：経費対象区分
			sbSQL.append(", BMN.TANPINKBN");														// F20	：単品管理区分
			sbSQL.append(", BMN.DELKIJYUNSU");														// F21	：削除基準区分
			sbSQL.append(", BMN.WARIGAIFLG");														// F22	：値引除外フラグ
			sbSQL.append(", BMN.ITEMBETSUCD");														// F23	：商品・非商品識別コード
			sbSQL.append(", BMN.URISEIGENFLG");														// F24	：販売制限フラグ
			sbSQL.append(", BMN.URIKETAKBN");														// F25	：売上金額最大桁数
			sbSQL.append(", TO_CHAR(BMN.ADDDT, 'yy/mm/dd')");										// F26	：登録日
			sbSQL.append(", TO_CHAR(BMN.UPDDT, 'yy/mm/dd')");										// F27	：更新日
			sbSQL.append(", BMN.OPERATOR");															// F28	：オペレータ
  			sbSQL.append(", TO_CHAR(BMN.UPDDT,'YYYYMMDDHH24MISSNNNNNN') as HDN_UPDDT ");			// F29	: 排他チェック用：更新日(非表示)
			sbSQL.append(" from INAMS.MSTBMN BMN");
			sbSQL.append(" where BMN.BMNCD = ? ");
			paramData.add(szSelBmoncd);
			sbSQL.append(" order by BMN.BMNCD");
			setParamData(paramData);
		}

		// オプション情報（タイトル）設定
		JSONObject option = new JSONObject();
		option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
		setOption(option);

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

	/**
	 * 更新処理実行
	 * @return
	 *
	 * @throws Exception
	 */
	@SuppressWarnings("static-access")
	/*private JSONObject updateData(HashMap<String, String> map, User userInfo) throws Exception {
		// パラメータ確認
		JSONArray dataArray = JSONArray.fromObject(map.get("DATA"));	// 対象情報
		JSONObject msgObj = new JSONObject();
		JSONArray msg = new JSONArray();

		// ログインユーザー情報取得
		String userId	= userInfo.getId();								// ログインユーザー

		String sendBtnid = map.get("SENDBTNID");							// 呼出しボタン
		String bmoncd = "";													// 入力部門コード
		String addSQL = "";

		// 更新情報
		String values = "";
		for (int i = 0; i < dataArray.size(); i++) {
			JSONObject data = dataArray.getJSONObject(i);
			if(data.isEmpty()){
				continue;
			}

			bmoncd = data.optString("F1");
			values += bmoncd +",";
			values += StringUtils.equals(data.optString("F2"), DefineReport.Values.NONE.getVal()) ? "null," : "'"+data.optString("F2")+"',";
			values += StringUtils.defaultIfEmpty("'"+data.optString("F3")+"'", "null")+",";
			values += StringUtils.defaultIfEmpty("'"+data.optString("F4")+"'", "null")+",";
			values += StringUtils.equals(data.optString("F5"), DefineReport.Values.NONE.getVal()) ? "null,":"'"+data.optString("F5")+"',";
			values += StringUtils.defaultIfEmpty(data.optString("F6"), "null")+",";
			values += StringUtils.equals(data.optString("F7"), DefineReport.Values.NONE.getVal()) ? "null,":"'"+data.optString("F7")+"',";
			values += StringUtils.equals(data.optString("F8"), DefineReport.Values.NONE.getVal()) ? "null,":"'"+data.optString("F8")+"',";
			values += StringUtils.equals(data.optString("F9"), DefineReport.Values.NONE.getVal()) ? "null,":"'"+data.optString("F9")+"',";
			values += StringUtils.equals(data.optString("F10"), DefineReport.Values.NONE.getVal()) ? "null,":"'"+data.optString("F10")+"',";
			values += StringUtils.defaultIfEmpty(data.optString("F11"), "null")+",";
			values += StringUtils.defaultIfEmpty(data.optString("F12"), "null")+",";
			values += StringUtils.defaultIfEmpty(data.optString("F13"), "null")+",";
			values += StringUtils.defaultIfEmpty(data.optString("F14"), "null")+",";
			values += StringUtils.defaultIfEmpty(data.optString("F15"), "null")+",";
			values += StringUtils.defaultIfEmpty(data.optString("F16"), "null")+",";
			values += StringUtils.equals(data.optString("F17"), DefineReport.Values.NONE.getVal()) ? "null,":"'"+data.optString("F17")+"',";
			values += StringUtils.equals(data.optString("F18"), DefineReport.Values.NONE.getVal()) ? "null,":"'"+data.optString("F18")+"',";
			values += StringUtils.equals(data.optString("F19"), DefineReport.Values.NONE.getVal()) ? "null,":"'"+data.optString("F19")+"',";
			values += StringUtils.equals(data.optString("F20"), DefineReport.Values.NONE.getVal()) ? "null,":"'"+data.optString("F20")+"',";
			values += StringUtils.defaultIfEmpty(data.optString("F21"), "null")+",";
			values += StringUtils.equals(data.optString("F22"), DefineReport.Values.NONE.getVal()) ? "null,":"'"+data.optString("F22")+"',";
			values += StringUtils.equals(data.optString("F23"), DefineReport.Values.NONE.getVal()) ? "null,":"'"+data.optString("F23")+"',";
			values += StringUtils.equals(data.optString("F24"), DefineReport.Values.NONE.getVal()) ? "null,":"'"+data.optString("F24")+"',";
			values += StringUtils.defaultIfEmpty(data.optString("F25"), "null")+",";
			values += "null,";
			values += "null,";
			values += "null,";
			values += "null,";
			values += "null)";

		}

		if(values.length()==0){
			msgObj.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
			return msgObj;
		}

		// 基本INSERT/UPDATE文
		StringBuffer sbSQL;
		ItemList iL = new ItemList();
		JSONArray dbDatas = new JSONArray();
		ArrayList<String> prmData = new ArrayList<String>();

		if(DefineReport.Button.NEW.getObj().equals(sendBtnid)){
			// 重複チェック：部門コード
			sbSQL = new StringBuffer();
			sbSQL.append("select * from INAMS.MSTBMN BMN where NVL(BMN.UPDKBN, 0) = 0 and BMN.BMNCD = "+bmoncd);
			dbDatas = iL.selectJSONArray(sbSQL.toString(), null, Defines.STR_JNDI_DS);
			if(dbDatas.size() > 0 ){
				msg.add(MessageUtility.getMsg("部門コードが重複しています。"));
			}

			// 論理削除データ有の場合SQL文を追加
			sbSQL = new StringBuffer();
			sbSQL.append("select * from INAMS.MSTBMN BMN where NVL(BMN.UPDKBN, 0) = 1 and BMN.BMNCD = "+bmoncd);
			dbDatas = iL.selectJSONArray(sbSQL.toString(), null, Defines.STR_JNDI_DS);
			if(dbDatas.size() > 0 ){
				addSQL = ", ADDDT = RE.ADDDT";
			}
		}

		// 更新SQL
		sbSQL = new StringBuffer();
		sbSQL.append("merge into INAMS.MSTBMN as T using (select");
		sbSQL.append(" T1.BMNCD");
		sbSQL.append(", T1.BMNKBN");
		sbSQL.append(", T1.BMNKN");
		sbSQL.append(", T1.BMNAN");
		sbSQL.append(", T1.ZEIKBN");
		sbSQL.append(", T1.ZEIRTHENKODT");
		sbSQL.append(", T1.ZEIRTKBN_OLD");
		sbSQL.append(", T1.ZEIRTKBN");
		sbSQL.append(", T1.ODBOOKKBN");
		sbSQL.append(", T1.HYOKAKBN");
		sbSQL.append(", T1.GENKART");
		sbSQL.append(", T1.CORPBMNCD");
		sbSQL.append(", T1.URIBMNCD");
		sbSQL.append(", T1.HOGANBMNCD");
		sbSQL.append(", T1.JYOGENAM");
		sbSQL.append(", T1.JYOGENSU");
		sbSQL.append(", T1.TANAOROTIMKB");
		sbSQL.append(", T1.POSBAIHENKBN");
		sbSQL.append(", T1.KEIHIKBN");
		sbSQL.append(", T1.TANPINKBN");
		sbSQL.append(", T1.DELKIJYUNSU");
		sbSQL.append(", T1.WARIGAIFLG");
		sbSQL.append(", T1.ITEMBETSUCD");
		sbSQL.append(", T1.URISEIGENFLG");
		sbSQL.append(", T1.URIKETAKBN");
		sbSQL.append(", "+DefineReport.ValUpdkbn.NML.getVal()+" as UPDKBN");
		sbSQL.append(", T1.SENDFLG");
		sbSQL.append(", '"+userId+"' as OPERATOR");
		sbSQL.append(", current timestamp as ADDDT");
		sbSQL.append(", current timestamp as UPDDT");
		sbSQL.append(" from (values ("+values+") as T1(");
		sbSQL.append("  BMNCD");
		sbSQL.append(", BMNKBN");
		sbSQL.append(", BMNKN");
		sbSQL.append(", BMNAN");
		sbSQL.append(", ZEIKBN");
		sbSQL.append(", ZEIRTHENKODT");
		sbSQL.append(", ZEIRTKBN_OLD");
		sbSQL.append(", ZEIRTKBN");
		sbSQL.append(", ODBOOKKBN");
		sbSQL.append(", HYOKAKBN");
		sbSQL.append(", GENKART");
		sbSQL.append(", CORPBMNCD");
		sbSQL.append(", URIBMNCD");
		sbSQL.append(", HOGANBMNCD");
		sbSQL.append(", JYOGENAM");
		sbSQL.append(", JYOGENSU");
		sbSQL.append(", TANAOROTIMKB");
		sbSQL.append(", POSBAIHENKBN");
		sbSQL.append(", KEIHIKBN");
		sbSQL.append(", TANPINKBN");
		sbSQL.append(", DELKIJYUNSU");
		sbSQL.append(", WARIGAIFLG");
		sbSQL.append(", ITEMBETSUCD");
		sbSQL.append(", URISEIGENFLG");
		sbSQL.append(", URIKETAKBN");
		sbSQL.append(", UPDKBN");
		sbSQL.append(", SENDFLG");
		sbSQL.append(", OPERATOR");
		sbSQL.append(", ADDDT");
		sbSQL.append(", UPDDT)) as RE on (T.BMNCD = RE.BMNCD)");
		sbSQL.append(" when matched then update set");
		sbSQL.append("  BMNKBN = RE.BMNKBN");
		sbSQL.append(", BMNKN = RE.BMNKN");
		sbSQL.append(", BMNAN = RE.BMNAN");
		sbSQL.append(", ZEIKBN = RE.ZEIKBN");
		sbSQL.append(", ZEIRTHENKODT = RE.ZEIRTHENKODT");
		sbSQL.append(", ZEIRTKBN_OLD = RE.ZEIRTKBN_OLD");
		sbSQL.append(", ZEIRTKBN = RE.ZEIRTKBN");
		sbSQL.append(", ODBOOKKBN = RE.ODBOOKKBN");
		sbSQL.append(", HYOKAKBN = RE.HYOKAKBN");
		sbSQL.append(", GENKART = RE.GENKART");
		sbSQL.append(", CORPBMNCD = RE.CORPBMNCD");
		sbSQL.append(", URIBMNCD = RE.URIBMNCD");
		sbSQL.append(", HOGANBMNCD = RE.HOGANBMNCD");
		sbSQL.append(", JYOGENAM = RE.JYOGENAM");
		sbSQL.append(", JYOGENSU = RE.JYOGENSU");
		sbSQL.append(", TANAOROTIMKB = RE.TANAOROTIMKB");
		sbSQL.append(", POSBAIHENKBN = RE.POSBAIHENKBN");
		sbSQL.append(", KEIHIKBN = RE.KEIHIKBN");
		sbSQL.append(", TANPINKBN = RE.TANPINKBN");
		sbSQL.append(", DELKIJYUNSU = RE.DELKIJYUNSU");
		sbSQL.append(", WARIGAIFLG = RE.WARIGAIFLG");
		sbSQL.append(", ITEMBETSUCD = RE.ITEMBETSUCD");
		sbSQL.append(", URISEIGENFLG = RE.URISEIGENFLG");
		sbSQL.append(", URIKETAKBN = RE.URIKETAKBN");
		sbSQL.append(   addSQL);
		sbSQL.append(", UPDKBN = RE.UPDKBN");
		sbSQL.append(", SENDFLG = RE.SENDFLG");
		sbSQL.append(", OPERATOR = RE.OPERATOR");
		sbSQL.append(", UPDDT = RE.UPDDT");
		sbSQL.append(" when not matched then insert values (");
		sbSQL.append("  RE.BMNCD");
		sbSQL.append(", RE.BMNKBN");
		sbSQL.append(", RE.BMNKN");
		sbSQL.append(", RE.BMNAN");
		sbSQL.append(", RE.ZEIKBN");
		sbSQL.append(", RE.ZEIRTHENKODT");
		sbSQL.append(", RE.ZEIRTKBN_OLD");
		sbSQL.append(", RE.ZEIRTKBN");
		sbSQL.append(", RE.ODBOOKKBN");
		sbSQL.append(", RE.HYOKAKBN");
		sbSQL.append(", RE.GENKART");
		sbSQL.append(", RE.CORPBMNCD");
		sbSQL.append(", RE.URIBMNCD");
		sbSQL.append(", RE.HOGANBMNCD");
		sbSQL.append(", RE.JYOGENAM");
		sbSQL.append(", RE.JYOGENSU");
		sbSQL.append(", RE.TANAOROTIMKB");
		sbSQL.append(", RE.POSBAIHENKBN");
		sbSQL.append(", RE.KEIHIKBN");
		sbSQL.append(", RE.TANPINKBN");
		sbSQL.append(", RE.DELKIJYUNSU");
		sbSQL.append(", RE.WARIGAIFLG");
		sbSQL.append(", RE.ITEMBETSUCD");
		sbSQL.append(", RE.URISEIGENFLG");
		sbSQL.append(", RE.URIKETAKBN");
		sbSQL.append(", RE.UPDKBN");
		sbSQL.append(", RE.SENDFLG");
		sbSQL.append(", RE.OPERATOR");
		sbSQL.append(", RE.ADDDT");
		sbSQL.append(", RE.UPDDT)");

		if (msg.size() > 0) {
			// 重複チェック_エラー時
			msgObj.put(MsgKey.E.getKey(), msg);

		}else{
			// 更新処理実行
			System.out.println(sbSQL);
			int count = super.executeSQL(sbSQL.toString(), prmData);
			if(StringUtils.isEmpty(getMessage())) {
				System.out.println("部門を " + MessageUtility.getMessage(Msg.S00003.getVal(),
						new String[] { Integer.toString(dataArray.size()), Integer.toString(count) }));
				msgObj.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00001.getVal()));
			}else{
				msgObj.put(MsgKey.E.getKey(), getMessage());
			}
		}
		return msgObj;
	}*/

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

		// 部門マスタINSERT/UPDATE処理
		this.createSqlBumon(data,map,userInfo);

		// 排他チェック実行
		String targetTable = null;
		String targetWhere = null;
		ArrayList<String> targetParam = new ArrayList<String>();
		if(dataArray.size() > 0){
			targetTable = "INAMS.MSTBMN";
			targetWhere = " BMNCD = ? and UPDKBN = 0";
			targetParam.add(data.optString("F1"));
			if(! super.checkExclusion(targetTable, targetWhere, targetParam, data.optString("F29"))){
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
	 * 部門マスタINSERT/UPDATE処理
	 *
	 * @param data
	 * @param map
	 * @param userInfo
	 */
	public String createSqlBumon(JSONObject data, HashMap<String, String> map, User userInfo){

		StringBuffer	sbSQL		= new StringBuffer();

		// ログインユーザー情報取得
		String userId		= userInfo.getId();					// ログインユーザー
		String sendBtnid	= map.get("SENDBTNID");				// 呼出しボタン

		ArrayList<String> prmData = new ArrayList<String>();
		Object[] valueData = new Object[]{};
		String values = "";

		int maxField = 25;		// Fxxの最大値
		for (int k = 1; k <= maxField; k++) {
			String key = "F" + String.valueOf(k);

			if(k == 1){
				values += String.valueOf(0 + 1);

			}

			if(! ArrayUtils.contains(new String[]{"F26, F27, F28, F29"}, key)){
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

		// 予約発注_納入日の登録・更新
		sbSQL = new StringBuffer();
		sbSQL.append(" merge into INAMS.MSTBMN as T using (select");
		sbSQL.append(" BMNCD");														// 部門
		sbSQL.append(", BMNKBN");													// 部門区分
		sbSQL.append(", BMNKN");													// 部門名称（漢字）
		sbSQL.append(", BMNAN");													// 部門名称（カナ）
		sbSQL.append(", ZEIKBN");													// 税区分
		sbSQL.append(", ZEIRTHENKODT");												// 税率変更日
		sbSQL.append(", ZEIRTKBN_OLD");												// 旧税率区分
		sbSQL.append(", ZEIRTKBN");													// 税率区分
		sbSQL.append(", ODBOOKKBN");													// オーダーブック出力区分
		sbSQL.append(", HYOKAKBN");													// 評価方法区分
		sbSQL.append(", GENKART");													// 原価率
		sbSQL.append(", CORPBMNCD");													// 会社部門
		sbSQL.append(", URIBMNCD");													// 売上計上部門
		sbSQL.append(", HOGANBMNCD");												// 包含部門
		sbSQL.append(", JYOGENAM");													// 上限金額
		sbSQL.append(", JYOGENSU");													// 上限数量
		sbSQL.append(", TANAOROTIMKB");												// 棚卸タイミング
		sbSQL.append(", POSBAIHENKBN");												// POS売変対象区分
		sbSQL.append(", KEIHIKBN");													// 経費対象区分
		sbSQL.append(", TANPINKBN");													// 単品管理区分
		sbSQL.append(", DELKIJYUNSU");												// 削除基準日数
		sbSQL.append(", WARIGAIFLG");												// 値引除外フラグ
		sbSQL.append(", ITEMBETSUCD");												// 商品・非商品識別コード
		sbSQL.append(", URISEIGENFLG");												// 販売制限フラグ
		sbSQL.append(", URIKETAKBN");												// 売上金額最大桁数
		sbSQL.append(", "+DefineReport.ValUpdkbn.NML.getVal()+" AS UPDKBN");		// 更新区分
		sbSQL.append(", 0 AS SENDFLG");												// 送信フラグ
		sbSQL.append(", '"+userId+"' AS OPERATOR ");								// オペレーター
		sbSQL.append(", current timestamp AS ADDDT ");								// 登録日
		sbSQL.append(", current timestamp AS UPDDT ");								// 更新日
		sbSQL.append(" from (values "+StringUtils.join(valueData, ",")+") as T1(NUM");
		sbSQL.append(", BMNCD");
		sbSQL.append(", BMNKBN");
		sbSQL.append(", BMNKN");
		sbSQL.append(", BMNAN");
		sbSQL.append(", ZEIKBN");
		sbSQL.append(", ZEIRTHENKODT");
		sbSQL.append(", ZEIRTKBN_OLD");
		sbSQL.append(", ZEIRTKBN");
		sbSQL.append(", ODBOOKKBN");
		sbSQL.append(", HYOKAKBN");
		sbSQL.append(", GENKART");
		sbSQL.append(", CORPBMNCD");
		sbSQL.append(", URIBMNCD");
		sbSQL.append(", HOGANBMNCD");
		sbSQL.append(", JYOGENAM");
		sbSQL.append(", JYOGENSU");
		sbSQL.append(", TANAOROTIMKB");
		sbSQL.append(", POSBAIHENKBN");
		sbSQL.append(", KEIHIKBN");
		sbSQL.append(", TANPINKBN");
		sbSQL.append(", DELKIJYUNSU");
		sbSQL.append(", WARIGAIFLG");
		sbSQL.append(", ITEMBETSUCD");
		sbSQL.append(", URISEIGENFLG");
		sbSQL.append(", URIKETAKBN");
		sbSQL.append(" ))as RE on (T.BMNCD = RE.BMNCD)");
		sbSQL.append(" when matched then update set");
		sbSQL.append("  BMNCD=RE.BMNCD");
		sbSQL.append(", BMNKBN=RE.BMNKBN");
		sbSQL.append(", BMNAN=RE.BMNAN");
		sbSQL.append(", BMNKN=RE.BMNKN");
		sbSQL.append(", ZEIKBN=RE.ZEIKBN");
		sbSQL.append(", ZEIRTHENKODT=RE.ZEIRTHENKODT");
		sbSQL.append(", ZEIRTKBN_OLD=RE.ZEIRTKBN_OLD");
		sbSQL.append(", ZEIRTKBN=RE.ZEIRTKBN");
		sbSQL.append(", ODBOOKKBN=RE.ODBOOKKBN");
		sbSQL.append(", HYOKAKBN=RE.HYOKAKBN");
		sbSQL.append(", GENKART=RE.GENKART");
		sbSQL.append(", CORPBMNCD=RE.CORPBMNCD");
		sbSQL.append(", URIBMNCD=RE.URIBMNCD");
		sbSQL.append(", HOGANBMNCD=RE.HOGANBMNCD");
		sbSQL.append(", JYOGENAM=RE.JYOGENAM");
		sbSQL.append(", JYOGENSU=RE.JYOGENSU");
		sbSQL.append(", TANAOROTIMKB=RE.TANAOROTIMKB");
		sbSQL.append(", POSBAIHENKBN=RE.POSBAIHENKBN");
		sbSQL.append(", KEIHIKBN=RE.KEIHIKBN");
		sbSQL.append(", TANPINKBN=RE.TANPINKBN");
		sbSQL.append(", DELKIJYUNSU=RE.DELKIJYUNSU");
		sbSQL.append(", WARIGAIFLG=RE.WARIGAIFLG");
		sbSQL.append(", ITEMBETSUCD=RE.ITEMBETSUCD");
		sbSQL.append(", URISEIGENFLG=RE.URISEIGENFLG");
		sbSQL.append(", URIKETAKBN=RE.URIKETAKBN");
		sbSQL.append(", UPDKBN=RE.UPDKBN");
		sbSQL.append(", SENDFLG=RE.SENDFLG");
		sbSQL.append(", OPERATOR=RE.OPERATOR");
		if(StringUtils.equals(DefineReport.Button.NEW.getObj(), sendBtnid)){
			//新規登録時には登録日の更新も行う。
			sbSQL.append(", ADDDT=RE.ADDDT");
		}
		sbSQL.append(", UPDDT=RE.UPDDT");
		sbSQL.append(" when not matched then insert (");
		sbSQL.append("  BMNCD");
		sbSQL.append(", BMNKBN");
		sbSQL.append(", BMNAN");
		sbSQL.append(", BMNKN");
		sbSQL.append(", ZEIKBN");
		sbSQL.append(", ZEIRTHENKODT");
		sbSQL.append(", ZEIRTKBN_OLD");
		sbSQL.append(", ZEIRTKBN");
		sbSQL.append(", ODBOOKKBN");
		sbSQL.append(", HYOKAKBN");
		sbSQL.append(", GENKART");
		sbSQL.append(", CORPBMNCD");
		sbSQL.append(", URIBMNCD");
		sbSQL.append(", HOGANBMNCD");
		sbSQL.append(", JYOGENAM");
		sbSQL.append(", JYOGENSU");
		sbSQL.append(", TANAOROTIMKB");
		sbSQL.append(", POSBAIHENKBN");
		sbSQL.append(", KEIHIKBN");
		sbSQL.append(", TANPINKBN");
		sbSQL.append(", DELKIJYUNSU");
		sbSQL.append(", WARIGAIFLG");
		sbSQL.append(", ITEMBETSUCD");
		sbSQL.append(", URISEIGENFLG");
		sbSQL.append(", URIKETAKBN");
		sbSQL.append(", UPDKBN");
		sbSQL.append(", SENDFLG");
		sbSQL.append(", OPERATOR");
		sbSQL.append(", ADDDT");
		sbSQL.append(", UPDDT");
		sbSQL.append(") values (");
		sbSQL.append("  RE.BMNCD");
		sbSQL.append(", RE.BMNKBN");
		sbSQL.append(", RE.BMNAN");
		sbSQL.append(", RE.BMNKN");
		sbSQL.append(", RE.ZEIKBN");
		sbSQL.append(", RE.ZEIRTHENKODT");
		sbSQL.append(", RE.ZEIRTKBN_OLD");
		sbSQL.append(", RE.ZEIRTKBN");
		sbSQL.append(", RE.ODBOOKKBN");
		sbSQL.append(", RE.HYOKAKBN");
		sbSQL.append(", RE.GENKART");
		sbSQL.append(", RE.CORPBMNCD");
		sbSQL.append(", RE.URIBMNCD");
		sbSQL.append(", RE.HOGANBMNCD");
		sbSQL.append(", RE.JYOGENAM");
		sbSQL.append(", RE.JYOGENSU");
		sbSQL.append(", RE.TANAOROTIMKB");
		sbSQL.append(", RE.POSBAIHENKBN");
		sbSQL.append(", RE.KEIHIKBN");
		sbSQL.append(", RE.TANPINKBN");
		sbSQL.append(", RE.DELKIJYUNSU");
		sbSQL.append(", RE.WARIGAIFLG");
		sbSQL.append(", RE.ITEMBETSUCD");
		sbSQL.append(", RE.URISEIGENFLG");
		sbSQL.append(", RE.URIKETAKBN");
		sbSQL.append(", RE.UPDKBN");
		sbSQL.append(", RE.SENDFLG");
		sbSQL.append(", RE.OPERATOR");
		sbSQL.append(", RE.ADDDT");
		sbSQL.append(", RE.UPDDT");
		sbSQL.append(")");

		if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("部門マスタ");

		// クリア
		prmData = new ArrayList<String>();
		valueData = new Object[]{};
		values = "";

		return sbSQL.toString();
	}


	/**
	 * 削除処理実行
	 * @return
	 *
	 * @throws Exception
	 */
	private JSONObject deleteData(HashMap<String, String> map, User userInfo) throws Exception {
		// パラメータ確認
		JSONArray dataArray = JSONArray.fromObject(map.get("DATA"));	// 対象情報

		JSONObject msgObj = new JSONObject();
		JSONArray msg = new JSONArray();

		// ログインユーザー情報取得
		String userId	= userInfo.getId();								// ログインユーザー
		String bmncd	= "";											// 部門コード

		// 更新情報
		JSONObject data = dataArray.getJSONObject(0);
		bmncd = data.optString("F1");

		// 基本INSERT/UPDATE文
		StringBuffer sbSQL;
		ArrayList<String> prmData = new ArrayList<String>();

		// 削除処理：更新区分に"1"（削除）を登録
		sbSQL = new StringBuffer();
		sbSQL.append("UPDATE INAMS.MSTBMN ");
		sbSQL.append("SET ");
		sbSQL.append(" SENDFLG = 0");
		sbSQL.append(",UPDKBN =" + DefineReport.ValUpdkbn.DEL.getVal());
		sbSQL.append(",OPERATOR ='" + userId + "'");
		sbSQL.append(",UPDDT = current timestamp ");
		sbSQL.append(" WHERE BMNCD = ? ");
		prmData.add(bmncd);

		int count = super.executeSQL(sbSQL.toString(), prmData);
		if(StringUtils.isEmpty(getMessage())){
			if (DefineReport.ID_DEBUG_MODE) System.out.println("部門を "+MessageUtility.getMessage(Msg.S00004.getVal(), new String[]{Integer.toString(count)}));
			msgObj.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00002.getVal()));
		}else{
			msgObj.put(MsgKey.E.getKey(), getMessage());
		}
		return msgObj;
	}

	/**
	 * チェック処理
	 *
	 * @throws Exception
	 */
	public JSONArray check(HashMap<String, String> map) {
		// 関連情報取得
		ItemList iL = new ItemList();
		// パラメータ確認
		JSONArray dataArray = JSONArray.fromObject(map.get("DATA"));	// 更新情報

		String sqlcommand			 = "";
		ArrayList<String> paramData	 = new ArrayList<String>();
		String outobj				 = map.get(DefineReport.ID_PARAM_OBJ);		// 実行ボタン
		JSONArray msg				 = new JSONArray();
		MessageUtility mu			 = new MessageUtility();

		// 入力値を取得
		JSONObject data = dataArray.getJSONObject(0);

		String sendBtnid	= map.get("SENDBTNID");			// 呼出しボタン

		// チェック処理
		// 新規登録重複チェック
		if(StringUtils.equals(DefineReport.Button.NEW.getObj(), sendBtnid)){
			if(dataArray.size() > 0){
				data = dataArray.getJSONObject(0);

				paramData	 = new ArrayList<String>();
				paramData.add(data.getString("F1"));
				sqlcommand = "select count(BMNCD) as VALUE from INAMS.MSTBMN where UPDKBN = "+DefineReport.ValUpdkbn.NML.getVal()+" and BMNCD = ?";

				@SuppressWarnings("static-access")
				JSONArray array = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
				if(array.size() > 0 && NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) > 0){
					JSONObject o = mu.getDbMessageObj("E00004", "部門コード");
					msg.add(o);
					return msg;
				}
			}
		}

		// 対象件数チェック
		if(dataArray.size() == 0 || dataArray.getJSONObject(0).isEmpty()){
			msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
			return msg;
		}

		// 入力制限：税区分が1、0の場合、税率区分の入力必須
		if(StringUtils.equals(data.optString("F5"), "0")
		|| StringUtils.equals(data.optString("F5"), "1")){
			if(StringUtils.isEmpty(data.getString("F8"))){
				JSONObject o = mu.getDbMessageObj("E11152");
				msg.add(o);
				return msg;
			}
		}

		// 入力制限：税区分が2の場合、税率区分の登録不可能
		if(StringUtils.equals(data.optString("F5"), "2")){
			if(StringUtils.isNotEmpty(data.getString("F8"))){
				JSONObject o = mu.getDbMessageObj("E11327");
				msg.add(o);
				return msg;
			}
		}

		// 入力制限：部門区分が1以外の場合、会社部門の登録不可能
		if(!StringUtils.equals(data.optString("F2"), "1")){
			if(!StringUtils.isEmpty(data.getString("F12"))){
				JSONObject o = mu.getDbMessageObj("EX1045");
				msg.add(o);
				return msg;
			}
		}

		// 入力制限：部門区分が2以外の場合、売上計上部門の登録不可能
		if(!StringUtils.equals(data.optString("F2"), "2")){
			if(!StringUtils.isEmpty(data.getString("F13"))){
				JSONObject o = mu.getDbMessageObj("EX1046");
				msg.add(o);
				return msg;
			}
		}
		return msg;
	}

	/**
	 * チェック処理(削除)
	 *
	 * @throws Exception
	 */
	@SuppressWarnings("static-access")
	public JSONArray checkDel(HashMap<String, String> map) {
		// 関連情報取得
		ItemList iL = new ItemList();
		// パラメータ確認
		JSONArray dataArray = JSONArray.fromObject(map.get("DATA"));	// 更新情報

		StringBuffer sbSQL = new StringBuffer();
		ArrayList<String> paramData	 = new ArrayList<String>();
		JSONArray msg				 = new JSONArray();

		// 入力値を取得
		JSONObject data = dataArray.getJSONObject(0);

		String bmncd		= data.optString("F1");			// 部門コード

		// 対象件数チェック
		if(dataArray.size() == 0 || dataArray.getJSONObject(0).isEmpty()){
			msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
			return msg;
		}

		// チェック処理
		// 新規登録重複チェック
		// 部門紐付チェック:商品マスタ
		sbSQL = new StringBuffer();
		sbSQL.append("select SHN.SHNCD from INAMS.MSTSHN SHN where SUBSTR(SHNCD,1,2) = RIGHT('00' || "+bmncd+",2) AND (SHN.YOT_BMNCD = "+bmncd+" or SHN.URI_BMNCD = "+bmncd+" or SHN.BMNCD = "+bmncd+") and NVL(SHN.UPDKBN, 0) <> 1 fetch first 1 rows only");
		JSONArray array = iL.selectJSONArray(sbSQL.toString(), null, Defines.STR_JNDI_DS);
		if(array.size() > 0 ){
			msg.add(MessageUtility.getMsg("商品マスタ"));
		}

		// 部門紐付チェック:大分類マスタ
		sbSQL = new StringBuffer();
		sbSQL.append("select DAI.BMNCD from INAMS.MSTDAIBRUI DAI where DAI.BMNCD = "+bmncd+" and NVL(DAI.UPDKBN, 0) <> 1 fetch first 1 rows only");
		array = iL.selectJSONArray(sbSQL.toString(), null, Defines.STR_JNDI_DS);
		if(array.size() > 0 ){
			msg.add(MessageUtility.getMsg("大分類マスタ"));
		}

		// 部門紐付チェック:値付分類_大分類マスタ
		sbSQL = new StringBuffer();
		sbSQL.append("select NEZ.BMNCD from INAMS.MSTDAIBRUI_NEZ NEZ where NEZ.BMNCD = "+bmncd+" and NVL(NEZ.UPDKBN, 0) <> 1 fetch first 1 rows only");
		array = iL.selectJSONArray(sbSQL.toString(), null, Defines.STR_JNDI_DS);
		if(array.size() > 0 ){
			msg.add(MessageUtility.getMsg("値付分類_大分類マスタ"));
		}

		// 部門紐付チェック:売場分類_大分類マスタ
		sbSQL = new StringBuffer();
		sbSQL.append("select URI.BMNCD from INAMS.MSTDAIBRUI_URI URI where URI.BMNCD = "+bmncd+" and NVL(URI.UPDKBN, 0) <> 1 fetch first 1 rows only");
		array = iL.selectJSONArray(sbSQL.toString(), null, Defines.STR_JNDI_DS);
		if(array.size() > 0 ){
			msg.add(MessageUtility.getMsg("売場分類_大分類マスタ"));
		}

		// 部門紐付チェック:用途分類_大分類マスタ
		sbSQL = new StringBuffer();
		sbSQL.append("select YOT.BMNCD from INAMS.MSTDAIBRUI_YOT YOT where YOT.BMNCD = "+bmncd+" and NVL(YOT.UPDKBN, 0) <> 1 fetch first 1 rows only");
		array = iL.selectJSONArray(sbSQL.toString(), null, Defines.STR_JNDI_DS);
		if(array.size() > 0 ){
			msg.add(MessageUtility.getMsg("用途分類_大分類マスタ"));
		}

		if(msg.size() > 0){
			//部門紐付チェック_エラー時
			msg.add(0, MessageUtility.getMsg("下記マスタにて部門コードを使用している為、削除出来ません。\n"));
			return msg;

		}
		return msg;
	}
}
