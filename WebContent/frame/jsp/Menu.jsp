<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="MS932" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="authentication.bean.*" %>
<%@ page import="authentication.defines.*" %>
<%@ page import="authentication.defines.Defines" %>
<%@ page import="authentication.dbaccess.DBinfo" %>

<%
	User lusr = (User)request.getSession().getAttribute(Consts.STR_SES_LOGINUSER);
	ArrayList menu = (ArrayList)request.getSession().getAttribute(Consts.STR_SES_REPSIDE);
	ArrayList rowdisp = (ArrayList)request.getSession().getAttribute("DISP");
	ArrayList mstr = (ArrayList)request.getSession().getAttribute(Consts.STR_SES_MSTSIDE);
	String repside = (String)request.getParameter(Form.REPORT_SIDE);
	String info = (String)request.getAttribute(Form.MENU_INFO_BTN);

	//パスワード有効期限警告メッセージ取得
	String pw_warning_msg = request.getAttribute(Defines.STR_SES_PASS_TERM_MSG) != null ? (String)request.getAttribute(Defines.STR_SES_PASS_TERM_MSG) : "";
	//初期化
	request.removeAttribute(Defines.STR_SES_PASS_TERM_MSG);
	// jsキャッシュ対応
	String prm = request.getSession().getId();
%>

<html>
<head>
<meta http-equiv="X-UA-Compatible" content="IE=edge" />
<meta http-equiv="Pragma" content="no-cache">
<meta http-equiv="Cache-Control" content="no-cache">
<meta http-equiv="Expires" content="Thu, 01 Dec 1994 16:00:00 GMT">
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta http-equiv="Content-Script-Type" content="text/javascript">
<title>メニュー</title>

<script type="text/javascript">
//パスワード有効期限警告メッセージ
	var msg_pw_warning="<%= pw_warning_msg %>";
	window.onload = function setFormat(){
		//パスワード警告メッセージ表示
		if (msg_pw_warning != ""){
			alert(msg_pw_warning);
		}
	}
function submit_forward(action,nm,val){
	var frm = document.forms[0];
 	var query = document.createElement("input");
	query.type = "hidden";
	query.name = nm;
	query.value = val;
	frm.appendChild(query);
	frm.action = action;

	try {
		frm.submit();
	} catch(e) {
		if (e.number == -2147024891) {
			alert("ファイル名をご確認下さい。");
		}
	}
}
</script>

<link rel="stylesheet" type="text/css" href="../css/common.css" >
<link rel="stylesheet" type="text/css" href="../frame/css/menu.css" />

<link rel="stylesheet" type="text/css" href="../themes/easyui/default/easyui.css">
<link rel="stylesheet" type="text/css" href="../themes/easyui/icon.css">
<link rel="stylesheet" type="text/css" href="../css/easyui_custom.css">
</head>

<body background="../frame/img/back.gif">
<!-- ***** container ***** -->
<div id="container">

<!-- ===  header === -->
<div id="header">
	<jsp:include page="header.jsp" flush="true" />
</div>
<!-- ===  /header=== -->

<!-- ===  content === -->
<div id="content">
<form id="frm_repout" name="frm_repout" method="post">
	<h2>メニュー</h2>

	<%--  エラーメッセージ領域 --%>
<%
if(request.getAttribute(Form.COMMON_MSG) != null){
%>
	<p class=<%= request.getAttribute(Form.COMMON_MSGTYPE).toString() %>>
		<%=request.getAttribute(Form.COMMON_MSG).toString()%>
	</p>
<%
}
%>
	<%-- /エラーメッセージ領域 --%>

	<div id="menu">

