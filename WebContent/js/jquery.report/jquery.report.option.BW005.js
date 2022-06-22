/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_ReportBW005',			// （必須）レポートオプションの確認
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
		dedefaultObjNum:	0,					// 初期化オブジェクト数
		initObjNum:	-1,
		initedObject: [],
		maxMergeCell: 1,
		onChangeFlag : false,
		onChangeFlag2 : false,
		columnName:'',	// OnClickRowの列名
		queried : false,
		initializes : true,
		onChangeReport: true,
		reportYobiInfo: function(){				// （必須）レポートメニューに登録された予備情報＝帳票をどういった表示にしたいかの情報
			return $('#reportYobi1').val();
		},
		focusRootId:"cc",						// （キー移動時必須）キー移動イベントのルートとなるパネルのID
		focusParentId:"",						// （キー移動時必須）現在フォーカスがあたっている項目の親となるパネルのID
		focusGridId:"",							// （キー移動時必須）現在フォーカスがあたっているDataGridのID
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

			// Load処理回避
			//$.tryChangeURL(null);

			// 初期化終了
			this.initializes =! this.initializes;

			//$.initialSearch(that);
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

			// 当帳票を「参照」で開いた場合
			if(that.reportYobiInfo()==='1'){
				$('#'+$.id.btn_new).hide();
				$('#'+$.id.btn_new).attr('disabled', 'disabled').hide();
				$('#'+$.id.btn_sel_csverr).hide();
				$('#'+$.id.btn_sel_csverr).attr('disabled', 'disabled').hide();
				$('#'+$.id.btn_csv_import).hide();
				$('#'+$.id.btn_csv_import).attr('disabled', 'disabled').hide();
				$('#'+$.id.btn_csv_import_yyk).hide();
				$('#'+$.id.btn_csv_import_yyk).attr('disabled', 'disabled').hide();
				$('#'+$.id.btn_sel_change).hide();
				$('#'+$.id.btn_sel_change).attr('disabled', 'disabled').hide();
				$('#'+$.id.btn_sel_refer).on("click", $.pushChangeReport);
				$.initReportInfo("BW005", "冷凍食品 参照 企画一覧" ,'参照');



			}else{
				$($.id.buttons).show();
				// 各種遷移ボタン
				$('#'+$.id.btn_copy).on("click", $.pushChangeReport);
				$('#'+$.id.btn_new).on("click", $.pushChangeReport);
				$('#'+$.id.btn_sel_copy).on("click", $.pushChangeReport);
				$('#'+$.id.btn_sel_change).on("click", $.pushChangeReport);
				$('#'+$.id.btn_csv_import).on("click", $.pushChangeReport);
				$('#'+$.id.btn_sel_csverr).on("click", $.pushChangeReport);
				$('#'+$.id.btn_sel_refer).hide();
				$('#'+$.id.btn_sel_refer).attr('disabled', 'disabled').hide();
				$.initReportInfo("BW005", "冷凍食品　新規・変更　企画一覧" ,'新規・変更');
			}
//			$('#btn_csv_import_yyk').on("click", function(e){alert('現在CSV出力機能は停止中です。');});
//			$('#'+$.id.btn_csv).on("click",that.pushCsv_test);
			$('#'+$.id.btn_csv).on("click",function(e){alert('現在CSV出力機能は停止中です。');});
