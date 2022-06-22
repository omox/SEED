/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_Reportx250',			// （必須）レポートオプションの確認
		jsonTemp:	[],							// （必須）検索条件情報_入力チェック前
		jsonString:	[],							// （必須）検索条件情報
		jsonStringCsv:	[],						// （CSV出力用）検索条件情報
		jsonHidden: [],							// （必須）親画面からの引き継ぎ情報
		jsonInit: [],							// （必須）検索条件初期情報

		caption: function(){					// （必須）タイトル
			return $('#reportname').val();
		},
		sortName: '',	// ソート項目名
		sortOrder: '',	// ソート順
		timeData : (new Date()).getTime(),

		dedefaultObjNum:	5,	// 初期化オブジェクト数

		initObjNum:	-1,
		initedObject: [],
		maxMergeCell: 1,
		onChangeFlag : false,
		onChangeFlag2 : false,
		columnName:'',	// OnClickRowの列名
		queried : false,

		initializes : true,

		onChangeReport: false,
		reportYobiInfo: function(){			// （必須）レポートメニューに登録された予備情報＝帳票をどういった表示にしたいかの情報
			return $('#reportYobi1').val();
		},
		sendBtnid: "",						// 呼出ボタンID情報
		pushBtnid: "",						// 実行ボタンID情報(検索系で利用)
		focusRootId:"cc",					// （キー移動時必須）キー移動イベントのルートとなるパネルのID
		focusParentId:"",					// （キー移動時必須）現在フォーカスがあたっている項目の親となるパネルのID
		focusGridId:"",						// （キー移動時必須）現在フォーカスがあたっているDataGridのID
		oldBmn:"",
		oldStcdTeian:"",

		initialize: function (reportno){	// （必須）初期化
			var that = this;

			// 引き継ぎ情報
			this.jsonHidden = $.getTargetValue();
			// 画面の初回基本設定
			this.setInitObjectState();
			// 初期検索条件設定
			this.jsonInit = $.getInitValue();
			// データ表示エリア初期化
			that.setGrid($.id.gridholder, reportno);
			// 初期化するオブジェクト数設定
			this.initObjNum = this.dedefaultObjNum;

			var isUpdateReport = false;

			// 入力テキストボックス系
			var inputbox = Object.getOwnPropertyNames($.id_inp);
			for ( var sel in inputbox ) {
				if($('#'+$.id_inp[inputbox[sel]]).length > 0){
					$.setInputbox(that, reportno, $.id_inp[inputbox[sel]], isUpdateReport);
				}
			}

			// 部門
			this.setBumon(reportno, $.id.SelBumon);
			// 発注先
			$.setTextBox(that, 'txt_hatyu', false, '', true);
			// 商品名
			$.setTextBox(that, 'txt_shohin', false, '', true);
			// 商品登録日FROM
			$.setDateBox(that, $.id.txt_ymdf, false, 'yyyymmdd', true, true);
			// 商品登録日TO
			$.setDateBox(that, $.id.txt_ymdt, false, 'yyyymmdd', true, true);

			// ボタン押下時処理
			$('#'+$.id.btn_shikakari).on("click", function(){ that.pushShikakari(); });
			$('#'+$.id.btn_prev).on("click", function(){ that.pushPrev(); });
			$('#'+$.id.btn_next).on("click", function(){ that.pushNext(); });
			$('#'+$.id.btn_download).on("click", function(){ that.pushDownload();});

			// 検索ボタン無効化
			$.setButtonState('#'+$.id.btn_search, false, 'success');

			// タブ移動サンプル
			$.changeReportByTabs(that);

			// 初期化終了
			this.initializes =! this.initializes;

			// チェックボックスの設定
			$.initCheckboxCss($("#"+that.focusRootId));
			// キーイベントの設定
			$.initKeyEvent(that);

			// ログ出力
			$.log(that.timeData, 'initialize:');
		},
		setInitObjectState: function(){	// 画面初期化時の項目制御
			var that = this;
			$.reg.search = true;	// 当画面はデフォルト検索
			// 引き継ぎ情報セット
			var sendBtnid = $.getJSONValue(that.jsonHidden, "sendBtnid");
			if(sendBtnid && sendBtnid.length > 0){
				that.sendBtnid = sendBtnid;
				$.reg.search = true;
			}

			$.initReportInfo("x250", "仕掛商品検索");
		},
		initCondition: function (){	// 条件初期値セット
			var that = this;
			// 初期化項目
		},

		/**
		 * ダウンロードボタン押下時、CSVダウンロードイベント
		 * @param {Object} e
		 */
		pushDownload:function(e){
			var that = this;

			// レポート番号取得
			var reportno=$($.id.hidden_reportno).val();
			var id = $(this).attr('id');

			// レポート定義位置
			var reportNumber = $.getReportNumber(reportno);
			if (typeof(reportNumber) !== 'number') { alert("レポート定義が見つかりません。"); return false;}

			// マスク削除
			$.removeMask();

			// フォーム情報取得
			$.report[reportNumber].getEasyUI();

			if ($.report[reportNumber].validation(id)) {
				var func_ok = function(){
					// マスク追加
					$.appendMask();

					// セッションタイムアウト、利用時間外の確認
					var isTimeout = $.checkIsTimeout();
					if (! isTimeout) {
						// 検索条件保持
						$.ajax({
							url: $.reg.easy,
							type: 'POST',
							async: false,
							data: {
								"page"	: reportno,
								"obj"	: $.id.btn_search,
								"sel"	: "json",
								"userid": $($.id.hidden_userid).val(),
								"user"	: $($.id.hiddenUser).val(),
								"report": $($.id.hiddenReport).val(),
								"json"	: JSON.stringify($.report[reportNumber].jsonStringCsv)
							},
							success: function(json){
								// Excel 出力
								$.report[reportNumber].srccsv(reportno, id);
							}
						});
					}
				};

				// CSVデータを出力します。よろしいですか？
				$.showMessage("W20015", undefined, func_ok);
			} else {
				return false;
			}
		},

		/**
		 * 仕掛ボタンイベント
		 */
		pushShikakari:function(id){
			var that = this;

			if($($.id.hiddenSelectIdx).val() === ''){
				alert('更新する項目を1件以上選択してください。');
				return false;
			}

			// Grid内全情報取得
			var rows = $($.id.gridholder).datagrid('getRows');

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
					action: 			$.id.action_update,	// 実行処理情報
					MODE:			"2",				// モード (確定状態に更新)
					DATA:			JSON.stringify(targetRows)
				},
				success: function(data){
					// JSONに変換
					var json = JSON.parse(data);

					// ｾｯｼｮﾝ内Option取得
					if(json.opts!==null){

						if(json.opts.E_MSG !== undefined){
							alert(json.opts.E_MSG);
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
			var rows = $($.id.gridholder).datagrid('getData');

			// 対象情報抜粋
			var targetRows = [];
			var index = $($.id.hiddenSelectIdx).val().split(",");

			for (var i=0; i<rows.rows.length; i++){
				if($.inArray(i+'', index) !== -1){
					targetRows.push(rows.rows[i]);
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
					action: 			$.id.action_delete,	// 実行処理情報
					DATA:			JSON.stringify(targetRows)
				},
				success: function(data){
					// JSONに変換
					var json = JSON.parse(data);

					// ｾｯｼｮﾝ内Option取得
					if(json.opts!==null){
						if(json.opts.E_MSG !== undefined){
							alert(json.opts.E_MSG);
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
		 * 却下ボタンイベント
		 */
		pushPrev:function(id){
			var that = this;

			if($($.id.hiddenSelectIdx).val() === ''){
				alert('更新する項目を1件以上選択してください。');
				return false;
			}

			// Grid内全情報取得
			var rows = $($.id.gridholder).datagrid('getData');

			// 対象情報抜粋
			var targetRows = [];
			var index = $($.id.hiddenSelectIdx).val().split(",");

			for (var i=0; i<rows.rows.length; i++){
				if($.inArray(i+'', index) !== -1){
					targetRows.push(rows.rows[i]);
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
					action: 			$.id.action_update,	// 実行処理情報
					MODE:			"9",				// モード (「確定」→「却下」)
					DATA:			JSON.stringify(targetRows)
				},
				success: function(data){
					// JSONに変換
					var json = JSON.parse(data);

					// ｾｯｼｮﾝ内Option取得
					if(json.opts!==null){
						if(json.opts.E_MSG !== undefined){
							alert(json.opts.E_MSG);
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
		 * 承認ボタンイベント
		 */
		pushNext:function(id){
			var that = this;

			if($($.id.hiddenSelectIdx).val() === ''){
				alert('更新する項目を1件以上選択してください。');
				return false;
			}

			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform+'_2').form('validate');
			if(rt == false) return false;

			// Grid内全情報取得
			var rows = $($.id.gridholder).datagrid('getData');

			// 対象情報抜粋
			var targetRows = [];
			var index = $($.id.hiddenSelectIdx).val().split(",");
			for (var i=0; i<rows.rows.length; i++){
				if($.inArray(i+'', index) !== -1){
					targetRows.push(rows.rows[i]);
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
					action: 			$.id.action_update,	// 実行処理情報
					MODE:			"3",				// モード (「確定」→「承認」)
					DATA:			JSON.stringify(targetRows)
				},
				success: function(data){
					// JSONに変換
					var json = JSON.parse(data);

					// ｾｯｼｮﾝ内Option取得
					if(json.opts!==null){
						if(json.opts.E_MSG !== undefined){
							alert(json.opts.E_MSG);
						}else if(json.opts.S_MSG !== undefined){
							alert(json.opts.S_MSG);
							// datagrid更新
							that.success(that.name, false);
						}
					}
				}
			});
		},
		isNumber: function (numVal){
			// チェック条件パターン
			var pattern = /^\d*$/;
			// 数値チェック
			return pattern.test(numVal);

		},
		validation: function (btnId){	// （必須）批准
			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform).form('validate');
			if(!rt){
				$.addErrState(that, $('.validatebox-invalid').eq(0), false);
				return rt;
			}

			var szHatyu			= $.getInputboxValue($('#txt_hatyu'));		// 発注先コード
			var flg = "";

			if(szHatyu.length !== 0){
				flg = that.isNumber(szHatyu);

				if(flg===false){
					$.addErrState(that, $('#txt_hatyu'), true);
					$.addErrState(that, $('#txt_hatyu'), true);
					alert("発注先コードは数字のみで入力してください。");
					rt = false;
				}
			}

			/*
			// combogrid 入力値チェック
			if (rt == true) rt = $.checkCombogrid(that.jsonTemp, $.id.SelHatyu, '発注先1');
			*/

			// 入力エラーなしの場合に検索条件を格納
			if(btnId===$.id.btn_download){
				if (rt == true) that.jsonStringCsv = that.jsonTemp.slice(0);
			}else{
				if (rt == true) {that.jsonString = that.jsonTemp.slice(0);
				 that.jsonStringCsv = that.jsonTemp.slice(0);
				}
			}

			// 入力チェック用の配列をクリア
			that.jsonTemp = [];
			return rt;
		},
		success: function(reportno, sortable, btnId){	// （必須）正処理
			if (sortable) sortable=1; else sortable=0;
			var that = this;
			// 検索実行
			var szSelBumon		= $.getJSONObject(this.jsonString, $.id.SelBumon).value;			// 部門
			var szShohin				= $.getJSONObject(this.jsonString, $.id.txt_shohin).value;			// 商品名
			var szHatyu				= $.getInputboxValue($('#txt_hatyu'));										// 発注先
			var szFromdate			= $.getJSONObject(this.jsonString, $.id.txt_ymdf).value;			// 商品登録日FROM
			var szTodate				= $.getJSONObject(this.jsonString, $.id.txt_ymdt).value;			// 商品登録日TO
			var szLimit				= 10000;																					// 検索上限数

			if(!btnId) btnId = $.id.btn_search;

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
					BTN:				btnId,
					BUMON:		szSelBumon,			// 部門
					SHOHIN:		szShohin,		// 商品名
					HATYU:			szHatyu,			// 発注先
					FROM_DATE:	szFromdate,			// 商品登録日FROM
					TO_DATE:		szTodate,			// 商品登録日TO
					LIMIT:			szLimit,			// 検索上限数
					t:					(new Date()).getTime(),
					sortable:		sortable,
					sortName:		that.sortName,
					sortOrder:		that.sortOrder,
					rows:			0				// 表示可能レコード数
				},
				function(json){
					// 検索処理エラー判定
					if($.searchError(json, undefined, that)) return false;

					// ログ出力
					$.log(that.timeData, 'query:');
					// Load処理回避
					$.tryChangeURL(null);

					var limit = 1000;
					var size = JSON.parse(json)["total"];
					if(size > limit){
						$.showMessage('E00010');
					}

					if (sortable===0){
						var options = dg.datagrid('options');

						// 初期検索時に並び替え情報のリセット
						options.sortName = null;
						options.sortOrder = null;
					}

					that.queried = true;
					that.pushBtnid = btnId;
					// Load処理回避
					$.tryChangeURL($.reg.jqeasy);

					// グリッド再描画（easyui 1.4.2 対応）
					dg.datagrid('load', {} );
					$.removeMask();
					// 検索ボタン無効化
					$.setButtonState('#'+$.id.btn_search, false, 'success');
					// ログ出力
					$.log(that.timeData, 'loaded:');
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
			// 部門
			this.jsonTemp.push({
				id:		$.id.SelBumon,
				value:	$('#'+$.id.SelBumon).combogrid('getValue'),
				text:	$('#'+$.id.SelBumon).combogrid('getText')
			});
			// 商品名
			this.jsonTemp.push({
				id:		$.id.txt_shohin,
				value:	$('#'+$.id.txt_shohin).textbox('getText'),
				text:	''
			});
			// 発注先
			this.jsonTemp.push({
				id:		'txt_hatyu',
				value:	$('#txt_hatyu').textbox('getValue'),
				text:	$('#txt_hatyu').textbox('getText')
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
		getComboErr: function (obj,editable,newValue,oldValue) {
			var data = obj.combobox('getData');

			if (!obj.hasClass('datagrid-editable-input')) {
				if (!$.setComboReload(obj,true) && !editable) {
					$.showMessage("E11302",["入力値"],function () {$.addErrState(this,obj,false)});
					obj.combobox('reload');
					obj.combobox('hidePanel');
				} else if ($.isEmptyVal(newValue)) {
					obj.combobox('setValue',obj.combobox('getData')[0].VALUE);
				} else if ($.isEmptyVal(oldValue)) {
					if (obj.next().find('[tabindex=1]').length===1) {
						obj.combo("textbox").focus();
					}
				}
			}
		},
		getChange: function (id) {
			var that = this;
			var newVal = $.getInputboxValue($('#'+id))*1;
			var oldVal = id===$.id.SelBumon ? that.oldBmn:that.oldDai;

			if (!$.isEmptyVal(oldVal) && oldVal===newVal) {
				return false;
			} else {
				return true;
			}
		},

		setBumon: function(reportno, id){		// 部門
			var that = this;
			var idx = -1;
			var onChange=false;
			var onPanel=false;

			// フォーカスアウトのタイミングの動作
			$('#'+id).next().on('focusout', function(e){
				// 変更があったか
				if (!that.getChange(id)) {
					return false;
				} else {
					that.oldBmn=$.getInputboxValue($('#'+id))*1;
				}

				var obj = $(this).prev();

				if (!$.setComboReload(obj,false)) {
					obj.combobox('reload');
				}
			});

			$('#'+id).combobox({
				panelWidth:250,
				url:$.reg.easy,
				required: true,
				editable: true,
				autoRowHeight:false,
				valueField:'VALUE',
				textField:'TEXT',
				multiple :false,
				prompt: '',
				icons: [{
				}],
				keyHandler: {
					up: $.fn.combobox.defaults.keyHandler.up,
					down: $.fn.combobox.defaults.keyHandler.down,
					left: $.fn.combobox.defaults.keyHandler.left,
					right: $.fn.combobox.defaults.keyHandler.right,
					enter: function(e){
						$('#'+id).combobox('hidePanel');
						e.preventDefault();
					},
					query: $.fn.combobox.defaults.keyHandler.query
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
					param.datatype	=	"combobox";
				},
				onLoadSuccess:function(data){
					// 選択値設定
					var val = null;
					if ($.inArray(id, that.initedObject) < 0){
						that.initedObject.push(id);
						var json = $.getJSONObject(that.jsonHidden, id);
						if(json && json.value!=""){
							val = new Array();
							for (var i=0; i<data.length; i++){
								if ($.inArray(data[i].VALUE, json.value)!=-1){
									val.push(data[i].VALUE);
								}
							}
							if (val.length===data.length || val.length===0){
								val = null;
							}
						}
					}
					if (val){
						$('#'+id).combobox('setValues',val);
					} else {
						$('#'+id).combobox('setValues','-1');
					}
					idx = 1;

					// ログ出力
					$.log(that.timeData, id+' init:');
					// 検索ボタン有効化
					$.setButtonState('#'+$.id.btn_search, true, id);
					// 初期表示検索処理
					$.initialSearch(that);
					that.onChangeFlag = false;
				},
				onShowPanel: function(){
					$.setScrollComboBox(id);
					that.onChangeFlag=false;
					onChange=false;
					onPanel=true;
				},
				onHidePanel: function(){
					if (onChange){
					}else{
						that.onChangeFlag=true;
					}
					onChange=false;
					onPanel=false;
				},
				onChange:function(newValue, oldValue, obj){
					// 変更があったか
					if (!that.getChange(id)) {
						return false;
					};

					if(obj===undefined){obj = $(this);}

					if(idx > 0){
						$.removeErrState();
						// 検索ボタン有効化
						$.setButtonState('#'+$.id.btn_search, true, id);
					}
					onChange=true;
					that.getComboErr(obj,false,newValue,oldValue);
				}
			});
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
				rownumbers:true,
				fit:true,
				view:scrollview,
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
							if($.inArray(rowData['F1'], ['作成中','確定']) !== -1){
								return '<input id="ck_'+rowIndex+'" type="checkbox" style="opacity:1;">';
							} else {
								return '';
							}
						}
					},
					{field:'F1',	title:'状態',	width:60,	align:'center',	halign:'center'},
				]],
				columns:[[
					{field:'F2',	title:'件名No',			width: 80,halign:'center',align:'left'},
					{field:'F3',	title:'商品コード',			width: 80,halign:'center',align:'left',
						formatter:function(value, rowData, rowIndex) { return '<a id="F3_'+rowIndex+'" href="#">'+value+'</a>'; }},
					{field:'F4',	title:'ソースコード1',		width:120,halign:'center',align:'left'},
					{field:'F5',	title:'商品名',					width:300,halign:'center',align:'left'},
					{field:'F6',	title:'扱<br>区分',			width: 40,halign:'center',align:'left'},
					{field:'F7',	title:'原価',						width: 70,halign:'center',align:'right',
						formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0.00');}},
					{field:'F8',	title:'本体売価',				width: 70,halign:'center',align:'right',
						formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0');}},
					{field:'F9',	title:'総額売価',				width: 70,halign:'center',align:'right',
						formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0');}},
					{field:'F10',	title:'店入数',				width: 70,halign:'center',align:'right',
						formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0');}},
					{field:'F11',	title:'親コード',			width: 80,halign:'center',align:'left'},
					{field:'F12',	title:'ワッペン区分',		width: 60,halign:'center',align:'left'},
					{field:'F13',	title:'一括区分',			width: 40,halign:'center',align:'left'},
					{field:'F14',	title:'標準仕入先',		width: 70,halign:'center',align:'left'},
					{field:'F15',	title:'分類コード',		width:100,halign:'center',align:'left'},
					{field:'F16',	title:'更新日',				width: 70,halign:'center',align:'center'},
					{field:'F17',	title:'衣料使い回し',		width: 50,halign:'center',align:'center'},
					{field:'F18',	title:'販売コード',	hidden:true},
				]],
				fitColumns:false,	// 指定カラム幅を適用する場合、false 指定。
				rowStyler:function(index, row){
				},
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

					// 商品コード
					var link = panel.find('.datagrid-row a').filter('[id^=F3_]');
					link.click(function(){
						var index = $(this).attr('id').split('_')[1];
						var targetRow = $($.id.gridholder).datagrid('getRows')[index];
						that.changeReport(reportno, "", targetRow);
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
		loadSuccessFunc:function(id, data){				// 画面遷移
			var that = this;
			// 画面遷移による検索以外の場合
			if(that.sendBtnid.length != 0){
				// 初回以外は移動OKのため、初期化
				that.sendBtnid = "";
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
		},
		getJSONString : function(){		// （必須）JSON形式の文字列
			return this.jsonString;
		},
		tryLoadMethods: function(id){	// （オプション）combo.onChange Event
			var that = this;
			// セッションタイムアウト確認
			if ($.checkIsTimeout(that)) return false;
			var _$id = $(id);
			try {
				_$id.combogrid('clear');
				var grid = _$id.combogrid('grid');
				grid.datagrid('load');
			} catch (e) {
				// combgrid 未更新時のERROR回避
				try{
					_$id.combobox('clear');
					_$id.combobox('reload');
				}catch(e){
				}
			}
		},
		changeReport:function(reportno, btnId, rowData){	// 画面遷移
			var that = this;

			// 遷移判定
			var index = 0;
			var childurl = "";

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
			$.setJSONObject(sendJSON, 'searchIndex',	2,	2);						// 検索画面位置インデックス


			var index = 3;
			var childurl = href[index];

			// 実行ボタン別処理
			switch (btnId) {
			case $.id.btn_back:
				// 転送先情報
				childurl = parent.$('#hdn_menu_path').val();

				break;
			default:
				$.setJSONObject(sendJSON, 'hiddenTeianStcd',	rowData['F1'],	rowData['F1']);		// 状態_仕掛商品
				$.setJSONObject(sendJSON, $.id.hiddenNoTeian,	rowData['F2'],	rowData['F2']);		// 件名No
				$.setJSONObject(sendJSON, $.id.hiddenShohin,	rowData['F3'],	rowData['F3']);		// 商品コード
				$.setJSONObject(sendJSON, 'hiddenSrccd',			rowData['F4'],	rowData['F4']);		// ソースコード1
				$.setJSONObject(sendJSON, 'hiddenShnkn',			rowData['F5'],	rowData['F5']);		// 商品名
				$.setJSONObject(sendJSON, 'hiddenAtsukkbn',		rowData['F6'],	rowData['F6']);		// 扱区分
				$.setJSONObject(sendJSON, 'hiddenGenka',			rowData['F7'],	rowData['F7']);		// 原価
				$.setJSONObject(sendJSON, 'hiddenBaika',			rowData['F8'],	rowData['F8']);		// 本体売価
				$.setJSONObject(sendJSON, 'hiddenSoBaika',		rowData['F9'],	rowData['F9']);		// 総額売価
				$.setJSONObject(sendJSON, 'hiddenTenIrisu',		rowData['F10'],	rowData['F10']);	// 店入数
				$.setJSONObject(sendJSON, 'hiddenOyacd',			rowData['F11'],	rowData['F11']);	// 親コード
				$.setJSONObject(sendJSON, 'hiddenWapkbn',		rowData['F12'],	rowData['F12']);	// ワッペン区分
				$.setJSONObject(sendJSON, 'hiddenAllkbn',			rowData['F13'],	rowData['F13']);	// 一括区分
				$.setJSONObject(sendJSON, 'hiddenSirsk',			rowData['F14'],	rowData['F14']);	// 標準仕入先
				$.setJSONObject(sendJSON, 'hiddenBunruicd',		rowData['F15'],	rowData['F15']);	// 分類コード
				$.setJSONObject(sendJSON, 'hiddenUpddt',			rowData['F16'],	rowData['F16']);	// 更新日
				$.setJSONObject(sendJSON, 'hiddenIryou',			rowData['F17'],	rowData['F17']);	// 衣料使い回し
				$.setJSONObject(sendJSON, "itemMode",		2,	2);							// 商品情報参照先
				$.setJSONObject(sendJSON, 'callpage', that.name, that.name);
				$.setJSONObject(sendJSON, "Mode", "Shikakari", "Shikakari");

				$.setJSONObject(sendJSON, $.id.txt_sel_shncd, rowData['F3'].replace("-",""), rowData['F3'].replace("-",""));
				$.setJSONObject(sendJSON, $.id.txt_sel_shnkn, rowData['F5'], rowData['F5']);
				$.setJSONObject(sendJSON, $.id_inp.txt_shncd, rowData['F3'], rowData['F3']);

				if(rowData["F1"]==="承認"||rowData["F1"]==="却下"||rowData["F1"]==="完了"){
					$.setJSONObject(sendJSON, 'sendBtnid', $.id.btn_sel_refer, $.id.btn_sel_refer);
				}else{
					$.setJSONObject(sendJSON, 'sendBtnid', $.id.btn_sel_change, $.id.btn_sel_change);
				}

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
		},
		srccsv: function(reportno, btnId){	// ここではCsv出力
			var that = this;
			// 処理時間計測用
			that.timeData = (new Date()).getTime();

			// 検索実行
			var szSelBumon		= $.getJSONObject(this.jsonStringCsv, $.id.SelBumon).value;			// 部門
			var szShohin				= $.getJSONObject(this.jsonStringCsv, $.id.txt_shohin).value;			// 商品名
			var szHatyu				= $.getInputboxValue($('#txt_hatyu'));			// 発注先
			var szFromdate				= $.getJSONObject(this.jsonString, $.id.txt_ymdf).value;				// 商品登録日FROM
			var szTodate					= $.getJSONObject(this.jsonString, $.id.txt_ymdt).value;				// 商品登録日TO
			if(!btnId) btnId = $.id.btn_search;

			// タイトル部
			var title = [["状態", "件名No", "更新区分","商品コード","商品コード桁数指定","定計区分","ソース区分_1","ソースコード_1","標準仕入先コード",
							"定貫不定貫区分","標準分類コード_部門","標準分類コード_大","標準分類コード_中","標準分類コード_小","標準分類コード_小小",
							"ＰＣ区分","商品種類","親商品コード","商品名（カナ）","商品名（漢字）","レシート名（カナ）","レシート名（漢字）",
							"プライスカード商品名称（漢字）","商品コメント・セールスコピー（漢字）","ＰＯＰ名称（漢字）","規格",
							"レギュラー情報_取扱フラグ","レギュラー情報_原価","レギュラー情報_売価","レギュラー情報_店入数",
							"レギュラー情報_一括伝票フラグ","レギュラー情報_ワッペン","販促情報_取扱フラグ","販促情報_原価","販促情報_売価",
							"販促情報_店入数","販促情報_ワッペン","販促情報_特売ワッペン","便区分","締め回数","小物区分","仕分区分","棚卸区分","期間",
							"ODS_賞味期限_春","ODS_賞味期限_夏","ODS_賞味期限_秋","ODS_賞味期限_冬","ODS_入荷期限","ODS_値引期限",
							"販促情報_スポット最低発注数","製造限度日数","リードタイムパターン","発注曜日_月","発注曜日_火","発注曜日_水",
							"発注曜日_木","発注曜日_金","発注曜日_土","発注曜日_日","配送パターン","ユニットプライス_容量","ユニットプライス_単位容量",
							"ユニットプライス_ユニット単位","商品サイズ_縦","商品サイズ_横","商品サイズ_奥行","商品サイズ_重量","取扱期間_開始日",
							"取扱期間_終了日","陳列形式コード","段積み形式コード","重なりコード","重なりサイズ","圧縮率","マスタ変更予定日",
							"店売価実施日","用途分類コード_部門","用途分類コード_大","用途分類コード_中","用途分類コード_小","売場分類コード_部門",
							"売場分類コード_大","売場分類コード_中","売場分類コード_小","エリア区分","店グループ（エリア）_1","仕入先コード_1",
							"配送パターン_1","店グループ（エリア）_2","仕入先コード_2","配送パターン_2","店グループ（エリア）_3","仕入先コード_3",
							"配送パターン_3","店グループ（エリア）_4","仕入先コード_4","配送パターン_4","店グループ（エリア）_5","仕入先コード_5",
							"配送パターン_5","店グループ（エリア）_6","仕入先コード_6","配送パターン_6","店グループ（エリア）_7","仕入先コード_7",
							"配送パターン_7","店グループ（エリア）_8","仕入先コード_8","配送パターン_8","店グループ（エリア）_9","仕入先コード_9",
							"配送パターン_9","店グループ（エリア）_10","仕入先コード_10","配送パターン_10","エリア区分","店グループ（エリア）_1",
							"原価_1","売価_1","店入数_1","店グループ（エリア）_2","原価_2","売価_2","店入数_2","店グループ（エリア）_3","原価_3",
							"売価_3","店入数_3","店グループ（エリア）_4","原価_4","売価_4","店入数_4","店グループ（エリア）_5","原価_5","売価_5",
							"店入数_5","エリア区分","店グループ（エリア）_1","扱い区分_1","店グループ（エリア）_2","扱い区分_2",
							"店グループ（エリア）_3","扱い区分_3","店グループ（エリア）_4","扱い区分_4","店グループ（エリア）_5","扱い区分_5",
							"店グループ（エリア）_6","扱い区分_6","店グループ（エリア）_7","扱い区分_7","店グループ（エリア）_8","扱い区分_8",
							"店グループ（エリア）_9","扱い区分_9","店グループ（エリア）_10","扱い区分_10","平均パック単価","ソース区分_2",
							"ソースコード_2","プライスカード出力有無","プライスカード_種類","プライスカード_色","税区分","税率区分","旧税率区分",
							"税率変更日","取扱停止","市場区分","ＰＢ区分","返品区分","輸入区分","裏貼","対象年齢","カロリー表示","加工区分",
							"産地（漢字）","酒級","度数","包材用途","包材材質","包材リサイクル対象","フラグ情報_ＥＬＰ","フラグ情報_ベルマーク",
							"フラグ情報_リサイクル","フラグ情報_エコマーク","メーカーコード","販売コード","添加物_1","添加物_2","添加物_3","添加物_4",
							"添加物_5","添加物_6","添加物_7","添加物_8","添加物_9","添加物_10","アレルギー_1","アレルギー_2","アレルギー_3",
							"アレルギー_4","アレルギー_5","アレルギー_6","アレルギー_7","アレルギー_8","アレルギー_9","アレルギー_10","アレルギー_11",
							"アレルギー_12","アレルギー_13","アレルギー_14","アレルギー_15","アレルギー_16","アレルギー_17","アレルギー_18",
							"アレルギー_19","アレルギー_20","アレルギー_21","アレルギー_22","アレルギー_23","アレルギー_24","アレルギー_25",
							"アレルギー_26","アレルギー_27","アレルギー_28","アレルギー_29","アレルギー_30","種別コード","衣料使い回しフラグ",
							"登録元","オペレータ","登録日","更新日"]];

			var kbn = 0;
			var data = {
				report:			that.name,								// レポート名
				'kbn':			 kbn,
				'type':			'csv',
				'header':		JSON.stringify(title),
				BTN:				btnId,
				BUMON:		szSelBumon,		// 部門
				SHOHIN:		szShohin,			// 商品名
				HATYU:			szHatyu,									// 発注先
				FROM_DATE:	szFromdate,								// 商品登録日FROM
				TO_DATE:		szTodate,									// 商品登録日TO
				t:					(new Date()).getTime(),
				rows:			0												// 表示可能レコード数
			};

			// 転送
			$.ajax({
				url: $.reg.srcexcel,
				type: 'POST',
				data: data,
				async: true
			})
			.done(function(){
				// Excel出力
				$.outputSearchExcel(reportno, 0);
			})
			.fail(function(){
				// Excel出力エラー
				$.outputSearchExcelError();
			})
			.always(function(){
				// 通信完了
				// ログ出力
				$.log(that.timeData, 'srcexcel:');
			});
		}
	} });
})(jQuery);