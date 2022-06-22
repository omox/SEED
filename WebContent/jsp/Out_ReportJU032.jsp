
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
	String page_suffix = "_win006";
	String page_suffix2 = "_winST010";
	String page_suffix3 = "_JU017";
	String page_suffix4 = "_winST009";
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
<div id="search" data-options="region:'north',border:false" style="display:none;height:170px;padding:3px 8px 0;" >
<table class="t-layout1">
	<tr>
		<td>
			<input class="easyui-numberbox" tabindex="1" col="F1" id="<%=DefineReport.InpText.MOYSCD.getObj()%>" check='<%=DefineReport.InpText.MOYSCD.getMaxlenTag()%>' data-options="prompt:'_-______-___',label:'催しコード',labelWidth:80" style="width:175px;" disabled="disabled">
			<input class="easyui-textbox" id="<%=DefineReport.InpText.MOYKN.getObj()%>" data-options="label:'　　催し名称',labelWidth:80" style="width:320px;" disabled="disabled"/>
		</td>
	</tr>
	<tr>
		<td>
			<input class="easyui-textbox" style="width:260px;" id="kikan_dummy" value="" data-options="label:'納入期間',labelWidth:80,required:true" disabled="disabled">
			<input class="easyui-textbox" id="<%=DefineReport.InpText.QASMDT.getObj()%>" data-options="prompt:'<%=DefineReport.Label.PROMPT_DTW.getTxt()%>',label:'　　店舗締切日',labelWidth:90" style="width:180px;" disabled="disabled"/>
		</td>
	</tr>
	<tr>
		<td>
			<input class="easyui-numberbox" col="F2" tabindex="1" id="<%=DefineReport.InpText.SHNCD.getObj()%>" check='<%=DefineReport.InpText.SHNCD.getMaxlenTag()%>' data-options="prompt:'____-____',label:'商品コード',labelWidth:80,required:true" style="width:155px;" for_btn="<%=DefineReport.Button.SHNCD.getObj()%>_VALUE">
			<input class="easyui-textbox" id="<%=DefineReport.InpText.SHNKN.getObj()%>" check='<%=DefineReport.InpText.SHNKN.getMaxlenTag()%>' for_inp="<%=DefineReport.InpText.SHNCD.getObj()%>_F1" style="width:280px;" disabled="disabled">
			<a href="#" class="easyui-linkbutton" tabindex="2" id="<%=DefineReport.Button.SHNCD.getObj()%>" title="<%=DefineReport.Button.SHNCD.getTxt()%>" >商品コード検索</a>
		</td>
	</tr>
	<tr>
		<td>
			<input class="easyui-combobox" tabindex="3" col="F3" id="<%=DefineReport.MeisyoSelect.KBN103051.getObj()%>" data-options="label:'商品区分',labelWidth:80,required:true" style="width:200px;">
			<input class="easyui-combobox" tabindex="4" col="F4" id="<%=DefineReport.MeisyoSelect.KBN103061.getObj()%>" data-options="label:'訂正区分',labelWidth:80,required:true" style="width:235px;">
			<input class="easyui-textbox" col="F5" id="<%=DefineReport.InpText.JUKBN.getObj()%>" value="1" data-options="label:'事前区分',labelWidth:60" disabled="disabled"  style="width:120px;text-align: left;">
			<input class="easyui-numberbox" tabindex="5" col="F6" id="<%=DefineReport.InpText.BDENKBN.getObj()%>" check='<%=DefineReport.InpText.BDENKBN.getMaxlenTag()%>' value="" data-options="label:'　　　　別伝区分',labelWidth:130,min:0,max:8" style="width:200px;text-align: left;">
		</td>
		<td>
			<input class="easyui-combobox" tabindex="6" col="F7" id="<%=DefineReport.MeisyoSelect.KBN103081.getObj()%>" for_inp="<%=DefineReport.InpText.SHNCD.getObj()%>_F2" data-options="label:'ワッペン区分',labelWidth:80,required:true" style="width:200px;">
		</td>
	</tr>
	<tr>
		<td>
			<input class="easyui-numberbox" tabindex="7" col="F8" id="<%=DefineReport.InpText.IRISU.getObj()%>" check='<%=DefineReport.InpText.IRISU.getMaxlenTag()%>' for_inp="<%=DefineReport.InpText.SHNCD.getObj()%>_F3" data-options="label:'入数',labelWidth:80,required:true,min:0" style="width:200px;text-align: right;">
			<input class="easyui-numberbox" tabindex="8" col="F9" id="<%=DefineReport.InpText.GENKAAM.getObj()%>" check='<%=DefineReport.InpText.GENKAAM.getMaxlenTag()%>' for_inp="<%=DefineReport.InpText.SHNCD.getObj()%>_F4" data-options="label:'原価',labelWidth:80,required:true,min:0" style="width:200px;text-align: right;">
			<input class="easyui-numberbox" tabindex="9" col="F10" id="<%=DefineReport.InpText.RG_BAIKAAM.getObj()%>" check='<%=DefineReport.InpText.RG_BAIKAAM.getMaxlenTag()%>' for_inp="<%=DefineReport.InpText.SHNCD.getObj()%>_F5" data-options="label:'　　　総売価',labelWidth:95,required:true,min:0" style="width:205px;text-align: right;">
			<input class="easyui-numberbox" col="F11" id="<%=DefineReport.InpText.BAIKAAM.getObj()%>" for_inp="<%=DefineReport.InpText.SHNCD.getObj()%>_F6" data-options="label:'本体売価',labelWidth:80,required:true" style="width:200px;text-align: right;" disabled="disabled">
		</td>
	</tr>
	<tr>
		<td>
			<input class="easyui-numberbox" tabindex="10" col="F12" id="<%=DefineReport.InpText.HTDT.getObj()%>" check='<%=DefineReport.InpText.HTDT.getMaxlenTag()%>' data-options="prompt:'<%=DefineReport.Label.PROMPT_DTW.getTxt()%>',label:'発注日',labelWidth:80,required:true" style="width:170px;">
			<input class="easyui-numberbox" tabindex="11" col="F13" id="<%=DefineReport.InpText.NNDT.getObj()%>" check='<%=DefineReport.InpText.NNDT.getMaxlenTag()%>' data-options="prompt:'<%=DefineReport.Label.PROMPT_DTW.getTxt()%>',label:'　　  納入日',labelWidth:110,required:true" style="width:200px;">
			<!-- 非表示でデータを保持 -->
			<input type="hidden" name="<%=DefineReport.Text.SHORIDT.getObj()%>" id="<%=DefineReport.Text.SHORIDT.getObj()%>" />
		</td>
	</tr>
