
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
	<table>
	<tr>
		<td>月度</td>
		<td><input class="easyui-numberbox" tabindex="1" col="F1" id="<%=DefineReport.InpText.QAYYYYMM.getObj()%>" check='<%=DefineReport.InpText.QAYYYYMM.getMaxlenTag()%>' data-options="required:true" style="width:50px;" value=""></td>
		<td><input class="easyui-numberbox" tabindex="2" col="F2" id="<%=DefineReport.InpText.QAEND.getObj()%>" check='<%=DefineReport.InpText.QAEND.getMaxlenTag()%>' data-options="required:true" style="width:30px;" value=""></td>
		<td id ='leaderTen'>リーダー店</td>
		<td id ='miseban'>店番</td>
		<td><input class="easyui-numberbox" tabindex="3" col="F3" id="<%=DefineReport.InpText.TENCD.getObj()%>" data-options="required:true" check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' style="width:50px;" value=""></td>
		<td><input class="easyui-textbox"   id="<%=DefineReport.InpText.TENKN.getObj()%>" col="F4" for_inp="<%=DefineReport.InpText.TENCD.getObj()%>_F3" check='<%=DefineReport.InpText.TENKN.getMaxlenTag()%>' style="width:300px;" value="" disabled="disabled"></td>
		<td class="col_btn"><a href="#" title="検索" id="btn_search" class="easyui-linkbutton"  tabindex="6" iconCls="icon-search"><span class="btnTxt">検索</span></a></td>
	</tr>
	<tr>
		<td>強制 </td>
		<td><label class="chk_lbl" for="kyosei_flg"><input type="checkbox" tabindex="4" id="kyosei_flg" value="1" col="F5" /></label></td>
	</tr>
	</table>
	<table id ='kaitouName'>
	<tr>
		<td >回答 </td>
		<td><input class="easyui-textbox"  style="width:40px;" value="" col="F6" id= "<%=DefineReport.InpText.MBANSFLG.getObj()%>" disabled="disabled"></td>
		<td >
		<font size="1">
			　　　　　　　　　　　　　注意：本画面では事前発注リストが作成されておらず、かつリーダー店が本締<br>
			　　　　　　　　　　　　　めの場合のみ編集が行えます。
		</font>
		</td>
	</tr>
	</table>
</div>
</form>
<form id="gf" class="e_grid">
<div id="list" data-options="region:'center',border:false" style="display:none;">
	<div class="easyui-datagrid" tabindex=9" id="gridholder"></div>
	<!-- Editor参照用 -->
	<div class="ref_editor" style="display: none;">
	<input type="checkbox" tabindex="-1" id="<%=DefineReport.Checkbox.MBSYFLG.getObj()%>"/>
	<input type="checkbox" tabindex="-1" id="<%=DefineReport.Checkbox.HBSTRTFLG.getObj()%>1"/>
	<input type="checkbox" tabindex="-1" id="<%=DefineReport.Checkbox.HBSTRTFLG.getObj()%>2"/>
	</div>
</div>
</form>
<div id="buttons" data-options="region:'south',border:false" style="display:none;height:30px;padding:0 30px;text-align: center;">
<div class="btn" style="float: left;">
	<table class="t-layout3">
	<tr><th style="min-width:135px;"></th><th style="min-width:10px;"></th>
		<th style="min-width:135px;"></th>
	</tr>
	<td><a href="#" title="戻る" class="easyui-linkbutton"  id="<%=DefineReport.Button.BACK.getObj()%>" tabindex="10" iconCls="icon-undo" style="width:125px;"><span class="btnTxt">戻る</span></a></td>
	<td></td>
	<td><a href="#" title="キャンセル" class="easyui-linkbutton" tabindex="12" id="<%=DefineReport.Button.CANCEL.getObj()%>" iconCls="icon-cancel" style="width:110px;"><%=DefineReport.Button.CANCEL.getTxt()%></a></td>
	<td><a href="#" title="登録" class="easyui-linkbutton" tabindex="13" id="<%=DefineReport.Button.UPD.getObj()%>" title="<%=DefineReport.Button.UPD.getTxt()%>" iconCls="icon-save" style="width:125px;"><span class="btnTxt">登録</span></a></td>
	<td><a href="#" title="選択(売価回答)" id="<%=DefineReport.Button.SEL_CHANGE.getObj()%>" class="easyui-linkbutton" tabindex="14" iconCls="icon-ok" style="width:125px;"><span>選択(売価回答)</span></a></td>
	<td></td>
	</table>
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
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.TG015.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.control.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.events.js?v=<%=prm %>"></script>
</html>