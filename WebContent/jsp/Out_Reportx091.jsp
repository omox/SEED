
<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8"%>

<%@ page import="java.util.Date"%>
<%@ page import="common.Defines" %>
<%@ page import="common.DefineReport" %>
<%@ page import="authentication.bean.*" %>
<%@ page import="authentication.defines.*" %>
<%
	// レポート番号
	String reportNo		=	(String)request.getSession().getAttribute(Defines.ID_REQUEST_JSP_REPORT);
	// ユーザ・セッション取得
	String userId		=	(String)request.getSession().getAttribute(Defines.ID_REQUEST_USER_ID);
	// 制限値取得
	String ro			=	(String)request.getSession().getAttribute(Defines.ID_REQUEST_CUSTOM_REPORT);
	// レポート名取得
	String reportName	=	(String)request.getSession().getAttribute(Defines.ID_REQUEST_TITLE_REPORT);
	// レポート予備情報
	String reportYobi1	=	(String)request.getSession().getAttribute(Defines.ID_REQUEST_REPORT_YOBI1);
	String reportYobi2	=	(String)request.getSession().getAttribute(Defines.ID_REQUEST_REPORT_YOBI2);

	// タイトル
	String titleName	=	"【 " + reportName + " 】";
	// 初期検索条件
	String initParam	= (String)request.getSession().getAttribute(Defines.ID_REQUEST_INIT_PARAM);

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

	String userTenpo = lusr.getTenpo();
	String userBumon = lusr.getBumon();

	// jsキャッシュ対応
	String prm = request.getSession().getId();
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html style="overflow-x: hidden;">
<head>
<title></title>
<meta http-equiv="X-UA-Compatible" content="IE=edge" />
<meta http-equiv="Pragma" content="no-cache">
<meta http-equiv="Cache-Control" content="no-cache">
<meta http-equiv="Expires" content="Thu, 01 Dec 1994 16:00:00 GMT">
<link rel="stylesheet" type="text/css" href="../css/report.css?v=<%=prm %>">
<link rel="stylesheet" type="text/css" href="../themes/easyui/default/easyui.css?v=<%=prm %>">
<link rel="stylesheet" type="text/css" href="../themes/easyui/default/custom_easyui.css?v=<%=prm %>">
<link rel="stylesheet" type="text/css" href="../themes/easyui/icon.css?v=<%=prm %>">
</head>
<body style="overflow-x: hidden;">
<div id="cc" class="easyui-layout" data-options="fit:true">
<form id="ff" method="post" style="display: inline">
<div id="search" data-options="region:'north',border:false" style="display:none;height:60px;padding:3px 8px 0;" >
	<table class="t-layout1">
	<tr>
	<td><%=DefineReport.Select.BUMON.getTxt()%></td>
	<td><input class="easyui-combobox" tabindex="1" id="<%=DefineReport.Select.BUMON.getObj()%>" style="width:170px;" data-options="panelHeight:200"></td>
	</tr>
	<tr>
	<td><%=DefineReport.InpText.CALLCD.getTxt()%></td>
	<td><input class="easyui-numberbox" tabindex="2" id="<%=DefineReport.InpText.CALLCD.getObj()%>" check='"datatyp":"lpadzero_text","maxlen":6' style="width:70px;"></td>
	<td><%=DefineReport.InpText.SHNCD.getTxt()%></td>
	<td><input class="easyui-numberbox" tabindex="3" id="<%=DefineReport.InpText.SHNCD.getObj()%>" check='<%=DefineReport.InpText.SHNCD.getMaxlenTag()%>' data-options="prompt:'____-____'" style="width:90px;"></td>
	<td class="col_btn" rowspan="4">
		<a href="#" title="検索" id="<%=DefineReport.Button.SEARCH.getObj()%>" class="easyui-linkbutton" tabindex="4" iconCls="icon-search"><span class="btnTxt">検索</span></a>
	</td>
	</tr>
	</table>
</div>
</form>
<form id="gf" class="e_grid">
<div id="list" data-options="region:'center',border:false" style="display:none;">
	<!-- EasyUI方式 -->
	<div tabindex="4" id="gridholder" class="easyui-datagrid placeFace" ></div>
