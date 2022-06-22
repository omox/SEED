<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8"%>
<%@ page import="common.DefineReport" %>
<%
	String page_suffix = "_winIT032";
%>
<div id="<%=page_suffix%>" title="" style="width:755px;height:450px;position: relative;display:none;">
	<div id="el<%=page_suffix%>" class="easyui-layout" data-options="fit:true" style="pading:0;">
		<form id="uf<%=page_suffix%>" method="post" style="display: inline" enctype="multipart/form-data" encoding="multipart/form-data" target="if<%=page_suffix%>">
		<div data-options="region:'north',border:false" style="height:70px;width:100%;padding:2px 5px 0;">
			<div class="Search">
				<table class="dataTable" cellspacing="0" cellpadding="0" style="display:inline;"><tbody>
				<tr>
					<td class="col_empty">商品コード</td>
					<td class="col_empty lbl_box"><input class="easyui-numberbox_" tabindex="-1" id="<%=DefineReport.InpText.SHNCD.getObj()%><%=page_suffix%>" check='<%=DefineReport.InpText.SHNCD.getMaxlenTag()%>' data-options="prompt:'____-____',readonly:true,editable:false" style="width: 90px;"></td>
					<td class="col_empty"></td>
					<td class="col_empty"></td>
					<td class="col_empty"></td>
				</tr>
				<tr>
					<td class="col_empty">商品名（カナ）</td>
					<td class="col_empty"><input class="easyui-textbox_" tabindex="1001" id="<%=DefineReport.InpText.SHNAN.getObj()%><%=page_suffix%>" check='<%=DefineReport.InpText.SHNAN.getMaxlenTag()%>' style="width:390px;"></td>
					<td class="col_empty"></td>
					<td class="col_empty"></td>
					<td class="col_empty"></td>
				</tr>
				<tr>
					<td class="col_empty">商品コード</td>
					<td class="col_empty"><input class="easyui-textbox_" tabindex="1002" id="<%=DefineReport.InpText.SHNKN.getObj()%><%=page_suffix%>" check='<%=DefineReport.InpText.SHNKN.getMaxlenTag()%>' style="width:390px;"></td>
					<td class="col_empty"></td>
					<td class="labelCell">店舗数</td>
					<td class="col_num"  ><span id="<%=DefineReport.Text.TEN_NUMBER.getObj()%><%=page_suffix%>" style="width:60px;display: block;"></span></td>
				</tr>
				</tbody></table>
			</div>
			<input type="file" accept=".csv" tabindex="-1" name="<%=DefineReport.Text.FILE.getObj()%><%=page_suffix%>" id="<%=DefineReport.Text.FILE.getObj()%><%=page_suffix%>" style="display: none;"/>
		</div>
		</form>
		<form id="gf<%=page_suffix%>" class="e_grid">
		<div data-options="region:'center',border:false" style="display:none;width:600px;">
			<div class="easyui-datagrid " tabindex="1003" id="<%=DefineReport.Grid.SUB.getObj()%><%=page_suffix%>"></div>
			<div class="ref_editor" style="display: none;">
				<input class="easyui-numberbox_" tabindex="-1" id="<%=DefineReport.InpText.AHSKB.getObj()%><%=page_suffix%>" check='<%=DefineReport.InpText.AHSKB.getMaxlenTag()%>' />
			</div>
		</div>
		<div data-options="region:'east',border:false" style="display:none;width:145px;">
			<fieldset style="height: 95%;text-align: ceneter;">
			<legend>店番 一括入力</legend>
				<div class="easyui-layout" data-options="fit:true" style="pading:0;">
					<div data-options="region:'center',border:false" style="display:none;">
						<div class="easyui-datagrid" tabindex="1008" id="<%=DefineReport.Grid.TENCDIINPUT.getObj()%><%=page_suffix%>" data-options="singleSelect:true,rownumbers:true, fit:true"></div>
						<div class="ref_editor" style="display: none;">
							<input  class="easyui-numberbox_" tabindex="-1" id="<%=DefineReport.InpText.TENCD.getObj()%><%=page_suffix%>" check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' />
						</div>
					</div>
					<div data-options="region:'south',border:false" style="display:none;height:50px;padding:5px 5px 0;">
						<a href="#" class="easyui-linkbutton" tabindex="1009" id="<%=DefineReport.Button.SET.getObj()%><%=page_suffix%>" title="<%=DefineReport.Button.SET.getTxt()%>" iconCls="icon-edit" style="width:110px;"><span class="btnTxt2"><%=DefineReport.Button.SET.getTxt()%></span></a>
					</div>
				</div>
			</fieldset>
		</div>
		</form>
		<div data-options="region:'south',border:false" style="display:none;height:30px;padding:2px 5px 0;">
			<div class="btn">
				<table class="t-layout3">
				<tr>
					<th style="min-width:110px;"></th><th style="min-width:10px;"></th>
					<th style="min-width:110px;"></th><th style="min-width:110px;"></th><th style="min-width:110px;"></th><th style="min-width:10px;"></th>
					<th style="min-width:110px;"></th><th style="min-width:110px;"></th>
				</tr>
				<tr>
					<td><a href="#" class="easyui-linkbutton" tabindex="1501" id="<%=DefineReport.Button.BACK.getObj()%><%=page_suffix%>" title="<%=DefineReport.Button.BACK.getTxt()%>" iconCls="icon-undo" style="width:100px;"><span><%=DefineReport.Button.BACK.getTxt()%></span></a></td>
					<td></td>
					<td><a href="#" class="easyui-linkbutton" tabindex="1502" id="<%=DefineReport.Button.CANCEL.getObj()%><%=page_suffix%>" title="<%=DefineReport.Button.CANCEL.getTxt()%>" iconCls="icon-cancel" style="width:100px;"><%=DefineReport.Button.CANCEL.getTxt()%></a></td>
					<td><a href="#" class="easyui-linkbutton" tabindex="1503" id="<%=DefineReport.Button.UPD.getObj()%><%=page_suffix%>" title="<%=DefineReport.Button.UPD.getTxt()%>" iconCls="icon-save" style="width:100px;"><span class="btnTxt"><%=DefineReport.Button.UPD.getTxt()%></span></a></td>
					<td><a href="#" class="easyui-linkbutton" tabindex="1504" id="<%=DefineReport.Button.CSV_IMPORT.getObj()%><%=page_suffix%>" title="<%=DefineReport.Button.CSV_IMPORT.getTxt()%>" iconCls="icon-save" style="width:100px;"><span class="btnTxt"><%=DefineReport.Button.CSV_IMPORT.getTxt()%></span></a></td>
					<td></td>
					<td><a href="#" class="easyui-linkbutton" tabindex="1505" id="<%=DefineReport.Button.STOP.getObj()%><%=page_suffix%>"  title="全停止" iconCls="icon-no" style="width:100px;"><span class="btnTxt">全停止</span></a></td>
					<td><a href="#" class="easyui-linkbutton" tabindex="1506" id="<%=DefineReport.Button.START.getObj()%><%=page_suffix%>" title="全実施" iconCls="icon-ok" style="width:100px;"><span class="btnTxt">全実施</span></a></td>
				</tr>
				</table>
			</div>
		</div>
	</div>
	<input type="hidden" name="<%=DefineReport.Hidden.CHANGED_IDX.getObj()%><%=page_suffix%>" id="<%=DefineReport.Hidden.CHANGED_IDX.getObj()%><%=page_suffix%>" />
</div>
<iframe name="if<%=page_suffix%>" id="if<%=page_suffix%>" frameborder="0" border="0" width="0" height="0" scrolling="yes"></iframe>
