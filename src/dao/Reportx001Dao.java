package dao;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import authentication.bean.User;
import common.CmnDate;
import common.DefineReport;
import common.JsonArrayData;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 */
public class Reportx001Dao extends ItemDao {

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public Reportx001Dao(String JNDIname) {
    super(JNDIname);
  }

  /**
   * 検索実行
   *
   * @return
   */
  @Override
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
  @Override
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
    if (userInfo == null) {
      return "";
    }

    String btnId = getMap().get("BTN"); // 実行ボタン

    // パラメータ確認
    // 必須チェック
    if ((btnId == null)) {
      System.out.println(super.getConditionLog());
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
    sbSQL.append(" select ");
    sbSQL.append("  T2.YOYAKU as YOYAKU"); // 予約
    sbSQL.append(" ,trim(left(T1.SHNCD, 4) || '-' || SUBSTR(T1.SHNCD, 5)) as SHNCD"); // 商品コード
    sbSQL.append(" ,T3.SRCCD as SRCCD"); // ソースコード1
    sbSQL.append(" ,T1.URICD"); // 販売コード
    sbSQL.append(" ,T1.SHNKN"); // 商品名
    sbSQL.append(" ,T1.RG_ATSUKFLG"); // 扱区分
    sbSQL.append(" ,T1.RG_GENKAAM"); // 原価
    sbSQL.append(" ,T1.RG_BAIKAAM"); // 本体売価
    sbSQL.append(" ," + DefineReport.ID_SQL_MD03111301_COL_RG); // 総額売価
    sbSQL.append(" ,T1.RG_IRISU"); // 店入数
    sbSQL.append(" ,case when trim(T1.PARENTCD) = '00000000' then '' else trim(left(T1.PARENTCD, 4) || '-' || SUBSTR(T1.PARENTCD, 5)) end as PARENTCD"); // 商品コード
    sbSQL.append(" ,T1.RG_WAPNFLG"); // ワッペン区分
    sbSQL.append(" ,T1.RG_IDENFLG"); // 一括区分
    sbSQL.append(" ,T1.SSIRCD"); // 標準仕入先
    sbSQL.append(" ,right('0'||COALESCE(T1.BMNCD,0),2)||'-'||right('0'||COALESCE(T1.DAICD,0),2)||'-'||right('0'||COALESCE(T1.CHUCD,0),2)||'-'||right('0'||COALESCE(T1.SHOCD,0),2) as BUNCD"); // 分類コード
    sbSQL.append(" ,COALESCE(DATE_FORMAT(T1.UPDDT, '%y/%m/%d'),'__/__/__')"); // 更新日
    sbSQL.append(" ,T5.NMKN"); // 衣料使い回し
    sbSQL.append(" ,trim(T1.SHNCD) as SHNCD"); // 商品コード
    sbSQL.append(" from INAMS.MSTSHN T1");
    sbSQL.append(" left outer join INAMS.MSTMEISHO T5 on T5.MEISHOKBN = '142' and trim(T5.MEISHOCD) = IRYOREFLG");
    sbSQL.append(" left outer join INAMS.MSTSRCCD T3 on T1.SHNCD = T3.SHNCD and T3.SEQNO = 1");
    sbSQL.append(" left outer join (select SHNCD, COUNT(YOYAKUDT) as YOYAKU from INAMS.MSTSHN_Y where COALESCE(UPDKBN, 0) <> 1 group by SHNCD) T2 on T1.SHNCD = T2.SHNCD");
    sbSQL.append(" " + DefineReport.ID_SQL_MD03111301_JOIN + "");
    sbSQL.append(" where " + szWhereCmd + " and COALESCE(T1.UPDKBN, 0) <> 1 ");
    sbSQL.append(" order by ");
    sbSQL.append("  T1.SHNCD ");

    // オプション情報（タイトル）設定
    JSONObject option = new JSONObject();
    option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
    setOption(option);

    // DB検索用パラメータ設定
    setParamData(paramData);

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(getClass().getSimpleName() + "[sql]" + sbSQL.toString());
    return sbSQL.toString();
  }

