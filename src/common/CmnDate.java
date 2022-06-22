/*
 * 作成日: 2006/12/05
 *
 */
package common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

/**
 * Dateを扱うクラス
 */
public class CmnDate extends Date {

	/**
	 *
	 */
	private static final long serialVersionUID = -6434746783286089750L;

	/**
	 *
	 */
	public CmnDate() {
		super();
	}

	/** 同日の最小の時を表す定数 */
	public static final int MINTIME_HOUR = 0;
	/** 同日の最小の分を表す定数 */
	public static final int MINTIME_MIN = 0;
	/** 同日の最小の秒を表す定数 */
	public static final int MINTIME_SEC = 0;
	/** 同日の最小のミリ秒を表す定数 */
	public static final int MINTIME_MSEC = 0;

	/** 同日の最大の時を表す定数 */
	public static final int MAXTIME_HOUR = 23;
	/** 同日の最大の分を表す定数 */
	public static final int MAXTIME_MIN = 59;
	/** 同日の最大の秒を表す定数 */
	public static final int MAXTIME_SEC = 59;
	/** 同日の最大のミリ秒を表す定数 */
	public static final int MAXTIME_MSEC = 999;

	/**
	 * 日付フォーマット種類
	 */
	public static enum DATE_FORMAT {
		/** デフォルト日付フォーマット 年月日 */
		DEFAULT_DATE("yyyyMMdd"),
		/** デフォルト日付フォーマット 年月 */
		DEFAULT_YM("yyyyMM"),
		/** デフォルト日付フォーマット 年 */
		DEFAULT_YEAR("yyyy"),
		/** デフォルト日付フォーマット 年月日時間 */
		DEFAULT_DATETIME("yyyyMMddHHmm"),
		/** DB date型の文字列表現 */
		DB_DATE("yyyy-MM-dd"),
		/** 8桁での文字列DBに格納する文字列表現(yyyyMMdd) */
		DB_DATE8("yyyyMMdd"),
		/** DB datetime型の文字列表現(yyyy-MM-dd HH:mm:ss.SSS) */
		DB_DATETIME("yyyy-MM-dd HH:mm:ss.SSS"),
		/** グリッド表示日付フォーマット 年月日(yyyy/MM/dd) */
		GRID_YMD("yyyy/MM/dd"),
		/** グリッド表示日付フォーマット 年月(yyyy/MM) */
		GRID_YM("yyyy/MM"),
		/** グリッド表示日付フォーマット 月日(MM/dd) */
		GRID_MD("MM/dd"),
		/** 入力日付フォーマット 年月日(yyMMdd) */
		INP_YMD("yyMMdd"),
		/** 入力日付フォーマット 年月(yyMM) */
		INP_YM("yyMM"),
		/** 入力日付フォーマット 月日(MMdd) */
		INP_MD("MMdd");

		/** 日付フォーマットパターン */
		private String pattern;
		/**
		 * 日付フォーマットパターンを拡張した列挙型を生成
		 *
		 * @param pattern
		 *			日付フォーマットパターン
		 */
		private DATE_FORMAT(final String pattern) {
			this.pattern = pattern;
		}

		/**
		 * 日付フォーマットパターンを返します。
		 *
		 * @return 日付フォーマットパターン
		 */
		public String formatPattern() {
			return this.pattern;
		}
	}

	/**
	 * システム日付を取得する
	 *
	 * @return String システム日付 YYYYMMDD
	 */
	public String getToday() {
		Calendar calendar = new GregorianCalendar();
		Date trialtime = new Date();
		calendar.setTime(trialtime);
		int intTodayY = calendar.get(Calendar.YEAR);
		int intTodayM = calendar.get(Calendar.MONTH) + 1;
		int intTodayD = calendar.get(Calendar.DAY_OF_MONTH);

		String strSdayY = Integer.toString(intTodayY);

		String strSdayM;
		if (intTodayM < 10) {
			strSdayM = "0" + Integer.toString(intTodayM);
		} else {
			strSdayM = Integer.toString(intTodayM);
		}

		String strSdayD;
		if (intTodayD < 10) {
			strSdayD = "0" + Integer.toString(intTodayD);
		} else {
			strSdayD = Integer.toString(intTodayD);
		}
		return strSdayY + strSdayM + strSdayD;
	}

