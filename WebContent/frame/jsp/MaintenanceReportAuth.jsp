<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="MS932"%>
<%@ page import="java.util.*" %>
<%@ page import="authentication.defines.*" %>
<%@ page import="authentication.bean.*" %>

<%
MaintenanceReportAuth user = (MaintenanceReportAuth)request.getAttribute(Consts.STR_REQ_REC);
ArrayList List = (ArrayList)session.getAttribute(Consts.STR_SES_GRD);
ArrayList sidelist = (ArrayList)session.getAttribute(Consts.STR_SES_GRD2);
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
<LINK HREF="../frame/css/m_reportauth.css" REL="stylesheet" TYPE="text/css">
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
		<form id="m_reportauth" method="post" action="../Servlet/MaintenanceUserReport.do">
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

				<div class="inpsection">
					<label for="<%=Form.REPORT_SIDE %>" class="required">分類名称</label>
					<select id="<%=Form.REPORT_SIDE %>" name="<%=Form.REPORT_SIDE %>" size="1">
						<option value="">&nbsp;</option>
<%
					for(int i = 0; i < sidelist.size() ; i++){
						Side reportside = (Side)sidelist.get(i);
%>
					 	<option value="<%= reportside.getSide() %>" <%if(user.getReport_side() != 0 &&  reportside.getSide() == user.getReport_side()){%>selected<%}%>><%=reportside.getSidename()  %></option>
<%
					}
%>
					</select>
				</div>

				<div class="inpsection">
					<label for="<%=Form.MTN_REPORT_CODE %>" class="required">レポート名称</label>
					<select id="<%=Form.MTN_REPORT_CODE %>" name="<%=Form.MTN_REPORT_CODE %>" size="1">
						<option value="">&nbsp;</option>
<%
					for(int i = 0; i < reportList.size() ; i++){
						Report report = (Report)reportList.get(i);
%>
					 	<option value="<%= report.getReport_no() %>" <%if(user.getReport_no() != 0 &&  report.getReport_no()== user.getReport_no()){%>selected<%}%>><%= report.getReport_name() %></option>
<%
					}
%>
					</select>
				</div>

				<div class="inpsection">
					<label for="<%=Form.REPORT_DISP_SIDE %>" class="required">表示順</label>
					<!--  <span class="hidden"><% if(Integer.toString(user.getReport_no()) != null){%><%=user.getDispNumber()  %><%}%>&nbsp;</span> -->
					<input class="text1" type="text" id="<%=Form.REPORT_DISP_SIDE %>" name="<%=Form.REPORT_DISP_SIDE %>" value="<% if (user.getReport_no() != 0){%><%= user.getDispNumber() %><%}%>" >
					<br class="clear">
				</div>
			</fieldset>

			<fieldset class="commands">
				<legend>入力したUser情報を登録・削除・又はリセットする事ができます。</legend>
				<input type="hidden" name="<%= Form.MTN_USR_ETY_IDX %>" value="<%= user.getIndex() %>">
				<input type="button" name="<%= Form.MTN_ENTBTN %>" value="登録" onclick="submit_forward('../Servlet/MaintenanceReportAuth.do','<%= Form.MTN_INPAREA %>','<%= Form.MTN_ENTBTN %>')">
				<input type="button" name="<%= Form.MTN_DELBTN %>" value="削除" onclick="if(confirm('削除してよろしいですか？')){submit_forward('../Servlet/MaintenanceReportAuth.do','<%= Form.MTN_INPAREA %>','<%= Form.MTN_DELBTN %>')}">
				<input type="button" name="<%= Form.MTN_RSTBTN %>" value="リセット" onclick="if(confirm('リセットしてよろしいですか？')){submit_forward('../Servlet/MaintenanceReportAuth.do','<%= Form.MTN_INPAREA %>','<%= Form.MTN_RSTBTN %>')}">
			</fieldset>
			<br class="clear">
		</div>

		<div id="view">
			<h3>ユーザー一覧</h3>

			<div class="tableContainer">
				<table style="word-break:break-all; word-wrap: break-word;"  border="0" cellpadding="0" cellspacing="0" class="scrollTable" summary="ユーザー一覧表 ユーザーの詳細情報を一覧で表示しています。">
					<thead>
						<tr>
							<th class="col_select">&nbsp;</th>
							<th class="col_no"    >No</th>
							<th class="col_sidename"   >分類名称</th>
							<th class="col_reportname"   >レポート名称</th>
							<th class="col_dispno" >表示順</th>
							<!--  <td class="col_space" >&nbsp;</td>-->
						</tr>
					</thead>
					<tbody>
<%
		String rowclass = null;
		for(int i = 0; i < List.size() ; i++){
			MaintenanceReportAuth report_auth = (MaintenanceReportAuth)List.get(i);
			if (report_auth.getState() != MaintenanceUser.STATE_DEL){
				switch(report_auth.getState()){
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
				if(user.getIndex() == i && Integer.toString(user.getReport_no()) != null){
					rowclass += " selectedRow";
				}
%>
						<tr class="<%= rowclass %>">
							<td class="col_select">
								<input type="button" name="<%= Form.MTN_SELBTN %><%= i %>" value="選択" onclick="submit_forward('../Servlet/MaintenanceReportAuth.do','<%= Form.MTN_SELAREA %>','<%= i %>')">
							</td>
							<td class="col_no">
								<%= i+1 %>
							</td>
							<td class="col_sidename">
<%
				for(int j = 0; j < sidelist.size() ; j++){
					Side reportside = (Side)sidelist.get(j);
				 	if(Integer.toString(report_auth.getReport_side()) != null && reportside.getSide()==report_auth.getReport_side()){
%>
							 	<%= reportside.getSidename() %>
<%
					}
				}
%>
							 </td>
							<td class="col_reportname"><%
				for(int j = 0; j < reportList.size() ; j++){
					Report report = (Report)reportList.get(j);;
					if(Integer.toString(report_auth.getReport_no()) != null && report.getReport_no()==report_auth.getReport_no()){
%>
								<%=report.getReport_name() %>
<%
					}
				}
%>
							</td>
							<td class="col_dispno">
								<%=report_auth.getDispNumber() %>&nbsp;
							</td>
						</tr>
<%
			}
		}
%>
					</tbody>
				</table>
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