<%@page import="org.apache.commons.lang.StringUtils"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8"%>
<%@ page import="java.util.Date"%>
<%@ page import="common.DefineReport" %>
<%@ page import="common.Defines" %>
<%
	String page_suffix = "_winST008";
	String reportNo	=	(String)request.getSession().getAttribute(Defines.ID_REQUEST_JSP_REPORT);

	if(StringUtils.equals("Out_ReportST008", reportNo)){
		page_suffix = "";
	}
%>
<div id="<%=page_suffix%>" title="<%=DefineReport.Button.TENINFO.getTxt()%>" style="width:855px;height:450px;position: relative;display:none;">
	<div id="el<%=page_suffix%>" class="easyui-layout" data-options="fit:true" style="pading:0;">
		<div data-options="region:'north',border:false" style="height:0px;width:100%;padding:2px 5px 0;">
			<form id="ff_<%=page_suffix%>" method="post" style="display: inline">
			<div class="Search">
				<table id="dTable2" class="dataTable" cellspacing="0" cellpadding="0">
				<tr>
					<td class="labelCell" style="width: 50px;text-align:center">部門</td>
					<td style="border-style: none;"></td>
					<td class="labelCell" style="width: 70px;text-align:center">ランクNo.</td>
					<td style="border-style: none;"></td>
					<td class="labelCell" style="width:300px;text-align:center">ランク名称</td>
					<td style="border-style: none;"></td>
					<td class="labelCell" style="width: 50px;text-align:center">臨時</td>
					<td style="border-style: none;"></td>
					<td class="labelCell" style="width:100px;text-align:center">催しコード</td>
				</tr>
				<tr>
					<td class="col_txt" style="height: 20px;"><span id="<%=DefineReport.InpText.BMNCD.getObj()%><%=page_suffix%>" col="F1"></span></td>
					<td style="border-style: none;"></td>
					<td class="col_txt">
						<input  class="easyui-numberbox" "col="F2" tabindex="1000" id="<%=DefineReport.InpText.RANKNO.getObj()%><%=page_suffix%>" check='<%=DefineReport.InpText.RANKNO.getMaxlenTag()%>' style="width:70px;" />
					</td>
					<td style="border-style: none;"></td>
					<td class="col_txt">
						<input "col="F3" class="easyui-textbox" tabindex="1001" id="<%=DefineReport.InpText.RANKKN.getObj()%><%=page_suffix%>" check='<%=DefineReport.InpText.RANKKN.getMaxlenTag()%>' style="width:300px;" />
					</td>
					<td style="border-style: none;"></td>
					<td class="col_chk" style="width:35px;"><label class="chk_lbl"><input type="checkbox" tabindex="-1" id="<%=DefineReport.Checkbox.RINJI.getObj()%><%=page_suffix%>" style="vertical-align:middle;"></label></td>
					<td style="border-style: none;"></td>
					<td class="col_txt" ><span id="<%=DefineReport.InpText.MOYSCD.getObj()%><%=page_suffix%>"col="F4"></span></td>
					<td style="border-style: none;"><a href="#" title="実績参照" id="<%=DefineReport.Button.JISSEKIREFER.getObj()%><%=page_suffix%>" class="easyui-linkbutton" tabindex="1002" iconCls="icon-search"><span class="btnTxt">実績参照</span></a></td>
				</tr>
				</table>
				<table>
					<tr>
						<td id = "gridLayout" style = "width: 550px;height: 300px;">
						<div class="easyui-datagrid " tabindex="1003" id="<%=DefineReport.Grid.SUBWINDOWTENIFO.getObj()%><%=page_suffix%>" data-options="singleSelect:true,rownumbers:true,fit:true"></div>
						<%--<div class="easyui-datagrid placeFace" tabindex="2" id="gridholder" data-options="singleSelect:true,rownumbers:true,fit:true"></div> --%>

						<div class="ref_editor" style="display: none;">
							<input  class="easyui-textbox"   tabindex="-1" id="<%=DefineReport.InpText.RANK.getObj()%><%=page_suffix%>" check='<%=DefineReport.InpText.RANK.getMaxlenTag()%>' />
						</div>
						</td>
						<td style = "width: 100px;height: 300px;">
						<TABLE id="dTable1" class="dataTable" cellspacing="0" cellpadding="0" style="display:inline;">
						<TBODY>
						<TR align="left">
							<TD class="labelCell" style="WIDTH: 70px;text-align:center">店舗数</TD>
						</TR>
						<TR align="left">
							<TD><input class="easyui-numberbox" tabindex="-1" col="F1" id="<%=DefineReport.Text.TEN_NUMBER.getObj()%><%=page_suffix%>" data-options="cls:'labelInputNum'" style="width:65px;text-alien:center;" readonly="readonly"></TD>
						</TR></TBODY></TABLE>
						<table>
							<tr>
								<td class="col_btn"><a href="#" title="店番順" id="<%=DefineReport.Button.TENNOORDER.getObj()%><%=page_suffix%>" class="easyui-linkbutton" tabindex="1005" iconCls="icon-search"><span class="btnTxt">店番順</span></a></td>
							</tr>
							<tr>
								<td class="col_btn"><a href="#" title="ランク順" id="<%=DefineReport.Button.RANKORDER.getObj()%><%=page_suffix%>" class="easyui-linkbutton" tabindex="1006" iconCls="icon-search"><span class="btnTxt">ランク順</span></a></td>
							</tr>
							<tr>
								<td class="col_btn"><a href="#" title="実績順" id="<%=DefineReport.Button.JISSEKIORDER.getObj()%><%=page_suffix%>" class="easyui-linkbutton" tabindex="1007" iconCls="icon-search"><span class="btnTxt">実績順</span></a></td>
							</tr>
						</table>
						</td>
						<td style="width:130px;height: 300px;">
						<fieldset style="width:130px;height: 300px;">
						<legend>店番 一括入力</legend>
						<table>
						<tr>
						<td>
							<input  class="easyui-textbox"   tabindex="1008" id="<%=DefineReport.InpText.RANKIINPUT.getObj()%><%=page_suffix%>" check='<%=DefineReport.InpText.RANKIINPUT.getMaxlenTag()%>'  data-options="label:'ランク',labelWidth:50" style="width:90px;" />
						</td>
						<td>
						</td>
						</tr>
						<tr>
							<td>
								<div style="height:220px;width: 100px;">
								<div class="easyui-datagrid" tabindex="1009" id="<%=DefineReport.Grid.TENCDIINPUT.getObj()%>_list<%=page_suffix%>" data-options="singleSelect:true,rownumbers:true, fit:true"></div>
								<div class="ref_editor" style="display: none;">
								<input  class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.TENCD.getObj()%><%=page_suffix%>" check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' />
								</div>
								</div>
							</td>
						</tr>
						<tr>
							<td><a href="#" class="easyui-linkbutton" tabindex="1010" id="<%=DefineReport.Button.SET.getObj()%><%=page_suffix%>" title="<%=DefineReport.Button.SET.getTxt()%>" iconCls="icon-edit"><span class="btnTxt2" style="width:60px;text-align:center"><%=DefineReport.Button.SET.getTxt()%></span></a></td>
						</tr>
						</table>
						</fieldset>
						</td>
					</tr>
				</table>
			</div>
			</form>
		</div>
		<div data-options="region:'south',border:false" style="display:none;height:30px;padding:2px 5px 0;">
			<div class="btn" style="text-align: center;">
				<a href="#" class="easyui-linkbutton" tabindex="1011" title="<%=DefineReport.Button.CANCEL.getTxt()%>" id="<%=DefineReport.Button.CANCEL.getObj()%><%=page_suffix%>" iconCls="icon-cancel" style="width:100px;"><span><%=DefineReport.Button.CANCEL.getTxt()%></span></a>

				<a href="#" class="easyui-linkbutton" tabindex="1012" title="<%=DefineReport.Button.UPD.getTxt()%>" id="<%=DefineReport.Button.UPD.getObj()%><%=page_suffix%>" iconCls="icon-edit" style="width:100px;"><span class="btnTxt"><%=DefineReport.Button.UPD.getTxt()%></span></a>

				<%-- <a href="#" class="easyui-linkbutton" tabindex="1012" title="<%=DefineReport.Button.SELECT.getTxt()%>" id="<%=DefineReport.Button.SELECT.getObj()%><%=page_suffix%>" iconCls="icon-edit" style="width:100px;"><span class="btnTxt"><%=DefineReport.Button.SELECT.getTxt()%></span></a> --%>
			</div>
			<div style="display: none;">
				<a href="#" title="実績参照" id="<%=DefineReport.Button.ZITREF.getObj()%><%=page_suffix%>" class="easyui-linkbutton" tabindex="-1" iconCls="icon-search"></a>
			</div>

		</div>
		</div>
	</div>

