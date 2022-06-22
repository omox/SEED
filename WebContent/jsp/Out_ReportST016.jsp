
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

	<tr><th style="min-width:35px;"></th><th style="min-width:250px;"></th><th style="min-width:25px;"></th><th style="min-width:250px;"></th><th style="min-width:120px;"></th></tr>
	<tr>
	<td colspan="6">
		<table id="dTable2" class="dataTable">
		<tr>
			<td class="labelCell" style="width: 30px;">週No.</td>
			<td class="labelCell" style="width: 95px;">催しコード</td>
			<td class="labelCell" style="width:300px;">催し名称</td>
			<td class="labelCell" style="width:175px;">催し期間</td>
			<td class="labelCell" style="width: 70px;">チラシ締め切</td>
		</tr>
		<tr>
			<td class="col_lbl_c"><span col="F4" id="<%=DefineReport.InpText.SHUNO.getObj()%>">　</span></td>
			<td class="col_txt" style="height: 20px;"><span col="F1"></span></td>
			<td class="col_txt"><span col="F2"></span></td>
			<td class="col_txt"><span col="F3"></span></td>
			<td class="col_txt"><span col="F5"></span></td>
		</tr>
		</table>
	</td>
	</tr>
	<tr>
	<td>部門</td>
	<td><select class="easyui-combobox" tabindex="1" id="<%=DefineReport.Select.BUMON.getObj()%>" style="width:250px;"></select></td>
	<td>BY</td>
	<td class="col_lbl_c"><select class="easyui-combobox" tabindex="2" id="<%=DefineReport.Select.BYCD.getObj()%>" style="width:250px;"></select></td>
	<td class="col_btn" style="vertical-align: middle;">
		<a href="#" title="検索" id="<%=DefineReport.Button.SEARCH.getObj()%>" class="easyui-linkbutton" tabindex="3" iconCls="icon-search"><span class="btnTxt">検索</span></a>
	</td>
	<td><div style="width:200px;"></div></td>
	</tr>
	</table>
</div>
</form>
<form id="gf" class="e_grid">
<div id="list" data-options="region:'center',border:false" style="display:none;">

	<!-- EasyUI方式 -->
	<div tabindex="4" id="gridholder" class="easyui-datagrid placeFace" ></div>
</div>
<!-- Editor参照用 -->
<div class="ref_editor" style="display: none;">
	<input type="checkbox" tabindex="-1" id="<%=DefineReport.Checkbox.SEL.getObj()%>" disabled="disabled" readonly="readonly"/>
	<input type="checkbox" tabindex="-1" id="<%=DefineReport.Checkbox.USE.getObj()%>"/>
</div>
</form>
<div id="buttons" data-options="region:'south',border:false" style="display:none;">
	<div class="btn" style="float: left;">
		<table class="t-layout3">
		<tr>
			<th style="min-width:140px;"></th><th style="min-width:10px;"></th>
			<th style="min-width:140px;"></th><th style="min-width:140px;"></th><th style="min-width:140px;"></th><th style="min-width:10px;"></th>
			<th style="min-width:140px;"></th>
		</tr>
		<tr>
			<td></td>
			<td></td>
			<td colspan="2"><a href="#" title="選択(販売・納入情報)" id="<%=DefineReport.Button.SEL_CHANGE.getObj()%>" class="easyui-linkbutton" tabindex="7" iconCls="icon-ok" style="width:270px;"><span>選択(販売・納入情報)</span></a></td>
			<td><a href="#" title="全選択/全解除" id="<%=DefineReport.Button.SELECT.getObj()%>" class="easyui-linkbutton" tabindex="7" iconCls="icon-ok" style="width:130px;"><span>全選択/全解除</span></a></td>
			<td></td>
			<td><a href="#" title="特売原稿CSV出力" id="<%=DefineReport.Button.CSV.getObj()%>1" class="easyui-linkbutton" tabindex="15" iconCls="icon-ok" style="width:130px;"><span>特売原稿CSV出力</span></a></td>
		</tr>
		<tr>
			<td><a href="#" title="戻る" id="<%=DefineReport.Button.BACK.getObj()%>" class="easyui-linkbutton" tabindex="6" iconCls="icon-undo" style="width:130px;"><span class="btnTxt">戻る</span></a></td>
			<td></td>
			<td><a href="#" title="新規" id="<%=DefineReport.Button.NEW.getObj()%>1" class="easyui-linkbutton" tabindex="7" iconCls="icon-ok" style="width:130px;"><span>新規</span></a></td>
			<td><a href="#" title="新規コピー" id="<%=DefineReport.Button.COPY.getObj()%>" class="easyui-linkbutton" tabindex="7" iconCls="icon-ok" style="width:130px;"><span>新規コピー</span></a></td>
			<td><a href="#" title="新規(全品割引)" id="<%=DefineReport.Button.NEW.getObj()%>2" class="easyui-linkbutton" tabindex="7" iconCls="icon-ok" style="width:130px;"><span>新規(全品割引)</span></a></td>
			<td></td>
			<td><a href="#" title="店別数量CSV出力" id="<%=DefineReport.Button.CSV.getObj()%>2" class="easyui-linkbutton" tabindex="16" iconCls="icon-ok" style="width:130px;"><span>店別数量CSV出力</span></a></td>
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
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.ST016.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.control.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.events.js?v=<%=prm %>"></script>
</html>