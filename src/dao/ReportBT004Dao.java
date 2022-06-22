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
public class ReportBT004Dao extends ItemDao {

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
	public ReportBT004Dao(String JNDIname) {
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

		String moyskbn	 = getMap().get("MOYSKBN"); 				// 催し区分
		String moysstdt	 = getMap().get("MOYSSTDT"); 				// 催し開始日

		// タイトル情報(任意)設定
		List<String> titleList = new ArrayList<String>();

		// 一覧表情報
		StringBuffer sbSQL = new StringBuffer();

		ArrayList<String> paramData = new ArrayList<String>();

		sbSQL.append(DefineReport.ID_SQL_CMN_WEEK);
		sbSQL.append(" select");
		sbSQL.append(" right('0000' || T1.KKKNO, 4) as KKKNO");											// F1 : 企画NO
		sbSQL.append(", T1.BTKN");																		// F2 : 分類割引名称
		sbSQL.append(", T1.HBSTDT_K || W1.JWEEK || '～' || T1.HBEDDT_K || W2.JWEEK");					// F3 : 販売期間
		sbSQL.append(", T1.MOYSKBN || '-' || T1.MOYSSTDT || '-' || right ('000' || T1.MOYSRBAN, 3)");	// F4 : 催しコード
		sbSQL.append(", T1.MOYKN");																		// F5 : 催し名称（漢字）
		sbSQL.append(", T1.HBSTDT_M || W3.JWEEK || '～' || T1.HBEDDT_M || W4.JWEEK");					// F6 : 催し期間
		sbSQL.append(", T1.MOYSKBN");																	// F7 : 催し区分
		sbSQL.append(", T1.MOYSSTDT");																	// F8 : サブ画面用パラメータ：催し開始日(非表示)
		sbSQL.append(", T1.MOYSRBAN");																	// F9 : サブ画面用パラメータ：催し連番(非表示)
		sbSQL.append(", T1.BMNCD");																		// F10 : サブ画面用パラメータ：部門(非表示)
		sbSQL.append(", T1.BMNKN");																		// F11 : サブ画面用パラメータ：部門名称（漢字）(非表示)
		sbSQL.append(" from (select");
		sbSQL.append(" KKK.KKKNO");
		sbSQL.append(", KKK.BTKN");
		sbSQL.append(", TO_CHAR(TO_DATE(KKK.HBSTDT, 'YYYYMMDD'), 'YY/MM/DD') as HBSTDT_K");
		sbSQL.append(", DAYOFWEEK(TO_DATE(KKK.HBSTDT, 'YYYYMMDD')) as HBSTDT_K_WNUM");
		sbSQL.append(", TO_CHAR(TO_DATE(KKK.HBEDDT, 'YYYYMMDD'), 'YY/MM/DD') as HBEDDT_K");
		sbSQL.append(", DAYOFWEEK(TO_DATE(KKK.HBEDDT, 'YYYYMMDD')) as HBEDDT_K_WNUM");
		sbSQL.append(", KKK.MOYSKBN");
		sbSQL.append(", KKK.MOYSSTDT");
		sbSQL.append(", KKK.MOYSRBAN");
		sbSQL.append(", MYCD.MOYKN");
		sbSQL.append(", KKK.BMNCD");
		sbSQL.append(", BMN.BMNKN");
		sbSQL.append(", TO_CHAR(TO_DATE(MYCD.HBSTDT, 'YYYYMMDD'), 'YY/MM/DD') as HBSTDT_M");
		sbSQL.append(", DAYOFWEEK(TO_DATE(MYCD.HBSTDT, 'YYYYMMDD')) as HBSTDT_M_WNUM");
		sbSQL.append(", TO_CHAR(TO_DATE(MYCD.HBEDDT, 'YYYYMMDD'), 'YY/MM/DD') as HBEDDT_M");
		sbSQL.append(", DAYOFWEEK(TO_DATE(MYCD.HBEDDT, 'YYYYMMDD')) as HBEDDT_M_WNUM");
		sbSQL.append(" from INATK.TOKBT_KKK KKK");
		sbSQL.append(" left join INATK.TOKMOYCD MYCD on MYCD.MOYSKBN = KKK.MOYSKBN and MYCD.MOYSSTDT = KKK.MOYSSTDT and MYCD.MOYSRBAN = KKK.MOYSRBAN");
		sbSQL.append(" left join INAMS.MSTBMN BMN on KKK.BMNCD = BMN.BMNCD");
		sbSQL.append(" where KKK.UPDKBN = "+DefineReport.ValUpdkbn.NML.getVal());
		if(!StringUtils.isEmpty(moyskbn)){
			sbSQL.append(" and KKK.MOYSKBN = ? ");
			paramData.add(moyskbn);
		}
		if(!StringUtils.isEmpty(moysstdt)){
			sbSQL.append(" and MYCD.HBSTDT = ? ");
			paramData.add(moysstdt);
		}
		sbSQL.append(") T1");
		sbSQL.append(" left outer join WEEK W1 on T1.HBSTDT_K_WNUM = W1.CWEEK");
		sbSQL.append(" left outer join WEEK W2 on T1.HBEDDT_K_WNUM = W2.CWEEK");
		sbSQL.append(" left outer join WEEK W3 on T1.HBSTDT_K_WNUM = W3.CWEEK");
		sbSQL.append(" left outer join WEEK W4 on T1.HBEDDT_K_WNUM = W4.CWEEK");
		sbSQL.append(" order by T1.KKKNO");

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
