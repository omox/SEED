/**
 * jquery report option
 */

;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_Reportx245',			// （必須）レポートオプションの確認
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

			$.initReportInfo("x245", "提案件名一覧");

			// 引き継ぎ情報
			this.jsonHidden = $.getTargetValue();

			// データ表示エリア初期化
			that.setGrid($.id.gridholder, reportno);

			// 初期化するオブジェクト数設定
			this.initObjNum = 3;
			// 取引先
			this.setTorihiki(reportno, $.id.SelTorihiki);
			// 件名No
			$.setTextBox(that, $.id.txt_teian_no, false, 'intMaxLen[9]', true);
			// 提案件名 (初期化オブジェクト数に含まない)
			$.setTextBox(that, $.id.txt_teian, false, 'maxByte[30, 60]', true);
			// 状態
			this.setCombogrid(reportno, $.id.SelStcdKenmei);

			// ボタン押下時処理
			$('#'+$.id.btn_prev).on("click", function(){ that.pushPrev(); });
			$('#'+$.id.btn_next).on("click", function(){ that.pushNext(); });

			// タブ移動サンプル
			$.changeReportByTabs(that);

			// 初期化終了
			that.initializes = false;

			// remove mask after loaded
			$.removeMask();

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
					targetRows.push(rows[i]);
				}
			}

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
		 * 作成中ボタンイベント
		 */
		pushPrev:function(){
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
					targetRows.push(rows[i]);
				}
			}

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
					MODE:			"2",				// モード (「作成中」に戻す)
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
					targetRows.push(rows[i]);
				}
			}

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
		getLen:function(str){
			var result = 0;
			for(var i=0;i<str.length;i++){
				var chr = str.charCodeAt(i);
				if((chr >= 0x00 && chr < 0x81) || (chr === 0xf8f0) || (chr >= 0xff61 && chr < 0xffa0) || (chr >= 0xf8f1 && chr < 0xf8f4)){
					//半角文字の場合は1を加算
					result += 1;
				}else{
					//それ以外の文字の場合は2を加算
					result += 2;
				}
			}
			//結果を返す
			return result;
		},
		/**
		 * 登録ボタンイベント
		 * @param {Object} e
		 */
		pushEntry:function(e){
			var that = this;
			var szSelTorihiki	= $('#'+$.id.SelTorihiki).combogrid('getValue');		// 取引先
			var szTxtTeian		= $('#'+$.id.txt_teian).textbox('getText');				// 提案件名

			// 入力値をTrim
			szTxtTeian = $.trim2(szTxtTeian);
			$('#'+$.id.txt_teian).textbox('setText', szTxtTeian);

			if(szTxtTeian.length < 1){
				alert('提案件名は必須入力です。');
				return false;
			}

			if(that.getLen(szTxtTeian) > 60) {
				alert('全角30文字、半角60文字まででで入力してください。');
				return false;
			}

			if(parseInt(szSelTorihiki) < 1){
				alert('取引先は必須入力です。');
				return false;
			}

			// フォーム情報取得
			that.getEasyUI();

			// EasyUI のフォームメソッド 'validate' 実施
			if(!that.validation()) return false;

			//提案件名との検索条件を消す
			that.jsonString[3] = {
				id:		$.id.txt_teian,
				value:	'',
				text:	''
			};

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
					MODE:			"1",				// モード (新規登録)
					TORIHIKI:		szSelTorihiki,		// 取引先
					TEIAN:			szTxtTeian			// 提案件名
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
							// 入力値クリア
							$('#'+$.id.txt_teian).textbox('setText', '');
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
			var szTxtTeianNo		= $.getJSONObject(this.jsonString, $.id.txt_teian_no).value;	// 件名No
			var szTxtTeian			= $.getJSONObject(this.jsonString, $.id.txt_teian).value;		// 提案件名
			var szSelStcdKenmei		= $.getJSONObject(this.jsonString, $.id.SelStcdKenmei).value;	// 状態
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
					TEIANNO:		szTxtTeianNo,		// 件名No
					TEIAN:			szTxtTeian,			// 提案件名
					STATE:			szSelStcdKenmei,	// 状態
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
					$($.id.hiddenChangedIdx).val('');

					// 結果表示
					dg.datagrid('loadData', json.rows);
					dg.datagrid('loaded');
					dg.datagrid('getPager').pagination('select', 1);

					// 検索ボタン無効化
					$.setButtonState('#'+$.id.btn_search, false, 'success');

					// remove mask after loaded
					//$.removeMask();

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
			// 件名No
			this.jsonTemp.push({
				id:		$.id.txt_teian_no,
				value:	$('#'+$.id.txt_teian_no).textbox('getText'),
				text:	''
			});
			// 提案件名
			this.jsonTemp.push({
				id:		$.id.txt_teian,
				value:	$('#'+$.id.txt_teian).textbox('getText'),
				text:	''
			});
			// 状態
			this.jsonTemp.push({
				id:		$.id.SelStcdKenmei,
				value:	$('#'+$.id.SelStcdKenmei).combogrid('getValue'),
				text:	$('#'+$.id.SelStcdKenmei).combogrid('getText')
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
					}
					if (data.rows.length > 0){
						$('#'+id).combogrid('grid').datagrid('selectRow', num);
					}
					else {
						$.showMessageIn("E", "取引先コードが設定されていません。", undefined, undefined, undefined, undefined, "E");
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
				onChange:function(newValue, oldValue){
					// 検索ボタン有効化
					$.setButtonState('#'+$.id.btn_search, true, id);
				},
			});
			$('#'+id).combogrid('grid').datagrid('loadData', $.SEL_DATA.KENMEI_STATE_DATA);
		},

		setGrid: function (id, reportNumber){	// グリッドの構築
			var that = this;
			// ページサイズ定義取得
			var pageList = $.fn.pagination.defaults.pageList;
			var pageSize = $.getJSONValue(this.jsonHidden, $.id.pageSize);
			if (pageSize==="") pageSize=$.fn.pagination.defaults.pageSize;
			pageSize = $.getDefaultPageSize(pageSize, pageList);

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
				pageList:pageList,
				frozenColumns:[[
					{field:'ck',	title:'',			width:40,	align:'center',	halign:'center',	resizable:false,
						formatter:function(value, rowData, rowIndex) {
							if(rowData['F1'] == 1 || rowData['F1'] == 2) {
								return '<input id="ck_'+rowIndex+'" type="checkbox" style="opacity: 1;">';
							} else {
								return '';
							}
						}
					},
					{field:'F1',	title:'状態',		width:60,	align:'center',	halign:'center',	resizable:false,
						formatter:function(value, rowData, rowIndex) {
							if(rowData['F1'] == 1){
								return '作成中';
							} else if (rowData['F1'] == 2){
								return '確定';
							} else if (rowData['F1'] == 3){
								return '仕掛';
							} else if (rowData['F1'] == 4){
								return '完了';
							}
						}
					},
					{field:'F2',	title:'次画面',		width:60,	align:'center',	halign:'center',	resizable:false,
						formatter:function(value, rowData, rowIndex) {
							if(rowData['F6'] == 0){
								return '<a id="F2_'+rowIndex+'" href="#">詳細へ</a>';
							} else {
								return '<a id="F2_'+rowIndex+'" href="#">一覧へ</a>';
							}
						}
					},
					{field:'F3',	title:'件名No',		width:60,	align:'left',	halign:'center',	resizable:false}
				]],
				columns:[[
					{field:'F4',	title:'取引先',		width:60,	align:'left',	halign:'center',	resizable:false},
					{field:'F5',	title:'提案件名',	width:180,	align:'left',	halign:'center',	resizable:false},
					{field:'F6',	title:'提案数',		width:65,	align:'right',	halign:'center',	resizable:false,
						formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0');}},
					{field:'F7',	title:'完了数',		width:65,	align:'right',	halign:'center',	resizable:false,
						formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0');}},
					{field:'F8',	title:'却下数',		width:65,	align:'right',	halign:'center',	resizable:false,
						formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0');}},
					{field:'F9',	title:'仕掛数',		width:65,	align:'right',	halign:'center',	resizable:false,
						formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0');}},
					{field:'F10',	title:'確定数',		width:65,	align:'right',	halign:'center',	resizable:false,
						formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0');}},
					{field:'F11',	title:'作成数',		width:65,	align:'right',	halign:'center',	resizable:false,
						formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0');}},
					{field:'F12',	title:'完了日',		width:80,	align:'left',	halign:'center',	resizable:false},
					{field:'F13',	title:'作成者',		width:180,	align:'left',	halign:'center',	resizable:false}
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
						that.changeReport(targetRow);
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
					// tb
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
		changeReport:function(rowData){	// 画面遷移

			var that = this;

			// JSON Object Clone ()
			var sendJSON = [];
			$.setJSONObject(sendJSON, $.id.send_mode,	1,	1);							// 遷移
			$.setJSONObject(sendJSON, $.id.hiddenNoTeian,	rowData['F3'],	rowData['F3']);	// 件名No
			$.setJSONObject(sendJSON, $.id.hiddenTorihiki,	rowData['F4'],	rowData['F4']);	// 取引先
			$.setJSONObject(sendJSON, 'callpage', that.name, that.name);								// 呼出し元レポート情報
			$.setJSONObject(sendJSON, $.id.hiddenStcdTeian, 1, 1);				// 提案状態、デフォルトは作成中

			// タブ要素(a)取得
			var elems = $('#tabContent', window.parent.document).map(
				function(i,e) {
					return e;
				}).get();
			var href = elems[0].value.split(',');

			// 転送先情報
			var index = 2;
			if (rowData['F6'] == 0) {
				//新規
				index = 3;
				$.setJSONObject(sendJSON, 'sendBtnid', $.id.btn_new, $.id.btn_new);
				// オブジェクト作成
				$.setJSONObject(sendJSON, $.id.txt_sel_shncd,'', '');
				$.setJSONObject(sendJSON, $.id.txt_sel_shnkn,'', '');
				$.setJSONObject(sendJSON, $.id_inp.txt_shncd, '', '');
			}
			//var index = rowData['F6'] == 0 ? 3 : 2;
			var childurl = href[index];

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

			var data = {
				'header': JSON.stringify(title)
			};

			var kbn = fColumns.length;

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