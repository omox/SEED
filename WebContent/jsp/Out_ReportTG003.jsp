
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
	<table class="t-layout1" style="vertical-align: top;">
	<tr>
	<td>
		<TABLE class="dataTable" cellspacing="0" cellpadding="0" style="display:inline;">
		<TBODY>
		<TR align="left">
			<TD class="labelCell" style="WIDTH: 60px;">催しコード</TD>
			<TD class="col_lbl" style="WIDTH: 95px;"><span col="F1"></span></TD>
			<TD class="labelCell" style="WIDTH: 60px;">催し名称</TD>
			<TD class="col_lbl"   style="WIDTH:300px;"><span col="F2"></span></TD>
			<TD class="labelCell" style="WIDTH: 60px;">催し期間</TD>
			<TD class="col_lbl_c" style="WIDTH:175px;"><span col="F3"></span></TD>
			<td style="border: 0;"></td>
			<TD class="col_lbl_r" style="WIDTH: 75px;"><span col="F4" style="font-weight: bold;color: red;"></span></TD>
		</TR></TBODY></TABLE>
		<input type="hidden" name="<%=DefineReport.Text.SHORIDT.getObj()%>" id="<%=DefineReport.Text.SHORIDT.getObj()%>" />
	</td>
	</tr>
	<tr>
	<td>
		<table class="like_datagrid">
		<thead>
			<tr>
				<th colspan="2">グループ名称</th>
				<th style="width: 60px;">店数</th>
				<th colspan="2">リーダー店</th>
			</tr>
		</thead>
		<tbody>
			<tr>
				<td class="col_txt">
				<input class="easyui-numberbox" tabindex="1" col="F5" id="<%=DefineReport.InpText.TENGPCD_TOK.getObj()%>" check='<%=DefineReport.InpText.TENGPCD_TOK.getMaxlenTag()%>' data-options="required:true" style="width:50px;">
				</td>
				<td class="col_txt"><input class="easyui-textbox" tabindex="2" col="F6" id="<%=DefineReport.InpText.TENGPKN.getObj()%>" check='<%=DefineReport.InpText.TENGPKN.getMaxlenTag()%>' data-options="required:true" style="width:330px;"></td>
				<td class="col_lbl_r"><span col="F9"></span></td>
				<td class="col_txt"><input class="easyui-numberbox" tabindex="3" col="F7" id="<%=DefineReport.InpText.TENCD.getObj()%>leader" check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' data-options="required:true" style="width:50px;"></td>
				<td class="col_lbl" style="width:330px;"><span col="F8" for_inp="<%=DefineReport.InpText.TENCD.getObj()%>leader_F3"></span></td>
			</tr>
		</tbody>
		</table>
	</td>
	</tr>
	</table>
</div>
<div data-options="region:'west',border:false" style="display:none;padding:0 8px;width:475px;">
	<table id="<%=DefineReport.Grid.TENPO.getObj()%>" class="easyui-datagrid" tabindex="4"></table>
	<!-- Editor参照用 -->
	<div class="ref_editor" style="display: none;">
		<!-- ここで未宣言は他で宣言済み -->
		<input class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.TENCD.getObj()%>" check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' data-options="" style="width:100%;"/>
		<input class="easyui-textbox" tabindex="-1" id="<%=DefineReport.InpText.TENKN.getObj()%>" check='<%=DefineReport.InpText.TENKN.getMaxlenTag()%>' for_inp="<%=DefineReport.InpText.TENCD.getObj()%>_F3" data-options="" style="width:100%;"/>
	</div>
