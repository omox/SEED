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

import authentication.bean.User;
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
public class Reportx031Dao extends ItemDao {

	private static String DECI_DIGITS = ",20,10";

	/**
	 * インスタンスを生成します。
	 * @param source
	 */
	public Reportx031Dao(String JNDIname) {
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
		JSONArray msg = this.check(map);

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
		String bmoncd	 = getMap().get("BUMON");								// 部門コード
		String bunrui	 = getMap().get("BUNRUI");								// 検索条件分類区分
		String tablename = "";													// 検索テーブル名

		// パラメータ確認
		// 必須チェック
		if ( bmoncd == null) {
			System.out.println(super.getConditionLog());
			return "";
		}

		if(StringUtils.equals(DefineReport.OptionBunruiKubun.HYOJUN.getVal(), bunrui)){
			tablename = "INAMS.MSTDAIBRUI";

		}else if(StringUtils.equals(DefineReport.OptionBunruiKubun.URIBA.getVal(), bunrui)){
			tablename = "INAMS.MSTDAIBRUI_URI";

		}else if(StringUtils.equals(DefineReport.OptionBunruiKubun.YOUTO.getVal(), bunrui)){
			tablename = "INAMS.MSTDAIBRUI_YOT";

		}

		// タイトル情報(任意)設定
		List<String> titleList = new ArrayList<String>();


		StringBuffer sbSQL = new StringBuffer();

		sbSQL.append(" select");
		sbSQL.append(" null as DAICD");
		sbSQL.append(", null");
		sbSQL.append(", null");
		sbSQL.append(", null");
		sbSQL.append(", null");
		sbSQL.append(", null");
		sbSQL.append(", null");
		sbSQL.append(", null");
		sbSQL.append(", null");
		sbSQL.append(", null");
		sbSQL.append(", null");
		sbSQL.append(", null");
		sbSQL.append(", 1 as SUPDKBN");
		sbSQL.append(", " + DefineReport.ValUpdkbn.NML.getVal());
		sbSQL.append(", null");
		// TODO 暫定
		sbSQL.append(" from (values ('', '')) as X(value, TEXT)");
		sbSQL.append(" union all");
		sbSQL.append(" select");
		sbSQL.append(" DAI.DAICD");																// F1	：大分類コード
		sbSQL.append(", DAI.DAIBRUIAN");														// F2	：大分類名（カナ）
		sbSQL.append(", DAI.DAIBRUIKN");														// F3	：大分類名（漢字）
		sbSQL.append(", DAI.ATR1");																// F4	：属性1
		sbSQL.append(", DAI.ATR2");																// F5	：属性2
		sbSQL.append(", DAI.ATR3");																// F6	：属性3
		sbSQL.append(", DAI.ATR4");																// F7	：属性4
		sbSQL.append(", DAI.ATR5");																// F8	：属性5
		sbSQL.append(", DAI.ATR6");																// F9	：属性6
		sbSQL.append(", TO_CHAR(DAI.ADDDT, 'yy/mm/dd')");										// F10	：登録日
		sbSQL.append(", TO_CHAR(DAI.UPDDT, 'yy/mm/dd')");										// F11	：更新日
		sbSQL.append(", DAI.OPERATOR");															// F12	：オペレータ
		sbSQL.append(", 0 as SUPDKBN");															// F13	：新規登録区分(非表示)
		sbSQL.append(", DAI.UPDKBN");															// F14	：更新区分
		sbSQL.append(", TO_CHAR(DAI.UPDDT, 'YYYYMMDDHH24MISSNNNNNN') as HDN_UPDDT");			// F15	：更新日(非表示)
		sbSQL.append(" from "+tablename+" DAI");
		sbSQL.append(" where NVL(DAI.UPDKBN, 0) = 0 and DAI.BMNCD = ? ");
		paramData.add(bmoncd);
		sbSQL.append(" order by DAICD, SUPDKBN");
		setParamData(paramData);

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

	}

	boolean isTest = true;

	/** 固定値定義（テーブルタイプ）<br>
	 *   */
	private enum TblType {
		/** 正 */
		SEI(1, "(正)"),
		/** 予約 */
		YYK(2, "(予)"),
		/** ジャーナル */
		JNL(3, "(ジャーナル)");

		private final Integer val;
		private final String txt;
		/** 初期化 */
		private TblType(Integer val, String txt) {
			this.val = val;
			this.txt = txt;
		}
		/** @return val 値 */
		public Integer getVal() { return val; }
		/** @return txt テキスト */
		public String getTxt() { return txt; }

	}

	/** 固定値定義（SQLタイプ）<br>
	 *   */
	private enum SqlType {
		/** INSERT */
		INS(1, "INSERT"),
		/** UPDATE */
		UPD(2, "UPDATE"),
		/** DELETE */
		DEL(3, "DELETE"),
		/** MERGE */
		MRG(4, "MERGE");

		private final Integer val;
		private final String txt;
		/** 初期化 */
		private SqlType(Integer val, String txt) {
			this.val = val;
			this.txt = txt;
		}
		/** @return val 値 */
		public Integer getVal() { return val; }
		/** @return txt テキスト */
		public String getTxt() { return txt; }
	}

	/** SQLリスト保持用変数 */
	ArrayList<String> sqlList = new ArrayList<String>();
	/** SQLのパラメータリスト保持用変数 */
	ArrayList<ArrayList<String>> prmList = new ArrayList<ArrayList<String>>();
	/** SQLログ用のラベルリスト保持用変数 */
	ArrayList<String> lblList = new ArrayList<String>();

	/** 処理対象テーブル名称 大分類*/
	String tableNameDAI = "";
	/** 処理対象テーブル名称 中分類*/
	String tableNameCHU = "";
	/** 処理対象テーブル名称 小分類*/
	String tableNameSHO = "";

	/** 削除データ 中分類*/
	JSONArray DeldataArrayCHU	 = new JSONArray();
	/** 削除データ 小分類*/
	JSONArray DeldataArraySHO	 = new JSONArray();


