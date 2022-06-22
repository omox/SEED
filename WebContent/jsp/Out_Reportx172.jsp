
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
<table class="t-layout1" style="margin:10px;">
 	<tr><th style="max-width:600px;"></th><th style="max-width:600px;"></th></tr>
		<tr>
			<td>
				配送パターン
				<input class="easyui-numberbox" tabindex="1" col="F1" id="<%=DefineReport.InpText.HSPTN.getObj()%>" check='<%=DefineReport.InpText.HSPTN.getMaxlenTag()%>' data-options="required:true" style="width:50px;text-align: left;">
			</td>
			<td>
				配送パターン名称
				<input class="easyui-textbox" tabindex="2" col="F2" id="<%=DefineReport.InpText.HSPTNKN.getObj()%>" check='<%=DefineReport.InpText.HSPTNKN.getMaxlenTag()%>' style="width:300px;">
			</td>
		</tr>
</table>
<fieldset style="width:400px">
	<legend>デフォルト設定</legend>
		<table>
			<tr>
				<td style= "width:2px"></td>
				<td>センターコード</td>
				<td>
					<input class="easyui-numberbox" tabindex="3" col="F3" id="<%=DefineReport.InpText.CENTERCD.getObj()%>" check='<%=DefineReport.InpText.CENTERCD.getMaxlenTag()%>' style="width:80px;">
				</td>
				<td>
				<input class="easyui-textbox" tabindex="4" id="<%=DefineReport.InpText.TENKN.getObj()%>_center" check='<%=DefineReport.InpText.TENKN.getMaxlenTag()%>' for_inp="<%=DefineReport.InpText.CENTERCD.getObj()%>_F3" style="width:200px;">
				</td>
			</tr>
		</table>
		<fieldset>
		<legend>横持先情報</legend>
		<table>
			<tr>
				<td>センターコード</td>
				<td>
					<input class="easyui-numberbox" tabindex="5" col="F4" id="<%=DefineReport.InpText.YCENTERCD.getObj()%>" check='<%=DefineReport.InpText.YCENTERCD.getMaxlenTag()%>' style="width:80px;">
				</td>
				<td>
				<input class="easyui-textbox" tabindex="6" id="<%=DefineReport.InpText.TENKN.getObj()%>_ycenter" check='<%=DefineReport.InpText.TENKN.getMaxlenTag()%>' for_inp="<%=DefineReport.InpText.YCENTERCD.getObj()%>_F3" style="width:200px;">
				</td>
			</tr>
		</table>
		</fieldset>
</fieldset>
<fieldset style="width:960px;">
	<legend>エリア別設定</legend>
			<table>
				<tr>
					<td>
						<div class="inp_box">
						<label class="rad_lbl"><input type="radio" tabindex="7" name="<%=DefineReport.Radio.AREAKBN.getObj()%>" value="0" style="vertical-align:middle;" checked="checked"/>エリア</label>
						<label class="rad_lbl"><input type="radio" tabindex="8" name="<%=DefineReport.Radio.AREAKBN.getObj()%>" value="1" style="vertical-align:middle;"/>店グループ</label>
						</div>
					</td>
					<td>配送グループ</td>
					<td>
						<input class="easyui-numberbox" tabindex="9" col="F6" id="<%=DefineReport.InpText.HSGPCD.getObj()%>" check='<%=DefineReport.InpText.HSGPCD.getMaxlenTag()%>' for_btn="<%=DefineReport.Button.HSGP.getObj()%>_F1" style="width:80px;">
					</td>
					<td>
						<input class="easyui-textbox" tabindex="10" col="F7" id="<%=DefineReport.InpText.HSGPKN.getObj()%>_YCENTER" check='<%=DefineReport.InpText.HSGPKN.getMaxlenTag()%>' for_btn="<%=DefineReport.Button.HSGP.getObj()%>_F2" style="width:320px;">
					</td>
					<td>
						<a href="#" class="easyui-linkbutton" tabindex="11" id="<%=DefineReport.Button.HSGP.getObj()%>" title="<%=DefineReport.Button.HSGP.getTxt()%>" iconCls="icon-search" style="width:102px;float:right;"><span>配送グループ</span></a>
					</td>
					<td>
						<!-- <a href="#" title="検索" id="btn_search_grid" class="easyui-linkbutton" tabindex="12" iconCls="icon-search"><span class="btnTxt">検索</span></a> -->
						<a href="#" title="検索" id="btn_search_grid" class="easyui-linkbutton" tabindex="12" iconCls="icon-search"><span class="btnTxt">検索</span></a>
					</td>

				</tr>
			</table>
	<div>
	<%-- <table id="<%=DefineReport.Grid.HSTNGP.getObj()%>" class="easyui-datagrid" tabindex="12" data-options="singleSelect:true,rownumbers:true,checkOnSelect:false,selectOnCheck:false,width:800,">

		<!-- <thead>
			<tr>
			<th data-options="field:'F14'				,editor:{type:'checkbox'}">削除</th>
			<th data-options="field:'TENGPCD'			,width:100,halign:'center',align:'right',editor:{type:'numberbox'}">店グループ</th>
			<th data-options="field:'TENGPKN'			,width:130,halign:'center',editor:{type:'textbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:true}}">店グループ名称</th>
			<th data-options="field:'CENTERCD'			,width:100,halign:'center',align:'right',editor:{type:'numberbox'}">センターコード</th>
			<th data-options="field:'TENKN_CENTER_G'	,width:130,halign:'center',editor:{type:'textbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:true}}">センターコード名称</th>
			<th data-options="field:'YCENTERCD'			,width:100,halign:'center',align:'right',editor:{type:'numberbox'}">横持センターコード</th>
			<th data-options="field:'TENKN_YCENTER_G'	,width:130,halign:'center',editor:{type:'textbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:true}}">横持センターコード名称</th>
			</tr>
		</thead> -->
	</table> --%>
	<div class="easyui-datagrid" tabindex="13" id="<%=DefineReport.Grid.EHSPTN.getObj()%>_hp012" style = "height:370px"></div>
	<!-- Editor参照用 -->
	<div class="ref_editor" style="display: none;">
	<input type="checkbox" tabindex="-1" id="chk_sel" />

	<input  class="easyui-textbox" tabindex="-1" id="<%=DefineReport.InpText.TENGPKN.getObj()%>" for_inp="txt_tengpcd_F3" check='<%=DefineReport.InpText.TENGPKN.getMaxlenTag()%>' />

	<input  class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.TENGPCD.getObj()%>" check='"datatyp":"lpadzero_text","maxlen":2'>
	<input  class="easyui-textbox" tabindex="-1" id="<%=DefineReport.InpText.TENGPKN.getObj()%>" check='<%=DefineReport.InpText.TENGPKN.getMaxlenTag()%>' for_inp="<%=DefineReport.InpText.TENGPCD.getObj()%>_F3">
	<input  class="easyui-textbox" tabindex="-1" id="<%=DefineReport.InpText.TENKN.getObj()%>_center_g" check='<%=DefineReport.InpText.TENKN.getMaxlenTag()%>' for_inp="<%=DefineReport.InpText.CENTERCD.getObj()%>_F3" />
	<input  class="easyui-textbox" tabindex="-1" id="<%=DefineReport.InpText.TENKN.getObj()%>_ycenter_g" check='<%=DefineReport.InpText.TENKN.getMaxlenTag()%>' for_inp="<%=DefineReport.InpText.YCENTERCD.getObj()%>_F3"/>
	</div>
	</div>
