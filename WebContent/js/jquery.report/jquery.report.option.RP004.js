/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_ReportRP004',			// （必須）レポートオプションの確認
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
		dedefaultObjNum:	6,	// 初期化オブジェクト数
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
		sendBtnid: "",						// （必須）呼出ボタンID情報
		focusRootId:"cc",					// （キー移動時必須）キー移動イベントのルートとなるパネルのID
		focusParentId:"",					// （キー移動時必須）現在フォーカスがあたっている項目の親となるパネルのID
		focusGridId:"",						// （キー移動時必須）現在フォーカスがあたっているDataGridのID
		editRowIndex:{},					// グリッド編集行保持
		baseData:[],						// 検索結果保持用
		updData:[],							// 検索結果保持用
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

//			// データ表示エリア初期化
//			that.setGrid($.id.gridholder, reportno);

			var isUpdateReport = true;

//			// 名称マスタ参照系
//			var meisyoSelect = Object.getOwnPropertyNames($.id_mei);
//			for ( var sel in meisyoSelect ) {
//				if($('#'+$.id_mei[meisyoSelect[sel]]).length > 0){
//					$.setMeisyoCombo(that, reportno, $.id_mei[meisyoSelect[sel]], isUpdateReport);
//				}
//			}
			// 編集可能データグリッドの共通処理設定
			// 編集エディターの機能拡張（非表示で参照用のオブジェクトが準備してある前提、参照オブジェクトはグリッドのフィールド名を元に判断）
			$.extendDatagridEditor();

			// 初期検索可能
			that.onChangeReport = true;

//			var count = 1;
			// 入力テキストボックス系
			var inputbox = Object.getOwnPropertyNames($.id_inp);
			for ( var sel in inputbox ) {
				if($('#'+$.id_inp[inputbox[sel]]).length > 0){
					$.setInputbox(that, reportno, $.id_inp[inputbox[sel]], isUpdateReport);
//					count++;
				}
			}
			$.setCheckboxInit(that.jsonHidden, 'chk_rinji', that);


			// ランク別数量
			that.setEditableGrid(that, reportno, $.id.grd_ranksuryo+'_list');

//			// 初期表示時に検索処理を通らない為フラグをtrueに
//			that.queried = true;

			// 初期化終了
			this.initializes =! this.initializes;

//			// 初期表示処理
//			that.onChangeReport = true;
//			$.initialDisplay(that);

			// Load処理回避
			//$.tryChangeURL(null);

//			$.initialSearch(that);

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

//			if(that.reportYobiInfo()==='1'){
//				$.setInputBoxDisable($($.id.hiddenChangedIdx));
//			}

			// 各種遷移ボタン
