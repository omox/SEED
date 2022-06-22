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
import common.DefineReport;
import common.DefineReport.DataType;
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
public class ReportTU002Dao extends ItemDao {

	private static String DECI_DIGITS = ",20,10";

	/**
	 * インスタンスを生成します。
	 * @param source
	 */
	public ReportTU002Dao(String JNDIname) {
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

		String szmoyskbn	 = getMap().get("MOYSKBN");			// 催し区分
		String szmoysstdt	 = getMap().get("MOYSSTDT");		// 催し日
		String szmoysrban	 = getMap().get("MOYSRBAN");		// 催し連番
		String tencd		 = userInfo.getTenpo();				// 店コード(担当店舗)
		String kijundt = common.CmnDate.dateFormat(common.CmnDate.convYYMMDD(szmoysstdt));
		// タイトル情報(任意)設定
		List<String> titleList = new ArrayList<String>();
		// DB検索用パラメータ
		ArrayList<String> paramData = new ArrayList<String>();
		// 基本情報取得
		JSONArray array = getTOKQJU_MOYData(getMap());

		// 一覧表情報
		StringBuffer sbSQL = new StringBuffer();

		sbSQL.append(DefineReport.ID_SQL_CMN_WEEK);
		sbSQL.append(" select");
		sbSQL.append(" T1.SHNCD");
		sbSQL.append(", T1.SHNKN");
		sbSQL.append(", T1.RG_GENKAAM");
		sbSQL.append(", T1.RG_BAIKAAM");
		sbSQL.append(", T1.RG_SOUBAIK");
		sbSQL.append(", T1.IRISU");
		sbSQL.append(", T1.GENKAAM");
		sbSQL.append(", T1.HONBAIK");
		sbSQL.append(", T1.BAIKAAM");
		sbSQL.append(", trunc(double(T1.HONBAIK - T1.GENKAAM) * 100 / T1.HONBAIK, 2)");
		sbSQL.append(", T1.NNDT || W1.JWEEK");
		sbSQL.append(", T1.HTSU");
		sbSQL.append(", T1.TSEIKBN");
		sbSQL.append(", T1.QASMDT");
		sbSQL.append(", T1.KANRINO");
		sbSQL.append(", T1.HDN_UPDDT");
		sbSQL.append(", T1.HTSU AS HDN_HTSU");
		sbSQL.append(" from (");
		sbSQL.append(" select");
		sbSQL.append(" SHN.SHNCD");
		sbSQL.append(", SHN.SHNKN");
		sbSQL.append(", SHN.RG_GENKAAM");
		sbSQL.append(", SHN.RG_BAIKAAM");
		sbSQL.append(","+DefineReport.ID_SQL_TOKBAIKA_COL_SOU.replaceAll("@BAIKA", "SHN.RG_BAIKAAM").replaceAll("@DT", "?")+ " as RG_SOUBAIK");
		paramData.add(StringUtils.defaultIfEmpty(kijundt, "0"));
		paramData.add(StringUtils.defaultIfEmpty(kijundt, "0"));
		paramData.add(StringUtils.defaultIfEmpty(kijundt, "0"));
		paramData.add(StringUtils.defaultIfEmpty(kijundt, "0"));
		sbSQL.append(", TASS.IRISU");
		sbSQL.append(", TASS.GENKAAM");
		sbSQL.append(", TASS.BAIKAAM");
		sbSQL.append(","+DefineReport.ID_SQL_TOKBAIKA_COL_HON.replaceAll("@BAIKA", "TASS.BAIKAAM").replaceAll("@DT", "?")+ " as HONBAIK");
		paramData.add(StringUtils.defaultIfEmpty(kijundt, "0"));
		paramData.add(StringUtils.defaultIfEmpty(kijundt, "0"));
		paramData.add(StringUtils.defaultIfEmpty(kijundt, "0"));
		paramData.add(StringUtils.defaultIfEmpty(kijundt, "0"));
		sbSQL.append(", TO_CHAR(TO_DATE('20' || right ('0' || TASS.NNDT, 6), 'YYYYMMDD'), 'YY/MM/DD') as NNDT");
		sbSQL.append(", DAYOFWEEK(TO_DATE('20' || right ('0' || TASS.NNDT, 6), 'YYYYMMDD')) as NNDT_WNUM");
		sbSQL.append(", SUBSTRING(TASS.TENHTSU_ARR, 1 + (? - 1) * 5, 5) AS HTSU");
		paramData.add(tencd);
		sbSQL.append(", TASS.TSEIKBN");
		sbSQL.append(", MYCD.NNEDDT");
		sbSQL.append(", TASM.QASMDT");
		sbSQL.append(", TASS.KANRINO");
		sbSQL.append(", TO_CHAR(TASS.UPDDT, 'YYYYMMDDHH24MISSNNNNNN') as HDN_UPDDT");
		sbSQL.append(" from INATK.TOKQJU_MOY TASM");
		sbSQL.append(" inner join INATK.TOKQJU_SHN TASS on TASM.MOYSKBN = TASS.MOYSKBN and TASM.MOYSSTDT = TASS.MOYSSTDT and TASM.MOYSRBAN = TASS.MOYSRBAN and SUBSTRING(TASS.TENHTSU_ARR, 1 + (? - 1) * 5, 5) <> ' ' and NVL(TASS.UPDKBN, 0) <> 1");
		paramData.add(tencd);
		sbSQL.append(" inner join INATK.TOKMOYCD MYCD on MYCD.MOYSKBN = TASM.MOYSKBN and MYCD.MOYSSTDT = TASM.MOYSSTDT and MYCD.MOYSRBAN = TASM.MOYSRBAN and NVL(MYCD.UPDKBN, 0) <> 1");
		sbSQL.append(" inner join INAMS.MSTSHN SHN on TASS.SHNCD = SHN.SHNCD and NVL(SHN.UPDKBN, 0) <> 1");
		sbSQL.append(" left join INAMS.MSTSHN M0 on M0.SHNCD = SHN.SHNCD and NVL(M0.UPDKBN, 0) <> 1");
		sbSQL.append(" left join INAMS.MSTBMN M1 on M1.BMNCD = SHN.BMNCD and NVL(M1.UPDKBN, 0) <> 1");
		sbSQL.append(" left join INAMS.MSTZEIRT M2 on M2.ZEIRTKBN = M0.ZEIRTKBN and NVL(M2.UPDKBN, 0) <> 1");
		sbSQL.append(" left join INAMS.MSTZEIRT M3 on M3.ZEIRTKBN = M0.ZEIRTKBN_OLD and NVL(M3.UPDKBN, 0) <> 1");
		sbSQL.append(" left join INAMS.MSTZEIRT M4 on M4.ZEIRTKBN = M1.ZEIRTKBN and NVL(M4.UPDKBN, 0) <> 1");
		sbSQL.append(" left join INAMS.MSTZEIRT M5 on M5.ZEIRTKBN = M1.ZEIRTKBN_OLD and NVL(M5.UPDKBN, 0) <> 1");
		sbSQL.append(" where TASM.MOYSKBN = ?");
		paramData.add(szmoyskbn);
		sbSQL.append(" and TASM.MOYSSTDT = ?");
		paramData.add(szmoysstdt);
		sbSQL.append(" and TASM.MOYSRBAN = ?");
		paramData.add(szmoysrban);
		sbSQL.append(" and NVL(TASM.UPDKBN, 0) <> 1 ");
		sbSQL.append(" ) T1");
		sbSQL.append(" left outer join WEEK W1 on T1.NNDT_WNUM = W1.CWEEK");
		sbSQL.append(" order by T1.KANRINO, T1.SHNCD, T1.NNEDDT");
		setParamData(paramData);

		// オプション情報（タイトル）設定
		JSONObject option = new JSONObject();
		option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
		option.put("rows_", array);
		setOption(option);

		if (DefineReport.ID_DEBUG_MODE)	System.out.println(getClass().getSimpleName()+"[sql]"+sbSQL.toString()+"[pam]"+paramData.toString());
		return sbSQL.toString();
	}

