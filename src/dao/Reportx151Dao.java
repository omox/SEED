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
public class Reportx151Dao extends ItemDao {

	private static String DECI_DIGITS = ",20,10";

	/**
	 * インスタンスを生成します。
	 * @param source
	 */
	public Reportx151Dao(String JNDIname) {
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

		// DB検索用パラメータ
		ArrayList<String> paramData = new ArrayList<String>();

		// 一覧表情報
		StringBuffer sbSQL = new StringBuffer();

		String szSircd			 = getMap().get("SIRCD");				// 仕入先コード
		String szSirkn			 = getMap().get("SIRKN");				// 仕入先名称
		String szShiiresakiyoto	 = getMap().get("SHIIRESAKIYOTO");		// 仕入先用途
		String szInageyazaiko	 = getMap().get("INAGEYAZAIKO");		// いなげや在庫

		String sqlWhere = "";

		if(!StringUtils.isEmpty(szSircd)){
			sqlWhere += " and SIR.SIRCD >= ?";
			paramData.add(szSircd);
		}
		if(!StringUtils.isEmpty(szSirkn)){
			sqlWhere += " and SIR.SIRKN like ?";
			paramData.add('%'+szSirkn+'%');
		}
		if(!StringUtils.equals(DefineReport.Values.NONE.getVal(), szShiiresakiyoto)){
			sqlWhere += " and SIR.SIRYOTOKBN = ?";
			paramData.add(szShiiresakiyoto);
		}
		if(!StringUtils.equals(DefineReport.Values.NONE.getVal(), szInageyazaiko)){
			sqlWhere += " and SIR.INAZAIKOKBN = ?";
			paramData.add(szInageyazaiko);
		}

		sbSQL.append(" select");
		sbSQL.append(" right('00000'|| SIR.SIRCD,6)");											// F1	：仕入先コード
		sbSQL.append(", SIR.SIRKN");															// F2	：仕入先名称
		sbSQL.append(", TO_CHAR(SIR.ADDDT, 'yy/mm/dd')");										// F3	：登録日
		sbSQL.append(", TO_CHAR(SIR.UPDDT, 'yy/mm/dd')");										// F4	：更新日
		sbSQL.append(" from INAMS.MSTSIR SIR");
		sbSQL.append(" where NVL(SIR.UPDKBN, 0) = 0");
		if(!StringUtils.isEmpty(sqlWhere)){
			sbSQL.append(sqlWhere);
		}
		sbSQL.append(" order by SIRCD");

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
