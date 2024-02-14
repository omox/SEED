package servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.iq80.snappy.Snappy;
import authentication.bean.User;
import authentication.defines.Consts;
import common.ChkUsableTime;
import common.DefineReport;
import common.Defines;
import common.MessageUtility;
import common.MessageUtility.Msg;
import common.MessageUtility.MsgKey;
import common.Pass;
import common.RowsLengthLimitUtility;
import dao.*;
import dto.JQEasyModel;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

/**
 * Servlet implementation class JQGridJSON
 */
public class JQGridJSON extends HttpServlet {
  private static final long serialVersionUID = 1L;

  /**
   * @see HttpServlet#HttpServlet()
   */
  public JQGridJSON() {
    super();
  }

  /**
   * @see HttpServlet#service(HttpServletRequest request, HttpServletResponse response)
   */
  @Override
  @SuppressWarnings("unchecked")
  protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    // 文字変換コード設定【重要】
    request.setCharacterEncoding("UTF-8");

    // パラメータ一覧【確認】
    HashMap<String, String> map = new HashMap<String, String>();
    Enumeration<String> enums = request.getParameterNames();
    while (enums.hasMoreElements()) {
      String name = enums.nextElement();
      // if (DefineReport.ID_DEBUG_MODE)
      // System.out.println(name + "=" + request.getParameter(name));
      map.put(name, request.getParameter(name));
    }

    // レポート情報
    String report = request.getParameter("report");
    if (report == null) {
      return;
    }

    // セッション
    HttpSession session = request.getSession(false);

    int start = 0;
    int limit = 0;

    List<String> actionList = Arrays.asList(DefineReport.ID_PARAM_ACTION_UPDATE, DefineReport.ID_PARAM_ACTION_DELETE);

    if (request.getParameter("rows") != null) {
      limit = Integer.parseInt(request.getParameter("rows")); // ページ辺りの表示レコード数;
    }

    // コネクションの取得
    String JNDIname = Defines.STR_JNDI_DS;
    boolean runSelect = true;

    // 利用時間外の確認
    String reportNo = "";
    boolean execut = true;

    // 例外処理(分類明細)
    String sendBtnid = map.get("SENDBTNID"); // 呼出しボタン
    if (StringUtils.isEmpty(sendBtnid) || (!StringUtils.isEmpty(sendBtnid) && !(StringUtils.equals("workTableDel", sendBtnid) || StringUtils.equals("workTableInit", sendBtnid)))) {
      if (session.getAttribute(Consts.STR_SES_REPORT_NO) != null) {
        reportNo = session.getAttribute(Consts.STR_SES_REPORT_NO).toString();
      }

      String fromData = getServletContext().getInitParameter(Consts.FROM_DATA);
      String toData = getServletContext().getInitParameter(Consts.TO_DATA);
      ChkUsableTime sys = new ChkUsableTime(fromData, toData);

      if (actionList.indexOf(map.get(DefineReport.ID_PARAM_ACTION)) != -1) {
        if (sys.isTimeOut(reportNo)) {
          JSONObject option = new JSONObject();
          JSONArray msg = new JSONArray();
          msg.add(MessageUtility.getMsg(DefineReport.ID_MSG_TIMEOUT_EXCEPTION));
          option.put(MsgKey.E.getKey(), msg);
          session.setAttribute(DefineReport.ID_SESSION_OPTION, option);
          execut = false;
          runSelect = false;
        }
      }
    }

