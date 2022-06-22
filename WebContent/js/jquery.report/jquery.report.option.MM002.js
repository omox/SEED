/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_ReportMM002',			// （必須）レポートオプションの確認
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
		dedefaultObjNum:	3,					// 初期化オブジェクト数
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
		pushBtnId: "",						// （必須）実行ボタンID情報(検索系で利用)
//		sendBtnid: "",						// （必須）呼出ボタンID情報
		focusRootId:"cc",					// （キー移動時必須）キー移動イベントのルートとなるパネルのID
		focusParentId:"",					// （キー移動時必須）現在フォーカスがあたっている項目の親となるパネルのID
		focusGridId:"",						// （キー移動時必須）現在フォーカスがあたっているDataGridのID
//		editRowIndex:{},					// グリッド編集行保持
//		baseData:[],						// 検索結果保持用
//		updData:[],							// 検索結果保持用
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

//			// 編集可能データグリッドの共通処理設定
//			// 編集エディターの機能拡張（非表示で参照用のオブジェクトが準備してある前提、参照オブジェクトはグリッドのフィールド名を元に判断）
//			$.extendDatagridEditor();
//
			// 初期検索可能
			that.onChangeReport = true;

			var count = 1;
//			// 名称マスタ参照系
//			var meisyoSelect = Object.getOwnPropertyNames($.id_mei);
//			for ( var sel in meisyoSelect ) {
//				if($('#'+$.id_mei[meisyoSelect[sel]]).length > 0){
//					$.setMeisyoCombo(that, reportno, $.id_mei[meisyoSelect[sel]], isUpdateReport);
//					count++;
//				}
//			}
			// 入力テキストボックス系
			var inputbox = Object.getOwnPropertyNames($.id_inp);
			for ( var sel in inputbox ) {
				if($('#'+$.id_inp[inputbox[sel]]).length > 0){
					$.setInputbox(that, reportno, $.id_inp[inputbox[sel]], isUpdateReport);
					count++;
				}
			}
//			// 商品一覧検索
//			that.setEditableGrid(that, reportno, $.id.grd_moyken_shohin+'_list');

			// 初期化終了
			this.initializes =! this.initializes;

//			$.initialSearch(that);

//			$.initialDisplay(that);

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

			var sendBtnid = $.getJSONValue(that.jsonHidden, "sendBtnid");
			if(sendBtnid && sendBtnid.length > 0){
				$.reg.search = true;
			}

			// 各種遷移ボタン
			$('#'+$.id.btn_tennoview).on("click", $.pushChangeReport);		// 店番表示
			$('#'+$.id.btn_sel_shninfo).on("click", $.pushChangeReport);	// 選択(商品情報)
			$.setInputBoxDisable($("#"+$.id_inp.txt_tencd));
			$.setInputBoxDisable($("#"+$.id_inp.txt_bmncd));
			$.setInputBoxDisable($("#"+$.id_inp.txt_moyscd));
			// 当帳票を「参照」で開いた場合
			if(that.reportYobiInfo()==='1'){
				$.initReportInfo("MM002", "催し検索　参照　商品一覧");
			} else {
				$.initReportInfo("MM002", "催し検索　変更　商品一覧");
			}

