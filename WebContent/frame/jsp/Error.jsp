<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="MS932" %>
<%@ page import="authentication.defines.*" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html lang="ja">
<head>
<META http-equiv="Content-Type" content="text/html; charset=UTF-8">
<META http-equiv="Content-Script-Type" content="text/javascript">
<title>�G���[���</title>
<jsp:include page="common_head.jsp" flush="true" />
<LINK HREF="../css/login.css" REL="stylesheet" TYPE="text/css">

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
<H2>�V�X�e���G���[���</H2>
	<p>�V�X�e���G���[���������܂���</p>
	<%if(request.getAttribute(Form.COMMON_MSG) != null){%>
	<p class="<%= request.getAttribute(Form.COMMON_MSGTYPE).toString() %>">
	<%=request.getAttribute(Form.COMMON_MSG).toString()%>
	</p>
	<%}%>
</div>
<!-- ===  /content === -->

<!-- ===  footer === -->
<div id="footer">
<%-- <jsp:include page="footer.jsp" flush="true" /> --%>
</div>
<!-- === /footer === -->

</div>
<!-- ***** /container ***** -->
</body>
</html>
