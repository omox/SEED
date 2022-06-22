<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8"%>
<%@ page import="common.DefineReport" %>
<%
	String page_suffix2 = "_winST010";
%>
<div id="<%=page_suffix2%>" title="<%=DefineReport.Button.RINZIRANK.getTxt()%>" style="width:600px;height:450px;position: relative;display:none;">
	<div id="el<%=page_suffix2%>" class="easyui-layout" data-options="fit:true" style="pading:0;">
		<div data-options="region:'north',border:false" style="height:70px;width:100%;padding:2px 5px 0;">
			<div class="Search">
				<table class="t-layout1">
				<tr>
					<td>
						<input class="easyui-numberbox" tabindex="1" id="<%=DefineReport.InpText.BMNCD.getObj()%><%=page_suffix2%>"" check='<%=DefineReport.InpText.BMNCD.getMaxlenTag()%>' data-options="label:'部門',labelWidth:30" style="width:100px;" value="" disabled="disabled">
					</td>
					<td>
						<label class="chk_lbl" for="chk_rinji">臨時<input type="checkbox" tabindex="2" id="<%=DefineReport.Checkbox.RINJI.getObj()%><%=page_suffix2%>" value="1" disabled="disabled" /></label>
					</td>
					<td>
						<input class="easyui-textbox" tabindex="3" id="<%=DefineReport.InpText.MOYSCD.getObj()%><%=page_suffix2%>"" check='<%=DefineReport.InpText.MOYSCD.getMaxlenTag()%>' data-options="label:'催しコード',labelWidth:65" style="width:150px;" value="" disabled="disabled">
					</td>
					<td class="col_btn">
					<a href="#" title="検索" id="<%=DefineReport.Button.SEARCH.getObj()%><%=page_suffix2%>" class="easyui-linkbutton" tabindex="4" iconCls="icon-search"><span class="btnTxt">検索</span></a>
					</td>
				</tr>
				</table>
			</div>
		</div>
		<div data-options="region:'center',border:false" style="display:none;">
			<div class="easyui-datagrid placeFace" tabindex="1005" id="<%=DefineReport.Grid.RINZIRANKNO.getObj()%><%=page_suffix2%>"></div>
		</div>
		<div data-options="region:'south',border:false" style="display:none;height:40px;padding:2px 5px 0;">
			<div class="btn" style="text-align: center;">
			<table class="t-layout3">
			<tr>
				<td><a href="#" class="easyui-linkbutton" tabindex="1006" title="<%=DefineReport.Button.CANCEL.getTxt()%>" id="<%=DefineReport.Button.CANCEL.getObj()%><%=page_suffix2%>" iconCls="icon-cancel" style="width:100px;"><span><%=DefineReport.Button.CANCEL.getTxt()%></span></a></td>
				<td><a href="#" class="easyui-linkbutton" tabindex="1007" title="<%=DefineReport.Button.NEW.getTxt()%>" id="<%=DefineReport.Button.NEW.getObj()%><%=page_suffix2%>" iconCls="icon-edit" style="width:100px;"><span class="btnTxt"><%=DefineReport.Button.NEW.getTxt()%></span></a></td>
				<td><a href="#" class="easyui-linkbutton" tabindex="1008" title="<%=DefineReport.Button.SEL_CHANGE.getTxt()%>" id="<%=DefineReport.Button.SEL_CHANGE.getObj()%><%=page_suffix2%>" iconCls="icon-edit" style="width:140px;"><span>選択（変更）</span></a></td>
				<td><a href="#" class="easyui-linkbutton" tabindex="1009" title="<%=DefineReport.Button.SEL_REFER.getTxt()%>" id="<%=DefineReport.Button.SEL_REFER.getObj()%><%=page_suffix2%>" iconCls="icon-edit" style="width:140px;"><span><%=DefineReport.Button.SEL_REFER.getTxt()%></span></a></td>
				</tr>
				</table>
			</div>
		</div>
	</div>
</div>
