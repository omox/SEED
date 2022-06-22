
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
<div data-options="region:'center',border:false" style="display:none;">
<table class="t-layout" style="margin:10px;">
 	<tr><th style="max-width:600px;"></th><th style="max-width:600px;"></th></tr>
		<tr>
			<td>
				<div class="inp_box"><input class="easyui-textbox" tabindex="1" col="F1" id="kikaku_dummy" data-options="label:'企画',labelWidth:150" style="width:450px;"></div>
			</td>
		</tr>
		<tr>
			<td>
				<div class="inp_box"><input class="easyui-textbox" tabindex="2" col="F3" id="kikan_dummy"  data-options="label:'納入期間',labelWidth:150" style="width:350px;"></div>
			</td>
		</tr>
		<tr>
			<td>
				<div class="inp_box"><input class="easyui-numberbox" tabindex="3" col="F6" id="<%=DefineReport.InpText.CATALGNO.getObj()%>" check='<%=DefineReport.InpText.CATALGNO.getMaxlenTag()%>' data-options="label:'カタログ番号',labelWidth:150" style="width:180px;"></div>
			</td>
		</tr>
		<tr>
			<td>
				<div class="inp_box"><input class="easyui-numberbox" tabindex="4" col="F7" id="<%=DefineReport.InpText.SHNCD.getObj()%>" check='<%=DefineReport.InpText.SHNCD.getMaxlenTag()%>' data-options="label:'商品コード',labelWidth:150,prompt:'____-____',required:true" style="width:230px;">
				<input class="easyui-textbox" tabindex="5" id="<%=DefineReport.InpText.SHNKN.getObj()%>" check='<%=DefineReport.InpText.SHNKN.getMaxlenTag()%>' for_inp="<%=DefineReport.InpText.SHNCD.getObj()%>_F1" style="width:280px;"></div>
			</td>
		</tr>
		<tr>
			<td>
				<div class="inp_box"><input class="easyui-numberbox" tabindex="6" col="F8" id="<%=DefineReport.InpText.HTDT.getObj()%>" check='<%=DefineReport.InpText.HTDT.getMaxlenTag()%>' data-options="label:'発注日',labelWidth:150,prompt:'<%=DefineReport.Label.PROMPT_DTW.getTxt()%>',required:true" style="width:250px;"></div>
			</td>
		</tr>
		<tr>
			<td>
				<div class="inp_box">
				<input class="easyui-numberbox" tabindex="7" col="F9" id="<%=DefineReport.InpText.UKESTDT.getObj()%>" check='<%=DefineReport.InpText.UKESTDT.getMaxlenTag()%>' data-options="prompt:'<%=DefineReport.Label.PROMPT_DTW.getTxt()%>',label:'受付期間',labelWidth:150,required:true" style="width:250px;">
				～
				<input class="easyui-numberbox" tabindex="8" col="F10" id="<%=DefineReport.InpText.UKEEDDT.getObj()%>" check='<%=DefineReport.InpText.UKEEDDT.getMaxlenTag()%>' data-options="prompt:'<%=DefineReport.Label.PROMPT_DTW.getTxt()%>',required:true"style="width:100px;">
				</div>
			</td>
		</tr>
		<tr>
			<td>
				<div class="inp_box">
				<input class="easyui-numberbox" tabindex="9" col="F11" id="<%=DefineReport.InpText.TENISTDT.getObj()%>" check='<%=DefineReport.InpText.TENISTDT.getMaxlenTag()%>' data-options="prompt:'<%=DefineReport.Label.PROMPT_DTW.getTxt()%>',label:'店舗入力期間',labelWidth:150,required:true" style="width:250px;">
				～
				<input class="easyui-numberbox" tabindex="10" col="F12" id="<%=DefineReport.InpText.TENIEDDT.getObj()%>" check='<%=DefineReport.InpText.TENIEDDT.getMaxlenTag()%>' data-options="prompt:'<%=DefineReport.Label.PROMPT_DTW.getTxt()%>',required:true"style="width:100px;">
				</div>
			</td>
		</tr>
		<tr>
			<td>
				<div class="inp_box"><input class="easyui-numberbox" tabindex="11" col="F13" id="<%=DefineReport.InpText.YOTEISU.getObj()%>" check='<%=DefineReport.InpText.YOTEISU.getMaxlenTag()%>' data-options="label:'予定数',labelWidth:150" style="width:230px;text-align: right;"></div>
			</td>
		</tr>
		<tr>
			<td>
				<div class="inp_box"><input class="easyui-numberbox" tabindex="12" col="F14" id="<%=DefineReport.InpText.GENDOSU.getObj()%>" check='<%=DefineReport.InpText.GENDOSU.getMaxlenTag()%>' data-options="label:'限度数',labelWidth:150" style="width:230px;text-align: right;"></div>
			</td>
		</tr>
