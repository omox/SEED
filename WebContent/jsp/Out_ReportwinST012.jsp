<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8"%>
<%@ page import="common.DefineReport" %>
<%
	String page_suffix = "_winST012";
%>
<div id="<%=page_suffix%>" title="通常率パターン　選択（ST012）" style="height:450px;position: relative;display:none;">
	<div id="el<%=page_suffix%>" class="easyui-layout" data-options="fit:true" style="pading:0;">
		<div data-options="region:'north',border:false" style="height:55px;width:100%;padding:2px 5px 0;">
			<div class="Search">
				<table class="dataTable" cellspacing="0" cellpadding="0">
				<tbody>
				<tr vAlign="middle">
				<td class="labelCell">部門</td>
				<td class="col_chk" style="width:35px;"><span id="<%=DefineReport.InpText.BMNCD.getObj()%><%=page_suffix%>" ></span></td>
				<td class="labelCell" for_kbn="2">種類臨時</td>
				<td class="col_chk" for_kbn="2" style="width:35px;"><label class="chk_lbl"><input type="checkbox" tabindex="1001" id="<%=DefineReport.Checkbox.RINJI.getObj()%><%=page_suffix%>" style="vertical-align:middle;"></label></td>
				<td class="col_empty" for_kbn="2"><a href="#" class="easyui-linkbutton" tabindex="1002" id="<%=DefineReport.Button.SEARCH.getObj()%><%=page_suffix%>" title="<%=DefineReport.Button.SEARCH.getTxt()%>" iconCls="icon-search" style="width:100px;"><span class="btnTxt"><%=DefineReport.Button.SEARCH.getTxt()%></span></a></td>
				</tr>
				</tbody></table>
				<table class="dataTable" cellspacing="0" cellpadding="0" for_kbn="2">
				<tbody>
				<tr vAlign="middle">
				<td class="labelCell" style="height: 22px;">対象店</td>
				<td class="col_chk" style="width: 35px;"><span id="<%=DefineReport.InpText.RANKNO_ADD.getObj()%><%=page_suffix%>" ></span></td>
				<td class="col_lbl" style="width:300px;"><span id="<%=DefineReport.InpText.RANKNO_ADD.getObj()%>_nm<%=page_suffix%>" ></span></td>
				<td class="labelCell">除外店</td>
				<td class="col_chk" style="width: 35px;"><span id="<%=DefineReport.InpText.RANKNO_DEL.getObj()%><%=page_suffix%>" ></span></td>
				<td class="col_lbl" style="width:300px;"><span id="<%=DefineReport.InpText.RANKNO_DEL.getObj()%>_nm<%=page_suffix%>" ></span></td>
				</tr>
				</tbody></table>
			</div>
		</div>
		<div data-options="region:'center',border:false" style="display:none;">
			<div class="easyui-datagrid placeFace" tabindex="1003" id="<%=DefineReport.Grid.SUB.getObj()%><%=page_suffix%>"></div>
		</div>
		<div data-options="region:'south',border:false" style="display:none;height:30px;padding:2px 5px 0;">
			<div class="btn" style="text-align: center;">
				<a href="#" class="easyui-linkbutton" tabindex="1004" title="<%=DefineReport.Button.CANCEL.getTxt()%>" id="<%=DefineReport.Button.CANCEL.getObj()%><%=page_suffix%>" iconCls="icon-cancel" style="width:100px;"><span><%=DefineReport.Button.CANCEL.getTxt()%></span></a>
				　
				<a href="#" class="easyui-linkbutton" tabindex="1005" title="<%=DefineReport.Button.SELECT.getTxt()%>" id="<%=DefineReport.Button.SELECT.getObj()%><%=page_suffix%>" iconCls="icon-edit" style="width:100px;"><span class="btnTxt"><%=DefineReport.Button.SELECT.getTxt()%></span></a>
			</div>
		</div>
	</div>
</div>
