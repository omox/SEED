<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8"%>
<%@ page import="common.DefineReport" %>
<%
	String page_suffix = "_win004";
%>
<div id="<%=page_suffix%>" title="<%=DefineReport.Button.HSPTN.getTxt()%>" style="width:600px;height:450px;position: relative;display:none;">
	<div id="el<%=page_suffix%>" class="easyui-layout" data-options="fit:true" style="pading:0;">
		<form id="ff<%=page_suffix%>" method="post" style="display: inline">

		<div data-options="region:'north',border:false" style="height:0px;width:100%;padding:0px 5px 0;">
			<div class="Search">
				<table id = "tool" style = "display: none;">
				<tbody>
				<tr vAlign="middle">
				<%-- <td><a href="#" class="easyui-linkbutton" tabindex="1004" id="<%=DefineReport.Button.SEARCH.getObj()%><%=page_suffix%>" title="<%=DefineReport.Button.SEARCH.getTxt()%>" iconCls="icon-search" style="width:100px;"><span class="btnTxt"><%=DefineReport.Button.SEARCH.getTxt()%></span></a></td> --%>
				</tr>
				<tr>
					<td>配送パターン</td>
					<td><input class="easyui-numberbox" tabindex="1001" id="<%=DefineReport.InpText.HSPTN.getObj()%><%=page_suffix%>" check='<%=DefineReport.InpText.HSPTN.getMaxlenTag()%>' style="width:130px;"></td>
					<td><a href="#" class="easyui-linkbutton" tabindex="1003" id="<%=DefineReport.Button.SEARCH.getObj()%><%=page_suffix%>" title="<%=DefineReport.Button.SEARCH.getTxt()%>" iconCls="icon-search" style="width:100px;"><span class="btnTxt"><%=DefineReport.Button.SEARCH.getTxt()%></span></a></td>
				</tr>
				<tr>
					<td>配送パターン名（漢字）</td>
					<td><input class="easyui-textbox" tabindex="1002" id="<%=DefineReport.InpText.HSPTNKN.getObj()%><%=page_suffix%>" check='<%=DefineReport.InpText.HSPTNKN.getMaxlenTag()%>' style="width:300px;display:none;"></td>
				</tr>
				</tbody></table>
			</div>
		</div>

		</form>
		<div data-options="region:'center',border:false">
			<div class="easyui-datagrid placeFace" tabindex="1004" id="<%=DefineReport.Grid.HSPTN.getObj()%><%=page_suffix%>" style=></div>
		</div>
		<div data-options="region:'south',border:false" style="display:none;height:30px;padding:2px 5px 0;">
			<div class="btn" style="text-align: center;">
				<a href="#" class="easyui-linkbutton" tabindex="1005" title="<%=DefineReport.Button.CANCEL.getTxt()%>" id="<%=DefineReport.Button.CANCEL.getObj()%><%=page_suffix%>" iconCls="icon-cancel" style="width:100px;"><span><%=DefineReport.Button.CANCEL.getTxt()%></span></a>
				　
				<a href="#" class="easyui-linkbutton" tabindex="1006" title="<%=DefineReport.Button.SELECT.getTxt()%>" id="<%=DefineReport.Button.SELECT.getObj()%><%=page_suffix%>" iconCls="icon-edit" style="width:100px;"><span class="btnTxt"><%=DefineReport.Button.SELECT.getTxt()%></span></a>
			</div>
		</div>
	</div>
</div>
