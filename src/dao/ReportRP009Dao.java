package dao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

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
public class ReportRP009Dao extends ItemDao {

	/**
	 * インスタンスを生成します。
	 * @param source
	 */
	public ReportRP009Dao(String JNDIname) {
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

		String szBmncd		= getMap().get("BMNCD");	// 部門
		String szRankno		= getMap().get("RANKNO");	// ランクNo.
		String szPtnnokbn	= getMap().get("PTNNOKBN");	// パターンNo.区分
		String szPtnno		= getMap().get("PTNNO");	// パターンNo.
		String szSousu		= getMap().get("SOUSU");	// 総数量

		// パラメータ確認
		// 必須チェック
		if ((szBmncd == null) || (szRankno == null) || (szPtnnokbn == null)) {
			System.out.println(super.getConditionLog());
			return "";
		}

//		String msg = this.check();
//		if (!StringUtils.isEmpty(msg)) {
//			System.out.println(super.getConditionLog());
//			return "";
//		}

		// 数量計算処理
		//String arraySuryo = getCalcData();

		// タイトル情報(任意)設定
		List<String> titleList = new ArrayList<String>();

		// 基本情報取得
		JSONArray array = getTENBETUSUData(getMap());

		// 一覧表情報
		StringBuffer sbSQL = new StringBuffer();

		if (StringUtils.equals("1", szPtnnokbn)) {
			// 数量パターン
			sbSQL.append("select");
			sbSQL.append(" right('000'||TENCD,3) as F1");															// F1 : 店番
			sbSQL.append(", (case when MISEUNYOKBN = 9 then NULL when COUNT = 0 then NULL else TENKN end) as F2");	// F2 : 店舗名
			sbSQL.append(", SURYO as F3");																			// F3 : 数量
			sbSQL.append(" from");
			sbSQL.append(" (select");
			sbSQL.append(" T1.TENCD");
			sbSQL.append(", T1.MISEUNYOKBN");
			sbSQL.append(", (select count(T2.TENCD) from INAMS.MSTTENBMN T2 where T2.TENCD=T1.TENCD and T2.BMNCD="+szBmncd+") as COUNT");
			sbSQL.append(", T1.TENKN");
			sbSQL.append(", (select SURYO from INATK.TOKSRYRANK where BMNCD="+szBmncd+" and SRYPTNNO="+szPtnno+" and TENRANK=(SUBSTR((select TENRANK_ARR from INATK.TOKRANK where BMNCD="+szBmncd+" and RANKNO="+szRankno+"),TENCD,1)))");
			sbSQL.append(" from INAMS.MSTTEN T1");
			sbSQL.append(" )");
			sbSQL.append(" where SURYO IS NOT NULL");
			sbSQL.append(" and TENCD <= 400");
			sbSQL.append(" order by TENCD");

		} else if (StringUtils.equals("2", szPtnnokbn)) {
			// 通常率パターン

			String Gokei = this.getGoukeiSuryo(getMap());

			sbSQL.append("with T1(IDX) as (select 1 from SYSIBM.SYSDUMMY1 union all select IDX + 1 from T1 where IDX < 400)");
			sbSQL.append(", WK as (select");
			sbSQL.append(" TENRT_ARR as ARR");
			sbSQL.append(", 5 as LEN");
			sbSQL.append(" from INATK.TOKRTPTN");
			sbSQL.append(" where BMNCD = " + szBmncd);
			sbSQL.append(" and RTPTNNO = " + szPtnno);
			sbSQL.append(" and UPDKBN = 0)");
			sbSQL.append(", ARRWK(IDX, RNK, S, ARR, LEN) as (select 1, SUBSTR(ARR, 1, LEN), 1 + LEN, ARR, LEN from WK union all select IDX + 1, SUBSTR(ARR, S, LEN), S + LEN, ARR, LEN from ARRWK where S <= LENGTH(ARR))");
			sbSQL.append(", BPRT as (select T1.IDX as TENCD, int (TRIM(T4.RNK)) as BPRT from T1 left join ARRWK T4 on T4.IDX = T1.IDX where T4.RNK is not null");
			sbSQL.append(")");

			// ランクマスタ
			sbSQL.append(", WK_RNK as (select");
			sbSQL.append(" TENRANK_ARR as ARR");
			sbSQL.append(", 1 as LEN");
			sbSQL.append(" from INATK.TOKRANK");
			if (StringUtils.equals(null ,szBmncd) || StringUtils.isEmpty(szBmncd)) {
				sbSQL.append(" where BMNCD = null");
			}else{
				sbSQL.append(" where BMNCD = "+szBmncd);
			}
			if (StringUtils.equals(null ,szRankno) || StringUtils.isEmpty(szRankno)) {
				sbSQL.append(" and RANKNO = null");
			}else{
				sbSQL.append(" and RANKNO =  "+szRankno);
			}
			sbSQL.append(" and UPDKBN = "+DefineReport.ValUpdkbn.NML.getVal());
			sbSQL.append(")");
			sbSQL.append(" , ARRWK_RNK(IDX, RNK, S, ARR, LEN) as (select");
			sbSQL.append(" 1");
			sbSQL.append(", SUBSTR(ARR, 1, LEN)");
			sbSQL.append(", 1 + LEN, ARR, LEN");
			sbSQL.append(" from WK_RNK");
			sbSQL.append(" union all select");
			sbSQL.append(" IDX + 1");
			sbSQL.append(", SUBSTR(ARR, S, LEN)");
			sbSQL.append(", S + LEN, ARR, LEN");
			sbSQL.append(" from ARRWK_RNK");
			sbSQL.append(" where S <= LENGTH(ARR))");
			sbSQL.append(" select");
			sbSQL.append(" right('000' || T1.TENCD, 3)");											// F1 : 店番
			sbSQL.append(", M1.TENKN");																// F2 : 店舗名
			sbSQL.append(", TRUNC((double (T1.BPRT) / SUM(T1.BPRT) over ()) * "+szSousu+", 0)");	// F3 : 数量
			sbSQL.append(", T1.BPRT");																// F4 : 分配率
			sbSQL.append(" from BPRT T1");
			sbSQL.append(" inner join ARRWK_RNK T2 on T1.TENCD = T2.IDX and TRIM(T2.RNK) <> ''");
			sbSQL.append(" left join INAMS.MSTTEN M1 on M1.TENCD = T1.TENCD");
			sbSQL.append(" order by T1.TENCD");
			sbSQL.append("");


		} else if (StringUtils.equals("3", szPtnnokbn)) {
			// 実績率パターン

			String Gokei = this.getGoukeiSuryo(getMap());

			sbSQL.append("with T1(IDX) as (select 1 from SYSIBM.SYSDUMMY1 union all select IDX + 1 from T1 where IDX < 400)");
			sbSQL.append(", WK as (select");
			sbSQL.append(" TENTEN_ARR as ARR");
			sbSQL.append(", 9 as LEN");
			sbSQL.append(" from INATK.TOKJRTPTN");
			sbSQL.append(" where BMNCD="+StringUtils.substring(szPtnno, 0, 3));
			sbSQL.append(" and WWMMFLG="+StringUtils.substring(szPtnno, 3, 4));
			sbSQL.append(" and YYMM="+StringUtils.substring(szPtnno, 4, 8));
			sbSQL.append(" and DAICD="+StringUtils.substring(szPtnno, 8, 10));
			sbSQL.append(" and CHUCD="+StringUtils.substring(szPtnno, 10, 12));
			sbSQL.append(")");
			// ランクマスタ
			sbSQL.append(", WK_RNK as (select");
			sbSQL.append(" TENRANK_ARR as ARR");
			sbSQL.append(", 1 as LEN");
			sbSQL.append(" from INATK.TOKRANK");
			if (StringUtils.equals(null ,szBmncd) || StringUtils.isEmpty(szBmncd)) {
				sbSQL.append(" where BMNCD = null");
			}else{
				sbSQL.append(" where BMNCD = "+szBmncd);
			}
			if (StringUtils.equals(null ,szRankno) || StringUtils.isEmpty(szRankno)) {
				sbSQL.append(" and RANKNO = null");
			}else{
				sbSQL.append(" and RANKNO =  "+szRankno);
			}
			sbSQL.append(" and UPDKBN = "+DefineReport.ValUpdkbn.NML.getVal());
			sbSQL.append(")");
			sbSQL.append(", ARRWK(IDX, TENSU, S, ARR, LEN) as (select 1, SUBSTR(ARR, 1, LEN), 1 + LEN, ARR, LEN from WK union all select IDX + 1, SUBSTR(ARR, S, LEN), S + LEN, ARR, LEN from ARRWK where S <= LENGTH(ARR))");
			sbSQL.append(" , ARRWK_RNK(IDX, RNK, S, ARR, LEN) as (select");
			sbSQL.append(" 1");
			sbSQL.append(", SUBSTR(ARR, 1, LEN)");
			sbSQL.append(", 1 + LEN, ARR, LEN");
			sbSQL.append(" from WK_RNK");
			sbSQL.append(" union all select");
			sbSQL.append(" IDX + 1");
			sbSQL.append(", SUBSTR(ARR, S, LEN)");
			sbSQL.append(", S + LEN, ARR, LEN");
			sbSQL.append(" from ARRWK_RNK");
			sbSQL.append(" where S <= LENGTH(ARR))");
			sbSQL.append(", TENSU as (select T1.IDX as TENCD, int (TRIM(T4.TENSU)) as TENSU from T1 left join ARRWK T4 on T4.IDX = T1.IDX where TRIM(T4.TENSU) <> '')");
			sbSQL.append(" select");
			sbSQL.append(" right ('000' || T1.TENCD, 3)");														// F1 : 店番
			sbSQL.append(", M1.TENKN");																			// F2 : 数量
			sbSQL.append(", TRUNC((double (T1.TENSU) / SUM(T1.TENSU) over ()) * "+szSousu+", 0) as SURYO");		// F3 : 数量
			sbSQL.append(", T1.TENSU");																			// F4 : 点数(数量計算用)
			sbSQL.append(" from TENSU T1");
			sbSQL.append(" inner join ARRWK_RNK T2 on T1.TENCD = T2.IDX and TRIM(T2.RNK) <> ''");
			sbSQL.append(" left join INAMS.MSTTEN M1 on M1.TENCD = T1.TENCD ");
			sbSQL.append("order by T1.TENCD");
		}

		// オプション情報（タイトル）設定
		JSONObject option = new JSONObject();
		option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
		option.put("rows_", array);
		setOption(option);

		if (DefineReport.ID_DEBUG_MODE) System.out.println(getClass().getSimpleName()+"[sql]"+sbSQL.toString());
		return sbSQL.toString();
	}

