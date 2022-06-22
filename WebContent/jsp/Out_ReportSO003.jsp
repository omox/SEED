
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
	<table class="dTable1">
	<tr>
	<td colspan="4">
		<table id="dTable2" class="dataTable">
		<tr>
			<td class="labelCell" style="width: 95px;">催しコード</td>
			<td class="labelCell" style="width:300px;">催し名称</td>
			<td class="labelCell" style="width:175px;">催し期間</td>
		</tr>
		<tr>
			<td class="col_txt" style="height: 20px;"><span col="F1" id="<%=DefineReport.InpText.MOYSCD.getObj()%>"></span></td>
			<td class="col_txt"><span col="F2"></span></td>
			<td class="col_txt"><span col="F3"></span></td>
		</tr>
		</table>
	</td>
	<td style = "width:50px"></td>
	<td>
	<table id="dTable3" class="dataTable">
		<tr>
			<td class="labelCell" style="width: 95px;">登録件数</td>
		</tr>
		<tr>
			<td class="col_txt" style="height: 20px;text-align: right;"><span col="F5" id="count_row"></span></td>
		</tr>
		</table>
	</td>
	</tr>
	<tr>
	<td>
		<table id="dTable4" class="dataTable">
		<tr>
			<td class="labelCell" style="width:50px;">部門</td>
			<td class="" style="height: 20px;min-width: 315px"><span col="F4" id="<%=DefineReport.InpText.BMNCD.getObj()%>"></span></td>
		</tr>
		</table>
	</td>
	</tr>
	</table>
	<!-- <table class="dTable4" class="dataTable">
		<tr>
		<td>部門</td>
		<td class="col_txt" style="height: 20px;"><span col="F4"></span></td>
		<td><input class="easyui-textbox" tabindex="-1" col="F4" id="bmncd" style="width:200px;" data-options="panelHeight:200"></td>
	</tr>
	</table> -->
</div>
</form>
<form id="gf" class="e_grid">
<div id="list" data-options="region:'center',border:false" style="display:none;">

	<!-- EasyUI方式 -->
	<div tabindex="1" id="gridholder" class="easyui-datagrid placeFace" ></div>
