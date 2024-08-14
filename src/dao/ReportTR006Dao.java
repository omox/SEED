/**
 *
 */
package dao;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.JDBCType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import authentication.bean.User;
import authentication.defines.Consts;
import common.CmnDate;
import common.DefineReport;
import common.DefineReport.DataType;
import common.Defines;
import common.FileList;
import common.ItemList;
import common.MessageUtility;
import common.MessageUtility.Msg;
import common.MessageUtility.MsgKey;
import dao.ReportBM006Dao.CSVTOKLayout;
import dao.ReportTR005Dao.HATSTRLayout;
import dto.JQEasyModel;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 */
public class ReportTR006Dao extends ItemDao {

  boolean isTest = true;


  /** 最大処理件数 */
  public static int MAX_ROW = 10000;

  /**
   * インスタンスを生成します。
   *
   * @param source
   */
  public ReportTR006Dao(String JNDIname) {
    super(JNDIname);
  }

  /**
   * ファイルアップロード
   *
   * @param userInfo
   *
   * @return
   * @throws IOException
   * @throws FileNotFoundException
   * @throws Exception
   */
  public JQEasyModel upload(HttpSession session, User userInfo, HashMap<String, String> map, String file) {
    // jqEasy 用 JSON モデル作成
    JQEasyModel json = new JQEasyModel();
    JSONObject option = new JSONObject();

    session.getAttribute(Consts.STR_SES_LOGINDT);
    String strMsg = null;
    map.get(DefineReport.InpText.COMMENTKN.getObj());

    // メッセージ情報取得
    MessageUtility mu = new MessageUtility();
    JSONArray errMsgList = new JSONArray();

    try {

      // ユーザー情報設定
      setUserInfo(userInfo);

      // *** 情報取得 ***
      FileList fl = new FileList();
      ArrayList<Object[]> dL = null;
      // ファイル読み込み
      dL = fl.readCsv(session, file);

      // *** 情報チェック ***
      int idxHeader = 0;
      // ファイルレイアウトチェック
      errMsgList = this.checkFileLayout(dL, idxHeader);

      // CVSトランSEQ情報
      // String seq = this.getCSVTOK_SEQ();

      new ArrayList<JSONObject>();
      // CSVを分解、画面上と同じように登録用のデータの形にする
      JSONArray astr = new JSONArray(), ajtr = new JSONArray(), attr = new JSONArray();
      JSONObject ostr = new JSONObject(), ojtr = new JSONObject(), ottr = new JSONObject(), ooth = new JSONObject();

      // ファイル内レコード件数
      int cnt = 0;

      // 重複確認用
      Map<String, String> tenShnCds = new HashMap<String, String>();

      // 対象外の店・商品・行を入れたい
      for (int i = idxHeader; i < dL.size(); i++) {
        Object[] data = dL.get(i);

        // 0:正規 1:次週
        String tenCd = StringUtils.trim(String.valueOf(data[FileLayout.TENCD.getNo() - 1])); // 店コード
        String shnCd = StringUtils.trim(String.valueOf(data[FileLayout.SHNCD.getNo() - 1])); // 商品コード
        String shuNo = StringUtils.trim(String.valueOf(data[FileLayout.SHUNO.getNo() - 1])); // 週No
        String jiseiKbn = StringUtils.trim(String.valueOf(data[FileLayout.JISEIKBN.getNo() - 1])); // 次正区分

        String key = tenCd + shnCd + shuNo + jiseiKbn;

        if (StringUtils.isEmpty(tenShnCds.get(key))) {
          tenShnCds.put(key, String.valueOf(i + 1));
        } else {
          tenShnCds.replace(key, String.valueOf(i + 1));
        }
      }

      if (errMsgList.size() == 0) {

        // データ加工
        for (int i = idxHeader; i < dL.size(); i++) {
          Object[] data = dL.get(i);
          // 0:正規 1:次週
          String jiseiKbn = StringUtils.trim((String) data[FileLayout.JISEIKBN.getNo() - 1]); // 次正区分

          // キー重複チェック
          String tenCd = StringUtils.trim(String.valueOf(data[FileLayout.TENCD.getNo() - 1])); // 店コード
          String shnCd = StringUtils.trim(String.valueOf(data[FileLayout.SHNCD.getNo() - 1])); // 商品コード
          String shuNo = StringUtils.trim(String.valueOf(data[FileLayout.SHUNO.getNo() - 1])); // 週No

          // 更新対象のレコードか判断
          if (!tenShnCds.get(tenCd + shnCd + shuNo + jiseiKbn).equals(String.valueOf(i + 1))) {
            continue;
          }

          // 上のチェックを通過したらレコード件数をカウントアップ
          cnt++;

          boolean jiseiErr = false; // 数値チェック用：次正区分

          for (FileLayout itm : FileLayout.values()) {
            String val = StringUtils.trim((String) data[itm.getNo() - 1]);

            if (itm.getTbl() == RefTable.HATSTR) {

              // 正規の場合
              if (jiseiKbn.equals("0")) {
                if (ostr.containsKey(itm.getCol())) {
                  astr.add(ostr);
                  ostr = new JSONObject();
                }
                if (itm.getCol().equals(HATSTRLayout.SHNCD.getCol())) {
                  ostr.put(itm.getCol(), val);
                  ostr.put(HATSTRLayout.BINKBN.getCol(), "1");
                } else if (itm.getCol().equals(HATSTRLayout.SURYO_SUN.getCol())) {
                  ostr.put(itm.getCol(), val);
                  ostr.put(HATSTRLayout.GYONO.getCol(), i + 1);
                } else {
                  ostr.put(itm.getCol(), val);
                }

                // 次週の場合
              } else {
                if (!jiseiKbn.equals("1")) {
                  // 0,1以外の数値が入力されていた場合、エラーとする。
                  jiseiErr = true;
                }

                if (ojtr.containsKey(itm.getCol())) {
                  ajtr.add(ojtr);
                  ojtr = new JSONObject();
                }
                if (itm.getCol().equals(HATSTRLayout.SHNCD.getCol())) {
                  ojtr.put(itm.getCol(), val);
                  ojtr.put(HATSTRLayout.BINKBN.getCol(), "1");
                } else if (itm.getCol().equals(HATSTRLayout.SURYO_SUN.getCol())) {
                  ojtr.put(itm.getCol(), val);
                  ojtr.put(HATSTRLayout.GYONO.getCol(), i + 1);
                } else {
                  ojtr.put(itm.getCol(), val);
                }
              }


              // 分解せずに、両方のデータを保持する(店別数量_CSV登録用)
              if (ottr.containsKey(itm.getCol())) {
                attr.add(ottr);
                ottr = new JSONObject();
              }

              if (itm.getCol().equals(HATSTRLayout.SHNCD.getCol())) {
                ottr.put(itm.getCol(), val);
                ottr.put(HATSTRLayout.BINKBN.getCol(), "1");
              } else if (itm.getCol().equals(HATSTRLayout.SURYO_SUN.getCol())) {
                ottr.put(itm.getCol(), val);
                ottr.put(HATSTRLayout.GYONO.getCol(), i + 1);

              } else if (itm.getCol().equals(HATSTRLayout.SHUNO.getCol())) {
                if (jiseiKbn.equals("0")) {
                  // 正規の場合は週Noは"0"で登録を行う
                  val = "0";
                }
                ottr.put(itm.getCol(), val);
              } else {
                ottr.put(itm.getCol(), val);
              }

            } else if (itm.getTbl() == RefTable.OTHER) {
              ooth.put(itm.getCol(), val);
            }
          }

          if (jiseiErr && errMsgList.size() == 0) {
            // 次正区分に、1，0以外の数値が入力された場合、エラーとする。
            JSONObject o = MessageUtility.getDbMessageIdObj("E11302", new String[] {FileLayout.JISEIKBN.getTxt()});
            errMsgList.add(o);
          }

          astr.add(ostr); // チェック用データ：正規
          ajtr.add(ojtr); // チェック用データ：次週
          attr.add(ottr); // 登録用データ：店別数量_CSV
          ostr = new JSONObject();
          ojtr = new JSONObject();
          ottr = new JSONObject();
        }
      }

      int errCount = 0;

      // *** 詳細情報チェック＋情報登録用SQL作成 ***
      if (errMsgList.size() == 0) {

        userInfo.getCD_user();

        // ヘッダ登録用SQL作成
        ReportTR005Dao dao = new ReportTR005Dao(super.JNDIname);

        // 詳細情報チェック
        List<JSONObject> msgList = new ArrayList<JSONObject>() {};
        msgList = dao.checkData(mu, this.selectHATSTR(astr, true), // 正規
            this.selectHATSTR(ajtr, true), // 次週
            ooth // その他
        );

        boolean isError = msgList.size() != 0;

        // CSV用情報セット
        /*
         * String csvUpdkbn = DefineReport.ValCsvUpdkbn.NEW.getVal();
         * dao.csvtok_add_data[CSVTOKLayout.SEQ.getNo()-1] = ooth.optString(CSVTOKLayout.SEQ.getCol()); //
         * CSV用.SEQ dao.csvtok_add_data[CSVTOKLayout.INPUTNO.getNo()-1] =
         * ooth.optString(CSVTOKLayout.INPUTNO.getCol()); // CSV用.入力番号
         * dao.csvtok_add_data[CSVTOKLayout.CSV_UPDKBN.getNo()-1] = csvUpdkbn; // CSV_UPDKBN
         */
        if (isError) {
          /*
           * dao.csvtok_add_data[CSVTOKLayout.ERRCD.getNo()-1] =
           * msgList.get(0).optString(CSVTOKLayout.ERRCD.getCol()); // ERRCD
           * dao.csvtok_add_data[CSVTOKLayout.ERRFLD.getNo()-1] =
           * msgList.get(0).optString(CSVTOKLayout.ERRFLD.getCol()); // ERRFLD
           * dao.csvtok_add_data[CSVTOKLayout.ERRVL.getNo()-1] =
           * msgList.get(0).optString(CSVTOKLayout.ERRVL.getCol()); // ERRVL
           * dao.csvtok_add_data[CSVTOKLayout.ERRTBLNM.getNo()-1] =
           * msgList.get(0).optString(CSVTOKLayout.ERRTBLNM.getCol()); // ERRTBLNM
           * dao.csvtok_add_data[CSVTOKLayout.INPUTNO.getNo()-1] =
           * msgList.get(0).optString(CSVTOKLayout.INPUTNO.getCol()); // ERRTBLNM
           *
           * dao.csvtok_add_data[CSVTOKLayout.MOYSKBN.getNo()-1] =
           * msgList.get(0).optString(CSVTOKLayout.MOYSKBN.getCol()); // MOYSKBN
           * dao.csvtok_add_data[CSVTOKLayout.MOYSSTDT.getNo()-1] =
           * msgList.get(0).optString(CSVTOKLayout.MOYSSTDT.getCol()); // MOYSSTDT
           * dao.csvtok_add_data[CSVTOKLayout.MOYSRBAN.getNo()-1] =
           * msgList.get(0).optString(CSVTOKLayout.MOYSRBAN.getCol()); // MOYSRBAN
           * dao.csvtok_add_data[CSVTOKLayout.BMNNO.getNo()-1] =
           * msgList.get(0).optString(CSVTOKLayout.BMNNO.getCol()); // BMNNO
           * dao.csvtok_add_data[CSVTOKLayout.BMNMAN.getNo()-1] =
           * msgList.get(0).optString(CSVTOKLayout.BMNMAN.getCol()); // BMNMAN
           * dao.csvtok_add_data[CSVTOKLayout.HBSTDT.getNo()-1] =
           * msgList.get(0).optString(CSVTOKLayout.HBSTDT.getCol()); // HBSTDT
           * dao.csvtok_add_data[CSVTOKLayout.HBEDDT.getNo()-1] =
           * msgList.get(0).optString(CSVTOKLayout.HBEDDT.getCol()); // HBEDDT
           * dao.csvtok_add_data[CSVTOKLayout.SHNCD.getNo()-1] =
           * msgList.get(0).optString(CSVTOKLayout.SHNCD.getCol()); // SHNCD
           */
          errCount++;
          this.setMessage(msgList.toString());

          // CSV取込トラン_催し別送信情報更新
          // JSONObject bmrtn = this.createSqlCSVTOKBM(userId, sysdate, dao);
        }
      }

      // エラーが一件も存在しない場合更新処理を実行
      if (errMsgList.size() == 0 && errCount == 0) {

        // 最新の情報取得
        // データ取得先判断
        // ReportTR005Dao dao = new ReportTR005Dao(super.JNDIname);
        ReportTR007Dao dao = new ReportTR007Dao(super.JNDIname);

        HashMap<String, String> sendmap = new HashMap<String, String>();

        // this.selectHATSTR(astr,false) // 正規
        // this.selectHATSTR(ajtr,false) // 次週

        sendmap.put("DATA_HATSTR", this.selectHATTTR(attr, true).toString());
        this.createCommandUpdateData(dao, userInfo, sendmap);

        this.sqlList.addAll(dao.sqlList);
        this.prmList.addAll(dao.prmList);
        this.lblList.addAll(dao.lblList);
      }

      // 更新処理
      try {
        option = this.updateData();
      } catch (Exception e) {
        e.printStackTrace();
        errMsgList.add(MessageUtility.getDbMessageIdObj("E30007", new String[] {}));
      }

      // 実行結果のメッセージを取得
      strMsg = this.getMessage();
      if (errMsgList.size() == 0) {
        // 実行トラン情報をJSに戻す
        option.put(DefineReport.Text.SEQ.getObj(), "");
        option.put(DefineReport.Text.STATUS.getObj(), "完了");
        option.put(DefineReport.Text.UPD_NUMBER.getObj(), cnt);
        option.put(DefineReport.Text.ERR_NUMBER.getObj(), errCount);
      }

    } catch (FileNotFoundException e) {
      e.printStackTrace();
      errMsgList.add(MessageUtility.getDbMessageIdObj("E20251", new String[] {}));
    } catch (Exception e) {
      e.printStackTrace();
      errMsgList.add(MessageUtility.getDbMessageIdObj("E00014", new String[] {}));
    }
    if (errMsgList.size() != 0) {
      option.put(DefineReport.Text.STATUS.getObj(), "失敗");

      option.put(MsgKey.E.getKey(), errMsgList.get(0));
    }
    // 実行結果のメッセージを設定
    json.setOpts(option);
    json.setMessage(strMsg);
    return json;
  }

