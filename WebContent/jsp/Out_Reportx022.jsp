
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
			<td style="min-width:400px;min-height:25px;">
				<input class="easyui-numberbox" tabindex="1" col="F1" id="<%=DefineReport.Text.SEL_BMNCD.getObj()%>" check='<%=DefineReport.InpText.BMNCD.getMaxlenTag()%>' data-options="label:'部門コード',labelWidth:150,required:true" style="width:200px;">
			</td>
			<td>
				<input class="easyui-numberbox" tabindex="12" col="F12" id="<%=DefineReport.InpText.CORPBMNCD.getObj()%>" check='<%=DefineReport.InpText.CORPBMNCD.getMaxlenTag()%>' data-options="label:'会社部門',labelWidth:150" style="width:270px;">
			</td>
		</tr>
		<tr>
			<td style="min-height:25px;">
				<input class="easyui-combobox" tabindex="2" col="F2" id="<%=DefineReport.MeisyoSelect.KBN501.getObj()%>" data-options="label:'部門区分',labelWidth:150" style="width:270px;">
			</td>
			<td>
				<input class="easyui-numberbox" tabindex="13" col="F13" id="<%=DefineReport.InpText.URIBMNCD.getObj()%>" check='<%=DefineReport.InpText.URIBMNCD.getMaxlenTag()%>' data-options="label:'売上計上部門',labelWidth:150" style="width:270px;">
			</td>
		</tr>
		<tr>
			<td style="min-height:25px;">
 				<input class="easyui-textbox" tabindex="3" col="F3" id="<%=DefineReport.InpText.BMKAN.getObj()%>" check='<%=DefineReport.InpText.BMKAN.getMaxlenTag()%>' data-options="label:'漢字名',labelWidth:150," style="width:320px;">
 			</td>
			<td>
				<input class="easyui-numberbox" tabindex="14" col="F14" id="<%=DefineReport.InpText.HOGANBMNCD.getObj()%>" check='<%=DefineReport.InpText.HOGANBMNCD.getMaxlenTag()%>' data-options="label:'包含部門',labelWidth:150" style="width:270px;">
			</td>
		</tr>
		<tr>
			<td style="min-height:25px;">
 				<input class="easyui-textbox" tabindex="4" col="F4" id="<%=DefineReport.InpText.BMNAN.getObj()%>" check='<%=DefineReport.InpText.BMNAN.getMaxlenTag()%>' data-options="label:'カナ名',labelWidth:150," style="width:320px;">
 			</td>
			<td>
				<input class="easyui-numberbox" tabindex="15" col="F15" id="<%=DefineReport.InpText.JYOGENAM.getObj()%>" check='<%=DefineReport.InpText.JYOGENAM.getMaxlenTag()%>' data-options="label:'上限金額',labelWidth:150" style="width:270px;text-align: right;">
			</td>
		</tr>
		<tr>
			<td style="min-height:25px;">
				<input class="easyui-combobox" tabindex="5" col="F5" id="<%=DefineReport.MeisyoSelect.KBN120.getObj()%>" data-options="label:'税区分',labelWidth:150" style="width:270px;">
			</td>
			<td>
				<input class="easyui-numberbox" tabindex="16" col="F16" id="<%=DefineReport.InpText.JYOGENSU.getObj()%>" check='<%=DefineReport.InpText.JYOGENSU.getMaxlenTag()%>' data-options="label:'上限数量',labelWidth:150" style="width:270px;text-align: right;">
			</td>
		</tr>
		<tr>
			<td style="min-height:25px;">
				<input class="easyui-numberbox" tabindex="6" col="F6" id="<%=DefineReport.InpText.ZEIRTHENKODT.getObj()%>" check='<%=DefineReport.InpText.ZEIRTHENKODT.getMaxlenTag()%>'data-options="label:'税率変更日',labelWidth:150,prompt:'__/__/__'" style="width:270px;">
			</td>
			<td>
				<input class="easyui-combobox" tabindex="17" col="F17" id="<%=DefineReport.MeisyoSelect.KBN505.getObj()%>" data-options="label:'棚卸タイミング',labelWidth:150" style="width:270px;">
			</td>
		</tr>
		<tr>
			<td style="min-height:25px;">
				<input class="easyui-combobox" tabindex="7" col="F7" id="<%=DefineReport.Select.ZEIRTKBN_OLD.getObj()%>" data-options="label:'旧税率区分',labelWidth:150" style="width:270px;">
			</td>
			<td>
				<input class="easyui-combobox" tabindex="18" col="F18" id="<%=DefineReport.MeisyoSelect.KBN506.getObj()%>" data-options="label:'ＰＯＳ売変対象区分',labelWidth:150" style="width:270px;">
			</td>
		</tr>
		<tr>
			 <td style="min-height:25px;">
				<input class="easyui-combobox" tabindex="8" col="F8" id="<%=DefineReport.Select.ZEIRTKBN.getObj()%>" data-options="label:'税率区分',labelWidth:150" style="width:270px;">
			</td>
			<td>
				<input class="easyui-combobox" tabindex="19" col="F19" id="<%=DefineReport.MeisyoSelect.KBN507.getObj()%>" data-options="label:'経費対象区分',labelWidth:150" style="width:270px;">
			</td>
		</tr>
		<tr>
			 <td style="min-height:25px;">
				<input class="easyui-combobox" tabindex="9" col="F9" id="<%=DefineReport.MeisyoSelect.KBN503.getObj()%>" data-options="label:'オーダーブック出力区分',labelWidth:150" style="width:270px;">
			</td>
			<td>
				<input class="easyui-combobox" tabindex="20" col="F20" id="<%=DefineReport.MeisyoSelect.KBN508.getObj()%>" data-options="label:'単品管理区分',labelWidth:150" style="width:270px;">
			</td>
		</tr>
		<tr>
			<td style="min-height:25px;">
				<input class="easyui-combobox" tabindex="10" col="F10" id="<%=DefineReport.MeisyoSelect.KBN504.getObj()%>" data-options="label:'評価方法区分',labelWidth:150" style="width:270px;">
			</td>
			<td>
				<input class="easyui-numberbox" tabindex="21" col="F21" id="<%=DefineReport.InpText.DELKIJYUNSU.getObj()%>" check='<%=DefineReport.InpText.DELKIJYUNSU.getMaxlenTag()%>' data-options="label:'削除基準日数',labelWidth:150" style="width:200px;text-align: right;">
			</td>
		</tr>
		<tr>
			<td style="min-height:25px;">
				<input class="easyui-numberbox" tabindex="11" col="F11" id="<%=DefineReport.InpText.GENKART.getObj()%>" check='<%=DefineReport.InpText.GENKART.getMaxlenTag()%>' data-options="label:'原価率',labelWidth:150" style="width:270px;text-align: right;">
			</td>
			<td>
				<input class="easyui-combobox" tabindex="22" col="F22" id="<%=DefineReport.MeisyoSelect.KBN509.getObj()%>" data-options="label:'値引除外',labelWidth:150" style="width:270px;">
			</td>
		</tr>
		<tr>
			<td style="min-height:25px;">
			</td>
			<td>
				<input class="easyui-combobox" tabindex="23" col="F23" id="<%=DefineReport.MeisyoSelect.KBN510.getObj()%>" data-options="label:'商品/非商品識別',labelWidth:150" style="width:270px;">
			</td>
		</tr>
		<tr>
			<td style="min-height:25px;">
			</td>
			<td>
				<input class="easyui-combobox" tabindex="24" col="F24" id="<%=DefineReport.MeisyoSelect.KBN511.getObj()%>" data-options="label:'販売制限',labelWidth:150" style="width:270px;">
			</td>
		</tr>
		<tr>
			<td style="min-height:25px;">
			</td>
			<td>
				<input class="easyui-numberbox" tabindex="25" col="F25" id="<%=DefineReport.InpText.URIKETAKBN.getObj()%>" check='<%=DefineReport.InpText.URIKETAKBN.getMaxlenTag()%>' data-options="label:'売上金額最大桁数',labelWidth:150" style="width:200px;text-align: right;">
 			</td>
		</tr>
	</table>
