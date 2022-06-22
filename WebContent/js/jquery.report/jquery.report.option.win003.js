/**
 * jquery sub window option
 */
;(function($) {

$.extend({

	win003: {
		name: 'Out_Reportwin003',
		prefix:'_tengp',
		suffix:'_win003',
		initializes: true,		// 初期化フラグ（全体）
		initializesCond: true,	// 初期化フラグ（条件用）
		lastIndex: -1,			// 編集位置（行）
		sortName: '',			// ソート項目名
		sortOrder: '',			// ソート順
		timeData : (new Date()).getTime(),
		callreportno:"",		// 呼出し元レポートNo
		callBtnid: "",			// 呼出ボタンID情報
		focusRootId:"_win003",	// （キー移動時必須）キー移動イベントのルートとなるパネルのID
		focusParentId:"",		// （キー移動時必須）現在フォーカスがあたっている項目の親となるパネルのID
		focusGridId:"",			// （キー移動時必須）現在フォーカスがあたっているDataGridのID
		gpkbn:'',				// グループ区分：店グループは仕様(1)、売価(2)、品揃(3)などで利用されている
		init: function(js) {
			var that = this;
			if(!that.initializes) return false;

			that.callreportno = js.name;

			// dataGrid 初期化
			this.setDataGrid('#grd'+that.prefix+that.suffix);

			// 検索条件初期化
			$.setInputbox(that, that.callreportno, $.id_inp.txt_tengpkn+that.suffix, false);

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
			var that = $.win003;

			// 標準部門コード入力チェック
			switch (that.callreportno) {
			case 'Out_Reportx002':
			case 'Out_Reportx247':
			case 'Out_Reportx251':
				var bmncd = $.getInputboxValue($('#'+$.id_inp.txt_bmncd));
				if(bmncd.length < 1 || bmncd === "0"){

					alert("部門コードを入力してください");
					return false;
				}

				break;
			default:
				break;

			}

			that.callBtnid = $(obj).attr('id');
			// グループ区分取得：ボタンIDの末尾にくっついている前提
			that.gpkbn = $(obj).attr('id').replace('btn'+that.prefix, "");

			// window 表示
			$('#'+that.suffix).window('open');
		},
		Clear:function(){
			var that = $.win003;
			that.initializesCond = true;
			// グリッド初期化
			$('#grd'+that.prefix+that.suffix).datagrid('clearSelections').datagrid({data: []}).datagrid('getPager').pagination('select', 1);	// ページ初期化
			that.initializesCond = false;
		},
		Search: function(){
			var that = $.win003;

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
			var that = $.win003;
			$('#'+that.suffix).window('close');
			return true;
		},
		Select: function(){
			var that = $.win003;

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
				var id = $.id.grd_tengp+that.gpkbn;
				// レポート定義位置
				var reportNumber = $.getReportNumber(that.callreportno);
				if($.report[reportNumber].editRowIndex[id]!==-1){
					var parentobj = $('#'+id).datagrid('getPanel');
					$.setInputboxValue(parentobj.find('#'+$.id_inp.txt_tengpcd+"_"), row["F1"]);
					$.setInputboxValue(parentobj.find('#'+$.id_inp.txt_tengpkn+"_"), row["F2"]);
					isSet = true;
				}else{
					var rows = $('#'+id).datagrid('getRows');
					for (var i=0; i<rows.length; i++){
						if(rows[i]["TENGPCD"]===undefined || rows[i]["TENGPCD"].length === 0){
							$('#'+id).datagrid('updateRow',{
								index: i,
								row:{
									TENGPCD:row["F1"],
									TENGPKN:row["F2"]
								}
							})
							isSet = true;
							break;
						}
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
			var that = $.win003;
			// 処理時間計測用
			that.timeData = (new Date()).getTime();

			// グリッド初期化&ローディング
			$('#'+id).datagrid('loading');

			// 情報設定
			var json = [{}];

			// 呼出し元別処理
			switch (that.callreportno) {
			case 'Out_Reportx002':
			case 'Out_Reportx247':
			case 'Out_Reportx251':
				// オブジェクト作成
				var gpkbn = that.gpkbn;
				// ※店異部門のみ特別！売価グループ区分を用いる
				if(that.gpkbn===$.id.value_gpkbn_tbmn){
					gpkbn=$.id.value_gpkbn_baika;
				}
				json = [{
					callpage:	$($.id.hidden_reportno).val(),									// 呼出元レポート名
					GPKBN:		gpkbn,															// グループ区分
					AREAKBN:	$("input[name="+$.id.rad_areakbn+that.gpkbn+"]:checked").val(),	// エリア区分
					BMNCD:		$.getInputboxValue($('#'+$.id_inp.txt_bmncd)),					// 標準部門コード
					TENGPKN:	$('#'+$.id_inp.txt_tengpkn+that.suffix).textbox('getValue')		// メーカー名（漢字）
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

						if(Number(json.total) === 0){
							$.showMessage('E11003');
						}

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
			switch ($($.id.hidden_reportno).val()) {
			case 'Out_Reportx002':
			case 'Out_Reportx247':
			case 'Out_Reportx251':
				// オブジェクト作成
				columns = [[
							{field:'F1', title:'店グループ',	width: 95, halign:'center',align:'left',formatter:function(value,row,index){ return $.getFormatLPad(value, $.len.tengp);}},
							{field:'F2', title:'店グループ名',	width:300, halign:'center',align:'left'},
							{field:'F3', title:'店舗数',		width: 95, halign:'center',align:'right'}
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