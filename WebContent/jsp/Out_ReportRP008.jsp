
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
<div class="lbl_box" style="width:600px;">
<table style="float:left;">
	<tr>
		<td>
			<table>
			<tr>
				<td>部門コード</td>
				<td><input class="easyui-numberbox" tabindex="1" col="F1" id="<%=DefineReport.InpText.BMNCD.getObj()%>" check='<%=DefineReport.InpText.BMNCD.getMaxlenTag()%>' data-options="required:true" style="width:60px;" value=""></td>
			</tr>
			<tr>
				<td>ランクNo.</td>
				<td><input class="easyui-numberbox" tabindex="2" col="F2" id="<%=DefineReport.InpText.RANKNO.getObj()%>" check='<%=DefineReport.InpText.RANKNO.getMaxlenTag()%>' data-options="required:true" style="width:60px;" value=""></td>
			</tr>
			</table>
		</td>
	</tr>
	<tr>
		<td>
		<fieldset style="width:440px;height:165px;">
			<table  class="t-layout1">
			<tr>
				<td>
				<table>
				<tr>
					<td style = "height:26px">
						<label class="rad_lbl"><input type="radio" col="F3" tabindex="3" name="<%=DefineReport.Radio.PTNNOKBN.getObj()%>" value="1" checked="checked"/>数量パターンNo.　</label>
						<input class="easyui-numberbox" col="F4" tabindex="4" id="<%=DefineReport.InpText.SRYPTNNO.getObj()%>" check='<%=DefineReport.InpText.SRYPTNNO.getMaxlenTag()%>' style="width:60px;" value="">
					</td>
				</tr>
				<tr>
					<td>ランクと数量パターンの組み合わせで数量計算</td>
				</tr>
				</table>
				</td>
			</tr>
			<tr>
				<td>
				<table>
				<tr>
					<td style = "height:26px">
						<label class="rad_lbl"><input type="radio" col="F3" tabindex="5" name="<%=DefineReport.Radio.PTNNOKBN.getObj()%>" value="2" style="vertical-align:middle;"/>通常率パターンNo.</label>
						<input class="easyui-numberbox" col="F5" tabindex="6" id="<%=DefineReport.InpText.RTPTNNO.getObj()%>" check='<%=DefineReport.InpText.RTPTNNO.getMaxlenTag()%>' style="width:60px;" value="">
						総数量
						<input class="easyui-numberbox" col="F6" tabindex="7" id="<%=DefineReport.InpText.RTSOUSU.getObj()%>" check='<%=DefineReport.InpText.RTSOUSU.getMaxlenTag()%>' style="width:120px;text-align:right;" data-options="min:0" value="">
					</td>
				</tr>
				<tr>
					<td>ランクと通常率パターンの組み合わせで総数量を比例分配</td>
				</tr>
				</table>
				</td>
			</tr>
			<tr>
				<td>
				<table>
				<tr>
					<td style = "height:26px">
						<label class="rad_lbl"><input type="radio" col="F3" tabindex="8" name="<%=DefineReport.Radio.PTNNOKBN.getObj()%>" value="3" style="vertical-align:middle;"/>実績率パターンNo.</label>
						<input class="easyui-numberbox" col="F7" tabindex="9" id="<%=DefineReport.InpText.JRTPTNNO.getObj()%>" check='<%=DefineReport.InpText.JRTPTNNO.getMaxlenTag()%>' style="width:120px;" value="">
						総数量
						<input class="easyui-numberbox" col="F8" tabindex="10" id="<%=DefineReport.InpText.JRTSOUSU.getObj()%>" check='<%=DefineReport.InpText.JRTSOUSU.getMaxlenTag()%>' style="width:120px;text-align:right;" data-options="min:0" value="">
					</td>
				</tr>
				<tr>
					<td>ランクと実績率パターンの組み合わせで総数量を比例分配</td>
				</tr>
				</table>
				</td>
			</tr>
			</table>
		</fieldset>
		</td>
	<tr/>
	<tr><td>※ 部門コード、ランクNo. は必須</td></tr>
	<tr><td>※ 数量パターンNo.、通常率パターンNo.、実積率パターンNo. のどちらかを入力する。</td></tr>
	</table>
	</div>
</div>

</form>






