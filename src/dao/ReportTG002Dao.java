package dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import authentication.bean.User;
import common.DefineReport;
import common.Defines;
import common.ItemList;
import common.JsonArrayData;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 */
public class ReportTG002Dao extends ItemDao {

	/**
	 * インスタンスを生成します。
	 * @param source
	 */
	public ReportTG002Dao(String JNDIname) {
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

		String szMoyskbn	= getMap().get("MOYSKBN");	// 催し区分
		String szMoysstdt	= getMap().get("MOYSSTDT");	// 催しコード（催し開始日）
		String szMoysrban	= getMap().get("MOYSRBAN");	// 催し連番

		// パラメータ確認
		// 必須チェック
		if ((szMoyskbn == null) || (szMoysstdt == null) || (szMoysrban == null)) {
			System.out.println(super.getConditionLog());
			return "";
		}

		// タイトル情報(任意)設定
		List<String> titleList = new ArrayList<String>();


		// 基本情報取得
		JSONArray array = getTOKMOYCDData(getMap());


		// 一覧表情報
		StringBuffer sbSQL = new StringBuffer();
		sbSQL.append(" select");
		sbSQL.append("   max(T2.KYOSEIFLG) as F1");
		sbSQL.append("  ,right('000'||T2.TENGPCD,3) as F2");
		sbSQL.append("  ,max(T2.TENGPKN) as F3");
		sbSQL.append("  ,max(case when T3.LDTENKBN = 1 then right('000'||T3.TENCD,3) end) as F4");
		sbSQL.append("  ,max(case when T3.LDTENKBN = 1 then M1.TENKN end) as F5");
		sbSQL.append("  ,count(T3.TENCD) as F6");
		sbSQL.append("  ,max(case when T2.QASYUKBN = 1 then 1 else 0 end) as F7");
		sbSQL.append("  ,max(case when T2.QASYUKBN = 2 then 1 else 0 end) as F8");
		sbSQL.append("  ,max(case when T2.QASYUKBN = 3 then 1 else 0 end) as F9");
		sbSQL.append("  ,max(case when T2.QASYUKBN = 4 then 1 else 0 end) as F10");
		sbSQL.append("  ,max(case when T2.QASYUKBN = 5 then 1 else 0 end) as F11");
		sbSQL.append("  ,T1.MOYSKBN");
		sbSQL.append("  ,T1.MOYSSTDT");
		sbSQL.append("  ,T1.MOYSRBAN");
		sbSQL.append("  ,T2.TENGPCD");
		sbSQL.append("  ,max(case when T3.LDTENKBN = 1 then T3.TENCD end)");
		sbSQL.append("  ,max(to_char(T2.UPDDT, 'YYYYMMDDHH24MISSNNNNNN')) as HDN_UPDDT");
		sbSQL.append(" from");
		sbSQL.append("  INATK.TOKMOYCD T1 ");
		sbSQL.append("  inner join INATK.TOKTG_TENGP T2 on T1.MOYSKBN = T2.MOYSKBN and T1.MOYSSTDT = T2.MOYSSTDT and T1.MOYSRBAN = T2.MOYSRBAN");
		sbSQL.append("  and T1.MOYSKBN = "+szMoyskbn+"");
		sbSQL.append("  and T1.MOYSSTDT = "+szMoysstdt+"");
		sbSQL.append("  and T1.MOYSRBAN = "+szMoysrban+"");
		sbSQL.append("  and T1.UPDKBN = 0 and T2.UPDKBN = 0");
		sbSQL.append("  left outer join INATK.TOKTG_TEN T3 on T1.MOYSKBN = T3.MOYSKBN and T1.MOYSSTDT = T3.MOYSSTDT and T1.MOYSRBAN = T3.MOYSRBAN and T2.TENGPCD = T3.TENGPCD and T2.KYOSEIFLG = T3.KYOSEIFLG");
		sbSQL.append("  left outer join INAMS.MSTTEN M1 on T3.TENCD = M1.TENCD");
		sbSQL.append(" group by T1.MOYSKBN, T1.MOYSSTDT, T1.MOYSRBAN, T2.TENGPCD");
		sbSQL.append(" order by T1.MOYSKBN, T1.MOYSSTDT, T1.MOYSRBAN, T2.TENGPCD");

		// オプション情報（タイトル）設定
		JSONObject option = new JSONObject();
		option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
		option.put("rows_", array);
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


	/**
	 * 正情報取得処理
	 *
	 * @throws Exception
	 */
	public JSONArray getTOKMOYCDData(HashMap<String, String> map) {
		String szMoyskbn	= map.get("MOYSKBN");	// 催し区分
		String szMoysstdt	= map.get("MOYSSTDT");	// 催しコード（催し開始日）
		String szMoysrban	= map.get("MOYSRBAN");	// 催し連番

		ArrayList<String> paramData = new ArrayList<String>();


		StringBuffer sbSQL = new StringBuffer();
		sbSQL.append(DefineReport.ID_SQL_CMN_WEEK);
		sbSQL.append("  select");
		sbSQL.append("    T1.MOYSKBN||'-'||right(T1.MOYSSTDT, 6)||'-'||right('000'||T1.MOYSRBAN, 3) as F1");
		sbSQL.append("  , T1.MOYKN as F2");
		sbSQL.append("  , TO_CHAR(TO_DATE(T1.HBSTDT, 'YYYYMMDD'), 'YY/MM/DD')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(TO_DATE(T1.HBSTDT, 'YYYYMMDD')))");
		sbSQL.append("    ||'～'||");
		sbSQL.append("    TO_CHAR(TO_DATE(T1.HBEDDT, 'YYYYMMDD'), 'YY/MM/DD')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(TO_DATE(T1.HBEDDT, 'YYYYMMDD'))) as F3");
		sbSQL.append("  , T1.OPERATOR as F4");
		sbSQL.append("  , nvl(TO_CHAR(T1.ADDDT, 'YY/MM/DD'),'__/__/__') as F5");
		sbSQL.append("  , nvl(TO_CHAR(T1.UPDDT, 'YY/MM/DD'),'__/__/__') as F6");
		sbSQL.append("  , TO_CHAR(T1.UPDDT, 'YYYYMMDDHH24MISSNNNNNN') as F7");
		sbSQL.append("  from INATK.TOKMOYCD T1");
		sbSQL.append("  where T1.UPDKBN = 0");
		sbSQL.append("  and T1.MOYSKBN = "+szMoyskbn+"");
		sbSQL.append("  and T1.MOYSSTDT = "+szMoysstdt+"");
		sbSQL.append("  and T1.MOYSRBAN = "+szMoysrban+"");

		ItemList iL = new ItemList();
		@SuppressWarnings("static-access")
		JSONArray array = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

		return array;
	}
}
