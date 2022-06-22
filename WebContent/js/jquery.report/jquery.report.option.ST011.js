/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_ReportST011',			// （必須）レポートオプションの確認
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
		dedefaultObjNum:	5,					// 初期化オブジェクト数
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

//			// 初期検索可能
//			that.onChangeReport = false;

			// 名称マスタ参照系
			var meisyoSelect = Object.getOwnPropertyNames($.id_mei);
			for ( var sel in meisyoSelect ) {
				if($('#'+$.id_mei[meisyoSelect[sel]]).length > 0){
					$.setMeisyoCombo(that, reportno, $.id_mei[meisyoSelect[sel]], isUpdateReport);
				}
			}
			// 入力テキストボックス系
			var inputbox = Object.getOwnPropertyNames($.id_inp);
			for ( var sel in inputbox ) {
				if($('#'+$.id_inp[inputbox[sel]]).length > 0){
					$.setInputbox(that, reportno, $.id_inp[inputbox[sel]], isUpdateReport);
				}
			}

			// ラジオボタン系
			$.setRadioInit(that.jsonHidden, $.id.rad_jissekibun, that);			// 実績分類
			$.setRadioInit(that.jsonHidden, $.id.rad_wwmmflg, that);			// 週月フラグ

			// 大分類
			this.setDaiBun(that,reportno, $.id.SelDaiBun, true);

			// 初期化終了
			this.initializes =! this.initializes;

			var newval = $('#'+$.id_inp.txt_bmncd).numberbox('getValue');
			$('#'+$.id_inp.txt_bmncd).numberbox('reset').numberbox('setValue', newval);


			var newval = $.getInputboxValue($('#'+$.id.rad_wwmmflg));
			$.setInputboxValue($('#'+$.id.rad_wwmmflg), newval);

			//$.initialSearch(that);

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
			// 引き継ぎ情報セット
			var sendBtnid = $.getJSONValue(that.jsonHidden, "sendBtnid");
			if(sendBtnid && sendBtnid.length > 0){
				$.reg.search = true;
			}
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

			var callpage = $.getJSONValue(that.jsonHidden, "callpage");
			if(callpage=='Out_ReportST008'){
				$('#'+$.id.btn_back).on("click", $.pushChangeReport);
				$('#'+$.id.btn_cancel).on("click", $.pushChangeReport);
				$('#'+$.id.btn_sel_kakutei).on("click", $.pushChangeReport);
				$("#"+$.id.btn_select).linkbutton('disable');
				$("#"+$.id.btn_select).attr('tabindex', -1).hide();
				$("#"+$.id.btn_sel_tenbetubrt).linkbutton('disable');
				$("#"+$.id.btn_sel_tenbetubrt).attr('tabindex', -1).hide();
				$.setInputBoxDisable($("#"+$.id_inp.txt_bmncd));
				$.initReportInfo("ST011", "ランクマスタ実績　参照");

			}else {
				$('#'+$.id.btn_back).on("click", $.pushChangeReport);
				$('#'+$.id.btn_sel_tenbetubrt).on("click", $.pushChangeReport);
				$("#"+$.id.btn_cancel).linkbutton('disable');
				$("#"+$.id.btn_cancel).attr('tabindex', -1).hide();
				$("#"+$.id.btn_select).linkbutton('disable');
				$("#"+$.id.btn_select).attr('tabindex', -1).hide();
				$("#"+$.id.btn_sel_kakutei).linkbutton('disable');
				$("#"+$.id.btn_sel_kakutei).attr('tabindex', -1).hide();
				$.initReportInfo("RP010", "実績率パターンマスタ　検索");
			}

			$('input[name="'+$.id.rad_jissekibun+'"]').change(function() {
				var rad_jissekibun = $.getInputboxValue($('#'+$.id.rad_jissekibun));
				var target = $("#"+$.id.SelDaiBun);
				if(rad_jissekibun === '1'){
					target.combobox('options').required = false;
					target.combobox('textbox').validatebox('options').required = false;
					target.combobox('validate');
					$.removeErrState();
					target.combobox('setValue','');
				} else if(rad_jissekibun === '2'){
					target.combobox('options').required = false;
					target.combobox('textbox').validatebox('options').required = false;
					target.combobox('validate');
					$.removeErrState();
					target.combobox('setValue','');
				} else {
					target.combobox('options').required = true;
					target.combobox('textbox').validatebox('options').required = true;
					target.combobox('validate');
					$.removeErrState();

					if ($.isEmptyVal(target.combobox('getValue'))) {
						target.combobox('setValue',target.combobox('getData')[0].VALUE);
					}
				}
			});
			$('input[name="'+$.id.rad_wwmmflg+'"]').change(function() {
				var rad_wwmmflg = $.getInputboxValue($('#'+$.id.rad_wwmmflg));

				var txt_yyww = $("#"+$.id_inp.txt_yyww);
				var txt_yymm = $("#"+$.id_inp.txt_yymm);

				if(rad_wwmmflg === '1'){
					txt_yyww.numberbox('options').required = true;
					txt_yyww.numberbox('textbox').validatebox('options').required = true;
					txt_yyww.numberbox('validate');
					txt_yymm.numberbox('options').required = false;
					txt_yymm.numberbox('textbox').validatebox('options').required = false;
					txt_yymm.numberbox('validate');
				} else {
					txt_yymm.numberbox('options').required = true;
					txt_yymm.numberbox('textbox').validatebox('options').required = true;
					txt_yymm.numberbox('validate');
					txt_yyww.numberbox('options').required = false;
					txt_yyww.numberbox('textbox').validatebox('options').required = false;
					txt_yyww.numberbox('validate');

				}
			});
		},
		initCondition: function (){	// 条件初期値セット
			var that = this;
			// 初期化項目
		},
		clear:function(){
			// 隠し情報初期化
			$($.id.hiddenChangedIdx).val("");						// 変更行Index
//			// グリッド初期化
//			this.success(this.name, false);
		},
		validation: function (){	// （必須）批准
			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform).form('validate');
			var txt_bmncd		= $('#'+$.id_inp.txt_bmncd).textbox('getValue');					// 部門
			var rad_jissekibun	= $("input[name="+'rad_jissekibun'+"]:checked").val(); 				// 実績分類
			var SelDaiBun		= $('#'+$.id.SelDaiBun).combobox('getValue')						// 大分類
			var rad_wwmmflg		= $("input[name="+'rad_wwmmflg'+"]:checked").val(); 				// 週月フラグ
			var txt_yyww		= $('#'+$.id_inp.txt_yyww).textbox('getValue');						// 年月(週No.)
			var txt_yymm		= $('#'+$.id_inp.txt_yymm).textbox('getValue');						// 年月(

			if (!txt_bmncd){
				$.showMessage('EX1025');
				rt = false;
			}
			if (rt) {
				if (rad_jissekibun === "1" && SelDaiBun) {
					$("#"+$.id.SelDaiBun).combobox('setValue','');
					$.showMessage('EX1052');
					rt = false;
				} else if (rad_jissekibun === "2" && SelDaiBun) {
					$("#"+$.id.SelDaiBun).combobox('setValue','');
					$.showMessage('EX1053');
					rt = false;
				} else if (rad_jissekibun === "3" && !SelDaiBun) {
					$.showMessage('EX1054', undefined, function(){$.addErrState(that, $('#'+$.id.SelDaiBun), true)});
					rt = false;
				}
			}
			if (rt) {
				if (rad_wwmmflg === "1" && !txt_yyww) {
					$.showMessage('EX1055', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_yyww), true)});
					rt = false;
				}
			}
			if (rt) {
				if (rad_wwmmflg === "2" && !txt_yymm) {
					$.showMessage('EX1056', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_yymm), true)});
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

			var txt_bmncd		= $('#'+$.id_inp.txt_bmncd).textbox('getValue');					// 部門
			var rad_jissekibun	= $("input[name="+'rad_jissekibun'+"]:checked").val(); 				// 実績分類
			var SelDaiBun		= $('#'+$.id.SelDaiBun).combobox('getValue')						// 大分類
			var rad_wwmmflg		= $("input[name="+'rad_wwmmflg'+"]:checked").val(); 				// 週月フラグ
			var txt_yyww		= $('#'+$.id_inp.txt_yyww).textbox('getValue');						// 年月(週No.)
			var txt_yymm		= $('#'+$.id_inp.txt_yymm).textbox('getValue');						// 年月(


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
					BMNCD:			txt_bmncd,		// 部門
					JISSEKIBUN:		rad_jissekibun,	// 実績分類
					DAIBUN:			SelDaiBun,		// 大分類選択
					WWMMFLG:		rad_wwmmflg,	// 週月フラグ
					YYWW:			txt_yyww,		// 年月(週No.)
					YYMM:			txt_yymm,		// 年月
					t:				(new Date()).getTime(),
					sortable:		sortable,
					sortName:		that.sortName,
					sortOrder:		that.sortOrder,
					rows:			0	// 表示可能レコード数
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
			// 部門
			this.jsonTemp.push({
				id:		$.id_inp.txt_bmncd,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_bmncd),
				text:	''
			});
			// 実績分類
			this.jsonTemp.push({
				id:		'rad_jissekibun',
				value:	$("input[name="+'rad_jissekibun'+"]:checked").val(),
				text:	''
			});
			// 大分類選択
			this.jsonTemp.push({
				id:		$.id.SelDaiBun,
				value:	$('#'+$.id.SelDaiBun).combobox('getValue'),
				text:	$('#'+$.id.SelDaiBun).combobox('getText')
			});
			// 週月フラグ
			this.jsonTemp.push({
				id:		'rad_wwmmflg',
				value:	$("input[name="+'rad_wwmmflg'+"]:checked").val(),
				text:	''
			});
			// 年月(週No)
			this.jsonTemp.push({
				id:		$.id_inp.txt_yyww,
				value:	$('#'+$.id_inp.txt_yyww).textbox('getValue'),
				text:	$('#'+$.id_inp.txt_yyww).textbox('getText')
			});
			// 年月
			this.jsonTemp.push({
				id:		$.id_inp.txt_yymm,
				value:	$('#'+$.id_inp.txt_yymm).textbox('getValue'),
				text:	$('#'+$.id_inp.txt_yymm).textbox('getText')
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
				          {title:'実績率パターンNo.', colspan:2,rowspan:1},
				          {title:'　', colspan:2,rowspan:1},
				          ],[
					{field:'F1',	title:'大分類',			width: 100,halign:'center',align:'left'},
					{field:'F2',	title:'中分類',			width: 100,halign:'center',align:'left'},
					{field:'F3',	title:'分類名称',		width: 600,halign:'center',align:'left'},
					{field:'F4',	title:'点数配列',	hidden:true},
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

					var callpage = $.getJSONValue(that.jsonHidden, "callpage");
					if(callpage == "Out_ReportRP006"){
						var getRowIndex = $.getJSONValue(that.jsonHidden, "scrollToIndex_"+id);
						if(getRowIndex !== ""){
							$(id).datagrid('scrollTo', {
								index: getRowIndex,
								callback: function(index){
									$(id).datagrid('selectRow', index);
								}
							});
						}

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
		getComboErr: function (obj,editable,newValue,oldValue) {
			var data = obj.combobox('getData');

			if (!obj.hasClass('datagrid-editable-input')) {
				if (!$.setComboReload(obj,true) && !editable) {
					$.showMessage("E11302",["入力値"],function () {$.addErrState(this,obj,false)});
					obj.combobox('reload');
					obj.combobox('hidePanel');
				} else if ($.isEmptyVal(newValue)) {
					if ($.getInputboxValue($('#'+$.id.rad_jissekibun))==='3') {
						obj.combobox('setValue',obj.combobox('getData')[0].VALUE);
					}
				} else if ($.isEmptyVal(oldValue)) {
					if (obj.next().find('[tabindex=1]').length===1) {
						obj.combo("textbox").focus();
					}
				}
			}
		},
		setDaiBun: function(that,reportno, id, isUpdateReport){		// 大分類
			var that = this;
			var idx = -1;
			var onChange=false;
			var onPanel=false;

			var changeFunc1 = null;
			if(isUpdateReport){
				changeFunc1 = function(){
					if(idx > 0 && that.queried && $($.id.hiddenChangedIdx).is(':enabled')){
						$($.id.hiddenChangedIdx).val("1");
					}
				};
			}
			// 当項目を変更した際に、値をセットする項目がある場合の処理（セット先の項目は、HTMLタグとして、for_inp=当項目のID_列名）
			var changeFunc2 = null;
			if($.isFunction(that.changeInputboxFunc)){
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

				if ($.getInputboxValue($('#'+$.id.rad_jissekibun))!=='3') {
					obj.combobox('setValue','');
					return false;
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
				icons:[{
				}],
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
						REQUIRED: 'REQUIRED',
						BUMON: $('#'+$.id_inp.txt_bmncd).textbox('getValue')
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
							if (val.length===data.length || val.length===0){
								val = null;
							}
						}
					}
					$('#'+id).combobox('setValue',val);

					idx = 1;
					// ログ出力
					$.log(that.timeData, id+' init:');
					that.onChangeFlag = false;
					$.ajaxSettings.async = true;
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
					if(obj===undefined){obj = $(this);}

					if(changeFunc1!==null){ changeFunc1();}
					if(changeFunc2!==null){ changeFunc2(newValue, obj);}
					if(idx > 0){
						$.removeErrState();
					}
					onChange=true;

					that.getComboErr(obj,false,newValue,oldValue);
				}
			});
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
			$.setJSONObject(sendJSON, 'sendBtnid', btnId, $('#'+btnId).attr("title"));					// 実行ボタン情報保持
			$.setJSONObject(sendJSON, 'reportYobi1', that.reportYobiInfo(), that.reportYobiInfo());		// 参照情報保持
			$.setJSONObject(sendJSON, 'callpage', that.name, that.name);								// 呼出し元レポート情報

			var callpage = $.getJSONValue(that.jsonHidden, "callpage");
			// 戻る実行時用に現在の画面情報を保持する
			var states = $.getBackBaseJSON(that);

			if(callpage=='Out_ReportST008'){
				var newrepinfos = $.getBackJSON(that, states, false);

				// 本画面ではsendMode=2での移動を想定していない為、
				// 前回選択行の情報をrepinfoから取り出し設定を送信情報に設定する。
				if(btnId==$.id.btn_cancel || btnId=='btn_back'){
					var targetId = callpage;
					newrepinfos.some(function(v, i){
					    if (v.id==targetId){
					    	var TMPCOND =newrepinfos[i].value.TMPCOND;
					    	var innerTargetId = "scrollToIndex_"+$.id.grd_teninfo+'_list'
					    	TMPCOND.some(function(w, j){
					    		if (w.id==innerTargetId){
					    			$.setJSONObject(sendJSON, innerTargetId, w.value, w.text);
					    		}
					    	});
					    }
					});
				}
			} else {
				var newrepinfos = $.getBackJSON(that, states, true);
				}
			$.setJSONObject(sendJSON, 'repinfo', newrepinfos, '');

			// 選択行
			var row = $($.id.gridholder).datagrid("getSelected");

			// 実行ボタン別処理
			switch (btnId) {
			case $.id.btn_sel_kakutei:	// 選択(確定)
				sendMode = 1;
				if(!row){
					$.showMessage('E00008');
					return false;
				}
				// 転送先情報
				index = 7;		// ST008 店情報(新規・変更)
				childurl = href[index];
				var txt_bmncd 	= $.getJSONValue(that.jsonHidden, $.id_inp.txt_bmncd);						// 部門
				var txt_rankno 	= $.getJSONValue(that.jsonHidden, $.id_inp.txt_rankno);						// ランクNo
				var txt_rankkn 	= $.getJSONValue(that.jsonHidden, $.id_inp.txt_rankkn);						// ランク名称
				var chk_rinji 	= $.getJSONValue(that.jsonHidden, $.id.chk_rinji);							// 臨時
				var txt_moyscd 	= $.getJSONValue(that.jsonHidden, $.id_inp.txt_moyscd);						// 催しコード
				var updData 	= $.getJSONValue(that.jsonHidden, 'updData');								// 検索結果保持(グリッド一覧)
				var initData 	= $.getJSONValue(that.jsonHidden, 'initData');								// 初回検索結果保持(グリッド一覧)
				// オブジェクト作成
				$.setJSONObject(sendJSON, $.id_inp.txt_bmncd, txt_bmncd, txt_bmncd);						// 部門
				$.setJSONObject(sendJSON, $.id_inp.txt_rankno, txt_rankno, txt_rankno);						// ランクNo
				$.setJSONObject(sendJSON, $.id_inp.txt_rankkn, txt_rankkn, txt_rankkn);						// ランク名称
				$.setJSONObject(sendJSON, $.id.chk_rinji, chk_rinji, chk_rinji);							// 臨時
				$.setJSONObject(sendJSON, $.id_inp.txt_moyscd, txt_moyscd, txt_moyscd);						// 催しコード
				$.setJSONObject(sendJSON, 'updData', updData, updData);										// 検索結果保持(グリッド一覧)
				$.setJSONObject(sendJSON, 'initData', updData, initData);									// 初回検索結果保持(グリッド一覧)
				$.setJSONObject(sendJSON, $.id_inp.txt_tenten_arr, row.F4, row.F4);							// 点数配列
				break;
			case $.id.btn_sel_tenbetubrt:// 選択(店別分配率)
				sendMode = 1;
				if(!row){
					$.showMessage('E00008');
					return false;
				}
				// 転送先情報
				index = 11;		// RP006 店別分配率(新規・変更・参照)
				childurl = href[index];

				$.setJSONObject(sendJSON, 'sendBtnid', btnId, $('#'+btnId).attr("title"));					// 実行ボタン情報保持

				var txt_bmncd		= $.getInputboxValue($('#'+$.id_inp.txt_bmncd));						// 部門
				var rad_wwmmflg		= $("input[name="+'rad_wwmmflg'+"]:checked").val();						// 週月フラグ
				var txt_yyww		= $.getInputboxValue($('#'+$.id_inp.txt_yyww));							// 年月(週No.)
				var txt_yymm		= $.getInputboxValue($('#'+$.id_inp.txt_yymm));							// 年月
				var rad_jissekibun	= $("input[name="+'rad_jissekibun'+"]:checked").val(); 					// 実績分類
				if (rad_wwmmflg ==1) {
					txt_yymm = txt_yyww;
				}
				// オブジェクト作成
				$.setJSONObject(sendJSON, $.id_inp.txt_bmncd, txt_bmncd, txt_bmncd);						// 部門
				$.setJSONObject(sendJSON, $.id.rad_wwmmflg, rad_wwmmflg, rad_wwmmflg);						// 週月フラグ
				$.setJSONObject(sendJSON, $.id_inp.txt_yymm, txt_yymm, txt_yymm);							// 年月(週No.)
				$.setJSONObject(sendJSON, $.id_inp.txt_daicd, row.F1, row.F1);								// 大分類
				$.setJSONObject(sendJSON, $.id_inp.txt_chucd, row.F2, row.F2);								// 中分類
				$.setJSONObject(sendJSON, $.id.rad_jissekibun, rad_jissekibun, rad_jissekibun);				// 実績分類
				break;
			case $.id.btn_back:
			case $.id.btn_cancel:
				for (var i = 0; i < newrepinfos.length; i++) {
					var callpage = newrepinfos[i].id;
					if (newrepinfos.length === 1) {
						childurl = parent.$('#hdn_menu_path').val();
					}
					if(callpage==='Out_ReportST008'){
						var txt_bmncd 	= $.getJSONValue(that.jsonHidden, $.id_inp.txt_bmncd);					// 部門
						var txt_rankno 	= $.getJSONValue(that.jsonHidden, $.id_inp.txt_rankno);					// ランクNo
						var txt_rankkn 	= $.getJSONValue(that.jsonHidden, $.id_inp.txt_rankkn);					// ランク名称
						var chk_rinji 	= $.getJSONValue(that.jsonHidden, $.id.chk_rinji);						// 臨時
						var txt_moyscd 	= $.getJSONValue(that.jsonHidden, $.id_inp.txt_moyscd);					// 催しコード
						var updData 	= $.getJSONValue(that.jsonHidden, 'updData');							// 検索結果保持(グリッド一覧)
						var initData 	= $.getJSONValue(that.jsonHidden, 'initData');							// 初回検索結果保持(グリッド一覧)
						// オブジェクト作成
						$.setJSONObject(sendJSON, $.id_inp.txt_bmncd, txt_bmncd, txt_bmncd);					// 部門
						$.setJSONObject(sendJSON, $.id_inp.txt_rankno, txt_rankno, txt_rankno);					// ランクNo
						$.setJSONObject(sendJSON, $.id_inp.txt_rankkn, txt_rankkn, txt_rankkn);					// ランク名称
						$.setJSONObject(sendJSON, $.id.chk_rinji, chk_rinji, chk_rinji);						// 臨時
						$.setJSONObject(sendJSON, $.id_inp.txt_moyscd, txt_moyscd, txt_moyscd);					// 催しコード
						$.setJSONObject(sendJSON, 'updData', updData, updData);									// 検索結果保持(グリッド一覧)
						$.setJSONObject(sendJSON, 'initData', updData, initData);								// 初回検索結果保持(グリッド一覧)
						index = 7;
						sendMode = 1;
						childurl = href[index];
					}
				}
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

			var parentObj = $('#'+that.focusRootId);
			if(id+"_"===obj.attr('id') && that.focusGridId!==""){
				parentObj = $('#'+that.focusGridId).datagrid('getPanel');
			}

			// DB問い合わせ系
			if($('[for_inp^='+id+'_]').length > 0){
				var param = that.getInputboxParams(that, id, newValue);
				$.getsetInputboxRowData(that.name, 'for_inp', id, param, that, parentObj);
			}

			// DB問い合わせ系
			/*if($('[for_inp^='+id+'_]').length > 0){
				var param = that.getInputboxParams(that, id, newValue);
				var rows = $.getSelectListData(that.name, $.id.action_change, id, param);
				var row = rows.length > 0 ? rows[0]:"";
				$.setInputboxRowData('for_inp', id, row, that, parentObj);
			}*/

			// 検索、入力後特殊処理
//			if(that.queried){
//
//			}
			if(id===$.id_inp.txt_bmncd){
				that.tryLoadMethods('#'+$.id.SelDaiBun);
			}
		}
	} });
})(jQuery);