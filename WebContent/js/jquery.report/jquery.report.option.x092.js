/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
			name:		'Out_Reportx092',			// （必須）レポートオプションの確認
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
			dedefaultObjNum:	31,	// 初期化オブジェクト数
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
			grd_genryo_data:[],					// グリッド情報
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

				// 個別レイアウト調整：単品管理区分
				$('#'+$.id_mei.kbn508).combobox({panelWidth:200,})

				// 検索実行
				that.onChangeReport = true;

				// 部門コード
				$.setInputbox(that, reportno, 'txt_shncd_M', isUpdateReport);
				$.setInputbox(that, reportno, 'txt_shnkn_M', isUpdateReport);
				$.setInputbox(that, reportno, 'txt_naikn_M', isUpdateReport);
				$.setInputbox(that, reportno, 'txt_genka_M', isUpdateReport);
				$.setInputbox(that, reportno, 'txt_budomari_M', isUpdateReport);
				$.setInputbox(that, reportno, 'txt_genkakei_M', isUpdateReport);
				$.setInputbox(that, reportno, 'txt_sirkn_M', isUpdateReport);
				$.setInputbox(that, reportno, 'chk_del_M', isUpdateReport);
				var count = 2;
				// 名称マスタ参照系
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
//				$('#'+$.id_inp.txt_shncd).textbox({
//					  onChange: function(newValue, oldValue){
//						that.changeInputboxFunc(that, $.id_inp.txt_sircd, newValue, $('#'+$.id_inp.txt_sircd))
//					 }
//				});
				$.extendDatagridEditor(that);
				that.setEditableGrid(that, reportno,$.id.grd_genryo );

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
				$('#'+$.id.btn_addline).on("click", that.addLine);
				// 新規：新規ボタン押下時
				if(that.sendBtnid===$.id.btn_new){
					$.initReportInfo("KK002", "値付器マスタ　新規", "登録");
					$('#'+$.id.btn_del).hide();
					$('#'+$.id.btn_del).attr('disabled', 'disabled').hide();
					$("#disp_record_info").hide();
					that.judgeRepType.sei_new = true;
					// 変更：検索・変更ボタン押下時
				}else if(that.sendBtnid===$.id.btn_search||that.sendBtnid===$.id.btn_sel_change){
					$.setInputBoxDisable($("#"+$.id_inp.txt_bmncd));
					$.setInputBoxDisable($("#"+$.id_inp.txt_callcd));
					$.initReportInfo("KK004", "値付器マスタ　変更", "変更");
				}

				if(that.reportYobiInfo()==='1'){
					$('#'+that.focusRootId).find('[id^=btn_]').each(function(){
						$(this).linkbutton('disable');
						$(this).attr('disabled', 'disabled').hide();
					});
					$('#'+js.focusRootId).find('.easyui-combobox').each(function(){
						$($(this).combobox('textbox')).attr('tabindex', -1).attr('readonly', 'readonly');
						$(this).attr('tabindex', -1).combobox('disable');
					});
					$('#'+js.focusRootId).find('.easyui-textbox').each(function(){
						$($(this).textbox('textbox')).attr('tabindex', -1).attr('readonly', 'readonly');
						$(this).attr('tabindex', -1).textbox('disable');
					});
					$('#'+js.focusRootId).find('.easyui-numberbox').each(function(){
						$($(this).numberbox('textbox')).attr('tabindex', -1).attr('readonly', 'readonly');
						$(this).attr('tabindex', -1).numberbox('disable');
					});

				}else{
					$($.id.buttons).show();
					// 各種遷移ボタン
					$('#'+$.id.btn_new).on("click", $.pushChangeReport);
					$('#'+$.id.btn_copy).on("click", $.pushChangeReport);
					$('#'+$.id.btn_cancel).on("click", $.pushChangeReport);
				}
				// 変更
				$($.id.hiddenChangedIdx).val('');
			},
			judgeRepType: {
				sei_new 		: false,	// 正 -新規
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
				var txt_bmncd		= $.getJSONValue(this.jsonHidden, $.id_inp.txt_bmncd)	// 部門コード
				var txt_callcd		= $.getJSONValue(this.jsonHidden, $.id_inp.txt_callcd)	// 呼出コード
				var txt_shncd		= $.getJSONValue(this.jsonHidden, $.id_inp.txt_shncd)	// 商品コード
				// 処理時間計測用
				that.timeData = (new Date()).getTime();
				// Loading表示
				$.appendMaskMsg();

				$.post(
						$.reg.jqgrid ,
						{
							report:			 that.name,		// レポート名
							TXT_CALLCD:		 txt_callcd,
							TXT_BMNCD: 		 txt_bmncd,
							TXT_SHNCD:		 txt_shncd,
							SENDBTNID:		 that.sendBtnid,
							t:				 (new Date()).getTime(),
							sortable:		 sortable,
							sortName:		 that.sortName,
							sortOrder:		 that.sortOrder,
							rows:			 1	// 表示可能レコード数
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
							that.setEditableGrid(that, reportno, $.id.grd_genryo);
							// 状態保存
							$.saveState2(reportno, that.getJSONString());

							$.win001.init(that);	// 仕入先
							$.win002.init(that);	// メーカー
							$.win006.init(that);	// 商品コード

							// ログ出力
							$.log(that.timeData, 'loaded:');
						}
				);
			},
			isEmptyVal: function (val, zeroEmpty){
				if(val === undefined){
					return true;
				}
				if(val.length === 0){
					return true;
				}
				if(zeroEmpty === true && val&""==="0"){
					return true;
				}
				return false;
			},


			updValidation: function (){	// （必須）批准
				var that = this;
				var row = $('#'+$.id.grd_genryo).datagrid("getSelected");
				var rowIndexr = $('#'+$.id.grd_genryo).datagrid("getRowIndex", row);
				$('#'+$.id.grd_genryo).datagrid('endEdit',rowIndexr);
				var isNew = that.judgeRepType.sei_new;
				var param = {};
				// EasyUI のフォームメソッド 'validate' 実施
				var rt = $($.id.toolbarform).form('validate');
				if(!rt){
					$.addErrState(that, $('.validatebox-invalid').eq(0), false);
					return rt;
				}

				var targetdate = [];

				var targetRows = $('#'+$.id.grd_genryo).datagrid('getRows');
				for (var i=0; i<targetRows.length; i++){
					if(targetRows[i]["F1"]){
						targetdate.push(targetRows[i]["F1"]);
					}
				}

				// 重複チェック：商品コード
				var targetdateF = targetdate.filter(function (element, index, self) { return self.indexOf(element)*1 === index; });
				if(targetdate.length !== targetdateF.length){
					$.showMessage('EX1022');
					return false;
				}

				var gridData01 = that.getGridData( "grd_data");
				var inpdata = gridData01[0];
				var txt_shncd = inpdata["F3"];
				var txt_shncd_new = txt_shncd;

				// 新規(正) 1.3　取得された商品コードが商品マスタテーブルに存在しない場合、エラー。
				var txt_shncd_chk = $.getInputboxData(that.name, $.id.action_check,  $.id_inp.txt_shncd, [{KEY:"MST_CNT",value:txt_shncd_new}]);
				if(txt_shncd_chk == "0"){
					$.showMessage('E20160');
					return false;
				}

				// 存在チェック:部門コード
				var bmncd = $.getInputboxValue($('#'+$.id_inp.txt_bmncd));
				if(bmncd || bmncd!==''){
					var msgid = that.checkInputboxFunc($.id_inp.txt_bmncd, bmncd , '');
					if(msgid !==null){
						$.showMessage(msgid);
						return false;
					}
				}

				for (var i=0; i<targetRows.length; i++){
					if(targetRows[i]["F1"] && targetRows[i]["F1"] !== '' && targetRows[i]["F9"]=='0'){
						// グリッド内の商品コードがマスタに存在しない値の場合エラー
						$.showMessage('E20160');
						return false;
					}

					if(!targetRows[i]["F1"] && targetRows[i]["F1"] == ''){
						if((targetRows[i]["F3"] && targetRows[i]["F3"] !== '')
						|| (targetRows[i]["F4"] && targetRows[i]["F4"] !== '')
						|| (targetRows[i]["F5"] && targetRows[i]["F5"] !== '')
						|| (targetRows[i]["F6"] && targetRows[i]["F6"] !== '')
						|| (targetRows[i]["F8"] && targetRows[i]["F8"] !== '0')){
							// 商品コードの入力がない場合に、他の項目に入力がある場合はエラー
							$.showMessage('EX1047', ['商品コード']);
							return false;
						}
					}
				}
				if(isNew){
					param["KEY"] =  "MST_CNT",
					param["BMNCD"] = $.getInputboxValue($('#'+$.id_inp.txt_bmncd));
					param["CALLCD"] = $.getInputboxValue($('#'+$.id_inp.txt_callcd));
					var netsukeCheck = $.getInputboxData(that.name, $.id.action_check,  "MSTNETSUKE", [param]);
					if(netsukeCheck!="0"){
						$.showMessage('E00004');
						return false;
					}

				}

				return rt;
			},
			updSuccess: function(id){	// validation OK時 の update処理
				var that = this;

				var txt_callcd	 = $.getInputboxValue($('#'+$.id_inp.txt_callcd));	// 呼出コード
				var txt_bmncd	 = $.getInputboxValue($('#'+$.id_inp.txt_bmncd));	// 部門コード

				// 変更行情報取得
				var changedIndex = $($.id.hiddenChangedIdx).val().split(",");

				var targetDatas = that.getGridData( "grd_data");
				var gridData = that.getGridData($.id.grd_genryo);


				// 計量風袋
				// 処理時間計測用
				that.timeData = (new Date()).getTime();
				// Loading表示
				$.appendMaskMsg();

				$.post(
						$.reg.jqgrid ,
						{
							report:			that.name,		// レポート名
							action:			$.id.action_update,	// 実行処理情報
							obj:			id,								// 実行オブジェクト
							SENDBTNID:		that.sendBtnid,
							BMNCD:			txt_bmncd,
							CALLCD:			txt_callcd,
							DATA:			JSON.stringify(targetDatas),	// 更新対象情報
							GENRYO_DATA:	JSON.stringify(gridData),		// 使用原料
							t:				(new Date()).getTime()
						},
						function(data){
							// 検索処理エラー判定
							if($.updError(id, data)) return false;

							var afterFunc = function(){
								// 初期化
								that.clear();
								that.changeReport(that.name, 'btn_return');
							};
							$.updNormal(data, afterFunc);

							// ログ出力
							$.log(that.timeData, 'loaded:');

						}
				);
			},
			getGridData: function ( target){
				var that = this;

				var data = {};

				// 基本情報
				if(target===undefined || target==="grd_data"){
					var targetData = [{}];
					var targetData2 = [];
					$('#'+that.focusRootId).find('[col^=F]').each(function(){
						var col = $(this).attr('col');
						targetData[0][col] = $.getInputboxValue($(this));
					});

					var rowData = {
							F1  : targetData[0]["F1"],		// F1	部門コード
							F2  : targetData[0]["F2"],		// F2	呼出コード
							F3  : targetData[0]["F3"],		// F3	商品コード
							F4  : targetData[0]["F5"],		// F4	商品名上段
							F5  : targetData[0]["F6"],		// F5	商品名下段
							F6  : targetData[0]["F15"],		// F6	生鮮・加工食品区分
							F7  : targetData[0]["F17"],		// F7	定貫・不定貫区分
							F8  : targetData[0]["F23"],		// F8	内容量
							F9  : targetData[0]["F14"],		// F9	使用トレイ
							F10 : targetData[0]["F16"],		// F10	梱包
							F11 : targetData[0]["F18"],		// F11	風袋
							F12 : targetData[0]["F20"],		// F12	下限重量
							F13 : targetData[0]["F22"],		// F13	上限重量
							F14 : targetData[0]["F30"],		// F13	上限重量
					};
					targetData2.push(rowData);
					data = targetData2;
				}

				// 使用原料マスタ
				if(target===undefined || target==="grd_genryo"){
					var targetRowsGenryo= [];
					var rowsGenryo	 = $('#'+$.id.grd_genryo).datagrid('getRows');
					var txt_callcd	 = $.getInputboxValue($('#'+$.id_inp.txt_callcd));	// 呼出コード
					var txt_bmncd	 = $.getInputboxValue($('#'+$.id_inp.txt_bmncd));	// 部門コード
					

					for (var i=0; i<rowsGenryo.length; i++){
						if((rowsGenryo[i]["F1"] && rowsGenryo[i]["F1"] != '')){
							var rowData = {
									F1 : rowsGenryo[i]["F1"],		// F1	商品コード
									F2 : txt_callcd,				// F2	商品コード
									F3 : txt_bmncd,					// F3	呼出コード
									F4 : rowsGenryo[i]["F3"],		// F4	部門コード
									F5 : rowsGenryo[i]["F4"],		// F5	原価原料
									F6 : rowsGenryo[i]["F5"],		// F6	歩留り
									F7 : rowsGenryo[i]["F6"],		// F7	原価小計
									F8 : rowsGenryo[i]["F8"],   //F8 削除
							};
							targetRowsGenryo.push(rowData);
						}
					}
					data = targetRowsGenryo;
				}

				return data;
			},
			delValidation: function (){	// （必須）批准
				var that = this;
				// EasyUI のフォームメソッド 'validate' 実施
				var rt = true;
				return rt;
			},
			delSuccess: function(id){
				var that = this;
				var is_warning = false;

				var targetDatas = [{}];
				$('#'+that.focusRootId).find('[col^=F]').each(function(){
					var forId = $(this).attr('col');
					targetDatas[0][forId] = $.getInputboxValue($(this));
				});
				var txt_bmncd		= $.getJSONValue(this.jsonHidden, $.id_inp.txt_bmncd)	// 部門コード
				var txt_callcd		= $.getJSONValue(this.jsonHidden, $.id_inp.txt_callcd)	// 呼出コード
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
							BMNCD:			txt_bmncd,
							CALLCD:			txt_callcd,
							DATA:			JSON.stringify(targetDatas),	// 更新対象情報
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
				//部門コード
				this.jsonTemp.push({
					id:		$.id_inp.txt_bmncd,
					value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_bmncd),
					text:	''
				});
				//呼出コード
				this.jsonTemp.push({
					id:		$.id_inp.txt_callcd,
					value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_callcd),
					text:	''
				});
				//商品コード
				this.jsonTemp.push({
					id:		$.id_inp.txt_shncd,
					value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_shncd),
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
			extenxDatagridEditorIds:{
				F1		: "txt_shncd_M",
				F2		: "txt_shnkn_M",
				F3		: "txt_naikn_M",
				F4		: "txt_genka_M",
				F5		: "txt_budomari_M",
				F6		: "txt_genkakei_M",
				F7		: "txt_sirkn_M",
				F8		: "chk_del_M",
			},
			setEditableGrid: function(that, reportno, id){		// 一覧
				that.editRowIndex[id] = -1;
				var cformatter =function(value,row,index){return value && value===$.id.value_on?$.id.text_on:$.id.text_off;};
				var cstyler =function(value,row,index){return 'color: red;font-weight: bold;';};
				var index = -1;
				var shnFormatter = function(value,row,index){
					if(row.F1 === '合計'){
						return row.F1;
					}else{
						return $.getFormatPrompt(row.F1, '####-####');
					}
				}
				var pomptFormatter =function(value){return $.getFormatPrompt(value, '####-####');};

				if (that.updData === "") {
					that.updData = that.getGridData($.id.grd_genryo);
				}
				$('#'+id).datagrid({
					url:$.reg.easy,
					frozenColumns:[[
					            {field:'F8',		title:'削除',			width: 40,halign:'center',align:'center',	formatter:cformatter,	editor:{type:'checkbox'},	styler:cstyler},
					            {field:'F1',		title:'商品コード',		width: 80 ,halign:'center',align:'left',editor:{type:'numberbox'},formatter:shnFormatter},
								]],
					columns:[[
								{field:'F2',		title:'商品名',			width: 300 ,halign:'center',align:'left',editor:{type:'textbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:true}}},
								{field:'F3',		title:'内容量',			width: 290 ,halign:'center',align:'left',editor:{type:'textbox'}},
								{field:'F4',		title:'原料原価',		width: 90 ,halign:'center',align:'right',editor:{type:'numberbox'},formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0');}},
								{field:'F5',		title:'歩留り',			width: 70 ,halign:'center',align:'right',editor:{type:'numberbox'},formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0');}},
								{field:'F6',		title:'原価小計',		width: 90 ,halign:'center',align:'right',editor:{type:'numberbox'},formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0');}},
								{field:'F7',		title:'仕入先',			width: 300 ,halign:'center',align:'left',editor:{type:'textbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:true}}},
							]],
					showFooter: true,
					onBeforeLoad:function(param){
						index = -1;
						var values = {};
						var txt_bmncd		= $.getJSONValue(that.jsonHidden, $.id_inp.txt_bmncd)	// 部門コード
						var txt_callcd		= $.getJSONValue(that.jsonHidden, $.id_inp.txt_callcd)	// 部門コード
						var txt_shncd		= $.getJSONValue(that.jsonHidden, $.id_inp.txt_shncd)	// 部門コード
						//var sircd	 = $('#'+$.id_inp.txt_sircd).numberbox('getValue');		// 企画No

						values["callpage"]	 = $($.id.hidden_reportno).val()				// 呼出元レポート名
						values["BMNCD"]		 = txt_bmncd										// 企画No
						values["CALLCD"]	= txt_callcd										// 企画No
						values["SHNCD"]		 = txt_shncd										// 企画No
						//var json = that.getGridParams(that, id);

						var json = [values];
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
					},
					onClickRow: function(index,field){$.clickEditableDatagridCell(that,id, index)},
					onBeginEdit:function(index,row){$.beginEditDatagridRow(that,id, index, row)},
					onEndEdit: function(index,row,changes){
						$.endEditDatagridRow(that, id, index, row)
						var txt_shncd_chk = $.getInputboxData(that.name, $.id.action_check,  $.id_inp.txt_shncd, [{KEY:"MST_CNT",value:row.F1}]);
						if(txt_shncd_chk==0){
							$('#'+$.id.grd_genryo).datagrid('updateRow',{ index: index,	row:{F1:row.F1,F2:row.F2,F3:row.F3,F4:row.F4,F5:row.F5,F6:row.F6,F7:row.F7,F8:row.F8,F9:'0'}});
						}else{
							$('#'+$.id.grd_genryo).datagrid('updateRow',{ index: index,	row:{F1:row.F1,F2:row.F2,F3:row.F3,F4:row.F4,F5:row.F5,F6:row.F6,F7:row.F7,F8:row.F8,F9:'1'}});
						}

							var rows = $('#'+$.id.grd_genryo).datagrid('getRows');
							var toatlA = 0;
							var toatlB = 0;
							var toatlC = 0;

							for (var i=0; i<rows.length; i++){
								var shnCheck = rows[i]["F9"];
								var delM = rows[i]["F8"];

								if(shnCheck=="1"&&delM!="1"){
									toatlA = Number(toatlA)+Number(rows[i]["F4"]);
									toatlB = Number(toatlB)+Number(rows[i]["F5"]);
									toatlC = Number(toatlC)+Number(rows[i]["F6"]);
								}
							}
							$('#'+$.id.grd_genryo).datagrid('reloadFooter',[{ F1:"合計",F2:"",F3:"",F4:toatlA,F5:toatlB,F6:toatlC,F7:''}]);
						}
				});
				var rows = $('#'+$.id.grd_genryo).datagrid('getRows');
				var toatlA = 0;
				var toatlB = 0;
				var toatlC = 0;

				for (var i=0; i<rows.length; i++){
					var shnCheck = rows[i]["F9"];
					var delM = rows[i]["F8"];

					if(shnCheck=="1"&&delM!="1"){
						toatlA = Number(toatlA)+Number(rows[i]["F4"]);
						toatlB = Number(toatlB)+Number(rows[i]["F5"]);
						toatlC = Number(toatlC)+Number(rows[i]["F6"]);
					}
				}
				$('#'+$.id.grd_genryo).datagrid('reloadFooter',[{ F1:"合計",F2:"",F3:"",F4:toatlA,F5:toatlB,F6:toatlC,F7:''}]);
			},
			setGridData: function (data, target){
				var that = this;

				// 基本データ
				if(target===undefined || target==="grd_data"){
					that.grd_data =  data["grd_data"];
				}

				// 計量風袋
				if(target===undefined || target===$.id.grd_kryofutai){
					that.grd_kryofutai_data =  data[$.id.grd_kryofutai];
				}

				return true;
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
			setResize: function(){		// （必須）リサイズ
				var changeHeight = $(window).height();
				if (0 < changeHeight) {

//					// window 幅取得
//					var changeWidth  = $(window).width();

//					// toolbar の調整
//					$($.id.toolbar).panel('resize',{width:changeWidth});

//					// toolbar の高さ調整
//					$.setToolbarHeight();

//					// DataGridの高さ
//					var gridholderHeight = 0;
//					var placeholderHeight = 0;

//					if ($($.id.gridholder).datagrid('options') != 'undefined') {
//					// tb
//					placeholderHeight = $($.id.toolbar).panel('panel').height() + $($.id.buttons).height();

//					// datagrid の格納された panel の高さ
//					gridholderHeight = $(window).height() - placeholderHeight;
//					}

//					$($.id.gridholder).datagrid('resize', {
//					width:	changeWidth,
//					height:	gridholderHeight
//					});
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
			addLine: function(){		// 新規入力行を表示する

				$('#'+$.id.grd_genryo).datagrid('appendRow',{ F1:"",F2:"",F3:"",F4:'',F5:'',F6:'',F7:'',F8:false,F9:'0'});

			},
			changeInputboxFunc:function(that, id, newValue, obj){

				var parentObj = $('#'+that.focusRootId);
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

				// DB問い合わせ系
				if($('[for_inp^='+id+'_]').length > 0){
					var param = that.getInputboxParams(that, id, newValue);
					var rows = $.getSelectListData(that.name, $.id.action_change, id, param);
					var row = rows.length > 0 ? rows[0]:"";
					//$.getsetInputboxRowData(that.name, 'for_inp', id, param, that, parentObj);
					$.setInputboxRowData('for_inp', id, row, that, parentObj);

				}

				var msgid = null;
				/*var msgParam  = [];
				if(id=="txt_shncd"||id=="txt_shncd_M"){
					var txt_shncd_chk = $.getInputboxData(that.name, $.id.action_check,  $.id_inp.txt_shncd, [{KEY:"MST_CNT",value:newValue}]);
					if(txt_shncd_chk == "0"){
						$.showMessage('E20160');
						return false;
					}
				}*/

				// 商品コード
				if(id===$.id_inp.txt_shncd || id==="txt_shncd_M"){
					msgid = that.checkInputboxFunc(id, newValue, '');
				}

				// 部門コード
				if(id===$.id_inp.txt_bmncd){
					msgid = that.checkInputboxFunc(id, newValue, '');
				}

				if(msgid !==null){
					$.showMessage(msgid, undefined, func_focus );
					return false;
				}
			},
			checkInputboxFunc:function(id, newValue, kbn, record, isNew){
				var that = this;
				var sdt, edt;

				// 商品コード
				if(id===$.id_inp.txt_shncd || id==="txt_shncd_M"){
					var param = {};
					param["KEY"] =  "MST_CNT";
					param["value"] = newValue;
					var chk_cnt = $.getInputboxData(that.name, $.id.action_check, $.id_inp.txt_shncd, [param]);
					if(chk_cnt==="" || chk_cnt==="0"){
						return "E20160";
					}
				}

				// 部門コード
				if(id===$.id_inp.txt_bmncd){
					var param = {};
					param["KEY"] =  "MST_CNT";
					param["value"] = newValue;
					var chk_cnt = $.getInputboxData(that.name, $.id.action_check, $.id_inp.txt_bmncd, [param]);
					if(chk_cnt==="" || chk_cnt==="0"){
						return "E11044";
					}
				}
				return null;
			},
			getInputboxParams: function(that, id, newValue){
				// 情報取得
				var values = {};
				values["value"] = newValue;
				values["TABLEKBN"] = that.baseTablekbn;

				// 情報設定
				return [values];
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

				// 戻る実行時用に現在の画面情報を保持する
				var states = $.getBackBaseJSON(that);
				// 各種グリッド情報を設定
				var newrepinfos = $.getBackJSON(that, states);
				$.setJSONObject(sendJSON, 'repinfo', newrepinfos, '');


				// 呼出別処理
				switch (btnId) {
				case $.id.btn_new:

					// 転送先情報
					index = 2;
					childurl = href[index];

					// オブジェクト作成
					$.setJSONObject(sendJSON, $.id_inp.txt_bmncd,'', '');

					break;
				case $.id.btn_copy:
					// 転送先情報
					index = 1;
					childurl = href[index];

					// オブジェクト作成
					$.setJSONObject(sendJSON, $.id_inp.txt_bmncd,'', '');

				case $.id.btn_cancel:
					// 転送先情報
					sendMode = 2;
					index = 1;
					childurl = href[index];

					break;
				case "btn_return":
					// 転送先情報
					sendMode = 2;
					index = 1;
					childurl = href[index];

					break;
				case $.id.btn_back:
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
						sendMode:sendMode,
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
		} });
})(jQuery);