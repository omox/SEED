<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="MS932" %>
<%@ page import="java.util.*" %>
<%@ page import="common.DefineReport" %>
<%@ page import="common.Defines" %>
<%@ page import="authentication.defines.*" %>
<%@ page import="authentication.bean.*" %>
<%@ page import="authentication.dbaccess.DBinfo" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%
	User lusr = (User)request.getSession().getAttribute(Consts.STR_SES_LOGINUSER);
	ArrayList menu = (ArrayList)request.getSession().getAttribute(Consts.STR_SES_REPSIDE);
	ArrayList rowdisp = (ArrayList)request.getSession().getAttribute("DISP");
	ArrayList mstr = (ArrayList)request.getSession().getAttribute(Consts.STR_SES_MSTSIDE);
	String repside = (String)request.getParameter(Form.REPORT_SIDE);
	String info = (String)request.getAttribute(Form.MENU_INFO_BTN);

	String userId	= lusr.getId();
	String user		= String.valueOf(lusr.getCD_user());
	String menuKbn	= request.getParameter(Form.PRM_MENU_KBN);

	// ���|�[�g�ԍ�
	String reportNo		=	(String)request.getSession().getAttribute(Defines.ID_REQUEST_JSP_REPORT);
	// �����l�擾
	String ro			=	(String)request.getSession().getAttribute(Defines.ID_REQUEST_CUSTOM_REPORT);
	// ���|�[�g���擾
	String reportName	=	(String)request.getSession().getAttribute(Defines.ID_REQUEST_TITLE_REPORT);
	// ���|�[�g�\�����
	String reportYobi1	=	(String)request.getSession().getAttribute(Defines.ID_REQUEST_REPORT_YOBI1);
	String reportYobi2	=	(String)request.getSession().getAttribute(Defines.ID_REQUEST_REPORT_YOBI2);

	// �^�C�g��
	String titleName	=	"�y " + reportName + " �z";
	// ������������
	String initParam	= (String)request.getSession().getAttribute(Defines.ID_REQUEST_INIT_PARAM);

	// �e��ʂ���̈����p�����
	String sendParam	= "";
	if (request.getSession().getAttribute(Defines.ID_REQUEST_SEND_PARAM)!=null) {
		sendParam	=	(String)request.getSession().getAttribute(Defines.ID_REQUEST_SEND_PARAM);
	}
	// ���|�[�gID
	String report	=	(String)request.getSession().getAttribute(Defines.ID_REQUEST_REPORT_NO);

	String userTenpo = lusr.getTenpo();
	String userBumon = lusr.getBumon();

	// js�L���b�V���Ή�
	String prm = request.getSession().getId();
