<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="MS932"%>
<%@ page import="common.Defines" %>
<!DOCTYPE HTML PUBLIC "-//w3c//dtd html 4.0 transitional//en">
<html>
<head>
<title>�G���[���</title>
<meta http-equiv="Content-Style-Type" content="text/css">
<link href="../css/report.css" rel="stylesheet" type="text/css">
</head>
<body>
<div id=view>
	<h4>�V�X�e���G���[���</h4>
	<p>�V�X�e���G���[���������܂���</p>
	<%
	if(request.getAttribute(Defines.STR_FRM_MSG) != null) {
	%>
		<p class=<%= request.getAttribute(Defines.STR_FRM_MSGTYPE).toString() %>><%=request.getAttribute(Defines.STR_FRM_MSG).toString()%></p>
	<%
	}
	%>
</div>
</body>
</html>
