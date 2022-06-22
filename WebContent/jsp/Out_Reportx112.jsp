
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
<div data-options="region:'north',border:false" style="display:none;height:90px;padding:2px 5px 0;overflow: visible;" >
	<div class="lbl_box" style="width:930px;">
		<table>
		<tr style="vertical-align: top;">
			<td>店コード</td>
			<td><input class="easyui-numberbox" tabindex="1" col="F1" id="<%=DefineReport.InpText.TENCD.getObj()%>" check='<%=DefineReport.InpText.TENCD.getMaxlenTag()%>' data-options="required:true" style="width:70px;"></td>
			<td><div style="width: 10px;"></div></td>
			<td><fieldset><legend>店舗名</legend>
				<table>
				<tr>
					<td>（漢字）</td>
					<td><input class="easyui-textbox" tabindex="2" col="F14" id="<%=DefineReport.InpText.TENKN.getObj()%>" check='<%=DefineReport.InpText.TENKN.getMaxlenTag()%>' data-options="required:true" style="width:300px;">店</td>
				</tr>
				<tr>
					<td>（カナ）</td>
					<td><input class="easyui-textbox" tabindex="3" col="F15" id="<%=DefineReport.InpText.TENAN.getObj()%>" check='<%=DefineReport.InpText.TENAN.getMaxlenTag()%>' data-options="required:true" style="width:300px;">ﾃﾝ</td>
				</tr>
				</table>
			</fieldset></td>
		</tr>
		</table>
	</div>
