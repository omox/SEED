
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
		<div data-options="region:'north',border:false" style="display:none;height:130px;">
			<table class="t-layout1" style="margin:10px;">
				<tr>
					<td style="padding-right:18px;">商品コード</td>
					<td style="height:30px;">
						<input class="easyui-numberbox" tabindex="-1" col="F1" id="<%=DefineReport.InpText.SHNCD.getObj()%>" check='<%=DefineReport.InpText.SHNCD.getMaxlenTag()%>' data-options="prompt:'____-____'" style="width:80px;">
						<input class="easyui-textbox" tabindex="-1" col="F2" id="<%=DefineReport.InpText.SHNKN.getObj()%>" style="width:310px;">
					</td>
					<td style="padding-left:40px;">便区分</td>
					<td style="height:30px;">
						<input class="easyui-numberbox" tabindex="-1" col="F3" id="<%=DefineReport.InpText.BINKBN.getObj()%>" style="width:25px;">
					</td>
				</tr>
			</table>
			<table class="t-layout" style="margin:10px;">
				<tr>
					<td></td>
					<td style="text-align:center;">月</td>
					<td style="text-align:center;">火</td>
					<td style="text-align:center;">水</td>
					<td style="text-align:center;">木</td>
					<td style="text-align:center;">金</td>
					<td style="text-align:center;">土</td>
					<td style="text-align:center;">日</td>
				</tr>
				<tr>
					<td style="padding-right:30px;">訂正区分</td>
					<td>
						<div class="inp_box">
							<input class="easyui-combobox" tabindex="1" col="F4" id="<%=DefineReport.MeisyoSelect.KBN105014.getObj()%>" data-options="panelHeight:200,required:true" style="width:140px;">
						</div>
					</td>
					<td>
						<div class="inp_box">
							<input class="easyui-combobox" tabindex="2" col="F5" id="<%=DefineReport.MeisyoSelect.KBN105017.getObj()%>" data-options="panelHeight:200,required:true" style="width:140px;">
						</div>
					</td>
					<td>
						<div class="inp_box">
							<input class="easyui-combobox" tabindex="3" col="F6" id="<%=DefineReport.MeisyoSelect.KBN105018.getObj()%>" data-options="panelHeight:200,required:true" style="width:140px;">
						</div>
					</td>
					<td>
						<div class="inp_box">
							<input class="easyui-combobox" tabindex="4" col="F7" id="<%=DefineReport.MeisyoSelect.KBN105019.getObj()%>" data-options="panelHeight:200,required:true" style="width:140px;">
						</div>
					</td>
					<td>
						<div class="inp_box">
							<input class="easyui-combobox" tabindex="5" col="F8" id="<%=DefineReport.MeisyoSelect.KBN1050110.getObj()%>" data-options="panelHeight:200,required:true" style="width:140px;">
						</div>
					</td>
					<td>
						<div class="inp_box">
							<input class="easyui-combobox" tabindex="6" col="F9" id="<%=DefineReport.MeisyoSelect.KBN1050111.getObj()%>" data-options="panelHeight:200,required:true" style="width:140px;">
						</div>
					</td>
					<td>
						<div class="inp_box">
							<input class="easyui-combobox" tabindex="7" col="F10" id="<%=DefineReport.MeisyoSelect.KBN1050112.getObj()%>" data-options="panelHeight:200,required:true" style="width:140px;">
						</div>
					</td>
				</tr>
				<tr id="hid_inp">
					<td>全店同一数量</td>
					<td>
						<input class="easyui-numberbox" tabindex="8" id="<%=DefineReport.InpText.ALL_SURYO_MON.getObj()%>" check='<%=DefineReport.InpText.ALL_SURYO_MON.getMaxlenTag()%>' style="width:140px;text-align:right" data-options="numonly:true">
					</td>
					<td>
						<input class="easyui-numberbox" tabindex="9" id="<%=DefineReport.InpText.ALL_SURYO_TUE.getObj()%>" check='<%=DefineReport.InpText.ALL_SURYO_TUE.getMaxlenTag()%>' style="width:140px;text-align:right" data-options="numonly:true">
					</td>
					<td>
						<input class="easyui-numberbox" tabindex="10" id="<%=DefineReport.InpText.ALL_SURYO_WED.getObj()%>" check='<%=DefineReport.InpText.ALL_SURYO_WED.getMaxlenTag()%>' style="width:140px;text-align:right" data-options="numonly:true">
					</td>
					<td>
						<input class="easyui-numberbox" tabindex="11" id="<%=DefineReport.InpText.ALL_SURYO_THU.getObj()%>" check='<%=DefineReport.InpText.ALL_SURYO_THU.getMaxlenTag()%>' style="width:140px;text-align:right" data-options="numonly:true">
					</td>
					<td>
						<input class="easyui-numberbox" tabindex="12" id="<%=DefineReport.InpText.ALL_SURYO_FRI.getObj()%>" check='<%=DefineReport.InpText.ALL_SURYO_FRI.getMaxlenTag()%>' style="width:140px;text-align:right" data-options="numonly:true">
					</td>
					<td>
						<input class="easyui-numberbox" tabindex="13" id="<%=DefineReport.InpText.ALL_SURYO_SAT.getObj()%>" check='<%=DefineReport.InpText.ALL_SURYO_SAT.getMaxlenTag()%>' style="width:140px;text-align:right" data-options="numonly:true">
					</td>
					<td>
						<input class="easyui-numberbox" tabindex="14" id="<%=DefineReport.InpText.ALL_SURYO_SUN.getObj()%>" check='<%=DefineReport.InpText.ALL_SURYO_SUN.getMaxlenTag()%>' style="width:140px;text-align:right" data-options="numonly:true">
					</td>
				</tr>
			</table>
		</div>
		<div data-options="region:'center',border:false" style="display:none;padding:2px 5px 0;">
			<div class="easyui-datagrid" tabindex="15" id="<%=DefineReport.Grid.HATSTRSHNTEN.getObj()%>_list" data-options="singleSelect:true,rownumbers:true, fit:true"></div>
			<div class="ref_editor" style="display: none;">
				<input  class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.TENCD.getObj()%>" check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' />
				<input  class="easyui-textbox"   tabindex="-1" id="<%=DefineReport.InpText.TENKN.getObj()%>" for_inp="<%=DefineReport.InpText.TENCD.getObj()%>_F3" check='<%=DefineReport.InpText.TENKN.getMaxlenTag()%>' />
				<input  class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.SURYO_MON.getObj()%>" for_inp="<%=DefineReport.InpText.ALL_SURYO_MON.getObj()%>" check='<%=DefineReport.InpText.SURYO_MON.getMaxlenTag()%>' data-options="numonly:true"/>
				<input  class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.SURYO_TUE.getObj()%>" for_inp="<%=DefineReport.InpText.ALL_SURYO_TUE.getObj()%>" check='<%=DefineReport.InpText.SURYO_TUE.getMaxlenTag()%>' data-options="numonly:true"/>
				<input  class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.SURYO_WED.getObj()%>" for_inp="<%=DefineReport.InpText.ALL_SURYO_WED.getObj()%>" check='<%=DefineReport.InpText.SURYO_WED.getMaxlenTag()%>' data-options="numonly:true"/>
				<input  class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.SURYO_THU.getObj()%>" for_inp="<%=DefineReport.InpText.ALL_SURYO_THU.getObj()%>" check='<%=DefineReport.InpText.SURYO_THU.getMaxlenTag()%>' data-options="numonly:true"/>
				<input  class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.SURYO_FRI.getObj()%>" for_inp="<%=DefineReport.InpText.ALL_SURYO_FRI.getObj()%>" check='<%=DefineReport.InpText.SURYO_FRI.getMaxlenTag()%>' data-options="numonly:true"/>
				<input  class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.SURYO_SAT.getObj()%>" for_inp="<%=DefineReport.InpText.ALL_SURYO_SAT.getObj()%>" check='<%=DefineReport.InpText.SURYO_SAT.getMaxlenTag()%>' data-options="numonly:true"/>
				<input  class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.SURYO_SUN.getObj()%>" for_inp="<%=DefineReport.InpText.ALL_SURYO_SUN.getObj()%>" check='<%=DefineReport.InpText.SURYO_SUN.getMaxlenTag()%>' data-options="numonly:true"/>
			<!--一部の入力項目については配送パターンにて定義済み  -->
			<!--名称マスタ項目については他の項目にて定義済み  -->
			<!--[実仕入先名称]項目については他の項目にて定義済み  -->
			</div>
		</div>
	</form>
	<div id="buttons" data-options="region:'south',border:false" style="display:none;height:30px;padding:0 20px;">
		<div class="btn" style="float: left;">
			<table class="t-layout3">
			<tr>
				<th style="min-width:135px;"></th><th style="min-width:10px;">
				<th style="min-width:120px;"></th><th style="min-width:120px;"></th><th style="min-width:120px;"></th></tr>
			<tr>
			<td><a href="#" title="戻る" id="<%=DefineReport.Button.BACK.getObj()%>" class="easyui-linkbutton" tabindex="16" iconCls="icon-undo" style="width:125px;"><span class="btnTxt">戻る</span></a></td>
			<td></td>
			<td><a href="#" title="キャンセル" class="easyui-linkbutton" tabindex="17" id="<%=DefineReport.Button.CANCEL.getObj()%>" iconCls="icon-cancel" style="width:110px;"><%=DefineReport.Button.CANCEL.getTxt()%></a></td>
			<td><a href="#" title="登録" class="easyui-linkbutton" tabindex="18" id="<%=DefineReport.Button.UPD.getObj()%>" title="<%=DefineReport.Button.UPD.getTxt()%>" iconCls="icon-save" style="width:110px;"><span class="btnTxt"><%=DefineReport.Button.UPD.getTxt()%></span></a></td>
			<td><a href="#" title="削除" class="easyui-linkbutton" tabindex="19" id="<%=DefineReport.Button.DEL.getObj()%>" title="<%=DefineReport.Button.DEL.getTxt()%>" iconCls="icon-remove" style="width:110px;"><span class="btnTxt"><%=DefineReport.Button.DEL.getTxt()%></span></a></td>
			</tr>
			</table>
		</div>
		<div id="disp_record_info" style="float: right;">
			<span class="labelName" tabindex="-1" style="padding-top: 3px;">
				登録日 <span col="F12" id="<%=DefineReport.Text.ADDDT.getObj()%>"></span>　更新日 <span col="F13" id="<%=DefineReport.Text.UPDDT.getObj()%>"></span>　オペレータ <span col="F11" id="<%=DefineReport.Text.OPERATOR.getObj()%>"></span>
			</span>
			<input type="hidden" col="F14" name="<%=DefineReport.Hidden.UPDDT.getObj()%>" id="<%=DefineReport.Hidden.UPDDT.getObj()%>" />
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
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.TR005.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.win001.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.control.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.events.js?v=<%=prm %>"></script>
</html>