</table>
</div>
<div data-options="region:'center',border:false" style="display:none;">
<div id="tt" class="easyui-tabs" data-options="plain:true,narrow:true,justified:true,fit:true">
<div title="同一数量発注入力" style="display:none;padding:3px 8px;" data-options="selected:true,index:0">
<table>
	<tr>
		<td>
			<input class="easyui-numberbox" col="F14" tabindex="12" id="<%=DefineReport.InpText.TENRANK.getObj()%>" check='<%=DefineReport.InpText.TAISYOTEN.getMaxlenTag()%>' data-options="label:'ランク',labelWidth:60" style="width:115px;" for_btn="<%=DefineReport.Button.RANKNO.getObj()%>_F1">
			<a href="#" class="easyui-linkbutton" tabindex="-1" id="<%=DefineReport.Button.RANKNO.getObj()%>" title="<%=DefineReport.Button.RANK.getTxt()%>" >ランク選択</a>
			<a href="#" class="easyui-linkbutton" tabindex="-1" id="<%=DefineReport.Button.RINZIRANK.getObj()%>" title="<%=DefineReport.Button.RINZIRANK.getTxt()%>" >臨時ランク作成</a>
		</td>
	</tr>
	<tr>
		<td>
			<input class="easyui-numberbox" col="F15" tabindex="15" id="<%=DefineReport.InpText.HTSU.getObj()%>" check='<%=DefineReport.InpText.HTSU.getMaxlenTag()%>' data-options="label:'発注数',labelWidth:60,min:0" style="width:115px;text-align:right" />
		</td>
	</tr>
</table>
</div>
<div title="ランク別発注数量入力" style="display:none;padding:3px 8px;" data-options="selected:true,index:1">
<table>
	<tr>
		<td>
			<input class="easyui-numberbox" col="F16" tabindex="16" id="<%=DefineReport.InpText.TENRANK.getObj()%>_2" check='<%=DefineReport.InpText.TAISYOTEN.getMaxlenTag()%>' data-options="label:'ランク',labelWidth:60" style="width:100px;" for_btn="<%=DefineReport.Button.RANKNO.getObj()%>2_F1">
			<a href="#" class="easyui-linkbutton" tabindex="-1" id="<%=DefineReport.Button.RANKNO.getObj()%>2" title="<%=DefineReport.Button.RANK.getTxt()%>" >ランク選択</a>
			<a href="#" class="easyui-linkbutton" tabindex="-1" id="<%=DefineReport.Button.RINZIRANK.getObj()%>2" title="<%=DefineReport.Button.RINZIRANK.getTxt()%>" >臨時ランク作成</a>
		</td>
	</tr>
	<tr>
		<td>
			<input class="easyui-numberbox" col="F17" tabindex="19" id="<%=DefineReport.InpText.SURYOPTN.getObj()%>" check='<%=DefineReport.InpText.SURYOPTN.getMaxlenTag()%>' data-options="label:'パターン',labelWidth:60" style="width:100px;" for_btn="<%=DefineReport.Button.SURYO.getObj()%>_F1"/>
			<a href="#" class="easyui-linkbutton" tabindex="-1" id="<%=DefineReport.Button.SURYO.getObj()%>" title="<%=DefineReport.Button.SURYO.getTxt()%>" >数量パターン選択</a>
		</td>
	</tr>
