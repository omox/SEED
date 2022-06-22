/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_Reportx051',			// （必須）レポートオプションの確認
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
		dedefaultObjNum:	5,	// 初期化オブジェクト数
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

			// IFrame読み込み時
			$('#if').on('load', function (e) {
				try {
					var contents = $(this).contents();
					var data = contents.find('body')[0].innerHTML;
					var json = JSON.parse(data);
					that.queried = true;
					if(json.opts!=null){
						// ログ情報の格納
						$.post(
							$.reg.easy ,
							{
								"page"	: $($.id.hidden_reportno).val() ,
								"obj"	: $.id.btn_upload,
								"sel"	: new Date().getTime(),
								"userid": $($.id.hidden_userid).val(),
								"user"	: $($.id.hiddenUser).val(),
								"report": $($.id.hiddenReport).val(),
								"json"	: ""
							},
							function(json){}
						);
					}
					if (json.message[0]==="" || json.message[0]===null || json.message[0]===undefined) {
						$.showMessage("I00001");
					} else {
						$.showMessage(json.message[0].MSGCD,json.message[0].PRM);
					}
				}catch(exception){
					$.showMessage("E00014");
					console.log(exception.message);
				}

				$(".messager-window").find(".l-btn").eq(0).click();	// メッセージ[取込中...]を閉じる。
				$.removeMask();
			});
			$.setCheckboxInit2(that.jsonHidden, 'chk_nomaker', that);
			$.setCheckboxInit2(that.jsonHidden, 'chk_dmakercd', that);
			$.setCheckboxInit2(that.jsonHidden, 'chk_makercd', that);

			// 入力テキストボックス系
			var inputbox = Object.getOwnPropertyNames($.id_inp);
			for ( var sel in inputbox ) {
				if($('#'+$.id_inp[inputbox[sel]]).length > 0){
					$.setInputbox(that, reportno, $.id_inp[inputbox[sel]], isUpdateReport);
				}
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
			if(sendBtnid && sendBtnid.length > 0){
				that.sendBtnid = sendBtnid;
				$.reg.search = true;
			}
			$('#'+$.id.btn_upload).on("click", that.pushUpload);
			// ファイルテキスト
			$("#"+$.id.txt_file).on("change", function (e) {
				//var abc = $(this).val();
				$("#"+$.id.txt_file+"_").textbox('setValue', $(this).val());
				$("#"+$.id.btn_upload).click();
				return false;
			});

			if(that.reportYobiInfo()==='1'){
				$('#'+$.id.btn_sel_change).linkbutton('disable').attr('disabled', 'disabled').hide().attr('tabindex', -1);
				$('#'+$.id.btn_file).linkbutton('disable').attr('disabled', 'disabled').hide().attr('tabindex', -1);
				$('#'+$.id.btn_sel_refer).on("click", $.pushChangeReport);

			}else{
				$('#'+$.id.btn_sel_refer).linkbutton('disable').attr('disabled', 'disabled').hide().attr('tabindex', -1);
				$('#'+$.id.btn_sel_change).on("click", $.pushChangeReport);
			}
			// 各種遷移ボタン
			$('#'+$.id.btn_upload).hide();
			$.initReportInfo("MR001", "メーカーマスタ　一覧");
		},
		initCondition: function (){	// 条件初期値セット
			var that = this;
			// 初期化項目
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
		success: function(reportno, sortable, btnId){	// （必須）正処理
			if (sortable) sortable=1; else sortable=0;
			var that = this;

			// 検索実行
			var txt_makercd		= $.getJSONObject(this.jsonString, $.id_inp.txt_makercd).value;		// メーカーコード
			var txt_makerkn		= $.getJSONObject(this.jsonString, $.id_inp.txt_makerkn).value;		// メーカー名（漢字）
			var chk_nomaker		= $("input[id=chk_nomaker]:checked").val();							// メーカー名無し
			var chk_dmakercd	= $("input[id="+$.id.chk_dmakercd+"]:checked").val();				// 代表メーカー
			var chk_makercd		= $("input[id="+$.id.chk_makercd+"]:checked").val();				// メーカー

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
					report			:that.name,					// レポート名
					MAKERCD			:txt_makercd,
					MAKERKN			:txt_makerkn,
					CHK_NOMAKERCD	:chk_nomaker,
					CHK_DMAKERCD	:chk_dmakercd,
					CHK_MAKERCD		:chk_makercd,
					SENDBTNID		:that.sendBtnid,
					t				:(new Date()).getTime(),
					sortable		:sortable,
					sortName		:that.sortName,
					sortOrder		:that.sortOrder,
					rows			:0							// 表示可能レコード数
				},
				function(json){
					// 検索処理エラー判定
					//if($.searchError(json)) return false;
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

					// ログ出力
					$.log(that.timeData, 'loaded:');
				}
			);
		},
		pushUpload:function(){
			if ($(this).linkbutton('options').disabled)	return false;

			// レポート番号取得
			var reportno=$($.id.hidden_reportno).val();

			// レポート定義位置
			var reportNumber = $.getReportNumber(reportno);
			if (typeof(reportNumber) !== 'number') { alert("レポート定義が見つかりません。"); return false;}

			// フォーム情報取得
			$.report[reportNumber].getEasyUI();

			if ($.report[reportNumber].upValidation()) {
				// セッションタイムアウト、利用時間外の確認
				var isTimeout = $.checkIsTimeout();
				if (! isTimeout) {
					// マスク追加
					$.appendMask();
					$.messager.alert({
						title: "情報",
						msg: "取込中...",
						ok: false,
						cancel: false,
						icon:"info",
						width:500,
						zIndex:100000,

					});
					// $(".messager-window").find(".dialog-button messager-button").eq(0).hide();
					$(".dialog-button messager-button").css("height","0px");

					$('#'+$.id.txt_status).textbox('setValue', "取込中");
					$('#'+$.id.txt_upd_number).textbox('setValue', "");

					// パラメータをセット
					$($.id.uploadform).append('<input type="hidden" id="report" name="report" value="'+$.report[reportNumber].name+'">');

					// EasyUiの中身をパラメータとしてセット
					for(var i=0;i<$.report[reportNumber].jsonString.length;i++){
						var id = $.report[reportNumber].jsonString[i].id;
						var val = $.report[reportNumber].jsonString[i].value;

						$($.id.uploadform).append('<input type="hidden" name="'+id+'" value="'+val+'">');
					}

					// アップロード実行
					var frm = $($.id.uploadform)[0];
					frm.action = $.reg.upload;
					frm.submit();
				}
				$("#"+$.id.txt_file).val("");
				return true;
			} else {
				return false;
			}
		},
		upValidation: function (id){	// （必須）批准
			var that = this;

			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.uploadform).form('validate');
			// ファイルチェック
			if (rt == true) {
				var file	= $('#'+$.id.txt_file).val();	// ファイル
				if (!file.match(/\.(txt)$/i)){
					if(file==""){
						return false;
					}

					rt = false;
					$.showMessage("E11012",["ファイル種類","テキストファイルを選択してください。"]);
				}
			}

			// 入力エラーなしの場合に検索条件を格納
			if (rt == true) that.jsonString = that.jsonTemp.slice(0);

			// 入力チェック用の配列をクリア
			that.jsonTemp = [];

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
			// メーカーコード
			this.jsonTemp.push({
				id:		$.id_inp.txt_makercd,
				value:	$('#'+$.id_inp.txt_makercd).textbox('getValue'),
				text:	$('#'+$.id_inp.txt_makercd).textbox('getText')
			});
			// メーカー名（漢字）
			this.jsonTemp.push({
				id:		$.id_inp.txt_makerkn,
				value:	$('#'+$.id_inp.txt_makerkn).textbox('getValue'),
				text:	$('#'+$.id_inp.txt_makerkn).textbox('getText')
			});
			// メーカー名無し
			this.jsonTemp.push({
				id:		'chk_nomaker',
				value:	$('#chk_nomaker').is(':checked') ? $.id.value_on : $.id.value_off,
				text:	''
			});
			// 代表メーカー
			this.jsonTemp.push({
				id:		'chk_dmakercd',
				value:	$('#chk_dmakercd').is(':checked') ? $.id.value_on : $.id.value_off,
				text:	''
			});
			// メーカー
			this.jsonTemp.push({
				id:		'chk_makercd',
				value:	$('#chk_makercd').is(':checked') ? $.id.value_on : $.id.value_off,
				text:	''
			});
			// ボタンID
			this.jsonTemp.push({
				id:		"SENDBTNID",
				value:	this.sendBtnid,
				text:	this.sendBtnid
			});
			// SEQ
			this.jsonTemp.push({
				id:		$.id.txt_seq,
				value:	$('#'+$.id.txt_seq).val(),
				text:	$('#'+$.id.txt_seq).val()
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
					{field:'F1',	title:'代表区分',			width:  40,halign:'center',align:'center'},
					{field:'F2',	title:'メーカーコード',		width: 100,halign:'center',align:'left'},
					{field:'F3',	title:'メーカー名(漢字)',	width: 200,halign:'center',align:'left'},
					{field:'F4',	title:'代表メーカーコード',	width:  90,halign:'center',align:'left'},
					{field:'F5',	title:'JANコード',			width: 150,halign:'center',align:'left'},
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
		loadSuccessFunc:function(id, data){				// 画面遷移
			var that = this;
			// 画面遷移による検索以外の場合
			if(that.sendBtnid.length === 0){
				// 1件のみの場合、遷移
				if(that.pushBtnid===$.id.btn_search
				|| that.pushBtnid===$.id.btn_copy){
					if(data.total===1){
						setTimeout(function(){
							$(id).datagrid('selectRow', 0);
							that.changeReport(that.name, that.pushBtnid);
						},0);
					}
				}
			}else{
				// 初回以外は移動OKのため、初期化
				that.sendBtnid = "";
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
			$.setJSONObject(sendJSON, 'sendBtnid', btnId, $('#'+btnId).attr("title"));					// 実行ボタン情報保持
			$.setJSONObject(sendJSON, 'reportYobi1', that.reportYobiInfo(), that.reportYobiInfo());		// 参照情報保持
			$.setJSONObject(sendJSON, 'callpage', that.name, that.name);								// 呼出し元レポート情報

			// 戻る実行時用に現在の画面情報を保持する
			var states = $.getBackBaseJSON(that);
			// TODO 各種グリッド情報を設定
			var newrepinfos = $.getBackJSON(that, states,true);
			$.setJSONObject(sendJSON, 'repinfo', newrepinfos, '');

			// 選択行
			var row = $($.id.gridholder).datagrid("getSelected");

			// 実行ボタン別処理
			switch (btnId) {
			case $.id.btn_sel_change:
			case $.id.btn_sel_refer:
				sendMode = 1;

				if(!row){
					$.showMessage('E00008');
					return false;
				}

				if(row.F1 == "代表"){
					$.setJSONObject(sendJSON, $.id_inp.txt_makerkn, row.F3, row.F3);
					index = 4;

				}else{
					index = 3;
				}

				// 転送先情報
				childurl = href[index];

				// オブジェクト作成
				$.setJSONObject(sendJSON, $.id_inp.txt_makercd, row.F2, row.F2);
				$.setJSONObject(sendJSON, 'reportName', that.name, that.name);								// 遷移元画面名保持
				$.setJSONObject(sendJSON, 'reportYobi1', that.reportYobiInfo(), that.reportYobiInfo());		// 参照情報保持
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