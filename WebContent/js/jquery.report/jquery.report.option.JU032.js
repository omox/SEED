/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_ReportJU032',			// （必須）レポートオプションの確認
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
		dedefaultObjNum:	18,					// 初期化オブジェクト数
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
		sendBtnid: "",							// （必須）呼出ボタンID情報
		focusRootId:"cc",						// （キー移動時必須）キー移動イベントのルートとなるパネルのID
		focusParentId:"",						// （キー移動時必須）現在フォーカスがあたっている項目の親となるパネルのID
		focusGridId:"",							// （キー移動時必須）現在フォーカスがあたっているDataGridのID
		editRowIndex:{},						// グリッド編集行保持
		gridData:[],							// 検索結果
		grd_fsirt_data:[],						// グリッド情報
		gridTitle:[],							// 検索結果
		unSelectIndex:'',
		keyEvent:false,
		oldValue:'',
		tenht:'1', // 1:同一発注数量 2:ランク別発注数量 3:店別発注数量
		btnUpdFocus:false,
		initialize: function (reportno){		// （必須）初期化
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

			// 初期表示処理
			that.onChangeReport = true;

			var count = 2;
			var meisyoSelect = Object.getOwnPropertyNames($.id_mei);
			for ( var sel in meisyoSelect ) {
				if($('#'+$.id_mei[meisyoSelect[sel]]).length > 0){
					$.setMeisyoCombo(that, reportno, $.id_mei[meisyoSelect[sel]], false);
					count++;
				}
			}
			// 入力テキストボックス系
			var inputbox = Object.getOwnPropertyNames($.id_inp);
			for ( var sel in inputbox ) {
				if($('#'+$.id_inp[inputbox[sel]]).length > 0){
					$.setInputbox(that, reportno, $.id_inp[inputbox[sel]], isUpdateReport);
					count++;
				}
			}
			$.setInputbox(that, reportno, 'kikan_dummy', isUpdateReport);
			// 処理日付取得
			that.getsetInputboxData(reportno, $.id.txt_shoridt, [{}], $.id.action_init);
			// 編集可能データグリッドの共通処理設定
			// 編集エディターの機能拡張（非表示で参照用のオブジェクトが準備してある前提、参照オブジェクトはグリッドのフィールド名を元に判断）

			$.extendDatagridEditor();

			that.setEditableGrid(that, reportno, "grd_tenhtsu_arr");

			// 登録ボタンにフォーカスがある場合にEnter押下で登録実行
			$('#'+$.id.btn_upd).on('focusout', function(e){
				that.btnUpdFocus = false;
			});
			$('#'+$.id.btn_upd).on('focus', function(e){
				that.btnUpdFocus = true;
			});

			// 初期化終了
			this.initializes =! this.initializes;
			$.win001.init(that);	// 仕入先
			$.win002.init(that);	// メーカー
			$.win006.init(that);	// 商品コード
			$.winST007.init(that);	// 店情報
			$.winST008.init(that);	// ランク店情報
			$.winST009.init(that);	// 商品コード
			$.winST010.init(that);	// 商品コード
			$.winST011.init(that);	// 実績参照
			$.JU017.init(that);		// 数量パターン
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

			$('#'+$.id.btn_cancel).on("click", $.pushChangeReport);
			$.initReportInfo("JU032", "店舗アンケート付き送付け 新規 商品情報", "新規");
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
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform).form('validate');
			// 入力エラーなしの場合に検索条件を格納
			//if (rt == true)
			that.jsonString = that.jsonTemp.slice(0);
			// 入力チェック用の配列をクリア
			that.jsonTemp = [];
			return true;
		},
		success: function(reportno, sortable){	// （必須）正処理

			if (sortable) sortable=1; else sortable=0;
			var that = this;
			// 検索実行

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			$($.id.gridholder).datagrid('loading');

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,		// レポート名
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
					that.setEditableGrid(that, reportno, "grd_tenhtsu_arr");

					// 各グリッドの値を保持する
					that.grd_fsirt_data		 =  $('#grd_tenhtsu_arr').datagrid('getRows');

					if ($.getInputboxValue($('#sel_shohinkbn1'))==='0') {
						// 更新非対象項目は非活性に
						$.setInputBoxDisableVariable($('#'+$.id_inp.txt_irisu),true);
						$.setInputBoxDisableVariable($('#'+$.id_inp.txt_genkaam),true);
						$.setInputBoxDisableVariable($('#'+$.id_inp.txt_rg_baikaam),true);
					}

					$('#tt').tabs({
						onSelect:function(title,index){

							if ($.isEmptyVal(that.unSelectIndex)) {
								if (index===1) {
									var target = $.getInputboxTextbox($('#'+$.id_inp.txt_tenrank+'_2'));
									target.focus();
								} else if (index===2) {
									$('#grd_tenhtsu_arr').datagrid('beginEdit', 0);
								}
								return false;
							}

							if (that.unSelectIndex === index) {
								that.unSelectIndex = '';
							} else if (that.unSelectIndex === 0) {
								$('#'+$.id.btn_upd).focus();
								$('#tt').tabs('select', that.unSelectIndex);
							} else if (that.unSelectIndex === 1) {
								$('#tt').tabs('select', that.unSelectIndex);
							}
						}
					});

					$("#btn_suryo").on('focus', function(e){
						var val = $.getInputboxValue($('#'+$.id_inp.txt_suryoptn));
						if (that.keyEvent && !$.isEmptyVal(val)) {
							$('#'+$.id.btn_upd).focus();
							that.keyEvent = false;
						}
					});

					// 状態保存
					$.saveState2(reportno, that.getJSONString());

					// ログ出力
					$.log(that.timeData, 'loaded:');
				}
			);
		},
		updValidation: function (){	// （必須）批准
			var that = this;

			// 店舗一覧の入力編集を終了する。
			var row = $('#grd_tenhtsu_arr').datagrid("getSelected");
			if(row){
				var rowIndex = $('#grd_tenhtsu_arr').datagrid("getRowIndex", row);
				$('#grd_tenhtsu_arr').datagrid('endEdit',rowIndex);
			}

			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform).form('validate');
			if(!rt){
				$.addErrState(that, $('.validatebox-invalid').eq(0), false);
				return rt;
			}

			var obj = '';
			var msgid = null;
			// 入力値のエラーチェック
			$('#'+that.focusRootId).find('[col^=F]').each(function(){
				msgid = that.checkInputboxFunc($(this).attr('id'),$.getInputboxValue($(this)));
				if(msgid !==null){
					obj = $(this);
					return false;
				}
			});

			if(msgid !==null){
				if (msgid.length === 2) {
					$.showMessage(msgid[0],[msgid[1]],function () { $.addErrState(that, obj,true) });
				} else {
					$.showMessage(msgid,undefined,function () { $.addErrState(that, obj,true) });
				}
				that.tenht = '1';
				return false;
			}

			// 変更の場合確認メッセージを表示
			that.updConfirmMsg = "W00001";

			var param = {};
			param["KEY"] =  "SEL";
			param["SHNCD"] = $.getInputboxValue($('#'+$.id_inp.txt_shncd));
			param["NNDT"] = $.getInputboxValue($('#'+$.id_inp.txt_nndt));
			param["HTDT"]	= $.getInputboxValue($('#'+$.id_inp.txt_htdt));
			param["MOYSCD"] = $.getInputboxValue($('#'+$.id_inp.txt_moyscd));

			var rank = $.getInputboxValue($('#'+$.id_inp.txt_tenrank));
			var rank2 = $.getInputboxValue($('#'+$.id_inp.txt_tenrank_2));

			if (rank!==null && rank!=="" && rank!==undefined) {
				if (rank2===null || rank2==="" || rank2===undefined) {
					param["RANK"] = rank;
				} else {
					param["RANK"] = rank2;
				}
			} else if (rank2!==null && rank2!=="" && rank2!==undefined) {
				param["RANK"] = rank2;
			} else {
				param["RANK"] = "";
			}

			var targetRows	= that.getGridData('grd_tenhtsu_arr');
			param["DATA_TENHT"] = JSON.stringify(targetRows); // 個別データグリッド:店別数量発注入力

			var row = $.getSelectListData(that.name, $.id.action_check,  $.id_inp.txt_shncd, [param]);

			// 重複エラーがある場合登録不可
			if(row.length != 0){
				$.showMessage("EX1047",[row[0].VALUE+"商品、発注日、納入日、店番で重複しない値"],function () { $.addErrState(that, $('#'+$.id_inp.txt_htdt),true) });
				return false;
			}
			return rt;
		},
		updSuccess: function(id){	// validation OK時 の update処理
			var that = this;

			// 変更行情報取得
			var changedIndex = $($.id.hiddenChangedIdx).val().split(",");

			var targetDatas = [{}];
			$('#'+that.focusRootId).find('[col^=F]').each(function(){
				var col = $(this).attr('col');
				targetDatas[0][col] = $.getInputboxValue($(this));
			});

			var targetRows	= that.getGridData('grd_tenhtsu_arr');
			var shoridt		= $.getInputboxValue($('#'+$.id.txt_shoridt));

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
					DATA:			JSON.stringify(targetDatas),		// 更新対象情報
					DATA_TENHT:		JSON.stringify(targetRows),			// 個別データグリッド:店別数量発注入力
					SHORIDT:		shoridt,							// 処理日付
					t:				(new Date()).getTime()
				},
				function(data){
					// 検索処理エラー判定
					if($.updError(id, data)) return false;

					var afterFunc = function(){
						// 初期化
						that.getEasyUI();
						that.jsonString = that.jsonTemp.slice(0);
						$('#'+that.focusRootId).find('[col^=F]').each(function(){
							if (!$(this).attr('disabled') && !$.isEmptyVal($.getInputboxValue($(this)))) {
								if ($(this).hasClass('easyui-combobox')) {
									var val = $(this).combobox('getData')[0].VALUE;
									if ($(this).attr('id')==='sel_wappenkbn1') {
										val = 1;
									}
									if ($(this).attr('id')!=='sel_shohinkbn1') {
										$(this).combobox('setValue',val);
									}
								} else {
									if (["F6","F12","F13","F14","F15","F16","F17"].indexOf($(this).attr('col'))===-1) {
										$.setInputboxValue($(this),'');
									}
								}
							}
						});
						var target = $.getInputboxTextbox($('#'+$.id_inp.txt_shncd));
						target.focus();
						that.clear();
					};
					$.updNormal(data, afterFunc);

					// ログ出力
					$.log(that.timeData, 'loaded:');
				}
			);

			// 保持データを更新する
			// 複数店舗一覧
			var gridData = that.getGridData('grd_tenhtsu_arr');
			that.setGridData(gridData, 'grd_tenhtsu_arr');
		},
		updConfirm: function(func){	// validation OK時 の update処理
			var that = this;
			var msgId = that.updConfirmMsg;
			var prm = "";

			if (msgId!=="W00001") {
				prm = [msgId];
				msgId = "W00001";
			}

			$.showMessage(msgId, prm, func);
		},
		delValidation: function (){	// （必須）批准
			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = true;

			// 仕入先コード
			var txt_sircd = $.getInputboxValue($('#'+$.id_inp.txt_sircd));

			if(txt_sircd === ""){
				// TODO
				alert('検索条件に仕入先コードを入力してください')
				return false;

			}

			// 入力チェック用の配列をクリア
			that.jsonTemp = [];

			return rt;
		},
		delSuccess: function(id){
			var that = this;
			//var is_warning = false;

			var targetDatas = [{}];
			$('#'+that.focusRootId).find('[col^=F]').each(function(){
				var forId = $(this).attr('col');
				targetDatas[0][forId] = $.getInputboxValue($(this));
			});

			var txt_sircd		= $('#'+$.id_inp.txt_sircd).textbox('getValue');		// 仕入先コード

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$.appendMaskMsg();

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,		// レポート名
					action:			$.id.action_delete,	// 実行処理情報
					obj:			id,								// 実行オブジェクト
					SIRCD:			txt_sircd,						// 仕入先コード
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
						that.changeReport(that.name, 'btn_return');
					};
					$.delNormal(data, afterFunc);

					// ログ出力
					$.log(that.timeData, 'loaded:');

					// マスク削除
					$.removeMaskMsg();
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
				id:		$.id_inp.txt_moyscd,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_moyskbn),
				text:	''
			});
			// 催しコード（催し開始日）
			this.jsonTemp.push({
				id:		$.id_inp.txt_moykn,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_moysstdt),
				text:	''
			});
			// 催し連番
			this.jsonTemp.push({
				id:		'kikan_dummy',
				value:	$.getJSONValue(this.jsonHidden, 'kikan_dummy'),
				text:	''
			});
			// 店舗締切日
			this.jsonTemp.push({
				id:		$.id_inp.txt_qasmdt,
				value:	$.getJSONValue(this.jsonHidden, $.id.txt_qasmdt),
				text:	''
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
		// パラメータを元にDBに問い合わせた結果を取得、画面上に設定する
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
				}
			});
			idx = 1;
		},
		setDefaultDate: function(){
			// 編集前のグリッドデータを保持する。
			var that = this;
			var enptyrows = []

			// 編集前データ保持：実仕入先一覧
			if($('#grd_tenhtsu_arr').datagrid('getRows')){
				that.grd_fsirt_data	 =  $('#grd_tenhtsu_arr').datagrid('getRows');
			}else{
				that.grd_fsirt_data	 =  enptyrows;
			}
		},
		setEditableGrid: function(that, reportno, id){		// データ表示
			var that = this;

			that.editRowIndex[id] = -1;
			var index = -1;

			$('#'+id).datagrid({
				nowrap: true,
				border: true,
				striped: true,
				collapsible:false,
				remoteSort: true,
				rownumbers:true,
				singleSelect:true,
				checkOnSelect:false,
				selectOnCheck:false,
				url:$.reg.easy,
				onBeforeLoad:function(param){
					index = -1;
					//var json = that.getGridParams(that, id);
					var txt_sircd = $.getInputboxValue($('#'+$.id_inp.txt_sircd));

					var json = [{"callpage":"Out_ReportJU032"}];
					// 情報設定
					param.page		=	reportno;
					param.obj		=	id;
					param.sel		=	(new Date()).getTime();
					param.target	=	id;
					param.action	=	$.id.action_init;
					param.json		=	JSON.stringify(json);
					param.datatype	=	"datagrid";
				},
				onLoadSuccess:function(data){
					// 各グリッドの値を保持する
					var txt_sircd = $.getInputboxValue($('#'+$.id_inp.txt_sircd));
					var gridData = that.getGridData(id);
					that.setGridData(gridData, id);
					that.queried = true;
				},
				onClickRow: function(index,field){$.clickEditableDatagridCell(that,id, index)},
				onBeginEdit:function(index,row){$.beginEditDatagridRow(that,id, index, row)},
				onEndEdit: function(index,row,changes){$.endEditDatagridRow(that, id, index, row)}
			});
		},
		getGridData: function (sircd, target){
			var that = this;
			var targetRows= [];

			if(target===undefined || target==='grd_tenhtsu_arr'){
				var rowsFsirt= $('#grd_tenhtsu_arr').datagrid('getRows');
				for (var i=0; i<rowsFsirt.length; i++){
					var rowDate = {
							F1	 : rowsFsirt[i]["TENCD"],
							F2	 : rowsFsirt[i]["SURYO"],
					};
					targetRows.push(rowDate);
				}
			}
			return targetRows;
		},
		setGridData: function (data, target){
			var that = this;

			// 複数仕入先店舗
			if(target===undefined || target==='grd_tenhtsu_arr'){
				that.grd_fsirt_data =  data['grd_tenhtsu_arr'];
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

			// 戻る実行時用に現在の画面情報を保持する
			var states = $.getBackBaseJSON(that);
			// 各種グリッド情報を設定
			var newrepinfos = $.getBackJSON(that, states, undefined, true);
			$.setJSONObject(sendJSON, 'repinfo', newrepinfos, '');

			// 呼出別処理
			switch (btnId) {
			case $.id.btn_back:
			case $.id.btn_cancel:
			case "btn_return":

				var callpage = $.getJSONValue(this.jsonHidden, 'callpage');
				if (callpage==='Out_ReportJU022') {
					var txt_moyscd		= $.getInputboxValue($('#'+$.id_inp.txt_moyscd));		// 催しコード
					var txt_moykn		= $.getInputboxValue($('#'+$.id_inp.txt_moykn));		// 催し名（漢字）
					var kikan_dummy		= $.getInputboxValue($('#'+'kikan_dummy'));				// 納入期間
					var txt_qasmdt		= $.getInputboxValue($('#'+$.id_inp.txt_qasmdt));		// 店舗締切日
					$.setJSONObject(sendJSON, $.id_inp.txt_moyscd,txt_moyscd, txt_moyscd);
					$.setJSONObject(sendJSON, $.id_inp.txt_moykn,txt_moykn, txt_moykn);
					$.setJSONObject(sendJSON, 'kikan_dummy',kikan_dummy, kikan_dummy);
					$.setJSONObject(sendJSON, $.id_inp.txt_qasmdt,txt_qasmdt, txt_qasmdt);
					$.setJSONObject(sendJSON, 'callpage', callpage, callpage);								// 呼出し元レポート情報
					sendMode = 1;
				} else {
					$.setJSONObject(sendJSON, 'callpage', that.name, that.name);								// 呼出し元レポート情報
					sendMode = 2;
				}

				$.setJSONObject(sendJSON, 'reportYobi1', that.reportYobiInfo(), that.reportYobiInfo());		// 参照情報保持
				// 転送先情報
				index = 4;
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
		keyEventInputboxFunc:function(e, code, that, obj){

			// *** Enter or Tab ****
			if(code === 13 || code === 9){
				var newValue = obj.val();
				var id = $(obj).attr("orizinid");

				// 発注数か数量ランクの入力後のフォーカス移動先は登録
				if (id===$.id_inp.txt_htsu) {
					if (!$.isEmptyVal(newValue)) {
						that.unSelectIndex = 0;
					}
				}
				if (id===$.id_inp.txt_suryoptn) {
					if (!$.isEmptyVal(newValue)) {
						that.unSelectIndex = 1;
					}
					that.keyEvent = true;
				}
				if (id===$.id_inp.txt_rg_baikaam) {
					that.oldValue = $.getInputboxValue($('#'+id));
				}
			}

			if (code === 13) {
				if(that.btnUpdFocus){
					$('#'+$.id.btn_upd).click();
					e.preventDefault();
				}
			}
		},
		changeInputboxFunc:function(that, id, newValue, obj){
			var that = this;

			var parentObj = $('#'+that.focusRootId);
			var txt_shncd = $.getInputboxValue($('#'+$.id_inp.txt_shncd));

			if(id+"_"===obj.attr('id') && that.focusGridId!==""){
				parentObj = $('#'+that.focusGridId).datagrid('getPanel');
			}

			var shncd = newValue;
			var shnkbn = $.getInputboxValue($('#sel_shohinkbn1'));
			var forinp_id = id;
			var shoriDt = $.getInputboxValue($('#'+$.id.txt_shoridt));
			var txt_moyscd = $.getInputboxValue($('#'+$.id_inp.txt_moyscd));
			if (id==='sel_shohinkbn1') {
				shncd = $.getInputboxValue($('#'+$.id_inp.txt_shncd));
				shnkbn = newValue;
				forinp_id = $.id_inp.txt_shncd;

				// 商品区分0以外のものを選択すると以下の項目が編集可能
				if (newValue!=='0') {
					// 更新非対象項目は非活性に
					$.setInputBoxEnableVariable($('#'+$.id_inp.txt_irisu),true);
					$.setInputBoxEnableVariable($('#'+$.id_inp.txt_genkaam),true);
					$.setInputBoxEnableVariable($('#'+$.id_inp.txt_rg_baikaam),true);
				} else {
					$.setInputBoxDisableVariable($('#'+$.id_inp.txt_irisu),true);
					$.setInputBoxDisableVariable($('#'+$.id_inp.txt_genkaam),true);
					$.setInputBoxDisableVariable($('#'+$.id_inp.txt_rg_baikaam),true);
				}
			} else if (id===$.id_inp.txt_rg_baikaam) {
				if (newValue==='0') {
					$.setInputboxValue($('#'+id),that.oldValue);
				} else {
					shncd = $.getInputboxValue($('#'+$.id_inp.txt_shncd));
					var param = [{"value":newValue,"shncd":shncd,"stdt":txt_moyscd.substring(1,7)}];
					var rows = $.getSelectListData(that.name, $.id.action_change, forinp_id, param);
					var row = rows.length > 0 ? rows[0]:"";
					if ($.isEmptyVal(row.F1)) {
						$.setInputboxValue($('#'+$.id_inp.txt_baikaam),newValue);
					} else {
						var zeirt = row.F1 / 100;
						var result = Math.ceil(newValue / (1+zeirt));
						$.setInputboxValue($('#'+$.id_inp.txt_baikaam),result);
					}
				}
			}

			var size = $('[for_inp^='+forinp_id+'_]').length ;

			// DB問い合わせ系
			if(size > 0){

				var param = '';

				if (forinp_id===$.id_inp.txt_shncd) {
					param = [{"value":shncd,"shnkbn":shnkbn,"stdt":txt_moyscd.substring(1,7)}];
				} else {
					param = that.getInputboxParams(that, forinp_id, newValue);
				}

				var rows = $.getSelectListData(that.name, $.id.action_change, forinp_id, param);
				var row = rows.length > 0 ? rows[0]:"";
				$.setInputboxRowData('for_inp', forinp_id, row, that, parentObj);
			}
		},
		checkInputboxFunc: function(id, newValue){

			var that = this;

			// 発注日
			if (id===$.id_inp.txt_htdt) {
				var inpNnDt		= $.getInputboxValue($('#'+$.id_inp.txt_nndt));
				var shoridt		= $.getInputboxValue($('#'+$.id.txt_shoridt));

				// 発注日 ＜ 納入日の条件で入力してください。
				if (inpNnDt <= newValue) {
					return "E20264";
				}

				// 発注日>処理日付の条件で入力してください。
				if (shoridt >= newValue) {
					return "E20127";
				}
			}

			// 納入日
			if (id===$.id_inp.txt_nndt) {
				var moyNnStDt	= $.getJSONValue(this.jsonHidden, $.id_inp.txt_nnstdt);
				var moyNnEdDt	= $.getJSONValue(this.jsonHidden, $.id_inp.txt_nneddt);

				// 納入日は納入期間の範囲で入力してください。
				if(moyNnStDt > newValue || moyNnEdDt < newValue){
					return "E20274";
				}

				// 催し区分=9で既に登録されている商品コードです。登録できません。
				var param = {};
				param["KEY"] =  "";

				$('#'+that.focusRootId).find('[col^=F]').each(function(){
					var col = $(this).attr('col');
					if (["F1","F2","F3","F6","F8","F9","F10","F12","F13"].indexOf(col)!==-1) {
						var val = $.getInputboxValue($(this));
						param[col] = val;
					}
				});
				var arr = $.getSelectListData(that.name, $.id.action_check, $.id_inp.txt_moyscd, [param]);
				if (arr[0].VALUE!=='0') {
					return ['E20162','催し区分=9で'];
				}
			}

			// 店ランク
			if (id===$.id_inp.txt_tenrank) {
				var htsu	= $.getInputboxValue($('#'+$.id_inp.txt_htsu));
				var rank	= $.getInputboxValue($('#'+$.id_inp.txt_tenrank+'_2'));
				var ptn		= $.getInputboxValue($('#'+$.id_inp.txt_suryoptn));

				// 同一数量発注入力の場合、店別数量発注入力登録できません。
				if ((!$.isEmptyVal(newValue) || !$.isEmptyVal(htsu)) &&
						(!$.isEmptyVal(rank) || !$.isEmptyVal(ptn))) {
					return "E20461";
				} else if ($.isEmptyVal(newValue) && $.isEmptyVal(htsu)) {
					that.tenht = '2';
				}

				var targetRows	= that.getGridData('grd_tenhtsu_arr');
				for (var i = 0; i < targetRows.length; i++) {
					if (!$.isEmptyVal(targetRows[i].F2)) {
						that.tenht = '3';
						break;
					}
				}

				// 同一数量発注入力の場合、店別数量発注入力登録できません。
				if ((!$.isEmptyVal(newValue) || !$.isEmptyVal(htsu)) && that.tenht==='3') {
					return "E20462";
				}

				// 同一発注数量、ランク別発注数量、店別発注数量のいづれか一つは入力してください。
				if ($.isEmptyVal(newValue) && $.isEmptyVal(htsu) && $.isEmptyVal(rank) && $.isEmptyVal(ptn) && that.tenht!=='3') {
					return "E20569";
				}

				if (that.tenht==='1') {
					if ($.isEmptyVal(newValue) && !$.isEmptyVal(htsu)) {
						return ['EX1103','ランク'];
					}

					var shncd = $.getInputboxValue($('#'+$.id_inp.txt_shncd));
					var moyscd = $.getInputboxValue($('#'+$.id_inp.txt_moyscd));
					if(shncd && shncd != ''){
						if(Number(newValue) >= 900 ){
							// 臨時ランクマスタ検索
							var param = {};
							param["KEY"] =  "MST_CNT";
							param["value"] = shncd.substring(0, 2) + ',' + newValue + ',' + moyscd;
							var chk_cnt = $.getInputboxData(that.name, $.id.action_check,  $.id_inp.txt_rankno + '_EX', [param]);
							if(chk_cnt==="" || chk_cnt==="0"){
								return "E20466";
							}
						}else{
							// ランクマスタ検索
							var param = {};
							param["KEY"] =  "MST_CNT";
							param["value"] = shncd.substring(0, 2) + ',' + newValue;
							var chk_cnt = $.getInputboxData(that.name, $.id.action_check,  $.id_inp.txt_rankno, [param]);
							if(chk_cnt==="" || chk_cnt==="0"){
								return "E20057";
							}
						}
					}
				}
			}

			// 発注数
			if (id===$.id_inp.txt_htsu) {
				var rank = $.getInputboxValue($('#'+$.id_inp.txt_tenrank));
				if (that.tenht==='1') {
					if ($.isEmptyVal(newValue) && !$.isEmptyVal(rank)) {
						return ['EX1103','発注数'];
					}
				}

			}

			// ランク2
			if (id===$.id_inp.txt_tenrank+'_2') {

				var ptn = $.getInputboxValue($('#'+$.id_inp.txt_suryoptn));

				// ランク別発注数量入力の場合、店別数量発注入力登録できません。
				if ((!$.isEmptyVal(newValue) || !$.isEmptyVal(ptn))) {
					if (that.tenht==='3') {
						return "E20463";
					}
					that.tenht = "2";
				}

				if (that.tenht==='2') {
					if ($.isEmptyVal(newValue) && !$.isEmptyVal(ptn)) {
						return ['EX1103','ランク'];
					}

					var shncd = $.getInputboxValue($('#'+$.id_inp.txt_shncd));
					var moyscd = $.getInputboxValue($('#'+$.id_inp.txt_moyscd));
					if(shncd && shncd != ''){
						if(Number(newValue) >= 900 ){
							// 臨時ランクマスタ検索
							var param = {};
							param["KEY"] =  "MST_CNT";
							param["value"] = shncd.substring(0, 2) + ',' + newValue + ',' + moyscd;
							var chk_cnt = $.getInputboxData(that.name, $.id.action_check,  $.id_inp.txt_rankno + '_EX', [param]);
							if(chk_cnt==="" || chk_cnt==="0"){
								return "E20466";
							}
						}else{
							// ランクマスタ検索
							var param = {};
							param["KEY"] =  "MST_CNT";
							param["value"] = shncd.substring(0, 2) + ',' + newValue;
							var chk_cnt = $.getInputboxData(that.name, $.id.action_check,  $.id_inp.txt_rankno, [param]);
							if(chk_cnt==="" || chk_cnt==="0"){
								return "E20057";
							}
						}
					}
				}
			}

			// 数量パターン
			if (id===$.id_inp.txt_suryoptn) {
				var rank = $.getInputboxValue($('#'+$.id_inp.txt_tenrank+'_2'));
				if (that.tenht==='2') {
					if ($.isEmptyVal(newValue) && !$.isEmptyVal(rank)) {
						return ['EX1103','パターン'];
					}

					var shncd = $.getInputboxValue($('#'+$.id_inp.txt_shncd));
					var moyscd = $.getInputboxValue($('#'+$.id_inp.txt_moyscd));
					if(shncd && shncd != ''){
						var chk_cnt = '';
						if(Number(newValue) >= 900 ){
							// 臨時数量パターンマスタ検索
							var param = {};
							param["KEY"] =  "MST_CNT";
							param["value"] = shncd.substring(0, 2) + ',' + newValue + ',' + moyscd.slice(0,1) + ',' + moyscd.slice(1,7) + ',' + moyscd.slice(7);
							chk_cnt = $.getInputboxData(that.name, $.id.action_check, 'txt_sryptnno', [param]);
						}else{
							// 数量パターンマスタ検索
							var param = {};
							param["KEY"] =  "MST_CNT";
							param["value"] = shncd.substring(0, 2) + ',' + newValue;
							chk_cnt = $.getInputboxData(that.name, $.id.action_check, 'txt_sryptnno', [param]);
						}
						if(chk_cnt==="" || chk_cnt==="0"){
							return "E20131";
						}
					}
				}
			}

			// 商品コード重複・マスタ存在チェック
			if (id===$.id_inp.txt_shncd) {
				if (newValue !== null && newValue !== '' && newValue !== undefined) {
					// マスタ存在チェック
					var param = {};
					param["KEY"] =  "MST_CNT";
					param["value"] = newValue;

					var chk_cnt = $.getInputboxData(that.name, $.id.action_check, $.id_inp.txt_shncd, [param]);
					if(chk_cnt==="" || chk_cnt==="0"){
						return "E11046";
					}
				}


			}
			return null;
		},
		getInputboxParams: function(that, id, newValue){
			// 情報取得
			var values = {};
			values["value"] = newValue;
			values["TABLEKBN"] = that.baseTablekbn;

			// 情報設定
			return [values];
		},
		// id_inp宣言の入力項目を共通で設定する
	} });
})(jQuery);