/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_ReportMM001',			// （必須）レポートオプションの確認
		jsonTemp:	[],							// （必須）検索条件情報_入力チェック前
		jsonString:	[],							// （必須）検索条件情報
		jsonHidden: [],							// （必須）親画面からの引き継ぎ情報
		jsonInit: [],							// （必須）検索条件初期情報
		caption: function(){					// （必須）タイトル
			return $('#reportname').val();
		},
		sortName: '',							// ソート項目名
		sortOrder: '',							// ソート順
		timeData : (new Date()).getTime(),
		dedefaultObjNum:	7,					// 初期化オブジェクト数
		initObjNum:	-1,
		initedObject: [],
		maxMergeCell: 1,
		onChangeFlag : false,
		onChangeFlag2 : false,
		columnName:'',	// OnClickRowの列名
		queried : false,
		initializes : true,
		onChangeReport: false,
		reportYobiInfo: function(){				// （必須）レポートメニューに登録された予備情報＝帳票をどういった表示にしたいかの情報
			return $('#reportYobi1').val();
		},
		focusRootId:"cc",						// （キー移動時必須）キー移動イベントのルートとなるパネルのID
		focusParentId:"",						// （キー移動時必須）現在フォーカスがあたっている項目の親となるパネルのID
		focusGridId:"",							// （キー移動時必須）現在フォーカスがあたっているDataGridのID
		initRow:"",
		initialize: function (reportno){		// （必須）初期化
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

			// 編集可能データグリッドの共通処理設定
			// 編集エディターの機能拡張（非表示で参照用のオブジェクトが準備してある前提、参照オブジェクトはグリッドのフィールド名を元に判断）
//			$.extendDatagridEditor();

			// 初期検索可能
//			that.onChangeReport = true;

			var count = 2;
			// 名称マスタ参照系
			var meisyoSelect = Object.getOwnPropertyNames($.id_mei);
			for ( var sel in meisyoSelect ) {
				if($('#'+$.id_mei[meisyoSelect[sel]]).length > 0){
					$.setMeisyoCombo(that, reportno, $.id_mei[meisyoSelect[sel]], isUpdateReport);
					count++;
				}
			}
			// 入力テキストボックス系
			var inputbox = Object.getOwnPropertyNames($.id_inp);
			for ( var sel in inputbox ) {
				if($('#'+$.id_inp[inputbox[sel]]).length > 0){
					$.setInputbox(that, reportno, $.id_inp[inputbox[sel]], isUpdateReport);
					count++;
				}
			}

			// 部門
			$.setMeisyoCombo(that, reportno, $.id.SelBumon, isUpdateReport);

			// 初期化終了
			this.initializes =! this.initializes;

			//$.initialSearch(that);

//			// チェックボックスの設定
//			$.initCheckboxCss($("#"+that.focusRootId));
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
//			var that = this;
//			var sendBtnid = $.getJSONValue(that.jsonHidden, "sendBtnid");
//			if(!sendBtnid){
//				sendBtnid = $('#sendBtnid').val();
//			}
//			$('#sendBtnid').val(sendBtnid);
//			that.sendBtnid = sendBtnid;
//
//			var reportYobi1 = $.getJSONValue(that.jsonHidden, "reportYobi1");
//			if(!reportYobi1){
//				reportYobi1 = $('#reportYobi1').val();
//			}
//			$('#reportYobi1').val(reportYobi1);
//
//			var sendBtnid = $.getJSONValue(that.jsonHidden, "sendBtnid");
//			if(sendBtnid && sendBtnid.length > 0){
//				$.reg.search = true;
//			}


			// 各種遷移ボタン
			$('#'+$.id.btn_tennoview).on("click", $.pushChangeReport);	// 店番表示

			// 当帳票を「参照」で開いた場合
			if(that.reportYobiInfo()==='1'){
				$('#'+$.id.btn_select).on("click", $.pushChangeReport);
				$.initReportInfo("MM001", "催し検索　参照　催し一覧");
			}else{
				// 各種遷移ボタン
				$('#'+$.id.btn_select).on("click", $.pushChangeReport);
				$.initReportInfo("MM001", "催し検索　変更　催し一覧");
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
		validation: function (){	// （必須）批准
			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform).form('validate');

			var that = this;
			var txt_shncd		= $('#'+$.id_inp.txt_shncd).textbox('getValue')							// 商品コード
			var SelBumon		= $('#'+$.id.SelBumon).combobox('getValue')								// 部門
			var txt_tencd		= $('#'+$.id_inp.txt_tencd).textbox('getValue')							// 店コード
			var kbn10002		= $('#'+$.id_mei.kbn10002).combobox('getValue')							// 催し区分
			var txt_hbstdt		= $('#'+$.id_inp.txt_hbstdt).textbox('getValue')						// 販売開始日
			var txt_hbeddt		= $('#'+$.id_inp.txt_hbeddt).textbox('getValue')						// 販売終了日
			var txt_nnstdt		= $('#'+$.id_inp.txt_nnstdt).textbox('getValue')						// 納入開始日
			var txt_nneddt		= $('#'+$.id_inp.txt_nneddt).textbox('getValue')						// 納入終了日

			// 入力チェック
			if (rt) {
				if (!txt_shncd) {			// 商品コード=空白
					if (!txt_tencd) {
//						$.showMessage('EX1036', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_tencd), true)});
						$.showMessage('EX1047', ["商品コード、店コードのいずれか"], function(){});
						$.addErrState(that, $('#'+$.id_inp.txt_shncd), true)
						$.addErrState(that, $('#'+$.id_inp.txt_tencd), true)
						rt = false;
					}
					if (rt) {
						if (SelBumon === "-1") {
							$.showMessage('E20037', undefined, function(){$.addErrState(that, $('#'+$.id.SelBumon), true)});
							rt = false;
						}
					}
				}
			}
			if (rt) {
				if (txt_shncd) {			// 商品コード<>空白
					if (SelBumon != "-1") {
//						$.showMessage('E20099', undefined, function(){$.addErrState(that, $('#'+$.id.SelBumon), true)});
						$.showMessage('E20099');
						$.addErrState(that, $('#'+$.id_inp.txt_shncd), true);
						$.addErrState(that, $('#'+$.id.SelBumon), true);
						rt = false;
					}
				}
			}
			if (rt) {
				if (!kbn10002) {	// 必須チェック
					$.showMessage('E00001', undefined, function(){$.addErrState(that, $('#'+$.id.kbn10002), true)});
					rt = false;
				}
			}
			if (rt) {
				if (!txt_hbstdt && txt_hbeddt) {
					$.showMessage('EX1035', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_hbstdt), true)});
					rt = false;
				}
			}
			if (rt) {
				if (txt_hbstdt && txt_hbeddt) {
					if (txt_hbstdt > txt_hbeddt) {
//						$.showMessage('E20006', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_hbstdt), true)});
						$.showMessage('E20006');
						$.addErrState(that, $('#'+$.id_inp.txt_hbstdt), true);
						$.addErrState(that, $('#'+$.id_inp.txt_hbeddt), true);
						rt = false;
					}
				}
			}
			if (rt) {
				if (!txt_nnstdt && txt_nneddt) {
					$.showMessage('E11311', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_nnstdt), true)});
					rt = false;
				}
			}
			if (rt) {
				if (txt_nnstdt && txt_nneddt) {
					if (txt_nnstdt > txt_nneddt) {
						$.showMessage('E20574');
						$.addErrState(that, $('#'+$.id_inp.txt_nnstdt), true);
						$.addErrState(that, $('#'+$.id_inp.txt_nneddt), true);
						return false;
					}
				}
			}
			// 入力エラーなしの場合に検索条件を格納
			if (rt == true) that.jsonString = that.jsonTemp.slice(0);
			// 入力チェック用の配列をクリア
			that.jsonTemp = [];
			return rt;
		},
		success: function(reportno, sortable){	// （必須）正処理
			if (sortable) sortable=1; else sortable=0;
			var that = this;

			var txt_shncd		= $.getJSONObject(that.jsonString, $.id_inp.txt_shncd).value;			// 商品コード
			var txt_tencd		= $.getJSONObject(that.jsonString, $.id_inp.txt_tencd).value;			// 店コード
			var txt_hbstdt		= $.getJSONObject(that.jsonString, $.id_inp.txt_hbstdt).value;			// 販売開始日
			var txt_hbeddt		= $.getJSONObject(that.jsonString, $.id_inp.txt_hbeddt).value;			// 販売終了日
			var txt_nnstdt		= $.getJSONObject(that.jsonString, $.id_inp.txt_nnstdt).value;			// 納入開始日
			var txt_nneddt		= $.getJSONObject(that.jsonString, $.id_inp.txt_nneddt).value;			// 納入終了日
			var sel_moyskbn		= $.getJSONObject(that.jsonString, $.id_mei.kbn10002).value;			// 催し区分
			var sel_bumon		= $.getJSONObject(that.jsonString, $.id.SelBumon).value;				// 部門

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
					SHNCD:			txt_shncd,		// 商品コード
					TENCD:			txt_tencd,		// 店コード
					HBSTDT:			txt_hbstdt,		// 販売開始日
					HBEDDT:			txt_hbeddt,		// 販売終了日
					NNSTDT:			txt_nnstdt,		// 納入開始日
					NNEDDT:			txt_nneddt,		// 納入終了日
					MOYSKBN			:sel_moyskbn,	// 催し区分
					BUMON			:sel_bumon,		// 部門
					t:				(new Date()).getTime(),
					sortable:		sortable,
					sortName:		that.sortName,
					sortOrder:		that.sortOrder,
					rows:			0	// 表示可能レコード数
				},
				function(json){
					// 検索処理エラー判定
					if($.searchError(json, undefined, that)) return false;
					var jsonp = JSON.parse(json);
					if(jsonp.total == null || jsonp.total === 0){
						$.showMessage('E11003');
					}

					// ログ出力
					$.log(that.timeData, 'query:');

					// Load処理回避
					$.tryChangeURL(null);

					that.initRow=0;

					if (sortable===0){
						var options = $($.id.gridholder).datagrid('options');
						// 初期検索時に並び替え情報のリセット
						options.sortName = null;
						options.sortOrder = null;
					}

					that.queried = true;
					// Load処理回避
					$.tryChangeURL($.reg.jqeasy);

					// グリッド再描画（easyui 1.4.2 対応）
					$($.id.gridholder).datagrid('load', {} );
					$.removeMask();

					// 商品コード未入力の場合、店番表示ボタン非活性
					var txt_shncd		= $.getInputboxValue($('#'+$.id_inp.txt_shncd));
					if (!txt_shncd) {
						$("#"+$.id.btn_tennoview).linkbutton('disable');
					} else {
						$("#"+$.id.btn_tennoview).linkbutton('enable');
					}

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
			// 商品コード
			this.jsonTemp.push({
				id:		$.id_inp.txt_shncd,
				value:	$('#'+$.id_inp.txt_shncd).textbox('getValue'),
				text:	$('#'+$.id_inp.txt_shncd).textbox('getText')
			});
			// 店コード
			this.jsonTemp.push({
				id:		$.id_inp.txt_tencd,
				value:	$('#'+$.id_inp.txt_tencd).textbox('getValue'),
				text:	$('#'+$.id_inp.txt_tencd).textbox('getText')
			});
			// 販売開始日
			this.jsonTemp.push({
				id:		$.id_inp.txt_hbstdt,
				value:	$('#'+$.id_inp.txt_hbstdt).textbox('getValue'),
				text:	$('#'+$.id_inp.txt_hbstdt).textbox('getText')
			});
			// 販売終了日
			this.jsonTemp.push({
				id:		$.id_inp.txt_hbeddt,
				value:	$('#'+$.id_inp.txt_hbeddt).textbox('getValue'),
				text:	$('#'+$.id_inp.txt_hbeddt).textbox('getText')
			});
			// 納入開始日
			this.jsonTemp.push({
				id:		$.id_inp.txt_nnstdt,
				value:	$('#'+$.id_inp.txt_nnstdt).textbox('getValue'),
				text:	$('#'+$.id_inp.txt_nnstdt).textbox('getText')
			});
			// 納入終了日
			this.jsonTemp.push({
				id:		$.id_inp.txt_nneddt,
				value:	$('#'+$.id_inp.txt_nneddt).textbox('getValue'),
				text:	$('#'+$.id_inp.txt_nneddt).textbox('getText')
			});
			// 催し区分
			this.jsonTemp.push({
				id:		$.id_mei.kbn10002,
				value:	$('#'+$.id_mei.kbn10002).combobox('getValue'),
				text:	$('#'+$.id_mei.kbn10002).combobox('getText')
			});
			// 部門
			this.jsonTemp.push({
				id:		$.id.SelBumon,
				value:	$('#'+$.id.SelBumon).combobox('getValue'),
				text:	$('#'+$.id.SelBumon).combobox('getText')
			});
		},
		setGrid: function (id, reportNumber){	// グリッドの構築
			var that = this;
			var init = true;
			var columns = that.getGridColumns(that, id);
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
				frozenColumns:[[]],
				columns:columns,
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

					// 初期表示時処理
					if (getRowIndex==="" && data.total !== 0) {
						getRowIndex = that.initRow;
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

						that.initRow="";
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
		getGridColumns:function(that, id){
			var columns = [];
			var columnBottom=[];
			columnBottom.push({field:'F1',	title:'催しコード',			width: 110,halign:'center',align:'left',
				formatter:function(value,row,index){
					return $.getFormatPrompt(value, '#-######-###');
				}
			});
			columnBottom.push({field:'F2',	title:'催し名称',			width: 500 ,halign:'center',align:'left'});
			columnBottom.push({field:'F3',	title:'販売期間',			width: 200 ,halign:'center',align:'left'});
			columnBottom.push({field:'F4',	title:'納入期間',			width: 200 ,halign:'center',align:'left'});
			columnBottom.push({field:'F5',	title:'管理番号',			width: 100 ,halign:'center',align:'left',
				formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '0000');}});
			columnBottom.push({field:'F6',	title:'B/M',				width: 60 ,halign:'center',align:'center'});
			columnBottom.push({field:'F7',	title:'BM番号',		hidden:true});
			columnBottom.push({field:'F8',	title:'登録種別',	hidden:true});
			columnBottom.push({field:'F9',	title:'催し区分',	hidden:true});
			columnBottom.push({field:'F10',	title:'催し開始日',	hidden:true});
			columnBottom.push({field:'F11',	title:'催し連番',	hidden:true});
			columnBottom.push({field:'F12',	title:'部門',	hidden:true});
			columns.push(columnBottom);
			return columns;

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
		repgrpInfo: {
			MM001:{idx:1},		// 催し検索 催し一覧 変更
			MM001_1:{idx:2},	// 催し検索 催し一覧 参照
			MM002:{idx:3},		// 催し検索 商品一覧
			MM003:{idx:4},		// 催し検索 店舗一覧
			TG016:{idx:5},		// 月間販売計画(チラシ計画) 商品情報
			BM006:{idx:6},		// 催し別送信情報 B/M別 変更/参照 明細
//			KS004:{idx:7},		// 個店特売承認 商品情報 修正 (ドライ)
//			KS005:{idx:8},		// 個店特売承認 商品情報 修正 (コード無し)
//			KS006:{idx:9},		// 個店特売承認 商品情報 修正 (青果)
//			KS007:{idx:10},		// 個店特売承認 商品情報 修正 (鮮魚)
//			KS008:{idx:11},		// 個店特売承認 商品情報 修正 (精肉)
//			KS009:{idx:12},		// 個店特売承認 商品情報 修正 (塩干)
//			KS010:{idx:13},		// 個店特売承認 商品情報 修正 (全品割引)
//			KS011:{idx:14},		// 個店特売承認 商品情報 修正 (原材料)
			SO003:{idx:7},		// 生活応援 新規・変更/参照 商品一覧
//			GY003:{idx:16},		// 月間山積 新規・変更/参照 商品一覧
			JU033:{idx:8},		// 店舗アンケート付き送付け 変更/参照 商品情報
			JU013:{idx:9},		// 事前打出し 変更/参照 商品情報
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
			var sendJSON = [];
			$.setJSONObject(sendJSON, 'sendBtnid', btnId, $('#'+btnId).attr("title"));					// 実行ボタン情報保持
			$.setJSONObject(sendJSON, 'reportYobi1', that.reportYobiInfo(), that.reportYobiInfo());		// 参照情報保持
			$.setJSONObject(sendJSON, 'callpage', that.name, that.name);								// 呼出し元レポート情報

			// 戻る実行時用に現在の画面情報を保持する
			var states = $.getBackBaseJSON(that);
			var newrepinfos = $.getBackJSON(that, states, true);
			$.setJSONObject(sendJSON, 'repinfo', newrepinfos, '');

			// 選択行
			var row = $($.id.gridholder).datagrid("getSelected");

			// 実行ボタン別処理
			switch (btnId) {
			case $.id.btn_tennoview:																	// 店番表示ボタン
				sendMode = 1;
				if(!row){
					$.showMessage('E00008');
					return false;
				}

				// 転送先情報
				index = 4;																				// MM003 催し検索 店舗一覧
//				index = that.repgrpInfo.MM003.idx;
				childurl = href[index];

				if (row.F6==""){
					var txt_bmflg = 0;
				} else {
					var txt_bmflg = 1;
				}
				var txt_shncd		= $.getInputboxValue($('#'+$.id_inp.txt_shncd));					// 商品コード
				// オブジェクト作成
				$.setJSONObject(sendJSON, $.id_inp.txt_moyscd, row.F1, row.F1);							// 催しコード
//				$.setJSONObject(sendJSON, $.id_inp.txt_moyskbn, row.F9, row.F9);						// 催し区分
//				$.setJSONObject(sendJSON, $.id_inp.txt_moysstdt, row.F10, row.F10);						// 催し開始日
//				$.setJSONObject(sendJSON, $.id_inp.txt_moysrban, row.F11, row.F11);						// 催し連番
				$.setJSONObject(sendJSON, $.id_inp.txt_kanrino, row.F5, row.F5);						// 管理番号
				$.setJSONObject(sendJSON, $.id_inp.txt_bmflg, txt_bmflg, txt_bmflg);					// B/Mフラグ
				$.setJSONObject(sendJSON, $.id_inp.txt_bmnno, row.F7, row.F7);							// B/M番号
				$.setJSONObject(sendJSON, $.id_inp.txt_shncd, txt_shncd, txt_shncd);					// 商品コード
				break;
			case $.id.btn_select:// 選択
				sendMode = 1;
				if(!row){
					$.showMessage('E00008');
					return false;
				}
				var txt_shncd		= $.getInputboxValue($('#'+$.id_inp.txt_shncd));					// 商品コード
				if (txt_shncd == "") {
					// 転送先情報
					index = 3;																			// MM002 催し検索 変更 商品一覧
//					index = that.repgrpInfo.MM002.idx;
					childurl = href[index];
					var txt_tencd		= $.getInputboxValue($('#'+$.id_inp.txt_tencd));				// 店コード
					var selBumon		= $('#'+$.id.SelBumon).combogrid('getValue');					// 部門
					if (row.F6==""){
						var txt_bmflg = "0";
					} else {
						var txt_bmflg = "1";
					}
					// オブジェクト作成
					$.setJSONObject(sendJSON, $.id_inp.txt_tencd, txt_tencd, txt_tencd);				// 店コード
					$.setJSONObject(sendJSON, $.id_inp.txt_bmncd, selBumon, selBumon);					// 部門
					$.setJSONObject(sendJSON, $.id_inp.txt_moyscd, row.F1, row.F1);						// 催しコード
//					$.setJSONObject(sendJSON, $.id_inp.txt_moyskbn, row.F9, row.F9);					// 催し区分
//					$.setJSONObject(sendJSON, $.id_inp.txt_moysstdt, row.F10, row.F10);					// 催し開始日
//					$.setJSONObject(sendJSON, $.id_inp.txt_moysrban, row.F11, row.F11);					// 催し連番
					$.setJSONObject(sendJSON, $.id_inp.txt_bmflg, txt_bmflg, txt_bmflg);				// B/Mフラグ
					$.setJSONObject(sendJSON, $.id_inp.txt_bmnno, row.F7, row.F7);						// B/M番号
					break;
				}

				var txt_shncd		= $.getInputboxValue($('#'+$.id_inp.txt_shncd));					// 商品コード
				var sel_moyskbn		= row.F9;															// 催し区分
				var selBumon		= $('#'+$.id.SelBumon).combogrid('getValue');						// 部門

				if (sel_moyskbn=="0" ||
					((sel_moyskbn=="1" || sel_moyskbn=="2" || sel_moyskbn=="3") && row.F6=="")) {
					// 転送先情報
					index = that.repgrpInfo.TG016.idx;
					childurl = href[index];
					if (row.F6==""){
						var txt_bmflg = "0";
					} else {
						var txt_bmflg = "1";
					}
					// オブジェクト作成
					$.setJSONObject(sendJSON, $.id_inp.txt_shncd, txt_shncd, txt_shncd);				// 商品コード
					$.setJSONObject(sendJSON, $.id_inp.txt_bmncd, row.F12, row.F12);					// 部門
					$.setJSONObject(sendJSON, $.id_inp.txt_moyskbn, row.F9, row.F9);					// 催し区分
					$.setJSONObject(sendJSON, $.id_inp.txt_moysstdt, row.F10, row.F10);					// 催し開始日
					$.setJSONObject(sendJSON, $.id_inp.txt_moysrban, row.F11, row.F11);					// 催し連番
					$.setJSONObject(sendJSON, $.id_inp.txt_kanrino, row.F5, row.F5);					// 管理番号
					$.setJSONObject(sendJSON, $.id_inp.txt_kanrieno, '0', '0');							// 枝番 TODO:仕様確認待ちの為、ひとまず0固定
					$.setJSONObject(sendJSON, $.id_inp.txt_addshukbn, row.F8, row.F8);					// 登録種別
					$.setJSONObject(sendJSON, $.id_inp.txt_bmflg, txt_bmflg, txt_bmflg);				// B/Mフラグ
					$.setJSONObject(sendJSON, $.id_inp.txt_bmnno, row.F7, row.F7);						// B/M番号
					break;
				} else if ((sel_moyskbn=="1" || sel_moyskbn=="2" || sel_moyskbn=="3") && row.F6!="") {
					// 転送先情報
					index = that.repgrpInfo.BM006.idx;
					childurl = href[index];
					// オブジェクト作成
//					$.setJSONObject(sendJSON, $.id_inp.txt_moyscd, row.F1, row.F1);						// 催しコード
					$.setJSONObject(sendJSON, $.id_inp.txt_moyskbn, row.F9, row.F9);					// 催し区分
					$.setJSONObject(sendJSON, $.id_inp.txt_moysstdt, row.F10, row.F10);					// 催し開始日
					$.setJSONObject(sendJSON, $.id_inp.txt_moysrban, row.F11, row.F11);					// 催し連番
					$.setJSONObject(sendJSON, $.id_inp.txt_bmnno, row.F7, row.F7);						// B/M番号
					$.setJSONObject(sendJSON, $.id_inp.txt_kanrino, row.F5, row.F5);					// 管理番号
					break;
//				} else if (sel_moyskbn=="4") {
//					// 転送先情報
//					index = that.repgrpInfo.KS004.idx;	// KS004～KS011
//					childurl = href[index];
//					if (row.F8=="0" || row.F8=="3") {
//						// オブジェクト作成
//						$.setJSONObject(sendJSON, $.id_inp.txt_bmncd, selBumon, selBumon);				// 部門
////						$.setJSONObject(sendJSON, $.id_inp.txt_moyscd, row.F1, row.F1);					// 催しコード
//						$.setJSONObject(sendJSON, $.id_inp.txt_moyskbn, row.F9, row.F9);				// 催し区分
//						$.setJSONObject(sendJSON, $.id_inp.txt_moysstdt, row.F10, row.F10);				// 催し開始日
//						$.setJSONObject(sendJSON, $.id_inp.txt_moysrban, row.F11, row.F11);				// 催し連番
//						$.setJSONObject(sendJSON, $.id_inp.txt_kanrino, row.F5, row.F5);				// 管理番号
//					} else {
//						// オブジェクト作成
//						$.setJSONObject(sendJSON, $.id_inp.txt_shncd, txt_shncd, txt_shncd);			// 商品コード
//						$.setJSONObject(sendJSON, $.id_inp.txt_bmncd, selBumon, selBumon);				// 部門
////						$.setJSONObject(sendJSON, $.id_inp.txt_moyscd, row.F1, row.F1);					// 催しコード
//						$.setJSONObject(sendJSON, $.id_inp.txt_moyskbn, row.F9, row.F9);				// 催し区分
//						$.setJSONObject(sendJSON, $.id_inp.txt_moysstdt, row.F10, row.F10);				// 催し開始日
//						$.setJSONObject(sendJSON, $.id_inp.txt_moysrban, row.F11, row.F11);				// 催し連番
//						$.setJSONObject(sendJSON, $.id_inp.txt_kanrino, row.F5, row.F5);				// 管理番号
//					}
//					break;
				} else if (sel_moyskbn=="5") {
					// 転送先情報
					index = that.repgrpInfo.SO003.idx;
					childurl = href[index];
					// オブジェクト作成
					$.setJSONObject(sendJSON, $.id_inp.txt_bmncd, row.F12, row.F12);					// 部門
//					$.setJSONObject(sendJSON, $.id_inp.txt_moyscd, row.F1, row.F1);						// 催しコード
					$.setJSONObject(sendJSON, $.id_inp.txt_moyskbn, row.F9, row.F9);					// 催し区分
					$.setJSONObject(sendJSON, $.id_inp.txt_moysstdt, row.F10, row.F10);					// 催し開始日
					$.setJSONObject(sendJSON, $.id_inp.txt_moysrban, row.F11, row.F11);					// 催し連番
					$.setJSONObject(sendJSON, $.id_inp.txt_kanrino, row.F5, row.F5);					// 管理番号
					break;
//				} else if (sel_moyskbn=="7") {
//					// 転送先情報
//					index = that.repgrpInfo.GY003.idx;
//					childurl = href[index];
//					// オブジェクト作成
//					$.setJSONObject(sendJSON, $.id_inp.txt_bmncd, selBumon, selBumon);					// 部門
////					$.setJSONObject(sendJSON, $.id_inp.txt_moyscd, row.F1, row.F1);					// 催しコード
//					$.setJSONObject(sendJSON, $.id_inp.txt_moyskbn, row.F9, row.F9);					// 催し区分
//					$.setJSONObject(sendJSON, $.id_inp.txt_moysstdt, row.F10, row.F10);					// 催し開始日
//					$.setJSONObject(sendJSON, $.id_inp.txt_moysrban, row.F11, row.F11);					// 催し連番
//					$.setJSONObject(sendJSON, $.id_inp.txt_kanrino, row.F5, row.F5);					// 管理番号
//					break;
				} else if (sel_moyskbn=="8") {
					// 転送先情報
					index = that.repgrpInfo.JU033.idx;
					childurl = href[index];
					// オブジェクト作成
					$.setJSONObject(sendJSON, $.id_inp.txt_shncd, txt_shncd, txt_shncd);				// 商品コード
					$.setJSONObject(sendJSON, $.id_inp.txt_moyscd, row.F1, row.F1);						// 催しコード
//					$.setJSONObject(sendJSON, $.id_inp.txt_moyskbn, row.F9, row.F9);					// 催し区分
//					$.setJSONObject(sendJSON, $.id_inp.txt_moysstdt, row.F10, row.F10);					// 催し開始日
//					$.setJSONObject(sendJSON, $.id_inp.txt_moysrban, row.F11, row.F11);					// 催し連番
					$.setJSONObject(sendJSON, $.id_inp.txt_kanrino, row.F5, row.F5);					// 管理番号
					break;
				} else if (sel_moyskbn=="9") {
					// 転送先情報
					index = that.repgrpInfo.JU013.idx;
					childurl = href[index];
					// オブジェクト作成
					$.setJSONObject(sendJSON, $.id_inp.txt_shncd, txt_shncd, txt_shncd);				// 商品コード
					$.setJSONObject(sendJSON, $.id_inp.txt_moyscd, row.F1, row.F1);						// 催しコード
//					$.setJSONObject(sendJSON, $.id_inp.txt_moyskbn, row.F9, row.F9);					// 催し区分
//					$.setJSONObject(sendJSON, $.id_inp.txt_moysstdt, row.F10, row.F10);					// 催し開始日
//					$.setJSONObject(sendJSON, $.id_inp.txt_moysrban, row.F11, row.F11);					// 催し連番
					$.setJSONObject(sendJSON, $.id_inp.txt_kanrino, row.F5, row.F5);					// 管理番号
					break;
				}
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
		},
		changeInputboxFunc:function(that, id, newValue, obj){

			var parentObj = $('#'+that.focusRootId);
			if(id+"_"===obj.attr('id') && that.focusGridId!==""){
				parentObj = $('#'+that.focusGridId).datagrid('getPanel');
			}

			// DB問い合わせ系
			if($('[for_inp^='+id+'_]').length > 0){
				var param = that.getInputboxParams(that, id, newValue);
				$.getsetInputboxRowData(that.name, 'for_inp', id, param, that, parentObj);
			}

			// DB問い合わせ系
			/*if($('[for_inp^='+id+'_]').length > 0){
				var param = that.getInputboxParams(that, id, newValue);
				var rows = $.getSelectListData(that.name, $.id.action_change, id, param);
				var row = rows.length > 0 ? rows[0]:"";
				$.setInputboxRowData('for_inp', id, row, that, parentObj);
			}*/

			// 検索、入力後特殊処理
//			if(that.queried){
//
//			}
			if(id===$.id_inp.txt_shncd || id===$.id_inp.txt_tencd){
				$.removeErrState();
			}
			if(id===$.id_inp.txt_hbstdt || id===$.id_inp.txt_hbeddt){
				$.removeErrState();
			}
			if(id===$.id_inp.txt_nnstdt || id===$.id_inp.txt_nneddt){
				$.removeErrState();
			}

		},
		keyEventInputboxFunc:function(e, code, that, obj){

			var id = $(obj).attr("orizinid");
			var value = '';

			// *** Enter or Tab ****
			if(code === 13 || code === 9) {
				if (code === 9){
					value = $(e.target).val();
				} else {
					value = $.getInputboxValue($('#'+id));
				}

				// 商品コード
				if(id===$.id_inp.txt_shncd){
					if(!$.isEmptyVal(value)){
						if(value.length < 8 ){
							value = ('00000000'+value).substr(-8);
							$.setInputboxValue($('#'+id), value);
						}else if(value.length > 8 ){
							value = value.substr(0, 8);
							$.setInputboxValue($('#'+id), value);
						}
						return false;
					}
				}
			}
		}
	} });
})(jQuery);