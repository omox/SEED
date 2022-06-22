package dao;

import java.util.ArrayList;
import java.util.List;

import authentication.bean.User;
import common.DefineReport;
import common.JsonArrayData;
import net.sf.json.JSONObject;

/**
 *
 */
public class ReportSO006Dao extends ItemDao {

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
	public ReportSO006Dao(String JNDIname) {
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

		// タイトル情報(任意)設定
		List<String> titleList = new ArrayList<String>();

		// 一覧表情報
		StringBuffer sbSQL = new StringBuffer();

		sbSQL.append(" select");
		sbSQL.append(" CSVH.OPERATOR");															// F1 : オペレータコード
		sbSQL.append(", TO_CHAR(CSVH.INPUT_DATE, 'YY/MM/DD')");									// F2 : 処理日
		sbSQL.append(", TO_CHAR(CSVH.INPUT_DATE, 'HH24:MI') as HDN_UPDDT");						// F3 : 時刻
		sbSQL.append(", CSVH.COMMENTKN");														// F4 : コメント
		sbSQL.append(", CSVH.SEQ");																// F5 : 非表示(SEQ)
		sbSQL.append(" from INATK.CSVTOKHEAD CSVH");
		sbSQL.append(" inner join (");
		sbSQL.append("  select CSO.SEQ");
		sbSQL.append("  from INATK.CSVTOK_SO CSO");
		sbSQL.append("  inner join INATK.TOKMOYCD MYCD on CSO.MOYSKBN = MYCD.MOYSKBN and CSO.MOYSSTDT = MYCD.MOYSSTDT and CSO.MOYSRBAN = MYCD.MOYSRBAN");
		sbSQL.append(" where NVL(CSO.UPDKBN, 0) <> 1 group by CSO.SEQ) CSVS on CSVS.SEQ = CSVH.SEQ ");
		sbSQL.append(" order by CSVH.SEQ");

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

	}
}
