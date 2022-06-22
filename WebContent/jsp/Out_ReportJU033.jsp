
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
<div id="search" data-options="region:'north',border:false" style="display:none;height:170px;padding:3px 8px 0;" >
<table class="t-layout1">
	<tr>
		<td>
			<input class="easyui-numberbox" tabindex="-1" col="F1" id="<%=DefineReport.InpText.MOYSCD.getObj()%>" check='<%=DefineReport.InpText.MOYSCD.getMaxlenTag()%>' data-options="prompt:'_-______-___',label:'催しコード',labelWidth:80" style="width:205px;" disabled="disabled">
			<!-- 非表示でデータを保持 -->
			<input type="hidden" name="<%=DefineReport.InpText.KANRINO.getObj()%>" id="<%=DefineReport.InpText.KANRINO.getObj()%>" />
			<input type="hidden" name="<%=DefineReport.InpText.TENHTSU_ARR.getObj()%>" id="<%=DefineReport.InpText.TENHTSU_ARR.getObj()%>" />
		</td>
		<td colspan=2>
			<input class="easyui-textbox" tabindex="-1" col="F2" id="<%=DefineReport.InpText.MOYKN.getObj()%>" data-options="label:'催し名称',labelWidth:80" style="width:350px;" disabled="disabled"/>
		</td>
	</tr>
	<tr>
		<td>
			<input class="easyui-textbox" tabindex="-1" col="F3" style="width:260px;" id="kikan_dummy" value="" data-options="label:'納入期間',labelWidth:80,required:true" disabled="disabled">
		</td>
		<td>
			<input class="easyui-textbox" tabindex="-1" col="F4" id="<%=DefineReport.InpText.QASMDT.getObj()%>" data-options="prompt:'<%=DefineReport.Label.PROMPT_DTW.getTxt()%>',label:'店舗締切日',labelWidth:80" style="width:220px;" disabled="disabled"/>
		</td>
	</tr>
	<tr>
		<td>
			<input class="easyui-numberbox" col="F5" tabindex="-1" id="<%=DefineReport.InpText.SHNCD.getObj()%>" check='<%=DefineReport.InpText.SHNCD.getMaxlenTag()%>' data-options="prompt:'____-____',label:'商品コード',labelWidth:80" style="width:200px;" disabled="disabled">
		</td>
		<td colspan=2>
			<input class="easyui-textbox" tabindex="-1" col="F6" id="<%=DefineReport.InpText.SHNKN.getObj()%>" check='<%=DefineReport.InpText.SHNKN.getMaxlenTag()%>' style="width:350px;" data-options="label:'商品名',labelWidth:80" disabled="disabled">
		</td>
	</tr>
	<tr>
		<td>
			<input class="easyui-combobox" tabindex="1" col="F7" id="<%=DefineReport.MeisyoSelect.KBN103051.getObj()%>" data-options="label:'商品区分',labelWidth:80,required:true" style="width:200px;">
		</td>
		<td>
			<input class="easyui-combobox" tabindex="2" col="F8" id="<%=DefineReport.MeisyoSelect.KBN103061.getObj()%>" data-options="label:'訂正区分',labelWidth:80,required:true" style="width:230px;">
		</td>
		<td>
			<input class="easyui-textbox" tabindex="-1" col="F9" id="<%=DefineReport.InpText.JUKBN.getObj()%>" value="" data-options="label:'　事前区分',labelWidth:80,required:true" disabled="disabled" style="width:140px;text-align: left;">
		</td>
		<td>
			<input class="easyui-numberbox" tabindex="3" col="F10" id="<%=DefineReport.InpText.BDENKBN.getObj()%>" check='<%=DefineReport.InpText.BDENKBN.getMaxlenTag()%>' value="" data-options="label:'　別伝区分',labelWidth:80,min:0,max:8" style="width:140px;text-align: left;">
		</td>
		<td>
			<input class="easyui-combobox" tabindex="4" col="F11" id="<%=DefineReport.MeisyoSelect.KBN103081.getObj()%>" for_inp="<%=DefineReport.InpText.SHNCD.getObj()%>_F2" data-options="label:'　ワッペン区分',labelWidth:100,required:true" style="width:220px;">
		</td>
	</tr>
	<tr>
		<td>
			<input class="easyui-numberbox" tabindex="5" col="F12" id="<%=DefineReport.InpText.IRISU.getObj()%>" check='<%=DefineReport.InpText.IRISU.getMaxlenTag()%>' for_inp="<%=DefineReport.InpText.SHNCD.getObj()%>_F3" data-options="label:'入数',labelWidth:80,required:true,min:0" style="width:200px;text-align: right;">
		</td>
		<td>
			<input class="easyui-numberbox" tabindex="6" col="F13" id="<%=DefineReport.InpText.GENKAAM.getObj()%>" check='<%=DefineReport.InpText.GENKAAM.getMaxlenTag()%>' for_inp="<%=DefineReport.InpText.SHNCD.getObj()%>_F4" data-options="label:'原価',labelWidth:80,required:true,min:0" style="width:200px;text-align: right;">
		</td>
		<td>
			<input class="easyui-numberbox" tabindex="7" col="F14" id="<%=DefineReport.InpText.RG_BAIKAAM.getObj()%>" check='<%=DefineReport.InpText.RG_BAIKAAM.getMaxlenTag()%>' for_inp="<%=DefineReport.InpText.SHNCD.getObj()%>_F5" data-options="label:'　総売価',labelWidth:80,required:true,min:0" style="width:140px;text-align: right;">
		</td>
		<td>
			<input class="easyui-numberbox" tabindex="-1"  col="F15" id="<%=DefineReport.InpText.BAIKAAM.getObj()%>"  for_inp="<%=DefineReport.InpText.SHNCD.getObj()%>_F6" data-options="label:'　本体売価',labelWidth:80,required:true" style="width:140px;text-align: right;" disabled="disabled">
		</td>
	</tr>
	<tr>
		<td>
			<input class="easyui-numberbox" tabindex="8" col="F16" id="<%=DefineReport.InpText.HTDT.getObj()%>" check='<%=DefineReport.InpText.HTDT.getMaxlenTag()%>' data-options="prompt:'<%=DefineReport.Label.PROMPT_DTW.getTxt()%>',label:'発注日',labelWidth:80,required:true" style="width:200px;">
		</td>
		<td>
			<input class="easyui-numberbox" tabindex="9" col="F17" id="<%=DefineReport.InpText.NNDT.getObj()%>" check='<%=DefineReport.InpText.NNDT.getMaxlenTag()%>' data-options="prompt:'<%=DefineReport.Label.PROMPT_DTW.getTxt()%>',label:'納入日',labelWidth:80,required:true" style="width:200px;">
			<!-- 非表示でデータを保持 -->
			<input type="hidden" name="<%=DefineReport.Text.SHORIDT.getObj()%>" id="<%=DefineReport.Text.SHORIDT.getObj()%>" />
			<input type="hidden" col="F22" name="txt_jutenkaikbn" id="txt_jutenkaikbn" />
			<input type="hidden" col="F23" name="txt_rankno_add" id="txt_rankno_add" />
			<input type="hidden" col="F24" name="txt_htsu" id="txt_htsu" />
			<input type="hidden" col="F25" name="txt_suryoptn" id="txt_suryoptn" />
		</td>
	</tr>
