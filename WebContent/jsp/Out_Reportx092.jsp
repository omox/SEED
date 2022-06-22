
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
<table class="t-layout" style="margin:10px;">

		<tr>
			<td>
				<div class="inp_box"><input class="easyui-numberbox" tabindex="1" col="F1" id="<%=DefineReport.InpText.BMNCD.getObj()%>" check='<%=DefineReport.InpText.BMNCD.getMaxlenTag()%>' data-options="label:'部門',labelWidth:150,required:true" style="width:200px;"></div>
			</td>
			<td rowspan="9" >
			<h4>使用原料</h4>
			<table>
				<tr>
					<td>
						<div style="height:200px;width:660px;">
							<div class="easyui-datagrid" tabindex="20" id="<%=DefineReport.Grid.MSTUGENRYO.getObj()%>" data-options="singleSelect:true,fit:true"></div>
							<div class="ref_editor" style="display: none;">
								<input  type="checkbox" tabindex="-1" id="<%=DefineReport.Checkbox.DEL.getObj()%>_M" value="1" />
								<input class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.SHNCD.getObj()%>_M"  check='<%=DefineReport.InpText.SHNCD.getMaxlenTag()%>' data-options="prompt:'____-____'">
								<input class="easyui-textbox" tabindex="-1" id="<%=DefineReport.InpText.SHNKN.getObj()%>_M" for_inp="<%=DefineReport.InpText.SHNCD.getObj()%>_M_F1" check='<%=DefineReport.InpText.SHNKN.getMaxlenTag()%>'>
								<input class="easyui-textbox" tabindex="-1" id="<%=DefineReport.InpText.NAIKN.getObj()%>_M"  check='<%=DefineReport.InpText.NAIKN.getMaxlenTag()%>'>
								<input class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.GENKA.getObj()%>_M"  check='<%=DefineReport.InpText.GENKA.getMaxlenTag()%>'>
								<input class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.BUDOMARI.getObj()%>_M"  check='<%=DefineReport.InpText.BUDOMARI.getMaxlenTag()%>'>
								<input class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.GENKAKEI.getObj()%>_M"  check='<%=DefineReport.InpText.GENKAKEI.getMaxlenTag()%>'>
								<input class="easyui-textbox" tabindex="-1" id="<%=DefineReport.InpText.SIRKN.getObj()%>_M" for_inp="<%=DefineReport.InpText.SHNCD.getObj()%>_M_F2"  check='<%=DefineReport.InpText.SIRKN.getMaxlenTag()%>'>
							</div>
						</div>
					</td>
				</tr>
			</table>
		</td>
		</tr>
		<tr>
			<td>
			 	<div class="inp_box"><input class="easyui-numberbox" tabindex="2" col="F2" id="<%=DefineReport.InpText.CALLCD.getObj()%>" check='<%=DefineReport.InpText.CALLCD.getMaxlenTag()%>' data-options="label:'呼出コード',labelWidth:150,required:true" style="width:220px;"></div>
			</td>
		</tr>
		<tr>
			<td>
			 	<div class="inp_box">
			 	<input class="easyui-numberbox" tabindex="3" col="F3" id="<%=DefineReport.InpText.SHNCD.getObj()%>" check='<%=DefineReport.InpText.SHNCD.getMaxlenTag()%>' data-options="prompt:'____-____',label:'商品コード',labelWidth:150,required:true" style="width:240px;"for_btn="<%=DefineReport.Button.SHNCD.getObj()%>_VALUE" >
				<a href="#" class="easyui-linkbutton" tabindex="4" id="<%=DefineReport.Button.SHNCD.getObj()%>" title="<%=DefineReport.Button.SHNCD.getTxt()%>" >商品コード検索</a>
				</div>
			</td>
		</tr>
		<tr>
			<td>
			 	<div class="inp_box"><input class="easyui-textbox" tabindex="5" col="F4" id="<%=DefineReport.InpText.SHNKN.getObj()%>" check='<%=DefineReport.InpText.SHNKN.getMaxlenTag()%>' for_inp="<%=DefineReport.InpText.SHNCD.getObj()%>_F1" data-options="label:'商品名(漢字)',labelWidth:150" style="width:400px;" disabled="disabled"></div>
			</td>
		</tr>
		<%-- <tr>
			<td>
			 	<div class="inp_box"><input class="easyui-textbox" tabindex="6" col="F5" id="<%=DefineReport.InpText.SHNMEIJYO.getObj()%>" check='<%=DefineReport.InpText.SHNMEIJYO.getMaxlenTag()%>' data-options="label:'商品名　上段',labelWidth:150" style="width:470px;"></div>
			</td>
		</tr>
		<tr>
			<td>
			 	<div class="inp_box"><input class="easyui-textbox" tabindex="7" col="F6" id="<%=DefineReport.InpText.SHNMEIGE.getObj()%>" check='<%=DefineReport.InpText.SHNMEIGE.getMaxlenTag()%>' data-options="label:'商品名　下段',labelWidth:150" style="width:470px;"></div>
			</td>
		</tr> --%>
		<tr>
			<td>
			 	<div class="inp_box"><input class="easyui-textbox" tabindex="6" col="F5" id="<%=DefineReport.InpText.SHNMEIJYO.getObj()%>" check='<%=DefineReport.InpText.SHNMEIJYO.getMaxlenTag()%>' data-options="label:'ラベル名　上段',labelWidth:150" style="width:470px;"></div>
			</td>
		</tr>
		<tr>
			<td>
			 	<div class="inp_box"><input class="easyui-textbox" tabindex="7" col="F6" id="<%=DefineReport.InpText.SHNMEIGE.getObj()%>" check='<%=DefineReport.InpText.SHNMEIGE.getMaxlenTag()%>' data-options="label:'ラベル名　下段',labelWidth:150" style="width:470px;"></div>
			</td>
		</tr>
		<tr>
			<td>
			 	<div class="inp_box">
			 		<input class="easyui-numberbox" col="F7" id="<%=DefineReport.InpText.DAICD.getObj()%>" check='<%=DefineReport.InpText.DAICD.getMaxlenTag()%>' for_inp="<%=DefineReport.InpText.SHNCD.getObj()%>_F2" data-options="label:'大分類',labelWidth:150" style="width:190px;" disabled="disabled">
			 		<input class="easyui-textbox" col="F8" id="<%=DefineReport.InpText.DAIBRUIKN.getObj()%>" check='<%=DefineReport.InpText.DAIBRUIKN.getMaxlenTag()%>' for_inp="<%=DefineReport.InpText.SHNCD.getObj()%>_F3" style="width:240px;" disabled="disabled">
			 	</div>
			</td>
		</tr>
		<tr>
			<td>
			 	<div class="inp_box">
				 	<input class="easyui-numberbox" col="F9" id="<%=DefineReport.InpText.CHUCD.getObj()%>" check='<%=DefineReport.InpText.CHUCD.getMaxlenTag()%>' for_inp="<%=DefineReport.InpText.SHNCD.getObj()%>_F4" data-options="label:'中分類',labelWidth:150" style="width:190px;" disabled="disabled">
				 	<input class="easyui-textbox" col="F10" id="<%=DefineReport.InpText.CHUBRUIKN.getObj()%>" check='<%=DefineReport.InpText.CHUBRUIKN.getMaxlenTag()%>' for_inp="<%=DefineReport.InpText.SHNCD.getObj()%>_F5" style="width:240px;" disabled="disabled">
			 	</div>
			</td>
		</tr>
		<tr>
			<td>
			 	<div class="inp_box">
			 		<input class="easyui-numberbox" col="F11" id="<%=DefineReport.InpText.SHOCD.getObj()%>" check='<%=DefineReport.InpText.SHOCD.getMaxlenTag()%>' for_inp="<%=DefineReport.InpText.SHNCD.getObj()%>_F6" data-options="label:'小分類',labelWidth:150" style="width:190px;" disabled="disabled">
			 		<input class="easyui-textbox" col="F12" id="<%=DefineReport.InpText.SHOBRUIKN.getObj()%>" check='<%=DefineReport.InpText.SHOBRUIKN.getMaxlenTag()%>' for_inp="<%=DefineReport.InpText.SHNCD.getObj()%>_F7" style="width:240px;" disabled="disabled">
			 	</div>
			</td>
		</tr>
		<tr>
			<td>
			 	<div class="inp_box"><input class="easyui-numberbox" tabindex="8" col="F13" id="<%=DefineReport.InpText.IRISU.getObj()%>" check='<%=DefineReport.InpText.IRISU.getMaxlenTag()%>'for_inp="<%=DefineReport.InpText.SHNCD.getObj()%>_F11" data-options="label:'入数',labelWidth:150" style="width:200px;text-align: right;" disabled="disabled"></div>
			</td>
			<td>
			 	<div class="inp_box"><input class="easyui-textbox" tabindex="15" col="F14" id="<%=DefineReport.InpText.UTRAY.getObj()%>" check='<%=DefineReport.InpText.UTRAY.getMaxlenTag()%>' data-options="label:'使用トレイ',labelWidth:150" style="width:360px;"></div>
			</td>
		</tr>
		<tr>
			<td>
			 	<div class="inp_box"><input class="easyui-combobox" tabindex="9" col="F15" id="<%=DefineReport.MeisyoSelect.KBN430.getObj()%>" data-options="label:'生鮮・加工食品',labelWidth:150,required:true" style="width:270px;"></div>
			</td>
			<td>
			 	<div class="inp_box"><input class="easyui-textbox" tabindex="16" col="F16" id="<%=DefineReport.InpText.KONPOU.getObj()%>" check='<%=DefineReport.InpText.KONPOU.getMaxlenTag()%>' data-options="label:'包装形態',labelWidth:150" style="width:290px;"></div>
			</td>
		</tr>
		<tr>
			<td>
			 	<div class="inp_box"><input class="easyui-combobox" tabindex="10" col="F17" id="<%=DefineReport.MeisyoSelect.KBN121.getObj()%>" data-options="label:'定貫・不定貫区分',labelWidth:150,required:true" style="width:270px;"></div>
			</td>
			<td>
			 	<div class="inp_box"><input class="easyui-numberbox" tabindex="17" col="F18" id="<%=DefineReport.InpText.FUTAI.getObj()%>" check='<%=DefineReport.InpText.FUTAI.getMaxlenTag()%>' data-options="label:'風袋',labelWidth:150" style="width:190px;text-align: right;"></div>
			</td>
		</tr>
		<tr>
			<td>
			 	<div class="inp_box"><input class="easyui-numberbox" tabindex="11" col="F19" id="<%=DefineReport.InpText.URICD.getObj()%>" check='<%=DefineReport.InpText.URICD.getMaxlenTag()%>' data-options="label:'販売コード',labelWidth:150" for_inp="<%=DefineReport.InpText.SHNCD.getObj()%>_F8" style="width:270px;" disabled="disabled"></div>
			</td>
			<td>
			 	<div class="inp_box"><input class="easyui-numberbox" tabindex="18" col="F20" id="<%=DefineReport.InpText.KAGENJRYO.getObj()%>" check='<%=DefineReport.InpText.KAGENJRYO.getMaxlenTag()%>' data-options="label:'下限重量',labelWidth:150" style="width:200px;text-align: right;"></div>
			</td>
		</tr>
		<tr>
			<td>
			 	<div class="inp_box"><input class="easyui-textbox" tabindex="12" col="F21" id="<%=DefineReport.InpText.KIKKN.getObj()%>" check='<%=DefineReport.InpText.KIKKN.getMaxlenTag()%>' data-options="label:'規格',labelWidth:150" style="width:270px;" for_inp="<%=DefineReport.InpText.SHNCD.getObj()%>_F9" disabled="disabled"></div>
			</td>
			<td>
			 	<div class="inp_box"><input class="easyui-numberbox" tabindex="19" col="F22" id="<%=DefineReport.InpText.JYOGENJRYO.getObj()%>" check='<%=DefineReport.InpText.JYOGENJRYO.getMaxlenTag()%>' data-options="label:'上限重量',labelWidth:150" style="width:200px;text-align: right;"></div>
			</td>
		</tr>
		<tr>
			<td>
			 	<div class="inp_box"><input class="easyui-textbox" tabindex="13" col="F23" id="<%=DefineReport.InpText.NAIKN.getObj()%>" check='<%=DefineReport.InpText.NAIKN.getMaxlenTag()%>' data-options="label:'内容量',labelWidth:150" style="width:440px;"></div>
			</td>
		</tr>
		<tr>
			<td>
			 	<div class="inp_box"><input class="easyui-numberbox" tabindex="14" col="F24" id="<%=DefineReport.InpText.ODS_NATSUSU.getObj()%>" check='<%=DefineReport.InpText.ODS_NATSUSU.getMaxlenTag()%>' data-options="label:'消費期限・賞味期限',labelWidth:150" for_inp="<%=DefineReport.InpText.SHNCD.getObj()%>_F10" style="width:210px;text-align: right;" disabled="disabled"></div>
			</td>
		</tr>
