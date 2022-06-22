
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
<table>
<!-- 1列目 -->
<tr><td style = "vertical-align: top;">
<table class="t-layout" style="margin:10px;">
 	<tr><th style="max-width:600px;"></th><th style="max-width:600px;"></th></tr>
		<tr>
			<td>
				<div id = "inputno" class="inp_box"><input class="easyui-numberbox" tabindex="1" col="F1" id="<%=DefineReport.InpText.MOYSCD.getObj()%>" check='<%=DefineReport.InpText.MOYSCD.getMaxlenTag()%>' data-options="label:'催しコード',prompt:'_-______-___',labelWidth:120,required:true" style="width:250px;"></div>
			</td>
		</tr>
		<tr>
			<td>
				<div id = "inputno" class="inp_box"><input class="easyui-textbox" tabindex="2" col="F2" id="<%=DefineReport.InpText.MOYKN.getObj()%>" check='<%=DefineReport.InpText.MOYKN.getMaxlenTag()%>' data-options="label:'催し名称',labelWidth:120" for_inp="<%=DefineReport.InpText.MOYSCD.getObj()%>_F2" style="width:410px;"></div>
			</td>
		</tr>
		<tr>
			<td>
				<div id = "inputno" class="inp_box"><input class="easyui-numberbox" tabindex="3" col="F3" id="<%=DefineReport.InpText.MOYSSTDT.getObj()%>" check='<%=DefineReport.InpText.MOYSSTDT.getMaxlenTag()%>' data-options="label:'催し期間',labelWidth:120,prompt:'<%=DefineReport.Label.PROMPT_DTW.getTxt()%>'" for_inp="<%=DefineReport.InpText.MOYSCD.getObj()%>_F4" style="width:220px;">
				～
				<input class="easyui-numberbox" tabindex="4" col="F4" id="<%=DefineReport.InpText.MOYSEDDT.getObj()%>" check='<%=DefineReport.InpText.MOYSEDDT.getMaxlenTag()%>' data-options="prompt:'<%=DefineReport.Label.PROMPT_DTW.getTxt()%>'" for_inp="<%=DefineReport.InpText.MOYSCD.getObj()%>_F5" style="width:100px;"></div>
			</td>
		</tr>
		<tr>
			<td>
				<div id = "inputno" class="inp_box"><input class="easyui-textbox" tabindex="5" col="F5" id="<%=DefineReport.InpText.BTKN.getObj()%>" check='<%=DefineReport.InpText.BTKN.getMaxlenTag()%>' data-options="label:'分類割引名称',labelWidth:120,required:true" style="width:410px;"></div>
			</td>
		</tr>
		<tr>
			<td>
				<div id = "inputno" class="inp_box"><input class="easyui-numberbox" tabindex="6" col="F6" id="<%=DefineReport.InpText.HBSTDT.getObj()%>" check='<%=DefineReport.InpText.HBSTDT.getMaxlenTag()%>' data-options="label:'販売期間',labelWidth:120,required:true,prompt:'<%=DefineReport.Label.PROMPT_DTW.getTxt()%>'" style="width:220px;">
				～
				<input class="easyui-numberbox" tabindex="7" col="F7" id="<%=DefineReport.InpText.HBEDDT.getObj()%>" check='<%=DefineReport.InpText.HBEDDT.getMaxlenTag()%>' data-options="prompt:'<%=DefineReport.Label.PROMPT_DTW.getTxt()%>',required:true" style="width:100px;"></div>
			</td>
		</tr>

		<%-- <tr>
			<td>
				<div id = "inputno" class="inp_box"><input class="easyui-numberbox" tabindex="8" col="F18" id="<%=DefineReport.InpText.PROMO_BGM_TM.getObj()%>" check='<%=DefineReport.InpText.PROMO_BGM_TM.getMaxlenTag()%>' data-options="label:'タイムサービス',labelWidth:120,prompt:'<%=DefineReport.Label.PROMPT_HHMM.getTxt()%>'" style="width:170px;">
				～
				<input class="easyui-numberbox" tabindex="9" col="F19" id="<%=DefineReport.InpText.PROMO_END_TM.getObj()%>" check='<%=DefineReport.InpText.PROMO_END_TM.getMaxlenTag()%>' data-options="prompt:'<%=DefineReport.Label.PROMPT_HHMM.getTxt()%>'" style="width:50px;"></div>
			</td>
		</tr> --%>

		<tr>
			<td>
				<div id = "inputno" class="inp_box"><input class="easyui-numberbox" tabindex="10" col="F8" id="<%=DefineReport.InpText.BMNCD.getObj()%>" check='<%=DefineReport.InpText.BMNCD.getMaxlenTag()%>' data-options="label:'部門',labelWidth:120,required:true" style="width:170px;">
				<input class="easyui-textbox" id="<%=DefineReport.InpText.BMKAN.getObj()%>" check='<%=DefineReport.InpText.BMKAN.getMaxlenTag()%>' for_inp="<%=DefineReport.InpText.BMNCD.getObj()%>_BMNKN" style="width:235px;" disabled="disabled">
				</div>
			</td>
		</tr>
		<tr>
			<td>
				<div id = "inputno" class="inp_box"><input class="easyui-numberbox" tabindex="11" col="F9" id="<%=DefineReport.InpText.DAICD.getObj()%>" check='<%=DefineReport.InpText.DAICD.getMaxlenTag()%>' data-options="label:'大分類',labelWidth:120,required:true" style="width:170px;">
				<input class="easyui-textbox" id="<%=DefineReport.InpText.DAIBRUIKN.getObj()%>" check='<%=DefineReport.InpText.DAIBRUIKN.getMaxlenTag()%>' for_inp="<%=DefineReport.InpText.DAICD.getObj()%>_TEXT2" style="width:235px;" disabled="disabled">
				</div>
			</td>
		</tr>
		<tr>
			<td>
				<div id = "inputno" class="inp_box"><input class="easyui-numberbox" tabindex="12" col="F10" id="<%=DefineReport.InpText.CHUCD.getObj()%>" check='<%=DefineReport.InpText.CHUCD.getMaxlenTag()%>' data-options="label:'中分類',labelWidth:120" style="width:170px;">
				<input class="easyui-textbox" id="<%=DefineReport.InpText.CHUBRUIKN.getObj()%>" check='<%=DefineReport.InpText.CHUBRUIKN.getMaxlenTag()%>' for_inp="<%=DefineReport.InpText.CHUCD.getObj()%>_TEXT2" style="width:235px;" disabled="disabled">
				</div>
			</td>
		</tr>
		<tr>
			<td>
				<div id = "inputno" class="inp_box"><input class="easyui-numberbox" tabindex="13" col="F11" id="<%=DefineReport.InpText.WARIRT.getObj()%>" check='<%=DefineReport.InpText.WARIRT.getMaxlenTag()%>' data-options="label:'割引率',labelWidth:120,required:true" style="width:170px;text-align:right"><label class="rad_lbl" style="width:50px;height:22px;text-align: center;vertical-align:middle; line-height: 22px;">%引き</label></div>
			</td>
		</tr>
