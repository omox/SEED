/**
 * jquery sub window option
 */
;(function($) {

$.extend({

	winTJ006: {
		name: 'Out_ReportwinTJ006',
		prefix:'_kouseihi',
		suffix:'_winTJ006',
		initializes: true,		// 初期化フラグ（全体）
		initializesCond: true,	// 初期化フラグ（条件用）
		initializesGrid: true,
		initedObject: [],
		lastIndex: -1,			// 編集位置（行）
		sortName: '',			// ソート項目名
		sortOrder: '',			// ソート順
		timeData : (new Date()).getTime(),
		queried : false,
		grdData:[],				// グリッド情報
		grdDataDT:[],			// 日付情報
		callreportno:"",		// 呼出し元レポートNo
		callBtnid: "",			// 呼出ボタンID情報
		focusRootId:"_winTJ006",// （キー移動時必須）キー移動イベントのルートとなるパネルのID
		focusParentId:"",		// （キー移動時必須）現在フォーカスがあたっている項目の親となるパネルのID
		focusGridId:"",			// （キー移動時必須）現在フォーカスがあたっているDataGridのID
		inpTenAddArr:"",
		inpTenDelArr:"",
		szLstno:"",
		szBmncd:"",
		editRowIndex:{},		// グリッド編集行保持
		columnName:'',			// OnClickRowの列名
		jsBack:"",
		weekStartPos:"",
		searched : false,
		init: function(js) {
			var that = this;
			that.jsBack = js;
			if(!that.initializes) return false;

			that.callreportno = js.name;

			$.setInputbox(that, that.callreportno, 'txt_bmnysanam1', true);
			$.setInputbox(that, that.callreportno, 'txt_bmnysanam2', true);
			$.setInputbox(that, that.callreportno, 'txt_bmnysanam3', true);
			$.setInputbox(that, that.callreportno, 'txt_bmnysanam4', true);
			$.setInputbox(that, that.callreportno, 'txt_bmnysanam5', true);
			$.setInputbox(that, that.callreportno, 'txt_bmnysanam6', true);
			$.setInputbox(that, that.callreportno, 'txt_bmnysanam7', true);
			$.setInputbox(that, that.callreportno, 'txt_bmnysanam8', true);
			$.setInputbox(that, that.callreportno, 'txt_bmnysanam9', true);
			$.setInputbox(that, that.callreportno, 'txt_bmnysanam10', true);
			$.setInputbox(that, that.callreportno, 'txt_kouseihi1', true);
			$.setInputbox(that, that.callreportno, 'txt_kouseihi2', true);
			$.setInputbox(that, that.callreportno, 'txt_kouseihi3', true);
			$.setInputbox(that, that.callreportno, 'txt_kouseihi4', true);
			$.setInputbox(that, that.callreportno, 'txt_kouseihi5', true);
			$.setInputbox(that, that.callreportno, 'txt_kouseihi6', true);
			$.setInputbox(that, that.callreportno, 'txt_kouseihi7', true);
			$.setInputbox(that, that.callreportno, 'txt_kouseihi8', true);
			$.setInputbox(that, that.callreportno, 'txt_kouseihi9', true);
			$.setInputbox(that, that.callreportno, 'txt_kouseihi10', true);

			// 呼出しボタンイベント設定
			$('[id^=btn'+that.prefix+']').each(function(){
				var id = $(this).attr('id');
				$('#'+id).click(function() {
					// 部門、リスト№を取得
					for (var i = 0; i < js.jsonHidden.length; i++) {
						if (js.jsonHidden[i].id===$.id.SelBumon) {
							that.szBmncd = js.jsonHidden[i].value;
						} else if (js.jsonHidden[i].id===$.id_inp.txt_lstno) {
							that.szLstno = js.jsonHidden[i].value;
						}
					}

					if (that.Open(this)) {

						that.Search();

						// window 表示
						$('#'+that.suffix).window('open');
					}
				});
			});

			// 登録
			$('#'+$.id.btn_upd+that.suffix).on("click", that.Update);
			// キャンセル
			$('#'+$.id.btn_cancel+that.suffix).on("click", that.Cancel);

			// ｳｲﾝﾄﾞｳ設定
			$('#'+that.suffix).window({
				iconCls:'icon-search',
				modal:true,
				collapsible:false,
				minimizable:false,
				maximizable:false,
				closed:true,
				cinline:false,
				zIndex:90000,
				onBeforeOpen:function(){
					// ウインドウ展開中リサイズイベント無効化
					$.reg.resize = false;
					js.focusParentId = that.suffix;
				},
				onOpen:function(){
				},
				onBeforeClose:function(){
					// ウインドウ展開中リサイズイベント有効化
					$.reg.resize = true;

					if($.getConfirmUnregistFlg($($.id.hiddenChangedIdx+that.suffix))){
						var func_ok = function(r){
							$($.id.hiddenChangedIdx+that.suffix).val("");	// 変更行Index
							$('#'+that.suffix).window('close');
							return true;
						};
						$.showMessage("E11025", undefined, func_ok);
						return false;
					}else{
						that.Clear();										// 変更内容初期化
					}

					js.focusParentId = js.focusRootId;
				},
				onClose:function(){
					$.extendDatagridEditor(that.jsBack);
					$('#'+js.focusParentId).find('#'+that.callBtnid).focus();
				}
			});

			// チェックボックスの設定
			$.initCheckboxCss($("#"+that.focusRootId));
			// キーイベントの設定
			$.initKeyEvent(that);

			that.initializes = !that.initializes;
		},
		Open: function(obj) {
			if ($(obj).is("disabled","disabled"))	return false;
			var that = $.winTJ006;
			that.callBtnid = $(obj).attr('id');

			// 呼出し元別処理
			switch (that.callreportno) {
			case 'Out_ReportTJ005':

				// オブジェクト作成
				$('#'+that.suffix).window({title: '特売販売計画＆事前発注 大分類構成比(TJ006)'});

				break;
			default:
				break;
			}
			return true;
		},
		Clear:function(){
			var that = $.winTJ006;
			that.initializesCond = false;
			// グリッド初期化
			$('#grd_subwindow'+that.prefix+that.suffix).datagrid('loadData',[]);
			that.initializesCond = true;
		},
		Search: function(){
			var that = $.winTJ006;

			// validate=falseの場合何もしない
			if(!that.validation()){ return false; }

			// セッションタイムアウト、利用時間外の確認
			var isTimeout = $.checkIsTimeout();
			if (! isTimeout) {
				// 検索実行
				that.success('grd_subwindow'+that.prefix+that.suffix);
			}

			return true;
		},
		success: function(id){	// 検索処理
			var that = $.winTJ006;
			// 処理時間計測用
			that.timeData = (new Date()).getTime();

			// 情報設定
			var json = [{}];

			// 呼出し元別処理
			switch (that.callreportno) {
			case 'Out_ReportTJ005':
				// オブジェクト作成
				json = [{
					callpage:	$($.id.hidden_reportno).val(),									// 呼出元レポート名
					BMNCD:	that.szBmncd,
					LSTNO:	that.szLstno,
				}];
				break;
			default:
				break;
			}

			// グリッド初期化&ローディング
			$('#'+id).datagrid('loading');

			$.post(
				$.reg.easy,
				{
					page	:	that.name,										// レポート名
					obj		:	id,
					sel		:	(new Date()).getTime(),
					target	:	id,
					action	:	$.id.action_init,
					json	:	JSON.stringify(json),
				},
				function(data){
					// ログ出力
					$.log(that.timeData, 'query:');
					if(data!==""){
						// JSONに変換
						var json = JSON.parse(data);

						// メインデータ表示
						that.setData(json.rows);
						if (that.initializesGrid) {
							// dataGrid 初期化
							that.setEditableGrid('grd_subwindow'+that.prefix+that.suffix);
							that.initializesGrid = false;
						} else {
							// グリッド初期化&ローディング
							$('#grd_subwindow'+that.prefix+that.suffix).datagrid('load');
						}

					}
					$('#'+id).datagrid('loaded');

					$.extendDatagridEditor(that);

					that.queried = true;
					// 隠し情報初期化
					$($.id.hiddenChangedIdx+that.suffix).val("");

					// 初期データを保持
					that.grdData = $('#'+id).datagrid('getRows');

					that.searched = true;

					// ログ出力
					$.log(that.timeData, 'loaded:');
				}
			);
		},
		Cancel:function(){
			var that = $.winTJ006;
			$('#'+that.suffix).window('close');
			return true;
		},
		validation: function (){	// （必須）批准
			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform+that.suffix).form('validate');
			return rt;
		},
		extenxDatagridEditorIds:{
			F2		: "txt_kouseihi1",
			F3		: "txt_kouseihi2",
			F4		: "txt_kouseihi3",
			F5		: "txt_kouseihi4",
			F6		: "txt_kouseihi5",
			F7		: "txt_kouseihi6",
			F8		: "txt_kouseihi7",
			F9		: "txt_kouseihi8",
			F10		: "txt_kouseihi9",
			F11		: "txt_kouseihi10",
		},
		setEditableGrid: function(id) {
			var that = this;

			var url = $.fn.datagrid.defaults.url;
			var formatter = function(value,row,index){if(!value){value=0.0;} return $.getFormat(value, '##0.0') + '%';};

			var columns = [];
			columns.push([
			    {field:'F1',	title:'',	width:98,halign:'center',align:'left'},
				{field:'F2',	title:'',	width:79,halign:'center',align:'right',editor:{type:'numberbox'},formatter:formatter},
			    {field:'F3',	title:'',	width:79,halign:'center',align:'right',editor:{type:'numberbox'},formatter:formatter},
				{field:'F4',	title:'',	width:79,halign:'center',align:'right',editor:{type:'numberbox'},formatter:formatter},
				{field:'F5',	title:'',	width:79,halign:'center',align:'right',editor:{type:'numberbox'},formatter:formatter},
				{field:'F6',	title:'',	width:79,halign:'center',align:'right',editor:{type:'numberbox'},formatter:formatter},
				{field:'F7',	title:'',	width:79,halign:'center',align:'right',editor:{type:'numberbox'},formatter:formatter},
				{field:'F8',	title:'',	width:79,halign:'center',align:'right',editor:{type:'numberbox'},formatter:formatter},
				{field:'F9',	title:'',	width:79,halign:'center',align:'right',editor:{type:'numberbox'},formatter:formatter},
				{field:'F10',	title:'',	width:79,halign:'center',align:'right',editor:{type:'numberbox'},formatter:formatter},
				{field:'F11',	title:'',	width:79,halign:'center',align:'right',editor:{type:'numberbox'},formatter:formatter},
				{field:'F12',	title:'',	width:79,halign:'center',align:'right'},
			]);

			switch (that.callreportno) {
			case 'Out_ReportTJ005':
				if (id==='grd_subwindow'+that.prefix+that.suffix) {
					// 呼出し元別処理
					url = $.reg.easy;
					var funcBeforeLoad = function(param){

						var json = [{
							"callpage":"Out_ReportTJ006",
							"LSTNO":that.szLstno,
							"BMNCD":that.szBmncd}
						];
						// 情報設定
						param.page		=	that.name;
						param.obj		=	'grd_subwindow'+that.prefix+'_main'+that.suffix;
						param.sel		=	(new Date()).getTime();
						param.target	=	id;
						param.action	=	$.id.action_init;
						param.json		=	JSON.stringify(json);
						param.datatype	=	"datagrid";
					};
				}
				break;
			default:
				break;
			}

			that.editRowIndex[id] = -1;
			$('#'+id).datagrid({
				url:url,
				nowrap: true,
				border: true,
				striped: true,
				collapsible:false,
				remoteSort: true,
				rownumbers:false,
				fit:true,
				frozenColumns:[[]],
				columns:columns,
				fitColumns:false,	// 指定カラム幅を適用する場合、false 指定。
				onBeforeLoad:funcBeforeLoad,
				rowStyler:function(index, row){
				},
				onLoadSuccess:function(data){
					// 各グリッドの値を保持する
					var gridData = that.getGridData('#'+id);
					that.setGridData(gridData, '#'+id);

					var rows = data.rows;
					if(that.searched == true){
						that.searched = false;

						for (var i=0; i<rows.length; i++){
							var row = {};
							var update = false;
							for(var j = 1; j <= 13; j++){
								var col = "F"+ j;
								var val = rows[i][col];
								if (col==='F12' || !$.isEmptyVal(val)) {
									row[col] = rows[i][col];
								} else {
									row[col] = 0.0;
									update = true;
								}
							}
							if (update) {
								$('#'+id).datagrid('updateRow', {
									index:i,
									row:row
								});
								$($.id.hiddenChangedIdx+that.suffix).val("1");
							}
						}
						that.setCellColor();
					}
				},
				onSelect:function(index){
				},
				onBeforeEdit:function(index,row){
				},
				onClickCell:function(rowIndex, field, value){
					// 列名保持
					that.columnName = field;
				},
				onClickRow: function(index,field){	$.clickEditableDatagridCell(that,id, index)},
				onBeginEdit:function(index,row){$.beginEditDatagridRow(that,id, index, row)},
				onEndEdit: function(index,row,changes){$.endEditDatagridRow(that, id, index, row)},
				onAfterEdit: function(index,row,changes){
					that.setCellColor();
				},
				autoRowHeight:false,
				pagination:false,
				pagePosition:'bottom',
				singleSelect:true,
				showHeader:false
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
		setData: function(rows, opts){		// データ表示
			var that = this;
			var msg = $('#'+that.focusRootId).find('[col^=MSG]');
			$.setInputboxValue($(msg), "");
			if(rows.length > 0){
				$('#'+that.focusRootId).find('[col^=F]').each(function(){
					var col = $(this).attr('col');
					if(rows[0][col]){
						$.setInputboxValue($(this), rows[0][col]);
						var v = $.getInputboxValue($(this));
						if(that.weekStartPos == "" && v == "月"){
							that.weekStartPos = col.substr(2);
						}
					}
				});

				// 日曜又は月曜からの開始
				if(Number(that.weekStartPos) < 3){
					$('#'+that.focusRootId).find('[id^=txt_kouseihi]').each(function(){
						var id = $(this).attr('id');
						var idNum = Number($(this).attr('id').substr(12));
						if(idNum < Number(that.weekStartPos) || idNum > Number(that.weekStartPos)+6){
							// 月曜から一週間分以外を編集不可に設定
							//$.setInputBoxDisable($('#'+id));
						}
					});
					$.setInputboxValue($(msg), "網がけの項目の値が各曜日の規定値として設定されます");
				}

				// 日付情報保持
				that.grdDataDT = [];
				for(var i=1; i<11; i++){
					that.grdDataDT.push(rows[0]["X"+i]);
				}
			}
		},
		getGridData: function (target){
			var that = this;

			var data = {};
			var targetRows= [];
			var rows	 = $(target).datagrid('getRows');

			for (var i=0; i<rows.length; i++){
				var rowDate = {
						F1	 : rows[i]["F1"], // 大分類名称
						F2	 : rows[i]["F2"], // 構成比
						F3	 : rows[i]["F3"], // 構成比
						F4	 : rows[i]["F4"], // 構成比
						F5	 : rows[i]["F5"], // 構成比
						F6	 : rows[i]["F6"], // 構成比
						F7	 : rows[i]["F7"], // 構成比
						F8	 : rows[i]["F8"], // 構成比
						F9	 : rows[i]["F9"], // 構成比
						F10	 : rows[i]["F10"], // 構成比
						F11	 : rows[i]["F11"], // 構成比
						F12	 : rows[i]["F12"], // 平均パック単価
						F13	 : rows[i]["F13"], // 大分類コード
				};
				targetRows.push(rowDate);
			}
			data[target] = targetRows;
			return data;
		},
		setGridData: function (data, target){
			var that = this;
			that.grdData =  data[target];
		},
		changeInputboxFunc:function(that, id, newValue, obj){
		},
		Update: function (id){
			var that = $.winTJ006;
			var grdid = "grd_subwindow"+that.prefix+that.suffix;

			// validate=falseの場合何もしない
			if(!that.updValidation(grdid)){ return false; }

			// 変更情報チェック
			if(!$.getConfirmUnregistFlg($($.id.hiddenChangedIdx+that.suffix))){
				$.showMessage('E20582', undefined, function(){$.addErrState(that, $('#'+$.id.btn_upd+that.suffix), false)});
				return false;
			}

			// 登録データの取得
			var targetData = that.getUpdateData();
			var targetDataDef = that.getUpdateDataDef();
			var targetDataBmnYsanAm = that.getUpdateDataBmnYsanAm();

			// 処理時間計測用
			that.timeData = (new Date()).getTime();

			var func_ok = function(r){
				// セッションタイムアウト、利用時間外の確認
				var isTimeout = $.checkIsTimeout();
				if(!isTimeout){
					$.post(
						$.reg.jqgrid ,
						{
							report:		that.name,						// レポート名
							action:		$.id.action_update,				// 実行処理情報
							obj:		grdid,							// 実行オブジェクト
							DATA:		JSON.stringify(targetData),		// 更新対象情報
							DATADEF:	JSON.stringify(targetDataDef),	// 更新対象情報（デフォルト）
							DATABMN:	JSON.stringify(targetDataBmnYsanAm),
							BMNCD:		that.szBmncd,					// 部門コード
							LSTNO:		that.szLstno,					// リストNo
							t:			(new Date()).getTime()
						},
						function(data){
							// 更新処理エラー判定
							if($.updError(id, data)) return false;

							var afterFunc = function(){
								// 検索実行
								that.success('grd_subwindow'+that.prefix+that.suffix);
							};
							$.updNormal(data, afterFunc);

							// マスク削除
							$.removeMaskMsg();

							// ログ出力
							$.log(that.timeData, 'loaded:');
						}
					);
				}
			}
			$.showMessage("W00001", undefined, func_ok);
		},
		updValidation: function (id){
			var that = $.winTJ006;

			// 入力編集を終了する。
			var row = $('#'+id).datagrid("getSelected");
			var rowIndex = $('#'+id).datagrid("getRowIndex", row);
			$('#'+id).datagrid('endEdit',rowIndex);

			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $('#'+that.suffix).form('validate');
			if(!rt){
				$.addErrState(that, $('.validatebox-invalid').eq(0), false);
				return rt;
			}

			return true;
		},
		getUpdateData: function(){
			// 保持したデータと入力データ比較を比較する。
			var that = this;
			var grdid = "grd_subwindow"+that.prefix+that.suffix;
			var targetRows= [];

			// 入力データ取得
			var newrows = $('#'+grdid).datagrid('getRows');

			for (var i=0; i<newrows.length; i++){
				for(var j=1; j< 11; j++){
					// 更新有り
					if(that.grdData[i]["F"+(j+1)] !== newrows[i]["F"+(j+1)]){
						var rowData = {
								DAICD	 : newrows[i]["F13"],		// 大分類コード
								TJDT	 : that.grdDataDT[j-1],		// 年月日
								URICMPRT : newrows[i]["F"+(j+1)],	// 売上構成比
						};
						targetRows.push(rowData);
					}
				}
			}
			return targetRows;
		},
		getUpdateDataDef: function(){
			// 保持したデータと入力データ比較を比較する。
			var that = this;
			var grdid = "grd_subwindow"+that.prefix+that.suffix;
			var targetRows= [];
			var pos = Number(that.weekStartPos);

			// 日曜又は月曜からの開始
			if(pos < 3){
				// 入力データ取得
				var newrows = $('#'+grdid).datagrid('getRows');

				for (var i=0; i<newrows.length; i++){
					// 更新有り
					if(that.grdData[i]["F"+(pos+1)] !== newrows[i]["F"+(pos+1)] ||
							that.grdData[i]["F"+(pos+2)] !== newrows[i]["F"+(pos+2)] ||
							that.grdData[i]["F"+(pos+3)] !== newrows[i]["F"+(pos+3)] ||
							that.grdData[i]["F"+(pos+4)] !== newrows[i]["F"+(pos+4)] ||
							that.grdData[i]["F"+(pos+5)] !== newrows[i]["F"+(pos+5)] ||
							that.grdData[i]["F"+(pos+6)] !== newrows[i]["F"+(pos+6)] ||
							that.grdData[i]["F"+(pos+7)] !== newrows[i]["F"+(pos+7)]){
						var rowData = {
								DAICD	     : newrows[i]["F13"],		// 大分類コード
								URICMPRT_MON : newrows[i]["F"+(pos+1)],	// 売上構成比_月
								URICMPRT_TUE : newrows[i]["F"+(pos+2)],	// 売上構成比_火
								URICMPRT_WED : newrows[i]["F"+(pos+3)],	// 売上構成比_水
								URICMPRT_THU : newrows[i]["F"+(pos+4)],	// 売上構成比_木
								URICMPRT_FRI : newrows[i]["F"+(pos+5)],	// 売上構成比_金
								URICMPRT_SAT : newrows[i]["F"+(pos+6)],	// 売上構成比_土
								URICMPRT_SUN : newrows[i]["F"+(pos+7)],	// 売上構成比_日
						};
						targetRows.push(rowData);
					}
				}
			}
			return targetRows;
		},
		getUpdateDataBmnYsanAm: function(){
			// 保持したデータと入力データ比較を比較する。
			var that = this;
			var targetRows= [];

			for (var i = 1; i <= 10; i++) {

				var val = $.getInputboxValue($('#txt_bmnysanam'+i));
				var rowData = {
						BMNYSANAM	: val,						// 部門予算
						TJDT		: that.grdDataDT[i-1],		// 年月日
				};
				targetRows.push(rowData);
			}
			return targetRows;
		},
		setCellColor: function(){
			var that = this;
			var pos = Number(that.weekStartPos);

			// 日曜又は月曜からの開始
			if(pos < 3){
				var rows = $('#'+that.focusRootId).find('.datagrid-view2').find('.datagrid-btable')[0].rows;
				for(var i = 0; i < rows.length; i++){
					var cells = rows[i].cells;
					for(var j=pos; j< pos+7; j++){
						cells[j].style["backgroundColor"] = "#38b2ff";
					}
				}
			}
		}
	}
});

})(jQuery);