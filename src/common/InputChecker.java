/*
 * 作成日: 2006/12/05
 *
 */
package common;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import common.CmnDate.DATE_FORMAT;
import common.DefineReport.DataType;
import net.sf.json.JSONArray;

/**
 * 入力チェック関連共通部品クラス
 * <p>
 * 入力チェック関連共通部品。
 * <dl>
 * <dt><b>変更履歴</b></dt>
 * <dd>2008.10.20 新規作成</dd>
 * </dl>
 *
 * @author EATONE
 *
 */
public class InputChecker {

	/**
	 * コンストラクタ
	 */
	protected InputChecker() {
		super();
	}

	/**
	 * nullまたは空文字または半角空白チェック。<br/>
	 * nullまたは空文字または半角空白かチェックする。<br>
	 *
	 * @param buffer
	 *			チェックする文字列
	 * @return boolean チェック結果(true="null"または空文字である false="null"でも空文字でもない)
	 */
	public static boolean isNull(String buffer) {
		return StringUtils.isBlank(buffer);
	}

	/**
	 * 文字列内容の存在チェック<br/>
	 * 文字列が空（nullまたは空文字または半角空白のみ)以外の場合に true を返します。
	 *
	 * @param buffer
	 *			文字列
	 * @return 文字列が null または 空文字 以外の場合に true それ以外の場合 false
	 */
	public static boolean isNotNull(String buffer) {
		return !InputChecker.isNull(buffer);
	}

	/**
	 * 桁数チェック(桁数以内)
	 * <p>
	 * 引数文字列が引数数値のバイト数以内かチェックする。<br>
	 *
	 * @param buffer
	 *			チェックする文字列
	 * @param max
	 *			MAX値
	 * @return boolean チェック結果 (true=超えていない false=超えている)
	 */
	public static boolean isTextLessThanMaxByteLength(String buffer, int max) {
		try {
			// null、空文字の時はtrueとする。
			if (StringUtils.isEmpty(buffer)) {
				return true;
			}

			// バイト数以内かチェック。超えていればfalseとする。
			if (buffer.getBytes("MS932").length > max) {
				return false;
			}

		} catch (UnsupportedEncodingException e) {
			return false;
		}

		return true;
	}

	/**
	 * 桁数チェック<br>
	 * 引数文字列が引数数値の文字数以内かチェックする。<br>
	 *
	 * @param buffer
	 *			チェックする文字列
	 * @param max
	 *			MAX値
	 * @return チェック結果 (true=超えていない false=超えている)
	 */
	public static boolean isTextLessThanMaxLength(String buffer, int max) {
		if (StringUtils.isEmpty(buffer)) {
			return true;
		}

		// 文字列が指定文字数以下であるかチェック。超えていればfalseとする。
		if (StringUtils.length(buffer) > max) {
			return false;
		}

		return true;
	}

	/**
	 * 行数チェック<br>
	 * 引数文字列が引数数値の行数以内かチェックする。<br>
	 *
	 * @param buffer
	 *			チェックする文字列
	 * @param max
	 *			MAX値
	 * @return チェック結果 (true=超えていない false=超えている)
	 */
	public static boolean isTextLessThanMaxLine(String buffer, int max) {
		if (StringUtils.isEmpty(buffer)) {
			return true;
		}

		// 文字列が指定行数以下であるかチェック。超えていればfalseとする。
		if (StringUtils.splitByWholeSeparatorPreserveAllTokens(buffer, "\n").length > max) {
			return false;
		}

		return true;
	}

	/**
	 * Numeric型桁数チェック<br>
	 * 引数文字列が引数数値の文字数以内かチェックする。<br>
	 * データ長に","が含まれる場合は整数部分、小数部分それぞれで文字数をチェックする。
	 *
	 * @param buffer
	 *			チェックする文字列
	 * @param dataLen
	 *			データ長
	 * @return チェック結果 (true=超えていない false=超えている)
	 */
	public static boolean isNumericLessThanMaxLength(String buffer,
			String dataLen) {
		return isNumericLessThanMaxLength(buffer, dataLen, ",");
	}

