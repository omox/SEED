/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_ReportST016',			// （必須）レポートオプションの確認
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
		dedefaultObjNum:	2,	// 初期化オブジェクト数
		initObjNum:	-1,
		initedObject: [],
		maxMergeCell: 1,
		onChangeFlag : false,
		onChangeFlag2 : false,
		columnName:'',	// OnClickRowの列名
		initqueried : false,
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
		editRowIndex:{},					// グリッド編集行

		baseData:[],						// 検索結果保持用

		isAllChecked:false,					// 全選択/解除用フラグ
		initialize: function (reportno){	// （必須）初期化
			var that = this;
			// 引き継ぎ情報
			this.jsonHidden = $.getTargetValue();
			// 画面の初回基本設定
			this.setInitObjectState();

			// 初期検索条件設定
			this.jsonInit = $.getInitValue();
			// データ表示エリア初期化
			that.setGrid($.id.gridholder.replace('#', ''), reportno);

			// 初期化するオブジェクト数設定
			this.initObjNum = this.dedefaultObjNum;
			// BYCD
			that.setBycd(that, reportno, $.id.sel_bycd, true);
			// 部門
			that.setBumon(reportno, $.id.SelBumon);

			// Load処理回避
			//$.tryChangeURL(null);

			// 初期化終了
			this.initializes =! this.initializes;

			// チェックボックスの設定
			$.initCheckboxCss($("#"+that.focusRootId));
			// キーイベントの設定
			$.initKeyEvent(that);

			// ログ出力
			$.log(that.timeData, 'initialize:');
		},
		judgeRepType: {
			ST016 		: true,
			ST016_sei	: false,
			ST016_ref	: false
		},
		repgrpInfo: {
			TG017:{idx:1},		// 特売・スポット計画 新規・変更
			TG017_1:{idx:2},	// 特売・スポット計画 参照
			ST022:{idx:3},		// 特売・スポット計画 CSV取込
			ST024:{idx:4},		// 特売・スポット計画 店一括数量CSV取込
			ST016:{idx:5},		// 特売・スポット計画 商品一覧
			ST024:{idx:6},		// 特売・スポット計画 CSV取込
			ST019:{idx:7},		// 特売・スポット計画 コピー元商品選択
			TG016:{idx:8}		// 月間販売計画 商品情報
		},
		setInitObjectState: function(){	// 画面初期化時の項目制御
			var that = this;
			// 引き継ぎ情報セット
			$.reg.search = true;	// 当画面ではヘッダー情報のため、検索は常に行う
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

			// 画面情報
			var repstatesBef = $.getJSONObject(that.jsonHidden, "repinfo");
			var repstates = [];
			if(repstatesBef){ repstates = repstates.concat(repstatesBef.value);}
			for (var i = 0; i < repstates.length; i++) {
				if(repstates[i].id===that.name && repstates[i].value){
					if(repstates[i].value.SENDBTNID){
						that.sendBtnid = repstates[i].value.SENDBTNID;
					}
					if(repstates[i].value.PUSHBTNID){
						that.pushBtnid = repstates[i].value.PUSHBTNID;
					}
					break;
				}
			}

			// 当帳票を「参照」で開いた場合
			if(that.reportYobiInfo()==='1'){
				$.setInputBoxDisable($('#'+$.id.btn_new+1)).hide();
				$.setInputBoxDisable($('#'+$.id.btn_copy)).hide();
				$.setInputBoxDisable($('#'+$.id.btn_new+2)).hide();
				$.setInputBoxDisable($('#'+$.id.btn_select)).hide();
				$.setInputBoxDisable($('#'+$.id.btn_csv+1)).hide();

				that.judgeRepType.ST016_ref = true;
				$.initReportInfo("ST016", "特売・スポット計画　商品一覧（参照）", "参照");
			}else{
				$('#'+$.id.btn_new+1).on("click", $.pushChangeReport);
				$('#'+$.id.btn_copy).on("click", $.pushChangeReport);
				$('#'+$.id.btn_new+2).on("click", $.pushChangeReport);

				$('#'+$.id.btn_select).on("click", function(e){
					var rows = $($.id.gridholder).datagrid('getRows');
					var val = $.id.value_on;
					if(that.isAllChecked){
						val = $.id.value_off;
					}
					for (var i=0; i<rows.length; i++){
						rows[i].USE = val;
						$($.id.gridholder).datagrid('refreshRow', i);
					}
					that.isAllChecked = !that.isAllChecked;
				});

				// CSV出力ボタン設定
				$('#'+$.id.btn_csv + 1).on("click", that.pushCsv);		// 特売原稿
				$('#'+$.id.btn_csv + 2).on("click", that.pushCsv);		// 店別数量

				that.judgeRepType.ST016_sei = true;
				$.initReportInfo("ST016", "特売・スポット計画　商品一覧", "一覧");
			}

			$($.id.buttons).show();
			$('#'+$.id.btn_sel_change).on("click", $.pushChangeReport);

			$($.id.hiddenChangedIdx).on("change", function()
			{
				$(this).val("");
			});

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
		validation: function (btnId){	// （必須）批准
			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform).form('validate');
			// 入力エラーなしの場合に検索条件を格納
			if (rt == true){
				that.jsonString = that.jsonTemp.slice(0);
				if(btnId===$.id.btn_csv + 1 || btnId===$.id.btn_csv + 2){
					if (rt == true) that.jsonStringCsv = that.jsonTemp.slice(0);
				}
			}
			// 入力チェック用の配列をクリア
			that.jsonTemp = [];
			return rt;
		},
		success: function(reportno, sortable, btnId){	// （必須）正処理
			if (sortable) sortable=1; else sortable=0;
			var that = this;
			// 検索実行
			var szMoyskbn	= $.getJSONObject(this.jsonString, $.id_inp.txt_moyskbn).value;		// 催し区分
			var szMoysstdt	= $.getJSONObject(this.jsonString, $.id_inp.txt_moysstdt).value;	// 催しコード（催し開始日）
			var szMoysrban	= $.getJSONObject(this.jsonString, $.id_inp.txt_moysrban).value;	// 催し連番
			var szBumon	= $.getJSONObject(this.jsonString, $.id.SelBumon).value;			// 部門
			var dtBumon	= $.getJSONObject(this.jsonString, $.id.SelBumon+'DATA').value;		// 部門のDATA
			var szBycd		= $.getJSONObject(this.jsonString, $.id.sel_bycd).value;			// BY
			if (!that.initqueried) {
				szBycd = !$.isEmptyVal($.getJSONValue(that.jsonHidden, $.id.sel_bycd)) ? $.getJSONValue(that.jsonHidden, $.id.sel_bycd):szBycd;
			}

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			$($.id.gridholder).datagrid('loading');

			// 最初回は、ヘッダー情報取得のための自動検索のため、一覧検索を行わない
			if(that.initqueried){
				that.pushBtnid = btnId;
			}

			// grid.options 取得
			var options = $($.id.gridholder).datagrid('options');
			that.sortName	= options.sortName;
			that.sortOrder	= options.sortOrder;

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,		// レポート名
					SENDBTNID:		that.sendBtnid,
					PUSHBTNID:		that.pushBtnid,
					MOYSKBN:		szMoyskbn,		// 催し区分
					MOYSSTDT:		szMoysstdt,		// 催しコード（催し開始日）
					MOYSRBAN:		szMoysrban,		// 催し連番
					BUMON:			szBumon,		// 部門
					BUMON_DATA:		JSON.stringify(dtBumon),		// 部門のDATA
					BYCD:			szBycd,			// BY
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

					var size = JSON.parse(json)["total"];
					if(size == 0 && that.initqueried){
						$.showMessage('E11003');
					}

					// 初期表示時のみ
					if (!that.initqueried) {
						// 部門へフォーカス
						$('#'+$.id.SelBumon).combo("textbox").focus();
						setTimeout(function(){
							$('#'+$.id.sel_bycd).combobox('setValue', szBycd);
						},300);
					}

					var opts = JSON.parse(json).opts
					if(opts && opts.rows_){
						// 基本データ表示
						that.setData(opts.rows_, opts);
						that.baseData = opts.rows_;
					}

					that.initqueried = true;
					that.queried = true;
					$($.id.hiddenChangedIdx).val("");						// 変更行Index

					// Load処理回避
					$.tryChangeURL($.reg.jqeasy);

					// グリッド再描画（easyui 1.4.2 対応）
					$($.id.gridholder).datagrid('load', {} );
					$.removeMask();
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

			// *** 引継情報 ***
			// 催し区分
			this.jsonTemp.push({
				id:		$.id_inp.txt_moyskbn,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_moyskbn),
				text:	''
			});
			// 催しコード（催し開始日）
			this.jsonTemp.push({
				id:		$.id_inp.txt_moysstdt,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_moysstdt),
				text:	''
			});
			// 催し連番
			this.jsonTemp.push({
				id:		$.id_inp.txt_moysrban,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_moysrban),
				text:	''
			});

			// *** 検索条件 ***
			// 部門
			this.jsonTemp.push({
				id:		$.id.SelBumon,
				value:	$('#'+$.id.SelBumon).combobox('getValue'),
				text:	$('#'+$.id.SelBumon).combobox('getText')
			});
