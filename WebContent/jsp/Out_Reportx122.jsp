
<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8"%>

<%@ page import="java.util.Date"%>
<%@ page import="common.Defines" %>
<%@ page import="common.DefineReport" %>
<%@ page import="authentication.bean.*" %>
<%@ page import="authentication.defines.*" %>
<%
	// レポート番号
	String reportNo		=	(String)request.getSession().getAttribute(Defines.ID_REQUEST_JSP_REPORT);
	// ユーザ・セッション取得
	String userId		=	(String)request.getSession().getAttribute(Defines.ID_REQUEST_USER_ID);
	// 制限値取得
	String ro			=	(String)request.getSession().getAttribute(Defines.ID_REQUEST_CUSTOM_REPORT);
	// レポート名取得
	String reportName	=	(String)request.getSession().getAttribute(Defines.ID_REQUEST_TITLE_REPORT);
	// レポート予備情報
	String reportYobi1	=	(String)request.getSession().getAttribute(Defines.ID_REQUEST_REPORT_YOBI1);
	String reportYobi2	=	(String)request.getSession().getAttribute(Defines.ID_REQUEST_REPORT_YOBI2);

	// タイトル
	String titleName	=	"【 " + reportName + " 】";
	// 初期検索条件
	String initParam	= (String)request.getSession().getAttribute(Defines.ID_REQUEST_INIT_PARAM);

	// 親画面からの引き継ぎ情報
	String sendParam	= "";
	if (request.getSession().getAttribute(Defines.ID_REQUEST_SEND_PARAM)!=null) {
		sendParam	=	(String)request.getSession().getAttribute(Defines.ID_REQUEST_SEND_PARAM);
	}
	// ユーザID
	String user		=	(String)request.getSession().getAttribute(Defines.ID_REQUEST_CUSTOM_USER);
	// レポートID
	String report	=	(String)request.getSession().getAttribute(Defines.ID_REQUEST_REPORT_NO);

	User lusr = (User)request.getSession().getAttribute(Consts.STR_SES_LOGINUSER);

	String userTenpo = lusr.getTenpo();
	String userBumon = lusr.getBumon();

	// jsキャッシュ対応
	String prm = request.getSession().getId();
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html style="overflow-x: hidden;">
<head>
<title></title>
<meta http-equiv="X-UA-Compatible" content="IE=edge" />
<meta http-equiv="Pragma" content="no-cache">
<meta http-equiv="Cache-Control" content="no-cache">
<meta http-equiv="Expires" content="Thu, 01 Dec 1994 16:00:00 GMT">
<link rel="stylesheet" type="text/css" href="../css/report.css?v=<%=prm %>">
<link rel="stylesheet" type="text/css" href="../themes/easyui/default/easyui.css?v=<%=prm %>">
<link rel="stylesheet" type="text/css" href="../themes/easyui/default/custom_easyui.css?v=<%=prm %>">
<link rel="stylesheet" type="text/css" href="../themes/easyui/icon.css?v=<%=prm %>">
</head>
<body style="overflow-x: hidden;">
<div id="cc" class="easyui-layout" data-options="fit:true">
<form id="ff" method="post" style="display: inline">
<div data-options="region:'center',border:false" style="display:none;">
<table class="t-layout1" style="margin:10px;">
	<tr>
		<td style="vertical-align: top;">
		<table class="t-layout">
			<tr>
				<td >
					<div class="inp_box"><input class="easyui-numberbox" tabindex="1" col="F1" id="<%=DefineReport.InpText.TENCD.getObj()%>" check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' data-options="label:'店コード',labelWidth:60,required:true" style="width:100px;"></div>
				</td>
				<td>
					<div class="inp_box"><input class="easyui-numberbox" tabindex="2" col="F2" id="<%=DefineReport.InpText.BMNCD2.getObj()%>" check='<%=DefineReport.InpText.BMNCD2.getMaxlenTag()%>' data-options="label:'部門',labelWidth:40,required:true" style="width:100px;"></div>
				</td>
			</tr>
		</table>
		</td>
	</tr>
	<tr>
	<td style="vertical-align: top; padding:10px;">
		<table class="t-layout">
			<tr>
				<td >
					<div class="inp_box"><select class="easyui-combobox" tabindex="3"  col="F3" id="<%=DefineReport.Select.READTMPTN.getObj()%>" data-options="label:'リードタイムパターン',labelWidth:150,required:true" style="width:350px;"></select></div>
				</td>
				<td rowspan="4">
					<div id = "newLegend">
					<fieldset>
						<legend>店舗部門マスタコピー</legend>
						<table class="t-layout">
							<tr>
								<td>
									<span>　　　　　コピー元→</span>
									<div class="inp_box"><input class="easyui-numberbox" tabindex="100" col="bmn_org" id="<%=DefineReport.InpText.BMNCD2.getObj()%>_bmn_org" check='<%=DefineReport.InpText.BMNCD2.getMaxlenTag()%>' data-options="label:'部門',labelWidth:60" style="width:100px;"></div>
									<div class="inp_box"><input class="easyui-numberbox" tabindex="101" col="ten_org" id="<%=DefineReport.InpText.TENCD.getObj()%>_ten_org" check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' data-options="label:'店コード',labelWidth:60" style="width:100px;"></div>
								</td>
								<td>
									<span>コピー先</span>
									<div class="inp_box"><input class="easyui-numberbox" tabindex="102" col="bmn_copy" id="<%=DefineReport.InpText.BMNCD2.getObj()%>_bmn_copy" check='<%=DefineReport.InpText.BMNCD2.getMaxlenTag()%>'  style="width:40px;"></div>
									<div class="inp_box"><input class="easyui-numberbox" tabindex="103" col="ten_copy" id="<%=DefineReport.InpText.TENCD.getObj()%>_ten_copy" check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>'  style="width:40px;"></div>
								</td>
							</tr>
						</table>
						<table class="t-layout">
							<tr>
								<td class="col_btn" rowspan="4" >
									<a href="#" title="検索" id="<%=DefineReport.Button.SEARCH.getObj()%>" class="easyui-linkbutton" tabindex="104" iconCls="icon-search"><span class="btnTxt">検索</span></a>
								</td>
							</tr>
						</table>
					</fieldset>
					</div>
				</td>
			</tr>
			<tr>
				<td>
					<div class="inp_box"><input class="easyui-textbox"   tabindex="4"  col="F4" id="<%=DefineReport.InpText.BMKAN.getObj()%>" check='<%=DefineReport.InpText.BMNKN.getMaxlenTag()%>' data-options="label:'部門（ﾃﾅﾝﾄ）名称（漢字）',labelWidth:150,required:true" style="width:420px;"></div>
				</td>
			</tr>
			<tr>
				<td>
					<div class="inp_box"><input class="easyui-numberbox" tabindex="5"  col="F5" id="<%=DefineReport.InpText.GROUPNO.getObj()%>" check='<%=DefineReport.InpText.GROUPNO.getMaxlenTag()%>' data-options="label:'グループNo',labelWidth:150,required:true" style="width:200px;"></div>
				</td>
			</tr>
			<tr>
				<td>
					<div class="inp_box"><input class="easyui-textbox" tabindex="6"  col="F6" id="<%=DefineReport.InpText.RECEIPTKN.getObj()%>" check='<%=DefineReport.InpText.BMNRECEIPTKN.getMaxlenTag()%>' data-options="label:'部門レシート名称（漢字）',labelWidth:150,required:true" style="width:420px;"></div>
				</td>
			</tr>
			<tr>
				<td>
					<div class="inp_box"><input class="easyui-textbox" tabindex="7"  col="F7" id="<%=DefineReport.InpText.RECEIPTAN.getObj()%>" check='<%=DefineReport.InpText.BMNRECEIPTAN.getMaxlenTag()%>' data-options="label:'部門レシート名称（カナ）',labelWidth:150,required:true" style="width:420px;"></div>
				</td>
			</tr>
			<tr>
				<td>
					<div class="inp_box"><select class="easyui-combobox" tabindex="8"  col="F8" id="<%=DefineReport.MeisyoSelect.KBN317.getObj()%>" data-options="label:'MIO区分',labelWidth:150,required:true" style="width:250px;"></select></div>
				</td>
			</tr>
			<tr>
				<td>
					<div class="inp_box"><input class="easyui-numberbox" tabindex="9" col="F9" id="<%=DefineReport.InpText.SHUKEICD.getObj()%>" check='<%=DefineReport.InpText.SHUKEICD.getMaxlenTag()%>' data-options="label:'集計CD',labelWidth:150,required:true" style="width:200px;"></div>
				</td>
			</tr>
			<tr>
				<td>
					<div class="inp_box"><select class="easyui-combobox" tabindex="10" col="F10" id="<%=DefineReport.MeisyoSelect.KBN318.getObj()%>" data-options="label:'割引区分',labelWidth:150,required:true" style="width:270px;"></select></div>
				</td>
			</tr>
			<tr>
				<td>
					<div class="inp_box"><input class="easyui-numberbox" tabindex="11" col="F11" id="<%=DefineReport.InpText.BMNGENKART.getObj()%>" check='<%=DefineReport.InpText.BMNGENKART.getMaxlenTag()%>' data-options="label:'部門原価率',labelWidth:150,required:true" style="width:250px; text-align: right;" ></div>
				</td>
				<td>
					<div class="inp_box"><select class="easyui-combobox" tabindex="19" col="F19" id="<%=DefineReport.MeisyoSelect.KBN323.getObj()%>" data-options="label:'売上げフラグ',labelWidth:150,required:true" style="width:230px;"></select></div>
				</td>
			</tr>
			<tr>
				<td>
					<div class="inp_box"><select class="easyui-combobox" tabindex="12" col="F12" id="<%=DefineReport.MeisyoSelect.KBN319.getObj()%>" data-options="label:'自社テナント',labelWidth:150,required:true" style="width:250px;"></select></div>
				</td>
				<td>
					<div class="inp_box"><input class="easyui-numberbox" tabindex="20" col="F20" id="<%=DefineReport.InpText.TENANTCD.getObj()%>" check='<%=DefineReport.InpText.TENANTCD.getMaxlenTag()%>' data-options="label:'テナントコード',labelWidth:150" style="width:230px; text-align: left" ></div>
				</td>
			</tr>
			<tr>
				<td>
					<div class="inp_box"><select class="easyui-combobox" tabindex="13" col="F13" id="<%=DefineReport.MeisyoSelect.KBN320.getObj()%>" data-options="label:'ロス分析対象',labelWidth:150,required:true" style="width:250px;"></select></div>
				</td>
				<td>
					<div class="inp_box"><input class="easyui-numberbox" tabindex="21" col="F21" id="<%=DefineReport.InpText.BMN_ATR1.getObj()%>" check='<%=DefineReport.InpText.BMN_ATR1.getMaxlenTag()%>' data-options="label:'部門属性1',labelWidth:150" style="width:180px;" ></div>
				</td>
			</tr>
			<tr>
				<td>
					<div class="inp_box"><select class="easyui-combobox" tabindex="14" col="F14" id="<%=DefineReport.MeisyoSelect.KBN321.getObj()%>" data-options="label:'予算区分',labelWidth:150,required:true" style="width:250px;"></select></div>
				</td>
				<td>
					<div class="inp_box"><input class="easyui-numberbox" tabindex="22" col="F22" id="<%=DefineReport.InpText.BMN_ATR2.getObj()%>" check='<%=DefineReport.InpText.BMN_ATR2.getMaxlenTag()%>' data-options="label:'部門属性2',labelWidth:150" style="width:180px;" ></div>
				</td>
			</tr>
			<tr>
				<td>
					<div class="inp_box"><select class="easyui-combobox" tabindex="15" col="F15" id="<%=DefineReport.MeisyoSelect.KBN322.getObj()%>" data-options="label:'棚卸対象区分',labelWidth:150,required:true" style="width:250px;"></select></div>
				</td>
				<td>
					<div class="inp_box"><input class="easyui-numberbox" tabindex="23" col="F23" id="<%=DefineReport.InpText.BMN_ATR3.getObj()%>" check='<%=DefineReport.InpText.BMN_ATR3.getMaxlenTag()%>' data-options="label:'部門属性3',labelWidth:150" style="width:180px;" ></div>
				</td>
			</tr>
			<tr>
				<td>
					<div class="inp_box"><select class="easyui-combobox" tabindex="16" col="F16" id="<%=DefineReport.MeisyoSelect.KBN118.getObj()%>" data-options="label:'プライスカード_種類',labelWidth:150,required:true" style="width:250px;"></select></div>
				</td>
				<td>
					<div class="inp_box"><input class="easyui-numberbox" tabindex="24" col="F24" id="<%=DefineReport.InpText.BMN_ATR4.getObj()%>" check='<%=DefineReport.InpText.BMN_ATR4.getMaxlenTag()%>' data-options="label:'部門属性4',labelWidth:150" style="width:180px;" ></div>
				</td>
			</tr>
			<tr>
				<td>
					<div class="inp_box"><select class="easyui-combobox" tabindex="17" col="F17" id="<%=DefineReport.MeisyoSelect.KBN119.getObj()%>" data-options="label:'プライスカード_色',labelWidth:150,required:true" style="width:250px;"></select></div>
				</td>
				<td>
					<div class="inp_box"><input class="easyui-numberbox" tabindex="25" col="F25" id="<%=DefineReport.InpText.BMN_ATR5.getObj()%>" check='<%=DefineReport.InpText.BMN_ATR5.getMaxlenTag()%>' data-options="label:'部門属性5',labelWidth:150" style="width:180px;" ></div>
				</td>
			</tr>
			<tr>
				<td>
					<div class="inp_box"><input class="easyui-numberbox" tabindex="18" col="F18" id="<%=DefineReport.InpText.URIAERACD.getObj()%>" check='<%=DefineReport.InpText.AERACD.getMaxlenTag()%>' data-options="label:'エリア',labelWidth:150,required:true" style="width:200px; text-align: left"></div>
				</td>
			</tr>
		</table>
		</td>
	</tr>
