/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_ReportHT002',			// （必須）レポートオプションの確認
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
		onChangeReport: false,
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
		grd_before_data:[],					// グリッド情報:変更前データ
		oldBmn:"",
		oldDai:"",
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

			// 編集可能データグリッドの共通処理設定
			// 編集エディターの機能拡張（非表示で参照用のオブジェクトが準備してある前提、参照オブジェクトはグリッドのフィールド名を元に判断）
			$.extendDatagridEditor(that);

			// 初期検索可能
			that.onChangeReport = false;

			// 入力テキストボックス系
			var inputbox = Object.getOwnPropertyNames($.id_inp);
			for ( var sel in inputbox ) {
				if($('#'+$.id_inp[inputbox[sel]]).length > 0){
					$.setInputbox(that, reportno, $.id_inp[inputbox[sel]], isUpdateReport);
				}
			}

			// 処理日付の曜日を取得
			$.getsetInputboxData(reportno, $.id.txt_shoridtweek, [{}], $.id.action_init);

			// 週№期間取得
			$.getsetInputboxData(reportno, $.id.txt_shunoperiod, [{}], $.id.action_init);

			// 部門・大分類・中分類
			this.setBumon(reportno, $.id.SelBumon);
			this.setDaiBun(reportno, $.id.SelDaiBun);
			this.setChuBun(reportno, $.id.SelChuBun);

			// 定量
			that.setEditableGrid(that, reportno, $.id.gridholder);

			$.winHT005.init(that);	// 入力商品一覧

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

			$.initReportInfo("HT002", "正規商品指定");

			// 各種遷移ボタン
			$('#'+$.id.btn_cancel).on("click", $.pushChangeReport);

			// クリックされたボタンのIDを保持
			$('#'+$.id.btn_del).on("click", function(){that.clickBtnid = $.id.btn_del});
			$('#'+$.id.btn_upd).on("click", function(){that.clickBtnid = $.id.btn_upd});
			$('#'+$.id.btn_clear).on("click", function(){that.clickBtnid = $.id.btn_clear});

			// 全体処理
			if(that.reportYobiInfo()==='1'){
				$.setInputBoxDisable($($.id.hiddenChangedIdx));
			}
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
			if (rt == true) that.jsonString = that.jsonTemp.slice(0);
			// 入力チェック用の配列をクリア
			that.jsonTemp = [];

			// 検索実行
			var sel_bumon	= $.getJSONObject(this.jsonString, $.id.SelBumon).value;		// 部門
			var txt_shncd	= $.getJSONObject(this.jsonString, $.id_inp.txt_shncd).value;	// 商品コード

			if (txt_shncd===null || txt_shncd==="" || txt_shncd===undefined) {
				if (sel_bumon==='-1' || (sel_bumon===null || sel_bumon==="" || sel_bumon===undefined)) {
					$.showMessage('EX1098');
					return false;
				}
			}

			return rt;
		},
		success: function(reportno, sortable){	// （必須）正処理

			if (sortable) sortable=1; else sortable=0;
			var that = this;

			// 検索実行
			var txt_shncd	= $.getJSONObject(this.jsonString, $.id_inp.txt_shncd).value;	// 商品コード
			var sel_bumon	= $.getJSONObject(this.jsonString, $.id.SelBumon).value;		// 部門
			var sel_daibun	= $.getJSONObject(this.jsonString, $.id.SelDaiBun).value;		// 大分類
			var sel_chubun	= $.getJSONObject(this.jsonString, $.id.SelChuBun).value;		// 中分類
			var seltenpo	= parseInt($.getJSONObject(this.jsonInit, 'SelTenpo').value);	// ログイン店舗

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$.appendMaskMsg();

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,		// レポート名
					SHNCD:			txt_shncd,
					BUMON:			sel_bumon,
					DAIBUN:			sel_daibun,
					CHUBUN:			sel_chubun,
					TEN:			seltenpo,
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

					var size = JSON.parse(json)["total"];
					if(size == 0){
						$.showMessage('E11003');
					}

					// 検索データ（想定）
					that.gridData = JSON.parse(json).rows;
					that.gridTitle = JSON.parse(json).titles;

					var opts = JSON.parse(json).opts

					// メインデータ表示
					that.setData(that.gridData, opts);
					that.queried = true;

					// Load処理回避
					$.tryChangeURL($.reg.jqeasy);

					// グリッド再描画（easyui 1.4.2 対応）
					$($.id.gridholder).datagrid('load', {} );
					$.removeMask();

					// 基本入力初期値保持　データを保持
					if (that.clickBtnid===$.id.btn_clear) {
						var gridData = that.getGridData('#gridholder');
						that.setGridData(gridData, '#gridholder');
					} else {
						var Data = that.getGridData('data');
						that.setGridData(Data, 'data');
					}

					$($.id.hiddenChangedIdx).val("");

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
			if(!rt){
				$.addErrState(that, $('.validatebox-invalid').eq(0), false);
				return rt;
			}

			// 処理日付が水曜日の場合登録処理はできません
			if ($.getInputboxValue($("#"+$.id.txt_shoridtweek))==='4'){
				$.showMessage('EX1038');
				return false;
			}

			// TODO: 背景を赤くする対応を追加
			//var msgid = that.checkInputboxFunc($.id_inp.txt_hstengpcd);
			var msgid = null;
			if(msgid !==null){
				$.showMessage(msgid);
				return false;
			}

			return rt;
		},
		updSuccess: function(id){	// validation OK時 の update処理
			var that = this;

			// 商品一覧のデータを取得
			var targetRowsTenSu = that.getMergeGridDate($.id.gridholder);
			var week = $.getInputboxValue($("#"+$.id.txt_shoridtweek));						// 処理曜日
			var seltenpo	= parseInt($.getJSONObject(this.jsonInit, 'SelTenpo').value);	// ログイン店舗

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
					DATA_TENSU:		JSON.stringify(targetRowsTenSu),	// 更新対象情報
					TEN:			seltenpo,							// ログイン店
					WEEK:			week,								// 曜日
					t:				(new Date()).getTime()
				},
				function(data){
					// 検索処理エラー判定
					if($.updError(id, data)) {
						$.removeMaskMsg(); return false;
					}

					var afterFunc = function(){
						// 初期化
						that.getEasyUI();
						that.jsonString = that.jsonTemp.slice(0);
						that.clear();
						that.changeReport(that.name, 'btn_return');
					};
					$.updNormal(data, afterFunc);
					$.removeMaskMsg();

					// ログ出力
					$.log(that.timeData, 'loaded:');
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
			// 商品コード
			this.jsonTemp.push({
				id:		$.id_inp.txt_shncd,
				value:	$('#'+$.id_inp.txt_shncd).textbox('getValue'),
				text:	$('#'+$.id_inp.txt_shncd).textbox('getText')
			});
			// 部門
			this.jsonTemp.push({
				id:		$.id.SelBumon,
				value:	$('#'+$.id.SelBumon).combobox('getValue'),
				text:	$('#'+$.id.SelBumon).combobox('getText')
			});
			// 大分類
			this.jsonTemp.push({
				id:		$.id.SelDaiBun,
				value:	$('#'+$.id.SelDaiBun).combobox('getValue'),
				text:	$('#'+$.id.SelDaiBun).combobox('getText')
			});
			// 中分類
			this.jsonTemp.push({
				id:		$.id.SelChuBun,
				value:	$('#'+$.id.SelChuBun).combobox('getValue'),
				text:	$('#'+$.id.SelChuBun).combobox('getText')
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
		getComboErr: function (obj,editable,newValue,oldValue) {
			var data = obj.combobox('getData');

			if (!obj.hasClass('datagrid-editable-input')) {
				if (!$.setComboReload(obj,true) && !editable) {
					$.showMessage("E11302",["入力値"],function () {$.addErrState(this,obj,false)});
					obj.combobox('reload');
					obj.combobox('hidePanel');
				} else if ($.isEmptyVal(newValue)) {
					obj.combobox('setValue',obj.combobox('getData')[0].VALUE);
				} else if ($.isEmptyVal(oldValue)) {
					if (obj.next().find('[tabindex=1]').length===1) {
						obj.combo("textbox").focus();
					}
				}
			}
		},
		getChange: function (id) {
			var that = this;
			var newVal = $.getInputboxValue($('#'+id))*1;
			var oldVal = id===$.id.SelBumon ? that.oldBmn:that.oldDai;

			if (!$.isEmptyVal(oldVal) && oldVal===newVal) {
				return false;
			} else {
				return true;
			}
		},
		setBumon: function(reportno, id){		// 部門
			var that = this;
			var idx = -1;
			var onChange=false;
			var onPanel=false;

			// フォーカスアウトのタイミングの動作
			$('#'+id).next().on('focusout', function(e){

				// 変更があったか
				if (!that.getChange(id)) {
					return false;
				} else {
					that.oldBmn=$.getInputboxValue($('#'+id))*1;
				}

				var obj = $(this).prev();

				if (!$.setComboReload(obj,false)) {
					obj.combobox('reload');
				}
			});

			$('#'+id).combobox({
				panelWidth:250,
				url:$.reg.easy,
				required: false,
				editable: true,
				autoRowHeight:false,
				valueField:'VALUE',
				textField:'TEXT',
				multiple :false,
				prompt: '',
				icons: [],
				keyHandler: {
					up: $.fn.combobox.defaults.keyHandler.up,
					down: $.fn.combobox.defaults.keyHandler.down,
					left: $.fn.combobox.defaults.keyHandler.left,
					right: $.fn.combobox.defaults.keyHandler.right,
					enter: function(e){
						$('#'+id).combobox('hidePanel');
						e.preventDefault();
					},
					query: $.fn.combobox.defaults.keyHandler.query
				},
				onBeforeLoad:function(param){
					idx = -1;
					// 情報設定
					var json = [{}];
					param.page		=	reportno;
					param.obj		=	id;
					param.sel		=	(new Date()).getTime();
					param.target	=	id;
					param.action	=	$.id.action_init;
					param.json		=	JSON.stringify(json);
					param.datatype	=	"combobox";
				},
				onLoadSuccess:function(data){
					// 初期化
					var val = null;
					var init = $.getJSONValue(that.jsonHidden, id);
					if (that.initedObject && $.inArray(id, that.initedObject) < 0){
						that.initedObject.push(id);
						for (var i=0; i<data.length; i++){
							if (data[i].VALUE == init){
								val = init;
								break;
							}
						}
					}
					if (val === null && data.length>0){
						val = data[0].VALUE;
					}
					if (val){
						$('#'+id).combobox('setValue', val);
					}
					idx = 1;
					// ログ出力
					$.log(that.timeData, id+' init:');
					that.onChangeFlag = false;
					// 大分類
					if ($.isEmptyVal(init)) {
						that.tryLoadMethods('#'+$.id.SelDaiBun);
					}
				},
				onShowPanel: function(){
					$.setScrollComboBox(id);
					that.onChangeFlag=false;
					onChange=false;
					onPanel=true;
				},
				onHidePanel: function(){
					if (onChange){
						// 変更があったか
						if (that.getChange(id)) {
							// 大分類
							that.tryLoadMethods('#'+$.id.SelDaiBun);
						};
					}else{
						that.onChangeFlag=true;
					}
					onChange=false;
					onPanel=false;
				},
				onChange:function(newValue, oldValue, obj){

					// 変更があったか
					if (!that.getChange(id)) {
						return false;
					};

					if(obj===undefined){obj = $(this);}

					if(idx > 0 && that.onChangeFlag){
						// 大分類
						that.tryLoadMethods('#'+$.id.SelDaiBun);
					}
					onChange=true;
					that.getComboErr(obj,false,newValue,oldValue);
				}
			});
		},
		setDaiBun: function(reportno, id){		// 大分類
			var that = this;
			var idx = -1;
			var onChange=false;
			var onPanel=false;

			// フォーカスアウトのタイミングの動作
			$('#'+id).next().on('focusout', function(e){
				// 変更があったか
				if (!that.getChange(id)) {
					return false;
				} else {
					that.oldDai=$.getInputboxValue($('#'+id))*1;
				}

				var obj = $(this).prev();

				if (!$.setComboReload(obj,false)) {
					obj.combobox('reload');
				}
			});

			$('#'+id).combobox({
				panelWidth:250,
				url:$.reg.easy,
				required: false,
				editable: true,
				autoRowHeight:false,
				valueField:'VALUE',
				textField:'TEXT',
				multiple :false,
				prompt:'',
				icons:[],
				keyHandler: {
					up: $.fn.combobox.defaults.keyHandler.up,
					down: $.fn.combobox.defaults.keyHandler.down,
					left: $.fn.combobox.defaults.keyHandler.left,
					right: $.fn.combobox.defaults.keyHandler.right,
					enter: function(e){
						$('#'+id).combobox('hidePanel');
						e.preventDefault();
					},
					query: $.fn.combobox.defaults.keyHandler.query
				},
				onBeforeLoad:function(param){
					idx = -1;
					// 初期化しない
					if (that.initializes) return false;
					// 情報設定
					var json = [{
						BUMON: $('#'+$.id.SelBumon).combobox('getValue')
					}];

					param.page		=	reportno;
					param.obj		=	id;
					param.sel		=	(new Date()).getTime();
					param.target	=	id;
					param.action	=	$.id.action_init;
					param.json		=	JSON.stringify(json);
					param.datatype	=	"combobox";
				},
				onLoadSuccess:function(data){
					// 初期化
					var val = null;
					if (that.initedObject && $.inArray(id, that.initedObject) < 0){
						that.initedObject.push(id);
						var init = $.getJSONValue(that.jsonHidden, id);
						for (var i=0; i<data.length; i++){
							if (data[i].VALUE == init){
								val = init;
								break;
							}
						}
					}
					if (val === null && data.length>0){
						val = data[0].VALUE;
					}
					if (val){
						$('#'+id).combobox('setValue', val);
					}
					idx = 1;
					// ログ出力
					$.log(that.timeData, id+' init:');
					that.onChangeFlag = false;
					$.ajaxSettings.async = true;
					// 中分類
					that.tryLoadMethods('#'+$.id.SelChuBun);
				},
				onShowPanel: function(){
					$.setScrollComboBox(id);
					that.onChangeFlag=false;
					onChange=false;
					onPanel=true;
				},
				onHidePanel: function(){
					if (onChange){
						// 変更があったか
						if (that.getChange(id)) {
							// 中分類
							that.tryLoadMethods('#'+$.id.SelChuBun);
						};
					}else{
						that.onChangeFlag=true;
					}
					onChange=false;
					onPanel=false;
				},
				onChange:function(newValue, oldValue, obj){
					// 変更があったか
					if (!that.getChange(id)) {
						return false;
					}
					if(obj===undefined){obj = $(this);}

					if(idx > 0 && that.onChangeFlag){
						// 上位変更時、下位更新は常に同期
						$.ajaxSettings.async = false;
						that.onChangeFlag = false;
						// 中分類
						that.tryLoadMethods('#'+$.id.SelChuBun);
					}
					onChange=true;
					that.getComboErr(obj,false,newValue,oldValue);
				}
			});
		},
		setChuBun: function(reportno, id){		// 中分類
			var that = this;
			var idx = -1;

			// フォーカスアウトのタイミングの動作
			$('#'+id).next().on('focusout', function(e){
				var obj = $(this).prev();

				if (!$.setComboReload(obj,false)) {
					obj.combobox('reload');
				}
			});

			$('#'+id).combobox({
				panelWidth:250,
				url:$.reg.easy,
				required: false,
				editable: true,
				autoRowHeight:false,
				valueField:'VALUE',
				textField:'TEXT',
				multiple :false,
				prompt: '',
				icons:[],
				keyHandler: {
					up: $.fn.combobox.defaults.keyHandler.up,
					down: $.fn.combobox.defaults.keyHandler.down,
					left: $.fn.combobox.defaults.keyHandler.left,
					right: $.fn.combobox.defaults.keyHandler.right,
					enter: function(e){
						$('#'+id).combobox('hidePanel');
						e.preventDefault();
					},
					query: $.fn.combobox.defaults.keyHandler.query
				},
				onBeforeLoad:function(param){
					idx = -1;
					// 初期化しない
					if (that.initializes) return false;

					// 情報設定
					var json = [{
						BUMON: $('#'+$.id.SelBumon).combobox('getValue'),
						DAI_BUN: $('#'+$.id.SelDaiBun).combobox('getValue')
					}];

					param.page		=	reportno;
					param.obj		=	id;
					param.sel		=	(new Date()).getTime();
					param.target	=	id;
					param.action	=	$.id.action_init;
					param.json		=	JSON.stringify(json);
					param.datatype	=	"combobox";
				},
				onLoadSuccess:function(data){
					// 初期化
					var val = null;
					if (that.initedObject && $.inArray(id, that.initedObject) < 0){
						that.initedObject.push(id);
						var init = $.getJSONValue(that.jsonHidden, id);
						for (var i=0; i<data.length; i++){
							if (data[i].VALUE == init){
								val = init;
								break;
							}
						}
					}
					if (val === null && data.length>0){
						val = data[0].VALUE;
					}
					if (val){
						$('#'+id).combobox('setValue', val);
					}
					idx = 1;
					// ログ出力
					$.log(that.timeData, id+' init:');
					that.onChangeFlag = true;
					$.ajaxSettings.async = true;
//					// 検索ボタン有効化
//					$.setButtonState('#'+$.id.btn_search, true, id);
					// 初期表示検索処理
					$.initialSearch(that);
				},
				onShowPanel:function(){
					$.setScrollComboBox(id);
				}
				,onChange:function(newValue, oldValue,obj){
					if(obj===undefined){obj = $(this);}
					that.getComboErr(obj,false,newValue,oldValue);
				}
			});
		},
		/**
		 * 中分類（中分類が利用不可の場合、すべて）変換
		 */
		convertBumonChuBun: function(value){
			// 中分類（中分類が利用不可の場合、すべて）
			if ($('#'+$.id.SelChuBun).combobox('options').disabled){
				value = ['-1'];
			}
			return value;
		},
		setEditableGrid: function(that, reportno, id){		// データ表示
			that.editRowIndex['gridholder'] = -1;
			var index = -1;
			var columns = that.getGridColumns(that, id);

			// ページサイズ定義取得
			var pageList = $.fn.pagination.defaults.pageList;
			var pageSize = $.getJSONValue(this.jsonHidden, $.id.pageSize);
			if (pageSize==="") pageSize=$.fn.pagination.defaults.pageSize;
			//pageSize = $.getDefaultPageSize(pageSize, pageList);
			pageSize = 50;

			$(id).datagrid({
				columns:columns,
				rownumbers:false,
				view:scrollview,
				pageSize:pageSize,
				pageList:pageList,
				onBeforeLoad:function(param){
					param.report = that.name;
				},
				onLoadSuccess:function(data){
					var gridData = that.getGridData('#gridholder');
					that.setGridData(gridData, '#gridholder');
				},
				onClickRow: function(index,field){$.clickEditableDatagridCell(that,'gridholder', index)},
				onBeginEdit:function(index,row){$.beginEditDatagridRow(that,'gridholder', index, row)},
				onEndEdit: function(index,row,changes){$.endEditDatagridRow(that, 'gridholder', index, row)},
				onBeforeEdit:function(index,row){
					if(row.F16==='3'&&row.F17==='3'&&row.F18==='3'&&row.F19==='3'&&row.F20==='3'&&row.F21==='3'&&row.F22==='3'){
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
					var col1 = $(id).datagrid('getColumnOption', 'F4');
					var col2 = $(id).datagrid('getColumnOption', 'F5');
					var col3 = $(id).datagrid('getColumnOption', 'F6');
					var col4 = $(id).datagrid('getColumnOption', 'F7');
					var col5 = $(id).datagrid('getColumnOption', 'F8');
					var col6 = $(id).datagrid('getColumnOption', 'F9');
					var col7 = $(id).datagrid('getColumnOption', 'F10');
					if(row.F16==='3'){
						col1.editor = false
					}else{
						col1.editor = {type:'numberbox'}
					}
					if(row.F17==='3'){
						col2.editor = false
					}else{
						col2.editor = {type:'numberbox'}
					}
					if(row.F18==='3'){
						col3.editor = false
					}else{
						col3.editor = {type:'numberbox'}
					}
					if(row.F19==='3'){
						col4.editor = false
					}else{
						col4.editor = {type:'numberbox'}
					}
					if(row.F20==='3'){
						col5.editor = false
					}else{
						col5.editor = {type:'numberbox'}
					}
					if(row.F21==='3'){
						col6.editor = false
					}else{
						col6.editor = {type:'numberbox'}
					}
					if(row.F22==='3'){
						col7.editor = false
					}else{
						col7.editor = {type:'numberbox'}
					}
				}
			});
		},
		extenxDatagridEditorIds:{
			 F4		: "txt_suryo_mon"
			,F5		: "txt_suryo_tue"
			,F6		: "txt_suryo_wed"
			,F7		: "txt_suryo_thu"
			,F8		: "txt_suryo_fri"
			,F9		: "txt_suryo_sat"
			,F10	: "txt_suryo_sun"
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
		getGridColumns:function(that, id){
			var columns = [];
			var columnBottom=[];
			var fields = ["F1","F2","F3","F4","F5","F6","F7","F8","F9","F10","F12","F13","F11","F14","F15","F23"];
			var titles = ["商品コード","商品名","便","月","火","水","木","金","土","日","登録日","更新日","入力者","排他用更新日時","排他用更新日時2","No"];

			var iformatter = function(value,row,index){ return $.getFormat(value, '##,##0');};

			columnBottom.push({field:fields[15],	title:titles[15],	width:40,	halign:'center',	align:'center',editor:{options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}});
			columnBottom.push({field:fields[0],		title:titles[0],	width:80,	halign:'center',	align:'left',
				formatter:function(value,row,index){
					return $.getFormatPrompt(value, '####-####');
				},editor:{options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}});
			columnBottom.push({field:fields[1],		title:titles[1],	width:250,	halign:'center',align:'left',editor:{options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}});
			columnBottom.push({field:fields[2],		title:titles[2],	width:20,	halign:'center',align:'left',editor:{options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}});
			columnBottom.push({field:fields[3],		title:titles[3],	width:55,	halign:'center',align:'right',formatter:iformatter,editor:{type:'numberbox'}});
			columnBottom.push({field:fields[4],		title:titles[4],	width:55,	halign:'center',align:'right',formatter:iformatter,editor:{type:'numberbox'}});
			columnBottom.push({field:fields[5],		title:titles[5],	width:55,	halign:'center',align:'right',formatter:iformatter,editor:{type:'numberbox'}});
			columnBottom.push({field:fields[6],		title:titles[6],	width:55,	halign:'center',align:'right',formatter:iformatter,editor:{type:'numberbox'}});
			columnBottom.push({field:fields[7],		title:titles[7],	width:55,	halign:'center',align:'right',formatter:iformatter,editor:{type:'numberbox'}});
			columnBottom.push({field:fields[8],		title:titles[8],	width:55,	halign:'center',align:'right',formatter:iformatter,editor:{type:'numberbox'}});
			columnBottom.push({field:fields[9],		title:titles[9],	width:55,	halign:'center',align:'right',formatter:iformatter,editor:{type:'numberbox'}});
			columnBottom.push({field:fields[10],	title:titles[10],	width:65,	halign:'center',align:'left',editor:{options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}});
			columnBottom.push({field:fields[11],	title:titles[11],	width:65,	halign:'center',align:'left',editor:{options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}});
			columnBottom.push({field:fields[12],	title:titles[12],	width:70,	halign:'center',align:'left',editor:{options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}});
			columnBottom.push({field:fields[13],	title:titles[13],	hidden:true});
			columnBottom.push({field:fields[14],	title:titles[14],	hidden:true});
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

			// 呼出別処理
			switch (btnId) {
			case $.id.btn_back:


				// 転送先情報
				childurl = parent.$('#hdn_menu_path').val();

				break;
			case "btn_return":
				// 転送先情報
				index = 1;
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
		},
		getGridData: function (target){

			var data = {};
			var targetRows= [];

			// 配送店グループ
			if(target===undefined || target===$.id.gridholder){
				// 表示行数が多すぎる場合、getRowsでは取得できない為、getDataから取得を行う。
				targetRows = $($.id.gridholder).datagrid('getData').firstRows;
				data[$.id.gridholder] = targetRows;
			}
			return data;
		},
		getMergeGridDate: function(target){
			// 保持したデータと入力データ比較を比較する。
			var that = this;

			var newrows = that.getGridData(target)[target];		// 変更データ
			var oldrows = [];
			var targetRows= [];

			if(target===undefined || target===$.id.gridholder){
				// 催しコード一覧
				oldrows = that.grd_before_data
				for (var i=0; i<newrows.length; i++){
					if( newrows[i]["F1"] != oldrows[i]["F1"] ||
							newrows[i]["F3"] != oldrows[i]["F3"] ||
							newrows[i]["F4"] != oldrows[i]["F4"] ||
							newrows[i]["F5"] != oldrows[i]["F5"] ||
							newrows[i]["F6"] != oldrows[i]["F6"] ||
							newrows[i]["F7"] != oldrows[i]["F7"] ||
							newrows[i]["F8"] != oldrows[i]["F8"] ||
							newrows[i]["F9"] != oldrows[i]["F9"] ||
							newrows[i]["F10"] != oldrows[i]["F10"]
					){
							var rowDate = {
									F1	 : newrows[i]["F1"],
									F2	 : newrows[i]["F2"],
									F3	 : newrows[i]["F3"],
									F4	 : newrows[i]["F4"],
									F5	 : newrows[i]["F5"],
									F6	 : newrows[i]["F6"],
									F7	 : newrows[i]["F7"],
									F8	 : newrows[i]["F8"],
									F9	 : newrows[i]["F9"],
									F10	 : newrows[i]["F10"],
									F11	 : newrows[i]["F14"],
									F12	 : newrows[i]["F15"],
							};
						if(rowDate){
							targetRows.push(rowDate);
						}
					}
				}
			}
			return targetRows;
		},
		setGridData: function (data, target){
			var that = this;
			if(target===undefined || target===$.id.gridholder){
				var data = {};
				var targetRows= [];

				if (that.grd_before_data.length === 0) {
					// 表示行数が多すぎる場合、getRowsでは取得できない為、getDataから取得を行う。
					var rows = $($.id.gridholder).datagrid('getData').firstRows;
					for (var i=0; i<rows.length; i++){
						var rowDate = {
								F1	 : rows[i]["F1"],
								F2	 : rows[i]["F2"],
								F3	 : rows[i]["F3"],
								F4	 : rows[i]["F4"],
								F5	 : rows[i]["F5"],
								F6	 : rows[i]["F6"],
								F7	 : rows[i]["F7"],
								F8	 : rows[i]["F8"],
								F9	 : rows[i]["F9"],
								F10	 : rows[i]["F10"],
								F14	 : rows[i]["F14"],
								F15	 : rows[i]["F15"],
							};
						targetRows.push(rowDate);
					}
					that.grd_before_data = targetRows;
				}
			}
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