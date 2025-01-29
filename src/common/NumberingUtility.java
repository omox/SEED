package common;

import java.util.ArrayList;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import authentication.bean.User;
import dao.ItemDao.SqlType;
import dao.Reportx002Dao;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 採番・付番処理管理クラス
 *
 * @author EATONE
 */
public class NumberingUtility {

  /**
   * コンストラクタ
   */
  public NumberingUtility() {
    super();
  }

  /**
   * 商品コード付番処理(取得のみ)<br>
   * 商品コードを仮に付番しておく処理、エラーがある場合は、エラー情報を返す
   *
   * @throws Exception
   */
  public static JSONObject execGetNewSHNCD(User userInfo, String inpshncd, String ketakbn, String bmncd) {

    // 商品マスタDao取得
    Reportx002Dao dao = new Reportx002Dao(Defines.STR_JNDI_DS);

    // ①添付資料（MD03100901）の商品コード付番機能
    // システム上は、空き番管理テーブルで利用可能かどうかをチェックする
    JSONObject result = dao.getNewSHNCD(inpshncd, ketakbn, bmncd);

    // ※取得できなかったらエラー
    if (result.size() == 0 || StringUtils.isEmpty(result.optString("VALUE"))) {
      // 手入力の場合、付番管理テーブルは条件に含めず、空き番のみで商品コード取得（使い回し考慮） or 付番済みJAVA再チェックもこちら
      if (DefineReport.ValKbn143.VAL1.getVal().equals(ketakbn)) {
        return MessageUtility.getDbMessageIdObj("E11203", new String[] {});
      } else {
        return MessageUtility.getDbMessageIdObj("E11160", new String[] {});
      }
    }
    String txt_shncd_new = result.optString("VALUE");

    // ②新規(正) 1.3 取得された商品コードが商品マスタテーブルに存在する場合、エラー。
    if (dao.checkMstExist(DefineReport.InpText.SHNCD.getObj(), txt_shncd_new)) {
      return MessageUtility.getDbMessageIdObj("E20162", new String[] {});
    }
    return result;
  }

  /**
   * 商品コード付番処理(追加)<br>
   * 商品コードを仮に付番しておく処理、エラーがある場合は、エラー情報を返す
   *
   * @throws Exception
   */
  public static JSONObject execHoldNewSHNCD(User userInfo, String txt_shncd_new) {

    // 商品マスタDao取得
    Reportx002Dao dao = new Reportx002Dao(Defines.STR_JNDI_DS);

    // 添付資料（MD03100901）の商品コード付番機能

    // 新規(正) 1.3 取得された商品コードが商品マスタテーブルに存在する場合、エラー。
    if (dao.checkMstExist(DefineReport.InpText.SHNCD.getObj(), txt_shncd_new)) {
      return MessageUtility.getDbMessageIdObj("E20162", new String[] {});
    }

    // システム上は、空き番管理テーブルで利用可能かどうかをチェックする
    // JSで付番したがすでにだれかに仮押さえされていた場合はエラー
    JSONObject result = dao.getNewSHNCD(txt_shncd_new, DefineReport.ValKbn143.VAL1.getVal(), null);
    if (result.size() == 0 || StringUtils.isEmpty(result.optString("VALUE"))) {
      return MessageUtility.getDbMessageIdObj("E30008", new String[] {});
    }

    // 設定内容
    JSONObject data = new JSONObject();
    data.put("F1", txt_shncd_new); // 取得商品コード
    data.put("F2", "1"); // 使用済みフラグ：使用済み

    // 正しく取得できた場合登録処理実施
    try {

      // --- 08.商品コード空き番
      JSONObject inf = dao.createSqlSYSSHNCD_AKI(userInfo.getId(), data, SqlType.MRG);

      ItemList iL = new ItemList();
      // 配列準備
      ArrayList<String> paramData = new ArrayList<String>();
      JSONArray prm = inf.optJSONArray("PRM");
      for (int i = 0; i < prm.size(); i++) {
        paramData.add(prm.optString(i));
      }
      String sqlcommand = inf.getString("SQL");

      iL.executeItem(sqlcommand, paramData, Defines.STR_JNDI_DS);
    } catch (Exception e) {
      return MessageUtility.getDbMessageIdObj("E11160", new String[] {});
    }
    return result;
  }

