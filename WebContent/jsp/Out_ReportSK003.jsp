
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
<table class="t-layout" style="margin:10px;">
 	<tr><th style="max-width:600px;"></th><th style="max-width:600px;"></th></tr>
 		<%-- <tr>
			<td></td>
			<td></td>
			<td></td>
			<td><div class="inp_box"><input class="easyui-numberbox" tabindex="2" col="F2" id="<%=DefineReport.InpText.KSPAGE.getObj()%>" check='<%=DefineReport.InpText.KSPAGE.getMaxlenTag()%>' data-options="label:'構成ページ',labelWidth:60" style="width:170px;"></div></td>
		</tr> --%>
		<tr>
			<td>
				<div id = "inputno" class="inp_box"><input class="easyui-numberbox" tabindex="1" col="F1" id="<%=DefineReport.InpText.INPUTNO.getObj()%>" check='<%=DefineReport.InpText.INPUTNO.getMaxlenTag()%>' data-options="label:'入力No',labelWidth:50" style="width:100px;"></div>
			</td>
			<td>
				<div class="inp_box"><input class="easyui-numberbox" tabindex="2" col="F2" id="<%=DefineReport.InpText.TENCD.getObj()%>" check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' data-options="label:'店舗',labelWidth:60,required:true" style="width:100px;"></div>
			</td>
			<td></td>
			<td>
				<div id = "kspage"><input class="easyui-numberbox" tabindex="3" col="F12" id="<%=DefineReport.InpText.KSPAGE.getObj()%>" check='<%=DefineReport.InpText.KSPAGE.getMaxlenTag()%>' data-options="label:'構成ページ',labelWidth:70,prompt:'____-____',required:true" style="width:160px;">
				<a href="#" title="検索" id="btn_search_kspage" class="easyui-linkbutton" tabindex="4" iconCls="icon-search"><span class="btnTxt">検索</span></a></div>
			</td>
		</tr>
		<tr>
			<td></td>
			<td colspan=2>
				<div class="inp_box"><input class="easyui-textbox" tabindex="5" col="F3" id="<%=DefineReport.InpText.TENKN.getObj()%>" check='<%=DefineReport.InpText.TENKN.getMaxlenTag()%>'  data-options="label:'店舗名',labelWidth:60" style="width:350px;" for_inp="<%=DefineReport.InpText.TENCD.getObj()%>_F3"></div>
			</td>
			<td>
				<div class="inp_box"><input class="easyui-numberbox" tabindex="6" col="F4" id="<%=DefineReport.InpText.HTDT.getObj()%>" check='<%=DefineReport.InpText.HTDT.getMaxlenTag()%>' data-options="label:'発注日',labelWidth:70,prompt:'<%=DefineReport.Label.PROMPT_DTW.getTxt()%>',required:true" style="width:160px;"></div>
			</td>
		</tr>
		<tr>
			<td></td>
			<td>
				<div class="inp_box"><input class="easyui-combobox" tabindex="8" col="F6" id="sel_shnkbn2" check='<%=DefineReport.InpText.SHNKBN.getMaxlenTag()%>' data-options="label:'商品区分',labelWidth:60,required:true" style="width:200px;"></div>
			</td>
			<td>
				<div class="inp_box"><input class="easyui-numberbox" tabindex="9" col="F7" id="<%=DefineReport.InpText.BDENKBN.getObj()%>" check='<%=DefineReport.InpText.BDENKBN.getMaxlenTag()%>' data-options="label:'別伝区分',labelWidth:60,min:0,max:8,required:true" style="width:90px;"></div>
			</td>
			<td>
				<div class="inp_box"><input class="easyui-numberbox" tabindex="7" col="F5" id="<%=DefineReport.InpText.NNDT.getObj()%>" check='<%=DefineReport.InpText.NNDT.getMaxlenTag()%>'  data-options="label:'納入日',labelWidth:70,prompt:'<%=DefineReport.Label.PROMPT_DTW.getTxt()%>',required:true" style="width:160px;"></div>
			</td>
		</tr>
</table>

<%--
<table>
	<tr>
		<td style = "vertical-align: bottom;">
			<div class="inp_box"><input class="easyui-numberbox" tabindex="11" id="<%=DefineReport.InpText.SURYO.getObj()%>" check='<%=DefineReport.InpText.SURYO.getMaxlenTag()%>' style="width:50px;">
			<a href="#" title="数量コピー" id="btn_suryo" class="easyui-linkbutton" tabindex="12" iconCls="icon-search"><span class="btnTxt">数量コピー</span></a></div>
		</td>
	</tr>
</table>
--%>

