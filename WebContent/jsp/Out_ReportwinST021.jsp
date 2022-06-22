<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8"%>
<%@ page import="common.DefineReport" %>
<%
	String page_suffix = "_winST021";
%>
<div id="<%=page_suffix%>" title="" style="width:945px;height:450px;position: relative;display:none;">
	<div id="el<%=page_suffix%>" class="easyui-layout" data-options="fit:true" style="pading:0;">
		<div data-options="region:'north',border:false" style="height:75px;width:100%;padding:2px 5px 0;">
			<div class="Search">
				<table class="dataTable" cellspacing="0" cellpadding="0" style="display:inline;">
				<tbody>
				<tr style="height:13px">
					<td class="labelCell" style="width: 35px;height:10px;line-height:6px;font-size: 10px;">週No.</td>
					<td class="labelCell" style="width: 90px;height:10px;line-height:6px;font-size: 10px;">催しコード</td>
					<td class="labelCell" style="width:300px;height:10px;line-height:6px;font-size: 10px;">催し名称</td>
					<td class="col_empty"></td>
					<td class="labelCell" style="width: 90px;height:10px;line-height:6px;font-size: 10px;">納入日</td>
					<td class="col_empty"></td>
					<td class="labelCell" style="width: 90px;height:10px;line-height:6px;font-size: 10px;">発注数</td>
					<td class="labelCell" style="width: 90px;height:10px;line-height:6px;font-size: 10px;">今回発注数</td>
					<td class="labelCell" style="width: 90px;height:10px;line-height:6px;font-size: 10px;">数量差</td>
				</tr>
				<tr>
					<td class="col_lbl_c" style="height:10px;line-height:6px;"><span id="<%=DefineReport.InpText.SHUNO.getObj()%><%=page_suffix%>" style="font-size: 10px;">　</span></td>
					<td class="col_lbl_c" style="height:10px;line-height:6px;"><span id="<%=DefineReport.InpText.MOYSCD.getObj()%><%=page_suffix%>" style="font-size: 10px;"></span></td>
					<td class="col_lbl" style="height:10px;line-height:6px;"><span id="<%=DefineReport.InpText.MOYKN.getObj()%><%=page_suffix%>" style="font-size: 10px;"></span></td>
					<td class="col_empty"></td>
					<td class="col_lbl_c" style="height:10px;line-height:6px;"><span id="<%=DefineReport.InpText.NNDT.getObj()%><%=page_suffix%>" style="font-size: 10px;"></span></td>
					<td class="col_empty"></td>
					<td class="col_lbl_r" style="height:10px;line-height:6px;"><span id="<%=DefineReport.InpText.SURYO.getObj()%>1<%=page_suffix%>" style="font-size: 10px;"></span></td>
					<td class="col_lbl_r" style="height:10px;line-height:6px;"><span id="<%=DefineReport.InpText.SURYO.getObj()%>2<%=page_suffix%>" style="font-size: 10px;"></span></td>
					<td class="col_lbl_r" style="height:10px;line-height:6px;"><span id="<%=DefineReport.InpText.SURYO.getObj()%>3<%=page_suffix%>" style="font-size: 10px;"></span></td>
				</tr>
				</tbody></table>
				<table class="dataTable" cellspacing="0" cellpadding="0" style="display:inline;">
				<tbody>
				<tr>
					<td class="labelCell" style="width:35px;height:10px;line-height:6px;font-size: 10px;">部門</td>
					<td class="labelCell" style="width:90px;height:10px;line-height:6px;font-size: 10px;">BY</td>
					<td class="col_empty"></td>
					<td class="labelCell" style="width:80px;height:10px;line-height:6px;font-size: 10px;">商品コード</td>
					<td class="labelCell" style="width:275px;height:10px;line-height:6px;font-size: 10px;">商品マスタ名称</td>
				</tr>
				<tr>
					<td class="col_lbl" style="height:10px;line-height:6px;"><span id="<%=DefineReport.InpText.BMNCD.getObj()%><%=page_suffix%>" style="font-size: 10px;"></span></td>
					<td class="col_lbl" style="height:10px;line-height:6px;"><span id="<%=DefineReport.Select.BYCD.getObj()%><%=page_suffix%>" style="font-size: 10px;"></span></td>
					<td class="col_empty"></td>
					<td class="col_lbl" style="height:10px;line-height:6px;"><span id="<%=DefineReport.InpText.SHNCD.getObj()%><%=page_suffix%>" style="font-size: 10px;"></span></td>
					<td class="col_lbl" style="height:10px;line-height:6px;"><span id="<%=DefineReport.InpText.SHNKN.getObj()%><%=page_suffix%>" style="font-size: 10px;"></span></td>
				</tr>
				</tbody></table>
				<table class="dataTable" cellspacing="0" cellpadding="0" style="display:hidden;">
				<tbody>
				<tr>
					<td class="labelCell" style="width: 50px;height:10px;line-height:6px;font-size: 10px;">ｸﾞﾙｰﾌﾟNo.</td>
					<td class="labelCell" style="width: 30px;height:10px;line-height:6px;font-size: 10px;">子No.</td>
					<td class="labelCell" style="width: 30px;height:10px;line-height:6px;font-size: 10px;">日替</td>
					<td class="col_empty"></td>
					<td class="labelCell" style="width:185px;height:10px;line-height:6px;font-size: 10px;">販売期間</td>
					<td class="labelCell" style="width:185px;height:10px;line-height:6px;font-size: 10px;">納入期間</td>
					<td class="labelCell" style="width: 60px;height:10px;line-height:6px;font-size: 10px;">チラシ未掲載</td>
				</tr>
				<tr>
					<td class="col_lbl" style="height:10px;line-height:6px;"><span id="<%=DefineReport.InpText.PARNO.getObj()%><%=page_suffix%>" style="font-size: 10px;"></span></td>
					<td class="col_lbl" style="height:10px;line-height:6px;"><span id="<%=DefineReport.InpText.CHLDNO.getObj()%><%=page_suffix%>" style="font-size: 10px;"></span></td>
					<td class="col_chk txt_chk" style="height:10px;line-height:6px;"><span id="<%=DefineReport.Checkbox.HIGAWRFLG.getObj()%><%=page_suffix%>" style="font-size: 10px;"></span></td>
					<td class="col_empty"></td>
					<td class="col_lbl" style="height:10px;line-height:6px;"><span id="<%=DefineReport.InpText.HBSTDT.getObj()%><%=page_suffix%>" style="font-size: 10px;"></span>～<span id="<%=DefineReport.InpText.HBEDDT.getObj()%><%=page_suffix%>" style="font-size: 10px;"></span></td>
					<td class="col_lbl" style="height:10px;line-height:6px;"><span id="<%=DefineReport.InpText.NNSTDT.getObj()%><%=page_suffix%>" style="font-size: 10px;"></span>～<span id="<%=DefineReport.InpText.NNEDDT.getObj()%><%=page_suffix%>" style="font-size: 10px;"></span></td>
					<td class="col_chk txt_chk" style="height:10px;line-height:6px;"><span id="<%=DefineReport.Checkbox.CHIRASFLG.getObj()%><%=page_suffix%>" style="font-size: 10px;"></span></td>
				</tr>
				</tbody>
				</table>
			</div>
		</div>
		<div data-options="region:'center',border:false" style="display:none;top: 90px;">
			<form id="gf<%=page_suffix%>" class="e_grid">
			<table class="like_datagrid">
			<thead class="no_header">
			<tr>
				<%for(int i = 0; i< 15; i++){%>
				<th></th>
				<%}%>
			</tr>
			</thead>
			<tbody>
			<%
			int idx = 0;
			for(int j = 0; j< 54; j++){
			%>
			<tr style="height:13px">
				<%
				if(j%2==0){
					for(int i = 0; i< 15; i++){
				%>
					<td class="col_lbl" style="height:10px;line-height:6px"><span col="HTASU<%=j%>_<%=i%>" style="font-size:10px;"></span></td>
				<%
					}
				}else{
					for(int i = 0; i< 15; i++){
						idx++;
				%>
					<td class="col_num" style="padding: 0 1px;height:3px">
					<input class="easyui-numberbox_" tabindex="<%=1000+idx%>" col="HTASU<%=j%>_<%=i%>" id="<%=DefineReport.InpText.HTASU.getObj()%><%=page_suffix%>_<%=idx%>" check='<%=DefineReport.InpText.HTASU.getMaxlenTag()%>' style="width:58px;height: 16px;">
					</td>
				<%
					}
				}
				%>
			</tr>
			<%}%>
			</tbody>
			</table>