  /**
   * 付番商品コード解除処理(解除)<br>
   * 商品コードを付番していたが、チェックの結果、エラーなどのためやはり使わない場合の処理
   *
   * @throws Exception
   */
  public static JSONObject execReleaseNewSHNCD(User userInfo, String txt_shncd_new) {

    // 商品マスタDao取得
    Reportx002Dao dao = new Reportx002Dao(Defines.STR_JNDI_DS);

    // 設定内容
    JSONObject data = new JSONObject();
    data.put("F1", txt_shncd_new); // 取得商品コード
    data.put("F2", "0"); // 使用済みフラグ：使用済み

    try {

      // --- 08.商品コード空き番
      JSONObject inf = dao.createSqlSYSSHNCD_AKI(userInfo.getId(), data, SqlType.UPD);

      ItemList iL = new ItemList();
      // 配列準備
      ArrayList<String> paramData = new ArrayList<String>();
      JSONArray prm = inf.optJSONArray("PRM");
      for (int i = 0; i < prm.size(); i++) {
        paramData.add(prm.optString(i));
      }
      String sqlcommand = inf.getString("SQL");

      iL.executeItem(sqlcommand, paramData, Defines.STR_JNDI_DS);
    } catch (Exception e) {
      // 失敗した場合取得できなかったエラーにする
      return MessageUtility.getDbMessageIdObj("E00005", new String[] {});
    }
    return data;
  }


  /**
   * 販売コード付番処理(追加)<br>
   * 販売コードを仮に付番しておく処理、エラーがある場合は、エラー情報を返す
   *
   * @throws Exception
   */
  public static JSONObject execHoldNewURICD(User userInfo, String shncd) {

    // 商品マスタDao取得
    Reportx002Dao dao = new Reportx002Dao(Defines.STR_JNDI_DS);

    // 添付資料（MD03100901）の販売コード付番機能
    // システム上は、空き番管理テーブルで利用可能かどうかをチェックする
    JSONObject result = dao.getNewURICD(shncd);

    // ※取得できなかったらエラー
    if (result.size() == 0 || StringUtils.isEmpty(result.optString("VALUE"))) {
      return MessageUtility.getDbMessageIdObj("E11164", new String[] {});
    }
    return result;
  }


  /**
   * 付番販売コード解除処理(解除)<br>
   * 販売コードを付番していたが、チェックの結果、エラーなどのためやはり使わない場合の処理
   *
   * @throws Exception
   */
  public static JSONObject execReleaseNewURICD(User userInfo, String txt_uricd_new) {

    // 商品マスタDao取得
    Reportx002Dao dao = new Reportx002Dao(Defines.STR_JNDI_DS);

    // 設定内容
    JSONObject data = new JSONObject();
    data.put("F1", txt_uricd_new); // 取得販売コード
    data.put("F2", "0"); // 使用済みフラグ：使用済み

    try {

      // --- 08.販売コード空き番
      JSONObject inf = dao.createSqlSYSURICD_AKI(userInfo.getId(), data, SqlType.UPD);

      ItemList iL = new ItemList();
      // 配列準備
      ArrayList<String> paramData = new ArrayList<String>();
      JSONArray prm = inf.optJSONArray("PRM");
      for (int i = 0; i < prm.size(); i++) {
        paramData.add(prm.optString(i));
      }
      String sqlcommand = inf.getString("SQL");

      iL.executeItem(sqlcommand, paramData, Defines.STR_JNDI_DS);
    } catch (Exception e) {
      // 失敗した場合取得できなかったエラーにする
      return MessageUtility.getDbMessageIdObj("E00005", new String[] {});
    }

    return data;
  }

  /**
   * 添付資料（MD03100901）商品コードのチェックデジット計算<br>
   *
   * @throws Exception
   */
  public static JSONObject calcCheckdigitSHNCD(User userInfo, String shncd) {

    // 商品コードは8桁入力前提
    if (shncd.length() != 8) {
      return MessageUtility.getDbMessageIdObj("E11089", new String[] {});
    }
    // チェックデジット計算
    int checkdigit = calcShncdCheckdigit(shncd);
    if (NumberUtils.toInt(StringUtils.right(shncd, 1)) != checkdigit) {
      return MessageUtility.getDbMessageIdObj("E11204", new String[] {});
    }

    // 設定内容
    JSONObject data = new JSONObject();
    data.put("F1", shncd); // 取得コード
    return data;
  }

