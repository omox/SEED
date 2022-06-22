
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
		<td style="text-align:left;vertical-align:bottom;">
			<table id="dTable2" class="dataTable">
			<tr>
				<td colspan=4></td>
				<td class="labelCell" style="width:200px;text-align:center;">中分類実績の場合</td>
			</tr>
			<tr>
				<td class="labelCell" style="width: 40px;text-align:center;">部門</td>
				<td class="labelCell" style="width: 60px;text-align:center;">部門実績</td>
				<td class="labelCell" style="width: 70px;text-align:center;">大分類実績</td>
				<td class="labelCell" style="width: 70px;text-align:center;">中分類実績</td>
				<td class="labelCell" style="width: 200px;text-align:center;">大分類選択</td>
				<td class="col_empty"></td>
				<td class="labelCell" style="width:150px;text-align:center;border:1px solid black;background-color:pink" id="message">販売実績から自動生成分</td>
			</tr>
			<tr>
				<td class="col_num" ><input class="easyui-numberbox" tabindex="1" id="<%=DefineReport.InpText.BMNCD.getObj()%>" check='<%=DefineReport.InpText.BMNCD.getMaxlenTag()%>' data-options="required:true" style="height:20px;width:40px;text-align:left;" value=""></td>

				<td class="col_num" style="text-align:center;"><input type="radio" tabindex="2" id="<%=DefineReport.Radio.JISSEKIBUN.getObj()%>" name="<%=DefineReport.Radio.JISSEKIBUN.getObj()%>" value="1" style="height: 20px;vertical-align:middle;" checked="checked"/></td>
				<td class="col_num" style="text-align:center;"><input type="radio" tabindex="3" id="<%=DefineReport.Radio.JISSEKIBUN.getObj()%>" name="<%=DefineReport.Radio.JISSEKIBUN.getObj()%>" value="2" style="height: 20px;vertical-align:middle;"/></td>
				<td class="col_num" style="text-align:center;"><input type="radio" tabindex="4" id="<%=DefineReport.Radio.JISSEKIBUN.getObj()%>" name="<%=DefineReport.Radio.JISSEKIBUN.getObj()%>" value="3" style="height: 20px;vertical-align:middle;"/></td>
				<td><select class="easyui-combobox" tabindex="5" id="<%=DefineReport.Select.DAI_BUN.getObj()%>" style="width:200px;"></select></td>
			</tr>
			</table>
		</td>
	</tr>
	</table>
	<table>
	<tr>
		<td style="text-align:left;vertical-align:bottom;">
		<table id="dTable2" class="dataTable">
		<tr>
			<td class="labelCell" style="width:60px;text-align:center;">週データ</td>
			<td class="labelCell" style="width:40px;text-align:center;">年　週</td>
			<td class="labelCell" style="width:60px;text-align:center;">月データ</td>
			<td class="labelCell" style="width:40px;text-align:center;">年　月</td>
		</tr>
		<tr>
			<td class="col_num" style="height:20px;text-align:center;"><input type="radio" tabindex="6" id="<%=DefineReport.Radio.WWMMFLG.getObj()%>" name="<%=DefineReport.Radio.WWMMFLG.getObj()%>" value="1" style="vertical-align:middle;" checked="checked"/></td>
			<td class="col_num" style="height:20px;"><input class="easyui-numberbox" tabindex="7" id="<%=DefineReport.InpText.YYWW.getObj()%>" check='<%=DefineReport.InpText.YYWW.getMaxlenTag()%>' style="height:20px;width:60px;text-align:left;" value=""></td>
			<td class="col_num" style="height:20px;text-align:center;"><input type="radio" tabindex="8" id="<%=DefineReport.Radio.WWMMFLG.getObj()%>" name="<%=DefineReport.Radio.WWMMFLG.getObj()%>" value="2" style="vertical-align:middle;"/></td>
			<td class="col_num" style="height:20px;"><input class="easyui-numberbox" tabindex="9" id="<%=DefineReport.InpText.YYMM.getObj()%>" check='<%=DefineReport.InpText.YYMM.getMaxlenTag()%>' style="height:20px;width:60px;text-align:left;" value=""></td>
		</tr>
		</table>
		</td>
		<td class="col_btn">
			<a href="#" title="検索" id="<%=DefineReport.Button.SEARCH.getObj()%>" class="easyui-linkbutton" tabindex="10" iconCls="icon-search"><span class="btnTxt">検索</span></a>
		</td>
	</tr>
	</table>
</div>
</form>
<form id="gf" class="e_grid">
<div id="list" data-options="region:'center',border:false" style="display:none;">

	<!-- EasyUI方式 -->
	<div tabindex="11" id="gridholder" class="easyui-datagrid placeFace" ></div>

</div>
</form>
<div id="buttons" data-options="region:'south',border:false" style="display:none;">
	<div class="btn" style="float: left;">
		<table class="t-layout3">
		<tr><th style="min-width:135px;"></th><th style="min-width:10px;"></th>
			<th style="min-width:135px;"></th><th style="min-width:135px;"></th></tr>
		<tr>
		<td><a href="#" title="戻る" id="<%=DefineReport.Button.BACK.getObj()%>" class="easyui-linkbutton" tabindex="9001" iconCls="icon-undo" style="width:125px;"><span class="btnTxt">戻る</span></a></td>
		<td><a href="#" class="easyui-linkbutton" tabindex="9002" id="<%=DefineReport.Button.CANCEL.getObj()%>" title="<%=DefineReport.Button.CANCEL.getTxt()%>" iconCls="icon-cancel" style="width:110px;"><span><%=DefineReport.Button.CANCEL.getTxt()%></span></a></td>
		<td><a href="#" title="選択(確定)" id="<%=DefineReport.Button.SEL_KAKUTEI.getObj()%>" class="easyui-linkbutton" tabindex="9003" iconCls="icon-ok" style="width:125px;"><span>選択(確定)</span></a></td>
		<td><a href="#" title="選択(店別分配率)" id="<%=DefineReport.Button.SEL_TENBETUBRT.getObj()%>" class="easyui-linkbutton" tabindex="9004" iconCls="icon-ok" style="width:125px;"><span>選択(店別分配率)</span></a></td>
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
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.ST011.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.control.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.events.js?v=<%=prm %>"></script>
</html>