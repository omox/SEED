
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
	<table id="dTable2" class="dataTable" cellspacing="0" cellpadding="0">
		<tr>
			<td class="labelCell" style="width: 50px;text-align:center;">部門</td>
			<td style="border-style: none;"></td>
			<td class="labelCell" style="width: 70px;text-align:center;">ランクNo.</td>
			<td style="border-style: none;"></td>
			<td class="labelCell" style="width:500px;text-align:center;">ランク名称</td>
			<td style="border-style: none;"></td>
			<td class="labelCell" style="width: 50px;text-align:center;">臨時</td>
			<td style="border-style: none;"></td>
			<td class="labelCell" style="width:100px;text-align:center;">催しコード</td>
		</tr>
		<tr>
			<td class="col_txt" style="height: 20px;"><span id="<%=DefineReport.InpText.BMNCD.getObj()%>"col="F1"></span></td>
			<%-- <td class="col_txt" style="height: 20px;"><input  class="easyui-numberbox"   tabindex="-1" id="<%=DefineReport.InpText.BMNCD.getObj()%>" check='<%=DefineReport.InpText.BMNCD.getMaxlenTag()%>' /></td> --%>
			<td style="border-style: none;"></td>
			<td class="col_txt"><span id="<%=DefineReport.InpText.RANKNO.getObj()%>"col="F2"></span></td>
			<td style="border-style: none;"></td>
			<td class="col_txt"><span id="<%=DefineReport.InpText.RANKKN.getObj()%>"col="F3"></span></td>
			<td style="border-style: none;"></td>
			<td class="col_chk" style="width:35px;"><label class="chk_lbl"><input type="checkbox" tabindex="-1" id="<%=DefineReport.Checkbox.RINJI.getObj()%>" style="vertical-align:middle;"></label></td>
			<td style="border-style: none;"></td>
			<td class="col_txt" ><span id="<%=DefineReport.InpText.MOYSCD.getObj()%>"col="F4"></span></td>
			<td style="border-style: none;display:none"><a href="#" title="実績参照" id="<%=DefineReport.Button.JISSEKIREFER.getObj()%>" class="easyui-linkbutton" tabindex="-1" iconCls="icon-search"><span class="btnTxt">実績参照</span></a></td>
		</tr>
	</table>
	<!-- <a href="#" title="検索" id="btn_search" class="easyui-linkbutton" tabindex="5" iconCls="icon-search"><span class="btnTxt">検索</span></a> -->

</div>
</form>
<form id="gf" class="e_grid" >
	<div data-options="region:'west',border:false" style = "display:none;">
		<div class="easyui-datagrid " tabindex="1" id="<%=DefineReport.Grid.RANKTENINFO.getObj()%>" data-options="singleSelect:true,rownumbers:true,fit:true" style="width:760px;"></div>
	</div>
</form>
	<div data-options="region:'center',border:false" >
		<table>
			<tr>
				<td>
					<table>
						<tr><td>店舗数</td></tr>
						<tr><td><input class="easyui-numberbox" tabindex="1003" id="<%=DefineReport.Text.TEN_NUMBER.getObj()%>"  style="width:60px;text-align: right;" value=""></td></tr>
						<tr>
							<td class="col_btn"><a href="#" title="店番順" id="<%=DefineReport.Button.TENNOORDER.getObj()%>" class="easyui-linkbutton" tabindex=2 iconCls="icon-search"><span class="btnTxt">店番順</span></a></td>
						</tr>
						<tr>
							<td class="col_btn"><a href="#" title="ランク順" id="<%=DefineReport.Button.RANKORDER.getObj()%>" class="easyui-linkbutton" tabindex="3" iconCls="icon-search"><span class="btnTxt">ランク順</span></a></td>
						</tr>
						<tr>
							<td class="col_btn"><a href="#" title="実績順" id="<%=DefineReport.Button.JISSEKIORDER.getObj()%>" class="easyui-linkbutton" tabindex="4" iconCls="icon-search"><span class="btnTxt">実績順</span></a></td>
						</tr>
					</table>
				</td>
			</tr>
		</table>
	</div>
<div id="buttons" data-options="region:'south',border:false" style="display:none;">
	<div class="btn" style="float: left;">
		<table class="t-layout3">
		<tr><th style="min-width:120px;"></th><th style="min-width:120px;"></th><th style="min-width:120px;"></th></tr>
		<tr>
		<td><a href="#" title="戻る" id="<%=DefineReport.Button.BACK.getObj()%>" class="easyui-linkbutton" tabindex="5" iconCls="icon-undo" style="width:110px;"><span class="btnTxt">戻る</span></a></td>
		</tr>
		</table>
	</div>
	<div id="disp_record_info" style="float: right;">
		<span class="labelName" tabindex="-1" style="padding-top: 3px;">
			登録日 <span col="F5" id="<%=DefineReport.Text.ADDDT.getObj()%>"></span>　更新日 <span col="F6" id="<%=DefineReport.Text.UPDDT.getObj()%>"></span>　オペレータ <span col="F7" id="<%=DefineReport.Text.OPERATOR.getObj()%>"></span>
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
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.ST007.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.control.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.events.js?v=<%=prm %>"></script>
</html>