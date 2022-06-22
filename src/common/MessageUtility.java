package common;

import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import common.DefineReport.DataType;
import common.DefineReport.Option;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * メッセージ関連ユーティリティクラス
 *
 * @author EATONE
 */
public class MessageUtility {
	/**
	 * コンストラクタ
	 */
	public MessageUtility() {
		super();
		initDbMessageListData();
	}

	/** メッセージ一覧情報保持 */
	static HashMap<String, JSONObject> dbMessageList;

	/**
	 * メッセージ一覧を返します。
	 *
	 * @return メッセージ一覧
	 */
	public static Map<String, JSONObject> getDbMessageList() {
		return dbMessageList;
	}

	/** メッセージ一覧情報取得 */
	public static boolean initDbMessageListData() {
		dbMessageList = new HashMap<String, JSONObject>();
		ItemList iL = new ItemList();

		ArrayList<String> paramData = new ArrayList<String>();
		String command = DefineReport.ID_SQL_MSG;
		@SuppressWarnings("static-access")
		JSONArray rows = iL.selectJSONArray(command, paramData, Defines.STR_JNDI_DS);
		for (int i=0; i<rows.size(); i++){
			dbMessageList.put(rows.optJSONObject(i).optString("MSGCD"), rows.optJSONObject(i));
		}
		return dbMessageList.size() > 0;
	}
	/** パラメータを元にDBに問い合わせた結果の文言を取得 */
	public String getDbMessage(String key, String... args){
		JSONObject msgObj = dbMessageList.get(key);
		String add1 = "", add2 = "", add3 = "";
		if(args.length > 0) add1 = args[0];
		if(args.length > 1) add2 = args[1];
		if(args.length > 2) add3 = args[2];
		if(StringUtils.contains(msgObj.optString("MSGTXT1"), "%")){
			return (msgObj.optString("MSGTXT1") + msgObj.optString("MSGTXT2")).replace("%1", add1).replace("%2", add2).replace("%3", add3);
		}else{
			return (add1 + msgObj.optString("MSGTXT1") + add2 + msgObj.optString("MSGTXT2") + add3);//.replace("/^\s+|\s+$/g","");
		}
	}

	/**	パラメータを元にDBに問い合わせた結果を取得 */
	public JSONObject getDbMessageObj(String messageId, String... addParam){
		JSONObject obj = dbMessageList.get(messageId);
		obj.put(TYPE, StringUtils.left(messageId, 1));
		obj.put(ID, messageId);
		obj.put(CD, StringUtils.replaceEach(StringUtils.replaceEach(messageId,new String[]{"EX", "WX", "IX"} ,new String[]{"","",""}) ,new String[]{"E", "W", "I"} ,new String[]{"","",""}) );
		obj.put(MSG, getDbMessage(messageId, addParam));
		obj.put(PRM, addParam);
		return obj;
	}

	/**	パラメータをJs側で使うための形式に変更 */
	public static JSONObject getDbMessageIdObj(String messageId, String... addParam){
		JSONObject obj = new JSONObject();
		obj.put(TYPE, StringUtils.left(messageId, 1));
		obj.put(ID, messageId);
		obj.put(CD, StringUtils.replaceEach(StringUtils.replaceEach(messageId,new String[]{"EX", "WX", "IX"} ,new String[]{"","",""}) ,new String[]{"E", "W", "I"} ,new String[]{"","",""}) );
		obj.put(PRM, addParam);
		return obj;
	}


	/**
	 * データタイプに応じた文字型エラーメッセージ情報を戻します。
	 *
	 * @param datatype
	 *			データタイプ
	 * @param addParam
	 *			メッセージに付加するパラメータ
	 * @return DBエラーメッセージ情報
	 */
	public JSONObject getDbMessageObjDataTypeErr(DefineReport.DataType dataType, String... addParam) {
		String messageId = "";
		if(DataType.SUUJI.equals(dataType)||DataType.LPADZERO.equals(dataType)||DataType.INTEGER.equals(dataType)){
			messageId = "E11019";
		}else if(DataType.DECIMAL.equals(dataType)){
			messageId = "E11019";
		}else if(DataType.DATE.equals(dataType)){
			messageId = "E30019";
		}else if(DataType.KANA.equals(dataType)){
			messageId = "E11018";
		}else if(DataType.ZEN.equals(dataType)){
			messageId = "EX1047";
			if (addParam.length > 0) {
				addParam[0] = addParam[0] + "全角文字";
			} else {
				addParam = new String[]{"全角文字"};
			}
		} else if (DataType.ALPHA.equals(dataType)) {
			messageId = "E30012";

			if (addParam.length > 0) {
				addParam[0] = addParam[0] + "半角英数字";
			} else {
				addParam = new String[]{"半角英数字"};
			}
		}
		return getDbMessageObj(messageId, addParam);
	}

