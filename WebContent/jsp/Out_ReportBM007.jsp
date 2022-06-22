
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
<form id="uf" method="post" style="display: inline" enctype="multipart/form-data" encoding="multipart/form-data" target="if">
<div id="search" data-options="region:'north',border:false" style="display:none;height:250px;padding:20px 8px 0;text-align:center;" >
	<table class="t-layout2"  style="text-align:left;">
	<tr><th style="width:100px;"><th style="width:100px;"><th style="width:400px;"><th style="width:100px;"><th style="width:100px;"></tr>
	<tr>
	<td></td>
	<td class="lbl_box" colspan="3">
		<input class="easyui-textbox" tabindex="-1" id="<%=DefineReport.Text.STATUS.getObj()%>" readonly="readonly" data-options="label:'ステータス',labelWidth:70,readonly:true,editable:false" style="width:520px;">
	</td>
	</tr>
	<tr>
	<td colspan="2"></td>
	<td class="lbl_box"><div><input class="easyui-textbox" tabindex="-1" id="<%=DefineReport.Text.UPD_NUMBER.getObj()%>" readonly="readonly" data-options="label:'取込件数',labelWidth:70,readonly:true,editable:false" style="width:150px;text-align: right">　件</div></td>
	</tr>
	<tr>
	<td colspan="2"></td>
	<td class="lbl_box"><div></div></td>
	</tr>
	<tr>
	<td colspan="2"></td>
	<td><input class="easyui-textbox" tabindex="1" id="<%=DefineReport.InpText.COMMENTKN.getObj()%>" check='<%=DefineReport.InpText.COMMENTKN.getMaxlenTag()%>' data-options="label:'コメント',labelWidth:70,required:true" style="width:340px;"></td>
	</tr>
	<tr>
	<td></td>
	<td class="lbl_box" colspan="3">
		<input class="easyui-textbox" tabindex="-1" id="<%=DefineReport.Text.FILE.getObj()%>_" data-options="label:'ファイル',labelWidth:70,required:true" class="File" style="width: 520px;" readonly="readonly"/>
	</td>
	</tr>
	<tr>
	<td colspan="2"></td>
	<td  style="text-align:center;">
		<span>
		<input type="file" accept=".csv" tabindex="2" name="<%=DefineReport.Text.FILE.getObj()%>" id="<%=DefineReport.Text.FILE.getObj()%>" style="width:102px;height:26px;"/>
		<label  tabindex="-1" for="<%=DefineReport.Text.FILE.getObj()%>" style="display: inline-block;" title="<%=DefineReport.Button.FILE.getTxt()%>" class="easyui-linkbutton" iconCls="icon-add"><span>ファイル選択</span></label>
		</span>
		<a href="#" tabindex="3" id="<%=DefineReport.Button.UPLOAD.getObj()%>" title="<%=DefineReport.Button.UPLOAD.getTxt()%>" class="easyui-linkbutton" iconCls="icon-save" style="width:100px;"><span>取込開始</span></a>
	</td>
	</tr>
	</table>
</div>
</form>
<div id="buttons" data-options="region:'south',border:false" style="display:none;">
	<div class="btn" style="float: left;">
		<table class="t-layout3">
		<tr><th style="min-width:140px;"></th><th style="min-width: 10px;"></th><th style="min-width:140px;"></th></tr>
		<tr>
		<td><a href="#" title="戻る" id="<%=DefineReport.Button.BACK.getObj()%>" class="easyui-linkbutton" tabindex="4" iconCls="icon-undo" style="width:130px;"><span class="btnTxt">戻る</span></a></td>
		<td></td>
		<td>
		<!--tabindexは有効時にJSで設定-->
		<a href="#" tabindex="-1" id="<%=DefineReport.Button.ERR_LIST.getObj()%>" title="<%=DefineReport.Button.ERR_LIST.getTxt()%>" class="easyui-linkbutton" data-options="iconCls:'icon-ok',disabled:true"><span>エラーリスト出力</span></a>
		</td>
		</tr>
		</table>
	</div>
	<input type="hidden" tabindex="-1" id="<%=DefineReport.Text.SEQ.getObj()%>" value=""/>
	<input type="hidden" tabindex="-1" id="<%=DefineReport.Text.ERR_NUMBER.getObj()%>" value=""/>
</div>
</div>
<iframe name="if" id="if" frameborder="0" border="0" width="0" height="0" scrolling="yes"></iframe>
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
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.BM007.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.win001.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.win002.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.control.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.events.js?v=<%=prm %>"></script>
</html>