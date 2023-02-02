
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
	String page_suffix = "_winTJ010";
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
<div id="list1" data-options="region:'north',border:false," style="display:none;" >
	<table id = "dTable1"  class="like_datagrid" data-options="rownumbers:true,width:598," style="float:left;">
		<thead class="no_header">
			<tr>
				<th style="max-width: 40px"></th>
				<th style="max-width: 40px"></th>
				<th style="max-width: 40px"></th>
				<th style="max-width: 40px"></th>
				<th style="max-width: 40px"></th>
			</tr>
		</thead>
		<tbody>
			<tr>
				<td class="col_tit" rowspan="1" style="width:80px;">リスト№</td>
				<td class="col_tit" rowspan="1" style="width:202px;">タイトル</td>
				<td class="col_tit" rowspan="1" style="width:62px;">期間</td>
				<td class="col_tit" rowspan="1" style="width:80px;">送信締切日</td>
				<td class="col_tit" colspan="2" style="width:135px;">部門</td>
			</tr>
			<tr>
				<td class="col_lbl_c" style="text-align:left;"><span col="F1"></span></td>
				<td class="col_lbl_c" style="text-align:left;"><span col="F2"></span></td>
				<td class="col_lbl_c" style="text-align:left;"><span col="F3"></span></td>
				<td class="col_lbl_c" style="text-align:left;"><span col="F4"></span></td>
				<td class="col_lbl_c" style="width:15px;text-align:left;"><span col="F5"></span></td>
				<td class="col_lbl_c" style="text-align:center;"><span col="F6"></span></td>
			</tr>
		</tbody>
	</table>
	<tr>
		<td>
			<table   class="like_datagrid" data-options="rownumbers:true,width:410," style="float:left;margin-left:10px;margin-bottom:10px">
			<thead class="no_header">
				<tr>
					<th style="max-width: 40px"></th>
					<th style="max-width: 40px"></th>
				</tr>
			</thead>
			<tbody>
			<tr>
				<td class="col_tit" style="width:64px;">レ売構成比</td>
				<td class="col_num" style="width:64px;"><input class="easyui-numberbox_" tabindex="1"   style="width:52px;" id="txt_kouseihi1" check='<%=DefineReport.InpText.KOUSEIHI.getMaxlenTag()%>' data-options="min:0,max:100">%</td>
			</tr>
			<tr>
				<td class="col_tit">レ荒構成比</td>
				<td class="col_num" style="width:64px;"><input class="easyui-numberbox_" tabindex="2"   style="width:52px;" id="txt_kouseihi2" check='<%=DefineReport.InpText.KOUSEIHI.getMaxlenTag()%>' data-options="min:0,max:100">%</td>
			</tr>
			<tr>
				<td class="col_tit">荒利率予算</td>
				<td class="col_num" style="width:64px;"><input class="easyui-numberbox_" tabindex="3"   style="width:52px;" id="txt_kouseihi3" check='<%=DefineReport.InpText.KOUSEIHI.getMaxlenTag()%>' data-options="min:0,max:100">%</td>
			</tr>
			</table>
	<div style="line-height: 1.1em;float:left;">
		<font size="1">
			　　週間発注可・追加不可　＆<br>
			　　週間発注可・追加可　　！<br>
			　　訂正不可　　　　　　　＃<br>
			　　週間発注不可・追加可　＠
		</font>
	</div>
	<div style = "height:60px;">
		<br>
	</div>
	<p style="clear:both;"></p>
		</td>
	</tr>
	<tr>
			<td>
				<table  class="like_datagrid" data-options="rownumbers:true,width:410," style="float:left;display:none;">
				<thead class="no_header">
					<tr>
						<th style="max-width: 40px"></th>
						<th style="max-width: 40px"></th>
						<th style="max-width: 40px"></th>
						<th style="max-width: 40px"></th>
						<th style="max-width: 40px"></th>
						<th style="max-width: 40px"></th>
						<th style="max-width: 40px"></th>
						<th style="max-width: 40px"></th>
						<th style="max-width: 40px"></th>
						<th style="max-width: 40px"></th>
					</tr>
				</thead>
				<tbody>
				<tr>
					<td class="col_tit" rowspan="3" style="width:70px;text-align:left;vertical-align:top;">
					<div style="width:70px;">アイテム</div>
					<div></div><br>
					<div></div><br>
					<div>コメント</div>
					</td>
					<td class="col_tit" rowspan="3" style="width:51px;text-align:left;vertical-align:top;">
					<div style="width:51px;"></div><br>
					<div>特売入数</div>
					<div>標準入数</div>
					<div></div>
					</td>
					<td class="col_tit" rowspan="3" style="width:51px;text-align:left;vertical-align:top;">
					<div style="width:51px;"></div><br>
					<div>事前原価</div>
					<div>追加原価</div>
					<div>標準原価</div>
					</td>
					<td class="col_tit" rowspan="3" style="width:61px;text-align:left;vertical-align:top;">
					<div style="width:61px;"></div><br>
					<div>特売総売</div>
					<div>特売本売</div>
					<div>標準総売</div>
					</td>
					<td class="col_tit" rowspan="3" style="width:61px;text-align:left;vertical-align:top;">
					<div style="width:61px;"></div><br>
					<div></div><br>
					<div>平均実装</div>
					<div></div>
					</td>

					<%-- <td class="col_tit" style="text-align:left"><div style="width:72px;">部門予算</div></td>
					<td class="col_num" ><input class="easyui-numberbox_" tabindex="4"   style="width:81px;" id="txt_yosan1" check='<%=DefineReport.InpText.BMNYSANAM.getMaxlenTag()%>'></td>
					<td class="col_num" ><input class="easyui-numberbox_" tabindex="5"   style="width:81px;" id="txt_yosan2" check='<%=DefineReport.InpText.BMNYSANAM.getMaxlenTag()%>'></td>
					<td class="col_num" ><input class="easyui-numberbox_" tabindex="6"   style="width:81px;" id="txt_yosan3" check='<%=DefineReport.InpText.BMNYSANAM.getMaxlenTag()%>'></td>
					<td class="col_num" ><input class="easyui-numberbox_" tabindex="7"   style="width:81px;" id="txt_yosan4" check='<%=DefineReport.InpText.BMNYSANAM.getMaxlenTag()%>'></td>
					<td class="col_num" ><input class="easyui-numberbox_" tabindex="8"   style="width:81px;" id="txt_yosan5" check='<%=DefineReport.InpText.BMNYSANAM.getMaxlenTag()%>'></td>
					<td class="col_num" ><input class="easyui-numberbox_" tabindex="9"   style="width:81px;" id="txt_yosan6" check='<%=DefineReport.InpText.BMNYSANAM.getMaxlenTag()%>'></td>
					<td class="col_num" ><input class="easyui-numberbox_" tabindex="10"   style="width:81px;" id="txt_yosan7" check='<%=DefineReport.InpText.BMNYSANAM.getMaxlenTag()%>'></td>
					<td class="col_num" ><input class="easyui-numberbox_" tabindex="11"   style="width:81px;" id="txt_yosan8" check='<%=DefineReport.InpText.BMNYSANAM.getMaxlenTag()%>'></td>
					<td class="col_num" ><input class="easyui-numberbox_" tabindex="12"   style="width:81px;" id="txt_yosan9" check='<%=DefineReport.InpText.BMNYSANAM.getMaxlenTag()%>'></td>
					<td class="col_num" ><input class="easyui-numberbox_" tabindex="13"   style="width:81px;" id="txt_yosan10" check='<%=DefineReport.InpText.BMNYSANAM.getMaxlenTag()%>'></td>
				 --%>
				 	<td class="col_tit" style="text-align:left"><div style="width:72px;">日付</div></td>
					<td class="col_lbl_c" style="text-align:center;width:81px;"><span id="txt_day1" ></span></td>
					<td class="col_lbl_c" style="text-align:center;width:81px;"><span id="txt_day2" ></span></td>
					<td class="col_lbl_c" style="text-align:center;width:81px;"><span id="txt_day3" ></span></td>
					<td class="col_lbl_c" style="text-align:center;width:81px;"><span id="txt_day4" ></span></td>
					<td class="col_lbl_c" style="text-align:center;width:81px;"><span id="txt_day5" ></span></td>
					<td class="col_lbl_c" style="text-align:center;width:81px;"><span id="txt_day6" ></span></td>
					<td class="col_lbl_c" style="text-align:center;width:81px;"><span id="txt_day7" ></span></td>
					<td class="col_lbl_c" style="text-align:center;width:81px;"><span id="txt_day8" ></span></td>
					<td class="col_lbl_c" style="text-align:center;width:81px;"><span id="txt_day9" ></span></td>
					<td class="col_lbl_c" style="text-align:center;width:81px;"><span id="txt_day10" ></span></td>
				</tr>
				<tr>
					<td class="col_tit" style="text-align:left">曜日</td>
					<td class="col_lbl_c" style="text-align:center"><span id="txt_youbi1"  ></span></td>
					<td class="col_lbl_c" style="text-align:center"><span id="txt_youbi2"  ></span></td>
					<td class="col_lbl_c" style="text-align:center"><span id="txt_youbi3"  ></span></td>
					<td class="col_lbl_c" style="text-align:center"><span id="txt_youbi4"  ></span></td>
					<td class="col_lbl_c" style="text-align:center"><span id="txt_youbi5"  ></span></td>
					<td class="col_lbl_c" style="text-align:center"><span id="txt_youbi6"  ></span></td>
					<td class="col_lbl_c" style="text-align:center"><span id="txt_youbi7"  ></span></td>
					<td class="col_lbl_c" style="text-align:center"><span id="txt_youbi8"  ></span></td>
					<td class="col_lbl_c" style="text-align:center"><span id="txt_youbi9"  ></span></td>
					<td class="col_lbl_c" style="text-align:center"><span id="txt_youbi10"  ></span></td>
				</tr>
				</table>
			</td>
		</tr>
