
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

<div data-options="region:'center',border:false" style="display:none;">
<div id="tt" class="easyui-tabs" data-options="plain:true,narrow:true,justified:true,fit:true">
<div title="日付、複数店舗" style="display:none;padding:3px 8px;" data-options="selected:true" id="sel1">
	<div id="cc_1" class="easyui-layout" data-options="fit:true">
	<div data-options="region:'center',border:false" style="display:none;">
		<form id="ff_1" method="post" style="display: inline">
		<table class="t-layout" style="margin:10px;">
			<tr><th style="max-width:600px;"></th><th style="max-width:600px;"></th></tr>
			<tr>
				<td>
				 	<div class="inp_box"><input class="easyui-combobox" tabindex="1" col="F1" id="<%=DefineReport.MeisyoSelect.KBN316.getObj()%>" data-options="label:'店休フラグ',labelWidth:150,required:true" style="width:270px;"></div>
				</td>
			</tr>
			<tr>
				<td>
					<div class="inp_box"><input class="easyui-numberbox" tabindex="2" id="<%=DefineReport.InpText.TENKYUDT.getObj()%>"  col="F2" check='<%=DefineReport.InpText.TENKYUDT.getMaxlenTag()%>' data-options="label:'日付',labelWidth:150,prompt:'__/__/__',required:true" style="width:220px;"/></div>
				</td>
			</tr>
		</table>
		<div id = "aa"style="height:400px;width: 400px;">
		<table id="<%=DefineReport.Grid.TENPO_M.getObj()%>" class="easyui-datagrid" tabindex="3" data-options="singleSelect:true,rownumbers:true,width:400,height:400,fit:true">
			<thead>
				<tr>
				<th data-options="field:'TENCD'			,width:70,halign:'center',align:'right',editor:'numberbox',formatter:function(value){var check = $('#'+$.id_inp.txt_tencd).attr('check') ? JSON.parse('{'+$('#'+$.id_inp.txt_tencd).attr('check')+'}'): JSON.parse('{}');return $.getFormatLPad(value, 3);}">店コード</th>
				<th data-options="field:'TENKN'			,width:200,halign:'center',editor:{type:'textbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:true}}">店舗名</th>
				</tr>
			</thead>
		</table>
		<!-- Editor参照用 -->
		<div class="ref_editor" style="display: none;">
		<input  class="easyui-textbox" tabindex="-1" id="<%=DefineReport.InpText.TENKN.getObj()%>" for_inp="<%=DefineReport.InpText.TENCD.getObj()%>_F3" check='<%=DefineReport.InpText.TENKN.getMaxlenTag()%>' />
		</div>
		</div>
		</form>
	</div>
	<div id="buttons" data-options="region:'south',border:false" style="display:none;">
		<div class="btn" style="float: left;">
		<table class="t-layout3">
		<tr><th style="min-width:135px;"></th><th style="min-width:10px;"></th>
			<th style="min-width:135px;"></th>
			<th style="min-width:135px;"></th>
		</tr>
		<tr>
		<td><a href="#" title="戻る" id="<%=DefineReport.Button.BACK.getObj()%>" tabindex="4" class="easyui-linkbutton" tabindex="901" iconCls="icon-undo" style="width:125px;"><span class="btnTxt">戻る</span></a></td>
		<td></td>
		<td><a href="#" title="キャンセル" class="easyui-linkbutton" tabindex="5" id="<%=DefineReport.Button.CANCEL.getObj()%>" iconCls="icon-cancel" style="width:125px;"><%=DefineReport.Button.CANCEL.getTxt()%></a></td>
		<td><a href="#" title="登録" class="easyui-linkbutton" tabindex="6" id="<%=DefineReport.Button.UPD_TAB1.getObj()%>" title="<%=DefineReport.Button.UPD_TAB1.getTxt()%>" iconCls="icon-save" style="width:125px;"><span class="btnTxt"><%=DefineReport.Button.UPD_TAB1.getTxt()%></span></a></td>
		</tr>
		</table>
		</div>


	</div>
	</div>
