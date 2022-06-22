package dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;

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
public class ReportMM003Dao extends ItemDao {

	/**
	 * インスタンスを生成します。
	 * @param source
	 */
	public ReportMM003Dao(String JNDIname) {
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

		String szMoyscd		= getMap().get("MOYSCD");	// 催しコード
		String szKanrino	= getMap().get("KANRINO");	// 管理番号
		String szBmflg		= getMap().get("BMFLG");	// B/Mフラグ
		String szBmnno		= getMap().get("BMNNO");	// B/M番号
		String szShncd		= getMap().get("SHNCD");	// 商品コード
//		String sortName		= getMap().get("sortName");
//		String sortOrder	= getMap().get("sortOrder");

		// タイトル情報(任意)設定
		List<String> titleList = new ArrayList<String>();

		// 基本情報取得
		JSONArray array = getTOKMOYCDData(getMap());

		// 一覧表情報
		StringBuffer sbSQL = new StringBuffer();

		sbSQL.append("select");
		sbSQL.append(" right ('000' || T1.TENCD, 3) as F1");									// F1 : 店番
		sbSQL.append(", T1.TENKN as F2");														// F2 : 店舗名
		sbSQL.append(" from INAMS.MSTTEN T1");
		sbSQL.append(" left join INATK.TOKMM_TEN T2");
		sbSQL.append(" on T1.TENCD = T2.TENCD");
		sbSQL.append(" left join INATK.TOKMM_SHN T3");
		sbSQL.append(" on T2.MOYSKBN = T3.MOYSKBN");
		sbSQL.append(" and T2.MOYSSTDT = T3.MOYSSTDT");
		sbSQL.append(" and T2.MOYSRBAN = T3.MOYSRBAN");
		sbSQL.append(" and T2.BMFLG = T3.BMFLG");
		sbSQL.append(" and T2.BMNO = T3.BMNO");
		sbSQL.append(" and T2.BMNCD = T3.BMNCD");
		sbSQL.append(" and T2.KANRINO = T3.KANRINO");
		sbSQL.append(" where T2.MOYSKBN = "+StringUtils.substring(szMoyscd, 0, 1));
		sbSQL.append(" and T2.MOYSSTDT = "+StringUtils.substring(szMoyscd, 1, 7));
		sbSQL.append(" and T2.MOYSRBAN = "+StringUtils.substring(szMoyscd, 7, 10));
		sbSQL.append(" and T2.BMFLG = "+szBmflg);
		sbSQL.append(" and T2.BMNO = "+szBmnno);
		sbSQL.append(" and T2.KANRINO = "+szKanrino);
		sbSQL.append(" and T3.SHNCD = "+szShncd);
		sbSQL.append(" order by T1.TENCD");

		// オプション情報（タイトル）設定
		JSONObject option = new JSONObject();
		option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
		option.put("rows_", array);
		setOption(option);


		if (DefineReport.ID_DEBUG_MODE) System.out.println(getClass().getSimpleName()+"[sql]"+sbSQL.toString());
		return sbSQL.toString();
	}

	/**
	 * 正情報取得処理
	 *
	 * @throws Exception
	 */
	public JSONArray getTOKMOYCDData(HashMap<String, String> map) {
		String szMoyscd		= getMap().get("MOYSCD");	// 催しコード
		String szKanrino	= getMap().get("KANRINO");	// 管理番号
		String szBmflg		= getMap().get("BMFLG");	// B/Mフラグ
		String szBmnno		= getMap().get("BMNNO");	// B/M番号
		String szShncd		= getMap().get("SHNCD");	// 商品コード

		ArrayList<String> paramData = new ArrayList<String>();

		StringBuffer sbSQL = new StringBuffer();
		sbSQL.append(" select");
		sbSQL.append(" substr(T1.SHNCD, 1, 4)||'-'||substr(T1.SHNCD, 5, 8)  as F1");								// F1 : 商品コード
		sbSQL.append(", T1.SHNKN as F2");																			// F2 : 商品名
		sbSQL.append(",T2.MOYSKBN||'-'||right('000000'||T2.MOYSSTDT, 6)||'-'||right('000'||T2.MOYSRBAN, 3) as F3");	// F3 : 催しコード
		sbSQL.append(" from INAMS.MSTSHN T1");
		sbSQL.append(" left join INATK.TOKMM_SHN T2");
		sbSQL.append(" on T1.SHNCD = T2.SHNCD");
		sbSQL.append(" where T2.MOYSKBN = "+StringUtils.substring(szMoyscd, 0, 1));
		sbSQL.append(" and T2.MOYSSTDT = "+StringUtils.substring(szMoyscd, 1, 7));
		sbSQL.append(" and T2.MOYSRBAN = "+StringUtils.substring(szMoyscd, 7, 10));
		sbSQL.append(" and T2.BMFLG = "+szBmflg);
		sbSQL.append(" and T2.BMNO = "+szBmnno);
		sbSQL.append(" and T2.KANRINO = "+szKanrino);
		sbSQL.append(" and T2.SHNCD = "+szShncd);

		ItemList iL = new ItemList();
		@SuppressWarnings("static-access")
		JSONArray array = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

		return array;
	}

	private void outputQueryList() {

		// 検索条件の加工クラス作成
		JsonArrayData jad = new JsonArrayData();
		jad.setJsonString(getJson());

	}
}