	/**
	 * データタイプに応じた桁数エラーメッセージ情報を戻します。
	 *
	 * @param datatype
	 *			データタイプ
	 * @param addParam
	 *			メッセージに付加するパラメータ
	 * @return DBエラーメッセージ情報
	 */
	public JSONObject getDbMessageObjLen(DefineReport.DataType dataType, String... addParam) {
		String messageId = "E11042";
		return getDbMessageObj(messageId, addParam);
	}


	/**
	 * フィールドタイプに応じた排他エラーメッセージ情報を戻します。
	 *
	 * @param fieldType
	 *			フィールドタイプ
	 * @param addParam
	 *			メッセージに付加するパラメータ
	 * @return DBエラーメッセージ情報
	 */
	public static JSONObject getDbMessageExclusion(FieldType fieldType, String... addParam) {
		String messageId = "E30008";
		if(FieldType.GRID.equals(fieldType)){
			messageId = "E30009";
		}
		return getDbMessageIdObj(messageId, addParam);
	}

	public static JSONObject getDbMessageMismatchUpdateDate() {
		return getDbMessageIdObj("E20613", new String[]{});
	}


	/** キー:メッセージタイプ */
	public static final String TYPE = "TYPE";
	/** キー:メッセージID */
	public static final String ID = "ID";
	/** キー:メッセージ内容 */
	public static final String MSG = "MSG";
	/** キー:メッセージCD */
	public static final String CD = "CD";
	/** キー:メッセージ用パラメータ */
	public static final String PRM = "PRM";

	/** 固定値:メッセージタイプ */
	public enum MsgType implements Option {
		/** エラー */
		E("E","エラー"),
		/** 正常 */
		S("S","正常"),
		/** 警告 */
		W("W","警告");

		private final String txt;
		private final String val;

		/** 初期化 */
		private MsgType(String val, String txt) {
			this.val = val;
			this.txt = txt;
		}
		/** @return val 値 */
		public String getVal() { return val; }
		/** @return txt 表示名称 */
		public String getTxt() { return txt; }
	}

	/** 固定値:メッセージ格納キー */
	public enum MsgKey {
		/** エラー */
		E("E_MSG"),
		/** 正常 */
		S("S_MSG"),
		/** 警告 */
		W("W_MSG");

		private final String key;
		/** 初期化 */
		private MsgKey(String key) {
			this.key = key;
		}
		/** @return val 値 */
		public String getKey() { return key; }
	}

	/** フィールドタイプ */
	public enum FieldType {
		/** デフォルト */
		DEFAULT("1"),
		/** グリッド内データ */
		GRID("2");

		/** ID */
		private final String id;

		/** 初期化 */
		FieldType(String id) {
			this.id = id;
		}
		/** @return id */
		public String getId() {
			return id;
		}
	}