	/**
	 * Numeric型桁数チェック<br>
	 * 引数文字列が引数数値の文字数以内かチェックする。<br>
	 * データ長に","が含まれる場合は整数部分、小数部分それぞれで文字数をチェックする。
	 *
	 * @param buffer
	 *			チェックする文字列
	 * @param dataLen
	 *			データ長
	 * @param separator
	 *			区切り文字
	 * @return チェック結果 (true=超えていない false=超えている)
	 */
	public static boolean isNumericLessThanMaxLength(String buffer,
			String dataLen, String separator) {
		// null、から文字のときはfalseとする
		if (StringUtils.isEmpty(buffer)) {
			return false;
		}

		int separatorCount = StringUtils.countMatches(dataLen, separator);
		int dotCount = StringUtils.countMatches(buffer, ".");

		// 文字列に小数点が複数含まれている場合やデータ長に区切り文字が複数含まれている場合は
		// 想定外の形式のためfalseとする
		if (dotCount > 1 || separatorCount > 1) {
			return false;
		}

		// データ長を区切り文字で分割する
		String[] splitLen = StringUtils.split(dataLen, separator);
		int integerMax = 0;
		int decimalMax = 0;
		if (separatorCount > 0) {
			integerMax = Integer.parseInt(splitLen[0])
					- Integer.parseInt(splitLen[1]);
			decimalMax = Integer.parseInt(splitLen[1]);
		}else{
			integerMax = Integer.parseInt(dataLen);
		}

		// 文字列を小数点で分割する
		String[] splitBuf = StringUtils.split(buffer, ".");
		int integerLength = buffer.length();
		int decimalLength = 0;
		String decumalBuf = "";
		if (dotCount > 0) {
			integerLength = splitBuf[0].length();
			decimalLength = splitBuf[1].length();
			decumalBuf = splitBuf[1];
		}

		// 整数部分の文字列長がデータ長の整数部分を超過した場合はfalseとする
		if (integerLength > integerMax) {
			return false;
		}

		// 小数部分の文字列長がデータ長の小数部分を超過した場合はfalseとする
		// 但し、小数部分の文字列 = "0" かつ データ長の小数部分 = "0" の場合を除く。
		// ______即ち、データ長が"7,0"の場合、文字列が"1234567.0"の場合はエラーとしない。
		if (decimalLength > decimalMax
				&& !(decimalMax == 0 && StringUtils.equals(decumalBuf, "0"))) {
			return false;
		}

		return true;
	}

	/**
	 * 桁数チェック(固定桁)
	 * <p>
	 * 引数文字列が引数数値のバイト桁数と同一かチェックする。<br>
	 *
	 * @param buffer
	 *			チェックする文字列
	 * @param fix
	 *			規定の桁数
	 * @return boolean チェック結果 (true=超えていない false=超えている)
	 */
	public static boolean isTextEqualFixByteLength(String buffer, int fix) {
		try {
			// null、空文字はの時はfalseとする。
			if (StringUtils.isEmpty(buffer)) {
				return false;
			}

			// バイト数以内かチェック。超えていればfalseとする。
			if (buffer.getBytes("MS932").length != fix) {
				return false;
			}

		} catch (UnsupportedEncodingException e) {
			return false;
		}

		return true;
	}

	/**
	 * 半角チェック
	 * <p>
	 * 半角かチェックする。<br>
	 *
	 * @param buffer
	 *			チェックする文字列
	 * @return boolean チェック結果 (true=半角 false=半角以外)
	 */
	public static boolean isHarfWidth(String buffer) {
		// null、空文字の時はfalseとする。
		if (StringUtils.isEmpty(buffer)) {
			return false;
		}

		// 半角でないときはfalseとする
		byte[] bytes = StringUtils.trimToEmpty(buffer).getBytes();
		int beams = buffer.length();
		if (beams != bytes.length) {
			return false;
		}
		return true;
	}