//			$('#'+$.id.btn_csv_import_yyk).on("click", $.pushCsv_test);

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
				    {field:'F1',	title:'販売開始日',						width: 100,halign:'center',align:'left'},
				    {field:'F2',	title:'部門',							width: 50,halign:'center',align:'left'},
					{field:'F3',	title:'名称',							width: 280,halign:'center',align:'left'},
					{field:'F4',	title:'割引率',							width: 60,halign:'center',align:'left'},
					{field:'F5',	title:'正規・カット区分',				width: 120,halign:'center',align:'left'},
					{field:'F6',	title:'ダミーコード',					width: 90,halign:'center',align:'left'},
					{field:'F7',	title:'ダミーコード名称',				width: 280,halign:'center',align:'left'}
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
			$.setJSONObject(sendJSON, 'reportYobi1', that.reportYobiInfo(), that.reportYobiInfo());		// 参照情報保持
			$.setJSONObject(sendJSON, 'callpage', that.name, that.name);								// 呼出し元レポート情報

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
				index = 2;
				childurl = href[index];

				// オブジェクト作成
				$.setJSONObject(sendJSON, $.id.txt_sel_bmncd,'', '');
				break;
			case $.id.btn_csv_import:
				// 転送先情報
				index = 4;
				childurl = href[index];

				// オブジェクト作成
				break;
			case $.id.btn_copy:
			case $.id.btn_sel_copy:
				if(!row){
					$.showMessage('E00008');
					return false;
				}

				// 転送先情報
				index = 2;
				childurl = href[index];

				// オブジェクト作成
				$.setJSONObject(sendJSON, $.id.txt_sel_bmncd, row.F1, row.F1);
				break;
			case $.id.btn_sel_csverr:

				// 転送先情報
				index = 5;
				childurl = href[index];

				break;
			case $.id.btn_sel_change:
				if(!row){
					$.showMessage('E00008');
					return false;
				}

				// 転送先情報
				index = 3;
				childurl = href[index];

				// オブジェクト作成
				$.setJSONObject(sendJSON, 'txt_hbstdt3', row.F1, row.F1);
				$.setJSONObject(sendJSON, $.id_inp.txt_bmncd, row.F2, row.F2);
				$.setJSONObject(sendJSON, $.id_inp.txt_meishokn, row.F3, row.F3);
				$.setJSONObject(sendJSON, 'txt_waribiki', row.F4, row.F4);
				$.setJSONObject(sendJSON, 'txt_seisi', row.F5, row.F5);
				$.setJSONObject(sendJSON, 'txt_dummycd2', row.F6, row.F6);
				$.setJSONObject(sendJSON, $.id_inp.txt_popkn, row.F7, row.F7);
				$.setJSONObject(sendJSON, $.id_inp.txt_hbstdt, row.F8, row.F8);
				$.setJSONObject(sendJSON, 'txt_hbstdt2', row.F12, row.F12);
				$.setJSONObject(sendJSON, 'txt_waribiki2', row.F9, row.F9);
				$.setJSONObject(sendJSON, 'txt_seisi2', row.F10, row.F10);
				$.setJSONObject(sendJSON, $.id_inp.txt_dummycd, row.F11, row.F11);
				break;
			case $.id.btn_sel_refer:
				if(!row){
					$.showMessage('E00008');
					return false;
				}

				// 転送先情報
				index = 3;
				childurl = href[index];

				// オブジェクト作成
				$.setJSONObject(sendJSON, $.id_inp.txt_bmncd, row.F2, row.F2);
				$.setJSONObject(sendJSON, $.id_inp.txt_meishokn, row.F3, row.F3);
				$.setJSONObject(sendJSON, 'txt_waribiki', row.F4, row.F4);
				$.setJSONObject(sendJSON, 'txt_seisi', row.F5, row.F5);
				$.setJSONObject(sendJSON, $.id_inp.txt_popkn, row.F7, row.F7);
				$.setJSONObject(sendJSON, $.id_inp.txt_hbstdt, row.F8, row.F8);
				$.setJSONObject(sendJSON, 'txt_waribiki2', row.F9, row.F9);
				$.setJSONObject(sendJSON, 'txt_seisi2', row.F10, row.F10);
				$.setJSONObject(sendJSON, $.id_inp.txt_dummycd, row.F11, row.F11);
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
		pushCsv_test : function(e){
			//
//			// TODO：仮
//			alert("現在チェックリスト出力機能は停止中です。");
//			return false;


			if ($(this).linkbutton('options').disabled)	return false;

			// レポート番号取得
			var reportno=$($.id.hidden_reportno).val();
			// レポート定義位置
			var reportNumber = $.getReportNumber(reportno);
			if (typeof(reportNumber) !== 'number') { alert("レポート定義が見つかりません。"); return false;}

			// JS情報取得
			var that = $.report[reportNumber];
			var id = $(this).attr('id');

			// チェック・確認処理
			var rtn = false;

			var row = $($.id.gridholder).datagrid("getSelected");

			// ② 画面に選択した1行の催し情報を出力する。何も選択しないと、エラー。
			if(!row){
				$.showMessage('E00008');
				return false;
			}



			if($.isFunction(that.outputFtpValidation)) { rtn = that.outputFtpValidation(id);}
			if(rtn){
				var func_ok = function(r){
					// セッションタイムアウト、利用時間外の確認
					var isTimeout = $.checkIsTimeout();
					if (! isTimeout) {
						// ログの書き込み
						$.ajax({
							url: $.reg.easy,
							type: 'POST',
							async: false,
							data: {
								"page"	: reportno ,
								"obj"	: id,
								"sel"	: new Date().getTime(),
								"userid": $($.id.hidden_userid).val(),
								"user"	: $($.id.hiddenUser).val(),
								"report": $($.id.hiddenReport).val(),
								"json"	: ""
							},
							success: function(json){
								that.outputFtpSuccess(id,reportno);
							}
						});
					}
					return true;
				};
				var message = "販売開始日"+row.F1+"、部門"+row.F2 + "、割引率"+row.F4 + "、正規カット"+row.F5 + "、ダミーコード"+row.F6;
				$.showMessage("EX1097", [message], func_ok);
				//$.showMessage("W20030", undefined, func_ok);
			}
		},
		outputFtp:function(e){
			//
//			// TODO：仮
			alert("現在チェックリスト出力機能は停止中です。");
			return false;


			if ($(this).linkbutton('options').disabled)	return false;

			// レポート番号取得
			var reportno=$($.id.hidden_reportno).val();
			// レポート定義位置
			var reportNumber = $.getReportNumber(reportno);
			if (typeof(reportNumber) !== 'number') { alert("レポート定義が見つかりません。"); return false;}

			// JS情報取得
			var that = $.report[reportNumber];
			var id = $(this).attr('id');

			// チェック・確認処理
			var rtn = false;

			if($.isFunction(that.outputFtpValidation)) { rtn = that.outputFtpValidation(id);}
			if(rtn){
				var func_ok = function(r){
					// セッションタイムアウト、利用時間外の確認
					var isTimeout = $.checkIsTimeout();
					if (! isTimeout) {
						// ログの書き込み
						$.ajax({
							url: $.reg.easy,
							type: 'POST',
							async: false,
							data: {
								"page"	: reportno ,
								"obj"	: id,
								"sel"	: new Date().getTime(),
								"userid": $($.id.hidden_userid).val(),
								"user"	: $($.id.hiddenUser).val(),
								"report": $($.id.hiddenReport).val(),
								"json"	: ""
							},
							success: function(json){
								that.outputFtpSuccess(id,reportno);
							}
						});
					}
					return true;
				};
				$.showMessage("W20030", undefined, func_ok);
			}
		},
		outputFtpValidation: function (){	// （必須）批准
			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = true;

			var that = this;

			var rt = true;

			// ① 部門：数値2桁。チェックリスト系のボタンを押す場合のみ必須
			/*var bmncd = $.getInputboxValue($('#'+$.id.SelBumon));
			if(bmncd == '-1'){
				// E20037	部門コードを選択してください。	 	0	 	E
				$.showMessage("E20037", undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_bmncd), true)});
				return false;
			}*/

			// 選択行
			/*var row = $($.id.gridholder).datagrid("getSelected");

			// ② 画面に選択した1行の催し情報を出力する。何も選択しないと、エラー。
			if(!row){
				$.showMessage('E00008');
				return false;
			}*/

			return rt;
		},
		outputFtpSuccess: function(id,reportno){

			var that = this;

			var row = $($.id.gridholder).datagrid("getSelected");


			var szHbsstdt	= row.F12;		// 販売開始日
			var szBmncd		= row.F2;		// 部門コード
			var szWaribiki	= row.F9;		// 割引率
			var szSeiki		= row.F10;		// 正規・カット区分
			var szDummycd	= row.F11;		// ダミーコード

			var fileName ="MDCR010";
			var datalen  = 251;

			var title = 'ファイル名：';
			var br = '<br>'

			var json = [{
				"callpage":that.name,
				"FILE":fileName,
				"DREQKIND":1,			// ≒SQL実行回数
				"REQLEN":datalen,
				"HBSSTDT":szHbsstdt,
				"BMNCD":szBmncd,
				"WARIBIKI":szWaribiki,
				"SEIKI":szSeiki,
				"DUMMY":szDummycd,
				"BTN":id
			}];

			// 処理時間計測用
			that.timeData = (new Date()).getTime();

			// Loading表示
			var msgCreate = '<font size="4px">　　送信中</font>';
			msgCreate += br+'<font size="2px">しばらくお待ちください</font>';
			msgCreate += br+br+'<font size="2px">'+title+fileName+'</font>';

			var panel = parent.$("#container");
			var msg=$("<div class=\"datagrid-mask-msg\" style=\"display:block;left:50%;\"></div>").html(msgCreate).appendTo(panel);
			msg._outerHeight(120);
			msg._outerWidth(200);
			msg.css({marginLeft:(-msg.outerWidth()/2),lineHeight:("25px")});

			$.ajax({
				url: $.reg.ftp,
				type: 'POST',
				async: false,
				data: {
					"page"	: reportno ,
					"obj"	: id,
					"sel"	: new Date().getTime(),
					"userid": $($.id.hidden_userid).val(),
					"user"	: $($.id.hiddenUser).val(),
					"report": $($.id.hiddenReport).val(),
					"json"	: JSON.stringify(json)
				},
				success: function(json){
					if (JSON.parse(json).length > 0) {
						$.removeMask();
						$.removeMaskMsg();

						// 正常終了の場合
						if (JSON.parse(json)[0].status==='0') {
							$.showMessage('IX1074',['',br,br+br+title+fileName]);
						} else if (JSON.parse(json)[0].code!=='530') {
							$.showMessage('EX1075',['',JSON.parse(json)[0].com,br+br+title+fileName]);
						} else {
							$.showMessage('EX1076',['',JSON.parse(json)[0].com,br+br+title+fileName]);
						}
					}
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