	/** 固定値:メッセージ一覧 */
	public enum Msg implements Option {
		/** 更新完了 */
		S00001("S00001","データを登録しました。"),
		/** 削除完了 */
		S00002("S00002","削除が完了しました。"),
		/** 更新完了(件数あり) */
		S00003("S00003","{0}件中 {1}件を更新しました。"),
		/** 削除完了(件数あり) */
		S00004("S00004","{0}件削除しました。"),
		/** 更新完了(件数あり) */
		S00005("S00005","{0}件により {1}件更新しました。"),
		/** 更新完了(件数あり) */
		S00006("S00006","{0}を {1}件更新しました。"),
		/** 更新完了(件数なし) */
		S00007("S00007","{0}を更新しました。"),
		/** 警告：検索結果件数オーバー */
		W00001("W00001","検索結果件数が{0}件を超えました。検索条件の絞込みを行ってください。"),
		/** 更新処理失敗 */
		E00001("E00001","更新処理に失敗しました。"),
		/** 削除処理失敗 */
		E00002("E00002","削除処理に失敗しました。"),
		/** 更新対象無し */
		E10000("E10000","更新対象データはありません。"),
		/** 必須指定 */
		E10001("E10001","{0}は、必須指定です。"),
		/** 文字数 */
		E10002("E10002","{0}は、{1}文字以下で入力してください。"),
		/** 半角数字文字数 */
		E10003("E10003","{0}は、{1}文字以下の半角数字で入力してください。"),
		/** 小数文字数 */
		E10004("E10004","{0}は、整数部{1}文字、小数部{2}文字以下の半角数字で入力してください。"),
		/** 日付形式 */
		E10005("E10005","{0}は、正しい日付を、西暦（{1}）で入力してください。"),
		/** 汎用 */
		E10006("E10006","{0}は、{1}で入力してください。"),
		/** E20001 */
		E20001("E20001","{1}件目にエラーがあります。{0}は、必須指定です。"),
		/** E20002 */
		E20002("E20002","{2}件目にエラーがあります。{0}は、{1}文字以下を指定してください。"),
		/** E20003 */
		E20003("E20003","{2}件目にエラーがあります。{0}は、{1}文字以下の半角数字を指定してください。"),
		/** E20004 */
		E20004("E20004","{3}件目にエラーがあります。{0}は、整数部{1}文字、小数部{2}文字以下の半角数字を指定してください。"),
		/** E20005 */
		E20005("E20005","{2}件目にエラーがあります。{0}は、正しい日付を、西暦（{1}）で入力してください。"),
		/** 汎用 */
		E20006("E20006","{2}件目にエラーがあります。{0}は、{1}で入力してください。"),
		;

		private final String txt;
		private final String val;

		/** 初期化 */
		private Msg(String val, String txt) {
			this.val = val;
			this.txt = txt;
		}
		/** @return val 値 */
		public String getVal() { return val; }
		/** @return txt 表示名称 */
		public String getTxt() { return txt; }
	}


	/** メッセージ取得 */
	public static String getMessage(String key) {
		return Msg.valueOf(key).getTxt();
	}
	public static String getMessage(String key, String... args) {
		return MessageFormat.format(Msg.valueOf(key).getTxt(), (Object[]) args);
	}

	/** メッセージ取得 */
	public static JSONObject getMsg(String message) {
		JSONObject obj = new JSONObject();
		obj.put(MSG, message);
		return obj;
	}

	public static JSONObject getMessageObj(MsgType type, String key, String... args) {
		JSONObject obj = new JSONObject();
		obj.put(TYPE, type.getVal());
		obj.put(ID, key);
		obj.put(MSG, getMessage(key,args));
		return obj;
	}

	/**
	 * メッセージを戻します。
	 *
	 * @param messageId
	 *			メッセージID
	 * @param addParam
	 *			メッセージに付加するパラメータ
	 * @return メッセージ
	 */
	public static JSONObject getMessageObj(String messageId, String... args) {
		MsgType type = MsgType.valueOf(StringUtils.left(messageId,1));
		return getMessageObj(type, messageId, args);
	}

	/**
	 * フィールドタイプに応じた必須入力エラーメッセージを戻します。
	 *
	 * @param field
	 *			フィールド
	 * @param fieldType
	 *			フィールドタイプ
	 * @param addParam
	 *			メッセージに付加するパラメータ
	 * @return 必須入力エラーメッセージ
	 */
	public static JSONObject getCheckNullMessage(String lbl, FieldType fieldType, String... addParam) {
		String messageId = Msg.valueOf(MsgType.E.getVal()+fieldType.getId()+"0001").getVal();
		String[] param = new String[]{ lbl };
		param = (String[]) ArrayUtils.addAll(param, addParam);
		return getMessageObj(MsgType.E, messageId, param);
	}

