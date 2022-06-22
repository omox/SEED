<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8"%>
<%@ page import="common.DefineReport" %>
<%
	String page_suffix = "_win002";
%>
<div id="<%=page_suffix%>" title="<%=DefineReport.Button.SIR.getTxt()%>" style="width:600px;height:450px;position: relative;display:none;">
	<div id="el<%=page_suffix%>" class="easyui-layout" data-options="fit:true" style="pading:0;">
		<div data-options="region:'north',border:false" style="height:70px;width:100%;padding:2px 5px 0;">
			<div class="Search">
				<table>
				<tbody>
				<tr vAlign="middle">
				<td>仕入先名（漢字）</td>
				<td><input class="easyui-textbox" tabindex="1001" id="<%=DefineReport.InpText.SIRKN.getObj()%><%=page_suffix%>" check='<%=DefineReport.InpText.SIRKN.getMaxlenTag()%>' style="width:305px;"></td>
				<td><a href="#" class="easyui-linkbutton" tabindex="1002" id="<%=DefineReport.Button.SEARCH.getObj()%><%=page_suffix%>" title="<%=DefineReport.Button.SEARCH.getTxt()%>" iconCls="icon-search" style="width:100px;"><span class="btnTxt"><%=DefineReport.Button.SEARCH.getTxt()%></span></a></td>
				</tr>
				</tbody></table>
			</div>
		</div>
		<div data-options="region:'center',border:false" style="display:none;">
			<div class="easyui-datagrid placeFace" tabindex="1003" id="<%=DefineReport.Grid.SIR.getObj()%><%=page_suffix%>"></div>
		</div>
		<div data-options="region:'south',border:false" style="display:none;height:30px;padding:2px 5px 0;">
			<div class="btn" style="text-align: center;">
				<a href="#" class="easyui-linkbutton" tabindex="1004" title="<%=DefineReport.Button.CANCEL.getTxt()%>" id="<%=DefineReport.Button.CANCEL.getObj()%><%=page_suffix%>" iconCls="icon-cancel" style="width:100px;"><span><%=DefineReport.Button.CANCEL.getTxt()%></span></a>
				　
				<a href="#" class="easyui-linkbutton" tabindex="1005" title="<%=DefineReport.Button.SELECT.getTxt()%>" id="<%=DefineReport.Button.SELECT.getObj()%><%=page_suffix%>" iconCls="icon-edit" style="width:100px;"><span class="btnTxt"><%=DefineReport.Button.SELECT.getTxt()%></span></a>
			</div>
		</div>
	</div>
</div>
