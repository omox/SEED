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
public class Reportx191Dao extends ItemDao {

	private static String DECI_DIGITS = ",20,10";

	/**
	 * インスタンスを生成します。
	 * @param source
	 */
	public Reportx191Dao(String JNDIname) {
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
		String szGpkbn	 = getMap().get("GPKBN");				// グループ区分
		String szBumon	 = getMap().get("BUMON");				// 部門

		String sqlWhere = "";

		// タイトル情報(任意)設定
		List<String> titleList = new ArrayList<String>();

		// 一覧表情報
		StringBuffer sbSQL = new StringBuffer();

		if(!StringUtils.equals(DefineReport.Values.NONE.getVal(), szGpkbn)){
			sqlWhere += " and STG.GPKBN = ? ";
			paramData.add(szGpkbn);
		}
		if(!StringUtils.equals(DefineReport.Values.NONE.getVal(), szBumon)){
			sqlWhere += " and STG.BMNCD = ? ";
			paramData.add(szBumon);
		}

		sbSQL.append("select");

		sbSQL.append(" case");												// F1 : 店グループ
		sbSQL.append(" when STG.GPKBN = 1 then right ('00' || STG.TENGPCD, 2)");
		sbSQL.append(" else right ('0000' || STG.TENGPCD, 4) end ");
		//sbSQL.append(" right ('0000' || STG.TENGPCD, 4)");
		sbSQL.append(", MAX(STG.TENGPKN)");									// F2 : 店グループ名称
		sbSQL.append(", case");												// F3 : 店舗数
		sbSQL.append("  when STG.AREAKBN = 0 then null");
		sbSQL.append(" when STG.GPKBN <> 1 then COUNT(STGT.TENCD) end");
		sbSQL.append(", TO_CHAR(MAX(STG.ADDDT), 'yy/mm/dd')");				// F4 : 登録日
		sbSQL.append(", TO_CHAR(MAX(STG.UPDDT), 'yy/mm/dd')");				// F5 : 更新日
		sbSQL.append(", STG.GPKBN");										// F6 : (非表示)グループ区分
		sbSQL.append(",right('0'||STG.BMNCD,2)");							// F7 : (非表示)部門
		sbSQL.append(", STG.AREAKBN");										// F8 : (非表示)エリア区分
		sbSQL.append(" from INAMS.MSTSHNTENGP STG");
		sbSQL.append(" left join INAMS.MSTSHNTENGPTEN STGT on STGT.GPKBN = STG.GPKBN and STGT.BMNCD = STG.BMNCD and STGT.TENGPCD = STG.TENGPCD");
		// TODO
		sbSQL.append(" where STG.UPDKBN = "+DefineReport.ValUpdkbn.NML.getVal());
		if(!StringUtils.isEmpty(sqlWhere)){
			sbSQL.append(sqlWhere);
		}
		sbSQL.append(" group by STG.GPKBN, STG.BMNCD, STG.AREAKBN, STG.TENGPCD");
		sbSQL.append(" order by STG.GPKBN, STG.BMNCD, STG.TENGPCD");
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
