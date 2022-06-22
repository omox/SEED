
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
	<div id="tt_3" class="easyui-tabs" data-options="plain:true,narrow:true,justified:true" style="width:100%;height:380px;">
	<div title="基本情報" style="display:none;padding:3px 8px;" data-options="selected:true">
	 <table>
		<tr><th style="max-width:500px;"></th><th style="max-width:500px;"></th></tr>
		<tr>
			<td style="width:100px;">仕入先コード</td>
			<td>
				<input class="easyui-numberbox" tabindex="1" col="F1" id="<%=DefineReport.InpText.SIRCD.getObj()%>" check='<%=DefineReport.InpText.SIRCD.getMaxlenTag()%>' data-options="required:true" style="width:180px;">
			</td>
			<td>仕入先名（漢字）</td>
			<td>
				<input class="easyui-textbox" tabindex="2" col="F2" id="<%=DefineReport.InpText.SIRKN.getObj()%>" check='"datatyp":"zen_text","maxlen":40' data-options="required:true" style="width:300px;">
			</td>
			<td>仕入先名（カナ）</td>
			<td>
				<input class="easyui-textbox" tabindex="3" col="F3" id="<%=DefineReport.InpText.SIRAN.getObj()%>" check='<%=DefineReport.InpText.SIRAN.getMaxlenTag()%>' data-options="required:true" style="width:300px;">
			</td>
		</tr>
	</table>
	<table>
		<tr>
			<td></td>
			<td>〒</td>
			<td>都道府県</td>
			<td>市町村</td>
			<td>町・市</td>
		</tr>
		<tr>
			<td style="width:100px;">住所（漢字）</td>
			<td>
				<input class="easyui-numberbox" tabindex="4" col="F4" id="<%=DefineReport.InpText.YUBINNO_U.getObj()%>" check='<%=DefineReport.InpText.YUBINNO_U.getMaxlenTag()%>' data-options="required:true" style="width:60px;">
				-
				<input class="easyui-numberbox" tabindex="5" col="F5" id="<%=DefineReport.InpText.YUBINNO_S.getObj()%>" check='<%=DefineReport.InpText.YUBINNO_S.getMaxlenTag()%>' data-options="required:true" style="width:60px;">
			</td>
			<td>
				<input class="easyui-textbox" tabindex="6" col="F6" id="<%=DefineReport.InpText.ADDRKN_T.getObj()%>" check='<%=DefineReport.InpText.ADDRKN_T.getMaxlenTag()%>' data-options="required:true" style="width:200px;">
			</td>
			<td>
				<input class="easyui-textbox" tabindex="7" col="F7" id="<%=DefineReport.InpText.ADDRKN_S.getObj()%>" check='<%=DefineReport.InpText.ADDRKN_S.getMaxlenTag()%>' data-options="required:true" style="width:200px;">
			</td>
			<td>
				<input class="easyui-textbox" tabindex="8" col="F8" id="<%=DefineReport.InpText.ADDRKN_M.getObj()%>" check='<%=DefineReport.InpText.ADDRKN_M.getMaxlenTag()%>' data-options="required:true" style="width:200px;">
				<input class="easyui-textbox" tabindex="9" col="F9" id="<%=DefineReport.InpText.ADDR_B.getObj()%>" check='<%=DefineReport.InpText.ADDR_B.getMaxlenTag()%>' data-options="required:true" style="width:200px;">
			</td>
		</tr>
	</table>
	<table>
		<tr>
			<td style="width:100px;">部署名（漢字）</td>
			<td>
				<input class="easyui-textbox" tabindex="10" col="F10" id="<%=DefineReport.InpText.BUSHOKN.getObj()%>" check='<%=DefineReport.InpText.BUSHOKN.getMaxlenTag()%>' style="width:300px;">
			</td>
		</tr>
	</table>
	<table>
		<tr>
			<td style="width:100px;">電話番号</td>
			<td>
				<input class="easyui-textbox" tabindex="11" col="F11" id="<%=DefineReport.InpText.TEL.getObj()%>" check='<%=DefineReport.InpText.TEL.getMaxlenTag()%>' style="width:100px;">
			</td>
			<td>内線番号</td>
			<td>
				<input class="easyui-textbox" tabindex="12" col="F12" id="<%=DefineReport.InpText.NAISEN.getObj()%>" check='<%=DefineReport.InpText.NAISEN.getMaxlenTag()%>' style="width:60px;">
			</td>
			<td>FAX番号</td>
			<td>
				<input class="easyui-textbox" tabindex="13" col="F13" id="<%=DefineReport.InpText.FAX.getObj()%>" check='<%=DefineReport.InpText.FAX.getMaxlenTag()%>' style="width:100px;">
			</td>
			<td>開始日</td>
			<td>
				<input class="easyui-numberbox" tabindex="14" col="F14" id="<%=DefineReport.InpText.STARTDT.getObj()%>" check='<%=DefineReport.InpText.STARTDT.getMaxlenTag()%>'data-options="prompt:'__/__/__'" data-options="required:true" style="width:70px;">
			</td>
		</tr>
		<tr>
			<td style="width:100px;">仕入先用途</td>
			<td>
				<select class="easyui-combobox_" tabindex="15" col="F15" id="<%=DefineReport.MeisyoSelect.KBN403.getObj()%>" style="width:170px;" data-options="required:true"></select>
			</td>
			<td>いなげや在庫</td>
			<td>
				<select class="easyui-combobox_" tabindex="16" col="F16" id="<%=DefineReport.MeisyoSelect.KBN404.getObj()%>" style="width:170px;" data-options="required:true"></select>
			</td>
			<td>EDI受信</td>
			<td>
				<select class="easyui-combobox_" tabindex="17" col="F17" id="<%=DefineReport.MeisyoSelect.KBN401.getObj()%>" style="width:170px;" data-options="required:true"></select>
			</td>
		</tr>
		<tr>
			<td style="width:100px;">買掛区分</td>
			<td>
				<select class="easyui-combobox_" tabindex="18" col="F18" id="<%=DefineReport.MeisyoSelect.KBN405.getObj()%>" style="width:170px;" data-options="required:true"></select>
			</td>
			<td>処理単価</td>
			<td>
				<input class="easyui-numberbox" tabindex="19" col="F19" id="<%=DefineReport.InpText.SYORTANKAAM.getObj()%>" check='<%=DefineReport.InpText.SYORTANKAAM.getMaxlenTag()%>' style="width:80px;text-align: right;">
			</td>
			<td>納税者番号</td>
			<td>
				<input class="easyui-numberbox" tabindex="20" col="F20" id="<%=DefineReport.InpText.NOZEISHANO.getObj()%>" check='<%=DefineReport.InpText.NOZEISHANO.getMaxlenTag()%>' style="width:180px;">
			</td>
		</tr>
	</table>
	<%-- <table>
		<tr>
			<td style="width:130px;">デフォルト一括区分</td>
			<td>
				<select class="easyui-combobox_" tabindex="21" col="F21" id="<%=DefineReport.MeisyoSelect.KBN405.getObj()%>"></select>
			</td>
		</tr>
	</table> --%>
	<table>
		<tr>
			<td style="vertical-align:top;">
				<table>
					<tr>
						<td style="width:130px;">デフォルト一括区分</td>
						<td>
							<select class="easyui-combobox_" tabindex="21" col="F21" id="<%=DefineReport.MeisyoSelect.KBN427.getObj()%>" style="width:170px;"></select>
						</td>
					</tr>
					<tr>
						<td style="width:130px;">代表仕入先コード</td>
						<td>
							<input class="easyui-numberbox" tabindex="22" col="F22" id="<%=DefineReport.InpText.DSIRCD.getObj()%>" check='<%=DefineReport.InpText.DSIRCD.getMaxlenTag()%>' style="width:60px;">
							<input class="easyui-textbox" tabindex="23" id="<%=DefineReport.InpText.SIRAN.getObj()%>_D" for_inp="<%=DefineReport.InpText.DSIRCD.getObj()%>_F3" check='<%=DefineReport.InpText.SIRAN.getMaxlenTag()%>'data-options="readonly:true,disabled:true" style="width:300px;">
						</td>
					</tr>
				</table>
			</td>
			<td>
				<fieldset>
				<legend>同報配信先</legend>
					<table>
						<tr>
							<td>伝票区分</td>
							<td>
								<select class="easyui-combobox_" tabindex="24" col="F24" id="<%=DefineReport.MeisyoSelect.KBN420.getObj()%>" data-options="required:true" style="width:170px;"></select>
							</td>
						</tr>
						<tr>
							<td>集計表</td>
							<td>
								<select class="easyui-combobox_" tabindex="25" col="F25" id="<%=DefineReport.MeisyoSelect.KBN421.getObj()%>" data-options="required:true" style="width:170px;"></select>
							</td>
						</tr>
						<tr>
							<td>ワッペン</td>
							<td>
								<select class="easyui-combobox_" tabindex="26" col="F26" id="<%=DefineReport.MeisyoSelect.KBN412.getObj()%>" data-options="required:true" style="width:170px;"></select>
							</td>
						</tr>
					</table>
				</fieldset>
			</td>
			<td>
				<table>
					<tr>
						<td>BMS対象区分</td>
						<td>
							<select class="easyui-combobox_" tabindex="27" col="F27" id="<%=DefineReport.MeisyoSelect.KBN428.getObj()%>" data-options="required:true" style="width:170px;"></select>
						</td>
					</tr>
					<tr>
						<td>自動検収区分</td>
						<td>
							<select class="easyui-combobox_" tabindex="28" col="F28" id="<%=DefineReport.MeisyoSelect.KBN429.getObj()%>" data-options="required:true" style="width:170px;"></select>
						</td>
					</tr>
					<tr>
						<td></td>
						<td>
							<input type="checkbox" tabindex="29" col="F29" id="chk_torihiki" />
							取引停止
						</td>
					</tr>
				</table>
			</td>
		</tr>
	</table>
	</div>
	<div title="配送パターン" style="display:none;padding:3px 8px;">
		<div style="height:29px;text-align: right;">
			<a href="#" class="easyui-linkbutton" tabindex="50" id="<%=DefineReport.Button.HSPTN.getObj()%>" title="<%=DefineReport.Button.HSPTN.getTxt()%>" iconCls="icon-search" style="width:120px;"><span>配送パターン</span></a>
		</div>
		<div style="height:300px;">
		<table id="<%=DefineReport.Grid.HSPTN.getObj()%>_list" class="easyui-datagrid" tabindex="51" data-options="singleSelect:true,rownumbers:true,fit:true"></table>
		</div>
		<%-- <div class="easyui-datagrid" tabindex="51" id="<%=DefineReport.Button.HSPTN.getObj()%>_list" data-options="fit:true"></div> --%>

		<div class="ref_editor" style="display: none;">
		<input  class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.HSPTN.getObj()%>" check='<%=DefineReport.InpText.HSPTN.getMaxlenTag()%>' />
		<input  class="easyui-textbox"   tabindex="-1" id="<%=DefineReport.InpText.HSPTNKN.getObj()%>" for_inp="<%=DefineReport.InpText.HSPTN.getObj()%>_F2" check='<%=DefineReport.InpText.HSPTNKN.getMaxlenTag()%>' />
		<input  class="easyui-textbox" tabindex="-1" id="<%=DefineReport.InpText.CENTERCD.getObj()%>" for_inp="<%=DefineReport.InpText.HSPTN.getObj()%>_F3" check='<%=DefineReport.InpText.CENTERCD.getMaxlenTag()%>'  />
		<input  class="easyui-textbox" tabindex="-1" id="<%=DefineReport.InpText.YCENTERCD.getObj()%>" for_inp="<%=DefineReport.InpText.HSPTN.getObj()%>_F4" check='<%=DefineReport.InpText.YCENTERCD.getMaxlenTag()%>'  />
		<!--名称マスタ項目については他の項目にて定義済み  -->
		<input  class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.RSIRCD.getObj()%>" check='<%=DefineReport.InpText.RSIRCD.getMaxlenTag()%>'/>
		<input  class="easyui-textbox"   tabindex="-1" id="<%=DefineReport.InpText.SIRKN.getObj()%>_r" for_inp="<%=DefineReport.InpText.RSIRCD.getObj()%>_F3" check='<%=DefineReport.InpText.TENGPKN.getMaxlenTag()%>' />

 		<select class="easyui-combobox" tabindex="-1" id="grd_<%=DefineReport.MeisyoSelect.KBN425.getObj()%>" style="width:170px;"></select>
		<select class="easyui-combobox" tabindex="-1" id="grd_<%=DefineReport.MeisyoSelect.KBN405.getObj()%>" style="width:170px;"></select>
		<select class="easyui-combobox" tabindex="-1" id="grd_<%=DefineReport.MeisyoSelect.KBN406.getObj()%>" style="width:170px;"></select>
		<select class="easyui-combobox" tabindex="-1" id="grd_<%=DefineReport.MeisyoSelect.KBN407.getObj()%>" style="width:170px;"></select>
		<select class="easyui-combobox" tabindex="-1" id="grd_<%=DefineReport.MeisyoSelect.KBN408.getObj()%>" style="width:170px;"></select>
		<select class="easyui-combobox" tabindex="-1" id="grd_<%=DefineReport.MeisyoSelect.KBN409.getObj()%>" style="width:170px;"></select>
		<select class="easyui-combobox" tabindex="-1" id="grd_<%=DefineReport.MeisyoSelect.KBN410.getObj()%>" style="width:170px;"></select>
		<select class="easyui-combobox" tabindex="-1" id="grd_<%=DefineReport.MeisyoSelect.KBN411.getObj()%>" style="width:170px;"></select>
		<select class="easyui-combobox" tabindex="-1" id="grd_<%=DefineReport.MeisyoSelect.KBN412.getObj()%>" style="width:170px;"></select>
		<select class="easyui-combobox" tabindex="-1" id="grd_<%=DefineReport.MeisyoSelect.KBN413.getObj()%>" style="width:170px;"></select>
		<select class="easyui-combobox" tabindex="-1" id="grd_<%=DefineReport.MeisyoSelect.KBN414.getObj()%>" style="width:170px;"></select>
		<select class="easyui-combobox" tabindex="-1" id="grd_<%=DefineReport.MeisyoSelect.KBN415.getObj()%>" style="width:170px;"></select>
		<select class="easyui-combobox" tabindex="-1" id="grd_<%=DefineReport.MeisyoSelect.KBN416.getObj()%>" style="width:170px;"></select>
		<select class="easyui-combobox" tabindex="-1" id="grd_<%=DefineReport.MeisyoSelect.KBN417.getObj()%>" style="width:170px;"></select>
		<select class="easyui-combobox" tabindex="-1" id="grd_<%=DefineReport.MeisyoSelect.KBN418.getObj()%>" style="width:170px;"></select>
		<select class="easyui-combobox" tabindex="-1" id="grd_<%=DefineReport.MeisyoSelect.KBN419.getObj()%>" style="width:170px;"></select>
		<select class="easyui-combobox" tabindex="-1" id="grd_<%=DefineReport.MeisyoSelect.KBN422.getObj()%>" style="width:170px;"></select>
		<select class="easyui-combobox" tabindex="-1" id="grd_<%=DefineReport.MeisyoSelect.KBN423.getObj()%>" style="width:170px;"></select>
		<select class="easyui-combobox" tabindex="-1" id="grd_<%=DefineReport.MeisyoSelect.KBN424.getObj()%>" style="width:170px;"></select>


		</div>
	</div>
	<div title="エリア別配送パターン" style="display:none;padding:3px 8px;">



	<div style="height:29px;text-align: right;">
			<a href="#" class="easyui-linkbutton" tabindex="52" id="<%=DefineReport.Button.EHSPTN.getObj()%>_list" title="<%=DefineReport.Button.EHSPTN.getTxt()%>" iconCls="icon-search" style="width:180px;float:right;"><span>エリア別配送パターン</span></a>
	</div>
	<div style="height:300px;">
		<table id="<%=DefineReport.Grid.EHSPTN.getObj()%>_list" class="easyui-datagrid" tabindex="53" data-options="singleSelect:true,rownumbers:true,fit:true"></table>
		</div>
		<%-- <div class="easyui-datagrid" tabindex="52" id="<%=DefineReport.Button.EHSPTN.getObj()%>_list" data-options="singleSelect:true,rownumbers:true"></div> --%>

		<div class="ref_editor" style="display: none;">
		<input  class="easyui-numberbox" tabindex="-1" id="<%=DefineReport.InpText.TENGPCD.getObj()%>" check='<%=DefineReport.InpText.TENGPCD.getMaxlenTag()%>' />
		<input  class="easyui-textbox"   tabindex="-1" id="<%=DefineReport.InpText.TENGPKN.getObj()%>" check='<%=DefineReport.InpText.TENGPKN.getMaxlenTag()%>' for_inp="<%=DefineReport.InpText.TENGPCD.getObj()%>_F3" />
		<!--一部の入力項目については配送パターンにて定義済み  -->
		<!--名称マスタ項目については他の項目にて定義済み  -->
		<!--[実仕入先名称]項目については他の項目にて定義済み  -->

		</div>
	</div>
	</div>
	<div style="height:10px;"></div>
	<fieldset>
		<legend>配送パターン　デフォルト設定</legend>
		<table>
			<tr>
				<td>
				<table>
				<tr>
					<td>計算センター</td>
					<td>
						<select class="easyui-combobox_" tabindex="30" col="F30" id="<%=DefineReport.MeisyoSelect.KBN406.getObj()%>" data-options="required:true" style="width:170px;"></select>
					</td>
					<td>伝票区分</td>
					<td>
						<select class="easyui-combobox_" tabindex="31" col="F31" id="<%=DefineReport.MeisyoSelect.KBN408.getObj()%>" data-options="required:true" style="width:170px;"></select>
					</td>
				</tr>
				<tr>
					<td>店別伝票フラグ</td>
					<td>
						<select class="easyui-combobox_" tabindex="32" col="F32" id="<%=DefineReport.MeisyoSelect.KBN425.getObj()%>" data-options="required:true" style="width:170px;"></select>
					</td>
					<td>集計表</td>
					<td>
						<select class="easyui-combobox_" tabindex="33" col="F33" id="<%=DefineReport.MeisyoSelect.KBN409.getObj()%>" data-options="required:true" style="width:170px;"></select>
					</td>
				</tr>
				<tr>
					<td>運用区分</td>
					<td>
						<select class="easyui-combobox_" tabindex="34" col="F34" id="<%=DefineReport.MeisyoSelect.KBN407.getObj()%>" data-options="required:true" style="width:170px;"></select>
					</td>
					<td>ピッキングデータ</td>
					<td>
						<select class="easyui-combobox_" tabindex="35" col="F35" id="<%=DefineReport.MeisyoSelect.KBN410.getObj()%>" data-options="required:true" style="width:170px;"></select>
					</td>
				</tr>
				<tr>
					<td>流通区分</td>
					<td>
						<select class="easyui-combobox_" tabindex="36" col="F36" id="<%=DefineReport.MeisyoSelect.KBN415.getObj()%>" data-options="required:true" style="width:170px;"></select>
					</td>
					<td>ピッキングリスト</td>
					<td>
						<select class="easyui-combobox_" tabindex="37" col="F37" id="<%=DefineReport.MeisyoSelect.KBN411.getObj()%>" data-options="required:true" style="width:170px;"></select>
					</td>
				</tr>
				<tr>
					<td></td>
					<td></td>
					<td>ワッペン</td>
					<td>
						<select class="easyui-combobox_" tabindex="38" col="F38" id="<%=DefineReport.MeisyoSelect.KBN412.getObj()%>_hptnd" data-options="required:true" style="width:170px;"></select>
					</td>
				</tr>
				<tr>
					<td></td>
					<td></td>
					<td>一括伝票</td>
					<td>
						<select class="easyui-combobox_" tabindex="39" col="F39" id="<%=DefineReport.MeisyoSelect.KBN413.getObj()%>" data-options="required:true" style="width:170px;"></select>
					</td>
				</tr>
				<tr>
					<td></td>
					<td></td>
					<td>加工指示</td>
					<td>
						<select class="easyui-combobox_" tabindex="40" col="F40" id="<%=DefineReport.MeisyoSelect.KBN414.getObj()%>" data-options="required:true" style="width:170px;"></select>
					</td>
				</tr>
				<%-- <tr>
					<td></td>
					<td></td>
					<td>実仕入先コード</td>
					<td>
						<input class="easyui-numberbox" tabindex="41" col="F41" id="<%=DefineReport.InpText.DF_RSIRCD.getObj()%>" check='<%=DefineReport.InpText.RSIRCD.getMaxlenTag()%>' style="width:60px;">
						<input class="easyui-textbox" tabindex="42" id="<%=DefineReport.InpText.SIRAN.getObj()%>_DF" for_inp="<%=DefineReport.InpText.DF_RSIRCD.getObj()%>_F3" check='<%=DefineReport.InpText.SIRAN.getMaxlenTag()%>' data-options="readonly:true,disabled:true" style="width:300px;">
					</td>
				</tr> --%>
			</table>
			</td>
			<td>
				<div id="tt_2" class="easyui-tabs" data-options="plain:true,narrow:true,justified:true" style="height:140px;width:370px;">
				<div title="在庫内訳">
					<table>
						<tr>
							<td>伝票区分</td>
							<td>
								<select class="easyui-combobox_" tabindex="43" col="F43" id="<%=DefineReport.MeisyoSelect.KBN416.getObj()%>" data-options="required:true" style="width:170px;"></select>
							</td>
						</tr>
						<tr>
							<td>集計表</td>
							<td>
								<select class="easyui-combobox_" tabindex="44" col="F44" id="<%=DefineReport.MeisyoSelect.KBN417.getObj()%>" data-options="required:true" style="width:170px;"></select>
							</td>
						</tr>
						<tr>
							<td>ピッキングデータ</td>
							<td>
								<select class="easyui-combobox_" tabindex="45" col="F45" id="<%=DefineReport.MeisyoSelect.KBN418.getObj()%>" data-options="required:true" style="width:170px;"></select>
							</td>
						</tr>
						<tr>
							<td>ピッキングリスト</td>
							<td>
								<select class="easyui-combobox_" tabindex="46" col="F46" id="<%=DefineReport.MeisyoSelect.KBN419.getObj()%>"  data-options="required:true" style="width:170px;"></select>
							</td>
						</tr>
					</table>
				</div>
				<div title="横持先センター">
					<table>
						<tr>
							<td>検収区分</td>
							<td>
								<select class="easyui-combobox_" tabindex="47" col="F47" id="<%=DefineReport.MeisyoSelect.KBN422.getObj()%>" data-options="required:true" style="width:170px;"></select>
							</td>
						</tr>
						<tr>
							<td>伝票区分</td>
							<td>
								<select class="easyui-combobox_" tabindex="48" col="F48" id="<%=DefineReport.MeisyoSelect.KBN423.getObj()%>" data-options="required:true" style="width:170px;"></select>
							</td>
						</tr>
						<tr>
							<td>集計表</td>
							<td>
								<select class="easyui-combobox_" tabindex="49" col="F49" id="<%=DefineReport.MeisyoSelect.KBN424.getObj()%>" data-options="required:true" style="width:170px;"></select>
							</td>
						</tr>
					</table>
				</div>
			</div>
			</td>
		</tr>
		<tr>
			<td colspan = '2'>
			<table>
				<tr>
					<td></td>
					<td style="width:254px;"></td>
					<td style="width:96px;">実仕入先コード</td>
					<td>
						<input class="easyui-numberbox" tabindex="41" col="F41" id="<%=DefineReport.InpText.DF_RSIRCD.getObj()%>" check='<%=DefineReport.InpText.RSIRCD.getMaxlenTag()%>' style="width:60px;">
						<input class="easyui-textbox" tabindex="42" id="<%=DefineReport.InpText.SIRAN.getObj()%>_DF" for_inp="<%=DefineReport.InpText.DF_RSIRCD.getObj()%>_F3" check='<%=DefineReport.InpText.SIRAN.getMaxlenTag()%>' data-options="readonly:true,disabled:true" style="width:300px;">
					</td>
				</tr>
			</table>
			</td>
		</tr>
		</table>
		</fieldset>
	</div>
