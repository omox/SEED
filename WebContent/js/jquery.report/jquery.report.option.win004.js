/**
 * jquery sub window option
 */
;(function($) {

$.extend({

	win004: {
		name: 'Out_Reportwin004',
		prefix:'_hsptn',
		suffix:'_win004',
		initializes: true,		// 初期化フラグ（全体）
		initializesCond: true,	// 初期化フラグ（条件用）
		lastIndex: -1,			// 編集位置（行）
		sortName: '',			// ソート項目名
		sortOrder: '',			// ソート順
		timeData : (new Date()).getTime(),
		callreportno:"",		// 呼出し元レポートNo
		callBtnid: "",			// 呼出ボタンID情報
		focusRootId:"_win004",	// （キー移動時必須）キー移動イベントのルートとなるパネルのID
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
			$('#'+$.id.btn_search+that.suffix).on("click", that.Search);
			// キャンセル
			$('#'+$.id.btn_cancel+that.suffix).on("click", that.Cancel);
			// 選択
			$('#'+$.id.btn_select+that.suffix).on("click", that.Select);

			// 検索条件表示切替
			if(that.callreportno === 'Out_Reportx152'){
				$('#'+'tool').show()
				$.setInputbox(that, that.callreportno, $.id_inp.txt_hsptn+that.suffix,	 true);
				$.setInputbox(that, that.callreportno, $.id_inp.txt_hsptnkn+that.suffix, true);

			}else{
				$('#'+'tool').hide()

			};

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
			var that = $.win004;
			that.callBtnid = $(obj).attr('id');

			// 検索条件無しの場合は即検索実行
			if(that.callreportno !== 'Out_Reportx152'){
				that.Display();
			}

			// window 表示
			$('#'+that.suffix).window('open');
		},
		Clear:function(){
			var that = $.win004;
			that.initializesCond = true;
			// グリッド初期化
			$('#grd'+that.prefix+that.suffix).datagrid('clearSelections').datagrid({data: []}).datagrid('getPager').pagination('select', 1);	// ページ初期化
			that.initializesCond = false;
		},
		Search: function(){
			var that = $.win004;

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
		Display: function(){
			var that = $.win004;

			// セッションタイムアウト、利用時間外の確認
			var isTimeout = $.checkIsTimeout();
			if (! isTimeout) {
				// 検索実行
				that.success("grd"+that.prefix+that.suffix);
			}

			return true;
		},
		Cancel:function(){
			var that = $.win004;
			$('#'+that.suffix).window('close');
			return true;
		},
		Select: function(){
			var that = $.win004;

			var row = $("#grd"+that.prefix+that.suffix).datagrid("getSelected");
			if(!row){
				$.showMessage('E00008');
				return false;
			}

			var isSet = false;
			// 呼出し元別処理
			switch (that.callreportno) {
			case 'Out_Reportx002':
			case 'Out_Reportx247':
			case 'Out_Reportx251':
				if(that.callBtnid==='btn'+that.prefix){
					isSet = $.setInputboxRowData('for_btn', that.callBtnid, row);
				}else{
					var id = that.callBtnid.replace('btn'+that.prefix, "");
					// レポート定義位置
					var reportNumber = $.getReportNumber(that.callreportno);
					if($.report[reportNumber].editRowIndex[id]!==-1){
						var parentobj = $('#'+id).datagrid('getPanel');
						$.setInputboxValue(parentobj.find('#'+$.id_inp.txt_hsptn+"_"), row["F1"]);
						$.setInputboxValue(parentobj.find('#'+$.id_inp.txt_hsptnkn+"_"), row["F2"]);
						isSet = true;
					}else{
						var rows = $('#'+id).datagrid('getRows');
						for (var i=0; i<rows.length; i++){
							if(rows[i]["HSPTN"]===undefined || rows[i]["HSPTN"].length === 0){
								$('#'+id).datagrid('updateRow',{
									index: i,
									row:{
										HSPTN:row["F1"],
										HSPTNKN:row["F2"]
									}
								})
								isSet = true;
								break;
							}
						}
					}
				}
				break;
			case 'Out_Reportx152':
				var id = $.id.grd_hsptn + '_list';
				// レポート定義位置
				var rows = $('#'+id).datagrid('getRows');

				// 行選択している場合、選択行をEdit化し値を設定する。
				/*var selectRows = $('#'+id).datagrid('getSelected');

				if(selectRows){
					var rowIndex = $('#'+id).datagrid("getRowIndex", selectRows);
					isSet = true;
					$('#'+id).datagrid('beginEdit', rowIndex);
					$('#'+$.id_inp.txt_hsptn + '_').numberbox('reset').numberbox('setValue', row["F1"]);

					break;
				}*/

				for (var i=0; i<rows.length; i++){
					if(rows[i]["HSPTN"]===undefined || rows[i]["HSPTN"].length === 0){
						/*$('#'+id).datagrid('updateRow',{
							index: i,
							row:{
								HSPTN		:row["F1"],
								HSPTNKN		:row["F2"],
								CENTERCD	:row["F3"],
								YCENTERCD	:row["F4"]
							}
						})*/
						isSet = true;

						$('#'+id).datagrid('beginEdit', i);
						$('#'+$.id_inp.txt_hsptn + '_').numberbox('reset').numberbox('setValue', row["F1"]);

						break;
					}
				}

				//var newval = $('#'+$.id_inp.txt_shncd).numberbox('getValue');
				//$('#'+$.id_inp.txt_shncd).numberbox('reset').numberbox('setValue', newval);

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

			// 検索条件指定なしの場合エラー
			var hsptn	 = $('#'+$.id_inp.txt_hsptn+that.suffix).textbox('getValue')			// 配送パターン
			var hsptnkn	 = $('#'+$.id_inp.txt_hsptnkn+that.suffix).textbox('getValue')			// 配送パターン名称（漢字）
			if(!hsptn && !hsptnkn ){
				$.showMessage('EX1013');
				rt = false;
			}

			return rt;
		},
		success: function(id){	// 検索処理
			var that = $.win004;
			// 処理時間計測用
			that.timeData = (new Date()).getTime();

			// グリッド初期化&ローディング
			$('#'+id).datagrid('loading');

			// 情報設定
			var json = [{}];

			// 呼出し元別処理
			switch (that.callreportno) {
			case 'Out_Reportx002':
			case 'Out_Reportx152':
			case 'Out_Reportx247':
			case 'Out_Reportx251':
				// オブジェクト作成
				json = [{
					callpage:	$($.id.hidden_reportno).val(),											// 呼出元レポート名
					HSPTN:		$('#'+$.id_inp.txt_hsptn+that.suffix).textbox('getValue'),				// 仕入先（漢字）
					HSPTNKN:	$('#'+$.id_inp.txt_hsptnkn+that.suffix).textbox('getValue')				// 仕入先（漢字）

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
			case 'Out_Reportx002':
			case 'Out_Reportx152':
			case 'Out_Reportx247':
			case 'Out_Reportx251':
				// オブジェクト作成
				columns = [[
							{field:'F1', title:'配送パターン',		width:105, halign:'center',align:'left',formatter:function(value,row,index){ return $.getFormatLPad(value, $.len.hsptn);}},
							{field:'F2', title:'配送パターン名称',	width:230, halign:'center',align:'left'},
							{field:'F3', title:'センターコード',	width:105, halign:'center',align:'left'},
							{field:'F4', title:'横持先センター',	width:105, halign:'center',align:'left'}
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