	/**
	 * 正情報取得処理
	 *
	 * @throws Exception
	 */
	public JSONArray getTOKQJU_MOYData(HashMap<String, String> map) {
		String szMoyskbn	= map.get("MOYSKBN");			// 催し区分
		String szMoysstdt	= map.get("MOYSSTDT");			// 催しコード（催し開始日）
		String szMoysrban	= map.get("MOYSRBAN");			// 催し連番

		ArrayList<String> paramData = new ArrayList<String>();

		StringBuffer sbSQL = new StringBuffer();

		sbSQL.append("select");
		sbSQL.append(" MYCD.MOYKN as F1");
		sbSQL.append(", TO_CHAR(QJUM.ADDDT, 'yy/mm/dd') as F2");
		sbSQL.append(", TO_CHAR(QJUM.UPDDT, 'yy/mm/dd') as F3");
		sbSQL.append(", QJUM.OPERATOR as F4");
		sbSQL.append(", TO_CHAR(QJUM.UPDDT, 'YYYYMMDDHH24MISSNNNNNN') as F5");
		sbSQL.append(" from INATK.TOKQJU_MOY QJUM");
		sbSQL.append(" inner join INATK.TOKMOYCD MYCD on MYCD.MOYSKBN = QJUM.MOYSKBN and MYCD.MOYSSTDT = QJUM.MOYSSTDT and MYCD.MOYSRBAN = QJUM.MOYSRBAN");
		sbSQL.append(" where QJUM.MOYSKBN = "+szMoyskbn);
		sbSQL.append(" and QJUM.MOYSSTDT = "+szMoysstdt);
		sbSQL.append(" and QJUM.MOYSRBAN =  "+szMoysrban);

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
		msgObj.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00001.getVal()));
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
		String sysdate = (String)request.getSession().getAttribute(Consts.STR_SES_LOGINDT);
		// 更新情報チェック(基本JS側で制御)
		JSONObject msgObj = new JSONObject();
		JSONArray msg = this.check(map, userInfo, sysdate);

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

	boolean isTest = true;

	/** SQLリスト保持用変数 */
	ArrayList<String> sqlList = new ArrayList<String>();
	/** SQLのパラメータリスト保持用変数 */
	ArrayList<ArrayList<String>> prmList = new ArrayList<ArrayList<String>>();
	/** SQLログ用のラベルリスト保持用変数 */
	ArrayList<String> lblList = new ArrayList<String>();

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

		JSONArray	dataArrayShn= JSONArray.fromObject(map.get("DATA_SHN"));	// 対象情報（主要な更新情報）

		// 排他チェック用
		JSONArray	msg			= new JSONArray();

		// パラメータ確認
		// 必須チェック
		if ( dataArray.isEmpty() || dataArray.size() != 1 || dataArray.getJSONObject(0).isEmpty()) {
			option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
			return option;
		}

		// 基本登録情報
		JSONObject data = dataArray.getJSONObject(0);
		//JSONObject datashn = dataArrayShn.getJSONObject(0);

		// 予約発注_納品日、予約発注_店舗INSERT/UPDATE処理
		this.createSqlQJU(data,map,userInfo);

		// 排他チェック実行
		if(!checkExclusionTen(data,dataArrayShn,userInfo)){
 			msg.add(MessageUtility.getDbMessageMismatchUpdateDate());
 			option.put(MsgKey.E.getKey(), msg);
 			option.put("DATA_SHN", dataArrayShn);
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
	public enum TYHBLayout implements MSTLayout{
		/** 店コード */
		TENCD(1,"TENCD","SMALLINT"),
		/** 部門コード */
		BMNCD(3,"BMNCD","SMALLINT");


		private final Integer no;
		private final String col;
		private final String typ;
		/** 初期化 */
		private TYHBLayout(Integer no, String col, String typ) {
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
	private JSONObject deleteData(HashMap<String, String> map, User userInfo) throws Exception {
		// パラメータ確認
		JSONArray dataArray = JSONArray.fromObject(map.get("DATA"));	// 対象情報
		JSONObject msgObj = new JSONObject();

		// 更新情報
		String tencd = "";
		String bmncd = "";
		for (int i = 0; i < dataArray.size(); i++) {
			JSONObject data = dataArray.getJSONObject(i);
			if(data.isEmpty()){
				continue;
			}
			tencd = data.optString("F1");						// 店コード
			bmncd = data.optString("F3");						// 部門コード
		}

		if(tencd.length()==0 || bmncd.length()==0){
			msgObj.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00002.getVal()));
			return msgObj;
		}

		// 基本INSERT/UPDATE文
		StringBuffer sbSQL;
		ArrayList<String> prmData = new ArrayList<String>();

		// 削除処理：更新区分に"1"（削除）を登録
		sbSQL = new StringBuffer();

		sbSQL.append("delete from INAMS.MSTTENYOBIHTBMN where");
		sbSQL.append(" TENCD = cast(? as SMALLINT)");
		sbSQL.append(" and BMNCD = cast(? as SMALLINT)");

		if (DefineReport.ID_DEBUG_MODE)	System.out.println(tencd);
		prmData.add(tencd);
		prmData.add(bmncd);
		int count = super.executeSQL(sbSQL.toString(), prmData);
		if (StringUtils.isEmpty(getMessage())) {
			if (DefineReport.ID_DEBUG_MODE) System.out.println("店舗曜日別発注部門マスタを " + MessageUtility.getMessage(Msg.S00004.getVal(), new String[] { Integer.toString(count) }));
			msgObj.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00002.getVal()));
		} else {
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
		JSONArray	dataArray		= JSONArray.fromObject(map.get("DATA"));		// 更新情報(配送グループ)
		JSONArray	dataArraySHN	= JSONArray.fromObject(map.get("DATA_SHN"));	// 更新情報(配送店グループ)
		String		sendBtnid		= map.get("SENDBTNID");							// 呼出しボタン
		String sqlcommand		 = "";
		ArrayList<String> paramData	 = new ArrayList<String>();

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

		for (int i = 0; i < dataArraySHN.size(); i++) {
			data = dataArraySHN.getJSONObject(i);
			if(data.isEmpty()){
				continue;
			}

			// 重複チェック：商品コード
			/*if(StringUtils.isEmpty(data.optString("F5"))){
				// 管理番号が空の時
				paramData  = new ArrayList<String>();
				paramData.add(data.getString("F1"));
				paramData.add(data.getString("F2"));
				paramData.add(data.getString("F3"));
				paramData.add(data.getString("F4"));
				paramData.add(data.getString("F6"));
				sqlcommand = "select COUNT(SHNCD) as value from INATK.TOKSO_SHN where NVL(UPDKBN, 0) <> 1 and MOYSKBN = ? and MOYSSTDT = ? and MOYSRBAN = ? and BMNCD = ? and SHNCD = ? ";

				@SuppressWarnings("static-access")
				JSONArray array = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
				if(array.size() > 0 && NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) > 0){
					JSONObject o = mu.getDbMessageObj("E00004", "商品コード");
					msg.add(o);
					return msg;
				}
			}*/
		}
		return msg;
	}

	public boolean checkExclusionTen(JSONObject data, JSONArray dataArraySHN, User userInfo) {

		// 格納用変数
		boolean flag = true;
		StringBuffer			sbSQL	= new StringBuffer();
		ItemList				iL		= new ItemList();
		JSONArray				dbDatas	= new JSONArray();

		// DB検索用パラメータ
		ArrayList<String> paramData = new ArrayList<String>();

		String moyskbn	= data.optString("F1");
		String moysstdt	= data.optString("F2");
		String moysrban	= data.optString("F3");

		String UPDDT_old = "";	// 更新日（画面表示時点）
		String UPDDT_now = "";	// 更新日（現時点）

		for (int i = 0; i < dataArraySHN.size(); i++) {
			data = dataArraySHN.getJSONObject(i);
			if(data.isEmpty()){
				continue;
			}

			sbSQL		= new StringBuffer();
			paramData	= new ArrayList<String>();
			dbDatas		= new JSONArray();

			sbSQL.append(" SELECT TO_CHAR(UPDDT,'YYYYMMDDHH24MISSNNNNNN') AS UPDDT FROM INATK.TOKQJU_SHN ");
			sbSQL.append(" where MOYSKBN = ?");
			paramData.add(moyskbn);
			sbSQL.append(" and MOYSSTDT = ?");
			paramData.add(moysstdt);
			sbSQL.append(" and MOYSRBAN = ?");
			paramData.add(moysrban);
			sbSQL.append(" and KANRINO = ?");
			paramData.add(data.optString("F4"));
			sbSQL.append(" and NVL(UPDKBN, 0) <> 1 ");
			if (DefineReport.ID_DEBUG_MODE)	{}
			//System.out.println(sbSQL.toString()+paramData.toString());

			dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

			if (dbDatas.size() == 0){
				return false;
			} else {

				UPDDT_old = data.optString("F7");						// 更新日（画面表示時点）
				UPDDT_now = dbDatas.getJSONObject(0).optString("UPDDT");// 更新日（現時点）

				// 更新日時比較
				if (!UPDDT_old.equals(UPDDT_now) ) {
					data.elementOpt("F7", UPDDT_now);
					flag = false;
				}
			}
		}
		return flag;
	}

	/**
	 * 店舗アンケート付き送付け_催し、商品INSERT/UPDATE処理
	 *
	 * @param data
	 * @param map
	 * @param userInfo
	 */
	public String createSqlQJU(JSONObject data, HashMap<String, String> map, User userInfo){

		StringBuffer	sbSQL			= new StringBuffer();
		JSONArray		dataArraySHN	= JSONArray.fromObject(map.get("DATA_SHN"));	// 更新情報(予約発注_納品日)

		// ログインユーザー情報取得
		String userId		= userInfo.getId();							// ログインユーザー
		String userTenpo	= userInfo.getTenpo();						// 担当店舗

		String MOYSKBN		= "";										// 催し区分
		String MOYSSTDT		= "";										// 催し開始日
		String MOYSRBAN		= "";										// 催し連番
		String KANRINO		= "";										// 管理番号
		String HATSU		= "";										// 発注数

		ArrayList<String> prmData = new ArrayList<String>();
		Object[] valueData = new Object[]{};
		String values = "";

		int maxField = 3;		// Fxxの最大値
		for (int k = 1; k <= maxField; k++) {
			String key = "F" + String.valueOf(k);

			if(k == 1){
				values += String.valueOf(0 + 1);
			}

			if(! ArrayUtils.contains(new String[]{""}, key)){
				String val = data.optString(key);

				// 催し区分を保持
				if(StringUtils.equals("F1", key)){
					MOYSKBN = val;

				// 催し開始日を保持
				}else if(StringUtils.equals("F2", key)){
					MOYSSTDT = val;

				// 催し連番を保持
				}else if(StringUtils.equals("F3", key)){
					MOYSRBAN = val;
				}

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

		sbSQL.append(" merge into INATK.TOKQJU_MOY as T using (select");
		sbSQL.append(" MOYSKBN");													// 催し区分
		sbSQL.append(", MOYSSTDT");													// 催し開始日
		sbSQL.append(", MOYSRBAN");													// 催し連番
		sbSQL.append(", "+DefineReport.ValUpdkbn.NML.getVal()+" AS UPDKBN");		// 更新区分
		sbSQL.append(", 0 AS SENDFLG");												// 送信フラグ
		sbSQL.append(", '"+userId+"' AS OPERATOR ");								// オペレーター
		sbSQL.append(", current timestamp AS ADDDT ");								// 登録日
		sbSQL.append(", current timestamp AS UPDDT ");								// 更新日
		sbSQL.append(" from (values "+StringUtils.join(valueData, ",")+") as T1(NUM, ");
		sbSQL.append(" MOYSKBN");
		sbSQL.append(", MOYSSTDT");
		sbSQL.append(", MOYSRBAN");
		sbSQL.append(" ))as RE on (T.MOYSKBN = RE.MOYSKBN and T.MOYSSTDT = RE.MOYSSTDT and T.MOYSRBAN = RE.MOYSRBAN)");
		sbSQL.append(" when matched then update set");
		sbSQL.append(" MOYSKBN=RE.MOYSKBN");
		sbSQL.append(", MOYSSTDT=RE.MOYSSTDT");
		sbSQL.append(", MOYSRBAN=RE.MOYSRBAN");
		sbSQL.append(", UPDKBN=RE.UPDKBN");
		sbSQL.append(", SENDFLG=RE.SENDFLG");
		sbSQL.append(", OPERATOR=RE.OPERATOR");
		sbSQL.append(", UPDDT=RE.UPDDT");
		sbSQL.append(" when not matched then insert (");
		sbSQL.append(" MOYSKBN");
		sbSQL.append(", MOYSSTDT");
		sbSQL.append(", MOYSRBAN");
		sbSQL.append(", UPDKBN");
		sbSQL.append(", SENDFLG");
		sbSQL.append(", OPERATOR");
		sbSQL.append(", ADDDT");
		sbSQL.append(", UPDDT");
		sbSQL.append(") values (");
		sbSQL.append(" RE.MOYSKBN");
		sbSQL.append(", RE.MOYSSTDT");
		sbSQL.append(", RE.MOYSRBAN");
		sbSQL.append(", RE.UPDKBN");
		sbSQL.append(", RE.SENDFLG");
		sbSQL.append(", RE.OPERATOR");
		sbSQL.append(", RE.ADDDT");
		sbSQL.append(", RE.UPDDT");
		sbSQL.append(")");

		if (DefineReport.ID_DEBUG_MODE)	System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("店舗アンケート付き送付け_催し");

		// クリア
		prmData = new ArrayList<String>();
		valueData = new Object[]{};
		values = "";


		maxField = 6;		// Fxxの最大値
		int len = dataArraySHN.size();
		for (int i = 0; i < len; i++) {
			JSONObject dataT = dataArraySHN.getJSONObject(i);
			if(! dataT.isEmpty()){
				for (int k = 1; k <= maxField; k++) {
					String key = "F" + String.valueOf(k);

					if(k == 1){
						values += String.valueOf(0 + 1);
					}

					if(! ArrayUtils.contains(new String[]{""}, key)){
						String val = dataT.optString(key);

						// 管理番号を保持
						if(StringUtils.equals("F4", key)){
							KANRINO = val;

						// 発注数を保持
						}else if(StringUtils.equals("F5", key)){
							HATSU = val;

						// 店発注数配列を設定
						}else if(StringUtils.equals("F6", key)){
							val = this.getARR(MOYSKBN, MOYSSTDT, MOYSRBAN, KANRINO, HATSU, userInfo);
						}

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

			if(valueData.length >= 100 || (i+1 == len && valueData.length > 0)){

				// 予約発注_店舗の登録・更新
				sbSQL = new StringBuffer();
				sbSQL.append(" merge into INATK.TOKQJU_SHN as T using (select");
				sbSQL.append(" MOYSKBN");													// 催し区分
				sbSQL.append(", MOYSSTDT");													// 催し開始日
				sbSQL.append(", MOYSRBAN");													// 催し連番
				sbSQL.append(", KANRINO");													// 管理番号
				sbSQL.append(", HTSU");														// 発注数
				sbSQL.append(", TENHTSU_ARR");												// 店発注数配列
				sbSQL.append(", "+DefineReport.ValUpdkbn.NML.getVal()+" AS UPDKBN");		// 更新区分
				sbSQL.append(", 0 AS SENDFLG");												// 送信フラグ
				sbSQL.append(", '"+userId+"' AS OPERATOR ");								// オペレーター
				sbSQL.append(", current timestamp AS UPDDT ");								// 更新日
				sbSQL.append(" from (values "+StringUtils.join(valueData, ",")+") as T1(NUM, ");
				sbSQL.append(" MOYSKBN");
				sbSQL.append(", MOYSSTDT");
				sbSQL.append(", MOYSRBAN");
				sbSQL.append(", KANRINO");
				sbSQL.append(", HTSU");
				sbSQL.append(", TENHTSU_ARR");
				sbSQL.append(" ))as RE on (T.MOYSKBN = RE.MOYSKBN and T.MOYSSTDT = RE.MOYSSTDT and T.MOYSRBAN = RE.MOYSRBAN and T.KANRINO = RE.KANRINO)");
				sbSQL.append(" when matched then update set");
				sbSQL.append(" MOYSKBN=RE.MOYSKBN");
				sbSQL.append(", MOYSSTDT=RE.MOYSSTDT");
				sbSQL.append(", MOYSRBAN=RE.MOYSRBAN");
				sbSQL.append(", KANRINO=RE.KANRINO");
				sbSQL.append(", HTSU=RE.HTSU");
				sbSQL.append(", TENHTSU_ARR=RE.TENHTSU_ARR");
				//sbSQL.append(", TENHTSU_ARR = left(TENHTSU_ARR, ("+userTenpo+" - 1) * 5) || right('00000' || RE.HTSU, 5) || TO_CHAR(SUBSTRING(TENHTSU_ARR, 1 + ("+userTenpo+") * 5))");
				sbSQL.append(", UPDKBN=RE.UPDKBN");
				sbSQL.append(", SENDFLG=RE.SENDFLG");
				sbSQL.append(", OPERATOR=RE.OPERATOR");
				sbSQL.append(", UPDDT=RE.UPDDT");
				/*sbSQL.append(" when not matched then insert (");
				sbSQL.append(" MOYSKBN");
				sbSQL.append(", MOYSSTDT");
				sbSQL.append(", MOYSRBAN");
				sbSQL.append(", KANRINO");
				sbSQL.append(", HTSU");
				sbSQL.append(", TENHTSU_ARR");
				sbSQL.append(", UPDKBN");
				sbSQL.append(", SENDFLG");
				sbSQL.append(", OPERATOR");
				sbSQL.append(", ADDDT");
				sbSQL.append(", UPDDT");
				sbSQL.append(") values (");
				sbSQL.append(" RE.MOYSKBN");
				sbSQL.append(", RE.MOYSSTDT");
				sbSQL.append(", RE.MOYSRBAN");
				sbSQL.append(", RE.KANRINO");
				sbSQL.append(", RE.HTSU");
				sbSQL.append(", RE.TENHTSU_ARR");
				sbSQL.append(", RE.UPDKBN");
				sbSQL.append(", RE.SENDFLG");
				sbSQL.append(", RE.OPERATOR");
				sbSQL.append(", RE.ADDDT");
				sbSQL.append(", RE.UPDDT");
				sbSQL.append(")");*/

				if (DefineReport.ID_DEBUG_MODE)	System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

				sqlList.add(sbSQL.toString());
				prmList.add(prmData);
				lblList.add("店舗アンケート付き送付け_商品");

				// クリア
				prmData = new ArrayList<String>();
				valueData = new Object[]{};
				values = "";
			}
		}
		return sbSQL.toString();
	}

	/**
	 * マスタ情報取得処理
	 *
	 * @throws Exception
	 */
	public boolean checkMstExist(boolean isNew,String outobj,String value1,String value2) {
		// 関連情報取得
		ItemList iL = new ItemList();
		// 配列準備
		ArrayList<String> paramData = new ArrayList<String>();
		String sqlcommand = "";
		String tbl = "";
		String col = "";
		String sqlwhere="";

		// 店舗コード
		if (outobj.equals(DefineReport.InpText.BMNCD.getObj())) {
			tbl="INAMS.MSTTEN";
			col="TENCD";
		}
		// 部門コード
		if (outobj.equals(DefineReport.InpText.BMNCD.getObj())) {
			tbl="INAMS.MSTBMN";
			col="BMNCD";
		}

		paramData.add(value1);
		paramData.add(value2);
		sqlcommand = DefineReport.ID_SQL_CHK_TBL_MULTI.replace("@T", tbl)+sqlwhere;

		@SuppressWarnings("static-access")
		JSONArray array = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
		if(array.size() > 0 && NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) > 0){
			return true;
		}

		return false;
	}

	/**
	 * 店発注数配列取得
	 *
	 * @throws Exception
	 */
	public String getARR(String MOYSKBN, String MOYSSTDT, String MOYSRBAN, String KANRINO, String SURYO, User userInfo) {
		// 関連情報取得
		ItemList iL = new ItemList();
		StringBuffer sbSQL = new StringBuffer();
		ArrayList<String> paramData	 = new ArrayList<String>();

		String userTenpo = userInfo.getTenpo();				// 担当店舗

		paramData.add(SURYO);
		paramData.add(MOYSKBN);
		paramData.add(MOYSSTDT);
		paramData.add(MOYSRBAN);
		paramData.add(KANRINO);

		sbSQL = new StringBuffer();

		sbSQL.append("select");
		sbSQL.append(" left (TENHTSU_ARR, ("+userTenpo+" - 1) * 5) || right ('00000' || ?, 5) || TO_CHAR(SUBSTRING(TENHTSU_ARR, 1 + ("+userTenpo+") * 5)) as value");
		sbSQL.append(" from INATK.TOKQJU_SHN");
		sbSQL.append(" where MOYSKBN = ?");
		sbSQL.append(" and MOYSSTDT = ?");
		sbSQL.append(" and MOYSRBAN = ?");
		sbSQL.append(" and KANRINO = ?");

		@SuppressWarnings("static-access")
		JSONArray array = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);
		String value = "";
		if(array.size() > 0){
			value = array.optJSONObject(0).optString("VALUE");
		}
		return value;
	}

}

