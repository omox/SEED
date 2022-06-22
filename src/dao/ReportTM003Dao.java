package dao;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import authentication.bean.User;
import common.DefineReport;
import common.DefineReport.Option;
import common.JsonArrayData;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 */
public class ReportTM003Dao extends ItemDao {

	/**
	 * インスタンスを生成します。
	 * @param source
	 */
	public ReportTM003Dao(String JNDIname) {
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
		ArrayList<String> paramData = new ArrayList<String>();
		String szMoysstdt = getMap().get("MOYSSTDT");			// 催しコード
		String szMoyskbn  = getMap().get("MOYSKBN");			// 催し区分
		JSONArray moyskbnAllArray	= JSONArray.fromObject(getMap().get("MOYSKBN_DATA"));	// 全催し区分

		// パラメータ確認
/*		// 必須チェック
		if ((btnId == null)) {
			System.out.println(super.getConditionLog());
			return "";
		}*/

		// タイトル情報(任意)設定
		List<String> titleList = new ArrayList<String>();

		// 一覧表情報
		StringBuffer sbSQL = new StringBuffer();
		sbSQL.append(DefineReport.ID_SQL_CMN_WEEK);
		sbSQL.append(" select");
		sbSQL.append("  T1.MOYSKBN||'-'||RIGHT(T1.MOYSSTDT, 6)||'-'||RIGHT('000'||T1.MOYSRBAN,3) as F1");
		sbSQL.append(" ,T1.NENMATKBN as F2");
		sbSQL.append(" ,TO_CHAR(TO_DATE(T1.HBSTDT, 'YYYYMMDD'), 'YY/MM/DD')||(select JWEEK from WEEK where CWEEK = dayofweek(TO_DATE(T1.HBSTDT, 'YYYYMMDD')))");
		sbSQL.append("  ||'～'||TO_CHAR(TO_DATE(T1.HBEDDT, 'YYYYMMDD'), 'YY/MM/DD')||(select JWEEK from WEEK where CWEEK = dayofweek(TO_DATE(T1.HBEDDT, 'YYYYMMDD'))) as F3");
		sbSQL.append(" ,TO_CHAR(TO_DATE(T1.NNSTDT, 'YYYYMMDD'), 'YY/MM/DD')||(select JWEEK from WEEK where CWEEK = dayofweek(TO_DATE(T1.NNSTDT, 'YYYYMMDD')))");
		sbSQL.append("  ||'～'||TO_CHAR(TO_DATE(T1.NNEDDT, 'YYYYMMDD'), 'YY/MM/DD')||(select JWEEK from WEEK where CWEEK = dayofweek(TO_DATE(T1.NNEDDT, 'YYYYMMDD'))) as F4");
		sbSQL.append(" ,TO_CHAR(TO_DATE(T1.PLUSDDT, 'YYYYMMDD'), 'YY/MM/DD')||(select JWEEK from WEEK where CWEEK = dayofweek(TO_DATE(T1.PLUSDDT, 'YYYYMMDD'))) as F5");
		sbSQL.append(" ,T1.MOYKN as F6");
		sbSQL.append(" ,T1.MOYAN as F7");
		sbSQL.append(" ,T1.MOYSKBN as F8,T1.MOYSSTDT as F9,T1.MOYSRBAN as F10");
		sbSQL.append(" from INATK.TOKMOYCD T1");
		sbSQL.append(" where T1.UPDKBN = 0");
		if(!DefineReport.Values.ALL.getVal().equals(szMoyskbn)){
			sbSQL.append(" and T1.MOYSKBN = ? ");
			paramData.add(szMoyskbn);
		}else{
			sbSQL.append(" and T1.MOYSKBN IN ("+StringUtils.removeEnd(StringUtils.replace(moyskbnAllArray.join(","),"\"","'"),",")+")");
		}
		if(!StringUtils.isEmpty(szMoysstdt)){
			sbSQL.append(" and T1.MOYSSTDT >= ? ");
			paramData.add(szMoysstdt);
		}
		sbSQL.append(" order by T1.MOYSKBN,T1.MOYSSTDT,T1.MOYSRBAN");
		setParamData(paramData);

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
	}

	/** 固定値定義（区分:催し区分） */
	public enum ValMoyKbn implements Option {
		/** レギュラー */
		R("0","レギュラー"),
		/** 特売 */
		T("1","特売"),
		/** スポット */
		S("2","スポット");
		private final String val;
		private final String txt;
		/** 初期化 */
		private ValMoyKbn(String val, String txt) {
			this.val = val;
			this.txt = txt;
		}
		/** @return val 値 */
		public String getVal() { return val; }
		/** @return txt 表示名称 */
		public String getTxt() { return txt; }
	}
}
