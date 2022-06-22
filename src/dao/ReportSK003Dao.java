package dao;

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
public class ReportSK003Dao extends ItemDao {

	/** SQLリスト保持用変数 */
	ArrayList<String> sqlList = new ArrayList<String>();
	/** SQLのパラメータリスト保持用変数 */
	ArrayList<ArrayList<String>> prmList = new ArrayList<ArrayList<String>>();
	/** SQLログ用のラベルリスト保持用変数 */
	ArrayList<String> lblList = new ArrayList<String>();

	/** 予約発注企画更新のKEY保持用変数 */
	String inputNo_seq = "";			//

	/** マスタレイアウト */
	public interface MSTLayout {
		public Integer getNo();
		public String getCol();
		public String getTyp();
		public String getId();
		public DataType getDataType();
		public boolean isText();
	}

	/**  新店改装店発注 */
	public enum HATSKLayout implements MSTLayout{

		/** 入力№ */
		INPUTNO(1,"INPUTNO","SMALLINT","入力No"),
		/** 店コード */
		TENNO(2,"TENNO","SMALLINT","店コード"),
		/** 発注日 */
		HTDT(3,"HTDT","INTEGER","発注日"),
		/** 納入日 */
		NNDT(4,"NNDT","INTEGER","納入日"),
		/** 商品区分 */
		SHNKBN(5,"SHNKBN","SMALLINT","商品区分"),
		/** 構成ページ */
		KSPAGE(6,"KSPAGE","INTEGER","構成ページ"),
		/** 別伝区分 */
		BDENKBN(7,"BDENKBN","SMALLINT","別伝区分"),
		/** 更新区分 */
		UPDKBN(8,"UPDKBN","SMALLINT","更新区分"),
		/** 送信フラグ */
		SENDFLG(9,"SENDFLG","SMALLINT","送信フラグ"),
		/** オペレータ */
		OPERATOR(10,"OPERATOR","VARCHAR(20)","オペレータ"),
		/** 登録日 */
		ADDDT(11,"ADDDT","TIMESTMP","登録日"),
		/** 更新日 */
		UPDDT(12,"UPDDT","TIMESTMP","更新日"),
		/** 行番号 */
		GYONO(13,"GYONO","INTEGER","行番号"),
;
		private final Integer no;
		private final String col;
		private final String typ;
		private final String text;
		/** 初期化 */
		private HATSKLayout(Integer no, String col, String typ, String text) {
			this.no = no;
			this.col = col;
			this.typ = typ;
			this.text = text;
		}
		/** @return col 列番号 */
		public Integer getNo() { return no; }
		/** @return tbl 列名 */
		public String getCol() { return col; }
		/** @return tbl 列型 */
		public String getTyp() { return typ; }
		/** @return tbl 列名(論理名称) */
		public String getText() { return text; }
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

	/**  新店改装店発注 */
	public enum HATSK_SHNLayout implements MSTLayout{

		/** 入力№ */
		INPUTNO(1,"INPUTNO","SMALLINT","入力№"),
		/** 管理番号 */
		KANRINO(2,"KANRINO","SMALLINT","管理番号"),
		/** 商品コード */
		SHNCD(3,"SHNCD","CHAR","商品コード"),
		/** 数量 */
		SURYO(4,"SURYO","SMALLINT","数量"),
		/** 送信フラグ */
		SENDFLG(5,"SENDFLG","SMALLINT","送信フラグ"),
		/** オペレータ */
		OPERATOR(6,"OPERATOR","VARCHAR","オペレータ"),
		/** 登録日 */
		ADDDT(7,"ADDDT","TIMESTMP","登録日"),
		/** 更新日 */
		UPDDT(8,"UPDDT","TIMESTMP","更新日"),
		/** 行番号 */
		GYONO(9,"GYONO","INTEGER","行番号"),
;
		private final Integer no;
		private final String col;
		private final String typ;
		private final String text;

		/** 初期化 */
		private HATSK_SHNLayout(Integer no, String col, String typ, String text) {
			this.no = no;
			this.col = col;
			this.typ = typ;
			this.text = text;
		}
		/** @return col 列番号 */
		public Integer getNo() { return no; }
		/** @return tbl 列名 */
		public String getCol() { return col; }
		/** @return tbl 列型 */
		public String getTyp() { return typ; }
		/** @return tbl 列名(論理名称) */
		public String getText() { return text; }
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

	/**  CSV取込トラン_商品マスタレイアウト(正マスタとの差分) */
	public enum CSVSHNLayout implements MSTLayout{
		/** SEQ */
		SEQ(1,"SEQ","INTEGER"),
		/** 入力番号 */
		INPUTNO(2,"INPUTNO","INTEGER"),
		/** エラーコード */
		ERRCD(3,"ERRCD","SMALLINT"),
		/** エラー箇所 */
		ERRFLD(4,"ERRFLD","VARCHAR(100)"),
		/** エラー値 */
		ERRVL(5,"ERRVL","VARCHAR(100)"),
		/** エラーテーブル名 */
		ERRTBLNM(6,"ERRTBLNM","VARCHAR(100)"),
		/** CSV登録区分 */
		CSV_UPDKBN(7,"CSV_UPDKBN","CHARACTER(1)"),
		/** 桁指定 */
		KETAKBN(8,"KETAKBN","SMALLINT");

