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
<link rel="stylesheet" type="text/css" href="../css/style.css" media="screen" charset="utf-8" />

<link rel="stylesheet" type="text/css" href="../css/jquery.jOrgChart.css" />
<link rel="stylesheet" type="text/css" href="../css/custom.css" />

<link rel="stylesheet" type="text/css" href="../themes/easyui/default/easyui.css">
<link rel="stylesheet" type="text/css" href="../themes/easyui/icon.css">

<link rel="stylesheet" type="text/css" href="../frame/css/menu.css" />

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
if (menu==null || menu.size()==0) {
%>
	<div class="m_child">
		<h3>本システムを利用することは出来ません</h3>
		<p style="height:300px;">各種ﾚﾎﾟｰﾄを閲覧する権限が与えられていません。</p>
	</div>
<%
} else {
%>
	<div id="menu_report" class="m_child_max">
		<h3>レポート</h3>
		<table>
		<tr>
			<td><p>閲覧したいレポートを選択して下さい。</p></td>

			<td><a id="bt_store"  href="#" class="easyui-linkbutton" plain="false" iconCls="icon-save">保存</a></td>
			<td><a id="bt_delete" href="#" class="easyui-linkbutton" plain="false" iconCls="icon-cancel">削除</a></td>
			<td><a href="#" class="easyui-linkbutton" plain="true"><label for="on_off">ロック</label></a></td>
			<td><input type="checkbox" id="on_off" checked="checked"/></td>
		</tr>
		</table>
	</div>
	<div id="chart" class="orgChart"></div>

	<ul id="org" style="display:none">	<!-- jOrgChart Start -->
	<li><img src="../frame/img/_logo.gif" alt="ロゴ"/>
	<ul>
<%
long di =0;
String columnNo="";
String columnNoOld="";
for(int i = 0;i<rowdisp.size();i++){
	// 列番号の取得
	columnNo=rowdisp.get(i).toString();
	// 現行の列番号と前回の列番号を比較して異なる場合、列用のメニューを書き込む

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
	<li>
		<a href="../Servlet/Report.do?<%= Form.REPORT_SIDE %>=<%= m.getSide() %>
		&<%= Form.REPORT_SIDE_ARRAY %>=<%= i %> "><%= m.getSidename() %>
		<!-- 列番号：<%=rowdisp.get(i)%>  -->
		</a>
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
<%
	// 現行の列番号を保持する
	columnNoOld = columnNo;
} // for 大外

// 管理者用メニュー
if(mstr!=null && mstr.size()!=0){
%>
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
		<a><%= m.getSidename() %></a>
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
	</li>	<!-- 管理者用メニュー　End -->
<%
}
%>

</ul>

</li>
</ul>	<!-- jOrgChart End -->
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

<script type="text/javascript" src="../js/jquery.jOrgChart.js"></script>
<script type="text/javascript" src="../js/iphone-style-checkboxes.js"></script>
<script type="text/javascript" src="../js/jquery.easyui.min.js"></script>

<script type="text/javascript" src="../js/jquery.info.control.js"></script>
<script type="text/javascript" src="../js/jquery.menu.control.js"></script>

<script type="text/javascript" src="../js/jquery-ui-1.8.16.custom.min.js"></script>