  /**
   * 添付資料（MD03111001）ソースコードのチェックデジット計算<br>
   *
   * @throws Exception
   */
  public static JSONObject calcCheckdigitSRCCD(User userInfo, String srccd, String sourcekbn) {

    String[] jankbn = new String[] {"1", "2"};
    String[] eankbn = new String[] {"3", "4"};
    String[] upcakbn = new String[] {"5"};
    String[] upcekbn = new String[] {"6"};

    // JAN
    if (ArrayUtils.contains(jankbn, sourcekbn)) {
      if (srccd.length() != 8 && srccd.length() != 13) {
        return MessageUtility.getDbMessageIdObj("E11168", new String[] {});
      }
      // ソース区分が1、2の場合、ソースコードの上2桁は45か49を入力して下さい。
      if (!ArrayUtils.contains(new String[] {"45", "49"}, StringUtils.left(srccd, 2))) {
        return MessageUtility.getDbMessageIdObj("E11166", new String[] {});
      }
      // チェックデジット計算
      int checkdigit = calcSrccdCheckdigit(srccd);
      if (NumberUtils.toInt(StringUtils.right(srccd, 1)) != checkdigit) {
        return MessageUtility.getDbMessageIdObj("E11165", new String[] {});
      }
    }

    // EAN
    if (ArrayUtils.contains(eankbn, sourcekbn)) {
      if (srccd.length() != 8 && srccd.length() != 13) {
        return MessageUtility.getDbMessageIdObj("E11168", new String[] {});
      }
      // ソース区分が3、4の場合、ソースコードの先頭2桁が10〜97（45,49は除く）を入力して下さい。
      if (ArrayUtils.contains(new String[] {"45", "49"}, StringUtils.left(srccd, 2)) || NumberUtils.toInt(StringUtils.left(srccd, 2)) < 10 || NumberUtils.toInt(StringUtils.left(srccd, 2)) > 97) {
        return MessageUtility.getDbMessageIdObj("E11167", new String[] {});
      }
      // チェックデジット計算
      int checkdigit = calcSrccdCheckdigit(srccd);
      if (NumberUtils.toInt(StringUtils.right(srccd, 1)) != checkdigit) {
        return MessageUtility.getDbMessageIdObj("E11165", new String[] {});
      }
    }

    // UPC-A
    if (ArrayUtils.contains(upcakbn, sourcekbn)) {
      if (srccd.length() != 10 && srccd.length() != 11) {
        return MessageUtility.getDbMessageIdObj("E11172", new String[] {});
      }

      if (srccd.length() == 11 && StringUtils.equals(StringUtils.left(srccd, 1), "0")) {
        return MessageUtility.getDbMessageIdObj("E11169", new String[] {});
      }

      // ソース区分が5でソースコードの先頭6桁が000000の場合、後ろ4の桁は9999以下で入力して下さい。
      if (srccd.length() == 10 && (StringUtils.equals(StringUtils.left(srccd, 6), "000000") && NumberUtils.toInt(StringUtils.right(srccd, 4)) > 9999)) {
        return MessageUtility.getDbMessageIdObj("E11224", new String[] {});
      }
    }

    // UPC-E
    if (ArrayUtils.contains(upcekbn, sourcekbn)) {
      if (srccd.length() != 6) {
        return MessageUtility.getDbMessageIdObj("E11171", new String[] {});
      }
    }


    new Reportx002Dao(Defines.STR_JNDI_DS);

    // 設定内容
    JSONObject data = new JSONObject();
    data.put("F1", srccd); // 取得コード
    return data;
  }



  /**
   * ITFコードのチェックデジット計算<br>
   *
   * @throws Exception
   */
  public static JSONObject calcCheckdigitITFCD(User userInfo, String itfcd) {

    // ITFコードは14桁入力前提
    if (itfcd.length() != 14) {
      return MessageUtility.getDbMessageIdObj("E11042", new String[] {});
    }
    // チェックデジット計算
    int checkdigit = calcItfcdCheckdigit(itfcd);
    if (NumberUtils.toInt(StringUtils.right(itfcd, 1)) != checkdigit) {
      return MessageUtility.getDbMessageIdObj("EX1048", new String[] {"ITFコード"});
    }

    // 設定内容
    JSONObject data = new JSONObject();
    data.put("F1", itfcd); // 取得コード
    return data;
  }

