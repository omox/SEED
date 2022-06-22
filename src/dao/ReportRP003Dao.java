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
public class ReportRP003Dao extends ItemDao {

	/**
	 * インスタンスを生成します。
	 * @param source
	 */
	public ReportRP003Dao(String JNDIname) {
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

		String szBmncd			 = getMap().get("BMNCD");				// 部門
		String szRinji			 = getMap().get("RINJI");				// 臨時
		String szMoyscd			 = getMap().get("MOYSCD");				// 催しコード

		// タイトル情報(任意)設定
		List<String> titleList = new ArrayList<String>();

		// DB検索用パラメータ
		ArrayList<String> paramData = new ArrayList<String>();

		// 一覧表情報
		StringBuffer sbSQL = new StringBuffer();

		if (StringUtils.equals("1", szRinji)) {
			// 臨時数量パターンテーブルを検索
			sbSQL.append(" select");
//			sbSQL.append(" SRYPTNNO");						// F1 : 数量パターンNo.
			sbSQL.append(" right('000'|| SRYPTNNO,3)");		// F1 : 数量パターンNo.
			sbSQL.append(", SRYPTNKN");						// F2 : 数量パターン名称
			sbSQL.append(" from INATK.TOKSRPTNEX");
			sbSQL.append(" where UPDKBN = 0");
			sbSQL.append(" and BMNCD = ? ");
			paramData.add(szBmncd);
			sbSQL.append(" and MOYSKBN = ? ");
			paramData.add(StringUtils.substring(szMoyscd, 0, 1));
			sbSQL.append(" and MOYSSTDT = ? " );
			paramData.add(StringUtils.substring(szMoyscd, 1, 7));
			sbSQL.append(" and MOYSRBAN = ? " );
			paramData.add(StringUtils.substring(szMoyscd, 7, 10));
			sbSQL.append(" order by SRYPTNNO");

		} else {
			// 数量パターンテーブルを検索
			sbSQL.append(" select");
//			sbSQL.append(" SRYPTNNO");						// F1 : 数量パターンNo.
			sbSQL.append(" right('000'|| SRYPTNNO,3)");		// F1 : 数量パターンNo.
			sbSQL.append(", SRYPTNKN");						// F2 : 数量パターン名称
			sbSQL.append(" from INATK.TOKSRPTN");
			sbSQL.append(" where UPDKBN = 0");
			sbSQL.append(" and BMNCD = ? ");
			paramData.add(szBmncd);
			sbSQL.append(" order by SRYPTNNO");
		}
		// DB検索用パラメータ設定
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

	}
}
