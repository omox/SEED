/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_ReportST015',			// （必須）レポートオプションの確認
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
		maxMergeCell: 0,
		onChangeFlag : false,
		onChangeFlag2 : false,
		columnName:'',	// OnClickRowの列名
		queried : false,
		initializes : true,
		onChangeReport: true,
		reportYobiInfo: function(){			// （必須）レポートメニューに登録された予備情報＝帳票をどういった表示にしたいかの情報
			return $('#reportYobi1').val();
		},
		pushBtnId: "",						// （必須）実行ボタンID情報(検索系で利用)
		sendBtnid: "",						// （必須）呼出ボタンID情報
		focusRootId:"cc",					// （キー移動時必須）キー移動イベントのルートとなるパネルのID
		focusParentId:"",					// （キー移動時必須）現在フォーカスがあたっている項目の親となるパネルのID
		focusGridId:"",						// （キー移動時必須）現在フォーカスがあたっているDataGridのID
		editRowIndex:{},					// グリッド編集行保持
		baseData:[],						// 検索結果保持用
		updData:[],							// 検索結果保持用
		initBmn:"",
		initRinji:"",
		initMoyscd:"",
		initRank:"",
		initRankkn:"",
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

			var count = 2;
			// 入力テキストボックス系
			var inputbox = Object.getOwnPropertyNames($.id_inp);
			for ( var sel in inputbox ) {
				if($('#'+$.id_inp[inputbox[sel]]).length > 0){
					$.setInputbox(that, reportno, $.id_inp[inputbox[sel]], isUpdateReport);
					count++;
				}
			}

			// チェックボックス
			$.setCheckboxInit(that.jsonHidden, 'chk_rinji', that);

			// 検索実行
			that.onChangeReport = true;

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

			// 各種遷移ボタン
			$('#'+$.id.btn_cancel).on("click", $.pushChangeReport);
			$("#"+$.id.btn_new).linkbutton('disable');
			$("#"+$.id.btn_new).attr('tabindex', -1).hide();
			$('#'+$.id.btn_sel_kakutei).on("click", $.pushChangeReport);
			$('#'+$.id.btn_sel_view).on("click", $.pushChangeReport);
			$.initReportInfo("ST015", "ランクマスタ　コピー元ランクNOの選択");

			// 変更
			$($.id.hiddenChangedIdx).val('');
		},
		initCondition: function (){	// 条件初期値セット
			var that = this;
			// 初期化項目
		},
		clear:function(){
//			// 隠し情報初期化
//			$($.id.hiddenChangedIdx).val("");						// 変更行Index

		},
		validation: function (){	// （必須）批准
			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform).form('validate');
			var txt_bmncd		= $('#'+$.id_inp.txt_bmncd).textbox('getValue');							// 部門
			var chk_rinji		= $('#'+$.id.chk_rinji).is(':checked') ? $.id.value_on : $.id.value_off; 	// 臨時
			var txt_moyscd		= $('#'+$.id_inp.txt_moyscd).textbox('getValue'); 							// 催しコード

			if (rt) {
				if (!txt_bmncd) {
					$.showMessage('EX1025', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_bmncd), true)});
					rt = false;
				}
			}
			if (rt) {
				if (chk_rinji == "1" && !txt_moyscd) {
					$.showMessage('EX1026', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_moyscd), true)});
					rt = false;
				}
			}
			if (rt) {
				if (chk_rinji == "0" && txt_moyscd) {
					$.showMessage('EX1027', undefined, function(){$.addErrState(that, $('#'+$.id.chk_rinji), true)});
					rt = false;
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

			var txt_bmncd		= $.getJSONObject(this.jsonString, $.id_inp.txt_bmncd).value;		// 部門
			var chk_rinji		= $.getJSONObject(this.jsonString, $.id.chk_rinji).value;			// 臨時
			var txt_moyscd		= $.getJSONObject(this.jsonString, $.id_inp.txt_moyscd).value;		// 催しコード

			// 引き継ぎ項目を保存
			if ($.isEmptyVal(that.initBmn)) {
				// 元画面情報
				var callpage = $.getJSONValue(that.jsonHidden, "callpage");
				if (callpage == "Out_ReportST007") {
					that.initBmn	= $.getJSONValue(that.jsonHidden, $.id_inp.txt_bmncd+'_ini');	// 部門
					that.initRinji	= $.getJSONValue(that.jsonHidden, $.id.chk_rinji+'_ini');		// 臨時
					that.initMoyscd	= $.getJSONValue(that.jsonHidden, $.id_inp.txt_moyscd+'_ini');	// 催しコード
					that.initRank	= $.getJSONValue(that.jsonHidden, $.id_inp.txt_rankno+'_ini');	// ランク№
					that.initRankkn	= $.getJSONValue(that.jsonHidden, $.id_inp.txt_rankkn+'_ini');	// ランク名称
				} else {
					that.initBmn	= $.getJSONValue(that.jsonHidden, $.id_inp.txt_bmncd);	// 部門
					that.initRinji	= $.getJSONValue(that.jsonHidden, $.id.chk_rinji);		// 臨時
					that.initMoyscd	= $.getJSONValue(that.jsonHidden, $.id_inp.txt_moyscd);	// 催しコード
					that.initRank	= $.getJSONValue(that.jsonHidden, $.id_inp.txt_rankno);	// ランク№
					that.initRankkn	= $.getJSONValue(that.jsonHidden, $.id_inp.txt_rankkn);	// ランク名称
				}
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
					report:			that.name,		// レポート名
					BMNCD:			txt_bmncd,
					RINJI:			chk_rinji,
					MOYSCD:			txt_moyscd,
					t:				(new Date()).getTime(),
					sortable:		sortable,
					sortName:		that.sortName,
					sortOrder:		that.sortOrder,
					rows:			0	// 表示可能レコード数
				},
				function(json){
					// 検索処理エラー判定
					if($.searchError(json)) return false;
					var jsonp = JSON.parse(json);
					var count = jsonp["total"];
					if(count === 0){
						$.showMessage('I30000');
					}

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
				id:		$.id_inp.txt_bmncd,
				value:	$('#'+$.id_inp.txt_bmncd).textbox('getValue'),
				text:	$('#'+$.id_inp.txt_bmncd).textbox('getText')
			});
			// 臨時
			this.jsonTemp.push({
				id:		'chk_rinji',
				value:	$('#chk_rinji').is(':checked') ? $.id.value_on : $.id.value_off,
				text:	''
			});
			// 催しコード
			this.jsonTemp.push({
				id:		$.id_inp.txt_moyscd,
				value:	$('#'+$.id_inp.txt_moyscd).textbox('getValue'),
				text:	$('#'+$.id_inp.txt_moyscd).textbox('getText')
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
				rownumbers:true,
				fit:true,
				view:scrollview,
				pageSize:pageSize,
				pageList:pageList,
				frozenColumns:[[]],
				columns:[[
					{field:'F1',	title:'ランクNo.',				width: 100,halign:'center',align:'left'},
					{field:'F2',	title:'ランク名称',				width: 300,halign:'center',align:'left'},
					{field:'F3',	title:'店舗数',					width: 100,halign:'center',align:'right'},
					{field:'F4',	title:'店ランク配列',			hidden:true}
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
//		getGridColumns:function(that, id){
//			var columns = [];
//			var columnBottom=[];
//
//			var fields = ["F1","F2","F3","F4","F5"];
//			var titles = ["店番","店舗名","ランク","参考販売実績","エリア"];
//			var dformatter =function(value){
//				var add20 = value && value.length===6;
//				return $.getFormatDt(value, add20);
//			};
//			columnBottom.push({field:fields[0],	title:titles[0],	width:70 ,halign:'center',align:'left'});
//			columnBottom.push({field:fields[1],	title:titles[1],	width:90 ,halign:'center',align:'left'});
//			columnBottom.push({field:fields[2],	title:titles[2],	width:160,halign:'center',align:'left'});
//			columnBottom.push({field:fields[3],	title:titles[3],	width:160,halign:'center',align:'left'});
//			columnBottom.push({field:fields[4],	title:titles[4],	width:100,halign:'center',align:'left', formatter:dformatter});
//			columns.push(columnBottom);
//			return columns;
//
//		},
//		setData: function(rows, opts){		// データ表示
//			var that = this;
//
//			if(rows.length > 0){
//				$('#'+that.focusRootId).find('[col^=F]').each(function(){
//					var col = $(this).attr('col');
//					if(rows[0][col]){
//						$.setInputboxValue($(this), rows[0][col]);
//					}
//				});
//			}
//		},
//		setObjectState: function(){	// 軸の選択内容による制御
//
//		},
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
			var newrepinfos = $.getBackJSON(that, states, false);
			$.setJSONObject(sendJSON, 'repinfo', newrepinfos, '');

			var txt_bmncd		= $.getInputboxValue($('#'+$.id_inp.txt_bmncd));						// 部門
			var txt_moyscd		= $.getInputboxValue($('#'+$.id_inp.txt_moyscd));						// 催しコード
			var chk_rinji		= $('#chk_rinji').is(':checked') ? $.id.value_on : $.id.value_off;		// 臨時
			// 選択行
			var row = $($.id.gridholder).datagrid("getSelected");
			var rowIndex = $($.id.gridholder).datagrid("getRowIndex", row);

			// コピー元の入力情報
			var repstatesBef = $.getJSONObject(that.jsonHidden, "repinfo");
			if(repstatesBef){
				var repinfos = [];
				repinfos = repinfos.concat(repstatesBef.value);
				for (var i = 0; i < repinfos.length; i++) {
					var repinfo = repinfos[i]
					var rankno = "";
					var rankkn = "";
					if(repinfo.id == 'Out_ReportST008'){
						var values = repinfo.value.TMPCOND		// 保持された入力情報
						for(var j = 0; j < values.length; j++){
							var value = values[j]
							if(value.id == $.id_inp.txt_rankno){
								rankno = value.value;

							}else if(value.id == $.id_inp.txt_rankkn){
								rankkn = value.value;

							}
						}
						var inputInfo = [{
								'txt_rankno' : rankno,			// ランクNo.
								'txt_rankkn' : rankkn,			// ランク名称
						}]
						$.setJSONObject(sendJSON, 'inputInfo', inputInfo, '');						// 入力情報
					}
				}
			}

			// 呼出別処理
			switch (btnId) {
			case $.id.btn_sel_kakutei:
				sendMode= 1;
				if(!row){
					$.showMessage('E00008');
					return false;
				}
				// 転送先情報
				index = 7;							// ST008 店情報　新規・変更
				childurl = href[index];
				var updData = $.getJSONValue(that.jsonHidden, 'updData');								// 検索結果保持(グリッド一覧)
				var initData 	= $.getJSONValue(that.jsonHidden, 'initData');							// 初回検索結果保持(グリッド一覧)

				// オブジェクト作成
				$.setJSONObject(sendJSON, $.id_inp.txt_bmncd, txt_bmncd, txt_bmncd);						// 部門
				$.setJSONObject(sendJSON, $.id_inp.txt_moyscd, txt_moyscd, txt_moyscd);						// 催しコード
				$.setJSONObject(sendJSON, $.id.chk_rinji, chk_rinji, chk_rinji);							// 臨時
				$.setJSONObject(sendJSON, $.id_inp.txt_bmncd+'_ini', that.initBmn, that.initBmn);			// 部門
				$.setJSONObject(sendJSON, $.id_inp.txt_moyscd+'_ini', that.initMoyscd, that.initMoyscd);	// 催しコード
				$.setJSONObject(sendJSON, $.id.chk_rinji+'_ini', that.initRinji, that.initRinji);			// 臨時
				$.setJSONObject(sendJSON, $.id_inp.txt_rankno+'_ini', that.initRank, that.initRank);		// ランク№
				$.setJSONObject(sendJSON, $.id_inp.txt_rankkn+'_ini', that.initRankkn, that.initRankkn);	// ランク名称
				$.setJSONObject(sendJSON, $.id_inp.txt_rankno, row.F1, row.F1);						// ランクNo.
				$.setJSONObject(sendJSON, $.id_inp.txt_rankkn, row.F2, row.F2);						// ランク名称
				$.setJSONObject(sendJSON, $.id_inp.txt_tenrank_arr, row.F4, row.F4);				// 店ランク配列
				$.setJSONObject(sendJSON, 'updData', updData, updData);								// 検索結果保持(グリッド一覧)
				$.setJSONObject(sendJSON, 'initData', updData, initData);							// 初回検索結果保持(グリッド一覧)
				break;
			case $.id.btn_sel_view:
				sendMode =1;
				if(!row){
					$.showMessage('E00008');
					return false;
				}
				// 実績参照
				var callpage = $.getJSONValue(that.jsonHidden, "callpage");
				// 転送先情報
				index = 9;							// ST007 店情報

				childurl = href[index];

				$.setJSONObject(sendJSON, 'sendBtnid', btnId, $('#'+btnId).attr("title"));				// 実行ボタン情報保持

				// オブジェクト作成
				$.setJSONObject(sendJSON, $.id_inp.txt_bmncd, txt_bmncd, txt_bmncd);					// 部門
				$.setJSONObject(sendJSON, $.id_inp.txt_moyscd, txt_moyscd, txt_moyscd);					// 催しコード
				$.setJSONObject(sendJSON, $.id.chk_rinji, chk_rinji, chk_rinji);						// 臨時
				$.setJSONObject(sendJSON, $.id_inp.txt_rankno, row.F1, row.F1);							// ランクNo.
				$.setJSONObject(sendJSON, $.id_inp.txt_bmncd+'_ini', that.initBmn, that.initBmn);			// 部門
				$.setJSONObject(sendJSON, $.id_inp.txt_moyscd+'_ini', that.initMoyscd, that.initMoyscd);	// 催しコード
				$.setJSONObject(sendJSON, $.id.chk_rinji+'_ini', that.initRinji, that.initRinji);			// 臨時
				$.setJSONObject(sendJSON, $.id_inp.txt_rankno+'_ini', that.initRank, that.initRank);		// ランク№
				$.setJSONObject(sendJSON, $.id_inp.txt_rankkn+'_ini', that.initRankkn, that.initRankkn);	// ランク名称
				$.setJSONObject(sendJSON, "scrollToIndex_"+$.id.gridholder, rowIndex);
				break;
			case $.id.btn_cancel:
			case $.id.btn_back:
//				sendMode= 1;
//				// 転送先情報
//				index = 7;							// ST008 店情報　新規・変更
//				childurl = href[index];
				var txt_bmncd 	= $.getJSONValue(that.jsonHidden, $.id_inp.txt_bmncd);				// 部門
				var txt_rankno 	= $.getJSONValue(that.jsonHidden, $.id_inp.txt_rankno);				// ランクNo
				var txt_rankkn 	= $.getJSONValue(that.jsonHidden, $.id_inp.txt_rankkn);				// ランク名称
				var chk_rinji 	= $.getJSONValue(that.jsonHidden, $.id.chk_rinji);					// 臨時
				var txt_moyscd 	= $.getJSONValue(that.jsonHidden, $.id_inp.txt_moyscd);				// 催しコード
				var updData 	= $.getJSONValue(that.jsonHidden, 'updData');						// 検索結果保持(グリッド一覧)
				var initData 	= $.getJSONValue(that.jsonHidden, 'initData');						// 初回検索結果保持(グリッド一覧)
				// オブジェクト作成
				$.setJSONObject(sendJSON, $.id_inp.txt_bmncd, that.initBmn, that.initBmn);			// 部門
				$.setJSONObject(sendJSON, $.id_inp.txt_moyscd, that.initMoyscd, that.initMoyscd);	// 催しコード
				$.setJSONObject(sendJSON, $.id.chk_rinji, that.initRinji, that.initRinji);			// 臨時
				$.setJSONObject(sendJSON, $.id_inp.txt_rankno, txt_rankno, txt_rankno);				// ランクNo
				$.setJSONObject(sendJSON, $.id_inp.txt_rankkn, txt_rankkn, txt_rankkn);				// ランク名称
				$.setJSONObject(sendJSON, 'updData', updData, updData);								// 検索結果保持(グリッド一覧)
				$.setJSONObject(sendJSON, 'initData', updData, initData);							// 初回検索結果保持(グリッド一覧)

				/*for (var i = 0; i < newrepinfos.length; i++) {
					var callpage = newrepinfos[i].id;
					if(callpage==='Out_ReportST008'){
						index = 7;
					}
				}*/
				var callpage = $.getJSONValue(that.jsonHidden, "callpage");
				if(callpage==='Out_ReportST008'){
					index = 7;
				}
//				sendMode = 2;
				sendMode = 1;
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

		}
	} });
})(jQuery);