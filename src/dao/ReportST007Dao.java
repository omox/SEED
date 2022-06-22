package dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import authentication.bean.User;
import common.DefineReport;
import common.Defines;
import common.ItemList;
import common.JsonArrayData;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 */
public class ReportST007Dao extends ItemDao {

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
	public ReportST007Dao(String JNDIname) {
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
	 * 他画面からの呼び出し検索実行
	 *
	 * @return
	 */
	public String createCommandSub(HashMap<String, String> map, User userInfo) {

		// ユーザー情報を設定
		super.setUserInfo(userInfo);

		// 検索条件などの情報を設定
		super.setMap(map);

		// 検索コマンド生成
		String command = createCommand();

		// 出力用検索条件生成
		outputQueryList();

		// 検索実行
		return command;
	}

	private String createCommand() {
		// ログインユーザー情報取得
		User userInfo = getUserInfo();
		if(userInfo==null){
			return "";
		}

		String szBmncd			 = getMap().get("BMNCD");	// 部門コード
		String szRankno			 = getMap().get("RANKNO");	// ランクNo
		String szMoyscd			 = getMap().get("MOYSCD");	// 催しコード
		String szMoyskbn		 = "";						// 催し区分
		String szMoysstdt		 = "";						// 催しコード（催し開始日）
		String szMoysrban		 = "";						// 催し連番

		if (szMoyscd.length() >= 8) {
			szMoyskbn = szMoyscd.substring(0,1);
			szMoysstdt = szMoyscd.substring(1,7);
			szMoysrban = szMoyscd.substring(7);
		}

		String szRinji			 = getMap().get("RINJI");				// 臨時検索
		String sortBtn = getMap().get("SORTBTN");
		String sortBtnId = "TENNO";	// 押下ボタン
		String sortBtnAz = "0";		// ソート順
		if (!sortBtn.equals("-")) {
			for (int i = 0; i < sortBtn.split("-").length; i++) {
				if (i == 0) {
					sortBtnId = sortBtn.split("-")[i];
				} else if (i == 1) {
					sortBtnAz = sortBtn.split("-")[i];
				}
			}
		}

		// タイトル情報(任意)設定
		List<String> titleList = new ArrayList<String>();

		// 基本情報取得
		JSONArray array = getTOKMOYCDData(getMap());

		if(!StringUtils.isEmpty(szRankno) && Integer.parseInt(szRankno) >= 900){
			// 引継いだランクNo.＞＝900の場合、臨時チェック状態を有りする。
			szRinji = "1";
		}else if(!StringUtils.isEmpty(szRankno) && Integer.parseInt(szRankno) < 900) {
			// 引継いだランクNo.＜900の場合、臨時チェック状態を無にしする。
			szRinji = "0";
		}

		// 一覧表情報
		StringBuffer sbSQL = new StringBuffer();
		sbSQL.append(" with WK as (select");

		if(StringUtils.equals("1", szRinji)){
			sbSQL.append(" TENRANK_ARR as ARR");
			sbSQL.append(", 1 as LEN");
			sbSQL.append(" from INATK.TOKRANKEX");
			sbSQL.append(" where MOYSKBN = "+szMoyskbn);
			sbSQL.append(" and MOYSSTDT = "+szMoysstdt);
			sbSQL.append(" and MOYSRBAN = "+szMoysrban);
			sbSQL.append(" and BMNCD = "+szBmncd);
			sbSQL.append(" and RANKNO = "+szRankno);
			sbSQL.append(" and UPDKBN = "+DefineReport.ValUpdkbn.NML.getVal());

		}else if (StringUtils.equals("0", szRinji)){
			sbSQL.append(" TENRANK_ARR as ARR");
			sbSQL.append(", 1 as LEN");
			sbSQL.append(" from INATK.TOKRANK");
			sbSQL.append(" where BMNCD = "+szBmncd);
			sbSQL.append(" and RANKNO =  "+szRankno);
			sbSQL.append(" and UPDKBN = "+DefineReport.ValUpdkbn.NML.getVal());
		}
		sbSQL.append(")");
		if(!StringUtils.isEmpty(szMoyskbn)&&!StringUtils.isEmpty(szMoysstdt)&&!StringUtils.isEmpty(szMoysrban)){
			sbSQL.append(", WK2 as (");
			sbSQL.append("select");
			sbSQL.append(" TENRANK_ARR as ARR");
			sbSQL.append(", 1 as LEN");
			sbSQL.append(" from INATK.TOKRANKEX");
			sbSQL.append(" where BMNCD = "+szBmncd);
			sbSQL.append(" and RANKNO = "+szRankno);
			sbSQL.append(" and UPDKBN = "+DefineReport.ValUpdkbn.NML.getVal());
			sbSQL.append(" and MOYSKBN = "+szMoyskbn);
			sbSQL.append(" and MOYSSTDT = "+szMoysstdt);
			sbSQL.append(" and MOYSRBAN = "+szMoysrban);
			sbSQL.append(" )");
		}
		sbSQL.append(", ARRWK2(IDX, RNK, S, ARR, LEN) as (select");
		sbSQL.append(" 1");
		sbSQL.append(", SUBSTR(ARR, 1, LEN)");
		sbSQL.append(", 1 + LEN");
		sbSQL.append(", ARR");
		sbSQL.append(", LEN");
		sbSQL.append(" from WK");
		sbSQL.append(" union all");
		sbSQL.append(" select");
		sbSQL.append(" IDX + 1");
		sbSQL.append(", SUBSTR(ARR, S, LEN)");
		sbSQL.append(", S + LEN");
		sbSQL.append(", ARR, LEN");
		sbSQL.append(" from ARRWK2");
		sbSQL.append(" where S + LEN < LENGTH(ARR)");
		sbSQL.append(")");
		sbSQL.append(DefineReport.ID_SQL_ARR_CMN);
		sbSQL.append(" select");
		sbSQL.append(" right ('000' || T2.TENCD, 3) as F1");									// F1 :店番
		sbSQL.append(", case");																	// F2 :店舗名称
		sbSQL.append("  when T1.RNK is null or T3.TENCD is null or T2.MISEUNYOKBN = 9 then null");
		sbSQL.append("  else T2.TENKN end as F2");
		sbSQL.append(", T1.RNK as F3");															// F3 :ランク
		sbSQL.append(", T3.AREACD as F4");														// F4 :エリア
		sbSQL.append(", T1_.RNK as F5");
		sbSQL.append(" from ARRWK T1");
		sbSQL.append(" inner join INAMS.MSTTEN T2 on T1.IDX = T2.TENCD and LENGTH(TRIM(T1.RNK)) > 0");
		sbSQL.append(" left join (select * from INAMS.MSTTENBMN where BMNCD = 1) T3 on T3.TENCD = T2.TENCD");
		sbSQL.append(" left join ARRWK2 T1_ on T1_.IDX = T1.IDX");
		if(StringUtils.equals("TENNO", sortBtnId)){
			sbSQL.append(" order by T2.TENCD");
			// 降順の場合
			if (sortBtnAz.equals("1")) {
				sbSQL.append(" desc");
			}
		}else if(StringUtils.equals("RANKNO", sortBtnId)){
			sbSQL.append(" order by T1.RNK");
			// 降順の場合
			if (sortBtnAz.equals("1")) {
				sbSQL.append(" desc");
				sbSQL.append(",T2.TENCD asc");
			}
		}else if(StringUtils.equals("ZISSEKI", sortBtnId)){
			sbSQL.append(" order by T1_.RNK");
		}

		// オプション情報（タイトル）設定
		JSONObject option = new JSONObject();
		option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
		option.put("rows_", array);
		setOption(option);

		if (DefineReport.ID_DEBUG_MODE) System.out.println(getClass().getSimpleName()+"[sql]"+sbSQL.toString());
		return sbSQL.toString();
	}

	private void outputQueryList() {

		// 検索条件の加工クラス作成
		JsonArrayData jad = new JsonArrayData();
		jad.setJsonString(getJson());

	}

	/**
	 * 正情報取得処理
	 *
	 * @throws Exception
	 */
	public JSONArray getTOKMOYCDData(HashMap<String, String> map) {

		String szBmncd		 = map.get("BMNCD");			// 部門コード
		String szRankno		 = map.get("RANKNO");			// ランクNo
		String szMoyscd			 = getMap().get("MOYSCD");	// 催しコード
		String szMoyskbn		 = "";						// 催し区分
		String szMoysstdt		 = "";						// 催しコード（催し開始日）
		String szMoysrban		 = "";						// 催し連番

		if (szMoyscd.length() >= 8) {
			szMoyskbn = szMoyscd.substring(0,1);
			szMoysstdt = szMoyscd.substring(1,7);
			szMoysrban = szMoyscd.substring(7);
		}

		String szRinji		 = map.get("RINJI");			// 臨時検索

		ArrayList<String> paramData = new ArrayList<String>();
		JSONArray array = new JSONArray();

		if(!StringUtils.isEmpty(szRankno) && Integer.parseInt(szRankno) >= 900){
			// 引継いだランクNo.＞＝900の場合、臨時チェック状態を有りする。
			szRinji = "1";
		}else if(!StringUtils.isEmpty(szRankno) && Integer.parseInt(szRankno) < 900) {
			// 引継いだランクNo.＜900の場合、臨時チェック状態を無にしする。
			szRinji = "0";
		} else {
			return array;
		}

		StringBuffer sbSQL = new StringBuffer();

		if(StringUtils.equals("1", szRinji)){
			sbSQL.append("select");
			sbSQL.append(" right('00' || REX.BMNCD, 2) as F1");
			sbSQL.append(", right('000' || REX.RANKNO, 3) as F2");
			sbSQL.append(", REX.RANKKN as F3");
//			sbSQL.append(", REX.MOYSKBN || '-' || REX.MOYSSTDT || '-' || REX.MOYSRBAN as F4");
			sbSQL.append(", right('0'||REX.MOYSKBN, 1) || '-' || right('000000'||REX.MOYSSTDT, 6) || '-' || right('000'||REX.MOYSRBAN, 3) as F4");
			sbSQL.append(", TO_CHAR(REX.ADDDT, 'yy/mm/dd') as F5");
			sbSQL.append(", TO_CHAR(REX.UPDDT, 'yy/mm/dd') as F6");
			sbSQL.append(", REX.OPERATOR as F7");
			sbSQL.append(" from INATK.TOKRANKEX REX");
			sbSQL.append(" where REX.MOYSKBN = "+szMoyskbn);
			sbSQL.append(" and REX.MOYSSTDT = "+szMoysstdt);
			sbSQL.append(" and REX.MOYSRBAN = "+szMoysrban);
			sbSQL.append(" and REX.BMNCD = "+szBmncd);
			sbSQL.append(" and REX.RANKNO = "+szRankno);

		}else if (StringUtils.equals("0", szRinji)){

			sbSQL.append("select");
			sbSQL.append(" right('00' || RNK.BMNCD, 2) as F1");
			sbSQL.append(", right('000' || RNK.RANKNO, 3) as F2");
			sbSQL.append(", RNK.RANKKN as F3");
			sbSQL.append(", null as F4");
			sbSQL.append(", TO_CHAR(RNK.ADDDT, 'yy/mm/dd') as F5");
			sbSQL.append(", TO_CHAR(RNK.UPDDT, 'yy/mm/dd') as F6");
			sbSQL.append(", RNK.OPERATOR as F7");
			sbSQL.append(" from INATK.TOKRANK RNK");
			sbSQL.append(" where RNK.BMNCD = "+szBmncd);
			sbSQL.append(" and RNK.RANKNO = "+szRankno);
		}

		ItemList iL = new ItemList();
		array = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

		return array;
	}
}