	/**
	 * システム時刻を取得する
	 *
	 * @return String システム時刻 HHMMSS
	 */
	public String getNowTime() {
		Calendar calendar = new GregorianCalendar();
		Date trialtime = new Date();
		calendar.setTime(trialtime);
		int intTodayHH = calendar.get(Calendar.HOUR_OF_DAY);
		int intTodayMM = calendar.get(Calendar.MINUTE);
		int intTodaySS = calendar.get(Calendar.SECOND);

		String strSdayHH;
		if (intTodayHH < 10) {
			strSdayHH = "0" + Integer.toString(intTodayHH);
		} else {
			strSdayHH = Integer.toString(intTodayHH);
		}

		String strSdayMM;
		if (intTodayMM < 10) {
			strSdayMM = "0" + Integer.toString(intTodayMM);
		} else {
			strSdayMM = Integer.toString(intTodayMM);
		}

		String strSdaySS;
		if (intTodaySS < 10) {
			strSdaySS = "0" + Integer.toString(intTodaySS);
		} else {
			strSdaySS = Integer.toString(intTodaySS);
		}

		return strSdayHH + strSdayMM + strSdaySS;
	}

	/**
	 * システム時刻を取得する
	 *
	 * @return String システム時刻 HHMMSS
	 */
	public String getTime(String sept) {
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(this);
		int intTodayHH = calendar.get(Calendar.HOUR_OF_DAY);
		int intTodayMM = calendar.get(Calendar.MINUTE);
		int intTodaySS = calendar.get(Calendar.SECOND);

		String strSdayHH;
		if (intTodayHH < 10) {
			strSdayHH = "0" + Integer.toString(intTodayHH);
		} else {
			strSdayHH = Integer.toString(intTodayHH);
		}

		String strSdayMM;
		if (intTodayMM < 10) {
			strSdayMM = "0" + Integer.toString(intTodayMM);
		} else {
			strSdayMM = Integer.toString(intTodayMM);
		}

		String strSdaySS;
		if (intTodaySS < 10) {
			strSdaySS = "0" + Integer.toString(intTodaySS);
		} else {
			strSdaySS = Integer.toString(intTodaySS);
		}

		return strSdayHH + sept + strSdayMM + sept + strSdaySS;
	}

	/**
	 * 時刻を設定する
	 *
	 * @param time HHMMSS
	 */
	@SuppressWarnings("deprecation")
	public void setTime(String time) {
		super.setHours(Integer.parseInt(time.substring(0, 2)));
		super.setMinutes(Integer.parseInt(time.substring(2, 4)));
		super.setSeconds(Integer.parseInt(time.substring(4, 6)));
	}

	/**
	 * システム日付の前日を取得する
	 *
	 * @return String システム日付の前日 YYYYMMDD
	 */
	public String getYesterday() {
		Calendar calendar = new GregorianCalendar();
		Date trialtime = new Date();
		calendar.setTime(trialtime);
		calendar.add(Calendar.DAY_OF_MONTH, -1);
		int intTodayY = calendar.get(Calendar.YEAR);
		int intTodayM = calendar.get(Calendar.MONTH) + 1;
		int intTodayD = calendar.get(Calendar.DAY_OF_MONTH);

		String strSdayY = Integer.toString(intTodayY);

		String strSdayM;
		if (intTodayM < 10) {
			strSdayM = "0" + Integer.toString(intTodayM);
		} else {
			strSdayM = Integer.toString(intTodayM);
		}

		String strSdayD;
		if (intTodayD < 10) {
			strSdayD = "0" + Integer.toString(intTodayD);
		} else {
			strSdayD = Integer.toString(intTodayD);
		}
		return strSdayY + strSdayM + strSdayD;
	}

