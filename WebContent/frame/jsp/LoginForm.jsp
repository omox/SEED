<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="MS932" %>
<%@ page import="java.util.*" %>
<%@ page import="authentication.defines.*" %>
<%@ page import="authentication.bean.*" %>
<%@ page import="authentication.dbaccess.DBinfo" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html lang="ja">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<meta http-equiv="Content-Script-Type" content="text/javascript">
	<link rel="stylesheet" type="text/css" href="../css/common.css" >
	<link HREF="../frame/css/login.css" rel="stylesheet" type="text/css">
	<title>ログイン画面</title>
</head>

<%
	// view 指定有りの場合は、セッションクリアしない
	if (request.getParameter(Form.LOGIN_VIEW) == null){
		session.removeAttribute(Consts.STR_SES_LOGINUSER);
	}
	// URLにメニュー区分があったら設定
	String MenuKbn = "-1";
	if (request.getParameter(Form.PRM_MENU_KBN) != null){
		MenuKbn = request.getParameter(Form.PRM_MENU_KBN);
	}
%>

<body onLoad="document.getElementById('User').focus()" background="../frame/img/back.gif">
<!-- ***** container ***** -->
<div id="container">

	<!-- ===  header === -->
	<div id="header">
		<jsp:include page="header.jsp" flush="true" />
	</div>
	<!-- ===  /header=== -->

	<!-- ===  content === -->
	<div id="content">
		<form id="login" method="post" action="../Servlet/Login.do">
			<h2>ログイン画面</h2>
			<div id="entry_area">
				<h3 style="width:585px">ログイン</h3>

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

				<fieldset class="tbl" style="width:570px;">
					<legend>User情報を入力して下さい。</legend>
					<label for="<%= Form.LOGIN_USER %>"><img alt="ユーザーＩＤ" src="../frame/img/userid.png"></label>
					<input class="text1" type="text" id="User" name="User" style="width:380px;"><br>
					<label for="<%= Form.LOGIN_PASS %>"><img alt="パスワード" src="../frame/img/password.png"></label>
					<input class="text1" type="password" id="Pass" name="Pass" style="width:380px;"><br>
					<input class="btn1" type="submit" value="ログイン">
					<input type="hidden" id="<%=Form.PRM_MENU_KBN %>" name="<%=Form.PRM_MENU_KBN %>" value="<%=MenuKbn%>">
				</fieldset>
			</div>
		</form>
	</div>
	<!-- ===  /content === -->

	<!-- ===  footer === -->
	<div id="footer">
<%-- 		<jsp:include page="footer.jsp" flush="true" /> --%>
	</div>
	<!-- === /footer === -->
</div>
<!-- ***** /container ***** -->
</body>
</html>
<script type="text/javascript" src="../js/jquery.min.js"></script>
<script type="text/javascript" src="../js/jquery.easyui.min.js"></script>
<script type="text/javascript" src="../js/jquery.info.control.js"></script>