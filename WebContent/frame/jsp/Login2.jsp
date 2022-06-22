<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="MS932" %>
<%@ page import="java.util.*" %>
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
<%
	// URLにメニュー区分があったら設定
	String MenuKbn = "-1";
	if (request.getParameter(Form.PRM_MENU_KBN) != null){
		MenuKbn = request.getParameter(Form.PRM_MENU_KBN);
	}
%>

<body>
<!-- ***** container ***** -->
<div id="container">

	<!-- ===  header === -->
	<div id="header">
		<jsp:include page="header.jsp" flush="true" />
	</div>
	<!-- ===  /header=== -->

	<!-- ===  content === -->
	<div id="content"  data-options="region:'north',border:false" >
		<div id="cc" class="easyui-layout" data-options="fit:true">
			<form id="login" method="post" action="../Servlet/Login.do">
				<h2 style="margin-bottom:20px;">ログイン画面</h2>
				<table class="t-layout1" style = "border-spacing:150px 30px;">
					<tr>
						<td>
							<a href="../Servlet/Login2.do?MenuKbn=4" class="easyui-linkbutton" tabindex="1" id="btn_type_master" name="type_master" style="width:150px;" ><span>マスター</span></a>
						</td>
					</tr>
					<tr>
						<td>
							<a href="../Servlet/Login2.do?MenuKbn=5"  class="easyui-linkbutton" tabindex="2" id="btn_type_tokhat" name="type_tokhat" style="width:150px;"><span>特売・発注関連</span></a>
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

<script type="text/javascript" src="../js/json2.min.js"></script>			<!-- json plugin -->
<script type="text/javascript" src="../js/exdate.js"></script>				<!-- exdate plugin -->

<script type="text/javascript" src="../js/jshashtable-2.1.js"></script>		<!-- jshashset plugin -->
<script type="text/javascript" src="../js/jquery.numberformatter.min.js"></script><!-- numberformatter plugin -->

<script type="text/javascript" src="../js/shortcut.js"></script>			<!-- shortcut plugin -->

<!-- Report Option & Control & Event  -->
<script type="text/javascript" src="../frame/js/Login2.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.control.js?v=<%=prm %>"></script>

<!-- ***** javaScript ***** -->
<script type="text/javascript">

	$(function(){
		$.report($.reportOption);

		var that = this

		var reportNumber = "Login2"
		$.report[0].initialize(reportNumber);

		(function() {
			// マスク追加
			$.appendMask();

		});
	});
</script>
</html>