<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8"%>
<%@ page import="common.DefineReport" %>
<%
	String page_suffix = "_winBM015";
%>
<div id="<%=page_suffix%>" title="<%=DefineReport.Button.TENKAKUNIN.getTxt()%>" style="width:600px;height:450px;position: relative;display:none;">
	<div id="el<%=page_suffix%>" class="easyui-layout" data-options="fit:true" style="pading:0;">
		<div data-options="region:'north',border:false" style="height:50px;width:100%;padding:2px 5px 0;">
			<div class="Search">
			<div id="reference" style="overflow: hidden;width:100%;text-align: right;">
				<TABLE id="dTable1" class="dataTable" cellspacing="0" cellpadding="0" style="display:inline;aline:right">
				<tbody>
				<TR align="left">
					<TD class="labelCell" style="WIDTH: 70px;text-align:center">店舗数</TD>
				</TR>
				<TR align="left">
					<TD><input class="easyui-numberbox" tabindex="-1" col="F1" id="<%=DefineReport.Text.TEN_NUMBER.getObj()%>" data-options="cls:'labelInputNum'" style="width:65px;text-alien:center;" readonly="readonly"></TD>
				</TR>
				</tbody></table>
				</div>
				<input type="hidden" name="<%=DefineReport.InpText.TAISYOTEN.getObj()%>_arr" id="<%=DefineReport.InpText.TAISYOTEN.getObj()%>_arr" />
			</div>
		</div>
		<div data-options="region:'center',border:false" style="display:none;">
			<div class="easyui-datagrid placeFace" tabindex="1005" id="<%=DefineReport.Grid.TENKAKUNIN.getObj()%><%=page_suffix%>"></div>
		</div>
		<div class="ref_editor" style="display: none;">
			<input  class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.TENCD.getObj()%><%=page_suffix%>" check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' />
			<input  class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.AREACD.getObj()%><%=page_suffix%>" check='<%=DefineReport.InpText.AREACD.getMaxlenTag()%>' />
		<!--一部の入力項目については配送パターンにて定義済み  -->
		<!--名称マスタ項目については他の項目にて定義済み  -->
		<!--[実仕入先名称]項目については他の項目にて定義済み  -->
		</div>
		<div data-options="region:'south',border:false" style="display:none;height:30px;padding:2px 5px 0;">
			<div class="btn" style="text-align: center;">
				<a href="#" class="easyui-linkbutton" tabindex="1006" title="<%=DefineReport.Button.CANCEL.getTxt()%>" id="<%=DefineReport.Button.CANCEL.getObj()%><%=page_suffix%>" iconCls="icon-cancel" style="width:100px;"><span><%=DefineReport.Button.CANCEL.getTxt()%></span></a>
			</div>
		</div>
	</div>
</div>
