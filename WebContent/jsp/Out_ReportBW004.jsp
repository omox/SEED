
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
	<table class="t-layout3">
	<tr>
		<td>
		<input class="easyui-numberbox" tabindex="1" col="F1" style="width:160px;" id="<%=DefineReport.InpText.HBSTDT.getObj()%>" check='<%=DefineReport.InpText.HBSTDT.getMaxlenTag()%>' value="" data-options="prompt:'<%=DefineReport.Label.PROMPT_DTW.getTxt()%>',label:'販売開始日',labelWidth:70,required:true">
		<input class="easyui-numberbox" tabindex="2" col="F2" style="width:70px;" id="<%=DefineReport.InpText.BMNCD.getObj()%>" check='<%=DefineReport.InpText.BMNCD.getMaxlenTag()%>' value="" data-options="label:'部門',labelWidth:30,required:true">
		<input class="easyui-textbox"   tabindex="3" col="F3" style="width:260px;" id="<%=DefineReport.InpText.MEISHOKN.getObj()%>" check='<%=DefineReport.InpText.MEISHOKN.getMaxlenTag()%>' value="" data-options="label:'名称',labelWidth:30,required:true">
		<!-- <input class="easyui-textbox"   col="F4" style="width:90px;" id="txt_waribiki"  value="" data-options="label:'割引率',labelWidth:50,required:true" disabled="disabled">
		<input class="easyui-textbox"   col="F5" style="width:130px;" id="txt_seisi"  value="" data-options="label:'正規・カット',labelWidth:80,required:true" disabled="disabled"> -->

		<select class="easyui-combobox" tabindex="4" col="F4" id="<%=DefineReport.MeisyoSelect.KBN103021.getObj()%>" style="width:120px;" data-options="label:'割引率',labelWidth:50,required:true"></select>
		<select class="easyui-combobox" tabindex="5" col="F5" id="<%=DefineReport.MeisyoSelect.KBN10303.getObj()%>"  style="width:160px;" data-options="label:'正規・カット',labelWidth:80,required:true"></select>
		</td>
	</tr>
	<tr>
	<td>
		<input class="easyui-numberbox" tabindex="6" col="F6"   style="width:180px;" id="<%=DefineReport.InpText.DUMMYCD.getObj()%>" check='<%=DefineReport.InpText.DUMMYCD.getMaxlenTag()%>' value="" data-options="prompt:'____-____',label:'ダミーコード',labelWidth:90,required:true">
		<input class="easyui-textbox"   tabindex="7" col="F7"   style="width:370px;" id="<%=DefineReport.InpText.POPKN.getObj()%>" check='<%=DefineReport.InpText.POPKN.getMaxlenTag()%>' value="" data-options="label:'ダミーコード名称',labelWidth:130,required:true" for_inp="<%=DefineReport.InpText.DUMMYCD.getObj()%>_F1" disabled="disabled">
		<input type="hidden" col="F8" name="<%=DefineReport.Hidden.WRITUKBN.getObj()%>" id="<%=DefineReport.Hidden.WRITUKBN.getObj()%>" />
		<input type="hidden" col="F9" name="<%=DefineReport.Hidden.SEICUTKBN.getObj()%>" id="<%=DefineReport.Hidden.SEICUTKBN.getObj()%>" />
		<input type="hidden" col="F10" name="hiddenInputFlg" id="hiddenInputFlg" />
		<input type="hidden" name="<%=DefineReport.Text.SHORIDT.getObj()%>" id="<%=DefineReport.Text.SHORIDT.getObj()%>" />
	</td>
	</tr>

	</table>