	/**
	 * 数値チェック
	 * <p>
	 * 数値かチェックする。(小数可、マイナス可、全角不可、全角不可)<br>
	 *
	 * @param buffer
	 *			チェックする文字列
	 * @return boolean チェック結果 (true=数値 false=数値以外)
	 */
	public static boolean isNumeric(String buffer) {
		// null、空文字の時はfalseとする。
		if (StringUtils.isEmpty(buffer)) {
			return false;
		}

		// マイナスが含まれる場合はfalseとする。
		Pattern p = Pattern.compile("[-]");
		if (p.matcher(buffer).find()){
			return false;
		}

		// 数値でない時はfalseとする
		try {
			Double.parseDouble(StringUtils.trimToEmpty(buffer));
		} catch (NumberFormatException e) {
			return false;
		}

		// 半角でないときはfalseとする
		byte[] bytes = StringUtils.trimToEmpty(buffer).getBytes();
		int beams = buffer.length();
		if (beams != bytes.length) {
			return false;
		}

		return true;
	}

	/**
	 * 数値の桁数チェック。<br>
	 * 整数部及び小数部が指定された桁数以下の場合、trueを返す。
	 *
	 * @param buffer
	 *			チェックする文字列
	 * @param integerDigits
	 *			整数部桁数（1以上）
	 * @param fractionDigits
	 *			小数部桁数（0以上）
	 * @return boolean チェック結果 (true=指定桁数以内 false=指定桁数オーバー)
	 */
	public static boolean isNumericLessThanDigits(String buffer,
			int integerDigits, int fractionDigits) {
		if (integerDigits < 1 || fractionDigits < 0) {
			return true;
		}

		// 数値でないときは、無視する
		if (!isNumeric(buffer)) {
			return true;
		}

		if (fractionDigits == 0) {
			return Pattern.matches("(-)?\\d{1," + integerDigits + "}", buffer);
		}

		return Pattern.matches("(-)?\\d{1," + integerDigits + "}(\\.\\d{1,"
				+ fractionDigits + "})?", buffer);
	}

	/**
	 * 整数チェック
	 * <p>
	 * 整数かチェックする。(マイナス可、全角不可)<br>
	 *
	 * @param buffer
	 *			チェックする文字列
	 * @return boolean チェック結果 (true=数値 false=数値以外)
	 */
	public static boolean isInteger(String buffer) {
		// 数値でない時はfalseとする
		if (!InputChecker.isNumeric(buffer)) {
			return false;
		}

		// 整数でない時はfalseをかえす。
		try {
			Integer.parseInt(buffer);
		} catch (NumberFormatException e) {
			return false;
		}

		return true;
	}

	/**
	 * 整数チェック
	 * <p>
	 * ゼロ以上の整数かチェックする。(全角不可)<br>
	 *
	 * @param buffer
	 *			チェックする文字列
	 * @return boolean チェック結果 (true=ゼロ以上の整数 false=それ以外)
	 */
	public static boolean isZeroOrPlusInteger(String buffer) {
		// 整数かどうかチェック
		if (!InputChecker.isInteger(buffer)) {
			return false;
		}

		// 整数に変換して0以上かチェック
		if (NumberUtils.toInt(StringUtils.trimToEmpty(buffer)) < 0) {
			return false;
		}
		return true;
	}

	/**
	 * 整数チェック
	 * <p>
	 * 正の整数かチェックする。(全角不可)<br>
	 *
	 * @param buffer
	 *			チェックする文字列
	 * @return boolean チェック結果 (true=ゼロ以上の整数 false=それ以外)
	 */
	public static boolean isPlusInteger(String buffer) {
		// 整数かどうかチェック
		if (!InputChecker.isInteger(buffer)) {
			return false;
		}

		// 整数に変換して1以上かチェック
		if (NumberUtils.toInt(StringUtils.trimToEmpty(buffer)) <= 0) {
			return false;
		}
		return true;
	}