<%--
	<tr>
		<td>
			<input class="easyui-numberbox" tabindex="1" col="F1" id="<%=DefineReport.InpText.BMNCD.getObj()%>" check='<%=DefineReport.InpText.BMNCD.getMaxlenTag()%>' data-options="label:'部門コード',labelWidth:70,required:true" style="width:120px;" value="">
		</td>
	</tr>
	<tr>
		<td>
			<input class="easyui-numberbox" tabindex="2" col="F2" id="<%=DefineReport.InpText.RANKNO.getObj()%>" check='<%=DefineReport.InpText.RANKNO.getMaxlenTag()%>' data-options="label:'ランクNo.',labelWidth:70,required:true" style="width:120px;" value="">
		</td>
	</tr>
	<tr>
		<td>
		<fieldset style="width:500px;height:160px;">
			<div class="inp_box">
				<label class="rad_lbl"><input type="radio" col="F3" tabindex="3" name="<%=DefineReport.Radio.PTNNOKBN.getObj()%>" value="1" style="vertical-align:middle;" checked="checked"/>数量パターンNo.</label>
				<input class="easyui-numberbox" col="F4" tabindex="4" id="<%=DefineReport.InpText.SRYPTNNO.getObj()%>" check='<%=DefineReport.InpText.SRYPTNNO.getMaxlenTag()%>' style="width:60px;" value=""><br>
				<p>ランクと数量パターンの組み合わせで数量計算</p><br>
				<label class="rad_lbl"><input type="radio" col="F3" tabindex="5" name="<%=DefineReport.Radio.PTNNOKBN.getObj()%>" value="2" style="vertical-align:middle;"/>通常率パターンNo.</label>
				<input class="easyui-numberbox" col="F5" tabindex="6" id="<%=DefineReport.InpText.RTPTNNO.getObj()%>" check='<%=DefineReport.InpText.RTPTNNO.getMaxlenTag()%>' style="width:60px;" value="">
				<input class="easyui-numberbox" col="F6" tabindex="7" id="<%=DefineReport.InpText.RTSOUSU.getObj()%>" check='<%=DefineReport.InpText.RTSOUSU.getMaxlenTag()%>' data-options="label:'総数量',labelWidth:45" style="width:120px;" value=""><br>
				<p>ランクと通常率パターンの組み合わせで総数量を比例分配</p><br>
				<label class="rad_lbl"><input type="radio" col="F3" tabindex="8" name="<%=DefineReport.Radio.PTNNOKBN.getObj()%>" value="3" style="vertical-align:middle;"/>実績率パターンNo.</label>
				<input class="easyui-textbox" col="F7" tabindex="9" id="<%=DefineReport.InpText.JRTPTNNO.getObj()%>" check='<%=DefineReport.InpText.JRTPTNNO.getMaxlenTag()%>' style="width:120px;" value="">
				<input class="easyui-numberbox" col="F8" tabindex="10" id="<%=DefineReport.InpText.JRTSOUSU.getObj()%>" check='<%=DefineReport.InpText.JRTSOUSU.getMaxlenTag()%>' data-options="label:'総数量',labelWidth:45" style="width:120px;" value=""><br>
				<p>ランクと実績率パターンの組み合わせで総数量を比例分配</p><br>
			</div>
		</fieldset>
		</td>
	<tr/>
	<tr></tr>
	<tr><td>※ 部門コード、ランクNo. は必須</td></tr>
	<tr><td>※ 数量パターンNo.、通常率パターンNo.、実積率パターンNo. のどちらかを入力する。</tr>
</table>
</div>
</div>
</form>
 --%>
<div id="buttons" data-options="region:'south',border:false" style="display:none;height:30px;padding:0 20px;">
	<div class="btn" style="float: left;">
		<table class="t-layout3">
			<tr>
				<td><a href="#" title="戻る" id="<%=DefineReport.Button.BACK.getObj()%>" class="easyui-linkbutton" tabindex="11" iconCls="icon-undo" style="width:125px;"><span class="btnTxt">戻る</span></a></td>
				<td><a href="#" title="キャンセル" id="<%=DefineReport.Button.CANCEL.getObj()%>" class="easyui-linkbutton" tabindex="12" iconCls="icon-cancel" style="width:125px;"><span class="btnTxt">キャンセル</span></a></td>
				<td><a href="#" title="店別数量展開" id="<%=DefineReport.Button.TENBETUSU.getObj()%>" class="easyui-linkbutton" tabindex="13" iconCls="icon-ok" style="width:125px;"><span class="btnTxt2" style="width:90px">店別数量展開</span></a></td>
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
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.RP008.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.control.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.events.js?v=<%=prm %>"></script>
</html>