/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_ReportTG012',			// （必須）レポートオプションの確認
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
		dedefaultObjNum:	5,	// 初期化オブジェクト数
		initObjNum:	-1,
		initedObject: [],
		maxMergeCell: 1,
		onChangeFlag : false,
		onChangeFlag2 : false,
		columnName:'',	// OnClickRowの列名
		queried : false,
		editRowIndex:{},						// グリッド編集行保持
		initializes : true,
		onChangeReport: false,
		reportYobiInfo: function(){			// （必須）レポートメニューに登録された予備情報＝帳票をどういった表示にしたいかの情報
			return $('#reportYobi1').val();
		},
		returnPageInfo: function(){			// （必須）レポートメニューに登録された予備情報＝帳票をどういった表示にしたいかの情報
			return $('#returnPageInfo1').val();
		},
		pushBtnId: "",						// （必須）実行ボタンID情報(検索系で利用)
		focusRootId:"cc",					// （キー移動時必須）キー移動イベントのルートとなるパネルのID
		focusParentId:"",					// （キー移動時必須）現在フォーカスがあたっている項目の親となるパネルのID
		focusGridId:"",						// （キー移動時必須）現在フォーカスがあたっているDataGridのID
		NUMBER:"",							// index
		initialize: function (reportno){	// （必須）初期化
			var that = this;
			// 画面の初回基本設定
			this.jsonHidden = $.getTargetValue();
			this.setInitObjectState();
			// 引き継ぎ情報
			var sendBtnid = $.getJSONValue(that.jsonHidden, "sendBtnid");
			if(sendBtnid && sendBtnid.length > 0){
				that.sendBtnid = sendBtnid;
				$.reg.search = true;
				that.onChangeReport = true;
			}
			// 初期検索条件設定
			this.jsonInit = $.getInitValue();


			// 初期化するオブジェクト数設定
			this.initObjNum = this.dedefaultObjNum;

			$.setCheckboxInit2(that.jsonHidden, $.id.chk_sime1flg, that);
			$.setCheckboxInit2(that.jsonHidden, $.id.chk_sime2flg, that);
			$.setCheckboxInit2(that.jsonHidden, $.id.chk_simeflg, that);

			// 呼出コード
			var count = 2;

			// 入力テキストボックス系
			var inputbox = Object.getOwnPropertyNames($.id_inp);
			for ( var sel in inputbox ) {
				if($('#'+$.id_inp[inputbox[sel]]).length > 0){
					$.setInputbox(that, reportno, $.id_inp[inputbox[sel]], false);
					count++;
				}
			}

			// 編集エディターの機能拡張（非表示で参照用のオブジェクトが準備してある前提、参照オブジェクトはグリッドのフィールド名を元に判断）
			$.extendDatagridEditor(that);

			// データ表示エリア初期化
			that.setGrid('gridholder', reportno);

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
			var sendBtnid = $.getJSONValue(that.jsonHidden, "sendBtnid");
			$('#'+$.id.btn_sel_change).on("click", $.pushChangeReport);
			$('#'+$.id.btn_sel_change+2).on("click", $.pushChangeReport);
			$('#'+$.id.btn_new).on("click", $.pushChangeReport);
			$('#'+$.id.btn_cancel).on("click", $.pushChangeReport);
			//$('#'+$.id.btn_upd).on("click", $.pushChangeReport);
			$.initReportInfo("TG012", "特売アンケート状況　催し一覧");
			$($.id.hiddenChangedIdx).val('');

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
			var txt_qayyyymm	= $.getInputboxValue($('#'+$.id_inp.txt_qayyyymm));	// 月度
			var txt_qaend	= $.getInputboxValue($('#'+$.id_inp.txt_qaend));	// 枝番
			var chk_hnctlflg		= $("input[id="+$.id.chk_hnctlflg+"]:checked").val();


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
					report:				that.name,		// レポート名
					BTN:				btnId,
					QAYYYYMM:			txt_qayyyymm,
					QAEND	:			txt_qaend,
					HNCT	:			chk_hnctlflg,
					t:					(new Date()).getTime(),
					sortable:			sortable,
					sortName:			that.sortName,
					sortOrder:			that.sortOrder,
					rows:				0	// 表示可能レコード数
				},
				function(json){
					// 検索処理エラー判定
					if($.searchError(json)) return false;

					// ログ出力
					$.log(that.timeData, 'query:');

					// Load処理回避
					$.tryChangeURL(null);
					var limit = 1001;
					var size = JSON.parse(json)["total"];
					if(size > limit){
						$.showMessage('E00010');
					} else if(size == 0){
						$.showMessage('E11003');
					}

					if (sortable===0){
						var options = $($.id.gridholder).datagrid('options');
						// 初期検索時に並び替え情報のリセット
						options.sortName = null;
						options.sortOrder = null;
					}

					that.queried = true;
					that.pushBtnId = btnId;
					// Load処理回避
					$.tryChangeURL($.reg.jqeasy);

					// グリッド再描画（easyui 1.4.2 対応）
					$($.id.gridholder).datagrid('load', {} );
					$.removeMask();
					// 基本入力初期値保持　データを保持保持
					var Data = that.getGridData('data');
					that.setGridData(Data, 'data');
					// ログ出力
					$.log(that.timeData, 'loaded:');
				}
			);
		},
		updValidation: function (){	// （必須）批准
			var that = this;

			var row = $($.id.gridholder).datagrid("getSelected");
			var rowIndex = $($.id.gridholder).datagrid("getRowIndex", row);
			$($.id.gridholder).datagrid('endEdit',rowIndex);

			// EasyUI のフォームメソッド 'validate' 実施
			//var rt = $($.id.toolbarform).form('validate');
			var rt = $($.id.toolbarform).form('validate');
//			var targetDatasShn = that.getMergeGridDate($.id.gridholder);
//			if(targetDatasShn.length==0){
//				$.showMessage('E40006');
//				return false;
//			}
//			if(!rt){
//				$.showMessage('E00001');
//				return rt;
//			}
			return rt;
		},
		updSuccess: function(id){	// validation OK時 の update処理
			var that = this;

			// 変更行情報取得
			var targetDatas = [{}];
			$('#'+that.focusRootId).find('[col^=F]').each(function(){
				var col = $(this).attr('col');
				targetDatas[0][col] = $.getInputboxValue($(this));
			});
//			that.changeReport(that.name, 'btn_upd');
//			return false;
			// 商品一覧のデータを取得
			var targetDatasShn = that.getMergeGridDate($.id.gridholder);

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			//$.appendMaskMsg();

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,		// レポート名
					action:			$.id.action_update,	// 実行処理情報
					obj:			id,								// 実行オブジェクト
					SENDBTNID:		that.sendBtnid,
//					IDX:			$($.id.hiddenChangedIdx).val(),	// 更新対象Index
					DATA:			JSON.stringify(targetDatasShn),	// 更新対象情報
					t:				(new Date()).getTime()
				},
				function(data){
					// 検索処理エラー判定
					if($.updError(id, data)) return false;


					var afterFunc = function(){
						// 初期化
						that.clear();
						that.changeReport(that.name, $.id.btn_upd);
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
			// アンケート月度
			this.jsonTemp.push({
				id:		$.id_inp.txt_qayyyymm,
				value:	$('#'+$.id_inp.txt_qayyyymm).numberbox('getValue'),
			});

			// アンケート月度枝番
			this.jsonTemp.push({
				id:		$.id_inp.txt_qaend,
				value:	$('#'+$.id_inp.txt_qaend).numberbox('getValue'),
				text:	$('#'+$.id_inp.txt_qaend).numberbox('getText'),
			});
		},
		setObjectState: function(){	// 軸の選択内容による制御

		},
		extenxDatagridEditorIds:{
			 F8		: "chk_sime1flg"		// チェックボックス（店不採用禁止)
			,F9		: "chk_sime2flg"		// チェックボックス（店売価選択禁)
			,F10	: "chk_simeflg"		// チェックボックス（店商品選択禁止)
		},
		setGrid: function (id, reportNumber){	// グリッドの構築
			var that = this;
			var init = true;
			// ページサイズ定義取得
			var pageList = $.fn.pagination.defaults.pageList;
			var pageSize = $.getJSONValue(this.jsonHidden, $.id.pageSize);
			var cstyler =function(value,row,index){return 'color: red;font-weight: bold;';};
			if (pageSize==="") pageSize=$.fn.pagination.defaults.pageSize;
			pageSize = $.getDefaultPageSize(pageSize, pageList);
			var cformatter =function(value,row,index){return value && value===$.id.value_on?$.id.text_on:$.id.text_off;};
			that.editRowIndex[id] = -1;
			$('#'+id).datagrid({
				nowrap: true,
				border: true,
				striped: true,
				collapsible:false,
				remoteSort: true,
				rownumbers:true,
				fit:true,
				singleSelect:true,
				checkOnSelect:false,
				selectOnCheck:false,
				frozenColumns:[[]],
				columns:[[
				        {title:'　', colspan:3},
				        {title:'全', colspan:1},
				        {title:'参加回答', colspan:3},
				        {title:'　', colspan:3},
				        ],[
						{field:'F1',	title:'催しコード',			width: 95,halign:'center',align:'left'},
						{field:'F2',	title:'催し名称',			width: 220,halign:'center',align:'left'},
						{field:'F3',	title:'催し期間',			width: 180,halign:'center',align:'left'},
						{field:'F4',	title:'回答',				width: 40,halign:'center',align:'center'},
						{field:'F5',	title:'店数',				width: 40,halign:'center',align:'right'},
						{field:'F6',	title:'参加',				width: 40,halign:'center',align:'right'},
						{field:'F7',	title:'不参加',				width: 40,halign:'center',align:'right'},
						{field:'F8',	title:'リーダー仮締',		styler:cstyler, formatter:cformatter,editor:{type:'checkbox'},width: 60,halign:'center',align:'center'},
						{field:'F9',	title:'リーダー本締',		styler:cstyler, formatter:cformatter,editor:{type:'checkbox'},width: 60,halign:'center',align:'center'},
						{field:'F10',	title:'各店締',				styler:cstyler, formatter:cformatter,editor:{type:'checkbox'},width: 60,halign:'center',align:'center'}
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
					// 各グリッドの値を保持する
					var gridData = that.getGridData('#'+id);
					that.setGridData(gridData, '#'+id);

					for (var i = 0; i < data["rows"].length; i++) {
						if (data["rows"][i].F15==='1') {
							$('#'+id).datagrid('getPanel').find("[datagrid-row-index='"+i+"']").css('color', 'red');
						}
					}

					// チェックボックスの設定
					$.initCheckboxCss($('#'+id).datagrid('getPanel').find('.datagrid-body'));

					// 前回選択情報をGridに反映
					var getRowIndex = $.getJSONValue(that.jsonHidden, "scrollToIndex_"+'#'+id);
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
				onClickRow: function(index,field){$.clickEditableDatagridCell(that,id, index)},
				onBeginEdit:function(index,row){$.beginEditDatagridRow(that,id, index, row)},
				onEndEdit: function(index,row,changes){
					$.endEditDatagridRow(that, id, index, row)
					row.CHK_SEL = $.id.value_off;
				},
				onAfterEdit: function(index,row,changes){
					// チェックボックスの再追加（EndEdit時に削除されるため）
					$.afterEditAddCheckbox($('#'+id).datagrid('getPanel').find("[datagrid-row-index='"+index+"']"));
					if (row["F15"]==='1') {
						$('#'+id).datagrid('getPanel').find("[datagrid-row-index='"+index+"']").css('color', 'red');
					}
				},
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
		//ここからマージ　確認
		getGridData: function (target){
			var that = this;

			var data = {};
			var targetRows= [];

			// 商品一覧
			if(target===undefined || target===$.id.gridholder){
				var rows	 = $($.id.gridholder).datagrid('getRows');			// 商品一覧

				for (var i=0; i<rows.length; i++){
					var rowDate = {
							F1	 : rows[i]["F11"],
							F2	 : rows[i]["F12"],
							F3	 : rows[i]["F13"],
							F4	 : rows[i]["F8"],
							F5	 : rows[i]["F9"],
							F6	 : rows[i]["F10"],
							F7	 : rows[i]["F14"],

					};
					targetRows.push(rowDate);
				}
				data[$.id.gridholder] = targetRows;
			}
			return data;
		},
		getMergeGridDate: function(target){
			// 保持したデータと入力データ比較を比較する。
			var that = this;

			var newrows = that.getGridData(target)[target];		// 変更データ
			var oldrows = [];
			var targetRows= [];

			if(target===undefined || target===$.id.gridholder){
				// 催しコード一覧
				oldrows = that.gridholder
				for (var i=0; i<newrows.length; i++){
					if( newrows[i]['F2'] != oldrows[i]['F2'] || newrows[i]['F3'] != oldrows[i]['F3']
					|| newrows[i]['F4'] != oldrows[i]['F4']|| newrows[i]['F5'] != oldrows[i]['F5']
					|| newrows[i]['F6'] != oldrows[i]['F6']){
							var rowDate = {
									F1	 : newrows[i]["F1"],
									F2	 : newrows[i]["F2"],
									F3	 : newrows[i]["F3"],
									F4	 : newrows[i]["F4"],
									F5	 : newrows[i]["F5"],
									F6	 : newrows[i]["F6"],
									F7	 : newrows[i]["F7"],
							};
						if(rowDate){
							targetRows.push(rowDate);
						}
					}
				}
			}
			return targetRows;
		},
		setGridData: function (data, target){
			var that = this;

			// 商品一覧
			if(target===undefined || target===$.id.gridholder){
				that.gridholder =  data[$.id.gridholder];
			}
		},
		//ここまでマージ
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

			// タブ要素(a)取得
			var elems = $('#tabContent', window.parent.document).map(
				function(i,e) {
					return e;
				}).get();
			var href = elems[0].value.split(',');

			// JSON Object Clone ()
			var sendJSON = [];
			$.setJSONObject(sendJSON, 'sendBtnid', btnId, $('#'+btnId).attr("title"));		// 実行ボタン情報保持
			$.setJSONObject(sendJSON, 'reportYobi1', that.reportYobiInfo(), that.reportYobiInfo());		// 参照情報保持
			$.setJSONObject(sendJSON, 'returnPageInfo1', 'TG012', 'TG012');		// 参照情報保持
			$.setJSONObject(sendJSON, 'callpage', that.name, that.name);								// 呼出し元レポート情報

			// 戻る実行時用に現在の画面情報を保持する
			var states = $.getBackBaseJSON(that);
			// 各種グリッド情報を設定
			var newrepinfos = $.getBackJSON(that, states);
			$.setJSONObject(sendJSON, 'repinfo', newrepinfos, '');// 呼出し元レポート情報


			// 選択行
			var row = $($.id.gridholder).datagrid("getSelected");

			// 実行ボタン別処理
			switch (btnId) {
			case $.id.btn_new:
				// 転送先情報
				index = 4;
				childurl = href[index];
				// EasyUI のフォームメソッド 'validate' 実施
//				var rt = $($.id.toolbarform).form('validate');
//				// 入力エラーなしの場合に検索条件を格納
//				if (rt == true) that.jsonString = that.jsonTemp.slice(0);
//				// 入力チェック用の配列をクリア
//				that.jsonTemp = [];
				that.getEasyUI();
				that.validation();
				var txt_qayyyymm		= $.getJSONObject(that.jsonString, $.id_inp.txt_qayyyymm).value;	// 月度
				var txt_qaend			= $.getJSONObject(that.jsonString, $.id_inp.txt_qaend).value;		// 枝番
				// オブジェクト作成
				$.setJSONObject(sendJSON, $.id_inp.txt_qayyyymm,txt_qayyyymm, txt_qayyyymm);
				$.setJSONObject(sendJSON, $.id_inp.txt_qaend,txt_qaend, txt_qaend);
				break;
			case $.id.btn_sel_change+2:
				// 転送先情報
				index = 4;
				childurl = href[index];
				// EasyUI のフォームメソッド 'validate' 実施
//				var rt = $($.id.toolbarform).form('validate');
//				// 入力エラーなしの場合に検索条件を格納
//				if (rt == true) that.jsonString = that.jsonTemp.slice(0);
//				// 入力チェック用の配列をクリア
//				that.jsonTemp = [];
				that.getEasyUI();
				that.validation();
				var txt_qayyyymm		= $.getJSONObject(that.jsonString, $.id_inp.txt_qayyyymm).value;	// 月度
				var txt_qaend			= $.getJSONObject(that.jsonString, $.id_inp.txt_qaend).value;		// 枝番
				// オブジェクト作成
				$.setJSONObject(sendJSON, $.id_inp.txt_qayyyymm,txt_qayyyymm, txt_qayyyymm);
				$.setJSONObject(sendJSON, $.id_inp.txt_qaend,txt_qaend, txt_qaend);
				break;
			case $.id.btn_copy:
			case $.id.btn_sel_copy:
			case $.id.btn_sel_refer:
				if(!row){
					$.showMessage('E00008');
					return false;
				}

				// 転送先情報
				index = 3;
				childurl = href[index];

				// オブジェクト作成
				$.setJSONObject(sendJSON, $.id.txt_moyskbn,row.F11, row.F11);
				$.setJSONObject(sendJSON, $.id.txt_moyoosi,row.F12, row.F12);
				$.setJSONObject(sendJSON, $.id.txt_moyoosi,row.F13, row.F13);
				break;
			case $.id.btn_sel_change:
				if(!row){
					$.showMessage('E00008');
					return false;
				}

				// 転送先情報
				index = 2;
				childurl = href[index];
				var txt_qayyyymm		= $.getJSONObject(this.jsonString, $.id_inp.txt_qayyyymm).value;	// 月度
				var txt_qaend			= $.getJSONObject(this.jsonString, $.id_inp.txt_qaend).text;		// 枝番
				// オブジェクト作成
				$.setJSONObject(sendJSON, $.id_inp.txt_moyskbn,row.F11, row.F11);
				$.setJSONObject(sendJSON, $.id_inp.txt_moysstdt,row.F12, row.F12);
				$.setJSONObject(sendJSON, $.id_inp.txt_moysrban,row.F13, row.F13);
				$.setJSONObject(sendJSON, $.id_inp.txt_qayyyymm,txt_qayyyymm, txt_qayyyymm);
				$.setJSONObject(sendJSON, $.id_inp.txt_qaend,txt_qaend, txt_qaend);
				break;
			case $.id.btn_upd:
				// 転送先情報
				index = 1;
				sendMode = 1;

				childurl = href[index];
				break;
			case $.id.btn_back:
			case $.id.btn_cancel:
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
		}
	} });
})(jQuery);