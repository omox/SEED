/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_Reportx005',			// （必須）レポートオプションの確認
		jsonTemp:	[],							// （必須）検索条件情報_入力チェック前
		jsonString:	[],							// （必須）検索条件情報
		jsonHidden: [],							// （必須）親画面からの引き継ぎ情報
		jsonInit: [],							// （必須）検索条件初期情報
		caption: function(){					// （必須）タイトル
			return $('#reportname').val();
		},
		sortName: '',	// ソート項目名
		sortOrder: '',	// ソート順
		timeData : (new Date()).getTime(),
		dedefaultObjNum:	6,	// 初期化オブジェクト数
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
		sendBtnid: "",						// （必須）呼出ボタンID情報
		pushBtnid: "",						// （必須）実行ボタンID情報(検索系で利用)
		focusRootId:"cc",					// （キー移動時必須）キー移動イベントのルートとなるパネルのID
		focusParentId:"",					// （キー移動時必須）現在フォーカスがあたっている項目の親となるパネルのID
		focusGridId:"",						// （キー移動時必須）現在フォーカスがあたっているDataGridのID
		initialize: function (reportno){	// （必須）初期化
			var that = this;
			// 引き継ぎ情報
			this.jsonHidden = $.getTargetValue();

			$.setMeisyoCombo(that, reportno, $.id.sel_bnnruikbn)

			// 画面の初回基本設定
			this.setInitObjectState();

			// 初期検索条件設定
			this.jsonInit = $.getInitValue();
			// データ表示エリア初期化
			that.setGrid($.id.gridholder, reportno);

			// 初期化するオブジェクト数設定
			this.initObjNum = this.dedefaultObjNum;
			// 検索を実行するためにフラグ変更
			that.onChangeReport = true;
			that.setLabelbox($.id.txt_seq);					// テキスト（SEQ) 必須
			that.setLabelbox($.id.txt_operator);			// テキスト（オペレータ)
			that.setLabelbox($.id.txt_upddt);				// テキスト（更新日)
			that.setLabelbox($.id.txt_updtm);				// テキスト（更新時刻)
			that.setLabelbox($.id_inp.txt_commentkn);		// テキスト（コメント)
			that.setLabelbox($.id.txt_err_number);			// テキスト（エラー件数)

			// Load処理回避
			//$.tryChangeURL(null);

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

			// 引き継ぎ情報セット
			var sendBtnid = $.getJSONValue(that.jsonHidden, "sendBtnid");
			if(!sendBtnid){
				sendBtnid = $('#sendBtnid').val();
			}
			$('#sendBtnid').val(sendBtnid);
			that.sendBtnid = sendBtnid;

			var reportYobi1 = $.getJSONValue(that.jsonHidden, "reportYobi1");
			if(!reportYobi1){
				reportYobi1 = $('#reportYobi1').val();
			}
			$('#reportYobi1').val(reportYobi1);


			// 当帳票を「参照」で開いた場合
			if(that.reportYobiInfo()==='1'){
				$($.id.buttons).hide();

				$.initReportInfo("IT025", "商品マスタ　CSV取込　エラー一覧", "一覧");
			}else{
				$($.id.buttons).show();
				// 各種ボタン
				$('#'+$.id.btn_err_change).on("click", $.pushChangeReport);

				$.initReportInfo("IT025", "商品マスタ　CSV取込　エラー選択", "選択");
			}

		},
		initCondition: function (){	// 条件初期値セット
			var that = this;
			// 初期化項目
		},
		clear:function(){
//			// 隠し情報初期化
//			$($.id.hiddenChangedIdx).val("");						// 変更行Index
//			// グリッド初期化
//			this.success(this.name, false);
		},
		validation: function (btnId){	// （必須）批准
			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform).form('validate');
			if(!rt){
				$.addErrState(that, $('.validatebox-invalid').eq(0), false);
				return rt;
			}
			// 入力エラーなしの場合に検索条件を格納
			if (rt == true) that.jsonString = that.jsonTemp.slice(0);
			// 入力チェック用の配列をクリア
			that.jsonTemp = [];
			return rt;
		},
		success: function(reportno, sortable, btnId){	// （必須）正処理
			if (sortable) sortable=1; else sortable=0;
			var that = this;
			// 検索実行
			var szSeq		= $.getJSONObject(this.jsonString, $.id.txt_seq).value;			// テキストボックス：SEQ
			if(!btnId) btnId = $.id.btn_search;

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// initialDisplayだと別にメッセージがでるので削除
			// $($.id.gridholder).datagrid('loading');

			// grid.options 取得
			var options = $($.id.gridholder).datagrid('options');
			that.sortName	= options.sortName;
			that.sortOrder	= options.sortOrder;

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,		// レポート名
					SEQ:			szSeq,
					BTN:			btnId,
					t:				(new Date()).getTime(),
					sortable:		sortable,
					sortName:		that.sortName,
					sortOrder:		that.sortOrder,
					rows:			0	// 表示可能レコード数
				},
				function(json){
					// 検索処理エラー判定
					if($.searchError(json)) return false;

					// ログ出力
					$.log(that.timeData, 'query:');

					// Load処理回避
					$.tryChangeURL(null);

					var opts = JSON.parse(json).opts
					if(opts && opts.rows_){
						// 基本データ表示
						that.setData(opts.rows_, opts);
					}

					that.queried = true;
					that.pushBtnid = btnId;
					// Load処理回避
					$.tryChangeURL($.reg.jqeasy);

					// グリッド再描画（easyui 1.4.2 対応）
					$($.id.gridholder).datagrid('load', {} );
					$.removeMask();
					$.removeMaskMsg();

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

			// SEQ
			this.jsonTemp.push({
				id:		$.id.txt_seq,
				value:	$('#'+$.id.txt_seq).val(),
				text:	$('#'+$.id.txt_seq).val()
			});
			// オペレーターコード
			this.jsonTemp.push({
				id:		$.id.txt_operator,
				value:	$('#'+$.id.txt_operator).textbox('getValue'),
				text:	$('#'+$.id.txt_operator).textbox('getText')
			});
			// 更新日
			this.jsonTemp.push({
				id:		$.id.txt_upddt,
				value:	$('#'+$.id.txt_upddt).numberbox('getValue'),
				text:	$('#'+$.id.txt_upddt).numberbox('getText')
			});
			// 更新時刻
			this.jsonTemp.push({
				id:		$.id.txt_updtm,
				value:	$('#'+$.id.txt_updtm).numberbox('getValue'),
				text:	$('#'+$.id.txt_updtm).numberbox('getText')
			});
			// コメント
			this.jsonTemp.push({
				id:		$.id_inp.txt_commentkn,
				value:	$('#'+$.id_inp.txt_commentkn).textbox('getValue'),
				text:	$('#'+$.id_inp.txt_commentkn).textbox('getText')
			});
			// エラー件数
			this.jsonTemp.push({
				id:		$.id.txt_err_number,
				value:	$('#'+$.id.txt_err_number).numberbox('getValue'),
				text:	$('#'+$.id.txt_err_number).numberbox('getText')
			});

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
		},
		setObjectState: function(){	// 軸の選択内容による制御

		},
		setLabelbox: function(id){	// 入力項目の初期値を設定する
			var that = this;

			// 初期化情報取得
			var json = $.getJSONObject(that.jsonHidden, id);
			var idx = -1;

			if($('#'+id).is(".easyui-textbox")){
				if (json && json.value.length > 0){ $('#'+id).textbox("setValue",json.value); }
			}else if($('#'+id).is(".easyui-numberbox")){
				var idx = -1;
				var formatter = $.fn.numberbox.defaults.formatter;
				var parser = $.fn.numberbox.defaults.parser;
				var options = $('#'+id).numberbox('options');
				if(options.prompt){
					var format = options.prompt.replace(/_/g, '#');
					formatter = function(value){
						return $.getFormatPrompt(value, format);
					};
					parser= function(value){
						return $.getParserPrompt(value);
					};
				}
				$('#'+id).numberbox({
					formatter:formatter,
					parser:parser
				});
				if (json && json.value.length > 0){ $('#'+id).numberbox("setValue",json.value); }
			}else{
				if (json && json.value.length > 0){ $('#'+id).val(json.value); }

			}
			if (that.initedObject && $.inArray(id, that.initedObject) < 0){
				that.initedObject.push(id);
				$.initialDisplay(that);
			}
			idx = 1;
		},
		setGrid: function (id, reportNumber){	// グリッドの構築
			var that = this;
			var init = true;
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
				pageSize:pageSize,
				pageList:pageList,
				frozenColumns:[[
					{field:'F1',	title:'商品コード',			width: 80,halign:'center',align:'left'},
				]],
				columns:[[
					{field:'F2',	title:'ソースコード1',		width: 120,halign:'center',align:'left'},
					{field:'F3',	title:'販売コード',			width: 70,halign:'center',align:'left'},
					{field:'F4',	title:'親コード',			width: 80,halign:'center',align:'left'},
					{field:'F5',	title:'商品名',				width:300,halign:'center',align:'left'},
					{field:'F6',	title:'扱<br>区分',			width: 40,halign:'center',align:'left'},
					{field:'F7',	title:'原価',				width: 70,halign:'center',align:'right',
						formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0.00');}},
					{field:'F8',	title:'本体売価',			width: 70,halign:'center',align:'right',
						formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0');}},
					{field:'F9',	title:'総額売価',			width: 70,halign:'center',align:'right',
						formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0');}},
					{field:'F10',	title:'店入数',				width: 70,halign:'center',align:'right',
						formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0');}},
					{field:'F11',	title:'ワッペン区分',		width: 60,halign:'center',align:'left'},
					{field:'F12',	title:'一括区分',			width: 40,halign:'center',align:'left'},
					{field:'F13',	title:'標準仕入先',			width: 70,halign:'center',align:'left'},
					{field:'F14',	title:'分類コード',			width: 70,halign:'center',align:'left'},
					{field:'F15',	title:'SEQ',				hidden:true},
					{field:'F16',	title:'入力番号',			hidden:true},
					{field:'F17',	title:'CSV登録区分',		hidden:true}
				]],
				fitColumns:false,	// 指定カラム幅を適用する場合、false 指定。
				rowStyler:function(index, row){
				},
				onSortColumn:function(sort, order){
					if (that.jsonString.length===0) return false;
					// カラム並び替え
					that.success(that.name, true);
				},
				onBeforeLoad:function(param){
					param.report = that.name;
				},
				onLoadSuccess:function(data){
					$.setInputboxValue($('#'+$.id.txt_err_number), that.getRecord(), true);

					if(init){
						init = false;
						that.setResize();
						return;	// 中断
					}

					// 検索後、初回のみ処理
					if (that.queried){
						that.queried = false;	// 検索後、初回のみ処理
						// 状態保存
						$.saveState(reportNumber, that.getJSONString(), id);
						// 警告
						$.showWarningMessage(data);
					}
				},
				onClickCell:function(rowIndex, field, value){
					// 列名保持
					that.columnName = field;
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

//				// window 幅取得
//				var changeWidth  = $(window).width();
//
//				// toolbar の調整
//				$($.id.toolbar).panel('resize',{width:changeWidth});

//				// toolbar の高さ調整
//				$.setToolbarHeight();

//				// DataGridの高さ
//				var gridholderHeight = 0;
//				var placeholderHeight = 0;

//				if ($($.id.gridholder).datagrid('options') != 'undefined') {
//					// tb
//					placeholderHeight = $($.id.toolbar).panel('panel').height() + $($.id.buttons).height();
//
//					// datagrid の格納された panel の高さ
//					gridholderHeight = $(window).height() - placeholderHeight;
//				}
//
//				$($.id.gridholder).datagrid('resize', {
//					width:	changeWidth,
//					height:	gridholderHeight
//				});
			}
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
		changeReport:function(reportno, btnId){				// 画面遷移
			var that = this;

			// 遷移判定
			var index = 0;
			var childurl = "";
			var sendMode = "";

			// タブ要素(a)取得
			var elems = $('#tabContent', window.parent.document).map(
				function(i,e) {
					return e;
				}).get();
			var href = elems[0].value.split(',');

			// JSON Object Clone ()
			var sendJSON = JSON.parse( JSON.stringify( that.jsonString ) );
			$.setJSONObject(sendJSON, 'sendBtnid', btnId, $('#'+btnId).attr("title"));					// 実行ボタン情報保持
			$.setJSONObject(sendJSON, 'reportYobi1', that.reportYobiInfo(), that.reportYobiInfo());		// 参照情報保持
			$.setJSONObject(sendJSON, 'callpage', that.name, that.name);								// 呼出し元レポート情報

			// 戻る実行時用に現在の画面情報を保持する
			var states = $.getBackBaseJSON(that);
			var newrepinfos = $.getBackJSON(that, states, false);
			$.setJSONObject(sendJSON, 'repinfo', newrepinfos, '');

			// 実行ボタン別処理
			switch (btnId) {
			case $.id.btn_back:
				// 転送先情報
				index = 1;
				// 元画面情報
				for (var i = 0; i < newrepinfos.length; i++) {
					var callpage = newrepinfos[i].id;
					if(callpage==='Out_Reportx004'){
						index = 4;
					}
					if(callpage==='Out_Reportx006'){
						index = 6;
					}
				}
				childurl = href[index];
				sendMode = 2;
				break;
			case $.id.btn_err_change:
				// 選択行
				var row = $($.id.gridholder).datagrid("getSelected");
				if(!row){
					$.showMessage('E00008');
					return false;
				}
				if(row.F17 !== $.id.value_csvupdkbn_new && row.F21 !== '1'){
					$.showMessage('E20160');
					return false;
				}
				if(row.F17 === $.id.value_csvupdkbn_new || row.F17 === $.id.value_csvupdkbn_upd){
					if(!($.isEmptyVal(row.F18, true) && $.isEmptyVal(row.F19, true))){
						$.showMessage("EX1096", ["正登録で、マスタ変更予定日・店売価実施日が０以外"]);
						return false;
					}
				}
				if(row.F17 === $.id.value_csvupdkbn_yyk && row.F22 !== '1' && row.F23 !== '1'){
					$.showMessage('E11304');
					return false;
				}
				if(row.F17 === $.id.value_csvupdkbn_ydel){
					$.showMessage("EX1096", ["CSV更新区分が予約取消"]);
					return false;
				}

				// 転送先情報
				index = 3;
				childurl = href[index];
				sendMode = 1;
				// オブジェクト作成
				$.setJSONObject(sendJSON, $.id.txt_sel_shncd, row.F20, row.F20);
				$.setJSONObject(sendJSON, $.id.txt_sel_shnkn, row.F5, row.F5);
				if(row.F17!==$.id.value_csvupdkbn_new){
					$.setJSONObject(sendJSON, $.id_inp.txt_shncd, row.F20, row.F20);
				}
				$.setJSONObject(sendJSON, $.id.txt_seq, row.F15, row.F15);
				$.setJSONObject(sendJSON, $.id.txt_inputno, row.F16, row.F16);
				$.setJSONObject(sendJSON, $.id.txt_csv_updkbn, row.F17, row.F17);
				$.setJSONObject(sendJSON, $.id_inp.txt_yoyakudt, row.F18, row.F18);
				$.setJSONObject(sendJSON, $.id_inp.txt_tenbaikadt, row.F19, row.F19);
				break;
			default:
				break;
			}

			$.SendForm({
				type: 'post',
				url: childurl,
				data: {
					sendMode:	sendMode,
					sendParam:	JSON.stringify( sendJSON )
				}
			});

		},
		excel: function(reportno){	// (必須)Excel出力
			// グリッドの情報取得
			var options = $($.id.gridholder).datagrid('options');

			// タイトル部
			var title = [];
			title = $.outputExcelTitle(title, options.frozenColumns);
			title = $.outputExcelTitle(title, options.columns);

			// タイトル数確認
			if ($.checkExcelTitle(title))	return;

			var kbn = options.frozenColumns[0].length;
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
				async: true
			})
			.done(function(){
				// Excel出力
				$.outputExcel(reportno, 0);
			})
			.fail(function(){
				// Excel出力エラー
				$.outputExcelError();
			})
			.always(function(){
				// 通信完了
			});
		}
	} });
})(jQuery);