<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8"%>
<%@ page import="common.Defines" %>
<%@ page import="common.DefineReport" %>
<%@ page import="authentication.bean.*" %>
<%@ page import="authentication.defines.*" %>
<% String prm = request.getSession().getId(); %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title></title>
<meta http-equiv="X-UA-Compatible" content="IE=edge" />
<meta http-equiv="Pragma" content="no-cache">
<meta http-equiv="Cache-Control" content="no-cache">
<meta http-equiv="Expires" content="Thu, 01 Dec 1994 16:00:00 GMT">
<link rel="stylesheet" type="text/css" href="../css/report.css?v=<%=prm%>">
<link rel="stylesheet" type="text/css" href="../themes/easyui/default/easyui.css?v=<%=prm%>">
<link rel="stylesheet" type="text/css" href="../themes/easyui/icon.css?v=<%=prm%>">
<link rel="stylesheet" type="text/css" href="../themes/easyui/default/custom_easyui.css?v=<%=prm%>">
</head>

<%
	// レポート番号
	String reportNo		=	(String)request.getSession().getAttribute(Defines.ID_REQUEST_JSP_REPORT);

	// ユーザ・セッション取得
	String userId		=	(String)request.getSession().getAttribute(Defines.ID_REQUEST_USER_ID);

	// 制限値取得
	String ro			=	(String)request.getSession().getAttribute(Defines.ID_REQUEST_CUSTOM_REPORT);

	// レポート名取得
	String reportName	=	(String)request.getSession().getAttribute(Defines.ID_REQUEST_TITLE_REPORT);

	// タイトル
	String titleName	=	"【 " + reportName + " 】";

	// 親画面からの引き継ぎ情報
	String sendParam	= "";
	if (request.getSession().getAttribute(Defines.ID_REQUEST_SEND_PARAM)!=null) {
		sendParam	=	(String)request.getSession().getAttribute(Defines.ID_REQUEST_SEND_PARAM);
	}
	// ユーザID
	String user		=	(String)request.getSession().getAttribute(Defines.ID_REQUEST_CUSTOM_USER);
	// レポートID
	String report	=	(String)request.getSession().getAttribute(Defines.ID_REQUEST_REPORT_NO);

	User lusr = (User)request.getSession().getAttribute(Consts.STR_SES_LOGINUSER);
%>
<body>
<!-- ツールバー -->
<div id="tb" style="padding:2px;height:auto;display:none;">
	<form id="ff" method="post" style="display: inline">
	<table>
		<tr>
			<td>
				<span><%=DefineReport.Select.TORIHIKI.getTxt() %></span>
			</td>
			<td>
				<input type="hidden" id="<%=DefineReport.Hidden.TORIHIKI.getObj() %>" value="<%=request.getSession().getAttribute(DefineReport.Hidden.TORIHIKI.getObj()) %>">
				<input id="<%=DefineReport.Select.TORIHIKI.getObj() %>" style="width:200px">
			</td>
			<td>
				<span><%=DefineReport.InpText.TEIAN.getTxt() %></span>
			</td>
			<td>
				<input type="hidden" id="<%=DefineReport.Hidden.NO_TEIAN.getObj() %>" value="<%=request.getSession().getAttribute(DefineReport.Hidden.NO_TEIAN.getObj()) %>">
				<input id="<%=DefineReport.Select.TEIAN.getObj() %>" style="width:200px">
			</td>
			<td>
				<span>状態</span>
			</td>
			<td>
				<input id="<%=DefineReport.Select.STCD_TEIAN.getObj() %>" style="width:120px">
			</td>
		</tr>
		<tr>
			<td>
				<span><%=DefineReport.Select.BUMON.getTxt() %></span>
			</td>
			<td>
				<input id="<%=DefineReport.Select.BUMON.getObj() %>" style="width:150px">
			</td>
			<td>
				<span>商品名</span>
			</td>
			<td>
				<input id="<%=DefineReport.InpText.SHNKN.getObj()%>" style="width:150px">
			</td>
		</tr>
		<tr>
			<td>
				<span>商品登録日</span>
			</td>
			<td colspan="3">
				<input id="<%=DefineReport.InpText.YMD_F.getObj() %>" style="width:150px">
				<span>　～　</span>
				<input id="<%=DefineReport.InpText.YMD_T.getObj() %>" style="width:150px">
			</td>
		</tr>
	</table>
	<table>
		<tr>
			<td>
				<a href="#" id="<%=DefineReport.Button.SEARCH.getObj()%>" title="<%=DefineReport.Button.SEARCH.getTxt()%>" class="easyui-linkbutton" iconCls="icon-search"><span class="btnTxt"><%=DefineReport.Button.SEARCH.getTxt()%></span></a>
				<a href="#" id="<%=DefineReport.Button.EXCEL.getObj()%>" title="<%=DefineReport.Button.EXCEL.getTxt()%>" class="easyui-linkbutton" iconCls="icon-excel"><span class="btnTxt"><%=DefineReport.Button.EXCEL.getTxt()%></span></a>
				<span style="margin-right: 30px;">&nbsp;</span>
				<a href="#" id="<%=DefineReport.Button.NEW.getObj()%>" title="<%=DefineReport.Button.NEW.getTxt()%>" class="easyui-linkbutton" iconCls=""><span class="btnTxt"><%=DefineReport.Button.NEW.getTxt()%></span></a>
				<a href="#" id="<%=DefineReport.Button.DELETE.getObj()%>" title="<%=DefineReport.Button.DELETE.getTxt()%>" class="easyui-linkbutton" iconCls=""><span class="btnTxt"><%=DefineReport.Button.DELETE.getTxt()%></span></a>

				<a href="#" id="<%=DefineReport.Button.NEXT.getObj()%>" title="<%=DefineReport.Button.NEXT.getTxt()%>" class="easyui-linkbutton" iconCls=""><span class="btnTxt"><%=DefineReport.Button.NEXT.getTxt()%></span></a>
			</td>
		</tr>
	</table>
	</form>