</table>
</div>
<div data-options="region:'center',border:false" style="display:none;">
<table>
	<tr>
		<td style = "vertical-align:top">
		<div>
			<table id="grd_tenhtsu_arr" class="easyui-datagrid" tabindex="10" data-options="singleSelect:true,rownumbers:true,width:470,height:350">
				<thead>
					<tr>
					<th data-options="field:'TENCD',width:60,halign:'center',align:'left',editor:{type:'numberbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},formatter:function(value){var targetId = $.id_inp.txt_tencd;var check = $('#'+targetId).attr('check') ? JSON.parse('{'+$('#'+targetId).attr('check')+'}'): JSON.parse('{}');return $.getFormatLPad(value, check.maxlen);}">店コード</th>
					<th data-options="field:'TENKN',width:300,halign:'center',editor:{type:'textbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:true}}">店舗名</th>
					<th data-options="field:'SURYO',width:60,halign:'center',align:'right',formatter:function(value,row,index){ return $.getFormat(value, '##,##0');},editor:{type:'numberbox'}">数量</th>
					</tr>
				</thead>
			</table>
			<!-- Editor参照用 -->
			<div class="ref_editor" style="display: none;">
			<input  class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.TENCD.getObj()%>" check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' />
			<input  class="easyui-textbox" tabindex="-1" id="<%=DefineReport.InpText.TENKN.getObj()%>" check='<%=DefineReport.InpText.TENKN.getMaxlenTag()%>' />
			<input  class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.SURYO.getObj()%>" check='<%=DefineReport.InpText.SURYO.getMaxlenTag()%>' data-options="min:0" />
			</div>
		</div>
		</td>
	</tr>