	/**
	 * 正情報取得処理
	 *
	 * @throws Exception
	 */
	public JSONArray getTENBETUSUData(HashMap<String, String> map) {
		String szBmncd		= getMap().get("BMNCD");	// 部門
		String szRankno		= getMap().get("RANKNO");	// ランクNo.
		String szPtnnokbn	= getMap().get("PTNNOKBN");	// パターンNo.区分
		String szPtnno		= getMap().get("PTNNO");	// パターンNo.
		String szSousu		= getMap().get("SOUSU");	// 総数量

		ArrayList<String> paramData = new ArrayList<String>();
		StringBuffer sbSQL = new StringBuffer();

		if (StringUtils.equals("1", szPtnnokbn)) {
			// 数量パターン
			sbSQL.append("select");
			sbSQL.append(" T1.BMNCD as F1");			// F1 : 部門
			sbSQL.append(", T1.RANKNO as F2");			// F2 : ランクNo.
			sbSQL.append(", T1.RANKKN as F3");			// F3 : ランク名称
			sbSQL.append(", T2.SRYPTNNO as F4");		// F4 : 数量パターンNo.
			sbSQL.append(", T2.SRYPTNKN as F5");		// F5 : 数量パターン名称
			sbSQL.append(", NULL as F6");				// F6 : 通常率パターンNo.
			sbSQL.append(", NULL as F7");				// F7 : 通常率パターン名称
			sbSQL.append(", NULL as F8");				// F8 : 実績率パターンNo.
			sbSQL.append(", NULL as F9");				// F9 : 実績率パターン名称
			sbSQL.append(", NULL as F10");				// F10 : 総数量
			sbSQL.append(", (select sum(SURYO) from"
					+ " (select TENCD, SURYO from"
						+ " (select T1.TENCD"
						+ " , (select SURYO from INATK.TOKSRYRANK where BMNCD="+szBmncd+" and SRYPTNNO="+szPtnno+" and"
							+ " TENRANK=(SUBSTR((select TENRANK_ARR from INATK.TOKRANK where BMNCD="+szBmncd+" and RANKNO="+szRankno+"),TENCD,1))"
							+ ")"
							+ " from INAMS.MSTTEN T1"
						+ ")"
						+ " where SURYO IS NOT NULL and TENCD <= 400"
					+ ")"
				+ ") as F11");					// F11 : 合計数
			sbSQL.append(", NULL as F12");		// F12 : 点数が設定されている店の数
			sbSQL.append(" from");
			sbSQL.append(" (select");
			sbSQL.append(" BMNCD");
			sbSQL.append(", RANKNO");
			sbSQL.append(", RANKKN");
			sbSQL.append(" from INATK.TOKRANK");
			sbSQL.append(" where BMNCD="+szBmncd);
			sbSQL.append(" and RANKNO="+szRankno);
			sbSQL.append(" and nvl(UPDKBN, 0) <> 1");
			sbSQL.append(") as T1");
			sbSQL.append(", (select");
			sbSQL.append(" BMNCD");
			sbSQL.append(", SRYPTNNO");
			sbSQL.append(", SRYPTNKN");
			sbSQL.append(" from INATK.TOKSRPTN");
			sbSQL.append(" where BMNCD="+szBmncd);
			sbSQL.append(" and SRYPTNNO="+szPtnno);
			sbSQL.append(" and nvl(UPDKBN, 0) <> 1");
			sbSQL.append(") as T2");

		} else if (StringUtils.equals("2", szPtnnokbn)) {
			// 通常率パターン
			sbSQL.append("select");
			sbSQL.append(" T1.BMNCD as F1");			// F1 : 部門
			sbSQL.append(", T1.RANKNO as F2");			// F2 : ランクNo.
			sbSQL.append(", T1.RANKKN as F3");			// F3 : ランク名称
			sbSQL.append(", NULL as F4");				// F4 : 数量パターンNo.
			sbSQL.append(", NULL as F5");				// F5 : 数量パターン名称
			sbSQL.append(", T2.RTPTNNO as F6");			// F6 : 通常率パターンNo.
			sbSQL.append(", T2.RTPTNKN as F7");			// F7 : 通常率パターン名称
			sbSQL.append(", NULL as F8");				// F8 : 実績率パターンNo.
			sbSQL.append(", NULL as F9");				// F9 : 実績率パターン名称
			sbSQL.append(", "+szSousu+" as F10");		// F10 : 総数量
			//sbSQL.append(", (select sum(SURYO) from (select (INT(SUBSTR('"+arraySuryo+"', ((TENCD -1) * 5 +1), 5))) as SURYO from INAMS.MSTTEN where TENCD <= 400)) as F11");			// F11 : 合計数
			//sbSQL.append(", (select count(TENCD) from (select TENCD from INAMS.MSTTEN where TENCD <= 400 and (INT(SUBSTR('"+arraySuryo+"', ((TENCD -1) * 5 +1), 5))) > 0)) as F12");	// F12 : 点数が設定されている店の数

			//sbSQL.append(", "+ this.getGoukeiSuryo(map) + " as F11");
			sbSQL.append(", NULL as F11");
			sbSQL.append(", NULL as F12");
			sbSQL.append(" from");
			sbSQL.append(" (select");
			sbSQL.append(" BMNCD");
			sbSQL.append(", RANKNO");
			sbSQL.append(", RANKKN");
			sbSQL.append(" from INATK.TOKRANK");
			sbSQL.append(" where BMNCD="+szBmncd);
			sbSQL.append(" and RANKNO="+szRankno);
			sbSQL.append(" and nvl(UPDKBN, 0) <> 1");
			sbSQL.append(") as T1");
			sbSQL.append(", (select");
			sbSQL.append(" BMNCD");
			sbSQL.append(", RTPTNNO");
			sbSQL.append(", RTPTNKN");
			sbSQL.append(" from INATK.TOKRTPTN");
			sbSQL.append(" where BMNCD="+szBmncd);
			sbSQL.append(" and RTPTNNO="+szPtnno);
			sbSQL.append(" and nvl(UPDKBN, 0) <> 1");
			sbSQL.append(") as T2");
		} else if (StringUtils.equals("3", szPtnnokbn)) {
			// 実績率パターン
			sbSQL.append("select");
			sbSQL.append(" T1.BMNCD as F1");			// F1 : 部門
			sbSQL.append(", T1.RANKNO as F2");			// F2 : ランクNo.
			sbSQL.append(", T1.RANKKN as F3");			// F3 : ランク名称
			sbSQL.append(", NULL as F4");				// F4 : 数量パターンNo.
			sbSQL.append(", NULL as F5");				// F5 : 数量パターン名称
			sbSQL.append(", NULL as F6");				// F6 : 通常率パターンNo.
			sbSQL.append(", NULL as F7");				// F7 : 通常率パターン名称
			sbSQL.append(", T2.JRTPTNNO as F8");		// F8 : 実績率パターンNo.
			sbSQL.append(", T2.JRTPTNKN as F9");		// F9 : 実績率パターン名称
			sbSQL.append(", "+szSousu+" as F10");		// F10 : 総数量
			//sbSQL.append(", (select sum(SURYO) from (select (INT(SUBSTR('"+arraySuryo+"', ((TENCD -1) * 9 +1), 9))) as SURYO from INAMS.MSTTEN where TENCD <= 400)) as F11");	// F11 : 合計数
			//sbSQL.append(", (select count(TENCD) from (select TENCD from INAMS.MSTTEN where TENCD <= 400 and (INT(SUBSTR('"+arraySuryo+"', ((TENCD -1) * 9 +1), 9)))  > 0)) as F12");	// F12 : 点数が設定されている店の数
			//sbSQL.append(", "+ this.getGoukeiSuryo(map) + " as F11");
			sbSQL.append(", NULL as F11");
			sbSQL.append(", NULL as F12");
			sbSQL.append(" from");
			sbSQL.append(" (select");
			sbSQL.append(" BMNCD");
			sbSQL.append(", RANKNO");
			sbSQL.append(", RANKKN");
			sbSQL.append(" from INATK.TOKRANK");
			sbSQL.append(" where BMNCD="+szBmncd);
			sbSQL.append(" and RANKNO="+szRankno);
			sbSQL.append(" and nvl(UPDKBN, 0) <> 1");
			sbSQL.append(") as T1");


			sbSQL.append(", (select");
			sbSQL.append(" JPT.BMNCD");
			sbSQL.append(", (right ('000' || JPT.BMNCD, 3) || right ('0' || JPT.WWMMFLG, 1) || right ('0000' || JPT.YYMM, 4) || right ('00' || JPT.DAICD, 2) || right ('00' || JPT.CHUCD, 2)) as JRTPTNNO");
			sbSQL.append(", case when CHU.CHUCD is not null then CHU.CHUBRUIKN when DAI.DAICD is not null then DAI.DAIBRUIKN end as JRTPTNKN");
			sbSQL.append(" from INATK.TOKJRTPTN JPT");
			sbSQL.append(" left join INAMS.MSTDAIBRUI DAI on DAI.BMNCD = JPT.BMNCD and DAI.DAICD = JPT.DAICD and NVL(DAI.UPDKBN, 0) <> 1");
			sbSQL.append(" left join INAMS.MSTCHUBRUI CHU on CHU.BMNCD = JPT.BMNCD and CHU.DAICD = JPT.DAICD and CHU.CHUCD = JPT.CHUCD and NVL(CHU.UPDKBN, 0) <> 1");
			sbSQL.append(" where JPT.BMNCD="+StringUtils.substring(szPtnno, 0, 3));
			sbSQL.append(" and JPT.WWMMFLG="+StringUtils.substring(szPtnno, 3, 4));
			sbSQL.append(" and JPT.YYMM="+StringUtils.substring(szPtnno, 4, 8));
			sbSQL.append(" and JPT.DAICD="+StringUtils.substring(szPtnno, 8, 10));
			sbSQL.append(" and JPT.CHUCD="+StringUtils.substring(szPtnno, 10, 12));
			sbSQL.append(") as T2");

			/*sbSQL.append(", (select");
			sbSQL.append(" BMNCD");
			sbSQL.append(", (right('000'||BMNCD, 3)||right('0'||WWMMFLG, 1)||right('0000'||YYMM, 4)||right('00'||DAICD, 2)||right('00'||CHUCD, 2)) as JRTPTNNO");
			sbSQL.append(", (select T3.CHUBRUIKN from INAMS.MSTCHUBRUI T3 where T3.BMNCD=1 and T3.DAICD=0 and T3.CHUCD=0) as JRTPTNKN");
			sbSQL.append(" from INATK.TOKJRTPTN");
			sbSQL.append(" where BMNCD="+StringUtils.substring(szPtnno, 0, 3));
			sbSQL.append(" and WWMMFLG="+StringUtils.substring(szPtnno, 3, 4));
			sbSQL.append(" and YYMM="+StringUtils.substring(szPtnno, 4, 8));
			sbSQL.append(" and DAICD="+StringUtils.substring(szPtnno, 8, 10));
			sbSQL.append(" and CHUCD="+StringUtils.substring(szPtnno, 10, 12));
			sbSQL.append(") as T2");*/
		}

		ItemList iL = new ItemList();
		@SuppressWarnings("static-access")
		JSONArray array = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

		return array;
	}