<%
boolean flgMenu = false;
boolean flgMst = false;
if(rowdisp != null && rowdisp.size() > 0){
	for(int i = 0; i < rowdisp.size(); i++){
		try{
			if(Integer.parseInt(rowdisp.get(i).toString()) > 100){
				// 列番号が100より大きい場合は管理者用メニュー側に表示
				flgMst = true;
			} else {
				// 列番号が100以下の場合は各種レポート側に表示
				flgMenu = true;
			}
		}catch(Exception ex){
		}
	}
}
if (flgMenu == false){
%>
	<div class="m_child">
		<h3>本システムを利用することは出来ません</h3>
		<p style="height:300px;">各種ﾚﾎﾟｰﾄを閲覧する権限が与えられていません。</p>
	</div>
<%
} else {
%>
	<div id="menu_report" class="m_child_max">
		<h3>各種レポート</h3>
		<p>閲覧したいレポートを選択して下さい。</p>
<%
String columnNo="";
String columnNoOld="";
for(int i = 0;i<rowdisp.size();i++){
	// 列番号の取得
	columnNo=rowdisp.get(i).toString();
	// 列番号が100より大きい場合は書き込まない
	try{
		if(Integer.parseInt(columnNo) > 100){
			continue;
		}
	}catch(Exception ex){
		continue;
	}
	// 現行の列番号と前回の列番号を比較して異なる場合、列用のメニューを書き込む
	if(!columnNoOld.equals(columnNo)){
%>
<%-- 大分類レポートの設定と行切り替え--%>
	</div>
	<div id="menu_report" class="m_child">
	<%--// 大分類レポートの設定と行切り替え--%>
<%
	}
	int no_len = 0;
	ArrayList report = null;
	no_len = i;
	Side m = (Side)menu.get(i);
	report = m.getReport();

	for(int k = 0; k< report.size() ;k++){
		Report rep = (Report)report.get(k);
		if(rep.getReport_boolean()){
			//reportsideが同じものを出力
			if(i == no_len ){

%>
<ul id="menu_report" class="easyui-tree" style="width:100%;float:left;">
	<li>
	<span>
		<a class="enableMenu" href="../Servlet/Report.do?<%= Form.REPORT_SIDE %>=<%= m.getSide() %>
			&<%= Form.REPORT_SIDE_ARRAY %>=<%= i %> "><%= m.getSidename() %><!-- 列番号：<%=rowdisp.get(i)%>  -->
		</a>
<!--
		<a href="#" class="enableMenu"> <%= m.getSidename() %> </a>
-->
	</span>
	<ul>
<%
				no_len = no_len +1;
			}
		}//レポートに値があるか判定しているif文
	}//report.sizeのfor文
%>
		<jsp:include page="report_list.jsp">
			<jsp:param value="<%= Integer.toString(m.getSide()) %>" name="ReportSide" />
			<jsp:param value="FullName" name="ReportNameType" />
			<jsp:param value="<%=i %>" name="ReportSide_array" />
			<jsp:param value="1" name="HiddenReport" />
		</jsp:include>
	</ul>
	</li>
</ul>
<%
	// 現行の列番号を保持する
	columnNoOld = columnNo;
} // for 大外
%>
</div>
<%
}
if ((mstr != null && mstr.size() > 0) || flgMst == true){
%>
	<div id="menu_system" class="m_child_max">
		<h3>管理者用メニュー</h3>
		<p>当システムの情報を閲覧する事ができます。</p>
<%
if (mstr != null && mstr.size() > 0){
%>
	</div>
	<div id="menu_report" class="m_child">
	<ul id="menu_system" class="easyui-tree" style="width:100%;float:left;">
		<li>
<%
	int no_len = 0;
	ArrayList report = null;
	for(int i=0; i < mstr.size(); i++){
		no_len = i;
		Side m = (Side)mstr.get(i);
		report = m.getReportMst();
		for(int k = 0; k< report.size() ;k++){
			//reportsideが同じものを出力
			if(i == no_len){
%>
		<span>
			<!-- グループマスタ固定 -->
			<a href="javascript:submit_forward('../Servlet/MaintenanceGroupsMst.do?Report=-5','MaintenanceSide','-1');"><%= m.getSidename() %></a>
		</span>
		<ul>
<%
				no_len = no_len +1;
			}
		}	//report.sizeのfor文
%>
		<jsp:include page="meintenance_report_list.jsp">
			<jsp:param value="<%= Integer.toString(m.getSide()) %>" name="ReportSide" />
			<jsp:param value="FullName" name="ReportNameType" />
		</jsp:include>
<%
	}
%>
		</ul>
		</li>
	</ul>
<%
}
if (flgMst == true){
String columnNo="";
String columnNoOld="";
for(int i = 0;i<rowdisp.size();i++){
	// 列番号の取得
	columnNo=rowdisp.get(i).toString();
	// 列番号が100以下の場合は書き込まない
	try{
		if(Integer.parseInt(columnNo) <= 100){
			continue;
		}
	}catch(Exception ex){
		continue;
	}
	// 現行の列番号と前回の列番号を比較して異なる場合、列用のメニューを書き込む
	if(!columnNoOld.equals(columnNo)){
%>
<%-- 大分類レポートの設定と行切り替え--%>
	</div>
	<div id="menu_report" class="m_child">
	<%--// 大分類レポートの設定と行切り替え--%>
<%
	}
	int no_len = 0;
	ArrayList report = null;
	no_len = i;
	Side m = (Side)menu.get(i);
	report = m.getReport();

	for(int k = 0; k< report.size() ;k++){
		Report rep = (Report)report.get(k);
		if(rep.getReport_boolean()){
			//reportsideが同じものを出力
			if(i == no_len){
%>
<ul id="menu_report" class="easyui-tree" style="width:100%;float:left;">
	<li>
	<span>
		<a class="enableMenu" href="../Servlet/Report.do?<%= Form.REPORT_SIDE %>=<%= m.getSide() %>
			&<%= Form.REPORT_SIDE_ARRAY %>=<%= i %> "><%= m.getSidename() %><!-- 列番号：<%=rowdisp.get(i)%>  -->
		</a>
	</span>
	<ul>
<%
				no_len = no_len +1;
			}
		}//レポートに値があるか判定しているif文
	}//report.sizeのfor文
%>
		<jsp:include page="report_list.jsp">
			<jsp:param value="<%= Integer.toString(m.getSide()) %>" name="ReportSide" />
			<jsp:param value="FullName" name="ReportNameType" />
			<jsp:param value="<%=i %>" name="ReportSide_array" />
			<jsp:param value="1" name="HiddenReport" />
		</jsp:include>
	</ul>
	</li>
</ul>
<%
	// 現行の列番号を保持する
	columnNoOld = columnNo;
} // for 大外
}
%>
</div>
<%
}
%>
</div>
</form>
</div>
<!-- ===  /content === -->

<!-- ===  footer === -->
<div id="footer">
</div>
<!-- === /footer === -->

</div>
<!-- ***** /container ***** -->
</body>
</html>
<script type="text/javascript" src="../js/jquery.min.js"></script>
<script type="text/javascript" src="../js/jquery.easyui.min.js"></script>
<script type="text/javascript" src="../js/jquery.info.control.js?v=<%=prm %>"></script>