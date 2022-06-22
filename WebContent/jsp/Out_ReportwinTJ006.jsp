<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8"%>
<%@ page import="common.DefineReport" %>
<%
	String page_suffix = "_winTJ006";
%>
<div id="<%=page_suffix%>" title="" style="width:1100px;height:550px;position: relative;display:none;">
	<div id="el<%=page_suffix%>" class="easyui-layout" data-options="fit:true" style="pading:0;">
		<div data-options="region:'north',border:false" style="height:80px;width:100%;padding:2px 100px 0;">
			<table id = "dTable1"  class="like_datagrid" data-options="rownumbers:true,width:598," style="float:left;">
				<thead class="no_header">
					<tr>
						<th style="max-width: 40px"></th>
						<th style="max-width: 40px"></th>
						<th style="max-width: 40px"></th>
						<th style="max-width: 40px"></th>
						<th style="max-width: 40px"></th>
					</tr>
				</thead>
				<tbody>
					<tr>
						<td class="col_tit" rowspan="1" style="width:140px;height:5px;">日付</td>
						<td class="col_lbl_c" style="text-align:center; width:49px;height:5px;"><span col="F1"></span></td>
						<td class="col_lbl_c" style="text-align:center; width:49px;height:5px;"><span col="F2"></span></td>
						<td class="col_lbl_c" style="text-align:center; width:49px;height:5px;"><span col="F3"></span></td>
						<td class="col_lbl_c" style="text-align:center; width:49px;height:5px;"><span col="F4"></span></td>
						<td class="col_lbl_c" style="text-align:center; width:49px;height:5px;"><span col="F5"></span></td>
						<td class="col_lbl_c" style="text-align:center; width:49px;height:5px;"><span col="F6"></span></td>
						<td class="col_lbl_c" style="text-align:center; width:49px;height:5px;"><span col="F7"></span></td>
						<td class="col_lbl_c" style="text-align:center; width:49px;height:5px;"><span col="F8"></span></td>
						<td class="col_lbl_c" style="text-align:center; width:49px;height:5px;"><span col="F9"></span></td>
						<td class="col_lbl_c" style="text-align:center; width:49px;height:5px;"><span col="F10"></span></td>
					</tr>
					<tr>
						<td class="col_tit" rowspan="1" style="width:140px;height:5px;">曜日</td>
						<td class="col_lbl_c" style="text-align:center; width:49px;height:5px;"><span col="F11"></span></td>
						<td class="col_lbl_c" style="text-align:center; width:49px;height:5px;"><span col="F12"></span></td>
						<td class="col_lbl_c" style="text-align:center; width:49px;height:5px;"><span col="F13"></span></td>
						<td class="col_lbl_c" style="text-align:center; width:49px;height:5px;"><span col="F14"></span></td>
						<td class="col_lbl_c" style="text-align:center; width:49px;height:5px;"><span col="F15"></span></td>
						<td class="col_lbl_c" style="text-align:center; width:49px;height:5px;"><span col="F16"></span></td>
						<td class="col_lbl_c" style="text-align:center; width:49px;height:5px;"><span col="F17"></span></td>
						<td class="col_lbl_c" style="text-align:center; width:49px;height:5px;"><span col="F18"></span></td>
						<td class="col_lbl_c" style="text-align:center; width:49px;height:5px;"><span col="F19"></span></td>
						<td class="col_lbl_c" style="text-align:center; width:49px;height:5px;"><span col="F20"></span></td>
					</tr>
					<tr>
						<td class="col_tit" rowspan="1" style="width:140px;height:5px;">部門予算</td>
						<td class="col_lbl_c">
							<input class="easyui-numberbox_" tabindex="100" col="F24" id="<%=DefineReport.InpText.BMNYSANAM.getObj()%>1" check='<%=DefineReport.InpText.BMNYSANAM.getMaxlenTag()%>' data-options="required:true" style="width:70px; text-align:right">
						</td>
						<td class="col_lbl_c" style="text-align:center; width:49px;height:5px;">
							<input class="easyui-numberbox_" tabindex="101" col="F25" id="<%=DefineReport.InpText.BMNYSANAM.getObj()%>2" check='<%=DefineReport.InpText.BMNYSANAM.getMaxlenTag()%>' data-options="required:true" style="width:70px; text-align:right">
						</td>
						<td class="col_lbl_c" style="text-align:center; width:49px;height:5px;">
							<input class="easyui-numberbox_" tabindex="102" col="F26" id="<%=DefineReport.InpText.BMNYSANAM.getObj()%>3" check='<%=DefineReport.InpText.BMNYSANAM.getMaxlenTag()%>' data-options="required:true" style="width:70px; text-align:right">
						</td>
						<td class="col_lbl_c" style="text-align:center; width:49px;height:5px;">
							<input class="easyui-numberbox_" tabindex="103" col="F27" id="<%=DefineReport.InpText.BMNYSANAM.getObj()%>4" check='<%=DefineReport.InpText.BMNYSANAM.getMaxlenTag()%>' data-options="required:true" style="width:70px; text-align:right">
						</td>
						<td class="col_lbl_c" style="text-align:center; width:49px;height:5px;">
							<input class="easyui-numberbox_" tabindex="104" col="F28" id="<%=DefineReport.InpText.BMNYSANAM.getObj()%>5" check='<%=DefineReport.InpText.BMNYSANAM.getMaxlenTag()%>' data-options="required:true" style="width:70px; text-align:right">
						</td>
						<td class="col_lbl_c" style="text-align:center; width:49px;height:5px;">
							<input class="easyui-numberbox_" tabindex="105" col="F29" id="<%=DefineReport.InpText.BMNYSANAM.getObj()%>6" check='<%=DefineReport.InpText.BMNYSANAM.getMaxlenTag()%>' data-options="required:true" style="width:70px; text-align:right">
						</td>
						<td class="col_lbl_c" style="text-align:center; width:49px;height:5px;">
							<input class="easyui-numberbox_" tabindex="106" col="F30" id="<%=DefineReport.InpText.BMNYSANAM.getObj()%>7" check='<%=DefineReport.InpText.BMNYSANAM.getMaxlenTag()%>' data-options="required:true" style="width:70px; text-align:right">
						</td>
						<td class="col_lbl_c" style="text-align:center; width:49px;height:5px;">
							<input class="easyui-numberbox_" tabindex="107" col="F31" id="<%=DefineReport.InpText.BMNYSANAM.getObj()%>8" check='<%=DefineReport.InpText.BMNYSANAM.getMaxlenTag()%>' data-options="required:true" style="width:70px; text-align:right">
						</td>
						<td class="col_lbl_c" style="text-align:center; width:49px;height:5px;">
							<input class="easyui-numberbox_" tabindex="108" col="F32" id="<%=DefineReport.InpText.BMNYSANAM.getObj()%>9" check='<%=DefineReport.InpText.BMNYSANAM.getMaxlenTag()%>' data-options="required:true" style="width:70px; text-align:right">
						</td>
						<td class="col_lbl_c" style="text-align:center; width:49px;height:5px;">
							<input class="easyui-numberbox_" tabindex="109" col="F33" id="<%=DefineReport.InpText.BMNYSANAM.getObj()%>10" check='<%=DefineReport.InpText.BMNYSANAM.getMaxlenTag()%>' data-options="required:true" style="width:70px; text-align:right">
						</td>
					</tr>
				</tbody>
			</table>
		</div>

  		<div data-options="region:'center',border:false" style="display:none;padding:0 100px 0;">
			<!-- EasyUI方式 -->

			<div style="display: flex; justify-content: space-between; width:970px;">
				<div>大分類売上構成比</div>
				<div><span col="MSG"></span></div>
				<div>平均パック単価</div>
			</div>

			<div style="height:360px;width:970px">
				<div tabindex="1005" id="<%=DefineReport.Grid.KOUSEIHI.getObj()%><%=page_suffix%>" class="easyui-datagrid placeFace" ></div>
				<div class="ref_editor" style="display: none;">
					<input class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.KOUSEIHI.getObj()%>1"  check='<%=DefineReport.InpText.KOUSEIHI.getMaxlenTag()%>' data-options="min:0,max:100">
					<input class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.KOUSEIHI.getObj()%>2"  check='<%=DefineReport.InpText.KOUSEIHI.getMaxlenTag()%>' data-options="min:0,max:100">
					<input class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.KOUSEIHI.getObj()%>3"  check='<%=DefineReport.InpText.KOUSEIHI.getMaxlenTag()%>' data-options="min:0,max:100">
					<input class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.KOUSEIHI.getObj()%>4"  check='<%=DefineReport.InpText.KOUSEIHI.getMaxlenTag()%>' data-options="min:0,max:100">
					<input class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.KOUSEIHI.getObj()%>5"  check='<%=DefineReport.InpText.KOUSEIHI.getMaxlenTag()%>' data-options="min:0,max:100">
					<input class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.KOUSEIHI.getObj()%>6"  check='<%=DefineReport.InpText.KOUSEIHI.getMaxlenTag()%>' data-options="min:0,max:100">
					<input class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.KOUSEIHI.getObj()%>7"  check='<%=DefineReport.InpText.KOUSEIHI.getMaxlenTag()%>' data-options="min:0,max:100">
					<input class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.KOUSEIHI.getObj()%>8"  check='<%=DefineReport.InpText.KOUSEIHI.getMaxlenTag()%>' data-options="min:0,max:100">
					<input class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.KOUSEIHI.getObj()%>9"  check='<%=DefineReport.InpText.KOUSEIHI.getMaxlenTag()%>' data-options="min:0,max:100">
					<input class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.KOUSEIHI.getObj()%>10"  check='<%=DefineReport.InpText.KOUSEIHI.getMaxlenTag()%>' data-options="min:0,max:100">
				</div>
			</div>
		</div>

		<div data-options="region:'south',border:false" style="display:none;height:50px;padding:2px 5px 0;">
			<div class="btn" style="text-align: center;">
				<a href="#" class="easyui-linkbutton" tabindex="1006" title="<%=DefineReport.Button.CANCEL.getTxt()%>" id="<%=DefineReport.Button.CANCEL.getObj()%><%=page_suffix%>" iconCls="icon-cancel" style="width:100px;"><span><%=DefineReport.Button.CANCEL.getTxt()%></span></a>
				<a href="#" class="easyui-linkbutton" tabindex="1007" title="<%=DefineReport.Button.UPD.getTxt()%>" id="<%=DefineReport.Button.UPD.getObj()%><%=page_suffix%>" iconCls="icon-save" style="width:110px;"><span><%=DefineReport.Button.UPD.getTxt()%></span></a>
			</div>
			<div id="disp_record_info" style="float: right;">
				<span class="labelName" tabindex="-1" style="padding-top: 3px;">
						登録日 <span col="F21" id="<%=DefineReport.Text.ADDDT.getObj()%><%=page_suffix%>"></span>　更新日 <span col="F22" id="<%=DefineReport.Text.UPDDT.getObj()%><%=page_suffix%>"></span>　オペレータ <span col="F23" id="<%=DefineReport.Text.OPERATOR.getObj()%><%=page_suffix%>"></span>
				</span>
			</div>
		</div>
	</div>
	<input type="hidden" name="<%=DefineReport.Hidden.CHANGED_IDX.getObj()%><%=page_suffix%>" id="<%=DefineReport.Hidden.CHANGED_IDX.getObj()%><%=page_suffix%>" />
</div>