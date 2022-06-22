<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="MS932" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="authentication.bean.Side" %>
<%@ page import="authentication.bean.Report" %>
<%@ page import="authentication.defines.*" %>

<%
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

			if (currentno.equals(Integer.toString(rep.getReport_no()))) {

%>
				<li>
					<%=reportName %>
				</li>
<%
			}else{

				parameterURL = "../Servlet/Report.do?" +
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

				String classHide = "";
				if (inclurepnames.length >= 3) {
					// 表示/非表示(hide)の指定
					classHide = inclurepnames[2];
				}

				// メニュー表示時(HiddenReport=1)に非表示指定の場合は、追加しない。
				// タブ表示時(HiddenReport=0)は、常に追加する。
				if (("".equals(classHide) && "1".equals(HiddenReport)) ||("0".equals(HiddenReport))) {
					if (rep.getEnableMenu()) {	// レポートON/OFF設定確認
%>
				<li class="<%=classHide %>">
					<a href="<%=parameterURL %>" class="jump" id="parent_<%=parentReportName %>" name="parent_<%=parentReportName %>"><%=reportName %></a>
				</li>
<%
					} else { // リンクなし
%>
				<li class="<%=classHide %>">
					<pre id="parent_<%=parentReportName %>" name="parent_<%=parentReportName %>" class="disableMenu"><%=reportName %></pre>
				</li>
<%
					}
				}
			}
		}
	}
%>