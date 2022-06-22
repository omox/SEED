<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8"%>
<%@ page import="common.DefineReport" %>
<%
	String page_suffix = "_win005";
%>
<div id="<%=page_suffix%>" title="<%=DefineReport.Button.TENPO.getTxt()%>" style="width:600px;height:500px;position: relative;display:none;">
	<div id="el<%=page_suffix%>" class="easyui-layout" data-options="fit:true" style="pading:0;">
		<div data-options="region:'north',border:false" style="height:0px;width:100%;padding:0px 5px 0;">
			<div class="Search">
				<table>
				<tbody>
				<tr vAlign="middle">
				<%-- <td><a href="#" class="easyui-linkbutton" tabindex="1004" id="<%=DefineReport.Button.SEARCH.getObj()%><%=page_suffix%>" title="<%=DefineReport.Button.SEARCH.getTxt()%>" iconCls="icon-search" style="width:100px;"><span class="btnTxt"><%=DefineReport.Button.SEARCH.getTxt()%></span></a></td> --%>
				</tr>
				<tr>
					<td>配送グループ</td>
					<td><input class="easyui-textbox" tabindex="1001" id="<%=DefineReport.InpText.HSGPCD.getObj()%><%=page_suffix%>" check='<%=DefineReport.InpText.HSGPCD.getMaxlenTag()%>' style="width:80px;display:none"></td>
					<td><input class="easyui-textbox" tabindex="1002" id="<%=DefineReport.InpText.HSGPKN.getObj()%><%=page_suffix%>" check='<%=DefineReport.InpText.HSGPKN.getMaxlenTag()%>' style="width:200px;display:none"></td>

				</tr>
				<tr>
					<td>店グループ</td>
					<td><input class="easyui-textbox" tabindex="1003" id="<%=DefineReport.InpText.TENGPCD.getObj()%><%=page_suffix%>" check='<%=DefineReport.InpText.TENGPCD.getMaxlenTag()%>' style="width:100px;display:none"></td>
					<td><input class="easyui-textbox" tabindex="1004" id="<%=DefineReport.InpText.TENGPKN.getObj()%><%=page_suffix%>" check='<%=DefineReport.InpText.TENGPKN.getMaxlenTag()%>' style="width:200px;display:none"></td>
				</tr>
				</tbody></table>
			</div>
		</div>
		<div data-options="region:'center',border:false" style="display:none;">
			<div class="easyui-datagrid placeFace" tabindex="1006" id="<%=DefineReport.Grid.TENPO.getObj()%><%=page_suffix%>"></div>
		</div>
		<div data-options="region:'south',border:false" style="display:none;height:30px;padding:2px 5px 0;">
			<div class="btn" style="text-align: center;">
				<a href="#" class="easyui-linkbutton" tabindex="1007" title="<%=DefineReport.Button.CANCEL.getTxt()%>" id="<%=DefineReport.Button.CANCEL.getObj()%><%=page_suffix%>" iconCls="icon-cancel" style="width:100px;"><span><%=DefineReport.Button.CANCEL.getTxt()%></span></a>
				　
				<%-- <a href="#" class="easyui-linkbutton" tabindex="1005" title="<%=DefineReport.Button.SELECT.getTxt()%>" id="<%=DefineReport.Button.SELECT.getObj()%><%=page_suffix%>" iconCls="icon-edit" style="width:100px;"><span class="btnTxt"><%=DefineReport.Button.SELECT.getTxt()%></span></a> --%>
			</div>
		</div>
	</div>
</div>