  private String createCommandForDl() {
    // ログインユーザー情報取得
    User userInfo = getUserInfo();
    if (userInfo == null) {
      return "";
    }

    String btnId = getMap().get("BTN"); // 実行ボタン

    // パラメータ確認
    // 必須チェック
    if ((btnId == null)) {
      System.out.println(super.getConditionLog());
      return "";
    }

    // DB検索用パラメータ
    ArrayList<String> paramData = new ArrayList<String>();

    String szWhereCmd = this.getSqlWhere(paramData);


    int maxMSTSIRGPSHN = 10;
    int maxMSTBAIKACTL = 5;
    int maxMSTSHINAGP = 10;

    int maxMSTTENKABUTSU1 = 10;
    int maxMSTTENKABUTSU2 = 30;

    // 一覧表情報
    StringBuffer sbSQL = new StringBuffer();

    sbSQL.append(" with");
    sbSQL.append(" RECURSIVE WKIDX(IDX) as (select 1 from (SELECT 1 AS DUMMY) DUMMY union all select IDX + 1 from WKIDX where IDX <= " + maxMSTTENKABUTSU1 + ") ");

    sbSQL.append(" ,WKSHN as (select SHNCD from INAMS.MSTSHN T1 where " + szWhereCmd + " and COALESCE(UPDKBN, 0) <> 1 )");
    sbSQL.append(" select ''"); // 更新区分 :A
    sbSQL.append("  ,rtrim(T.SHNCD)"); // 商品コード :
    sbSQL.append("  ,null"); // 商品コード桁数 :0
    sbSQL.append("  ,max(T.TEIKEIKBN)"); // 定計区分 :0
    sbSQL.append("  ,max(case X.IDX when 1 then T1.SOURCEKBN end)"); // ソース区分_1 :1
    sbSQL.append("  ,rtrim(max(case X.IDX when 1 then ''''||T1.SRCCD end))"); // ソースコード_1 :'4901737828775 ※シングルクォーテーション
    sbSQL.append("  ,max(T.SSIRCD)"); // 標準仕入先コード :13324
    sbSQL.append("  ,max(T.TEIKANKBN)"); // 定貫不定貫区分 :1
    sbSQL.append("  ,max(T.BMNCD)"); // 標準部門コード :010
    sbSQL.append("  ,max(T.DAICD),max(T.CHUCD),max(T.SHOCD),max(right('00'||T.SSHOCD,2))"); // 標準分類コード :41,05,97
    sbSQL.append("  ,max(T.PCKBN)"); // ＰＣ区分 :
    sbSQL.append("  ,max(T.SHNKBN)"); // 商品種類 :0
    sbSQL.append("  ,rtrim(max(T.PARENTCD))"); // 親商品コード :0
    sbSQL.append("  ,max('\"'||T.SHNAN||'\"'),max('\"'||T.SHNKN||'\"')"); // 商品名 :"ｽｷﾞﾖ ﾔｻｲﾆｷﾞﾘｱｹﾞ","スギヨ 野菜にぎり揚"
    sbSQL.append("  ,max('\"'||T.RECEIPTAN||'\"'),max('\"'||T.RECEIPTKN||'\"')"); // レシート名 :
    sbSQL.append("  ,max('\"'||T.PCARDKN||'\"')"); // プライスカード商品名称（漢字）
    sbSQL.append("  ,max('\"'||T.SALESCOMKN||'\"')"); // 商品コメント・セールスコピー（漢字）
    sbSQL.append("  ,max('\"'||T.POPKN||'\"')"); // ＰＯＰ名称（漢字）
    sbSQL.append("  ,max('\"'||T.KIKKN||'\"')"); // 規格
    sbSQL.append("  ,max(T.RG_ATSUKFLG),max(TRIM('.' FROM TRIM('0' FROM T.RG_GENKAAM))),max(T.RG_BAIKAAM),max(T.RG_IRISU),rtrim(max(T.RG_IDENFLG)),rtrim(max(T.RG_WAPNFLG))"); // レギュラー情報_取扱フラグ、原価、売価、店入数、一括伝票、ワッペン
    sbSQL.append("  ,max(T.HS_ATSUKFLG),max(TRIM('.' FROM TRIM('0' FROM T.HS_GENKAAM))),max(T.HS_BAIKAAM),max(T.HS_IRISU),rtrim(max(T.HS_WAPNFLG)),rtrim(max(T.HP_SWAPNFLG))"); // 販促情報_取扱フラグ、原価、売価、店入数、ワッペン、特売ワッペン
    sbSQL.append("  ,max(T.BINKBN)"); // 便区分
    sbSQL.append("  ,max(T.SIMEKAISU)"); // 締め回数
    sbSQL.append("  ,max(right('00'||T.KOMONOKBM,2))"); // 小物区分
    sbSQL.append("  ,max(right('00'||T.SIWAKEKBN,2))"); // 仕分区分
    sbSQL.append("  ,max(right('00'||T.TANAOROKBN,2))"); // 棚卸区分
    sbSQL.append("  ,rtrim(max(T.KIKANKBN))"); // 期間
    sbSQL.append("  ,max(T.ODS_HARUSU),max(T.ODS_NATSUSU),max(T.ODS_AKISU),max(T.ODS_FUYUSU),max(T.ODS_NYUKASU),max(T.ODS_NEBIKISU)"); // ODS_賞味期限_春、夏、秋、冬、入荷期限、値引期限
    sbSQL.append("  ,max(T.HS_SPOTMINSU)"); // 販促情報_スポット最低発注数
    sbSQL.append("  ,max(T.SEIZOGENNISU)"); // 製造限度日数
    sbSQL.append("  ,max(T.READTMPTN)"); // リードタイムパターン
    sbSQL.append("  ,max(T.HAT_MONKBN),max(T.HAT_TUEKBN),max(T.HAT_WEDKBN),max(T.HAT_THUKBN),max(T.HAT_FRIKBN),max(T.HAT_SATKBN),max(T.HAT_SUNKBN)"); // 発注曜日_月、火、水、木、金、土、日
    sbSQL.append("  ,max(T.HSPTN)"); // 配送パターン
    sbSQL.append("  ,max(T.UP_YORYOSU),max(T.UP_TYORYOSU),max(right('00'||T.UP_TANIKBN,2))"); // ユニットプライス_容量、単位容量、ユニット単位
    sbSQL.append("  ,max(T.SHNTATESZ),max(T.SHNYOKOSZ),max(T.SHNOKUSZ),max(T.SHNJRYOSZ)"); // 商品サイズ_縦、横、奥行、重量
    sbSQL.append("  ,max(T.ATSUK_STDT),max(T.ATSUK_EDDT)"); // 取扱期間_開始日、終了日
    sbSQL.append("  ,rtrim(max(T.CHINRETUCD)),rtrim(max(T.DANTUMICD)),rtrim(max(T.KASANARICD)),max(T.KASANARISZ),max(T.ASSHUKURT)"); // 陳列形式コード、段積み形式コード、重なりコード、重なりサイズ、圧縮率
    sbSQL.append("  ,max(T.YOYAKUDT),max(T.TENBAIKADT)"); // マスタ変更予定日、店売価実施日
    sbSQL.append("  ,max(T.YOT_BMNCD),max(T.YOT_DAICD),max(T.YOT_CHUCD),max(T.YOT_SHOCD)"); // 用途分類コード_部門、大分類、中分類、小分類
    sbSQL.append("  ,max(T.URI_BMNCD),max(T.URI_DAICD),max(T.URI_CHUCD),max(T.URI_SHOCD)"); // 売場分類コード_部門、大分類、中分類、小分類
    sbSQL.append("  ,max(T2.AREAKBN)"); // エリア区分
    for (int i = 1; i <= maxMSTSIRGPSHN; i++) {
      sbSQL.append("  ,max(case X.IDX when " + i + " then T2.TENGPCD end),max(case X.IDX when " + i + " then T2.SIRCD end),max(case X.IDX when " + i + " then T2.HSPTN end)"); // エリア区分、仕入先コード、配送パターン
    }
    sbSQL.append("  ,max(T3.AREAKBN)");
    for (int i = 1; i <= maxMSTBAIKACTL; i++) {
      sbSQL.append(",max(case X.IDX when " + i + " then T3.TENGPCD end)"); // 店グループ（エリア）
      sbSQL.append(",CAST(max(case X.IDX when " + i + " then (case when MOD(T3.GENKAAM, 1) = 0 then CAST(T3.GENKAAM AS SIGNED) else T3.GENKAAM end) end) AS CHAR )"); // 原価
      sbSQL.append(",max(case X.IDX when " + i + " then T3.BAIKAAM end)"); // 売価
      sbSQL.append(",max(case X.IDX when " + i + " then T3.IRISU end)"); // 店入数

    }
    sbSQL.append("  ,max(T4.AREAKBN)"); // エリア区分
    for (int i = 1; i <= maxMSTSHINAGP; i++) {
      sbSQL.append("  ,max(case X.IDX when " + i + " then T4.TENGPCD end),max(case X.IDX when " + i + " then T4.ATSUKKBN end)"); // 店グループ（エリア）、扱い区分
    }
    sbSQL.append("  ,max(T5.AVGPTANKAAM)"); // 平均パック単価
    sbSQL.append("  ,max(case X.IDX when 2 then T1.SOURCEKBN end),max(case X.IDX when 2 then ''''||T1.SRCCD end)"); // ソース区分_2、ソースコード_2
    sbSQL.append("  ,max(T.PCARD_OPFLG),max(T.PCARD_SHUKBN),max(T.PCARD_IROKBN)"); // プライスカード出力有無、種類、色
    sbSQL.append("  ,max(T.ZEIKBN),max(T.ZEIRTKBN),max(T.ZEIRTKBN_OLD),max(T.ZEIRTHENKODT)"); // 税区分、税率区分、旧税率区分、税率変更日
    sbSQL.append("  ,max(T.TEISHIKBN),max(T.ICHIBAKBN),max(T.PBKBN),max(T.HENPIN_KBN),max(right('00'||T.IMPORTKBN,2)),max(T.URABARIKBN),max(T.TAISHONENSU),max(T.CALORIESU),max(T.KAKOKBN)"); // 取扱停止、市場区分、ＰＢ区分、返品区分、輸入区分、裏貼、対象年齢、カロリー表示、加工区分
    sbSQL.append("  ,max('\"'||T.SANCHIKN||'\"'),max(right('00'||T.SHUKYUKBN,2)),max(T.DOSU),max(T.HZI_YOTO),max(T.HZI_ZAISHITU),max(T.HZI_RECYCLE)"); // 産地（漢字）、酒級、度数、包材用途、包材材質、包材リサイクル対象
    sbSQL.append("  ,max(T.ELPFLG),max(T.BELLMARKFLG),max(T.RECYCLEFLG),max(T.ECOFLG)"); // フラグ情報_ＥＬＰ、ベルマーク、リサイクル、エコマーク
    sbSQL.append("  ,max(T.MAKERCD),max(T.URICD)"); // メーカーコード、販売コード
    for (int i = 1; i <= maxMSTTENKABUTSU1; i++) {
      sbSQL.append("  ,max(case X.IDX when " + i + " then T6.TENKABCD end)"); // 添加物
    }
    for (int i = 1; i <= maxMSTTENKABUTSU2; i++) {
      sbSQL.append("  ,max(case X.IDX when " + i + " then T7.TENKABCD end)"); // アレルギー
    }
    sbSQL.append("  ,rtrim(max(T.SHUBETUCD)),max(T.IRYOREFLG),max(T.TOROKUMOTO),max(T.OPERATOR),max(DATE_FORMAT(T.ADDDT,'%Y%m%d')),max(DATE_FORMAT(T.UPDDT,'%Y%m%d'))"); // 種別コード、衣料使い回しフラグ、登録元、オペレータ、登録日、更新日
    sbSQL.append(" , max(case when T.BMNCD IN (20,23,70,73) THEN T.K_HONKB ELSE null END)");
    sbSQL.append(" , max(case when T.BMNCD IN (20,23,70,73) THEN T.K_WAPNFLG_R ELSE null END)");
    sbSQL.append(" , max(case when T.BMNCD IN (20,23,70,73) THEN T.K_TORIKB ELSE null END)");
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


    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(getClass().getSimpleName() + "[sql]" + sbSQL.toString());
    return sbSQL.toString();
  }


