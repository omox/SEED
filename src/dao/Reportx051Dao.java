package dao;

import java.io.FileNotFoundException;
import java.sql.JDBCType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import authentication.bean.User;
import common.DefineReport;
import common.DefineReport.DataType;
import common.Defines;
import common.FileList;
import common.InputChecker;
import common.ItemList;
import common.JsonArrayData;
import common.MessageUtility;
import common.MessageUtility.Msg;
import common.MessageUtility.MsgKey;
import dto.JQEasyModel;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 */
public class Reportx051Dao extends ItemDao {

  /** 最大処理件数 */
  public static int MAX_ROW = 100000;

  boolean isTest = true;

  boolean FullFileFlg = false;

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public Reportx051Dao(String JNDIname) {
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

    String szMakercd = getMap().get("MAKERCD"); // メーカコード
    String szMakerkn = getMap().get("MAKERKN"); // メーカー名
    String ckNomakercd = getMap().get("CHK_NOMAKERCD"); // チェック_メーカー名無し
    String ckDmakercd = getMap().get("CHK_DMAKERCD"); // チェック_代表メーカーコード
    String ckMakercd = getMap().get("CHK_MAKERCD"); // チェック_メーカー
    getMap().get("SENDBTNID");

    String sqlWhere = "";

    // タイトル情報(任意)設定
    List<String> titleList = new ArrayList<String>();

    // DB検索用パラメータ
    ArrayList<String> paramData = new ArrayList<String>();

    // 一覧表情報
    StringBuffer sbSQL = new StringBuffer();

    if (szMakercd.length() > 0) {
      sqlWhere += " and MAK1.MAKERCD >= ? ";
      paramData.add(szMakercd);
    }
    if (szMakerkn.length() > 0 && StringUtils.isEmpty(ckNomakercd)) {
      sqlWhere += " and MAK1.MAKERKN like ? ";
      paramData.add('%' + szMakerkn + '%');
    }
    if (StringUtils.equals("1", ckNomakercd)) {
      sqlWhere += " and (MAK1.MAKERKN is null or MAK1.MAKERKN = '')";
    }

    if (StringUtils.equals("1", ckDmakercd) || StringUtils.equals("1", ckMakercd)) {
      String sqlWhere_sub = "";
      if (StringUtils.equals("1", ckDmakercd)) {
        // sqlWhere += " and (MAK2.MAKERCD is not null)";
        sqlWhere_sub += " or MAK2.MAKERCD is not null or MAK1.DMAKERCD is null ";
      }
      if (StringUtils.equals("1", ckMakercd)) {
        sqlWhere_sub += " or MAK2.MAKERCD is null and MAK1.DMAKERCD is not null";
      }
      if (!StringUtils.isEmpty(sqlWhere_sub)) {
        sqlWhere += " and (" + StringUtils.replaceOnce(sqlWhere_sub, "or", "") + ")";
      }
    }

    sbSQL.append("select distinct");
    sbSQL.append(" case"); // F1 ：更新区分
    sbSQL.append("  when MAK2.MAKERCD is not null or MAK1.DMAKERCD is null then '代表'");
    sbSQL.append("  else '' end");
    sbSQL.append(", MAK1.MAKERCD"); // F2 ：メーカーコード
    sbSQL.append(", MAK1.MAKERKN"); // F3 ：メーカー名（漢字）
    sbSQL.append(", MAK1.DMAKERCD"); // F4 ：代表メーカーコード
    sbSQL.append(", MAK1.JANCD"); // F5 ：JANコード
    sbSQL.append(" from (select * from INAMS.MSTMAKER where IFNULL(UPDKBN, 0) = 0) MAK1");
    sbSQL.append(" left join (select * from INAMS.MSTMAKER where IFNULL(UPDKBN, 0) = 0) MAK2 on MAK2.DMAKERCD = MAK1.MAKERCD");
    sbSQL.append(" where IFNULL(MAK1.UPDKBN, 0) = 0");
    if (!StringUtils.isEmpty(sqlWhere)) {
      sbSQL.append(sqlWhere);
    }
    sbSQL.append(" order by MAK1.MAKERCD");

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

  public JQEasyModel upload(HttpSession session, User userInfo, HashMap<String, String> map, String file) {
    // jqEasy 用 JSON モデル作成
    JQEasyModel json = new JQEasyModel();
    JSONObject option = new JSONObject();
    JSONArray msgList = new JSONArray();
    String strMsg = null;

    // メッセージ情報取得
    MessageUtility mu = new MessageUtility();

    try {

      // ユーザー情報設定
      setUserInfo(userInfo);

      // *** 情報取得 ***
      FileList fl = new FileList();
      ArrayList<Object[]> dL = null;
      // ファイル読み込み
      dL = fl.readTxt(session, file);

      // *** 情報チェック ***
      int idxHeader = 0;
      // ファイルレイアウトチェック
      msgList = this.checkFileLayout(dL, idxHeader);

      // CSVを分解、画面上と同じように登録用のデータの形にする
      JSONArray astr = new JSONArray();
      JSONObject ostr = new JSONObject();

      // ファイル内レコード件数
      int cnt = 0;

      JSONArray dataArray = new JSONArray();
      JSONArray dataArrayJ = new JSONArray();
      if (msgList == null) {
        if (FullFileFlg) {
          // データ加工
          for (int i = idxHeader; i < dL.size(); i++) {
            Object[] data = dL.get(i);
            cnt++;
            for (FullFileLayout itm : FullFileLayout.values()) {
              String val = StringUtils.trim((String) data[itm.getNo() - 1]);

              if (itm.getTbl() == RefTable.MAKER) {

                if (itm.getCol().equals(FullFileLayout.UPDKBN.getCol())) {
                  if ("1".equals(val) || "3".equals(val)) {
                    ostr.put(itm.getCol(), 0);
                  } else {
                    ostr.put(itm.getCol(), 1);
                  }
                  // 1，3，9の更新区分も保持
                  ostr.put("TRUEUPDKBN", val);

                } else if (itm.getCol().equals(FullFileLayout.MAKERCD.getCol())) {
                  ostr.put(itm.getCol(), val);
                } else if (itm.getCol().equals(FullFileLayout.DMAKERCD.getCol())) {
                  ostr.put(itm.getCol(), val);
                } else if (itm.getCol().equals(FullFileLayout.MAKERKN.getCol())) {
                  if (val.isEmpty()) {
                    ostr.put(itm.getCol(), val);
                  } else {
                    StringBuffer sb = new StringBuffer();
                    int cnt1 = 0;

                    for (int j = 0; j < val.length(); j++) {
                      String tmpStr = val.substring(j, j + 1);
                      byte[] b = tmpStr.getBytes("SJIS");
                      if (cnt1 + b.length > 40) {
                        // ostr.put(itm.getCol(), val);
                        break;
                      } else {
                        sb.append(tmpStr);
                        cnt1 += b.length;
                      }

                    }
                    ostr.put(itm.getCol(), sb.toString());
                  }
                } else if (itm.getCol().equals(FullFileLayout.MAKERAN.getCol())) {
                  StringBuffer sb = new StringBuffer();
                  int cnt1 = 0;

                  for (int j = 0; j < val.length(); j++) {
                    String tmpStr = val.substring(j, j + 1);
                    byte[] b = tmpStr.getBytes("SJIS");
                    if (cnt1 + b.length > 20) {
                      // ostr.put(itm.getCol(), val);
                      break;
                    } else {
                      sb.append(tmpStr);
                      cnt1 += b.length;
                    }

                  }
                  ostr.put(itm.getCol(), sb.toString());
                } else if (itm.getCol().equals(FullFileLayout.HOUJINKEITAIICHI.getCol())) {
                  ostr.put(itm.getCol(), val);
                } else if (itm.getCol().equals(FullFileLayout.HOUJINKEITAIMOJI.getCol())) {
                  ostr.put(itm.getCol(), val);
                } else if (itm.getCol().equals(FullFileLayout.JUSYO1.getCol())) {
                  ostr.put(itm.getCol(), val);
                } else if (itm.getCol().equals(FullFileLayout.DELDT.getCol())) {
                  ostr.put(itm.getCol(), val);
                }
              }
            }
            astr.add(ostr);
            ostr = new JSONObject();
          }
        } else {
          // データ加工
          for (int i = idxHeader; i < dL.size(); i++) {
            Object[] data = dL.get(i);
            cnt++;
            for (ShortFileLayout itm : ShortFileLayout.values()) {
              String val = StringUtils.trim((String) data[itm.getNo() - 1]);
              if (itm.getTbl() == RefTable.MAKER) {
                if (itm.getCol().equals(FullFileLayout.UPDKBN.getCol())) {
                  if ("1".equals(val) || "3".equals(val)) {
                    ostr.put(itm.getCol(), 0);
                  } else {
                    ostr.put(itm.getCol(), 1);
                  }
                  // 1，3，9の更新区分も保持
                  ostr.put("TRUEUPDKBN", val);
                } else if (itm.getCol().equals(FullFileLayout.MAKERCD.getCol())) {
                  ostr.put(itm.getCol(), val);
                } else if (itm.getCol().equals(FullFileLayout.DMAKERCD.getCol())) {
                  ostr.put(itm.getCol(), val);
                } else if (itm.getCol().equals(FullFileLayout.DELDT.getCol())) {
                  ostr.put(itm.getCol(), val);
                }
              }
            }
            astr.add(ostr);
            ostr = new JSONObject();
          }
        }
        dataArray = this.selectMAKER(astr);
        dataArrayJ = this.selectJICFS(astr);
      }
      // 基本情報チェック(ファイル取込前チェック)

      int errCount = 0;

      // *** 詳細情報チェック＋情報登録用SQL作成 ***
      if (msgList == null) {

        dataArray = this.setUpdateData(map, userInfo, mu, dataArray);
        dataArrayJ = this.setUpdateDataJ(map, userInfo, mu, dataArrayJ);
        msgList = new JSONArray();
        boolean isError = false; // エラーデータは更新対象に含まれずスキップするため、エラーで処理をは止めない。

        if (isError) {
          errCount++;
          this.setMessage(msgList.toString());
        } else {
          // エラーが一件も存在しない場合更新処理を実行
          // 最新の情報取得
          // データ取得先判断

          JSONArray dataArrayDel = new JSONArray();// createDelMakerCd();

          this.createSqlMAKER(dataArray, dataArrayDel, userInfo);
          this.createSqlJICFS(dataArrayJ, userInfo);
          JSONObject rtn = this.updateData();

          if (rtn.containsKey(MsgKey.E.getKey())) {
            strMsg = rtn.get(MsgKey.E.getKey()).toString();
          } else {
            strMsg = this.getMessage();
          }
        }
      } else {
        errCount++;
        this.setMessage(msgList.toString());
      }
      if (!StringUtils.isEmpty(this.getMessage())) {
        strMsg = this.getMessage();
      } else if (strMsg == null) {
        JSONObject rtn = this.updateData();

        if (rtn.containsKey(MsgKey.E.getKey())) {
          strMsg = rtn.get(MsgKey.E.getKey()).toString();
        } else {
          strMsg = this.getMessage();
        }
      }

      // 更新成功
      String status = "処理";

      if (errCount == 0 && StringUtils.isEmpty(strMsg)) {
        status += "が終了しました";
      } else {
        status += "を中断しました";
        cnt = 0;
      }

      // 実行トラン情報をJSに戻す
      option.put(DefineReport.Text.SEQ.getObj(), "");
      option.put(DefineReport.Text.STATUS.getObj(), status);
      option.put(DefineReport.Text.UPD_NUMBER.getObj(), cnt);
      option.put(DefineReport.Text.ERR_NUMBER.getObj(), errCount);

      // option.put("result", success);
      json.setOpts(option);

    } catch (FileNotFoundException e) {
      e.printStackTrace();
      // msg = MessageUtility.getMsgText(Msg.E00003.getVal(), "\n" + e.getMessage());
    } catch (Exception e) {
      e.printStackTrace();
      // msg = MessageUtility.getMsgText(Msg.E00003.getVal(), "\n" + e.getMessage());
    }
    // 実行結果のメッセージを設定
    json.setMessage(strMsg);
    return json;
  }

  private JSONArray setUpdateData(HashMap<String, String> map, User userInfo, MessageUtility mu, JSONArray dataArray) {

    StringBuffer sbSQL = new StringBuffer();
    new JSONArray();
    ArrayList<String> prmData = new ArrayList<String>();
    JSONArray dbDatas = new JSONArray();
    // 関連情報取得
    ItemList iL = new ItemList();

    JSONArray dataArrayAf = new JSONArray(); // エラー扱いではないデータを保持する。
    JSONArray dataArrayEr = new JSONArray(); // エラー扱いのデータを保持する。

    // メーカーマスタ：重複チェック
    HashSet<String> checkData = new HashSet<String>();
    ArrayList<String> MAKERCODE = new ArrayList<>();
    for (int j = 0; j < dataArray.size(); j++) {
      String check = dataArray.optJSONObject(j).optString("F1");
      JSONObject data = dataArray.optJSONObject(j);

      boolean isErr = false; // エラー判定
      boolean isduplication = false; // 重複判定
      boolean isNull = false; // dataArrayAf配列に重複データが有るか判定

      if (check.isEmpty()) {
        continue;
      }
      checkData.add(check);
      MAKERCODE.add(check);

      // ファイル内重複チェック
      if (checkData.size() != MAKERCODE.size()) {
        MAKERCODE.remove((MAKERCODE.size() - 1));
        isduplication = true;
      }

      // 必須項目チェック
      String kbn = StringUtils.trim(data.optString("F1"));
      if (StringUtils.isEmpty(kbn)) {
        isErr = true;
      }

      // 属性チェック
      if (!isErr) {
        for (MSTMAKERLayout colinf : MSTMAKERLayout.values()) {
          String val = StringUtils.trim(data.optString(colinf.getId()));
          if (StringUtils.isNotEmpty(val)) {
            DataType dtype = null;
            int[] digit = null;
            try {
              DefineReport.InpText inpsetting = DefineReport.InpText.valueOf(colinf.getCol());
              dtype = inpsetting.getType();
              digit = new int[] {inpsetting.getDigit1(), inpsetting.getDigit2()};
            } catch (IllegalArgumentException e) {
              dtype = colinf.getDataType();
              digit = colinf.getDigit();
            }
            // ②データ桁チェック
            if (!InputChecker.checkDataLen(dtype, val, digit)) {
              isErr = true;
            }
            // ①データ型による文字種チェック
            if (!InputChecker.checkDataType(dtype, val)) {
              isErr = true;
            }
          }
        }
      }

      if (!isErr) {
        if (StringUtils.equals("1", data.optString("TRUEUPDKBN"))) {
          // チェック処理
          // 既存データチェック:メーカーマスタ
          sbSQL = new StringBuffer();
          prmData = new ArrayList<String>();
          sbSQL.append("select MAKERCD from INAMS.MSTMAKER where MAKERCD = ? and IFNULL(UPDKBN, 0) <> 1 fetch first 1 rows only");
          prmData.add(data.optString("F1"));
          dbDatas = iL.selectJSONArray(sbSQL.toString(), prmData, Defines.STR_JNDI_DS);
          if (dbDatas.size() > 0) {
            // 新規登録時に、同一のメーカーコードが既に登録されている場合はエラーとする。
            isErr = true;
          }

        } else if (StringUtils.equals("3", data.optString("TRUEUPDKBN"))) {
          // チェック処理
          // 部門紐付チェック:メーカーマスタ
          sbSQL = new StringBuffer();
          prmData = new ArrayList<String>();
          sbSQL.append("select MAKERCD from INAMS.MSTMAKER where MAKERCD = ? and IFNULL(UPDKBN, 0) <> 1 fetch first 1 rows only");
          prmData.add(data.optString("F1"));
          dbDatas = iL.selectJSONArray(sbSQL.toString(), prmData, Defines.STR_JNDI_DS);
          if (DefineReport.ID_DEBUG_MODE)
            System.out.println("既存データチェック:メーカーマスタ=" + dbDatas.size());
          if (dbDatas.size() == 0) {

            if (isduplication) {
              for (int j2 = 0; j2 < dataArrayAf.size(); j2++) {
                dataArray.optJSONObject(j2).optString("F1");
                JSONObject data2 = dataArray.optJSONObject(j2);

                boolean kondouCheck = data2.optString("F1").equals(check);
                if (kondouCheck) {
                  if (data2.optString("TRUEUPDKBN").equals("1")) {
                    break;
                  } else if (data2.optString("TRUEUPDKBN").equals("3")) {
                    break;
                  } else if (data2.optString("TRUEUPDKBN").equals("9")) {
                    break;
                  }
                }
              }
            } else {
              // 変更登録時に、同一のメーカーコードがテーブルに存在しない場合はエラーとする。
              isErr = true;
            }
          }
        } else if (StringUtils.equals("9", data.optString("TRUEUPDKBN"))) {

          // チェック処理
          // 部門紐付チェック:メーカーマスタ
          sbSQL = new StringBuffer();
          prmData = new ArrayList<String>();
          sbSQL.append("select DMAKERCD from INAMS.MSTMAKER where DMAKERCD = ? and IFNULL(UPDKBN, 0) <> 1 fetch first 1 rows only");
          prmData.add(data.optString("F1"));
          dbDatas = iL.selectJSONArray(sbSQL.toString(), prmData, Defines.STR_JNDI_DS);
          if (dbDatas.size() > 0) {
            isErr = true;
          }
        }
      }

      // 更新データを保持する。
      if (!isErr) {
        if (isduplication) {
          if (dataArrayAf.size() == 0) {
            isNull = true;
          }

          // メーカーコード重複データの場合
          for (int k = 0; k < dataArrayAf.size(); k++) {
            JSONObject dataAf = dataArrayAf.optJSONObject(k);
            String makerCd = dataAf.optString("F1");
            if (StringUtils.equals(check, makerCd)) {
              isNull = false;
              dataArrayAf.set(k, data);
              break;
            } else {
              isNull = true;
            }
          }

          // 更新区分１(新規)のエラーデータと重複の場合
          if (isNull && dataArrayEr.size() > 0) {
            for (int k = 0; k < dataArrayEr.size(); k++) {
              JSONObject dataAf = dataArrayEr.optJSONObject(k);
              String makerCd = dataAf.optString("F1");
              if (StringUtils.equals(check, makerCd)) {
                // dataArrayAf.set(k, data);
                dataArrayAf.add(data);
                break;
              }
            }
          }

        } else {
          dataArrayAf.add(data);
        }
      } else {
        dataArrayEr.add(data);
      }
    }
    return dataArrayAf;
  }

  private JSONArray setUpdateDataJ(HashMap<String, String> map, User userInfo, MessageUtility mu, JSONArray dataArray) {

    new StringBuffer();
    new JSONArray();
    new ArrayList<String>();
    new JSONArray();
    new ItemList();

    JSONArray dataArrayAf = new JSONArray(); // エラー扱いではないデータを保持する。

    // メーカーマスタ：重複チェック
    HashSet<String> checkData = new HashSet<String>();
    ArrayList<String> MAKERCODE = new ArrayList<>();
    for (int j = 0; j < dataArray.size(); j++) {
      String check = dataArray.optJSONObject(j).optString("F2");
      JSONObject data = dataArray.optJSONObject(j);

      boolean isErr = false; // エラー判定
      boolean isduplication = false; // 重複判定

      if (check.isEmpty()) {
        continue;
      }
      checkData.add(check);
      MAKERCODE.add(check);

      // ファイル内重複チェック
      if (checkData.size() != MAKERCODE.size()) {
        MAKERCODE.remove((MAKERCODE.size() - 1));
        isduplication = true;
      }

      // 必須項目チェック
      String kbn = StringUtils.trim(data.optString("F2"));
      if (StringUtils.isEmpty(kbn)) {
        isErr = true;
      }

      // 属性チェック
      if (!isErr) {
        for (JICFS_MSTMAKERLayout colinf : JICFS_MSTMAKERLayout.values()) {
          String val = StringUtils.trim(data.optString(colinf.getId()));
          if (StringUtils.isNotEmpty(val)) {
            DataType dtype = null;
            int[] digit = null;
            try {
              DefineReport.InpText inpsetting = DefineReport.InpText.valueOf(colinf.getCol());
              dtype = inpsetting.getType();
              digit = new int[] {inpsetting.getDigit1(), inpsetting.getDigit2()};
            } catch (IllegalArgumentException e) {
              dtype = colinf.getDataType();
              digit = colinf.getDigit();
            }
            // ②データ桁チェック
            if (!InputChecker.checkDataLen(dtype, val, digit)) {
              isErr = true;
            }
            // ①データ型による文字種チェック
            if (!InputChecker.checkDataType(dtype, val)) {
              isErr = true;
            }
          }
        }
      }

      // 更新データを保持する。
      if (!isErr) {
        if (isduplication) {
          // メーカーコード重複データの場合
          for (int k = 0; k < dataArrayAf.size(); k++) {
            JSONObject dataAf = dataArrayAf.optJSONObject(k);
            String makerCd = dataAf.optString("F2");
            if (StringUtils.equals(check, makerCd)) {
              dataArrayAf.set(k, data);
              break;
            }
          }
        } else {
          dataArrayAf.add(data);
        }
      }
    }
    return dataArrayAf;
  }



  /**
   * メーカーマスタINSERT/UPDATE処理
   *
   * @param data
   * @param map
   * @param userInfo
   */
  public String createSqlMAKER(JSONArray dataArray, JSONArray dataArrayDel, User userInfo) {

    StringBuffer sbSQL = new StringBuffer();

    // ログインユーザー情報取得
    String userId = userInfo.getId(); // ログインユーザー

    ArrayList<String> prmData = new ArrayList<String>();
    Object[] valueData = new Object[] {};
    String values = "";

    int maxField = 6; // Fxxの最大値
    for (int j = 0; j < dataArray.size(); j++) {
      for (int i = 1; i <= maxField; i++) {
        String col = "F" + i;
        String val = dataArray.optJSONObject(j).optString(col);
        if (StringUtils.isEmpty(val) && col.equals("F2")) {
          values += ", (select trim(SUBSTRING(MAKERAN, 1, 20)) from INAAD.JICFS_MSTMAKER where MAKERCD = " + dataArray.optJSONObject(j).optString("F1") + ")";
        } else if (StringUtils.isEmpty(val) && col.equals("F3")) {
          values += ", (select trim(SUBSTRING(MAKERKN, 1, 40)) from INAAD.JICFS_MSTMAKER where MAKERCD = " + dataArray.optJSONObject(j).optString("F1") + ")";
        } else if (StringUtils.isEmpty(val)) {
          values += ", null";
        } else {
          if (i == 1) {
            values += " ?";
          } else {
            values += " , ? ";
          }
          prmData.add(val);
        }
        // if(StringUtils.isEmpty(val)){
        // values += ", null";
        // }else{
        // if(i==1){
        // values += " ?";
        // }else{
        // values += " , ? ";
        // }
        // prmData.add(val);
        // }
        if (i == maxField) {
          valueData = ArrayUtils.add(valueData, "(" + values + ")");
          values = "";
        }
      }
      if (valueData.length >= 100 || (j + 1 == dataArray.size() && valueData.length > 0)) {
        // メーカーマスタの登録・更新
        sbSQL = new StringBuffer();
        sbSQL.append(" merge into INAMS.MSTMAKER as T using (select");
        sbSQL.append(" MAKERCD"); // メーカーコード
        sbSQL.append(", MAKERAN"); // メーカー名（カナ）
        sbSQL.append(", MAKERKN"); // メーカー名（漢字）
        sbSQL.append(", JANCD"); // JANコード
        sbSQL.append(", DMAKERCD"); // 代表メーカーコード
        sbSQL.append(", UPDKBN"); // 更新区分：
        sbSQL.append(", 0 as SENDFLG"); // 送信フラグ
        sbSQL.append(", '" + userId + "' AS OPERATOR "); // オペレーター：
        sbSQL.append(", current timestamp AS ADDDT "); // 登録日：
        sbSQL.append(", current timestamp AS UPDDT "); // 更新日：
        sbSQL.append(" from (values " + StringUtils.join(valueData, ",") + ") as T1( ");
        sbSQL.append(" MAKERCD");
        sbSQL.append(", MAKERAN");
        sbSQL.append(", MAKERKN");
        sbSQL.append(", JANCD");
        sbSQL.append(", DMAKERCD");
        sbSQL.append(", UPDKBN");
        sbSQL.append(" ))as RE on (T.MAKERCD = RE.MAKERCD)");
        sbSQL.append(" when matched then update set");
        sbSQL.append(" MAKERCD=RE.MAKERCD");
        // if(FullFileFlg){
        sbSQL.append(", MAKERAN=RE.MAKERAN");
        sbSQL.append(", MAKERKN=RE.MAKERKN");
        // }
        sbSQL.append(", DMAKERCD=RE.DMAKERCD");
        sbSQL.append(", UPDKBN=RE.UPDKBN");
        sbSQL.append(", SENDFLG=RE.SENDFLG");
        sbSQL.append(", OPERATOR=RE.OPERATOR");
        sbSQL.append(", UPDDT=RE.UPDDT");
        sbSQL.append(", ADDDT = case when IFNULL(UPDKBN, 0) = 1 then RE.ADDDT else ADDDT end");
        sbSQL.append(" when not matched then insert (");
        sbSQL.append(" MAKERCD");
        sbSQL.append(", MAKERAN");
        sbSQL.append(", MAKERKN");
        sbSQL.append(", DMAKERCD");
        sbSQL.append(", UPDKBN");
        sbSQL.append(", SENDFLG");
        sbSQL.append(", OPERATOR");
        sbSQL.append(", ADDDT");
        sbSQL.append(", UPDDT");
        sbSQL.append(") values (");
        sbSQL.append(" RE.MAKERCD");
        sbSQL.append(", RE.MAKERAN");
        sbSQL.append(", RE.MAKERKN");
        sbSQL.append(", RE.DMAKERCD");
        sbSQL.append(", RE.UPDKBN");
        sbSQL.append(", RE.SENDFLG");
        sbSQL.append(", RE.OPERATOR");
        sbSQL.append(", RE.ADDDT");
        sbSQL.append(", RE.UPDDT");
        sbSQL.append(")");

        if (DefineReport.ID_DEBUG_MODE)
          System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

        sqlList.add(sbSQL.toString());
        prmList.add(prmData);
        lblList.add("メーカーマスタ");

        // クリア
        prmData = new ArrayList<String>();
        valueData = new Object[] {};
        values = "";
      }
    }

    // メーカーマスタ削除
    prmData = new ArrayList<String>();
    valueData = new Object[] {};
    values = "";

    maxField = 1; // Fxxの最大値
    for (int j = 0; j < dataArrayDel.size(); j++) {
      for (int i = 1; i <= maxField; i++) {
        String col = "F" + i;
        String val = dataArrayDel.optJSONObject(j).optString(col);
        if (StringUtils.isEmpty(val)) {
          values += ", null";
        } else {
          if (i == 1) {
            values += " ?";
          } else {
            values += " , ? ";
          }
          prmData.add(val);
        }
        if (i == maxField) {
          valueData = ArrayUtils.add(valueData, "(" + values + ")");
          values = "";
        }
      }

      if (valueData.length >= 100 || (j + 1 == dataArrayDel.size() && valueData.length > 0)) {
        // メーカーマスタの登録・更新
        sbSQL = new StringBuffer();
        sbSQL.append(" merge into INAMS.MSTMAKER as T using (select");
        sbSQL.append(" MAKERCD"); // メーカーコード
        sbSQL.append(", 1 as UPDKBN"); // 更新区分
        sbSQL.append(", 0 as SENDFLG"); // 送信フラグ
        sbSQL.append(", '" + userId + "' AS OPERATOR "); // オペレーター：
        sbSQL.append(", current timestamp AS ADDDT "); // 登録日：
        sbSQL.append(", current timestamp AS UPDDT "); // 更新日：
        sbSQL.append(" from (values " + StringUtils.join(valueData, ",") + ") as T1( ");
        sbSQL.append(" MAKERCD");
        sbSQL.append(" ))as RE on (T.MAKERCD = RE.MAKERCD)");
        sbSQL.append(" when matched then update set");
        sbSQL.append(" UPDKBN=RE.UPDKBN");
        sbSQL.append(", SENDFLG=RE.SENDFLG");
        sbSQL.append(", OPERATOR=RE.OPERATOR");
        sbSQL.append(", UPDDT=RE.UPDDT");
        sbSQL.append(", ADDDT = case when IFNULL(UPDKBN, 0) = 1 then RE.ADDDT else ADDDT end");

        if (DefineReport.ID_DEBUG_MODE)
          System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

        sqlList.add(sbSQL.toString());
        prmList.add(prmData);
        lblList.add("メーカーマスタ_論理削除");

        // クリア
        prmData = new ArrayList<String>();
        valueData = new Object[] {};
        values = "";
      }
    }

    return sbSQL.toString();
  }

  /**
   * メーカーマスタINSERT/UPDATE処理
   *
   * @param data
   * @param map
   * @param userInfo
   */
  public String createSqlJICFS(JSONArray dataArray, User userInfo) {

    StringBuffer sbSQL = new StringBuffer();

    userInfo.getId();

    ArrayList<String> prmData = new ArrayList<String>();
    Object[] valueData = new Object[] {};
    String values = "";

    int maxField = 11; // Fxxの最大値
    for (int j = 0; j < dataArray.size(); j++) {
      for (int i = 1; i <= maxField; i++) {
        String col = "F" + i;
        String val = dataArray.optJSONObject(j).optString(col);
        if (StringUtils.isEmpty(val)) {
          values += ", null";
        } else {
          if (i == 1) {
            values += " ?";
          } else {
            values += " , ? ";
          }
          prmData.add(val);
        }
        if (i == maxField) {
          valueData = ArrayUtils.add(valueData, "(" + values + ")");
          values = "";
        }
      }
      if (valueData.length >= 100 || (j + 1 == dataArray.size() && valueData.length > 0)) {
        // メーカーマスタの登録・更新
        sbSQL = new StringBuffer();
        sbSQL.append(" merge into INAAD.JICFS_MSTMAKER as T using (select");
        sbSQL.append(" 'E' as RECKBN"); // レコード区分
        sbSQL.append(", 'D5' as DATAKIND"); // データ種別
        sbSQL.append(", UPDKBN"); // 更新区分
        sbSQL.append(", '1' as HYOJUNTANSHUKUKBN"); // 標準/短縮識別区分
        sbSQL.append(", MAKERCD"); // 標準メーカーコード
        sbSQL.append(", DMAKERCD"); // 代表メーカーコード
        sbSQL.append(", MAKERKN"); // メーカー名（漢字）
        sbSQL.append(", MAKERAN"); // メーカー名（カナ）
        sbSQL.append(", HOUJINKEITAIICHI"); // 法人形態（位置）
        sbSQL.append(", HOUJINKEITAIMOJI"); // 法人形態（文字）
        sbSQL.append(", JUSYO1"); // 住所1
        sbSQL.append(", TO_CHAR(current date, 'YYYYMMDD') as ADDDT"); // 登録年月日
        sbSQL.append(", TO_CHAR(current date, 'YYYYMMDD') as UPDDT"); // 内容更新年月日
        sbSQL.append(", DELDT"); // 廃止年月日
        sbSQL.append(" from (values " + StringUtils.join(valueData, ",") + ") as T1( ");
        sbSQL.append(" UPDKBN");
        sbSQL.append(", MAKERCD");
        sbSQL.append(", DMAKERCD");
        sbSQL.append(", MAKERKN");
        sbSQL.append(", MAKERAN");
        sbSQL.append(", HOUJINKEITAIICHI");
        sbSQL.append(", HOUJINKEITAIMOJI");
        sbSQL.append(", JUSYO1");
        sbSQL.append(", ADDDT");
        sbSQL.append(", UPDDT");
        sbSQL.append(", DELDT");
        sbSQL.append(" ))as RE on (T.MAKERCD = RE.MAKERCD)");
        sbSQL.append(" when matched then update set");
        sbSQL.append(" RECKBN=RE.RECKBN");
        sbSQL.append(", DATAKIND=RE.DATAKIND");
        sbSQL.append(", UPDKBN=RE.UPDKBN");
        sbSQL.append(", HYOJUNTANSHUKUKBN=RE.HYOJUNTANSHUKUKBN");
        sbSQL.append(", MAKERCD=RE.MAKERCD");
        sbSQL.append(", DMAKERCD=RE.DMAKERCD");
        if (FullFileFlg) {
          sbSQL.append(", MAKERKN=RE.MAKERKN");
          sbSQL.append(", MAKERAN=RE.MAKERAN");
          sbSQL.append(", HOUJINKEITAIICHI=RE.HOUJINKEITAIICHI");
          sbSQL.append(", HOUJINKEITAIMOJI=RE.HOUJINKEITAIMOJI");
          sbSQL.append(", JUSYO1=RE.JUSYO1");
        }
        sbSQL.append(", ADDDT=case when IFNULL(UPDKBN, 0) = 1 then RE.ADDDT else ADDDT end");
        sbSQL.append(", UPDDT=RE.UPDDT");
        sbSQL.append(", DELDT=RE.DELDT");
        sbSQL.append(" when not matched then insert (");
        sbSQL.append(" RECKBN");
        sbSQL.append(", DATAKIND");
        sbSQL.append(", UPDKBN");
        sbSQL.append(", HYOJUNTANSHUKUKBN");
        sbSQL.append(", MAKERCD");
        sbSQL.append(", DMAKERCD");
        sbSQL.append(", MAKERKN");
        sbSQL.append(", MAKERAN");
        sbSQL.append(", HOUJINKEITAIICHI");
        sbSQL.append(", HOUJINKEITAIMOJI");
        sbSQL.append(", JUSYO1");
        sbSQL.append(", ADDDT");
        sbSQL.append(", UPDDT");
        sbSQL.append(", DELDT");
        sbSQL.append(") values (");
        sbSQL.append(" RE.RECKBN");
        sbSQL.append(", RE.DATAKIND");
        sbSQL.append(", RE.UPDKBN");
        sbSQL.append(", RE.HYOJUNTANSHUKUKBN");
        sbSQL.append(", RE.MAKERCD");
        sbSQL.append(", RE.DMAKERCD");
        sbSQL.append(", RE.MAKERKN");
        sbSQL.append(", RE.MAKERAN");
        sbSQL.append(", RE.HOUJINKEITAIICHI");
        sbSQL.append(", RE.HOUJINKEITAIMOJI");
        sbSQL.append(", RE.JUSYO1");
        sbSQL.append(", RE.ADDDT");
        sbSQL.append(", RE.UPDDT");
        sbSQL.append(", RE.DELDT");
        sbSQL.append(")");

        if (DefineReport.ID_DEBUG_MODE)
          System.out.println(this.getClass().getName() + ":" + sbSQL.toString());

        sqlList.add(sbSQL.toString());
        prmList.add(prmData);
        lblList.add("JICFSメーカーマスタ_全件");

        // クリア
        prmData = new ArrayList<String>();
        valueData = new Object[] {};
        values = "";
      }
    }

    return sbSQL.toString();
  }

  /**
   * メーカコード情報取得処理
   *
   * @param isSei 正/予約
   * @param isNew 新規/更新
   * @param data CSV抜粋データ
   *
   * @throws Exception
   */
  public JSONArray selectMAKER(JSONArray array) {
    JSONObject dataAf = new JSONObject();
    JSONObject dataBf = new JSONObject();
    JSONArray dataArray = new JSONArray(); // 対象情報（主要な更新情報）

    for (int j = 0; j < array.size(); j++) {
      dataBf = array.getJSONObject(j);
      dataAf = new JSONObject();
      if (dataBf.isEmpty()) {
        continue;
      }

      String trueUpdkbn = dataBf.optString("TRUEUPDKBN");
      dataAf.put("TRUEUPDKBN", trueUpdkbn);

      if (FullFileFlg) {

        for (FullFileLayout itm : FullFileLayout.values()) {
          String value = "";
          if (StringUtils.equals(itm.getCol(), "UPDKBN")) {
            value = dataBf.optString("UPDKBN");
            dataAf.put("F6", value);
          } else if (StringUtils.equals(itm.getCol(), "MAKERCD")) {
            value = dataBf.optString("MAKERCD");
            dataAf.put("F1", value);
          } else if (StringUtils.equals(itm.getCol(), "DMAKERCD")) {
            value = dataBf.optString("DMAKERCD");
            dataAf.put("F5", value);
          } else if (StringUtils.equals(itm.getCol(), "MAKERKN")) {
            value = dataBf.optString("MAKERKN");
            dataAf.put("F3", value);
          } else if (StringUtils.equals(itm.getCol(), "MAKERAN")) {
            value = dataBf.optString("MAKERAN");
            dataAf.put("F2", value);
          } else if (StringUtils.equals(itm.getCol(), "DELDT")) {
            dataAf.put("F4", null);
          }
        }
      } else {
        for (ShortFileLayout itm : ShortFileLayout.values()) {
          String value = "";
          if (StringUtils.equals(itm.getCol(), "UPDKBN")) {
            value = dataBf.optString("UPDKBN");
            dataAf.put("F6", value);
          } else if (StringUtils.equals(itm.getCol(), "MAKERCD")) {
            value = dataBf.optString("MAKERCD");
            dataAf.put("F1", value);
          } else if (StringUtils.equals(itm.getCol(), "DMAKERCD")) {
            value = dataBf.optString("DMAKERCD");
            dataAf.put("F5", value);
          } else if (StringUtils.equals(itm.getCol(), "DELDT")) {
            dataAf.put("F2", null);
            dataAf.put("F3", null);
            dataAf.put("F4", null);
          }
        }
      }
      dataArray.add(dataAf);
    }
    return dataArray;
  }

  /**
   * メーカコード情報取得処理
   *
   * @param isSei 正/予約
   * @param isNew 新規/更新
   * @param data CSV抜粋データ
   *
   * @throws Exception
   */
  public JSONArray selectJICFS(JSONArray array) {
    JSONObject dataAf = new JSONObject();
    JSONObject dataBf = new JSONObject();
    JSONArray dataArray = new JSONArray(); // 対象情報（主要な更新情報）

    for (int j = 0; j < array.size(); j++) {
      dataBf = array.getJSONObject(j);
      dataAf = new JSONObject();
      if (dataBf.isEmpty()) {
        continue;
      }

      String trueUpdkbn = dataBf.optString("TRUEUPDKBN");
      dataAf.put("TRUEUPDKBN", trueUpdkbn);

      if (FullFileFlg) {

        for (FullFileLayout itm : FullFileLayout.values()) {
          String value = "";
          if (StringUtils.equals(itm.getCol(), "UPDKBN")) {
            value = dataBf.optString("UPDKBN");
            dataAf.put("F1", value);
          } else if (StringUtils.equals(itm.getCol(), "MAKERCD")) {
            value = dataBf.optString("MAKERCD");
            dataAf.put("F2", value);
          } else if (StringUtils.equals(itm.getCol(), "DMAKERCD")) {
            value = dataBf.optString("DMAKERCD");
            dataAf.put("F3", value);
          } else if (StringUtils.equals(itm.getCol(), "MAKERKN")) {
            value = dataBf.optString("MAKERKN");
            dataAf.put("F4", value);
          } else if (StringUtils.equals(itm.getCol(), "MAKERAN")) {
            value = dataBf.optString("MAKERAN");
            dataAf.put("F5", value);
          } else if (StringUtils.equals(itm.getCol(), "HOUJINKEITAIICHI")) {
            value = dataBf.optString("HOUJINKEITAIICHI");
            dataAf.put("F6", value);
          } else if (StringUtils.equals(itm.getCol(), "HOUJINKEITAIMOJI")) {
            value = dataBf.optString("HOUJINKEITAIMOJI");
            dataAf.put("F7", value);
          } else if (StringUtils.equals(itm.getCol(), "JUSYO1")) {
            value = dataBf.optString("JUSYO1");
            dataAf.put("F8", value);
          } else if (StringUtils.equals(itm.getCol(), "ADDDT")) {
            value = dataBf.optString("ADDDT");
            dataAf.put("F9", value);
          } else if (StringUtils.equals(itm.getCol(), "UPDDT")) {
            value = dataBf.optString("UPDDT");
            dataAf.put("F10", value);
          } else if (StringUtils.equals(itm.getCol(), "DELDT")) {
            value = dataBf.optString("DELDT");
            dataAf.put("F11", value);
          }
        }
      } else {
        for (ShortFileLayout itm : ShortFileLayout.values()) {
          String value = "";
          if (StringUtils.equals(itm.getCol(), "UPDKBN")) {
            value = dataBf.optString("UPDKBN");
            dataAf.put("F1", value);
          } else if (StringUtils.equals(itm.getCol(), "MAKERCD")) {
            value = dataBf.optString("MAKERCD");
            dataAf.put("F2", value);
          } else if (StringUtils.equals(itm.getCol(), "DMAKERCD")) {
            value = dataBf.optString("DMAKERCD");
            dataAf.put("F3", value);
          } else if (StringUtils.equals(itm.getCol(), "ADDDT")) {
            value = dataBf.optString("ADDDT");
            dataAf.put("F9", value);
          } else if (StringUtils.equals(itm.getCol(), "UPDDT")) {
            value = dataBf.optString("UPDDT");
            dataAf.put("F10", value);
          } else if (StringUtils.equals(itm.getCol(), "DELDT")) {
            value = dataBf.optString("DELDT");
            dataAf.put("F11", value);
            dataAf.put("F4", null);
            dataAf.put("F5", null);
            dataAf.put("F6", null);
            dataAf.put("F7", null);
            dataAf.put("F8", null);
          }
        }
      }
      dataArray.add(dataAf);
    }
    return dataArray;
  }

  public JSONArray createDelMakerCd() {

    JSONArray array = new JSONArray();

    // 関連情報取得
    StringBuffer sbSQL = new StringBuffer();
    JSONArray dbDatas = new JSONArray();
    ItemList iL = new ItemList();

    sbSQL.append(" SELECT T1.MAKERCD FROM INAMS.MSTMAKER T1");
    sbSQL.append(" WHERE T1.MAKERCD<>T1.DMAKERCD AND T1.UPDKBN <> 1 AND");
    sbSQL.append(" NOT EXISTS(");
    sbSQL.append(" SELECT 1 FROM(");
    sbSQL.append(" SELECT MAKERCD FROM INAMS.MSTSHN WHERE MAKERCD IS NOT NULL AND UPDKBN<>1");
    sbSQL.append(" UNION ALL");
    sbSQL.append(" SELECT MAKERCD FROM INAMS.MSTSHN_Y WHERE MAKERCD IS NOT NULL AND UPDKBN<>1)T2");
    sbSQL.append(" WHERE T1.MAKERCD=T2.MAKERCD) AND");
    sbSQL.append(" NOT EXISTS(");
    sbSQL.append(" SELECT 1 FROM");
    sbSQL.append(" (SELECT CASE WHEN SOURCEKBN = 1 AND left(SRCCD,3) IN ('456','457','458','459') THEN left(SRCCD,9) ");
    sbSQL.append(" WHEN SOURCEKBN = 1 AND left(SRCCD,3) NOT IN ('456','457','458','459') THEN left(SRCCD,7)");
    sbSQL.append(" WHEN SOURCEKBN = 2 THEN left(SRCCD,6)");
    sbSQL.append(" ELSE LEFT(SHNCD,2) || '00002' END AS MAKERCD");
    sbSQL.append(" FROM INAMS.MSTSRCCD");
    sbSQL.append(" WHERE SEQNO=1");
    sbSQL.append(" UNION ALL");
    sbSQL.append(" SELECT CASE WHEN SOURCEKBN = 1 AND left(SRCCD,3) IN ('456','457','458','459') THEN left(SRCCD,9) ");
    sbSQL.append(" WHEN SOURCEKBN = 1 AND left(SRCCD,3) NOT IN ('456','457','458','459') THEN left(SRCCD,7)");
    sbSQL.append(" WHEN SOURCEKBN = 2 THEN left(SRCCD,6)");
    sbSQL.append(" ELSE LEFT(SHNCD,2) || '00002' END AS MAKERCD");
    sbSQL.append(" FROM INAMS.MSTSRCCD_Y");
    sbSQL.append(" WHERE SEQNO=1)T2");
    sbSQL.append(" WHERE T1.MAKERCD=T2.MAKERCD)");

    dbDatas = iL.selectJSONArray(sbSQL.toString(), new ArrayList<String>(), Defines.STR_JNDI_DS);

    for (int i = 0; i < dbDatas.size(); i++) {
      JSONObject obj = new JSONObject();

      String makerCd = dbDatas.getJSONObject(i).optString("MAKERCD");
      obj.put("F1", makerCd);
      array.add(obj);
    }
    return array;
  }

  private JSONArray checkFileLayout(ArrayList<Object[]> eL, int idxHeader) {
    // 1.件数チェック
    JSONArray msg = new JSONArray();
    MessageUtility mu = new MessageUtility();
    if (eL.size() <= idxHeader) {
      // return "ファイルにデータがありません。";
      // JSONObject o = MessageUtility.getDbMessageObj("E40001", new String[]{});
      JSONObject o = mu.getDbMessageObj("E40001", new String[] {});
      msg.add(o);
      return msg;
    } else if (eL.size() > MAX_ROW + idxHeader) {
      // return "データが多すぎます。当画面では、"+MAX_ROW+"件以下のデータを想定しています。";
      // JSONObject o = MessageUtility.getDbMessageObj("E11234", new String[]{});
      JSONObject o = mu.getDbMessageObj("E11234", new String[] {});
      msg.add(o);
      return msg;
    }

    // 3.項目数チェック
    if (FullFileLayout.values().length != eL.get(idxHeader).length && ShortFileLayout.values().length != eL.get(idxHeader).length) {
      // JSONObject o = MessageUtility.getDbMessageIdObj("E40008", new String[]{});
      JSONObject o = mu.getDbMessageObj("E40008", new String[] {});
      msg.add(o);
      return msg;
    }

    if (FullFileLayout.values().length == eL.get(idxHeader).length) {
      FullFileFlg = true;
    } else {
      FullFileFlg = false;
    }

    return null;
  }

  /** SQLリスト保持用変数 */
  ArrayList<String> sqlList = new ArrayList<String>();
  /** SQLのパラメータリスト保持用変数 */
  ArrayList<ArrayList<String>> prmList = new ArrayList<ArrayList<String>>();
  /** SQLログ用のラベルリスト保持用変数 */
  ArrayList<String> lblList = new ArrayList<String>();

  /**
   * 更新処理実行
   *
   * @return
   *
   * @throws Exception
   */
  private JSONObject updateData() throws Exception {

    JSONObject option = new JSONObject();

    ArrayList<Integer> countList = new ArrayList<Integer>();
    if (sqlList.size() > 0) {
      countList = super.executeSQLs(sqlList, prmList);
    }

    if (StringUtils.isEmpty(getMessage())) {
      int count = 0;
      for (int i = 0; i < countList.size(); i++) {
        count += countList.get(i);
        if (DefineReport.ID_DEBUG_MODE)
          System.out.println(MessageUtility.getMessage(Msg.S00006.getVal(), new String[] {lblList.get(i), Integer.toString(countList.get(i))}));
      }
      if (count == 0) {
        option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E10000.getVal()));
      } else {
        option.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00001.getVal()));
      }
    } else {
      option.put(MsgKey.E.getKey(), getMessage());
    }

    return option;
  }

  /** File出力項目の参照テーブル */
  public enum RefTable {

    /** メーカーマスタ */
    MAKER(1, "メーカーマスタ"),
    /** その他 */
    OTHER(2, "その他");

    private final Integer col;
    private final String txt;

    /** 初期化 */
    private RefTable(Integer col, String txt) {
      this.col = col;
      this.txt = txt;
    }

    /** @return col 列番号 */
    public Integer getCol() {
      return col;
    }

    /** @return txt 表示名称 */
    public String getTxt() {
      return txt;
    }
  }

  /** JICFSメーカーマスタフル Fileレイアウト */
  public enum FullFileLayout {
    /** 更新区分 */
    RECKBN(1, "レコード区分", RefTable.OTHER, "RECKBN"), DATAKIND(2, "データ種別", RefTable.OTHER, "DATAKIND"), UPDKBN(3, "更新区分", RefTable.MAKER, "UPDKBN"), HYOJUNTANSHUKUKBN(4, "標準/短縮識別区分", RefTable.OTHER,
        "HYOJUNTANSHUKUKBN"), MAKERCD(5, "標準メーカコード", RefTable.MAKER, "MAKERCD"), DMAKERCD(6, "代表メーカコード", RefTable.MAKER, "DMAKERCD"), MAKERKN(7, "会社名", RefTable.MAKER, "MAKERKN"), MAKERAN(8, "会社名カナ",
            RefTable.MAKER, "MAKERAN"), HOUJINKEITAIICHI(9, "法人形態（位置）", RefTable.MAKER, "HOUJINKEITAIICHI"), HOUJINKEITAIMOJI(10, "法人形態（文字）", RefTable.MAKER, "HOUJINKEITAIMOJI"), JUSYO1(11, "住所１",
                RefTable.MAKER, "JUSYO1"), ADDDT(12, "登録年月日", RefTable.OTHER, "ADDDT"), UPDDT(13, "内容変更年月日", RefTable.OTHER, "UPDDT"), DELDT(14, "廃止年月日", RefTable.MAKER, "DELDT");

    private final Integer no;
    private final String txt;
    private final RefTable tbl;
    private final String col;

    /** 初期化 */
    private FullFileLayout(Integer no, String txt, RefTable tbl, String col) {
      this.no = no;
      this.txt = txt;
      this.tbl = tbl;
      this.col = col;
    }

    /** @return col 列番号 */
    public Integer getNo() {
      return no;
    }

    /** @return txt 表示名称 */
    public String getTxt() {
      return txt;
    }

    /** @return tbl 参照テーブル */
    public RefTable getTbl() {
      return tbl;
    }

    /** @return tbl 参照列 */
    public String getCol() {
      return col;
    }
  }

  /** JICFSメーカーマスタショート Fileレイアウト */
  public enum ShortFileLayout {
    /** 更新区分 */
    RECKBN(1, "レコード区分", RefTable.OTHER, "RECKBN"), DATAKIND(2, "データ種別", RefTable.OTHER, "DATAKIND"), UPDKBN(3, "更新区分", RefTable.MAKER, "UPDKBN"), HYOJUNTANSHUKUKBN(4, "標準/短縮識別区分", RefTable.OTHER,
        "HYOJUNTANSHUKUKBN"), MAKERCD(5, "標準メーカコード", RefTable.MAKER, "MAKERCD"), DMAKERCD(6, "代表メーカコード", RefTable.MAKER,
            "DMAKERCD"), ADDDT(7, "登録年月日", RefTable.OTHER, "ADDDT"), UPDDT(8, "内容変更年月日", RefTable.OTHER, "UPDDT"), DELDT(9, "廃止年月日", RefTable.MAKER, "DELDT");

    private final Integer no;
    private final String txt;
    private final RefTable tbl;
    private final String col;

    /** 初期化 */
    private ShortFileLayout(Integer no, String txt, RefTable tbl, String col) {
      this.no = no;
      this.txt = txt;
      this.tbl = tbl;
      this.col = col;
    }

    /** @return col 列番号 */
    public Integer getNo() {
      return no;
    }

    /** @return txt 表示名称 */
    public String getTxt() {
      return txt;
    }

    /** @return tbl 参照テーブル */
    public RefTable getTbl() {
      return tbl;
    }

    /** @return tbl 参照列 */
    public String getCol() {
      return col;
    }
  }

  /** メーカーマスタレイアウト */
  public enum MSTMAKERLayout implements MSTLayout {
    /** メーカーコード */
    MAKERCD(1, "標準メーカコード", "INTEGER"),
    /** メーカー名（カナ） */
    MAKERAN(2, "会社名カナ", "VARCHAR(20)"),
    /** メーカー名（漢字） */
    MAKERKN(3, "会社名", "VARCHAR(40)"),
    /** JANコード */
    JANCD(4, "JANコード", "CHARACTER(14)"),
    /** 代表メーカーコード */
    DMAKERCD(5, "代表メーカーコード", "INTEGER"),
    /** 更新区分 */
    UPDKBN(6, "更新区分", "SMALLINT");

    private final Integer no;
    private final String col;
    private final String typ;

    /** 初期化 */
    private MSTMAKERLayout(Integer no, String col, String typ) {
      this.no = no;
      this.col = col;
      this.typ = typ;
    }

    /** @return col 列番号 */
    @Override
    public Integer getNo() {
      return no;
    }

    /** @return tbl 列名 */
    @Override
    public String getCol() {
      return col;
    }

    /** @return tbl 列型 */
    @Override
    public String getTyp() {
      return typ;
    }

    /** @return col Id */
    @Override
    public String getId() {
      return "F" + Integer.toString(no);
    }

    /** @return datatype データ型のみ */
    @Override
    public DataType getDataType() {
      if (typ.indexOf("INT") != -1) {
        return DefineReport.DataType.INTEGER;
      }
      if (typ.indexOf("DEC") != -1) {
        return DefineReport.DataType.DECIMAL;
      }
      if (typ.indexOf("DATE") != -1) {
        return DefineReport.DataType.DATE;
      }
      return DefineReport.DataType.TEXT;
    }

    /** @return boolean */
    @Override
    public boolean isText() {
      return getDataType() == DefineReport.DataType.TEXT;
    }

    /** @return digit 桁数 */
    public int[] getDigit() {
      int digit1 = 0, digit2 = 0;
      if (typ.indexOf(",") > 0) {
        String[] sDigit = typ.substring(typ.indexOf("(") + 1, typ.indexOf(")")).split(",");
        digit1 = NumberUtils.toInt(sDigit[0]);
        digit2 = NumberUtils.toInt(sDigit[1]);
      } else if (typ.indexOf("(") > 0) {
        digit1 = NumberUtils.toInt(typ.substring(typ.indexOf("(") + 1, typ.indexOf(")")));
      } else if (StringUtils.equals(typ, JDBCType.SMALLINT.getName())) {
        digit1 = dbNumericTypeInfo.SMALLINT.getDigit();
      } else if (StringUtils.equals(typ, JDBCType.INTEGER.getName())) {
        digit1 = dbNumericTypeInfo.INTEGER.getDigit();
      }
      return new int[] {digit1, digit2};
    }
  }

  /** JICFSメーカーマスタレイアウト */
  public enum JICFS_MSTMAKERLayout implements MSTLayout {
    /** 更新区分 */
    UPDKBN(1, "更新区分", "VARCHAR(1)"),
    /** 標準メーカーコード */
    MAKERCD(2, "標準メーカコード", "INTEGER"),
    /** 代表メーカーコード */
    DMAKERCD(3, "代表メーカコード", "INTEGER"),
    /** メーカー名（漢字） */
    MAKERKN(4, "会社名", "VARCHAR(60)"),
    /** メーカー名（カナ） */
    MAKERAN(5, "会社名カナ", "VARCHAR(40)"),
    /** 法人形態（位置） */
    HOUJINKEITAIICHI(6, "法人形態（位置）", "VARCHAR(1)"),
    /** 法人形態（文字） */
    HOUJINKEITAIMOJI(7, "法人形態（文字）", "VARCHAR(2)"),
    /** 住所1 */
    JUSYO1(8, "住所１", "VARCHAR(20)"),
    /** 廃止年月日 */
    DELDT(9, "廃止年月日", "VARCHAR(8)");


    private final Integer no;
    private final String col;
    private final String typ;

    /** 初期化 */
    private JICFS_MSTMAKERLayout(Integer no, String col, String typ) {
      this.no = no;
      this.col = col;
      this.typ = typ;
    }

    /** @return col 列番号 */
    @Override
    public Integer getNo() {
      return no;
    }

    /** @return tbl 列名 */
    @Override
    public String getCol() {
      return col;
    }

    /** @return tbl 列型 */
    @Override
    public String getTyp() {
      return typ;
    }

    /** @return col Id */
    @Override
    public String getId() {
      return "F" + Integer.toString(no);
    }

    /** @return datatype データ型のみ */
    @Override
    public DataType getDataType() {
      if (typ.indexOf("INT") != -1) {
        return DefineReport.DataType.INTEGER;
      }
      if (typ.indexOf("DEC") != -1) {
        return DefineReport.DataType.DECIMAL;
      }
      if (typ.indexOf("DATE") != -1) {
        return DefineReport.DataType.DATE;
      }
      return DefineReport.DataType.TEXT;
    }

    /** @return boolean */
    @Override
    public boolean isText() {
      return getDataType() == DefineReport.DataType.TEXT;
    }

    /** @return digit 桁数 */
    public int[] getDigit() {
      int digit1 = 0, digit2 = 0;
      if (typ.indexOf(",") > 0) {
        String[] sDigit = typ.substring(typ.indexOf("(") + 1, typ.indexOf(")")).split(",");
        digit1 = NumberUtils.toInt(sDigit[0]);
        digit2 = NumberUtils.toInt(sDigit[1]);
      } else if (typ.indexOf("(") > 0) {
        digit1 = NumberUtils.toInt(typ.substring(typ.indexOf("(") + 1, typ.indexOf(")")));
      } else if (StringUtils.equals(typ, JDBCType.SMALLINT.getName())) {
        digit1 = dbNumericTypeInfo.SMALLINT.getDigit();
      } else if (StringUtils.equals(typ, JDBCType.INTEGER.getName())) {
        digit1 = dbNumericTypeInfo.INTEGER.getDigit();
      }
      return new int[] {digit1, digit2};
    }
  }

  private enum dbNumericTypeInfo {
    /** SMALLINT */
    SMALLINT(5, -32768, 32768),
    /** INT */
    INT(10, -2147483648, 2147483648l),
    /** INTEGER */
    INTEGER(10, -2147483648, 2147483648l);

    private final int digit;

    /** 初期化 */
    private dbNumericTypeInfo(int digit, long min, long max) {
      this.digit = digit;
    }

    /** @return digit */
    public int getDigit() {
      return digit;
    }
  }

  private void outputQueryList() {

    // 検索条件の加工クラス作成
    JsonArrayData jad = new JsonArrayData();
    jad.setJsonString(getJson());

  }
}
