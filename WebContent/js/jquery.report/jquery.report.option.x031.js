/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_Reportx031',			// （必須）レポートオプションの確認
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
		dedefaultObjNum:	12,					// 初期化オブジェクト数
		initObjNum:	-1,
		initedObject: [],
		maxMergeCell: 0,
		onChangeFlag : false,
		onChangeFlag2 : false,
		columnName:'',	// OnClickRowの列名
		queried : false,
		initializes : true,
		onChangeReport: false,
		reportYobiInfo: function(){				// （必須）レポートメニューに登録された予備情報＝帳票をどういった表示にしたいかの情報
			return $('#reportYobi1').val();
		},
		sendBtnid: "",						 	// （必須）呼出ボタンID情報
		focusRootId:"cc",						// （キー移動時必須）キー移動イベントのルートとなるパネルのID
		focusParentId:"",						// （キー移動時必須）現在フォーカスがあたっている項目の親となるパネルのID
		focusGridId:"",							// （キー移動時必須）現在フォーカスがあたっているDataGridのID
		editRowIndex:{},						// グリッド編集行保持
		gridData:[],							// 検索結果
		gridTitle:[],							// 検索結果
		updateDatas:{},							// 各画面引継ぎ更新データ一覧
		serchFlg : true,						// 検索ボタン押下判定
		initialize: function (reportno){		// （必須）初期化
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

			var isUpdateReport = false;

			// 検索条件：部門コード
			$.setInputbox(that, reportno, $.id_inp.txt_bmncd, isUpdateReport);

			// 検索条件：分類区分
			$.setMeisyoCombo(that, reportno, $.id.sel_bnnruikbn, isUpdateReport)

			// 個別レイアウト調整：分類区分
			//$('#'+$.id.sel_bnnruikbn).combobox({panelWidth:200,})

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

			$.setCheckboxInit2(that.jsonHidden, 'chk_f14', that);

			// 編集可能データグリッドの共通処理設定
			// 編集エディターの機能拡張（非表示で参照用のオブジェクトが準備してある前提、参照オブジェクトはグリッドのフィールド名を元に判断）
			$.extendDatagridEditor();

			that.setEditableGrid(that, reportno, "gridholder");

			// 初期化終了
			this.initializes =! this.initializes;

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
			var sendBtnid = $.getJSONValue(that.jsonHidden, "sendBtnid");
			if(sendBtnid && sendBtnid.length > 0){
				$.reg.search = true;
				that.serchFlg = false			// 自動検索時は押下判定をfalseにする。
			}
			$.initReportInfo("BR011", "大分類マスタ　一覧", "大分類マスタ");

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
			$('#'+$.id.btn_select).on("click", $.pushChangeReport);
			$('#'+$.id.btn_cancel).on("click", $.pushChangeReport);

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
			//$($.id.hiddenChangedIdx).val("");						// 変更行Index
			// グリッド初期化
			this.success(this.name, false);
		},
		validation: function (){	// （必須）批准
			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform).form('validate');
			if(!rt){
				return rt;
			}

			// マスタ存在チェック：部門コード
			var bmncd	 = $.getInputboxValue($('#'+$.id_inp.txt_bmncd)); 			// 部門コード
			var msgid = that.checkInputboxFunc($.id_inp.txt_bmncd, bmncd, '');
			if(msgid !==null){
				$.showMessage(msgid);
				var rt= false;
				return false;
			}

			// 入力エラーなしの場合に検索条件を格納
			if (rt == true) that.jsonString = that.jsonTemp.slice(0);
			// 入力チェック用の配列をクリア
			that.jsonTemp = [];

			// 最新の引継ぎ更新データ一覧を保持
			this.setUpdateDatas(that);

			if(that.updateDatas[$.id_update.Reportx031].length > 0
					|| that.updateDatas[$.id_update.Reportx032].length > 0
					|| that.updateDatas[$.id_update.Reportx033].length > 0){

				if(!that.sendBtnid  || that.sendBtnid.length == 0){
					// フォーム情報取得
					//$.report[that.name].getEasyUI();

					$.confirmReportUnregist(function(){
						var reportNumber = $.getReportNumber(reportno);
						var reportno = that.name;

						// マスク追加
						$.appendMask();

						// セッションタイムアウト、利用時間外の確認
						var isTimeout = $.checkIsTimeout();
						if (! isTimeout) {
							// 検索条件保持
							$.ajax({
								url: $.reg.easy,
								type: 'POST',
								async: false,
								data: {
									"page"	: reportno,
									"obj"	: $.id.btn_search,
									"sel"	: "json",
									"userid": $($.id.hidden_userid).val(),
									"user"	: $($.id.hiddenUser).val(),
									"report": $($.id.hiddenReport).val(),
									"json"	: JSON.stringify($.report[0].getJSONString())
								},
								success: function(json){
									// 検索実行
									$.report[0].success(reportno, 0, $.id.btn_search);
								}
							});
						}
						//return true;

					});
					return false;
				}
			}
			return rt;
		},
		success: function(reportno, sortable){	// （必須）正処理
			if (sortable) sortable=1; else sortable=0;
			var that = this;

			// 検索実行
			var szBumon		= $.getJSONObject(this.jsonString, $.id_inp.txt_bmncd).value;			// 部門
			var szBunrui	= $.getJSONObject(this.jsonString, $.id.sel_bnnruikbn).value;			// 分類区分

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			//$($.id.gridholder).datagrid('loading');

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

					// Load処理回避
					$.tryChangeURL($.reg.jqeasy);

					// 保持している値をすべてクリア
					if(that.serchFlg){
						that.clearUpdateData();
					}else{
						that.serchFlg = true;
					}

					// グリッド再描画（easyui 1.4.2 対応）
					//$($.id.gridholder).datagrid('load', {} );
					//$('#'+'grd_tengp2').datagrid('reload');
					$.removeMask();
					$.removeMaskMsg();
					$($.id.gridholder).datagrid('reload');

					that.sendBtnid = "";

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

			//targetRowsx031 = $($.id.gridholder).datagrid('getRows');

			this.setUpdateDatas(that);
			targetRowsx031 = that.updateDatas[$.id_update.Reportx031];
			targetRowsx032 = $.getJSONValue(that.jsonHidden, $.id_update.Reportx032);
			targetRowsx033 = $.getJSONValue(that.jsonHidden, $.id_update.Reportx033);


			// 新規行に削除チェックがある場合エラー
			/*var rows = $($.id.gridholder).datagrid('getRows');
			for (var i=0; i<rows.length; i++){
				if(rows[i]["F13"] == "1" && rows[i]["F14"] == "1"){

				}
			}*/

			var emsg = this.checkNewInsert3(targetRowsx031, targetRowsx032, targetRowsx033);
			if(emsg.length > 0){
				$.showMessage(emsg);
				return false;
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

			// 親データ削除による不要更新子データを取り除く
			this.setCheckInfOtherDatasDai()

			var szBunrui	= $.getJSONObject(this.jsonString, $.id.sel_bnnruikbn).value;		// 検索分類区分
			var szBumon		= $.getJSONObject(this.jsonString, $.id_inp.txt_bmncd).value;		// 検索部門コード
			var szDaicd		= "";																// 検索大分類コード
			var szChucd		= "";																// 検索中分類コード
			var szShocd		= "";																// 検索小分類コード

			for (var i = 0; i < that.jsonHidden.length; i++) {
				if (that.jsonHidden[i].id === $.id_inp.txt_daicd) {
					szDaicd		= $.getJSONObject(this.jsonHidden, $.id_inp.txt_daicd).value;

				}else if (that.jsonHidden[i].id === $.id_inp.txt_chucd) {
					szChucd		= $.getJSONObject(this.jsonHidden, $.id_inp.txt_chucd).value;

				}else if (that.jsonHidden[i].id === $.id_inp.txt_shocd) {
					szShocd		= $.getJSONObject(this.jsonHidden, $.id_inp.txt_shocd).value;

				}
			}

			// 変更行情報取得
			//var changedIndex = $($.id.hiddenChangedIdx).val().split(",");
			// Grid内全情報取得
			var rows = $($.id.gridholder).datagrid('getRows');
			// 対象情報抜粋
			var targetRows = [];
			targetRows = this.setUpdateData(that.gridData, rows);

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$.appendMask();

			$.post(
					$.reg.jqgrid ,
					{
						report:			'Out_Reportx031',				// レポート名
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
				value:	$('#'+$.id_inp.txt_bmncd).textbox('getValue'),
				text:	$('#'+$.id_inp.txt_bmncd).textbox('getText')
			});
			// 引継ぎ大分類コード
			this.jsonTemp.push({
				id:		$.id_inp.txt_daicd,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_daicd),
				text:	''
			});
			// 引継ぎ分類コード
			this.jsonTemp.push({
				id:		$.id_inp.txt_chucd,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_chucd),
				text:	''
			});
			// 引継ぎ小分類コード
			this.jsonTemp.push({
				id:		$.id_inp.txt_shocd,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_shocd),
				text:	''
			});
			// 検索分類区分
			this.jsonTemp.push({
				id:		$.id.sel_bnnruikbn,
				value:	$('#'+$.id.sel_bnnruikbn).combogrid('getValue'),
				text:	$('#'+$.id.sel_bnnruikbn).combogrid('getText')
			});

		},
		setData: function(rows, opts){		// データ表示
			var that = this;

			if(rows.length > 0){
				/*$('#'+that.focusRootId).find('[col^=F]').each(function(){
					var col = $(this).attr('col');
					if(rows[0][col]){
						$.setInputboxValue($(this), rows[0][col]);
					}
				});*/
				var col = $(this).attr('col');
				if(rows[0][col]){
					$.setInputboxValue($(this), rows[0][col]);
				}
			}
		},
		setUpdateData: function(oldrows, newrows){		// 更新データを保持
			// TODO
			// Grid内全情報取得
			var ColumnsLength = 14;
			var updFlg = false

			var szBumon		= $.getJSONObject(this.jsonString, $.id_inp.txt_bmncd).value;			// 部門
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
						};
					targetRows.push(rowData);
					updFlg = false;
				}
			}
			return targetRows;
		},
		clearUpdateData: function(){
			var that = this
			var enptyRows = [];

			that.updateDatas[$.id_update.Reportx031] = enptyRows;
			that.updateDatas[$.id_update.Reportx032] = enptyRows;
			that.updateDatas[$.id_update.Reportx033] = enptyRows;
			//that.updateDatas[$.id_update.Reportx034] = enptyRows;
		},
		getGridData: function (target){
			var that = this;

			var data = {};
			var targetRows= [];

			// 基本情報
			that.updateDatas["targetRows_"+name.replace("Out_Report", "")] = thisrows;

			// 納品日一覧
			if(target===undefined || target===$.id_update.Reportx031){
				var rows	 = $($.id.gridholder).datagrid('getRows');				// グリッド情報
				var bmncd	 = $('#'+$.id_inp.txt_bmncd).numberbox('getValue');		// 部門コード

				for (var i=0; i<rows.length; i++){
					var rowDate = {
							F1	 : bmncd,
							F2	 : rows[i][F1],
							F3	 : rows[i][F2],
							F4	 : rows[i][F3],
							F5	 : rows[i][F4],
							F6	 : rows[i][F5],
							F7	 : rows[i][F6],
							F8	 : rows[i][F7],
							F9	 : rows[i][F8],
							F10	 : rows[i][F9],
							F11	 : rows[i][F13],
							F12	 : rows[i][F14],
					};
					targetRows.push(rowDate);
				}
				data[target] = targetRows;
			}

			else if(target===undefined || target===$.id_update.Reportx032){

			}
			return data;
		},
		getMergeGridDate: function(target){
			// 保持したデータと入力データ比較を比較する。
			var that = this;

			var newrows = that.getGridData(target)[target];		// 変更データ
			var oldrows = [];
			var targetRows= [];

			if(target===undefined || target===$.id_update.Reportx031){
				oldrows = that.updateDatasx031

			}else if(target===undefined || target===$.id_update.Reportx032){
				oldrows = that.updateDatasx032

			}else if(target===undefined || target===$.id_update.Reportx033){
				oldrows = that.updateDatasx033

			}

			if(target==!undefined){
				// 店舗一覧
				var shoridt	 = $('#'+$.id.txt_shoridt).val();						// 処理日付
				for (var i=0; i<newrows.length; i++){
					if((oldrows[i]['F2'] ? oldrows[i]['F2'] : "") !== (newrows[i]['F2'] ? newrows[i]['F2'] : "")
							|| (oldrows[i]['F3'] ? oldrows[i]['F3'] : "") !== (newrows[i]['F3'] ? newrows[i]['F3'] : "")
							|| (oldrows[i]['F4'] ? oldrows[i]['F4'] : "") !== (newrows[i]['F4'] ? newrows[i]['F4'] : "")
							|| (oldrows[i]['F5'] ? oldrows[i]['F5'] : "") !== (newrows[i]['F5'] ? newrows[i]['F5'] : "")
							|| (oldrows[i]['F6'] ? oldrows[i]['F6'] : "") !== (newrows[i]['F6'] ? newrows[i]['F6'] : "")
							|| (oldrows[i]['F7'] ? oldrows[i]['F7'] : "") !== (newrows[i]['F7'] ? newrows[i]['F7'] : "")
							|| (oldrows[i]['F8'] ? oldrows[i]['F8'] : "") !== (newrows[i]['F8'] ? newrows[i]['F8'] : "")
							|| (oldrows[i]['F9'] ? oldrows[i]['F9'] : "") !== (newrows[i]['F9'] ? newrows[i]['F9'] : "")
							|| (oldrows[i]['F10'] ? oldrows[i]['F10'] : "") !== (newrows[i]['F10'] ? newrows[i]['F10'] : "")
					){
						if(newrows[i]["F1"] && newrows[i]["F2"] ){
							var rowDate = {
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
							};
							if(rowDate){
								targetRows.push(rowDate);
							}
						}
					}
				}
			}
			return targetRows;
		},
		setGridData: function (data, target){
			var that = this;

			// 実仕入先一覧
			if(target===undefined || target==='targetRows_x031'){
				that.updateDatasx031 =  data[target];

			}else if(target===undefined || target==='targetRows_x032'){
				that.updateDatasx032 =  data[target];

			}else if(target===undefined || target==='targetRows_x033'){
				that.updateDatasx033 =  data[target];

			}
		},
		setUpdateDatas: function(that){		// 更新データ一覧を保持

			var name = that.name;
			var rows = $($.id.gridholder).datagrid('getRows');
			var enpryRows = [];
			var thisrows = [];
			thisrows = this.setUpdateData(that.gridData,rows);
			var allupdaterows = []				// この画面で入力を行って保持してきた更新データ
			allupdaterows = that.updateDatas["targetRows_"+name.replace("Out_Report", "")];

			var szBumon		= $.getJSONObject(this.jsonString, $.id_inp.txt_bmncd).value;			// 部門
			var szBunrui	= $.getJSONObject(this.jsonString, $.id.sel_bnnruikbn).value;			// 分類区分

			if(allupdaterows.length > 0){
				// 引継ぎデータが存在する場合

				var allupdaterows2 = [];			// この画面とは異なる検索条件で保持されてきたデータ

				for (var i=0; i<allupdaterows.length; i++){

					if(rows.length > 0){
						// 現在のデータと比較する。
						if(allupdaterows[i]['F16'] === szBunrui
						&& allupdaterows[i]['F17'] === szBumon){

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
		setCheckInfOtherDatasDai: function(){
			// 親データを削除チェックして、子データの変更を行っていた場合、親が消されることで子データも消されるため、意味のない変更となる。
			// その為、変更された子データを取り除いたデータを登録に使用する。
			var that = this;

			var targetRowsx031 = [];
			var targetRowsx032 = [];
			var targetRowsx033 = [];

			targetRowsx031 = that.updateDatas[$.id_update.Reportx031];
			targetRowsx032 = that.updateDatas[$.id_update.Reportx032];
			targetRowsx033 = that.updateDatas[$.id_update.Reportx033];

			var targetRowsx031_Del = [];

			var targetRowsx032_Af = [];			// 中分類登録データ(親削除データを除く)
			var targetRowsx033_Af = [];			// 小分類登録データ(親削除データを除く)

			for (var i=0; i< targetRowsx031.length; i++){
				if(targetRowsx031[i]['F14'] == '1'){
					// 削除データ有の場合
					var bunrui	 = targetRowsx031[i]['F16']
					var bmncd	 = targetRowsx031[i]['F17']
					var daicd	 = targetRowsx031[i]['F1']

					targetRowsx031_Del.push(targetRowsx031[i])
				}
			}

			// 中分類データ抽出
			var countFlg = false
			for (var j=0; j< targetRowsx032.length; j++){

				var bunrui	 = targetRowsx032[j]['F16']
				var bmncd	 = targetRowsx032[j]['F17']
				var daicd	 = targetRowsx032[j]['F18']

				countFlg = false;

				if(targetRowsx031_Del.length > 0){
					var newLines = targetRowsx031_Del.filter(function(item, index){
						if((item.F16).indexOf(bunrui) >= 0
								&&(item.F17).indexOf(bmncd) >= 0
								&&(item.F1).indexOf(daicd) >= 0){
								// 中分類データの中に、削除大分類データに紐ずくデータがある場合
							countFlg = true;
						}
					});
				}
				if(!countFlg){
					targetRowsx032_Af.push(targetRowsx032[j])
				}
			}

			// 小分類データ抽出
			var countFlg = false
			for (var j=0; j< targetRowsx033.length; j++){

				var bunrui	 = targetRowsx033[j]['F16']
				var bmncd	 = targetRowsx033[j]['F17']
				var daicd	 = targetRowsx033[j]['F18']

				countFlg = false;

				if(targetRowsx031_Del.length > 0){
					var newLines = targetRowsx031_Del.filter(function(item, index){
						if((item.F16).indexOf(bunrui) >= 0
								&&(item.F17).indexOf(bmncd) >= 0
								&&(item.F1).indexOf(daicd) >= 0){
								// 小分類データの中に、削除大分類データに紐ずくデータがある場合
							countFlg = true;
						}
					});
				}
				if(!countFlg){
					targetRowsx033_Af.push(targetRowsx033[j])
				}
			}

			// 削除予定の更新データ(親が削除される子データ)を取り除いたデータを設定
			that.updateDatas[$.id_update.Reportx032] = targetRowsx032_Af;
			that.updateDatas[$.id_update.Reportx033] = targetRowsx033_Af;
		},
		setUpdateDataThis: function(targetdata){
			var that = this;
			var thisdatasAf = [];

			for (var i=0; i< that.updateDatas[$.id_update.Reportx031].length; i++){
				thisdatasAf.push(that.updateDatas[$.id_update.Reportx031][i])
			}

			for (var i=0; i< targetdata.length; i++){
				thisdatasAf.push(targetdata[i]);
			}
			that.updateDatas[$.id_update.Reportx031] = thisdatasAf;
		},
		setUpdateDatasCridIn: function(that, id){		// 保持されて遷移されてきた更新データ一覧をグリッドに設定
			var rows = [];
			rows = $.getJSONValue(that.jsonHidden, "targetRows_"+that.name.replace("Out_Report", ""));
			var szBumon		= $.getJSONObject(this.jsonString, $.id_inp.txt_bmncd).value;			// 部門
			var szBunrui	= $.getJSONObject(this.jsonString, $.id.sel_bnnruikbn).value;			// 分類区分

			if(rows.length > 0){
				for (var i=0; i<rows.length; i++){

					if(rows[i]['F16'] === szBunrui && rows[i]['F17'] === szBumon){
						// データグリッドを更新
						$('#'+id).datagrid('updateRow',{
							index: Number(rows[i]['idx']) - 1,
							row: rows[i],
						})

						// チェックボックスの再描画
						$.afterEditAddCheckbox($('#'+id).datagrid('getPanel').find("[datagrid-row-index='"+(rows[i]['idx']-1)+"']"));

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

						if(dairows[i]["F14"] == '1'){
							// 新規行に削除チェックがある場合
							errFlg = true
						}
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

			/*if(newchurows.length > 0){
				// 親との紐付きを確認
				var target1 = '';
				var target2 = '';
				var target3 = '';
				var target4 = '';

				for (var i=0; i<newchurows.length; i++){
					target1 = newdairows[i]['F16'];			// 分類区分
					target2 = newdairows[i]['F17'];			// 部門コード
					target3 = newdairows[i]['F18'];			// 大分類コード


					var newLines = newdairows.filter(function(item, index){
						if((item.F16).indexOf(target1) >= 0
							&&(item.F17).indexOf(target2) >= 0
							&&(item.F1).indexOf(target3) >= 0){
								// 新規大分類データの中に、新規中分類データに紐ずくデータがある場合は処理なし。
							}else{
								// 紐ずくデータがない場合
								// 既存大分類データに親データがあるか確かめる。
								newLines = dairows.filter(function(item, index){
									if((item.F16).indexOf(target1) >= 0
										&&(item.F17).indexOf(target2) >= 0
										&&(item.F1).indexOf(target3) >= 0){
										// 既存大分類データの中に、新規中分類データに紐ずくデータがある場合は処理なし。
									}else{
										errFlg = true;
									}
								});
							}
					});
				}
			}*/

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

			var errFlg = false;
			var newdairows = [];	// 大分類新規行
			var newchurows = [];	// 中分類新規行
			var newshorows = [];	// 小分類新規行


			for (var i=0; i<dairows.length; i++){
				if(dairows[i]["F13"] == '1'){
					newdairows.push(dairows[i])
				}
			}

			for (var i=0; i<churows.length; i++){
				if(churows[i]["F13"] == '1'){
					newchurows.push(churows[i])
				}
			}

			for (var i=0; i<shorows.length; i++){
				if(shorows[i]["F13"] == '1'){
					newshorows.push(shorows[i])
				}
			}

			var target1 = '';
			var target2 = '';
			var target3 = '';
			newchurows.some(function(v, i){
			    if (v.F16===target1 && v.F17===target2){
			    	// 存在有
			    }
			});

			for (var i=0; i<newdairows.length; i++){
				target1 = newdairows[i]['F16'];
				target2 = newdairows[i]['F17'];
				newchurows.some(function(v, i){
					if(newchurows.F16.indexOf(target1)===-1 && newchurows.F17.indexOf(target2)===-1 )

				    if (v.F16===target1 && v.F17===target2){
				    	// 存在有
				    }
				});
			}

			if(newdairows.length > 0){
				if(newchurows.length > 0){
					errFlg = true
				}else{
					for (var i=0; i<newdairows.length; i++){
						for (var j=0; j<newchurows.length; j++){
							if(newdairows[i]['F16'] === newchurows[i]['F16']
								&& newdairows[i]['F17'] === newchurows[i]['F17']
								&& newdairows[i]['F1'] === newchurows[i]['F18']
							){}else{
								errFlg = true
							}
						}
					}
				}

				if(newshorows.length > 0){
					errFlg = true
				}else{
					for (var i=0; i<newdairows.length; i++){
						for (var j=0; j<newchurows.length; j++){
							if(newdairows[i]['F16'] === newchurows[i]['F16']
								&& newdairows[i]['F17'] === newchurows[i]['F17']
								&& newdairows[i]['F1'] === newchurows[i]['F18']
							){}else{
								errFlg = true
							}
						}
					}
				}
			}

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
					// 必須入力チェック：大分類名(漢字)
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
				textArray.push('大分類コード');
				$.showMessage('E00004',textArray);
				updflg = false;
				return updflg
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
				          	{field:'F14',		title:'削除',					checkbox:true,	width:  90,halign:'center',align:'center'},
				          	{field:'F1',		title:'大分類コード', 			width:  90,halign:'center',align:'left',formatter:formatterLPad,editor:{type:'numberbox'}},
				          	{field:'F3',		title:'大分類名（漢字）',		width: 130,halign:'center',align:'left',editor:{type:'textbox',options:{editable:true,disabled:false,readonly:false}}},
							{field:'F2',		title:'大分類名（カナ）',		width: 130,halign:'center',align:'left',editor:{type:'textbox',options:{editable:true,disabled:false,readonly:false}}},
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
					that.setUpdateDatasCridIn(that, id);
					var panel = $('#'+id).datagrid('getPanel');
					$(panel).find(':checkbox').filter("[tabindex!=-1]").filter('[disabled!=disabled]').each(function(){
						$(this).on('change', function(e){
							//func_focusout_editgrid(e);
						});
					});

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
						var targetName = "scrollToIndex_"+id;
						that.jsonHidden.some(function(v, i){
						    if (v.id==targetName) that.jsonHidden.splice(i,1);
						});
					}
				},
				onSelect:function(index){
					var rows = $('#'+id).datagrid('getRows');
					var col = $('#'+id).datagrid('getColumnOption', 'F1');
					if(rows[index]["F13"]=='1'){
						col.editor = {
								type:'numberbox',
								options:{cls:'labelInput',editable:true,disabled:false,readonly:false},
								formatter:formatterLPad
							}

					}else{
						col.editor = false
					}
				},
				onCheck:function(index){
					var rows = $('#'+id).datagrid('getRows');
					if($('#'+id).datagrid('getPanel').find("[datagrid-row-index='"+index+"']").find(":checkbox").prop("checked")){
						rows[index]["F14"]='1'
					}else{
						rows[index]["F14"]=''
					}
					$($.id.hiddenChangedIdx).val("1");
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

				index = 1;
				childurl = href[index];

				$.setJSONObject(sendJSON, $.id_update.Reportx031, enptyRows,enptyRows);			// データクリア：大分類マスタ
				$.setJSONObject(sendJSON, $.id_update.Reportx032, enptyRows,enptyRows);			// データクリア：中分類マスタ
				$.setJSONObject(sendJSON, $.id_update.Reportx033, enptyRows,enptyRows);			// データクリア：小分類マスタ

				break;
			case $.id.btn_select:

				sendMode = 1;
				// 新規登録行選択時
				var rowIndex = $($.id.gridholder).datagrid("getRowIndex", row);
				$($.id.gridholder).datagrid('endEdit',rowIndex);

				// 画面内遷移時は変更なし扱い
				if($($.id.hiddenChangedIdx).is(':enabled')){
					$.setJSONObject(sendJSON, "onChanged", 1, 1);
				}

				if(!row){
					$.showMessage('E00008');
					return false;
				}
				//他画面へのデータ引継ぎを行う為、入力値をエラーチェックする。
				var rt = $($.id.toolbarform).form('validate');
				if(!rt){
					$.showMessage('E00001');
					return rt;
				}
				if(row['F13']=='1'){
					if(row['F1']==""){
						$.showMessage('EX1103',['大分類コード']);
						return false;
					}
				}
				// 入力値チェック
				if(!this.InputCheck()){
					return false;
				}

				// 転送先情報
				index = 2;
				childurl = href[index];

				var szBunrui			= $.getJSONObject(this.jsonString, $.id.sel_bnnruikbn).value;		// 分類区分
				var txt_sel_bmncd		= $.getJSONObject(this.jsonString, $.id_inp.txt_bmncd).value;		// 検索部門コード

				// オブジェクト作成
				$.setJSONObject(sendJSON, $.id.sel_bnnruikbn, szBunrui, szBunrui);							// 検索条件：分類区分
				$.setJSONObject(sendJSON, $.id_inp.txt_bmncd, txt_sel_bmncd, txt_sel_bmncd);				// 検索条件：部門コード
				$.setJSONObject(sendJSON, $.id_inp.txt_daicd, row.F1, row.F1);								// 検索条件：大分類コード
				$.setJSONObject(sendJSON, "beforIndex", 1, 1);												// 遷移前のindex

				break;
			case $.id.btn_cancel:
			case $.id.btn_back:
				// 転送先情報
				childurl = parent.$('#hdn_menu_path').val();
				// 変更して遷移した際に、登録画面の為メッセージが出てしまう。

				break;
			default:
				break;
			}

			// 引継ぎ用更新データ
			if(btnId==$.id.btn_select){
				var targetRowsx031 = [];
				var targetRowsx032 = [];
				var targetRowsx033 = [];

				// データグリッド内のチェック情報をクリアする。
				this.changeClearCheckInfo("gridholder")

				// 最新の引継ぎ更新データ一覧を保持
				this.setUpdateDatas(that);

				if(btnId===$.id.btn_select){
					targetRowsx031 = that.updateDatas[$.id_update.Reportx031];
					targetRowsx032 = that.updateDatas[$.id_update.Reportx032];
					targetRowsx033 = that.updateDatas[$.id_update.Reportx033];
				}

				$.setJSONObject(sendJSON, $.id_update.Reportx031, targetRowsx031,targetRowsx031);			// 引継ぎ更新データ：大分類マスタ
				$.setJSONObject(sendJSON, $.id_update.Reportx032, targetRowsx032,targetRowsx032);			// 引継ぎ更新データ：中分類マスタ
				$.setJSONObject(sendJSON, $.id_update.Reportx033, targetRowsx033,targetRowsx033);			// 引継ぎ更新データ：小分類マスタ
			}

			if(btnId===$.id.btn_select){
				that.SendForm({
					type: 'post',
					url: childurl,
					data: {
						sendMode:	sendMode,
						sendParam:	JSON.stringify( sendJSON )
					}
				});
			}else{
				$.SendForm({
					type: 'post',
					url: childurl,
					data: {
						sendMode:	sendMode,
						sendParam:	JSON.stringify( sendJSON )
					}
				});
			}
		},
		SendForm : function(s){
			var func = function(){
				var def = {
					type: 'get',
					url: location.href,
					data: {}
				};

				s = jQuery.extend(true, s, jQuery.extend(true, {}, def, s));

				var form = $('<form style="display:none;">')
					.attr({
						'method': s.type,
						'action': s.url
					})
					.appendTo(top.document.body);

				for (var a in s.data) {
					$('<input>')
						.attr({
							'name': a,
							'value': s.data[a]
						})
						.appendTo(form[0]);
				};
				form[0].submit();
			};
			func()
			//$.confirmReportUnregist(func);
		},
		// IDとvalueでチェック処理を実施
		checkInputboxFunc:function(id, newValue, kbn, record, isNew){
			var that = this;

			// 商品コード
			if(id===$.id_inp.txt_bmncd){
				if(newValue !== '' && newValue){

					// マスタ存在チェック：部門コード
					var param = {};
					param["KEY"] =  "MST_CNT";
					param["value"] = newValue;
					var chk_cnt = $.getInputboxData(that.name, $.id.action_check, $.id_inp.txt_bmncd, [param]);
					if(chk_cnt==="" || chk_cnt==="0"){
						return "E11097";
					}
				}
			}
			return null;
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