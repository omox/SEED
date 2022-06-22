
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
<style type="text/css">
<!--
.like_datagrid th, .like_datagrid td {padding: 0 2px;}
.like_datagrid_head td{
    background-color: #efefef;
    background: linear-gradient(to bottom,#F9F9F9 0,#efefef 100%);
    background-repeat: repeat-x;
    font-weight: normal;
    text-align: center;
}

.like_datagrid_head_top td{
	border-top-width: 1px;
}

.like_datagrid_head td.col_empty{
	background: #ffffff;
}

.noborder > td {
	border:0;
}
.bycd .textbox-text,.bycd .combobox-item{
	font-size: 9pt;
}
-->
</style>
</head>
<body style="overflow-x: hidden;">
<div id="cc" class="easyui-layout" data-options="fit:true">
<form id="ff" method="post" style="display: inline">
<div data-options="region:'north',border:false" style="display:none;padding:2px 5px 0;height:325px;">
	<table class="t-layout1">
	<tr>
		<th style="min-width:650px;"></th><th style="min-width:207px;"></th><th style="min-width:107px;"></th>
	</tr>
	<tr>
		<td colspan="3">
			<!-- 月締後変更処理 -->
			<div class="lbl_box" style="text-align: center;color:red;font-weight:bold;max-width: 1000px;">
				<span col="F1" id="<%=DefineReport.Text.STATUS.getObj()%>"></span>
			</div>
		</td>
	</tr>
	<tr>
		<td>
			<table class="dataTable" cellspacing="0" cellpadding="0" style="display:inline;">
			<tbody>
			<tr>
				<td class="labelCell" style="WIDTH: 35px;">週No.</td>
				<td class="labelCell" style="WIDTH: 90px;">催しコード</td>
				<td class="labelCell" style="WIDTH:300px;">催し名称</td>
				<td class="labelCell" style="WIDTH: 60px;">一日遅ﾊﾟﾀﾝ</td>
			</tr>
			<tr>
				<td class="col_lbl_l"><span col="F2" id="<%=DefineReport.InpText.SHUNO.getObj()%>">　</span></td>
				<td class="col_lbl_l"><span col="F3" id="<%=DefineReport.InpText.MOYSCD.getObj()%>"></span></td>
				<td class="col_lbl"  ><span col="F4" id="<%=DefineReport.InpText.MOYKN.getObj()%>"></span></td>
				<td class="col_lbl"  ><span col="F5" id="<%=DefineReport.InpText.HBOKUREFLG.getObj()%>"></span></td>
			</tr>
			</tbody></table>
		</td>
		<td></td>
		<td style="float: right;">
			<table class="dataTable" cellspacing="0" cellpadding="0" style="display:inline;">
			<tbody>
			<tr>
				<td class="labelCell" style="WIDTH: 60px;">一日遅ｽﾗｲﾄﾞしない</td>
			</tr>
			<tr>
				<td class="col_chk">
					<input type="checkbox" class="chkbox" tabindex="-1" col="F6" id="<%=DefineReport.Checkbox.HBSLIDEFLG.getObj()%>"><label class="chk_lbl" for="<%=DefineReport.Checkbox.HBSLIDEFLG.getObj()%>">販売</label>
					<input type="checkbox" class="chkbox" tabindex="-1" col="F7" for_kbn="1,2,4,5" id="<%=DefineReport.Checkbox.NHSLIDEFLG.getObj()%>"><label class="chk_lbl" for_kbn="1,2,4,5" for="<%=DefineReport.Checkbox.NHSLIDEFLG.getObj()%>">納入</label>
				</td>
			</tr>
			</tbody></table>
		</td>
	</tr>
	<tr>
		<td style="vertical-align: top;">
			<table class="dataTable" cellspacing="0" cellpadding="0" style="display:hidden;">
			<tbody>
			<tr>
				<td class="labelCell">部門</td>
				<td class="labelCell">BY</td>
				<td class="col_empty"></td>
				<td class="labelCell">商品コード</td>
				<td class="labelCell">商品マスタ名称</td>
				<td class="col_empty"></td>
				<td class="labelCell" style="WIDTH: 50px;">ｸﾞﾙｰﾌﾟNo.</td>
				<td class="labelCell" style="WIDTH: 30px;">子No.</td>
				<td class="labelCell" style="WIDTH: 30px;">日替</td>
			</tr>
			<tr>
				<td class="col_num"  ><span class="labelName" style="WIDTH:35px;text-align:left" col="F8" id="<%=DefineReport.InpText.BMNCD.getObj()%>"></span></td>
				<td class="col_lbl_c bycd"><select class="easyui-combobox_" tabindex="-1" col="F9" id="<%=DefineReport.Select.BYCD.getObj()%>" check='<%=DefineReport.InpText.BYCD.getMaxlenTag()%>' style="width:120px;"></select></td>
				<td class="col_empty"></td>
				<td class="col_lbl"  ><input class="easyui-numberbox_" tabindex="4" col="F10" id="<%=DefineReport.InpText.SHNCD.getObj()%>" check='<%=DefineReport.InpText.SHNCD.getMaxlenTag()%>' data-options="prompt:'____-____'" style="width:80px;text-align:left" for_btn="<%=DefineReport.Button.SHNCD.getObj()%>_F1"></td>
				<td class="col_lbl"  ><span  class="labelName" style="WIDTH:275px;" col="F11" id="<%=DefineReport.InpText.SHNKN.getObj()%>"></span></td>
				<td class="col_empty"></td>
				<td class="col_num"  ><input class="easyui-textbox_"   tabindex="5" col="F13" id="<%=DefineReport.InpText.PARNO.getObj()%>" check='<%=DefineReport.InpText.PARNO.getMaxlenTag()%>' style="width:50px;text-align:left"></td>
				<td class="col_num"  ><input class="easyui-numberbox_" tabindex="6" col="F14" id="<%=DefineReport.InpText.CHLDNO.getObj()%>" check='<%=DefineReport.InpText.CHLDNO.getMaxlenTag()%>' style="width:25px;text-align:left"></td>
				<td class="col_chk"  ><input type="checkbox" class="chkbox" tabindex="7" col="F15" id="<%=DefineReport.Checkbox.HIGAWRFLG.getObj()%>"><label class="chk_lbl" for="<%=DefineReport.Checkbox.HIGAWRFLG.getObj()%>"></label></td>
			</tr>
			</tbody>
			</table>
		</td>
		<td colspan="2" rowspan="2" style="vertical-align: top;">
			<table class="dataTable" cellspacing="0" cellpadding="0" style="display:hidden;">
			<tbody>
			<tr>
				<td class="labelCell" style="WIDTH:185px;">販売期間</td>
				<td class="col_empty" rowspan="2" style="vertical-align: top;">
					<a href="#" class="easyui-linkbutton" tabindex="-1" id="<%=DefineReport.Button.COPY.getObj()%>1" title="前複写" style="width:50px;"><span>前複写</span></a>
				</td>
				<td class="labelCell" style="WIDTH: 60px;">チラシ未掲載</td>
			</tr>
			<tr>
				<td class="col_txt">
					<input class="easyui-numberbox_" tabindex="10" col="F16" id="<%=DefineReport.InpText.HBSTDT.getObj()%>" check='<%=DefineReport.InpText.HBSTDT.getMaxlenTag()%>' data-options="prompt:'<%=DefineReport.Label.PROMPT_DTW.getTxt()%>'" style="width:88px;">
					～
					<input class="easyui-numberbox_" tabindex="11" col="F17" id="<%=DefineReport.InpText.HBEDDT.getObj()%>" check='<%=DefineReport.InpText.HBEDDT.getMaxlenTag()%>' data-options="prompt:'<%=DefineReport.Label.PROMPT_DTW.getTxt()%>'" style="width:88px;">
				</td>
				<td class="col_chk">
					<input type="checkbox" class="chkbox" tabindex="-1" col="F20" id="<%=DefineReport.Checkbox.CHIRASFLG.getObj()%>"><label class="chk_lbl" for="<%=DefineReport.Checkbox.CHIRASFLG.getObj()%>"></label>
				</td>
			</tr>
			<tr>
				<td class="labelCell" style="WIDTH:185px;">納入期間</td>
				<td class="col_empty"></td>
			</tr>
			<tr>
				<td class="col_txt">
					<input class="easyui-numberbox_" tabindex="12" col="F18" id="<%=DefineReport.InpText.NNSTDT.getObj()%>" check='<%=DefineReport.InpText.NNSTDT.getMaxlenTag()%>' data-options="prompt:'<%=DefineReport.Label.PROMPT_DTW.getTxt()%>'" style="width:88px;">
					～
					<input class="easyui-numberbox_" tabindex="13" col="F19" id="<%=DefineReport.InpText.NNEDDT.getObj()%>" check='<%=DefineReport.InpText.NNEDDT.getMaxlenTag()%>' data-options="prompt:'<%=DefineReport.Label.PROMPT_DTW.getTxt()%>'" style="width:88px;">
				</td>
				<td class="col_empty"></td>
			</tr>
			</tbody></table>
			<table class="dataTable" for_kbn="2,3,4,5" cellspacing="0" cellpadding="0" style="display:hidden;">
			<tbody>
			<tr>
				<td class="col_empty" colspan="4" style="text-align: right;">単位：千円</td>
			</tr>
			<tr>
				<td class="labelCell" style="text-align:center">予定数</td>
				<td class="labelCell" style="WIDTH: 50px;text-align:center">仕入額</td>
				<td class="labelCell" style="WIDTH: 50px;text-align:center">販売額</td>
				<td class="labelCell" style="WIDTH: 50px;text-align:center">荒利額</td>
			</tr>
			<tr>
				<td class="col_num"><input class="easyui-numberbox_" tabindex="60" col="F53" id="<%=DefineReport.InpText.HBYOTEISU.getObj()%>" check='<%=DefineReport.InpText.HBYOTEISU.getMaxlenTag()%>' data-options="min:0" style="width:58px;"></td>
				<td class="col_lbl_r"><span col="F54"></span></td>
				<td class="col_lbl_r"><span col="F55"></span></td>
				<td class="col_lbl_r"><span col="F56"></span></td>
			</tr>
			</tbody>
			</table>
		</td>
	</tr>
	<tr>
		<td>
			<table class="t-layout1">
			<tbody>
			<tr>
				<td style="vertical-align: top;">
					<a href="#" class="easyui-linkbutton" tabindex="-1" id="<%=DefineReport.Button.COPY.getObj()%>2" title="前複写" style="width:50px;"><span>前複写</span></a>
				</td>
				<td style="vertical-align: top;">
					<a href="#" class="easyui-linkbutton" tabindex="-1" id="<%=DefineReport.Button.RANKNO.getObj()%>_add_a" title="対象店" style="width:50px;"><span>対象店</span></a>
					<input class="easyui-numberbox_" tabindex="22" col="F21" id="<%=DefineReport.InpText.RANKNO_ADD.getObj()%>" check='<%=DefineReport.InpText.RANKNO_ADD.getMaxlenTag()%>' for_btn="<%=DefineReport.Button.RANKNO.getObj()%>_add_a_F1" style="width:37px;">
					<span style="display: none;" col="F155" id="<%=DefineReport.InpText.RANKNO_ADD.getObj()%>_nm" for_btn="<%=DefineReport.Button.RANKNO.getObj()%>_add_a_F2"></span>
					<br>
					<a href="#" class="easyui-linkbutton" tabindex="-1" id="<%=DefineReport.Button.RANKNO.getObj()%>_del" title="除外店" style="width:50px;"><span>除外店</span></a>
					<input class="easyui-numberbox_" tabindex="24" col="F22" id="<%=DefineReport.InpText.RANKNO_DEL.getObj()%>" check='<%=DefineReport.InpText.RANKNO_DEL.getMaxlenTag()%>' for_btn="<%=DefineReport.Button.RANKNO.getObj()%>_del_F1" style="width:37px;">
					<span style="display: none;" col="F156" id="<%=DefineReport.InpText.RANKNO_DEL.getObj()%>_nm" for_btn="<%=DefineReport.Button.RANKNO.getObj()%>_del_F2"></span>
				</td>
				<td>
					<table id="spread_tjten" class="like_datagrid">
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
						<th style="max-width: 40px"></th>
					</tr>
					</thead>
					<tbody>
					<tr>
						<td class="col_tit">追加</td>
						<td class="col_num"><input class="easyui-numberbox_" tabindex="-1" tabindex-bk="30" col="F23" id="<%=DefineReport.InpText.TENCD.getObj()%>_add1"  check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' style="width:35px;text-align:left"></td>
						<td class="col_num"><input class="easyui-numberbox_" tabindex="-1" tabindex-bk="31" col="F24" id="<%=DefineReport.InpText.TENCD.getObj()%>_add2"  check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' style="width:35px;text-align:left"></td>
						<td class="col_num"><input class="easyui-numberbox_" tabindex="-1" tabindex-bk="32" col="F25" id="<%=DefineReport.InpText.TENCD.getObj()%>_add3"  check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' style="width:35px;text-align:left"></td>
						<td class="col_num"><input class="easyui-numberbox_" tabindex="-1" tabindex-bk="33" col="F26" id="<%=DefineReport.InpText.TENCD.getObj()%>_add4"  check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' style="width:35px;text-align:left"></td>
						<td class="col_num"><input class="easyui-numberbox_" tabindex="-1" tabindex-bk="34" col="F27" id="<%=DefineReport.InpText.TENCD.getObj()%>_add5"  check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' style="width:35px;text-align:left"></td>
						<td class="col_num"><input class="easyui-numberbox_" tabindex="-1" tabindex-bk="35" col="F28" id="<%=DefineReport.InpText.TENCD.getObj()%>_add6"  check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' style="width:35px;text-align:left"></td>
						<td class="col_num"><input class="easyui-numberbox_" tabindex="-1" tabindex-bk="36" col="F29" id="<%=DefineReport.InpText.TENCD.getObj()%>_add7"  check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' style="width:35px;text-align:left"></td>
						<td class="col_num"><input class="easyui-numberbox_" tabindex="-1" tabindex-bk="37" col="F30" id="<%=DefineReport.InpText.TENCD.getObj()%>_add8"  check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' style="width:35px;text-align:left"></td>
						<td class="col_num"><input class="easyui-numberbox_" tabindex="-1" tabindex-bk="38" col="F31" id="<%=DefineReport.InpText.TENCD.getObj()%>_add9"  check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' style="width:35px;text-align:left"></td>
						<td class="col_num"><input class="easyui-numberbox_" tabindex="-1" tabindex-bk="39" col="F32" id="<%=DefineReport.InpText.TENCD.getObj()%>_add10" check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' style="width:35px;text-align:left"></td>
					</tr>
					<tr>
						<td class="col_tit">ランク</td>
						<td class="col_num"><input class="easyui-textbox_" tabindex="-1" tabindex-bk="40" col="F33" id="<%=DefineReport.InpText.TENRANK.getObj()%>1"  check='<%=DefineReport.InpText.RANK.getMaxlenTag()%>' style="width:35px;text-align:left"></td>
						<td class="col_num"><input class="easyui-textbox_" tabindex="-1" tabindex-bk="41" col="F34" id="<%=DefineReport.InpText.TENRANK.getObj()%>2"  check='<%=DefineReport.InpText.RANK.getMaxlenTag()%>' style="width:35px;text-align:left"></td>
						<td class="col_num"><input class="easyui-textbox_" tabindex="-1" tabindex-bk="42" col="F35" id="<%=DefineReport.InpText.TENRANK.getObj()%>3"  check='<%=DefineReport.InpText.RANK.getMaxlenTag()%>' style="width:35px;text-align:left"></td>
						<td class="col_num"><input class="easyui-textbox_" tabindex="-1" tabindex-bk="43" col="F36" id="<%=DefineReport.InpText.TENRANK.getObj()%>4"  check='<%=DefineReport.InpText.RANK.getMaxlenTag()%>' style="width:35px;text-align:left"></td>
						<td class="col_num"><input class="easyui-textbox_" tabindex="-1" tabindex-bk="44" col="F37" id="<%=DefineReport.InpText.TENRANK.getObj()%>5"  check='<%=DefineReport.InpText.RANK.getMaxlenTag()%>' style="width:35px;text-align:left"></td>
						<td class="col_num"><input class="easyui-textbox_" tabindex="-1" tabindex-bk="45" col="F38" id="<%=DefineReport.InpText.TENRANK.getObj()%>6"  check='<%=DefineReport.InpText.RANK.getMaxlenTag()%>' style="width:35px;text-align:left"></td>
						<td class="col_num"><input class="easyui-textbox_" tabindex="-1" tabindex-bk="46" col="F39" id="<%=DefineReport.InpText.TENRANK.getObj()%>7"  check='<%=DefineReport.InpText.RANK.getMaxlenTag()%>' style="width:35px;text-align:left"></td>
						<td class="col_num"><input class="easyui-textbox_" tabindex="-1" tabindex-bk="47" col="F40" id="<%=DefineReport.InpText.TENRANK.getObj()%>8"  check='<%=DefineReport.InpText.RANK.getMaxlenTag()%>' style="width:35px;text-align:left"></td>
						<td class="col_num"><input class="easyui-textbox_" tabindex="-1" tabindex-bk="48" col="F41" id="<%=DefineReport.InpText.TENRANK.getObj()%>9"  check='<%=DefineReport.InpText.RANK.getMaxlenTag()%>' style="width:35px;text-align:left"></td>
						<td class="col_num"><input class="easyui-textbox_" tabindex="-1" tabindex-bk="49" col="F42" id="<%=DefineReport.InpText.TENRANK.getObj()%>10" check='<%=DefineReport.InpText.RANK.getMaxlenTag()%>' style="width:35px;text-align:left"></td>
					</tr>
					<tr>
						<td class="col_tit">除外</td>
						<td class="col_num"><input class="easyui-numberbox_" tabindex="-1" tabindex-bk="50" col="F43" id="<%=DefineReport.InpText.TENCD.getObj()%>_del1"  check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' style="width:35px;text-align:left"></td>
						<td class="col_num"><input class="easyui-numberbox_" tabindex="-1" tabindex-bk="51" col="F44" id="<%=DefineReport.InpText.TENCD.getObj()%>_del2"  check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' style="width:35px;text-align:left"></td>
						<td class="col_num"><input class="easyui-numberbox_" tabindex="-1" tabindex-bk="52" col="F45" id="<%=DefineReport.InpText.TENCD.getObj()%>_del3"  check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' style="width:35px;text-align:left"></td>
						<td class="col_num"><input class="easyui-numberbox_" tabindex="-1" tabindex-bk="53" col="F46" id="<%=DefineReport.InpText.TENCD.getObj()%>_del4"  check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' style="width:35px;text-align:left"></td>
						<td class="col_num"><input class="easyui-numberbox_" tabindex="-1" tabindex-bk="54" col="F47" id="<%=DefineReport.InpText.TENCD.getObj()%>_del5"  check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' style="width:35px;text-align:left"></td>
						<td class="col_num"><input class="easyui-numberbox_" tabindex="-1" tabindex-bk="55" col="F48" id="<%=DefineReport.InpText.TENCD.getObj()%>_del6"  check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' style="width:35px;text-align:left"></td>
						<td class="col_num"><input class="easyui-numberbox_" tabindex="-1" tabindex-bk="56" col="F49" id="<%=DefineReport.InpText.TENCD.getObj()%>_del7"  check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' style="width:35px;text-align:left"></td>
						<td class="col_num"><input class="easyui-numberbox_" tabindex="-1" tabindex-bk="57" col="F50" id="<%=DefineReport.InpText.TENCD.getObj()%>_del8"  check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' style="width:35px;text-align:left"></td>
						<td class="col_num"><input class="easyui-numberbox_" tabindex="-1" tabindex-bk="58" col="F51" id="<%=DefineReport.InpText.TENCD.getObj()%>_del9"  check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' style="width:35px;text-align:left"></td>
						<td class="col_num"><input class="easyui-numberbox_" tabindex="-1" tabindex-bk="59" col="F52" id="<%=DefineReport.InpText.TENCD.getObj()%>_del10" check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' style="width:35px;text-align:left"></td>
					</tr>
					</tbody>
					</table>
				</td>
			</tr>
			</tbody></table>
		</td>
	</tr>
	</tbody></table>
	<table class="t-layout1">
	<tr for_kbn="4,5">
		<td style="vertical-align: top;">
			<label class="rad_lbl"><input type="radio" tabindex="-1" col="F57" name="<%=DefineReport.Radio.TKANPLUKBN.getObj()%>" id="<%=DefineReport.Radio.TKANPLUKBN.getObj()%>1" value="<%=DefineReport.ValKbn10414.VAL1.getVal()%>" style="vertical-align:middle;"/>PLU商品・定貫商品</label>
		</td>
		<td style="vertical-align: top;">
			<label class="rad_lbl"><input type="radio" tabindex="-1" col="F57" name="<%=DefineReport.Radio.TKANPLUKBN.getObj()%>" id="<%=DefineReport.Radio.TKANPLUKBN.getObj()%>2" value="<%=DefineReport.ValKbn10414.VAL2.getVal()%>" style="vertical-align:middle;"/>不定貫商品</label>
		</td>
	</tr>
	<tr>
		<td style="vertical-align: top;">
			<table class="t-layout1">
			<tbody>
			<tr>
				<td style="vertical-align: top;padding-right:5px;">
					<table class="like_datagrid" for_kbn="2,4,5">
					<thead>
						<tr>
						<th style="width: 60px">　</th>
						<th>原価</th>
						<th>A総売価</th>
						<th style="width: 70px">本体売価</th>
						<th>入数</th>
						<th style="width: 40px">値入</th>
						</tr>
					</thead>
					<tbody>
						<tr id="spread_reg">
							<td class="col_tit">レギュラー</td>
							<td class="col_lbl_r"><span col="F58"></span></td>
							<td class="col_lbl_r"><span col="F59"></span></td>
							<td class="col_lbl_r"><span col="F60"></span></td>
							<td class="col_lbl_r"><span col="F61"></span></td>
							<td class="col_lbl_r"><span col="F62"></span></td>
						</tr>
						<tr id="spread_mae">
							<td class="col_tit">特売事前</td>
							<td class="col_num"><input class="easyui-numberbox_" tabindex="63" col="F63" name='genka' id="<%=DefineReport.InpText.GENKAAM_MAE.getObj()%>" check='<%=DefineReport.InpText.GENKAAM_MAE.getMaxlenTag()%>' data-options="min:1" style="width:78px;"></td>
							<td class="col_num"><input class="easyui-numberbox_" tabindex="65" col="F64" id="<%=DefineReport.InpText.A_BAIKAAM.getObj()%>1" check='<%=DefineReport.InpText.A_BAIKAAM.getMaxlenTag()%>' data-options="min:1" style="width:58px;"></td>
							<td class="col_lbl_r"><span col="F65"></span></td>
							<td class="col_num"><input class="easyui-numberbox_" tabindex="66" col="F66" id="<%=DefineReport.InpText.IRISU.getObj()%>1" check='<%=DefineReport.InpText.IRISU.getMaxlenTag()%>' data-options="min:1" style="width:35px;"></td>
							<td class="col_lbl_r"><span col="F67" id="<%=DefineReport.Text.RG_NEIRE.getObj()%>" ></span></td>
						</tr>
						<tr id="spread_ato">
							<td class="col_tit">特売追加</td>
							<td class="col_num"><input class="easyui-numberbox_" tabindex="64" col="F68" name='genka' id="<%=DefineReport.InpText.GENKAAM_ATO.getObj()%>" check='<%=DefineReport.InpText.GENKAAM_ATO.getMaxlenTag()%>' style="width:78px;"></td>
							<td class="col_lbl_r"><span col="F69"></span></td>
							<td class="col_lbl_r"><span col="F70"></span></td>
							<td class="col_lbl_r"><span col="F71"></span></td>
							<td class="col_lbl_r"><span col="F72"></span></td>
						</tr>
					</table>
					<table class="dataTable" for_kbn="1" cellspacing="0" cellpadding="0" style="display:hidden;">
					<tbody>
					<tr>
						<td class="labelCell">A総売価</td>
						<td class="labelCell">B総売価</td>
						<td class="labelCell">C総売価</td>
					</tr>
					<tr id="spread_sobaika">
						<td class="col_lbl_c"><select class="easyui-combobox_" tabindex="67" col="F100" id="<%=DefineReport.MeisyoSelect.KBN10656.getObj()%>_a" style="width:90px;"></select></td>
						<td class="col_lbl_c"><select class="easyui-combobox_" tabindex="68" col="F101" id="<%=DefineReport.MeisyoSelect.KBN10656.getObj()%>_b" style="width:90px;"></select></td>
						<td class="col_lbl_c"><select class="easyui-combobox_" tabindex="69" col="F102" id="<%=DefineReport.MeisyoSelect.KBN10656.getObj()%>_c" style="width:90px;"></select></td>
					</tr>
					</tbody></table>

					<table class="like_datagrid" for_kbn="3">
					<thead>
						<tr>
						<th style="width: 60px">　</th>
						<th>総売価</th>
						</tr>
					</thead>
					<tbody>
						<tr id="spread_reg">
							<td class="col_tit">A総売価</td>
							<td class="col_num"><input class="easyui-numberbox_" tabindex="67" col="F64" id="<%=DefineReport.InpText.A_BAIKAAM.getObj()%>3" check='<%=DefineReport.InpText.A_BAIKAAM.getMaxlenTag()%>' data-options="min:1" style="width:58px;"></td>
						</tr>
						<tr id="spread_mae">
							<td class="col_tit">B総売価</td>
							<td class="col_num"><input class="easyui-numberbox_" tabindex="68" col="F73" id="<%=DefineReport.InpText.B_BAIKAAM.getObj()%>3" check='<%=DefineReport.InpText.B_BAIKAAM.getMaxlenTag()%>' data-options="min:1" style="width:58px;"></td>
						</tr>
						<tr id="spread_ato">
							<td class="col_tit">C総売価</td>
							<td class="col_num"><input class="easyui-numberbox_" tabindex="69" col="F77" id="<%=DefineReport.InpText.C_BAIKAAM.getObj()%>3" check='<%=DefineReport.InpText.C_BAIKAAM.getMaxlenTag()%>' data-options="min:1" style="width:58px;"></td>
						</tr>
					</table>


				</td>
				<td style="vertical-align: top;">
					<table class="like_datagrid" for_kbn="2,4,5" cellspacing="0" cellpadding="0" style="display:hidden;">
					<tbody>
					<tr class="like_datagrid_head like_datagrid_head_top">
						<td class="labelCell">B総売価</td>
						<td class="labelCell" style="width:70px;">本体売価</td>
						<td class="labelCell" style="width:40px;">値入</td>
					</tr>
					<tr id="spread_b">
						<td class="col_num"  ><input class="easyui-numberbox_" tabindex="70" col="F73" id="<%=DefineReport.InpText.B_BAIKAAM.getObj()%>1" check='<%=DefineReport.InpText.B_BAIKAAM.getMaxlenTag()%>' data-options="min:1" style="width:58px;"></td>
						<td class="col_lbl_r"><span col="F74"></span></td>
						<td class="col_lbl_r"><span col="F75"></span></td>
					</tr>
					<tr class="like_datagrid_head">
						<td class="labelCell">C総売価</td>
						<td class="labelCell">本体売価</td>
						<td class="labelCell">値入</td>
					</tr>
					<tr id="spread_c">
						<td class="col_num"><input class="easyui-numberbox_" tabindex="73" col="F77" id="<%=DefineReport.InpText.C_BAIKAAM.getObj()%>1" check='<%=DefineReport.InpText.C_BAIKAAM.getMaxlenTag()%>' data-options="min:1" style="width:58px;"></td>
						<td class="col_lbl_r"><span col="F78"></span></td>
						<td class="col_lbl_r"><span col="F79"></span></td>
					</tr>
					</tbody></table>
				</td>
				<td style="vertical-align: top;padding-right: 0;">
					<table class="like_datagrid" cellspacing="0" cellpadding="0" style="display:hidden;">
					<tbody>
					<tr class="like_datagrid_head like_datagrid_head_top">
						<td class="labelCell">B売店</td>
						<td class="col_empty" rowspan="2" style="vertical-align: top;">
							<a href="#" class="easyui-linkbutton" tabindex="72" id="<%=DefineReport.Button.RANKNO.getObj()%>_add_b" title="対象店" style="width:50px;"><span>対象店</span></a>
						</td>
					</tr>
					<tr>
						<td class="col_num"><input class="easyui-numberbox_" tabindex="71" col="F76" id="<%=DefineReport.InpText.RANKNO_ADD_B.getObj()%>"  check='<%=DefineReport.InpText.RANKNO_ADD_B.getMaxlenTag()%>' for_btn="<%=DefineReport.Button.RANKNO.getObj()%>_add_b_F1" style="width:35px;"></td>
					</tr>
					<tr class="like_datagrid_head">
						<td class="labelCell">C売店</td>
						<td class="col_empty" rowspan="2" style="vertical-align: top;">
							<a href="#" class="easyui-linkbutton" tabindex="75" id="<%=DefineReport.Button.RANKNO.getObj()%>_add_c" title="対象店" style="width:50px;"><span>対象店</span></a>
						</td>
					</tr>
					<tr>
						<td class="col_num"><input class="easyui-numberbox_" tabindex="74" col="F80" id="<%=DefineReport.InpText.RANKNO_ADD_C.getObj()%>" check='<%=DefineReport.InpText.RANKNO_ADD_C.getMaxlenTag()%>' for_btn="<%=DefineReport.Button.RANKNO.getObj()%>_add_c_F1" style="width:35px;"></td>
					</tr>
					</tbody></table>
				</td>
				<td style="vertical-align: top;padding-right: 0;">
					<a href="#" class="easyui-linkbutton" tabindex="76" id="<%=DefineReport.Button.SUB.getObj()%>_winTG018_h" title="販売店確認" style="width:70px;"><span>販売店確認</span></a>
					<br>
					<a href="#" class="easyui-linkbutton" tabindex="77" id="<%=DefineReport.Button.SUB.getObj()%>_winTG018_n" title="納入店確認" style="width:70px;"><span>納入店確認</span></a>
				</td>
			</tr>
			</tbody></table>
		</td>
		<td colspan="2" style="vertical-align: top;">
			<table class="like_datagrid" for_kbn="4,5">
			<thead>
				<tr>
				<th style="width: 40px">　</th>
				<th>100g総売価</th>
				<th>1Kg原価</th>
				<th>1Kg総売価</th>
				<th>P原価</th>
				<th>P総売価</th>
				<th>入数</th>
				</tr>
			</thead>
			<tbody>
				<tr id="spread_a_sobaika">
					<td class="col_tit">A総売</td>
					<td class="col_num"><input class="easyui-numberbox_" tabindex="80" col="F81" id="<%=DefineReport.InpText.A_BAIKAAM.getObj()%>2" check='<%=DefineReport.InpText.A_BAIKAAM.getMaxlenTag()%>' data-options="min:1" style="width:58px;"></td>
					<td class="col_num"><input class="easyui-numberbox_" tabindex="83" col="F82" id="<%=DefineReport.InpText.GENKAAM_1KG.getObj()%>" check='<%=DefineReport.InpText.GENKAAM_1KG.getMaxlenTag()%>' data-options="min:1" style="width:78px;"></td>
					<td class="col_num"><input class="easyui-numberbox_" tabindex="84" col="F83" id="<%=DefineReport.InpText.A_GENKAAM_1KG.getObj()%>" check='<%=DefineReport.InpText.A_GENKAAM_1KG.getMaxlenTag()%>' data-options="min:1" style="width:58px;"></td>
					<td class="col_num"><input class="easyui-numberbox_" tabindex="87" col="F84" id="<%=DefineReport.InpText.GENKAAM_PACK.getObj()%>" check='<%=DefineReport.InpText.GENKAAM_PACK.getMaxlenTag()%>' data-options="min:1" style="width:78px;"></td>
					<td class="col_num"><input class="easyui-numberbox_" tabindex="88" col="F85" id="<%=DefineReport.InpText.A_BAIKAAM_PACK.getObj()%>" check='<%=DefineReport.InpText.A_BAIKAAM_PACK.getMaxlenTag()%>'  data-options="min:1"style="width:58px;"></td>
					<td class="col_num"><input class="easyui-numberbox_" tabindex="91" col="F86" id="<%=DefineReport.InpText.IRISU.getObj()%>2" check='<%=DefineReport.InpText.IRISU.getMaxlenTag()%>' data-options="min:1" style="width:35px;"></td>
				</tr>
				<tr id="spread_b_sobaika">
					<td class="col_tit">B総売</td>
					<td class="col_num"><input class="easyui-numberbox_" tabindex="81" col="F87" id="<%=DefineReport.InpText.B_BAIKAAM.getObj()%>2" check='<%=DefineReport.InpText.B_BAIKAAM.getMaxlenTag()%>' data-options="min:1" style="width:58px;"></td>
					<td class="col_lbl_r"><span col="F88"></span></td>
					<td class="col_num"><input class="easyui-numberbox_" tabindex="85" col="F89" id="<%=DefineReport.InpText.B_GENKAAM_1KG.getObj()%>" check='<%=DefineReport.InpText.B_GENKAAM_1KG.getMaxlenTag()%>' data-options="min:1" style="width:58px;"></td>
					<td class="col_lbl_r"><span col="F90"></span></td>
					<td class="col_num"><input class="easyui-numberbox_" tabindex="89" col="F91" id="<%=DefineReport.InpText.B_BAIKAAM_PACK.getObj()%>" check='<%=DefineReport.InpText.B_BAIKAAM_PACK.getMaxlenTag()%>' data-options="min:1" style="width:58px;"></td>
					<td class="col_lbl_r"><span col="F92"></span></td>
				</tr>
				<tr id="spread_c_sobaika">
					<td class="col_tit">C総売</td>
					<td class="col_num"><input class="easyui-numberbox_" tabindex="82" col="F93" id="<%=DefineReport.InpText.C_BAIKAAM.getObj()%>2" check='<%=DefineReport.InpText.C_BAIKAAM.getMaxlenTag()%>' data-options="min:1" style="width:58px;"></td>
					<td class="col_lbl_r"><span col="F94"></span></td>
					<td class="col_num"><input class="easyui-numberbox_" tabindex="86" col="F95" id="<%=DefineReport.InpText.C_GENKAAM_1KG.getObj()%>" check='<%=DefineReport.InpText.C_GENKAAM_1KG.getMaxlenTag()%>' data-options="min:1" style="width:58px;"></td>
					<td class="col_lbl_r"><span col="F96"></span></td>
					<td class="col_num"><input class="easyui-numberbox_" tabindex="90" col="F97" id="<%=DefineReport.InpText.C_BAIKAAM_PACK.getObj()%>" check='<%=DefineReport.InpText.C_BAIKAAM_PACK.getMaxlenTag()%>' data-options="min:1" style="width:58px;"></td>
					<td class="col_lbl_r"><span col="F98"></span></td>
				</tr>
			</table>
			<table class="dataTable" for_kbn="1" cellspacing="0" cellpadding="0" style="display:inline;"><tbody>
			<tr>
				<td class="labelCell" style="WIDTH: 60px;">発注原売価適用しない</td>
			</tr>
			<tr>
				<td class="col_chk">
					<input type="checkbox" class="chkbox" tabindex="92" col="F99" id="<%=DefineReport.Checkbox.HTGENBAIKAFLG.getObj()%>"><label class="chk_lbl" for="<%=DefineReport.Checkbox.HTGENBAIKAFLG.getObj()%>"></label>
				</td>
			</tr>
			</tbody></table>
		</td>
	</tr>
	</table>
</div>
<div data-options="region:'center',border:false" style="display:none;padding:2px 5px 0;">
	<div id="tt_1" class="easyui-tabs" data-options="plain:true,narrow:true,justified:true,fit:true" style="height: 500px;">
	<div id="tt_1_hb" title="販売情報" style="display:none;padding:3px 8px;" data-options="selected:true">
		<table class="t-layout1">
		<tbody>
		<tr>
			<td colspan="5">
				<table class="dataTable" cellspacing="0" cellpadding="0" style="display:hidden;"><tbody>
				<tr>
					<td class="labelCell" for_kbn="3,4,5">産地</td>
					<td class="labelCell" for_kbn="2,3,4,5">メーカー名</td>
					<td class="labelCell">ＰＯＰ名称</td>
				</tr>
				<tr>
					<td class="col_txt" for_kbn="3,4,5">
						<input class="easyui-textbox_" tabindex="100" col="F103" id="<%=DefineReport.InpText.SANCHIKN.getObj()%>" check='"datatyp":"zen_text","maxlen":"40"' style="width:250px;">
					</td>
					<td class="col_txt" for_kbn="2,3,4,5">
						<input class="easyui-textbox_" tabindex="101" col="F104" id="<%=DefineReport.InpText.MAKERKN.getObj()%>" check='"datatyp":"zen_text","maxlen":"28"' style="width:250px;">
					</td>
					<td class="col_txt">
						<input class="easyui-textbox_" tabindex="102" col="F105" id="<%=DefineReport.InpText.POPKN.getObj()%>" check='"datatyp":"zen_text","maxlen":"40"' style="width:250px;">
					</td>
				</tr>
				</tbody>
				</table>
			</td>
		</tr>
		<tr>
			<td>
				<table class="dataTable" cellspacing="0" cellpadding="0" style="display:hidden;"><tbody>
				<tr>
					<td colspan="6" class="labelCell" for_kbn="2,3,4,5" style="width:400px;">規格</td>
					<td rowspan="4" class="col_empty" for_kbn="2,3,4,5" style="padding: 0;"></td>
					<td rowspan="6" class="col_empty" for_kbn="2,3,4,5" style="vertical-align: bottom;padding-bottom:4px;">
						<table cellspacing="0" cellpadding="0" style="display:hidden;"><tbody>
						<tr class="noborder">
						<td for_kbn="2,4,5" style="vertical-align: bottom;">
							<table id="spread_ko" class="like_datagrid" style="table-layout:fixed;">
							<thead class="no_header">
								<tr>
								<th style="width: 10px">　</th>
								<th style="width: 35px">総売価</th>
								<th>総売価</th>
								</tr>
							</thead>
							<tbody>
								<tr>
									<td rowspan="3" class="col_tit"><span style="writing-mode: vertical-rl;">１個売り</span></td>
									<td class="col_tit">総売価A</td>
									<td class="col_num"><input class="easyui-numberbox_" tabindex="110" col="F113" id="<%=DefineReport.InpText.KO_A_BAIKAAN.getObj()%>" check='<%=DefineReport.InpText.KO_A_BAIKAAN.getMaxlenTag()%>' data-options="min:1" style="width:58px;"></td>
								</tr>
								<tr>
									<td class="col_tit">総売価B</td>
									<td class="col_num"><input class="easyui-numberbox_" tabindex="111" col="F114" id="<%=DefineReport.InpText.KO_B_BAIKAAN.getObj()%>" check='<%=DefineReport.InpText.KO_B_BAIKAAN.getMaxlenTag()%>' data-options="min:1" style="width:58px;"></td>
								</tr>
								<tr>
									<td class="col_tit">総売価C</td>
									<td class="col_num"><input class="easyui-numberbox_" tabindex="112" col="F115" id="<%=DefineReport.InpText.KO_C_BAIKAAN.getObj()%>" check='<%=DefineReport.InpText.KO_C_BAIKAAN.getMaxlenTag()%>' data-options="min:1" style="width:58px;"></td>
								</tr>
							</table>
						</td>
						<td for_kbn="2,3,4,5">
							<table id="spread_bd1" class="like_datagrid" style="table-layout:fixed;">
							<thead class="no_header">
								<tr>
								<th style="width: 10px">　</th>
								<th style="width: 35px">総売価</th>
								<th>総売価</th>
								</tr>
							</thead>
							<tbody>
								<tr>
									<td rowspan="4" class="col_tit"><span style="writing-mode: vertical-rl;">バンドル１</span></td>
									<td class="col_tit">点数1</td>
									<td class="col_num"><input class="easyui-numberbox_" tabindex="120" col="F116" id="<%=DefineReport.InpText.BD1_TENSU.getObj()%>" check='<%=DefineReport.InpText.BD1_TENSU.getMaxlenTag()%>' data-options="min:1" style="width:58px;"></td>
								</tr>
								<tr>
									<td class="col_tit">総売価1A</td>
									<td class="col_num"><input class="easyui-numberbox_" tabindex="121" col="F117" id="<%=DefineReport.InpText.BD1_A_BAIKAAN.getObj()%>" check='<%=DefineReport.InpText.BD1_A_BAIKAAN.getMaxlenTag()%>' data-options="min:1" style="width:58px;"></td>
								</tr>
								<tr>
									<td class="col_tit">総売価1B</td>
									<td class="col_num"><input class="easyui-numberbox_" tabindex="122" col="F118" id="<%=DefineReport.InpText.BD1_B_BAIKAAN.getObj()%>" check='<%=DefineReport.InpText.BD1_B_BAIKAAN.getMaxlenTag()%>' data-options="min:1" style="width:58px;"></td>
								</tr>
								<tr>
									<td class="col_tit">総売価1C</td>
									<td class="col_num"><input class="easyui-numberbox_" tabindex="123" col="F119" id="<%=DefineReport.InpText.BD1_C_BAIKAAN.getObj()%>" check='<%=DefineReport.InpText.BD1_C_BAIKAAN.getMaxlenTag()%>' data-options="min:1" style="width:58px;"></td>
								</tr>
							</table>
						</td>
						<td for_kbn="2,3,4,5">
							<table id="spread_bd2" class="like_datagrid" style="table-layout:fixed;">
							<thead class="no_header">
								<tr>
								<th style="width: 10px;">　</th>
								<th style="width: 35px;">総売価</th>
								<th>総売価</th>
								</tr>
							</thead>
							<tbody>
								<tr>
									<td rowspan="4" class="col_tit"><span style="writing-mode: vertical-rl;">バンドル２</span></td>
									<td class="col_tit">点数2</td>
									<td class="col_num"><input class="easyui-numberbox_" tabindex="124" col="F120" id="<%=DefineReport.InpText.BD2_TENSU.getObj()%>" check='<%=DefineReport.InpText.BD2_TENSU.getMaxlenTag()%>' data-options="min:1" style="width:58px;"></td>
								</tr>
								<tr>
									<td class="col_tit">総売価2A</td>
									<td class="col_num"><input class="easyui-numberbox_" tabindex="125" col="F121" id="<%=DefineReport.InpText.BD2_A_BAIKAAN.getObj()%>" check='<%=DefineReport.InpText.BD2_A_BAIKAAN.getMaxlenTag()%>' data-options="min:1" style="width:58px;"></td>
								</tr>
								<tr>
									<td class="col_tit">総売価2B</td>
									<td class="col_num"><input class="easyui-numberbox_" tabindex="126" col="F122" id="<%=DefineReport.InpText.BD2_B_BAIKAAN.getObj()%>" check='<%=DefineReport.InpText.BD2_B_BAIKAAN.getMaxlenTag()%>' data-options="min:1" style="width:58px;"></td>
								</tr>
								<tr>
									<td class="col_tit">総売価2C</td>
									<td class="col_num"><input class="easyui-numberbox_" tabindex="127" col="F123" id="<%=DefineReport.InpText.BD2_C_BAIKAAN.getObj()%>" check='<%=DefineReport.InpText.BD2_C_BAIKAAN.getMaxlenTag()%>' data-options="min:1" style="width:58px;"></td>
								</tr>
							</table>
						</td>
						<td for_kbn="5" style="vertical-align: top">
							<table id="spread_100g" class="like_datagrid">
							<thead>
								<tr>
								<th style="width: 10px;">　</th>
								<th>100g相当</th>
								</tr>
							</thead>
							<tbody>
								<tr>
									<td class="col_tit">A総売</td>
									<td class="col_num"><input class="easyui-numberbox_" tabindex="128" col="F124" id="<%=DefineReport.InpText.A_BAIKAAM_100G.getObj()%>" check='<%=DefineReport.InpText.A_BAIKAAM_100G.getMaxlenTag()%>' data-options="min:1" style="width:58px;"></td>
								</tr>
								<tr>
									<td class="col_tit">B総売</td>
									<td class="col_num"><input class="easyui-numberbox_" tabindex="129" col="F125" id="<%=DefineReport.InpText.B_BAIKAAM_100G.getObj()%>" check='<%=DefineReport.InpText.B_BAIKAAM_100G.getMaxlenTag()%>' data-options="min:1" style="width:58px;"></td>
								</tr>
								<tr>
									<td class="col_tit">C総売</td>
									<td class="col_num"><input class="easyui-numberbox_" tabindex="130" col="F126" id="<%=DefineReport.InpText.C_BAIKAAM_100G.getObj()%>" check='<%=DefineReport.InpText.C_BAIKAAM_100G.getMaxlenTag()%>' data-options="min:1" style="width:58px;"></td>
								</tr>
							</table>
						</td>
						<td for_kbn="4" style="vertical-align: top">
							<table id="spread_nama" class="dataTable" cellspacing="0" cellpadding="0" style="display:hidden;"><tbody>
							<tr>
								<td class="labelCell" style="width:30px;">生食</td>
								<td class="labelCell" style="width:30px;">加熱</td>
							</tr>
							<tr>
								<td class="col_chk"><input type="checkbox" class="chkbox" tabindex="131" col="F127" name="<%=DefineReport.Checkbox.NAMANETUKBN.getObj()%>" id="<%=DefineReport.Checkbox.NAMANETUKBN.getObj()%>1" value="<%=DefineReport.ValKbn10411.VAL1.getVal()%>"><label class="chk_lbl" for="<%=DefineReport.Checkbox.NAMANETUKBN.getObj()%>1"></label></td>
								<td class="col_chk"><input type="checkbox" class="chkbox" tabindex="132" col="F128" name="<%=DefineReport.Checkbox.NAMANETUKBN.getObj()%>" id="<%=DefineReport.Checkbox.NAMANETUKBN.getObj()%>2" value="<%=DefineReport.ValKbn10411.VAL2.getVal()%>"><label class="chk_lbl" for="<%=DefineReport.Checkbox.NAMANETUKBN.getObj()%>2"></label></td>
							</tr>
							<tr>
								<td class="labelCell">解凍</td>
								<td class="labelCell">養殖</td>
							</tr>
							<tr>
								<td class="col_chk"><input type="checkbox" class="chkbox" tabindex="133" col="F129" id="<%=DefineReport.Checkbox.KAITOFLG.getObj()%>"><label class="chk_lbl" for="<%=DefineReport.Checkbox.KAITOFLG.getObj()%>"></label></td>
								<td class="col_chk"><input type="checkbox" class="chkbox" tabindex="134" col="F130" id="<%=DefineReport.Checkbox.YOSHOKUFLG.getObj()%>"><label class="chk_lbl" for="<%=DefineReport.Checkbox.YOSHOKUFLG.getObj()%>"></label></td>
							</tr>
							</tbody>
							</table>
						</td>
						</tr>
						</tbody></table>
					</td>
				</tr>
				<tr>
					<td colspan="6" class="col_txt" for_kbn="2,3,4,5">
						<input class="easyui-textbox_" tabindex="103" col="F106" id="<%=DefineReport.InpText.KIKKN.getObj()%>" check='"datatyp":"zen_text","maxlen":"46"' style="width:400px;">
					</td>
				</tr>
				<tr>
					<td rowspan="2" class="labelCell" style="width: 10px;">制<br>限</td>
					<td colspan="1" class="labelCell" style="width: 60px;">先着人数</td>
					<td colspan="1" class="labelCell">限定表現</td>
					<td colspan="1" class="labelCell" style="width: 40px;">一人</td>
					<td colspan="1" class="labelCell" style="width: 80px;">単位</td>
				</tr>
				<tr>
					<td class="col_num"><input class="easyui-numberbox_" tabindex="104" col="F107" id="<%=DefineReport.InpText.SEGN_NINZU.getObj()%>" check='<%=DefineReport.InpText.SEGN_NINZU.getMaxlenTag()%>' data-options="min:1" style="width:55px;"></td>
					<td class="col_lbl_c"><select class="easyui-combobox_" tabindex="105" col="F108" id="<%=DefineReport.MeisyoSelect.KBN10670.getObj()%>" data-options="editable:true" style="width:120px;"></select></td>
					<td class="col_num"><input class="easyui-numberbox_" tabindex="106" col="F109" id="<%=DefineReport.InpText.SEGN_1KOSU.getObj()%>" check='<%=DefineReport.InpText.SEGN_1KOSU.getMaxlenTag()%>' data-options="min:1" style="width:35px;"></td>
					<td class="col_lbl_c"><select class="easyui-combobox_" tabindex="107" col="F110" id="<%=DefineReport.MeisyoSelect.KBN10671.getObj()%>" data-options="panelMinWidth:100,editable:true" style="width: 80px;"></select></td>
				</tr>
				<tr>
					<td colspan="2" class="labelCell">PLU配信しない</td>
					<td rowspan="2" colspan="4" class="col_empty"></td>
					<td colspan="1" class="labelCell" for_kbn="2,3,4,5" style="width: 40px;">よりどり</td>
				</tr>
				<tr>
					<td colspan="2" class="col_chk"><input type="checkbox" class="chkbox" tabindex="108" col="F111" id="<%=DefineReport.Checkbox.PLUSNDFLG.getObj()%>"><label class="chk_lbl" for="<%=DefineReport.Checkbox.PLUSNDFLG.getObj()%>"></label></td>
					<td class="col_chk" for_kbn="2,3,4,5"><input type="checkbox" class="chkbox" tabindex="109" col="F112" id="<%=DefineReport.Checkbox.YORIFLG.getObj()%>"><label class="chk_lbl" for="<%=DefineReport.Checkbox.YORIFLG.getObj()%>"></label></td>
				</tr>
				</tbody></table>
			</td>
		</tr>
		<tr>
			<td>＜チラシ・ＰＯＰ情報＞</td>
		</tr>
		<tr id="spread_chipo">
			<td colspan="5">
				<table class="dataTable" cellspacing="0" cellpadding="0" style="display:hidden;"><tbody>
				<tr>
					<td colspan="1" class="labelCell" style="width:100px;">目玉情報</td>
					<td colspan="1" class="col_empty"></td>
					<td colspan="1" class="labelCell" style="width:100px;">ＰＯＰコード</td>
					<td colspan="1" class="labelCell" style="width: 65px;">ＰＯＰサイズ</td>
					<td colspan="1" class="labelCell" style="width: 35px;">枚数</td>
					<td colspan="1" class="col_empty"></td>
					<td colspan="1" class="labelCell" style="width:200px;">商品サイズ</td>
					<td colspan="1" class="col_empty"></td>
					<td colspan="1" class="labelCell" style="width: 65px;">商品色</td>
				</tr>
				<tr>
					<td colspan="1" class="col_lbl_c"><select class="easyui-combobox_" tabindex="140" col="F131" id="<%=DefineReport.MeisyoSelect.KBN10660.getObj()%>" style="width:90px;"></select></td>
					<td colspan="1" class="col_empty"></td>
					<td colspan="1" class="col_num"  ><input class="easyui-numberbox_" tabindex="141" col="F132" id="<%=DefineReport.InpText.POPCD.getObj()%>" check='<%=DefineReport.InpText.POPCD.getMaxlenTag()%>' data-options="min:1" style="width:90px;"></td>
					<td colspan="1" class="col_lbl"  ><input class="easyui-textbox_" tabindex="142" col="F133" id="<%=DefineReport.InpText.POPSZ.getObj()%>" check='<%=DefineReport.InpText.POPSZ.getMaxlenTag()%>' style="width:50px;"></td>
					<td colspan="1" class="col_num"  ><input class="easyui-numberbox_" tabindex="143" col="F134" id="<%=DefineReport.InpText.POPSU.getObj()%>" check='<%=DefineReport.InpText.POPSU.getMaxlenTag()%>' data-options="min:1" style="width:35px;"></td>
					<td colspan="1" class="col_empty"></td>
					<td colspan="1" class="col_lbl"  ><input class="easyui-textbox_" tabindex="144" col="F135" id="<%=DefineReport.InpText.SHNSIZE.getObj()%>" check='<%=DefineReport.InpText.SHNSIZE.getMaxlenTag()%>' style="width:255px;"></td>
					<td colspan="1" class="col_empty"></td>
					<td colspan="1" class="col_lbl"  ><input class="easyui-textbox_" tabindex="145" col="F136" id="<%=DefineReport.InpText.SHNCOLOR.getObj()%>" check='<%=DefineReport.InpText.SHNCOLOR.getMaxlenTag()%>' style="width:130px;"></td>
				</tr>
				</tbody></table>
				<table class="dataTable" cellspacing="0" cellpadding="0" style="display:hidden;"><tbody>
				<tr>
					<td colspan="1" class="labelCell">その他日替コメント</td>
					<td colspan="1" class="col_lbl_c"><input class="easyui-textbox_" tabindex="146" col="F137" id="<%=DefineReport.InpText.COMMENT_HGW.getObj()%>" check='<%=DefineReport.InpText.COMMENT_HGW.getMaxlenTag()%>' style="width:700px;"></td>
				</tr>
				<tr>
					<td colspan="1" class="labelCell">ＰＯＰコメント(キャッチコピー)</td>
					<td colspan="1" class="col_lbl_c"><input class="easyui-textbox_" tabindex="147" col="F138" id="<%=DefineReport.InpText.COMMENT_POP.getObj()%>" check='<%=DefineReport.InpText.COMMENT_POP.getMaxlenTag()%>' style="width:700px;"></td>
				</tr>
				</tbody></table>
			</td>
		</tr>
		<tr>
			<td colspan="5">
				<table class="t-layout3">
				<tr>
					<th style="min-width:120px;"></th>
				</tr>
				<tr>
					<td><a href="#" title="商品コード検索" id="<%=DefineReport.Button.SHNCD.getObj()%>" class="easyui-linkbutton" tabindex="149" iconCls="icon-search" style="width:120px;"><span class="">商品コード検索</span></a></td>
				</tr>
				</table>
			</td>
		</tr>
		</tbody></table>
	</div>
	<div id="tt_1_nn" title="納入情報" style="display:none;padding:3px 8px;">
		<table class="t-layout1"  cellspacing="0" cellpadding="0" >
		<tbody>
		<tr>
			<td>
				<table class="dataTable" cellspacing="0" cellpadding="0" style="display:hidden;"><tbody>
				<tr>
					<td colspan="2" class="labelCell">事前打出</td>
					<td colspan="1" class="labelCell">特売コメント</td>
					<td colspan="1" class="labelCell">カット店展開しない</td>
					<td rowspan="2" class="col_empty">
						<a href="#" title="数値展開方法" id="<%=DefineReport.Button.TENKAI.getObj()%>" class="easyui-linkbutton" tabindex="154" iconCls="icon-edit" style="width:110px;"><span>数値展開方法</span></a>
					</td>
				</tr>
				<tr>
					<td colspan="1" class="col_chk" style="width:30px;">
						<input type="checkbox" class="chkbox" tabindex="150" col="F139" id="<%=DefineReport.Checkbox.JUFLG.getObj()%>"><label class="chk_lbl" for="<%=DefineReport.Checkbox.JUFLG.getObj()%>"></label>
					</td>
					<td colspan="1" class="col_txt">
						<input class="easyui-numberbox_" tabindex="151" col="F140" id="<%=DefineReport.InpText.JUHTDT.getObj()%>" check='<%=DefineReport.InpText.JUHTDT.getMaxlenTag()%>' data-options="prompt:'<%=DefineReport.Label.PROMPT_DTW.getTxt()%>'" style="width:88px;">
					</td>
					<td colspan="1" class="col_lbl_c">
						<input class="easyui-textbox_" tabindex="152" col="F141" id="<%=DefineReport.InpText.COMMENT_TB.getObj()%>" check='<%=DefineReport.InpText.COMMENT_TB.getMaxlenTag()%>' style="width:400px;">
					</td>
					<td colspan="1" class="col_chk">
						<input type="checkbox" class="chkbox" tabindex="153" col="F142" id="<%=DefineReport.Checkbox.CUTTENFLG.getObj()%>"><label class="chk_lbl" for="<%=DefineReport.Checkbox.CUTTENFLG.getObj()%>"></label>
					</td>
				</tr>
				</tbody>
				</table>
			</td>
			<td rowspan="2" style="vertical-align: top;">
				<table class="like_datagrid" style="table-layout:fixed;">
				<thead class="no_header">
					<tr>
					<th style="width: 40px;">　</th>
					<th style="width: 35px;">　</th>
					</tr>
				</thead>
				<tbody>
					<tr>
						<td class="col_tit">便区分</td>
						<td class="col_num"><input class="easyui-numberbox_" tabindex="155" col="F143" id="<%=DefineReport.InpText.BINKBN.getObj()%>" check='<%=DefineReport.InpText.BINKBN.getMaxlenTag()%>' style="width:65px;text-align: left"></td>
					</tr>
					<tr>
						<td class="col_tit">別伝区分</td>
						<td class="col_num"><input class="easyui-numberbox_" tabindex="156" col="F144" id="<%=DefineReport.InpText.BDENKBN.getObj()%>" check='<%=DefineReport.InpText.BDENKBN.getMaxlenTag()%>' data-options="min:0,max:8" style="width:65px;text-align: left"></td>
					</tr>
					<tr>
						<td class="col_tit">ワッペン区分</td>
						<td class="col_num"><input class="easyui-numberbox_" tabindex="157" col="F145" id="<%=DefineReport.InpText.WAPPNKBN.getObj()%>" check='<%=DefineReport.InpText.WAPPNKBN.getMaxlenTag()%>' style="width:65px;text-align: left"></td>
					</tr>
					<tr>
						<td class="col_tit">週次伝送flg</td>
						<td class="col_chk"><input type="checkbox" class="chkbox" tabindex="158" col="F146" id="<%=DefineReport.Checkbox.SHUDENFLG.getObj()%>"><label class="chk_lbl" for="<%=DefineReport.Checkbox.SHUDENFLG.getObj()%>"></label></td>
					</tr>
					<tr>
						<td class="col_tit">PC区分</td>
						<td class="col_lbl"><span col="F147" id="<%=DefineReport.MeisyoSelect.KBN102.getObj()%>"></span></td>
					</tr>
				</table>
				<div style="padding-top:95px;">
					<fieldset>
					<legend>訂正区分</legend>
						0:制限無し<br>
						1:週間可、追加不可<br>
						2:週間可、追加可<br>
						3:訂正不可<br>
						4:週間不可、追加可<br>
					</fieldset>
				</div>
			</td>
		</tr>
		<tr>
			<td>
				<table class="like_datagrid">
				<thead class="no_header">
				<tr>
					<th style="width: 50px"></th>
					<th></th>
					<th></th>
					<th></th>
					<th></th>
					<th></th>
					<th></th>
					<th></th>
					<th></th>
					<th></th>
					<th></th>
					<th style="width: 80px"></th>
				</tr>
				</thead>
				<tbody>
				<tr>
					<td class="col_tit">日付</td>
					<td class="col_lbl_c"><label class="rad_lbl"><input type="radio" tabindex="160" id="<%=DefineReport.Radio.SEL.getObj()%>1" name="<%=DefineReport.Radio.SEL.getObj()%>"/><span col="N1_1"></span></label></td>
					<td class="col_lbl_c"><label class="rad_lbl"><input type="radio" tabindex="161" id="<%=DefineReport.Radio.SEL.getObj()%>2" name="<%=DefineReport.Radio.SEL.getObj()%>"/><span col="N1_2"></span></label></td>
					<td class="col_lbl_c"><label class="rad_lbl"><input type="radio" tabindex="162" id="<%=DefineReport.Radio.SEL.getObj()%>3" name="<%=DefineReport.Radio.SEL.getObj()%>"/><span col="N1_3"></span></label></td>
					<td class="col_lbl_c"><label class="rad_lbl"><input type="radio" tabindex="163" id="<%=DefineReport.Radio.SEL.getObj()%>4" name="<%=DefineReport.Radio.SEL.getObj()%>"/><span col="N1_4"></span></label></td>
					<td class="col_lbl_c"><label class="rad_lbl"><input type="radio" tabindex="164" id="<%=DefineReport.Radio.SEL.getObj()%>5" name="<%=DefineReport.Radio.SEL.getObj()%>"/><span col="N1_5"></span></label></td>
					<td class="col_lbl_c"><label class="rad_lbl"><input type="radio" tabindex="165" id="<%=DefineReport.Radio.SEL.getObj()%>6" name="<%=DefineReport.Radio.SEL.getObj()%>"/><span col="N1_6"></span></label></td>
					<td class="col_lbl_c"><label class="rad_lbl"><input type="radio" tabindex="166" id="<%=DefineReport.Radio.SEL.getObj()%>7" name="<%=DefineReport.Radio.SEL.getObj()%>"/><span col="N1_7"></span></label></td>
					<td class="col_lbl_c"><label class="rad_lbl"><input type="radio" tabindex="167" id="<%=DefineReport.Radio.SEL.getObj()%>8" name="<%=DefineReport.Radio.SEL.getObj()%>"/><span col="N1_8"></span></label></td>
					<td class="col_lbl_c"><label class="rad_lbl"><input type="radio" tabindex="168" id="<%=DefineReport.Radio.SEL.getObj()%>9" name="<%=DefineReport.Radio.SEL.getObj()%>"/><span col="N1_9"></span></label></td>
					<td class="col_lbl_c"><label class="rad_lbl"><input type="radio" tabindex="169" id="<%=DefineReport.Radio.SEL.getObj()%>10" name="<%=DefineReport.Radio.SEL.getObj()%>"/><span col="N1_10"></span></label></td>
					<td class="col_lbl_c"></td>
				</tr>
				<tr>
					<td class="col_tit">曜日</td>
					<td class="col_lbl_c"><span col="N2_1"></span></td>
					<td class="col_lbl_c"><span col="N2_2"></span></td>
					<td class="col_lbl_c"><span col="N2_3"></span></td>
					<td class="col_lbl_c"><span col="N2_4"></span></td>
					<td class="col_lbl_c"><span col="N2_5"></span></td>
					<td class="col_lbl_c"><span col="N2_6"></span></td>
					<td class="col_lbl_c"><span col="N2_7"></span></td>
					<td class="col_lbl_c"><span col="N2_8"></span></td>
					<td class="col_lbl_c"><span col="N2_9"></span></td>
					<td class="col_lbl_c"><span col="N2_10"></span></td>
					<td class="col_lbl_c"></td>
				</tr>
				<tr>
					<td class="col_tit">販売日</td>
					<td class="col_lbl_c txt_chk"><span col="N3_1"></span></td>
					<td class="col_lbl_c txt_chk"><span col="N3_2"></span></td>
					<td class="col_lbl_c txt_chk"><span col="N3_3"></span></td>
					<td class="col_lbl_c txt_chk"><span col="N3_4"></span></td>
					<td class="col_lbl_c txt_chk"><span col="N3_5"></span></td>
					<td class="col_lbl_c txt_chk"><span col="N3_6"></span></td>
					<td class="col_lbl_c txt_chk"><span col="N3_7"></span></td>
					<td class="col_lbl_c txt_chk"><span col="N3_8"></span></td>
					<td class="col_lbl_c txt_chk"><span col="N3_9"></span></td>
					<td class="col_lbl_c txt_chk"><span col="N3_10"></span></td>
					<td class="col_lbl_c txt_chk"><span></span></td>
				</tr>
				<tr>
					<td class="col_tit">納入日</td>
					<td class="col_chk"><input type="checkbox" tabindex="170" col="N4_1" id="<%=DefineReport.Checkbox.NNDT.getObj()%>1"/></td>
					<td class="col_chk"><input type="checkbox" tabindex="171" col="N4_2" id="<%=DefineReport.Checkbox.NNDT.getObj()%>2"/></td>
					<td class="col_chk"><input type="checkbox" tabindex="172" col="N4_3" id="<%=DefineReport.Checkbox.NNDT.getObj()%>3"/></td>
					<td class="col_chk"><input type="checkbox" tabindex="173" col="N4_4" id="<%=DefineReport.Checkbox.NNDT.getObj()%>4"/></td>
					<td class="col_chk"><input type="checkbox" tabindex="174" col="N4_5" id="<%=DefineReport.Checkbox.NNDT.getObj()%>5"/></td>
					<td class="col_chk"><input type="checkbox" tabindex="175" col="N4_6" id="<%=DefineReport.Checkbox.NNDT.getObj()%>6"/></td>
					<td class="col_chk"><input type="checkbox" tabindex="176" col="N4_7" id="<%=DefineReport.Checkbox.NNDT.getObj()%>7"/></td>
					<td class="col_chk"><input type="checkbox" tabindex="177" col="N4_8" id="<%=DefineReport.Checkbox.NNDT.getObj()%>8"/></td>
					<td class="col_chk"><input type="checkbox" tabindex="178" col="N4_9" id="<%=DefineReport.Checkbox.NNDT.getObj()%>9"/></td>
					<td class="col_chk"><input type="checkbox" tabindex="179" col="N4_10" id="<%=DefineReport.Checkbox.NNDT.getObj()%>10"/></td>
					<td class="col_lbl_c"><span>数量計</span></td>
				</tr>
				<tr>
					<td class="col_tit">発注総数</td>
					<td class="col_num"><input class="easyui-numberbox_" tabindex="180" col="N5_1" id="<%=DefineReport.InpText.HTASU.getObj()%>1" check='<%=DefineReport.InpText.HTASU.getMaxlenTag()%>' data-options="min:0" style="width:60px;"></td>
					<td class="col_num"><input class="easyui-numberbox_" tabindex="181" col="N5_2" id="<%=DefineReport.InpText.HTASU.getObj()%>2" check='<%=DefineReport.InpText.HTASU.getMaxlenTag()%>' data-options="min:0" style="width:60px;"></td>
					<td class="col_num"><input class="easyui-numberbox_" tabindex="182" col="N5_3" id="<%=DefineReport.InpText.HTASU.getObj()%>3" check='<%=DefineReport.InpText.HTASU.getMaxlenTag()%>' data-options="min:0" style="width:60px;"></td>
					<td class="col_num"><input class="easyui-numberbox_" tabindex="183" col="N5_4" id="<%=DefineReport.InpText.HTASU.getObj()%>4" check='<%=DefineReport.InpText.HTASU.getMaxlenTag()%>' data-options="min:0" style="width:60px;"></td>
					<td class="col_num"><input class="easyui-numberbox_" tabindex="184" col="N5_5" id="<%=DefineReport.InpText.HTASU.getObj()%>5" check='<%=DefineReport.InpText.HTASU.getMaxlenTag()%>' data-options="min:0" style="width:60px;"></td>
					<td class="col_num"><input class="easyui-numberbox_" tabindex="185" col="N5_6" id="<%=DefineReport.InpText.HTASU.getObj()%>6" check='<%=DefineReport.InpText.HTASU.getMaxlenTag()%>' data-options="min:0" style="width:60px;"></td>
					<td class="col_num"><input class="easyui-numberbox_" tabindex="186" col="N5_7" id="<%=DefineReport.InpText.HTASU.getObj()%>7" check='<%=DefineReport.InpText.HTASU.getMaxlenTag()%>' data-options="min:0" style="width:60px;"></td>
					<td class="col_num"><input class="easyui-numberbox_" tabindex="187" col="N5_8" id="<%=DefineReport.InpText.HTASU.getObj()%>8" check='<%=DefineReport.InpText.HTASU.getMaxlenTag()%>' data-options="min:0" style="width:60px;"></td>
					<td class="col_num"><input class="easyui-numberbox_" tabindex="188" col="N5_9" id="<%=DefineReport.InpText.HTASU.getObj()%>9" check='<%=DefineReport.InpText.HTASU.getMaxlenTag()%>' data-options="min:0" style="width:60px;"></td>
					<td class="col_num"><input class="easyui-numberbox_" tabindex="189" col="N5_10" id="<%=DefineReport.InpText.HTASU.getObj()%>10" check='<%=DefineReport.InpText.HTASU.getMaxlenTag()%>' data-options="min:0" style="width:60px;"></td>
					<td class="col_lbl_r"><span col="N5_11"></span></td>
				</tr>
				<tr>
					<td class="col_tit">　</td>
					<td class="col_lbl"><span col="N6_1">通常数pt</span></td>
					<td class="col_lbl"><span col="N6_2">通常数pt</span></td>
					<td class="col_lbl"><span col="N6_3">通常数pt</span></td>
					<td class="col_lbl"><span col="N6_4">通常数pt</span></td>
					<td class="col_lbl"><span col="N6_5">通常数pt</span></td>
					<td class="col_lbl"><span col="N6_6">通常数pt</span></td>
					<td class="col_lbl"><span col="N6_7">通常数pt</span></td>
					<td class="col_lbl"><span col="N6_8">通常数pt</span></td>
					<td class="col_lbl"><span col="N6_9">通常数pt</span></td>
					<td class="col_lbl"><span col="N6_10">通常数pt</span></td>
					<td class="col_lbl"><span></span></td>
				</tr>
				<tr>
					<td class="col_tit">パタンNo.</td>
					<td class="col_num"><input class="easyui-numberbox_" tabindex="190" col="N7_1" id="<%=DefineReport.InpText.PTNNO.getObj()%>1" check='<%=DefineReport.InpText.PTNNO.getMaxlenTag()%>' style="width:60px;text-align: left"></td>
					<td class="col_num"><input class="easyui-numberbox_" tabindex="191" col="N7_2" id="<%=DefineReport.InpText.PTNNO.getObj()%>2" check='<%=DefineReport.InpText.PTNNO.getMaxlenTag()%>' style="width:60px;text-align: left"></td>
					<td class="col_num"><input class="easyui-numberbox_" tabindex="192" col="N7_3" id="<%=DefineReport.InpText.PTNNO.getObj()%>3" check='<%=DefineReport.InpText.PTNNO.getMaxlenTag()%>' style="width:60px;text-align: left"></td>
					<td class="col_num"><input class="easyui-numberbox_" tabindex="193" col="N7_4" id="<%=DefineReport.InpText.PTNNO.getObj()%>4" check='<%=DefineReport.InpText.PTNNO.getMaxlenTag()%>' style="width:60px;text-align: left"></td>
					<td class="col_num"><input class="easyui-numberbox_" tabindex="194" col="N7_5" id="<%=DefineReport.InpText.PTNNO.getObj()%>5" check='<%=DefineReport.InpText.PTNNO.getMaxlenTag()%>' style="width:60px;text-align: left"></td>
					<td class="col_num"><input class="easyui-numberbox_" tabindex="195" col="N7_6" id="<%=DefineReport.InpText.PTNNO.getObj()%>6" check='<%=DefineReport.InpText.PTNNO.getMaxlenTag()%>' style="width:60px;text-align: left"></td>
					<td class="col_num"><input class="easyui-numberbox_" tabindex="196" col="N7_7" id="<%=DefineReport.InpText.PTNNO.getObj()%>7" check='<%=DefineReport.InpText.PTNNO.getMaxlenTag()%>' style="width:60px;text-align: left"></td>
					<td class="col_num"><input class="easyui-numberbox_" tabindex="197" col="N7_8" id="<%=DefineReport.InpText.PTNNO.getObj()%>8" check='<%=DefineReport.InpText.PTNNO.getMaxlenTag()%>' style="width:60px;text-align: left"></td>
					<td class="col_num"><input class="easyui-numberbox_" tabindex="198" col="N7_9" id="<%=DefineReport.InpText.PTNNO.getObj()%>9" check='<%=DefineReport.InpText.PTNNO.getMaxlenTag()%>' style="width:60px;text-align: left"></td>
					<td class="col_num"><input class="easyui-numberbox_" tabindex="199" col="N7_10" id="<%=DefineReport.InpText.PTNNO.getObj()%>10" check='<%=DefineReport.InpText.PTNNO.getMaxlenTag()%>' style="width:60px;text-align: left"></td>
					<td class="col_lbl"><span></span></td>
				</tr>
				<tr>
					<td class="col_tit">訂正区分</td>
					<td class="col_num"><input class="easyui-numberbox_" tabindex="200" col="N8_1" id="<%=DefineReport.InpText.TSEIKBN.getObj()%>1" check='<%=DefineReport.InpText.TSEIKBN.getMaxlenTag()%>' data-options="min:0,max:4" style="width:60px;text-align: left"></td>
					<td class="col_num"><input class="easyui-numberbox_" tabindex="201" col="N8_2" id="<%=DefineReport.InpText.TSEIKBN.getObj()%>2" check='<%=DefineReport.InpText.TSEIKBN.getMaxlenTag()%>' data-options="min:0,max:4" style="width:60px;text-align: left"></td>
					<td class="col_num"><input class="easyui-numberbox_" tabindex="202" col="N8_3" id="<%=DefineReport.InpText.TSEIKBN.getObj()%>3" check='<%=DefineReport.InpText.TSEIKBN.getMaxlenTag()%>' data-options="min:0,max:4" style="width:60px;text-align: left"></td>
					<td class="col_num"><input class="easyui-numberbox_" tabindex="203" col="N8_4" id="<%=DefineReport.InpText.TSEIKBN.getObj()%>4" check='<%=DefineReport.InpText.TSEIKBN.getMaxlenTag()%>' data-options="min:0,max:4" style="width:60px;text-align: left"></td>
					<td class="col_num"><input class="easyui-numberbox_" tabindex="204" col="N8_5" id="<%=DefineReport.InpText.TSEIKBN.getObj()%>5" check='<%=DefineReport.InpText.TSEIKBN.getMaxlenTag()%>' data-options="min:0,max:4" style="width:60px;text-align: left"></td>
					<td class="col_num"><input class="easyui-numberbox_" tabindex="205" col="N8_6" id="<%=DefineReport.InpText.TSEIKBN.getObj()%>6" check='<%=DefineReport.InpText.TSEIKBN.getMaxlenTag()%>' data-options="min:0,max:4" style="width:60px;text-align: left"></td>
					<td class="col_num"><input class="easyui-numberbox_" tabindex="206" col="N8_7" id="<%=DefineReport.InpText.TSEIKBN.getObj()%>7" check='<%=DefineReport.InpText.TSEIKBN.getMaxlenTag()%>' data-options="min:0,max:4" style="width:60px;text-align: left"></td>
					<td class="col_num"><input class="easyui-numberbox_" tabindex="207" col="N8_8" id="<%=DefineReport.InpText.TSEIKBN.getObj()%>8" check='<%=DefineReport.InpText.TSEIKBN.getMaxlenTag()%>' data-options="min:0,max:4" style="width:60px;text-align: left"></td>
					<td class="col_num"><input class="easyui-numberbox_" tabindex="208" col="N8_9" id="<%=DefineReport.InpText.TSEIKBN.getObj()%>9" check='<%=DefineReport.InpText.TSEIKBN.getMaxlenTag()%>' data-options="min:0,max:4" style="width:60px;text-align: left"></td>
					<td class="col_num"><input class="easyui-numberbox_" tabindex="209" col="N8_10" id="<%=DefineReport.InpText.TSEIKBN.getObj()%>10" check='<%=DefineReport.InpText.TSEIKBN.getMaxlenTag()%>' data-options="min:0,max:4" style="width:60px;text-align: left"></td>
					<td class="col_lbl"><span></span></td>
				</tr>
				<tr>
					<td class="col_tit">店舗数</td>
					<td class="col_lbl_r"><span col="N9_1"></span></td>
					<td class="col_lbl_r"><span col="N9_2"></span></td>
					<td class="col_lbl_r"><span col="N9_3"></span></td>
					<td class="col_lbl_r"><span col="N9_4"></span></td>
					<td class="col_lbl_r"><span col="N9_5"></span></td>
					<td class="col_lbl_r"><span col="N9_6"></span></td>
					<td class="col_lbl_r"><span col="N9_7"></span></td>
					<td class="col_lbl_r"><span col="N9_8"></span></td>
					<td class="col_lbl_r"><span col="N9_9"></span></td>
					<td class="col_lbl_r"><span col="N9_10"></span></td>
					<td class="col_lbl_c"><span>期間計</span></td>
				</tr>
				<tr>
					<td class="col_tit">展開数</td>
					<td class="col_lbl_r"><span col="N10_1"></span></td>
					<td class="col_lbl_r"><span col="N10_2"></span></td>
					<td class="col_lbl_r"><span col="N10_3"></span></td>
					<td class="col_lbl_r"><span col="N10_4"></span></td>
					<td class="col_lbl_r"><span col="N10_5"></span></td>
					<td class="col_lbl_r"><span col="N10_6"></span></td>
					<td class="col_lbl_r"><span col="N10_7"></span></td>
					<td class="col_lbl_r"><span col="N10_8"></span></td>
					<td class="col_lbl_r"><span col="N10_9"></span></td>
					<td class="col_lbl_r"><span col="N10_10"></span></td>
					<td class="col_lbl_r"><span col="N10_11"></span></td>
				</tr>
				<tr>
					<td class="col_tit">数量差</td>
					<td class="col_lbl_r"><span col="N11_1"></span></td>
					<td class="col_lbl_r"><span col="N11_2"></span></td>
					<td class="col_lbl_r"><span col="N11_3"></span></td>
					<td class="col_lbl_r"><span col="N11_4"></span></td>
					<td class="col_lbl_r"><span col="N11_5"></span></td>
					<td class="col_lbl_r"><span col="N11_6"></span></td>
					<td class="col_lbl_r"><span col="N11_7"></span></td>
					<td class="col_lbl_r"><span col="N11_8"></span></td>
					<td class="col_lbl_r"><span col="N11_9"></span></td>
					<td class="col_lbl_r"><span col="N11_10"></span></td>
					<td class="col_lbl_r"><span col="N11_11"></span></td>
				</tr>
				<tr>
					<td class="col_tit">原価計</td>
					<td class="col_lbl_r"><span col="N12_1"></span></td>
					<td class="col_lbl_r"><span col="N12_2"></span></td>
					<td class="col_lbl_r"><span col="N12_3"></span></td>
					<td class="col_lbl_r"><span col="N12_4"></span></td>
					<td class="col_lbl_r"><span col="N12_5"></span></td>
					<td class="col_lbl_r"><span col="N12_6"></span></td>
					<td class="col_lbl_r"><span col="N12_7"></span></td>
					<td class="col_lbl_r"><span col="N12_8"></span></td>
					<td class="col_lbl_r"><span col="N12_9"></span></td>
					<td class="col_lbl_r"><span col="N12_10"></span></td>
					<td class="col_lbl_r"><span col="N12_11"></span></td>
				</tr>
				<tr>
					<td class="col_tit">本売価計</td>
					<td class="col_lbl_r"><span col="N13_1"></span></td>
					<td class="col_lbl_r"><span col="N13_2"></span></td>
					<td class="col_lbl_r"><span col="N13_3"></span></td>
					<td class="col_lbl_r"><span col="N13_4"></span></td>
					<td class="col_lbl_r"><span col="N13_5"></span></td>
					<td class="col_lbl_r"><span col="N13_6"></span></td>
					<td class="col_lbl_r"><span col="N13_7"></span></td>
					<td class="col_lbl_r"><span col="N13_8"></span></td>
					<td class="col_lbl_r"><span col="N13_9"></span></td>
					<td class="col_lbl_r"><span col="N13_10"></span></td>
					<td class="col_lbl_r"><span col="N13_11"></span></td>
				</tr>
				<tr>
					<td class="col_tit">荒利額</td>
					<td class="col_lbl_r"><span col="N14_1"></span></td>
					<td class="col_lbl_r"><span col="N14_2"></span></td>
					<td class="col_lbl_r"><span col="N14_3"></span></td>
					<td class="col_lbl_r"><span col="N14_4"></span></td>
					<td class="col_lbl_r"><span col="N14_5"></span></td>
					<td class="col_lbl_r"><span col="N14_6"></span></td>
					<td class="col_lbl_r"><span col="N14_7"></span></td>
					<td class="col_lbl_r"><span col="N14_8"></span></td>
					<td class="col_lbl_r"><span col="N14_9"></span></td>
					<td class="col_lbl_r"><span col="N14_10"></span></td>
					<td class="col_lbl_r"><span col="N14_11"></span></td>
				</tr>
				<tr style="display: none;">
					<td class="col_tit">hidden</td>
					<td class="col_lbl_r"><span col="N90_1"></span><span col="N91_1"></span><span col="N50_1"></span><span col="N94_1"></span></td>
					<td class="col_lbl_r"><span col="N90_2"></span><span col="N91_2"></span><span col="N50_2"></span><span col="N94_2"></span></td>
					<td class="col_lbl_r"><span col="N90_3"></span><span col="N91_3"></span><span col="N50_3"></span><span col="N94_3"></span></td>
					<td class="col_lbl_r"><span col="N90_4"></span><span col="N91_4"></span><span col="N50_4"></span><span col="N94_4"></span></td>
					<td class="col_lbl_r"><span col="N90_5"></span><span col="N91_5"></span><span col="N50_5"></span><span col="N94_5"></span></td>
					<td class="col_lbl_r"><span col="N90_6"></span><span col="N91_6"></span><span col="N50_6"></span><span col="N94_6"></span></td>
					<td class="col_lbl_r"><span col="N90_7"></span><span col="N91_7"></span><span col="N50_7"></span><span col="N94_7"></span></td>
					<td class="col_lbl_r"><span col="N90_8"></span><span col="N91_8"></span><span col="N50_8"></span><span col="N94_8"></span></td>
					<td class="col_lbl_r"><span col="N90_9"></span><span col="N91_9"></span><span col="N50_9"></span><span col="N94_9"></span></td>
					<td class="col_lbl_r"><span col="N90_10"></span><span col="N91_10"></span><span col="N50_10"></span><span col="N94_10"></span></td>
					<td class="col_lbl_r"></td>
				</tr>
				</table>
			</td>
		</tr>
		<tr>
			<td colspan="2">
				<table class="t-layout3">
				<tr>
					<th style="min-width:110px;"></th><th style="min-width:110px;"></th><th style="min-width:110px;"></th>
				</tr>
				<tr>
					<td><a href="#" title="再計算" id="<%=DefineReport.Button.CALC.getObj()%>" class="easyui-linkbutton" tabindex="300" iconCls="icon-sum" style="width:100px;"><span class="btnTxt">再計算</span></a></td>
					<td><a href="#" title="パターン" id="<%=DefineReport.Button.SUB.getObj()%>_winST012" class="easyui-linkbutton" tabindex="301" iconCls="icon-search" style="width:100px;"><span class="btnTxt">パターン</span></a></td>
					<td><a href="#" title="店別数量" id="<%=DefineReport.Button.TENBETUSU.getObj()%>" class="easyui-linkbutton" tabindex="302" iconCls="icon-search" style="width:100px;"><span class="btnTxt">店別数量</span></a></td>
				</tr>
				</table>
			</td>
		</tr>
		</tbody></table>
	</div>
	</div>
	<input type="hidden" name="<%=DefineReport.Text.SHORIDT.getObj()%>" id="<%=DefineReport.Text.SHORIDT.getObj()%>" />
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
			<td><a href="#" title="戻る" id="<%=DefineReport.Button.BACK.getObj()%>" class="easyui-linkbutton" tabindex="310" iconCls="icon-undo" style="width:110px;"><span class="btnTxt">戻る</span></a></td>
			<td></td>
			<td><a href="#" class="easyui-linkbutton" tabindex="311" id="<%=DefineReport.Button.CANCEL.getObj()%>" title="<%=DefineReport.Button.CANCEL.getTxt()%>" iconCls="icon-cancel" style="width:110px;"><%=DefineReport.Button.CANCEL.getTxt()%></a></td>
			<td><a href="#" class="easyui-linkbutton" tabindex="312" id="<%=DefineReport.Button.UPD.getObj()%>" title="<%=DefineReport.Button.UPD.getTxt()%>" iconCls="icon-save" style="width:110px;"><span class="btnTxt"><%=DefineReport.Button.UPD.getTxt()%></span></a
				><a href="#" class="easyui-linkbutton" tabindex="313" id="<%=DefineReport.Button.KYOKA.getObj()%>" title="<%=DefineReport.Button.KYOKA.getTxt()%>" iconCls="icon-save" style="width:110px;"><span class="btnTxt"><%=DefineReport.Button.KYOKA.getTxt()%></span></a>
			</td>
			<td><a href="#" class="easyui-linkbutton" tabindex="314" id="<%=DefineReport.Button.DEL.getObj()%>" title="<%=DefineReport.Button.DEL.getTxt()%>" iconCls="icon-remove" style="width:110px;"><span class="btnTxt"><%=DefineReport.Button.DEL.getTxt()%></span></a></td>
		</tr>
		</table>
	</div>
	<div id="disp_record_info" style="float: right;">
		<span class="labelName" tabindex="-1" style="padding-top: 3px;">
			登録日 <span col="F151" id="<%=DefineReport.Text.ADDDT.getObj()%>"></span>　更新日 <span col="F152" id="<%=DefineReport.Text.UPDDT.getObj()%>"></span>　オペレータ <span col="F150" id="<%=DefineReport.Text.OPERATOR.getObj()%>"></span>
		</span>
		<input type="hidden" tabindex="-1" col="F153" name="<%=DefineReport.Hidden.UPDDT.getObj()%>" id="<%=DefineReport.Hidden.UPDDT.getObj()%>" />
		<input type="hidden" tabindex="-1" col="F154" id="<%=DefineReport.InpText.TENKAIKBN.getObj()%>"/>
		<input type="hidden" tabindex="-1" col="F157"/><input type="hidden" tabindex="-1" col="F158"/><input type="hidden" tabindex="-1" col="F159"/>
		<input type="hidden" tabindex="-1" col="F160"/><input type="hidden" tabindex="-1" col="F161"/>
		<input type="hidden" tabindex="-1" col="F162"/>
		<input type="hidden" tabindex="-1" col="F163"/><input type="hidden" tabindex="-1" col="F164"/><input type="hidden" tabindex="-1" col="F165"/>
		<input type="hidden" tabindex="-1" col="F166"/><input type="hidden" tabindex="-1" col="F167"/><input type="hidden" tabindex="-1" col="F168"/><input type="hidden" tabindex="-1" col="F169"/>
		<input type="hidden" tabindex="-1" col="F170"/><input type="hidden" tabindex="-1" col="F171"/>
		<input type="hidden" tabindex="-1" col="F172"/><input type="hidden" tabindex="-1" col="F173"/><input type="hidden" tabindex="-1" col="F174"/>
		<input type="hidden" tabindex="-1" col="F175"/>
		<input type="hidden" tabindex="-1" col="F176"/>
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
	<jsp:include page="Out_Reportwin006.jsp" flush="true" />
	<jsp:include page="Out_ReportwinST007.jsp" flush="true" />
	<jsp:include page="Out_ReportwinST009.jsp" flush="true" />
	<jsp:include page="Out_ReportwinST012.jsp" flush="true" />
	<jsp:include page="Out_ReportwinST020.jsp" flush="true" />
	<jsp:include page="Out_ReportwinST021.jsp" flush="true" />
 	<jsp:include page="Out_ReportwinTG018.jsp" flush="true" />
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
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.TG016.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.win001.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.win002.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.win006.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.winST007.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.winST009.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.winST012.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.winST020.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.winST021.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.winTG018.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.control.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.events.js?v=<%=prm %>"></script>
</html>