</table>
</td>
<!-- 2列目 -->
<td  style = "vertical-align: bottom;">
		<table id="dg2" class="like_datagrid">
			<thead>
				<tr>
				<th>
					<a href="#" class="easyui-linkbutton" tabindex="19" id="<%=DefineReport.Button.TAISYOTEN.getObj()%>_add" title="<%=DefineReport.Button.TAISYOTEN.getTxt()%>" style="width:50px;"><span><%=DefineReport.Button.TAISYOTEN.getTxt()%></span></a>
				</th>
				<th>
					<a href="#" class="easyui-linkbutton" tabindex="20" id="<%=DefineReport.Button.JYOGAITEN.getObj()%>_del" title="<%=DefineReport.Button.JYOGAITEN.getTxt()%>" style="width:50px;"><span><%=DefineReport.Button.JYOGAITEN.getTxt()%></span></a>
				</th>
				</tr>
			</thead>
			<tbody>
				<tr>
					<td class="col_txt"><input class="easyui-numberbox" tabindex="21" col="F12" id="<%=DefineReport.InpText.TAISYOTEN.getObj()%>" check='<%=DefineReport.InpText.TAISYOTEN.getMaxlenTag()%>' data-options="required:true" style="width:50px;" for_btn="<%=DefineReport.Button.TAISYOTEN.getObj()%>_add_F1"></td>
					<td class="col_txt"><input class="easyui-numberbox" tabindex="22" col="F13" id="<%=DefineReport.InpText.JYOGAITEN.getObj()%>" check='<%=DefineReport.InpText.JYOGAITEN.getMaxlenTag()%>' data-options="" style="width:50px;" for_btn="<%=DefineReport.Button.TAISYOTEN.getObj()%>_del_F1"></td>
				</tr>
			</tbody>
		</table>
