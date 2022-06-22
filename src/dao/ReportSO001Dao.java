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
public class ReportSO001Dao extends ItemDao {

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
	public ReportSO001Dao(String JNDIname) {
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

	/**
	 * 検索実行
	 *
	 * @return
	 */
	public boolean selectForDL() {

		// 検索コマンド生成
		String command = createCommandForDl();

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

		String moysstdt	 = getMap().get("MOYSSTDT"); 				// 催し開始日
		String bmncd	 = getMap().get("BMNCD"); 					// 部門

		// タイトル情報(任意)設定
		List<String> titleList = new ArrayList<String>();
		ArrayList<String> paramData = new ArrayList<String>();
		// 一覧表情報
		StringBuffer sbSQL = new StringBuffer();

		sbSQL.append(DefineReport.ID_SQL_CMN_WEEK);
		sbSQL.append(" select");
		sbSQL.append(" T1.MOYSCD");															// F1 : 企画NO
		sbSQL.append(", T1.MOYKN");															// F2 : 催し名称
		sbSQL.append(", T1.HBSTDT || W1.JWEEK || '～' || T1.HBEDDT || W2.JWEEK");			// F3 : 催し期間
		sbSQL.append(", T1.COUNT");															// F4 : 登録件数
		sbSQL.append(", T1.MOYSKBN");														// F5 : 非表示：催し区分
		sbSQL.append(", T1.MOYSSTDT");														// F6 : 非表示：催し開始日
		sbSQL.append(", T1.MOYSRBAN");														// F7 : 非表示：催し連番
		sbSQL.append(" from (select");
		sbSQL.append(" MYCD.MOYSKBN || '-' || MYCD.MOYSSTDT || '-' || right('000'||MYCD.MOYSRBAN, 3) as MOYSCD");
		sbSQL.append(", MAX(MYCD.MOYKN) as MOYKN");
		sbSQL.append(", MYCD.MOYSKBN");
		sbSQL.append(", MYCD.MOYSSTDT");
		sbSQL.append(", MYCD.MOYSRBAN");
		sbSQL.append(", TO_CHAR(TO_DATE(MAX(MYCD.HBSTDT), 'YYYYMMDD'), 'YY/MM/DD') as HBSTDT");
		sbSQL.append(", DAYOFWEEK(TO_DATE(MAX(MYCD.HBSTDT), 'YYYYMMDD')) as HBSTDT_WNUM");
		sbSQL.append(", TO_CHAR(TO_DATE(MAX(MYCD.HBEDDT), 'YYYYMMDD'), 'YY/MM/DD') as HBEDDT");
		sbSQL.append(", DAYOFWEEK(TO_DATE(MAX(MYCD.HBEDDT), 'YYYYMMDD')) as HBEDDT_WNUM");
		sbSQL.append(", case when SUM(SHN.COUNT) is null then 0 else SUM(SHN.COUNT) end COUNT ");
		sbSQL.append(" from INATK.TOKMOYCD MYCD");
		if(!StringUtils.equals(DefineReport.Values.NONE.getVal(), bmncd)){
			sbSQL.append(" left join INATK.TOKSO_BMN BMN on BMN.MOYSKBN = MYCD.MOYSKBN and BMN.MOYSSTDT = MYCD.MOYSSTDT and BMN.MOYSRBAN = MYCD.MOYSRBAN and BMN.UPDKBN = MYCD.UPDKBN");
			sbSQL.append(" and BMN.BMNCD = ? ");
			paramData.add(bmncd);
		}
		sbSQL.append(" left join (select MOYSKBN, MOYSSTDT, MOYSRBAN, BMNCD, COUNT(NVL(SHNCD,0)) as COUNT from INATK.TOKSO_SHN where NVL(UPDKBN, 0) <> 1 group by MOYSKBN, MOYSSTDT, MOYSRBAN, BMNCD) SHN on SHN.MOYSKBN = MYCD.MOYSKBN and SHN.MOYSSTDT = MYCD.MOYSSTDT and SHN.MOYSRBAN = MYCD.MOYSRBAN");
		if(!StringUtils.equals(DefineReport.Values.NONE.getVal(), bmncd)){
			sbSQL.append("  and SHN.BMNCD = BMN.BMNCD ");
		}
		sbSQL.append(" where MYCD.MOYSKBN = 5 and MYCD.MOYSSTDT >= ? and NVL(MYCD.UPDKBN, 0) <> 1 ");
		paramData.add(moysstdt);
		sbSQL.append(" group by MYCD.MOYSKBN, MYCD.MOYSSTDT, MYCD.MOYSRBAN) T1");
		sbSQL.append(" left outer join WEEK W1 on T1.HBSTDT_WNUM = W1.CWEEK");
		sbSQL.append(" left outer join WEEK W2 on T1.HBEDDT_WNUM = W2.CWEEK");
		sbSQL.append(" order by T1.MOYSCD");
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

	private String createCommandForDl() {
		// ログインユーザー情報取得
		User userInfo = getUserInfo();
		if(userInfo==null){
			return "";
		}

		String btnId = getMap().get("BTN");			// 実行ボタン

		// パラメータ確認
		// 必須チェック
		if ((btnId == null)) {
			System.out.println(super.getConditionLog());
			return "";
		}

		// DB検索用パラメータ
		ArrayList<String> paramData = new ArrayList<String>();

		String moysstdt	 = getMap().get("MOYSSTDT"); 				// 催し開始日
		String bmncd	 = getMap().get("BMNCD"); 					// 部門

		// 一覧表情報
		StringBuffer sbSQL = new StringBuffer();
		sbSQL.append(DefineReport.ID_SQL_CMN_WEEK);

		sbSQL.append(" select distinct");
		sbSQL.append(" MYCD.MOYSKBN || MYCD.MOYSSTDT || right ('000' || MYCD.MOYSRBAN, 3) as MOYSCD");
		sbSQL.append(", MYCD.MOYKN");
		sbSQL.append(", MYCD.MOYSKBN");
		sbSQL.append(", MYCD.MOYSSTDT");
		sbSQL.append(", MYCD.HBSTDT");
		sbSQL.append(", MYCD.HBSTDT");
		sbSQL.append(", ' '");
		sbSQL.append(", '0D0A'");
		sbSQL.append(" from INATK.TOKMOYCD MYCD");
		sbSQL.append(" inner join INATK.TOKSO_BMN BMN on BMN.MOYSKBN = MYCD.MOYSKBN and BMN.MOYSSTDT = MYCD.MOYSSTDT and BMN.MOYSRBAN = MYCD.MOYSRBAN and BMN.UPDKBN = MYCD.UPDKBN");
		sbSQL.append(" inner join (select");
		sbSQL.append("  MOYSKBN");
		sbSQL.append(" , MOYSSTDT");
		sbSQL.append(" , MOYSRBAN");
		sbSQL.append(" , BMNCD");
		sbSQL.append("  from INATK.TOKSO_SHN where NVL(UPDKBN, 0) <> 1");
		sbSQL.append("  group by MOYSKBN, MOYSSTDT, MOYSRBAN, BMNCD");
		sbSQL.append(") SHN on SHN.MOYSKBN = MYCD.MOYSKBN and SHN.MOYSSTDT = MYCD.MOYSSTDT and SHN.MOYSRBAN = MYCD.MOYSRBAN and SHN.BMNCD = BMN.BMNCD");
		sbSQL.append(" where NVL(BMN.UPDKBN, 0) <> 1");
		sbSQL.append(" and MYCD.MOYSKBN = 5");
		sbSQL.append(" and BMN.MOYSSTDT = "+moysstdt);

		if(!StringUtils.equals(DefineReport.Values.NONE.getVal(), bmncd)){
			sbSQL.append(" and BMN.BMNCD = "+bmncd);
		}

		// DB検索用パラメータ設定
		setParamData(paramData);

		if (DefineReport.ID_DEBUG_MODE) System.out.println(getClass().getSimpleName()+"[sql]"+sbSQL.toString());
		return sbSQL.toString();
	}
}
