/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_ReportTM003',			// （必須）レポートオプションの確認
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
			$.setInputbox(that, reportno, $.id_inp.txt_moysstdt);
			// 催し区分
			that.setEditCombo(that, reportno, $.id_mei.kbn10002, true);

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
		setInitObjectState: function(){	// 画面初期化時の項目制御
			var that = this;
			// 引き継ぎ情報セット
			var sendBtnid = $.getJSONValue(that.jsonHidden, "sendBtnid");
			if(sendBtnid && sendBtnid.length > 0){
				that.sendBtnid = sendBtnid;
				$.reg.search = true;
			}

			// 当帳票を「参照」で開いた場合
			if(that.reportYobiInfo()==='1'){	// 現在参照無し
				$.setInputBoxDisable($("#"+$.id.btn_new)).hide();
				$.setInputBoxDisable($("#"+$.id.btn_sel_change)).hide();

				$.initReportInfo("TM003", "催しコード　その他催し　一覧　参照");
			}else{
				// 各種ボタン
				$('#'+$.id.btn_new).on("click", $.pushChangeReport);
				$('#'+$.id.btn_sel_change).on("click", $.pushChangeReport);
				$('#'+$.id.btn_sel_refer).on("click", $.pushChangeReport);
				$.initReportInfo("TM003", "催しコード　その他催し　一覧");
			}
			$($.id.buttons).show();
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
			if (rt == true) that.jsonString = that.jsonTemp.slice(0);
			// 入力チェック用の配列をクリア
			that.jsonTemp = [];
			return rt;
		},
		success: function(reportno, sortable, btnId){	// （必須）正処理
			if (sortable) sortable=1; else sortable=0;
			var that = this;
			// 検索実行
			var szMoysstdt	= $.getJSONObject(this.jsonString, $.id_inp.txt_moysstdt).value;	// 催しコード（催し開始日）
			var szMoyskbn	= $.getJSONObject(this.jsonString, $.id_mei.kbn10002).value;		// 催し区分
			var dtMoyskbn	= $.getJSONObject(this.jsonString, $.id_mei.kbn10002+'DATA').value;	// 催し区分のDATA
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
					MOYSSTDT:		szMoysstdt,		// 催しコード（催し開始日）
					MOYSKBN:		szMoyskbn,		// 催し区分
					MOYSKBN_DATA:	JSON.stringify(dtMoyskbn),		// 催し区分のDATA
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
					if(size == 0){
						$.showMessage('E11003');
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

					// グリッド再描画（easyui 1.4.2 対応）
					$($.id.gridholder).datagrid('load', {} );
					$.removeMask();

/*					// 検索ボタン無効化
					$.setButtonState('#'+$.id.btn_search, false, 'success');
*/
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

			// 催しコード（催し開始日）
			this.jsonTemp.push({
				id:		$.id_inp.txt_moysstdt,
				value:	$('#'+$.id_inp.txt_moysstdt).textbox('getValue'),
				text:	$('#'+$.id_inp.txt_moysstdt).textbox('getText')
			});
			// 催し区分
			 this.jsonTemp.push({
				id:		$.id_mei.kbn10002,
				value:	$('#'+$.id_mei.kbn10002).combobox('getValue'),
				text:	$('#'+$.id_mei.kbn10002).combobox('getText')
			});

			// 催し区分(DATA)
			var dtMoykbn = $('#'+$.id_mei.kbn10002).combobox('getData');
			var dataMoykbn = [];
			for(var i=0;i<dtMoykbn.length;i++){
				if(dtMoykbn[i].VALUE!=='-1'){
					dataMoykbn.push(dtMoykbn[i].VALUE);
				}
			}
			this.jsonTemp.push({
				id:		$.id_mei.kbn10002+'DATA',
				value:	dataMoykbn,
				text:	'全催し区分情報'
			});
		},
		setObjectState: function(){	// 軸の選択内容による制御

		},
		setEditCombo: function(that, reportno, id, topBlank){
			var idx = -1;

			$('#'+id).combobox({
				 url:$.reg.easy,
				//loader: myloader,
				required: false,
				editable: true,
				autoRowHeight:false,
				panelHeight:220,
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
						var val = null;
						var init = $('#'+id).combobox('getValue');
						var data = $('#'+id).combobox('getData');
						for (var i=0; i<data.length; i++){
							if(val === null){
								val = data[i].VALUE;
							}
							if (data[i].VALUE == init){
								val = data[i].VALUE;
								break;
							}
						}
						$('#'+id).combobox('setValue', val);
						$('#'+id).combobox('hidePanel');
						e.preventDefault();
					},
					query: $.fn.combobox.defaults.keyHandler.query
				},
				onBeforeLoad:function(param){
					idx=-1;

					// 情報設定
					var json = [{}];
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
					var init = null;
					if (that.initedObject && $.inArray(id, that.initedObject) < 0){
						that.initedObject.push(id);
						init = $.getJSONValue(that.jsonHidden, id);
					}
					var val = null;
					for (var i=0; i<data.length; i++){
						if(val === null && data[i].VALUE !== -1){
							val = data[i].VALUE;
						}
						if (data[i].VALUE == init){
							val = data[i].VALUE;
							break;
						}
					}
					if(val){
						$('#'+id).combobox('setValue', val);
					}
					// 初期化
					idx = 1;
					// ログ出力
					$.log(that.timeData, id+' init:');
					// 初期表示検索処理
					$.initialSearch(that);
				},
				onChange:function(newValue, oldValue){
					if(idx > 0){ $.removeErrState(); }
					if(idx > 0 && that.queried){
						$($.id.hiddenChangedIdx).val("1");
					}
				}
			});
			idx = 1;
		},
		setGrid: function (id, reportNumber){	// グリッドの構築
			var that = this;
			var init = true;
			// ページサイズ定義取得
			var pageList = $.fn.pagination.defaults.pageList;
			var pageSize = $.getJSONValue(this.jsonHidden, $.id.pageSize);
			if (pageSize==="") pageSize=$.fn.pagination.defaults.pageSize;
			pageSize = $.getDefaultPageSize(pageSize, pageList);

			var cstyler =function(value,row,index){return 'color: red;font-weight: bold;';};
			var cformatter =function(value,row,index){return value && value===$.id.value_on?$.id.text_on:$.id.text_off;};


			$(id).datagrid({
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
				frozenColumns:[[]],
				columns:[[
					{field:'F1',	title:'催しコード',			width:100,halign:'center',align:'left'},
					{field:'F2',	title:'年末区分',			width: 40,halign:'center',align:'center',	formatter:cformatter,	styler:cstyler},
					{field:'F3',	title:'販売期間',			width:180,halign:'center',align:'left'},
					{field:'F4',	title:'納入期間',			width:180,halign:'center',align:'left'},
					{field:'F5',	title:'PLU配信日',			width: 85,halign:'center',align:'left'},
					{field:'F6',	title:'催し名称（漢字）',	width:300,halign:'center',align:'left'},
					{field:'F7',	title:'催し名称（カナ）',	width:200,halign:'center',align:'left'}
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

					// 前回選択情報をGridに反映
					var getRowIndex = $.getJSONValue(that.jsonHidden, "scrollToIndex_"+id);
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
			var sendJSON = [];
			$.setJSONObject(sendJSON, 'sendBtnid', btnId, $('#'+btnId).attr("title"));		// 実行ボタン情報保持

			// 戻る実行時用に現在の画面情報を保持する
			var states = $.getBackBaseJSON(that);
			var newrepinfos = $.getBackJSON(that, states, true);
			$.setJSONObject(sendJSON, 'repinfo', newrepinfos, '');

			// 選択行
			var row = $($.id.gridholder).datagrid("getSelected");

			// 実行ボタン別処理
			switch (btnId) {
			case $.id.btn_new:
				// 転送先情報
				index = 4;
				childurl = href[index];
				sendMode = 1;
				break;
			case $.id.btn_sel_change:
			case $.id.btn_sel_refer:
				if(!row){
					$.showMessage('E00008');
					return false;
				}
				if([0,1,4].indexOf(row.F8*1)!==-1){
					$.showMessage("E20043");
					return false;
				}else if([2].indexOf(row.F8*1)!==-1&&row.F4.length > 0){
					$.showMessage("E20044");
					return false;
				}

				// 転送先情報
				index = 4;
				childurl = href[index];
				sendMode = 1;
				// オブジェクト作成
				$.setJSONObject(sendJSON, $.id_inp.txt_moyskbn,  row.F8, row.F8);	// 催し区分
				$.setJSONObject(sendJSON, $.id_inp.txt_moysstdt, row.F9, row.F9);	// 催し開始日
				$.setJSONObject(sendJSON, $.id_inp.txt_moysrban, row.F10, row.F10);	// 催し連番
				break;
			case $.id.btn_back:
				// 転送先情報
				childurl = parent.$('#hdn_menu_path').val();

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
		getInputboxParams: function(that, id, newValue){
			// 情報取得
			var values = {};
			values["value"] = newValue;
			// 情報設定
			return [values];
		}
	} });
})(jQuery);