<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="MS932"%>
<%@ page import="java.util.*" %>
<%@ page import="authentication.defines.*" %>
<%@ page import="authentication.bean.*" %>

<%
	MaintenanceUser user = (MaintenanceUser)request.getAttribute(Consts.STR_REQ_REC);
	ArrayList userList = (ArrayList)session.getAttribute(Consts.STR_SES_GRD);
	ArrayList posList = (ArrayList)session.getAttribute(Consts.STR_SES_GRD2);
	ArrayList athList = (ArrayList)session.getAttribute(Consts.STR_SES_GRD3);
	ArrayList groupList = (ArrayList)session.getAttribute(Consts.STR_SES_GRD5);
	ArrayList posList_user = (ArrayList)session.getAttribute(Consts.STR_SES_GRD6);
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
<LINK HREF="../frame/css/m_user.css" REL="stylesheet" TYPE="text/css">
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
	<form id="m_user" method="post" action="../Servlet/MaintenanceUser.do">
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
					<label for="<%=Form.MTN_USR_ETY_CD %>" class="required">ユーザーID</label>
					<!--  <span class="hidden"><% if(user.getCD_user() != 0){%><%= user.getCD_user() %><%}%>&nbsp;</span> -->
					<input class="text1" type="text" id="<%=Form.MTN_USR_ETY_CD %>" name="<%=Form.MTN_USR_ETY_CD %>" value="<% if (user.getCD_user() != 0){%><%= user.getCD_user() %><%}%>" >
					<br class="clear">
				</div>

				<div class="inpsection">
					<label for="<%=Form.MTN_USR_ETY_UID %>" class="required">ユーザーID</label>
					<!--  <span class="hidden"><% if(user.getId() != null){%><%= user.getId() %><%}%>&nbsp;</span> -->
					<input class="text1" type="text" id="<%=Form.MTN_USR_ETY_UID %>" name="<%=Form.MTN_USR_ETY_UID %>" value="<% if (user.getId() != null){%><%= user.getId() %><%}%>" >
					<br class="clear">
				</div>

				<div class="inpsection">
					<label for="<%=Form.MTN_USR_ETY_PAS %>" class="required">パスワード</label>
					<input class="text1" type="text" id="<%=Form.MTN_USR_ETY_PAS %>" name="<%=Form.MTN_USR_ETY_PAS %>" value="<% if(user.getPass() != null){%><%= user.getPass() %><%}%>" >
					<span id="cal_pwdt"></span>
				</div>

				<div class="inpsection">
					<label for="<%=Form.MTN_USR_ETY_NM_FAMILY %>" class="required">姓</label>
					<!--<span class="hidden"><% if(user.getNm_family() != null){%><%= user.getNm_family() %><%}%>&nbsp;</span>-->
					<input class="text1" type="text" id="<%=Form.MTN_USR_ETY_NM_FAMILY %>" name="<%=Form.MTN_USR_ETY_NM_FAMILY %>" value="<% if(user.getNm_family() != null){%><%= user.getNm_family() %><%}%>" >
					<br class="clear">
				</div>

				<div class="inpsection">
					<label for="<%=Form.MTN_USR_ETY_NM_NAME %>" class="required">名</label>
					<!--  <span class="hidden"><% if(user.getNm_name() != null){%><%= user.getNm_name() %><%}%>&nbsp;</span>-->
					<input class="text1" type="text" id="<%=Form.MTN_USR_ETY_NM_NAME %>" name="<%=Form.MTN_USR_ETY_NM_NAME %>" value="<% if(user.getNm_name() != null){%><%= user.getNm_name() %><%}%>" >
					<br class="clear">
				</div>

				<div class="inpsection">
					<label for="<%=Form.MTN_USR_ETY_POS %>" class="required">ロール名</label>
					<select id="<%=Form.MTN_USR_ETY_POS %>" multiple name="<%=Form.MTN_USR_ETY_POS %>" size="4">

<%
					for(int i = 0; i < posList.size() ; i++){
						Position pos = (Position)posList.get(i);

%>
			 			<option value="<%= pos.getPosition() %>" <%

						if(user.getPoslist() !=null){
							String[] poslist=user.getPoslist();
							for(int k = 0 ; k< poslist.length;k++){
								if(poslist[k] != null && pos.getPosition() == Integer.parseInt(poslist[k])){%>selected<%}
							}
						}else{
						 	for(int k = 0; k < posList_user.size() ; k++){
								Position pos_user = (Position)posList_user.get(k);
								if(user.getId() != null && pos_user.getCd_user() == user.getCD_user() && pos_user.getPosition()== pos.getPosition()){%>
									selected
<%								}
							}
						}
%>
				 			><%= pos.getPositionName() %>
			 			</option>
<%
					}
%>
					</select>
				</div>

				<div class="inpsection">
					<label for="<%=Form.MTN_USR_ETY_ATH %>" class="required">グループ名</label>
					<select id="<%=Form.MTN_USR_ETY_ATH %>" Multiple name="<%=Form.MTN_USR_ETY_ATH %>" size="4">
<%
			for(int i = 0; i < athList.size() ; i++){
				Auth ath = (Auth)athList.get(i);
%>
				 		<option value="<%= ath.getGroup() %>" <%
			 	if(user.getGrouplist() != null){
			 		String[] group_date = user.getGrouplist();
			 		for(int k = 0; k< group_date.length;k++){
			 			if(group_date[k] != null && ath.getGroup()==Integer.parseInt(group_date[k])){%>selected<%}
			 		}
			 	}else{
				 	for(int k = 0; k < groupList.size() ; k++){
				 		Auth group = (Auth)groupList.get(k);
				 		if(user.getId() != null && group.getCd_user()== user.getCD_user() && group.getGroup() == ath.getGroup()){
%>
							selected
<%
						}
					}
				}
%>
						 	><%= ath.getGroupName() %>
				 		</option>
<%
			}
