<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8"%>

<div id="win-tg" class="easyui-dialog" title="店舗グループ"
	iconCls="icon-edit" closed="true" modal="true" maximizable="true" resizable="true" toolbar="#dlg-toolbar-tg"
	style="width:800px;height:400px;padding:5px;">
	<div class="easyui-layout" fit="true">
		<div id="dlg-toolbar-tg">
			<table cellpadding="0" cellspacing="0" style="width:100%">
				<tr>
					<td>
						<span>店舗グループ</span>
						<input id="itemCell-tg" style="width:200px">
						<a href="javascript:void(0)" id="sb1-tg" class="easyui-splitbutton" menu="#mm1-tg" iconCls="icon-ok">表示</a>
					</td>
					<td style="text-align:right">
						<a href="javascript:void(0)" id="qt-tg" class="easyui-linkbutton" iconCls="icon-edit" plain="false">適用</a>
					</td>
				</tr>
			</table>
		</div>
		<div region="center" border="false" style="padding:0px;background:#fff;border:0px solid #ccc;">
			<div id="tt-tg" style="width:auto;height:auto">
			</div>
		</div>
	</div>
</div>
<div id="mm1-tg" style="width:100px;">
	<div id="mmExpand-tg" iconCls="icon-ok">表示</div>
	<div id="mmDelete-tg" iconCls="icon-cancel">削除</div>
</div>
<div id="dd-tg" class="easyui-dialog" style="width:300px;height:300px"
		data-options="title:'店舗コード複数追加',buttons:'#bb-tg',modal:true, closed:true, resizable:false">
	<div id="ddtext-tg" style="width:auto;height:auto"></div>
</div>
<div id="bb-tg">
	<a href="#" id="bbadd-tg" class="easyui-linkbutton" style="width:270px;">追加</a>
</div>
