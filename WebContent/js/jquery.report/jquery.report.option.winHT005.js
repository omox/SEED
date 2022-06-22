/**
 * jquery sub window option
 */
;(function($) {

$.extend({

	winHT005: {
		name: 'Out_ReportwinHT005',
		prefix:'_sub',
		suffix:'_winHT005',
		initializes: true,		// 初期化フラグ（全体）
		initializesCond: true,	// 初期化フラグ（条件用）
		lastIndex: -1,			// 編集位置（行）
		sortName: '',			// ソート項目名
		sortOrder: '',			// ソート順
		timeData : (new Date()).getTime(),
		callreportno:"",		// 呼出し元レポートNo
		callreportInit:[],		// 呼出し元レポートからの引き継ぎ情報
		callBtnid: "",			// 呼出ボタンID情報
		focusRootId:"_winHT005",	// （キー移動時必須）キー移動イベントのルートとなるパネルのID
		focusParentId:"",		// （キー移動時必須）現在フォーカスがあたっている項目の親となるパネルのID
		focusGridId:"",			// （キー移動時必須）現在フォーカスがあたっているDataGridのID
		init: function(js) {
			var that = this;
			if(!that.initializes) return false;

			that.callreportno = js.name;
			that.callreportInit = js.jsonInit;

			// 呼出しボタンイベント設定
			$('[id^=btn'+that.prefix+']').each(function(){
				var id = $(this).attr('id');
				$('#'+id).click(function() { that.Open(this); });
			});
			// キャンセル
			$('#'+$.id.btn_cancel+that.suffix).on("click", that.Cancel);

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
				width:775,
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
			var that = $.winHT005;
			that.callBtnid = $(obj).attr('id');

			// 呼出し元別処理
			switch (that.callreportno) {
			case 'Out_ReportHT002':
			case 'Out_ReportHT004':
				// オブジェクト作成
				$('#'+that.suffix).window({title: '入力済商品一覧(HT005)'});
				break;
			case 'Out_ReportHT007':
			case 'Out_ReportHT009':
				// オブジェクト作成
				$('#'+that.suffix).window({title: '入力済商品一覧(HT010)'});
				break;
			default:
				break;
			}

			// dataGrid 初期化
			this.setDataGrid('#grd'+that.prefix+that.suffix);

			that.Search();

			// window 表示
			$('#'+that.suffix).window('open');
		},
		Clear:function(){
			var that = $.winHT005;
			that.initializesCond = true;
			// グリッド初期化
			$('#grd'+that.prefix+that.suffix).datagrid('clearSelections').datagrid({data: []}).datagrid('getPager').pagination('select', 1);	// ページ初期化
			that.initializesCond = false;
		},
		Search: function(){
			var that = $.winHT005;

			// validate=falseの場合何もしない
			if(!that.validation()){ return false; }

			// セッションタイムアウト、利用時間外の確認
			var isTimeout = $.checkIsTimeout();
			if (! isTimeout) {
				// 検索実行
				//that.success("grd_subwindow"+that.prefix+that.suffix);
				that.success("grd"+that.prefix+that.suffix);
			}

			return true;
		},
		Cancel:function(){
			var that = $.winHT005;
			$('#'+that.suffix).window('close');
			return true;
		},
		Select: function(){
			var that = $.winHT005;

			var row = $("#grd"+that.prefix+that.suffix).datagrid("getSelected");
			if(!row){
				$.showMessage('E00008');
				return false;
			}

			// 取得した情報を、オブジェクトに設定する
			// 設定先の判定：オブジェクトに for_btn,for_inpタグなどを使用して呼出し元(呼出しボタン名)と列名が設定されている項目
			var isSet = $.setInputboxRowData('for_btn', that.callBtnid, row);
			if(isSet){
				$('#'+that.suffix).window('close');
			}
			return true;
		},
		validation: function (){	// （必須）批准
			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform+that.suffix).form('validate');
			return rt;
		},
		success: function(id){	// 検索処理
			var that = $.winHT005;
			// 処理時間計測用
			that.timeData = (new Date()).getTime();

			// グリッド初期化&ローディング
			$('#'+id).datagrid('loading');

			// 情報設定
			var json = [{}];

			// 呼出し元別処理
			switch (that.callreportno) {
			case 'Out_ReportHT002':
			case 'Out_ReportHT004':
				// オブジェクト作成
				json = [{
					callpage:	$($.id.hidden_reportno).val(),										// 呼出元レポート名
					BTN:		'test',
					TEN:		parseInt($.getJSONObject(that.callreportInit, 'SelTenpo').value),
					SHUNO:""
				}];
				break;
			case 'Out_ReportHT007':
			case 'Out_ReportHT009':
				// オブジェクト作成
				json = [{
					callpage:	$($.id.hidden_reportno).val(),										// 呼出元レポート名
					BTN:		'test',
					TEN:		parseInt($.getJSONObject(that.callreportInit, 'SelTenpo').value),
					SHUNO:		$.getInputboxValue($('#'+$.id.sel_shunoperiod))
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

					// メインデータ表示
					//that.setData(json.rows);

					// ログ出力
					$.log(that.timeData, 'loaded:');
				}
			);
		},
		setDataGrid: function(id) {
			var that = this;

			// 呼出し元別処理
			var columns = null;
			switch (that.callreportno) {
			case 'Out_ReportHT002':
			case 'Out_ReportHT004':
			case 'Out_ReportHT007':
			case 'Out_ReportHT009':
				// オブジェクト作成
				columns = [[
							{field:'SHNCD',		title:'商品コード',	width: 80,halign:'center',align:'left',
								formatter:function(value,row,index){
									return $.getFormatPrompt(value, '####-####');
								}
							},
							{field:'SHNKN',		title:'商品名',		width: 250,halign:'center',align:'left'},
							{field:'SURYO_MON',	title:'月',			width: 55,halign:'center',align:'right'},
							{field:'SURYO_TUE',	title:'火',			width: 55,halign:'center',align:'right'},
							{field:'SURYO_WED',	title:'水',			width: 55,halign:'center',align:'right'},
							{field:'SURYO_THU',	title:'木',			width: 55,halign:'center',align:'right'},
							{field:'SURYO_FRI',	title:'金',			width: 55,halign:'center',align:'right'},
							{field:'SURYO_SAT',	title:'土',			width: 55,halign:'center',align:'right'},
							{field:'SURYO_SUN',	title:'日',			width: 55,halign:'center',align:'right'},
							{field:'ADDDT',		title:'登録日',		width: 65,halign:'center',align:'left'},
							{field:'UPDDT',		title:'更新日',		width: 65,halign:'center',align:'left'},
							{field:'OPERATOR',	title:'オペレータ',	width: 70,halign:'center',align:'left'},
						]];
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
				rownumbers:true,
				fit:true,
				view:scrollview,
				frozenColumns:[[]],
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
		},
		setData: function(rows, opts){		// データ表示
			var that = this;
			if(rows.length > 0){
				$('#'+that.focusRootId).find('[col^=F]').each(function(){
					//var col = $(this).attr('col');
					var col = 'TENNUMBER'
					if(rows[0][col]){
						$.setInputboxValue($(this), rows[0][col]);
					}
				});
			}
		},
	}
});

})(jQuery);