</div>

</form>
<form id="gf" class="e_grid">

<div id="list2" data-options="region:'center',border:false" style="display:none;">

	<!-- EasyUI方式 -->
	<div tabindex="14" id="gridholder" class="easyui-datagrid placeFace" ></div>
	<div class="ref_editor" style="display: none;">
		<input class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.SHNCD.getObj()%>"  check='<%=DefineReport.InpText.SHNCD.getMaxlenTag()%>'>
		<input class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.HTSU.getObj()%>1"  check='<%=DefineReport.InpText.HTSU.getMaxlenTag()%>' data-options="min:0">
		<input class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.HTSU.getObj()%>2"  check='<%=DefineReport.InpText.HTSU.getMaxlenTag()%>' data-options="min:0">
		<input class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.HTSU.getObj()%>3"  check='<%=DefineReport.InpText.HTSU.getMaxlenTag()%>' data-options="min:0">
		<input class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.HTSU.getObj()%>4"  check='<%=DefineReport.InpText.HTSU.getMaxlenTag()%>' data-options="min:0">
		<input class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.HTSU.getObj()%>5"  check='<%=DefineReport.InpText.HTSU.getMaxlenTag()%>' data-options="min:0">
		<input class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.HTSU.getObj()%>6"  check='<%=DefineReport.InpText.HTSU.getMaxlenTag()%>' data-options="min:0">
		<input class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.HTSU.getObj()%>7"  check='<%=DefineReport.InpText.HTSU.getMaxlenTag()%>' data-options="min:0">
		<input class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.HTSU.getObj()%>8"  check='<%=DefineReport.InpText.HTSU.getMaxlenTag()%>' data-options="min:0">
		<input class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.HTSU.getObj()%>9"  check='<%=DefineReport.InpText.HTSU.getMaxlenTag()%>' data-options="min:0">
		<input class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.HTSU.getObj()%>10" check='<%=DefineReport.InpText.HTSU.getMaxlenTag()%>' data-options="min:0">
	</div>
