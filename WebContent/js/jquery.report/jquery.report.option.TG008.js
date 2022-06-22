/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_ReportTG008',			// （必須）レポートオプションの確認
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
		maxMergeCell: 1,
		onChangeFlag : false,
		onChangeFlag2 : false,
		columnName:'',	// OnClickRowの列名
		queried : false,
		queried2: false,
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
		editRowIndex:{},					// グリッド編集行保持
		grd_data:[],						// メイン情報
		grd_data_other:[],					// 補足情報：その他、テーブルに登録しない情報などを保持
		initRow:"",
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
			// 部門
			that.setBumon(reportno, $.id.SelBumon);
			$.setCheckboxInit2(that.jsonHidden, $.id.chk_sel, that);
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
			TG008 		: false,	// 008
			TG008_sei	: false,	// 008 -正
			TG008_ref	: false,	// 008 -参照
			TG009 		: false,	// 009
			TG009_sei	: false,	// 009 -正
//			TG009_ref	: false		// 009 -参照	※現状ありえない
		},
		repgrpInfo: {
			TG001:{idx:1},		// 新規・変更
			TG001_1:{idx:2},	// 参照
			TG002:{idx:3},		// 店舗グループ一覧
			TG003:{idx:4},		// 店舗グループ店情報
			TG008:{idx:5},		// 商品一覧
			TG040:{idx:6},		// コピー元店舗グループ一覧
			TG016:{idx:7}		// 商品情報
		},
		setInitObjectState: function(){	// 画面初期化時の項目制御
			var that = this;
			// 引き継ぎ情報セット
			var sendBtnid = $.getJSONValue(that.jsonHidden, "sendBtnid");
			if(sendBtnid && sendBtnid.length > 0){
				that.sendBtnid = sendBtnid;
				$.reg.search = true;
				that.onChangeReport = true;
				if(sendBtnid === $.id.btn_back){
					that.pushBtnid = sendBtnid;
				}
			}
			var reportYobi1 = $.getJSONValue(that.jsonHidden, "reportYobi1");
			if(!reportYobi1){
				reportYobi1 = $('#reportYobi1').val();
			}
			$('#reportYobi1').val(reportYobi1);

			// 戻るボタン遷移の場合は、特殊な引数利用
			if(sendBtnid===$.id.btn_back){
				var callpage = $.getJSONValue(that.jsonHidden, "callpage");
				if(callpage.indexOf("008") > -1){
					that.judgeRepType.TG008 = true;
				}else{
					that.judgeRepType.TG009 = true;
				}
			}else{
				if(sendBtnid===$.id.btn_sel_change+1){
					that.judgeRepType.TG008 = true;
				}else{
					that.judgeRepType.TG009 = true;
				}
			}

			var hidobjids = [];	// 非表示
			// 当帳票を「参照」で開いた場合
			if(that.reportYobiInfo()==='1'){
				$('#'+$.id.btn_sel_refer).on("click", $.pushChangeReport);
				hidobjids = hidobjids.concat([$.id.btn_sel_change,$.id.btn_cancel,$.id.btn_upd]);
				if(that.judgeRepType.TG008){
					that.judgeRepType.TG008_ref = true;
					$.initReportInfo("TG008", "月間販売計画（チラシ計画）　商品一覧（参照）", "参照");
//				}else{
//					that.judgeRepType.TG009_ref = true;
//					$.initReportInfo("TG009", "月間販売計画（チラシ計画）　変更後申請商品　一覧（参照）", "参照");
				}
			}else{
				$('#'+$.id.btn_sel_change).on("click", $.pushChangeReport);
				hidobjids = hidobjids.concat([$.id.btn_sel_refer]);
				// 各種ボタン
				if(that.judgeRepType.TG008){
					hidobjids = hidobjids.concat([$.id.btn_cancel,$.id.btn_upd]);
					that.judgeRepType.TG008_sei = true;
					$.initReportInfo("TG008", "月間販売計画（チラシ計画）　商品一覧", "一覧");
				}else{
					$('#'+$.id.btn_cancel).on("click", $.pushChangeReport);
					that.judgeRepType.TG009_sei = true;
					$.initReportInfo("TG009", "月間販売計画（チラシ計画）　変更申請商品　一覧", "一覧");
				}
			}
			if(that.judgeRepType.TG008){
				$("#"+$.id.btn_cancel).parent('td').hide();
				$("#"+$.id.btn_upd).parent('td').hide();
				$.setInputBoxDisable($($.id.hiddenChangedIdx));
			}
			// 非表示化
			for (var i = 0; i < hidobjids.length; i++) {
				$.setInputBoxDisable($('#'+hidobjids[i])).hide();
			}
			$($.id.buttons).show();
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
		validation: function (btnId){	// （必須）批准
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
			var szMoyskbn	= $.getJSONObject(this.jsonString, $.id_inp.txt_moyskbn).value;		// 催し区分
			var szMoysstdt	= $.getJSONObject(this.jsonString, $.id_inp.txt_moysstdt).value;	// 催しコード（催し開始日）
			var szMoysrban	= $.getJSONObject(this.jsonString, $.id_inp.txt_moysrban).value;	// 催し連番
			var szBumon	= $.getJSONObject(this.jsonString, $.id.SelBumon).value;			// 部門
			var dtBumon	= $.getJSONObject(this.jsonString, $.id.SelBumon+'DATA').value;		// 部門のDATA

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			$($.id.gridholder).datagrid('loading');

			if(btnId) that.pushBtnid = btnId;

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
					PAGEID:			that.judgeRepType.TG008?that.name:that.name.replace("008", "009"),
					MOYSKBN:		szMoyskbn,		// 催し区分
					MOYSSTDT:		szMoysstdt,		// 催しコード（催し開始日）
					MOYSRBAN:		szMoysrban,		// 催し連番
					BUMON:			JSON.stringify(szBumon),		// 部門
					BUMON_DATA:		JSON.stringify(dtBumon),		// 部門のDATA
					t:				(new Date()).getTime(),
					sortable:		sortable,
					sortName:		that.sortName,
					sortOrder:		that.sortOrder,
					rows:			0	// 表示可能レコード数
				},
				function(json){
					// 検索処理エラー判定
					if($.searchError(json)) return false;

					var size = JSON.parse(json)["total"];
					if(that.pushBtnid===$.id.btn_search && size === 0){
						$.showMessage('I30000');
					}

					// ログ出力
					$.log(that.timeData, 'query:');

					// Load処理回避
					$.tryChangeURL(null);

					that.initRow=0;

					var opts = JSON.parse(json).opts
					if(opts && opts.rows_){
						// 基本データ表示
						that.setData(opts.rows_, opts);
					}

					that.queried = true;
					that.queried2= true;
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
		getGridData: function (target){
			var that = this;
			var data = {};

			var szMoyskbn	= $.getJSONObject(this.jsonString, $.id_inp.txt_moyskbn).value;		// 催し区分
			var szMoysstdt	= $.getJSONObject(this.jsonString, $.id_inp.txt_moysstdt).value;	// 催しコード（催し開始日）
			var szMoysrban	= $.getJSONObject(this.jsonString, $.id_inp.txt_moysrban).value;	// 催し連番

			// 基本情報
			if(target===undefined || target==="grd_data"){
				var targetRows= [];
				if(that.judgeRepType.TG009){
					var rows= $($.id.gridholder).datagrid('getRows');
					for (var i=0; i<rows.length; i++){
						if(rows[i]["F22"] === $.id.value_on){
							var rowData = {
									F1  : szMoyskbn,					// F1  : 催し区分	MOYSKBN
									F2  : szMoysstdt,					// F2  : 催し開始日	MOYSSTDT
									F3  : szMoysrban,					// F3  : 催し連番	MOYSRBAN
									F4  : rows[i]["F16"],				// F4  : 部門コード	BMNCD
									F5  : rows[i]["F17"],				// F5  : 管理No		KANRINO
									F6  : rows[i]["F18"],				// F6  : 管理No枝番	KANRIENO
									F91 : rows[i]["F23"],				// F91 : 月締変更理由
									F92 : rows[i]["F14"],				// F92 : 月締変更許可フラグ
									F104: rows[i]["F20"],				// F104: 更新日時	UPDDT

									RNO : i								// 行番号(チェック用に保持)
							};
							targetRows.push(rowData);
						}
					}
				}
				data["grd_data"] = targetRows;
			}
			return data;
		},
		setGridData: function (data, target){
			var that = this;

			// 基本データ
			if(target===undefined || target==="grd_data"){
				that.grd_data =  data["grd_data"];
			}
			return true;
		},
		updValidation: function (id){	// （必須）批准
			var that = this;
			var errMsg= "";
			var rt = true;

			// 入力編集を終了する。
			var row = $($.id.gridholder).datagrid("getSelected");
			var rowIndex = $($.id.gridholder).datagrid("getRowIndex", row);
			$($.id.gridholder).datagrid('endEdit',rowIndex);

			var gridData = that.getGridData();

			// 現在の画面情報を変数に格納
			var rows = gridData["grd_data"];	// 検証用情報取得
			if(rows.length===0){
				$.showMessage('E20582');
				return false;
			}

			// 入力情報を変数に格納
			if (rt == true) that.setGridData(gridData);	// 更新用情報取得

			return rt;
		},
		updSuccess: function(id){	// validation OK時 の update処理
			var that = this;

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$($.id.gridholder).datagrid('loading');

			// 変更行情報取得
			var changedIndex = $($.id.hiddenChangedIdx).val().split(",");

			// 基本情報
			var targetData = that.grd_data;

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,			// レポート名
					action:			$.id.action_update,	// 実行処理情報
					obj:			id,					// 実行オブジェクト
					SENDBTNID:		that.sendBtnid,
					DATA:			JSON.stringify(targetData),				// 更新対象情報
					t:				(new Date()).getTime()
				},
				function(data){
					// 検索処理エラー判定
					if($.updError(id, data)) return false;

					// Grid編集行情報を初期化
					that.editRowIndex['gridholder'] = -1;

					var afterFunc = function(){
						// 初期化
						that.success(that.name, false, id);
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
					that.onChangeFlag = true;
					// 初期表示検索処理
					$.initialDisplay(that);
				},
				onShowPanel: function(){
					$.setScrollComboBox(id);
					that.onChangeFlag=false;
					onChange=false;
					onPanel=true;
				},
				onHidePanel: function(){
					if(onChange){
					}else{
						that.onChangeFlag=true;
					}
					onChange=false;
					onPanel=false;
				},
				onChange:function(newValue, oldValue, obj){
					if(obj===undefined){obj = $(this);}
					that.getComboErr(obj,false,newValue,oldValue);
				}
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
		setObjectState: function(){	// 軸の選択内容による制御

		},
		extenxDatagridEditorIds:{
			 F14	: "chk_sel"
		},
		setGrid: function (id, reportNumber){	// グリッドの構築
			var that = this;
			var init = true;

			var dformatter =function(value){ return $.getFormatDt(value, true);};
			var cstyler=function(value,row,index){return 'color: red;font-weight: bold;';};
			var cformatter =function(value,row,index){return value && value===$.id.value_on?$.id.text_on:$.id.text_off;};


			var fColumns= [], fColumnBottom=[];
			var columns = [], columnBottom=[];

			if(that.judgeRepType.TG009){
				fColumnBottom.push({field:'F14',	title:'変<br>許可',		width: 35,halign:'center',align:'center',	formatter:cformatter,	editor:{type:'checkbox'},	styler:cstyler});
				fColumnBottom.push({field:'F15',	title:'内容',			width: 50,halign:'center',align:'center'});
			}
			fColumnBottom.push({field:'F1',	title:'G№',				width: 40,halign:'center',align:'left'});
			fColumnBottom.push({field:'F2',	title:'子№',				width: 40,halign:'center',align:'left'});
			fColumnBottom.push({field:'F3',	title:'目玉',				width: 50,halign:'center',align:'center'});
			fColumnBottom.push({field:'F4',	title:'日替',				width: 30,halign:'center',align:'center',	formatter:cformatter,	styler:cstyler});
			fColumnBottom.push({field:'F5',	title:'商品コード',			width: 80,halign:'center',align:'left'});
			columnBottom.push({field:'F6',	title:'全割',				width: 30,halign:'center',align:'center',	formatter:cformatter,	styler:cstyler});
			columnBottom.push({field:'F7',	title:'メーカー名(or産地)',	width:300,halign:'center',align:'left'});
			columnBottom.push({field:'F8',	title:'チラシ・POP名称',	width:300,halign:'center',align:'left'});
			columnBottom.push({field:'F9',	title:'規格',				width:225,halign:'center',align:'left'});
			columnBottom.push({field:'F10',	title:'販売期間',			width:150,halign:'center',align:'left'});
			columnBottom.push({field:'F11',	title:'チラシ未掲載',		width: 50,halign:'center',align:'center',	formatter:cformatter,	styler:cstyler});
			columnBottom.push({field:'F12',	title:'先着人数、限定表現、一人、単位',	width:200,halign:'center',align:'left'});
			columnBottom.push({field:'F13',	title:'POPコード',			width:120,halign:'center',align:'left'});

			fColumns.push(fColumnBottom);
			columns.push(columnBottom);

			var funcClickRow = $.fn.datagrid.defaults.onClickRow;
			var funcBeginEdit= $.fn.datagrid.defaults.onBeginEdit;
			var funcEndEdit= $.fn.datagrid.defaults.onEndEdit;
			var funcAfterEdit= $.fn.datagrid.defaults.onAfterEdit;
			if(that.judgeRepType.TG009){
				that.editRowIndex[id] = -1;
				funcLoadSuccess = function(data){
					// チェックボックスの設定
					$.initCheckboxCss($('#'+id).datagrid('getPanel').find('.datagrid-body'));
				};
				funcClickRow = function(index,field){$.clickEditableDatagridCell(that,id, index)};
				funcBeginEdit = function(index,row){
					$.beginEditDatagridRow(that,id, index, row);
				};
				funcEndEdit = function(index,row,changes){$.endEditDatagridRow(that, id, index, row)};
				funcAfterEdit = function(index,row,changes){
					var rowobj = $('#'+id).datagrid('getPanel').find("[datagrid-row-index='"+index+"']");
					// チェックボックスの再追加（EndEdit時に削除されるため）
					$.afterEditAddCheckbox(rowobj);
				};
				// 編集可能データグリッドの共通処理設定
				// 編集エディターの機能拡張（非表示で参照用のオブジェクトが準備してある前提、参照オブジェクトはグリッドのフィールド名を元に判断）
				$.extendDatagridEditor(that);
			}
			// ページサイズ定義取得
			var pageList = $.fn.pagination.defaults.pageList;
			var pageSize = $.getJSONValue(this.jsonHidden, $.id.pageSize);
			if (pageSize==="") pageSize=$.fn.pagination.defaults.pageSize;
			pageSize = $.getDefaultPageSize(pageSize, pageList);
			$('#'+id).datagrid({
				nowrap: true,
				border: true,
				striped: true,
				collapsible:false,
				remoteSort: true,
				rownumbers:false,
				fit:true,
				view:scrollview,
				pageSize:pageSize,
				pageList:pageList,
				frozenColumns:fColumns,
				columns:columns,
				fitColumns:false,	// 指定カラム幅を適用する場合、false 指定。
				onBeforeLoad:function(param){
					param.report = that.name;
				},
				onLoadSuccess:function(data){
					// 検索後、初回のみ処理
					if (that.queried2){
						that.queried2= false;	// 検索後、初回のみ処理

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

					// 初期表示時処理
					if (getRowIndex==="" && data.total !== 0) {
						getRowIndex = that.initRow;
					}

					if(getRowIndex !== ""){
						$('#'+id).datagrid('scrollTo', {
							index: getRowIndex,
							callback: function(index){
								$('#'+id).datagrid('selectRow', index);
							}
						});

						// 検索可能な一覧画面では、検索時に前回選択情報が適用されてしまう為、
						// 一度適用したら要素を削除する。
						var targetName = "scrollToIndex_"+'#'+id;
						that.jsonHidden.some(function(v, i){
						    if (v.id==targetName) that.jsonHidden.splice(i,1);
						});

						that.initRow="";
					}
				},
				onClickCell:function(rowIndex, field, value){
					// 列名保持
					that.columnName = field;
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
			var pageid = that.name;
			if(that.judgeRepType.TG009){
				pageid = that.name.replace("008", "009");
			}
			$.setJSONObject(sendJSON, 'callpage', pageid, pageid);										// 呼出し元レポート情報

			// 戻る実行時用に現在の画面情報を保持する
			var states = $.getBackBaseJSON(that);
			var newrepinfos = $.getBackJSON(that, states, false);
			$.setJSONObject(sendJSON, 'repinfo', newrepinfos, '');

			// 実行ボタン別処理
			switch (btnId) {
			case $.id.btn_sel_change:
			case $.id.btn_sel_refer:
				// 選択行
				var row = $($.id.gridholder).datagrid("getSelected");
				if(!row){
					$.showMessage('E00008');
					return false;
				}

				// 転送先情報
				index = that.repgrpInfo.TG016.idx;
				childurl = href[index];
				sendMode = 1;
				// オブジェクト作成
				$.setJSONObject(sendJSON, $.id_inp.txt_bmncd,    row.F16, row.F16);
				$.setJSONObject(sendJSON, $.id_inp.txt_kanrino,  row.F17, row.F17);
				$.setJSONObject(sendJSON, $.id_inp.txt_kanrieno, row.F18, row.F18);
				$.setJSONObject(sendJSON, $.id_inp.txt_addshukbn,row.F19, row.F19);
				break;
			case $.id.btn_upd:
			case $.id.btn_cancel:
			case $.id.btn_back:
				// 転送先情報
				index = that.repgrpInfo.TG001.idx;
				if(that.reportYobiInfo()==='1'){
					index = that.repgrpInfo.TG001_1.idx;
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
		changeInputboxFunc:function(that, id, newValue, obj){

			var parentObj = $('#'+that.focusRootId);
			var func_focus = function(){$.addErrState(that, $('#'+obj.attr('id')), true)};

			// DB問い合わせ系
			if($('[for_inp^='+id+'_]').length > 0){
				var param = that.getInputboxParams(that, id, newValue);
				$.getsetInputboxRowData(that.name, 'for_inp', id, param, that, parentObj);
			}

			// 検索、入力後特殊処理
			if(that.queried){
				// グリッド編集系処理
				if(that.focusGridId!==undefined && that.editRowIndex[that.focusGridId] > -1){
					var record = $('#'+that.focusGridId).datagrid("getRows")[that.editRowIndex[that.focusGridId]];
					// グリッド編集系変更処理
					record["F22"] = '1';		// CHANGE_IDX
				}
			}
		}
	} });
})(jQuery);