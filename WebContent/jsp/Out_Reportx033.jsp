
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
<div id="search" data-options="region:'north',border:false" style="display:none;height:40px;padding:3px 8px 0;" >
	<div>
	<input class="easyui-combobox" tabindex="-1" id="<%=DefineReport.Select.BNNRUIKBN.getObj()%>" data-options="label:'分類区分',labelWidth:75,editable:false,disabled:true,readonly:true" style="width:130px;">
	<%=DefineReport.InpText.BMNCD.getTxt()%>
	<input class="easyui-numberbox" tabindex="2" id="<%=DefineReport.InpText.BMNCD.getObj()%>" check='<%=DefineReport.InpText.BMNCD.getMaxlenTag()%>' data-options="required:true" style="width:80px;">
	<%=DefineReport.InpText.DAICD.getTxt()%>
	<input class="easyui-numberbox" tabindex="3" id="<%=DefineReport.InpText.DAICD.getObj()%>" check='<%=DefineReport.InpText.DAICD.getMaxlenTag()%>' data-options="required:true" style="width:80px;">
	<%=DefineReport.InpText.CHUCD.getTxt()%>
	<input class="easyui-numberbox" tabindex="4" id="<%=DefineReport.InpText.CHUCD.getObj()%>" check='<%=DefineReport.InpText.CHUCD.getMaxlenTag()%>' data-options="required:true" style="width:80px;">
	<a href="#" title="検索" id="btn_search" class="easyui-linkbutton" tabindex="-1" iconCls="icon-search" style="display:none"><span class="btnTxt">検索</span></a>
	</div>
</div>
<div id="list" data-options="region:'center',border:false" style="display:none;">

	<!-- EasyUI方式 -->
	<div tabindex="5" id="gridholder" class="easyui-datagrid" data-options="fit:true"></div>
	<!-- <table>
		<tr>
			<td style = "height:550px;width:1100px">
			<div tabindex="5" id="gridholder" class="easyui-datagrid" data-options="fit:true"></div>
			</td>
		</tr>
	</table> -->
	<!-- Editor参照用 -->
	<div class="ref_editor" style="display: none;">
		<input class="easyui-numberbox" tabindex="-1" col="F1"  id='txt_f1'  check='<%=DefineReport.InpText.SHOCD.getMaxlenTag()%>'/>
		<input class="easyui-textbox" 	tabindex="-1" col="F2"  id="txt_f2"  check='<%=DefineReport.InpText.SHOBRUIAN.getMaxlenTag()%>'data-options="editable:true">
		<input class="easyui-textbox" 	tabindex="-1" col="F3"  id="txt_f3"  check='<%=DefineReport.InpText.SHOBRUIKN.getMaxlenTag()%>'data-options="editable:true">
		<input class="easyui-numberbox" tabindex="-1" col="F4"  id="txt_f4"  check='<%=DefineReport.InpText.ATR.getMaxlenTag()%>'>
		<input class="easyui-numberbox" tabindex="-1" col="F5"  id="txt_f5"  check='<%=DefineReport.InpText.ATR.getMaxlenTag()%>'>
		<input class="easyui-numberbox" tabindex="-1" col="F6"  id="txt_f6"  check='<%=DefineReport.InpText.ATR.getMaxlenTag()%>'>
		<input class="easyui-numberbox" tabindex="-1" col="F7"  id="txt_f7"  check='<%=DefineReport.InpText.ATR.getMaxlenTag()%>'>
		<input class="easyui-numberbox" tabindex="-1" col="F8"  id="txt_f8"  check='<%=DefineReport.InpText.ATR.getMaxlenTag()%>'>
		<input class="easyui-numberbox" tabindex="-1" col="F9" 	id="txt_f9"  check='<%=DefineReport.InpText.ATR.getMaxlenTag()%>'>
		<input class="easyui-textbox" 	tabindex="-1" col="F10" id='txt_f10' readonly="readonly" data-options="readonly:true,editable:false"  style="width:120px;" value="">
		<input class="easyui-textbox" 	tabindex="-1" col="F11" id="txt_f11" readonly="readonly" data-options="readonly:true,editable:false"  style="width:120px;" value="">
		<input class="easyui-textbox" 	tabindex="-1" col="F12" id="txt_f12" readonly="readonly" data-options="readonly:true,editable:false"  style="width:120px;" value="">
		<input class="easyui-numberbox" tabindex="-1" col="F13" id="txt_f13" readonly="readonly" data-options="readonly:true,editable:false">
	</div>
</div>
</form>
<div id="buttons" data-options="region:'south',border:false" style="display:none;height:30px;padding:0 30px;text-align: center;align: left;">
	<div class="btn" style="text-align: left;">
				<div class="btn" style="float: left;">
		<table class="t-layout3">
	<tr><th style="min-width:135px;"></th><th style="min-width:10px;"></th>
		<th style="min-width:135px;"></th>
		<th style="min-width:135px;"></th>
	</tr>
	<tr>
		<td><a href="#" title="戻る" id="<%=DefineReport.Button.BACK.getObj()%>" class="easyui-linkbutton" tabindex="6" iconCls="icon-undo" style="width:125px;"><span class="btnTxt">戻る</span></a></td>
		<td></td>
		<td><a href="#" class="easyui-linkbutton" tabindex="7" id="<%=DefineReport.Button.CANCEL.getObj()%>" title="<%=DefineReport.Button.CANCEL.getTxt()%>" iconCls="icon-cancel" style="width:125px;"><span><%=DefineReport.Button.CANCEL.getTxt()%></span></a></td>
		<td><a href="#" class="easyui-linkbutton" tabindex="8" id="<%=DefineReport.Button.UPD.getObj()%>" title="<%=DefineReport.Button.UPD.getTxt()%>" iconCls="icon-save" style="width:125px;"><span class="btnTxt"><%=DefineReport.Button.UPD.getTxt()%></span></a></td>
		<%-- <td><a href="#" title="選択" class="easyui-linkbutton" tabindex="9" id="<%=DefineReport.Button.SELECT.getObj()%>" title="<%=DefineReport.Button.SELECT.getTxt()%>" iconCls="icon-remove" style="width:150px;"><%=DefineReport.Button.SELECT.getTxt()%>(小小分類へ)</a></td> --%>
		</tr>
		</table>
	</div>
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
	<!-- 引き継ぎ情報(JSON文字列変換済 "'" で囲む) -->
	<input type="hidden" name="hiddenParam" id="hiddenParam" value='<%=sendParam %>' />
	<!-- ユーザID -->
	<input type="hidden" name="hiddenUser" id="hiddenUser" value="<%=user %>" />
	<!-- レポートID -->
	<input type="hidden" name="hiddenReport" id="hiddenReport" value="<%=report %>" />
	<!-- 初期条件情報(JSON文字列変換済 "'" で囲む) -->
	<input type="hidden" name="hiddenInit" id="hiddenInit" value='<%=initParam %>' />

	<input type="hidden" name="<%=DefineReport.Hidden.CHANGED_IDX.getObj()%>" id="<%=DefineReport.Hidden.CHANGED_IDX.getObj()%>" />

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
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.x033.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.control.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.events.js?v=<%=prm %>"></script>
</html>