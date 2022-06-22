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
public class ReportMM001Dao extends ItemDao {

	/**
	 * インスタンスを生成します。
	 * @param source
	 */
	public ReportMM001Dao(String JNDIname) {
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

		String szShncd		= getMap().get("SHNCD");	// 商品コード
		String szTencd		= getMap().get("TENCD");	// 店コード
		String szHbstdt		= getMap().get("HBSTDT");	// 販売開始日
		String szHbeddt		= getMap().get("HBEDDT");	// 販売終了日
		String szNnstdt		= getMap().get("NNSTDT");	// 納入開始日
		String szNneddt		= getMap().get("NNEDDT");	// 納入終了日
		String szMoysKbn	= getMap().get("MOYSKBN");	// 催し区分
		String szBumon		= getMap().get("BUMON");	// 部門

		// DB検索用パラメータ
		String sqlWhere = "";
		String sqlWhere2 = " where 1=1 ";
		String sqlFrom = "";
		ArrayList<String> paramData = new ArrayList<String>();

		// タイトル情報(任意)設定
		List<String> titleList = new ArrayList<String>();

		if(!"-1".equals(szMoysKbn)){
			sqlWhere2 = " where T1.MOYSKBN = ? ";
			paramData.add(szMoysKbn);
		}

		if (!StringUtils.isEmpty(szHbstdt)) {
			if (!StringUtils.isEmpty(szHbeddt)) {
				sqlWhere2 += " and TO_CHAR(TO_DATE(T1.HBSTDT,'YYYYMMDD'),'YYMMDD') <= ? ";
				paramData.add(szHbeddt);
				sqlWhere2 += " and TO_CHAR(TO_DATE(T1.HBEDDT,'YYYYMMDD'),'YYMMDD') >= ? ";
				paramData.add(szHbstdt);
			} else {
				sqlWhere2 += " and TO_CHAR(TO_DATE(T1.HBSTDT,'YYYYMMDD'),'YYMMDD') >= ? ";
				paramData.add(szHbstdt);
			}
		}
		if (!StringUtils.isEmpty(szNnstdt)) {
			if (!StringUtils.isEmpty(szNneddt)) {
				sqlWhere2 += " and TO_CHAR(TO_DATE(T1.NNSTDT,'YYYYMMDD'),'YYMMDD') <= ? ";
				paramData.add(szNneddt);
				sqlWhere2 += " and TO_CHAR(TO_DATE(T1.NNEDDT,'YYYYMMDD'),'YYMMDD') >= ? ";
				paramData.add(szNnstdt);
			} else {
				sqlWhere2 += " and TO_CHAR(TO_DATE(T1.NNSTDT,'YYYYMMDD'),'YYMMDD') >= ? ";
				paramData.add(szNnstdt);
			}
		}

		if (!StringUtils.isEmpty(szShncd) && StringUtils.isEmpty(szTencd)) {
			sqlWhere += " and T3.SHNCD = ? ";
			paramData.add(szShncd);
		} else if (!StringUtils.isEmpty(szShncd) && !StringUtils.isEmpty(szTencd)) {
			// b. 商品コード、店コード
			sqlWhere += " and T3.SHNCD = ? ";
			paramData.add(szShncd);

			sqlFrom = ",INATK.TOKMM_TEN T4 ";
			sqlWhere += " and T3.MOYSKBN = T4.MOYSKBN";
			sqlWhere += " and T3.MOYSSTDT = T4.MOYSSTDT";
			sqlWhere += " and T3.MOYSRBAN = T4.MOYSRBAN";
			sqlWhere += " and T3.BMFLG = T4.BMFLG";
			sqlWhere += " and T3.BMNO = T4.BMNO";
			sqlWhere += " and T3.BMNCD = T4.BMNCD";
			sqlWhere += " and T3.KANRINO = T4.KANRINO";
			sqlWhere += " and T4.TENCD = ? ";
			paramData.add(szTencd);
		} else if (StringUtils.isEmpty(szShncd) && !StringUtils.isEmpty(szTencd) && !StringUtils.isEmpty(szBumon)) {
			// c. 店コード、部門
			sqlWhere += " and T3.BMNCD = ? ";
			paramData.add(szBumon);

			sqlFrom = ",INATK.TOKMM_TEN T4 ";
			sqlWhere += " and T3.MOYSKBN = T4.MOYSKBN";
			sqlWhere += " and T3.MOYSSTDT = T4.MOYSSTDT";
			sqlWhere += " and T3.MOYSRBAN = T4.MOYSRBAN";
			sqlWhere += " and T3.BMFLG = T4.BMFLG";
			sqlWhere += " and T3.BMNO = T4.BMNO";
			sqlWhere += " and T3.BMNCD = T4.BMNCD";
			sqlWhere += " and T3.KANRINO = T4.KANRINO";
			sqlWhere += " and T4.TENCD = ? ";
			paramData.add(szTencd);
		} else {
			// エラー
			System.out.println(super.getConditionLog());
			return "";
		}

		// 一覧表情報
		StringBuffer sbSQL = new StringBuffer();
		sbSQL.append(DefineReport.ID_SQL_CMN_WEEK);
		sbSQL.append(" select");
		sbSQL.append(" MOYSKBN || right('000000' || MOYSSTDT,6) || right('000' || MOYSRBAN,3) as F1");								// F1 催しコード
		sbSQL.append(", MOYKN as F2");																								// F2 催し名称
		sbSQL.append(", case");
		sbSQL.append(" when (HBSTDTD IS NULL or HBSTDTD='') and (HBEDDTD IS NULL or HBEDDTD='') then ''");
		sbSQL.append(" when (HBSTDTD IS NOT NULL and HBSTDTD <> '') and (HBEDDTD IS NULL or HBEDDTD='') then HBSTDTD || HBSTDTW");
		sbSQL.append(" when (HBSTDTD IS NULL or HBSTDTD='') and (HBEDDTD IS NOT NULL and HBEDDTD <> '') then HBEDDTD || HBEDDTW");
		sbSQL.append(" else HBSTDTD || HBSTDTW || '～' || HBEDDTD || HBEDDTW end as F3");											// F3 販売期間
		sbSQL.append(", case");
		sbSQL.append(" when (NNSTDTD IS NULL or NNSTDTD='') and (NNEDDTD IS NULL or NNEDDTD='') then ''");
		sbSQL.append(" when (NNSTDTD IS NOT NULL and NNSTDTD <> '') and (NNEDDTD IS NULL or NNEDDTD='') then NNSTDTD || NNSTDTW");
		sbSQL.append(" when (NNSTDTD IS NULL or NNSTDTD='') and (NNEDDTD IS NOT NULL and NNEDDTD <> '') then NNEDDTD || NNEDDTW");
		sbSQL.append(" else NNSTDTD || NNSTDTW || '～' || NNEDDTD || NNEDDTW end as F4");											// F4 納入期間
		sbSQL.append(", KANRINO as F5");																							// F5 管理番号
		sbSQL.append(", case");
		sbSQL.append(" when BMFLG = 0 then ''");
		sbSQL.append(" when BMFLG = 1 then '●'");
		sbSQL.append(" else NULL end as F6");																						// F6 B/M
		sbSQL.append(", BMNO as F7");																								// F7 BM番号
		sbSQL.append(", ADDSHUKBN as F8");																							// F8 登録種別
		sbSQL.append(", MOYSKBN as F9");																							// F9 催し区分
		sbSQL.append(", MOYSSTDT as F10");																							// F10 催し開始日
		sbSQL.append(", MOYSRBAN as F11");																							// F11 催し連番
		sbSQL.append(", BMNCD as F12");																								// F12 部門
		sbSQL.append(" from(");
		sbSQL.append(" select");
		sbSQL.append(" T1.MOYSKBN");
		sbSQL.append(" , T1.MOYSSTDT");
		sbSQL.append(" , T1.MOYSRBAN");
		sbSQL.append(" , T2.MOYKN");
		sbSQL.append(" , TO_CHAR(TO_DATE(T1.HBSTDT,'YYYYMMDD'),'YY/MM/DD') as HBSTDTD");
		sbSQL.append(" , TO_CHAR(TO_DATE(T1.HBEDDT,'YYYYMMDD'),'YY/MM/DD') as HBEDDTD");
		sbSQL.append(" , TO_CHAR(TO_DATE(T1.NNSTDT,'YYYYMMDD'),'YY/MM/DD') as NNSTDTD");
		sbSQL.append(" , TO_CHAR(TO_DATE(T1.NNEDDT,'YYYYMMDD'),'YY/MM/DD') as NNEDDTD");
		sbSQL.append(" , (select JWEEK from WEEK where CWEEK=DAYOFWEEK(TO_DATE(T1.HBSTDT,'YYYYMMDD'))) HBSTDTW");
		sbSQL.append(" , (select JWEEK from WEEK where CWEEK=DAYOFWEEK(TO_DATE(T1.HBEDDT,'YYYYMMDD'))) HBEDDTW");
		sbSQL.append(" , (select JWEEK from WEEK where CWEEK=DAYOFWEEK(TO_DATE(T1.NNSTDT,'YYYYMMDD'))) NNSTDTW");
		sbSQL.append(" , (select JWEEK from WEEK where CWEEK=DAYOFWEEK(TO_DATE(T1.NNEDDT,'YYYYMMDD'))) NNEDDTW");
		sbSQL.append(" ,T3.KANRINO");
		sbSQL.append(" ,T3.BMFLG");
		sbSQL.append(", T3.BMNO");
		sbSQL.append(", T3.ADDSHUKBN");
		sbSQL.append(", T3.BMNCD");
		sbSQL.append(" from INATK.TOKMM T1");
		sbSQL.append(" left join INATK.TOKMOYCD T2");
		sbSQL.append(" on T1.MOYSKBN = T2.MOYSKBN");
		sbSQL.append(" and T1.MOYSSTDT = T2.MOYSSTDT");
		sbSQL.append(" and T1.MOYSRBAN = T2.MOYSRBAN");
		sbSQL.append(" ,INATK.TOKMM_SHN T3");
		sbSQL.append(sqlFrom);
		sbSQL.append(sqlWhere2);
		sbSQL.append(" and T1.MOYSKBN = T3.MOYSKBN");
		sbSQL.append(" and T1.MOYSSTDT = T3.MOYSSTDT");
		sbSQL.append(" and T1.MOYSRBAN = T3.MOYSRBAN");
		sbSQL.append(" and T1.BMFLG = T3.BMFLG");
		sbSQL.append(sqlWhere);
		sbSQL.append(" )");
		sbSQL.append(" order by F1, F6, F5");

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
