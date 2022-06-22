/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_ReportBT001',			// （必須）レポートオプションの確認
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
		dedefaultObjNum:	2,	// 初期化オブジェクト数
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
			return $('#reportYobi1').val();},
		reportYobiInfo2: function(){
			return $('#reportYobi2').val();},
		pushBtnId: "",						// （必須）実行ボタンID情報(検索系で利用)
		focusRootId:"cc",					// （キー移動時必須）キー移動イベントのルートとなるパネルのID
		focusParentId:"",					// （キー移動時必須）現在フォーカスがあたっている項目の親となるパネルのID
		focusGridId:"",						// （キー移動時必須）現在フォーカスがあたっているDataGridのID
		initialize: function (reportno){	// （必須）初期化
			var that = this;
			// 引き継ぎ情報
			this.jsonHidden = $.getTargetValue();
			// 初期検索条件設定
			this.jsonInit = $.getInitValue();

			// 画面の初回基本設定
			this.setInitObjectState();

			// データ表示エリア初期化
			that.setGrid($.id.gridholder, reportno);

			// 初期化するオブジェクト数設定
			this.initObjNum = this.dedefaultObjNum;

			var isUpdateReport = false;

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
				}
			}

			// 初期化終了
			this.initializes =! this.initializes;

			$.initialDisplay(that);

			// キーイベントの設定
			$.initKeyEvent(that);

			// ログ出力
			$.log(that.timeData, 'initialize:');
		},
		setInitObjectState: function(){	// 画面初期化時の項目制御
			var that = this;
			// 引き継ぎ情報セット
			var sendBtnid = $.getJSONValue(that.jsonHidden, "sendBtnid");
			if(sendBtnid && sendBtnid.length > 0){
				that.sendBtnid = sendBtnid;
				$.reg.search = true;
			}

			if(that.reportYobiInfo()==='0'){
				//$.reg.search = true;
			}

			// 各種ボタン
			$('#'+$.id.btn_new).on("click", $.pushChangeReport);
			$('#'+$.id.btn_sel_change).on("click", $.pushChangeReport);
			$('#'+$.id.btn_sel_refer).on("click", $.pushChangeReport);

			if(that.reportYobiInfo()==='0'){
				$.initReportInfo("BT004", "分類割引　新規・変更　一覧");
				$('#'+$.id.btn_sel_refer).linkbutton('disable').attr('disabled', 'disabled').hide();
				$.setInputBoxDisable($("#"+$.id_inp.txt_moysstdt));
				$.setInputBoxDisable($("#"+$.id_mei.kbn10002));
				$("#"+$.id.btn_search).attr('tabindex', -1).hide();
				$("#"+$.id.btn_search).linkbutton('disable');
				$("#searchCon").hide();

			}else if(that.reportYobiInfo()==='1'){
				$.initReportInfo("BT001", "分類割引　参照　一覧");
				$('#'+$.id.btn_sel_change).linkbutton('disable').attr('disabled', 'disabled').hide();
				$("#"+$.id.btn_new).linkbutton('disable');
				$("#"+$.id.btn_new).attr('tabindex', -1).hide();

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
			// 入力エラーなしの場合に検索条件を格納
			if (rt == true) that.jsonString = that.jsonTemp.slice(0);
			// 入力チェック用の配列をクリア
			that.jsonTemp = [];
			return rt;
		},
		success: function(reportno, sortable){	// （必須）正処理
			if (sortable) sortable=1; else sortable=0;
			var that = this;

			var szMoyskbn		= '';		// 催し開始日
			var szMoysstdt		= '';		// 催し区分

			if(that.reportYobiInfo()==='1'){
				// 参照モードの時のみ検索条件をセット。
				szMoyskbn		= $.getJSONObject(this.jsonString, $.id_mei.kbn10002).value.replace('-1','');
				szMoysstdt		= $.getInputboxValue($('#'+$.id_inp.txt_moysstdt));
			}

			// initialDisplayでのMaskMsgを削除
			$.removeMaskMsg();

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
					report			:'Out_ReportBT004',			// レポート名
					SENDBTNID		:that.sendBtnid,
					MOYSKBN			:szMoyskbn,
					MOYSSTDT		:szMoysstdt,
					YOBIINFO		:that.reportYobiInfo(),
					t				:(new Date()).getTime(),
					sortable		:sortable,
					sortName		:that.sortName,
					sortOrder		:that.sortOrder,
					rows			:0							// 表示可能レコード数
				},
				function(json){
					// 検索処理エラー判定
					if($.searchError(json)) return false;

					// ログ出力
					$.log(that.timeData, 'query:');

					// Load処理回避
					$.tryChangeURL(null);

					var size = JSON.parse(json)["total"];
					if(size == 0){
						$.showMessage('E11003');
					}

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

					// ログ出力
					$.log(that.timeData, 'loaded:');
				}
			);
		},
		delValidation: function (){	// （必須）批准
			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = true;

			var row = $($.id.gridholder).datagrid("getSelected");
			if(!row){
				alert("データが選択されていません。");
				return false;
			}
			return rt;
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
			// 催し開始日
			this.jsonTemp.push({
				id:		$.id_inp.txt_moysstdt,
				value:	$('#'+$.id_inp.txt_moysstdt).textbox('getValue'),
				text:	$('#'+$.id_inp.txt_moysstdt).textbox('getText')
			});
			// 催し区分
			this.jsonTemp.push({
				id:		$.id_mei.kbn10002,
				value:	$('#'+$.id_mei.kbn10002).combobox('getValue'),
				text:	$('#'+$.id_mei.kbn10002).combobox('getText')
			});
		},
		setGrid: function (id, reportNumber){	// グリッドの構築
			var that = this;
			var init = true;
			var columns = that.getGridColumns(that, id);
			// ページサイズ定義取得
			var pageList = $.fn.pagination.defaults.pageList;
			var pageSize = $.getJSONValue(this.jsonHidden, $.id.pageSize);
			var dformatter =function(value){
				var add20 = value && value.length===6;
				var addweek = 1;	// フラグ用仮パラメータ(週まで表示したい際に使用)
				return $.getFormatDt(value, add20, addweek);
			};
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
					}

					// 前回選択情報をGridに反映
					var getRowIndex = $.getJSONValue(that.jsonHidden, "scrollToIndex_"+id);
					if(getRowIndex !== ""){
						$(id).datagrid('scrollTo', {
							index: getRowIndex,
							callback: function(index){
								$(id).datagrid('selectRow', index);
							}
						});
					}
				},
				onClickCell:function(rowIndex, field, value){
					// 列名保持
					that.columnName = field;
				},
				onClickRow:function(rowIndex, rowData){
					// ドリルリンク
					//that.changeReport($.id.column_class, that.columnName, rowData);
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

			columnBottom.push({field:'F1',	title:'企画No',				width:  60,halign:'center',align:'right'});
			columnBottom.push({field:'F2',	title:'分類割引名称',		width: 300,halign:'center',align:'left'});
			columnBottom.push({field:'F3',	title:'販売期間',			width: 190,halign:'center',align:'left'});
			columnBottom.push({field:'F4',	title:'催しコード',			width: 100,halign:'center',align:'left'});
			columnBottom.push({field:'F5',	title:'催し名称（漢字）',	width: 300,halign:'center',align:'left'});
			columnBottom.push({field:'F6',	title:'催し期間',			width: 190,halign:'center',align:'left'});
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
			$.setJSONObject(sendJSON, 'sendBtnid', btnId, $('#'+btnId).attr("title"));						// 実行ボタン情報保持
			$.setJSONObject(sendJSON, 'reportYobi1_bf', that.reportYobiInfo(), that.reportYobiInfo());		// 参照情報保持
			$.setJSONObject(sendJSON, 'callpage', that.name, that.name);									// 呼出し元レポート情報

			// 戻る実行時用に現在の画面情報を保持する
			var states = $.getBackBaseJSON(that);
			// 各種グリッド情報を設定
			var newrepinfos = $.getBackJSON(that, states);
			$.setJSONObject(sendJSON, 'repinfo', newrepinfos, '');

			// 選択行
			var row = $($.id.gridholder).datagrid("getSelected");

			// 実行ボタン別処理
			switch (btnId) {
			case $.id.btn_new:
				// 転送先情報
				index = 3;
				childurl = href[index];

				break;
			case $.id.btn_sel_change:
			case $.id.btn_sel_refer:

				if(!row){
					$.showMessage('E00008');
					return false;
				}
				if(btnId===$.id.btn_sel_change){
					// 転送先情報
					index = 3;
				}else if(btnId===$.id.btn_sel_refer){
					// 転送先情報
					index = 4;
				}
				childurl = href[index];

				$.setJSONObject(sendJSON, $.id_inp.txt_kkkno,    row.F1, row.F1);			// 企画No
				$.setJSONObject(sendJSON, $.id_inp.txt_moyskbn,  row.F7, row.F7);			// 催し区分
				$.setJSONObject(sendJSON, $.id_inp.txt_moysstdt, row.F8, row.F8);			// 催しコード（催し開始日）
				$.setJSONObject(sendJSON, $.id_inp.txt_moysrban, row.F9, row.F9)			// 催し連番
				$.setJSONObject(sendJSON, $.id_inp.txt_bmncd, 	 row.F10, row.F10);			// 部門コード
				$.setJSONObject(sendJSON, $.id_inp.txt_bmkan,    row.F11, row.F11);			// 部門名称（漢字）

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
		},
		csv: function(reportno){	// Csv出力
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
				'kbn'	: kbn,
				'type'	: 'csv'
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