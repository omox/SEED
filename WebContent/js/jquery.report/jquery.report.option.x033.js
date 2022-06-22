/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_Reportx033',			// （必須）レポートオプションの確認
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
			that.updateDatas[$.id_update.Reportx031] = $.getJSONValue(that.jsonHidden, $.id_update.Reportx031) === '' ? []:$.getJSONValue(that.jsonHidden, $.id_update.Reportx031);
			that.updateDatas[$.id_update.Reportx032] = $.getJSONValue(that.jsonHidden, $.id_update.Reportx032) === '' ? []:$.getJSONValue(that.jsonHidden, $.id_update.Reportx032);
			that.updateDatas[$.id_update.Reportx033] = $.getJSONValue(that.jsonHidden, $.id_update.Reportx033) === '' ? []:$.getJSONValue(that.jsonHidden, $.id_update.Reportx033);
			//that.updateDatas[$.id_update.Reportx034] = $.getJSONValue(that.jsonHidden, $.id_update.Reportx034) === '' ? []:$.getJSONValue(that.jsonHidden, $.id_update.Reportx034);

			// メンテナンス用アラート(各画面で引き継いでいるデータ数を表示)
			//alert("x031:"+that.updateDatas[$.id_update.Reportx031].length + " \nx032:" + that.updateDatas[$.id_update.Reportx032].length + " \nx033:"+that.updateDatas[$.id_update.Reportx033].length);

			if(that.updateDatas[$.id_update.Reportx031].length > 0
			|| that.updateDatas[$.id_update.Reportx032].length > 0
			|| that.updateDatas[$.id_update.Reportx033].length > 0){
				$($.id.hiddenChangedIdx).val("1")
			}

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

			//$.initialSearch(that);

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

			$.initReportInfo("BR013", "小分類マスタ　一覧", "新規・変更");
			//$('#'+$.id.btn_select).on("click", $.pushChangeReport);
			$('#'+$.id.btn_cancel).on("click", $.pushChangeReport);
			$.setInputBoxDisable($("#"+$.id_inp.sel_bnnruikbn));
			$.setInputBoxDisable($("#"+$.id_inp.txt_bmncd));
			$.setInputBoxDisable($("#"+$.id_inp.txt_daicd));
			$.setInputBoxDisable($("#"+$.id_inp.txt_chucd));
			$.setInputBoxDisable($("#"+$.id_inp.txt_shocd));

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
				$span.removeClass("chk_dummy");
				$('.datagrid-header-check').css('fontSize', '10px');

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
			var szBunrui	= $.getJSONObject(this.jsonHidden, $.id.sel_bnnruikbn).value;			// 分類区分
			var szBumon		= $.getJSONObject(this.jsonString, $.id_inp.txt_bmncd).value;			// 部門コード
			var szDaicd		= $.getJSONObject(this.jsonString, $.id_inp.txt_daicd).value;			// 大分類コード
			var szChucd		= $.getJSONObject(this.jsonString, $.id_inp.txt_chucd).value;			// 中分類コード

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			$.removeMask();
			$.removeMaskMsg();
			$($.id.gridholder).datagrid('loading');

			// grid.options 取得
			var options = $($.id.gridholder).datagrid('options');
			that.sortName	= options.sortName;
			that.sortOrder	= options.sortOrder;

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,		// レポート名
					BUNRUI:			szBunrui,		// 分類区分
					BUMON:			szBumon,		// 部門コード
					DAICD:			szDaicd,		// 大分類コード
					CHUCD: 			szChucd,		// 中分類コード
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
					$.removeMask();
					$.removeMaskMsg();
					$($.id.gridholder).datagrid('reload');

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
				$.addErrState(that, $('.validatebox-invalid').eq(0), false);
				return rt;
			}

			// 新規登録時の入力チェック
			var targetRowsx031 = [];
			var targetRowsx032 = [];
			var targetRowsx033 = [];

			this.setUpdateDatas(that);
			targetRowsx031 = $.getJSONValue(that.jsonHidden, $.id_update.Reportx031);
			targetRowsx032 = $.getJSONValue(that.jsonHidden, $.id_update.Reportx032);
			targetRowsx033 = that.updateDatas[$.id_update.Reportx033];
			var emsg = this.checkNewInsert3(targetRowsx031, targetRowsx032, targetRowsx033);
			if(emsg.length > 0){
				$.showMessage(emsg);
				return false;
			}

			// 入力値チェック
			if(!this.InputCheck()){
				return false;
			}

			/*// 入力エラーなしの場合に検索条件を格納
			if (rt == true) that.jsonString = that.jsonTemp.slice(0);
			// 入力チェック用の配列をクリア
			that.jsonTemp = [];*/
			return rt;

		},
		updSuccess: function(id){	// validation OK時 の update処理
			var that = this;

			// 最新の引継ぎ更新データ一覧を保持
			this.setUpdateDatas(that);

			// 親子データ削除による不要更新親子データを取り除く
			this.setCheckInfOtherDatasSho()

			var szBunrui	= $.getJSONObject(this.jsonHidden, $.id.sel_bnnruikbn).value;			// 検索分類区分
			var szBumon		= $.getJSONObject(this.jsonString, $.id_inp.txt_bmncd).value;			// 部門コード
			var szDaicd		= $.getJSONObject(this.jsonString, $.id_inp.txt_daicd).value;			// 大分類コード
			var szChucd		= $.getJSONObject(this.jsonString, $.id_inp.txt_chucd).value;			// 中分類コード
			var szShocd		= "";																	// 小分類コード

			for (var i = 0; i < that.jsonHidden.length; i++) {
				if (that.jsonHidden[i].id === $.id_inp.txt_shocd) {
					szShocd		= $.getJSONObject(this.jsonHidden, $.id_inp.txt_shocd).value;

				}
			}

			// 変更行情報取得
			var changedIndex = $($.id.hiddenChangedIdx).val().split(",");
			// Grid内全情報取得
			var rows = $($.id.gridholder).datagrid('getRows');
			// 対象情報抜粋
			var targetRows = [];
			targetRows = this.setUpdateData(that.gridData,rows);

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$.appendMask();

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

			var szBumon		= $.getJSONObject(this.jsonString, $.id_inp.txt_bmncd).value;			// 部門
			var szDaicd		= $.getJSONObject(this.jsonString, $.id_inp.txt_daicd).value;			// 大分類コード
			var szChucd		= $.getJSONObject(this.jsonString, $.id_inp.txt_chucd).value;			// 中分類コード
			var szBunrui	= $.getJSONObject(this.jsonString, $.id.sel_bnnruikbn).value;			// 分類区分

			// 対象情報抜粋
			var targetRows = [];
			for (var i=0; i<newrows.length; i++){
				for (var j=0; j<ColumnsLength; j++){
					// 変更前データと変更後データで比較を行う
					if(newrows[i]["F"+(j+1)]!=oldrows[i]["F"+(j+1)]){
						updFlg = true;
					}
				}

				if(newrows[i]["F13"] == '1'){
					// 新規行データ
					if(newrows[i]["F14"] == '1'){
						// 削除チェック有の場合は送らない
						//updFlg = false;
					}
				}

				if(updFlg && (newrows[i]["F1"] ? newrows[i]["F1"] : '') != ''){
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

							F16	 : szBunrui,
							F17	 : szBumon,
							F18	 : szDaicd,
							F19	 : szChucd,
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
			var allupdaterows = []				// この画面で入力を行って保持してきた更新データ
			allupdaterows = that.updateDatas["targetRows_"+name.replace("Out_Report", "")]

			var szBumon		= $.getJSONObject(this.jsonString, $.id_inp.txt_bmncd).value;			// 部門
			var szDaicd		= $.getJSONObject(this.jsonString, $.id_inp.txt_daicd).value;			// 大分類コード
			var szChucd		= $.getJSONObject(this.jsonString, $.id_inp.txt_chucd).value;			// 中分類コード
			var szBunrui	= $.getJSONObject(this.jsonString, $.id.sel_bnnruikbn).value;			// 分類区分

			if(allupdaterows.length > 0){
				// 引継ぎデータが存在する場合

				var allupdaterows2 = [];			// この画面とは異なる検索条件で保持されてきたデータ

				for (var i=0; i<allupdaterows.length; i++){

					if(rows.length > 0){
						// 現在のデータと比較する。
						if(allupdaterows[i]['F16'] === szBunrui
						&& allupdaterows[i]['F17'] === szBumon
						&& allupdaterows[i]['F18'] === szDaicd
						&& allupdaterows[i]['F19'] === szChucd){

						}else{
						// ユニークキー(検索条件値)が一致しない項目(違う検索条件で検索された項目)を保持する。
						// ※ユニークキーが同じ項目(本画面入力データ)は下記処理にて追加される為不要。
							allupdaterows2.push(allupdaterows[i])
						}
					}
				}
				// 不要箇所を削除したデータに置き換える。
				that.updateDatas["targetRows_"+name.replace("Out_Report", "")] = allupdaterows2;
			}

			if(that.updateDatas["targetRows_"+name.replace("Out_Report", "")] === ''){
				// 引継ぎデータ無しの場合['']が入ってしまう為、置き換える。
				that.updateDatas["targetRows_"+name.replace("Out_Report", "")] = [];

			}

			if(thisrows.length > 0){
				that.setUpdateDataThis(thisrows);
			}

			// データ無しの場合、空のデータを設定する。
			for(var i=0; i< 3; i++){
				var rows = []
				rows = that.updateDatas["targetRows_x03"+(i+1)]
				if(rows.length > 0){

				}else{
					that.updateDatas["targetRows_x03"+(i+1)] = enpryRows
				}
			}
		},
		setCheckInfOtherDatasSho: function(){
			// 親データを削除チェックして子データの変更を行っていた場合や、子データをすべて削除チェックして親データの変更を行っていた場合
			// 関連する親子データの削除により、変更した箇所も削除されるため意味のない変更となる。
			// その為、変更された子データを取り除いたデータを登録に使用する。
			var that = this;

			var targetRowsx031 = [];
			var targetRowsx032 = [];
			var targetRowsx033 = [];

			targetRowsx031 = that.updateDatas[$.id_update.Reportx031];
			targetRowsx032 = that.updateDatas[$.id_update.Reportx032];
			targetRowsx033 = that.updateDatas[$.id_update.Reportx033];

			var targetRowsx033_Del = [];

			var allDelete = false				// 全件削除フラグ

			var targetRowsx031_Af = [];			// 大分類登録データ(親削除データを除く)
			var targetRowsx032_Af = [];			// 中分類登録データ(親削除データを除く)

			for (var i=0; i< targetRowsx033.length; i++){
				if(targetRowsx033[i]['F14'] == '1'){
					// 削除データ有の場合
					targetRowsx033_Del.push(targetRowsx033[i])
				}
			}

			var rows = $($.id.gridholder).datagrid('getRows');
			var dataLength = 0;
			for (var i=0; i< rows.length; i++){
				if(rows[i]["F1"] && rows[i]["F1"] !== ''){
					dataLength += 1;
				}
			}
			if(dataLength == targetRowsx033_Del.length){
				// 表示件数 = 削除件数の場合
				allDelete = true
			}

			// 中分類データ抽出
			var countFlg = false
			var count = 0;
			for (var j=0; j< targetRowsx032.length; j++){

				var bunrui	 = targetRowsx032[j]['F16']
				var bmncd	 = targetRowsx032[j]['F17']
				var daicd	 = targetRowsx032[j]['F18']
				var chucd	 = targetRowsx032[j]['F1']

				countFlg = false;
				if(allDelete){
					var newLines = targetRowsx033_Del.filter(function(item, index){
						if((item.F16).indexOf(bunrui) >= 0
								&&(item.F17).indexOf(bmncd) >= 0
								&&(item.F18).indexOf(daicd) >= 0
								&&(item.F19).indexOf(chucd) >= 0

						){
								// 中分類データの中に、削除小分類データに紐ずくデータがある場合
							countFlg = true;
						}
					});
				}
				if(!countFlg){
					targetRowsx032_Af.push(targetRowsx032[j])
				}else{
					// 削除予定の中分類コードをカウント
					count += 1;
				}
			}

			// 大分類データ抽出
			var countFlg = false
			var chk_cnt = 0;

			if(targetRowsx033_Del.length > 0){
				var bunrui	 = targetRowsx033_Del[0]['F16']
				var bmncd	 = targetRowsx033_Del[0]['F17']
				var daicd	 = targetRowsx033_Del[0]['F18']
				var param = {};
				param["KEY"] =  "MST_CNT";
				param["value"] = bmncd + ',' + daicd + ',' + bunrui;
				chk_cnt = $.getInputboxData(that.name, $.id.action_check, $.id_inp.txt_chucd, [param]);		// 既存中分類データ
			}


			for (var j=0; j< targetRowsx031.length; j++){

				var bunrui	 = targetRowsx031[j]['F16']
				var bmncd	 = targetRowsx031[j]['F17']
				var daicd	 = targetRowsx031[j]['F1']

				countFlg = false;

				if(allDelete && Number(chk_cnt) == count){
					// 同じ親を持つ中分類 = 削除予定の中分類の場合

					if(targetRowsx033_Del.length > 0){
						var newLines = targetRowsx033_Del.filter(function(item, index){
							if((item.F16).indexOf(bunrui) >= 0
									&&(item.F17).indexOf(bmncd) >= 0
									&&(item.F18).indexOf(daicd) >= 0){
									// 中分類データの中に、削除大分類データに紐ずくデータがある場合
								countFlg = true;
							}
						});
					}
				}
				if(!countFlg){
					targetRowsx031_Af.push(targetRowsx031[j])
				}
			}

			// 削除予定の更新データ(親が削除される子データ)を取り除いたデータを設定
			that.updateDatas[$.id_update.Reportx031] = targetRowsx031_Af;
			that.updateDatas[$.id_update.Reportx032] = targetRowsx032_Af;
		},
		setUpdateDataThis: function(targetdata){
			var that = this;
			var thisdatasAf = [];

			for (var i=0; i< that.updateDatas[$.id_update.Reportx033].length; i++){
				thisdatasAf.push(that.updateDatas[$.id_update.Reportx033][i])
			}

			for (var i=0; i< targetdata.length; i++){
				thisdatasAf.push(targetdata[i]);
			}
			that.updateDatas[$.id_update.Reportx033] = thisdatasAf;
		},
		setUpdateDatasCridIn: function(that, id){		// 保持されて遷移されてきた更新データ一覧をグリッドに設定
			var rows = [];
			rows = $.getJSONValue(that.jsonHidden, "targetRows_"+that.name.replace("Out_Report", ""));

			var szBumon		= $.getJSONObject(this.jsonString, $.id_inp.txt_bmncd).value;			// 部門
			var szDaicd		= $.getJSONObject(this.jsonString, $.id_inp.txt_daicd).value;			// 大分類コード
			var szChucd		= $.getJSONObject(this.jsonString, $.id_inp.txt_chucd).value;			// 中分類コード
			var szBunrui	= $.getJSONObject(this.jsonString, $.id.sel_bnnruikbn).value;			// 分類区分

			if(rows.length > 0){
				for (var i=0; i<rows.length; i++){

					if(rows[i]['F16'] === szBunrui && rows[i]['F17'] === szBumon && rows[i]['F18'] === szDaicd && rows[i]['F19'] === szChucd){
						// データグリッドを更新
						$('#'+id).datagrid('updateRow',{
							index: Number(rows[i]['idx']) - 1,
							row: rows[i],
						})

						// チェックボックスの再描画
						$.afterEditAddCheckbox($('#'+id).datagrid('getPanel').find("[datagrid-row-index='"+(rows[i]['idx'] - 1)+"']"));

						if(rows[i]['F14']=='1'){
							$('#'+id).datagrid('checkRow', (rows[i]['idx'] - 1));
						}
					}
				}
				$($.id.hiddenChangedIdx).val("1")
			}
		},
		checkNewInsert3: function(dairows, churows, shorows){		// 新規データ
			// TODO
			// Grid内全情報取得
			var ColumnsLength = 14;
			var emsg = "";
			var updFlgDai = false;
			var updFlgChu = false;
			var updFlgSho = false;
			var newcount = 0;
			// 対象情報抜粋
			var targetRows = [];

			var errFlg = false;
			var newdairows = [];	// 大分類新規行
			var newchurows = [];	// 中分類新規行
			var newshorows = [];	// 小分類新規行

			var target1 = '';
			var target2 = '';
			var target3 = '';
			var target4 = '';


			// 新規大分類データ
			for (var i=0; i<dairows.length; i++){
				if(dairows[i]["F13"] == '1'){
					if(dairows[i]["F1"]){
						newdairows.push(dairows[i]);
					}
				}
			}

			// 新規中分類データ
			for (var i=0; i<churows.length; i++){
				if(churows[i]["F13"] == '1'){
					if(churows[i]["F1"]){
						newchurows.push(churows[i]);
					}
				}
			}

			// 新規小分類データ
			for (var i=0; i<shorows.length; i++){
				if(shorows[i]["F13"] == '1'){
					if(shorows[i]["F1"]){
						newshorows.push(shorows[i]);

						if(shorows[i]["F14"] == '1'){
							// 新規行に削除チェックがある場合
							errFlg = true
						}
					}
				}
			}

			if(newdairows.length > 0){
				// 大分類
				var count = 0;
				var countFlg = false
				for (var i=0; i<newdairows.length; i++){
					target1 = newdairows[i]['F16'];			// 分類区分
					target2 = newdairows[i]['F17'];			// 部門コード
					target3 = newdairows[i]['F1'];			// 大分類コード
					countFlg = false;

					var newLines = newchurows.filter(function(item, index){
						if((item.F16).indexOf(target1) >= 0
								&&(item.F17).indexOf(target2) >= 0
								&&(item.F18).indexOf(target3) >= 0){
								// 中分類データの中に、小分類データに紐ずくデータがある場合
							countFlg = true;
						}
					});
					if(countFlg){
						count += 1
					}
				}
				if(count !== newdairows.length){
					errFlg = true;
				}
			}

			if(newchurows.length > 0){
				// 中分類
				var count = 0;
				var countFlg = false
				for (var i=0; i<newchurows.length; i++){
					target1 = newchurows[i]['F16'];			// 分類区分
					target2 = newchurows[i]['F17'];			// 部門コード
					target3 = newchurows[i]['F18'];			// 大分類コード
					target4 = newchurows[i]['F1'];			// 中分類コード
					countFlg = false;

					if(newshorows.length > 0){
						var newLines = newshorows.filter(function(item, index){
							if((item.F16).indexOf(target1) >= 0
									&&(item.F17).indexOf(target2) >= 0
									&&(item.F18).indexOf(target3) >= 0
									&&(item.F19).indexOf(target4) >= 0){
									// 中分類データの中に、小分類データに紐ずくデータがある場合
								countFlg = true;
							}
						});
						if(countFlg){
							count += 1
						}
					}else{
						errFlg = true;
					}
				}
				if(count !== newchurows.length){
					errFlg = true;
				}
			}

			if(errFlg){
				emsg = "EX1028";
			}
			return emsg;
		},
		checkNewInsert2: function(dairows, churows, shorows){		// 新規データ
			// TODO
			// Grid内全情報取得
			var emsg = "";
			var errFlg = false;
			var newdairows = [];	// 大分類新規行
			var newchurows = [];	// 中分類新規行
			var newshorows = [];	// 小分類新規行
			var newchurows2 = [];	// 中分類新規行(親が既存行の場合)
			var newshorows2 = [];	// 小分類新規行(親が既存行の場合)

			var target1 = '';
			var target2 = '';
			var target3 = '';
			var target4 = '';

			// 新規大分類データ
			for (var i=0; i<dairows.length; i++){
				if(dairows[i]["F13"] == '1'){
					if(dairows[i]["F1"]){
						newdairows.push(dairows[i]);
					}
				}
			}

			// 新規中分類データ
			for (var i=0; i<churows.length; i++){
				if(churows[i]["F13"] == '1'){
					if(churows[i]["F1"]){

						newchurows2.push(churows[i]);	// 一時的に新規データをすべて保持

						var newLines = newdairows.filter(function(item, index){
							if((item.F16).indexOf(churows[i]['F16']) >= 0
								&&(item.F17).indexOf(churows[i]['F17']) >= 0
								&&(item.F1).indexOf(churows[i]['F18']) >= 0){

								// 新規登録データに親データが存在するデータのみを保持
								newchurows.push(churows[i]);
								newchurows2.pop();	// 親が既存データではない情報の為、追加したデータを削除する。
								}
						});
					}
				}
			}

			// 新規小分類データ
			for (var i=0; i<shorows.length; i++){
				if(shorows[i]["F13"] == '1'){
					if(shorows[i]["F1"]){

						newshorows2.push(churows[i]);	// 一時的に新規データをすべて保持

						var newLines = newchurows.filter(function(item, index){
							if((item.F16).indexOf(shorows[i]['F16']) >= 0
								&&(item.F17).indexOf(shorows[i]['F17']) >= 0
								&&(item.F18).indexOf(shorows[i]['F18']) >= 0
								&&(item.F1).indexOf(shorows[i]['F19']) >= 0){

								// 新規登録データに親データが存在するデータのみを保持
								newshorows.push(shorows[i]);
								newshorows2.pop();	// 親が既存データではない情報の為、追加したデータを削除する。
								}
						});
					}
				}
			}

			if(newdairows.length > 0 && newchurows.length > 0 && newshorows.length > 0){
				// 3画面で新規行が入力さてている場合。

				var target1 = '';
				var target2 = '';
				var target3 = '';
				var target4 = '';

				// 大・中紐付きチェック
				for (var i=0; i<newdairows.length; i++){
					target1 = newdairows[i]['F16'];			// 分類区分
					target2 = newdairows[i]['F17'];			// 部門コード
					target3 = newdairows[i]['F1'];			// 大分類コード

					var newLines = newchurows.filter(function(item, index){
						if((item.F16).indexOf(target1) >= 0
								&&(item.F17).indexOf(target2) >= 0
								&&(item.F18).indexOf(target3) >= 0){
								// 中分類データの中に、小分類データに紐ずくデータがある場合は処理なし。
							}else{
								// 紐ずくデータがない場合
								errFlg = true;
							}
					});
				}

				// 中・小紐付きチェック
				for (var i=0; i<newchurows.length; i++){
					target1 = newchurows[i]['F16'];			// 分類区分
					target2 = newchurows[i]['F17'];			// 部門コード
					target3 = newchurows[i]['F18'];			// 大分類コード
					target4 = newchurows[i]['F1'];			// 中分類コード

					var newLines = newshorows.filter(function(item, index){
						if((item.F16).indexOf(target1) >= 0
								&&(item.F17).indexOf(target2) >= 0
								&&(item.F18).indexOf(target3) >= 0
								&&(item.F19).indexOf(target4) >= 0){
								// 小分類データの中に、中分類データに紐ずくデータがある場合は処理なし。
							}else{
								// 紐ずくデータがない場合エラー
								errFlg = true;
							}
					});

				}
			}else if(newchurows2.length > 0){
				// 既存データの親を持つ新規中分類の場合
				if(shorows.length > 0){
					for (var i=0; i<newchurows2.length; i++){
						target1 = newchurows2[i]['F16'];		// 分類区分
						target2 = newchurows2[i]['F17'];		// 部門コード
						target3 = newchurows2[i]['F18'];		// 大分類コード
						target4 = newchurows2[i]['F1'];			// 中分類コード

						var newLines = shorows.filter(function(item, index){
							if((item.F16).indexOf(target1) >= 0
									&&(item.F17).indexOf(target2) >= 0
									&&(item.F18).indexOf(target3) >= 0
									&&(item.F19).indexOf(target4) >= 0){
									// 新規小分類データの中に、新規中分類データに紐ずくデータがある場合は処理なし。
								}else{
									// 紐ずくデータがない場合エラー
									errFlg = true;
								}
						});
					}
				}else{
					// 新規小分類データが存在しない場合はエラー
					errFlg = true;
				}
			}else if(newdairows.length > 0 && newchurows.length > 0 && newshorows.length > 0){
				// 小分類のみ入力がある場合は処理なし。
			}else if(newdairows.length > 0 || newchurows.length > 0){
				// 大分類、中分類にのみ入力がある場合。
				errFlg = true;
			}

			if(errFlg){
				emsg = "EX1028";
			}
			return emsg;
		},
		checkNewInsert: function(dairows, churows, shorows){		// 新規データ
			// TODO
			// Grid内全情報取得
			var ColumnsLength = 14;
			var emsg = "";
			var updFlgDai = false;
			var updFlgChu = false;
			var updFlgSho = false;
			var newcount = 0;
			// 対象情報抜粋
			var targetRows = [];
			for (var i=0; i<dairows.length; i++){
				// 変更前データと変更後データで比較を行う
				if(dairows[i]["F13"] == '1'){
					if(dairows[i]["F1"]==""){

					}else{
						updFlgDai= true
					}
				}
			}

			for (var i=0; i<churows.length; i++){
				// 変更前データと変更後データで比較を行う
				if(churows[i]["F13"] == '1'){
					if(churows[i]["F1"]==""){

					}else{
						updFlgChu = true;
					}
				}
			}

			for (var i=0; i<shorows.length; i++){
				// 変更前データと変更後データで比較を行う
				if(shorows[i]["F13"] == '1'){
					if(shorows[i]["F1"]==""){

					}else{
						updFlgSho = true;
					}
				}
			}

			if(updFlgDai){
				if(updFlgChu && updFlgSho){

				}else{
					emsg = "新規登録を行う場合は、大分類、中分類、小分類をセットで登録してください。";
				}
			}

			if(updFlgChu){
				if(updFlgSho){

				}else{
					emsg = "新規登録を行う場合は、中分類、小分類をセットで登録してください。";
				}
			}
			return emsg;
		},
		InputCheck :function(){
			var updflg = true;
			var dbunrui = [], dbunruikeys = [];
			var targetRowsDbunrui = $($.id.gridholder).datagrid('getRows');
			for (var i=0; i<targetRowsDbunrui.length; i++){
				dbunrui.push(targetRowsDbunrui[i]["F1"]);

				if(targetRowsDbunrui[i]["F1"] && targetRowsDbunrui[i]["F1"] !== ''){
					// 必須入力チェック：小分類名(漢字)
					if(targetRowsDbunrui[i]["F3"] == ''){
						var textArray = []
						textArray.push('分類名(漢字)');
						$.showMessage('E00001',textArray);
						updflg = false;
						return updflg
					}

					// 必須入力チェック：大分類名(ｶﾅ)
					if(targetRowsDbunrui[i]["F2"] == ''){
						var textArray = []
						textArray.push('分類名(ｶﾅ)');
						$.showMessage('E00001',textArray);
						updflg = false;
						return updflg
					}
				}

				//dbunruikeys.push(targetRowsDbunrui[i]["F1"]);
			}
			//alert(dbunrui.push(targetRowsDbunrui[24]["F1"]));
			// 画面に同じ大分類がある場合、エラー。
			var dbunrui_ = dbunrui.filter(function (element, index, self) { return self.indexOf(element)*1 === index; });
			if(dbunrui.length !== dbunrui_.length){
				//alert($.getMessage('E11112'));
				var textArray = []
				textArray.push('小分類コード');
				$.showMessage('E00004',textArray);
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
				          	{field:'F1',		title:'小分類コード', 			width:  90,halign:'center',align:'left',formatter:formatterLPad,editor:{type:'numberbox'}},
							{field:'F3',		title:'小分類名（漢字）',		width: 130,halign:'center',align:'left',editor:{type:'textbox',options:{editable:true,disabled:false,readonly:false}}},
							{field:'F2',		title:'小分類名（カナ）',		width: 130,halign:'center',align:'left',editor:{type:'textbox',options:{editable:true,disabled:false,readonly:false}}},
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
					}

				},
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
					$($.id.hiddenChangedIdx).val("1")
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
					$($.id.hiddenChangedIdx).val("1")
				},
				onUncheck:function(index){
					var rows = $('#'+id).datagrid('getRows');
					rows[index]["F14"]='0'
					$($.id.hiddenChangedIdx).val("1");
				},
				onClickCell:function(rowIndex, field, value){
					// 列名保持
					that.columnName = field;
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
		changeClearCheckInfo:function(id){
			// 画面遷移時に、Checboxの状態を引き継がない為、rowのcheckboxの情報をクリアする。
			var that = this;
			var rows = $('#'+id).datagrid('getRows');

			for (var i=0; i<rows.length; i++){
				if(rows[i]["F14"] == '1'){
					rows[i]["F14"] = '0'
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
			$.setJSONObject(sendJSON, 'reportYobi1', that.reportYobiInfo(), that.reportYobiInfo());			// 参照情報保持
			$.setJSONObject(sendJSON, 'callpage', that.name, that.name);

			// 戻る実行時用に現在の画面情報を保持する
			var states = $.getBackBaseJSON(that);
			// 各種グリッド情報を設定
			var newrepinfos = $.getBackJSON(that, states);
			$.setJSONObject(sendJSON, 'repinfo', newrepinfos, '');// 呼出し元レポート情報

			// Grid内編集中の場合はEndEditを行う
			var editIdx = that.editRowIndex['gridholder']
			if(editIdx != -1){
				$($.id.gridholder).datagrid('endEdit',editIdx);
			}

			// 選択行
			var row = $($.id.gridholder).datagrid("getSelected");

			// 実行ボタン別処理
			switch (btnId) {
			case $.id.btn_upd:
				var enptyRows = []

				index = 3;
				childurl = href[index];

				$.setJSONObject(sendJSON, $.id_update.Reportx031, enptyRows,enptyRows);			// データクリア：大分類マスタ
				$.setJSONObject(sendJSON, $.id_update.Reportx032, enptyRows,enptyRows);			// データクリア：中分類マスタ
				$.setJSONObject(sendJSON, $.id_update.Reportx033, enptyRows,enptyRows);			// データクリア：小分類マスタ

				break;
			case $.id.btn_cancel:
			case $.id.btn_back:

				// 入力値チェック
				if(!this.InputCheck()){
					return false;
				}

				sendMode = 1
				// 転送先情報
				index = 2;
				childurl = href[index];

				var szBunrui	= $.getJSONObject(this.jsonString, $.id.sel_bnnruikbn).value;				// 分類区分
				var szBumon		= $.getJSONObject(this.jsonString, $.id_inp.txt_bmncd).value;				// 検索部門コード
				var szDaicd		= $.getJSONObject(this.jsonString, $.id_inp.txt_daicd).value;				// 検索大分類コード
				var szChucd		= $.getJSONObject(this.jsonString, $.id_inp.txt_chucd).value;				// 引継ぎ中分類コード
				var szShocd		= $.getJSONObject(this.jsonString, $.id_inp.txt_shocd).value;				// 引継ぎ小分類コード


				$.setJSONObject(sendJSON, $.id.sel_bnnruikbn, szBunrui, szBunrui);							// 検索条件：分類区分
				$.setJSONObject(sendJSON, $.id_inp.txt_bmncd, szBumon, szBumon);							// 検索条件：部門コード
				$.setJSONObject(sendJSON, $.id_inp.txt_daicd, szDaicd, szDaicd);							// 検索条件：大分類コード
				$.setJSONObject(sendJSON, $.id_inp.txt_chucd, szChucd, szChucd);							// 検索条件：中分類コード
				$.setJSONObject(sendJSON, $.id_inp.txt_shocd, szShocd, szShocd);							// 検索条件：小分類コード

				// 変更して遷移した際に、登録画面の為メッセージが出てしまう。
				// 本画面は登録画面間の遷移の為メッセージを出さないように設定。
				$($.id.hiddenChangedIdx).val('');
				break;
			default:
				break;
			}

			// 引継ぎ用更新データ
			if(btnId==$.id.btn_select || btnId==$.id.btn_cancel || btnId=='btn_back' ){

				var targetRowsx031 = [];
				var targetRowsx032 = [];
				var targetRowsx033 = [];
				var targetRowsx034 = [];

				// データグリッド内のチェック情報をクリアする。
				this.changeClearCheckInfo("gridholder")

				// 最新の引継ぎ更新データ一覧を保持
				this.setUpdateDatas(that);

				// キャンセルボタン押下時は入力値を削除する。
				if(btnId==$.id.btn_cancel){
					that.cancelInptData(that.updateDatas[$.id_update.Reportx033])
				}

				targetRowsx031 = that.updateDatas[$.id_update.Reportx031];
				targetRowsx032 = that.updateDatas[$.id_update.Reportx032];
				targetRowsx033 = that.updateDatas[$.id_update.Reportx033];

				$.setJSONObject(sendJSON, $.id_update.Reportx031, targetRowsx031,targetRowsx031);			// 引継ぎ更新データ：大分類マスタ
				$.setJSONObject(sendJSON, $.id_update.Reportx032, targetRowsx032,targetRowsx032);			// 引継ぎ更新データ：中分類マスタ
				$.setJSONObject(sendJSON, $.id_update.Reportx033, targetRowsx033,targetRowsx033);			// 引継ぎ更新データ：小分類マスタ

				// 本画面ではsendMode=2での移動を想定していない為、
				// 前回選択行の情報をrepinfoから取り出し設定を送信情報に設定する。
				if(btnId==$.id.btn_cancel || btnId=='btn_back'){
					var targetId = "Out_Reportx03" + index;
					newrepinfos.some(function(v, i){
					    if (v.id==targetId){
					    	var TMPCOND =newrepinfos[i].value.TMPCOND;
					    	var innerTargetId = "scrollToIndex_#gridholder"
					    	TMPCOND.some(function(w, j){
					    		if (w.id==innerTargetId){
					    			$.setJSONObject(sendJSON, innerTargetId, w.value, w.text);
					    		}
					    	});
					    }
					});
				}
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
		cancelInptData: function(targetrow){
			// キャンセルボタン押下時に入力データを削除する。
			var that = this;

			var szBunrui	= $.getJSONObject(this.jsonString, $.id.sel_bnnruikbn).value;			// 分類区分
			var szBumon		= $.getJSONObject(this.jsonString, $.id_inp.txt_bmncd).value;			// 部門コード
			var szDaicd		= $.getJSONObject(this.jsonString, $.id_inp.txt_daicd).value;			// 大分類コード
			var szChucd		= $.getJSONObject(this.jsonString, $.id_inp.txt_chucd).value;			// 中分類コード

			var arterrows = [];

			targetrow.some(function(v, i){
			    if (v.F16==szBunrui && v.F17==szBumon && v.F18==szDaicd && v.F19==szChucd){

			    }else{
			    	arterrows.push(targetrow[i])
			    }
			});
			that.updateDatas[$.id_update.Reportx033] = arterrows
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