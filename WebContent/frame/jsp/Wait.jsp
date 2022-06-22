<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="MS932" %>
<%@ page import="authentication.defines.*" %>
<%@ page import="authentication.util.*" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html lang="ja">
<head>
<META http-equiv="Content-Type" content="text/html; charset=UTF-8">
<META http-equiv="Content-Script-Type" content="text/javascript">
<jsp:include page="common_head.jsp" flush="true" />
<LINK HREF="../frame/css/cmn.css" REL="stylesheet" TYPE="text/css">
<LINK HREF="../frame/css/login.css" REL="stylesheet" TYPE="text/css">
<title>メンテナンス中</title>
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
	<h2>メンテナンス中</h2>
	<div id="WORK">
		<IMG src="../frame/img/civ_005w.gif" alt="準備中" />
		<br>
		只今、システムメンテナンス中です。<br />
		メンテナンスの終了までお待ちください。<br />
		<br>
	</div>
</div>
<!-- ===  /content === -->

</div>
<!-- ***** /container ***** -->
</body>
</html>