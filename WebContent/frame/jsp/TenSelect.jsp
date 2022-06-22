<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="MS932" %>
<%@ page import="java.util.*" %>
<%@ page import="common.DefineReport" %>
<%@ page import="authentication.defines.*" %>
<%@ page import="authentication.bean.*" %>
<%@ page import="authentication.dbaccess.DBinfo" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
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
<html lang="ja">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<link rel="stylesheet" type="text/css" href="../css/common.css" >
	<link HREF="../frame/css/login.css" rel="stylesheet" type="text/css">
	<title>ログイン画面</title>

<link rel="stylesheet" type="text/css" href="../themes/easyui/default/easyui.css?v=<%=prm %>">
<link rel="stylesheet" type="text/css" href="../themes/easyui/default/custom_easyui.css?v=<%=prm %>">
<link rel="stylesheet" type="text/css" href="../themes/easyui/icon.css?v=<%=prm %>">
</head>
<body>
<!-- ***** container ***** -->
<div id="container">

	<!-- ===  header === -->
	<div id="header">
		<jsp:include page="header.jsp" flush="true" />
	</div>
	<!-- ===  /header=== -->

	<!-- ===  content === -->
	<div id="content">
		<div id="cc" class="easyui-layout" data-options="fit:true">
			<form id="login2" method="post" action="../Servlet/Login2.do?MenuKbn=6">
					<h2 style="margin-bottom:20px;">ログイン画面</h2>
					<table class="t-layout1" style = "border-spacing:20px 30px;font-weight: bold;">
						<tr>
							<td>
								<input class="easyui-combobox" tabindex="1" id="<%=DefineReport.Select.TENPO.getObj()%>" name="<%=DefineReport.Select.TENPO.getObj()%>" data-options="label:'担当店舗',labelWidth:80,panelHeight:400" style="height:20px; width:300px; ">
							</td>
							<td>
								<a class="easyui-linkbutton" tabindex="2" id="btn_search" style="width:100px;"><span>選択</span></a>
							</td>
						</tr>
					</table>
			</form>
		</div>
	</div>
</div>
</body>
<script type="text/javascript" src="../js/jquery.min.js"></script>
<script type="text/javascript" src="../js/jquery.easyui.min.js"></script>
<script type="text/javascript" src="../js/jquery.info.control.js"></script>

<script type="text/javascript" src="../js/json2.min.js"></script>			<!-- json plugin -->
<script type="text/javascript" src="../js/exdate.js"></script>				<!-- exdate plugin -->

<script type="text/javascript" src="../js/jshashtable-2.1.js"></script>		<!-- jshashset plugin -->
<script type="text/javascript" src="../js/jquery.numberformatter.min.js"></script><!-- numberformatter plugin -->

<script type="text/javascript" src="../js/shortcut.js"></script>			<!-- shortcut plugin -->

<!-- Report Option & Control & Event  -->
<script type="text/javascript" src="../frame/js/TenSelect.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.control.js?v=<%=prm %>"></script>

<!-- ***** javaScript ***** -->
<script type="text/javascript">

	$(function(){
		$.report($.reportOption);

		var that = this

		// マスク追加
		$.appendMask();

		var reportNumber = "TenSelect"
		$.report[0].initialize(reportNumber);

		(function() {
			// マスク追加
			$.appendMask();

		});
	});
</script>
<style type="text/css">
	td { height: 25px; }

	input {
		height: 18pt;
		font-size: 10pt;

	}
</style>
</html>