		private final Integer no;
		private final String col;
		private final String typ;
		/** 初期化 */
		private CSVSHNLayout(Integer no, String col, String typ) {
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
		public String getId() { return "F" + Integer.toString(no) ;}
		/** @return col Id */
		public String getId2() { return "F" + Integer.toString(no+HATSKLayout.values().length) ;}
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
	}

	/** 固定値定義（テーブルタイプ）<br>
	 *   */
	public enum TblType {
		/** 正 */
		SEI(1, "(正)"),
		/** 予約 */
		YYK(2, "(予)"),
		/** ジャーナル */
		JNL(3, "(ジャーナル)"),
		/** CSVトラン */
		CSV(4, "(CSVトラン)");

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

	/**
	 * インスタンスを生成します。
	 * @param source
	 */
	public ReportSK003Dao(String JNDIname) {
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

	// 削除処理
	public JSONObject delete(HttpServletRequest request,HttpSession session,  HashMap<String, String> map, User userInfo) {
		String sysdate = (String)request.getSession().getAttribute(Consts.STR_SES_LOGINDT);

		JSONObject option = new JSONObject();
		JSONArray msgList = this.checkDel(map, userInfo, sysdate);

		if(msgList.size() > 0){
			option.put(MsgKey.E.getKey(), msgList);
			return option;
		}

		// 更新処理
		try {
			option = this.deleteData(map, userInfo, sysdate);
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

		String szInputNo	 = getMap().get("INPUTNO"); 			// 入力No
		String sendBtnid	 = getMap().get("SENDBTNID");			// 呼出しボタン

		// パラメータ確認
		// 必須チェック
		if ( szInputNo == null) {
			System.out.println(super.getConditionLog());
			return "";
		}

		// タイトル情報(任意)設定
		List<String> titleList = new ArrayList<String>();

		// 一覧表情報
		StringBuffer sbSQL = new StringBuffer();

		if(StringUtils.equals(DefineReport.Button.SEL_CHANGE.getObj(), sendBtnid)
				|| StringUtils.equals(DefineReport.Button.SEL_REFER.getObj(), sendBtnid)){

			sbSQL.append("select");
			//sbSQL.append(" HSK.INPUTNO");
			sbSQL.append(" right ('00000' || HSK.INPUTNO, 5)");
			sbSQL.append(", HSK.TENNO");
			sbSQL.append(", TEN.TENKN");
			sbSQL.append(", right ('0' || HSK.HTDT, 6)");
			sbSQL.append(", right ('0' || HSK.NNDT, 6)");
			sbSQL.append(", HSK.SHNKBN");
			sbSQL.append(", HSK.BDENKBN");
			sbSQL.append(", TO_CHAR(HSK.ADDDT, 'yy/mm/dd')");
			sbSQL.append(", TO_CHAR(HSK.UPDDT, 'yy/mm/dd')");
			sbSQL.append(", HSK.OPERATOR");
			sbSQL.append(", TO_CHAR(HSK.UPDDT, 'YYYYMMDDHH24MISSNNNNNN') as HDN_UPDDT");
			sbSQL.append(", right ('00000000' || HSK.KSPAGE, 8)");
			sbSQL.append(" from INATK.HATSK HSK");
			sbSQL.append(" left join INAMS.MSTTEN TEN on TEN.TENCD = HSK.TENNO");
			sbSQL.append(" where HSK.UPDKBN = " + DefineReport.ValUpdkbn.NML.getVal());
			sbSQL.append(" and HSK.INPUTNO = "+szInputNo);
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

	}

	/**
	 * チェック処理
	 *
	 * @throws Exception
	 */
	public JSONArray check(HashMap<String, String> map, User userInfo, String sysdate) {

		// パラメータ確認
		JSONArray	dataArray		= JSONArray.fromObject(map.get("DATA"));		// 更新情報(配送グループ)
		JSONArray	dataArrayShn	= JSONArray.fromObject(map.get("DATA_SHN"));	// 更新情報(配送店グループ)
		String		sendBtnid		= map.get("SENDBTNID");							// 呼出しボタン
		String		mode			= map.get("DISOMODE");

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

		if((dataArrayShn.size() == 0 || dataArrayShn.getJSONObject(0).isEmpty()) && !(mode.equals("TypeC") || mode.equals("TypeD"))){
			JSONObject o = mu.getDbMessageObj("E00001", new String[]{"数量"});
			msg.add(o);
			return msg;
		} else {
			for (int i = 0; i < dataArrayShn.size(); i++) {
				JSONObject data = dataArrayShn.getJSONObject(i);
				String shncd	= data.optString("F3");
				String htsu		= data.optString("F4");

				// 商品コードが9999-9994の場合
				if (StringUtils.isEmpty(shncd) || StringUtils.isEmpty(htsu)) {
					if ((!StringUtils.isEmpty(shncd) && !shncd.trim().equals("99999994")) || StringUtils.isEmpty(shncd)) {
						JSONObject o = mu.getDbMessageObj("E11316", new String[]{});
						msg.add(o);
						return msg;
					}
				}
			}
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

	public List<JSONObject> checkData(HashMap<String, String> map, User userInfo, MessageUtility mu,
			JSONArray dataArray,			// 対象情報（主要な更新情報）
			JSONArray dataArrayShn			// 対象情報（追加更新情報）
			) {
		JSONArray msg = new JSONArray();
		JSONObject data	 = new JSONObject();
		ItemList			iL		= new ItemList();

		String sqlcommand		 = "";
		ArrayList<String> paramData	 = new ArrayList<String>();

		String tencd = "";

		// 1.新店改装店発注
		for (int i = 0; i < dataArray.size(); i++) {
			data = dataArray.optJSONObject(i);
			if(data.isEmpty()){
				continue;
			}

			String reqNo = String.valueOf(data.optString(HATSKLayout.GYONO.getId())) + "行目：";

			// 必須入力チェック
			String reqErrMsgCol = "";
			if(StringUtils.isEmpty(reqErrMsgCol) && StringUtils.isEmpty(data.optString("F2"))){
				reqErrMsgCol = HATSKLayout.TENNO.getText();
			}
			if(StringUtils.isEmpty(reqErrMsgCol) && StringUtils.isEmpty(data.optString("F3"))){
				reqErrMsgCol = HATSKLayout.HTDT.getText();
			}
			if(StringUtils.isEmpty(reqErrMsgCol) && StringUtils.isEmpty(data.optString("F4"))){
				reqErrMsgCol = HATSKLayout.NNDT.getText();
			}
			if(StringUtils.isEmpty(reqErrMsgCol) && StringUtils.isEmpty(data.optString("F5"))){
				reqErrMsgCol = HATSKLayout.SHNKBN.getText();
			}
			if(StringUtils.isNotEmpty(reqErrMsgCol)){
				JSONObject o = MessageUtility.getDbMessageIdObj("EX1047", new String[]{reqNo+reqErrMsgCol});
				msg.add(o);
				return msg;
			}

			// 基本データチェック:入力値がテーブル定義と矛盾してないか確認、
			String ErrTblNm = "HATSK";
			for(HATSKLayout colinf: HATSKLayout.values()){

				// 行番号ならチェックはなし
				if (colinf.getId().equals(HATSKLayout.GYONO.getId())) {
					continue;
				}

				String val = StringUtils.trim(data.optString(colinf.getId()));
				if(StringUtils.isNotEmpty(val)){
					DataType dtype = null;
					int[] digit = null;
					try {
						DefineReport.InpText inpsetting = DefineReport.InpText.valueOf(colinf.getCol());
						dtype = inpsetting.getType();
						digit = new int[]{inpsetting.getDigit1(), inpsetting.getDigit2()};
					}catch (IllegalArgumentException e){
						dtype = colinf.getDataType();
						digit = colinf.getDigit();
					}
					// ①データ型による文字種チェック
					if(!InputChecker.checkDataType(dtype, val)){
						JSONObject o = mu.getDbMessageObjDataTypeErr(dtype, reqNo+colinf.getText()+"は");
						//this.setCsvshnErrinfo(o, ErrTblNm, colinf.getCol());
						msg.add(o);
						if(!colinf.isText()){
							//data.element(colinf.getId(), "");		// CSVトラン用に空
						}
						return msg;
					}
					// ②データ桁チェック
					if(!InputChecker.checkDataLen(dtype, val, digit)){
						JSONObject o = mu.getDbMessageObjLen(dtype, reqNo+colinf.getText()+"は");
						//this.setCsvshnErrinfo(o, ErrTblNm, colinf.getCol());
						msg.add(o);
						return msg;
						//data.element(colinf.getId(), "");		// CSVトラン用に空
					}
				}
			}


			//入力チェック：店コード
			if(StringUtils.isNotEmpty(data.optString("F2"))){
				if(StringUtils.isEmpty(tencd)){
					tencd = data.optString("F2");
				}

				// 範囲チェック
				if(Integer.parseInt(data.optString("F2")) < 1 || Integer.parseInt(data.optString("F2")) > 400){
					JSONObject o = MessageUtility.getDbMessageIdObj("E20110", new String[]{reqNo+HATSKLayout.TENNO.getText()});
					msg.add(o);
					return msg;
				}

				// 存在チェック
				paramData  = new ArrayList<String>();
				paramData.add(data.getString("F2"));
				sqlcommand = "select COUNT(TENCD) as value from INAMS.MSTTEN where NVL(UPDKBN, 0) <> 1 and MISEUNYOKBN <> 9 and TENCD = ? ";
				@SuppressWarnings("static-access")
				JSONArray array = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
				if(NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) == 0){
					JSONObject o = mu.getDbMessageObj("EX1077", new String[]{reqNo});
					msg.add(o);
					return msg;
				}
			}

			//入力チェック：発注日
			if(StringUtils.isNotEmpty(data.optString("F3"))){
				String hatdt = data.optString("F3");
				String nndt = data.optString("F4");
				String shoridt = "";

				// 存在チェック
				paramData  = new ArrayList<String>();
				sqlcommand = "select SHORIDT as VALUE from INAAD.SYSSHORIDT where NVL(UPDKBN, 0) <> 1";
				@SuppressWarnings("static-access")
				JSONArray array = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
				if(array.size() > 0){
					shoridt = array.getJSONObject(0).optString("VALUE");
				}

				// 処理日付と比較
				if(StringUtils.isNotEmpty(shoridt)){
					if(Integer.parseInt(hatdt) <= Integer.parseInt(shoridt)){
						JSONObject o = mu.getDbMessageObj("E20127", new String[]{reqNo});
						msg.add(o);
						return msg;
					}
				}

				// 納入日と比較
				if(StringUtils.isNotEmpty(nndt)){
					if(Integer.parseInt(nndt) <= Integer.parseInt(hatdt)){
						JSONObject o = mu.getDbMessageObj("E20264", new String[]{reqNo});
						msg.add(o);
						return msg;
					}
				}
			}

			//入力チェック：別伝区分
			if(StringUtils.isNotEmpty(data.optString("F7"))){

				// 範囲チェック
				if(Integer.parseInt(data.optString("F7")) < 0 || Integer.parseInt(data.optString("F7")) > 8){
					JSONObject o = MessageUtility.getDbMessageIdObj("E20356", new String[]{reqNo});
					msg.add(o);
					return msg;
				}
			}
		}

		// 1.新店改装店発注_商品
		for (int i = 0; i < dataArrayShn.size(); i++) {

			String reqNo = String.valueOf(data.optString(HATSK_SHNLayout.GYONO.getId())) + "行目：";

			data = dataArrayShn.optJSONObject(i);
			if(data.isEmpty()){
				continue;
			}

			// 必須入力チェック
			String reqErrMsgCol = "";
			if(StringUtils.isEmpty(reqErrMsgCol) && StringUtils.isEmpty(data.optString("F3"))){
				reqErrMsgCol = HATSK_SHNLayout.SHNCD.getText();
			}
			if(StringUtils.isEmpty(reqErrMsgCol) && StringUtils.isEmpty(data.optString("F4"))){
				reqErrMsgCol = HATSK_SHNLayout.SURYO.getText();
			}
			if(StringUtils.isNotEmpty(reqErrMsgCol)){
				JSONObject o = MessageUtility.getDbMessageIdObj("EX1047", new String[]{reqNo+reqErrMsgCol});
				msg.add(o);
				return msg;
			}

			// 基本データチェック:入力値がテーブル定義と矛盾してないか確認、
			String ErrTblNm = "HATSK_SHN";
			for(HATSK_SHNLayout colinf: HATSK_SHNLayout.values()){

				// 行番号ならチェックはなし
				if (colinf.getId().equals(HATSK_SHNLayout.GYONO.getId())) {
					continue;
				}

				String val = StringUtils.trim(data.optString(colinf.getId()));
				if(StringUtils.isNotEmpty(val)){
					DataType dtype = null;
					int[] digit = null;
					try {
						DefineReport.InpText inpsetting = DefineReport.InpText.valueOf(colinf.getCol());
						dtype = inpsetting.getType();
						digit = new int[]{inpsetting.getDigit1(), inpsetting.getDigit2()};
					}catch (IllegalArgumentException e){
						dtype = colinf.getDataType();
						digit = colinf.getDigit();
					}
					// ①データ型による文字種チェック
					if(!InputChecker.checkDataType(dtype, val)){
						JSONObject o = mu.getDbMessageObjDataTypeErr(dtype, reqNo+colinf.getText()+"は");
						//this.setCsvshnErrinfo(o, ErrTblNm, colinf.getCol());
						msg.add(o);
						if(!colinf.isText()){
							//data.element(colinf.getId(), "");		// CSVトラン用に空
						}
						return msg;
					}
					// ②データ桁チェック
					if(!InputChecker.checkDataLen(dtype, val, digit)){
						JSONObject o = mu.getDbMessageObjLen(dtype, reqNo+colinf.getText()+"は");
						//this.setCsvshnErrinfo(o, ErrTblNm, colinf.getCol());
						msg.add(o);
						return msg;
						//data.element(colinf.getId(), "");		// CSVトラン用に空
					}
				}
			}

			// 存在チェック：商品コード
			if(StringUtils.isNotEmpty(data.optString("F3"))){

				// 商品マスタ
				paramData  = new ArrayList<String>();
				paramData.add(data.getString("F3"));
				sqlcommand = "select COUNT(SHNCD) as value from INAMS.MSTSHN where NVL(UPDKBN, 0) <> 1 and SHNCD = ? ";
				@SuppressWarnings("static-access")
				JSONArray array = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
				if(NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) == 0){
					JSONObject o = mu.getDbMessageObj("E20124", new String[]{reqNo});
					msg.add(o);
					return msg;
				}

				// 店舗部門マスタ
				if(StringUtils.isNotEmpty(tencd)){
					paramData  = new ArrayList<String>();
					paramData.add(tencd);
					paramData.add(data.getString("F3").substring(0,2));
					sqlcommand = "select COUNT(TENCD) as value from INAMS.MSTTENBMN where NVL(UPDKBN, 0) <> 1 and TENCD = ? and  BMNCD = ?";
					array = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
					if(NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) == 0){
						JSONObject o = mu.getDbMessageObj("E20219", new String[]{reqNo});
						msg.add(o);
						return msg;
					}
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
	public JSONArray checkDel(HashMap<String, String> map, User userInfo, String sysdate) {

		// 削除データ検索用コード
		JSONArray	dataArray	= JSONArray.fromObject(map.get("DATA"));	// 対象情報（主要な更新情報）
		//String		tengpcd		= map.get("TENGPCD");						// 配送店グループコード

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

		/*if(tengpcd.isEmpty() || tengpcd == null){
			msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
			return msg;
		}*/

		// 基本登録情報
		//JSONObject data = dataArray.getJSONObject(0);

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
		JSONArray	dataArray	= JSONArray.fromObject(map.get("DATA"));	// 対象情報（主要な更新情報）

		// 排他チェック用
		JSONArray			msg			= new JSONArray();
		String sendBtnid				= map.get("SENDBTNID");						// 呼出しボタン
		String dispMod					= map.get("DISOMODE");						// 表示状態

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
			}else{
				targetParam.add(inputNo_seq);
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

				if(StringUtils.equals("TypeA", dispMod)){
					//option.put(MsgKey.S.getKey(), MessageUtility.getMessage("入力No"+kkk_seq+"で登録しました。"));
					//option.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00007.getVal(), kkk_seq));
					//option.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00001.getVal()));
					JSONObject sMsg = new JSONObject();

					if(StringUtils.isNotEmpty(inputNo_seq)){
						sMsg = MessageUtility.getDbMessageIdObj("I00002", new String[]{"入力No"+inputNo_seq+"で"});
					}else{
						option.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00001.getVal()));
					}
					option.put(MsgKey.S.getKey(), sMsg);
				}else{
					option.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00001.getVal()));
				}
				//option.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00001.getVal()));
			}
		}else{
			option.put(MsgKey.E.getKey(), getMessage());
		}
		return option;
	}

