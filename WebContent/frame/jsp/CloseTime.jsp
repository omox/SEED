<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="MS932" %>
<%@ page import="authentication.defines.*" %>
<%@ page import="authentication.util.*" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html lang="ja">
<head>
<META http-equiv="Content-Type" content="text/html; charset=UTF-8">
<META http-equiv="Content-Script-Type" content="text/javascript">
<%
	//web.xml���玞�ԏ��擾
	String fromData = application.getInitParameter("FROM_DATA_MSG");
	String toData = application.getInitParameter("TO_DATA_MSG");
%>
<jsp:include page="common_head.jsp" flush="true" />
<LINK HREF="../frame/css/cmn.css" REL="stylesheet" TYPE="text/css">
<LINK HREF="../frame/css/login.css" REL="stylesheet" TYPE="text/css">
<title>�����p���Ԃ̂��ē�</title>
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
	<h2>�����p���Ԃ̂��ē�</h2>
	<div id="WORK">
		�V�X�e�����p���ԊO�ł��B <br>
		�����p���Ԃ܂ł��҂��������B<br>
	</div>
</div>
<!-- ===  /content === -->

</div>
<!-- ***** /container ***** -->
</body>
</html>