	/**
	 * フィールドタイプに応じた文字数超過エラーメッセージを戻します。
	 *
	 * @param field
	 *			フィールド
	 * @param fieldType
	 *			フィールドタイプ
	 * @param addParam
	 *			メッセージに付加するパラメータ
	 * @return 文字数超過エラーメッセージ
	 */
	public static JSONObject getCheckMaxLengthMessage(DefineReport.InpText field,
			FieldType fieldType, String... addParam) {
		String messageId = "";
		String[] param = null;
		if(DataType.INTEGER.equals(field.getType())){
			messageId = Msg.valueOf(MsgType.E.getVal()+fieldType.getId()+"0003").getVal();
			param = new String[]{ field.getTxt(), String.valueOf(field.getDigit1()) };
		}else if(DataType.DECIMAL.equals(field.getType())){
			messageId = Msg.valueOf(MsgType.E.getVal()+fieldType.getId()+"0004").getVal();
			param = new String[]{ field.getTxt(), String.valueOf(field.getDigit1()), String.valueOf(field.getDigit2()) };
		}else{
			messageId = Msg.valueOf(MsgType.E.getVal()+fieldType.getId()+"0002").getVal();
			param = new String[]{ field.getTxt(), String.valueOf(field.getDigit1()) };
		}
		param = (String[]) ArrayUtils.addAll(param, addParam);
		return getMessageObj(MsgType.E, messageId, param);
	}


	/**
	 * フィールドタイプに応じた数値チェックエラーメッセージを戻します。
	 *
	 * @param field
	 *			フィールド
	 * @param fieldType
	 *			フィールドタイプ
	 * @param addParam
	 *			メッセージに付加するパラメータ
	 * @return 数値チェックエラーメッセージ
	 */
	public static JSONObject getCheckZeroOrPlusIntegerMessage(DefineReport.InpText field,
			FieldType fieldType, String... addParam) {
		String messageId = Msg.valueOf(MsgType.E.getVal()+fieldType.getId()+"0006").getVal();
		String[] param =	new String[]{ field.getTxt(), "ゼロ以上の整数" };
		param = (String[]) ArrayUtils.addAll(param, addParam);
		return getMessageObj(MsgType.E, messageId, param);
	}

