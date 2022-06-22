<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8"%>
<%@ page import="common.DefineReport" %>
<%
	String page_suffix3 = "_JU017";
%>
<div id="<%=page_suffix3%>" title="<%=DefineReport.Button.SURYO.getTxt()%>" style="width:600px;height:450px;position: relative;display:none;">
	<div id="el<%=page_suffix3%>" class="easyui-layout" data-options="fit:true" style="pading:0;">
		<div data-options="region:'north',border:false" style="height:70px;width:100%;padding:2px 5px 0;">
			<div class="Search">
				<table class="dataTable" cellspacing="0" cellpadding="0">
				<tbody>
				<tr vAlign="middle">
				<td class="labelCell" style="text-align:center">部門</td>
				</tr>
				<TR>
				<td class="col_chk" style="width:35px;text-align:left"><span id="<%=DefineReport.InpText.BMNCD.getObj()%><%=page_suffix3%>" ></span></td>
				</tr>
				</tbody></table>
			</div>
		</div>
		<div data-options="region:'center',border:false" style="display:none;">
			<div class="easyui-datagrid placeFace" tabindex="1" id="<%=DefineReport.Grid.SURYO.getObj()%><%=page_suffix3%>"></div>
			<div class="ref_editor" style="display: none;">
				<input  class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.SRYPTNNO.getObj()%>" check='<%=DefineReport.InpText.SRYPTNNO.getMaxlenTag()%>' />
			<!--一部の入力項目については配送パターンにて定義済み  -->
			<!--名称マスタ項目については他の項目にて定義済み  -->
			<!--[実仕入先名称]項目については他の項目にて定義済み  -->
			</div>
		</div>
		<div data-options="region:'south',border:false" style="display:none;height:40px;padding:2px 5px 0;">
			<div class="btn" style="text-align: center;">
			<table class="t-layout3">
			<tr>
				<td><a href="#" class="easyui-linkbutton" tabindex="2" title="<%=DefineReport.Button.CANCEL.getTxt()%>" id="<%=DefineReport.Button.CANCEL.getObj()%><%=page_suffix3%>" iconCls="icon-cancel" style="width:100px;"><span><%=DefineReport.Button.CANCEL.getTxt()%></span></a></td>
				<td><a href="#" class="easyui-linkbutton" tabindex="3" title="<%=DefineReport.Button.SELECT.getTxt()%>" id="<%=DefineReport.Button.SELECT.getObj()%><%=page_suffix3%>" iconCls="icon-edit" style="width:100px;"><span class="btnTxt"><%=DefineReport.Button.SELECT.getTxt()%></span></a></td>
				</tr>
				</table>
			</div>
		</div>
	</div>
</div>
