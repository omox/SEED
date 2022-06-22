<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="MS932"%>
<%@ page import="java.util.*" %>
<%@ page import="authentication.defines.*" %>
<%@ page import="authentication.bean.*" %>

<%
MaintenanceInfoAuth infoAuth = (MaintenanceInfoAuth)request.getAttribute(Consts.STR_REQ_REC);
ArrayList List = (ArrayList)session.getAttribute(Consts.STR_SES_GRD);
ArrayList posList = (ArrayList)session.getAttribute(Consts.STR_SES_GRD2);
ArrayList groupList = (ArrayList)session.getAttribute(Consts.STR_SES_GRD3);
ArrayList infoList = (ArrayList)session.getAttribute(Consts.STR_SES_GRD4);
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
<LINK HREF="../frame/css/m_infoauth.css" REL="stylesheet" TYPE="text/css">
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
		<form id="m_infoauth" method="post" action="../Servlet/MaintenanceInfoAuth.do">
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
						<label for="<%=Form.MTN_USR_ETY_ATH %>" class="required">グループ名</label>
						<select id="<%=Form.MTN_USR_ETY_ATH %>" name="<%=Form.MTN_USR_ETY_ATH %>" size="1">
							<option value="">&nbsp;</option>
<%
						for(int i = 0; i < groupList.size() ; i++){
							Auth group = (Auth)groupList.get(i);
%>
						 	<option value="<%= group.getGroup() %>" <%if(infoAuth.getCd_group() != 0 && group.getGroup()==infoAuth.getCd_group()){%>selected<%}%>><%= group.getGroupName() %></option>
<%
						}
%>
						</select>
					</div>

					<div class="inpsection">
						<label for="<%=Form.MTN_USR_ETY_POS %>" class="required">ロール名</label>
						<select id="<%=Form.MTN_USR_ETY_POS %>" name="<%=Form.MTN_USR_ETY_POS %>" size="1">
							<option value="">&nbsp;</option>
<%
						for(int i = 0; i < posList.size() ; i++){
							Position pos = (Position)posList.get(i);
%>
						 	<option value="<%= pos.getPosition() %>" <%if(infoAuth.getCd_pos() != 0 && pos.getPosition()==infoAuth.getCd_pos()){%>selected<%}%>><%= pos.getPositionName() %></option>
<%
						}
%>
						</select>
					</div>

					<div class="inpsection">
						<label for="<%=Form.MTN_INFO_CODE %>" class="required">お知らせ</label>
						<select id="<%=Form.MTN_INFO_CODE %>" name="<%=Form.MTN_INFO_CODE %>" size="1">
							<option value="">&nbsp;</option>
<%
						for(int i = 0; i < infoList.size() ; i++){
							Info info = (Info)infoList.get(i);
%>
						 	<option value="<%= info.getCd_info() %>" <%if(infoAuth.getCd_info() != 0 &&  info.getCd_info()==infoAuth.getCd_info()){%>selected<%}%>><%= info.getTitle() %></option>
<%
						}
%>
						</select>
					</div>
				</fieldset>

				<fieldset class="commands">
					<legend>入力したUser情報を登録・削除・又はリセットする事ができます。</legend>
					<input type="hidden" name="<%= Form.MTN_USR_ETY_IDX %>" value="<%= infoAuth.getIndex() %>">
					<input type="button" name="<%= Form.MTN_ENTBTN %>" value="登録" onclick="submit_forward('../Servlet/MaintenanceInfoAuth.do','<%= Form.MTN_INPAREA %>','<%= Form.MTN_ENTBTN %>')">
					<input type="button" name="<%= Form.MTN_DELBTN %>" value="削除" onclick="if(confirm('削除してよろしいですか？')){submit_forward('../Servlet/MaintenanceInfoAuth.do','<%= Form.MTN_INPAREA %>','<%= Form.MTN_DELBTN %>')}">
					<input type="button" name="<%= Form.MTN_RSTBTN %>" value="リセット" onclick="if(confirm('リセットしてよろしいですか？')){submit_forward('../Servlet/MaintenanceInfoAuth.do','<%= Form.MTN_INPAREA %>','<%= Form.MTN_RSTBTN %>')}">
				</fieldset>
				<br class="clear">
			</div>

			<div id="view">
			<h3>お知らせ一覧</h3>

				<div class="tableContainer">
					<table style="word-break:break-all; word-wrap: break-word;" border="0" cellpadding="0" cellspacing="0" class="scrollTable" summary="ユーザー一覧表 ユーザーの詳細情報を一覧で表示しています。">
						<thead>
							<tr>
								<th class="col_select">&nbsp;</th>
								<th class="col_no"    >No</th>
								<th class="col_group" >グループ名</th>
								<th class="col_pos"   >ロール名</th>
								<th class="col_info"  >お知らせ</th>
							</tr>
						</thead>
						<tbody>
<%
		String rowclass = null;
		for(int i = 0; i < List.size() ; i++){
			MaintenanceInfoAuth listdata = (MaintenanceInfoAuth)List.get(i);
			if (listdata.getState() != MaintenanceUser.STATE_DEL){
				switch(listdata.getState()){
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
				if(infoAuth.getIndex() == i && infoAuth.getCd_group() != 0){
					rowclass += " selectedRow";
				}
%>
							<tr class="<%= rowclass %>">
								<td class="col_select">
									<input type="button" name="<%= Form.MTN_SELBTN %><%= i %>" value="選択" onclick="submit_forward('../Servlet/MaintenanceInfoAuth.do','<%= Form.MTN_SELAREA %>','<%= i %>')">
								</td>
								<td class="col_no">
									<%= i+1 %>
								</td>
								<td class="col_group">
<%
				for(int j = 0;j < groupList.size() ; j++){
					Auth group = (Auth)groupList.get(j);
					if(listdata.getCd_group() != 0 && group.getGroup()==listdata.getCd_group()){
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
				 	if(listdata.getCd_pos() != 0 && pos.getPosition()==listdata.getCd_pos()){
%>
							 	<%= pos.getPositionName() %>
<%
					}
				}
%>
								 </td>
								<td class="col_info">
<%
				for(int j = 0; j < infoList.size() ; j++){
					Info info = (Info)infoList.get(j);
					if(listdata.getCd_info() != 0 && info.getCd_info()==listdata.getCd_info()){
%>
								<%=info.getTitle() %>
<%
					}
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

<!-- ===  /content === -->

	<!-- ===  footer === -->
	<div id="footer">
	</div>
	<!-- === /footer === -->

</div>
<!-- ***** /container ***** -->
</body>
</html>