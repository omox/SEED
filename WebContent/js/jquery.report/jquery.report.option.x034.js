/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_Reportx034',			// （必須）レポートオプションの確認
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
		maxMergeCell: 0,
		onChangeFlag : false,
		onChangeFlag2 : false,
		columnName:'',	// OnClickRowの列名
		queried : false,
		initializes : true,
		onChangeReport: true,
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
		updateDatas:{},						// 各画面引継ぎ更新データ一覧
		initialize: function (reportno){	// （必須）初期化
			var that = this;
			// 引き継ぎ情報
			this.jsonHidden = $.getTargetValue();

			// 引き継ぎ情報セット
			$('#'+$.id.txt_yoyaku).val($.getJSONValue(that.jsonHidden, $.id.txt_yoyaku));
			var sendBtnid = $.getJSONValue(that.jsonHidden, "sendBtnid");
			if(!sendBtnid){
				sendBtnid = $('#sendBtnid').val();
			}
			$('#sendBtnid').val(sendBtnid);
			that.sendBtnid = sendBtnid;

			// メッセージ一覧取得
			$.initMessageListData(reportno);

			// 画面の初回基本設定
			this.setInitObjectState();

			// 全画面の引継ぎ更新データを保持
			that.updateDatas[$.id_update.Reportx031] = $.getJSONValue(that.jsonHidden, $.id_update.Reportx031);
			that.updateDatas[$.id_update.Reportx032] = $.getJSONValue(that.jsonHidden, $.id_update.Reportx032);
			that.updateDatas[$.id_update.Reportx033] = $.getJSONValue(that.jsonHidden, $.id_update.Reportx033);
			that.updateDatas[$.id_update.Reportx034] = $.getJSONValue(that.jsonHidden, $.id_update.Reportx034);

			// 初期検索条件設定
			this.jsonInit = $.getInitValue();

			// 初期化するオブジェクト数設定
			this.initObjNum = this.dedefaultObjNum;

			var isUpdateReport = true;

			// 検索条件：部門コード
			$.setInputbox(that, reportno, $.id_inp.txt_bmncd, isUpdateReport);
			// 検索条件：大分類コード
			$.setInputbox(that, reportno, $.id_inp.txt_daicd, isUpdateReport);
			// 検索条件：中分類コード
			$.setInputbox(that, reportno, $.id_inp.txt_chucd, isUpdateReport);
			// 検索条件：小分類コード
			$.setInputbox(that, reportno, $.id_inp.txt_shocd, isUpdateReport);
			// 検索条件：分類区分
			$.setMeisyoCombo(that, reportno, $.id.sel_bnnruikbn, isUpdateReport)
			$("#"+$.id.sel_bnnruikbn).combobox('setValue', $.getJSONValue(that.jsonHidden, $.id.sel_bnnruikbn));

			isUpdateReport = true;
			$.setInputbox(that, reportno, "txt_f1", isUpdateReport);
			$.setInputbox(that, reportno, 'txt_f2', isUpdateReport);
			$.setInputbox(that, reportno, 'txt_f3', isUpdateReport);
			$.setInputbox(that, reportno, 'txt_f4', isUpdateReport);
			$.setInputbox(that, reportno, 'txt_f5', isUpdateReport);
			$.setInputbox(that, reportno, 'txt_f6', isUpdateReport);
			$.setInputbox(that, reportno, 'txt_f7', isUpdateReport);
			$.setInputbox(that, reportno, 'txt_f8', isUpdateReport);
			$.setInputbox(that, reportno, 'txt_f9', isUpdateReport);

			// 編集可能データグリッドの共通処理設定
			// 編集エディターの機能拡張（非表示で参照用のオブジェクトが準備してある前提、参照オブジェクトはグリッドのフィールド名を元に判断）
			$.extendDatagridEditor();

			that.setEditableGrid(that, reportno, "gridholder");

			// 検索実行
			that.onChangeReport = true;

			// 初期化終了
			this.initializes =! this.initializes;

			$.initialSearch(that);

			// チェックボックスの設定
			$.initCheckboxCss($("#"+that.focusRootId));

			// datagridでCheckbox指定時にタイトルもCheckboxになってしまう為、再度タイトル名を変更する。
			this.renameCheckBox("gridholder", "F14", "削除");

			// キーイベントの設定
			$.initKeyEvent(that);

			// ログ出力
			$.log(that.timeData, 'initialize:');
		},
		setInitObjectState: function(){	// 画面初期化時の項目制御
			var that = this;

			parent.$('#btn_back').click(function(){

				// 入力編集を終了する。
				var row = $($.id.gridholder).datagrid("getSelected");
				var rowIndex = $($.id.gridholder).datagrid("getRowIndex", row);
				$($.id.gridholder).datagrid('endEdit',rowIndex);

				// レポート番号取得
				var reportno=$($.id.hidden_reportno).val();
				// レポート定義位置
				var reportNumber = $.getReportNumber(reportno);
				that.changeReport(reportNumber, 'btn_back')

			});

			$.initReportInfo("BR014", "小小分類マスタ　一覧", "新規・変更");
			$('#'+$.id.btn_select).on("click", $.pushChangeReport);
			$('#'+$.id.btn_cancel).on("click", $.pushChangeReport);
			$.setInputBoxDisable($("#"+$.id_inp.txt_bmncd));
			$.setInputBoxDisable($("#"+$.id_inp.txt_daicd));
			$.setInputBoxDisable($("#"+$.id_inp.txt_chucd));
			$.setInputBoxDisable($("#"+$.id_inp.txt_shocd));
			$.setInputBoxDisable($("#"+$.id_inp.sel_bnnruikbn));
			// 変更
			$($.id.hiddenChangedIdx).val('');
		},
		initCondition: function (){	// 条件初期値セット
			var that = this;
			// 初期化項目
		},
		renameCheckBox: function(id,field,newname){

			// タイトル名変更
			var dgPanel = $('#'+id).datagrid('getPanel');
			var $field = $('td[field='+field+']',dgPanel);
			var $field = $('td[field='+field+']',dgPanel);
			if($field.length){
				var $span = $('span', $field).eq(0);
				$span.html(newname);
			}

			// タイトル部のチェック判定無効化
			$('#'+id).datagrid('getPanel').find('div.datagrid-header input[type=checkbox]').attr('disabled','disabled');

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
			if (rt == true) that.jsonString = that.jsonTemp.slice(0);
			// 入力チェック用の配列をクリア
			that.jsonTemp = [];
			return rt;
		},
		success: function(reportno, sortable){	// （必須）正処理
			if (sortable) sortable=1; else sortable=0;
			var that = this;
			// 検索実行
			var szBumon		= $.getJSONObject(this.jsonString, $.id_inp.txt_bmncd).value;			// 部門コード
			var szDaicd		= $.getJSONObject(this.jsonString, $.id_inp.txt_daicd).value;			// 大分類コード
			var szChucd		= $.getJSONObject(this.jsonString, $.id_inp.txt_chucd).value;			// 中分類コード
			var szShocd		= $.getJSONObject(this.jsonString, $.id_inp.txt_shocd).value;			// 小分類コード

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			$($.id.gridholder).datagrid('loading');

			// grid.options 取得
			var options = $($.id.gridholder).datagrid('options');
			that.sortName	= options.sortName;
			that.sortOrder	= options.sortOrder;

			// Loading表示
			//$.appendMaskMsg();

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,		// レポート名
					BUMON:			szBumon,		// 部門コード
					DAICD:			szDaicd,		// 大分類コード
					CHUCD:			szChucd,		// 中分類コード
					SHOCD:			szShocd,		// 小分類コード
					t:				(new Date()).getTime(),
					sortable:		sortable,
					sortName:		that.sortName,
					sortOrder:		that.sortOrder,
					rows:			10000			// 表示可能レコード数
				},
				function(json){
					// 検索処理エラー判定
					if($.searchError(json)) return false;

					// ログ出力
					$.log(that.timeData, 'query:');

					var opts = JSON.parse(json).opts

					// Load処理回避
					//$.tryChangeURL(null);


					// 検索結果を保持
					that.gridData = JSON.parse(json).rows;
					that.gridTitle = JSON.parse(json).titles;
					if(opts && opts.rows_y){
						that,yoyakuData = opts.rows_y;
					}

					// メインデータ表示
					that.setData(that.gridData, opts);
					that.queried = true;

					if (sortable===0){
						var options = $($.id.gridholder).datagrid('options');
						// 初期検索時に並び替え情報のリセット
						options.sortName = null;
						options.sortOrder = null;
					}

					that.queried = true;
					// Load処理回避
					$.tryChangeURL($.reg.jqeasy);

					// グリッド再描画（easyui 1.4.2 対応）
					//$($.id.gridholder).datagrid('load', {} );
					$($.id.gridholder).datagrid('reload');
					$.removeMask();

					// 状態保存
					$.saveState2(reportno, that.getJSONString());

					// ログ出力
					$.log(that.timeData, 'loaded:');
				}
			);
		},
		updValidation: function (id){	// （必須）批准

			// 入力編集を終了する。
			var row = $($.id.gridholder).datagrid("getSelected");
			var rowIndex = $($.id.gridholder).datagrid("getRowIndex", row);
			$($.id.gridholder).datagrid('endEdit',rowIndex);

			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform).form('validate');
			if(!rt){
				$.showMessage('E00001');
				return rt;
			}

			// 入力値チェック
			if(!this.InputCheck()){
				return false;
			}


			return rt;
		},
		updSuccess: function(id){	// validation OK時 の update処理
			var that = this;

			// 最新の引継ぎ更新データ一覧を保持
			this.setUpdateDatas(that);

			var szBunrui	= $.getJSONObject(this.jsonString, $.id.sel_bnnruikbn).value;			// 検索分類区分
			var szBumon		= $.getJSONObject(this.jsonString, $.id_inp.txt_bmncd).value;			// 部門コード
			var szDaicd		= $.getJSONObject(this.jsonString, $.id_inp.txt_daicd).value;			// 大分類コード
			var szChucd		= $.getJSONObject(this.jsonString, $.id_inp.txt_chucd).value;			// 中分類コード
			var szShocd		= $.getJSONObject(this.jsonString, $.id_inp.txt_shocd).value;			// 小分類コード

			// 変更行情報取得
			//var changedIndex = $($.id.hiddenChangedIdx).val().split(",");
			// Grid内全情報取得
			var rows = $($.id.gridholder).datagrid('getRows');
			// 対象情報抜粋
			var targetRows = [];
			targetRows = this.setUpdateData(that.gridData,rows);

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			//$.appendMaskMsg();

			$.post(
					$.reg.jqgrid ,
					{
						report:			'Out_Reportx031',					// レポート名
						action:			$.id.action_update,					// 実行処理情報
						obj:			id,									// 実行オブジェクト
						BUNRUI:			szBunrui,							// 検索分類区分
						BUMON:			szBumon,							// 検索部門コード
						DAICD:			szDaicd,							// 検索大分類コード
						CHUCD:			szChucd,							// 検索中分類コード
						SHOCD:			szShocd,							// 検索小分類コード
						DATA:			JSON.stringify(that.updateDatas),	// 更新対象情報
						t:				(new Date()).getTime()
					},
					function(data){
						// 検索処理エラー判定
						if($.updError(id, data)) return false;


						var afterFunc = function(){
							// 初期化
							that.clear();
							$.setInputBoxDisable($($.id.hiddenChangedIdx));
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

			// 検索部門コード
			this.jsonTemp.push({
				id:		$.id_inp.txt_bmncd,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_bmncd),
				text:	''
			});
			// 検索大分類コード
			this.jsonTemp.push({
				id:		$.id_inp.txt_daicd,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_daicd),
				text:	''
			});
			// 検索中分類コード
			this.jsonTemp.push({
				id:		$.id_inp.txt_chucd,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_chucd),
				text:	''
			});
			// 検索小分類コード
			this.jsonTemp.push({
				id:		$.id_inp.txt_shocd,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_shocd),
				text:	''
			});
			// 検索分類区分
			this.jsonTemp.push({
				id:		$.id.sel_bnnruikbn,
				value:	$.getJSONValue(this.jsonHidden, $.id.sel_bnnruikbn),
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
		setUpdateData: function(oldrows, newrows){		// 更新データを保持
			// TODO
			// Grid内全情報取得
			var ColumnsLength = 14;
			var updFlg = false
			// 対象情報抜粋
			var targetRows = [];
			for (var i=0; i<newrows.length; i++){
				for (var j=0; j<ColumnsLength; j++){
					// 変更前データと変更後データで比較を行う
					if(newrows[i]["F"+(j+1)]!=oldrows[i]["F"+(j+1)]){
						updFlg = true;
					}
				}

				if(updFlg){
					var rowData = {
							idx	 : i+1,
							F1	 : newrows[i]["F1"],
							F2	 : newrows[i]["F2"],
							F3	 : newrows[i]["F3"],
							F4	 : newrows[i]["F4"],
							F5	 : newrows[i]["F5"],
							F6	 : newrows[i]["F6"],
							F7	 : newrows[i]["F7"],
							F8	 : newrows[i]["F8"],
							F9	 : newrows[i]["F9"],
							F10	 : newrows[i]["F10"],
							F11	 : newrows[i]["F11"],
							F12	 : newrows[i]["F12"],
							F13	 : newrows[i]["F13"],
							F14	 : newrows[i]["F14"],
							F15	 : newrows[i]["F15"],
						};
					targetRows.push(rowData);
					updFlg = false;
					}
			}
			return targetRows;
		},
		setUpdateDatas: function(that){		// 更新データ一覧を保持

			var name = that.name;
			var rows = $($.id.gridholder).datagrid('getRows');
			var enpryRows = [];
			var thisrows = [];
			thisrows = this.setUpdateData(that.gridData,rows);

			// 現在の画面での入力更新データを保持
			that.updateDatas["targetRows_"+name.replace("Out_Report", "")] = thisrows;

			// データ無しの場合、空のデータを設定する。
			for(var i=0; i< 4; i++){
				var rows = []
				rows = that.updateDatas["targetRows_x03"+(i+1)]
				if(rows.length > 0){

				}else{
					that.updateDatas["targetRows_x03"+(i+1)] = enpryRows
				}
			}
		},
		setUpdateDatasCridIn: function(that, id){		// 保持されて遷移されてきた更新データ一覧をグリッドに設定
			var rows = [];
			rows = $.getJSONValue(that.jsonHidden, "targetRows_"+that.name.replace("Out_Report", ""));

			if(rows.length > 0){
				for (var i=0; i<rows.length; i++){

					// データグリッドを更新
					$('#'+id).datagrid('updateRow',{
						index: Number(rows[i]['idx']) - 1,
						row: rows[i],
					})

					// チェックボックスの再描画
					$.afterEditAddCheckbox($('#'+id).datagrid('getPanel').find("[datagrid-row-index='"+rows[i]['idx']+"']"));

					if(rows[i]['F14']=='1'){
						$('#'+id).datagrid('checkRow', rows[i]['idx']);
					}
				}
				$($.id.hiddenChangedIdx).val("1")
			}
		},
		InputCheck :function(){
			var updflg = true;
			var dbunrui = [], dbunruikeys = [];
			var targetRowsDbunrui = $($.id.gridholder).datagrid('getRows');
			for (var i=0; i<targetRowsDbunrui.length; i++){
				dbunrui.push(targetRowsDbunrui[i]["F1"]);

				//dbunruikeys.push(targetRowsDbunrui[i]["F1"]);
			}
			//alert(dbunrui.push(targetRowsDbunrui[24]["F1"]));
			// 画面に同じ大分類がある場合、エラー。
			var dbunrui_ = dbunrui.filter(function (element, index, self) { return self.indexOf(element)*1 === index; });
			if(dbunrui.length !== dbunrui_.length){
				alert($.getMessage('E11112'));
				updflg = false;
			}

			return updflg
		},
		setEditableGrid: function(that, reportno, id){		// データ表示
			var that = this;
			var init = true;
			var pageList = $.fn.pagination.defaults.pageList;
			var pageSize = $.getJSONValue(this.jsonHidden, $.id.pageSize);
			var targetId = 'txt_f1';
			var check = $('#'+targetId).attr("check") ? JSON.parse('{'+$('#'+targetId).attr("check")+'}'): JSON.parse('{}');		// 大分類コードのcheck要素を取得
			var formatterLPad = function(value){
				return $.getFormatLPad(value, check.maxlen);
			};
			var parserLPad= function(value){
				return $.getParserLPad(value);
			};
			that.editRowIndex[id] = -1;
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
				frozenColumns:[[]],
				columns:[[
				          	{field:'F14',		title:'削除',					checkbox:true,	width:  50,halign:'center',align:'center'},
				          	{field:'F1',		title:'小小分類コード', 		width:  90,halign:'center',align:'right',formatter:formatterLPad,editor:{type:'numberbox'}},
							{field:'F3',		title:'小小分類名（漢字）',		width: 130,halign:'center',align:'left',editor:{type:'textbox',options:{editable:true,disabled:false,readonly:false}}},
							{field:'F2',		title:'小小分類名（カナ）',		width: 130,halign:'center',align:'left',editor:{type:'textbox',options:{editable:true,disabled:false,readonly:false}}},
							{field:'F4',		title:'属性1',					width:  50,halign:'center',align:'left',editor:{type:'numberbox'}},
							{field:'F5',		title:'属性2',					width:  50,halign:'center',align:'left',editor:{type:'numberbox'}},
							{field:'F6',		title:'属性3',					width:  50,halign:'center',align:'left',editor:{type:'numberbox'}},
							{field:'F7',		title:'属性4',					width:  50,halign:'center',align:'left',editor:{type:'numberbox'}},
							{field:'F8',		title:'属性5',					width:  50,halign:'center',align:'left',editor:{type:'numberbox'}},
							{field:'F9',		title:'属性6',					width:  50,halign:'center',align:'left',editor:{type:'numberbox'}},
							{field:'F10',		title:'登録日',					width:  80,halign:'center',align:'left'},
							{field:'F11',		title:'更新日',					width:  80,halign:'center',align:'left'},
							{field:'F12',		title:'オペレーター',			width: 100,halign:'center',align:'left'},
							{field:'F13',		title:'新規登録区分(非表示)',	width: 100,halign:'center',align:'left',hidden:true},
							]],
				url:$.reg.easy,
				onBeforeLoad:function(param){},
				onLoadSuccess:function(data){
					// チェックボックスの設定
					$.initCheckboxCss($('#'+id).datagrid('getPanel').find('.datagrid-body'));
					// 引継ぎ更新データが存在する場合は設定
					that.setUpdateDatasCridIn(that, id)
				},

				/*toolbar:[{
					text:'追加',
					iconCls:'icon-add',
					handler: function(){$.appendDatagridRow(that, id)}
				}],*/
				onSelect:function(index){
					var rows = $('#'+id).datagrid('getRows');
					var col = $('#'+id).datagrid('getColumnOption', 'F1');
					if(rows[index]["F13"]=='1'){
						col.editor = {
						type:'numberbox',
						options:{cls:'labelInput',editable:true,disabled:false,readonly:false}
						}
					}else{
						col.editor = false
					}
				},
				onCheckAll:function(index){
					$($.id.hiddenChangedIdx).val("1")
					var Checkedlows = $('#'+id).datagrid('getChecked');
					for (var i=0; i<Checkedlows.length; i++){
						if($('#'+id).datagrid('getPanel').find("[datagrid-row-index='"+i+"']").find(":checkbox").prop("checked")){
							Checkedlows[i]["F14"]='1'
						}else{
							Checkedlows[i]["F14"]=''
						}
					}
				},
				onCheck:function(index){
					$($.id.hiddenChangedIdx).val("1");
					// TODO 暫定
					var rows = $('#'+id).datagrid('getRows');
					if($('#'+id).datagrid('getPanel').find("[datagrid-row-index='"+index+"']").find(":checkbox").prop("checked")){
						rows[index]["F14"]='1'
					}else{
						rows[index]["F14"]=''
					}
				},
				onClickRow: function(index,field){$.clickEditableDatagridCell(that,id, index)},
				onBeginEdit:function(index,row){$.beginEditDatagridRow(that,id, index, row)},
				onEndEdit: function(index,row,changes){$.endEditDatagridRow(that, id, index, row)},
				onAfterEdit: function(index,row,changes){
					// チェックボックスの再追加（EndEdit時に削除されるため）
					$.afterEditAddCheckbox($('#'+id).datagrid('getPanel').find("[datagrid-row-index='"+index+"']"));
				}
			});
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
			var sendMode = "";

			// タブ要素(a)取得
			var elems = $('#tabContent', window.parent.document).map(
				function(i,e) {
					return e;
				}).get();
			var href = elems[0].value.split(',');

			// JSON Object Clone ()
			var sendJSON = [];
			$.setJSONObject(sendJSON, 'sendBtnid', btnId, $('#'+btnId).attr("title"));		// 実行ボタン情報保持

			// 選択行
			var row = $($.id.gridholder).datagrid("getSelected");

			// 実行ボタン別処理
			switch (btnId) {
			case $.id.btn_upd:
				var enptyRows = []

				index = 4;
				childurl = href[index];

				$.setJSONObject(sendJSON, $.id_update.Reportx031, enptyRows,enptyRows);			// データクリア：大分類マスタ
				$.setJSONObject(sendJSON, $.id_update.Reportx032, enptyRows,enptyRows);			// データクリア：中分類マスタ
				$.setJSONObject(sendJSON, $.id_update.Reportx033, enptyRows,enptyRows);			// データクリア：小分類マスタ
				$.setJSONObject(sendJSON, $.id_update.Reportx034, enptyRows,enptyRows);			// データクリア：小小分類マスタ

				break;
			case $.id.btn_cancel:
			case $.id.btn_back:
				// 転送先情報
				index = 3;
				childurl = href[index];
				break;

			/*case 'btn_back':

				var beroIdx	= $.getJSONObject(this.jsonHidden, 'beforIndex').value;				// 分類区分
				index = beroIdx;
				childurl = href[index];

				var szBunrui	= $.getJSONObject(this.jsonString, $.id.sel_bnnruikbn).value;				// 分類区分
				var szBumon		= $.getJSONObject(this.jsonString, $.id_inp.txt_bmncd).value;				// 検索部門コード
				var szDaicd		= $.getJSONObject(this.jsonString, $.id_inp.txt_daicd).value;				// 検索大分類コード
				var szChucd		= $.getJSONObject(this.jsonString, $.id_inp.txt_chucd).value;				// 検索中分類コード
				var szShocd		= $.getJSONObject(this.jsonString, $.id_inp.txt_shocd).value;				// 検索中分類コード

				// オブジェクト作成
				$.setJSONObject(sendJSON, $.id.sel_bnnruikbn, szBunrui, szBunrui);							// 検索条件：分類区分
				$.setJSONObject(sendJSON, $.id_inp.txt_bmncd, szBumon, szBumon);							// 検索条件：部門コード
				$.setJSONObject(sendJSON, $.id_inp.txt_daicd, szDaicd, szDaicd);							// 検索条件：大分類コード
				$.setJSONObject(sendJSON, $.id_inp.txt_chucd, szChucd, szChucd);							// 検索条件：中分類コード
				$.setJSONObject(sendJSON, $.id_inp.txt_shocd, szShocd, szShocd);							// 検索条件：小分類コード
				$.setJSONObject(sendJSON, "beforIndex", 4, 4);									// 遷移前のindex
				break;*/
			default:
				break;
			}

			// 引継ぎ用更新データ
			if(btnId=='btn_back' ){

				var targetRowsx031 = [];
				var targetRowsx032 = [];
				var targetRowsx033 = [];
				var targetRowsx034 = [];

				// 最新の引継ぎ更新データ一覧を保持
				this.setUpdateDatas(that);

				targetRowsx031 = that.updateDatas[$.id_update.Reportx031];
				targetRowsx032 = that.updateDatas[$.id_update.Reportx032];
				targetRowsx033 = that.updateDatas[$.id_update.Reportx033];
				targetRowsx034 = that.updateDatas[$.id_update.Reportx034];

				$.setJSONObject(sendJSON, $.id_update.Reportx031, targetRowsx031,targetRowsx031);			// 引継ぎ更新データ：大分類マスタ
				$.setJSONObject(sendJSON, $.id_update.Reportx032, targetRowsx032,targetRowsx032);			// 引継ぎ更新データ：中分類マスタ
				$.setJSONObject(sendJSON, $.id_update.Reportx033, targetRowsx033,targetRowsx033);			// 引継ぎ更新データ：小分類マスタ
				$.setJSONObject(sendJSON, $.id_update.Reportx034, targetRowsx034,targetRowsx034);			// 引継ぎ更新データ：小小分類マスタ
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
		}
	} });
})(jQuery);