</fieldset>
</div>
</form>
<div id="buttons" data-options="region:'south',border:false" style="display:none;height:30px;padding:0 30px;text-align: center;">
	<div class="btn" style="float: left;">
		<table class="t-layout3">
		<tr><th style="min-width:135px;"></th><th style="min-width:10px;"></th>
		<th style="min-width:135px;"></th>
		<th style="min-width:135px;"></th>
		<th style="min-width:135px;"></th>
		</tr>
		<tr>
		<td><a href="#" title="戻る" id="<%=DefineReport.Button.BACK.getObj()%>" class="easyui-linkbutton" tabindex="14" iconCls="icon-undo" style="width:125px;"><span class="btnTxt">戻る</span></a></td>
		<td><a href="#" class="easyui-linkbutton" tabindex="15" id="<%=DefineReport.Button.CANCEL.getObj()%>" title="<%=DefineReport.Button.CANCEL.getTxt()%>" iconCls="icon-cancel" style="width:125px;"><span><%=DefineReport.Button.CANCEL.getTxt()%></span></a></td>
		<td><a href="#" class="easyui-linkbutton" tabindex="16" id="<%=DefineReport.Button.UPD.getObj()%>" title="<%=DefineReport.Button.UPD.getTxt()%>" iconCls="icon-save" style="width:125px;"><span class="btnTxt"><%=DefineReport.Button.UPD.getTxt()%></span></a></td>
		<td><a href="#" class="easyui-linkbutton" tabindex="17" id="<%=DefineReport.Button.DEL.getObj()%>" title="<%=DefineReport.Button.DEL.getTxt()%>" iconCls="icon-remove" style="width:125px;"><span class="btnTxt"><%=DefineReport.Button.DEL.getTxt()%></span></a></td>
		</tr>
		</table>
	</div>
	<div id="disp_record_info" style="float: right;">
		<span class="labelName" tabindex="-1" style="padding-top: 3px;">
				登録日 <span col="F8" id="<%=DefineReport.Text.ADDDT.getObj()%>"></span>　更新日 <span col="F9" id="<%=DefineReport.Text.UPDDT.getObj()%>"></span>　オペレータ <span col="F10" id="<%=DefineReport.Text.OPERATOR.getObj()%>"></span>
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

	<input type="hidden" name="sendBtnid" id="sendBtnid" value='' />
</div>

<div style="display:none;">
	<jsp:include page="Out_Reportwin009.jsp" flush="true" />
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
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.x172.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.win009.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.control.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.events.js?v=<%=prm %>"></script>
</html>