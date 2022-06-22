<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="MS932"%>
<%@ page import="java.util.*" %>
<%@ page import="authentication.defines.*" %>
<%@ page import="authentication.bean.*" %>

<%
	MaintenanceGroupsMst user = (MaintenanceGroupsMst)request.getAttribute(Consts.STR_REQ_REC);
	ArrayList List = (ArrayList)session.getAttribute(Consts.STR_SES_GRD);
	String title = (String)session.getAttribute(Consts.STR_SES_TITLE);
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html lang="ja">
<head>
<meta http-equiv="X-UA-Compatible" content="IE=7" />
<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=UTF-8">
<META http-equiv="Content-Script-Type" content="text/javascript">
<META http-equiv="Content-Style-Type" content="text/css">

<title><%=title %></title>
<jsp:include page="common_head.jsp" flush="true" />

<LINK HREF="../frame/css/m_groupmst.css" REL="stylesheet" TYPE="text/css">
<link rel="stylesheet" type="text/css" href="../themes/easyui/default/easyui.css">
<link rel="stylesheet" type="text/css" href="../themes/easyui/icon.css">
<script type="text/javascript" src="../js/jquery.min.js"></script>
<script type="text/javascript" src="../js/jquery.easyui.min.js"></script>
</head>

<body>
<!-- ***** container ***** -->
<div id="container">

<!-- ===  header === -->
<div id="header">
	<jsp:include page="header.jsp" flush="true" />
</div>
<!-- ===  /header=== -->

