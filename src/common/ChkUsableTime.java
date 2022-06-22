/*
 * 作成日: 2013/12/18
 *
 */
package common;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.lang.StringUtils;

import authentication.connection.DBConnection;
import authentication.defines.SQL;
import net.sf.json.JSONArray;

/**
 * システム利用可能時間帯チェッククラス
 */
public class ChkUsableTime {
	private CmnDate dayfrom_;
	private CmnDate dayto_;

	// 許可ユーザー
	private static final String ID_USER_NAME = "system";

	/**
	 * コンストラクタ <br>
	 * web.xmlのシステム利用時間をパラメータとする
	 *
	 * @param fromData
	 * @param toData
	 */
	public ChkUsableTime(String fromData, String toData) {
		String from_ = fromData + "00";
		String to_ = toData + "59";

		dayfrom_ = new CmnDate();
		dayfrom_.setTime(from_);
		dayto_ = new CmnDate();
		dayto_.setTime(to_);
	}

	/**
	 * 利用可能時間外かを返す
	 *
	 * @param userid
	 * @return
	 */
	public boolean isCloseTime(String userid) {
		// if(true) { return false; }	// 無効化

		// 許可ユーザーの場合
		if (ID_USER_NAME.equals(userid)) { return false; }

		// from <= systime <= to はOK
		Date day = new Date();
		if ((day.compareTo(dayfrom_) == -1) || (day.compareTo(dayto_) == 1)) { return true; }
		return false;
	}

	/**
	 * メンテナンス中かを返す
	 *
	 * @param userid
	 * @return
	 */
	public boolean isWaitTime(String userid) {
		// if(true) { return false; }	// 無効化

		// 許可ユーザーの場合
		if (ID_USER_NAME.equals(userid)) { return false; }

		int cnt = 1;
		Map<String, Object> mu = null;
		try {
			String sql = "select count(*) as CNT from " + SQL.system_schema + ".SYS_WAIT";
			QueryRunner qr = new QueryRunner(DBConnection.getDataSource());
			MapHandler rsh = new MapHandler();
			mu = qr.query(sql, rsh);
			cnt = Integer.parseInt(mu.get("CNT").toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return (cnt > 0);
	}

	/**
	 * 利用時間外確認処理
	 * @return true 利用時間外
	 */
	public boolean isTimeOut (String reportNo){
		// 利用時間外チェック

		if(StringUtils.isEmpty(reportNo)){
			// レポートNoが空の場合はチェックを行わない
			return false;
		}

		StringBuffer sbSQL = new StringBuffer();
		sbSQL.append("with WK as ( select");
		sbSQL.append(" case");
		sbSQL.append(" when CD_POSITION = "+DefineReport.systemPosition.POS_HM.getVal()+" then '1'");
		sbSQL.append(" when CD_POSITION = "+DefineReport.systemPosition.POS_HK.getVal()+" then '2'");
		sbSQL.append(" when CD_POSITION = "+DefineReport.systemPosition.POS_TK.getVal()+" then '3'");
		sbSQL.append(" end as VALUE");
		sbSQL.append(" from KEYSYS.SYS_MENU");
		sbSQL.append(" where CD_GROUP = "+DefineReport.systemGroups.INAGEYA.getVal());
		sbSQL.append(" and CD_REPORT_NO = ?");

		sbSQL.append(" )");
		sbSQL.append(" select");
		sbSQL.append("  case");
		sbSQL.append("  when WK.value = '1' then CTLFLG01");
		sbSQL.append("  when WK.value = '2' then CTLFLG01");
		sbSQL.append("  when WK.value = '3' then CTLFLG02");
		sbSQL.append("  end as VALUE");
		sbSQL.append(" from INAAD.SYSSHORIDT, WK ");

		ArrayList<String> paramData = new ArrayList<String>();
		paramData.add(reportNo);

		ItemList iL = new ItemList();
		@SuppressWarnings("static-access")
		JSONArray array = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

		if(array.size() > 0 && StringUtils.isNotEmpty(array.optJSONObject(0).optString("VALUE"))){
			String kbn = array.optJSONObject(0).optString("VALUE");

			if(StringUtils.equals("1", kbn)){
				//  利用時間外の場合
				return true;
			}
		}
		return false;
	}
}
