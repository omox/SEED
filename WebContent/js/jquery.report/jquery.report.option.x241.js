/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_Reportx241',			// （必須）レポートオプションの確認
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
		dedefaultObjNum:	3,	// 初期化オブジェクト数
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
		clickBtnid: "",						// 当画面で押下されたボタンIDを保持
		focusRootId:"cc",					// （キー移動時必須）キー移動イベントのルートとなるパネルのID
		focusParentId:"",					// （キー移動時必須）現在フォーカスがあたっている項目の親となるパネルのID
		focusGridId:"",						// （キー移動時必須）現在フォーカスがあたっているDataGridのID
		editRowIndex:{},					// グリッド編集行保持
		gridData:[],						// 検索結果
		gridTitle:[],						// 検索結果
		shuno_list:{},						// 週Noの情報を保持（Init時に取得）
		grd_hattr:[],						// グリッド情報:店別数量
		oldBmn:"",
		oldDai:"",
		totalRows:0,
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

			var isUpdateReport = false;

			// 初期検索可能
			that.onChangeReport = true;

			$.setInputbox(that, reportno, 'txt_user_id', isUpdateReport);
			$.setInputbox(that, reportno, 'txt_name', isUpdateReport);

			// 店舗コード
			$.setMeisyoCombo(that, reportno, $.id.SelTenpo, isUpdateReport);

			// ユーザー情報
			that.setEditableGrid(that, reportno, $.id.gridholder);

			// 初期化終了
			this.initializes =! this.initializes;

			// キーイベントの設定
			$.initKeyEvent(that);

			// ログ出力
			$.log(that.timeData, 'initialize:');
		},
		setInitObjectState: function(){	// 画面初期化時の項目制御
			var that = this;
			$.reg.search = true;	// 当画面はデフォルト検索
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
			if (that.reportYobiInfo()==='1') {
				$('#'+$.id.btn_upd).linkbutton('disable').attr('disabled', 'disabled').hide();
				$('#'+$.id.btn_del).linkbutton('disable').attr('disabled', 'disabled').hide();
				$('#'+$.id.btn_cancel).linkbutton('disable').attr('disabled', 'disabled').hide();

				$.initReportInfo("x241", "ユーザー情報一覧　参照", "");
			}else{
				$.initReportInfo("x241", "ユーザー情報一覧　新規・更新", "");
			}

			// 各種遷移ボタン
			$('#'+$.id.btn_cancel).on("click", $.pushChangeReport);
			$('#'+$.id.btn_sel_change).on("click", $.pushChangeReport);

			// クリックされたボタンのIDを保持
			$('#'+$.id.btn_del).on("click", function(){that.clickBtnid = $.id.btn_del});

			// 全体処理
			if(that.reportYobiInfo()==='1'){
				$.setInputBoxDisable($($.id.hiddenChangedIdx));
			}
		},
		initCondition: function (){	// 条件初期値セット
			var that = this;
			// 初期化項目
		},
		clear:function(){
			// 隠し情報初期化
			$($.id.hiddenChangedIdx).val("");	// 変更行Index
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
			var txt_user_id	= $.getInputboxValue($('#txt_user_id'));	// ID
			var txt_name	= $.getInputboxValue($('#txt_name'));		// 姓名
			var sel_tenpo	= $.getInputboxValue($('#SelTenpo'));		// 店舗

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$.appendMaskMsg();

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,		// レポート名
					ID:				txt_user_id,
					NAME:			txt_name,
					TEN:			sel_tenpo,
					SENDBTNID:		that.sendBtnid,
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

					// 検索データ（想定）
					that.gridData = JSON.parse(json).rows;
					that.gridTitle = JSON.parse(json).titles;
					that.totalRows = JSON.parse(json).total;

					var opts = JSON.parse(json).opts

					// メインデータ表示
					that.setData(that.gridData, opts);
					that.queried = true;

					// データグリッド初期化
					that.setEditableGrid(that, reportno, $.id.gridholder);

					// 一覧画面へ戻る
					if (that.clickBtnid === $.id.btn_del || that.clickBtnid === $.id.btn_upd) {
						that.changeReport(that.name, 'btn_return');
					}

					that.queried = true;
					// Load処理回避
					$.tryChangeURL($.reg.jqeasy);

					var size = JSON.parse(json)["total"];
					if(size == 0){
						$.showMessage('E20038');
					}

					// グリッド再描画（easyui 1.4.2 対応）
					$($.id.gridholder).datagrid('load', {} );
					$.removeMask();

					// 選択状態をクリア
					$($.id.gridholder).datagrid('clearSelections');

					// 状態保存
					$.saveState2(reportno, that.getJSONString());

					// ログ出力
					$.log(that.timeData, 'loaded:');
				}
			);
		},
		delValidation: function (){	// （必須）批准
			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = true;

			// 選択行
			var row = $($.id.gridholder).datagrid('getSelections');
			if(row.length === 0){
				$.showMessage('E00008');
				return false;
			}

			return rt;
		},
		delSuccess: function(id){

			var that = this;
			var targetDatas = [{}];
			$('#'+that.focusRootId).find('[col^=F]').each(function(){
				var col = $(this).attr('col');
				targetDatas[0][col] = $.getInputboxValue($(this));
			});
			var row = $($.id.gridholder).datagrid("getSelections");

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$.appendMaskMsg();
			$.appendMask();

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,						// レポート名
					action:			$.id.action_delete,				// 実行処理情報
					obj:			id,								// 実行オブジェクト
					DATA:			JSON.stringify(row),	// 更新対象情報(配送グループ)
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
			// ユーザー
			this.jsonTemp.push({
				id:		'txt_user_id',
				value:	$('#txt_user_id').textbox('getValue'),
				text:	$('#txt_user_id').textbox('getText')
			});
			// 姓名
			this.jsonTemp.push({
				id:		'txt_name',
				value:	$('#txt_name').textbox('getValue'),
				text:	$('#txt_name').textbox('getText')
			});
			// 店舗
			this.jsonTemp.push({
				id:		'SelTenpo',
				value:	$('#SelTenpo').combobox('getValue'),
				text:	$('#SelTenpo').combobox('getText')
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
			var index = -1;
//			var frozenColumns	= that.getGridFrozenColumns(that, id);
//			var columns			= that.getGridColumns(that, id);

			// ページサイズ定義取得
			var pageList = [10,20,30,50,100,200];
			var pageSize = $.getJSONValue(this.jsonHidden, $.id.pageSize);
			if (pageSize==="") pageSize=50;
			pageSize = $.getDefaultPageSize(pageSize, pageList);

			var columns = [];
			var columnBottom=[];
			var cformatter =function(value,row,index){return value && value===$.id.value_on?$.id.text_on:$.id.text_off;};
			var cstyler =function(value,row,index){return 'color: red;font-weight: bold;';};

			$(id).datagrid({
				columns:[[
					{field:'ck',	checkbox:true,			width:35},
					{field:'F1',	title:'ユーザーID',		width:100},
					{field:'F2',	title:'パスワード',		width:160},
					{field:'F3',	title:'姓 名',			width:270},
					{field:'F4',	title:'所属',			width:250},
					{field:'F5',	title:'有効期限',		width:85},
					{field:'F6',	title:'本部マスタ',		width:85},
					{field:'F7',	title:'本部特売',		width:85},
					{field:'F8',	title:'店舗画面',		width:85},
					{field:'F9',	title:'更新ユーザー',	width:200},
					{field:'F10',	title:'更新日',			width:125},
					{field:'F11',	title:'ユーザーコード',	hidden:true},
				]],
				idField:'F1',
				autoRowHeight:false,
				pageList:pageList,
				pageSize:pageSize,
				rownumberWidth:45,
				onBeforeLoad:function(param){
					param.report = that.name;
				},
				onLoadSuccess:function(data){

					$('input[type=checkbox]').css('opacity','100');
					$('input[type=checkbox]').css('position','relative');
					$('.datagrid-header-check').css('height','20px')
					$('.datagrid-header-check input').css('margin-right','2px');
					// ログ出力
					$.log(that.timeData, 'query:');
				},
				pagePosition:'bottom',
				pagination:true,
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
		extenxDatagridEditorIds:{},
		setObjectState: function(){	// 軸の選択内容による制御

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
			var sendMode = 1;	// 遷移後に、前回検索条件を表示したい場合、""のまま、ここで設定した条件を表示したい場合"1"

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

			// 戻る実行時用に現在の画面情報を保持する
			var states = $.getBackBaseJSON(that);
			// 各種グリッド情報を設定
			var newrepinfos = $.getBackJSON(that, states);
			$.setJSONObject(sendJSON, 'repinfo', newrepinfos, '');

			// 選択行
			var row = $($.id.gridholder).datagrid('getSelections');

			// 呼出別処理
			switch (btnId) {
			case $.id.btn_sel_change:

				if(row.length === 0){
					$.showMessage('E00008');
					return false;
				}

				if (row.length !== 1){
					$.showMessage('EX1119',['編集するデータ１件']);
					return false;
				}

				index=2;
				childurl = href[index];

				// オブジェクト作成
				$.setJSONObject(sendJSON, 'txt_user_cd', $($.id.gridholder).datagrid("getSelected").F11, $($.id.gridholder).datagrid("getSelected").F11); // ユーザーコード
				break;
			case $.id.btn_cancel:
			case $.id.btn_back:
			case "btn_return":
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
					sendMode:	sendMode,
					sendParam:	JSON.stringify( sendJSON )
				}
			});
		},
	} });
})(jQuery);