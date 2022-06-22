
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
	<table class="t-layout1">
	<tr>
		<td><%=DefineReport.InpText.SHNCD.getTxt()%></td>
		<td><input class="easyui-numberbox" tabindex="1" id="<%=DefineReport.InpText.SHNCD.getObj()%>" check='<%=DefineReport.InpText.SHNCD.getMaxlenTag()%>' data-options="prompt:'____-____'" style="width:80px;"></td>
		<td><%=DefineReport.InpText.JANCD.getTxt()%></td>
		<td><input class="easyui-numberbox" tabindex="2" id="<%=DefineReport.InpText.SRCCD.getObj()%>" check='<%=DefineReport.InpText.SRCCD.getMaxlenTag()%>' style="width:120px;"></td>
		<td class="col_btn" rowspan="4">
		<a href="#" title="検索" id="<%=DefineReport.Button.SEARCH.getObj()%>" class="easyui-linkbutton" iconCls="icon-search" style="visibility:hidden"><span class="btnTxt">検索</span></a>
	</td>
	</tr>
	</table>
	<hr />
	<table>
	<tr>
		<td style="height:89px;padding:0 8px 2px 0;">
			<table id="dg2" class="like_datagrid">
			<thead class="no_header">
				<tr>
					<th style="max-width: 80px"></th>
					<th ></th>
					<th style="max-width: 500px"></th>
					<th style="max-width: 350px"></th>
				</tr>
			</thead>
			<tbody>
				<tr>
					<td class="col_tit">商品コード</td>
					<td class="col_tit"></td>
					<td class="col_tit">商品名</td>
					<td class="col_tit">規格名</td>
				</tr>
				<tr>
					<td class="col_txt"><input class="easyui-textbox" tabindex="3" col="F1" id="<%=DefineReport.InpText.DUMMYCD.getObj()%>" style="width:80px;"></td>
					<td class="col_txt">(漢字)</td>
					<td class="col_txt"><input class="easyui-textbox" tabindex="4" col="F2" id="<%=DefineReport.InpText.SHNKN.getObj()%>" check='<%=DefineReport.InpText.SHNKN.getMaxlenTag()%>' style="width:500px;"></td>
					<td class="col_txt"><input class="easyui-textbox" tabindex="5" col="F3" id="<%=DefineReport.InpText.KIKKN.getObj()%>" check='<%=DefineReport.InpText.KIKKN.getMaxlenTag()%>' style="width:350px;"></td>
				</tr>
				<tr>
					<td class="col_num"></td>
					<td class="col_txt">(カナ)</td>
					<td class="col_txt"><input class="easyui-textbox" tabindex="6" col="F4" id="<%=DefineReport.InpText.SHNAN.getObj()%>" check='<%=DefineReport.InpText.SHNAN.getMaxlenTag()%>' style="width:500px;"></td>
					<td class="col_txt"><input class="easyui-textbox" tabindex="7" col="F5" id="<%=DefineReport.InpText.KIKAN.getObj()%>" check='<%=DefineReport.InpText.KIKAN.getMaxlenTag()%>' style="width:350px;"></td>
				</tr>
			</tbody>
			</table>
		</td>
	</tr>
	<tr>
		<td style="height:89px;padding:0 8px 2px 0;">
			<table id="dg2" class="like_datagrid">
			<thead class="no_header">
				<tr>
					<th style="max-width: 120px"></th>
					<th style="max-width: 120px"></th>
					<th style="max-width: 40px"></th>
					<th style="max-width: 40px"></th>
					<th style="max-width: 40px"></th>
					<th style="max-width: 60px"></th>
					<th style="max-width: 60px"></th>
				</tr>
			</thead>
			<tbody>
				<tr>
					<td class="col_tit">JANコード1</td>
					<td class="col_tit">JANコード2</td>
					<td class="col_tit">大分類</td>
					<td class="col_tit">中分類</td>
					<td class="col_tit">小分類</td>
					<td class="col_tit">メーカー</td>
					<td class="col_tit">仕入先</td>
					<td class="col_tit">仕入先名称</td>
				</tr>
				<tr>
					<td class="col_num"><input class="easyui-numberbox" tabindex="8" col="F6" id="<%=DefineReport.InpText.JANCD1.getObj()%>" check='<%=DefineReport.InpText.JANCD1.getMaxlenTag()%>' style="width:120px;text-align:left;"></td>
					<td class="col_num"><input class="easyui-numberbox" tabindex="9" col="F7" id="<%=DefineReport.InpText.JANCD2.getObj()%>" check='<%=DefineReport.InpText.JANCD2.getMaxlenTag()%>' style="width:120px;text-align:left;"></td>
					<td class="col_num"><input class="easyui-numberbox" tabindex="10" col="F8" id="<%=DefineReport.InpText.DAICD.getObj()%>" check='<%=DefineReport.InpText.DAICD.getMaxlenTag()%>' style="width:40px;text-align:left;"></td>
					<td class="col_num"><input class="easyui-numberbox" tabindex="11" col="F9" id="<%=DefineReport.InpText.CHUCD.getObj()%>" check='<%=DefineReport.InpText.CHUCD.getMaxlenTag()%>' style="width:40px;text-align:left;"></td>
					<td class="col_num"><input class="easyui-numberbox" tabindex="12" col="F10" id="<%=DefineReport.InpText.SHOCD.getObj()%>" check='<%=DefineReport.InpText.SHOCD.getMaxlenTag()%>' style="width:40px;text-align:left;"></td>
					<td class="col_num"><input class="easyui-numberbox" tabindex="13" col="F11" id="<%=DefineReport.InpText.MAKERCD.getObj()%>" check='<%=DefineReport.InpText.MAKERCD.getMaxlenTag()%>' style="width:60px;text-align:left;"></td>
					<td class="col_num"><input class="easyui-numberbox" tabindex="14" col="F12" id="<%=DefineReport.InpText.SSIRCD.getObj()%>" check='<%=DefineReport.InpText.SSIRCD.getMaxlenTag()%>' style="width:60px;text-align:left;"></td>
					<td class="col_txt"><input class="easyui-textbox"   tabindex="15" col="F13" id="<%=DefineReport.InpText.SIRKN.getObj()%>" check='<%=DefineReport.InpText.SIRKN.getMaxlenTag()%>' style="width:450px;text-align:left;"></td>
				</tr>
			</tbody>
			</table>
		</td>
	</tr>
	<tr>
		<td style="height:89px;padding:0 8px 2px 0;">
			<table id="dg6" class="like_datagrid">
			<thead>
				<tr style="">
					<th style="width:87px"></th>
					<th style="width:60px">原価</th>
					<th style="width:60px">売価</th>
					<th style="width:60px">入数</th>
					<th style="width:60px">平均パック単価</th>
				</tr>
			</thead>
			<tbody>
				<tr>
					<td class="col_tit">レギュラー</td>
					<td class="col_num"><input class="easyui-numberbox" tabindex="16" col="F14" id="<%=DefineReport.InpText.RG_GENKAAM.getObj()%>" check='<%=DefineReport.InpText.RG_GENKAAM.getMaxlenTag()%>' style="width:80px;"></td>
					<td class="col_num"><input class="easyui-numberbox" tabindex="17" col="F15" id="<%=DefineReport.InpText.RG_BAIKAAM.getObj()%>" check='<%=DefineReport.InpText.RG_BAIKAAM.getMaxlenTag()%>' style="width:80px;"></td>
					<td class="col_num"><input class="easyui-numberbox" tabindex="18" col="F16" id="<%=DefineReport.InpText.RG_IRISU.getObj()%>" check='<%=DefineReport.InpText.RG_IRISU.getMaxlenTag()%>' style="width:80px;"></td>
					<td class="col_num"><input class="easyui-numberbox" tabindex="19" col="F17" id="<%=DefineReport.InpText.AVGPTANKAAM.getObj()%>" check='<%=DefineReport.InpText.AVGPTANKAAM.getMaxlenTag()%>' style="width:80px;"></td>
				</tr>
				<tr>
					<td class="col_tit">山積・特売</td>
					<td class="col_num"><input class="easyui-numberbox" tabindex="20" col="F18" id="<%=DefineReport.InpText.HS_GENKAAM.getObj()%>" check='<%=DefineReport.InpText.HS_GENKAAM.getMaxlenTag()%>' style="width:80px;"></td>
					<td class="col_num"><input class="easyui-numberbox" tabindex="21" col="F19" id="<%=DefineReport.InpText.HS_BAIKAAM.getObj()%>" check='<%=DefineReport.InpText.HS_BAIKAAM.getMaxlenTag()%>' style="width:80px;"></td>
					<td class="col_num"><input class="easyui-numberbox" tabindex="22" col="F20" id="<%=DefineReport.InpText.HS_IRISU.getObj()%>" check='<%=DefineReport.InpText.HS_IRISU.getMaxlenTag()%>' style="width:80px;"></td>
					<td class="col_num"><input class="easyui-numberbox" tabindex="23" col="F21" id="<%=DefineReport.InpText.HS_AVGPTANKAAM.getObj()%>" check='<%=DefineReport.InpText.AVGPTANKAAM.getMaxlenTag()%>' style="width:80px;"></td>
				</tr>
			</tbody>
			</table>
		</td>
	</tr>
	<tr>
		<td style="height: 77px;width:382px;padding-right: 8px;">
			<table id="dg9" class="like_datagrid">
			<thead>
				<tr>
				<th style="width:102px">　</th>
				<th style="width: 40px">月</th>
				<th style="width: 40px">火</th>
				<th style="width: 40px">水</th>
				<th style="width: 40px">木</th>
				<th style="width: 40px">金</th>
				<th style="width: 40px">土</th>
				<th style="width: 40px">日</th>
				</tr>
			</thead>
			<tbody>
				<tr>
					<td class="col_tit">リードタイム</td>
					<td class="col_lbl_c" style="text-align: right;"><span col="F22"></span></td>
					<td class="col_lbl_c" style="text-align: right;"><span col="F23"></span></td>
					<td class="col_lbl_c" style="text-align: right;"><span col="F24"></span></td>
					<td class="col_lbl_c" style="text-align: right;"><span col="F25"></span></td>
					<td class="col_lbl_c" style="text-align: right;"><span col="F26"></span></td>
					<td class="col_lbl_c" style="text-align: right;"><span col="F27"></span></td>
					<td class="col_lbl_c" style="text-align: right;"><span col="F28"></span></td>
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
			<th style="min-width:120px;"></th><th style="min-width:120px;"></th>
		</tr>
		<tr>
			<td><a href="#" title="戻る" id="<%=DefineReport.Button.BACK.getObj()%>" class="easyui-linkbutton" tabindex="9001" iconCls="icon-undo" style="width:110px;"><span class="btnTxt">戻る</span></a></td>
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
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.MI001.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.control.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.events.js?v=<%=prm %>"></script>
</html>