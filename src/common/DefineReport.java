package common;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

public class DefineReport {

  /**
   * 開発関連
   */
  /* デバッグ(true:開発時、false:リリース時 */
  public final static boolean ID_DEBUG_MODE = true;

  /**
   * PARAMETER 関連
   */
  /* パラメータ：ページ番号 */
  public final static String ID_PARAM_PAGE = "page";
  /* パラメータ：オブジェクト番号 */
  public final static String ID_PARAM_OBJ = "obj";
  /* パラメータ：選択値 */
  public final static String ID_PARAM_VAL = "sel";
  /* パラメータ：JSON値 */
  public final static String ID_PARAM_JSON = "json";
  /* パラメータ：アクション */
  public final static String ID_PARAM_ACTION = "action";
  /* パラメータ：ターゲット */
  public final static String ID_PARAM_TARGET = "target";
  /* パラメータ：タイプ */
  public final static String ID_PARAM_TYPE = "datatype";
  /* パラメータ：URL */
  public final static String ID_PARAM_URL = "url";

  /* パラメータ：最大取得件数 */
  public final static String ID_SEARCHJSON_PARAM_MAXROW = "maxRows";
  /* パラメータ：検索キー */
  public final static String ID_SEARCHJSON_PARAM_NAMEWITH = "q";

  /* パラメータ：アクション 初期値 */
  public final static String ID_PARAM_ACTION_DEFAULT = "run"; // 検索実行
  public final static String ID_PARAM_ACTION_GET = "get"; // 通常通信
  public final static String ID_PARAM_ACTION_INIT = "init"; // 項目初期化
  public final static String ID_PARAM_ACTION_CHANGE = "change"; // 項目変更
  public final static String ID_PARAM_ACTION_ITEMS = "items"; // 商品入力
  public final static String ID_PARAM_ACTION_TENPO = "tenpo"; // 店舗グループ
  public final static String ID_PARAM_ACTION_SHIORI = "shiori"; // 定義保存
  public final static String ID_PARAM_ACTION_STORE = "store"; // メンテナンス保存
  public final static String ID_PARAM_ACTION_AUTOQUERY = "autoQuery"; // 自動検索
  public final static String ID_PARAM_ACTION_UPDATE = "update"; // 更新処理
  public final static String ID_PARAM_ACTION_DELETE = "delete"; // 削除処理
  public final static String ID_PARAM_ACTION_CHECK = "check"; // チェック処理
  public final static String ID_PARAM_ACTION_STATUS = "STATUS"; // 状況
  public final static String ID_PARAM_ACTION_BACK = "BACK"; // 戻る
  public final static String ID_PARAM_ACTION_FOWRWARD = "FOWRWARD"; // 進む

  /* パラメータ：タイプ 初期値 */
  public final static String ID_PARAM_TYPE_COMBOBOX = "combobox"; // combobox
  public final static String ID_PARAM_TYPE_COMBOGRID = "combogrid"; // combobox
  public final static String ID_PARAM_TYPE_DATAGRID = "datagrid"; // datagrid

  // JSON：メンバー：実値
  public final static String ID_JSON_VALUE = "value";
  // JSON：メンバー：表示値
  public final static String ID_JSON_TEXT = "text";

  /* パラメータ：オプション情報(title) */
  public final static String ID_PARAM_OPT_TITLE = "title";
  /* パラメータ：オプション情報(titles) */
  public final static String ID_PARAM_OPT_TITLES = "titles";
  /* パラメータ：オプション情報(header) */
  public final static String ID_PARAM_OPT_HEAD = "header";
  /* パラメータ：オプション情報(footer) */
  public final static String ID_PARAM_OPT_FOOT = "footer";

  /**
   * ページ番号
   */
  /* マスタ */
  /** ページ番号 商品マスタ検索 */
  public final static String ID_PAGE_ITEM = "ItemSets";
  /** 画面ID：日別予算作成画面 */
  public final static String ID_PAGE_001 = "Out_Report001";

  /** 画面ID：商品マスタ一覧画面 */
  public final static String ID_PAGE_X001 = "Out_Reportx001";
  /** 画面ID：商品マスタ登録画面 */
  public final static String ID_PAGE_X002 = "Out_Reportx002";
  /** 画面ID：商品情報照会画面 */
  public final static String ID_PAGE_X003 = "Out_Reportx003";
  /** 画面ID：商品マスタCSV取込画面 */
  public final static String ID_PAGE_X004 = "Out_Reportx004";
  /** 画面ID：商品マスタCSV取込エラー選択画面 */
  public final static String ID_PAGE_X005 = "Out_Reportx005";
  /** 画面ID：商品マスタCSV取込エラー選択画面 */
  public final static String ID_PAGE_X006 = "Out_Reportx006";
  /** 画面ID：商品マスタCSV取込エラーリスト画面 */
  public final static String ID_PAGE_X007 = "Out_Reportx007";
  /** 画面ID：商品マスタ ソースコードマスタ */
  public final static String ID_PAGE_IT031 = "Out_ReportIT031";
  /** 画面ID：商品マスタ 自動発注停止（店別） */
  public final static String ID_PAGE_IT032 = "Out_ReportIT032";
  /** 画面ID：部門マスタ検索画面 */
  public final static String ID_PAGE_X021 = "Out_Reportx021";
  /** 画面ID：部門マスタ登録画面 */
  public final static String ID_PAGE_X022 = "Out_Reportx022";
  /** 画面ID：大分類マスタ検索・登録画面 */
  public final static String ID_PAGE_X031 = "Out_Reportx031";
  /** 画面ID：中分類マスタ検索・登録画面 */
  public final static String ID_PAGE_X032 = "Out_Reportx032";
  /** 画面ID：小分類マスタ検索・登録画面 */
  public final static String ID_PAGE_X033 = "Out_Reportx033";
  /** 画面ID：小小分類マスタ検索・登録画面 */
  public final static String ID_PAGE_X034 = "Out_Reportx034";
  /** 画面ID：大・中・小分類マスタ検索・登録画面 */
  public final static String ID_PAGE_X035 = "Out_Reportx035";
  /** 画面ID：リードタイムマスタ一覧画面 */
  public final static String ID_PAGE_X041 = "Out_Reportx041";
  /** 画面ID：リードタイムマスタ登録画面 */
  public final static String ID_PAGE_X042 = "Out_Reportx042";
  /** 画面ID：メーカーマスタ一覧画面 */
  public final static String ID_PAGE_X051 = "Out_Reportx051";
  /** 画面ID：メーカーマスタ登録画面 */
  public final static String ID_PAGE_X052 = "Out_Reportx052";
  /** 画面ID：メーカーマスタ一覧画面（2） */
  public final static String ID_PAGE_X053 = "Out_Reportx053";
  /** 画面ID：計量器マスタ一覧画面 */
  public final static String ID_PAGE_X091 = "Out_Reportx091";
  /** 画面ID：計量器マスタ一覧画面 */
  public final static String ID_PAGE_X092 = "Out_Reportx092";
  /** 画面ID：風袋マスタ一覧画面 */
  public final static String ID_PAGE_X101 = "Out_Reportx101";
  /** 画面ID：風袋マスタ登録画面 */
  public final static String ID_PAGE_X102 = "Out_Reportx102";
  /** 画面ID：店舗マスタ検索画面 */
  public final static String ID_PAGE_X111 = "Out_Reportx111";
  /** 画面ID：店舗マスタ登録画面 */
  public final static String ID_PAGE_X112 = "Out_Reportx112";
  /** 画面ID：店舗部門マスタ一覧画面 */
  public final static String ID_PAGE_X121 = "Out_Reportx121";
  /** 画面ID：店舗部門マスタ登録画面 */
  public final static String ID_PAGE_X122 = "Out_Reportx122";
  /** 画面ID：店舗曜日別発注部門マスタ一覧画面 */
  public final static String ID_PAGE_X131 = "Out_Reportx131";
  /** 画面ID：店舗休日マスタ一覧画面 */
  public final static String ID_PAGE_X141 = "Out_Reportx141";
  /** 画面ID：店舗休日マスタ登録画面 */
  public final static String ID_PAGE_X142 = "Out_Reportx142";
  /** 画面ID：店舗休日マスタ変更画面 */
  public final static String ID_PAGE_X143 = "Out_Reportx143";
  /** 画面ID：仕入先マスタ一覧画面 */
  public final static String ID_PAGE_X151 = "Out_Reportx151";
  /** 画面ID：仕入先マスタ登録画面 */
  public final static String ID_PAGE_X152 = "Out_Reportx152";
  /** 画面ID：複数仕入先マスタ検索・登録画面 */
  public final static String ID_PAGE_X161 = "Out_Reportx161";
  /** 画面ID：配送パターンマスタ一覧画面 */
  public final static String ID_PAGE_X171 = "Out_Reportx171";
  /** 画面ID：配送パターンマスタ登録画面 */
  public final static String ID_PAGE_X172 = "Out_Reportx172";
  /** 画面ID：名称マスタ検索画面 */
  public final static String ID_PAGE_X181 = "Out_Reportx181";
  /** 画面ID：商品店グループ検索画面 */
  public final static String ID_PAGE_X191 = "Out_Reportx191";
  /** 画面ID：商品店グループ登録画面 */
  public final static String ID_PAGE_X192 = "Out_Reportx192";
  /** 画面ID：配送グループ一覧画面 */
  public final static String ID_PAGE_X201 = "Out_Reportx201";
  /** 画面ID：配送グループ 新規登録 */
  public final static String ID_PAGE_X202 = "Out_Reportx202";
  /** 画面ID：配送グループ 店グループ 一覧 参照 */
  public final static String ID_PAGE_X203 = "Out_Reportx203";
  /** 画面ID：配送グループ 店グループ 一覧 新規・変更 */
  public final static String ID_PAGE_X204 = "Out_Reportx204";
  /** 画面ID：プライスカード新規画面 */
  public final static String ID_PAGE_X211 = "Out_Reportx211";
  /** 画面ID：プライスカード一覧 参照 */
  public final static String ID_PAGE_X212 = "Out_Reportx212";
  /** 画面ID：プライスカード（枚数指定）新規・変更・参照 */
  public final static String ID_PAGE_X213 = "Out_Reportx213";
  /** 画面ID：プライスカード（同一枚数）新規・変更・参照 */
  public final static String ID_PAGE_X214 = "Out_Reportx214";
  /** 画面ID：プライスカード（店、構成ページ、枚数指定）新規・変更・参照 */
  public final static String ID_PAGE_X215 = "Out_Reportx215";
  /** 画面ID：プライスカード（店、部門、枚数指定）新規・変更・参照 */
  public final static String ID_PAGE_X216 = "Out_Reportx216";
  /** 画面ID：プライスカード（店指定）新規・変更・参照 */
  public final static String ID_PAGE_X217 = "Out_Reportx217";
  /** 画面ID：プライスカード（全店）新規・変更・参照 */
  public final static String ID_PAGE_X218 = "Out_Reportx218";
  /** 画面ID：コースマスタ登録 検索 */
  public final static String ID_PAGE_X221 = "Out_Reportx221";

  /* 特売 */
  /** 画面ID：催しコード 週刊催し一覧画面 */
  public final static String ID_PAGE_TM001 = "Out_ReportTM001";
  /** 画面ID：催しコード 週刊催し登録画面 */
  public final static String ID_PAGE_TM002 = "Out_ReportTM002";
  /** 画面ID：催しコード その他催し一覧画面 */
  public final static String ID_PAGE_TM003 = "Out_ReportTM003";
  /** 画面ID：催しコード その他催し登録画面 */
  public final static String ID_PAGE_TM004 = "Out_ReportTM004";
  /** 画面ID：催しコード 部門一覧画面 */
  public final static String ID_PAGE_TM005 = "Out_ReportTM005";
  /** 画面ID：特売アンケート状況 催し一覧画面 */
  public final static String ID_PAGE_TG012 = "Out_ReportTG012";
  /** 画面ID：特売アンケート状況 催し・グループ別画面 */
  public final static String ID_PAGE_TG013 = "Out_ReportTG013";
  /** 画面ID：特売アンケート状況 グループ・店別画面 */
  public final static String ID_PAGE_TG014 = "Out_ReportTG014";
  /** 画面ID：特売アンケート状況 グループ・店別画面 */
  public final static String ID_PAGE_TG015 = "Out_ReportTG015";
  /** 画面ID：特売アンケート状況 売価選択(一括)画面 */
  public final static String ID_PAGE_SA003 = "Out_ReportSA003";
  /** 画面ID：特売アンケート状況 売価・商品選択画面 */
  public final static String ID_PAGE_SA004 = "Out_ReportSA004";
  /** 画面ID：特売アンケート状況 売価差替画面 */
  public final static String ID_PAGE_SA005 = "Out_ReportSA005";
  /** 画面ID：特売アンケート状況 売価差替画面 */
  public final static String ID_PAGE_SA008 = "Out_ReportSA008";
  /** 画面ID：予約発注 企画-新規・変更 企画一覧 */
  public final static String ID_PAGE_YH000 = "Out_ReportYH000";
  /** 画面ID：予約発注 企画-新規・変更 企画情報 */
  public final static String ID_PAGE_YH001 = "Out_ReportYH001";
  /** 画面ID：予約発注 企画-新規・変更 商品情報 */
  public final static String ID_PAGE_YH002 = "Out_ReportYH002";
  /** 画面ID：予約発注 企画-修正 企画情報 */
  public final static String ID_PAGE_YH201 = "Out_ReportYH201";
  /** 画面ID：予約発注 企画-修正 商品情報 */
  public final static String ID_PAGE_YH202 = "Out_ReportYH202";
  /** 画面ID：予約発注 企画-修正 店別発注数量 */
  public final static String ID_PAGE_YH203 = "Out_ReportYH203";

  /** 画面ID：新装店発注 企画-修正 商品情報 */
  public final static String ID_PAGE_SK000 = "Out_ReportSK000";
  /** 画面ID：新装店発注 新改店発注商品別 一覧 */
  public final static String ID_PAGE_SK002 = "Out_ReportSK002";
  /** 画面ID：新装店発注 新改店発注商品別 新規 */
  public final static String ID_PAGE_SK003 = "Out_ReportSK003";
  /** 画面ID：新装店発注 新改店発注構成別 一覧 */
  public final static String ID_PAGE_SK005 = "Out_ReportSK002";
  /** 画面ID：新装店発注 新改店発注構成別 新規 */
  public final static String ID_PAGE_SK006 = "Out_ReportSK003";

  /** 画面ID：新装店発注 新改店発注商品別 変更 */
  public final static String ID_PAGE_SK008 = "Out_ReportSK003";
  /** 画面ID：新装店発注 新改店発注商品別 参照 */
  public final static String ID_PAGE_SK009 = "Out_ReportSK003";
  /** 画面ID：新装店発注 新改店発注構成別 変更 */
  public final static String ID_PAGE_SK010 = "Out_ReportSK003";
  /** 画面ID：新装店発注 新改店発注構成別 参照 */
  public final static String ID_PAGE_SK011 = "Out_ReportSK003";

  /** 画面ID：月間販売計画（チラシ計画） 催し一覧 */
  public final static String ID_PAGE_TG001 = "Out_ReportTG001";
  /** 画面ID：月間販売計画（チラシ計画） 店舗グループ一覧 */
  public final static String ID_PAGE_TG002 = "Out_ReportTG002";
  /** 画面ID：月間販売計画（チラシ計画） 店舗グループ一店舗情報 */
  public final static String ID_PAGE_TG003 = "Out_ReportTG003";
  /** 画面ID：月間販売計画（チラシ計画） 商品一覧 */
  public final static String ID_PAGE_TG008 = "Out_ReportTG008";
  /** 画面ID：月間販売計画（チラシ計画） 変更申請商品一覧 */
  public final static String ID_PAGE_TG009 = "Out_ReportTG009";
  /** 画面ID：月間販売計画（チラシ計画） 商品情報一覧 */
  public final static String ID_PAGE_TG016 = "Out_ReportTG016";
  /** 画面ID：月間販売計画（チラシ計画） 販売店確認 */
  public final static String ID_PAGE_TG018 = "Out_ReportTG018";
  /** 画面ID：月間販売計画（チラシ計画） 納入店確認 */
  public final static String ID_PAGE_TG019 = "Out_ReportTG019";
  /** 画面ID：月間販売計画（チラシ計画） コピー元店舗グループ */
  public final static String ID_PAGE_TG040 = "Out_ReportTG040";
  /** 画面ID：特売・スポット計画 店別数量 */
  public final static String ID_PAGE_ST021 = "Out_ReportST021";

  /** 画面ID：特売・スポット計画 催し一覧 */
  public final static String ID_PAGE_TG017 = "Out_ReportTG017";
  /** 画面ID：特売・スポット計画 商品一覧 */
  public final static String ID_PAGE_ST016 = "Out_ReportST016";
  /** 画面ID：特売・スポット計画 コピー元商品選択 */
  public final static String ID_PAGE_ST019 = "Out_ReportST019";
  /** 画面ID：特売・スポット計画 CSV取込 */
  public final static String ID_PAGE_ST022 = "Out_ReportST022";
  /** 画面ID：特売・スポット計画 特売原稿｜店別数量｜店一括数量CSV取込 */
  public final static String ID_PAGE_ST024 = "Out_ReportST024";
  /** 画面ID：特売・スポット計画 店別数量訂正 */
  public final static String ID_PAGE_TG020 = "Out_ReportTG020";

  /** 画面ID：ランクパターンマスタ 数量パターンマスタ 一覧 */
  public final static String ID_PAGE_RP003 = "Out_ReportRP003";
  /** 画面ID：ランクパターンマスタ 数量パターンマスタ ランク別数量 */
  public final static String ID_PAGE_RP004 = "Out_ReportRP004";
  /** 画面ID：ランクパターンマスタ 通常率パターンマスタ 一覧 */
  public final static String ID_PAGE_RP005 = "Out_ReportRP005";
  /** 画面ID：ランクパターンマスタ 率パターン 店別分配率 新規・変更・参照 */
  public final static String ID_PAGE_RP006 = "Out_ReportRP006";
  /** 画面ID：ランクパターンマスタ ランク 店コピー */
  public final static String ID_PAGE_RP007 = "Out_ReportRP007";
  /** 画面ID：ランクパターンマスタ 数量計算 */
  public final static String ID_PAGE_RP008 = "Out_ReportRP008";
  /** 画面ID：ランクパターンマスタ 数量計算 店別数量展開 */
  public final static String ID_PAGE_RP009 = "Out_ReportRP009";
  /** 画面ID：ランクパターンマスタ 通常率パターンマスタ CSV取込 */
  public final static String ID_PAGE_RP011 = "Out_ReportRP011";
  /** 画面ID：ランクパターンマスタ ランクマスタ 店情報 新規・変更 */
  public final static String ID_PAGE_ST008 = "Out_ReportST008";
  /** 画面ID：ランクパターンマスタ ランクマスタ 店情報 参照 */
  public final static String ID_PAGE_ST007 = "Out_ReportST007";
  /** 画面ID：ランクパターンマスタ ランクマスタ 一覧 */
  public final static String ID_PAGE_ST010 = "Out_ReportST010";
  /** 画面ID：ランクパターンマスタ ランクマスタ実績 参照 */
  public final static String ID_PAGE_ST011 = "Out_ReportST011";
  /** 画面ID：ランクパターンマスタ ランクマスタ コピー元ランクNO選択 */
  public final static String ID_PAGE_ST015 = "Out_ReportST015";

  /** 画面ID：事前打出し 新規・更新 催し一覧 */
  public final static String ID_PAGE_JU001 = "Out_ReportJU001";
  /** 画面ID： 事前打出し 参照・新規・変更 商品一覧 */
  public final static String ID_PAGE_JU011 = "Out_ReportJU011";
  /** 画面ID： 事前打出し 新規 商品 */
  public final static String ID_PAGE_JU012 = "Out_ReportJU012";
  /** 画面ID： 事前打出し 参照・変更 商品情報 */
  public final static String ID_PAGE_JU013 = "Out_ReportJU013";
  /** 画面ID： 店舗アンケート付き送付け 新規 催し */
  public final static String ID_PAGE_JU022 = "Out_ReportJU022";
  /** 画面ID： 店舗アンケート付き送付け 新規・変更 催し一覧 */
  public final static String ID_PAGE_JU027 = "Out_ReportJU027";
  /** 画面ID： 店舗アンケート付き送付け 新規・変更 商品一覧 */
  public final static String ID_PAGE_JU031 = "Out_ReportJU031";
  /** 画面ID： 店舗アンケート付き送付け 新規 商品情報 */
  public final static String ID_PAGE_JU032 = "Out_ReportJU032";
  /** 画面ID： 店舗アンケート付き送付け 新規 商品情報 */
  public final static String ID_PAGE_JU033 = "Out_ReportJU033";
  /** 画面ID： 事前打出し CSV取込 */
  public final static String ID_PAGE_JU037 = "Out_ReportJU037";
  /** 画面ID： 店舗アンケート付き送付け CSV取込 */
  public final static String ID_PAGE_JU038 = "Out_ReportJU038";

  /** 画面ID：冷凍食品 CSV取込 */
  public final static String ID_PAGE_BW000 = "Out_ReportBW000";
  /** 画面ID：冷凍食品 新規・変更・参照 商品一覧 */
  public final static String ID_PAGE_BW002 = "Out_ReportBW002";
  /** 画面ID：冷凍食品 冷凍食品 CSV取込 エラー修正 */
  public final static String ID_PAGE_BW004 = "Out_ReportBW004";
  /** 画面ID：冷凍食品 CSV取込 エラー選択 */
  public final static String ID_PAGE_BW003 = "Out_ReportBW003";
  /** 画面ID：冷凍食品 新規・変更 企画一覧画面 */
  public final static String ID_PAGE_BW005 = "Out_ReportBW005";
  /** 画面ID：冷凍食品 CSV取込 エラーリスト画面 */
  public final static String ID_PAGE_BW006 = "Out_ReportBW006";

  /** 画面ID：催し別送信情報 B/M別 新規・変更 催し一覧画面 */
  public final static String ID_PAGE_BM003 = "Out_ReportBM003";
  /** 画面ID：催し別送信情報 B/M別 新規・変更・参照 明細画面 */
  public final static String ID_PAGE_BM006 = "Out_ReportBM006";
  /** 画面ID：催し別送信情報 新規・変更 CSV取込画面 */
  public final static String ID_PAGE_BM007 = "Out_ReportBM007";
  /** 画面ID：催し別送信情報 B/M別 新規・変更 B/M番号一覧画面 */
  public final static String ID_PAGE_BM010 = "Out_ReportBM010";
  /** 画面ID：催し別送信情報 新規・変更 CSV取込 エラー選択画面 */
  public final static String ID_PAGE_BM013 = "Out_ReportBM013";
  /** 画面ID：催し別送信情報 新規・変更 CSV取込 エラーリスト画面 */
  public final static String ID_PAGE_BM014 = "Out_ReportBM014";
  /** 画面ID：催し別送信情報 対象店確認画面 */
  public final static String ID_PAGE_BM015 = "Out_ReportBM015";
  /** 画面ID：分類割合 新規・変更 一覧 */
  public final static String ID_PAGE_BT004 = "Out_ReportBT004";
  /** 画面ID：分類割合 新規・変更 */
  public final static String ID_PAGE_BT002 = "Out_ReportBT002";
  /** 画面ID：分類割合 参照 */
  public final static String ID_PAGE_BT001 = "Out_ReportBT001";
  /** 画面ID：生活応援 新規・変更 催し一覧 */
  public final static String ID_PAGE_SO001 = "Out_ReportSO001";
  /** 画面ID：生活応援 部門別件数 */
  public final static String ID_PAGE_SO002 = "Out_ReportSO002";
  /** 画面ID：生活応援 商品一覧 */
  public final static String ID_PAGE_SO003 = "Out_ReportSO003";
  /** 画面ID：生活応援 CVS取込 */
  public final static String ID_PAGE_SO004 = "Out_ReportSO004";
  /** 画面ID：生活応援 CVS取込 エラー選択 */
  public final static String ID_PAGE_SO006 = "Out_ReportSO006";
  /** 画面ID：生活応援 CVS取込 エラーリスト */
  public final static String ID_PAGE_SO007 = "Out_ReportSO007";
  /** 画面ID：商品コード履歴検索 生活応援 催し一覧 */
  public final static String ID_PAGE_SR003 = "Out_ReportSR003";
  /** 画面ID：商品コード履歴検索 商品選択 */
  public final static String ID_PAGE_SH001 = "Out_ReportSH001";
  /** 画面ID：定量 通常 新規画面 */
  public final static String ID_PAGE_TR001 = "Out_ReportTR001";
  /** 画面ID：定量 通常 変更 一覧画面 */
  public final static String ID_PAGE_TR004 = "Out_ReportTR004";
  /** 画面ID：定量 通常 店舗別数量 変更画面 */
  public final static String ID_PAGE_TR005 = "Out_ReportTR005";
  /** 画面ID：定量 CSV取込データ CSV取込画面 */
  public final static String ID_PAGE_TR006 = "Out_ReportTR006";
  /** 画面ID：定量 CSV取込データ 変更画面 */
  public final static String ID_PAGE_TR007 = "Out_ReportTR007";
  /** 画面ID：定量 一括追加・削除画面 */
  public final static String ID_PAGE_TR016 = "Out_ReportTR016";

  /** 画面ID：催し検索 変更・参照 催し一覧 */
  public final static String ID_PAGE_MM001 = "Out_ReportMM001";
  /** 画面ID：催し検索 変更・参照 商品一覧 */
  public final static String ID_PAGE_MM002 = "Out_ReportMM002";
  /** 画面ID：催し検索 変更・参照 店舗一覧 */
  public final static String ID_PAGE_MM003 = "Out_ReportMM003";

  /** 画面ID：予約発注 一覧 企画一覧 */
  public final static String ID_PAGE_HY001 = "Out_ReportHY001";
  /** 画面ID：予約発注 一覧 商品一覧 */
  public final static String ID_PAGE_HY002 = "Out_ReportHY002";
  /** 画面ID：予約発注 一覧 発注数量一覧 */
  public final static String ID_PAGE_HY003 = "Out_ReportHY003";

  /* 店舗特売 */
  /** 画面ID：定量数量訂正 正規商品指定 */
  public final static String ID_PAGE_HT002 = "Out_ReportHT002";
  /** 画面ID：定量数量訂正 正規曜日指定 */
  public final static String ID_PAGE_HT004 = "Out_ReportHT004";
  /** 画面ID：定量数量訂正 次週商品指定 */
  public final static String ID_PAGE_HT007 = "Out_ReportHT007";
  /** 画面ID：定量数量訂正 次週曜日指定 */
  public final static String ID_PAGE_HT009 = "Out_ReportHT009";

  /** 画面ID：商品マスタ検索 */
  public final static String ID_PAGE_MI001 = "Out_ReportMI001";

  /** 画面ID：アンケート発注一覧 */
  public final static String ID_PAGE_TU001 = "Out_ReportTU001";
  /** 画面ID：アンケート発注 数量入力 */
  public final static String ID_PAGE_TU002 = "Out_ReportTU002";
  /** 画面ID：鮮魚 特売計画＆事前発注 */
  public final static String ID_PAGE_TJ001 = "Out_ReportTJ001";
  /** 画面ID：鮮魚 特売計画＆事前発注 構成比 */
  public final static String ID_PAGE_TJ002 = "Out_ReportwinTJ002";
  /** 画面ID：鮮魚 特売計画＆事前発注 分類明細 */
  public final static String ID_PAGE_TJ003 = "Out_ReportwinTJ003";
  /** 画面ID：精肉 特売計画＆事前発注 */
  public final static String ID_PAGE_TJ005 = "Out_ReportTJ005";
  /** 画面ID：精肉 特売計画＆事前発注 構成比 */
  public final static String ID_PAGE_TJ006 = "Out_ReportwinTJ006";
  /** 画面ID：精肉 特売計画＆事前発注 分類明細 */
  public final static String ID_PAGE_TJ007 = "Out_ReportwinTJ007";
  /** 画面ID：精肉 特売計画＆事前発注 */
  public final static String ID_PAGE_TJ009 = "Out_ReportTJ009";
  /** 画面ID：ディリ－ 特売計画＆事前発注 */
  public final static String ID_PAGE_TJ011 = "Out_ReportTJ011";
  /** 画面ID：事前発注 部門選択 */
  public final static String ID_PAGE_TJ014 = "Out_ReportTJ014";
  /** 画面ID：事前発注 週一覧 */
  public final static String ID_PAGE_TJ015 = "Out_ReportTJ015";

  /** 画面ID：発注情報検索 */
  public final static String ID_PAGE_X231 = "Out_Reportx231";

  /** 画面ID：セット販売 催し一覧 */
  public final static String ID_PAGE_GM001 = "Out_ReportGM001";
  /** 画面ID：セット販売 セット番号一覧 */
  public final static String ID_PAGE_GM002 = "Out_ReportGM002";
  /** 画面ID：セット販売 明細 */
  public final static String ID_PAGE_GM003 = "Out_ReportGM003";

  /** 画面ID：ユーザー情報一覧 */
  public final static String ID_PAGE_X241 = "Out_Reportx241";
  public final static String ID_PAGE_X242 = "Out_Reportx242";
  /** 画面ID：ユーザー履歴管理画面 */
  public final static String ID_PAGE_X243 = "Out_Reportx243";
  /** 画面ID：商品マスタ履歴管理画面 */
  public final static String ID_PAGE_X244 = "Out_Reportx244";
  /** 画面ID：商品マスタ検索画面 */
  public final static String ID_PAGE_X261 = "Out_Reportx261";
  /** 画面ID：商品登録限度数画面 */
  public final static String ID_PAGE_X271 = "Out_Reportx271";

  /* Web商談 */
  /** 画面ID：提案件名一覧 */
  public final static String ID_PAGE_X245 = "Out_Reportx245";
  /** 画面ID：提案商品一覧 */
  public final static String ID_PAGE_X246 = "Out_Reportx246";
  /** 画面ID：提案商品登録 */
  public final static String ID_PAGE_X247 = "Out_Reportx247";
  /** 画面ID：CSV提案商品登録 */
  public final static String ID_PAGE_X248 = "Out_Reportx248";
  /** 画面ID：提案商品検索 */
  public final static String ID_PAGE_X249 = "Out_Reportx249";
  /** 画面ID：仕掛商品検索 */
  public final static String ID_PAGE_X250 = "Out_Reportx250";
  /** 画面ID：仕掛商品登録 */
  public final static String ID_PAGE_X251 = "Out_Reportx251";
  /** 画面ID：提案商品CSVエラーリスト */
  public final static String ID_PAGE_X252 = "Out_Reportx252";
  /** 画面ID：提案商品CSV選択 */
  public final static String ID_PAGE_X253 = "Out_Reportx253";
  /** 画面ID：商品検索画面 */
  public final static String ID_PAGE_X280 = "Out_Reportx280";

  /** PL系ページ情報 */
  public final static String[] ID_PAGE_PL = new String[] {ID_PAGE_001};

  public static boolean isPLPage(String page) {
    return ArrayUtils.contains(ID_PAGE_PL, page);
  }

  /** 閉店店舗表示画面 */
  public final static String[] ID_PAGE_DISP_CLOSED = new String[] {ID_PAGE_001};

  public static boolean isPastPage(String page) {
    return ArrayUtils.contains(ID_PAGE_DISP_CLOSED, page);
  }

  /** 子画面 */
  public final static String[] ID_PAGE_CHILD = new String[] {ID_PAGE_ITEM};

  /** 子画面か否か */
  public static boolean isChildPage(String page) {
    return ArrayUtils.contains(ID_PAGE_CHILD, page);
  }

  /** 特殊ページ情報:パスワード */
  public final static String ID_ADMIN_PASS_HEAD_042 = "ina";

  /**
   * HTML 関連
   */
  public final static String LBL_SUFFIX = " ：";

  /** HTML 関連項目の基本情報 */
  public interface Item {
    public String getObj();

    public String getTxt();
  }

  /** 指定した項目の全オブジェクト名を取得 */
  public static String[] getItemObjs(Item[] enumCls) {
    String[] objs = new String[] {};
    for (Item itm : enumCls) {
      objs = (String[]) ArrayUtils.add(objs, itm.getObj());
    }
    return objs;
  }

  /** 指定した項目でオブジェクト名が一致する項目を取得 */
  public static Item getItemData(Item[] enumCls, String obj) {
    for (Item itm : enumCls) {
      if (StringUtils.equals(obj, itm.getObj())) {
        return itm;
      }
    }
    return null;
  }

  /** HTML関連 ボタン */
  public enum Button implements Item {
    /** 検索 */
    SEARCH("btn_search", "検索"),
    /** Excel */
    EXCEL("btn_excel", "Excel"),
    /** 商品入力 */
    INPUT("btn_input", ""),
    /** 店舗グループ入力 */
    INPUT_TENPOG("btn_input_TenpoG", ""),
    /** 保存 */
    ENTRY("btn_entry", "保存"),
    /** 保存（定義保存） */
    ENTRY_SHIORI("btn_entry_shiori", "保存"),
    /** 保存（店舗グループ） */
    ENTRY_TENPOG("btn_entry-tg", "保存"),
    /** 呼出 */
    CALL("btn_call", "呼出"),
    /** 呼出（店舗グループ） */
    CALL_TENPOG("btn_call-tg", "呼出"),
    /** 削除 */
    DELETE("btn_delete", "削除"),
    /** 削除（店舗グループ） */
    DELETE_TENPOG("btn_delete-tg", "削除"),
    /** 削除（定義保存） */
    DELETE_SHIORI("btn_delete_shiori", "削除"),
    /** 適用（定義保存） */
    VIEW_SHIORI("btn_view_shiori", "適用"),
    /** 条件リセット */
    RESET("btn_reset", "戻る"),
    /** (特殊ページ)ログイン */
    LOGIN("btn_login", "ログイン"),
    /** キャンセル */
    CANCEL("btn_cancel", "キャンセル"),
    /** アップロード */
    UPLOAD("btn_upload", "アップロード"),
    /** ダウンロード */
    DOWNLOAD("btn_download", "ダウンロード"),
    /** 検索＋Excel出力 */
    SEARCH_EXCEL("btn_srcexcel", "テンプレート出力"),
    /** 戻る */
    BACK("btn_back", "戻る"),
    /** ページクリア */
    CLEAR("btn_clear", "ページクリア")

    /** 登録 */
    ,UPD("btn_upd", "登録")
    /** 削除 */
    ,DEL("btn_del", "削除")
    /** 全削除 */
    ,ALL_DEL("btn_all_del", "削除")
    /** 新規 */
    ,NEW("btn_new", "新規")
    /** コピー */
    ,COPY("btn_copy", "コピー")
    /** 選択 */
    ,SELECT("btn_select", "選択")
    /** 選択コピー */
    ,SEL_COPY("btn_sel_copy", "コピー")
    /** 選択（変更） */
    ,SEL_CHANGE("btn_sel_change", "変更")
    /** 選択（参照） */
    ,SEL_REFER("btn_sel_refer", "選択（参照）")
    /** 選択（確定） */
    ,SEL_KAKUTEI("btn_sel_kakutei", "選択（確定）")
    /** 選択（表示） */
    ,SEL_VIEW("btn_sel_view", "選択（表示）")
    /** 選択（店別分配率） */
    ,SEL_TENBETUBRT("btn_sel_tenbetubrt", "選択（店別分配率）")
    /** 選択（商品情報） */
    ,SEL_SHNINFO("btn_sel_shninfo", "選択（商品情報）")

    /** CSVエラー選択 */
    ,SEL_CSVERR("btn_sel_csverr", "CSVエラー選択")
    /** ファイル選択 */
    ,FILE("btn_file", "ファイル選択")
    /** 計算 */
    ,CALC("btn_calc", "計算")
    /** CSV出力 */
    ,CSV("btn_csv", "CSV出力")
    /** CSV取込 */
    ,CSV_IMPORT("btn_csv_import", "CSV取込")
    /** CSV予約取込 */
    ,CSV_IMPORT_YYK("btn_csv_import_yyk", "CSV予約取込")
    /** エラーリスト出力 */
    ,ERR_LIST("btn_err_list", "エラーリスト出力")
    /** エラー修正 */
    ,ERR_CHANGE("btn_err_change", "エラー修正")
    /** 正 */
    ,SEI("btn_sei", "正")
    /** 予約1 */
    ,YOYAKU1("btn_yoyaku1", "予1")
    /** 予約2 */
    ,YOYAKU2("btn_yoyaku2", "予2")
    /** 開始 */
    ,START("btn_start", "開始")
    /** 停止 */
    ,STOP("btn_stop", "停止")
    /** 変更許可 */
    ,KYOKA("btn_kyoka", "変更許可")

    /** タブ別登録 */
    ,UPD_TAB1("btn_tab1", "登録")
    /** タブ別登録 */
    ,UPD_TAB2("btn_tab2", "登録")
    /** タブ別キャンセル */
    ,CANCEL_TAB2("btn_cancel2", "キャンセル")
    /** タブ別リターン */
    ,BACK_TAB2("btn_back2", "リターン")

    /** プライスカード */
    ,SAKUBAIKAKB1("btn_sakubaikakb1", "標準売価で作成"), SAKUBAIKAKB2("btn_sakubaikakb2", "店別売価で作成")

    /** 選択（店グループ一覧） */
    ,SEL_REFER_TENG("btn_sel_refer_teng", "選択（店グループ一覧）")

    /** 催し別送信情報 */
    ,CHECKLIST("btn_checklist", "チェックリスト"), TAISYOTEN("btn_rankno", "対象店"), JYOGAITEN("btn_rankno", "除外店"), TENKAKUNIN("btn_tenkakunin", "店確認")

    /** ランクマスタ */
    ,JISSEKIREFER("btn_jissekirefer", "実績参照"), TENNOORDER("btn_tennoorder", "店番順"), RANKORDER("btn_rankorder", "ランク順"), JISSEKIORDER("btn_jissekiorder", "実績順"), SET("btn_set", "設定")

    /** 催し検索 */
    ,TENNOVIEW("btn_tennoview", "店番表示")

    /** ---- サブウインドウ呼出 ---- **/
    /** 汎用 ※_winXXXXと画面IDをつけて利用する想定 */
    ,SUB("btn_sub", "サブ")

    /** メーカー検索 */
    ,MAKER("btn_maker", "メーカー検索（IT001_1）")
    /** 商品番号検索 */
    ,SHNCD("btn_shncd", "商品番号検索")
    /** 仕入先検索 */
    ,SIR("btn_sir", "仕入先検索（IT001_2）")
    /** 店グループ検索 */
    ,TENGP("btn_tengp", "店グループ検索（IT002_1）")
    /** 配送パターン検索 */
    ,HSPTN("btn_hsptn", "配送パターン検索（SJ002_2）")
    /** エリア別配送パターン検索 */
    ,EHSPTN("btn_ehsptn", "エリア配送パターン検索（SJ002_3）")
    /** 配送グループ検索 */
    ,HSGP("btn_hsgp", "配送グループ検索（HP011_1）")
    /** 店舗一覧検索 */
    ,TENPO("btn_tenpo", "店舗一覧検索（IT002_5）")
    /** 風袋検索 */
    ,FUTAI("btn_futai_search", "風袋検索")
    /** アレルギー */
    ,ALLERGY("btn_allergy", "アレルギー")
    /** 添加物 */
    ,TENKABUTSU("btn_tenkabutsu", "添加物")
    /** ソースコード */
    ,SRCCD("btn_srccd", "ソースコード")
    /** ランク選択 */
    ,RANK("btn_rank", "ランク")
    /** 臨時ランク作成 */
    ,RINZIRANK("btn_rinzirank", "ランクマスター一覧（ST010）")
    /** 数量パターン検索 */
    ,SURYO("btn_suryo", "数量")
    /** 店別数量展開 */
    ,TENBETUSU("btn_tenbetusu", "店別数量展開（ST021）")
    /** 対象店舗情報 */
    ,RANKNO("btn_rankno", "ランクNo.選択（ST009）")
    /** 数値展開方法 */
    ,TENKAI("btn_tenkai", "数値展開方法（ST020）")
    /** ランク店情報 */
    ,RANKTENINFO("btn_runkTenTnfo", "ランク店情報（ST007）")
    /** ランク店情報 */
    ,TENINFO("btn_teninfo", "ランク店情報（ST008）")
    /** 構成比 */
    ,KOUSEIHI("btn_kouseihi", "構成比")
    /** 分類明細 */
    ,BUNRUIMEISAI("btn_bunruimeisai", "分類明細")
    /** 再計算 */
    ,SAIKEISAN("btn_saikeisan", "再計算")
    /** 計画計表示 */
    ,KEIKAKU("btn_keikaku", "計画計表示（TJ010）")
    /** 計画計表示 */
    ,ADDLINE("btn_addline", "行追加")
    /** 実績参照 */
    ,ZITREF("btn_zitref", "実績参照（TJ011）")

    /* Web商談 */
    /** 作成中 */
    ,PREV("btn_prev", "作成中")
    /** 確定 */
    ,NEXT("btn_next", "確定")
    /** 仕掛 */
    ,NEXT_SHIKAKARI("btn_shikakari", "仕掛")
    /** 戻る */
    ,RETURN("btn_return", "戻る");

    private final String obj;
    private final String txt;

    /** 初期化 */
    private Button(String obj, String txt) {
      this.obj = obj;
      this.txt = txt;
    }

    /** @return obj Object名 */
    @Override
    public String getObj() {
      return obj;
    }

    /** @return txt 表示名称 */
    @Override
    public String getTxt() {
      return txt;
    }
  }

  /** HTML関連 選択リスト */
  public enum Select implements Item {
    /** 期間 */
    KIKAN("SelKikan", "期間"),
    /** 期間FROM */
    KIKAN_F("SelKikanF", "開始"),
    /** 期間TO */
    KIKAN_T("SelKikanT", "終了"),
    /** 年月日FROM */
    YMD_F("SelYmdF", "年月日"),
    /** 年月日TO */
    YMD_T("SelYmdT", "年月日"),
    /** 年月FROM */
    YM_F("SelYmF", "年月"),
    /** 年月TO */
    YM_T("SelYmT", "年月"),
    /** 年FROM */
    YEAR_F("SelYearF", "年"),
    /** 年TO */
    YEAR_T("SelYearT", "年"),
    /** 週FROM */
    WEEK_F("SelWeekF", "週"),
    /** 週TO */
    WEEK_T("SelWeekT", "週"),
    /** 年度FROM */
    FYEAR_F("SelFYearF", "年度"),
    /** 年度TO */
    FYEAR_T("SelFYearT", "年度"),
    /** 比較期間FROM */
    KIKAN_F2("SelKikanF2", "開始"),
    /** 期間TO */
    KIKAN_T2("SelKikanT2", "終了"),
    /** 比較比較年月日FROM */
    YMD_F2("SelYmdF2", "比較年月日"),
    /** 比較年月日TO */
    YMD_T2("SelYmdT2", "比較年月日"),
    /** 比較年月FROM */
    YM_F2("SelYmF2", "比較年月"),
    /** 比較年月TO */
    YM_T2("SelYmT2", "比較年月"),
    /** 比較年FROM */
    YEAR_F2("SelYearF2", "比較年"),
    /** 比較年TO */
    YEAR_T2("SelYearT2", "比較年"),
    /** 比較週FROM */
    WEEK_F2("SelWeekF2", "比較週"),
    /** 比較週TO */
    WEEK_T2("SelWeekT2", "比較週"),
    /** 比較年度FROM */
    FYEAR_F2("SelFYearF2", "年度"),
    /** 比較年度TO */
    FYEAR_T2("SelFYearT2", "年度"),
    /** 店舗グループ */
    TENPO_G("SelTenpoG", "種別"),
    /** 店舗 */
    TENPO("SelTenpo", "店舗"),
    /** 企業 */
    KIGYO("SelKigyo", "企業"),
    /** 販売統括部門 */
    HANTOUBU("SelHanToubu", ""),
    /** 販売部 */
    HANBAIBU("SelHanbaibu", "販売部"),
    /** 部門グループ */
    BUMON_G("SelBumonG", "部門グ"),
    /** 部門 */
    BUMON("SelBumon", "部門"),
    /** 大分類 */
    DAI_BUN("SelDaiBun", "大分類"),
    /** 中分類 */
    CHU_BUN("SelChuBun", "中分類"),
    /** 小分類 */
    SHO_BUN("SelShoBun", "小分類"),
    /** 商品 */
    SYOHIN("SelSyohin", "商品"),
    /** 条件 */
    WHERE("SelWhere", ""),
    /** 商品カテゴリ */
    CATEGORY("SelCategory", "商品カテゴリ"),
    /** 商品区分 */
    SHNKBN("sel_shnkbn2", "商品区分"),
    /** 定義保存 */
    SHIORI("SelShiori", "定義保存"),
    /** セルセンター */
    CENTER("sel_center", "センターコード"),
    /** 便コード */
    SUPPLYNO("sel_supplyno", "便コード"),
    /** 催し区分 */
    MOYSKBN("sel_moyskbn", "催し区分"),
    /** センターORR */
    CENTER_ORR("sel_center_orr", "センターコード"),
    /** 便コードORR */
    SUPPLYNO_ORR("sel_supplyno_orr", "便コード"),
    /** 曜日 */
    YOBIKBN1("sel_yobikbn1", "曜日"),
    /** パスワード変更 */
    DISPLAY("sel_display", "パスワード変更")

    /** メッセージ一覧 */
    ,MSG_LIST("msg_list", "メッセージ一覧")

    /** 禁止文字一覧 */
    ,PROHIBITED_LIST("prohibited_list", "禁止文字一覧")

    /** 税率区分 */
    ,ZEIRTKBN("sel_zeirtkbn", "税率区分")
    /** 旧税率区分 */
    ,ZEIRTKBN_OLD("sel_zeirtkbn_old", "旧税率区分")
    /** 分類区分 */
    ,BNNRUIKBN("sel_bnnruikbn", "分類区分")
    /** リードタイムパターン */
    ,READTMPTN("sel_readtmptn", "リードタイムパターン")

    /** 週№ */
    ,SHUNO("sel_shuno", "週№")
    /** BY */
    ,BYCD("sel_bycd", "BY")
    /** BY */
    ,TENKN("sel_tenkn", "店舗名称(漢字)")
    /** 催しコード */
    ,MOYSCD("sel_moyscd", "催しコード")
    /** 週№ */
    ,SHUNOPERIOD("sel_shunoperiod", "週№")

    // パスワード変更用
    , AUTH("sel_auth", "権限")

    /* Web商談 */
    /** ライン */
    ,LINE("SelLine", "ライン")
    /** クラス */
    ,CLASS("SelClass", "クラス")
    /** 取引先 */
    ,TORIHIKI("SelTorihiki", "取引先")
    /** 提案件名 */
    ,TEIAN("SelTeian", "提案件名")
    /** 発注先 */
    ,HATYU("SelHatyu", "発注先")
    /** 状態_提案件名 **/
    ,STCD_KENMEI("SelStcdKenmei", "状態_提案件名")
    /** 状態_提案商品 **/
    ,STCD_TEIAN("SelStcdTeian", "状態_提案商品")
    /** 状態_仕掛商品 **/
    ,STCD_SHIKAKARI("SelStcdShikakari", "状態_仕掛商品");

    private final String obj;
    private final String txt;

    /** 初期化 */
    private Select(String obj, String txt) {
      this.obj = obj;
      this.txt = txt;
    }

    /** @return obj Object名 */
    @Override
    public String getObj() {
      return obj;
    }

    /** @return txt 表示名称 */
    @Override
    public String getTxt() {
      return txt;
    }
  }

  /** HTML関連 テキスト */
  public enum Text implements Item {
    /** 件数 */
    NUMBER("TxtNumber", "件数"),
    /** 件数 */
    NUMBER_MAX("TxtNumberMax", "表示件数"),

    /** ダイアログ内パスワード入力欄 */
    PASS("txt_pass", ""),
    /** ファイルパス */
    FILE("txt_file", "")

    /** 選択商品コード */
    ,SEL_SHNCD("txt_sel_shncd", "商品コード")
    /** 選択商品名（漢字） */
    ,SEL_SHNKN("txt_sel_shnkn", "商品名（漢字）")
    /** 選択部門コード */
    ,SEL_BMNCD("txt_sel_bmncd", "部門")
    /** 選択仕入先コード */
    ,SEL_SIRCD("txt_sel_sircd", "仕入先コード")
    /** レギュラー総売価 */
    ,RG_SOUBAIKA("txt_rg_soubaika", "総売価")
    /** レギュラー値入率 */
    ,RG_NEIRE("txt_rg_neire", "値入率")
    /** 販促総売価 */
    ,HS_SOUBAIKA("txt_hs_soubaika", "総売価")
    /** 販促値入率 */
    ,HS_NEIRE("txt_hs_neire", "値入率")
    /** 売価グループ総売価 */
    ,BG_SOUBAIKA("txt_bg_soubaika", "総売価")
    /** 売価グループ値入率 */
    ,BG_NEIRE("txt_bg_neire", "値入率")

    /** 予約件数 */
    ,YOYAKU("txt_yoyaku", "予約件数")
    /** 登録日 */
    ,ADDDT("txt_adddt", "登録日")
    /** 更新日 */
    ,UPDDT("txt_upddt", "更新日")
    /** 更新時刻 */
    ,UPDTM("txt_updtm", "更新時刻")
    /** オペレータ */
    ,OPERATOR("txt_operator", "オペレータ")

    /** ステータス */
    ,STATUS("txt_status", "ステータス")
    /** 取込件数 */
    ,UPD_NUMBER("txt_upd_number", "取込件数")
    /** エラー件数 */
    ,ERR_NUMBER("txt_err_number", "エラー件数")
    /** エラー件数 */
    ,TEN_NUMBER("txt_ten_number", "店舗数")

    /** テーブル区分 */
    ,TABLEKBN("txt_tablekbn", "テーブル区分")
    /** SEQ */
    ,SEQ("txt_seq", "SEQ")
    /** 入力番号 */
    ,INPUTNO("txt_inputno", "入力番号")
    /** CSV登録区分 */
    ,CSV_UPDKBN("txt_csv_updkbn", "CSV登録区分")

    /** 評価方法区分(kbn504) */
    ,HYOKAKBN("txt_hyokakbn", "評価方法区分")
    /** コード */
    ,CODE("txt_code", "コード")

    /** 処理日付 */
    ,SHORIDT("txt_shoridt", "処理日付")

    /** 処理日付曜日 */
    ,SHORIDTWEEK("txt_shoridtweek", "処理日付曜日")

    /** 週№期間 */
    ,SHUNOPERIOD("txt_shunoperiod", "週№期間")

    /** 特売総売価 */
    ,TOK_SOUBAIKA("txt_tok_soubaika", "総売価")
    /** 特売本体売価 */
    ,TOK_HONBAIKA("txt_tok_honbaika", "本体売価")

    , AREAKBN("txt_areakbn", "エリア区分")

    /* Web商談 */
    /** 取引先 */
    ,TORIHIKI("txt_torihiki", "取引先")
    /** 代表スキャニング */
    ,SCAN("txt_scan", "代表スキャニング")
    /** 商品名 */
    ,SHOHIN("txt_shohin", "商品名")
    /** 登録者 */
    ,TOROKU("txt_toroku", "登録者")
    /** 発注先 */
    ,HATYU("txt_hatyu", "発注先");

    private final String obj;
    private final String txt;

    /** 初期化 */
    private Text(String obj, String txt) {
      this.obj = obj;
      this.txt = txt;
    }

    /** @return obj Object名 */
    @Override
    public String getObj() {
      return obj;
    }

    /** @return txt 表示名称 */
    @Override
    public String getTxt() {
      return txt;
    }
  }

  /** HTML関連 名称マスタオブジェクト */
  public enum MeisyoSelect implements Item {
    /** 取引停止 */
    KBN101(101, "sel_teishikbn", "取引停止")
    /** PC区分 */
    ,KBN102(102, "sel_pckbn", "PC区分")
    /** 加工区分 */
    ,KBN103(103, "sel_kakokbn", "加工区分")
    /** 市場区分 */
    ,KBN104(104, "sel_ichibakbn", "市場区分")
    /** 商品種類 */
    ,KBN105(105, "sel_shnkbn", "商品種類")
    /** レギュラー情報_取扱フラグ */
    ,KBN106(106, "sel_rg_atsukflg", "レギュラー情報_取扱フラグ")
    /** レギュラー情報_一括伝票フラグ */
    ,KBN107(107, "sel_rg_idenflg", "レギュラー情報_一括伝票フラグ")
    /** レギュラー情報_ワッペン */
    ,KBN108(108, "sel_rg_wapnflg", "ワッペン")
    /** 販促情報_取扱フラグ */
    ,KBN109(109, "sel_hs_atsukflg", "販促情報_取扱フラグ")
    /** 販促情報_ワッペン */
    ,KBN110(110, "sel_hs_wapnflg", "ワッペン")
    /** 販促情報_特売ワッペン */
    ,KBN111(111, "sel_hp_swapnflg", "特売ワッペン")
    /** 規格_単位 */
    ,KBN112(112, "sel_kikaku_tani", "規格_単位")
    /** ユニットプライス_ユニット単位 */
    ,KBN113(113, "sel_up_tanikbn", "ユニット単位")
    /** PB区分 */
    ,KBN114(114, "sel_pbkbn", "PB区分")
    /** 小物区分 */
    ,KBN115(115, "sel_komonokbm", "小物区分")
    /** 棚卸区分 */
    ,KBN116(116, "sel_tanaorokbn", "棚卸区分")
    /** 定計区分 */
    ,KBN117(117, "sel_teikeikbn", "定計区分")
    /** プライスカード_種類 */
    ,KBN118(118, "sel_pcard_shukbn", "種類")
    /** プライスカード_色 */
    ,KBN119(119, "sel_pcard_irokbn", "色")
    /** 税区分_商品 */
    ,KBN120(120, "sel_zeikbn", "税区分")
    /** 定貫不定貫区分 */
    ,KBN121(121, "sel_teikankbn", "定貫不定貫区分")
    /** 輸入区分 */
    ,KBN122(122, "sel_importkbn", "輸入区分")
    /** 返品区分 */
    ,KBN123(123, "sel_henpinkbn", "返品区分")
    /** フラグ情報_ELP */
    ,KBN124(124, "sel_flgjoho_elp", "フラグ情報_ELP")
    /** フラグ情報_ベルマーク */
    ,KBN125(125, "sel_flgjoho_berumark", "フラグ情報_ベルマーク")
    /** フラグ情報_リサイクル */
    ,KBN126(126, "sel_flgjoho_risaikuru", "フラグ情報_リサイクル")
    /** フラグ情報_エコマーク */
    ,KBN127(127, "sel_flgjoho_ekomark", "フラグ情報_エコマーク")
    /** 期間 */
    ,KBN128(128, "sel_kikan", "期間")
    /** 酒級 */
    ,KBN129(129, "sel_shukyukbn", "酒級")
    /** 裏貼 */
    ,KBN130(130, "sel_urabarikbn", "裏貼")
    /** プライスカ−ド出力有無 */
    ,KBN131(131, "sel_pricecardshutsuryokumu", "プライスカ−ド出力有無")
    /** 便区分 */
    ,KBN132(132, "sel_binkbn", "便区分")
    /** 発注曜日 */
    ,KBN133(133, "sel_hachuyobi", "発注曜日")
    /** 締め回数 */
    ,KBN134(134, "sel_simekaisu", "締め回数")
    /** エリア区分 */
    ,KBN135(135, "sel_ariakbn", "エリア区分")
    /** ソ−ス区分 */
    ,KBN136(136, "sel_sourcekbn", "ソ−ス区分")
    /** 添加物区分 */
    ,KBN137(137, "sel_tenkabutsukbn", "添加物区分")
    /** 添加物コード */
    ,KBN138(138, "sel_tenkabcd", "添加物コード")
    /** 扱い区分 */
    ,KBN139(139, "sel_atsukkbn", "扱い区分")
    /** グル−プ区分 */
    ,KBN140(140, "sel_gpkbn", "グル−プ区分")
    /** 規格_個数単位 */
    ,KBN141(141, "sel_kikaku_kosutani", "規格_個数単位")
    /** 衣料使い回しフラグ */
    ,KBN142(142, "sel_iryoreflg", "衣料使い回し")
    /** 桁指定 */
    ,KBN143(143, "sel_ketashitei", "桁指定")
    /** プライスカード_種類 */
    ,KBN144(144, "sel_pricecard_shurui2", "プライスカード_種類")
    /** プライスカード_色 */
    ,KBN145(145, "sel_pricecard_iro2", "プライスカード_色")
    /** アレルギーコード */
    ,KBN146(146, "sel_allergycd", "アレルギーコード")
    /** 青果センターエリア */
    ,KBN301(301, "sel_seikakibokbn", "青果センターエリア")
    /** 鮮魚区分 */
    ,KBN302(302, "sel_sengyokbn", "鮮魚区分")
    /** 精肉区分 */
    ,KBN303(303, "sel_seinikukbn", "精肉区分")
    /** S/C送信フラグ */
    ,KBN304(304, "sel_sc_sflg", "S/C送信フラグ")
    /** 構成マスタ送信フラグ */
    ,KBN305(305, "sel_kosei_sflg", "構成マスタ送信フラグ")
    /** 分類マスタ送信フラグ */
    ,KBN306(306, "sel_bunrui_sflg", "分類マスタ送信フラグ")
    /** 部門マスタ送信フラグ */
    ,KBN307(307, "sel_bmn_sflg", "部門マスタ送信フラグ")
    /** 予備1送信フラグ */
    ,KBN308(308, "sel_yobi1_sflg", "予備1送信フラグ")
    /** 予備2送信フラグ */
    ,KBN309(309, "sel_yobi2_sflg", "予備2送信フラグ")
    /** 予算区分 */
    ,KBN310(310, "sel_yosankbn", "予算区分")
    /** エレベータ */
    ,KBN311(311, "sel_elevtrflg", "エレベータ")
    /** エスカレータ */
    ,KBN312(312, "sel_escaltrflg", "エスカレータ")
    /** 店運用区分 */
    ,KBN313(313, "sel_miseunyokbn", "店運用区分")
    /** 青果市場コード */
    ,KBN314(314, "sel_seikacd", "青果市場コード")
    /** 曜日区分 */
    ,KBN3151(315, "sel_yobikbn1", "曜日区分")
    /** 店休フラグ */
    ,KBN316(316, "sel_tenkyuflg", "店休フラグ")
    /** ＭＩＯ区分 */
    ,KBN317(317, "sel_miokbn", "ＭＩＯ区分")
    /** 割引区分 */
    ,KBN318(318, "sel_waribikikbn", "割引区分")
    /** 自社テナント */
    ,KBN319(319, "sel_jishatenant", "自社テナント")
    /** ロス分析対象 */
    ,KBN320(320, "sel_losbunsekitaisho", "ロス分析対象")
    /** 予算区分_部門 */
    ,KBN321(321, "sel_yosankbn_bumon", "予算区分_部門")
    /** 棚卸対象区分 */
    ,KBN322(322, "sel_tanaoroshitaishokbn", "棚卸対象区分")
    /** 売上フラグ */
    ,KBN323(323, "sel_uriageflg", "売上フラグ")
    /** 販売部 */
    ,KBN324(324, "sel_hanbaibucd", "販売部")
    /** 地区 */
    ,KBN325(325, "sel_chikucd", "地区")
    /** デポ区分 */
    ,KBN330(330, "sel_depokbn", "デポ区分")
    /** ingfanカード */
    ,KBN331(331, "sel_ingfan", "ingfanカード")
    /** ピュアウォーター */
    ,KBN332(332, "sel_water", "ピュアウォーター")
    /** ATM */
    ,KBN333(333, "sel_atm", "ATM")
    /** レジ */
    ,KBN334(334, "sel_rezi", "お客様お会計レジ")
    /** ドライアイス */
    ,KBN335(335, "sel_dryice", "ドライアイス")
    /** 証明写真 */
    ,KBN336(336, "sel_photo", "証明写真")
    /** DPE */
    ,KBN337(337, "sel_dpe", "DPE")
    /** お届けサービス */
    ,KBN338(338, "sel_otodokeservice", "お届けサービス")
    /** 電子マネー */
    ,KBN339(339, "sel_densimoney", "電子マネー")
    /** ペット減容器 */
    ,KBN340(340, "sel_petgenyouki", "ペット減容器")
    /** くつろぎスペース */
    ,KBN341(341, "sel_kuturogispace", "くつろぎスペース")
    /** ＥＤＩ受信 */
    ,KBN401(401, "sel_edijushin", "ＥＤＩ受信")
    /** ＥＤＩ送信 */
    ,KBN402(402, "sel_edisoshin", "ＥＤＩ送信")
    /** 仕入先用途 */
    ,KBN403(403, "sel_shiiresakiyoto", "仕入先用途")
    /** いなげや在庫 */
    ,KBN404(404, "sel_inageyazaiko", "いなげや在庫")
    /** 買掛区分 */
    ,KBN405(405, "sel_kaikakekbn", "買掛区分")
    /** 計算センター */
    ,KBN406(406, "sel_keisancenter", "計算センター")
    /** 運用区分 */
    ,KBN407(407, "sel_unyokbn", "運用区分")
    /** 伝票区分 */
    ,KBN408(408, "sel_denpyokbn", "伝票区分")
    /** 集計表 */
    ,KBN409(409, "sel_shukeihyo1", "集計表")
    /** ピッキングデータ */
    ,KBN410(410, "sel_pickingdata", "ピッキングデータ")
    /** ピッキングリスト */
    ,KBN411(411, "sel_pickinglist", "ピッキングリスト")
    /** ワッペン */
    ,KBN412(412, "sel_wappen", "ワッペン")
    /** 一括伝票 */
    ,KBN413(413, "sel_ikkatsudenpyo", "一括伝票")
    /** 加工指示 */
    ,KBN414(414, "sel_kakoshiji", "加工指示")
    /** 流通区分 */
    ,KBN415(415, "sel_ryutsukbn", "流通区分")
    /** 在庫内訳_伝票区分 */
    ,KBN416(416, "sel_zaikochiwake_denpyokbn", "在庫内訳_伝票区分")
    /** 在庫内訳_集計表 */
    ,KBN417(417, "sel_zaikochiwake_shukeihyo", "在庫内訳_集計表")
    /** 集計表 */
    ,KBN4172(417, "sel_shukeihyo2", "集計表")
    /** 在庫内訳_ピッキングデータ */
    ,KBN418(418, "sel_zaikochiwake_pickingdata", "在庫内訳_ピッキングデータ")
    /** 在庫内訳_ピッキングリスト */
    ,KBN419(419, "sel_zaikochiwake_pickinglist", "在庫内訳_ピッキングリスト")
    /** 同報配信先_伝票区分 */
    ,KBN420(420, "sel_dohohaishinsaki_denpyokbn", "同報配信先_伝票区分")
    /** 同報配信先_集計表 */
    ,KBN421(421, "sel_dohohaishinsaki_shukeihyo", "同報配信先_集計表")
    /** 横持先_検収区分 */
    ,KBN422(422, "sel_yokomochisaki_kenshukbn", "横持先_検収区分")
    /** 横持先_伝票区分 */
    ,KBN423(423, "sel_yokomochisaki_denpyokbn", "横持先_伝票区分")
    /** 横持先_集計表 */
    ,KBN424(424, "sel_yokomochisaki_shukeihyo", "横持先_集計表")
    /** 店別伝票フラグ */
    ,KBN425(425, "sel_tembetsudenpyoflg", "店別伝票フラグ")
    /** 取引停止フラグ */
    ,KBN426(426, "sel_torihikiteishiflg", "取引停止フラグ")
    /** デフォルト_一括区分 */
    ,KBN427(427, "sel_def_ikkatsukbn", "デフォルト_一括区分")
    /** BMS対象区分 */
    ,KBN428(428, "sel_bmskbn", "BMS対象区分")
    /** 自動検収区分 */
    ,KBN429(429, "sel_autokbn", "自動検収区分")
    /** 生鮮・加工食品区分 */
    ,KBN430(430, "sel_kakokbn2", "生鮮・加工食品区分")
    /** 部門区分 */
    ,KBN501(501, "sel_bumonkbn", "部門区分")
    /** 税区分_部門 */
    ,KBN502(502, "sel_zeikbn_bumon", "税区分_部門")
    /** オーダーブック出力区分 */
    ,KBN503(503, "sel_orderbookshutsuryokukbn", "オーダーブック出力区分")
    /** 評価方法区分 */
    ,KBN504(504, "sel_hyokakbn", "評価方法区分")
    /** 棚卸タイミング */
    ,KBN505(505, "sel_tanaoroshitiming", "棚卸タイミング")
    /** ＰＯＳ売変対象区分 */
    ,KBN506(506, "sel_posbaihentaishokbn", "ＰＯＳ売変対象区分")
    /** 経費対象区分 */
    ,KBN507(507, "sel_keihitaishokbn", "経費対象区分")
    /** 単品管理区分 */
    ,KBN508(508, "sel_tanpinkanrikbn", "単品管理区分")
    /** 値引除外フラグ */
    ,KBN509(509, "sel_nebikijogaiflg", "値引除外フラグ")
    /** 商品 */
    ,KBN510(510, "sel_shohin", "商品")
    /** 販売制限フラグ */
    ,KBN511(511, "sel_hanbaiseigenflg", "販売制限フラグ")
    /** 加工日印字 */
    ,KBN601(601, "sel_kakobiinji", "加工日印字")
    /** 加工時印字 */
    ,KBN602(602, "sel_kakojiinji", "加工時印字")
    /** 加工時選択 */
    ,KBN603(603, "sel_kakoji_sentaku", "加工時選択")
    /** 消費日印字 */
    ,KBN604(604, "sel_shohibiinji", "消費日印字")
    /** 消費時印字 */
    ,KBN605(605, "sel_shohijiinji", "消費時印字")
    /** 表貼選択 */
    ,KBN606(606, "sel_omotehari_sentaku", "表貼選択")
    /** 裏貼発行選択 */
    ,KBN607(607, "sel_uraharihakko_sentaku", "裏貼発行選択")
    /** 裏貼選択 */
    ,KBN608(608, "sel_urahari_sentaku", "裏貼選択")
    /** アイキャッチラベル発行 */
    ,KBN609(609, "sel_eyecatchlabelhakko", "アイキャッチラベル発行")
    /** 副ラベル発行 */
    ,KBN610(610, "sel_fukulabelhakko", "副ラベル発行")
    /** 表バー印字 */
    ,KBN611(611, "sel_hyobar_inji", "表バー印字")
    /** 発行モード */
    ,KBN612(612, "sel_hakkomode", "発行モード")
    /** 自動検知 */
    ,KBN613(613, "sel_jidokenchi", "自動検知")
    /** 実績収集 */
    ,KBN614(614, "sel_jissekishushu", "実績収集")
    /** 包装速度 */
    ,KBN615(615, "sel_hososokudo", "包装速度")
    /** コード体系 */
    ,KBN616(616, "sel_cdtaikei", "コード体系")
    /** 風袋000 */
    ,KBN617(617, "sel_futai", "風袋")
    /** プライスカード発行サイズ */
    ,KBN701(701, "sel_pricecardhakkosize", "プライスカード発行サイズ")
    /** 枚数指定方法 */
    ,KBN702(702, "sel_maisushiteihoho", "枚数指定方法")
    /** 作成売価区分 */
    ,KBN703(703, "sel_sakuseibaikakbn", "作成売価区分")
    /** 権限区分_マスタ */
    ,KBN801(801, "sel_kengenkbn_mst", "権限区分_マスタ")
    /** 権限区分_発注特売 */
    ,KBN802(802, "sel_kengenkbn_hatchutokubai", "権限区分_発注特売")
    /** 管理者フラグ */
    ,KBN803(803, "sel_kanrishaflg", "管理者フラグ")
    /** 特別週フラグ */
    ,KBN10001(10001, "sel_tshuflg", "特別週フラグ")
    /** 催し区分 */
    ,KBN10002(10002, "sel_moyskbn", "催し区分")
    /** 年末区分 */
    ,KBN10003(10003, "sel_nenmatkbn", "年末区分")
    /** 1遅れスライド_販売 */
    ,KBN10004(10004, "sel_okureslide_hanbai", "1遅れスライド_販売")
    /** 1遅れスライド_納入 */
    ,KBN10005(10005, "sel_okureslide_nonyu", "1遅れスライド_納入")
    /** 検証の括り */
    ,KBN10006(10006, "sel_kenshokukuri", "検証の括り")
    /** デフォルト_数展開 */
    ,KBN10007(10007, "sel_def_sutenkai", "デフォルト_数展開")
    /** デフォルト_実績率パタン数値 */
    ,KBN10008(10008, "sel_def_jissekiritsupatansuchi", "デフォルト_実績率パタン数値")
    /** デフォルト_前年同週 */
    ,KBN10009(10009, "sel_def_zennendoshu", "デフォルト_前年同週")
    /** デフォルト_同年同週 */
    ,KBN10010(10010, "sel_def_donendoshu", "デフォルト_同年同週")
    /** デフォルト_カット店展開 */
    ,KBN10011(10011, "sel_def_cuttentenkai", "デフォルト_カット店展開")
    /** B/Mフラグ */
    ,KBN10012(10012, "sel_bmflg", "B/Mフラグ")
    /** デフォルト_部門属性 */
    ,KBN10013(10013, "sel_def_bumonzokusei", "デフォルト_部門属性")
    /** 週月フラグ */
    ,KBN10101(10101, "sel_shutsukiflg", "週月フラグ")
    /** 対象除外フラグ */
    ,KBN102011(10201, "sel_taishojogaiflg1", "対象除外フラグ")
    /** B/Mタイプ */
    ,KBN10202(10202, "sel_bm_typ", "B/Mタイプ")
    /** 割引率区分 */
    ,KBN103021(10302, "sel_waribikiritsukbn1", "割引率区分")
    /** 正規・カット区分 */
    ,KBN10303(10303, "sel_seiki_cutkbn", "正規・カット区分")
    /** 対象除外フラグ */
    ,KBN103042(10304, "sel_taishojogaiflg2", "対象除外フラグ")
    /** 商品区分 */
    ,KBN103051(10305, "sel_shohinkbn1", "商品区分")
    /** 訂正区分 */
    ,KBN103061(10306, "sel_teiseikbn1", "訂正区分")
    /** 事前区分 */
    ,KBN103071(10307, "sel_jizenkbn1", "事前区分")
    /** ワッペン区分 */
    ,KBN103081(10308, "sel_wappenkbn1", "ワッペン区分")
    /** 展開方法 */
    ,KBN103091(10309, "sel_tenkaihoho1", "展開方法")
    /** 商品区分 */
    ,KBN103102(10310, "sel_shohinkbn2", "商品区分")
    /** 訂正区分 */
    ,KBN103112(10311, "sel_teiseikbn2", "訂正区分")
    /** 事前区分 */
    ,KBN103122(10312, "sel_jizenkbn2", "事前区分")
    /** ワッペン区分 */
    ,KBN103132(10313, "sel_wappenkbn2", "ワッペン区分")
    /** 展開方法 */
    ,KBN103142(10314, "sel_tenkaihoho2", "展開方法")
    /** 実施方法 */
    ,KBN10401(10401, "sel_jisshihoho", "実施方法")
    /** サイズ */
    ,KBN10402(10402, "sel_size", "サイズ")
    /** 用紙向き */
    ,KBN10403(10403, "sel_yoshimuki", "用紙向き")
    /** 地図有無 */
    ,KBN10404(10404, "sel_chizuumu", "地図有無")
    /** 募集_FP */
    ,KBN10405(10405, "sel_boshu_fp", "募集_FP")
    /** 承認区分 */
    ,KBN104061(10406, "sel_shoninkbn1", "承認区分")
    /** リーダー店フラグ */
    ,KBN10407(10407, "sel_leadertenflg", "リーダー店フラグ")
    /** よりどりフラグ */
    ,KBN104081(10408, "sel_yoridoriflg1", "よりどりフラグ")
    /** 発注方法 */
    ,KBN10409(10409, "sel_hatchuhoho", "発注方法")
    /** 割引率区分 */
    ,KBN104102(10410, "sel_waribikiritsukbn2", "割引率区分")
    /** 生食加熱区分 */
    ,KBN104111(10411, "sel_seishokukanetsukbn1", "生食加熱区分")
    /** 解凍フラグ */
    ,KBN104121(10412, "sel_kaitoflg1", "解凍フラグ")
    /** 養殖フラグ */
    ,KBN104131(10413, "sel_yoshokuflg1", "養殖フラグ")
    /** 定貫PLU・不定貫区分 */
    ,KBN104141(10414, "sel_teikanplu_futeikankbn1", "定貫PLU・不定貫区分")
    /** 実施方法_インストア */
    ,KBN10416(10416, "sel_jisshihoho_instore", "実施方法_インストア")
    /** 実施方法_センターパック */
    ,KBN10417(10417, "sel_jisshihoho_centerpack", "実施方法_センターパック")
    /** 承認区分 */
    ,KBN104182(10418, "sel_shoninkbn2", "承認区分")
    /** 募集_NP */
    ,KBN10419(10419, "sel_boshu_np", "募集_NP")
    /** 募集_YP */
    ,KBN10420(10420, "sel_boshu_yp", "募集_YP")
    /** 訂正区分 */
    ,KBN104213(10421, "sel_teiseikbn3", "訂正区分")
    /** 訂正区分 */
    ,KBN105014(10501, "sel_teiseikbn4", "訂正区分")
    /** 商品区分 */
    ,KBN10502(10502, "sel_shohinkbn", "商品区分")
    /** 曜日区分 */
    ,KBN105032(10503, "sel_yobikbn2", "曜日区分")
    /** 販売日1日遅許可フラグ */
    ,KBN10601(10601, "sel_hanbaibi1nichichikyokaflg", "販売日1日遅許可フラグ")
    /** 月締フラグ */
    ,KBN10602(10602, "sel_tsukijimeflg", "月締フラグ")
    /** 本部コントロールフラグ */
    ,KBN10603(10603, "sel_honbucontrolflg", "本部コントロールフラグ")
    /** 店不採用禁止フラグ */
    ,KBN10604(10604, "sel_tenfusaiyokinshiflg", "店不採用禁止フラグ")
    /** 店売価選択禁止フラグ */
    ,KBN10605(10605, "sel_tenbaika_sentakukinshiflg", "店売価選択禁止フラグ")
    /** 店商品選択禁止フラグ */
    ,KBN10606(10606, "sel_tenshohin_sentakukinshiflg", "店商品選択禁止フラグ")
    /** 仮締フラグ_リーダー店 */
    ,KBN10607(10607, "sel_karishimeflg_leaderten", "仮締フラグ_リーダー店")
    /** 本締フラグ_リーダー店 */
    ,KBN10608(10608, "sel_honshimeflg_leaderten", "本締フラグ_リーダー店")
    /** 本締フラグ_各店 */
    ,KBN10609(10609, "sel_honshimeflg_kakuten", "本締フラグ_各店")
    /** 強制グループフラグ */
    ,KBN10610(10610, "sel_kyoseiflg", "強制グループフラグ")
    /** アンケート種類 */
    ,KBN10611(10611, "sel_qasyukbn", "アンケート種類")
    /** リーダー店区分 */
    ,KBN10612(10612, "sel_leadertenkbn", "リーダー店区分")
    /** リーダー店採用フラグ */
    ,KBN10613(10613, "sel_leadertensaiyoflg", "リーダー店採用フラグ")
    /** 売価一括選択 */
    ,KBN106141(10614, "sel_baikaikkatsu_sentaku1", "売価一括選択")
    /** 売価一括選択 */
    ,KBN106152(10615, "sel_baikaikkatsu_sentaku2", "売価一括選択")
    /** スケジュール回答フラグ */
    ,KBN10616(10616, "sel_schedule_kaitoflg", "スケジュール回答フラグ")
    /** アイテム回答フラグ */
    ,KBN10617(10617, "sel_item_kaitoflg", "アイテム回答フラグ")
    /** 各店採用フラグ */
    ,KBN10618(10618, "sel_kakutensaiyoflg", "各店採用フラグ")
    /** 販売開始フラグ */
    ,KBN10619(10619, "sel_hanbaikaishiflg", "販売開始フラグ")
    /** 各店回答フラグ */
    ,KBN10620(10620, "sel_kakuten_kaitoflg", "各店回答フラグ")
    /** 売価選択 */
    ,KBN10621(10621, "sel_baika_sentaku", "売価選択")
    /** 商品選択 */
    ,KBN10622(10622, "sel_shohin_sentaku", "商品選択")
    /** 1日遅スライド_販売 */
    ,KBN10651(10651, "sel_nichiokureslide_hanbai", "1日遅スライド_販売")
    /** 1日遅スライド_納入 */
    ,KBN10652(10652, "sel_nichiokureslide_nonyu", "1日遅スライド_納入")
    /** 日替フラグ */
    ,KBN10653(10653, "sel_hitaiflg", "日替フラグ")
    /** チラシ未掲載 */
    ,KBN10654(10654, "sel_chirashimikeisai", "チラシ未掲載")
    /** 発注原売価適用フラグ */
    ,KBN10655(10655, "sel_hatchuharabaikatekiyoflg", "発注原売価適用フラグ")
    /** 割引率区分 */
    ,KBN10656(10656, "sel_writukbn", "割引率区分")
    /** 定貫PLU・不定貫区分 */
    ,KBN106572(10657, "sel_teikanplu_futeikankbn2", "定貫PLU・不定貫区分")
    /** PLU配信フラグ */
    ,KBN10658(10658, "sel_pluhaishinflg", "PLU配信フラグ")
    /** よりどりフラグ */
    ,KBN106592(10659, "sel_yoridoriflg2", "よりどりフラグ")
    /** 目玉区分 */
    ,KBN10660(10660, "sel_medamakbn", "目玉区分")
    /** 生食加熱区分 */
    ,KBN106612(10661, "sel_seishokukanetsukbn2", "生食加熱区分")
    /** 解凍フラグ */
    ,KBN106622(10662, "sel_kaitoflg2", "解凍フラグ")
    /** 養殖フラグ */
    ,KBN106632(10663, "sel_yoshokuflg2", "養殖フラグ")
    /** カット店展開フラグ */
    ,KBN10664(10664, "sel_cuttentenkaiflg", "カット店展開フラグ")
    /** 便区分 */
    ,KBN10665(10665, "sel_binkbn", "便区分")
    /** ワッペン区分 */
    ,KBN10666(10666, "sel_wappnkbn", "ワッペン区分")
    /** 週次仕入先伝送フラグ */
    ,KBN10668(10668, "sel_shutsugishiiresakidensoflg", "週次仕入先伝送フラグ")
    /** 訂正区分 */
    ,KBN106695(10669, "sel_teiseikbn5", "訂正区分")
    /** 制限ー限定表現 */
    ,KBN10670(10670, "sel_segn_gentei", "制限ー限定表現")
    /** 制限ー単位 */
    ,KBN10671(10671, "sel_segn_1kosutni", "制限ー単位")
    /** 月締変更理由 */
    ,KBN10672(10672, "sel_tsukishimehenkoriyu", "月締変更理由")
    /** 納品形態 */
    ,KBN10803(10803, "sel_nohinkeitai", "納品形態")
    /** 訂正区分 */
    ,KBN108046(10804, "sel_teiseikbn6", "訂正区分")
    /** 催し区分 */
    ,KBN10805(10805, "sel_moyooshikbn", "催し区分")
    /** 画面ＩＴ０２４：登録更新ＣＳＶ取込件数上限 */
    ,KBN30001(30001, "sel_it024_torokukoshincsv_torikomikensujogen", "画面ＩＴ０２４：登録更新ＣＳＶ取込件数上限")
    /** 訂正区分 */
    ,KBN105017(10501, "sel_teiseikbn7", "訂正区分")
    /** 訂正区分 */
    ,KBN105018(10501, "sel_teiseikbn8", "訂正区分")
    /** 訂正区分 */
    ,KBN105019(10501, "sel_teiseikbn9", "訂正区分")
    /** 訂正区分 */
    ,KBN1050110(10501, "sel_teiseikbn10", "訂正区分")
    /** 訂正区分 */
    ,KBN1050111(10501, "sel_teiseikbn11", "訂正区分")
    /** 訂正区分 */
    ,KBN1050112(10501, "sel_teiseikbn12", "訂正区分")

    /** 追加：保温区分 */
    ,KBN151(151, "sel_k_honkb", "保温区分")
    /** 追加：デリカワッペン区分_レギュラー */
    ,KBN152(152, "sel_k_wapnflg", "デリカワッペン区分_レギュラー")
    /** 追加：取扱区分 */
    ,KBN153(153, "sel_k_torikb", "取扱区分")

    /** セルセンターコード */
    ,KBN910005(910005, "sel_center", "センター")
    /** 配送便No */
    ,KBN910006(910006, "sel_supplyno", "便")
    /** コースマスタ配送順MAX値 */
    ,KBN910007(910007, "sel_storeseq", "配送順序")
    /** コースマスタコースMAX値 */
    ,KBN910008(910008, "sel_courseno", "コース番号")
    /** 状態 */
    ,KBN910009(910009, "sel_errordiv", "状態");

    private final Integer cd;
    private final String obj;
    private final String txt;

    /** 初期化 */
    private MeisyoSelect(Integer cd, String obj, String txt) {
      this.cd = cd;
      this.obj = obj;
      this.txt = txt;
    }

    /** @return cd 名称コード区分 */
    public Integer getCd() {
      return cd;
    }

    /** @return obj Object名 */
    @Override
    public String getObj() {
      return obj;
    }

    /** @return txt 表示名称 */
    @Override
    public String getTxt() {
      return txt;
    }
  }

  /** データ型 */
  public enum DataType {
    /** 文字(特に制限無し) */
    TEXT("text"),
    /** 全角文字 */
    ZEN("zen_text"),
    /** 大文字の半角英文字 */
    ALPHAL("AlphaL_text"),
    /** 半角カナ字 */
    KANA("kana_text"),
    /** 半角英数字 */
    ALPHA("alpha_text"),
    /** 半角数字 */
    SUUJI("suuji_text"),
    /** 半角数字、半角スペース */
    SUUJISPACE("suujispace_text"),
    /** 半角数字、半角ハイフン */
    SUUJIHAIHUN("suujihaihun_text"),
    /** 左ゼロ埋め半角数字 */
    LPADZERO("lpadzero_text"),
    /** 電話番号 */
    TEL("tel"),
    /** FAX */
    FAX("fax"),
    /** 整数 */
    INTEGER("integer"),
    /** 小数 */
    DECIMAL("decimal"),
    /** 日付 */
    DATE("date"),
    /** 年月 */
    YYMM("yymm"),
    /** 月日 */
    MMDD("mmdd"),
    /** 時分 */
    HHMM("hhmm");

    private final String obj;

    /** 初期化 */
    private DataType(String obj) {
      this.obj = obj;
    }

    /** @return obj Object名 */
    public String getObj() {
      return obj;
    }

    /**
     * 指定されたデータ型がテキストかどうかを戻します。
     *
     * @param dataType データ型
     * @return true：テキスト false：その他
     */
    public boolean isText() {
      return TEXT.equals(getObj()) || ZEN.equals(getObj()) || KANA.equals(getObj()) || ALPHA.equals(getObj()) || SUUJI.equals(getObj()) || LPADZERO.equals(getObj()) || TEL.equals(getObj())
          || FAX.equals(getObj());
    }

    /** @return データ型タグ */
    public String getTypTag() {
      return "\"datatyp\":\"" + getObj() + "\"";
    }
  }

  /** HTML関連 テキスト(入力用) */
  public enum InpText implements Item {
    /** ----- 汎用 ----- **/
    /** 年月From */
    STYM("txt_stym", "開始年月", DataType.YYMM, 4, 0),
    /** 年月To */
    ENYM("txt_enym", "終了年月", DataType.YYMM, 4, 0),

    /** ----- マスタ ----- **/
    /** 商品コード */
    SHNCD("txt_shncd", "商品コード", DataType.SUUJI, 8, 0),
    /** 商品名（カナ） */
    SHNAN("txt_shnan", "商品名（カナ）", DataType.KANA, 20, 0),
    /** 商品名（漢字） */
    SHNKN("txt_shnkn", "商品名（漢字）", DataType.ZEN, 40, 0),
    /** ソースコード */
    SRCCD("txt_srccd", "ソースコード", DataType.SUUJI, 14, 0),
    /** 標準仕入先コード */
    SSIRCD("txt_ssircd", "仕入先コード", DataType.LPADZERO, 6, 0),
    /** 仕入先コード */
    SIRCD("txt_sircd", "仕入先コード", DataType.LPADZERO, 6, 0),
    /** 標準仕入先名（漢字） */
    SIRKN("txt_sirkn", "標準仕入先名（漢字）", DataType.TEXT, 40, 0),
    /** 標準仕入先名（カナ） */
    SIRAN("txt_siran", "標準仕入先名（カナ）", DataType.KANA, 20, 0),
    /** メーカーコード */
    MAKERCD("txt_makercd", "メーカーコード", DataType.SUUJI, 9, 0),
    /** メーカー（漢字） */
    MAKERKN("txt_makerkn", "メーカー（漢字）", DataType.ZEN, 40, 0),
    /** メーカー（カナ） */
    MAKERAN("txt_makeran", "メーカー（カナ）", DataType.KANA, 20, 0),
    /** JANコード */
    JANCD("txt_jancd", "JANコード", DataType.SUUJI, 14, 0),
    /** 更新日from */
    UPDDTF("txt_upddtf", "更新日", DataType.DATE, 10, 0),
    /** 更新日to */
    UPDDTT("txt_upddtt", "～", DataType.DATE, 10, 0),
    /** CSV出力用商品コード */
    CSVSHNCD("txt_csvshncd", "CSV出力用商品コード", DataType.SUUJI, 8, 0),
    /** 店コード */
    TENCD("txt_tencd", "店コード", DataType.LPADZERO, 3, 0),
    /** 店休日 */
    TENKYUDT("txt_tenkyudt", "店休日", DataType.DATE, 8, 0),
    /** 店休日from */
    TENKYU_STDT("txt_tenkyustdt", "店休日", DataType.DATE, 8, 0),
    /** 店休日to */
    TENKYU_ENDT("txt_tenkyuendt", "店休日", DataType.DATE, 8, 0),
    /** 店舗名称（漢字） */
    TENKN("txt_tenkn", "店舗名称（漢字）", DataType.ZEN, 40, 0),
    /** 標準分類:部門コード */
    BMNCD("txt_bmncd", "部門コード", DataType.LPADZERO, 2, 0),
    /** 標準分類:部門コード(０埋め) */
    BMNCD2("txt_bmncd", "部門コード", DataType.LPADZERO, 2, 0),
    /** 標準分類:部門名称（カナ） */
    BMNAN("txt_bmnan", "部門名称（カナ）", DataType.KANA, 20, 0),
    /** 標準分類:部門名称（漢字） */
    BMKAN("txt_bmkan", "部門名称（漢字）", DataType.ZEN, 40, 0),
    /** 標準分類:全社部門コード */
    CORPBMNCD("txt_corpbmncd", "全社部門コード", DataType.LPADZERO, 3, 0),
    /** 標準分類:売上計上部門コード */
    URIBMNCD("txt_uribmncd", "売上計上部門コード", DataType.LPADZERO, 3, 0),
    /** 標準分類:包含部門コード */
    HOGANBMNCD("txt_hoganbmncd", "包含部門コード", DataType.LPADZERO, 3, 0),
    /** 標準分類:大分類コード */
    DAICD("txt_daicd", "大分類コード", DataType.LPADZERO, 2, 0),
    /** 標準分類:大分類名（カナ） */
    DAIBRUIAN("txt_daibruian", "大分類名（カナ）", DataType.KANA, 20, 0),
    /** 標準分類:大分類名（漢字） */
    DAIBRUIKN("txt_daibruikn", "大分類名（漢字）", DataType.ZEN, 30, 0),
    /** 標準分類:中分類コード */
    CHUCD("txt_chucd", "中分類コード", DataType.LPADZERO, 2, 0),
    /** 標準分類:中分類名（カナ） */
    CHUBRUIAN("txt_chubruian", "中分類名（カナ）", DataType.KANA, 20, 0),
    /** 標準分類:中分類名（漢字） */
    CHUBRUIKN("txt_chubruikn", "中分類名（漢字）", DataType.ZEN, 30, 0),
    /** 標準分類:小分類コード */
    SHOCD("txt_shocd", "小分類コード", DataType.LPADZERO, 2, 0),
    /** 標準分類:小分類名（カナ） */
    SHOBRUIAN("txt_shobruian", "小分類名（カナ）", DataType.KANA, 20, 0),
    /** 標準分類:小分類名（漢字） */
    SHOBRUIKN("txt_shobruikn", "小分類名（漢字）", DataType.ZEN, 30, 0),
    /** 標準分類:小小分類コード */
    SSHOCD("txt_sshocd", "小小分類コード", DataType.LPADZERO, 1, 0),
    /** 標準分類:小小分類コード */
    SSHOBRUIAN("txt_sshobruian", "小小分類名（カナ）", DataType.KANA, 20, 0),
    /** 標準分類:小小分類コード */
    SSHOBRUIKN("txt_sshobruikn", "小小分類名（漢字）", DataType.TEXT, 30, 0),
    /** 用途分類:部門コード */
    YOT_BMNCD("txt_yot_bmncd", "部門コード", DataType.LPADZERO, 2, 0),
    /** 用途分類:大分類コード */
    YOT_DAICD("txt_yot_daicd", "大分類コード", DataType.LPADZERO, 2, 0),
    /** 用途分類:中分類コード */
    YOT_CHUCD("txt_yot_chucd", "中分類コード", DataType.LPADZERO, 2, 0),
    /** 用途分類:小分類コード */
    YOT_SHOCD("txt_yot_shocd", "小分類コード", DataType.LPADZERO, 2, 0),
    /** 用途分類:小小分類コード */
    YOT_SSHOCD("txt_yot_sshocd", "小小分類コード", DataType.LPADZERO, 1, 0),
    /** 売場分類:部門コード */
    URI_BMNCD("txt_uri_bmncd", "部門コード", DataType.LPADZERO, 2, 0),
    /** 売場分類:大分類コード */
    URI_DAICD("txt_uri_daicd", "大分類コード", DataType.LPADZERO, 2, 0),
    /** 売場分類:中分類コード */
    URI_CHUCD("txt_uri_chucd", "中分類コード", DataType.LPADZERO, 2, 0),
    /** 売場分類:小分類コード */
    URI_SHOCD("txt_uri_shocd", "小分類コード", DataType.LPADZERO, 2, 0),
    /** 売場分類:小小分類コード */
    URI_SSHOCD("txt_uri_sshocd", "小小分類コード", DataType.LPADZERO, 1, 0),
    /** 値付分類:部門コード */
    NEZ_BMNCD("txt_nez_bmncd", "部門コード", DataType.LPADZERO, 2, 0),
    /** 値付分類:大分類コード */
    NEZ_DAICD("txt_nez_daicd", "大分類コード", DataType.LPADZERO, 2, 0),
    /** 値付分類:中分類コード */
    NEZ_CHUCD("txt_nez_chucd", "中分類コード", DataType.LPADZERO, 2, 0),
    /** 値付分類:小分類コード */
    NEZ_SHOCD("txt_nez_shocd", "小分類コード", DataType.LPADZERO, 2, 0),
    /** 属性（部門分類マスタ用） */
    ATR("txt_atr", "属性", DataType.TEXT, 1, 0),
    /** レシートカナ名 */
    RECEIPTAN("txt_receiptan", "カナ名", DataType.KANA, 20, 0),
    /** レシート漢字名 */
    RECEIPTKN("txt_receiptkn", "漢字名", DataType.ZEN, 40, 0),
    /** プライスカード商品名称 */
    PCARDKN("txt_pcardkn", "商品名称", DataType.ZEN, 40, 0),
    /** メーカー・産地 */
    SANCHIKN("txt_sanchikn", "メーカー・産地", DataType.TEXT, 40, 0),
    /** POP名称 */
    POPKN("txt_popkn", "POP名称", DataType.ZEN, 40, 0),
    /** 販売コード */
    URICD("txt_uricd", "販売コード", DataType.SUUJI, 6, 0),
    /** 商品コメント・セールスコピー */
    SALESCOMKN("txt_salescomkn", "商品コメント・<br>セールスコピー", DataType.ZEN, 60, 0),
    /** 親商品コード */
    PARENTCD("txt_parentcd", "親商品コード", DataType.SUUJI, 8, 0),
    /** 規格 */
    KIKKN("txt_kikkn", "規格", DataType.ZEN, 46, 0),
    /** 規格(カナ) */
    KIKAN("txt_kikan", "規格(カナ)", DataType.TEXT, 46, 0),
    /** 容量 */
    UP_YORYOSU("txt_up_yoryosu", "容量", DataType.INTEGER, 5, 0),
    /** 単位容量 */
    UP_TYORYOSU("txt_up_tyoryosu", "単位容量", DataType.INTEGER, 3, 0),
    /** 呼出コード */
    YOBIDASHICD("txt_yobidashicd", "呼出コード", DataType.TEXT, 8, 0),
    /** 店グループ */
    TENGPCD("txt_tengpcd", "店グループ", DataType.LPADZERO, 4, 0),
    /** 店グループ(特売) */
    TENGPCD_TOK("txt_tengpcd", "店グループ", DataType.LPADZERO, 3, 0),
    /** 店舗グループ名（漢字） */
    TENGPKN("txt_tengpkn", "店グループ名称", DataType.ZEN, 40, 0),
    /** 店舗グループ名（カナ） */
    TENGPAN("txt_tengpan", "店舗グループ名（カナ）", DataType.KANA, 20, 0),
    /** 取扱期間From */
    ATSUK_STDT("txt_atsuk_stdt", "取扱期間", DataType.DATE, 8, 0),
    /** 取扱期間To */
    ATSUK_EDDT("txt_atsuk_eddt", "～", DataType.DATE, 8, 0),
    /** 原価 */
    GENKAAM("txt_genkaam", "原価", DataType.DECIMAL, 8, 2),
    /** 原価率 */
    GENKART("txt_genkart", "原価率", DataType.DECIMAL, 4, 2),
    /** 本体売価 */
    BAIKAAM("txt_baikaam", "本体売価", DataType.INTEGER, 6, 0),
    /** 店入数 */
    IRISU("txt_irisu", "店入数", DataType.INTEGER, 3, 0),
    /** レギュラー原価 */
    RG_GENKAAM("txt_rg_genkaam", "レギュラー原価", DataType.DECIMAL, 8, 2),
    /** レギュラー本体売価 */
    RG_BAIKAAM("txt_rg_baikaam", "レギュラー本体売価", DataType.INTEGER, 6, 0),
    /** レギュラー店入数 */
    RG_IRISU("txt_rg_irisu", "レギュラー店入数", DataType.INTEGER, 3, 0),
    /** 販促原価 */
    HS_GENKAAM("txt_hs_genkaam", "販促原価", DataType.DECIMAL, 8, 2),
    /** 販促本体売価 */
    HS_BAIKAAM("txt_hs_baikaam", "販促本体売価", DataType.INTEGER, 6, 0),
    /** 販促店入数 */
    HS_IRISU("txt_hs_irisu", "販促店入数", DataType.INTEGER, 3, 0),
    /** 上限金額 */
    JYOGENAM("txt_jyogenam", "上限金額", DataType.INTEGER, 11, 0),
    /** 上限数量 */
    JYOGENSU("txt_jyogensu", "上限数量", DataType.INTEGER, 11, 0),
    /** 税率変更日 */
    ZEIRTHENKODT("txt_zeirthenkodt", "税率変更日", DataType.DATE, 8, 0),
    /** 店別異部門商品コード */
    TENSHNCD("txt_tenshncd", "店別異部門商品コード", DataType.SUUJI, 8, 0),
    /** ITFコード */
    ITFCD("txt_itfcd", "ITFコード", DataType.SUUJI, 14, 0),
    /** センター入数 */
    CENTER_IRISU("txt_center_irisu", "センター入数", DataType.INTEGER, 3, 0),
    /** EDIあり */
    EDI_RKBN("txt_edi_rkbn", "EDIあり", DataType.INTEGER, 1, 0),
    /** EDI送信 */
    EDI_SKBN("txt_edi_skbn", "EDI送信", DataType.INTEGER, 1, 0),
    /** 配送パターンコード */
    HSPTN("txt_hsptn", "配送パターンコード", DataType.LPADZERO, 3, 0),
    /** 配送パターン名 */
    HSPTNKN("txt_hsptnkn", "配送パターン名", DataType.ZEN, 40, 0),
    /** リードタイムパターン */
    READTMPTN("txt_readtmptn", "リードタイムパターン", DataType.LPADZERO, 3, 0),
    /** リードタイムパターン(0埋め) */
    READTMPTN_2("txt_readtmptn", "リードタイムパターン", DataType.LPADZERO, 3, 0),
    /** リードタイムパターン */
    READTMPTNKN("txt_readtmptnkn", "リードタイム名称", DataType.ZEN, 40, 0),
    /** リードタイム */
    READTM("txt_readtm", "リードタイム", DataType.TEXT, 2, 0),
    /** 一括伝票フラグ */
    RG_IDENFLG("txt_rg_idenflg", "一括伝票フラグ", DataType.SUUJI, 1, 0),
    /** 一括伝票内容 */
    NMKN("txt_nmkn", "一括伝票内容", DataType.TEXT, 24, 0),
    /** スポット最低発注数 */
    HS_SPOTMINSU("txt_hs_spotminsu", "スポット最低発注数", DataType.INTEGER, 2, 0),
    /** 仕分区分 */
    SIWAKEKBN("txt_siwakekbn", "仕分区分", DataType.INTEGER, 2, 0),
    /** 包材用途 */
    HZI_YOTO("txt_hzi_yoto", "包材用途", DataType.INTEGER, 1, 0),
    /** 包材材質 */
    HZI_ZAISHITU("txt_hzi_zaishitu", "包材材質", DataType.INTEGER, 1, 0),
    /** 製造限度日数 */
    SEIZOGENNISU("txt_seizogennisu", "製造限度日数", DataType.INTEGER, 3, 0),
    /** 対象年齢 */
    TAISHONENSU("txt_taishonensu", "対象年齢", DataType.INTEGER, 2, 0),
    /** カロリー表示 */
    CALORIESU("txt_caloriesu", "カロリー表示", DataType.INTEGER, 5, 0),
    /** 度数 */
    DOSU("txt_dosu", "度数", DataType.SUUJI, 3, 0),
    /** 縦商品サイズ */
    SHNTATESZ("txt_shntatesz", "縦商品サイズ", DataType.INTEGER, 4, 0),
    /** 横商品サイズ */
    SHNYOKOSZ("txt_shnyokosz", "横商品サイズ", DataType.INTEGER, 4, 0),
    /** 奥行商品サイズ */
    SHNOKUSZ("txt_shnokusz", "奥行商品サイズ", DataType.INTEGER, 4, 0),
    /** 重量商品サイズ */
    SHNJRYOSZ("txt_shnjryosz", "重量商品サイズ", DataType.DECIMAL, 6, 1),
    /** 春賞味期限 */
    ODS_HARUSU("txt_ods_harusu", "春賞味期限", DataType.INTEGER, 4, 0),
    /** 夏賞味期限 */
    ODS_NATSUSU("txt_ods_natsusu", "夏賞味期限", DataType.INTEGER, 4, 0),
    /** 秋賞味期限 */
    ODS_AKISU("txt_ods_akisu", "秋賞味期限", DataType.INTEGER, 4, 0),
    /** 冬賞味期限 */
    ODS_FUYUSU("txt_ods_fuyusu", "冬賞味期限", DataType.INTEGER, 4, 0),
    /** 入荷期限 */
    ODS_NYUKASU("txt_ods_nyukasu", "入荷期限", DataType.INTEGER, 4, 0),
    /** 値引期限 */
    ODS_NEBIKISU("txt_ods_nebikisu", "値引期限", DataType.INTEGER, 4, 0),
    /** 陳列形式コード */
    CHINRETUCD("txt_chinretucd", "陳列形式コード", DataType.SUUJI, 1, 0),
    /** 段積み形式コード */
    DANTUMICD("txt_dantumicd", "段積み形式コード", DataType.LPADZERO, 2, 0),
    /** 重なりコード */
    KASANARICD("txt_kasanaricd", "重なりコード", DataType.SUUJI, 1, 0),
    /** 重なりサイズ */
    KASANARISZ("txt_kasanarisz", "重なりサイズ", DataType.INTEGER, 3, 0),
    /** 圧縮率 */
    ASSHUKURT("txt_asshukurt", "圧縮率", DataType.INTEGER, 2, 0),
    /** 種別コード */
    SHUBETUCD("txt_shubetucd", "種別コード", DataType.SUUJI, 2, 0),
    /** マスタ変更予定日 */
    YOYAKUDT("txt_yoyakudt", "マスタ変更予定日", DataType.DATE, 6, 0),
    /** 店売価実施日 */
    TENBAIKADT("txt_tenbaikadt", "店売価実施日", DataType.DATE, 6, 0),
    /** 削除基準日数 */
    DELKIJYUNSU("txt_delkijyunsu", "削除基準日数", DataType.INTEGER, 3, 0),
    /** 売上金額最大桁数 */
    URIKETAKBN("txt_uriketakbn", "売上金額最大桁数", DataType.INTEGER, 1, 0),
    /** 商品名 上段 */
    SHNMEIJYO("txt_shnmeijyo", "商品名　上段", DataType.ZEN, 50, 0),
    /** 商品名 下段 */
    SHNMEIGE("txt_shnmeige", "商品名　下段", DataType.ZEN, 50, 0),
    /** 値付分類コード */
    BRUICD("txt_bruicd", "値付分類コード", DataType.SUUJI, 9, 0),
    /** 添加物番号 */
    TENKABUTSUNO("txt_tenkabutsuno", "添加物番号", DataType.SUUJI, 4, 0),
    /** 保存温度番号 */
    HOZONONDONO("txt_hozonondono", "保存温度番号", DataType.SUUJI, 2, 0),
    /** 保存方法番号 */
    HOZONHOHONO("txt_hozonhohono", "保存方法番号", DataType.SUUJI, 2, 0),
    /** 産地名番号 */
    SANCHINO("txt_sanchino", "産地名番号", DataType.SUUJI, 3, 0),
    /** フリー1マスター番号 */
    FREE1MSTNO("txt_free1mstno", "フリー1マスター番号", DataType.SUUJI, 2, 0),
    /** フリー2マスター番号 */
    FREE2MSTNO("txt_free2mstno", "フリー2マスター番号", DataType.SUUJI, 2, 0),
    /** フリー3マスター番号 */
    FREE3MSTNO("txt_free3mstno", "フリー3マスター番号", DataType.SUUJI, 2, 0),
    /** フリー4マスター番号 */
    FREE4MSTNO("txt_free4mstno", "フリー4マスター番号", DataType.SUUJI, 2, 0),
    /** フリー5マスター番号 */
    FREE5MSTNO("txt_free5mstno", "フリー5マスター番号", DataType.SUUJI, 2, 0),
    /** 加工元店番号 */
    KAKOGENTENNO("txt_kakogentenno", "加工元店番号", DataType.SUUJI, 4, 0),
    /** 税金番号 */
    ZEIKINNO("txt_zeikinno", "税金番号", DataType.SUUJI, 1, 0),
    /** 表フォーマット番号 */
    OMOTEFORMNO("txt_omoteformno", "表フォーマット番号", DataType.SUUJI, 2, 0),
    /** 裏フォーマット番号 */
    URAFORMNO("txt_uraformno", "裏フォーマット番号", DataType.SUUJI, 2, 0),
    /** 上限重量 */
    JYOGENJRYO("txt_jyogenjryo", "上限重量", DataType.INTEGER, 4, 0),
    /** 下限重量 */
    KAGENJRYO("txt_kagenjryo", "下限重量", DataType.INTEGER, 4, 0),
    /** 風袋重量 */
    FUTAIJRYO("txt_futaijryo", "風袋重量", DataType.SUUJI, 3, 0),
    /** 風袋枝番 */
    FUTAIEDABAN("txt_futaiedaban", "風袋枝番", DataType.SUUJI, 4, 0),
    /** 風袋名称(漢字) */
    FTAIKN("txt_futaikn", "風袋名称(漢字)", DataType.TEXT, 40, 0),
    /** 風袋名称(漢カナ) */
    FTAIAN("txt_futaian", "風袋名称(カナ)", DataType.TEXT, 20, 0),
    /** 重量 */
    JRYO("txt_jryo", "重量", DataType.SUUJI, 4, 0),
    /** コード体系 */
    CODETAIKEI("txt_codetaikei", "コード体系", DataType.SUUJI, 10, 0),
    /** トレー番号 */
    TONERNO("txt_tonerno", "トレー番号", DataType.TEXT, 4, 0),
    /** 検索文字 */
    KENSAKU("txt_kensaku", "検索文字", DataType.TEXT, 50, 0),
    /** 名称コード区分名称 */
    MEISHOKBNKN("txt_meishokbnkn", "名称コード区分名称", DataType.TEXT, 80, 0),
    /** 名称コード区分 */
    MEISHOKBN("txt_meishokbn", "名称コード区分", DataType.TEXT, 6, 0),
    /** コメント */
    COMMENTKN("txt_commentkn", "コメント", DataType.TEXT, 40, 0),
    /** 住所_都道府県（漢字） */
    ADDRKN_T("txt_addrkn_t", "住所_都道府県（漢字）", DataType.ZEN, 10, 0),
    /** 住所_市区町村（漢字） */
    ADDRKN_S("txt_addrkn_s", "住所_市区町村（漢字）", DataType.ZEN, 20, 0),
    /** 住所_町字（漢字） */
    ADDRKN_M("txt_addrkn_m", "住所_町字（漢字）", DataType.ZEN, 10, 0),
    /** 住所_番地（漢字） */
    ADDR_B("txt_addr_b", "住所_番地（漢字）", DataType.ZEN, 40, 0),
    /** 部署名（漢字） */
    BUSHOKN("txt_bushokn", "部署名（漢字）", DataType.ZEN, 40, 0),
    /** 郵便番号_上桁 */
    YUBINNO_U("txt_yubinno_u", "郵便番号_上桁", DataType.LPADZERO, 3, 0),
    /** 郵便番号_下桁 */
    YUBINNO_S("txt_yubinno_s", "郵便番号_下桁", DataType.LPADZERO, 4, 0),
    /** 電話番号 */
    TEL("txt_tel", "電話番号", DataType.SUUJIHAIHUN, 20, 0),
    /** 内線番号 */
    NAISEN("txt_naisen", "内線番号", DataType.ALPHA, 4, 0),
    /** FAX番号 */
    FAX("txt_fax", "FAX番号", DataType.KANA, 20, 0),
    /** 代表仕入先コード */
    DSIRCD("txt_dsircd", "代表仕入先コード", DataType.LPADZERO, 6, 0),
    /** 伝送先親仕入先コード */
    DOYASIRCD("txt_doyasircd", "伝送先親仕入先コード", DataType.INTEGER, 6, 0),
    /** 開始日 */
    STARTDT("txt_startdt", "開始日", DataType.DATE, 6, 0),
    /** 納税者番号 */
    NOZEISHANO("txt_nozeishano", "納税者番号", DataType.SUUJI, 12, 0),
    /** 処理単価 */
    SYORTANKAAM("txt_syortankaam", "処理単価", DataType.INTEGER, 6, 0),
    /** 基本料金 */
    KHNRYOKINAM("txt_khnryokinam", "基本料金", DataType.INTEGER, 6, 0),
    /** 取引停止フラグ */
    STOPFLG("txt_stopflg", "取引停止フラグ", DataType.INTEGER, 1, 0),
    /** 同報配信先コード */
    DOHOCD("txt_dohocd", "同報配信先コード", DataType.TEXT, 6, 0),
    /** デフォルト_実仕入先コード */
    DF_RSIRCD("txt_df_rsircd", "デフォルト_実仕入先コード", DataType.LPADZERO, 6, 0),
    /** センターコード */
    CENTERCD("txt_centercd", "センターコード", DataType.LPADZERO, 3, 0),
    /** 横持先センターコード */
    YCENTERCD("txt_ycentercd", "横持先センターコード", DataType.LPADZERO, 3, 0),
    /** 店別伝票フラグ */
    TENDENFLG("txt_tendenflg", "店別伝票フラグ", DataType.INTEGER, 1, 0),
    /** 計算センター */
    VANKBN("txt_vankbn", "計算センター", DataType.INTEGER, 1, 0),
    /** 運用区分 */
    UNYOKBN("txt_unyokbn", "運用区分", DataType.INTEGER, 1, 0),
    /** 伝票区分 */
    DENPKBN("txt_denpkbn", "伝票区分", DataType.INTEGER, 1, 0),
    /** 集計表 */
    SHUHKBN("txt_shuhkbn", "集計表", DataType.INTEGER, 1, 0),
    /** ピッキングデータ */
    PICKDKBN("txt_pickdkbn", "ピッキングデータ", DataType.INTEGER, 1, 0),
    /** ピッキングリスト */
    PICKLKBN("txt_picklkbn", "ピッキングリスト", DataType.INTEGER, 1, 0),
    /** ワッペン */
    WAPNKBN("txt_wapnkbn", "ワッペン", DataType.INTEGER, 1, 0),
    /** 一括伝票 */
    IDENPKBN("txt_idenpkbn", "一括伝票", DataType.INTEGER, 1, 0),
    /** 加工指示 */
    KAKOSJKBN("txt_kakosjkbn", "加工指示", DataType.INTEGER, 1, 0),
    /** 流通区分 */
    RYUTSUKBN("txt_ryutsukbn", "流通区分", DataType.INTEGER, 1, 0),
    /** 在庫内訳_伝票区分 */
    ZDENPKBN("txt_zdenpkbn", "在庫内訳_伝票区分", DataType.INTEGER, 1, 0),
    /** 在庫内訳_集計表 */
    ZSHUHKBN("txt_zshuhkbn", "在庫内訳_集計表", DataType.INTEGER, 1, 0),
    /** 在庫内訳_ピッキングデータ */
    ZPICKDKBN("txt_zpickdkbn", "在庫内訳_ピッキングデータ", DataType.INTEGER, 1, 0),
    /** 在庫内訳_ピッキングリスト */
    ZPICKLKBN("txt_zpicklkbn", "在庫内訳_ピッキングリスト", DataType.INTEGER, 1, 0),
    /** 実仕入先コード */
    RSIRCD("txt_rsircd", "実仕入先コード", DataType.LPADZERO, 6, 0),
    /** 横持先_検収区分 */
    YKNSHKBN("txt_yknshkbn", "横持先_検収区分", DataType.INTEGER, 1, 0),
    /** 横持先_伝票区分 */
    YDENPKBN("txt_ydenpkbn", "横持先_伝票区分", DataType.INTEGER, 1, 0),
    /** 横持先_集計表 */
    DSHUHKBN("txt_dshuhkbn", "横持先_集計表", DataType.INTEGER, 1, 0),
    /** 配送グループ */
    HSGPCD("txt_hsgpcd", "配送グループ", DataType.LPADZERO, 4, 0),
    /** 配送店グループ */
    HSTENGPCD("txt_hstengpcd", "配送店グループ", DataType.LPADZERO, 4, 0),
    /** 配送店グループ */
    HSTENGPCD_A("txt_hstengpcd_a", "配送店グループ", DataType.LPADZERO, 2, 0),
    /** 配送店グループ */
    HSTENGPCD_T("txt_hstengpcd_t", "配送店グループ", DataType.LPADZERO, 4, 0),
    /** 入力順番 */
    SEQNO("txt_seqno", "入力順番", DataType.INTEGER, 1, 0),
    /** 配送グループ名称（漢字） */
    HSGPKN("txt_hsgpkn", "配送グループ名称（漢字）", DataType.ZEN, 40, 0),
    /** 配送グループ名称（カナ） */
    HSGPAN("txt_hsgpan", "配送グループ名称（カナ）", DataType.KANA, 20, 0),
    /** 店舗名称（カナ） */
    TENAN("txt_tenan", "店舗名称（カナ）", DataType.KANA, 20, 0),
    /** 店舗属性5 */
    TENATR5("txt_tenatr5", "店舗属性5", DataType.INTEGER, 1, 0),
    /** 店舗属性6 */
    TENATR6("txt_tenatr6", "店舗属性6", DataType.INTEGER, 1, 0),
    /** 開設日 */
    TENOPENDT("txt_tenopendt", "開設日", DataType.DATE, 8, 0),
    /** 閉鎖日 */
    TENCLOSEDT("txt_tenclosedt", "閉鎖日", DataType.DATE, 8, 0),
    /** 改装日（1） */
    KAISODT1("txt_kaisodt1", "改装日（1）", DataType.DATE, 8, 0),
    /** 改装日（2） */
    KAISODT2("txt_kaisodt2", "改装日（2）", DataType.DATE, 8, 0),
    /** 改装日（3） */
    KAISODT3("txt_kaisodt3", "改装日（3）", DataType.DATE, 8, 0),
    /** データ取込開設日 */
    DTORIKOMIDT("txt_dtorikomidt", "データ取込開設日", DataType.DATE, 8, 0),
    /** 営業時間1_開始月日 */
    EGYOTM1_STMD("txt_egyotm1_stmd", "営業時間1_開始月日", DataType.MMDD, 4, 0),
    /** 営業時間1_終了月日 */
    EGYOTM1_EDMD("txt_egyotm1_edmd", "営業時間1_終了月日", DataType.MMDD, 4, 0),
    /** 営業時間1_開始時間 */
    EGYOTM1_STHM("txt_egyotm1_sthm", "営業時間1_開始時間", DataType.HHMM, 4, 0),
    /** 営業時間1_終了時間 */
    EGYOTM1_EDHM("txt_egyotm1_edhm", "営業時間1_終了時間", DataType.HHMM, 4, 0),
    /** 営業時間2_開始月日 */
    EGYOTM2_STMD("txt_egyotm2_stmd", "営業時間2_開始月日", DataType.MMDD, 4, 0),
    /** 営業時間2_終了月日 */
    EGYOTM2_EDMD("txt_egyotm2_edmd", "営業時間2_終了月日", DataType.MMDD, 4, 0),
    /** 営業時間2_開始時間 */
    EGYOTM2_STHM("txt_egyotm2_sthm", "営業時間2_開始時間", DataType.HHMM, 4, 0),
    /** 営業時間2_終了時間 */
    EGYOTM2_EDHM("txt_egyotm2_edhm", "営業時間2_終了時間", DataType.HHMM, 4, 0),
    /** 営業時間3_開始月日 */
    EGYOTM3_STMD("txt_egyotm3_stmd", "営業時間3_開始月日", DataType.MMDD, 4, 0),
    /** 営業時間3_終了月日 */
    EGYOTM3_EDMD("txt_egyotm3_edmd", "営業時間3_終了月日", DataType.MMDD, 4, 0),
    /** 営業時間3_開始時間 */
    EGYOTM3_STHM("txt_egyotm3_sthm", "営業時間3_開始時間", DataType.HHMM, 4, 0),
    /** 営業時間3_終了時間 */
    EGYOTM3_EDHM("txt_egyotm3_edhm", "営業時間3_終了時間", DataType.HHMM, 4, 0),
    /** 取扱区分_CD */
    ATSUK_CD("txt_atsuk_cd", "取扱区分_CD", DataType.INTEGER, 2, 0),
    /** 取扱区分_クレジットカード */
    ATSUK_CREDIT("txt_atsuk_credit", "取扱区分_クレジットカード", DataType.INTEGER, 2, 0),
    /** 取扱区分_コインレスカード */
    ATSUK_COLESS("txt_atsuk_coless", "取扱区分_コインレスカード", DataType.INTEGER, 2, 0),
    /** 取扱区分_ポイントカード */
    ATSUK_POINT("txt_atsuk_point", "取扱区分_ポイントカード", DataType.INTEGER, 2, 0),
    /** 実働人員 */
    STAFFSU("txt_staffsu", "実働人員", DataType.DECIMAL, 7, 1),
    /** 住所_番地（漢字） */
    ADDRKN_B("txt_addrkn_b", "住所_番地（漢字）", DataType.ZEN, 60, 0),
    /** 住所_都道府県（カナ） */
    ADDRAN_T("txt_addran_t", "住所_都道府県（カナ）", DataType.KANA, 10, 0),
    /** 住所_市町村（カナ） */
    ADDRAN_S("txt_addran_s", "住所_市町村（カナ）", DataType.KANA, 10, 0),
    /** 住所_町字（カナ） */
    ADDRAN_M("txt_addran_m", "住所_町字（カナ）", DataType.KANA, 10, 0),
    /** 住所_番地（カナ） */
    ADDRAN_B("txt_addran_b", "住所_番地（カナ）", DataType.KANA, 20, 0),
    /** エリア */
    URIAERACD("txt_uriaeracd", "エリア", DataType.INTEGER, 2, 0),
    /** 地域 */
    CHIIKICD("txt_chiikicd", "地域", DataType.INTEGER, 1, 0),
    /** 最寄り駅 */
    MOYORIEKIKN("txt_moyoriekikn", "最寄り駅", DataType.ZEN, 30, 0),
    /** バス停 */
    BUSSTOPKN("txt_busstopkn", "バス停", DataType.ZEN, 30, 0),
    /** モデル店 */
    MODELTEN("txt_modelten", "モデル店", DataType.INTEGER, 3, 0),
    /** 電話番号1 */
    TEL1("txt_tel1", "電話番号1", DataType.SUUJIHAIHUN, 14, 0),
    /** 電話番号2 */
    TEL2("txt_tel2", "電話番号2", DataType.SUUJIHAIHUN, 14, 0),
    /** 電話番号3 */
    TEL3("txt_tel3", "電話番号3", DataType.SUUJIHAIHUN, 14, 0),
    /** 電話番号4 */
    TEL4("txt_tel4", "電話番号4", DataType.SUUJIHAIHUN, 14, 0),
    /** 電話番号5 */
    TEL5("txt_tel5", "電話番号5", DataType.SUUJIHAIHUN, 14, 0),
    /** FAX番号1 */
    FAX1("txt_fax1", "FAX番号1", DataType.SUUJIHAIHUN, 14, 0),
    /** FAX番号2 */
    FAX2("txt_fax2", "FAX番号2", DataType.SUUJIHAIHUN, 14, 0),
    /** 競合店1 */
    TEN1("txt_ten1", "競合店1", DataType.ZEN, 40, 0),
    /** 競合店2 */
    TEN2("txt_ten2", "競合店2", DataType.ZEN, 40, 0),
    /** 競合店3 */
    TEN3("txt_ten3", "競合店3", DataType.ZEN, 40, 0),
    /** 競合店4 */
    TEN4("txt_ten4", "競合店4", DataType.ZEN, 40, 0),
    /** 競合店5 */
    TEN5("txt_ten5", "競合店5", DataType.ZEN, 40, 0),
    /** テナント１ 種別 */
    TENANTO1_SYUBETU("txt_tenant1_syubetu", "テナント1　種別", DataType.TEXT, 20, 0),
    /** テナント２ 種別 */
    TENANTO2_SYUBETU("txt_tenant2_syubetu", "テナント2　種別", DataType.TEXT, 20, 0),
    /** テナント３ 種別 */
    TENANTO3_SYUBETU("txt_tenant3_syubetu", "テナント3　種別", DataType.TEXT, 20, 0),
    /** テナント４ 種別 */
    TENANTO4_SYUBETU("txt_tenant4_syubetu", "テナント4　種別", DataType.TEXT, 20, 0),
    /** テナント５ 種別 */
    TENANTO5_SYUBETU("txt_tenant5_syubetu", "テナント5　種別", DataType.TEXT, 20, 0),
    /** テナント６ 種別 */
    TENANTO6_SYUBETU("txt_tenant6_syubetu", "テナント6　種別", DataType.TEXT, 20, 0),
    /** テナント７ 種別 */
    TENANTO7_SYUBETU("txt_tenant7_syubetu", "テナント7　種別", DataType.TEXT, 20, 0),
    /** テナント８ 種別 */
    TENANTO8_SYUBETU("txt_tenant8_syubetu", "テナント8　種別", DataType.TEXT, 20, 0),
    /** テナント９ 種別 */
    TENANTO9_SYUBETU("txt_tenant9_syubetu", "テナント9　種別", DataType.TEXT, 20, 0),
    /** テナント１０ 種別 */
    TENANTO10_SYUBETU("txt_tenant10_syubetu", "テナント10　種別", DataType.TEXT, 20, 0),
    /** テナント11別 */
    TENANTO11_SYUBETU("txt_tenant11_syubetu", "テナント11　種別", DataType.TEXT, 20, 0),
    /** テナント12 種別 */
    TENANTO12_SYUBETU("txt_tenant12_syubetu", "テナント12　種別", DataType.TEXT, 20, 0),
    /** テナント13 種別 */
    TENANTO13_SYUBETU("txt_tenant13_syubetu", "テナント13　種別", DataType.TEXT, 20, 0),
    /** テナント14 種別 */
    TENANTO14_SYUBETU("txt_tenant14_syubetu", "テナント14　種別", DataType.TEXT, 20, 0),
    /** テナント15 種別 */
    TENANTO15_SYUBETU("txt_tenant15_syubetu", "テナント15　種別", DataType.TEXT, 20, 0),
    /** テナント16 種別 */
    TENANTO16_SYUBETU("txt_tenant16_syubetu", "テナント16　種別", DataType.TEXT, 20, 0),
    /** テナント17 種別 */
    TENANTO17_SYUBETU("txt_tenant17_syubetu", "テナント17　種別", DataType.TEXT, 20, 0),
    /** テナント18 種別 */
    TENANTO18_SYUBETU("txt_tenant18_syubetu", "テナント18　種別", DataType.TEXT, 20, 0),
    /** テナント19 種別 */
    TENANTO19_SYUBETU("txt_tenant19_syubetu", "テナント19　種別", DataType.TEXT, 20, 0),
    /** テナント20 種別 */
    TENANTO20_SYUBETU("txt_tenant20_syubetu", "テナント20　種別", DataType.TEXT, 20, 0),
    /** テナント1 社名 */
    TENANTO1_SYAMEI("txt_tenant1_syamei", "テナント1　社名", DataType.ZEN, 40, 0),
    /** テナント2 社名 */
    TENANTO2_SYAMEI("txt_tenant2_syamei", "テナント2　社名", DataType.ZEN, 40, 0),
    /** テナント3 社名 */
    TENANTO3_SYAMEI("txt_tenant3_syamei", "テナント3　社名", DataType.ZEN, 40, 0),
    /** テナント4 社名 */
    TENANTO4_SYAMEI("txt_tenant4_syamei", "テナント4　社名", DataType.ZEN, 40, 0),
    /** テナント5 社名 */
    TENANTO5_SYAMEI("txt_tenant5_syamei", "テナント5　社名", DataType.ZEN, 40, 0),
    /** テナント6 社名 */
    TENANTO6_SYAMEI("txt_tenant6_syamei", "テナント6　社名", DataType.ZEN, 40, 0),
    /** テナント7 社名 */
    TENANTO7_SYAMEI("txt_tenant7_syamei", "テナント7　社名", DataType.ZEN, 40, 0),
    /** テナント8 社名 */
    TENANTO8_SYAMEI("txt_tenant8_syamei", "テナント8　社名", DataType.ZEN, 40, 0),
    /** テナント9 社名 */
    TENANTO9_SYAMEI("txt_tenant9_syamei", "テナント9　社名", DataType.ZEN, 40, 0),
    /** テナント10 社名 */
    TENANTO10_SYAMEI("txt_tenant10_syamei", "テナント10　社名", DataType.ZEN, 40, 0),
    /** テナント11 社名 */
    TENANTO11_SYAMEI("txt_tenant11_syamei", "テナント11　社名", DataType.ZEN, 40, 0),
    /** テナント12 社名 */
    TENANTO12_SYAMEI("txt_tenant12_syamei", "テナント12　社名", DataType.ZEN, 40, 0),
    /** テナント13 社名 */
    TENANTO13_SYAMEI("txt_tenant13_syamei", "テナント13　社名", DataType.ZEN, 40, 0),
    /** テナント14 社名 */
    TENANTO14_SYAMEI("txt_tenant14_syamei", "テナント14　社名", DataType.ZEN, 40, 0),
    /** テナント15 社名 */
    TENANTO15_SYAMEI("txt_tenant15_syamei", "テナント15　社名", DataType.ZEN, 40, 0),
    /** テナント16 社名 */
    TENANTO16_SYAMEI("txt_tenant16_syamei", "テナント16　社名", DataType.ZEN, 40, 0),
    /** テナント17 社名 */
    TENANTO17_SYAMEI("txt_tenant17_syamei", "テナント17　社名", DataType.ZEN, 40, 0),
    /** テナント18 社名 */
    TENANTO18_SYAMEI("txt_tenant18_syamei", "テナント18　社名", DataType.ZEN, 40, 0),
    /** テナント19 社名 */
    TENANTO19_SYAMEI("txt_tenant19_syamei", "テナント19　社名", DataType.ZEN, 40, 0),
    /** テナント20 社名 */
    TENANTO20_SYAMEI("txt_tenant20_syamei", "テナント20　社名", DataType.ZEN, 40, 0),
    /** 尺数1 */
    SYAKUSUU1("txt_syakusuu1", "尺数1", DataType.INTEGER, 7, 0),
    /** 尺数2 */
    SYAKUSUU2("txt_syakusuu2", "尺数2", DataType.INTEGER, 7, 0),
    /** 尺数3 */
    SYAKUSUU3("txt_syakusuu3", "尺数3", DataType.INTEGER, 7, 0),
    /** 尺数4 */
    SYAKUSUU4("txt_syakusuu4", "尺数4", DataType.INTEGER, 7, 0),
    /** 尺数5 */
    SYAKUSUU5("txt_syakusuu5", "尺数5", DataType.INTEGER, 7, 0),
    /** 尺数6 */
    SYAKUSUU6("txt_syakusuu6", "尺数6", DataType.INTEGER, 7, 0),
    /** 尺数7 */
    SYAKUSUU7("txt_syakusuu7", "尺数7", DataType.INTEGER, 7, 0),
    /** 尺数8 */
    SYAKUSUU8("txt_syakusuu8", "尺数8", DataType.INTEGER, 7, 0),
    /** 尺数9 */
    SYAKUSUU9("txt_syakusuu9", "尺数9", DataType.INTEGER, 7, 0),
    /** 尺数10 */
    SYAKUSUU10("txt_syakusuu10", "尺数10", DataType.INTEGER, 7, 0),
    /** 尺数11 */
    SYAKUSUU11("txt_syakusuu11", "尺数11", DataType.INTEGER, 7, 0),
    /** 尺数12 */
    SYAKUSUU12("txt_syakusuu12", "尺数12", DataType.INTEGER, 7, 0),
    /** 尺数13 */
    SYAKUSUU13("txt_syakusuu13", "尺数13", DataType.INTEGER, 7, 0),
    /** 尺数15 */
    SYAKUSUU15("txt_syakusuu15", "尺数15", DataType.INTEGER, 7, 0),
    /** 尺数20 */
    SYAKUSUU20("txt_syakusuu20", "尺数20", DataType.INTEGER, 7, 0),
    /** 尺数23 */
    SYAKUSUU23("txt_syakusuu23", "尺数23", DataType.INTEGER, 7, 0),
    /** 尺数34 */
    SYAKUSUU34("txt_syakusuu34", "尺数34", DataType.INTEGER, 7, 0),
    /** 尺数43 */
    SYAKUSUU43("txt_syakusuu43", "尺数43", DataType.INTEGER, 7, 0),
    /** 尺数44 */
    SYAKUSUU44("txt_syakusuu44", "尺数44", DataType.INTEGER, 7, 0),
    /** 尺数54 */
    SYAKUSUU54("txt_syakusuu54", "尺数54", DataType.INTEGER, 7, 0),
    /** 敷地面積 */
    AREA_BA("txt_area_ba", "敷地面積", DataType.INTEGER, 7, 0),
    /** 建築面積 */
    AREA_KENTIKU("txt_area_kentiku", "建築面積", DataType.INTEGER, 7, 0),
    /** 敷地面積_B1_床面積 */
    AERA_B1YUKA("txt_aera_b1yuka", "敷地面積_B1_床面積", DataType.INTEGER, 7, 0),
    /** 敷地面積_B1_売場面積 */
    AREA_B1URIBA("txt_area_b1uriba", "敷地面積_B1_売場面積", DataType.INTEGER, 7, 0),
    /** 敷地面積_1F_床面積 */
    AREA_1FYUKA("txt_area_1fyuka", "敷地面積_1F_床面積", DataType.INTEGER, 7, 0),
    /** 敷地面積_1F_売場面積 */
    AREA_FURIBA("txt_area_furiba", "敷地面積_1F_売場面積", DataType.INTEGER, 7, 0),
    /** 敷地面積_2F_床面積 */
    AREA_2FYUKA("txt_area_2fyuka", "敷地面積_2F_床面積", DataType.INTEGER, 7, 0),
    /** 敷地面積_2F_売場面積 */
    AREA_2FURIBA("txt_area_2furiba", "敷地面積_2F_売場面積", DataType.INTEGER, 7, 0),
    /** 敷地面積_3F_床面積 */
    AREA_3FYUKA("txt_area_3fyuka", "敷地面積_3F_床面積", DataType.INTEGER, 7, 0),
    /** 敷地面積_3F_売場面積 */
    AREA_3FURIBA("txt_area_3furiba", "敷地面積_3F_売場面積", DataType.INTEGER, 7, 0),
    /** 敷地面積_4F_床面積 */
    AREA_4FYUKA("txt_area_4fyuka", "敷地面積_4F_床面積", DataType.INTEGER, 7, 0),
    /** 敷地面積_4F_売場面積 */
    AREA_4FURIBA("txt_area_4furiba", "敷地面積_4F_売場面積", DataType.INTEGER, 7, 0),
    /** 駐車台数_普通車_敷地内 */
    PARK_NM_BA("txt_park_nm_ba", "駐車台数_普通車_敷地内", DataType.INTEGER, 5, 0),
    /** 駐車台数_普通車_屋上 */
    PARK_NM_YANE("txt_park_nm_yane", "駐車台数_普通車_屋上", DataType.INTEGER, 5, 0),
    /** 駐車台数_普通車_飛地 */
    PARK_NM_TOBI("txt_park_nm_tobi", "駐車台数_普通車_飛地", DataType.INTEGER, 5, 0),
    /** 駐車台数_軽_敷地内 */
    PARK_LT_BA("txt_park_lt_ba", "駐車台数_軽_敷地内", DataType.INTEGER, 5, 0),
    /** 駐車台数_軽_屋上 */
    PARK_LT_YANE("txt_park_lt_yane", "駐車台数_軽_屋上", DataType.INTEGER, 5, 0),
    /** 駐車台数_軽_飛地 */
    PARK_LT_TOBI("txt_park_lt_tobi", "駐車台数_軽_飛地", DataType.INTEGER, 5, 0),
    /** 駐車台数_障害者_敷地内 */
    PARK_HC_BA("txt_park_hc_ba", "駐車台数_障害者_敷地内", DataType.INTEGER, 5, 0),
    /** 平均回転率 */
    HEIKINKAITENRITU("txt_heikinkaitenritu", "平均回転率", DataType.DECIMAL, 4, 1),
    /** 必要台数 */
    HITUYOUDAISUU("txt_hituyoudaisuu", "必要台数", DataType.DECIMAL, 4, 1),
    /** 駐車台数_障害者_屋上 */
    PARK_HC_YANE("txt_park_hc_yane", "駐車台数_障害者_屋上", DataType.INTEGER, 5, 0),
    /** 駐車台数_障害者_飛地 */
    PARK_HC_TOBI("txt_park_hc_tobi", "駐車台数_障害者_飛地", DataType.INTEGER, 5, 0),
    /** オーナー（店）_名前 */
    OWNT_NMKN("txt_ownt_nmkn", "オーナー（店）_名前", DataType.TEXT, 30, 0),
    /** オーナー（店）_住所_都道府県 */
    OWNT_ADDRKN_T("txt_ownt_addrkn_t", "オーナー（店）_住所_都道府県", DataType.TEXT, 10, 0),
    /** オーナー（店）_住所_市町村 */
    OWNT_ADDRKN_S("txt_ownt_addrkn_s", "オーナー（店）_住所_市町村", DataType.TEXT, 20, 0),
    /** オーナー（店）_住所_町字 */
    OWNT_ADDRKN_M("txt_ownt_addrkn_m", "オーナー（店）_住所_町字", DataType.TEXT, 10, 0),
    /** オーナー（店）_住所_番地 */
    OWNT_ADDRKN_B("txt_ownt_addrkn_b", "オーナー（店）_住所_番地", DataType.TEXT, 60, 0),
    /** オーナー（駐車場）_名前 */
    OWNP_NMKN("txt_ownp_nmkn", "オーナー（駐車場）_名前", DataType.TEXT, 30, 0),
    /** オーナー（駐車場）_住所_都道府県 */
    OWNP_ADDRKN_T("txt_ownp_addrkn_t", "オーナー（駐車場）_住所_都道府県", DataType.TEXT, 10, 0),
    /** オーナー（駐車場）_住所_市町村 */
    OWNP_ADDRKN_S("txt_ownp_addrkn_s", "オーナー（駐車場）_住所_市町村", DataType.TEXT, 20, 0),
    /** オーナー（駐車場）_住所_町字 */
    OWNP_ADDRKN_M("txt_ownp_addrkn_m", "オーナー（駐車場）_住所_町字", DataType.TEXT, 10, 0),
    /** オーナー（駐車場）_住所_番地 */
    OWNP_ADDRKN_B("txt_ownp_addrkn_b", "オーナー（駐車場）_住所_番地", DataType.TEXT, 60, 0),
    /** オーナー（その他）_名前 */
    OWNO_NMKN("txt_owno_nmkn", "オーナー（その他）_名前", DataType.TEXT, 30, 0),
    /** オーナー（その他）_住所_都道府県 */
    OWNO_ADDRKN_T("txt_owno_addrkn_t", "オーナー（その他）_住所_都道府県", DataType.TEXT, 10, 0),
    /** オーナー（その他）_住所_市町村 */
    OWNO_ADDRKN_S("txt_owno_addrkn_s", "オーナー（その他）_住所_市町村", DataType.TEXT, 20, 0),
    /** オーナー（その他）_住所_町村 */
    OWNO_ADDRKN_M("txt_owno_addrkn_m", "オーナー（その他）_住所_町村", DataType.TEXT, 10, 0),
    /** オーナー（その他）_住所_番地 */
    OWNO_ADDRKN_B("txt_owno_addrkn_b", "オーナー（その他）_住所_番地", DataType.TEXT, 60, 0),
    /** 店舗数 */
    TENPOSU("txt_tenposu", "店舗数", DataType.INTEGER, 5, 0),
    /** プライスカード発行トラン コメント */
    COMAN("txt_coman", "コメント", DataType.KANA, 15, 0),
    /** プライスカード発行トラン 商品マスタ予約日 */
    MST_YOYAKUDT("txt_mst_yoyakudt", "商品マスタ予約日", DataType.DATE, 10, 0),
    /** プライスカード発行枚数トラン 構成ページ */
    KOSEPAGE("txt_kosepage", "構成ページ", DataType.SUUJI, 8, 0),
    /** プライスカード発行枚数トラン 枚数 */
    MAISU("txt_maisu", "枚数", DataType.INTEGER, 3, 0),
    /** プライスカード発行トラン コピー枚数 */
    COPYSU("txt_copysu", "コピー枚数", DataType.INTEGER, 3, 0),
    /** 基準日 */
    STANDARDDATE("txt_standarddate", "基準日", DataType.DATE, 8, 0),
    /** 処理日付 */
    SHORIDT("txt_shoridt", "処理日付", DataType.DATE, 8, 0),
    /** タイトル */
    TLTLE("txt_title", "タイトル", DataType.TEXT, 15, 0),
    /** コース番号 */
    COURSENO("txt_courseno", "コース番号", DataType.INTEGER, 3, 0),
    /** 店舗コード */
    STORECD("txt_storecd", "店舗コード", DataType.LPADZERO, 3, 0),
    /** 有効開始日 */
    EFFECTIVESTARTDATE("txt_effectivestartdate", "有効開始日", DataType.DATE, 8, 0),
    /** 有効終了日 */
    EFFECTIVEENDDATE("txt_effectiveenddate", "有効終了日", DataType.DATE, 8, 0),
    /** 取扱終了日 */
    HANDLEENDDATE("txt_handleenddate", "取扱終了日", DataType.DATE, 8, 0),

    /** 有効開始日 */
    YUKO_STDT("txt_yuko_stdt", "有効開始日", DataType.DATE, 10, 0),
    /** 有効終了日 */
    YUKO_EDDT("txt_yuko_eddt", "有効終了日", DataType.DATE, 10, 0),
    /** グループ分類名 */
    GRPKN("sel_grpkn", "グループ分類名", DataType.TEXT, 100, 0),
    /** 自動発注区分 */
    AHSKB("txt_ahskb", "自動発注区分", DataType.INTEGER, 1, 0),

    /** 使用トレイ */
    UTRAY("txt_utray", "使用トレイ", DataType.ZEN, 30, 0),
    /** 包装形態 */
    KONPOU("txt_konpou", "包装形態", DataType.ZEN, 20, 0),
    /** 風袋 */
    FUTAI("txt_futai", "風袋", DataType.INTEGER, 3, 0),
    /** 内容量 */
    NAIKN("txt_naikn", "内容量", DataType.ZEN, 46, 0),
    /** 原材原価 */
    GENKA("txt_genka", "原料原価", DataType.INTEGER, 8, 0),
    /** 歩留り */
    BUDOMARI("txt_budomari", "歩留り", DataType.INTEGER, 4, 0),
    /** 原価小計 */
    GENKAKEI("txt_genkakei", "原価小計", DataType.INTEGER, 8, 0),
    /** 呼出コード */
    CALLCD("txt_callcd", "呼出コード", DataType.LPADZERO, 6, 0),
    /** 登録限度数 */
    MAXSU("txt_maxsu", "登録限度数", DataType.INTEGER, 4, 0),
    /** デフォルト_登録限度数 */
    DE_MAXSU("txt_de_maxsu", "デフォルト_登録限度数", DataType.INTEGER, 4, 0),

    /** ----- 販促 ----- **/
    /** 週№ */
    SHUNO("txt_shuno", "週№", DataType.INTEGER, 4, 0),
    /** 催し区分 */
    MOYSKBN("txt_moyskbn", "催し区分", DataType.INTEGER, 1, 0),
    /** 催しコード（催し開始日） */
    MOYSSTDT("txt_moysstdt", "催しコード（催し開始日）", DataType.DATE, 6, 0),
    /** 催し終了日 */
    MOYSEDDT("txt_moyseddt", "催し終了日", DataType.DATE, 6, 0),
    /** 催し連番 */
    MOYSRBAN("txt_moysrban", "催し連番", DataType.LPADZERO, 3, 0),
    /** 催し連番(入力用) */
    MOYSRBANINP("txt_moysrbaninp", "催し連番", DataType.LPADZERO, 2, 0),
    /** B/M番号 */
    BMNNO("txt_bmnno", "B/M番号", DataType.LPADZERO, 3, 0),
    /** セット番号 */
    STNO("txt_stno", "セット番号", DataType.LPADZERO, 3, 0),
    /** B/M名称（漢字） */
    BMNMKN("txt_bmnmkn", "B/M名称（漢字）", DataType.ZEN, 40, 0),
    /** B/M名称（ｶﾅ） */
    BMNMAN("txt_bmnman", "B/M名称（ｶﾅ）", DataType.KANA, 20, 0),
    /** 1個売り総売価1 */
    BD_KOSU1("txt_bd_kosu1", "1個売り総売価1", DataType.INTEGER, 2, 0),
    /** 1個売り総売価金額 */
    BD_BAIKAAN1("txt_baikaan1", "1個売り総売価1金額", DataType.INTEGER, 6, 0),
    /** 1個売り総売価2 */
    BD_KOSU2("txt_bd_kosu2", "1個売り総売価2", DataType.INTEGER, 2, 0),
    /** 1個売り総売価金額2 */
    BD_BAIKAAN2("txt_baikaan2", "1個売り総売価2金額", DataType.INTEGER, 6, 0),
    /** 催しコード（連結フォーマット版） */
    MOYSCD("txt_moyscd", "催しコード", DataType.SUUJI, 10, 0),
    /** 販売開始日 */
    HBSTDT("txt_hbstdt", "販売開始日", DataType.DATE, 6, 0),
    /** 販売終了日 */
    HBEDDT("txt_hbeddt", "販売終了日", DataType.DATE, 6, 0),
    /** 販売期間 */
    HBPERIOD("txt_hbperiod", "販売期間", DataType.TEXT, 30, 0),
    /** 催し期間 */
    MOYPERIOD("txt_moyperiod", "催し期間", DataType.TEXT, 30, 0),
    /** 納入開始日 */
    NNSTDT("txt_nnstdt", "納入開始日", DataType.DATE, 6, 0),
    /** 納入終了日 */
    NNEDDT("txt_nneddt", "納入終了日", DataType.DATE, 6, 0),
    /** 納入期間 */
    NNPERIOD("txt_nnperiod", "納入期間", DataType.TEXT, 30, 0),
    /** PLU配信日 */
    PLUSDDT("txt_plusddt", "PLU配信日", DataType.DATE, 6, 0),
    /** 催し名称（漢字） */
    MOYKN("txt_moykn", "催し名称（漢字）", DataType.ZEN, 40, 0),
    /** 催し名称(半角カナ) */
    MOYAN("txt_moyan", "催し名称(半角カナ)", DataType.KANA, 20, 0),
    /** 月締め */
    GTSIMEDT("txt_gtsimedt", "月締め", DataType.DATE, 6, 0),
    /** 最終締 */
    LSIMEDT("txt_lsimedt", "最終締", DataType.DATE, 6, 0),
    /** アンケート月度 */
    QAYYYYMM("txt_qayyyymm", "アンケート月度", DataType.YYMM, 4, 0),
    /** アンケート月度枝番 */
    QAEND("txt_qaend", "アンケート月度枝番", DataType.LPADZERO, 2, 0),
    /** アンケート作成日 */
    QACREDT("txt_qacredt", "アンケート作成日", DataType.DATE, 6, 0),
    /** アンケート再作成日 */
    QARCREDT("txt_qarcredt", "アンケート再作成日", DataType.DATE, 6, 0),
    /** アンケート取込開始日 */
    QADEVSTDT("txt_qadevstdt", "アンケート取込開始日", DataType.DATE, 6, 0),
    /** 行番号 */
    GYONO("txt_gyono", "行番号", DataType.SUUJI, 7, 0),
    /** エラー箇所 */
    ERRFLD("txt_errfld", "エラー箇所", DataType.TEXT, 100, 0),
    /** エラー理由 */
    MSGTXT1("txt_msgtxt1", "エラー理由", DataType.TEXT, 100, 0),
    /** エラー値 */
    ERRVL("txt_errvl", "エラー値", DataType.TEXT, 100, 0),
    /** 店コード */
    TAISYOTEN("txt_rankno_add", "対象店コード", DataType.LPADZERO, 3, 0),
    /** 店コード */
    JYOGAITEN("txt_rankno_del", "除外店コード", DataType.LPADZERO, 3, 0),

    /** 月 */
    HATFLG_MON("txt_hatflg_mon", "月", DataType.INTEGER, 1, 0),
    /** 火 */
    HATFLG_TUE("txt_hatflg_thu", "火", DataType.INTEGER, 1, 0),
    /** 水 */
    HATFLG_WED("txt_hatflg_wed", "水", DataType.INTEGER, 1, 0),
    /** 木 */
    HATFLG_THU("txt_hatflg_thu", "木", DataType.INTEGER, 1, 0),
    /** 金 */
    HATFLG_FRI("txt_hatflg_fri", "金", DataType.INTEGER, 1, 0),
    /** 土 */
    HATFLG_SAT("txt_hatflg_sat", "土", DataType.INTEGER, 1, 0),
    /** 日 */
    HATFLG_SUN("txt_hatflg_sun", "日", DataType.INTEGER, 1, 0),
    /** 企画コード */
    KKKCD("txt_kkkcd", "企画コード", DataType.LPADZERO, 4, 0),
    /** 企画名称 */
    KKKKM("txt_kkkkm", "企画名称", DataType.ZEN, 40, 0),
    /** 企画No */
    KKKNO("txt_kkkno", "企画No", DataType.INTEGER, 4, 0),
    /** カタログ番号 */
    CATALGNO("txt_catalgno", "カタログ番号", DataType.LPADZERO, 2, 0),
    /** 発注日 */
    HTDT("txt_htdt", "発注日", DataType.DATE, 8, 0),
    /** 受付開始日 */
    UKESTDT("txt_ukestdt", "受付開始日", DataType.DATE, 8, 0),
    /** 受付終了日 */
    UKEEDDT("txt_ukeeddt", "受付終了日", DataType.DATE, 8, 0),
    /** 店舗入力開始日 */
    TENISTDT("txt_tenistdt", "店舗入力開始日", DataType.DATE, 8, 0),
    /** 店舗入力終了日 */
    TENIEDDT("txt_tenieddt", "店舗入力終了日", DataType.DATE, 8, 0),
    /** 予定数 */
    YOTEISU("txt_yoteisu", "予定数", DataType.SUUJI, 6, 0),
    /** 限度数 */
    GENDOSU("txt_gendosu", "限度数", DataType.SUUJI, 6, 0),
    /** 納入日 */
    NNDT("txt_nndt", "納入日", DataType.DATE, 6, 0),
    /** 発注数 */
    HTSU("txt_htsu", "発注数", DataType.INTEGER, 5, 0),
    /** 分類割引名称 */
    BTKN("txt_btkn", "分類割引名称", DataType.ZEN, 40, 0),
    /** 割引率 */
    WARIRT("txt_warirt", "割引率", DataType.INTEGER, 2, 0),

    /** A_BAIKAAM：A総売価 */
    A_BAIKAAM("txt_a_baikaam", "A総売価", DataType.INTEGER, 6, 0),
    /** A_BAIKAAM_100G：A総売価 */
    A_BAIKAAM_100G("txt_a_baikaam_100g", "A総売価", DataType.INTEGER, 6, 0),
    /** Aランク */
    A_RANKNO("txt_a_rankno", "Aランク", DataType.LPADZERO, 3, 0),
    /** A_BAIKAAM_PACK：P総売価 */
    A_BAIKAAM_PACK("txt_a_baikaam_pack", "P総売価", DataType.INTEGER, 6, 0),
    /** A_GENKAAM_1KG：1Kg総売価 */
    A_GENKAAM_1KG("txt_a_genkaam_1kg", "1Kg総売価", DataType.INTEGER, 6, 0),
    /** A_WRITUKBN：A総売価 */
    A_WRITUKBN("txt_a_writukbn", "A総売価", DataType.INTEGER, 6, 0),
    /** B_BAIKAAM：B総売価 */
    B_BAIKAAM("txt_b_baikaam", "B総売価", DataType.INTEGER, 6, 0),
    /** B_BAIKAAM_100G：B総売価 */
    B_BAIKAAM_100G("txt_b_baikaam_100g", "B総売価", DataType.INTEGER, 6, 0),
    /** Bランク */
    B_RANKNO("txt_b_rankno", "Bランク", DataType.LPADZERO, 3, 0),
    /** B_BAIKAAM_PACK：P総売価 */
    B_BAIKAAM_PACK("txt_b_baikaam_pack", "P総売価", DataType.INTEGER, 6, 0),
    /** B_GENKAAM_1KG：1Kg総売価 */
    B_GENKAAM_1KG("txt_b_genkaam_1kg", "1Kg総売価", DataType.INTEGER, 6, 0),
    /** B_WRITUKBN：B総売価 */
    B_WRITUKBN("txt_b_writukbn", "B総売価", DataType.INTEGER, 6, 0),
    /** BD1_A_BAIKAAN：総売価1A */
    BD1_A_BAIKAAN("txt_bd1_a_baikaan", "総売価1A", DataType.INTEGER, 6, 0),
    /** BD1_B_BAIKAAN：総売価1B */
    BD1_B_BAIKAAN("txt_bd1_b_baikaan", "総売価1B", DataType.INTEGER, 6, 0),
    /** BD1_C_BAIKAAN：総売価1C */
    BD1_C_BAIKAAN("txt_bd1_c_baikaan", "総売価1C", DataType.INTEGER, 6, 0),
    /** BD1_TENSU：点数1 */
    BD1_TENSU("txt_bd1_tensu", "点数1", DataType.INTEGER, 3, 0),
    /** BD2_A_BAIKAAN：総売価２A */
    BD2_A_BAIKAAN("txt_bd2_a_baikaan", "総売価２A", DataType.INTEGER, 6, 0),
    /** BD2_B_BAIKAAN：総売価２B */
    BD2_B_BAIKAAN("txt_bd2_b_baikaan", "総売価２B", DataType.INTEGER, 6, 0),
    /** BD2_C_BAIKAAN：総売価２C */
    BD2_C_BAIKAAN("txt_bd2_c_baikaan", "総売価２C", DataType.INTEGER, 6, 0),
    /** BD2_TENSU：点数2 */
    BD2_TENSU("txt_bd2_tensu", "点数2", DataType.INTEGER, 3, 0),
    /** BDENKBN：別伝区分 */
    BDENKBN("txt_bdenkbn", "別伝区分", DataType.INTEGER, 1, 0),
    /** BINKBN：便区分 */
    BINKBN("txt_binkbn", "便区分", DataType.INTEGER, 1, 0),
    /** C_BAIKAAM：C総売価 */
    C_BAIKAAM("txt_c_baikaam", "C総売価", DataType.INTEGER, 6, 0),
    /** C_BAIKAAM_100G：C総売価 */
    C_BAIKAAM_100G("txt_c_baikaam_100g", "C総売価", DataType.INTEGER, 6, 0),
    /** Cランク */
    C_RANKNO("txt_c_rankno", "Cランク", DataType.LPADZERO, 3, 0),
    /** C_BAIKAAM_PACK：P総売価 */
    C_BAIKAAM_PACK("txt_c_baikaam_pack", "P総売価", DataType.INTEGER, 6, 0),
    /** C_GENKAAM_1KG：1Kg総売価 */
    C_GENKAAM_1KG("txt_c_genkaam_1kg", "1Kg総売価", DataType.INTEGER, 6, 0),
    /** C_WRITUKBN：C総売価 */
    C_WRITUKBN("txt_c_writukbn", "C総売価", DataType.INTEGER, 6, 0),
    /** CHLDNO：子No. */
    CHLDNO("txt_chldno", "子No.", DataType.LPADZERO, 2, 0),
    /** COMMENT_HGW：その他日替コメント */
    COMMENT_HGW("txt_comment_hgw", "その他日替コメント", DataType.ZEN, 100, 0),
    /** COMMENT_POP：POPコメント */
    COMMENT_POP("txt_comment_pop", "POPコメント", DataType.ZEN, 100, 0),
    /** COMMENT_TB：特売コメント */
    COMMENT_TB("txt_comment_tb", "特売コメント", DataType.ZEN, 60, 0),
    /** GENKAAM_1KG：1Kg原価 */
    GENKAAM_1KG("txt_genkaam_1kg", "1Kg原価", DataType.DECIMAL, 8, 2),
    /** GENKAAM_ATO：原価 */
    GENKAAM_ATO("txt_genkaam_ato", "原価", DataType.DECIMAL, 8, 2),
    /** GENKAAM_MAE：原価 */
    GENKAAM_MAE("txt_genkaam_mae", "原価", DataType.DECIMAL, 8, 2),
    /** GENKAAM_PACK：P原価 */
    GENKAAM_PACK("txt_genkaam_pack", "P原価", DataType.DECIMAL, 8, 2),
    /** HBOKUREFLG：一日遅パタン */
    HBOKUREFLG("txt_hbokureflg", "一日遅パタン", DataType.INTEGER, 1, 0),
    /** HBYOTEISU：予定数 */
    HBYOTEISU("txt_hbyoteisu", "予定数", DataType.INTEGER, 6, 0),
    /** HTASU：発注総数 */
    HTASU("txt_htasu", "発注総数", DataType.INTEGER, 6, 0),
    /** JUHTDT：事前打出(日付) */
    JUHTDT("txt_juhtdt", "事前打出(日付)", DataType.DATE, 8, 0),
    /** KO_A_BAIKAAN：総売価A */
    KO_A_BAIKAAN("txt_ko_a_baikaan", "総売価A", DataType.INTEGER, 6, 0),
    /** KO_B_BAIKAAN：総売価B */
    KO_B_BAIKAAN("txt_ko_b_baikaan", "総売価B", DataType.INTEGER, 6, 0),
    /** KO_C_BAIKAAN：総売価C */
    KO_C_BAIKAAN("txt_ko_c_baikaan", "総売価C", DataType.INTEGER, 6, 0),
    /** PARNO：グループNo. */
    PARNO("txt_parno", "グループNo.", DataType.ALPHA, 3, 0),
    /** POPCD：POPコード */
    POPCD("txt_popcd", "POPコード", DataType.LPADZERO, 10, 0),
    /** POPSU：枚数 */
    POPSU("txt_popsu", "枚数", DataType.INTEGER, 2, 0),
    /** POPSZ：POPサイズ */
    POPSZ("txt_popsz", "POPサイズ", DataType.KANA, 3, 0),
    /** PTNNO：パターンNo. */
    PTNNO("txt_ptnno", "パターンNo.", DataType.LPADZERO, 3, 0),
    /** 対象店 */
    RANKNO_ADD("txt_rankno_add", "対象店", DataType.LPADZERO, 3, 0),
    /** RANKNO_ADD_A：対象店 */
    RANKNO_ADD_A("txt_rankno_add_a", "対象店", DataType.LPADZERO, 3, 0),
    /** RANKNO_ADD_B：B売店 */
    RANKNO_ADD_B("txt_rankno_add_b", "B売店", DataType.LPADZERO, 3, 0),
    /** RANKNO_ADD_B：C売店 */
    RANKNO_ADD_C("txt_rankno_add_c", "C売店", DataType.LPADZERO, 3, 0),
    /** RANKNO_DEL：除外店 */
    RANKNO_DEL("txt_rankno_del", "除外店", DataType.LPADZERO, 3, 0),
    /** SEGN_1KOSU：一人 */
    SEGN_1KOSU("txt_segn_1kosu", "一人", DataType.INTEGER, 3, 0),
    /** SEGN_NINZU：先着人数 */
    SEGN_NINZU("txt_segn_ninzu", "先着人数", DataType.INTEGER, 5, 0),
    /** SHNCOLOR：商品色 */
    SHNCOLOR("txt_shncolor", "商品色", DataType.ZEN, 20, 0),
    /** SHNSIZE：商品サイズ */
    SHNSIZE("txt_shnsize", "商品サイズ", DataType.ZEN, 40, 0),
    /** TENKAIKBN：展開区分 */
    TENKAIKBN("txt_tenkaikbn", "展開区分", DataType.INTEGER, 1, 0),
    /** TENKAISU：展開数 */
    TENKAISU("txt_tenkaisu", "展開数", DataType.INTEGER, 6, 0),
    /** TENRANK：ランク（1～10） */
    TENRANK("txt_tenrank", "ランク（1～10）", DataType.INTEGER, 1, 0),
    /** TPSU：店舗数 */
    TPSU("txt_tpsu", "店舗数", DataType.INTEGER, 3, 0),
    /** TSEIKBN：訂正区分 */
    TSEIKBN("txt_tseikbn", "訂正区分", DataType.INTEGER, 1, 0),
    /** WAPPNKBN：ワッペン区分 */
    WAPPNKBN("txt_wappnkbn", "ワッペン区分", DataType.INTEGER, 1, 0),
    /** 全店同一数量_月 */
    ALL_SURYO_MON("txt_all_suryo_mon", "全店同一数量_月", DataType.INTEGER, 5, 0),
    /** 全店同一数量_火 */
    ALL_SURYO_TUE("txt_all_suryo_tue", "全店同一数量_火", DataType.INTEGER, 5, 0),
    /** 全店同一数量_水 */
    ALL_SURYO_WED("txt_all_suryo_wed", "全店同一数量_水", DataType.INTEGER, 5, 0),
    /** 全店同一数量_木 */
    ALL_SURYO_THU("txt_all_suryo_thu", "全店同一数量_木", DataType.INTEGER, 5, 0),
    /** 全店同一数量_金 */
    ALL_SURYO_FRI("txt_all_suryo_fri", "全店同一数量_金", DataType.INTEGER, 5, 0),
    /** 全店同一数量_土 */
    ALL_SURYO_SAT("txt_all_suryo_sat", "全店同一数量_土", DataType.INTEGER, 5, 0),
    /** 全店同一数量_日 */
    ALL_SURYO_SUN("txt_all_suryo_sun", "全店同一数量_日", DataType.INTEGER, 5, 0),
    /** 数量_月 */
    SURYO_MON("txt_suryo_mon", "数量_月", DataType.INTEGER, 5, 0),
    /** 数量_火 */
    SURYO_TUE("txt_suryo_tue", "数量_火", DataType.INTEGER, 5, 0),
    /** 数量_水 */
    SURYO_WED("txt_suryo_wed", "数量_水", DataType.INTEGER, 5, 0),
    /** 数量_木 */
    SURYO_THU("txt_suryo_thu", "数量_木", DataType.INTEGER, 5, 0),
    /** 数量_金 */
    SURYO_FRI("txt_suryo_fri", "数量_金", DataType.INTEGER, 5, 0),
    /** 数量_土 */
    SURYO_SAT("txt_suryo_sat", "数量_土", DataType.INTEGER, 5, 0),
    /** 数量_日 */
    SURYO_SUN("txt_suryo_sun", "数量_日", DataType.INTEGER, 5, 0),
    /** 部門予算 */
    // 仕様書に記載がない為、テーブル定義書を元に入力桁数を指定
    BMNYSANAM("txt_bmnysanam", "部門予算", DataType.INTEGER, 7, 0),
    /** 構成比 */
    // 仕様書に記載がない為、テーブル定義書を元に入力桁数を指定
    KOUSEIHI("txt_kouseihi", "構成比", DataType.DECIMAL, 5, 1),
    /** BYCD */
    BYCD("txt_bycd", "便BYCD", DataType.INTEGER, 7, 0),

    // TODO 暫定
    /** サプライNo */
    SUPPLYNO("txt_supplyno", "サプライNo", DataType.INTEGER, 0, 0),
    /** グループ№ */
    GROUPNO("txt_groupno", "グループ№", DataType.SUUJI, 4, 0),
    /** 集計CD */
    SHUKEICD("txt_shukeicd", "集計CD", DataType.SUUJI, 4, 0),
    /** 部門原価率 */
    BMNGENKART("txt_bmn_genkart", "部門原価率", DataType.DECIMAL, 5, 2),
    /** エリア */
    AERACD("txt_aeracd", "エリア", DataType.INTEGER, 2, 0),
    /** テナント */
    TENANTCD("txt_tenantcd", "テナントコード", DataType.LPADZERO, 5, 0),
    /** テナント */
    BMNKN("txt_bmnkn", "部門（テナント）名称（漢字）", DataType.ZEN, 20, 0),
    /** 部門レシート名称（漢字） */
    BMNRECEIPTKN("txt_bmnreceiptkn", "漢字名", DataType.ZEN, 40, 0),
    /** 部門レシート名称（カナ） */
    BMNRECEIPTAN("txt_bmnreceiptan", "カナ名", DataType.KANA, 20, 0),
    /** 部門属性1 */
    BMN_ATR1("txt_bmn_atr1", "部門属性1", DataType.SUUJI, 1, 0),
    /** 部門属性2 */
    BMN_ATR2("txt_bmn_atr2", "部門属性1", DataType.SUUJI, 1, 0),
    /** 部門属性3 */
    BMN_ATR3("txt_bmn_atr3", "部門属性1", DataType.SUUJI, 1, 0),
    /** 部門属性4 */
    BMN_ATR4("txt_bmn_atr4", "部門属性1", DataType.SUUJI, 1, 0),
    /** 部門属性5 */
    BMN_ATR5("txt_bmn_atr5", "部門属性1", DataType.SUUJI, 1, 0),
    /** 取扱アイテム数 */
    TENITEMSU("txt_tenitemsu", "取扱アイテム数", DataType.SUUJI, 7, 0),
    /** PLUレコード数 */
    TENPLUSU("txt_tenplusu", "PLUレコード数", DataType.SUUJI, 7, 0),
    /** 入力№ */
    INPUTNO("txt_inputno", "入力№", DataType.LPADZERO, 5, 0),

    /** コピー先店番 */
    CPTOTENNO("txt_cptotenno", "コピー先", DataType.LPADZERO, 3, 0),
    /** コピー元店番 */
    CPFROMTENNO("txt_cpfromtenno", "コピー元", DataType.LPADZERO, 3, 0),
    /** ランク№開始 */
    RANKNOST("txt_ranknost", "ランク№開始", DataType.LPADZERO, 3, 0),
    /** ランク№終了 */
    RANKNOED("txt_ranknoed", "ランク№終了", DataType.LPADZERO, 3, 0),
    /** 通常率ﾊﾟﾀｰﾝ№開始 */
    RTPTNNOST("txt_rtptnnost", "通常率ﾊﾟﾀｰﾝ№開始", DataType.LPADZERO, 3, 0),
    /** 通常率ﾊﾟﾀｰﾝ№終了 */
    RTPTNNOED("txt_rtptnnoed", "通常率ﾊﾟﾀｰﾝ№終了", DataType.LPADZERO, 3, 0),
    /** 実績率ﾊﾟﾀｰﾝ№開始 */
    JRTPTNNOST("txt_jrtptnnost", "実績率ﾊﾟﾀｰﾝ№開始", DataType.LPADZERO, 12, 0),
    /** 実績率ﾊﾟﾀｰﾝ№終了 */
    JRTPTNNOED("txt_jrtptnnoed", "実績率ﾊﾟﾀｰﾝ№終了", DataType.LPADZERO, 12, 0),
    /** ランク№ */
    RANKNO("txt_rankno", "ランク№", DataType.LPADZERO, 3, 0),
    /** 数量ﾊﾟﾀｰﾝ№ */
    SRYPTNNO("txt_sryptnno", "数量ﾊﾟﾀｰﾝ№", DataType.LPADZERO, 3, 0),
    /** 部門(通常率ﾊﾟﾀｰﾝ) */
    RTPTNBMNCD("txt_rtptnbmncd", "部門", DataType.LPADZERO, 2, 0),
    /** 通常率ﾊﾟﾀｰﾝ№ */
    RTPTNNO("txt_rtptnno", "通常率ﾊﾟﾀｰﾝ№", DataType.LPADZERO, 3, 0),
    /** 総数量(通常率) */
    RTSOUSU("txt_rtsousu", "総数量", DataType.INTEGER, 6, 0),
    /** 部門(実績率ﾊﾟﾀｰﾝ) */
    JRTPTNBMNCD("txt_jrtptnbmncd", "部門", DataType.LPADZERO, 2, 0),
    /** 実績率ﾊﾟﾀｰﾝ№ */
    JRTPTNNO("txt_jrtptnno", "実績率ﾊﾟﾀｰﾝ№", DataType.LPADZERO, 12, 0),
    /** 総数量(実績率) */
    JRTSOUSU("txt_jrtsousu", "総数量", DataType.INTEGER, 6, 0),
    /** ランク名称 */
    RANKKN("txt_rankkn", "ランク名称", DataType.ZEN, 40, 0),
    /** 数量パターン名称 */
    SRYPTNKN("txt_sryptnkn", "数量パターン名称", DataType.ZEN, 40, 0),
    /** 通常率パターン名称 */
    RTPTNKN("txt_rtptnkn", "通常率パターン名称", DataType.ZEN, 40, 0),
    /** 実績率パターン名称 */
    JRTPTNKN("txt_jrtptnkn", "実績率パターン名称", DataType.TEXT, 40, 0),
    /** 総数量 */
    SOUSU("txt_sousu", "総数量", DataType.INTEGER, 6, 0),
    /** 合計数 */
    GOUKEISU("txt_goukeisu", "合計数", DataType.INTEGER, 6, 0),
    /** 分配率 */
    BUNPAIRT("txt_bunpairt", "分配率", DataType.INTEGER, 5, 0),
    /** 売上 */
    URIAGE("txt_uriage", "売上", DataType.INTEGER, 6, 0),
    /** 点数 */
    TENSU("txt_tensu", "点数", DataType.INTEGER, 6, 0),
    /** 年月 */
    YYMM("txt_yymm", "年月", DataType.LPADZERO, 4, 0),
    /** 年月(週No.) */
    YYWW("txt_yyww", "年月(週No.)", DataType.LPADZERO, 4, 0),
    /** ランク */
    RANK("txt_rank", "ランク", DataType.ALPHAL, 1, 0),
    /** 参考販売実績 */
    SANKOUHBJ("txt_sankouhbj", "参考販売実績", DataType.TEXT, 40, 0),
    /** 店ランク配列 */
    TENRANKARR("txt_tenrank_arr", "店ランク配列", DataType.TEXT, 400, 0),
    /** エリア */
    AREACD("txt_areacd", "エリア", DataType.INTEGER, 2, 0),

    /** 名称 */
    MEISHOKN("txt_meishokn", "名称", DataType.TEXT, 40, 0),
    /** ダミーコード（商品コード） */
    DUMMYCD("txt_dummycd", "ダミーコード（商品コード）", DataType.SUUJI, 8, 0),
    /** 管理番号 */
    KANRINO("txt_kanrino", "管理番号", DataType.INTEGER, 4, 0),
    /** 管理番号枝番 */
    KANRIENO("txt_kanrieno", "管理番号枝番", DataType.INTEGER, 0, 0),
    /** 数量 */
    SURYO("txt_suryo", "数量", DataType.INTEGER, 5, 0),
    /** 商品区分 */
    SHNKBN("txt_shnkbn", "商品区分", DataType.INTEGER, 1, 0),
    /** 構成ページ */
    KSPAGE("txt_kspage", "構成ページ", DataType.LPADZERO, 8, 0),
    /** 店発注数配列 */
    TENHTSU_ARR("txt_tenhtsu_arr", "店発注数配列", DataType.TEXT, 2000, 0),
    /** 事前区分 */
    JUKBN("txt_jukbn", "事前区分", DataType.INTEGER, 1, 0),
    /** ランク */
    RANKCD_ADD("txt_rankcd_add", "ランク", DataType.INTEGER, 1, 0),
    /** ランク */
    SURYOPTN("txt_suryoptn", "ランク", DataType.LPADZERO, 3, 0),
    /** B/Mフラグ */
    BMFLG("txt_bmflg", "B/Mフラグ", DataType.SUUJI, 1, 0),
    /** 店舗アンケート締切日 */
    QASMDT("txt_qasmdt", "店舗アンケート締切日", DataType.DATE, 8, 0),
    /** リスト№ */
    LSTNO("txt_lstno", "リストNo", DataType.SUUJI, 6, 0),
    /** 平均パック単価 */
    AVGPTANKAAM("txt_avgptankaam", "平均パック単価", DataType.INTEGER, 5, 0),
    /** 平均パック単価(販促) */
    HS_AVGPTANKAAM("txt_hs_avgptankaam", "平均パック単価(販促)", DataType.INTEGER, 5, 0),
    /** JANコード1 */
    JANCD1("txt_jancd1", "JANコード1", DataType.SUUJISPACE, 14, 0),
    /** JANコード2 */
    JANCD2("txt_jancd2", "JANコード2", DataType.SUUJISPACE, 14, 0),
    /** 点数配列 */
    TENTENARR("txt_tenten_arr", "点数配列", DataType.TEXT, 3600, 0),
    /** ランク(店番一括入力) */
    RANKIINPUT("txt_rankiinput", "ランク", DataType.ALPHAL, 1, 0),
    /** 店番(店番一括入力) */
    TENCDIINPUT("txt_tencdiinput", "店番", DataType.LPADZERO, 3, 0),
    /** 最低発注数 */
    MINSU("txt_minsu", "最低発注数", DataType.INTEGER, 3, 0),
    /** 各店回答フラグ */
    MBANSFLG("txt_mbansflg", "各店回答フラグ", DataType.TEXT, 1, 0),
    /** 売価差替1 */
    URICHGAM1("txt_urichgam1", "売価差替1", DataType.INTEGER, 6, 0),
    /** 売価差替1 */
    URICHGAM2("txt_urichgam2", "売価差替2", DataType.INTEGER, 6, 0),
    /** 売価差替1 */
    URICHGAM3("txt_urichgam3", "売価差替3", DataType.INTEGER, 6, 0),
    /** 所属コード */
    SZKCD("txt_szkcd", "所属コード", DataType.INTEGER, 3, 0),
    /** 店舗年齢 */
    TEN_NENSU("txt_tennensu", "店舗年齢", DataType.DECIMAL, 4, 1),
    /** 日商 */
    NISSYOU("txt_nissyou", "店舗年齢", DataType.INTEGER, 5, 0),
    /** 売上前比 */
    URIAGEZENHI("txt_uriagezenhi", "売上前比", DataType.DECIMAL, 4, 1),
    /** 荒利率 */
    ARARIRITU("txt_arariritu", "荒利率", DataType.DECIMAL, 4, 1),
    /** 冷設 */
    REISETU("txt_reisetu", "冷設", DataType.ZEN, 40, 0),
    /** 惣菜 */
    SOUZAI("txt_souzai", "惣菜", DataType.ZEN, 40, 0),
    /** ゴンドラ */
    GONDORA("txt_gondora", "ゴンドラ", DataType.ZEN, 40, 0),
    /** AED */
    AED("txt_aed", "AED", DataType.ZEN, 40, 0),
    /** タイムサービス 開始時間 */
    PROMO_BGM_TM("txt_promo_bgm_tm", "タイムサービス 開始時間", DataType.HHMM, 19, 0),
    /** タイムサービス 終了時間 */
    PROMO_END_TM("txt_promo_end_tm", "タイムサービス 終了時間", DataType.HHMM, 19, 0),
    /** セット名称 */
    STMN("txt_stmn", "セット名称", DataType.ZEN, 20, 0),
    /** 成立価格 */
    ESTGK("txt_estgk", "成立価格", DataType.INTEGER, 8, 0),
    /** セット番号 */
    STNO2("txt_stno2", "セット番号", DataType.LPADZERO, 4, 0),
    /** 入力不可フラグ */
    NGFLG("txt_ngflg", "入力不可フラグ", DataType.SUUJI, 1, 0),

    // パスワード変更用
    /** パスワード */
    PASS("txt_pass", "パスワード", DataType.ALPHA, 20, 0),
    /** ID */
    USER_ID("txt_user_id", "ID", DataType.ALPHA, 13, 0),
    /** 姓名 */
    NAME("txt_name", "姓名", DataType.TEXT, 60, 0),
    /** 姓 */
    NMFAMILY("txt_nm_family", "姓", DataType.TEXT, 60, 0),
    /** 名 */
    NMNAME("txt_nm_name", "名", DataType.TEXT, 60, 0),
    /** 所属 */
    SZK("txt_szk", "所属", DataType.TEXT, 60, 0),

    // ユーザー履歴・アイテム履歴
    /** 期間（開始日） */
    FROM_DATE("txt_fromdate", "期間", DataType.DATE, 10, 0),
    /** 期間（終了日） */
    TO_DATE("txt_todate", "～", DataType.DATE, 10, 0),
    /** 商品名（漢字） */
    SHOHINKN("txt_shohinkn", "商品名（漢字）", DataType.ZEN, 40, 0),

    // Web商談
    /** 提案件名 */
    TEIAN("txt_teian", "提案件名", DataType.TEXT, 60, 0),
    /** 件名No */
    TEIANNO("txt_teian_no", "件名No", DataType.SUUJI, 9, 0),
    /** 取引先 */
    TORIHIKI("txt_torihiki", "取引先", DataType.SUUJI, 6, 0),
    /** 代表スキャニング */
    SCAN("txt_scan", "代表スキャニング", DataType.SUUJI, 13, 0),
    /** 発注先コード */
    HATYU("txt_hatyu", "発注先", DataType.SUUJI, 7, 0),
    /** 年月日FROM */
    YMD_F("txt_ymdf", "年月日", DataType.DECIMAL, 8, 0),
    /** 年月日TO */
    YMD_T("txt_ymdt", "年月日", DataType.DECIMAL, 8, 0),;

    private final String obj;
    private final String txt;
    private final DataType type;
    private final int digit1;
    private final int digit2;

    /** 初期化 */
    private InpText(String obj, String txt, DataType type, int digit1, int digit2) {
      this.obj = obj;
      this.txt = txt;
      this.type = type;
      this.digit1 = digit1;
      this.digit2 = digit2;
    }

    /** @return obj Object名 */
    @Override
    public String getObj() {
      return obj;
    }

    /** @return txt 表示名称 */
    @Override
    public String getTxt() {
      return txt;
    }

    /** @return txt データ型 */
    public DataType getType() {
      return type;
    }

    /** @return obj 桁数 */
    public int getDigit1() {
      return digit1;
    }

    /** @return txt 小数部桁数 */
    public int getDigit2() {
      return digit2;
    }

    /** @return len 桁数 */
    public int getLen() {
      return digit1 + digit2;
    }

    /** @return lbl ラベル */
    public String getLbl() {
      return txt + LBL_SUFFIX;
    }

    /** @return データ型タグ */
    public String getDatatypTag() {
      return type.getTypTag();
    }

    /** @return 最大値タグ */
    public String getMaxlenTag() {
      if (type.equals(DataType.DECIMAL)) {
        return getDatatypTag() + ",\"maxlen1\":" + (digit1 - digit2) + ",\"maxlen2\":" + digit2;
      } else {
        return getDatatypTag() + ",\"maxlen\":" + (digit1 + digit2);
      }
    }
  }

  /** HTML関連 ラベル */
  public enum Label {
    /** 分析期間 */
    KIKAN1("分析期間"),
    /** 比較期間 */
    KIKAN2("比較期間"),
    /** 特定日 */
    TKT_DT("特定日"),
    /** 基準日 */
    KJN_DT("基準日"),
    /** 開始年月 */
    OPN_YM("開店年月"),
    /** パスワード認証 */
    PASS("パスワード認証"),
    /** 登録 */
    REGIST("登録"),
    /** 詳細表示 */
    SYS("詳細"),

    /** プロンプト(商品コード) */
    PROMPT_SHNCD("____-____"),
    /** プロンプト(日付) */
    PROMPT_DT("__/__/__"),
    /** プロンプト(日付) */
    PROMPT_DT2("____/__/__"),
    /** プロンプト(日付＋週) */
    PROMPT_DTW("__/__/__(_)"),
    /** プロンプト(日付＋週) */
    PROMPT_DTW2("____/__/__(_)"),
    /** プロンプト(年月) */
    PROMPT_YM("__/__"),
    /** プロンプト(時分) */
    PROMPT_HHMM("__:__")

    /* Web商談 */
    /** 承認 */
    , SYONIN("承認")
    /** 却下 */
    , KYAKKA("却下")
    /** 適用開始日 */
    ,TEKIYOUBI("適用開始日");

    private final String txt;

    /** 初期化 */
    private Label(String txt) {
      this.txt = txt;
    }

    /** @return txt 表示名称 */
    public String getTxt() {
      return txt;
    }
  }

  /** HTML関連 チェックボックス */
  public enum Checkbox implements Item {
    /** 定義なし */
    NONE("", ""),
    /** 計表示 */
    KEI("ChkKei", "表示"),
    /** 既存店 */
    KIZ("ChkKiz", "既存店"),
    /** 単日 */
    TAN("ChkTan", "単日"),
    /** 累計 */
    RUI("ChkRui", "累計"),
    /** 定義保存の共有 */
    SHIORI("ChkShiori", "共有"),
    /** 詳細表示 */
    SYS("ChkSys", "詳細表示")

    /** 削除 */
    ,DEL("chk_del", "削除")
    /** 使用 */
    ,USEF("chk_use", "使用")
    /** 選択 */
    ,SEL("chk_sel", "選択")
    /** CSV対象 */
    ,CSV("chk_csv", "CSV対象")
    /** 削除 */
    ,UPD("chk_updkbn", "削除")

    /** 一時フラグ */
    ,KARIFLG("chk_kariflg", "臨時")
    /** ギフトコードフラグ */
    ,GIFTCD("chk_giftcd", "ギフトコード")

    /** 代表メーカー */
    ,DMAKERCD("chk_dmakercd", "代表メーカー")
    /** メーカー */
    ,MAKERCD("chk_makercd", "メーカー")
    /** 特別週 */
    ,TSHUFLG("chk_tshuflg", "特別週")
    /** 年末区分 */
    ,NENMATKBN("chk_nenmatkbn", "年末区分")
    /** アンケート本部ctl */
    ,HNCTLFLG("chk_hnctlflg", "アンケート本部ctl")
    /** 1日遅パターン有 */
    ,HBOKUREFLG("chk_hbokureflg", "1日遅パターン有")
    /** 月締 */
    ,GTSIMEFLG("chk_gtsimeflg", "月締")
    /** 事発リスト作成済 */
    ,JLSTCREFLG("chk_jlstcreflg", "事発リスト作成済")
    /** 店不採用禁止 */
    ,TPNG1FLG("chk_tpng1flg", "店不採用禁止")
    /** 店売価選択禁 */
    ,TPNG2FLG("chk_tpng2flg", "店売価選択禁")
    /** 店商品選択禁止 */
    ,TPNG3FLG("chk_tpng3flg", "店商品選択禁止")
    /** リーダー仮締 */
    ,SIMEFLG1_LD("chk_sime1flg", "リーダー仮締")
    /** リーダー本締 */
    ,SIMEFLG2_LD("chk_sime2flg", "リーダー本締")
    /** 各店締 */
    ,SIMEFLG_MB("chk_simeflg", "各店締")
    /** 強制フラグ */
    ,KYOSEIFLG("kyosei_flg", "強制フラグ")
    /** 採用区分 */
    ,MBSYFLG("mbsy_flg", "採用区分")
    /** 売価選択 */
    ,URISELKBN("urisel_flg", "売価選択")
    /** 通常開始 */
    ,HBSTRTFLG("hbstrt_flg", "通常開始")

    /** 臨時 */
    ,RINJI("chk_rinji", "臨時")

    /** 一日遅スライドしない-販売 */
    ,HBSLIDEFLG("chk_hbslideflg", "一日遅スライドしない-販売")
    /** 一日遅スライドしない-納入 */
    ,NHSLIDEFLG("chk_nhslideflg", "一日遅スライドしない-納入")
    /** 日替 */
    ,HIGAWRFLG("chk_higawrflg", "日替")
    /** チラシ未掲載 */
    ,CHIRASFLG("chk_chirasflg", "チラシ未掲載")
    /** 発注原売価適用しない */
    ,HTGENBAIKAFLG("chk_htgenbaikaflg", "発注原売価適用しない")
    /** PLU配信しない */
    ,PLUSNDFLG("chk_plusndflg", "PLU配信しない")
    /** よりどり */
    ,YORIFLG("chk_yoriflg", "よりどり")
    /** 生食・加熱 */
    ,NAMANETUKBN("chk_namanetukbn", "生食・加熱")
    /** 解凍 */
    ,KAITOFLG("chk_kaitoflg", "解凍")
    /** 養殖 */
    ,YOSHOKUFLG("chk_yoshokuflg", "養殖")
    /** 事前打出(チェック) */
    ,JUFLG("chk_juflg", "事前打出(チェック)")
    /** カット店展開しない */
    ,CUTTENFLG("chk_cuttenflg", "カット店展開しない")
    /** 週次伝送flg */
    ,SHUDENFLG("chk_shudenflg", "週次伝送flg")
    /** PC区分 */
    ,PCKBN("chk_pckbn", "PC区分")
    /** 販売日 */
    ,HBDT("chk_hbdt", "販売日")
    /** 納入日 */
    ,NNDT("chk_nndt", "納入日")
    /** 正規 */
    ,SEIKI("chk_seiki", "正規")
    /** 次週 */
    ,JISYU("chk_jisyu", "次週")

    /** 生鮮 */
    ,SEISEN("chk_seisen", "生鮮")
    /** 納入情報 */
    ,NNINFO("chk_nninfo", "納入情報")
    /** 販売情報 */
    ,HBINFO("chk_hbinfo", "販売情報")
    /** 検証-大分類 */
    ,KSDAIBRUI("chk_ksdaibrui", "検証-大分類")
    /** 検証-中分類 */
    ,KSCHUBRUI("chk_kschubrui", "検証-中分類")
    /** デフォルト_数展開-率P */
    ,DSUEXRTPTN("chk_dsuexrtptn", "デフォルト_数展開-率P")
    /** デフォルト_数展開-数P */
    ,DSUEXSUPTN("chk_dsuexsuptn", "デフォルト_数展開-数P")
    /** デフォルト_数展開-率P */
    ,DSUEXJRTPTN("chk_dsuexjrtptn", "デフォルト_数展開-実P")
    /** デフォルト_実績率パターン数値-売上 */
    ,DRTEXURI("chk_drtexuri", "デフォルト_実績率パターン数値-売上")
    /** デフォルト_実績率パターン数値-売上 */
    ,DRTEXTEN("chk_drtexten", "デフォルト_実績率パターン数値-点数")
    /** デフォルト_前年同週-大 */
    ,DZNENDSDAI("chk_dznendsdai", "デフォルト_前年同週-大")
    /** デフォルト_前年同週-中 */
    ,DZNENDSCHU("chk_dznendschu", "デフォルト_前年同週-中")
    /** デフォルト_同年同月-大 */
    ,DDNENDSDAI("chk_ddnendsdai", "デフォルト_同年同月-大")
    /** デフォルト_同年同月-中 */
    ,DDNENDSCHU("chk_ddnendschu", "デフォルト_同年同月-中")
    /** デフォルト_カット店展開 */
    ,DCUTEX("chk_dcutex", "デフォルト_カット店展開")
    /** デフォルト_ちらしのみ */
    ,DCHIRAS("chk_dchiras", "デフォルト_ちらしのみ")

    ;

    private final String obj;
    private final String txt;

    /** 初期化 */
    private Checkbox(String obj, String txt) {
      this.obj = obj;
      this.txt = txt;
    }

    /** @return obj Object名 */
    @Override
    public String getObj() {
      return obj;
    }

    /** @return txt 表示名称 */
    @Override
    public String getTxt() {
      return txt;
    }
  }

  /** HTML関連 ラジオボタン */
  public enum Radio {
    /** 選択 */
    SEL("rad_sel", "選択", ""),

    /** エリア区分 */
    AREAKBN("rad_areakbn", "エリア区分", ""),

    /** プライスカード */
    PCARDSZ1("rad_pcardsz1", "プライスカードサイズ", ""), PCARDSZ2("rad_pcardsz2", "プライスカードサイズ", ""), MAISUHOHOKB1("rad_maisuhohokb1", "枚数指定方法１", ""), MAISUHOHOKB2("rad_maisuhohokb2", "枚数指定方法２", ""),

    QASYUKBN("rad_qasyukbn", "アンケート種類", ""),
    /* 採用 */
    ADOPT("rad_adopt", "採用", ""),

    /* 総売価 */
    URIASELKBN("rad_auriaselkbn", "総売価", ""),

    BMTYP("rad_bmtyp", "B/Mタイプ", ""),

    /** ランクマスター */
    MSTKBN("rad_mstkbn", "マスター区分", ""), DATAKBN("rad_datakbn", "データ指定区分", ""), PTNNOKBN("rad_ptnnokbn", "パターンNo.区分", ""), JISSEKIBUN("rad_jissekibun", "実績分類", ""), WWMMFLG("rad_wwmmflg", "週月フラグ", ""),

    /** デフォルト_数展開 */
    DSUEXKBN("rad_dsuexkbn", "デフォルト_数展開", ""),
    /** デフォルト_実績率パタン数値 */
    DRTEXKBN("rad_drtexkbn", "デフォルト_実績率パタン数値", ""),
    /** デフォルト_前年同週 */
    DZNENDSKBN("rad_dznendskbn", "デフォルト_前年同週", ""),
    /** デフォルト_同年同月 */
    DDNENDSKBN("rad_ddnendskbn", "デフォルト_同年同月", ""),

    /** 展開方法 */
    TENKAIKBN("rad_tenkaikbn", "展開方法", ""),
    /** 実績率パタン数値 */
    JSKPTNSYUKBN("rad_jskptnsyukbn", "実績率パタン数値", ""),
    /** 実績率パタン前年同月 */
    JSKPTNZNENMKBN("rad_jskptnznenmkbn", "実績率パタン前年同月", ""),
    /** 実績率パタン前年同週 */
    JSKPTNZNENWKBN("rad_jskptnznenwkbn", "実績率パタン前年同週", ""),

    /** PLU商品・定貫商品 ／ 不定貫商品 */
    TKANPLUKBN("rad_tkanplukbn", "PLU商品・定貫商品　／　不定貫商品", "");

    private final String obj;
    private final String txt;
    private final String val;

    /** 初期化 */
    private Radio(String obj, String txt, String val) {
      this.obj = obj;
      this.txt = txt;
      this.val = val;
    }

    /** @return obj Object名 */
    public String getObj() {
      return obj;
    }

    /** @return txt 表示名称 */
    public String getTxt() {
      return txt;
    }

    /** @return val 値 */
    public String getVal() {
      return val;
    }
  }

  /** HTML関連 ラジオボタン(コード) */
  public enum RadCode {
    /** コード */
    NAME("rad_code", "コード", ""),
    /** 商品コード */
    ID1("rad_code1", "商品コード", "1"),
    /** ソースコード */
    ID2("rad_code2", "ソースコード", "2"),
    /** 販売コード */
    ID3("rad_code3", "販売コード", "3");
    ;

    private final String obj;
    private final String txt;
    private final String val;

    /** 初期化 */
    private RadCode(String obj, String txt, String val) {
      this.obj = obj;
      this.txt = txt;
      this.val = val;
    }

    /** @return obj Object名 */
    public String getObj() {
      return obj;
    }

    /** @return txt 表示名称 */
    public String getTxt() {
      return txt;
    }

    /** @return val 値 */
    public String getVal() {
      return val;
    }
  }

  /** HTML関連 グリッド */
  public enum Grid implements Item {
    /** 汎用 */
    SUB("grd_sub", ""),

    /** メーカー */
    MAKER("grd_maker", "メーカー"),
    /** 商品コード */
    SHNCD("grd_shncd", "商品コード"),
    /** 仕入先 */
    SIR("grd_sir", "仕入先"),
    /** 店グループ */
    TENGP("grd_tengp", "店グループ"),
    /** 店舗一覧 */
    TENPO("grd_tenpo", "店舗一覧"),
    /** 配送パターン */
    HSPTN("grd_hsptn", "配送パターン"),
    /** 配送グループ */
    HSGP("grd_hsgp", "配送グループ"),
    /** 配送店グループ */
    HSTNGP("grd_hstengp", "配送店グループ"),
    /** エリア別配送パターン */
    EHSPTN("grd_ehsptn", "エリア別配送パターン"),
    /** ソースコード */
    SRCCD("grd_srccd", "ソースコード"),
    /** アレルギー */
    ALLERGY("grd_allergy", "アレルギー"),
    /** 添加物 */
    TENKABUTSU("grd_tenkabutsu", "添加物"),
    /** 風袋選択リスト */
    FUTAI("grd_futai", "風袋"),
    /** 計量器風袋選択リスト */
    KRYOFUTAI("grd_kryofutai", "計量風袋"),
    /** グループ分類 */
    GROUP("grd_group", "グループ分類"),
    /** 実仕入先 */
    ZITSIR("grd_zitsir", "実仕入先"),
    /** 実仕入先 */
    FSTENPO("grd_fstenpo", "複数仕入先店舗"),
    /** 催しコード_レギュラー */
    MOYCD_R("grd_moycd_r", "催しコード_レギュラー"),
    /** 催しコード_スポット */
    MOYCD_S("grd_moycd_s", "催しコード_スポット"),
    /** 催しコード_特売 */
    MOYCD_T("grd_moycd_t", "催しコード_特売"),
    /** 店舗名 */
    TENPO_M("grd_tenpo_m", "店舗名"),
    /** 配送店グループ */
    HSTGP("grd_hstgp", "配送店グループ"),
    /** 配送グループ店 */
    HSGPT("grd_gpten", "配送グループ店"),
    /** 商品一覧(予約発注) */
    SHOHIN("grd_shohin", "商品一覧"),
    /** 納品日一覧(予約発注) */
    NOHIN("grd_nohin", "納品日一覧"),
    /** 店舗一覧(予約発注) */
    TENPO＿YH("grd_tenpo_yh", "店舗一覧"),
    /** 店舗一覧(新店改装発注) */
    TENPO＿SK("grd_tenpo_sk", "店舗一覧"),
    /** プライスカード発行枚数一覧 */
    PCARDSU("grd_pcardsu", "プライスカード発行枚数一覧"),
    /** コースマスタ */
    COURSEMT("grd_coursemt", "コースマスタ"),
    /** B/M番号一覧 */
    BMNNO("grd_bmnno", "B/M番号一覧"),
    /** セット番号一覧 */
    SETNO("grd_setno", "セット番号一覧"),
    /** B/M催し送信商品一覧 */
    BMSHN("grd_bmshn", "B/M催し送信商品一覧"),
    /** 店確認 */
    ADTEN("grd_adten", "店確認"),
    /** 店同一数量発注入力 */
    TENHTSU_ARR("grd_tenhtsu_arr", "店同一数量発注入力"),
    /** 正規定量 */
    HATSTRSHNTEN("grd_hatstrshnten", "正規定量商品店一覧"),
    /** ランク№ */
    RANKNO("grd_rankno", "ランク№"),
    /** 部門予算 */
    BUMONYOSAN("grd_bumonyosan", "部門予算"),
    /** 計画計 */
    KEIKAKU("grd_keikaku", "計画計"),

    /**
     * 販促 他画面参照 通常画面をサブ画面として使用する場合は、オブジェクト名を[grd_subwindow_***]の形にする。
     */
    /** 共通名称 */
    SUBWINDOW("grd_subwindow", "共通名称"),
    /** 対象店確認 */
    TENKAKUNIN("grd_subwindow_tenkakunin", "店確認"),
    /** 臨時ランク確認 */
    RINZIRANKNO("grd_subwindow_rinzirank", "臨時ランク確認"),
    /** ランク確認 */
    SUBWINDOWTENIFO("grd_subwindow_teninfo", "ランク確認"),
    /** ランク確認 */
    SUBWINDOWRANKNO("grd_subwindow_rank", "ランク確認"),
    /** 店情報 */
    TENINFO("grd_teninfo", "店情報"),
    /** 店番一括入力 */
    TENCDIINPUT("grd_tencdiinput", "店番一括入力"),
    /** 店別数量展開 */
    TENBETUSU("grd_tenbetusu", "店別数量展開"),
    /** 実績率パターン店別分配率 */
    JRTPTNTENBETUBRT("grd_jrtptntenbetubrt", "実績率パターン店別分配率"),
    /** 通常率パターン店別分配率 */
    RTPTNTENBETUBRT("grd_rtptntenbetubrt", "通常率パターン店別分配率"),
    /** ランク別数量 */
    RANKSURYO("grd_ranksuryo", "ランク別数量"),
    /** 数量 */
    SURYO("grd_subwindow_suryo", "数量パターン"),
    /** 数値展開方法 */
    TENKAI("grd_tenkai", "数値展開方法"),
    /** 対象店確認 */
    RANKTENINFO("grd_subwindow_runkTenTnfo", "ランク店情報"),
    /** 対象店確認 */
    SHNCDSUB("grd_subwindow_shncd", "店確認"),
    /** 使用原料 */
    MSTUGENRYO("grd_genryo", "使用原料"),
    /** 対象店確認 */
    ZITREF("grd_subwindow_zitref", "実績参照"),
    /** セット販売 */
    SET("grd_set", "セット販売"),
    /** セット販売 */
    SET2("grd_set2", "セット販売2"),
    /** 分類明細 */
    SEL_VIEW("grd_subwindow_sel_view", "分類明細"),
    /** 部門予算 */
    BUMONYOSAN_SUB("grd_subwindow_bumonyosan", "部門予算"),
    /** 構成比 */
    KOUSEIHI("grd_subwindow_kouseihi", "構成比");

    private final String obj;
    private final String txt;

    /** 初期化 */
    private Grid(String obj, String txt) {
      this.obj = obj;
      this.txt = txt;
    }

    /** @return obj Object名 */
    @Override
    public String getObj() {
      return obj;
    }

    /** @return txt 表示名称 */
    @Override
    public String getTxt() {
      return txt;
    }
  }

  /** HTML関連 遷移時更新データ保持 */
  public enum UpdateId implements Item {
    /** 大分類マスタ */
    Reportx031("targetRows_x031", "大分類マスタ"),
    /** 中分類マスタ */
    Reportx032("targetRows_x032", "中分類マスタ"),
    /** 小分類マスタ */
    Reportx033("targetRows_x033", "小分類マスタ"),
    /** 分類マスタ */
    Reportx034("targetRows_x034", "小小分類マスタ");

    private final String obj;
    private final String txt;

    /** 初期化 */
    private UpdateId(String obj, String txt) {
      this.obj = obj;
      this.txt = txt;
    }

    /** @return obj Object名 */
    @Override
    public String getObj() {
      return obj;
    }

    /** @return txt 表示名称 */
    @Override
    public String getTxt() {
      return txt;
    }
  }

  /** Download関連 */
  public enum Download {
    /** CSV出力 */
    PAGE_X001("商品一覧画面出力CSV"), PAGE_X250("仕掛商品検索画面出力CSV");

    private final String txt;

    /** 初期化 */
    private Download(String txt) {
      this.txt = txt;
    }

    /** @return txt 表示名称 */
    public String getTxt() {
      return txt;
    }
  }

  /** 利用時間外チェック用 システム権限定義 */
  public enum systemPosition {

    /** 本部マスタ */
    POS_HM("24321"),
    /** 本部特売 */
    POS_HK("24320"),
    /** 店舗特売 */
    POS_TK("24322");

    private final String val;

    /** 初期化 */
    private systemPosition(String txt) {
      this.val = txt;
    }

    /** @return txt 表示名称 */
    public String getVal() {
      return val;
    }
  }

  /** 利用時間外チェック用 システム権限定義 */
  public enum systemGroups {

    /** システム */
    SYSTEM("-2"),
    /** イートン */
    EATONE("-1"),
    /** いなげや */
    INAGEYA("1");

    private final String val;

    /** 初期化 */
    private systemGroups(String txt) {
      this.val = txt;
    }

    /** @return txt 表示名称 */
    public String getVal() {
      return val;
    }
  }

  /** HTML関連 選択リストオプション */
  public interface Option {
    public String getVal();

    public String getTxt();
  }

  public static String getOptions(Option[] enumCls) {
    return getOptions(enumCls, new String[0]);
  }

  public static String getOptions(Option[] enumCls, String... excludeValues) {
    String rtn = "";
    for (Option opt : enumCls) {
      // 除外するリストに含まれていた場合、リストに追加しない
      if (ArrayUtils.contains(excludeValues, opt.getVal())) {
        continue;
      }
      rtn += "<option value=\"" + opt.getVal() + "\">" + opt.getTxt() + "</option>";
    }
    return rtn;
  }

  public static String getOptionDatas(Option[] enumCls) {
    String rtn = "";
    for (Option opt : enumCls) {
      rtn += "{VALUE:'" + opt.getVal() + "',TEXT:'" + opt.getTxt() + "'},";
    }
    return "data:[" + StringUtils.removeEnd(rtn, ",") + "]";
  }

  public static Option getOptionFromText(Option[] enumCls, String text) {
    for (Option opt : enumCls) {
      if (StringUtils.equals(text, opt.getTxt())) {
        return opt;
      }
    }
    return null;
  }

  public static Option getOptionFromValue(Option[] enumCls, String value) {
    for (Option opt : enumCls) {
      if (StringUtils.equals(value, opt.getVal())) {
        return opt;
      }
    }
    return null;
  }

  /** HTML関連 選択リストオプション(期間-月週日) */
  public enum OptionKikan implements Option {
    /** 日 */
    DAY("3", "日"),
    /** 週 */
    WEEK("2", "週"),
    /** 月 */
    MONTH("1", "月");

    private final String val;
    private final String txt;

    /** 初期化 */
    private OptionKikan(String val, String txt) {
      this.val = val;
      this.txt = txt;
    }

    /** @return val 値 */
    @Override
    public String getVal() {
      return val;
    }

    /** @return txt 表示名称 */
    @Override
    public String getTxt() {
      return txt;
    }
  }

  /** HTML関連 選択リストオプション(期間-月日) */
  public enum OptionKikanMD implements Option {
    /** 日 */
    DAY("3", "日"),
    /** 月 */
    MONTH("1", "月");

    private final String val;
    private final String txt;

    /** 初期化 */
    private OptionKikanMD(String val, String txt) {
      this.val = val;
      this.txt = txt;
    }

    /** @return val 値 */
    @Override
    public String getVal() {
      return val;
    }

    /** @return txt 表示名称 */
    @Override
    public String getTxt() {
      return txt;
    }
  }

  /** HTML関連 選択リストオプション(期間-年度月週日) */
  public enum OptionKikanYMWD implements Option {
    /** 日 */
    DAY("3", "日"),
    /** 週 */
    WEEK("2", "週"),
    /** 月 */
    MONTH("1", "月"),
    /** 年度 */
    FYEAR("4", "年度");

    private final String val;
    private final String txt;

    /** 初期化 */
    private OptionKikanYMWD(String val, String txt) {
      this.val = val;
      this.txt = txt;
    }

    /** @return val 値 */
    @Override
    public String getVal() {
      return val;
    }

    /** @return txt 表示名称 */
    @Override
    public String getTxt() {
      return txt;
    }
  }

  /** HTML関連 選択リストオプション(期間-年度月) */
  public enum OptionKikanYM implements Option {
    /** 月 */
    MONTH("1", "月"),
    /** 年度 */
    FYEAR("4", "年度");

    private final String val;
    private final String txt;

    /** 初期化 */
    private OptionKikanYM(String val, String txt) {
      this.val = val;
      this.txt = txt;
    }

    /** @return val 値 */
    @Override
    public String getVal() {
      return val;
    }

    /** @return txt 表示名称 */
    @Override
    public String getTxt() {
      return txt;
    }
  }

  /** HTML関連 選択リストオプション(期間-月) */
  public enum OptionKikanM implements Option {
    /** 月 */
    MONTH("1", "月");

    private final String val;
    private final String txt;

    /** 初期化 */
    private OptionKikanM(String val, String txt) {
      this.val = val;
      this.txt = txt;
    }

    /** @return val 値 */
    @Override
    public String getVal() {
      return val;
    }

    /** @return txt 表示名称 */
    @Override
    public String getTxt() {
      return txt;
    }
  }

  /** HTML関連 選択リストオプション(単累) */
  public enum OptionTanRui implements Option {
    /** 単日 */
    TAN("1", "単日"),
    /** 累計 */
    RUI("2", "累計");

    private final String val;
    private final String txt;

    /** 初期化 */
    private OptionTanRui(String val, String txt) {
      this.val = val;
      this.txt = txt;
    }

    /** @return val 値 */
    @Override
    public String getVal() {
      return val;
    }

    /** @return txt 表示名称 */
    @Override
    public String getTxt() {
      return txt;
    }
  }

  /** HTML関連 選択リストオプション(定義保存-共有) */
  public enum OptionSioriKubun implements Option {
    /** 個人 */
    MYSELF("0", "　"),
    /** 部署 */
    SECTION("2", "部署"),
    /** 全社 */
    COMPANY("1", "全社");

    private final String val;
    private final String txt;

    /** 初期化 */
    private OptionSioriKubun(String val, String txt) {
      this.val = val;
      this.txt = txt;
    }

    /** @return val 値 */
    @Override
    public String getVal() {
      return val;
    }

    /** @return txt 表示名称 */
    @Override
    public String getTxt() {
      return txt;
    }
  }

  /** HTML関連 選択リストオプション(定義保存-共有) */
  public enum OptionBunruiKubun implements Option {
    /** 個人 */
    HYOJUN("1", "標準"),
    /** 部署 */
    YOUTO("2", "用途"),
    /** 全社 */
    URIBA("3", "売場");

    private final String val;
    private final String txt;

    /** 初期化 */
    private OptionBunruiKubun(String val, String txt) {
      this.val = val;
      this.txt = txt;
    }

    /** @return val 値 */
    @Override
    public String getVal() {
      return val;
    }

    /** @return txt 表示名称 */
    @Override
    public String getTxt() {
      return txt;
    }
  }

  /** 固定値定義 */
  public enum Values implements Option {
    /** 選択値空白 */
    NONE("-1", ""),

    /** すべて */
    ALL("-1", "すべて"),

    /** 全て */
    ALLKAN("-1", "全て"),

    /** チェック ON */
    ON("1", "✔"),
    /** チェック OFF */
    OFF("0", ""),

    /** 合計 */
    SUM("-1", "合計"),
    /** 平均 */
    AVG("-2", "平均"),

    /** 全部門 */
    BUMON_ALL_TOK("-1", "99-全部門"),

    /** 全店計 */
    TENPO_ALL("-99", "全店計"),
    /** 新店計 */
    TENPO_NEW("-98", "新店計"),
    /** 既存店計 */
    TENPO_EX("-97", "既存店小計"),
    /** 店舗別 */
    TENPO_EACH("-96", "店舗別"),

    /** 店舗グループ:販売統括部 */
    TENPO_G_HAN("-89", "販売統括部"),
    /** 店舗グループ:青果市場 */
    TENPO_G_SEI("-88", "青果市場"),
    /** 店舗グループ:ボンマタン */
    TENPO_G_BON("-87", "ボンマタン"),

    /** 送信フラグ：未送信 */
    SENDFLG_UN("0", "未送信")

    ;

    private final String val;
    private final String txt;

    /** 初期化 */
    private Values(String val, String txt) {
      this.val = val;
      this.txt = txt;
    }

    /** @return val 値 */
    @Override
    public String getVal() {
      return val;
    }

    /** @return txt 表示名称 */
    @Override
    public String getTxt() {
      return txt;
    }
  }

  /**
   * 固定値定義（表側、表列項目一覧）<br>
   * VAL0:なし<br>
   * VAL1:時系列<br>
   * VAL2:部門グループ<br>
   * VAL3:部門<br>
   * VAL4:大分類<br>
   * VAL5:中分類<br>
   * VAL6:小分類<br>
   * VAL7:商品<br>
   * VAL8:販売統括部<br>
   * VAL9:販売部<br>
   * VAL10:店舗<br>
   * VAL11:市場<br>
   * VAL12:仕入先<br>
   * VAL13:メーカー<br>
   * VAL14:表示項目<br>
   * VAL15:勘定科目<br>
   * VAL16:ランキング<br>
   * VAL17:構成頁<br>
   * VAL18:レジ番号<br>
   * VAL19:催し区分<br>
   * VAL20:時間帯<br>
   * VAL21:当月/累計<br>
   * VAL22:所属<br>
   * VAL23:職種<br>
   */
  public enum ValHyo implements Option {
    /** なし */
    VAL0("0", "なし"),
    /** 時系列 */
    VAL1("1", "時系列"),
    /** 部門グループ */
    VAL2("2", "部門グループ"),
    /** 部門 */
    VAL3("3", "部門"),
    /** 大分類 */
    VAL4("4", "大分類"),
    /** 中分類 */
    VAL5("5", "中分類"),
    /** 小分類 */
    VAL6("6", "小分類"),
    /** 商品 */
    VAL7("7", "商品"),
    /** 販売統括部 */
    VAL8("8", "販売統括部"),
    /** 販売部 */
    VAL9("9", "販売部"),
    /** 店舗 */
    VAL10("10", "店舗"),
    /** 市場 */
    VAL11("11", "市場"),
    /** 仕入先 */
    VAL12("12", "仕入先"),
    /** メーカー */
    VAL13("13", "メーカー"),
    /** 表示項目 */
    VAL14("14", "表示項目"),
    /** 勘定科目 */
    VAL15("15", "勘定科目"),
    /** ランキング */
    VAL16("16", "ランキング"),
    /** 構成頁 */
    VAL17("17", "構成頁"),
    /** レジ番号 */
    VAL18("18", "レジ番号"),
    /** 催し区分 */
    VAL19("19", "催し区分"),
    /** 時間帯 */
    VAL20("20", "時間帯"),
    /** 当月/累計 */
    VAL21("21", "当月/累計"),
    /** 所属 */
    VAL22("22", "所属"),
    /** 職種 */
    VAL23("23", "職種"),
    /** 週別 */
    VAL24("24", "週別"),
    /** 曜日別 */
    VAL25("25", "曜日別"),
    /** ランク基準 */
    VAL26("26", "ランク基準"),
    /** 伸び率評価基準 */
    VAL27("27", "伸び率評価基準"),
    /** 販売区分 */
    VAL28("28", "販売区分"),
    /** 伝票明細 */
    VAL29("29", "伝票明細"),
    /** 本部 */
    VAL30("30", "本部"),
    /** 統括部門 */
    VAL31("31", "統括部門"),
    /** 単日累計 */
    VAL32("32", "単日累計"),
    /** 表示項目（横） */
    VAL33("33", "表示項目（横）"),
    /** 中計 */
    VAL34("34", "中計"),
    /** 店舗・構成頁 */
    VAL35("35", "店舗・構成頁"),
    /** 商品区分 */
    VAL36("36", "商品区分"),
    /** 曜日 */
    VAL37("37", "曜日"),
    /** 部門別既存店 */
    VAL38("38", "部門別既存店");

    private final String val;
    private final String txt;

    /** 初期化 */
    private ValHyo(String val, String txt) {
      this.val = val;
      this.txt = txt;
    }

    /** @return val 値 */
    @Override
    public String getVal() {
      return val;
    }

    /** @return txt 表示名称 */
    @Override
    public String getTxt() {
      return txt;
    }
  }

  /**
   * 固定値定義（既存店区分）<br>
   */
  public enum ValKizonKbn implements Option {
    /** 新店 */
    NEW("1", "新店"),
    /** 既存店 */
    KIZON("2", "既存店");

    private final String val;
    private final String txt;

    /** 初期化 */
    private ValKizonKbn(String val, String txt) {
      this.val = val;
      this.txt = txt;
    }

    /** @return val 値 */
    @Override
    public String getVal() {
      return val;
    }

    /** @return txt 表示名称 */
    @Override
    public String getTxt() {
      return txt;
    }
  }

  /**
   * 固定値定義（更新区分）<br>
   */
  public enum ValUpdkbn implements Option {
    /** 通常 */
    NML("0", "通常"),
    /** 削除 */
    DEL("1", "削除");

    private final String val;
    private final String txt;

    /** 初期化 */
    private ValUpdkbn(String val, String txt) {
      this.val = val;
      this.txt = txt;
    }

    /** @return val 値 */
    @Override
    public String getVal() {
      return val;
    }

    /** @return txt 表示名称 */
    @Override
    public String getTxt() {
      return txt;
    }
  }

  /**
   * 固定値定義（CSV更新区分）<br>
   */
  public enum ValCsvUpdkbn implements Option {
    /** TODO:新規の区分は？ */
    NEW("A", "新規"),
    /** 変更 */
    UPD("U", "変更"),
    /** 削除 */
    DEL("X", "削除"),
    /** 予約 */
    YYK("Y", "予約"),
    /** 予約取消 */
    YDEL("D", "予約取消");

    private final String val;
    private final String txt;

    /** 初期化 */
    private ValCsvUpdkbn(String val, String txt) {
      this.val = val;
      this.txt = txt;
    }

    /** @return val 値 */
    @Override
    public String getVal() {
      return val;
    }

    /** @return txt 表示名称 */
    @Override
    public String getTxt() {
      return txt;
    }
  }

  /**
   * 固定値定義（ファイル更新区分）<br>
   */
  public enum ValFileUpdkbn implements Option {
    /** 新規 */
    NEW("A", "新規"),
    /** 変更 */
    UPD("U", "変更"),
    /** 削除 */
    DEL("X", "削除"),
    /** 予約 */
    YYK("Y", "予約"),
    /** 予約取消 */
    YDEL("D", "予約取消");

    private final String val;
    private final String txt;

    /** 初期化 */
    private ValFileUpdkbn(String val, String txt) {
      this.val = val;
      this.txt = txt;
    }

    /** @return val 値 */
    @Override
    public String getVal() {
      return val;
    }

    /** @return txt 表示名称 */
    @Override
    public String getTxt() {
      return txt;
    }
  }

  /** 固定値定義（区分:PC区分） */
  public enum ValKbn102 implements Option {
    /** 通常商品 */
    VAL0("0", "通常商品"),
    /** ＰＣ商品 */
    VAL1("1", "ＰＣ商品");

    private final String val;
    private final String txt;

    /** 初期化 */
    private ValKbn102(String val, String txt) {
      this.val = val;
      this.txt = txt;
    }

    /** @return val 値 */
    @Override
    public String getVal() {
      return val;
    }

    /** @return txt 表示名称 */
    @Override
    public String getTxt() {
      return txt;
    }
  }

  /** 固定値定義（区分:商品種類） */
  public enum ValKbn105 implements Option {
    /** 通常商品 */
    VAL0("0", "通常商品"),
    /** 原材料 */
    VAL1("1", "原材料"),
    /** 包材 */
    VAL2("2", "包材"),
    /** 消耗品 */
    VAL3("3", "消耗品"),
    /** 催事テナントコメント */
    VAL4("4", "催事テナントコメント"),
    /** 分類割引ダミー */
    VAL5("5", "分類割引ダミー"),
    /** ラックジョバー */
    VAL6("6", "ラックジョバー");

    private final String val;
    private final String txt;

    /** 初期化 */
    private ValKbn105(String val, String txt) {
      this.val = val;
      this.txt = txt;
    }

    /** @return val 値 */
    @Override
    public String getVal() {
      return val;
    }

    /** @return txt 表示名称 */
    @Override
    public String getTxt() {
      return txt;
    }
  }

  /**
   * 固定値定義（区分:定計区分）<br>
   */
  public enum ValKbn117 implements Option {
    /** ＰＬＵ */
    VAL0("0", "ＰＬＵ"),
    /** ＮＯＮ−ＰＬＵ */
    VAL1("1", "ＮＯＮ−ＰＬＵ");

    private final String val;
    private final String txt;

    /** 初期化 */
    private ValKbn117(String val, String txt) {
      this.val = val;
      this.txt = txt;
    }

    /** @return val 値 */
    @Override
    public String getVal() {
      return val;
    }

    /** @return txt 表示名称 */
    @Override
    public String getTxt() {
      return txt;
    }
  }

  /**
   * 固定値定義（区分:税区分）<br>
   */
  public enum ValKbn120 implements Option {
    /** 外税 */
    VAL0("0", "外税"),
    /** 内税 */
    VAL1("1", "内税"),
    /** 非課税 */
    VAL2("2", "非課税"),
    /** 部門マスタに準拠 */
    VAL3("3", "部門マスタに準拠");

    private final String val;
    private final String txt;

    /** 初期化 */
    private ValKbn120(String val, String txt) {
      this.val = val;
      this.txt = txt;
    }

    /** @return val 値 */
    @Override
    public String getVal() {
      return val;
    }

    /** @return txt 表示名称 */
    @Override
    public String getTxt() {
      return txt;
    }
  }

  /** 固定値定義（区分:定貫不定貫区分） */
  public enum ValKbn121 implements Option {
    /** 不定貫 */
    VAL0("0", "不定貫"),
    /** 定貫 */
    VAL1("1", "定貫");

    private final String val;
    private final String txt;

    /** 初期化 */
    private ValKbn121(String val, String txt) {
      this.val = val;
      this.txt = txt;
    }

    /** @return val 値 */
    @Override
    public String getVal() {
      return val;
    }

    /** @return txt 表示名称 */
    @Override
    public String getTxt() {
      return txt;
    }
  }

  /** 固定値定義（区分:便区分） */
  public enum ValKbn132 implements Option {
    /** １便 */
    VAL1("1", "１便"),
    /** ２便 */
    VAL2("2", "２便");

    private final String val;
    private final String txt;

    /** 初期化 */
    private ValKbn132(String val, String txt) {
      this.val = val;
      this.txt = txt;
    }

    /** @return val 値 */
    @Override
    public String getVal() {
      return val;
    }

    /** @return txt 表示名称 */
    @Override
    public String getTxt() {
      return txt;
    }
  }

  /** 固定値定義（区分:締め回数） */
  public enum ValKbn134 implements Option {
    /** １回締め */
    VAL1("1", "１回締め"),
    /** ２回締め */
    VAL2("2", "２回締め");

    private final String val;
    private final String txt;

    /** 初期化 */
    private ValKbn134(String val, String txt) {
      this.val = val;
      this.txt = txt;
    }

    /** @return val 値 */
    @Override
    public String getVal() {
      return val;
    }

    /** @return txt 表示名称 */
    @Override
    public String getTxt() {
      return txt;
    }
  }

  /** 固定値定義（区分:エリア区分） */
  public enum ValKbn135 implements Option {
    /** エリア */
    VAL0("0", "エリア"),
    /** 店グル−プ */
    VAL1("1", "店グル−プ");

    private final String val;
    private final String txt;

    /** 初期化 */
    private ValKbn135(String val, String txt) {
      this.val = val;
      this.txt = txt;
    }

    /** @return val 値 */
    @Override
    public String getVal() {
      return val;
    }

    /** @return txt 表示名称 */
    @Override
    public String getTxt() {
      return txt;
    }
  }

  /** 固定値定義（区分:衣料使い回しフラグ） */
  public enum ValKbn142 implements Option {
    /** 通常 */
    VAL0("0", "通常"),
    /** 使い回し */
    VAL1("1", "使い回し");

    private final String val;
    private final String txt;

    /** 初期化 */
    private ValKbn142(String val, String txt) {
      this.val = val;
      this.txt = txt;
    }

    /** @return val 値 */
    @Override
    public String getVal() {
      return val;
    }

    /** @return txt 表示名称 */
    @Override
    public String getTxt() {
      return txt;
    }
  }

  /**
   * 固定値定義（区分:桁指定）<br>
   */
  public enum ValKbn143 implements Option {
    /** 入力なし */
    VAL0("0", "入力なし"),
    /** ８桁入力 */
    VAL1("1", "８桁入力"),
    /** ４桁入力 */
    VAL2("2", "４桁入力");

    private final String val;
    private final String txt;

    /** 初期化 */
    private ValKbn143(String val, String txt) {
      this.val = val;
      this.txt = txt;
    }

    /** @return val 値 */
    @Override
    public String getVal() {
      return val;
    }

    /** @return txt 表示名称 */
    @Override
    public String getTxt() {
      return txt;
    }
  }

  /** 固定値定義（区分:評価方法区分） */
  public enum ValKbn504 implements Option {
    /** 棚卸対象外 */
    VAL0("0", "棚卸対象外"),
    /** 最終仕入原価法 */
    VAL1("1", "最終仕入原価法"),
    /** 売価還元法 */
    VAL2("2", "売価還元法"),
    /** 総平均法 */
    VAL3("3", "総平均法");

    private final String val;
    private final String txt;

    /** 初期化 */
    private ValKbn504(String val, String txt) {
      this.val = val;
      this.txt = txt;
    }

    /** @return val 値 */
    @Override
    public String getVal() {
      return val;
    }

    /** @return txt 表示名称 */
    @Override
    public String getTxt() {
      return txt;
    }
  }

  /** 固定値定義（区分:催し区分） */
  public enum ValKbn10002 implements Option {
    /** レギュラー */
    VAL0("0", "レギュラー"),
    /** 全店特売 */
    VAL1("1", "全店特売"),
    /** 週間山積 */
    VAL2("2", "週間山積"),
    /** 個店特売 */
    VAL3("3", "個店特売"),
    /** 個店特売 */
    VAL4("4", "個店特売"),
    /** 生活応援 */
    VAL5("5", "生活応援"),
    /** 月間山積 */
    VAL7("7", "月間山積"),
    /** 店舗アンケート付き送付け */
    VAL8("8", "店舗アンケート付き送付け"),
    /** 事前打出し */
    VAL9("9", "事前打出し");

    private final String val;
    private final String txt;

    /** 初期化 */
    private ValKbn10002(String val, String txt) {
      this.val = val;
      this.txt = txt;
    }

    /** @return val 値 */
    @Override
    public String getVal() {
      return val;
    }

    /** @return txt 表示名称 */
    @Override
    public String getTxt() {
      return txt;
    }
  }

  /** 固定値定義（区分:デフォルト_数展開） */
  public enum ValKbn10007 implements Option {
    /** 通常率パターン */
    VAL1("1", "通常率パターン"),
    /** 数量パターン */
    VAL2("2", "数量パターン"),
    /** 実績率パターン */
    VAL3("3", "実績率パターン");

    private final String val;
    private final String txt;

    /** 初期化 */
    private ValKbn10007(String val, String txt) {
      this.val = val;
      this.txt = txt;
    }

    /** @return val 値 */
    @Override
    public String getVal() {
      return val;
    }

    /** @return txt 表示名称 */
    @Override
    public String getTxt() {
      return txt;
    }
  }

  /** 固定値定義（区分:デフォルト_実績率パタン数値） */
  public enum ValKbn10008 implements Option {
    /** なし */
    VAL0("0", "なし"),
    /** 売上 */
    VAL1("1", "売上"),
    /** 点数 */
    VAL2("2", "点数");

    private final String val;
    private final String txt;

    /** 初期化 */
    private ValKbn10008(String val, String txt) {
      this.val = val;
      this.txt = txt;
    }

    /** @return val 値 */
    @Override
    public String getVal() {
      return val;
    }

    /** @return txt 表示名称 */
    @Override
    public String getTxt() {
      return txt;
    }
  }

  /** 固定値定義（区分:デフォルト_前年期間） */
  public enum ValKbn10009 implements Option {
    /** なし */
    VAL0("0", "なし"),
    /** 大 */
    VAL1("1", "大"),
    /** 中 */
    VAL2("2", "中"),
    /** 部門 */
    VAL3("3", "部");

    private final String val;
    private final String txt;

    /** 初期化 */
    private ValKbn10009(String val, String txt) {
      this.val = val;
      this.txt = txt;
    }

    /** @return val 値 */
    @Override
    public String getVal() {
      return val;
    }

    /** @return txt 表示名称 */
    @Override
    public String getTxt() {
      return txt;
    }
  }

  /** 固定値定義（区分:強制グループフラグ） */
  public enum ValKbn10610 implements Option {
    /** 通常 */
    VAL0("0", "通常"),
    /** 強制 */
    VAL1("1", "強制");

    private final String val;
    private final String txt;

    /** 初期化 */
    private ValKbn10610(String val, String txt) {
      this.val = val;
      this.txt = txt;
    }

    /** @return val 値 */
    @Override
    public String getVal() {
      return val;
    }

    /** @return txt 表示名称 */
    @Override
    public String getTxt() {
      return txt;
    }
  }

  /** 固定値定義（区分:アンケート種類） */
  public enum ValKbn10611 implements Option {
    /** 売価選択（一括） */
    VAL1("1", "売価選択（一括）"),
    /** 売価選択(商品別) */
    VAL2("2", "売価選択(商品別)"),
    /** 売価差替 */
    VAL3("3", "売価差替"),
    /** 売価・商品選択 */
    VAL4("4", "売価・商品選択"),
    /** 不参加 */
    VAL5("5", "不参加");

    private final String val;
    private final String txt;

    /** 初期化 */
    private ValKbn10611(String val, String txt) {
      this.val = val;
      this.txt = txt;
    }

    /** @return val 値 */
    @Override
    public String getVal() {
      return val;
    }

    /** @return txt 表示名称 */
    @Override
    public String getTxt() {
      return txt;
    }
  }

  /** 固定値定義（区分:定貫PLU・不定貫区分） */
  public enum ValKbn10414 implements Option {
    /** 定貫ＰＬＵ */
    VAL1("1", "定貫ＰＬＵ"),
    /** 不定貫 */
    VAL2("2", "不定貫");

    private final String val;
    private final String txt;

    /** 初期化 */
    private ValKbn10414(String val, String txt) {
      this.val = val;
      this.txt = txt;
    }

    /** @return val 値 */
    @Override
    public String getVal() {
      return val;
    }

    /** @return txt 表示名称 */
    @Override
    public String getTxt() {
      return txt;
    }
  }

  /** 固定値定義（区分:生食加熱区分） */
  public enum ValKbn10411 implements Option {
    /** 生食 */
    VAL1("1", "生食"),
    /** 加熱 */
    VAL2("2", "加熱");

    private final String val;
    private final String txt;

    /** 初期化 */
    private ValKbn10411(String val, String txt) {
      this.val = val;
      this.txt = txt;
    }

    /** @return val 値 */
    @Override
    public String getVal() {
      return val;
    }

    /** @return txt 表示名称 */
    @Override
    public String getTxt() {
      return txt;
    }
  }

  /** 固定値定義（区分:登録種別区分） */
  public enum ValAddShuKbn implements Option {
    /** 全品割引 */
    VAL1("1", "全品割引"),
    /** ドライ */
    VAL2("2", "ドライ"),
    /** 青果 */
    VAL3("3", "青果"),
    /** 鮮魚 */
    VAL4("4", "鮮魚"),
    /** 生肉 */
    VAL5("5", "生肉");

    private final String val;
    private final String txt;

    /** 初期化 */
    private ValAddShuKbn(String val, String txt) {
      this.val = val;
      this.txt = txt;
    }

    /** @return val 値 */
    @Override
    public String getVal() {
      return val;
    }

    /** @return txt 表示名称 */
    @Override
    public String getTxt() {
      return txt;
    }
  }

  /**
   * 固定値定義（グループ区分）<br>
   */
  public enum ValGpkbn implements Option {
    /** 仕入グループ */
    SIR("1", "仕入グループ"),
    /** 売価グループ */
    BAIKA("2", "売価グループ"),
    /** 品揃グループ */
    SHINA("3", "品揃グループ"),
    /** 店別異部門 */
    TBMN("4", "店別異部門");

    private final String val;
    private final String txt;

    /** 初期化 */
    private ValGpkbn(String val, String txt) {
      this.val = val;
      this.txt = txt;
    }

    /** @return val 値 */
    @Override
    public String getVal() {
      return val;
    }

    /** @return txt 表示名称 */
    @Override
    public String getTxt() {
      return txt;
    }
  }

  /**
   * 固定値定義（添加物区分）<br>
   */
  public enum ValTenkabkbn implements Option {
    /** 添加物 */
    VAL1("1", "添加物"),
    /** アレルギー */
    VAL2("2", "アレルギー");

    private final String val;
    private final String txt;

    /** 初期化 */
    private ValTenkabkbn(String val, String txt) {
      this.val = val;
      this.txt = txt;
    }

    /** @return val 値 */
    @Override
    public String getVal() {
      return val;
    }

    /** @return txt 表示名称 */
    @Override
    public String getTxt() {
      return txt;
    }
  }

  /**
   * 固定値定義（更新区分）<br>
   */
  public enum ValSrccdSeqno implements Option {
    /** ソースコード1 */
    SRC1("1", "ソースコード1"),
    /** ソースコード2 */
    SRC2("2", "ソースコード2"),
    /** 仮登録コード */
    KARI("9", "仮登録コード"),
    /** 一般コード */
    NML("", "一般コード");

    private final String val;
    private final String txt;

    /** 初期化 */
    private ValSrccdSeqno(String val, String txt) {
      this.val = val;
      this.txt = txt;
    }

    /** @return val 値 */
    @Override
    public String getVal() {
      return val;
    }

    /** @return txt 表示名称 */
    @Override
    public String getTxt() {
      return txt;
    }
  }

  /**
   * 固定値定義（テーブル区分）<br>
   */
  public enum ValTablekbn implements Option {
    /** 正 */
    SEI("0", "(正)"),
    /** 予約 */
    YYK("1", "(予)"),
    /** CSV ※処理用に追加 */
    CSV("9", "(CSV)");

    private final String val;
    private final String txt;

    /** 初期化 */
    private ValTablekbn(String val, String txt) {
      this.val = val;
      this.txt = txt;
    }

    /** @return val 値 */
    @Override
    public String getVal() {
      return val;
    }

    /** @return txt 表示名称 */
    @Override
    public String getTxt() {
      return txt;
    }
  }

  /**
   * 固定値定義（ジャーナル処理区分）<br>
   */
  public enum InfTrankbn {
    /** INSERT */
    INS("1", "INSERT"),
    /** UPDATE */
    UPD("2", "UPDATE"),
    /** DELETE */
    DEL("3", "DELETE");

    private final String val;
    private final String txt;

    /** 初期化 */
    private InfTrankbn(String val, String txt) {
      this.val = val;
      this.txt = txt;
    }

    /** @return val 値 */
    public String getVal() {
      return val;
    }

    /** @return txt テキスト */
    public String getTxt() {
      return txt;
    }
  }

  /**
   * 固定値定義（作成売価区分）<br>
   */
  public enum SakuBaikakbn {
    /** 標準売価 */
    NML("1", "標準売価"),
    /** 店別売価 */
    TEN("2", "店別売価");

    private final String val;
    private final String txt;

    /** 初期化 */
    private SakuBaikakbn(String val, String txt) {
      this.val = val;
      this.txt = txt;
    }

    /** @return val 値 */
    public String getVal() {
      return val;
    }

    /** @return txt テキスト */
    public String getTxt() {
      return txt;
    }
  }

  /**
   * 固定値定義（枚数指定方法区分）<br>
   */
  public enum MaisuHohokbn {
    /** 枚数指定 */
    MAISU("1", "標準売価"),
    /** 同一枚数 */
    DOMAISU("2", "標準売価"),
    /** 店、構成ページ、枚数指定 */
    TENMAISU("3", "標準売価"),
    /** 店、部門、枚数指定 */
    BMNMAISU("4", "標準売価"),
    /** 店指定 */
    TEN("5", "標準売価"),
    /** 全店 */
    ALL("6", "店別売価");

    private final String val;
    private final String txt;

    /** 初期化 */
    private MaisuHohokbn(String val, String txt) {
      this.val = val;
      this.txt = txt;
    }

    /** @return val 値 */
    public String getVal() {
      return val;
    }

    /** @return txt テキスト */
    public String getTxt() {
      return txt;
    }
  }

  /**
   * 固定値定義（ソース区分）<br>
   */
  public enum Sourcekbn {
    /** 削除 */
    DEL("9", "削除");

    private final String val;
    private final String txt;

    /** 初期化 */
    private Sourcekbn(String val, String txt) {
      this.val = val;
      this.txt = txt;
    }

    /** @return val 値 */
    public String getVal() {
      return val;
    }

    /** @return txt テキスト */
    public String getTxt() {
      return txt;
    }
  }

  /**
   * 表示項目<br>
   * 初期値
   */
  public enum OutputInit implements Option {
    /** 01.時間帯売上実績 */
    VAL001("[\"01\",\"02\",\"03\",\"04\",\"06\",\"08\"]", "01.時間帯売上実績"),
    /** 05.ベスト＆ワースト分析 */
    VAL005("[\"01\",\"02\",\"14\",\"05\",\"04\",\"12\",\"11\",\"15\",\"16\"]", "05.ベスト＆ワースト分析"),
    /** 06.比較分析 */
    VAL006("[\"01\",\"11\",\"09\",\"10\",\"12\",\"13\"]", "06.比較分析"),
    /** 07.平均日商 */
    VAL007("[\"0101\",\"0201\",\"0901\",\"1001\",\"1101\",\"0301\",\"0501\",\"0601\",\"0701\"]", "07.平均日商"),
    /** 12.催し実績 */
    VAL012("[\"0101\",\"0201\",\"0301\",\"0401\",\"0501\",\"0601\",\"0701\",\"0801\",\"0901\",\"1001\",\"1101\"]", "12.催し実績"),
    /** 25.構成頁 */
    VAL025("[\"2\",\"3\",\"4\"]", "25.構成頁");

    private final String val;
    private final String txt;

    /** 初期化 */
    private OutputInit(String val, String txt) {
      this.val = val;
      this.txt = txt;
    }

    /** @return val 値 */
    @Override
    public String getVal() {
      return val;
    }

    /** @return txt 表示名称 */
    @Override
    public String getTxt() {
      return txt;
    }
  }

  /** HTML関連 隠し情報 */
  public enum Hidden {
    /** 変更行 */
    CHANGED_IDX("hiddenChangedIdx", ""),
    /** 選択行 */
    SELECT_IDX("hiddenSelectIdx", ""),

    /** 更新日時 */
    UPDDT("hiddenUpddt", ""),
    /** 事前発注リスト作成済フラグ */
    JLSTCREFLG("jlstcreFlg", ""),

    /** エリア区分 */
    AREA("hiddenArea", ""),
    /** 正規・カット */
    SEICUTKBN("hiddenSeicutkbn", ""),
    /** 割引率 */
    WRITUKBN("hiddenWritukbn", "")

    /* Web商談 */
    /** 件名No */
    ,NO_TEIAN("hiddenNoTeian", "")
    /** 件名No */
    ,TORIHIKI("hiddenTorihiki", "")
    /** 状態_仕掛商品 */
    ,STCD_SHIKAKARI("hiddenStcdShikakari", "")
    /** 状態_提案商品 */
    ,STCD_TEIAN("hiddenStcdTeian", "");

    private final String obj;
    private final String val;

    /** 初期化 */
    private Hidden(String obj, String val) {
      this.obj = obj;
      this.val = val;
    }

    /** @return obj Object名 */
    public String getObj() {
      return obj;
    }

    /** @return val 値 */
    public String getVal() {
      return val;
    }
  }

  /**
   * SQL 関連
   */
  /** 定数value */
  public static final String VAL = "VALUE";
  /** 定数text */
  public static final String TXT = "TEXT";
  /** 定数カレンダー最小日付 */
  public static final String MINDT = "MINDYMD";
  /** 定数カレンダー最大日付 */
  public static final String MAXDT = "MAXDYMD";
  /** 定数最大行数+1 */
  public static final String MAX_ROWNUM = "20001";
  /** 定数四捨五入桁数(率:速報系) */
  public static final int CMN_DIGITS = 1;
  /** 定数四捨五入桁数(率:月次系) */
  public static final int CMN_DIGITS_GTJ = 2;
  /** 定数四捨五入桁数(金額) */
  public static final int CMN_DIGITS_GK = 0;

  /** 定数短縮text */
  public static final String STXT = "STEXT";
  /** 定数カナ */
  public static final String KANA = "KANA";
  /** 定数区分kbn */
  public static final String KBN = "KBN";
  /** 定数区分text */
  public static final String KTXT = "KTEXT";

  /** 定数区分コードと名称の区切り文字 */
  public static final String SEPARATOR = "-";

  /** 営業レポート切換基準年度 2011年以降：売価還元対応 */
  public static final int EIR_CHANGE_FYEAR = 2011;

  /** SQL 関連 スキーマ一覧） */
  public enum Schema {
    /** マスタ情報 */
    INAMS("INAMS");

    private final String val;

    /** 初期化 */
    private Schema(String val) {
      this.val = val;
    }

    /** @return val 値 */
    public String getVal() {
      return val;
    }
  }

  /** SQL 関連 サブグリッドの行数） */
  public enum SubGridRowNumber {
    /** 基本行数 */
    DEF("10"),
    /** 仕入グループ */
    TENGP_SIR("20"),
    /** 売価グループ */
    TENGP_BAIKA("20"), // TODO：記述無し→とりあえず仕入に合わせる
    /** 品揃えグループ */
    TENGP_SHINA("10"),
    /** 店異部門 */
    TENGP_TBMN("5"),
    /** 添加物 */
    TENKABUTSU("10"),
    /** グループ分類 */
    MSTGRP("5"),
    /** グループ分類 */
    MOYCD("50"),
    /** 予備品 */
    TENYOBIHTBMN("99"),
    /** 配送店グループ */
    HSTGP("20"), // TODO：記載無し→いつでも変更できるようにしておく
    /** 配送グループ店 */
    HSGPT("20"), // TODO：記載無し→いつでも変更できるようにしておく
    /** 予約発注_商品一覧 */
    SHOHIN("999"), // TODO：記載無し→いつでも変更できるようにしておく
    /** 予約発注_店舗一覧 */
    TENPO_YH("400"),
    /** 新店改装発注_店舗一覧 */
    TENPO_SK("500"),
    /** プライスカード発行枚数 */
    PCARDMAISU("50"), // TODO：記載無し→いつでも変更できるようにしておく
    /** 店舗休日 */
    TEMPM("200"),
    /** 店舗一覧 */
    TENPO("300"),
    /** 店舗一覧(TG003) */
    TENPO_TG003("999"),
    /** 配送パターン */ // TODO：記載無し→いつでも変更できるようにしておく
    HSPTN("20"),
    /** 店舗一覧 */
    ADTEN("10"), // TODO：n件と記載されている為、可変の可能性がある。
    /** BM商品 */
    BMSHN("200"), // TODO：記載無し→いつでも変更できるようにしておく
    /** BM商品 */
    MOYDEF("100"), // TODO：記載無し→いつでも変更できるようにしておく
    /** 実仕入先コード一覧 */
    ZITSIR("9"),
    /** セット商品 */
    SET("30"),; // TODO：記載無し→いつでも変更できるようにしておく

    private final String val;

    /** 初期化 */
    private SubGridRowNumber(String val) {
      this.val = val;
    }

    /** @return val 値 */
    public String getVal() {
      return val;
    }
  }

  // CAST 定義
  public final static String ID_CAST_NUMBER = " AS DECIMAL(24,0)) ";
  public final static String ID_CAST_DECI_1 = " AS DECIMAL(15,1)) ";
  public final static String ID_CAST_DECI_2 = " AS DECIMAL(15,2)) ";
  // SQL：空白
  public final static String ID_SQL_BLANK = "select 0 from SYSIBM.SYSDUMMY1 where 0 = 1";
  public final static String ID_SQL_BLANK2 = "select VALUE, TEXT from (values ROW('" + Values.NONE.getVal() + "', ' ')) as X(value, TEXT) union all ";
  public final static String ID_SQL_BLANK3 = "select VALUE, TEXT, TEXT as TEXT2 from (values ROW('" + Values.NONE.getVal() + "', ' ')) as X(value, TEXT) union all ";
  public final static String ID_SQL_BLANK4 = "select VALUE, TEXT, TEXT as TEXT2, TEXT as TEXT3 from (values ROW('" + Values.NONE.getVal() + "', ' ')) as X(value, TEXT) union all ";
  // SQL：すべて
  public final static String ID_SQL_ALL = "select VALUE, TEXT from (values ROW('" + Values.NONE.getVal() + "', 'すべて')) as X(value, TEXT)";
  public final static String ID_SQL_ALL2 = "select VALUE, TEXT, SEQ from (values ROW('" + Values.NONE.getVal() + "', 'すべて', -1)) as X(value, TEXT, SEQ)";

  // SQL：共通
  public final static String ID_SQL_CMN = "select cast(? as varchar) as VALUE from SYSIBM.SYSDUMMY1";
  public final static String ID_SQL_CMN_FOOTER = " order by VALUE";
  public final static String ID_SQL_CMN_WHERE = " where COALESCE(UPDKBN,0) <> 1 ";
  public final static String ID_SQL_CMN_WHERE2 = " and COALESCE(UPDKBN,0) <> 1 ";
  public final static String ID_SQL_GRD_CMN = "WITH RECURSIVE T1(IDX) as (select 1 from (SELECT 1 AS DUMMY) DUMMY UNION ALL SELECT IDX+1 FROM T1 WHERE IDX < @M ) ";
  public final static String ID_SQL_GRD_CMN0 = "WITH RECURSIVE T1(IDX) as (select 1 from (SELECT 1 AS DUMMY) DUMMY UNION ALL SELECT IDX+1 FROM T1 WHERE IDX < @M ) ";
  public final static String ID_SQL_GRD_EMPTY = ID_SQL_GRD_CMN + "select IDX from T1";
  public final static String ID_SQL_CMN_WEEK = "WITH WEEK AS (SELECT CWEEK ,'(' || JWEEK || ')' AS JWEEK ,JWEEK AS JWEEK2 FROM ( SELECT 1 AS CWEEK , '日' AS JWEEK "
      + "UNION SELECT 2 , '月' UNION SELECT 3 ,'火' UNION SELECT 4 ,'水' UNION SELECT 5 ,'木' UNION SELECT 6 ,'金' UNION SELECT 7 ,'土' ) T1 ) ";

  public final static String ID_SQL_CHK_TBL = "select count(@C) as VALUE from @T where @C in (?)";
  public final static String ID_SQL_CHK_TBL_SEL = "select @C as VALUE from @T where @C in (?)";

  public final static String ID_SQL_CHK_TBL_MULTI = "select count(*) as VALUE from @T where ";

  public final static String ID_SQL_ARR_CMN = ",ARRWK(IDX, RNK, S, ARR, LEN) as (" + " select 1, SUBSTR(ARR, 1 , LEN), 1 + LEN, ARR, LEN from WK " + " union all"
      + " select IDX+1, SUBSTR(ARR, S , LEN), S + LEN, ARR, LEN from ARRWK where S <= length(ARR)" + ") ";

  // SQL：カレンダーマスター
  public final static String ID_SQL_CAL = "select min(COMTOB) as " + MINDT + ", max(COMTOB) as " + MAXDT + " from INAYS.MCALTT";

  // SQL：区分テーブル
  public final static String ID_SQL_KBN = "select rtrim(IKBUQID) as VALUE, IBKUQVL as TEXT from INAMS.PIMSKB where IKBGPID = ? ";
  public final static String ID_SQL_KBNS = "select rtrim(IKBUQID) as VALUE, IBKUQVL as TEXT from INAMS.PIMSKB where IKBGPID in (@) ";
  public final static String ID_SQL_KBN_TAIL = " order by IBKVSEQ";

  // SQL：名称マスタ
  public final static String ID_SQL_MEISYO_CD_WHERE = " and MEISHOCD = ? ";
  public final static String ID_SQL_MEISYO_CD_WHERE2 = " and MEISHOCD||'" + SEPARATOR + "'||rtrim(NMKN) like ? ";
  public final static String ID_SQL_MEISYO_HEAD = "select " + VAL + " as " + KBN + "," + VAL + "," + TXT + " as " + KTXT + "," + TXT + "," + TXT + " as TEXT2," + TXT + " as " + STXT + "," + TXT
      + " as " + KANA + " from (values ROW('" + Values.NONE.getVal() + "','　')) as X(" + VAL + "," + TXT + ") union all ";
  public final static String ID_SQL_MEISYO_HEAD2 = "select " + VAL + " as " + KBN + "," + VAL + "," + TXT + " as " + KTXT + "," + TXT + "," + TXT + " as TEXT2," + TXT + " as " + STXT + "," + TXT
      + " as " + KANA + " from (values ROW('" + Values.ALLKAN.getVal() + "','" + Values.ALLKAN.getTxt() + "')) as X(" + VAL + "," + TXT + ") union all ";
  public final static String ID_SQL_MEISYO_HEAD3 = "select " + VAL + " as " + KBN + "," + VAL + "," + TXT + " as " + KTXT + "," + TXT + "," + TXT + " as TEXT2," + TXT + " as " + STXT + "," + TXT
      + " as " + KANA + " from (values ROW('" + Values.ALL.getVal() + "','" + Values.ALL.getTxt() + "')) as X(" + VAL + "," + TXT + ") union all ";
  public final static String ID_SQL_MEISYO = "select MEISHOKBN as " + KBN + ", MEISHOCD as " + VAL + ", MEISHOKBNKN as " + KTXT + ", MEISHOCD||'" + SEPARATOR + "'||rtrim(NMKN) as " + TXT
      + ", rtrim(NMKN) as TEXT2, TNMKN as " + STXT + ", NMAN as " + KANA + " from INAMS.MSTMEISHO where MEISHOKBN = ? ";
  public final static String ID_SQL_MEISYO2 = "select MEISHOKBN as " + KBN + ", MEISHOCD as " + VAL + ", MEISHOKBNKN as " + KTXT + ", rtrim(NMKN) as " + TXT + ", rtrim(NMKN) as TEXT2, TNMKN as "
      + STXT + ", NMAN as " + KANA + " from INAMS.MSTMEISHO where MEISHOKBN = ? ";
  public final static String ID_SQL_MEISYO_FOOTER = " order by " + KBN + ", " + VAL + ", " + TXT + "";
  public final static String ID_SQL_MEISYO_FOOTER2 = " order by " + KBN + ", " + VAL + " DESC, " + TXT + "";
  public final static String ID_SQL_MEISYO_PROHIBITED = "select * from (values ROW('" + DataType.KANA.getObj() + "', 'ｧｨｩｪｫｯｬｭｮ', 'ｱｲｳｴｵﾂﾔﾕﾖ'),ROW('" + DataType.ZEN.getObj()
      + "', '〜', 'ー'),ROW('ng', ',', null)) as MSTMEISHO(MOJI, " + TXT + ", " + TXT + "2)";

  // SQL：メッセージマスター
  public final static String ID_SQL_MSG_WHERE = " where MSGCD = ?";
  public final static String ID_SQL_MSG_WHERE2 = " where MSGCD like ? or MSGCD like ? or MSGCD like ? ";
  public final static String ID_SQL_MSG = "select * from INAAD.MSTSYSMSG ";

  // SQL：処理日付 TODO:本当に取り方これでいいのか確認？
  public final static String ID_SQLSHORIDT = "select SHORIDT as " + VAL + " from INAAD.SYSSHORIDT where COALESCE(UPDKBN, 0) <> 1 order by ID desc limit 1 ";
  public final static String ID_SQLSHORIDT2 = "select SUBSTR(SHORIDT,3) as " + VAL + " from INAAD.SYSSHORIDT where COALESCE(UPDKBN, 0) <> 1 order by ID desc limit 1 ";
  public final static String ID_SQLSHORIDT3 = "select SHORIDT as " + VAL + "2,DATE_FORMAT(DATE_FORMAT(SHORIDT,'%Y%m%d') + INTERVAL 1 YEAR,'%Y%m%d') as " + VAL
      + "3 from INAAD.SYSSHORIDT where COALESCE(UPDKBN, 0) <> 1 order by ID desc limit 1 ";

  // SQL：処理日付曜日
  public final static String ID_SQLSHORIDTWEEK = "select DAYOFWEEK(DATE_FORMAT(SHORIDT,'%Y%m%d')) as " + VAL + " from INAAD.SYSSHORIDT where COALESCE(UPDKBN, 0) <> 1 order by ID desc limit 1 ";

  // SQL：週№期間を取得
  public final static String ID_SQLSHUNOPERIOD = DefineReport.ID_SQL_CMN_WEEK + ",SHORI as(SELECT CASE WHEN DAYOFWEEK(DATE_FORMAT(" + VAL + ",'%Y%m%d')) = 2 OR DAYOFWEEK(DATE_FORMAT(" + VAL
      + ",'%Y%m%d')) = 3 THEN DATE_FORMAT(DATE_ADD(DATE_FORMAT(" + VAL + ",'%Y%m%d'), INTERVAL 7 DAY),'%Y%m%d') ELSE DATE_FORMAT(DATE_ADD(DATE_FORMAT(" + VAL
      + ",'%Y%m%d') , INTERVAL 14 DAY),'%Y%m%d') END AS DT FROM(" + DefineReport.ID_SQLSHORIDT + ")T1) "
      + "SELECT DATE_FORMAT(DATE_FORMAT(SHUNO.STARTDT,'%Y%m%d'),'%Y/%m/%d') || (select JWEEK from WEEK where CWEEK=DAYOFWEEK(DATE_FORMAT(SHUNO.STARTDT,'%Y%m%d'))) || '～' || DATE_FORMAT(DATE_FORMAT(SHUNO.ENDDT,'%Y%m%d'),'%Y/%m/%d') || (select JWEEK from WEEK where CWEEK=DAYOFWEEK(DATE_FORMAT(SHUNO.ENDDT,'%Y%m%d'))) AS "
      + VAL + " FROM INAAD.SYSSHUNO AS SHUNO, SHORI WHERE SHORI.DT BETWEEN SHUNO.STARTDT AND SHUNO.ENDDT";
  // SQL：期間(初期表示用)
  public final static String ID_SQL_KIKAN_DAY_INIT = "select max(COMTOB) as DT1, max(COMTOB) as DT2 from INAYS.MCALTT";
  public final static String ID_SQL_KIKAN_WEEK_INIT =
      "select COALESCE(MAX(NENDO), - 1) as NENDO, COALESCE(MIN(COMTOB), - 1) as MINDT, COALESCE(MAX(COMTOB), - 1) as MAXDT from INAYS.MCALTT where NENSYUU = (select integer(NENSYUU) - 100 from INAYS.MCALTT where COMTOB = ?)";
  // 登録系画面
  public final static String ID_SQL_KIKAN_MONTH_INIT = "select DT1, DT2 from ( select max(NENTUKI) as DT1, max(NENTUKI) as DT2 from INAYS.MCALTT where NENTUKI = ?"
      + " union all select max(NENTUKI) as DT1, max(NENTUKI) as DT2 from INAYS.MCALTT where NENTUKI = ?"
      + " union all select max(NENTUKI) as DT1, max(NENTUKI) as DT2 from INAYS.MCALTT ) T where DT1 is not null fetch first 1 row only";
  // 確認系画面
  public final static String ID_SQL_KIKAN_MONTH_INIT2 = "select DT1, DT2 from ( select max(NENTUKI) as DT1, max(NENTUKI) as DT2 from INAYS.MCALTT where NENTUKI = ? and '01' < ?"
      + " union all select max(NENTUKI) as DT1, max(NENTUKI) as DT2 from INAYS.MCALTT where NENTUKI = ?"
      + " union all select max(NENTUKI) as DT1, max(NENTUKI) as DT2 from INAYS.MCALTT ) T where DT1 is not null fetch first 1 row only";
  /** 共通（INAYS.MCALTT） */
  // SQL：期間(月)共通部
  private final static String ID_SQL_KIKAN_YM_2 =
      ", TEXT from (select MIN(NENTUKI) as MINDT, MAX(NENTUKI) as MAXDT, left(NENTUKI,4) || '年' || substr(NENTUKI,5) || '月' as TEXT from INAYS.MCALTT group by NENTUKI) order by VALUE desc";
  // SQL：期間(月)FROM
  public final static String ID_SQL_KIKAN_YM_FROM = "select MINDT as VALUE" + ID_SQL_KIKAN_YM_2;
  // SQL：期間(月)TO
  public final static String ID_SQL_KIKAN_YM_TO = "select MAXDT as VALUE" + ID_SQL_KIKAN_YM_2;

  // SQL 催しコード
  public final static String ID_SQL_MOYSCD =
      "select CONCAT(CONCAT(CONCAT(CONCAT(T1.MOYSKBN, '-'), T1.MOYSSTDT), '-'), right ('000' || T1.MOYSRBAN, 3)) as VALUE, CONCAT(CONCAT(CONCAT(CONCAT(T1.MOYSKBN, '-'), T1.MOYSSTDT), '-'), right ('000' || T1.MOYSRBAN, 3)) as TEXT from INATK.TOKMOYCD T1 ";
  public final static String ID_SQL_MOYSCD_HEAD = " union all select VALUE, TEXT from (values ROW('-1', '　')) as X(value, TEXT) ORDER BY VALUE ";
  public final static String ID_SQL_HEAD_MOYSCD =
      "select VALUE, TEXT, TEXT2, TEXT3, TEXT4 from (values ROW('-1', '　', 0, 0, 0)) as X(value, TEXT, TEXT2, TEXT3, TEXT4) union all select CONCAT(CONCAT(CONCAT(CONCAT(T1.MOYSKBN, '-'), T1.MOYSSTDT), '-'), T1.MOYSRBAN) as VALUE, CONCAT(CONCAT(CONCAT(CONCAT(T1.MOYSKBN, '-'), T1.MOYSSTDT), '-'), T1.MOYSRBAN) as TEXT, T1.MOYSKBN as TEXT2, T1.MOYSSTDT as TEXT3, T1.MOYSRBAN as TEXT4 from INATK.TOKMOYCD T1 where T1.MOYSKBN = 8 order by TEXT2, TEXT3, TEXT4;";
  public final static String ID_SQL_MOYO = DefineReport.ID_SQL_CMN_WEEK
      + "select MOYKN as F2,DATE_FORMAT(DATE_FORMAT(NNSTDT, '%Y%m%d'), '%y/%m/%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(NNSTDT, '%Y%m%d')))    ||'～'||    DATE_FORMAT(DATE_FORMAT(NNEDDT, '%Y%m%d'), '%y/%m/%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(NNEDDT, '%Y%m%d'))) as F3, NNSTDT as F4, NNEDDT as F5 from INATK.TOKMOYCD where MOYSKBN = ? and MOYSSTDT = ? and MOYSRBAN = ?";
  public final static String ID_SQL_MOYSCD2 = DefineReport.ID_SQL_CMN_WEEK
      + "select MOYKN as F2,DATE_FORMAT(DATE_FORMAT(NNSTDT, '%Y%m%d'), '%y%m%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(NNSTDT, '%Y%m%d')))    ||'～'||    DATE_FORMAT(DATE_FORMAT(NNEDDT, '%Y%m%d'), '%y%m%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(NNEDDT, '%Y%m%d'))) as F3"
      + ",DATE_FORMAT(DATE_FORMAT(HBSTDT, '%Y%m%d'), '%y%m%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(HBSTDT, '%Y%m%d'))) as F4"
      + ",DATE_FORMAT(DATE_FORMAT(HBEDDT, '%Y%m%d'), '%y%m%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(HBEDDT, '%Y%m%d'))) as F5"
      + ",DATE_FORMAT(DATE_FORMAT(PLUSDDT, '%Y%m%d'), '%y%m%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(PLUSDDT, '%Y%m%d'))) as F6,PLUSFLG as F7, PLUSDDT as F8"
      + " from INATK.TOKMOYCD where MOYSKBN = ? and MOYSSTDT = ? and MOYSRBAN = ?";
  public final static String ID_SQL_TOKMOYCD = "SELECT COUNT(MOYSKBN) AS " + VAL + " FROM INATK.TOKMOYCD WHERE MOYSKBN=? AND MOYSSTDT=? AND MOYSRBAN=? AND PLUSFLG='1'";

  // SQL PLU配信済のPLU配信日
  public final static String ID_SQL_PLUSDDT = "SELECT PLUSDDT AS " + VAL + " FROM INATK.TOKMOYCD WHERE MOYSKBN=? AND MOYSSTDT=? AND MOYSRBAN=? AND PLUSFLG='1'";

  // SQL 分類割引 重複チェック
  public final static String ID_SQL_NEWDEPLICATECHEACK = "SELECT COUNT(*) as " + VAL + " FROM INATK.TOKBT_KKK" + " WHERE MOYSKBN =? AND MOYSSTDT =? AND BMNCD =? AND DAICD =?";

  // SQL 分類割引 重複チェック 中分類含む場合
  public final static String ID_SQL_NEWDEPLICATECHEACK_C = "SELECT COUNT(*) as " + VAL + " from INATK.TOKBT_KKK" + " WHERE MOYSKBN =? AND MOYSSTDT =? AND BMNCD =? AND DAICD =? AND CHUCD =?";

  /** 分類割引_企画（INATK.TOKBT_KKK） */
  // SQL 販売期間_終了日
  public final static String ID_SQL_HBEDDT = "SELECT HBEDDT as " + VAL + " FROM INATK.TOKBT_KKK WHERE KKKNO =?";

  /** 共通（INAAD.MCALTT） */
  // SQL：期間(月)TO
  public final static String ID_SQL_SHUNO = " select RIGHT('0000'||T1.SHUNO,4) as " + VAL
      + ", RIGHT('0000'||T1.SHUNO,4)||'-'||DATE_FORMAT(DATE_FORMAT(T1.STARTDT, '%y/%m/%d'), '%y/%m/%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T1.STARTDT, '%Y%m%d')))"
      + "||'～'||DATE_FORMAT(DATE_FORMAT(T1.ENDDT, '%y/%m/%d'), '%y/%m/%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T1.ENDDT, '%Y%m%d'))) as " + TXT + "@C"
      + " from INAAD.SYSSHUNO T1" + " @T";
  public final static String ID_SQL_TOKSHUNO = " select RIGHT('0000'||T2.SHUNO,4) as " + VAL
      + ", CASE WHEN T1.STARTDT IS NULL AND T1.ENDDT IS NULL THEN RIGHT ('0000' || T2.SHUNO, 4) || '-' ELSE RIGHT('0000'||T2.SHUNO,4)||'-'||DATE_FORMAT(DATE_FORMAT(T1.STARTDT, '%Y%m%d'), '%y/%m/%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T1.STARTDT, '%Y/%m/%d')))"
      + "||'～'||DATE_FORMAT(DATE_FORMAT(T1.ENDDT, '%Y/%m/%d'), '%y/%m/%d')||(select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T1.ENDDT, '%Y/%m/%d'))) END as " + TXT + "@C"
      + " from INATK.TOKMOYSYU T2 LEFT JOIN INAAD.SYSSHUNO T1 ON T1.SHUNO = T2.SHUNO where T2.UPDKBN = 0 " + " @T";
  // SQL：週№(処理日付を基準日とした週、翌週、翌々週のデータ)
  public final static String ID_SQL_SHUNO2 = DefineReport.ID_SQL_CMN_WEEK
      + ",SHORI as(SELECT DAYOFWEEK(DATE_FORMAT(VALUE, '%Y%m%d')) AS DT,DATE_FORMAT(DATE_FORMAT(VALUE, '%Y%m%d'), '%Y%m%d') AS N,DATE_FORMAT(DATE_ADD(DATE_FORMAT(VALUE, '%Y%m%d') , INTERVAL 7 DAY), '%Y%m%d') AS N1,DATE_FORMAT(DATE_ADD(DATE_FORMAT(VALUE, '%Y%m%d'), INTERVAL 14 DAY), '%Y%m%d') AS N2 FROM("
      + DefineReport.ID_SQLSHORIDT + ")T2) " + "SELECT SHUNO.SHUNO AS " + VAL + ",SUBSTR(SHUNO.SHUNO,1,2) || '" + SEPARATOR
      + "' ||SUBSTR(SHUNO.SHUNO,3,4) ||' ' || DATE_FORMAT(DATE_FORMAT(SHUNO.STARTDT,'%Y%m%d'),'%Y/%m/%d') || (select JWEEK from WEEK where CWEEK=DAYOFWEEK(DATE_FORMAT(SHUNO.STARTDT,'%Y%m%d'))) || '～' || DATE_FORMAT(DATE_FORMAT(SHUNO.ENDDT,'%Y%m%d'),'%Y/%m/%d') || (select JWEEK from WEEK where CWEEK=DAYOFWEEK(DATE_FORMAT(SHUNO.ENDDT,'%Y%m%d'))) AS "
      + TXT + " FROM INAAD.SYSSHUNO AS SHUNO, SHORI "
      + "WHERE (SHORI.DT IN (2,3) AND (SHORI.N BETWEEN SHUNO.STARTDT AND SHUNO.ENDDT OR SHORI.N1 BETWEEN SHUNO.STARTDT AND SHUNO.ENDDT)) OR ((SHORI.DT <> 2 AND SHORI.DT <> 3) AND (SHORI.N1 BETWEEN SHUNO.STARTDT AND SHUNO.ENDDT OR SHORI.N2 BETWEEN SHUNO.STARTDT AND SHUNO.ENDDT)) ORDER BY SHUNO.SHUNO";

  // SQL：週№(処理日付を基準日とした、翌週、翌々週以降のデータ)
  public final static String ID_SQL_SHUN_TR007 = DefineReport.ID_SQL_CMN_WEEK + " select * from (select VALUE, TEXT from (values ROW('-1', '　')) as X(value, TEXT)"
      + " union all select right ('0000' || T1.SHUNO, 4) as value, right ('0000' || T1.SHUNO, 4) || '-' || DATE_FORMAT(DATE_FORMAT(T1.STARTDT, '%y/%m/%d'), '%y/%m/%d') || (select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T1.STARTDT, '%Y%m%d'))) || '～' || DATE_FORMAT(DATE_FORMAT(T1.ENDDT, '%y/%m/%d'), '%y/%m/%d') || (select JWEEK from WEEK where CWEEK = DAYOFWEEK(DATE_FORMAT(T1.ENDDT, '%Y%m%d'))) as TEXT from INAAD.SYSSHUNO T1 inner join (select distinct SHUNO from INATK.HATTR_CSV where COALESCE(UPDKBN, 0) <> 1) T2 on T1.SHUNO = T2.SHUNO"
      + " ) T order by replace (value, '-1', '9999') desc";

  public final static String ID_SQL_SHUNO_HEAD = "select VALUE, TEXT from (values ROW('" + Values.NONE.getVal() + "', '　')) as X(value, TEXT) union all ";
  public final static String ID_SQL_SHUNO_HEAD2 = "select VALUE, TEXT from (values ROW('" + Values.NONE.getVal() + "', '')) as X(value, TEXT) union all ";

  // 期間共通（閏年除外）
  public final static String ID_SQL_KIKAN_CMN_WHERE = " NENDO > 0 ";

  // SQL：販売部・店舗共通
  public final static String ID_SQL_TEN_HANTOUBU_WHERE = " and HTOUKATU_CD_S = ? ";
  public final static String ID_SQL_TEN_HANTOUBU_WHEREP = " and HTOUKATU_CD_P = ? ";
  public final static String ID_SQL_TEN_HANTOUBUS_WHERE = " and HTOUKATU_CD_S in (@) ";
  public final static String ID_SQL_TEN_HANTOUBUS_WHEREP = " and HTOUKATU_CD_P in (@) ";
  public final static String ID_SQL_TEN_ICHIBA_WHERE = " and ICHIBA = ? ";
  public final static String ID_SQL_TEN_ICHIBAS_WHERE = " and ICHIBA in (@) ";
  public final static String ID_SQL_TEN_HANBAIBU_WHERE = " and HANBAIB_S = ? ";
  public final static String ID_SQL_TEN_HANBAIBU_WHEREP = " and HANBAIB_P = ? ";
  public final static String ID_SQL_TEN_HANBAIBUS_WHERE = " and HTOUKATU_CD_S||HANBAIB_S in (@) ";
  public final static String ID_SQL_TEN_HANBAIBUS_WHEREP = " and HTOUKATU_CD_P||HANBAIB_P in (@) ";
  /** 閉鎖店除外条件 */
  public final static String ID_SQL_TEN_EXIST = " and (TENHEH >= TO_CHAR(current date, 'yyyyMMdd') or TENHEH = 0) ";
  /** 既存店条件 */
  public final static String ID_SQL_TEN_KZN =
      " and exists (select 'X' from INAMS.MTNPJIK M20 where M20.NENTUKI = ? and M20.MISECD = T1.MISECD and M20.KIZONTEN_KBN = '" + DefineReport.ValKizonKbn.KIZON.getVal() + "') ";
  /** 新店条件 */
  public final static String ID_SQL_TEN_NEW =
      " and exists (select 'X' from INAMS.MTNPJIK M20 where M20.NENTUKI = ? and M20.MISECD = T1.MISECD and M20.KIZONTEN_KBN = '" + DefineReport.ValKizonKbn.NEW.getVal() + "') ";

  // SQL：店舗
  public final static String ID_SQL_TENPO_FOOTER = " order by VALUE";
  /** 共通（INAMS.TENPO_MST） */
  public final static String ID_SQL_TENPO = "select MISECD as VALUE, MISECD|| ' ' || rtrim(TENMEI) as TEXT from INAMS.TENPO_MST";
  /** 店舗データ（INATR.MTNPXX） */
  public final static String ID_SQL_TENPO_XX = "select MISECD as VALUE,  MISECD|| ' ' || rtrim(TENMEI) as TEXT from INATR.MTNPXX";
  /** 共通（INAMS.MSTTENT） */
  public final static String ID_SQL_TENPO_MDM = "select right ('000' || TENCD, 3) as value, right ('000' || TENCD, 3) || '" + SEPARATOR
      + "' || COALESCE(TRIM(TRAILING '　' FROM(RTRIM(TENKN))), '') as TEXT, TRIM(TRAILING '　' FROM(RTRIM(TENKN))) as TEXT2 from INAMS.MSTTEN order by VALUE";

  public final static String ID_SQL_TENPO_HEAD = "select VALUE, TEXT from (values ROW('" + Values.NONE.getVal() + "', 'すべて')) as X(value, TEXT) union all ";
  public final static String ID_SQL_TENPO_HEAD2 = "select VALUE, TEXT from (values " + " (" + Values.TENPO_ALL.getVal() + ", '" + Values.TENPO_ALL.getTxt() + "'),(" + Values.TENPO_NEW.getVal() + ", '"
      + Values.TENPO_NEW.getTxt() + "'),(" + Values.TENPO_EX.getVal() + ", '" + Values.TENPO_EX.getTxt() + "')" + ") as X(value, TEXT) union all ";
  public final static String ID_SQL_TENPO_HEAD3 = "select VALUE, TEXT from (values ROW('', '')) as X(value, TEXT) union all ";
  public final static String ID_SQL_TENPO_MDM_HEAD =
      "select " + VAL + "," + TXT + "," + TXT + " as TEXT2 from (values ROW('" + Values.NONE.getVal() + "','　')) as X(" + VAL + "," + TXT + ") union all ";

  // SQL：店舗 TODO:
  public final static String ID_SQL_TEN =
      "select TENCD as F1, TENAN as F2, CASE WHEN TENKN IS NULL OR TENKN = '' THEN '　' ELSE TENKN END as F3 from INAMS.MSTTEN where UPDKBN = " + DefineReport.ValUpdkbn.NML.getVal();

  // SQL：週No. TODO:
  public final static String ID_SQL_SHUNO_M = "select SHUNO as F1 from INAAD.SYSSHUNO where STARTDT <= ? and ? <= ENDDT ";

  // SQL：店グループ TODO:
  public final static String ID_SQL_TEN_MR002 = "select TENGPCD as F1, TENGPAN as F2, TENGPKN as F3 from INAMS.MSTHSTENGP where HSGPCD = ? and TENGPCD = ? ";
  public final static String ID_SQL_TEN_SI002 =
      "select HSTG.HSGPCD as F1, HSTG.TENGPAN as F2, HSTG.TENGPKN as F3 from INAMS.MSTAREAHSPTN EHP left join INAMS.MSTHSTENGP HSTG on HSTG.HSGPCD = EHP.HSGPCD and HSTG.TENGPCD = EHP.TENGPCD where EHP.HSPTN = ? and EHP.TENGPCD = ? ";
  public final static String ID_SQL_TENGPCD = "select TENGPCD as VALUE from INATK.TOKTG_QATEN where MOYSKBN = ? and MOYSSTDT = ? and MOYSRBAN = ? and TENCD = ? and KYOSEIFLG = ? and LDTENKBN = 1";

  // SQL：付番済管理番号
  public final static String SUMI_KANRINO =
      "select SYSR.SUMI_KANRINO + 1 as VALUE from INATK.SYSRS SYSR where SYSR.HBSTDT = ? and SYSR.BMNCD = ? and SYSR.WRITUKBN = ? and SYSR.SEICUTKBN = ? and SYSR.DUMMYCD = ? ";

  // SQL：店舗一覧(汎用):
  public final static String ID_SQL_TENPO_LIST = ID_SQL_GRD_CMN + "select T1.IDX, T2.TENCD, T2.TENKN from T1"
      + " left join (select ROW_NUMBER() over (order by T.TENCD) as IDX, T.TENCD as TENCD, M.TENKN as TENKN from @T T left join INAMS.MSTTEN M on T.TENCD = M.TENCD and M.UPDKBN = "
      + DefineReport.ValUpdkbn.NML.getVal() + " fetch first @M row only) T2 on T1.IDX = T2.IDX order by T1.IDX ,T2.TENCD";

  // SQL：店舗一覧(エリア区分１) TODO:
  public final static String ID_SQL_TENPO_TB =
      "select right('000' || TBM.TENCD, 3) as F1, MAX(TEN.TENKN) as F2 from INAMS.MSTTENBMN TBM left join INAMS.MSTTEN TEN on TEN.TENCD = TBM.TENCD where TBM.AREACD = ? group by TBM.TENCD order by TBM.TENCD";

  // SQL：店舗一覧(エリア区分２) TODO:
  public final static String ID_SQL_TENPO_HG =
      "select right('000' || HGT.TENCD, 3) as F1, TEN.TENKN as F2 from INAMS.MSTHSGPTEN HGT left join INAMS.MSTTEN TEN on TEN.TENCD = HGT.TENCD where HGT.HSGPCD = ? and HGT.TENGPCD = ? order by HGT.TENCD";

  // SQL：店舗一覧(エリア区分２) TODO:
  public final static String ID_SQL_TENPO_HG2 = ID_SQL_GRD_CMN
      + "SELECT T1.IDX,T2.TENCD,T2.TENKN FROM T1 LEFT JOIN (SELECT ROW_NUMBER() OVER (ORDER BY HGT.HSGPCD,HGT.TENGPCD,HGT.TENCD) AS IDX,HGT.TENCD,TEN.TENKN FROM INAMS.MSTHSGPTEN HGT LEFT JOIN INAMS.MSTTEN TEN ON TEN.TENCD=HGT.TENCD WHERE HGT.HSGPCD=? AND HGT.TENGPCD=? AND HGT.UPDKBN=0 ORDER BY HGT.HSGPCD,HGT.TENGPCD,HGT.TENCD) AS T2 ON T1.IDX=T2.IDX ORDER BY T1.IDX";

  // SQL：店舗一覧(商品店グループ店舗参照) TODO:
  public final static String ID_SQL_TENPO_STGT =
      "with RECURSIVE T1(IDX) as (select 1 from (SELECT 1 AS DUMMY) AS DUMMY union all select IDX + 1 from T1 where IDX < @M) select T1.IDX, T2.TENCD, T2.TENKN from T1"
          + " left join (select ROW_NUMBER() over (order by TENGP.GPKBN, TENGP.BMNCD, TENGP.TENGPCD, TENGP.TENCD) as IDX, TENGP.TENCD as TENCD, TEN.TENKN as TENKN from INAMS.MSTSHNTENGPTEN TENGP left join (select TENCD, TENKN from INAMS.MSTTEN where UPDKBN = "
          + DefineReport.ValUpdkbn.NML.getVal()
          + ") TEN on TEN.TENCD = TENGP.TENCD where TENGP.GPKBN = ? and TENGP.BMNCD = ? and TENGP.TENGPCD = ? order by TENGP.TENCD) T2 on T1.IDX = T2.IDX order by T1.IDX ,T2.TENCD";

  // SQL：店舗一覧(発注数項目あり) TODO:
  public final static String ID_SQL_TENPO_HTS =
      "with RECURSIVE T1(IDX) as (select 1 from (SELECT 1 AS DUMMY) DUMMY union all select IDX + 1 from T1 where IDX < @M) select T1.IDX, T2.F1 as TENCD, T2.F2 as TENKN, T2.F3 as HTSU, T2.F4 as HTSU_T, case when T2.F1 is not null then 0 else 1 end as SINKFLG from T1 left join (select ROW_NUMBER() over (order by HTEN.TENCD) as IDX, HTEN.TENCD as F1, MAX(TEN.TENKN) as F2, SUM(case when COALESCE(HTEN.INPUTDT, 0) < ? then HTEN.HTSU else 0 end) as F3, SUM(case when COALESCE(HTEN.INPUTDT, 0) = ? then HTEN.HTSU else 0 end) as F4"
          + " from INATK.HATYH_TEN HTEN left join INAMS.MSTTEN TEN on TEN.TENCD = HTEN.TENCD @W group by HTEN.KKKCD, HTEN.SHNCD, HTEN.TENCD order by HTEN.KKKCD, HTEN.SHNCD, HTEN.TENCD) T2 on T1.IDX = T2.IDX order by T1.IDX";

  // SQL：店舗一覧(新店改装発注) TODO:
  public final static String ID_SQL_TENPO_SK =
      "WITH RECURSIVE T1(IDX) as (select 1 from (SELECT 1 AS DUMMY) DUMMY union all select IDX + 1 from T1 where IDX < @M) select T1.IDX, T2.KANRINO, T2.SHNCD, T2.SHNKN, T2.SURYO from T1 left join (select ROW_NUMBER() over (order by HSKS.INPUTNO, HSKS.KANRINO) as IDX, HSKS.KANRINO, case when HSKS.SHNCD = 99999994 then '99999994' else SHN.SHNCD end as SHNCD, case when HSKS.SHNCD = 99999994 then '棚段変わります' else SHN.SHNKN end as SHNKN, HSKS.SURYO from INATK.HATSK_SHN HSKS left join INAMS.MSTSHN SHN on SHN.SHNCD = right ('00000000' || TRIM(HSKS.SHNCD), 8) where (COALESCE(SHN.UPDKBN, 0) <> 1 or right ('00000000' || TRIM(HSKS.SHNCD), 8) = '99999994') and right ('00000' || HSKS.INPUTNO, 5) = ? order by HSKS.KANRINO) T2 on T1.IDX = T2.IDX @W order by T1.IDX";

  // SQL：店舗一覧(新店改装発注_構成別) TODO:
  public final static String ID_SQL_TENPO_SK_K =
      "WITH RECURSIVE T1(IDX) as (select 1 from (SELECT 1 AS DUMMY) DUMMY union all select IDX + 1 from T1 where IDX < @M) select T1.IDX, T2.SHNCD, T2.SHNKN from T1 left join (select ROW_NUMBER() over (order by KSPG.LINENO) as IDX, case when KSPG.SHNCD = 99999994 then '99999994' else SHN.SHNCD end as SHNCD, case when KSPG.SHNCD = 99999994 then '棚段変わります' else SHN.SHNKN end as SHNKN, KSPG.LINENO from INAMS.MSTKSPAGE KSPG inner join INAMS.MSTTEN TEN on COALESCE(TEN.UPDKBN, 0) <> 1 and TEN.TENCD = KSPG.TENCD left join INAMS.MSTSHN SHN on SHN.SHNCD = right ('00000000' || TRIM(KSPG.SHNCD), 8) @W order by KSPG.LINENO) T2 on T1.IDX = T2.IDX order by T1.IDX, T2.LINENO";

  // SQL：店確認
  public final static String ID_SQL_ADTEN =
      "with RECURSIVE T1(IDX) as (select 1 from (SELECT 1 AS DUMMY) DUMMY union all select IDX + 1 from T1 where IDX < @M) select T1.IDX, T2.TENCD as ADDTEN, T3.TENCD as DELTEN from T1 left join (select ROW_NUMBER() over (order by KKKNO, TENCD) as IDX, TENCD from INATK.TOKBT_ADTEN where ADDDELFLG = 1 and KKKNO = ? ) T2 on T1.IDX = T2.IDX left join (select ROW_NUMBER() over (order by KKKNO, TENCD) as IDX, TENCD from INATK.TOKBT_ADTEN where ADDDELFLG = 0 and KKKNO = ? ) T3 on T1.IDX = T3.IDX order by T1.IDX";

  // SQL：本部ユーザ初回利用時の店舗（関連情報の最小公約店コード）
  public final static String ID_SQL_TENPO_MIN_CD = "SELECT MIN(T0.TENCD) FROM INAMS.MSTTEN T0 "
  // + "INNER JOIN INATK.HATYH_TEN T1 ON T0.MISECD = T1.MISECD"
  ;

  // SQL：部門・分類系共通
  public final static String ID_SQL_BMN_BUMON_WHERE = " and BMNCD = ? ";
  public final static String ID_SQL_BMN_BUMONS_WHERE = " and right('0'||BMNCD,2) in (@) ";
  public final static String ID_SQL_BMN_DAI_WHERE = " and DAICD = ? ";
  public final static String ID_SQL_BMN_DAIS_WHERE = " and right('0'||BMNCD,2)||right('0'||DAICD,2) in (@) ";
  public final static String ID_SQL_BMN_CHU_WHERE = " and CHUCD = ? ";
  public final static String ID_SQL_BMN_CHUS_WHERE = " and right('0'||BMNCD,2)||right('0'||DAICD,2)||right('0'||CHUCD,2) in (@) ";
  public final static String ID_SQL_BMN_SHO_WHERE = " and SHOCD = ? ";
  public final static String ID_SQL_BMN_SHOS_WHERE = " and right('0'||BMNCD,2)||right('0'||DAICD,2)||right('0'||CHUCD,2)||right('0'||SHOCD,2) in (@) ";
  public final static String ID_SQL_BMN_SSHO_WHERE = " and SSHOCD = ? ";

  public final static String ID_SQL_BMN_DAI_WHERE_M = " and DAICD <> '00'";
  public final static String ID_SQL_BMN_CHU_WHERE_M = " and CHUCD <> '00'";
  public final static String ID_SQL_BMN_SHO_WHERE_M = " and SHOCD <> '00'";

  // SQL：部門
  public final static String ID_SQL_BUMON_HEAD =
      "select VALUE, TEXT, TEXT as TEXT2 from (values ROW('" + Values.ALL.getVal() + "','" + Values.ALL.getTxt() + "')) as X(" + VAL + "," + TXT + ") union all ";
  public final static String ID_SQL_BUMON_HEAD2 = "select VALUE, TEXT, TEXT as TEXT2 from (values ROW('" + Values.NONE.getVal() + "', '　')) as X(value, TEXT) union all ";
  public final static String ID_SQL_BUMON_HEAD3 =
      "select VALUE, TEXT, TEXT as TEXT2 from (values ROW('" + Values.NONE.getVal() + "', '" + Values.BUMON_ALL_TOK.getTxt() + "')) as X(value, TEXT) union all ";
  public final static String ID_SQL_BUMON_CMN = "select right('0'||BMNCD,2) as VALUE, right('0'||BMNCD,2)||'" + SEPARATOR + "'||COALESCE(rtrim(max(BMNKN)),'') as TEXT, rtrim(max(BMNKN)) as TEXT2 ";
  public final static String ID_SQL_BUMON_CMN_C =
      "select right('0'||BMNCD,2) as VALUE, right('0'||BMNCD,2)||'" + SEPARATOR + "'||COALESCE(rtrim(BMNKN),'') as TEXT, rtrim(BMNKN) as TEXT2, INAMS.MSTBMN.* ";
  public final static String ID_SQL_BUMON = ID_SQL_BUMON_CMN + "from INAMS.MSTBMN";
  public final static String ID_SQL_BUMON_C = ID_SQL_BUMON_CMN_C + "from INAMS.MSTBMN";
  public final static String ID_SQL_BUMON_FOOTER = " group by BMNCD order by VALUE";
  public final static String ID_SQL_BUMON_FOOTER_C = " order by VALUE";
  public final static String ID_SQL_BUMON_UNSELECTED =
      " select VALUE, TEXT, TEXT as TEXT2 from (values ROW(- 1, '')) as X(value, TEXT) union all select BMNCD as value, right ('0' || BMNCD, 2) || '-' || RTRIM(RTRIM(MAX(BMNKN)), '　') as TEXT, RTRIM(RTRIM(MAX(BMNKN)), '　') as TEXT2 from INAMS.MSTBMN group by BMNCD order by value";
  public final static String ID_SQL_BUMON_ZIZEN =
      " select VALUE , TEXT , TEXT as TEXT2 from (values ROW('-1', '　')) as X(value, TEXT) union all select right ('0' || T1.BMNCD, 2) as VALUE , right ('0' || T1.BMNCD, 2) || '-' || rtrim(max(T1.BMNKN)) as TEXT ,rtrim(max(T1.BMNKN)) as TEXT2  from INAMS.MSTBMN T1 left join INATK.TOKTJ_TEN T2 on T2.TENCD = ? and T2.BMNCD = T1.BMNCD where T2.LSTNO = ? and T2.TENCD = ? and T2.BMNCD = T1.BMNCD group by   T1.BMNCD order by VALUE";// TODO
  // 定数を入れている為後で修正

  // SQL：大分類
  public final static String ID_SQL_DAI_BUN_HEAD = "select VALUE, TEXT, TEXT as TEXT2 from (values ROW('" + Values.NONE.getVal() + "', 'すべて')) as X(value, TEXT) union all ";
  public final static String ID_SQL_DAI_BUN_HEAD2 = "select VALUE, TEXT, TEXT as TEXT2 from (values ROW('" + Values.NONE.getVal() + "', '　')) as X(value, TEXT) union all ";
  public final static String ID_SQL_DAI_BUN_CMN = "select right('0'||BMNCD,2)||right('0'||DAICD,2) as VALUE, right('0'||BMNCD,2)||right('0'||DAICD,2)||'" + SEPARATOR
      + "'||COALESCE(rtrim(rtrim(max(DAIBRUIKN))),'') as TEXT, rtrim(rtrim(max(DAIBRUIKN))) as TEXT2 ";
  public final static String ID_SQL_DAI_BUN_SHC =
      "select right('0'||DAICD,2) as VALUE, right('0'||DAICD,2) ||'" + SEPARATOR + "'||COALESCE(rtrim(rtrim(max(DAIBRUIKN))),'') as TEXT, rtrim(rtrim(max(DAIBRUIKN))) as TEXT2 ";
  public final static String ID_SQL_DAI_BUN_SHC2 =
      "select right('0'||BMNCD,2)||right('0'||DAICD,2) as VALUE, right('0'||DAICD,2) ||'" + SEPARATOR + "'||COALESCE(rtrim(rtrim(max(DAIBRUIKN))),'') as TEXT, rtrim(rtrim(max(DAIBRUIKN))) as TEXT2 ";
  public final static String ID_SQL_DAI_BUN_SHC3 =
      "select right('0'||BMNCD,2)||right('0'||DAICD,2) as VALUE, DAICD ||'" + SEPARATOR + "'||COALESCE(rtrim(rtrim(max(DAIBRUIKN))),'') as TEXT, rtrim(rtrim(max(DAIBRUIKN))) as TEXT2 ";
  public final static String ID_SQL_DAI_BUN = ID_SQL_DAI_BUN_CMN + "from INAMS.MSTDAIBRUI ";
  public final static String ID_SQL_DAI_BUN_S = ID_SQL_DAI_BUN_SHC + "from INAMS.MSTDAIBRUI ";
  public final static String ID_SQL_DAI_BUN_S2 = ID_SQL_DAI_BUN_SHC2 + "from INAMS.MSTDAIBRUI ";
  public final static String ID_SQL_DAI_BUN_S3 = ID_SQL_DAI_BUN_SHC3 + "from INAMS.MSTDAIBRUI ";
  public final static String ID_SQL_DAI_BUN_NEZ = ID_SQL_DAI_BUN_CMN + "from INAMS.MSTDAIBRUI_NEZ ";
  public final static String ID_SQL_DAI_BUN_URI = ID_SQL_DAI_BUN_CMN + "from INAMS.MSTDAIBRUI_URI ";
  public final static String ID_SQL_DAI_BUN_YOT = ID_SQL_DAI_BUN_CMN + "from INAMS.MSTDAIBRUI_YOT ";
  public final static String ID_SQL_DAI_BUN_FOOTER = " group by BMNCD,DAICD order by VALUE";

  // SQL：中分類
  public final static String ID_SQL_CHU_BUN_HEAD = "select VALUE, TEXT, TEXT as TEXT2 from (values ROW('" + Values.NONE.getVal() + "', 'すべて')) as X(value, TEXT) union all ";
  public final static String ID_SQL_CHU_BUN_HEAD2 = "select VALUE, TEXT, TEXT as TEXT2 from (values ROW('" + Values.NONE.getVal() + "', '　')) as X(value, TEXT) union all ";
  public final static String ID_SQL_CHU_BUN_CMN = "select right('0'||BMNCD,2)||right('0'||DAICD,2)||right('0'||CHUCD,2) as VALUE, right('0'||BMNCD,2)||right('0'||DAICD,2)||right('0'||CHUCD,2)||'"
      + SEPARATOR + "'||COALESCE(rtrim(rtrim(max(CHUBRUIKN))),'') as TEXT, rtrim(rtrim(max(CHUBRUIKN))) as TEXT2 ";
  public final static String ID_SQL_CHU_BUN_SHC =
      "select right('0'||CHUCD,2) as VALUE, right('0'||CHUCD,2) ||'" + SEPARATOR + "'||COALESCE(rtrim(rtrim(max(CHUBRUIKN))),'') as TEXT , rtrim(rtrim(max(CHUBRUIKN))) as TEXT2 ";
  public final static String ID_SQL_CHU_BUN_SHC2 = "select right('0'||BMNCD,2)||right('0'||DAICD,2)||right('0'||CHUCD,2) as VALUE, right('0'||CHUCD,2) ||'" + SEPARATOR
      + "'||COALESCE(rtrim(rtrim(max(CHUBRUIKN))),'') as TEXT , rtrim(rtrim(max(CHUBRUIKN))) as TEXT2 ";
  public final static String ID_SQL_CHU_BUN_SHC3 = "select right('0'||BMNCD,2)||right('0'||DAICD,2)||right('0'||CHUCD,2) as VALUE, CHUCD ||'" + SEPARATOR
      + "'||COALESCE(rtrim(rtrim(max(CHUBRUIKN))),'') as TEXT , rtrim(rtrim(max(CHUBRUIKN))) as TEXT2 ";
  public final static String ID_SQL_CHU_BUN = ID_SQL_CHU_BUN_CMN + "from INAMS.MSTCHUBRUI";
  public final static String ID_SQL_CHU_BUN_S = ID_SQL_CHU_BUN_SHC + "from INAMS.MSTCHUBRUI";
  public final static String ID_SQL_CHU_BUN_S2 = ID_SQL_CHU_BUN_SHC2 + "from INAMS.MSTCHUBRUI";
  public final static String ID_SQL_CHU_BUN_S3 = ID_SQL_CHU_BUN_SHC3 + "from INAMS.MSTCHUBRUI";
  public final static String ID_SQL_CHU_BUN_NEZ = ID_SQL_CHU_BUN_CMN + "from INAMS.MSTCHUBRUI_NEZ";
  public final static String ID_SQL_CHU_BUN_URI = ID_SQL_CHU_BUN_CMN + "from INAMS.MSTCHUBRUI_URI";
  public final static String ID_SQL_CHU_BUN_YOT = ID_SQL_CHU_BUN_CMN + "from INAMS.MSTCHUBRUI_YOT";
  public final static String ID_SQL_CHU_BUN_FOOTER = " group by BMNCD,DAICD,CHUCD order by VALUE";

  // SQL：小分類
  public final static String ID_SQL_SHO_BUN_HEAD = "select VALUE, TEXT, TEXT as TEXT2 from (values ROW('" + Values.NONE.getVal() + "', 'すべて')) as X(value, TEXT) union all ";
  public final static String ID_SQL_SHO_BUN_CMN =
      "select right('0'||BMNCD,2)||right('0'||DAICD,2)||right('0'||CHUCD,2)||right('0'||SHOCD,2) as VALUE, right('0'||BMNCD,2)||right('0'||DAICD,2)||right('0'||CHUCD,2)||right('0'||SHOCD,2)||'"
          + SEPARATOR + "'||COALESCE(rtrim(rtrim(max(SHOBRUIKN))),'') as TEXT, rtrim(rtrim(max(SHOBRUIKN))) as TEXT2 ";
  public final static String ID_SQL_SHO_BUN = ID_SQL_SHO_BUN_CMN + "from INAMS.MSTSHOBRUI";
  public final static String ID_SQL_SHO_BUN_NEZ = ID_SQL_SHO_BUN_CMN + "from INAMS.MSTSHOBRUI_NEZ";
  public final static String ID_SQL_SHO_BUN_URI = ID_SQL_SHO_BUN_CMN + "from INAMS.MSTSHOBRUI_URI";
  public final static String ID_SQL_SHO_BUN_YOT = ID_SQL_SHO_BUN_CMN + "from INAMS.MSTSHOBRUI_YOT";
  public final static String ID_SQL_SHO_BUN_FOOTER = " group by BMNCD,DAICD,CHUCD,SHOCD order by VALUE";

  // SQL：小小分類
  public final static String ID_SQL_SSHO_BUN_HEAD = "select VALUE, TEXT, TEXT as TEXT2 from (values ROW('" + Values.NONE.getVal() + "', 'すべて')) as X(value, TEXT) union all ";
  public final static String ID_SQL_SSHO_BUN =
      "select right('0'||BMNCD,2)||right('0'||DAICD,2)||right('0'||CHUCD,2)||right('0'||SHOCD,2)||SSHOCD as VALUE, right('0'||BMNCD,2)||right('0'||DAICD,2)||right('0'||CHUCD,2)||right('0'||SHOCD,2)||'"
          + SEPARATOR + "'||COALESCE(rtrim(rtrim(max(SSHOBRUIKN))),'') as TEXT, rtrim(rtrim(max(SSHOBRUIKN))) as TEXT2 from INAMS.MSTSSHOBRUI";
  public final static String ID_SQL_SSHO_BUN_FOOTER = " group by BMNCD,DAICD,CHUCD,SHOCD order by VALUE";

  // SQL:部門マスタ存在チェック

  // SQL:部門マスタ存在チェック
  public final static String ID_SQL_BMN_CHK_HEAD =
      "with INP as (select BMNCD, DAICD, CHUCD, SHOCD, SSHOCD from (values ROW(cast(? as signed), cast(? as signed), cast(? as signed), cast(? as signed), cast(? as signed))) as X(BMNCD, DAICD, CHUCD, SHOCD, SSHOCD))";
  public final static String ID_SQL_BMN_CHK = ID_SQL_BMN_CHK_HEAD
      + "select COALESCE(M1.BMNCD || '', '0') AS '1', COALESCE(M2.DAICD || '', '0') AS '2', COALESCE(M3.CHUCD || '', '0') AS '3', COALESCE(M4.SHOCD || '', '0') AS '4', COALESCE(M5.SSHOCD || '', '0') AS '5' from INP T1 "
      + " left outer join INAMS.MSTBMN M1 on T1.BMNCD = M1.BMNCD and COALESCE(M1.UPDKBN,0) <> 1"
      + " left outer join INAMS.MSTDAIBRUI@ M2  on T1.BMNCD = M2.BMNCD and T1.DAICD = M2.DAICD and COALESCE(M2.UPDKBN,0) <> 1"
      + " left outer join INAMS.MSTCHUBRUI@ M3  on T1.BMNCD = M3.BMNCD and T1.DAICD = M3.DAICD and T1.CHUCD = M3.CHUCD and COALESCE(M3.UPDKBN,0) <> 1"
      + " left outer join INAMS.MSTSHOBRUI@ M4  on T1.BMNCD = M4.BMNCD and T1.DAICD = M4.DAICD and T1.CHUCD = M4.CHUCD and T1.SHOCD = M4.SHOCD and COALESCE(M4.UPDKBN,0) <> 1"
      + " left outer join INAMS.MSTSSHOBRUI M5 on T1.BMNCD = M5.BMNCD and T1.DAICD = M5.DAICD and T1.CHUCD = M5.CHUCD and T1.SHOCD = M5.SHOCD and T1.SSHOCD = M5.SSHOCD and COALESCE(M5.UPDKBN,0) <> 1";
  // SQL:値付分類コードチェック
  public final static String ID_SQL_KRYO_CHK_HEAD =
      "with INP as (select BMNCD, DAICD, CHUCD, SHOCD from (values ROW(cast(? as CHAR), cast(? as CHAR), cast(? as CHAR), cast(? as CHAR))) as X(BMNCD, DAICD, CHUCD, SHOCD))";
  public final static String ID_SQL_KRYO_CHK =
      ID_SQL_KRYO_CHK_HEAD + "select COALESCE(M1.BMNCD || '', '') AS '1', COALESCE(M2.DAICD || '', '') AS '2', COALESCE(M3.CHUCD || '', '') AS '3', COALESCE(M4.SHOCD || '', '') AS '4' from INP T1 "
          + " left outer join INAMS.MSTBMN M1 on T1.BMNCD = M1.BMNCD and COALESCE(M1.UPDKBN,0) <> 1"
          + " left outer join INAMS.MSTDAIBRUI@ M2  on T1.BMNCD = M2.BMNCD and T1.DAICD = M2.DAICD and COALESCE(M2.UPDKBN,0) <> 1"
          + " left outer join INAMS.MSTCHUBRUI@ M3  on T1.BMNCD = M3.BMNCD and T1.DAICD = M3.DAICD and T1.CHUCD = M3.CHUCD and COALESCE(M3.UPDKBN,0) <> 1"
          + " left outer join INAMS.MSTSHOBRUI@ M4  on T1.BMNCD = M4.BMNCD and T1.DAICD = M4.DAICD and T1.CHUCD = M4.CHUCD and T1.SHOCD = M4.SHOCD and COALESCE(M4.UPDKBN,0) <> 1";

  // SQL:商品マスタ
  /** 共通（INAMS.MSTSHN） */
  public final static String ID_SQL_SHN_CHK_UPDATECNT =
      "select CNT as VALUE from (select COALESCE(max(UPDATECNT), 0) as CNT from INAAD.SYSSHNCOUNT where UPDATEDT = ?) AS T1 where CNT < (select COALESCE(MAXSU, 0) from INAAD.SYSSHNGENDOSU)";
  public final static String ID_SQL_SHN_CHK_UPDATECNT2 =
      "select (select COALESCE(MAXSU, 0) from INAAD.SYSSHNGENDOSU) - CNT as VALUE from (select COALESCE(max(UPDATECNT), 0) as CNT from INAAD.SYSSHNCOUNT where UPDATEDT = ?) AS MT ";
  public final static String ID_SQL_SHN_SHNCD = "select SUBSTRING(right ('0000000' || RTRIM(SHNCD), 8), 1, 4) || '" + SEPARATOR
      + "' || SUBSTRING(right ('0000000' || RTRIM(SHNCD), 8), 5, 4) AS F1,SHNKN as F2,right ('0000000' || RTRIM(SHNCD), 8) as VALUE from INAMS.MSTSHN";
  public final static String ID_SQL_SHN_SHNCD_TAIL = " order by VALUE LIMIT 1001 ";
  // SQL：商品名（漢字） TODO:
  public final static String ID_SQL_SHNKN = "select case when SHNKN is null or SHNKN = '' then '　' else SHNKN end as F1 from INAMS.MSTSHN " + DefineReport.ID_SQL_CMN_WHERE + " and SHNCD = ?";
  // SQL：商品一覧 TODO:
  public final static String ID_SQL_SHOHIN = DefineReport.ID_SQL_CMN_WEEK
      + " select T1.UPDKBN, T1.NDTSET as F1, right ('00' || T1.CATALGNO, 2) as F2, trim(left(T1.SHNCD, 4) || '-' || SUBSTR(T1.SHNCD, 5)) as F3, T1.SHNKN as F4, T1.HTDT as F5, case when T1.UKESTDT = T1.UKEEDDT then T1.UKESTDT || W1.JWEEK else T1.UKESTDT || W1.JWEEK || '～' || T1.UKEEDDT || W2.JWEEK end as F6, case when T1.TENISTDT = T1.TENIEDDT then T1.TENISTDT || W3.JWEEK else T1.TENISTDT || W3.JWEEK || '～' || T1.TENIEDDT || W4.JWEEK end as F7, T1.YOTEISU as F8, T1.GENDOSU as F9, T1.UKESTDT_HIDE as F10, T1.UKEEDDT_HIDE as F11, T1.TENISTDT_HIDE as F12, T1.TENIEDDT_HIDE as F13"
      + " from (select SHN.UPDKBN, case when (select NNDT from INATK.HATYH_NNDT where KKKCD = SHN.KKKCD and SHNCD = SHN.SHNCD LIMIT 1) = 0 then 0 else 1 end as NDTSET, SHN.CATALGNO, SHN.SHNCD, SHNM.SHNKN, SHN.HTDT, SHN.UKESTDT as UKESTDT_HIDE, SHN.UKEEDDT as UKEEDDT_HIDE, SHN.TENISTDT as TENISTDT_HIDE, SHN.TENIEDDT as TENIEDDT_HIDE, DATE_FORMAT(DATE_FORMAT('20' || right ('0' || SHN.UKESTDT, 6), '%Y%m%d'), '%y/%m/%d') as UKESTDT, DAYOFWEEK(DATE_FORMAT('20' || right ('0' || SHN.UKESTDT, 6), '%Y%m%d')) as UKESTDT_WNUM, DATE_FORMAT(DATE_FORMAT('20' || right ('0' || SHN.UKEEDDT, 6), '%Y%m%d'), '%y/%m/%d') as UKEEDDT"
      + ", DAYOFWEEK(DATE_FORMAT('20' || right ('0' || SHN.UKEEDDT, 6), '%Y%m%d')) as UKEEDDT_WNUM, DATE_FORMAT(DATE_FORMAT('20' || right ('0' || SHN.TENISTDT, 6), '%Y%m%d'), '%y/%m/%d') as TENISTDT, DAYOFWEEK(DATE_FORMAT('20' || right ('0' || SHN.TENISTDT, 6), '%Y%m%d')) as TENISTDT_WNUM, DATE_FORMAT(DATE_FORMAT('20' || right ('0' || SHN.TENIEDDT, 6), '%Y%m%d'), '%y/%m/%d') as TENIEDDT, DAYOFWEEK(DATE_FORMAT('20' || right ('0' || SHN.TENIEDDT, 6), '%Y%m%d')) as TENIEDDT_WNUM, SHN.YOTEISU, SHN.GENDOSU"
      + " from INATK.HATYH_SHN SHN left join INAMS.MSTSHN SHNM on SHNM.SHNCD = SHN.SHNCD where SHN.UPDKBN = 0 and SHN.KKKCD = ?) T1 left outer join WEEK W1 on T1.UKESTDT_WNUM = W1.CWEEK left outer join WEEK W2 on T1.UKEEDDT_WNUM = W2.CWEEK left outer join WEEK W3 on T1.TENISTDT_WNUM = W3.CWEEK left outer join WEEK W4 on T1.TENIEDDT_WNUM = W4.CWEEK order by T1.CATALGNO, T1.SHNCD";
  // SQL：商品一覧(発注数項目あり) TODO:
  public final static String ID_SQL_SHOHIN_HTS = DefineReport.ID_SQL_CMN_WEEK
      + "select T1.NGFLG as F1, TRIM(left (T1.SHNCD, 4) || '-' || SUBSTR(T1.SHNCD, 5)) as F2, T1.SHNKN as F3, T1.HTDT as F4, case when T1.UKESTDT = T1.UKEEDDT then T1.UKESTDT || W1.JWEEK else T1.UKESTDT || W1.JWEEK || '～' || T1.UKEEDDT || W2.JWEEK end as F5, case when T1.TENISTDT = T1.TENIEDDT then T1.TENISTDT || W3.JWEEK else T1.TENISTDT || W3.JWEEK || '～' || T1.TENIEDDT || W4.JWEEK end as F6, T1.HTSU_SUM as F7"
      + ", T1.HTSU_T as F8, T1.YOTEISU as F9, T1.GENDOSU as F10, T1.UKESTDT as F11, T1.UKEEDDT as F12, T1.TENISTDT as F13, T1.TENIEDDT as F14, case when T1.NSTDT = T1.NEDDT then T1.NSTDT || W5.JWEEK else T1.NSTDT || W5.JWEEK || '～' || T1.NEDDT || W6.JWEEK end as F15 from (select SHN.NGFLG, SHN.SHNCD, SHNM.SHNKN, SHN.HTDT, DATE_FORMAT(DATE_FORMAT('20' || right ('0' || SHN.UKESTDT, 6), '%Y%m%d'), '%y/%m/%d') as UKESTDT, DAYOFWEEK(DATE_FORMAT('20' || right ('0' || SHN.UKESTDT, 6), '%Y%m%d')) as UKESTDT_WNUM, DATE_FORMAT(DATE_FORMAT('20' || right ('0' || SHN.UKEEDDT, 6), '%Y%m%d'), '%y/%m/%d') as UKEEDDT"
      + ", DAYOFWEEK(DATE_FORMAT('20' || right ('0' || SHN.UKEEDDT, 6), '%Y%m%d')) as UKEEDDT_WNUM, DATE_FORMAT(DATE_FORMAT('20' || right ('0' || SHN.TENISTDT, 6), '%Y%m%d'), '%y/%m/%d') as TENISTDT, DAYOFWEEK(DATE_FORMAT('20' || right ('0' || SHN.TENISTDT, 6), '%Y%m%d')) as TENISTDT_WNUM, DATE_FORMAT(DATE_FORMAT('20' || right ('0' || SHN.TENIEDDT, 6), '%Y%m%d'), '%y/%m/%d') as TENIEDDT, DAYOFWEEK(DATE_FORMAT('20' || right ('0' || SHN.TENIEDDT, 6), '%Y%m%d')) as TENIEDDT_WNUM, DATE_FORMAT(DATE_FORMAT('20' || right ('0' || YHN.NSDT, 6), '%Y%m%d'), '%y/%m/%d') as NSTDT"
      + ", DAYOFWEEK(DATE_FORMAT('20' || right ('0' || YHN.NSDT, 6), '%Y%m%d')) as NSTDT_WNUM, DATE_FORMAT(DATE_FORMAT('20' || right ('0' || YHN.NEDT, 6), '%Y%m%d'), '%y/%m/%d') as NEDDT, DAYOFWEEK(DATE_FORMAT('20' || right ('0' || YHN.NEDT, 6), '%Y%m%d')) as NEDDT_WNUM, HTEN.HTSU_SUM, HTEN.HTSU_T, SHN.YOTEISU, SHN.GENDOSU, SHN.CATALGNO from INATK.HATYH_SHN SHN left join INAMS.MSTSHN SHNM on SHNM.SHNCD = SHN.SHNCD left join (select KKKCD, SHNCD, SUM(case when INPUTDT < ? then HTSU else 0 end) as HTSU_SUM, SUM(case when INPUTDT = ? then HTSU else 0 end) as HTSU_T"
      + " from INATK.HATYH_TEN group by KKKCD, SHNCD) HTEN on HTEN.KKKCD = SHN.KKKCD and HTEN.SHNCD = SHN.SHNCD left join (select KKKCD, SHNCD, MAX(NNDT) as NEDT, MIN(NNDT) as NSDT from INATK.HATYH_NNDT group by KKKCD, SHNCD) YHN on YHN.KKKCD = SHN.KKKCD and YHN.SHNCD = SHN.SHNCD where SHN.UPDKBN = 0 and SHN.KKKCD = ? ) T1 left outer join WEEK W1 on T1.UKESTDT_WNUM = W1.CWEEK left outer join WEEK W2 on T1.UKEEDDT_WNUM = W2.CWEEK left outer join WEEK W3 on T1.TENISTDT_WNUM = W3.CWEEK left outer join WEEK W4 on T1.TENIEDDT_WNUM = W4.CWEEK"
      + " left outer join WEEK W5 on T1.NSTDT_WNUM = W5.CWEEK left outer join WEEK W6 on T1.NEDDT_WNUM = W6.CWEEK order by T1.CATALGNO, T1.SHNCD";
  // SQL：商品名（漢字） TODO:
  public final static String ID_SQL_SHNKN_TOK =
      "SELECT T2.F1,T2.F2,T2.F3,T2.F4,CASE WHEN T3.ZEIRT IS NULL THEN T2.F5 ELSE TRUNCATE(CAST(T2.F5 AS DECIMAL(8,2)) + (CAST(T2.F5 AS DECIMAL(8,2)) * (T3.ZEIRT / 100)), 0) END F5,T2.F5 AS F6 FROM(SELECT SHNCD,? AS SHNKBN,CASE WHEN (HS_GENKAAM <> 0 AND HS_GENKAAM IS NOT NULL) AND (HS_BAIKAAM <> 0 AND HS_BAIKAAM IS NOT NULL) AND (HS_IRISU <> 0 AND HS_IRISU IS NOT NULL) THEN 1 ELSE 0 END KBN,UPDKBN FROM INAMS.MSTSHN) T1,(SELECT 0 AS KBN,SHNCD,SHNKN AS F1,RG_WAPNFLG AS F2,RG_IRISU AS F3,RG_GENKAAM AS F4,RG_BAIKAAM AS F5 FROM INAMS.MSTSHN UNION ALL SELECT 1 AS KBN,SHNCD,SHNKN AS F1,CASE WHEN ? = '2' THEN CASE WHEN TRIM(HS_WAPNFLG) = '' OR HS_WAPNFLG IS NULL THEN '-1' ELSE HS_WAPNFLG END ELSE CASE WHEN TRIM(HP_SWAPNFLG) = '' OR HP_SWAPNFLG IS NULL THEN '-1' ELSE HP_SWAPNFLG END END AS F2,HS_IRISU AS F3,HS_GENKAAM AS F4,HS_BAIKAAM AS F5 FROM INAMS.MSTSHN) T2,(SELECT T1.SHNCD,T2.ZEIRT FROM(SELECT T1.SHNCD,CASE WHEN T1.ZEIKBN <> '3' THEN CASE WHEN T1.ZEIKBN <> '0' THEN NULL WHEN T1.ZEIKBN = '0' AND SUBSTR(T1.ZEIRTHENKODT,3,6) <= ? THEN T1.ZEIRTKBN WHEN T1.ZEIKBN = '0' AND SUBSTR(T1.ZEIRTHENKODT,3,6) > ? THEN T1.ZEIRTKBN_OLD END ELSE CASE WHEN T2.ZEIKBN <> '0' THEN NULL WHEN T2.ZEIKBN = '0' AND SUBSTR(T2.ZEIRTHENKODT,3,6) <= ? THEN T2.ZEIRTKBN WHEN T2.ZEIKBN = '0' AND SUBSTR(T2.ZEIRTHENKODT,3,6) > ? THEN T2.ZEIRTKBN_OLD END END ZEIRTKBN FROM INAMS.MSTSHN T1 LEFT JOIN INAMS.MSTBMN T2 ON SUBSTR(T1.SHNCD,1,2) = T2.BMNCD) T1 LEFT JOIN INAMS.MSTZEIRT T2 ON T1.ZEIRTKBN = T2.ZEIRTKBN) T3 WHERE T1.SHNCD=? AND T1.UPDKBN=0 AND T1.SHNCD = T2.SHNCD AND ((T1.SHNKBN IN (1, 2) AND T1.KBN = T2.KBN) OR (T1.SHNKBN = 0 AND T1.SHNKBN = T2.KBN)) AND T1.SHNCD=T3.SHNCD"; // SQL：商品名（漢字）
  // TODO:
  public final static String ID_SQL_SHNKN_TOK2 =
      "SELECT DISTINCT T2.F1,T2.F2,T1.SHNCD AS F3 FROM (SELECT SHNCD,CASE WHEN (HS_GENKAAM<>0 AND HS_GENKAAM IS NOT NULL) AND (HS_BAIKAAM<>0 AND HS_BAIKAAM IS NOT NULL) AND (HS_IRISU<>0 AND HS_IRISU IS NOT NULL) THEN 1 ELSE 0 END KBN,UPDKBN FROM INAMS.MSTSHN) T1,(SELECT 0 AS KBN,SHNCD,SHNKN AS F1,RG_GENKAAM AS F2 FROM INAMS.MSTSHN UNION ALL SELECT 1 AS KBN,SHNCD,SHNKN AS F1,HS_GENKAAM AS F2 FROM INAMS.MSTSHN) T2 WHERE T1.SHNCD=? AND T1.UPDKBN=0 AND T1.SHNCD=T2.SHNCD ";
  public final static String ID_SQL_SHNKN_PC = "select case when PCARDKN is null or PCARDKN = '' then '　' else PCARDKN end as F1 from INAMS.MSTSHN " + DefineReport.ID_SQL_CMN_WHERE + " and SHNCD = ?";

  // SQL：冷凍食品（新規コード） TODO:
  public final static String ID_SQL_TOKRS = "select MTSH.SANCHIKN as F3,  MTSH.SHNKN as F4,  MTSH.KIKKN as F5, case when MTSH.HS_IRISU is NOT NULL then MTSH.HS_IRISU else MTSH.RG_IRISU END as F6 , "
      + DefineReport.ID_SQL_TOKBAIKA_COL_SOU.replaceAll("@BAIKA", "MTSH.RG_BAIKAAM").replaceAll("@DT", "?")
      + " as F7, MTSH.RG_BAIKAAM as F8, MTSH.RG_GENKAAM as F9, MSBR.SHOBRUIKN as F13 from INAMS.MSTSHN MTSH left join INAMS.MSTMAKER MSMK on MTSH.MAKERCD = MSMK.MAKERCD left join INAMS.MSTSHOBRUI MSBR on MTSH.DAICD = MSBR.DAICD and MTSH.CHUCD = MSBR.CHUCD and MTSH.SHOCD = MSBR.SHOCD and MSBR.BMNCD = MTSH.BMNCD left outer join INAMS.MSTSHN M0 on M0.SHNCD = MTSH.SHNCD and COALESCE(M0.UPDKBN, 0) <> 1 left outer join INAMS.MSTBMN M1 on M1.BMNCD = MTSH.BMNCD and COALESCE(M1.UPDKBN, 0) <> 1 left outer join INAMS.MSTZEIRT M2 on M2.ZEIRTKBN = M0.ZEIRTKBN and COALESCE(M2.UPDKBN, 0) <> 1 left outer join INAMS.MSTZEIRT M3 on M3.ZEIRTKBN = M0.ZEIRTKBN_OLD and COALESCE(M3.UPDKBN, 0) <> 1 left outer join INAMS.MSTZEIRT M4 on M4.ZEIRTKBN = M1.ZEIRTKBN and COALESCE(M4.UPDKBN, 0) <> 1 left outer join INAMS.MSTZEIRT M5 on M5.ZEIRTKBN = M1.ZEIRTKBN_OLD and COALESCE(M5.UPDKBN, 0) <> 1 where MTSH.UPDKBN <> 1 and MTSH.SHNCD = ?";

  // SQL：冷凍食品（新売価） TODO:
  public final static String ID_SQL_BAIKAAM = "select " + DefineReport.ID_SQL_TOKBAIKA_COL_SOU.replaceAll("@BAIKA", "?").replaceAll("@DT", "TRSH.HBSTDT")
      + " as F11 from INATK.TOKRS_SHN TRSH left join INAMS.MSTSHN MTSH on TRSH.SHNCD = MTSH.SHNCD left join INAMS.MSTSHOBRUI MSBR on MTSH.DAICD = MSBR.DAICD and MTSH.CHUCD = MSBR.CHUCD and MTSH.SHOCD = MSBR.SHOCD and MSBR.BMNCD = MTSH.BMNCD left outer join INAMS.MSTSHN M0 on M0.SHNCD = TRSH.SHNCD and COALESCE(M0.UPDKBN, 0) <> 1 left outer join INAMS.MSTBMN M1 on M1.BMNCD = TRSH.BMNCD and COALESCE(M1.UPDKBN, 0) <> 1 left outer join INAMS.MSTZEIRT M2 on M2.ZEIRTKBN = M0.ZEIRTKBN and COALESCE(M2.UPDKBN, 0) <> 1 left outer join INAMS.MSTZEIRT M3 on M3.ZEIRTKBN = M0.ZEIRTKBN_OLD and COALESCE(M3.UPDKBN, 0) <> 1 left outer join INAMS.MSTZEIRT M4 on M4.ZEIRTKBN = M1.ZEIRTKBN and COALESCE(M4.UPDKBN, 0) <> 1 left outer join INAMS.MSTZEIRT M5 on M5.ZEIRTKBN = M1.ZEIRTKBN_OLD and COALESCE(M5.UPDKBN, 0) <> 1 where TRSH.HBSTDT = ? and TRSH.BMNCD = ? and TRSH.WRITUKBN = ? and TRSH.SEICUTKBN = ? and TRSH.DUMMYCD = ? and TRSH.UPDKBN <> 1 and TRSH.SHNCD = ?";

  // SQL：税率区分
  public final static String ID_SQL_ZEIRT = "select VALUE, TEXT from (values ROW('" + Values.NONE.getVal()
      + "', '　')) as X(value, TEXT) union all select CAST(ZRT.ZEIRTKBN AS CHAR) as value, ZRT.ZEIRTKBN || '-' ||COALESCE(TRIM(ZRT.ZEIRTKN),'') as TEXT from INAMS.MSTZEIRT ZRT "
      + DefineReport.ID_SQL_CMN_WHERE + " order by value";

  // SQL：分類区分
  public final static String ID_SQL_BUNRUI = "select VALUE, TEXT from (values ROW(1, '標準'), ROW(2, '用途'), ROW(3, '売場')) as X(value, TEXT) order by value";

  // SQL：商品登録限度数管理テーブル
  public final static String ID_SQL_DE_MAXSU = "select DE_MAXSU as VALUE from INAAD.SYSSHNGENDOSU";
  public final static String ID_SQL_MAXSU = "select MAXSU as VALUE from INAAD.SYSSHNGENDOSU";

  // SQL：店舗名称(漢字)
  public final static String ID_SQL_TENKN = "select TENCD as VALUE, TENKN as TEXT from INAMS.MSTTEN  " + DefineReport.ID_SQL_CMN_WHERE + DefineReport.ID_SQL_CMN_FOOTER;
  public final static String ID_SQL_TENKN2 =
      "select TENCD as VALUE, right ('00' || TENCD, 3) || '-' ||COALESCE(TENKN,'') as TEXT from INAMS.MSTTEN " + DefineReport.ID_SQL_CMN_WHERE + " and TENCD = ? order by right ('00' || TENCD,3 )";
  public final static String ID_SQL_TENKN_HEAD =
      "select TENCD as VALUE, right ('00' || TENCD, 3) || '-' ||COALESCE(TENKN,'') as TEXT from INAMS.MSTTEN " + DefineReport.ID_SQL_CMN_WHERE + "order by right ('00' || TENCD,3 )";
  public final static String ID_SQL_TENKN_HEAD2 =
      "select right ('00' || T1.TENCD, 3) as VALUE, right ('00' || T1.TENCD, 3) || '-' || T2.TENKN as TEXT from INATK.TOKTG_QATEN T1 left join INAMS.MSTTEN T2 on T2.TENCD = T1.TENCD where MOYSKBN = ? and T1.MOYSSTDT = ? and T1.MOYSRBAN = ? and T1.KYOSEIFLG = ? and T1.TENGPCD = ? and T2.TENCD = T1.TENCD order by VALUE";

  // SQL：センターコード
  public final static String ID_SQL_CENTER_HEAD = "select VALUE, TEXT from (values ROW('" + Values.NONE.getVal() + "', '　')) as X(value, TEXT) union all ";
  public final static String ID_SQL_CENTER =
      "SELECT DISTINCT CENTERCD AS VALUE, CENTERCD TEXT FROM INAORR.ORRCOURSEMASTER WHERE DATE_FORMAT(EFFECTIVESTARTDATE,'%Y%m%d') <= ? AND DATE_FORMAT(EFFECTIVEENDDATE,'%Y%m%d') >= ? AND LOGICALDELFLG = "
          + DefineReport.ValUpdkbn.NML.getVal() + " ORDER BY VALUE";

  // SQL：便コード
  public final static String ID_SQL_SUPPLYNO_HEAD = "select VALUE, TEXT from (values ROW('" + Values.NONE.getVal() + "', '　')) as X(value, TEXT) union all ";
  public final static String ID_SQL_SUPPLYNO = "SELECT DISTINCT SUPPLYNO AS VALUE , SUPPLYNO AS TEXT FROM INAORR.ORRCOURSEMASTER ";

  // SQL：商品区分
  public final static String ID_SQL_SHNKBN =
      "select MEISHOKBN as KBN, MEISHOCD as VALUE, MEISHOKBNKN as KTEXT, MEISHOCD || '-' ||COALESCE(RTRIM(NMKN),'') as TEXT, RTRIM(NMKN) as TEXT2, TNMKN as STEXT, NMAN as KANA from INAMS.MSTMEISHO where MEISHOKBN = '10502' and MEISHOCD = 0 order by KBN, value, TEXT";

  // SQL：メーカー
  /** 共通（INAMS.MSTMAKER） */
  public final static String ID_SQL_MAKER = "select case when (DMAKERCD = MAKERCD or DMAKERCD is null) then '代表' end as F1, MAKERCD as F2, MAKERKN as F3, DMAKERCD as F4 from INAMS.MSTMAKER";
  public final static String ID_SQL_MAKER_FOOTER = " order by MAKERCD";

  // SQL：仕入先 TODO:
  /** 共通（INAMS.MSTSIR） */
  public final static String ID_SQL_SIR = "select SIRCD as F1, SIRAN as F2, SIRKN as F3, case when EDI_RKBN = 1 then 'EDIあり' end as F4, DF_IDENKBN as F5 from INAMS.MSTSIR";
  public final static String ID_SQL_SIR_ = "select * from INAMS.MSTSIR " + DefineReport.ID_SQL_CMN_WHERE + " and SIRCD = ?";
  public final static String ID_SQL_SIR_FOOTER = " order by SIRCD";

  // SQL：実仕入先 TODO:
  /** 共通（INAMS.MSTSIR） */
  public final static String ID_SQL_ZITSIR = ID_SQL_GRD_CMN
      + " select T1.IDX, T2.RSIRCD, T3.SIRKN as SIRKN_R from T1 left join (select ROW_NUMBER() over (order by SIRCD) as IDX, MSTFR.* from INAMS.MSTFUKUSUSIR_R AS MSTFR where SIRCD = ? and UPDKBN = "
      + DefineReport.ValUpdkbn.NML.getVal() + " order by SIRCD LIMIT @M ) T2 on T1.IDX = T2.SEQNO" + " left join INAMS.MSTSIR T3 on T3.SIRCD = T2.RSIRCD order by T1.IDX";

  // SQL：複数仕入先店舗一覧 TODO:
  /** 共通（INAMS.MSTFUKUSUSIR_T） */
  public final static String ID_SQL_FSTNEPO =
      "select TEN.TENCD, TEN.TENKN, FST.SEQNO from INAMS.MSTTEN TEN left join (select TENCD, MAX(SEQNO) as SEQNO from INAMS.MSTFUKUSUSIR_T where UPDKBN = 0 and SIRCD = ? group by SIRCD, TENCD) FST on FST.TENCD = TEN.TENCD where TEN.UPDKBN = "
          + DefineReport.ValUpdkbn.NML.getVal() + " order by TEN.TENCD";

  // SQL：配送パターン TODO:
  /** 共通（INAMS.MSTHSPTN） */
  public final static String ID_SQL_HSPTN = "select HSPTN as F1, HSPTNKN as F2, right ('000' || CENTERCD, 3) as F3, right ('000' || TRIM(YCENTERCD), 3) as F4 from INAMS.MSTHSPTN";
  public final static String ID_SQL_HSPTN_FOOTER = " order by HSPTN";

  // SQL：配送パターン(仕入先マスタ用) TODO:
  /** 共通（INAMS.MSTHSPTN） */
  // public final static String ID_SQL_HSPTN_LIST = "with T1(IDX) as (select 1
  // from SYSIBM.SYSDUMMY1 union all select IDX + 1 from T1 where IDX < @M)
  // select T1.IDX, T2.HSPTN, T2.TENDENFLG, T2.VANKBN, T2.UNYOKBN, T2.DENPKBN,
  // T2.SHUHKBN, T2.PICKDKBN, T2.PICKLKBN, T2.WAPNKBN, T2.IDENPKBN,
  // T2.KAKOSJKBN, T2.RYUTSUKBN, T2.ZDENPKBN, T2.ZSHUHKBN, T2.ZPICKDKBN,
  // T2.ZPICKLKBN, T2.RSIRCD, T2.YKNSHKBN, T2.YDENPKBN, T2.DSHUHKBN from T1
  // left join (select ROW_NUMBER() over (order by SIRCD, HSPTN) as IDX, *
  // from (select HSPS.SIRCD, HSP.HSPTN, HSPS.TENDENFLG, HSPS.VANKBN,
  // HSPS.UNYOKBN, HSPS.DENPKBN, HSPS.SHUHKBN, HSPS.PICKDKBN, HSPS.PICKLKBN,
  // HSPS.WAPNKBN, HSPS.IDENPKBN, HSPS.KAKOSJKBN, HSPS.RYUTSUKBN,
  // HSPS.ZDENPKBN, HSPS.ZSHUHKBN, HSPS.ZPICKDKBN, HSPS.ZPICKLKBN,
  // HSPS.RSIRCD, HSPS.YKNSHKBN, HSPS.YDENPKBN, HSPS.DSHUHKBN from
  // INAMS.MSTHSPTNSIR HSPS left join INAMS.MSTHSPTN HSP on HSP.HSPTN =
  // HSPS.HSPTN where HSPS.SIRCD = ? order by HSPS.SIRCD, HSP.HSPTN)) T2 on
  // T1.IDX = T2.IDX order by T1.IDX, T2.SIRCD, T2.HSPTN";
  public final static String ID_SQL_HSPTN_LIST =
      "WITH RECURSIVE T1(IDX) as (select 1 from (SELECT 1 AS DUMMY) DUMMY UNION ALL SELECT IDX+1 FROM T1 WHERE IDX < @M ) select T1.IDX, T2.HSPTN, T2.HSPTNKN, right ('000' || T2.CENTERCD, 3) as CENTERCD, right ('000' || TRIM(T2.YCENTERCD), 3) as YCENTERCD, T2.TENDENFLG || '-' || RTRIM(M1.NMKN) as TEMBETSUDENPYOFLG, T2.VANKBN || '-' || RTRIM(M2.NMKN) as KEISANCENTER, T2.UNYOKBN || '-' || RTRIM(M3.NMKN) as UNYOKBN, T2.DENPKBN || '-' || RTRIM(M4.NMKN) as DENPYOKBN, T2.SHUHKBN || '-' || RTRIM(M5.NMKN) as SHUKEIHYO1, T2.PICKDKBN || '-' || RTRIM(M6.NMKN) as PICKINGDATA, T2.PICKLKBN || '-' || RTRIM(M7.NMKN) as PICKINGLIST, T2.WAPNKBN || '-' || RTRIM(M8.NMKN) as WAPPEN, T2.IDENPKBN || '-' || RTRIM(M9.NMKN) as IKKATSUDENPYO, T2.KAKOSJKBN || '-' || RTRIM(M10.NMKN) as KAKOSHIJI, T2.RYUTSUKBN || '-' || RTRIM(M11.NMKN) as RYUTSUKBN"
          + ", T2.ZDENPKBN || '-' || RTRIM(M12.NMKN) as ZAIKOCHIWAKE_DENPYOKBN, T2.ZSHUHKBN || '-' || RTRIM(M13.NMKN) as ZAIKOCHIWAKE_SHUKEIHYO, T2.ZPICKDKBN || '-' || RTRIM(M14.NMKN) as ZAIKOCHIWAKE_PICKINGDATA, T2.ZPICKLKBN || '-' || RTRIM(M15.NMKN) as ZAIKOCHIWAKE_PICKINGLIST, case when T2.SIRKN is not null then right ('000000' || TRIM(T2.RSIRCD), 6) end  as RSIRCD, T2.SIRKN as SIRKN_R, T2.YKNSHKBN || '-' || RTRIM(M16.NMKN) as YOKOMOCHISAKI_KENSHUKBN, T2.YDENPKBN || '-' || RTRIM(M17.NMKN) as YOKOMOCHISAKI_DENPYOKBN, T2.DSHUHKBN || '-' || RTRIM(M18.NMKN) as YOKOMOCHISAKI_SHUKEIHYO from T1 left join (select ROW_NUMBER() over (order by SIRCD, HSPTN) as IDX, Te1.* from (select HSPS.SIRCD, HSP.HSPTN, HSP.HSPTNKN, HSP.CENTERCD, TRIM(HSP.YCENTERCD) as YCENTERCD, HSPS.TENDENFLG, HSPS.VANKBN, HSPS.UNYOKBN, HSPS.DENPKBN, HSPS.SHUHKBN, HSPS.PICKDKBN, HSPS.PICKLKBN, HSPS.WAPNKBN, HSPS.IDENPKBN, HSPS.KAKOSJKBN, HSPS.RYUTSUKBN, HSPS.ZDENPKBN, HSPS.ZSHUHKBN, HSPS.ZPICKDKBN, HSPS.ZPICKLKBN, HSPS.RSIRCD, SIR.SIRKN, HSPS.YKNSHKBN, HSPS.YDENPKBN, HSPS.DSHUHKBN"
          + " from INAMS.MSTHSPTNSIR HSPS left join INAMS.MSTHSPTN HSP on HSP.HSPTN = HSPS.HSPTN left join INAMS.MSTSIR SIR on SIR.SIRCD = NULLIF(Trim(HSPS.RSIRCD), '') where HSPS.SIRCD = ? order by HSPS.SIRCD, HSP.HSPTN) AS Te1 ) T2 on T1.IDX = T2.IDX left outer join INAMS.MSTMEISHO M1 on M1.MEISHOKBN = 425 and CONVERT(T2.TENDENFLG,CHAR) = M1.MEISHOCD left outer join INAMS.MSTMEISHO M2 on M2.MEISHOKBN = 406 and CONVERT(T2.VANKBN,CHAR) = M2.MEISHOCD left outer join INAMS.MSTMEISHO M3 on M3.MEISHOKBN = 407 and CONVERT(T2.UNYOKBN,CHAR) = M3.MEISHOCD left outer join INAMS.MSTMEISHO M4 on M4.MEISHOKBN = 408 and CONVERT(T2.DENPKBN,CHAR) = M4.MEISHOCD left outer join INAMS.MSTMEISHO M5 on M5.MEISHOKBN = 409 and CONVERT(T2.SHUHKBN,CHAR) = M5.MEISHOCD left outer join INAMS.MSTMEISHO M6 on M6.MEISHOKBN = 410 and CONVERT(T2.PICKDKBN,CHAR) = M6.MEISHOCD left outer join INAMS.MSTMEISHO M7 on M7.MEISHOKBN = 411 and CONVERT(T2.PICKLKBN,CHAR) = M7.MEISHOCD"
          + " left outer join INAMS.MSTMEISHO M8 on M8.MEISHOKBN = 412 and CONVERT(T2.WAPNKBN,CHAR) = M8.MEISHOCD left outer join INAMS.MSTMEISHO M9 on M9.MEISHOKBN = 413 and CONVERT(T2.IDENPKBN,CHAR) = M9.MEISHOCD left outer join INAMS.MSTMEISHO M10 on M10.MEISHOKBN = 414 and CONVERT(T2.KAKOSJKBN,CHAR) = M10.MEISHOCD left outer join INAMS.MSTMEISHO M11 on M11.MEISHOKBN = 415 and CONVERT(T2.RYUTSUKBN,CHAR) = M11.MEISHOCD left outer join INAMS.MSTMEISHO M12 on M12.MEISHOKBN = 416 and CONVERT(T2.ZDENPKBN,CHAR) = M12.MEISHOCD left outer join INAMS.MSTMEISHO M13 on M13.MEISHOKBN = 417 and CONVERT(T2.ZSHUHKBN,CHAR) = M13.MEISHOCD left outer join INAMS.MSTMEISHO M14 on M14.MEISHOKBN = 418 and CONVERT(T2.ZPICKDKBN,CHAR) = M14.MEISHOCD left outer join INAMS.MSTMEISHO M15 on M15.MEISHOKBN = 419 and CONVERT(T2.ZPICKLKBN,CHAR) = M15.MEISHOCD"
          + " left outer join INAMS.MSTMEISHO M16 on M16.MEISHOKBN = 422 and CONVERT(T2.YKNSHKBN,CHAR) = M16.MEISHOCD left outer join INAMS.MSTMEISHO M17 on M17.MEISHOKBN = 423 and CONVERT(T2.YDENPKBN,CHAR) = M17.MEISHOCD left outer join INAMS.MSTMEISHO M18 on M18.MEISHOKBN = 424 and CONVERT(T2.DSHUHKBN,CHAR) = M18.MEISHOCD order by T1.IDX, T2.SIRCD, T2.HSPTN";

  // SQL：エリア別配送パターン TODO:
  /** 共通（INAMS.MSTHSPTN） */
  public final static String ID_SQL_EHSPTN =
      "select right('000' || EHPTN.HSPTN, 3) as F1, MAX(HPTN.HSPTNKN) as F2, case when EHPTN.AREAKBN = 1 then right ('0000' || EHPTN.TENGPCD, 4) when EHPTN.AREAKBN = 0 then right ('00' || EHPTN.TENGPCD, 2) end as F3, MAX(HTG.TENGPKN) as F4, right ('000' || MAX(EHPTN.CENTERCD), 3) as F5, right ('000' || MAX(trim(EHPTN.YCENTERCD)), 3) as F6"
          + ", COUNT(case when EHPTN.AREAKBN = 1 then HSGPTEN.TENCD when EHPTN.AREAKBN = 0 then TENB.TENCD end) as F7, MAX(EHPTN.AREAKBN) as F8, right ('0000' || MAX(EHPTN.HSGPCD), 4) as F9, MAX(HSGP.HSGPKN) as F10 from INAMS.MSTAREAHSPTN EHPTN"
          + " left join INAMS.MSTHSPTN HPTN on HPTN.HSPTN = EHPTN.HSPTN left join INAMS.MSTHSTENGP HTG on HTG.HSGPCD = EHPTN.HSGPCD and HTG.TENGPCD = EHPTN.TENGPCD left join INAMS.MSTHSGPTEN HSGPTEN on HSGPTEN.HSGPCD = EHPTN.HSGPCD and HSGPTEN.TENGPCD = EHPTN.TENGPCD"
          + " left join (select AREACD, TENCD from INAMS.MSTTENBMN group by AREACD, TENCD) TENB on TENB.AREACD = EHPTN.TENGPCD left join INAMS.MSTHSGP HSGP on HSGP.HSGPCD = EHPTN.HSGPCD group by EHPTN.HSPTN, EHPTN.TENGPCD, EHPTN.AREAKBN";

  // SQL：エリア別配送パターン(仕入先マスタ) TODO:
  /** 共通（INAMS.MSTHSPTN） */
  public final static String ID_SQL_EHSPTN_LIST =
      "WITH RECURSIVE T1(IDX) as (select 1 from (SELECT 1 AS DUMMY) DUMMY UNION ALL SELECT IDX+1 FROM T1 WHERE IDX < @M ) select T1.IDX, T2.HSPTN, T2.HSPTNKN, right ('000' || T2.CENTERCD, 3) as CENTERCD, right ('000' || TRIM(T2.YCENTERCD), 3) as YCENTERCD, right ('0000' || T2.TENGPCD, 4) as TENGPCD, T2.TENGPKN, T2.HSPTN, T2.TENDENFLG || '-' || RTRIM(M1.NMKN) as TEMBETSUDENPYOFLG, T2.VANKBN || '-' || RTRIM(M2.NMKN) as KEISANCENTER, T2.UNYOKBN || '-' || RTRIM(M3.NMKN) as UNYOKBN, T2.DENPKBN || '-' || RTRIM(M4.NMKN) as DENPYOKBN, T2.SHUHKBN || '-' || RTRIM(M5.NMKN) as SHUKEIHYO1, T2.PICKDKBN || '-' || RTRIM(M6.NMKN) as PICKINGDATA, T2.PICKLKBN || '-' || RTRIM(M7.NMKN) as PICKINGLIST, T2.WAPNKBN || '-' || RTRIM(M8.NMKN) as WAPPEN, T2.IDENPKBN || '-' || RTRIM(M9.NMKN) as IKKATSUDENPYO, T2.KAKOSJKBN || '-' || RTRIM(M10.NMKN) as KAKOSHIJI, T2.RYUTSUKBN || '-' || RTRIM(M11.NMKN) as RYUTSUKBN"
          + ", T2.ZDENPKBN || '-' || RTRIM(M12.NMKN) as ZAIKOCHIWAKE_DENPYOKBN, T2.ZSHUHKBN || '-' || RTRIM(M13.NMKN) as ZAIKOCHIWAKE_SHUKEIHYO, T2.ZPICKDKBN || '-' || RTRIM(M14.NMKN) as ZAIKOCHIWAKE_PICKINGDATA, T2.ZPICKLKBN || '-' || RTRIM(M15.NMKN) as ZAIKOCHIWAKE_PICKINGLIST, case when T2.SIRKN is not null then right ('000000' || TRIM(T2.RSIRCD), 6) end  as RSIRCD, T2.SIRKN as SIRKN_R, T2.YKNSHKBN || '-' || RTRIM(M16.NMKN) as YOKOMOCHISAKI_KENSHUKBN, T2.YDENPKBN || '-' || RTRIM(M17.NMKN) as YOKOMOCHISAKI_DENPYOKBN, T2.DSHUHKBN || '-' || RTRIM(M18.NMKN) as YOKOMOCHISAKI_SHUKEIHYO from T1 left join (select ROW_NUMBER() over (order by SIRCD, HSPTN, TENGPCD) as IDX, Te1.* from (select EHSPS.SIRCD, EHSPS.HSPTN, HSPTN.HSPTNKN, EHSP.CENTERCD, TRIM(EHSP.YCENTERCD) as YCENTERCD, EHSPS.TENGPCD, HSTG.TENGPKN, EHSPS.TENDENFLG, EHSPS.VANKBN, EHSPS.UNYOKBN, EHSPS.DENPKBN, EHSPS.SHUHKBN, EHSPS.PICKDKBN, EHSPS.PICKLKBN, EHSPS.WAPNKBN, EHSPS.IDENPKBN, EHSPS.KAKOSJKBN, EHSPS.RYUTSUKBN, EHSPS.ZDENPKBN, EHSPS.ZSHUHKBN, EHSPS.ZPICKDKBN, EHSPS.ZPICKLKBN, EHSPS.RSIRCD, SIR.SIRKN, EHSPS.YKNSHKBN, EHSPS.YDENPKBN, EHSPS.DSHUHKBN"
          + " from INAMS.MSTAREAHSPTNSIR EHSPS left join INAMS.MSTAREAHSPTN EHSP on EHSP.HSPTN = EHSPS.HSPTN and EHSP.TENGPCD = EHSPS.TENGPCD left join INAMS.MSTHSPTN HSPTN on HSPTN.HSPTN = EHSPS.HSPTN left join INAMS.MSTHSTENGP HSTG on HSTG.HSGPCD = EHSP.HSGPCD and HSTG.TENGPCD = EHSP.TENGPCD left join INAMS.MSTSIR SIR on SIR.SIRCD = NULLIF(TRIM(EHSPS.RSIRCD), '') where EHSPS.SIRCD = ? order by EHSPS.SIRCD, EHSP.HSPTN, EHSP.TENGPCD) AS Te1) T2 on T1.IDX = T2.IDX left outer join INAMS.MSTMEISHO M1 on M1.MEISHOKBN = 425 and CONVERT(T2.TENDENFLG,CHAR) = M1.MEISHOCD left outer join INAMS.MSTMEISHO M2 on M2.MEISHOKBN = 406 and CONVERT(T2.VANKBN,CHAR) = M2.MEISHOCD left outer join INAMS.MSTMEISHO M3 on M3.MEISHOKBN = 407 and CONVERT(T2.UNYOKBN,CHAR) = M3.MEISHOCD left outer join INAMS.MSTMEISHO M4 on M4.MEISHOKBN = 408 and CONVERT(T2.DENPKBN,CHAR) = M4.MEISHOCD left outer join INAMS.MSTMEISHO M5 on M5.MEISHOKBN = 409 and CONVERT(T2.SHUHKBN,CHAR) = M5.MEISHOCD"
          + " left outer join INAMS.MSTMEISHO M6 on M6.MEISHOKBN = 410 and CONVERT(T2.PICKDKBN,CHAR) = M6.MEISHOCD left outer join INAMS.MSTMEISHO M7 on M7.MEISHOKBN = 411 and CONVERT(T2.PICKLKBN,CHAR) = M7.MEISHOCD left outer join INAMS.MSTMEISHO M8 on M8.MEISHOKBN = 412 and CONVERT(T2.WAPNKBN,CHAR) = M8.MEISHOCD left outer join INAMS.MSTMEISHO M9 on M9.MEISHOKBN = 413 and CONVERT(T2.IDENPKBN,CHAR) = M9.MEISHOCD left outer join INAMS.MSTMEISHO M10 on M10.MEISHOKBN = 414 and CONVERT(T2.KAKOSJKBN,CHAR) = M10.MEISHOCD left outer join INAMS.MSTMEISHO M11 on M11.MEISHOKBN = 415 and CONVERT(T2.RYUTSUKBN,CHAR) = M11.MEISHOCD left outer join INAMS.MSTMEISHO M12 on M12.MEISHOKBN = 416 and CONVERT(T2.ZDENPKBN,CHAR) = M12.MEISHOCD left outer join INAMS.MSTMEISHO M13 on M13.MEISHOKBN = 417 and CONVERT(T2.ZSHUHKBN,CHAR) = M13.MEISHOCD left outer join INAMS.MSTMEISHO M14 on M14.MEISHOKBN = 418 and CONVERT(T2.ZPICKDKBN,CHAR) = M14.MEISHOCD left outer join INAMS.MSTMEISHO M15 on M15.MEISHOKBN = 419 and CONVERT(T2.ZPICKLKBN,CHAR) = M15.MEISHOCD"
          + " left outer join INAMS.MSTMEISHO M16 on M16.MEISHOKBN = 422 and CONVERT(T2.YKNSHKBN,CHAR) = M16.MEISHOCD left outer join INAMS.MSTMEISHO M17 on M17.MEISHOKBN = 423 and CONVERT(T2.YDENPKBN,CHAR) = M17.MEISHOCD left outer join INAMS.MSTMEISHO M18 on M18.MEISHOKBN = 424 and CONVERT(T2.DSHUHKBN,CHAR) = M18.MEISHOCD order by T1.IDX, T2.SIRCD, T2.HSPTN, T2.TENGPCD";

  // SQL：エリア別配送パターン(配送パターンマスタ画面で使用) TODO:
  /** 共通（INAMS.MSTHSPTN） */
  public final static String ID_SQL_EHSPTN_HP012_INS =
      "with RECURSIVE T1(IDX) as (select 1 from (SELECT 1 AS DUMMY) DUMMY union all select IDX + 1 from T1 where IDX < @M) select T1.IDX, T2.TENGPCD, T2.TENGPKN, null as CENTERCD, null as YCENTERCD, null as TENKN_CENTER_G, null as TENKN_YCENTER_G, T2.SELCHECK as SEL from T1 left join (select ROW_NUMBER() over (order by TENGPCD) as IDX ,TENGPCD ,TENGPKN ,SELCHECK  from (select HSTENGP.TENGPCD, HSTENGP.TENGPKN, 0 as SELCHECK from INAMS.MSTHSGP HSGP"
          + " inner join INAMS.MSTHSTENGP HSTENGP on HSTENGP.HSGPCD = HSGP.HSGPCD and COALESCE(HSTENGP.UPDKBN, 0) <> 1 where COALESCE(HSGP.UPDKBN, 0) <> 1 and HSGP.AREAKBN = ? and HSGP.HSGPCD = ? ) T5 order by TENGPCD LIMIT @M ) T2 on T1.IDX = T2.IDX order by T1.IDX";

  public final static String ID_SQL_EHSPTN_HP012_UPD =
      "with RECURSIVE T1(IDX) as (select 1 from (SELECT 1 AS DUMMY) DUMMY union all select IDX + 1 from T1 where IDX < @M) select T1.IDX, T2.TENGPCD, T2.TENGPKN, case when T2.CENTERCD = 0 then null else TRIM(T2.CENTERCD) end as CENTERCD, case when replace (T2.YCENTERCD, '0', '') = '' then null else TRIM(T2.YCENTERCD) end as YCENTERCD, case when T2.CENTERCD = 0 then null else TEN1.TENKN end as TENKN_CENTER_G, case when replace (T2.YCENTERCD, '0', '') = '' then null else TEN2.TENKN end as TENKN_YCENTER_G, T2.SELCHECK as SEL from T1 left join (select ROW_NUMBER() over (order by TENGPCD) as IDX ,TENGPCD ,TENGPKN ,CENTERCD ,YCENTERCD ,SELCHECK  from (select HSTENGP.TENGPCD, HSTENGP.TENGPKN, AHS.CENTERCD as CENTERCD, case when TRIM(COALESCE(AHS.YCENTERCD, '')) = '' then null else TRIM(AHS.YCENTERCD) end as YCENTERCD, case when AHS.TENGPCD is not null then 1 else 0 end as SELCHECK from INAMS.MSTHSGP HSGP"
          + " inner join INAMS.MSTHSTENGP HSTENGP on HSTENGP.HSGPCD = HSGP.HSGPCD and COALESCE(HSTENGP.UPDKBN, 0) <> 1 left join (select * from INAMS.MSTAREAHSPTN where HSPTN = ? ) AHS on AHS.HSGPCD = HSGP.HSGPCD and AHS.TENGPCD = HSTENGP.TENGPCD and AHS.AREAKBN = HSGP.AREAKBN where COALESCE(HSGP.UPDKBN, 0) <> 1 and HSGP.AREAKBN = ? and HSGP.HSGPCD = ? ) T5 order by TENGPCD LIMIT @M ) T2 on T1.IDX = T2.IDX left join INAMS.MSTTEN TEN1 on TEN1.TENCD = T2.CENTERCD left join INAMS.MSTTEN TEN2 on TEN2.TENCD = T2.YCENTERCD order by T1.IDX";

  public final static String ID_SQL_NOHIN =
      "with RECURSIVE T1(IDX) as (select 1 from (SELECT 1 AS DUMMY) DUMMY union all select IDX + 1 from T1 where IDX < 10), WEEK as (select CWEEK, JWEEK from (values ROW(1, '日'), ROW(2, '月'), ROW(3, '火'), ROW(4, '水'), ROW(5, '木'), ROW(6, '金'), ROW(7, '土')) as TMP(CWEEK, JWEEK)) select T1.IDX, right (T2.NNDT, 6) as NNDT, W1.JWEEK as WEE, T2.YOTEISU as YOTEISU, T2.GENDOSU as GENDOSU from T1 left join (select ROW_NUMBER() over (order by KKKCD, SHNCD) as IDX, NNDT, YOTEISU, GENDOSU, DAYOFWEEK(DATE_FORMAT('20' || right ('0' || NNDT, 6), '%Y%m%d')) as NNDT_WNUM from INATK.HATYH_NNDT where KKKCD = ? and SHNCD = ? ) as T2 on T2.IDX = T1.IDX left outer join WEEK W1 on T2.NNDT_WNUM = W1.CWEEK order by T1.IDX";

  // SQL：納品日一覧(発注数項目あり) TODO:
  public final static String ID_SQL_NOHIN_HTS =
      "select NNDT.KKKCD, NNDT.SHNCD, NNDT.NNDT, case when NNDT.KKKCD is not null then TO_CHAR(right (NNDT.NNDT, 6)) else '合計' end as F1, SUM(case when COALESCE(TEN.INPUTDT, 0) < ? then TEN.HTSU else 0 end) as F2, SUM(case when COALESCE(TEN.INPUTDT, 0) = ? then TEN.HTSU else 0 end) as F3, case when NNDT.KKKCD is not null then MAX(NNDT.YOTEISU) else 0 end as F4, case when NNDT.KKKCD is not null then MAX(NNDT.GENDOSU) else 0 end as F5 from INATK.HATYH_NNDT NNDT"
          + " left join INATK.HATYH_TEN TEN on TEN.KKKCD = NNDT.KKKCD and TEN.SHNCD = NNDT.SHNCD and TEN.NNDT = NNDT.NNDT where NNDT.KKKCD = ? and NNDT.SHNCD = ? group by NNDT.KKKCD, NNDT.SHNCD, NNDT.NNDT WITH ROLLUP having NNDT.NNDT is not null or NNDT.KKKCD is null order by NNDT.KKKCD, NNDT.SHNCD, NNDT.NNDT";

  // SQL：リードタイムパターン TODO:
  /** 共通（INAMS.MSTREADTM） */
  public final static String ID_SQL_READTMPTN_HEAD = "select VALUE, TEXT, TEXT as TEXT2 from (values ROW(" + Values.NONE.getVal() + ", '　')) as X(value, TEXT) union all ";
  public final static String ID_SQL_READTMPTN =
      "select READTMPTN as VALUE, right('000'||READTMPTN,3)||'" + SEPARATOR + "'||COALESCE(rtrim(READTMPTNKN),'') as TEXT, rtrim(READTMPTNKN) as TEXT2 from INAMS.MSTREADTM" + ID_SQL_CMN_WHERE;
  public final static String ID_SQL_READTMPTN2 =
      "select READTMPTN as F1, rtrim(rtrim(READTMPTNKN)) as F2, READTM_MON as F3, READTM_TUE as F4, READTM_WED as F5, READTM_THU as F6, READTM_FRI as F7, READTM_SAT as F8, READTM_SUN as F9 from INAMS.MSTREADTM";
  public final static String ID_SQL_READTMPTN_WHERE = ID_SQL_CMN_WHERE + " and READTMPTN = ? ";

  // SQL：ソースコード
  /** 共通（INAMS.MSTSRCCD） */
  // ソースコード：優先順位判断
  public final static String ID_SQL_SRCCD_COL = "T.SHNCD,T.SRCCD,T.YOYAKUDT,T.SEQNO,T.SOURCEKBN,T.YUKO_STDT,T.YUKO_EDDT";
  public final static String ID_SQL_SRCCD_JAN = " select " + ID_SQL_SRCCD_COL + " ,T.JAN1NO" + " from (" + "  select SHNCD,SRCCD,YOYAKUDT,SEQNO,SOURCEKBN,YUKO_STDT,YUKO_EDDT"
      + "  ,row_number() over(partition by SHNCD,YOYAKUDT order by COALESCE(SEQNO, 9),case when STNO <= 0 then abs(STNO) end,case when STNO > 0 then STNO end,SRCCD) as JAN1NO" + "  from (select "
      + ID_SQL_SRCCD_COL + ",days(to_date(T.YUKO_STDT,'YYYYMMDD'))-days(current date) as STNO,days(to_date(T.YUKO_EDDT,'YYYYMMDD'))-days(current date) as EDNO from @T ) WK" + " ) T";
  public final static String ID_SQL_SRCCD_SEL = " select trim(T.SRCCD) as SRCCD, T.SOURCEKBN||'" + SEPARATOR
      + "'||RTRIM(T2.NMKN) as SOURCEKBN, T.SOURCEKBN as SOURCEKBN2, COALESCE(right(T.YUKO_STDT, 6),'') as YUKO_STDT, COALESCE(right(T.YUKO_EDDT,6),'') as YUKO_EDDT, COALESCE(CAST(SEQNO AS CHAR ), '') as SEQNO";
  public final static String ID_SQL_SRCCD_JOIN = " left outer join INAMS.MSTMEISHO T2 on T2.MEISHOKBN = " + MeisyoSelect.KBN136.getCd() + " and CAST(T.SOURCEKBN AS CHAR) = T2.MEISHOCD";
  public final static String ID_SQL_SRCCD1 = ID_SQL_SRCCD_SEL + " from (select " + ID_SQL_SRCCD_COL + " from @T) T" + ID_SQL_SRCCD_JOIN + " order by T.SEQNO";
  public final static String ID_SQL_SRCCD2 = ID_SQL_SRCCD_SEL + " from (" + ID_SQL_SRCCD_JAN + ") T" + ID_SQL_SRCCD_JOIN + " order by T.JAN1NO";

  // SQL：配送グループ
  public final static String ID_SQL_HSGP = "select right('0000'||HSGPCD, 4) as F1 ,HSGPKN as F2 from INAMS.MSTHSGP " + ID_SQL_CMN_WHERE + " and AREAKBN = ?";
  public final static String ID_SQL_HSGP_FOOTER = " order by F1";

  // SQL：配送店グループ TODO:
  public final static String ID_SQL_HSTENGP = ID_SQL_GRD_CMN
      + "select T1.IDX, T2.TENGPCD, T2.TENGPKN, T2.CENTERCD, T2.YCENTERCD from T1 left join (select ROW_NUMBER() over (order by TENGPCD) as IDX, * from (select HTGP.TENGPCD, HTGP.TENGPKN, EHPTN.CENTERCD, EHPTN.YCENTERCD from INAMS.MSTHSTENGP HTGP left join INAMS.MSTAREAHSPTN EHPTN on EHPTN.TENGPCD = HTGP.TENGPCD and EHPTN.HSGPCD = HTGP.HSGPCD where HTGP.UPDKBN = 0 and HTGP.HSGPCD = ? ) order by TENGPCD fetch first @M rows only) T2 on T1.IDX = T2.IDX order by T1.IDX";
  public final static String ID_SQL_HSTENGP2 = ID_SQL_GRD_CMN
      + "SELECT T1.IDX, T2.TENGPCD AS HSTENGPCD, T2.TENGPKN, T2.TENGPAN, case when T2.TENGPCD is not null then 0 else 1 end as SINTENKBN FROM T1 LEFT JOIN (SELECT ROW_NUMBER() OVER (ORDER BY TENGPCD) AS IDX, TENGPCD, TENGPKN, TENGPAN FROM INAMS.MSTHSTENGP WHERE HSGPCD = ? AND UPDKBN = ? ORDER BY HSGPCD,TENGPCD) AS T2 ON T1.IDX = T2.IDX ORDER BY T1.IDX";
  public final static String ID_SQL_HSTENGP3 =
      "SELECT TENGP.TENGPCD AS HSTENGPCD, TENGP.TENGPKN AS TENGPKN, CASE HSGP.AREAKBN WHEN '0' THEN '0' WHEN '1' THEN CNT.CNT END AS TENPOSU, HSGP.AREAKBN FROM INAMS.MSTHSGP AS HSGP, INAMS.MSTHSTENGP AS TENGP, (SELECT COUNT(GPTEN.TENCD) AS CNT, TENGP.HSGPCD AS HSGPCD, TENGP.TENGPCD AS TENGPCD FROM INAMS.MSTHSTENGP AS TENGP LEFT JOIN INAMS.MSTHSGPTEN AS GPTEN ON TENGP.HSGPCD = GPTEN.HSGPCD AND TENGP.TENGPCD = GPTEN.TENGPCD AND GPTEN.UPDKBN = ? GROUP BY TENGP.HSGPCD, TENGP.TENGPCD) AS CNT WHERE HSGP.HSGPCD = ? AND HSGP.UPDKBN = ? AND HSGP.HSGPCD = TENGP.HSGPCD AND TENGP.UPDKBN = ? AND TENGP.HSGPCD = CNT.HSGPCD AND TENGP.TENGPCD = CNT.TENGPCD";

  // SQL：店グループ
  public static final String ID_SQL_MSTGRP = ID_SQL_GRD_CMN + "select T2.GRPID, T3.GRPKN from T1"
      + " left outer join (select ROW_NUMBER() over (order by GRPID) as IDX, TA1.* from INAMS.MSTGRP AS TA1 where SHNCD like ? order by GRPID LIMIT @M ) T2 on T1.IDX = T2.IDX"
      + " left outer join INAMS.MSTGROUP T3 on T2.GRPID = T3.GRPID";
  public static final String ID_SQL_MSTGRP_Y = ID_SQL_GRD_CMN + "select T2.GRPID, T3.GRPKN from T1"
      + " left outer join (select ROW_NUMBER() over (order by GRPID) as IDX, TA1.* from INAMS.MSTGRP_Y AS TA1 where SHNCD like ? and YOYAKUDT = ? order by GRPID LIMIT @M ) T2 on T1.IDX = T2.IDX"
      + " left outer join INAMS.MSTGROUP T3 on T2.GRPID = T3.GRPID";
  public static final String ID_SQL_MSTGROUP = "select GRPID as " + VAL + ", GRPKN as " + TXT + " from INAMS.MSTGROUP " + ID_SQL_CMN_WHERE + " and GRPKN like ? order by GRPKN LIMIT @M  ";
  public static final String ID_SQL_MSTGROUP2 = "with INP as (select IDX, GRPKN from (values @V) as X(IDX, GRPKN,T3,T4,T5,T6,T7))"
      + "select T2.GRPID, T1.GRPKN from INP T1 left outer join INAMS.MSTGROUP T2 on T1.GRPKN = T2.GRPKN and COALESCE(T2.UPDKBN,0) <> 1 order by T1.IDX, T2.GRPID";

  // SQL：商品店グループ TODO:
  /** 共通（INAMS.MSTSHNTENGP） */
  public final static String ID_SQL_TENGP = "select T1.TENGPCD as VALUE, T1.TENGPCD||'" + SEPARATOR + "'||rtrim(rtrim(T1.TENGPKN)) as TEXT, T1.TENGPKN as TEXT2, T1.AREAKBN from INAMS.MSTSHNTENGP T1 "
      + ID_SQL_CMN_WHERE + " and CAST(T1.TENGPCD AS CHAR) = ? and CAST(T1.GPKBN AS CHAR) = ? and CAST(T1.AREAKBN AS CHAR) = ? and CAST(T1.BMNCD AS CHAR) = ?";
  public final static String ID_SQL_TENGP2 = "select T1.TENGPCD as F1, max(rtrim(rtrim(T1.TENGPKN))) as F2, count(T2.TENCD) as F3"
      + " from INAMS.MSTSHNTENGP T1 left outer join INAMS.MSTSHNTENGPTEN T2 on T1.GPKBN = T2.GPKBN and T1.BMNCD = T2.BMNCD and T1.TENGPCD = T2.TENGPCD "
      + " where T1.UPDKBN = 0 and T1.GPKBN = ? and T1.BMNCD = ? and T1.AREAKBN = ? and T1.TENGPKN like ? group by T1.TENGPCD";
  public final static String ID_SQL_TENGP_CHK_TEN_CNT = "select count(TENCD) as CNT from INAMS.MSTSHNTENGP T1 inner join INAMS.MSTSHNTENGPTEN T2"
      + " on T1.GPKBN = T2.GPKBN and T1.BMNCD = T2.BMNCD and T1.TENGPCD = T2.TENGPCD and T1.GPKBN = ? and T1.BMNCD = ? and T1.AREAKBN = ? and T1.TENGPCD in (@) and COALESCE(T1.UPDKBN,0) <> 1 group by TENCD having count(TENCD) > 1";

  /** 仕入グループ（INAMS.MSTSIRGPSHN） */
  public final static String ID_SQL_TENGP_SIR = ID_SQL_GRD_CMN + "select T2.TENGPCD, T3.TENGPKN, T2.SIRCD, T2.SIRCD as SSIRCD, T4.SIRKN, T2.HSPTN, T5.HSPTNKN, T2.AREAKBN from T1"
      + " left outer join (select ROW_NUMBER() over (order by TENGPCD) as IDX, TA1.* from INAMS.MSTSIRGPSHN AS TA1 where SHNCD like ? and AREAKBN = ? order by TENGPCD LIMIT @M ) T2 on T1.IDX = T2.IDX"
      + " left outer join INAMS.MSTSHNTENGP T3 on T2.TENGPCD = T3.TENGPCD and T2.AREAKBN = T3.AREAKBN and T3.GPKBN = " + ValGpkbn.SIR.getVal() + " and T3.BMNCD = ? and COALESCE(T3.UPDKBN,0) <> 1 "
      + " left outer join INAMS.MSTSIR T4 on T2.SIRCD = T4.SIRCD and COALESCE(T4.UPDKBN,0) <> 1 " + " left outer join INAMS.MSTHSPTN T5 on T2.HSPTN = T5.HSPTN and COALESCE(T5.UPDKBN,0) <> 1 ";
  public final static String ID_SQL_TENGP_SIR_Y = ID_SQL_GRD_CMN + "select T2.TENGPCD, T3.TENGPKN, T2.SIRCD, T2.SIRCD as SSIRCD, T4.SIRKN, T2.HSPTN, T5.HSPTNKN, T2.AREAKBN from T1"
      + " left outer join (select ROW_NUMBER() over (order by TENGPCD) as IDX, TA1.* from INAMS.MSTSIRGPSHN_Y AS TA1 where SHNCD like ?  and YOYAKUDT = ? and AREAKBN = ? order by TENGPCD LIMIT @M) T2 on T1.IDX = T2.IDX"
      + " left outer join INAMS.MSTSHNTENGP T3 on T2.TENGPCD = T3.TENGPCD and T2.AREAKBN = T3.AREAKBN and T3.GPKBN = " + ValGpkbn.SIR.getVal() + " and T3.BMNCD = ? and COALESCE(T3.UPDKBN,0) <> 1 "
      + " left outer join INAMS.MSTSIR T4 on T2.SIRCD = T4.SIRCD and COALESCE(T4.UPDKBN,0) <> 1 " + " left outer join INAMS.MSTHSPTN T5 on T2.HSPTN = T5.HSPTN and COALESCE(T5.UPDKBN,0) <> 1 ";
  public final static String ID_SQL_TENGP_SIR_C = ID_SQL_GRD_CMN + "select T2.TENGPCD, T3.TENGPKN, T2.SIRCD, T2.SIRCD as SSIRCD, T4.SIRKN, T2.HSPTN, T5.HSPTNKN, T2.AREAKBN from T1"
      + " left outer join (select ROW_NUMBER() over (order by TENGPCD) as IDX, * from INAMS.CSVSIRSHN where SEQ = ? and INPUTNO = ? order by TENGPCD fetch first @M rows only) T2 on T1.IDX = T2.IDX"
      + " left outer join INAMS.MSTSHNTENGP T3 on T2.TENGPCD = T3.TENGPCD and T2.AREAKBN = T3.AREAKBN and T3.GPKBN = " + ValGpkbn.SIR.getVal() + " and T3.BMNCD = ? and COALESCE(T3.UPDKBN,0) <> 1 "
      + " left outer join INAMS.MSTSIR T4 on T2.SIRCD = T4.SIRCD and COALESCE(T4.UPDKBN,0) <> 1 " + " left outer join INAMS.MSTHSPTN T5 on T2.HSPTN = T5.HSPTN and COALESCE(T5.UPDKBN,0) <> 1 ";
  public final static String ID_SQL_TENGP_SIR_CHK_CNT = "select count(TENCD) from INAMS.MSTSHNTENGP T1 inner join INAMS.MSTSHNTENGPTEN T2"
      + " on T1.GPKBN = T2.GPKBN and T1.BMNCD = T2.BMNCD and T1.TENGPCD = T2.TENGPCD and T1.GPKBN = ? and T1.BMNCD = ? and T1.AREAKBN = ? and T1.TENGPCD in (@) and COALESCE(T1.UPDKBN,0) <> 1 group by TENCD having count(TENCD) > 1";

  /** 売価グループ（INAMS.MSTBAIKACTL） */
  public final static String ID_SQL_TENGP_BAIKA = ID_SQL_GRD_CMN + "select T2.TENGPCD, T3.TENGPKN, T2.GENKAAM, T2.BAIKAAM, '' as BG_SOUBAIKA, '' as BG_NEIRE, T2.IRISU, T2.AREAKBN from T1"
      + " left outer join (select ROW_NUMBER() over (order by TENGPCD) as IDX, TA1.* from INAMS.MSTBAIKACTL AS TA1 where SHNCD like ? and AREAKBN = ? order by TENGPCD LIMIT @M ) T2 on T1.IDX = T2.IDX"
      + " left outer join INAMS.MSTSHNTENGP T3 on T2.TENGPCD = T3.TENGPCD and T2.AREAKBN = T3.AREAKBN and T3.GPKBN = " + ValGpkbn.BAIKA.getVal()
      + " and T3.BMNCD = ? and COALESCE(T3.UPDKBN,0) <> 1 ORDER BY T2.TENGPCD IS NULL asc, T2.TENGPCD  asc";
  public final static String ID_SQL_TENGP_BAIKA_Y = ID_SQL_GRD_CMN + "select T2.TENGPCD, T3.TENGPKN, T2.GENKAAM, T2.BAIKAAM, '' as BG_SOUBAIKA, '' as BG_NEIRE, T2.IRISU, T2.AREAKBN from T1"
      + " left outer join (select ROW_NUMBER() over (order by TENGPCD) as IDX, TA1.* from INAMS.MSTBAIKACTL_Y AS TA1 where SHNCD like ?  and YOYAKUDT = ? and AREAKBN = ? order by TENGPCD LIMIT @M ) T2 on T1.IDX = T2.IDX"
      + " left outer join INAMS.MSTSHNTENGP T3 on T2.TENGPCD = T3.TENGPCD and T2.AREAKBN = T3.AREAKBN and T3.GPKBN = " + ValGpkbn.BAIKA.getVal()
      + " and T3.BMNCD = ? and COALESCE(T3.UPDKBN,0) <> 1 ORDER BY T2.TENGPCD IS NULL asc, T2.TENGPCD  asc";
  public final static String ID_SQL_TENGP_BAIKA_C = ID_SQL_GRD_CMN + "select T2.TENGPCD, T3.TENGPKN, T2.GENKAAM, T2.BAIKAAM, '' as BG_SOUBAIKA, '' as BG_NEIRE, T2.IRISU, T2.AREAKBN from T1"
      + " left outer join (select ROW_NUMBER() over (order by TENGPCD) as IDX, TA1.* from INAMS.CSVBAIKACTL AS TA1 where SEQ = ? and INPUTNO = ? order by TENGPCD LIMIT @M ) T2 on T1.IDX = T2.IDX"
      + " left outer join INAMS.MSTSHNTENGP T3 on T2.TENGPCD = T3.TENGPCD and T2.AREAKBN = T3.AREAKBN and T3.GPKBN = " + ValGpkbn.BAIKA.getVal()
      + " and T3.BMNCD = ? and COALESCE(T3.UPDKBN,0) <> 1 ORDER BY T2.TENGPCD IS NULL asc, T2.TENGPCD  asc";

  /** 品揃えグループ（INAMS.MSTSHINAGP） */
  public final static String ID_SQL_TENGP_SHINA = ID_SQL_GRD_CMN + "select T2.TENGPCD, T3.TENGPKN, T2.ATSUKKBN||'" + SEPARATOR + "'||T4.NMKN as ATSUKKBN, T2.AREAKBN from T1"
      + " left outer join (select ROW_NUMBER() over (order by TENGPCD) as IDX, TA1.* from INAMS.MSTSHINAGP AS TA1 where SHNCD like ? and AREAKBN = ? order by TENGPCD LIMIT @M ) T2 on T1.IDX = T2.IDX"
      + " left outer join INAMS.MSTSHNTENGP T3 on T2.TENGPCD = T3.TENGPCD and T2.AREAKBN = T3.AREAKBN and T3.GPKBN = " + ValGpkbn.SHINA.getVal() + " and T3.BMNCD = ? and COALESCE(T3.UPDKBN,0) <> 1 "
      + " left outer join INAMS.MSTMEISHO T4 on T4.MEISHOKBN = " + MeisyoSelect.KBN139.getCd() + " and CAST(T2.ATSUKKBN AS CHAR) = T4.MEISHOCD";
  public final static String ID_SQL_TENGP_SHINA_Y = ID_SQL_GRD_CMN + "select T2.TENGPCD, T3.TENGPKN, T2.ATSUKKBN||'" + SEPARATOR + "'||T4.NMKN as ATSUKKBN, T2.AREAKBN from T1"
      + " left outer join (select ROW_NUMBER() over (order by TENGPCD) as IDX, TA1.* from INAMS.MSTSHINAGP_Y AS TA1 where SHNCD like ?  and YOYAKUDT = ? and AREAKBN = ? order by TENGPCD LIMIT @M ) T2 on T1.IDX = T2.IDX"
      + " left outer join INAMS.MSTSHNTENGP T3 on T2.TENGPCD = T3.TENGPCD and T2.AREAKBN = T3.AREAKBN and T3.GPKBN = " + ValGpkbn.SHINA.getVal() + " and T3.BMNCD = ? and COALESCE(T3.UPDKBN,0) <> 1 "
      + " left outer join INAMS.MSTMEISHO T4 on T4.MEISHOKBN = " + MeisyoSelect.KBN139.getCd() + " and CAST(T2.ATSUKKBN AS CHAR) = T4.MEISHOCD";
  public final static String ID_SQL_TENGP_SHINA_C = ID_SQL_GRD_CMN + "select T2.TENGPCD, T3.TENGPKN, T2.ATSUKKBN||'" + SEPARATOR + "'||T4.NMKN as ATSUKKBN, T2.AREAKBN from T1"
      + " left outer join (select ROW_NUMBER() over (order by TENGPCD) as IDX, TA1.* from INAMS.CSVSHINAGP AS TA1 where SEQ = ? and INPUTNO = ? order by TENGPCD LIMIT @M ) T2 on T1.IDX = T2.IDX"
      + " left outer join INAMS.MSTSHNTENGP T3 on T2.TENGPCD = T3.TENGPCD and T2.AREAKBN = T3.AREAKBN and T3.GPKBN = " + ValGpkbn.SHINA.getVal() + " and T3.BMNCD = ? and COALESCE(T3.UPDKBN,0) <> 1 "
      + " left outer join INAMS.MSTMEISHO T4 on T4.MEISHOKBN = " + MeisyoSelect.KBN139.getCd() + " and CAST(T2.ATSUKKBN AS CHAR ) = T4.MEISHOCD";

  /** 店異部門（INAMS.MSTSHNTENBMN）※店別異部門の店グループ情報は、商品店グループでは、売価グループを参照する */
  public final static String ID_SQL_TENGP_TBMN = ID_SQL_GRD_CMN + "select T2.TENSHNCD,T2.TENGPCD, T3.TENGPKN, T2.AREAKBN, trim(T2.SRCCD) as SRCCD from T1"
      + " left outer join (select ROW_NUMBER() over (order by TENGPCD) as IDX, TA1.* from INAMS.MSTSHNTENBMN AS TA1 where SHNCD like ? and AREAKBN = ? order by TENGPCD LIMIT @M ) T2 on T1.IDX = T2.IDX"
      + " left outer join INAMS.MSTSHNTENGP T3 on T2.TENGPCD = T3.TENGPCD and T2.AREAKBN = T3.AREAKBN and T3.GPKBN = " + ValGpkbn.BAIKA.getVal() + " and T3.BMNCD = ? and COALESCE(T3.UPDKBN,0) <> 1 ";
  public final static String ID_SQL_TENGP_TBMN_Y = ID_SQL_GRD_CMN + "select T2.TENSHNCD,T2.TENGPCD, T3.TENGPKN, T2.AREAKBN, trim(T2.SRCCD) as SRCCD from T1"
      + " left outer join (select ROW_NUMBER() over (order by TENGPCD) as IDX, TA1.* from INAMS.MSTSHNTENBMN_Y AS TA1 where SHNCD like ?  and YOYAKUDT = ? and AREAKBN = ? order by TENGPCD LIMIT @M ) T2 on T1.IDX = T2.IDX"
      + " left outer join INAMS.MSTSHNTENGP T3 on T2.TENGPCD = T3.TENGPCD and T2.AREAKBN = T3.AREAKBN and T3.GPKBN = " + ValGpkbn.BAIKA.getVal() + " and T3.BMNCD = ? and COALESCE(T3.UPDKBN,0) <> 1 ";
  public final static String ID_SQL_TENGP_TBMN_C = ID_SQL_GRD_CMN + "select T2.TENSHNCD,T2.TENGPCD, T3.TENGPKN, T2.AREAKBN, trim(T2.SRCCD) as SRCCD from T1"
      + " left outer join (select ROW_NUMBER() over (order by TENGPCD) as IDX, TA1.* from INAMS.CSVMSTSHNTENBMN AS TA1 where SEQ = ? and INPUTNO = ? order by TENGPCD LIMIT @M ) T2 on T1.IDX = T2.IDX"
      + " left outer join INAMS.MSTSHNTENGP T3 on T2.TENGPCD = T3.TENGPCD and T2.AREAKBN = T3.AREAKBN and T3.GPKBN = " + ValGpkbn.BAIKA.getVal() + " and T3.BMNCD = ? and COALESCE(T3.UPDKBN,0) <> 1 ";

  // SQL：添加物 TODO:
  /** 共通（INAMS.MSTTENKABUTSU） */
  public final static String ID_SQL_ALLERGY2 = "select T1.VALUE, T1.TEXT, case when T2.TENKABCD is null then '' else '1' end as SEL from (" + DefineReport.ID_SQL_MEISYO + ") T1"
      + " left outer join @T T2 on T2.TENKABCD = T1.VALUE @W order by VALUE";
  public final static String ID_SQL_TENKABUTSU2 = ID_SQL_GRD_CMN + "select T2.TENKABCD2||'" + SEPARATOR + "'||RTRIM(T3.NMKN) as TENKABCD, T2.TENKABCD2 as TENKABCD2 from T1"
      + " left outer join (select ROW_NUMBER() over (order by TENKABCD) as IDX, right ('0000'||TENKABCD, 5) as TENKABCD2 , TA1.*  from @T AS TA1 where TENKABKBN = ? @W order by TENKABCD LIMIT 10) T2 on T1.IDX = T2.IDX"
      + " left outer join INAMS.MSTMEISHO T3 on T3.MEISHOKBN = " + MeisyoSelect.KBN138.getCd() + " and T2.TENKABCD2= T3.MEISHOCD ";

  // SQL：自動発注
  /** 共通（INAMS.MSTAHS） */
  public final static String ID_SQL_AHS = "select M1.TENCD, M1.TENKN, COALESCE(T1.AHSKB, '" + Values.OFF.getVal() + "') as AHSKB, M2.TENITEMSU, M1.URIAERACD from INAMS.MSTTEN M1"
      + " left outer join INAMS.MSTTENJSKI M2 on M1.TENCD = M2.TENCD " + " left outer join @T T1  on M1.TENCD = T1.TENCD @W" + " where M1.UPDKBN = 0 order by TENCD";

  // SQL：催し_デフォルト設定
  /** 共通（INATK.TOKMOYDEF） */
  public static final String ID_SQL_TOKMOYDEF = "select BMNCD as " + VAL + " from INATK.TOKMOYDEF where COALESCE(UPDKBN, 0) = 0";
  public static final String ID_SQL_TOKMOYDEF2 =
      "select BMNCD, SZKCD, HBSLIDEKBN, NNSLIDEKBN, KSGRPKBN, DSUEXKBN, DRTEXKBN, DZNENDSKBN, DDNENDSKBN, DCUTEXKBN, DCHIRASKBN, DBMNATRKBN from INATK.TOKMOYDEF where COALESCE(UPDKBN, 0) = 0";

  // SQL：週間発注計画_計画計表示
  /** 共通（INAMS.MSTAHS） */
  public final static String ID_SQL_KKH_TJ009 =
      "with WK_YSN as(select LSTNO, TENCD, BMNCD, SUM(COALESCE(BMNYSANAM, 0)) as BMNYSANAM_KEI from INATK.TOKTJ_BMNYSAN group by LSTNO, TENCD, BMNCD) select T3.LSTNO, T3.BMNCD, T3.TENCD, W1.BMNYSANAM_KEI, T3.HYOSEQNO, T3.IRISU_TB, T3.BAIKAAM_TB, T3.GENKAAM_MAE, T3.SHNKBN_01, T3.SHNKBN_02, T3.SHNKBN_03, T3.SHNKBN_04, T3.SHNKBN_05, T3.SHNKBN_06, T3.SHNKBN_07, T3.SHNKBN_08, T3.SHNKBN_09, T3.SHNKBN_10, T3.HTSU_01, T3.HTSU_02, T3.HTSU_03, T3.HTSU_04, T3.HTSU_05, T3.HTSU_06, T3.HTSU_07, T3.HTSU_08, T3.HTSU_09, T3.HTSU_10 from (select LSTNO from INATK.TOKTJ group by LSTNO) T1 inner join INATK.TOKTJ_SHN T2 on T1.LSTNO = T2.LSTNO inner join INATK.TOKTJ_TEN T3 on T2.LSTNO = T3.LSTNO and T2.BMNCD = T3.BMNCD and T2.HYOSEQNO = T3.HYOSEQNO left join WK_YSN W1 on W1.LSTNO = T3.LSTNO and W1.TENCD = T3.TENCD and W1.BMNCD = T3.BMNCD where T3.LSTNO = ? and T3.BMNCD = ? and T3.TENCD = ? ";

  // SQL：B/M番号一覧
  public final static String ID_SQL_BMNNO =
      "SELECT ROW_NUMBER() OVER (ORDER BY MOYSKBN,MOYSSTDT,MOYSRBAN,BMNNO) AS IDX,BMNNO,CASE WHEN (HBSTDTD IS NULL OR HBSTDTD='') AND (HBEDDTD IS NULL OR HBEDDTD='') THEN '' WHEN HBSTDTD = HBEDDTD THEN HBSTDTD || HBSTDTW ELSE HBSTDTD || HBSTDTW || '～' || HBEDDTD || HBEDDTW END AS HBPERIOD,BMNMAN,MOYSKBN,MOYSSTDT,MOYSRBAN FROM (SELECT MOYSKBN,MOYSSTDT,MOYSRBAN,BMNNO,DATE_FORMAT(DATE_FORMAT(HBSTDT,'%Y%m%d'),'%y/%m/%d') AS HBSTDTD,DATE_FORMAT(DATE_FORMAT(HBEDDT,'%Y%m%d'),'%y/%m/%d') AS HBEDDTD,BMNMAN,CASE WHEN DAYOFWEEK(DATE_FORMAT(HBSTDT,'%Y%m%d'))='1' THEN '(日)' WHEN DAYOFWEEK(DATE_FORMAT(HBSTDT,'%Y%m%d'))='2' THEN '(月)' WHEN DAYOFWEEK(DATE_FORMAT(HBSTDT,'%Y%m%d'))='3' THEN '(火)' WHEN DAYOFWEEK(DATE_FORMAT(HBSTDT,'%Y%m%d'))='4' THEN '(水)' WHEN DAYOFWEEK(DATE_FORMAT(HBSTDT,'%Y%m%d'))='5' THEN '(木)' WHEN DAYOFWEEK(DATE_FORMAT(HBSTDT,'%Y%m%d'))='6' THEN '(金)' WHEN DAYOFWEEK(DATE_FORMAT(HBSTDT,'%Y%m%d'))='7' THEN '(土)' END AS HBSTDTW,CASE WHEN DAYOFWEEK(DATE_FORMAT(HBEDDT,'%Y%m%d'))='1' THEN '(日)' WHEN DAYOFWEEK(DATE_FORMAT(HBEDDT,'%Y%m%d'))='2' THEN '(月)' WHEN DAYOFWEEK(DATE_FORMAT(HBEDDT,'%Y%m%d'))='3' THEN '(火)' WHEN DAYOFWEEK(DATE_FORMAT(HBEDDT,'%Y%m%d'))='4' THEN '(水)' WHEN DAYOFWEEK(DATE_FORMAT(HBEDDT,'%Y%m%d'))='5' THEN '(木)' WHEN DAYOFWEEK(DATE_FORMAT(HBEDDT,'%Y%m%d'))='6' THEN '(金)' WHEN DAYOFWEEK(DATE_FORMAT(HBEDDT,'%Y%m%d'))='7' THEN '(土)' END AS HBEDDTW FROM INATK.TOKBM WHERE MOYSKBN=? AND MOYSSTDT = ? AND MOYSRBAN=? AND UPDKBN="
          + DefineReport.ValUpdkbn.NML.getVal() + " ORDER BY MOYSKBN,MOYSSTDT,MOYSRBAN,BMNNO)M";
  // SQL：B/M商品一覧
  public final static String ID_SQL_BMSHN = ID_SQL_GRD_CMN
      + "SELECT T1.IDX,'0' AS UPDKBN, trim(T2.SHNCD) AS SHNCD,T2.SHNKN,T2.GENKAAM,T2.MOYSKBN,T2.MOYSSTDT,T2.MOYSRBAN,T2.BMNNO,T2.KANRINO FROM T1 LEFT JOIN (SELECT ROW_NUMBER() OVER (ORDER BY MOYSKBN,MOYSSTDT,MOYSRBAN,BMNNO,KANRINO,SHNCD) AS IDX,MOYSKBN,MOYSSTDT,MOYSRBAN,BMNNO,KANRINO,SHNCD,SHNKN,GENKAAM FROM (SELECT MOYSKBN,MOYSSTDT,MOYSRBAN,BMNNO,KANRINO,MS.SHNCD,MS.SHNKN,GENKAAM FROM (SELECT MOYSKBN,MOYSSTDT,MOYSRBAN,BMNNO,KANRINO,SHNCD,GENKAAM FROM INATK.TOKBM_SHN WHERE MOYSKBN=? AND MOYSSTDT=? AND MOYSRBAN=? AND BMNNO=? ORDER BY MOYSKBN,MOYSSTDT,MOYSRBAN,BMNNO,KANRINO,SHNCD) BM LEFT JOIN INAMS.MSTSHN MS ON BM.SHNCD=MS.SHNCD)T3) AS T2 ON T1.IDX=T2.IDX ORDER BY T1.IDX";
  // SQL：B/M番号一覧
  public final static String ID_SQL_TOKBM_D1 =
      "SELECT DISTINCT 'D1' D1_1,right('0'||COALESCE(TO_CHAR(BM.MOYSKBN),''),1) D1_2,right('000000'||COALESCE(TO_CHAR(BM.MOYSSTDT),''),6) D1_3,right('000'||COALESCE(TO_CHAR(BM.MOYSRBAN),''),3) D1_4,left(COALESCE(MY.MOYKN,'')||'　　　　　　　　　　　　　　　　　　　　',40) D1_5,right('00'||SUBSTR(COALESCE(TO_CHAR(MY.SHUNO),'0000'),1,2),2) D1_6,right('00'||SUBSTR(COALESCE(TO_CHAR(MY.SHUNO),'0000'),3,2),2) D1_7,right('00000000'||COALESCE(TO_CHAR(MY.HBSTDT),''),8) D1_8,right('00000000'||COALESCE(TO_CHAR(MY.HBEDDT),''),8) D1_9,right('00000000'||COALESCE(TO_CHAR(MY.NNSTDT),''),8) D1_10,right('00000000'||COALESCE(TO_CHAR(MY.NNEDDT),''),8) D1_11 FROM INATK.TOKBM BM LEFT JOIN INATK.TOKMOYCD MY ON BM.MOYSKBN=MY.MOYSKBN AND BM.MOYSSTDT=MY.MOYSSTDT AND BM.MOYSRBAN=MY.MOYSRBAN WHERE BM.MOYSKBN=?AND BM.MOYSSTDT=?AND BM.MOYSRBAN=?AND BM.UPDKBN="
          + DefineReport.ValUpdkbn.NML.getVal();
  // SQL：B/M番号一覧
  public final static String ID_SQL_TOKBM_D2 =
      "SELECT 'D2' D2_1,right('0'||COALESCE(BM.MOYSKBN,''), 1) D2_2,right('000000'||COALESCE(BM.MOYSSTDT,''), 6) D2_3,right('000'||COALESCE(TO_CHAR(BM.MOYSRBAN),''), 3) D2_4,right('0000'||COALESCE(TO_CHAR(BM.BMNNO),''), 4) D2_5,left(COALESCE(BM.BMNMAN,'')||'                    ', 20) D2_6,left(COALESCE(BM.BMNMKN,'')||'　　　　　　　　　　　　　　　　　　　　', 40) D2_7,right('000000'||COALESCE(TO_CHAR(BM.BAIKAAM),''), 6) D2_8,right('000'||COALESCE(TO_CHAR(BM.BD_KOSU1),''), 3) D2_9,right('000000'||COALESCE(TO_CHAR(BM.BD_BAIKAAN1),''), 6) D2_10,right('000'||COALESCE(TO_CHAR(BM.BD_KOSU2),''), 3) D2_11,right('000000'||COALESCE(TO_CHAR(BM.BD_BAIKAAN2),''), 6) D2_12,right('00000000'||COALESCE(TO_CHAR(BM.HBSTDT),''), 8) D2_13,right('00000000'||COALESCE(TO_CHAR(BM.HBEDDT),''), 8) D2_14,right('000'||COALESCE(TO_CHAR(BM.BMNCD_RANK),''), 3) D2_15,right('000'||COALESCE(TO_CHAR(BM.RANKNO_ADD),''), 3) D2_16,right('000'||COALESCE(TO_CHAR(BM.RANKNO_DEL),''), 3) D2_17,right('000'||COALESCE(TO_CHAR(TJ.TENCD),''), 3) D2_18_1,COALESCE(TO_CHAR(TJ.TJFLG),'') D2_18_2 FROM INATK.TOKBM BM LEFT JOIN INATK.TOKBM_TJTEN TJ ON BM.MOYSKBN=TJ.MOYSKBN AND BM.MOYSSTDT=TJ.MOYSSTDT AND BM.MOYSRBAN=TJ.MOYSRBAN AND BM.BMNNO=TJ.BMNNO WHERE BM.MOYSKBN=?AND BM.MOYSSTDT=?AND BM.MOYSRBAN=?AND BM.UPDKBN="
          + DefineReport.ValUpdkbn.NML.getVal() + " ORDER BY BM.MOYSKBN,BM.MOYSSTDT,BM.MOYSRBAN,BM.BMNNO,BM.BMNMAN,TJ.TJFLG DESC,TJ.TENCD";
  // SQL：B/M番号一覧
  public final static String ID_SQL_TOKBM_D3 =
      "SELECT 'D3' D3_1,right('0'||COALESCE(TO_CHAR(BM.MOYSKBN),''), 1) D3_2,right('000000'||COALESCE(TO_CHAR(BM.MOYSSTDT),''), 6) D3_3,right('000'||COALESCE(TO_CHAR(BM.MOYSRBAN),''), 3) D3_4,right('0000'||COALESCE(TO_CHAR(BM.BMNNO),''), 4) D3_5,left(COALESCE(BM.SHNCD,'')||'              ', 14) D3_6,right('00000000'||COALESCE(TO_CHAR(BM.GENKAAM),''), 8) D3_7,left(COALESCE(MS.POPKN,'')||'　　　　　　　　　　　　　　　　　　　　', 40) D3_8,left(COALESCE(MM.MAKERKN,'')||'　　　　　　　　　　　　　　', 28) D3_9,left(COALESCE(MS.KIKKN,'')||'　　　　　　　　　　　　　　　　　　　　　　　', 46) D3_10 FROM INATK.TOKBM_SHN BM LEFT JOIN INAMS.MSTSHN MS ON BM.SHNCD = MS.SHNCD LEFT JOIN INAMS.MSTMAKER MM ON MS.MAKERCD=MM.MAKERCD WHERE BM.MOYSKBN=?AND BM.MOYSSTDT=?AND BM.MOYSRBAN=? ORDER BY BM.MOYSKBN,BM.MOYSSTDT,BM.MOYSRBAN,BM.BMNNO,BM.SHNCD,MM.MAKERCD";

  // SQL：セット番号一覧
  public final static String ID_SQL_SETNO = ID_SQL_GRD_CMN
      + "SELECT T1.IDX,right('0000'||T2.STNO, 4) as STNO ,T2.HBPERIOD,T2.STNM,T2.MOYSKBN,T2.MOYSSTDT,T2.MOYSRBAN FROM T1 LEFT JOIN (SELECT ROW_NUMBER() OVER (ORDER BY MOYSKBN,MOYSSTDT,MOYSRBAN,STNO) AS IDX,STNO,CASE WHEN (HBSTDTD IS NULL OR HBSTDTD='') AND (HBEDDTD IS NULL OR HBEDDTD='') THEN '' WHEN HBSTDTD = HBEDDTD THEN HBSTDTD || HBSTDTW ELSE HBSTDTD || HBSTDTW || '～' || HBEDDTD || HBEDDTW END AS HBPERIOD,STNM,MOYSKBN,MOYSSTDT,MOYSRBAN FROM (SELECT MOYSKBN,MOYSSTDT,MOYSRBAN,STNO,DATE_FORMAT(DATE_FORMAT(HBSTDT,'%Y%m%d'),'%y%m%d') AS HBSTDTD,DATE_FORMAT(DATE_FORMAT(HBEDDT,'%Y%m%d'),'%y%m%d') AS HBEDDTD,STNM,CASE WHEN DAYOFWEEK(DATE_FORMAT(HBSTDT,'%Y%m%d'))='1' THEN '(日)' WHEN DAYOFWEEK(DATE_FORMAT(HBSTDT,'%Y%m%d'))='2' THEN '(月)' WHEN DAYOFWEEK(DATE_FORMAT(HBSTDT,'%Y%m%d'))='3' THEN '(火)' WHEN DAYOFWEEK(DATE_FORMAT(HBSTDT,'%Y%m%d'))='4' THEN '(水)' WHEN DAYOFWEEK(DATE_FORMAT(HBSTDT,'%Y%m%d'))='5' THEN '(木)' WHEN DAYOFWEEK(DATE_FORMAT(HBSTDT,'%Y%m%d'))='6' THEN '(金)' WHEN DAYOFWEEK(DATE_FORMAT(HBSTDT,'%Y%m%d'))='7' THEN '(土)' END AS HBSTDTW,CASE WHEN DAYOFWEEK(DATE_FORMAT(HBEDDT,'%Y%m%d'))='1' THEN '(日)' WHEN DAYOFWEEK(DATE_FORMAT(HBEDDT,'%Y%m%d'))='2' THEN '(月)' WHEN DAYOFWEEK(DATE_FORMAT(HBEDDT,'%Y%m%d'))='3' THEN '(火)' WHEN DAYOFWEEK(DATE_FORMAT(HBEDDT,'%Y%m%d'))='4' THEN '(水)' WHEN DAYOFWEEK(DATE_FORMAT(HBEDDT,'%Y%m%d'))='5' THEN '(木)' WHEN DAYOFWEEK(DATE_FORMAT(HBEDDT,'%Y%m%d'))='6' THEN '(金)' WHEN DAYOFWEEK(DATE_FORMAT(HBEDDT,'%Y%m%d'))='7' THEN '(土)' END AS HBEDDTW FROM INATK.TOKMM_KKK WHERE MOYSKBN=? AND MOYSSTDT = ? AND MOYSRBAN=? AND UPDKBN="
      + DefineReport.ValUpdkbn.NML.getVal() + " ORDER BY MOYSKBN,MOYSSTDT,MOYSRBAN,STNO)T3) AS T2 ON T1.IDX=T2.IDX ORDER BY T1.IDX";
  // SQL :セット販売商品一覧
  public final static String ID_SQL_SETNOLIST = ID_SQL_GRD_CMN
      + "SELECT T1.IDX, trim(T2.SHNCD) AS SHNCD,T2.SHNKN FROM T1 LEFT JOIN (SELECT ROW_NUMBER() OVER (ORDER BY KANRINO,SHNCD) AS IDX,MOYSKBN,MOYSSTDT,MOYSRBAN,STNO,KANRINO,SHNCD,SHNKN FROM (SELECT MOYSKBN,MOYSSTDT,MOYSRBAN,STNO,KANRINO,MS.SHNCD,MS.SHNKN FROM (SELECT MOYSKBN,MOYSSTDT,MOYSRBAN,STNO,SHNCD,KANRINO FROM INATK.TOKMM_SHO WHERE MOYSKBN=? AND MOYSSTDT=? AND MOYSRBAN=? AND STNO=? AND WARIGP = ? ORDER BY MOYSKBN,MOYSSTDT,MOYSRBAN,STNO,KANRINO,SHNCD) TK LEFT JOIN INAMS.MSTSHN MS ON TK.SHNCD=MS.SHNCD)T3) AS T2 ON T1.IDX=T2.IDX ORDER BY T1.IDX";

  // SQL：正規定量商品店一覧
  public final static String ID_SQL_HATSTRSHNTEN =
      "SELECT * FROM(SELECT 1 AS NUM,'月' AS WEEK,MEISHOCD||'" + SEPARATOR + "'||RTRIM(NMKN) AS TEISEIKBN4,0 AS SURYO FROM INAMS.MSTMEISHO WHERE MEISHOKBN = '" + MeisyoSelect.KBN105014.getCd()
          + "' AND MEISHOCD='0' UNION ALL SELECT 2 AS NUM,'火' AS WEEK,MEISHOCD||'" + SEPARATOR + "'||RTRIM(NMKN) AS TEISEIKBN4,0 AS SURYO FROM INAMS.MSTMEISHO WHERE MEISHOKBN = '"
          + MeisyoSelect.KBN105014.getCd() + "' AND MEISHOCD='0' UNION ALL SELECT 3 AS NUM,'水' AS WEEK,MEISHOCD||'" + SEPARATOR
          + "'||RTRIM(NMKN) AS TEISEIKBN4,0 AS SURYO FROM INAMS.MSTMEISHO WHERE MEISHOKBN = '" + MeisyoSelect.KBN105014.getCd() + "' AND MEISHOCD='0' UNION ALL SELECT 4 AS NUM,'木' AS WEEK,MEISHOCD||'"
          + SEPARATOR + "'||RTRIM(NMKN) AS TEISEIKBN4,0 AS SURYO FROM INAMS.MSTMEISHO WHERE MEISHOKBN = '" + MeisyoSelect.KBN105014.getCd()
          + "' AND MEISHOCD='0' UNION ALL SELECT 5 AS NUM,'金' AS WEEK,MEISHOCD||'" + SEPARATOR + "'||RTRIM(NMKN) AS TEISEIKBN4,0 AS SURYO FROM INAMS.MSTMEISHO WHERE MEISHOKBN = '"
          + MeisyoSelect.KBN105014.getCd() + "' AND MEISHOCD='0' UNION ALL SELECT 6 AS NUM,'土' AS WEEK,MEISHOCD||'" + SEPARATOR
          + "'||RTRIM(NMKN) AS TEISEIKBN4,0 AS SURYO FROM INAMS.MSTMEISHO WHERE MEISHOKBN = '" + MeisyoSelect.KBN105014.getCd() + "' AND MEISHOCD='0' UNION ALL SELECT 7 AS NUM,'日' AS WEEK,MEISHOCD||'"
          + SEPARATOR + "'||RTRIM(NMKN) AS TEISEIKBN4,0 AS SURYO FROM INAMS.MSTMEISHO WHERE MEISHOKBN = '" + MeisyoSelect.KBN105014.getCd() + "' AND MEISHOCD='0') AS T1 ORDER BY NUM";
  // SQL：正規定量商品店一覧
  public final static String ID_SQL_HATSTRSHNTEN2 =
      "SELECT TEN.TENCD,MST.TENKN,TEN.SURYO_MON,TEN.SURYO_TUE,TEN.SURYO_WED,TEN.SURYO_THU,TEN.SURYO_FRI,TEN.SURYO_SAT,TEN.SURYO_SUN FROM INATK.HATSTR_TEN TEN LEFT JOIN INAMS.MSTTEN MST ON TEN.TENCD=MST.TENCD WHERE TEN.SHNCD=?AND TEN.BINKBN=?AND TEN.UPDKBN="
          + DefineReport.ValUpdkbn.NML.getVal() + " ORDER BY TEN.TENCD";
  // SQL：入力済商品一覧
  public final static String ID_SQL_HATSTRTEN = "WITH SHORI as(" + ID_SQLSHORIDT
      + ") SELECT TN.SHNCD,MS.SHNKN,TN.SURYO_MON,TN.SURYO_TUE,TN.SURYO_WED,TN.SURYO_THU,TN.SURYO_FRI,TN.SURYO_SAT,TN.SURYO_SUN,DATE_FORMAT(TN.ADDDT,'%y/%m/%d') AS ADDDT,DATE_FORMAT(TN.UPDDT,'%y/%m/%d') AS UPDDT,TN.OPERATOR FROM INATK.HATSTR_TEN TN,INAMS.MSTSHN MS,SHORI WHERE TN.SHNCD=MS.SHNCD AND DATE_FORMAT(DATE_FORMAT(TN.UPDDT,'%Y%m%d'),'%Y/%m/%d')=DATE_FORMAT(SHORI."
      + VAL + ",'%Y/%m/%d') AND TN.TENCD=? AND COALESCE(TN.UPDKBN, 0) <> 1 AND COALESCE(MS.UPDKBN, 0) <> 1 ORDER BY TN.SHNCD";
  public final static String ID_SQL_HATJTRTEN = "WITH SHORI as(" + ID_SQLSHORIDT
      + ") SELECT TN.SHNCD,MS.SHNKN,TN.SURYO_MON,TN.SURYO_TUE,TN.SURYO_WED,TN.SURYO_THU,TN.SURYO_FRI,TN.SURYO_SAT,TN.SURYO_SUN,DATE_FORMAT(TN.ADDDT,'%y/%m/%d') AS ADDDT,DATE_FORMAT(TN.UPDDT,'%y/%m/%d') AS UPDDT,TN.OPERATOR FROM INATK.HATJTR_TEN TN,INAMS.MSTSHN MS,SHORI WHERE TN.SHNCD=MS.SHNCD AND DATE_FORMAT(DATE_FORMAT(TN.UPDDT,'%Y%m%d'),'%Y/%m/%d')=DATE_FORMAT(SHORI."
      + VAL + ",'%Y/%m/%d') AND TN.TENCD=? AND TN.SHUNO=? AND COALESCE(MS.UPDKBN, 0) <> 1 ORDER BY TN.SHNCD";

  // SQL：風袋マスタリスト TODO:
  /** 共通（INAMS.MSTFTAI） */
  public final static String ID_SQL_FUTAI =
      ID_SQL_GRD_CMN + "select  MSFT.FTAISHUKBN, MSFT.FTAIECD,CONCAT(CONCAT(CONCAT(MSFT.FTAISHUKBN, MSFT.FTAIECD),'-' ),MSFT.FTAIKN ) as FUTAI  from INAMS.MSTFTAI MSFT where MSFT.FTAISHUKBN = ";
  public final static String ID_SQL_FUTAI_TAIL = " order by MSFT.FTAIECD";
  /** 共通（INAMS.MSTKRYOFTAI） */
  public final static String ID_SQL_KRYOFUTAI = ID_SQL_GRD_CMN
      + "select MSKF.FTAISHUKBN,MSKF.FTAIECD, CONCAT(CONCAT(CONCAT(MSKF.FTAISHUKBN, MSKF.FTAIECD),'-' ),MSFT.FTAIKN ) as FUTAI  from INAMS.MSTKRYOFTAI MSKF left join INAMS.MSTFTAI MSFT ON MSKF.FTAISHUKBN = MSFT.FTAISHUKBN AND MSKF.FTAIECD = MSFT.FTAIECD where MSKF.YOBIDASHICD = ";
  public final static String ID_SQL_KRYOFUTAI_TAIL = " order by MSFT.FTAIECD";

  // SQL：プライスカード発行枚数
  public final static String ID_SQL_TRNPCARDMAISU = ID_SQL_GRD_CMN
      + "SELECT T1.IDX,T2.UPDKBN,T2.SHNCD,T2.SHNKN,T2.TENCD,T2.TENKN,T2.BMNCD,T2.BMNKN,T2.MAISU,T2.KOSEPAGE,T2.SEQ FROM T1 LEFT JOIN (SELECT ROW_NUMBER() OVER (ORDER BY PCMAISU.SHNCD) AS IDX,PCMAISU.UPDKBN,PCMAISU.SHNCD,CASE WHEN SHNM.PCARDKN IS NULL OR SHNM.PCARDKN = '' THEN '　' ELSE SHNM.PCARDKN END AS SHNKN,PCMAISU.TENCD,CASE WHEN TENM.TENKN IS NULL OR TENM.TENKN = '' THEN '　' ELSE TENM.TENKN END AS TENKN,PCMAISU.BMNCD,CASE WHEN BMNM.BMNKN IS NULL OR BMNM.BMNKN = '' THEN '　' ELSE BMNM.BMNKN END AS BMNKN,PCMAISU.MAISU,PCMAISU.KOSEPAGE,PCMAISU.SEQ FROM INAMS.TRNPCARDSU AS PCMAISU LEFT JOIN INAMS.MSTSHN AS SHNM ON PCMAISU.SHNCD=SHNM.SHNCD LEFT JOIN INAMS.MSTTEN AS TENM ON PCMAISU.TENCD=TENM.TENCD LEFT JOIN INAMS.MSTBMN AS BMNM ON PCMAISU.BMNCD=BMNM.BMNCD WHERE PCMAISU.INPUTNO=? AND PCMAISU.UPDKBN=? ORDER BY PCMAISU.SHNCD) AS T2 ON T1.IDX=T2.IDX ORDER BY T1.IDX";

  // SQL：数量パターン
  /** 数量パターン(INATK.TOKSRPTN) */
  public final static String ID_SQL_SURYO =
      "select  '' as F1,'' as F2,'店舗数' as F3,(LENGTH(TKRK.TENRANK_ARR)- LENGTH(REPLACE(TKRK.TENRANK_ARR, 'A', '')))/ LENGTH('A') as F4 ,(LENGTH(TKRK.TENRANK_ARR)- LENGTH(REPLACE(TKRK.TENRANK_ARR, 'B', '')))/ LENGTH('B')  as F5 ,(LENGTH(TKRK.TENRANK_ARR)- LENGTH(REPLACE(TKRK.TENRANK_ARR, 'C', '')))/ LENGTH('C')  as F6 ,(LENGTH(TKRK.TENRANK_ARR)- LENGTH(REPLACE(TKRK.TENRANK_ARR, 'D', '')))/ LENGTH('D')  as F7 ,(LENGTH(TKRK.TENRANK_ARR)- LENGTH(REPLACE(TKRK.TENRANK_ARR, 'E', '')))/ LENGTH('E')  as F8 ,(LENGTH(TKRK.TENRANK_ARR)- LENGTH(REPLACE(TKRK.TENRANK_ARR, 'F', '')))/ LENGTH('F')  as F9 ,(LENGTH(TKRK.TENRANK_ARR)- LENGTH(REPLACE(TKRK.TENRANK_ARR, 'G', '')))/ LENGTH('G')  as F10 ,(LENGTH(TKRK.TENRANK_ARR)- LENGTH(REPLACE(TKRK.TENRANK_ARR, 'H', '')))/ LENGTH('H')  as F11 ,(LENGTH(TKRK.TENRANK_ARR)- LENGTH(REPLACE(TKRK.TENRANK_ARR, 'I', '')))/ LENGTH('I')  as F12 ,(LENGTH(TKRK.TENRANK_ARR)- LENGTH(REPLACE(TKRK.TENRANK_ARR, 'J', '')))/ LENGTH('J')  as F13 ,(LENGTH(TKRK.TENRANK_ARR)- LENGTH(REPLACE(TKRK.TENRANK_ARR, 'K', '')))/ LENGTH('K')  as F14 ,(LENGTH(TKRK.TENRANK_ARR)- LENGTH(REPLACE(TKRK.TENRANK_ARR, 'L', '')))/ LENGTH('L')  as F15 ,(LENGTH(TKRK.TENRANK_ARR)- LENGTH(REPLACE(TKRK.TENRANK_ARR, 'M', '')))/ LENGTH('M')  as F16 ,(LENGTH(TKRK.TENRANK_ARR)- LENGTH(REPLACE(TKRK.TENRANK_ARR, 'N', '')))/ LENGTH('N')  as F17 ,(LENGTH(TKRK.TENRANK_ARR)- LENGTH(REPLACE(TKRK.TENRANK_ARR, 'O', '')))/ LENGTH('O') as F18 ,(LENGTH(TKRK.TENRANK_ARR)- LENGTH(REPLACE(TKRK.TENRANK_ARR, 'P', '')))/ LENGTH('P') as F19 ,(LENGTH(TKRK.TENRANK_ARR)- LENGTH(REPLACE(TKRK.TENRANK_ARR, 'Q', '')))/ LENGTH('Q') as F20 ,(LENGTH(TKRK.TENRANK_ARR)- LENGTH(REPLACE(TKRK.TENRANK_ARR, 'R', '')))/ LENGTH('R') as F21 ,(LENGTH(TKRK.TENRANK_ARR)- LENGTH(REPLACE(TKRK.TENRANK_ARR, 'S', '')))/ LENGTH('S') as F22 ,"
          + "(LENGTH(TKRK.TENRANK_ARR)- LENGTH(REPLACE(TKRK.TENRANK_ARR, 'T', '')))/ LENGTH('T') as F23 ,(LENGTH(TKRK.TENRANK_ARR)- LENGTH(REPLACE(TKRK.TENRANK_ARR, 'U', '')))/ LENGTH('U') as F24 ,(LENGTH(TKRK.TENRANK_ARR)- LENGTH(REPLACE(TKRK.TENRANK_ARR, 'V', '')))/ LENGTH('V') as F25 ,(LENGTH(TKRK.TENRANK_ARR)- LENGTH(REPLACE(TKRK.TENRANK_ARR, 'W', '')))/ LENGTH('W') as F26  ,(LENGTH(TKRK.TENRANK_ARR)- LENGTH(REPLACE(TKRK.TENRANK_ARR, 'X', '')))/ LENGTH('X') as F27 ,(LENGTH(TKRK.TENRANK_ARR)- LENGTH(REPLACE(TKRK.TENRANK_ARR, 'Y', '')))/ LENGTH('Y') as F28 ,(LENGTH(TKRK.TENRANK_ARR)- LENGTH(REPLACE(TKRK.TENRANK_ARR, 'Z', '')))/ LENGTH('Z') as F29  from INATK.TOKRANK TKRK where TKRK.BMNCD = ? and TKRK.RANKNO = ? union"
          + " select right ('00'||TSPN.SRYPTNNO, 3) ,TSPN.SRYPTNKN ,char(sum(TSRK.SURYO)) , sum(case when TSRK.TENRANK = 'A' then TSRK.SURYO END),sum(case when TSRK.TENRANK = 'B' then TSRK.SURYO END) ,sum(case when TSRK.TENRANK = 'C' then TSRK.SURYO END) ,sum(case when TSRK.TENRANK = 'D' then TSRK.SURYO END) ,sum(case when TSRK.TENRANK = 'E' then TSRK.SURYO END) ,sum(case when TSRK.TENRANK = 'F' then TSRK.SURYO END) ,sum(case when TSRK.TENRANK = 'G' then TSRK.SURYO END) ,sum(case when TSRK.TENRANK = 'H' then TSRK.SURYO END) ,sum(case when TSRK.TENRANK = 'I' then TSRK.SURYO END) ,sum(case when TSRK.TENRANK = 'J' then TSRK.SURYO END) ,sum(case when TSRK.TENRANK = 'K' then TSRK.SURYO END) ,sum(case when TSRK.TENRANK = 'L' then TSRK.SURYO END) ,sum(case when TSRK.TENRANK = 'M' then TSRK.SURYO END) ,sum(case when TSRK.TENRANK = 'N' then TSRK.SURYO END) ,sum(case when TSRK.TENRANK = 'O' then TSRK.SURYO END) ,sum(case when TSRK.TENRANK = 'P' then TSRK.SURYO END) ,sum(case when TSRK.TENRANK = 'Q' then TSRK.SURYO END) ,sum(case when TSRK.TENRANK = 'R' then TSRK.SURYO END) ,sum(case when TSRK.TENRANK = 'S' then TSRK.SURYO END) ,sum(case when TSRK.TENRANK = 'T' then TSRK.SURYO END) ,sum(case when TSRK.TENRANK = 'U' then TSRK.SURYO END) ,sum(case when TSRK.TENRANK = 'V' then TSRK.SURYO END) ,sum(case when TSRK.TENRANK = 'W' then TSRK.SURYO END) ,sum(case when TSRK.TENRANK = 'X' then TSRK.SURYO END) ,sum(case when TSRK.TENRANK = 'Y' then TSRK.SURYO END) ,sum(case when TSRK.TENRANK = 'Z' then TSRK.SURYO END)  from INATK.TOKSRPTN TSPN left join INATK.TOKSRYRANK TSRK on TSRK.BMNCD = ? where TSPN.BMNCD = ? and TSRK.BMNCD =? group by TSRK.BMNCD, TSRK.SRYPTNNO,TSPN.SRYPTNKN,TSPN.SRYPTNNO  order by F1";
  public final static String ID_SQL_TENHTSU_ARR =
      "select T1.TENCD,case when SUBSTRING(T2.TENHTSU_ARR, 1 + (T1.TENCD - 1) * 5, 5) = '' then null else T1.TENKN END as TENKN,case when SUBSTRING(T2.TENHTSU_ARR, 1 + (T1.TENCD - 1) * 5, 5) = '' then null else cast(SUBSTRING(T2.TENHTSU_ARR, 1 + (T1.TENCD - 1) * 5, 5) as INTEGER) END as SURYO from INAMS.MSTTEN as T1 left join  ? T2 on T2.MOYSKBN = ? and T2.MOYSSTDT = ? and T2.MOYSRBAN = ? and T2.KANRINO = ? where T1.TENCD <= ? and T1.MISEUNYOKBN <> 9 and T1.UPDKBN = 0 order by right('00'||T1.TENCD,3)";
  // 店発注数配列
  public final static String ID_SQL_SURYO2 =
      "select T1.SRYPTNNO as F1,max(T1.SRYPTNKN) as F2,sum(T2.SURYO) as F3 @C, T1.SRYPTNNO as IDX" + " from (select BMNCD,SRYPTNNO,SRYPTNKN from INATK.TOKSRPTN where BMNCD = ? and UPDKBN = 0) T1"
          + " left outer join INATK.TOKSRYRANK T2 on T1.BMNCD = T2.BMNCD and T1.SRYPTNNO = T2.SRYPTNNO" + " group by T1.SRYPTNNO";
  public final static String ID_SQL_SURYO2EX = "select T1.SRYPTNNO as F1,max(T1.SRYPTNKN) as F2,sum(T2.SURYO) as F3 @C , T1.SRYPTNNO as IDX"
      + " from (select BMNCD,MOYSKBN,MOYSSTDT,MOYSRBAN,SRYPTNNO,SRYPTNKN from INATK.TOKSRPTNEX where UPDKBN = 0 and BMNCD = ? and MOYSKBN = ? and MOYSSTDT = ? and MOYSRBAN = ?) T1"
      + " left outer join INATK.TOKSRYRANKEX T2 on T1.BMNCD = T2.BMNCD and T1.MOYSKBN = T2.MOYSKBN and T1.MOYSSTDT = T2.MOYSSTDT and T1.MOYSRBAN = T2.MOYSRBAN and T1.SRYPTNNO = T2.SRYPTNNO"
      + " group by T1.SRYPTNNO";

  // SQL：率パターン
  /** 通常率パターン(INATK.TOKRTPTN) */
  public final static String ID_SQL_RTPTN = "select RTPTNNO as F1, RTPTNKN as F2 from INATK.TOKRTPTN where UPDKBN = 0 and BMNCD = ? order by RTPTNNO";

  // SQL：ランク
  /** ランクマスタ(INATK.TOKRANK) */
  public final static String ID_SQL_RANK =
      "select right('000'||RANKNO,3) as F1, RANKKN as F2,length(REGEXP_REPLACE(TENRANK_ARR, '[^A-Z]', '')) as F3, RANKNO as F4 from INATK.TOKRANK where COALESCE(UPDKBN, 0) <> 1 and BMNCD = ? order by RANKNO";
  /** 臨時ランクマスタ(INATK.TOKRANKEX) */
  public final static String ID_SQL_RANKEX =
      "select right('000'||RANKNO,3) as F1, RANKKN as F2,length(REGEXP_REPLACE(TENRANK_ARR, '[^A-Z]', '')) as F3, RANKNO as F4, MOYSKBN as F5, MOYSSTDT as F6, MOYSRBAN as F7 "
          + " from INATK.TOKRANKEX where COALESCE(UPDKBN, 0) <> 1 and BMNCD = ? and MOYSKBN = ? and MOYSSTDT = ? and MOYSRBAN = ? order by RANKNO";
  /** ランク・臨時マスタ共通 */
  public final static String ID_SQL_RANK_SELECT = "SELECT BMNCD,TENRANK_ARR ";
  public final static String ID_SQL_RANKARR_SELECT = "SELECT TENRANK_ARR AS VALUE ";
  public final static String ID_SQL_RANK_FROM = "FROM INATK.TOKRANK ";
  public final static String ID_SQL_RANKEX_FROM = "FROM INATK.TOKRANKEX ";
  public final static String ID_SQL_RANK_FROM_SP = "FROM INATK.TOKSP_SHN ";
  public final static String ID_SQL_RANK_FROM_TG = "FROM INATK.TOKTG_SHN ";
  public final static String ID_SQL_RANK_WHERE = "WHERE BMNCD=? AND RANKNO=? ";
  public final static String ID_SQL_RANKEX_WHERE = "WHERE BMNCD=? AND MOYSKBN=? AND MOYSSTDT=? AND MOYSRBAN=? AND RANKNO=? ";
  public final static String ID_SQL_RANK_WHERE_SHN = "WHERE MOYSKBN=? AND MOYSSTDT=? AND MOYSRBAN=? AND BMNCD=? AND KANRINO=? ";

  // // SQL：店情報
  // public final static String ID_SQL_TENINFO_NEW = "select
  // right('000'||T1.TENCD,3) as F1, (case when (T1.MISEUNYOKBN <> 9 and
  // T2.TENCD IS NOT NULL) then T1.TENKN else NULL end) as F2, NULL as F3,
  // NULL as F4, T2.AREACD as F5 from INAMS.MSTTEN T1 left outer join (select
  // TENCD, BMNCD, AREACD from INAMS.MSTTENBMN where BMNCD=? and COALESCE(UPDKBN,
  // 0) <> 1) T2 on T1.TENCD=T2.TENCD where T1.TENCD <=400";
  // public final static String ID_SQL_TENINFO_CHANGE = "select
  // right('000'||T1.TENCD,3) as F1, (case when (T1.MISEUNYOKBN <> 9 and
  // T2.TENCD IS NOT NULL) then T1.TENKN else NULL end) as F2, (case when ? IS
  // NOT NULL then end) as F3, NULL as F4, T2.AREACD as F5 from INAMS.MSTTEN
  // T1 left outer join (select TENCD, BMNCD, AREACD from INAMS.MSTTENBMN
  // where BMNCD=? and COALESCE(UPDKBN, 0) <> 1) T2 on T1.TENCD=T2.TENCD where
  // T1.TENCD <=400";
  // public final static String ID_SQL_TENINFO_CHANGE_EX = "select
  // right('000'||T1.TENCD,3) as F1, (case when (T1.MISEUNYOKBN <> 9 and
  // T2.TENCD IS NOT NULL) then T1.TENKN else NULL end) as F2, NULL as F3,
  // NULL as F4, T2.AREACD as F5 from INAMS.MSTTEN T1 left outer join (select
  // TENCD, BMNCD, AREACD from INAMS.MSTTENBMN where BMNCD=? and COALESCE(UPDKBN,
  // 0) <> 1) T2 on T1.TENCD=T2.TENCD where T1.TENCD <=400";

  // SQL：店番一括入力
  public final static String ID_SQL_TENCDIINPUT = "select NULL as TENCD from INAMS.MSTTEN";
  // SQL：店別数量展開
  public final static String ID_SQL_TENBETUSU = "select TEN.TENCD, TEN.TENKN, 0 as SURYO from INAMS.MSTTEN TEN where TEN.MISEUNYOKBN <> 9 and TEN.TENCD <= 400";

  // SQL：店番一括入力チェック
  public final static String ID_SQL_TENBMN = "select COUNT(1) as VALUE from INAMS.MSTTEN T1 left join INAMS.MSTTENBMN T2 on T1.TENCD = T2.TENCD and T2.UPDKBN <> 1 where T1.TENCD = ? and T2.BMNCD = ? "
      + "and T1.MISEUNYOKBN <> 9 and T1.TENCD <= 400 and T1.UPDKBN <> 1 ";

  // // TODO
  // // SQL：実績率パターン店別分配率
  // public final static String ID_SQL_JRTPTNTENBETUBRT = "select TEN.TENCD as
  // F1, case when TEN.MISEUNYOKBN <> 9 then TEN.TENKN else NULL end as F2, 0
  // as F3, 0 as F4 from INAMS.MSTTEN TEN where TEN.TENCD <= 400 and
  // COALESCE(TEN.UPDKBN, 0) = 0 order by TEN.TENCD";
  // // SQL：通常率パターン店別分配率
  // public final static String ID_SQL_RTPTNTENBETUBRT_NEW = "select TEN.TENCD
  // as F1, case when TEN.MISEUNYOKBN <> 9 then TEN.TENKN else NULL end as F2,
  // NULL as F3, NULL as F4, 0 as F5 from INAMS.MSTTEN TEN where TEN.TENCD <=
  // 400 and COALESCE(TEN.UPDKBN, 0) = 0 order by TEN.TENCD";
  // public final static String ID_SQL_RTPTNTENBETUBRT_CHANGE = "select
  // TEN.TENCD as F1, case when TEN.MISEUNYOKBN <> 9 then TEN.TENKN else NULL
  // end as F2, NULL as F3, NULL as F4, 0 as F5 from INAMS.MSTTEN TEN where
  // TEN.TENCD <= 400 and COALESCE(TEN.UPDKBN, 0) = 0 order by TEN.TENCD";

  // SQL：ランク別数量
  public final static String ID_SQL_RANKSURYO_NEW =
      "select 'A' as 'RANK' , 0 as SURYO from (SELECT 1 AS DUMMY) DUMMY union all  select 'B' as 'RANK' , 0 as SURYO from (SELECT 1 AS DUMMY) DUMMY union all select 'C' as 'RANK' , 0 as SURYO from (SELECT 1 AS DUMMY) DUMMY union all select 'D' as 'RANK' , 0 as SURYO from (SELECT 1 AS DUMMY) DUMMY union all select 'E' as 'RANK' , 0 as SURYO from (SELECT 1 AS DUMMY) DUMMY union all select 'F' as 'RANK' , 0 as SURYO from (SELECT 1 AS DUMMY) DUMMY union all select 'G' as 'RANK' , 0 as SURYO from (SELECT 1 AS DUMMY) DUMMY union all select 'H' as 'RANK' , 0 as SURYO from (SELECT 1 AS DUMMY) DUMMY union all select 'I' as 'RANK' , 0 as SURYO from (SELECT 1 AS DUMMY) DUMMY union all select 'J' as 'RANK' , 0 as SURYO from (SELECT 1 AS DUMMY) DUMMY union all select 'K' as 'RANK' , 0 as SURYO from (SELECT 1 AS DUMMY) DUMMY union all select 'L' as 'RANK' , 0 as SURYO from (SELECT 1 AS DUMMY) DUMMY union all select 'M' as 'RANK' , 0 as SURYO from (SELECT 1 AS DUMMY) DUMMY union all select 'N' as 'RANK' , 0 as SURYO from (SELECT 1 AS DUMMY) DUMMY union all select 'O' as 'RANK' , 0 as SURYO from (SELECT 1 AS DUMMY) DUMMY union all select 'P' as 'RANK' , 0 as SURYO from (SELECT 1 AS DUMMY) DUMMY union all select 'Q' as 'RANK' , 0 as SURYO from (SELECT 1 AS DUMMY) DUMMY union all select 'R' as 'RANK' , 0 as SURYO from (SELECT 1 AS DUMMY) DUMMY union all select 'S' as 'RANK' , 0 as SURYO from (SELECT 1 AS DUMMY) DUMMY union all select 'T' as 'RANK' , 0 as SURYO from (SELECT 1 AS DUMMY) DUMMY union all select 'U' as 'RANK' , 0 as SURYO from (SELECT 1 AS DUMMY) DUMMY union all select 'V' as 'RANK' , 0 as SURYO from (SELECT 1 AS DUMMY) DUMMY union all select 'W' as 'RANK' , 0 as SURYO from (SELECT 1 AS DUMMY) DUMMY union all select 'X' as 'RANK' , 0 as SURYO from (SELECT 1 AS DUMMY) DUMMY union all select 'Y' as 'RANK' , 0 as SURYO from (SELECT 1 AS DUMMY) DUMMY union all select 'Z' as 'RANK' , 0 as SURYO from (SELECT 1 AS DUMMY) DUMMY order by 'RANK'";
  public final static String ID_SQL_RANKSURYO_CHANGE =
      "select SRYRANK.TENRANK as 'RANK', SRYRANK.SURYO as SURYO from INATK.TOKSRYRANK SRYRANK where SRYRANK.BMNCD = ? and SRYRANK.SRYPTNNO = ? order by SRYRANK.TENRANK";
  public final static String ID_SQL_RANKSURYO_CHANGE_EX =
      "select SRYRANKEX.TENRANK as 'RANK', SRYRANKEX.SURYO from INATK.TOKSRYRANKEX SRYRANKEX where SRYRANKEX.BMNCD = ? and SRYRANKEX.SRYPTNNO = ? and SRYRANKEX.MOYSKBN = ? and SRYRANKEX.MOYSSTDT = ? and SRYRANKEX.MOYSRBAN = ? order by SRYRANKEX.TENRANK";

  // SQL：社員情報
  /** ログイン管理テーブル（INAAD.SYSLOGIN T1） */
  public final static String ID_SQL_SYSLOGIN =
      "select T1.SHAINCD as VALUE, RIGHT('00'||ROW_NUMBER() over(order by T1.SIMEIKN COLLATE utf8mb4_ja_0900_as_cs_ks ),2)||T1.SIMEIKN as TEXT, T1.SIMEIKN as TEXT2, RIGHT('00'||ROW_NUMBER() over(order by T1.SIMEIKN COLLATE utf8mb4_ja_0900_as_cs_ks ),2) as TEXT3 from (SELECT USER_ID AS SHAINCD, CASE WHEN YOBI_3 IS NULL OR YOBI_3='' THEN 0 ELSE CAST(YOBI_3 AS SIGNED) END AS SZKCD, NM_FAMILY || NM_NAME AS SIMEIKN FROM KEYSYS.SYS_USERS) T1";
  public final static String ID_SQL_SYSLOGIN2 =
      "select T1.SHAINCD as VALUE, T1.SHAINCD||T1.SIMEIKN as TEXT, T1.SIMEIKN as TEXT2, T1.SHAINCD as TEXT3 from (SELECT USER_ID AS SHAINCD, CASE WHEN YOBI_3 IS NULL OR YOBI_3='' THEN 0 ELSE CAST(YOBI_3 AS SIGNED) END AS SZKCD, NM_FAMILY || NM_NAME AS SIMEIKN FROM KEYSYS.SYS_USERS) T1";
  public final static String ID_SQL_SYSLOGIN_WHERE_MOY = " inner join INATK.TOKMOYDEF T2 on T1.SZKCD  = T2.SZKCD and T2.BMNCD = ? ";
  public final static String ID_SQL_SYSLOGIN_WHERE_LIKE = " where RIGHT('00'||ROW_NUMBER() over(order by T1.SIMEIKN),2)||':'||T1.SIMEIKN like ? ";
  public final static String ID_SQL_SYSLOGIN_FOOTER = " order by case VALUE when '-1' then VALUE else TEXT end ";

  public final static String ID_SQL_BUMONYOSAN =
      "WITH WEEK as ( select CWEEK , JWEEK as JWEEK from ( values (1, '日') , (2, '月') , (3, '火') , (4, '水') , (5, '木') , (6, '金') , (7, '土') ) as TMP(CWEEK, JWEEK) ) select T2.JTDT_01 as N1 ,T2.JTDT_02 as N2 ,T2.JTDT_03 as N3 ,T2.JTDT_04 as N4 ,T2.JTDT_05 as N5 ,T2.JTDT_06 as N6 ,T2.JTDT_07 as N7 ,T2.JTDT_08 as N8 ,T2.JTDT_09 as N9 ,T2.JTDT_10 as N10 ,(select JWEEK from WEEK where CWEEK = DAYOFWEEK(TO_DATE(T2.JTDT_01, 'YYYYMMDD'))) as N11 ,(select JWEEK from WEEK where CWEEK = DAYOFWEEK(TO_DATE(T2.JTDT_02, 'YYYYMMDD'))) as N12 ,(select JWEEK from WEEK where CWEEK = DAYOFWEEK(TO_DATE(T2.JTDT_03, 'YYYYMMDD'))) as N13 ,(select JWEEK from WEEK where CWEEK = DAYOFWEEK(TO_DATE(T2.JTDT_04, 'YYYYMMDD'))) as N14 ,(select JWEEK from WEEK where CWEEK = DAYOFWEEK(TO_DATE(T2.JTDT_05, 'YYYYMMDD'))) as N15 ,(select JWEEK from WEEK where CWEEK = DAYOFWEEK(TO_DATE(T2.JTDT_06, 'YYYYMMDD'))) as N16 ,(select JWEEK from WEEK where CWEEK = DAYOFWEEK(TO_DATE(T2.JTDT_07, 'YYYYMMDD'))) as N17 ,(select JWEEK from WEEK where CWEEK = DAYOFWEEK(TO_DATE(T2.JTDT_08, 'YYYYMMDD'))) as N18 ,(select JWEEK from WEEK where CWEEK = DAYOFWEEK(TO_DATE(T2.JTDT_09, 'YYYYMMDD'))) as N19 ,(select JWEEK from WEEK where CWEEK = DAYOFWEEK(TO_DATE(T2.JTDT_10, 'YYYYMMDD'))) as N20 from INATK.TOKTJ_BMNYSAN T1 left join INATK.TOKTJ_TEN T2 on T2.LSTNO = T1.LSTNO and T2.BMNCD = T1.BMNCD and T2.TENCD = T1.TENCD where T1.LSTNO = 170501 and T1.TENCD = 210 and T1.BMNCD = 12 order by T1.TJDT";

  /** 添付資料（MD03100901）の商品コード付番機能 */
  public final static String ID_SQL_MD03100901 = "select min(trim(T1.SHNCD)) as VALUE from (SELECT * FROM INAAD.SYSSHNCD_AKI) T1 where T1.USEFLG = 0 and T1.SHNCD like ? @W";
  public final static String ID_SQL_MD03100901_EXISTS_AUTO =
      "select 'X' from INAAD.SYSSHNCD_FU T2 where CAST(left(@C, 2) AS signed) = T2.BMNCD and CAST(substr(@C, 3, 5) AS signed) between T2.STARTNO and T2.ENDNO";
  public final static String ID_SQL_MD03100901_WHERE_AUTO = "and exists(" + ID_SQL_MD03100901_EXISTS_AUTO + ")";

  /** 添付資料（MD03100902）の販売コード付番機能 */
  public final static String ID_SQL_MD03100902 = "with FU as (select STARTNO, ENDNO, SUMINO from INAAD.SYSURICD_FU LIMIT 1)"
      + ", AKI as (select MIN(T1.URICD) as MIN_AKI_URICD from INAAD.SYSURICD_AKI T1 inner join FU T2 on T1.URICD between T2.STARTNO and T2.ENDNO and COALESCE(T1.USEFLG, 0) <> 1)"
      + " select case when ENDNO > SUMINO then SUMINO + 1 when ENDNO = SUMINO then MIN_AKI_URICD end as VALUE, 0 as USEFLG from FU, AKI";
  public final static String ID_SQL_MD03100902_USE = "select T1.URICD as VALUE, 1 as USEFLG, T1.SHNCD, T1.UPDDT, T1.UPDKBN from INAMS.MSTSHN T1 where T1.SHNCD = ? order by T1.UPDDT desc LIMIT 1";

  /** 添付資料（MD03112501）のメーカーコードの取得方法 */
  public final static String ID_SQL_MD03112501 = "with INP as (select CD, KBN, BMNCD from (values ROW(cast(? as CHAR (14)), cast(? as CHAR (2)), cast(? as CHAR (2)))) as X(CD, KBN, BMNCD))"
      + "select case when CD='' then right('0'||BMNCD,2)||'00001'" + " when KBN='1' and left(CD,2)='45' and SUBSTR(CD,3,1)<= 5 then left(CD,7)"
      + " when KBN='1' and left(CD,2)='45' and SUBSTR(CD,3,1) > 5 then left(CD,9)" + " when KBN='1' and left(CD,2)='49' then left(CD,7)" + " when KBN='2' then left(CD,6)"
      + " else right('0'||BMNCD,2)||'00002' end as VALUE" + " from INP";

  /** SQL:添付資料（MD03111301）の総売価の取得 */
  public final static String ID_SQL_MD03111301_IN =
      "(values ROW(cast(? as CHAR), cast(? as CHAR), cast(? as CHAR), cast(? as CHAR), cast(? as CHAR), cast(? as CHAR), cast(? as CHAR))) as X(BMNCD, ZEIKBN, ZEIRTKBN, ZEIRTKBN_OLD, ZEIRTHENKODT, TENBAIKADT, BAIKAAM))";
  public final static String ID_SQL_MD03111301_IN_RG = "with INP as (select BMNCD, ZEIKBN, ZEIRTKBN, ZEIRTKBN_OLD, ZEIRTHENKODT, TENBAIKADT, BAIKAAM as RG_BAIKAAM from " + ID_SQL_MD03111301_IN;
  public final static String ID_SQL_MD03111301_IN_HS = "with INP as (select BMNCD, ZEIKBN, ZEIRTKBN, ZEIRTKBN_OLD, ZEIRTHENKODT, TENBAIKADT, BAIKAAM as HS_BAIKAAM from " + ID_SQL_MD03111301_IN;
  public final static String ID_SQL_MD03111301_COL_RG =
      " case when T1.ZEIKBN = 0 and COALESCE(T1.ZEIRTHENKODT,0) <= COALESCE(T4.SHORIDT, 0) then COALESCE(truncate(T1.RG_BAIKAAM,0),0) + COALESCE(truncate((T1.RG_BAIKAAM*M2.ZEIRT)/100, 0), 0)"
          + " when T1.ZEIKBN = 0 and COALESCE(T1.ZEIRTHENKODT,0) >  COALESCE(T4.SHORIDT, 0) then COALESCE(truncate(T1.RG_BAIKAAM,0),0) + COALESCE(truncate((T1.RG_BAIKAAM*M3.ZEIRT)/100, 0), 0)"
          + " when T1.ZEIKBN = 3 and M1.ZEIKBN = 0 and COALESCE(M1.ZEIRTHENKODT,0) <= COALESCE(T4.SHORIDT, 0) then COALESCE(truncate(T1.RG_BAIKAAM,0),0) + COALESCE(truncate((T1.RG_BAIKAAM*M4.ZEIRT)/100, 0), 0)"
          + " when T1.ZEIKBN = 3 and M1.ZEIKBN = 0 and COALESCE(M1.ZEIRTHENKODT,0) >  COALESCE(T4.SHORIDT, 0) then COALESCE(truncate(T1.RG_BAIKAAM,0),0) + COALESCE(truncate((T1.RG_BAIKAAM*M5.ZEIRT)/100, 0), 0)"
          + " else T1.RG_BAIKAAM end";
  public final static String ID_SQL_MD03111301_COL_HS =
      " case when T1.ZEIKBN = 0 and COALESCE(T1.ZEIRTHENKODT,0) <= COALESCE(T4.SHORIDT, 0) then COALESCE(truncate(T1.HS_BAIKAAM,0),0) + COALESCE(truncate((T1.HS_BAIKAAM*M2.ZEIRT)/100, 0), 0)"
          + " when T1.ZEIKBN = 0 and COALESCE(T1.ZEIRTHENKODT,0) >  COALESCE(T4.SHORIDT, 0) then COALESCE(truncate(T1.HS_BAIKAAM,0),0) + COALESCE(truncate((T1.HS_BAIKAAM*M3.ZEIRT)/100, 0), 0)"
          + " when T1.ZEIKBN = 3 and M1.ZEIKBN = 0 and COALESCE(M1.ZEIRTHENKODT,0) <= COALESCE(T4.SHORIDT, 0) then COALESCE(truncate(T1.HS_BAIKAAM,0),0) + COALESCE(truncate((T1.HS_BAIKAAM*M4.ZEIRT)/100, 0), 0)"
          + " when T1.ZEIKBN = 3 and M1.ZEIKBN = 0 and COALESCE(M1.ZEIRTHENKODT,0) >  COALESCE(T4.SHORIDT, 0) then COALESCE(truncate(T1.HS_BAIKAAM,0),0) + COALESCE(truncate((T1.HS_BAIKAAM*M5.ZEIRT)/100, 0), 0)"
          + " else T1.HS_BAIKAAM end";
  public final static String ID_SQL_MD03111301_COL_RG_Y = StringUtils.replace(ID_SQL_MD03111301_COL_RG, "T4.SHORIDT", "T1.TENBAIKADT");
  public final static String ID_SQL_MD03111301_COL_HS_Y = StringUtils.replace(ID_SQL_MD03111301_COL_HS, "T4.SHORIDT", "T1.TENBAIKADT");
  public final static String ID_SQL_MD03111301_JOIN_Y =
      " left outer join INAMS.MSTBMN M1 on T1.BMNCD = M1.BMNCD and COALESCE(M1.UPDKBN, 0) <> 1" + " left outer join INAMS.MSTZEIRT M2 on M2.ZEIRTKBN = T1.ZEIRTKBN and COALESCE(M2.UPDKBN,0) <> 1"
          + " left outer join INAMS.MSTZEIRT M3 on M3.ZEIRTKBN = T1.ZEIRTKBN_OLD and COALESCE(M3.UPDKBN,0) <> 1"
          + " left outer join INAMS.MSTZEIRT M4 on M4.ZEIRTKBN = M1.ZEIRTKBN and COALESCE(M4.UPDKBN,0) <> 1"
          + " left outer join INAMS.MSTZEIRT M5 on M5.ZEIRTKBN = M1.ZEIRTKBN_OLD and COALESCE(M5.UPDKBN,0) <> 1";
  public final static String ID_SQL_MD03111301_JOIN =
      " left outer join (select ID, SHORIDT as SHORIDT from INAAD.SYSSHORIDT where COALESCE(UPDKBN, 0) <>1 LIMIT 1) T4 on 1=1" + ID_SQL_MD03111301_JOIN_Y;
  public final static String ID_SQL_MD03111301_RG = ID_SQL_MD03111301_IN_RG + " select " + ID_SQL_MD03111301_COL_RG + " as VALUE from INP as T1" + ID_SQL_MD03111301_JOIN;
  public final static String ID_SQL_MD03111301_HS = ID_SQL_MD03111301_IN_HS + " select " + ID_SQL_MD03111301_COL_HS + " as VALUE from INP as T1" + ID_SQL_MD03111301_JOIN;
  public final static String ID_SQL_MD03111301_RG_Y = ID_SQL_MD03111301_IN_RG + " select " + ID_SQL_MD03111301_COL_RG_Y + " as VALUE from INP as T1" + ID_SQL_MD03111301_JOIN_Y;
  public final static String ID_SQL_MD03111301_HS_Y = ID_SQL_MD03111301_IN_HS + " select " + ID_SQL_MD03111301_COL_HS_Y + " as VALUE from INP as T1" + ID_SQL_MD03111301_JOIN_Y;

  /** SQL:添付資料（特売基本情報）の本体売価/総売価の取得 */
  public final static String ID_SQL_TOKBAIKA_IN = "with INP as (select SHNCD, BAIKA, DT from (values ROW(cast(? as char), cast(? as char), cast(? as char))) as X(SHNCD, BAIKA, DT))";
  public final static String ID_SQL_TOKBAIKA_COL_SOU =
      " case when M0.ZEIKBN = 0 and COALESCE(M0.ZEIRTHENKODT,0) <= COALESCE(@DT, 0) then COALESCE(truncate(@BAIKA,0),0) + COALESCE(truncate(cast(@BAIKA*M2.ZEIRT as decimal(31,0))/100, 0), 0)"
          + " when M0.ZEIKBN = 0 and COALESCE(M0.ZEIRTHENKODT,0) >  COALESCE(@DT, 0) then COALESCE(truncate(@BAIKA,0),0) + COALESCE(truncate(cast(@BAIKA*M3.ZEIRT as decimal(31,0))/100, 0), 0)"
          + " when M0.ZEIKBN = 3 and M1.ZEIKBN = 0 and COALESCE(M1.ZEIRTHENKODT,0) <= COALESCE(@DT, 0) then COALESCE(truncate(@BAIKA,0),0) + COALESCE(truncate(cast(@BAIKA*M4.ZEIRT as decimal(31,0))/100, 0), 0)"
          + " when M0.ZEIKBN = 3 and M1.ZEIKBN = 0 and COALESCE(M1.ZEIRTHENKODT,0) >  COALESCE(@DT, 0) then COALESCE(truncate(@BAIKA,0),0) + COALESCE(truncate(cast(@BAIKA*M5.ZEIRT as decimal(31,0))/100, 0), 0)"
          + " else @BAIKA end";

  // 割引本体売価 小数点切上げ
  public final static String ID_SQL_TOKBAIKA_COL_HON =
      " case when M0.ZEIKBN = 0 and COALESCE(M0.ZEIRTHENKODT, 0) <= COALESCE(@DT, 0) then CEILING(cast(@BAIKA as double) / NULLIF(1 + cast(M2.ZEIRT as decimal(15,0)) / 100, 0))"
          + " when M0.ZEIKBN = 0 and COALESCE(M0.ZEIRTHENKODT, 0) >  COALESCE(@DT, 0) then CEILING(cast(@BAIKA as double) / NULLIF(1 + cast(M3.ZEIRT as decimal(15,0)) / 100, 0))"
          + " when M0.ZEIKBN = 3 and M1.ZEIKBN = 0 and COALESCE(M1.ZEIRTHENKODT, 0) <= COALESCE(@DT, 0) then CEILING(cast(@BAIKA as double) / NULLIF(1 + cast(M4.ZEIRT as decimal(15,0)) / 100, 0))"
          + " when M0.ZEIKBN = 3 and M1.ZEIKBN = 0 and COALESCE(M1.ZEIRTHENKODT, 0) >  COALESCE(@DT, 0) then CEILING(cast(@BAIKA as double) / NULLIF(1 + cast(M5.ZEIRT as decimal(15,0)) / 100, 0))"
          + " else @BAIKA end";

  public final static String ID_SQL_TOKBAIKA_COL_HON2 =
      " case when M0.ZEIKBN = 0 and COALESCE(SUBSTR(M0.ZEIRTHENKODT, 3, 6), 0) <= COALESCE(@DT, 0) then CEILING(cast(@BAIKA as double) / NULLIF(1 + cast(M2.ZEIRT as decimal(15,0)) / 100, 0))"
          + " when M0.ZEIKBN = 0 and COALESCE(SUBSTR(M0.ZEIRTHENKODT, 3, 6), 0) >  COALESCE(@DT, 0) then CEILING(cast(@BAIKA as double) / NULLIF(1 + cast(M3.ZEIRT as decimal(15,0)) / 100, 0))"
          + " when M0.ZEIKBN = 3 and M1.ZEIKBN = 0 and COALESCE(SUBSTR(M1.ZEIRTHENKODT, 3, 6), 0) <= COALESCE(@DT, 0) then CEILING(cast(@BAIKA as double) / NULLIF(1 + cast(M4.ZEIRT as decimal(15,0)) / 100, 0))"
          + " when M0.ZEIKBN = 3 and M1.ZEIKBN = 0 and COALESCE(SUBSTR(M1.ZEIRTHENKODT, 3, 6), 0) >  COALESCE(@DT, 0) then CEILING(cast(@BAIKA as double) / NULLIF(1 + cast(M5.ZEIRT as decimal(15,0)) / 100, 0))"
          + " else @BAIKA end";

  public final static String ID_SQL_TOKBAIKA_JOIN = " left outer join INAMS.MSTSHN M0 on M0.SHNCD = @SHNCD   and COALESCE(M0.UPDKBN, 0) <> 1 "
      + " left outer join INAMS.MSTBMN M1 on M1.BMNCD = M0.BMNCD and COALESCE(M1.UPDKBN, 0) <> 1" + " left outer join INAMS.MSTZEIRT M2 on M2.ZEIRTKBN = M0.ZEIRTKBN and COALESCE(M2.UPDKBN, 0) <> 1"
      + " left outer join INAMS.MSTZEIRT M3 on M3.ZEIRTKBN = M0.ZEIRTKBN_OLD and COALESCE(M3.UPDKBN, 0) <> 1"
      + " left outer join INAMS.MSTZEIRT M4 on M4.ZEIRTKBN = M1.ZEIRTKBN and COALESCE(M4.UPDKBN, 0) <> 1"
      + " left outer join INAMS.MSTZEIRT M5 on M5.ZEIRTKBN = M1.ZEIRTKBN_OLD and COALESCE(M5.UPDKBN, 0) <> 1";
  public final static String ID_SQL_TOKBAIKA_SOU = ID_SQL_TOKBAIKA_IN + " select " + ID_SQL_TOKBAIKA_COL_SOU + " as VALUE from INP as T1" + ID_SQL_TOKBAIKA_JOIN;
  public final static String ID_SQL_TOKBAIKA_HON = ID_SQL_TOKBAIKA_IN + " select " + ID_SQL_TOKBAIKA_COL_HON + " as VALUE from INP as T1" + ID_SQL_TOKBAIKA_JOIN;
  public final static String ID_SQL_TOKBAIKA_HON2 =
      "SELECT T2.ZEIRT AS F1 FROM(SELECT CASE WHEN T1.ZEIKBN <> '3' THEN CASE WHEN T1.ZEIKBN <> '0' THEN NULL WHEN T1.ZEIKBN = '0' AND SUBSTR(T1.ZEIRTHENKODT, 3, 6) <= ? THEN T1.ZEIRTKBN WHEN T1.ZEIKBN = '0' AND SUBSTR(T1.ZEIRTHENKODT, 3, 6) > ? THEN T1.ZEIRTKBN_OLD END ELSE CASE WHEN T2.ZEIKBN <> '0' THEN NULL WHEN T2.ZEIKBN='0' AND SUBSTR(T2.ZEIRTHENKODT, 3, 6) <= ? THEN T2.ZEIRTKBN WHEN T2.ZEIKBN = '0' AND SUBSTR(T2.ZEIRTHENKODT, 3, 6) > ? THEN T2.ZEIRTKBN_OLD END END ZEIRTKBN FROM INAMS.MSTSHN T1 LEFT JOIN INAMS.MSTBMN T2 ON SUBSTR(T1.SHNCD, 1, 2) = T2.BMNCD WHERE T1.SHNCD=?) T1 LEFT JOIN INAMS.MSTZEIRT T2 ON T1.ZEIRTKBN=T2.ZEIRTKBN";

  // 特売共通処理：全品割引商品登録時のチェック用SQL
  public final static String ID_SQL_TOKRS_KKK_CNT = "with INP as (" + "  select HBSTDT, int (left (LPAD(SHNCD, 8, '0'), 2)) as BMNCD, SHNCD, WRITUKBN"
      + "  from (values ROW(cast(? as varchar), cast(? as varchar), cast(? as varchar))) as X(HBSTDT, SHNCD, WRITUKBN)" + " )" + " select count(T.HBSTDT) as VALUE from INATK.TOKRS_KKK T"
      + " inner join (" + "  select MAX(T.HBSTDT) as HBSTDT, T.BMNCD, X.WRITUKBN, X.SHNCD" + "  from INATK.TOKRS_KKK T "
      + "  inner join INP X on T.HBSTDT <= X.HBSTDT and T.BMNCD = X.BMNCD and T.DUMMYCD = SHNCD and T.SEICUTKBN = 0" + "  group by T.BMNCD, X.WRITUKBN, X.SHNCD"
      + " ) X on T.HBSTDT = X.HBSTDT and T.BMNCD = X.BMNCD and T.WRITUKBN = X.WRITUKBN and T.DUMMYCD = SHNCD and T.SEICUTKBN = 0";

  /**
   * 商品カテゴリ関連のSQL
   */
  /** 商品条件（T_CTG_HEAD）検索 */
  public static final String ID_SQL_KEY_HEAD =
      "select VALUE, TEXT from (values ROW('', '')) as X(value, TEXT) union all " + "select TO_CHAR(CD_CTG) as VALUE, NM_CTG as TEXT from KEYSYS.T_CTG_HEAD where CD_USER = ? order by TEXT";

  /** 商品条件（T_CTG_HEAD）検索 */
  public static final String ID_SQL_KEY_HEAD_NO =
      "SELECT RTRIM(T1.CD_ITEM) AS F1, T2.SYOM AS F2, ROW_NUMBER() OVER () AS F3 FROM KEYSYS.T_CTG_ITEM AS T1 INNER JOIN INATR.SYOUHIN_MST AS T2 ON T1.CD_ITEM = T2.SYOCD WHERE T1.CD_CTG = ? ORDER BY T1.NO_LINE ASC";

  /** 商品条件（T_CTG_HEAD）登録 */
  public static final String ID_SQL_KEY_SET_HEAD =
      "merge into KEYSYS.T_CTG_HEAD T1 using (select cast (? as integer) as CD_USER, cast (? as varchar (100)) as NM_CTG from SYSIBM.SYSDUMMY1) T2 on (T1.CD_USER = T2.CD_USER and T1.NM_CTG = T2.NM_CTG) when matched then update set T1.DT_UPDATE = current_timestamp when not matched then insert (T1.CD_USER, T1.NM_CTG, T1.DT_UPDATE) values (T2.CD_USER, T2.NM_CTG, current_timestamp) else ignore";

  /** 商品条件（T_CTG_ITEM）削除 */
  public static final String ID_SQL_KEY_DELETE_ITEM = "delete from KEYSYS.T_CTG_ITEM where CD_CTG in (select CD_CTG from KEYSYS.T_CTG_HEAD where CD_USER = ? and NM_CTG = ?)";

  /** 商品条件（T_CTG_ITEM）登録 */
  public static final String ID_SQL_KEY_SET_ITEM =
      "merge into KEYSYS.T_CTG_ITEM T1 using (select CD_CTG, cast (? as varchar (20)) as CD_ITEM, cast (? as integer) as NO_LINE from KEYSYS.T_CTG_HEAD where CD_USER = ? and NM_CTG = ? ) T2 on (T1.CD_CTG = T2.CD_CTG and T1.CD_ITEM = T2.CD_ITEM and T1.NO_LINE = T2.NO_LINE) when not matched then insert (T1.CD_CTG, T1.CD_ITEM, T1.NO_LINE) values (T2.CD_CTG, T2.CD_ITEM, T2.NO_LINE) else ignore";

  /** 商品条件（T_CTG_HEAD）削除専用 */
  public static final String ID_SQL_KEY_DELETE_HEAD_SP = "delete from KEYSYS.T_CTG_HEAD where CD_CTG = ?";

  /** 商品条件（T_CTG_ITEM）削除専用 */
  public static final String ID_SQL_KEY_DELETE_ITEM_SP = "delete from KEYSYS.T_CTG_ITEM where CD_CTG = ?";

  /** SQL：商品検索（JANコード） */
  public final static String ID_SQL_SYOHIN_JAN =
      "SELECT VALUE, TEXT FROM (SELECT SYOCD AS VALUE, SYOM AS TEXT FROM INAMS.SYOUHIN_MST WHERE (SYOCD LIKE ? OR SYOM LIKE ? ) FETCH FIRST 30 ROWS ONLY) ORDER BY TEXT";

  /**
   * 店舗グループ関連のSQL
   */
  /** 店舗グループ（T_TNP_HEAD）検索 */
  public static final String ID_SQL_KEY_HEAD_TG =
      "select VALUE, TEXT from (values ROW('', '')) as X(value, TEXT) union all " + "select TO_CHAR(CD_CTG) as VALUE, NM_CTG as TEXT from KEYSYS.T_TNP_HEAD where CD_USER = ? order by TEXT";

  /** 店舗グループ（T_TNP_HEAD）検索 */
  public static final String ID_SQL_KEY_HEAD_NO_TG =
      "SELECT RTRIM(T1.CD_ITEM) AS F1, T2.TENMEI AS F2, ROW_NUMBER() OVER () AS F3 FROM KEYSYS.T_TNP_ITEM AS T1 INNER JOIN INAMS.TENPO_MST AS T2 ON T1.CD_ITEM = T2.MISECD WHERE T1.CD_CTG = ? ORDER BY T1.NO_LINE ASC";

  /** 店舗グループ（T_TNP_HEAD）登録 */
  public static final String ID_SQL_KEY_SET_HEAD_TG =
      "merge into KEYSYS.T_TNP_HEAD T1 using (select cast (? as integer) as CD_USER, cast (? as varchar (100)) as NM_CTG from SYSIBM.SYSDUMMY1) T2 on (T1.CD_USER = T2.CD_USER and T1.NM_CTG = T2.NM_CTG) when matched then update set T1.DT_UPDATE = current_timestamp when not matched then insert (T1.CD_USER, T1.NM_CTG, T1.DT_UPDATE) values (T2.CD_USER, T2.NM_CTG, current_timestamp) else ignore";

  /** 店舗グループ（T_TNP_ITEM）削除 */
  public static final String ID_SQL_KEY_DELETE_ITEM_TG = "delete from KEYSYS.T_TNP_ITEM where CD_CTG in (select CD_CTG from KEYSYS.T_TNP_HEAD where CD_USER = ? and NM_CTG = ?)";

  /** 店舗グループ（T_TNP_ITEM）登録 */
  public static final String ID_SQL_KEY_SET_ITEM_TG =
      "merge into KEYSYS.T_TNP_ITEM T1 using (select CD_CTG, cast (? as varchar (20)) as CD_ITEM, cast (? as integer) as NO_LINE from KEYSYS.T_TNP_HEAD where CD_USER = ? and NM_CTG = ? ) T2 on (T1.CD_CTG = T2.CD_CTG and T1.CD_ITEM = T2.CD_ITEM and T1.NO_LINE = T2.NO_LINE) when not matched then insert (T1.CD_CTG, T1.CD_ITEM, T1.NO_LINE) values (T2.CD_CTG, T2.CD_ITEM, T2.NO_LINE) else ignore";

  /** 店舗グループ（T_TNP_HEAD）削除専用 */
  public static final String ID_SQL_KEY_DELETE_HEAD_SP_TG = "delete from KEYSYS.T_TNP_HEAD where CD_CTG = ?";

  /** 店舗グループ（T_TNP_ITEM）削除専用 */
  public static final String ID_SQL_KEY_DELETE_ITEM_SP_TG = "delete from KEYSYS.T_TNP_ITEM where CD_CTG = ?";

  /** SQL：店舗検索 */
  public final static String ID_SQL_SYOHIN_TENPO =
      "SELECT VALUE, TEXT FROM (SELECT MISECD AS VALUE, TENMEI AS TEXT FROM INAMS.TENPO_MST WHERE (MISECD LIKE ? OR TENMEI LIKE ? ) FETCH FIRST 30 ROWS ONLY) ORDER BY TEXT";
  /** SQL：店舗基本マスタ検索 */
  public final static String ID_SQL_TENPOKHN = "select distinct CASE WHEN TENKN IS NULL THEN '　' ELSE TENKN END as F1, TENAN as F2 from INAMS.MSTTEN where TENCD = ?";

  /**
   * 定義保存関連のSQL
   */
  /** 検索条件の削除 */
  public static final String ID_SQL_DELETE_SHIORI = "DELETE FROM KEYSYS.T_SHIORI WHERE CD_SHIORI=?";
  /** 検索条件の登録 */
  public static final String ID_SQL_INSERT_SHIORI = "INSERT INTO KEYSYS.T_SHIORI(CD_USER,CD_REPORT,NM_SHIORI,SNAPSHOT,FG_PUBLIC) VALUES(?,?,?,?,?)";
  /** SQL：定義検索 */
  public final static String ID_SQL_SELECT_SHIORI = "SELECT VALUE, TEXT, PUBLIC, FG_PUBLIC, CD_USER, SNAPSHOT FROM ( "
      + "(SELECT CD_SHIORI AS VALUE, NM_SHIORI AS TEXT, '' AS PUBLIC, FG_PUBLIC, CD_USER, SNAPSHOT, 0 AS SORT FROM KEYSYS.T_SHIORI WHERE FG_PUBLIC = 0 AND CD_USER = ? AND CD_REPORT = ?) "
      + "UNION ALL (SELECT VALUE, TEXT, PUBLIC, FG_PUBLIC, CD_USER, SNAPSHOT, SORT FROM (SELECT CD_SHIORI AS VALUE, NM_SHIORI AS TEXT, '部署' AS PUBLIC, FG_PUBLIC, T1.CD_USER, SNAPSHOT, 8 AS SORT, COALESCE(T4.HTOUKATU_CD_S || T4.HANBAIB_S, T3.JIMU_SOSIKI_CD, '') AS FGCD FROM KEYSYS.T_SHIORI T1 INNER JOIN KEYSYS.SYS_USERS T2 ON T1.CD_USER = T2.CD_USER LEFT OUTER JOIN INAMS.JINJI_KIHON_MST T3 ON T2.USER_ID = T3.SYAIN_NO LEFT OUTER JOIN INAMS.TENPO_MST T4 ON T3.JIMU_SOSIKI_CD = T4.MISECD WHERE FG_PUBLIC = 2 AND CD_REPORT = ?) X WHERE X.FGCD IN (SELECT COALESCE(T4.HTOUKATU_CD_S || T4.HANBAIB_S, T3.JIMU_SOSIKI_CD, '') AS FGCD FROM KEYSYS.SYS_USERS T2 LEFT OUTER JOIN INAMS.JINJI_KIHON_MST T3 ON T2.USER_ID = T3.SYAIN_NO LEFT OUTER JOIN INAMS.TENPO_MST T4 ON T3.JIMU_SOSIKI_CD = T4.MISECD WHERE T2.CD_USER = ?)) "
      + "UNION ALL (SELECT CD_SHIORI AS VALUE, NM_SHIORI AS TEXT, '全社' AS PUBLIC, FG_PUBLIC, CD_USER, SNAPSHOT, 9 AS SORT FROM KEYSYS.T_SHIORI WHERE FG_PUBLIC = 1 AND CD_REPORT = ?) "
      + ") WHERE TEXT LIKE ? ORDER BY SORT, TEXT FETCH FIRST 300 ROWS ONLY ";

  // "SELECT VALUE, TEXT, PUBLIC, CD_USER, SNAPSHOT FROM ((SELECT CD_SHIORI AS
  // VALUE, NM_SHIORI AS TEXT, CASE WHEN FG_PUBLIC = 1 THEN '*' ELSE '' END AS
  // PUBLIC, CD_USER, SNAPSHOT FROM KEYSYS.T_SHIORI WHERE CD_USER = ? AND
  // CD_REPORT = ? FETCH FIRST 30 ROWS ONLY) UNION ALL (SELECT CD_SHIORI AS
  // VALUE, NM_SHIORI AS TEXT, CASE WHEN FG_PUBLIC = 1 THEN '*' ELSE '' END AS
  // PUBLIC, CD_USER, SNAPSHOT FROM KEYSYS.T_SHIORI WHERE FG_PUBLIC = 1 AND
  // CD_USER <> ? AND CD_REPORT = ? FETCH FIRST 270 ROWS ONLY)) ORDER BY
  // PUBLIC, TEXT";

  /**
   * ログ情報の登録
   */
  public static final String ID_SQL_INSERT_SYSLOGS = "insert into KEYSYS.SYS_LOGS(CD_USER,DT_ACTION,CD_ACTION,REMARK,ID_USER,ID_REPORT) values(?,current_timestamp,?,?,?,?)";
  public static final String ID_SQL_INSERT_FTPLOGS = "insert into INAAD.SYSFTPLOG(SEQ,STARTDT,SENDADDR,RECVADDR,FILENM,OPERATOR,ADDDT) values(?,current_timestamp,?,?,?,?,current_timestamp)";
  public static final String ID_SQL_UPDATE_FTPLOGS = "update INAAD.SYSFTPLOG set ENDDT=current_timestamp,STATUS=?,ERRCD=?,COMMENTS=?,ADDDT=current_timestamp where SEQ=?";

  /**
   * ログ情報のアクション
   */
  public static final String ID_ACTION_LOGIN = "login";
  public static final String ID_ACTION_QUERY = "query";
  public static final String ID_ACTION_EXCEL = "excel";

  /**
   * 検索条件の削除
   */
  public static final String ID_SQL_DELETE_SNAPSHOT = "DELETE FROM KEYSYS.SYS_SNAPSHOT WHERE CD_USER=? AND CD_REPORT=?";

  /**
   * 検索条件の登録
   */
  public static final String ID_SQL_INSERT_SNAPSHOT = "INSERT INTO KEYSYS.SYS_SNAPSHOT(CD_USER,CD_REPORT,SNAPSHOT,NM_CREATE,NM_UPDATE) VALUES(?,?,?,?,?)";

  /**
   * 検索条件の検索
   */
  public static final String ID_SQL_SELECT_SNAPSHOT = "SELECT SNAPSHOT FROM KEYSYS.SYS_SNAPSHOT WHERE CD_USER=? and CD_REPORT=?";

  /**
   * SESSION 関連
   */
  /* 「照会」押下時の分析条件(JSON)を格納するセッション名 */
  public final static String ID_SESSION_STORAGE = "STORAGE";
  public final static String ID_SESSION_TABLE = "table";
  public final static String ID_SESSION_WHERE = "where";
  public final static String ID_SESSION_META = "meta";
  public final static String ID_SESSION_HEADER = "header";
  public final static String ID_SESSION_OPTION = "option";
  public final static String ID_SESSION_OPT_TABLE = "opt_table";
  public final static String ID_SESSION_MSG = "message";
  public final static String ID_SESSION_FILE = "file";
  /* 照会以外で利用しているセッション名 */
  public final static String ID_SESSION_PREFIX_TEMP = "_tmp"; // 検索⇒即Excel出力やアップロードなど、一時利用セッションの識別子

  public final static String ID_MSG_SQL_EXCEPTION = "SQLエラーが発生しました。";
  public final static String ID_MSG_APP_EXCEPTION = "アプリケーションエラーが発生しました。";

  public final static String ID_MSG_TIMEOUT_EXCEPTION = "利用時間外の為、処理を実行できません。";

  public final static String ID_MSG_COLUMN_GREATER = "横軸に展開する情報が多すぎます。表示条件を絞り込んでください。";
  public final static String ID_SQLSTATE_APPLICATION_HEPE = "54001";
  public final static String ID_SQLSTATE_COLUMN_OVER = "54004";
  public final static String ID_SQLSTATE_COLUMN_GREATER = "54011";
  public final static String ID_SQLSTATE_BUFFER_GREATER = "54048";

  public final static String ID_MSG_CONNECTION_REST = "再度、検索を実行してください。";
  public final static String ID_SQLSTATE_CONNECTION_RESET = "08001";

  /**
   * Web商談 SQL：取引先
   */
  public final static String ID_SQL_TORIHIKI_HEAD = "select VALUE, TEXT from (values ROW('" + Values.NONE.getVal() + "',' ')) as X(" + VAL + "," + TXT + ") union all ";
  public final static String ID_SQL_TORIHIKI_x245 = "select SSM.SIRCD as VALUE, SSM.SIRCD || ' ' || SSM.SIRKN as TEXT from INAMS.MSTSIR SSM where SSM.SIRCD = ? order by VALUE";
  public final static String ID_SQL_TORIHIKI_NO_YOBI_x245 = ID_SQL_TORIHIKI_HEAD
      + "select SSM.SIRCD as VALUE, SSM.SIRCD || ' ' || SSM.SIRKN as TEXT from INAMS.MSTSIR SSM inner join (select MAX(STARTDT) as STARTDT, SIRCD from INAMS.MSTSIR where STARTDT <= ?  group by SIRCD) TMP on TMP.STARTDT = SSM.STARTDT and TMP.SIRCD = SSM.SIRCD order by VALUE";

  /**
   * Web商談 SQL：提案
   */
  public final static String ID_SQL_TEIAN_NO_YOBI_x246 = "select TIKKNNO as VALUE, TIKKNNM as TEXT from INAWS.PIMTIK";
  public final static String ID_SQL_TEIAN_x246 = "select TIKKNNO as VALUE, TIKKNNM as TEXT from INAWS.PIMTIK WHERE TIKTTCD = ?";

  /**
   * Web商談 仕入グループ（INAWS.PIMTISIRGPSHN）, （INAWS.PIMSISIRGPSHN）
   */
  public final static String ID_SQL_TENGP_SIR_TEIAN = ID_SQL_GRD_CMN + "select T2.TENGPCD, T3.TENGPKN, T2.SIRCD, T2.SIRCD as SSIRCD, T4.SIRKN, T2.HSPTN, T5.HSPTNKN, T2.AREAKBN from T1"
      + " left outer join (select ROW_NUMBER() over (order by TENGPCD) as IDX, * from INAWS.PIMTISIRGPSHN where SHNCD like ? and AREAKBN = ? order by TENGPCD fetch first @M rows only) T2 on T1.IDX = T2.IDX"
      + " left outer join INAMS.MSTSHNTENGP T3 on T2.TENGPCD = T3.TENGPCD and T2.AREAKBN = T3.AREAKBN and T3.GPKBN = " + ValGpkbn.SIR.getVal() + " and T3.BMNCD = ? and COALESCE(T3.UPDKBN,0) <> 1 "
      + " left outer join INAMS.MSTSIR T4 on T2.SIRCD = T4.SIRCD and COALESCE(T4.UPDKBN,0) <> 1 " + " left outer join INAMS.MSTHSPTN T5 on T2.HSPTN = T5.HSPTN and COALESCE(T5.UPDKBN,0) <> 1 ";
  public final static String ID_SQL_TENGP_SIR_SHIKAKARI = ID_SQL_GRD_CMN + "select T2.TENGPCD, T3.TENGPKN, T2.SIRCD, T2.SIRCD as SSIRCD, T4.SIRKN, T2.HSPTN, T5.HSPTNKN, T2.AREAKBN from T1"
      + " left outer join (select ROW_NUMBER() over (order by TENGPCD) as IDX, * from INAWS.PIMSISIRGPSHN where SHNCD like ? and AREAKBN = ? order by TENGPCD fetch first @M rows only) T2 on T1.IDX = T2.IDX"
      + " left outer join INAMS.MSTSHNTENGP T3 on T2.TENGPCD = T3.TENGPCD and T2.AREAKBN = T3.AREAKBN and T3.GPKBN = " + ValGpkbn.SIR.getVal() + " and T3.BMNCD = ? and COALESCE(T3.UPDKBN,0) <> 1 "
      + " left outer join INAMS.MSTSIR T4 on T2.SIRCD = T4.SIRCD and COALESCE(T4.UPDKBN,0) <> 1 " + " left outer join INAMS.MSTHSPTN T5 on T2.HSPTN = T5.HSPTN and COALESCE(T5.UPDKBN,0) <> 1 ";
  /**
   * Web商談 売価グループ（INAWS.PIMTIBAIKACTL）, （INAWS.PIMSIBAIKACTL）
   */
  public final static String ID_SQL_TENGP_BAIKA_TEIAN = ID_SQL_GRD_CMN + "select T2.TENGPCD, T3.TENGPKN, T2.GENKAAM, T2.BAIKAAM, '' as BG_SOUBAIKA, '' as BG_NEIRE, T2.IRISU, T2.AREAKBN from T1"
      + " left outer join (select ROW_NUMBER() over (order by TENGPCD) as IDX, * from INAWS.PIMTIBAIKACTL where SHNCD like ? and AREAKBN = ? order by TENGPCD fetch first @M rows only) T2 on T1.IDX = T2.IDX"
      + " left outer join INAMS.MSTSHNTENGP T3 on T2.TENGPCD = T3.TENGPCD and T2.AREAKBN = T3.AREAKBN and T3.GPKBN = " + ValGpkbn.BAIKA.getVal() + " and T3.BMNCD = ? and COALESCE(T3.UPDKBN,0) <> 1 ";
  public final static String ID_SQL_TENGP_BAIKA_SHIKAKARI = ID_SQL_GRD_CMN + "select T2.TENGPCD, T3.TENGPKN, T2.GENKAAM, T2.BAIKAAM, '' as BG_SOUBAIKA, '' as BG_NEIRE, T2.IRISU, T2.AREAKBN from T1"
      + " left outer join (select ROW_NUMBER() over (order by TENGPCD) as IDX, * from INAWS.PIMSIBAIKACTL where SHNCD like ? and AREAKBN = ? order by TENGPCD fetch first @M rows only) T2 on T1.IDX = T2.IDX"
      + " left outer join INAMS.MSTSHNTENGP T3 on T2.TENGPCD = T3.TENGPCD and T2.AREAKBN = T3.AREAKBN and T3.GPKBN = " + ValGpkbn.BAIKA.getVal() + " and T3.BMNCD = ? and COALESCE(T3.UPDKBN,0) <> 1 ";

  /**
   * Web商談 ソースコード
   */
  /**
   * Web商談 品揃えグループ（INAWS.PIMTISHINAGP）, （INAWS.PIMSISHINAGP）
   */
  public final static String ID_SQL_TENGP_SHINA_TEIAN = ID_SQL_GRD_CMN + "select T2.TENGPCD, T3.TENGPKN, T2.ATSUKKBN||'" + SEPARATOR + "'||T4.NMKN as ATSUKKBN, T2.AREAKBN from T1"
      + " left outer join (select ROW_NUMBER() over (order by TENGPCD) as IDX, * from INAWS.PIMTISHINAGP where SHNCD like ? and AREAKBN = ? order by TENGPCD fetch first @M rows only) T2 on T1.IDX = T2.IDX"
      + " left outer join INAMS.MSTSHNTENGP T3 on T2.TENGPCD = T3.TENGPCD and T2.AREAKBN = T3.AREAKBN and T3.GPKBN = " + ValGpkbn.SHINA.getVal() + " and T3.BMNCD = ? and COALESCE(T3.UPDKBN,0) <> 1 "
      + " left outer join INAMS.MSTMEISHO T4 on T4.MEISHOKBN = " + MeisyoSelect.KBN139.getCd() + " and TO_CHAR(T2.ATSUKKBN) = T4.MEISHOCD";
  public final static String ID_SQL_TENGP_SHINA_SHIKAKARI = ID_SQL_GRD_CMN + "select T2.TENGPCD, T3.TENGPKN, T2.ATSUKKBN||'" + SEPARATOR + "'||T4.NMKN as ATSUKKBN, T2.AREAKBN from T1"
      + " left outer join (select ROW_NUMBER() over (order by TENGPCD) as IDX, * from INAWS.PIMSISHINAGP where SHNCD like ? and AREAKBN = ? order by TENGPCD fetch first @M rows only) T2 on T1.IDX = T2.IDX"
      + " left outer join INAMS.MSTSHNTENGP T3 on T2.TENGPCD = T3.TENGPCD and T2.AREAKBN = T3.AREAKBN and T3.GPKBN = " + ValGpkbn.SHINA.getVal() + " and T3.BMNCD = ? and COALESCE(T3.UPDKBN,0) <> 1 "
      + " left outer join INAMS.MSTMEISHO T4 on T4.MEISHOKBN = " + MeisyoSelect.KBN139.getCd() + " and TO_CHAR(T2.ATSUKKBN) = T4.MEISHOCD";

  /**
   * Web商談 メーカー（INAWS.PIMTIMAKER）, （INAWS.PIMSIMAKER）
   */
  public final static String ID_SQL_MAKER_TEIAN = "select case when (DMAKERCD = MAKERCD or DMAKERCD is null) then '代表' end as F1, MAKERCD as F2, MAKERKN as F3, DMAKERCD as F4 from INAWS.PIMTIMAKER";
  public final static String ID_SQL_MAKER_SHIKAKARI =
      "select case when (DMAKERCD = MAKERCD or DMAKERCD is null) then '代表' end as F1, MAKERCD as F2, MAKERKN as F3, DMAKERCD as F4 from INAWS.PIMSIMAKER";

  /**
   * 時間帯売上実績の範囲
   */
  public final static String ID_MODE_TIME_NEW = "1"; // 最新情報のみ参照
  public final static String ID_MODE_TIME_OLD = "2"; // 過去情報のみ参照
  public final static String ID_MODE_TIME_RNG = "3"; // 最新＋過去情報を参照
  /**
   * 隠蔽情報
   */
  // レポート番号
  public final static String ID_HIDDEN_REPORT_NO = "reportno";
  // レポート名
  public final static String ID_HIDDEN_REPORT_NAME = "reportname";
  // ユーザID
  public final static String ID_HIDDEN_USER_ID = "userid";
}
