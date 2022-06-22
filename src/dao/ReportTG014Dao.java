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
public class ReportTG014Dao extends ItemDao {

	/**
	 * インスタンスを生成します。
	 * @param source
	 */
	public ReportTG014Dao(String JNDIname) {
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
		String szTengpcd	= getMap().get("TENGPCD");	// 催し連番

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

		// DB検索用パラメータ
		ArrayList<String> paramData = new ArrayList<String>();

		paramData.add(szMoyskbn);
		paramData.add(szMoysstdt);
		paramData.add(szMoysrban);
		paramData.add(szTengpcd);
		// 一覧表情報
		StringBuffer sbSQL = new StringBuffer();
		sbSQL.append(" select");
		sbSQL.append(" right('000' || TTQT.TENCD, 3)");
		sbSQL.append(" ,MTTN.TENKN");
		sbSQL.append(" ,CASE WHEN TTQT.MBANSFLG = 1 THEN '〇' WHEN TTQT.MBANSFLG = 0 THEN '×' END");
		sbSQL.append(" ,TTQT.MBSYFLG");
		sbSQL.append(" ,TTQT.KYOSEIFLG");
		sbSQL.append(" ,CASE WHEN TTQT.MBSYFLG = '2' THEN 1 ELSE 0 END MBSYFLG ");
		sbSQL.append(" from");
		sbSQL.append(" INATK.TOKTG_QATEN TTQT");
		sbSQL.append(" left join");
		sbSQL.append(" INAMS.MSTTEN MTTN");
		sbSQL.append(" on TTQT.TENCD = MTTN.TENCD");
		sbSQL.append(" where");
		sbSQL.append(" TTQT.MOYSKBN = ? ");
		sbSQL.append(" and TTQT.MOYSSTDT = ? ");
		sbSQL.append(" and TTQT.MOYSRBAN = ? ");
		sbSQL.append(" and TTQT.TENGPCD = ? ");
		sbSQL.append(" order by right('000' || TTQT.TENCD, 3)");

		setParamData(paramData);

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
		String szTengpcd	= map.get("TENGPCD");	// 催し連番


		ArrayList<String> paramData = new ArrayList<String>();

		for(int i = 0;i<3;i++){
			paramData.add(szMoyskbn);
			paramData.add(szMoysstdt);
			paramData.add(szMoysrban);
		}

		paramData.add(szTengpcd);

		StringBuffer sbSQL = new StringBuffer();
		sbSQL.append(DefineReport.ID_SQL_CMN_WEEK);
		sbSQL.append(" select");
		sbSQL.append("    TTQT1.MOYSKBN || '-' || right (TTQT1.MOYSSTDT, 6) || '-' || right ('000' || TTQT1.MOYSRBAN, 3) as F1");
		sbSQL.append("  , TMYC.MOYKN as F2");
		sbSQL.append("  , TO_CHAR(TO_DATE(TMYC.HBSTDT, 'YYYYMMDD'), 'YY/MM/DD')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(TO_DATE(TMYC.HBSTDT, 'YYYYMMDD')))");
		sbSQL.append("    ||'～'||");
		sbSQL.append("    TO_CHAR(TO_DATE(TMYC.HBEDDT, 'YYYYMMDD'), 'YY/MM/DD')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(TO_DATE(TMYC.HBEDDT, 'YYYYMMDD'))) as F3");
		sbSQL.append("  , TTKH.HBOKUREFLG as F4");
		sbSQL.append("  , right('000' || TTQT2.TENGPCD, 3) as F5");
		sbSQL.append("  , TTTG.TENGPKN as F6");
		sbSQL.append("  , TTQT2.CNT as F7");
		sbSQL.append("  , right('000' || TTQT1.TENCD, 3) as F8");
		sbSQL.append("  , MTTN.TENKN as F9");
		sbSQL.append("  , (case when TTQG.QASYUKBN = 1  then '1' else '0' END) as F10");
		sbSQL.append("  , (case when TTQG.QASYUKBN = 2  then '1' else '0' END) as F11");
		sbSQL.append("  , (case when TTQG.QASYUKBN = 3  then '1' else '0' END) as F12");
		sbSQL.append("  , (case when TTQG.QASYUKBN = 4  then '1' else '0' END) as F13");
		sbSQL.append(" from");
		sbSQL.append("  INATK.TOKTG_QAGP TTQG ");
		sbSQL.append(" left join");
		sbSQL.append(" (select");
		sbSQL.append(" TTQT.MOYSKBN");
		sbSQL.append(" , TTQT.MOYSSTDT");
		sbSQL.append(" , TTQT.MOYSRBAN");
		sbSQL.append(" , TTQT.TENCD");
		sbSQL.append(" , TTQT.TENGPCD");
		sbSQL.append(" from");
		sbSQL.append(" INATK.TOKTG_QAGP TTQG");
		sbSQL.append(" inner join INATK.TOKTG_QATEN TTQT");
		sbSQL.append(" on  TTQG.MOYSKBN  = TTQT.MOYSKBN");
		sbSQL.append(" and TTQG.MOYSSTDT = TTQT.MOYSSTDT");
		sbSQL.append(" and TTQG.MOYSRBAN = TTQT.MOYSRBAN");
		sbSQL.append(" where");
		sbSQL.append(" TTQT.MOYSKBN = ? ");
		sbSQL.append(" and TTQT.MOYSSTDT = ? ");
		sbSQL.append(" and TTQT.MOYSRBAN = ? ");
		sbSQL.append(" and  TTQT.LDTENKBN = 1");
		sbSQL.append(" group by");
		sbSQL.append("  TTQT.MOYSKBN");
		sbSQL.append(" ,TTQT.MOYSSTDT");
		sbSQL.append(" ,TTQT.MOYSRBAN");
		sbSQL.append(" ,TTQT.TENCD");
		sbSQL.append(" ,TTQT.TENGPCD");
		sbSQL.append(" ) TTQT1");
		sbSQL.append(" on TTQT1.TENGPCD =  TTQG.TENGPCD");
		sbSQL.append(" left join");
		sbSQL.append(" (select");
		sbSQL.append(" count(*) AS CNT");
		sbSQL.append(" , TTQT.TENGPCD");
		sbSQL.append(" from");
		sbSQL.append(" INATK.TOKTG_QATEN TTQT");
		sbSQL.append(" where");
		sbSQL.append(" TTQT.MOYSKBN = ? ");
		sbSQL.append(" and TTQT.MOYSSTDT = ? ");
		sbSQL.append(" and TTQT.MOYSRBAN = ? ");
		sbSQL.append(" group by");
		sbSQL.append(" TTQT.MOYSKBN");
		sbSQL.append(" , TTQT.MOYSSTDT");
		sbSQL.append(" , TTQT.MOYSRBAN");
		sbSQL.append(" , TTQT.TENGPCD) TTQT2");
		sbSQL.append(" on TTQT2.TENGPCD =  TTQG.TENGPCD");
		sbSQL.append(" left join INAMS.MSTTEN MTTN");
		sbSQL.append(" on MTTN.TENCD = TTQT1.TENCD");
		sbSQL.append(" left join INATK.TOKTG_TENGP TTTG");
		sbSQL.append(" on  TTTG.TENGPCD  = TTQT1.TENGPCD");
		sbSQL.append(" and TTTG.MOYSKBN  = TTQT1.MOYSKBN");
		sbSQL.append(" and TTTG.MOYSSTDT = TTQT1.MOYSSTDT");
		sbSQL.append(" and TTTG.MOYSRBAN = TTQT1.MOYSRBAN");
		sbSQL.append(" left join INATK.TOKMOYCD TMYC");
		sbSQL.append(" on TMYC.MOYSKBN = TTQT1.MOYSKBN");
		sbSQL.append(" and TMYC.MOYSSTDT = TTQT1.MOYSSTDT");
		sbSQL.append(" and TMYC.MOYSRBAN = TTQT1.MOYSRBAN");
		sbSQL.append(" left join INATK.TOKTG_KHN TTKH");
		sbSQL.append(" on TTKH.MOYSKBN = TTQT1.MOYSKBN");
		sbSQL.append(" and TTKH.MOYSSTDT = TTQT1.MOYSSTDT");
		sbSQL.append(" and TTKH.MOYSRBAN = TTQT1.MOYSRBAN");
		sbSQL.append(" where");
		sbSQL.append(" TTQG.MOYSKBN = ? ");
		sbSQL.append(" and TTQG.MOYSSTDT = ? ");
		sbSQL.append(" and TTQG.MOYSRBAN = ? ");
		sbSQL.append(" and TTQT2.TENGPCD = ? ");
		sbSQL.append(" order by F1");

		setParamData(paramData);

		ItemList iL = new ItemList();
		@SuppressWarnings("static-access")
		JSONArray array = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

		return array;
	}
}
