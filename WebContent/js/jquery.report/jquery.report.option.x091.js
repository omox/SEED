/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_Reportx091',			// （必須）レポートオプションの確認
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
		dedefaultObjNum:	3,	// 初期化オブジェクト数
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
		pushBtnId: "",						// （必須）実行ボタンID情報(検索系で利用)
		focusRootId:"cc",					// （キー移動時必須）キー移動イベントのルートとなるパネルのID
		focusParentId:"",					// （キー移動時必須）現在フォーカスがあたっている項目の親となるパネルのID
		focusGridId:"",						// （キー移動時必須）現在フォーカスがあたっているDataGridのID
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
			// 呼出コード
			$.setInputbox(that, reportno, $.id_inp.txt_callcd, false);
			// 商品コード
			$.setInputbox(that, reportno, $.id_inp.txt_shncd, false);
			// 部門
			$.setMeisyoCombo(that, reportno, $.id.SelBumon, false);

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
			var sendBtnid = $.getJSONValue(that.jsonHidden, "sendBtnid");
			if(sendBtnid && sendBtnid.length > 0){
				$.reg.search = true;
			}
			// 当帳票を「参照」で開いた場合
			if(that.reportYobiInfo()==='1'){
				$('#'+that.focusRootId).find('[id^=btn_]').not('[id^=btn_search]').each(function(){
					var that = this;
					$(that).linkbutton('disable');
					$(that).attr('disabled', 'disabled').hide();
				});
			}else{
				$($.id.buttons).show();
				// 各種遷移ボタン
				$('#'+$.id.btn_copy).on("click", $.pushChangeReport);
				$('#'+$.id.btn_new).on("click", $.pushChangeReport);
				$('#'+$.id.btn_sel_copy).on("click", $.pushChangeReport);
				$('#'+$.id.btn_sel_change).on("click", $.pushChangeReport);
			}
			$.initReportInfo("KK001", "値付器マスタ　一覧");
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
			var szSelBumon			= $.getJSONObject(this.jsonString, $.id.SelBumon).value;				// 部門
			var szTxtCallcd	= $.getJSONObject(this.jsonString, $.id_inp.txt_callcd).value;			// ナンバーボックス：呼び出しコード
			var szTxtShncd			= $.getJSONObject(this.jsonString, $.id_inp.txt_shncd).value;					// ナンバーボックス：商品コード

			if(!btnId) btnId = $.id.btn_search;

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			$($.id.gridholder).datagrid('loading');

			// grid.options 取得
			var options = $($.id.gridholder).datagrid('options');
			that.sortName	= options.sortName;
			that.sortOrder	= options.sortOrder;

			$.post(
				$.reg.jqgrid ,
				{
					report:				that.name,		// レポート名
					BTN:				btnId,
					BUMON:				szSelBumon,		// 部門
					TXT_CALLCD:			szTxtCallcd,		// 呼出コード
					TXT_SHNCD:			szTxtShncd,		// 商品コード
					t:					(new Date()).getTime(),
					sortable:			sortable,
					sortName:			that.sortName,
					sortOrder:			that.sortOrder,
					rows:				0	// 表示可能レコード数
				},
				function(json){
					// 検索処理エラー判定
					if($.searchError(json)) return false;

					// ログ出力
					$.log(that.timeData, 'query:');

					// Load処理回避
					$.tryChangeURL(null);

					if (sortable===0){
						var options = $($.id.gridholder).datagrid('options');
						// 初期検索時に並び替え情報のリセット
						options.sortName = null;
						options.sortOrder = null;
					}

					that.queried = true;
					that.pushBtnId = btnId;
					// Load処理回避
					$.tryChangeURL($.reg.jqeasy);

					// グリッド再描画（easyui 1.4.2 対応）
					$($.id.gridholder).datagrid('load', {} );
					$.removeMask();

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
				value:	$('#'+$.id.SelBumon).combobox('getValues'),
				text:	$('#'+$.id.SelBumon).combobox('getText')
			});
			// 呼出コード
			this.jsonTemp.push({
				id:		$.id_inp.txt_callcd,
				value:	$('#'+$.id_inp.txt_callcd).numberbox('getValue'),
				text:	$('#'+$.id_inp.txt_callcd).numberbox('getText')
			});
			// 商品コード
			this.jsonTemp.push({
				id:		$.id_inp.txt_shncd,
				value:	$('#'+$.id_inp.txt_shncd).numberbox('getValue'),
				text:	$('#'+$.id_inp.txt_shncd).numberbox('getText')
			});
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
				frozenColumns:[[]],
				columns:[[
						{field:'F1',	title:'部門コード',					width: 80,halign:'center',align:'left'},
						{field:'F2',	title:'呼出コード',					width: 80,halign:'center',align:'left'},
						{field:'F3',	title:'商品コード',					width: 100,halign:'center',align:'left'},
						{field:'F4',	title:'商品名（漢字）',				width: 250,halign:'center',align:'left'},
						{field:'F5',	title:'ラベル名　上段',				width: 300,halign:'center',align:'left'},
						{field:'F6',	title:'ラベル名　下段',				width: 300,halign:'center',align:'left'},
//						{field:'F5',	title:'商品名　上段',				width: 300,halign:'center',align:'left'},
//						{field:'F6',	title:'商品名　下段',				width: 300,halign:'center',align:'left'},
						{field:'F7',	title:'大分類',						width: 50,halign:'center',align:'left'},
						{field:'F8',	title:'中分類',						width: 50,halign:'center',align:'left'},
						{field:'F9',	title:'小分類',						width: 50,halign:'center',align:'left'},
						{field:'F10',	title:'入数',						width: 60,halign:'center',align:'right'},
						{field:'F11',	title:'生鮮・加工食品区分',			width: 120,halign:'center',align:'left'},
						{field:'F12',	title:'定貫・不定貫区分',			width: 120,halign:'center',align:'left'},
						{field:'F13',	title:'販売コード',					width: 120,halign:'center',align:'left'},
						{field:'F14',	title:'規格',						width: 120,halign:'center',align:'left'},
						{field:'F15',	title:'内容量',						width: 120,halign:'center',align:'left'},
						{field:'F16',	title:'消費期限・消費期限',			width: 120,halign:'center',align:'right'},
						{field:'F17',	title:'使用トレイ',					width: 120,halign:'center',align:'left'},
						{field:'F18',	title:'包装形態',					width: 120,halign:'center',align:'left'},
						{field:'F19',	title:'風袋',						width: 60,halign:'center',align:'right',formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0');}},
						{field:'F20',	title:'下限重量',					width: 80,halign:'center',align:'right',formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0');}},
						{field:'F21',	title:'上限重量',					width: 80,halign:'center',align:'right',formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0');}}
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
					// 検索後、初回のみ処理
					if (that.queried){
						that.queried = false;	// 検索後、初回のみ処理
						// 状態保存
						$.saveState(reportNumber, that.getJSONString(), id);
						// 警告
						$.showWarningMessage(data);

						if($.getJSONValue(that.jsonHidden, "scrollToIndex_"+id) == ""){
							$.setJSONObject(that.jsonHidden, "scrollToIndex_"+id, 0, 0);
						}
					}

					// 前回選択情報をGridに反映
					var getRowIndex = data.total===0 ? '':$.getJSONValue(that.jsonHidden, "scrollToIndex_"+id);
					if (data.total !== 0 && (data.total-1) < getRowIndex) {
						getRowIndex = getRowIndex-1;
					}
					if(getRowIndex !== ""){
						$(id).datagrid('scrollTo', {
							index: getRowIndex,
							callback: function(index){
								$(id).datagrid('selectRow', index);
							}
						});

						// 検索可能な一覧画面では、検索時に前回選択情報が適用されてしまう為、
						// 一度適用したら要素を削除する。
						var targetName = "scrollToIndex_"+id;
						that.jsonHidden.some(function(v, i){
						    if (v.id==targetName) that.jsonHidden.splice(i,1);
						});
					}
				},
				onClickCell:function(rowIndex, field, value){
					// 列名保持
					that.columnName = field;
				},
				onClickRow:function(rowIndex, rowData){
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

			// タブ要素(a)取得
			var elems = $('#tabContent', window.parent.document).map(
				function(i,e) {
					return e;
				}).get();
			var href = elems[0].value.split(',');

			// JSON Object Clone ()
			var sendJSON = [];
			$.setJSONObject(sendJSON, 'sendBtnid', btnId, $('#'+btnId).attr("title"));		// 実行ボタン情報保持

			// 戻る実行時用に現在の画面情報を保持する
			var states = $.getBackBaseJSON(that);
			// TODO 各種グリッド情報を設定
			var newrepinfos = $.getBackJSON(that, states,true);
			$.setJSONObject(sendJSON, 'repinfo', newrepinfos, '');

			// 選択行
			var row = $($.id.gridholder).datagrid("getSelected");

			// 実行ボタン別処理
			switch (btnId) {
			case $.id.btn_new:
				// 転送先情報
				index = 2;
				childurl = href[index];

				// オブジェクト作成
				$.setJSONObject(sendJSON, $.id.txt_sel_bmncd,'', '');
				$.setJSONObject(sendJSON, $.id_inp.txt_callcd,'', '');
				$.setJSONObject(sendJSON, $.id_inp.txt_shncd,'', '');
				break;
			case $.id.btn_copy:
			case $.id.btn_sel_copy:
			case $.id.btn_sel_change:
				if(!row){
					$.showMessage('E00008');
					return false;
				}

				// 転送先情報
				index = 2;
				childurl = href[index];

				// オブジェクト作成
				$.setJSONObject(sendJSON, $.id_inp.txt_bmncd,row.F1, row.F1);
				$.setJSONObject(sendJSON, $.id_inp.txt_callcd, row.F2, row.F2);
				$.setJSONObject(sendJSON, $.id_inp.txt_shncd, row.F22, row.F22);
				break;
			case $.id.btn_back:
				// 転送先情報
				childurl = parent.$('#hdn_menu_path').val();

				break;
			default:
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