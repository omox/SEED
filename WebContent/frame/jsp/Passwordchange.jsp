<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="MS932"%>
<%@ page import="java.util.*" %>
<%@ page import="authentication.defines.*" %>
<%@ page import="authentication.bean.*" %>
<% User lusr = (User)session.getAttribute(Consts.STR_SES_LOGINUSER);%>
<html>
<head>
<meta http-equiv="X-UA-Compatible" content="IE=edge" />
<meta http-equiv="Pragma" content="no-cache">
<meta http-equiv="Cache-Control" content="no-cache">
<meta http-equiv="Expires" content="Thu, 01 Dec 1994 16:00:00 GMT">
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta http-equiv="Content-Script-Type" content="text/javascript">

<META http-equiv="Content-Style-Type" content="text/css">
<title>�p�X���[�h�ύX</title>
<jsp:include page="common_head.jsp" flush="true" />
<LINK HREF="../frame/css/passwordchange.css" REL="stylesheet" TYPE="text/css">
<link rel="stylesheet" type="text/css" href="../themes/easyui/default/easyui.css">
<link rel="stylesheet" type="text/css" href="../themes/easyui/icon.css">
</head>

<body onLoad="document.getElementById('<%= Form.MTN_OLD_PASS %>').focus()">
<!-- ***** container ***** -->
<div id="container">

	<!-- ===  header === -->
	<div id="header">
		<jsp:include page="header.jsp" flush="true" />
	</div>
	<!-- ===  /header=== -->


	<!-- ===  content === -->
	<div id="content">

		<form id="m_user" method="post" action="../Servlet/PasswordChange.do">
			<h2>�p�X���[�h�ύX</h2>
			<%--  �G���[���b�Z�[�W�̈� --%>
			<%if(request.getAttribute(Form.COMMON_MSG) != null){%>
			<p class=<%= request.getAttribute(Form.COMMON_MSGTYPE).toString() %>><%=request.getAttribute(Form.COMMON_MSG).toString()%></p><%}%>
			<%-- /�G���[���b�Z�[�W�̈� --%>

			<div id="entry">
				<h3>�����̓G���A</h3>
				<p class="memo">�p�X���[�h��ύX�E�X�V���邱�Ƃ��ł��܂��B</p>
				<fieldset class="tbl">
					<legend>User������͂��ĉ������B</legend>
					<div class="inpsection">
						<label for="<%=Form.MTN_USR_ETY_CD %>" class="required">���[�U�[ID</label>
						<%=lusr.getId() %>
						<br class="clear">
					</div>
				</fieldset>
				<fieldset class="tbl">
					<div class="inpsection">
						<label for="<%=Form.MTN_OLD_PASS %>" class="required">���p�X���[�h</label>
						<input class="text1" type="password" id="<%= Form.MTN_OLD_PASS %>" name="<%= Form.MTN_OLD_PASS %>">
						<br class="clear">
					</div>
				</fieldset>
				<fieldset class="tbl">
					<div class="inpsection">
						<label for="<%=Form.MTN_NEW_PASS %>" class="required">�V�p�X���[�h</label>
						<input class="text1" type="password" id="<%= Form.MTN_NEW_PASS %>" name="<%= Form.MTN_NEW_PASS %>">
						<br class="clear">
					</div>
				</fieldset>
				<fieldset class="commands">
					<input type="button" name="<%= Form.MTN_SAVBTN %>" value="�ۑ�" onclick="if(confirm('�ۑ����Ă�낵���ł����H')){submit_forward('../Servlet/PasswordChange.do','<%= Form.MTN_INPAREA %>','<%= Form.MTN_SAVBTN %>')}">
					<!--  onkeypress="if(confirm('�ۑ����Ă�낵���ł����H')){submit_forward('../Servlet/PasswordChange.do','<%= Form.MTN_INPAREA %>','<%= Form.MTN_SAVBTN %>')}">-->
				</fieldset>
				<br class="clear">
			</div>
		</form>
	</div>
</div>
</body>
</html>
<script type="text/javascript" src="../js/jquery.min.js"></script>
<script type="text/javascript" src="../js/jquery.easyui.min.js"></script>