<table  id="dg3" class="like_datagrid">
		<thead>
			<tr>
				<th style="width: 80px;" colspan="2">
					<a href="#" class="easyui-linkbutton" tabindex="31" id="<%=DefineReport.Button.TENKAKUNIN.getObj()%>" title="<%=DefineReport.Button.TENKAKUNIN.getTxt()%>" style="width:110px;"><span><%=DefineReport.Button.TENKAKUNIN.getTxt()%></span></a>
				</th>
			</tr>
		</thead>
 	<!-- <tr><th style="max-width:600px;"></th><th style="max-width:600px;"></th></tr> -->
	<%-- <tr>
		<td><a href="#" class="easyui-linkbutton" tabindex="19" id="<%=DefineReport.Button.TAISYOTEN.getObj()%>_add" title="<%=DefineReport.Button.TAISYOTEN.getTxt()%>" iconCls="icon-edit"><span class="btnTxt2" style="width:40px;text-align:left"><%=DefineReport.Button.TAISYOTEN.getTxt()%></span></a></td>
		<td><a href="#" class="easyui-linkbutton" tabindex="20" id="<%=DefineReport.Button.JYOGAITEN.getObj()%>_del" title="<%=DefineReport.Button.JYOGAITEN.getTxt()%>" iconCls="icon-edit"><span class="btnTxt2" style="width:40px;text-align:left"><%=DefineReport.Button.JYOGAITEN.getTxt()%></span></a></td>
	</tr>
	<tr>
		<td><div id = "inputno" class="inp_box"><input class="easyui-numberbox" tabindex="21" col="F12" id="<%=DefineReport.InpText.RANKNO_ADD.getObj()%>" check='<%=DefineReport.InpText.RANKNO_ADD.getMaxlenTag()%>' style="width:70px;" for_btn="<%=DefineReport.Button.TAISYOTEN.getObj()%>_add_F1"></div></td>
		<td><div id = "inputno" class="inp_box"><input class="easyui-numberbox" tabindex="22" col="F13" id="<%=DefineReport.InpText.RANKNO_DEL.getObj()%>" check='<%=DefineReport.InpText.RANKNO_DEL.getMaxlenTag()%>' style="width:70px;" for_btn="<%=DefineReport.Button.TAISYOTEN.getObj()%>_del_F1"></td>
	</tr> --%>
	<%-- <tr>
		<td colspan=2>
			<a href="#" class="easyui-linkbutton" tabindex="31" id="<%=DefineReport.Button.TENKAKUNIN.getObj()%>" title="<%=DefineReport.Button.TENKAKUNIN.getTxt()%>" iconCls="icon-edit"><span class="btnTxt2" style="width:120px;text-align:center"><%=DefineReport.Button.TENKAKUNIN.getTxt()%></span></a>
		</td>
	</tr> --%>
	<tr>
		<td class="col_txt"><input class="easyui-numberbox" tabindex="32" col="F31" id="<%=DefineReport.InpText.TENCD.getObj()%>_add_1" check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' data-options="min:0,max:400" style="width:50px;"></td>
		<td class="col_txt"><input class="easyui-numberbox" tabindex="33" col="F21" id="<%=DefineReport.InpText.TENCD.getObj()%>_del_1" check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' data-options="min:0,max:400" style="width:50px;"></td>
	</tr>
	<tr>
		<td class="col_txt"><input class="easyui-numberbox" tabindex="34" col="F32" id="<%=DefineReport.InpText.TENCD.getObj()%>_add_2" check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' data-options="min:0,max:400" style="width:50px;"></td>
		<td class="col_txt"><input class="easyui-numberbox" tabindex="35" col="F22" id="<%=DefineReport.InpText.TENCD.getObj()%>_del_2" check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' data-options="min:0,max:400" style="width:50px;"></td>
	</tr>
	<tr>
		<td class="col_txt"><input class="easyui-numberbox" tabindex="36" col="F33" id="<%=DefineReport.InpText.TENCD.getObj()%>_add_3" check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' data-options="min:0,max:400" style="width:50px;"></td>
		<td class="col_txt"><input class="easyui-numberbox" tabindex="37" col="F23" id="<%=DefineReport.InpText.TENCD.getObj()%>_del_3" check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' data-options="min:0,max:400" style="width:50px;"></td>
	</tr>
	<tr>
		<td class="col_txt"><input class="easyui-numberbox" tabindex="38" col="F34" id="<%=DefineReport.InpText.TENCD.getObj()%>_add_4" check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' data-options="min:0,max:400" style="width:50px;"></td>
		<td class="col_txt"><input class="easyui-numberbox" tabindex="39" col="F24" id="<%=DefineReport.InpText.TENCD.getObj()%>_del_4" check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' data-options="min:0,max:400" style="width:50px;"></td>
	</tr>
	<tr>
		<td class="col_txt"><input class="easyui-numberbox" tabindex="41" col="F35" id="<%=DefineReport.InpText.TENCD.getObj()%>_add_5" check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' data-options="min:0,max:400" style="width:50px;"></td>
		<td class="col_txt"><input class="easyui-numberbox" tabindex="42" col="F25" id="<%=DefineReport.InpText.TENCD.getObj()%>_del_5" check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' data-options="min:0,max:400" style="width:50px;"></td>
	</tr>
	<tr>
		<td class="col_txt"><input class="easyui-numberbox" tabindex="43" col="F36" id="<%=DefineReport.InpText.TENCD.getObj()%>_add_6" check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' data-options="min:0,max:400" style="width:50px;"></td>
		<td class="col_txt"><input class="easyui-numberbox" tabindex="44" col="F26" id="<%=DefineReport.InpText.TENCD.getObj()%>_del_6" check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' data-options="min:0,max:400" style="width:50px;"></td>
	</tr>
		<tr>
		<td class="col_txt"><input class="easyui-numberbox" tabindex="45" col="F37" id="<%=DefineReport.InpText.TENCD.getObj()%>_add_7" check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' data-options="min:0,max:400" style="width:50px;"></td>
		<td class="col_txt"><input class="easyui-numberbox" tabindex="46" col="F27" id="<%=DefineReport.InpText.TENCD.getObj()%>_del_7" check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' data-options="min:0,max:400" style="width:50px;"></td>
	</tr>
	<tr>
		<td class="col_txt"><input class="easyui-numberbox" tabindex="47" col="F38" id="<%=DefineReport.InpText.TENCD.getObj()%>_add_8" check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' data-options="min:0,max:400" style="width:50px;"></td>
		<td class="col_txt"><input class="easyui-numberbox" tabindex="48" col="F28" id="<%=DefineReport.InpText.TENCD.getObj()%>_del_8" check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' data-options="min:0,max:400" style="width:50px;"></td>
	</tr>
	<tr>
		<td class="col_txt"><input class="easyui-numberbox" tabindex="49" col="F39" id="<%=DefineReport.InpText.TENCD.getObj()%>_add_9" check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' data-options="min:0,max:400" style="width:50px;"></td>
		<td class="col_txt"><input class="easyui-numberbox" tabindex="50" col="F29" id="<%=DefineReport.InpText.TENCD.getObj()%>_del_9" check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' data-options="min:0,max:400" style="width:50px;"></td>
	</tr>
	<tr>
		<td class="col_txt"><input class="easyui-numberbox" tabindex="51" col="F40" id="<%=DefineReport.InpText.TENCD.getObj()%>_add_10" check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' data-options="min:0,max:400" style="width:50px;"></td>
		<td class="col_txt"><input class="easyui-numberbox" tabindex="52" col="F30" id="<%=DefineReport.InpText.TENCD.getObj()%>_del_10" check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' data-options="min:0,max:400" style="width:50px;"></td>
	</tr>