  /*
   * private List<JSONObject> checkData(ReportBM006Dao dao, HashMap<String, String> map, User
   * userInfo, String sysdate1, MessageUtility mu, JSONArray dataArray, // BM催し送信 JSONArray
   * dataArraySHN, // BM催し送信_商品 JSONArray dataArrayTJ, // BM催し送信_対象除外店 JSONObject dataOther // その他情報 )
   * { JSONArray msg = new JSONArray();
   *
   * // DB最新情報再取得 JSONObject seiJsonObject = new JSONObject();
   *
   * String ErrTblNm = "CSVファイル";
   *
   * JSONObject data = dataArray.optJSONObject(0);
   *
   * return msg; }
   */

  private JSONArray checkFileLayout(ArrayList<Object[]> eL, int idxHeader) {

    JSONArray msg = new JSONArray();

    // 1.件数チェック
    if (eL.size() <= idxHeader) {
      // return "ファイルにデータがありません。";
      JSONObject o = MessageUtility.getDbMessageIdObj("E40001", new String[] {});
      msg.add(o);
    } else if (eL.size() > MAX_ROW + idxHeader) {
      JSONObject o = MessageUtility.getDbMessageIdObj("E11234", new String[] {});
      msg.add(o);
      return msg;
    }
    // // 2.ヘッダーチェック
    // Object[] inpHeader = eL.get(idxHeader - 1);
    // for(FileLayout item : FileLayout.values()){
    // if(item.getNo()-1 < inpHeader.length){
    // if(!StringUtils.equals(item.getTxt(), (String) inpHeader[item.getNo()-1])){
    // return "\nファイルの列情報が異なります。正しいCSVファイルかどうか確認してください。";
    // }
    // }
    // }
    // 3.項目数チェック
    if (FileLayout.values().length != eL.get(idxHeader).length) {
      JSONObject o = MessageUtility.getDbMessageIdObj("E40008", new String[] {});
      msg.add(o);
      return msg;
    }
    return msg;
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

  /**
   * 更新処理実行
   *
   * @param updCount
   * @return
   *
   * @throws Exception
   */
  /*
   * private JSONObject createCommandUpdateData( ReportTR005Dao dao, User userInfo, JSONArray
   * dataArray, // 正規 JSONArray dataArrayJ // 次週 ) throws Exception {
   *
   * JSONObject option = new JSONObject();
   *
   * // パラメータ確認 JSONObject result = dao.createSqlHatstrCsv(dataArray, dataArrayJ, userInfo);
   *
   * return option; }
   */
  private JSONObject createCommandUpdateData(ReportTR007Dao dao, User userInfo, HashMap<String, String> map) throws Exception {

    JSONObject option = new JSONObject();

    // 登録処理実行
    dao.createSqlHatTr_CSV(map, userInfo);

    return option;
  }

  /**
   * CSV取込トラン_特売ヘッダINSERT処理
   *
   * @param userId
   * @param data
   * @param tbl
   *
   * @throws Exception
   */
  public JSONObject createSqlCSVTOKHEAD(int userId, String sysdate, String seq, String commentkn) {
    JSONObject result = new JSONObject();
    CmnDate.dbDateFormat(sysdate);
    // 更新情報
    ArrayList<String> prmData = new ArrayList<String>();

    LinkedList<String> valList = new LinkedList<String>();
    valList.add(""); //

    String values = "";
    values += " " + seq;
    values += "," + userId;
    values += ",CURRENT_TIMESTAMP ";
    if (isTest) {
      values += ",'" + commentkn + "'";
    } else {
      values += ",?";
      prmData.add(commentkn);
    }

    StringBuffer sbSQL;
    sbSQL = new StringBuffer();
    sbSQL.append("insert into INATK.CSVTOKHEAD(SEQ, OPERATOR, INPUT_DATE, COMMENTKN)");
    sbSQL.append("values(" + values + ")");
    if (DefineReport.ID_DEBUG_MODE) {
      System.out.println("/*" + this.getClass().getName() + "*/ " + sbSQL.toString());
    }

    this.sqlList.add(sbSQL.toString());
    this.prmList.add(prmData);
    this.lblList.add("CSV取込トラン_特売ヘッダ");
    return result;
  }

  /**
   * CSV取込トラン_BM催し送信INSERT処理
   *
   * @param userId
   * @param data
   * @param tbl
   *
   * @throws Exception
   */
  public JSONObject createSqlCSVTOKBM(int userId, String sysdate, ReportBM006Dao dao) {
    JSONObject result = new JSONObject();
    String dbsysdate = CmnDate.dbDateFormat(sysdate);
    // 更新情報
    ArrayList<String> prmData = new ArrayList<String>();

    LinkedList<String> valList = new LinkedList<String>();
    valList.add(""); //

    String values = "";
    for (CSVTOKLayout itm : CSVTOKLayout.values()) {
      String val = (dao.csvtok_add_data[itm.getNo() - 1]);

      if (StringUtils.isEmpty(val)) {
        values += " null,";
      } else {
        values += " ?,";
        prmData.add(val);
      }
    }
    values += "?,?";
    prmData.add(String.valueOf(userId));
    prmData.add(dbsysdate);

    StringBuffer sbSQL;
    sbSQL = new StringBuffer();
    sbSQL.append("insert into INATK.CSVTOK_BM(");
    sbSQL.append("SEQ,");
    sbSQL.append("INPUTNO,");
    sbSQL.append("ERRCD,");
    sbSQL.append("ERRFLD,");
    sbSQL.append("ERRVL,");
    sbSQL.append("ERRTBLNM,");
    sbSQL.append("CSV_UPDKBN,");
    sbSQL.append("MOYSKBN,");
    sbSQL.append("MOYSSTDT,");
    sbSQL.append("MOYSRBAN,");
    sbSQL.append("BMNNO,");
    sbSQL.append("BMNMAN,");
    sbSQL.append("HBSTDT,");
    sbSQL.append("HBEDDT,");
    sbSQL.append("SHNCD,");
    sbSQL.append("OPERATOR,");
    sbSQL.append("ADDDT)");
    sbSQL.append("values(" + values + ")");
    if (DefineReport.ID_DEBUG_MODE) {
      System.out.println("/*" + this.getClass().getName() + "*/ " + sbSQL.toString());
    }

    this.sqlList.add(sbSQL.toString());
    this.prmList.add(prmData);
    this.lblList.add("CSV取込トラン_BM催し送信");
    return result;
  }

  /**
   * 子テーブルマスタ共通SQL作成処理
   *
   * @param dataArray CSV抜粋データ
   * @param layout マスタレイアウト
   * @return
   *
   * @throws Exception
   */
  private String setSelectCommandMST(JSONArray dataArray, MSTLayout[] layouts, ArrayList<String> prmData) {
    String values = "", names = "", rows = "";
    for (int j = 0; j < dataArray.size(); j++) {
      values = "";
      names = "";
      for (MSTLayout itm : layouts) {
        String col = itm.getCol();
        String val = dataArray.optJSONObject(j).optString(col);

        if (StringUtils.isEmpty(val)) {
          values += ", null";
        } else {
          if (isTest) {
            values += ", '" + val + "'";
          } else {
            prmData.add(val);
            values += ", ?";
          }
        }
        names += ", " + col;
      }
      rows += ",(" + StringUtils.removeStart(values, ",") + ")";
    }
    rows = StringUtils.removeStart(rows, ",");
    names = StringUtils.removeStart(names, ",");

    // 共通SQL
    StringBuffer sbSQLIn = new StringBuffer();
    sbSQLIn.append(" select ");
    for (MSTLayout itm : layouts) {
      if (itm.getNo() > 1) {
        sbSQLIn.append(",");
      }
      if (itm.isText()) {
        sbSQLIn.append(itm.getCol() + " as " + itm.getCol());
      } else {
        sbSQLIn.append("cast(" + itm.getCol() + " as " + itm.getTyp() + ") as " + itm.getCol());
      }
    }
    sbSQLIn.append(" from (values" + rows + ") as T1(" + names + ")");

    // 基本Select文
    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append(" select ");
    for (MSTLayout itm : layouts) {
      if (itm.getNo() > 1) {
        sbSQL.append(",");
      }
      sbSQL.append("RE." + itm.getCol() + " as " + itm.getId());
    }
    sbSQL.append(" from (" + sbSQLIn.toString() + ") RE");

    return sbSQL.toString();
  }

  /**
   * 子テーブルマスタ共通SQL作成処理
   *
   * @param dataArray CSV抜粋データ
   * @param layout マスタレイアウト
   * @return
   *
   * @throws Exception
   */
  private String checkSelectCommandMST(JSONArray dataArray, MSTLayout[] layouts, ArrayList<String> prmData) {
    String values = "", names = "", rows = "";
    for (int j = 0; j < dataArray.size(); j++) {
      values = "";
      names = "";
      for (MSTLayout itm : layouts) {
        String col = itm.getCol();
        String val = dataArray.optJSONObject(j).optString(col);

        if (StringUtils.isEmpty(val)) {
          values += ", null";
        } else {
          if (isTest) {
            values += ", '" + val + "'";
          } else {
            prmData.add(val);
            values += ", ?";
          }
        }
        names += ", " + col;
      }
      rows += ",ROW(" + StringUtils.removeStart(values, ",") + ")";
    }
    rows = StringUtils.removeStart(rows, ",");
    names = StringUtils.removeStart(names, ",");

    // 共通SQL
    StringBuffer sbSQLIn = new StringBuffer();
    sbSQLIn.append(" select ");
    for (MSTLayout itm : layouts) {
      if (itm.getNo() > 1) {
        sbSQLIn.append(",");
      }
      sbSQLIn.append(itm.getCol() + " as " + itm.getCol());
    }
    sbSQLIn.append(" from (values " + rows + ") as T1(" + names + ")");

    // 基本Select文
    StringBuffer sbSQL = new StringBuffer();
    sbSQL.append(" select ");
    for (MSTLayout itm : layouts) {
      if (itm.getNo() > 1) {
        sbSQL.append(",");
      }
      sbSQL.append("RE." + itm.getCol() + " as " + itm.getId());
    }
    sbSQL.append(" from (" + sbSQLIn.toString() + ") RE");

    return sbSQL.toString();
  }

  /**
   * 正規定量_店舗別数量
   *
   * @param isSei 正/予約
   * @param isNew 新規/更新
   * @param data CSV抜粋データ
   *
   * @throws Exception
   */
  private JSONArray selectHATSTR(JSONArray dataArray, boolean checkFlg) {
    ArrayList<String> prmData = new ArrayList<>();
    String sqlCommand = "";
    if (checkFlg) {
      sqlCommand = this.checkSelectCommandMST(dataArray, HATSTRLayout.values(), prmData);
    } else {
      sqlCommand = this.setSelectCommandMST(dataArray, HATSTRLayout.values(), prmData);
    }
    if (DefineReport.ID_DEBUG_MODE) {
      System.out.println("/*" + this.getClass().getName() + "*/" + sqlCommand);
    }

    ItemList iL = new ItemList();
    @SuppressWarnings("static-access")
    JSONArray rtnArray = iL.selectJSONArray(sqlCommand, prmData, Defines.STR_JNDI_DS);
    return rtnArray;
  }

  /**
   * 店舗別数量_CSV
   *
   * @param isSei 正/予約
   * @param isNew 新規/更新
   * @param data CSV抜粋データ
   *
   * @throws Exception
   */
  private JSONArray selectHATTTR(JSONArray dataArray, boolean checkFlg) {
    ArrayList<String> prmData = new ArrayList<>();
    String sqlCommand = "";
    if (checkFlg) {
      sqlCommand = this.checkSelectCommandMST(dataArray, HATTTRLayout.values(), prmData);
    } else {
      sqlCommand = this.setSelectCommandMST(dataArray, HATTTRLayout.values(), prmData);
    }
    if (DefineReport.ID_DEBUG_MODE) {
      System.out.println("/*" + this.getClass().getName() + "*/ " + sqlCommand);
    }

    ItemList iL = new ItemList();
    @SuppressWarnings("static-access")
    JSONArray rtnArray = iL.selectJSONArray(sqlCommand, prmData, Defines.STR_JNDI_DS);
    return rtnArray;
  }

  /**
   * SEQ情報取得処理
   *
   * @throws Exception
   */
  public String getCSVTOK_SEQ() {
    // 関連情報取得
    ItemList iL = new ItemList();
    String sqlColCommand = "SELECT INAMS.nextval('SEQ003') AS \"1\"";
    @SuppressWarnings("static-access")
    JSONArray array = iL.selectJSONArray(sqlColCommand, null, Defines.STR_JNDI_DS);
    String value = "";
    if (array.size() > 0) {
      value = array.optJSONObject(0).optString("1");
    }
    return value;
  }

  /** File出力項目の参照テーブル */
  public enum RefTable {

    /** 正規定量_店別数量(次週定量を含む) */
    HATSTR(1, "正規定量_店別数量"),
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


  /** Fileレイアウト */
  public enum FileLayout {
    /** 更新区分 */
    TENCD(1, "店舗", RefTable.HATSTR, "TENCD"), SHNCD(2, "商品コード", RefTable.HATSTR, "SHNCD"), SHNAN(3, "商品名（ｶﾅ）", RefTable.OTHER, "SHNAN"), JISEIKBN(4, "次正区分", RefTable.HATSTR, "JISEIKBN"), SURYO_MON(5,
        "数量_月", RefTable.HATSTR, "SURYO_MON"), SURYO_TUE(6, "数量_火", RefTable.HATSTR, "SURYO_TUE"), SURYO_WED(7, "数量_水", RefTable.HATSTR, "SURYO_WED"), SURYO_THU(8, "数量_木", RefTable.HATSTR,
            "SURYO_THU"), SURYO_FRI(9, "数量_金", RefTable.HATSTR, "SURYO_FRI"), SURYO_SAT(10, "数量_土", RefTable.HATSTR, "SURYO_SAT"), SURYO_SUN(11, "数量_日", RefTable.HATSTR, "SURYO_SUN"), SHUNO(12,
                "週No.", RefTable.HATSTR, "SHUNO"), OPERATOR(13, "オペレータ", RefTable.OTHER, "OPERATOR"), ADDDT(14, "登録日", RefTable.OTHER, "ADDDT"), UPDDT(15, "更新日", RefTable.OTHER, "UPDDT");

    private final Integer no;
    private final String txt;
    private final RefTable tbl;
    private final String col;

    /** 初期化 */
    private FileLayout(Integer no, String txt, RefTable tbl, String col) {
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

  /** 店別数量_CSV(次週も含む) */
  public enum HATTTRLayout implements MSTLayout {

    // TR007の登録処理時の送信データに合わせたレイアウトです。
    /** 商品コード */
    UPDKBN(1, "UPDKBN", "SMALLINT", "更新区分"),
    /** 週No. */
    JISEIKBN(2, "JISEIKBN", "CHARACTER(1)", "次正区分"),
    /** 週No. */
    SHUNO(3, "SHUNO", "SMALLINT", "週No"),
    /** 商品コード */
    SHNCD(4, "SHNCD", "CHARACTER(14)", "商品コード"),
    /** 店 */
    TENCD(5, "TENCD", "SMALLINT", "店コード"),
    /** 数量_月 */
    SURYO_MON(6, "SURYO_MON", "INTEGER", "数量_月"),
    /** 数量_火 */
    SURYO_TUE(7, "SURYO_TUE", "INTEGER", "数量_火"),
    /** 数量_水 */
    SURYO_WED(8, "SURYO_WED", "INTEGER", "数量_水"),
    /** 数量_木 */
    SURYO_THU(9, "SURYO_THU", "INTEGER", "数量_木"),
    /** 数量_金 */
    SURYO_FRI(10, "SURYO_FRI", "INTEGER", "数量_金"),
    /** 数量_土 */
    SURYO_SAT(11, "SURYO_SAT", "INTEGER", "数量_土"),
    /** 数量_日 */
    SURYO_SUN(12, "SURYO_SUN", "INTEGER", "数量_日");

    private final Integer no;
    private final String col;
    private final String typ;
    private final String text;

    /** 初期化 */
    private HATTTRLayout(Integer no, String col, String typ, String text) {
      this.no = no;
      this.col = col;
      this.typ = typ;
      this.text = text;
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

    /** @return tbl 列名(論理名称) */
    public String getText() {
      return text;
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
}
