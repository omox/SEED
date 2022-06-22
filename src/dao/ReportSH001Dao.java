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
public class ReportSH001Dao extends ItemDao {

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
	public ReportSH001Dao(String JNDIname) {
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

		String szShncd			 = getMap().get("SHNCD"); 	// 商品コード
		String szShnkn			 = getMap().get("SHNKN"); 		// 商品名称
		String szSsircd			 = getMap().get("SSIRCD");	 	// 仕入先コード
		String szMaker			 = getMap().get("MAKER"); 		// メーカーコード
		String szBumon			 = getMap().get("BUMON"); 		// 部門
		String szDaibun			 = getMap().get("DAICD"); 		// 大分類
		String szChubun			 = getMap().get("CHUCD"); 		// 中分類
		String kbn121			 = getMap().get("TEIKANKBN");	// 住所不定区分;
		String kbn117			 = getMap().get("TEIKEIKBN");	// 定形区分;
		String kbn105			 = getMap().get("SHNKBN");		// 商品種類;

		String sqlWhere = "";

		// タイトル情報(任意)設定
		List<String> titleList = new ArrayList<String>();

		// 一覧表情報
		StringBuffer sbSQL = new StringBuffer();

		sbSQL.append(DefineReport.ID_SQL_SHN_SHNCD);

		sqlWhere = DefineReport.ID_SQL_CMN_WHERE;
		if(StringUtils.isNotEmpty(szShncd)){
			sqlWhere += " and SHNCD  >= '" + szShncd +"'";
		}
		if(StringUtils.isNotEmpty(szBumon)){
			sqlWhere += " and BMNCD = '" + szBumon +"'";
		}
		if(StringUtils.isNotEmpty(szDaibun)){
			sqlWhere += " and DAICD = '" + szDaibun +"'";
		}
		if(StringUtils.isNotEmpty(szChubun)){
			sqlWhere += " and CHUCD = '" + szChubun +"'";
		}
		if(StringUtils.isNotEmpty(szShnkn)){
			sqlWhere += " and SHNKN like '%" + szShnkn +"%'";
		}
		if(StringUtils.isNotEmpty(szSsircd)){
			sqlWhere += " and SSIRCD = '" + szSsircd +"'";
		}
		if(StringUtils.isNotEmpty(szMaker)){
			sqlWhere += " and MAKERCD = '" + szMaker +"'";
		}
		if(!StringUtils.equals(DefineReport.Values.NONE.getVal(), kbn121) && StringUtils.isNotEmpty(kbn121)){
			sqlWhere += " and TEIKANKBN = '" + kbn121 +"'";
		}
		if(!StringUtils.equals(DefineReport.Values.NONE.getVal(), kbn117) && StringUtils.isNotEmpty(kbn117)){
			sqlWhere += " and TEIKEIKBN = '" + kbn117 +"'";
		}
		if(!StringUtils.equals(DefineReport.Values.NONE.getVal(), kbn105) && StringUtils.isNotEmpty(kbn105)){
			sqlWhere += " and SHNKBN = '" + kbn105 +"'";
		}
		sbSQL.append(sqlWhere);
		sbSQL.append(DefineReport.ID_SQL_SHN_SHNCD_TAIL);

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
