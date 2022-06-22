
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
<div id="list" data-options="region:'center',border:false" style="display:none;">
<table class="t-layout" style="margin:10px;">
	<tr>
		<td style="height:30px;">
			<input class="easyui-numberbox" tabindex="1" col="F1" id="<%=DefineReport.InpText.MOYSCD.getObj()%>" check='<%=DefineReport.InpText.MOYSCD.getMaxlenTag()%>' data-options="prompt:'_-______-___',label:'催しコード',labelWidth:105,required:true" style="width:205px;">
		</td>
		<td>
			<input type="hidden" col="F20" name="<%=DefineReport.InpText.MOYSKBN.getObj()%>" id="<%=DefineReport.InpText.MOYSKBN.getObj()%>" />
			<input type="hidden" col="F22" name="<%=DefineReport.InpText.MOYSRBAN.getObj()%>" id="<%=DefineReport.InpText.MOYSRBAN.getObj()%>" />
		</td>
	</tr>
	<tr>
		<td style="height:30px;">
			<input class="easyui-textbox" tabindex="2" col="F2" id="<%=DefineReport.InpText.MOYKN.getObj()%>" data-options="label:'催し名称',labelWidth:105" style="width:420px;" for_inp="<%=DefineReport.InpText.MOYSCD.getObj()%>_F2" check='<%=DefineReport.InpText.MOYKN.getMaxlenTag()%>'/>
		</td>
	</tr>
	<tr>
		<td style="height:30px;">
			<input class="easyui-numberbox" tabindex="3" col="F3" id="<%=DefineReport.InpText.MOYSSTDT.getObj()%>" for_inp="<%=DefineReport.InpText.MOYSCD.getObj()%>_F4" check='<%=DefineReport.InpText.MOYSSTDT.getMaxlenTag()%>' data-options="prompt:'<%=DefineReport.Label.PROMPT_DTW.getTxt()%>',label:'催し期間',labelWidth:105" style="width:200px;">
			<input class="easyui-numberbox" tabindex="4" col="F4" id="<%=DefineReport.InpText.MOYSEDDT.getObj()%>" for_inp="<%=DefineReport.InpText.MOYSCD.getObj()%>_F5" check='<%=DefineReport.InpText.MOYSEDDT.getMaxlenTag()%>' data-options="prompt:'<%=DefineReport.Label.PROMPT_DTW.getTxt()%>',label:'～',labelWidth:20" style="width:115px;">
			<input class="easyui-numberbox" tabindex="5" col="F5" id="<%=DefineReport.InpText.PLUSDDT.getObj()%>" for_inp="<%=DefineReport.InpText.MOYSCD.getObj()%>_F6" check='<%=DefineReport.InpText.PLUSDDT.getMaxlenTag()%>' data-options="prompt:'<%=DefineReport.Label.PROMPT_DTW.getTxt()%>',label:'　　PLU配信日',labelWidth:90" style="width:185px;">
		</td>
	</tr>
	<tr>
		<td style="height:30px;">
			<input class="easyui-numberbox" tabindex="-1" col="F6" id="<%=DefineReport.InpText.BMNNO.getObj()%>" data-options="label:'B/M番号',labelWidth:105" style="width:140px;" check='<%=DefineReport.InpText.BMNNO.getMaxlenTag()%>'/>
		</td>
		<td rowspan="2" style="height:30px;">
			<fieldset style="width:140px;margin-left: -150px;">
				<legend>B/Mタイプ</legend>
				<div class="inp_box">
					<label class="rad_lbl"><input type="radio" col="F8" tabindex="-1" name="<%=DefineReport.Radio.BMTYP.getObj()%>" value="1" style="vertical-align:middle;" checked="checked"/>一段階</label>
					<label class="rad_lbl"><input type="radio" col="F8" tabindex="-1" name="<%=DefineReport.Radio.BMTYP.getObj()%>" value="2" style="vertical-align:middle;"/>二段階</label>
				</div>
			</fieldset>
		</td>
	</tr>
	<tr>
		<td style="height:30px;">
			<input class="easyui-textbox" tabindex="6" col="F7" id="<%=DefineReport.InpText.BMNMAN.getObj()%>" data-options="label:'B/M名称（ｶﾅ）',labelWidth:105" style="width:270px;" check='<%=DefineReport.InpText.BMNMAN.getMaxlenTag()%>'>
		</td>
	</tr>
	<tr>
		<td style="height:30px;">
			<input class="easyui-textbox" tabindex="7" col="F9" id="<%=DefineReport.InpText.BMNMKN.getObj()%>" data-options="label:'B/M名称（漢字）',labelWidth:105" style="width:420px;" check='<%=DefineReport.InpText.BMNMKN.getMaxlenTag()%>'/>
		</td>
	</tr>
	<tr>
		<td style="height:30px;">
			<input class="easyui-numberbox" tabindex="8" col="F10" id="<%=DefineReport.InpText.BAIKAAM.getObj()%>" data-options="label:'１個売り総売価',labelWidth:105,required:true,min:1" style="width:170px;text-align: right;" check='<%=DefineReport.InpText.BAIKAAM.getMaxlenTag()%>'/>
			<label>円</label>
		</td>
	</tr>
	<tr>
		<td style="height:30px;">
			<input class="easyui-numberbox" tabindex="9" col="F11" id="<%=DefineReport.InpText.BD_KOSU1.getObj()%>" data-options="label:'個数総売価　　　（１）',labelWidth:137,required:true,min:1" style="width:180px;text-align: right;" check='<%=DefineReport.InpText.BD_KOSU1.getMaxlenTag()%>'/>
			<input class="easyui-numberbox" tabindex="10" col="F12" id="<%=DefineReport.InpText.BD_BAIKAAN1.getObj()%>" data-options="label:'個',labelWidth:20,required:true,min:1" style="width:90px;text-align: right;" check='<%=DefineReport.InpText.BD_BAIKAAN1.getMaxlenTag()%>'/>
			<input class="easyui-numberbox" tabindex="11" col="F13" id="<%=DefineReport.InpText.BD_KOSU2.getObj()%>" data-options="label:'円　　（２）',labelWidth:80,required:true,min:1" style="width:120px;text-align: right;" check='<%=DefineReport.InpText.BD_KOSU2.getMaxlenTag()%>'/>
			<input class="easyui-numberbox" tabindex="12" col="F14" id="<%=DefineReport.InpText.BD_BAIKAAN2.getObj()%>" data-options="label:'個',labelWidth:20,required:true,min:1" style="width:90px;text-align: right;" check='<%=DefineReport.InpText.BD_BAIKAAN2.getMaxlenTag()%>'/>
			<label>円</label>
		</td>
	</tr>
	<tr>
		<td style="height:30px;">
			<input class="easyui-numberbox" tabindex="13" col="F15" id="<%=DefineReport.InpText.HBSTDT.getObj()%>" check='<%=DefineReport.InpText.HBSTDT.getMaxlenTag()%>' data-options="prompt:'<%=DefineReport.Label.PROMPT_DTW.getTxt()%>',label:'販売期間',labelWidth:105" style="width:200px;">
			<input class="easyui-numberbox" tabindex="14" col="F16" id="<%=DefineReport.InpText.HBEDDT.getObj()%>" check='<%=DefineReport.InpText.HBEDDT.getMaxlenTag()%>' data-options="prompt:'<%=DefineReport.Label.PROMPT_DTW.getTxt()%>',label:'～',labelWidth:20" style="width:115px;">
			<input type="hidden" col="F47" name="<%=DefineReport.InpText.HBSTDT.getObj()%>" id="<%=DefineReport.InpText.HBSTDT.getObj()%>" />
			<input type="hidden" col="F48" name="<%=DefineReport.InpText.HBEDDT.getObj()%>" id="<%=DefineReport.InpText.HBEDDT.getObj()%>" />
		</td>
	</tr>
	<tr>
		<td style="height:30px;padding-right: 8px; width:570px">
		<div style="height:350px;width:570px">
		<div class="easyui-datagrid" tabindex="15" id="<%=DefineReport.Grid.BMSHN.getObj()%>_list" data-options="singleSelect:true,rownumbers:true, fit:true"></div>
		<!-- Editor参照用 -->
		<div class="ref_editor" style="display: none;">
			<input type="checkbox" tabindex="-1" id="<%=DefineReport.Checkbox.UPD.getObj()%>"/>
			<input  class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.SHNCD.getObj()%>" for_inp="<%=DefineReport.InpText.SHNCD.getObj()%>_F3" check='<%=DefineReport.InpText.SHNCD.getMaxlenTag()%>' data-options="prompt:'____-____'" style="width:80px;"/>
			<input  class="easyui-textbox"   tabindex="-1" id="<%=DefineReport.InpText.SHNKN.getObj()%>" for_inp="<%=DefineReport.InpText.SHNCD.getObj()%>_F1" check='<%=DefineReport.InpText.SHNKN.getMaxlenTag()%>' />
			<input  class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.GENKAAM.getObj()%>" for_inp="<%=DefineReport.InpText.SHNCD.getObj()%>_F2" check='<%=DefineReport.InpText.GENKAAM.getMaxlenTag()%>' style="text-align: right;"/>
		</div>

		</div>

		</td>
		<td style="height: 77px;width:382px;padding-right: 5px;">

		<input class="easyui-numberbox" tabindex="16" col="F17" id="<%=DefineReport.InpText.BMNCD.getObj()%>" data-options="label:'　　　　部門',labelWidth:80,required:true" style="width:120px;" check='<%=DefineReport.InpText.BMNCD.getMaxlenTag()%>'/>
		<table id="dg2" class="like_datagrid">
			<thead>
				<tr>
				<th>
					<a href="#" class="easyui-linkbutton" tabindex="17" id="<%=DefineReport.Button.TAISYOTEN.getObj()%>_add" title="<%=DefineReport.Button.TAISYOTEN.getTxt()%>" style="width:50px;"><span><%=DefineReport.Button.TAISYOTEN.getTxt()%></span></a>
				</th>
				<th>
					<a href="#" class="easyui-linkbutton" tabindex="18" id="<%=DefineReport.Button.JYOGAITEN.getObj()%>_del" title="<%=DefineReport.Button.JYOGAITEN.getTxt()%>" style="width:50px;"><span><%=DefineReport.Button.JYOGAITEN.getTxt()%></span></a>
				</th>
				</tr>
			</thead>
			<tbody>
				<tr>
					<td class="col_txt"><input class="easyui-numberbox" tabindex="19" col="F18" id="<%=DefineReport.InpText.TAISYOTEN.getObj()%>" check='<%=DefineReport.InpText.TAISYOTEN.getMaxlenTag()%>' data-options="required:true" style="width:50px;" for_btn="<%=DefineReport.Button.TAISYOTEN.getObj()%>_add_F1"></td>
					<td class="col_txt"><input class="easyui-numberbox" tabindex="20" col="F19" id="<%=DefineReport.InpText.JYOGAITEN.getObj()%>" check='<%=DefineReport.InpText.JYOGAITEN.getMaxlenTag()%>' data-options="" style="width:50px;" for_btn="<%=DefineReport.Button.TAISYOTEN.getObj()%>_del_F1"></td>
				</tr>
			</tbody>
		</table>
		<table id="dg3" class="like_datagrid">
			<thead>
				<tr>
				<th style="width: 80px;" colspan="2">
					<a href="#" class="easyui-linkbutton" tabindex="21" id="<%=DefineReport.Button.TENKAKUNIN.getObj()%>" title="<%=DefineReport.Button.TENKAKUNIN.getTxt()%>" style="width:110px;"><span><%=DefineReport.Button.TENKAKUNIN.getTxt()%></span></a>
				</th>
				</tr>
			</thead>
			<tbody>
				<tr>
					<td class="col_txt"><input class="easyui-numberbox" tabindex="22" col="F37" id="<%=DefineReport.InpText.TENCD.getObj()%>_add_1" check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' data-options="" style="width:50px;"></td>
					<td class="col_txt"><input class="easyui-numberbox" tabindex="32" col="F27" id="<%=DefineReport.InpText.TENCD.getObj()%>_del_1" check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' data-options="" style="width:50px;"></td>
				</tr>
				<tr>
					<td class="col_txt"><input class="easyui-numberbox" tabindex="23" col="F38" id="<%=DefineReport.InpText.TENCD.getObj()%>_add_2" check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' data-options="" style="width:50px;"></td>
					<td class="col_txt"><input class="easyui-numberbox" tabindex="33" col="F28" id="<%=DefineReport.InpText.TENCD.getObj()%>_del_2" check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' data-options="" style="width:50px;"></td>
				</tr>
				<tr>
					<td class="col_txt"><input class="easyui-numberbox" tabindex="24" col="F39" id="<%=DefineReport.InpText.TENCD.getObj()%>_add_3" check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' data-options="" style="width:50px;"></td>
					<td class="col_txt"><input class="easyui-numberbox" tabindex="34" col="F29" id="<%=DefineReport.InpText.TENCD.getObj()%>_del_3" check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' data-options="" style="width:50px;"></td>
				</tr>
				<tr>
					<td class="col_txt"><input class="easyui-numberbox" tabindex="25" col="F40" id="<%=DefineReport.InpText.TENCD.getObj()%>_add_4" check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' data-options="" style="width:50px;"></td>
					<td class="col_txt"><input class="easyui-numberbox" tabindex="35" col="F30" id="<%=DefineReport.InpText.TENCD.getObj()%>_del_4" check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' data-options="" style="width:50px;"></td>
				</tr>
				<tr>
					<td class="col_txt"><input class="easyui-numberbox" tabindex="26" col="F41" id="<%=DefineReport.InpText.TENCD.getObj()%>_add_5" check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' data-options="" style="width:50px;"></td>
					<td class="col_txt"><input class="easyui-numberbox" tabindex="36" col="F31" id="<%=DefineReport.InpText.TENCD.getObj()%>_del_5" check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' data-options="" style="width:50px;"></td>
				</tr>
				<tr>
					<td class="col_txt"><input class="easyui-numberbox" tabindex="27" col="F42" id="<%=DefineReport.InpText.TENCD.getObj()%>_add_6" check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' data-options="" style="width:50px;"></td>
					<td class="col_txt"><input class="easyui-numberbox" tabindex="37" col="F32" id="<%=DefineReport.InpText.TENCD.getObj()%>_del_6" check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' data-options="" style="width:50px;"></td>
				</tr>
				<tr>
					<td class="col_txt"><input class="easyui-numberbox" tabindex="28" col="F43" id="<%=DefineReport.InpText.TENCD.getObj()%>_add_7" check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' data-options="" style="width:50px;"></td>
					<td class="col_txt"><input class="easyui-numberbox" tabindex="38" col="F33" id="<%=DefineReport.InpText.TENCD.getObj()%>_del_7" check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' data-options="" style="width:50px;"></td>
				</tr>
				<tr>
					<td class="col_txt"><input class="easyui-numberbox" tabindex="29" col="F44" id="<%=DefineReport.InpText.TENCD.getObj()%>_add_8" check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' data-options="" style="width:50px;"></td>
					<td class="col_txt"><input class="easyui-numberbox" tabindex="39" col="F34" id="<%=DefineReport.InpText.TENCD.getObj()%>_del_8" check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' data-options="" style="width:50px;"></td>
				</tr>
				<tr>
					<td class="col_txt"><input class="easyui-numberbox" tabindex="30" col="F45" id="<%=DefineReport.InpText.TENCD.getObj()%>_add_9" check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' data-options="" style="width:50px;"></td>
					<td class="col_txt"><input class="easyui-numberbox" tabindex="40" col="F35" id="<%=DefineReport.InpText.TENCD.getObj()%>_del_9" check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' data-options="" style="width:50px;"></td>
				</tr>
				<tr>
					<td class="col_txt"><input class="easyui-numberbox" tabindex="31" col="F46" id="<%=DefineReport.InpText.TENCD.getObj()%>_add_10" check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' data-options="" style="width:50px;"></td>
					<td class="col_txt"><input class="easyui-numberbox" tabindex="41" col="F36" id="<%=DefineReport.InpText.TENCD.getObj()%>_del_10" check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' data-options="" style="width:50px;"></td>
				</tr>
			</tbody>
		</table>
		</td>
	</tr>
