/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_Reportx244',			// （必須）レポートオプションの確認
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
		pushBtnid: "",						// 実行ボタンID情報(検索系で利用)
		focusRootId:"cc",					// （キー移動時必須）キー移動イベントのルートとなるパネルのID
		focusParentId:"",					// （キー移動時必須）現在フォーカスがあたっている項目の親となるパネルのID
		focusGridId:"",						// （キー移動時必須）現在フォーカスがあたっているDataGridのID
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
			// 期間
			$.setInputbox(that, reportno, 'txt_fromdate', isUpdateReport);
			$.setInputbox(that, reportno, 'txt_todate', isUpdateReport);
			// 中分類
			this.setChuBun(reportno, $.id.SelChuBun);
			// 大分類
			this.setDaiBun(reportno, $.id.SelDaiBun);
			// 部門
			this.setBumon(reportno, $.id.SelBumon);

			// サブウインドウの初期化
			$.win001.init(that);	// メーカー
			$.win002.init(that);	// 仕入先

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

			$.initReportInfo("x244", "商品マスタ履歴管理　一覧");

		},
		initCondition: function (){	// 条件初期値セット
			var that = this;
			// 初期化項目
		},
		validation: function (btnId){	// （必須）批准
			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform).form('validate');
			if(!rt){
				$.addErrState(that, $('.validatebox-invalid').eq(0), false);
				return rt;
			}

			var szShncd			= $.getJSONObject(this.jsonTemp, $.id_inp.txt_shncd).value;		// 商品コード
			if(rt){
				var check = JSON.parse('{'+$('#'+$.id_inp.txt_shncd).attr("check")+'}');
				if(szShncd.length > 0 && szShncd.length !== check.maxlen*1){
					$.showMessage('EX1006', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_shncd), true)});
					rt = false;
				}
			}
			if(rt){
				if(btnId === $.id.btn_copy && szShncd.length === 0){
					$.showMessage('EX1007', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_shncd), true)});
					rt = false;
				}
			}

			var szSelBumon		= $.getJSONObject(this.jsonTemp, $.id.SelBumon).value;			// 部門
			var szFromDate		= $.getJSONObject(this.jsonTemp, 'txt_fromdate').value;			// 開始日
			var szToDate		= $.getJSONObject(this.jsonTemp, 'txt_todate').value;			// 終了日
			if(rt){
				if(szShncd.length === 0 && szFromDate.length === 0 && szToDate.length === 0){
					$.addErrState(that, $('#'+$.id_inp.txt_shncd), true);
					$.addErrState(that, $('#'+'txt_fromdate'), true);
					$.addErrState(that, $('#'+'txt_todate'), true);
					alert('商品コード、期間のいずれかを入力して下さい。');
					rt = false;
				}
			}
			if(rt){
				if(szFromDate.length != 0 || szToDate.length != 0){

					var reFromDate = szFromDate.replace("/", "");
					var reToDate = szToDate.replace("/", "");
					if(reFromDate.length === 6){
						var addstr = "20";
						if(Number(reFromDate.substr(0,2)) > 50){
							addstr = "19";
						}
					}
					reFromDate = addstr+reFromDate.substr(0,8);
					if(reToDate.length === 6){
						var addstr = "20";
						if(Number(reToDate.substr(0,2)) > 50){
							addstr = "19";
						}
					}
					reToDate = addstr+reToDate.substr(0,8);

					var reFromYear = reFromDate.substr(0,4);
					var reFromMonth = reFromDate.substr(4,2);
					var reFromDay = reFromDate.substr(6,2);
					var dFromDate = new Date(reFromYear, Number(reFromMonth) -1, reFromDay);
					var dToDate = new Date(reToDate.substr(0,4), Number(reToDate.substr(4,2)) -1, reToDate.substr(6,2));

					// 1ヶ月
					var addMonth = Number(reFromMonth) + 1;
					var endDate = new Date(reFromYear, addMonth, 0).getDate();

					if(reFromDay > endDate){
						reFromDay = endDate;
					}else{
						reFromDay = reFromDay -1;
					}

					var limitDate = new Date(reFromYear, addMonth -1, reFromDay);
					var diffDate = limitDate.getTime() - dToDate.getTime();

					if(diffDate < 0){
						$.addErrState(that, $('#'+'txt_fromdate'), true);
						$.addErrState(that, $('#'+'txt_todate'), true);
						alert("期間は最大1ヶ月の範囲で指定して下さい。(例:3月1日～3月31日)");
						rt = false;
					}
				}
			}
			if(rt){
				if(szSelBumon.indexOf("13") !== -1 || szSelBumon.indexOf("27") !== -1 ){
					for(var i=0; i < szSelBumon.length; i++){
						if(["13","27"].indexOf(szSelBumon[i])===-1){
							$.showMessage('EX1008', undefined, function(){$.addErrState(that, $('#'+$.id.SelBumon), true)});
							rt= false;
							break;
						}
					}
				}
			}
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
			var szShncd			= $.getJSONObject(this.jsonString, $.id_inp.txt_shncd).value;		// 商品コード
			var szShohinkn		= $.getInputboxValue($('#txt_shohinkn'));		// 商品名（漢字）
			var szFromdate		= $.getInputboxValue($('#txt_fromdate'));	// 開始日
			var szTodate		= $.getInputboxValue($('#txt_todate'));		// 終了日
			var szSelBumon		= $.getJSONObject(this.jsonString, $.id.SelBumon).value;			// 部門
			var dtSelBumon		= $.getJSONObject(this.jsonString, $.id.SelBumon+'DATA').value;		// 部門のDATA
			var szSelDaiBun		= $.getJSONObject(this.jsonString, $.id.SelDaiBun).value;			// 大分類
			var dtSelDaiBun		= $.getJSONObject(this.jsonString, $.id.SelDaiBun+'DATA').value;	// 大分類のDATA
			var szSelChuBun		= $.getJSONObject(this.jsonString, $.id.SelChuBun).value;			// 中分類
			var dtSelChuBun		= $.getJSONObject(this.jsonString, $.id.SelChuBun+'DATA').value;	// 中分類のDATA
			var szSsircd		= $.getJSONObject(this.jsonString, $.id_inp.txt_ssircd).value;		// 仕入先コード
			var szMakercd		= $.getJSONObject(this.jsonString, $.id_inp.txt_makercd).value;		// メーカーコード

			if(!btnId) btnId = $.id.btn_search;

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
					BTN:			btnId,
					SHNCD:			szShncd,		// 商品コード
					SHOHINKN:		szShohinkn,		// 商品名(漢字)
					FROM_DATE:		szFromdate,
					TO_DATE:		szTodate,
					BUMON:			JSON.stringify(szSelBumon),		// 部門
					BUMON_DATA:		JSON.stringify(dtSelBumon),		// 部門のDATA
					DAI_BUN:		JSON.stringify(szSelDaiBun),	// 大分類
					DAI_BUN_DATA:	JSON.stringify(dtSelDaiBun),	// 大分類のDATA
					CHU_BUN:		JSON.stringify(szSelChuBun),	// 中分類
					CHU_BUN_DATA:	JSON.stringify(dtSelChuBun),	// 中分類のDATA
					SSIRCD:			szSsircd,						// 仕入先コード
					MAKERCD:		szMakercd,						// メーカーコード
					t:				(new Date()).getTime(),
					sortable:		sortable,
					sortName:		that.sortName,
					sortOrder:		that.sortOrder,
					rows:			0	// 表示可能レコード数
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
						var options = $($.id.gridholder).datagrid('options');
						// 初期検索時に並び替え情報のリセット
						options.sortName = null;
						options.sortOrder = null;
					}

					that.queried = true;
					that.pushBtnid = btnId;
					// Load処理回避
					$.tryChangeURL($.reg.jqeasy);

					// 前画面からの遷移時商品コードはクリア
					if ($.reg.search) {
						$.setInputboxValue($('#'+$.id_inp.txt_shncd),"");
					}

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

			// 商品コード
			this.jsonTemp.push({
				id:		$.id_inp.txt_shncd,
				value:	$('#'+$.id_inp.txt_shncd).numberbox('getValue'),
				text:	$('#'+$.id_inp.txt_shncd).numberbox('getText')
			});
			// 商品名（漢字）
			this.jsonTemp.push({
				id:		'txt_shohinkn',
				value:	$('#txt_shohinkn').textbox('getValue'),
				text:	$('#txt_shohinkn').textbox('getText')
			});

			// 部門
			this.jsonTemp.push({
				id:		$.id.SelBumon,
				value:	$('#'+$.id.SelBumon).combobox('getValues'),
				text:	$('#'+$.id.SelBumon).combobox('getText')
			});
			// 全選択or未選択=「すべて」
			$.convertComboBox(this.jsonTemp,$.id.SelBumon);

			// 部門(DATA)
			var dtBumon = $('#'+$.id.SelBumon).combobox('getData');
			var dataBumon = [];
			for(var i=0;i<dtBumon.length;i++){
				dataBumon.push(dtBumon[i].VALUE);
			}
			this.jsonTemp.push({
				id:		$.id.SelBumon+'DATA',
				value:	dataBumon,
				text:	'全部門情報'
			});

			// 大分類
			this.jsonTemp.push({
				id:		$.id.SelDaiBun,
				value:	$('#'+$.id.SelDaiBun).combobox('getValues'),
				text:	$('#'+$.id.SelDaiBun).combobox('getText')
			});
			// 全選択or未選択=「すべて」
			$.convertComboBox(this.jsonTemp,$.id.SelDaiBun);
			// 大分類(DATA)
			var dtDaibun = $('#'+$.id.SelDaiBun).combobox('getData');
			var dataDaibun = [];
			for(var i=0;i<dtDaibun.length;i++){
				dataDaibun.push(dtDaibun[i].VALUE);
			}
			this.jsonTemp.push({
				id:		$.id.SelDaiBun+'DATA',
				value:	dataDaibun,
				text:	'全大分類情報'
			});

			// 中分類
			this.jsonTemp.push({
				id:		$.id.SelChuBun,
				value:	$('#'+$.id.SelChuBun).combobox('getValues'),
				text:	$('#'+$.id.SelChuBun).combobox('getText')
			});
			// 全選択or未選択=「すべて」
			$.convertComboBox(this.jsonTemp,$.id.SelChuBun);
			// 中分類(DATA)
			var dtChuBun = $('#'+$.id.SelChuBun).combobox('getData');
			var dataChuBun = [];
			for(var i=0;i<dtChuBun.length;i++){
				dataChuBun.push(dtChuBun[i].VALUE);
			}
			this.jsonTemp.push({
				id:		$.id.SelChuBun+'DATA',
				value:	dataChuBun,
				text:	'全中分類情報'
			});
			// 仕入先コード
			this.jsonTemp.push({
				id:		$.id_inp.txt_ssircd,
				value:	$('#'+$.id_inp.txt_ssircd).numberbox('getValue'),
				text:	$('#'+$.id_inp.txt_ssircd).numberbox('getText')
			});
			// メーカーコード
			this.jsonTemp.push({
				id:		$.id_inp.txt_makercd,
				value:	$('#'+$.id_inp.txt_makercd).numberbox('getValue'),
				text:	$('#'+$.id_inp.txt_makercd).numberbox('getText')
			});
			// 開始日
			this.jsonTemp.push({
				id:		'txt_fromdate',
				value:	$('#txt_fromdate').textbox('getValue'),
				text:	$('#txt_fromdate').textbox('getText')
			});
			// 終了日
			this.jsonTemp.push({
				id:		'txt_todate',
				value:	$('#txt_todate').textbox('getValue'),
				text:	$('#txt_todate').textbox('getText')
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
						REQUIRED: 'REQUIRED'
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
					that.onChangeFlag = false;
					// 大分類
					that.tryLoadMethods('#'+$.id.SelDaiBun);
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
						}
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

					if(idx > 0){ $.removeErrState(); }
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
				required: true,
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

					if(idx > 0){ $.removeErrState(); }
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
				required: true,
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
							if (val.length===data.length){
								val = null;
							}
							if ($.isArray(val) && val.length===0){	// 旧コード対応
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
					that.onChangeFlag = true;
					$.ajaxSettings.async = true;

					// 初期表示検索処理
					$.initialSearch(that);
				},
				onShowPanel:function(){
					$.setScrollComboBox(id);
				}
				,onChange:function(newValue, oldValue,obj){
					if(idx > 0){ $.removeErrState(); }

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
		setObjectState: function(){	// 軸の選択内容による制御

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
					{field:'F1',	title:'商品コード',			width: 80,halign:'center',align:'left'},
					{field:'F2',	title:'商品名',				width:300,halign:'center',align:'left'},
					{field:'F3',	title:'仕入先',				width:280,halign:'center',align:'left'},
					{field:'F4',	title:'原価',				width: 70,halign:'center',align:'right',
						formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0.00');}},
					{field:'F5',	title:'本体売価',			width: 70,halign:'center',align:'right',
						formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0');}},
					{field:'F6',	title:'店入数',				width: 70,halign:'center',align:'right',
						formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0');}},
					{field:'F7',	title:'更新者',				width:250,halign:'center',align:'left'},
					{field:'F8',	title:'更新日',				width:125,halign:'center',align:'left'},
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
					// 検索後、初回のみ処理
					if (that.queried){
						that.queried = false;	// 検索後、初回のみ処理
						// 状態保存
						$.saveState(reportNumber, that.getJSONString(), id);

						// 警告
						$.showWarningMessage(data);
						that.loadSuccessFunc(id, data);

						if($.getJSONValue(that.jsonHidden, "scrollToIndex_"+id) == ""){
							$.setJSONObject(that.jsonHidden, "scrollToIndex_"+id, 0, 0);
						}
					}

					// 前回選択情報をGridに反映
					var test = $.getJSONValue(that.jsonHidden, "scrollToIndex_"+id)
					test = test * 1
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
			var newrepinfos = $.getBackJSON(that, states, true);
			$.setJSONObject(sendJSON, 'repinfo', newrepinfos, '');


			// 選択行
			var row = $($.id.gridholder).datagrid("getSelected");

			// 実行ボタン別処理
			switch (btnId) {
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
		keyEventInputboxFunc:function(e, code, that, obj){

			var id = $(obj).attr("orizinid");

			// *** Enter or Tab ****
			if(code === 13 || code === 9){
				// 商品コード
				if(id===$.id_inp.txt_shncd){
					var value = $.getInputboxValue($('#'+id));
					if(!$.isEmptyVal(value)){
						if(value.length < 8 ){
							value = ('00000000'+value).substr(-8);
							$.setInputboxValue($('#'+id), value);
						}else if(value.length > 8 ){
							value = value.substr(0, 8);
							$.setInputboxValue($('#'+id), value);
						}
						// 検索ボタン押下
						$('#'+$.id.btn_search).trigger('click');
						e.preventDefault();
						return false;
					}
				}
			}
		}
	} });
})(jQuery);