</table>
</div>
</form>
<div id="buttons" data-options="region:'south',border:false" style="display:none;height:30px;padding:0 20px;">
	<div class="btn" style="float: left;">
		<table class="t-layout3">
		<tr><th style="min-width:135px;"></th><th style="min-width:10px;"></th>
		<th style="min-width:135px;"></th>
		<th style="min-width:135px;"></th>
		<th style="min-width:135px;"></th>
		</tr>
		<tr>
		<td><a href="#" title="戻る" id="<%=DefineReport.Button.BACK.getObj()%>" class="easyui-linkbutton" tabindex="146" iconCls="icon-undo" style="width:125px;"><span class="btnTxt">戻る</span></a></td>
		<td></td>
		<td><a href="#" class="easyui-linkbutton" tabindex="147" id="<%=DefineReport.Button.CANCEL.getObj()%>" title="<%=DefineReport.Button.CANCEL.getTxt()%>" iconCls="icon-cancel" style="width:125px;"><%=DefineReport.Button.CANCEL.getTxt()%></a></td>
		<td><a href="#" class="easyui-linkbutton" tabindex="148" id="<%=DefineReport.Button.UPD.getObj()%>" title="<%=DefineReport.Button.UPD.getTxt()%>" iconCls="icon-save" style="width:125px;"><span class="btnTxt"><%=DefineReport.Button.UPD.getTxt()%></span></a></td>
		<td><a href="#" class="easyui-linkbutton" tabindex="149" id="<%=DefineReport.Button.DEL.getObj()%>" title="<%=DefineReport.Button.DEL.getTxt()%>" iconCls="icon-remove" style="width:125px;"><span class="btnTxt"><%=DefineReport.Button.DEL.getTxt()%></span></a></td>
		</tr>
		</table>
	</div>
	<div id="disp_record_info" style="float: right;">
		<span class="labelName" tabindex="-1" style="padding-top: 3px;">
				登録日 <span col="F26" id="<%=DefineReport.Text.ADDDT.getObj()%>"></span>　更新日 <span col="F27" id="<%=DefineReport.Text.UPDDT.getObj()%>"></span>　オペレータ <span col="F28" id="<%=DefineReport.Text.OPERATOR.getObj()%>"></span>
		</span>
		<input type="hidden" col="F29" name="<%=DefineReport.Hidden.UPDDT.getObj()%>" id="<%=DefineReport.Hidden.UPDDT.getObj()%>" />
	</div>
