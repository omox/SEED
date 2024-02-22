package dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import authentication.bean.User;
import common.DefineReport;
import common.Defines;
import common.ItemList;
import common.JsonArrayData;
import common.MessageUtility;
import common.MessageUtility.FieldType;
import common.MessageUtility.Msg;
import common.MessageUtility.MsgKey;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 */
public class Reportx112Dao extends ItemDao {

  /** SQLリスト保持用変数 */
  ArrayList<String> sqlList = new ArrayList<String>();
  /** SQLのパラメータリスト保持用変数 */
  ArrayList<ArrayList<String>> prmList = new ArrayList<ArrayList<String>>();
  /** SQLログ用のラベルリスト保持用変数 */
  ArrayList<String> lblList = new ArrayList<String>();

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public Reportx112Dao(String JNDIname) {
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

  private String createCommand() {
    // ログインユーザー情報取得
    User userInfo = getUserInfo();
    if (userInfo == null) {
      return "";
    }
    ArrayList<String> paramData = new ArrayList<String>();
    String szTencd = getMap().get("TENCD"); // 店コード
    String sendBtnid = getMap().get("SENDBTNID"); // 呼出しボタン

    // パラメータ確認
    // 必須チェック
    if (szTencd == null || sendBtnid == null) {
      System.out.println(super.getConditionLog());
      return "";
    }

    StringBuffer sbSQL = new StringBuffer();

    // 変更/参照
    if (!DefineReport.Button.NEW.getObj().equals(sendBtnid)) {
      for (int i = 0; i < 41; i++) {
        paramData.add(szTencd);
      }
      sbSQL.append("select");
      sbSQL.append("  TEN.TENCD"); // F1 ：店コード
      sbSQL.append(", TEN.SEIKAAREAKBN"); // F2 ：青果センターエリア
      sbSQL.append(", TEN.SENGYOKBN"); // F3 ：鮮魚区分
      sbSQL.append(", TEN.SEINIKUKBN"); // F4 ：精肉区分
      sbSQL.append(", case when TEN.TENOPENDT <> 0 then right(TEN.TENOPENDT, 6) end"); // F5 ：開設日
      sbSQL.append(", case when TEN.TENCLOSEDT <> 0 then right(TEN.TENCLOSEDT, 6) end"); // F6 ：閉鎖日
      sbSQL.append(", case when TEN.KAISODT1 <> 0 then right(TEN.KAISODT1, 6) end"); // F7 ：改装日（1）
      sbSQL.append(", case when TEN.KAISODT2 <> 0 then right(TEN.KAISODT2, 6) end"); // F8 ：改装日（2）
      sbSQL.append(", right('00' || TEN.HANBAIBUCD, 2)"); // F9 ：販売部
      sbSQL.append(", right('00' || TEN.CHIKUCD, 2)"); // F10 ：地区
      sbSQL.append(", TEN.URIAERACD"); // F11 ：販売エリア
      sbSQL.append(", TEN.CHIIKICD"); // F12 ：地域
      sbSQL.append(", TEN.YOSANKBN_T"); // F13 ：予算区分_店舗
      sbSQL.append(", TEN.TENKN"); // F14 ：店舗名称（漢字）
      sbSQL.append(", TEN.TENAN"); // F15 ：店舗名称（カナ）
      sbSQL.append(", right('000' || TEN.YUBINNO_U, 3)"); // F16 ：郵便番号_上桁
      sbSQL.append(", right('0000' || TEN.YUBINNO_S, 4)"); // F17 ：郵便番号_下桁
      sbSQL.append(", TEN.ADDRKN_T"); // F18 ：住所_都道府県（漢字）
      sbSQL.append(", TEN.ADDRKN_S"); // F19 ：住所_市区町村（漢字）
      sbSQL.append(", TEN.ADDRKN_M"); // F20 ：住所_町字（漢字）
      sbSQL.append(", TEN.ADDRKN_B"); // F21 ：住所_番地（漢字）
      sbSQL.append(", TEN.ADDRAN_T"); // F22 ：住所_都道府県（カナ）
      sbSQL.append(", TEN.ADDRAN_S"); // F23 ：住所_市区町村（カナ）
      sbSQL.append(", TEN.ADDRAN_M"); // F24 ：住所_町字（カナ）
      sbSQL.append(", TEN.ADDRAN_B"); // F25 ：住所_番地（カナ）
      sbSQL.append(", TEN.MOYORIEKIKN"); // F26 ：最寄り駅
      sbSQL.append(", TEN.BUSSTOPKN"); // F27 ：バス停
      sbSQL.append(", TEN.OWNT_NMKN"); // F28 ：オーナー（店）_名前
      sbSQL.append(", TEN.OWNT_ADDRKN_T"); // F29 ：オーナー（店）_住所_都道府県
      sbSQL.append(", TEN.OWNT_ADDRKN_S"); // F30 ：オーナー（店）_住所_市区町村
      sbSQL.append(", TEN.OWNT_ADDRKN_M"); // F31 ：オーナー（店）_住所_町字
      sbSQL.append(", TEN.OWNT_ADDRKN_B"); // F32 ：オーナー（店）_住所_番地
      sbSQL.append(", TEN.OWNP_NMKN"); // F33 ：オーナー（駐車場）_名前
      sbSQL.append(", TEN.OWNP_ADDRKN_T"); // F34 ：オーナー（駐車場）_住所_都道府県
      sbSQL.append(", TEN.OWNP_ADDRKN_S"); // F35 ：オーナー（駐車場）_住所_市区町村
      sbSQL.append(", TEN.OWNP_ADDRKN_M"); // F36 ：オーナー（駐車場）_住所_町字
      sbSQL.append(", TEN.OWNP_ADDRKN_B"); // F37 ：オーナー（駐車場）_住所_番地
      sbSQL.append(", TEN.OWNO_NMKN"); // F38 ：オーナー（その他）_名前
      sbSQL.append(", TEN.OWNO_ADDRKN_T"); // F39 ：オーナー（その他）_住所_都道府県
      sbSQL.append(", TEN.OWNO_ADDRKN_S"); // F40 ：オーナー（その他）_住所_市区町村
      sbSQL.append(", TEN.OWNO_ADDRKN_M"); // F41 ：オーナー（その他）_住所_町村
      sbSQL.append(", TEN.OWNO_ADDRKN_B"); // F42 ：オーナー（その他）_住所_番地
      sbSQL.append(", TEN.TEL1"); // F43 ：電話番号1
      sbSQL.append(", TEN.TEL2"); // F44 ：電話番号2
      sbSQL.append(", TEN.TEL3"); // F45 ：電話番号3
      sbSQL.append(", TEN.TEL4"); // F46 ：電話番号4
      sbSQL.append(", TEN.TEL5"); // F47 ：電話番号5
      sbSQL.append(", TEN.FAX1"); // F48 ：FAX番号1
      sbSQL.append(", TEN.FAX2"); // F49 ：FAX番号2
      sbSQL.append(", case when TEN.EGYOTM1_STMD <> 0 then right('0000' || TEN.EGYOTM1_STMD, 4) end"); // F50 ：営業時間1_開始月日
      sbSQL.append(", case when TEN.EGYOTM1_EDMD <> 0 then right('0000' || TEN.EGYOTM1_EDMD, 4) end"); // F51 ：営業時間1_終了月日
      sbSQL.append(", right('0000' || TEN.EGYOTM1_STHM, 4)"); // F52 ：営業時間1_開始時間
      sbSQL.append(", right('0000' || TEN.EGYOTM1_EDHM, 4)"); // F53 ：営業時間1_終了時間
      sbSQL.append(", case when TEN.EGYOTM2_STMD <> 0 then right('0000' || TEN.EGYOTM2_STMD, 4) end"); // F54 ：営業時間2_開始月日
      sbSQL.append(", case when TEN.EGYOTM2_EDMD <> 0 then right('0000' || TEN.EGYOTM2_EDMD, 4) end"); // F55 ：営業時間2_終了月日
      sbSQL.append(", right('0000' || TEN.EGYOTM2_STHM, 4)"); // F56 ：営業時間2_開始時間
      sbSQL.append(", right('0000' || TEN.EGYOTM2_EDHM, 4)"); // F57 ：営業時間2_終了時間
      sbSQL.append(", TEN.AREA_BA"); // F58 ：敷地面積
      sbSQL.append(", TEN.AERA_B1YUKA"); // F59 ：敷地面積_B1_床面積
      sbSQL.append(", TEN.AREA_B1URIBA"); // F60 ：敷地面積_B1_売場面積
      sbSQL.append(", TEN.AREA_1FYUKA"); // F61 ：敷地面積_1F_床面積
      sbSQL.append(", TEN.AREA_FURIBA"); // F62 ：敷地面積_1F_売場面積
      sbSQL.append(", TEN.AREA_2FYUKA"); // F63 ：敷地面積_2F_床面積
      sbSQL.append(", TEN.AREA_2FURIBA"); // F64 ：敷地面積_2F_売場面積
      sbSQL.append(", TEN.AREA_3FYUKA"); // F65 ：敷地面積_3F_床面積
      sbSQL.append(", TEN.AREA_3FURIBA"); // F66 ：敷地面積_3F_売場面積
      sbSQL.append(", TEN.AREA_4FYUKA"); // F67 ：敷地面積_4F_床面積
      sbSQL.append(", TEN.AREA_4FURIBA"); // F68 ：敷地面積_4F_売場面積
      sbSQL.append(", TEN.PARK_NM_BA"); // F69 ：駐車台数_普通車_敷地内
      sbSQL.append(", TEN.PARK_NM_YANE"); // F70 ：駐車台数_普通車_屋上
      sbSQL.append(", TEN.PARK_NM_TOBI"); // F71 ：駐車台数_普通車_飛地
      sbSQL.append(", TEN.PARK_LT_BA"); // F72 ：駐車台数_軽_敷地内
      sbSQL.append(", TEN.PARK_LT_YANE"); // F73 ：駐車台数_軽_屋上
      sbSQL.append(", TEN.PARK_LT_TOBI"); // F74 ：駐車台数_軽_飛地
      sbSQL.append(", TEN.PARK_HC_BA"); // F75 ：駐車台数_障害者_敷地内
      sbSQL.append(", TEN.PARK_HC_YANE"); // F76 ：駐車台数_障害者_屋上
      sbSQL.append(", TEN.PARK_HC_TOBI"); // F77 ：駐車台数_障害者_飛地
      sbSQL.append(", TEN.ELEVTRFLG"); // F78 ：エレベータ
      sbSQL.append(", TEN.ESCALTRFLG"); // F79 ：エスカレータ
      sbSQL.append(", right('00' || TEN.SEIKACD, 2)"); // F80 ：青果市場コード
      sbSQL.append(", TEN.STAFFSU"); // F81 ：実働人員
      sbSQL.append(", TEN.MISEUNYOKBN"); // F82 ：店運用区分
      sbSQL.append(", TEN.TENAGE"); // F83 ：店舗年齢
      sbSQL.append(", TEN.URIDAY"); // F84 ：日商
      sbSQL.append(", TEN.URIZEN"); // F85 ：売上前比
      sbSQL.append(", TEN.ARARI"); // F86 ：荒利率
      sbSQL.append(", TEN.JMREI"); // F87 ：什器メーカー_冷設
      sbSQL.append(", TEN.JMSOU"); // F88 ：什器メーカー_惣菜
      sbSQL.append(", TEN.JMGON"); // F89 ：什器メーカー_ゴンドラ
      sbSQL.append(", TEN.SVIKFLG"); // F90 ：ingfanカード
      sbSQL.append(", TEN.SVPWFLG"); // F91 ：ピュアウォーター
      sbSQL.append(", TEN.SVATMFLG"); // F92 ：ATM
      sbSQL.append(", TEN.SVSRFLG"); // F93 ：お客様お会計レジ
      sbSQL.append(", TEN.SVDIFLG"); // F94 ：ドライアイス
      sbSQL.append(", TEN.SVSSFLG"); // F95 ：証明写真
      sbSQL.append(", TEN.SVDPEFLG"); // F96 ：DPE
      sbSQL.append(", TEN.SVOSFLG"); // F97 ：お届けサービス
      sbSQL.append(", TEN.SVDMFLG"); // F98 ：電子マネー
      sbSQL.append(", TEN.SVPTFLG"); // F99 ：ペット減容器
      sbSQL.append(", TEN.SVAED"); // F100 ：AED
      sbSQL.append(", TEN.SVKSFLG"); // F101 ：くつろぎスペース
      sbSQL.append(", TEN.MODELCD"); // F102：モデル店
      sbSQL.append(", TEN.COMP1"); // F103 ：競合店１位
      sbSQL.append(", TEN.COMP2"); // F104 ：競合店２位
      sbSQL.append(", TEN.COMP3"); // F105 ：競合店３位
      sbSQL.append(", TEN.COMP4"); // F106 ：競合店４位
      sbSQL.append(", TEN.COMP5"); // F107 ：競合店５位
      sbSQL.append(", TEN.KAITENRT"); // F108 ：平均回転率
      sbSQL.append(", TEN.DAISU"); // F109 ：必要台数
      sbSQL.append(", TEN.KENTIKU"); // F110 ：建築面積
      sbSQL.append(", TEN.OPERATOR"); // F101 ：オペレータ
      sbSQL.append(", DATE_FORMAT(TEN.ADDDT, '%y/%m/%d')"); // F102 ：登録日
      sbSQL.append(", DATE_FORMAT(TEN.UPDDT, '%y/%m/%d')"); // F113 ：更新日
      sbSQL.append(", DATE_FORMAT(TEN.UPDDT, '%Y%m%d%H%i%s%f') as HDN_UPDDT"); // F114 ：更新日時
      sbSQL.append(", M1.NMKN"); // F115 ：テナント1_種別
      sbSQL.append(", M2.NMKN"); // F116 ：テナント2_種別
      sbSQL.append(", M3.NMKN"); // F117 ：テナント3_種別
      sbSQL.append(", M4.NMKN"); // F118 ：テナント4_種別
      sbSQL.append(", M5.NMKN"); // F119 ：テナント5_種別
      sbSQL.append(", M6.NMKN"); // F120 ：テナント6_種別
      sbSQL.append(", M7.NMKN"); // F121 ：テナント7_種別
      sbSQL.append(", M8.NMKN"); // F122 ：テナント8_種別
      sbSQL.append(", M9.NMKN"); // F123 ：テナント9_種別
      sbSQL.append(", M10.NMKN"); // F124 ：テナント10_種別
      sbSQL.append(", M11.NMKN"); // F125 ：テナント11_種別
      sbSQL.append(", M12.NMKN"); // F126 ：テナント12_種別
      sbSQL.append(", M13.NMKN"); // F127 ：テナント13_種別
      sbSQL.append(", M14.NMKN"); // F128 ：テナント14_種別
      sbSQL.append(", M15.NMKN"); // F129 ：テナント15_種別
      sbSQL.append(", M16.NMKN"); // F130 ：テナント16_種別
      sbSQL.append(", M17.NMKN"); // F131 ：テナント17_種別
      sbSQL.append(", M18.NMKN"); // F132 ：テナント18_種別
      sbSQL.append(", M19.NMKN"); // F133 ：テナント19_種別
      sbSQL.append(", M20.NMKN"); // F134 ：テナント20_種別
      sbSQL.append(", T1.TENANTKN"); // F135 ：テナント1_社名
      sbSQL.append(", T2.TENANTKN"); // F136 ：テナント2_社名
      sbSQL.append(", T3.TENANTKN"); // F137 ：テナント3_社名
      sbSQL.append(", T4.TENANTKN"); // F138 ：テナント4_社名
      sbSQL.append(", T5.TENANTKN"); // F139 ：テナント5_社名
      sbSQL.append(", T6.TENANTKN"); // F140 ：テナント6_社名
      sbSQL.append(", T7.TENANTKN"); // F141 ：テナント7_社名
      sbSQL.append(", T8.TENANTKN"); // F142 ：テナント8_社名
      sbSQL.append(", T9.TENANTKN"); // F143 ：テナント9_社名
      sbSQL.append(", T10.TENANTKN"); // F144 ：テナント10_社名
      sbSQL.append(", T11.TENANTKN"); // F145 ：テナント11_社名
      sbSQL.append(", T12.TENANTKN"); // F146 ：テナント12_社名
      sbSQL.append(", T13.TENANTKN"); // F147 ：テナント13_社名
      sbSQL.append(", T14.TENANTKN"); // F148 ：テナント14_社名
      sbSQL.append(", T15.TENANTKN"); // F149 ：テナント15_社名
      sbSQL.append(", T16.TENANTKN"); // F150 ：テナント16_社名
      sbSQL.append(", T17.TENANTKN"); // F151 ：テナント17_社名
      sbSQL.append(", T18.TENANTKN"); // F152 ：テナント18_社名
      sbSQL.append(", T19.TENANTKN"); // F153 ：テナント19_社名
      sbSQL.append(", T20.TENANTKN"); // F154 ：テナント20_社名
      sbSQL.append(", T21.SYAKU"); // F155 ：尺数_1部門
      sbSQL.append(", T22.SYAKU"); // F156 ：尺数_2部門
      sbSQL.append(", T23.SYAKU"); // F157 ：尺数_3部門
      sbSQL.append(", T24.SYAKU"); // F158 ：尺数_4部門
      sbSQL.append(", T25.SYAKU"); // F159 ：尺数_5部門
      sbSQL.append(", T26.SYAKU"); // F160 ：尺数_6部門
      sbSQL.append(", T27.SYAKU"); // F161 ：尺数_7部門
      sbSQL.append(", T28.SYAKU"); // F162 ：尺数_8部門
      sbSQL.append(", T29.SYAKU"); // F163 ：尺数_9部門
      sbSQL.append(", T30.SYAKU"); // F164 ：尺数_10部門
      sbSQL.append(", T31.SYAKU"); // F165 ：尺数_11部門
      sbSQL.append(", T32.SYAKU"); // F166 ：尺数_12部門
      sbSQL.append(", T33.SYAKU"); // F167 ：尺数_13部門
      sbSQL.append(", T34.SYAKU"); // F168 ：尺数_14部門
      sbSQL.append(", T35.SYAKU"); // F169 ：尺数_15部門
      sbSQL.append(", T36.SYAKU"); // F170 ：尺数_16部門
      sbSQL.append(", T37.SYAKU"); // F171 ：尺数_17部門
      sbSQL.append(", T38.SYAKU"); // F172 ：尺数_18部門
      sbSQL.append(", T39.SYAKU"); // F173 ：尺数_19部門
      sbSQL.append(", T40.SYAKU"); // F174 ：尺数_20部門
      sbSQL.append(", '1'"); // F175 ：部門コード
      sbSQL.append(", '2'"); // F156 ：部門コード
      sbSQL.append(", '3'"); // F177 ：部門コード
      sbSQL.append(", '4'"); // F178 ：部門コード
      sbSQL.append(", '5'"); // F179 ：部門コード
      sbSQL.append(", '6'"); // F180 ：部門コード
      sbSQL.append(", '7'"); // F181 ：部門コード
      sbSQL.append(", '8'"); // F182 ：部門コード
      sbSQL.append(", '9'"); // F183 ：部門コード
      sbSQL.append(", '10'"); // F184 ：部門コード
      sbSQL.append(", '11'"); // F185 ：部門コード
      sbSQL.append(", '12'"); // F186 ：部門コード
      sbSQL.append(", '13'"); // F187 ：部門コード
      sbSQL.append(", '15'"); // F188 ：部門コード
      sbSQL.append(", '34'"); // F189 ：部門コード
      sbSQL.append(", '43'"); // F190 ：部門コード
      sbSQL.append(", '44'"); // F191 ：部門コード
      sbSQL.append(", '54'"); // F192 ：部門コード
      sbSQL.append(", '20'"); // F193 ：部門コード
      sbSQL.append(", '23'"); // F194 ：部門コード
      sbSQL.append(" from INAMS.MSTTEN TEN");
      sbSQL.append(" left join INAMS.MSTTENTENANT T1");
      sbSQL.append(" on T1.TENCD = ?");
      sbSQL.append(" and  T1.TENANTKB = 1");
      sbSQL.append(" left join INAMS.MSTTENTENANT T2");
      sbSQL.append(" on T2.TENCD = ?");
      sbSQL.append(" and  T2.TENANTKB = 2");
      sbSQL.append(" left join INAMS.MSTTENTENANT T3");
      sbSQL.append(" on T3.TENCD = ?");
      sbSQL.append(" and  T3.TENANTKB = 3");
      sbSQL.append(" left join INAMS.MSTTENTENANT T4");
      sbSQL.append(" on T4.TENCD = ?");
      sbSQL.append(" and  T4.TENANTKB = 4");
      sbSQL.append(" left join INAMS.MSTTENTENANT T5");
      sbSQL.append(" on T5.TENCD = ?");
      sbSQL.append(" and  T5.TENANTKB = 5");
      sbSQL.append(" left join INAMS.MSTTENTENANT T6");
      sbSQL.append(" on T6.TENCD = ?");
      sbSQL.append(" and  T6.TENANTKB = 6");
      sbSQL.append(" left join INAMS.MSTTENTENANT T7");
      sbSQL.append(" on T7.TENCD = ?");
      sbSQL.append(" and  T7.TENANTKB = 7");
      sbSQL.append(" left join INAMS.MSTTENTENANT T8");
      sbSQL.append(" on T8.TENCD = ?");
      sbSQL.append(" and  T8.TENANTKB = 8");
      sbSQL.append(" left join INAMS.MSTTENTENANT T9");
      sbSQL.append(" on T9.TENCD = ?");
      sbSQL.append(" and  T9.TENANTKB = 9");
      sbSQL.append(" left join INAMS.MSTTENTENANT T10");
      sbSQL.append(" on T10.TENCD = ?");
      sbSQL.append(" and  T10.TENANTKB = 10");
      sbSQL.append(" left join INAMS.MSTTENTENANT T11");
      sbSQL.append(" on T11.TENCD = ?");
      sbSQL.append(" and  T11.TENANTKB = 11");
      sbSQL.append(" left join INAMS.MSTTENTENANT T12");
      sbSQL.append(" on T12.TENCD = ?");
      sbSQL.append(" and  T12.TENANTKB = 12");
      sbSQL.append(" left join INAMS.MSTTENTENANT T13");
      sbSQL.append(" on T13.TENCD = ?");
      sbSQL.append(" and  T13.TENANTKB = 13");
      sbSQL.append(" left join INAMS.MSTTENTENANT T14");
      sbSQL.append(" on T14.TENCD = ?");
      sbSQL.append(" and  T14.TENANTKB = 14");
      sbSQL.append(" left join INAMS.MSTTENTENANT T15");
      sbSQL.append(" on T15.TENCD = ?");
      sbSQL.append(" and  T15.TENANTKB = 15");
      sbSQL.append(" left join INAMS.MSTTENTENANT T16");
      sbSQL.append(" on T16.TENCD = ?");
      sbSQL.append(" and  T16.TENANTKB = 16");
      sbSQL.append(" left join INAMS.MSTTENTENANT T17");
      sbSQL.append(" on T17.TENCD = ?");
      sbSQL.append(" and  T17.TENANTKB = 17");
      sbSQL.append(" left join INAMS.MSTTENTENANT T18");
      sbSQL.append(" on T1.TENCD = ?");
      sbSQL.append(" and  T1.TENANTKB = 18");
      sbSQL.append(" left join INAMS.MSTTENTENANT T19");
      sbSQL.append(" on T1.TENCD = ?");
      sbSQL.append(" and  T1.TENANTKB = 19");
      sbSQL.append(" left join INAMS.MSTTENTENANT T20");
      sbSQL.append(" on T1.TENCD = ?");
      sbSQL.append(" and  T1.TENANTKB = 20");
      sbSQL.append(" left join INAMS.MSTTENBMNSK T21");
      sbSQL.append(" on T21.TENCD = ?");
      sbSQL.append(" and  T21.BMNCD = 1");
      sbSQL.append(" left join INAMS.MSTTENBMNSK T22");
      sbSQL.append(" on T22.TENCD = ?");
      sbSQL.append(" and  T22.BMNCD = 2");
      sbSQL.append(" left join INAMS.MSTTENBMNSK T23");
      sbSQL.append(" on T23.TENCD = ?");
      sbSQL.append(" and  T23.BMNCD = 3");
      sbSQL.append(" left join INAMS.MSTTENBMNSK T24");
      sbSQL.append(" on T24.TENCD = ?");
      sbSQL.append(" and  T24.BMNCD = 4");
      sbSQL.append(" left join INAMS.MSTTENBMNSK T25");
      sbSQL.append(" on T25.TENCD = ?");
      sbSQL.append(" and  T25.BMNCD = 5");
      sbSQL.append(" left join INAMS.MSTTENBMNSK T26");
      sbSQL.append(" on T26.TENCD = ?");
      sbSQL.append(" and  T26.BMNCD = 6");
      sbSQL.append(" left join INAMS.MSTTENBMNSK T27");
      sbSQL.append(" on T27.TENCD = ?");
      sbSQL.append(" and  T27.BMNCD = 7");
      sbSQL.append(" left join INAMS.MSTTENBMNSK T28");
      sbSQL.append(" on T28.TENCD = ?");
      sbSQL.append(" and  T28.BMNCD = 8");
      sbSQL.append(" left join INAMS.MSTTENBMNSK T29");
      sbSQL.append(" on T29.TENCD = ?");
      sbSQL.append(" and  T29.BMNCD = 9");
      sbSQL.append(" left join INAMS.MSTTENBMNSK T30");
      sbSQL.append(" on T30.TENCD = ?");
      sbSQL.append(" and  T30.BMNCD = 10");
      sbSQL.append(" left join INAMS.MSTTENBMNSK T31");
      sbSQL.append(" on T31.TENCD = ?");
      sbSQL.append(" and  T31.BMNCD = 11");
      sbSQL.append(" left join INAMS.MSTTENBMNSK T32");
      sbSQL.append(" on T32.TENCD = ?");
      sbSQL.append(" and  T32.BMNCD = 12");
      sbSQL.append(" left join INAMS.MSTTENBMNSK T33");
      sbSQL.append(" on T33.TENCD = ?");
      sbSQL.append(" and  T33.BMNCD = 13");
      sbSQL.append(" left join INAMS.MSTTENBMNSK T34");
      sbSQL.append(" on T34.TENCD = ?");
      sbSQL.append(" and  T34.BMNCD = 15");
      sbSQL.append(" left join INAMS.MSTTENBMNSK T35");
      sbSQL.append(" on T35.TENCD = ?");
      sbSQL.append(" and  T35.BMNCD = 34");
      sbSQL.append(" left join INAMS.MSTTENBMNSK T36");
      sbSQL.append(" on T36.TENCD = ?");
      sbSQL.append(" and  T36.BMNCD = 43");
      sbSQL.append(" left join INAMS.MSTTENBMNSK T37");
      sbSQL.append(" on T37.TENCD = ?");
      sbSQL.append(" and  T37.BMNCD = 44");
      sbSQL.append(" left join INAMS.MSTTENBMNSK T38");
      sbSQL.append(" on T38.TENCD = ?");
      sbSQL.append(" and  T38.BMNCD = 54");
      sbSQL.append(" left join INAMS.MSTTENBMNSK T39");
      sbSQL.append(" on T39.TENCD = ?");
      sbSQL.append(" and  T39.BMNCD = 20");
      sbSQL.append(" left join INAMS.MSTTENBMNSK T40");
      sbSQL.append(" on T40.TENCD = ?");
      sbSQL.append(" and  T40.BMNCD = 23");
      sbSQL.append(" left join INAMS.MSTMEISHO M1");
      sbSQL.append(" on M1.MEISHOKBN = 342");
      sbSQL.append(" and M1.MEISHOCD = '01'");
      sbSQL.append(" left join INAMS.MSTMEISHO M2");
      sbSQL.append(" on M2.MEISHOKBN = 342");
      sbSQL.append(" and M2.MEISHOCD = '02'");
      sbSQL.append(" left join INAMS.MSTMEISHO M3");
      sbSQL.append(" on M3.MEISHOKBN = 342");
      sbSQL.append(" and M3.MEISHOCD = '03'");
      sbSQL.append(" left join INAMS.MSTMEISHO M4");
      sbSQL.append(" on M4.MEISHOKBN = 342");
      sbSQL.append(" and M4.MEISHOCD = '04'");
      sbSQL.append(" left join INAMS.MSTMEISHO M5");
      sbSQL.append(" on M5.MEISHOKBN = 342");
      sbSQL.append(" and M5.MEISHOCD = '05'");
      sbSQL.append(" left join INAMS.MSTMEISHO M6");
      sbSQL.append(" on M6.MEISHOKBN = 342");
      sbSQL.append(" and M6.MEISHOCD = '06'");
      sbSQL.append(" left join INAMS.MSTMEISHO M7");
      sbSQL.append(" on M7.MEISHOKBN = 342");
      sbSQL.append(" and M7.MEISHOCD = '07'");
      sbSQL.append(" left join INAMS.MSTMEISHO M8");
      sbSQL.append(" on M8.MEISHOKBN = 342");
      sbSQL.append(" and M8.MEISHOCD = '08'");
      sbSQL.append(" left join INAMS.MSTMEISHO M9");
      sbSQL.append(" on M9.MEISHOKBN = 342");
      sbSQL.append(" and M9.MEISHOCD = '09'");
      sbSQL.append(" left join INAMS.MSTMEISHO M10");
      sbSQL.append(" on M10.MEISHOKBN = 342");
      sbSQL.append(" and M10.MEISHOCD = '10'");
      sbSQL.append(" left join INAMS.MSTMEISHO M11");
      sbSQL.append(" on M11.MEISHOKBN = 342");
      sbSQL.append(" and M11.MEISHOCD = '11'");
      sbSQL.append(" left join INAMS.MSTMEISHO M12");
      sbSQL.append(" on M12.MEISHOKBN = 342");
      sbSQL.append(" and M12.MEISHOCD = '12'");
      sbSQL.append(" left join INAMS.MSTMEISHO M13");
      sbSQL.append(" on M13.MEISHOKBN = 342");
      sbSQL.append(" and M13.MEISHOCD = '13'");
      sbSQL.append(" left join INAMS.MSTMEISHO M14");
      sbSQL.append(" on M14.MEISHOKBN = 342");
      sbSQL.append(" and M14.MEISHOCD = '14'");
      sbSQL.append(" left join INAMS.MSTMEISHO M15");
      sbSQL.append(" on M15.MEISHOKBN = 342");
      sbSQL.append(" and M15.MEISHOCD = '15'");
      sbSQL.append(" left join INAMS.MSTMEISHO M16");
      sbSQL.append(" on M16.MEISHOKBN = 342");
      sbSQL.append(" and M16.MEISHOCD = '16'");
      sbSQL.append(" left join INAMS.MSTMEISHO M17");
      sbSQL.append(" on M17.MEISHOKBN = 342");
      sbSQL.append(" and M17.MEISHOCD = '17'");
      sbSQL.append(" left join INAMS.MSTMEISHO M18");
      sbSQL.append(" on M18.MEISHOKBN = 342");
      sbSQL.append(" and M18.MEISHOCD = '18'");
      sbSQL.append(" left join INAMS.MSTMEISHO M19");
      sbSQL.append(" on M19.MEISHOKBN = 342");
      sbSQL.append(" and M19.MEISHOCD = '19'");
      sbSQL.append(" left join INAMS.MSTMEISHO M20");
      sbSQL.append(" on M20.MEISHOKBN = 342");
      sbSQL.append(" and M20.MEISHOCD = '20'");
      sbSQL.append(" where TEN.TENCD = ?");
      sbSQL.append(" and COALESCE(TEN.UPDKBN, 0) = 0");

    } else {
      sbSQL.append("select");
      sbSQL.append("  ''"); // F1 ：店コード
      sbSQL.append(", ''"); // F2 ：青果センターエリア
      sbSQL.append(", ''"); // F3 ：鮮魚区分
      sbSQL.append(", ''"); // F4 ：精肉区分
      sbSQL.append(", ''"); // F5 ：開設日
      sbSQL.append(", ''"); // F6 ：閉鎖日
      sbSQL.append(", ''"); // F7 ：改装日（1）
      sbSQL.append(", ''"); // F8 ：改装日（2）
      sbSQL.append(", ''"); // F9 ：販売部
      sbSQL.append(", ''"); // F10 ：地区
      sbSQL.append(", ''"); // F11 ：販売エリア
      sbSQL.append(", ''"); // F12 ：地域
      sbSQL.append(", ''"); // F13 ：予算区分_店舗
      sbSQL.append(", ''"); // F14 ：店舗名称（漢字）
      sbSQL.append(", ''"); // F15 ：店舗名称（カナ）
      sbSQL.append(", ''"); // F16 ：郵便番号_上桁
      sbSQL.append(", ''"); // F17 ：郵便番号_下桁
      sbSQL.append(", ''"); // F18 ：住所_都道府県（漢字）
      sbSQL.append(", ''"); // F19 ：住所_市区町村（漢字）
      sbSQL.append(", ''"); // F20 ：住所_町字（漢字）
      sbSQL.append(", ''"); // F21 ：住所_番地（漢字）
      sbSQL.append(", ''"); // F22 ：住所_都道府県（カナ）
      sbSQL.append(", ''"); // F23 ：住所_市区町村（カナ）
      sbSQL.append(", ''"); // F24 ：住所_町字（カナ）
      sbSQL.append(", ''"); // F25 ：住所_番地（カナ）
      sbSQL.append(", ''"); // F26 ：最寄り駅
      sbSQL.append(", ''"); // F27 ：バス停
      sbSQL.append(", ''"); // F28 ：オーナー（店）_名前
      sbSQL.append(", ''"); // F29 ：オーナー（店）_住所_都道府県
      sbSQL.append(", ''"); // F30 ：オーナー（店）_住所_市区町村
      sbSQL.append(", ''"); // F31 ：オーナー（店）_住所_町字
      sbSQL.append(", ''"); // F32 ：オーナー（店）_住所_番地
      sbSQL.append(", ''"); // F33 ：オーナー（駐車場）_名前
      sbSQL.append(", ''"); // F34 ：オーナー（駐車場）_住所_都道府県
      sbSQL.append(", ''"); // F35 ：オーナー（駐車場）_住所_市区町村
      sbSQL.append(", ''"); // F36 ：オーナー（駐車場）_住所_町字
      sbSQL.append(", ''"); // F37 ：オーナー（駐車場）_住所_番地
      sbSQL.append(", ''"); // F38 ：オーナー（その他）_名前
      sbSQL.append(", ''"); // F39 ：オーナー（その他）_住所_都道府県
      sbSQL.append(", ''"); // F40 ：オーナー（その他）_住所_市区町村
      sbSQL.append(", ''"); // F41 ：オーナー（その他）_住所_町村
      sbSQL.append(", ''"); // F42 ：オーナー（その他）_住所_番地
      sbSQL.append(", ''"); // F43 ：電話番号1
      sbSQL.append(", ''"); // F44 ：電話番号2
      sbSQL.append(", ''"); // F45 ：電話番号3
      sbSQL.append(", ''"); // F46 ：電話番号4
      sbSQL.append(", ''"); // F47 ：電話番号5
      sbSQL.append(", ''"); // F48 ：FAX番号1
      sbSQL.append(", ''"); // F49 ：FAX番号2
      sbSQL.append(", ''"); // F50 ：営業時間1_開始月日
      sbSQL.append(", ''"); // F51 ：営業時間1_終了月日
      sbSQL.append(", ''"); // F52 ：営業時間1_開始時間
      sbSQL.append(", ''"); // F53 ：営業時間1_終了時間
      sbSQL.append(", ''"); // F54 ：営業時間2_開始月日
      sbSQL.append(", ''"); // F55 ：営業時間2_終了月日
      sbSQL.append(", ''"); // F56 ：営業時間2_開始時間
      sbSQL.append(", ''"); // F57 ：営業時間2_終了時間
      sbSQL.append(", ''"); // F58 ：敷地面積
      sbSQL.append(", ''"); // F59 ：敷地面積_B1_床面積
      sbSQL.append(", ''"); // F60 ：敷地面積_B1_売場面積
      sbSQL.append(", ''"); // F61 ：敷地面積_1F_床面積
      sbSQL.append(", ''"); // F62 ：敷地面積_1F_売場面積
      sbSQL.append(", ''"); // F63 ：敷地面積_2F_床面積
      sbSQL.append(", ''"); // F64 ：敷地面積_2F_売場面積
      sbSQL.append(", ''"); // F65 ：敷地面積_3F_床面積
      sbSQL.append(", ''"); // F66 ：敷地面積_3F_売場面積
      sbSQL.append(", ''"); // F67 ：敷地面積_4F_床面積
      sbSQL.append(", ''"); // F68 ：敷地面積_4F_売場面積
      sbSQL.append(", ''"); // F69 ：駐車台数_普通車_敷地内
      sbSQL.append(", ''"); // F70 ：駐車台数_普通車_屋上
      sbSQL.append(", ''"); // F71 ：駐車台数_普通車_飛地
      sbSQL.append(", ''"); // F72 ：駐車台数_軽_敷地内
      sbSQL.append(", ''"); // F73 ：駐車台数_軽_屋上
      sbSQL.append(", ''"); // F74 ：駐車台数_軽_飛地
      sbSQL.append(", ''"); // F75 ：駐車台数_障害者_敷地内
      sbSQL.append(", ''"); // F76 ：駐車台数_障害者_屋上
      sbSQL.append(", ''"); // F77 ：駐車台数_障害者_飛地
      sbSQL.append(", ''"); // F78 ：エレベータ
      sbSQL.append(", ''"); // F79 ：エスカレータ
      sbSQL.append(", ''"); // F80 ：青果市場コード
      sbSQL.append(", ''"); // F81 ：実働人員
      sbSQL.append(", ''"); // F82 ：店運用区分
      sbSQL.append(", ''"); // F83 ：店舗年齢
      sbSQL.append(", ''"); // F84 ：日商
      sbSQL.append(", ''"); // F85 ：売上前比
      sbSQL.append(", ''"); // F86 ：荒利率
      sbSQL.append(", ''"); // F87 ：什器メーカー_冷設
      sbSQL.append(", ''"); // F88 ：什器メーカー_惣菜
      sbSQL.append(", ''"); // F89 ：什器メーカー_ゴンドラ
      sbSQL.append(", ''"); // F90 ：ingfanカード
      sbSQL.append(", ''"); // F81 ：ピュアウォーター
      sbSQL.append(", ''"); // F82 ：ATM
      sbSQL.append(", ''"); // F93 ：お客様お会計レジ
      sbSQL.append(", ''"); // F94 ：ドライアイス
      sbSQL.append(", ''"); // F95 ：証明写真
      sbSQL.append(", ''"); // F96 ：DPE
      sbSQL.append(", ''"); // F97 ：お届けサービス
      sbSQL.append(", ''"); // F98 ：電子マネー
      sbSQL.append(", ''"); // F99 ：ペット減容器
      sbSQL.append(", ''"); // F100 ：AED
      sbSQL.append(", ''"); // F101 ：くつろぎスペース
      sbSQL.append(", ''"); // F102 ：モデル店
      sbSQL.append(", ''"); // F103 ：競合店１位
      sbSQL.append(", ''"); // F104 ：競合店２位
      sbSQL.append(", ''"); // F105 ：競合店３位
      sbSQL.append(", ''"); // F106 ：競合店４位
      sbSQL.append(", ''"); // F107 ：競合店５位
      sbSQL.append(", ''"); // F108 ：平均回転率
      sbSQL.append(", ''"); // F109 ：必要台数
      sbSQL.append(", ''"); // F100 ：建築面積
      sbSQL.append(", ''"); // F101 ：オペレータ
      sbSQL.append(", ''"); // F102 ：登録日
      sbSQL.append(", ''"); // F113 ：更新日
      sbSQL.append(", ''"); // F114 ：更新日時
      sbSQL.append(", M1.NMKN"); // F115 ：テナント1_種別
      sbSQL.append(", M2.NMKN"); // F116 ：テナント2_種別
      sbSQL.append(", M3.NMKN"); // F117 ：テナント3_種別
      sbSQL.append(", M4.NMKN"); // F118 ：テナント4_種別
      sbSQL.append(", M5.NMKN"); // F119 ：テナント5_種別
      sbSQL.append(", M6.NMKN"); // F120 ：テナント6_種別
      sbSQL.append(", M7.NMKN"); // F121 ：テナント7_種別
      sbSQL.append(", M8.NMKN"); // F122 ：テナント8_種別
      sbSQL.append(", M9.NMKN"); // F123 ：テナント9_種別
      sbSQL.append(", M10.NMKN"); // F124 ：テナント10_種別
      sbSQL.append(", M11.NMKN"); // F125 ：テナント11_種別
      sbSQL.append(", M12.NMKN"); // F126 ：テナント12_種別
      sbSQL.append(", M13.NMKN"); // F127 ：テナント13_種別
      sbSQL.append(", M14.NMKN"); // F128 ：テナント14_種別
      sbSQL.append(", M15.NMKN"); // F129 ：テナント15_種別
      sbSQL.append(", M16.NMKN"); // F130 ：テナント16_種別
      sbSQL.append(", M17.NMKN"); // F131 ：テナント17_種別
      sbSQL.append(", M18.NMKN"); // F132 ：テナント18_種別
      sbSQL.append(", M19.NMKN"); // F133 ：テナント19_種別
      sbSQL.append(", M20.NMKN"); // F134 ：テナント20_種別
      sbSQL.append(", ''"); // F135 ：テナント1_社名
      sbSQL.append(", ''"); // F136 ：テナント2_社名
      sbSQL.append(", ''"); // F137 ：テナント3_社名
      sbSQL.append(", ''"); // F138 ：テナント4_社名
      sbSQL.append(", ''"); // F139 ：テナント5_社名
      sbSQL.append(", ''"); // F140 ：テナント6_社名
      sbSQL.append(", ''"); // F141 ：テナント7_社名
      sbSQL.append(", ''"); // F142 ：テナント8_社名
      sbSQL.append(", ''"); // F143 ：テナント9_社名
      sbSQL.append(", ''"); // F144 ：テナント10_社名
      sbSQL.append(", ''"); // F145 ：テナント11_社名
      sbSQL.append(", ''"); // F146 ：テナント12_社名
      sbSQL.append(", ''"); // F147 ：テナント13_社名
      sbSQL.append(", ''"); // F148 ：テナント14_社名
      sbSQL.append(", ''"); // F149 ：テナント15_社名
      sbSQL.append(", ''"); // F150 ：テナント16_社名
      sbSQL.append(", ''"); // F151 ：テナント17_社名
      sbSQL.append(", ''"); // F152 ：テナント18_社名
      sbSQL.append(", ''"); // F153 ：テナント19_社名
      sbSQL.append(", ''"); // F154 ：テナント20_社名
      sbSQL.append(", ''"); // F155 ：尺数_1部門
      sbSQL.append(", ''"); // F156 ：尺数_2部門
      sbSQL.append(", ''"); // F157 ：尺数_3部門
      sbSQL.append(", ''"); // F155 ：尺数_4部門
      sbSQL.append(", ''"); // F159 ：尺数_5部門
      sbSQL.append(", ''"); // F150 ：尺数_6部門
      sbSQL.append(", ''"); // F161 ：尺数_7部門
      sbSQL.append(", ''"); // F162 ：尺数_8部門
      sbSQL.append(", ''"); // F163 ：尺数_9部門
      sbSQL.append(", ''"); // F164 ：尺数_10部門
      sbSQL.append(", ''"); // F165 ：尺数_11部門
      sbSQL.append(", ''"); // F166 ：尺数_12部門
      sbSQL.append(", ''"); // F167 ：尺数_13部門
      sbSQL.append(", ''"); // F178 ：尺数_14部門
      sbSQL.append(", ''"); // F169 ：尺数_15部門
      sbSQL.append(", ''"); // F170 ：尺数_16部門
      sbSQL.append(", ''"); // F171 ：尺数_17部門
      sbSQL.append(", ''"); // F172 ：尺数_18部門
      sbSQL.append(", ''"); // F173 ：尺数_19部門
      sbSQL.append(", ''"); // F174 ：尺数_20部門
      sbSQL.append(", '1'"); // F175 ：部門コード
      sbSQL.append(", '2'"); // F176 ：部門コード
      sbSQL.append(", '3'"); // F177 ：部門コード
      sbSQL.append(", '4'"); // F178 ：部門コード
      sbSQL.append(", '5'"); // F179 ：部門コード
      sbSQL.append(", '6'"); // F180 ：部門コード
      sbSQL.append(", '7'"); // F181 ：部門コード
      sbSQL.append(", '8'"); // F182 ：部門コード
      sbSQL.append(", '9'"); // F183 ：部門コード
      sbSQL.append(", '10'"); // F184 ：部門コード
      sbSQL.append(", '11'"); // F185 ：部門コード
      sbSQL.append(", '12'"); // F186 ：部門コード
      sbSQL.append(", '13'"); // F187 ：部門コード
      sbSQL.append(", '15'"); // F188 ：部門コード
      sbSQL.append(", '34'"); // F189 ：部門コード
      sbSQL.append(", '43'"); // F190 ：部門コード
      sbSQL.append(", '44'"); // F191 ：部門コード
      sbSQL.append(", '54'"); // F192 ：部門コード
      sbSQL.append(", '20'"); // F193 ：部門コード
      sbSQL.append(", '23'"); // F194 ：部門コード
      sbSQL.append(" from INAMS.MSTTEN TEN");
      sbSQL.append(" left join INAMS.MSTMEISHO M1");
      sbSQL.append(" on M1.MEISHOKBN = 342");
      sbSQL.append(" and M1.MEISHOCD = '01'");
      sbSQL.append(" left join INAMS.MSTMEISHO M2");
      sbSQL.append(" on M2.MEISHOKBN = 342");
      sbSQL.append(" and M2.MEISHOCD = '02'");
      sbSQL.append(" left join INAMS.MSTMEISHO M3");
      sbSQL.append(" on M3.MEISHOKBN = 342");
      sbSQL.append(" and M3.MEISHOCD = '03'");
      sbSQL.append(" left join INAMS.MSTMEISHO M4");
      sbSQL.append(" on M4.MEISHOKBN = 342");
      sbSQL.append(" and M4.MEISHOCD = '04'");
      sbSQL.append(" left join INAMS.MSTMEISHO M5");
      sbSQL.append(" on M5.MEISHOKBN = 342");
      sbSQL.append(" and M5.MEISHOCD = '05'");
      sbSQL.append(" left join INAMS.MSTMEISHO M6");
      sbSQL.append(" on M6.MEISHOKBN = 342");
      sbSQL.append(" and M6.MEISHOCD = '06'");
      sbSQL.append(" left join INAMS.MSTMEISHO M7");
      sbSQL.append(" on M7.MEISHOKBN = 342");
      sbSQL.append(" and M7.MEISHOCD = '07'");
      sbSQL.append(" left join INAMS.MSTMEISHO M8");
      sbSQL.append(" on M8.MEISHOKBN = 342");
      sbSQL.append(" and M8.MEISHOCD = '08'");
      sbSQL.append(" left join INAMS.MSTMEISHO M9");
      sbSQL.append(" on M9.MEISHOKBN = 342");
      sbSQL.append(" and M9.MEISHOCD = '09'");
      sbSQL.append(" left join INAMS.MSTMEISHO M10");
      sbSQL.append(" on M10.MEISHOKBN = 342");
      sbSQL.append(" and M10.MEISHOCD = '10'");
      sbSQL.append(" left join INAMS.MSTMEISHO M11");
      sbSQL.append(" on M11.MEISHOKBN = 342");
      sbSQL.append(" and M11.MEISHOCD = '11'");
      sbSQL.append(" left join INAMS.MSTMEISHO M12");
      sbSQL.append(" on M12.MEISHOKBN = 342");
      sbSQL.append(" and M12.MEISHOCD = '12'");
      sbSQL.append(" left join INAMS.MSTMEISHO M13");
      sbSQL.append(" on M13.MEISHOKBN = 342");
      sbSQL.append(" and M13.MEISHOCD = '13'");
      sbSQL.append(" left join INAMS.MSTMEISHO M14");
      sbSQL.append(" on M14.MEISHOKBN = 342");
      sbSQL.append(" and M14.MEISHOCD = '14'");
      sbSQL.append(" left join INAMS.MSTMEISHO M15");
      sbSQL.append(" on M15.MEISHOKBN = 342");
      sbSQL.append(" and M15.MEISHOCD = '15'");
      sbSQL.append(" left join INAMS.MSTMEISHO M16");
      sbSQL.append(" on M16.MEISHOKBN = 342");
      sbSQL.append(" and M16.MEISHOCD = '16'");
      sbSQL.append(" left join INAMS.MSTMEISHO M17");
      sbSQL.append(" on M17.MEISHOKBN = 342");
      sbSQL.append(" and M17.MEISHOCD = '17'");
      sbSQL.append(" left join INAMS.MSTMEISHO M18");
      sbSQL.append(" on M18.MEISHOKBN = 342");
      sbSQL.append(" and M18.MEISHOCD = '18'");
      sbSQL.append(" left join INAMS.MSTMEISHO M19");
      sbSQL.append(" on M19.MEISHOKBN = 342");
      sbSQL.append(" and M19.MEISHOCD = '19'");
      sbSQL.append(" left join INAMS.MSTMEISHO M20");
      sbSQL.append(" on M20.MEISHOKBN = 342");
      sbSQL.append(" and M20.MEISHOCD = '20'");


    }

    // DB検索用パラメータ設定
    setParamData(paramData);

    if (DefineReport.ID_DEBUG_MODE)
      System.out.println(getClass().getSimpleName() + "[sql]" + sbSQL.toString());
    return sbSQL.toString();
  }

  private void outputQueryList() {

    // 検索条件の加工クラス作成
    JsonArrayData jad = new JsonArrayData();
    jad.setJsonString(getJson());

    // 保存用 List (検索情報)作成
    setWhere(new ArrayList<List<String>>());

    // 共通箇所設定
    createCmnOutput(jad);

  }


  /**
   * 更新処理
   *
   * @param request
   * @param session
   * @param map
   * @param userInfo
   * @return
   */
  public JSONObject update(HttpServletRequest request, HttpSession session, HashMap<String, String> map, User userInfo) {
    // 更新情報チェック(基本JS側で制御)
    JSONObject msgObj = new JSONObject();
    JSONArray msg = this.check(map);

    if (msg.size() > 0) {
      msgObj.put(MsgKey.E.getKey(), msg);
      return msgObj;
    }

    // 更新処理
    try {
      msgObj = this.updateData(map, userInfo);
    } catch (Exception e) {
      e.printStackTrace();
      msgObj.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
    }
    return msgObj;
  }

  /**
   * 更新処理実行
   *
   * @param map
   * @param userInfo
   * @return
   * @throws Exception
   */
  private JSONObject updateData(HashMap<String, String> map, User userInfo) throws Exception {
    JSONObject option = new JSONObject();
    JSONArray msg = new JSONArray();

    // パラメータ確認
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 更新情報

    // SQL発行：店舗マスタ
    this.createSqlTen(dataArray, map, userInfo);

    // 排他チェック実行
    String targetTable = null;
    String targetWhere = null;
    ArrayList<String> targetParam = new ArrayList<String>();
    if (dataArray.size() > 0) {
      JSONObject data = dataArray.getJSONObject(0);
      targetTable = "INAMS.MSTTEN";
      targetWhere = "TENCD = ?";
      targetParam.add(data.optString("F1"));
      if (!super.checkExclusion(targetTable, targetWhere, targetParam, data.optString("F114"))) {
        msg.add(MessageUtility.getDbMessageExclusion(FieldType.DEFAULT, new String[] {}));
        option.put(MsgKey.E.getKey(), msg);
        return option;
      }
    }

    ArrayList<Integer> countList = new ArrayList<Integer>();
    if (sqlList.size() > 0) {
      // 更新処理実行
      countList = super.executeSQLs(sqlList, prmList);
    }
    int successCount = 0;
    if (StringUtils.isEmpty(super.getMessage())) {
      for (int i = 0; i < countList.size(); i++) {
        successCount++;
        if (DefineReport.ID_DEBUG_MODE)
          System.out.println(MessageUtility.getMessage(Msg.S00006.getVal(), new String[] {lblList.get(i), Integer.toString(countList.get(i))}));
      }

    } else {
      option.put(MsgKey.E.getKey(), super.getMessage());
    }

    // SQL発行：店舗テナントマスタ
    this.createSqlTenant(dataArray, map, userInfo);

    countList = new ArrayList<Integer>();
    if (sqlList.size() > 0) {
      // 更新処理実行
      countList = super.executeSQLs(sqlList, prmList);
    }

    if (StringUtils.isEmpty(super.getMessage())) {
      for (int i = 0; i < countList.size(); i++) {
        successCount++;
        if (DefineReport.ID_DEBUG_MODE)
          System.out.println(MessageUtility.getMessage(Msg.S00006.getVal(), new String[] {lblList.get(i), Integer.toString(countList.get(i))}));
      }
    } else {
      option.put(MsgKey.E.getKey(), super.getMessage());
    }

    // SQL発行：店舗部門尺数マスタ
    this.createSqlSyakusu(dataArray, map, userInfo);

    countList = new ArrayList<Integer>();
    if (sqlList.size() > 0) {
      // 更新処理実行
      countList = super.executeSQLs(sqlList, prmList);
    }

    if (StringUtils.isEmpty(super.getMessage())) {
      for (int i = 0; i < countList.size(); i++) {
        successCount++;
        if (DefineReport.ID_DEBUG_MODE)
          System.out.println(MessageUtility.getMessage(Msg.S00006.getVal(), new String[] {lblList.get(i), Integer.toString(countList.get(i))}));
      }
    } else {
      option.put(MsgKey.E.getKey(), super.getMessage());
    }

    if (successCount == 0) {
      option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E10000.getVal()));
    } else {
      option.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00001.getVal()));
    }



    return option;
  }

  /**
   * 店舗マスタ INSERT/UPDATE処理のSQL作成
   *
   * @param dataArray
   * @param map
   * @param userInfo
   * @return
   */
  public String createSqlTen(JSONArray dataArray, HashMap<String, String> map, User userInfo) {
    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー
    String sendBtnid = map.get("SENDBTNID"); // 呼出しボタン

    StringBuffer sbSQL = new StringBuffer();
    ArrayList<String> prmData = new ArrayList<String>();
    Object[] valueData = new Object[] {};
    String values = "";

    int maxField = 110; // Fxxの最大値
    int len = dataArray.size();
    for (int i = 0; i < len; i++) {
      JSONObject data = dataArray.getJSONObject(i);
      if (!data.isEmpty()) {
        for (int k = 1; k <= maxField; k++) {
          String key = "F" + String.valueOf(k);

          /*
           * if (k == 1) { values += String.valueOf(i + 1); }
           */

          String val = data.optString(key);
          if (StringUtils.isEmpty(val)) {
            values += ", null";
          } else {
            values += ", ?";
            prmData.add(val);
          }

          if (k == maxField) {
            valueData = ArrayUtils.add(valueData, values);
            values = "";
          }
        }
      }

      if (valueData.length >= 100 || (i + 1 == len && valueData.length > 0)) {
        sbSQL = new StringBuffer();
        sbSQL.append("REPLACE INTO INAMS.MSTTEN (");
        sbSQL.append("  TENCD"); // F1 ：店コード
        sbSQL.append(", SEIKAAREAKBN"); // F2 ：青果センターエリア
        sbSQL.append(", SENGYOKBN"); // F3 ：鮮魚区分
        sbSQL.append(", SEINIKUKBN"); // F4 ：精肉区分
        sbSQL.append(", TENOPENDT"); // F5 ：開設日
        sbSQL.append(", TENCLOSEDT"); // F6 ：閉鎖日
        sbSQL.append(", KAISODT1"); // F7 ：改装日（1）
        sbSQL.append(", KAISODT2"); // F8 ：改装日（2）
        sbSQL.append(", HANBAIBUCD"); // F9 ：販売部
        sbSQL.append(", CHIKUCD"); // F10 ：地区
        sbSQL.append(", URIAERACD"); // F11 ：販売エリア
        sbSQL.append(", CHIIKICD"); // F12 ：地域
        sbSQL.append(", YOSANKBN_T"); // F13 ：予算区分_店舗
        sbSQL.append(", TENKN"); // F14 ：店舗名称（漢字）
        sbSQL.append(", TENAN"); // F15 ：店舗名称（カナ）
        sbSQL.append(", YUBINNO_U"); // F16 ：郵便番号_上桁
        sbSQL.append(", YUBINNO_S"); // F17 ：郵便番号_下桁
        sbSQL.append(", ADDRKN_T"); // F18 ：住所_都道府県（漢字）
        sbSQL.append(", ADDRKN_S"); // F19 ：住所_市区町村（漢字）
        sbSQL.append(", ADDRKN_M"); // F20 ：住所_町字（漢字）
        sbSQL.append(", ADDRKN_B"); // F21 ：住所_番地（漢字）
        sbSQL.append(", ADDRAN_T"); // F22 ：住所_都道府県（カナ）
        sbSQL.append(", ADDRAN_S"); // F23 ：住所_市区町村（カナ）
        sbSQL.append(", ADDRAN_M"); // F24 ：住所_町字（カナ）
        sbSQL.append(", ADDRAN_B"); // F25 ：住所_番地（カナ）
        sbSQL.append(", MOYORIEKIKN"); // F26 ：最寄り駅
        sbSQL.append(", BUSSTOPKN"); // F27 ：バス停
        sbSQL.append(", OWNT_NMKN"); // F28 ：オーナー（店）_名前
        sbSQL.append(", OWNT_ADDRKN_T"); // F29 ：オーナー（店）_住所_都道府県
        sbSQL.append(", OWNT_ADDRKN_S"); // F30 ：オーナー（店）_住所_市区町村
        sbSQL.append(", OWNT_ADDRKN_M"); // F31 ：オーナー（店）_住所_町字
        sbSQL.append(", OWNT_ADDRKN_B"); // F32 ：オーナー（店）_住所_番地
        sbSQL.append(", OWNP_NMKN"); // F33 ：オーナー（駐車場）_名前
        sbSQL.append(", OWNP_ADDRKN_T"); // F34 ：オーナー（駐車場）_住所_都道府県
        sbSQL.append(", OWNP_ADDRKN_S"); // F35 ：オーナー（駐車場）_住所_市区町村
        sbSQL.append(", OWNP_ADDRKN_M"); // F36 ：オーナー（駐車場）_住所_町字
        sbSQL.append(", OWNP_ADDRKN_B"); // F37 ：オーナー（駐車場）_住所_番地
        sbSQL.append(", OWNO_NMKN"); // F38 ：オーナー（その他）_名前
        sbSQL.append(", OWNO_ADDRKN_T"); // F39 ：オーナー（その他）_住所_都道府県
        sbSQL.append(", OWNO_ADDRKN_S"); // F40 ：オーナー（その他）_住所_市区町村
        sbSQL.append(", OWNO_ADDRKN_M"); // F41 ：オーナー（その他）_住所_町村
        sbSQL.append(", OWNO_ADDRKN_B"); // F42 ：オーナー（その他）_住所_番地
        sbSQL.append(", TEL1"); // F43 ：電話番号1
        sbSQL.append(", TEL2"); // F44 ：電話番号2
        sbSQL.append(", TEL3"); // F45 ：電話番号3
        sbSQL.append(", TEL4"); // F46 ：電話番号4
        sbSQL.append(", TEL5"); // F47 ：電話番号5
        sbSQL.append(", FAX1"); // F48 ：FAX番号1
        sbSQL.append(", FAX2"); // F49 ：FAX番号2
        sbSQL.append(", EGYOTM1_STMD"); // F50 ：営業時間1_開始月日
        sbSQL.append(", EGYOTM1_EDMD"); // F51 ：営業時間1_終了月日
        sbSQL.append(", EGYOTM1_STHM"); // F52 ：営業時間1_開始時間
        sbSQL.append(", EGYOTM1_EDHM"); // F53 ：営業時間1_終了時間
        sbSQL.append(", EGYOTM2_STMD"); // F54 ：営業時間2_開始月日
        sbSQL.append(", EGYOTM2_EDMD"); // F55 ：営業時間2_終了月日
        sbSQL.append(", EGYOTM2_STHM"); // F56 ：営業時間2_開始時間
        sbSQL.append(", EGYOTM2_EDHM"); // F57 ：営業時間2_終了時間
        sbSQL.append(", AREA_BA"); // F58 ：敷地面積
        sbSQL.append(", AERA_B1YUKA"); // F59 ：敷地面積_B1_床面積
        sbSQL.append(", AREA_B1URIBA"); // F60 ：敷地面積_B1_売場面積
        sbSQL.append(", AREA_1FYUKA"); // F61 ：敷地面積_1F_床面積
        sbSQL.append(", AREA_FURIBA"); // F62 ：敷地面積_1F_売場面積
        sbSQL.append(", AREA_2FYUKA"); // F63 ：敷地面積_2F_床面積
        sbSQL.append(", AREA_2FURIBA"); // F64 ：敷地面積_2F_売場面積
        sbSQL.append(", AREA_3FYUKA"); // F65 ：敷地面積_3F_床面積
        sbSQL.append(", AREA_3FURIBA"); // F66 ：敷地面積_3F_売場面積
        sbSQL.append(", AREA_4FYUKA"); // F67 ：敷地面積_4F_床面積
        sbSQL.append(", AREA_4FURIBA"); // F68 ：敷地面積_4F_売場面積
        sbSQL.append(", PARK_NM_BA"); // F69 ：駐車台数_普通車_敷地内
        sbSQL.append(", PARK_NM_YANE"); // F70 ：駐車台数_普通車_屋上
        sbSQL.append(", PARK_NM_TOBI"); // F71 ：駐車台数_普通車_飛地
        sbSQL.append(", PARK_LT_BA"); // F72 ：駐車台数_軽_敷地内
        sbSQL.append(", PARK_LT_YANE"); // F73 ：駐車台数_軽_屋上
        sbSQL.append(", PARK_LT_TOBI"); // F74 ：駐車台数_軽_飛地
        sbSQL.append(", PARK_HC_BA"); // F75 ：駐車台数_障害者_敷地内
        sbSQL.append(", PARK_HC_YANE"); // F76 ：駐車台数_障害者_屋上
        sbSQL.append(", PARK_HC_TOBI"); // F77 ：駐車台数_障害者_飛地
        sbSQL.append(", ELEVTRFLG"); // F78 ：エレベータ
        sbSQL.append(", ESCALTRFLG"); // F79 ：エスカレータ
        sbSQL.append(", SEIKACD"); // F80 ：青果市場コード
        sbSQL.append(", STAFFSU"); // F81 ：実働人員
        sbSQL.append(", MISEUNYOKBN"); // F82 ：店運用区分
        sbSQL.append(", TENAGE"); // F83 ：店舗年齢
        sbSQL.append(", URIDAY"); // F84 ：日商
        sbSQL.append(", URIZEN"); // F85 ：売上前比
        sbSQL.append(", ARARI"); // F86 ：荒利率
        sbSQL.append(", JMREI"); // F87 ：什器メーカー_冷設
        sbSQL.append(", JMSOU"); // F88 ：什器メーカー_惣菜
        sbSQL.append(", JMGON"); // F89 ：什器メーカー_ゴンドラ
        sbSQL.append(", SVIKFLG"); // F90 ：ingfanカード
        sbSQL.append(", SVPWFLG"); // F91 ：ピュアウォーター
        sbSQL.append(", SVATMFLG"); // F92 ：ATM
        sbSQL.append(", SVSRFLG"); // F93 ：お客様お会計レジ
        sbSQL.append(", SVDIFLG"); // F94 ：ドライアイス
        sbSQL.append(", SVSSFLG"); // F95 ：証明写真
        sbSQL.append(", SVDPEFLG"); // F96 ：DPE
        sbSQL.append(", SVOSFLG"); // F97 ：お届けサービス
        sbSQL.append(", SVDMFLG"); // F98 ：電子マネー
        sbSQL.append(", SVPTFLG"); // F99 ：ペット減容器
        sbSQL.append(", SVAED"); // F100 ：AED
        sbSQL.append(", SVKSFLG"); // F101 ：くつろぎスペース
        sbSQL.append(", MODELCD"); // F102 ：モデル店
        sbSQL.append(", COMP1"); // F103 ：競合店１位
        sbSQL.append(", COMP2"); // F104 ：競合店２位
        sbSQL.append(", COMP3"); // F105 ：競合店３位
        sbSQL.append(", COMP4"); // F106 ：競合店４位
        sbSQL.append(", COMP5"); // F107 ：競合店５位
        sbSQL.append(", KAITENRT"); // F108 ：平均回転率
        sbSQL.append(", DAISU"); // F109 ：必要台数
        sbSQL.append(", KENTIKU"); // F110 ：建築面積
        sbSQL.append(",UPDKBN"); // 更新区分：
        sbSQL.append(",SENDFLG"); // 送信区分：
        sbSQL.append(",OPERATOR"); // オペレーター：
        if (StringUtils.equals(DefineReport.Button.NEW.getObj(), sendBtnid)) {
          sbSQL.append(",ADDDT ");// 登録日 新規登録時には登録日の更新も行う。
        }
        sbSQL.append(",UPDDT"); // 更新日：

        sbSQL.append(")");
        sbSQL.append("VALUES (");
        sbSQL.append(StringUtils.join(valueData, ",").substring(1));
        sbSQL.append(", " + DefineReport.ValUpdkbn.NML.getVal()); // 更新区分
        sbSQL.append(", " + DefineReport.Values.SENDFLG_UN.getVal() + " "); // 送信区分：
        sbSQL.append(", '" + userId + "' "); // オペレーター
        if (StringUtils.equals(DefineReport.Button.NEW.getObj(), sendBtnid)) {
          sbSQL.append(", CURRENT_TIMESTAMP "); // 登録日 新規登録時には登録日の更新も行う。
        }
        sbSQL.append(", CURRENT_TIMESTAMP "); // 更新日
        sbSQL.append(")");

        if (DefineReport.ID_DEBUG_MODE)
          System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

        sqlList.add(sbSQL.toString());
        prmList.add(prmData);
        lblList.add("店舗マスタ");

        // クリア
        prmData = new ArrayList<String>();
        valueData = new Object[] {};
        values = "";
      }
    }

    return sbSQL.toString();
  }


  /**
   * 店舗テナントマスタ INSERT/UPDATE処理のSQL作成
   *
   * @param dataArray
   * @param map
   * @param userInfo
   * @return
   */
  public String createSqlTenant(JSONArray dataArray, HashMap<String, String> map, User userInfo) {
    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー
    String sendBtnid = map.get("SENDBTNID"); // 呼出しボタン

    StringBuffer sbSQL = new StringBuffer();
    ArrayList<String> prmData = new ArrayList<String>();
    String values = "";
    String updateRows = "";
    sqlList = new ArrayList<String>();
    prmList = new ArrayList<ArrayList<String>>();
    lblList = new ArrayList<String>();

    int maxField = 134; // Fxxの最大値
    int len = dataArray.size();
    int j = 0;
    for (int i = 0; i < len; i++) {
      JSONObject data = dataArray.getJSONObject(i);
      if (!data.isEmpty()) {
        for (int k = 115; k <= maxField; k++) {
          String key = "F" + String.valueOf(k);

          j++;
          if (!StringUtils.isEmpty(data.optString(key))) {
            prmData.add(data.optString("F1"));
            prmData.add(String.valueOf(j));
            if (StringUtils.isEmpty(data.optString("F" + (k + 20)))) {
              values += ",NULL";
            } else {
              prmData.add(data.optString("F" + (k + 20)));
              values += ",?";
            }
          }

        }
      }
      updateRows = StringUtils.removeStart(values, ",");
      if (!values.isEmpty()) {
        sbSQL = new StringBuffer();
        sbSQL.append("REPLACE INTO INAMS.MSTTENTENANT (");
        sbSQL.append("  TENCD"); // 店コード
        sbSQL.append(", TENANTKB"); // テント種別
        sbSQL.append(", TENANTKN"); // テナント社名
        // sbSQL.append(", SEQNO");
        sbSQL.append(", SENDFLG"); // 送信区分：
        sbSQL.append(", UPDKBN"); // 更新区分：
        sbSQL.append(", OPERATOR"); // オペレーター：
        if (StringUtils.equals(DefineReport.Button.NEW.getObj(), sendBtnid)) {
          sbSQL.append(", ADDDT ");// 登録日 新規登録時には登録日の更新も行う。
        }
        sbSQL.append(", UPDDT"); // 更新日：


        // sbSQL.append(" from (values " + updateRows + ") as T1(");
        // sbSQL.append(" TENCD");
        // sbSQL.append(", TENANTKB");
        // sbSQL.append(", TENANTKN");
        // sbSQL.append("))as RE on (T.TENCD = RE.TENCD and T.TENANTKB = RE.TENANTKB)");
        // sbSQL.append(" when matched then update set");
        // sbSQL.append(" TENCD = RE.TENCD");
        // sbSQL.append(", TENANTKB = RE.TENANTKB");
        // sbSQL.append(", TENANTKN = RE.TENANTKN");
        // sbSQL.append(", SENDFLG = RE.SENDFLG");
        // sbSQL.append(", UPDKBN = RE.UPDKBN");
        // sbSQL.append(", OPERATOR = RE.OPERATOR");
        // sbSQL.append(", UPDDT = RE.UPDDT");
        // sbSQL.append(" when not matched then insert(");
        // sbSQL.append(" TENCD");
        // sbSQL.append(", TENANTKB");
        // sbSQL.append(", TENANTKN");
        // sbSQL.append(", SENDFLG");
        // sbSQL.append(", UPDKBN");
        // sbSQL.append(", OPERATOR");
        // sbSQL.append(", ADDDT");
        // sbSQL.append(", UPDDT");


        sbSQL.append(") VALUES (");
        sbSQL.append("  " + updateRows + " ");
        // sbSQL.append(", ?");
        // sbSQL.append(", ?");
        // sbSQL.append(", ?");
        // sbSQL.append(", NULL");
        // sbSQL.append(", NULL");
        sbSQL.append(", 0 "); // 送信フラグ：
        sbSQL.append(", " + DefineReport.ValUpdkbn.NML.getVal() + " "); // 更新区分：
        sbSQL.append(", '" + userId + "' "); // オペレーター：
        if (StringUtils.equals(DefineReport.Button.NEW.getObj(), sendBtnid)) {
          sbSQL.append(", CURRENT_TIMESTAMP "); // 登録日 新規登録時には登録日の更新も行う。
        }
        sbSQL.append(", CURRENT_TIMESTAMP "); // 更新日
        sbSQL.append(")");

        if (DefineReport.ID_DEBUG_MODE)
          System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

        sqlList.add(sbSQL.toString());
        prmList.add(prmData);
        lblList.add("店舗テナントマスタ");

        // クリア
        prmData = new ArrayList<String>();
        values = "";
      }
    }

    return sbSQL.toString();
  }

  /**
   * 店舗部門尺数マスタ INSERT/UPDATE処理のSQL作成
   *
   * @param dataArray
   * @param map
   * @param userInfo
   * @return
   */
  public String createSqlSyakusu(JSONArray dataArray, HashMap<String, String> map, User userInfo) {
    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー
    String sendBtnid = map.get("SENDBTNID"); // 呼出しボタン

    StringBuffer sbSQL = new StringBuffer();
    ArrayList<String> prmData = new ArrayList<String>();
    String values = "";
    sqlList = new ArrayList<String>();
    prmList = new ArrayList<ArrayList<String>>();
    lblList = new ArrayList<String>();

    int maxField = 194; // Fxxの最大値
    int len = dataArray.size();
    for (int i = 0; i < len; i++) {
      JSONObject data = dataArray.getJSONObject(i);
      if (!data.isEmpty()) {
        for (int k = 175; k <= maxField; k++) {
          String key = "F" + String.valueOf(k);
          prmData.add(data.optString("F1"));
          prmData.add(data.optString(key));
          if (StringUtils.isEmpty(data.optString("F" + (k - 20)))) {
            values += ",(?, ?, null)";
          } else {
            prmData.add(data.optString("F" + (k - 20)));
            values += ",(?, ?, ?)";
          }

        }
      }
      StringUtils.removeStart(values, ",");
      if (!values.isEmpty()) {
        sbSQL = new StringBuffer();
        sbSQL.append("REPLACE INTO INAMS.MSTTENBMNSK (");
        sbSQL.append("  TENCD"); // 店コード
        sbSQL.append(", BMNCD"); // 部門コード
        sbSQL.append(", SYAKU"); // 尺数
        sbSQL.append(", SENDFLG"); // 送信区分
        sbSQL.append(", UPDKBN"); // 更新区分
        sbSQL.append(", OPERATOR"); // オペレーター
        if (StringUtils.equals(DefineReport.Button.NEW.getObj(), sendBtnid)) {
          sbSQL.append(", ADDDT"); // 登録日
        }
        sbSQL.append(", UPDDT"); // 更新日

        sbSQL.append(") values (");
        sbSQL.append("  TENCD");
        sbSQL.append(", BMNCD");
        sbSQL.append(", SYAKU");
        sbSQL.append(", " + DefineReport.Values.SENDFLG_UN.getVal() + " "); // 送信区分
        sbSQL.append(", " + DefineReport.ValUpdkbn.NML.getVal() + " "); // 更新区分
        sbSQL.append(", '" + userId + "' "); // オペレーター
        if (StringUtils.equals(DefineReport.Button.NEW.getObj(), sendBtnid)) {
          sbSQL.append(", CURRENT_TIMESTAMP "); // 登録日
        }
        sbSQL.append(", CURRENT_TIMESTAMP "); // 更新日
        sbSQL.append(")");

        if (DefineReport.ID_DEBUG_MODE)
          System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

        sqlList.add(sbSQL.toString());
        prmList.add(prmData);
        lblList.add("店舗部門尺数マスタ");

        // クリア
        prmData = new ArrayList<String>();
        values = "";
      }
    }

    return sbSQL.toString();
  }


  /**
   * チェック処理
   *
   * @param map
   * @return
   */
  @SuppressWarnings("static-access")
  public JSONArray check(HashMap<String, String> map) {
    // パラメータ確認
    String sendBtnid = map.get("SENDBTNID"); // 呼出しボタン
    JSONArray dataArray = JSONArray.fromObject(map.get("DATA")); // 更新情報

    JSONArray msg = new JSONArray();

    // チェック処理
    // 対象件数チェック
    if (dataArray.size() == 0 || dataArray.getJSONObject(0).isEmpty()) {
      msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
      return msg;
    }

    // 入力値を取得
    JSONObject data = dataArray.getJSONObject(0);

    ItemList iL = new ItemList();
    StringBuffer sbSQL = new StringBuffer();
    JSONArray dbDatas = new JSONArray();

    if (DefineReport.Button.NEW.getObj().equals(sendBtnid)) {
      // 重複チェック：店コード
      sbSQL = new StringBuffer();
      sbSQL.append("select * from INAMS.MSTTEN TEN where TEN.TENCD = " + data.optString("F1") + " and COALESCE(TEN.UPDKBN, 0) = 0");
      dbDatas = iL.selectJSONArray(sbSQL.toString(), null, Defines.STR_JNDI_DS);
      if (dbDatas.size() > 0) {
        msg.add(MessageUtility.getDbMessageIdObj("E11040", new String[] {"店コード"}));
      }
    }

    return msg;
  }
}
