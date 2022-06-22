<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="MS932"%>
<%@ page import="java.util.*" %>
<%@ page import="authentication.defines.*" %>
<%@ page import="authentication.bean.*" %>

<%
	MaintenanceInfo info = (MaintenanceInfo)request.getAttribute(Consts.STR_REQ_REC);
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
<LINK HREF="../frame/css/m_info.css" REL="stylesheet" TYPE="text/css">
<link rel="stylesheet" type="text/css" href="../themes/easyui/default/easyui.css">
<link rel="stylesheet" type="text/css" href="../themes/easyui/icon.css">
<script type="text/javascript" src="../js/jquery.min.js"></script>
<script type="text/javascript" src="../js/jquery.easyui.min.js"></script>
<script type="text/javascript" src="../js/jquery.info.control.js"></script>
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

	<form id="m_info" method="post" action="../Servlet/MaintenanceInfo.do">

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
					<label for="<%=Form.MTN_INFO_CODE %>" class="required">お知らせコード</label>
					<!--  <span class="hidden"><% if(Integer.toString(info.getCd_info()) != null){%><%=info.getCd_info()  %><%}%>&nbsp;</span> -->
					<input class="text1" type="text" id="<%=Form.MTN_INFO_CODE %>" name="<%=Form.MTN_INFO_CODE %>" value="<% if (Integer.toString(info.getCd_info())!= null){%> <%= info.getCd_info() %><%}%>" >
				</div>

				<div class="inpsection">
					<label for="<%=Form.MTN_INFO_ETY_DT_START %>" class="required">表示開始日</label>
					<!--  <span class="hidden"><% if(info.getDt_start() != null){%><%=info.getDt_start()  %><%}%>&nbsp;</span> -->
					<input class="text1" type="text" id="<%=Form.MTN_INFO_ETY_DT_START %>" name="<%=Form.MTN_INFO_ETY_DT_START %>" value="<% if (info.getDt_start() != null){%><%= info.getDt_start() %><%}%>" >
					<br class="clear">
				</div>

				<div class="inpsection">
					<label for="<%=Form.MTN_INFO_ETY_DT_END %>" class="required">表示終了日</label>
					<!--  <span class="hidden"><% if(info.getDt_end() != null){%><%=info.getDt_end()  %><%}%>&nbsp;</span> -->
					<input class="text1" type="text" id="<%=Form.MTN_INFO_ETY_DT_END %>" name="<%=Form.MTN_INFO_ETY_DT_END %>" value="<% if (info.getDt_end() != null){%><%= info.getDt_end() %><%}%>" >
					<br class="clear">
				</div>

				<div class="inpsection">
					<label for="<%=Form.MTN_INFO_ETY_FLG_ALWAYS %>" class="required">常時表示設定</label>
					<!--  <span class="hidden"><% if(info.getFlg_disp_always() != null){%><%=info.getFlg_disp_always()  %><%}%>&nbsp;</span> -->
					<input type="checkbox" id="<%=Form.MTN_INFO_ETY_FLG_ALWAYS %>" name="<%=Form.MTN_INFO_ETY_FLG_ALWAYS %>" value="<%=Consts.INFO_ALWAYS %>" <% if (Consts.INFO_ALWAYS.equals(info.getFlg_disp_always())){%>checked<%}%> >
					<label for="<%=Form.MTN_INFO_ETY_FLG_ALWAYS %>" class="label2">常時表示する</label>
					<br class="clear">
				</div>

				<div class="inpsection">
					<label for="<%=Form.MTN_INFO_ETY_NO_DISP %>" class="required">表示順</label>
					<!--  <span class="hidden"><% if(info.getNo_disp() != null){%><%=info.getNo_disp()  %><%}%>&nbsp;</span> -->
					<input class="text1" type="text" id="<%=Form.MTN_INFO_ETY_NO_DISP %>" name="<%=Form.MTN_INFO_ETY_NO_DISP %>" value="<% if (info.getNo_disp() != null){%><%= info.getNo_disp() %><%}%>" >
					<br class="clear">
				</div>

				<div class="inpsection wide1">
					<label for="<%=Form.MTN_INFO_ETY_TITLE %>" class="required">タイトル</label>
					<input class="text3" type="text" id="<%=Form.MTN_INFO_ETY_TITLE %>" name="<%=Form.MTN_INFO_ETY_TITLE %>" value="<% if (info.getTitle() != null){%><%= info.getTitle() %><%}%>" >
					<br class="clear">
				</div>

				<div class="inpsection wide1">
					<label for="<%=Form.MTN_INFO_ETY_INFO %>" class="required">お知らせ</label>
					<textarea class="text3" rows="4" id="<%=Form.MTN_INFO_ETY_INFO %>" name="<%=Form.MTN_INFO_ETY_INFO %>"><% if (info.getInformation() != null){%><%= info.getInformation() %><%}%></textarea>
					<br class="clear">
				</div>
			</fieldset>

			<fieldset class="commands">
				<legend>入力したUser情報を登録・削除・又はリセットする事ができます。</legend>
				<input type="hidden" name="<%= Form.MTN_USR_ETY_IDX %>" value="<%= info.getIndex() %>">
				<input type="button" name="<%= Form.MTN_ENTBTN %>" value="登録" onclick="submit_forward('../Servlet/MaintenanceInfo.do','<%= Form.MTN_INPAREA %>','<%= Form.MTN_ENTBTN %>')">
				<input type="button" name="<%= Form.MTN_DELBTN %>" value="削除" onclick="if(confirm('削除してよろしいですか？')){submit_forward('../Servlet/MaintenanceInfo.do','<%= Form.MTN_INPAREA %>','<%= Form.MTN_DELBTN %>')}">
				<input type="button" name="<%= Form.MTN_RSTBTN %>" value="リセット" onclick="if(confirm('リセットしてよろしいですか？')){submit_forward('../Servlet/MaintenanceInfo.do','<%= Form.MTN_INPAREA %>','<%= Form.MTN_RSTBTN %>')}">
				<input type="button" name="<%= Form.MTN_BTN_CLEAN %>" class="btn2" value="不要データ削除" onclick="if(confirm('削除してよろしいですか？')){submit_forward('../Servlet/MaintenanceInfo.do','<%= Form.MTN_INPAREA %>','<%= Form.MTN_BTN_CLEAN %>')}">
				<input type="button" id="info_open" class="btn2" value="プレビュー">
			</fieldset>
			<br class="clear">
		</div>



		<div id="view">
			<h3>お知らせ一覧</h3>
			<div class="tableContainer">
				<table style="word-break:break-all; word-wrap: break-word; table-layout:fixed;"  border="0" cellpadding="0" cellspacing="0" class="scrollTable" summary="ユーザー一覧表 ユーザーの詳細情報を一覧で表示しています。">
					<thead>
						<tr>
							<th class="col_select"    >&nbsp;</th>
							<th class="col_no"        >No</th>
							<th class="col_dt_start"  >表示開始日</th>
							<th class="col_dt_end"    >表示終了日</th>
							<th class="col_flg_always">常時表示</th>
							<th class="col_no_disp"   >表示順</th>
							<th class="col_title"     >タイトル</th>
							<th class="col_info"      >お知らせ</th>
							<!--  <td class="col_space" >&nbsp;</td>-->
						</tr>
					</thead>
					<tbody>
