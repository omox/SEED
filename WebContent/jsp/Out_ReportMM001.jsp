
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
<div id="search" data-options="region:'north',border:false" style="display:none;" >
	<table class="t-layout1">
	<tr>
		<td><input class="easyui-numberbox" tabindex="1" id="<%=DefineReport.InpText.SHNCD.getObj()%>" check='<%=DefineReport.InpText.SHNCD.getMaxlenTag()%>' data-options="label:'商品コード',labelWidth:80,prompt:'____-____'" style="width:180px;" value=""></td>
		<td><input class="easyui-numberbox" tabindex="2" id="<%=DefineReport.InpText.TENCD.getObj()%>" check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' data-options="label:'店コード',labelWidth:60,labelAlign:'left'" style="width:100px;" value=""></td>
		<td style="height:30px;">
			<div class="inp_box">
				<select class="easyui-combobox" tabindex="3" id="<%=DefineReport.Select.MOYSKBN.getObj()%>" data-options="required:true,label:'催し区分',labelWidth:65,labelAlign:'left',panelHeight:200" style="width:250px;"></select>
			</div>
		</td>
	</tr>
	<tr>
		<td><input class="easyui-numberbox" tabindex="4" id="<%=DefineReport.InpText.HBSTDT.getObj()%>" check='<%=DefineReport.InpText.HBSTDT.getMaxlenTag()%>' data-options="label:'催し販売期間',labelWidth:80,prompt:'<%=DefineReport.Label.PROMPT_DTW.getTxt()%>'" style="width:200px;" value=""></td>
		<td><input class="easyui-numberbox" tabindex="5" id="<%=DefineReport.InpText.HBEDDT.getObj()%>" check='<%=DefineReport.InpText.HBEDDT.getMaxlenTag()%>' data-options="label:'～',labelWidth:20,labelAlign:'center',prompt:'<%=DefineReport.Label.PROMPT_DTW.getTxt()%>'" style="width:150px;" value=""></td>
		<td style="height:30px;">
			<div class="inp_box">
				<select class="easyui-combobox" tabindex="6" id="<%=DefineReport.Select.BUMON.getObj()%>" data-options="label:'部門',labelWidth:65,labelAlign:'left',panelHeight:200" style="width:300px;"></select>
			</div>
		</td>
	</tr>
	<tr>
		<td><input class="easyui-numberbox" tabindex="7" id="<%=DefineReport.InpText.NNSTDT.getObj()%>" check='<%=DefineReport.InpText.NNSTDT.getMaxlenTag()%>' data-options="label:'催し納入期間',labelWidth:80,prompt:'<%=DefineReport.Label.PROMPT_DTW.getTxt()%>'" style="width:200px;" value=""></td>
		<td><input class="easyui-numberbox" tabindex="8" id="<%=DefineReport.InpText.NNEDDT.getObj()%>" check='<%=DefineReport.InpText.NNEDDT.getMaxlenTag()%>' data-options="label:'～',labelWidth:20,labelAlign:'center',prompt:'<%=DefineReport.Label.PROMPT_DTW.getTxt()%>'" style="width:150px;" value=""></td>
		<td class="col_btn">
			<a href="#" title="検索" id="<%=DefineReport.Button.SEARCH.getObj()%>" class="easyui-linkbutton" tabindex="9" iconCls="icon-search"><span class="btnTxt">検索</span></a>
			<a href="#" title="店番表示" id="<%=DefineReport.Button.TENNOVIEW.getObj()%>" class="easyui-linkbutton" tabindex="10" iconCls="icon-search"><span class="btnTxt">店番表示</span></a>
		</td>
	</tr>
	</table>
	<div style="font-size:9pt">
	<table bgcolor="#cccccc">

	<tr>
		<td>注意:</td>
	</tr>
	<tr>
		<td>・本画面は、①商品コード、②商品コード・店コード、③店コード・部門の３パターンでのみ検索を行えます。販売・納入期間</td>
	</tr>
	<tr>
		<td>　はどのパターンでも任意で指定可能です。</td>
	</tr>
	<tr>
		<td>・本画面とMM002においては、1日前の情報が表示されます。</td>
	</tr>
	</table>
	</div>
</div>
</form>
<form id="gf" class="e_grid">
<div id="list" data-options="region:'center',border:false" style="display:none;">

	<!-- EasyUI方式 -->
	<div id="gridholder" class="easyui-datagrid placeFace" ></div>
	<!-- <div tabindex="11" id="gridholder" class="easyui-datagrid placeFace" ></div> -->
</div>
</form>
<div id="buttons" data-options="region:'south',border:false" style="display:none;">
	<div class="btn" style="float: left;">
		<table class="t-layout3">
		<tr>
			<th style="min-width:120px;"></th><th style="min-width:10px;"></th>
			<th style="min-width:120px;"></th><th style="min-width:120px;"></th>
		</tr>
		<tr>
			<td><a href="#" title="戻る" id="<%=DefineReport.Button.BACK.getObj()%>" class="easyui-linkbutton" tabindex="9001" iconCls="icon-undo" style="width:110px;"><span class="btnTxt">戻る</span></a></td>
		<td><a href="#" class="easyui-linkbutton" tabindex="9001" id="<%=DefineReport.Button.SELECT.getObj()%>" title="<%=DefineReport.Button.SELECT.getTxt()%>" iconCls="icon-ok" style="width:110px;"><span class="btnTxt"><%=DefineReport.Button.SELECT.getTxt()%></span></a></td>
		</tr>
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

	<input type="hidden" name="<%=DefineReport.Hidden.CHANGED_IDX.getObj()%>" id="<%=DefineReport.Hidden.CHANGED_IDX.getObj()%>" />

	<!-- 初期条件情報(JSON文字列変換済 "'" で囲む) -->
	<input type="hidden" name="reportYobi1" id="reportYobi1" value='<%=reportYobi1 %>' />
	<input type="hidden" name="reportYobi2" id="reportYobi2" value='<%=reportYobi2 %>' />

	<input type="hidden" name="sendBtnid" id="sendBtnid" value='' />
</div>
<div style="display:none;">
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
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.MM001.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.control.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.events.js?v=<%=prm %>"></script>
</html>