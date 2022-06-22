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
public class Reportx181Dao extends ItemDao {

	/**
	 * インスタンスを生成します。
	 *
	 * @param source
	 */
	public Reportx181Dao(String JNDIname) {
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
		if (userInfo == null) {
			return "";
		}

		String szTxtMeishokbnkn = getMap().get("TXT_MEISHOKBNKN"); // コード1
		String szTxtMeishokbn = getMap().get("TXT_MEISHOKBN"); // コード2

		String btnId = getMap().get("BTN"); // 実行ボタン
		// DB検索用パラメータ
		ArrayList<String> paramData = new ArrayList<String>();
		// パラメータ確認
		// 必須チェック
		if ((btnId == null)) {
			System.out.println(super.getConditionLog());
			return "";
		}

		// タイトル情報(任意)設定
		List<String> titleList = new ArrayList<String>();

		String sqlConditions = "";

		if (!StringUtils.isEmpty(szTxtMeishokbnkn) && !StringUtils.isEmpty(szTxtMeishokbn)) {
			sqlConditions = " where MMIS.MEISHOKBNKN LIKE ? and int(MEISHOKBN) >=?";
			paramData.add('%'+szTxtMeishokbnkn+'%');
			paramData.add(szTxtMeishokbn);
		} else if (!StringUtils.isEmpty(szTxtMeishokbnkn)) {
			sqlConditions = " where MMIS.MEISHOKBNKN LIKE ?";
			paramData.add('%'+szTxtMeishokbnkn+'%');
		} else if (!StringUtils.isEmpty(szTxtMeishokbn)) {
			sqlConditions = " where int(MMIS.MEISHOKBN) >=?";
			paramData.add(szTxtMeishokbn);
		}

		// 一覧表情報
		StringBuffer sbSQL = new StringBuffer();
		sbSQL.append("  select ");
		sbSQL.append("  MMIS.MEISHOKBN ,");
		sbSQL.append("  MMIS.MEISHOKBNKN ,");
		sbSQL.append("  MMIS.MEISHOCD ,");
		sbSQL.append("  MMIS.NMKN ,");
		sbSQL.append("  MMIS.TNMKN ,");
		sbSQL.append("  MMIS.NMAN ");
		sbSQL.append("  from ");
		sbSQL.append("  INAMS.MSTMEISHO MMIS ");
		sbSQL.append(sqlConditions);
		sbSQL.append("  order by LPAD(MMIS.MEISHOKBN,6,0) , LPAD(MMIS.MEISHOCD,6,0) ASC ");

		// オプション情報（タイトル）設定
		JSONObject option = new JSONObject();
		option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
		setOption(option);

		// DB検索用パラメータ設定
		setParamData(paramData);

		if (DefineReport.ID_DEBUG_MODE)	System.out.println(getClass().getSimpleName() + "[sql]" + sbSQL.toString());
		return sbSQL.toString();

	}

	private void outputQueryList() {

		// 検索条件の加工クラス作成
		JsonArrayData jad = new JsonArrayData();
		jad.setJsonString(getJson());

		// 保存用 List (検索情報)作成
		setWhere(new ArrayList<List<String>>());
		List<String> cells = new ArrayList<String>();

		// タイトル名称
		cells.add(jad.getJSONText(DefineReport.ID_HIDDEN_REPORT_NAME));
		cells.add("");
		cells.add(jad.getJSONText(DefineReport.ID_HIDDEN_REPORT_NAME));
		getWhere().add(cells);

		// 空白行
		cells = new ArrayList<String>();
		cells.add("");
		getWhere().add(cells);

		cells = new ArrayList<String>();
		cells.add("");
		cells.add("");
		cells.add(DefineReport.Select.KIKAN.getTxt());
		cells.add(jad.getJSONText(DefineReport.Select.KIKAN_F.getObj()));
		cells.add(DefineReport.Select.TENPO.getTxt());
		cells.add(jad.getJSONText(DefineReport.Select.TENPO.getObj()));
		cells.add(DefineReport.Select.BUMON.getTxt());
		cells.add(jad.getJSONText(DefineReport.Select.BUMON.getObj()));
		getWhere().add(cells);

		// 空白行
		cells = new ArrayList<String>();
		cells.add("");
		getWhere().add(cells);
	}
}
