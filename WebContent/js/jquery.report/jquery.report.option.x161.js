/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_Reportx161',			// （必須）レポートオプションの確認
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
		dedefaultObjNum:	6,					// 初期化オブジェクト数
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
		grd_zitsir_data:[],						// グリッド情報
		grd_fsirt_data:[],						// グリッド情報
		gridTitle:[],							// 検索結果
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

			// 個別レイアウト調整：単品管理区分
			//$('#'+$.id_mei.kbn425).combobox({panelWidth:200,})

			that.onChangeReport = false;

			//that.queried = true;

			// 入力テキストボックス系
			var inputbox = Object.getOwnPropertyNames($.id_inp);
			var isSearchId = [$.id_inp.txt_sircd, $.id_inp.txt_sirkn];		// 検索条件のID
			for ( var sel in inputbox ) {
				if($('#'+$.id_inp[inputbox[sel]]).length > 0){
					var isUpdateReport = isSearchId.indexOf(inputbox[sel]) === -1;
					$.setInputbox(that, reportno, $.id_inp[inputbox[sel]], isUpdateReport);
				}
			}

			that.setEditableGrid(that, reportno, "grd_zitsir");
			that.setEditableGrid(that, reportno, "grd_fstenpo");

			$('#'+$.id_inp.txt_sircd).textbox({
				  onChange: function(newValue, oldValue){
					that.changeInputboxFunc(that, $.id_inp.txt_sircd, newValue, $('#'+$.id_inp.txt_sircd))
				 }
			});

			// 初期化終了
			this.initializes =! this.initializes;

			// 名称を検索するため、フォーカスを動かす。
			var newval = $('#'+$.id_inp.txt_sircd).numberbox('getValue');
			$('#'+$.id_inp.txt_sircd).numberbox('reset').numberbox('setValue', newval);

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
			if(that.reportYobiInfo()==='1'){
				//$($.id.buttons).hide();
				// 新規登録ボタン非表示
				$('#'+$.id.btn_upd).linkbutton('disable');
				$('#'+$.id.btn_upd).attr('disabled', 'disabled').hide();
				$('#'+$.id.btn_cancel).linkbutton('disable');
				$('#'+$.id.btn_cancel).attr('disabled', 'disabled').hide();
				$('#'+$.id.btn_del).linkbutton('disable');
				$('#'+$.id.btn_del).attr('disabled', 'disabled').hide();
				$.initReportInfo("SI021", "複数仕入先 参照", "参照");
				$('#'+$.id.btn_cancel).on("click", $.pushChangeReport);
				$.setInputBoxDisable($($.id.hiddenChangedIdx));

			}else if(that.reportYobiInfo()==='2'){
				$('#btn_search').attr('disabled', 'disabled').hide();
				$('#btn_del').attr('disabled', 'disabled').hide();
				$('#'+$.id.btn_cancel).on("click", $.pushChangeReport);
				$.initReportInfo("SI023", "複数仕入先 新規登録", "新規");
				that.jsonHidden = [];
				that.jsonString = [];
				that.jsonTemp = [];
				that.sendBtnid = $.id.btn_new;

			}else if(that.reportYobiInfo()==='0'){

				$.initReportInfo("SI022", "複数仕入先 変更", "変更");
				$('#'+$.id.btn_cancel).on("click", $.pushChangeReport);

			}
			$.setInputBoxDisable($("#"+$.id_inp.txt_sirkn));
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


			var sircd = $.getInputboxValue($('#'+$.id_inp.txt_sircd));			// 仕入先コード
			if(sircd && sircd !== ''){
				// マスタ存在チェック：仕入先マスタ
				var msgid = that.checkInputboxFunc($.id_inp.txt_sircd, sircd, '');
				if(msgid != null){
					//$.showMessage(msgid, undefined, func_focus );
					$.showMessage(msgid, undefined);
					return false;
				}

				// マスタ存在チェック：複数仕入先_実仕入先マスタ
				var param = {};
				param["KEY"] =  "MST_CNT";
				param["value"] = sircd;
				var chk_cnt = $.getInputboxData(that.name, $.id.action_check, $.id_inp.txt_rsircd, [param]);
				if(chk_cnt==="" || chk_cnt==="0"){
					$.showMessage('EX1100', ['仕入先コード','複数仕入先_実仕入先マスタ']);
					return false;
				}
			}

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
			var txt_sircd		= $.getJSONObject(this.jsonString, $.id_inp.txt_sircd).value;		// 仕入先コード

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			$($.id.gridholder).datagrid('loading');


			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,		// レポート名
					SIRCD:			txt_sircd,
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
					that.setEditableGrid(that, reportno, "grd_zitsir");
					that.setEditableGrid(that, reportno, "grd_fstenpo");

					// 各グリッドの値を保持する
					that.grd_zitsir_data	 =  $('#grd_zitsir').datagrid('getRows');
					that.grd_fsirt_data		 =  $('#grd_fstenpo').datagrid('getRows');

					//targetRows = this.setUpdateData(that.gridData,rows);

					// 状態保存
					$.saveState2(reportno, that.getJSONString());

					// ログ出力
					$.log(that.timeData, 'loaded:');
				}
			);
		},
		updValidation: function (){	// （必須）批准
			var that = this;

			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform).form('validate');
			if(!rt){
				$.addErrState(that, $('.validatebox-invalid').eq(0), false);
				return rt;
			}

			// 実仕入先一覧の入力編集を終了する。
			var row = $('#grd_zitsir').datagrid("getSelected");
			if(row){
				var rowIndex = $('#grd_zitsir').datagrid("getRowIndex", row);
				$('#grd_zitsir').datagrid('endEdit',rowIndex);
			}

			// 店舗一覧の入力編集を終了する。
			var row = $('#grd_fstenpo').datagrid("getSelected");
			if(row){
				var rowIndex = $('#grd_fstenpo').datagrid("getRowIndex", row);
				$('#grd_fstenpo').datagrid('endEdit',rowIndex);
			}

			var sircd = $('#'+$.id_inp.txt_sircd).numberbox('getValue');	// 仕入先コード
			if(sircd && sircd !== ''){
				var msgid = that.checkInputboxFunc($.id_inp.txt_sircd, sircd , '');
				if(msgid !==null){
					$.showMessage(msgid);
					return false;
				}

				if(that.reportYobiInfo()==='2'){
					msgid = that.checkInputboxFunc($.id_inp.txt_sircd + '_notExist', sircd , '');
					if(msgid !==null){
						$.showMessage(msgid, ['複数仕入先_実仕入先マスタに、仕入先コード']);
						return false;
					}
				}else if(that.reportYobiInfo()==='0'){
					if(that.queried){
						var param = {};
						param["KEY"] =  "MST_CNT";
						param["value"] = sircd;
						var chk_cnt = $.getInputboxData(that.name, $.id.action_check, $.id_inp.txt_rsircd, [param]);
						if(chk_cnt==="" || chk_cnt==="0"){
							$.showMessage('EX1100', ['仕入先コード','複数仕入先_実仕入先マスタ']);
							return false;
						}
					}
				}
			}

			// グリッド項目：実仕入先
			//var targetRows = that.getGridData(txt_sircd, 'grd_zitsir')['grd_zitsir'];
			var rowsZitsir= $('#grd_zitsir').datagrid('getRows');
			var rsircds	= [];
			for (var i=0; i<rowsZitsir.length; i++){
				if(rowsZitsir[i]['RSIRCD']){
					if(rowsZitsir[i]['RSIRCD'] !== '' ){
						if(rowsZitsir[i]['SIRKN_R'] ? "" : rowsZitsir[i]['SIRKN_R'] === '' ){
							// 仕入先名称が空の時
							$.showMessage('E11099');
							return false;

						}
					}

					// 実仕入先コード一覧に、検索条件部の仕入先コードが入力されている場合。
					/*if(sircd && sircd !== ''){
						if(Number(rowsZitsir[i]['RSIRCD']) == Number(sircd)){
							$.showMessage('E11081',['仕入先コードが']);
							return false;
						}
					}*/
					rsircds.push(rowsZitsir[i]["RSIRCD"]);
				}
			}

			// 重複チェック：実仕入先コード
			var rsircd = rsircds.filter(function (element, index, self) { return self.indexOf(element)*1 === index; });
			if(rsircds.length !== rsircd.length){
				$.showMessage('EX1117');
				return false;
			}


			// 仕入先コード
			//var txt_sircd = $.getInputboxValue($('#'+$.id_inp.txt_sircd));
			var txt_sircd = $('#'+$.id_inp.txt_sircd).textbox('getValue');

			// 入力データ：実仕入先一覧
			var targetRowsZitsir = $('#grd_zitsir').datagrid('getRows');

			// 入力データ：複数仕入先店舗一覧
			var targetRowsFsirt = $('#grd_fstenpo').datagrid('getRows');

			var seqnos	 = [];					// 対象店舗フラグ
			if(targetRowsFsirt){
				for (var i=0; i<targetRowsFsirt.length; i++){
					if(targetRowsFsirt[i]['SEQNO']){
						seqnos.push(targetRowsFsirt[i]['SEQNO'])
					}
				}
			}

			var gyonos	 = [];					// 行No
			if(targetRowsZitsir){
				for (var i=0; i<targetRowsZitsir.length; i++){
					if(targetRowsZitsir[i]['RSIRCD']){
						gyonos.push(targetRowsZitsir[i]['IDX'])
					}
				}
			}

			/*if(txt_sircd === ""){
				alert('検索条件に仕入先コードを入力してください')
				return false;
			}*/

			// 整合性チェック：対象フラグで入力されている数値の行の仕入先は入力必須。
			if(seqnos.length !== 0){
				if(gyonos.length === 0){
					$.showMessage('E11001');
					return false;

				}else{
					for (var i=0; i<seqnos.length; i++){
						if($.inArray(seqnos[i], gyonos)===-1){
							$.showMessage('E11001');
							return false;
						}
					}
				}
			}



			// 入力チェック用の配列をクリア
			that.jsonTemp = [];

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

			var txt_sircd = $('#'+$.id_inp.txt_sircd).textbox('getValue');
			var newrows = that.getGridData(txt_sircd, 'grd_zitsir')['grd_zitsir'];		// 変更データ
			var oldrows = that.grd_zitsir_data

			var txt_sircd = $.getInputboxValue($('#'+$.id_inp.txt_sircd));
			var targetRowsZitsir	 = that.getMergeGridDate(txt_sircd, 'grd_zitsir');
			var targetRowsFsirt		 = that.getMergeGridDate(txt_sircd, 'grd_fstenpo');
			//var targetRowsZitsir = that.getGridData(txt_sircd, 'grd_zitsir')['grd_zitsir']
			//var targetRowsFsirt	 = that.getGridData(txt_sircd, 'grd_fstenpo')['grd_fstenpo']

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
					YOBINFO:		that.reportYobiInfo(),
					SIRCD:			txt_sircd,
					DATA:			JSON.stringify(targetDatas),		// 更新対象情報
					DATA_ZITSIR:	JSON.stringify(targetRowsZitsir),	// 個別データグリッド:実仕入先一覧
					DATA_FSIRT:		JSON.stringify(targetRowsFsirt),	// 個別データグリッド:複数仕入先店舗一覧
					t:				(new Date()).getTime()
				},
				function(data){
					// 検索処理エラー判定
					if($.updError(id, data)) return false;

					var afterFunc = function(){
						// 初期化
						$.setInputBoxDisable($($.id.hiddenChangedIdx));
						that.onChangeReport = true;
						that.changeReport(that.name, 'btn_return');
					};
					$.updNormal(data, afterFunc);

					// マスク削除
					$.removeMaskMsg();

					// ログ出力
					$.log(that.timeData, 'loaded:');
				}
			);

			// 保持データを更新する
			// 複数仕入先
			var gridData = that.getGridData(txt_sircd, 'grd_zitsir');
			that.setGridData(gridData, 'grd_zitsir');
			// 複数店舗一覧
			var gridData = that.getGridData(txt_sircd, 'grd_fstenpo');
			that.setGridData(gridData, 'grd_fstenpo');
		},
		delValidation: function (){	// （必須）批准
			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = true;

			// 仕入先コード
			var txt_sircd = $.getInputboxValue($('#'+$.id_inp.txt_sircd));

			if(txt_sircd === ""){
				// TODO
				alert('検索条件に仕入先コードを入力してください')
				return false;

			}

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

			var txt_sircd		= $('#'+$.id_inp.txt_sircd).textbox('getValue');		// 仕入先コード

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
					SIRCD:			txt_sircd,						// 仕入先コード
					//IDX:			$($.id.hiddenChangedIdx).val(),	// 更新対象Index
					//DATA:			JSON.stringify(targetRows),		// 更新対象情報
					DATA:			JSON.stringify(targetDatas),	// 更新対象情報
					t:				(new Date()).getTime()
				},
				function(data){
					// 検索処理エラー判定
					if($.delError(id, data)) return false;

					var afterFunc = function(){
						// 初期化
						$.setInputBoxDisable($($.id.hiddenChangedIdx));
						that.jsonString = [];
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
			// 仕入先コード
			this.jsonTemp.push({
				id:		$.id_inp.txt_sircd,
				value:	$('#'+$.id_inp.txt_sircd).textbox('getValue'),
				text:	$('#'+$.id_inp.txt_sircd).textbox('getText')
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
		getMergeGridDate: function(sircd, target){
			// 保持したデータと入力データ比較を比較する。
			var that = this;

			var newrows = that.getGridData(sircd, target)[target];		// 変更データ
			//var oldrows = [];
			var targetRows= [];

			if(target===undefined || target==='grd_zitsir'){
				var oldpk = [];
				//oldrows = that.grd_zitsir_data

				for (var i=0; i<newrows.length; i++){
					if((newrows[i]['F3'] ? newrows[i]['F3'] : "") !== "" ){
						var rowDate = {
								F1	 : newrows[i]["F1"],
								F2	 : newrows[i]["F2"],
								F3	 : newrows[i]["F3"],
								//F4	 : '1',
						};
						if(rowDate){
							targetRows.push(rowDate);
						}
					}
					/*if((oldrows[i]['F3'] ? oldrows[i]['F3'] : "") !== (newrows[i]['F3'] ? newrows[i]['F3'] : "")){
						if((newrows[i]['F3'] ? newrows[i]['F3'] : "") === "" && (oldrows[i]['F3'] ? oldrows[i]['F3'] : "") !== ""){
							// 新規入力データが空で、初期検索データが空でない場合。
							var rowDate = {
									F1	 : oldrows[i]["F1"],
									F2	 : oldrows[i]["F2"],
									F3	 : oldrows[i]["F3"],
									F4	 : '1',
							};
						}else if(newrows[i]['F3'] ? newrows[i]['F3'] : "" !== ""){
							// 新規入力データが空でない場合。
							var rowDate = {
									F1	 : newrows[i]["F1"],
									F2	 : newrows[i]["F2"],
									F3	 : newrows[i]["F3"],
									F4	 : '0',
							};
						}
						if(rowDate){
							targetRows.push(rowDate);
						}
					}*/
				}
			}

			if(target===undefined || target==='grd_fstenpo'){
				//var oldpk = [];
				//oldrows = that.grd_fsirt_data
				for (var i=0; i<newrows.length; i++){
					if((newrows[i]['F3'] ? newrows[i]['F3'] : "") !== "" ){
						var rowDate = {
								F1	 : newrows[i]["F1"],
								F2	 : newrows[i]["F2"],
								F3	 : newrows[i]["F3"],
						};
						if(rowDate){
							targetRows.push(rowDate);
						}
					}

					/*if((oldrows[i]['F3'] ? oldrows[i]['F3'] : "") !== (newrows[i]['F3'] ? newrows[i]['F3'] : "")){
						if((newrows[i]['F3'] ? newrows[i]['F3'] : "") === "" && (oldrows[i]['F3'] ? oldrows[i]['F3'] : "") !== ""){
							var rowDate = {
									F1	 : newrows[i]["F1"],
									F2	 : newrows[i]["F2"],
									F3	 : '0',
							};
						}else if(newrows[i]['F3'] ? newrows[i]['F3'] : "" !== ""){
							var rowDate = {
									F1	 : newrows[i]["F1"],
									F2	 : newrows[i]["F2"],
									F3	 : newrows[i]["F3"],
							};
						}
						if(rowDate){
							targetRows.push(rowDate);
						}
					}*/
				}
			}
			return targetRows;
		},
		setDefaultDate: function(){
			// 編集前のグリッドデータを保持する。
			var that = this;
			var enptyrows = []

			// 編集前データ保持：実仕入先一覧
			if($('#grd_zitsir').datagrid('getRows')){
				that.grd_zitsir_data	 =  $('#grd_zitsir').datagrid('getRows');
			}else{
				that.grd_zitsir_data	 =  enptyrows;
			}

			// 編集前データ保持：実仕入先一覧
			if($('#grd_fstenpo').datagrid('getRows')){
				that.grd_fsirt_data	 =  $('#grd_fstenpo').datagrid('getRows');
			}else{
				that.grd_fsirt_data	 =  enptyrows;
			}
		},
		setEditableGrid: function(that, reportno, id){		// データ表示
			var that = this;
			/*var parser= function(value){
				if (undefined===value) return '';
				if (value===null) return '';
				if (value==='') return '';
				if (! (''+value).match(/^[0-9]*$/)) return '';
				return value*1;
			};
			var check = $('#txt_tencd').attr("check") ? JSON.parse('{'+$('#'+id).attr("check")+'}'): JSON.parse('{}');
			var formatter = function(value){
				return $.paddingLeft(value, check.maxlen);
			};*/

			var funcClickRow = $.fn.datagrid.defaults.onClickRow;
			var funcBeginEdit= $.fn.datagrid.defaults.onBeginEdit;
			var funcEndEdit= $.fn.datagrid.defaults.onEndEdit;
			//var funcAfterEdit= $.fn.datagrid.defaults.onAfterEdit;

			if(that.reportYobiInfo()!=='1'){
				that.editRowIndex[id] = -1;
				funcClickRow = function(index,field){$.clickEditableDatagridCell(that,id, index)};
				funcBeginEdit = function(index,row){$.beginEditDatagridRow(that,id, index, row)};
				funcEndEdit = function(index,row,changes){$.endEditDatagridRow(that, id, index, row);};


				// 編集可能データグリッドの共通処理設定
				// 編集エディターの機能拡張（非表示で参照用のオブジェクトが準備してある前提、参照オブジェクトはグリッドのフィールド名を元に判断）
				$.extendDatagridEditor(that);
			}

			//that.editRowIndex[id] = -1;
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
					//var json = that.getGridParams(that, id);
					var txt_sircd = $.getInputboxValue($('#'+$.id_inp.txt_sircd));

					var json = [{"callpage":"Out_Reportx161","SIRCD":txt_sircd}];
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
					if(that.reportYobiInfo()==='2'){
						that.queried = true;
					}
				},
				onClickRow: funcClickRow,
				onBeginEdit:funcBeginEdit,
				onEndEdit: funcEndEdit,
				//onClickRow: function(index,field){$.clickEditableDatagridCell(that,id, index)},
				//onBeginEdit:function(index,row){$.beginEditDatagridRow(that,id, index, row)},
				//onEndEdit: function(index,row,changes){$.endEditDatagridRow(that, id, index, row)}
			});
		},
		getGridData: function (sircd, target){
			var that = this;

			var data = {};
			var targetRows= [];

			// 実仕入先一覧
			if(target===undefined || target==='grd_zitsir'){
				var rowsZitsir= $('#grd_zitsir').datagrid('getRows');
				for (var i=0; i<rowsZitsir.length; i++){
					var rowDate = {
							F1	 : sircd,
							F2	 : rowsZitsir[i]["IDX"],
							F3	 : rowsZitsir[i]["RSIRCD"],
					};
					targetRows.push(rowDate);
				}
				data['grd_zitsir'] = targetRows;
			}
			if(target===undefined || target==='grd_fstenpo'){
				var rowsFsirt= $('#grd_fstenpo').datagrid('getRows');
				for (var i=0; i<rowsFsirt.length; i++){
					var rowDate = {
							F1	 : sircd,
							F2	 : rowsFsirt[i]["TENCD"],
							F3	 : rowsFsirt[i]["SEQNO"],
					};
					targetRows.push(rowDate);
				}
				data['grd_fstenpo'] = targetRows;
			}
			return data;
		},
		setGridData: function (data, target){
			var that = this;

			// 実仕入先一覧
			if(target===undefined || target==='grd_zitsir'){
				that.grd_zitsir_data =  data['grd_zitsir'];
			}

			// 複数仕入先店舗
			if(target===undefined || target==='grd_fstenpo'){
				that.grd_fsirt_data =  data['grd_fstenpo'];
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

			// 呼出別処理
			switch (btnId) {
			case $.id.btn_back:
			case $.id.btn_cancel:
				// 転送先情報
				childurl = parent.$('#hdn_menu_path').val();

				break;
			case "btn_return":
				// 転送先情報
				if(that.reportYobiInfo()==='2'){
					index = 1;
				}else if(that.reportYobiInfo()==='0'){
					index = 2;
				}else if(that.reportYobiInfo()==='1'){
					index = 3;
				}
				sendMode = 2;
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
		changeInputboxFunc:function(that, id, newValue, obj){

			var parentObj = $('#'+that.focusRootId);
			var msgParam  = [];
			var func_focus = function(){setTimeout(function(){
				var target = $.getInputboxTextbox($('#'+id));
				target.focus();
			},50);};
			if(id+"_"===obj.attr('id') && that.focusGridId!==""){
				parentObj = $('#'+that.focusGridId).datagrid('getPanel');
				func_focus = function(){setTimeout(function(){
					var target = $.getInputboxTextbox($('#'+id+'_'));
					target.focus();
				},50);};
			}

			/*var parentObj = $('#'+that.focusRootId);
			if(id+"_"===obj.attr('id') && that.focusGridId!==""){
				parentObj = $('#'+that.focusGridId).datagrid('getPanel');
			}*/

			// DB問い合わせ系
			/*if($('[for_inp^='+id+'_]').length > 0){
				var param = that.getInputboxParams(that, id, newValue);
				$.getsetInputboxRowData(that.name, 'for_inp', id, param, that, parentObj);
			}*/

			// DB問い合わせ系
			if($('[for_inp^='+id+'_]').length > 0){
				var param = that.getInputboxParams(that, id, newValue);
				var rows = $.getSelectListData(that.name, $.id.action_change, id, param);
				var row = rows.length > 0 ? rows[0]:"";
				$.setInputboxRowData('for_inp', id, row, that, parentObj);
			}

			// 検索、入力後特殊処理
			//if(that.queried){
			if(true){
				var msgid = null;

				// 仕入先コード
				if(id===$.id_inp.txt_sircd || id===$.id_inp.txt_rsircd){
					if(newValue !==''){
						// 値が空文字の場合は確認しない。
						msgid = that.checkInputboxFunc(id, newValue, '');
					}

					if(id===$.id_inp.txt_sircd){
						if(that.reportYobiInfo()==='2'){
							if(msgid == null){
								msgid = that.checkInputboxFunc(id + '_notExist', newValue, '');
								msgParam = ['複数仕入先_実仕入先マスタに、仕入先コード'];
							}
						}
					}
				}

				// 実仕入先コード一覧
				/*if(that.focusGridId === 'grd_zitsir' && id===$.id_inp.txt_rsircd){
					var rows = $('#'+that.focusGridId).datagrid('getRows');
					if(rows){
						for (var i=0; i<rows.length; i++){
							if(rows[i]['RSIRCD']){
								if(String(newValue) === rows[i]['RSIRCD']){
									$.showMessage('E11081');
								}
							}
						}
					}
				}*/

				if(msgid !==null){
					if(msgParam.length > 0){
						$.showMessage(msgid, msgParam, func_focus );
					}else{
						$.showMessage(msgid, undefined, func_focus );
						return false;
					}
				}
			}
		},
		// IDとvalueでチェック処理を実施
		checkInputboxFunc:function(id, newValue, kbn, record, isNew){
			var that = this;

			// 仕入先コード
			if(id===$.id_inp.txt_sircd || id===$.id_inp.txt_rsircd){
				var param = {};
				param["KEY"] =  "MST_CNT";
				param["value"] = newValue;
				var chk_cnt = $.getInputboxData(that.name, $.id.action_check, $.id_inp.txt_sircd, [param]);
				if(chk_cnt==="" || chk_cnt==="0"){
					return "E11099";
				}
			}

			if(id===$.id_inp.txt_sircd + '_notExist'){
				// 複数仕入先_実仕入先マスタに既存データ存在する場合はエラー
				var param = {};
				param["KEY"] =  "MST_CNT";
				param["value"] = newValue;
				var chk_cnt = $.getInputboxData(that.name, $.id.action_check, $.id_inp.txt_rsircd, [param]);
				if(Number(chk_cnt) > 0){
					return "E00004";
				}
			}
			return null;
		},
		getInputboxParams: function(that, id, newValue){
			// 情報取得
			var values = {};
			values["value"] = newValue;
			values["TABLEKBN"] = that.baseTablekbn;

			// 仕入先
			if(id===$.id_inp.txt_sircd){
				values["SIRCD"] = $.getInputboxValue($('#'+$.id_inp.txt_sircd));
			}
			// 実仕入先
			if(id===$.id_inp.txt_rsircd){
				values["RSIRCD"] = $.getInputboxValue($('#'+$.id_inp.txt_rsircd));
			}

			// 情報設定
			return [values];
		},
		keyEventInputboxFunc:function(e, code, that, obj){

			var id = $(obj).attr("id");

			// *** Enter or Tab ****
			if(code === 13 || code === 9){
				// 仕入先コード
				if(id===$.id.btn_search){
					// 検索ボタン押下
					$('#'+$.id.btn_search).trigger('click');
					e.preventDefault();
					return false;

				}
			}
		}
	} });
})(jQuery);