%>
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
	<form id="login" method="post" action="../Servlet/Login.do" style="display: inline">
		<div data-options="region:'north',border:false" style="display:none;">
			<table class="t-layout1" style="margin:10px 10px 0px 10px;;">
				<tr>
					<td style="height:30px;"" colspan="3">��p�X���[�h�̕ύX��</td>
				</tr>
				<tr>
					<td style="height:30px;color: red;">*</td>
					<td style="height:30px;">���݂̃p�X���[�h</td>
					<td style="height:30px;">
						<input class="easyui-textbox" col="F1" tabindex="1" id="<%=DefineReport.InpText.PASS.getObj()%>_old" check='<%=DefineReport.InpText.PASS.getMaxlenTag()%>' data-options="required:true" style="width:170px;">
					</td>
				</tr>
				<tr>
					<td style="height:30px;color: red;">*</td>
					<td style="height:30px;">�V�p�X���[�h</td>
					<td style="height:30px;">
						<input class="easyui-textbox" col="F2" tabindex="2" id="<%=DefineReport.InpText.PASS.getObj()%>_new" check='<%=DefineReport.InpText.PASS.getMaxlenTag()%>' data-options="required:true" style="width:170px;">
					</td>
				</tr>
				<tr>
					<td style="height:30px;color: red;">*</td>
					<td style="height:30px;">�V�p�X���[�h(�m�F�p)</td>
					<td style="height:30px;">
						<input class="easyui-textbox" col="F2" tabindex="3" id="<%=DefineReport.InpText.PASS.getObj()%>_check" check='<%=DefineReport.InpText.PASS.getMaxlenTag()%>' data-options="required:true" style="width:170px;">
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
				<th style="min-width:120px;"></th></tr>
			<tr>
			<td><a href="#" title="�߂�" id="<%=DefineReport.Button.BACK.getObj()%>" class="easyui-linkbutton" tabindex="4" iconCls="icon-undo" style="width:125px;"><span class="btnTxt">�߂�</span></a></td>
			<td></td>
			<td><a href="#" title="�o�^" class="easyui-linkbutton" tabindex="44" id="<%=DefineReport.Button.UPD.getObj()%>" title="<%=DefineReport.Button.UPD.getTxt()%>" iconCls="icon-save" style="width:110px;"><span class="btnTxt"><%=DefineReport.Button.UPD.getTxt()%></span></a></td>
			</tr>
			</table>
		</div>
	</div>
</div>
<div id="debug" style="visibility: hidden;">
	<!-- report ��� -->
	<input type="hidden" name="reportno" id="reportno" value="Pass"/>
	<!-- ���|�[�g�� ��� -->
	<input type="hidden" name="reportname" id="reportname" value="<%=reportName %>"/>
	<!-- ���[�U�[ ��� -->
	<input type="hidden" name="userid" id="userid" value="<%=userId %>"/>
	<input type="hidden" name="menukbn" id="menukbn" value="<%=menuKbn %>"/>
	<input type="hidden" name="userTenpo" id="userTenpo" value="<%=userTenpo %>"/>
	<input type="hidden" name="userBumon" id="userBumon" value="<%=userBumon %>"/>
	<!-- �����p�����(JSON������ϊ��� "'" �ň͂�) -->
	<input type="hidden" name="hiddenParam" id="hiddenParam" value='<%=sendParam %>' />
	<!-- ���[�UID -->
	<input type="hidden" name="hiddenUser" id="hiddenUser" value="<%=user %>" />
	<!-- ���|�[�gID -->
	<input type="hidden" name="hiddenReport" id="hiddenReport" value="<%=report %>" />
	<!-- �����������(JSON������ϊ��� "'" �ň͂�) -->
	<input type="hidden" name="hiddenInit" id="hiddenInit" value='<%=initParam %>' />

	<input type="hidden" name="<%=DefineReport.Hidden.CHANGED_IDX.getObj()%>" id="<%=DefineReport.Hidden.CHANGED_IDX.getObj()%>" />

	<!-- �����������(JSON������ϊ��� "'" �ň͂�) -->
	<input type="hidden" name="reportYobi1" id="reportYobi1" value='<%=reportYobi1 %>' />
	<input type="hidden" name="reportYobi2" id="reportYobi2" value='<%=reportYobi2 %>' />
</div>
</body>
<!-- Load jQuery and JS files -->
<script type="text/javascript" src="../js/jquery.min.js?v=<%=prm %>"></script>			<!-- jquery -->
<script type="text/javascript" src="../js/jquery.easyui.min.js?v=<%=prm %>"></script>	<!-- EasyUI framework -->
<script type="text/javascript" src="../js/easyui-lang-ja.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.info.control.js"></script>

<script type="text/javascript" src="../js/json2.min.js"></script>			<!-- json plugin -->
<script type="text/javascript" src="../js/exdate.js"></script>				<!-- exdate plugin -->

<script type="text/javascript" src="../js/jshashtable-2.1.js"></script>		<!-- jshashset plugin -->
<script type="text/javascript" src="../js/jquery.numberformatter.min.js"></script><!-- numberformatter plugin -->

<script type="text/javascript" src="../js/shortcut.js"></script>			<!-- shortcut plugin -->

<!-- Report Option & Control & Event  -->
<script type="text/javascript" src="../frame/js/Pass.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.control.js?v=<%=prm %>"></script>

<!-- ***** javaScript ***** -->
<script type="text/javascript">

	$(function(){
		$.report($.reportOption);

		var that = this

		// �}�X�N�ǉ�
		$.appendMask();

		var reportNumber = "Pass"
		$.report[0].initialize(reportNumber);

		(function() {
			// �}�X�N�ǉ�
			$.appendMask();

		});
	});
</script>

<style type="text/css">
	td { height: 25px; }

	input {
		height: 18pt;
		font-size: 10pt;

	}
</style>

</html>