</table>
	<div id = shngrid style="height:137px;width:300px;padding-left: 135px;">
		<div class="easyui-datagrid" tabindex="13" id="<%=DefineReport.Grid.NOHIN.getObj()%>"></div>
	</div>
	<div style="display: none;">
	<!-- 非表示でデータを保持 -->
		<input class="easyui-numberbox" tabindex="-1" col="F2" id="<%=DefineReport.InpText.KKKCD.getObj()%>" check='<%=DefineReport.InpText.KKKCD.getMaxlenTag()%>' >
		<input class="easyui-numberbox" tabindex="-1" col="F4" id="<%=DefineReport.InpText.NNSTDT.getObj()%>" check='<%=DefineReport.InpText.NNSTDT.getMaxlenTag()%>' >
		<input class="easyui-numberbox" tabindex="-1" col="F5" id="<%=DefineReport.InpText.NNEDDT.getObj()%>" check='<%=DefineReport.InpText.NNEDDT.getMaxlenTag()%>' >

		<input class="easyui-numberbox" tabindex="-1" col="F19" id="<%=DefineReport.InpText.NGFLG.getObj()%>" check='<%=DefineReport.InpText.NGFLG.getMaxlenTag()%>' >
	</div>
	<!-- Editor参照用 -->
	<div class="ref_editor" style="display: none;">
		<%-- <input class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.YOTEISU.getObj()%>"  check='<%=DefineReport.InpText.YOTEISU.getMaxlenTag()%>'>
		<input class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.GENDOSU.getObj()%>"  check='<%=DefineReport.InpText.GENDOSU.getMaxlenTag()%>'> --%>

		<input class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.NNDT.getObj()%>" data-options="prompt:'<%=DefineReport.Label.PROMPT_DT.getTxt()%>'" check='<%=DefineReport.InpText.NNDT.getMaxlenTag()%>'/>


	</div>
</div>
</form>

<div id="buttons" data-options="region:'south',border:false" style="display:none;height:30px;padding:0 20px;">
	<div class="btn" style="float: left;">
		<table class="t-layout3">
		<tr><th style="min-width:120px;"></th><th style="min-width:120px;"></th><th style="min-width:120px;"></th></tr>
		<tr>
		<td><a href="#" title="戻る" id="<%=DefineReport.Button.BACK.getObj()%>" class="easyui-linkbutton" tabindex="14" iconCls="icon-undo" style="width:125px;"><span class="btnTxt">戻る</span></a></td>
		<td><a href="#" class="easyui-linkbutton" tabindex="15" id="<%=DefineReport.Button.CANCEL.getObj()%>" title="<%=DefineReport.Button.CANCEL.getTxt()%>" iconCls="icon-cancel" style="width:110px;"><span><%=DefineReport.Button.CANCEL.getTxt()%></span></a></td>
		<td><a href="#" class="easyui-linkbutton" tabindex="16" id="<%=DefineReport.Button.UPD.getObj()%>" title="<%=DefineReport.Button.UPD.getTxt()%>" iconCls="icon-save" style="width:110px;"><span class="btnTxt"><%=DefineReport.Button.UPD.getTxt()%></span></a></td>
		</tr>
		</table>
	</div>
	<div id="disp_record_info" style="float: right;">
		<span class="labelName" tabindex="-1" style="padding-top: 3px;">
				登録日 <span col="F17" id="<%=DefineReport.Text.ADDDT.getObj()%>"></span>　更新日 <span col="F18" id="<%=DefineReport.Text.UPDDT.getObj()%>"></span>　オペレータ <span col="F16" id="<%=DefineReport.Text.OPERATOR.getObj()%>"></span>
		</span>
		<input type="hidden" name="<%=DefineReport.Text.SHORIDT.getObj()%>" id="<%=DefineReport.Text.SHORIDT.getObj()%>" />
		<input type="hidden" col="F15" name="<%=DefineReport.Hidden.UPDDT.getObj()%>" id="<%=DefineReport.Hidden.UPDDT.getObj()%>" />
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
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.YH002.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.control.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.events.js?v=<%=prm %>"></script>
</html>