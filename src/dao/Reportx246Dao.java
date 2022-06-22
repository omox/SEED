package dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;

import authentication.bean.User;
import common.CmnDate;
import common.DefineReport;
import common.JsonArrayData;
import common.MessageUtility;
import common.MessageUtility.Msg;
import common.MessageUtility.MsgKey;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 */
public class Reportx246Dao extends ItemDao {

	/**
	 * インスタンスを生成します。
	 * @param source
	 */
	public Reportx246Dao(String JNDIname) {
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

		// タイトル情報(任意)設定
		List<String> titleList = new ArrayList<String>();

		// DB検索用パラメータ
		ArrayList<String> paramData = new ArrayList<String>();

		String szWhereCmd = this.getSqlWhere(paramData);

		// 一覧表情報
		// TODO:天気・気温情報今適当
		StringBuffer sbSQL = new StringBuffer();
		sbSQL.append(" select");
		sbSQL.append(" case when T1.TITSTCD = 1 then '作成中' when T1.TITSTCD = 2 then '確定' when T1.TITSTCD = 3 then '仕掛' when T1.TITSTCD = 4 then '完了' when T1.TITSTCD = 9 then '却下' else '' end");
//		sbSQL.append(" T1.TITSTCD");	// 状態
		sbSQL.append(" ,trim(left(lpad(T1.SHNCD, 8, '0'), 4) || '-' || SUBSTR(lpad(T1.SHNCD, 8, '0'), 5)) as SHNCD"); //商品コード
//		sbSQL.append(" ,trim(T1.SHNCD) as SHNCD");	// 商品コード
		sbSQL.append(" ,T3.SRCCD as SRCCD");	// ソースコード1
		sbSQL.append(" ,T1.SHNKN");				// 商品名
		sbSQL.append(" ,T1.RG_ATSUKFLG");		// 扱区分
		sbSQL.append(" ,T1.RG_GENKAAM");		// 原価
		sbSQL.append(" ,T1.RG_BAIKAAM");		// 本体売価
		sbSQL.append(" ,"+DefineReport.ID_SQL_MD03111301_COL_RG);		// 総額売価
		sbSQL.append(" ,T1.RG_IRISU");			// 店入数
		sbSQL.append(" ,case when trim(T1.PARENTCD) = '00000000' then '' else trim(left(T1.PARENTCD, 4) || '-' || SUBSTR(T1.PARENTCD, 5)) end as PARENTCD");	// 商品コード
		sbSQL.append(" ,T1.RG_WAPNFLG");		// ワッペン区分
		sbSQL.append(" ,T1.RG_IDENFLG");		// 一括区分
		sbSQL.append(" ,T1.SSIRCD");			// 標準仕入先
		sbSQL.append(" ,right('0'||nvl(T1.BMNCD,0),2)||'-'||right('0'||nvl(T1.DAICD,0),2)||'-'||right('0'||nvl(T1.CHUCD,0),2)||'-'||right('0'||nvl(T1.SHOCD,0),2) as BUNCD");	// 分類コード
		sbSQL.append(" ,nvl(TO_CHAR(T1.UPDDT, 'YY/MM/DD'),'__/__/__')");			// 更新日
		sbSQL.append(" ,T5.NMKN");				// 衣料使い回し
		sbSQL.append(" from INAWS.PIMTIT T1");
		sbSQL.append(" left outer join INAMS.MSTMEISHO T5 on T5.MEISHOKBN = '142' and trim(T5.MEISHOCD) = IRYOREFLG");
		sbSQL.append(" left outer join INAMS.MSTSRCCD T3 on T1.SHNCD = T3.SHNCD and T3.SEQNO = 1");
		sbSQL.append(" "+DefineReport.ID_SQL_MD03111301_JOIN);
		if (szWhereCmd.equals("")) {
			sbSQL.append(" where nvl(T1.UPDKBN, 0) <> 1 ");
		} else {
			sbSQL.append(" where " + szWhereCmd + " and nvl(T1.UPDKBN, 0) <> 1 ");
		}
		sbSQL.append(" order by ");
		sbSQL.append("  T1.SHNCD ");

		// オプション情報（タイトル）設定
		JSONObject option = new JSONObject();
		option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
		setOption(option);

		// DB検索用パラメータ設定
		setParamData(paramData);

		if (DefineReport.ID_DEBUG_MODE)	System.out.println(getClass().getSimpleName()+"[sql]"+sbSQL.toString());
		return sbSQL.toString();
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

		String szWhereCmd = this.getSqlWhere(paramData);


		int maxMSTSIRGPSHN =10;
		int maxMSTBAIKACTL = 5;
		int maxMSTSHINAGP = 10;

		int maxMSTTENKABUTSU1 = 10;
		int maxMSTTENKABUTSU2 = 30;

		// 一覧表情報
		StringBuffer sbSQL = new StringBuffer();

		sbSQL.append(" with");
		sbSQL.append("  WKSHN as (select SHNCD from INAMS.MSTSHN T1 where " + szWhereCmd + " and nvl(UPDKBN, 0) <> 1 )");
		sbSQL.append(" ,WKIDX(IDX) as (select 1 from SYSIBM.SYSDUMMY1 union all select IDX + 1 from WKIDX where IDX <= "+maxMSTTENKABUTSU1+") ");
		sbSQL.append(" select ''");														// 更新区分			:A
		sbSQL.append("  ,rtrim(T.SHNCD)");												// 商品コード		:
		sbSQL.append("  ,null");														// 商品コード桁数	:0
		sbSQL.append("  ,max(T.TEIKEIKBN)");											// 定計区分			:0
		sbSQL.append("  ,max(case X.IDX when 1 then T1.SOURCEKBN end)");				// ソース区分_1		:1
		sbSQL.append("  ,rtrim(max(case X.IDX when 1 then ''''||T1.SRCCD end))");		// ソースコード_1	:'4901737828775	※シングルクォーテーション
		sbSQL.append("  ,max(T.SSIRCD)");												// 標準仕入先コード	:13324
		sbSQL.append("  ,max(T.TEIKANKBN)");											// 定貫不定貫区分	:1
		sbSQL.append("  ,max(T.BMNCD)");												// 標準部門コード	:010
		sbSQL.append("  ,max(T.DAICD),max(T.CHUCD),max(T.SHOCD),max(right('00'||T.SSHOCD,2))");		// 標準分類コード	:41,05,97
		sbSQL.append("  ,max(T.PCKBN)");												// ＰＣ区分			:
		sbSQL.append("  ,max(T.SHNKBN)");												// 商品種類			:0
		sbSQL.append("  ,rtrim(max(T.PARENTCD))");										// 親商品コード		:0
		sbSQL.append("  ,max('\"'||T.SHNAN||'\"'),max('\"'||T.SHNKN||'\"')");			// 商品名			:"ｽｷﾞﾖ ﾔｻｲﾆｷﾞﾘｱｹﾞ","スギヨ　野菜にぎり揚"
		sbSQL.append("  ,max('\"'||T.RECEIPTAN||'\"'),max('\"'||T.RECEIPTKN||'\"')");	// レシート名		:
		sbSQL.append("  ,max('\"'||T.PCARDKN||'\"')");									// プライスカード商品名称（漢字）
		sbSQL.append("  ,max('\"'||T.SALESCOMKN||'\"')");								// 商品コメント・セールスコピー（漢字）
		sbSQL.append("  ,max('\"'||T.POPKN||'\"')");									// ＰＯＰ名称（漢字）
		sbSQL.append("  ,max('\"'||T.KIKKN||'\"')");									// 規格
		sbSQL.append("  ,max(T.RG_ATSUKFLG),max(RTRIM(TRIM('0' FROM T.RG_GENKAAM),'.')),max(T.RG_BAIKAAM),max(T.RG_IRISU),rtrim(max(T.RG_IDENFLG)),rtrim(max(T.RG_WAPNFLG))");		// レギュラー情報_取扱フラグ、原価、売価、店入数、一括伝票、ワッペン
		sbSQL.append("  ,max(T.HS_ATSUKFLG),max(RTRIM(TRIM('0' FROM T.HS_GENKAAM),'.')),max(T.HS_BAIKAAM),max(T.HS_IRISU),rtrim(max(T.HS_WAPNFLG)),rtrim(max(T.HP_SWAPNFLG))");		// 販促情報_取扱フラグ、原価、売価、店入数、ワッペン、特売ワッペン
		sbSQL.append("  ,max(T.BINKBN)");												// 便区分
		sbSQL.append("  ,max(T.SIMEKAISU)");											// 締め回数
		sbSQL.append("  ,max(right('00'||T.KOMONOKBM,2))");								// 小物区分
		sbSQL.append("  ,max(right('00'||T.SIWAKEKBN,2))");								// 仕分区分
		sbSQL.append("  ,max(right('00'||T.TANAOROKBN,2))");							// 棚卸区分
		sbSQL.append("  ,rtrim(max(T.KIKANKBN))");										// 期間
		sbSQL.append("  ,max(T.ODS_HARUSU),max(T.ODS_NATSUSU),max(T.ODS_AKISU),max(T.ODS_FUYUSU),max(T.ODS_NYUKASU),max(T.ODS_NEBIKISU)");					// ODS_賞味期限_春、夏、秋、冬、入荷期限、値引期限
		sbSQL.append("  ,max(T.HS_SPOTMINSU)");											// 販促情報_スポット最低発注数
		sbSQL.append("  ,max(T.SEIZOGENNISU)");											// 製造限度日数
		sbSQL.append("  ,max(T.READTMPTN)");											// リードタイムパターン
		sbSQL.append("  ,max(T.HAT_MONKBN),max(T.HAT_TUEKBN),max(T.HAT_WEDKBN),max(T.HAT_THUKBN),max(T.HAT_FRIKBN),max(T.HAT_SATKBN),max(T.HAT_SUNKBN)");	// 発注曜日_月、火、水、木、金、土、日
		sbSQL.append("  ,max(T.HSPTN)");												// 配送パターン
		sbSQL.append("  ,max(T.UP_YORYOSU),max(T.UP_TYORYOSU),max(right('00'||T.UP_TANIKBN,2))");		// ユニットプライス_容量、単位容量、ユニット単位
		sbSQL.append("  ,max(T.SHNTATESZ),max(T.SHNYOKOSZ),max(T.SHNOKUSZ),max(T.SHNJRYOSZ)");			// 商品サイズ_縦、横、奥行、重量
		sbSQL.append("  ,max(T.ATSUK_STDT),max(T.ATSUK_EDDT)");							// 取扱期間_開始日、終了日
		sbSQL.append("  ,rtrim(max(T.CHINRETUCD)),rtrim(max(T.DANTUMICD)),rtrim(max(T.KASANARICD)),max(T.KASANARISZ),max(T.ASSHUKURT)");					// 陳列形式コード、段積み形式コード、重なりコード、重なりサイズ、圧縮率
		sbSQL.append("  ,max(T.YOYAKUDT),max(T.TENBAIKADT)");							// マスタ変更予定日、店売価実施日
		sbSQL.append("  ,max(T.YOT_BMNCD),max(T.YOT_DAICD),max(T.YOT_CHUCD),max(T.YOT_SHOCD)");			// 用途分類コード_部門、大分類、中分類、小分類
		sbSQL.append("  ,max(T.URI_BMNCD),max(T.URI_DAICD),max(T.URI_CHUCD),max(T.URI_SHOCD)");			// 売場分類コード_部門、大分類、中分類、小分類
		sbSQL.append("  ,max(T2.AREAKBN)");												// エリア区分
		for (int i=1; i<=maxMSTSIRGPSHN; i++) {
			sbSQL.append("  ,max(case X.IDX when "+i+" then T2.TENGPCD end),max(case X.IDX when "+i+" then T2.SIRCD end),max(case X.IDX when "+i+" then T2.HSPTN end)");		// エリア区分、仕入先コード、配送パターン
		}
		sbSQL.append("  ,max(T3.AREAKBN)");
		for (int i=1; i<=maxMSTBAIKACTL; i++) {
			sbSQL.append(",max(case X.IDX when "+i+" then T3.TENGPCD end)");			// 店グループ（エリア）
			sbSQL.append(",TO_CHAR(max(case X.IDX when "+i+" then (case when MOD(T3.GENKAAM, 1) = 0 then int(T3.GENKAAM) else double(T3.GENKAAM) end) end))");		// 原価
			sbSQL.append(",max(case X.IDX when "+i+" then T3.BAIKAAM end)");			// 売価
			sbSQL.append(",max(case X.IDX when "+i+" then T3.IRISU end)");				// 店入数

		}
		sbSQL.append("  ,max(T4.AREAKBN)");												// エリア区分
		for (int i=1; i<=maxMSTSHINAGP; i++) {
			sbSQL.append("  ,max(case X.IDX when "+i+" then T4.TENGPCD end),max(case X.IDX when "+i+" then T4.ATSUKKBN end)");			// 店グループ（エリア）、扱い区分
		}
		sbSQL.append("  ,max(T5.AVGPTANKAAM)");											// 平均パック単価
		sbSQL.append("  ,max(case X.IDX when 2 then T1.SOURCEKBN end),max(case X.IDX when 2 then ''''||T1.SRCCD end)");					// ソース区分_2、ソースコード_2
		sbSQL.append("  ,max(T.PCARD_OPFLG),max(T.PCARD_SHUKBN),max(T.PCARD_IROKBN)");	// プライスカード出力有無、種類、色
		sbSQL.append("  ,max(T.ZEIKBN),max(T.ZEIRTKBN),max(T.ZEIRTKBN_OLD),max(T.ZEIRTHENKODT)");	// 税区分、税率区分、旧税率区分、税率変更日
		sbSQL.append("  ,max(T.TEISHIKBN),max(T.ICHIBAKBN),max(T.PBKBN),max(T.HENPIN_KBN),max(right('00'||T.IMPORTKBN,2)),max(T.URABARIKBN),max(T.TAISHONENSU),max(T.CALORIESU),max(T.KAKOKBN)");		// 取扱停止、市場区分、ＰＢ区分、返品区分、輸入区分、裏貼、対象年齢、カロリー表示、加工区分
		sbSQL.append("  ,max('\"'||T.SANCHIKN||'\"'),max(right('00'||T.SHUKYUKBN,2)),max(T.DOSU),max(T.HZI_YOTO),max(T.HZI_ZAISHITU),max(T.HZI_RECYCLE)");		// 産地（漢字）、酒級、度数、包材用途、包材材質、包材リサイクル対象
		sbSQL.append("  ,max(T.ELPFLG),max(T.BELLMARKFLG),max(T.RECYCLEFLG),max(T.ECOFLG)");		// フラグ情報_ＥＬＰ、ベルマーク、リサイクル、エコマーク
		sbSQL.append("  ,max(T.MAKERCD),max(T.URICD)");									// メーカーコード、販売コード
		for (int i=1; i<=maxMSTTENKABUTSU1; i++) {
			sbSQL.append("  ,max(case X.IDX when "+i+" then T6.TENKABCD end)");			// 添加物
		}
		for (int i=1; i<=maxMSTTENKABUTSU2; i++) {
			sbSQL.append("  ,max(case X.IDX when "+i+" then T7.TENKABCD end)");			// アレルギー
		}
		sbSQL.append("  ,rtrim(max(T.SHUBETUCD)),max(T.IRYOREFLG),max(T.TOROKUMOTO),max(T.OPERATOR),max(TO_CHAR(T.ADDDT,'YYYYMMDD')),max(TO_CHAR(T.UPDDT,'YYYYMMDD'))");		// 種別コード、衣料使い回しフラグ、登録元、オペレータ、登録日、更新日
		sbSQL.append(" from INAMS.MSTSHN T");
		sbSQL.append(" inner join WKSHN T0 on T.SHNCD = T0.SHNCD");
		sbSQL.append(" inner join WKIDX X on 1 = 1");
		sbSQL.append(" left outer join (");
		// SEQNO＝ソースコード優先順位という認識
		sbSQL.append("   select T1.SHNCD,T1.SRCCD, T1.SOURCEKBN, ROW_NUMBER() over(PARTITION by T1.SHNCD order by SEQNO) as RNO");
		sbSQL.append("   from INAMS.MSTSRCCD T1");
		sbSQL.append("   inner join WKSHN T0 on T0.SHNCD = T1.SHNCD and T1.SEQNO <= 2");
		sbSQL.append(" ) T1 on T.SHNCD = T1.SHNCD and X.IDX = T1.RNO");
		sbSQL.append(" left outer join (");
		sbSQL.append("   select T2.SHNCD,T2.AREAKBN,T2.TENGPCD, T2.SIRCD, T2.HSPTN, ROW_NUMBER() over(PARTITION by T2.SHNCD) as RNO");
		sbSQL.append("   from INAMS.MSTSIRGPSHN T2 ");
		sbSQL.append("   inner join WKSHN T0 on T0.SHNCD = T2.SHNCD");
		sbSQL.append(" ) T2 on T.SHNCD = T2.SHNCD and X.IDX = T2.RNO and T2.RNO <= " + maxMSTSIRGPSHN);
		sbSQL.append(" left outer join (");
		sbSQL.append("   select T3.SHNCD,T3.AREAKBN,T3.TENGPCD, T3.GENKAAM, T3.BAIKAAM, T3.IRISU, ROW_NUMBER() over(PARTITION by T3.SHNCD) as RNO");
		sbSQL.append("   from INAMS.MSTBAIKACTL T3 ");
		sbSQL.append("   inner join WKSHN T0 on T0.SHNCD = T3.SHNCD");
		sbSQL.append(" ) T3 on T.SHNCD = T3.SHNCD and X.IDX = T3.RNO and T3.RNO <= " + maxMSTBAIKACTL);
		sbSQL.append(" left outer join (");
		sbSQL.append("   select T4.SHNCD,T4.AREAKBN,T4.TENGPCD, T4.ATSUKKBN, ROW_NUMBER() over(PARTITION by T4.SHNCD) as RNO");
		sbSQL.append("   from INAMS.MSTSHINAGP T4 ");
		sbSQL.append("   inner join WKSHN T0 on T0.SHNCD = T4.SHNCD");
		sbSQL.append(" ) T4 on T.SHNCD = T4.SHNCD and X.IDX = T4.RNO and T4.RNO <= " + maxMSTSHINAGP);
		sbSQL.append(" left outer join INAMS.MSTAVGPTANKA T5 on T.SHNCD = T5.SHNCD");
		sbSQL.append(" left outer join (");
		sbSQL.append("   select T6.SHNCD, T6.TENKABCD, ROW_NUMBER() over(PARTITION by T6.SHNCD) as RNO");
		sbSQL.append("   from INAMS.MSTTENKABUTSU T6 ");
		sbSQL.append("   inner join WKSHN T0 on T0.SHNCD = T6.SHNCD and T6.TENKABKBN = 2");
		sbSQL.append(" ) T6 on T.SHNCD = T6.SHNCD and X.IDX = T6.RNO and T6.RNO <= " + maxMSTTENKABUTSU1);
		sbSQL.append(" left outer join (");
		sbSQL.append("   select T7.SHNCD, T7.TENKABCD, ROW_NUMBER() over(PARTITION by T7.SHNCD) as RNO");
		sbSQL.append("   from INAMS.MSTTENKABUTSU T7 ");
		sbSQL.append("   inner join WKSHN T0 on T0.SHNCD = T7.SHNCD and T7.TENKABKBN = 1");
		sbSQL.append(" ) T7 on T.SHNCD = T7.SHNCD and X.IDX = T7.RNO and T7.RNO <= " + maxMSTTENKABUTSU1);

		sbSQL.append(" group by T.SHNCD");
		sbSQL.append(" order by T.SHNCD");


		// DB検索用パラメータ設定
		setParamData(paramData);


		if (DefineReport.ID_DEBUG_MODE)	System.out.println(getClass().getSimpleName()+"[sql]"+sbSQL.toString());
		return sbSQL.toString();
	}