</div>
</form>
<form id="gf" class="e_grid">
<div id="list" data-options="region:'center',border:false" style="display:none;">
	<div class="easyui-datagrid" tabindex="9" id="gridholder"></div>
	<!-- Editor参照用 -->
	<div class="ref_editor" style="display: none;">
	<%-- <input  class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.SHNCD.getObj()%>"  check='<%=DefineReport.InpText.SHNCD.getMaxlenTag()%>'/>
	<input  class="easyui-textbox" tabindex="-1" id="<%=DefineReport.InpText.MAKERKN.getObj()%>"  style="width:80px;" for_inp="<%=DefineReport.InpText.SHNCD.getObj()%>_F3"/ >
	<input  class="easyui-textbox" tabindex="-1" id="<%=DefineReport.InpText.SHNKN.getObj()%>"  style="width:80px;" for_inp="<%=DefineReport.InpText.SHNCD.getObj()%>"/ >
	<input  class="easyui-textbox" tabindex="-1" id="<%=DefineReport.InpText.KIKKN.getObj()%>"  style="width:80px;" for_inp="<%=DefineReport.InpText.SHNCD.getObj()%>_F5"/>
	<input  class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.IRISU.getObj()%>"  style="width:80px;" for_inp="<%=DefineReport.InpText.SHNCD.getObj()%>_F6"/>
	<input  class="easyui-numberbox" tabindex="-1" id="txt_sougakubaika" style="width:80px;" for_inp="<%=DefineReport.InpText.SHNCD.getObj()%>_F7"/>
	<input  class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.RG_BAIKAAM.getObj()%>"  style="width:80px;"for_inp="<%=DefineReport.InpText.SHNCD.getObj()%>_F8"/>
	<input  class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.RG_GENKAAM.getObj()%>"  style="width:80px;" for_inp="<%=DefineReport.InpText.SHNCD.getObj()%>_F9"/>
	<input  class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.BAIKAAM.getObj()%>" check='<%=DefineReport.InpText.BAIKAAM.getMaxlenTag()%>' style="width:80px;" for_inp="<%=DefineReport.InpText.BAIKAAM.getObj()%>_F10"/>
	<input  class="easyui-numberbox" tabindex="-1" id="txt_hontaibaika" style="width:80px;" for_inp="<%=DefineReport.InpText.BAIKAAM.getObj()%>_F11"/>
	<input  class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.GENKAAM.getObj()%>" check='<%=DefineReport.InpText.GENKAAM.getMaxlenTag()%>' style="width:80px;" "/>
	<input  class="easyui-textbox" tabindex="-1" id="<%=DefineReport.InpText.SHOBRUIKN.getObj()%>" style="width:80px;" for_inp="<%=DefineReport.InpText.SHNCD.getObj()%>_F11"/> --%>

		<input  class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.SHNCD.getObj()%>"  style="width:80px;" check='<%=DefineReport.InpText.SHNCD.getMaxlenTag()%>'/>
	<input  class="easyui-textbox" tabindex="-1" id="<%=DefineReport.InpText.MAKERKN.getObj()%>"  style="width:80px;" for_inp="<%=DefineReport.InpText.SHNCD.getObj()%>_F3"/ >
	<input  class="easyui-textbox" tabindex="-1" id="<%=DefineReport.InpText.SHNKN.getObj()%>"  style="width:80px;" for_inp="<%=DefineReport.InpText.SHNCD.getObj()%>_F4" check='<%=DefineReport.InpText.SHNKN.getMaxlenTag()%>'/ >
	<input  class="easyui-textbox" tabindex="-1" id="<%=DefineReport.InpText.KIKKN.getObj()%>"  style="width:80px;" for_inp="<%=DefineReport.InpText.SHNCD.getObj()%>_F5" check='<%=DefineReport.InpText.KIKKN.getMaxlenTag()%>'/>
	<input  class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.IRISU.getObj()%>"  style="width:80px;" for_inp="<%=DefineReport.InpText.SHNCD.getObj()%>_F6" check='<%=DefineReport.InpText.IRISU.getMaxlenTag()%>'/>
	<input  class="easyui-numberbox" tabindex="-1" id="txt_sougakubaika" style="width:80px;" for_inp="<%=DefineReport.InpText.SHNCD.getObj()%>_F7"/>
	<input  class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.RG_BAIKAAM.getObj()%>"  style="width:80px;"for_inp="<%=DefineReport.InpText.SHNCD.getObj()%>_F8"/>
	<input  class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.RG_GENKAAM.getObj()%>"  style="width:80px;" for_inp="<%=DefineReport.InpText.SHNCD.getObj()%>_F9"/>
	<input  class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.BAIKAAM.getObj()%>" check='<%=DefineReport.InpText.BAIKAAM.getMaxlenTag()%>' style="width:80px;" for_inp="<%=DefineReport.InpText.BAIKAAM.getObj()%>_F10"/>
	<input  class="easyui-numberbox" tabindex="-1" id="txt_hontaibaika" style="width:80px;" for_inp="<%=DefineReport.InpText.BAIKAAM.getObj()%>_F11"/>
	<input  class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.GENKAAM.getObj()%>" check='<%=DefineReport.InpText.GENKAAM.getMaxlenTag()%>' style="width:80px; "/>
	<input  class="easyui-textbox" tabindex="-1" id="<%=DefineReport.InpText.SHOBRUIKN.getObj()%>" style="width:80px;" for_inp="<%=DefineReport.InpText.SHNCD.getObj()%>_F13"/>
	</div>
</div>
</form>
<div id="buttons" data-options="region:'south',border:false" style="display:none;">
	<div class="btn" style="float: left;">
		<table class="t-layout3">
		<tr>
			<th style="min-width:135px;"></th><th style="min-width:10px;"></th>
			<th style="min-width:135px;"></th><th style="min-width:135px;"></th><th style="min-width:135px;"></th><th style="min-width:135px;"></th>
		</tr>
		<tr>
		<td><a href="#" title="戻る" id="<%=DefineReport.Button.BACK.getObj()%>" class="easyui-linkbutton" tabindex="10" iconCls="icon-undo" style="width:125px;"><span class="btnTxt">戻る</span></a></td>
		<td></td>
		<td><a href="#" title="キャンセル" class="easyui-linkbutton" tabindex="11" id="<%=DefineReport.Button.CANCEL.getObj()%>" iconCls="icon-cancel" style="width:125px;"><span>キャンセル</span></a></td>
		<td><a href="#" title="登録" class="easyui-linkbutton" tabindex="12" id="<%=DefineReport.Button.UPD.getObj()%>" title="<%=DefineReport.Button.UPD.getTxt()%>" iconCls="icon-save" style="width:125px;"><span class="btnTxt"><%=DefineReport.Button.UPD.getTxt()%></span></a></td>
		<td><a href="#" title="削除"   class="easyui-linkbutton" tabindex="13" id="<%=DefineReport.Button.DEL.getObj()%>" title="<%=DefineReport.Button.DEL.getTxt()%>" iconCls="icon-remove" style="width:125px;"><span class="btnTxt" id="del" >削除</span></a></td>
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
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.BW004.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.control.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.events.js?v=<%=prm %>"></script>
</html>