</div>
</div>
<div id="debug" style="visibility: hidden;">
	<!-- report 情報 -->
	<input type="hidden" name="reportno" id="reportno" value="<%=reportNo %>"/>
	<!-- レポート名 情報 -->
	<input type="hidden" name="reportname" id="reportname" value="<%=reportName %>"/>
	<!-- ユーザー 情報 -->
	<input type="hidden" name="userid" id="userid" value="<%=userId %>"/>
	<input type="hidden" name="userTenpo" id="userTenpo" value="<%=userTenpo %>"/>
	<input type="hidden" name="userBumon" id="userBumon" value="<%=userBumon %>"/>
	<!-- 引き継ぎ情報(JSON文字列変換済 "'" で囲む) -->
	<input type="hidden" name="hiddenParam" id="hiddenParam" value='<%=sendParam %>' />
	<!-- ユーザID -->
	<input type="hidden" name="hiddenUser" id="hiddenUser" value="<%=user %>" />
	<!-- レポートID -->
	<input type="hidden" name="hiddenReport" id="hiddenReport" value="<%=report %>" />
	<!-- 初期条件情報(JSON文字列変換済 "'" で囲む) -->
	<input type="hidden" name="hiddenInit" id="hiddenInit" value='<%=initParam %>' />

	<input type="hidden" name="<%=DefineReport.Hidden.CHANGED_IDX.getObj()%>" id="<%=DefineReport.Hidden.CHANGED_IDX.getObj()%>" />

	<!-- 初期条件情報(JSON文字列変換済 "'" で囲む) -->
	<input type="hidden" name="reportYobi1" id="reportYobi1" value='<%=reportYobi1 %>' />
	<input type="hidden" name="reportYobi2" id="reportYobi2" value='<%=reportYobi2 %>' />
</div>

<div style="display:none;">
</div>
</body>

<!-- Load jQuery and JS files -->
<script type="text/javascript" src="../js/jquery.min.js?v=<%=prm %>"></script>			<!-- jquery -->
<script type="text/javascript" src="../js/jquery.easyui.min.js?v=<%=prm %>"></script>	<!-- EasyUI framework -->
<script type="text/javascript" src="../js/easyui-lang-ja.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/datagrid-scrollview.js?v=<%=prm %>"></script>	<!-- EasyUI framework -->

<script type="text/javascript" src="../js/json2.min.js"></script>			<!-- json plugin -->
<script type="text/javascript" src="../js/exdate.js"></script>				<!-- exdate plugin -->

<script type="text/javascript" src="../js/jshashtable-2.1.js"></script>		<!-- jshashset plugin -->
<script type="text/javascript" src="../js/jquery.numberformatter.min.js"></script><!-- numberformatter plugin -->

<script type="text/javascript" src="../js/shortcut.js"></script>			<!-- shortcut plugin -->

<!-- Report Option & Control & Event  -->
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.x122.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.control.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.events.js?v=<%=prm %>"></script>
</html>