//			 変更
//			$($.id.hiddenChangedIdx).val('');

		},
		initCondition: function (){	// 条件初期値セット
			var that = this;
			// 初期化項目
		},
		clear:function(){
//			// 隠し情報初期化
//			$($.id.hiddenChangedIdx).val("");						// 変更行Index
			// グリッド初期化
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
			var txt_tencd		= $.getJSONObject(this.jsonString, $.id_inp.txt_tencd).value;			// 店コード
			var txt_bmncd		= $.getJSONObject(this.jsonString, $.id_inp.txt_bmncd).value;			// 部門コード
			var txt_moyscd		= $.getJSONObject(this.jsonString, $.id_inp.txt_moyscd).value;			// 催しコード
			var txt_bmflg		= $.getJSONObject(this.jsonString, $.id_inp.txt_bmflg).value;			// B/Mフラグ
			var txt_bmnno		= $.getJSONObject(this.jsonString, $.id_inp.txt_bmnno).value;			// B/M番号

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
					report:			that.name,		// レポート名
					TENCD:			txt_tencd,		// 店コード
					BMNCD:			txt_bmncd,		// 部門コード
					MOYSCD:			txt_moyscd,		// 催しコード
					BMFLG:			txt_bmflg,		// B/Mフラグ
					BMNNO:			txt_bmnno,		// B/M番号
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
//		setGridData: function (data, target){
//			var that = this;
//
//			return true;
//		},
		getEasyUI: function(){	// （必須）情報の取得
			// 初期化
			this.jsonTemp = [];

			// レポート名
			this.jsonTemp.push({
				id:		"reportname",
				value:	this.caption(),
				text:	this.caption()
			});
			// 店コード
			this.jsonTemp.push({
				id:		$.id_inp.txt_tencd,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_tencd),
				text:	''
			});
			// 部門コード
			this.jsonTemp.push({
				id:		$.id_inp.txt_bmncd,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_bmncd),
				text:	''
			});
			// 催しコード
			this.jsonTemp.push({
				id:		$.id_inp.txt_moyscd,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_moyscd),
				text:	''
			});
			// B/Mフラグ
			this.jsonTemp.push({
				id:		$.id_inp.txt_bmflg,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_bmflg),
				text:	''
			});
			// B/M番号
			this.jsonTemp.push({
				id:		$.id_inp.txt_bmnno,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_bmnno),
				text:	''
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

			columnBottom.push({field:'F1',	title:'GNo.',				width: 30 ,halign:'center',align:'left'});
			columnBottom.push({field:'F2',	title:'子No.',				width: 30 ,halign:'center',align:'right'});
			columnBottom.push({field:'F3',	title:'商品コード',			width: 110,halign:'center',align:'left',
				formatter:function(value,row,index){
					return $.getFormatPrompt(value, '####-####');
				}
			});
			columnBottom.push({field:'F4',	title:'商品マスター名称',	width: 500 ,halign:'center',align:'left'});
			columnBottom.push({field:'F5',	title:'販売期間',			width: 200 ,halign:'center',align:'left'});
			columnBottom.push({field:'F6',	title:'納入期間',			width: 200 ,halign:'center',align:'left'});
			columnBottom.push({field:'F7',	title:'管理番号',			width: 100 ,halign:'center',align:'left',
				formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '0000');}});
			columnBottom.push({field:'F8',	title:'催しコード',			hidden:true});
			columnBottom.push({field:'F9',	title:'催し区分',			hidden:true});
			columnBottom.push({field:'F10',	title:'催し開始日',			hidden:true});
			columnBottom.push({field:'F11',	title:'催し連番',			hidden:true});
			columnBottom.push({field:'F12',	title:'登録種別',			hidden:true});
			columnBottom.push({field:'F13',	title:'B/Mフラグ',			hidden:true});
			columnBottom.push({field:'F14',	title:'B/M番号',			hidden:true});
			columns.push(columnBottom);
			return columns;

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
			TG016:{idx:5},		// 特売スポット計画 商品情報
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
			var newrepinfos = $.getBackJSON(that, states, false);
			$.setJSONObject(sendJSON, 'repinfo', newrepinfos, '');

			// 選択行
			var row = $($.id.gridholder).datagrid("getSelected");

			// 実行ボタン別処理
			switch (btnId) {
			case $.id.btn_tennoview:	// 店番表示
				sendMode = 1;
				if(!row){
					$.showMessage('E00008');
					return false;
				}
				// 転送先情報
				index = 4;		// MM003 催し検索 店舗一覧
				childurl = href[index];
//				var txt_moyscd		= $.getInputboxValue($('#'+$.id_inp.txt_moyscd));			// 催しコード
				// オブジェクト作成
//				$.setJSONObject(sendJSON, $.id_inp.txt_moyscd, txt_moyscd, txt_moyscd);			// 催しコード
				$.setJSONObject(sendJSON, $.id_inp.txt_moyscd, row.F8, row.F8);					// 催しコード
//				$.setJSONObject(sendJSON, $.id_inp.txt_moyskbn, row.F9, row.F9);				// 催し区分
//				$.setJSONObject(sendJSON, $.id_inp.txt_moysstdt, row.F10, row.F10);				// 催し開始日
//				$.setJSONObject(sendJSON, $.id_inp.txt_moysrban, row.F11, row.F11);				// 催し連番
				$.setJSONObject(sendJSON, $.id_inp.txt_kanrino, row.F7, row.F);					// 管理番号
				$.setJSONObject(sendJSON, $.id_inp.txt_bmflg, row.F13, row.F13);				// B/Mフラグ
				$.setJSONObject(sendJSON, $.id_inp.txt_bmnno, row.F14, row.F14);				// B/M番号
				$.setJSONObject(sendJSON, $.id_inp.txt_shncd, row.F3, row.F3);					// 商品コード
				break;
			case $.id.btn_sel_shninfo:	// 選択(商品情報)
				sendMode = 1;
				if(!row){
					$.showMessage('E00008');
					return false;
				}
//				var txt_moyscd		= $.getInputboxValue($('#'+$.id_inp.txt_moyscd));			// 催しコード
				var txt_bmncd		= $.getInputboxValue($('#'+$.id_inp.txt_bmncd));			// 部門コード
				if (row.F9=="0" ||
					((row.F9=="1" || row.F9=="2" || row.F9=="3") && row.F13=="0")) {
					// 転送先情報
					index = that.repgrpInfo.TG016.idx;
					childurl = href[index];
					// オブジェクト作成
//					$.setJSONObject(sendJSON, $.id_inp.txt_moyscd, txt_moyscd, txt_moyscd);		// 催しコード
//					$.setJSONObject(sendJSON, $.id_inp.txt_moyscd, row.F8, row.F8);				// 催しコード
					$.setJSONObject(sendJSON, $.id_inp.txt_moyskbn, row.F9, row.F9);			// 催し区分
					$.setJSONObject(sendJSON, $.id_inp.txt_moysstdt, row.F10, row.F10);			// 催し開始日
					$.setJSONObject(sendJSON, $.id_inp.txt_moysrban, row.F11, row.F11);			// 催し連番
					$.setJSONObject(sendJSON, $.id_inp.txt_bmncd, txt_bmncd, txt_bmncd);		// 部門コード
					$.setJSONObject(sendJSON, $.id_inp.txt_shncd, row.F3, row.F3);				// 商品コード
					$.setJSONObject(sendJSON, $.id_inp.txt_kanrino, row.F7, row.F7);			// 管理番号
					$.setJSONObject(sendJSON, $.id_inp.txt_kanrieno, '0', '0');					// 枝番 TODO:仕様確認待ちの為、ひとまず0固定
					$.setJSONObject(sendJSON, $.id_inp.txt_addshukbn, row.F12, row.F12);		// 登録種別
					break;
				} else if ((row.F9=="1" || row.F9=="2" || row.F9=="3") && row.F13=="1") {
					// 転送先情報
					index = that.repgrpInfo.BM006.idx;
					childurl = href[index];
					// オブジェクト作成
//					$.setJSONObject(sendJSON, $.id_inp.txt_moyscd, row.F8, row.F8);				// 催しコード
					$.setJSONObject(sendJSON, $.id_inp.txt_moyskbn, row.F9, row.F9);			// 催し区分
					$.setJSONObject(sendJSON, $.id_inp.txt_moysstdt, row.F10, row.F10);			// 催し開始日
					$.setJSONObject(sendJSON, $.id_inp.txt_moysrban, row.F11, row.F11);			// 催し連番
					$.setJSONObject(sendJSON, $.id_inp.txt_bmnno, row.F14, row.F14);			// B/M番号
					$.setJSONObject(sendJSON, $.id_inp.txt_kanrino, row.F7, row.F7);			// 管理番号
					break;
//				} else if (row.F9=="4") {
//					// 転送先情報
//					index = that.repgrpInfo.KS004.idx;	// KS004～KS011
//					childurl = href[index];
//					if (row.F12=="0" || row.F12=="3") {
//						// オブジェクト作成
////						$.setJSONObject(sendJSON, $.id_inp.txt_moyscd, row.F8, row.F8);			// 催しコード
//						$.setJSONObject(sendJSON, $.id_inp.txt_moyskbn, row.F9, row.F9);		// 催し区分
//						$.setJSONObject(sendJSON, $.id_inp.txt_moysstdt, row.F10, row.F10);		// 催し開始日
//						$.setJSONObject(sendJSON, $.id_inp.txt_moysrban, row.F11, row.F11);		// 催し連番
//						$.setJSONObject(sendJSON, $.id_inp.txt_bmncd, txt_bmncd, txt_bmncd);	// 部門コード
//						$.setJSONObject(sendJSON, $.id_inp.txt_kanrino, row.F7, row.F7);		// 管理番号
//					} else {
//						// オブジェクト作成
////						$.setJSONObject(sendJSON, $.id_inp.txt_moyscd, row.F8, row.F8);			// 催しコード
//						$.setJSONObject(sendJSON, $.id_inp.txt_moyskbn, row.F9, row.F9);		// 催し区分
//						$.setJSONObject(sendJSON, $.id_inp.txt_moysstdt, row.F10, row.F10);		// 催し開始日
//						$.setJSONObject(sendJSON, $.id_inp.txt_moysrban, row.F11, row.F11);		// 催し連番
//						$.setJSONObject(sendJSON, $.id_inp.txt_bmncd, txt_bmncd, txt_bmncd);	// 部門コード
//						$.setJSONObject(sendJSON, $.id_inp.txt_shncd, row.F3, row.F3);			// 商品コード
//						$.setJSONObject(sendJSON, $.id_inp.txt_kanrino, row.F7, row.F7);		// 管理番号
//					}
//					break;
				} else if (row.F9=="5") {
					// 転送先情報
					index = that.repgrpInfo.SO003.idx;
					childurl = href[index];
					// オブジェクト作成
//					$.setJSONObject(sendJSON, $.id_inp.txt_moyscd, row.F8, row.F8);				// 催しコード
					$.setJSONObject(sendJSON, $.id_inp.txt_moyskbn, row.F9, row.F9);			// 催し区分
					$.setJSONObject(sendJSON, $.id_inp.txt_moysstdt, row.F10, row.F10);			// 催し開始日
					$.setJSONObject(sendJSON, $.id_inp.txt_moysrban, row.F11, row.F11);			// 催し連番
					$.setJSONObject(sendJSON, $.id_inp.txt_bmncd, txt_bmncd, txt_bmncd);		// 部門コード
					$.setJSONObject(sendJSON, $.id_inp.txt_shncd, row.F3, row.F3);				// 商品コード
					$.setJSONObject(sendJSON, $.id_inp.txt_kanrino, row.F7, row.F7);			// 管理番号
					break;
//				} else if (row.F9=="7") {
//					// 転送先情報
//					index = that.repgrpInfo.GY003.idx;
//					childurl = href[index];
//					// オブジェクト作成
////					$.setJSONObject(sendJSON, $.id_inp.txt_moyscd, row.F8, row.F8);				// 催しコード
//					$.setJSONObject(sendJSON, $.id_inp.txt_moyskbn, row.F9, row.F9);			// 催し区分
//					$.setJSONObject(sendJSON, $.id_inp.txt_moysstdt, row.F10, row.F10);			// 催し開始日
//					$.setJSONObject(sendJSON, $.id_inp.txt_moysrban, row.F11, row.F11);			// 催し連番
//					$.setJSONObject(sendJSON, $.id_inp.txt_bmncd, txt_bmncd, txt_bmncd);		// 部門コード
//					$.setJSONObject(sendJSON, $.id_inp.txt_shncd, row.F3, row.F3);				// 商品コード
//					$.setJSONObject(sendJSON, $.id_inp.txt_kanrino, row.F7, row.F7);			// 管理番号
//					break;
				} else if (row.F9=="8") {
					// 転送先情報
					index = that.repgrpInfo.JU033.idx;
					childurl = href[index];
					// オブジェクト作成
					$.setJSONObject(sendJSON, $.id_inp.txt_moyscd, row.F8, row.F8);				// 催しコード
//					$.setJSONObject(sendJSON, $.id_inp.txt_moyskbn, row.F9, row.F9);			// 催し区分
//					$.setJSONObject(sendJSON, $.id_inp.txt_moysstdt, row.F10, row.F10);			// 催し開始日
//					$.setJSONObject(sendJSON, $.id_inp.txt_moysrban, row.F11, row.F11);			// 催し連番
					$.setJSONObject(sendJSON, $.id_inp.txt_bmncd, txt_bmncd, txt_bmncd);		// 部門コード
					$.setJSONObject(sendJSON, $.id_inp.txt_shncd, row.F3, row.F3);				// 商品コード
					$.setJSONObject(sendJSON, $.id_inp.txt_kanrino, row.F7, row.F7);			// 管理番号
					break;
				} else if (row.F9=="9") {
					// 転送先情報
					index = that.repgrpInfo.JU013.idx;
					childurl = href[index];
					// オブジェクト作成
					$.setJSONObject(sendJSON, $.id_inp.txt_moyscd, row.F8, row.F8);				// 催しコード
//					$.setJSONObject(sendJSON, $.id_inp.txt_moyskbn, row.F9, row.F9);			// 催し区分
//					$.setJSONObject(sendJSON, $.id_inp.txt_moysstdt, row.F10, row.F10);			// 催し開始日
//					$.setJSONObject(sendJSON, $.id_inp.txt_moysrban, row.F11, row.F11);			// 催し連番
					$.setJSONObject(sendJSON, $.id_inp.txt_bmncd, txt_bmncd, txt_bmncd);		// 部門コード
					$.setJSONObject(sendJSON, $.id_inp.txt_shncd, row.F3, row.F3);				// 商品コード
					$.setJSONObject(sendJSON, $.id_inp.txt_kanrino, row.F7, row.F7);			// 管理番号
					break;
				}
				break;
			case $.id.btn_back:
				// 転送先情報
				for (var i = 0; i < newrepinfos.length; i++) {
					var callpage = newrepinfos[i].id;
					var reportYobi1 = $.getJSONValue(that.jsonHidden, "reportYobi1");
					if(callpage==='Out_ReportMM001'){
						if (reportYobi1 === '0') {
							index = 1;
						} else {
							index = 2;
						}
					}
				}
				sendMode = 2;
				childurl = href[index];
//				var reportYobi1 = $.getJSONValue(that.jsonHidden, "reportYobi1");
//				if (reportYobi1 === '0') {
//					index = 1;
//				} else {
//					index = 2;
//				}
//				childurl = href[index];
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