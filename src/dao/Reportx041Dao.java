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
public class Reportx041Dao extends ItemDao {

	private static String DECI_DIGITS = ",20,10";

	/**
	 * インスタンスを生成します。
	 * @param source
	 */
	public Reportx041Dao(String JNDIname) {
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
		sbSQL.append(" right('00'||READTMPTN,3)");												// F1	：リードタイムパターン
		sbSQL.append(", READTMPTNKN");															// F2	：リードタイム名称
		sbSQL.append(", READTM_MON");															// F3	：リードタイム_月
		sbSQL.append(", READTM_TUE");															// F4	：リードタイム_火
		sbSQL.append(", READTM_WED");															// F5	：リードタイム_水
		sbSQL.append(", READTM_THU");															// F6	：リードタイム_木
		sbSQL.append(", READTM_FRI");															// F7	：リードタイム_金
		sbSQL.append(", READTM_SAT");															// F8	：リードタイム_土
		sbSQL.append(", READTM_SUN");															// F9	：リードタイム_日
		sbSQL.append(", OPERATOR");																// F10	：オペレータ
		sbSQL.append(", TO_CHAR(ADDDT, 'yy/mm/dd')");											// F11	：登録日
		sbSQL.append(", TO_CHAR(UPDDT, 'yy/mm/dd')");											// F12	：更新日
		sbSQL.append(" from INAMS.MSTREADTM");
		sbSQL.append(" where NVL(UPDKBN, 0) = 0");
		sbSQL.append(" order by READTMPTN");

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
