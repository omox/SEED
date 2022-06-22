<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8"%>
<%@ page import="common.DefineReport" %>
<%
	String page_suffix = "_win006";
%>
<div id="<%=page_suffix%>" title="<%=DefineReport.Button.SHNCD.getTxt()%>" style="width:625px;height:500px;position: relative;display:none;">
	<div id="el<%=page_suffix%>" class="easyui-layout" data-options="fit:true" style="pading:0;">
		<div data-options="region:'north',border:false" style="height:175px;width:100%;padding:2px 5px 0;">
			<div class="Search">
				<table>
				<tr class="inp_hide">
				<td colspan="2">
					<input class="easyui-numberbox" tabindex="1" id="<%=DefineReport.InpText.SHNCD.getObj()%><%=page_suffix%>" check='<%=DefineReport.InpText.SHNCD.getMaxlenTag()%>' style="width:170px;" data-options="prompt:'____-____',label:'商品コード',labelWidth:90">
					<a href="#" class="easyui-linkbutton" tabindex="2" id="<%=DefineReport.Button.SELECT.getObj()%><%=page_suffix%>_shncd" title="<%=DefineReport.Button.SELECT.getTxt()%>"><span class="btnTxt" >選択</span></a>
				</td>
				</tr>
				<tr>
				<td colspan="2">
					<select class="easyui-combobox" tabindex="4" id="<%=DefineReport.Select.BUMON.getObj()%><%=page_suffix%>" data-options="label:'部門',labelWidth:90" style="width:270px;"></select>
				</td>
				</tr>
				<tr>
				<td>
					<select class="easyui-combobox" tabindex="5" id="<%=DefineReport.Select.DAI_BUN.getObj()%><%=page_suffix%>" data-options="label:'大分類',labelWidth:90" style="width:270px;"></select>
				</td>
				<td>
					<select class="easyui-combobox" tabindex="6" id="<%=DefineReport.Select.CHU_BUN.getObj()%><%=page_suffix%>" data-options="label:'中分類',labelWidth:95" style="width:295px;"></select>
				</td>
				</tr>
				<tr>
				<td>
					<input class="easyui-numberbox" tabindex="6" id="<%=DefineReport.InpText.SSIRCD.getObj()%><%=page_suffix%>" check='<%=DefineReport.InpText.SSIRCD.getMaxlenTag()%>' style="width:150px;" for_btn="<%=DefineReport.Button.SIR.getObj()%>_F1" data-options="label:'仕入先コード',labelWidth:90">
					<a href="#" class="easyui-linkbutton" tabindex="7" id="<%=DefineReport.Button.SIR.getObj()%>" title="<%=DefineReport.Button.SIR.getTxt()%>"><span class="btnTxt" >仕入先</span></a>
				</td>
				<td>
					<input class="easyui-numberbox" tabindex="8" id="<%=DefineReport.InpText.MAKERCD.getObj()%><%=page_suffix%>" check='<%=DefineReport.InpText.MAKERCD.getMaxlenTag()%>' style="width:190px;" for_btn="<%=DefineReport.Button.MAKER.getObj()%>_F2" data-options="label:'メーカーコード',labelWidth:95">
					<a href="#" class="easyui-linkbutton" tabindex="9" id="<%=DefineReport.Button.MAKER.getObj()%>" title="<%=DefineReport.Button.MAKER.getTxt()%>" ><span class="btnTxt">メーカー</span></a>
				</td>
				<tr>
				<td colspan="2">
					<select class="easyui-combobox" tabindex="10" id="<%=DefineReport.MeisyoSelect.KBN121.getObj()%><%=page_suffix%>" style="width:210px;" data-options="label:'定貫不定貫区分',labelWidth:90"></select>
					<select class="easyui-combobox" tabindex="11" id="<%=DefineReport.MeisyoSelect.KBN117.getObj()%><%=page_suffix%>" style="width:180px;" data-options="label:'定計区分',labelWidth:60"></select>
					<select class="easyui-combobox" tabindex="12" id="<%=DefineReport.MeisyoSelect.KBN105.getObj()%><%=page_suffix%>" style="width:180px;" data-options="label:'商品種類',labelWidth:60"></select>
				</td>
				</tr>
				<tr vAlign="middle">
				<td colspan="2">
					<input class="easyui-textbox" tabindex="13" id="<%=DefineReport.InpText.SHNKN.getObj()%><%=page_suffix%>" check='<%=DefineReport.InpText.SHNKN.getMaxlenTag()%>' style="width:305px;"data-options="label:'商品名漢字',labelWidth:90">
					<a href="#" class="easyui-linkbutton" tabindex="14" id="<%=DefineReport.Button.SEARCH.getObj()%><%=page_suffix%>" title="<%=DefineReport.Button.SEARCH.getTxt()%>" iconCls="icon-search" style="width:100px;"><span class="btnTxt"><%=DefineReport.Button.SEARCH.getTxt()%></span></a>
				</td>
				</tr>
				</table>
			</div>
		</div>
		<div data-options="region:'center',border:false" style="display:none;">
			<div class="easyui-datagrid placeFace" tabindex="15" id="<%=DefineReport.Grid.SHNCD.getObj()%><%=page_suffix%>"></div>
		</div>
		<div data-options="region:'south',border:false" style="display:none;height:30px;padding:2px 5px 0;">
			<div class="btn" style="text-align: center;">
				<a href="#" class="easyui-linkbutton" tabindex="16" title="<%=DefineReport.Button.CANCEL.getTxt()%>" id="<%=DefineReport.Button.CANCEL.getObj()%><%=page_suffix%>" iconCls="icon-cancel" style="width:100px;"><span><%=DefineReport.Button.CANCEL.getTxt()%></span></a>
				　
				<a href="#" class="easyui-linkbutton" tabindex="17" title="<%=DefineReport.Button.SELECT.getTxt()%>" id="<%=DefineReport.Button.SELECT.getObj()%><%=page_suffix%>" iconCls="icon-edit" style="width:100px;"><span class="btnTxt"><%=DefineReport.Button.SELECT.getTxt()%></span></a>
			</div>
		</div>
	</div>
</div>
