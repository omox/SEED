/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_Reportx003',			// （必須）レポートオプションの確認
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
		dedefaultObjNum:	9,	// 初期化オブジェクト数
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
		pushBtnid: "",						// （必須）実行ボタンID情報(検索系で利用)
		focusRootId:"cc",					// （キー移動時必須）キー移動イベントのルートとなるパネルのID
		focusParentId:"",					// （キー移動時必須）現在フォーカスがあたっている項目の親となるパネルのID
		focusGridId:"",						// （キー移動時必須）現在フォーカスがあたっているDataGridのID
		initialize: function (reportno){	// （必須）初期化
			var that = this;
			// 画面の初回基本設定
			this.setInitObjectState();

			// 引き継ぎ情報
			this.jsonHidden = $.getTargetValue();
			// 初期検索条件設定
			this.jsonInit = $.getInitValue();
			// データ表示エリア初期化
			that.setGrid($.id.gridholder, reportno);

			// 初期化するオブジェクト数設定
			this.initObjNum = this.dedefaultObjNum;

			// コード選択
			$.setRadioInit(that.jsonHidden, $.id.rad_code, that);
			// コード1－8
			for (var i=1; i<=8; i++){
				$.setInputbox(that, reportno, $.id.txt_code+i, false);
			}

			// ラジオボタン系
			that.setRadio(that, $.id.rad_code);

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
			$.initReportInfo("SJ002", "商品情報照会　検索");
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

			var szRadCode		= $.getJSONObject(this.jsonTemp, $.id.rad_code).value;				// ラジオボタン：コード
			var szTxtCode1		= $.getJSONObject(this.jsonTemp, $.id.txt_code+1).value;			// テキストボックス：コード
			var szTxtCode2		= $.getJSONObject(this.jsonTemp, $.id.txt_code+2).value;			// テキストボックス：コード
			var szTxtCode3		= $.getJSONObject(this.jsonTemp, $.id.txt_code+3).value;			// テキストボックス：コード
			var szTxtCode4		= $.getJSONObject(this.jsonTemp, $.id.txt_code+4).value;			// テキストボックス：コード
			var szTxtCode5		= $.getJSONObject(this.jsonTemp, $.id.txt_code+5).value;			// テキストボックス：コード
			var szTxtCode6		= $.getJSONObject(this.jsonTemp, $.id.txt_code+6).value;			// テキストボックス：コード
			var szTxtCode7		= $.getJSONObject(this.jsonTemp, $.id.txt_code+7).value;			// テキストボックス：コード
			var szTxtCode8		= $.getJSONObject(this.jsonTemp, $.id.txt_code+8).value;			// テキストボックス：コード

			if(rt){
				if(szTxtCode1.length === 0 && szTxtCode2.length === 0 && szTxtCode3.length === 0 && szTxtCode4.length === 0 && szTxtCode5.length === 0 && szTxtCode6.length === 0 && szTxtCode7.length === 0 && szTxtCode8.length === 0 ){
					$.showMessage('EX1020');
					rt = false;
				}
			}

			// 販売コード選択時エラーチェック
			if(rt){
				if(szRadCode == 3){
					var ErrCode = ""

					if(szTxtCode1.length > 6){
						ErrCode = szTxtCode1;

					}else if(szTxtCode2.length > 6){
						ErrCode = szTxtCode2;

					}else if(szTxtCode3.length > 6){
						ErrCode = szTxtCode3;

					}else if(szTxtCode4.length > 6){
						ErrCode = szTxtCode4;

					}else if(szTxtCode5.length > 6){
						ErrCode = szTxtCode5;

					}else if(szTxtCode6.length > 6){
						ErrCode = szTxtCode6;

					}else if(szTxtCode7.length > 6){
						ErrCode = szTxtCode7;

					}else if(szTxtCode8.length > 6){
						ErrCode = szTxtCode8;
					}

					if(ErrCode != ""){
						// 6桁以上の入力値がある場合はエラーとする
						$.showMessage('E11302', [ErrCode + " 入力値の桁数", "。6桁で入力してください。"]);
						rt = false;
					}

					if(szTxtCode1.length > 6 && szTxtCode2.length > 6 && szTxtCode3.length > 6 && szTxtCode4.length > 6 && szTxtCode5.length > 6 && szTxtCode6.length > 6 && szTxtCode7.length > 6 && szTxtCode8.length > 6 ){
						$.showMessage('E11302', []);
						rt = false;
					}
				}
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
			var szRadCode		= $.getJSONObject(this.jsonString, $.id.rad_code).value;			// ラジオボタン：コード
			var szTxtCode1		= $.getJSONObject(this.jsonString, $.id.txt_code+1).value;			// テキストボックス：コード
			var szTxtCode2		= $.getJSONObject(this.jsonString, $.id.txt_code+2).value;			// テキストボックス：コード
			var szTxtCode3		= $.getJSONObject(this.jsonString, $.id.txt_code+3).value;			// テキストボックス：コード
			var szTxtCode4		= $.getJSONObject(this.jsonString, $.id.txt_code+4).value;			// テキストボックス：コード
			var szTxtCode5		= $.getJSONObject(this.jsonString, $.id.txt_code+5).value;			// テキストボックス：コード
			var szTxtCode6		= $.getJSONObject(this.jsonString, $.id.txt_code+6).value;			// テキストボックス：コード
			var szTxtCode7		= $.getJSONObject(this.jsonString, $.id.txt_code+7).value;			// テキストボックス：コード
			var szTxtCode8		= $.getJSONObject(this.jsonString, $.id.txt_code+8).value;			// テキストボックス：コード

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
					report:			that.name,		// レポート名
					BTN:			btnId,
					RAD_CODE:		szRadCode,		// コードタイプ
					TXT_CODE1:		szTxtCode1,		// コード1
					TXT_CODE2:		szTxtCode2,		// コード2
					TXT_CODE3:		szTxtCode3,		// コード3
					TXT_CODE4:		szTxtCode4,		// コード4
					TXT_CODE5:		szTxtCode5,		// コード5
					TXT_CODE6:		szTxtCode6,		// コード6
					TXT_CODE7:		szTxtCode7,		// コード7
					TXT_CODE8:		szTxtCode8,		// コード8
					t:				(new Date()).getTime(),
					sortable:		sortable,
					sortName:		that.sortName,
					sortOrder:		that.sortOrder,
					rows:			0	// 表示可能レコード数
				},
				function(json){
					// 検索処理エラー判定
					if($.searchError(json, undefined, that)) return false;

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
					that.pushBtnid = btnId;
					// Load処理回避
					$.tryChangeURL($.reg.jqeasy);

					// グリッド再描画（easyui 1.4.2 対応）
					$($.id.gridholder).datagrid('load', {} );
					$.removeMask();

/*					// 検索ボタン無効化
					$.setButtonState('#'+$.id.btn_search, false, 'success');
*/
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

			// 区分
			this.jsonTemp.push({
				id:		$.id.rad_code,
				value:	$('input[name="'+$.id.rad_code+'"]:checked').val(),
				text:	$.trim($('input[name="'+$.id.rad_code+'"]:checked').parent().text())
			});

			// コード1－8
			for (var i=1; i<=8; i++){
				this.jsonTemp.push({
					id:		$.id.txt_code+i,
					value:	$('#'+$.id.txt_code+i).numberbox('getValue'),
					text:	$('#'+$.id.txt_code+i).numberbox('getText')
				});
			}
		},
		setObjectState: function(){	// 軸の選択内容による制御

		},
		setRadio: function(reportno, name){
			var id = name;
			var maxlen = 0;
			$('input[name="'+id+'"]').change(function() {
				if ($(this).val() === '1') {
					maxlen = 8;
				} else if ($(this).val() === '2') {
					maxlen = 13;
				} else {
					maxlen = 6;
				}

				for (var i = 0; i < 8; i++) {
					$.setInputboxValue($('#'+$.id.txt_code+(i+1)),'');
					$('#'+$.id.txt_code+(i+1)).numberbox('textbox').validatebox('options').validType = 'intMaxLen['+maxlen+']';
				}
			});
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
				rownumbers:false,
				fit:true,
				view:scrollview,
				pageSize:pageSize,
				pageList:pageList,
				frozenColumns:[[
					{field:'F1',	title:'　',					width: 30,halign:'center',align:'center'},
					{field:'F2',	title:'商品コード',			width: 80,halign:'center',align:'left'},
				]],
				columns:[[
					{field:'F3',	title:'ソースコード1<br>ソースコード2',	width: 120,halign:'center',align:'left'},
					{field:'F4',	title:'販売コード',			width: 70,halign:'center',align:'left'},
					{field:'F5',	title:'親コード',			width: 80,halign:'center',align:'left'},
					{field:'F6',	title:'商品名',				width:300,halign:'center',align:'left'},
					{field:'F7',	title:'扱区',			width: 70,halign:'center',align:'left'},
					{field:'F8',	title:'　',					width: 40,halign:'center',align:'left'},
					{field:'F9',	title:'原価',				width: 70,halign:'center',align:'right',
						formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0.00');}},
					{field:'F10',	title:'本体売価',			width: 70,halign:'center',align:'right',
						formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0');}},
					{field:'F11',	title:'総額売価',			width: 70,halign:'center',align:'right',
						formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0');}},
					{field:'F12',	title:'店入数',				width: 70,halign:'center',align:'right',
						formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0');}},
					{field:'F13',	title:'ワッペン',			width: 40,halign:'center',align:'left'},
					{field:'F14',	title:'　',					width: 40,halign:'center',align:'left'},
					{field:'F15',	title:'標準仕入先',			width: 70,halign:'center',align:'left'},
					{field:'F16',	title:'分類コード',			width: 80,halign:'center',align:'left'},
					{field:'F17',	title:'更新日',				width: 80,halign:'center',align:'left'}
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
			var sendJSON = JSON.parse( JSON.stringify( that.jsonString ) );
			$.setJSONObject(sendJSON, 'sendBtnid', btnId, $('#'+btnId).attr("title"));					// 実行ボタン情報保持
			$.setJSONObject(sendJSON, 'reportYobi1', that.reportYobiInfo(), that.reportYobiInfo());		// 参照情報保持
			$.setJSONObject(sendJSON, 'callpage', that.name, that.name);								// 呼出し元レポート情報

			// 戻る実行時用に現在の画面情報を保持する
			var states = $.getBackBaseJSON(that);
			var newrepinfos = $.getBackJSON(that, states, true);
			$.setJSONObject(sendJSON, 'repinfo', newrepinfos, '');

			// 実行ボタン別処理
			switch (btnId) {
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