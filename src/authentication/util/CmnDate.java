/*
 * 作成日: 2008/12/05
 *
 */

package authentication.util;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Dateを扱うクラス
 */
public class CmnDate extends Date {

	/**
	 *
	 */
	private static final long serialVersionUID = 7224121995572012726L;

	/**
	 *
	 */
	public CmnDate() {
		super();
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
	public String getNowTimeHHMM() {
		Calendar calendar = new GregorianCalendar();
		Date trialtime = new Date();
		calendar.setTime(trialtime);
		int intTodayHH = calendar.get(Calendar.HOUR_OF_DAY);
		int intTodayMM = calendar.get(Calendar.MINUTE);

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

		return strSdayHH + strSdayMM;
	}

	public int getTimeHH(String hh) {
		return Integer.parseInt(hh.substring(0, 2));
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
	public void setTime(String time) {
		super.setHours(Integer.parseInt(time.substring(0, 2)));
		super.setMinutes(Integer.parseInt(time.substring(2, 4)));
		super.setSeconds(Integer.parseInt(time.substring(4, 6)));
	}

	/**
	 * 日付計算()
	 *
	 *  prm cal 指定日付, addYear 年, addMonth 月, addDate 日
	 *  return Calendar
	 *
	 **/
	public String addDate_String(Calendar cal,int addYera,int addMonth,int addDate){
		//日付指定無しなら現在日付を取得
		if (cal == null) {
			cal = Calendar.getInstance();
		}
		cal.add(Calendar.YEAR, addYera);
		cal.add(Calendar.MONTH, addMonth);
		cal.add(Calendar.DATE, addDate);

		String strSdayY = Integer.toString(cal.get(Calendar.YEAR));

		String strSdayM;
		if (cal.get(Calendar.MONTH)+1 < 10) {
			strSdayM = "0" + Integer.toString(cal.get(Calendar.MONTH)+1);
		} else {
			strSdayM = Integer.toString(cal.get(Calendar.MONTH)+1);
		}

		String strSdayD;
		if (cal.get(Calendar.DATE) < 10) {
			strSdayD = "0" + Integer.toString(cal.get(Calendar.DATE));
		} else {
			strSdayD = Integer.toString(cal.get(Calendar.DATE));
		}
		return strSdayY + strSdayM + strSdayD;
	}
}
