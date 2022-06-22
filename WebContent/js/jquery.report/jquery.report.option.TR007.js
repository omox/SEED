/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_ReportTR007',			// （必須）レポートオプションの確認
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
		dedefaultObjNum:	12,	// 初期化オブジェクト数
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
		grd_hattr:[],						// グリッド情報:店別数量
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
			//$.extendDatagridEditor(that);

			// 初期検索可能
			that.onChangeReport = false;

			// 入力テキストボックス系
			var inputbox = Object.getOwnPropertyNames($.id_inp);
			for ( var sel in inputbox ) {
				if($('#'+$.id_inp[inputbox[sel]]).length > 0){
					$.setInputbox(that, reportno, $.id_inp[inputbox[sel]], isUpdateReport);
				}
			}

			var isSearchId = [$.id_inp.txt_shncd, $.id_inp.txt_tencd];		// 検索条件のID
			for ( var sel in inputbox ) {
				if($('#'+$.id_inp[inputbox[sel]]).length > 0){
					var isUpdateReport = isSearchId.indexOf(inputbox[sel]) === -1;
					$.setInputbox(that, reportno, $.id_inp[inputbox[sel]], isUpdateReport);
				}
			}

			// 部門・大分類・中分類
			this.setBumon(reportno, $.id.SelBumon);
			this.setDaiBun(reportno, $.id.SelDaiBun);
			this.setChuBun(reportno, $.id.SelChuBun);
			// 週№
			//this.setShuno(that, reportno, $.id.sel_shuno);
			this.setShuno(that, reportno, $.id.sel_shunoperiod);


			// 定量
			that.setEditableGrid(that, reportno, $.id.gridholder);

			// チェックボックス初期化
			$.setCheckboxInit2(that.jsonHidden, $.id.chk_updkbn, that);
			$.setCheckboxInit(that.jsonHidden, $.id.chk_seiki, that);
			$.setCheckboxInit(that.jsonHidden, $.id.chk_jisyu, that);

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

			// 当帳票を「新規」で開いた場合
			if (that.reportYobiInfo()==='1') {
				$('#'+$.id.btn_upd).linkbutton('disable').attr('disabled', 'disabled').hide();
				$('#'+$.id.btn_cancel).linkbutton('disable').attr('disabled', 'disabled').hide();

				$.initReportInfo("TR007", "定量　CSV取込データ　参照", "CSV参照");
			}else{
				$.initReportInfo("TR007", "定量　CSV取込データ　変更", "CSV変更");
			}

			// 各種遷移ボタン
			$('#'+$.id.btn_cancel).on("click", $.pushChangeReport);

			// クリックされたボタンのIDを保持
			$('#'+$.id.btn_del).on("click", function(){that.clickBtnid = $.id.btn_del});
			$('#'+$.id.btn_upd).on("click", function(){that.clickBtnid = $.id.btn_upd});

			// 入力不可に設定
			$.setInputBoxDisableVariable($("#"+$.id.sel_shunoperiod),true);

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
			var sel_daibun	= $.getJSONObject(this.jsonString, $.id.SelDaiBun).value;		// 大分類
			var txt_shncd	= $.getJSONObject(this.jsonString, $.id_inp.txt_shncd).value;	// 商品コード

			if (txt_shncd===null || txt_shncd==="" || txt_shncd===undefined) {
				if (sel_bumon==='-1' || sel_daibun==='-1' ||
					(sel_bumon===null || sel_bumon==="" || sel_bumon===undefined) ||
					(sel_daibun===null || sel_daibun==="" || sel_daibun===undefined)) {
					$.showMessage('E20254');
					return false;
				}
			}

			// 桁数、マスタ存在チェック：商品コード
			var msgid = null;
			msgid = that.checkInputboxFunc($.id_inp.txt_shncd, txt_shncd, '');
			if(msgid !== null){
				$.showMessage(msgid);
				return false;
			}

			var chk_seiki	= $.getInputboxValue($('#'+$.id.chk_seiki));						// 正規
			var chk_jisyu	= $.getInputboxValue($('#'+$.id.chk_jisyu));						// 次週
			var sel_shuno	= $.getJSONObject(this.jsonString, $.id.sel_shunoperiod).value;		// 週No.

			// 必須入力チェック：次正区分
			if(chk_jisyu===$.id.value_off && chk_seiki===$.id.value_off){
				$.showMessage('EX1032');
				return false;
			}

			if(chk_jisyu===$.id.value_off &&
				(sel_shuno!==null && sel_shuno!=="" && sel_shuno!==undefined && sel_shuno!=="-1")){
				$.showMessage('EX1031');
				return false;
			}

			return rt;
		},
		success: function(reportno, sortable){	// （必須）正処理

			if (sortable) sortable=1; else sortable=0;
			var that = this;

			// 検索実行
			var txt_shncd	= $.getJSONObject(this.jsonString, $.id_inp.txt_shncd).value;		// 商品コード
			var txt_tencd	= $.getJSONObject(this.jsonString, $.id_inp.txt_tencd).value;		// 店コード
			var sel_bumon	= $.getJSONObject(this.jsonString, $.id.SelBumon).value;			// 部門
			var sel_daibun	= $.getJSONObject(this.jsonString, $.id.SelDaiBun).value;			// 大分類
			var sel_chubun	= $.getJSONObject(this.jsonString, $.id.SelChuBun).value;			// 中分類
			var sel_shuno	= $.getJSONObject(this.jsonString, $.id.sel_shunoperiod).value;		// 週No.
			var chk_seiki	= $.getInputboxValue($('#'+$.id.chk_seiki));						// 正規
			var chk_jisyu	= $.getInputboxValue($('#'+$.id.chk_jisyu));						// 次週

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$.appendMaskMsg();

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,		// レポート名
					SHNCD:			txt_shncd,
					TENCD:			txt_tencd,
					BUMON:			sel_bumon,
					DAIBUN:			sel_daibun,
					CHUBUN:			sel_chubun,
					SHUNO:			sel_shuno,
					SEIKI:			chk_seiki,
					JISYU:			chk_jisyu,
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
					that.setData(that.gridData, opts);
					that.queried = true;

					// データグリッド初期化
					that.setEditableGrid(that, reportno, $.id.gridholder);

					// 一覧画面へ戻る
					if (that.clickBtnid === $.id.btn_del || that.clickBtnid === $.id.btn_upd) {
						that.changeReport(that.name, 'btn_return');
					}

					that.queried = true;
					// Load処理回避
					$.tryChangeURL($.reg.jqeasy);

					var size = JSON.parse(json)["total"];
					if(size == 0){
						$.showMessage('E20038');
					}

					// グリッド再描画（easyui 1.4.2 対応）
					$($.id.gridholder).datagrid('load', {} );
					$.removeMask();

					// 状態保存
					$.saveState2(reportno, that.getJSONString());

					// 検索結果を保持する。
					var Data = that.getGridData($.id.gridholder);
					that.setGridData(Data, 'data');

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
				alert('入力内容を確認してください。');
				return rt;
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

			var txt_shncd	= $.getJSONObject(this.jsonString, $.id_inp.txt_shncd).value;	// 商品コード
			var txt_tencd	= $.getJSONObject(this.jsonString, $.id_inp.txt_tencd).value;	// 店コード
			var sel_bumon	= $.getJSONObject(this.jsonString, $.id.SelBumon).value;		// 部門
			var sel_daibun	= $.getJSONObject(this.jsonString, $.id.SelDaiBun).value;		// 大分類
			var sel_chubun	= $.getJSONObject(this.jsonString, $.id.SelChuBun).value;		// 中分類
			var sel_shuno	= $.getJSONObject(this.jsonString, $.id.sel_shunoperiod).value;		// 週No.
			var chk_seiki	= $.getInputboxValue($('#'+$.id.chk_seiki));					// 正規
			var chk_jisyu	= $.getInputboxValue($('#'+$.id.chk_jisyu));					// 次週
			var hdn_upddt	= "";
			$('#'+that.focusRootId).find('[col^=F18]').each(function(){
				var col = $(this).attr('col');
				hdn_upddt = $.getInputboxValue($(this));
			});


			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$.appendMaskMsg();
			$.appendMask();

			var tenFlg = 1;
			if ($.getInputboxValue($('#'+$.id_inp.txt_tencd))==="" ||
					$.getInputboxValue($('#'+$.id_inp.txt_tencd))===null ||
					$.getInputboxValue($('#'+$.id_inp.txt_tencd))===undefined) {
				tenFlg = 0;
			}

			// 入力データ：正規定量_店別数量(次正も含む)
			//var targetRowsHatTtr = that.getGridData($.id.gridholder);
			var targetRowsHatTtr = that.getMergeGridDate();

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$.appendMaskMsg();

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,		// レポート名
					action:			$.id.action_update,	// 実行処理情報
					obj:			id,								// 実行オブジェクト
					SENDBTNID:		that.sendBtnid,
//					IDX:			$($.id.hiddenChangedIdx).val(),		// 更新対象Index
					DATA_HATSTR:	JSON.stringify(targetRowsHatTtr),	// 更新対象情報(正規定量_店別数量(次週も含む))
					SHNCD:			txt_shncd,
					TENCD:			txt_tencd,
					BUMON:			sel_bumon,
					DAIBUN:			sel_daibun,
					CHUBUN:			sel_chubun,
					SHUNO:			sel_shuno,
					SEIKI:			chk_seiki,
					JISYU:			chk_jisyu,
					TENFLG:			tenFlg,								// 店番フラグ
					HDN_UPDDT:		hdn_upddt,
					t:				(new Date()).getTime()
				},
				function(data){
					// 検索処理エラー判定
					if($.updError(id, data)) return false;

					var afterFunc = function(){
						// 初期化
						that.getEasyUI();
						that.jsonString = that.jsonTemp.slice(0);
						that.clear();
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
			$.appendMask();

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
			// 店コード
			this.jsonTemp.push({
				id:		$.id_inp.txt_tencd,
				value:	$('#'+$.id_inp.txt_tencd).textbox('getValue'),
				text:	$('#'+$.id_inp.txt_tencd).textbox('getText')
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
			// 週No.
			this.jsonTemp.push({
				id:		$.id.sel_shunoperiod,
				value:	$('#'+$.id.sel_shunoperiod).combobox('getValue'),
				text:	$('#'+$.id.sel_shunoperiod).combobox('getText')
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
		setShuno: function(that, reportno, id){		// データ表示
			var that = this;
			var idx = -1;
			var onChange=false;
			var onPanel=false;

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
					// 選択値設定
					var val = null;
					if ($.inArray(id, that.initedObject) < 0){
						that.initedObject.push(id);
						var json = $.getJSONObject(that.jsonHidden, id);
						if(json && json.value!=""){
							val = new Array();
							for (var i=0; i<data.length; i++){
								if ($.inArray(data[i].VALUE, json.value)!=-1){
									val.push(data[i].VALUE);
								}
							}
						}
					}
					if (val === null && data.length>0){
						val = data[0].VALUE;
					}
					if (val){
						$('#'+id).combobox('setValues',val);
					}
					idx = 1;
					// ログ出力
					$.log(that.timeData, id+' init:');
					that.onChangeFlag = false;
				},
				onShowPanel: function(){
					$.setScrollComboBox(id);
					that.onChangeFlag=false;
					onChange=false;
					onPanel=true;
				},
				onHidePanel: function(){
					if (!onChange){
						that.onChangeFlag=true;
					}
					onChange=false;
					onPanel=false;
				},
				onChange:function(newValue, oldValue, obj){
					onChange=true;

					if(obj===undefined){obj = $(this);}
					that.getComboErr(obj,false,newValue,oldValue);
				}
			});
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
						//REQUIRED: 'REQUIRED',
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
				},
				onChange:function(newValue, oldValue, obj){
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
			//that.editRowIndex['gridholder'] = -1;
			var index = -1;
			var columns = that.getGridColumns(that, id);

			// ページサイズ定義取得
			var pageList = $.fn.pagination.defaults.pageList;
			var pageSize = $.getJSONValue(this.jsonHidden, $.id.pageSize);
			if (pageSize==="") pageSize=$.fn.pagination.defaults.pageSize;
			//pageSize = $.getDefaultPageSize(pageSize, pageList);
			pageSize = 50;

			var funcClickRow = $.fn.datagrid.defaults.onClickRow;
			var funcBeginEdit= $.fn.datagrid.defaults.onBeginEdit;
			var funcEndEdit= $.fn.datagrid.defaults.onEndEdit;
			var funcAfterEdit= $.fn.datagrid.defaults.onAfterEdit;
			if(that.reportYobiInfo()!=='1'){
				that.editRowIndex['gridholder'] = -1;
				funcClickRow = function(index,field){$.clickEditableDatagridCell(that,'gridholder', index)};
				funcBeginEdit = function(index,row){$.beginEditDatagridRow(that,'gridholder', index, row)};
				funcEndEdit = function(index,row,changes){$.endEditDatagridRow(that, 'gridholder', index, row)};

				// 編集可能データグリッドの共通処理設定
				// 編集エディターの機能拡張（非表示で参照用のオブジェクトが準備してある前提、参照オブジェクトはグリッドのフィールド名を元に判断）
				$.extendDatagridEditor(that);
			}

			$(id).datagrid({
				columns:columns,
				view:scrollview,
				pageSize:pageSize,
				pageList:pageList,
				onBeforeLoad:function(param){
					param.report = that.name;
				},
				onLoadSuccess:function(data){
					var gridData = that.getGridData(id);
					that.setGridData(gridData);
				},
				onBeforeEdit:function(index,row){
					var allRows	 = $(id).datagrid('getRows');

					if(!row.F19 || row.F19 =='1'){	// F19:送信フラグ
						// 送信フラグ=1(バッチで送信済みのデータ)の場合
						var adds = that.editRowIndex[that.focusGridId] > index ? -1:1;
						var nextindex = 0;
						//var nextindex = index + adds;

						// 次のEdit可能な行を探す。
						for(var i = index; i < allRows.length; i++ ){
							var nextRow = allRows[i]
							if(nextRow.F19 !='1'){
								nextindex = $(id).datagrid("getRowIndex", nextRow);
								break;
							}
						}
						if(index == (allRows.length-1)){
							// 最終行が選択された場合
							nextindex = index +1

						}else if(nextindex == 0){
							// Edit可能な行が存在しなかった場合
							nextindex = (allRows.length-1);

						}

						// 次の行に移るか、次の項目に移るかする
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
				onClickRow: funcClickRow,
				onBeginEdit:funcBeginEdit,
				onEndEdit: funcEndEdit,
			});
		},
		extenxDatagridEditorIds:{
			 F7		: "txt_suryo_mon"
			,F8		: "txt_suryo_tue"
			,F9		: "txt_suryo_wed"
			,F10	: "txt_suryo_thu"
			,F11	: "txt_suryo_fri"
			,F12	: "txt_suryo_sat"
			,F13	: "txt_suryo_sun"
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
			var cformatter =function(value,row,index){return value && value===$.id.value_on?$.id.text_on:$.id.text_off;};
			var targetId = $.id_inp.txt_tencd;
			var check = $('#'+targetId).attr("check") ? JSON.parse('{'+$('#'+targetId).attr("check")+'}'): JSON.parse('{}');	// コードのcheck要素を取得
			var formatterLPad = function(value){
				return $.getFormatLPad(value, check.maxlen);
			};

			var iformatter = function(value,row,index){ return $.getFormat(value, '##,##0');};

			var cstyler =function(value,row,index){return 'color: red;font-weight: bold;';};
			var fields = ["F1","F2","F3","F4","F5","F6","F7","F8","F9","F10","F11","F12","F13","F14"];
			var titles = ["次正区分","週No.","商品コード","商品名","店番","店舗名","月","火","水","木","金","土","日","便区分"];
			if (that.reportYobiInfo()==='1') {
				columnBottom.push({field:'UPDKBN',	title:'削除',	hidden:true});
			} else {
				columnBottom.push({field:'UPDKBN',	title:'削除',	width:35,	align:'center',	formatter:cformatter,	editor:{type:'checkbox'},	styler:cstyler});
			}
			columnBottom.push({field:fields[0],		title:titles[0],	width:60,	align:'left',	editor:{options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}});
			columnBottom.push({field:fields[1],		title:titles[1],	width:50,	align:'center',	editor:{options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}});
			columnBottom.push({field:fields[2],		title:titles[2],	width:80,	align:'center',
				formatter:function(value,row,index){
					return $.getFormatPrompt(value, '####-####');
				},editor:{options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}});
			columnBottom.push({field:fields[3],		title:titles[3],	width:300,	halign:'center',align:'left',	editor:{options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}});
			columnBottom.push({field:fields[4],		title:titles[4],	width:40,	align:'left',formatter:formatterLPad,editor:{options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}});
			columnBottom.push({field:fields[5],		title:titles[5],	width:230,	halign:'center',align:'left',	editor:{options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}});
			columnBottom.push({field:fields[6],		title:titles[6],	width:60,	halign:'center',align:'right',formatter:iformatter,editor:{type:'numberbox'}});
			columnBottom.push({field:fields[7],		title:titles[7],	width:60,	halign:'center',align:'right',formatter:iformatter,editor:{type:'numberbox'}});
			columnBottom.push({field:fields[8],		title:titles[8],	width:60,	halign:'center',align:'right',formatter:iformatter,editor:{type:'numberbox'}});
			columnBottom.push({field:fields[9],		title:titles[9],	width:60,	halign:'center',align:'right',formatter:iformatter,editor:{type:'numberbox'}});
			columnBottom.push({field:fields[10],	title:titles[10],	width:60,	halign:'center',align:'right',formatter:iformatter,editor:{type:'numberbox'}});
			columnBottom.push({field:fields[11],	title:titles[11],	width:60,	halign:'center',align:'right',formatter:iformatter,editor:{type:'numberbox'}});
			columnBottom.push({field:fields[12],	title:titles[12],	width:60,	halign:'center',align:'right',formatter:iformatter,editor:{type:'numberbox'}});
			columnBottom.push({field:fields[13],	title:titles[13],	hidden:true});
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

			// 戻る実行時用に現在の画面情報を保持する
			var states = $.getBackBaseJSON(that);
			// 各種グリッド情報を設定
			var newrepinfos = $.getBackJSON(that, states);
			$.setJSONObject(sendJSON, 'repinfo', newrepinfos, '');

			// 呼出別処理
			switch (btnId) {
			case $.id.btn_cancel:
			case $.id.btn_back:
			case "btn_return":
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
		getGridData: function (target){

			var targetRows= [];

			// 店別数量
			if(target===undefined || target===$.id.gridholder){
				var rowsHstgp= $($.id.gridholder).datagrid('getRows');
				for (var i=0; i<rowsHstgp.length; i++){
					if(rowsHstgp[i]["F3"] == "" || rowsHstgp[i]["F3"] == null ){

					}else{
						var rowDate = {
								F1	 : rowsHstgp[i]["UPDKBN"],	// F1	更新区分
								F2	 : rowsHstgp[i]["F1"],		// F2	次正区分
								F3	 : rowsHstgp[i]["F2"],		// F3	週№
								F4	 : rowsHstgp[i]["F3"],		// F4	商品コード
								F5	 : rowsHstgp[i]["F5"],		// F5	店コード
								F6	 : rowsHstgp[i]["F7"],		// F6	店別数量_月
								F7	 : rowsHstgp[i]["F8"],		// F7	店別数量_火
								F8	 : rowsHstgp[i]["F9"],		// F8	店別数量_水
								F9	 : rowsHstgp[i]["F10"],		// F9	店別数量_木
								F10	 : rowsHstgp[i]["F11"],		// F10	店別数量_金
								F11	 : rowsHstgp[i]["F12"],		// F11	店別数量_土
								F12	 : rowsHstgp[i]["F13"],		// F12	店別数量_日
								F13	 : rowsHstgp[i]["F18"],		// F13	更新日(排他チェック用)
							};
						targetRows.push(rowDate);
					}
				}
			}
			return targetRows;
		},
		getMergeGridDate: function(){
			// 保持したデータと入力データ比較を比較する。
			var that = this;

			var newrows = that.getGridData($.id.gridholder) ? that.getGridData($.id.gridholder) : [];		// 変更データ
			var oldrows = that.grd_hattr;		// 検索時保持データ
			var targetRows= [];

			for (var i=0; i<newrows.length; i++){

				if((newrows[i]['F1']) == '1'){
					// 削除区分1の場合は入力項目に変更があっても反映を行わず、更新区分(F1)以外は検索時のデータを送信する。
					var rowDate = {
							F1	 : newrows[i]["F1"],
							F2	 : oldrows[i]["F2"],
							F3	 : oldrows[i]["F3"],
							F4	 : oldrows[i]["F4"],
							F5	 : oldrows[i]["F5"],
							F6	 : oldrows[i]["F6"],
							F7	 : oldrows[i]["F7"],
							F8	 : oldrows[i]["F8"],
							F9	 : oldrows[i]["F9"],
							F10	 : oldrows[i]["F10"],
							F11	 : oldrows[i]["F11"],
							F12	 : oldrows[i]["F12"],
							F13	 : oldrows[i]["F13"],
					};
					if(rowDate){
						targetRows.push(rowDate);
					}
				}else if((oldrows[i]['F6'] ? oldrows[i]['F6'] : "")	 != (newrows[i]['F6'] ? newrows[i]['F6'] : "")
						|| (oldrows[i]['F7'] ? oldrows[i]['F7'] : "")	 != (newrows[i]['F7'] ? newrows[i]['F7'] : "")
						|| (oldrows[i]['F8'] ? oldrows[i]['F8'] : "")	 != (newrows[i]['F8'] ? newrows[i]['F8'] : "")
						|| (oldrows[i]['F9'] ? oldrows[i]['F9'] : "")	 != (newrows[i]['F9'] ? newrows[i]['F9'] : "")
						|| (oldrows[i]['F10'] ? oldrows[i]['F10'] : "")	 != (newrows[i]['F10'] ? newrows[i]['F10'] : "")
						|| (oldrows[i]['F11'] ? oldrows[i]['F11'] : "")	 != (newrows[i]['F11'] ? newrows[i]['F11'] : "")
						|| (oldrows[i]['F12'] ? oldrows[i]['F12'] : "")	 != (newrows[i]['F12'] ? newrows[i]['F12'] : "")
				){
					// 入力値に変更があったデータのみを保持する。
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
							F11	 : newrows[i]["F11"],
							F12	 : newrows[i]["F12"],
							F13	 : newrows[i]["F13"],
					};
					if(rowDate){
						targetRows.push(rowDate);
					}
				}
			}
			return targetRows;
		},
		setGridData: function (data){
			var that = this;

			// 店別数量
			that.grd_hattr =  data;
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

			//　次正区分 正規
			if(id===$.id.chk_seiki){
				var chk_jisyu	= $.getInputboxValue($('#'+$.id.chk_jisyu));				// 次週

				if(newValue == '1' && chk_jisyu !== '1'){
					// 選択地をクリア
					$('#'+$.id.sel_shunoperiod).combobox('setValue', -1);
					// 入力不可に設定
					$.setInputBoxDisableVariable($("#"+$.id.sel_shunoperiod),true);
				}else{
					// 入力可に設定
					$.setInputBoxEnableVariable($("#"+$.id.sel_shunoperiod),true);
					// 選択地をクリア
					$('#'+$.id.sel_shunoperiod).combobox('setValue', -1);
				}
			}

			//　次正区分 次週
			if(id===$.id.chk_jisyu){
				var chk_seiki	= $.getInputboxValue($('#'+$.id.chk_seiki));				// 正規
				var shuNo		= $.getInputboxValue($('#'+$.id.sel_shunoperiod));			// 週番号

				if(newValue == '1'){
					// 入力可に設定
					$.setInputBoxEnableVariable($("#"+$.id.sel_shunoperiod),true);
					// 選択地をクリア
					$('#'+$.id.sel_shunoperiod).combobox('setValue', -1);
				}else {
					// 選択地をクリア
					if(shuNo !== ''){
						$('#'+$.id.sel_shunoperiod).combobox('setValue', -1);
					}
					// 入力不可に設定
					$.setInputBoxDisableVariable($("#"+$.id.sel_shunoperiod),true);
				}
			}

			// 検索、入力後特殊処理
			if(that.queried){

			}

			// 商品コード
			if(id===$.id_inp.txt_shncd){
				if(newValue !== '' && newValue){
					$.setInputboxValue($('#'+$.id_inp.txt_shncd),$.getFormatLPad(newValue, 8));
				}
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

			// 商品コード
			if(id===$.id_inp.txt_shncd){
				if(newValue !== '' && newValue){
					// 商品コード
					if(newValue.length < 8){
						return "EX1006";
					}

					var param = {};
					param["KEY"] =  "MST_CNT";
					param["value"] = newValue;
					var chk_cnt = $.getInputboxData(that.name, $.id.action_check, $.id_inp.txt_shncd, [param]);
					if(chk_cnt==="" || chk_cnt==="0"){
						return "E11046";
					}
				}
			}

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