	public String getGoukeiSuryo(HashMap<String, String> map) {

		String szBmncd		= getMap().get("BMNCD");	// 部門
		String szRankno		= getMap().get("RANKNO");	// ランクNo.
		String szPtnnokbn	= getMap().get("PTNNOKBN");	// パターンNo.区分
		String szPtnno		= getMap().get("PTNNO");	// パターンNo.
		String szSousu		= getMap().get("SOUSU");	// 総数量

		String gokei = "";

		ArrayList<String> paramData = new ArrayList<String>();
		StringBuffer sbSQL = new StringBuffer();

		if (StringUtils.equals("1", szPtnnokbn)) {
			// 数量パターン


		} else if (StringUtils.equals("2", szPtnnokbn)) {
			// 通常率パターン
			sbSQL.append("with T1(IDX) as (select 1 from SYSIBM.SYSDUMMY1 union all select IDX + 1 from T1 where IDX < 400)");
			sbSQL.append(", WK as (select");
			sbSQL.append(" TENRT_ARR as ARR");
			sbSQL.append(", 5 as LEN");
			sbSQL.append(" from INATK.TOKRTPTN");
			sbSQL.append(" where BMNCD = " + szBmncd);
			sbSQL.append(" and RTPTNNO = " + szPtnno);
			sbSQL.append(" and UPDKBN = 0)");
			sbSQL.append(", ARRWK(IDX, RNK, S, ARR, LEN) as (select 1, SUBSTR(ARR, 1, LEN), 1 + LEN, ARR, LEN from WK union all select IDX + 1, SUBSTR(ARR, S, LEN), S + LEN, ARR, LEN from ARRWK where S <= LENGTH(ARR))");
			sbSQL.append(", BPRT as (select T1.IDX as TENCD, int (TRIM(T4.RNK)) as BPRT from T1 left join ARRWK T4 on T4.IDX = T1.IDX where T4.RNK is not null");
			sbSQL.append(")");
			sbSQL.append(" select");
			sbSQL.append(" int(SUM(T.SURYO)) as value");
			sbSQL.append(" from (select T1.TENCD, TRUNC((double (T1.BPRT) / SUM(T1.BPRT) over ()) * "+szSousu+", 0) as SURYO from BPRT T1) T");

		} else if (StringUtils.equals("3", szPtnnokbn)) {

			sbSQL.append("with T1(IDX) as (select 1 from SYSIBM.SYSDUMMY1 union all select IDX + 1 from T1 where IDX < 400)");
			sbSQL.append(", WK as (select");
			sbSQL.append(" TENTEN_ARR as ARR");
			sbSQL.append(", 9 as LEN");
			sbSQL.append(" from INATK.TOKJRTPTN");
			sbSQL.append(" where BMNCD="+StringUtils.substring(szPtnno, 0, 3));
			sbSQL.append(" and WWMMFLG="+StringUtils.substring(szPtnno, 3, 4));
			sbSQL.append(" and YYMM="+StringUtils.substring(szPtnno, 4, 8));
			sbSQL.append(" and DAICD="+StringUtils.substring(szPtnno, 8, 10));
			sbSQL.append(" and CHUCD="+StringUtils.substring(szPtnno, 10, 12));
			sbSQL.append(")");
			sbSQL.append(", ARRWK(IDX, TENSU, S, ARR, LEN) as (select 1, SUBSTR(ARR, 1, LEN), 1 + LEN, ARR, LEN from WK union all select IDX + 1, SUBSTR(ARR, S, LEN), S + LEN, ARR, LEN from ARRWK where S <= LENGTH(ARR))");
			sbSQL.append(", TENSU as (select T1.IDX as TENCD, int (TRIM(T4.TENSU)) as TENSU from T1 left join ARRWK T4 on T4.IDX = T1.IDX where TRIM(T4.TENSU) <> '')");
			sbSQL.append(" select");
			sbSQL.append(" int(SUM(T.SURYO)) as value");
			sbSQL.append(" from (select");
			sbSQL.append(" T1.TENCD");
			sbSQL.append(", TRUNC((double (T1.TENSU) / SUM(T1.TENSU) over ()) * "+szSousu+", 0) as SURYO from TENSU T1) T");
		}

		ItemList iL = new ItemList();
		@SuppressWarnings("static-access")
		JSONArray array = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);
		if (array.size() > 0){
			gokei = array.getJSONObject(0).optString("VALUE");

		}

