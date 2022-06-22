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
public class Reportx203Dao extends ItemDao {

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
	public Reportx203Dao(String JNDIname) {
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
		String szHsgpcd = getMap().get("HSGPCD"); // 配送グループコード
		ArrayList<String> paramData = new ArrayList<String>();
		String sqlWhere = "";

		if(StringUtils.isEmpty(szHsgpcd)){
			sqlWhere += "null";
		}else{
			sqlWhere += "?";
			paramData.add(szHsgpcd);
		}

		// タイトル情報(任意)設定
		List<String> titleList = new ArrayList<String>();

		// 前画面から引き継いだ配送グループコードで検索
		StringBuffer sbSQL = new StringBuffer();
		sbSQL.append("SELECT ");
		sbSQL.append("HSGPCD ");													// F1	: 配送グループ
		sbSQL.append(",HSGPKN ");													// F2	: 配送グループ名称
		sbSQL.append(",OPERATOR ");													// F3	: オペレーター
		sbSQL.append(",TO_CHAR(ADDDT,'YY/MM/DD') AS ADDDT ");						// F4	: 登録日
		sbSQL.append(",TO_CHAR(UPDDT,'YY/MM/DD') AS UPDDT ");						// F5	: 更新日
		sbSQL.append(",TO_CHAR(UPDDT,'YYYYMMDDHH24MISSNNNNNN') as HDN_UPDDT ");		// F6	: 更新日時
		sbSQL.append(",AREAKBN ");													// F7	: エリア区分
		sbSQL.append("FROM ");
		sbSQL.append("INAMS.MSTHSGP ");
		sbSQL.append("WHERE ");
		sbSQL.append("HSGPCD="+ sqlWhere +" AND ");
		sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal() + " ");
		sbSQL.append("ORDER BY HSGPCD");

		// オプション情報（タイトル）設定
		JSONObject option = new JSONObject();
		option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
		setOption(option);

		// DB検索用パラメータ設定
		setParamData(paramData);

		if (DefineReport.ID_DEBUG_MODE)	System.out.println(getClass().getSimpleName()+"[sql]"+sbSQL.toString());
		return sbSQL.toString();
	}

	private void outputQueryList() {

		// 検索条件の加工クラス作成
		JsonArrayData jad = new JsonArrayData();
		jad.setJsonString(getJson());

	}
}