</div>
	<div data-options="region:'center',border:false" style = "display:none;">
		<div id = "suryo" class="inp_box" style = "display:table-cell;width:180px;vertical-align: bottom;" data-options="fit:true,">
			<table>
				<tr>
					<td>
						<input class="easyui-numberbox" tabindex="11" id="<%=DefineReport.InpText.SURYO.getObj()%>" check='<%=DefineReport.InpText.SURYO.getMaxlenTag()%>' data-options="numonly:true" style="width:50px;text-align:right">
					</td>
					<td>
						<a href="#" title="数量コピー" id="btn_suryo" class="easyui-linkbutton" tabindex="12" iconCls="icon-search"><span class="btnTxt" style = "width:80px;">数量コピー</span></a>
					</td>
				</tr>
			</table>
			<%-- <input class="easyui-numberbox" tabindex="11" id="<%=DefineReport.InpText.SURYO.getObj()%>" check='<%=DefineReport.InpText.SURYO.getMaxlenTag()%>' style="width:50px;">
			<a href="#" title="数量コピー" id="btn_suryo" class="easyui-linkbutton" tabindex="12" iconCls="icon-search"><span class="btnTxt" style = "width:80px;">数量コピー</span></a> --%>
	</div>
</div>
</form>
<form id="gf" class="e_grid">

<div id="list" data-options="region:'west',border:false" style="display:none;">

<%-- <table>
	<tr>
		<td style ="width:680px;hight:100px">
	<div class="easyui-datagrid" tabindex="10" id="<%=DefineReport.Grid.TENPO＿SK.getObj()%>" data-options="singleSelect:true,rownumbers:true" style="width:650px;"></div>

		</td>
	</tr>
</table> --%>

	<div class="easyui-datagrid" tabindex="10" id="<%=DefineReport.Grid.TENPO＿SK.getObj()%>" data-options="singleSelect:true,rownumbers:true" style="width:590px;"></div>

	<div style="display: none;">
	<!-- 非表示でデータを保持 -->
		<input type="hidden" name="<%=DefineReport.Text.SHORIDT.getObj()%>" id="<%=DefineReport.Text.SHORIDT.getObj()%>" />
	</div>
	<!-- Editor参照用 -->
		<div class="ref_editor" style="display: none;">
		<input type="checkbox" tabindex="-1" id="<%=DefineReport.Checkbox.SEL.getObj()%>"/>
		<input class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.SHNCD.getObj()%>"  check='<%=DefineReport.InpText.SHNCD.getMaxlenTag()%>' data-options="prompt:'____-____'">
		<input class="easyui-textbox" 	tabindex="-1" id="<%=DefineReport.InpText.SHNKN.getObj()%>"  check='<%=DefineReport.InpText.SHNKN.getMaxlenTag()%>' for_inp="<%=DefineReport.InpText.SHNCD.getObj()%>_F1">
		<input class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.SURYO.getObj()%>"  check='<%=DefineReport.InpText.SURYO.getMaxlenTag()%>'>
	</div>

</div>
</form>
<div id="buttons" data-options="region:'south',border:false" style="display:none;height:30px;padding:0 20px;">
	<div class="btn" style="float: left;">
		<table class="t-layout3">
		<tr><th style="min-width:135px;"></th><th style="min-width:10px;"></th>
		<!-- <th style="min-width:135px;"></th>
		<th style="min-width:135px;"></th>
		<th style="min-width:135px;"></th> -->
		<tr>
		<td><a href="#" title="戻る" id="<%=DefineReport.Button.BACK.getObj()%>" class="easyui-linkbutton" tabindex="13" iconCls="icon-undo" style="width:125px;"><span class="btnTxt">戻る</span></a></td>
		<td></td>
		<td><a href="#" class="easyui-linkbutton" tabindex="14" id="<%=DefineReport.Button.CANCEL.getObj()%>" title="<%=DefineReport.Button.CANCEL.getTxt()%>" iconCls="icon-cancel" style="width:125px;"><span><%=DefineReport.Button.CANCEL.getTxt()%></span></a></td>
		<td><a href="#" class="easyui-linkbutton" tabindex="15" id="<%=DefineReport.Button.UPD.getObj()%>" title="<%=DefineReport.Button.UPD.getTxt()%>" iconCls="icon-save" style="width:125px;"><span class="btnTxt"><%=DefineReport.Button.UPD.getTxt()%></span></a></td>
		<td><a href="#" class="easyui-linkbutton" tabindex="16" id="<%=DefineReport.Button.DEL.getObj()%>" title="全削除" iconCls="icon-remove" style="width:125px;"><span class="btnTxt">全削除</span></a></td>
		</tr>
		</table>
	</div>
	<div id="disp_record_info" style="float: right;">
		<span class="labelName" tabindex="-1" style="padding-top: 3px;">
				登録日 <span col="F8" id="<%=DefineReport.Text.ADDDT.getObj()%>"></span>　更新日 <span col="F9" id="<%=DefineReport.Text.UPDDT.getObj()%>"></span>　オペレータ <span col="F10" id="<%=DefineReport.Text.OPERATOR.getObj()%>"></span>
		</span>
		<input type="hidden" col="F11" name="<%=DefineReport.Hidden.UPDDT.getObj()%>" id="<%=DefineReport.Hidden.UPDDT.getObj()%>" />
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

	<!-- 処理日付 -->
	<input type="hidden" name="<%=DefineReport.Text.SHORIDT.getObj()%>" id="<%=DefineReport.Text.SHORIDT.getObj()%>" />

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
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.SK003.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.control.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.events.js?v=<%=prm %>"></script>
</html>