	/**
	 * システム日付の翌日を取得する
	 *
	 * @return String システム日付の翌日 YYYYMMDD
	 */
	public String getTomorrow() {
		Calendar calendar = new GregorianCalendar();
		Date trialtime = new Date();
		calendar.setTime(trialtime);
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		int intTodayY = calendar.get(Calendar.YEAR);
		int intTodayM = calendar.get(Calendar.MONTH) + 1;
		int intTodayD = calendar.get(Calendar.DAY_OF_MONTH);

		String strSdayY = Integer.toString(intTodayY);

		String strSdayM;
		if (intTodayM < 10) {
			strSdayM = "0" + Integer.toString(intTodayM);
		} else {
			strSdayM = Integer.toString(intTodayM);
		}

		String strSdayD;
		if (intTodayD < 10) {
			strSdayD = "0" + Integer.toString(intTodayD);
		} else {
			strSdayD = Integer.toString(intTodayD);
		}
		return strSdayY + strSdayM + strSdayD;
	}

	/**
	 * 指定されたパターンでシステム日時を取得する
	 *
	 * @return String システム日時
	 */
	public String getSystemDate(DATE_FORMAT format) {
		Date date = new Date();
		return dateFormat(date, format.formatPattern());
	}

	/**
	 * 指定されたパターンで日付/時刻をフォーマットします。
	 *
	 * @param date
	 *			フォーマット対象となる日付
	 * @param pattern
	 *			日付をフォーマットする際に使用されるパターン
	 * @return フォーマットされた日付（対象日付がnullの場合はnull）
	 */
	private static String dateFormat(java.util.Date date, String pattern) {
		if (date == null) {
			return null;
		}
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		return sdf.format(date);
	}

	/**
	 * 指定されたパターンの文字列を日付/時刻に変換します。
	 *
	 * @param source
	 *			対象となる文字列
	 * @param pattern
	 *			日付に変換する際に使用されるパターン
	 * @return 日付/時刻
	 */
	private static java.util.Date convDate(String source, String pattern) {
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		try {
			return sdf.parse(source);
		} catch (ParseException e) {
			return null;
		} catch (NullPointerException e) {
			return null;
		}
	}

	/**
	 * 指定されたパターンで日付/時間をフォーマットします。
	 *
	 * @param date
	 *			フォーマット対象となる日付
	 * @param format
	 *			日付をフォーマットする際に使用されるパターン
	 * @return フォーマットされた日付
	 */
	public static String dateFormat(java.util.Date date, DATE_FORMAT format) {
		return dateFormat(date, format.formatPattern());
	}


	/**
	 * 指定されたパターンで文字列を日付/時刻に変換します。
	 *
	 * @param source
	 *			対象となる文字列
	 * @param format
	 *			日付に変換する際に使用されるパターン
	 * @return 日付/時刻
	 */
	public static java.util.Date convDate(String source, DATE_FORMAT format) {
		return convDate(source, format.formatPattern());
	}

	/**
	 * 標準形式の年月日 文字列を 日付オブジェクト に変換します。
	 *
	 * @param source
	 *			画面入力形式の年月日文字列
	 * @return 日付オブジェクト
	 */
	public static java.util.Date convDate(String source) {
		return convDate(source, DATE_FORMAT.DEFAULT_DATE);
	}

	/**
	 * 入力形式の年月日 文字列を 日付オブジェクト に変換します。
	 *
	 * @param source
	 *			画面入力形式の年月日文字列
	 * @return 日付オブジェクト
	 */
	public static String getConvInpDate(String source) {
		String addstr = "";
		String value = source.replace("/", "");
		if(value.length()==6){
			addstr = "20";
			if(NumberUtils.toInt(source.substring(0,2)) > 50){
				addstr = "19";
			}
		}
		return (addstr+value).substring(0, 8);
	}

	/**
	 * 入力形式の年月日 文字列を 日付オブジェクト に変換します。
	 *
	 * @param source
	 *			画面入力形式の年月日文字列
	 * @return 日付オブジェクト
	 */
	public static java.util.Date convInpDate(String source) {
		String value = getConvInpDate(source);
		return convDate(value, DATE_FORMAT.DEFAULT_DATE);
	}

