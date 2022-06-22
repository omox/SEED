/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_Reportx142',			// （必須）レポートオプションの確認
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
		dedefaultObjNum:	8,	// 初期化オブジェクト数
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
		gridData:[],						// 検索結果
		gridTitle:[],						// 検索結果
		gridTitle:[],						// 検索結果
		grd_tenpo_m:[],				// グリッド情報:配送パターンマスタ
		grd_tenpo_data:[],				// グリッド情報:配送パターンマスタ
		btnid:"",
		yes:false,
		kaitou:false,
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
			$.getsetInputboxData(reportno, $.id.txt_shoridt, [{}], $.id.action_init);
			var isUpdateReport = true;

			// 初期検索可能
			that.onChangeReport = true;

			that.setTenkyuFlg(that, reportno, $.id_mei.kbn316+'_kikan', isUpdateReport);

			var count = 2;
			// 名称マスタ参照系
			var meisyoSelect = Object.getOwnPropertyNames($.id_mei);
			for ( var sel in meisyoSelect ) {
				if($('#'+$.id_mei[meisyoSelect[sel]]).length > 0){
					$.setMeisyoCombo(that, reportno, $.id_mei[meisyoSelect[sel]], isUpdateReport);
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

			// 編集可能データグリッドの共通処理設定
			// 編集エディターの機能拡張（非表示で参照用のオブジェクトが準備してある前提、参照オブジェクトはグリッドのフィールド名を元に判断）
			$.extendDatagridEditor();

			$('#btn_tab1').click(function() {
				that.btnid = "tab1";
			});

			$('#btn_tab2').click(function() {
				that.btnid = "tab2";
			});

			// 店グループ（仕入）
			that.setEditableGrid(that, reportno, $.id.grd_tenpo_m);
			$('#'+$.id.btn_tab1).on("click", $.pushUpd);
			$('#'+$.id.btn_tab2).on("click", $.pushUpd);

			// 初期化終了
			this.initializes =! this.initializes;

			// キーイベントの設定
			$.initKeyEvent(that);
			//this.initKeyEvent(that);
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
			// 各種遷移ボタン
			$('sel1').on("click", $.pushChangeReport);
			$('#'+$.id.btn_new).on("click", $.pushChangeReport);
			$('#'+$.id.btn_copy).on("click", $.pushChangeReport);
			$('#'+$.id.btn_cancel).on("click", $.pushChangeReport);
			$('#'+$.id.btn_cancel2).on("click", $.pushChangeReport);
			$('#'+$.id.btn_back2).on("click", $.pushChangeReport);
			// 変更
			$($.id.hiddenChangedIdx).val('');

			$.initReportInfo("TP011", "店舗休日マスタ　新規", "新規");
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
			return rt;
		},
		success: function(reportno, sortable){	// （必須）正処理

			if (sortable) sortable=1; else sortable=0;
			var that = this;
			// 検索実行
			var txt_tencd		= $.getJSONObject(this.jsonString, $.id_inp.txt_tencd).value;		// 店舗コード

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$.appendMaskMsg();

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,		// レポート名
					TENCD:			txt_tencd,
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

					// グリッド再描画
					$('#'+$.id.grd_tenpo_m).datagrid('reload');

					// 状態保存
					$.saveState2(reportno, that.getJSONString());

					// ログ出力
					$.log(that.timeData, 'loaded:');
				}
			);
		},
		updValidation: function (){	// （必須）批准
			var that = this;

			var row = $('#'+$.id.grd_tenpo_m).datagrid("getSelected");
			var rowIndex = $('#'+$.id.grd_tenpo_m).datagrid("getRowIndex", row);
			$('#'+$.id.grd_tenpo_m).datagrid('endEdit',rowIndex);

			// EasyUI のフォームメソッド 'validate' 実施

			var shdt = $.convDate($('#'+$.id.txt_shoridt).val(), false);
			if(that.btnid == "tab1"){
				var rt1 = $($.id.toolbarform+"_1").form('validate');
				if(!rt1){
					$.showMessage('E00001');
					return rt1;
				}
				// 日付チェック
				var dt = $.convDate($.getInputboxValue($('#'+$.id_inp.txt_tenkyudt)));
				if($.getDateDiffDay(shdt,dt)>100){
					$.showMessage('EX1014');
					return false;
				}
				// 店舗コードの重複チェック
				var srccds = [];
				var targetRowsSrccd = $('#'+$.id.grd_tenpo_m).datagrid('getRows');

				for (var i=0; i<targetRowsSrccd.length; i++){
					if(targetRowsSrccd[i]["TENCD"] != null && targetRowsSrccd[i]["TENCD"] != ""){
					srccds.push(targetRowsSrccd[i]["TENCD"]);
					}
				}
				var srccds_ = srccds.filter(function (element, index, self) { return self.indexOf(element)*1 === index; });
				if(srccds.length<=0){
					$.showMessage('EX1036');
					return false;
				}
				if(srccds.length !== srccds_.length){
					$.showMessage('E11141');
					return false;
				}
				var targetdate = [];
				var targetRows = $('#'+$.id.grd_tenpo_m).datagrid('getRows');
				for (var i=0; i<targetRows.length; i++){
					if(targetRows[i]["TENCD"]){
						var param = {};
						param["KEY"] =  "MST_CNT",
						param["TENCD"] = targetRows[i]["TENCD"],
						param["TENKYUDT"] = $.getInputboxValue($('#'+$.id_inp.txt_tenkyudt));
						var rt1 = $.getInputboxData(that.name, $.id.action_check,  $.id_inp.txt_tenkyudt, [param]);
						if(rt1 > 0){
							$.showMessage('E00004');
							return false;
						}
					}
				}
				for (var i=0; i<targetRows.length; i++){
					if(targetRows[i]["TENCD"]){
						var param = {};
						param["KEY"] =  "MST_CNT",
						param["value"] = targetRows[i]["TENCD"]
						var rt1 = $.getInputboxData(that.name, $.id.action_check,  $.id_inp.txt_tencd, [param]);
						if(rt1 <= 0){
							$.showMessage('E11096');
							return false;
						}
					}
				}
				return rt1;
			}
			if(that.btnid == "tab2"){

				var rt2 = $($.id.toolbarform+"_2").form('validate');
				if(!rt2){
					$.showMessage('E00001');
					return rt2;
				}
				var sdt = $.convDate($.getInputboxValue($('#'+$.id_inp.txt_tenkyustdt)));
				var edt = $.convDate($.getInputboxValue($('#'+$.id_inp.txt_tenkyuendt)));

				var days = $.getDateDiffDay(shdt,sdt);
				// 日付チェック
				if($.getDateDiffDay(shdt,sdt)>100 && $.getDateDiffDay(shdt,edt)>100){
					$.showMessage('EX1014');
					return false;
				}
				var days = $.getDateDiffDay(sdt, edt);
				if(days<0){
					$.showMessage('E11020');
					return false;
				}
				var targetKikanRows= [];
				for (var i=0; i<=days; i++){
						var param = {};
						param["KEY"] =  "MST_CNT",
						param["TENCD"] = $.getInputboxValue($('#'+$.id_inp.txt_tencd)),
						param["TENKYUDT"] =$.dateFormat(sdt,'yyyymmdd');
						var rt2 = $.getInputboxData(that.name, $.id.action_check,  $.id_inp.txt_tenkyudt, [param]);
						sdt.setDate(sdt.getDate() + 1);
						if(rt2 > 0){
							$.showMessage('E00004');
							return false;
						}
				}

				var param = {};
				param["KEY"] =  "MST_CNT",
				param["value"] = $.getInputboxValue($('#'+$.id_inp.txt_tencd))
				var rt1 = $.getInputboxData(that.name, $.id.action_check,  $.id_inp.txt_tencd, [param]);
				if(rt1 <= 0){
					$.showMessage('E11096');
					return false;
				}
				return rt2;
			}
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

			// 新規登録時には配送パターン
			/*if(that.sendBtnid =  $.id.btn_sel_change){
				var enptyrows = [];
				targetDatas = enptyrows;
			}*/

			// エリア別設定グリッドのデータを取得
			var targetDatas_Ten = [];
			var txt_tenkyudt = $('#'+$.id_inp.txt_tenkyudt).textbox('getValue');
			targetDatas_Ten = that.getGridData($.id.grd_tenpo_m);

			var targetRowstenpo= that.grd_tenpo_m;

			var targetDatas_Kikan = [];
			targetDatas_Kikan = that.getGridData('KIKAN');

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
//					IDX:			$($.id.hiddenChangedIdx).val(),	// 更新対象Index
					DATA:			JSON.stringify(targetDatas),	// 更新対象情報
					DATA_TEN:		JSON.stringify(targetDatas_Ten),
					DATA_KIKAN:		JSON.stringify(targetDatas_Kikan),
					t:				(new Date()).getTime()
				},
				function(data){
					// 検索処理エラー判定
					if($.updError(id, data)) return false;

					var afterFunc = function(){
						// 初期化
						that.clear();
						that.changeReport(that.name, 'btn_return');
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

			// 店グループ
			this.jsonTemp.push({
				id:		$.id_inp.txt_tencd,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_tencd),
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
		setEditableGrid: function(that, reportno, id){		// データ表示
			that.editRowIndex[id] = -1;
			var index = -1;
			$('#'+id).datagrid({
				url:$.reg.easy,
				onBeforeLoad:function(param){
					index = -1;
					var values = {};

					values["callpage"]	 = $($.id.hidden_reportno).val()										// 呼出元レポート名

					var json = [values];
					// 情報設定
					param.page		=	reportno;
					param.obj		=	id;
					param.sel		=	(new Date()).getTime();
					param.target	=	id;
					param.action	=	$.id.action_init;
					param.json		=	JSON.stringify(json);
					param.datatype	=	"datagrid";
				},

				onClickRow: function(index,field){
						$.clickEditableDatagridCell(that,id, index);
				},
				onBeginEdit:function(index,row){
						$.beginEditDatagridRow(that,id, index, row)
				},
				onEndEdit: function(index,row,changes){$.endEditDatagridRow(that, id, index, row)},
			});
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
		setTenkyuFlg: function(that, reportno, id, isUpdateReport){
			// 名称区分が重複する入力項目が存在する為、専用のcomboboxを作成するfunctionを使用する。
			var idx = -1;

			var tag_options = $('#'+id).attr('data-options');
			if(tag_options){
				tag_options = '\"'+tag_options.replace(/'/g, '').replace(/,/g, '\",\"').replace(/:/g, '\":\"')+'\"';
			}
			var options = tag_options ? JSON.parse('{'+tag_options+'}'): JSON.parse('{}');

			var required = options && options.required;
			var topBlank = !required;
			var panelWidth = options && options.panelWidth ? options.panelWidth : null;
			var panelHeight = options && options.panelHeight ? options.panelHeight :'auto';
			var suffix = that.suffix ? that.suffix : '';
			var changeFunc1 = null;
			if(isUpdateReport){
				changeFunc1 = function(){
					if(idx > 0 && that.queried){
						$($.id.hiddenChangedIdx+suffix).val("1");
					}
				};
			}
			// 当項目を変更した際に、値をセットする項目がある場合の処理（セット先の項目は、HTMLタグとして、for_inp=当項目のID_列名）
			var changeFunc2 = null;
						if ($.isFunction(that.changeInputboxFunc)){
				changeFunc2 = function(newValue, obj){
					that.changeInputboxFunc(that, id, newValue, obj);
				};
			}else{
				if($('[for_inp^='+id+'_]').length > 0){
					changeFunc2 = function(newValue){
						var param = [{"value":newValue}];
						$.getsetInputboxRowData(reportno, 'for_inp', id, param, that);
					};
				}
			}

			// フォーカスアウトのタイミングの動作
			$('#'+id).next().on('focusout', function(e){
				var obj = $(this).prev();

				if (!$.setComboReload(obj,false)) {
					obj.combobox('reload');
				}
			});

			$('#'+id).combobox({
				url:$.reg.easy,
				required: required,
				editable: true,
				autoRowHeight:false,
				panelWidth:panelWidth,
				panelHeight:panelHeight,
				valueField:'VALUE',
				textField:'TEXT',
				multiple :false,
				prompt: '',
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
					// 情報設定
					var json = [{
						DUMMY: 'DUMMY'
					}];
					if(topBlank){json[0]['TOPBLANK'] = topBlank;}

					param.page		=	reportno;
					param.obj		=	$.id_mei.kbn316;
					param.sel		=	(new Date()).getTime();
					param.target	=	$.id_mei.kbn316;
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
					if(suffix===''){
						if(isUpdateReport){
							// 初期表示処理
							$.initialDisplay(that);
						}else{
//							// 検索ボタン有効化
//							$.setButtonState('#'+$.id.btn_search, true, id);
							// 初期表示検索処理
							$.initialSearch(that);
						}
					}
				},
				onChange:function(newValue, oldValue, obj){
					if(obj===undefined){obj = $(this);}

					if(changeFunc1!==null){ changeFunc1();}
					if(changeFunc2!==null){ changeFunc2(newValue, $(this));}

					that.getComboErr(obj,false,newValue,oldValue);
				}
			});
		},
		getGridData: function (target){
			var that = this;

			var data = {};

			// 基本情報
			if(target===undefined || target==="grd_data"){
				var targetData = [{}];
				$('#'+that.focusRootId).find('[col^=F]').each(function(){
					var col = $(this).attr('col');
					targetData[0][col] = $.getInputboxValue($(this));
				});
				data = targetData;
			}
			// 店舗
			if(target===undefined || target===$.id.grd_tenpo_m){
				var rowsAreahsptn	 = $('#'+$.id.grd_tenpo_m).datagrid('getRows');
				var targetRows= [];
				for (var i=0; i<rowsAreahsptn.length; i++){
					var rowDate = {
							IDX	 : $.getParserDt(rowsAreahsptn[i]["IDX"]),
							F3	 : $.getParserDt(rowsAreahsptn[i]["TENCD"]),
					};

					if($.getParserDt(rowsAreahsptn[i]["TENCD"])){
						targetRows.push(rowDate);
					}
				}
				data = targetRows;
			}
			if(target===undefined || target==='KIKAN'){
			var sdt = $.convDate($.getInputboxValue($('#'+$.id_inp.txt_tenkyustdt)));
			var edt = $.convDate($.getInputboxValue($('#'+$.id_inp.txt_tenkyuendt)));

			var days = $.getDateDiffDay(sdt, edt);
			var targetKikanRows= [];

			for (var i=0; i<=days; i++){
				sdt+i;
				var rowDate = {
						F5	: $.dateFormat(sdt,'yyyymmdd')
				}
				sdt.setDate(sdt.getDate() + 1);
				targetKikanRows.push(rowDate);
			}
			data = targetKikanRows;
			}
			return data;
		},
		setGridData: function (data, target){
			var that = this;

			// 実仕入先一覧
			if(target===undefined || target===$.id.grd_tenpo_m){
				that.grd_tenpo_data =  data[$.id.grd_tenpo_m];
			}
		},
		setObjectState: function(){	// 軸の選択内容による制御

		},
		getGridParams:function(that, id){
			var values = {};
			values["callpage"] = $($.id.hidden_reportno).val()										// 呼出元レポート名

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
			var sendMode = "";

			// タブ要素(a)取得
			var elems = $('#tabContent', window.parent.document).map(
				function(i,e) {
					return e;
				}).get();
			var href = elems[0].value.split(',');

			// JSON Object Clone ()
			var sendJSON = [];
			$.setJSONObject(sendJSON, 'sendBtnid', btnId, $('#'+btnId).attr("title"));		// 実行ボタン情報保持

			// 戻る実行時用に現在の画面情報を保持する
			var states = $.getBackBaseJSON(that);
			// TODO 各種グリッド情報を設定
			var newrepinfos = $.getBackJSON(that, states);
			$.setJSONObject(sendJSON, 'repinfo', newrepinfos, '');
			// 呼出別処理
			switch (btnId) {
			case $.id.btn_back:
				// 転送先情報
				sendMode = 2;
				index = 1;
				childurl = href[index];

				break;
			case $.id.btn_back2:
				// 転送先情報
				sendMode = 2;
				index = 1;
				childurl = href[index];

				break;
			case $.id.btn_cancel:
				// 転送先情報
				sendMode = 2;
				index = 1;
				childurl = href[index];

				break;
			case $.id.btn_cancel2:
				// 転送先情報
				sendMode = 2;
				index = 1;
				childurl = href[index];

				break;
			case "btn_return":
				// 転送先情報
				sendMode = 2;
				index = 1;
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
		changeInputboxFunc:function(that, id, newValue, obj){

			if(id == 'tt'){
					// レポート番号取得
					var reportno=$($.id.hidden_reportno).val();
					// レポート定義位置
					var reportNumber = $.getReportNumber(reportno);
					if (typeof(reportNumber) !== 'number') { alert("レポート定義が見つかりません。"); return false;}
					// JS情報取得
					var that = $.report[reportNumber];
					var id = $(this).attr('id');
					if(that.yes == true&&that.kaitou==false){
						$($.id.hiddenChangedIdx).val('');
						that.yes = false;
					}

					var func_ok = function(r){
						// セッションタイムアウト、利用時間外の確認
						var isTimeout = $.checkIsTimeout();
						if (! isTimeout) {
							// 検索条件保持
							$.ajax({
								url: $.reg.easy,
								type: 'POST',
								async: false,
								data: {
									"page"	: reportno,
									"obj"	: $.id.btn_search,
									"sel"	: "json",
									"userid": $($.id.hidden_userid).val(),
									"user"	: $($.id.hiddenUser).val(),
									"report": $($.id.hiddenReport).val(),
									"json"	: JSON.stringify($.report[reportNumber].getJSONString())
								},
								success: function(json){
									if(newValue == 1){
										$('#'+$.id_mei.kbn316+'_kikan').combobox('reload');
										$('#'+$.id_inp.txt_tencd).textbox('setValue', "");
										$('#'+$.id_inp.txt_tenkyustdt).textbox('setValue', "");
										$('#'+$.id_inp.txt_tenkyuendt).textbox('setValue', "");
									}else if(newValue == 0){
										$('#'+$.id.grd_tenpo_m).datagrid('reload');
										$('#'+$.id_mei.kbn316).combobox('reload');
										$('#'+$.id_inp.txt_tenkyudt).textbox('setValue', "");
									}
									that.yes = true;
									$($.id.hiddenChangedIdx).val('');
								}
							});
						}
						return false;
					};
					var func_no = function(r){
						// セッションタイムアウト、利用時間外の確認
						var isTimeout = $.checkIsTimeout();
						if (! isTimeout) {
							// 検索条件保持
							$.ajax({
								url: $.reg.easy,
								type: 'POST',
								async: false,
								data: {
									"page"	: reportno,
									"obj"	: $.id.btn_search,
									"sel"	: "json",
									"userid": $($.id.hidden_userid).val(),
									"user"	: $($.id.hiddenUser).val(),
									"report": $($.id.hiddenReport).val(),
									"json"	: JSON.stringify($.report[reportNumber].getJSONString())
								},
								success: function(json){
									if(newValue == 1){
										that.kaitou = true;
										$('#tt').tabs('select',1);
									}else if(newValue == 0){
										that.kaitou = true;
										$('#tt').tabs('select',0);
									}
								}
							});
						}
						return false;
					};
					var changedIndex = $($.id.hiddenChangedIdx).val().split(",");
					if(that.kaitou==false && changedIndex!=""){
						$.showMessage("EX1099", undefined, func_ok,func_no);
					}else{
						that.kaitou=false;
					}
			}

			var parentObj = $('#'+that.focusRootId);
			if(id+"_"===obj.attr('id') && that.focusGridId!==""){
				parentObj = $('#'+that.focusGridId).datagrid('getPanel');
			}

			// DB問い合わせ系
			if($('[for_inp^='+id+'_]').length > 0){
				var param = that.getInputboxParams(that, id, newValue);
				$.getsetInputboxRowData(that.name, 'for_inp', id, param, that, parentObj);
			}

			// 検索、入力後特殊処理
			if(that.queried){
				// 特殊処理
				//var bmncd = $.getInputboxValue($('#'+$.id_inp.txt_bmncd));
				if(that.focusGridId === $.id.grd_srccd && that.editRowIndex[$.id.grd_srccd] === 0 && bmncd.length > 0 && $.getInputboxValue($('#'+$.id_inp.txt_makercd)).length === 0){

				}

				// 新規：新規・新規コピー・選択コピーボタン押下時
				if(that.sendBtnid===$.id.btn_new||that.sendBtnid===$.id.btn_copy||that.sendBtnid===$.id.btn_sel_copy){

				}

				// 存在チェック：店舗コード系
				if(id===$.id_inp.txt_centercd ||id===$.id_inp.txt_ycentercd){

				}
			}
		},
		getInputboxParams: function(that, id, newValue){
			// 情報取得
			var values = {};
			values["value"] = newValue;
			values["TABLEKBN"] = that.baseTablekbn;


			// 情報設定
			return [values];
		},
		// キーイベント初期設定
		initKeyEvent : function(that) {

			var index = $('#'+$.id.grd_tenpo_m).datagrid('selectRow')

				// var that = this
				// キー移動イベントの設定
				that.focusParentId = that.focusRootId;
				this.setReadyKeyEvent(that);	// 初期化したオブジェクトに対し、キーイベントの準備を行う
				$('#'+that.focusRootId).find('[tabindex]').each(function(){ $.setKeyEvent(that, $(this)); });	// tabindexが設定された項目に対し、キーイベントの設定を行う
				$('#'+that.focusRootId).find('[tabindex]').filter("[tabindex!=-1]").filter('[disabled!=disabled]').filter(":visible").sort(function(a, b) {
					return parseInt($(a).attr('tabIndex'), 10) - parseInt($(b).attr('tabIndex'), 10);
				}).eq(0).focus();
		},
		setReadyKeyEvent : function(js) {
			// グリッドからフォーカスが外れた場合の処理
			var func_focusout_editgrid = function(e){
				if(js.focusGridId !==''){
					if(js.editRowIndex!==undefined && js.editRowIndex[js.focusGridId]!==undefined){
						if(js.editRowIndex[js.focusGridId]!==-1){
							$.endEditingDatagrid(js)
						}
					}
				}
			};

			// マウス操作等のタブの遷移時のフォーカス指定
			$('#'+js.focusRootId).find('.easyui-tabs').each(function(){
				var id = $(this).attr('id');
				$('#'+id).tabs({
					onSelect: function(title,index){
						$($('#'+id).tabs('getTab', index)).find('[tabindex]').filter("[tabindex!=-1]").filter('[disabled!=disabled]').eq(0).focus();
					}
				});
			});
			// easyui系の項目は、HTMLから実際画面上で操作する項目を作成するので、キーイベント用に項目の設定を行う
			$('#'+js.focusRootId).find('.easyui-datagrid').each(function(){
				var that = this;
				// targetに設定をコピー
				var target = $(that).parents('.datagrid').eq(0);
				target.attr('tabindex', $(that).attr('tabindex'));
				// キーイベントの対象となってしまうのでコピー元にtabindex=-1設定
				$(that).attr('tabindex', -1);
				// gridのキー移動は、該当gridのID指定が前提になっているため、フォーカス処理で、グリッドIDを保持、一行目選択
				target.on('focus', function(e){
					var gridId = $(that).attr('id');
					// フォーカスがあたったグリッドと編集中グリッドが異なる場合は編集終了確認追加
					if(js.focusGridId!==gridId){
						func_focusout_editgrid(e);
					}
					// 直前に選択していたグリッドと異なる
					if($('#'+gridId).datagrid('getRows').length > 0 && (js.focusGridId ===''|| js.focusGridId!==gridId)){
						js.focusGridId = gridId;
						var rowindex = 0;
						if($('#'+js.focusGridId).datagrid('getSelected')!==null){
							rowindex = $('#'+js.focusGridId).datagrid('getRowIndex', $('#'+js.focusGridId).datagrid('getSelected'));
						}
						var isEditing = undefined;
						if(js.editRowIndex!==undefined && js.editRowIndex[js.focusGridId]!==undefined){
							isEditing = false;
							if(js.editRowIndex[js.focusGridId]!==-1){
								rowindex = js.editRowIndex[js.focusGridId];
								isEditing = true;
							}
						}
						$('#'+js.focusGridId).datagrid('selectRow', rowindex);
						$('#'+js.focusGridId).datagrid('scrollTo', rowindex);
						if(isEditing === true){
							$('#'+js.focusGridId).datagrid('getPanel').find(':input[tabindex]').eq(0).focus();
						}else if(isEditing === false){
							setTimeout(function(){
								$('#'+js.focusGridId).datagrid('beginEdit', rowindex);
							},0);
						}
					}else{
						js.focusGridId = $(that).attr('id');
					}
				});
			});
			$('#'+js.focusRootId).find('.easyui-combobox').each(function(){
				var that = this;
				// targetに設定をコピー
				var target = $(that).combobox('textbox');
				if($(that).is('[readonly=readonly]')){
					target.attr('tabindex', -1);
				}else{
					target.attr('tabindex', $(that).attr('tabindex'));
					target.on('focus', function(e){
						//(that).combobox('showPanel');
						func_focusout_editgrid(e);
					});
				}
				// キーイベントの対象となってしまうのでコピー元にtabindex=-1設定
				$(that).attr('tabindex', -1);
			});
			$('#'+js.focusRootId).find('.easyui-textbox').each(function(){
				var that = this;
				// targetに設定をコピー
				var target = $(that).textbox('textbox');
				if($(that).is('[readonly=readonly]')){
					target.attr('tabindex', -1);
				}else{
					target.attr('tabindex', $(that).attr('tabindex'));
					// 編集グリッドがある場合は編集終了確認追加
					if(js.editRowIndex!==undefined){
						target.on('focus', function(e){ func_focusout_editgrid(e); });
					}
				}
				// キーイベントの対象となってしまうのでコピー元にtabindex=-1設定
				$(that).attr('tabindex', -1);
			});
			$('#'+js.focusRootId).find('.easyui-numberbox').each(function(){
				var that = this;
				// targetに設定をコピー
				var target = $(that).numberbox('textbox');
				if($(that).is('[readonly=readonly]')){
					target.attr('tabindex', -1);
				}else{
					target.attr('tabindex', $(that).attr('tabindex'));
					// 編集グリッドがある場合は編集終了確認追加
					if(js.editRowIndex!==undefined){
						target.on('focus', function(e){ func_focusout_editgrid(e); });
					}
				}
				// キーイベントの対象となってしまうのでコピー元にtabindex=-1設定
				$(that).attr('tabindex', -1);
			});
		},
	} });
})(jQuery);