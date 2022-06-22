package dao;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.JDBCType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import authentication.bean.User;
import authentication.defines.Consts;
import common.CmnDate;
import common.DefineReport;
import common.DefineReport.DataType;
import common.Defines;
import common.InputChecker;
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
public class Reportx102Dao extends ItemDao {

	/**
	 * インスタンスを生成します。
	 * @param source
	 */
	public Reportx102Dao(String JNDIname) {
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

	private String createCommand() {

		StringBuffer sbSQL = new StringBuffer();
		return sbSQL.toString();
	}

	boolean isTest = true;

	private void outputQueryList() {

		// 検索条件の加工クラス作成
		JsonArrayData jad = new JsonArrayData();
		jad.setJsonString(getJson());

		// 保存用 List (検索情報)作成
		setWhere(new ArrayList<List<String>>());
		List<String> cells = new ArrayList<String>();

		// 期間系
		cells = new ArrayList<String>();
		cells.add( DefineReport.Select.KIKAN.getTxt() );
		cells.add( jad.getJSONText(DefineReport.Select.KIKAN_F.getObj()));
		getWhere().add(cells);

		// 店舗系
		cells = new ArrayList<String>();
		cells.add( DefineReport.Select.KIGYO.getTxt());
		cells.add( DefineReport.Select.TENPO.getTxt());
		cells.add( jad.getJSONText( DefineReport.Select.TENPO.getObj()) );
		getWhere().add(cells);

		// 分類系
		cells = new ArrayList<String>();
		cells.add( DefineReport.Select.BUMON.getTxt());
		cells.add( jad.getJSONText( DefineReport.Select.BUMON.getObj()) );
		getWhere().add(cells);

		// 共通箇所設定
		createCmnOutput(jad);
	}

	/**
	 * 更新処理実行
	 * @return
	 *
	 * @throws Exception
	 */
	private JSONObject updateData(HashMap<String, String> map, User userInfo, String sysdate) throws Exception {

		String dbsysdate = CmnDate.dbDateFormat(sysdate);

		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();
		String values = "";
		int kryoColNum = 10;		// テーブル列数
		// パラメータ確認
		JSONArray dataArray = JSONArray.fromObject(map.get("DATA"));	// 対象情報
		JSONObject msgObj = new JSONObject();
		JSONArray msg = new JSONArray();

		// ログインユーザー情報取得
		int userId	= userInfo.getCD_user();								// ログインユーザー

		String sendBtnid = map.get("SENDBTNID");							// 呼出しボタン
		String bmoncd = map.get("SEL_BMONCD");								// 入力部門コード

		// 更新情報
		for(int j=0; j < dataArray.size(); j++){
			values = "";
			for (int i = 1; i <= kryoColNum; i++) {
				String col = "F" + i;
				String val = dataArray.optJSONObject(j).optString(col);
				if(i==6){		// 更新フラグ
					val = "0";
				}else if(i==7){		// 送信フラグ
					val = "0";
				}else if(i==8){		// オペレータ
					val = ""+userId;
				}else if(i==9){		// 登録日
					val = dbsysdate;
				}else if(i==10){		// 更新日
					val = dbsysdate;
				}
				if(isTest){
					if(StringUtils.isEmpty(val)){
						values += ", null";
					}else{
						if(i==1){
							values += " '"+val+"'";
						}else{
							values += ", '"+val+"'";
						}
					}
				}else{
					prmData.add(val);
					values += ", ?";
				}
			}
		}
		if(values.length()==0){
			msgObj.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
			return msgObj;
		}
		// 基本INSERT/UPDATE文
		StringBuffer sbSQL;

		if(DefineReport.Button.NEW.getObj().equals(sendBtnid)){
			// 論理削除データ有の場合SQL文を追加
			sbSQL = new StringBuffer();
			sbSQL.append("select * from INAMS.MSTKRYO RYO where NVL(RYO.UPDKBN, 0) = 1 and RYO.BMNCD = "+bmoncd);
		}
		// 更新SQL
		sbSQL = new StringBuffer();
		sbSQL.append("merge into INAMS.MSTFTAI as T using (select");
		sbSQL.append("   FTAISHUKBN");// F1 : 風袋種別
		sbSQL.append(" , FTAIECD");// F2 : 風袋枝番
		sbSQL.append(" , NVL(FTAIKN ,'')FTAIKN");// F3 : 風袋名称(カナ)
		sbSQL.append(" , NVL(FTAIAN ,'')FTAIAN");// F4 : 風袋名称(漢字)
		sbSQL.append(" , JRYO");// F5 : 重量
		sbSQL.append(" , UPDKBN");// F6 : 更新区分
		sbSQL.append(" , SENDFLG");// F7 : 送信フラグ
		sbSQL.append(" , OPERATOR");// F8 : オペレータ
		sbSQL.append(" , ADDDT");// F9 : 登録日
		sbSQL.append(" , UPDDT");// F10: 更新日
		sbSQL.append(" from (values ("+values+") ) as T1(");
		sbSQL.append("   FTAISHUKBN");// F1 : 風袋種別
		sbSQL.append(" , FTAIECD");// F2 : 風袋枝番
		sbSQL.append(" , FTAIKN");// F3 : 風袋名称(カナ)
		sbSQL.append(" , FTAIAN");// F4 : 風袋名称(漢字)
		sbSQL.append(" , JRYO");// F5 : 重量
		sbSQL.append(" , UPDKBN");// F6 : 更新区分
		sbSQL.append(" , SENDFLG");// F7 : 送信フラグ
		sbSQL.append(" , OPERATOR");// F8 : オペレータ
		sbSQL.append(" , ADDDT");// F9 : 登録日
		sbSQL.append(", UPDDT)) as RE on (T.FTAISHUKBN = RE.FTAISHUKBN and T.FTAIECD = RE.FTAIECD)");// F10: 更新日
		sbSQL.append(" when matched then update set");
		sbSQL.append("  FTAISHUKBN=RE.FTAISHUKBN");// F1 : 風袋種別
		sbSQL.append(" ,FTAIECD=RE.FTAIECD");// F2 : 風袋枝番
		sbSQL.append(" ,FTAIKN=RE.FTAIKN");// F3 : 風袋名称(カナ)
		sbSQL.append(" ,FTAIAN=RE.FTAIAN");// F4 : 風袋名称(漢字)
		sbSQL.append(" ,JRYO=RE.JRYO");// F5 : 重量
		sbSQL.append(" ,UPDKBN=RE.UPDKBN");// F6 : 更新区分
		sbSQL.append(" ,SENDFLG=RE.SENDFLG");// F7 : 送信フラグ
		sbSQL.append(" ,OPERATOR=RE.OPERATOR");// F8 : オペレータ
		sbSQL.append(" ,ADDDT=RE.ADDDT");// F9 : 登録日
		sbSQL.append(" ,UPDDT=RE.UPDDT");// F10: 更新日
		sbSQL.append(" when not matched then insert values (");
		sbSQL.append("  RE.FTAISHUKBN");// F1 : 風袋種別
		sbSQL.append(" ,RE.FTAIECD");// F2 : 風袋枝番
		sbSQL.append(" ,RE.FTAIKN");// F3 : 風袋名称(カナ)
		sbSQL.append(" ,RE.FTAIAN");// F4 : 風袋名称(漢字)
		sbSQL.append(" ,RE.JRYO");// F5 : 重量
		sbSQL.append(" ,RE.UPDKBN");// F6 : 更新区分
		sbSQL.append(" ,RE.SENDFLG");// F7 : 送信フラグ
		sbSQL.append(" ,RE.OPERATOR");// F8 : オペレータ
		sbSQL.append(" ,RE.ADDDT");// F9 : 登録日
		sbSQL.append(" ,RE.UPDDT )");// F10: 更新日

		if (msg.size() > 0) {
			// 重複チェック_エラー時
			msgObj.put(MsgKey.E.getKey(), msg);

		}else{
			// 更新処理実行
			if (DefineReport.ID_DEBUG_MODE) System.out.println(sbSQL);
			int count = super.executeSQL(sbSQL.toString(), prmData);
			if(StringUtils.isEmpty(getMessage())) {
				if (DefineReport.ID_DEBUG_MODE) System.out.println("風袋マスタを " + MessageUtility.getMessage(Msg.S00003.getVal(),
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
	public enum FTAILayout implements MSTLayout{
		/** 風袋種別 */
		FTAISHUKBN(1,"FTAISHUKBN","SMALLINT"),
		/** 風袋枝番 */
		FTAIECD(2,"FTAIECD","SMALLINT"),
		/** 漢字名 */
		FTAIKN(3,"FTAIKN","VARCHAR(40)"),
		/** カナ名 */
		FTAIAN(4,"FTAIAN","VARCHAR(20)"),
		/** 重量 */
		JRYO(5,"JRYO","SMALLINT");

		private final Integer no;
		private final String col;
		private final String typ;
		/** 初期化 */
		private FTAILayout(Integer no, String col, String typ) {
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
		String futaishukbn = "";
		String futaicd = "";
		for (int i = 0; i < dataArray.size(); i++) {
			JSONObject data = dataArray.getJSONObject(i);
			if(data.isEmpty()){
				continue;
			}
			futaishukbn += "'"+data.optString("F1")+"'";						// 風袋区分
			futaicd += "'"+data.optString("F2")+"'";						// 風袋コード
		}

		if(futaishukbn.length()==0 || futaicd.length()==0){
			msgObj.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00002.getVal()));
			return msgObj;
		}

		// 基本INSERT/UPDATE文
		StringBuffer sbSQL;
		ArrayList<String> prmData = new ArrayList<String>();

		// 削除処理：更新区分に"1"（削除）を登録
		sbSQL = new StringBuffer();
		sbSQL.append("update INAMS.MSTFTAI FTAI set FTAI.UPDKBN = " + DefineReport.ValUpdkbn.DEL.getVal()
		+ " where FTAI.FTAISHUKBN = " + futaishukbn + " and FTAI.FTAIECD = " + futaicd);

		int count = super.executeSQL(sbSQL.toString(), prmData);
		if (StringUtils.isEmpty(getMessage())) {
			if (DefineReport.ID_DEBUG_MODE) System.out.println("風袋マスタを "
					+ MessageUtility.getMessage(Msg.S00004.getVal(), new String[] { Integer.toString(count) }));
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
			boolean isNew, boolean isChange,
			HashMap<String, String> map, User userInfo, String sysdate1, MessageUtility mu,
			JSONArray dataArray			// 対象情報（主要な更新情報）
			) {
		JSONArray msg = new JSONArray();
		String ErrTblNm = "MSTKRYO";
		JSONObject data = dataArray.optJSONObject(0);

		// 基本データチェック:入力値がテーブル定義と矛盾してないか確認、
		// 1.計量器マスタ
		for(FTAILayout colinf : FTAILayout.values()) {
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

		if(!isChange&&this.checkMstExist(isNew,data.optString(FTAILayout.FTAIECD.getId()),data.optString(FTAILayout.FTAISHUKBN.getId()))){
			JSONObject o;
			if(isNew){
				o = mu.getDbMessageObj("E00004", new String[]{});
			}else{
				o = mu.getDbMessageObj("E00006", new String[]{});
			}
			msg.add(o);
			return msg;
		}

		FTAILayout[] targetCol = null;

		targetCol = new FTAILayout[]{FTAILayout.FTAISHUKBN,FTAILayout.FTAIECD};

		for(FTAILayout colinf: targetCol){
			if(StringUtils.isEmpty(data.optString(colinf.getId()))){
				JSONObject o = mu.getDbMessageObj("E00001", new String[]{});
				msg.add(o);
				return msg;
			}
		}
		return msg;
	}
	/**
	 * マスタ情報取得処理
	 *
	 * @throws Exception
	 */
	public boolean checkMstExist(boolean isNew,String value1,String value2) {
		// 関連情報取得
		ItemList iL = new ItemList();
		// 配列準備
		ArrayList<String> paramData = new ArrayList<String>();
		String sqlcommand = "";
		String tbl = "";
		if(isNew){
			// 風袋テーブル
			tbl="INAMS.MSTFTAI";

			//風袋コード
			paramData.add(value1);
			//風袋種類
			paramData.add(value2);
			sqlcommand = DefineReport.ID_SQL_CHK_TBL_MULTI.replace("@T", tbl);
			sqlcommand += " FTAIECD = ? and FTAISHUKBN = ?" ;

			@SuppressWarnings("static-access")
			JSONArray array = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
			if(array.size() > 0 && NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) > 0){
				return true;
			}
		}else{
			String col = "";
			// 風袋コード
			tbl="INAMS.MSTKRYOFTAI";
			col="FTAIECD";

			paramData.add(value1);
			sqlcommand = DefineReport.ID_SQL_CHK_TBL.replace("@T", tbl).replaceAll("@C", col);

			@SuppressWarnings("static-access")
			JSONArray array = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
			if(array.size() > 0 && NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) > 0){
				return true;
			}
		}
		return false;
	}
}
