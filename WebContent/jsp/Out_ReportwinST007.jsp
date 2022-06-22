<%@page import="org.apache.commons.lang.StringUtils"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8"%>
<%@ page import="java.util.Date"%>
<%@ page import="common.DefineReport" %>
<%@ page import="common.Defines" %>
<%
	String page_suffix = "_winST007";
	String reportNo	=	(String)request.getSession().getAttribute(Defines.ID_REQUEST_JSP_REPORT);

	if(StringUtils.equals("Out_ReportST007", reportNo)){
		page_suffix = "";
	}
%>
<div id="<%=page_suffix%>" title="<%=DefineReport.Button.RANKTENINFO.getTxt()%>" style="width:700px;height:450px;position: relative;display:none;">
	<div id="el<%=page_suffix%>" class="easyui-layout" data-options="fit:true" style="pading:0;">
		<div data-options="region:'north',border:false" style="height:44px;width:100%;padding:2px 5px 0;">
			<div class="Search">
				<table id="dTable2" class="dataTable" cellspacing="0" cellpadding="0">
				<tr>
					<td class="labelCell" style="width: 50px;">部門</td>
					<td style="border-style: none;"></td>
					<td class="labelCell" style="width: 70px;">ランクNo.</td>
					<td style="border-style: none;"></td>
					<td class="labelCell" style="width:300px;">ランク名称</td>
					<td style="border-style: none;"></td>
					<td class="labelCell" style="width: 50px;">臨時</td>
					<td style="border-style: none;"></td>
					<td class="labelCell" style="width:100px;">催しコード</td>
				</tr>
				<tr>
					<td class="col_txt" style="height: 20px;"><span id="<%=DefineReport.InpText.BMNCD.getObj()%><%=page_suffix%>"col="F1"></span></td>
					<%-- <td class="col_txt" style="height: 20px;"><input  class="easyui-numberbox"   tabindex="-1" id="<%=DefineReport.InpText.BMNCD.getObj()%><%=page_suffix%>" check='<%=DefineReport.InpText.BMNCD.getMaxlenTag()%>' /></td> --%>
					<td style="border-style: none;"></td>
					<td class="col_txt"><span id="<%=DefineReport.InpText.RANKNO.getObj()%><%=page_suffix%>"col="F2"></span></td>
					<td style="border-style: none;"></td>
					<td class="col_txt"><span id="<%=DefineReport.InpText.RANKKN.getObj()%><%=page_suffix%>"col="F3"></span></td>
					<td style="border-style: none;"></td>
					<td class="col_chk" style="width:35px;"><label class="chk_lbl"><input type="checkbox" tabindex="-1" id="<%=DefineReport.Checkbox.RINJI.getObj()%><%=page_suffix%>" style="vertical-align:middle;"></label></td>
					<td style="border-style: none;"></td>
					<td class="col_txt" ><span id="<%=DefineReport.InpText.MOYSCD.getObj()%><%=page_suffix%>"col="F4"></span></td>
					<td style="border-style: none;display:none"><a href="#" title="実績参照" id="<%=DefineReport.Button.JISSEKIREFER.getObj()%><%=page_suffix%>" class="easyui-linkbutton" tabindex="-1" iconCls="icon-search"><span class="btnTxt">実績参照</span></a></td>
				</tr>
				</table>
			</div>
		</div>
		<div data-options="region:'center',border:false">
			<div class="easyui-datagrid " tabindex="1001" id="<%=DefineReport.Grid.RANKTENINFO.getObj()%><%=page_suffix%>" data-options="singleSelect:true,rownumbers:true,fit:true" style="width:400px;"></div>
		</div>
		<div data-options="region:'east',border:false">
		<table>
					<tr>
						<td>
						<TABLE id="dTable1" class="dataTable" cellspacing="0" cellpadding="0" style="display:inline;aline:right">
						<tbody>
						<TR align="left">
							<TD class="labelCell" style="WIDTH: 70px;text-align:center">店舗数</TD>
						</TR>
						<TR align="left">
							<TD><input class="easyui-numberbox" tabindex="-1" col="F1" id="<%=DefineReport.Text.TEN_NUMBER.getObj()%><%=page_suffix%>" data-options="cls:'labelInputNum'" style="width:65px;text-alien:center;" readonly="readonly"></TD>
						</TR>
						</tbody></table>
						<table>
							<tr>
								<td class="col_btn">
									<a href="#" title="店番順" id="<%=DefineReport.Button.TENNOORDER.getObj()%><%=page_suffix%>" class="easyui-linkbutton" tabindex=1004 iconCls="icon-search"><span class="btnTxt">店番順</span></a>
									<!-- 非表示でデータを保持 -->
									<input type="hidden" name="<%=DefineReport.Button.TENNOORDER.getObj()%>_az<%=page_suffix%>" id="<%=DefineReport.Button.TENNOORDER.getObj()%>_az<%=page_suffix%>" value='1' />
								</td>
							</tr>
							<tr>
								<td class="col_btn">
									<a href="#" title="ランク順" id="<%=DefineReport.Button.RANKORDER.getObj()%><%=page_suffix%>" class="easyui-linkbutton" tabindex="1005" iconCls="icon-search"><span class="btnTxt">ランク順</span></a>
									<input type="hidden" name="<%=DefineReport.Button.RANKORDER.getObj()%>_az<%=page_suffix%>" id="<%=DefineReport.Button.RANKORDER.getObj()%>_az<%=page_suffix%>" value='0' />
								</td>
							</tr>
							<tr>
								<td class="col_btn">
									<a href="#" title="実績順" id="<%=DefineReport.Button.JISSEKIORDER.getObj()%>" class="easyui-linkbutton" tabindex="1006" iconCls="icon-search"><span class="btnTxt">実績順</span></a>
									<input type="hidden" name="<%=DefineReport.Button.JISSEKIORDER.getObj()%>_az<%=page_suffix%>" id="<%=DefineReport.Button.JISSEKIORDER.getObj()%>_az<%=page_suffix%>" value='0' />
								</td>
							</tr>
						</table>
						</td>
						<td>

					 	<div id ="layout_ST008">
						<fieldset style="width:160px;height: 600px;display:none">
						<legend>店番 一括入力</legend>
						<table>
						<tr>
						<td>ランク</td>
						<td><input class="easyui-numberbox" tabindex="1007" id="<%=DefineReport.Text.TEN_NUMBER.getObj()%><%=page_suffix%>"  style="width:60px;" value=""></td>
						<td>
						</td>
						</tr>
						<tr>
							<td>
								<div style="height:300px;width: 100px;">
								<div class="easyui-datagrid" tabindex="1008" id="<%=DefineReport.Grid.TENCDIINPUT.getObj()%>_list" data-options="singleSelect:true,rownumbers:true, fit:true"></div>
								<div class="ref_editor" style="display: none;">
								<input  class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.TENCD.getObj()%><%=page_suffix%>" check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' />
								</div>
								</div>
							</td>
						</tr>
						<tr>
							<td><a href="#" class="easyui-linkbutton" tabindex="1009" id="<%=DefineReport.Button.SET.getObj()%>" title="<%=DefineReport.Button.SET.getTxt()%>" iconCls="icon-edit" style="margin-top: 50px"><span class="btnTxt2" style="width:60px;text-align:left"><%=DefineReport.Button.SET.getTxt()%></span></a></td>
						</tr>
						</table>
						</fieldset>
						</div>
						</td>
					</tr>
				</table>

		</div>
		<div data-options="region:'south',border:false" style="display:none;height:30px;padding:2px 5px 0;">
			<div class="btn" style="text-align: center;">
				<a href="#" class="easyui-linkbutton" tabindex="1010" title="<%=DefineReport.Button.CANCEL.getTxt()%>" id="<%=DefineReport.Button.CANCEL.getObj()%><%=page_suffix%>" iconCls="icon-cancel" style="width:100px;"><span><%=DefineReport.Button.CANCEL.getTxt()%></span></a>
			</div>
		</div>
		</div>
	</div>

