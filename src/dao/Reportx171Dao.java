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
public class Reportx171Dao extends ItemDao {

	/**
	 * インスタンスを生成します。
	 * @param source
	 */
	public Reportx171Dao(String JNDIname) {
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
		// タイトル情報(任意)設定
		List<String> titleList = new ArrayList<String>();

		// 一覧表情報
		StringBuffer sbSQL = new StringBuffer();

		String szHsptn			 = getMap().get("HSPTN");				// 配送先パターン

		String sqlWhere = "";

		if(!StringUtils.isEmpty(szHsptn)){
			sqlWhere += "  and HPTN.HSPTN >= ?";
			paramData.add(szHsptn);
		}

		sbSQL.append(" select");
		sbSQL.append(" right ('000' || HPTN.HSPTN, 3)");													// F1	：配送パターン
		sbSQL.append(", HPTN.HSPTNKN");													// F2	：配送パターン名称
		sbSQL.append(", right ('000' || HPTN.CENTERCD, 3)");							// F3	：センターコード
		sbSQL.append(", right ('000' || EHPTN.CENTERCD, 3)");							// F4	：横持先センターコード
		sbSQL.append(" from INAMS.MSTHSPTN HPTN");
		sbSQL.append(" left join INAMS.MSTAREAHSPTN EHPTN on HPTN.HSPTN = EHPTN.HSPTN");
		sbSQL.append(" where HPTN.UPDKBN = 0");
		if(!StringUtils.isEmpty(sqlWhere)){
			sbSQL.append(sqlWhere);
		}
		sbSQL.append(" order by HPTN.HSPTN");
		setParamData(paramData);
		// オプション情報（タイトル）設定
		JSONObject option = new JSONObject();
		option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
		setOption(option);

		if (DefineReport.ID_DEBUG_MODE)	System.out.println(getClass().getSimpleName()+"[sql]"+sbSQL.toString());
		return sbSQL.toString();
	}

	private void outputQueryList() {

		// 検索条件の加工クラス作成
		JsonArrayData jad = new JsonArrayData();
		jad.setJsonString(getJson());

	}
}