</div>
<div title="店舗、期間" style="display:none;padding:3px 8px;" data-options="selected:true" id="sel2">
	<div id="cc_2" class="easyui-layout" data-options="fit:true">
	<div data-options="region:'center',border:false" style="display:none;">
		<form id="ff_2" method="post" style="display: inline">
		<table class="t-layout" style="margin:10px;">
			<tr><th style="max-width:600px;"></th><th style="max-width:600px;"></th></tr>
			<tr>
				<td>
				 	<div class="inp_box"><input class="easyui-combobox" tabindex="7" col="F4" id="<%=DefineReport.MeisyoSelect.KBN316.getObj()%>_kikan" data-options="label:'店休フラグ',labelWidth:150,required:true" style="width:270px;"></div>
				</td>
			</tr>
			<tr>
				<td>
					<div class="inp_box"><input class="easyui-numberbox" tabindex="8" id="<%=DefineReport.InpText.TENCD.getObj()%>" data-options="label:'店舗',labelWidth:150,required:true" check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' col="F6" style="width:230px;" ></input></div>
				</td>
			</tr>
			<tr>
				<td>
					<div class="inp_box">
					<span class="tit_span" style="width:145px;">日付</span>
					<input class="easyui-numberbox" tabindex="9" id="<%=DefineReport.InpText.TENKYU_STDT.getObj()%>"  col="STDT" check='<%=DefineReport.InpText.TENKYU_STDT.getMaxlenTag()%>' data-options="prompt:'__/__/__',required:true" style="width:65px;"/>
					<span class="tit_span" style="width:10px;">～</span>
					<input class="easyui-numberbox" tabindex="10" id="<%=DefineReport.InpText.TENKYU_ENDT.getObj()%>"  col="ENDT" check='<%=DefineReport.InpText.TENKYU_ENDT.getMaxlenTag()%>' data-options="prompt:'__/__/__',required:true" style="width:65px;"/>
					</div>
				</td>
			</tr>
			<tr>
				<td>
					<input type="hidden" name="<%=DefineReport.Text.SHORIDT.getObj()%>" id="<%=DefineReport.Text.SHORIDT.getObj()%>" />
				</td>
			</tr>
		</table>

		</form>
	</div>
	<div id="buttons" data-options="region:'south',border:false" style="display:none;">
		<div class="btn" style="float: left;">
		<table class="t-layout3">
		<tr><th style="min-width:135px;"></th><th style="min-width:10px;"></th>
			<th style="min-width:135px;"></th>
			<th style="min-width:135px;"></th>
		</tr>
		<tr>
		<td><a href="#" title="戻る" id="<%=DefineReport.Button.BACK_TAB2.getObj()%>" class="easyui-linkbutton" tabindex="11" iconCls="icon-undo" style="width:125px;"><span class="btnTxt">戻る</span></a></td>
		<td></td>
		<td><a href="#" title="キャンセル" class="easyui-linkbutton" tabindex="12" id="<%=DefineReport.Button.CANCEL_TAB2.getObj()%>" iconCls="icon-cancel" style="width:125px;"><%=DefineReport.Button.CANCEL.getTxt()%></a></td>
		<td><a href="#" title="登録" class="easyui-linkbutton" tabindex="13" id="<%=DefineReport.Button.UPD_TAB2.getObj()%>" title="<%=DefineReport.Button.UPD_TAB2.getTxt()%>" iconCls="icon-save" style="width:125px;"><span class="btnTxt"><%=DefineReport.Button.UPD_TAB1.getTxt()%></span></a></td>
		</tr>
		</table>
		</div>
		<%-- <div class="btn" style="text-align: left;bottom:1000px;"	>
			<a href="#" title="戻る" id="<%=DefineReport.Button.BACK_TAB2.getObj()%>" class="easyui-linkbutton" tabindex="11" iconCls="icon-undo" style="width:125px;"><span class="btnTxt">戻る</span></a>
			<a href="#" title="キャンセル" class="easyui-linkbutton" tabindex="12" id="<%=DefineReport.Button.CANCEL_TAB2.getObj()%>" iconCls="icon-cancel" style="width:110px;"><%=DefineReport.Button.CANCEL.getTxt()%></a>
			<a href="#" title="登録" class="easyui-linkbutton" tabindex="13" id="<%=DefineReport.Button.UPD_TAB2.getObj()%>" title="<%=DefineReport.Button.UPD_TAB2.getTxt()%>" iconCls="icon-save" style="width:110px;"><span class="btnTxt"><%=DefineReport.Button.UPD_TAB1.getTxt()%></span></a>
		</div> --%>
	</div>
</div>
</div>
</form>
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
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.x142.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.control.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.events.js?v=<%=prm %>"></script>
</html>