//			$('#'+$.id.btn_cancel).on("click", $.pushChangeReport);
//			$('#'+$.id.btn_upd).on("click", $.pushChangeReport);
//			$('#'+$.id.btn_del).on("click", $.pushChangeReport);

			if(that.sendBtnid===$.id.btn_new){
				$('#'+$.id.btn_cancel).on("click", $.pushChangeReport);
//				$('#'+$.id.btn_upd).on("click", $.pushChangeReport);
				$("#"+$.id.btn_del).linkbutton('disable');
				$("#"+$.id.btn_del).attr('tabindex', -1).hide();
				$.setInputBoxDisable($("#"+$.id_inp.txt_bmncd));
				$.setInputBoxDisable($("#"+$.id.chk_rinji));
				$.setInputBoxDisable($("#"+$.id_inp.txt_moyscd));
				$.setInputBoxDisable($("#"+$.id_inp.txt_rank));
				$.initReportInfo("RP004", "数量パターンマスタ　ランク別数量　新規", "新規");

			}else if(that.sendBtnid===$.id.btn_sel_change){
				$('#'+$.id.btn_cancel).on("click", $.pushChangeReport);
//				$('#'+$.id.btn_upd).on("click", $.pushChangeReport);
//				$('#'+$.id.btn_del).on("click", $.pushChangeReport);
				$.setInputBoxDisable($("#"+$.id_inp.txt_bmncd));
				$.setInputBoxDisable($("#"+$.id.chk_rinji));
				$.setInputBoxDisable($("#"+$.id_inp.txt_moyscd));
				$.setInputBoxDisable($("#"+$.id_inp.txt_rank));
				$.setInputBoxDisable($("#"+$.id_inp.txt_sryptnno));
				$.initReportInfo("RP004", "数量パターンマスタ　ランク別数量　変更", "変更");

			}else if (that.sendBtnid===$.id.btn_sel_refer) {
				$('#'+$.id.btn_cancel).on("click", $.pushChangeReport);
				$("#"+$.id.btn_upd).linkbutton('disable');
				$("#"+$.id.btn_upd).attr('tabindex', -1).hide();
				$("#"+$.id.btn_del).linkbutton('disable');
				$("#"+$.id.btn_del).attr('tabindex', -1).hide();
				$.setInputBoxDisable($("#"+$.id_inp.txt_bmncd));
				$.setInputBoxDisable($("#"+$.id.chk_rinji));
				$.setInputBoxDisable($("#"+$.id_inp.txt_moyscd));
				$.setInputBoxDisable($("#"+$.id_inp.txt_sryptnno));
				$.setInputBoxDisable($("#"+$.id_inp.txt_sryptnkn));
				$.setInputBoxDisable($("#"+$.id_inp.txt_rank));
				$.setInputBoxDisable($("#"+$.id_inp.txt_suryo));
				$.initReportInfo("RP004", "数量パターンマスタ　ランク別数量　参照", "参照");
//				$.setInputBoxDisable($($.id.hiddenChangedIdx));
			}
