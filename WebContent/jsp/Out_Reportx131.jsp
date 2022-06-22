
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
<div>
	<table class="t-layout1">
	<tr><th style="max-width:90px;"></th><th style="max-width:115px;"></th><th style="max-width:90px;"></th><th style="max-width:395px;"></th><th style="max-width:120px;"></th><th style="max-width:110px;"></th><th style="max-width:115px;"></th></tr>
	<tr>
	<td><%=DefineReport.InpText.TENCD.getTxt()%></td>
	<td><input class="easyui-numberbox" tabindex="1" col="F1" id="<%=DefineReport.InpText.TENCD.getObj()%>" check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>'  style="width:60px;"data-options="required:true"></td>
	<td><%=DefineReport.InpText.TENKN.getTxt()%></td>
	<td><input class="easyui-textbox" tabindex="2" id="<%=DefineReport.InpText.TENKN.getObj()%>" check='<%=DefineReport.InpText.TENKN.getMaxlenTag()%>' for_inp="<%=DefineReport.InpText.TENCD.getObj()%>_F3" style="width:370px;" value=""></td>

	<td class="col_btn" rowspan="4">
		<a href="#" title="検索" id="<%=DefineReport.Button.SEARCH.getObj()%>" class="easyui-linkbutton" tabindex="3" iconCls="icon-search"><span class="btnTxt">検索</span></a>
	</td>
	</tr>

	</table>
</div>
</div>

<div id="list" data-options="region:'center',border:false" style="display:none;">

	<!-- EasyUI方式 -->
	<div tabindex="4" id="gridholder" class="easyui-datagrid" data-options="fit:true"></div>

 	<!-- <table id="gridholder" class="easyui-datagrid" tabindex="4"   data-options="rownumbers:true,singleSelect:true,fit:true,checkOnSelect:false,selectOnCheck:false,editor:{type:'textbox',options:{cls:'labelInput',editable:true,disabled:false,readonly:false}}">
		<thead>
			<tr>
			 	<th data-options="field:'F14',checkbox:true,value:1">削除</th>
				<th data-options="field:'F3',width:40,halign:'center',align:'left',editor:{type:'numberbox',options:{cls:'labelInput',editable:true,disabled:false,readonly:false}}">部門</th>
				<th data-options="field:'F4',width:260,halign:'center',editor:{type:'textbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:false}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}">部門名</th>
				<th data-options="field:'F5',width:30,halign:'center',align:'center',editor:{type:'checkbox'},styler:function(value,row,index){return 'color: red;font-weight: bold;';},formatter:function(value,row,index){return value && value===$.id.value_on?$.id.text_on:$.id.text_off;}">月</th>
				<th data-options="field:'F6',width:30,halign:'center',align:'center',editor:{type:'checkbox'},styler:function(value,row,index){return 'color: red;font-weight: bold;';},formatter:function(value,row,index){return value && value===$.id.value_on?$.id.text_on:$.id.text_off;}">火</th>
				<th data-options="field:'F7',width:30,halign:'center',align:'center',editor:{type:'checkbox'},styler:function(value,row,index){return 'color: red;font-weight: bold;';},formatter:function(value,row,index){return value && value===$.id.value_on?$.id.text_on:$.id.text_off;}">水</th>
				<th data-options="field:'F8',width:30,halign:'center',align:'center',editor:{type:'checkbox'},styler:function(value,row,index){return 'color: red;font-weight: bold;';},formatter:function(value,row,index){return value && value===$.id.value_on?$.id.text_on:$.id.text_off;}">木</th>
				<th data-options="field:'F9',width:30,halign:'center',align:'center',editor:{type:'checkbox'},styler:function(value,row,index){return 'color: red;font-weight: bold;';},formatter:function(value,row,index){return value && value===$.id.value_on?$.id.text_on:$.id.text_off;}">金</th>
				<th data-options="field:'F10',width:30,halign:'center',align:'center',editor:{type:'checkbox'},styler:function(value,row,index){return 'color: red;font-weight: bold;';},formatter:function(value,row,index){return value && value===$.id.value_on?$.id.text_on:$.id.text_off;}">土</th>
				<th data-options="field:'F11',width:30,halign:'center',align:'center',editor:{type:'checkbox'},styler:function(value,row,index){return 'color: red;font-weight: bold;';},formatter:function(value,row,index){return value && value===$.id.value_on?$.id.text_on:$.id.text_off;}">日</th>
				<th data-options="field:'F12',hidden:true,width:260,halign:'center',editor:{type:'textbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:false}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}">部門名</th>
				<th data-options="field:'F13',hidden:true,width:260,halign:'center',editor:{type:'textbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:false}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}">部門名</th>
				<th data-options="field:'F14',hidden:true,width:260,halign:'center',editor:{type:'textbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:false}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}">部門名</th>
				<th data-options="field:'F15',hidden:true,width:260,halign:'center',editor:{type:'textbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:false}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}">部門名</th>


		</thead>
	</table> -->

	<!-- Editor参照用 -->
	<div class="ref_editor" style="display: none;">
	 	<!-- <input type="checkbox" class="chkbox" tabindex="-1" col="F14" id="txt_f14" data-options="multiple:true" value="1"> -->
		<input class="easyui-numberbox" tabindex="-1" id='txt_f3'  check='<%=DefineReport.InpText.BMNCD.getMaxlenTag()%>' data-options="editable:true"/>
		<input class="easyui-textbox" 	tabindex="-1" id="txt_f4" for_inp="<%=DefineReport.InpText.BMNCD.getObj()%>_TEXT2" check='<%=DefineReport.InpText.BMKAN.getMaxlenTag()%>'data-options="required:false">

		<input type="checkbox" tabindex="-1" id="chk_f5"/>
		<input type="checkbox" tabindex="-1" id="chk_f6"/>
		<input type="checkbox" tabindex="-1" id="chk_f7"/>
		<input type="checkbox" tabindex="-1" id="chk_f8"/>
		<input type="checkbox" tabindex="-1" id="chk_f9"/>
		<input type="checkbox" tabindex="-1" id="chk_f10"/>
		<input type="checkbox" tabindex="-1" id="chk_f11"/>
