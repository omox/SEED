<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="MS932"%>
<%@ page import="java.util.*" %>
<%@ page import="authentication.defines.*" %>
<%@ page import="authentication.bean.*" %>

<%
MaintenanceReportName user = (MaintenanceReportName)request.getAttribute(Consts.STR_REQ_REC);
ArrayList List = (ArrayList)session.getAttribute(Consts.STR_SES_GRD);
ArrayList jspList = (ArrayList)session.getAttribute(Consts.STR_SES_GRD2);
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
<LINK HREF="../frame/css/m_reportname.css" REL="stylesheet" TYPE="text/css">
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
		<form id="m_reportname" method="post" action="../Servlet/MaintenanceReportName.do">
			<h2><%=title %></h2>
				<input type="hidden" name="<%= Form.MTN_SIDE %>" value="<%=Consts.SYSTEM_MENTENANCE %>">
				<%--  エラーメッセージ領域 --%>
<%
				if(request.getAttribute(Form.COMMON_MSG) != null){
%>
					<p class=<%= request.getAttribute(Form.COMMON_MSGTYPE).toString() %>><%=request.getAttribute(Form.COMMON_MSG).toString()%></p>
<%
				}
%>

				<%-- /エラーメッセージ領域 --%>

				<div id="entry">
					<h3>情報入力エリア</h3>
					<p class="memo">新規・あるいは登録されている情報を変更・更新する事ができます。</p>

					<fieldset class="tbl">
						<legend>User情報を入力して下さい。</legend>

						<div class="inpsection_hidden">
							<label for="<%=Form.MTN_REPORT_CODE %>" class="required">レポートコード</label>
							<!--  <span class="hidden"><% if(user.getReport_no() != 0){%><%=user.getReport_no()  %><%}%>&nbsp;</span> -->
							<input class="text1" type="text" id="<%=Form.MTN_REPORT_CODE %>" name="<%=Form.MTN_REPORT_CODE %>" value="<% if (user.getReport_no() != 0){%><%=user.getReport_no() %><%}%>" >
							<br class="clear">
						</div>

						<div class="inpsection">
							<label for="<%=Form.MTN_NM_REPORT %>" class="required">レポート名称</label>
							<!--  <span class="hidden"><% if(user.getReport_no() != 0){%><%=user.getReport_no()  %><%}%>&nbsp;</span> -->
							<input class="text1" type="text" id="<%=Form.MTN_NM_REPORT %>" name="<%=Form.MTN_NM_REPORT %>" value="<% if (user.getReport_name() != null){%><%=user.getReport_name() %><%}%>" >
							<br class="clear">
						</div>

						<div class="inpsection">
							<label for="<%=Form.MTN_REPORT_NM_SHORT %>" class="required">レポート略称</label>
							<!--  <span class="hidden"><% if(user.getReport_no() != 0){%><%=user.getReport_no()  %><%}%>&nbsp;</span> -->
							<input class="text1" type="text" id="<%=Form.MTN_REPORT_NM_SHORT %>" name="<%=Form.MTN_REPORT_NM_SHORT %>" value="<% if (user.getReport_shortname() != null){%><%=user.getReport_shortname() %><%}%>" >
							<br class="clear">
						</div>

						<div class="inpsection">
							<label for="<%=Form.MTN_REPORT_JSP %>" class="required">レポートJSP</label>
							<select id="<%=Form.MTN_REPORT_JSP %>" name="<%=Form.MTN_REPORT_JSP %>" >
								<option value="">&nbsp;</option>
<%
						for(int i = 0; i < jspList.size() ; i++){
							String jspfaile = jspList.get(i).toString().trim();
%>
							 	<option value="<%= jspfaile %>" <%if(user.getReport_jsp() != null && user.getReport_jsp().equals(jspfaile)){%>selected<%}%>><%= jspfaile %></option>
<%
						}
%>
							</select>
						 	<br class="clear">
						</div>

						<div class="inpsection">
							<label for="<%=Form.MTN_CUSTOM_VALUE %>" class="required">プロパティ</label>
							<!--  <span class="hidden"><% if(user.getReport_no() != 0){%><%=user.getReport_no()  %><%}%>&nbsp;</span> -->
							<input class="text1" type="text" id="<%=Form.MTN_CUSTOM_VALUE %>" name="<%=Form.MTN_CUSTOM_VALUE %>" value="<% if (user.getReport_custom() != null){%><%=user.getReport_custom() %><%}%>" >
							<br class="clear">
						</div>
					</fieldset>

					<fieldset class="commands">
						<legend>入力したUser情報を登録・削除・又はリセットする事ができます。</legend>
						<input type="hidden" name="<%= Form.MTN_USR_ETY_IDX %>" value="<%= user.getIndex() %>">
						<input type="button" name="<%= Form.MTN_ENTBTN %>" value="登録" onclick="submit_forward('../Servlet/MaintenanceReportName.do','<%= Form.MTN_INPAREA %>','<%= Form.MTN_ENTBTN %>')">
						<input type="button" name="<%= Form.MTN_DELBTN %>" value="削除" onclick="if(confirm('削除してよろしいですか？')){submit_forward('../Servlet/MaintenanceReportName.do','<%= Form.MTN_INPAREA %>','<%= Form.MTN_DELBTN %>')}">
						<input type="button" name="<%= Form.MTN_RSTBTN %>" value="リセット" onclick="if(confirm('リセットしてよろしいですか？')){submit_forward('../Servlet/MaintenanceReportName.do','<%= Form.MTN_INPAREA %>','<%= Form.MTN_RSTBTN %>')}">
					</fieldset>
					<br class="clear">
				</div>


				<div id="view">
				<h3>ユーザー一覧</h3>

					<div class="tableContainer">
						<table style="word-break:break-all; word-wrap: break-word;" border="0" cellpadding="0" cellspacing="0" class="scrollTable" summary="ユーザー一覧表 ユーザーの詳細情報を一覧で表示しています。">
							<thead>
								<tr>
									<th class="col_select">&nbsp;</th>
									<th class="col_no"    >No</th>
									<th class="col_reportname"   >レポート名称</th>
									<th class="col_reportname_2"   >レポート略称</th>
									<th class="col_jsp"   >レポートJSP</th>
									<th class="col_property"   >プロパティ</th>
								</tr>
							</thead>
							<tbody>
<%
					String rowclass = null;
					for(int i = 0; i < List.size() ; i++){
						MaintenanceReportName report_Name = (MaintenanceReportName)List.get(i);
						if (report_Name.getState() != MaintenanceReportName.STATE_DEL){
							switch(report_Name.getState()){
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
							if(user.getIndex() == i && report_Name.getReport_no() != 0){
								rowclass += " selectedRow";
							}
%>
								<tr class="<%= rowclass %>">
									<td class="col_select">
										<input type="button" name="<%= Form.MTN_SELBTN %><%= i %>" value="選択" onclick="submit_forward('../Servlet/MaintenanceReportName.do','<%= Form.MTN_SELAREA %>','<%= i %>')">
									</td>
									<td class="col_no">
										<%= i+1 %>
									</td>
									<td class="col_reportname">
										<%=report_Name.getReport_name() %>
									</td>
									<td class="col_reportname_2">
										<%=report_Name.getReport_shortname() %>
									</td>
									<td class="col_jsp">
										<%=report_Name.getReport_jsp() %>&nbsp;
									</td>
									<td class="col_property">
										<%if(report_Name.getReport_custom() != null){ %><%=report_Name.getReport_custom() %>
<%
										}
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
</div>
</body>
</html>