<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="MS932" %>
<%@page import="common.CmnDate"%>
<%@ page import="authentication.defines.*" %>
<%@ page import="authentication.bean.*" %>
<%
	String path = request.getContextPath();
	String logo = "../frame/img/logo_inageya.gif";
	User lusr = (User)session.getAttribute(Consts.STR_SES_LOGINUSER);
	if (lusr != null){
		if (!"".equals(lusr.getLogo_())){
			logo = lusr.getLogo_();
		}
	}
	String icon = "";//"../frame/img/ICON_AQUA.ico";

	String logindt = (String)session.getAttribute(Consts.STR_SES_LOGINDT);
	String sysdate = "";
	if(logindt != null){
		sysdate = CmnDate.dateFormat(CmnDate.convDate(logindt), CmnDate.DATE_FORMAT.GRID_YMD);
	}

	String tenkn = "";

	if (lusr != null){

		tenkn = lusr.getYobi2Nm_();
		if (!"".equals(tenkn) && tenkn != null){
			tenkn += "　";
		} else {
			tenkn = "";
		}
	}
 %>

<script type="text/javascript">
	var script = document.createElement("link");
	script.setAttribute("rel", "icon");
	script.setAttribute("type", "image/x-icon");
	script.setAttribute("href", "../frame/img/ICA.ico");
	document.getElementsByTagName("head")[0].appendChild(script);
</script>


<div id="top_title">
<a href="http://www.inageya.co.jp/"><img src="<%=logo %>" alt="社名ロゴ"></a>
</div>

<%
if ((request.getParameter(Form.LOGIN_VIEW) == null) && (session.getAttribute(Consts.STR_SES_LOGINUSER) != null)){
	// ログオン画面表示時のパラメータ情報を取得
	String User = session.getAttribute("_"+Form.LOGIN_USER)==null ? "" : (String)session.getAttribute("_"+Form.LOGIN_USER);
	String Pass = session.getAttribute("_"+Form.LOGIN_PASS)==null ? "" : (String)session.getAttribute("_"+Form.LOGIN_PASS);
	String View = session.getAttribute("_"+Form.LOGIN_VIEW)==null ? "" : (String)session.getAttribute("_"+Form.LOGIN_VIEW);
// 	String Parameter = "";
// 	if (!"".equals(User)){
// 		// ログアウト時に初期化するパラメータ情報
// 		Parameter = "?"+Form.LOGIN_USER+"="+User+"&"+Form.LOGIN_PASS+"="+Pass+"&"+Form.LOGIN_VIEW+"="+View;
// 	}
%>
<div id="top_navi" style="padding-left: 10px;">
	<a href="<%=path%>/Servlet/Menu.do">メニュー</a>
	<input type="hidden" id="hdn_menu_path" value="<%=path%>/Servlet/Menu.do" />
	<span>　</span>
	<%--
	<a href="<%=path%>/Servlet/Login.do<%=Parameter %>">ログアウト</a>
	--%>
	<a href="#" id="hlnk_close" onclick="window.close(); return false;">閉じる</a>
	<br/>
</div>
<div id="infomation">
	<div id="user_info" style="display:block;">
		<div style="overflow:visible;">
		<span id="login_dt"><%= sysdate %></span>　<span id="disp_report_id">　 　　</span>
		</div>
		<span><%= tenkn + lusr.getName() %>様</span>
<%-- 		<a href="<%=path%>/Servlet/PasswordChange.do?"+ <%=Form.MTN_SIDE%> + "','"+ <%=Consts.SYSTEM_MENTENANCE%> +"'>パスワード変更</a> --%>
	</div>
</div>
<%
} else {
%>
<div id="top_navi">
	<span>　</span>
	<a href="#" id="hlnk_close" onclick="window.close(); return false;">閉じる</a>
</div>
<%
}
%>