</div>
</form>
<div id="buttons" data-options="region:'south',border:false" style="display:none;height:30px;padding:0 30px;text-align: center;">
<div class="btn" style="float: left;">
	<table class="t-layout3">
	<tr><th style="min-width:135px;"></th><th style="min-width:10px;">
		<th style="min-width:135px;"></th><th style="min-width:135px;"></th><th style="min-width:135px;"></th><th style="min-width:10px;"></th>
	<tr>
	<td><a href="#" title="戻る" id="<%=DefineReport.Button.BACK.getObj()%>" class="easyui-linkbutton" tabindex="20" iconCls="icon-undo" style="width:125px;"><span class="btnTxt">戻る</span></a></td>
	<td><a href="#" title="新規" id="<%=DefineReport.Button.NEW.getObj()%>" class="easyui-linkbutton" tabindex="21" iconCls="icon-add" style="width:125px;"><span class="btnTxt">新規</span></a></td>
	<td><a href="#" title="選択(変更)" id="<%=DefineReport.Button.SEL_CHANGE.getObj()%>" class="easyui-linkbutton" tabindex="22" iconCls="icon-ok" style="width:125px;"><span>選択(変更)</span></a></td>
	<td></td>
	</table>
</div>
</div>
</div>
<div id="debug" style="visibility: hidden;">
	<!-- report 情報 -->
	<input type="hidden" name="reportno" id="reportno" value="<%=reportNo %>"/>
	<!-- レポート名 情報 -->
	<input type="hidden" name="reportname" id="reportname" value="<%=reportName %>"/>
	<!-- ユーザー 情報 -->
	<input type="hidden" name="userid" id="userid" value="<%=userId %>"/>
	<input type="hidden" name="userTenpo" id="userTenpo" value="<%=userTenpo %>"/>
	<input type="hidden" name="userBumon" id="userBumon" value="<%=userBumon %>"/>
	<!-- 引き継ぎ情報(JSON文字列変換済 "'" で囲む) -->
	<input type="hidden" name="hiddenParam" id="hiddenParam" value='<%=sendParam %>' />
	<!-- ユーザID -->
	<input type="hidden" name="hiddenUser" id="hiddenUser" value="<%=user %>" />
	<!-- レポートID -->
	<input type="hidden" name="hiddenReport" id="hiddenReport" value="<%=report %>" />
	<!-- 初期条件情報(JSON文字列変換済 "'" で囲む) -->
	<input type="hidden" name="hiddenInit" id="hiddenInit" value='<%=initParam %>' />


	<!-- 初期条件情報(JSON文字列変換済 "'" で囲む) -->
	<input type="hidden" name="reportYobi1" id="reportYobi1" value='<%=reportYobi1 %>' />
	<input type="hidden" name="reportYobi2" id="reportYobi2" value='<%=reportYobi2 %>' />
</div>

</body>

<!-- Load jQuery and JS files -->
<script type="text/javascript" src="../js/jquery.min.js?v=<%=prm %>"></script>			<!-- jquery -->
<script type="text/javascript" src="../js/jquery.easyui.min.js?v=<%=prm %>"></script>	<!-- EasyUI framework -->
<script type="text/javascript" src="../js/easyui-lang-ja.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/datagrid-scrollview.js?v=<%=prm %>"></script>	<!-- EasyUI framework -->

<script type="text/javascript" src="../js/json2.min.js"></script>			<!-- json plugin -->
<script type="text/javascript" src="../js/exdate.js"></script>				<!-- exdate plugin -->

<script type="text/javascript" src="../js/jshashtable-2.1.js"></script>		<!-- jshashset plugin -->
<script type="text/javascript" src="../js/jquery.numberformatter.min.js"></script><!-- numberformatter plugin -->

<script type="text/javascript" src="../js/shortcut.js"></script>			<!-- shortcut plugin -->

<!-- Report Option & Control & Event  -->
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.x091.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.control.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.events.js?v=<%=prm %>"></script>
</html>