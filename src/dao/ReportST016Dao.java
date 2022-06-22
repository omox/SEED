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
public class ReportST016Dao extends ItemDao {

	/**
	 * インスタンスを生成します。
	 * @param source
	 */
	public ReportST016Dao(String JNDIname) {
		super(JNDIname);
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

		String szBumon		 = getMap().get("BMNCD"); 								// 部門
		String szMoyskbn	 = getMap().get("MOYSKBN");								// 催し区分
		String szMoysstdt	 = getMap().get("MOYSSTDT");							// 催しコード（催し開始日）
		String szMoysrban	 = getMap().get("MOYSRBAN");							// 催し連番
		JSONArray shnData	= JSONArray.fromObject(getMap().get("SHNDATA"));		// 選択商品情報

		String szTableBMN	 = "";													// テーブル名称：全店特売_部門(アンケート有/無)
		String szTableSHN	 = "";													// テーブル名称：全店特売_商品(アンケート有/無)
		String szTableTJTEN	 = "";													// テーブル名称：全店特売_対象除外店(アンケート有/無)
		String szTableNNDT	 = "";													// テーブル名称：全店特売_納入日(アンケート有/無)

		boolean isTOKTG = super.isTOKTG(szMoyskbn, szMoysrban);
		if(StringUtils.equals("1", szMoyskbn) && 50 <= Integer.parseInt(szMoysrban)){
			isTOKTG = true;
		}

		if(isTOKTG){
			szTableBMN	 = "INATK.TOKTG_BMN";
			szTableSHN	 = "INATK.TOKTG_SHN";
			szTableTJTEN = "INATK.TOKTG_TJTEN";
			szTableNNDT	 = "INATK.TOKTG_NNDT";
		}else{
			szTableBMN	 = "INATK.TOKSP_BMN";
			szTableSHN	 = "INATK.TOKSP_SHN";
			szTableTJTEN = "INATK.TOKSP_TJTEN";
			szTableNNDT	 = "INATK.TOKSP_NNDT";
		}

		CsvFileInfo cfi = null;
		if(StringUtils.equals(btnId,  DefineReport.Button.CSV.getObj() + "1")){
			cfi = CsvFileInfo.CSV1;
		}else if(StringUtils.equals(btnId,  DefineReport.Button.CSV.getObj() + "2")){
			cfi = CsvFileInfo.CSV2;
		}

		// 一覧表情報
		StringBuffer sbSQL = new StringBuffer();

		if(StringUtils.equals(btnId,  DefineReport.Button.CSV.getObj() + "1")){
			// 特売原稿CSVボタン押下時
			sbSQL.append(" with CAL as (select");
			sbSQL.append(" MOYSKBN");
			sbSQL.append(", MOYSSTDT");
			sbSQL.append(", MOYSRBAN");
			sbSQL.append(", BMNCD");
			sbSQL.append(", KANRINO");
			sbSQL.append(", KANRIENO");
			sbSQL.append(", (TO_CHAR(TO_DATE(MYOSNNSTDT, 'YYYYMMDD'), 'YYYYMMDD')) as DT1");
			sbSQL.append(", TO_CHAR(TO_DATE(MYOSNNSTDT, 'YYYYMMDD') + 1 DAYS, 'YYYYMMDD') as DT2");
			sbSQL.append(", TO_CHAR(TO_DATE(MYOSNNSTDT, 'YYYYMMDD') + 2 DAYS, 'YYYYMMDD') as DT3");
			sbSQL.append(", TO_CHAR(TO_DATE(MYOSNNSTDT, 'YYYYMMDD') + 3 DAYS, 'YYYYMMDD') as DT4");
			sbSQL.append(", TO_CHAR(TO_DATE(MYOSNNSTDT, 'YYYYMMDD') + 4 DAYS, 'YYYYMMDD') as DT5");
			sbSQL.append(", TO_CHAR(TO_DATE(MYOSNNSTDT, 'YYYYMMDD') + 5 DAYS, 'YYYYMMDD') as DT6");
			sbSQL.append(", TO_CHAR(TO_DATE(MYOSNNSTDT, 'YYYYMMDD') + 6 DAYS, 'YYYYMMDD') as DT7");
			sbSQL.append(", TO_CHAR(TO_DATE(MYOSNNSTDT, 'YYYYMMDD') + 7 DAYS, 'YYYYMMDD') as DT8");
			sbSQL.append(", TO_CHAR(TO_DATE(MYOSNNSTDT, 'YYYYMMDD') + 8 DAYS, 'YYYYMMDD') as DT9");
			sbSQL.append(", TO_CHAR(TO_DATE(MYOSNNSTDT, 'YYYYMMDD') + 9 DAYS, 'YYYYMMDD') as DT10");
			sbSQL.append(", case when int (TO_CHAR(TO_DATE(MYOSNNSTDT, 'YYYYMMDD') + 0 DAYS, 'YYYYMMDD')) between NNSTDT and NNEDDT then (TO_CHAR(TO_DATE(MYOSNNSTDT, 'YYYYMMDD') + 0 DAYS, 'YYYYMMDD')) end as NDT1");
			sbSQL.append(", case when int (TO_CHAR(TO_DATE(MYOSNNSTDT, 'YYYYMMDD') + 1 DAYS, 'YYYYMMDD')) between NNSTDT and NNEDDT then (TO_CHAR(TO_DATE(MYOSNNSTDT, 'YYYYMMDD') + 1 DAYS, 'YYYYMMDD')) end as NDT2");
			sbSQL.append(", case when int (TO_CHAR(TO_DATE(MYOSNNSTDT, 'YYYYMMDD') + 2 DAYS, 'YYYYMMDD')) between NNSTDT and NNEDDT then (TO_CHAR(TO_DATE(MYOSNNSTDT, 'YYYYMMDD') + 2 DAYS, 'YYYYMMDD')) end as NDT3");
			sbSQL.append(", case when int (TO_CHAR(TO_DATE(MYOSNNSTDT, 'YYYYMMDD') + 3 DAYS, 'YYYYMMDD')) between NNSTDT and NNEDDT then (TO_CHAR(TO_DATE(MYOSNNSTDT, 'YYYYMMDD') + 3 DAYS, 'YYYYMMDD')) end as NDT4");
			sbSQL.append(", case when int (TO_CHAR(TO_DATE(MYOSNNSTDT, 'YYYYMMDD') + 4 DAYS, 'YYYYMMDD')) between NNSTDT and NNEDDT then (TO_CHAR(TO_DATE(MYOSNNSTDT, 'YYYYMMDD') + 4 DAYS, 'YYYYMMDD')) end as NDT5");
			sbSQL.append(", case when int (TO_CHAR(TO_DATE(MYOSNNSTDT, 'YYYYMMDD') + 5 DAYS, 'YYYYMMDD')) between NNSTDT and NNEDDT then (TO_CHAR(TO_DATE(MYOSNNSTDT, 'YYYYMMDD') + 5 DAYS, 'YYYYMMDD')) end as NDT6");
			sbSQL.append(", case when int (TO_CHAR(TO_DATE(MYOSNNSTDT, 'YYYYMMDD') + 6 DAYS, 'YYYYMMDD')) between NNSTDT and NNEDDT then (TO_CHAR(TO_DATE(MYOSNNSTDT, 'YYYYMMDD') + 6 DAYS, 'YYYYMMDD')) end as NDT7");
			sbSQL.append(", case when int (TO_CHAR(TO_DATE(MYOSNNSTDT, 'YYYYMMDD') + 7 DAYS, 'YYYYMMDD')) between NNSTDT and NNEDDT then (TO_CHAR(TO_DATE(MYOSNNSTDT, 'YYYYMMDD') + 7 DAYS, 'YYYYMMDD')) end as NDT8");
			sbSQL.append(", case when int (TO_CHAR(TO_DATE(MYOSNNSTDT, 'YYYYMMDD') + 8 DAYS, 'YYYYMMDD')) between NNSTDT and NNEDDT then (TO_CHAR(TO_DATE(MYOSNNSTDT, 'YYYYMMDD') + 8 DAYS, 'YYYYMMDD')) end as NDT9");
			sbSQL.append(", case when int (TO_CHAR(TO_DATE(MYOSNNSTDT, 'YYYYMMDD') + 9 DAYS, 'YYYYMMDD')) between NNSTDT and NNEDDT then (TO_CHAR(TO_DATE(MYOSNNSTDT, 'YYYYMMDD') + 9 DAYS, 'YYYYMMDD')) end as NDT10");
			sbSQL.append(" from "+szTableSHN);
			sbSQL.append(" where MOYSKBN = "+szMoyskbn);
			sbSQL.append(" and MOYSSTDT = "+szMoysstdt);
			sbSQL.append(" and MOYSRBAN = "+szMoysrban);

			if(!StringUtils.equals(DefineReport.Values.NONE.getVal(), szBumon)){
				sbSQL.append(" and BMNCD = " + szBumon);
			}
			if(!shnData.isEmpty()){
				sbSQL.append(" and BMNCD || KANRINO in ("+StringUtils.removeEnd(StringUtils.replace(StringUtils.replace(shnData.join(","),"\"0","\""),"\"",""),",")+")");
			}

			sbSQL.append(" and NVL(UPDKBN, 0) <> 1)");
			sbSQL.append(", WK as (select");
			sbSQL.append(" MOYSKBN");
			sbSQL.append(", MOYSSTDT");
			sbSQL.append(", MOYSRBAN");
			sbSQL.append(", BMNCD");
			sbSQL.append(", KANRINO");
			sbSQL.append(", KANRIENO");
			sbSQL.append(", MAX(case when IDX = 1 then (case when TJFLG = 1 then TENCD end) end) as ADT1");
			sbSQL.append(", MAX(case when IDX = 2 then (case when TJFLG = 1 then TENCD end) end) as ADT2");
			sbSQL.append(", MAX(case when IDX = 3 then (case when TJFLG = 1 then TENCD end) end) as ADT3");
			sbSQL.append(", MAX(case when IDX = 4 then (case when TJFLG = 1 then TENCD end) end) as ADT4");
			sbSQL.append(", MAX(case when IDX = 5 then (case when TJFLG = 1 then TENCD end) end) as ADT5");
			sbSQL.append(", MAX(case when IDX = 6 then (case when TJFLG = 1 then TENCD end) end) as ADT6");
			sbSQL.append(", MAX(case when IDX = 7 then (case when TJFLG = 1 then TENCD end) end) as ADT7");
			sbSQL.append(", MAX(case when IDX = 8 then (case when TJFLG = 1 then TENCD end) end) as ADT8");
			sbSQL.append(", MAX(case when IDX = 9 then (case when TJFLG = 1 then TENCD end) end) as ADT9");
			sbSQL.append(", MAX(case when IDX = 10 then (case when TJFLG = 1 then TENCD end) end) as ADT10");
			sbSQL.append(", MAX(case when IDX = 1 then (case when TJFLG = 1 then TENRANK end) end) as RNK1");
			sbSQL.append(", MAX(case when IDX = 2 then (case when TJFLG = 1 then TENRANK end) end) as RNK2");
			sbSQL.append(", MAX(case when IDX = 3 then (case when TJFLG = 1 then TENRANK end) end) as RNK3");
			sbSQL.append(", MAX(case when IDX = 4 then (case when TJFLG = 1 then TENRANK end) end) as RNK4");
			sbSQL.append(", MAX(case when IDX = 5 then (case when TJFLG = 1 then TENRANK end) end) as RNK5");
			sbSQL.append(", MAX(case when IDX = 6 then (case when TJFLG = 1 then TENRANK end) end) as RNK6");
			sbSQL.append(", MAX(case when IDX = 7 then (case when TJFLG = 1 then TENRANK end) end) as RNK7");
			sbSQL.append(", MAX(case when IDX = 8 then (case when TJFLG = 1 then TENRANK end) end) as RNK8");
			sbSQL.append(", MAX(case when IDX = 9 then (case when TJFLG = 1 then TENRANK end) end) as RNK9");
			sbSQL.append(", MAX(case when IDX = 10 then (case when TJFLG = 1 then TENRANK end) end) as RNK10");
			sbSQL.append(", MAX(case when IDX = 1 then (case when TJFLG = 2 then TENCD end) end) as DLT1");
			sbSQL.append(", MAX(case when IDX = 2 then (case when TJFLG = 2 then TENCD end) end) as DLT2");
			sbSQL.append(", MAX(case when IDX = 3 then (case when TJFLG = 2 then TENCD end) end) as DLT3");
			sbSQL.append(", MAX(case when IDX = 4 then (case when TJFLG = 2 then TENCD end) end) as DLT4");
			sbSQL.append(", MAX(case when IDX = 5 then (case when TJFLG = 2 then TENCD end) end) as DLT5");
			sbSQL.append(", MAX(case when IDX = 6 then (case when TJFLG = 2 then TENCD end) end) as DLT6");
			sbSQL.append(", MAX(case when IDX = 7 then (case when TJFLG = 2 then TENCD end) end) as DLT7");
			sbSQL.append(", MAX(case when IDX = 8 then (case when TJFLG = 2 then TENCD end) end) as DLT8");
			sbSQL.append(", MAX(case when IDX = 9 then (case when TJFLG = 2 then TENCD end) end) as DLT9");
			sbSQL.append(", MAX(case when IDX = 10 then (case when TJFLG = 2 then TENCD end) end) as DLT10");
			sbSQL.append(" from (select");
			sbSQL.append(" ROW_NUMBER() over (partition by MOYSKBN, MOYSSTDT, MOYSRBAN, BMNCD, KANRINO, KANRIENO order by MOYSKBN, MOYSSTDT, MOYSRBAN, BMNCD, KANRINO, KANRIENO, TENCD) as IDX");
			sbSQL.append(", MOYSKBN");
			sbSQL.append(", MOYSSTDT");
			sbSQL.append(", MOYSRBAN");
			sbSQL.append(", BMNCD");
			sbSQL.append(", KANRINO");
			sbSQL.append(", KANRIENO");
			sbSQL.append(", TENCD");
			sbSQL.append(", TJFLG");
			sbSQL.append(", TENRANK");
			sbSQL.append(" from "+szTableTJTEN+")");
			sbSQL.append(" group by MOYSKBN, MOYSSTDT, MOYSRBAN, BMNCD, KANRINO, KANRIENO)");
			sbSQL.append(", CAL2 as (select");
			sbSQL.append(" T1.MOYSKBN");
			sbSQL.append(", T1.MOYSSTDT");
			sbSQL.append(", T1.MOYSRBAN");
			sbSQL.append(", T1.BMNCD");
			sbSQL.append(", T1.KANRINO");
			sbSQL.append(", T1.KANRIENO");
			sbSQL.append(", MAX(case when T1.NNDT = CAL.NDT1 then T1.NNDT end) as NDT1");
			sbSQL.append(", MAX(case when T1.NNDT = CAL.NDT2 then T1.NNDT end) as NDT2");
			sbSQL.append(", MAX(case when T1.NNDT = CAL.NDT3 then T1.NNDT end) as NDT3");
			sbSQL.append(", MAX(case when T1.NNDT = CAL.NDT4 then T1.NNDT end) as NDT4");
			sbSQL.append(", MAX(case when T1.NNDT = CAL.NDT5 then T1.NNDT end) as NDT5");
			sbSQL.append(", MAX(case when T1.NNDT = CAL.NDT6 then T1.NNDT end) as NDT6");
			sbSQL.append(", MAX(case when T1.NNDT = CAL.NDT7 then T1.NNDT end) as NDT7");
			sbSQL.append(", MAX(case when T1.NNDT = CAL.NDT8 then T1.NNDT end) as NDT8");
			sbSQL.append(", MAX(case when T1.NNDT = CAL.NDT9 then T1.NNDT end) as NDT9");
			sbSQL.append(", MAX(case when T1.NNDT = CAL.NDT10 then T1.NNDT end) as NDT10");
			sbSQL.append(", MAX(case when T1.NNDT = CAL.NDT1 then T1.HTASU end) as HTS1");
			sbSQL.append(", MAX(case when T1.NNDT = CAL.NDT2 then T1.HTASU end) as HTS2");
			sbSQL.append(", MAX(case when T1.NNDT = CAL.NDT3 then T1.HTASU end) as HTS3");
			sbSQL.append(", MAX(case when T1.NNDT = CAL.NDT4 then T1.HTASU end) as HTS4");
			sbSQL.append(", MAX(case when T1.NNDT = CAL.NDT5 then T1.HTASU end) as HTS5");
			sbSQL.append(", MAX(case when T1.NNDT = CAL.NDT6 then T1.HTASU end) as HTS6");
			sbSQL.append(", MAX(case when T1.NNDT = CAL.NDT7 then T1.HTASU end) as HTS7");
			sbSQL.append(", MAX(case when T1.NNDT = CAL.NDT8 then T1.HTASU end) as HTS8");
			sbSQL.append(", MAX(case when T1.NNDT = CAL.NDT9 then T1.HTASU end) as HTS9");
			sbSQL.append(", MAX(case when T1.NNDT = CAL.NDT10 then T1.HTASU end) as HTS10");
			sbSQL.append(", MAX(case when T1.NNDT = CAL.NDT1 then T1.PTNNO end) as PTN1");
			sbSQL.append(", MAX(case when T1.NNDT = CAL.NDT2 then T1.PTNNO end) as PTN2");
			sbSQL.append(", MAX(case when T1.NNDT = CAL.NDT3 then T1.PTNNO end) as PTN3");
			sbSQL.append(", MAX(case when T1.NNDT = CAL.NDT4 then T1.PTNNO end) as PTN4");
			sbSQL.append(", MAX(case when T1.NNDT = CAL.NDT5 then T1.PTNNO end) as PTN5");
			sbSQL.append(", MAX(case when T1.NNDT = CAL.NDT6 then T1.PTNNO end) as PTN6");
			sbSQL.append(", MAX(case when T1.NNDT = CAL.NDT7 then T1.PTNNO end) as PTN7");
			sbSQL.append(", MAX(case when T1.NNDT = CAL.NDT8 then T1.PTNNO end) as PTN8");
			sbSQL.append(", MAX(case when T1.NNDT = CAL.NDT9 then T1.PTNNO end) as PTN9");
			sbSQL.append(", MAX(case when T1.NNDT = CAL.NDT10 then T1.PTNNO end) as PTN10");
			sbSQL.append(", MAX(case when T1.NNDT = CAL.NDT1 then T1.TSEIKBN end) as TSK1");
			sbSQL.append(", MAX(case when T1.NNDT = CAL.NDT2 then T1.TSEIKBN end) as TSK2");
			sbSQL.append(", MAX(case when T1.NNDT = CAL.NDT3 then T1.TSEIKBN end) as TSK3");
			sbSQL.append(", MAX(case when T1.NNDT = CAL.NDT4 then T1.TSEIKBN end) as TSK4");
			sbSQL.append(", MAX(case when T1.NNDT = CAL.NDT5 then T1.TSEIKBN end) as TSK5");
			sbSQL.append(", MAX(case when T1.NNDT = CAL.NDT6 then T1.TSEIKBN end) as TSK6");
			sbSQL.append(", MAX(case when T1.NNDT = CAL.NDT7 then T1.TSEIKBN end) as TSK7");
			sbSQL.append(", MAX(case when T1.NNDT = CAL.NDT8 then T1.TSEIKBN end) as TSK8");
			sbSQL.append(", MAX(case when T1.NNDT = CAL.NDT9 then T1.TSEIKBN end) as TSK9");
			sbSQL.append(", MAX(case when T1.NNDT = CAL.NDT10 then T1.TSEIKBN end) as TSK10");
			sbSQL.append(" from "+szTableNNDT+" T1");
			sbSQL.append(" inner join CAL on CAL.MOYSKBN = T1.MOYSKBN and CAL.MOYSSTDT = T1.MOYSSTDT and CAL.MOYSRBAN = T1.MOYSRBAN and CAL.BMNCD = T1.BMNCD and CAL.KANRINO = T1.KANRINO and CAL.KANRIENO = T1.KANRIENO");
			sbSQL.append(" group by T1.MOYSKBN, T1.MOYSSTDT, T1.MOYSRBAN, T1.BMNCD, T1.KANRINO, T1.KANRIENO)");
			sbSQL.append(" select");
			sbSQL.append(" 'U' as 更新区分 ");																											// F1 : 更新区分
			sbSQL.append(", T4.BMNCD as 部門");																											// F1 : 部門
			sbSQL.append(", left (T1.SHUNO, 2) as 週№_年");																							// F2 : 週№_年
			sbSQL.append(", right (T1.SHUNO, 2) as 週№_週");																							// F3 : 週№_週
			sbSQL.append(", T1.MOYSKBN || LPAD(T1.MOYSSTDT, 6, '0') || LPAD(T1.MOYSRBAN, 3, '0') as 催しコード");										// F4 :	催しコード
			sbSQL.append(", T1.MOYKN as 催し名称");																										// F5 :	催し名称
			sbSQL.append(", T1.HBSTDT as 催し販売期間_開始日");																							// F6 :	催し販売期間_開始日
			sbSQL.append(", T1.HBEDDT as 催し販売期間_終了日");																							// F7 :	催し販売期間_終了日
			sbSQL.append(", case when DF.DBMNATRKBN = 1 then T1.NNSTDT_TGF else T1.NNSTDT end as 催し納品期間_開始日");									// F8 :	催し納品期間_開始日
			sbSQL.append(", case when DF.DBMNATRKBN = 1 then T1.NNEDDT_TGF else T1.NNEDDT end as 催し納品期間_終了日");									// F9 :	催し納品期間_終了日
			if(isTOKTG){
				sbSQL.append(", T2.HBOKUREFLG as 催し１日遅れフラグ");																					// F10 :	催し１日遅れフラグ
			}else{
				sbSQL.append(", null as 催し１日遅れフラグ");
			}
			sbSQL.append(", T4.KANRINO as 管理番号");																										// F11 :	管理番号
			sbSQL.append(", T4.PARNO as グループ№");																										// F12 :	グループ№
			sbSQL.append(", T4.CHLDNO as 子№");																											// F13 :	子№
			sbSQL.append(", LPAD(NVL(TO_CHAR(T4.SHNCD), ''), 8, '0') as 商品コード");																		// F14 :	商品コード
			sbSQL.append(", T4.POPKN as \"ＰＯＰ名称(漢字)\"");																									// F15 :	ＰＯＰ名称(漢字)
			sbSQL.append(", T4.MAKERKN as \"メーカー名(漢字)\"");																								// F16 :	メーカー名(漢字)
			sbSQL.append(", T4.KIKKN as 規格");																												// F17 :	規格
			sbSQL.append(", T4.SANCHIKN as 産地");																											// F18 :	産地
			sbSQL.append(", T4.NAMANETUKBN as 生食加熱区分");																								// F19 :	生食加熱区分
			sbSQL.append(", T4.KAITOFLG as 解凍フラグ");																									// F20 :	解凍フラグ
			sbSQL.append(", T4.YOSHOKUFLG as 養殖フラグ");																									// F21 :	養殖フラグ
			sbSQL.append(", T4.HIGAWRFLG as 日替区分");																										// F22 :	日替区分
			sbSQL.append(", T4.HBSTDT as 販売期間_開始日");																									// F23 :	販売期間_開始日
			sbSQL.append(", T4.HBEDDT as 販売期間_終了日");																									// F24 :	販売期間_終了日
			sbSQL.append(", T4.NNSTDT as 納入期間_開始日");																									// F25 :	納入期間_開始日
			sbSQL.append(", T4.NNEDDT as 納入期間_終了日");																									// F26 :	納入期間_終了日

			if(isTOKTG){
				sbSQL.append(", T4.HBSLIDEFLG as 販売スライドフラグ");																								// F27 :	販売スライドフラグ
				sbSQL.append(", T4.NHSLIDEFLG as 納品スライドフラグ");																								// F28 :	納品スライドフラグ
				sbSQL.append(", T4.RANKNO_ADD as 特売Ａ売価店グループ");																							// F29 :	特売Ａ売価店グループ
				sbSQL.append(", null as 特売Ｂ売価店グループ");																										// F30 :	特売Ｂ売価店グループ
				sbSQL.append(", null as 特売Ｃ売価店グループ");																										// F31 :	特売Ｃ売価店グループ
				sbSQL.append(", null as 特売除外店グループ");																										// F32 :	特売除外店グループ
			}else{
				sbSQL.append(", null as 販売スライドフラグ");																										// F27 :	販売スライドフラグ
				sbSQL.append(", null as 納品スライドフラグ");																										// F28 :	納品スライドフラグ
				sbSQL.append(", T4.RANKNO_ADD_A as 特売Ａ売価店グループ");																							// F29 :	特売Ａ売価店グループ
				sbSQL.append(", T4.RANKNO_ADD_B as 特売Ｂ売価店グループ");																							// F30 :	特売Ｂ売価店グループ
				sbSQL.append(", T4.RANKNO_ADD_C as 特売Ｃ売価店グループ");																							// F31 :	特売Ｃ売価店グループ
				sbSQL.append(", T4.RANKNO_DEL as 特売除外店グループ");																								// F32 :	特売除外店グループ
			}

			sbSQL.append(", case when T4.TKANPLUKBN = 1 or T4.TKANPLUKBN is null then T4.GENKAAM_MAE else null end as 特売事前原価");								// F33 :	特売事前原価
			sbSQL.append(", case when T4.TKANPLUKBN = 1 or T4.TKANPLUKBN is null then T4.GENKAAM_ATO else null end as 特売追加原価");								// F34 :	特売追加原価
			sbSQL.append(", T4.IRISU as 特売事前入数");																												// F35 :	特売事前入数
			sbSQL.append(", case when T4.TKANPLUKBN = 1 or T4.TKANPLUKBN is null then T4.A_BAIKAAM else null end as 特売Ａ売価");									// F36 :	特売Ａ売価
			sbSQL.append(","+DefineReport.ID_SQL_TOKBAIKA_COL_HON2.replaceAll("@BAIKA", "T4.A_BAIKAAM").replaceAll("@DT", "T1.MOYSSTDT") + " as 特売Ａ本体売価");	// F37 :	特売Ａ本体売価
			sbSQL.append(", case when T4.TKANPLUKBN = 1 or T4.TKANPLUKBN is null then T4.B_BAIKAAM else null end as 特売Ｂ売価");									// F38 :	特売Ｂ売価
			sbSQL.append(","+DefineReport.ID_SQL_TOKBAIKA_COL_HON2.replaceAll("@BAIKA", "T4.B_BAIKAAM").replaceAll("@DT", "T1.MOYSSTDT") + " as 特売Ｂ本体売価");	// F39 :	特売Ｂ本体売価
			sbSQL.append(", case when T4.TKANPLUKBN = 1 or T4.TKANPLUKBN is null then T4.C_BAIKAAM else null end as 特売Ｃ売価");									// F40 :	特売Ｃ売価
			sbSQL.append(","+DefineReport.ID_SQL_TOKBAIKA_COL_HON2.replaceAll("@BAIKA", "T4.C_BAIKAAM").replaceAll("@DT", "T1.MOYSSTDT") + " as 特売Ｃ本体売価");	// F41 :	特売Ｃ本体売価
			sbSQL.append(", case when T4.ADDSHUKBN in (4, 5) then T4.TKANPLUKBN else null end as 定貫ＰＬＵ・不定貫区分");											// F42 :	定貫ＰＬＵ・不定貫区分
			sbSQL.append(", case when T4.ADDSHUKBN in (4, 5) then T4.A_BAIKAAM else null end as 特売Ａ100ｇ売価");												// F43 :	特売Ａ100ｇ売価
			sbSQL.append(", case when T4.ADDSHUKBN in (4, 5) then T4.A_GENKAAM_1KG else null end as 特売Ａ1㎏売価");											// F44 :	特売Ａ1㎏売価
			sbSQL.append(", case when T4.ADDSHUKBN in (4, 5) then T4.A_BAIKAAM_PACK else null end as 特売ＡP売価");												// F45 :	特売ＡP売価
			sbSQL.append(", case when T4.ADDSHUKBN in (4, 5) then T4.B_BAIKAAM else null end as 特売Ｂ100ｇ売価");												// F46 :	特売Ｂ100ｇ売価
			sbSQL.append(", case when T4.ADDSHUKBN in (4, 5) then T4.B_GENKAAM_1KG else null end as 特売Ｂ1㎏売価");											// F47 :	特売Ｂ1㎏売価
			sbSQL.append(", case when T4.ADDSHUKBN in (4, 5) then T4.B_BAIKAAM_PACK else null end as 特売ＢP売価");												// F48 :	特売ＢP売価
			sbSQL.append(", case when T4.ADDSHUKBN in (4, 5) then T4.C_BAIKAAM else null end as 特売Ｃ100ｇ売価");												// F49 :	特売Ｃ100ｇ売価
			sbSQL.append(", case when T4.ADDSHUKBN in (4, 5) then T4.C_GENKAAM_1KG else null end as 特売Ｃ1㎏売価");											// F50 :	特売Ｃ1㎏売価
			sbSQL.append(", case when T4.ADDSHUKBN in (4, 5) then T4.C_BAIKAAM_PACK else null end as 特売ＣP売価");												// F51 :	特売ＣP売価
			sbSQL.append(", case when T4.ADDSHUKBN <> 5 then T4.A_BAIKAAM_100G else null end as Ａ売価_100ｇ相当");												// F52 :	Ａ売価_100ｇ相当
			sbSQL.append(", case when T4.ADDSHUKBN <> 5 then T4.B_BAIKAAM_100G else null end as Ｂ売価_100ｇ相当");												// F53 :	Ｂ売価_100ｇ相当
			sbSQL.append(", case when T4.ADDSHUKBN <> 5 then T4.C_BAIKAAM_100G else null end as Ｃ売価_100ｇ相当");												// F54 :	Ｃ売価_100ｇ相当
			sbSQL.append(", case when T4.ADDSHUKBN in (4, 5) then T4.GENKAAM_1KG else null end as \"1㎏原価\"");													// F55 :	1㎏原価
			sbSQL.append(", case when T4.ADDSHUKBN in (4, 5) then T4.GENKAAM_PACK else null end as P原価");														// F56 :	P原価
			sbSQL.append(", case when T4.ADDSHUKBN <> 1 then T4.A_WRITUKBN else null end as Ａ割引率");															// F57 :	Ａ割引率
			sbSQL.append(", case when T4.ADDSHUKBN <> 1 then T4.B_WRITUKBN else null end as Ｂ割引率");															// F58 :	Ｂ割引率
			sbSQL.append(", case when T4.ADDSHUKBN <> 1 then T4.C_WRITUKBN else null end as Ｃ割引率");															// F59 :	Ｃ割引率
			sbSQL.append(", T4.HBYOTEISU as 予定数");																											// F60 :	予定数
			sbSQL.append(", case when T4.ADDSHUKBN <> 1 then T4.YORIFLG else null end as よりどりフラグ");														// F61 :	よりどりフラグ
			sbSQL.append(", T4.KO_A_BAIKAAN as １個売売価Ａ");																									// F62 :	１個売売価Ａ
			sbSQL.append(", T4.KO_B_BAIKAAN as １個売売価Ｂ");																									// F63 :	１個売売価Ｂ
			sbSQL.append(", T4.KO_C_BAIKAAN as １個売売価Ｃ");																									// F64 :	１個売売価Ｃ
			sbSQL.append(", T4.BD1_TENSU as バンドル１点数");																									// F65 :	バンドル１点数
			sbSQL.append(", T4.BD1_A_BAIKAAN as バンドル１売価Ａ");																								// F66 :	バンドル１売価Ａ
			sbSQL.append(", T4.BD1_B_BAIKAAN as バンドル１売価Ｂ");																								// F67 :	バンドル１売価Ｂ
			sbSQL.append(", T4.BD1_C_BAIKAAN as バンドル１売価Ｃ");																								// F68 :	バンドル１売価Ｃ
			sbSQL.append(", T4.BD2_TENSU as バンドル２点数");																									// F69 :	バンドル２点数
			sbSQL.append(", T4.BD2_A_BAIKAAN as バンドル２売価Ａ");																								// F70 :	バンドル２売価Ａ
			sbSQL.append(", T4.BD2_B_BAIKAAN as バンドル２売価Ｂ");																								// F71 :	バンドル２売価Ｂ
			sbSQL.append(", T4.BD2_C_BAIKAAN as バンドル２売価Ｃ");																								// F72 :	バンドル２売価Ｃ
			sbSQL.append(", T4.MEDAMAKBN as 目玉");																												// F73 :	目玉
			sbSQL.append(", T4.COMMENT_POP as ＰＯＰコメント");																									// F74 :	ＰＯＰコメント
			sbSQL.append(", T4.COMMENT_TB as 特売コメント");																									// F75 :	特売コメント
			sbSQL.append(", T4.COMMENT_HGW as その他日替コメント");																								// F76 :	その他日替コメント
			sbSQL.append(", T4.SEGN_NINZU as 制限_先着人数");																									// F77 :	制限_先着人数
			sbSQL.append(", T4.SEGN_GENTEI as 制限_限定表現");																									// F78 :	制限_限定表現
			sbSQL.append(", T4.SEGN_1KOSU as 制限_一人");																										// F79 :	制限_一人
			sbSQL.append(", T4.SEGN_1KOSUTNI as 制限_単位");																									// F80 :	制限_単位
			sbSQL.append(", T4.CUTTENFLG as カット店展開フラグ");																								// F81 :	カット店展開フラグ
			sbSQL.append(", T4.CHIRASFLG as チラシ未掲載フラグ");																								// F82 :	チラシ未掲載フラグ
			sbSQL.append(", T4.PLUSNDFLG as ＰＬＵ未配信フラグ");																								// F83 :	ＰＬＵ未配信フラグ
			sbSQL.append(", T4.HTGENBAIKAFLG as 発注原売価適用フラグ");																							// F84 :	発注原売価適用フラグ
			sbSQL.append(", case when T4.ADDSHUKBN = 1 then '1' else '0' end as 全品割引区分");																	// F85 :	全品割引区分
			sbSQL.append(", case when int (T4.MYOSNNSTDT) between case when DF.DBMNATRKBN = 1 then T1.NNSTDT_TGF else T1.NNSTDT end and case when DF.DBMNATRKBN = 1 then T1.NNEDDT_TGF else T1.NNEDDT end then T4.MYOSNNSTDT else null end as 催し日付1");	// F86 :	催し日付1
			sbSQL.append(", case when int (CAL.DT2) between case when DF.DBMNATRKBN = 1 then T1.NNSTDT_TGF else T1.NNSTDT end and case when DF.DBMNATRKBN = 1 then T1.NNEDDT_TGF else T1.NNEDDT end then CAL.DT2 else null end as 催し日付2");				// F87 :	催し日付2
			sbSQL.append(", case when int (CAL.DT3) between case when DF.DBMNATRKBN = 1 then T1.NNSTDT_TGF else T1.NNSTDT end and case when DF.DBMNATRKBN = 1 then T1.NNEDDT_TGF else T1.NNEDDT end then CAL.DT3 else null end as 催し日付3");				// F88 :	催し日付3
			sbSQL.append(", case when int (CAL.DT4) between case when DF.DBMNATRKBN = 1 then T1.NNSTDT_TGF else T1.NNSTDT end and case when DF.DBMNATRKBN = 1 then T1.NNEDDT_TGF else T1.NNEDDT end then CAL.DT4 else null end as 催し日付4");				// F89 :	催し日付4
			sbSQL.append(", case when int (CAL.DT5) between case when DF.DBMNATRKBN = 1 then T1.NNSTDT_TGF else T1.NNSTDT end and case when DF.DBMNATRKBN = 1 then T1.NNEDDT_TGF else T1.NNEDDT end then CAL.DT5 else null end as 催し日付5");				// F90 :	催し日付5
			sbSQL.append(", case when int (CAL.DT6) between case when DF.DBMNATRKBN = 1 then T1.NNSTDT_TGF else T1.NNSTDT end and case when DF.DBMNATRKBN = 1 then T1.NNEDDT_TGF else T1.NNEDDT end then CAL.DT6 else null end as 催し日付6");				// F91 :	催し日付6
			sbSQL.append(", case when int (CAL.DT7) between case when DF.DBMNATRKBN = 1 then T1.NNSTDT_TGF else T1.NNSTDT end and case when DF.DBMNATRKBN = 1 then T1.NNEDDT_TGF else T1.NNEDDT end then CAL.DT7 else null end as 催し日付7");				// F92 :	催し日付7
			sbSQL.append(", case when int (CAL.DT8) between case when DF.DBMNATRKBN = 1 then T1.NNSTDT_TGF else T1.NNSTDT end and case when DF.DBMNATRKBN = 1 then T1.NNEDDT_TGF else T1.NNEDDT end then CAL.DT8 else null end as 催し日付8");				// F93 :	催し日付8
			sbSQL.append(", case when int (CAL.DT9) between case when DF.DBMNATRKBN = 1 then T1.NNSTDT_TGF else T1.NNSTDT end and case when DF.DBMNATRKBN = 1 then T1.NNEDDT_TGF else T1.NNEDDT end then CAL.DT9 else null end as 催し日付9");				// F94 :	催し日付9
			sbSQL.append(", case when int (CAL.DT10) between case when DF.DBMNATRKBN = 1 then T1.NNSTDT_TGF else T1.NNSTDT end and case when DF.DBMNATRKBN = 1 then T1.NNEDDT_TGF else T1.NNEDDT end then CAL.DT10 else null end as 催し日付10");			// F95 :	催し日付10
			sbSQL.append(", case when int (T4.MYOSNNSTDT) between T4.HBSTDT and T4.HBEDDT then 1 else null end as 販売対象1");								// F96 :	販売対象1
			sbSQL.append(", case when int (CAL.DT2) between T4.HBSTDT and T4.HBEDDT then 1 else null end as 販売対象2");									// F97 :	販売対象2
			sbSQL.append(", case when int (CAL.DT3) between T4.HBSTDT and T4.HBEDDT then 1 else null end as 販売対象3");									// F98 :	販売対象3
			sbSQL.append(", case when int (CAL.DT4) between T4.HBSTDT and T4.HBEDDT then 1 else null end as 販売対象4");									// F99 :	販売対象4
			sbSQL.append(", case when int (CAL.DT5) between T4.HBSTDT and T4.HBEDDT then 1 else null end as 販売対象5");									// F100 :	販売対象5
			sbSQL.append(", case when int (CAL.DT6) between T4.HBSTDT and T4.HBEDDT then 1 else null end as 販売対象6");									// F101 :	販売対象6
			sbSQL.append(", case when int (CAL.DT7) between T4.HBSTDT and T4.HBEDDT then 1 else null end as 販売対象7");									// F102 :	販売対象7
			sbSQL.append(", case when int (CAL.DT8) between T4.HBSTDT and T4.HBEDDT then 1 else null end as 販売対象8");									// F103 :	販売対象8
			sbSQL.append(", case when int (CAL.DT9) between T4.HBSTDT and T4.HBEDDT then 1 else null end as 販売対象9");									// F104 :	販売対象9
			sbSQL.append(", case when int (CAL.DT10) between T4.HBSTDT and T4.HBEDDT then 1 else null end as 販売対象10");									// F105 :	販売対象10
			sbSQL.append(", case when CAL2.NDT1 is not null then 1 else null end as 納品対象1");															// F106 :	納品対象1
			sbSQL.append(", case when CAL2.NDT2 is not null then 1 else null end as 納品対象2");															// F107 :	納品対象2
			sbSQL.append(", case when CAL2.NDT3 is not null then 1 else null end as 納品対象3");															// F108 :	納品対象3
			sbSQL.append(", case when CAL2.NDT4 is not null then 1 else null end as 納品対象4");															// F109 :	納品対象4
			sbSQL.append(", case when CAL2.NDT5 is not null then 1 else null end as 納品対象5");															// F110 :	納品対象5
			sbSQL.append(", case when CAL2.NDT6 is not null then 1 else null end as 納品対象6");															// F111 :	納品対象6
			sbSQL.append(", case when CAL2.NDT7 is not null then 1 else null end as 納品対象7");															// F112 :	納品対象7
			sbSQL.append(", case when CAL2.NDT8 is not null then 1 else null end as 納品対象8");															// F113 :	納品対象8
			sbSQL.append(", case when CAL2.NDT9 is not null then 1 else null end as 納品対象9");															// F114 :	納品対象9
			sbSQL.append(", case when CAL2.NDT10 is not null then 1 else null end as 納品対象10");															// F115 :	納品対象10
			sbSQL.append(", case when T4.TENKAIKBN = '2' then null when CAL2.NDT1 is not null or int (T4.NNSTDT) between T4.HBSTDT and T4.HBEDDT then CAL2.HTS1 end as 発注総数1");			// F116 :	発注総数1
			sbSQL.append(", case when T4.TENKAIKBN = '2' then null when CAL2.NDT2 is not null or int (CAL.DT2) between T4.HBSTDT and T4.HBEDDT then CAL2.HTS2 end as 発注総数2");			// F117 :	発注総数2
			sbSQL.append(", case when T4.TENKAIKBN = '2' then null when CAL2.NDT3 is not null or int (CAL.DT3) between T4.HBSTDT and T4.HBEDDT then CAL2.HTS3 end as 発注総数3");			// F118 :	発注総数3
			sbSQL.append(", case when T4.TENKAIKBN = '2' then null when CAL2.NDT4 is not null or int (CAL.DT4) between T4.HBSTDT and T4.HBEDDT then CAL2.HTS4 end as 発注総数4");			// F119 :	発注総数4
			sbSQL.append(", case when T4.TENKAIKBN = '2' then null when CAL2.NDT5 is not null or int (CAL.DT5) between T4.HBSTDT and T4.HBEDDT then CAL2.HTS5 end as 発注総数5");			// F120 :	発注総数5
			sbSQL.append(", case when T4.TENKAIKBN = '2' then null when CAL2.NDT6 is not null or int (CAL.DT6) between T4.HBSTDT and T4.HBEDDT then CAL2.HTS6 end as 発注総数6");			// F121 :	発注総数6
			sbSQL.append(", case when T4.TENKAIKBN = '2' then null when CAL2.NDT7 is not null or int (CAL.DT7) between T4.HBSTDT and T4.HBEDDT then CAL2.HTS7 end as 発注総数7");			// F122 :	発注総数7
			sbSQL.append(", case when T4.TENKAIKBN = '2' then null when CAL2.NDT8 is not null or int (CAL.DT8) between T4.HBSTDT and T4.HBEDDT then CAL2.HTS8 end as 発注総数8");			// F123 :	発注総数8
			sbSQL.append(", case when T4.TENKAIKBN = '2' then null when CAL2.NDT9 is not null or int (CAL.DT9) between T4.HBSTDT and T4.HBEDDT then CAL2.HTS9 end as 発注総数9");			// F124 :	発注総数9
			sbSQL.append(", case when T4.TENKAIKBN = '2' then null when CAL2.NDT10 is not null or int (CAL.DT10) between T4.HBSTDT and T4.HBEDDT then CAL2.HTS10 end as 発注総数10");		// F125 :	発注総数10
			sbSQL.append(", case when CAL2.NDT1 is not null or int (T4.NNSTDT) between T4.HBSTDT and T4.HBEDDT then CAL2.PTN1 end as パタン№1");		// F126 :	パタン№1
			sbSQL.append(", case when CAL2.NDT2 is not null or int (CAL.DT2) between T4.HBSTDT and T4.HBEDDT then CAL2.PTN2 end as パタン№2");			// F127 :	パタン№2
			sbSQL.append(", case when CAL2.NDT3 is not null or int (CAL.DT3) between T4.HBSTDT and T4.HBEDDT then CAL2.PTN3 end as パタン№3");			// F128 :	パタン№3
			sbSQL.append(", case when CAL2.NDT4 is not null or int (CAL.DT4) between T4.HBSTDT and T4.HBEDDT then CAL2.PTN4 end as パタン№4");			// F129 :	パタン№4
			sbSQL.append(", case when CAL2.NDT5 is not null or int (CAL.DT5) between T4.HBSTDT and T4.HBEDDT then CAL2.PTN5 end as パタン№5");			// F130 :	パタン№5
			sbSQL.append(", case when CAL2.NDT6 is not null or int (CAL.DT6) between T4.HBSTDT and T4.HBEDDT then CAL2.PTN6 end as パタン№6");			// F131 :	パタン№6
			sbSQL.append(", case when CAL2.NDT7 is not null or int (CAL.DT7) between T4.HBSTDT and T4.HBEDDT then CAL2.PTN7 end as パタン№7");			// F132 :	パタン№7
			sbSQL.append(", case when CAL2.NDT8 is not null or int (CAL.DT8) between T4.HBSTDT and T4.HBEDDT then CAL2.PTN8 end as パタン№8");			// F133 :	パタン№8
			sbSQL.append(", case when CAL2.NDT9 is not null or int (CAL.DT9) between T4.HBSTDT and T4.HBEDDT then CAL2.PTN9 end as パタン№9");			// F134 :	パタン№9
			sbSQL.append(", case when CAL2.NDT10 is not null or int (CAL.DT10) between T4.HBSTDT and T4.HBEDDT then CAL2.PTN10 end as パタン№10");		// F135 :	パタン№10
			sbSQL.append(", case when CAL2.NDT1 is not null or int (T4.NNSTDT) between T4.HBSTDT and T4.HBEDDT then CAL2.TSK1 end as 訂正区分1");		// F136 :	訂正区分1
			sbSQL.append(", case when CAL2.NDT2 is not null or int (CAL.DT2) between T4.HBSTDT and T4.HBEDDT then CAL2.TSK2 end as 訂正区分2");			// F137 :	訂正区分2
			sbSQL.append(", case when CAL2.NDT3 is not null or int (CAL.DT3) between T4.HBSTDT and T4.HBEDDT then CAL2.TSK3 end as 訂正区分3");			// F138 :	訂正区分3
			sbSQL.append(", case when CAL2.NDT4 is not null or int (CAL.DT4) between T4.HBSTDT and T4.HBEDDT then CAL2.TSK4 end as 訂正区分4");			// F139 :	訂正区分4
			sbSQL.append(", case when CAL2.NDT5 is not null or int (CAL.DT5) between T4.HBSTDT and T4.HBEDDT then CAL2.TSK5 end as 訂正区分5");			// F140 :	訂正区分5
			sbSQL.append(", case when CAL2.NDT6 is not null or int (CAL.DT6) between T4.HBSTDT and T4.HBEDDT then CAL2.TSK6 end as 訂正区分6");			// F141 :	訂正区分6
			sbSQL.append(", case when CAL2.NDT7 is not null or int (CAL.DT7) between T4.HBSTDT and T4.HBEDDT then CAL2.TSK7 end as 訂正区分7");			// F142 :	訂正区分7
			sbSQL.append(", case when CAL2.NDT8 is not null or int (CAL.DT8) between T4.HBSTDT and T4.HBEDDT then CAL2.TSK8 end as 訂正区分8");			// F143 :	訂正区分8
			sbSQL.append(", case when CAL2.NDT9 is not null or int (CAL.DT9) between T4.HBSTDT and T4.HBEDDT then CAL2.TSK9 end as 訂正区分9");			// F144 :	訂正区分9
			sbSQL.append(", case when CAL2.NDT10 is not null or int (CAL.DT10) between T4.HBSTDT and T4.HBEDDT then CAL2.TSK10 end as 訂正区分10");		// F145 :	訂正区分10
			sbSQL.append(", WK.ADT1 as 追加店1, WK.ADT2 as 追加店2, WK.ADT3 as 追加店3, WK.ADT4 as 追加店4, WK.ADT5 as 追加店5, WK.ADT6 as 追加店6, WK.ADT7 as 追加店7, WK.ADT8 as 追加店8, WK.ADT9 as 追加店9, WK.ADT10 as 追加店10");	// F146~F155 :	追加店1~10
			sbSQL.append(", WK.RNK1 as 追加店ランク1, WK.RNK2 as 追加店ランク2, WK.RNK3 as 追加店ランク3, WK.RNK4 as 追加店ランク4, WK.RNK5 as 追加店ランク5, WK.RNK6 as 追加店ランク6, WK.RNK7 as 追加店ランク7, WK.RNK8 as 追加店ランク8, WK.RNK9 as 追加店ランク9, WK.RNK10 as 追加店ランク10");	// F156~F165 :	追加店ランク1~10
			sbSQL.append(", WK.DLT1 as 除外店1, WK.DLT2 as 除外店2, WK.DLT3 as 除外店3, WK.DLT4 as 除外店4, WK.DLT5 as 除外店5, WK.DLT6 as 除外店6, WK.DLT7 as 除外店7, WK.DLT8 as 除外店8, WK.DLT9 as 除外店9, WK.DLT10 as 除外店10");	// F166~F175 :	除外店1~10
			sbSQL.append(", T4.BINKBN as 便区分");																											// F176 :	便区分
			sbSQL.append(", T4.BDENKBN as 別伝区分");																										// F177 :	別伝区分
			sbSQL.append(", T4.WAPPNKBN as ワッペン区分");																									// F178 :	ワッペン区分
			sbSQL.append(", T4.JUFLG as 事前打出区分");																										// F179 :	事前打出区分
			sbSQL.append(", T4.JUHTDT as 事前打出発注日");																									// F180 :	事前打出発注日
			sbSQL.append(", T4.SHUDENFLG as 週次伝送区分");																									// F181 :	週次伝送区分
			sbSQL.append(", T4.POPCD as ＰＯＰコード");																										// F182 :	ＰＯＰコード
			sbSQL.append(", T4.POPSU as ＰＯＰ枚数");																										// F183 :	ＰＯＰ枚数
			sbSQL.append(", T4.POPSZ as ＰＯＰサイズ");																										// F184 :	ＰＯＰサイズ
			sbSQL.append(", T4.SHNSIZE as 商品サイズ");																										// F185 :	商品サイズ
			sbSQL.append(", T4.SHNCOLOR as 商品色");																										// F186 :	商品色
			sbSQL.append(", T4.TENKAIKBN as ﾊﾟﾀｰﾝ種類");																									// F187 :	ﾊﾟﾀｰﾝ種類
			sbSQL.append(", T4.JSKPTNSYUKBN as 実績率PT数値");																								// F188 :	実績率PT数値
			sbSQL.append(", T4.JSKPTNZNENWKBN as 実績率PT前年同週");																						// F189 :	実績率PT前年同週
			sbSQL.append(", T4.JSKPTNZNENMKBN as 実績率PT前年同月");																						// F190 :	実績率PT前年同月
			sbSQL.append(", T4.BYCD as バイヤーコード");																									// F191 :	バイヤーコード
			sbSQL.append(", T4.OPERATOR as オペレータ");																									// F192 :	オペレータ
			sbSQL.append(", TO_CHAR(T4.ADDDT, 'yyyymmdd') as 登録日");																						// F193 :	登録日
			sbSQL.append(", TO_CHAR(current timestamp, 'yyyymmdd') as 更新日");																				// F194 :	更新日

			sbSQL.append(" from INATK.TOKMOYCD T1");
			if(isTOKTG){
				sbSQL.append(" inner join INATK.TOKTG_KHN T2 on T2.MOYSKBN = T1.MOYSKBN and T2.MOYSSTDT = T1.MOYSSTDT and T2.MOYSRBAN = T1.MOYSRBAN and NVL(T2.UPDKBN, 0) <> 1");
			}
			sbSQL.append(" inner join "+szTableBMN+" T3 on T3.MOYSKBN = T1.MOYSKBN and T3.MOYSSTDT = T1.MOYSSTDT and T3.MOYSRBAN = T1.MOYSRBAN and NVL(T3.UPDKBN, 0) <> 1 and NVL(T1.UPDKBN, 0) <> 1");
			sbSQL.append(" inner join "+szTableSHN+" T4 on T4.MOYSKBN = T3.MOYSKBN and T4.MOYSSTDT = T3.MOYSSTDT and T4.MOYSRBAN = T3.MOYSRBAN and T4.MOYSRBAN = T3.MOYSRBAN and T4.BMNCD = T3.BMNCD and NVL(T4.UPDKBN, 0) <> 1");
			sbSQL.append(" inner join CAL on T4.MOYSKBN = CAL.MOYSKBN and T4.MOYSSTDT = CAL.MOYSSTDT and T4.MOYSRBAN = CAL.MOYSRBAN and T4.BMNCD = CAL.BMNCD and T4.KANRINO = CAL.KANRINO and T4.KANRIENO = CAL.KANRIENO");
			sbSQL.append(" left outer join CAL2 on CAL2.MOYSKBN = T4.MOYSKBN and CAL2.MOYSSTDT = T4.MOYSSTDT and CAL2.MOYSRBAN = T4.MOYSRBAN and CAL2.BMNCD = T4.BMNCD and CAL2.KANRINO = T4.KANRINO and CAL2.KANRIENO = T4.KANRIENO");
			sbSQL.append(" left outer join WK on WK.MOYSKBN = T4.MOYSKBN and WK.MOYSSTDT = T4.MOYSSTDT and WK.MOYSRBAN = T4.MOYSRBAN and WK.BMNCD = T4.BMNCD and WK.KANRINO = T4.KANRINO and WK.KANRIENO = T4.KANRIENO");
			sbSQL.append(" left outer join INAMS.MSTSHN M0 on M0.SHNCD = T4.SHNCD and NVL(M0.UPDKBN, 0) <> 1 left outer join INAMS.MSTBMN M1 on M1.BMNCD = T4.BMNCD and NVL(M1.UPDKBN, 0) <> 1");
			sbSQL.append(" left outer join INAMS.MSTZEIRT M2 on M2.ZEIRTKBN = M0.ZEIRTKBN and NVL(M2.UPDKBN, 0) <> 1");
			sbSQL.append(" left outer join INAMS.MSTZEIRT M3 on M3.ZEIRTKBN = M0.ZEIRTKBN_OLD and NVL(M3.UPDKBN, 0) <> 1");
			sbSQL.append(" left outer join INAMS.MSTZEIRT M4 on M4.ZEIRTKBN = M1.ZEIRTKBN and NVL(M4.UPDKBN, 0) <> 1");
			sbSQL.append(" left outer join INAMS.MSTZEIRT M5 on M5.ZEIRTKBN = M1.ZEIRTKBN_OLD and NVL(M5.UPDKBN, 0) <> 1");
			sbSQL.append(" left join INATK.TOKMOYDEF DF on T4.BMNCD = DF.BMNCD and nvl(DF.UPDKBN, 0) <> 1");
			sbSQL.append(" order by T4.BMNCD, T1.MOYSSTDT, T1.MOYSSTDT, T1.MOYSKBN, T4.KANRINO, T4.KANRIENO");

		}else{

			// 店別数量CSVボタン押下時
			sbSQL.append(" with WK as (select");
			sbSQL.append(" MOYSKBN");
			sbSQL.append(", MOYSSTDT");
			sbSQL.append(", MOYSRBAN");
			sbSQL.append(", BMNCD");
			sbSQL.append(", KANRINO");
			sbSQL.append(", KANRIENO");
			sbSQL.append(", NNDT");
			sbSQL.append(", OPERATOR");
			sbSQL.append(", ADDDT");
			sbSQL.append(", UPDDT");
			sbSQL.append(", TENHTSU_ARR as ARR");
			sbSQL.append(", 5 as LEN");
			sbSQL.append(" from "+szTableNNDT);
			sbSQL.append(" where MOYSKBN = "+szMoyskbn);
			sbSQL.append(" and MOYSSTDT = "+szMoysstdt);
			sbSQL.append(" and MOYSRBAN = "+szMoysrban);

			if(!StringUtils.equals(DefineReport.Values.NONE.getVal(), szBumon)){
				sbSQL.append(" and BMNCD = " + szBumon);
			}
			if(!shnData.isEmpty()){
				sbSQL.append(" and BMNCD || KANRINO in ("+StringUtils.removeEnd(StringUtils.replace(StringUtils.replace(shnData.join(","),"\"0","\""),"\"",""),",")+")");
			}

			sbSQL.append(" ), ARRWK(IDX, MOYSKBN, MOYSSTDT, MOYSRBAN, BMNCD, KANRINO, KANRIENO, NNDT, OPERATOR, ADDDT, UPDDT, SURYO, S, ARR, LEN) as (select");
			sbSQL.append(" 1");
			sbSQL.append(", MOYSKBN");
			sbSQL.append(", MOYSSTDT");
			sbSQL.append(", MOYSRBAN");
			sbSQL.append(", BMNCD");
			sbSQL.append(", KANRINO");
			sbSQL.append(", KANRIENO");
			sbSQL.append(", NNDT");
			sbSQL.append(", OPERATOR");
			sbSQL.append(", ADDDT");
			sbSQL.append(", UPDDT");
			sbSQL.append(", SUBSTR(ARR, 1, LEN)");
			sbSQL.append(", 1 + LEN");
			sbSQL.append(", ARR");
			sbSQL.append(", LEN");
			sbSQL.append(" from WK");
			sbSQL.append(" union all select");
			sbSQL.append(" IDX + 1");
			sbSQL.append(", MOYSKBN");
			sbSQL.append(", MOYSSTDT");
			sbSQL.append(", MOYSRBAN");
			sbSQL.append(", BMNCD");
			sbSQL.append(", KANRINO");
			sbSQL.append(", KANRIENO");
			sbSQL.append(", NNDT");
			sbSQL.append(", OPERATOR");
			sbSQL.append(", ADDDT");
			sbSQL.append(", UPDDT");
			sbSQL.append(", SUBSTR(ARR, S, LEN)");
			sbSQL.append(", S + LEN");
			sbSQL.append(", ARR");
			sbSQL.append(", LEN");
			sbSQL.append(" from ARRWK");
			sbSQL.append(" where S + LEN < LENGTH(ARR)");
			sbSQL.append(")");
			sbSQL.append(" select");
			sbSQL.append(" 'U' as 更新区分 ");																// F0	: 更新区分
			sbSQL.append(", T5.MOYSKBN || T5.MOYSSTDT || right ('000' || T5.MOYSRBAN, 3) as 催しコード");	// F1	: 催しコード
			sbSQL.append(", T1.MOYKN AS 催し名称");															// F2	: 催し名称
			sbSQL.append(", T4.HBSTDT AS 催し販売期間_開始日");												// F3	: 催し販売期間_開始日
			sbSQL.append(", T4.HBEDDT AS 催し販売期間_終了日");												// F4	: 催し販売期間_終了日
			sbSQL.append(", T5.BMNCD AS 部門");																// F5	: 部門
			sbSQL.append(", T5.KANRINO AS 管理番号");														// F6	: 管理番号
			sbSQL.append(", T4.SHNCD AS 商品コード");														// F7	: 商品コード
			sbSQL.append(", T4.POPKN AS \"ＰＯＰ名称(漢字)\"");													// F8	: ＰＯＰ名称(漢字)
			sbSQL.append(", T5.NNDT AS 納入日");															// F9	: 納入日
			sbSQL.append(", right ('000' || M1.TENCD, 3) as 店番");											// F10	: 店番
			sbSQL.append(", case");																			// F11	: 店舗名称
			sbSQL.append("  when T5.SURYO is null or M2.TENCD is null or M1.MISEUNYOKBN = 9 then null");
			sbSQL.append("  else M1.TENKN end as 店舗名称");
			sbSQL.append(", int (NVL(T5.SURYO, 0)) as 数量");												// F12	: 数量
			sbSQL.append(", T4.SANCHIKN AS 産地");															// F13	: 産地
			sbSQL.append(", T4.MAKERKN AS メーカー名");														// F14	: メーカー名
			sbSQL.append(", T4.KIKKN AS 規格");																// F15	: 規格
			sbSQL.append(", T4.DAICD AS 大分類コード");														// F16	: 大分類コード
			sbSQL.append(", M3.DAIBRUIKN AS 大分類名称");													// F17	: 大分類名称
			sbSQL.append(", T4.CHUCD AS 中分類コード");														// F18	: 中分類コード
			sbSQL.append(", M4.CHUBRUIKN AS 中分類名称");													// F19	: 中分類名称
			sbSQL.append(", M5.SHOCD AS 小分類コード");														// F20	: 小分類コード
			sbSQL.append(", M6.SHOBRUIKN AS 小分類名称");													// F21	: 小分類名称
			sbSQL.append(", T5.OPERATOR AS オペレータ");													// F22	: オペレータ
			sbSQL.append(", TO_CHAR(T5.ADDDT, 'yyyymmdd') AS 登録日");										// F23	: 登録日
			sbSQL.append(", TO_CHAR(current timestamp, 'yyyymmdd') AS 更新日");								// F24	: 更新日
			sbSQL.append(" from INATK.TOKMOYCD T1");
			if(isTOKTG){
				sbSQL.append(" inner join INATK.TOKTG_KHN T2 on T2.MOYSKBN = T1.MOYSKBN and T2.MOYSSTDT = T1.MOYSSTDT and T2.MOYSRBAN = T1.MOYSRBAN and NVL(T2.UPDKBN, 0) <> 1");
			}
			sbSQL.append(" inner join "+szTableBMN+" T3 on T3.MOYSKBN = T1.MOYSKBN and T3.MOYSSTDT = T1.MOYSSTDT and T3.MOYSRBAN = T1.MOYSRBAN and NVL(T3.UPDKBN, 0) <> 1 and NVL(T1.UPDKBN, 0) <> 1");
			sbSQL.append(" inner join "+szTableSHN+" T4 on T4.MOYSKBN = T3.MOYSKBN and T4.MOYSSTDT = T3.MOYSSTDT and T4.MOYSRBAN = T3.MOYSRBAN and T4.MOYSRBAN = T3.MOYSRBAN and T4.BMNCD = T3.BMNCD and NVL(T4.UPDKBN, 0) <> 1");
			sbSQL.append(" inner join ARRWK T5 on T5.MOYSKBN = T4.MOYSKBN and T5.MOYSSTDT = T4.MOYSSTDT and T5.MOYSRBAN = T4.MOYSRBAN and T5.MOYSRBAN = T4.MOYSRBAN and T5.BMNCD = T4.BMNCD and T5.KANRINO = T4.KANRINO and T5.KANRIENO = T4.KANRIENO and NVL(T4.UPDKBN, 0) <> 1");
			sbSQL.append(" inner join INAMS.MSTTEN M1 on T5.IDX = M1.TENCD and LENGTH(TRIM(T5.SURYO)) > 0 left join (select * from INAMS.MSTTENBMN where BMNCD = 1) M2 on M2.TENCD = M1.TENCD inner join INAMS.MSTDAIBRUI M3 on M3.BMNCD = T4.BMNCD and M3.DAICD = T4.DAICD and NVL(M3.UPDKBN, 0) <> 1");
			sbSQL.append(" left outer join INAMS.MSTCHUBRUI M4 on M4.BMNCD = T4.BMNCD and M4.DAICD = T4.DAICD and M4.CHUCD = T4.CHUCD and NVL(M4.UPDKBN, 0) <> 1");
			sbSQL.append(" inner join INAMS.MSTSHN M5 on M5.SHNCD = T4.SHNCD and NVL(M5.UPDKBN, 0) <> 1");
			sbSQL.append(" left outer join INAMS.MSTSHOBRUI M6 on M6.BMNCD = T4.BMNCD and M6.DAICD = T4.DAICD and M6.CHUCD = T4.CHUCD and M6.SHOCD = M5.SHOCD and NVL(M6.UPDKBN, 0) <> 1 ");
			sbSQL.append(" order by T5.MOYSKBN, T5.MOYSSTDT, T5.MOYSRBAN, T5.BMNCD, T5.KANRINO, T5.NNDT, T4.SHNCD, M1.TENCD");
		}

		// オプション情報設定
		JSONObject option = new JSONObject();
		option.put("FILE_NAME", cfi.geFnm());
		setOption(option);

		// DB検索用パラメータ設定
		setParamData(paramData);

		if (DefineReport.ID_DEBUG_MODE) System.out.println(getClass().getSimpleName()+"[sql]"+sbSQL.toString());
		return sbSQL.toString();
	}

