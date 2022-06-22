<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="MS932" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="authentication.bean.*" %>
<%@ page import="authentication.defines.*" %>
<%@ page import="authentication.defines.Defines" %>
<%@ page import="authentication.dbaccess.DBinfo" %>

<%
	User lusr = (User)request.getSession().getAttribute(Consts.STR_SES_LOGINUSER);
	ArrayList menu = (ArrayList)request.getSession().getAttribute(Consts.STR_SES_REPSIDE);
	ArrayList rowdisp = (ArrayList)request.getSession().getAttribute("DISP");
	ArrayList mstr = (ArrayList)request.getSession().getAttribute(Consts.STR_SES_MSTSIDE);
	String repside = (String)request.getParameter(Form.REPORT_SIDE);
	String info = (String)request.getAttribute(Form.MENU_INFO_BTN);

	//�p�X���[�h�L�������x�����b�Z�[�W�擾
	String pw_warning_msg = request.getAttribute(Defines.STR_SES_PASS_TERM_MSG) != null ? (String)request.getAttribute(Defines.STR_SES_PASS_TERM_MSG) : "";
	//������
	request.removeAttribute(Defines.STR_SES_PASS_TERM_MSG);
	// js�L���b�V���Ή�
	String prm = request.getSession().getId();
%>

<html>
<head>
<meta http-equiv="X-UA-Compatible" content="IE=edge" />
<meta http-equiv="Pragma" content="no-cache">
<meta http-equiv="Cache-Control" content="no-cache">
<meta http-equiv="Expires" content="Thu, 01 Dec 1994 16:00:00 GMT">
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta http-equiv="Content-Script-Type" content="text/javascript">
<title>���j���[</title>

<script type="text/javascript">
//�p�X���[�h�L�������x�����b�Z�[�W
	var msg_pw_warning="<%= pw_warning_msg %>";
	window.onload = function setFormat(){
		//�p�X���[�h�x�����b�Z�[�W�\��
		if (msg_pw_warning != ""){
			alert(msg_pw_warning);
		}
	}
function submit_forward(action,nm,val){
	var frm = document.forms[0];
 	var query = document.createElement("input");
	query.type = "hidden";
	query.name = nm;
	query.value = val;
	frm.appendChild(query);
	frm.action = action;

	try {
		frm.submit();
	} catch(e) {
		if (e.number == -2147024891) {
			alert("�t�@�C���������m�F�������B");
		}
	}
}
</script>

<link rel="stylesheet" type="text/css" href="../css/common.css" >
<link rel="stylesheet" type="text/css" href="../frame/css/menu.css" />

<link rel="stylesheet" type="text/css" href="../themes/easyui/default/easyui.css">
<link rel="stylesheet" type="text/css" href="../themes/easyui/icon.css">
<link rel="stylesheet" type="text/css" href="../css/easyui_custom.css">
</head>

<body background="../frame/img/back.gif">
<!-- ***** container ***** -->
<div id="container">

<!-- ===  header === -->
<div id="header">
	<jsp:include page="header.jsp" flush="true" />
</div>
<!-- ===  /header=== -->

<!-- ===  content === -->
<div id="content">
<form id="frm_repout" name="frm_repout" method="post">
	<h2>���j���[</h2>

	<%--  �G���[���b�Z�[�W�̈� --%>
<%
if(request.getAttribute(Form.COMMON_MSG) != null){
%>
	<p class=<%= request.getAttribute(Form.COMMON_MSGTYPE).toString() %>>
		<%=request.getAttribute(Form.COMMON_MSG).toString()%>
	</p>
<%
}
%>
	<%-- /�G���[���b�Z�[�W�̈� --%>

	<div id="menu">

<%
boolean flgMenu = false;
boolean flgMst = false;
if(rowdisp != null && rowdisp.size() > 0){
	for(int i = 0; i < rowdisp.size(); i++){
		try{
			if(Integer.parseInt(rowdisp.get(i).toString()) > 100){
				// ��ԍ���100���傫���ꍇ�͊Ǘ��җp���j���[���ɕ\��
				flgMst = true;
			} else {
				// ��ԍ���100�ȉ��̏ꍇ�͊e�탌�|�[�g���ɕ\��
				flgMenu = true;
			}
		}catch(Exception ex){
		}
	}
}
if (flgMenu == false){
%>
	<div class="m_child">
		<h3>�{�V�X�e���𗘗p���邱�Ƃ͏o���܂���</h3>
		<p style="height:300px;">�e����߰Ă��{�����錠�����^�����Ă��܂���B</p>
	</div>
<%
} else {
%>
	<div id="menu_report" class="m_child_max">
		<h3>�e�탌�|�[�g</h3>
		<p>�{�����������|�[�g��I�����ĉ������B</p>
<%
String columnNo="";
String columnNoOld="";
for(int i = 0;i<rowdisp.size();i++){
	// ��ԍ��̎擾
	columnNo=rowdisp.get(i).toString();
	// ��ԍ���100���傫���ꍇ�͏������܂Ȃ�
	try{
		if(Integer.parseInt(columnNo) > 100){
			continue;
		}
	}catch(Exception ex){
		continue;
	}
	// ���s�̗�ԍ��ƑO��̗�ԍ����r���ĈقȂ�ꍇ�A��p�̃��j���[����������
	if(!columnNoOld.equals(columnNo)){
%>
<%-- �啪�ރ��|�[�g�̐ݒ�ƍs�؂�ւ�--%>
	</div>
	<div id="menu_report" class="m_child">
	<%--// �啪�ރ��|�[�g�̐ݒ�ƍs�؂�ւ�--%>
<%
	}
	int no_len = 0;
	ArrayList report = null;
	no_len = i;
	Side m = (Side)menu.get(i);
	report = m.getReport();

	for(int k = 0; k< report.size() ;k++){
		Report rep = (Report)report.get(k);
		if(rep.getReport_boolean()){
			//reportside���������̂��o��
			if(i == no_len ){

%>
<ul id="menu_report" class="easyui-tree" style="width:100%;float:left;">
	<li>
	<span>
		<a class="enableMenu" href="../Servlet/Report.do?<%= Form.REPORT_SIDE %>=<%= m.getSide() %>
			&<%= Form.REPORT_SIDE_ARRAY %>=<%= i %> "><%= m.getSidename() %><!-- ��ԍ��F<%=rowdisp.get(i)%>  -->
		</a>
<!--
		<a href="#" class="enableMenu"> <%= m.getSidename() %> </a>
-->
	</span>
	<ul>
<%
				no_len = no_len +1;
			}
		}//���|�[�g�ɒl�����邩���肵�Ă���if��
	}//report.size��for��
%>
		<jsp:include page="report_list.jsp">
			<jsp:param value="<%= Integer.toString(m.getSide()) %>" name="ReportSide" />
			<jsp:param value="FullName" name="ReportNameType" />
			<jsp:param value="<%=i %>" name="ReportSide_array" />
			<jsp:param value="1" name="HiddenReport" />
		</jsp:include>
	</ul>
	</li>
</ul>
<%
	// ���s�̗�ԍ���ێ�����
	columnNoOld = columnNo;
} // for ��O
%>
</div>
<%
}
if ((mstr != null && mstr.size() > 0) || flgMst == true){
%>
	<div id="menu_system" class="m_child_max">
		<h3>�Ǘ��җp���j���[</h3>
		<p>���V�X�e���̏����{�����鎖���ł��܂��B</p>
<%
if (mstr != null && mstr.size() > 0){
%>
	</div>
	<div id="menu_report" class="m_child">
	<ul id="menu_system" class="easyui-tree" style="width:100%;float:left;">
		<li>
<%
	int no_len = 0;
	ArrayList report = null;
	for(int i=0; i < mstr.size(); i++){
		no_len = i;
		Side m = (Side)mstr.get(i);
		report = m.getReportMst();
		for(int k = 0; k< report.size() ;k++){
			//reportside���������̂��o��
			if(i == no_len){
%>
		<span>
			<!-- �O���[�v�}�X�^�Œ� -->
			<a href="javascript:submit_forward('../Servlet/MaintenanceGroupsMst.do?Report=-5','MaintenanceSide','-1');"><%= m.getSidename() %></a>
		</span>
		<ul>
<%
				no_len = no_len +1;
			}
		}	//report.size��for��
%>
		<jsp:include page="meintenance_report_list.jsp">
			<jsp:param value="<%= Integer.toString(m.getSide()) %>" name="ReportSide" />
			<jsp:param value="FullName" name="ReportNameType" />
		</jsp:include>
<%
	}
%>
		</ul>
		</li>
	</ul>
<%
}
if (flgMst == true){
String columnNo="";
String columnNoOld="";
for(int i = 0;i<rowdisp.size();i++){
	// ��ԍ��̎擾
	columnNo=rowdisp.get(i).toString();
	// ��ԍ���100�ȉ��̏ꍇ�͏������܂Ȃ�
	try{
		if(Integer.parseInt(columnNo) <= 100){
			continue;
		}
	}catch(Exception ex){
		continue;
	}
	// ���s�̗�ԍ��ƑO��̗�ԍ����r���ĈقȂ�ꍇ�A��p�̃��j���[����������
	if(!columnNoOld.equals(columnNo)){
%>
<%-- �啪�ރ��|�[�g�̐ݒ�ƍs�؂�ւ�--%>
	</div>
	<div id="menu_report" class="m_child">
	<%--// �啪�ރ��|�[�g�̐ݒ�ƍs�؂�ւ�--%>
<%
	}
	int no_len = 0;
	ArrayList report = null;
	no_len = i;
	Side m = (Side)menu.get(i);
	report = m.getReport();

	for(int k = 0; k< report.size() ;k++){
		Report rep = (Report)report.get(k);
		if(rep.getReport_boolean()){
			//reportside���������̂��o��
			if(i == no_len){
%>
<ul id="menu_report" class="easyui-tree" style="width:100%;float:left;">
	<li>
	<span>
		<a class="enableMenu" href="../Servlet/Report.do?<%= Form.REPORT_SIDE %>=<%= m.getSide() %>
			&<%= Form.REPORT_SIDE_ARRAY %>=<%= i %> "><%= m.getSidename() %><!-- ��ԍ��F<%=rowdisp.get(i)%>  -->
		</a>
	</span>
	<ul>
<%
				no_len = no_len +1;
			}
		}//���|�[�g�ɒl�����邩���肵�Ă���if��
	}//report.size��for��
%>
		<jsp:include page="report_list.jsp">
			<jsp:param value="<%= Integer.toString(m.getSide()) %>" name="ReportSide" />
			<jsp:param value="FullName" name="ReportNameType" />
			<jsp:param value="<%=i %>" name="ReportSide_array" />
			<jsp:param value="1" name="HiddenReport" />
		</jsp:include>
	</ul>
	</li>
</ul>
<%
	// ���s�̗�ԍ���ێ�����
	columnNoOld = columnNo;
} // for ��O
}
%>
</div>
<%
}
%>
</div>
</form>
</div>
<!-- ===  /content === -->

<!-- ===  footer === -->
<div id="footer">
</div>
<!-- === /footer === -->

</div>
<!-- ***** /container ***** -->
</body>
</html>
<script type="text/javascript" src="../js/jquery.min.js"></script>
<script type="text/javascript" src="../js/jquery.easyui.min.js"></script>
<script type="text/javascript" src="../js/jquery.info.control.js?v=<%=prm %>"></script>