</div>

<!-- グリッド -->
<div id="gridholder"  class="placeFace" ></div>
<input type="hidden" id="<%=DefineReport.Hidden.SELECT_IDX.getObj() %>" value="">
<input type="hidden" id="<%=DefineReport.Hidden.CHANGED_IDX.getObj() %>" value="">

<div id="buttons" data-options="region:'south',border:false">
	<div class="btn" style="float: left;">
		<table class="t-layout3">
		<tr><th style="min-width:135px;"></th><th style="min-width:10px;"></th>
			<th style="min-width:135px;"></th><th style="min-width:135px;"></th><th style="min-width:135px;"></th><th style="min-width:10px;"></th>
			<th style="min-width:135px;"></th><th style="min-width:135px;"></th><th style="min-width:135px;"></th></tr>
		<tr>
		<td><a href="#" title="戻る" id="<%=DefineReport.Button.BACK.getObj()%>" class="easyui-linkbutton" tabindex="21" iconCls="icon-undo" style="width:125px;"><span class="btnTxt">戻る</span></a></td>
		<td></td>
		</tr>
		</table>
	</div>
</div>

<div id="debug">
	<!-- report 情報 -->
	<input type="hidden" name="reportno" id="reportno" value="<%=reportNo %>"/>
	<!-- レポート名 情報 -->
	<input type="hidden" name="reportname" id="reportname" value="<%=reportName %>"/>
	<!-- ユーザー 情報 -->
	<input type="hidden" name="userid" id="userid" value="<%=userId %>"/>
	<!-- 引き継ぎ情報(JSON文字列変換済 "'" で囲む) -->
	<input type="hidden" name="hiddenParam" id="hiddenParam" value='<%=sendParam %>' />
	<!-- ユーザID -->
	<input type="hidden" name="hiddenUser" id="hiddenUser" value="<%=user %>" />
	<!-- レポートID -->
	<input type="hidden" name="hiddenReport" id="hiddenReport" value="<%=report %>" />
</div>

</body>

<!-- Load jQuery and JS files -->
<script type="text/javascript" src="../js/jquery.min.js?v=<%=prm %>"></script>			<!-- jquery -->
<script type="text/javascript" src="../js/jquery.easyui.min.js?v=<%=prm %>"></script>	<!-- EasyUI framework -->
<script type="text/javascript" src="../js/easyui-lang-ja.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/datagrid-scrollview.js?v=<%=prm%>"></script>

<script type="text/javascript" src="../js/json2.min.js"></script>			<!-- json plugin -->
<script type="text/javascript" src="../js/exdate.js"></script>				<!-- exdate plugin -->

<script type="text/javascript" src="../js/jshashtable-2.1.js"></script>		<!-- jshashset plugin -->
<script type="text/javascript" src="../js/jquery.numberformatter.min.js"></script><!-- numberformatter plugin -->

<script type="text/javascript" src="../js/shortcut.js"></script>			<!-- shortcut plugin -->

<!-- Report Option & Control & Event  -->
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.x246.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.control.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.events.js?v=<%=prm %>"></script>
</html>