</div>
<!-- Editor参照用 -->
<div class="ref_editor" style="display: none;">
	<input type="checkbox" tabindex="-1" id="<%=DefineReport.Checkbox.DEL.getObj()%>"/>
	<input class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.SHNCD.getObj()%>"  check='<%=DefineReport.InpText.SHNCD.getMaxlenTag()%>' data-options="prompt:'____-____'">
	<input class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.A_BAIKAAM.getObj()%>" check='<%=DefineReport.InpText.A_BAIKAAM.getMaxlenTag()%>'/>


	<input class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.A_RANKNO.getObj()%>" check='<%=DefineReport.InpText.A_RANKNO.getMaxlenTag()%>' for_inp="<%=DefineReport.InpText.SHNCD.getObj()%>_F10"/>
	<input class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.B_BAIKAAM.getObj()%>" check='<%=DefineReport.InpText.B_BAIKAAM.getMaxlenTag()%>' for_inp="<%=DefineReport.InpText.SHNCD.getObj()%>_F11"/>
	<input class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.B_RANKNO.getObj()%>" check='<%=DefineReport.InpText.B_RANKNO.getMaxlenTag()%>' for_inp="<%=DefineReport.InpText.SHNCD.getObj()%>_F12"/>
	<input class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.C_BAIKAAM.getObj()%>" check='<%=DefineReport.InpText.C_BAIKAAM.getMaxlenTag()%>' for_inp="<%=DefineReport.InpText.SHNCD.getObj()%>_F13"/>
	<input class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.C_RANKNO.getObj()%>" check='<%=DefineReport.InpText.C_RANKNO.getMaxlenTag()%>' for_inp="<%=DefineReport.InpText.SHNCD.getObj()%>_F14"/>
	<input class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.POPCD.getObj()%>" check='<%=DefineReport.InpText.POPCD.getMaxlenTag()%>' for_inp="<%=DefineReport.InpText.SHNCD.getObj()%>_F15"/>
	<input class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.POPSU.getObj()%>" check='<%=DefineReport.InpText.POPSU.getMaxlenTag()%>' for_inp="<%=DefineReport.InpText.SHNCD.getObj()%>_F16"/>
	<input class="easyui-textbox" tabindex="-1" id="<%=DefineReport.InpText.POPSZ.getObj()%>" check='<%=DefineReport.InpText.POPSZ.getMaxlenTag()%>' for_inp="<%=DefineReport.InpText.SHNCD.getObj()%>_F17"/>
	<input class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.RG_BAIKAAM.getObj()%>" check='<%=DefineReport.InpText.RG_BAIKAAM.getMaxlenTag()%>' for_inp="<%=DefineReport.InpText.SHNCD.getObj()%>_F18"/>


	<input class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.RG_IRISU.getObj()%>" check='<%=DefineReport.InpText.RG_IRISU.getMaxlenTag()%>' for_inp="<%=DefineReport.InpText.SHNCD.getObj()%>_F3"/>
	<input class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.RG_GENKAAM.getObj()%>" check='<%=DefineReport.InpText.RG_GENKAAM.getMaxlenTag()%>' for_inp="<%=DefineReport.InpText.SHNCD.getObj()%>_F5"/>

	<%-- for_inp="<%=DefineReport.InpText.TENCD.getObj()%>_F3" --%>

	<input class="easyui-textbox" tabindex="-1" id="<%=DefineReport.InpText.MAKERKN.getObj()%>"  check='"datatyp":"zen_text","maxlen":28' for_inp="<%=DefineReport.InpText.SHNCD.getObj()%>_F6"/>
	<input class="easyui-textbox" tabindex="-1" id="<%=DefineReport.InpText.SHNKN.getObj()%>"  check='<%=DefineReport.InpText.SHNKN.getMaxlenTag()%>' for_inp="<%=DefineReport.InpText.SHNCD.getObj()%>_F1"/>
	<input class="easyui-textbox" tabindex="-1" id="<%=DefineReport.InpText.KIKKN.getObj()%>"  check='<%=DefineReport.InpText.KIKKN.getMaxlenTag()%>' for_inp="<%=DefineReport.InpText.SHNCD.getObj()%>_F2"/>

	<input class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.GENKAAM.getObj()%>" check='<%=DefineReport.InpText.GENKAAM.getMaxlenTag()%>' for_inp="<%=DefineReport.InpText.SHNCD.getObj()%>_F9"/>
	<input class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.IRISU.getObj()%>" check='<%=DefineReport.InpText.IRISU.getMaxlenTag()%>' for_inp="<%=DefineReport.InpText.SHNCD.getObj()%>_F8"/>
	<input class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.MINSU.getObj()%>" check='<%=DefineReport.InpText.MINSU.getMaxlenTag()%>'for_inp="<%=DefineReport.InpText.SHNCD.getObj()%>_F4"/>

	<input class="easyui-textbox" tabindex="-1" id="<%=DefineReport.Text.HS_NEIRE.getObj()%>_a" />
	<input class="easyui-textbox" tabindex="-1" id="<%=DefineReport.Text.HS_NEIRE.getObj()%>_b" />
	<input class="easyui-textbox" tabindex="-1" id="<%=DefineReport.Text.HS_NEIRE.getObj()%>_c" />

	<input class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.Text.TOK_HONBAIKA.getObj()%>_a" />
	<input class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.Text.TOK_HONBAIKA.getObj()%>_b" />
	<input class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.Text.TOK_HONBAIKA.getObj()%>_c" />

	<input class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.BAIKAAM.getObj()%>" for_inp="<%=DefineReport.InpText.SHNCD.getObj()%>_F7"/>

</div>
</form>
<div id="buttons" data-options="region:'south',border:false" style="display:none;">
		<div class="btn" style="float: left;">
		<table class="t-layout3">
		<tr><th style="min-width:135px;"></th><th style="min-width:10px;"></th>
		<th style="min-width:135px;"></th>
		<th style="min-width:135px;"></th>
		<th style="min-width:135px;"></th>

		<tr>
		<td><a href="#" title="戻る" id="<%=DefineReport.Button.BACK.getObj()%>" class="easyui-linkbutton" tabindex="2" iconCls="icon-undo" style="width:125px;"><span class="btnTxt">戻る</span></a></td>
		<td></td>
		<td><a href="#" class="easyui-linkbutton" tabindex="3" id="<%=DefineReport.Button.CANCEL.getObj()%>" title="<%=DefineReport.Button.CANCEL.getTxt()%>" iconCls="icon-cancel" style="width:125px;"><%=DefineReport.Button.CANCEL.getTxt()%></a></td>
		<td><a href="#" class="easyui-linkbutton" tabindex="4" id="selectLastrow" title="最下行へ" style="width:125px;"><span>最下行へ</span></a></td>
		<td><a href="#" class="easyui-linkbutton" tabindex="5" id="<%=DefineReport.Button.DEL.getObj()%>" title="<%=DefineReport.Button.DEL.getTxt()%>" iconCls="icon-remove" style="width:125px;"><span class="btnTxt">全削除</span></a></td>
		<td><a href="#" title="CSV出力" id="<%=DefineReport.Button.CSV.getObj()%>" class="easyui-linkbutton" tabindex="9" iconCls="icon-excel" style="width:125px;"><span>CSV出力</span></a></td>
		</tr>
		</table>
	</div>
	<div>
		<input type="hidden" col="F6" name="<%=DefineReport.Hidden.UPDDT.getObj()%>" id="<%=DefineReport.Hidden.UPDDT.getObj()%>" />
		<input type="hidden" col="F7" id="txt_plusflg" />
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
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.SO003.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.control.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.events.js?v=<%=prm %>"></script>
</html>