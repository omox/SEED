<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="MS932" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="authentication.bean.Side" %>
<%@ page import="authentication.bean.Report" %>
<%@ page import="authentication.bean.User" %>
<%@ page import="authentication.defines.*" %>
<div id="tabs">
<%
	// ユーザ情報取得※CD_AUTHの値を追加（nullの場合は、タブ表示）
	User lusr = (User)session.getAttribute(Consts.STR_SES_LOGINUSER);
	String initParamName = "";
	if (lusr.getCd_auth_() != null) {
		initParamName += lusr.getCd_auth_();
	}

	// メニューリストと分類番号
	ArrayList menu = (ArrayList)session.getAttribute(Consts.STR_SES_REPSIDE);
	int repside = Integer.parseInt(request.getParameter(Form.REPORT_SIDE).toString().trim());

	// メニュー分類番号と一致するレポートリスト取得
	ArrayList report = null;
	for(int i = 0; i < menu.size(); i++){
		Side m = (Side)menu.get(i);
		if (m.getSide() == repside) {
			report = m.getReport();
		}
	}

	// 現行のレポート番号取得
	String current = ((String)request.getParameter(Form.REPORT_NO));
	String currentno = current!=null?current:"0";

	// 表示形式
	String repview = ((String)request.getParameter(Form.REPORT_NAMETYPE));
	String no = ((String)request.getParameter(Form.REPORT_SIDE_ARRAY));
	String HiddenReport = (request.getParameter("HiddenReport") == null) ? "" :((String)request.getParameter("HiddenReport"));

	// レポート名称
	String reportName = "";
	// パラメーター文
	String parameterURL = "";
	String href="";
	int tabSelected=0;

	String hostname = request.getRequestURL().toString();
	String context = request.getContextPath();
	hostname = "";//hostname.substring(0, hostname.indexOf(context)); 2015/04/07 comment out by omoto

	// 表示処理
	for(int i = 0; i< report.size(); i++){
		Report rep = (Report)report.get(i);

		if (rep.getReport_boolean()) {

			// 表示名称の設定
			if (repview == null) {
				reportName = rep.getReport_shortname();
			} else {
				reportName = rep.getReport_name();
			}

			parameterURL = hostname + context + "/Servlet/Report.do?" +
				Form.REPORT_SIDE_ARRAY + "="  + no + "&" +
				Form.REPORT_NO_ARRAY + "=" + Integer.toString(i) + "&" +
				Form.REPORT_SIDE + "=" + rep.getReport_side() + "&" +
				Form.REPORT_NO + "=" + rep.getReport_no();

			// カスタムプロパティの確認
			String[] inclurepnames = rep.getReport_custom().toString().split(",");

			String parentReportName = rep.getReport_custom();
			if (inclurepnames.length >= 2) {
				// 呼出元（戻り先）の指定
				parentReportName = inclurepnames[1];
			}

			// 非表示タブ設定
			String inclurepname = (String)request.getSession().getAttribute(Defines.ID_REQUEST_JSP_REPORT);
			String classHide = "";
			if (inclurepnames.length >= 3) {
				// 表示/非表示(hide)の指定
				classHide = inclurepnames[2];
			}
			if(!rep.getEnableMenu()){
				// 表示/非表示(hide)の指定
				classHide = "hide";
			}

			if (currentno.equals(Integer.toString(rep.getReport_no()))) {
				if ("".equals(classHide)) {
					href += ","+parameterURL;
				} else {
					href += ","+parameterURL;	// (2013/01/24 変更※動作問題ない場合は削除予定) href += ",#";

					reportName = "　　";
				}
				classHide = "";// カレントタブは常時表示
				// href 有効
				parameterURL = hostname + context + "/jsp/Out_Report.jsp";
				// カレントタブ番号
				tabSelected = i ;
			} else {
				href += ","+parameterURL;
				// href 無効
				parameterURL = "";
			}

			// メニュー表示時(HiddenReport=1)に非表示指定の場合は、追加しない。
			// タブ表示時(HiddenReport=0)は、常に追加する。
			// レポートON/OFF設定確認
			if ((("".equals(classHide) && "1".equals(HiddenReport)) || ("0".equals(HiddenReport)))) {
%>
				<div id="parent_<%=parentReportName %>"
					title="<%=reportName %>"
					class="<%=classHide %>"
					href="<%=parameterURL %>"
					cache=false
					closable=false
					style="overflow:hidden">
				</div>
<%
			}
		}
	}
%>
</div>
<input type="hidden" id="tabSelected" name="tabSelected" value="<%=tabSelected %>"/>
<input type="hidden" id="tabContent" name="tabContent" value="<%=href %>"/>
<input type="hidden" id="tabView" name="tabView" value="<%=initParamName %>"/>
<script type="text/javascript" src="../js/jquery.report.tabs.js"></script>