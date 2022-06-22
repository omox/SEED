<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8"%>
<%@ page import="common.DefineReport" %>
<%
	String page_suffix = "_winIT031";
%>
<div id="<%=page_suffix%>" title="" style="width:945px;height:450px;position: relative;display:none;">
	<div id="el<%=page_suffix%>" class="easyui-layout" data-options="fit:true" style="pading:0;">
		<form id="uf<%=page_suffix%>" method="post" style="display: inline" enctype="multipart/form-data" encoding="multipart/form-data" target="if<%=page_suffix%>">
		<div data-options="region:'north',border:false" style="height:30px;width:100%;padding:5px 5px 0;">
			<div class="Search">
				<table class="dataTable" cellspacing="0" cellpadding="0" style="display:inline;">
				<tbody>
				<tr>
					<td class="labelCell">商品コード</td>
					<td class="col_lbl"  style="width: 70px;"><span id="<%=DefineReport.InpText.SHNCD.getObj()%><%=page_suffix%>"></span></td>
					<td class="labelCell">第1ソースコード</td>
					<td class="col_lbl"  style="width:120px;"><span id="<%=DefineReport.InpText.SRCCD.getObj()%>1<%=page_suffix%>"></span></td>
					<td class="labelCell">第2ソースコード</td>
					<td class="col_lbl"  style="width:120px;"><span id="<%=DefineReport.InpText.SRCCD.getObj()%>2<%=page_suffix%>"></span></td>
				</tr>
				</tbody></table>
			</div>
			<input type="file" accept=".csv" tabindex="-1" name="<%=DefineReport.Text.FILE.getObj()%><%=page_suffix%>" id="<%=DefineReport.Text.FILE.getObj()%><%=page_suffix%>" style="display: none;"/>
		</div>
		</form>
		<form id="gf<%=page_suffix%>" class="e_grid">
		<div data-options="region:'center',border:false" style="display:none;">
			<div class="easyui-datagrid placeFace" tabindex="1005" id="<%=DefineReport.Grid.SRCCD.getObj()%><%=page_suffix%>"></div>
			<!-- Editor参照用 -->
			<div class="ref_editor" style="display: block;">
				<input type="checkbox" tabindex="-1" id="<%=DefineReport.Checkbox.DEL.getObj()%><%=page_suffix%>"/>
				<input class="easyui-textbox_" tabindex="-1" id="<%=DefineReport.InpText.SRCCD.getObj()%><%=page_suffix%>" check='<%=DefineReport.InpText.SRCCD.getMaxlenTag()%>'/>
				<select class="easyui-combobox_" tabindex="-1" id="<%=DefineReport.MeisyoSelect.KBN136.getObj()%><%=page_suffix%>" data-options="panelWidth:140"></select>
				<input class="easyui-numberbox_" tabindex="-1" id="<%=DefineReport.InpText.YUKO_STDT.getObj()%><%=page_suffix%>" data-options="prompt:'<%=DefineReport.Label.PROMPT_DT.getTxt()%>'" check='<%=DefineReport.InpText.YUKO_STDT.getMaxlenTag()%>'/>
				<input class="easyui-numberbox_" tabindex="-1" id="<%=DefineReport.InpText.YUKO_EDDT.getObj()%><%=page_suffix%>" data-options="prompt:'<%=DefineReport.Label.PROMPT_DT.getTxt()%>'" check='<%=DefineReport.InpText.YUKO_EDDT.getMaxlenTag()%>'/>
				<input class="easyui-numberbox_" tabindex="-1" id="<%=DefineReport.InpText.SEQNO.getObj()%><%=page_suffix%>" check='<%=DefineReport.InpText.SEQNO.getMaxlenTag()%>'/>
			</div>
		</div>
		</form>
		<div data-options="region:'south',border:false" style="display:none;height:30px;padding:2px 5px 0;">
			<div class="btn">
				<table class="t-layout3">
				<tr>
					<th style="min-width:110px;"></th><th style="min-width:10px;"></th>
					<th style="min-width:110px;"></th><th style="min-width:110px;"></th><th style="min-width:110px;"></th>
				</tr>
				<tr>
					<td><a href="#" class="easyui-linkbutton" tabindex="1501" id="<%=DefineReport.Button.BACK.getObj()%><%=page_suffix%>" title="<%=DefineReport.Button.BACK.getTxt()%>" iconCls="icon-undo" style="width:100px;"><span><%=DefineReport.Button.BACK.getTxt()%></span></a></td>
					<td></td>
					<td><a href="#" class="easyui-linkbutton" tabindex="1502" id="<%=DefineReport.Button.NEW.getObj()%><%=page_suffix%>" title="追加" iconCls="icon-add" style="width:100px;">追加</a></td>
					<td><a href="#" class="easyui-linkbutton" tabindex="1503" id="<%=DefineReport.Button.UPD.getObj()%><%=page_suffix%>" title="<%=DefineReport.Button.UPD.getTxt()%>" iconCls="icon-save" style="width:100px;" data-options="winIT031:false"><span class="btnTxt">F12:<%=DefineReport.Button.UPD.getTxt()%></span></a></td>
					<td><a href="#" class="easyui-linkbutton" tabindex="1503" id="<%=DefineReport.Button.CSV_IMPORT.getObj()%><%=page_suffix%>" title="<%=DefineReport.Button.CSV_IMPORT.getTxt()%>" iconCls="icon-save" style="width:100px;"><span class="btnTxt"><%=DefineReport.Button.CSV_IMPORT.getTxt()%></span></a></td>
				</tr>
				</table>
			</div>
		</div>
	</div>
	<input type="hidden" name="<%=DefineReport.Hidden.CHANGED_IDX.getObj()%><%=page_suffix%>" id="<%=DefineReport.Hidden.CHANGED_IDX.getObj()%><%=page_suffix%>" />
</div>
<iframe name="if<%=page_suffix%>" id="if<%=page_suffix%>" frameborder="0" border="0" width="0" height="0" scrolling="yes"></iframe>
