<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="MS932"%>
<%@ page import="authentication.defines.*" %>
<%@ page import="authentication.bean.*" %>

<%
	Info info = (Info) request.getAttribute(Consts.STR_REQ_REC);
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html lang="ja">
<head>
<meta http-equiv="X-UA-Compatible" content="IE=7" />
<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=UTF-8">
<META http-equiv="Content-Script-Type" content="text/javascript">
<META http-equiv="Content-Style-Type" content="text/css">


<title>‚¨’m‚ç‚¹</title>

<link rel="stylesheet" type="text/css" href="../frame/css/infoview.css" />
<link rel="stylesheet" type="text/css" href="../themes/easyui/default/easyui.css">
<link rel="stylesheet" type="text/css" href="../themes/easyui/icon.css">
<script type="text/javascript" src="../js/jquery.min.js"></script>
<script type="text/javascript" src="../js/jquery.easyui.min.js"></script>
</head>

<body>

	<div id="info_area">

		<div id="view_title">
			<b><%=info.getTitle() != null ? info.getTitle() : "" %></b>
		</div>

		<div id="view_info">
			<%=info.getInformation() != null ? info.getInformation() : "" %>
		</div>

	</div>

</body>
</html>
