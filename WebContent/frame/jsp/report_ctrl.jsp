<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="MS932"%>
<%@ page import="java.util.ArrayList" %>
<%@ page import="authentication.bean.Side" %>
<%@ page import="authentication.bean.Report" %>
<%@ page import="authentication.defines.*" %>

<%
	// メニューリストと分類番号
	ArrayList menu = (ArrayList)session.getAttribute(Consts.STR_SES_REPSIDE);
	int repside = Integer.parseInt(request.getParameter(Form.REPORT_SIDE));

	// メニュー分類番号と一致するレポートリスト取得
	ArrayList report = null;
	for(int i = 0; i < menu.size(); i++){
		Side m = (Side)menu.get(i);
		if (m.getSide() == repside) {
			report = m.getReport();
			break;
		}
	}

	// 現行のレポート番号取得
	String current = ((String)request.getParameter(Form.REPORT_NO));
	String currentno = current!=null?current:"0";

	// 該当レポートの情報取得
	Report rep=null;
	for(int i = 0; i< report.size(); i++){
		Report r = (Report)report.get(i);
		if (r.getReport_boolean()){
			if (currentno.equals(Long.toString(r.getReport_no()))){
				rep=r;
				break;
			}
		}
	}

	if (rep != null) {
		String reportjsp = "/jsp/" + rep.getReport_jsp() + ".jsp";
%>
		<jsp:include page="<%=reportjsp%>" flush="true"></jsp:include>
<%
	}
%>