<%
		String rowclass = null;
		for(int i = 0; i < List.size() ; i++){
			MaintenanceInfo listdata = (MaintenanceInfo)List.get(i);
			if (listdata.getState() != MaintenanceUser.STATE_DEL){
				switch(listdata.getState()){
				case MaintenanceInfo.STATE_UPD:
					rowclass = "updatedRow"; break;
				default:
					switch(i%2){
					case 0:
						rowclass="alternateRow"; break;
					default:
						rowclass="normalRow"; break;
					}
				}
				if(info.getIndex() == i && Integer.toString(info.getCd_info()) != null){
					rowclass += " selectedRow";
				}
%>
						<tr class="<%= rowclass %>">
							<td class="col_select">
								<input type="button" name="<%= Form.MTN_SELBTN %><%= i %>" value="選択" onclick="submit_forward('../Servlet/MaintenanceInfo.do','<%= Form.MTN_SELAREA %>','<%= i %>')">
							</td>
							<td class="col_no">
								<%= i+1 %>
							</td>
							<td class="col_dt_start">
								<%=listdata.getDt_start() %>&nbsp;
							</td>
							<td class="col_dt_end">
								<%=listdata.getDt_end() %>&nbsp;
							</td>
							<td class="col_flg_always">
								<%=Consts.INFO_ALWAYS.equals(listdata.getFlg_disp_always()) ? listdata.getFlg_disp_always() : "&nbsp;" %>
							</td>
							<td class="col_no_disp">
								<%=listdata.getNo_disp() %>
							</td>
							<td class="col_title straight">
								<span class="col_title straight"><%=listdata.getTitle() %></span>
							</td>
							<td class="col_info straight">
								<span class="col_info straight"><%=listdata.getInformation() %></span>
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
	<form id="infoView">
		<input type="hidden" id="<%=Form.MTN_INFO_VIEW_TITLE %>" name="<%=Form.MTN_INFO_VIEW_TITLE %>" >
		<input type="hidden" id="<%=Form.MTN_INFO_VIEW_INFO %>" name="<%=Form.MTN_INFO_VIEW_INFO %>" >
	</form>
</div>
</div>
</body>
</html>