	/**
	 * 大分類マスタINSERT/UPDATE処理
	 *
	 * @param dataArray
	 * @param tablename
	 * @param map
	 * @param userInfo
	 */
	public String createSqlDai(JSONArray dataArray, String bunrui, HashMap<String, String> map, User userInfo){
		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();

		String updateRows	 = "";											// 更新データ
		String values		 = "";
		Object[] valueData	 = new Object[]{};
		StringBuffer sbSQL	 = new StringBuffer();

		// ログインユーザー情報取得
		String userId	= userInfo.getId();								// ログインユーザー

		String bmoncd	 = map.get("BUMON");							// 入力部門コード
		String daicd	 = "";											// 大分類コード
		String adddt	 = "";											// 登録日
		String updflg	 = "";											// 更新フラグ
		String tablename = "";											// 更新対象テーブル名称

		if(StringUtils.equals(DefineReport.OptionBunruiKubun.HYOJUN.getVal(), bunrui)){
			tablename = "INAMS.MSTDAIBRUI";

		}else if(StringUtils.equals(DefineReport.OptionBunruiKubun.URIBA.getVal(), bunrui)){
			tablename = "INAMS.MSTDAIBRUI_URI";

		}else if(StringUtils.equals(DefineReport.OptionBunruiKubun.YOUTO.getVal(), bunrui)){
			tablename = "INAMS.MSTDAIBRUI_YOT";

		}

		int maxField = 9;		// Fxxの最大値
		int len = dataArray.size();
		for (int i = 0; i < len; i++) {
			JSONObject dataCld = dataArray.getJSONObject(i);
			if(! dataCld.isEmpty()){
				// 削除データは取り込まない。
				if(!StringUtils.equals(DefineReport.ValUpdkbn.DEL.getVal(), dataCld.optString("F14"))){
					for (int k = 1; k <= maxField; k++) {
						String key = "F" + String.valueOf(k);

						if(k == 1){
							values += String.valueOf(0 + 1);

							// 部門コード
							values += ", ?";
							prmData.add(dataCld.optString("F17"));
						}

						if(! ArrayUtils.contains(new String[]{""}, key)){
							String val = dataCld.optString(key);

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
				}
			}

			if(valueData.length >= 100 || (i+1 == len && valueData.length > 0)){
				sbSQL = new StringBuffer();
				sbSQL.append(" merge into "+tablename+" as T using (select");
				sbSQL.append(" BMNCD");														// 部門
				sbSQL.append(", DAICD");													// 大分類
				sbSQL.append(", DAIBRUIAN");												// 大分類名（カナ）
				sbSQL.append(", DAIBRUIKN");												// 大分類名（漢字）
				sbSQL.append(", ATR1");														// 属性1
				sbSQL.append(", ATR2");														// 属性2
				sbSQL.append(", ATR3");														// 属性3
				sbSQL.append(", ATR4");														// 属性4
				sbSQL.append(", ATR5");														// 属性5
				sbSQL.append(", ATR6");														// 属性6
				sbSQL.append(", "+DefineReport.ValUpdkbn.NML.getVal()+" AS UPDKBN");		// 更新区分：
				sbSQL.append(", 0 as SENDFLG");											// 送信フラグ
				sbSQL.append(", '"+userId+"' AS OPERATOR ");								// オペレーター：
				sbSQL.append(", current timestamp AS ADDDT ");								// 登録日：
				sbSQL.append(", current timestamp AS UPDDT ");								// 更新日：
				sbSQL.append(" from (values "+StringUtils.join(valueData, ",")+") as T1(NUM");
				sbSQL.append(", BMNCD");
				sbSQL.append(", DAICD");
				sbSQL.append(", DAIBRUIAN");
				sbSQL.append(", DAIBRUIKN");
				sbSQL.append(", ATR1");
				sbSQL.append(", ATR2");
				sbSQL.append(", ATR3");
				sbSQL.append(", ATR4");
				sbSQL.append(", ATR5");
				sbSQL.append(", ATR6");
				sbSQL.append(" ))as RE on (T.BMNCD = RE.BMNCD and T.DAICD = RE.DAICD)");
				sbSQL.append(" when matched then update set");
				sbSQL.append(" BMNCD=RE.BMNCD");
				sbSQL.append(", DAICD=RE.DAICD");
				sbSQL.append(", DAIBRUIAN=RE.DAIBRUIAN");
				sbSQL.append(", DAIBRUIKN=RE.DAIBRUIKN");
				sbSQL.append(", ATR1=RE.ATR1");
				sbSQL.append(", ATR2=RE.ATR2");
				sbSQL.append(", ATR3=RE.ATR3");
				sbSQL.append(", ATR4=RE.ATR4");
				sbSQL.append(", ATR5=RE.ATR5");
				sbSQL.append(", ATR6=RE.ATR6");
				sbSQL.append(", UPDKBN=RE.UPDKBN");
				sbSQL.append(", SENDFLG=RE.SENDFLG");
				sbSQL.append(", OPERATOR=RE.OPERATOR");
				sbSQL.append(", UPDDT=RE.UPDDT");
				sbSQL.append(", ADDDT = case when NVL(UPDKBN, 0) = 1 then RE.ADDDT else ADDDT end");
				sbSQL.append(" when not matched then insert (");
				sbSQL.append(" BMNCD");
				sbSQL.append(", DAICD");
				sbSQL.append(", DAIBRUIAN");
				sbSQL.append(", DAIBRUIKN");
				sbSQL.append(", ATR1");
				sbSQL.append(", ATR2");
				sbSQL.append(", ATR3");
				sbSQL.append(", ATR4");
				sbSQL.append(", ATR5");
				sbSQL.append(", ATR6");
				sbSQL.append(", UPDKBN");
				sbSQL.append(", SENDFLG");
				sbSQL.append(", OPERATOR");
				sbSQL.append(", ADDDT");
				sbSQL.append(", UPDDT");
				sbSQL.append(") values (");
				sbSQL.append(" RE.BMNCD");
				sbSQL.append(", RE.DAICD");
				sbSQL.append(", RE.DAIBRUIAN");
				sbSQL.append(", RE.DAIBRUIKN");
				sbSQL.append(", RE.ATR1");
				sbSQL.append(", RE.ATR2");
				sbSQL.append(", RE.ATR3");
				sbSQL.append(", RE.ATR4");
				sbSQL.append(", RE.ATR5");
				sbSQL.append(", RE.ATR6");
				sbSQL.append(", RE.UPDKBN");
				sbSQL.append(", RE.SENDFLG");
				sbSQL.append(", RE.OPERATOR");
				sbSQL.append(", RE.ADDDT");
				sbSQL.append(", RE.UPDDT");
				sbSQL.append(")");

				if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

				sqlList.add(sbSQL.toString());
				prmList.add(prmData);
				lblList.add("大分類マスタ");

				// クリア
				prmData = new ArrayList<String>();
				valueData = new Object[]{};
				values = "";
			}
		}
		return sbSQL.toString();
	}

	/**
	 * 中分類マスタINSERT/UPDATE処理
	 *
	 * @param dataArray
	 * @param tablename
	 * @param map
	 * @param userInfo
	 */
	public String createSqlChu(JSONArray dataArray, String bunrui, HashMap<String, String> map, User userInfo){
		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();

		String updateRows	 = "";											// 更新データ
		String values		 = "";
		Object[] valueData	 = new Object[]{};
		StringBuffer sbSQL	 = new StringBuffer();

		// ログインユーザー情報取得
		String userId	= userInfo.getId();								// ログインユーザー

		String bmoncd	 = map.get("BUMON");							// 入力部門コード
		String daicd	 = map.get("DAICD");;							// 大分類コード
		String tablename = "";											// 更新対象テーブル名称

		if(StringUtils.equals(DefineReport.OptionBunruiKubun.HYOJUN.getVal(), bunrui)){
			tablename = "INAMS.MSTCHUBRUI";

		}else if(StringUtils.equals(DefineReport.OptionBunruiKubun.URIBA.getVal(), bunrui)){
			tablename = "INAMS.MSTCHUBRUI_URI";

		}else if(StringUtils.equals(DefineReport.OptionBunruiKubun.YOUTO.getVal(), bunrui)){
			tablename = "INAMS.MSTCHUBRUI_YOT";

		}

		int maxField = 9;		// Fxxの最大値
		int len = dataArray.size();
		for (int i = 0; i < len; i++) {
			JSONObject dataCld = dataArray.getJSONObject(i);
			if(! dataCld.isEmpty()){
				// 削除データは取り込まない。
				if(!StringUtils.equals(DefineReport.ValUpdkbn.DEL.getVal(), dataCld.optString("F14"))){
					for (int k = 1; k <= maxField; k++) {
						String key = "F" + String.valueOf(k);

						if(k == 1){
							values += String.valueOf(0 + 1);

							// 部門コード
							values += ", ?";
							prmData.add(dataCld.optString("F17"));

							// 大分類コード
							values += ", ?";
							prmData.add(dataCld.optString("F18"));
						}

						if(! ArrayUtils.contains(new String[]{""}, key)){
							String val = dataCld.optString(key);

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
				}
			}

			if(valueData.length >= 100 || (i+1 == len && valueData.length > 0)){
				sbSQL = new StringBuffer();
				sbSQL.append(" merge into "+tablename+" as T using (select");
				sbSQL.append(" BMNCD");														// 部門
				sbSQL.append(", DAICD");													// 大分類
				sbSQL.append(", CHUCD");													// 中分類
				sbSQL.append(", CHUBRUIAN");												// 大分類名（カナ）
				sbSQL.append(", CHUBRUIKN");												// 大分類名（漢字）
				sbSQL.append(", ATR1");														// 属性1
				sbSQL.append(", ATR2");														// 属性2
				sbSQL.append(", ATR3");														// 属性3
				sbSQL.append(", ATR4");														// 属性4
				sbSQL.append(", ATR5");														// 属性5
				sbSQL.append(", ATR6");														// 属性6
				sbSQL.append(", "+DefineReport.ValUpdkbn.NML.getVal()+" AS UPDKBN");		// 更新区分：
				sbSQL.append(", 0 as SENDFLG");											// 送信フラグ
				sbSQL.append(", '"+userId+"' AS OPERATOR ");								// オペレーター：
				sbSQL.append(", current timestamp AS ADDDT ");								// 登録日：
				sbSQL.append(", current timestamp AS UPDDT ");								// 更新日：
				sbSQL.append(" from (values "+StringUtils.join(valueData, ",")+") as T1(NUM");
				sbSQL.append(", BMNCD");
				sbSQL.append(", DAICD");
				sbSQL.append(", CHUCD");
				sbSQL.append(", CHUBRUIAN");
				sbSQL.append(", CHUBRUIKN");
				sbSQL.append(", ATR1");
				sbSQL.append(", ATR2");
				sbSQL.append(", ATR3");
				sbSQL.append(", ATR4");
				sbSQL.append(", ATR5");
				sbSQL.append(", ATR6");
				sbSQL.append(" ))as RE on (T.BMNCD = RE.BMNCD and T.DAICD = RE.DAICD and T.CHUCD = RE.CHUCD)");
				sbSQL.append(" when matched then update set");
				sbSQL.append(" BMNCD=RE.BMNCD");
				sbSQL.append(", DAICD=RE.DAICD");
				sbSQL.append(", CHUCD=RE.CHUCD");
				sbSQL.append(", CHUBRUIAN=RE.CHUBRUIAN");
				sbSQL.append(", CHUBRUIKN=RE.CHUBRUIKN");
				sbSQL.append(", ATR1=RE.ATR1");
				sbSQL.append(", ATR2=RE.ATR2");
				sbSQL.append(", ATR3=RE.ATR3");
				sbSQL.append(", ATR4=RE.ATR4");
				sbSQL.append(", ATR5=RE.ATR5");
				sbSQL.append(", ATR6=RE.ATR6");
				sbSQL.append(", UPDKBN=RE.UPDKBN");
				sbSQL.append(", SENDFLG=RE.SENDFLG");
				sbSQL.append(", OPERATOR=RE.OPERATOR");
				sbSQL.append(", UPDDT=RE.UPDDT");
				sbSQL.append(", ADDDT = case when NVL(UPDKBN, 0) = 1 then RE.ADDDT else ADDDT end");
				sbSQL.append(" when not matched then insert (");
				sbSQL.append(" BMNCD");
				sbSQL.append(", DAICD");
				sbSQL.append(", CHUCD");
				sbSQL.append(", CHUBRUIAN");
				sbSQL.append(", CHUBRUIKN");
				sbSQL.append(", ATR1");
				sbSQL.append(", ATR2");
				sbSQL.append(", ATR3");
				sbSQL.append(", ATR4");
				sbSQL.append(", ATR5");
				sbSQL.append(", ATR6");
				sbSQL.append(", UPDKBN");
				sbSQL.append(", SENDFLG");
				sbSQL.append(", OPERATOR");
				sbSQL.append(", ADDDT");
				sbSQL.append(", UPDDT");
				sbSQL.append(") values (");
				sbSQL.append(" RE.BMNCD");
				sbSQL.append(", RE.DAICD");
				sbSQL.append(", RE.CHUCD");
				sbSQL.append(", RE.CHUBRUIAN");
				sbSQL.append(", RE.CHUBRUIKN");
				sbSQL.append(", RE.ATR1");
				sbSQL.append(", RE.ATR2");
				sbSQL.append(", RE.ATR3");
				sbSQL.append(", RE.ATR4");
				sbSQL.append(", RE.ATR5");
				sbSQL.append(", RE.ATR6");
				sbSQL.append(", RE.UPDKBN");
				sbSQL.append(", RE.SENDFLG");
				sbSQL.append(", RE.OPERATOR");
				sbSQL.append(", RE.ADDDT");
				sbSQL.append(", RE.UPDDT");
				sbSQL.append(")");

				if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

				sqlList.add(sbSQL.toString());
				prmList.add(prmData);
				lblList.add("中分類マスタ");

				// クリア
				prmData = new ArrayList<String>();
				valueData = new Object[]{};
				values = "";
			}
		}
		return sbSQL.toString();
	}

	/**
	 * 小分類マスタINSERT/UPDATE処理
	 *
	 * @param dataArray
	 * @param tablename
	 * @param map
	 * @param userInfo
	 */
	public String createSqlSho(JSONArray dataArray, String bunrui, HashMap<String, String> map, User userInfo){
		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();

		String updateRows = "";											// 更新データ
		String values		 = "";
		Object[] valueData	 = new Object[]{};
		StringBuffer sbSQL	 = new StringBuffer();

		// ログインユーザー情報取得
		String userId	= userInfo.getId();								// ログインユーザー

		String bmoncd	 = map.get("BUMON");							// 入力部門コード
		String daicd	 = map.get("DAICD");							// 入力大分類コード
		String chucd	 = map.get("CHUCD");							// 入力中分類コード
		String shocd	 = "";											// 入力小分類コード
		String adddt	 = "";											// 登録日
		String updflg	 = "";											// 更新フラグ
		String supdate	 = "";											// 新規登録フラグ
		String tablename = "";											// 更新対象テーブル名称

		if(StringUtils.equals(DefineReport.OptionBunruiKubun.HYOJUN.getVal(), bunrui)){
			tablename = "INAMS.MSTSHOBRUI";

		}else if(StringUtils.equals(DefineReport.OptionBunruiKubun.URIBA.getVal(), bunrui)){
			tablename = "INAMS.MSTSHOBRUI_URI";

		}else if(StringUtils.equals(DefineReport.OptionBunruiKubun.YOUTO.getVal(), bunrui)){
			tablename = "INAMS.MSTSHOBRUI_YOT";

		}

		int maxField = 9;		// Fxxの最大値
		int len = dataArray.size();
		for (int i = 0; i < len; i++) {
			JSONObject dataCld = dataArray.getJSONObject(i);
			if(! dataCld.isEmpty()){
				// 削除データは取り込まない。
				if(!StringUtils.equals(DefineReport.ValUpdkbn.DEL.getVal(), dataCld.optString("F14"))){
					for (int k = 1; k <= maxField; k++) {
						String key = "F" + String.valueOf(k);

						if(k == 1){
							values += String.valueOf(0 + 1);

							// 共通項目：部門コード
							values += ", ?";
							prmData.add(dataCld.optString("F17"));

							// 共通項目：大分類コード
							values += ", ?";
							prmData.add(dataCld.optString("F18"));

							// 共通項目：小分類コード
							values += ", ?";
							prmData.add(dataCld.optString("F19"));
						}

						if(! ArrayUtils.contains(new String[]{""}, key)){
							String val = dataCld.optString(key);

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
				}
			}

			if(valueData.length >= 100 || (i+1 == len && valueData.length > 0)){
				sbSQL = new StringBuffer();
				sbSQL.append(" merge into "+tablename+" as T using (select");
				sbSQL.append(" BMNCD");														// 部門
				sbSQL.append(", DAICD");													// 大分類
				sbSQL.append(", CHUCD");													// 中分類
				sbSQL.append(", SHOCD");													// 小分類
				sbSQL.append(", SHOBRUIAN");												// 大分類名（カナ）
				sbSQL.append(", SHOBRUIKN");												// 大分類名（漢字）
				sbSQL.append(", ATR1");														// 属性1
				sbSQL.append(", ATR2");														// 属性2
				sbSQL.append(", ATR3");														// 属性3
				sbSQL.append(", ATR4");														// 属性4
				sbSQL.append(", ATR5");														// 属性5
				sbSQL.append(", ATR6");														// 属性6
				sbSQL.append(", "+DefineReport.ValUpdkbn.NML.getVal()+" AS UPDKBN");		// 更新区分：
				sbSQL.append(", 0 as SENDFLG");											// 送信フラグ
				sbSQL.append(", '"+userId+"' AS OPERATOR ");								// オペレーター：
				sbSQL.append(", current timestamp AS ADDDT ");								// 登録日：
				sbSQL.append(", current timestamp AS UPDDT ");								// 更新日：
				sbSQL.append(" from (values "+StringUtils.join(valueData, ",")+") as T1(NUM");
				sbSQL.append(", BMNCD");
				sbSQL.append(", DAICD");
				sbSQL.append(", CHUCD");
				sbSQL.append(", SHOCD");
				sbSQL.append(", SHOBRUIAN");
				sbSQL.append(", SHOBRUIKN");
				sbSQL.append(", ATR1");
				sbSQL.append(", ATR2");
				sbSQL.append(", ATR3");
				sbSQL.append(", ATR4");
				sbSQL.append(", ATR5");
				sbSQL.append(", ATR6");
				sbSQL.append(" ))as RE on (T.BMNCD = RE.BMNCD and T.DAICD = RE.DAICD and T.CHUCD = RE.CHUCD and T.SHOCD = RE.SHOCD)");
				sbSQL.append(" when matched then update set");
				sbSQL.append(" BMNCD=RE.BMNCD");
				sbSQL.append(", DAICD=RE.DAICD");
				sbSQL.append(", CHUCD=RE.CHUCD");
				sbSQL.append(", SHOCD=RE.SHOCD");
				sbSQL.append(", SHOBRUIAN=RE.SHOBRUIAN");
				sbSQL.append(", SHOBRUIKN=RE.SHOBRUIKN");
				sbSQL.append(", ATR1=RE.ATR1");
				sbSQL.append(", ATR2=RE.ATR2");
				sbSQL.append(", ATR3=RE.ATR3");
				sbSQL.append(", ATR4=RE.ATR4");
				sbSQL.append(", ATR5=RE.ATR5");
				sbSQL.append(", ATR6=RE.ATR6");
				sbSQL.append(", UPDKBN=RE.UPDKBN");
				sbSQL.append(", SENDFLG=RE.SENDFLG");
				sbSQL.append(", OPERATOR=RE.OPERATOR");
				sbSQL.append(", UPDDT=RE.UPDDT");
				sbSQL.append(", ADDDT = case when NVL(UPDKBN, 0) = 1 then RE.ADDDT else ADDDT end");
				sbSQL.append(" when not matched then insert (");
				sbSQL.append(" BMNCD");
				sbSQL.append(", DAICD");
				sbSQL.append(", CHUCD");
				sbSQL.append(", SHOCD");
				sbSQL.append(", SHOBRUIAN");
				sbSQL.append(", SHOBRUIKN");
				sbSQL.append(", ATR1");
				sbSQL.append(", ATR2");
				sbSQL.append(", ATR3");
				sbSQL.append(", ATR4");
				sbSQL.append(", ATR5");
				sbSQL.append(", ATR6");
				sbSQL.append(", UPDKBN");
				sbSQL.append(", SENDFLG");
				sbSQL.append(", OPERATOR");
				sbSQL.append(", ADDDT");
				sbSQL.append(", UPDDT");
				sbSQL.append(") values (");
				sbSQL.append(" RE.BMNCD");
				sbSQL.append(", RE.DAICD");
				sbSQL.append(", RE.CHUCD");
				sbSQL.append(", RE.SHOCD");
				sbSQL.append(", RE.SHOBRUIAN");
				sbSQL.append(", RE.SHOBRUIKN");
				sbSQL.append(", RE.ATR1");
				sbSQL.append(", RE.ATR2");
				sbSQL.append(", RE.ATR3");
				sbSQL.append(", RE.ATR4");
				sbSQL.append(", RE.ATR5");
				sbSQL.append(", RE.ATR6");
				sbSQL.append(", RE.UPDKBN");
				sbSQL.append(", RE.SENDFLG");
				sbSQL.append(", RE.OPERATOR");
				sbSQL.append(", RE.ADDDT");
				sbSQL.append(", RE.UPDDT");
				sbSQL.append(")");

				if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

				sqlList.add(sbSQL.toString());
				prmList.add(prmData);
				lblList.add("小分類マスタ");

				// クリア
				prmData = new ArrayList<String>();
				valueData = new Object[]{};
				values = "";
			}
		}
		return sbSQL.toString();
	}

	/**
	 * 小小分類マスタINSERT/UPDATE処理
	 *
	 * @param dataArray
	 * @param tablename
	 * @param map
	 * @param userInfo
	 */
	public String createSqlSsho(JSONArray dataArray, String bunrui, HashMap<String, String> map, User userInfo){
		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();

		String updateRows = "";											// 更新データ

		// ログインユーザー情報取得
		String userId	= userInfo.getId();								// ログインユーザー

		String bmoncd	 = map.get("BUMON");							// 入力部門コード
		String daicd	 = map.get("DAICD");							// 入力大分類コード
		String chucd	 = map.get("CHUCD");							// 入力中分類コード
		String shocd	 = map.get("SHOCD");;							// 入力小分類コード
		String sshocd	 = "";											// 入力小小分類コード
		String adddt	 = "";											// 登録日
		String updflg	 = "";											// 更新フラグ
		String supdate	 = "";											// 新規登録フラグ

		for (int i = 0; i < dataArray.size(); i++) {
			JSONObject data = dataArray.getJSONObject(i);
			if(data.isEmpty()){
				continue;
			}

			sshocd	 = data.optString("F1");
			supdate	 = data.optString("F13");
			updflg = data.optString("F14");
			String values = "";

			if(isTest){
				values += bmoncd +",";
				values += daicd +",";
				values += chucd +",";
				values += shocd +",";
				values += StringUtils.defaultIfEmpty(data.optString("F1"), "null")+",";
				values += StringUtils.defaultIfEmpty("'"+data.optString("F2")+"'", "null")+",";
				values += StringUtils.defaultIfEmpty("'"+data.optString("F3")+"'", "null")+",";
				values += StringUtils.defaultIfEmpty(data.optString("F4"), "null")+",";
				values += StringUtils.defaultIfEmpty(data.optString("F5"), "null")+",";
				values += StringUtils.defaultIfEmpty(data.optString("F6"), "null")+",";
				values += StringUtils.defaultIfEmpty(data.optString("F7"), "null")+",";
				values += StringUtils.defaultIfEmpty(data.optString("F8"), "null")+",";
				values += StringUtils.defaultIfEmpty(data.optString("F9"), "null")+",";
				values += updflg+",";;
				values += "null,";
				values += "null,";
				values += "null,";
				values += "null";

			}else{
				for (int j = 0; j < data.size(); j++) {
					values += ", ?";
				}
				values = StringUtils.removeStart(values, ",");

				for (String prm: values.split(",", 0)){
					prmData.add(prm);
				}
			}

			// 削除処理
			if(StringUtils.equals(updflg, DefineReport.ValUpdkbn.DEL.getVal())){
				/*JSONArray msgdl = this.deleteChildDataDai(map, data);
				if(msgdl.size() > 0){
				}*/
			}

			if(!StringUtils.isEmpty(sshocd)){
				// 未入力新規登録データを省く
				updateRows += ",("+values+")";
			}
		}
		updateRows = StringUtils.removeStart(updateRows, ",");

		StringBuffer sbSQL;
		sbSQL = new StringBuffer();
		sbSQL.append("merge into INAMS.MSTSSHOBRUI as T using (select");
		sbSQL.append(" T1.BMNCD");
		sbSQL.append(", T1.DAICD");
		sbSQL.append(", T1.CHUCD");
		sbSQL.append(", T1.SHOCD");
		sbSQL.append(", T1.SSHOCD");
		sbSQL.append(", T1.SSHOBRUIAN");
		sbSQL.append(", T1.SSHOBRUIKN");
		sbSQL.append(", T1.ATR1");
		sbSQL.append(", T1.ATR2");
		sbSQL.append(", T1.ATR3");
		sbSQL.append(", T1.ATR4");
		sbSQL.append(", T1.ATR5");
		sbSQL.append(", T1.ATR6");
		sbSQL.append(", T1.UPDKBN");
		sbSQL.append(", T1.SENDFLG");
		sbSQL.append(", '"+userId+"' as OPERATOR");
		sbSQL.append(", current timestamp as ADDDT");
		sbSQL.append(", current timestamp as UPDDT");
		sbSQL.append(" from (values "+updateRows+") as T1(");
		sbSQL.append(" BMNCD");
		sbSQL.append(", DAICD");
		sbSQL.append(", CHUCD");
		sbSQL.append(", SHOCD");
		sbSQL.append(", SSHOCD");
		sbSQL.append(", SSHOBRUIAN");
		sbSQL.append(", SSHOBRUIKN");
		sbSQL.append(", ATR1");
		sbSQL.append(", ATR2");
		sbSQL.append(", ATR3");
		sbSQL.append(", ATR4");
		sbSQL.append(", ATR5");
		sbSQL.append(", ATR6");
		sbSQL.append(", UPDKBN");
		sbSQL.append(", SENDFLG");
		sbSQL.append(", OPERATOR");
		sbSQL.append(", ADDDT");
		sbSQL.append(", UPDDT)) as RE on T.BMNCD = RE.BMNCD and T.DAICD = RE.DAICD and T.CHUCD = RE.CHUCD and T.SHOCD = RE.SHOCD and T.SSHOCD = RE.SSHOCD");
		sbSQL.append(" when matched then update set");
		sbSQL.append(" BMNCD = RE.BMNCD");
		sbSQL.append(", DAICD = RE.DAICD");
		sbSQL.append(", CHUCD = RE.CHUCD");
		sbSQL.append(", SHOCD = RE.SHOCD");
		sbSQL.append(", SSHOCD = RE.SSHOCD");
		sbSQL.append(", SSHOBRUIAN = RE.SSHOBRUIAN");
		sbSQL.append(", SSHOBRUIKN = RE.SSHOBRUIKN");
		sbSQL.append(", ATR1 = RE.ATR1");
		sbSQL.append(", ATR2 = RE.ATR2");
		sbSQL.append(", ATR3 = RE.ATR3");
		sbSQL.append(", ATR4 = RE.ATR4");
		sbSQL.append(", ATR5 = RE.ATR5");
		sbSQL.append(", ATR6 = RE.ATR6");
		sbSQL.append(", UPDKBN = RE.UPDKBN");
		sbSQL.append(", SENDFLG = RE.SENDFLG");
		sbSQL.append(", OPERATOR = RE.OPERATOR");
		sbSQL.append( 	adddt);
		sbSQL.append(", UPDDT = RE.UPDDT");
		sbSQL.append(" when not matched then insert values (");
		sbSQL.append(" RE.BMNCD");
		sbSQL.append(", RE.DAICD");
		sbSQL.append(", RE.CHUCD");
		sbSQL.append(", RE.SHOCD");
		sbSQL.append(", RE.SSHOCD");
		sbSQL.append(", RE.SSHOBRUIAN");
		sbSQL.append(", RE.SSHOBRUIKN");
		sbSQL.append(", RE.ATR1");
		sbSQL.append(", RE.ATR2");
		sbSQL.append(", RE.ATR3");
		sbSQL.append(", RE.ATR4");
		sbSQL.append(", RE.ATR5");
		sbSQL.append(", RE.ATR6");
		sbSQL.append(", RE.UPDKBN");
		sbSQL.append(", RE.SENDFLG");
		sbSQL.append(", RE.OPERATOR");
		sbSQL.append(", RE.ADDDT");
		sbSQL.append(", RE.UPDDT)");

		if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("小小分類マスタ");

		return sbSQL.toString();
	}

	/**
	 * 大分類マスタDELETE処理
	 *
	 * @param dataArray
	 * @param tablename
	 * @param map
	 * @param userInfo
	 */
	public String createDelSqlDai(JSONArray dataArray, String bunrui, HashMap<String, String> map, User userInfo){
		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();

		String updateRows	 = "";											// 更新データ
		String values		 = "";
		Object[] valueData	 = new Object[]{};
		StringBuffer sbSQL	 = new StringBuffer();

		// ログインユーザー情報取得
		String userId	= userInfo.getId();								// ログインユーザー

		String bmoncd	 = map.get("BUMON");							// 入力部門コード
		String daicd	 = "";											// 大分類コード
		String adddt	 = "";											// 登録日
		String updflg	 = "";											// 更新フラグ
		String tablename = "";											// 更新対象テーブル名称

		if(StringUtils.equals(DefineReport.OptionBunruiKubun.HYOJUN.getVal(), bunrui)){
			tablename = "INAMS.MSTDAIBRUI";

		}else if(StringUtils.equals(DefineReport.OptionBunruiKubun.URIBA.getVal(), bunrui)){
			tablename = "INAMS.MSTDAIBRUI_URI";

		}else if(StringUtils.equals(DefineReport.OptionBunruiKubun.YOUTO.getVal(), bunrui)){
			tablename = "INAMS.MSTDAIBRUI_YOT";

		}

		int maxField = 1;		// Fxxの最大値
		int len = dataArray.size();
		for (int i = 0; i < len; i++) {
			JSONObject dataCld = dataArray.getJSONObject(i);


			if(! dataCld.isEmpty()){
				if(StringUtils.equals(DefineReport.ValUpdkbn.DEL.getVal(), dataCld.optString("F14"))){
					for (int k = 1; k <= maxField; k++) {
						String key = "F" + String.valueOf(k);

						if(k == 1){
							values += String.valueOf(0 + 1);

							// 部門コード
							values += ", ?";
							prmData.add(dataCld.optString("F17"));
						}

						if(! ArrayUtils.contains(new String[]{""}, key)){
							String val = dataCld.optString(key);

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
					// 子要素の削除
					this.deleteChildDataDai(map, userInfo, bunrui, dataCld);
				}
			}

			if(valueData.length >= 100 || (i+1 == len && valueData.length > 0)){
				sbSQL = new StringBuffer();
				sbSQL.append(" merge into "+tablename+" as T using (select");
				sbSQL.append(" BMNCD");														// 部門
				sbSQL.append(", DAICD");													// 大分類
				sbSQL.append(", "+DefineReport.ValUpdkbn.DEL.getVal()+" AS UPDKBN");		// 更新区分：
				sbSQL.append(", 0 as SENDFLG");												// 送信フラグ
				sbSQL.append(", '"+userId+"' AS OPERATOR ");								// オペレーター：
				sbSQL.append(", current timestamp AS UPDDT ");								// 更新日：
				sbSQL.append(" from (values "+StringUtils.join(valueData, ",")+") as T1(NUM");
				sbSQL.append(", BMNCD");
				sbSQL.append(", DAICD");
				sbSQL.append(" ))as RE on (T.BMNCD = RE.BMNCD and T.DAICD = RE.DAICD)");
				sbSQL.append(" when matched then update set");
				sbSQL.append(" BMNCD=RE.BMNCD");
				sbSQL.append(", DAICD=RE.DAICD");
				sbSQL.append(", UPDKBN=RE.UPDKBN");
				sbSQL.append(", SENDFLG=RE.SENDFLG");
				sbSQL.append(", OPERATOR=RE.OPERATOR");
				sbSQL.append(", UPDDT=RE.UPDDT");

				if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

				sqlList.add(sbSQL.toString());
				prmList.add(prmData);
				lblList.add("大分類マスタ");

				// クリア
				prmData = new ArrayList<String>();
				valueData = new Object[]{};
				values = "";
			}
		}
		return sbSQL.toString();
	}

	/**
	 * 中分類マスタDELETE処理
	 *
	 * @param dataArray
	 * @param tablename
	 * @param map
	 * @param userInfo
	 */
	public String createDelSqlChu(JSONArray dataArray, String bunrui, HashMap<String, String> map, User userInfo){
		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();

		String updateRows	 = "";											// 更新データ
		String values		 = "";
		Object[] valueData	 = new Object[]{};
		StringBuffer sbSQL	 = new StringBuffer();
		JSONArray DeldataArray	 = new JSONArray();

		// ログインユーザー情報取得
		String userId	= userInfo.getId();								// ログインユーザー

		String bmoncd	 = map.get("BUMON");							// 入力部門コード
		String daicd	 = map.get("DAICD");;							// 大分類コード
		String tablename = "";											// 更新対象テーブル名称

		if(StringUtils.equals(DefineReport.OptionBunruiKubun.HYOJUN.getVal(), bunrui)){
			tablename = "INAMS.MSTCHUBRUI";

		}else if(StringUtils.equals(DefineReport.OptionBunruiKubun.URIBA.getVal(), bunrui)){
			tablename = "INAMS.MSTCHUBRUI_URI";

		}else if(StringUtils.equals(DefineReport.OptionBunruiKubun.YOUTO.getVal(), bunrui)){
			tablename = "INAMS.MSTCHUBRUI_YOT";

		}

		int maxField = 1;		// Fxxの最大値
		int len = dataArray.size();
		for (int i = 0; i < len; i++) {
			JSONObject dataCld = dataArray.getJSONObject(i);

			if(! dataCld.isEmpty()){
				if(StringUtils.equals(DefineReport.ValUpdkbn.DEL.getVal(), dataCld.optString("F14"))){
					for (int k = 1; k <= maxField; k++) {
						String key = "F" + String.valueOf(k);

						if(k == 1){
							values += String.valueOf(0 + 1);

							// 部門コード
							values += ", ?";
							prmData.add(dataCld.optString("F17"));

							// 大分類コード
							values += ", ?";
							prmData.add(dataCld.optString("F18"));
						}

						if(! ArrayUtils.contains(new String[]{""}, key)){
							String val = dataCld.optString(key);

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
					// 子要素の削除
					this.deleteChildDataChu(map, userInfo, bunrui, dataCld);

					// 削除する中分類コードの件数を保持
					JSONObject obj = new JSONObject();
					obj.put("TABLENAME", tablename);
					obj.put("BMNCD", dataCld.optString("F17"));
					obj.put("DAICD", dataCld.optString("F18"));
					obj.put("COUNT", 1);

					JSONObject Dletedata = new JSONObject();

					if(DeldataArrayCHU.size() > 0){
						for (int l = 0; l < DeldataArrayCHU.size(); l++) {
							Dletedata = DeldataArrayCHU.getJSONObject(l);
							if( StringUtils.equals(Dletedata.optString("TABLENAME"), tablename)
								&& StringUtils.equals(Dletedata.optString("BMNCD"), dataCld.optString("F17"))
								&& StringUtils.equals(Dletedata.optString("DAICD"), dataCld.optString("F18"))){

								DeldataArrayCHU.getJSONObject(l).put("COUNT", (Integer.parseInt(Dletedata.optString("COUNT")) + 1));
							}else{
								DeldataArrayCHU.add(obj);
							}
						}
					}else{
						DeldataArrayCHU.add(obj);
					}
				}
			}

			if(valueData.length >= 100 || (i+1 == len && valueData.length > 0)){
				sbSQL = new StringBuffer();
				sbSQL.append(" merge into "+tablename+" as T using (select");
				sbSQL.append(" BMNCD");														// 部門
				sbSQL.append(", DAICD");													// 大分類
				sbSQL.append(", CHUCD");													// 中分類
				sbSQL.append(", "+DefineReport.ValUpdkbn.DEL.getVal()+" AS UPDKBN");		// 更新区分：
				sbSQL.append(", 0 as SENDFLG");												// 送信フラグ
				sbSQL.append(", '"+userId+"' AS OPERATOR ");								// オペレーター：
				sbSQL.append(", current timestamp AS UPDDT ");								// 更新日：
				sbSQL.append(" from (values "+StringUtils.join(valueData, ",")+") as T1(NUM");
				sbSQL.append(", BMNCD");
				sbSQL.append(", DAICD");
				sbSQL.append(", CHUCD");
				sbSQL.append(" ))as RE on (T.BMNCD = RE.BMNCD and T.DAICD = RE.DAICD and T.CHUCD = RE.CHUCD)");
				sbSQL.append(" when matched then update set");
				sbSQL.append(" BMNCD=RE.BMNCD");
				sbSQL.append(", DAICD=RE.DAICD");
				sbSQL.append(", CHUCD=RE.CHUCD");
				sbSQL.append(", UPDKBN=RE.UPDKBN");
				sbSQL.append(", SENDFLG=RE.SENDFLG");
				sbSQL.append(", OPERATOR=RE.OPERATOR");
				sbSQL.append(", UPDDT=RE.UPDDT");

				if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

				sqlList.add(sbSQL.toString());
				prmList.add(prmData);
				lblList.add("中分類マスタ");

				// クリア
				prmData = new ArrayList<String>();
				valueData = new Object[]{};
				values = "";
			}
		}
		return sbSQL.toString();
	}

	/**
	 * 小分類マスタINSERT/UPDATE処理
	 *
	 * @param dataArray
	 * @param tablename
	 * @param map
	 * @param userInfo
	 */
	public String createDelSqlSho(JSONArray dataArray, String bunrui, HashMap<String, String> map, User userInfo){
		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();

		String updateRows = "";											// 更新データ
		String values		 = "";
		Object[] valueData	 = new Object[]{};
		StringBuffer sbSQL	 = new StringBuffer();

		// ログインユーザー情報取得
		String userId	= userInfo.getId();								// ログインユーザー

		String bmoncd	 = map.get("BUMON");							// 入力部門コード
		String daicd	 = map.get("DAICD");							// 入力大分類コード
		String chucd	 = map.get("CHUCD");							// 入力中分類コード
		String shocd	 = "";											// 入力小分類コード
		String adddt	 = "";											// 登録日
		String updflg	 = "";											// 更新フラグ
		String supdate	 = "";											// 新規登録フラグ
		String tablename = "";											// 更新対象テーブル名称

		if(StringUtils.equals(DefineReport.OptionBunruiKubun.HYOJUN.getVal(), bunrui)){
			tablename = "INAMS.MSTSHOBRUI";

		}else if(StringUtils.equals(DefineReport.OptionBunruiKubun.URIBA.getVal(), bunrui)){
			tablename = "INAMS.MSTSHOBRUI_URI";

		}else if(StringUtils.equals(DefineReport.OptionBunruiKubun.YOUTO.getVal(), bunrui)){
			tablename = "INAMS.MSTSHOBRUI_YOT";

		}

		int maxField = 1;		// Fxxの最大値
		int len = dataArray.size();
		for (int i = 0; i < len; i++) {
			JSONObject dataCld = dataArray.getJSONObject(i);
			// 削除データのみを取り込む
			if(! dataCld.isEmpty()){
				if(StringUtils.equals(DefineReport.ValUpdkbn.DEL.getVal(), dataCld.optString("F14"))){

					for (int k = 1; k <= maxField; k++) {
						String key = "F" + String.valueOf(k);

						if(k == 1){
							values += String.valueOf(0 + 1);

							// 共通項目：部門コード
							values += ", ?";
							prmData.add(dataCld.optString("F17"));

							// 共通項目：大分類コード
							values += ", ?";
							prmData.add(dataCld.optString("F18"));

							// 共通項目：中分類コード
							values += ", ?";
							prmData.add(dataCld.optString("F19"));
						}

						if(! ArrayUtils.contains(new String[]{""}, key)){
							String val = dataCld.optString(key);

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
					// 子要素の削除
					//this.deleteChildDataSho(map, userInfo, bunrui, dataCld);
					// 削除する小分類コードの件数を保持
					this.addDleteDate(dataCld, tablename);
				}
			}

			if(valueData.length >= 100 || (i+1 == len && valueData.length > 0)){
				sbSQL = new StringBuffer();
				sbSQL.append(" merge into "+tablename+" as T using (select");
				sbSQL.append(" BMNCD");														// 部門
				sbSQL.append(", DAICD");													// 大分類
				sbSQL.append(", CHUCD");													// 中分類
				sbSQL.append(", SHOCD");													// 小分類
				sbSQL.append(", "+DefineReport.ValUpdkbn.DEL.getVal()+" AS UPDKBN");		// 更新区分：
				sbSQL.append(", 0 as SENDFLG");												// 送信フラグ
				sbSQL.append(", '"+userId+"' AS OPERATOR ");								// オペレーター：
				sbSQL.append(", current timestamp AS UPDDT ");								// 登録日：
				sbSQL.append(" from (values "+StringUtils.join(valueData, ",")+") as T1(NUM");
				sbSQL.append(", BMNCD");
				sbSQL.append(", DAICD");
				sbSQL.append(", CHUCD");
				sbSQL.append(", SHOCD");
				sbSQL.append(" ))as RE on (T.BMNCD = RE.BMNCD and T.DAICD = RE.DAICD and T.CHUCD = RE.CHUCD and T.SHOCD = RE.SHOCD)");
				sbSQL.append(" when matched then update set");
				sbSQL.append(" BMNCD=RE.BMNCD");
				sbSQL.append(", DAICD=RE.DAICD");
				sbSQL.append(", CHUCD=RE.CHUCD");
				sbSQL.append(", SHOCD=RE.SHOCD");
				sbSQL.append(", UPDKBN=RE.UPDKBN");
				sbSQL.append(", SENDFLG=RE.SENDFLG");
				sbSQL.append(", OPERATOR=RE.OPERATOR");
				sbSQL.append(", UPDDT=RE.UPDDT");

				if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

				sqlList.add(sbSQL.toString());
				prmList.add(prmData);
				lblList.add("小分類マスタ");

				// クリア
				prmData = new ArrayList<String>();
				valueData = new Object[]{};
				values = "";
			}
		}
		return sbSQL.toString();
	}


	/**
	 * 更新処理実行
	 * @return
	 *
	 * @throws Exception
	 */
	private JSONObject updateData(HashMap<String, String> map, User userInfo) throws Exception {

		// パラメータ確認
		JSONObject dataArray = JSONObject.fromObject(map.get("DATA"));	// 対象情報

		JSONArray dataArrayDai	 = dataArray.getJSONArray(DefineReport.UpdateId.Reportx031.getObj());		// 対象情報（正情報）
		JSONArray dataArrayChu	 = dataArray.getJSONArray(DefineReport.UpdateId.Reportx032.getObj());		// 対象情報（正情報）
		JSONArray dataArraySho	 = dataArray.getJSONArray(DefineReport.UpdateId.Reportx033.getObj());		// 対象情報（正情報）
		//JSONArray dataArraySsho	 = dataArray.getJSONArray(DefineReport.UpdateId.Reportx034.getObj());		// 対象情報（正情報）

		JSONArray msg		 = new JSONArray();
		JSONObject option	 = new JSONObject();

		// ログインユーザー情報取得
		String userId	= userInfo.getId();									// ログインユーザー

		String bmoncd	 = map.get("BUMON");								// 入力部門コード
		String bunrui	 = map.get("BUNRUI");								// 検索条件分類区分

		// パラメータ確認
		// 必須チェック
		if ( bmoncd == null || dataArray.isEmpty()) {
			option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
			return option;
		}

		// 処理対象テーブル名称を設定
		if(StringUtils.equals(DefineReport.OptionBunruiKubun.URIBA.getVal(), bunrui)){
			tableNameDAI = "INAMS.MSTDAIBRUI_URI";
			tableNameCHU = "INAMS.MSTCHUBRUI_URI";
			tableNameSHO = "INAMS.MSTSHOBRUI_URI";

		}else if(StringUtils.equals(DefineReport.OptionBunruiKubun.YOUTO.getVal(), bunrui)){
			tableNameDAI = "INAMS.MSTDAIBRUI_YOT";
			tableNameCHU = "INAMS.MSTCHUBRUI_YOT";
			tableNameSHO = "INAMS.MSTSHOBRUI_YOT";

		}else{
			tableNameDAI = "INAMS.MSTDAIBRUI";
			tableNameCHU = "INAMS.MSTCHUBRUI";
			tableNameSHO = "INAMS.MSTSHOBRUI";
		}

		// SQL発行：大分類マスタ
		if(dataArrayDai.size() > 0){
			// 新規・登録SQL
			this.createSqlDai(dataArrayDai, bunrui, map, userInfo);
			// 削除SQL
			this.createDelSqlDai(dataArrayDai, bunrui, map, userInfo);
		}
		// SQL発行：中分類マスタ
		if(dataArrayChu.size() > 0){
			// 新規・登録SQL
			this.createSqlChu(dataArrayChu, bunrui, map, userInfo);
			// 削除SQL
			this.createDelSqlChu(dataArrayChu, bunrui, map, userInfo);
		}
		// SQL発行：小分類マスタ
		if(dataArraySho.size() > 0){
			// 新規・登録SQL
			this.createSqlSho(dataArraySho, bunrui, map, userInfo);
			// 削除SQL
			this.createDelSqlSho(dataArraySho, bunrui, map, userInfo);
		}

		// SQL発行：小小分類マスタ
		/*if(dataArraySsho.size() > 0){
			//this.createSqlSsho(dataArraySsho, bunrui, map, userInfo);
		}*/

		// 親要素削除：中分類
		JSONObject errmsg = this.deleteParentDataChu(userInfo, bunrui);
		if(errmsg.size() > 0){
 			option.put(MsgKey.E.getKey(), errmsg);
 			return option;
		}

		// 親要素削除：小分類
		errmsg = this.deleteParentDataSho(userInfo, bunrui);
		if(errmsg.size() > 0){
 			option.put(MsgKey.E.getKey(), errmsg);
 			return option;
		}


		// 排他チェック実行
		String targetTable = null;
		String targetWhere = null;
		ArrayList<String> targetParam = new ArrayList<String>();
		// ⑨CSVエラー修正

		// 排他チェック：大分類グリッド
		if(dataArrayDai.size() > 0){
			String daicd = "";						// 検索用：大分類コード
			String rownum = "";						// エラー行数

			for (int i = 0; i < dataArrayDai.size(); i++) {
				JSONObject data = dataArrayDai.getJSONObject(i);
				if(data.isEmpty()){
					continue;
				}

				daicd = data.optString("F1");
				targetTable = tableNameDAI;
				targetWhere = " NVL(UPDKBN, 0) <> "+DefineReport.ValUpdkbn.DEL.getVal()+" and BMNCD = ? and DAICD = ?";
				targetParam = new ArrayList<String>();
				targetParam.add(bmoncd);
				targetParam.add(daicd);

				if(!super.checkExclusion(targetTable, targetWhere, targetParam, data.optString("F15"))){
					rownum = (data.optString("idx"));
		 			msg.add(MessageUtility.getDbMessageExclusion(FieldType.GRID, new String[]{rownum}));
		 			option.put(MsgKey.E.getKey(), msg);
					return option;
				}
			}
		}

		// 排他チェック：中分類グリッド
		if(dataArrayChu.size() > 0){
			String daicd = "";						// 検索用：大分類コード
			String chucd = "";						// 検索用：中分類コード
			String rownum = "";						// エラー行数

			for (int i = 0; i < dataArrayChu.size(); i++) {
				JSONObject data = dataArrayChu.getJSONObject(i);
				if(data.isEmpty()){
					continue;
				}

				chucd = data.optString("F1");
				daicd = data.optString("F18");
				targetTable = tableNameCHU;
				targetWhere = " NVL(UPDKBN, 0) <> "+DefineReport.ValUpdkbn.DEL.getVal()+" and BMNCD = ? and DAICD = ? and CHUCD = ?";
				targetParam = new ArrayList<String>();
				targetParam.add(bmoncd);
				targetParam.add(daicd);
				targetParam.add(chucd);

				if(!super.checkExclusion(targetTable, targetWhere, targetParam, data.optString("F15"))){
					rownum = data.optString("idx");
		 			msg.add(MessageUtility.getDbMessageExclusion(FieldType.GRID, new String[]{rownum}));
		 			option.put(MsgKey.E.getKey(), msg);
					return option;
				}
			}
		}

		// 排他チェック：小分類グリッド
		if(dataArraySho.size() > 0){
			String daicd = "";						// 検索用：大分類コード
			String chucd = "";						// 検索用：中分類コード
			String shocd = "";						// 検索用：小分類コード
			String rownum = "";						// エラー行数

			for (int i = 0; i < dataArraySho.size(); i++) {
				JSONObject data = dataArraySho.getJSONObject(i);
				if(data.isEmpty()){
					continue;
				}

				shocd = data.optString("F1");
				daicd = data.optString("F18");
				chucd = data.optString("F19");
				targetTable = tableNameSHO;
				targetWhere = " NVL(UPDKBN, 0) <> "+DefineReport.ValUpdkbn.DEL.getVal()+" and BMNCD = ? and DAICD = ? and CHUCD = ? and SHOCD = ?";
				targetParam = new ArrayList<String>();
				targetParam.add(bmoncd);
				targetParam.add(daicd);
				targetParam.add(chucd);
				targetParam.add(shocd);

				if(!super.checkExclusion(targetTable, targetWhere, targetParam, data.optString("F15"))){
					rownum = data.optString("idx");
		 			msg.add(MessageUtility.getDbMessageExclusion(FieldType.GRID, new String[]{rownum}));
		 			option.put(MsgKey.E.getKey(), msg);
					return option;
				}
			}
		}
		// 排他チェック：小小分類グリッド
				/*if(dataArraySsho.size() > 0){
					String daicd = map.get("DAICD");		// 検索用：大分類コード
					String chucd = map.get("CHUCD");		// 検索用：中分類コード
					String shocd = map.get("SHOCD");		// 検索用：小分類コード
					String sshocd = "";						// 検索用：小小分類コード
					String rownum = "";						// エラー行数

					for (int i = 0; i < dataArraySsho.size(); i++) {
						JSONObject data = dataArraySsho.getJSONObject(i);
						if(data.isEmpty()){
							continue;
						}

						sshocd		 = data.optString("F1");
						targetTable	 = "INAMS.MSTSSHOBRUI";
						targetWhere	 = " NVL(UPDKBN, 0) <> "+DefineReport.ValUpdkbn.DEL.getVal()+" and BMNCD = ? and DAICD = ? and CHUCD = ? and SHOCD = ? and SSHOCD = ?";
						targetParam	 = new ArrayList<String>();
						targetParam.add(bmoncd);
						targetParam.add(daicd);
						targetParam.add(chucd);
						targetParam.add(shocd);
						targetParam.add(sshocd);

						if(!super.checkExclusion(targetTable, targetWhere, targetParam, data.optString("F15"))){
							rownum = data.optString("idx");
				 			msg.add(MessageUtility.getDbMessageExclusion(FieldType.GRID, new String[]{rownum}));
				 			option.put(MsgKey.E.getKey(), msg);
							return option;
						}
					}
				}*/


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
	 * 子要素削除(大分類)SQL発行
	 * @return
	 *
	 * @throws Exception
	 */
	private JSONArray deleteChildDataDai(HashMap<String, String> map, User userInfo, String bunrui, JSONObject date){

		String bmoncd	 = map.get("BUMON");								// 入力部門コード
		String daicd	 = date.optString("F1");							// 入力大分類コード

		String tablenameChu = "";											// 更新対象テーブル名称
		String tablenameSho = "";											// 更新対象テーブル名称

		String userId	= userInfo.getId();									// ログインユーザー
		JSONArray msg = new JSONArray();

		// 基本INSERT/UPDATE文
		ItemList iL = new ItemList();
		JSONArray dbDatas = new JSONArray();
		StringBuffer sbSQL;
		ArrayList<String> prmData = new ArrayList<String>();

		if(StringUtils.equals(DefineReport.OptionBunruiKubun.HYOJUN.getVal(), bunrui)){
			tablenameChu = "INAMS.MSTCHUBRUI";
			tablenameSho = "INAMS.MSTSHOBRUI";

		}else if(StringUtils.equals(DefineReport.OptionBunruiKubun.URIBA.getVal(), bunrui)){
			tablenameChu = "INAMS.MSTCHUBRUI_URI";
			tablenameSho = "INAMS.MSTSHOBRUI_URI";

		}else if(StringUtils.equals(DefineReport.OptionBunruiKubun.YOUTO.getVal(), bunrui)){
			tablenameChu = "INAMS.MSTCHUBRUI_YOT";
			tablenameSho = "INAMS.MSTSHOBRUI_YOT";

		}

		// 紐付く子要素の削除
		// 削除処理：中分類マスタの更新区分に"1"（削除）するSQLを設定
		sbSQL = new StringBuffer();
		sbSQL.append("update "+tablenameChu+" CHU set");
		sbSQL.append(" CHU.UPDKBN = "+DefineReport.ValUpdkbn.DEL.getVal());
		sbSQL.append(", CHU.SENDFLG = 0");
		sbSQL.append(", CHU.OPERATOR = '" + userId + "'");
		sbSQL.append(", CHU.UPDDT = current timestamp");
		sbSQL.append(" where nvl(CHU.UPDKBN,0) <> 1 and CHU.BMNCD = ? and CHU.DAICD = ?");

		prmData.add(bmoncd);
		prmData.add(daicd);
		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("中分類マスタ");

		// 削除処理：小分類マスタの更新区分に"1"（削除）するSQLを設定
		sbSQL = new StringBuffer();
		prmData = new ArrayList<String>();
		sbSQL.append("update "+tablenameSho+" SHO set");
		sbSQL.append(" SHO.UPDKBN = "+DefineReport.ValUpdkbn.DEL.getVal());
		sbSQL.append(", SHO.SENDFLG = 0");
		sbSQL.append(", SHO.OPERATOR = '" + userId + "'");
		sbSQL.append(", SHO.UPDDT = current timestamp");
		sbSQL.append(" where nvl(SHO.UPDKBN,0) <> 1 and SHO.BMNCD = ? and SHO.DAICD = ?");

		prmData.add(bmoncd);
		prmData.add(daicd);
		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("小分類マスタ");
		return msg;
	}

	/**
	 * 子要素削除(中分類)SQL発行
	 * @return
	 *
	 * @throws Exception
	 */
	@SuppressWarnings("static-access")
	private JSONArray deleteChildDataChu(HashMap<String, String> map, User userInfo, String bunrui, JSONObject date){

		String bmoncd	 = map.get("BUMON");								// 部門コード
		String daicd	 = date.optString("F18");							// 大分類コード
		String chucd	 = date.optString("F1");							// 中分類コード

		String tablenameDai = "";											// 更新対象テーブル名称
		String tablenameChu = "";											// 更新対象テーブル名称
		String tablenameSho = "";											// 更新対象テーブル名称

		String userId	= userInfo.getId();									// ログインユーザー
		JSONArray msg = new JSONArray();

		// 基本INSERT/UPDATE文
		ItemList iL = new ItemList();
		JSONArray dbDatas = new JSONArray();
		StringBuffer sbSQL;
		ArrayList<String> prmData = new ArrayList<String>();

		if(StringUtils.equals(DefineReport.OptionBunruiKubun.HYOJUN.getVal(), bunrui)){
			tablenameDai = "INAMS.MSTDAIBRUI";
			tablenameChu = "INAMS.MSTCHUBRUI";
			tablenameSho = "INAMS.MSTSHOBRUI";

		}else if(StringUtils.equals(DefineReport.OptionBunruiKubun.URIBA.getVal(), bunrui)){
			tablenameDai = "INAMS.MSTDAIBRUI_URI";
			tablenameChu = "INAMS.MSTCHUBRUI_URI";
			tablenameSho = "INAMS.MSTSHOBRUI_URI";

		}else if(StringUtils.equals(DefineReport.OptionBunruiKubun.YOUTO.getVal(), bunrui)){
			tablenameDai = "INAMS.MSTDAIBRUI_YOT";
			tablenameChu = "INAMS.MSTCHUBRUI_YOT";
			tablenameSho = "INAMS.MSTSHOBRUI_YOT";

		}

		// 削除処理：小分類マスタの更新区分に"1"（削除）するSQLを設定
		sbSQL = new StringBuffer();
		prmData = new ArrayList<String>();
		sbSQL.append("update "+tablenameSho+" SHO set");
		sbSQL.append(" SHO.UPDKBN = "+DefineReport.ValUpdkbn.DEL.getVal()+"");
		sbSQL.append(", SHO.SENDFLG = 0");
		sbSQL.append(", SHO.OPERATOR = '" + userId + "'");
		sbSQL.append(", SHO.UPDDT = current timestamp");
		sbSQL.append(" where nvl(SHO.UPDKBN,0) <> 1 and SHO.BMNCD = ? and SHO.DAICD = ? and SHO.CHUCD = ?");

		prmData.add(bmoncd);
		prmData.add(daicd);
		prmData.add(chucd);
		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("小分類マスタ");

		return msg;
	}

	/**
	 * 親要素削除(中分類)SQL発行
	 * @return
	 *
	 * @throws Exception
	 */
	@SuppressWarnings("static-access")
	private JSONObject deleteParentDataChu(User userInfo, String bunrui){

		String bmoncd	 = "";								// 部門コード
		String daicd	 = "";								// 大分類コード
		String chucd	 = "";								// 大分類コード


		String tablenameChu = "";							// 更新対象テーブル名称
		String tablenameSho = "";							// 更新対象テーブル名称

		ArrayList<String> checkParams = new ArrayList<>();	// 存在チェック用SQLのパラメータ格納

		String userId	= userInfo.getId();					// ログインユーザー
		MessageUtility mu	 = new MessageUtility();
		JSONObject msgObj = new JSONObject();


		// 基本INSERT/UPDATE文
		ItemList iL = new ItemList();
		JSONArray dbDatas = new JSONArray();
		StringBuffer sbSQL;
		ArrayList<String> prmData = new ArrayList<String>();

		// 中分類マスタ_他中分類検索
		// 削除件数を比較
		JSONObject Dletedata = new JSONObject();

		// 小分類削除データより、全権削除による中分類データが生じている場合追加を行う。
		if(DeldataArraySHO.size() > 0){
			for (int i = 0; i < DeldataArraySHO.size(); i++) {
				Dletedata = DeldataArraySHO.getJSONObject(i);

				bmoncd			 = Dletedata.optString("BMNCD");
				daicd			 = Dletedata.optString("DAICD");
				chucd			 = Dletedata.optString("CHUCD");
				tablenameSho	 = Dletedata.optString("TABLENAME");

				// マスタに登録された小分類の件数を取得
				sbSQL = new StringBuffer();
				sbSQL.append("select");
				sbSQL.append(" COUNT(SHO.CHUCD) as COUNT");
				sbSQL.append(" from "+tablenameSho+" SHO");
				sbSQL.append(" where SHO.BMNCD = "+bmoncd);
				sbSQL.append(" and SHO.DAICD = "+daicd);
				sbSQL.append(" and SHO.CHUCD = "+chucd);
				sbSQL.append(" and NVL(SHO.UPDKBN, 0) <> 1");
				dbDatas = iL.selectJSONArray(sbSQL.toString(), null, Defines.STR_JNDI_DS);

				if(Dletedata.optInt("COUNT") == dbDatas.getJSONObject(i).getInt("COUNT")){
					// 削除する中分類データを保持
					JSONObject dataCld = new JSONObject();
					dataCld.put("F17", bmoncd);
					dataCld.put("F18", daicd);
					this.addDleteDate(dataCld, tableNameCHU);
				}
			}
		}


		if(DeldataArrayCHU.size() > 0){
			for (int l = 0; l < DeldataArrayCHU.size(); l++) {
				Dletedata = DeldataArrayCHU.getJSONObject(l);

				bmoncd			 = Dletedata.optString("BMNCD");
				daicd			 = Dletedata.optString("DAICD");
				tablenameChu	 = Dletedata.optString("TABLENAME");

				// マスタに登録された中分類の件数を取得
				sbSQL = new StringBuffer();
				sbSQL.append("select");
				sbSQL.append(" COUNT(CHU.CHUCD) as COUNT");
				sbSQL.append(" from "+tablenameChu+" CHU");
				sbSQL.append(" where CHU.BMNCD = "+bmoncd);
				sbSQL.append(" and CHU.DAICD = "+daicd);
				sbSQL.append(" and NVL(CHU.UPDKBN, 0) <> 1");
				dbDatas = iL.selectJSONArray(sbSQL.toString(), null, Defines.STR_JNDI_DS);

				if(Dletedata.optInt("COUNT") == dbDatas.getJSONObject(l).getInt("COUNT")){
					// 中分類全権削除の場合

					// 削除チェック
					checkParams = new ArrayList<>();
					checkParams.add(bmoncd);
					checkParams.add(daicd);

					if(this.checkShnMstExist(DefineReport.InpText.DAICD.getObj(), checkParams, bunrui)){
						msgObj = mu.getDbMessageObj("E00006");
						return msgObj;

					}

					// 紐付く親要素の削除
					sbSQL = new StringBuffer();
					sbSQL.append("update "+tableNameDAI+" DAI set");
					sbSQL.append(" DAI.UPDKBN = "+DefineReport.ValUpdkbn.DEL.getVal());
					sbSQL.append(", DAI.SENDFLG = 0");
					sbSQL.append(", DAI.OPERATOR = '" + userId + "'");
					sbSQL.append(", DAI.UPDDT = current timestamp");
					sbSQL.append(" where nvl(DAI.UPDKBN,0) <> 1 and DAI.BMNCD = ? and DAI.DAICD = ?");

					prmData = new ArrayList<String>();
					prmData.add(bmoncd);
					prmData.add(daicd);
					sqlList.add(sbSQL.toString());
					prmList.add(prmData);
					lblList.add("大分類マスタ");
				}
			}
		};
		return msgObj;
	}

	/**
	 * 親要素削除(小分類)SQL発行
	 * @return
	 *
	 * @throws Exception
	 */
	@SuppressWarnings("static-access")
	private JSONObject deleteParentDataSho(User userInfo, String bunrui){

		String bmoncd	 = "";								// 部門コード
		String daicd	 = "";								// 大分類コード
		String chucd	 = "";								// 中分類コード

		String tablenameSho = "";							// 更新対象テーブル名称
		ArrayList<String> checkParams = new ArrayList<>();	// 存在チェック用SQLのパラメータ格納

		String userId	= userInfo.getId();					// ログインユーザー
		MessageUtility mu	 = new MessageUtility();
		JSONObject msgObj = new JSONObject();


		// 基本INSERT/UPDATE文
		ItemList iL = new ItemList();
		JSONArray dbDatas = new JSONArray();
		StringBuffer sbSQL;
		ArrayList<String> prmData = new ArrayList<String>();

		// 中分類マスタ_他中分類検索
		// 削除件数を比較
		JSONObject Dletedata = new JSONObject();
		if(DeldataArraySHO.size() > 0){
			for (int l = 0; l < DeldataArraySHO.size(); l++) {
				Dletedata = DeldataArraySHO.getJSONObject(l);

				bmoncd			 = Dletedata.optString("BMNCD");
				daicd			 = Dletedata.optString("DAICD");
				chucd			 = Dletedata.optString("CHUCD");
				tablenameSho	 = Dletedata.optString("TABLENAME");

				// マスタに登録された小分類の件数を取得
				sbSQL = new StringBuffer();
				sbSQL.append("select");
				sbSQL.append(" COUNT(SHO.CHUCD) as COUNT");
				sbSQL.append(" from "+tablenameSho+" SHO");
				sbSQL.append(" where SHO.BMNCD = "+bmoncd);
				sbSQL.append(" and SHO.DAICD = "+daicd);
				sbSQL.append(" and SHO.CHUCD = "+chucd);
				sbSQL.append(" and NVL(SHO.UPDKBN, 0) <> 1");
				dbDatas = iL.selectJSONArray(sbSQL.toString(), null, Defines.STR_JNDI_DS);

				if(Dletedata.optInt("COUNT") == dbDatas.getJSONObject(l).getInt("COUNT")){
					// 小分類全権削除の場合

					// 削除チェック
					checkParams = new ArrayList<>();
					checkParams.add(bmoncd);
					checkParams.add(daicd);
					checkParams.add(chucd);

					if(this.checkShnMstExist(DefineReport.InpText.CHUCD.getObj(), checkParams, bunrui)){
						msgObj = mu.getDbMessageObj("E00006");
						return msgObj;

					}

					// 紐付く親要素の削除
					// 削除処理：中分類マスタの更新区分に"1"（削除）するSQLを設定
					sbSQL = new StringBuffer();
					sbSQL.append("update "+tableNameCHU+" CHU set");
					sbSQL.append(" CHU.UPDKBN = "+DefineReport.ValUpdkbn.DEL.getVal());
					sbSQL.append(", CHU.SENDFLG = 0");
					sbSQL.append(", CHU.OPERATOR = '" + userId + "'");
					sbSQL.append(", CHU.UPDDT = current timestamp");
					sbSQL.append(" where nvl(CHU.UPDKBN,0) <> 1 and CHU.BMNCD = ? and CHU.DAICD = ? and CHU.CHUCD = ?");

					prmData = new ArrayList<String>();
					prmData.add(bmoncd);
					prmData.add(daicd);
					prmData.add(chucd);
					sqlList.add(sbSQL.toString());
					prmList.add(prmData);
					lblList.add("中分類マスタ");
				}
			}
		};
		return msgObj;
	}
	private void addDleteDate(JSONObject dataCld, String tablename){

		JSONObject Dletedata = new JSONObject();
		JSONObject obj = new JSONObject();

		if(StringUtils.isNotEmpty(dataCld.optString("F19"))){

			// 削除する小分類コードの件数を保持
			obj = new JSONObject();
			obj.put("TABLENAME", tablename);
			obj.put("BMNCD", dataCld.optString("F17"));
			obj.put("DAICD", dataCld.optString("F18"));
			obj.put("CHUCD", dataCld.optString("F19"));
			obj.put("COUNT", 1);

			if(DeldataArraySHO.size() > 0){
				for (int l = 0; l < DeldataArraySHO.size(); l++) {
					Dletedata = DeldataArraySHO.getJSONObject(l);
					if( StringUtils.equals(Dletedata.optString("TABLENAME"), tablename)
						&& StringUtils.equals(Dletedata.optString("BMNCD"), dataCld.optString("F17"))
						&& StringUtils.equals(Dletedata.optString("DAICD"), dataCld.optString("F18"))
						&& StringUtils.equals(Dletedata.optString("CHUCD"), dataCld.optString("F19"))){

						DeldataArraySHO.getJSONObject(l).put("COUNT", (Integer.parseInt(Dletedata.optString("COUNT")) + 1));
					}else{
						DeldataArraySHO.add(obj);
					}
				}
			}else{
				DeldataArraySHO.add(obj);
			}
		}else{

			// 削除する中分類コードの件数を保持
			obj = new JSONObject();
			obj.put("TABLENAME", tablename);
			obj.put("BMNCD", dataCld.optString("F17"));
			obj.put("DAICD", dataCld.optString("F18"));
			obj.put("COUNT", 1);

			if(DeldataArrayCHU.size() > 0){
				for (int l = 0; l < DeldataArrayCHU.size(); l++) {
					Dletedata = DeldataArrayCHU.getJSONObject(l);
					if( StringUtils.equals(Dletedata.optString("TABLENAME"), tablename)
						&& StringUtils.equals(Dletedata.optString("BMNCD"), dataCld.optString("F17"))
						&& StringUtils.equals(Dletedata.optString("DAICD"), dataCld.optString("F18"))){

						DeldataArrayCHU.getJSONObject(l).put("COUNT", (Integer.parseInt(Dletedata.optString("COUNT")) + 1));
					}else{
						DeldataArrayCHU.add(obj);
					}
				}
			}else{
				DeldataArrayCHU.add(obj);
			}



		}



	}

	/**
	 * 削除処理実行
	 * @return
	 *
	 * @throws Exception
	 */
	@SuppressWarnings("static-access")
	private JSONObject deleteData(HashMap<String, String> map, User userInfo) throws Exception {
		// パラメータ確認
		JSONArray dataArray = JSONArray.fromObject(map.get("DATA"));	// 対象情報

		JSONObject msgObj = new JSONObject();
		JSONArray msg = new JSONArray();

		// ログインユーザー情報取得
		String userId	= userInfo.getId();								// ログインユーザー

		// 更新情報
		String values = "";
		String bmoncd = "";												// 入力部門コード
		String daicd = "";												// 入力大分類コード
		for (int i = 0; i < dataArray.size(); i++) {
			JSONObject data = dataArray.getJSONObject(i);
			if(data.isEmpty()){
				continue;
			}

			bmoncd=data.optString("F2");
			daicd=data.optString("F2");
			values += ",'"+data.optString("F2")+"'";						// 大分類コード
		}
		values = StringUtils.removeStart(values, ",");

		if(values.length()==0){
			msgObj.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00002.getVal()));
			return msgObj;
		}

		// 基本INSERT/UPDATE文
		ItemList iL = new ItemList();
		JSONArray dbDatas = new JSONArray();
		StringBuffer sbSQL;
		ArrayList<String> prmData = new ArrayList<String>();

		return msgObj;
	}

	/**
	 * チェック処理
	 *
	 * @throws Exception
	 */
	@SuppressWarnings("static-access")
	public JSONArray check(HashMap<String, String> map) {
		// パラメータ確認
		JSONObject dataArray = JSONObject.fromObject(map.get("DATA"));	// 対象情報

		String bmoncd	 = map.get("BUMON");									// 部門コード
		String daicd	 = map.get("DAICD");									// 大分類コード
		String chucd	 = map.get("CHUCD");									// 中分類コード
		String shocd 	 = map.get("SHOCD");									// 小分類コード

		String bunrui	 = map.get("BUNRUI");									// 検索条件分類区分
		String tablenameBMN = "";												// 対象テーブル：部門
		String tablenameDAI = "";												// 対象テーブル：大分類
		String tablenameCHU = "";												// 対象テーブル：中分類
		String tablenameSHO = "";												// 対象テーブル：小分類

		ArrayList<String> checkParams = new ArrayList<>();						// 存在チェック用SQLのパラメータ格納

		JSONArray dataArrayDai	 = dataArray.getJSONArray(DefineReport.UpdateId.Reportx031.getObj());		// 対象情報（正情報）
		JSONArray dataArrayChu	 = dataArray.getJSONArray(DefineReport.UpdateId.Reportx032.getObj());		// 対象情報（正情報）
		JSONArray dataArraySho	 = dataArray.getJSONArray(DefineReport.UpdateId.Reportx033.getObj());		// 対象情報（正情報）
		//JSONArray dataArraySsho	 = dataArray.getJSONArray(DefineReport.UpdateId.Reportx034.getObj());		// 対象情報（正情報）

		String outobj		= map.get(DefineReport.ID_PARAM_OBJ);		// 実行ボタン
		boolean newRecordDai = false;
		boolean newRecordChu = false;
		boolean newRecordSho = false;

		JSONArray msg		 = new JSONArray();
		MessageUtility mu	 = new MessageUtility();


		if(StringUtils.equals(DefineReport.OptionBunruiKubun.URIBA.getVal(), bunrui)){
			tablenameDAI = "INAMS.MSTDAIBRUI_URI";
			tablenameCHU = "INAMS.MSTCHUBRUI_URI";
			tablenameSHO = "INAMS.MSTSHOBRUI_URI";

		}else if(StringUtils.equals(DefineReport.OptionBunruiKubun.YOUTO.getVal(), bunrui)){
			tablenameDAI = "INAMS.MSTDAIBRUI_YOT";
			tablenameCHU = "INAMS.MSTCHUBRUI_YOT";
			tablenameSHO = "INAMS.MSTSHOBRUI_YOT";

		}else{
			tablenameDAI = "INAMS.MSTDAIBRUI";
			tablenameCHU = "INAMS.MSTCHUBRUI";
			tablenameSHO = "INAMS.MSTSHOBRUI";
		}

		// 入力値を取得
		//JSONObject data = dataArray;

		// 基本INSERT/UPDATE文
		ItemList iL = new ItemList();
		JSONArray dbDatas = new JSONArray();
		StringBuffer sbSQL;
		ArrayList<String> prmData = new ArrayList<String>();


		// 大分類
		if(!dataArrayDai.isEmpty()){
			for (int i = 0; i < dataArrayDai.size(); i++) {
				JSONObject data = dataArrayDai.getJSONObject(i);
				if(data.isEmpty()){
					continue;
				}

				bmoncd	 = data.optString("F17");
				daicd	 = data.optString("F1");

				// 紐付きチェック：商品マスタ
				if(StringUtils.equals(data.optString("F14"), DefineReport.ValUpdkbn.DEL.getVal())){
					checkParams = new ArrayList<>();
					checkParams.add(bmoncd);
					checkParams.add(daicd);

					if(this.checkShnMstExist(DefineReport.InpText.DAICD.getObj(), checkParams, bunrui)){
						JSONObject o = mu.getDbMessageObj("E00006");
						msg.add(o);
						return msg;

					}
				}

				if(StringUtils.equals(data.optString("F13"), "1")){
					newRecordDai = true;
					sbSQL = new StringBuffer();
					sbSQL.append("select * from "+tablenameDAI+" DAI where NVL(DAI.UPDKBN, 0) = 0 and DAI.BMNCD = "+bmoncd+" and DAI.DAICD = "+daicd);
					dbDatas = iL.selectJSONArray(sbSQL.toString(), null, Defines.STR_JNDI_DS);
					if(dbDatas.size() > 0 ){
						JSONObject o = mu.getDbMessageObj("E00004", "大分類コード");
						msg.add(o);
						return msg;
						//msg.add(MessageUtility.getMsg("大分類コードが重複しています。"));
					}
				}
			}
		}

		// 中分類
		if(!dataArrayChu.isEmpty()){
			for (int i = 0; i < dataArrayChu.size(); i++) {
				JSONObject data = dataArrayChu.getJSONObject(i);
				if(data.isEmpty()){
					continue;
				}

				bmoncd	 = data.optString("F17");
				daicd	 = data.optString("F18");
				chucd	 = data.optString("F1");

				// 紐付きチェック：商品マスタ
				if(StringUtils.equals(data.optString("F14"), DefineReport.ValUpdkbn.DEL.getVal())){
					checkParams = new ArrayList<>();
					checkParams.add(bmoncd);
					checkParams.add(daicd);
					checkParams.add(chucd);

					if(this.checkShnMstExist(DefineReport.InpText.CHUCD.getObj(), checkParams, bunrui)){
						JSONObject o = mu.getDbMessageObj("E00006");
						msg.add(o);
						return msg;

					}
				}

				if(StringUtils.equals(data.optString("F13"), "1")){
					newRecordChu = true;
					sbSQL = new StringBuffer();
					sbSQL.append("select * from "+tablenameCHU+" CHU where NVL(CHU.UPDKBN, 0) = 0 and CHU.BMNCD = "+bmoncd+" and CHU.DAICD = "+daicd+" and CHU.CHUCD = "+chucd);
					dbDatas = iL.selectJSONArray(sbSQL.toString(), null, Defines.STR_JNDI_DS);
					if(dbDatas.size() > 0 ){
						JSONObject o = mu.getDbMessageObj("E00004", "中分類コード");
						msg.add(o);
						return msg;
						//msg.add(MessageUtility.getMsg("中分類コードが重複しています。"));
					}
				}
			}
		}

		// 小分類
		if(!dataArraySho.isEmpty()){
			for (int i = 0; i < dataArraySho.size(); i++) {
				JSONObject data = dataArraySho.getJSONObject(i);
				if(data.isEmpty()){
					continue;
				}

				bmoncd	 = data.optString("F17");
				daicd	 = data.optString("F18");
				chucd	 = data.optString("F19");
				shocd	 = data.optString("F1");

				// 紐付きチェック：商品マスタ
				if(StringUtils.equals(data.optString("F14"), DefineReport.ValUpdkbn.DEL.getVal())){

					checkParams = new ArrayList<>();
					checkParams.add(bmoncd);
					checkParams.add(daicd);
					checkParams.add(chucd);
					checkParams.add(shocd);

					if(this.checkShnMstExist(DefineReport.InpText.SHOCD.getObj(), checkParams, bunrui)){
						JSONObject o = mu.getDbMessageObj("E00006");
						msg.add(o);
						return msg;

					}
				}

				if(StringUtils.equals(data.optString("F13"), "1")){
					newRecordSho = true;
					sbSQL = new StringBuffer();
					sbSQL.append("select * from "+tablenameSHO+" SHO where NVL(SHO.UPDKBN, 0) = 0 and SHO.BMNCD = "+bmoncd+" and SHO.DAICD = "+daicd+" and SHO.CHUCD = "+chucd+" and SHO.SHOCD = "+shocd);
					dbDatas = iL.selectJSONArray(sbSQL.toString(), null, Defines.STR_JNDI_DS);
					if(dbDatas.size() > 0 ){
						JSONObject o = mu.getDbMessageObj("E00004", "小分類コード");
						msg.add(o);
						return msg;
						//msg.add(MessageUtility.getMsg("小分類コードが重複しています。"));
					}
				}
			}
		}
		return msg;
	}
	/**
	 * マスタ情報取得処理
	 *
	 * @throws Exception
	 */
	public boolean checkShnMstExist(String outobj, ArrayList<String> Pramdates, String bunrui) {
		// 関連情報取得
		ItemList iL = new ItemList();
		// 配列準備
		ArrayList<String> paramData = new ArrayList<String>();
		String sqlcommand = "";
		String tbl = "";
		String col = "";
		String hedText = "";
		String sqlwhere=" where NVL(UPDKBN, 0) <> 1";

		if(StringUtils.equals(DefineReport.OptionBunruiKubun.URIBA.getVal(), bunrui)){
			hedText = "URI_";

		}else if(StringUtils.equals(DefineReport.OptionBunruiKubun.YOUTO.getVal(), bunrui)){
			hedText = "YOT_";

		}

		// 大分類コード
		if (outobj.equals(DefineReport.InpText.DAICD.getObj())) {
			tbl = "INAMS.MSTSHN";
			col = hedText + "DAICD";
			sqlwhere += " and " + hedText + "BMNCD = ?"
					+ " and "+ hedText + "DAICD = ?";
		}
		// 中分類コード
		if (outobj.equals(DefineReport.InpText.CHUCD.getObj())) {
			tbl = "INAMS.MSTSHN";
			col = hedText + "CHUCD";
			sqlwhere += " and " + hedText + "BMNCD = ?"
					+ " and "+ hedText + "DAICD = ?"
					+ " and "+ hedText + "CHUCD = ?";

		}
		// 小分類コード
		if (outobj.equals(DefineReport.InpText.SHOCD.getObj())) {
			tbl="INAMS.MSTSHN";
			col = hedText + "SHOCD";
			sqlwhere += " and " + hedText + "BMNCD = ?"
					+ " and "+ hedText + "DAICD = ?"
					+ " and "+ hedText + "CHUCD = ?"
					+ " and "+ hedText + "SHOCD = ?";
		}

		for (int i = 0; i < Pramdates.size(); i++) {
			paramData.add(Pramdates.get(i));
		}

		sqlcommand = "select @C from @T";
		sqlcommand = sqlcommand.replace("@T", tbl).replace("@C", col)+sqlwhere +" fetch first 1 rows only";
		@SuppressWarnings("static-access")
		JSONArray array = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
		if(array.size() > 0){
			return true;
		}

		return false;
	}
}