	/**
	 * 半角英数字→全角英数字
	 * @param p 文字列
	 * @return 変換後文字
	 */
	public static String HanToZenForAlpha(String p) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < p.length(); i++) {
			Character c = new Character(p.charAt(i));
			// Unicode半角ラテン文字のコード範囲(!～~)であるか？
			if (c.compareTo(new Character((char)0x0021)) >= 0
				&& c.compareTo(new Character((char)0x007e)) <= 0) {
				// 変換文字に0xfee0を加算して全角文字に変換する
				Character x = new Character((char) (c.charValue()
							+ (new Character((char)0xfee0)).charValue()));
				sb.append(x);
			} else { // 半角ラテン文字以外なら変換しない
				sb.append(c);
			}
		}
		return sb.toString();
	}
	/**
	 * 全角英数字→半角英数字
	 * @param p 文字列
	 * @return 変換後文字
	 */
	public static String ZenToHanForAlpha(String p) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < p.length(); i++) {
			Character c = new Character(p.charAt(i));
			// Unicode全角ラテン文字のコード範囲(！から～)であるか？
			if (c.compareTo(new Character((char)0xff01)) >= 0
				&& c.compareTo(new Character((char)0xff5e)) <= 0) {
				// 変換文字から0xfee0を減算して半角文字に変換します
				Character x = new Character((char) (c.charValue()
							 - (new Character((char)0xfee0)).charValue()));
				sb.append(x);
			} else { // 全角ラテン文字以外なら変換しない
				sb.append(c);
			}
		}
		return sb.toString();
	}

	/**
	 * カナの全角・半角対応表
	 */
	private static final String kanaHanZenTbl[][] = {
		// 2文字構成の濁点付き半角カナ
		// 必ずテーブルに先頭に置いてサーチ順を優先すること
		{ "ｶﾞ", "ガ" }, { "ｷﾞ", "ギ" }, { "ｸﾞ", "グ" }, { "ｹﾞ", "ゲ" }, { "ｺﾞ", "ゴ" },
		{ "ｻﾞ", "ザ" }, { "ｼﾞ", "ジ" }, { "ｽﾞ", "ズ" }, { "ｾﾞ", "ゼ" }, { "ｿﾞ", "ゾ" },
		{ "ﾀﾞ", "ダ" }, { "ﾁﾞ", "ヂ" }, { "ﾂﾞ", "ヅ" }, { "ﾃﾞ", "デ" }, { "ﾄﾞ", "ド" },
		{ "ﾊﾞ", "バ" }, { "ﾋﾞ", "ビ" }, { "ﾌﾞ", "ブ" }, { "ﾍﾞ", "ベ" }, { "ﾎﾞ", "ボ" },
		{ "ﾊﾟ", "パ" }, { "ﾋﾟ", "ピ" }, { "ﾌﾟ", "プ" }, { "ﾍﾟ", "ペ" }, { "ﾎﾟ", "ポ" },
		{ "ｳﾞ", "ヴ" },
		// 1文字構成の半角カナ
		{ "ｱ", "ア" }, { "ｲ", "イ" }, { "ｳ", "ウ" }, { "ｴ", "エ" }, { "ｵ", "オ" },
		{ "ｶ", "カ" }, { "ｷ", "キ" }, { "ｸ", "ク" }, { "ｹ", "ケ" }, { "ｺ", "コ" },
		{ "ｻ", "サ" }, { "ｼ", "シ" }, { "ｽ", "ス" }, { "ｾ", "セ" }, { "ｿ", "ソ" },
		{ "ﾀ", "タ" }, { "ﾁ", "チ" }, { "ﾂ", "ツ" }, { "ﾃ", "テ" }, { "ﾄ", "ト" },
		{ "ﾅ", "ナ" }, { "ﾆ", "ニ" }, { "ﾇ", "ヌ" }, { "ﾈ", "ネ" }, { "ﾉ", "ノ" },
		{ "ﾊ", "ハ" }, { "ﾋ", "ヒ" }, { "ﾌ", "フ" }, { "ﾍ", "ヘ" }, { "ﾎ", "ホ" },
		{ "ﾏ", "マ" }, { "ﾐ", "ミ" }, { "ﾑ", "ム" }, { "ﾒ", "メ" }, { "ﾓ", "モ" },
		{ "ﾔ", "ヤ" }, { "ﾕ", "ユ" }, { "ﾖ", "ヨ" },
		{ "ﾗ", "ラ" }, { "ﾘ", "リ" }, { "ﾙ", "ル" }, { "ﾚ", "レ" }, { "ﾛ", "ロ" },
		{ "ﾜ", "ワ" }, { "ｲ", "ヰ" }, { "ｴ", "ヱ" }, { "ｦ", "ヲ" }, { "ﾝ", "ン" },
		{ "ｧ", "ァ" }, { "ｨ", "ィ" }, { "ｩ", "ゥ" }, { "ｪ", "ェ" }, { "ｫ", "ォ" },
		{ "ｬ", "ャ" }, { "ｭ", "ュ" }, { "ｮ", "ョ" }, { "ｯ", "ッ" },
		// Unicode半角ラテン文字のコード範囲(!～~)以外の記号
		{ " ", "　" }, { "｡", "。" }, { "､", "、" }, { "｢", "「" }, { "｣", "」" }, { "･", "・" }, { "ｰ", "ー" }
	};
	/**
	 * 半角カナ→全角カナ
	 * @param p 文字列
	 * @return 変換後文字
	 */
	public static String HanToZenForKana(String p) {
		StringBuffer sb = new StringBuffer();

		for (int i = 0, j = 0; i < p.length(); i++) {
			Character c = new Character(p.charAt(i));
			// Unicode半角カタカナのコード範囲か？
			if (c.compareTo(new Character((char)0xff61)) >= 0
				&& c.compareTo(new Character((char)0xff9f)) <= 0) {
				// 半角全角変換テーブルを検索する
				for (j = 0; j < kanaHanZenTbl.length; j++) {
					if (p.substring(i).startsWith(kanaHanZenTbl[j][0])) {
						sb.append(kanaHanZenTbl[j][1]);
						i += kanaHanZenTbl[j][0].length() - 1;
						break;
					}
				}

				// 検索できなければ、変換しない
				if (j >= kanaHanZenTbl.length) {
					sb.append(c);
				}
			} else { // Unicode半角カタカナ以外なら変換しない
				sb.append(c);
			}
		}
		return sb.toString();
	}
	/**
	 * 全角カナ→半角カナ
	 * @param p 文字列
	 * @return 変換後文字
	 */
	public static String ZenToHanForKana(String p) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0, j = 0; i < p.length(); i++) {
			Character c = new Character(p.charAt(i));
			// Unicode全角カタカナのコード範囲か?
			if (c.compareTo(new Character((char)0x30a1)) >= 0
				&& c.compareTo(new Character((char)0x30fc)) <= 0) {
				// 半角全角変換テーブルを検索する
				for (j = 0; j < kanaHanZenTbl.length; j++) {
					if (p.substring(i).startsWith(kanaHanZenTbl[j][1])) {
						sb.append(kanaHanZenTbl[j][0]);
						break;
					}
				}
				// 検索できなければ、変換しない
				if (j >= kanaHanZenTbl.length) {
						sb.append(c);
				}
			} else { // 全角カタカナ以外なら変換しない
					sb.append(c);
			}
		}
		return sb.toString();
	}


	/**
	 * 半角文字→全角文字（英数字、カナ、一部記号）
	 * @param p 文字列
	 * @return 変換後文字
	 */
	public static String HanToZen(String p) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0, j = 0; i < p.length(); i++) {
			Character c = new Character(p.charAt(i));
			// Unicode半角ラテン文字のコード範囲(!～~)であるか？
			if (c.compareTo(new Character((char)0x0021)) >= 0
				&& c.compareTo(new Character((char)0x007e)) <= 0) {
				// 変換文字に0xfee0を加算して全角文字に変換する
				Character x = new Character((char) (c.charValue()
							+ (new Character((char)0xfee0)).charValue()));
				sb.append(x);
			} else { // 半角ラテン文字以外なら半角全角変換テーブルを検索する
				for (j = 0; j < kanaHanZenTbl.length; j++) {
					if (p.substring(i).startsWith(kanaHanZenTbl[j][0])) {
						sb.append(kanaHanZenTbl[j][1]);
						i += kanaHanZenTbl[j][0].length() - 1;
						break;
					}
				}
				// 検索できなければ、変換しない
				if (j >= kanaHanZenTbl.length) {
					sb.append(c);
				}
			}
		}
		return sb.toString();
	}
	/**
	 * 全角文字→半角文字
	 * @param p 文字列
	 * @return 変換後文字
	 */
	public static String ZenToHan(String p) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0, j = 0; i < p.length(); i++) {
			Character c = new Character(p.charAt(i));
			// Unicode全角ラテン文字のコード範囲(！から～)であるか？
			if (c.compareTo(new Character((char)0xff01)) >= 0
				&& c.compareTo(new Character((char)0xff5e)) <= 0) {
				// 変換文字から0xfee0を減算して半角文字に変換します
				Character x = new Character((char) (c.charValue()
							 - (new Character((char)0xfee0)).charValue()));
				sb.append(x);
			} else { // 全角ラテン文字以外なら半角全角変換テーブルを検索する
				for (j = 0; j < kanaHanZenTbl.length; j++) {
					if (p.substring(i).startsWith(kanaHanZenTbl[j][1])) {
						sb.append(kanaHanZenTbl[j][0]);
						break;
					}
				}
				// 検索できなければ、変換しない
				if (j >= kanaHanZenTbl.length) {
						sb.append(c);
				}
			}
		}
		return sb.toString();
	}


	/**
	 * バイト数取得
	 * @param p 文字列
	 * @return バイト数
	 */
	public static Integer getDefByteLen(String p) {
		return p.getBytes(Charset.forName("UTF8")).length;
	}

	public static String leftB(String str, Integer len){
		StringBuffer sb = new StringBuffer();
		int cnt = 0;

		try{
			for (int i = 0; i < str.length(); i++) {
				String tmpStr = str.substring(i, i + 1);
				byte[] b = tmpStr.getBytes(Charset.forName("UTF8"));
				if (cnt + b.length > len) {
					return sb.toString();
				} else {
					sb.append(tmpStr);
					cnt += b.length;
				}
			}
		} catch (Exception e){
			e.printStackTrace();
		}
		return sb.toString();
	}

}
