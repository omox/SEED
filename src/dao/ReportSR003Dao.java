package dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
public class ReportSR003Dao extends ItemDao {

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
	public ReportSR003Dao(String JNDIname) {
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

		String szShncd			 = getMap().get("SHNCD"); 		// 商品コード
		String szSyoridt		 = getMap().get("SYORIDT"); 	// 処理日付

		// パラメータ確認
		// 必須チェック
		if ( szShncd == null && szSyoridt == null) {
			System.out.println(super.getConditionLog());
			return "";
		}

		// タイトル情報(任意)設定
		List<String> titleList = new ArrayList<String>();

		// 基本情報取得
		JSONArray array = getTOKMOYCDData(getMap());

		// 一覧表情報
		StringBuffer sbSQL = new StringBuffer();

		sbSQL.append(DefineReport.ID_SQL_CMN_WEEK);
		sbSQL.append(" select");
		sbSQL.append(" T1.MOYSKBN || '-' || right (T1.MOYSSTDT, 6) || '-' || right ('000' || T1.MOYSRBAN, 3) as F1");			// F1 : 催しコード
		sbSQL.append(", TO_CHAR(TO_DATE(T1.HBSTDT, 'YYYYMMDD'), 'YY/MM/DD')");													// F2 : 販売期間
		sbSQL.append("  || (select JWEEK from WEEK where CWEEK = DAYOFWEEK(TO_DATE(T1.HBSTDT, 'YYYYMMDD')))");
		sbSQL.append("  || '～' || TO_CHAR(TO_DATE(T1.HBEDDT, 'YYYYMMDD'), 'YY/MM/DD')");
		sbSQL.append("  || (select JWEEK from WEEK where CWEEK = DAYOFWEEK(TO_DATE(T1.HBEDDT, 'YYYYMMDD'))) as F3");
		sbSQL.append(", TO_CHAR(TO_DATE(T1.NNSTDT, 'YYYYMMDD'), 'YY/MM/DD')");													// F3 : 納入期間
		sbSQL.append("  || (select JWEEK from WEEK where CWEEK = DAYOFWEEK(TO_DATE(T1.NNSTDT, 'YYYYMMDD')))");
		sbSQL.append("  || '～' || TO_CHAR(TO_DATE(T1.NNEDDT, 'YYYYMMDD'), 'YY/MM/DD')");
		sbSQL.append("  || (select JWEEK from WEEK where CWEEK = DAYOFWEEK(TO_DATE(T1.NNEDDT, 'YYYYMMDD'))) as F3");
		sbSQL.append(", T2.IRISU");																								// F4 : 生活応援入数
		sbSQL.append(", T2.MINSU");																								// F5 : 最低発注数
		sbSQL.append(", T2.GENKAAM");																							// F6 : 生活応援原価

		sbSQL.append(", T2.A_BAIKAAM");
		sbSQL.append(", T2.B_BAIKAAM");
		/*sbSQL.append(", case");																									// F7 : A総売価
		sbSQL.append("  when MSSHN.ZEIKBN = 3 then (");
		sbSQL.append("   case");
		sbSQL.append("   when MSBMN.ZEIRTHENKODT <= "+szSyoridt+" then TRUNC(double (T2.A_BAIKAAM) + (double (T2.A_BAIKAAM) * (ZRT_BMN.ZEIRT / 100)), 0)");
		sbSQL.append("   when MSBMN.ZEIRTHENKODT > "+szSyoridt+" then TRUNC(double (T2.A_BAIKAAM) + (double (T2.A_BAIKAAM) * (ZRT_OLD_BMN.ZEIRT / 100)), 0) end)");
		sbSQL.append("  when MSSHN.ZEIKBN = 1 or MSSHN.ZEIKBN = 2 then T2.A_BAIKAAM");
		sbSQL.append("  when MSSHN.ZEIKBN = 0 then (");
		sbSQL.append("   case");
		sbSQL.append("   when MSSHN.ZEIRTHENKODT <= "+szSyoridt+" then TRUNC(double (T2.A_BAIKAAM) + (double (T2.A_BAIKAAM) * (ZRT.ZEIRT / 100)), 0)");
		sbSQL.append("   when MSSHN.ZEIRTHENKODT > "+szSyoridt+" then TRUNC(double (T2.A_BAIKAAM) + (double (T2.A_BAIKAAM) * (ZRT_OLD.ZEIRT / 100)), 0) end) end");
		sbSQL.append(", case");																									// F8 : B総売価
		sbSQL.append("  when MSSHN.ZEIKBN = 3 then (");
		sbSQL.append("   case when MSBMN.ZEIRTHENKODT <= "+szSyoridt+" then TRUNC(double (T2.B_BAIKAAM) + (double (T2.B_BAIKAAM) * (ZRT_BMN.ZEIRT / 100)), 0)");
		sbSQL.append("   when MSBMN.ZEIRTHENKODT > "+szSyoridt+" then TRUNC(double (T2.B_BAIKAAM) + (double (T2.B_BAIKAAM) * (ZRT_OLD_BMN.ZEIRT / 100)), 0) end)");
		sbSQL.append("   when MSSHN.ZEIKBN = 1 or MSSHN.ZEIKBN = 2 then T2.B_BAIKAAM");
		sbSQL.append("  when MSSHN.ZEIKBN = 0 then (case");
		sbSQL.append("   when MSSHN.ZEIRTHENKODT <= "+szSyoridt+" then TRUNC(double (T2.B_BAIKAAM) + (double (T2.B_BAIKAAM) * (ZRT.ZEIRT / 100)), 0)");
		sbSQL.append("   when MSSHN.ZEIRTHENKODT > "+szSyoridt+" then TRUNC(double (T2.B_BAIKAAM) + (double (T2.B_BAIKAAM) * (ZRT_OLD.ZEIRT / 100)), 0) end) end");
		*/
		sbSQL.append(", T2.B_RANKNO");																							// F9 : Bランク
		/*
		sbSQL.append(", case");																									// F10 : C総売価
		sbSQL.append("  when MSSHN.ZEIKBN = 3 then (");
		sbSQL.append("   case");
		sbSQL.append("   when MSBMN.ZEIRTHENKODT <= "+szSyoridt+" then TRUNC(double (T2.C_BAIKAAM) + (double (T2.C_BAIKAAM) * (ZRT_BMN.ZEIRT / 100)), 0)");
		sbSQL.append("   when MSBMN.ZEIRTHENKODT > "+szSyoridt+" then TRUNC(double (T2.C_BAIKAAM) + (double (T2.C_BAIKAAM) * (ZRT_OLD_BMN.ZEIRT / 100)), 0) end)");
		sbSQL.append("  when MSSHN.ZEIKBN = 1 or MSSHN.ZEIKBN = 2 then T2.C_BAIKAAM");
		sbSQL.append("  when MSSHN.ZEIKBN = 0 then (");
		sbSQL.append("   case");
		sbSQL.append("   when MSSHN.ZEIRTHENKODT <= "+szSyoridt+" then TRUNC(double (T2.C_BAIKAAM) + (double (T2.C_BAIKAAM) * (ZRT.ZEIRT / 100)), 0)");
		sbSQL.append("   when MSSHN.ZEIRTHENKODT > "+szSyoridt+" then TRUNC(double (T2.C_BAIKAAM) + (double (T2.C_BAIKAAM) * (ZRT_OLD.ZEIRT / 100)), 0) end) end");
		*/
		sbSQL.append(", T2.C_BAIKAAM");
		sbSQL.append(", T2.C_RANKNO");																							// F11 : Cランク
		sbSQL.append(" from INATK.TOKMOYCD T1");
		sbSQL.append(" inner join INATK.TOKSO_SHN T2 on T2.MOYSKBN = T1.MOYSKBN and T2.MOYSSTDT = T1.MOYSSTDT and T2.MOYSRBAN = T1.MOYSRBAN");
		sbSQL.append(" left join INAMS.MSTBMN BMN on BMN.BMNCD = T2.BMNCD");
		sbSQL.append(" left join INAMS.MSTBMN MSBMN on MSBMN.BMNCD = T2.BMNCD");
		sbSQL.append(" left join INAMS.MSTSHN MSSHN on MSSHN.SHNCD = T2.SHNCD");
		sbSQL.append(" left join INAMS.MSTZEIRT ZRT on ZRT.ZEIRTKBN = MSSHN.ZEIRTKBN");
		sbSQL.append(" left join INAMS.MSTZEIRT ZRT_OLD on ZRT_OLD.ZEIRTKBN = MSSHN.ZEIRTKBN_OLD");
		sbSQL.append(" left join INAMS.MSTZEIRT ZRT_BMN on ZRT_BMN.ZEIRTKBN = MSBMN.ZEIRTKBN");
		sbSQL.append(" left join INAMS.MSTZEIRT ZRT_OLD_BMN on ZRT_OLD_BMN.ZEIRTKBN = MSBMN.ZEIRTKBN_OLD");
		sbSQL.append(" where T2.SHNCD = "+szShncd);
		sbSQL.append(" order by T1.MOYSKBN, T1.MOYSSTDT, T1.MOYSRBAN");

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

		String szShncd		 = map.get("SHNCD"); 		// 商品コード
		String szSyoridt	 = map.get("SYORIDT"); 	// 処理日付

		ArrayList<String> paramData = new ArrayList<String>();

		StringBuffer sbSQL = new StringBuffer();

		sbSQL.append(" select");
		sbSQL.append(" SUBSTRING(right ('0000000' || RTRIM(char (T1.SHNCD)), 8), 1, 4) || '-' || SUBSTRING(right ('0000000' || RTRIM(char (T1.SHNCD)), 8), 5, 4) as F1");
		sbSQL.append(", T1.SHNKN as F2");
		sbSQL.append(", right ('00' || T1.BMNCD, 2) as F3");
		sbSQL.append(", T1.RG_GENKAAM as F4");
		sbSQL.append(", T1.SOBAIKA as F5");
		sbSQL.append(", T1.RG_IRISU as F6");
		sbSQL.append(", TRUNC((double (T1.HTBAIKA) - T1.RG_GENKAAM) / T1.HTBAIKA * 100, 2) as F7");
		sbSQL.append(" from (select");
		sbSQL.append(" SHN.SHNCD");
		sbSQL.append(", SHN.SHNKN");
		sbSQL.append(", SHN.BMNCD");
		sbSQL.append(", SHN.RG_GENKAAM");
		sbSQL.append(", SHN.RG_IRISU");
		sbSQL.append(", case");
		sbSQL.append("  when SHN.ZEIKBN = 3 then (");
		sbSQL.append("   case");
		sbSQL.append("   when BMN.ZEIRTHENKODT <= "+szSyoridt+" then TRUNC(double (SHN.RG_BAIKAAM) + (double (SHN.RG_BAIKAAM) * (ZRT_BMN.ZEIRT / 100)), 0)");
		sbSQL.append("   when BMN.ZEIRTHENKODT > "+szSyoridt+" then TRUNC(double (SHN.RG_BAIKAAM) + (double (SHN.RG_BAIKAAM) * (ZRT_OLD_BMN.ZEIRT / 100)), 0) end)");
		sbSQL.append("  when SHN.ZEIKBN = 1 or SHN.ZEIKBN = 2 then SHN.RG_BAIKAAM when SHN.ZEIKBN = 0 then (");
		sbSQL.append("   case");
		sbSQL.append("   when SHN.ZEIRTHENKODT <= "+szSyoridt+" then TRUNC((double (SHN.RG_BAIKAAM) + (double (SHN.RG_BAIKAAM) * (ZRT.ZEIRT / 100))), 0)");
		sbSQL.append("   when SHN.ZEIRTHENKODT > "+szSyoridt+" then TRUNC(double (SHN.RG_BAIKAAM) + (double (SHN.RG_BAIKAAM) * (ZRT_OLD.ZEIRT / 100)), 0) end) end as SOBAIKA");
		sbSQL.append(", case");
		sbSQL.append("  when SHN.ZEIKBN = 3 then (");
		sbSQL.append("   case");
		sbSQL.append("   when BMN.ZEIRTHENKODT <= "+szSyoridt+" then TRUNC((double (SHN.RG_BAIKAAM) + (double (SHN.RG_BAIKAAM) * (ZRT_BMN.ZEIRT / 100))) / NULLIF(1 + (NVL(ZRT_BMN.ZEIRT, 0) / 100), 0), 0)");
		sbSQL.append("   when BMN.ZEIRTHENKODT > "+szSyoridt+" then TRUNC((double (SHN.RG_BAIKAAM) + (double (SHN.RG_BAIKAAM) * (ZRT_OLD_BMN.ZEIRT / 100))) / NULLIF(1 + (NVL(ZRT_OLD_BMN.ZEIRT, 0) / 100), 0), 0) end)");
		sbSQL.append("  when SHN.ZEIKBN = 1 or SHN.ZEIKBN = 2 then SHN.RG_BAIKAAM when SHN.ZEIKBN = 0 then (");
		sbSQL.append("   case");
		sbSQL.append("   when SHN.ZEIRTHENKODT <= "+szSyoridt+" then TRUNC((double (SHN.RG_BAIKAAM) + (double (SHN.RG_BAIKAAM) * (ZRT.ZEIRT / 100))) / NULLIF(1 + (NVL(ZRT.ZEIRT, 0) / 100), 0), 0)");
		sbSQL.append("   when SHN.ZEIRTHENKODT > "+szSyoridt+" then TRUNC((double (SHN.RG_BAIKAAM) + (double (SHN.RG_BAIKAAM) * (ZRT_OLD.ZEIRT / 100))) / NULLIF(1 + (NVL(ZRT_OLD.ZEIRT, 0) / 100), 0), 0) end) end as HTBAIKA");
		sbSQL.append(" from INAMS.MSTSHN SHN");
		sbSQL.append(" left join INAMS.MSTZEIRT ZRT on ZRT.ZEIRTKBN = SHN.ZEIRTKBN");
		sbSQL.append(" left join INAMS.MSTZEIRT ZRT_OLD on ZRT_OLD.ZEIRTKBN = SHN.ZEIRTKBN_OLD");
		sbSQL.append(" left join INAMS.MSTBMN BMN on BMN.BMNCD = SHN.BMNCD");
		sbSQL.append(" left join INAMS.MSTZEIRT ZRT_BMN on ZRT_BMN.ZEIRTKBN = BMN.ZEIRTKBN");
		sbSQL.append(" left join INAMS.MSTZEIRT ZRT_OLD_BMN on ZRT_OLD_BMN.ZEIRTKBN = BMN.ZEIRTKBN_OLD");
		sbSQL.append(" where NVL(SHN.UPDKBN, 0) <> 1 and SHN.SHNCD = "+szShncd);
		sbSQL.append(") T1");

		ItemList iL = new ItemList();
		@SuppressWarnings("static-access")
		JSONArray array = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

		return array;
	}
}
