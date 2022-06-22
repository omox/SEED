/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_ReportGM003',			// （必須）レポートオプションの確認
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
		dedefaultObjNum:	37,	// 初期化オブジェクト数
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
		gridData2:[],						// 検索結果
		gridTitle:[],						// 検索結果
		updConfirmMsg:"",
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

			// 初期検索可能
			that.onChangeReport = true;


			// 入力テキストボックス系
			var inputbox = Object.getOwnPropertyNames($.id_inp);
			for ( var sel in inputbox ) {
				if($('#'+$.id_inp[inputbox[sel]]).length > 0){
					$.setInputbox(that, reportno, $.id_inp[inputbox[sel]], isUpdateReport);
				}
			}

			that.setGrid(that, reportno, $.id.grd_set+'_list');
			that.setGrid(that, reportno, $.id.grd_set2+'_list');
			// 初期化終了
			this.initializes =! this.initializes;

			// ログ出力
			$.log(that.timeData, 'initialize:');
		},
		searched_initialize: function (reportno, opts){	// 検索結果を受けての初期化
			var that = this;
			$.extendDatagridEditor(that);
			// プライスカード発行枚数

			if(that.sendBtnid===$.id.btn_new){
				$.winST009.init(that);	// 対象店
				$.winST007.init(that);	// 除外店
			}

			$.winBM015.init(that);	// 対象店確認

			// チェックボックスの設定
			$.initCheckboxCss($("#"+that.focusRootId));
			// キーイベントの設定
			$.initKeyEvent(that);

			if(that.sendBtnid===$.id.btn_new){

				$.setInputboxValue($('#hiddenUpddt'),'');
			}

			that.queried = true;

			// ログ出力
			$.log(that.timeData, 'searched_initialize:');
		},
		setInitObjectState: function(){	// 画面初期化時の項目制御
			var that = this;
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

			// 当帳票を「新規」で開いた場合
			if(that.sendBtnid===$.id.btn_new){
				$('#'+$.id.btn_del).linkbutton('disable').attr('disabled', 'disabled').hide();

				$.initReportInfo("GM003", "セット販売　新規　明細", "新規");
			} else if (that.sendBtnid===$.id.btn_sel_change) {
				$('#'+$.id.btn_rankno+'_add').linkbutton('disable').attr('disabled', 'disabled');
				$('#'+$.id.btn_rankno+'_del').linkbutton('disable').attr('disabled', 'disabled');

				$.setInputBoxDisable($('#'+$.id_inp.txt_moyscd));
				$.setInputBoxDisable($('#'+$.id_inp.txt_bmncd));
				$.setInputBoxDisable($('#'+$.id_inp.txt_rankno_add));
				$.setInputBoxDisable($('#'+$.id_inp.txt_rankno_del));
				$.setInputBoxDisable($('#'+$.id_inp.txt_stno2));
				$.initReportInfo("GM003", "セット販売　変更　明細", "変更");
			} else {
				$('#'+$.id.btn_rankno+'_add').linkbutton('disable').attr('disabled', 'disabled');
				$('#'+$.id.btn_rankno+'_del').linkbutton('disable').attr('disabled', 'disabled');
				$('#'+$.id.btn_upd).linkbutton('disable').attr('disabled', 'disabled').hide();
				$('#'+$.id.btn_del).linkbutton('disable').attr('disabled', 'disabled').hide();
				$('#'+$.id.btn_cancel).linkbutton('disable').attr('disabled', 'disabled').hide();

				$.setInputBoxDisable($('#'+$.id_inp.txt_moyscd));
				$.setInputBoxDisable($($.id.hiddenChangedIdx));
				$.setInputBoxDisable($('#'+$.id_inp.txt_bmncd));
				$.setInputBoxDisable($('#'+$.id_inp.txt_rankno_add));
				$.setInputBoxDisable($('#'+$.id_inp.txt_rankno_del));
				$.initReportInfo("GM003", "セット販売　参照　明細", "参照");
			}

			// 各種遷移ボタン
			$('#'+$.id.btn_cancel).on("click", $.pushChangeReport);

			// 更新非対象項目は非活性に
			$.setInputBoxDisable($('#'+$.id_inp.txt_moykn));
			$.setInputBoxDisable($('#'+$.id_inp.txt_moysstdt));
			$.setInputBoxDisable($('#'+$.id_inp.txt_moyseddt));
			$.setInputBoxDisable($('#'+$.id_inp.txt_plusddt));
			$.setInputBoxDisable($('#'+$.id_inp.txt_bmnno));
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
			return rt;
		},
		success: function(reportno, sortable){	// （必須）正処理

			if (sortable) sortable=1; else sortable=0;
			var that = this;

			// 検索実行
			var txt_moyskbn		= $.getJSONObject(this.jsonString, $.id_inp.txt_moyskbn).value;		// 催し区分
			var txt_moysstdt	= $.getJSONObject(this.jsonString, $.id_inp.txt_moysstdt).value;	// 催し開始日
			var txt_moysrban	= $.getJSONObject(this.jsonString, $.id_inp.txt_moysrban).value;	// 催し連番
			var txt_stno		= $.getJSONObject(this.jsonString, $.id_inp.txt_stno2).value;		// B/M番号

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$.appendMaskMsg();

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,		// レポート名
					MOYSKBN:		txt_moyskbn,
					MOYSSTDT:		txt_moysstdt,
					MOYSRBAN:		txt_moysrban,
					STNO:			txt_stno,
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

					// 検索結果をうけての子テーブルマスタ項目などの初期化設定
					that.searched_initialize(reportno, opts);

					// 現在情報を変数に格納(追加した情報については個別にロード成功時に実施)
					that.setGridData(that.getGridData("", "grd_data"), "grd_data");

					// 初期表示時に既に部門、対象店の入力があった場合
					var rankno = $.getInputboxValue($('#'+$.id_inp.txt_rankno_add));
					var bmncd = $.getInputboxValue($('#'+$.id_inp.txt_bmncd));
					if (rankno!=='' && rankno!==null && rankno!==undefined &&
							bmncd!=='' && bmncd!==null && bmncd!==undefined) {
						$.winBM015.getTenArr($.id_inp.txt_rankno_add);
					}

					// 状態保存
					$.saveState2(reportno, that.getJSONString());

					// ログ出力
					$.log(that.timeData, 'loaded:');
				}
			);
		},
		updValidation: function (){	// （必須）批准

			//プライスカード発行枚数グリッドの編集を終了する。
			var row = $('#'+$.id.grd_set+'_list').datagrid("getSelected");
			var rowIndex = $('#'+$.id.grd_set+'_list').datagrid("getRowIndex", row);
			$('#'+$.id.grd_set+'_list').datagrid('endEdit',rowIndex);
			//プライスカード発行枚数グリッドの編集を終了する。
			var row = $('#'+$.id.grd_set2+'_list').datagrid("getSelected");
			var rowIndex = $('#'+$.id.grd_set2+'_list').datagrid("getRowIndex", row);
			$('#'+$.id.grd_set2+'_list').datagrid('endEdit',rowIndex);

			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform).form('validate');
			if(!rt){
				$.addErrState(that, $('.validatebox-invalid').eq(0), false);
				return rt;
			}

			// 変更の場合確認メッセージを表示
			that.updConfirmMsg = "W00001";
			if (that.sendBtnid===$.id.btn_sel_change) {
				var param = {};
				param["KEY"] =  "CNT";
				param["value"] = $.getInputboxValue($('#'+$.id_inp.txt_moyscd));
				var chk_cnt = $.getInputboxData(that.name, $.id.action_check, $.id_inp.txt_moyscd,[param]);
				if(chk_cnt==="1"){
					that.updConfirmMsg = "E20271";
				}
			}

			// TODO:背景を赤くする対応を追加
			//var msgid = that.checkInputboxFunc($.id_inp.txt_shncd);
			//msgid = that.checkInputboxFunc($.id_inp.txt_maisu);
			var msgid = null;
			if(msgid !==null){
				$.showMessage(msgid);
				return false;
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

			// 入力データ：催し送信＿商品一覧
			var targetRowsBmshn = that.getGridData($.id.grd_set+'_list');
			// 入力データ：催し送信＿商品一覧
			var targetRowsBmshn2 = that.getGridData($.id.grd_set2+'_list');

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$.appendMaskMsg();

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,						// レポート名
					action:			$.id.action_update,				// 実行処理情報
					obj:			id,								// 実行オブジェクト
					SENDBTNID:		that.sendBtnid,
					DATA:			JSON.stringify(targetDatas),		// 更新対象情報(セット販売情報情報)
					DATA_BMSHN:		JSON.stringify(targetRowsBmshn),	// 更新対象情報(セット販売＿商品一覧)
					DATA_BMSHN2:	JSON.stringify(targetRowsBmshn2),	// 更新対象情報(セット販売＿商品一覧)
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
						that.changeReport(that.name, 'btn_return');
					};
					$.updNormal(data, afterFunc);

					// ログ出力
					$.log(that.timeData, 'loaded:');
				}
			);
		},
		updConfirm: function(func){	// validation OK時 の update処理
			var that = this;
			var msgId = that.updConfirmMsg;
			$.showMessage(msgId, undefined, func);
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

			// 入力データ：催し送信＿商品一覧
			var targetRowsBmshn = that.getGridData($.id.grd_set+'_list');
			var targetRowsBmshn2 = that.getGridData($.id.grd_set2+'_list');

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$.appendMaskMsg();

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,							// レポート名
					action:			$.id.action_delete,					// 実行処理情報
					obj:			id,									// 実行オブジェクト
					DATA:			JSON.stringify(targetDatas),		// 更新対象情報(BM催し送信情報)
					DATA_BMSHN:		JSON.stringify(targetRowsBmshn),	// 更新対象情報(BM催し送信＿商品一覧)
					DATA_BMSHN2:	JSON.stringify(targetRowsBmshn2),	// 更新対象情報(BM催し送信＿商品一覧)
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
			// 催し開始日
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
			// セット番号
			this.jsonTemp.push({
				id:		$.id_inp.txt_stno2,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_stno2),
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
		setGrid: function(that, reportno, id, chk){		// データ表示
			var index = -1;

			var columns = that.getGridColumns(that, id);

			var funcClickRow = $.fn.datagrid.defaults.onClickRow;
			var funcBeginEdit= $.fn.datagrid.defaults.onBeginEdit;
			var funcEndEdit= $.fn.datagrid.defaults.onEndEdit;
			var funcAfterEdit= $.fn.datagrid.defaults.onAfterEdit;
			if(that.sendBtnid!==$.id.btn_sel_refer){
				that.editRowIndex[id] = -1;
				funcClickRow = function(index,field){$.clickEditableDatagridCell(that,id, index)};
				funcBeginEdit = function(index,row){
					$.beginEditDatagridRow(that,id, index, row);
				};
				funcEndEdit = function(index,row,changes){
					$.endEditDatagridRow(that, id, index, row);
					that.setEdit(id,true);
				};
				funcAfterEdit = function(index,row,changes){
					var rowobj = $('#'+id).datagrid('getPanel').find("[datagrid-row-index='"+index+"']");
					// チェックボックスの再追加（EndEdit時に削除されるため）
					$.afterEditAddCheckbox(rowobj);
					// ボタンオブジェクトの再追加（EndEdit時に削除されるため）
					rowobj.find(".easyui-linkbutton").on("click", $.pushUpd).linkbutton({ width:  45, height: 18});
					var rows = $('#'+id).datagrid('getRows');
					that.setEdit(id,true);

				};

				// 編集可能データグリッドの共通処理設定
				// 編集エディターの機能拡張（非表示で参照用のオブジェクトが準備してある前提、参照オブジェクトはグリッドのフィールド名を元に判断）
				$.extendDatagridEditor();
			}

			$('#'+id).datagrid({
				url:$.reg.easy,
				columns:columns,
				fit:true,
				singleSelect:true,
				checkOnSelect:false,
				selectOnCheck:false,
				onBeforeLoad:function(param){
					index = -1;
					var txt_moyskbn		= $.getJSONValue(that.jsonHidden, $.id_inp.txt_moyskbn);	// 催し区分
					var txt_moysstdt	= $.getJSONValue(that.jsonHidden, $.id_inp.txt_moysstdt);	// 催し開始日
					var txt_moysrban	= $.getJSONValue(that.jsonHidden, $.id_inp.txt_moysrban);	// 催し連番
					var txt_stno2		= $.getJSONValue(that.jsonHidden, $.id_inp.txt_stno2);		// セット番号

					var json = [{"callpage":"Out_ReportGM003","MOYSKBN":txt_moyskbn,"MOYSSTDT":txt_moysstdt,"MOYSRBAN":txt_moysrban,"SETNO":txt_stno2}];
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
					// 情報保持
					var gridData = that.getGridData(id);
					that.setGridData(gridData,id);

					// チェックボックスの設定
					$.initCheckboxCss($('#'+id).datagrid('getPanel').find('.datagrid-body'));
				},
				onClickRow: funcClickRow,
				onBeginEdit:funcBeginEdit,
				onEndEdit: funcEndEdit,
				onAfterEdit: funcAfterEdit,
				onSelect:function(index){
					if(that.sendBtnid!==$.id.btn_sel_refer){
						var rows = $('#'+id).datagrid('getRows');
						if(id=='grd_set_list'){
							var row = $('#'+$.id.grd_set2+'_list').datagrid("getSelected");
							var rowIndex = $('#'+$.id.grd_set2+'_list').datagrid("getRowIndex", row);
							$('#'+$.id.grd_set2+'_list').datagrid('endEdit',rowIndex);
							that.editRowIndex[$.id.grd_set2+'_list'] = -1;
						}else{
							var row = $('#'+$.id.grd_set+'_list').datagrid("getSelected");
							var rowIndex = $('#'+$.id.grd_set+'_list').datagrid("getRowIndex", row);
							$('#'+$.id.grd_set+'_list').datagrid('endEdit',rowIndex);
							that.editRowIndex[$.id.grd_set+'_list'] = -1;
						}
					}
					that.setEdit(id,true);
				},
			});
		},
		setEdit: function(id,flg){
			var col = $('#'+id).datagrid('getColumnOption', 'SHNCD');
			if (flg) {
				col.formatter = function(value,row,index){
					return $.getFormatPrompt(value, '####-####');
				};
				col.editor = {type:'numberbox'};
				col.styler = '';
			} else {
				col.editor = {
						type:'numberbox',
						options:{cls:'labelInput',editable:false,disabled:true,readonly:true}
					};
				col.styler = function(value,row,index){return 'background-color:#f5f5f5;';};
			}
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
			var cstyler =function(value,row,index){return 'color: red;font-weight: bold;';};
			var fields = ["SHNCD","SHNKN"];
			var titles = ["商品コード","商品名"];


			columnBottom.push({field:fields[0],	title:titles[0],	width:80,	halign:'center', align:'left',
				formatter:function(value,row,index){
					return $.getFormatPrompt(value, '####-####');
				},editor:{type:'numberbox'}});
			columnBottom.push({field:fields[1],	title:titles[1],	width:300,	halign:'center',align:'left',	editor:{type:'textbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}});
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
		repgrpInfo: {
			MM001:{idx:1},		// 催し検索 変更 催し一覧
			MM001_1:{idx:2},	// 催し検索 参照 催し一覧
			MM002:{idx:3}		// 催し検索 商品一覧
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
			$.setJSONObject(sendJSON, 'callpage', that.name, that.name);							// 呼出し元レポート情報

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
				sendMode = 2;

				// 元画面情報
				var callpage = $.getJSONValue(that.jsonHidden, "callpage");
				// 転送先情報
				if(callpage==='Out_ReportGM001'){
					index = 1;
				}else if(callpage==='Out_ReportGM002') {
					index = 4;
				}

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
		changeInputboxFunc:function(that, id, newValue, obj){

			var that = this;
			var parentObj = $('#'+that.focusRootId);
			if(id+"_"===obj.attr('id') && that.focusGridId!==""){
				parentObj = $('#'+that.focusGridId).datagrid('getPanel');
			}

			// TODO:背景を赤くする対応を追加
			var msgid = that.checkInputboxFunc(id,newValue);
			if(msgid !==null){
				$.showMessage(msgid, undefined, function () { $.addErrState(that, $('#'+id),true) });
				return false;
			}

			// 店配列取得
			$.winBM015.getTenArr(id);

			// DB問い合わせ系
			if($('[for_inp^='+id+'_]').length > 0){
				var param = that.getInputboxParams(that, id, newValue);
				$.getsetInputboxRowData(that.name, 'for_inp', id, param, that, parentObj);
			}

			var txt_moyscd = $.getInputboxValue($('#'+$.id_inp.txt_moyscd));
			var txt_moysrban = "";

			if (txt_moyscd.length >= 7) {
				for (var i=7; i<txt_moyscd.length; i++) {
					txt_moysrban += txt_moyscd.slice(i,i+1);
				}

				txt_moyscd = txt_moyscd.slice(0,7) + ("000"+txt_moysrban).slice(-3);
			}

			$.setInputboxValue($('#'+$.id_inp.txt_moyscd), txt_moyscd);

			// 催し期間が設定された場合、販売期間を設定
			if (id===$.id_inp.txt_moyseddt) {
				$.setInputboxValue($('#'+$.id_inp.txt_hbstdt), $.getInputboxText($('#'+$.id_inp.txt_moysstdt)));
				$.setInputboxValue($('#'+$.id_inp.txt_hbeddt), newValue);
			}

		},
		getInputboxParams: function(that, id, newValue){
			// 情報取得
			var values = {};
			values["value"] = newValue;

			// 情報設定
			return [values];
		},
		getGridData: function (target){

			var targetRows= [];


			if(target===undefined || target==="grd_data"){
				var targetData = [{}];
				$('#'+that.focusRootId).find('[col^=F]').each(function(){
					var col = $(this).attr('col');
					var val = $.getInputboxValue($(this));
					if($(this).hasClass('easyui-combobox') && val==="-1"){ val = "";}
					targetData[0][col] = val;
				});
				data["grd_data"] = targetData;
			}


			// プライスカード発行枚数
			if(target===undefined || target===$.id.grd_set+'_list'){
				var rowsPcardsu= $('#'+$.id.grd_set+'_list').datagrid('getRows');
				for (var i=0; i<rowsPcardsu.length; i++){
					if(rowsPcardsu[i]["SHNCD"] == "" || rowsPcardsu[i]["SHNCD"] == null ){

					}else{
						var rowDate = {
								F1	 : rowsPcardsu[i]["SHNCD"],
								F2	 : rowsPcardsu[i]["SHNKN"],
							};
						targetRows.push(rowDate);
					}
				}
			}
			if(target===undefined || target===$.id.grd_set2+'_list'){
				var rowsPcardsu= $('#'+$.id.grd_set2+'_list').datagrid('getRows');
				for (var i=0; i<rowsPcardsu.length; i++){
					if(rowsPcardsu[i]["SHNCD"] == "" || rowsPcardsu[i]["SHNCD"] == null ){

					}else{
						var rowDate = {
								F1	 : rowsPcardsu[i]["SHNCD"],
								F2	 : rowsPcardsu[i]["SHNKN"],
						};
						targetRows.push(rowDate);
					}
				}
			}
			return targetRows;
		},
		setGridData: function (data,target){
			var that = this;

			if(target===undefined || target==="grd_data"){
				that.grd_data =  data["grd_data"];
			}

			if(target===undefined || target===$.id.grd_set+'_list'){
				that.gridData =  data;
			}

			if(target===undefined || target===$.id.grd_set2+'_list'){
				that.gridData2 =  data;
			}
			// 基本データ
			return true;
		},
		checkInputboxFunc: function(id, newValue){

			var that = this;

			// 催しコード存在チェック
			if (id===$.id_inp.txt_moyscd) {
				if (newValue !== null && newValue !== '' && newValue !== undefined) {

					var moyskbn = newValue.substr(0,1);

					if (moyskbn!=='1' && moyskbn!=='2' && moyskbn!=='3'){
						return "E20004";
					}

					// マスタ存在チェック
					var param = {};
					param["KEY"] =  "MST_CNT";
					param["value"] = newValue;

					var chk_cnt = $.getInputboxData(that.name, $.id.action_check, $.id_inp.txt_moyscd, [param]);
					if(chk_cnt==="" || chk_cnt==="0"){
						return "E20005";
					}
				}
			}

			if (id===$.id_inp.txt_bmncd) {
				if (newValue !== null && newValue !== '' && newValue !== undefined) {
					// マスタ存在チェック
					var param = {};
					param["KEY"] =  "MST_CNT";
					param["value"] = newValue;

					var chk_cnt = $.getInputboxData(that.name, $.id.action_check, $.id_inp.txt_bmncd, [param]);
					if(chk_cnt==="" || chk_cnt==="0"){
						return "E11097";
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
	} });
})(jQuery);