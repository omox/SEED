/**
 * jquery report option
 */
;(function($) {
/*
 * MODE:「状態」の移行後のステータス値
 * 提案件名：1:作成中, 2:確定, 3:仕掛, 4:完了
 * 提案商品：1:作成中, 2:確定, 3:仕掛, 4:完了, 9:却下
 * 仕掛商品：1:作成中, 2:確定, 4:完了, 9:却下
 */
	$.extend({
		reportOption: {
		name:		'Out_Reportx249',			// （必須）レポートオプションの確認
		jsonTemp:	[],							// （必須）検索条件情報_入力チェック前
		jsonString:	[],							// （必須）検索条件情報
		jsonStringCsv:	[],						// （CSV出力用）検索条件情報
		jsonHidden: [],							// （必須）親画面からの引き継ぎ情報
		jsonInit: [],							// （必須）検索条件初期情報

		caption: function(){					// （必須）タイトル
			return $('#reportname').val();
		},
		sortName: '',	// ソート項目名
		sortOrder: '',	// ソート順
		timeData : (new Date()).getTime(),

		dedefaultObjNum:	7,	// 初期化オブジェクト数

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
		pushBtnid: "",						// 実行ボタンID情報(検索系で利用)
		focusRootId:"cc",					// （キー移動時必須）キー移動イベントのルートとなるパネルのID
		focusParentId:"",					// （キー移動時必須）現在フォーカスがあたっている項目の親となるパネルのID
		focusGridId:"",						// （キー移動時必須）現在フォーカスがあたっているDataGridのID
		oldBmn:"",
		oldStcdTeian:"",

		initialize: function (reportno){	// （必須）初期化
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

			// 入力テキストボックス系
			var inputbox = Object.getOwnPropertyNames($.id_inp);
			for ( var sel in inputbox ) {
				if($('#'+$.id_inp[inputbox[sel]]).length > 0){
					$.setInputbox(that, reportno, $.id_inp[inputbox[sel]], isUpdateReport);
				}
			}

			// 取引先
			$.setTextBox(that, 'txt_torihiki', false, '', true);
			// 件名No
			$.setTextBox(that, 'txt_teian_no', false, '', true);
			// 提案件名
			$.setTextBox(that, 'txt_teian', false, '', true);
			// 状態
			this.setStcdTeian(reportno, $.id.SelStcdTeian);
			// 部門
			this.setBumon(reportno, $.id.SelBumon);
			// 商品名
			$.setTextBox(that, 'txt_shohin', false, '', true);
			// 商品登録日FROM
			$.setDateBox(that, $.id.txt_ymdf, false, 'yyyymmdd', true, true);
			// 商品登録日TO
			$.setDateBox(that, $.id.txt_ymdt, false, 'yyyymmdd', true, true);

			// ボタン押下時処理
			$('#'+$.id.btn_next).on("click", function(){ that.pushNext(); });

			// 検索ボタン無効化
			$.setButtonState('#'+$.id.btn_search, false, 'success');

			// タブ移動サンプル
			$.changeReportByTabs(that);

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
			$.reg.search = true;	// 当画面はデフォルト検索
			// 引き継ぎ情報セット
			var sendBtnid = $.getJSONValue(that.jsonHidden, "sendBtnid");
			if(sendBtnid && sendBtnid.length > 0){
				that.sendBtnid = sendBtnid;
				$.reg.search = true;
			}

			$.initReportInfo("x249", "提案商品検索");

		},
		initCondition: function (){	// 条件初期値セット
			var that = this;
			// 初期化項目
		},

		/**
		 * 仕掛ボタンイベント
		 */
		pushNext:function(id){
			var that = this;

			if($($.id.hiddenSelectIdx).val() === ''){
				alert('更新する項目を1件以上選択してください。');
				return false;
			}

			// Grid内全情報取得
			var rows = $($.id.gridholder).datagrid('getRows');

			// 対象情報抜粋
			var targetRows = [];
			var index = $($.id.hiddenSelectIdx).val().split(",");
			for (var i=0; i<rows.length; i++){
				if($.inArray(i+'', index) !== -1){
					targetRows.push(rows[i]);
				}
			}

			// 確認ポップアップ
			if(confirm('状態を更新しますか？') == false) return false;

			// セッションタイムアウト、利用時間外の確認
			if($.checkIsTimeout()) return false;

			// 更新処理
			$.ajax({
				url: $.reg.jqgrid,
				type: 'POST',
				async: false,
				data: {
					report:			that.name,			// レポート名
					action: 		$.id.action_update,	// 実行処理情報
					MODE:			"1",				// モード (仕掛商品に登録)
					DATA:			JSON.stringify(targetRows)
				},
				success: function(data){
					// JSONに変換
					var json = JSON.parse(data);

					// ｾｯｼｮﾝ内Option取得
					if(json.opts!==null){

						if(json.opts.E_MSG !== undefined){
							alert(json.opts.E_MSG);
						}else if(json.opts.S_MSG !== undefined){
							//alert(json.opts.S_MSG.MSG);
							alert(json.opts.S_MSG);
							// datagrid更新
							that.success(that.name, false);
						}
					}

				}
			});
		},
		isNumber: function (numVal){
			// チェック条件パターン
			var pattern = /^\d*$/;
			// 数値チェック
			return pattern.test(numVal);
		},

		validation: function (btnId){	// （必須）批准
			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform).form('validate');
			if(!rt){
				$.addErrState(that, $('.validatebox-invalid').eq(0), false);
				return rt;
			}
			var szTorihiki			= $.getJSONObject(this.jsonTemp, $.id.txt_torihiki).value;		// 取引先
			var szTeianNo		= $.getJSONObject(this.jsonTemp, $.id.txt_teian_no).value;		// 件名No
			var flg = "";

			if(szTorihiki.length !== 0){
				flg = that.isNumber(szTorihiki);

				if(flg===false){
					$.addErrState(that, $('#'+$.id.txt_torihiki), true);
					$.addErrState(that, $('#'+$.id.txt_torihiki), true);
					alert("取引先は数字のみで入力してください。");
					rt = false;
				}
			}

			if(szTeianNo.length !== 0){
				flg = that.isNumber(szTeianNo);

				if(flg===false){
					$.addErrState(that, $('#'+$.id.txt_teian_no), true);
					$.addErrState(that, $('#'+$.id.txt_teian_no), true);
					alert("件名Noは数字のみで入力してください。");
					rt = false;
				}
			}

			// combogrid 入力値チェック
			//if (rt == true) rt = $.checkCombogrid(that.jsonTemp, $.id.SelStcdTeian, '状態');
			//if (rt == true) rt = $.checkCombogrid(that.jsonTemp, $.id.SelBumon, '部門');

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
			var szTorihiki		= $.getJSONObject(this.jsonString, $.id.txt_torihiki).value;		// 取引先
			var szTeianNo		= $.getJSONObject(this.jsonString, $.id.txt_teian_no).value;		// 件名No
			var szTeian			= $.getJSONObject(this.jsonString, $.id.txt_teian).value;		// 提案件名
			var szSelStcdTeian		= $.getJSONObject(this.jsonString, $.id.SelStcdTeian).value;	// 状態
			var szSelBumon			= $.getJSONObject(this.jsonString, $.id.SelBumon).value;		// 部門
			var szShohin			= $.getJSONObject(this.jsonString, $.id.txt_shohin).value;		// 商品名
			var szFromdate				= $.getJSONObject(this.jsonString, $.id.txt_ymdf).value;				// 商品登録日FROM
			var szTodate					= $.getJSONObject(this.jsonString, $.id.txt_ymdt).value;				// 商品登録日TO
			var szLimit				= 10000;	// 検索上限数

			if(!btnId) btnId = $.id.btn_search;

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			var dg = $($.id.gridholder);
			dg.datagrid('loading');

			// grid.options 取得
			var options = dg.datagrid('options');
			that.sortName	= options.sortName;
			that.sortOrder	= options.sortOrder;

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,			// レポート名
					BTN:				btnId,
					TORIHIKI:		szTorihiki,		// 取引先
					TEIANNO:		szTeianNo,		// 件名No
					TEIAN:			szTeian,			// 提案件名
					STATE:			szSelStcdTeian,		// 状態
					BUMON:		szSelBumon,			// 部門
					SHOHIN:		szShohin,		// 商品名
					FROM_DATE:			szFromdate,			// 商品登録日FROM
					TO_DATE:			szTodate,			// 商品登録日TO
					LIMIT:			szLimit,			// 検索上限数
					t:				(new Date()).getTime(),
					sortable:		sortable,
					sortName:		that.sortName,
					sortOrder:		that.sortOrder,
					rows:			0		// 表示可能レコード数
				},
				function(json){
					// 検索処理エラー判定
					if($.searchError(json, undefined, that)) return false;

					// ログ出力
					$.log(that.timeData, 'query:');
					// Load処理回避
					$.tryChangeURL(null);

					var limit = 1000;
					var size = JSON.parse(json)["total"];
					if(size > limit){
						$.showMessage('E00010');
					}

					if (sortable===0){
						var options = dg.datagrid('options');

						// 初期検索時に並び替え情報のリセット
						options.sortName = null;
						options.sortOrder = null;
					}

					that.queried = true;
					that.pushBtnid = btnId;
					// Load処理回避
					$.tryChangeURL($.reg.jqeasy);

					// グリッド再描画（easyui 1.4.2 対応）
					dg.datagrid('load', {} );
					$.removeMask();
					// 検索ボタン無効化
					$.setButtonState('#'+$.id.btn_search, false, 'success');
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
			// 取引先
			this.jsonTemp.push({
				id:		$.id.txt_torihiki,
				value:	$('#'+$.id.txt_torihiki).numberbox('getValue'),
				text:		$('#'+$.id.txt_torihiki).numberbox('getText')
			});
			// 件名No
			this.jsonTemp.push({
				id:		$.id.txt_teian_no,
				value:	$('#'+$.id.txt_teian_no).textbox('getText'),
				text:	''
			});
			// 提案件名
			this.jsonTemp.push({
				id:		$.id.txt_teian,
				value:	$('#'+$.id.txt_teian).textbox('getText'),
				text:	''
			});
			// 状態
			this.jsonTemp.push({
				id:		$.id.SelStcdTeian,
				value:	$('#'+$.id.SelStcdTeian).combogrid('getValue'),
				text:	$('#'+$.id.SelStcdTeian).combogrid('getText')
			});
			// 部門
			this.jsonTemp.push({
				id:		$.id.SelBumon,
				value:	$('#'+$.id.SelBumon).combogrid('getValue'),
				text:	$('#'+$.id.SelBumon).combogrid('getText')
			});
			// 商品名
			this.jsonTemp.push({
				id:		$.id.txt_shohin,
				value:	$('#'+$.id.txt_shohin).textbox('getText'),
				text:	''
			});
			// 商品登録日FROM
			this.jsonTemp.push({
				id:		$.id.txt_ymdf,
				value:	$('#'+$.id.txt_ymdf).datebox('getValue'),
				text:	$('#'+$.id.txt_ymdf).datebox('getText')
			});
			// 商品登録日TO
			this.jsonTemp.push({
				id:		$.id.txt_ymdt,
				value:	$('#'+$.id.txt_ymdt).datebox('getValue'),
				text:	$('#'+$.id.txt_ymdt).datebox('getText')
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

		setTorihiki: function(reportno, id){		// 取引先
			var that = this;
			var idx = -1;
			$('#'+id).combogrid({
				panelWidth:250,
				url:$.reg.easy,
				required: false,
				editable: true,
				mode: 'remote',
				hasDownArrow: false,
				showHeader: false,
				idField:'VALUE',
				textField:'TEXT',
				columns:[[
					{field:'TEXT',	title:'',	width:250}
				]],
				fitColumns: true,
				onBeforeLoad:function(param){
					idx = -1;

					// セッションタイムアウト確認
					if ($.checkIsTimeout(that)) return false;

					// 情報設定
					if ($.inArray(id, that.initedObject) < 0){
						param.q = $.getJSONValue(that.jsonHidden, id);
					}

					var json = [{
						DUMMY: 'DUMMY'
					}];

					param.page		=	reportno;
					param.obj		=	id;
					param.sel		=	(new Date()).getTime();
					param.target	=	id;
					param.action	=	$.id.action_init;
					param.json		=	JSON.stringify(json);
				},
				onLoadSuccess:function(data){
					// 初期化
					var num = -1;
					if ($.inArray(id, that.initedObject) < 0){
						that.initedObject.push(id);
						var val = $.getJSONValue(that.jsonHidden, id);
						for (var i=0; i<data.rows.length; i++){
							if (data.rows[i].VALUE == val){
								num = i;
								break;
							}
						}
					}
					if (data.rows.length > 0 && num >= 0){
						$(this).combogrid('grid').datagrid('selectRow', num);
					}
					idx = 1;
					// ログ出力
					$.log(that.timeData, id+' init:');
					// 検索ボタン有効化
					$.setButtonState('#'+$.id.btn_search, true, id);
					// 初期表示検索処理
					$.initialSearch(that);
				},
				onChange:function(newValue, oldValue){
					if(idx > 0){
						// 検索ボタン有効化
						$.setButtonState('#'+$.id.btn_search, true, id);
					}
				}
			});
		},
		setStcdTeian: function(reportno, id){		// 状態_提案商品
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

			var data = [
				{"VALUE":"-1","TEXT":"　"},
				{"VALUE":"01","TEXT":"作成中"},
				{"VALUE":"02","TEXT":"確定"},
				{"VALUE":"03","TEXT":"仕掛"},
				{"VALUE":"04","TEXT":"完了"},
				{"VALUE":"09","TEXT":"却下"}
			];

			$('#'+id).combobox({
				panelWidth:250,
				panelHeight:'auto',
				url:$.reg.easy,
				required: true,
				editable: true,
				autoRowHeight:false,
				valueField:'VALUE',
				textField:'TEXT',
				data: data,
				multiple :false,
				prompt: '',
				icons: [{
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
					// 情報設定
					var json = [{
						DUMMY: 'DUMMY'
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
					val = new Array();
					for (var i=0; i<data.length; i++){
						if ($.inArray(data.value)!=-1){
							val.push(data[i].VALUE);
						}
					}
					if (val.length===data.length || val.length===0){
						val = null;
					}

					if (val){
						$('#'+id).combobox('setValues',val);
					} else {
						$('#'+id).combobox('setValues','-1');
					}
					idx = 1;

					// ログ出力
					$.log(that.timeData, id+' init:');
					// 検索ボタン有効化
					$.setButtonState('#'+$.id.btn_search, true, id);
					that.onChangeFlag = false;
				},
				onShowPanel: function(){
					$.setScrollComboBox(id);
					that.onChangeFlag=false;
					onChange=false;
					onPanel=true;
				},
				onHidePanel: function(){
					if (onChange){
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
					$.removeErrState();
					// 検索ボタン有効化
					$.setButtonState('#'+$.id.btn_search, true, id);
					onChange=true;
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
				required: true,
				editable: true,
				autoRowHeight:false,
				valueField:'VALUE',
				textField:'TEXT',
				multiple :false,
				prompt: '',
				icons: [{
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
					// 情報設定
					var json = [{
						DUMMY: 'DUMMY'
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
					if (val){
						$('#'+id).combobox('setValues',val);
					} else {
						$('#'+id).combobox('setValues','-1');
					}
					idx = 1;

					// ログ出力
					$.log(that.timeData, id+' init:');
					// 検索ボタン有効化
					$.setButtonState('#'+$.id.btn_search, true, id);
					// 初期表示検索処理
					$.initialSearch(that);
					that.onChangeFlag = false;
				},
				onShowPanel: function(){
					$.setScrollComboBox(id);
					that.onChangeFlag=false;
					onChange=false;
					onPanel=true;
				},
				onHidePanel: function(){
					if (onChange){
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

					if(idx > 0){
						$.removeErrState();
						// 検索ボタン有効化
						$.setButtonState('#'+$.id.btn_search, true, id);
					}
					onChange=true;
					that.getComboErr(obj,false,newValue,oldValue);
				}
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
				sortName: "SORT",
				autoRowHeight:false,
				singleSelect:true,
				//toolbar:$.id.buttons,
				pagination:true,
				pagePosition:'bottom',
				pageSize:pageSize,
				pageList:pageList,
				frozenColumns:[[
					{field:'ck',	title:'',			width:40,	align:'center',	halign:'center',	resizable:false,
						formatter:function(value, rowData, rowIndex) {
							if($.inArray(rowData['F1'], ['作成中','確定']) !== -1){
								return '<input id="ck_'+rowIndex+'" type="checkbox" style="opacity:1;">';
							} else {
								return '';
							}
						}
					},
					{field:'F1',	title:'状態',	width:60,	align:'center',	halign:'center'},
				]],
				columns:[[
					{field:'F2',	title:'件名No',			width: 80,halign:'center',align:'left'},
					{field:'F3',	title:'提案件名',					width:150,halign:'center',align:'left'},
					{field:'F4',	title:'商品コード',			width: 80,halign:'center',align:'left',
						formatter:function(value, rowData, rowIndex) { return '<a id="F4_'+rowIndex+'" href="#">'+value+'</a>'; }},
					{field:'F5',	title:'ソースコード1',		width:120,halign:'center',align:'left'},
					{field:'F6',	title:'商品名',					width:300,halign:'center',align:'left'},
					{field:'F7',	title:'扱<br>区分',			width: 40,halign:'center',align:'left'},
					{field:'F8',	title:'原価',						width: 70,halign:'center',align:'right',
						formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0.00');}},
					{field:'F9',	title:'本体売価',				width: 70,halign:'center',align:'right',
						formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0');}},
					{field:'F10',	title:'総額売価',				width: 70,halign:'center',align:'right',
						formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0');}},
					{field:'F11',	title:'店入数',				width: 70,halign:'center',align:'right',
						formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0');}},
					{field:'F12',	title:'親コード',			width: 80,halign:'center',align:'left'},
					{field:'F13',	title:'ワッペン区分',		width: 60,halign:'center',align:'left'},
					{field:'F14',	title:'一括区分',			width: 40,halign:'center',align:'left'},
					{field:'F15',	title:'標準仕入先',		width: 70,halign:'center',align:'left'},
					{field:'F16',	title:'分類コード',		width:100,halign:'center',align:'left'},
					{field:'F17',	title:'更新日',				width: 70,halign:'center',align:'center'},
					{field:'F18',	title:'衣料使い回し',		width: 50,halign:'center',align:'center'},
				]],
				fitColumns:false,	// 指定カラム幅を適用する場合、false 指定。
				rowStyler:function(index, row){
				},
				onSortColumn:function(sort, order){
					if (that.jsonString.length===0) return false;
					// カラム並び替え
					that.success(that.name, true);
				},
				onLoadSuccess:function(data){
					// (暫定)オブジェクトのコントロール
					var panel = $(id).datagrid('getPanel');

					// View内の入出力項目を調整
					var inputs = panel.find('.datagrid-row :input');

					// 商品コード
					var link = panel.find('.datagrid-row a').filter('[id^=F4_]');
					link.click(function(){
						var index = $(this).attr('id').split('_')[1];
						var targetRow = $($.id.gridholder).datagrid('getRows')[index];
						that.changeReport(reportno, "", targetRow);
					});

					// 選択チェックボックス
					var input = inputs.filter('[id^=ck_]');
					input.change(function(){
						var index = $(this).attr('id').split('_')[1];
						if ($(this).is(':checked')) {
							$.onCheck_grid(index);
						} else {
							$.onUnCheck_grid(index);
						}
					});

					inputs.filter('.validatebox-text').on('focus', function() { ctrlFocus(this); });

					// 選択制御
					$.onInitAllSelect();

					// 状態保存
					$.saveState(reportNumber, that.getJSONString(), this);
				},

				loadFilter:function(data){
					if (typeof data.length == 'number' && typeof data.splice == 'function'){	// is array
						data = {
							total: data.length,
							rows: data
						};
					}
					var dg = $(this);
					var opts = dg.datagrid('options');
					$.createPagenation(that, dg, opts, data);
					if (!data.originalRows){
						data.originalRows = (data.rows);
					}
					var start = (opts.pageNumber-1)*parseInt(opts.pageSize);
					var end = start + parseInt(opts.pageSize);
					data.rows = (data.originalRows.slice(start, end));
					return data;
				}
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
			if(that.sendBtnid.length != 0){
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
		changeReport:function(reportno, btnId, rowData){				// 画面遷移
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
			$.setJSONObject(sendJSON, $.id.send_mode,	1,	1);						// 遷移
			$.setJSONObject(sendJSON, 'searchInput',	that.jsonString,	'');	// 検索条件
			$.setJSONObject(sendJSON, 'searchIndex',	1,	1);						// 検索画面位置インデックス

			// 転送先情報
			var index = 3;
			var childurl = href[index];

			// 実行ボタン別処理
			switch (btnId) {
			case $.id.btn_back:
				// 転送先情報
				childurl = parent.$('#hdn_menu_path').val();

				break;
			default:
				$.setJSONObject(sendJSON, 'hiddenTeianStcd',	rowData['F1'],	rowData['F1']);		// 状態_提案商品
				$.setJSONObject(sendJSON, $.id.hiddenNoTeian,	rowData['F2'],	rowData['F2']);		// 件名No
				$.setJSONObject(sendJSON, 'hiddenTeianKNNM',	rowData['F3'],	rowData['F3']);		// 提案件名
				$.setJSONObject(sendJSON, $.id.hiddenShohin,	rowData['F4'],	rowData['F4']);		// 商品コード
				$.setJSONObject(sendJSON, 'hiddenSrccd',			rowData['F5'],	rowData['F5']);		// ソースコード1
				$.setJSONObject(sendJSON, 'hiddenShnkn',			rowData['F6'],	rowData['F6']);		// 商品名
				$.setJSONObject(sendJSON, 'hiddenAtsukkbn',		rowData['F7'],	rowData['F7']);		// 扱区分
				$.setJSONObject(sendJSON, 'hiddenGenka',			rowData['F8'],	rowData['F8']);		// 原価
				$.setJSONObject(sendJSON, 'hiddenBaika',			rowData['F9'],	rowData['F9']);		// 本体売価
				$.setJSONObject(sendJSON, 'hiddenSoBaika',		rowData['F10'],	rowData['F10']);		// 総額売価
				$.setJSONObject(sendJSON, 'hiddenTenIrisu',		rowData['F11'],	rowData['F11']);		// 店入数
				$.setJSONObject(sendJSON, 'hiddenOyacd',			rowData['F12'],	rowData['F12']);		// 親コード
				$.setJSONObject(sendJSON, 'hiddenWapkbn',		rowData['F13'],	rowData['F13']);		// ワッペン区分
				$.setJSONObject(sendJSON, 'hiddenAllkbn',			rowData['F14'],	rowData['F14']);		// 一括区分
				$.setJSONObject(sendJSON, 'hiddenSirsk',			rowData['F15'],	rowData['F15']);	// 標準仕入先
				$.setJSONObject(sendJSON, 'hiddenBunruicd',		rowData['F16'],	rowData['F16']);	// 分類コード
				$.setJSONObject(sendJSON, 'hiddenUpddt',			rowData['F17'],	rowData['F17']);	// 更新日
				$.setJSONObject(sendJSON, 'hiddenIryou',			rowData['F18'],	rowData['F18']);	// 衣料使い回し
				$.setJSONObject(sendJSON, "itemMode",		rowData['F1'] == '確定' ? 1 : 2,	'');	// 商品情報参照先
				$.setJSONObject(sendJSON, 'callpage', that.name, that.name);
				$.setJSONObject(sendJSON, "Mode", "Teian", "Teian");

				$.setJSONObject(sendJSON, $.id.txt_sel_shncd, rowData['F4'].replace("-",""), rowData['F4'].replace("-",""));
				$.setJSONObject(sendJSON, $.id.txt_sel_shnkn, rowData['F6'], rowData['F6']);
				$.setJSONObject(sendJSON, $.id_inp.txt_shncd, rowData['F4'], rowData['F4']);
				$.setJSONObject(sendJSON, 'sendBtnid', $.id.btn_sel_refer, $.id.btn_sel_refer);

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
			var fColumns = options.frozenColumns[0].slice(1);	// checkbox列以外を取得
			title = $.outputExcelTitle(title, [ fColumns ]);
			title = $.outputExcelTitle(title, options.columns);

			var kbn = fColumns.length;

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
				async: false,
				success: function(){
					// Excel出力
					$.outputExcel(reportno, kbn);
				},
				error: function(){
					// Excel出力エラー
					$.outputExcelError();
				}
			});
		}
	} });
})(jQuery);