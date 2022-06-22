<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8"%>
<%@ page import="common.DefineReport" %>
<%
	String page_suffix = "_winTJ010";
%>
<div id="<%=page_suffix%>" title="<%=DefineReport.Button.KEIKAKU.getTxt()%>" style="width:500px;height:250px;position: relative;display:none;">
	<div id="el<%=page_suffix%>" class="easyui-layout" data-options="fit:true" style="pading:0;">
		<div data-options="region:'north',border:false" style="height:200px;width:100%;padding:2px 5px 0;">
			<div class="Search">
				<table   class="like_datagrid" data-options="rownumbers:true,width:410," style="float:left;margin-left:10px;margin-bottom:10px">
					<thead class="no_header">
						<tr>
							<th style="max-width: 40px"></th>
							<th style="max-width: 40px"></th>
						</tr>
					</thead>
					<tbody>
						<tr>
							<td class="col_tit" style="width:64px;">　</td>
							<td class="col_tit" style="width:64px;">売価計</td>
							<td class="col_tit" style="width:64px;">売上構成比</td>
							<td class="col_tit" style="width:64px;">荒利額</td>
							<td class="col_tit" style="width:64px;">荒利構成比</td>
						</tr>
						<tr>
							<td class="col_tit">予算</td>
							<td class="col_lbl_c" style="text-align:right"><span id="txt_yosankei1"  ></span></td>
							<td class="col_lbl_c" style="text-align:right"><span ></span></td>
							<td class="col_lbl_c" style="text-align:right"><span id="txt_yosankei2"  ></span></td>
							<td class="col_lbl_c" style="text-align:right"><span ></span></td>
						</tr>
						<tr>
							<td class="col_tit">特売</td>
							<td class="col_lbl_a" style="text-align:right"><span id="txt_tokubai1"  ></span></td>
							<td class="col_lbl_a" style="text-align:right"><span id="txt_tokubai2"  ></span>%</td>
							<td class="col_lbl_a" style="text-align:right"><span id="txt_tokubai3"  ></span></td>
							<td class="col_lbl_a" style="text-align:right"><span id="txt_tokubai4"  ></span>%</td>
						</tr>
						<tr>
							<td class="col_tit">山積み</td>
							<td class="col_lbl_a" style="text-align:right"><span id="txt_yamadumi1"  ></span></td>
							<td class="col_lbl_a" style="text-align:right"><span id="txt_yamadumi2"  ></span>%</td>
							<td class="col_lbl_a" style="text-align:right"><span id="txt_yamadumi3"  ></span></td>
							<td class="col_lbl_a" style="text-align:right"><span id="txt_yamadumi4"  ></span>%</td>
						</tr>
						<tr>
							<td class="col_tit">レギュラー</td>
							<td class="col_lbl_a" style="text-align:right"><span id="txt_regular1"  ></span></td>
							<td class="col_lbl_a" style="text-align:right"><span id="txt_regular2"  ></span>%</td>
							<td class="col_lbl_a" style="text-align:right"><span id="txt_regular3"  ></span></td>
							<td class="col_lbl_a" style="text-align:right"><span id="txt_regular4"  ></span>%</td>
						</tr>
						<tr>
							<td class="col_tit">過不足</td>
							<td class="col_lbl_a" style="text-align:right"><span id="txt_kabusoku1"  ></span></td>
							<td class="col_lbl_a" style="text-align:right"><span id="txt_kabusoku2"  ></span>%</td>
							<td class="col_lbl_a" style="text-align:right"><span id="txt_kabusoku3"  ></span></td>
							<td class="col_lbl_a" style="text-align:right"><span id="txt_kabusoku4"  ></span>%</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>
		<div data-options="region:'south',border:false" style="display:none;height:30px;padding:2px 5px 0;">
			<div class="btn" style="text-align: center;">
				<a href="#" class="easyui-linkbutton" tabindex="1004" title="<%=DefineReport.Button.CANCEL.getTxt()%>" id="<%=DefineReport.Button.CANCEL.getObj()%><%=page_suffix%>" iconCls="icon-cancel" style="width:120px;"><span><%=DefineReport.Button.CANCEL.getTxt()%></span></a>
			</div>
		</div>
	</div>
</div>
