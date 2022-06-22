/**
 * jquery sub window option
 */
;(function($) {

$.extend({

	winTJ003: {
		name: 'Out_ReportwinTJ003',
		prefix:'_sel_view',
		prefix2:'_bumonyosan',
		suffix:'_winTJ003',
		initializes: true,		// 初期化フラグ（全体）
		initializesCond: true,	// 初期化フラグ（条件用）
		initializesGrid: true,
		lastIndex: -1,			// 編集位置（行）
		sortName: '',			// ソート項目名
		sortOrder: '',			// ソート順
		timeData : (new Date()).getTime(),
		callreportno:"",		// 呼出し元レポートNo
		callBtnid: "",			// 呼出ボタンID情報
		focusRootId:"_winTJ003",	// （キー移動時必須）キー移動イベントのルートとなるパネルのID
		focusParentId:"",		// （キー移動時必須）現在フォーカスがあたっている項目の親となるパネルのID
		focusGridId:"",			// （キー移動時必須）現在フォーカスがあたっているDataGridのID
		inpDayArr:"",
		szLstno:"",
		szBmncd:"",
		init: function(js) {
			var that = this;
			if(!that.initializes) return false;

			that.callreportno = js.name;

			// 呼出しボタンイベント設定
			$('[id^=btn'+that.prefix+']').each(function(){
				var id = $(this).attr('id');
				$('#'+id).click(function() {

					// 部門、リスト№を取得
					for (var i = 0; i < js.jsonHidden.length; i++) {
						if (js.jsonHidden[i].id===$.id.SelBumon) {
							that.szBmncd = js.jsonHidden[i].value;
						} else if (js.jsonHidden[i].id===$.id_inp.txt_lstno) {
							that.szLstno = js.jsonHidden[i].value;
						}
					}

					if (that.Open(this)) {

						that.Search();

						// window 表示
						$('#'+that.suffix).window('open');
					}
				});
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
				height:540,
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
			var that = $.winTJ003;
			that.callBtnid = $(obj).attr('id');

			// 呼出し元別処理
			switch (that.callreportno) {
			case 'Out_ReportTJ001':

				// オブジェクト作成
				$('#'+that.suffix).window({title: '特売販売計画＆事前発注 分類明細(TJ003)'});

				break;
			default:
				break;
			}
			return true;
		},
		Clear:function(){
			var that = $.winTJ003;
			that.initializesCond = false;
			// グリッド初期化
			$('#grd_subwindow'+that.prefix+that.suffix).datagrid('loadData',[]);
			$('#grd_subwindow'+that.prefix2+that.suffix).datagrid('loadData',[]);
			that.initializesCond = true;
		},
		Search: function(){
			var that = $.winTJ003;

			// validate=falseの場合何もしない
			if(!that.validation()){ return false; }

			// セッションタイムアウト、利用時間外の確認
			var isTimeout = $.checkIsTimeout();
			if (! isTimeout) {
				// 検索実行
				that.success('grd_subwindow'+that.prefix+that.suffix);
			}

			return true;
		},
		Cancel:function(){
			var that = $.winTJ003;
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
			var that = $.winTJ003;
			// 処理時間計測用
			that.timeData = (new Date()).getTime();

			// 情報設定
			var json = [{}];

			// 呼出し元別処理
			switch (that.callreportno) {
			case 'Out_ReportTJ001':
				// オブジェクト作成
				json = [{
					callpage:	$($.id.hidden_reportno).val(),									// 呼出元レポート名
					BMNCD:	that.szBmncd,
					LSTNO:	that.szLstno,
				}];
				break;
			default:
				break;
			}

			// グリッド初期化&ローディング
			$('#'+id).datagrid('loading');

			$.post(
				$.reg.easy,
				{
					page	:	that.name,										// レポート名
					obj		:	id,
					sel		:	(new Date()).getTime(),
					target	:	id,
					action	:	$.id.action_init,
					json	:	JSON.stringify(json),
				},
				function(data){
					// ログ出力
					$.log(that.timeData, 'query:');
					if(data!==""){
						// JSONに変換
						var json = JSON.parse(data);

						// メインデータ表示
						that.setData(json.rows);

						if (that.initializesGrid) {
							// dataGrid 初期化
							that.setDataGrid('grd_subwindow'+that.prefix+that.suffix);
							that.setDataGrid('grd_subwindow'+that.prefix2+that.suffix);
							that.initializesGrid = false;
						} else {
							// グリッド初期化&ローディング
							$('#grd_subwindow'+that.prefix+that.suffix).datagrid('load');
						}
					}

					// ログ出力
					$.log(that.timeData, 'loaded:');
				}
			);
		},
		setDataGrid: function(id) {
			var that = this;

			var funcBeforeLoad = function (param) {
				param.report = that.name;
			}
			var url = $.fn.datagrid.defaults.url;
			var cstyler1 =function(value,row,index){
				return 'background-color:#f5f5f5;';
			};

			var iformatter_s = function(value,row,index){
				if(row.F2 === '1' || row.F2 === '2'){
					return $.getFormat(value, '#,##0');
				} else {
					return $.getFormat(value, '#,##0.0')+'%';
				}
			};

			var columns = [];
			columns.push([
			    {field:'F1',	title:'',	hidden:true},
			    {field:'F2',	title:'',	hidden:true},
				{field:'F3',	title:'',	width:55,halign:'center',align:'left',styler:cstyler1},
				{field:'F4',	title:'',	width:60,halign:'center',align:'left'},
				{field:'F5',	title:'',	width:60,halign:'center',align:'right',formatter:iformatter_s},
				{field:'F6',	title:'',	width:60,halign:'center',align:'right',formatter:iformatter_s},
				{field:'F7',	title:'',	width:60,halign:'center',align:'right',formatter:iformatter_s},
				{field:'F8',	title:'',	width:60,halign:'center',align:'right',formatter:iformatter_s},
				{field:'F9',	title:'',	width:60,halign:'center',align:'right',formatter:iformatter_s},
				{field:'F10',	title:'',	width:60,halign:'center',align:'right',formatter:iformatter_s},
				{field:'F11',	title:'',	width:60,halign:'center',align:'right',formatter:iformatter_s},
				{field:'F12',	title:'',	width:60,halign:'center',align:'right',formatter:iformatter_s},
				{field:'F13',	title:'',	width:60,halign:'center',align:'right',formatter:iformatter_s},
				{field:'F14',	title:'',	width:60,halign:'center',align:'right',formatter:iformatter_s},
				{field:'F15',	title:'',	width:60,halign:'center',align:'right',formatter:iformatter_s},
				{field:'F16',	title:'',	hidden:true},
				{field:'F17',	title:'',	hidden:true},
				{field:'F18',	title:'',	hidden:true},
				{field:'F19',	title:'',	hidden:true},
				{field:'F20',	title:'',	hidden:true},
				{field:'F21',	title:'',	hidden:true},
				{field:'F22',	title:'',	hidden:true},
				{field:'F23',	title:'',	hidden:true},
				{field:'F24',	title:'',	hidden:true},
				{field:'F25',	title:'',	hidden:true},
			]);
			var field = 'F3';

			switch (that.callreportno) {
			case 'Out_ReportTJ001':
				if (id==='grd_subwindow'+that.prefix+that.suffix) {
					// 呼出し元別処理
					url = $.reg.easy;
					funcBeforeLoad = function(param){

						var json = [{"callpage":"Out_ReportTJ003","LISTNO":that.szLstno,"BMNCD":that.szBmncd,"INPDAYARR":that.inpDayArr}];
						// 情報設定
						param.page		=	that.name;
						param.obj		=	'grd_subwindow'+that.prefix+'_main'+that.suffix;
						param.sel		=	(new Date()).getTime();
						param.target	=	id;
						param.action	=	$.id.action_init;
						param.json		=	JSON.stringify(json);
						param.datatype	=	"datagrid";
						$('#'+that.suffix).find('.datagrid-header').hide();
					};
				}
				break;
			default:
				break;
			}

			$('#'+id).datagrid({
				url:url,
				nowrap: true,
				border: true,
				striped: true,
				collapsible:false,
				remoteSort: true,
				rownumbers:false,
				fit:true,
				frozenColumns:[[]],
				columns:columns,
				fitColumns:false,	// 指定カラム幅を適用する場合、false 指定。
				onBeforeLoad:funcBeforeLoad,
				rowStyler:function(index, row){
				},
				onLoadSuccess:function(data){
					// セルのマージ
					var data = $('#'+id).datagrid('getRows');
					var length = data.length/4;
					for (var i=0; i<length; i++){
						var index = i*4;
						$('#'+id).datagrid('mergeCells',{index:index,field:field,rowspan:4});
					}

					// マスク削除
					$.removeMask();
					if($('#'+id).hasClass("datagrid-f")){
						$('#'+id).datagrid('loaded');
					}
					$.removeMaskMsg();

					if (id==='grd_subwindow'+that.prefix+that.suffix && that.initializesCond) {

						var rowDate = {}	// 初期化
						var targetRows= [];
						var uriYosan = 0;
						var htBaika = 0;
						var htBaika_n = 0;
						var arari = 0;
						for (var i=0; i<data.length; i++){
							var no = data[i]["F2"];
							var num = 3;
							if (no==='1') {
								num = 0;
							} else if (no==='2') {
								num = 1;
							} else if (no==='3') {
								num = 2;
							}

							// 隠し項目
							for (var col = 16; col <= 25; col++) {
								// 売上予算 or 発注売価は単純な加算
								var colName = "F"+col;
								data[num][colName] = data[num][colName]*1 + data[i][colName]*1;

								if (no==='1') {
									uriYosan += data[i][colName]*1;
								} else if (no==='2') {
									htBaika += data[i][colName]*1;
								} else if (no==='3') {
									htBaika_n += data[i][colName]*1;
								} else if (no==='4') {
									arari += data[i][colName]*1;
								}
							}
						}

						var kikanCol = 'F26';
						for (var i = 0; i < 3; i++) {

							// 隠し項目
							if (i == 0) {
								data[i][kikanCol] = uriYosan;
							} else if (i == 1) {
								data[i][kikanCol] = htBaika;
							} else {

								i += 1;

								// 退避
								data[i][kikanCol] = arari;

								for (var col = 16; col <= 25; col++) {

									// 発注売価と荒利の取り出し
									var colName = "F"+col;
									arari = data[i][colName];
									htBaika = data[2][colName];

									if (htBaika == 0 || htBaika==='0.0') {
										data[i][colName] = '0.0';
									} else {
										data[i][colName] = (arari / htBaika) * 100;
										data[i][colName] = Math.round(data[i][colName] * 10)/10;
									}
								}

								// 期間計
								if (htBaika_n===0.0) {
									data[i][kikanCol] = '0.0';
								} else {
									data[i][kikanCol] = (data[i][kikanCol] / htBaika_n) * 100;
									data[i][kikanCol] = Math.round(data[i][kikanCol] * 10)/10;
								}

								i -= 1;

								for (var col = 16; col <= 25; col++) {

									// 売上予算と発注売価の取り出し
									var colName = "F"+col;
									uriYosan = data[0][colName];
									htBaika = data[1][colName];

									if (uriYosan===0) {
										data[i][colName] = '0.0';
									} else {
										data[i][colName] = (htBaika / uriYosan) * 100;
										data[i][colName] = Math.round(data[i][colName] * 10)/10;
									}
								}

								// 期間計
								if (data[0][kikanCol]===0) {
									data[i][kikanCol] = '0.0';
								} else {
									data[i][kikanCol] = (data[1][kikanCol] / data[0][kikanCol]) * 100;
									data[i][kikanCol] = Math.round(data[i][kikanCol] * 10)/10;
								}
							}
						}
						for (var i = 0; i < 4; i++) {

							var rowDate = {
									F1	 : data[i]["F1"],
									F2	 : data[i]["F2"],
									F3	 : '部門計',
									F4	 : data[i]["F4"],
									F5	 : data[i]["F16"],
									F6	 : data[i]["F17"],
									F7	 : data[i]["F18"],
									F8	 : data[i]["F19"],
									F9	 : data[i]["F20"],
									F10	 : data[i]["F21"],
									F11	 : data[i]["F22"],
									F12	 : data[i]["F23"],
									F13	 : data[i]["F24"],
									F14	 : data[i]["F25"],
									F15	 : data[i]["F26"]
							};

							targetRows.push(rowDate);
						}
						$('#grd_subwindow'+that.prefix2+that.suffix).datagrid('loadData',targetRows);
					}
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
					var col = $(this).attr('col');
					if(rows[0][col]){
						$.setInputboxValue($(this), rows[0][col]);
					}
				});
			}

			// 日付の保持
			that.inpDayArr = [];
			for (var i = 1; i <= 10; i++) {
				that.inpDayArr.push(rows[0]['X'+i]);
			}
		},
	}
});

})(jQuery);