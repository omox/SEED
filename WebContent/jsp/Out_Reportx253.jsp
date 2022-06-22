
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
	<div id="reference" style="overflow: hidden;width:100%;">
		<TABLE id="dTable1" class="dataTable" cellspacing="0" cellpadding="0" style="display:inline;">
		<TBODY>
		<TR align="left">
			<TD class="labelCell" style="WIDTH:130px;">オペレーターコード</TD>
			<TD class="labelCell" style="WIDTH: 70px;">処理日</TD>
			<TD class="labelCell" style="WIDTH: 70px;">時刻</TD>
			<TD class="labelCell" style="WIDTH:200px;">コメント</TD>
			<TD style="border: none;width:50%;"></TD>
			<TD class="labelCell" style="WIDTH: 70px;">エラー件数</TD>
		</TR>
		<TR align="left">
			<TD><input class="easyui-textbox" col="F2" tabindex="-1" id="<%=DefineReport.Text.OPERATOR.getObj()%>" data-options="cls:'labelInput'" style="width:125px;" readonly="readonly"></TD>
			<TD><input class="easyui-numberbox" col="F3" tabindex="-1" id="<%=DefineReport.Text.UPDDT.getObj()%>" data-options="prompt:'__/__/__',cls:'labelInput'" style="width:65px;" readonly="readonly"></TD>
			<TD><input class="easyui-numberbox" col="F4" tabindex="-1" id="<%=DefineReport.Text.UPDTM.getObj()%>" data-options="prompt:'__:__:__',cls:'labelInput'" style="width:65px;" readonly="readonly"></TD>
			<TD><input class="easyui-textbox" col="F5" tabindex="-1" id="<%=DefineReport.InpText.COMMENTKN.getObj()%>" data-options="cls:'labelInput'" style="width:195px;" readonly="readonly"></TD>
			<TD style="border: none;"></TD>
			<TD><input class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.Text.ERR_NUMBER.getObj()%>" data-options="cls:'labelInputNum'" style="width:65px;text-alien:right;" readonly="readonly"></TD>
		</TR></TBODY></TABLE>
		<input type="hidden" tabindex="-1" id="<%=DefineReport.Text.TABLEKBN.getObj()%>" value=""/>
		<input type="hidden" col="F1" tabindex="-1" id="<%=DefineReport.Text.SEQ.getObj()%>" value=""/>
	</div>
</div>
</form>
<form id="gf" class="e_grid">
<div id="list" data-options="region:'center',border:false" style="display:none;">

	<!-- EasyUI方式 -->
	<div tabindex="1" id="gridholder" class="easyui-datagrid placeFace" ></div>
</div>
</form>
<div id="buttons" data-options="region:'south',border:false" style="display:none;">
	<div class="btn" style="float: left;">
		<table class="t-layout3">
		<tr><th style="min-width:135px;"></th><th style="min-width:10px;"></th>
			<th style="min-width:135px;"></th></tr>
		<tr>
		<td><a href="#" title="戻る" id="<%=DefineReport.Button.BACK.getObj()%>" class="easyui-linkbutton" tabindex="2" iconCls="icon-undo" style="width:125px;"><span class="btnTxt">戻る</span></a></td>
		<td></td>
		<td><a href="#" title="選択(修正)" id="<%=DefineReport.Button.ERR_CHANGE.getObj()%>" class="easyui-linkbutton" tabindex="3" iconCls="icon-ok" style="width:125px;"><span>選択(修正)</span></a></td>
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


	<!-- 初期条件情報(JSON文字列変換済 "'" で囲む) -->
	<input type="hidden" name="reportYobi1" id="reportYobi1" value='<%=reportYobi1 %>' />
	<input type="hidden" name="reportYobi2" id="reportYobi2" value='<%=reportYobi2 %>' />
</div>

<div style="display:none;">
	<jsp:include page="Out_Reportwin001.jsp" flush="true" />
 	<jsp:include page="Out_Reportwin002.jsp" flush="true" />
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
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.x253.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.win001.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.win002.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.control.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.events.js?v=<%=prm %>"></script>
</html>