</div>
</form>
<!-- <form id="gf" class="e_grid">
<div id="list" data-options="region:'center',border:false" style="display:none;">

	EasyUI方式
	<div tabindex="6" id="gridholder" class="easyui-datagrid placeFace" ></div>
</div>
</form> -->
<div id="buttons" data-options="region:'south',border:false" style="display:none;height:30px;padding:0 30px;text-align: center;">
	<div class="btn" style="float: left;">
		<table class="t-layout3">
		<tr>
		<th style="min-width:135px;"></th><th style="min-width:10px;"></th>
		<th style="min-width:135px;"></th>
		<th style="min-width:135px;"></th>
		<th style="min-width:135px;"></th>
		<th style="min-width:135px;"></th>
		<tr>
		<td><a href="#" title="戻る" id="<%=DefineReport.Button.BACK.getObj()%>" class="easyui-linkbutton" tabindex="15" iconCls="icon-undo" style="width:110px;"><span class="btnTxt">戻る</span></a></td>
		<td></td>
		<td><a href="#" class="easyui-linkbutton" tabindex="16" id="<%=DefineReport.Button.CLEAR.getObj()%>" title="<%=DefineReport.Button.CLEAR.getTxt()%>" iconCls="icon-cancel" style="width:110px;">入力クリア</a></td>
		<td><a href="#" class="easyui-linkbutton" tabindex="17" id="<%=DefineReport.Button.KEIKAKU.getObj()%>" title="<%=DefineReport.Button.KEIKAKU.getTxt()%>" iconCls="icon-ok" style="width:110px;">計画計表示</a></td>
		<td><a href="#" class="easyui-linkbutton" tabindex="18" id="<%=DefineReport.Button.SAIKEISAN.getObj()%>" title="<%=DefineReport.Button.SAIKEISAN.getTxt()%>" iconCls="icon-ok" style="width:110px;">再計算</a></td>
		<td><a href="#" class="easyui-linkbutton" tabindex="19" id="<%=DefineReport.Button.UPD.getObj()%>" title="<%=DefineReport.Button.UPD.getTxt()%>" iconCls="icon-save" style="width:110px;"><span class="btnTxt"><%=DefineReport.Button.UPD.getTxt()%></span></a></td>
		<td><a href="#" class="easyui-linkbutton" tabindex="20" id="<%=DefineReport.Button.ADDLINE.getObj()%>" title="<%=DefineReport.Button.ADDLINE.getTxt()%>" iconCls="icon-ok" style="width:110px;">行追加</a></td>
		</tr>
		</table>
	</div>
	<div id="disp_record_info" style="float: right;">
		<span class="labelName" tabindex="-1" style="padding-top: 3px;">
				登録日 <span col="F7" id="<%=DefineReport.Text.ADDDT.getObj()%>"></span>　更新日 <span col="F8" id="<%=DefineReport.Text.UPDDT.getObj()%>"></span>　オペレータ <span col="F9" id="<%=DefineReport.Text.OPERATOR.getObj()%>"></span>
		</span>
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
<jsp:include page="Out_ReportwinTJ010.jsp" flush="true" />
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
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.TJ009.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.winTJ010.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.control.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.events.js?v=<%=prm %>"></script>
</html>