</div>
<div data-options="region:'center',border:false" style="display:none;padding:0;">
	<table class="t-layout1" style="vertical-align: top;">
	<tr><th style="min-width:350px;"></th></tr>
	<tr>
	<td style="vertical-align: top;">
		<table class="like_datagrid">
		<thead>
			<tr>
				<th style="width: 50px;">売価選択<br>（一括）</th>
				<th style="width: 50px;">売価選択<br>（商品別）</th>
				<th style="width: 50px;">売価差替</th>
				<th style="width: 50px;">売価商品<br>選択</th>
				<th style="width: 50px;">不参加</th>
			</tr>
		</thead>
		<tbody>
			<tr>
				<td class="col_chk"><label class="rad_lbl"><input type="radio" tabindex="5" col="F10" name="<%=DefineReport.Radio.QASYUKBN.getObj()%>" value="<%=DefineReport.ValKbn10611.VAL1.getVal()%>"/></label></td>
				<td class="col_chk"><label class="rad_lbl"><input type="radio" tabindex="6" col="F10" name="<%=DefineReport.Radio.QASYUKBN.getObj()%>" value="<%=DefineReport.ValKbn10611.VAL2.getVal()%>"/></label></td>
				<td class="col_chk"><label class="rad_lbl"><input type="radio" tabindex="-1" col="F10" name="<%=DefineReport.Radio.QASYUKBN.getObj()%>" value="<%=DefineReport.ValKbn10611.VAL3.getVal()%>" disabled="disabled" /></label></td>
				<td class="col_chk"><label class="rad_lbl"><input type="radio" tabindex="8" col="F10" name="<%=DefineReport.Radio.QASYUKBN.getObj()%>" value="<%=DefineReport.ValKbn10611.VAL4.getVal()%>"/></label></td>
				<td class="col_chk"><label class="rad_lbl"><input type="radio" tabindex="9" col="F10" name="<%=DefineReport.Radio.QASYUKBN.getObj()%>" value="<%=DefineReport.ValKbn10611.VAL5.getVal()%>"/></label></td>
			</tr>
		</tbody>
		</table>
	</td>
	</tr>
	<tr>
	<td style="vertical-align: top;">
		<table id="tbl_qacredt" class="t-layout1" style="vertical-align: top;display:none;">
		<tr>
		<td>
			<table class="like_datagrid">
			<thead>
				<tr><th style="width:210px;" colspan="2">アンケート作成日</th></tr>
			</thead>
			<tbody>
				<tr>
					<td class="col_tit">通常グループ</td>
					<td class="col_tit">本強制グループ</td>
				</tr>
				<tr>
					<td class="col_lbl_c"><span col="F11"></span></td>
					<td class="col_txt"><input class="easyui-numberbox" tabindex="10" col="F12" id="<%=DefineReport.InpText.QACREDT.getObj()%>" check='<%=DefineReport.InpText.QACREDT.getMaxlenTag()%>' data-options="prompt:'<%=DefineReport.Label.PROMPT_DTW2.getTxt()%>',required:true" style="width:104px;"></td>
				</tr>
			</tbody>
			</table>
		</td>
		</tr>
		<tr>
		<td>
			<table class="like_datagrid">
			<thead>
				<tr><th style="width:210px;" colspan="2">アンケート再作成日</th></tr>
			</thead>
			<tbody>
				<tr>
					<td class="col_tit">通常グループ</td>
					<td class="col_tit">本強制グループ</td>
				</tr>
				<tr>
					<td class="col_lbl_c"><span col="F13"></span></td>
					<td class="col_txt"><input class="easyui-numberbox" tabindex="11" col="F14" id="<%=DefineReport.InpText.QARCREDT.getObj()%>" check='<%=DefineReport.InpText.QARCREDT.getMaxlenTag()%>' data-options="prompt:'<%=DefineReport.Label.PROMPT_DTW2.getTxt()%>'" style="width:104px;"></td>
				</tr>
			</tbody>
			</table>
		</td>
		</tr>
		</table>
	</td>
	</tr>
	</table>
</div>
</form>
<div id="buttons" data-options="region:'south',border:false" style="display:none;">
	<div class="btn" style="float: left;">
		<table class="t-layout3">
		<tr>
			<th style="min-width:120px;"></th><th style="min-width:10px;"></th>
			<th style="min-width:120px;"></th><th style="min-width:120px;"></th><th style="min-width:120px;"></th>
		</tr>
		<tr>
			<td><a href="#" title="戻る" id="<%=DefineReport.Button.BACK.getObj()%>" class="easyui-linkbutton" tabindex="12" iconCls="icon-undo" style="width:110px;"><span class="btnTxt">戻る</span></a></td>
			<td></td>
			<td><a href="#" title="<%=DefineReport.Button.CANCEL.getTxt()%>" class="easyui-linkbutton" tabindex="13" id="<%=DefineReport.Button.CANCEL.getObj()%>" iconCls="icon-cancel" style="width:110px;"><span><%=DefineReport.Button.CANCEL.getTxt()%></span></a></td>
			<td><a href="#" title="登録" class="easyui-linkbutton" tabindex="14" id="<%=DefineReport.Button.UPD.getObj()%>" iconCls="icon-save" style="width:110px;"><span class="btnTxt">登録</span></a></td>
			<td><a href="#"  title="<%=DefineReport.Button.DEL.getTxt()%>"class="easyui-linkbutton" tabindex="15" id="<%=DefineReport.Button.DEL.getObj()%>" iconCls="icon-remove" style="width:110px;"><span class="btnTxt"><%=DefineReport.Button.DEL.getTxt()%></span></a></td>
		</tr>
		</table>
	</div>
	<div id="disp_record_info" style="float: right;">
		<span class="labelName" style="padding-top: 3px;">
			登録日 <span col="F16" id="<%=DefineReport.Text.ADDDT.getObj()%>"></span>　更新日 <span col="F17" id="<%=DefineReport.Text.UPDDT.getObj()%>"></span>　オペレータ <span col="F15" id="<%=DefineReport.Text.OPERATOR.getObj()%>"></span>
		</span>
		<input type="hidden" col="F18" name="<%=DefineReport.Hidden.UPDDT.getObj()%>" id="<%=DefineReport.Hidden.UPDDT.getObj()%>" />
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
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.TG003.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.control.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.events.js?v=<%=prm %>"></script>
</html>