	private String getSqlWhere(ArrayList<String> paramData) {

		String teian	= getMap().get("TEIAN");	// 提案No
		String torihiki	= getMap().get("TORIHIKI");	// 取引先
		String state	= getMap().get("STATE");	// 状態
		String bumon = getMap().get("BUMON");		// 部門
		String fromDate	= getMap().get("YMD_F");	// 商品登録日FROM
		String toDate	= getMap().get("YMD_T");	// 商品登録日TONo
		String szShnkn	= getMap().get("SHNKN");	// 商品名（漢字）


		String szWhereCmd = "";

		// 提案
		szWhereCmd += " T1.TITKNNO = " + teian;

		// 取引先
		if (!torihiki.toString().equals(DefineReport.Values.ALL.getVal())){
			szWhereCmd += " and T1.SSIRCD = " + torihiki;
		}

		// 状態
		if (!StringUtils.isEmpty(state)){
			szWhereCmd += " and T1.TITSTCD = " + state;
		}

		// 部門
		if (!bumon.toString().equals(DefineReport.Values.ALL.getVal())){
			szWhereCmd += " and T1.BMNCD = " + bumon;
		}

		// *** その他条件
		if (!StringUtils.isEmpty(szShnkn)){
			szWhereCmd += " and T1.SHNKN like ?";
			paramData.add("%" + szShnkn + "%");
		}

		// 商品登録日
		if (!StringUtils.isEmpty(fromDate)){
			String convdt = CmnDate.getConvInpDate(fromDate);
			szWhereCmd += " and to_char(T1.ADDDT, 'YYYYMMDD') >= '" + convdt + "'";
		}
		if (!StringUtils.isEmpty(toDate)){
			String convdt = CmnDate.getConvInpDate(toDate);
			szWhereCmd += " and to_char(T1.ADDDT, 'YYYYMMDD') <= '" + convdt + "'";
		}

		return szWhereCmd;
	}

