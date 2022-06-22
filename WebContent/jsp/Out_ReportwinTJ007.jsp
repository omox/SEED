<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8"%>
<%@ page import="common.DefineReport" %>
<%
	String page_suffix = "_winTJ007";
%>
<div id="<%=page_suffix%>" title="" style="width:855px;height:450px;position: relative;display:none;">
	<div id="el<%=page_suffix%>" class="easyui-layout" data-options="fit:true" style="pading:0;">
		<div data-options="region:'north',border:false" style="height:55px;width:800px;padding-top:2px; padding-left:65px;">
			<table id = "dTable1"  class="like_datagrid" data-options="rownumbers:true,width:598," style="float:left;">
				<thead class="no_header">
					<tr>
						<th style="max-width: 40px"></th>
						<th style="max-width: 40px"></th>
						<th style="max-width: 40px"></th>
						<th style="max-width: 40px"></th>
						<th style="max-width: 40px"></th>
						<th style="max-width: 40px"></th>
						<th style="max-width: 40px"></th>
						<th style="max-width: 40px"></th>
						<th style="max-width: 40px"></th>
						<th style="max-width: 40px"></th>
						<th style="max-width: 40px"></th>
					</tr>
				</thead>
				<tbody>
					<tr>
						<td class="col_tit" rowspan="1" style="width:51px;height:5px;">日付</td>
						<td class="col_lbl_c" style="text-align:center; width:51px;height:5px;"><span col="F1"></span></td>
						<td class="col_lbl_c" style="text-align:center; width:51px;height:5px;"><span col="F2"></span></td>
						<td class="col_lbl_c" style="text-align:center; width:51px;height:5px;"><span col="F3"></span></td>
						<td class="col_lbl_c" style="text-align:center; width:51px;height:5px;"><span col="F4"></span></td>
						<td class="col_lbl_c" style="text-align:center; width:51px;height:5px;"><span col="F5"></span></td>
						<td class="col_lbl_c" style="text-align:center; width:51px;height:5px;"><span col="F6"></span></td>
						<td class="col_lbl_c" style="text-align:center; width:51px;height:5px;"><span col="F7"></span></td>
						<td class="col_lbl_c" style="text-align:center; width:51px;height:5px;"><span col="F8"></span></td>
						<td class="col_lbl_c" style="text-align:center; width:51px;height:5px;"><span col="F9"></span></td>
						<td class="col_lbl_c" style="text-align:center; width:51px;height:5px;"><span col="F10"></span></td>
					</tr>
					<tr>
						<td class="col_tit" rowspan="1" style="width:51px;height:5px;">曜日</td>
						<td class="col_lbl_c" style="text-align:center; width:51px;height:5px;"><span col="F11"></span></td>
						<td class="col_lbl_c" style="text-align:center; width:51px;height:5px;"><span col="F12"></span></td>
						<td class="col_lbl_c" style="text-align:center; width:51px;height:5px;"><span col="F13"></span></td>
						<td class="col_lbl_c" style="text-align:center; width:51px;height:5px;"><span col="F14"></span></td>
						<td class="col_lbl_c" style="text-align:center; width:51px;height:5px;"><span col="F15"></span></td>
						<td class="col_lbl_c" style="text-align:center; width:51px;height:5px;"><span col="F16"></span></td>
						<td class="col_lbl_c" style="text-align:center; width:51px;height:5px;"><span col="F17"></span></td>
						<td class="col_lbl_c" style="text-align:center; width:51px;height:5px;"><span col="F18"></span></td>
						<td class="col_lbl_c" style="text-align:center; width:51px;height:5px;"><span col="F19"></span></td>
						<td class="col_lbl_c" style="text-align:center; width:51px;height:5px;"><span col="F20"></span></td>
					</tr>
				</tbody>
			</table>
			<div style="padding-top:35px;">　　　　　発注数計</div>
		</div>
		<div data-options="region:'center',border:false" style="display:none;">
			<!-- EasyUI方式 -->
			<div style="display: flex; justify-content: space-between; width:836px;">
				<div>大分類別明細</div>
				<div>　期間計　　予定数比</div>
			</div>
			<div style="height:320px">
				<div tabindex="11" id="<%=DefineReport.Grid.SEL_VIEW.getObj()%><%=page_suffix%>" class="easyui-datagrid placeFace" ></div>
			</div>
			<div style="height:79px">
				<div tabindex="12" id="<%=DefineReport.Grid.BUMONYOSAN_SUB.getObj()%><%=page_suffix%>" class="easyui-datagrid placeFace" ></div>
			</div>
		</div>
		<div data-options="region:'south',border:false" style="display:none;height:30px;padding:2px 5px 0;">
			<div class="btn" style="text-align: center;">
				<a href="#" class="easyui-linkbutton" tabindex="1006" title="<%=DefineReport.Button.CANCEL.getTxt()%>" id="<%=DefineReport.Button.CANCEL.getObj()%><%=page_suffix%>" iconCls="icon-cancel" style="width:100px;"><span><%=DefineReport.Button.CANCEL.getTxt()%></span></a>
			</div>
		</div>
	</div>
</div>