    // 更新処理
    if (execut && DefineReport.ID_PARAM_ACTION_UPDATE.equals(map.get(DefineReport.ID_PARAM_ACTION))) {
      // メッセージ表示用
      JSONObject option = null;

      User userInfo = (User) request.getSession().getAttribute(Consts.STR_SES_LOGINUSER);

      String menuKbn = "-1";

      // 本部マスタ
      if ((!StringUtils.isEmpty(userInfo.getYobi6_()) && !userInfo.getYobi6_().equals("-1")) || StringUtils.isEmpty(userInfo.getYobi6_())) {
        menuKbn = "4";

        // 本部特売
      } else if ((!StringUtils.isEmpty(userInfo.getYobi8_()) && !userInfo.getYobi8_().equals("-1")) || StringUtils.isEmpty(userInfo.getYobi8_())) {
        menuKbn = "5";
      }

      if ("Pass".equals(report)) {
        option = new Pass(JNDIname).update(request, session, map, userInfo);
      } else if (DefineReport.ID_PAGE_X242.equals(report)) {
        option = new Reportx242Dao(JNDIname).update(request, session, map, userInfo);
      }

      // 本部マスタ画面の操作でかつ更新権限がない or 特売マスタ画面の操作でかつ更新権限がない
      else if ((menuKbn.equals("4") && StringUtils.isEmpty(userInfo.getYobi6_())) || (menuKbn.equals("5") && StringUtils.isEmpty(userInfo.getYobi8_()))) {

        option = new JSONObject();
        option.put(MsgKey.E.getKey(), MessageUtility.getDbMessageIdObj("E00012", new String[] {}));
      }

      /* マスタ */
      else if (DefineReport.ID_PAGE_X002.equals(report)) {
        option = new Reportx002Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_X022.equals(report)) {
        option = new Reportx022Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_X031.equals(report)) {
        option = new Reportx031Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_X032.equals(report)) {
        option = new Reportx032Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_X033.equals(report)) {
        option = new Reportx033Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_X034.equals(report)) {
        option = new Reportx034Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_X042.equals(report)) {
        option = new Reportx042Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_X052.equals(report)) {
        option = new Reportx052Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_X092.equals(report)) {
        option = new Reportx092Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_X102.equals(report)) {
        option = new Reportx102Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_X112.equals(report)) {
        option = new Reportx112Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_X122.equals(report)) {
        option = new Reportx122Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_X131.equals(report)) {
        option = new Reportx131Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_X142.equals(report)) {
        option = new Reportx142Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_X143.equals(report)) {
        option = new Reportx143Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_X152.equals(report)) {
        option = new Reportx152Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_X161.equals(report)) {
        option = new Reportx161Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_X172.equals(report)) {
        option = new Reportx172Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_X192.equals(report)) {
        option = new Reportx192Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_X202.equals(report)) { // 配送グループ
        option = new Reportx202Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_X204.equals(report)) {
        option = new Reportx204Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_X213.equals(report)) { // プライスカード
        option = new Reportx213Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_X214.equals(report)) {
        option = new Reportx214Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_X215.equals(report)) {
        option = new Reportx215Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_X216.equals(report)) {
        option = new Reportx216Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_X217.equals(report)) {
        option = new Reportx217Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_X218.equals(report)) {
        option = new Reportx218Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_X221.equals(report)) {
        option = new Reportx221Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_X271.equals(report)) {
        option = new Reportx271Dao(JNDIname).update(request, session, map, userInfo);
      }

      /* 特売 */
      else if (DefineReport.ID_PAGE_TM002.equals(report)) {
        option = new ReportTM002Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_TM004.equals(report)) {
        option = new ReportTM004Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_TM005.equals(report)) {
        option = new ReportTM005Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_TG001.equals(report)) {
        option = new ReportTG001Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_TG003.equals(report)) {
        option = new ReportTG003Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_TG008.equals(report)) {
        option = new ReportTG008Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_TG012.equals(report)) {
        option = new ReportTG012Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_TG015.equals(report)) {
        option = new ReportTG015Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_TG016.equals(report)) {
        if (DefineReport.Button.KYOKA.getObj().equals(map.get(DefineReport.ID_PARAM_OBJ))) {
          option = new ReportTG016Dao(JNDIname).update2(request, session, map, userInfo);
        } else {
          option = new ReportTG016Dao(JNDIname).update(request, session, map, userInfo);
        }

      } else if (DefineReport.ID_PAGE_TG040.equals(report)) {
        option = new ReportTG040Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_SA003.equals(report)) {
        option = new ReportSA003Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_SA004.equals(report)) {
        option = new ReportSA004Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_SA005.equals(report)) {
        option = new ReportSA005Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_SA008.equals(report)) {
        option = new ReportSA008Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_YH001.equals(report)) {
        option = new ReportYH001Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_YH201.equals(report)) {
        option = new ReportYH201Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_YH002.equals(report)) {
        option = new ReportYH002Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_YH203.equals(report)) {
        option = new ReportYH203Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_BW002.equals(report)) {
        option = new ReportBW002Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_BW004.equals(report)) {
        option = new ReportBW004Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_BM006.equals(report)) {
        option = new ReportBM006Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_RP004.equals(report)) {
        option = new ReportRP004Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_RP006.equals(report)) {
        option = new ReportRP006Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_RP007.equals(report)) {
        option = new ReportRP007Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_ST008.equals(report)) {
        option = new ReportST008Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_ST021.equals(report)) {
        option = new ReportST021Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_TR001.equals(report)) {
        option = new ReportTR001Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_TR005.equals(report)) {
        option = new ReportTR005Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_TR007.equals(report)) {
        option = new ReportTR007Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_TR016.equals(report)) {
        option = new ReportTR016Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_TU002.equals(report)) {
        option = new ReportTU002Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_TJ001.equals(report)) {
        option = new ReportTJ005Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_TJ005.equals(report)) {
        option = new ReportTJ005Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_TJ002.equals(report) || DefineReport.ID_PAGE_TJ006.equals(report)) {
        option = new ReportTJ006Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_TJ009.equals(report)) {
        option = new ReportTJ005Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_TJ011.equals(report)) {
        option = new ReportTJ005Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_SK003.equals(report)) {
        option = new ReportSK003Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_BT002.equals(report)) {
        option = new ReportBT002Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_SO003.equals(report)) {
        option = new ReportSO003Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_JU012.equals(report)) {
        option = new ReportJU012Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_JU013.equals(report)) {
        option = new ReportJU013Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_JU022.equals(report)) {
        option = new ReportJU022Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_JU032.equals(report)) {
        option = new ReportJU032Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_JU033.equals(report)) {
        option = new ReportJU033Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_TG020.equals(report)) {
        option = new ReportST021Dao(JNDIname).update(request, session, map, userInfo);

        /* 店舗特売 */
      } else if (DefineReport.ID_PAGE_HT002.equals(report)) {
        option = new ReportHT002Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_HT004.equals(report)) {
        option = new ReportHT004Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_HT007.equals(report)) {
        option = new ReportHT007Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_HT009.equals(report)) {
        option = new ReportHT009Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_HY003.equals(report)) {
        option = new ReportHY003Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_GM003.equals(report)) {
        option = new ReportGM003Dao(JNDIname).update(request, session, map, userInfo);
      } else if ("Pass".equals(report)) {
        option = new Pass(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_X242.equals(report)) {
        option = new Reportx242Dao(JNDIname).update(request, session, map, userInfo);
      } else if (DefineReport.ID_PAGE_X245.equals(report)) {
        option = new Reportx245Dao(JNDIname).update(request, session, map, userInfo);
      } else if (DefineReport.ID_PAGE_X246.equals(report)) {
        // WEB商談
        option = new Reportx246Dao(JNDIname).update(request, session, map, userInfo);
      } else if (DefineReport.ID_PAGE_X247.equals(report)) {
        // WEB商談
        option = new Reportx247Dao(JNDIname).update(request, session, map, userInfo);
      } else if (DefineReport.ID_PAGE_X249.equals(report)) {
        option = new Reportx249Dao(JNDIname).update(request, session, map, userInfo);
      } else if (DefineReport.ID_PAGE_X250.equals(report)) {
        option = new Reportx250Dao(JNDIname).update(request, session, map, userInfo);
      } else if (DefineReport.ID_PAGE_X251.equals(report)) {
        option = new Reportx251Dao(JNDIname).update(request, session, map, userInfo);
      }

      boolean is_empty = true;
      for (MsgKey key : MsgKey.values()) {
        if (option == null) {
          break;
        } else if (option.containsKey(key.getKey())) {
          is_empty = false;
          break;
        }
      }
      if (is_empty) {
        // 表示するメッセージがない場合、エラーメッセージをセット
        option = new JSONObject();
        JSONArray msg = new JSONArray();
        msg.add(MessageUtility.getMessageObj(Msg.E00001.getVal()));
        option.put(MsgKey.E.getKey(), msg);
      }
      session.setAttribute(DefineReport.ID_SESSION_OPTION, option);
      runSelect = false;
    }

    // 削除処理が存在する場合
    if (execut && DefineReport.ID_PARAM_ACTION_DELETE.equals(map.get(DefineReport.ID_PARAM_ACTION))) {
      // メッセージ表示用
      JSONObject option = null;

      User userInfo = (User) request.getSession().getAttribute(Consts.STR_SES_LOGINUSER);

      String menuKbn = "-1";

      // 本部マスタ
      if ((!StringUtils.isEmpty(userInfo.getYobi7_()) && !userInfo.getYobi7_().equals("-1")) || StringUtils.isEmpty(userInfo.getYobi7_())) {
        menuKbn = "4";
      }

      /* マスタ 本部マスタのみ削除権限チェックは個別対応 */
      if (DefineReport.ID_PAGE_X002.equals(report)) {
        option = new Reportx002Dao(JNDIname).delete(request, session, map, userInfo);

        // 本部マスタ画面の操作でかつ削除権限がない
      } else if (menuKbn.equals("4") && StringUtils.isEmpty(userInfo.getYobi7_())) {
        option = new JSONObject();
        option.put(MsgKey.E.getKey(), MessageUtility.getDbMessageIdObj("E00012", new String[] {}));

      } else if (DefineReport.ID_PAGE_X022.equals(report)) {
        option = new Reportx022Dao(JNDIname).delete(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_X042.equals(report)) {
        option = new Reportx042Dao(JNDIname).delete(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_X052.equals(report)) {
        option = new Reportx052Dao(JNDIname).delete(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_X092.equals(report)) {
        option = new Reportx092Dao(JNDIname).delete(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_X102.equals(report)) {
        option = new Reportx102Dao(JNDIname).delete(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_X122.equals(report)) {
        option = new Reportx122Dao(JNDIname).delete(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_X131.equals(report)) {
        option = new Reportx131Dao(JNDIname).delete(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_X143.equals(report)) {
        option = new Reportx143Dao(JNDIname).delete(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_X161.equals(report)) {
        option = new Reportx161Dao(JNDIname).delete(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_X172.equals(report)) {
        option = new Reportx172Dao(JNDIname).delete(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_X192.equals(report)) {
        option = new Reportx192Dao(JNDIname).delete(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_X202.equals(report)) { // 配送グループマスタ
        option = new Reportx202Dao(JNDIname).delete(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_X204.equals(report)) {
        option = new Reportx204Dao(JNDIname).delete(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_X213.equals(report)) { // プライスカード
        option = new Reportx213Dao(JNDIname).delete(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_X214.equals(report)) {
        option = new Reportx214Dao(JNDIname).delete(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_X215.equals(report)) {
        option = new Reportx215Dao(JNDIname).delete(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_X216.equals(report)) {
        option = new Reportx216Dao(JNDIname).delete(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_X217.equals(report)) {
        option = new Reportx217Dao(JNDIname).delete(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_X218.equals(report)) {
        option = new Reportx218Dao(JNDIname).delete(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_X221.equals(report)) { // コースマスタ
        option = new Reportx221Dao(JNDIname).delete(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_BW002.equals(report)) {
        option = new ReportBW002Dao(JNDIname).delete(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_BW004.equals(report)) {
        option = new ReportBW004Dao(JNDIname).delete(request, session, map, userInfo);
      }

      /* 特売 */
      else if (DefineReport.ID_PAGE_TM004.equals(report)) {
        option = new ReportTM004Dao(JNDIname).delete(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_YH000.equals(report)) {
        option = new ReportYH000Dao(JNDIname).delete(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_TG003.equals(report)) {
        option = new ReportTG003Dao(JNDIname).delete(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_TG012.equals(report)) { // 特売アンケート
        option = new ReportTG012Dao(JNDIname).update(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_TG016.equals(report)) { // 特売アンケート
        option = new ReportTG016Dao(JNDIname).delete(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_SO003.equals(report)) {
        option = new ReportSO003Dao(JNDIname).delete(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_BM006.equals(report)) {
        option = new ReportBM006Dao(JNDIname).delete(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_RP004.equals(report)) {
        option = new ReportRP004Dao(JNDIname).delete(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_RP006.equals(report)) {
        option = new ReportRP006Dao(JNDIname).delete(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_ST008.equals(report)) {
        option = new ReportST008Dao(JNDIname).delete(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_TJ014.equals(report)) {
        option = new ReportTJ014Dao(JNDIname).delete(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_TR001.equals(report)) {
        option = new ReportTR001Dao(JNDIname).delete(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_TR005.equals(report)) {
        option = new ReportTR005Dao(JNDIname).delete(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_TR016.equals(report)) {
        option = new ReportTR016Dao(JNDIname).delete(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_SK003.equals(report)) {
        option = new ReportSK003Dao(JNDIname).delete(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_BT002.equals(report)) {
        option = new ReportBT002Dao(JNDIname).delete(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_JU013.equals(report)) {
        option = new ReportJU013Dao(JNDIname).delete(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_JU022.equals(report)) {
        option = new ReportJU022Dao(JNDIname).delete(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_JU033.equals(report)) {
        option = new ReportJU033Dao(JNDIname).delete(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_GM003.equals(report)) {
        option = new ReportGM003Dao(JNDIname).delete(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_X241.equals(report)) {
        option = new Reportx241Dao(JNDIname).delete(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_X245.equals(report)) {
        // WEB商談
        option = new Reportx245Dao(JNDIname).delete(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_X246.equals(report)) {
        // WEB商談
        option = new Reportx246Dao(JNDIname).delete(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_X247.equals(report)) {
        // WEB商談
        option = new Reportx247Dao(JNDIname).delete(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_X250.equals(report)) {
        option = new Reportx250Dao(JNDIname).delete(request, session, map, userInfo);

      } else if (DefineReport.ID_PAGE_X251.equals(report)) {
        option = new Reportx251Dao(JNDIname).delete(request, session, map, userInfo);

      }

      boolean is_empty = true;
      for (MsgKey key : MsgKey.values()) {
        if (option == null) {
          break;
        } else if (option.containsKey(key.getKey())) {
          is_empty = false;
          break;
        }
      }
      if (is_empty) {
        // 表示するメッセージがない場合、エラーメッセージをセット
        option = new JSONObject();
        JSONArray msg = new JSONArray();
        msg.add(MessageUtility.getMessageObj(Msg.E00002.getVal()));
        option.put(MsgKey.E.getKey(), msg);
      }
      session.setAttribute(DefineReport.ID_SESSION_OPTION, option);
      runSelect = false;

    }

    // 検索実行
    if (runSelect) {

      // 検索クラス作成

      /* マスタ */
      if (DefineReport.ID_PAGE_X001.equals(report)) {
        convertItem(new Reportx001Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_X002.equals(report)) {
        convertItem(new Reportx002Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_X003.equals(report)) {
        convertItem(new Reportx003Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_X005.equals(report)) {
        convertItem(new Reportx005Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_X006.equals(report)) {
        convertItem(new Reportx006Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_X007.equals(report)) {
        convertItem(new Reportx007Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_X021.equals(report)) {
        convertItem(new Reportx021Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_X022.equals(report)) {
        convertItem(new Reportx022Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_X031.equals(report)) {
        convertItem(new Reportx031Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_X032.equals(report)) {
        convertItem(new Reportx032Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_X033.equals(report)) {
        convertItem(new Reportx033Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_X034.equals(report)) {
        convertItem(new Reportx034Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_X041.equals(report)) {
        convertItem(new Reportx041Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_X042.equals(report)) {
        convertItem(new Reportx042Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_X051.equals(report)) {
        convertItem(new Reportx051Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_X052.equals(report)) {
        convertItem(new Reportx052Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_X053.equals(report)) {
        convertItem(new Reportx053Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_X091.equals(report)) {
        convertItem(new Reportx091Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_X092.equals(report)) {
        convertItem(new Reportx092Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_X101.equals(report)) {
        convertItem(new Reportx101Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_X111.equals(report)) {
        convertItem(new Reportx111Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_X112.equals(report)) {
        convertItem(new Reportx112Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_X121.equals(report)) {
        convertItem(new Reportx121Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_X122.equals(report)) {
        convertItem(new Reportx122Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_X131.equals(report)) {
        convertItem(new Reportx131Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_X141.equals(report)) {
        convertItem(new Reportx141Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_X143.equals(report)) {
        convertItem(new Reportx143Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_X151.equals(report)) {
        convertItem(new Reportx151Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_X152.equals(report)) {
        convertItem(new Reportx152Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_X171.equals(report)) {
        convertItem(new Reportx171Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_X172.equals(report)) {
        convertItem(new Reportx172Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_X181.equals(report)) {
        convertItem(new Reportx181Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_X191.equals(report)) {
        convertItem(new Reportx191Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_X192.equals(report)) {
        convertItem(new Reportx192Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_X201.equals(report)) {
        convertItem(new Reportx201Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_X202.equals(report)) {
        convertItem(new Reportx202Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_X203.equals(report)) {
        convertItem(new Reportx203Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_X204.equals(report)) {
        convertItem(new Reportx204Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_X211.equals(report)) {
        convertItem(new Reportx211Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_X212.equals(report)) {
        convertItem(new Reportx212Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_X213.equals(report)) {
        convertItem(new Reportx213Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_X214.equals(report)) {
        convertItem(new Reportx214Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_X215.equals(report)) {
        convertItem(new Reportx215Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_X216.equals(report)) {
        convertItem(new Reportx216Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_X217.equals(report)) {
        convertItem(new Reportx217Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_X218.equals(report)) {
        convertItem(new Reportx218Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_X221.equals(report)) {
        convertItem(new Reportx221Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_X231.equals(report)) {
        convertItem(new Reportx231Dao(JNDIname), request, map, limit, session, start);

      }

      /* 特売 */
      else if (DefineReport.ID_PAGE_TM001.equals(report)) {
        convertItem(new ReportTM001Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_TM002.equals(report)) {
        convertItem(new ReportTM002Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_TM003.equals(report)) {
        convertItem(new ReportTM003Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_TM004.equals(report)) {
        convertItem(new ReportTM004Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_TM005.equals(report)) {
        convertItem(new ReportTM005Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_TG001.equals(report)) {
        convertItem(new ReportTG001Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_TG002.equals(report)) {
        convertItem(new ReportTG002Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_TG003.equals(report)) {
        convertItem(new ReportTG003Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_TG008.equals(report)) {
        convertItem(new ReportTG008Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_TG012.equals(report)) {
        convertItem(new ReportTG012Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_TG013.equals(report)) {
        convertItem(new ReportTG013Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_TG014.equals(report)) {
        convertItem(new ReportTG014Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_TG015.equals(report)) {
        convertItem(new ReportTG015Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_TG016.equals(report)) {
        convertItem(new ReportTG016Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_TG017.equals(report)) {
        convertItem(new ReportTG017Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_TG040.equals(report)) {
        convertItem(new ReportTG040Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_SA003.equals(report)) {
        convertItem(new ReportSA003Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_SA004.equals(report)) {
        convertItem(new ReportSA004Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_SA005.equals(report)) {
        convertItem(new ReportSA005Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_SA008.equals(report)) {
        convertItem(new ReportSA008Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_YH000.equals(report)) {
        convertItem(new ReportYH000Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_YH001.equals(report)) {
        convertItem(new ReportYH001Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_YH002.equals(report)) {
        convertItem(new ReportYH002Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_YH201.equals(report)) {
        convertItem(new ReportYH201Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_YH202.equals(report)) {
        convertItem(new ReportYH202Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_YH203.equals(report)) {
        convertItem(new ReportYH203Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_BW002.equals(report)) {
        convertItem(new ReportBW002Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_BW003.equals(report)) {
        convertItem(new ReportBW003Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_BW004.equals(report)) {
        convertItem(new ReportBW004Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_BW005.equals(report)) {
        convertItem(new ReportBW005Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_BW006.equals(report)) {
        convertItem(new ReportBW006Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_BM003.equals(report)) {
        convertItem(new ReportBM003Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_BM006.equals(report)) {
        convertItem(new ReportBM006Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_BM010.equals(report)) {
        convertItem(new ReportBM010Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_BM013.equals(report)) {
        convertItem(new ReportBM013Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_BM014.equals(report)) {
        convertItem(new ReportBM014Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_BM015.equals(report)) {
        convertItem(new ReportBM015Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_SK002.equals(report)) {
        convertItem(new ReportSK002Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_SK005.equals(report)) {
        convertItem(new ReportSK002Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_SK003.equals(report)) {
        convertItem(new ReportSK003Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_ST010.equals(report)) {
        convertItem(new ReportST010Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_ST016.equals(report)) {
        convertItem(new ReportST016Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_TG020.equals(report)) {
        convertItem(new ReportTG020Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_ST019.equals(report)) {
        convertItem(new ReportST019Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_ST024.equals(report)) {
        convertItem(new ReportST024Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_RP007.equals(report)) {
        convertItem(new ReportRP007Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_JU001.equals(report)) {
        convertItem(new ReportJU001Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_JU011.equals(report)) {
        convertItem(new ReportJU011Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_JU012.equals(report)) {
        convertItem(new ReportJU012Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_JU022.equals(report)) {
        convertItem(new ReportJU022Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_JU027.equals(report)) {
        convertItem(new ReportJU027Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_JU031.equals(report)) {
        convertItem(new ReportJU031Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_JU032.equals(report)) {
        convertItem(new ReportJU032Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_JU033.equals(report)) {
        convertItem(new ReportJU033Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_RP003.equals(report)) {
        convertItem(new ReportRP003Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_RP004.equals(report)) {
        convertItem(new ReportRP004Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_RP005.equals(report)) {
        convertItem(new ReportRP005Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_RP006.equals(report)) {
        convertItem(new ReportRP006Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_RP007.equals(report)) {
        convertItem(new ReportRP007Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_RP008.equals(report)) {
        convertItem(new ReportRP008Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_RP009.equals(report)) {
        convertItem(new ReportRP009Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_RP011.equals(report)) {
        convertItem(new ReportRP011Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_ST007.equals(report)) {
        convertItem(new ReportST007Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_ST008.equals(report)) {
        convertItem(new ReportST008Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_ST010.equals(report)) {
        convertItem(new ReportST010Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_ST011.equals(report)) {
        convertItem(new ReportST011Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_ST015.equals(report)) {
        convertItem(new ReportST015Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_JU013.equals(report)) {
        convertItem(new ReportJU013Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_TR001.equals(report)) {
        convertItem(new ReportTR001Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_TR004.equals(report)) {
        convertItem(new ReportTR004Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_TR005.equals(report)) {
        convertItem(new ReportTR005Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_TR007.equals(report)) {
        convertItem(new ReportTR007Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_SR003.equals(report)) {
        convertItem(new ReportSR003Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_SH001.equals(report)) {
        convertItem(new ReportSH001Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_MM001.equals(report)) {
        convertItem(new ReportMM001Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_MM002.equals(report)) {
        convertItem(new ReportMM002Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_MM003.equals(report)) {
        convertItem(new ReportMM003Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_BT002.equals(report)) {
        convertItem(new ReportBT002Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_BT004.equals(report)) {
        convertItem(new ReportBT004Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_SO001.equals(report)) {
        convertItem(new ReportSO001Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_SO002.equals(report)) {
        convertItem(new ReportSO002Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_SO003.equals(report)) {
        convertItem(new ReportSO003Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_SO006.equals(report)) {
        convertItem(new ReportSO006Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_SO007.equals(report)) {
        convertItem(new ReportSO007Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_HY001.equals(report)) {
        convertItem(new ReportHY001Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_HY002.equals(report)) {
        convertItem(new ReportHY002Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_HY003.equals(report)) {
        convertItem(new ReportHY003Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_TU001.equals(report)) {
        convertItem(new ReportTU001Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_TU002.equals(report)) {
        convertItem(new ReportTU002Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_TJ001.equals(report)) {
        convertItem(new ReportTJ001Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_TJ005.equals(report)) {
        convertItem(new ReportTJ005Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_TJ009.equals(report)) {
        convertItem(new ReportTJ009Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_TJ011.equals(report)) {
        convertItem(new ReportTJ011Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_TJ014.equals(report)) {
        convertItem(new ReportTJ014Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_TJ015.equals(report)) {
        convertItem(new ReportTJ015Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_MI001.equals(report)) {
        convertItem(new ReportMI001Dao(JNDIname), request, map, limit, session, start);

        /* 店舗特売 */
      } else if (DefineReport.ID_PAGE_HT002.equals(report)) {
        convertItem(new ReportHT002Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_HT004.equals(report)) {
        convertItem(new ReportHT004Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_HT007.equals(report)) {
        convertItem(new ReportHT007Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_HT009.equals(report)) {
        convertItem(new ReportHT009Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_GM001.equals(report)) {
        convertItem(new ReportGM001Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_GM002.equals(report)) {
        convertItem(new ReportGM002Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_GM003.equals(report)) {
        convertItem(new ReportGM003Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_X241.equals(report)) {
        convertItem(new Reportx241Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_X242.equals(report)) {
        convertItem(new Reportx242Dao(JNDIname), request, map, limit, session, start);
      } else if (DefineReport.ID_PAGE_X243.equals(report)) {
        convertItem(new Reportx243Dao(JNDIname), request, map, limit, session, start);
      } else if (DefineReport.ID_PAGE_X244.equals(report)) {
        convertItem(new Reportx244Dao(JNDIname), request, map, limit, session, start);
      } else if (DefineReport.ID_PAGE_X245.equals(report)) {
        // WEB商談
        convertItem(new Reportx245Dao(JNDIname), request, map, limit, session, start);
      } else if (DefineReport.ID_PAGE_X246.equals(report)) {
        // WEB商談
        convertItem(new Reportx246Dao(JNDIname), request, map, limit, session, start);
      } else if (DefineReport.ID_PAGE_X247.equals(report)) {

        // 新規かつ商品コードの入力がある場合
        if (DefineReport.Button.NEW.getObj().equals(sendBtnid) && map.containsKey("SHNCD") && !StringUtils.isEmpty(map.get("SHNCD"))) {

          // ボタンを変更に置き換え
          map.replace("SENDBTNID", DefineReport.Button.SEL_CHANGE.getObj());
          session.setAttribute(DefineReport.Hidden.NO_TEIAN.getObj(), "0");

          // WEB商談
          convertItem(new Reportx002Dao(JNDIname), request, map, limit, session, start);
        } else {
          // WEB商談
          convertItem(new Reportx247Dao(JNDIname), request, map, limit, session, start);
        }
      } else if (DefineReport.ID_PAGE_X249.equals(report)) {
        convertItem(new Reportx249Dao(JNDIname), request, map, limit, session, start);
      } else if (DefineReport.ID_PAGE_X250.equals(report)) {
        convertItem(new Reportx250Dao(JNDIname), request, map, limit, session, start);
      } else if (DefineReport.ID_PAGE_X251.equals(report)) {
        convertItem(new Reportx251Dao(JNDIname), request, map, limit, session, start);
      } else if (DefineReport.ID_PAGE_X261.equals(report)) {
        convertItem(new Reportx001Dao(JNDIname), request, map, limit, session, start);
      } else if (DefineReport.ID_PAGE_X252.equals(report)) {
        convertItem(new Reportx252Dao(JNDIname), request, map, limit, session, start);
      } else if (DefineReport.ID_PAGE_X253.equals(report)) {
        convertItem(new Reportx253Dao(JNDIname), request, map, limit, session, start);

      } else if (DefineReport.ID_PAGE_X280.equals(report)) {
        convertItem(new Reportx001Dao(JNDIname), request, map, limit, session, start);

      }
    }

    // レコード情報の格納先(JSONObject)作成
    JSONObject jsonOB = new JSONObject();

    // jqEasy 用 JSON モデル作成
    JQEasyModel json = new JQEasyModel();

    // 項目単位の情報格納
    List<JSONObject> lineData = new ArrayList<JSONObject>();

    // レコードカウント
    int count = -1;

    // セルインデックス
    int index = 0;

    String states = "";

    if (session.getAttribute(DefineReport.ID_SESSION_TABLE) != null) {

      ArrayList<byte[]> al = (ArrayList<byte[]>) session.getAttribute(DefineReport.ID_SESSION_TABLE);
      int records = al.size();
      if (records > 0) {
        records--; // タイトル行の除外
      }
      json.setTotal(records); // 総レコード数の設定

      // 実績なしの店舗・分類列を除外 ---------------------------------------
      if (DefineReport.ID_PAGE_001.equals(report)) {
        String szHYORETSU = map.get("HYORETSU"); // 表列
        if (DefineReport.ValHyo.VAL22.getVal().equals(szHYORETSU)) {
          // 固定列＋総計を除外開始位置に設定
          int startIdx = 1;
          // 後方の列数
          int rearCols = 2;
          // 除外しない列のキー（タイトル後方一致）
          String[] notDeKeys = new String[] {"計"};
          al = excludeColumns(session, json, al, startIdx, 1, rearCols, notDeKeys);
        }
      }
      // --------------------------------------------------------------------------

      int page = 1; // ページ位置（初期値）

      // 総ページ数の算出
      int total_pages = 0;
      double ii = (double) records / (double) limit;
      if (records > 0) {
        total_pages = (int) Math.ceil(ii);
      } else {
        total_pages = 0;
      } // if for some

      if (page > total_pages) {
        page = total_pages; // calculate the starting position of the
                            // rows
      }

      start = limit * page - limit; // if for some reasons start position

      if (start < 0) {
        start = 0;
      }

      int state = 0;
      boolean stateFlag = false;

      Iterator<byte[]> itr = al.iterator();

      // 項目単位の情報格納
      lineData = new ArrayList<JSONObject>();

      while (itr.hasNext()) {

        count++;

        // 表示範囲の情報取得
        if (((start + 1) <= count) && (count <= (start + limit))) {

          // jqGrid 用レコード情報準備
          JSONObject n1 = new JSONObject();

          // セル（列）情報リスト
          byte[] bytes = itr.next();
          String[] columnsList = StringUtils.splitPreserveAllTokens(new String(Snappy.uncompress(bytes, 0, bytes.length), "UTF-8"), "\t");

          index = 0;
          for (String col : columnsList) {
            index++;

            if (state == index) {
              // easyui.treegrid state : closed
              states = col;
              n1.put("state", states);
              if (!"".equals(states) && states != null) {
                n1.put("iconCls", "icon-ok");
              }
              // itrCols.next(); // 次のセルへ移動
            } else {
              // セル（列）生成
              n1.put("F" + String.valueOf(index), col);
            }

          }

          // 行情報へセル情報を追加
          lineData.add(n1);

        } else if (count == 0) {

          // タイトル
          // セル（列）情報リスト
          byte[] bytes = itr.next();
          String[] columnsList = StringUtils.splitPreserveAllTokens(new String(Snappy.uncompress(bytes, 0, bytes.length), "UTF-8"), "\t");

          for (String col : columnsList) {
            state++;
            if ("STATE".equals(col)) {
              stateFlag = true;
              break;
            }
          }
          // state カラムがない場合は、初期化
          if (!stateFlag) {
            state = 0;
          }

        } else {

          // 次レコード移動
          itr.next();
        }

      }

      // if (DefineReport.ID_DEBUG_MODE) {
      // System.out.println("size : " + lineData.size());
      // }

    } else {
      // 事前検索結果がsessionに保持されていません。
      System.out.println("table属性がsessionに保持されていません。");
    }

    // レコード情報の格納（JSON形式変換用）
    json.setRows(lineData);

    // JQEasyJSONでレコード情報格納の場合
    // オプション情報設定
    JSONObject option = (JSONObject) session.getAttribute(DefineReport.ID_SESSION_OPTION);

    if (option != null) {
      if (option.containsKey(DefineReport.ID_PARAM_OPT_TITLE) && json.getTitles() == null) {
        JSONArray titArray = option.getJSONArray(DefineReport.ID_PARAM_OPT_TITLE);
        json.setTitles((String[]) titArray.toArray(new String[titArray.size()]));
      }

      // 検索結果の表示上限数を保持
      if (ArrayUtils.contains(RowsLengthLimitUtility.defaultValues, limit)) {
        // クライアント側でlimitの設定がない場合。
        String DIspId = new RowsLengthLimitUtility().getPageDispID(report, map);
        if (StringUtils.isNotEmpty(DIspId)) {
          int limitData = new RowsLengthLimitUtility().searchResultLimit(DIspId);
          if (limitData != 0) {
            limit = limitData;
          }
        }
      }
      option.put("rows", limit);

      json.setOpts(option);
    }

    // JSON 形式へ変換
    jsonOB = JSONObject.fromObject(JSONSerializer.toJSON(json));
    // if (DefineReport.ID_DEBUG_MODE) {
    // System.out.println(jsonOB.toString());
    // }

    // JSON データのロード
    response.setContentType("text/html;charset=UTF-8");
    PrintWriter pw = response.getWriter();
    pw.print(jsonOB);

  }

  /**
   * 分析
   *
   * @param ItemInterface
   * @param request
   * @param map
   * @param limit
   * @param session
   * @param start
   * @param con
   */
  private void convertItem(ItemInterface shopItem, HttpServletRequest request, HashMap<String, String> map, int limit, HttpSession session, int start) {

    try {
      // セッション情報取得
      User userInfo = (User) request.getSession().getAttribute(Consts.STR_SES_LOGINUSER);

      // ログインユーザー情報セット
      shopItem.setUserInfo(userInfo);
      // 条件セット
      shopItem.setMap(map);
      // 検索条件（Excel出力用）
      shopItem.setJson((String) session.getAttribute(DefineReport.ID_SESSION_STORAGE));
      // 検索開始位置取得
      shopItem.setStart(start);
      // 検索取得数
      shopItem.setLimit(limit);
      // SQL 実行
      shopItem.selectBy();

      // セッション保持
      session.setAttribute(DefineReport.ID_SESSION_TABLE, shopItem.getTable());
      session.setAttribute(DefineReport.ID_SESSION_WHERE, shopItem.getWhere());
      session.setAttribute(DefineReport.ID_SESSION_META, shopItem.getMeta());
      session.setAttribute(DefineReport.ID_SESSION_OPTION, shopItem.getOption());
      session.setAttribute(DefineReport.ID_SESSION_MSG, shopItem.getMessage());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * 実績なしの店舗・分類列を除外<br>
   * OptionのTitleは全ての列タイトルが設定されている前提
   *
   * @param session
   * @param json
   * @param al
   * @param startIdx 開始列インデックス
   * @param loopCnt １店舗・１分類あたりの列数
   * @param rearCols 後方の列数
   * @param notDeKeys 除外しない列のキー（タイトル後方一致）
   * @return
   * @throws IOException
   */
  @SuppressWarnings({"unchecked", "unused"})
  private ArrayList<byte[]> excludeColumns(HttpSession session, JQEasyModel json, ArrayList<byte[]> al, int startIdx, int loopCnt, int rearCols, String[] notDeKeys) throws IOException {

    String[] titles = StringUtils.splitPreserveAllTokens(new String(Snappy.uncompress(al.get(0), 0, al.get(0).length), "UTF-8"), "\t");
    // 終了列インデックス
    int endIdx = titles.length - rearCols;

    // オプション内のタイトル情報
    JSONObject option = (JSONObject) session.getAttribute(DefineReport.ID_SESSION_OPTION);

    // 除外しない列のインデックスを格納
    ArrayList<Integer> notDelIndex = new ArrayList<Integer>();

    int index = -1;
    for (byte[] rows : al) {
      index++;

      // タイトル行は処理を飛ばす
      if (index == 0) {
        continue;
      }

      String[] columnsList = StringUtils.splitPreserveAllTokens(new String(Snappy.uncompress(rows, 0, rows.length), "UTF-8"), "\t");
      for (int i = startIdx; i < endIdx; i++) {
        // 既に除外しない列と判断されている場合は処理を飛ばす
        if (notDelIndex.contains(i)) {
          continue;
        }

        try {
          // タイトルから除外対象外か判断（2016.09.27 小計行はデータがなくても表示するケースに対応して追加）
          if (option != null && option.containsKey(DefineReport.ID_PARAM_OPT_TITLE) && ArrayUtils.isNotEmpty(notDeKeys)) {
            JSONArray titArray = option.getJSONArray(DefineReport.ID_PARAM_OPT_TITLE);
            if (StringUtils.endsWithAny(titArray.optString(i), notDeKeys)) {
              // 除外対象外
              throw new Exception();
            }
          }

          if (StringUtils.isNotEmpty(columnsList[i]) && Double.parseDouble(columnsList[i]) != 0) {
            // 実績あり
            throw new Exception();
          }
        } catch (Exception e) {
          // 実績あり（値が0でない or 文字列が入っている）
          int grpNo = (i - startIdx) / loopCnt; // 小数点以下切捨て
          int grpTop = grpNo * loopCnt + startIdx; // グループ先頭の列インデックス
          for (int j = 0; j < loopCnt; j++) {
            notDelIndex.add(grpTop + j);
          }
        }
      }
    }

    // 除外後のデータ格納
    ArrayList<byte[]> newData = new ArrayList<byte[]>();

    new StringBuffer();
    Iterator<byte[]> itr = al.iterator();
    while (itr.hasNext()) {
      ArrayList<String> cols = new ArrayList<String>();

      // セル（列）情報リスト
      byte[] bytes = itr.next();
      String[] columnsList = StringUtils.splitPreserveAllTokens(new String(Snappy.uncompress(bytes, 0, bytes.length), "UTF-8"), "\t");
      index = -1;

      for (String col : columnsList) {
        index++;

        if (index < startIdx || index >= endIdx || notDelIndex.contains(index)) {
          cols.add(col);

        }
      }

      newData.add(Snappy.compress(StringUtils.join(cols.toArray(new String[cols.size()]), "\t").getBytes("UTF-8")));
    }

    // 除外前のメタデータ取得
    ArrayList<Integer> oldMetaData = (ArrayList<Integer>) session.getAttribute(DefineReport.ID_SESSION_META);

    // 除外後のメタデータ格納
    ArrayList<Integer> newMetaData = new ArrayList<Integer>();

    Iterator<Integer> itrMeta = oldMetaData.iterator();
    index = -1;
    while (itrMeta.hasNext()) {
      index++;

      if (index < startIdx || index >= endIdx || notDelIndex.contains(index)) {
        newMetaData.add(itrMeta.next());

      } else {
        itrMeta.next();
      }
    }

    if (option != null && option.containsKey(DefineReport.ID_PARAM_OPT_TITLE)) {
      // 除外後のメタデータ格納
      JSONObject newOption = new JSONObject();

      Iterator<String> oit = option.keySet().iterator();
      while (oit.hasNext()) {
        String key = oit.next();
        if (StringUtils.equals(key, DefineReport.ID_PARAM_OPT_TITLE)) {
          ArrayList<String> titleList = new ArrayList<String>();
          JSONArray titArray = option.getJSONArray(DefineReport.ID_PARAM_OPT_TITLE);
          for (index = 0; index < endIdx; index++) {
            // 既に除外しない列と判断されている場合は処理を飛ばす
            if (index < startIdx || index >= endIdx || notDelIndex.contains(index)) {
              titleList.add(titArray.getString(index));
            }
          }
          newOption.put(key, titleList.toArray(new String[titleList.size()]));

        } else {
          newOption.put(key, option.get(key));
        }
      }
      session.setAttribute(DefineReport.ID_SESSION_OPTION, newOption);
    }

    // セッション保持
    session.setAttribute(DefineReport.ID_SESSION_TABLE, newData);
    session.setAttribute(DefineReport.ID_SESSION_META, newMetaData);

    return newData;
  }
}