	/**
	 * 数値チェック
	 * <p>
	 * ゼロ以上の数値かチェックする。(小数可、マイナス可、全角不可、全角不可)<br>
	 *
	 * @param buffer
	 *			チェックする文字列
	 * @return boolean チェック結果 (true=数値 false=数値以外)
	 */
	public static boolean isZeroOrPlusNumeric(String buffer) {
		// 数値でなければfalseとする
		if (!isNumeric(buffer)) {
			return false;
		}

		// ゼロ以上でなければfalseとする
		if (NumberUtils.toDouble(StringUtils.trimToEmpty(buffer)) < 0d) {
			return false;
		}

		return true;
	}

	/**
	 * 日付チェック<br>
	 * 引数文字列が標準入力形式かチェックする。
	 *
	 * @param buffer
	 *			チェックする文字列
	 * @return チェック結果 (true=数値 false=数値以外)
	 */
	public static boolean isValidDate(String buffer) {
		return isValidDate(buffer, DATE_FORMAT.DEFAULT_DATE);
	}

	/**
	 * 日付チェック
	 * <p>
	 * 引数文字列が指定された日付フォーマットと一致するかチェックする。<br>
	 *
	 * @param buffer
	 *			チェックする文字列
	 * @param format
	 *			日付フォーマット
	 * @return boolean チェック結果 (true=数値 false=数値以外)
	 */
	public static boolean isValidDate(String buffer, DATE_FORMAT format) {
		SimpleDateFormat dateFormat;

		// null、空文字の時はfalseとする。
		if (StringUtils.isEmpty(buffer)) {
			return false;
		}
		// 日付/時刻解析を厳密に行うかどうかを設定する。
		dateFormat = new SimpleDateFormat(format.formatPattern());
		dateFormat.setLenient(false);

		try {
			dateFormat.parse(buffer);
			return true;
		} catch (ParseException e) {
			return false;
		}
	}

	/**
	 * 日付FromToチェック
	 * <p>
	 * 引数日付がFromTo正しいかチェックする。<br>
	 * FromTo文字列がデフォルトの日付型になっていることが前提
	 *
	 * @param from
	 *			開始日として入力された文字列
	 * @param to
	 *			終了日として入力された文字列
	 * @return boolean チェック結果 (true=FromTo正しい false=FromTo逆)
	 *
	 * @throws ParseException
	 *			 解析例外
	 */
	public static boolean isFromToDate(String from, String to){
		// 日付形式でない時はfalseをかえす。
		if (!isValidDate(from)) {
			return false;
		}
		if (!isValidDate(to)) {
			return false;
		}
		// FromTo逆の時はfalseをかえす。
		if (CmnDate.convDate(from, DATE_FORMAT.DEFAULT_DATE).getTime() > CmnDate.convDate(to,DATE_FORMAT.DEFAULT_DATE).getTime()) {
			return false;
		}
		return true;
	}


	/**
	 * 閏年チェック
	 * <p>
	 * 引数文字列が指定された日付フォーマットと一致するかチェックする。<br>
	 *
	 * @param buffer
	 *			チェックする文字列
	 * @param format
	 *			日付フォーマット
	 * @return boolean チェック結果 (true=数値 false=数値以外)
	 */
	public static boolean isLeapYear(String buffer) {
		int year = NumberUtils.toInt(StringUtils.left(buffer, 4));
		return year % 4 == 0 && year % 100 != 0 || year % 400 == 0 ;
	}


