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
public class ReportJU027Dao extends ItemDao {

	/**
	 * インスタンスを生成します。
	 * @param source
	 */
	public ReportJU027Dao(String JNDIname) {
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

		// DB検索用パラメータ
		String sqlWhere	= "";
		ArrayList<String> paramData = new ArrayList<String>();

		// ログインユーザー情報取得
		User userInfo = getUserInfo();
		if(userInfo==null){
			return "";
		}

		String szMoysstdt = getMap().get("MOYSSTDT");			// 催しコード

		// タイトル情報(任意)設定
		List<String> titleList = new ArrayList<String>();

		// 催し開始日
		if (StringUtils.isEmpty(szMoysstdt)) {
			sqlWhere += "T1.MOYSSTDT=null ";
		} else {
			sqlWhere += "T1.MOYSSTDT >=? ";
			paramData.add(szMoysstdt);
		}

		// 一覧表情報
		StringBuffer sbSQL = new StringBuffer();
		sbSQL.append(DefineReport.ID_SQL_CMN_WEEK);
		sbSQL.append("select");
		sbSQL.append(" T1.MOYSKBN||'-'||right(T1.MOYSSTDT, 6)||'-'||right('000'||T1.MOYSRBAN, 3) as 催しコード");//F1催しコード
		sbSQL.append(",T1.MOYKN as 催し名称");//F2催し名称
		sbSQL.append(",TO_CHAR(TO_DATE(T1.NNSTDT, 'YYYYMMDD'), 'YY/MM/DD')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(TO_DATE(T1.NNSTDT, 'YYYYMMDD')))    ||'～'||    TO_CHAR(TO_DATE(T1.NNEDDT, 'YYYYMMDD'), 'YY/MM/DD')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(TO_DATE(T1.NNEDDT, 'YYYYMMDD'))) as 納入期間");//F3納入期間
		sbSQL.append(",TO_CHAR(TO_DATE(T2.QASMDT, 'YYYYMMDD'), 'YY/MM/DD')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(TO_DATE(T2.QASMDT, 'YYYYMMDD'))) as 店舗締切日");	// F4店舗締切日
		sbSQL.append(",count(T3.KANRINO) as 商品数");	// F5商品数
		sbSQL.append(",CONCAT(CONCAT(T1.MOYSKBN, right(T1.MOYSSTDT, 6)), right('000'||T1.MOYSRBAN, 3)) as 催しコード");// F6催しコード
		sbSQL.append(" from INATK.TOKMOYCD T1");
		sbSQL.append(",INATK.TOKQJU_MOY T2 LEFT JOIN INATK.TOKQJU_SHN T3 ON ");
		sbSQL.append(" T3.MOYSKBN=T2.MOYSKBN");
		sbSQL.append(" and T3.MOYSSTDT=T2.MOYSSTDT");
		sbSQL.append(" and T3.MOYSRBAN=T2.MOYSRBAN");
		sbSQL.append(" and T3.UPDKBN="+DefineReport.ValUpdkbn.NML.getVal());
		sbSQL.append(" where ");
		sbSQL.append(sqlWhere);
		sbSQL.append(" and T2.MOYSKBN=T1.MOYSKBN");
		sbSQL.append(" and T2.MOYSSTDT=T1.MOYSSTDT");
		sbSQL.append(" and T2.MOYSRBAN=T1.MOYSRBAN");
		sbSQL.append(" and T1.UPDKBN="+DefineReport.ValUpdkbn.NML.getVal());
		sbSQL.append(" and T2.UPDKBN="+DefineReport.ValUpdkbn.NML.getVal());
		sbSQL.append(" group by T1.MOYSKBN");
		sbSQL.append(",T1.MOYSSTDT");
		sbSQL.append(",T1.MOYSRBAN");
		sbSQL.append(",T1.MOYKN");
		sbSQL.append(",T1.NNSTDT");
		sbSQL.append(",T1.NNEDDT");
		sbSQL.append(",T2.QASMDT");

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

		// 保存用 List (検索情報)作成
		setWhere(new ArrayList<List<String>>());
	}
}
