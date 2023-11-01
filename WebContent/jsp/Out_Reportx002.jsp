
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
<body style="overflow-x: hidden;" onhelp="return false">
<div id="cc" class="easyui-layout" data-options="fit:true">
<form id="ff" method="post" style="display: inline">
<div data-options="region:'north',border:false" style="display:none;height:30px;padding:2px 5px 0;overflow: visible;" >
	<div class="lbl_box" style="width:930px;">
		<span>
		<input class="easyui-numberbox_" tabindex="410" col="F2" id="<%=DefineReport.InpText.YOYAKUDT.getObj()%>" check='<%=DefineReport.InpText.YOYAKUDT.getMaxlenTag()%>' data-options="label:'マスタ変更予定日',labelWidth:105,required:true,prompt:'<%=DefineReport.Label.PROMPT_DT.getTxt()%>'" style="width:170px;">
		<input class="easyui-numberbox_" tabindex="411" col="F3" id="<%=DefineReport.InpText.TENBAIKADT.getObj()%>" check='<%=DefineReport.InpText.TENBAIKADT.getMaxlenTag()%>' data-options="label:'店売価実施日',labelWidth:80,required:true,prompt:'<%=DefineReport.Label.PROMPT_DT.getTxt()%>'" style="width:145px;">
		</span>
		<input class="easyui-numberbox_" tabindex="-1" id="<%=DefineReport.Text.SEL_SHNCD.getObj()%>" check='<%=DefineReport.InpText.SHNCD.getMaxlenTag()%>' readonly="readonly" data-options="label:'<%=DefineReport.Text.SEL_SHNCD.getTxt()%>',labelWidth:65,prompt:'<%=DefineReport.Label.PROMPT_SHNCD.getTxt()%>',readonly:true,editable:false" style="width:145px;">
		<input class="easyui-textbox_" tabindex="-1" id="<%=DefineReport.Text.SEL_SHNKN.getObj()%>" readonly="readonly" data-options="label:'<%=DefineReport.Text.SEL_SHNKN.getTxt()%>',labelWidth:90,readonly:true,editable:false" style="width:320px;">
		<a href="#" class="easyui-linkbutton" tabindex="412" id="<%=DefineReport.Button.YOYAKU1.getObj()%>" title="<%=DefineReport.Button.YOYAKU1.getTxt()%>" iconCls="icon-edit"><span class="btnTxt2"><%=DefineReport.Button.YOYAKU1.getTxt()%></span></a>
		<a href="#" class="easyui-linkbutton" tabindex="-1" id="<%=DefineReport.Button.SEI.getObj()%>" title="<%=DefineReport.Button.SEI.getTxt()%>" iconCls="icon-edit" style="display: none;"><span class="btnTxt2"><%=DefineReport.Button.SEI.getTxt()%></span></a>
		<a href="#" class="easyui-linkbutton" tabindex="413" id="<%=DefineReport.Button.YOYAKU2.getObj()%>" title="<%=DefineReport.Button.YOYAKU2.getTxt()%>" iconCls="icon-edit"><span class="btnTxt2"><%=DefineReport.Button.YOYAKU2.getTxt()%></span></a>
		<input type="hidden" col="F126" id="<%=DefineReport.Text.YOYAKU.getObj()%>" value='' />

		<!-- CVSエラー時利用  -->
		<input type="hidden" tabindex="-1" id="<%=DefineReport.Text.SEQ.getObj()%>" value=""/>
		<input type="hidden" tabindex="-1" id="<%=DefineReport.Text.INPUTNO.getObj()%>" value=""/>
		<input type="hidden" tabindex="-1" id="<%=DefineReport.Text.CSV_UPDKBN.getObj()%>" value=""/>
	</div>
