<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="MS932"%>
<%@ page import="java.util.*" %>
<%@ page import="authentication.defines.*" %>
<%@ page import="authentication.bean.*" %>

<%
MaintenanceUserReport user = (MaintenanceUserReport)request.getAttribute(Consts.STR_REQ_REC);
ArrayList List = (ArrayList)session.getAttribute(Consts.STR_SES_GRD);
ArrayList posList = (ArrayList)session.getAttribute(Consts.STR_SES_GRD2);
ArrayList groupList = (ArrayList)session.getAttribute(Consts.STR_SES_GRD3);
ArrayList reportList = (ArrayList)session.getAttribute(Consts.STR_SES_GRD4);
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
<LINK HREF="../frame/css/m_userreport.css" REL="stylesheet" TYPE="text/css">
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
		<form id="m_userreport" method="post" action="../Servlet/MaintenanceUserReport.do">
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

					<div class="inpsection">
						<label for="<%=Form.MTN_USR_ETY_ATH %>" class="required">�O���[�v��</label>
						<select id="<%=Form.MTN_USR_ETY_ATH %>" name="<%=Form.MTN_USR_ETY_ATH %>" size="1">
							<option value="">&nbsp;</option>
<%
						for(int i = 0; i < groupList.size() ; i++){
							Auth group = (Auth)groupList.get(i);
%>
						 	<option value="<%= group.getGroup() %>" <%if(user.getGroup() != 0 && group.getGroup()==user.getGroup()){%>selected<%}%>><%= group.getGroupName() %></option>
<%
						}
%>
						</select>
					</div>

					<div class="inpsection">
						<label for="<%=Form.MTN_USR_ETY_POS %>" class="required">���[����</label>
						<select id="<%=Form.MTN_USR_ETY_POS %>" name="<%=Form.MTN_USR_ETY_POS %>" size="1">
							<option value="">&nbsp;</option>
<%
						for(int i = 0; i < posList.size() ; i++){
							Position pos = (Position)posList.get(i);
%>
						 	<option value="<%= pos.getPosition() %>" <%if(user.getPos() != 0 && pos.getPosition()==user.getPos()){%>selected<%}%>><%= pos.getPositionName() %></option>
<%
						}
%>
						</select>
					</div>

					<div class="inpsection">
						<label for="<%=Form.MTN_REPORT_CODE %>" class="required">���|�[�g��</label>
						<select id="<%=Form.MTN_REPORT_CODE %>" name="<%=Form.MTN_REPORT_CODE %>" size="1">
							<option value="">&nbsp;</option>
<%
						for(int i = 0; i < reportList.size() ; i++){
							Report report = (Report)reportList.get(i);
%>
						 	<option value="<%= report.getReport_no() %>" <%if(user.getReport_no() != 0 &&  report.getReport_no()==user.getReport_no()){%>selected<%}%>><%= report.getReport_name() %></option>
<%
						}
%>
						</select>
					</div>

					<div class="inpsection">
						<label for="<%=Form.MTN_ENABLE_MENU %>" class="required">�L���^����</label>
						<select id="<%=Form.MTN_ENABLE_MENU %>" name="<%=Form.MTN_ENABLE_MENU %>" size="1">
							<option value="1">�L��</option>
							<option value="0" <% if("0".equals(user.getEnableMenu())){ %> selected <% } %> >����</option>
						</select>
						<br class="clear">
					</div>

				</fieldset>

				<fieldset class="commands">
					<legend>���͂���User����o�^�E�폜�E���̓��Z�b�g���鎖���ł��܂��B</legend>
					<input type="hidden" name="<%= Form.MTN_USR_ETY_IDX %>" value="<%= user.getIndex() %>">
					<input type="button" name="<%= Form.MTN_ENTBTN %>" value="�o�^" onclick="submit_forward('../Servlet/MaintenanceUserReport.do','<%= Form.MTN_INPAREA %>','<%= Form.MTN_ENTBTN %>')">
					<input type="button" name="<%= Form.MTN_DELBTN %>" value="�폜" onclick="if(confirm('�폜���Ă�낵���ł����H')){submit_forward('../Servlet/MaintenanceUserReport.do','<%= Form.MTN_INPAREA %>','<%= Form.MTN_DELBTN %>')}">
					<input type="button" name="<%= Form.MTN_RSTBTN %>" value="���Z�b�g" onclick="if(confirm('���Z�b�g���Ă�낵���ł����H')){submit_forward('../Servlet/MaintenanceUserReport.do','<%= Form.MTN_INPAREA %>','<%= Form.MTN_RSTBTN %>')}">
				</fieldset>
				<br class="clear">
			</div>

			<div id="view">
			<h3>���[�U�[�ꗗ</h3>

				<div class="tableContainer">
					<table style="word-break:break-all; word-wrap: break-word;" border="0" cellpadding="0" cellspacing="0" class="scrollTable" summary="���[�U�[�ꗗ�\ ���[�U�[�̏ڍ׏����ꗗ�ŕ\�����Ă��܂��B">
						<thead>
							<tr>
								<th class="col_select">&nbsp;</th>
								<th class="col_no"    >No</th>
								<th class="col_group"   >�O���[�v��</th>
								<th class="col_pos"   >���[����</th>
								<th class="col_reponame" >���|�[�g��</th>
								<th class="col_enable" >�L��</th>
							</tr>
						</thead>
						<tbody>
<%
		String rowclass = null;
		for(int i = 0; i < List.size() ; i++){
			MaintenanceUserReport repolist = (MaintenanceUserReport)List.get(i);
			if (repolist.getState() != MaintenanceUser.STATE_DEL){
				switch(repolist.getState()){
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
				if(user.getIndex() == i && user.getGroup() != 0){
					rowclass += " selectedRow";
				}
%>
							<tr class="<%= rowclass %>">
								<td class="col_select">
									<input type="button" name="<%= Form.MTN_SELBTN %><%= i %>" value="�I��" onclick="submit_forward('../Servlet/MaintenanceUserReport.do','<%= Form.MTN_SELAREA %>','<%= i %>')">
								</td>
								<td class="col_no">
									<%= i+1 %>
								</td>
								<td class="col_group">
<%
				for(int j = 0;j < groupList.size() ; j++){
					Auth group = (Auth)groupList.get(j);
					if(repolist.getGroup() != 0 && group.getGroup()==repolist.getGroup()){
%>
								<%= group.getGroupName() %>
<%
					}
				}
%>
								</td>
								<td class="col_pos">
<%
				for(int j = 0; j < posList.size() ; j++){
					Position pos = (Position)posList.get(j);
				 	if(repolist.getPos() != 0 && pos.getPosition()==repolist.getPos()){
%>
							 	<%= pos.getPositionName() %>
<%
					}
				}
%>
								 </td>
								<td class="col_reponame">
<%
				for(int j = 0; j < reportList.size() ; j++){
					Report report = (Report)reportList.get(j);
					if(repolist.getReport_no() != 0 && report.getReport_no()==repolist.getReport_no()){
%>
								<%=report.getReport_name() %>
<%
					}
				}
%>
									&nbsp;
								</td>
								<td class="col_enable">
<%
				String enableMnu = "";
				if ("1".equals(repolist.getEnableMenu())){
					enableMnu = "�L��";
				} else {
					enableMnu = "����";
				}
%>
								<%=enableMnu %>

<%
%>
									&nbsp;
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

<!-- ===  /content === -->

	<!-- ===  footer === -->
	<div id="footer">
	</div>
	<!-- === /footer === -->

</div>
<!-- ***** /container ***** -->
</body>
</html>