/**
 * jquery sub window option
 * 通常率/数量パターン選択(ST012/ST013)
 */
;(function($) {

$.extend({

	winST012: {
		name: 'Out_ReportwinST012',
		prefix:'_sub',
		suffix:'_winST012',
		initializes: true,		// 初期化フラグ（全体）
		initializesCond: true,	// 初期化フラグ（条件用）
		lastIndex: -1,			// 編集位置（行）
		sortName: '',			// ソート項目名
		sortOrder: '',			// ソート順
		timeData : (new Date()).getTime(),
		callreportno:"",		// 呼出し元レポートNo
		callBtnid: "",			// 呼出ボタンID情報
		callreportHidden:[],	// 呼出し元レポートからの引き継ぎ情報
		TenrankArr:"",

		focusRootId:"_winST012",// （キー移動時必須）キー移動イベントのルートとなるパネルのID
		focusParentId:"",		// （キー移動時必須）現在フォーカスがあたっている項目の親となるパネルのID
		focusGridId:"",			// （キー移動時必須）現在フォーカスがあたっているDataGridのID
		repKbn:'',				// 帳票タイプ
		init: function(js) {
			var that = this;
			if(!that.initializes) return false;

			that.callreportno = js.name;
			that.callreportHidden = js.jsonHidden;
			if(js.judgeRepType.sei_upd){
				that.TenrankArr = js.getColValue("F162");
			}

			// 呼出しボタンイベント設定
			$('[id^=btn'+that.prefix+that.suffix+']').each(function(){
				var id = $(this).attr('id');
				$('#'+id).click(function() { that.Open(this); });
			});
			// 検索
			$('#'+$.id.btn_search+that.suffix).on("click", that.Search);
			// キャンセル
			$('#'+$.id.btn_cancel+that.suffix).on("click", that.Cancel);
			// 選択
			$('#'+$.id.btn_select+that.suffix).on("click", that.Select);

			// ｳｲﾝﾄﾞｳ設定
			$('#'+that.suffix).window({
				iconCls:'icon-search',
				modal:true,
				collapsible:false,
				minimizable:false,
				maximizable:false,
				closed:true,
				cinline:false,
				zIndex:90000,
				onBeforeOpen:function(){
					// ウインドウ展開中リサイズイベント無効化
					$.reg.resize = false;
					js.focusParentId = that.suffix;
				},
				onOpen:function(){
					$('#'+js.focusParentId).find('[tabindex]').filter("[tabindex!=-1]").filter('[disabled!=disabled]').filter(":visible").eq(0).focus();
				},
				onBeforeClose:function(){
					// ウインドウ展開中リサイズイベント有効化
					$.reg.resize = true;
					that.Clear();
					js.focusParentId = js.focusRootId;
				},
				onClose:function(){
					$('#'+js.focusParentId).find('#'+that.callBtnid).focus();
				}
			});

			// チェックボックスの設定
			$.initCheckboxCss($("#"+that.focusRootId));
			// キーイベントの設定
			$.initKeyEvent(that);

			that.initializes = !that.initializes;
		},
		Open: function(obj) {
			if ($(obj).is("disabled","disabled"))	return false;
			var that = $.winST012;
			that.callBtnid = $(obj).attr('id');

			that.repKbn = $('#'+$.id_inp.txt_tenkaikbn).val();

			if(that.repKbn=== $.id.value_tenkaikbn_tr){
				$('#'+that.suffix).window({title: '特売・スポット計画　通常率パターン　選択(ST012)'}).window('resize', {width:500}).window('center');
			}else if(that.repKbn=== $.id.value_tenkaikbn_ts){
				$('#'+that.suffix).window({title: '特売・スポット計画　数量パターン　選択(ST013)'}).window('resize', {width:1100}).window('center');
			}else{
				$.showMessage('E20369');
				return false;
			}
			var chks =$('input[name="'+$.id.rad_sel+'"]:checked');
			if(chks.length===0){
				$.showMessage('E40081');
				return false;
			}
			var idx = chks.eq(0).attr("id").replace($.id.rad_sel, "_");

			// 画面情報表示
			$('#'+that.focusRootId).find('[id]').filter('span').each(function(){
				var refid = $(this).attr('id').replace(that.suffix, '');
				if($('#'+refid)){
					$(this).text($.getInputboxText($('#'+refid)));
				}
			});
			$('#'+that.focusRootId).find('[for_kbn]').each(function(){
				var kbns = $(this).attr('for_kbn').split(",");
				if(kbns.indexOf(that.repKbn) === -1){
					$(this).hide();
				}else{
					$(this).show();
				}
			});

			// dataGrid 初期化
			this.setDataGrid('#grd'+that.prefix+that.suffix);

			that.Search();

			// window 表示
			$('#'+that.suffix).window('open');
		},
		Clear:function(){
			var that = $.winST012;
			that.initializesCond = true;
			// グリッド初期化
			$('#grd'+that.prefix+that.suffix).datagrid('clearSelections').datagrid({data: []}).datagrid('getPager').pagination('select', 1);	// ページ初期化
			that.initializesCond = false;
		},
		Search: function(){
			var that = $.winST012;

			// validate=falseの場合何もしない
			if(!that.validation()){ return false; }

			// セッションタイムアウト、利用時間外の確認
			var isTimeout = $.checkIsTimeout();
			if (! isTimeout) {
				// 検索実行
				that.success("grd"+that.prefix+that.suffix);
			}

			return true;
		},
		Cancel:function(){
			var that = $.winST012;
			$('#'+that.suffix).window('close');
			return true;
		},
		Select: function(){
			var that = $.winST012;

			var row = $("#grd"+that.prefix+that.suffix).datagrid("getSelected");
			if(!row){
				$.showMessage('E00008');
				return false;
			}
			if(!row['F1']){
				return false;
			}
			// 取得した情報を、オブジェクトに設定する
			var chks =$('input[name="'+$.id.rad_sel+'"]:checked');
			var col = 'N7'+chks.eq(0).attr("id").replace($.id.rad_sel, "_");
			if($('[col='+col+']')){
				$.setInputboxValue($('[col='+col+']'), row['F1']);
			}
			$('#'+that.suffix).window('close');
			return true;
		},
		validation: function (){	// （必須）批准
			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform+that.suffix).form('validate');
			return rt;
		},
		success: function(id){	// 検索処理
			var that = $.winST012;
			// 処理時間計測用
			that.timeData = (new Date()).getTime();

			// グリッド初期化&ローディング
			$('#'+id).datagrid('loading');

			// 情報設定
			var json = [{}];

			// 呼出し元別処理
			switch (that.repKbn) {
			case $.id.value_tenkaikbn_tr:
				// オブジェクト作成
				json = [{
					callpage:	$($.id.hidden_reportno).val(),										// 呼出元レポート名
					BMNCD:		$.getJSONValue(that.callreportHidden, $.id_inp.txt_bmncd),			// 部門コード
					KBN:		that.repKbn															// 帳票タイプ
				}];
				break;
			case $.id.value_tenkaikbn_ts:

				var tencdAdds = [], rankAdds = [], tencdDels = [];
				for (var i = 0; i < 10; i++){
					var add = $.getInputboxValue($('#'+$.id_inp.txt_tencd+"_add"+(i+1)));
					if(!$.isEmptyVal(add)){
						tencdAdds.push(add);
						rankAdds.push($.getInputboxValue($('#'+$.id_inp.txt_tenrank+(i+1))));
					}
					var del = $.getInputboxValue($('#'+$.id_inp.txt_tencd+"_del"+(i+1)));
					if(!$.isEmptyVal(del)){
						tencdDels.push(del);
					}
				}

				// オブジェクト作成
				json = [{
					callpage:	$($.id.hidden_reportno).val(),										// 呼出元レポート名
					MOYSKBN:	$.getJSONValue(that.callreportHidden, $.id_inp.txt_moyskbn),		// 催し区分
					MOYSSTDT:	$.getJSONValue(that.callreportHidden, $.id_inp.txt_moysstdt),		// 催し区分
					MOYSRBAN:	$.getJSONValue(that.callreportHidden, $.id_inp.txt_moysrban),		// 催し連番
					BMNCD:		$.getJSONValue(that.callreportHidden, $.id_inp.txt_bmncd),			// 部門コード
					RINJI:		$.getInputboxValue($('#'+$.id.chk_rinji+that.suffix)),				// 臨時
					KBN:		that.repKbn,															// 帳票タイプ
					RANKNO_ADD:	$.getInputboxValue($('#'+$.id_inp.txt_rankno_add)),
					RANKNO_DEL:	$.getInputboxValue($('#'+$.id_inp.txt_rankno_del)),
					TENCD_ADDS:tencdAdds,
					TENRANK_ADDS:rankAdds,
					TENCD_DELS:tencdDels,
					TENRANK_ARR:that.TenrankArr
				}];
				break;
			default:
				break;
			}

			$.post(
				$.reg.easy,
				{
					page	:	that.name,										// レポート名
					obj		:	id,
					sel		:	(new Date()).getTime(),
					target	:	id,
					action	:	$.id.action_init,
					json	:	JSON.stringify(json),
					datatype:	'datagrid'
				},
				function(data){
					// ログ出力
					$.log(that.timeData, 'query:');
					var dg =$('#'+id);
					if(data!==""){
						// JSONに変換
						var json = JSON.parse(data);

						// 結果表示
						dg.datagrid('loadData', json.rows);
					}
					dg.datagrid('loaded');
					// ログ出力
					$.log(that.timeData, 'loaded:');
				}
			);
		},
		setDataGrid: function(id) {
			var that = this;
			var frozencolumns = [[]];
			var columns = [[]];
			switch (that.repKbn) {
			case $.id.value_tenkaikbn_tr:
				columns = [[
							{field:'F1', title:'率パタン<br>No.',	width: 60,halign:'center',align:'left',formatter:function(value,row,index){ return $.getFormatLPad(value, $.len.ptnno);}},
							{field:'F2', title:'データ名称',		width:300,halign:'center',align:'left'}
						]];
				break;
			case $.id.value_tenkaikbn_ts:
				frozencolumns = [[
							{field:'F1', title:'数パタン<br>No.',	width: 60,halign:'center',align:'left',formatter:function(value,row,index){ return $.getFormatLPad(value, $.len.ptnno);}}
						]];
				columns = [[
							{field:'F2', title:'パターン名称',		width:270,halign:'center',align:'left'},
							{field:'F3', title:'合計',				width: 70,halign:'center',align:'right',formatter:function(value,row,index){ return $.getFormat(value, '#,##0');}}
						]];
				var field = 4;
				var titles = ['A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'];
				for(var i in titles){
					columns[0].push({field:'F'+field, title:titles[i],	width: 50,halign:'center',align:'right',formatter:function(value,row,index){ return $.getFormat(value, '#,##0');}});
					field++;
				}
				break;
			default:
				break;
			}

			$(id).datagrid({
				nowrap: true,
				border: true,
				striped: true,
				collapsible:false,
				remoteSort: true,
				rownumbers:false,
				fit:true,
				view:scrollview,
				frozenColumns:frozencolumns,
				columns:columns,
				fitColumns:false,	// 指定カラム幅を適用する場合、false 指定。
				rowStyler:function(index, row){
				},
				onLoadSuccess:function(data){
				},
				autoRowHeight:false,
				pagination:false,
				pagePosition:'bottom',
				singleSelect:true
			});
			if (	(!jQuery.support.opacity)
				&&	(!jQuery.support.style)
				&&	(typeof document.documentElement.style.maxHeight == "undefined")
				) {
				// ページリストに select を利用している。IE6  のバグで z-index が適用されない。
				// modalダイアログを利用する場合は、表示なしにする必要あり。
				$.fn.pagination.defaults.showPageList = false;
			}
		}
	}
});

})(jQuery);