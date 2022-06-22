
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
		<td>
			<table>
			<tr><td style = "height:24px">部門コード</td></tr>
			<tr><td style = "height:24px">数量パターンNo.</td></tr>
			<tr><td style = "height:24px">数量パターン名称</td></tr>
			</table>
		</td>
		<td>
			<table>
			<tr><td><input class="easyui-numberbox" tabindex="1" col="F1" id="<%=DefineReport.InpText.BMNCD.getObj()%>" check='<%=DefineReport.InpText.BMNCD.getMaxlenTag()%>' style="width:60px;height:24px;" value=""></td></tr>
			<tr><td><input class="easyui-numberbox" tabindex="4" col="F4" id="<%=DefineReport.InpText.SRYPTNNO.getObj()%>" check='<%=DefineReport.InpText.SRYPTNNO.getMaxlenTag()%>' style="width:60px;height:24px;" value=""></td></tr>
			<tr><td><input class="easyui-textbox" tabindex="5" col="F5" id="<%=DefineReport.InpText.SRYPTNKN.getObj()%>" check='<%=DefineReport.InpText.SRYPTNKN.getMaxlenTag()%>' style="width:500px;height:24px;" value=""></td></tr>
			</table>
		</td>
		<td style="text-align:left;vertical-align:top;">
			<table id="dTable2" class="dataTable">
			<tr><td class="labelCell" style="width:30px;text-align:center;">臨時</td></tr>
			<tr><td class="col_num" style="height:24px;"><input type="checkbox" tabindex="2" col="F2" id="<%=DefineReport.Checkbox.RINJI.getObj()%>" value="1" /></td></tr>
			</table>
		</td>
		<td style="text-align:left;vertical-align:top;">
			<table id="dTable2" class="dataTable">
			<tr><td class="labelCell" style="width:100px;text-align:center;">催しコード</td></tr>
			<tr><td class="col_num" style="height:20px;text-align:left;"><input class="easyui-numberbox" tabindex="3" col="F3" id="<%=DefineReport.InpText.MOYSCD.getObj()%>" check='<%=DefineReport.InpText.MOYSCD.getMaxlenTag()%>' data-options="prompt:'_-______-___'" style="width:150px;height:24px;text-align:left;" value=""></td></tr>
		</table>
		</td>


<%--
		<td>部門コード</td>
		<td><input class="easyui-numberbox" tabindex="1" col="F1" id="<%=DefineReport.InpText.BMNCD.getObj()%>" check='<%=DefineReport.InpText.BMNCD.getMaxlenTag()%>' style="width:60px;" value=""></td>
		<td>
			<table id="dTable2" class="dataTable">
			<tr>
				<td class="labelCell" style="width: 30px;text-align:center;">臨時</td>
			</tr>
			<tr>
				<td class="col_num" style="height: 20px;"><input type="checkbox" tabindex="2" col="F2" id="<%=DefineReport.Checkbox.RINJI.getObj()%>" value="1" /></td>
			</tr>
			</table>
		</td>
		<td>
			<table id="dTable2" class="dataTable">
			<tr>
				<td class="labelCell" style="width: 100px;text-align:center;">催しコード</td>
			</tr>
			<tr>
				<td class="col_num" style="height: 20px;text-align:left;""><input class="easyui-numberbox" tabindex="3" col="F3" id="<%=DefineReport.InpText.MOYSCD.getObj()%>" check='<%=DefineReport.InpText.MOYSCD.getMaxlenTag()%>' data-options="prompt:'_-______-___'" style="width:150px;text-align:left;" value=""></td>
			</tr>
			</table>
		</td>
	</tr>
	<tr>
		<td>数量パターンNo.</td>
		<td><input class="easyui-numberbox" tabindex="4" col="F4" id="<%=DefineReport.InpText.SRYPTNNO.getObj()%>" check='<%=DefineReport.InpText.SRYPTNNO.getMaxlenTag()%>' style="width:60px;" value=""></td>
	</tr>
	<tr>
		<td>数量パターン名称</td>
		<td><input class="easyui-textbox" tabindex="5" col="F5" id="<%=DefineReport.InpText.SRYPTNKN.getObj()%>" check='<%=DefineReport.InpText.SRYPTNKN.getMaxlenTag()%>' style="width:400px;" value=""></td>
 --%>
	</tr>
	</table>
</div>
<div data-options="region:'center',border:false" style="display:none;padding:2px 5px 0;">
	<div class="easyui-datagrid" tabindex="6" id="<%=DefineReport.Grid.RANKSURYO.getObj()%>_list" data-options="singleSelect:true,rownumbers:true, fit:true"></div>
	<div class="ref_editor" style="display: none;">
		<input  class="easyui-textbox" tabindex="-1" id="<%=DefineReport.InpText.RANK.getObj()%>" check='<%=DefineReport.InpText.RANK.getMaxlenTag()%>' />
		<input  class="easyui-numberbox"   tabindex="-1" id="<%=DefineReport.InpText.SURYO.getObj()%>" check='<%=DefineReport.InpText.SURYO.getMaxlenTag()%>' data-options="min:0"/>
	</div>
</div>
</form>
<div id="buttons" data-options="region:'south',border:false" style="display:none;">
	<div class="btn" style="float: left;">
		<table class="t-layout3">
		<tr><th style="min-width:135px;"></th><th style="min-width:10px;"></th>
			<th style="min-width:135px;"></th><th style="min-width:135px;"></th></tr>
		<tr>
		<td><a href="#" title="戻る" id="<%=DefineReport.Button.BACK.getObj()%>" class="easyui-linkbutton" tabindex="901" iconCls="icon-undo" style="width:125px;"><span class="btnTxt">戻る</span></a></td>
		<td></td>
		<td><a href="#" class="easyui-linkbutton" tabindex="902" id="<%=DefineReport.Button.CANCEL.getObj()%>" title="<%=DefineReport.Button.CANCEL.getTxt()%>" iconCls="icon-cancel" style="width:110px;"><%=DefineReport.Button.CANCEL.getTxt()%></a></td>
		<td><a href="#" class="easyui-linkbutton" tabindex="903" id="<%=DefineReport.Button.UPD.getObj()%>" title="<%=DefineReport.Button.UPD.getTxt()%>" iconCls="icon-save" style="width:110px;"><span class="btnTxt"><%=DefineReport.Button.UPD.getTxt()%></span></a></td>
		<td><a href="#" class="easyui-linkbutton" tabindex="904" id="<%=DefineReport.Button.DEL.getObj()%>" title="<%=DefineReport.Button.DEL.getTxt()%>" iconCls="icon-remove" style="width:110px;"><span class="btnTxt"><%=DefineReport.Button.DEL.getTxt()%></span></a></td>
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
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.RP004.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.control.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.events.js?v=<%=prm %>"></script>
</html>