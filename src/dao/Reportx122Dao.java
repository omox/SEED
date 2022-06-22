package dao;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.JDBCType;
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
import common.CmnDate;
import common.DefineReport;
import common.DefineReport.DataType;
import common.InputChecker;
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
public class Reportx122Dao extends ItemDao {

	/**
	 * インスタンスを生成します。
	 * @param source
	 */
	public Reportx122Dao(String JNDIname) {
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
		JSONArray msg = this.check(map, userInfo, sysdate);

		if(msg.size() > 0){
			msgObj.put(MsgKey.E.getKey(), msg);
			return msgObj;
		}

		// 更新処理
		try {
			msgObj = this.updateData(map, userInfo,sysdate);
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
		String sysdate = (String)request.getSession().getAttribute(Consts.STR_SES_LOGINDT);
		// 更新情報チェック(基本JS側で制御)
		JSONObject msgObj = new JSONObject();

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

		String sendBtnid	 = getMap().get("SENDBTNID");			// 呼出しボタン

		// タイトル情報(任意)設定
		List<String> titleList = new ArrayList<String>();

		StringBuffer sbSQL = new StringBuffer();

		if(DefineReport.Button.SEARCH.getObj().equals(sendBtnid) ){
			String tenorg	 = getMap().get("TENORG");			// 店コードコピー元
			String bmnorg	 = getMap().get("BMNORG");			// 部門コードコピー元
			String tencopy	 = getMap().get("TENCOPY");			// 店コードコピー先
			String bmncopy	 = getMap().get("BMNCOPY");			// 部門コードコピー元

			// パラメータ確認
			// 必須チェック
			if ( StringUtils.isEmpty(tenorg) || StringUtils.isEmpty(bmnorg) || StringUtils.isEmpty(tencopy) || StringUtils.isEmpty(bmncopy)) {
				System.out.println(super.getConditionLog());
				return "";
			}

			// DB検索用パラメータ
			ArrayList<String> paramData = new ArrayList<String>();

			paramData.add(tenorg);
			paramData.add(bmnorg);

			sbSQL.append(" select ");
			sbSQL.append(tencopy);
			sbSQL.append(" AS ");
			sbSQL.append(" TENCD ,");															// F1	：店舗コード
			sbSQL.append(bmncopy);
			sbSQL.append(" AS ");
			sbSQL.append("  BMNCD ");															// F2	：部門コード
			sbSQL.append(", READTMPTN");														// F3	：リードタイムパターン
			sbSQL.append(", BMNKN");															// F4	：部門名称漢字
			sbSQL.append(", GROUPNO");															// F5	：グループ№
			sbSQL.append(", BMNRECEIPTKN");														// F6	：部門レシート名称（漢字）
			sbSQL.append(", BMNRECEIPTAN");														// F7	：部門レシート名称（カナ）
			sbSQL.append(", MIOKBN");															// F8	：MIO区分
			sbSQL.append(", SHUKEICD");															// F91	：集計OD
			sbSQL.append(", WARIBIKIKBN");														// F10	：割引区分
			sbSQL.append(", BMNGENKART");														// F11	：部門原価率
			sbSQL.append(", TENANTKBN");														// F12	：自社テナント
			sbSQL.append(", LOSSTKBN");															// F13	：ロス分析対象
			sbSQL.append(", YOSANKBN_B");														// F14	：予算区分
			sbSQL.append(", TANAOROTKBN");														// F15	：棚卸対象区分
			sbSQL.append(", PCARD_SHUKBN");														// F16	：プライスカード種類
			sbSQL.append(", PCARD_IROKBN");														// F17	：プライスカード色
			sbSQL.append(", AREACD");															// F18	：エリア
			sbSQL.append(", URIFLG");															// F19	：売上フラグ
			sbSQL.append(", TENANTCD");															// F20	：テナントコード
			sbSQL.append(", BMN_ATR1");															// F21	：部門属性1
			sbSQL.append(", BMN_ATR2");															// F22	：部門属性２
			sbSQL.append(", BMN_ATR3");															// F23	：部門属性３
			sbSQL.append(", BMN_ATR4");															// F24	：部門属性４
			sbSQL.append(", BMN_ATR5");															// F25	：部門属性５
			sbSQL.append(" from INAMS.MSTTENBMN");
			sbSQL.append(" where NVL(UPDKBN, 0) = 0");
			sbSQL.append(" and TENCD = ? ");
			sbSQL.append(" and BMNCD = ? ");
			setParamData(paramData);
		}else if(DefineReport.Button.SEL_CHANGE.getObj().equals(sendBtnid)||DefineReport.Button.SEL_REFER.getObj().equals(sendBtnid)){
			String bmncd	 = getMap().get("BMNCD");			// 部門コード
			String tencd	 = getMap().get("TENCD");			// 店コード

			if ( bmncd == null || tencd == null ) {
				System.out.println(super.getConditionLog());
				return "";
			}

			ArrayList<String> paramData = new ArrayList<String>();
			paramData.add(tencd);
			paramData.add(bmncd);

			sbSQL.append(" select ");
			sbSQL.append("  MTBN.TENCD ");																// F1	：店舗コード
			sbSQL.append(", MTBN.BMNCD ");																// F2	：部門コード
			sbSQL.append(", MTBN.READTMPTN");															// F3	：リードタイムパターン
			sbSQL.append(", MTBN.BMNKN");																// F4	：部門名称漢字
			sbSQL.append(", MTBN.GROUPNO");																// F5	：グループ№
			sbSQL.append(", MTBN.BMNRECEIPTKN");														// F6	：部門レシート名称（漢字）
			sbSQL.append(", MTBN.BMNRECEIPTAN");														// F7	：部門レシート名称（カナ）
			sbSQL.append(", MTBN.MIOKBN");																// F8	：MIO区分
			sbSQL.append(", MTBN.SHUKEICD");															// F91	：集計OD
			sbSQL.append(", MTBN.WARIBIKIKBN");															// F10	：割引区分
			sbSQL.append(", MTBN.BMNGENKART");															// F11	：部門原価率
			sbSQL.append(", MTBN.TENANTKBN");															// F12	：自社テナント
			sbSQL.append(", MTBN.LOSSTKBN");															// F13	：ロス分析対象
			sbSQL.append(", MTBN.YOSANKBN_B");															// F14	：予算区分
			sbSQL.append(", MTBN.TANAOROTKBN");															// F15	：棚卸対象区分
			sbSQL.append(", MTBN.PCARD_SHUKBN");														// F16	：プライスカード種類
			sbSQL.append(", MTBN.PCARD_IROKBN");														// F17	：プライスカード色
			sbSQL.append(", MTBN.AREACD");																// F18	：エリア
			sbSQL.append(", MTBN.URIFLG");																// F19	：売上フラグ
			sbSQL.append(", MTBN.TENANTCD");															// F20	：テナントコード
			sbSQL.append(", MTBN.BMN_ATR1");															// F21	：部門属性1
			sbSQL.append(", MTBN.BMN_ATR2");															// F22	：部門属性２
			sbSQL.append(", MTBN.BMN_ATR3");															// F23	：部門属性３
			sbSQL.append(", MTBN.BMN_ATR4");															// F24	：部門属性４
			sbSQL.append(", MTBN.BMN_ATR5");															// F25	：部門属性５
			sbSQL.append(", TO_CHAR(ADDDT, 'yy/mm/dd')");												// F26	：画面下部表示用_登録日
			sbSQL.append(", TO_CHAR(UPDDT, 'yy/mm/dd')");												// F27	：画面下部表示用_更新日
			sbSQL.append(", OPERATOR");																	// F28	：画面下部表示用_オペレーター
			sbSQL.append(" from INAMS.MSTTENBMN MTBN");
			sbSQL.append(" where NVL(UPDKBN, 0) = 0");
			sbSQL.append(" and MTBN.TENCD = ?");
			sbSQL.append(" and MTBN.BMNCD = ?");
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
	private JSONObject updateData(HashMap<String, String> map, User userInfo,String sysdate) throws Exception {
		// パラメータ確認
		JSONArray dataArray = JSONArray.fromObject(map.get("DATA"));	// 対象情報
		JSONObject msgObj = new JSONObject();
		JSONArray msg = new JSONArray();
		JSONObject option	 = new JSONObject();

		String dbsysdate = CmnDate.dbDateFormat(sysdate);

		// ログインユーザー情報取得
		String userId	= userInfo.getId();							// ログインユーザー
		ArrayList<String> prmData = new ArrayList<String>();
		JSONObject data = dataArray.getJSONObject(0);
		Object[] valueData = new Object[]{};
		// パラメータ確認
		// ログインユーザー情報取得

		String values = "";
		// 更新情報
		int maxField = 25;		// Fxxの最大値
		for (int k = 1; k <= maxField; k++) {
			String key = "F" + String.valueOf(k);

			String val = data.optString(key);
			if(StringUtils.isEmpty(val)){
				values += ", null";
			}else{
				if(k == 1){
					values += " ?";
				}else{
					values += ", ?";
				}

				prmData.add(val);
			}


			if(k == maxField){
				valueData = ArrayUtils.add(valueData, "("+values+")");
				values = "";
			}
		}
		// 基本INSERT/UPDATE文
		StringBuffer sbSQL = new StringBuffer();
		// 配送グループマスタの登録・更新
		sbSQL.append("MERGE INTO INAMS.MSTTENBMN AS T USING (SELECT ");
		sbSQL.append(" TENCD");
		sbSQL.append(", BMNCD");
		sbSQL.append(", READTMPTN");
		sbSQL.append(", BMNKN");
		sbSQL.append(", GROUPNO");
		sbSQL.append(", BMNRECEIPTKN");
		sbSQL.append(", BMNRECEIPTAN");
		sbSQL.append(", MIOKBN");
		sbSQL.append(", SHUKEICD");
		sbSQL.append(", WARIBIKIKBN");
		sbSQL.append(", BMNGENKART");
		sbSQL.append(", TENANTKBN");
		sbSQL.append(", LOSSTKBN");
		sbSQL.append(", YOSANKBN_B");
		sbSQL.append(", TANAOROTKBN");
		sbSQL.append(", PCARD_SHUKBN");
		sbSQL.append(", PCARD_IROKBN");
		sbSQL.append(", AREACD");
		sbSQL.append(", URIFLG");
		sbSQL.append(", TENANTCD");
		sbSQL.append(", BMN_ATR1");
		sbSQL.append(", BMN_ATR2");
		sbSQL.append(", BMN_ATR3");
		sbSQL.append(", BMN_ATR4");
		sbSQL.append(", BMN_ATR5");
		sbSQL.append(", "+DefineReport.Values.SENDFLG_UN.getVal()+" AS SENDFLG");	// 送信フラグ：
		sbSQL.append(", "+DefineReport.ValUpdkbn.NML.getVal()+" AS UPDKBN");		// 更新区分：
		sbSQL.append(", '"+userId+"' AS OPERATOR ");								// オペレーター：
		sbSQL.append(", current timestamp AS ADDDT ");								// 登録日：
		sbSQL.append(", current timestamp AS UPDDT ");								// 更新日：
		sbSQL.append(" FROM (values "+StringUtils.join(valueData, ",")+") as T1(");
		sbSQL.append(" TENCD");
		sbSQL.append(", BMNCD");
		sbSQL.append(", READTMPTN");
		sbSQL.append(", BMNKN");
		sbSQL.append(", GROUPNO");
		sbSQL.append(", BMNRECEIPTKN");
		sbSQL.append(", BMNRECEIPTAN");
		sbSQL.append(", MIOKBN");
		sbSQL.append(", SHUKEICD");
		sbSQL.append(", WARIBIKIKBN");
		sbSQL.append(", BMNGENKART");
		sbSQL.append(", TENANTKBN");
		sbSQL.append(", LOSSTKBN");
		sbSQL.append(", YOSANKBN_B");
		sbSQL.append(", TANAOROTKBN");
		sbSQL.append(", PCARD_SHUKBN");
		sbSQL.append(", PCARD_IROKBN");
		sbSQL.append(", AREACD");
		sbSQL.append(", URIFLG");
		sbSQL.append(", TENANTCD");
		sbSQL.append(", BMN_ATR1");
		sbSQL.append(", BMN_ATR2");
		sbSQL.append(", BMN_ATR3");
		sbSQL.append(", BMN_ATR4");
		sbSQL.append(", BMN_ATR5");
		sbSQL.append(")) as RE on (");
		sbSQL.append("T.TENCD = RE.TENCD and T.BMNCD = RE.BMNCD ");
		sbSQL.append(") WHEN MATCHED THEN UPDATE SET ");
		sbSQL.append(" TENCD = RE.TENCD");
		sbSQL.append(", BMNCD = RE.BMNCD");
		sbSQL.append(", AREACD = RE.AREACD");
		sbSQL.append(", READTMPTN = RE.READTMPTN");
		sbSQL.append(", BMNKN = RE.BMNKN");
		sbSQL.append(", GROUPNO = RE.GROUPNO");
		sbSQL.append(", BMNRECEIPTAN = RE.BMNRECEIPTAN");
		sbSQL.append(", BMNRECEIPTKN = RE.BMNRECEIPTKN");
		sbSQL.append(", MIOKBN = RE.MIOKBN");
		sbSQL.append(", SHUKEICD = RE.SHUKEICD");
		sbSQL.append(", WARIBIKIKBN = RE.WARIBIKIKBN");
		sbSQL.append(", BMNGENKART = RE.BMNGENKART");
		sbSQL.append(", TENANTKBN = RE.TENANTKBN");
		sbSQL.append(", LOSSTKBN = RE.LOSSTKBN");
		sbSQL.append(", YOSANKBN_B = RE.YOSANKBN_B");
		sbSQL.append(", TANAOROTKBN = RE.TANAOROTKBN");
		sbSQL.append(", PCARD_SHUKBN = RE.PCARD_SHUKBN");
		sbSQL.append(", PCARD_IROKBN = RE.PCARD_IROKBN");
		sbSQL.append(", TENANTCD = RE.TENANTCD");
		sbSQL.append(", BMN_ATR1 = RE.BMN_ATR1");
		sbSQL.append(", BMN_ATR2 = RE.BMN_ATR2");
		sbSQL.append(", BMN_ATR3 = RE.BMN_ATR3");
		sbSQL.append(", BMN_ATR4 = RE.BMN_ATR4");
		sbSQL.append(", BMN_ATR5 = RE.BMN_ATR5");
		sbSQL.append(", URIFLG = RE.URIFLG");
		sbSQL.append(", SENDFLG = RE.SENDFLG");
		sbSQL.append(", UPDKBN=RE.UPDKBN");
		sbSQL.append(", OPERATOR=RE.OPERATOR");
		sbSQL.append(", UPDDT=RE.UPDDT ");
		sbSQL.append(", ADDDT = case when NVL(UPDKBN, 0) = 1 then RE.ADDDT else ADDDT end");
		sbSQL.append("  WHEN NOT MATCHED THEN INSERT(");
		sbSQL.append("  TENCD");
		sbSQL.append(", BMNCD");
		sbSQL.append(", AREACD");
		sbSQL.append(", READTMPTN");
		sbSQL.append(", BMNKN");
		sbSQL.append(", GROUPNO");
		sbSQL.append(", BMNRECEIPTAN");
		sbSQL.append(", BMNRECEIPTKN");
		sbSQL.append(", MIOKBN");
		sbSQL.append(", SHUKEICD");
		sbSQL.append(", WARIBIKIKBN");
		sbSQL.append(", BMNGENKART");
		sbSQL.append(", TENANTKBN");
		sbSQL.append(", LOSSTKBN");
		sbSQL.append(", YOSANKBN_B");
		sbSQL.append(", TANAOROTKBN");
		sbSQL.append(", PCARD_SHUKBN");
		sbSQL.append(", PCARD_IROKBN");
		sbSQL.append(", TENANTCD");
		sbSQL.append(", BMN_ATR1");
		sbSQL.append(", BMN_ATR2");
		sbSQL.append(", BMN_ATR3");
		sbSQL.append(", BMN_ATR4");
		sbSQL.append(", BMN_ATR5");
		sbSQL.append(", URIFLG");
		sbSQL.append(", SENDFLG");
		sbSQL.append(", UPDKBN");
		sbSQL.append(", OPERATOR");
		sbSQL.append(", ADDDT");
		sbSQL.append(", UPDDT");
		sbSQL.append(") VALUES (");
		sbSQL.append(" RE.TENCD");
		sbSQL.append(", RE.BMNCD");
		sbSQL.append(", RE.AREACD");
		sbSQL.append(", RE.READTMPTN");
		sbSQL.append(", RE.BMNKN");
		sbSQL.append(", RE.GROUPNO");
		sbSQL.append(", RE.BMNRECEIPTAN");
		sbSQL.append(", RE.BMNRECEIPTKN");
		sbSQL.append(", RE.MIOKBN");
		sbSQL.append(", RE.SHUKEICD");
		sbSQL.append(", RE.WARIBIKIKBN");
		sbSQL.append(", RE.BMNGENKART");
		sbSQL.append(", RE.TENANTKBN");
		sbSQL.append(", RE.LOSSTKBN");
		sbSQL.append(", RE.YOSANKBN_B");
		sbSQL.append(", RE.TANAOROTKBN");
		sbSQL.append(", RE.PCARD_SHUKBN");
		sbSQL.append(", RE.PCARD_IROKBN");
		sbSQL.append(", RE.TENANTCD");
		sbSQL.append(", RE.BMN_ATR1");
		sbSQL.append(", RE.BMN_ATR2");
		sbSQL.append(", RE.BMN_ATR3");
		sbSQL.append(", RE.BMN_ATR4");
		sbSQL.append(", RE.BMN_ATR5");
		sbSQL.append(", RE.URIFLG");
		sbSQL.append(", RE.UPDKBN");
		sbSQL.append(", RE.SENDFLG");
		sbSQL.append(", RE.OPERATOR");
		sbSQL.append(", RE.ADDDT");
		sbSQL.append(", RE.UPDDT");
		sbSQL.append(")");

		String targetTable =" INAMS.MSTTENBMN ";
		String targetWhere ="";
		ArrayList<String> targetParam = new ArrayList<String>();

		targetParam.add(data.optString("F1"));
		targetParam.add(data.optString("F2"));
		targetWhere =" NVL(UPDKBN, 0) <> "+DefineReport.ValUpdkbn.DEL.getVal()+" and TENCD = ? and BMNCD = ?";

		targetParam = new ArrayList<String>();
		targetParam.add(data.optString("F1"));
		targetParam.add(data.optString("F2"));

		String sendBtnid	= map.get("SENDBTNID");
		if( DefineReport.Button.NEW.getObj().equals(sendBtnid)){
			if(!(StringUtils.isEmpty(data.optString("F1")) && StringUtils.isEmpty(data.optString("F2")))){
				if(!super.checkExclusion(targetTable, targetWhere, targetParam, data.optString("F28"))){
					msg.add(MessageUtility.getDbMessageExclusion(FieldType.DEFAULT, new String[]{}));
					option.put(MsgKey.E.getKey(), msg);
					return option;
				}
			}
		}
		if (msg.size() > 0) {
			// 重複チェック_エラー時
			msgObj.put(MsgKey.E.getKey(), msg);
		}else{
			// 更新処理実行
			if (DefineReport.ID_DEBUG_MODE) System.out.println(sbSQL);
			int count = super.executeSQL(sbSQL.toString(), prmData);
			if(StringUtils.isEmpty(getMessage())) {
				if (DefineReport.ID_DEBUG_MODE) System.out.println("店舗部門を " + MessageUtility.getMessage(Msg.S00003.getVal(),
						new String[] { Integer.toString(dataArray.size()), Integer.toString(count) }));
				msgObj.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00001.getVal()));
			}else{
				msgObj.put(MsgKey.E.getKey(), getMessage());
			}
		}
		return msgObj;
	}
	/** マスタレイアウト */
	public interface MSTLayout {
		public Integer getNo();
		public String getCol();
		public String getTyp();
		public String getId();
		public DataType getDataType();
		public boolean isText();
	}

	/**  商品マスタレイアウト */
	public enum MSTTENBMNLayout implements MSTLayout{
		/** 店コード */
		TENCD(1,"TENCD","SMALLINT"),
		/** 部門 */
		BMNCD(2,"BMNCD","SMALLINT"),
		/** 部門（テナント）名称（漢字） */
		BMNKN(4,"BMNKN","TEXT"),
		/** グループ№ */
		GROUPNO(5,"GROUPNO","SMALLINT"),
		/** 部門レシート名称（漢字） */
		BMNRECEIPTKN(6,"BMNRECEIPTKN","TEXT"),
		/** 部門レシート名称（カナ） */
		BMNRECEIPTAN(7,"BMNRECEIPTAN","TEXT"),
		/** 集計CD */
		SHUKEICD(9,"SHUKEICD","SMALLINT"),
		/** 部門原価率 */
		BMNGENKART(11,"BMNGENKART","SMALLINT"),
		/** エリア */
		AREACD(18,"AREACD","SMALLINT"),
		/** テナントコード */
		TENANTCD(20,"TENANTCD","SMALLINT"),
		/** 部門属性1 */
		BMN_ATR1(21,"BMN_ATR1","SMALLINT"),
		/** 部門属性2 */
		BMN_ATR2(22,"BMN_ATR2","SMALLINT"),
		/** 部門属性3 */
		BMN_ATR3(23,"BMN_ATR3","SMALLINT"),
		/** 部門属性4 */
		BMN_ATR4(24,"BMN_ATR4","SMALLINT"),
		/** 部門属性5 */
		BMN_ATR5(25,"BMN_ATR5","SMALLINT");

		private final Integer no;
		private final String col;
		private final String typ;
		/** 初期化 */
		private MSTTENBMNLayout(Integer no, String col, String typ) {
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

	private enum dbNumericTypeInfo {
		/** SMALLINT */
		SMALLINT(5, -32768, 32768),
		/** INT */
		INT(10, -2147483648 ,  2147483648l),
		/** INTEGER */
		INTEGER(10, -2147483648 ,  2147483648l);

		private final int digit;
		private final long max;
		private final long min;
		/** 初期化 */
		private dbNumericTypeInfo(int digit, long min,long max) {
			this.digit = digit;
			this.min = min;
			this.max = max;
		}
		/** @return digit */
		public int getDigit() { return digit; }
		/** @return min */
		public long getMin() { return min; }
		/** @return max */
		public long getMax() { return max; }
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
		String bmncd	 = map.get("BMNCD");			// 部門コード
		String tencd	 = map.get("TENCD");			// 店コード
		String userId	= userInfo.getId();				// ログインユーザー

		JSONObject msgObj = new JSONObject();

		// 基本INSERT/UPDATE文
		StringBuffer sbSQL;
		ArrayList<String> prmData = new ArrayList<String>();

		// 削除処理：更新区分に"1"（削除）を登録
		sbSQL = new StringBuffer();
		sbSQL.append("  update INAMS.MSTTENBMN MTB set ");
		sbSQL.append("  MTB.UPDKBN = "+DefineReport.ValUpdkbn.DEL.getVal());
		sbSQL.append(", UPDDT = current timestamp");
		sbSQL.append(", SENDFLG = "+DefineReport.Values.SENDFLG_UN.getVal());
		sbSQL.append(", OPERATOR = '"+userId+"'");
		sbSQL.append("  where");
		sbSQL.append("  BMNCD = "+bmncd);
		sbSQL.append("  and TENCD = "+tencd);

		int count = super.executeSQL(sbSQL.toString(), prmData);
		if(StringUtils.isEmpty(getMessage())){
			if (DefineReport.ID_DEBUG_MODE) System.out.println("店舗部門マスタを "+MessageUtility.getMessage(Msg.S00004.getVal(), new String[]{Integer.toString(count)}));
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
	public JSONArray check(HashMap<String, String> map, User userInfo, String sysdate) {
		// パラメータ確認
		JSONArray dataArray = JSONArray.fromObject(map.get("DATA"));	// 更新情報

		String sendBtnid	= map.get("SENDBTNID");	// 呼出しボタン

		MessageUtility mu = new MessageUtility();
		// ①正 .新規
		boolean isNew = DefineReport.Button.NEW.getObj().equals(sendBtnid);
		// ②正 .変更
		boolean isChange = DefineReport.Button.SEL_CHANGE.getObj().equals(sendBtnid);

		List<JSONObject> msgList = this.checkData(isNew,isChange,map,userInfo,sysdate,mu,dataArray);

		JSONArray msgArray = new JSONArray();

		if(msgList.size() > 0){
			msgArray.add(msgList.get(0));
		}
		return msgArray;
	}
	public List<JSONObject> checkData(
			boolean isNew,boolean isChange,
			HashMap<String, String> map, User userInfo, String sysdate1, MessageUtility mu,
			JSONArray dataArray			// 対象情報（主要な更新情報）
			) {
		JSONArray msg = new JSONArray();
		JSONObject data = dataArray.optJSONObject(0);

		String obj = map.get(DefineReport.ID_PARAM_OBJ);
		// 基本データチェック:入力値がテーブル定義と矛盾してないか確認、
		// 1.店舗休日マスタ(複数店舗)
		for(MSTTENBMNLayout colinf : MSTTENBMNLayout.values()) {
			String val = StringUtils.trim(data.optString(colinf.getId()));
			if(StringUtils.isNotEmpty(val)) {
				DataType dtype = null;
				int[] digit = null;
				try{
					DefineReport.InpText inpsetting = DefineReport.InpText.valueOf(colinf.getCol());
					dtype = inpsetting.getType();
					digit = new int[] { inpsetting.getDigit1(), inpsetting.getDigit2() };
				}catch(IllegalArgumentException e){
					dtype = colinf.getDataType();
					digit = colinf.getDigit();
				}
				// ①データ型による文字種チェック
				if (!InputChecker.checkDataType(dtype, val)) {
					JSONObject o = mu.getDbMessageObjDataTypeErr(dtype, new String[] {});
					msg.add(o);
					return msg;
				}
				// ②データ桁チェック
				if (!InputChecker.checkDataLen(dtype, val, digit)) {
					JSONObject o = mu.getDbMessageObjLen(dtype, new String[] {});
					msg.add(o);
					return msg;
				}
			}
		}

		MSTTENBMNLayout[] targetCol = null;

		targetCol = new MSTTENBMNLayout[]{MSTTENBMNLayout.BMNCD,MSTTENBMNLayout.TENCD,MSTTENBMNLayout.BMNKN,MSTTENBMNLayout.GROUPNO,MSTTENBMNLayout.BMNRECEIPTKN,MSTTENBMNLayout.BMNRECEIPTAN,
				MSTTENBMNLayout.SHUKEICD};

		for(MSTTENBMNLayout colinf: targetCol){

			if(StringUtils.isEmpty(data.optString(colinf.getId()))){
				JSONObject o = mu.getDbMessageObj("E00001", new String[]{});
				msg.add(o);
				return msg;
			}
		}
		return msg;
	}
}