//			// 変更
//			$($.id.hiddenChangedIdx).val('');
		},
		initCondition: function (){	// 条件初期値セット
			var that = this;
			// 初期化項目
		},
		clear:function(){
			// 隠し情報初期化
			$($.id.hiddenChangedIdx).val("");						// 変更行Index
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
			var chk_rinji		= $('#chk_rinji').is(':checked') ? $.id.value_on : $.id.value_off;	// 臨時
			var txt_moyscd		= $.getJSONObject(this.jsonString, $.id_inp.txt_moyscd).value;		// 催しコード
			var txt_sryptnno	= $.getJSONObject(this.jsonString, $.id_inp.txt_sryptnno).value;	// 数量パターンNo.

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
//			$.appendMaskMsg();

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,		// レポート名
					BMNCD:			txt_bmncd,		// 部門
					RINJI:			chk_rinji,		// 臨時
					MOYSCD:			txt_moyscd,		// 催しコード
					SRYPTNNO:		txt_sryptnno,	// 数量パターンNo.
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

					var limit = 1;
					var size = JSON.parse(json)["total"];
					if(size < limit){
						$.showMessage('I30000');
					}

					// ログ出力
					$.log(that.timeData, 'query:');

					var opts = JSON.parse(json).opts

					// 検索結果を保持
					that.baseData = JSON.parse(json).rows;

					// メインデータ表示
					that.setData(that.baseData, opts);
					that.queried = true;

					// 状態保存
					$.saveState2(reportno, that.getJSONString());

					// ログ出力
					$.log(that.timeData, 'loaded:');
				}
			);
		},
		setGridData: function (data, target){
			var that = this;

			return true;
		},
		updValidation: function (){	// （必須）批准
			var that = this;
			var row = $('#'+$.id.grd_ranksuryo+'_list').datagrid("getSelected");
			var rowIndex = $('#'+$.id.grd_ranksuryo+'_list').datagrid("getRowIndex", row);
			$('#'+$.id.grd_ranksuryo+'_list').datagrid('endEdit',rowIndex);

			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform).form('validate');
			var txt_sryptnno			= $('#'+$.id_inp.txt_sryptnno).textbox('getValue');			// 数量パターンNo.
			var txt_sryptnkn			= $('#'+$.id_inp.txt_sryptnkn).textbox('getValue');			// 数量パターン名称
			var chk_rinji 				= $('#chk_rinji').is(':checked') ? $.id.value_on : $.id.value_off;	// 臨時
			// 入力チェック
			if (rt) {
				if(that.sendBtnid===$.id.btn_new && !txt_sryptnno){
					$.showMessage('EX1067', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_sryptnno), true)});
					rt = false;
				}
			}
			if (rt) {
				if (!txt_sryptnkn) {
					$.showMessage('EX1083', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_sryptnkn), true)});
					rt = false;
				}
			}
			if (rt) {
				if (chk_rinji==='0' && Number(txt_sryptnno) >= 900) {
					$.showMessage('E20101', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_sryptnno), true)});
					rt = false;
				}
			}
			if (rt) {
				if (chk_rinji==='1' && Number(txt_sryptnno) < 900) {
					$.showMessage('E20102', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_sryptnno), true)});
					rt = false;
				}
			}
			return rt;
		},
		updSuccess: function(id){	// validation OK時 の update処理
			var that = this;

			var targetDatas = [{}];
			$('#'+that.focusRootId).find('[col^=F]').each(function(){
				var col = $(this).attr('col');
				targetDatas[0][col] = $.getInputboxValue($(this));
			});
			// 入力データ：ランク数量
			var targetRowsRanksuryo = that.getGridData($.id.grd_ranksuryo+'_list');

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
//			$.appendMaskMsg();

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,								// レポート名
					action:			$.id.action_update,						// 実行処理情報
					obj:			id,										// 実行オブジェクト
					SENDBTNID:		that.sendBtnid,
					DATA:			JSON.stringify(targetDatas),			// 更新対象情報
					DATA_RANKSURYO:	JSON.stringify(targetRowsRanksuryo),	// 更新対象情報
					t:				(new Date()).getTime()
				},
				function(data){
					// 検索処理エラー判定
					if($.updError(id, data)) return false;

					var afterFunc = function(){
						// 初期化
						that.clear();
						that.changeReport(that.name, $.id.btn_back);
					};
					$.updNormal(data, afterFunc);

					// ログ出力
					$.log(that.timeData, 'loaded:');
				}
			);
		},
		delValidation: function (){	// （必須）批准
			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = true;
			// 入力チェック用の配列をクリア
			that.jsonTemp = [];

			return rt;
		},
		delSuccess: function(id){
			var that = this;

			var targetDatas = [{}];
			$('#'+that.focusRootId).find('[col^=F]').each(function(){
				var col = $(this).attr('col');
				targetDatas[0][col] = $.getInputboxValue($(this));
			});

//			// 入力データ：率パターン
//			var targetRowsRanksuryo = that.getGridData($.id.grd_ranksuryo+'_list');

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			//$.appendMaskMsg();

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,		// レポート名
					action:			$.id.action_delete,	// 実行処理情報
					obj:			id,								// 実行オブジェクト
					//IDX:			$($.id.hiddenChangedIdx).val(),	// 更新対象Index
					//DATA:			JSON.stringify(targetRows),		// 更新対象情報
					DATA:			JSON.stringify(targetDatas),	// 更新対象情報
					t:				(new Date()).getTime()
				},
				function(data){
					// 検索処理エラー判定
					if($.delError(id, data)) return false;

					var afterFunc = function(){
						// 初期化
						that.clear();
						that.changeReport(that.name, $.id.btn_back);
					};
					$.delNormal(data, afterFunc);

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
			// 数量パターンNo.
			this.jsonTemp.push({
				id:		$.id_inp.txt_sryptnno,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_sryptnno),
				text:	''
			});
			// 数量パターンNo.
			this.jsonTemp.push({
				id:		$.id_inp.txt_sryptnno,
				value:	$('#'+$.id_inp.txt_sryptnno).textbox('getValue'),
				text:	$('#'+$.id_inp.txt_sryptnno).textbox('getText')
			});
			// 数量パターン名称
			this.jsonTemp.push({
				id:		$.id_inp.txt_sryptnkn,
				value:	$('#'+$.id_inp.txt_sryptnkn).textbox('getValue'),
				text:	$('#'+$.id_inp.txt_sryptnkn).textbox('getText')
			});
			// ランク
			this.jsonTemp.push({
				id:		$.id_inp.txt_rank,
				value:	$('#'+$.id_inp.txt_rank).textbox('getValue'),
				text:	$('#'+$.id_inp.txt_rank).textbox('getText')
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
		setEditableGrid: function(that, reportno, id){		// データ表示
			that.editRowIndex[id] = -1;
			var index = -1;
//			var targetId = $.id_inp.txt_rank;
//			var check = $('#'+targetId).attr("check") ? JSON.parse('{'+$('#'+targetId).attr("check")+'}'): JSON.parse('{}');	// コードのcheck要素を取得
//			var formatterLPad = function(value){
//				return $.getFormatLPad(value, check.maxlen);
//			};
			$('#'+id).datagrid({
				url:$.reg.easy,
				columns:[[
							{field:'RANK',	title:'ランク',	width: 70 ,halign:'center',align:'left'},
							{field:'SURYO',	title:'数量',	width: 70 ,halign:'center',align:'right',editor:{type:'numberbox'}},
						]],
				onBeforeLoad:function(param){
					index = -1;
					var sendBtnid = that.sendBtnid;
					var txt_bmncd = $.getInputboxValue($('#'+$.id_inp.txt_bmncd));
					var chk_rinji = $('#chk_rinji').is(':checked') ? $.id.value_on : $.id.value_off;			// 臨時
					var txt_moyscd = $.getInputboxValue($('#'+$.id_inp.txt_moyscd));
					var txt_sryptnno = $.getInputboxValue($('#'+$.id_inp.txt_sryptnno));
					var json = [{"callpage":"Out_ReportRP004","SENDBTNID":sendBtnid,"BMNCD":txt_bmncd,"RINJI":chk_rinji,"MOYSCD":txt_moyscd,"SRYPTNNO":txt_sryptnno}];
					// 情報設定
					param.page		=	reportno;
					param.obj		=	id;
					param.sel		=	(new Date()).getTime();
					param.target	=	id;
					param.action	=	$.id.action_init;
					param.json		=	JSON.stringify(json);
					param.datatype	=	"datagrid";
					param.report = that.name;
				},
				onLoadSuccess:function(data){},
				onClickRow: function(index,field){$.clickEditableDatagridCell(that,id, index)},
				onBeginEdit:function(index,row){$.beginEditDatagridRow(that,id, index, row)},
				onEndEdit: function(index,row,changes){$.endEditDatagridRow(that, id, index, row)}
			});
		},
		getGridData: function (target){

			var targetRows= [];

			if(target===undefined || target===$.id.grd_ranksuryo+'_list'){
				var rowsRanksuryo= $('#'+$.id.grd_ranksuryo+'_list').datagrid('getRows');
				for (var i=0; i<rowsRanksuryo.length; i++){
					if(rowsRanksuryo[i]["RANK"] == "" || rowsRanksuryo[i]["RANK"] == null ){

					}else{
						var rowDate = {
								F1	 : rowsRanksuryo[i]["RANK"],
								F2	 : rowsRanksuryo[i]["SURYO"],
							};
						targetRows.push(rowDate);
					}
				}
			}
			return targetRows;
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
			$.setJSONObject(sendJSON, 'callpage', that.name, that.name);								// 呼出し元レポート情報

			// 戻る実行時用に現在の画面情報を保持する
			var states = $.getBackBaseJSON(that);
			var newrepinfos = $.getBackJSON(that, states, false);
			$.setJSONObject(sendJSON, 'repinfo', newrepinfos, '');

			// 呼出別処理
			switch (btnId) {
			case $.id.btn_back:
			case $.id.btn_cancel:
				// 元画面情報
//				var callpage = $.getJSONValue(that.jsonHidden, "callpage");
				// 転送先情報
				index = 6;
//				if(that.reportYobiInfo()==='1'){
//					index = 6;
//				}
				for (var i = 0; i < newrepinfos.length; i++) {
					var callpage = newrepinfos[i].id;
					if(callpage==='Out_ReportRP003'){
						index = 6;
					}
				}
				sendMode = 2;
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