</div>
</form>
<div id="buttons" data-options="region:'south',border:false" style="display:none;">
	<div class="btn" style="float: left;">
		<table class="t-layout3">
		<tr>
		<th style="min-width:135px;"></th><th style="min-width:10px;"></th>
		<th style="min-width:135px;"></th>
		<th style="min-width:135px;"></th>
		<th style="min-width:135px;"></th>
		</tr>
		<tr>
		<td><a href="#" title="戻る" id="<%=DefineReport.Button.BACK.getObj()%>" class="easyui-linkbutton" tabindex="26" iconCls="icon-undo" style="width:125px;"><span class="btnTxt">戻る</span></a></td>
		<td></td>
		<td><a href="#" class="easyui-linkbutton" tabindex="27" id="<%=DefineReport.Button.CANCEL.getObj()%>" title="<%=DefineReport.Button.CANCEL.getTxt()%>" iconCls="icon-cancel" style="width:125px;"><span><%=DefineReport.Button.CANCEL.getTxt()%></span></a></td>
		<td><a href="#" class="easyui-linkbutton" tabindex="28" id="<%=DefineReport.Button.UPD.getObj()%>" title="<%=DefineReport.Button.UPD.getTxt()%>" iconCls="icon-save" style="width:125px;"><span class="btnTxt"><%=DefineReport.Button.UPD.getTxt()%></span></a></td>
		<td><a href="#" class="easyui-linkbutton" tabindex="29" id="<%=DefineReport.Button.DEL.getObj()%>" title="<%=DefineReport.Button.DEL.getTxt()%>" iconCls="icon-remove" style="width:125px;"><span class="btnTxt"><%=DefineReport.Button.DEL.getTxt()%></span></a></td>
		</tr>
		</table>
	</div>
	<div id="disp_record_info" style="float: right;">
		<span class="labelName" tabindex="-1" style="padding-top: 3px;">
				登録日 <span col="F26" id="<%=DefineReport.Text.ADDDT.getObj()%>"></span>　更新日 <span col="F27" id="<%=DefineReport.Text.UPDDT.getObj()%>"></span>　オペレータ <span col="F28" id="<%=DefineReport.Text.OPERATOR.getObj()%>"></span>
		</span>
		<input type="hidden" col="F29" name="<%=DefineReport.Hidden.UPDDT.getObj()%>" id="<%=DefineReport.Hidden.UPDDT.getObj()%>" />
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
<%-- 	<jsp:include page="Out_Reportwin002.jsp" flush="true" /> --%>
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
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.x022.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.control.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.events.js?v=<%=prm %>"></script>
</html>