<%-- 		<input class="easyui-textbox" 	tabindex="-1" col="F12"  id="txt_f12"  check='<%=DefineReport.InpText.BMKAN.getMaxlenTag()%>'data-options="required:false">
		<input class="easyui-textbox" 	tabindex="-1" col="F13"  id="txt_f13"  check='<%=DefineReport.InpText.BMKAN.getMaxlenTag()%>'data-options="required:false">
		<input class="easyui-textbox" 	tabindex="-1" col="F14"  id="txt_f14"  check='<%=DefineReport.InpText.BMKAN.getMaxlenTag()%>'data-options="required:false">
		<input class="easyui-textbox" 	tabindex="-1" col="F15"  id="txt_f15"  check='<%=DefineReport.InpText.BMKAN.getMaxlenTag()%>'data-options="required:false">

		 <input class="easyui-checkbox" 	tabindex="-1" col="F5"  id="txt_f5"  check='<%=DefineReport.InpText.HATFLG_MON.getMaxlenTag()%>'>
		<input class="easyui-checkbox" tabindex="-1" col="F6"  id="txt_f6"  check='<%=DefineReport.InpText.HATFLG_TUE.getMaxlenTag()%>'>
		<input class="easyui-checkbox" tabindex="-1" col="F7"  id="txt_f7"  check='<%=DefineReport.InpText.HATFLG_WED.getMaxlenTag()%>'>
		<input class="easyui-checkbox" tabindex="-1" col="F8"  id="txt_f8"  check='<%=DefineReport.InpText.HATFLG_THU.getMaxlenTag()%>'>
		<input class="easyui-checkbox" tabindex="-1" col="F9"  id="txt_f9"  check='<%=DefineReport.InpText.HATFLG_FRI.getMaxlenTag()%>'>
		<input class="easyui-checkbox" tabindex="-1" col="F10" id="txt_f10"  check='<%=DefineReport.InpText.HATFLG_SAT.getMaxlenTag()%>'>
		<input class="easyui-checkbox" tabindex="-1" col="F11" id="txt_f11"  check='<%=DefineReport.InpText.HATFLG_SUN.getMaxlenTag()%>'>
 --%>
	</div>
</div>
</form>



<div id="buttons" data-options="region:'south',border:false" style="display:none;">
	<div class="btn" style="float: left;">
		<table class="t-layout3">
		<tr><th style="min-width:135px;"></th><th style="min-width:10px;"></th>
		<th style="min-width:135px;"></th>
		<th style="min-width:135px;"></th>
		</tr>
		<tr>
		<td><a href="#" title="戻る" id="<%=DefineReport.Button.BACK.getObj()%>" class="easyui-linkbutton" tabindex="5" iconCls="icon-undo" style="width:125px;"><span class="btnTxt">戻る</span></a></td>
		<td></td>
		<td><a href="#" title="キャンセル" id="<%=DefineReport.Button.CANCEL.getObj()%>" class="easyui-linkbutton" tabindex="6" iconCls="icon-cancel" style="width:125px;"><span class="btnTxt">キャンセル</span></a></td>
		<td><a href="#" title="登録" id="<%=DefineReport.Button.UPD.getObj()%>" class="easyui-linkbutton" tabindex="7" iconCls="icon-add" style="width:125px;"><span>登録</span></a></td>
		<td><a href="#" title="削除" id="<%=DefineReport.Button.DEL.getObj()%>" class="easyui-linkbutton" tabindex="8" iconCls="icon-remove" style="width:125px;"><span>削除</span></a></td>
		<td></td>
		</tr>

		</table>
	</div>
	<div id="disp_record_info" style="float: right;">
		<span class="labelName" tabindex="-1" style="padding-top: 3px;">
				登録日 <span col="F7" id="<%=DefineReport.Text.ADDDT.getObj()%>"></span>　更新日 <span col="F8" id="<%=DefineReport.Text.UPDDT.getObj()%>"></span>　オペレータ <span col="F9" id="<%=DefineReport.Text.OPERATOR.getObj()%>"></span>
		</span>
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
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.x131.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.control.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.events.js?v=<%=prm %>"></script>
</html>