/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_ReportJU031',			// （必須）レポートオプションの確認
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
		dedefaultObjNum:	4,	// 初期化オブジェクト数
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
		pushBtnId: "",						// 実行ボタンID情報(検索系で利用)
		focusRootId:"cc",					// （キー移動時必須）キー移動イベントのルートとなるパネルのID
		focusParentId:"",					// （キー移動時必須）現在フォーカスがあたっている項目の親となるパネルのID
		focusGridId:"",						// （キー移動時必須）現在フォーカスがあたっているDataGridのID
		moycdData:[],						// 検索結果保持用(催しコード/催し基本)
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
			var isUpdateReport = true;

			// 初期表示処理
			that.onChangeReport = true;

			// 入力テキストボックス系
			var inputbox = Object.getOwnPropertyNames($.id_inp);
			for ( var sel in inputbox ) {
				if($('#'+$.id_inp[inputbox[sel]]).length > 0){
					$.setInputbox(that, reportno, $.id_inp[inputbox[sel]], isUpdateReport);
				}
			}

			$.setInputbox(that, reportno, 'kikan_dummy', isUpdateReport);

			// Load処理回避
			//$.tryChangeURL(null);

			// 初期化終了
			this.initializes =! this.initializes;

			$.initialDisplay(that);

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
			var reportYobi1 = $.getJSONValue(that.jsonHidden, "reportYobi1");
			if(!reportYobi1){
				reportYobi1 = $('#reportYobi1').val();
			}
			$('#reportYobi1').val(reportYobi1);

			$($.id.buttons).show();
			// 各種ボタン
			if(reportYobi1  =='1'){
				$.initReportInfo("JU031", "店舗アンケート付き送付け 参照 商品一覧", "参照");
				$('#'+$.id.btn_sel_change).hide();
				$('#'+$.id.btn_sel_change).attr('disabled', 'disabled').hide();
				$('#'+$.id.btn_new).hide();
				$('#'+$.id.btn_new).attr('disabled', 'disabled').hide();
			}else{
				$.initReportInfo("JU031", "店舗アンケート付き送付け 新規・変更 商品一覧", "一覧");
				$('#'+$.id.btn_select).hide();
				$('#'+$.id.btn_select).attr('disabled', 'disabled').hide();
			}
			$('#'+$.id.btn_sel_change).on("click", $.pushChangeReport);
			$('#'+$.id.btn_new).on("click", $.pushChangeReport);
			$('#'+$.id.btn_select).on("click", $.pushChangeReport);
		},
		initCondition: function (){	// 条件初期値セット
			var that = this;
			// 初期化項目
		},
		clear:function(){
			// 隠し情報初期化
			$($.id.hiddenChangedIdx).val("");						// 変更行Index
//			// グリッド初期化
//			this.success(this.name, false);
		},
		validation: function (btnId){	// （必須）批准
			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			//var rt = $($.id.toolbarform).form('validate');
			var rt = true;
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
			var szMoyscd	= $.getJSONValue(this.jsonHidden, $.id_inp.txt_moyscd);		// 催しコード

			if(!btnId) btnId = $.id.btn_search;

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// initialDisplayだと別にメッセージがでるので削除
			// $($.id.gridholder).datagrid('loading');

			// grid.options 取得
			var options = $($.id.gridholder).datagrid('options');
			that.sortName	= options.sortName;
			that.sortOrder	= options.sortOrder;
			var sendBtnid = $.getJSONValue(that.jsonHidden, "sendBtnid");
			if(!sendBtnid){
				sendBtnid = $('#sendBtnid').val();
			}
			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,		// レポート名
					SENDBTNID:		sendBtnid,
					MOYSCD:			szMoyscd,		// 催しコード
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
						that.moycdData = opts.rows_;
					}

					that.queried = true;
					that.pushBtnId = btnId;
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
			// レポート名
			this.jsonTemp.push({
				id:		$.id_inp.txt_moyscd,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_moyscd),
				text:	''
			});
			// レポート名
			this.jsonTemp.push({
				id:		$.id_inp.txt_moykn,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_moykn),
				text:	''
			});
			this.jsonTemp.push({
				id:		'kikan_dummy',
				value:	$.getJSONValue(this.jsonHidden, 'kikan_dummy'),
				text:	''
			});
			// レポート名
			this.jsonTemp.push({
				id:		$.id_inp.txt_qasmdt,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_qasmdt),
				text:	''
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
		setGrid: function (id, reportNumber){	// グリッドの構築
			var that = this;
			var init = true;
			// ページサイズ定義取得
			var pageList = $.fn.pagination.defaults.pageList;
			var pageSize = $.getJSONValue(this.jsonHidden, $.id.pageSize);
			if (pageSize==="") pageSize=$.fn.pagination.defaults.pageSize;
			pageSize = $.getDefaultPageSize(pageSize, pageList);

			var bcstyler =function(value,row,index){return 'background-color:#f5f5f5;';};
			var dformatter =function(value){ return $.getFormatDt(value, true);};
			var cstyler=function(value,row,index){return 'color: red;font-weight: bold;background-color:#f5f5f5;';};
			var cformatter =function(value,row,index){return value && value===$.id.value_on?$.id.text_on:$.id.text_off;};
			var sendBtnid = $.getJSONValue(that.jsonHidden, "sendBtnid");
			if(!sendBtnid){
				sendBtnid = $('#sendBtnid').val();
			}

				$(id).datagrid({
					nowrap: true,
					border: true,
					striped: true,
					collapsible:false,
					remoteSort: true,
					rownumbers:false,
					fit:true,
					pageSize:pageSize,
					pageList:pageList,
					frozenColumns:[[]],
					columns:[[
						{field:'F1',	title:'商品コード',					        width:100,	halign:'center',align:'left'},
						{field:'F2',	title:'商品名',								width:300,	halign:'center',align:'left'},
						{field:'F3',	title:'入数',								width:50,	halign:'center',align:'right'},
						{field:'F4',	title:'原価',								width:90,	halign:'center',align:'right',formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0.00');}},
						{field:'F5',	title:'本体売価',	    					width:90,	halign:'center',align:'right',formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0');}},
						{field:'F6',	title:'総売価',		    					width:90,	halign:'center',align:'right',formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0');}},
						{field:'F7',	title:'発注日',	    						width:90,	halign:'center',align:'left'},
						{field:'F8',	title:'納入日',	    						width:90,	halign:'center',align:'left'},
						{field:'F9',	title:'管理番号',	    					width:70,	halign:'center',align:'left',formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '0000');}},
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
						var getRowIndex = data.total===0 ? '':$.getJSONValue(that.jsonHidden, "scrollToIndex_"+$.id.gridholder)*1;
						if (data.total !== 0 && (data.total-1) < getRowIndex) {
							getRowIndex = getRowIndex-1;
						}
						if(getRowIndex !== ""){
							$($.id.gridholder).datagrid('scrollTo', {
								index: getRowIndex,
								callback: function(index){
									$($.id.gridholder).datagrid('selectRow', index);
								}
							});
						}

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
			var sendMode = "";		// 遷移後に、前回検索条件を表示したい場合、""のまま、ここで設定した条件を表示したい場合"1"
			// タブ要素(a)取得
			var elems = $('#tabContent', window.parent.document).map(
				function(i,e) {
					return e;
				}).get();
			var href = elems[0].value.split(',');

			// JSON Object Clone ()
			var sendJSON = [];
			$.setJSONObject(sendJSON, 'sendBtnid', btnId, $('#'+btnId).attr("title"));					// 実行ボタン情報保持
			$.setJSONObject(sendJSON, 'reportYobi1', that.reportYobiInfo(), that.reportYobiInfo());		// 参照情報保持
			$.setJSONObject(sendJSON, 'callpage', that.name, that.name);								// 呼出し元レポート情報

			// 戻る実行時用に現在の画面情報を保持する
			var states = $.getBackBaseJSON(that);
			// 各種グリッド情報を設定
			var newrepinfos = $.getBackJSON(that, states);
			$.setJSONObject(sendJSON, 'repinfo', newrepinfos, '');

			// 実行ボタン別処理
			switch (btnId) {
			case $.id.btn_new:
				// 転送先情報
				index = 5;
				childurl = href[index];
				sendMode = 1;
				// オブジェクト作成
				that.getEasyUI();
				that.validation();
				var txt_moyscd		= $.getInputboxValue($('#'+$.id_inp.txt_moyscd));		// 催しコード
				var txt_moykn		= $.getInputboxValue($('#'+$.id_inp.txt_moykn));		// 催し名（漢字）
				var kikan_dummy		= $.getInputboxValue($('#'+'kikan_dummy'));				// 納入期間
				var txt_qasmdt		= $.getInputboxValue($('#'+$.id_inp.txt_qasmdt));		// 店舗締切日
				$.setJSONObject(sendJSON, $.id_inp.txt_moyscd,txt_moyscd, txt_moyscd);
				$.setJSONObject(sendJSON, $.id_inp.txt_moykn,txt_moykn, txt_moykn);
				$.setJSONObject(sendJSON, 'kikan_dummy',kikan_dummy, kikan_dummy);
				$.setJSONObject(sendJSON, $.id_inp.txt_qasmdt,txt_qasmdt, txt_qasmdt);
				$.setJSONObject(sendJSON, $.id_inp.txt_nnstdt,that.moycdData[0]["NNSTDT"], that.moycdData[0]["NNSTDT"]);
				$.setJSONObject(sendJSON, $.id_inp.txt_nneddt,that.moycdData[0]["NNEDDT"], that.moycdData[0]["NNEDDT"]);
				break;
			case $.id.btn_sel_change:
				// 選択行
				var row = $($.id.gridholder).datagrid("getSelected");
				if(!row){
					$.showMessage('E00008');
					return false;
				}
				// 転送先情報
				index = 6;
				childurl = href[index];
				sendMode = 1;
				// オブジェクト作成
				that.getEasyUI();
				that.validation();
				var txt_moyscd		= $.getInputboxValue($('#'+$.id_inp.txt_moyscd));		// 催しコード
				$.setJSONObject(sendJSON, $.id_inp.txt_moyscd,txt_moyscd, txt_moyscd);
				$.setJSONObject(sendJSON, $.id_inp.txt_kanrino,row.F9, row.F9);
				break;
			case $.id.btn_select:
				// 選択行
				var row = $($.id.gridholder).datagrid("getSelected");
				if(!row){
					$.showMessage('E00008');
					return false;
				}
				// 転送先情報
				var row = $($.id.gridholder).datagrid("getSelected");
				index = 6;
				childurl = href[index];
				sendMode = 1;
				that.getEasyUI();
				that.validation();
				var txt_moyscd		= $.getInputboxValue($('#'+$.id_inp.txt_moyscd));		// 催しコード
				$.setJSONObject(sendJSON, $.id_inp.txt_moyscd,txt_moyscd, txt_moyscd);
				$.setJSONObject(sendJSON, $.id_inp.txt_kanrino,row.F9, row.F9);
				$.setJSONObject(sendJSON, 'reportYobi1', that.reportYobiInfo(), that.reportYobiInfo());		// 参照情報保持
				break;
			case $.id.btn_back:
				// 転送先情報
				index = 1;
				if(that.reportYobiInfo()==='1'){
					index = 2;
				}
				sendMode = 2;
				childurl = href[index];
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