		return gokei;
	}

	private void outputQueryList() {

		// 検索条件の加工クラス作成
		JsonArrayData jad = new JsonArrayData();
		jad.setJsonString(getJson());

		// 保存用 List (検索情報)作成
		setWhere(new ArrayList<List<String>>());
	}

	/**
	 * 数量計算処理
	 *
	 * @throws Exception
	 */
	@SuppressWarnings("static-access")
	public String getCalcData() {

		String szBmncd		= getMap().get("BMNCD");	// 部門
		String szPtnnokbn	= getMap().get("PTNNOKBN");	// パターンNo.区分
		String szPtnno		= getMap().get("PTNNO");	// パターンNo.
		String szSousu		= getMap().get("SOUSU");	// 総数量

		ArrayList<String> paramData	 = new ArrayList<String>();
		String sqlcommand = "";
		String arraySuryo = "";

		// チェック処理
		ItemList iL = new ItemList();
		paramData	 = new ArrayList<String>();

		if (StringUtils.equals("1", szPtnnokbn)) {
			// 数量パターン
			return arraySuryo;

		} else if (StringUtils.equals("2", szPtnnokbn)) {
			// 通常率パターン
			paramData.add(szBmncd);
			paramData.add(szPtnno);
			sqlcommand = "select";
			sqlcommand += " TENCD";
			sqlcommand += ", SURYO";
			sqlcommand += " from";
			sqlcommand += " (select";
			sqlcommand += " T1.TENCD";
			sqlcommand += ", (INT(SUBSTR((select TENRT_ARR from INATK.TOKRTPTN where BMNCD=? and RTPTNNO=?), ((TENCD -1) * 5 + 1), 5))) as SURYO";
			sqlcommand += " from INAMS.MSTTEN T1";
			sqlcommand += " )";
			sqlcommand += " where SURYO IS NOT NULL";
			sqlcommand += " and TENCD <= 400";
			sqlcommand += " order by TENCD";

		} else if (StringUtils.equals("3", szPtnnokbn)) {
			// 実績率パターン
			paramData.add(StringUtils.substring(szPtnno, 0, 3));
			paramData.add(StringUtils.substring(szPtnno, 3, 4));
			paramData.add(StringUtils.substring(szPtnno, 4, 8));
			paramData.add(StringUtils.substring(szPtnno, 8, 10));
			paramData.add(StringUtils.substring(szPtnno, 10, 12));
			sqlcommand = "select";
			sqlcommand += " TENCD";
			sqlcommand += ", SURYO";
			sqlcommand += " from";
			sqlcommand += " (select";
			sqlcommand += " T1.TENCD";
			sqlcommand += ", (INT(SUBSTR((select TENTEN_ARR from INATK.TOKJRTPTN where BMNCD=? and WWMMFLG=? and YYMM=? and DAICD=? and CHUCD=?), ((TENCD -1) * 9 + 1), 9))) as SURYO";
			sqlcommand += " from INAMS.MSTTEN T1";
			sqlcommand += " )";
			sqlcommand += " where SURYO IS NOT NULL";
			sqlcommand += " and TENCD <= 400";
			sqlcommand += " order by TENCD";
		}

		JSONArray arrayDataSuryo = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);

		int arraySize = arrayDataSuryo.size();

		int totalSuryo = 0;		// 合計分配率
		for(int j=0; j < arraySize; j++){
			totalSuryo += NumberUtils.toInt(arrayDataSuryo.getJSONObject(j).optString("SURYO"));
		}

		int suryoData[][];
		suryoData = new int[arraySize][2];

		BigDecimal suryo;
		BigDecimal calBig;
		int calInt = 0;

		for(int j=0; j < arraySize; j++){
			suryo = BigDecimal.valueOf(NumberUtils.toLong(arrayDataSuryo.getJSONObject(j).optString("SURYO")));
			// 計算式
			calBig = suryo.multiply(BigDecimal.valueOf(NumberUtils.toLong(szSousu))).divide(BigDecimal.valueOf(totalSuryo), 0, BigDecimal.ROUND_DOWN);
			calInt = Integer.valueOf(calBig.toString());
			if (calInt < 1) {
				suryoData[j][1] = 0;
			} else {
				suryoData[j][1] = NumberUtils.toInt(arrayDataSuryo.getJSONObject(j).optString("SURYO"));
			}
			suryoData[j][0] =  NumberUtils.toInt(arrayDataSuryo.getJSONObject(j).optString("TENCD"));
		}

		// 展開数総計
		int newCount = 0;
		for(int j=0; j < arraySize; j++){
			newCount += suryoData[j][1];
		}
		int inputSousu = NumberUtils.toInt(szSousu);
		int newCount2 = newCount;

		if (0 < newCount) {
			if (newCount < inputSousu) {	// 展開数量 < 発注総数 の場合

				// 分配率の大きい順にソート
				Arrays.sort(suryoData, new Comparator<int[]>() {
					public int compare(int[] a, int[] b) {
						if(a[1] < b[1]) {
							return 1;
						}else if (a[1] == b[1]) {
							return 0;
						}else {
							return -1;
				}}});

				for (int i=0; i < inputSousu; i++) {
//				while (newCount2 < inputSousu) {
					for (int k=0; k < arraySize; k++) {
						// 加算
						if (0 < suryoData[k][1]) {
							suryoData[k][1] = suryoData[k][1] + 1;

							newCount2 = 0;
							for(int j=0; j < arraySize; j++){
								newCount2 += suryoData[j][1];	// 展開数量計算
							}
							if (newCount2 == inputSousu) {
								break;
							}
						}
					}
					if (newCount2 == inputSousu) {
						break;
					}
				}

			} else {	// 展開数量 > 発注総数 の場合

				// 分配率の小さい順にソート
				Arrays.sort(suryoData, new Comparator<int[]>() {
					public int compare(int[] a, int[] b) {
						if(a[1] > b[1]) {
							return 1;
						}else if (a[1] == b[1]) {
							return 0;
						}else {
							return -1;
				}}});

					for (int i=0; i < inputSousu; i++) {
//					while (newCount2 > inputSousu) {
						for (int k=0; k < arraySize; k++) {
							// 減算
							if (0 < suryoData[k][1]) {
								suryoData[k][1] = suryoData[k][1] - 1;

								newCount2 = 0;
								for(int j=0; j < arraySize; j++){
									newCount2 += suryoData[j][1];	// 展開数量計算
								}
								if (newCount2 == inputSousu) {
									break;
								}
								if (newCount2 == 0) {
									break;
								}
							}
						}
						if (newCount2 == inputSousu) {
							break;
						}
					}
			}
		}
		// 店番順にソート
		Arrays.sort(suryoData, new Comparator<int[]>() {
			public int compare(int[] a, int[] b) {
				if(a[0] > b[0]) {
					return 1;
				}else if (a[1] == b[1]) {
					return 0;
				}else {
					return -1;
		}}});

