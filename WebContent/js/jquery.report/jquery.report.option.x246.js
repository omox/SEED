/**
 * jquery report option
 */

;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_Reportx246',			// （必須）レポートオプションの確認
		jsonTemp:	[],							// （必須）検索条件情報_入力チェック前
		jsonString:	[],							// （必須）検索条件情報
		jsonHidden: [],							// （必須）親画面からの引き継ぎ情報
		caption: function(){					// （必須）タイトル
			return $('#reportname').val();
		},
		sortName: '',	// ソート項目名
		sortOrder: '',	// ソート順
		timeData : (new Date()).getTime(),
		initObjNum:	-1,
		initedObject: [],
		initializes : true,
		initialize: function (reportno){	// （必須）初期化
			var that = this;
			// ツールバー初期化
			$($.id.toolbar).panel({
				border: false,
				onCollapse:function(){
					that.setResize();
				},
				onExpand:function(){
					that.setResize();
				},
				collapsible:false
			});

			$.initReportInfo("x246", "提案商品一覧");

			// 引き継ぎ情報
			this.jsonHidden = $.getTargetValue();

			if($.getJSONValue(that.jsonHidden, $.id.send_mode) == '1' && $.getJSONValue(that.jsonHidden, 'reportname') != ''){
				// 初期検索を実施
				$.reg.search = true;
			}

			// データ表示エリア初期化
			that.setGrid($.id.gridholder, reportno);

			// 初期化するオブジェクト数設定
			this.initObjNum = 8;
			// 取引先
			this.setTorihiki(reportno, $.id.SelTorihiki);
			// 提案件名
			this.setTeian(reportno, $.id.SelTeian);
			// 状態
			this.setCombogrid(reportno, $.id.SelStcdTeian);
			// 部門
			this.setBumon(reportno, $.id.SelBumon);
			// 商品名
			$.setTextBox(that, $.id_inp.txt_shnkn, false, '', true);
			// 商品登録日FROM
			$.setDateBox(that, $.id.txt_ymdf, false, 'yyyymmdd', true, true);
			// 商品登録日TO
			$.setDateBox(that, $.id.txt_ymdt, false, 'yyyymmdd', true, true);

			// ボタン押下時処理
			$('#'+$.id.btn_new).on("click", function(){ that.changeReport($.id.btn_new); });
			$('#'+$.id.btn_csv_import).on("click", function(){ that.changeReport($.id.btn_csv_import); });
			$('#'+$.id.btn_next).on("click", function(){ that.pushNext(); });
			$('#'+$.id.btn_back).on("click", function(){ that.changeReport($.id.btn_back); });

			// タブ移動サンプル
			$.changeReportByTabs(that);


			// 初期化終了
			that.initializes = false;

			// remove mask after loaded
			$.removeMask();

			$.reg.search = true;
			$.initialSearch(that);

			// ログ出力
			$.log(that.timeData, 'initialize:');

			that.setResize();
		},

		/**
		 * 削除ボタンイベント
		 * @param {Object} e
		 */
		pushDelete:function(e){
			var that = this;

			if($($.id.hiddenSelectIdx).val() === ''){
				alert('削除する項目を1件以上選択してください。');
				return false;
			}

			// Grid内全情報取得
			var rows = $($.id.gridholder).datagrid('getData').originalRows;

			// 対象情報抜粋
			var targetRows = [];
			var index = $($.id.hiddenSelectIdx).val().split(",");
			for (var i=0; i<rows.length; i++){
				if($.inArray(i+'', index) !== -1){
					//xxxx-xxnn to nn
					rows[i].F2 = rows[i].F2.replace('-', '');
					targetRows.push(rows[i]);
				}
			}

			var szSelTeian	= $.getJSONValue(that.jsonString, $.id.SelTeian);	// 提案件名

			// 確認ポップアップ
			if(confirm('削除しますか？') == false) return false;

			// セッションタイムアウト、利用時間外の確認
			if($.checkIsTimeout()) return false;

			// 更新処理
			$.ajax({
				url: $.reg.jqgrid,
				type: 'POST',
				async: false,
				data: {
					report:			that.name,			// レポート名
					action: 		$.id.action_delete,	// 実行処理情報
					TEIAN:			szSelTeian,			// 提案件名
					DATA:			JSON.stringify(targetRows)
				},
				success: function(data){
					// JSONに変換
					var json = JSON.parse(data);

					// ｾｯｼｮﾝ内Option取得
					if(json.opts!==null){
						if(json.opts.E_MSG !== undefined){
							var msg = "";
							$.each(json.opts.E_MSG, function() {
								msg += this.MSG + "\n";
							});
							alert(msg);
						}else if(json.opts.S_MSG !== undefined){
							alert(json.opts.S_MSG);
							// datagrid更新
							that.success(that.name, false);
						}
					}
				}
			});
		},

		/**
		 * 確定ボタンイベント
		 */
		pushNext:function(){
			var that = this;

			if($($.id.hiddenSelectIdx).val() === ''){
				alert('更新する項目を1件以上選択してください。');
				return false;
			}

			// Grid内全情報取得
			var rows = $($.id.gridholder).datagrid('getData').originalRows;

			// 対象情報抜粋
			var targetRows = [];
			var index = $($.id.hiddenSelectIdx).val().split(",");
			for (var i=0; i<rows.length; i++){
				if($.inArray(i+'', index) !== -1){
					//xxxx-xxnn to nn
					rows[i].F2 = rows[i].F2.replace('-', '');
					targetRows.push(rows[i]);
				}
			}

			var szSelTeian	= $.getJSONValue(that.jsonString, $.id.SelTeian);	// 取引先

			// 確認ポップアップ
			if(confirm('状態を更新しますか？') == false) return false;

			// セッションタイムアウト、利用時間外の確認
			if($.checkIsTimeout()) return false;

			// 更新処理
			$.ajax({
				url: $.reg.jqgrid,
				type: 'POST',
				async: false,
				data: {
					report:			that.name,			// レポート名
					action: 		$.id.action_update,	// 実行処理情報
					MODE:			"3",				// モード (「作成中」→「確定」)
					TEIAN:			szSelTeian,			// 提案件名
					DATA:			JSON.stringify(targetRows)
				},
				success: function(data){
					// JSONに変換
					var json = JSON.parse(data);

					// ｾｯｼｮﾝ内Option取得
					if(json.opts!==null){
						if(json.opts.E_MSG !== undefined){
							var msg = "";
							$.each(json.opts.E_MSG, function() {
								msg += this.MSG + "\n";
							});
							alert(msg);
						}else if(json.opts.S_MSG !== undefined){
							alert(json.opts.S_MSG);
							// datagrid更新
							that.success(that.name, false);
						}
					}
				}
			});
		},

		validation: function (){	// （必須）批准
			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform).form('validate');

			// 入力エラーなしの場合に検索条件を格納
			if (rt == true) that.jsonString = that.jsonTemp.slice(0);
			// 入力チェック用の配列をクリア
			that.jsonTemp = [];
			return rt;
		},
		success: function(reportno, sortable){	// （必須）正処理
			if (sortable) sortable=1; else sortable=0;
			var that = this;
			// 検索実行
			var szSelTorihiki		= $.getJSONObject(this.jsonString, $.id.SelTorihiki).value;		// 取引先
			var szSelTeian			= $.getJSONObject(this.jsonString, $.id.SelTeian).value;		// 提案件名
			var szSelStcdTeian		= $.getJSONObject(this.jsonString, $.id.SelStcdTeian).value;	// 状態
			var szSelBumon			= $.getJSONObject(this.jsonString, $.id.SelBumon).value;		// 部門
			// if auto search run before Bumon loaded
			if(szSelBumon == null || szSelBumon === "") {
				szSelBumon = -1;
			}
			var szShohin			= $.getJSONObject(this.jsonString, $.id_inp.txt_shnkn).value;		// 商品名
			var szTxtYmdF			= $.getJSONObject(this.jsonString, $.id.txt_ymdf).value;			// 商品登録日FROM
			var szTxtYmdT			= $.getJSONObject(this.jsonString, $.id.txt_ymdt).value;			// 商品登録日TO
			var szLimit				= 10000;	// 検索上限数
			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			var dg = $($.id.gridholder);
			dg.datagrid('loading');

			// grid.options 取得
			var options = dg.datagrid('options');
			that.sortName	= options.sortName;
			that.sortOrder	= options.sortOrder;

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,			// レポート名
					TORIHIKI:		szSelTorihiki,		// 取引先
					TEIAN:			szSelTeian,			// 提案件名
					STATE:			szSelStcdTeian,		// 状態
					BUMON:			szSelBumon,			// 部門
					SHNKN:			szShohin,		// 商品名
					YMD_F:			szTxtYmdF,			// 商品登録日FROM
					YMD_T:			szTxtYmdT,			// 商品登録日TO
					t:				(new Date()).getTime(),
					sortable:		sortable,
					sortName:		that.sortName,
					sortOrder:		that.sortOrder,
					rows:			szLimit				// 表示可能レコード数
				},
				function(data){
					// 検索処理エラー判定
					if($.searchError(data)) return false;

					// ログ出力
					$.log(that.timeData, 'query:');

					// JSONに変換
					var json = JSON.parse(data);

					if (sortable===0){
						// 初期検索時に並び替え情報のリセット
						options.sortName = null;
						options.sortOrder = null;
					}

					// 初期化
					$($.id.hiddenSelectIdx).val('');

					// 結果表示
					dg.datagrid('loadData', json.rows);
					dg.datagrid('loaded');
					dg.datagrid('getPager').pagination('select', 1);

					// 検索ボタン無効化
					$.setButtonState('#'+$.id.btn_search, false, 'success');

					// ログ出力
					$.log(that.timeData, 'loaded:');

					// 検索上限数に達した場合にポップアップ表示
					if(json.rows.length >= szLimit){
						alert('検索結果が上限数 '+szLimit+' 件に達しました。');
					}
				}
			);
		},
		getEasyUI: function(){	// （必須）情報の取得
			// 初期化
			this.jsonTemp = [];

			// レポート名
			this.jsonTemp.push({
				id:		"reportname",
				value:	this.caption(),
				text:	this.caption()
			});
			// 取引先
			this.jsonTemp.push({
				id:		$.id.SelTorihiki,
				value:	$('#'+$.id.SelTorihiki).combogrid('getValue'),
				text:	$('#'+$.id.SelTorihiki).combogrid('getText')
			});
			// 提案件名
			this.jsonTemp.push({
				id:		$.id.SelTeian,
				value:	$('#'+$.id.SelTeian).combogrid('getValue'),
				text:	$('#'+$.id.SelTeian).combogrid('getText')
			});
			// 状態
			this.jsonTemp.push({
				id:		$.id.SelStcdTeian,
				value:	$('#'+$.id.SelStcdTeian).combogrid('getValue'),
				text:	$('#'+$.id.SelStcdTeian).combogrid('getText')
			});
			// 部門
			this.jsonTemp.push({
				id:		$.id.SelBumon,
				value:	$('#'+$.id.SelBumon).combogrid('getValue'),
				text:	$('#'+$.id.SelBumon).combogrid('getText')
			});
			// 商品名
			this.jsonTemp.push({
				id:		$.id_inp.txt_shnkn,
				value:	$('#'+$.id_inp.txt_shnkn).textbox('getText'),
				text:	''
			});
			// 商品登録日FROM
			this.jsonTemp.push({
				id:		$.id.txt_ymdf,
				value:	$('#'+$.id.txt_ymdf).datebox('getValue'),
				text:	$('#'+$.id.txt_ymdf).datebox('getText')
			});
			// 商品登録日TO
			this.jsonTemp.push({
				id:		$.id.txt_ymdt,
				value:	$('#'+$.id.txt_ymdt).datebox('getValue'),
				text:	$('#'+$.id.txt_ymdt).datebox('getText')
			});
		},

		setTorihiki: function(reportno, id){		// 取引先
			var that = this;
			var idx = -1;
			$('#'+id).combogrid({
				panelWidth:250,
				panelHeight:400,
				url:$.reg.easy,
				required: true,
				editable: false,
				showHeader: false,
				idField:'VALUE',
				textField:'TEXT',
				columns:[[
					{field:'TEXT',	title:'',	width:250}
				]],
				fitColumns: true,
				onShowPanel:function(){
					$.setScrollGrid(this);
				},
				onBeforeLoad:function(param){
					idx = -1;
					// 情報設定
					var json = [{
						DUMMY: 'DUMMY'
					}];

					param.page		=	reportno;
					param.obj		=	id;
					param.sel		=	(new Date()).getTime();
					param.target	=	id;
					param.action	=	$.id.action_init;
					param.json		=	JSON.stringify(json);
				},
				onLoadSuccess:function(data){
					// 初期化
					var num = 0;
					if ($.inArray(id, that.initedObject) < 0){
						that.initedObject.push(id);
						var val = $('#'+$.id.hiddenTorihiki).val();
						for (var i=0; i<data.rows.length; i++){
							if (data.rows[i].VALUE == val){
								num = i;
								break;
							}
						}
					}
					if (data.rows.length > 0){
						$('#'+id).combogrid('grid').datagrid('selectRow', num);
					} else {
						$.showMessageIn("E", "取引先コードが設定されていません。", undefined, undefined, undefined, undefined, "E");
					}
					idx = 1;
					// ログ出力
					$.log(that.timeData, id+' init:');
					// 検索ボタン有効化
					$.setButtonState('#'+$.id.btn_search, true, id);
					// 初期表示検索処理
					$.reg.search = true;
					$.initialSearch(that);
					$.tryClickSearch();
				},
				onChange:function(newValue, oldValue){
					if(idx > 0){
						// 検索ボタン有効化
						$.setButtonState('#'+$.id.btn_search, true, id);
					}
				}
			});
		},
		setTeian: function(reportno, id){		// 提案件名
			var that = this;
			var idx = -1;
			$('#'+id).combogrid({
				panelWidth:250,
				panelHeight:75,
				url:$.reg.easy,
				required: true,
				editable: false,
				showHeader: false,
				idField:'VALUE',
				textField:'TEXT',
				columns:[[
					{field:'TEXT',	title:'',	width:400}
				]],
				fitColumns: true,
				onShowPanel:function(){
					$.setScrollGrid(this);
				},
				onBeforeLoad:function(param){
					idx = -1;
					// 情報設定
					var json = [{
						TEIAN: $('#'+$.id.hiddenNoTeian).val()
					}];

					param.page		=	reportno;
					param.obj		=	id;
					param.sel		=	(new Date()).getTime();
					param.target	=	id;
					param.action	=	$.id.action_init;
					param.json		=	JSON.stringify(json);
				},
				onLoadSuccess:function(data){
					// 初期化
					var num = 0;
					if ($.inArray(id, that.initedObject) < 0){
						that.initedObject.push(id);
						var val = $('#'+$.id.hiddenNoTeian).val();
						for (var i=0; i<data.rows.length; i++){
							if (data.rows[i].VALUE == val){
								num = i;
								break;
							}
						}
					}
					if (data.rows.length > 0){
						$('#'+id).combogrid('grid').datagrid('selectRow', num);
					}
					idx = 1;
					// ログ出力
					$.log(that.timeData, id+' init:');
					// 検索ボタン有効化
					$.setButtonState('#'+$.id.btn_search, true, id);
					// 初期表示検索処理
					$.initialSearch(that);
					$.reg.search = true;
					$.tryClickSearch();
				},
				onChange:function(newValue, oldValue){
					if(idx > 0){
						// 検索ボタン有効化
						$.setButtonState('#'+$.id.btn_search, true, id);
					}
				}
			});
		},
		setCombogrid: function(reportno, id){
			var that = this;
			var idx = -1;
			$('#'+id).combogrid({
				panelWidth:120,
				panelHeight:null,
				panelMaxHeight:200,
				required: false,
				editable: false,
				showHeader: false,
				idField:'VALUE',
				textField:'TEXT',
				columns:[[
					{field:'TEXT',	title:'',	width:120}
				]],
				fitColumns: true,
				onShowPanel:function(){
					$.setScrollGrid(this);
				},
				onBeforeLoad:function(param){
					idx = -1;
					// 情報設定
					var json = [{
						DUMMY: 'DUMMY'
					}];

					param.page		=	reportno;
					param.obj		=	id;
					param.sel		=	(new Date()).getTime();
					param.target	=	id;
					param.action	=	$.id.action_init;
					param.json		=	JSON.stringify(json);
				},
				onChange:function(newValue, oldValue){
					// 検索ボタン有効化
					$.setButtonState('#'+$.id.btn_search, true, id);
				}
			});
			$('#'+id).combogrid('grid').datagrid('loadData', $.SEL_DATA.KENMEI_STATE_DATA);
		},
		setBumon: function(reportno, id){		// 部門
			var that = this;
			var idx = -1;
			$('#'+id).combogrid({
				panelWidth:250,
				url:$.reg.easy,
				required: false,
				editable: false,
				showHeader: false,
				idField:'VALUE',
				textField:'TEXT',
				columns:[[
					{field:'TEXT',	title:'',	width:250}
				]],
				fitColumns: true,
				onShowPanel:function(){
					$.setScrollGrid(this);
				},
				onBeforeLoad:function(param){
					idx = -1;
					// 情報設定
					var json = [{
						DUMMY: 'DUMMY'
					}];

					param.page		=	reportno;
					param.obj		=	id;
					param.sel		=	(new Date()).getTime();
					param.target	=	id;
					param.action	=	$.id.action_init;
					param.json		=	JSON.stringify(json);
				},
				onLoadSuccess:function(data){
					// 初期化
					var num = 0;
					if (data.rows.length > 0){
						$('#'+id).combogrid('grid').datagrid('selectRow', num);
					}
					idx = 1;
					// ログ出力
					$.log(that.timeData, id+' init:');
					// 検索ボタン有効化
					$.setButtonState('#'+$.id.btn_search, true, id);
					// 初期表示検索処理
					$.initialSearch(that);
				},
				onChange:function(newValue, oldValue){
					if(idx > 0){
						// 検索ボタン有効化
						$.setButtonState('#'+$.id.btn_search, true, id);
					}
				}
			});
		},

		setGrid: function (id, reportNumber){	// グリッドの構築
			var that = this;
			// ページサイズ定義取得
			var pageSize = 10;
			$(id).datagrid({
				nowrap: true,
				border: true,
				striped: true,
				collapsible:false,
				remoteSort: true,
				sortName: "SORT",
				autoRowHeight:false,
				singleSelect:true,
				pagination:true,
				pagePosition:'bottom',
				pageSize:pageSize,
				pageList:[pageSize],
				frozenColumns:[[
					{field:'ck',	title:'', width:40,	align:'center',	halign:'center', resizable:false,
						formatter:function(value, rowData, rowIndex) {
							if (rowData['F1'] == '作成中') {
								return '<input id="ck_'+rowIndex+'" type="checkbox" style="opacity: 1;">';
							} else {
								return '';
							}
//							if (rowData['F1'] == 1) {
//								return '<input id="ck_'+rowIndex+'" type="checkbox" style="opacity: 1;">';
//							} else {
//								return '';
//							}

						}
					},
				]],
				columns:[[
					{field:'F1', title:'状態', width:60, align:'center',	halign:'center', resizable:false,
						formatter:function(value, rowData, rowIndex) {
							return value;
//							if(rowData['F1'] == 1){
//								return '作成中';
//							} else if (rowData['F1'] == 2){
//								return '確定';
//							} else if (rowData['F1'] == 3){
//								return '仕掛';
//							} else if (rowData['F1'] == 4){
//								return '完了';
//							} else if (rowData['F1'] == 9){
//								return '却下';
//							}
						}
					},
					{field:'F2', title:'商品コード', width: 80,halign:'center',align:'left',
						formatter:function(value, rowData, rowIndex) {
							return '<a id="F2_'+rowIndex+'" href="#">'+value+'</a>';
						}
					},
					{field:'F3',	title:'ソースコード1',		width:120,halign:'center',align:'left'},
					//{field:'F4',	title:'販売コード',			width: 70,halign:'center',align:'left'},
					{field:'F4',	title:'商品名',				width:300,halign:'center',align:'left'},
					{field:'F5',	title:'扱<br>区分',			width: 40,halign:'center',align:'left'},
					{field:'F6',	title:'原価',				width: 70,halign:'center',align:'right',
						formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0.00');}},
					{field:'F7',	title:'本体売価',			width: 70,halign:'center',align:'right',
						formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0');}},
					{field:'F8',	title:'総額売価',			width: 70,halign:'center',align:'right',
						formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0');}},
					{field:'F9',	title:'店入数',				width: 70,halign:'center',align:'right',
						formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0');}},
					{field:'F10',	title:'親コード',			width: 80,halign:'center',align:'left'},
					{field:'F11',	title:'ワッペン区分',		width: 60,halign:'center',align:'left'},
					{field:'F12',	title:'一括区分',			width: 40,halign:'center',align:'left'},
					{field:'F13',	title:'標準仕入先',			width: 70,halign:'center',align:'left'},
					{field:'F14',	title:'分類コード',			width:100,halign:'center',align:'left'},
					{field:'F15',	title:'更新日',				width: 70,halign:'center',align:'center'},
					{field:'F16',	title:'衣料使い回し',		width: 50,halign:'center',align:'center'},
				]],
				fitColumns:false,	// 指定カラム幅を適用する場合、false 指定。
				onSortColumn:function(sort, order){
					if (that.jsonString.length===0) return false;
					// カラム並び替え
					that.success(that.name, true);
				},
				onLoadSuccess:function(data){
					// (暫定)オブジェクトのコントロール
					var panel = $(id).datagrid('getPanel');

					// View内の入出力項目を調整
					var inputs = panel.find('.datagrid-row :input');

					// 次画面
					var link = panel.find('.datagrid-row a').filter('[id^=F2_]');
					link.click(function(){
						var index = $(this).attr('id').split('_')[1];
						var targetRow = $($.id.gridholder).datagrid('getRows')[index];
						if (targetRow['F1'] == '作成中') {
							that.changeReport($.id.btn_sel_change, targetRow);
						} else {
							that.changeReport($.id.btn_sel_refer, targetRow);
						}

					});

					// 選択チェックボックス
					var input = inputs.filter('[id^=ck_]');
					input.change(function(){
						var index = $(this).attr('id').split('_')[1];
						if ($(this).is(':checked')) {
							$.onCheck_grid(index);
						} else {
							$.onUnCheck_grid(index);
						}
					});

					inputs.filter('.validatebox-text').on('focus', function() { ctrlFocus(this); });

					// 選択制御
					$.onInitAllSelect();

					// 状態保存
					$.saveState(reportNumber, that.getJSONString(), this);
				},
				loadFilter:function(data){
					if (typeof data.length == 'number' && typeof data.splice == 'function'){	// is array
						data = {
							total: data.length,
							rows: data
						};
					}
					var dg = $(this);
					var opts = dg.datagrid('options');
					$.createPagenation(that, dg, opts, data);
					if (!data.originalRows){
						data.originalRows = (data.rows);
					}
					var start = (opts.pageNumber-1)*parseInt(opts.pageSize);
					var end = start + parseInt(opts.pageSize);
					data.rows = (data.originalRows.slice(start, end));
					return data;
				}
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
		getRecord: function(){		// （必須）レコード件数を戻す
			var data = $($.id.gridholder).datagrid('getData');
			if (data == null) {
				return 0;
			} else {
				return data.total;
			}
		},
		setResize: function(){		// （必須）リサイズ
			var changeHeight = $(window).height();
			if (0 < changeHeight) {

				// window 幅取得
				var changeWidth  = $(window).width();

				var dg = $($.id.gridholder);
				var opts = dg.datagrid('options');

				// toolbar の調整
				$($.id.toolbar).panel('resize',{width:changeWidth});

				// DataGridの高さ
				var gridholderHeight = 0;
				var placeholderHeight = 0;

				if (opts !== 'undefined') {
					// toolbar + bottom buttons
					placeholderHeight = $($.id.toolbar).panel('panel').height() + $($.id.buttons).height();
					// datagrid の格納された panel の高さ
					gridholderHeight = $(window).height() - placeholderHeight;

					$.createPagenation(this, dg, opts, dg.datagrid('getData'));
				}

				$($.id.gridholder).datagrid('resize', {
					width:	changeWidth,
					height:	gridholderHeight
				});
			}
		},
		getJSONString : function(){		// （必須）JSON形式の文字列
			return this.jsonString;
		},
		tryLoadMethods: function(id){	// （オプション）combo.onChange Event
			var that = this;
			// セッションタイムアウト確認
			if ($.checkIsTimeout(that)) return false;
			try {
				$(id).combogrid('clear');
				var grid = $(id).combogrid('grid');
				grid.datagrid('load');
			} catch (e) {
				// combgrid 未更新時のERROR回避
			}
		},
		changeReport:function(btnId,targetRow){	// 画面遷移
			var that = this;

			// タブ要素(a)取得
			var elems = $('#tabContent', window.parent.document).map(
				function(i,e) {
					return e;
				}).get();
			var href = elems[0].value.split(',');

			// JSON Object Clone ()
			var sendJSON = [];
			$.setJSONObject(sendJSON, $.id.send_mode,	1,	1);						// 遷移
			$.setJSONObject(sendJSON, 'searchInput',	that.jsonString,	'');	// 検索条件
			$.setJSONObject(sendJSON, 'callpage', that.name, that.name);			// 呼出し元レポート情報

			// 遷移判定
			var index = 0;
			var childurl = "";

			var teian = $('#'+$.id.SelTeian).combogrid('getValue');
			var torihiki = $('#'+$.id.SelTorihiki).combogrid('getValue');
			$.setJSONObject(sendJSON, 'sendBtnid', btnId, btnId);
			$.setJSONObject(sendJSON, $.id.hiddenNoTeian, teian, teian);	// 件名No
			$.setJSONObject(sendJSON, $.id.hiddenTorihiki, torihiki, torihiki);	// 取引先
			$.setJSONObject(sendJSON, $.id.hiddenStcdTeian, 1, 1);	//  提案状態、デフォルトは作成中

			// 選択行
			var row = $.isEmptyVal(targetRow) ? $($.id.gridholder).datagrid("getSelected") : targetRow;

			// 実行ボタン別処理
			switch (btnId) {
				case $.id.btn_new:
					// 転送先情報
					index = 3;
					childurl = href[index];
					// オブジェクト作成
					$.setJSONObject(sendJSON, $.id.txt_sel_shncd,'', '');
					$.setJSONObject(sendJSON, $.id.txt_sel_shnkn,'', '');
					$.setJSONObject(sendJSON, $.id_inp.txt_shncd, '', '');
					$.setJSONObject(sendJSON, $.id.hiddenTorihiki, torihiki, torihiki);	// 取引先
					break;
				case $.id.btn_sel_refer:
				case $.id.btn_sel_change:
					if(!row){
						$.showMessage('E00008');
						return false;
					}

					// 転送先情報
					index = 3;
					childurl = href[index];

					// オブジェクト作成
					//xxxx-xxnn to nn
					var target_shncd = row.F2.replace('-', '');
					$.setJSONObject(sendJSON, $.id.txt_sel_shncd, target_shncd, target_shncd);
					$.setJSONObject(sendJSON, $.id.txt_sel_shnkn, row.F4, row.F4);
					$.setJSONObject(sendJSON, $.id_inp.txt_shncd, target_shncd, target_shncd);
					var stcdTeian = 0;
					if(row.F1 == '作成中'){
						stcdTeian = 1;
					} else if (row.F1 == '確定'){
						stcdTeian = 2;
					} else if (row.F1 == '仕掛'){
						stcdTeian = 3;
					} else if (row.F1 == '完了'){
						stcdTeian = 4;
					} else if (row.F1 == '却下'){
						stcdTeian = 9;
					}
					$.setJSONObject(sendJSON, $.id.hiddenStcdTeian, stcdTeian, stcdTeian);	//  提案状態
					break;
				case $.id.btn_csv_import:
					// 転送先情報
					index = 4;
					childurl = href[index];
					break;
				case $.id.btn_sel_csverr:
					// 転送先情報
					index = 6;
					childurl = href[index];
					break;
				default: // include case id.btn_back
					index = 1;
					childurl = href[index];
					break;
			}

			$.SendForm({
				type: 'post',
				url: childurl,
				data: {
					sendMode:	1,
					sendParam:	JSON.stringify( sendJSON )
				}
			});
		},
		excel: function(reportno){	// (必須)Excel出力
			// グリッドの情報取得
			var options = $($.id.gridholder).datagrid('options');

			// タイトル部
			var title = [];
			var fColumns = options.frozenColumns[0].slice(1);	// checkbox列以外を取得
			title = $.outputExcelTitle(title, [ fColumns ]);
			title = $.outputExcelTitle(title, options.columns);
			var kbn = fColumns.length;
			var data = {
				'header': JSON.stringify(title),
				'report': reportno,
				'kbn'	: kbn
			};

			// 転送
			$.ajax({
				url: $.reg.excel,
				type: 'POST',
				data: data,
				async: false,
				success: function(){
					// Excel出力
					$.outputExcel(reportno, kbn);
				},
				error: function(){
					// Excel出力エラー
					$.outputExcelError();
				}
			});
		}
	} });
})(jQuery);