	/**
	 * 数値From～Toチェック<br>
	 * 2つが等しい場合は<code>true</code>を返します。
	 *
	 * @param from
	 *			数値From
	 * @param to
	 *			数値To
	 * @return boolean チェック結果 (true=FromTo正しい false=FromTo逆)
	 */
	public static boolean isFromToNumeric(String from, String to) {
		// いずれかが数値でない場合、無視
		if (!isNumeric(from) || !isNumeric(to)) {
			return true;
		}
		return Double.valueOf(from) <= Double.valueOf(to);
	}

	/**
	 * 列数チェック<br>
	 *
	 * @param 複数選択項目の列数(上段→下段の順で指定)
	 *
	 * @return boolean チェック結果 (true=正 false=誤)
	 */
	public static boolean checkColumnSize(JSONArray[] arrays) {
		int size = 1;
		for(JSONArray array : arrays){
			if(array.equals(arrays[0])){
				size = size * (array.size() + 1);
			}else{
				size = size * array.size();
			}
		}
		return isFromToNumeric(Integer.toString(size), "1000");
	}


	/**
	 * 行数チェック<br>
	 *
	 * @param 行数文字列
	 *
	 * @return boolean チェック結果 (true=正 false=誤)
	 */
	public static boolean checkRowSize(String cntStr) {
		return isFromToNumeric(cntStr, Integer.toString(NumberUtils.toInt(DefineReport.MAX_ROWNUM)-1));
	}



	/**
	 * 特殊ページパスワードチェック<br>
	 * TODO:特殊処理inageya
	 * @param 行数文字列
	 *
	 * @return boolean チェック結果 (true=正 false=誤)
	 */
	public static boolean checkPass042(String cntStr) {
		String pass = DefineReport.ID_ADMIN_PASS_HEAD_042 + StringUtils.right(new CmnDate().getToday(),4);
		return StringUtils.equals(pass, cntStr);
	}


	/**
	 * データタイプに応じた文字型チェック(空白=true)
	 *
	 * @param datatype
	 *			データタイプ
	 * @param buffer
	 *			チェックする文字列
	 * @return boolean チェック結果 (true=正 false=誤)
	 */
	public static boolean checkDataType(DefineReport.DataType dataType, String buffer) {
		boolean result = true;
		if(StringUtils.isEmpty(buffer)){
			return result;
		}
		if(DataType.SUUJI.equals(dataType)||DataType.LPADZERO.equals(dataType)){
			result = isNumeric(buffer);
		}else if(DataType.INTEGER.equals(dataType)){
			result = isInteger(buffer);
		}else if(DataType.DECIMAL.equals(dataType)){
			result = isNumeric(buffer);
		}else if(DataType.DATE.equals(dataType)){								// 0 or 日付
			DATE_FORMAT df = DATE_FORMAT.DEFAULT_DATE;
			if(buffer.contains("/")){
				df = DATE_FORMAT.GRID_YMD;
			}else if(buffer.contains("-")){
				df = DATE_FORMAT.DB_DATE;
			}
			if(buffer.length() == df.formatPattern().length()){
				result = isValidDate(buffer, df);
			}else if(buffer.length() == df.formatPattern().length()-2){
				result = isValidDate("20"+buffer, df);
			}else{
				result = StringUtils.equals(buffer, "0");
			}
		}else if(DataType.YYMM.equals(dataType)){								// 0 or 日付
			DATE_FORMAT df = DATE_FORMAT.DEFAULT_YM;
			if(buffer.contains("/")){
				df = DATE_FORMAT.GRID_YM;
			}
			if(buffer.length() == df.formatPattern().length()){
				result = isValidDate(buffer+"01", df);
			}else if(buffer.length() == df.formatPattern().length()-2){
				result = isValidDate("20"+buffer+"01", df);
			}else{
				result = StringUtils.equals(buffer, "0");
			}
		}else if(DataType.KANA.equals(dataType)){								// 半角カナ英数字記号
			result = buffer.matches("^[｡-ﾟa-zA-Z0-9 -/:-@\\[-\\`\\{-\\~]+$");
		}else if(DataType.ALPHA.equals(dataType)){								// 半角英数字記号
			result = buffer.matches("^[a-zA-Z0-9 -/:-@\\[-\\`\\{-\\~]+$");
		}else if(DataType.ZEN.equals(dataType)){								// 全角文字
			result = buffer.matches("^[^ -~｡-ﾟ]+$");
		}
		return result;
	}

