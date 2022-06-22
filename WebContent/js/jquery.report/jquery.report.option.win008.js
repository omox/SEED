/**
 * jquery sub window option
 */
;(function($) {

$.extend({

	win008: {
		name: 'Out_Reportwin008',
		prefix:'_ehsptn',
		suffix:'_win008',
		initializes: true,		// 初期化フラグ（全体）
		initializesCond: true,	// 初期化フラグ（条件用）
		lastIndex: -1,			// 編集位置（行）
		sortName: '',			// ソート項目名
		sortOrder: '',			// ソート順
		timeData : (new Date()).getTime(),
		callreportno:"",		// 呼出し元レポートNo
		callBtnid: "",			// 呼出ボタンID情報
		focusRootId:"_win008",	// （キー移動時必須）キー移動イベントのルートとなるパネルのID
		focusParentId:"",		// （キー移動時必須）現在フォーカスがあたっている項目の親となるパネルのID
		focusGridId:"",			// （キー移動時必須）現在フォーカスがあたっているDataGridのID
		init: function(js) {
			var that = this;
			if(!that.initializes) return false;

			that.callreportno = js.name;

			// dataGrid 初期化
			this.setDataGrid('#grd'+that.prefix+that.suffix);

			// 呼出しボタンイベント設定
			$('[id^=btn'+that.prefix+']').each(function(){
				var id = $(this).attr('id');
				$('#'+id).click(function() { that.Open(this); });
			});
			// 検索
			// $('#'+$.id.btn_search+that.suffix).on("click", that.Search);
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
			var that = $.win008;
			that.callBtnid = $(obj).attr('id');

			that.Search();

			// window 表示
			$('#'+that.suffix).window('open');
		},
		Clear:function(){
			var that = $.win008;
			that.initializesCond = true;
			// グリッド初期化
			$('#grd'+that.prefix+that.suffix).datagrid('clearSelections').datagrid({data: []}).datagrid('getPager').pagination('select', 1);	// ページ初期化
			that.initializesCond = false;
		},
		Search: function(){
			var that = $.win008;

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
			var that = $.win008;
			$('#'+that.suffix).window('close');
			return true;
		},
		Select: function(){
			var that = $.win008;

			var row = $("#grd"+that.prefix+that.suffix).datagrid("getSelected");
			if(!row){
				$.showMessage('E00008');
				return false;
			}

			var isSet = false;
			// 呼出し元別処理
			switch (that.callreportno) {
			case 'Out_Reportx152':
				var id = $.id.grd_ehsptn + '_list';
				// レポート定義位置
				var rows = $('#'+id).datagrid('getRows');

				// 行選択している場合、選択行をEdit化し値を設定する。
				/*var selectRows = $('#'+id).datagrid('getSelected');
				if(selectRows){
					var rowIndex = $('#'+id).datagrid("getRowIndex", selectRows);
					isSet = true;
					$('#'+id).datagrid('beginEdit', rowIndex);
					$('#'+$.id_inp.txt_hsptn + '_').numberbox('reset').numberbox('setValue', row["F1"]);
					$('#'+$.id_inp.txt_tengpcd + '_').numberbox('reset').numberbox('setValue', row["F3"]);

					break;
				}*/
				for (var i=0; i<rows.length; i++){
					if(rows[i]["HSPTN"]===undefined || rows[i]["HSPTN"].length === 0){
						/*$('#'+id).datagrid('updateRow',{
							index: i,
							row:{
								HSPTN		:row["F1"],
								HSPTNKN		:row["F2"],
								CENTERCD	:row["F5"],
								YCENTERCD	:row["F6"],
								TENGPCD		:row["F3"],
								TENGPKN		:row["F4"],
							}
						})*/
						isSet = true;

						$('#'+id).datagrid('beginEdit', i);
						$('#'+$.id_inp.txt_hsptn + '_').numberbox('reset').numberbox('setValue', row["F1"]);
						$('#'+$.id_inp.txt_tengpcd + '_').numberbox('reset').numberbox('setValue', row["F3"]);

						break;
					}
				}
				break;
			default:
				// 取得した情報を、オブジェクトに設定する
				// 設定先の判定：オブジェクトに for_btn,for_inpタグなどを使用して呼出し元(呼出しボタン名)と列名が設定されている項目
				isSet = $.setInputboxRowData('for_btn', that.callBtnid, row);
				break;
			}

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
			var that = $.win008;
			// 処理時間計測用
			that.timeData = (new Date()).getTime();

			// グリッド初期化&ローディング
			$('#'+id).datagrid('loading');

			// 情報設定
			var json = [{}];

			// 呼出し元別処理
			switch (that.callreportno) {
			case 'Out_Reportx152':
				// オブジェクト作成
				json = [{
					callpage:	$($.id.hidden_reportno).val()										// 呼出元レポート名
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

			// 呼出し元別処理
			var columns = null;
			switch (that.callreportno) {
			case 'Out_Reportx152':
				// オブジェクト作成
				columns = [[
							{field:'F1', title:'配送パターン',		width:100, halign:'center',align:'left'},
							{field:'F2', title:'配送パターン名称',	width:200, halign:'center',align:'left'},
							{field:'F3', title:'店グループ',		width: 80, halign:'center',align:'left'},
							{field:'F4', title:'店グループ名',		width:200, halign:'center',align:'left'},
							{field:'F5', title:'センターコード',	width:100, halign:'center',align:'left'},
							{field:'F6', title:'横持先センター',	width:100, halign:'center',align:'left'},
							{field:'F7', title:'店舗数',			width: 60, halign:'center',align:'right'},
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
		}
	}
});

})(jQuery);