</table>
</div>
</form>
<!-- <form id="gf" class="e_grid">
<div id="list" data-options="region:'center',border:false" style="display:none;">

	EasyUI方式
	<div tabindex="6" id="gridholder" class="easyui-datagrid placeFace" ></div>
</div>
</form> -->
<div id="buttons" data-options="region:'south',border:false" style="display:none;height:30px;padding:0 20px;">
	<div class="btn" style="float: left;">
		<table class="t-layout3">
		<tr>
			<th style="min-width:135px;"></th><th style="min-width:10px;">
			<th style="min-width:135px;"></th><th style="min-width:135px;"></th><th style="min-width:135px;"></th></tr>
		<tr>
		<td><a href="#" title="戻る" id="<%=DefineReport.Button.BACK.getObj()%>" class="easyui-linkbutton" tabindex="11" iconCls="icon-undo" style="width:110px;"><span class="btnTxt">戻る</span></a></td>
		<td></td>
		<td><a href="#" class="easyui-linkbutton" tabindex="12" id="<%=DefineReport.Button.CANCEL.getObj()%>" title="<%=DefineReport.Button.CANCEL.getTxt()%>" iconCls="icon-cancel" style="width:125px;"><%=DefineReport.Button.CANCEL.getTxt()%></a></td>
		<td><a href="#" class="easyui-linkbutton" tabindex="13" id="<%=DefineReport.Button.UPD.getObj()%>" title="<%=DefineReport.Button.UPD.getTxt()%>" iconCls="icon-save" style="width:125px;"><span class="btnTxt"><%=DefineReport.Button.UPD.getTxt()%></span></a></td>
		<td><a href="#" class="easyui-linkbutton" tabindex="14" id="<%=DefineReport.Button.DEL.getObj()%>" title="<%=DefineReport.Button.DEL.getTxt()%>" iconCls="icon-remove" style="width:110px;"><span class="btnTxt"><%=DefineReport.Button.DEL.getTxt()%></span></a></td>
		</tr>
		</table>
	</div>
	<div id="disp_record_info" style="float: right;">
		<span class="labelName" tabindex="-1" style="padding-top: 3px;">
			登録日 <span col="F19" id="<%=DefineReport.Text.ADDDT.getObj()%>"></span>　更新日 <span col="F20" id="<%=DefineReport.Text.UPDDT.getObj()%>"></span>　オペレータ <span col="F18" id="<%=DefineReport.Text.OPERATOR.getObj()%>"></span>
		</span>
		<input type="hidden" col="F21" name="<%=DefineReport.Hidden.UPDDT.getObj()%>" id="<%=DefineReport.Hidden.UPDDT.getObj()%>" />
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
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.JU033.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.control.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.events.js?v=<%=prm %>"></script>
</html>