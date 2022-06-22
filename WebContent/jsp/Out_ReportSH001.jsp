
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


<%-- <table class="t-layout1">
	<tr>
		<td>商品コード</td>
		<td><input class="easyui-numberbox" tabindex="1" id="<%=DefineReport.InpText.SHNCD.getObj()%>" check='<%=DefineReport.InpText.SHNCD.getMaxlenTag()%>' style="width:100px;"></td>
		<td><a href="#" class="easyui-linkbutton" tabindex="2" id="<%=DefineReport.Button.SELECT.getObj()%>_shncd" title="<%=DefineReport.Button.SELECT.getTxt()%>"><span class="btnTxt" >選択</span></a></td>
	</tr>
	<tr>
		<td>部門コード</td>
		<td><select class="easyui-combobox" tabindex="3" id="<%=DefineReport.Select.BUMON.getObj()%>"style="width:100px;"></select></td>
		<td>大分類</td>
		<td><select class="easyui-combobox" tabindex="4" id="<%=DefineReport.Select.DAI_BUN.getObj()%>"style="width:100px;"></select></td>
		<td>中分類</td>
		<td><select class="easyui-combobox" tabindex="5" id="<%=DefineReport.Select.CHU_BUN.getObj()%>"style="width:100px;"></select></td>
	</tr>
	<tr>
		<td>仕入先コード</td>
		<td><input class="easyui-numberbox" tabindex="6" id="<%=DefineReport.InpText.SSIRCD.getObj()%>" check='<%=DefineReport.InpText.SSIRCD.getMaxlenTag()%>' style="width:100px;" for_btn="<%=DefineReport.Button.SIR.getObj()%>_F1"></td>
		<td><a href="#" class="easyui-linkbutton" tabindex="7" id="<%=DefineReport.Button.SIR.getObj()%>" title="<%=DefineReport.Button.SIR.getTxt()%>">仕入先</a></td>
		<td>メーカーコード</td>
		<td><a href="#" class="easyui-linkbutton" tabindex="9" id="<%=DefineReport.Button.MAKER.getObj()%>" title="<%=DefineReport.Button.MAKER.getTxt()%>" >メーカー</a></td>
		<td><input class="easyui-numberbox" tabindex="8" id="<%=DefineReport.InpText.MAKERCD.getObj()%>" check='<%=DefineReport.InpText.MAKERCD.getMaxlenTag()%>' style="width:100px;" for_btn="<%=DefineReport.Button.MAKER.getObj()%>_F2"></td>
	</tr>
	<tr><td>住所不定貫区分</td>
		<td><select class="easyui-combobox" tabindex="10" id="<%=DefineReport.MeisyoSelect.KBN121.getObj()%>" style="width:100px;"></select></td>
		<td>定計区分</td>
		<td><select class="easyui-combobox" tabindex="11" id="<%=DefineReport.MeisyoSelect.KBN117.getObj()%>" style="width:100px;"></select></td>
		<td>商品種類</td>
		<td><select class="easyui-combobox" tabindex="12" id="<%=DefineReport.MeisyoSelect.KBN105.getObj()%>" style="width:100px;"></select></td>
	</tr>
	<tr>
		<td>商品名漢字</td>
		<td colspan=2><input class="easyui-textbox" tabindex="13" id="<%=DefineReport.InpText.SHNKN.getObj()%>" check='<%=DefineReport.InpText.SHNKN.getMaxlenTag()%>' style="width:100px;"></td>
		<td><a href="#" class="easyui-linkbutton" tabindex="14" id="<%=DefineReport.Button.SEARCH.getObj()%>" title="<%=DefineReport.Button.SEARCH.getTxt()%>" iconCls="icon-search" style="width:100px;"><span class="btnTxt"><%=DefineReport.Button.SEARCH.getTxt()%></span></a></td>
	</tr>