</table>


</div>

</form>
<div id="buttons" data-options="region:'south',border:false" style="display:none;height:30px;padding:0 30px;text-align: center;">
	<div class="btn" style="float: left;">
		<table class="t-layout3">
		<tr><th style="min-width:120px;"></th><th style="min-width:120px;"></th><th style="min-width:120px;"></th></tr>
		<tr>
		<td><a href="#" title="戻る" id="<%=DefineReport.Button.BACK.getObj()%>" class="easyui-linkbutton" tabindex="151" iconCls="icon-undo" style="width:110px;"><span class="btnTxt">戻る</span></a></td>
		<td><a href="#" class="easyui-linkbutton" tabindex="152" id="<%=DefineReport.Button.CANCEL.getObj()%>" title="<%=DefineReport.Button.CANCEL.getTxt()%>" iconCls="icon-cancel" style="width:110px;"><%=DefineReport.Button.CANCEL.getTxt()%></a></td>
		<td><a href="#" class="easyui-linkbutton" tabindex="153" id="<%=DefineReport.Button.UPD.getObj()%>" title="<%=DefineReport.Button.UPD.getTxt()%>" iconCls="icon-save" style="width:110px;"><span class="btnTxt"><%=DefineReport.Button.UPD.getTxt()%></span></a></td>
		<td><a href="#" class="easyui-linkbutton" tabindex="154" id="<%=DefineReport.Button.DEL.getObj()%>" title="<%=DefineReport.Button.DEL.getTxt()%>" iconCls="icon-remove" style="width:110px;"><span class="btnTxt"><%=DefineReport.Button.DEL.getTxt()%></span></a></td>
		<td><a href="#" class="easyui-linkbutton" tabindex="155" id="<%=DefineReport.Button.ADDLINE.getObj()%>" title="<%=DefineReport.Button.ADDLINE.getTxt()%>" iconCls="icon-ok" style="width:110px;">行追加</a></td>
		</tr>
		</table>
	</div>
	<div id="disp_record_info" style="float: right;">
		<span class="labelName" tabindex="-1" style="padding-top: 3px;">
				登録日 <span col="F27" id="<%=DefineReport.Text.ADDDT.getObj()%>"></span>　更新日 <span col="F28" id="<%=DefineReport.Text.UPDDT.getObj()%>"></span>　オペレータ <span col="F29" id="<%=DefineReport.Text.OPERATOR.getObj()%>"></span>
		</span>
		<input type="hidden" col="F30" name="<%=DefineReport.Hidden.UPDDT.getObj()%>" id="<%=DefineReport.Hidden.UPDDT.getObj()%>" />

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
 	<jsp:include page="Out_Reportwin006.jsp" flush="true" />
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
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.x092.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.win001.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.win002.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.win006.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.control.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.events.js?v=<%=prm %>"></script>
</html>