</div>
<div data-options="region:'center',border:false" style="display:none;">
	<div id="tt" class="easyui-tabs" data-options="plain:true,narrow:true,justified:true,fit:true">
	<div title="基本情報" style="display:none;padding:3px 8px;" data-options="selected:true">
		<table class="t-layout">
		<tr><th style="max-width:500px;"></th><th style="max-width:500px;"></th></tr>
		<tr>
		<td>
			<div class="lbl_box">
				<input class="easyui-numberbox_" tabindex="1" col="F1" id="<%=DefineReport.InpText.SHNCD.getObj()%>" check='<%=DefineReport.InpText.SHNCD.getMaxlenTag()%>' data-options="label:'商品コード',labelWidth:90,prompt:'<%=DefineReport.Label.PROMPT_SHNCD.getTxt()%>'" style="width:180px;">
				　
				<span><select class="easyui-combobox_" tabindex="2" col="F125" id="<%=DefineReport.MeisyoSelect.KBN143.getObj()%>" data-options="label:'桁指定',labelWidth:45,required:true" style="width:200px;"></select></span>
			</div>
			<div class="inp_box">
				<input class="easyui-textbox_" tabindex="3" col="F20" id="<%=DefineReport.InpText.SHNAN.getObj()%>" check='<%=DefineReport.InpText.SHNAN.getMaxlenTag()%>' data-options="label:'商品名（カナ）',labelWidth:90,required:true" style="width:480px;">
			</div>
			<div class="inp_box">
				<input class="easyui-textbox_" tabindex="4" col="F21" id="<%=DefineReport.InpText.SHNKN.getObj()%>" check='<%=DefineReport.InpText.SHNKN.getMaxlenTag()%>' data-options="label:'商品名（漢字）',labelWidth:90" style="width:480px;">
			</div>
		</td>
		<td>
			<div class="inp_box">
				<input class="easyui-textbox_" tabindex="151" col="F46" id="<%=DefineReport.InpText.KIKKN.getObj()%>" check='<%=DefineReport.InpText.KIKKN.getMaxlenTag()%>' data-options="label:'規格',labelWidth:90" style="width:335px;">
				<input type="checkbox" class="chkbox" tabindex="152" col="F97" id="chk_price" value=""><label class="chk_lbl" for="chk_price">プライスカード出力有無</label>
			</div>
			<fieldset>
			<legend>ユニットプライス</legend>
			<input class="easyui-numberbox_" tabindex="153" col="F47" id="<%=DefineReport.InpText.UP_YORYOSU.getObj()%>" check='<%=DefineReport.InpText.UP_YORYOSU.getMaxlenTag()%>' data-options="label:'容量',labelWidth:30,min:0" style="width:100px;text-align: right;">
			　
			<input class="easyui-numberbox_" tabindex="154" col="F48" id="<%=DefineReport.InpText.UP_TYORYOSU.getObj()%>" check='<%=DefineReport.InpText.UP_TYORYOSU.getMaxlenTag()%>' data-options="label:'単位容量',labelWidth:55,min:0" style="width:125px;text-align: right;">
			　
			<select class="easyui-combobox_" tabindex="155" col="F49" id="<%=DefineReport.MeisyoSelect.KBN113.getObj()%>"data-options="label:'ユニット単位',labelWidth:80" style="width:200px;"></select>
			</fieldset>
		</td>
		</tr>
		<tr>
		<td style="padding:0 8px 2px 0">
			<div id="tt_1" class="easyui-tabs" data-options="plain:true,narrow:true,justified:true" style="height:82px;width:490px;">
				<div title="レシート品名" class="box" style="overflow: visible;">
					<div class="inp_box">
						<input class="easyui-textbox_" tabindex="110" col="F24" id="<%=DefineReport.InpText.RECEIPTAN.getObj()%>" check='<%=DefineReport.InpText.RECEIPTAN.getMaxlenTag()%>' data-options="label:'カナ名',labelWidth:85" style="width:474px;">
					</div>
					<div>
						<input class="easyui-textbox_" tabindex="111" col="F25" id="<%=DefineReport.InpText.RECEIPTKN.getObj()%>" check='<%=DefineReport.InpText.RECEIPTKN.getMaxlenTag()%>' data-options="label:'漢字名',labelWidth:85" style="width:474px;">
					</div>
				</div>
				<div title="プライスカード" class="box" style="overflow: visible;">
					<div class="inp_box">
						<input class="easyui-textbox_" tabindex="115" col="F22" id="<%=DefineReport.InpText.PCARDKN.getObj()%>" check='<%=DefineReport.InpText.PCARDKN.getMaxlenTag()%>' data-options="label:'商品名称',labelWidth:85" style="width:474px;">
					</div>
					<div style="padding-left:85px;">
						<select class="easyui-combobox_" tabindex="116" col="F64" id="<%=DefineReport.MeisyoSelect.KBN118.getObj()%>"data-options="label:'種類',labelWidth:40,required:true" style="width:140px;"></select>
						　
						<select class="easyui-combobox_" tabindex="117" col="F65" id="<%=DefineReport.MeisyoSelect.KBN119.getObj()%>"data-options="label:'色',labelWidth:40,required:true" style="width:140px;"></select>
					</div>
				</div>
			</div>
		</td>
		<td rowspan="2" style="vertical-align: top;">
			<table class="t-layout2">
			<tr><th style="min-width:360px;"></th><th style="max-width:140px;"></th></tr>
			<tr>
			<td>
				<select class="easyui-combobox_" tabindex="160" col="F57" id="<%=DefineReport.MeisyoSelect.KBN117.getObj()%>"data-options="label:'定計区分',labelWidth:90,panelWidth:120,required:true" style="width:180px;"></select>
			</td>
			<td>
				<a href="#" class="easyui-linkbutton" tabindex="161" id="<%=DefineReport.Button.SRCCD.getObj()%>" title="<%=DefineReport.Button.SRCCD.getTxt()%>" iconCls="icon-search" style="width:132px;"><span>ソースコード登録</span></a>
			</td>
			</tr>
			<tr>
			<td colspan="2">
				<div style="height:77px;width: 495px;">
					<table id="<%=DefineReport.Grid.SRCCD.getObj()%>" class="easyui-datagrid_" tabindex="162">
					</table>
				</div>
			</td>
			</tr>
			</table>
			<table class="t-layout2">
			<tr><th style="min-width:90px;"></th><th style="min-width:400px;"></th></tr>
			<tr>
			<td style="line-height: 12px;overflow: visible;">商品コメント・<br>セールスコピー</td>
			<td style="vertical-align: middle;">
				<input class="easyui-textbox_" tabindex="122" col="F95" id="<%=DefineReport.InpText.SALESCOMKN.getObj()%>" check='<%=DefineReport.InpText.SALESCOMKN.getMaxlenTag()%>' style="width:405px;">
			</td>
			</tr>
			<tr>
			<td>メーカー名称</td>
			<td>
				<input class="easyui-numberbox_" tabindex="170" col="F72" id="<%=DefineReport.InpText.MAKERCD.getObj()%>" check='<%=DefineReport.InpText.MAKERCD.getMaxlenTag()%>' style="width:80px;" for_btn="<%=DefineReport.Button.MAKER.getObj()%>_F2">
				<span class="lbl_box">
					<input class="easyui-textbox_" tabindex="-1" id="<%=DefineReport.InpText.MAKERKN.getObj()%>" readonly="readonly" for_inp="<%=DefineReport.InpText.MAKERCD.getObj()%>_F3" for_btn="<%=DefineReport.Button.MAKER.getObj()%>_F3" data-options="readonly:true" style="width:215px;">
				</span>
				<a href="#" class="easyui-linkbutton" tabindex="172" id="<%=DefineReport.Button.MAKER.getObj()%>" title="<%=DefineReport.Button.MAKER.getTxt()%>" iconCls="icon-search" style="width:102px;"><span>メーカー検索</span></a>
			</td>
			</table>
		</td>
		</tr>
		<tr>
		<td>
			<div class="inp_box">
				<input class="easyui-textbox_" tabindex="120" col="F30" id="<%=DefineReport.InpText.SANCHIKN.getObj()%>" check='"datatyp":"zen_text","maxlen":40' data-options="label:'メーカー・産地',labelWidth:90" style="width:480px;">
			</div>
			<div class="inp_box">
				<input class="easyui-textbox_" tabindex="121" col="F23" id="<%=DefineReport.InpText.POPKN.getObj()%>" check='<%=DefineReport.InpText.POPKN.getMaxlenTag()%>' data-options="label:'ＰＯＰ名称',labelWidth:90" style="width:480px;">
			</div>
			<table class="t-layout1">
			<tr>
			<td class="inp_box">
				<input class="easyui-numberbox_" tabindex="124" col="F98" id="<%=DefineReport.InpText.PARENTCD.getObj()%>" check='<%=DefineReport.InpText.PARENTCD.getMaxlenTag()%>' data-options="label:'親商品コード',labelWidth:90,prompt:'<%=DefineReport.Label.PROMPT_SHNCD.getTxt()%>'" style="width:180px;">
			</td>
			<td class="lbl_box">
				<input class="easyui-numberbox_" tabindex="-1" col="F94" id="<%=DefineReport.InpText.URICD.getObj()%>" check='<%=DefineReport.InpText.URICD.getMaxlenTag()%>' data-options="label:'販売コード',labelWidth:65,readonly:true" readonly="readonly" style="width:150px;">
			</td>
			<td class="lbl_box">
				<input class="easyui-textbox_" tabindex="-1" col="F122" id="<%=DefineReport.InpText.YOBIDASHICD.getObj()%>" check='<%=DefineReport.InpText.YOBIDASHICD.getMaxlenTag()%>' readonly="readonly" data-options="label:'呼出コード',labelWidth:65,readonly:true" style="width:150px;">
			</td>
			</tr>
			</table>
		</td>
		</tr>
		<tr>
		<td style="padding-bottom:2px;">
			<div id="tt_2" class="easyui-tabs" data-options="plain:true,narrow:true,justified:true" style="height:154px;width:490px;">
			<div title="標準分類">
				<table id="dg2" class="like_datagrid">
				<thead class="no_header">
					<tr>
						<th style="width: 80px;">分類</th>
						<th style="width: 45px;">コード</th>
						<th style="width:358px;">名称</th>
					</tr>
				</thead>
				<tbody>
					<tr>
						<td class="col_tit">部門</td>
						<td class="col_cd "><input class="easyui-numberbox_" tabindex="130" col="F12" id="<%=DefineReport.InpText.BMNCD.getObj()%>" check='<%=DefineReport.InpText.BMNCD.getMaxlenTag()%>' data-options="required:true" style="width:40px;"></td>
						<td class="col_lbl"><span for_inp="<%=DefineReport.InpText.BMNCD.getObj()%>_TEXT2"></span><input type="hidden" id="<%=DefineReport.Text.HYOKAKBN.getObj()%>" for_inp="<%=DefineReport.InpText.BMNCD.getObj()%>_HYOKAKBN"/></td>
					</tr>
					<tr>
						<td class="col_tit">大分類</td>
						<td class="col_cd "><input class="easyui-numberbox_" tabindex="131" col="F13" id="<%=DefineReport.InpText.DAICD.getObj()%>" check='<%=DefineReport.InpText.DAICD.getMaxlenTag()%>' style="width:40px;"></td>
						<td class="col_lbl"><span for_inp="<%=DefineReport.InpText.DAICD.getObj()%>_TEXT2"></span></td>
					</tr>
					<tr>
						<td class="col_tit">中分類</td>
						<td class="col_cd "><input class="easyui-numberbox_" tabindex="132" col="F14" id="<%=DefineReport.InpText.CHUCD.getObj()%>" check='<%=DefineReport.InpText.CHUCD.getMaxlenTag()%>' style="width:40px;"></td>
						<td class="col_lbl"><span for_inp="<%=DefineReport.InpText.CHUCD.getObj()%>_TEXT2"></span></td>
					</tr>
					<tr>
						<td class="col_tit">小分類</td>
						<td class="col_cd "><input class="easyui-numberbox_" tabindex="133" col="F15" id="<%=DefineReport.InpText.SHOCD.getObj()%>" check='<%=DefineReport.InpText.SHOCD.getMaxlenTag()%>' style="width:40px;"></td>
						<td class="col_lbl"><span for_inp="<%=DefineReport.InpText.SHOCD.getObj()%>_TEXT2"></span></td>
					</tr>
					<!-- 20171214.非表示 -->
					<tr style="display: none;">
						<td class="col_tit">小小分類</td>
						<!-- tabindex:19→-1 -->
						<td class="col_cd "><input class="easyui-numberbox_" tabindex="-1" col="F16" id="<%=DefineReport.InpText.SSHOCD.getObj()%>" check='<%=DefineReport.InpText.SSHOCD.getMaxlenTag()%>' style="width:40px;" disabled="disabled"></td>
						<td class="col_lbl"><span for_inp="<%=DefineReport.InpText.SSHOCD.getObj()%>_TEXT2"></span></td>
					</tr>
				</tbody>
				</table>
			</div>
			<div title="用途分類">
				<table id="dg3" class="like_datagrid">
				<thead class="no_header">
					<tr>
						<th style="width: 80px;">分類</th>
						<th style="width: 45px;">コード</th>
						<th style="width:358px;">名称</th>
					</tr>
				</thead>
				<tbody>
					<tr>
						<td class="col_tit">部門</td>
						<td class="col_cd "><input class="easyui-numberbox_" tabindex="-1" col="F4" id="<%=DefineReport.InpText.YOT_BMNCD.getObj()%>" check='<%=DefineReport.InpText.YOT_BMNCD.getMaxlenTag()%>' style="width:40px;"></td>
						<td class="col_lbl"><span for_inp="<%=DefineReport.InpText.YOT_BMNCD.getObj()%>_TEXT2"></span></td>
					</tr>
					<tr>
						<td class="col_tit">大分類</td>
						<td class="col_cd "><input class="easyui-numberbox_" tabindex="-1" col="F5" id="<%=DefineReport.InpText.YOT_DAICD.getObj()%>" check='<%=DefineReport.InpText.YOT_DAICD.getMaxlenTag()%>' style="width:40px;"></td>
						<td class="col_lbl"><span for_inp="<%=DefineReport.InpText.YOT_DAICD.getObj()%>_TEXT2"></span></td>
					</tr>
					<tr>
						<td class="col_tit">中分類</td>
						<td class="col_cd "><input class="easyui-numberbox_" tabindex="-1" col="F6" id="<%=DefineReport.InpText.YOT_CHUCD.getObj()%>" check='<%=DefineReport.InpText.YOT_CHUCD.getMaxlenTag()%>' style="width:40px;"></td>
						<td class="col_lbl"><span for_inp="<%=DefineReport.InpText.YOT_CHUCD.getObj()%>_TEXT2"></span></td>
					</tr>
					<tr>
						<td class="col_tit">小分類</td>
						<td class="col_cd "><input class="easyui-numberbox_" tabindex="-1" col="F7" id="<%=DefineReport.InpText.YOT_SHOCD.getObj()%>" check='<%=DefineReport.InpText.YOT_SHOCD.getMaxlenTag()%>' style="width:40px;"></td>
						<td class="col_lbl"><span for_inp="<%=DefineReport.InpText.YOT_SHOCD.getObj()%>_TEXT2"></span></td>
					</tr>
					<!-- 20171214.非表示 -->
					<tr style="display: none;">
						<td class="col_tit">小小分類</td>
						<!-- tabindex:24→-1 -->
						<td class="col_cd "><input class="easyui-numberbox_" tabindex="-1" id="<%=DefineReport.InpText.YOT_SSHOCD.getObj()%>" check='<%=DefineReport.InpText.YOT_SSHOCD.getMaxlenTag()%>' style="width:40px;" disabled="disabled"></td>
						<td class="col_lbl"><span for_inp="<%=DefineReport.InpText.YOT_SSHOCD.getObj()%>_TEXT2"></span></td>
					</tr>
				</tbody>
				</table>
			</div>
			<div title="売場分類">
				<table id="dg4" class="like_datagrid">
				<thead class="no_header">
					<tr>
						<th style="width: 80px;">分類</th>
						<th style="width: 45px;">コード</th>
						<th style="width:358px;">名称</th>
					</tr>
				</thead>
				<tbody>
					<tr>
						<td class="col_tit">部門</td>
						<td class="col_cd "><input class="easyui-numberbox_" tabindex="-1" col="F8" id="<%=DefineReport.InpText.URI_BMNCD.getObj()%>" check='<%=DefineReport.InpText.URI_BMNCD.getMaxlenTag()%>' style="width:40px;"></td>
						<td class="col_lbl"><span for_inp="<%=DefineReport.InpText.URI_BMNCD.getObj()%>_TEXT2"></span></td>
					</tr>
					<tr>
						<td class="col_tit">大分類</td>
						<td class="col_cd "><input class="easyui-numberbox_" tabindex="-1" col="F9" id="<%=DefineReport.InpText.URI_DAICD.getObj()%>" check='<%=DefineReport.InpText.URI_DAICD.getMaxlenTag()%>' style="width:40px;"></td>
						<td class="col_lbl"><span for_inp="<%=DefineReport.InpText.URI_DAICD.getObj()%>_TEXT2"></span></td>
					</tr>
					<tr>
						<td class="col_tit">中分類</td>
						<td class="col_cd "><input class="easyui-numberbox_" tabindex="-1" col="F10" id="<%=DefineReport.InpText.URI_CHUCD.getObj()%>" check='<%=DefineReport.InpText.URI_CHUCD.getMaxlenTag()%>' style="width:40px;"></td>
						<td class="col_lbl"><span for_inp="<%=DefineReport.InpText.URI_CHUCD.getObj()%>_TEXT2"></span></td>
					</tr>
					<tr>
						<td class="col_tit">小分類</td>
						<td class="col_cd "><input class="easyui-numberbox_" tabindex="-1" col="F11" id="<%=DefineReport.InpText.URI_SHOCD.getObj()%>" check='<%=DefineReport.InpText.URI_SHOCD.getMaxlenTag()%>' style="width:40px;"></td>
						<td class="col_lbl"><span for_inp="<%=DefineReport.InpText.URI_SHOCD.getObj()%>_TEXT2"></span></td>
					</tr>
					<!-- 20171214.非表示 -->
					<tr style="display: none;">
						<td class="col_tit">小小分類</td>
						<!-- tabindex:29→-1 -->
						<td class="col_cd "><input class="easyui-numberbox_" tabindex="-1" id="<%=DefineReport.InpText.URI_SSHOCD.getObj()%>" check='<%=DefineReport.InpText.URI_SSHOCD.getMaxlenTag()%>' style="width:40px;" disabled="disabled"></td>
						<td class="col_lbl"><span for_inp="<%=DefineReport.InpText.URI_SSHOCD.getObj()%>_TEXT2"></span></td>
					</tr>
				</tbody>
				</table>
			</div>
			<div title="グループ分類">
				<table id="<%=DefineReport.Grid.GROUP.getObj()%>" class="easyui-datagrid_" tabindex="-1" data-options="singleSelect:true,rownumbers:false, fit:true, showHeader:false,fitColumns:true">
				<thead>
					<tr>
						<th data-options="field:'GRPKN',width: 486,halign:'center',editor:'combobox'">グループ分類名</th>
					</tr>
				</thead>
				</table>
				<!-- Editor参照用 -->
				<div class="ref_editor" style="display: block;">
					<input class="easyui-combobox_" tabindex="-1" id="<%=DefineReport.InpText.GRPKN.getObj()%>" data-options="hasDownArrow:false" style="width:100px;" />
				</div>
			</div>
		</td>
		<td style="vertical-align: bottom;padding-bottom:2px;">
			<fieldset>
			<legend>店別異部門</legend>
			<div class="inp_box">
				<label class="rad_lbl"><input type="radio" tabindex="173" col="F131" name="<%=DefineReport.Radio.AREAKBN.getObj()%><%=DefineReport.ValGpkbn.TBMN.getVal()%>" value="<%=DefineReport.ValKbn135.VAL0.getVal()%>" style="vertical-align:middle;"/>エリア</label>
				<label class="rad_lbl"><input type="radio" tabindex="174" col="F131" name="<%=DefineReport.Radio.AREAKBN.getObj()%><%=DefineReport.ValGpkbn.TBMN.getVal()%>" value="<%=DefineReport.ValKbn135.VAL1.getVal()%>" style="vertical-align:middle;"/>店グループ</label>
				<a href="#" class="easyui-linkbutton" tabindex="175" id="<%=DefineReport.Button.TENGP.getObj()%><%=DefineReport.ValGpkbn.TBMN.getVal()%>" title="<%=DefineReport.Button.TENGP.getTxt()%>" iconCls="icon-search" style="width:102px;float:right;"><span>店グループ</span></a>
			</div>
			<div style="height:102px;width:488px;clear:both;">
				<table id="<%=DefineReport.Grid.TENGP.getObj()%><%=DefineReport.ValGpkbn.TBMN.getVal()%>" class="easyui-datagrid_" tabindex="176">
				</table>
				<!-- Editor参照用 -->
				<div class="ref_editor" style="display: none;">
					<input class="easyui-numberbox_" tabindex="-1" id="<%=DefineReport.InpText.TENSHNCD.getObj()%>" check='<%=DefineReport.InpText.TENSHNCD.getMaxlenTag()%>' data-options="prompt:'<%=DefineReport.Label.PROMPT_SHNCD.getTxt()%>'"/>
					<input class="easyui-textbox_" tabindex="-1" id="<%=DefineReport.InpText.SRCCD.getObj()%>" check='<%=DefineReport.InpText.SRCCD.getMaxlenTag()%>'/>
				</div>
			</div>
			</fieldset>

			<!-- 20171020.非表示 -->
			<div style="vertical-align: bottom;padding-bottom:2px;display:none;">
				<fieldset>
				<legend>品揃えグループ</legend>
				<div class="inp_box">
				<!-- tabindex:42,43→-1 -->
					<label class="rad_lbl"><input type="radio" tabindex="-1" col="F128" name="<%=DefineReport.Radio.AREAKBN.getObj()%><%=DefineReport.ValGpkbn.SHINA.getVal()%>" value="<%=DefineReport.ValKbn135.VAL0.getVal()%>" style="vertical-align:middle;" disabled="disabled"/>エリア</label>
					<label class="rad_lbl"><input type="radio" tabindex="-1" col="F128" name="<%=DefineReport.Radio.AREAKBN.getObj()%><%=DefineReport.ValGpkbn.SHINA.getVal()%>" value="<%=DefineReport.ValKbn135.VAL1.getVal()%>" style="vertical-align:middle;" disabled="disabled"/>店グループ</label>
					<!-- tabindex:44→-1 -->
					<a href="#" class="easyui-linkbutton" tabindex="-1" id="<%=DefineReport.Button.TENGP.getObj()%><%=DefineReport.ValGpkbn.SHINA.getVal()%>" title="<%=DefineReport.Button.TENGP.getTxt()%>" iconCls="icon-search" style="width:102px;float:right;" disabled="disabled"><span>店グループ</span></a>
				</div>
				<div style="height:102px;width:488px;clear:both;">
					<!-- tabindex:45→-1 -->
					<table id="<%=DefineReport.Grid.TENGP.getObj()%><%=DefineReport.ValGpkbn.SHINA.getVal()%>" class="easyui-datagrid_" tabindex="-1">
					</table>
					<!-- Editor参照用 -->
					<div class="ref_editor" style="display: none;">
						<input  class="easyui-numberbox_" tabindex="-1" id="<%=DefineReport.InpText.TENGPCD.getObj()%>" check='<%=DefineReport.InpText.TENGPCD.getMaxlenTag()%>' for_btn="<%=DefineReport.Button.TENGP.getObj()%>_F1"/>
						<input  class="easyui-textbox_" tabindex="-1" id="<%=DefineReport.InpText.TENGPKN.getObj()%>" for_inp="<%=DefineReport.InpText.TENGPCD.getObj()%>_TEXT2" for_btn="<%=DefineReport.Button.TENGP.getObj()%>_F2"/>
						<select class="easyui-combobox_" tabindex="-1" id="<%=DefineReport.MeisyoSelect.KBN139.getObj()%>"></select>
					</div>
				</div>
				</fieldset>
			</div>
		</td>
		</tr>
		<tr>
		<td>
			<table class="t-layout2">
			<tr>
			<td>
				<input class="easyui-numberbox_" tabindex="181" col="F17" id="<%=DefineReport.InpText.ATSUK_STDT.getObj()%>" check='<%=DefineReport.InpText.ATSUK_STDT.getMaxlenTag()%>' data-options="label:'取扱期間',labelWidth:60,prompt:'__/__/__'" style="width:125px;" value="">
				<input class="easyui-numberbox_" tabindex="182" col="F18" id="<%=DefineReport.InpText.ATSUK_EDDT.getObj()%>" check='<%=DefineReport.InpText.ATSUK_EDDT.getMaxlenTag()%>' data-options="label:'～',labelWidth:17,prompt:'__/__/__'" style="width:82px;" value="">
				　
			</td>
			<td>
				<select class="easyui-combobox_" tabindex="184" col="F29" id="<%=DefineReport.MeisyoSelect.KBN105.getObj()%>" data-options="label:'商品種類',labelWidth:60,panelWidth:180,panelHeight:120,required:true" style="width:200px;"></select>
			</td>
			</tr>
			<tr>
			<td>
				<select class="easyui-combobox_" tabindex="183" col="F19" id="<%=DefineReport.MeisyoSelect.KBN101.getObj()%>" data-options="label:'取扱停止',labelWidth:60,required:true" style="width:200px;"></select>
			</td>
			<td for_bmn="20,23,31,70,73">
				<select class="easyui-combobox_" tabindex="185" col="F116" id="<%=DefineReport.MeisyoSelect.KBN151.getObj()%>" data-options="label:'保温区分',labelWidth:60,panelWidth:180,panelHeight:120" style="width:200px;"></select>
			</td>
			</tr>
			</table>
		</td>
		<td>
			<div class="inp_box">
				<select class="easyui-combobox_" tabindex="191" col="F26" id="<%=DefineReport.MeisyoSelect.KBN102.getObj()%>" data-options="label:'PC区分',labelWidth:60,required:true" style="width:200px;"></select>
				　
				<select class="easyui-combobox_" tabindex="192" col="F71" id="<%=DefineReport.MeisyoSelect.KBN121.getObj()%>" data-options="label:'定貫不定貫区分',labelWidth:90,required:true" style="width:220px;"></select>
			</div>
			<div class="inp_box">
				<select class="easyui-combobox_" tabindex="193" col="F27" id="<%=DefineReport.MeisyoSelect.KBN103.getObj()%>" data-options="label:'加工区分',labelWidth:60,required:true" style="width:200px;"></select>
				　
				<select class="easyui-combobox_" tabindex="194" col="F56" id="<%=DefineReport.MeisyoSelect.KBN116.getObj()%>" data-options="label:'棚卸区分',labelWidth:90,panelHeight:100,required:true" style="width:220px;"></select>
			</div>
		</td>
		</tr>
		</table>
	</div>
	<div title="仕入・発注" style="display:none;padding:3px 8px;">
		<table class="t-layout" style="vertical-align: top;">
		<tr><th style="max-width:580px;"></th><th style="max-width:420px;"></th></tr>
		<tr>
		<td style="height:89px;padding:0 8px 2px 0;">
			<table id="dg6" class="like_datagrid">
			<thead>
				<tr style="">
					<th style="width:87px"></th>
					<th style="width:50px">取扱う</th>
					<th style="width:80px">原価</th>
					<th style="width:60px">本体売価</th>
					<th style="width:60px">総売価</th>
					<th style="width:60px">値入率</th>
					<th style="width:60px">平均パック<br>単価</th>
					<th style="width:40px">店入数</th>
				</tr>
			</thead>
			<tbody>
				<tr>
					<td class="col_tit">レギュラー</td>
					<td class="col_chk"><input type="checkbox" tabindex="201" col="F33" id="chk_rg_atsukflg" /></td>
					<td class="col_num"><input class="easyui-numberbox_" tabindex="202" col="F34" id="<%=DefineReport.InpText.RG_GENKAAM.getObj()%>" check='<%=DefineReport.InpText.RG_GENKAAM.getMaxlenTag()%>' style="width:100%;" data-options="min:0"></td>
					<td class="col_num"><input class="easyui-numberbox_" tabindex="203" col="F35" id="<%=DefineReport.InpText.RG_BAIKAAM.getObj()%>" check='<%=DefineReport.InpText.RG_BAIKAAM.getMaxlenTag()%>' style="width:100%;" data-options="min:0"></td>
					<td class="col_lbl_r"><span id="<%=DefineReport.Text.RG_SOUBAIKA.getObj()%>"></span></td>
					<td class="col_lbl_r"><span id="<%=DefineReport.Text.RG_NEIRE.getObj()%>" ></span></td>
					<td class="col_lbl_r"><span col="F123"></span></td>
					<td class="col_num"><input class="easyui-numberbox_" tabindex="204" col="F36" id="<%=DefineReport.InpText.RG_IRISU.getObj()%>" check='<%=DefineReport.InpText.RG_IRISU.getMaxlenTag()%>' style="width:100%;" data-options="min:0"></td>
				</tr>
				<tr>
					<td class="col_tit">販促</td>
					<td class="col_chk"><input type="checkbox" tabindex="205" col="F39" id="chk_hs_atsukflg" /></td>
					<td class="col_num"><input class="easyui-numberbox_" tabindex="206" col="F40" id="<%=DefineReport.InpText.HS_GENKAAM.getObj()%>" check='<%=DefineReport.InpText.HS_GENKAAM.getMaxlenTag()%>' style="width:100%;" data-options="min:0"></td>
					<td class="col_num"><input class="easyui-numberbox_" tabindex="207" col="F41" id="<%=DefineReport.InpText.HS_BAIKAAM.getObj()%>" check='<%=DefineReport.InpText.HS_BAIKAAM.getMaxlenTag()%>' style="width:100%;" data-options="min:0"></td>
					<td class="col_lbl_r"><span id="<%=DefineReport.Text.HS_SOUBAIKA.getObj()%>"></span></td>
					<td class="col_lbl_r"><span id="<%=DefineReport.Text.HS_NEIRE.getObj()%>"></span></td>
					<td class="col_lbl_r"><span col="F124"></span></td>
					<td class="col_num"><input class="easyui-numberbox_" tabindex="208" col="F42" id="<%=DefineReport.InpText.HS_IRISU.getObj()%>" check='<%=DefineReport.InpText.HS_IRISU.getMaxlenTag()%>' style="width:100%;" data-options="min:0"></td>
				</tr>
			</tbody>
			</table>
		</td>
		<td style="padding-bottom:2px; vertical-align: bottom;">
			<fieldset>
			<legend>税情報</legend>
			<div class="inp_box">
				<select class="easyui-combobox_" tabindex="-1" col="F66" id="<%=DefineReport.MeisyoSelect.KBN120.getObj()%>" data-options="label:'税区分',labelWidth:54,required:true" style="width:191px;"></select>
				　
				<input class="easyui-numberbox_" tabindex="-1" col="F69" id="<%=DefineReport.InpText.ZEIRTHENKODT.getObj()%>" check='<%=DefineReport.InpText.ZEIRTHENKODT.getMaxlenTag()%>' data-options="label:'税率変更日',labelWidth:67,prompt:'__/__/__'" style="width:145px;">
			</div>
			<div>
				<select class="easyui-combobox_" tabindex="-1" col="F67" id="<%=DefineReport.Select.ZEIRTKBN.getObj()%>" data-options="label:'税率区分',labelWidth:54" style="width:191px;"></select>
				　
				<select class="easyui-combobox_" tabindex="-1" col="F68" id="<%=DefineReport.Select.ZEIRTKBN_OLD.getObj()%>" data-options="label:'旧税率区分',labelWidth:67" style="width:195px;"></select>
			</div>
			</fieldset>
		</td>
		</tr>
		<tr>
		<td colspan="2" style="padding-bottom:2px;">
			<input class="easyui-textbox_" tabindex="-1" col="F120" id="<%=DefineReport.InpText.ITFCD.getObj()%>" check='<%=DefineReport.InpText.ITFCD.getMaxlenTag()%>' data-options="label:'ITFコード',labelWidth:70" style="width:190px;">
			<input class="easyui-numberbox_" tabindex="-1" col="F121" id="<%=DefineReport.InpText.CENTER_IRISU.getObj()%>" check='<%=DefineReport.InpText.CENTER_IRISU.getMaxlenTag()%>' data-options="label:'センター入数',labelWidth:80,min:0" style="width:140px;text-align: right;">
		</td>
		</tr>
		<tr>
		<td colspan="2" style="padding-bottom:2px;">
			<div id="tt_3" class="easyui-tabs" data-options="plain:true,narrow:true,justified:true" style="height:206px;width:1000px;">
			<div title="売価グループ">
				<div class="box">
					<label class="rad_lbl"><input type="radio" tabindex="-1" col="F129" name="<%=DefineReport.Radio.AREAKBN.getObj()%><%=DefineReport.ValGpkbn.BAIKA.getVal()%>" value="<%=DefineReport.ValKbn135.VAL0.getVal()%>"/>エリア</label>
					<label class="rad_lbl"><input type="radio" tabindex="-1" name="<%=DefineReport.Radio.AREAKBN.getObj()%><%=DefineReport.ValGpkbn.BAIKA.getVal()%>" value="<%=DefineReport.ValKbn135.VAL1.getVal()%>"/>店グループ</label>
					<div style="float:right;">
					<a href="#" class="easyui-linkbutton" tabindex="-1" id="<%=DefineReport.Button.TENGP.getObj()%><%=DefineReport.ValGpkbn.BAIKA.getVal()%>" title="<%=DefineReport.Button.TENGP.getTxt()%>" iconCls="icon-search" style="width:102px;float:right;"><span>店グループ</span></a>
					</div>
				</div>
				<table id="<%=DefineReport.Grid.TENGP.getObj()%><%=DefineReport.ValGpkbn.BAIKA.getVal()%>" class="easyui-datagrid_" tabindex="-1">
				</table>
				<!-- Editor参照用 -->
				<div class="ref_editor" style="display: none;">
					<!-- ここで未宣言は他で宣言済み -->
					<input  class="easyui-numberbox_" tabindex="-1" id="<%=DefineReport.InpText.GENKAAM.getObj()%>" check='<%=DefineReport.InpText.GENKAAM.getMaxlenTag()%>' data-options="min:0"/>
					<input  class="easyui-numberbox_" tabindex="-1" id="<%=DefineReport.InpText.BAIKAAM.getObj()%>" check='<%=DefineReport.InpText.BAIKAAM.getMaxlenTag()%>' data-options="min:0"/>
					<input  class="easyui-numberbox_" tabindex="-1" id="<%=DefineReport.InpText.IRISU.getObj()%>" check='<%=DefineReport.InpText.IRISU.getMaxlenTag()%>' data-options="min:0"/>
					<input  class="easyui-textbox_" tabindex="-1" id="<%=DefineReport.Text.BG_SOUBAIKA.getObj()%>" check='<%=DefineReport.InpText.RG_BAIKAAM.getMaxlenTag()%>'/>
					<input  class="easyui-textbox_" tabindex="-1" id="<%=DefineReport.Text.BG_NEIRE.getObj()%>"/>

				</div>
			</div>
			<div title="仕入グループ">
				<div class="box">
					<label class="rad_lbl"><input type="radio" tabindex="-1" col="F130" name="<%=DefineReport.Radio.AREAKBN.getObj()%><%=DefineReport.ValGpkbn.SIR.getVal()%>" value="<%=DefineReport.ValKbn135.VAL0.getVal()%>"/>エリア</label>
					<label class="rad_lbl"><input type="radio" tabindex="-1" name="<%=DefineReport.Radio.AREAKBN.getObj()%><%=DefineReport.ValGpkbn.SIR.getVal()%>" value="<%=DefineReport.ValKbn135.VAL1.getVal()%>"/>店グループ</label>
					<div style="float:right;">
					<a href="#" class="easyui-linkbutton" tabindex="-1" id="<%=DefineReport.Button.TENGP.getObj()%><%=DefineReport.ValGpkbn.SIR.getVal()%>" title="<%=DefineReport.Button.TENGP.getTxt()%>" iconCls="icon-search" style="width:102px;"><span>店グループ</span></a>
					<a href="#" class="easyui-linkbutton" tabindex="-1" id="<%=DefineReport.Button.SIR.getObj()%><%=DefineReport.Grid.TENGP.getObj()%><%=DefineReport.ValGpkbn.SIR.getVal()%>" title="<%=DefineReport.Button.SIR.getTxt()%>" iconCls="icon-search" style="width:100px;"><span class="btnTxt">仕入先</span></a>
					<a href="#" class="easyui-linkbutton" tabindex="-1" id="<%=DefineReport.Button.HSPTN.getObj()%><%=DefineReport.Grid.TENGP.getObj()%><%=DefineReport.ValGpkbn.SIR.getVal()%>" title="<%=DefineReport.Button.HSPTN.getTxt()%>" iconCls="icon-search" style="width:100px;"><span>配送パターン</span></a>
					</div>
				</div>
				<table id="<%=DefineReport.Grid.TENGP.getObj()%><%=DefineReport.ValGpkbn.SIR.getVal()%>" class="easyui-datagrid_" tabindex="-1">
				</table>
				<!-- Editor参照用 -->
				<div class="ref_editor" style="display: none;">
					<!-- ここで未宣言は他で宣言済み -->
				</div>
			</div>
			</div>
		</td>
		</tr>
		<tr>
		<td style="padding-right:8px;">
			<div class="lbl_box" style="text-align: right;padding-right: 1px;">
				<a href="#" class="easyui-linkbutton" tabindex="-1" id="<%=DefineReport.Button.SIR.getObj()%>" title="<%=DefineReport.Button.SIR.getTxt()%>" iconCls="icon-search" style="width:120px;">　<span class="btnTxt">仕入先</span></a>
				　
				<a href="#" class="easyui-linkbutton" tabindex="-1" id="<%=DefineReport.Button.HSPTN.getObj()%>" title="<%=DefineReport.Button.HSPTN.getTxt()%>" iconCls="icon-search" style="width:120px;"><span>配送パターン</span></a>
				　
				<input class="easyui-textbox_" tabindex="-1" id="<%=DefineReport.InpText.EDI_RKBN.getObj()%>" for_inp="<%=DefineReport.InpText.SSIRCD.getObj()%>_F4" readonly="readonly" data-options="readonly:true,editable:false"  style="width:120px;" value="">
			</div>
			<table class="t-layout2">
			<tr><th style="max-width:210px;"></th><th style="max-width:360px;"></th></tr>
			<tr>
			<td>
				<input class="easyui-numberbox_" tabindex="232" col="F31" id="<%=DefineReport.InpText.SSIRCD.getObj()%>" check='<%=DefineReport.InpText.SSIRCD.getMaxlenTag()%>' for_btn="<%=DefineReport.Button.SIR.getObj()%>_F1" data-options="label:'標準仕入先コード',labelWidth:130,required:true" style="width:200px;">
			</td>
			<td class="lbl_box">
				<input class="easyui-textbox_" tabindex="-1" id="<%=DefineReport.InpText.SIRKN.getObj()%>" for_inp="<%=DefineReport.InpText.SSIRCD.getObj()%>_F3" for_btn="<%=DefineReport.Button.SIR.getObj()%>_F3" readonly="readonly" data-options="readonly:true,editable:false"  style="width:370px;">
			</td>
			</tr>
			<tr>
			<td>
				<input class="easyui-numberbox_" tabindex="233" col="F32" id="<%=DefineReport.InpText.HSPTN.getObj()%>" check='<%=DefineReport.InpText.HSPTN.getMaxlenTag()%>' for_btn="<%=DefineReport.Button.HSPTN.getObj()%>_F1" data-options="label:'配送パターン',labelWidth:130,required:true" style="width:170px;">
			</td>
			<td class="lbl_box">
				<input class="easyui-textbox_" tabindex="-1" id="<%=DefineReport.InpText.HSPTNKN.getObj()%>" for_inp="<%=DefineReport.InpText.HSPTN.getObj()%>_F2" for_btn="<%=DefineReport.Button.HSPTN.getObj()%>_F2" readonly="readonly" data-options="readonly:true,editable:false"  style="width:370px;">
			</td>
			</tr>
			<tr>
			<td colspan=2>
				<select class="easyui-combobox_" tabindex="237" col="F107" id="<%=DefineReport.Select.READTMPTN.getObj()%>" data-options="label:'リードタイムパターン',labelWidth:130" style="width:500px;"></select>
			</td>
			</tr>
			</table>
			<table class="t-layout">
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
						<td class="col_tit">発注不可</td>
						<td class="col_chk"><input type="checkbox" tabindex="240" col="F100" id="chk_hat_monkbn"/></td>
						<td class="col_chk"><input type="checkbox" tabindex="241" col="F101" id="chk_hat_tuekbn"/></td>
						<td class="col_chk"><input type="checkbox" tabindex="242" col="F102" id="chk_hat_wedkbn"/></td>
						<td class="col_chk"><input type="checkbox" tabindex="243" col="F103" id="chk_hat_thukbn"/></td>
						<td class="col_chk"><input type="checkbox" tabindex="244" col="F104" id="chk_hat_frikbn"/></td>
						<td class="col_chk"><input type="checkbox" tabindex="245" col="F105" id="chk_hat_satkbn"/></td>
						<td class="col_chk"><input type="checkbox" tabindex="246" col="F106" id="chk_hat_sunkbn"/></td>
					</tr>
					<tr>
						<td class="col_tit">リードタイム</td>
						<td class="col_lbl_r"><span for_inp="<%=DefineReport.Select.READTMPTN.getObj()%>_F3"></span></td>
						<td class="col_lbl_r"><span for_inp="<%=DefineReport.Select.READTMPTN.getObj()%>_F4"></span></td>
						<td class="col_lbl_r"><span for_inp="<%=DefineReport.Select.READTMPTN.getObj()%>_F5"></span></td>
						<td class="col_lbl_r"><span for_inp="<%=DefineReport.Select.READTMPTN.getObj()%>_F6"></span></td>
						<td class="col_lbl_r"><span for_inp="<%=DefineReport.Select.READTMPTN.getObj()%>_F7"></span></td>
						<td class="col_lbl_r"><span for_inp="<%=DefineReport.Select.READTMPTN.getObj()%>_F8"></span></td>
						<td class="col_lbl_r"><span for_inp="<%=DefineReport.Select.READTMPTN.getObj()%>_F9"></span></td>
					</tr>
				</table>
			</td>
			<td style="vertical-align: top;width:180px;">
				<div class="inp_box">
					<select class="easyui-combobox_" tabindex="247" col="F108" id="<%=DefineReport.MeisyoSelect.KBN134.getObj()%>" data-options="label:'締め回数',labelWidth:60,required:true" style="width:180px;"></select>
				</div>
				<div class="inp_box">
					<select class="easyui-combobox_" tabindex="248" col="F99" id="<%=DefineReport.MeisyoSelect.KBN132.getObj()%>" data-options="label:'便',labelWidth:60,required:true" style="width:180px;"></select>
				</div>
			</td>
			</tr>
			</table>
		</td>
		<td style="vertical-align: top;">
			<div id="tt_4" class="easyui-tabs" data-options="plain:true,narrow:true,justified:true" style="height: 136px;width:420px;padding-bottom:2px;">
				<div title="レギュラー" class="box">
					<div class="inp_box">
						<input class="easyui-numberbox_" tabindex="251" col="F37" id="<%=DefineReport.InpText.RG_IDENFLG.getObj()%>" check='<%=DefineReport.InpText.RG_IDENFLG.getMaxlenTag()%>' for_inp="<%=DefineReport.InpText.SSIRCD.getObj()%>_F5" for_btn="<%=DefineReport.Button.SIR.getObj()%>_F5" data-options="label:'一括伝票フラグ',labelWidth:120,required:true" style="width:170px;">
						<span class="lbl_box">
							<input class="easyui-textbox_" tabindex="-1" id="<%=DefineReport.InpText.NMKN.getObj()%>" for_inp="<%=DefineReport.InpText.RG_IDENFLG.getObj()%>_TEXT2" readonly="readonly" data-options="readonly:true,editable:false" style="width:200px;">
						</span>
					</div>
					<div class="inp_box">
						<select class="easyui-combobox_" tabindex="252" col="F38" id="<%=DefineReport.MeisyoSelect.KBN108.getObj()%>" data-options="label:'ワッペン',labelWidth:120,required:false" style="width:300px;"></select>
					</div>
					<div class="inp_box" for_bmn="20,23,31,70,73">
						<select class="easyui-combobox_" tabindex="253" col="F117" id="<%=DefineReport.MeisyoSelect.KBN152.getObj()%>_r" data-options="label:'デリカワッペン区分',labelWidth:120" style="width:300px;"></select>
					</div>

				</div>
				<div title="販促" class="box">
					<div class="inp_box">
						<input class="easyui-numberbox_" tabindex="254" col="F44" id="<%=DefineReport.InpText.HS_SPOTMINSU.getObj()%>" check='<%=DefineReport.InpText.HS_SPOTMINSU.getMaxlenTag()%>' data-options="label:'スポット最低発注数',labelWidth:120,min:0" style="width:170px;text-align: right;">
					</div>
					<div class="inp_box">
						<select class="easyui-combobox_" tabindex="255" col="F43" id="<%=DefineReport.MeisyoSelect.KBN110.getObj()%>" data-options="label:'ワッペン',labelWidth:120" style="width:300px;"></select>
					</div>
					<div class="inp_box">
						<select class="easyui-combobox_" tabindex="256" col="F45" id="<%=DefineReport.MeisyoSelect.KBN111.getObj()%>" data-options="label:'特売ワッペン',labelWidth:120" style="width:300px;"></select>
					</div>
					<div style="display: none;">
					<!-- <div class="inp_box" for_bmn="20,23,31,70,73"> tabindex 103 → -1-->
						<select class="easyui-combobox_" tabindex="-1" col="F118" id="<%=DefineReport.MeisyoSelect.KBN152.getObj()%>_h" data-options="label:'デリカワッペン区分',labelWidth:120" style="width:300px;" disabled="disabled"></select>
					</div>
				</div>
			</div>
			<input class="easyui-numberbox_" tabindex="258" col="F74" id="<%=DefineReport.InpText.SIWAKEKBN.getObj()%>" check='<%=DefineReport.InpText.SIWAKEKBN.getMaxlenTag()%>' data-options="label:'仕分区分',labelWidth:70,min:0" style="width:120px;">
			<span for_bmn="20,23,31,70,73">
				<select class="easyui-combobox_" tabindex="259" col="F119" id="<%=DefineReport.MeisyoSelect.KBN153.getObj()%>" data-options="label:'取扱区分',labelWidth:70" style="width:200px;"></select>
			</span>
		</td>
		</tr>
		</table>
	</div>
	<div title="その他" style="display:none;padding:3px 8px;">
		<table class="t-layout">
		<tr><th style="max-width:250px;"></th><th style="max-width:250px;"></th><th style="max-width:250px;"></th><th style="max-width:250px;"></th></tr>
		<tr>
		<td colspan=3>
		</td>
		<td class="inp_box">
			<label class="chk_lbl"><input type="checkbox" tabindex="300" col="F109" id="chk_iryoreflg" style="vertical-align:middle;">衣料使い回し</label>
		</td>
		</tr>
		<tr>
		<td style="padding-right:8px;">
			<div class="inp_box">
				<select class="easyui-combobox_" tabindex="301" col="F54" id="<%=DefineReport.MeisyoSelect.KBN114.getObj()%>" data-options="label:'ＰＢ区分',labelWidth:60" style="width:220px;"></select>
			</div>
			<div class="inp_box">
				<select class="easyui-combobox_" tabindex="302" col="F73" id="<%=DefineReport.MeisyoSelect.KBN122.getObj()%>" data-options="label:'輸入区分',labelWidth:60,panelHeight:200" style="width:220px;"></select>
			</div>
		</td>
		<td style="padding-right:8px;">
			<div class="inp_box">
				<select class="easyui-combobox_" tabindex="303" col="F28" id="<%=DefineReport.MeisyoSelect.KBN104.getObj()%>" data-options="label:'市場区分',labelWidth:90" style="width:230px;"></select>
			</div>
			<div class="inp_box">
				<select class="easyui-combobox_" tabindex="304" col="F55" id="<%=DefineReport.MeisyoSelect.KBN115.getObj()%>" data-options="label:'小物区分',labelWidth:90" style="width:230px;"></select>
			</div>
		</td>
		<td style="padding-right:8px;">
			<div class="inp_box">
				<select class="easyui-combobox_" tabindex="305" col="F75" id="<%=DefineReport.MeisyoSelect.KBN123.getObj()%>" data-options="label:'返品区分',labelWidth:60" style="width:220px;"></select>
			</div>
			<div class="inp_box">
				<select class="easyui-combobox_" tabindex="306" col="F96" id="<%=DefineReport.MeisyoSelect.KBN130.getObj()%>" data-options="label:'裏貼',labelWidth:60" style="width:220px;"></select>
			</div>
		</td>
		<td rowspan=2 style="vertical-align: top;">
			<div style="height: 78px;width:180px;">
			<table id="dg10" class="like_datagrid">
			<thead class="no_header">
				<tr>
					<th style="width:120px;">分類</th>
					<th style="width: 58px;">コード</th>
				</tr>
			</thead>
			<tbody>
				<tr>
					<td class="col_tit">包材用途</td>
					<td class="col_num"><input class="easyui-numberbox_" tabindex="310" col="F82" id="<%=DefineReport.InpText.HZI_YOTO.getObj()%>" check='<%=DefineReport.InpText.HZI_YOTO.getMaxlenTag()%>' style="width:100%;" data-options="min:0" ></td>
				</tr>
				<tr>
					<td class="col_tit">包材材質</td>
					<td class="col_num"><input class="easyui-numberbox_" tabindex="311" col="F83" id="<%=DefineReport.InpText.HZI_ZAISHITU.getObj()%>" check='<%=DefineReport.InpText.HZI_ZAISHITU.getMaxlenTag()%>' style="width:100%;" data-options="min:0"></td>
				</tr>
				<tr>
					<td class="col_tit">包材リサイクル対象</td>
					<td class="col_chk"><input type="checkbox" tabindex="312" col="F84" id="chk_hzi_recycle"></td>
				</tr>
			</tbody>
			</table>
			</div>
		</td>
		</tr>
		<tr>
		<td>
			<div style="height:127px;width:182px;">
			<table id="dg11" class="like_datagrid">
			<thead>
				<tr>
					<th style="width: 80px;"></th>
					<th style="width:100px;">対象をチェック</th>
				</tr>
			</thead>
			<tbody>
				<tr>
					<td class="col_tit">ＥＬＰ</td>
					<td class="col_chk"><input type="checkbox" tabindex="313" col="F78" id="chk_elpflg"></td>
				</tr>
				<tr>
					<td class="col_tit">ベルマーク</td>
					<td class="col_chk"><input type="checkbox" tabindex="314" col="F79" id="chk_bellmarkflg"></td>
				</tr>
				<tr>
					<td class="col_tit">リサイクル</td>
					<td class="col_chk"><input type="checkbox" tabindex="315" col="F80" id="chk_recycleflg"></td>
				</tr>
				<tr>
					<td class="col_tit">エコマーク</td>
					<td class="col_chk"><input type="checkbox" tabindex="316" col="F81" id="chk_ecoflg"></td>
				</tr>
			</tbody>
			</table>
			</div>
		</td>
		<td style="vertical-align: top;">
			<div class="inp_box">
				<input class="easyui-numberbox_" tabindex="320" col="F70" id="<%=DefineReport.InpText.SEIZOGENNISU.getObj()%>" check='<%=DefineReport.InpText.SEIZOGENNISU.getMaxlenTag()%>' data-options="label:'製造限度日数',labelWidth:90,min:0" style="width:230px;text-align: right;">
			</div>
			<div class="inp_box">
				<input class="easyui-numberbox_" tabindex="321" col="F76" id="<%=DefineReport.InpText.TAISHONENSU.getObj()%>" check='<%=DefineReport.InpText.TAISHONENSU.getMaxlenTag()%>' data-options="label:'対象年齢',labelWidth:90,min:0" style="width:230px;text-align: right;">
			</div>
			<div class="inp_box">
				<input class="easyui-numberbox_" tabindex="322" col="F77" id="<%=DefineReport.InpText.CALORIESU.getObj()%>" check='<%=DefineReport.InpText.CALORIESU.getMaxlenTag()%>' data-options="label:'カロリー表示',labelWidth:90,min:0" style="width:230px;text-align: right;">
			</div>
		</td>
		<td style="vertical-align: top;">
			<div class="inp_box">
				<select class="easyui-combobox_" tabindex="323" col="F86" id="<%=DefineReport.MeisyoSelect.KBN129.getObj()%>" data-options="label:'酒級',labelWidth:60,panelHeight:200" style="width:220px;"></select>
			</div>
			<div class="inp_box">
			<input class="easyui-textbox_" tabindex="324" col="F87" id="<%=DefineReport.InpText.DOSU.getObj()%>" check='<%=DefineReport.InpText.DOSU.getMaxlenTag()%>' data-options="label:'度数',labelWidth:60" style="width:220px;">
			</div>
		</td>
		</tr>
		<tr>
		<td style="padding:9px 8px 0 0;vertical-align:top">
			<div style="height:127px;max-width:234px;">
			<table id="dg12" class="like_datagrid">
			<thead>
				<tr>
					<th style="width: 60px;"></th>
					<th style="width:100px;">商品サイズ</th>
					<th style="width: 60px;">単位</th>
				</tr>
			</thead>
			<tbody>
				<tr>
					<td class="col_tit">縦</td>
					<td class="col_num"><input class="easyui-numberbox_" tabindex="325" col="F51" id="<%=DefineReport.InpText.SHNTATESZ.getObj()%>" check='<%=DefineReport.InpText.SHNTATESZ.getMaxlenTag()%>' style="width:100%;" data-options="min:0"></td>
					<td class="col_lbl">mm</td>
				</tr>
				<tr>
					<td class="col_tit">横</td>
					<td class="col_num"><input class="easyui-numberbox_" tabindex="326" col="F50" id="<%=DefineReport.InpText.SHNYOKOSZ.getObj()%>" check='<%=DefineReport.InpText.SHNYOKOSZ.getMaxlenTag()%>' style="width:100%;" data-options="min:0"></td>
					<td class="col_lbl">mm</td>
				</tr>
				<tr>
					<td class="col_tit">奥行</td>
					<td class="col_num"><input class="easyui-numberbox_" tabindex="327" col="F52" id="<%=DefineReport.InpText.SHNOKUSZ.getObj()%>" check='<%=DefineReport.InpText.SHNOKUSZ.getMaxlenTag()%>' style="width:100%;" data-options="min:0"></td>
					<td class="col_lbl">mm</td>
				</tr>
				<tr>
					<td class="col_tit">重量</td>
					<td class="col_num"><input class="easyui-numberbox_" tabindex="328" col="F53" id="<%=DefineReport.InpText.SHNJRYOSZ.getObj()%>" check='<%=DefineReport.InpText.SHNJRYOSZ.getMaxlenTag()%>' style="width:100%;" data-options="min:0"></td>
					<td class="col_lbl">g</td>
				</tr>
			</tbody>
			</table>
			</div>
		</td>
		<td colspan="3">
			<table class="t-layout">
			<tr><th style="max-width:300px;"></th><th style="max-width:450px;"></th></tr>
			<tr>
			<td style="padding-right:8px;">
				<fieldset>
				<legend>ＯＤＳ</legend>
					<table class="t-layout">
					<tr><th style="width:100px;"></th><th style="width:180px;"></th></tr>
					<tr>
					<td style="padding-right: 5px;">
						<div style="height:128px;width:95px;">
						<table id="dg13" class="like_datagrid">
						<thead>
							<tr>
								<th style="width:30px;"></th>
								<th style="width:63px;">賞味期限</th>
							</tr>
						</thead>
						<tbody>
							<tr>
								<td class="col_tit">春</td>
								<td class="col_num"><input class="easyui-numberbox_" tabindex="330" col="F58" id="<%=DefineReport.InpText.ODS_HARUSU.getObj()%>" check='<%=DefineReport.InpText.ODS_HARUSU.getMaxlenTag()%>' style="width:100%;" data-options="min:0"></td>
							</tr>
							<tr>
								<td class="col_tit">夏</td>
								<td class="col_num"><input class="easyui-numberbox_" tabindex="331" col="F59" id="<%=DefineReport.InpText.ODS_NATSUSU.getObj()%>" check='<%=DefineReport.InpText.ODS_NATSUSU.getMaxlenTag()%>' style="width:100%;" data-options="min:0"></td>
							</tr>
							<tr>
								<td class="col_tit">秋</td>
								<td class="col_num"><input class="easyui-numberbox_" tabindex="332" col="F60" id="<%=DefineReport.InpText.ODS_AKISU.getObj()%>" check='<%=DefineReport.InpText.ODS_AKISU.getMaxlenTag()%>' style="width:100%;" data-options="min:0"></td>
							</tr>
							<tr>
								<td class="col_tit">冬</td>
								<td class="col_num"><input class="easyui-numberbox_" tabindex="333" col="F61" id="<%=DefineReport.InpText.ODS_FUYUSU.getObj()%>" check='<%=DefineReport.InpText.ODS_FUYUSU.getMaxlenTag()%>' style="width:100%;" data-options="min:0"></td>
							</tr>
						</tbody>
						</table>
						</div>
					</td>
					<td style="vertical-align: top;">
						<div style="height:77px;width:180px;">
						<table id="dg14" class="like_datagrid">
						<thead>
							<tr>
								<th style="width: 70px;">期間</th>
								<th style="width:108px;">Ｄ－Ｂ</th>
							</tr>
						</thead>
						<tbody>
							<tr>
								<td class="col_tit">入荷期限</td>
								<td class="col_num"><input class="easyui-numberbox_" tabindex="335" col="F62" id="<%=DefineReport.InpText.ODS_NYUKASU.getObj()%>" check='<%=DefineReport.InpText.ODS_NYUKASU.getMaxlenTag()%>' style="width:100%;" data-options="min:0"></td>
							</tr>
							<tr>
								<td class="col_tit">値引期限</td>
								<td class="col_num"><input class="easyui-numberbox_" tabindex="336" col="F63" id="<%=DefineReport.InpText.ODS_NEBIKISU.getObj()%>" check='<%=DefineReport.InpText.ODS_NEBIKISU.getMaxlenTag()%>' style="width:100%;" data-options="min:0"></td>
							</tr>
						</tbody>
						</table>
						</div>
					</td>
					</tr>
					</table>
				</fieldset>
			</td>
			<td style="vertical-align: top;">
				<table class="t-layout">
				<tr>
				<td style="vertical-align: top;padding-top:2px">
					<div style="height:142px;width:450px;display:none;">
					<!-- tabindex:135→-1 -->
					<table id="<%=DefineReport.Grid.ALLERGY.getObj()%>" class="easyui-datagrid_" tabindex="-1"
						data-options="singleSelect:true,checkOnSelect:false,selectOnCheck:false,rownumbers:true,fit:true" disabled="disabled">
					<thead>
						<tr>
							<th data-options="field:'CHK',checkbox:true">取扱う</th>
							<th data-options="field:'TEXT',width:373,halign:'center',align:'left'">アレルギー</th>

						</tr>
					</thead>
					</table>
					</div>
				</td>
				</tr>
				<tr>
				<td style="vertical-align: top;">
					<fieldset>
					<legend>eBASE</legend>
						<!-- tabindex:136,137→-1 TODO:有効時に戻すこと！-->
						<a href="#" class="easyui-linkbutton" tabindex="-1" id="<%=DefineReport.Button.TENKABUTSU.getObj()%>" title="<%=DefineReport.Button.TENKABUTSU.getTxt()%>" iconCls="icon-edit" style="width:110px;" data-options="disabled:true" disabled="disabled"><span><%=DefineReport.Button.TENKABUTSU.getTxt()%></span></a>
						<a href="#" class="easyui-linkbutton" tabindex="-1" id="<%=DefineReport.Button.ALLERGY.getObj()%>" title="<%=DefineReport.Button.ALLERGY.getTxt()%>" iconCls="icon-edit" style="width:110px;" data-options="disabled:true" disabled="disabled"><span><%=DefineReport.Button.ALLERGY.getTxt()%></span></a>
					</fieldset>
				</td>
				</tr>
				<tr>
				<td style="vertical-align: bottom;padding-top:20px;">
					<fieldset>
					<legend>自動発注停止(店別)</legend>
						<!-- tabindex:138→-1→339 TODO:有効時に戻すこと！-->
						<a href="#" class="easyui-linkbutton" tabindex="-1" id="<%=DefineReport.Button.SUB.getObj()%>_winIT032" title="自動発注停止" iconCls="icon-edit" style="width:110px;" data-options="disabled:true" disabled="disabled"><span>登録・変更</span></a>
					</fieldset>
				</td>
				</tr>
				</table>
			</td>
			</tr>
			</table>
		</td>
		</tr>
		<tr>
		<td style="padding-right:8px;">
			<fieldset>
			<legend>陳列情報</legend>
				<div style="height:153px;width:222px;">
					<table id="dg16" class="like_datagrid">
					<thead class="no_header">
						<tr>
							<th style="width:130px;"></th>
							<th style="width: 90px;"></th>
						</tr>
					</thead>
					<tbody>
						<tr>
							<td class="col_tit">陳列形式コード</td>
							<td class="col_txt"><input class="easyui-textbox_" tabindex="340" col="F88" id="<%=DefineReport.InpText.CHINRETUCD.getObj()%>" check='<%=DefineReport.InpText.CHINRETUCD.getMaxlenTag()%>' style="width:100%;"></td>
						</tr>
						<tr>
							<td class="col_tit">段積み形式コード</td>
							<td class="col_txt"><input class="easyui-textbox_" tabindex="341" col="F89" id="<%=DefineReport.InpText.DANTUMICD.getObj()%>" check='<%=DefineReport.InpText.DANTUMICD.getMaxlenTag()%>' style="width:100%;"></td>
						</tr>
						<tr>
							<td class="col_tit">重なりコード</td>
							<td class="col_txt"><input class="easyui-textbox_" tabindex="342" col="F90" id="<%=DefineReport.InpText.KASANARICD.getObj()%>" check='<%=DefineReport.InpText.KASANARICD.getMaxlenTag()%>' style="width:100%;"></td>
						</tr>
						<tr>
							<td class="col_tit">重なりサイズ</td>
							<td class="col_num"><input class="easyui-numberbox_" tabindex="343" col="F91" id="<%=DefineReport.InpText.KASANARISZ.getObj()%>" check='<%=DefineReport.InpText.KASANARISZ.getMaxlenTag()%>' style="width:100%;" data-options="min:0"></td>
						</tr>
						<tr>
							<td class="col_tit">圧縮率</td>
							<td class="col_num"><input class="easyui-numberbox_" tabindex="344" col="F92" id="<%=DefineReport.InpText.ASSHUKURT.getObj()%>" check='<%=DefineReport.InpText.ASSHUKURT.getMaxlenTag()%>' style="width:100%;" data-options="min:0"></td>
						</tr>
						<tr>
							<td class="col_tit">種別コード</td>
							<td class="col_txt"><input class="easyui-textbox_" tabindex="345" col="F93" id="<%=DefineReport.InpText.SHUBETUCD.getObj()%>" check='<%=DefineReport.InpText.SHUBETUCD.getMaxlenTag()%>' style="width:100%;"></td>
						</tr>
					</tbody>
					</table>
				</div>
			</fieldset>
		</td>
		<td colspan="3" style="vertical-align: bottom;">
			<div style="height:167px;width: 750px;display:none;">
				<!-- tabindex:145→-1 -->
				<table id="<%=DefineReport.Grid.TENKABUTSU.getObj()%>" class="easyui-datagrid_" tabindex="-1" data-options="singleSelect:true,rownumbers:true,fit:true">
				<thead>
					<tr>
						<th data-options="field:'KENSAKU',width:101,halign:'center',align:'left',editor:'textbox'">検索文字</th>
						<th data-options="field:'TENKABCD',width:600,halign:'center',align:'left',editor:'combobox'">添加物</th>
					</tr>
				</thead>
				</table>
				<!-- Editor参照用 -->
				<div class="ref_editor" style="display: none;">
					<input class="easyui-textbox_" tabindex="-1" id="<%=DefineReport.InpText.KENSAKU.getObj()%>" check='<%=DefineReport.InpText.KENSAKU.getMaxlenTag()%>'/>
					<select class="easyui-combobox_" tabindex="-1" id="<%=DefineReport.MeisyoSelect.KBN138.getObj()%>" for_inp="<%=DefineReport.InpText.KENSAKU.getObj()%>" data-options="panelHeight:150"></select>
				</div>
			</div>
		</td>
		</tr>
		</table>
	</div>
	</div>
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
		<td><a href="#" title="戻る" id="<%=DefineReport.Button.BACK.getObj()%>" class="easyui-linkbutton" tabindex="400" iconCls="icon-undo" style="width:110px;"><span class="btnTxt">戻る</span></a></td>
		<td></td>
		<td><a href="#" class="easyui-linkbutton" tabindex="401" id="<%=DefineReport.Button.CANCEL.getObj()%>" title="<%=DefineReport.Button.CANCEL.getTxt()%>" iconCls="icon-cancel" style="width:110px;"><span>F1:キャンセル</span></a></td>
		<td><a href="#" class="easyui-linkbutton" tabindex="402" id="<%=DefineReport.Button.UPD.getObj()%>" title="<%=DefineReport.Button.UPD.getTxt()%>" iconCls="icon-save" style="width:110px;"><span class="btnTxt">F12:<%=DefineReport.Button.UPD.getTxt()%></span></a></td>
		<td><a href="#" class="easyui-linkbutton" tabindex="403" id="<%=DefineReport.Button.DEL.getObj()%>" title="<%=DefineReport.Button.DEL.getTxt()%>" iconCls="icon-remove" style="width:110px;"><span class="btnTxt"><%=DefineReport.Button.DEL.getTxt()%></span></a></td>
		</tr>
		</table>
	</div>
	<div id="disp_record_info" style="float: right;">
		<span class="labelName" tabindex="-1" style="padding-top: 3px;">
			登録日 <span col="F114" id="<%=DefineReport.Text.ADDDT.getObj()%>"></span>　更新日 <span col="F115" id="<%=DefineReport.Text.UPDDT.getObj()%>"></span>　オペレータ <span col="F113" id="<%=DefineReport.Text.OPERATOR.getObj()%>"></span>
		</span>
		<input type="hidden" col="F127" name="<%=DefineReport.Hidden.UPDDT.getObj()%>" id="<%=DefineReport.Hidden.UPDDT.getObj()%>" />
		<input type="hidden" name="<%=DefineReport.Text.SHORIDT.getObj()%>" id="<%=DefineReport.Text.SHORIDT.getObj()%>" />
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
	<jsp:include page="Out_Reportwin001.jsp" flush="true" />
	<jsp:include page="Out_Reportwin002.jsp" flush="true" />
	<jsp:include page="Out_Reportwin003.jsp" flush="true" />
	<jsp:include page="Out_Reportwin004.jsp" flush="true" />
	<jsp:include page="Out_ReportwinIT031.jsp" flush="true" />
	<jsp:include page="Out_ReportwinIT032.jsp" flush="true" />
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
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.x002.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.win001.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.win002.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.win003.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.win004.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.winIT031.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.winIT032.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.control.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.events.js?v=<%=prm %>"></script>
</html>