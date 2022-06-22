/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_Reportx203',			// （必須）レポートオプションの確認
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
		scrollToId:[],						// 戻り時にフォーカス行を指定したい場合(gridholder以外)は指定
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

			// 編集可能データグリッドの共通処理設定
			// 編集エディターの機能拡張（非表示で参照用のオブジェクトが準備してある前提、参照オブジェクトはグリッドのフィールド名を元に判断）
			//$.extendDatagridEditor();

			// 初期検索可能
			that.onChangeReport = true;

			// 入力テキストボックス系
			var inputbox = Object.getOwnPropertyNames($.id_inp);
			for ( var sel in inputbox ) {
				if($('#'+$.id_inp[inputbox[sel]]).length > 0){
					$.setInputbox(that, reportno, $.id_inp[inputbox[sel]], isUpdateReport);
				}
			}

			// 初期表示時に検索処理を通らない為フラグをtrueに
			that.queried = true;

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
			var sendBtnid = $.getJSONValue(that.jsonHidden, "sendBtnid");
			if(!sendBtnid){
				sendBtnid = $('#sendBtnid').val();
			}
			$('#sendBtnid').val(sendBtnid);
			that.sendBtnid = sendBtnid;

			if (sendBtnid && sendBtnid.length > 0) {
				$.reg.search = true;
			}

			var reportYobi1 = $.getJSONValue(that.jsonHidden, "reportYobi1");
			if(!reportYobi1){
				reportYobi1 = $('#reportYobi1').val();
			}
			$('#reportYobi1').val(reportYobi1);

			// 当帳票を「参照」で開いた場合
			if(that.reportYobiInfo()==='1'){
				$('#'+$.id.btn_new).linkbutton('disable').attr('disabled', 'disabled').hide();
				$('#'+$.id.btn_sel_change).linkbutton('disable').attr('disabled', 'disabled').hide();
				$.initReportInfo("SI034", "配送グループ　店グループ一覧　参照", "参照");
			} else {
				$('#'+$.id.btn_sel_refer).linkbutton('disable').attr('disabled', 'disabled').hide();
				$.initReportInfo("SI034", "配送グループ　店グループ一覧", "一覧");
			}

			// 各種遷移ボタン
			$('#'+$.id.btn_new).on("click", $.pushChangeReport);
			$('#'+$.id.btn_sel_change).on("click", $.pushChangeReport);
			$('#'+$.id.btn_sel_refer).on("click", $.pushChangeReport);

			// 更新非対象項目は非活性に
			$.setInputBoxDisable($('#'+$.id_inp.txt_hsgpcd));
			$.setInputBoxDisable($('#'+$.id_inp.txt_hsgpkn));

			// 変更
			$($.id.hiddenChangedIdx).val('');
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
			var txt_hsgpcd = $.getJSONObject(this.jsonString, $.id_inp.txt_hsgpcd).value;	// 配送グループコード

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$.appendMaskMsg();

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,		// レポート名
					HSGPCD:			txt_hsgpcd,
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

					// データグリッド初期化
					that.setGrid(that, reportno, $.id.grd_hstgp+'_list');

					// 状態保存
					$.saveState2(reportno, that.getJSONString());

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
			// 配送グループコード
			this.jsonTemp.push({
				id:		$.id_inp.txt_hsgpcd,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_hsgpcd),
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
		setGrid: function(that, reportno, id){		// データ表示

			var that = this;
			var init = true;
			// ページサイズ定義取得
			var pageList = $.fn.pagination.defaults.pageList;
			var pageSize = $.getJSONValue(this.jsonHidden, $.id.pageSize);

			var maxlen = 2;
			if ($.getInputboxValue($('#'+$.id.txt_areakbn))==='1') {
				maxlen = 4;
			}
			var formatterLPad = function(value){
				return $.getFormatLPad(value, maxlen);
			};

			// 選択行を保持するGridのIDを保持(ID=gridholderの場合は保持不要)
			that.scrollToId[0] = id;

			if (pageSize==="") pageSize=$.fn.pagination.defaults.pageSize;
			pageSize = $.getDefaultPageSize(pageSize, pageList);
			$('#'+id).datagrid({
				url:$.reg.easy,
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
							{field:'HSTENGPCD',	title:'店グループ'				,width: 70  ,halign:'center',align:'left',formatter:formatterLPad},
							{field:'TENGPKN',	title:'店グループ名称（漢字）'	,width: 350 ,halign:'center',align:'left'},
							{field:'TENPOSU',	title:'店舗数'					,width: 70  ,halign:'center',align:'right'},
							{field:'AREAKBN',	title:'エリア区分',	hidden:true},
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
					index = -1;
					var txt_hsgpcd = $.getInputboxValue($('#'+$.id_inp.txt_hsgpcd));
					var json = [{"callpage":"Out_Reportx203","HSGPCD":txt_hsgpcd,"FLG":"1"}];
					// 情報設定
					param.page		=	reportno;
					param.obj		=	id;
					param.sel		=	(new Date()).getTime();
					param.target	=	id;
					param.action	=	$.id.action_init;
					param.json		=	JSON.stringify(json);
					param.datatype	=	"datagrid";
					param.report = that.name;
				},
				onLoadSuccess:function(data){
					// 検索後、初回のみ処理
					if (that.queried){
						that.queried = false;	// 検索後、初回のみ処理
						// 状態保存
						$.saveState(reportno, that.getJSONString(), '#'+id);
						// 警告
						$.showWarningMessage(data);
					}

					// 前回選択情報をGridに反映
					var getRowIndex = $.getJSONValue(that.jsonHidden, "scrollToIndex_"+id);
					if(getRowIndex !== ""){
						$('#'+id).datagrid('scrollTo', {
							index: getRowIndex,
							callback: function(index){
								$('#'+id).datagrid('selectRow', index);
							}
						});
					}
				},
				onClickCell:function(rowIndex, field, value){
					// 列名保持
					that.columnName = field;
				},
				onClickRow:function(rowIndex, rowData){
					// ドリルリンク
					//that.changeReport($.id.column_class, that.columnName, rowData);
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
			$.setJSONObject(sendJSON, 'callpage', that.name, that.name);								// 呼出し元レポート情報

			// 戻る実行時用に現在の画面情報を保持する
			var states = $.getBackBaseJSON(that);
			// 各種グリッド情報を設定
			var newrepinfos = $.getBackJSON(that, states);
			$.setJSONObject(sendJSON, 'repinfo', newrepinfos, '');

			var row = $('#'+$.id.grd_hstgp+'_list').datagrid("getSelected");

			// 呼出別処理
			switch (btnId) {
			case $.id.btn_new:

				if(!row){
					$.showMessage('E00008');
					return false;
				}

				// エリア区分 = 0 or 店舗数 <> 0はエラー
				if (row.AREAKBN==="0") {

					$.showMessage('E11029');
					return false;
				}

				if (row.TENPOSU !== "0") {

					$.showMessage('E11033');
					return false;
				}

				// 転送先情報
				sendMode = "1";
				index = 5;
				childurl = href[index];

				// オブジェクト作成
				var txt_hsgpcd = $.getJSONObject(this.jsonString, $.id_inp.txt_hsgpcd).value;	// 配送グループコード
				$.setJSONObject(sendJSON, $.id_inp.txt_hsgpcd, txt_hsgpcd, txt_hsgpcd);
				$.setJSONObject(sendJSON, $.id_inp.txt_hstengpcd, row.HSTENGPCD, row.HSTENGPCD);

				break;
			case $.id.btn_sel_change:
			case $.id.btn_sel_refer:

				if(!row){
					$.showMessage('E00008');
					return false;
				}

				// エリア区分 = 0 or 店舗数 = 0はエラー
				if (row.AREAKBN==="0") {

					$.showMessage('E11029');
					return false;
				}

				if (row.TENPOSU === "0") {

					$.showMessage('E11034');
					return false;
				}

				// 転送先情報
				sendMode = "1";
				index = 5;
				childurl = href[index];

				// オブジェクト作成
				var txt_hsgpcd = $.getJSONObject(this.jsonString, $.id_inp.txt_hsgpcd).value;	// 配送グループコード
				$.setJSONObject(sendJSON, $.id_inp.txt_hsgpcd, txt_hsgpcd, txt_hsgpcd);
				$.setJSONObject(sendJSON, $.id_inp.txt_hstengpcd, row.HSTENGPCD, row.HSTENGPCD);

				break;

			case $.id.btn_back:

				// 転送先情報
				sendMode = 2;

				// 転送先情報
				if(that.reportYobiInfo()==='1'){
					index = 2;
				}else{
					index = 1;
				}

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
		getGridData: function (target){

			var targetRows= [];

			// 配送店グループ
			if(target===undefined || target===$.id.grd_hstgp+'_list'){
				var rowsHstgp= $('#'+$.id.grd_hstgp+'_list').datagrid('getRows');
				for (var i=0; i<rowsHstgp.length; i++){
					if(rowsHstgp[i]["HSTENGPCD"] == "" || rowsHstgp[i]["HSTENGPCD"] == null ){

					}else{
						var rowDate = {
								F1	 : rowsHstgp[i]["HSTENGPCD"],
								F2	 : rowsHstgp[i]["TENGPKN"],
								F3	 : rowsHstgp[i]["TENPOSU"],
							};
						targetRows.push(rowDate);
					}
				}
			}
			return targetRows;
		},
	} });
})(jQuery);