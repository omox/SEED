
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
<div data-options="region:'north',border:false" style="display:none;height:90px;padding:2px 5px 0;overflow: visible;" >
<div class="lbl_box" style="width:500px;">
<table style="float:left;">
	<tr>
		<td>
			<table>
			<tr>
				<td>部門</td>
				<td><input class="easyui-numberbox" tabindex="1" col="F1" id="<%=DefineReport.InpText.BMNCD.getObj()%>" check='<%=DefineReport.InpText.BMNCD.getMaxlenTag()%>' data-options="required:true" style="width:40px;" value=""></td>
			</tr>
			</table>
		</td>
	</tr>
	<tr>
		<td>
			<table>
			<tr>
				<td></td>
				<td align="center">コピー先</td>
				<td align="center">　←　</td>
				<td align="center">コピー元</td>
			</tr>
			<tr>
				<td>店番</td>
				<td><input class="easyui-numberbox" tabindex="2" col="F2" id="<%=DefineReport.InpText.CPTOTENNO.getObj()%>" check='<%=DefineReport.InpText.CPTOTENNO.getMaxlenTag()%>' data-options="required:true,min:0,max:400" style="width:40px;" value=""></td>
				<td></td>
				<td><input class="easyui-numberbox" tabindex="3" col="F3" id="<%=DefineReport.InpText.CPFROMTENNO.getObj()%>" check='<%=DefineReport.InpText.CPFROMTENNO.getMaxlenTag()%>' data-options="required:true" style="width:40px;" value=""></td>
			</tr>
			</table>
		</td>
	</tr>
</table>
</div>
</div>
<div data-options="region:'center',border:false" style="display:none;">
	<table>
	<tr>
		<td>
		<fieldset style="width:560px;height: 110px;">
			<table>
			<tr>
				<td>
				<label class="rad_lbl"><input type="radio" tabindex="4" col="F4" id="<%=DefineReport.Radio.MSTKBN.getObj()%>" name="<%=DefineReport.Radio.MSTKBN.getObj()%>" value="1" style="vertical-align:middle;" checked="checked"/>ランクマスター</label>
				</td>

				<td>
					<fieldset style="width:380px;">
						<label class="rad_lbl"><input type="radio" tabindex="5" col="F5" name="<%=DefineReport.Radio.DATAKBN.getObj()%>_1" value="0" style="vertical-align:middle;" checked="checked"/>全て</label>
						<label class="rad_lbl"><input type="radio" tabindex="6" col="F5" name="<%=DefineReport.Radio.DATAKBN.getObj()%>_1" value="1" style="vertical-align:middle;"/>ランクNo.　</label>
						<input class="easyui-numberbox" tabindex="7" col="F6" id="<%=DefineReport.InpText.RANKNOST.getObj()%>" check='<%=DefineReport.InpText.RANKNOST.getMaxlenTag()%>' style="width:60px;" value="">
						～
						<input class="easyui-numberbox" tabindex="8" col="F7" id="<%=DefineReport.InpText.RANKNOED.getObj()%>" check='<%=DefineReport.InpText.RANKNOED.getMaxlenTag()%>' style="width:60px;" value="">
					</fieldset>
				</td>
			</tr>
			<tr>
				<td><label class="rad_lbl"><input type="radio" tabindex="9" col="F4" id="<%=DefineReport.Radio.MSTKBN.getObj()%>" name="<%=DefineReport.Radio.MSTKBN.getObj()%>" value="2" style="vertical-align:middle;"/>通常率パターンマスター</label></td>
				<td>
					<fieldset style="width:380px;">
						<label class="rad_lbl"><input type="radio" tabindex="10" col="F5" name="<%=DefineReport.Radio.DATAKBN.getObj()%>_2" value="0" style="vertical-align:middle;"/>全て</label>
						<label class="rad_lbl"><input type="radio" tabindex="11" col="F5" name="<%=DefineReport.Radio.DATAKBN.getObj()%>_2" value="1" style="vertical-align:middle;"/>パターンNo.</label>
						<input class="easyui-numberbox" tabindex="12" col="F8" id="<%=DefineReport.InpText.RTPTNNOST.getObj()%>" check='<%=DefineReport.InpText.RTPTNNOST.getMaxlenTag()%>' style="width:60px;" value="">
						～
						<input class="easyui-numberbox" tabindex="13" col="F9" id="<%=DefineReport.InpText.RTPTNNOED.getObj()%>" check='<%=DefineReport.InpText.RTPTNNOED.getMaxlenTag()%>' style="width:60px;" value="">
					</fieldset>
				</td>
			</tr>
			<tr>
				<td><label class="rad_lbl"><input type="radio" tabindex="14" col="F4" id="<%=DefineReport.Radio.MSTKBN.getObj()%>" name="<%=DefineReport.Radio.MSTKBN.getObj()%>" value="3" style="vertical-align:middle;"/>実績率パターンマスター</label></td>
				<td>

					<fieldset style="width:380px;">
						<label class="rad_lbl"><input type="radio" tabindex="15" col="F5" name="<%=DefineReport.Radio.DATAKBN.getObj()%>_3" value="0" style="vertical-align:middle;"/>全て</label>
						<label class="rad_lbl"><input type="radio" tabindex="16" col="F5" name="<%=DefineReport.Radio.DATAKBN.getObj()%>_3" value="1" style="vertical-align:middle;"/>パターンNo.</label>
						<input class="easyui-numberbox" tabindex="17" col="F10" id="<%=DefineReport.InpText.JRTPTNNOST.getObj()%>" check='<%=DefineReport.InpText.JRTPTNNOST.getMaxlenTag()%>' style="width:100px;" value="">
						～
						<input class="easyui-numberbox" tabindex="18" col="F11" id="<%=DefineReport.InpText.JRTPTNNOED.getObj()%>" check='<%=DefineReport.InpText.JRTPTNNOED.getMaxlenTag()%>' style="width:100px;" value="">
					</fieldset>
				</td>
			</tr>
			</table>
		</fieldset>
		</td>
	<tr/>
	</table>
</div>
</form>
<div id="buttons" data-options="region:'south',border:false" style="display:none;height:30px;padding:0 20px;">
	<div class="btn" style="float: left;">
		<table class="t-layout3">
			<tr>
				<td><a href="#" title="戻る" id="<%=DefineReport.Button.BACK.getObj()%>" class="easyui-linkbutton" tabindex="19" iconCls="icon-undo" style="width:125px;"><span class="btnTxt">戻る</span></a></td>
				<td><a href="#" title="キャンセル" id="<%=DefineReport.Button.CANCEL.getObj()%>" class="easyui-linkbutton" tabindex="20" iconCls="icon-cancel" style="width:125px;"><span class="btnTxt">キャンセル</span></a></td>
				<td><a href="#" title="登録" id="<%=DefineReport.Button.UPD.getObj()%>" class="easyui-linkbutton" tabindex="21" iconCls="icon-save" style="width:125px;"><span class="btnTxt">登録</span></a></td>
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
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.RP007.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.control.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.events.js?v=<%=prm %>"></script>
</html>