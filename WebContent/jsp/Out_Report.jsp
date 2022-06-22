<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="MS932" %>
<%
	String src = request.getContextPath() + "/Servlet/ReportContent.do";
%>
<!--[if IE]>
<iframe scrolling="yes" frameborder="0" src="<%=src %>" style="width:100%; height:100%;"></iframe>
<![endif]-->

<!--[if !IE]> <-->
<object type="text/html" data="<%=src %>" style="width:100%; height:100%;">
<p>non object</p>
</object>
<!--> <![endif]-->

<iframe scrolling="yes" frameborder="0" src="" name="blank" style="width:0; height:0;"></iframe>
