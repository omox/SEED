
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
<div data-options="region:'west',border:false" style="display:none;">
<table class="t-layout1" style="margin:10px;">
	<tr>
		<td style="vertical-align: top;">
		<table class="t-layout">
			<tr>
				<td >
					<div class="inp_box"><input class="easyui-combobox" tabindex="1" col="F1" id="<%=DefineReport.MeisyoSelect.KBN140.getObj()%>" data-options="label:'グループ区分',labelWidth:100,panelHeight:80" style="width:220px;"></div>
				</td>
			</tr>
			<tr>
				<td>
					<div class="inp_box"><input class="easyui-combobox" tabindex="2" col="F2" id="<%=DefineReport.Select.BUMON.getObj()%>" data-options="label:'部門',labelWidth:100,panelHeight:200" style="width:250px;"></div>
				</td>
			</tr>
			<tr>
				<td>
					<div id = tengp1 class="inp_box"><input class="easyui-numberbox" tabindex="3" col="F3" id="<%=DefineReport.InpText.TENGPCD.getObj()%>" check='<%=DefineReport.InpText.TENGPCD.getMaxlenTag()%>' data-options="label:'店グループ',labelWidth:100" style="width:150px;"></div>

					<div id = tengp2 class="inp_box"><input class="easyui-numberbox" tabindex="3" col="F3" id="<%=DefineReport.InpText.TENGPCD.getObj()%>_2" check='"datatyp":"lpadzero_text","maxlen":2' data-options="label:'店グループ',labelWidth:100" style="width:150px;"></div>
				</td>
			</tr>
			<tr>
				<td>
					<div class="inp_box"><input class="easyui-textbox" tabindex="4" col="F5" id="<%=DefineReport.InpText.TENGPKN.getObj()%>" check='<%=DefineReport.InpText.TENGPKN.getMaxlenTag()%>' data-options="label:'漢字名称',labelWidth:100,required:true" style="width:350px;"></div>
				</td>
			</tr>
			<tr>
				<td>
					<div class="inp_box"><input class="easyui-textbox" tabindex="5" col="F4" id="<%=DefineReport.InpText.TENGPAN.getObj()%>" check='<%=DefineReport.InpText.TENGPAN.getMaxlenTag()%>' data-options="label:'カナ名称',labelWidth:100" style="width:320px;"></div>
				</td>
			</tr>
			<tr>
				<td>
					<label class="chk_lbl"><input type="checkbox" tabindex="6" col="F6" id="chk_torihiki" />
					プライスカード出力有無
					</label>
				</td>
			</tr>
			<tr>
				<td>
					<label class="rad_lbl"><input type="radio" tabindex="7" name="<%=DefineReport.Radio.AREAKBN.getObj()%>" value="0" style="vertical-align:middle;"/>エリア</label>
					<label class="rad_lbl"><input type="radio" tabindex="8" name="<%=DefineReport.Radio.AREAKBN.getObj()%>" value="1" style="vertical-align:middle;" checked="checked"/>店グループ</label>
				</td>

			</tr>
			<tr>
				<td>
				<div id = "Items">
								<fieldset style="width:100px">
				<legend>アイテム数</legend>
				<input class="easyui-textbox" tabindex="-1" id="txt_countItem" col="F11" style="width:80px;text-align: right;">
				</fieldset>
				</div>
				</td>

			</tr>
		</table>
		</td>
	</tr>
</table>
<div>
</div>
</div>
</form>
<form id="gf" class="e_grid" >
	<div data-options="region:'center',border:false" style = "display:none;">
		<table id="<%=DefineReport.Grid.TENPO.getObj()%>" class="easyui-datagrid" tabindex="9" data-options="singleSelect:true,rownumbers:true,width:350, ">
				<thead>
					<tr>
					<th data-options="field:'TENCD'		,width:70  ,halign:'center',editor:'numberbox',formatter:function(value){var targetId = $.id_inp.txt_tencd;var check = $('#'+targetId).attr('check') ? JSON.parse('{'+$('#'+targetId).attr('check')+'}'): JSON.parse('{}');return $.getFormatLPad(value, check.maxlen);}">店コード</th>
					<th data-options="field:'TENKN'		,width:350 ,halign:'center',editor:{type:'textbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}">店舗名</th>
					</tr>
				</thead>
		</table>
		<div class="ref_editor" style="display: none;">
		<input  class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.TENCD.getObj()%>" check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' />
		<input  class="easyui-textbox"   tabindex="-1" id="<%=DefineReport.InpText.TENKN.getObj()%>" for_inp="<%=DefineReport.InpText.TENCD.getObj()%>_F3" check='<%=DefineReport.InpText.TENKN.getMaxlenTag()%>' />
		</div>
	</div>
</form>
<div id="buttons" data-options="region:'south',border:false" style="display:none;height:30px;padding:0 20px;">
	<div class="btn" style="float: left;">
		<table class="t-layout3">
		<tr><th style="min-width:135px;"></th><th style="min-width:10px;"></th>
			<th style="min-width:135px;"></th><th style="min-width:135px;"></th><th style="min-width:135px;"></th>
		</tr>
		<tr>
		<td><a href="#" title="戻る" id="<%=DefineReport.Button.BACK.getObj()%>" class="easyui-linkbutton" tabindex="10" iconCls="icon-undo" style="width:125px;"><span class="btnTxt">戻る</span></a></td>
		<td></td>
		<td><a href="#" class="easyui-linkbutton" tabindex="11" id="<%=DefineReport.Button.CANCEL.getObj()%>" title="<%=DefineReport.Button.CANCEL.getTxt()%>" iconCls="icon-cancel" style="width:125px;"><span><%=DefineReport.Button.CANCEL.getTxt()%></span></a></td>
		<td><a href="#" class="easyui-linkbutton" tabindex="12" id="<%=DefineReport.Button.UPD.getObj()%>" title="<%=DefineReport.Button.UPD.getTxt()%>" iconCls="icon-save" style="width:125px;"><span class="btnTxt"><%=DefineReport.Button.UPD.getTxt()%></span></a></td>
		<td><a href="#" class="easyui-linkbutton" tabindex="13" id="<%=DefineReport.Button.DEL.getObj()%>" title="<%=DefineReport.Button.DEL.getTxt()%>" iconCls="icon-remove" style="width:125px;"><span class="btnTxt"><%=DefineReport.Button.DEL.getTxt()%></span></a></td>
		</tr>
		</table>
	</div>
	<div id="disp_record_info" style="float: right;">
		<span class="labelName" tabindex="-1" style="padding-top: 3px;">
				登録日 <span col="F9" id="<%=DefineReport.Text.ADDDT.getObj()%>"></span>　更新日 <span col="F10" id="<%=DefineReport.Text.UPDDT.getObj()%>"></span>　オペレータ <span col="F8" id="<%=DefineReport.Text.OPERATOR.getObj()%>"></span>
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
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.x192.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.control.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.events.js?v=<%=prm %>"></script>
</html>