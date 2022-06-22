/**
 *
 */
package common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import net.sf.json.JSONArray;

/**
 * DivEnumコンボボックス用データ生成クラス<br>
 * DivEnum:カレンダーマスタ情報取得
 *
 * @author EATONE
 *
 */
public class DivEnumList{

	/** カレンダーマップ */
	private static Map<String, Map<String, String>> calMap;

	/**
	 * 初期化します。
	 */
	public static void divEnumInitialize() {
		calMapInitialize();
	}


	/**
	 * カレンダーマップを取得します。
	 *
	 * @return カレンダーマップ
	 */
	public static Map<String, Map<String, String>> getCalMap() {
		return calMap;
	}

	/**
	 * カレンダーマップを初期化します。
	 */
	public static void calMapInitialize() {
		calMap = new HashMap<String, Map<String, String>>();

		ItemList iL = new ItemList();
		ArrayList<String> paramData = new ArrayList<String>();

		// SATTRスキーマ
		Map<String, String> cal = new HashMap<String,String>();
		@SuppressWarnings("static-access")
		JSONArray calList = iL.selectJSONArray(DefineReport.ID_SQL_CAL, paramData, Defines.STR_JNDI_DS);
		if(calList.size() > 0){
			cal.put(DefineReport.MINDT, ObjectUtils.toString(calList.getJSONObject(0).get(DefineReport.MINDT)));
			cal.put(DefineReport.MAXDT, ObjectUtils.toString(calList.getJSONObject(0).get(DefineReport.MAXDT)));
		}else{
			cal.put(DefineReport.MINDT, "0");
			cal.put(DefineReport.MAXDT, "99999999");
		}
		calMap.put(DefineReport.Schema.INAMS.getVal(), cal);

	}

	/**
	 * カレンダー最小日付を取得します。
	 *
	 * @param schema スキーマ
	 * @return カレンダー最小日付
	 */
	public static String getMinDt(DefineReport.Schema schema) {
		return StringUtils.defaultIfEmpty(getCalMap().get(schema.getVal()).get(DefineReport.MINDT),"0");
	}

	/**
	 * カレンダー最大日付を取得します。
	 *
	 * @param schema スキーマ
	 * @return カレンダー最大日付
	 */
	public static String getMaxDt(DefineReport.Schema schema) {
		return StringUtils.defaultIfEmpty(getCalMap().get(schema.getVal()).get(DefineReport.MAXDT),"99999999");
	}

}