<%-- 			<div class="easyui-datagrid placeFace" tabindex="1001" id="<%=DefineReport.Grid.TENBETUSU.getObj()%><%=page_suffix%>"></div> --%>
			</form>
		</div>
		<div data-options="region:'south',border:false" style="display:none;height:30px;padding:2px 5px 0;">
			<div class="btn">
				<table class="t-layout3">
				<tr>
					<th style="min-width:115px;"></th><th style="min-width:10px;"></th>
					<th style="min-width:115px;"></th><th style="min-width:115px;"></th>
				</tr>
				<tr>
					<td><a href="#" class="easyui-linkbutton" tabindex="1501" id="<%=DefineReport.Button.BACK.getObj()%><%=page_suffix%>" title="<%=DefineReport.Button.BACK.getTxt()%>" iconCls="icon-undo" style="width:100px;"><span><%=DefineReport.Button.BACK.getTxt()%></span></a></td>
					<td></td>
					<td><a href="#" class="easyui-linkbutton" tabindex="1502" id="<%=DefineReport.Button.CANCEL.getObj()%><%=page_suffix%>" title="<%=DefineReport.Button.CANCEL.getTxt()%>" iconCls="icon-cancel" style="width:100px;"><%=DefineReport.Button.CANCEL.getTxt()%></a></td>
					<td><a href="#" class="easyui-linkbutton" tabindex="1503" id="<%=DefineReport.Button.UPD.getObj()%><%=page_suffix%>" title="<%=DefineReport.Button.UPD.getTxt()%>" iconCls="icon-save" style="width:100px;"><span class="btnTxt"><%=DefineReport.Button.UPD.getTxt()%></span></a></td>
				</tr>
				</table>
			</div>
		</div>
	</div>
	<input type="hidden" name="<%=DefineReport.Hidden.CHANGED_IDX.getObj()%><%=page_suffix%>" id="<%=DefineReport.Hidden.CHANGED_IDX.getObj()%><%=page_suffix%>" />
	<input type="hidden" name="<%=DefineReport.Hidden.CHANGED_IDX.getObj()%><%=page_suffix%>" id="<%=DefineReport.Hidden.CHANGED_IDX.getObj()%><%=page_suffix%>_upd" />
</div>
