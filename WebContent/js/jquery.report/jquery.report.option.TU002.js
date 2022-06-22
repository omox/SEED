/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_ReportTU002',			// （必須）レポートオプションの確認
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
		sendBtnid: "",						// （必須）呼出ボタンID情報
		clickBtnid: "",						// 当画面で押下されたボタンIDを保持
		focusRootId:"cc",					// （キー移動時必須）キー移動イベントのルートとなるパネルのID
		focusParentId:"",					// （キー移動時必須）現在フォーカスがあたっている項目の親となるパネルのID
		focusGridId:"",						// （キー移動時必須）現在フォーカスがあたっているDataGridのID
		editRowIndex:{},					// グリッド編集行保持
		gridData:[],						// 検索結果
		gridTitle:[],						// 検索結果
		shuno_list:{},						// 週Noの情報を保持（Init時に取得）
		grd_suryo:[],						// グリッド情報:数量入力
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

			var isUpdateReport = true;

			// 入力テキストボックス系
			var inputbox = Object.getOwnPropertyNames($.id_inp);
			for ( var sel in inputbox ) {
				if($('#'+$.id_inp[inputbox[sel]]).length > 0){
					$.setInputbox(that, reportno, $.id_inp[inputbox[sel]], isUpdateReport);
				}
			}
			// 定量
			that.getsetInputboxData(reportno, $.id.txt_shoridt, [{}], $.id.action_init);
			//that.setEditableGrid(that, reportno, $.id.gridholder);

			// 初期検索可能
			that.onChangeReport = false;
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
			if(!sendBtnid){
				sendBtnid = $('#sendBtnid').val();
			}
			$('#sendBtnid').val(sendBtnid);
			that.sendBtnid = sendBtnid;

			if (sendBtnid && sendBtnid.length > 0) {
				$.reg.search = true;
			}

			var reportYobi1 = $.getJSONValue(that.jsonHidden, "reportYobi1");
			if(!reportYobi1){
				reportYobi1 = $('#reportYobi1').val();
			}
			$('#reportYobi1').val(reportYobi1);

			$.initReportInfo("TU002", "アンケート発注　数量入力");

			var txt_qasmdt	 = $.getJSONValue(that.jsonHidden, $.id_inp.txt_qasmdt);

			// 各種遷移ボタン
			$('#'+$.id.btn_cancel).on("click", $.pushChangeReport);

			// クリックされたボタンのIDを保持
			$('#'+$.id.btn_del).on("click", function(){that.clickBtnid = $.id.btn_del});
			$('#'+$.id.btn_upd).on("click", function(){that.clickBtnid = $.id.btn_upd});
		},
		initCondition: function (){	// 条件初期値セット
			var that = this;
			// 初期化項目
		},
		clear:function(){
			// 隠し情報初期化
			$($.id.hiddenChangedIdx).val("");	// 変更行Index

			// グリッド初期化
			this.success(this.name, false);
		},
		endUpdate:function (){

			// レポート番号取得
			var reportno = $($.id.hidden_reportno).val();
			// レポート定義位置
			var reportNumber = $.getReportNumber(reportno);
			this.changeReport(reportNumber, 'btn_return')

		},
		validation: function (){	// （必須）批准
			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = true;
			// 入力エラーなしの場合に検索条件を格納
			that.jsonString = that.jsonTemp.slice(0);

			return rt;
		},
		success: function(reportno, sortable){	// （必須）正処理

			if (sortable) sortable=1; else sortable=0;
			var that = this;

			// 検索実行
			var txt_moyskbn		= $.getJSONObject(this.jsonHidden, $.id_inp.txt_moyskbn).value;		// B/M番号
			var txt_moysstdt		= $.getJSONObject(this.jsonHidden, $.id_inp.txt_moysstdt).value;		// B/M番号
			var txt_moysrban		= $.getJSONObject(this.jsonHidden, $.id_inp.txt_moysrban).value;		// B/M番号

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$.appendMaskMsg();

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,		// レポート名
					MOYSKBN:		txt_moyskbn,
					MOYSSTDT:		txt_moysstdt,
					MOYSRBAN:		txt_moysrban,
					SENDBTNID:		that.sendBtnid,
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

					// 検索データ（想定）
					that.gridData = JSON.parse(json).rows;
					that.gridTitle = JSON.parse(json).titles;

					var opts = JSON.parse(json).opts

					// メインデータ表示
					if(opts && opts.rows_){
						// 基本データ表示
						that.setData(opts.rows_, opts);
					}
					that.queried = true;

					// データグリッド初期化
					//that.setEditableGrid(that, reportno, $.id.gridholder);

					// 一覧画面へ戻る
					/*if (that.clickBtnid === $.id.btn_del || that.clickBtnid === $.id.btn_upd) {
						that.changeReport(that.name, 'btn_return');
					}*/

					that.queried = true;
					// Load処理回避
					$.tryChangeURL($.reg.jqeasy);

					// グリッド再描画（easyui 1.4.2 対応）
					$($.id.gridholder).datagrid('load', {} );
					$.removeMask();

					// 状態保存
					$.saveState2(reportno, that.getJSONString());

					// ログ出力
					$.log(that.timeData, 'loaded:');
				}
			);
		},
		updValidation: function (){	// （必須）批准

			//配送点グループグリッドの編集を終了する。
			var row = $($.id.gridholder).datagrid("getSelected");
			var rowIndex = $($.id.gridholder).datagrid("getRowIndex", row);
			$($.id.gridholder).datagrid('endEdit',rowIndex);

			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform).form('validate');

			// TODO: 背景を赤くする対応を追加
			//var msgid = that.checkInputboxFunc($.id_inp.txt_hstengpcd);
			var msgid = null;
			if(msgid !==null){
				$.showMessage(msgid);
				return false;
			}
			// 入力データ：店舗アンケート付き送付け_商品
			var targetDatas_SHN = that.getMergeGridDate('gridholder');
			if (targetDatas_SHN.length===0){
				$.showMessage('E20582');
				return false;
			}

			return rt;
		},
		updSuccess: function(id){	// validation OK時 の update処理
			var that = this;

			// 基本入力情報取得
			var targetDatas = that.getGridData("data")["data"];

			// 入力データ：店舗アンケート付き送付け_商品
			var targetDatas_SHN = that.getMergeGridDate('gridholder');

			// 処理時間計測用
			that.timeData = (new Date()).getTime();

			$.post(
				$.reg.jqgrid ,
				{
					report:		that.name,					// レポート名
					action:		$.id.action_update,	// 実行処理情報
					obj:			id,									// 実行オブジェクト
					SENDBTNID:that.sendBtnid,
					DATA:			JSON.stringify(targetDatas),		// 更新対象情報
					DATA_SHN:	JSON.stringify(targetDatas_SHN),// 更新対象情報(店舗アンケート付き送付け_商品)
					t:				(new Date()).getTime()
				},
				function(data){

					// ログ出力
					$.log(that.timeData, 'loaded:');

					var afterFunc = function(){
						// 初期化
						that.getEasyUI();
						that.jsonString = that.jsonTemp.slice(0);
						that.clear();
						that.changeReport(that.name, 'btn_return');
					};

					// メッセージ処理（後処理あり）
					//$.ExMessage(data,afterFunc);
					//return false;

					// 検索処理エラー判定
					if($.updError(id, data)){
						// 更新日時を最新に更新
						var json = JSON.parse(data);
						var rows= $($.id.gridholder).datagrid('getRows');

						for (var i=0; i<json.opts.DATA_SHN.length; i++){	// 更新情報（数量変更に対して最新の更新日時あり）
							for (var j=0; j<rows.length; j++){							// 画面上の情報に対して更新
								if (json.opts.DATA_SHN[i]['F4']===rows[j]['F15']){	// F4 = F15 = 管理番号
									rows[j]['F16'] = json.opts.DATA_SHN[i]['F7'];			// F16 = F7 = 更新日時
									break;
								}
							}
						}

						return false;
					}

					$.updNormal(data, afterFunc);
					$.removeMaskMsg();

				}
			);
		},
		delValidation: function (){	// （必須）批准
			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = true;
			return rt;
		},
		delSuccess: function(id){

			var that = this;
			var targetDatas = [{}];
			$('#'+that.focusRootId).find('[col^=F]').each(function(){
				var col = $(this).attr('col');
				targetDatas[0][col] = $.getInputboxValue($(this));
			});
			var row = $($.id.gridholder).datagrid("getSelected");

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$.appendMaskMsg();

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,						// レポート名
					action:			$.id.action_delete,				// 実行処理情報
					obj:			id,								// 実行オブジェクト
					DATA:			JSON.stringify(targetDatas),	// 更新対象情報(配送グループ)
					t:			(new Date()).getTime()
				},
				function(data){
					// 検索処理エラー判定
					if($.delError(id, data)) return false;

					var afterFunc = function(){
						// 初期化
						that.clear();
						that.changeReport(that.name, 'btn_return');
					};
					$.delNormal(data, afterFunc);
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
			// 催し区分
			this.jsonTemp.push({
				id:		$.id_inp.txt_moyskbn,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_moyskbn),
				text:	''
			});
			// 催しコード（催し開始日）
			this.jsonTemp.push({
				id:		$.id_inp.txt_moysstdt,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_moysstdt),
				text:	''
			});
			// 催し連番
			this.jsonTemp.push({
				id:		$.id_inp.txt_moysrban,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_moysrban),
				text:	''
			});
			// 商品コード
			this.jsonTemp.push({
				id:		$.id_inp.txt_moykn,
				value:	$('#'+$.id_inp.txt_moykn).textbox('getValue'),
				text:	$('#'+$.id_inp.txt_moykn).textbox('getText')
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
		extenxDatagridEditorIds:{
			 F12		: "txt_htsu"
		},
		setEditableGrid: function(that, reportno, id){		// データ表示
			var that = this;
			//that.editRowIndex['gridholder'] = -1;
			var index = -1;
			var columns = that.getGridColumns(that, id);
			var nneddt = $('#'+$.id.txt_shoridt).val();		// 処理日付

			var suryo = "";

			$(id).datagrid({
				columns:columns,
				onBeforeLoad:function(param){
					param.report = that.name;
				},
				onLoadSuccess:function(data){
					// 検索結果を保持
					var gridData = that.getGridData('gridholder');
					that.setGridData(gridData, 'gridholder');

					var nnstdt = data.rows[0].F14;
					var nneddt			= $('#'+$.id.txt_shoridt).val();									// 処理日付
					var sdt = $.convDate(nnstdt, true);
					var edt = $.convDate(nneddt, true);
					if(sdt >= edt ){
						$('#'+$.id.btn_clear).linkbutton('enable').attr('tabindex', 10);
					}
					$.setInputBoxDisableVariable($('#'+$.id.btn_upd),true);
				},
				onClickRow: function(index,field){
						$.clickEditableDatagridCell(that,'gridholder', index)

				},
				onBeforeEdit:function(index,row){
					var shoridt			= $('#'+$.id.txt_shoridt).val();									// 処理日付
					var nnstdt = row.F14;					// アンケート締切日
					var sdt = $.convDate(nnstdt, true);
					var edt = $.convDate(nneddt, true);
					if(sdt < edt || row.F13 == '3'){
						// 次の行に移るか、次の項目に移るかする
						// 次の行に移るか、次の項目に移るかする
						var adds = that.editRowIndex[that.focusGridId] > index ? -1:1;
						var nextindex = index + adds;
						if(nextindex >= 0 && nextindex < $(id).datagrid('getRows').length){
							$(id).datagrid('selectRow', nextindex);
							$(id).datagrid('beginEdit', nextindex);
						}else{
							that.editRowIndex[that.focusGridId] = -9;	// 次の項目に移動するために未編集状態と違うインデックス設定
							var evt = $.Event('keydown');
							evt.keyCode = 13;
							evt.shiftKey = adds === -1;
							$(id).parents('.datagrid').eq(0).trigger(evt);
						}
						return false;
					}
				},
				onSelect:function(index,row){
						$('gridholder').datagrid('beginEdit', index);
				},
				onBeginEdit:function(index,row){
					$.beginEditDatagridRow(that,'gridholder', index, row);
					suryo = row.F12
				},
				onEndEdit: function(index,row,changes){
					$.endEditDatagridRow(that, 'gridholder', index, row);
					if (suryo!==row.F12) {
						$.setInputBoxEnableVariable($('#'+$.id.btn_upd));
					}
				}
			});
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
		getGridColumns:function(that, id){
			var columns = [];
			var columnBottom=[];
			var columtop=[];
			var fields = ["F1","F2","F3","F4","F5","F6","F7","F8","F9","F10","F11","F12","F13","F14","F15"];
			var titles = ["商品コード","商品名","原価","本体価格","総額売価","入数","原価","本体売価","総額売価","値入率","納入日","数量","訂正区分","アンケート締切日","管理番号"];
			columtop.push({title:'　', colspan:2});
			columtop.push({title:'レギュラー', colspan:3});
			columtop.push({title:'アンケート条件', colspan:5});
			columtop.push({title:'　', colspan:2});
			columnBottom.push({field:fields[0],		title:titles[0],	width:80,	halign:'center',align:'left',
				formatter:function(value,row,index){
					return $.getFormatPrompt(value, '####-####');
				},editor:{options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}});
			columnBottom.push({field:fields[1],		title:titles[1],	width:270,	halign:'center',align:'left',editor:{options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}});
			columnBottom.push({field:fields[2],		title:titles[2],	width:65,	halign:'center',align:'right',formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0.00');},editor:{options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}});
			columnBottom.push({field:fields[3],		title:titles[3],	width:60,	halign:'center',align:'right',formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0');},editor:{options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}});
			columnBottom.push({field:fields[4],		title:titles[4],	width:60,	halign:'center',align:'right',formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0');},editor:{options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}});
			columnBottom.push({field:fields[5],		title:titles[5],	width:40,	halign:'center',align:'right',editor:{options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}});
			columnBottom.push({field:fields[6],		title:titles[6],	width:65,	halign:'center',align:'right',formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0.00');},editor:{options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},editor:{options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}});
			columnBottom.push({field:fields[7],		title:titles[7],	width:60,	halign:'center',align:'right',formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0');},editor:{options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}});
			columnBottom.push({field:fields[8],		title:titles[8],	width:60,	halign:'center',align:'right',formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0');},editor:{options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}});
			columnBottom.push({field:fields[9],		title:titles[9],	width:50,	halign:'center',align:'right',formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0.00');},editor:{options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}});
			columnBottom.push({field:fields[10],	title:titles[10],	width:100,	halign:'center',align:'left',editor:{options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}});
			columnBottom.push({field:fields[11],	title:titles[11],	width:80,	halign:'center',align:'right',formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0');},editor:{type:'numberbox'}
				,styler:function(value,row,index){
					// 数量変更後の色
					if(typeof that.grd_suryo[index] !== "undefined" && +that.grd_suryo[index]["F5"] !== +value){
						//return 'background-color:#FEF4F4;';
					}
				}
			});
			columnBottom.push({field:fields[12],	title:titles[12],	width:80,	halign:'center',align:'right',hidden:true});
			columnBottom.push({field:fields[13],	title:titles[13],	width:80,	halign:'center',align:'right',hidden:true});
			columnBottom.push({field:fields[14],	title:titles[14],	width:80,	halign:'center',align:'right',hidden:true});

			columns.push(columtop);
			columns.push(columnBottom);
			return columns;

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
			var sendMode = "";	// 遷移後に、前回検索条件を表示したい場合、""のまま、ここで設定した条件を表示したい場合"1"

			// タブ要素(a)取得
			var elems = $('#tabContent', window.parent.document).map(
				function(i,e) {
					return e;
				}).get();
			var href = elems[0].value.split(',');

			// JSON Object Clone ()
			var sendJSON = [];
			$.setJSONObject(sendJSON, 'sendBtnid', btnId, $('#'+btnId).attr("title"));				// 実行ボタン情報保持
			$.setJSONObject(sendJSON, 'reportYobi1', that.reportYobiInfo(), that.reportYobiInfo());	// 参照情報保持
			$.setJSONObject(sendJSON, 'callpage', that.name, that.name);

			// 戻る実行時用に現在の画面情報を保持する
			var states = $.getBackBaseJSON(that);
			// 各種グリッド情報を設定
			var newrepinfos = $.getBackJSON(that, states);
			$.setJSONObject(sendJSON, 'repinfo', newrepinfos, '');// 呼出し元レポート情報

			// 呼出別処理
			switch (btnId) {
			case $.id.btn_cancel:
			case $.id.btn_back:
			case $.id.btn_upd:
			case "btn_return":

				index = 3;
				childurl = href[index];
				sendMode = 2

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
		getsetInputboxData: function(reportno, id, param, action){
			var that = this
			if(action===undefined) action = $.id.action_change;
			idx = -1;
			// 情報設定
			$.ajax({
				url: $.reg.easy,
				type: 'POST',
				async: false,
				data: {
					page	: reportno,
					obj		: id,
					sel		: (new Date()).getTime(),
					target	: id,
					action	: action,
					json	: JSON.stringify(param)
				},
				success: function(json){
					var value = "";
					if(json !=="" &&  JSON.parse(json).rows.length > 0){
						value = JSON.parse(json).rows[0].VALUE;
					}
					$.setInputboxValue($('#'+id), value);

					// 編集可能データグリッドの共通処理設定
					// 編集エディターの機能拡張（非表示で参照用のオブジェクトが準備してある前提、参照オブジェクトはグリッドのフィールド名を元に判断）
					$.extendDatagridEditor(that);

					// 数量入力
					//that.setGrid2('gridholder', reportno);
					that.setEditableGrid(that, reportno, $.id.gridholder);

				}
			});
			idx = 1;
		},
		getGridData: function (target){

			var data = {};
			var targetRows= [];

			// 基本情報
			if(target===undefined || target==="data"){

				var szMoyskbn	= $.getJSONObject(this.jsonString, $.id_inp.txt_moyskbn).value;		// 催し区分
				var szMoysstdt	= $.getJSONObject(this.jsonString, $.id_inp.txt_moysstdt).value;	// 催しコード（催し開始日）
				var szMoysrban	= $.getJSONObject(this.jsonString, $.id_inp.txt_moysrban).value;	// 催し連番
				var szUpddt		= $.getInputboxValue($('#hiddenUpddt'));							// 更新日時(排他チェック用)

				var rowDate = {
						F1	 : szMoyskbn,					// 催し区分
						F2	 : szMoysstdt,					// 催し開始日
						F3	 : szMoysrban,					// 催し連番
						F4	 : szUpddt,						// 更新日時(排他チェック用)
				};
				if(rowDate){
					targetRows.push(rowDate);
				}
				data[target] = targetRows;
			}

			// 数量入力
			if(target===undefined || target==='gridholder'){
				var szMoyskbn	= $.getJSONObject(this.jsonString, $.id_inp.txt_moyskbn).value;		// 催し区分
				var szMoysstdt	= $.getJSONObject(this.jsonString, $.id_inp.txt_moysstdt).value;	// 催しコード（催し開始日）
				var szMoysrban	= $.getJSONObject(this.jsonString, $.id_inp.txt_moysrban).value;	// 催し連番
				var rows= $($.id.gridholder).datagrid('getRows');
				for (var i=0; i<rows.length; i++){
					if(rows[i]["SHNCD"] == "" || rows[i]["SHNCD"] == null ){
						var rowDate = {
								F1	 : szMoyskbn,									// 催し区分
								F2	 : szMoysstdt,									// 催しコード（催し開始日）
								F3	 : szMoysrban,									// 催し連番
								F4	 : rows[i]["F15"],								// 管理番号
								F5	 : rows[i]["F12"],								// 発注数
								F6	 : rows[i]["F16"],								// 更新日時
								F7	 : rows[i]["F17"],								// 変更前発注数
							};
						targetRows.push(rowDate);
					}
				}
				data[target] = targetRows;
			}
			return data;
		},
		setGridData: function (data, target){
			var that = this;

			// 基本情報
			/*if(target===undefined || target==="data"){
				that.data =  data["data"];
			}*/

			// 商品一覧
			if(target===undefined || target==='gridholder'){
				that.grd_suryo =  data['gridholder'];
			}
		},
		getMergeGridDate: function(target, del){
			// 保持したデータと入力データ比較を比較する。
			var that = this;

			var newrows			 = that.getGridData(target)[target] ? that.getGridData(target)[target] : [];		// 変更データ
			var oldrows			 = [];
			var targetRows		 = [];

			// 発注数一覧
			if(target===undefined || target==='gridholder'){
				oldrows = that.grd_suryo
				for (var i=0; i<newrows.length; i++){

					if((oldrows[i]['F5'] ? +oldrows[i]['F5'] : 0) !== (newrows[i]['F5'] ? +newrows[i]['F5'] : 0)){
						if((oldrows[i]['F1'] && oldrows[i]['F1'] !== "")
								&& (oldrows[i]['F2'] && oldrows[i]['F2'] !== "")
								&& (oldrows[i]['F3'] && oldrows[i]['F3'] !== "")
								&& (oldrows[i]['F4'] && oldrows[i]['F4'] !== "")
						){
							var rowDate = {
									F1	 : newrows[i]["F1"],							// 催し区分
									F2	 : newrows[i]["F2"],							// 催しコード（催し開始日）
									F3	 : newrows[i]["F3"],							// 催し連番
									F4	 : newrows[i]["F4"],							// 管理番号
									F5	 : newrows[i]["F5"],							// 発注数
									F6	 : '',											// 店発注数量
									F7	 : newrows[i]["F6"],							// 更新日
									F8	 : newrows[i]["F7"],							// 変更前店発注数量
							};
							if(rowDate){
								targetRows.push(rowDate);
							}
						}
					}
				}
			}
			return targetRows;
		},
		changeInputboxFunc:function(that, id, newValue, obj){

			var that = this;
			var parentObj = $('#'+that.focusRootId);
			if(id+"_"===obj.attr('id') && that.focusGridId!==""){
				parentObj = $('#'+that.focusGridId).datagrid('getPanel');
			}

			// DB問い合わせ系
			if($('[for_inp^='+id+'_]').length > 0){
				var param = that.getInputboxParams(that, id, newValue);
				var rows = $.getSelectListData(that.name, $.id.action_change, id, param);
				var row = rows.length > 0 ? rows[0]:"";
				$.setInputboxRowData('for_inp', id, row, that, parentObj);
			}

			// TODO: 背景を赤くする対応を追加
			//var msgid = that.checkInputboxFunc(id,newValue);
			var msgid = null;
			if(msgid !==null){
				$.showMessage(msgid);
				return false;
			}
		},
		getInputboxParams: function(that, id, newValue){
			// 情報取得
			var values = {};
			values["value"] = newValue;

			// 情報設定
			return [values];
		},
		checkInputboxFunc: function(id, newValue){

			var that = this;

			// 店グループコード重複チェック
			if (id===$.id_inp.txt_hstengpcd) {

				if (newValue === null || newValue === '' || newValue === undefined) {
					return null;
				}

				// 配送店グループ一覧
				var hstgps			= [];
				var hstengpcd		= "";
				var errFlg			= true; // グリッドの入力チェックに使用
				var targetRowsHstgp	= $($.id.gridholder).datagrid('getRows');

				for (var i=0; i<targetRowsHstgp.length; i++){

					// 配送店グループコードを格納
					hstengpcd = targetRowsHstgp[i]["HSTENGPCD"];

					// 配送店グループの情報を必ず1行は入力
					if ((errFlg && (hstengpcd != '' && hstengpcd != null)) || (newValue !== null && newValue !== '' && newValue !== undefined)) {
						errFlg = false;
					}

					// エリア区分が0の場合店舗部門マスタの存在チェック(ここでは桁数のチェックのみ)
					if ($("input[name="+$.id.rad_areakbn+"]:checked").val() === '0') {
						if (parseInt(hstengpcd) > 99) {
							return 'E11041';
						}

					// エリア区分が1の場合数値チェック(10番以上での登録)
					} else {
						if (parseInt(hstengpcd) < 10) {
							return 'E11038';
						}
					}

					if (i===that.editRowIndex[$.id.gridholder]) {
						hstgps.push(newValue);
					} else {
						if (hstengpcd != null && hstengpcd != '' && hstengpcd !== undefined) {
							// 重複チェック用
							hstgps.push(hstengpcd);
						}
					}
				}

				// 店グループの入力が存在しなかった場合
				if (errFlg) {
					return 'EX1017';
				}

				// 重複チェック
				var hstgps_ = hstgps.filter(function (element, index, self) { return self.indexOf(element)*1 === index; });
				if(hstgps.length !== hstgps_.length){
					return 'E11141';
				}
			}
			return null;
		},
	} });
})(jQuery);