%>
					</select>
				</div>

			</fieldset>

			<fieldset class="commands">
				<legend>入力したUser情報を登録・削除・又はリセットする事ができます。</legend>
				<input type="hidden" name="<%= Form.MTN_USR_ETY_IDX %>" value="<%= user.getIndex() %>">
				<input type="button" name="<%= Form.MTN_ENTBTN %>" value="登録" onclick="submit_forward('../Servlet/MaintenanceUser.do','<%= Form.MTN_INPAREA %>','<%= Form.MTN_ENTBTN %>')">
				<input type="button" name="<%= Form.MTN_DELBTN %>" value="削除" onclick="if(confirm('削除してよろしいですか？')){submit_forward('../Servlet/MaintenanceUser.do','<%= Form.MTN_INPAREA %>','<%= Form.MTN_DELBTN %>')}">
				<input type="button" name="<%= Form.MTN_RSTBTN %>" value="リセット" onclick="if(confirm('リセットしてよろしいですか？')){submit_forward('../Servlet/MaintenanceUser.do','<%= Form.MTN_INPAREA %>','<%= Form.MTN_RSTBTN %>')}">
			</fieldset>
		</div>

		<div id="view">
			<h3>ユーザー一覧</h3>

			<div class="tableContainer">
				<table style="word-break:break-all; word-wrap: break-word;" border="0" cellpadding="0" cellspacing="0" class="scrollTable" summary="ユーザー一覧表 ユーザーの詳細情報を一覧で表示しています。">
					<thead>
						<tr>
							<th class="col_select">&nbsp;</th>
							<th class="col_no"    >No</th>
							<th class="col_uid"   >ユーザーID</th>
							<th class="col_upw"   >パスワード</th>
							<th class="col_uname" >姓名</th>
							<!--
							<th class="col_auth2" >権限</th>
							<th class="col_range" >有効期限</th>
							 -->
							<th class="col_pos"   >ロール名</th>
							<th class="col_auth"  >グループ名</th>
						</tr>
					</thead>
					<tbody>
<%
		String rowclass = null;
		String writeData = "";
		for(int i = 0; i < userList.size() ; i++){
			MaintenanceUser usr = (MaintenanceUser)userList.get(i);
			if (usr.getState() != MaintenanceUser.STATE_DEL){
				switch(usr.getState()){
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
				if(user.getIndex() == i && user.getId() != null){
					rowclass += " selectedRow";
				}
%>
						<tr class="<%= rowclass %>">
							<td class="col_select">
								<input type="button" name="<%= Form.MTN_SELBTN %><%= i %>" value="選択" onclick="submit_forward('../Servlet/MaintenanceUser.do','<%= Form.MTN_SELAREA %>','<%= i %>')">
							</td>
							<td class="col_no">
								<%= i+1 %>
							</td>
							<td class="col_uid">
								<%= usr.getId() %>
							</td>
							<td class="col_upw">
								<%= usr.getPass() %>&nbsp;
							</td>
							<td class="col_uname">
								<%= usr.getName() %>&nbsp;
							</td>
							<!--
							<td class="col_auth2">
								<%//=usr.getCd_auth_() %>
							</td>
							<td class="col_range">
								<%//=usr.getDt_pw_term_() %>
							</td>
							 -->
							<td class="col_pos">
<%
				writeData = "";
				if (usr.getPoslist() ==null){

					for(int j = 0; j < posList_user.size() ; j++){
						Position pos = (Position)posList_user.get(j);

			 			if(usr.getId() != null && pos.getCd_user() == usr.getCD_user()){
			 				if (!writeData.equals("")) {
			 					writeData += ", ";
			 				}
			 				writeData += pos.getPositionName();
			 			}
			 		}

				} else {
					String[] position =usr.getPoslist();
					for(int k = 0;k<position.length;k++){

						for(int j = 0; j < posList.size() ; j++){
							Position pos = (Position)posList.get(j);

				 			if(position[k] != null && pos.getPosition() == Integer.parseInt(position[k])){
				 				if (!writeData.equals("")) {
				 					writeData += ", ";
				 				}
				 				writeData += pos.getPositionName() + ",";
				 			}
						}
					}
				}
%>
			 					<%=writeData  %>
							</td>
							<td class="col_auth">
<%
				writeData = "";
				if(usr.getGrouplist() ==null) {

					for(int j = 0; j < groupList.size() ; j++){
						Auth group = (Auth)groupList.get(j);
					 	if(usr.getId() != null && group.getCd_user()== usr.getCD_user()){
			 				if (!writeData.equals("")) {
			 					writeData += ", ";
			 				}
					 		writeData += group.getGroupName();
					 	}
					 }
				} else {
					String[] group =usr.getGrouplist();
					for(int k = 0;k<group.length;k++){
						for(int j = 0; j < athList.size() ; j++){

							Auth ath = (Auth)athList.get(j);
			 				if(group[k] != null && ath.getGroup()==Integer.parseInt(group[k])){
				 				if (!writeData.equals("")) {
				 					writeData += ", ";
				 				}
						 		writeData += ath.getGroupName();
						 	}
						 }
					}
				}
%>
			 					<%= writeData %>
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