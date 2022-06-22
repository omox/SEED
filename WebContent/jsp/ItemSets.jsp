<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8"%>

<div id="win" class="easyui-dialog" title="商品グループ"
	iconCls="icon-edit" closed="true" modal="true" maximizable="true" resizable="true" toolbar="#dlg-toolbar"
	style="width:800px;height:400px;padding:5px;">
	<div class="easyui-layout" fit="true">
		<div id="dlg-toolbar">
			<table cellpadding="0" cellspacing="0" style="width:100%">
				<tr>
					<td>
						<span>商品グループ</span>
						<input id="itemCell" style="width:200px">
						<a href="javascript:void(0)" id="sb1" class="easyui-splitbutton" menu="#mm1" iconCls="icon-ok">表示</a>
					</td>
					<td style="text-align:right">
						<a href="javascript:void(0)" id="qt" class="easyui-linkbutton" iconCls="icon-edit" plain="false">適用</a>
					</td>
				</tr>
			</table>
		</div>
		<div region="center" border="false" style="padding:0px;background:#fff;border:0px solid #ccc;">
			<div id="tt" style="width:auto;height:auto">
			</div>
		</div>
	</div>
</div>
<div id="mm1" style="width:100px;">
	<div id="mmExpand" iconCls="icon-ok">表示</div>
	<div id="mmDelete" iconCls="icon-cancel">削除</div>
</div>
<div id="dd" class="easyui-dialog" style="width:300px;height:300px"
		data-options="title:'商品コード複数追加',buttons:'#bb',modal:true, closed:true, resizable:false">
	<div id="ddtext" style="width:auto;height:auto"></div>
</div>
<div id="bb">
	<a href="#" id="bbadd" class="easyui-linkbutton" style="width:270px;">追加</a>
</div>
