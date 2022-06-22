
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
<div data-options="region:'north',border:false" style="display:none;height:50px;padding:2px 5px 0;overflow: visible;" >
	<div style="width:930px;">
		<table class="t-layout">
		<tr><td style="min-width:240px;">週No.</td><td style="max-width:210px;"><label class="chk_lbl" for="<%=DefineReport.Checkbox.TSHUFLG.getObj()%>">特別週</label></td></tr>
		<tr>
		<td>
			<span>
				<select class="easyui-combobox" tabindex="1" col="F1" id="<%=DefineReport.Select.SHUNO.getObj()%>" style="width:230px;" check='<%=DefineReport.InpText.SHUNO.getMaxlenTag()%>' data-options="panelHeight:300,editable:true,required: true" ></select>
			</span>
			<span style="display:none;"><input class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.SHUNO.getObj()%>" check='<%=DefineReport.InpText.SHUNO.getMaxlenTag()%>' style="width:230px;"></span>
		</td>
		<td style="text-align: center;">
			<input type="checkbox" tabindex="2" col="F2" id="<%=DefineReport.Checkbox.TSHUFLG.getObj()%>" value="">
		</td>
		</tr>
		</table>
		<input type="hidden" name="<%=DefineReport.Text.SHORIDT.getObj()%>" id="<%=DefineReport.Text.SHORIDT.getObj()%>" />
	</div>
</div>
<div data-options="region:'center',border:false" style="display:none;padding:2px 5px 0;">
	<h4>レギュラー</h4>
	<div style="height:137px;">
		<div class="easyui-datagrid" tabindex="3" id="<%=DefineReport.Grid.MOYCD_R.getObj()%>"></div>
	</div>
	<h4>スポット</h4>
	<div style="height:137px;">
		<div class="easyui-datagrid" tabindex="4" id="<%=DefineReport.Grid.MOYCD_S.getObj()%>"></div>
	</div>
	<h4>特売</h4>
	<div style="height:162px;">
		<div class="easyui-datagrid" tabindex="5" id="<%=DefineReport.Grid.MOYCD_T.getObj()%>"></div>
	</div>
	<!-- Editor参照用 -->
	<div class="ref_editor" style="display: none;">
		<input type="checkbox" tabindex="-1" id="<%=DefineReport.Checkbox.DEL.getObj()%>"/>
		<input type="checkbox" tabindex="-1" id="<%=DefineReport.Checkbox.USE.getObj()%>"/>
		<input class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.MOYSKBN.getObj()%>"/>
		<input class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.MOYSSTDT.getObj()%>" check='<%=DefineReport.InpText.MOYSSTDT.getMaxlenTag()%>'/>
		<input class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.MOYSRBANINP.getObj()%>" check='<%=DefineReport.InpText.MOYSRBANINP.getMaxlenTag()%>'/>
		<input class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.HBSTDT.getObj()%>" data-options="prompt:'<%=DefineReport.Label.PROMPT_DT.getTxt()%>'" check='<%=DefineReport.InpText.HBSTDT.getMaxlenTag()%>'/>
		<input class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.HBEDDT.getObj()%>" data-options="prompt:'<%=DefineReport.Label.PROMPT_DT.getTxt()%>'" check='<%=DefineReport.InpText.HBEDDT.getMaxlenTag()%>'/>
		<input class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.NNSTDT.getObj()%>" data-options="prompt:'<%=DefineReport.Label.PROMPT_DT.getTxt()%>'" check='<%=DefineReport.InpText.NNSTDT.getMaxlenTag()%>'/>
		<input class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.NNEDDT.getObj()%>" data-options="prompt:'<%=DefineReport.Label.PROMPT_DT.getTxt()%>'" check='<%=DefineReport.InpText.NNEDDT.getMaxlenTag()%>'/>
		<input class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.PLUSDDT.getObj()%>" data-options="prompt:'<%=DefineReport.Label.PROMPT_DT.getTxt()%>'" check='<%=DefineReport.InpText.PLUSDDT.getMaxlenTag()%>'/>
		<input class="easyui-textbox" tabindex="-1" id="<%=DefineReport.InpText.MOYKN.getObj()%>" check='<%=DefineReport.InpText.MOYKN.getMaxlenTag()%>'/>
		<input class="easyui-textbox" tabindex="-1" id="<%=DefineReport.InpText.MOYAN.getObj()%>" check='<%=DefineReport.InpText.MOYAN.getMaxlenTag()%>'/>
		<input type="checkbox" tabindex="-1" id="<%=DefineReport.Checkbox.NENMATKBN.getObj()%>"/>
		<input class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.BMNCD.getObj()%>" check='<%=DefineReport.InpText.BMNCD.getMaxlenTag()%>'/>
	</div>
</div>
</form>
<div id="buttons" data-options="region:'south',border:false" style="display:none;">
	<div class="btn" style="float: left;">
		<table class="t-layout3">
		<tr>
			<th style="min-width:120px;"></th><th style="min-width:10px;"></th>
			<th style="min-width:120px;"></th><th style="min-width:120px;"></th>
		</tr>
		<tr>
			<td><a href="#" title="戻る" id="<%=DefineReport.Button.BACK.getObj()%>" class="easyui-linkbutton" tabindex="10" iconCls="icon-undo" style="width:110px;"><span class="btnTxt">戻る</span></a></td>
			<td></td>
			<td><a href="#" class="easyui-linkbutton" tabindex="11" id="<%=DefineReport.Button.CANCEL.getObj()%>" title="<%=DefineReport.Button.CANCEL.getTxt()%>" iconCls="icon-cancel" style="width:110px;"><%=DefineReport.Button.CANCEL.getTxt()%></a></td>
			<td><a href="#" class="easyui-linkbutton" tabindex="12" id="<%=DefineReport.Button.UPD.getObj()%>" title="<%=DefineReport.Button.UPD.getTxt()%>" iconCls="icon-save" style="width:110px;"><span class="btnTxt"><%=DefineReport.Button.UPD.getTxt()%></span></a></td>
		</tr>
		</table>
	</div>
	<div id="disp_record_info" style="float: right;">
		<span class="labelName" tabindex="-1" style="padding-top: 3px;">
			登録日 <span col="F6" id="<%=DefineReport.Text.ADDDT.getObj()%>"></span>　更新日 <span col="F7" id="<%=DefineReport.Text.UPDDT.getObj()%>"></span>　オペレータ <span col="F5" id="<%=DefineReport.Text.OPERATOR.getObj()%>"></span>
		</span>
		<input type="hidden" col="F8" name="<%=DefineReport.Hidden.UPDDT.getObj()%>" id="<%=DefineReport.Hidden.UPDDT.getObj()%>" />
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
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.TM002.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.control.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.events.js?v=<%=prm %>"></script>
</html>