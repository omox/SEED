package servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang.StringUtils;
import authentication.bean.User;
import authentication.defines.Consts;
import common.ChkUsableTime;
import common.DefineReport;
import common.Defines;
import common.GetSqlCommandChange;
import common.GetSqlCommandCheck;
import common.GetSqlCommandInit;
import common.ItemList;
import common.TenpoList;
import dao.DBConnection;
import dto.JQEasyModel;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

/**
 * Servlet implementation class EasyToJSONServlet
 */
public class EasyToJSONServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;


  /**
   * @see HttpServlet#HttpServlet()
   */
  public EasyToJSONServlet() {
    super();
  }


  /**
   * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    // 文字変換コード設定【重要】
    request.setCharacterEncoding("UTF-8");

    // パラメータ一覧【確認】
    HashMap<String, String> maps = new HashMap<String, String>();
    Enumeration<String> enums = request.getParameterNames();
    while (enums.hasMoreElements()) {
      String name = enums.nextElement();
      maps.put(name, request.getParameter(name));
    }

    if (maps.get(DefineReport.ID_PARAM_PAGE) == null) {
    } else {
      maps.get(DefineReport.ID_PARAM_PAGE);
    }
    String outobj = maps.get(DefineReport.ID_PARAM_OBJ) == null ? "" : maps.get(DefineReport.ID_PARAM_OBJ);
    if (maps.get(DefineReport.ID_PARAM_VAL) == null) {
    } else {
      maps.get(DefineReport.ID_PARAM_VAL);
    }
    String outjson = maps.get(DefineReport.ID_PARAM_JSON) == null ? "" : maps.get(DefineReport.ID_PARAM_JSON);
    String outaction = maps.get(DefineReport.ID_PARAM_ACTION) == null ? DefineReport.ID_PARAM_ACTION_DEFAULT : maps.get(DefineReport.ID_PARAM_ACTION);
    String outtarget = maps.get(DefineReport.ID_PARAM_TARGET) == null ? "" : maps.get(DefineReport.ID_PARAM_TARGET);
    String datatype = maps.get(DefineReport.ID_PARAM_TYPE) == null ? "" : maps.get(DefineReport.ID_PARAM_TYPE);

    if (maps.get(DefineReport.ID_SEARCHJSON_PARAM_MAXROW) == null) {
    } else {
      maps.get(DefineReport.ID_SEARCHJSON_PARAM_MAXROW);
    }

    // 検索キー
    String nameWith = maps.get(DefineReport.ID_SEARCHJSON_PARAM_NAMEWITH) == null ? "" : maps.get(DefineReport.ID_SEARCHJSON_PARAM_NAMEWITH);
    if (!StringUtils.isEmpty(nameWith)) {
      nameWith = "%" + nameWith + "%";
    }

    // セッションの取得
    HttpSession session = request.getSession();

    // JSON 戻り値格納
    String json = "";

    // 配列準備
    ArrayList<String> paramData = new ArrayList<String>();

    // SQL構文
    String sqlcommand = "";

    // JSONパラメータの解析
    JSONArray map = new JSONArray();
    if (!"".equals(outjson)) {
      map = (JSONArray) JSONSerializer.toJSON(outjson);
    }

    String jndiName = Defines.STR_JNDI_DS;

    // ページ単位に処理分離
    try {

      // アクション指定
      if (outaction.equals(DefineReport.ID_PARAM_ACTION_GET)) {

        // 検索ボタン押下
        if (outobj.equals(DefineReport.Button.SEARCH.getObj())) {

          if (session.getAttribute(Consts.STR_SES_LOGINUSER) == null) {
            // セッションタイムアウト時
            sqlcommand = "select 1";

          } else {
            // ユーザーID取得
            String userid = maps.get("userid") == null ? "0" : maps.get("userid");

            // web.xmlからサイト利用時間を取得
            String fromData = getServletContext().getInitParameter(Consts.FROM_DATA);
            String toData = getServletContext().getInitParameter(Consts.TO_DATA);
            /* 利用可能時間チェック */
            ChkUsableTime sys = new ChkUsableTime(fromData, toData);
            if (sys.isCloseTime(userid) || sys.isWaitTime(userid)) {
              // 利用時間外 || メンテナンス中
              sqlcommand = "select 1";
            } else {
              //
              sqlcommand = "select 0";
            }
          }
        }

        // (特殊ページ)ログインボタン押下
        if (outobj.equals(DefineReport.Button.LOGIN.getObj())) {
          map.get(0);
          boolean result = false;
          // if (DefineReport.ID_PAGE_042.equals(outpage)){
          // result = InputChecker.checkPass042(obj.optString("PASS"));
          // }
          if (result) {
            sqlcommand = "select 1";
          } else {
            //
            sqlcommand = "select 0";
          }
        }

      } else if (outaction.equals(DefineReport.ID_PARAM_ACTION_AUTOQUERY)) {

        // SQLベース
        ItemList il = new ItemList();
        // セッション情報取得
        User userInfo = (User) session.getAttribute(Consts.STR_SES_LOGINUSER);
        String userid = userInfo == null ? "0" : "" + userInfo.getCD_user();

        if (outtarget.equals("save")) {

          // 検索条件の削除
          sqlcommand = DefineReport.ID_SQL_DELETE_SNAPSHOT;
          paramData.add(userid);
          paramData.add("0");
          il.executeItem(sqlcommand, paramData, Defines.STR_JNDI_DS);

          // 検索条件の登録
          sqlcommand = DefineReport.ID_SQL_INSERT_SNAPSHOT;
          paramData.clear();
          paramData.add(userid);
          paramData.add("0");
          paramData.add(map.toString());
          paramData.add(userInfo.getId());
          paramData.add(userInfo.getId());
          il.executeItem(sqlcommand, paramData, Defines.STR_JNDI_DS);

          // 後述の処理を実行しない
          sqlcommand = "";

        } else if (outtarget.equals("load")) {

          sqlcommand = DefineReport.ID_SQL_SELECT_SNAPSHOT;
          paramData.add(userid);
          paramData.add("0");
        }


      } else if (outaction.equals(DefineReport.ID_PARAM_ACTION_ITEMS)) {
        /** 商品入力関連 */
        if (outobj.equals(DefineReport.Button.ENTRY.getObj())) {
          // Toolbar「保存」ボタン処理
          ItemList il = new ItemList();
          il.setItemQuery(map, Defines.STR_JNDI_DS);

        } else if (outobj.equals(DefineReport.Button.INPUT.getObj())) {
          // 「商品グループ」ComboGrid処理
          sqlcommand = DefineReport.ID_SQL_KEY_HEAD;
          JSONObject obj = (JSONObject) map.get(0);
          paramData.add(obj.getString("CD_USER"));

        } else if (outobj.equals(DefineReport.Button.CALL.getObj())) {
          // 「呼出」ボタン処理
          sqlcommand = DefineReport.ID_SQL_KEY_HEAD_NO;
          JSONObject obj = (JSONObject) map.get(0);
          paramData.add(obj.getString("CD_CTG"));

        } else if (outobj.equals(DefineReport.Button.DELETE.getObj())) {
          // 「削除」ボタン処理
          ItemList il = new ItemList();
          il.deleteItemQuery(map, Defines.STR_JNDI_DS);

          sqlcommand = DefineReport.ID_SQL_KEY_HEAD;
          JSONObject obj = (JSONObject) map.get(0);
          paramData.add(obj.getString("CD_USER"));

        } else {
          // 商品コード入力
          if (StringUtils.isEmpty(nameWith)) {
            sqlcommand = DefineReport.ID_SQL_BLANK;

          } else {
            sqlcommand = DefineReport.ID_SQL_SYOHIN_JAN;
            paramData.add(nameWith);
            paramData.add(nameWith);
          }
        }

      } else if (outaction.equals(DefineReport.ID_PARAM_ACTION_TENPO)) {
        /** 店舗グループ関連 */
        if (outobj.equals(DefineReport.Button.ENTRY_TENPOG.getObj())) {
          // Toolbar「保存」ボタン処理
          TenpoList tl = new TenpoList();
          tl.setTenpoGroup(map, Defines.STR_JNDI_DS);

        } else if (outobj.equals(DefineReport.Button.INPUT_TENPOG.getObj())) {
          // 「店舗グループ」ComboGrid処理
          sqlcommand = DefineReport.ID_SQL_KEY_HEAD_TG;
          JSONObject obj = (JSONObject) map.get(0);
          paramData.add(obj.getString("CD_USER"));

        } else if (outobj.equals(DefineReport.Button.CALL_TENPOG.getObj())) {
          // 「呼出」ボタン処理
          sqlcommand = DefineReport.ID_SQL_KEY_HEAD_NO_TG;
          JSONObject obj = (JSONObject) map.get(0);
          paramData.add(obj.getString("CD_CTG"));

        } else if (outobj.equals(DefineReport.Button.DELETE_TENPOG.getObj())) {
          // 「削除」ボタン処理
          TenpoList il = new TenpoList();
          il.deleteItemQuery(map, Defines.STR_JNDI_DS);

          sqlcommand = DefineReport.ID_SQL_KEY_HEAD_TG;
          JSONObject obj = (JSONObject) map.get(0);
          paramData.add(obj.getString("CD_USER"));

        } else {
          // 店舗コード入力
          if (StringUtils.isEmpty(nameWith)) {
            sqlcommand = DefineReport.ID_SQL_BLANK;

          } else {
            sqlcommand = DefineReport.ID_SQL_SYOHIN_TENPO;
            paramData.add(nameWith);
            paramData.add(nameWith);
          }
        }

      } else if (outaction.equals(DefineReport.ID_PARAM_ACTION_SHIORI)) {

        /** 定義保存関連 */
        // SQLベース
        ItemList il = new ItemList();
        if (outobj.equals(DefineReport.Button.ENTRY_SHIORI.getObj())) { // 保存ボタン処理
          // 定義保存の削除
          if (!maps.get("CD_SHIORI").equals(maps.get("NM_SHIORI"))) {
            sqlcommand = DefineReport.ID_SQL_DELETE_SHIORI;
            paramData.add(maps.get("CD_SHIORI"));
            il.executeItem(sqlcommand, paramData, Defines.STR_JNDI_DS);
          }

          // 定義保存の登録
          sqlcommand = DefineReport.ID_SQL_INSERT_SHIORI;
          paramData.clear();
          paramData.add(maps.get("CD_USER"));
          paramData.add(maps.get("CD_REPORT"));
          paramData.add(maps.get("NM_SHIORI"));
          paramData.add(map.toString());
          paramData.add(maps.get("FG_PUBLIC"));
          il.executeItem(sqlcommand, paramData, Defines.STR_JNDI_DS);
          // 後述の処理を実行しない
          sqlcommand = "";

        } else if (outobj.equals(DefineReport.Button.DELETE_SHIORI.getObj())) { // 削除ボタン処理
          // 定義保存の削除
          sqlcommand = DefineReport.ID_SQL_DELETE_SHIORI;
          paramData.add(maps.get("CD_SHIORI"));
          il.executeItem(sqlcommand, paramData, Defines.STR_JNDI_DS);
          // 後述の処理を実行しない
          sqlcommand = "";

        } else if (outobj.equals(DefineReport.Select.SHIORI.getObj())) { // 初期化処理
          if (StringUtils.isEmpty(nameWith)) {
            nameWith = "%%"; // 定義のみ全て検索
          }
          // 個人
          paramData.add(maps.get("CD_USER"));
          paramData.add(maps.get("CD_REPORT"));
          // 部署
          paramData.add(maps.get("CD_REPORT"));
          paramData.add(maps.get("CD_USER"));
          // 全社
          paramData.add(maps.get("CD_REPORT"));
          // 定義名称
          paramData.add(StringUtils.removeStart(nameWith, "%"));

          sqlcommand = DefineReport.ID_SQL_SELECT_SHIORI;

        }

      } else if (outaction.equals(DefineReport.ID_PARAM_ACTION_INIT)) {

        new GetSqlCommandInit();
        json = GetSqlCommandInit.getSqlcommand(request, maps, datatype, jndiName);

      } else if (outaction.equals(DefineReport.ID_PARAM_ACTION_CHANGE)) {

        new GetSqlCommandChange();
        json = GetSqlCommandChange.getSqlcommand(request, maps, datatype, jndiName);

      } else if (outaction.equals(DefineReport.ID_PARAM_ACTION_CHECK)) {

        new GetSqlCommandCheck();
        json = GetSqlCommandCheck.getSqlcommand(request, maps, datatype, jndiName);

      } else if (outaction.equals(DefineReport.ID_PARAM_ACTION_DEFAULT)) {

        // 照会ボタン押下
        if (outobj.equals(DefineReport.Button.SEARCH.getObj())) {

          // セッション設定
          session.setAttribute(DefineReport.ID_SESSION_STORAGE, map.toString());

          // パラメータ取得
          String userid = maps.get("userid") == null ? "0" : maps.get("userid");
          String user = maps.get("user") == null ? "0" : maps.get("user");
          String report = maps.get("report") == null ? "0" : maps.get("report");

          // SQLベース
          ItemList il = new ItemList();

          if (outtarget.equals("")) {
            // 更新コマンドの準備
            sqlcommand = DefineReport.ID_SQL_INSERT_SYSLOGS;
            paramData.clear();
            paramData.add(userid);
            paramData.add(DefineReport.ID_ACTION_QUERY);
            paramData.add("");
            paramData.add(user);
            paramData.add(report);

            // レポート参照ログの書き込み
            il.executeItem(sqlcommand, paramData, Defines.STR_JNDI_DS);

          } else {
            // 検索条件の削除
            sqlcommand = DefineReport.ID_SQL_DELETE_SNAPSHOT;
            paramData.add(user);
            paramData.add(report);
            il.executeItem(sqlcommand, paramData, Defines.STR_JNDI_DS);

            // 検索条件の登録
            sqlcommand = DefineReport.ID_SQL_INSERT_SNAPSHOT;
            paramData.clear();
            paramData.add(user);
            paramData.add(report);
            paramData.add(map.toString());
            paramData.add(userid);
            paramData.add(userid);
            il.executeItem(sqlcommand, paramData, Defines.STR_JNDI_DS);
          }

          // 後述の処理を実行しない
          sqlcommand = "";
        }

        // Excelボタン押下
        if (outobj.equals(DefineReport.Button.EXCEL.getObj())) {

          // パラメータ取得
          String userid = maps.get("userid") == null ? "0" : maps.get("userid");
          String user = maps.get("user") == null ? "0" : maps.get("user");
          String report = maps.get("report") == null ? "0" : maps.get("report");

          // 更新コマンドの準備
          sqlcommand = DefineReport.ID_SQL_INSERT_SYSLOGS;
          paramData.add(userid);
          paramData.add(DefineReport.ID_ACTION_EXCEL);
          paramData.add("");
          paramData.add(user);
          paramData.add(report);

          // レポート参照ログの書き込み
          ItemList il = new ItemList();
          il.executeItem(sqlcommand, paramData, Defines.STR_JNDI_DS);

          // 後述の処理を実行しない
          sqlcommand = "";
        }

        // その他ボタン押下
        if (outobj.equals(DefineReport.Button.UPLOAD.getObj())) {
          // パラメータ取得
          String userid = maps.get("userid") == null ? "0" : maps.get("userid");
          String user = maps.get("user") == null ? "0" : maps.get("user");
          String report = maps.get("report") == null ? "0" : maps.get("report");

          // 更新コマンドの準備
          sqlcommand = DefineReport.ID_SQL_INSERT_SYSLOGS;
          paramData.add(userid);
          paramData.add(StringUtils.removeStart(outobj, "btn_"));
          paramData.add("");
          paramData.add(user);
          paramData.add(report);

          // レポート参照ログの書き込み
          ItemList il = new ItemList();
          il.executeItem(sqlcommand, paramData, Defines.STR_JNDI_DS);

          // 後述の処理を実行しない
          sqlcommand = "";
        }
      } else {
        System.out.println(DefineReport.ID_PARAM_ACTION + "：未指定");
      }

      // SQL構文の実行（コマンド指定あり）
      if (!"".equals(sqlcommand) && "".equals(json)) {
        try {
          json = selectJSON(sqlcommand, paramData, jndiName, datatype);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }

      // JSON データのロード
      response.setContentType("text/html;charset=UTF-8");
      PrintWriter pw = response.getWriter();
      pw.print(json.toString());
      pw.close();

    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  /**
   * 検索＆JSON生成
   *
   * @param sqlCommand
   * @param paramData
   * @param JNDIname
   * @param datatype
   * @return JSON文字列
   * @throws Exception
   */
  public static String selectJSON(String sqlCommand, ArrayList<String> paramData, String JNDIname, String datatype) throws Exception {

    if (DefineReport.ID_DEBUG_MODE) {
      System.out.println("/* selectJSON: */" + sqlCommand + "; /* " + paramData.toString() + " */");
    }
    String jsonString = "";

    // 検索
    Connection con = null; // コネクション
    try {
      // コネクションの取得
      con = DBConnection.getConnection(JNDIname);

      // 実行SQL設定
      PreparedStatement stmt = con.prepareStatement(sqlCommand);

      // 分類コードパラメータ判断
      for (int i = 0; i < paramData.size(); i++) {
        stmt.setString((i + 1), paramData.get(i));
      }
      // SQL実行
      ResultSet rs = stmt.executeQuery();

      int total = 0;

      // カラム数
      ResultSetMetaData rsmd = rs.getMetaData();
      int sizeColumn = rsmd.getColumnCount();

      JSONArray json = new JSONArray();

      // レコード情報の格納先(JSONObject)作成
      JSONObject jsonOB = new JSONObject();

      // 項目単位の情報格納
      List<JSONObject> lineData = new ArrayList<JSONObject>();

      // 結果の取得
      while (rs.next()) {
        total++;

        JSONObject obj = new JSONObject();

        for (int i = 1; i <= sizeColumn; i++) {
          obj.put(rsmd.getColumnLabel(i), rs.getString(i));
        }

        // 行データ格納
        json.add(obj);

        // 行情報へセル情報を追加
        lineData.add(obj);

      }
      if (datatype.length() == 0) {
        // jqEasy 用 JSON モデル作成
        JQEasyModel jsonModel = new JQEasyModel();
        jsonModel.setRows(lineData);
        jsonModel.setTotal(total);
        jsonOB = JSONObject.fromObject(JSONSerializer.toJSON(jsonModel));

        jsonString = jsonOB.toString();

      } else if (DefineReport.ID_PARAM_TYPE_COMBOBOX.equals(datatype)) {
        // datatype = combobox
        jsonString = json.toString();
      } else if (DefineReport.ID_PARAM_TYPE_DATAGRID.equals(datatype)) {
        // jqEasy 用 JSON モデル作成
        JQEasyModel jsonModel = new JQEasyModel();
        jsonModel.setRows(lineData);
        jsonModel.setTotal(total);
        jsonOB = JSONObject.fromObject(JSONSerializer.toJSON(jsonModel));

        jsonString = jsonOB.toString();
      }

      con.commit();
      con.close();

    } catch (Exception e) {
      /* 接続解除 */
      if (con != null) {
        try {
          con.rollback();
          con.close();
        } catch (SQLException e1) {
          e1.printStackTrace();
        }
      }
      e.printStackTrace();
    }

    return jsonString;
  }

  /**
   * JSONObjectの内容をSqlで取得するSQL生成
   *
   * @param obj
   * @return JSON文字列
   */
  @SuppressWarnings("unchecked")
  public static String convertJsonobjToSql(JSONObject obj) {

    String sqlcol = "";
    Iterator<String> keys = obj.keys();
    while (keys.hasNext()) {
      String key = keys.next();
      sqlcol += ",'" + obj.optString(key) + "' as " + key;
    }

    return "select " + StringUtils.removeStart(sqlcol, ",") + " from (SELECT 1 AS DUMMY) DUMMY";
  }
}
