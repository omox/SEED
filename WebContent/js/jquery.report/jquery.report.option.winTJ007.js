/**
 * jquery sub window option
 */
;(function($) {

$.extend({

	winTJ007: {
		name: 'Out_ReportwinTJ007',
		prefix:'_sel_view',
		prefix2:'_bumonyosan',
		suffix:'_winTJ007',
		initializes: true,		// 初期化フラグ（全体）
		initializesCond: true,	// 初期化フラグ（条件用）
		initializesGrid: true,
		lastIndex: -1,			// 編集位置（行）
		sortName: '',			// ソート項目名
		sortOrder: '',			// ソート順
		timeData : (new Date()).getTime(),
		callreportno:"",		// 呼出し元レポートNo
		callBtnid: "",			// 呼出ボタンID情報
		focusRootId:"_winTJ007",	// （キー移動時必須）キー移動イベントのルートとなるパネルのID
		focusParentId:"",		// （キー移動時必須）現在フォーカスがあたっている項目の親となるパネルのID
		focusGridId:"",			// （キー移動時必須）現在フォーカスがあたっているDataGridのID
		inpDayArr:"",
		szLstno:"",
		szBmncd:"",
		outRowIndex:5,
		daiBruiCnt:0,
		gridData:[],			// 検索結果保持用(催しコード/催し基本)
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
			var that = $.winTJ007;
			that.callBtnid = $(obj).attr('id');

			// 呼出し元別処理
			switch (that.callreportno) {
			case 'Out_ReportTJ005':

				// オブジェクト作成
				$('#'+that.suffix).window({title: '特売販売計画＆事前発注 分類明細(TJ007)'});

				break;
			default:
				break;
			}
			return true;
		},
		Clear:function(){
			var that = $.winTJ007;
			that.initializesCond = false;
			// グリッド初期化
			$('#grd_subwindow'+that.prefix+that.suffix).datagrid('loadData',[]);
			$('#grd_subwindow'+that.prefix2+that.suffix).datagrid('loadData',[]);
			that.gridData = [];
			that.outRowIndex = 5;
			that.initializesCond = true;
		},
		Search: function(){
			var that = $.winTJ007;

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
			var that = $.winTJ007;
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
			var that = $.winTJ007;
			// 処理時間計測用
			that.timeData = (new Date()).getTime();

			// 情報設定
			var json = [{}];

			// 呼出し元別処理
			switch (that.callreportno) {
			case 'Out_ReportTJ005':
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

			var cstyler2 =function(value,row,index){
				if (!$.isEmptyVal(value)) {
					return '';
				} else {
					return 'background-color:#f5f5f5;';
				}
			};

			var iformatter_s = function(value,row,index){
				if((row.F3 === '1' && row.F6 === '3') || (row.F3 === '0' && row.F6 === '2')){
					return $.getFormat(value, '#,##0')+'P';
				} else if((row.F3 === '0' && row.F6 === '1') || (row.F3 === '2' && row.F6 !== '1') || (row.F3 === '1' && row.F6 !== '3')|| row.F3 === '3') {
					return $.getFormat(value, '#,##0')+'千円';
				} else {
					return $.getFormat(value, '#,##0.0')+'kg';
				}
			};

			var iformatter_s2 = function(value,row,index){
				if ($.isEmptyVal(value)) {
					return '';
				} else {
					if(row.F3 === '3' && row.F6 === '4'){
						return $.getFormat(value, '#,##0')+'P';
					} else if(row.F3 === '3' && row.F6 === '5') {
						return $.getFormat(value, '#,##0.0')+'%';
					}
				}
			};

			var iformatter_s3 = function(value,row,index){
				return $.getFormat(value, '#,##0')+'千円';
			};

			var iformatter_keitai = function(value,row,index){
				if (row.F3 === '1' ) {
					return 'センター<br>パック';
				} else if (row.F3 === '2' ) {
						return '原料';
				} else if (row.F3 === '3' ) {
					return '発注計';
				} else {
					return value;
				}
			};

			var iformatter_koumoku = function(value,row,index){
				if(row.F3 === '0' && row.F6 === '1'){
					return '売上予算';
				} else if (row.F3 === '0' && row.F6 === '2') {
					return '予定数量';
				} else if (row.F3 === '1' && row.F6 === '3') {
					return '発注数量';
				} else if ((row.F3 === '1' && row.F6 === '4') || (row.F3 === '2' && row.F6 === '2')) {
					return '発注売価';
				} else if ((row.F3 === '1' && row.F6 === '5') || (row.F3 === '2' && row.F6 === '3')) {
					return '発注原価';
				} else if (row.F3 === '2' && row.F6 === '1') {
					return '発注重量';
				} else if (row.F3 === '3' && row.F6 === '4') {
					return '売価計';
				} else if (row.F3 === '3' && row.F6 === '5') {
					return '原価計';
				} else {
					return value;
				}
			};

			var columnBottom=[];
			var columns = [];
			columnBottom.push({field:'F1',	title:'',	width:65,halign:'center',align:'left',styler:cstyler1});
			columnBottom.push({field:'F2',	title:'',	width:60,halign:'center',align:'left'});
			columnBottom.push({field:'F3',	title:'',	width:60,halign:'center',align:'right',formatter:iformatter_s3});
			columnBottom.push({field:'F4',	title:'',	width:60,halign:'center',align:'right',formatter:iformatter_s3});
			columnBottom.push({field:'F5',	title:'',	width:60,halign:'center',align:'right',formatter:iformatter_s3});
			columnBottom.push({field:'F6',	title:'',	width:60,halign:'center',align:'right',formatter:iformatter_s3});
			columnBottom.push({field:'F7',	title:'',	width:60,halign:'center',align:'right',formatter:iformatter_s3});
			columnBottom.push({field:'F8',	title:'',	width:60,halign:'center',align:'right',formatter:iformatter_s3});
			columnBottom.push({field:'F9',	title:'',	width:60,halign:'center',align:'right',formatter:iformatter_s3});
			columnBottom.push({field:'F10',	title:'',	width:60,halign:'center',align:'right',formatter:iformatter_s3});
			columnBottom.push({field:'F11',	title:'',	width:60,halign:'center',align:'right',formatter:iformatter_s3});
			columnBottom.push({field:'F12',	title:'',	width:60,halign:'center',align:'right',formatter:iformatter_s3});
			columnBottom.push({field:'F13',	title:'',	width:60,halign:'center',align:'right',formatter:iformatter_s3});

			switch (that.callreportno) {
			case 'Out_ReportTJ005':
				if (id==='grd_subwindow'+that.prefix+that.suffix) {
					// 呼出し元別処理
					url = $.reg.easy;
					funcBeforeLoad = function(param){

						var json = [{"callpage":"Out_ReportTJ007","LISTNO":that.szLstno,"BMNCD":that.szBmncd,"OUTROWINDEX":that.outRowIndex,"INPDAYARR":that.inpDayArr}];
						// 情報設定
						param.page		=	that.name;
						param.obj		=	'grd_subwindow'+that.prefix+'_main'+that.suffix;
						param.sel		=	(new Date()).getTime();
						param.target	=	id;
						param.action	=	$.id.action_init;
						param.json		=	JSON.stringify(json);
						param.datatype	=	"datagrid";
					};

					columnBottom=[];
					columnBottom.push({field:'F2',	title:'',	width:10,halign:'center',align:'left',styler:cstyler1});
					columnBottom.push({field:'F4',	title:'',	width:55,halign:'center',align:'left',styler:cstyler1,formatter:iformatter_keitai});
					columnBottom.push({field:'F5',	title:'',	width:60,halign:'center',align:'left',formatter:iformatter_koumoku});
					columnBottom.push({field:'F7',	title:'',	width:60,halign:'center',align:'right',formatter:iformatter_s});
					columnBottom.push({field:'F8',	title:'',	width:60,halign:'center',align:'right',formatter:iformatter_s});
					columnBottom.push({field:'F9',	title:'',	width:60,halign:'center',align:'right',formatter:iformatter_s});
					columnBottom.push({field:'F10',	title:'',	width:60,halign:'center',align:'right',formatter:iformatter_s});
					columnBottom.push({field:'F11',	title:'',	width:60,halign:'center',align:'right',formatter:iformatter_s});
					columnBottom.push({field:'F12',	title:'',	width:60,halign:'center',align:'right',formatter:iformatter_s});
					columnBottom.push({field:'F13',	title:'',	width:60,halign:'center',align:'right',formatter:iformatter_s});
					columnBottom.push({field:'F14',	title:'',	width:60,halign:'center',align:'right',formatter:iformatter_s});
					columnBottom.push({field:'F15',	title:'',	width:60,halign:'center',align:'right',formatter:iformatter_s});
					columnBottom.push({field:'F16',	title:'',	width:60,halign:'center',align:'right',formatter:iformatter_s});
					columnBottom.push({field:'F17',	title:'',	width:60,halign:'center',align:'right',formatter:iformatter_s});
					columnBottom.push({field:'F18',	title:'',	width:60,halign:'center',align:'right',styler:cstyler2,formatter:iformatter_s2});
					columnBottom.push({field:'F1',	title:'',	hidden:true});
					columnBottom.push({field:'F3',	title:'',	hidden:true});
					columnBottom.push({field:'F6',	title:'',	hidden:true});
					$('#'+that.suffix).find('.datagrid-header').hide();
				}
				break;
			default:
				break;
			}

			columns.push(columnBottom);

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
					var length = data.length/10;

					// 次のロード対象のデータを取得
					if (that.daiBruiCnt >= that.outRowIndex && that.initializesCond) {
						for (var i = 0; i < data.length; i++) {

							var rowDate = {
									F1	 : data[i]["F1"],
									F2	 : data[i]["F2"],
									F3	 : data[i]["F3"],
									F4	 : data[i]["F4"],
									F5	 : data[i]["F5"],
									F6	 : data[i]["F6"],
									F7	 : data[i]["F7"],
									F8	 : data[i]["F8"],
									F9	 : data[i]["F9"],
									F10	 : data[i]["F10"],
									F11	 : data[i]["F11"],
									F12	 : data[i]["F12"],
									F13	 : data[i]["F13"],
									F14	 : data[i]["F14"],
									F15	 : data[i]["F15"],
									F16	 : data[i]["F16"],
									F17	 : data[i]["F17"],
									F18	 : data[i]["F18"]
							};

							that.gridData.push(rowDate);
						}

						that.outRowIndex += 5;

						if($('#'+id).hasClass("datagrid-f")){
							$('#'+id).datagrid('loaded');
						}
						$('#'+id).datagrid('loading');

						if (that.daiBruiCnt < that.outRowIndex) {
							$('#'+id).datagrid('loadData',that.gridData);
						} else {
							// グリッド初期化&ローディング
							$('#'+id).datagrid('load');
						}
					} else {
						for (var i=0; i<length; i++){
							var index = i*10;
							if (id==='grd_subwindow'+that.prefix+that.suffix) {
								$('#'+id).datagrid('mergeCells',{index:index,field:'F2',rowspan:2,colspan:2});
								$('#'+id).datagrid('mergeCells',{index:index,field:'F2',rowspan:2,colspan:2});
								$('#'+id).datagrid('mergeCells',{index:index+2,field:'F2',rowspan:8});
								$('#'+id).datagrid('mergeCells',{index:index+2,field:'F4',rowspan:3});
								$('#'+id).datagrid('mergeCells',{index:index+5,field:'F4',rowspan:3});
								$('#'+id).datagrid('mergeCells',{index:index+8,field:'F4',rowspan:2});
								$('#'+id).datagrid('mergeCells',{index:index,field:'F18',rowspan:8,styler:cstyler1});
							} else {
								$('#'+id).datagrid('mergeCells',{index:index,field:'F1',rowspan:3});
							}
						}

						if($('#'+id).hasClass("datagrid-f")){
							$('#'+id).datagrid('loaded');
						}

						// マスク削除
						$.removeMask();
						$.removeMaskMsg();
					}

					if (that.daiBruiCnt === length && that.initializesCond) {
						var rowDate = {}	// 初期化
						var targetRows= [];
						for (var i=1; i<=length; i++){
							var index = i*10;

							// 形態
							var keitai	= data[i]["F3"];
							var no		= data[i]["F6"];
							var num		= 0;

							// 売上予算
							if (keitai === '0' && no === '1') {
								index += 0;
								num = 0;
								data[num]["F5"] = '売上予算';

							// 売価計
							} else if (keitai === '3' && no === '4') {
								index += 8;
								num = 8;
								data[num]["F5"] = '売価計';

							// 原価計
							} else if (keitai === '3' && no === '5') {
								index += 9;
								num = 9;
								data[num]["F5"] = '原価計';
							} else {
								continue;
							}

							data[num]["F7"] = data[num]["F7"]*1 + data[i]["F7"]*1;
							data[num]["F8"] = data[num]["F8"]*1 + data[i]["F8"]*1;
							data[num]["F9"] = data[num]["F9"]*1 + data[i]["F9"]*1;
							data[num]["F10"] = data[num]["F10"]*1 + data[i]["F10"]*1;
							data[num]["F11"] = data[num]["F11"]*1 + data[i]["F11"]*1;
							data[num]["F12"] = data[num]["F12"]*1 + data[i]["F12"]*1;
							data[num]["F13"] = data[num]["F13"]*1 + data[i]["F13"]*1;
							data[num]["F14"] = data[num]["F14"]*1 + data[i]["F14"]*1;
							data[num]["F15"] = data[num]["F15"]*1 + data[i]["F15"]*1;
							data[num]["F16"] = data[num]["F16"]*1 + data[i]["F16"]*1;
						}

						for (var i = 0; i < 10; i++) {

							if (!(i == 0 || i == 8 || i == 9)) {
								continue;
							}

							var rowDate = {
									F1	 : '部門計',
									F2	 : data[i]["F5"],
									F3	 : data[i]["F7"],
									F4	 : data[i]["F8"],
									F5	 : data[i]["F9"],
									F6	 : data[i]["F10"],
									F7	 : data[i]["F11"],
									F8	 : data[i]["F12"],
									F9	 : data[i]["F13"],
									F10	 : data[i]["F14"],
									F11	 : data[i]["F15"],
									F12	 : data[i]["F16"],
									F13	 : data[i]["F17"]
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

				// 日付の保持
				that.inpDayArr = [];
				for (var i = 1; i <= 10; i++) {
					that.inpDayArr.push(rows[0]['X'+i]);
				}

				// 大分類の件数
				that.daiBruiCnt	= parseInt(rows[0]["CNT"]);
			}
		},
	}
});

})(jQuery);