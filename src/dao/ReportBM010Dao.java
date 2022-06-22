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
public class ReportBM010Dao extends ItemDao {

	/** SQLリスト保持用変数 */
	ArrayList<String> sqlList = new ArrayList<String>();
	/** SQLのパラメータリスト保持用変数 */
	ArrayList<ArrayList<String>> prmList = new ArrayList<ArrayList<String>>();
	/** SQLログ用のラベルリスト保持用変数 */
	ArrayList<String> lblList = new ArrayList<String>();

	/**
	 * インスタンスを生成します。
	 * @param source
	 */
	public ReportBM010Dao(String JNDIname) {
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

		// DB検索用パラメータ
		String szMoysKbn	= getMap().get("MOYSKBN");	// 催し区分
		String szMoysStDt	= getMap().get("MOYSSTDT");	// 催し販売開始日
		String szMoysRban	= getMap().get("MOYSRBAN");	// 催し連番

		ArrayList<String> paramData = new ArrayList<String>();
		String sqlWhere = "";

		if(StringUtils.isEmpty(szMoysKbn)){
			sqlWhere += "MY.MOYSKBN=null AND ";
		}else{
			sqlWhere += "MY.MOYSKBN=? AND ";
			paramData.add(szMoysKbn);
		}

		if(StringUtils.isEmpty(szMoysStDt)){
			sqlWhere += "MY.MOYSSTDT=null AND ";
		}else{
			sqlWhere += "MY.MOYSSTDT=? AND ";
			paramData.add(szMoysStDt);
		}

		if(StringUtils.isEmpty(szMoysRban)){
			sqlWhere += "MY.MOYSRBAN=null ";
		}else{
			sqlWhere += "MY.MOYSRBAN=? ";
			paramData.add(szMoysRban);
		}

		// タイトル情報(任意)設定
		List<String> titleList = new ArrayList<String>();

		// 前画面から引き継いだ配送グループコードで検索
		StringBuffer sbSQL = new StringBuffer();
		sbSQL.append("SELECT ");
		sbSQL.append("MY.MOYSKBN || MY.MOYSSTDT || right('000'||MY.MOYSRBAN, 3) AS MOYSCD ");	// F1	: 催しコード
		sbSQL.append(",MY.MOYSKBN ");															// F2	: 催し区分
		sbSQL.append(",MY.MOYSSTDT ");															// F3	: 催し開始日
		sbSQL.append(",MY.MOYSRBAN ");															// F4	: 催し連番
		sbSQL.append(",CASE WHEN BM.MOYSKBN IS NULL THEN '0' ELSE '1' END AS MOYSKBN_CHECK ");	// F5	: データ存在有無
		sbSQL.append(",MY.OPERATOR ");															// F6	: オペレーター
		sbSQL.append(",TO_CHAR(MY.ADDDT,'YY/MM/DD') AS ADDDT ");								// F7	: 登録日
		sbSQL.append(",TO_CHAR(MY.UPDDT,'YY/MM/DD') AS UPDDT ");								// F8	: 更新日
		sbSQL.append(",TO_CHAR(MY.UPDDT,'YYYYMMDDHH24MISSNNNNNN') as HDN_UPDDT ");				// F9	: 更新日時
		sbSQL.append("FROM ");
		sbSQL.append("INATK.TOKMOYCD AS MY LEFT JOIN INATK.TOKBM AS BM ON ");
		sbSQL.append("MY.MOYSKBN=BM.MOYSKBN AND MY.MOYSSTDT=BM.MOYSSTDT AND MY.MOYSRBAN=BM.MOYSRBAN ");
		sbSQL.append("WHERE ");
		sbSQL.append(sqlWhere);
		sbSQL.append("ORDER BY ");
		sbSQL.append("MOYSKBN ");
		sbSQL.append(",MOYSSTDT ");
		sbSQL.append(",MOYSRBAN ");

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
