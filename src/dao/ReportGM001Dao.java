package dao;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import authentication.bean.User;
import common.DefineReport;
import common.JsonArrayData;
import net.sf.json.JSONObject;

/**
 *
 */
public class ReportGM001Dao extends ItemDao {

	/**
	 * インスタンスを生成します。
	 * @param source
	 */
	public ReportGM001Dao(String JNDIname) {
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

		String szMoysKbn	= getMap().get("MOYSKBN");	// 催し区分
		String szMoysStDt	= getMap().get("MOYSSTDT");	// 催し開始日

		// 配送グループ名称（漢字）に入力があった場合部分一致検索を行う
		// DB検索用パラメータ
		String sqlWhere = "";
		ArrayList<String> paramData = new ArrayList<String>();

		if(StringUtils.isEmpty(szMoysKbn)){
			sqlWhere += "T1.MOYSKBN=null ";

		// 全てを選択された場合
		}else if (szMoysKbn.equals("-1")) {
			sqlWhere += "T1.MOYSKBN IN ('1','2','3') ";
		} else {
			sqlWhere += "T1.MOYSKBN=? ";
			paramData.add(szMoysKbn);
		}

		if(StringUtils.isEmpty(szMoysStDt)){
			sqlWhere += "";
		}else{
			sqlWhere += " AND T1.MOYSSTDT>=? ";
			paramData.add(szMoysStDt);
		}

		// タイトル情報(任意)設定
		List<String> titleList = new ArrayList<String>();

		// 一覧表情報
		StringBuffer sbSQL = new StringBuffer();
		sbSQL.append(DefineReport.ID_SQL_CMN_WEEK);
		sbSQL.append(" SELECT ");
		sbSQL.append(" MOYSKBN || MOYSSTDT || right('000'||MOYSRBAN, 3) AS MOYSCD ");
		sbSQL.append(",CASE ");
		sbSQL.append("WHEN (HBSTDTD IS NULL OR HBSTDTD='') AND (HBEDDTD IS NULL OR HBEDDTD='') THEN '' ");
		sbSQL.append("WHEN HBSTDTD = HBEDDTD THEN HBSTDTD || HBSTDTW ");
		sbSQL.append("ELSE HBSTDTD || HBSTDTW || '～' || HBEDDTD || HBEDDTW END AS HBPERIOD ");
		sbSQL.append(",CASE  ");
		sbSQL.append("WHEN (NNSTDTD IS NULL OR NNSTDTD='') AND (NNEDDTD IS NULL OR NNEDDTD='') THEN '' ");
		sbSQL.append("WHEN NNSTDTD = NNEDDTD THEN NNSTDTD || NNSTDTW ");
		sbSQL.append("ELSE NNSTDTD || NNSTDTW || '～' || NNEDDTD || NNEDDTW END AS NNPERIOD ");
		sbSQL.append(",MOYKN ");
		sbSQL.append(",MOYSKBN ");
		sbSQL.append(",MOYSSTDT ");
		sbSQL.append(",MOYSRBAN ");
		sbSQL.append("FROM ");
		sbSQL.append("(SELECT DISTINCT ");
		sbSQL.append(" T1.MOYSKBN ");
		sbSQL.append(",T1.MOYSSTDT ");
		sbSQL.append(",T1.MOYSRBAN ");
		sbSQL.append(",T1.MOYKN ");
		sbSQL.append(",TO_CHAR(TO_DATE(T1.HBSTDT,'YYYYMMDD'),'YY/MM/DD') AS HBSTDTD ");
		sbSQL.append(",TO_CHAR(TO_DATE(T1.HBEDDT,'YYYYMMDD'),'YY/MM/DD') AS HBEDDTD ");
		sbSQL.append(",TO_CHAR(TO_DATE(T1.NNSTDT,'YYYYMMDD'),'YY/MM/DD') AS NNSTDTD ");
		sbSQL.append(",TO_CHAR(TO_DATE(T1.NNEDDT,'YYYYMMDD'),'YY/MM/DD') AS NNEDDTD ");
		sbSQL.append(",(select JWEEK from WEEK where CWEEK=DAYOFWEEK(TO_DATE(T1.HBSTDT,'YYYYMMDD'))) HBSTDTW");
		sbSQL.append(",(select JWEEK from WEEK where CWEEK=DAYOFWEEK(TO_DATE(T1.HBEDDT,'YYYYMMDD'))) HBEDDTW");
		sbSQL.append(",(select JWEEK from WEEK where CWEEK=DAYOFWEEK(TO_DATE(T1.NNSTDT,'YYYYMMDD'))) NNSTDTW");
		sbSQL.append(",(select JWEEK from WEEK where CWEEK=DAYOFWEEK(TO_DATE(T1.NNEDDT,'YYYYMMDD'))) NNEDDTW ");
		sbSQL.append("FROM ");
		sbSQL.append(" INATK.TOKMOYCD T1 INNER JOIN INATK.TOKMM_KKK T2 ON T1.MOYSKBN=T2.MOYSKBN AND T1.MOYSSTDT=T2.MOYSSTDT AND T1.MOYSRBAN=T2.MOYSRBAN AND T2.UPDKBN="+DefineReport.ValUpdkbn.NML.getVal());
		sbSQL.append(" WHERE " + sqlWhere + " AND T1.UPDKBN="+DefineReport.ValUpdkbn.NML.getVal());
		sbSQL.append(" ORDER BY ");
		sbSQL.append(" 	T1.MOYSKBN ");
		sbSQL.append(",T1.MOYSSTDT ");
		sbSQL.append(",T1.MOYSRBAN)");

		// オプション情報（タイトル）設定
		JSONObject option = new JSONObject();
		option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
		setOption(option);

		// DB検索用パラメータ設定
		setParamData(paramData);

		if (DefineReport.ID_DEBUG_MODE) System.out.println(getClass().getSimpleName()+"[sql]"+sbSQL.toString());
		return sbSQL.toString();
	}

	private void outputQueryList() {

		// 検索条件の加工クラス作成
		JsonArrayData jad = new JsonArrayData();
		jad.setJsonString(getJson());

	}
}
