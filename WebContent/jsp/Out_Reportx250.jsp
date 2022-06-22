
<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8"%>

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
	String userYobi1 = lusr.getYobi1_();
	String ShoninUser = "0000002";

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
<link rel="stylesheet" type="text/css" href="../css/report.css?v=<%=prm%>">
<link rel="stylesheet" type="text/css" href="../themes/easyui/default/easyui.css?v=<%=prm%>">
<link rel="stylesheet" type="text/css" href="../themes/easyui/default/custom_easyui.css?v=<%=prm%>">
<link rel="stylesheet" type="text/css" href="../themes/easyui/icon.css?v=<%=prm%>">
</head>
<body style="overflow-x: hidden;">

<div id="cc" class="easyui-layout" data-options="fit:true">
	<form id="ff" method="post" style="display: inline">
		<div id="search" data-options="region:'north',border:false" style="display:none;" >
			<table class="t-layout1">
				<tr>
					<td><%=DefineReport.Select.BUMON.getTxt() %></td>
					<td><input class="easyui-combobox" tabindex="1" id="<%=DefineReport.Select.BUMON.getObj() %>" style="width:150px"></td>
				</tr>
				<tr>
					<td><%=DefineReport.Text.SHOHIN.getTxt() %></td>
					<td><input class="easyui-textbox" tabindex="3" id="<%=DefineReport.Text.SHOHIN.getObj() %>" style="width:150px"></td>
					<td><%=DefineReport.Text.HATYU.getTxt() %>コード</td>
					<td><input class="easyui-textbox" tabindex="4" id="<%=DefineReport.Text.HATYU.getObj() %>" style="width:150px"></td>
				</tr>
				<tr>
					<td>商品登録日</td>
					<td colspan="3">
						<input class="easyui-numberbox" tabindex="5" id="<%=DefineReport.InpText.YMD_F.getObj() %>" check='<%=DefineReport.InpText.FROM_DATE.getMaxlenTag()%>' style="width:150px">
						<span>　～　</span>
						<input class="easyui-numberbox" tabindex="6" id="<%=DefineReport.InpText.YMD_T.getObj() %>" check='<%=DefineReport.InpText.TO_DATE.getMaxlenTag()%>' style="width:150px">
					</td>
				</tr>
			</table>
			<table>
				<tr>
					<td>
						<a href="#" id="<%=DefineReport.Button.SEARCH.getObj()%>" title="<%=DefineReport.Button.SEARCH.getTxt()%>" class="easyui-linkbutton" tabindex="7" iconCls="icon-search"><span class="btnTxt"><%=DefineReport.Button.SEARCH.getTxt()%></span></a>
						<a href="#" id="<%=DefineReport.Button.EXCEL.getObj()%>" title="<%=DefineReport.Button.EXCEL.getTxt()%>" class="easyui-linkbutton" tabindex="8" iconCls="icon-excel"><span class="btnTxt"><%=DefineReport.Button.EXCEL.getTxt()%></span></a>
						<a href="#" id="<%=DefineReport.Button.DOWNLOAD.getObj()%>" title="CSVダウンロード" class="easyui-linkbutton" tabindex="9" iconCls="icon-excel">CSVダウンロード</a>

						<span style="margin-right: 30px;">&nbsp;</span>
						<a href="#" id="<%=DefineReport.Button.NEXT_SHIKAKARI.getObj()%>" title="<%=DefineReport.Button.NEXT_SHIKAKARI.getTxt()%>" class="easyui-linkbutton" tabindex="10" iconCls=""><span class="btnTxt"><%=DefineReport.Button.NEXT_SHIKAKARI.getTxt()%></span></a>
						<a href="#" id="<%=DefineReport.Button.DELETE.getObj()%>" title="<%=DefineReport.Button.DELETE.getTxt()%>" class="easyui-linkbutton" tabindex="11" iconCls=""><span class="btnTxt"><%=DefineReport.Button.DELETE.getTxt()%></span></a>
						<span style="margin-right: 30px;">&nbsp;</span>
						<a href="#" id="<%=DefineReport.Button.PREV.getObj()%>" title="<%=DefineReport.Label.KYAKKA.getTxt()%>" class="easyui-linkbutton" tabindex="12" iconCls=""><span class="btnTxt"><%=DefineReport.Label.KYAKKA.getTxt()%></span></a>
					<%if(userYobi1.equals(ShoninUser)){ %>
						<span style="margin-right: 30px;">&nbsp;</span>
						<a href="#" id="<%=DefineReport.Button.NEXT.getObj()%>" title="<%=DefineReport.Label.SYONIN.getTxt()%>" class="easyui-linkbutton" tabindex="14" iconCls=""><span class="btnTxt"><%=DefineReport.Label.SYONIN.getTxt()%></span></a>
					<%}else{ %>
					<%} %>
					</td>
				</tr>
			</table>
		</div>
	</form>

	<form id="gf" class="e_grid">
		<div id="list" data-options="region:'center',border:false" style="display:none;">
			<!-- EasyUI方式 -->
			<div tabindex="20" id="gridholder"  class="easyui-datagrid placeFace" ></div>
		</div>
	</form>
	<!-- ツールバー -->
	<div id="buttons" data-options="region:'south',border:false" style="display:none;">
		<div class="btn" style="float: left;">
			<table class="t-layout3">
				<tr><th style="min-width:135px;"></th><th style="min-width:10px;"></th>
					<th style="min-width:135px;"></th><th style="min-width:135px;"></th><th style="min-width:135px;"></th><th style="min-width:10px;"></th>
					<th style="min-width:135px;"></th><th style="min-width:135px;"></th><th style="min-width:135px;"></th></tr>
				<tr>
					<td><a href="#" title="戻る" id="<%=DefineReport.Button.BACK.getObj()%>" class="easyui-linkbutton" tabindex="21" iconCls="icon-undo" style="width:125px;"><span class="btnTxt">戻る</span></a></td>
				</tr>
			</table>
		</div>
	</div>

	<input type="hidden" id="<%=DefineReport.Hidden.SELECT_IDX.getObj() %>" value="">
	<input type="hidden" id="<%=DefineReport.Hidden.CHANGED_IDX.getObj() %>" value="">
</div>

<div id="debug">
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
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.x250.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.control.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.events.js?v=<%=prm %>"></script>
</html>