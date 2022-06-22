<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8"%>
<%@ page import="common.DefineReport" %>
<%
	String page_suffix = "_winST020";
%>
<div id="<%=page_suffix%>" title="<%=DefineReport.Button.TENKAI.getTxt()%>" style="width:400px;height:300px;position: relative;display:none;">
	<div id="el<%=page_suffix%>" class="easyui-layout" data-options="fit:true" style="pading:0;">
		<div data-options="region:'center',border:false" style="display:none;padding:2px 5px 0;">
		<table class="t-layout1">
		<tr><td>
			<table class="dataTable" cellspacing="0" cellpadding="0">
			<tbody>
			<tr><td class="labelCell" colspan="3">パターン種類</td></tr>
			<tr>
				<td class="col_lbl_c">
					<label class="rad_lbl" style="width: 50px;">実績率pt<br>
					<input type="radio" tabindex="1001" name="<%=DefineReport.Radio.TENKAIKBN.getObj()%><%=page_suffix%>" id="<%=DefineReport.Radio.TENKAIKBN.getObj()%><%=DefineReport.ValKbn10007.VAL3.getVal()%><%=page_suffix%>" value="<%=DefineReport.ValKbn10007.VAL3.getVal()%>"/>
					</label>
				</td>
				<td class="col_lbl_c">
					<label class="rad_lbl" style="width: 50px;">通常率pt<br>
					<input type="radio" tabindex="1002" name="<%=DefineReport.Radio.TENKAIKBN.getObj()%><%=page_suffix%>" id="<%=DefineReport.Radio.TENKAIKBN.getObj()%><%=DefineReport.ValKbn10007.VAL1.getVal()%><%=page_suffix%>" value="<%=DefineReport.ValKbn10007.VAL1.getVal()%>"/>
					</label>
				</td>
				<td class="col_lbl_c">
					<label class="rad_lbl" style="width: 50px;">通常数pt<br>
					<input type="radio" tabindex="1003" name="<%=DefineReport.Radio.TENKAIKBN.getObj()%><%=page_suffix%>" id="<%=DefineReport.Radio.TENKAIKBN.getObj()%><%=DefineReport.ValKbn10007.VAL2.getVal()%><%=page_suffix%>" value="<%=DefineReport.ValKbn10007.VAL2.getVal()%>"/>
					</label>
				</td>
			</tr>
			</tbody></table>
		</td></tr>
		<tr><td>
			<table class="dataTable" cellspacing="0" cellpadding="0">
			<tbody>
			<tr><td class="labelCell" colspan="2">実績率pt数値</td></tr>
			<tr>
				<td class="col_lbl_c">
					<label class="rad_lbl" style="width: 50px;">売上<br>
					<input type="radio" tabindex="1004" name="<%=DefineReport.Radio.JSKPTNSYUKBN.getObj()%><%=page_suffix%>" id="<%=DefineReport.Radio.JSKPTNSYUKBN.getObj()%><%=DefineReport.ValKbn10008.VAL1.getVal()%><%=page_suffix%>" value="<%=DefineReport.ValKbn10008.VAL1.getVal()%>"/>
					</label>
				</td>
				<td class="col_lbl_c">
					<label class="rad_lbl" style="width: 50px;">点数<br>
					<input type="radio" tabindex="1005" name="<%=DefineReport.Radio.JSKPTNSYUKBN.getObj()%><%=page_suffix%>" id="<%=DefineReport.Radio.JSKPTNSYUKBN.getObj()%><%=DefineReport.ValKbn10008.VAL2.getVal()%><%=page_suffix%>" value="<%=DefineReport.ValKbn10008.VAL2.getVal()%>"/>
					</label>
				</td>
			</tr>
			</tbody></table>
		</td></tr>
		<tr><td>
			<table class="dataTable" cellspacing="0" cellpadding="0">
			<tbody>
			<tr>
				<td class="labelCell" colspan="3">実績率pt前年同週</td>
				<td class="col_empty"></td>
				<td class="labelCell" colspan="3">実績率pt前年同月</td>
			</tr>
			<tr>
				<td class="col_lbl_c">
					<label class="rad_lbl" style="width: 50px;">部門実績<br>
					<input type="radio" tabindex="1006" name="<%=DefineReport.Radio.JSKPTNZNENWKBN.getObj()%><%=page_suffix%>" id="<%=DefineReport.Radio.JSKPTNZNENWKBN.getObj()%><%=DefineReport.ValKbn10009.VAL3.getVal()%><%=page_suffix%>" value="<%=DefineReport.ValKbn10009.VAL3.getVal()%>"/>
					</label>
				</td>
				<td class="col_lbl_c">
					<label class="rad_lbl" style="width: 50px;">大実績<br>
					<input type="radio" tabindex="1007" name="<%=DefineReport.Radio.JSKPTNZNENWKBN.getObj()%><%=page_suffix%>" id="<%=DefineReport.Radio.JSKPTNZNENWKBN.getObj()%><%=DefineReport.ValKbn10009.VAL1.getVal()%><%=page_suffix%>" value="<%=DefineReport.ValKbn10009.VAL1.getVal()%>"/>
					</label>
				</td>
				<td class="col_lbl_c">
					<label class="rad_lbl" style="width: 50px;">中実績<br>
					<input type="radio" tabindex="1008" name="<%=DefineReport.Radio.JSKPTNZNENWKBN.getObj()%><%=page_suffix%>" id="<%=DefineReport.Radio.JSKPTNZNENWKBN.getObj()%><%=DefineReport.ValKbn10009.VAL2.getVal()%><%=page_suffix%>" value="<%=DefineReport.ValKbn10009.VAL2.getVal()%>"/>
					</label>
				</td>
				<td class="col_empty"></td>
				<td class="col_lbl_c">
					<label class="rad_lbl" style="width: 50px;">部門実績<br>
					<input type="radio" tabindex="1009" name="<%=DefineReport.Radio.JSKPTNZNENMKBN.getObj()%><%=page_suffix%>" id="<%=DefineReport.Radio.JSKPTNZNENMKBN.getObj()%><%=DefineReport.ValKbn10009.VAL3.getVal()%><%=page_suffix%>" value="<%=DefineReport.ValKbn10009.VAL3.getVal()%>"/>
					</label>
				</td>
				<td class="col_lbl_c">
					<label class="rad_lbl" style="width: 50px;">大実績<br>
					<input type="radio" tabindex="1010" name="<%=DefineReport.Radio.JSKPTNZNENMKBN.getObj()%><%=page_suffix%>" id="<%=DefineReport.Radio.JSKPTNZNENMKBN.getObj()%><%=DefineReport.ValKbn10009.VAL1.getVal()%><%=page_suffix%>" value="<%=DefineReport.ValKbn10009.VAL1.getVal()%>"/>
					</label>
				</td>
				<td class="col_lbl_c">
					<label class="rad_lbl" style="width: 50px;">中実績<br>
					<input type="radio" tabindex="1011" name="<%=DefineReport.Radio.JSKPTNZNENMKBN.getObj()%><%=page_suffix%>" id="<%=DefineReport.Radio.JSKPTNZNENMKBN.getObj()%><%=DefineReport.ValKbn10009.VAL2.getVal()%><%=page_suffix%>" value="<%=DefineReport.ValKbn10009.VAL2.getVal()%>"/>
					</label>
				</td>
			</tr>
			</tbody></table>
		</td></tr>
		</table>
		</div>
		<div data-options="region:'south',border:false" style="display:none;height:30px;padding:2px 5px 0;">
			<div class="btn" style="text-align: center;">
				<a href="#" class="easyui-linkbutton" tabindex="1020" title="<%=DefineReport.Button.CANCEL.getTxt()%>" id="<%=DefineReport.Button.CANCEL.getObj()%><%=page_suffix%>" iconCls="icon-cancel" style="width:100px;"><span><%=DefineReport.Button.CANCEL.getTxt()%></span></a>
				　
				<a href="#" class="easyui-linkbutton" tabindex="1021" title="設定" id="<%=DefineReport.Button.SELECT.getObj()%><%=page_suffix%>" iconCls="icon-edit" style="width:100px;"><span class="btnTxt">設定</span></a>
			</div>
		</div>
	</div>
</div>