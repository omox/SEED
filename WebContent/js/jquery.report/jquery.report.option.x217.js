/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_Reportx217',			// （必須）レポートオプションの確認
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
		dedefaultObjNum:	4,	// 初期化オブジェクト数
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

			// チェックボックス初期化
			$.setCheckboxInit2(that.jsonHidden, $.id.chk_updkbn, that);

			// 入力テキストボックス系
			var inputbox = Object.getOwnPropertyNames($.id_inp);
			for ( var sel in inputbox ) {
				if($('#'+$.id_inp[inputbox[sel]]).length > 0){
					$.setInputbox(that, reportno, $.id_inp[inputbox[sel]], isUpdateReport);
				}
			}

			// ラジオボタン系
			that.setRadio(that, $.id.rad_pcardsz2);

			// 初期化終了
			this.initializes =! this.initializes;

			// ログ出力
			$.log(that.timeData, 'initialize:');
		},
		searched_initialize: function (reportno, opts){	// 検索結果を受けての初期化
			var that = this;

			// プライスカード発行枚数
			that.setGrid(that, reportno, $.id.grd_pcardsu+'_list');

			// チェックボックスの設定
			$.initCheckboxCss($("#"+that.focusRootId));
			// キーイベントの設定
			$.initKeyEvent(that);

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
			if(that.sendBtnid===$.id.btn_sakubaikakb2){
				$('#'+$.id.btn_del).linkbutton('disable').attr('disabled', 'disabled').hide();
				$.initReportInfo("PC004", "プライスカード　（店指定）　新規", "新規");
			} else if (that.sendBtnid===$.id.btn_sel_change) {
				$.initReportInfo("PC004", "プライスカード　（店指定）　変更", "変更");
			} else {
				$('#'+$.id.btn_upd).linkbutton('disable').attr('disabled', 'disabled').hide();
				$('#'+$.id.btn_del).linkbutton('disable').attr('disabled', 'disabled').hide();
				$('#'+$.id.btn_cancel).linkbutton('disable').attr('disabled', 'disabled').hide();
				$.setInputBoxDisable($($.id.hiddenChangedIdx));
				$.initReportInfo("PC004", "プライスカード　（店指定）　参照", "参照");
			}

			// 各種遷移ボタン
			$('#'+$.id.btn_cancel).on("click", $.pushChangeReport);

			// 更新非対象項目は非活性に
			$.setInputBoxDisable($('#'+$.id_inp.txt_coman));
			$.setInputBoxDisable($('#'+$.id_inp.txt_mst_yoyakudt));
			$.setInputBoxDisable($("input[name="+$.id.rad_pcardsz2+"]"));
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
			var txt_inputno = $.getJSONObject(this.jsonString, $.id.txt_inputno).value;	// 入力番号

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$.appendMaskMsg();

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,		// レポート名
					INPUTNO:		txt_inputno,
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
					that.setGridData(that.getGridData($.id.grd_pcardsu+'_list'));

					// 状態保存
					$.saveState2(reportno, that.getJSONString());

					// ログ出力
					$.log(that.timeData, 'loaded:');
				}
			);
		},
		updValidation: function (){	// （必須）批准

			//プライスカード発行枚数グリッドの編集を終了する。
			var row = $('#'+$.id.grd_pcardsu+'_list').datagrid("getSelected");
			var rowIndex = $('#'+$.id.grd_pcardsu+'_list').datagrid("getRowIndex", row);
			$('#'+$.id.grd_pcardsu+'_list').datagrid('endEdit',rowIndex);

			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform).form('validate');
			if(!rt){
				$.addErrState(that, $('.validatebox-invalid').eq(0), false);
				return rt;
			}

			if(!that.checkInputboxFunc()) {
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

			// 入力データ：プライスカード発行枚数
			var targetRowsPcardsu = that.getGridData($.id.grd_pcardsu+'_list');

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
					DATA:			JSON.stringify(targetDatas),		// 更新対象情報(プライスカード発行トラン)
					DATA_PCSU:		JSON.stringify(targetRowsPcardsu),	// 更新対象情報(プライスカード発行枚数トラン)
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
					DATA:			JSON.stringify(targetDatas),		// 更新対象情報(プライスカード)
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
			// 入力No
			this.jsonTemp.push({
				id:		$.id.txt_inputno,
				value:	$.getJSONValue(this.jsonHidden, $.id.txt_inputno),
				text:	''
			});
			// コメント
			this.jsonTemp.push({
				id:		$.id_inp.txt_coman,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_coman),
				text:	''
			});
			// マスタ予約日付
			this.jsonTemp.push({
				id:		$.id_inp.txt_mst_yoyakudt,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_mst_yoyakudt),
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
		setRadio: function(reportno, name){
			var that = this;
			var idx = -1;

			var id = name;
			// Radio 要素の初期化
			// 初期化情報取得
			var json = $.getJSONObject(that.jsonHidden, id);
			if (json){
				// 初期化
				$('input[name="'+id+'"]').val([json.value]);
			}
			$('input[name="'+id+'"]').change(function() {
				if(idx > 0 && that.queried){
					$($.id.hiddenChangedIdx).val("1");

					//$("#"+$.id.grd_tengp+gpkbn).datagrid("reload");
					//that.editRowIndex[$.id.grd_tengp+gpkbn] = -1;
				}
			});

			if(that){
				if ($.inArray(id, that.initedObject) < 0){
					that.initedObject.push(id);
				}
				// 初期表示処理
				$.initialDisplay(that);
			}

			idx = 1;
		},
		setGrid: function(that, reportno, id, chk){		// データ表示
			var that = this;
			var init = true;
			// ページサイズ定義取得
			var pageList = $.fn.pagination.defaults.pageList;
			var pageSize = $.getJSONValue(this.jsonHidden, $.id.pageSize);
			if (pageSize==="") pageSize=$.fn.pagination.defaults.pageSize;
			pageSize = $.getDefaultPageSize(pageSize, pageList);

			// ページサイズ定義取得
			var pageList = $.fn.pagination.defaults.pageList;
			var pageSize = $.getJSONValue(this.jsonHidden, $.id.pageSize);
			if (pageSize==="") pageSize=$.fn.pagination.defaults.pageSize;
			pageSize = $.getDefaultPageSize(pageSize, pageList);

			var columns = that.getGridColumns(that, id);

			var funcBeforeLoad = function(param){
				index = -1;
				var txt_inputno		= $.getInputboxValue($('#'+$.id.txt_inputno));
				var json = [{"callpage":"Out_Reportx217","INPUTNO":txt_inputno}];
				// 情報設定
				param.page		=	reportno;
				param.obj		=	id;
				param.sel		=	(new Date()).getTime();
				param.target	=	id;
				param.action	=	$.id.action_init;
				param.json		=	JSON.stringify(json);
				param.datatype	=	"datagrid";
			};

			var funcEnter = function(e){
				if ($.endEditingDatagrid(that)){
					$.pushUpd(e);
				}
			};
			var funcClickRow = $.fn.datagrid.defaults.onClickRow;
			var funcBeginEdit= $.fn.datagrid.defaults.onBeginEdit;
			var funcEndEdit= $.fn.datagrid.defaults.onEndEdit;
			var funcAfterEdit= $.fn.datagrid.defaults.onAfterEdit;
			if(that.sendBtnid!==$.id.btn_sel_refer){
				that.editRowIndex[id] = -1;
				funcClickRow = function(index,field){$.clickEditableDatagridCell(that,id, index)};
				funcBeginEdit = function(index,row){
					$.beginEditDatagridRow(that,id, index, row)
				};
				funcEndEdit = function(index,row,changes){
					$.endEditDatagridRow(that, id, index, row);
				};
				funcAfterEdit = function(index,row,changes){
					var rowobj = $('#'+id).datagrid('getPanel').find("[datagrid-row-index='"+index+"']");
					// チェックボックスの再追加（EndEdit時に削除されるため）
					$.afterEditAddCheckbox(rowobj);
					// ボタンオブジェクトの再追加（EndEdit時に削除されるため）
					rowobj.find(".easyui-linkbutton").on("click", $.pushUpd).linkbutton({ width:  45, height: 18});
				};

				// 編集可能データグリッドの共通処理設定
				// 編集エディターの機能拡張（非表示で参照用のオブジェクトが準備してある前提、参照オブジェクトはグリッドのフィールド名を元に判断）
				$.extendDatagridEditor(that);
			}

			$('#'+id).datagrid({
				url:$.reg.easy,
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
				onBeforeLoad:funcBeforeLoad,
				onLoadSuccess:function(data){

					// 情報保持
					var gridData = that.getGridData($.id.grd_pcardsu+'_list');
					that.setGridData(gridData);

					// チェックボックスの設定
					$.initCheckboxCss($('#'+id).datagrid('getPanel').find('.datagrid-body'));

					// gridにfocusが合っている場合は最初の行を選択する。
					$(':focus').find('.easyui-datagrid').each(function(){
						var gridObj = this
						var gridId = gridObj.id

						if($('#'+gridId).datagrid('getRows').length > 0){
							$('#'+gridId).datagrid('selectRow', 0);
							if(that.editRowIndex!==undefined && that.editRowIndex[that.focusGridId]!==undefined){
								$('#'+gridId).datagrid('beginEdit', 0);
							}
						}
					})
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

			if (	(!jQuery.support.opacity)
					&&	(!jQuery.support.style)
					&&	(typeof document.documentElement.style.maxHeight == "undefined")
					) {
					// ページリストに select を利用している。IE6  のバグで z-index が適用されない。
					// modalダイアログを利用する場合は、表示なしにする必要あり。
					$.fn.pagination.defaults.showPageList = false;
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
			var targetId = $.id_inp.txt_tencd;
			var check = $('#'+targetId).attr("check") ? JSON.parse('{'+$('#'+targetId).attr("check")+'}'): JSON.parse('{}');	// コードのcheck要素を取得
			var formatterLPad = function(value){
				return $.getFormatLPad(value, check.maxlen);
			};
			var fields = ["TENCD","TENKN","SHNCD","SHNKN","SEQ"];
			var titles = ["店コード","店名","商品コード","商品名","SEQ"];

			if (that.sendBtnid===$.id.btn_sel_change) {
				columnBottom.push({field:'UPDKBN',	title:'削除',	width:35,	align:'center',	formatter:cformatter,	editor:{type:'checkbox'},	styler:cstyler});
			} else {
				columnBottom.push({field:'UPDKBN',	title:'削除',	hidden:true});
			}
			columnBottom.push({field:fields[0],	title:titles[0],	width:60,	halign:'center',align:'left',formatter:formatterLPad,editor:{type:'numberbox'}});
			columnBottom.push({field:fields[1],	title:titles[1],	width:220,	halign:'center',align:'left',	editor:{type:'textbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}});
			columnBottom.push({field:fields[2],	title:titles[2],	width:80,	halign:'center',align:'left',
				formatter:function(value,row,index){
					return $.getFormatPrompt(value, '####-####');
				},editor:{type:'numberbox'}}
			);
			columnBottom.push({field:fields[3],	title:titles[3],	width:300,	halign:'center',align:'left',	editor:{type:'textbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}});
			columnBottom.push({field:fields[4],	title:titles[4],	hidden:true});
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
			var sendMode = "1";	// 遷移後に、前回検索条件を表示したい場合、""のまま、ここで設定した条件を表示したい場合"1"

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
				if(that.sendBtnid===$.id.btn_sakubaikakb2){
					index = 1;
				} else {
					index = 2;
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
		changeInputboxFunc:function(that, id, newValue, obj){

			var that = this;
			var parentObj = $('#'+that.focusRootId);
			if(id+"_"===obj.attr('id') && that.focusGridId!==""){
				parentObj = $('#'+that.focusGridId).datagrid('getPanel');
			}

			// DB問い合わせ系
			if (id===$.id_inp.txt_shncd && newValue.length > 8) {
				newValue = newValue.substr(0,8);
			}
			if($('[for_inp^='+id+'_]').length > 0){
				var param = that.getInputboxParams(that, id, newValue);
				var rows = $.getSelectListData(that.name, $.id.action_change, id, param);
				var row = rows.length > 0 ? rows[0]:"";
				$.setInputboxRowData('for_inp', id, row, that, parentObj);
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

			// プライスカード発行枚数
			if(target===undefined || target===$.id.grd_pcardsu+'_list'){
				var rowsPcardsu= $('#'+$.id.grd_pcardsu+'_list').datagrid('getRows');
				for (var i=0; i<rowsPcardsu.length; i++){
					if((rowsPcardsu[i]["TENCD"] == "" || rowsPcardsu[i]["TENCD"] == null) &&
							(rowsPcardsu[i]["SHNCD"] == "" || rowsPcardsu[i]["SHNCD"] == null)){

					}else{
						var rowDate = {
								F1	 : rowsPcardsu[i]["UPDKBN"],
								F2	 : rowsPcardsu[i]["TENCD"],
								F3	 : rowsPcardsu[i]["TENKN"],
								F4	 : rowsPcardsu[i]["SHNCD"],
								F5	 : rowsPcardsu[i]["SHNKN"],
								F6	 : rowsPcardsu[i]["SEQ"],
							};
						targetRows.push(rowDate);
					}
				}
			}
			return targetRows;
		},
		setGridData: function (data){
			var that = this;

			// 基本データ
			that.gridData =  data;
			return true;
		},
		checkInputboxFunc: function(){

			var that = this;

			// プライスカード発行枚数一覧
			var tencd		= "";
			var shncd		= "";
			var errFlg		= true; // グリッドの入力チェックに使用
			var targetRows	= $('#'+$.id.grd_pcardsu+'_list').datagrid('getRows');

			for (var i=0; i<targetRows.length; i++){
				// 商品コードを格納
				tencd = targetRows[i]["TENCD"];
				shncd = targetRows[i]["SHNCD"];

				if (!$.isEmptyVal(tencd)) {
					// マスタ存在チェック
					var param = {};
					param["KEY"] =  "MST_CNT";
					param["value"] = tencd;

					var chk_cnt = $.getInputboxData(that.name, $.id.action_check, $.id_inp.txt_tencd, [param]);
					if(chk_cnt==="" || chk_cnt==="0"){
						$.showMessage("E11096", undefined, function(){$.addErrState(that, $('#'+$.id.grd_pcardsu+'_list'), true, {NO:i, ID:$.id_inp.txt_tencd})});
						return false;
					}
					errFlg = false;
				}

				if (!$.isEmptyVal(shncd)) {
					var param = {};
					param["KEY"] =  "MST_CNT";
					param["value"] = shncd;

					var chk_cnt = $.getInputboxData(that.name, $.id.action_check, $.id_inp.txt_shncd, [param]);
					if(chk_cnt==="" || chk_cnt==="0"){
						$.showMessage("E11046", undefined, function(){$.addErrState(that, $('#'+$.id.grd_pcardsu+'_list'), true, {NO:i, ID:$.id_inp.txt_shncd})});
						return false;
					}
					errFlg = false;
				}

				maisu = targetRows[i]["MAISU"];

				if ((!$.isEmptyVal(tencd) || !$.isEmptyVal(shncd))) {
					if (($.isEmptyVal(tencd) || $.isEmptyVal(shncd))) {
						$.showMessage("E11049", ["店コードと商品コードを"], function(){$.addErrState(that, $('#'+$.id.grd_pcardsu+'_list'), true, {NO:i, ID:$.id_inp.txt_tencd})});
						return false;
					}
				}
			}

			// 商品コードの入力が存在しなかった場合
			if (errFlg) {
				$.showMessage("EX1047",["店コード"]);
				return false;
			}
			return true;
		},
	} });
})(jQuery);