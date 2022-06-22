/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_ReportJU013',			// （必須）レポートオプションの確認
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
		dedefaultObjNum:	14,					// 初期化オブジェクト数
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
		oldValue:'',
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

			// 個別レイアウト調整：単品管理区分
			//$('#'+$.id_mei.kbn425).combobox({panelWidth:200,})

			that.onChangeReport = true;

			var count = 2;
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

			// 処理日付取得
			that.getsetInputboxData(reportno, $.id.txt_shoridt, [{}], $.id.action_init);

			that.setEditableGrid(that, reportno, "grd_tenhtsu_arr");

			//$.initialSearch(that);

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
			// 当帳票を「参照」で開いた場合
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

			$($.id.buttons).show();

			// 当帳票を「参照」で開いた場合
			if(that.reportYobiInfo()==='1'){
				$('#'+$.id.btn_upd).hide();
				$('#'+$.id.btn_upd).attr('disabled', 'disabled').hide();
				$('#'+$.id.btn_del).hide();
				$('#'+$.id.btn_del).attr('disabled', 'disabled').hide();
				$.setInputBoxDisable($($.id.hiddenChangedIdx));
				$.initReportInfo("JU013", "事前打出し　参照　商品情報");
			}else{
				$($.id.buttons).show();
				// 各種ボタン
				$.initReportInfo("JU013", "事前打出し 変更 商品情報");
			}
			$('#'+$.id.btn_cancel).on("click", $.pushChangeReport);
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
			if (rt == true)
			that.jsonString = that.jsonTemp.slice(0);
			// 入力チェック用の配列をクリア
			that.jsonTemp = [];
			return true;
		},
		success: function(reportno, sortable){	// （必須）正処理
			if (sortable) sortable=1; else sortable=0;
			var that = this;
			// 検索実行
			var szMoyscd	= $.getJSONValue(this.jsonHidden, $.id_inp.txt_moyscd);		// 催しコード（催し区分）
			var szKanrino	= $.getJSONValue(this.jsonHidden, $.id_inp.txt_kanrino);	// 管理番号

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			$($.id.gridholder).datagrid('loading');

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,		// レポート名
					t:				(new Date()).getTime(),
					sortable:		sortable,
					MOYSCD:			szMoyscd,
					KANRINO:		szKanrino,
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

					// 各グリッドの値を保持する
					that.grd_fsirt_data		 =  $('#grd_tenhtsu_arr').datagrid('getRows');

					if ($.getInputboxValue($('#sel_shohinkbn1'))==='0') {
						// 更新非対象項目は非活性に
						$.setInputBoxDisableVariable($('#'+$.id_inp.txt_irisu),true);
						$.setInputBoxDisableVariable($('#'+$.id_inp.txt_genkaam),true);
						$.setInputBoxDisableVariable($('#'+$.id_inp.txt_rg_baikaam),true);
					}

					$($.id.hiddenChangedIdx).val("");

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

			// 変更の場合確認メッセージを表示
			that.updConfirmMsg = "W00001";

			var param = {};
			param["KEY"] =  "SEL";
			param["SHNCD"] = $.getInputboxValue($('#'+$.id_inp.txt_shncd));
			param["NNDT"] = $.getInputboxValue($('#'+$.id_inp.txt_nndt));
			param["HTDT"]	= $.getInputboxValue($('#'+$.id_inp.txt_htdt));
			param["MOYSCD"] = $.getInputboxValue($('#'+$.id_inp.txt_moyscd));

			var rank = $.getInputboxValue($('#'+$.id_inp.txt_tenrank));
			var rank2 = $.getInputboxValue($('#'+$.id_inp.txt_tenrank2));

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
			var szKanrino	= $.getJSONValue(this.jsonHidden, $.id_inp.txt_kanrino);	// 管理番号
			param["KANRINO"] = szKanrino;

			var targetRows	= that.getGridData('grd_tenhtsu_arr');
			param["DATA_TENHT"] = JSON.stringify(targetRows); // 個別データグリッド:店別数量発注入力

			var row = $.getSelectListData(that.name, $.id.action_check,  $.id_inp.txt_shncd, [param]);

			// 重複エラーがある場合登録不可
			if(row.length != 0){
				$.showMessage("EX1047",[row[0].VALUE+"商品、納入日、店番で重複しない値"],function () { $.addErrState(that, $('#'+$.id_inp.txt_htdt),true) });
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
			var szKanrino	= $.getJSONValue(this.jsonHidden, $.id_inp.txt_kanrino);	// 管理番号

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
					KANRINO:		szKanrino,
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

			// 保持データを更新する
			// 複数店舗一覧
			var gridData = that.getGridData(txt_sircd, 'grd_tenhtsu_arr');
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

			var szKanrino	= $.getJSONValue(this.jsonHidden, $.id_inp.txt_kanrino);	// 管理番号

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
					DATA:			JSON.stringify(targetDatas),	// 更新対象情報
					KANRINO:		szKanrino,
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
			// 催しコード
			this.jsonTemp.push({
				id:		$.id_inp.txt_moyscd,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_moyscd),
				text:	''
			});
			// 管理番号
			this.jsonTemp.push({
				id:		$.id_inp.txt_kanrino,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_kanrino),
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

			var funcClickRow = $.fn.datagrid.defaults.onClickRow;
			var funcBeginEdit= $.fn.datagrid.defaults.onBeginEdit;
			var funcEndEdit= $.fn.datagrid.defaults.onEndEdit;
			var funcAfterEdit= $.fn.datagrid.defaults.onAfterEdit;
			if((that.sendBtnid!==$.id.btn_select && that.sendBtnid!==$.id.btn_sel_shninfo) ||
					(that.sendBtnid===$.id.btn_select && that.reportYobiInfo()==='0') ||
					(that.sendBtnid===$.id.btn_sel_shninfo && that.reportYobiInfo()==='0')
			){
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
					var txt_moyscd = $.getInputboxValue($('#'+$.id_inp.txt_moyscd));
					var txt_kanrino = $.getJSONValue(that.jsonHidden, $.id_inp.txt_kanrino);

					var json = [{"callpage":"Out_ReportJU013","MOYSCD":txt_moyscd,"KANRINO":txt_kanrino}];
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
					var gridData = that.getGridData(txt_sircd, id);
					that.setGridData(gridData, id);
					that.queried = true;
				},
				onClickRow: funcClickRow,
				onBeginEdit:funcBeginEdit,
				onEndEdit: funcEndEdit,
				onAfterEdit: funcAfterEdit
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
			$.setJSONObject(sendJSON, 'callpage', that.name, that.name);								// 呼出し元レポート情報

			// 戻る実行時用に現在の画面情報を保持する
			var states = $.getBackBaseJSON(that);
			// 各種グリッド情報を設定
			var newrepinfos = $.getBackJSON(that, states);
			$.setJSONObject(sendJSON, 'repinfo', newrepinfos, '');

			// 呼出別処理
			switch (btnId) {
			case $.id.btn_back:
			case $.id.btn_cancel:
			case "btn_return":

				// 転送先情報
				sendMode = 2;

				// 元画面情報
				var callpage = $.getJSONValue(that.jsonHidden, "callpage");
				// 転送先情報
				index = 3;

				if(callpage==='Out_ReportMM001') {
					var reportYobi1 = $.getJSONValue(that.jsonHidden, "reportYobi1");
					if (reportYobi1 === '0') {
						index = that.repgrpInfo.MM001.idx;
					} else {
						index = that.repgrpInfo.MM001_1.idx;
					}
				}else if(callpage==='Out_ReportMM002') {
					index = that.repgrpInfo.MM002.idx;
				}

				$.setJSONObject(sendJSON, 'reportYobi1', that.reportYobiInfo(), that.reportYobiInfo());		// 参照情報保持
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

				if (id===$.id_inp.txt_rg_baikaam) {
					that.oldValue = $.getInputboxValue($('#'+id));
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
					if (that.reportYobiInfo()!=='1') {
						// 更新非対象項目は非活性に
						$.setInputBoxEnableVariable($('#'+$.id_inp.txt_irisu),true);
						$.setInputBoxEnableVariable($('#'+$.id_inp.txt_genkaam),true);
						$.setInputBoxEnableVariable($('#'+$.id_inp.txt_rg_baikaam),true);
					}
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
					param = [{"value":shncd,"shnkbn":shnkbn}];
				} else {
					param = that.getInputboxParams(that, forinp_id, newValue);
				}

				var rows = $.getSelectListData(that.name, $.id.action_change, forinp_id, param);
				var row = rows.length > 0 ? rows[0]:"";
				$.setInputboxRowData('for_inp', forinp_id, row, that, parentObj);
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

		getInputboxParams: function(that, id, newValue){
			// 情報取得
			var values = {};
			values["value"] = newValue;
			values["TABLEKBN"] = that.baseTablekbn;

			// 情報設定
			return [values];
		},
	} });
})(jQuery);