	/**
	 * 削除処理実行
	 * @return
	 *
	 * @throws Exception
	 */
	private JSONObject deleteData(HashMap<String, String> map, User userInfo, String sysdate) throws Exception {
		// パラメータ確認
		JSONObject	option		= new JSONObject();
		JSONArray	dataArray	= JSONArray.fromObject(map.get("DATA"));	// 対象情報（主要な更新情報）

		String	inputNo			= "";										// 入力No

		// パラメータ確認
		// 必須チェック
		if ( dataArray.isEmpty() || dataArray.size() != 1 || dataArray.getJSONObject(0).isEmpty()) {
			option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
			return option;
		}

		// 基本登録情報
		JSONObject data = dataArray.getJSONObject(0);
		inputNo =data.optString("F1");

		if(StringUtils.isNotEmpty(inputNo)){
			this.createSqlDelHATSK(userInfo, inputNo);
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

		if(StringUtils.equals(DefineReport.Button.NEW.getObj(), sendBtnid)){
			inputNo_seq = this.getINPUTNO_SEQ();
		}

		int maxField = 7;		// Fxxの最大値
		for (int k = 1; k <= maxField; k++) {
			String key = "F" + String.valueOf(k);

			if(k == 1){
				values += String.valueOf(0 + 1);

			}

			if(! ArrayUtils.contains(new String[]{""}, key)){
				String val = data.optString(key);

				// 新規登録時はシーケンスを使用する。
				if(StringUtils.equals("F1", key)){
					if(StringUtils.isNotEmpty(inputNo_seq)){
						val = inputNo_seq;
					}
					inputNo = val;
				}else if(StringUtils.equals("F5", key)){
					if(StringUtils.equals(DefineReport.Values.NONE.getVal(), val)){
						val = "";
					}

				}else if(StringUtils.equals("F7", key)){
					if(StringUtils.isEmpty(val)){
						val = "0";
					}
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

		// 新店改装店発注の登録・更新
		sbSQL = new StringBuffer();
		sbSQL.append(" merge into INATK.HATSK as T using (select");
		sbSQL.append(" INPUTNO");													// 入力№
		sbSQL.append(", TENNO");													// 店コード
		sbSQL.append(", HTDT");														// 発注日
		sbSQL.append(", NNDT");														// 納入日
		sbSQL.append(", SHNKBN");													// 商品区分
		sbSQL.append(", KSPAGE");													// 構成ページ
		sbSQL.append(", BDENKBN");													// 別伝区分
		sbSQL.append(", 0 as SENDFLG");
		sbSQL.append(", "+DefineReport.ValUpdkbn.NML.getVal()+" AS UPDKBN");		// 更新区分：
		sbSQL.append(", '"+userId+"' AS OPERATOR ");								// オペレーター：
		sbSQL.append(", current timestamp AS ADDDT ");								// 登録日：
		sbSQL.append(", current timestamp AS UPDDT ");								// 更新日：
		sbSQL.append(" from (values "+StringUtils.join(valueData, ",")+") as T1(NUM");
		sbSQL.append(", INPUTNO");
		sbSQL.append(", TENNO");
		sbSQL.append(", HTDT");
		sbSQL.append(", NNDT");
		sbSQL.append(", SHNKBN");
		sbSQL.append(", KSPAGE");
		sbSQL.append(", BDENKBN");
		sbSQL.append(" ))as RE on (T.INPUTNO = RE.INPUTNO)");
		sbSQL.append(" when matched then update set");
		sbSQL.append("  INPUTNO=RE.INPUTNO");
		sbSQL.append(" ,TENNO=RE.TENNO");
		sbSQL.append(" ,HTDT=RE.HTDT");
		sbSQL.append(" ,NNDT=RE.NNDT");
		sbSQL.append(" ,SHNKBN=RE.SHNKBN");
		sbSQL.append(" ,KSPAGE=RE.KSPAGE");
		sbSQL.append(" ,BDENKBN=RE.BDENKBN");
		sbSQL.append(" ,UPDKBN=RE.UPDKBN");
		sbSQL.append(" ,SENDFLG=RE.SENDFLG");
		sbSQL.append(" ,OPERATOR=RE.OPERATOR");
		sbSQL.append(", ADDDT = case when NVL(UPDKBN, 0) = 1 then RE.ADDDT else ADDDT end");
		sbSQL.append(" ,UPDDT=RE.UPDDT");
		sbSQL.append(" when not matched then insert (");
		sbSQL.append("  INPUTNO");
		sbSQL.append(" ,TENNO");
		sbSQL.append(" ,HTDT");
		sbSQL.append(" ,NNDT");
		sbSQL.append(" ,SHNKBN");
		sbSQL.append(" ,KSPAGE");
		sbSQL.append(" ,BDENKBN");
		sbSQL.append(" ,UPDKBN");
		sbSQL.append(" ,SENDFLG");
		sbSQL.append(" ,OPERATOR");
		sbSQL.append(" ,ADDDT");
		sbSQL.append(" ,UPDDT");
		sbSQL.append(") values (");
		sbSQL.append("  RE.INPUTNO");
		sbSQL.append(" ,RE.TENNO");
		sbSQL.append(" ,RE.HTDT");
		sbSQL.append(" ,RE.NNDT");
		sbSQL.append(" ,RE.SHNKBN");
		sbSQL.append(" ,RE.KSPAGE");
		sbSQL.append(" ,RE.BDENKBN");
		sbSQL.append(" ,RE.UPDKBN");
		sbSQL.append(" ,RE.SENDFLG");
		sbSQL.append(" ,RE.OPERATOR");
		sbSQL.append(" ,RE.ADDDT");
		sbSQL.append(" ,RE.UPDDT");
		sbSQL.append(")");

		if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("新店改装店発注");

		// クリア
		prmData = new ArrayList<String>();
		valueData = new Object[]{};
		values = "";

		// 新店改装店発注＿内部管理テーブルの登録処理
		/*if(StringUtils.equals(DefineReport.Button.NEW.getObj(), sendBtnid)){
			this.createSqlSYSSK(userInfo, inputNo, 0);
		}*/
		// 新店改装店発注＿商品テーブルの削除処理
		sbSQL = new StringBuffer();
		prmData.add(inputNo);
		sbSQL.append(" delete from INATK.HATSK_SHN where INPUTNO = ?");

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("新店改装店発注_商品");

		// クリア
		prmData = new ArrayList<String>();
		valueData = new Object[]{};
		values = "";


		maxField = 4;		// Fxxの最大値
		int len = dataArrayT.size();
		int kanriNo =  StringUtils.isNotEmpty(this.getINPUTNO(inputNo)) ? Integer.parseInt(this.getINPUTNO(inputNo)): 0;
		for (int i = 0; i < len; i++) {
			JSONObject dataCld = dataArrayT.getJSONObject(i);
			if(! dataCld.isEmpty()){
				for (int k = 1; k <= maxField; k++) {
					String key = "F" + String.valueOf(k);

					if(k == 1){
						values += String.valueOf(0 + 1);

					}

					if(! ArrayUtils.contains(new String[]{""}, key)){
						String val = dataCld.optString(key);

						// 新規登録時はシーケンスを使用する。
						if(StringUtils.equals("F1", key)){
							if(StringUtils.isNotEmpty(inputNo)){
								val = inputNo;
							}
						}else if(StringUtils.equals("F2", key)){
							if(StringUtils.isEmpty(val)){
								// 取得した管理番号+1で登録を行う。
								//kanriNo = ""(kanriNo + ) + kanriNo;
								kanriNo += 1;
								val = ""+kanriNo;
							}
						}else if(StringUtils.equals("F4", key)){
							if(StringUtils.isEmpty(val)){
								// 数量にデフォルト値"0"を設定する。
								val = "0";
							}
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

				// 新店改装店発注_商品の登録・更新
				sbSQL = new StringBuffer();
				sbSQL.append(" merge into INATK.HATSK_SHN as T using (select");
				sbSQL.append(" INPUTNO");													// 入力№
				sbSQL.append(", KANRINO");													// 管理番号
				sbSQL.append(", SHNCD");													// 商品コード
				sbSQL.append(", SURYO");													// 数量
				sbSQL.append(", 0 as SENDFLG");
				sbSQL.append(", '"+userId+"' AS OPERATOR ");								// オペレーター：
				sbSQL.append(", current timestamp AS ADDDT ");								// 登録日：
				sbSQL.append(", current timestamp AS UPDDT ");								// 更新日：
				sbSQL.append(" from (values "+StringUtils.join(valueData, ",")+") as T1(NUM");
				sbSQL.append(", INPUTNO");
				sbSQL.append(", KANRINO");
				sbSQL.append(", SHNCD");
				sbSQL.append(", SURYO");
				sbSQL.append(" ))as RE on (T.INPUTNO = RE.INPUTNO and T.KANRINO = RE.KANRINO)");
				sbSQL.append(" when matched then update set");
				sbSQL.append("  INPUTNO=RE.INPUTNO");
				sbSQL.append(" ,KANRINO=RE.KANRINO");
				sbSQL.append(" ,SHNCD=RE.SHNCD");
				sbSQL.append(" ,SURYO=RE.SURYO");
				sbSQL.append(" ,SENDFLG=RE.SENDFLG");
				sbSQL.append(" ,OPERATOR=RE.OPERATOR");
				//sbSQL.append(" ,ADDDT=RE.ADDDT");
				sbSQL.append(" ,UPDDT=RE.UPDDT");
				sbSQL.append(" when not matched then insert (");
				sbSQL.append("  INPUTNO");
				sbSQL.append(" ,KANRINO");
				sbSQL.append(" ,SHNCD");
				sbSQL.append(" ,SURYO");
				sbSQL.append(" ,SENDFLG");
				sbSQL.append(" ,OPERATOR");
				sbSQL.append(" ,ADDDT");
				sbSQL.append(" ,UPDDT");
				sbSQL.append(") values (");
				sbSQL.append("  RE.INPUTNO");
				sbSQL.append(" ,RE.KANRINO");
				sbSQL.append(" ,RE.SHNCD");
				sbSQL.append(" ,RE.SURYO");
				sbSQL.append(" ,RE.SENDFLG");
				sbSQL.append(" ,RE.OPERATOR");
				sbSQL.append(" ,RE.ADDDT");
				sbSQL.append(" ,RE.UPDDT");
				sbSQL.append(")");

				if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

				sqlList.add(sbSQL.toString());
				prmList.add(prmData);
				lblList.add("新店改装店発注_商品");

				// クリア
				prmData = new ArrayList<String>();
				valueData = new Object[]{};
				values = "";
			}
		}
		// 最終行の管理番号を登録。
		if(len > 0){
			this.createSqlSYSSK(userInfo, inputNo, kanriNo);
		}

		// 新店改装店発注_商品 削除
		if(len == 0 && !StringUtils.equals(DefineReport.Button.NEW.getObj(), sendBtnid)){
			this.createSqlDelHATSK(userInfo, inputNo);
		}

		/*if(dataArrayTDel.size() > 0){
			if(StringUtils.equals("1", allCheck)){
				// 親テーブル削除
				this.createSqlDelHATSK(userInfo, inputNo);

			}else{
				ArrayList<String> paramData	 = new ArrayList<String>();
				for (int j = 0; j < dataArrayTDel.size(); j++) {
					data = dataArrayTDel.getJSONObject(j);
					if(data.isEmpty()){
						continue;
					}

					paramData	 = new ArrayList<String>();
					paramData.add(data.getString("F1"));
					paramData.add(data.getString("F2"));

					sbSQL = new StringBuffer();
					sbSQL.append(" delete from INATK.HATSK_SHN where INPUTNO = ? and KANRINO = ? ");

					sqlList.add(sbSQL.toString());
					prmList.add(paramData);
					lblList.add("新店改装店発注_商品");
				}
			}
		}*/
		return sbSQL.toString();
	}

	/**
	 * マスタ情報取得処理
	 *
	 * @throws Exception
	 */
	public boolean checkMstExist(String outobj, String value) {
		// 関連情報取得
		ItemList iL = new ItemList();
		// 配列準備
		ArrayList<String> paramData = new ArrayList<String>();
		String sqlcommand = "";

		String tbl = "";
		String col = "";
		String rep = "";
		// 商品コード
		if (outobj.equals(DefineReport.InpText.SHNCD.getObj())){
			tbl="INAMS.MSTSHN";
			col="SHNCD";
		}

		// 店コード
		if (outobj.equals(DefineReport.InpText.TENCD.getObj())){
			tbl="INAMS.MSTTEN";
			col="TENCD";
		}


		if(tbl.length()>0&&col.length()>0){
			if(paramData.size() > 0 && rep.length() > 0 ){
				sqlcommand = DefineReport.ID_SQL_CHK_TBL.replace("@T", tbl).replaceAll("@C", col).replace("?", rep);
			}else{
				paramData.add(value);
				sqlcommand = DefineReport.ID_SQL_CHK_TBL.replace("@T", tbl).replaceAll("@C", col);
			}

			@SuppressWarnings("static-access")
			JSONArray array = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
			if(array.size() > 0 && NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) > 0){
				return true;
			}
		}
		return false;
	}

	/**
	 * 新店改装店発注＿内部管理テーブルINSERT/UPDATE SQL作成処理
	 *
	 * @param userInfo
	 * @param Sqlprm		 入力No
	 * @param count			 登録件数
	 *
	 * @throws Exception
	 */
	public JSONObject createSqlSYSSK(User userInfo, String Sqlprm, int count){
		JSONObject result = new JSONObject();

		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();

		String szTable  = "INATK.SYSSK";
		String userId	= userInfo.getId();											// ログインユーザー

		StringBuffer sbSQL;
		sbSQL = new StringBuffer();
		sbSQL.append("merge into "+szTable+" as T");
		sbSQL.append(" using (  select ");
		sbSQL.append(" INPUTNO");													// 入力№
		sbSQL.append(", SUMI_KANRINO");												// 付番済番号
		sbSQL.append(", '"+userId+"' AS OPERATOR ");								// オペレーター：
		sbSQL.append(", current timestamp AS ADDDT ");								// 登録日：
		sbSQL.append(", current timestamp AS UPDDT ");								// 更新日：
		sbSQL.append(" from (values("+Sqlprm+", "+count+")) as T1(INPUTNO, SUMI_KANRINO)");
		sbSQL.append(" ) as RE on (T.INPUTNO = RE.INPUTNO) ");
		sbSQL.append(" when matched then ");
		sbSQL.append(" update set");
		sbSQL.append("  SUMI_KANRINO = "+count);									// F1 : 付番済番号
		sbSQL.append(" ,UPDDT= current timestamp");									// F2 : 更新日
		sbSQL.append(" when not matched then insert (");
		sbSQL.append(" INPUTNO");
		sbSQL.append(", SUMI_KANRINO");
		sbSQL.append(", OPERATOR");
		sbSQL.append(", ADDDT");
		sbSQL.append(", UPDDT");
		sbSQL.append(") values (");
		sbSQL.append(" RE.INPUTNO");
		sbSQL.append(", RE.SUMI_KANRINO");
		sbSQL.append(", RE.OPERATOR");
		sbSQL.append(", RE.ADDDT");
		sbSQL.append(", RE.UPDDT");
		sbSQL.append(")");

		if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("新店改装店発注＿内部管理テーブル");
		return result;
	}

	/**
	 * 新店改装店発注DELETE SQL作成処理
	 *
	 * @param userInfo
	 * @param Sqlprm		 入力No
	 * @throws Exception
	 */
	public JSONObject createSqlDelHATSK(User userInfo, String Sqlprm){
		JSONObject result = new JSONObject();

		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();

		String userId	= userInfo.getId();											// ログインユーザー

		StringBuffer sbSQL;
		sbSQL = new StringBuffer();
		prmData.add(Sqlprm);

		//sbSQL.append("delete from INATK.HATSK where INPUTNO = ?");
		sbSQL.append("UPDATE INATK.HATSK ");
		sbSQL.append("SET ");
		sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.DEL.getVal());
		sbSQL.append(",OPERATOR='" + userId + "'");
		sbSQL.append(",UPDDT=current timestamp ");
		sbSQL.append(" WHERE INPUTNO = ? ");

		if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("新店改装店発注テーブル");
		return result;
	}

	/**
	 * SEQ情報取得処理(新店改装店発注)
	 *
	 * @throws Exception
	 */
	public String getINPUTNO_SEQ() {
		// 関連情報取得
		ItemList iL = new ItemList();
		String sqlColCommand = "VALUES NEXTVAL FOR INAMS.SEQ004";
		@SuppressWarnings("static-access")
		JSONArray array = iL.selectJSONArray(sqlColCommand, null, Defines.STR_JNDI_DS);
		String value = "";
		if(array.size() > 0){
			value = array.optJSONObject(0).optString("1");
		}
		return value;
	}

	/**
	 * SEQ情報取得処理(新店改装店発注)
	 *
	 * @throws Exception
	 */
	public String getINPUTNO(String Sqlprm) {
		// 関連情報取得
		ItemList iL = new ItemList();
		ArrayList<String> paramData	 = new ArrayList<String>();

		paramData.add(Sqlprm);
		String sqlcommand = "select SUMI_KANRINO as F1 from INATK.SYSSK where INPUTNO = ?";
		@SuppressWarnings("static-access")
		JSONArray array = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
		String value = "";
		if(array.size() > 0){
			value = array.optJSONObject(0).optString("F1");
		}
		return value;
	}
}