	private String createCommand() {
		// ログインユーザー情報取得
		User userInfo = getUserInfo();
		if(userInfo==null){
			return "";
		}

		String szMoyskbn	= getMap().get("MOYSKBN");		// 催し区分
		String szMoysstdt	= getMap().get("MOYSSTDT");		// 催しコード（催し開始日）
		String szMoysrban	= getMap().get("MOYSRBAN");		// 催し連番
		String pushBtnid	= getMap().get("PUSHBTNID");	// 実行ボタン
		String sendBtnid	= getMap().get("SENDBTNID");	// 呼出ボタン

		String szBumon		= getMap().get("BUMON");		// 部門
		JSONArray bumonAllArray	= JSONArray.fromObject(getMap().get("BUMON_DATA"));	// 全部門
		String szBycd		= getMap().get("BYCD");			// BYコード

		// パラメータ確認
		// 必須チェック
		if ((szMoyskbn == null)||(szMoysstdt == null)||(szMoysrban == null)||(sendBtnid == null)) {
			System.out.println(super.getConditionLog());
			return "";
		}

		// 全店特売アンケート有/無
		boolean isTOKTG = super.isTOKTG(szMoyskbn, szMoysrban);

		// タイトル情報(任意)設定
		List<String> titleList = new ArrayList<String>();

		// 基本情報取得
		JSONArray array = getTOKMOYCDData(getMap());


		String szTableSHN = "INATK.TOKTG_SHN";		// 全店特売(アンケート有/無)_商品
		String szTableTJTEN="INATK.TOKTG_TJTEN";	// 全店特売(アンケート有/無)_対象除外店
		String szTableNNDT ="INATK.TOKTG_NNDT";		// 全店特売(アンケート有/無)_納入日
		if(!isTOKTG){
			szTableSHN = "INATK.TOKSP_SHN";
			szTableTJTEN="INATK.TOKSP_TJTEN";
			szTableNNDT ="INATK.TOKSP_NNDT";
		}

		// 一覧表情報
		StringBuffer sbSQL = new StringBuffer();
		if(StringUtils.isNotEmpty(pushBtnid)){
			sbSQL.append(DefineReport.ID_SQL_CMN_WEEK);
			sbSQL.append(" select F1,F2,F3,F4,F5,F6,F7,F8,F9,F10,F11,F12,F13,F14,F15,F16,F17,F18,F19");
			sbSQL.append(" from (");
			sbSQL.append("  select");
			sbSQL.append("   T1.PARNO as F1");
			sbSQL.append("  ,T1.CHLDNO as F2");
			if(isTOKTG){
				// ① 当催しがアンケート有の場合、全店特売（アンケート有）_商品.月締変更理由<>0and全店特売（アンケート有）_商品.月締変更許可フラグ=0の時、チェック。
				sbSQL.append("  ,case when T1.GTSIMECHGKBN <> 0 and T1.GTSIMEOKFLG = 0 then '" + DefineReport.Values.ON.getVal() + "' end as F3");
			}else{
				// ② 当催しがアンケート無の場合、無視。
				sbSQL.append("  ,null as F3");
			}
			sbSQL.append("  ,trim(left(T1.SHNCD, 4)||'-'||SUBSTR(T1.SHNCD, 5)) as F4");
			sbSQL.append("  ,M1.SHNKN as F5");
			sbSQL.append("  ,T1.BINKBN as F6");
			sbSQL.append("  ,case when M1.SHNKBN = 1 then '" + DefineReport.Values.ON.getVal() + "' end as F7");
			sbSQL.append("  ,case when T1.HBSTDT=T1.HBEDDT then to_char(to_date(T1.HBSTDT, 'YYYYMMDD'), 'YY/MM/DD')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(TO_DATE(T1.HBSTDT, 'YYYYMMDD'))) else ");
			sbSQL.append("  to_char(to_date(T1.HBSTDT, 'YYYYMMDD'), 'YY/MM/DD')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(TO_DATE(T1.HBSTDT, 'YYYYMMDD')))");
			sbSQL.append("   ||'～'||");
			sbSQL.append("   to_char(to_date(T1.HBEDDT, 'YYYYMMDD'), 'YY/MM/DD')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(TO_DATE(T1.HBEDDT, 'YYYYMMDD'))) end as F8");
			sbSQL.append("  ,case when T1.NNSTDT=T1.NNEDDT then to_char(to_date(T1.NNSTDT, 'YYYYMMDD'), 'YY/MM/DD')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(TO_DATE(T1.NNSTDT, 'YYYYMMDD'))) else ");
			sbSQL.append("  to_char(to_date(T1.NNSTDT, 'YYYYMMDD'), 'YY/MM/DD')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(TO_DATE(T1.NNSTDT, 'YYYYMMDD')))");
			sbSQL.append("   ||'～'||");
			sbSQL.append("   to_char(to_date(T1.NNEDDT, 'YYYYMMDD'), 'YY/MM/DD')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(TO_DATE(T1.NNEDDT, 'YYYYMMDD'))) end as F9");
			sbSQL.append("  ,case when T1.BD1_TENSU is not null then '" + DefineReport.Values.ON.getVal() + "' end as F10");
			sbSQL.append("  ,case when T1.ADDSHUKBN = " + DefineReport.ValAddShuKbn.VAL1.getVal());
			if(isTOKTG){
				sbSQL.append("  and T1.B_WRITUKBN is not null");
			}else{
				sbSQL.append("  and T1.B_BAIKAAM is not null");
			}
			sbSQL.append("   then '" + DefineReport.Values.ON.getVal() + "' end as F11");
			if(isTOKTG){
				sbSQL.append("  ,T1.RANKNO_ADD as F12");
				sbSQL.append("  ,null as F13");
			}else{
				sbSQL.append("  ,T1.RANKNO_ADD_A as F12");
				sbSQL.append("  ,T1.RANKNO_DEL as F13");
			}
			sbSQL.append("  ,T1.BMNCD as F14");
			sbSQL.append("  ,T1.KANRINO as F15");
			sbSQL.append("  ,T1.KANRIENO as F16");
			sbSQL.append("  ,T1.ADDSHUKBN as F17");
			if(isTOKTG){
				sbSQL.append("  ,T1.GTSIMECHGKBN as F18");	// 月締変更理由
				sbSQL.append("  ,T1.GTSIMEOKFLG as F19");	// 月締変更許可フラグ
			}else{
				sbSQL.append("  ,null as F18");				// 月締変更理由
				sbSQL.append("  ,null as F19");				// 月締変更許可フラグ
			}

			sbSQL.append("  ,row_number() over(partition by T1.MOYSKBN,T1.MOYSSTDT,T1.MOYSRBAN,T1.BMNCD,T1.KANRINO order by T1.KANRIENO desc) as RNO");
			sbSQL.append("  from "+szTableSHN+" T1");
			sbSQL.append("  left outer join INAMS.MSTSHN M1 on M1.SHNCD = T1.SHNCD and nvl(M1.UPDKBN, 0) <> 1");
			sbSQL.append("  where T1.UPDKBN = 0");
			sbSQL.append("    and T1.MOYSKBN = "+szMoyskbn+"");
			sbSQL.append("    and T1.MOYSSTDT = "+szMoysstdt+"");
			sbSQL.append("    and T1.MOYSRBAN = "+szMoysrban+"");
			// 部門
			if (DefineReport.Values.ALL.getVal().equals(szBumon)){
				sbSQL.append("    and RIGHT('0'||T1.BMNCD,2) IN ("+StringUtils.removeEnd(StringUtils.replace(bumonAllArray.join(","),"\"","'"),",")+")");
			}else{
				sbSQL.append("    and RIGHT('0'||T1.BMNCD,2) = '"+szBumon+"'");
			}
			// BYコード
			if (StringUtils.isNotEmpty(szBycd) && !DefineReport.Values.NONE.getVal().equals(szBycd)){
				sbSQL.append("    and T1.BYCD = "+szBycd);
			}
//			sbSQL.append("    and T1.HBSTDT is not null");
//			sbSQL.append("    and T1.HBEDDT is not null");
			sbSQL.append(" ) T");
			sbSQL.append(" where RNO = 1");
			sbSQL.append(" order by F15");
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

		// 保存用 List (検索情報)作成
		setWhere(new ArrayList<List<String>>());
	}


	/**
	 * 正情報取得処理
	 *
	 * @throws Exception
	 */
	public JSONArray getTOKMOYCDData(HashMap<String, String> map) {
		String szMoyskbn	= map.get("MOYSKBN");	// 催し区分
		String szMoysstdt	= map.get("MOYSSTDT");	// 催しコード（催し開始日）
		String szMoysrban	= map.get("MOYSRBAN");	// 催し連番

		ArrayList<String> paramData = new ArrayList<String>();

		StringBuffer sbSQL = new StringBuffer();
		sbSQL.append(DefineReport.ID_SQL_CMN_WEEK);
		sbSQL.append(",WKCD as (");
		sbSQL.append(" select T1.MOYSKBN, T1.MOYSSTDT, T1.MOYSRBAN, T1.MOYKN, T1.HBSTDT, T1.HBEDDT, T1.SHUNO ");
		sbSQL.append(" from INATK.TOKMOYCD T1");
		sbSQL.append(" where T1.UPDKBN = 0");
		sbSQL.append("   and T1.MOYSKBN = "+szMoyskbn+"");
		sbSQL.append("   and T1.MOYSSTDT = "+szMoysstdt+"");
		sbSQL.append("   and T1.MOYSRBAN = "+szMoysrban+"");
		sbSQL.append(")");
		sbSQL.append(" select");
		sbSQL.append("   T1.MOYSKBN||'-'||right(T1.MOYSSTDT, 6)||'-'||right('000'||T1.MOYSRBAN, 3) as F1");
		sbSQL.append(" , T1.MOYKN as F2");
		sbSQL.append(" , case when T1.HBSTDT=T1.HBEDDT then to_char(to_date(T1.HBSTDT, 'YYYYMMDD'), 'YY/MM/DD')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(TO_DATE(T1.HBSTDT, 'YYYYMMDD'))) else ");
		sbSQL.append(" to_char(to_date(T1.HBSTDT, 'YYYYMMDD'), 'YY/MM/DD')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(TO_DATE(T1.HBSTDT, 'YYYYMMDD')))");
		sbSQL.append("   ||'～'||");
		sbSQL.append("   to_char(to_date(T1.HBEDDT, 'YYYYMMDD'), 'YY/MM/DD')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(TO_DATE(T1.HBEDDT, 'YYYYMMDD'))) end as F3");
		sbSQL.append(" , T1.SHUNO as F4");
		sbSQL.append(" , to_char(to_date(T2.GTSIMEDT,'YYYYMMDD'),'YY/MM/DD')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(TO_DATE(T2.GTSIMEDT, 'YYYYMMDD'))) as F5 ");
		sbSQL.append("  ,T2.GTSIMEFLG as F6");		// 月締フラグ
		sbSQL.append(" from WKCD T1");
		sbSQL.append(" left outer join INATK.TOKTG_KHN T2 on T1.MOYSKBN = T2.MOYSKBN and T1.MOYSSTDT = T2.MOYSSTDT and T1.MOYSRBAN = T2.MOYSRBAN and T2.UPDKBN = 0");

		ItemList iL = new ItemList();
		@SuppressWarnings("static-access")
		JSONArray array = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

		return array;
	}

	/**  CSV出力ファイル情報() */
	public enum CsvFileInfo {
		/** チラシ原稿CSV出力 */
		CSV1(1,"btn_csv1","特売原稿CSV出力", "特売・スポット計画　商品一覧　特売原稿CSV", 194),
		/** POP原稿CSV出力 */
		CSV2(2,"btn_csv2","店別数量CSV出力", "特売・スポット計画　商品一覧　店別数量CSV", 24);

		private final Integer no;
		private final String bid;
		private final String bnm;
		private final String fnm;
		private final Integer len;
		/** 初期化 */
		private CsvFileInfo(Integer no, String bid, String bnm, String fnm, Integer len) {
			this.no = no;
			this.bid = bid;
			this.bnm = bnm;
			this.fnm = fnm;
			this.len = len;
		}
		/** @return no 連番 */
		public Integer getNo() { return no; }
		/** @return bid ボタンID*/
		public String getBid() { return bid; }
		/** @return bnm ボタン名 */
		public String geBnm() { return bnm; }
		/** @return bnm ファイル名 */
		public String geFnm() { return fnm; }
		/** @return len レコード長 */
		public Integer getLen() { return len; }
		/** @return len レコード長(改行コード桁除外) */
		public Integer getDataLen() { return len-LEN_NEW_LINE_CODE; }

	}
}
