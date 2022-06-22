
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
<div id="search" data-options="region:'north',border:false" style="display:none;height:250px;padding:3px 8px 0;" >
<table>
	<tr>
		<td>
			<input class="easyui-textbox" tabindex="1" col="F1" id="<%=DefineReport.InpText.COMAN.getObj()%>" check='<%=DefineReport.InpText.COMAN.getMaxlenTag()%>' data-options="label:'コメント(15文字)',labelWidth:125,required:true" style="width:270px;">
		</td>
	</tr>
	<tr>
		<td>
			<input class="easyui-numberbox" tabindex="2" col="F2" id="<%=DefineReport.InpText.MST_YOYAKUDT.getObj()%>" check='<%=DefineReport.InpText.MST_YOYAKUDT.getMaxlenTag()%>' data-options="label:'商品マスター予約日付',labelWidth:125,prompt:'__/__/__'" style="width:195px;">
		</td>
	</tr>
	<tr>
		<td>
			<div style="float:left;height:180px;padding:3px 8px 0;">
			<fieldset style="width:230px;height: 140px;">
				<div class="inp_box" style="float:left;height:180px;padding:3px 8px 0;">
					<label class="rad_lbl"><input type="radio" col="F3" tabindex="3" name="<%=DefineReport.Radio.MAISUHOHOKB1.getObj()%>" value="1" style="vertical-align:middle;" checked="checked"/>枚数指定</label><br>
					<label class="rad_lbl"><input type="radio" col="F3" tabindex="4" name="<%=DefineReport.Radio.MAISUHOHOKB1.getObj()%>" value="2" style="vertical-align:middle;"/>同一枚数</label>
				</div>
				<div style="float:left;height:180px;padding:3px 8px 0;">
					<fieldset style="width:80px">
						<legend>PCサイズ</legend>
						<div class="inp_box">
							<label class="rad_lbl"><input type="radio" col="F4" tabindex="5" name="<%=DefineReport.Radio.PCARDSZ1.getObj()%>" value="1" style="vertical-align:middle;" checked="checked"/>大</label>
							<label class="rad_lbl"><input type="radio" col="F4" tabindex="6" name="<%=DefineReport.Radio.PCARDSZ1.getObj()%>" value="2" style="vertical-align:middle;"/>小</label>
						</div>
					</fieldset>
					<a href="#" class="easyui-linkbutton" tabindex="7" id="<%=DefineReport.Button.SAKUBAIKAKB1.getObj()%>" title="<%=DefineReport.Button.SAKUBAIKAKB1.getTxt()%>" iconCls="icon-edit" style="margin-top: 50px"><span class="btnTxt2" style="width:90px;text-align:left"><%=DefineReport.Button.SAKUBAIKAKB1.getTxt()%></span></a>
				</div>
			</fieldset>
			</div>
			<div style="float:left;height:180px;padding:3px 8px 0;">
			<fieldset style="width:330px;height: 140px;">
				<div class="inp_box" style="float:left;height:180px;padding:3px 8px 0;">
					<label class="rad_lbl"><input type="radio" col="F5" tabindex="8" name="<%=DefineReport.Radio.MAISUHOHOKB2.getObj()%>" value="3" style="vertical-align:middle;" checked="checked"/>店、構成ページ、枚数指定</label><br>
					<label class="rad_lbl"><input type="radio" col="F5" tabindex="9" name="<%=DefineReport.Radio.MAISUHOHOKB2.getObj()%>" value="4" style="vertical-align:middle;"/>店、部門、枚数指定</label><br>
					<label class="rad_lbl"><input type="radio" col="F5" tabindex="10" name="<%=DefineReport.Radio.MAISUHOHOKB2.getObj()%>" value="5" style="vertical-align:middle;"/>店指定</label><br>
					<label class="rad_lbl"><input type="radio" col="F5" tabindex="11" name="<%=DefineReport.Radio.MAISUHOHOKB2.getObj()%>" value="6" style="vertical-align:middle;"/>全店</label>
				</div>
				<div style="float:left;height:180px;padding:3px 8px 0;">
					<fieldset style="width:80px">
						<legend>PCサイズ</legend>
							<div class="inp_box">
								<label class="rad_lbl"><input type="radio" col="F6" tabindex="12" name="<%=DefineReport.Radio.PCARDSZ2.getObj()%>" value="1" style="vertical-align:middle;" checked="checked"/>大</label>
								<label class="rad_lbl"><input type="radio" col="F6" tabindex="13" name="<%=DefineReport.Radio.PCARDSZ2.getObj()%>" value="2" style="vertical-align:middle;"/>小</label>
							</div>
					</fieldset>
					<a href="#" class="easyui-linkbutton" tabindex="14" id="<%=DefineReport.Button.SAKUBAIKAKB2.getObj()%>" title="<%=DefineReport.Button.SAKUBAIKAKB2.getTxt()%>" iconCls="icon-edit" style="margin-top: 50px"><span class="btnTxt2" style="width:90px;text-align:left"><%=DefineReport.Button.SAKUBAIKAKB2.getTxt()%></span></a>
				</div>
			</fieldset>
			</div>
		</td>
	</tr>
</table>
<div>
</div>
</div>
</form>
<div id="buttons" data-options="region:'south',border:false" style="display:none;height:30px;padding:0 20px;">
	<div class="btn" style="float: left;">
		<table class="t-layout3">
			<tr>
				<td><a href="#" title="戻る" id="<%=DefineReport.Button.BACK.getObj()%>" class="easyui-linkbutton" tabindex="15" iconCls="icon-undo" style="width:125px;"><span class="btnTxt">戻る</span></a></td>
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
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.x211.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.control.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.events.js?v=<%=prm %>"></script>
</html>