	/**
	 * 削除処理
	 * @param request
	 * @param session
	 * @param map
	 * @param userInfo
	 * @return
	 */
	public JSONObject delete(HttpServletRequest request, HttpSession session, HashMap<String, String> map, User userInfo) {
		// メッセージ格納用
		JSONObject rtn = new JSONObject();
		JSONArray msg = new JSONArray();

		// パラメータ確認
		JSONArray dataArray	= JSONArray.fromObject(map.get("DATA"));	// 更新情報

		ArrayList<String> prmData = new ArrayList<String>();
		int upCount = 0;

		// コネクション
		Connection con = null;

		try {
			// コネクションの取得
			con = DBConnection.getConnection(this.JNDIname);
			con.setAutoCommit(false);

			String sqlCommand = "delete from INAWS.PIMTIT where SHNCD = ?";

			for (int i = 0; i < dataArray.size(); i++) {
				String shncd = "";
				if(!dataArray.getJSONObject(i).isEmpty()){
					shncd = dataArray.getJSONObject(i).optString("F2");
				}
				if(shncd.isEmpty()){
					continue;
				}

				prmData = new ArrayList<String>();
				prmData.add(shncd);

				// SQL実行
				upCount += updateBySQL(sqlCommand, prmData, con);
			}

			con.commit();
			con.close();

			rtn.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00004.getVal(), String.valueOf(upCount)));

		} catch (Exception e) {
			/* 接続解除 */
			if (con != null){
				try {
					con.rollback();
					con.close();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}
			e.printStackTrace();
			msg.add(MessageUtility.getMessage(Msg.E00002.getVal()));
			rtn.put(MsgKey.E.getKey(), msg);
		}

		return rtn;
	}

	/**
	 * 更新処理実行
	 * @param userInfo
	 * @param map
	 * @return
	 * @throws Exception
	 */
	private JSONObject updateData(User userInfo, HashMap<String, String> map) throws Exception {
		JSONObject rtn = new JSONObject();

		// パラメータ確認
		JSONArray dataArray	= JSONArray.fromObject(map.get("DATA"));	// 更新情報
		String szTeian		= map.get("TEIAN");							// 提案件名

		String userID = userInfo.getId();
		CmnDate dateInfo = new CmnDate();
		String sysDate = dateInfo.getToday();				// 本日日付
		String sysTime = dateInfo.getNowTime();				// 現在時刻

		ArrayList<String> prmData = new ArrayList<String>();
		int upCount = 0;

		// コネクション
		Connection con = null;

		try {
			// コネクションの取得
			con = DBConnection.getConnection(this.JNDIname);
			con.setAutoCommit(false);

			String sqlCommand = "update INAWS.PIMTIT set TITSTCD = 2 where SHNCD = ?";

			for (int i = 0; i < dataArray.size(); i++) {
				String shncd = "";
				if(!dataArray.getJSONObject(i).isEmpty()){
					shncd = dataArray.getJSONObject(i).optString("F2");
				}
				if(shncd.isEmpty()){
					continue;
				}

				prmData = new ArrayList<String>();
				prmData.add(shncd);

				// SQL実行
				upCount += updateBySQL(sqlCommand, prmData, con);
			}

			con.commit();
			con.close();

			if (dataArray.size() == upCount) {
				rtn.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00007.getVal(), "対象データ"));
			} else {
				rtn.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00006.getVal(), String.valueOf(upCount), String.valueOf(dataArray.size() - upCount)));
			}

		} catch (Exception e) {
			/* 接続解除 */
			if (con != null){
				try {
					con.rollback();
					con.close();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}
			e.printStackTrace();
			throw e;
		}

		return rtn;
	}

	/**
	 * 更新処理
	 * @param request
	 * @param session
	 * @param map
	 * @param userInfo
	 * @return
	 */
	public JSONObject update(HttpServletRequest request, HttpSession session, HashMap<String, String> map, User userInfo) {
		// メッセージ格納用
		JSONObject rtn = new JSONObject();
		JSONArray msg = new JSONArray();

		String mode = map.get("MODE");	// 更新処理モード

		// 更新処理
		try {
			if(StringUtils.equals("3", mode)){
				// 「作成中」→「確定」に更新
				rtn = updateData(userInfo, map);
			}

		} catch (Exception e) {
			e.printStackTrace();
			msg.add(MessageUtility.getMessage(Msg.E00001.getVal()));
			rtn.put(MsgKey.E.getKey(), msg);
		}

		return rtn;
	}


	private void outputQueryList() {

		// 検索条件の加工クラス作成
		JsonArrayData jad = new JsonArrayData();
		jad.setJsonString(getJson());

		// 保存用 List (検索情報)作成
		setWhere(new ArrayList<List<String>>());
		List<String> cells = new ArrayList<String>();

		// タイトル名称
		cells.add("提案商品一覧");// jad.getJSONText(DefineReport.ID_HIDDEN_REPORT_NAME));
		getWhere().add(cells);

		// 空白行
		cells = new ArrayList<String>();
		cells.add("");
		getWhere().add(cells);

		// 取引先
		cells = new ArrayList<String>();
		cells.add( DefineReport.Select.TORIHIKI.getTxt() );
		cells.add( jad.getJSONText(DefineReport.Select.TORIHIKI.getObj()) );
		// 提案件名
		cells.add( DefineReport.Select.TEIAN.getTxt() );
		cells.add( jad.getJSONText(DefineReport.Select.TEIAN.getObj()) );
		// 状態
		cells.add( DefineReport.Select.STCD_TEIAN.getTxt() );
		cells.add( jad.getJSONText(DefineReport.Select.STCD_TEIAN.getObj()) );
		getWhere().add(cells);

		// 部門
		cells = new ArrayList<String>();
		cells.add( DefineReport.Select.BUMON.getTxt());
		cells.add( jad.getJSONText( DefineReport.Select.BUMON.getObj()) );
		// 商品名
		cells.add( DefineReport.InpText.SHNKN.getTxt());
		cells.add( jad.getJSONValue(DefineReport.InpText.SHNKN.getObj()) );
		getWhere().add(cells);

		// 商品登録日
		cells = new ArrayList<String>();
		cells.add( "商品登録日" );
		cells.add( jad.getJSONText(DefineReport.InpText.YMD_F.getObj()) );
		cells.add( "～" );
		cells.add( jad.getJSONText(DefineReport.InpText.YMD_T.getObj()) );
		getWhere().add(cells);

		// 空白行
		cells = new ArrayList<String>();
		cells.add("");
		getWhere().add(cells);
	}
}
