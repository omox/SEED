/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_ReportST007',			// （必須）レポートオプションの確認
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
		maxMergeCell: 0,
		onChangeFlag : false,
		onChangeFlag2 : false,
		columnName:'',	// OnClickRowの列名
		queried : false,
		initializes : true,
		onChangeReport: false,
		reportYobiInfo: function(){			// （必須）レポートメニューに登録された予備情報＝帳票をどういった表示にしたいかの情報
			return $('#reportYobi1').val();
		},
		sortBtnid_: "",						// 並び替えボタンID情報
		sendBtnid: "",						// （必須）呼出ボタンID情報
		focusRootId:"cc",					// （キー移動時必須）キー移動イベントのルートとなるパネルのID
		focusParentId:"",					// （キー移動時必須）現在フォーカスがあたっている項目の親となるパネルのID
		focusGridId:"",						// （キー移動時必須）現在フォーカスがあたっているDataGridのID
		editRowIndex:{},					// グリッド編集行保持
		baseData:[],						// 検索結果保持用
		updData:[],							// 検索結果保持用
		gridData:[],						// 検索結果保持用
		initialize: function (reportno){	// （必須）初期化
			var that = this;
			// 引き継ぎ情報
			this.jsonHidden = $.getTargetValue();
			// 画面の初回基本設定
			this.setInitObjectState();

			// 初期検索条件設定
			this.jsonInit = $.getInitValue();

			// 初期化するオブジェクト数設定
			this.initObjNum = this.dedefaultObjNum;

			// データ表示エリア初期化
			that.setGrid($.id.gridholder, reportno);

			var isUpdateReport = true;

			// 編集可能データグリッドの共通処理設定
			// 編集エディターの機能拡張（非表示で参照用のオブジェクトが準備してある前提、参照オブジェクトはグリッドのフィールド名を元に判断）
			$.extendDatagridEditor();

			// 初期検索可能
			that.onChangeReport = true;

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


			if(that.sendBtnid===$.id.btn_new){
				var mode = "0";
			} else if (that.sendBtnid===$.id.btn_sel_change || that.sendBtnid===$.id.btn_sel_refer) {
				var mode = "1";
			}

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

			// データグリッドのIDを変更。
			$('#'+$.id.grd_subwindow_runkTenTnfo).attr('id', "gridholder");

			// 各種遷移ボタン
			$('#'+$.id.btn_jissekirefer).on("click", $.pushChangeReport);	// 実績参照
			$('#'+$.id.btn_set).on("click", $.pushChangeReport);			// 設定
			$('#'+$.id.btn_cancel).on("click", $.pushChangeReport);			// キャンセル
			$('#'+$.id.btn_upd).on("click", $.pushChangeReport);			// 登録
			$('#'+$.id.btn_del).on("click", $.pushChangeReport);			// 削除
			$('#'+$.id.btn_copy).on("click", $.pushChangeReport);			// コピー
			$.initReportInfo("ST007", "ランクマスタ店情報　参照", "参照");

			$('#layout_ST008').hide();
			$('#'+$.id.btn_jissekiorder).linkbutton('disable');
			$('#'+$.id.btn_jissekiorder).attr('disabled', 'disabled').hide();
			$('#'+$.id.btn_jissekirefer).linkbutton('disable');
			$('#'+$.id.btn_jissekirefer).attr('disabled', 'disabled').hide();
			$.setInputBoxDisable($("#"+$.id_inp.txt_ten_number));				// 店舗数
			$.setInputBoxDisable($("#"+$.id.chk_rinji));						// 臨時

//			$('#'+$.id.btn_tennoorder).on("click", $.pushChangeReport);
//
			$('#'+$.id.btn_tennoorder).on("click", function(e){
				if (that.sortOrder==="" || (that.sortOrder==="ASC" && that.sortBtnid_==="TENNO")) {
					that.sortOrder = "DESC";
				} else {
					that.sortOrder = "ASC";
				}
				that.sortBtnid_ = "TENNO";
				that.sortGridRows($.id.gridholder, that.sortBtnid_, that.sortOrder);
			});
			$('#'+$.id.btn_rankorder).on("click", function(e){
				if (that.sortOrder==="ASC" && that.sortBtnid_==="RANKNO") {
					that.sortOrder = "DESC";
				} else {
					that.sortOrder = "ASC";
				}
				that.sortBtnid_ = "RANKNO";
				that.sortGridRows($.id.gridholder, that.sortBtnid_, that.sortOrder);
			});
			$('#'+$.id.btn_jissekiorder).on("click", function(e){
				that.sortName = "SANKOUHBJ";
				if (that.sortOrder==="" || that.sortOrder==="DESC") {
					that.sortOrder = "ASC";
				} else {
					that.sortOrder = "DESC";
				}
			});

			// 変更
			$($.id.hiddenChangedIdx).val('');
		},
		sortData:function(){

		},
		initCondition: function (){	// 条件初期値セット
			var that = this;
			// 初期化項目
		},
		clear:function(){
			// 隠し情報初期化
			$($.id.hiddenChangedIdx).val("");						// 変更行Index
			// グリッド初期化
			this.success(this.name, false);
		},
		validation: function (){	// （必須）批准
			var that = this;
			var rt = true;
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
			var txt_bmncd		= $.getJSONObject(this.jsonString, $.id_inp.txt_bmncd).value;		// 部門
			var chk_rinji		= $.getJSONObject(this.jsonString, $.id.chk_rinji).value;			// 臨時
			var txt_moyscd		= $.getJSONObject(this.jsonString, $.id_inp.txt_moyscd).value;		// 催しコード
			var txt_rankno		= $.getJSONObject(this.jsonString, $.id_inp.txt_rankno).value;		// ランクNo.

			// 処理時間計測用
			that.timeData = (new Date()).getTime();

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,		// レポート名
					BMNCD:			txt_bmncd,
					MOYSCD:			txt_moyscd,
					RINJI:			chk_rinji,
					RANKNO:			txt_rankno,
					SENDBTNID:		that.sendBtnid,
					SORTBTN:		that.sortBtnid_,
					t:				(new Date()).getTime(),
					sortable:		sortable,
					sortName:		that.sortName,
					sortOrder:		that.sortOrder,
					rows:			1	// 表示可能レコード数
				},
				function(json){
					// 検索処理エラー判定
					if($.searchError(json)) return false;

					// ログ出力
					$.log(that.timeData, 'query:');

					var opts = JSON.parse(json).opts
					that.gridData = JSON.parse(json).rows

					// 検索結果を保持
					that.baseData = JSON.parse(json).rows;

					// メインデータ表示
					that.setData(opts.rows_, opts);
					that.queried = true;

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
		getGridData: function (target){
			var that = this;

			var data = {};
			var targetRows= [];

			// 実仕入先一覧
			if(target===undefined || target===$.id.gridholder){
				var rows	 = $($.id.gridholder).datagrid('getRows');

				for (var i=0; i<rows.length; i++){
					var rowDate = {
							F1	 : rows[i]["F1"],
							F2	 : rows[i]["F2"],
							F3	 : rows[i]["F3"],
							F4	 : rows[i]["F4"],
					};
					targetRows.push(rowDate);
				}
				data[$.id.gridholder] = targetRows;
			}
			return data;
		},
		setTenCount:function (){
			var that = this;
			var count = '';
			var rows	 = $($.id.gridholder).datagrid('getRows');
			if(rows){
				count = rows.length;
			}
			$.setInputboxValue($('#'+ $.id_inp.txt_ten_number), count);
		},
		updValidation: function (){	// （必須）批准
			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform).form('validate');
			if(!rt){
				$.messager.alert($.message.ID_MESSAGE_TITLE_WARN,'入力内容を確認してください。','warning');
				return rt;
			}

			that.updData = [];
			var inpData = {};

			$('#'+that.focusRootId).find('[col^=F]').each(function(){
				var col = $(this).attr('col');
				inpData[col] = $.getInputboxValue($(this));
			});

			if(rt){
				that.updData.push(inpData);
			}

			return rt;
		},
		updSuccess: function(id){	// validation OK時 の update処理
			var that = this;

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$.appendMaskMsg();

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,							// レポート名
					action:			$.id.action_update,					// 実行処理情報
					obj:			id,									// 実行オブジェクト
					SENDBTNID:		that.sendBtnid,
					DATA:			JSON.stringify(that.updData),		// 更新対象情報
					t:				(new Date()).getTime()
				},
				function(data){
					// 検索処理エラー判定
					if($.updError(id, data)) return false;

					var afterFunc = function(){
						// 初期化
						that.clear();
						that.changeReport(that.name, $.id.btn_upd);
					};
					$.updNormal(data, afterFunc);

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
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_bmncd),
				text:	''
			});
			// 催しコード
			this.jsonTemp.push({
				id:		$.id_inp.txt_moyscd,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_moyscd),
				text:	''
			});
			// 臨時
			this.jsonTemp.push({
				id:		$.id.chk_rinji,
				value:	$.getJSONValue(this.jsonHidden, $.id.chk_rinji),
				text:	''
			});
			// ランクNo.
			this.jsonTemp.push({
				id:		$.id_inp.txt_rankno,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_rankno),
				text:	''
			});
		},
		setGrid: function (id, reportNumber, mode){	// グリッドの構築
			var that = this;
			var init = true;
			// ページサイズ定義取得
			var pageList = $.fn.pagination.defaults.pageList;
			var pageSize = $.getJSONValue(this.jsonHidden, $.id.pageSize);
			if (pageSize==="") pageSize=$.fn.pagination.defaults.pageSize;
			pageSize = $.getDefaultPageSize(pageSize, pageList);
//			var columns = that.getGridColumns(that, id);
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
//				columns:columns,
				columns:[[
					{field:'F1',	title:'店番',				width: 70,halign:'center',align:'left'},
					{field:'F2',	title:'店舗名',				width: 500,halign:'center',align:'left'},
					{field:'F3',	title:'ランク',				width: 70,halign:'center',align:'left'},
					//{field:'F4',	title:'参考販売実績',		width: 100,halign:'center',align:'right'},
					{field:'F4',	title:'エリア',				width: 70,halign:'center',align:'left'}
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
					that.setTenCount();

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

			var fields = ["F1","F2","F3","F4","F5"];
			var titles = ["店番","店舗名","ランク","参考販売実績","エリア"];
			var dformatter =function(value){
				var add20 = value && value.length===6;
				return $.getFormatDt(value, add20);
			};
			columnBottom.push({field:fields[0],	title:titles[0],	width:70 ,halign:'center',align:'left'});
			columnBottom.push({field:fields[1],	title:titles[1],	width:90 ,halign:'center',align:'left'});
			columnBottom.push({field:fields[2],	title:titles[2],	width:160,halign:'center',align:'left'});
			columnBottom.push({field:fields[3],	title:titles[3],	width:160,halign:'center',align:'left'});
			columnBottom.push({field:fields[4],	title:titles[4],	width:100,halign:'center',align:'left', formatter:dformatter});
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
		sortGridRows: function (id, sortBtnid, sortOrder){
			var taht =this;
			var rows = $(id).datagrid('getRows');
			if(rows){
				if (sortBtnid==="TENNO") {
					if (sortOrder==="ASC") {
						rows.sort(function(a,b){
						    if(a.F1>b.F1) return 1;
						    if(a.F1<b.F1) return -1;
						    return 0;
						});
					} else {
						rows.sort(function(a,b){
						    if(a.F1<b.F1) return 1;
						    if(a.F1>b.F1) return -1;
						    return 0;
						});
					}
					for (var i=0; i<rows.length; i++){
						$(id).datagrid('updateRow',{
							index: i,
							row: { F1:rows[i] }
						})
					}
				}
				if (sortBtnid==="RANKNO") {
					if (sortOrder==="ASC") {
						rows.sort(function(a,b){
						    if(a.F3>b.F3) return 1;
						    if(a.F3<b.F3) return -1;
						    return 0;
						});
					} else {
						rows.sort(function(a,b){
						    if(a.F3<b.F3) return 1;
						    if(a.F3>b.F3) return -1;
						    return 0;
						});
					}
					for (var i=0; i<rows.length; i++){
						$(id).datagrid('updateRow',{
							index: i,
//							row: rows[i],
							row: { F3:rows[i] }
						})
					}
				}
			}
		},
		setObjectState: function(){	// 軸の選択内容による制御

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
			var txt_bmncd		= $.getInputboxValue($('#'+$.id_inp.txt_bmncd));		// 部門

			// 戻る実行時用に現在の画面情報を保持する
			var states = $.getBackBaseJSON(that);
			var newrepinfos = $.getBackJSON(that, states, false);
			$.setJSONObject(sendJSON, 'repinfo', newrepinfos, '');

			// 呼出別処理
			switch (btnId) {
			case $.id.btn_back:
			case $.id.btn_cancel:
			case $.id.btn_upd:
			case $.id.btn_del:
				// 元画面情報
				var callpage = $.getJSONValue(that.jsonHidden, "callpage");
				// 転送先情報
				if (callpage == "Out_ReportST010") {
					index = 1;
					sendMode = 2;
				} else if (callpage == "Out_ReportST015") {
					sendMode = 1;
					// 元画面情報
					var callpage = $.getJSONValue(that.jsonHidden, "callpage");
					// 転送先情報
					index = 8;		// ST015 ランクマスタ コピー元ランクＮＯ選択
					if(that.reportYobiInfo()==='1'){
						index = 2;
					}
					childurl = href[index];
					var txt_bmncd	= $.getInputboxValue($('#'+$.id_inp.txt_bmncd));	// 部門
					var txt_rankno	= $.getInputboxValue($('#'+$.id_inp.txt_rankno));	// ランクNo
					var txt_rankkn	= $.getInputboxValue($('#'+$.id_inp.txt_rankkn));	// ランク名称
					var chk_rinji	= $.getInputboxValue($('#'+$.id.chk_rinji));		// 臨時
					var txt_moyscd	= $.getInputboxValue($('#'+$.id_inp.txt_moyscd));	// 催しコード
					var txt_bmncd_ini	= $.getJSONValue(this.jsonHidden, $.id_inp.txt_bmncd+'_ini');	// 部門
					var txt_rankno_ini	= $.getJSONValue(this.jsonHidden, $.id_inp.txt_rankno+'_ini');	// ランクNo
					var txt_rankkn_ini	= $.getJSONValue(this.jsonHidden, $.id_inp.txt_rankkn+'_ini');	// ランク名称
					var chk_rinji_ini	= $.getJSONValue(this.jsonHidden, $.id.chk_rinji+'_ini');		// 臨時
					var txt_moyscd_ini	= $.getJSONValue(this.jsonHidden, $.id_inp.txt_moyscd+'_ini');	// 催しコード
					var scrollToIndex	= $.getJSONValue(this.jsonHidden, "scrollToIndex_"+$.id.gridholder);

					// オブジェクト作成
					$.setJSONObject(sendJSON, $.id_inp.txt_bmncd, txt_bmncd, txt_bmncd);					// 部門
					$.setJSONObject(sendJSON, $.id_inp.txt_rankno, txt_rankno, txt_rankno);					// ランクNo
					$.setJSONObject(sendJSON, $.id_inp.txt_rankkn, txt_rankkn, txt_rankkn);					// ランク名称
					$.setJSONObject(sendJSON, $.id.chk_rinji, chk_rinji, chk_rinji);						// 臨時
					$.setJSONObject(sendJSON, $.id_inp.txt_moyscd, txt_moyscd, txt_moyscd);					// 催しコード
					$.setJSONObject(sendJSON, $.id_inp.txt_bmncd+'_ini', txt_bmncd_ini, txt_bmncd_ini);		// 部門
					$.setJSONObject(sendJSON, $.id_inp.txt_rankno+'_ini', txt_rankno_ini, txt_rankno_ini);	// ランクNo
					$.setJSONObject(sendJSON, $.id_inp.txt_rankkn+'_ini', txt_rankkn_ini, txt_rankkn_ini);	// ランク名称
					$.setJSONObject(sendJSON, $.id.chk_rinji+'_ini', chk_rinji_ini, chk_rinji_ini);			// 臨時
					$.setJSONObject(sendJSON, $.id_inp.txt_moyscd+'_ini', txt_moyscd_ini, txt_moyscd_ini);	// 催しコード
					$.setJSONObject(sendJSON, "scrollToIndex_"+$.id.gridholder, scrollToIndex, scrollToIndex);	// index

					$($.id.hiddenChangedIdx).val('');														// 画面遷移時にメッセージを表示させない
					break;
				}
				if(that.reportYobiInfo()==='1'){
					index = 2;
				}

				childurl = href[index];
				break;
			case $.id.btn_copy:
				// 元画面情報
				var callpage = $.getJSONValue(that.jsonHidden, "callpage");
				// 転送先情報
				index = 8;		// ST015 ランクマスタ コピー元ランクＮＯ選択
				if(that.reportYobiInfo()==='1'){
					index = 2;
				}
				childurl = href[index];

				// オブジェクト作成
				$.setJSONObject(sendJSON, $.id_inp.txt_bmncd, txt_bmncd, txt_bmncd);		// 部門

				break;
			case $.id.btn_jissekirefer:	// 実績参照
				var callpage = $.getJSONValue(that.jsonHidden, "callpage");
				// 転送先情報
				index = 4;	// ST011 (ランクマスタ実績参照)

				if(that.reportYobiInfo()==='1'){
					index = 2;
				}
				childurl = href[index];

				// オブジェクト作成
				$.setJSONObject(sendJSON, $.id_inp.txt_bmncd, txt_bmncd, txt_bmncd);		// 部門

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