</form>
<div id="buttons" data-options="region:'south',border:false" style="display:none;height:30px;padding:0 30px;text-align: center;">
	<div class="btn" style="float: left;">
		<table class="t-layout3">
		<tr>
		<th style="min-width:135px;"></th><th style="min-width:10px;"></th>
		<th style="min-width:135px;"></th>
		<th style="min-width:135px;"></th>
		</tr>
		<tr>
		<td><a href="#" title="戻る" id="<%=DefineReport.Button.BACK.getObj()%>" class="easyui-linkbutton" tabindex="54" iconCls="icon-undo" style="width:125px;"><span class="btnTxt">戻る</span></a></td>
		<td></td>
		<td><a href="#" class="easyui-linkbutton" tabindex="55" id="<%=DefineReport.Button.CANCEL.getObj()%>" title="<%=DefineReport.Button.CANCEL.getTxt()%>" iconCls="icon-cancel" style="width:125px;"><%=DefineReport.Button.CANCEL.getTxt()%></a></td>
		<td><a href="#" class="easyui-linkbutton" tabindex="56" id="<%=DefineReport.Button.UPD.getObj()%>" title="<%=DefineReport.Button.UPD.getTxt()%>" iconCls="icon-save" style="width:125px;"><span class="btnTxt"><%=DefineReport.Button.UPD.getTxt()%></span></a></td>
		</tr>
		</table>
	</div>
	<div id="disp_record_info" style="float: right;">
		<span class="labelName" tabindex="-1" style="padding-top: 3px;">
				登録日 <span col="F50" id="<%=DefineReport.Text.ADDDT.getObj()%>"></span>　更新日 <span col="F51" id="<%=DefineReport.Text.UPDDT.getObj()%>"></span>　オペレータ <span col="F52" id="<%=DefineReport.Text.OPERATOR.getObj()%>"></span>
		</span>
		<%-- <input type="hidden" col="F10" name="<%=DefineReport.Hidden.UPDDT.getObj()%>" id="<%=DefineReport.Hidden.UPDDT.getObj()%>" /> --%>
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

	<input type="hidden" name="sendBtnid" id="sendBtnid" value='' />
</div>
<div style="display:none;">
	<jsp:include page="Out_Reportwin004.jsp" flush="true" />
	<jsp:include page="Out_Reportwin005.jsp" flush="true" />
	<jsp:include page="Out_Reportwin008.jsp" flush="true" />
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
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.x152.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.win004.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.win005.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.win008.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.control.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.events.js?v=<%=prm %>"></script>
</html>