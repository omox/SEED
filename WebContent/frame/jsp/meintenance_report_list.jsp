<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="MS932" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="authentication.bean.Side" %>
<%@ page import="authentication.bean.Report" %>
<%@ page import="authentication.defines.*" %>
<%@ page import="authentication.defines.Defines" %>

<%
	ArrayList mstr = (ArrayList)session.getAttribute(Consts.STR_SES_MSTSIDE);
	int repside = Integer.parseInt(request.getParameter(Form.REPORT_SIDE));

	ArrayList report = null;
	for(int i = 0; i < mstr.size(); i++){
		Side m = (Side)mstr.get(i);
		if (m.getSide()==repside) {
	report = m.getReport();
		}
	}

	String mmurl = null;

	// メンテナンス表示
	for(int i = 0; i< report.size(); i++){
		Report rep = (Report)report.get(i);
	
		if(rep.getReport_no() == -2){
			// ユーザマスタ
			mmurl = "javascript:submit_forward('../Servlet/MaintenanceUser.do?"+ Form.REPORT_NAME +"="+ rep.getReport_no() +"','"+ Form.MTN_SIDE + "','"+ Consts.SYSTEM_MENTENANCE +"');";

		}else if(rep.getReport_no()== -3){
			// ユーザレポート管理
			mmurl = "javascript:submit_forward('../Servlet/MaintenanceUserReport.do?"+ Form.REPORT_NAME +"="+ rep.getReport_no() +"','"+ Form.MTN_SIDE + "','"+ Consts.SYSTEM_MENTENANCE +"');";

		}else if(rep.getReport_no()== -4){
			// レポート管理
			mmurl = "javascript:submit_forward('../Servlet/MaintenanceReportAuth.do?"+ Form.REPORT_NAME +"="+ rep.getReport_no() +"','"+ Form.MTN_SIDE + "','"+ Consts.SYSTEM_MENTENANCE +"');";

		}else if(rep.getReport_no()== -5){
			// グループマスタ
			mmurl = "javascript:submit_forward('../Servlet/MaintenanceGroupsMst.do?"+ Form.REPORT_NAME +"="+ rep.getReport_no() +"','"+ Form.MTN_SIDE + "','"+ Consts.SYSTEM_MENTENANCE +"');";			

		}else if(rep.getReport_no()== -6){
			// ロールマスタ
			mmurl = "javascript:submit_forward('../Servlet/MaintenancePosmst.do?"+ Form.REPORT_NAME +"="+ rep.getReport_no() +"','"+ Form.MTN_SIDE + "','"+ Consts.SYSTEM_MENTENANCE +"');";

		}else if(rep.getReport_no()== -7){
			// レポート分類マスタ
			mmurl = "javascript:submit_forward('../Servlet/MaintenanceReportSide.do?"+ Form.REPORT_NAME +"="+ rep.getReport_no() +"','"+ Form.MTN_SIDE + "','"+ Consts.SYSTEM_MENTENANCE +"');";

		}else if(rep.getReport_no()== -8){
			// レポートマスタ
			mmurl = "javascript:submit_forward('../Servlet/MaintenanceReportName.do?"+ Form.REPORT_NAME +"="+ rep.getReport_no() +"','"+ Form.MTN_SIDE + "','"+ Consts.SYSTEM_MENTENANCE +"');";

		} else if (rep.getReport_no() == -10) {
			// お知らせマスタ
			mmurl = "javascript:submit_forward('../Servlet/MaintenanceInfo.do?"+ Form.REPORT_NAME +"="+ rep.getReport_no() +"','"+ Form.MTN_SIDE + "','"+ Consts.SYSTEM_MENTENANCE +"');";
		
		} else if (rep.getReport_no() == -11) {
			// お知らせ管理
			mmurl = "javascript:submit_forward('../Servlet/MaintenanceInfoAuth.do?"+ Form.REPORT_NAME +"="+ rep.getReport_no() +"','"+ Form.MTN_SIDE + "','"+ Consts.SYSTEM_MENTENANCE +"');";
		}
%>
		<li>
			<a href="<%= mmurl %>"><%= rep.getReport_name() %></a>
		</li>
<%
	}
%>