</div>
<div data-options="region:'center',border:false" style="display:none;">
	<div id="tt" class="easyui-tabs" data-options="plain:true,narrow:true,justified:true,fit:true">
	<div title="基本情報" style="display:none;padding:3px 8px;" data-options="selected:true">
		<table>
		<tr><th style="max-width:200px;"></th><th style="width:10px;"></th><th style="max-width:300px;"></th><th style="width:10px;"></th><th style="max-width:350px;"></th></tr>
		<tr style="vertical-align: top;">
		<td>
			<table>
			<tr>
				<td style="width: 110px;">開設日</td>
				<td><input class="easyui-numberbox" tabindex="1001" col="F5" id="<%=DefineReport.InpText.TENOPENDT.getObj()%>" check='<%=DefineReport.InpText.TENOPENDT.getMaxlenTag()%>' data-options="required:true,prompt:'__/__/__'" style="width:70px;"></td>
			</tr>
			<tr>
				<td>閉鎖日</td>
				<td><input class="easyui-numberbox" tabindex="1002" col="F6" id="<%=DefineReport.InpText.TENCLOSEDT.getObj()%>" check='<%=DefineReport.InpText.TENCLOSEDT.getMaxlenTag()%>' data-options="prompt:'__/__/__'" style="width:70px;"></td>
			</tr>
			<tr>
				<td>改装日（1）</td>
				<td><input class="easyui-numberbox" tabindex="1003" col="F7" id="<%=DefineReport.InpText.KAISODT1.getObj()%>" check='<%=DefineReport.InpText.KAISODT1.getMaxlenTag()%>' data-options="prompt:'__/__/__'" style="width:70px;"></td>
			</tr>
			<tr>
				<td>　　　（2）</td>
				<td><input class="easyui-numberbox" tabindex="1004" col="F8" id="<%=DefineReport.InpText.KAISODT2.getObj()%>" check='<%=DefineReport.InpText.KAISODT2.getMaxlenTag()%>' data-options="prompt:'__/__/__'" style="width:70px;"></td>
			</tr>
			<tr>
				<td>店舗年齢</td>
				<td><input class="easyui-numberbox" tabindex="1006" col="F83" id="<%=DefineReport.InpText.TEN_NENSU.getObj()%>" check='<%=DefineReport.InpText.TEN_NENSU.getMaxlenTag()%>' style="width:60px;text-align: right;"></td>
			</tr>
			<tr>
				<td>日商</td>
				<td><input class="easyui-numberbox" tabindex="1007" col="F84" id="<%=DefineReport.InpText.NISSYOU.getObj()%>" check='<%=DefineReport.InpText.NISSYOU.getMaxlenTag()%>' style="width:60px;text-align: right;"></td>
			</tr>
			<tr>
				<td>売上前比</td>
				<td><input class="easyui-numberbox" tabindex="1008" col="F85" id="<%=DefineReport.InpText.URIAGEZENHI.getObj()%>" check='<%=DefineReport.InpText.URIAGEZENHI.getMaxlenTag()%>' style="width:60px;text-align: right;"></td>
			</tr>
			<tr>
				<td>荒利率</td>
				<td><input class="easyui-numberbox" tabindex="1009" col="F86" id="<%=DefineReport.InpText.ARARIRITU.getObj()%>" check='<%=DefineReport.InpText.ARARIRITU.getMaxlenTag()%>' style="width:60px;text-align: right;"></td>
			</tr>
			<tr>
				<td colspan="2"><fieldset><legend>営業時間</legend>
					<table>
					<tr style="vertical-align: top;">
						<td style="width: 55px;">&nbsp;</td>
						<td rowspan="5"><fieldset><legend>１</legend>
							<table>
							<tr>
								<td><input class="easyui-numberbox" tabindex="1111" col="F50" id="<%=DefineReport.InpText.EGYOTM1_STMD.getObj()%>" check='<%=DefineReport.InpText.EGYOTM1_STMD.getMaxlenTag()%>' data-options="prompt:'__/__'" style="width:50px;"></td>
							</tr>
							<tr>
								<td><input class="easyui-numberbox" tabindex="1112" col="F51" id="<%=DefineReport.InpText.EGYOTM1_EDMD.getObj()%>" check='<%=DefineReport.InpText.EGYOTM1_EDMD.getMaxlenTag()%>' data-options="prompt:'__/__'" style="width:50px;"></td>
							</tr>
							<tr>
								<td><input class="easyui-numberbox" tabindex="1113" col="F52" id="<%=DefineReport.InpText.EGYOTM1_STHM.getObj()%>" check='<%=DefineReport.InpText.EGYOTM1_STHM.getMaxlenTag()%>' data-options="prompt:'__:__'" style="width:50px;"></td>
							</tr>
							<tr>
								<td><input class="easyui-numberbox" tabindex="1114" col="F53" id="<%=DefineReport.InpText.EGYOTM1_EDHM.getObj()%>" check='<%=DefineReport.InpText.EGYOTM1_EDHM.getMaxlenTag()%>' data-options="prompt:'__:__'" style="width:50px;"></td>
							</tr>
							</table>
						</fieldset></td>
						<td rowspan="5"><fieldset><legend>２</legend>
							<table>
							<tr>
								<td><input class="easyui-numberbox" tabindex="1121" col="F54" id="<%=DefineReport.InpText.EGYOTM2_STMD.getObj()%>" check='<%=DefineReport.InpText.EGYOTM2_STMD.getMaxlenTag()%>' data-options="prompt:'__/__'" style="width:50px;"></td>
							</tr>
							<tr>
								<td><input class="easyui-numberbox" tabindex="1122" col="F55" id="<%=DefineReport.InpText.EGYOTM2_EDMD.getObj()%>" check='<%=DefineReport.InpText.EGYOTM2_EDMD.getMaxlenTag()%>' data-options="prompt:'__/__'" style="width:50px;"></td>
							</tr>
							<tr>
								<td><input class="easyui-numberbox" tabindex="1123" col="F56" id="<%=DefineReport.InpText.EGYOTM2_STHM.getObj()%>" check='<%=DefineReport.InpText.EGYOTM2_STHM.getMaxlenTag()%>' data-options="prompt:'__:__'" style="width:50px;"></td>
							</tr>
							<tr>
								<td><input class="easyui-numberbox" tabindex="1124" col="F57" id="<%=DefineReport.InpText.EGYOTM2_EDHM.getObj()%>" check='<%=DefineReport.InpText.EGYOTM2_EDHM.getMaxlenTag()%>' data-options="prompt:'__:__'" style="width:50px;"></td>
							</tr>
							</table>
						</fieldset></td>
					</tr>
					<tr>
						<td>開始月日</td>
					</tr>
					<tr>
						<td>終了月日</td>
					</tr>
					<tr>
						<td>開店時間</td>
					</tr>
					<tr>
						<td>閉店時間</td>
					</tr>
					</table>
				</fieldset></td>
			</tr>
			</table>
		</td>
		<td></td>
		<td>
			<table>
			<tr>
				<td>青果センターエリア</td>
				<td><select class="easyui-combobox" tabindex="1101" col="F2" id="<%=DefineReport.MeisyoSelect.KBN301.getObj()%>" data-options="required:true" style="width:170px;"></select></td>
			</tr>
			<tr>
				<td>鮮魚区分</td>
				<td><select class="easyui-combobox" tabindex="1102" col="F3" id="<%=DefineReport.MeisyoSelect.KBN302.getObj()%>" data-options="required:true" style="width:170px;"></select></td>
			</tr>
			<tr>
				<td>精肉区分</td>
				<td><select class="easyui-combobox" tabindex="1103" col="F4" id="<%=DefineReport.MeisyoSelect.KBN303.getObj()%>" data-options="required:true" style="width:170px;"></select></td>
			</tr>
			<tr>
				<td>予算区分</td>
				<td><select class="easyui-combobox" tabindex="1104" col="F13" id="<%=DefineReport.MeisyoSelect.KBN310.getObj()%>" data-options="required:true" style="width:170px;"></select></td>
			</tr>
			<tr>
				<td>青果市場コード</td>
				<td><select class="easyui-combobox" tabindex="1105" col="F80" id="<%=DefineReport.MeisyoSelect.KBN314.getObj()%>" data-options="required:true" style="width:170px;"></select></td>
			</tr>
			<tr>
				<td>実働人員</td>
				<td><input class="easyui-numberbox" tabindex="1106" col="F81" id="<%=DefineReport.InpText.STAFFSU.getObj()%>" check='<%=DefineReport.InpText.STAFFSU.getMaxlenTag()%>' style="width:80px; text-align: right;"></td>
			</tr>
			<tr>
				<td>店運用区分</td>
				<td><select class="easyui-combobox" tabindex="1107" col="F82" id="<%=DefineReport.MeisyoSelect.KBN313.getObj()%>" data-options="required:true" style="width:170px;"></select></td>
			</tr>
			<tr>
				<td colspan="2"><fieldset><legend>什器</legend>
					<table>
					<tr>
						<td style="width: 100px;">冷設</td>
						<td><input class="easyui-textbox" tabindex="1125" col="F87" id="<%=DefineReport.InpText.REISETU.getObj()%>" check='<%=DefineReport.InpText.REISETU.getMaxlenTag()%>' style="width:260px;"></td>
					</tr>
					<tr>
						<td>惣菜</td>
						<td><input class="easyui-textbox" tabindex="1126" col="F88" id="<%=DefineReport.InpText.SOUZAI.getObj()%>" check='<%=DefineReport.InpText.SOUZAI.getMaxlenTag()%>' style="width:260px;"></td>
					</tr>
					<tr>
						<td>ゴンドラ</td>
						<td><input class="easyui-textbox" tabindex="1127" col="F89" id="<%=DefineReport.InpText.GONDORA.getObj()%>" check='<%=DefineReport.InpText.GONDORA.getMaxlenTag()%>' style="width:260px;"></td>
					</tr>
					</table>
				</fieldset></td>
			</tr>
			</table>
		</td>
		<td></td>
		<td>
			<table>
			<tr>
				<td colspan="2"><fieldset><legend>付加サービス</legend>
					<table>
					<tr>
						<td>ingfanカード</td>
						<td><select class="easyui-combobox" tabindex="1212" col="F90" id="<%=DefineReport.MeisyoSelect.KBN331.getObj()%>" data-options="required:true" style="width:170px;"></select></td>
					</tr>
					<tr>
						<td>ピュアウォーター</td>
						<td><select class="easyui-combobox" tabindex="1213" col="F91" id="<%=DefineReport.MeisyoSelect.KBN332.getObj()%>" data-options="required:true" style="width:170px;"></select></td>
					</tr>
					<tr>
						<td>ATM</td>
						<td><select class="easyui-combobox" tabindex="1214" col="F92" id="<%=DefineReport.MeisyoSelect.KBN333.getObj()%>" data-options="required:true" style="width:170px;"></select></td>
					</tr>
					<tr>
						<td>お客様お会計レジ</td>
						<td><select class="easyui-combobox" tabindex="1215" col="F93" id="<%=DefineReport.MeisyoSelect.KBN334.getObj()%>" data-options="required:true" style="width:170px;"></select></td>
					</tr>
					<tr>
						<td>ドライアイス</td>
						<td><select class="easyui-combobox" tabindex="1216" col="F94" id="<%=DefineReport.MeisyoSelect.KBN335.getObj()%>" data-options="required:true" style="width:170px;"></select></td>
					</tr>
					<tr>
						<td>証明写真</td>
						<td><select class="easyui-combobox" tabindex="1217" col="F95" id="<%=DefineReport.MeisyoSelect.KBN336.getObj()%>" data-options="required:true" style="width:170px;"></select></td>
					</tr>
					<tr>
						<td>DPE</td>
						<td><select class="easyui-combobox" tabindex="1218" col="F96" id="<%=DefineReport.MeisyoSelect.KBN337.getObj()%>" data-options="required:true" style="width:170px;"></select></td>
					</tr>
					<tr>
						<td>お届けサービス</td>
						<td><select class="easyui-combobox" tabindex="1219" col="F97" id="<%=DefineReport.MeisyoSelect.KBN338.getObj()%>" data-options="required:true" style="width:170px;"></select></td>
					</tr>
					<tr>
						<td>電子マネー</td>
						<td><select class="easyui-combobox" tabindex="1220" col="F98" id="<%=DefineReport.MeisyoSelect.KBN339.getObj()%>" data-options="required:true" style="width:170px;"></select></td>
					</tr>
					<tr>
						<td>ペット減容器</td>
						<td><select class="easyui-combobox" tabindex="1221" col="F99" id="<%=DefineReport.MeisyoSelect.KBN340.getObj()%>" data-options="required:true" style="width:170px;"></select></td>
					</tr>
					<tr>
						<td>AED</td>
						<td><select class="easyui-textbox" tabindex="1222" col="F100" id="<%=DefineReport.InpText.AED.getObj()%>" check='<%=DefineReport.InpText.AED.getMaxlenTag()%>' style="width:260px;"></select></td>
					</tr>
					<tr>
						<td>くつろぎスペース</td>
						<td><select class="easyui-combobox" tabindex="1223" col="F101" id="<%=DefineReport.MeisyoSelect.KBN341.getObj()%>" data-options="required:true" style="width:170px;"></select></td>
					</tr>
					</table>
				</fieldset></td>
			</tr>
			</table>
		</td>
		</tr>
		</table>
	</div>

	<div title="所在地・連絡先" style="display:none;padding:3px 8px;">
		<table>
		<tr><th style="width:100px;"></th><th style="max-width:300px;"></th><th style="width:30px;"></th><th style="max-width:300px;"></th><th style="width:30px;"></th><th style="max-width:300px;"><th style="max-width:200px;"></th></tr>
		<tr>
			<td colspan="7"><fieldset><legend>住所</legend>
				<table>
				<tr>
					<td style="text-align: right;">〒</td>
					<td>
						<input class="easyui-numberbox" tabindex="2001" col="F16" id="<%=DefineReport.InpText.YUBINNO_U.getObj()%>" check='<%=DefineReport.InpText.YUBINNO_U.getMaxlenTag()%>' style="width:50px;">
						<span>-</span>
						<input class="easyui-numberbox" tabindex="2002" col="F17" id="<%=DefineReport.InpText.YUBINNO_S.getObj()%>" check='<%=DefineReport.InpText.YUBINNO_S.getMaxlenTag()%>' style="width:50px;">
					</td>
				</tr>
				<tr>
					<td>（カナ）</td>
					<td><input class="easyui-textbox" tabindex="2003" col="F22" id="<%=DefineReport.InpText.ADDRAN_T.getObj()%>" check='<%=DefineReport.InpText.ADDRAN_T.getMaxlenTag()%>' style="width:100px;"></td>
					<td><input class="easyui-textbox" tabindex="2004" col="F23" id="<%=DefineReport.InpText.ADDRAN_S.getObj()%>" check='<%=DefineReport.InpText.ADDRAN_S.getMaxlenTag()%>' style="width:200px;"></td>
					<td><input class="easyui-textbox" tabindex="2005" col="F24" id="<%=DefineReport.InpText.ADDRAN_M.getObj()%>" check='<%=DefineReport.InpText.ADDRAN_M.getMaxlenTag()%>' style="width:100px;"></td>
					<td><input class="easyui-textbox" tabindex="2006" col="F25" id="<%=DefineReport.InpText.ADDRAN_B.getObj()%>" check='<%=DefineReport.InpText.ADDRAN_B.getMaxlenTag()%>' style="width:380px;"></td>
				</tr>
				<tr>
					<td></td>
					<td style="text-align: right;">都道府県</td>
					<td style="text-align: right;">市区町村</td>
					<td style="text-align: right;">町・字</td>
					<td style="text-align: right;"></td>
				</tr>
				<tr>
					<td>（漢字）</td>
					<td><input class="easyui-textbox" tabindex="2007" col="F18" id="<%=DefineReport.InpText.ADDRKN_T.getObj()%>" check='<%=DefineReport.InpText.ADDRKN_T.getMaxlenTag()%>' style="width:100px;"></td>
					<td><input class="easyui-textbox" tabindex="2008" col="F19" id="<%=DefineReport.InpText.ADDRKN_S.getObj()%>" check='<%=DefineReport.InpText.ADDRKN_S.getMaxlenTag()%>' style="width:200px;"></td>
					<td><input class="easyui-textbox" tabindex="2009" col="F20" id="<%=DefineReport.InpText.ADDRKN_M.getObj()%>" check='<%=DefineReport.InpText.ADDRKN_M.getMaxlenTag()%>' style="width:100px;"></td>
					<td><input class="easyui-textbox" tabindex="2010" col="F21" id="<%=DefineReport.InpText.ADDRKN_B.getObj()%>" check='<%=DefineReport.InpText.ADDRKN_B.getMaxlenTag()%>' style="width:380px;"></td>
				</tr>
				</table>
			</fieldset></td>
		</tr>
		<tr>
			<td>&nbsp;</td>
		</tr>
		<tr style="vertical-align: top;">
			<td></td>
			<td>
				<table>
				<tr>
					<td>最寄り駅</td>
					<td><input class="easyui-textbox" tabindex="2101" col="F26" id="<%=DefineReport.InpText.MOYORIEKIKN.getObj()%>" check='<%=DefineReport.InpText.MOYORIEKIKN.getMaxlenTag()%>' style="width:200px;"></td>
				</tr>
				<tr>
					<td>バス停</td>
					<td><input class="easyui-textbox" tabindex="2102" col="F27" id="<%=DefineReport.InpText.BUSSTOPKN.getObj()%>" check='<%=DefineReport.InpText.BUSSTOPKN.getMaxlenTag()%>' style="width:200px;"></td>
				</tr>
				<tr>
					<td>販売部</td>
					<td><select class="easyui-combobox" tabindex="2103" col="F9" id="<%=DefineReport.MeisyoSelect.KBN324.getObj()%>" data-options="required:true" style="width:170px;"></select></td>
				</tr>
				<tr>
					<td>地区</td>
					<td><select class="easyui-combobox" tabindex="2104" col="F10" id="<%=DefineReport.MeisyoSelect.KBN325.getObj()%>" data-options="required:true" style="width:170px;"></select></td>
				</tr>
				<tr>
					<td>販売エリア</td>
					<td><input class="easyui-numberbox" tabindex="2105" col="F11" id="<%=DefineReport.InpText.URIAERACD.getObj()%>" check='<%=DefineReport.InpText.URIAERACD.getMaxlenTag()%>' style="width:30px;text-align: right;"></td>
				</tr>
				<tr>
					<td>地域</td>
					<td><input class="easyui-numberbox" tabindex="2106" col="F12" id="<%=DefineReport.InpText.CHIIKICD.getObj()%>" check='<%=DefineReport.InpText.CHIIKICD.getMaxlenTag()%>' style="width:30px;text-align: right;"></td>
				</tr>
				<tr>
					<td>モデル店</td>
					<td><input class="easyui-numberbox" tabindex="2107" col="F102" id="<%=DefineReport.InpText.MODELTEN.getObj()%>" check='<%=DefineReport.InpText.MODELTEN.getMaxlenTag()%>' style="width:40px;text-align: right;"></td>
				</tr>
				</table>
			</td>
			<td></td>
			<td><fieldset><legend>電話番号</legend>
				<table>
				<tr>
					<td>１</td>
					<td><input class="easyui-textbox" tabindex="2201" col="F43" id="<%=DefineReport.InpText.TEL1.getObj()%>" check='<%=DefineReport.InpText.TEL1.getMaxlenTag()%>' style="width:120px;"></td>
				</tr>
				<tr>
					<td>２</td>
					<td><input class="easyui-textbox" tabindex="2202" col="F44" id="<%=DefineReport.InpText.TEL2.getObj()%>" check='<%=DefineReport.InpText.TEL2.getMaxlenTag()%>' style="width:120px;"></td>
				</tr>
				<tr>
					<td>３</td>
					<td><input class="easyui-textbox" tabindex="2203" col="F45" id="<%=DefineReport.InpText.TEL3.getObj()%>" check='<%=DefineReport.InpText.TEL3.getMaxlenTag()%>' style="width:120px;"></td>
				</tr>
				<tr>
					<td>４</td>
					<td><input class="easyui-textbox" tabindex="2204" col="F46" id="<%=DefineReport.InpText.TEL4.getObj()%>" check='<%=DefineReport.InpText.TEL4.getMaxlenTag()%>' style="width:120px;"></td>
				</tr>
				<tr>
					<td>５</td>
					<td><input class="easyui-textbox" tabindex="2205" col="F47" id="<%=DefineReport.InpText.TEL5.getObj()%>" check='<%=DefineReport.InpText.TEL5.getMaxlenTag()%>' style="width:120px;"></td>
				</tr>
				</table>
			</fieldset>
			<fieldset><legend>FAX番号</legend>
				<table>
				<tr>
					<td>１</td>
					<td><input class="easyui-textbox" tabindex="2301" col="F48" id="<%=DefineReport.InpText.FAX1.getObj()%>" check='<%=DefineReport.InpText.FAX1.getMaxlenTag()%>' style="width:120px;"></td>
				</tr>
				<tr>
					<td>２</td>
					<td><input class="easyui-textbox" tabindex="2302" col="F49" id="<%=DefineReport.InpText.FAX2.getObj()%>" check='<%=DefineReport.InpText.FAX2.getMaxlenTag()%>' style="width:120px;"></td>
				</tr>
				</table>
			</fieldset>
			</td>
			<td></td>
			<td><fieldset><legend>競合店</legend>
				<table>
				<tr>
					<td>１位</td>
					<td><input class="easyui-textbox" tabindex="2401" col="F103" id="<%=DefineReport.InpText.TEN1.getObj()%>" check='<%=DefineReport.InpText.TEN1.getMaxlenTag()%>' style="width:260px;"></td>
				</tr>
				<tr>
					<td>２位</td>
					<td><input class="easyui-textbox" tabindex="2402" col="F104" id="<%=DefineReport.InpText.TEN2.getObj()%>" check='<%=DefineReport.InpText.TEN2.getMaxlenTag()%>' style="width:260px;"></td>
				</tr>
				<tr>
					<td>３位</td>
					<td><input class="easyui-textbox" tabindex="2403" col="F105" id="<%=DefineReport.InpText.TEN3.getObj()%>" check='<%=DefineReport.InpText.TEN3.getMaxlenTag()%>' style="width:260px;"></td>
				</tr>
				<tr>
					<td>４位</td>
					<td><input class="easyui-textbox" tabindex="2404" col="F106" id="<%=DefineReport.InpText.TEN4.getObj()%>" check='<%=DefineReport.InpText.TEN4.getMaxlenTag()%>' style="width:260px;"></td>
				</tr>
				<tr>
					<td>５位</td>
					<td><input class="easyui-textbox" tabindex="2405" col="F107" id="<%=DefineReport.InpText.TEN5.getObj()%>" check='<%=DefineReport.InpText.TEN5.getMaxlenTag()%>' style="width:260px;"></td>
				</tr>
				</table>
			</fieldset></td>
			<td></td>
		</tr>
		</table>
	</div>

	<div title="その他" style="display:none;padding:3px 8px;">
		<table>
		<tr><th style="max-width:280px;"></th><th style="width:10px;"></th><th style="max-width:110px;"></th><th style="width:10px;"></th><th style="max-width:550px;"></th></tr>
		<tr style="vertical-align: top;">
			<td><fieldset><legend>駐車台数</legend>
				<table>
				<tr>
					<td></td>
					<td>普通車</td>
					<td>軽</td>
					<td>障害者</td>
				</tr>
				<tr>
					<td>敷地内</td>
					<td><input class="easyui-numberbox" tabindex="3001" col="F69" id="<%=DefineReport.InpText.PARK_NM_BA.getObj()%>" check='<%=DefineReport.InpText.PARK_NM_BA.getMaxlenTag()%>' style="width:70px; text-align: right;"></td>
					<td><input class="easyui-numberbox" tabindex="3006" col="F72" id="<%=DefineReport.InpText.PARK_LT_BA.getObj()%>" check='<%=DefineReport.InpText.PARK_LT_BA.getMaxlenTag()%>' style="width:70px; text-align: right;"></td>
					<td><input class="easyui-numberbox" tabindex="3009" col="F75" id="<%=DefineReport.InpText.PARK_HC_BA.getObj()%>" check='<%=DefineReport.InpText.PARK_HC_BA.getMaxlenTag()%>' style="width:70px; text-align: right;"></td>
				</tr>
				<tr>
					<td>屋上</td>
					<td><input class="easyui-numberbox" tabindex="3002" col="F70" id="<%=DefineReport.InpText.PARK_NM_YANE.getObj()%>" check='<%=DefineReport.InpText.PARK_NM_YANE.getMaxlenTag()%>' style="width:70px; text-align: right;"></td>
					<td><input class="easyui-numberbox" tabindex="3007" col="F73" id="<%=DefineReport.InpText.PARK_LT_YANE.getObj()%>" check='<%=DefineReport.InpText.PARK_LT_YANE.getMaxlenTag()%>' style="width:70px; text-align: right;"></td>
					<td><input class="easyui-numberbox" tabindex="3010" col="F76" id="<%=DefineReport.InpText.PARK_HC_YANE.getObj()%>" check='<%=DefineReport.InpText.PARK_HC_YANE.getMaxlenTag()%>' style="width:70px; text-align: right;"></td>
				</tr>
				<tr>
					<td>飛地</td>
					<td><input class="easyui-numberbox" tabindex="3003" col="F71" id="<%=DefineReport.InpText.PARK_NM_TOBI.getObj()%>" check='<%=DefineReport.InpText.PARK_NM_TOBI.getMaxlenTag()%>' style="width:70px; text-align: right;"></td>
					<td><input class="easyui-numberbox" tabindex="3008" col="F74" id="<%=DefineReport.InpText.PARK_LT_TOBI.getObj()%>" check='<%=DefineReport.InpText.PARK_LT_TOBI.getMaxlenTag()%>' style="width:70px; text-align: right;"></td>
					<td><input class="easyui-numberbox" tabindex="3011" col="F77" id="<%=DefineReport.InpText.PARK_HC_TOBI.getObj()%>" check='<%=DefineReport.InpText.PARK_HC_TOBI.getMaxlenTag()%>' style="width:70px; text-align: right;"></td>
				</tr>
				<tr>
					<td>平均回転率</td>
					<td><input class="easyui-numberbox" tabindex="3004" col="F108" id="<%=DefineReport.InpText.HEIKINKAITENRITU.getObj()%>" check='<%=DefineReport.InpText.HEIKINKAITENRITU.getMaxlenTag()%>' style="width:70px; text-align: right;"></td>
				</tr>
				<tr>
					<td>必要台数</td>
					<td><input class="easyui-numberbox" tabindex="3005" col="F109" id="<%=DefineReport.InpText.HITUYOUDAISUU.getObj()%>" check='<%=DefineReport.InpText.HITUYOUDAISUU.getMaxlenTag()%>' style="width:70px; text-align: right;"></td>
				</tr>
				</table>
			</fieldset></td>
			<td></td>
			<td><fieldset><legend>敷地面積</legend>
				<table>
				<tr>
					<td><input class="easyui-numberbox" tabindex="3012" col="F58" id="<%=DefineReport.InpText.AREA_BA.getObj()%>" check='<%=DefineReport.InpText.AREA_BA.getMaxlenTag()%>' style="width:90px; text-align: right;"></td>
				</tr>
				</table>
			</fieldset></td>
			<td></td>
			<td><fieldset><legend>建築面積</legend>
				<table>
				<tr>
					<td><input class="easyui-numberbox" tabindex="3013" col="F110" id="<%=DefineReport.InpText.AREA_KENTIKU.getObj()%>" check='<%=DefineReport.InpText.AREA_KENTIKU.getMaxlenTag()%>' style="width:90px; text-align: right;"></td>
				</tr>
				</table>
			</fieldset></td>
			<td></td>
			<td><fieldset>
				<table>
				<tr>
					<td></td>
					<td>B1</td>
					<td>1F</td>
					<td>2F</td>
					<td>3F</td>
					<td>4F</td>
				</tr>
				<tr>
					<td>延床面積</td>
					<td><input class="easyui-numberbox" tabindex="3014" col="F59" id="<%=DefineReport.InpText.AERA_B1YUKA.getObj()%>" check='<%=DefineReport.InpText.AERA_B1YUKA.getMaxlenTag()%>' style="width:90px; text-align: right;"></td>
					<td><input class="easyui-numberbox" tabindex="3016" col="F61" id="<%=DefineReport.InpText.AREA_1FYUKA.getObj()%>" check='<%=DefineReport.InpText.AREA_1FYUKA.getMaxlenTag()%>' style="width:90px; text-align: right;"></td>
					<td><input class="easyui-numberbox" tabindex="3018" col="F63" id="<%=DefineReport.InpText.AREA_2FYUKA.getObj()%>" check='<%=DefineReport.InpText.AREA_2FYUKA.getMaxlenTag()%>' style="width:90px; text-align: right;"></td>
					<td><input class="easyui-numberbox" tabindex="3020" col="F65" id="<%=DefineReport.InpText.AREA_3FYUKA.getObj()%>" check='<%=DefineReport.InpText.AREA_3FYUKA.getMaxlenTag()%>' style="width:90px; text-align: right;"></td>
					<td><input class="easyui-numberbox" tabindex="3022" col="F67" id="<%=DefineReport.InpText.AREA_4FYUKA.getObj()%>" check='<%=DefineReport.InpText.AREA_4FYUKA.getMaxlenTag()%>' style="width:90px; text-align: right;"></td>
				</tr>
				<tr>
					<td>売場面積</td>
					<td><input class="easyui-numberbox" tabindex="3015" col="F60" id="<%=DefineReport.InpText.AREA_B1URIBA.getObj()%>" check='<%=DefineReport.InpText.AREA_B1URIBA.getMaxlenTag()%>' style="width:90px; text-align: right;"></td>
					<td><input class="easyui-numberbox" tabindex="3017" col="F62" id="<%=DefineReport.InpText.AREA_FURIBA.getObj()%>" check='<%=DefineReport.InpText.AREA_FURIBA.getMaxlenTag()%>' style="width:90px; text-align: right;"></td>
					<td><input class="easyui-numberbox" tabindex="3019" col="F64" id="<%=DefineReport.InpText.AREA_2FURIBA.getObj()%>" check='<%=DefineReport.InpText.AREA_2FURIBA.getMaxlenTag()%>' style="width:90px; text-align: right;"></td>
					<td><input class="easyui-numberbox" tabindex="3021" col="F66" id="<%=DefineReport.InpText.AREA_3FURIBA.getObj()%>" check='<%=DefineReport.InpText.AREA_3FURIBA.getMaxlenTag()%>' style="width:90px; text-align: right;"></td>
					<td><input class="easyui-numberbox" tabindex="3023" col="F68" id="<%=DefineReport.InpText.AREA_4FURIBA.getObj()%>" check='<%=DefineReport.InpText.AREA_4FURIBA.getMaxlenTag()%>' style="width:90px; text-align: right;"></td>
				</tr>
				</table>
			</fieldset>
			<table>
				<tr>
					<td>エスカレーター</td>
					<td><select class="easyui-combobox" tabindex="3024" col="F79" id="<%=DefineReport.MeisyoSelect.KBN312.getObj()%>" data-options="required:true" style="width:100px;"></select></td>
					<td>エレベータ</td>
					<td><select class="easyui-combobox" tabindex="3025" col="F78" id="<%=DefineReport.MeisyoSelect.KBN311.getObj()%>" data-options="required:true" style="width:100px;"></select></td>
				</tr>
			</table>
			</td>
		</tr>
		</table>
		<br>
		<table>
		<tr><th style="width:20px;"></th></tr>
		<tr style="vertical-align: top;">
			<td>
			<fieldset><legend>テナント</legend>
				<table class="t-layout">
					<tr><th style="width:100px;"></th><th style="width:100px;"></th></tr>
					<tr>
					<td style="padding-right: 5px;">
						<div style="height:290px;width:425px;">
						<table id="dg13" class="like_datagrid">
						<thead>
							<tr>
								<th style="width:200px;">テナント</th>
								<th style="width:420px;">社名</th>
							</tr>
						</thead>
						<tbody>
							<tr>
								<td class="col_txt"><span col="F115"></span></td>
								<td class="col_txt"><input class="easyui-textbox" tabindex="3100" col="F135" id="<%=DefineReport.InpText.TENANTO1_SYAMEI.getObj()%>" check='<%=DefineReport.InpText.TENANTO1_SYAMEI.getMaxlenTag()%>' style="width:100%;"></td>
							</tr>
							<tr>
								<td class="col_txt"><span col="F116"></span></td>
								<td class="col_txt"><input class="easyui-textbox" tabindex="3101" col="F136" id="<%=DefineReport.InpText.TENANTO2_SYAMEI.getObj()%>" check='<%=DefineReport.InpText.TENANTO2_SYAMEI.getMaxlenTag()%>' style="width:100%;"></td>
							</tr>
							<tr>
								<td class="col_txt"><span col="F117"></span></td>
								<td class="col_txt"><input class="easyui-textbox" tabindex="3102" col="F137" id="<%=DefineReport.InpText.TENANTO3_SYAMEI.getObj()%>" check='<%=DefineReport.InpText.TENANTO3_SYAMEI.getMaxlenTag()%>' style="width:100%;"></td>
							</tr>
							<tr>
								<td class="col_txt"><span col="F118"></span></td>
								<td class="col_txt"><input class="easyui-textbox" tabindex="3103" col="F138" id="<%=DefineReport.InpText.TENANTO4_SYAMEI.getObj()%>" check='<%=DefineReport.InpText.TENANTO4_SYAMEI.getMaxlenTag()%>' style="width:100%;"></td>
							</tr>
							<tr>
								<td class="col_txt"><span col="F119"></span></td>
								<td class="col_txt"><input class="easyui-textbox" tabindex="3104" col="F139" id="<%=DefineReport.InpText.TENANTO5_SYAMEI.getObj()%>" check='<%=DefineReport.InpText.TENANTO5_SYAMEI.getMaxlenTag()%>' style="width:100%;"></td>
							</tr>
							<tr>
								<td class="col_txt"><span col="F120"></span></td>
								<td class="col_txt"><input class="easyui-textbox" tabindex="3105" col="F140" id="<%=DefineReport.InpText.TENANTO6_SYAMEI.getObj()%>" check='<%=DefineReport.InpText.TENANTO6_SYAMEI.getMaxlenTag()%>' style="width:100%;"></td>
							</tr>
							<tr>
								<td class="col_txt"><span col="F121"></span></td>
								<td class="col_txt"><input class="easyui-textbox" tabindex="3106" col="F141" id="<%=DefineReport.InpText.TENANTO7_SYAMEI.getObj()%>" check='<%=DefineReport.InpText.TENANTO7_SYAMEI.getMaxlenTag()%>' style="width:100%;"></td>
							</tr>
							<tr>
								<td class="col_txt"><span col="F122"></span></td>
								<td class="col_txt"><input class="easyui-textbox" tabindex="3107" col="F142" id="<%=DefineReport.InpText.TENANTO8_SYAMEI.getObj()%>" check='<%=DefineReport.InpText.TENANTO8_SYAMEI.getMaxlenTag()%>' style="width:100%;"></td>
							</tr>
							<tr>
								<td class="col_txt"><span col="F123"></span></td>
								<td class="col_txt"><input class="easyui-textbox" tabindex="3108" col="F143" id="<%=DefineReport.InpText.TENANTO9_SYAMEI.getObj()%>" check='<%=DefineReport.InpText.TENANTO9_SYAMEI.getMaxlenTag()%>' style="width:100%;"></td>
							</tr>
							<tr>
								<td class="col_txt"><span col="F124"></span></td>
								<td class="col_txt"><input class="easyui-textbox" tabindex="3109" col="F144" id="<%=DefineReport.InpText.TENANTO10_SYAMEI.getObj()%>" check='<%=DefineReport.InpText.TENANTO10_SYAMEI.getMaxlenTag()%>' style="width:100%;"></td>
							</tr>
						</tbody>
						</table>
						</div>
					</td>
					<td style="vertical-align: top;">
						<div style="height:77px;width:425px;">
						<table id="dg14" class="like_datagrid">
						<thead>
							<tr>
								<th style="width:200px;">テナント</th>
								<th style="width:420px;">社名</th>
							</tr>
						</thead>
						<tbody>
							<tr>
								<td class="col_txt"><span col="F125"></span></td>
								<td class="col_txt"><input class="easyui-textbox" tabindex="3110" col="F145" id="<%=DefineReport.InpText.TENANTO11_SYAMEI.getObj()%>" check='<%=DefineReport.InpText.TENANTO11_SYAMEI.getMaxlenTag()%>' style="width:100%;"></td>
							</tr>
							<tr>
								<td class="col_txt"><span col="F126"></span></td>
								<td class="col_txt"><input class="easyui-textbox" tabindex="3111" col="F146" id="<%=DefineReport.InpText.TENANTO12_SYAMEI.getObj()%>" check='<%=DefineReport.InpText.TENANTO12_SYAMEI.getMaxlenTag()%>' style="width:100%;"></td>
							</tr>
							<tr>
								<td class="col_txt"><span col="F127"></span></td>
								<td class="col_txt"><input class="easyui-textbox" tabindex="3112" col="F147" id="<%=DefineReport.InpText.TENANTO13_SYAMEI.getObj()%>" check='<%=DefineReport.InpText.TENANTO13_SYAMEI.getMaxlenTag()%>' style="width:100%;"></td>
							</tr>
							<tr>
								<td class="col_txt"><span col="F128"></span></td>
								<td class="col_txt"><input class="easyui-textbox" tabindex="3113" col="F148" id="<%=DefineReport.InpText.TENANTO14_SYAMEI.getObj()%>" check='<%=DefineReport.InpText.TENANTO14_SYAMEI.getMaxlenTag()%>' style="width:100%;"></td>
							</tr>
							<tr>
								<td class="col_txt"><span col="F129"></span></td>
								<td class="col_txt"><input class="easyui-textbox" tabindex="3114" col="F149" id="<%=DefineReport.InpText.TENANTO15_SYAMEI.getObj()%>" check='<%=DefineReport.InpText.TENANTO15_SYAMEI.getMaxlenTag()%>' style="width:100%;"></td>
							</tr>
							<tr>
								<td class="col_txt"><span col="F130"></span></td>
								<td class="col_txt"><input class="easyui-textbox" tabindex="3115" col="F150" id="<%=DefineReport.InpText.TENANTO16_SYAMEI.getObj()%>" check='<%=DefineReport.InpText.TENANTO16_SYAMEI.getMaxlenTag()%>' style="width:100%;"></td>
							</tr>
							<tr>
								<td class="col_txt"><span col="F131"></span></td>
								<td class="col_txt"><input class="easyui-textbox" tabindex="3116" col="F151" id="<%=DefineReport.InpText.TENANTO17_SYAMEI.getObj()%>" check='<%=DefineReport.InpText.TENANTO17_SYAMEI.getMaxlenTag()%>' style="width:100%;"></td>
							</tr>
							<tr>
								<td class="col_txt"><span col="F132"></span></td>
								<td class="col_txt"><input class="easyui-textbox" tabindex="3117" col="F152" id="<%=DefineReport.InpText.TENANTO18_SYAMEI.getObj()%>" check='<%=DefineReport.InpText.TENANTO18_SYAMEI.getMaxlenTag()%>' style="width:100%;"></td>
							</tr>
							<tr>
								<td class="col_txt"><span col="F133"></span></td>
								<td class="col_txt"><input class="easyui-textbox" tabindex="3118" col="F153" id="<%=DefineReport.InpText.TENANTO19_SYAMEI.getObj()%>" check='<%=DefineReport.InpText.TENANTO19_SYAMEI.getMaxlenTag()%>' style="width:100%;"></td>
							</tr>
							<tr>
								<td class="col_txt"><span col="F134"></span></td>
								<td class="col_txt"><input class="easyui-textbox" tabindex="3119" col="F154" id="<%=DefineReport.InpText.TENANTO20_SYAMEI.getObj()%>" check='<%=DefineReport.InpText.TENANTO20_SYAMEI.getMaxlenTag()%>' style="width:100%;"></td>
							</tr>
						</tbody>
						</table>
						</div>
					</td>
					</tr>
					</table>
			</fieldset>
			</td>
			<td>
			<fieldset><legend>尺数</legend>
				<table class="t-layout">
					<tr><th style="width:100px;"></th></tr>
					<tr>
					<td style="padding-right: 5px;">
						<div style="height:290px;width:125px;">
						<table id="dg13" class="like_datagrid">
						<thead>
							<tr>
								<th style="width:30px;">部門</th>
								<th style="width:90px;">尺数</th>
							</tr>
						</thead>
						<tbody>
							<tr>
								<td class="col_txt"><span col="F175"></span></td>
								<td class="col_txt"><input class="easyui-numberbox" tabindex="3120" col="F155" id="<%=DefineReport.InpText.SYAKUSUU1.getObj()%>" check='<%=DefineReport.InpText.SYAKUSUU1.getMaxlenTag()%>' style="width:100%;text-align: right;"></td>
							</tr>
							<tr>
								<td class="col_txt"><span col="F176"></span></td>
								<td class="col_txt"><input class="easyui-numberbox" tabindex="3121" col="F156" id="<%=DefineReport.InpText.SYAKUSUU2.getObj()%>" check='<%=DefineReport.InpText.SYAKUSUU2.getMaxlenTag()%>' style="width:100%;text-align: right;"></td>
							</tr>
							<tr>
								<td class="col_txt"><span col="F177"></span></td>
								<td class="col_txt"><input class="easyui-numberbox" tabindex="3122" col="F157" id="<%=DefineReport.InpText.SYAKUSUU3.getObj()%>" check='<%=DefineReport.InpText.SYAKUSUU3.getMaxlenTag()%>' style="width:100%;text-align: right;"></td>
							</tr>
							<tr>
								<td class="col_txt"><span col="F178"></span></td>
								<td class="col_txt"><input class="easyui-numberbox" tabindex="3123" col="F158" id="<%=DefineReport.InpText.SYAKUSUU4.getObj()%>" check='<%=DefineReport.InpText.SYAKUSUU4.getMaxlenTag()%>' style="width:100%;text-align: right;"></td>
							</tr>
							<tr>
								<td class="col_txt"><span col="F179"></span></td>
								<td class="col_txt"><input class="easyui-numberbox" tabindex="3124" col="F159" id="<%=DefineReport.InpText.SYAKUSUU5.getObj()%>" check='<%=DefineReport.InpText.SYAKUSUU5.getMaxlenTag()%>' style="width:100%;text-align: right;"></td>
							</tr>
							<tr>
								<td class="col_txt"><span col="F180"></span></td>
								<td class="col_txt"><input class="easyui-numberbox" tabindex="3125" col="F160" id="<%=DefineReport.InpText.SYAKUSUU6.getObj()%>" check='<%=DefineReport.InpText.SYAKUSUU6.getMaxlenTag()%>' style="width:100%;text-align: right;"></td>
							</tr>
							<tr>
								<td class="col_txt"><span col="F181"></span></td>
								<td class="col_txt"><input class="easyui-numberbox" tabindex="3126" col="F161" id="<%=DefineReport.InpText.SYAKUSUU7.getObj()%>" check='<%=DefineReport.InpText.SYAKUSUU7.getMaxlenTag()%>' style="width:100%;text-align: right;"></td>
							</tr>
							<tr>
								<td class="col_txt"><span col="F182"></span></td>
								<td class="col_txt"><input class="easyui-numberbox" tabindex="3127" col="F162" id="<%=DefineReport.InpText.SYAKUSUU8.getObj()%>" check='<%=DefineReport.InpText.SYAKUSUU8.getMaxlenTag()%>' style="width:100%;text-align: right;"></td>
							</tr>
							<tr>
								<td class="col_txt"><span col="F183"></span></td>
								<td class="col_txt"><input class="easyui-numberbox" tabindex="3128" col="F163" id="<%=DefineReport.InpText.SYAKUSUU9.getObj()%>" check='<%=DefineReport.InpText.SYAKUSUU9.getMaxlenTag()%>' style="width:100%;text-align: right;"></td>
							</tr>
							<tr>
								<td class="col_txt"><span col="F184"></span></td>
								<td class="col_txt"><input class="easyui-numberbox" tabindex="3129" col="F164" id="<%=DefineReport.InpText.SYAKUSUU10.getObj()%>" check='<%=DefineReport.InpText.SYAKUSUU10.getMaxlenTag()%>' style="width:100%;text-align: right;"></td>
							</tr>
						</tbody>
						</table>
						</div>
					</td>
					<td style="vertical-align: top;">
						<div style="height:77px;width:125px;">
						<table id="dg14" class="like_datagrid">
						<thead>
							<tr>
								<th style="width:30px;">部門</th>
								<th style="width:90px;">尺数</th>
							</tr>
						</thead>
						<tbody>
							<tr>
								<td class="col_txt"><span col="F185"></span></td>
								<td class="col_txt"><input class="easyui-numberbox" tabindex="3130" col="F165" id="<%=DefineReport.InpText.SYAKUSUU11.getObj()%>" check='<%=DefineReport.InpText.SYAKUSUU11.getMaxlenTag()%>' style="width:100%;text-align: right;"></td>
							</tr>
							<tr>
								<td class="col_txt"><span col="F186"></span></td>
								<td class="col_txt"><input class="easyui-numberbox" tabindex="3131" col="F166" id="<%=DefineReport.InpText.SYAKUSUU12.getObj()%>" check='<%=DefineReport.InpText.SYAKUSUU12.getMaxlenTag()%>' style="width:100%;text-align: right;"></td>
							</tr>
							<tr>
								<td class="col_txt"><span col="F187"></span></td>
								<td class="col_txt"><input class="easyui-numberbox" tabindex="3132" col="F167" id="<%=DefineReport.InpText.SYAKUSUU13.getObj()%>" check='<%=DefineReport.InpText.SYAKUSUU13.getMaxlenTag()%>' style="width:100%;text-align: right;"></td>
							</tr>
							<tr>
								<td class="col_txt"><span col="F188"></span></td>
								<td class="col_txt"><input class="easyui-numberbox" tabindex="3133" col="F168" id="<%=DefineReport.InpText.SYAKUSUU15.getObj()%>" check='<%=DefineReport.InpText.SYAKUSUU15.getMaxlenTag()%>' style="width:100%;text-align: right;"></td>
							</tr>
							<tr>
								<td class="col_txt"><span col="F189"></span></td>
								<td class="col_txt"><input class="easyui-numberbox" tabindex="3134" col="F169" id="<%=DefineReport.InpText.SYAKUSUU20.getObj()%>" check='<%=DefineReport.InpText.SYAKUSUU20.getMaxlenTag()%>' style="width:100%;text-align: right;"></td>
							</tr>
							<tr>
								<td class="col_txt"><span col="F190"></span></td>
								<td class="col_txt"><input class="easyui-numberbox" tabindex="3135" col="F170" id="<%=DefineReport.InpText.SYAKUSUU23.getObj()%>" check='<%=DefineReport.InpText.SYAKUSUU23.getMaxlenTag()%>' style="width:100%;text-align: right;"></td>
							</tr>
							<tr>
								<td class="col_txt"><span col="F191"></span></td>
								<td class="col_txt"><input class="easyui-numberbox" tabindex="3136" col="F171" id="<%=DefineReport.InpText.SYAKUSUU34.getObj()%>" check='<%=DefineReport.InpText.SYAKUSUU34.getMaxlenTag()%>' style="width:100%;text-align: right;"></td>
							</tr>
							<tr>
								<td class="col_txt"><span col="F192"></span></td>
								<td class="col_txt"><input class="easyui-numberbox" tabindex="3137" col="F172" id="<%=DefineReport.InpText.SYAKUSUU43.getObj()%>" check='<%=DefineReport.InpText.SYAKUSUU43.getMaxlenTag()%>' style="width:100%;text-align: right;"></td>
							</tr>
							<tr>
								<td class="col_txt"><span col="F193"></span></td>
								<td class="col_txt"><input class="easyui-numberbox" tabindex="3138" col="F173" id="<%=DefineReport.InpText.SYAKUSUU44.getObj()%>" check='<%=DefineReport.InpText.SYAKUSUU44.getMaxlenTag()%>' style="width:100%;text-align: right;"></td>
							</tr>
							<tr>
								<td class="col_txt"><span col="F194"></span></td>
								<td class="col_txt"><input class="easyui-numberbox" tabindex="3139" col="F174" id="<%=DefineReport.InpText.SYAKUSUU54.getObj()%>" check='<%=DefineReport.InpText.SYAKUSUU54.getMaxlenTag()%>' style="width:100%;text-align: right;"></td>
							</tr>
						</tbody>
						</table>
						</div>
					</td>
					</tr>
					</table>
			</fieldset>
			</td>
		</tr>
		</table>
	</div>
	</div>