//			// 全選択or未選択=「すべて」
//			$.convertComboBox(this.jsonTemp,$.id.SelBumon);

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

			// BYコード
			this.jsonTemp.push({
				id:		$.id.sel_bycd,
				value:	$('#'+$.id.sel_bycd).combobox('getValue'),
				text:	$('#'+$.id.sel_bycd).combobox('getText')
			});
		},
		setByComboReload: function (target,check) {

			var tag_options = target.attr('data-options');
			if(tag_options){
				tag_options = '\"'+tag_options.replace(/'/g, '').replace(/,/g, '\",\"').replace(/([a-zA-Z0-9]+):/g, '$1\":\"')+'\"';
			}
			var options = tag_options ? JSON.parse('{'+tag_options+'}'): JSON.parse('{}');
			var editable = options && options.editable;

			var data = target.combobox('getData');
			var val = target.next().children('.textbox-value').val();
			var txt = target.combobox('getText');

			for (var i = 0; i < data.length; i++) {

				var dataVal = data[i].TEXT3;
				if (val*1===dataVal*1) {
					val = dataVal;
				}

				if (check && (!(data[i].VALUE==='-1' && (val==='1'||val==='-')) && (data[i].VALUE.indexOf(val) >= 0 || data[i].TEXT.indexOf(txt) >= 0))) {
					return true;
				} else if (!check && ((data[i].TEXT3 === val) || (data[i].VALUE===target.next().children('.textbox-value').val()))) {
					target.combobox('setValue',data[0].VALUE);
					target.combobox('setValue',data[i].VALUE);
					return true;
				}
			}
			return editable;
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
		setBumon: function(reportno, id){		// 部門
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
					// BYコード
					that.tryLoadMethods('#'+$.id.sel_bycd);
				},
				onShowPanel: function(){
					$.setScrollComboBox(id);
					that.onChangeFlag=false;
					onChange=false;
					onPanel=true;
				},
				onHidePanel: function(){
					if (onChange){
						// BYコード
						that.tryLoadMethods('#'+$.id.sel_bycd);
					}else{
						that.onChangeFlag=true;
					}
					onChange=false;
					onPanel=false;
				},
				onChange:function(newValue, oldValue, obj){
					if(obj===undefined){obj = $(this);}

					if(idx > 0 && that.onChangeFlag){
						// BYコード
						that.tryLoadMethods('#'+$.id.sel_bycd);
						$.removeErrState();
					}
					onChange=true;

					that.getComboErr(obj,false,newValue,oldValue);
				}
			});
		},
		setBycd: function(that, reportno, id, topBlank){
			var idx = -1;

			// フォーカスアウトのタイミングの動作
			$('#'+id).next().on('focusout', function(e){
				var obj = $(this).prev();

				if (!that.setByComboReload(obj,false)) {
					obj.combobox('reload');
				}
			});

			$('#'+id).combobox({
				 url:$.reg.easy,
				//loader: myloader,
				required: false,
				editable: true,
				autoRowHeight:false,
				hasDownArrow: true,
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
					idx = -1;
					// 初期化しない
					if (that.initializes) return false;
					// 情報設定
					var json = [{
						BUMON: $('#'+$.id.SelBumon).combobox('getValue')
					}];
					if(topBlank){json[0]['TOPBLANK'] = topBlank;}

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
					// 初期表示検索処理
					$.initialSearch(that);
				},
				onChange:function(newValue, oldValue, obj){
					if(obj===undefined){obj = $(this);}

					if(idx > 0){
						$.removeErrState();
					}

					that.getComboErr(obj,false,newValue,oldValue);
				}
			});
			idx = 1;
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
		setObjectState: function(){	// 軸の選択内容による制御

		},
		extenxDatagridEditorIds:{
			 F14	: "chk_sel"
		},
		setGrid: function (id, reportNumber){	// グリッドの構築
			var that = this;
			var init = true;
			// ページサイズ定義取得
			var pageList = $.fn.pagination.defaults.pageList;
			var pageSize = $.getJSONValue(this.jsonHidden, $.id.pageSize);
			if (pageSize==="") pageSize=$.fn.pagination.defaults.pageSize;
			pageSize = $.getDefaultPageSize(pageSize, pageList);

			var bcstyler =function(value,row,index){return 'background-color:#f5f5f5;';};
			var dformatter =function(value){ return $.getFormatDt(value, true);};
			var cstyler =function(value,row,index){return 'color: red;font-weight: bold;';};
			var cstyler2=function(value,row,index){return 'color: red;font-weight: bold;background-color:#f5f5f5;';};
			var cformatter =function(value,row,index){return value && value===$.id.value_on?$.id.text_on:$.id.text_off;};


			var columns = [];
			var columnBottom=[];
			columnBottom.push({field:'SEL',	title:'選択',				width: 35,halign:'center',align:'center',	formatter:cformatter,	editor:{type:'checkbox'},	styler:cstyler2});
			columnBottom.push({field:'USE',	title:'CSV対象',			width: 35,halign:'center',align:'center',	formatter:cformatter,	editor:{type:'checkbox'},	styler:cstyler});
			columnBottom.push({field:'F3',	title:'月締後変更',			width: 35,halign:'center',align:'center',	formatter:cformatter,	styler:cstyler2});
			columnBottom.push({field:'F1',	title:'G№',				width: 40,halign:'center',align:'center',	styler:bcstyler});
			columnBottom.push({field:'F2',	title:'子№',				width: 40,halign:'center',align:'center',	styler:bcstyler});
			columnBottom.push({field:'F4',	title:'商品コード',			width: 80,halign:'center',align:'left',		styler:bcstyler});
			columnBottom.push({field:'F5',	title:'商品名称',			width:300,halign:'center',align:'left',		styler:bcstyler});
			columnBottom.push({field:'F6',	title:'便区分',				width: 40,halign:'center',align:'left',		styler:bcstyler});
			columnBottom.push({field:'F7',	title:'原材',				width: 35,halign:'center',align:'center',	formatter:cformatter,	styler:cstyler2});
			columnBottom.push({field:'F8',	title:'販売期間',			width:180,halign:'center',align:'left',		styler:bcstyler});
			columnBottom.push({field:'F9',	title:'納入期間',			width:180,halign:'center',align:'left',		styler:bcstyler});
			columnBottom.push({field:'F10',	title:'B/M',				width: 35,halign:'center',align:'left',		formatter:cformatter,	styler:cstyler2});
			columnBottom.push({field:'F11',	title:'BC',					width: 35,halign:'center',align:'left',		formatter:cformatter,	styler:cstyler2});
			columnBottom.push({field:'F12',	title:'対象店ランクNo.',	width: 50,halign:'center',align:'left',		formatter:function(value,row,index){ return $.getFormatLPad(value, $.len.tencd);},styler:bcstyler});
			columnBottom.push({field:'F13',	title:'除外店ランクNo.',	width: 50,halign:'center',align:'left',		formatter:function(value,row,index){ return $.getFormatLPad(value, $.len.tencd);},styler:bcstyler});
			columns.push(columnBottom);

			var funcClickRow = $.fn.datagrid.defaults.onClickRow;
			var funcBeginEdit= $.fn.datagrid.defaults.onBeginEdit;
			var funcEndEdit= $.fn.datagrid.defaults.onEndEdit;
			var funcAfterEdit= $.fn.datagrid.defaults.onAfterEdit;
			//if(!that.judgeRepType.ST016_ref){
				that.editRowIndex[id] = -1;
				funcLoadSuccess = function(data){
					// チェックボックスの設定
					$.initCheckboxCss($('#'+id).datagrid('getPanel').find('.datagrid-body'));
				};
				funcClickRow = function(index,field){$.clickEditableDatagridCell(that,id, index)};
				funcBeginEdit = function(index,row){
					$.beginEditDatagridRow(that,id, index, row);
				};
				funcEndEdit = function(index,row,changes){
					$.endEditDatagridRow(that, id, index, row);
					row.SEL = $.id.value_off;
				};
				funcAfterEdit = function(index,row,changes){
					var rowobj = $('#'+id).datagrid('getPanel').find("[datagrid-row-index='"+index+"']");
					// チェックボックスの再追加（EndEdit時に削除されるため）
					$.afterEditAddCheckbox(rowobj);
				};
				// 編集可能データグリッドの共通処理設定
				// 編集エディターの機能拡張（非表示で参照用のオブジェクトが準備してある前提、参照オブジェクトはグリッドのフィールド名を元に判断）
				$.extendDatagridEditor(that);
			//}

			$('#'+id).datagrid({
				nowrap: true,
				border: true,
				striped: false,
				collapsible:false,
				remoteSort: true,
				rownumbers:true,
				fit:true,
				view:scrollview,
				pageSize:pageSize,
				pageList:pageList,
				frozenColumns:[[]],
				columns:columns,
				fitColumns:false,	// 指定カラム幅を適用する場合、false 指定。
				rowStyler:function(index, row){
				},
				onLoadSuccess:function(data){
					// 検索後、初回のみ処理
					if (that.queried){
						that.queried = false;	// 検索後、初回のみ処理
						// 状態保存
						$.saveState(reportNumber, that.getJSONString(), '#'+id);
						// 警告
						$.showWarningMessage(data);
					}

					// 前回選択情報をGridに反映
					var getRowIndex = data.total===0 ? '':$.getJSONValue(that.jsonHidden, "scrollToIndex_"+'#'+id);
					if (data.total !== 0 && (data.total-1) < getRowIndex) {
						getRowIndex = getRowIndex-1;
					}
					if(getRowIndex !== ""){
						$('#'+id).datagrid('scrollTo', {
							index: getRowIndex,
							callback: function(index){
								$('#'+id).datagrid('selectRow', index);
								$('#'+id).datagrid('beginEdit', index);
							}
						});

						// 検索可能な一覧画面では、検索時に前回選択情報が適用されてしまう為、
						// 一度適用したら要素を削除する。
						var targetName = "scrollToIndex_"+'#'+id;
						that.jsonHidden.some(function(v, i){
						    if (v.id==targetName) that.jsonHidden.splice(i,1);
						});
					}
				},
				onClickCell:function(rowIndex, field, value){
					// 列名保持
					that.columnName = field;
				},
				onSelect:function(index,row){
					//選択をチェックする。
					row.SEL = $.id.value_on;
				},
				onClickRow: funcClickRow,
				onBeginEdit:funcBeginEdit,
				onEndEdit: funcEndEdit,
				onAfterEdit: funcAfterEdit,
				autoRowHeight:false,
				pagination:false,
				pagePosition:'bottom',
				singleSelect:true
			});
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
			var sendJSON = JSON.parse( JSON.stringify( that.jsonString ) );
			$.setJSONObject(sendJSON, 'sendBtnid', btnId, $('#'+btnId).attr("title"));					// 実行ボタン情報保持
			$.setJSONObject(sendJSON, 'reportYobi1', that.reportYobiInfo(), that.reportYobiInfo());		// 参照情報保持
			$.setJSONObject(sendJSON, 'callpage', that.name, that.name);								// 呼出し元レポート情報

			// 戻る実行時用に現在の画面情報を保持する
			var states = $.getBackBaseJSON(that);
			var newrepinfos = $.getBackJSON(that, states, false);
			$.setJSONObject(sendJSON, 'repinfo', newrepinfos, '');

			// 画面部門
			var szSelBumon	= $.getInputboxValue($('#'+$.id.SelBumon));			// 部門
			if(btnId===$.id.btn_new+1 || btnId===$.id.btn_new+2 ){
				if(szSelBumon === $.id.valueSel_Head){
					$.showMessage('E20037', undefined, function(){$.addErrState(that, $('#'+$.id.SelBumon), true)});
					return false;
				}
			}

			// 画面部門より算出種別区分
			var szAddshukbn = '';
			if(btnId===$.id.btn_new+1){
				if([2,9,15].indexOf(szSelBumon*1)!==-1){
					szAddshukbn = $.id.value_addshukbn_3;
				}else if([4,6].indexOf(szSelBumon*1)!==-1){
					szAddshukbn = $.id.value_addshukbn_4;
				}else if([5].indexOf(szSelBumon*1)!==-1){
					szAddshukbn = $.id.value_addshukbn_5;
				}else{
					szAddshukbn = $.id.value_addshukbn_2;
				}
			}else if(btnId===$.id.btn_new+2){
				szAddshukbn = $.id.value_addshukbn_1;
			}

			// 実行ボタン別処理
			switch (btnId) {
			// 選択変更
			case $.id.btn_sel_change:
				var row = $($.id.gridholder).datagrid("getSelected");
				// ①選択行がないと、エラー。
				if(!row){
					$.showMessage('E00008');
					return false;
				}
				// ②全店特売（アンケート有）_基本.月締フラグ = 1　AND((月締変更許可フラグ = 0:未許可 or NULL)　AND月締変更理由<>0)の場合は遷移できない。
				if(that.baseData[0]["F6"] && that.baseData[0]["F6"] === "1" && row.F3 === $.id.value_on ){
					// E20260	月締めフラグがON、かつ月締変更依頼中でない商品の場合遷移できます。	 	0	 	E
					$.showMessage('E20260');
					return false;
				}

				// 転送先情報
				index = that.repgrpInfo.TG016.idx;
				childurl = href[index];
				sendMode = 1;
				// オブジェクト作成
				$.setJSONObject(sendJSON, $.id_inp.txt_bmncd, ('00'+row.F14).slice(-2), ('00'+row.F14).slice(-2));
				$.setJSONObject(sendJSON, $.id_inp.txt_kanrino,  row.F15, row.F15);
				$.setJSONObject(sendJSON, $.id_inp.txt_kanrieno, row.F16, row.F16);
				$.setJSONObject(sendJSON, $.id_inp.txt_addshukbn,row.F17, row.F17);
				break;
			// 新規
			case $.id.btn_new+1:
			// 新規(全品割引)
			case $.id.btn_new+2:
				// ① 必須入力チェックを行う。
				// ② 当催しはアンケート有の場合、全店特売（アンケート有）_基本.月締フラグ=0のみ、遷移できる。
				if(that.baseData[0]["F6"] && that.baseData[0]["F6"] !== "0"){
					// E20190	当催しは月締フラグがセットされている為、遷移できません	 	0	 	E
					$.showMessage('E20190');
					return false;
				}

				// 転送先情報
				index = that.repgrpInfo.TG016.idx;
				childurl = href[index];
				sendMode = 1;

				// オブジェクト作成
				$.setJSONObject(sendJSON, $.id_inp.txt_bmncd,    szSelBumon,  szSelBumon);
				$.setJSONObject(sendJSON, $.id_inp.txt_addshukbn,szAddshukbn, szAddshukbn);
				break;
			// 新規コピー
			case $.id.btn_copy:
				// ① 必須入力チェックを行う。
				// ② 当催しはアンケート有の場合、全店特売（アンケート有）_基本.月締フラグ=0のみ、遷移できる。
				if(that.baseData[0]["F6"] && that.baseData[0]["F6"] !== '0'){
					// E20190	当催しは月締フラグがセットされている為、遷移できません	 	0	 	E
					$.showMessage('E20190');
					return false;
				}

				// 転送先情報
				index = that.repgrpInfo.ST019.idx;
				childurl = href[index];
				sendMode = 1;
				// オブジェクト作成
				var szMoyscd = $('#'+that.focusRootId).find('[col=F1]').text()
				$.setJSONObject(sendJSON, $.id_inp.txt_moyscd,szMoyscd,  szMoyscd);
				$.setJSONObject(sendJSON, $.id_inp.txt_bmncd, szSelBumon,szSelBumon);
				break;
			case $.id.btn_upd:
			case $.id.btn_cancel:
			case $.id.btn_back:
				// 転送先情報
				index = that.repgrpInfo.TG017.idx;
				if(that.reportYobiInfo()==='1'){
					index = that.repgrpInfo.TG017_1.idx;
				}
				childurl = href[index];
				sendMode = 2;
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
		/**
		 * CSV出力ボタンイベント
		 * @param {Object} e
		 */
		pushCsv : function(e){

			// TODO：仮
			//alert("現在CSV出力機能は停止中です。");
			//return false;

			if ($(this).linkbutton('options').disabled)	return false;

			// レポート番号取得
			var reportno=$($.id.hidden_reportno).val();
			var id = $(this).attr('id');

			// レポート定義位置
			var reportNumber = $.getReportNumber(reportno);
			if (typeof(reportNumber) !== 'number') { alert("レポート定義が見つかりません。"); return false;}

			// 入力編集を終了する。
			var row = $($.id.gridholder).datagrid("getSelected");
			var rowIndex = $($.id.gridholder).datagrid("getRowIndex", row);
			$($.id.gridholder).datagrid('endEdit',rowIndex);

			var shnData = $.report[reportNumber].getCsvGridData('gridholder');
			// チェック数確認
			if(!shnData || shnData.length == 0){
				$.showMessage("E20282");
				return false;
			}
			// チェック数上限確認
			if(id == $.id.btn_csv + 2){
				// 店舗数量CSV出力のとき
				if(shnData.length > 10){
					$.showMessage("E20281");
					return false;
				}
			}

			// マスク削除
			$.removeMask();

			// フォーム情報取得
			$.report[reportNumber].getEasyUI();

			if ($.report[reportNumber].validation(id)) {



				var func_ok = function(){
					// マスク追加
					$.appendMask();

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
								"json"	: JSON.stringify($.report[reportNumber].jsonStringCsv)
							},
							success: function(json){
								// Excel 出力
								$.report[reportNumber].srccsv(reportno, id);
							}
						});
					}
				};

				// CSVデータを出力します。よろしいですか？
				$.showMessage("W20033", undefined, func_ok);
			} else {
				return false;
			}
		},
		srccsv: function(reportno, btnId){	// ここではCsv出力
			var that = this;
			// 処理時間計測用
			that.timeData = (new Date()).getTime();

			var that = this;
			// 検索実行
			var szMoyskbn		= $.getJSONObject(this.jsonStringCsv, $.id_inp.txt_moyskbn).value;		// 催しコード
			var szMoysstdt		= $.getJSONObject(this.jsonStringCsv, $.id_inp.txt_moysstdt).value;		// 催し開始日
			var szMoysrban		= $.getJSONObject(this.jsonStringCsv, $.id_inp.txt_moysrban).value;		// 催し連番
			var szBumon			= $.getJSONObject(this.jsonString, $.id.SelBumon).value;			// 部門
			var shnData = that.getCsvGridData('gridholder');

			if(!btnId) btnId = $.id.btn_search;

			var kbn = 0;
			var data = {
				report:			that.name,						// レポート名
				'kbn':			 kbn,
				'type':			'csv',
				BTN:			btnId,
				MOYSKBN:		szMoyskbn,						// 催し区分
				MOYSSTDT:		szMoysstdt,						// 催し開始日
				MOYSRBAN:		szMoysrban,						// 催し連番
				BMNCD:			szBumon,						// 部門
				SHNDATA:		JSON.stringify(shnData),		// 選択商品情報
				//NEW_DATA:		JSON.stringify(targetData),		// 更新対象情報

				t:				(new Date()).getTime(),
				rows:			0	// 表示可能レコード数
			};

			// 転送
			$.ajax({
				url: $.reg.srcexcel,
				type: 'POST',
				data: data,
				async: true
			})
			.done(function(){
				// Excel出力
				$.outputSearchExcel(reportno, 0);
			})
			.fail(function(){
				// Excel出力エラー
				$.outputSearchExcelError();
			})
			.always(function(){
				// 通信完了
				// ログ出力
				$.log(that.timeData, 'srcexcel:');
			});
		},
		getCsvGridData: function (target){
			var targetdata= [];
			var row = $('#'+target).datagrid('getRows')

			for (var i=0; i<row.length; i++){
				if(row[i]["USE"] && row[i]["USE"] == '1'){
					targetdata.push(row[i]["F14"] + row[i]["F15"]);		// 部門コード + 管理番号
				}
			}
			return targetdata
		}
	} });
})(jQuery);