</table> --%>

	<table class="t-layout">
	<tr>
	<td>
		<input class="easyui-numberbox" tabindex="1" id="<%=DefineReport.InpText.SHNCD.getObj()%>" check='<%=DefineReport.InpText.SHNCD.getMaxlenTag()%>' style="width:170px;" data-options="prompt:'____-____',label:'商品コード',labelWidth:90">
		<a href="#" class="easyui-linkbutton" tabindex="2" id="<%=DefineReport.Button.SELECT.getObj()%>_shncd" title="<%=DefineReport.Button.SELECT.getTxt()%>"><span class="btnTxt" >選択</span></a>
	</td>
	</tr>
	<tr>
	<td>
		<select class="easyui-combobox" tabindex="3" id="<%=DefineReport.Select.BUMON.getObj()%>" data-options="label:'部門',labelWidth:90" style="width:270px;"></select>
		<select class="easyui-combobox" tabindex="4" id="<%=DefineReport.Select.DAI_BUN.getObj()%>" data-options="label:'大分類',labelWidth:50" style="width:230px;"></select>
		<select class="easyui-combobox" tabindex="5" id="<%=DefineReport.Select.CHU_BUN.getObj()%>" data-options="label:'中分類',labelWidth:50" style="width:230px;"></select>
	</td>
	</tr>
	<tr>
	<td>
		<input class="easyui-numberbox" tabindex="6" id="<%=DefineReport.InpText.SSIRCD.getObj()%>" check='<%=DefineReport.InpText.SSIRCD.getMaxlenTag()%>' for_btn="<%=DefineReport.Button.SIR.getObj()%>_F1" data-options="label:'仕入先コード',labelWidth:90" style="width:150px;">
		<a href="#" class="easyui-linkbutton" tabindex="7" id="<%=DefineReport.Button.SIR.getObj()%>" title="<%=DefineReport.Button.SIR.getTxt()%>"><span class="btnTxt" >仕入先</span></a>
		<input class="easyui-numberbox" tabindex="8" id="<%=DefineReport.InpText.MAKERCD.getObj()%>" check='<%=DefineReport.InpText.MAKERCD.getMaxlenTag()%>' for_btn="<%=DefineReport.Button.MAKER.getObj()%>_F2" data-options="label:'メーカーコード',labelWidth:100" style="width:180px;">
		<a href="#" class="easyui-linkbutton" tabindex="9" id="<%=DefineReport.Button.MAKER.getObj()%>" title="<%=DefineReport.Button.MAKER.getTxt()%>" ><span class="btnTxt">メーカー</span></a>
	</td>
	</tr>
	<tr>
	<td>
		<select class="easyui-combobox" tabindex="10" id="<%=DefineReport.MeisyoSelect.KBN121.getObj()%>" style="width:180px;" data-options="label:'住所不定貫区分',labelWidth:90"></select>
		<select class="easyui-combobox" tabindex="11" id="<%=DefineReport.MeisyoSelect.KBN117.getObj()%>" style="width:180px;" data-options="label:'定計区分',labelWidth:60"></select>
		<select class="easyui-combobox" tabindex="12" id="<%=DefineReport.MeisyoSelect.KBN105.getObj()%>" style="width:210px;" data-options="label:'商品種類',labelWidth:60"></select>
	</td>
	</tr>
	<tr vAlign="middle">
	<td>
		<input class="easyui-textbox" tabindex="13" id="<%=DefineReport.InpText.SHNKN.getObj()%>" check='"datatyp":"text","maxlen":40' style="width:400px;"data-options="label:'商品名漢字',labelWidth:90">
		<a href="#" class="easyui-linkbutton" tabindex="14" id="<%=DefineReport.Button.SEARCH.getObj()%>" title="<%=DefineReport.Button.SEARCH.getTxt()%>" iconCls="icon-search" style="width:100px;"><span class="btnTxt"><%=DefineReport.Button.SEARCH.getTxt()%></span></a>
	</td>
	</tr>
	</table>
</div>
</form>
<form id="gf" class="e_grid">
<div id="list" data-options="region:'center',border:false" style="display:none;">

	<!-- EasyUI方式 -->
	<div tabindex="15" id="gridholder" class="easyui-datagrid placeFace" ></div>
</div>
<!-- Editor参照用 -->
</form>
<div id="buttons" data-options="region:'south',border:false" style="display:none;">
	<div class="btn" style="float: left;">
		<table class="t-layout3">
		<tr>
		<td><a href="#" title="戻る" id="<%=DefineReport.Button.BACK.getObj()%>" class="easyui-linkbutton" tabindex="15" iconCls="icon-undo" style="width:125px;"><span class="btnTxt">戻る</span></a></td>
		<td></td>
		<td><a href="#" title="<%=DefineReport.Button.CANCEL.getTxt()%>" class="easyui-linkbutton" tabindex="16" id="<%=DefineReport.Button.CANCEL.getObj()%>" iconCls="icon-cancel" style="width:125px;"><span><%=DefineReport.Button.CANCEL.getTxt()%></span></a></td>
		<td><a href="#" title="<%=DefineReport.Button.SELECT.getTxt()%>" class="easyui-linkbutton" tabindex="17" id="<%=DefineReport.Button.SELECT.getObj()%>" iconCls="icon-edit" style="width:100px;"><span><%=DefineReport.Button.SELECT.getTxt()%></span></a></td>


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
<div style="display:none;">
	<jsp:include page="Out_Reportwin001.jsp" flush="true" />
	<jsp:include page="Out_Reportwin002.jsp" flush="true" />
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
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.SH001.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.win001.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.win002.js?v=<%=prm %>"></script>

<script type="text/javascript" src="../js/jquery.report/jquery.report.control.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.events.js?v=<%=prm %>"></script>
</html>