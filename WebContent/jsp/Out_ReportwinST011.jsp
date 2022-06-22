<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8"%>
<%@ page import="common.DefineReport" %>
<%
	String page_suffix2 = "_winST011";
%>
<div id="<%=page_suffix2%>" title="<%=DefineReport.Button.ZITREF.getTxt()%>" style="width:600px;height:450px;position: relative;display:none;">
	<div id="el<%=page_suffix2%>" class="easyui-layout" data-options="fit:true" style="pading:0;">
		<div data-options="region:'north',border:false" style="height:130px;width:100%;padding:2px 5px 0;">
			<div class="Search">
				<table class="t-layout1">
				<tr>
					<td style="text-align:left;vertical-align:bottom;">
						<table id="dTable2" class="dataTable">
							<tr>
								<td colspan=4></td>
								<td class="labelCell" style="width:200px;text-align:center;">中分類実績の場合</td>
							</tr>
							<tr>
								<td class="labelCell" style="width: 40px;text-align:center;">部門</td>
								<td class="labelCell" style="width: 60px;text-align:center;">部門実績</td>
								<td class="labelCell" style="width: 70px;text-align:center;">大分類実績</td>
								<td class="labelCell" style="width: 70px;text-align:center;">中分類実績</td>
								<td class="labelCell" style="width: 200px;text-align:center;">大分類選択</td>
							</tr>
							<tr>
								<td class="col_num" ><input class="easyui-numberbox" tabindex="1" id="<%=DefineReport.InpText.BMNCD.getObj()%><%=page_suffix2%>" check='<%=DefineReport.InpText.BMNCD.getMaxlenTag()%>' data-options="required:true" style="height:20px;width:40px;text-align:left;" value=""></td>

								<td class="col_num" style="text-align:center;"><input type="radio" tabindex="2" id="<%=DefineReport.Radio.JISSEKIBUN.getObj()%><%=page_suffix2%>" name="<%=DefineReport.Radio.JISSEKIBUN.getObj()%><%=page_suffix2%>" value="1" style="height: 20px;vertical-align:middle;" checked="checked"/></td>
								<td class="col_num" style="text-align:center;"><input type="radio" tabindex="3" id="<%=DefineReport.Radio.JISSEKIBUN.getObj()%><%=page_suffix2%>" name="<%=DefineReport.Radio.JISSEKIBUN.getObj()%><%=page_suffix2%>" value="2" style="height: 20px;vertical-align:middle;"/></td>
								<td class="col_num" style="text-align:center;"><input type="radio" tabindex="4" id="<%=DefineReport.Radio.JISSEKIBUN.getObj()%><%=page_suffix2%>" name="<%=DefineReport.Radio.JISSEKIBUN.getObj()%><%=page_suffix2%>" value="3" style="height: 20px;vertical-align:middle;"/></td>
								<td><select class="easyui-combobox" tabindex="5" id="<%=DefineReport.Select.DAI_BUN.getObj()%><%=page_suffix2%>" style="width:200px;"></select></td>
							</tr>
						</table>
					</td>
				</tr>
				</table>
					<table>
						<tr>
							<td style="text-align:left;vertical-align:bottom;">
							<table id="dTable2" class="dataTable">
								<tr>
									<td class="labelCell" style="width:60px;text-align:center;">週データ</td>
									<td class="labelCell" style="width:40px;text-align:center;">年　週</td>
									<td class="labelCell" style="width:60px;text-align:center;">月データ</td>
									<td class="labelCell" style="width:40px;text-align:center;">年　月</td>
								</tr>
								<tr>
									<td class="col_num" style="height:20px;text-align:center;"><input type="radio" tabindex="6" id="<%=DefineReport.Radio.WWMMFLG.getObj()%><%=page_suffix2%>" name="<%=DefineReport.Radio.WWMMFLG.getObj()%><%=page_suffix2%>" value="1" style="vertical-align:middle;" checked="checked"/></td>
									<td class="col_num" style="height:20px;"><input class="easyui-numberbox" tabindex="7" id="<%=DefineReport.InpText.YYWW.getObj()%><%=page_suffix2%>" check='<%=DefineReport.InpText.YYWW.getMaxlenTag()%>' style="height:20px;width:60px;text-align:left;" value=""></td>
									<td class="col_num" style="height:20px;text-align:center;"><input type="radio" tabindex="8" id="<%=DefineReport.Radio.WWMMFLG.getObj()%><%=page_suffix2%>>" name="<%=DefineReport.Radio.WWMMFLG.getObj()%><%=page_suffix2%>" value="2" style="vertical-align:middle;"/></td>
									<td class="col_num" style="height:20px;"><input class="easyui-numberbox" tabindex="9" id="<%=DefineReport.InpText.YYMM.getObj()%><%=page_suffix2%>" check='<%=DefineReport.InpText.YYMM.getMaxlenTag()%>' style="height:20px;width:60px;text-align:left;" value=""></td>
								</tr>
							</table>
							</td>
							<td class="col_btn">
								<a href="#" title="検索" id="<%=DefineReport.Button.SEARCH.getObj()%><%=page_suffix2%>" class="easyui-linkbutton" tabindex="10" iconCls="icon-search"><span class="btnTxt">検索</span></a>
							</td>
						</tr>
					</table>
			</div>
		</div>
		<div data-options="region:'center',border:false" style="display:none;">
			<%-- <div class="easyui-datagrid placeFace" tabindex="1005" id="<%=DefineReport.Grid.ZITREF.getObj()%><%=page_suffix2%>"></div> --%>
			<div class="easyui-datagrid " tabindex="11" id="<%=DefineReport.Grid.ZITREF.getObj()%><%=page_suffix2%>" data-options="singleSelect:true,fit:true" style="width:400px;"></div>

		</div>
		<div data-options="region:'south',border:false" style="display:none;height:40px;padding:2px 5px 0;">
			<div class="btn" style="text-align: center;">
							<a href="#" class="easyui-linkbutton" tabindex="12" title="<%=DefineReport.Button.CANCEL.getTxt()%><%=page_suffix2%>" id="<%=DefineReport.Button.CANCEL.getObj()%><%=page_suffix2%>" iconCls="icon-cancel" style="width:100px;"><span><%=DefineReport.Button.CANCEL.getTxt()%></span></a>
							<a href="#" class="easyui-linkbutton" tabindex="13" title="<%=DefineReport.Button.SEL_KAKUTEI.getTxt()%><%=page_suffix2%>" id="<%=DefineReport.Button.SEL_KAKUTEI.getObj()%><%=page_suffix2%>" iconCls="icon-edit" style="width:140px;"><span>選択（確定）</span></a>
			</div>
		</div>
	</div>
</div>