	/**
	 * 入力形式の年月日 文字列を 日付オブジェクト に変換します。
	 *
	 * @param source
	 *			画面入力形式の年月日文字列
	 * @return 日付オブジェクト
	 */
	public static java.util.Date convYYMMDD(String source) {
		String addstr = "20";
		if(NumberUtils.toInt(source.substring(0,2)) > 50){
			addstr = "19";
		}
		String value = (addstr+source).replace("/", "").substring(0, 8);
		return convDate(value, DATE_FORMAT.DEFAULT_DATE);
	}

	/**
	 * 指定された日付オブジェクトを 標準形式の年月日 を表す文字列にします。
	 *
	 * @param date
	 *			日付オブジェクト
	 * @return 標準の入力形式の年月日の文字列
	 */
	public static String dateFormat(java.util.Date date) {
		return dateFormat(date, DATE_FORMAT.DEFAULT_DATE);
	}

	/**
	 * 指定された日付オブジェクトを 標準形式の年月 を表す文字列にします。
	 *
	 * @param date
	 *			日付オブジェクト
	 * @return 標準の入力形式の年月日の文字列
	 */
	public static String dateFormatYM(java.util.Date date) {
		return dateFormat(date, DATE_FORMAT.DEFAULT_YM);
	}

	/**
	 * 指定された日付オブジェクトを DB date型の年月日 を表す文字列にします。
	 *
	 * @param date
	 *			日付オブジェクト
	 * @return 標準の入力形式の年月日の文字列
	 */
	public static String dbDateFormat(java.util.Date date) {
		return dateFormat(date, DATE_FORMAT.DB_DATE);
	}

	/**
	 * 標準形式の年月日文字列を DB date型の年月日 を表す文字列にします。
	 *
	 * @param source
	 *			日付文字列
	 * @return DB date型の年月日の文字列
	 */
	public static String dbDateFormat(String source) {
		if (StringUtils.isEmpty(source)) {
			return null;
		}
		return dbDateFormat(convDate(source));
	}

	/**
	 * DB date型の年月日文字列を 標準形式の年月日 を表す文字列にします。
	 *
	 * @param source
	 *			日付文字列
	 * @return 標準の表示形式の年月日の文字列
	 */
	public static String defDateFormat(String source) {
		if (StringUtils.isEmpty(source)) {
			return null;
		}
		return dateFormat(convDate(source, DATE_FORMAT.DB_DATE8));
	}


	/**
	 * 基準日付の中で、最小の時間（00時00分00.000秒）を表す日付オブジェクトを返します。
	 *
	 * @param date
	 *			基準日付
	 * @return 基準日付の中で、最大の時間を表す日付
	 */
	public static Date getMinTime(Date date) {

		Calendar calendar = Calendar.getInstance();

		calendar.setTime(date);

		calendar.set(Calendar.HOUR_OF_DAY, MINTIME_HOUR);
		calendar.set(Calendar.MINUTE, MINTIME_MIN);
		calendar.set(Calendar.SECOND, MINTIME_SEC);
		calendar.set(Calendar.MILLISECOND, MINTIME_MSEC);

		return calendar.getTime();
	}

	/**
	 * 基準日付の中で、最大の時間（23時59分59.999秒）を表す日付オブジェクトを返します。
	 *
	 * @param date
	 *			基準日付
	 * @return 基準日付の中で、最大の時間を表す日付
	 */
	public static Date getMaxTime(Date date) {

		Calendar calendar = Calendar.getInstance();

		calendar.setTime(date);

		calendar.set(Calendar.HOUR_OF_DAY, MAXTIME_HOUR);
		calendar.set(Calendar.MINUTE, MAXTIME_MIN);
		calendar.set(Calendar.SECOND, MAXTIME_SEC);
		calendar.set(Calendar.MILLISECOND, MAXTIME_MSEC);

		return calendar.getTime();
	}