  private String getSqlWhere(ArrayList<String> paramData) {

    String szShncd = getMap().get("SHNCD"); // 商品コード
    String szShnkn = getMap().get("SHNKN"); // 商品名（漢字）
    String szSrccd = getMap().get("SRCCD"); // ソースコード

    JSONArray bumonArray = JSONArray.fromObject(getMap().get("BUMON")); // 部門
    JSONArray bumonAllArray = JSONArray.fromObject(getMap().get("BUMON_DATA")); // 全部門
    JSONArray daiBunArray = JSONArray.fromObject(getMap().get("DAI_BUN")); // 大分類
    JSONArray chuBunArray = JSONArray.fromObject(getMap().get("CHU_BUN")); // 中分類

    String daiBun = bumonArray.optString(0) + daiBunArray.optString(0);
    String chuBun = bumonArray.optString(0) + daiBunArray.optString(0) + chuBunArray.optString(0);

    String szSsircd = getMap().get("SSIRCD"); // 仕入先コード
    String szMakercd = getMap().get("MAKERCD"); // メーカーコード
    String szCsvshncd = getMap().get("CSVSHNCD"); // CSV出力用商品コード
    String szTeikankbn = getMap().get("TEIKANKBN"); // 定貫不定貫区分
    String szTeikeikbn = getMap().get("TEIKEIKBN"); // 定計区分
    String szUpddtf = getMap().get("UPDDTF"); // 更新日from
    String szUpddtt = getMap().get("UPDDTT"); // 更新日to
    String szIryoreflg = getMap().get("IRYOREFLG"); // 衣料使い回しフラグ

    String btnId = getMap().get("BTN"); // 実行ボタン

    String szWhereCmd = "";

    // *** その他条件
    // 商品コード
    if (!StringUtils.isEmpty(szShncd)) {
      szWhereCmd = " T1.SHNCD = '" + szShncd + "'";
    } else {
      // *** 分類条件
      // 部門
      if (bumonArray.optString(0).equals(DefineReport.Values.ALL.getVal())) {
        szWhereCmd = " CAST(SUBSTR(T1.SHNCD, 1, 2) AS signed) IN (" + StringUtils.removeEnd(StringUtils.replace(StringUtils.replace(bumonAllArray.join(","), "\"0", "\""), "\"", ""), ",") + ")";
        szWhereCmd += " and T1.BMNCD IN (" + StringUtils.removeEnd(StringUtils.replace(StringUtils.replace(bumonAllArray.join(","), "\"0", "\""), "\"", ""), ",") + ")";
      } else {
        szWhereCmd = " CAST(SUBSTR(T1.SHNCD, 1, 2) AS signed) = " + StringUtils.removeEnd(StringUtils.replace(StringUtils.replace(bumonArray.join(","), "\"0", "\""), "\"", ""), ",");
        szWhereCmd += " and T1.BMNCD = " + StringUtils.removeEnd(StringUtils.replace(StringUtils.replace(bumonArray.join(","), "\"0", "\""), "\"", ""), ",");
      }
    }

    String szWhereBun = "";
    // 大分類設定
    if (!daiBunArray.optString(0).equals(DefineReport.Values.ALL.getVal())) {
      szWhereBun = " and RIGHT('0'||T1.BMNCD,2)||RIGHT('0'||T1.DAICD,2) = " + daiBun;
    }
    // 中分類指定
    if (!chuBunArray.optString(0).equals(DefineReport.Values.ALL.getVal())) {
      szWhereBun = " and RIGHT('0'||T1.BMNCD,2)||RIGHT('0'||T1.DAICD,2)||RIGHT('0'||T1.CHUCD,2) = " + chuBun;
    }
    szWhereCmd += szWhereBun;

    // *** その他条件
    if (!StringUtils.isEmpty(szShnkn)) {
      szWhereCmd += " and T1.SHNKN like ?";
      paramData.add("%" + szShnkn + "%");
    }
    if (!StringUtils.isEmpty(szSrccd)) {
      szWhereCmd += " and exists ( select 'X' from INAMS.MSTSRCCD T2 where T1.SHNCD = T2.SHNCD and T2.SRCCD = '" + szSrccd + "')";
    }
    if (!StringUtils.isEmpty(szSsircd)) {
      szWhereCmd += " and T1.SSIRCD = " + szSsircd;
    }
    if (!StringUtils.isEmpty(szMakercd)) {
      szWhereCmd += " and T1.MAKERCD = " + szMakercd;
    }
    if (!StringUtils.isEmpty(szCsvshncd)) {
      if (StringUtils.equals(btnId, DefineReport.Button.CSV.getObj())) {
        szWhereCmd += " and T1.SHNCD like '" + szCsvshncd + "%'";
      }
    }
    if (!DefineReport.Values.ALL.getVal().equals(szTeikankbn)) {
      szWhereCmd += " and T1.TEIKANKBN = " + szTeikankbn;
    }
    if (!DefineReport.Values.ALL.getVal().equals(szTeikeikbn)) {
      szWhereCmd += " and T1.TEIKEIKBN = " + szTeikeikbn;
    }
    if (!StringUtils.isEmpty(szUpddtf)) {
      String convdt = CmnDate.getConvInpDate(szUpddtf);
      szWhereCmd += " and DATE_FORMAT(T1.UPDDT, '%Y%m%d') >= '" + convdt + "'";
    }
    if (!StringUtils.isEmpty(szUpddtt)) {
      String convdt = CmnDate.getConvInpDate(szUpddtt);
      szWhereCmd += " and DATE_FORMAT(T1.UPDDT, '%Y%m%d') <= '" + convdt + "'";
    }
    if (!DefineReport.Values.ALL.getVal().equals(szIryoreflg)) {
      String[] vals = new String[] {DefineReport.ValKbn142.VAL0.getVal(), DefineReport.ValKbn142.VAL1.getVal()};
      if (ArrayUtils.contains(vals, szIryoreflg)) {
        szWhereCmd += " and T1.IRYOREFLG = " + szIryoreflg;
      } else {
        szWhereCmd += " and T1.IRYOREFLG in (" + StringUtils.join(vals, ",") + ")";
      }
    }
    return szWhereCmd;
  }


  private void outputQueryList() {

    // 検索条件の加工クラス作成
    JsonArrayData jad = new JsonArrayData();
    jad.setJsonString(getJson());

    // 保存用 List (検索情報)作成
    setWhere(new ArrayList<List<String>>());
    List<String> cells = new ArrayList<String>();

    // タイトル名称
    cells.add("商品マスタ");// jad.getJSONText(DefineReport.ID_HIDDEN_REPORT_NAME));
    getWhere().add(cells);

    // 空白行
    cells = new ArrayList<String>();
    cells.add("");
    getWhere().add(cells);

    cells = new ArrayList<String>();
    cells.add(DefineReport.Select.KIKAN.getTxt());
    cells.add(jad.getJSONText(DefineReport.Select.KIKAN_F.getObj()));
    cells.add(DefineReport.Select.TENPO.getTxt());
    cells.add(jad.getJSONText(DefineReport.Select.TENPO.getObj()));
    cells.add(DefineReport.Select.BUMON.getTxt());
    cells.add(jad.getJSONText(DefineReport.Select.BUMON.getObj()));
    getWhere().add(cells);

    // 空白行
    cells = new ArrayList<String>();
    cells.add("");
    getWhere().add(cells);
  }
}
