
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
	<table style="float:left;" data-options="rownumbers:true">
	<tr>
		<td style="width:900px;">
			<input class="easyui-numberbox" id="<%=DefineReport.InpText.TENCD.getObj()%>"  style="width:110px;" value="" data-options="label:'リーダー店',labelWidth:70,required:true" check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' disabled="disabled">
			<input class="easyui-textbox" col="F1" id="<%=DefineReport.InpText.TENKN.getObj()%>"  style="width:350px;" value="" data-options="label:'号店',labelWidth:30,required:true" disabled="disabled">
			<div style="float:right;height:20px;padding:3px 20px 0;">
				<label class="chk_lbl" >売価選択禁止<input type="checkbox" col="F9"  disabled="disabled" /></label><br>
				<label class="chk_lbl" >店不採用禁止<input type="checkbox" col="F10" disabled="disabled" /></label><br>
				<label class="chk_lbl" >商品選択禁止<input type="checkbox" col="F11"  disabled="disabled" /></label><br>
				<label class="chk_lbl" >1日遅ﾊﾟﾀﾝ有 <input type="checkbox" col="F12"  disabled="disabled" /></label><br>
			</div>
		</td>
	</tr>
	<tr>
		<td>
			<input class="easyui-textbox" col="F2" id="<%=DefineReport.InpText.TENGPKN.getObj()%>"  style="width:350px;" value="" data-options="label:'グループ',labelWidth:70,required:true" disabled="disabled">
			<select class="easyui-combobox" col="F3" id="<%=DefineReport.Select.TENKN.getObj()%>" style="width:420px;" value="" disabled="disabled"></select>
		</td>
	</tr>
	<tr>
		<td>
			<input class="easyui-textbox"  col="F4"   style="width:260px;" value="" data-options="label:'販売期間',labelWidth:70,required:true" disabled="disabled">
			<input class="easyui-textbox"  col="F5"   style="width:360px;" value="" data-options="label:'催し名称',labelWidth:65,required:true" disabled="disabled">
		</td>
	</tr>
	<tr>
	<td>
	<div style="float:left;height:40px;padding:6px 0px 0;" >
		<fieldset style="width:120px">
			<div class="inp_box">
				<label class="rad_lbl"><input type="radio" col="F6" tabindex="1" name="<%=DefineReport.Radio.ADOPT.getObj()%>" value="1" style="vertical-align:middle;" checked="checked"/>採用</label>
				<label class="rad_lbl"><input type="radio" col="F6" tabindex="2" name="<%=DefineReport.Radio.ADOPT.getObj()%>" value="0" style="vertical-align:middle;" />不採用</label>
			</div>
		</fieldset>
	</div>
	<div style="float:left;height:40px;padding:6px 8px 0;" id = "baikaLegend" style="float:left;">
		<fieldset style="width:240px">
			<div class="inp_box">
				<label class="rad_lbl"><input type="radio" col="F7" tabindex="3" name="<%=DefineReport.Radio.URIASELKBN.getObj()%>" id = "baika1"  value="1" style="vertical-align:middle;" checked="checked"/>A総売価</label>
				<label class="rad_lbl"><input type="radio" col="F7" tabindex="4" name="<%=DefineReport.Radio.URIASELKBN.getObj()%>" id = "baika2"  value="2" style="vertical-align:middle;" />B総売価</label>
				<label class="rad_lbl"><input type="radio" col="F7" tabindex="5" name="<%=DefineReport.Radio.URIASELKBN.getObj()%>" id = "baika3"  value="3" style="vertical-align:middle;" />C総売価</label>
			</div>
		</fieldset>
	</div>
	<div style="float:left;padding:10px 25px 0;">

		<input class="easyui-textbox"  col="F8"  style="width:60px;" value="" data-options="label:'回答',labelWidth:30,required:true" disabled="disabled">
	</div>
	<div  id = "searchCombo" style="padding:10px 25px 0;">
		<input type="hidden" col="F13" name="<%=DefineReport.Hidden.JLSTCREFLG.getObj()%>" id="<%=DefineReport.Hidden.JLSTCREFLG.getObj()%>" />
		<input type="hidden" col="F14" name="<%=DefineReport.Hidden.UPDDT.getObj()%>" id="<%=DefineReport.Hidden.UPDDT.getObj()%>" />
	</div>
	<p style="clear:both;"></p>
	</td>
	</tr>
	<tr>
	</tr>
	</table>
</div>
</form>
<form id="gf" class="e_grid">
<div id="list" data-options="region:'center',border:false" style="display:none;">

	<!-- EasyUI方式 -->
	<div tabindex="71" id="gridholder" class="easyui-datagrid placeFace" ></div>
</div>
</form>
<div id="buttons" data-options="region:'south',border:false" style="display:none;">
	<div class="btn" style="float: left;">
		<table class="t-layout3">
		<tr>
			<th style="min-width:135px;"></th><th style="min-width:10px;"></th>
			<th style="min-width:135px;"></th><th style="min-width:135px;"></th>
		</tr>
		<tr>
		<td><a href="#" title="戻る" id="<%=DefineReport.Button.BACK.getObj()%>" class="easyui-linkbutton" tabindex="101" iconCls="icon-undo" style="width:125px;"><span class="btnTxt">戻る</span></a></td>
		<td></td>
		<td><a href="#" title="キャンセル" class="easyui-linkbutton" tabindex="102" id="<%=DefineReport.Button.CANCEL.getObj()%>" iconCls="icon-cancel" style="width:125px;"><span>キャンセル</span></a></td>
		<td><a href="#" title="登録" class="easyui-linkbutton" tabindex="103" id="<%=DefineReport.Button.UPD.getObj()%>" title="<%=DefineReport.Button.UPD.getTxt()%>" iconCls="icon-save" style="width:110px;"><span class="btnTxt"><%=DefineReport.Button.UPD.getTxt()%></span></a></td>
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
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.SA003.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.control.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.events.js?v=<%=prm %>"></script>
</html>