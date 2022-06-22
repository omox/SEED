<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="MS932" %>
<%@ page import="authentication.defines.*" %>
<%@ page import="authentication.validation.*" %>
<%@ page import="authentication.bean.User" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html lang="ja">
<head>
<META http-equiv="Content-Type" content="text/html; charset=UTF-8">
<META http-equiv="Content-Script-Type" content="text/javascript">
<%
	// Web.xml‚©‚çŽžŠÔ‚ðŽæ“¾
	String fromData = application.getInitParameter(Consts.FROM_DATA);
	String toData = application.getInitParameter(Consts.TO_DATA);

	// ŽžŠÔŠO‚©‚Ç‚¤‚©‚Ì”»’è‚ðs‚¤
	LoginValidation vld = new LoginValidation();
	String injp = vld.closeTime(fromData, toData, "");
	String includejsp = application.getInitParameter(injp);
%>
</head>

<jsp:include page="<%=includejsp %>" flush="true" />

</html>