  /**
   * 商品コードのチェックデジットを返す（チェック方式：モジュラス11）
   *
   * @param cd
   * @return JSON文字列
   */
  private static int calcShncdCheckdigit(String cd) {

    // チェックデジット計算
    String cd_chk = cd;

    // １．このコードに何桁目であるかの番号をつけます。
    String[] arr = cd_chk.split("");

    // ２．7桁目まで右端から数えた桁数をかけてたします
    int res2 = NumberUtils.toInt(arr[0]) * 8 + NumberUtils.toInt(arr[1]) * 7 + NumberUtils.toInt(arr[2]) * 6 + NumberUtils.toInt(arr[3]) * 5 + NumberUtils.toInt(arr[4]) * 4
        + NumberUtils.toInt(arr[5]) * 3 + NumberUtils.toInt(arr[6]) * 2;

    // ３．２．で計算した数を11で割った余りをだします。
    int res3 = res2 % 11;

    // ４．11-３．で計算した数の下1桁がチェックデジット
    int res4 = (11 - res3) % 10;

    return res4;
  }

  /**
   * ソースコードのチェックデジットを返す
   *
   * @param cd
   * @return JSON文字列
   */
  private static int calcSrccdCheckdigit(String cd) {

    // TODO:チェックデジット計算
    String cd_chk = StringUtils.right("00000" + cd, 13);

    // １．このコードに右端から何桁目であるかの番号をつけます。 桁位置 13 12 11 10 9 8 7 6 5 4 3 2 1
    String[] arr = cd_chk.split("");

    // ２．偶数の桁に当たるコードの数字をすべてプラスする(ここでは０始まり)
    int res2 = NumberUtils.toInt(arr[1]) + NumberUtils.toInt(arr[3]) + NumberUtils.toInt(arr[5]) + NumberUtils.toInt(arr[7]) + NumberUtils.toInt(arr[9]) + NumberUtils.toInt(arr[11]);

    // ３．２．で計算した数を3倍にします。
    int res3 = res2 * 3;

    // ４．右端1の桁を除いた奇数の桁にあたる数字を同様にプラスします。
    int res4 = NumberUtils.toInt(arr[0]) + NumberUtils.toInt(arr[2]) + NumberUtils.toInt(arr[4]) + NumberUtils.toInt(arr[6]) + NumberUtils.toInt(arr[8]) + NumberUtils.toInt(arr[10]);

    // ５．３．と４．で得られた数字を合計する
    int res5 = res3 + res4;

    // ６．最後に５．で得られた数字の下1桁の数字を10から引きます。※下1桁が「0」となった場合は、チェックデジットは「0」となります。
    int res6 = res5 % 10 == 0 ? 0 : 10 - res5 % 10;

    return res6;
  }


  /**
   * ITFコードのチェックデジットを返す
   *
   * @param cd
   * @return JSON文字列
   */
  private static int calcItfcdCheckdigit(String cd) {

    // チェックデジット計算
    String cd_chk = cd;

    // １．このコードに右端から何桁目であるかの番号をつけます。 桁位置 14 13 12 11 10 9 8 7 6 5 4 3 2 1
    String[] arr = cd_chk.split("");

    // ２．偶数の桁に当たるコードの数字をすべてプラスする
    int res2 = NumberUtils.toInt(arr[0]) + NumberUtils.toInt(arr[2]) + NumberUtils.toInt(arr[4]) + NumberUtils.toInt(arr[6]) + NumberUtils.toInt(arr[8]) + NumberUtils.toInt(arr[10])
        + NumberUtils.toInt(arr[12]);

    // ３．２．で計算した数を3倍にします。
    int res3 = res2 * 3;

    // ４．右端1の桁を除いた奇数の桁にあたる数字を同様にプラスします。
    int res4 = NumberUtils.toInt(arr[1]) + NumberUtils.toInt(arr[3]) + NumberUtils.toInt(arr[5]) + NumberUtils.toInt(arr[7]) + NumberUtils.toInt(arr[9]) + NumberUtils.toInt(arr[11]);

    // ５．３．と４．で得られた数字を合計する
    int res5 = res3 + res4;

    // ６．最後に５．で得られた数字の下1桁の数字を10から引きます。※下1桁が「0」となった場合は、チェックデジットは「0」となります。
    int res6 = res5 % 10 == 0 ? 0 : 10 - res5 % 10;

    return res6;
  }
}