//		// 確認用
//		int newCount2 = 0;
//		for(int j=0; j < arrayDataSuryo.size(); j++){
//			newCount2 += suryoData[j][1];
//		}

		if (StringUtils.equals("2", szPtnnokbn)) {
			for (int l=0; l < arraySize; l++) {
				arraySuryo += StringUtils.leftPad(String.valueOf(suryoData[l][1]), 5, '0');
			}
		} else if (StringUtils.equals("3", szPtnnokbn)) {
			for (int l=0; l < arraySize; l++) {
				arraySuryo += StringUtils.leftPad(String.valueOf(suryoData[l][1]), 9, '0');
			}
		}
		return arraySuryo;
	}

	/**
	 * チェック処理
	 *
	 * @param map
	 * @return
	 */
	@SuppressWarnings("static-access")
	public String check() {

		String szBmncd		= getMap().get("BMNCD");	// 部門
		String szRankno		= getMap().get("RANKNO");	// ランクNo.
		String szPtnnokbn	= getMap().get("PTNNOKBN");	// パターンNo.区分
		String szPtnno		= getMap().get("PTNNO");	// パターンNo.
		String szSousu		= getMap().get("SOUSU");	// 総数量


		ArrayList<String> paramData	 = new ArrayList<String>();
		String sqlcommand			 = "";
//		JSONArray	msg		 = new JSONArray();
//		MessageUtility	mu	 = new MessageUtility();
		JSONArray array = null;
		String msg = "";

		// チェック処理
		ItemList iL = new ItemList();
		paramData	 = new ArrayList<String>();
		paramData.add(szBmncd);
		paramData.add(szRankno);
		sqlcommand = "select count(BMNCD) as VALUE from INATK.TOKRANK where UPDKBN = "+DefineReport.ValUpdkbn.NML.getVal()+" and BMNCD=? and RANKNO=?";
		array = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
		if(array.size() > 0 && NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) < 1){
//			JSONObject o = mu.getDbMessageObj("E20057", "ランクマスタ");
//			msg.add(o);
			msg = "E20057";
			return msg;
		}

		if (StringUtils.equals("2", szPtnnokbn)) {
			paramData	 = new ArrayList<String>();
			paramData.add(szBmncd);									// 部門
			paramData.add(szPtnno);									// 率パターンNo.
			sqlcommand = "select count(TENCD) as VALUE from"
					+ " (select TENCD, SURYO from"
					+ " (select T1.TENCD, (INT(SUBSTR((select TENRT_ARR from INATK.TOKRTPTN where BMNCD=? and RTPTNNO=?), ((TENCD * 5) -4), 5))) as SURYO"
					+ " from INAMS.MSTTEN T1)"
					+ " where SURYO IS NOT NULL and TENCD <= 400"
					+ ")";
			array = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
			if(array.size() > 0 && NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) > NumberUtils.toInt(szSousu)){
//				JSONObject o = mu.getDbMessageObj("E20057", "ランクマスタ");	// TODO
//				msg.add(o);
				msg = "E20057";
				return msg;
			}

		} else if (StringUtils.equals("3", szPtnnokbn)) {
			paramData	 = new ArrayList<String>();
			paramData.add(StringUtils.substring(szPtnno, 0, 3));	// 部門
			paramData.add(StringUtils.substring(szPtnno, 3, 4));	// 週月フラグ
			paramData.add(StringUtils.substring(szPtnno, 4, 8));	// 年月
			paramData.add(StringUtils.substring(szPtnno, 8, 10));	// 大分類
			paramData.add(StringUtils.substring(szPtnno, 10, 12));	// 中分類
			sqlcommand = "select count(TENCD) as VALUE from"
					+ " (select TENCD, SURYO from"
					+ " (select T1.TENCD, (INT(SUBSTR((select TENTEN_ARR from INATK.TOKJRTPTN where BMNCD=? and WWMMFLG=? and YYMM=? and DAICD=? and CHUCD=?), ((TENCD-1)* 9 +1), 9))) as SURYO"
					+ " from INAMS.MSTTEN T1)"
					+ " where SURYO IS NOT NULL and TENCD <= 400"
					+ ")";
			array = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
			if(array.size() > 0 && NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) > NumberUtils.toInt(szSousu)){
//				JSONObject o = mu.getDbMessageObj("E20057", "ランクマスタ");	// TODO
//				msg.add(o);
				msg = "E20057";
				return msg;
			}
		}
		return msg;
	}
}