	/**
	 * データタイプに応じた桁数チェック
	 *
	 * @param datatype
	 *			データタイプ
	 * @param buffer
	 *			チェックする文字列
	 * @return boolean チェック結果 (true=正 false=誤)
	 */
	public static boolean checkDataLen(DefineReport.DataType dataType, String buffer, int[] digit) {
		boolean result = true;
		if(DataType.INTEGER.equals(dataType)){
			result = isNumericLessThanMaxLength(buffer, Integer.toString(digit[0]), ",");
		}else if(DataType.DECIMAL.equals(dataType)){
			result = isNumericLessThanMaxLength(buffer, digit[0]+","+digit[1], ",");
		}else if(DataType.DATE.equals(dataType)){
			DATE_FORMAT df = DATE_FORMAT.DEFAULT_DATE;
			if(buffer.contains("/")){
				df = DATE_FORMAT.GRID_YMD;
			}else if(buffer.contains("-")){
				df = DATE_FORMAT.DB_DATE;
			}
			result = buffer.length() <= df.formatPattern().length();
		}else if(DataType.YYMM.equals(dataType)){
			DATE_FORMAT df = DATE_FORMAT.DEFAULT_YM;
			if(buffer.contains("/")){
				df = DATE_FORMAT.GRID_YM;
			}
			result = buffer.length() <= df.formatPattern().length();
		}else{
			result = isTextLessThanMaxByteLength(buffer, digit[0]);
		}
		return result;
	}

	/**
	 * 名称マスタ情報存在確認
	 *
	 * @throws Exception
	 */
	public static boolean checkKbnExist(String objKey, String value, String meisyoKbn) {

		// 項目情報の取得
		DefineReport.MeisyoSelect inpsetting = null;
		if(StringUtils.isEmpty(meisyoKbn)){
			// 名称区分未設定の場合はobjKeyで項目情報を取得する。
			for(DefineReport.MeisyoSelect colinf: DefineReport.MeisyoSelect.values()){
				if(StringUtils.equalsIgnoreCase(colinf.getObj().replace("sel_", ""), objKey)){
					inpsetting = colinf;
					break;
				}
			}
		}

		// 名称区分の設定
		String szMeisyoKbn = "";

		if(StringUtils.isNotEmpty(meisyoKbn)){
			szMeisyoKbn = meisyoKbn;
		}else if(inpsetting != null){
			szMeisyoKbn = ""+ inpsetting.getCd();
		}

		if(StringUtils.isEmpty(szMeisyoKbn) || StringUtils.isEmpty(value)){
			// 名称区分が取得できない場合、もしくは入力が空の場合は存在チェックを行わない。
			return true;
		}

		// 関連情報取得
		ItemList iL = new ItemList();
		// 配列準備
		ArrayList<String> paramData = new ArrayList<String>();
		String sqlcommand = "";

		String tbl = "";
		String col = "";
		String whr ="";

		tbl="INAMS.MSTMEISHO";
		col="int(MEISHOCD)";
		whr=" and MEISHOKBN = ?";

		if(tbl.length()>0&&col.length()>0){
			paramData.add(value);
			paramData.add(szMeisyoKbn);
			sqlcommand = DefineReport.ID_SQL_CHK_TBL.replace("@T", tbl).replaceAll("@C", col) + whr;

			@SuppressWarnings("static-access")
			JSONArray array = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
			if(array.size() == 0 || NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) == 0){
				return false;
			}
		}
		return true;
	}


}