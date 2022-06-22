<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8"%>
<%@ page import="common.DefineReport" %>
<%
	String page_suffix = "_winTG018";
%>
<div id="<%=page_suffix%>" title="" style="width:1070px;height:450px;position: relative;display:none;">
	<div id="el<%=page_suffix%>" class="easyui-layout" data-options="fit:true" style="pading:0;">
		<div data-options="region:'north',border:false" style="height:90px;width:100%;padding:2px 5px 0;">
			<div class="Search">
				<table class="dataTable" cellspacing="0" cellpadding="0" style="display:inline;">
				<tbody>
				<tr>
					<td class="labelCell" style="min-width: 35px;">週No.</td>
					<td class="labelCell" style="min-width: 90px;">催しコード</td>
					<td class="labelCell" style="min-width:300px;">催し名称</td>
					<td class="col_empty"></td>
					<td class="labelCell" style="min-width: 35px;">部門</td>
					<td class="labelCell" style="min-width: 90px;">BY</td>
					<td class="col_empty"></td>
					<td class="col_empty" style="min-width:186px;"></td>
					<td class="labelCell" style="min-width: 90px;" for_kbn="n">納入日</td>
				</tr>
				<tr>
					<td class="col_lbl_l"><span id="<%=DefineReport.InpText.SHUNO.getObj()%><%=page_suffix%>">　</span></td>
					<td class="col_lbl_l"><span id="<%=DefineReport.InpText.MOYSCD.getObj()%><%=page_suffix%>"></span></td>
					<td class="col_lbl"  ><span id="<%=DefineReport.InpText.MOYKN.getObj()%><%=page_suffix%>"></span></td>
					<td class="col_empty"></td>
					<td class="col_lbl_l"><span id="<%=DefineReport.InpText.BMNCD.getObj()%><%=page_suffix%>"></span></td>
					<td class="col_lbl_l"><span id="<%=DefineReport.Select.BYCD.getObj()%><%=page_suffix%>"></span></td>
					<td class="col_empty"></td>
					<td class="col_empty"></td>
					<td class="col_lbl_c" for_kbn="n"><span id="<%=DefineReport.InpText.NNDT.getObj()%><%=page_suffix%>"></span></td>
				</tr>
				</tbody></table>
				<table class="dataTable" cellspacing="0" cellpadding="0" style="display:hidden;">
				<tbody>
				<tr>
					<td class="labelCell" style="min-width:80px;">商品コード</td>
					<td class="labelCell" style="min-width:350px;">商品マスタ名称</td>
					<td class="col_empty"></td>
					<td class="labelCell" style="min-width: 50px;">ｸﾞﾙｰﾌﾟNo.</td>
					<td class="labelCell" style="min-width: 30px;">子No.</td>
					<td class="labelCell" style="min-width: 30px;">日替</td>
					<td class="col_empty"></td>
					<td class="labelCell" style="min-width:185px;">販売期間</td>
					<td class="labelCell" style="min-width:185px;">納入期間</td>
					<td class="labelCell" style="min-width: 60px;">チラシ未掲載</td>
				</tr>
				<tr>
					<td class="col_num" style="text-align: left"><span id="<%=DefineReport.InpText.SHNCD.getObj()%><%=page_suffix%>"></span></td>
					<td class="col_lbl"  ><span id="<%=DefineReport.InpText.SHNKN.getObj()%><%=page_suffix%>"></span></td>
					<td class="col_empty"></td>
					<td class="col_num" style="text-align: left"><span id="<%=DefineReport.InpText.PARNO.getObj()%><%=page_suffix%>"></span></td>
					<td class="col_num" style="text-align: left"><span id="<%=DefineReport.InpText.CHLDNO.getObj()%><%=page_suffix%>"></span></td>
					<td class="col_chk txt_chk"  ><span id="<%=DefineReport.Checkbox.HIGAWRFLG.getObj()%><%=page_suffix%>"></span></td>
					<td class="col_empty"></td>
					<td class="col_num" style="text-align: left"><span id="<%=DefineReport.InpText.HBSTDT.getObj()%><%=page_suffix%>"></span>～<span id="<%=DefineReport.InpText.HBEDDT.getObj()%><%=page_suffix%>"></span></td>
					<td class="col_num" style="text-align: left"><span id="<%=DefineReport.InpText.NNSTDT.getObj()%><%=page_suffix%>"></span>～<span id="<%=DefineReport.InpText.NNEDDT.getObj()%><%=page_suffix%>"></span></td>
					<td class="col_chk txt_chk"  ><span id="<%=DefineReport.Checkbox.CHIRASFLG.getObj()%><%=page_suffix%>"></span></td>
				</tr>
				</tbody>
				</table>
			</div>
		</div>
		<div data-options="region:'center',border:false" style="display:none;">
			<div class="easyui-datagrid placeFace" tabindex="1001" id="<%=DefineReport.Grid.SUB.getObj()%><%=page_suffix%>"></div>
		</div>
		<div data-options="region:'south',border:false" style="display:none;height:30px;padding:2px 5px 0;">
			<div class="btn">
				<a href="#" class="easyui-linkbutton" tabindex="1002" title="<%=DefineReport.Button.BACK.getTxt()%>" id="<%=DefineReport.Button.BACK.getObj()%><%=page_suffix%>" iconCls="icon-undo" style="width:100px;"><span><%=DefineReport.Button.BACK.getTxt()%></span></a>
			</div>
		</div>
	</div>
</div>