</table>
</div>
</form>
<div id="buttons" data-options="region:'south',border:false" style="display:none;height:30px;padding:0 20px;">
	<div class="btn" style="float: left;">
		<table class="t-layout3">
		<tr>
			<th style="min-width:135px;"></th><th style="min-width:10px;">
			<th style="min-width:120px;"></th><th style="min-width:120px;"></th><th style="min-width:120px;"></th></tr>
		<tr>
		<td><a href="#" title="戻る" id="<%=DefineReport.Button.BACK.getObj()%>" class="easyui-linkbutton" tabindex="42" iconCls="icon-undo" style="width:125px;"><span class="btnTxt">戻る</span></a></td>
		<td></td>
		<td><a href="#" title="キャンセル" class="easyui-linkbutton" tabindex="43" id="<%=DefineReport.Button.CANCEL.getObj()%>" iconCls="icon-cancel" style="width:110px;"><%=DefineReport.Button.CANCEL.getTxt()%></a></td>
		<td><a href="#" title="登録" class="easyui-linkbutton" tabindex="44" id="<%=DefineReport.Button.UPD.getObj()%>" title="<%=DefineReport.Button.UPD.getTxt()%>" iconCls="icon-save" style="width:110px;"><span class="btnTxt"><%=DefineReport.Button.UPD.getTxt()%></span></a></td>
		<td><a href="#" title="削除" class="easyui-linkbutton" tabindex="45" id="<%=DefineReport.Button.DEL.getObj()%>" title="<%=DefineReport.Button.DEL.getTxt()%>" iconCls="icon-remove" style="width:110px;"><span class="btnTxt">全削除</span></a></td>
		</tr>
		</table>
	</div>
	<div id="disp_record_info" style="float: right;">
		<span class="labelName" tabindex="-1" style="padding-top: 3px;">
			登録日 <span col="F24" id="<%=DefineReport.Text.ADDDT.getObj()%>"></span>　更新日 <span col="F25" id="<%=DefineReport.Text.UPDDT.getObj()%>"></span>　オペレータ <span col="F23" id="<%=DefineReport.Text.OPERATOR.getObj()%>"></span>
		</span>
		<input type="hidden" col="F26" name="<%=DefineReport.Hidden.UPDDT.getObj()%>" id="<%=DefineReport.Hidden.UPDDT.getObj()%>" />
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
	<jsp:include page="Out_ReportwinST009.jsp" flush="true" />
	<jsp:include page="Out_ReportwinST007.jsp" flush="true" />
	<jsp:include page="Out_Reportwin_BM015.jsp" flush="true" />
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
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.BM006.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.winST009.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.winST007.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.winBM015.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.control.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.events.js?v=<%=prm %>"></script>
</html>