</table>
</div>
<div title="店別数量発注入力" style="display:none;padding:3px 8px;" data-options="selected:true,index:2">
<table>
	<tr>
		<td style = "vertical-align:top">
		<div>
			<table id="grd_tenhtsu_arr" class="easyui-datagrid" tabindex="21" data-options="singleSelect:true,rownumbers:true,width:470,height:320">
				<thead>
					<tr>
					<th data-options="field:'TENCD',width:60,halign:'center',align:'left',editor:{type:'numberbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},formatter:function(value){var targetId = $.id_inp.txt_tencd;var check = $('#'+targetId).attr('check') ? JSON.parse('{'+$('#'+targetId).attr('check')+'}'): JSON.parse('{}');return $.getFormatLPad(value, check.maxlen);}">店コード</th>
					<th data-options="field:'TENKN',width:300,halign:'center',editor:{type:'textbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:true}}">店舗名</th>
					<th data-options="field:'SURYO',width:60,halign:'center',align:'right',formatter:function(value,row,index){ return $.getFormat(value, '##,##0');},editor:{type:'numberbox'}">数量</th>
					</tr>
				</thead>
			</table>
			<!-- Editor参照用 -->
			<div class="ref_editor" style="display: none;">
			<input  class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.TENCD.getObj()%>" check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' />
			<input  class="easyui-textbox" tabindex="-1" id="<%=DefineReport.InpText.TENKN.getObj()%>" check='<%=DefineReport.InpText.TENKN.getMaxlenTag()%>' />
			<input  class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.SURYO.getObj()%>" check='<%=DefineReport.InpText.SURYO.getMaxlenTag()%>' data-options="min:0" />
			</div>
		</div>
		</td>
	</tr>
</table>
</div>
</div>
</div>
<div style="display: none;">
<!-- 非表示でデータを保持 -->
<a href="#" class="easyui-linkbutton" tabindex="-1" id="<%=DefineReport.Button.TENINFO.getObj()%>" title="<%=DefineReport.Button.TENINFO.getTxt()%>" iconCls="icon-search" style="width:180px;"><span><%=DefineReport.Button.TENINFO.getTxt()%></span></a>
<a href="#" class="easyui-linkbutton" tabindex="-1" id="<%=DefineReport.Button.RANKTENINFO.getObj()%>" title="<%=DefineReport.Button.RANKTENINFO.getTxt()%>" iconCls="icon-search" style="width:180px;"><span><%=DefineReport.Button.RANKTENINFO.getTxt()%></span></a>
</div>
</form>
<!-- <form id="gf" class="e_grid">
<div id="list" data-options="region:'center',border:false" style="display:none;">

	EasyUI方式
	<div tabindex="6" id="gridholder" class="easyui-datagrid placeFace" ></div>
</div>
</form> -->
<div id="buttons" data-options="region:'south',border:false" style="display:none;height:30px;padding:0 30px;text-align: center;">
	<div class="btn" style="text-align: left;">
		<table class="t-layout3">
		<tr><th style="min-width:120px;"></th><th style="min-width:120px;"></th><th style="min-width:120px;"></th></tr>
		<tr>
		<td><a href="#" title="戻る" id="<%=DefineReport.Button.BACK.getObj()%>" class="easyui-linkbutton" tabindex="101" iconCls="icon-undo" style="width:110px;"><span class="btnTxt">戻る</span></a></td>
		<td><a href="#" class="easyui-linkbutton" tabindex="102" id="<%=DefineReport.Button.CANCEL.getObj()%>" title="<%=DefineReport.Button.CANCEL.getTxt()%>" iconCls="icon-cancel" style="width:110px;"><span>キャンセル</span></a></td>
		<td><a href="#" class="easyui-linkbutton" tabindex="103" id="<%=DefineReport.Button.UPD.getObj()%>" title="<%=DefineReport.Button.UPD.getTxt()%>" iconCls="icon-save" style="width:110px;"><span class="btnTxt"><%=DefineReport.Button.UPD.getTxt()%></span></a></td>
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

	<input type="hidden" name="sendBtnid" id="sendBtnid" value='' />
</div>

<div style="display:none;">
<jsp:include page="Out_Reportwin001.jsp" flush="true" />
<jsp:include page="Out_Reportwin002.jsp" flush="true" />
<jsp:include page="Out_Reportwin006.jsp" flush="true" />
<jsp:include page="Out_ReportwinST007.jsp" flush="true" />
<jsp:include page="Out_ReportwinST008.jsp" flush="true" />
<jsp:include page="Out_ReportwinST009.jsp" flush="true" />
<jsp:include page="Out_ReportwinST010.jsp" flush="true" />
<jsp:include page="Out_ReportwinST011.jsp" flush="true" />
<jsp:include page="Out_ReportJU017.jsp" flush="true" />
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
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.JU032.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.JU017.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.win001.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.win002.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.win006.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.winST007.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.winST008.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.winST009.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.winST010.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.winST011.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.control.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.events.js?v=<%=prm %>"></script>
</html>