<!-- ===  content === -->
<div id="content">

	<form id="m_user" method="post" action="../Servlet/MaintenanceGroupsMst.do">

		<h2><%=title %></h2>
		<input type="hidden" name="<%= Form.MTN_SIDE %>" value="<%=Consts.SYSTEM_MENTENANCE %>">

		<%--  �G���[���b�Z�[�W�̈� --%>
<%
		if(request.getAttribute(Form.COMMON_MSG) != null){
%>
			<p class=<%= request.getAttribute(Form.COMMON_MSGTYPE).toString() %>><%=request.getAttribute(Form.COMMON_MSG).toString()%></p>
<%
		}
%>
		<%-- /�G���[���b�Z�[�W�̈� --%>

		<div id="entry">

			<h3>�����̓G���A</h3>
			<p class="memo">�V�K�E���邢�͓o�^����Ă������ύX�E�X�V���鎖���ł��܂��B</p>

			<fieldset class="tbl">
				<legend>User������͂��ĉ������B</legend>

				<div class="inpsection_hidden">
					<label for="<%=Form.MTN_USR_ETY_ATH %>" class="required">�O���[�v�R�[�h</label>
					<!--  <span class="hidden"><% if(Integer.toString(user.getGroup()) != null){%><%=user.getGroup()  %><%}%>&nbsp;</span> -->
					<input class="text1" type="text" id="<%=Form.MTN_USR_ETY_ATH %>" name="<%=Form.MTN_USR_ETY_ATH %>" value="<% if (Integer.toString(user.getGroup())!= null){%> <%= user.getGroup() %><%}%>" >
					<br class="clear">
				</div>

				<div class="inpsection">
					<label for="<%=Form.MTN_USR_ETY_ATH_NAME %>" class="required">�O���[�v��</label>
					<!--  <span class="hidden"><% if(Long.toString(user.getGroup()) != null){%><%=user.getGroup()  %><%}%>&nbsp;</span> -->
					<input class="text1" type="text" id="<%=Form.MTN_USR_ETY_ATH_NAME %>" name="<%=Form.MTN_USR_ETY_ATH_NAME %>" value="<% if (user.getGroupName() != null){%><%= user.getGroupName() %><%}%>" >
					<br class="clear">
				</div>

				<div class="inpsection">
					<label for="<%=Form.MTN_CUSTOM_VALUE %>" class="required">�v���p�e�B</label>
					<!--  <span class="hidden"><% if(Long.toString(user.getGroup()) != null){%><%=user.getGroup()  %><%}%>&nbsp;</span> -->
					<input class="text1" type="text" id="<%=Form.MTN_CUSTOM_VALUE %>" name="<%=Form.MTN_CUSTOM_VALUE %>" value="<% if (user.getCustom_value() != null){%><%= user.getCustom_value() %><%}%>" >
					<br class="clear">
				</div>
			</fieldset>

			<fieldset class="commands">
				<legend>���͂���User����o�^�E�폜�E���̓��Z�b�g���鎖���ł��܂��B</legend>
				<input type="hidden" name="<%= Form.MTN_USR_ETY_IDX %>" value="<%= user.getIndex() %>">
				<input type="button" name="<%= Form.MTN_ENTBTN %>" value="�o�^" onclick="submit_forward('../Servlet/MaintenanceGroupsMst.do','<%= Form.MTN_INPAREA %>','<%= Form.MTN_ENTBTN %>')">
				<input type="button" name="<%= Form.MTN_DELBTN %>" value="�폜" onclick="if(confirm('�폜���Ă�낵���ł����H')){submit_forward('../Servlet/MaintenanceGroupsMst.do','<%= Form.MTN_INPAREA %>','<%= Form.MTN_DELBTN %>')}">
				<input type="button" name="<%= Form.MTN_RSTBTN %>" value="���Z�b�g" onclick="if(confirm('���Z�b�g���Ă�낵���ł����H')){submit_forward('../Servlet/MaintenanceGroupsMst.do','<%= Form.MTN_INPAREA %>','<%= Form.MTN_RSTBTN %>')}">
			</fieldset>
			<br class="clear">
		</div>



		<div id="view">
			<h3>���[�U�[�ꗗ</h3>
			<div class="tableContainer">
				<table style="word-break:break-all; word-wrap: break-word;"  border="0" cellpadding="0" cellspacing="0" class="scrollTable" summary="���[�U�[�ꗗ�\ ���[�U�[�̏ڍ׏����ꗗ�ŕ\�����Ă��܂��B">
					<thead>
						<tr>
							<th class="col_select">&nbsp;</th>
							<th class="col_no"    >No</th>
							<th class="col_group"   >�O���[�v��</th>
							<th class="col_property"   >�v���p�e�B</th>
							<!--  <td class="col_space" >&nbsp;</td>-->
						</tr>
					</thead>
					<tbody>
<%
		String rowclass = null;
		for(int i = 0; i < List.size() ; i++){
			MaintenanceGroupsMst group_get = (MaintenanceGroupsMst)List.get(i);
			if (group_get.getState() != MaintenanceUser.STATE_DEL){
				switch(group_get.getState()){
				case MaintenanceUser.STATE_UPD:
					rowclass = "updatedRow"; break;
				default:
					switch(i%2){
					case 0:
						rowclass="alternateRow"; break;
					default:
						rowclass="normalRow"; break;
					}
				}
				if(user.getIndex() == i && Integer.toString(user.getGroup()) != null){
					rowclass += " selectedRow";
				}
%>
						<tr class="<%= rowclass %>">
							<td class="col_select">
								<input type="button" name="<%= Form.MTN_SELBTN %><%= i %>" value="�I��" onclick="submit_forward('../Servlet/MaintenanceGroupsMst.do','<%= Form.MTN_SELAREA %>','<%= i %>')">
							</td>
							<td class="col_no">
								<%= i+1 %>
							</td>
							<td class="col_group">
								<%=group_get.getGroupName() %>
							</td>
							<td class="col_property">
								<%if(group_get.getCustom_value()!= null){ %><%=group_get.getCustom_value() %><%}%>&nbsp;
							</td>
						</tr>
<%
			}
		}
%>
					</tbody>
				</table>
			</div>
		</div>
	</form>
</div>
</div>
</body>
</html>