</table>
</td></tr>
</table>
	 <div style="display: none;">
		<input type="hidden" name="<%=DefineReport.Text.SHORIDT.getObj()%>" id="<%=DefineReport.Text.SHORIDT.getObj()%>" />
		<!-- <input type="hidden" name="txt_plukbn" id="txt_plukbn" col="F20"/> -->
		<input class="easyui-numberbox" tabindex="-1" id="txt_plusflg"  for_inp="<%=DefineReport.InpText.MOYSCD.getObj()%>_F7" style="width:100px;">
		<input class="easyui-numberbox" tabindex="-1" col="F20" id="txt_plukbn"  for_inp="<%=DefineReport.InpText.MOYSCD.getObj()%>_F8" style="width:100px;">

	</div>
</div>
</form>

<div id="buttons" data-options="region:'south',border:false" style="display:none;height:30px;padding:0 20px;">
	<div class="btn" style="float: left;">
		<table class="t-layout3">
		<tr>
		<th style="min-width:135px;"></th><th style="min-width:10px;"></th>
		<th style="min-width:135px;"></th>
		<th style="min-width:135px;"></th>
		<th style="min-width:135px;"></th>
		</tr>
		<tr>
		<td><a href="#" title="戻る" id="<%=DefineReport.Button.BACK.getObj()%>" class="easyui-linkbutton" tabindex="53" iconCls="icon-undo" style="width:125px;"><span class="btnTxt">戻る</span></a></td>
		<td></td>
		<td><a href="#" class="easyui-linkbutton" tabindex="54" id="<%=DefineReport.Button.CANCEL.getObj()%>" title="<%=DefineReport.Button.CANCEL.getTxt()%>" iconCls="icon-cancel" style="width:125px;"><span><%=DefineReport.Button.CANCEL.getTxt()%></span></a></td>
		<td><a href="#" class="easyui-linkbutton" tabindex="55" id="<%=DefineReport.Button.UPD.getObj()%>" title="<%=DefineReport.Button.UPD.getTxt()%>" iconCls="icon-save" style="width:125px;"><span class="btnTxt"><%=DefineReport.Button.UPD.getTxt()%></span></a></td>
		<td><a href="#" class="easyui-linkbutton" tabindex="56" id="<%=DefineReport.Button.DEL.getObj()%>" title="<%=DefineReport.Button.DEL.getTxt()%>" iconCls="icon-remove" style="width:125px;"><span class="btnTxt"><%=DefineReport.Button.DEL.getTxt()%></span></a></td>
		</tr>
		</table>
	</div>
	<div id="disp_record_info" style="float: right;">
		<span class="labelName" tabindex="-1" style="padding-top: 3px;">
				登録日 <span col="F15" id="<%=DefineReport.Text.ADDDT.getObj()%>"></span>　更新日 <span col="F16" id="<%=DefineReport.Text.UPDDT.getObj()%>"></span>　オペレータ <span col="F14" id="<%=DefineReport.Text.OPERATOR.getObj()%>"></span>
		</span>
		<input type="hidden" col="F17" name="<%=DefineReport.Hidden.UPDDT.getObj()%>" id="<%=DefineReport.Hidden.UPDDT.getObj()%>" />
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
	<jsp:include page="Out_Reportwin_BM015.jsp" flush="true" />
	<jsp:include page="Out_ReportwinST009.jsp" flush="true" />
	<jsp:include page="Out_ReportwinST007.jsp" flush="true"  />
	<jsp:include page="Out_ReportwinST008.jsp" flush="true" />
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
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.BT002.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.winBM015.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.winST009.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.winST008.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.winST007.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.control.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.events.js?v=<%=prm %>"></script>
</html>