</div>
</form>
<div id="buttons" data-options="region:'south',border:false" style="display:none;">
	<div class="btn" style="float: left;">
		<table class="t-layout3">
		<tr>
			<th style="min-width:120px;"></th><th style="min-width:10px;"></th>
			<th style="min-width:120px;"></th><th style="min-width:120px;"></th>
		</tr>
		<tr>
		<td><a href="#" title="戻る" id="<%=DefineReport.Button.BACK.getObj()%>" class="easyui-linkbutton" tabindex="9001" iconCls="icon-undo" style="width:110px;"><span class="btnTxt">戻る</span></a></td>
		<td></td>
		<td><a href="#" class="easyui-linkbutton" tabindex="9002" id="<%=DefineReport.Button.CANCEL.getObj()%>" title="<%=DefineReport.Button.CANCEL.getTxt()%>" iconCls="icon-cancel" style="width:110px;"><%=DefineReport.Button.CANCEL.getTxt()%></a></td>
		<td><a href="#" class="easyui-linkbutton" tabindex="9003" id="<%=DefineReport.Button.UPD.getObj()%>" title="<%=DefineReport.Button.UPD.getTxt()%>" iconCls="icon-save" style="width:110px;"><span class="btnTxt"><%=DefineReport.Button.UPD.getTxt()%></span></a></td>
		</tr>
		</table>
	</div>
	<div id="disp_record_info" style="float: right;">
		<span class="labelName" tabindex="-1" style="padding-top: 3px;">
			登録日 <span col="F112" id="<%=DefineReport.Text.ADDDT.getObj()%>"></span>　更新日 <span col="F113" id="<%=DefineReport.Text.UPDDT.getObj()%>"></span>　オペレータ <span col="F111" id="<%=DefineReport.Text.OPERATOR.getObj()%>"></span>
		</span>
		<input type="hidden" col="F114" name="<%=DefineReport.Hidden.UPDDT.getObj()%>" id="<%=DefineReport.Hidden.UPDDT.getObj()%>" />
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
<script type="text/javascript" src="../js/jquery.report/jquery.report.option.x112.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.control.js?v=<%=prm %>"></script>
<script type="text/javascript" src="../js/jquery.report/jquery.report.events.js?v=<%=prm %>"></script>
</html>