	/**
	 * 指定された日付オブジェクトの指定されたフィールドに、増減値を加えた日付オブジェクトを返却します。
	 *
	 * @param targetDate
	 *			対象日付
	 * @param field
	 *			変更対象フィールド
	 * @param amount
	 *			増減値
	 * @return 増減値を加えた日付オブジェクト
	 */
	public static Date getDate(Date targetDate, int field, int amount) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(targetDate);
		cal.add(field, amount);
		return cal.getTime();
	}

	/**
	 * 指定された日付けオブジェクトの月フィールドに増減値を加えた日付けオブジェクトを返却します。
	 *
	 * @param targetDate
	 *			対象日付け
	 * @param amount
	 *			増減値
	 * @return 増減値を加えた日付けオブジェクト
	 */
	public static Date getMonthAddedDate(Date targetDate, int amount) {
		return getDate(targetDate, Calendar.MONTH, amount);
	}

	/**
	 * 指定された日付けオブジェクトの、日付けフィールドに増減値を加えた日付けオブジェクトを返却します。
	 *
	 * @param targetDate
	 *			対象日付け
	 * @param amount
	 *			増減値
	 * @return 増減値を加えた日付オブジェクト
	 */
	public static Date getDayAddedDate(Date targetDate, int amount) {
		return getDate(targetDate, Calendar.DAY_OF_MONTH, amount);
	}

	/**
	 * 基準日付と同月の1日を表す日付オブジェクトを返します。<br>
	 * 時分秒のフィールドは未定義(0)の状態となります。
	 *
	 * @param date
	 *			基準日付
	 * @return 基準日付と同月の1日を表す日付
	 */
	public static Date getFirstDateOfMonth(Date date) {

		Calendar calendar = Calendar.getInstance();

		calendar.setTime(date);

		// 日付のフィールドを1日に設定します。
		calendar.set(Calendar.DAY_OF_MONTH, 1);

		// 時分秒のフィールドを未定義にします。
		return getMinTime(calendar.getTime());
	}

	/**
	 * 基準日付と同月の最終日を表す日付オブジェクトを返します。<br>
	 * 時分秒の各フィールドは最大(23:59:59.999)の状態となります。
	 *
	 * @param date
	 *			基準日付
	 * @return 基準日付と同月の最終日を表す日付
	 */
	public static Date getLastDateOfMonth(Date date) {
		Calendar calendar = Calendar.getInstance();

		calendar.setTime(getFirstDateOfMonth(date));

		calendar.add(Calendar.MONTH, 1);

		calendar.add(Calendar.DAY_OF_YEAR, -1);

		return getMaxTime(calendar.getTime());
	}

	/**
	 * 指定された形式の年月日 文字列 を 基準日付とし、同月の1日を表す日付オブジェクトを返します。<br>
	 * 時分秒のフィールドは未定義(0)の状態となります。
	 *
	 * @param date
	 *			基準日付
	 * @return 基準日付と同月の1日を表す日付
	 */
	public static Date getFirstDateOfMonth(String source, DATE_FORMAT format) {
		return getFirstDateOfMonth(convDate(source, format));
	}

	/**
	 * 指定された形式の年月日 文字列 を 基準日付とし、同月の最終日を表す日付オブジェクトを返します。<br>
	 * 時分秒の各フィールドは最大(23:59:59.999)の状態となります。
	 *
	 * @param date
	 *			基準日付
	 * @return 基準日付と同月の最終日を表す日付
	 */
	public static Date getLastDateOfMonth(String source, DATE_FORMAT format) {
		return getLastDateOfMonth(convDate(source, format));
	}

	/**
	 * 標準形式の年月日 文字列 を 基準日付とし、同月の1日を表す日付オブジェクトを返します。<br>
	 * 時分秒のフィールドは未定義(0)の状態となります。
	 *
	 * @param date
	 *			基準日付
	 * @return 基準日付と同月の1日を表す日付
	 */
	public static Date getFirstDateOfMonth(String source) {
		return getFirstDateOfMonth(convDate(source, DATE_FORMAT.DEFAULT_DATE));
	}

	/**
	 * 標準形式の年月日 文字列 を 基準日付とし、同月の最終日を表す日付オブジェクトを返します。<br>
	 * 時分秒の各フィールドは最大(23:59:59.999)の状態となります。
	 *
	 * @param date
	 *			基準日付
	 * @return 基準日付と同月の最終日を表す日付
	 */
	public static Date getLastDateOfMonth(String source) {
		return getLastDateOfMonth(convDate(source, DATE_FORMAT.DEFAULT_DATE));
	}

}
