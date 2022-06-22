/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_Reportx131',			// （必須）レポートオプションの確認
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
		dedefaultObjNum:	10,					// 初期化オブジェクト数
		initObjNum:	-1,
		initedObject: [],
		maxMergeCell: 2,
		onChangeFlag : false,
		onChangeFlag2 : false,
		columnName:'',	// OnClickRowの列名
		queried : false,
		initializes : true,
		onChangeReport: false,
		reportYobiInfo: function(){				// （必須）レポートメニューに登録された予備情報＝帳票をどういった表示にしたいかの情報
			return $('#reportYobi1').val();
		},
		pushBtnId: "",						// （必須）実行ボタンID情報(検索系で利用)
		focusRootId:"cc",						// （キー移動時必須）キー移動イベントのルートとなるパネルのID
		focusParentId:"",						// （キー移動時必須）現在フォーカスがあたっている項目の親となるパネルのID
		focusGridId:"",							// （キー移動時必須）現在フォーカスがあたっているDataGridのID
		editRowIndex:{},						// グリッド編集行保持
		record_info:false,						// 登録者情報の表示の有無
		gridData:[],						// 検索結果
		initialize: function (reportno){		// （必須）初期化
			var that = this;
			// 引き継ぎ情報
			this.jsonHidden = $.getTargetValue();

			// 画面の初回基本設定
			this.setInitObjectState();

			// 初期検索条件設定
			this.jsonInit = $.getInitValue();
			// データ表示エリア初期化

			// 初期化するオブジェクト数設定
			this.initObjNum = this.dedefaultObjNum;

			$.setInputbox(that, reportno, "txt_f3", true);

			// 入力テキストボックス系
			var inputbox = Object.getOwnPropertyNames($.id_inp);
			for ( var sel in inputbox ) {
				if($('#'+$.id_inp[inputbox[sel]]).length > 0){
					$.setInputbox(that, reportno, $.id_inp[inputbox[sel]], false);
				}
			}

			var isUpdateReport = true;

			$.setCheckboxInit2(that.jsonHidden, "chk_f5", that);
			$.setCheckboxInit2(that.jsonHidden, "chk_f6", that);
			$.setCheckboxInit2(that.jsonHidden, "chk_f7", that);
			$.setCheckboxInit2(that.jsonHidden, "chk_f8", that);
			$.setCheckboxInit2(that.jsonHidden, "chk_f9", that);
			$.setCheckboxInit2(that.jsonHidden, "chk_f10", that);
			$.setCheckboxInit2(that.jsonHidden, "chk_f11", that);

			// Load処理回避
			//$.tryChangeURL(null);

			// 編集可能データグリッドの共通処理設定
			// 編集エディターの機能拡張（非表示で参照用のオブジェクトが準備してある前提、参照オブジェクトはグリッドのフィールド名を元に判断）
			$.extendDatagridEditor();

			that.setEditableGrid(that, reportno, "gridholder");

			// 初期化終了
			this.initializes =! this.initializes;

			$.initialSearch(that);

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
			if(sendBtnid && sendBtnid.length > 0){
				that.onChangeReport = true;
				$.removeMaskMsg();
			}
			// 当帳票を「参照」で開いた場合
			if(that.reportYobiInfo()==='1'){
				$.initReportInfo("TP009", "店舗曜日別発注部門マスタ　参照");
				// 戻るボタン以外すべて非表示にする
				//$($.id.buttons).find("a[id!="+$.id.btn_back+"]").hide();
				$('#'+$.id.btn_upd).linkbutton('disable');
				$('#'+$.id.btn_upd).attr('disabled', 'disabled').hide();
				$('#'+$.id.btn_cancel).linkbutton('disable');
				$('#'+$.id.btn_cancel).attr('disabled', 'disabled').hide();
				$('#'+$.id.btn_del).linkbutton('disable');
				$('#'+$.id.btn_del).attr('disabled', 'disabled').hide();
				$.setInputBoxDisable($($.id.hiddenChangedIdx));
			}else{
				// 各種遷移ボタン
				//that.reportYobiInfo = '0'
				$('#'+$.id.btn_cancel).on("click", $.pushChangeReport);
				$.initReportInfo("TP009", "店舗曜日別発注部門マスタ　新規登録/変更");
			}
			if(!that.record_info){
				$("#disp_record_info").hide();
			}else{
				$("#disp_record_info").show();
			}
			$.setInputBoxDisable($("#"+$.id_inp.txt_tenkn));
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
			if(!rt){
				return rt;
			}

			// マスタ存在チェック：店コード
			var tencd	 = $.getInputboxValue($('#'+$.id_inp.txt_tencd)); 			// 店コード
			var msgid = that.checkInputboxFunc($.id_inp.txt_tencd, tencd, '');
			if(msgid !==null){
				$.showMessage(msgid);
				var rt= false;
				return false;
			}

			// 入力エラーなしの場合に検索条件を格納
			if (rt == true) that.jsonString = that.jsonTemp.slice(0);
			// 入力チェック用の配列をクリア
			that.jsonTemp = [];
			return rt;
		},
		getGridParams:function(that, id){
			var values = {};
			values["callpage"]	= $($.id.hidden_reportno).val()										// 呼出元レポート名
			values["SEL_SHUNO"]	= $.getInputboxValue($('#'+$.id.sel_shuno))							// 参照商品コード
			//values["SHUNO"]		= $.getInputboxValue($('#'+$.id_inp.txt_shncd))						// 入力商品コード
			return [values];
		},
		getGridColumns:function(that, id){
			var columns = [];
			var columnBottom=[];

			var fields = ["BMNCD","BMNKN","HATFLG_MON","HATFLG_TUE","HATFLG_WED","HATFLG_THU","HATFLG_FRI","HATFLG_SAT"];
			var titles = ["部門","部門名","月","火","水","木","金","土","日"];
			var bcstyler =function(value,row,index){return 'background-color:#f5f5f5;';};
			var dformatter =function(value){
				var add20 = value && value.length===6;
				return $.getFormatDt(value, add20);
			};
			var dparser = function(s){return s.slice(-6);};
			var cstyler =function(value,row,index){return 'color: red;font-weight: bold;';};


			if(that.judgeRepType.sei_new||that.judgeRepType.sei_upd){
				if(that.judgeRepType.sei_upd){
					columnBottom.push({field:'CHK_DEL',	title:'削除',	width:35,	align:'center',	formatter:cformatter,	editor:{type:'checkbox'},	styler:cstyler});
					columnBottom.push({field:'BMNKN',	title:'部門名',	width:35,	align:'center',	formatter:cformatter,	styler:function(value,row,index){return 'background-color:#f5f5f5;color:red;font-weight: bold;';}});
				}

				columnBottom.push({field:fields[5],	title:titles[5],	width:40,	align:'center',	formatter:cstyler,	editor:{type:'checkbox'},	styler:cstyler,});
				columnBottom.push({field:fields[6],	title:titles[6],	width:40,	align:'center',	formatter:cstyler,	editor:{type:'checkbox'},	styler:cstyler,});
				columnBottom.push({field:fields[7],	title:titles[7],	width:40,	align:'center',	formatter:cstyler,	editor:{type:'checkbox'},	styler:cstyler,});
				columnBottom.push({field:fields[8],	title:titles[8],	width:40,	align:'center',	formatter:cstyler,	editor:{type:'checkbox'},	styler:cstyler,});
				columnBottom.push({field:fields[9],	title:titles[9],	width:40,	align:'center',	formatter:cstyler,	editor:{type:'checkbox'},	styler:cstyler,});
				columnBottom.push({field:fields[10],title:titles[10],	width:40,	align:'center',	formatter:cstyler,	editor:{type:'checkbox'},	styler:cstyler,});
				columnBottom.push({field:fields[11],title:titles[11],	width:40,	align:'center',	formatter:cstyler,	editor:{type:'checkbox'},	styler:cstyler,});

			}
		},
		success: function(reportno, sortable){	// （必須）正処理
			if (sortable) sortable=1; else sortable=0;
			var that = this;

			// 検索実行
			var txt_tencd		= $.getJSONObject(this.jsonString, $.id_inp.txt_tencd).value;		// 店コード
			var txt_tenkn		= $.getJSONObject(this.jsonString, $.id_inp.txt_tenkn).value;		// 店舗名称（漢字）

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
					TENCD			:txt_tencd,
					TENKN			:txt_tenkn,
					SENDBTNID		:that.sendBtnid,
					t:				(new Date()).getTime(),
					sortable:		sortable,
					sortName:		that.sortName,
					sortOrder:		that.sortOrder,
					rows:			99	// 表示可能レコード数
				},

				function(json){
					// 検索処理エラー判定
					if($.searchError(json)) return false;

					// ログ出力
					$.log(that.timeData, 'query:');

					// Load処理回避
					$.tryChangeURL(null);

					if (sortable===0){
						var options = $($.id.gridholder).datagrid('options');
						// 初期検索時に並び替え情報のリセット
						options.sortName = null;
						options.sortOrder = null;
					}

					that.queried = true;
					// Load処理回避
					$.tryChangeURL($.reg.jqeasy);

					// 登録者情報を表示
					that.record_info = true;
					that.setInitObjectState();

					// グリッド再描画
					$($.id.gridholder).datagrid('reload');
					$.removeMask();

					// 状態保存
					$.saveState2(reportno, that.getJSONString());

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
			var rt = $($.id.toolbarform).form('validate');
			if(!rt){
				$.addErrState(that, $('.validatebox-invalid').eq(0), false);
				return rt;
			}


			// 店舗コード
			var tencd = $.getInputboxValue($('#' +$.id_inp.txt_tencd));
			var msgid = that.checkInputboxFunc($.id_inp.txt_tencd, tencd, '');
			if(msgid !==null){
				$.showMessage(msgid);
				return false;
			}

			// 部門コード
			var targetDatas = [];
			targetDatas = that.getMergeGridDate('gridholder');
			for (var i=0; i<targetDatas.length; i++){
				var date = targetDatas[i];

				var msgid = that.checkInputboxFunc($.id_inp.txt_bmncd, date.F2, '');
				if(msgid !==null){
					$.showMessage(msgid);
					return false;
				}
			}

			// 重複チェック
			var targetdate = [];									//部門コード
			var targetRows = $($.id.gridholder).datagrid('getRows');
			for (var i=0; i<targetRows.length; i++){
				if(targetRows[i]["F3"] && targetRows[i]["F3"] !=="" ){
					targetdate.push(Number(targetRows[i]["F3"]));
				}else{
					if(targetRows[i]["F5"] == $.id.value_on
							|| targetRows[i]["F6"] == $.id.value_on
							|| targetRows[i]["F7"] == $.id.value_on
							|| targetRows[i]["F8"] == $.id.value_on
							|| targetRows[i]["F9"] == $.id.value_on
							|| targetRows[i]["F10"] == $.id.value_on
							|| targetRows[i]["F11"] == $.id.value_on
							){
						$.showMessage('E20125');
						return false;
					}
				}
			}
			var targetdateF = targetdate.filter(function (element, index, self) { return self.indexOf(element)*1 === index; });
			if(targetdate.length !== targetdateF.length){
				$.showMessage('E11040',['部門コード']);
				return false;
			}


			var targetDatas = [];
			targetDatas = that.getMergeGridDate('gridholder');

/*			if(!rt){
				$.showMessage('E00001');
				return rt;
			}*/
			return rt;
		},
		updSuccess: function(id){	// validation OK時 の update処理
			var that = this;

			// 基本情報
			var targetData = that.grd_data;

			// 変更行情報取得
			//var changedIndex = $($.id.hiddenChangedIdx).val().split(",");

			/*var targetDatas = [{}];
			$('#'+that.focusRootId).find('[col^=F]').each(function(){
				var col = $(this).attr('col');
				targetDatas[0][col] = $.getInputboxValue($(this));
			});*/

			var targetDatas = [];
			targetDatas = that.getMergeGridDate('gridholder');

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
					DATA:			JSON.stringify(targetDatas),	// 更新対象情報
					t:				(new Date()).getTime()
				},
				function(data){
					// 検索処理エラー判定
					if($.updError(id, data)) return false;

					var afterFunc = function(){
						// 初期化
						that.clear();
						// グリッド再描画（easyui 1.4.2 対応）
						$($.id.gridholder).datagrid('load', {} );
						$.setInputBoxDisable($($.id.hiddenChangedIdx));
						that.changeReport(that.name, 'btn_return');

					};
					$.updNormal(data, afterFunc);

					// ログ出力
					$.log(that.timeData, 'loaded:');
				}

			);
		},
		delValidation: function (){	// （必須）批准
			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = true;


			var row = $($.id.gridholder).datagrid("getSelected");
			if(!row){
				$.showMessage('E00008');
				rt =false;
				return rt;
			}

			return rt;
		},
		delSuccess: function(id){
			var that = this;
			var is_warning = false;

			var targetDatas = [];

			// 選択行
			var row = $($.id.gridholder).datagrid("getSelected");
			if(!row){
				$.showMessage('E00008');
				return false;
			}

			targetDatas[0] = row
			var txt_tencd		= $.getJSONObject(this.jsonString, $.id_inp.txt_tencd).value;		// 店コード


			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			//$.appendMaskMsg();

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,						// レポート名
					action:			$.id.action_delete,				// 実行処理情報
					obj:			id,								// 実行オブジェクト
					TENCD:			txt_tencd,
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
;
				}
			);
		},
		setDate:function(){
			var that = this
			var rows	 = that.getGridData('gridholder_date')['gridholder_date'];
/*			var maxvalueF12 = "";
			var maxvalueF13 = "";
			var maxvalueF14 = "";*/

			if(rows.length !==0){


				$.setInputboxValue($('#txt_operator'), rows[0].F12);
				$.setInputboxValue($('#txt_adddt'), rows[0].F13);
				$.setInputboxValue($('#txt_upddt'), rows[0].F14);
			}
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
			// 店コード
			this.jsonTemp.push({
				id:		$.id_inp.txt_tencd,
				value:	$('#'+$.id_inp.txt_tencd).textbox('getValue'),
				text:	$('#'+$.id_inp.txt_tencd).textbox('getText')
			});
			// 店舗名称（漢字）
			this.jsonTemp.push({
				id:		$.id_inp.txt_tenkn,
				value:	$('#'+$.id_inp.txt_tenkn).textbox('getValue'),
				text:	$('#'+$.id_inp.txt_tenkn).textbox('getText')
			});
		},
		setEditableGrid: function(that, reportno, id){		// データ表示
			var that = this;
			var init = true;
			var cstyler =function(value,row,index){return 'color: red;font-weight: bold;';};
			var cformatter =function(value,row,index){return value && value===$.id.value_on?$.id.text_on:$.id.text_off;};

			//that.editRowIndex[id] = -1;
			//var index = -1;

			var funcClickRow = $.fn.datagrid.defaults.onClickRow;
			var funcBeginEdit= $.fn.datagrid.defaults.onBeginEdit;
			var funcEndEdit= $.fn.datagrid.defaults.onEndEdit;
			var funcAfterEdit= $.fn.datagrid.defaults.onAfterEdit;
			if(that.reportYobiInfo()!=='1'){
				that.editRowIndex[id] = -1;
				funcClickRow = function(index,field){$.clickEditableDatagridCell(that,id, index)};
				funcBeginEdit = function(index,row){
					// 先頭の削除チェックにフォーカスを合わせない。
					$('#'+$.id.chk_del+"_").attr('tabindex', -1);
					if(row.F15 == '0'){
						// 既存商品コードは編集不可
						$.setInputBoxDisable($('#txt_f3_'));
					}
					$.beginEditDatagridRow(that,id, index, row)
				};
				funcEndEdit = function(index,row,changes){$.endEditDatagridRow(that, id, index, row)};
				funcAfterEdit = function(index,row,changes){
					var rowobj = $('#'+id).datagrid('getPanel').find("[datagrid-row-index='"+index+"']");
					// チェックボックスの再追加（EndEdit時に削除されるため）
					$.afterEditAddCheckbox(rowobj);

				};

				// 編集可能データグリッドの共通処理設定
				// 編集エディターの機能拡張（非表示で参照用のオブジェクトが準備してある前提、参照オブジェクトはグリッドのフィールド名を元に判断）
				$.extendDatagridEditor(that);

			}

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
				          	{field:'F3',		title:'部門',			editor:{type:'textbox'},width: 40,halign:'center',align:'left',formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '00');}},
				          	{field:'F4',		title:'部門名',			editor:{type:'textbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:false}},styler:function(value,row,index){return 'background-color:#f5f5f5;';},width: 260,halign:'center',align:'left'},
				          	{field:'F5',		title:'月',				editor:{type:'checkbox'}, styler:cstyler, formatter:cformatter,	width:  30,halign:'center',align:'center'},
				          	{field:'F6',		title:'火',				editor:{type:'checkbox'}, styler:cstyler, formatter:cformatter,	width:  30,halign:'center',align:'center'},
				          	{field:'F7',		title:'水', 			editor:{type:'checkbox'}, styler:cstyler, formatter:cformatter,	width:  30,halign:'center',align:'center'},
				          	{field:'F8',		title:'木', 			editor:{type:'checkbox'}, styler:cstyler, formatter:cformatter,	width:  30,halign:'center',align:'center'},
				          	{field:'F9',		title:'金', 			editor:{type:'checkbox'}, styler:cstyler, formatter:cformatter,	width:  30,halign:'center',align:'center'},
				          	{field:'F10',		title:'土', 			editor:{type:'checkbox'}, styler:cstyler, formatter:cformatter,	width:  30,halign:'center',align:'center'},
				          	{field:'F11',		title:'日', 			editor:{type:'checkbox'}, styler:cstyler, formatter:cformatter,	width:  30,halign:'center',align:'center'},
				          	{field:'F12',		title:'オペレーター',	width:  90,halign:'center',align:'right',hidden:true},
				          	{field:'F13',		title:'登録日',			width:  90,halign:'center',align:'right',hidden:true},
				          	{field:'F14',		title:'更新日',			width:  90,halign:'center',align:'right',hidden:true},
				          	{field:'F15',		title:'既存行フラグ',	width:  90,halign:'center',align:'right',hidden:true},
							]],
				onBeforeLoad:function(param){},
				onLoadSuccess:function(data){

					// チェックボックスの設定
					$.initCheckboxCss($('#'+id).datagrid('getPanel').find('.datagrid-body'));

					var gridData = that.getGridData(id);
					that.setGridData(gridData, id);
					that.setDate();
				},

				/*onSelect:function(index){
					var rows = $('#'+id).datagrid('getRows');
					var col = $('#'+id).datagrid('getColumnOption', 'F3');
					if(rows[index]["F15"]==='0'){
						col.editor = false

					}else{

						col.editor = {
								type:'numberbox',
								options:{cls:'labelInput',editable:true,disabled:false,readonly:false}
							}
					}
				},*/
				onClickCell:function(rowIndex, field, value){
					// 列名保持
					that.columnName = field;
				},
				onClickRow: funcClickRow,
				onBeginEdit:funcBeginEdit,
				onEndEdit: funcEndEdit,
				onAfterEdit: funcAfterEdit,
			});
		},
		getGridData: function (target){
			var that = this;

			var data = {};
			var targetRows= [];

			// 実仕入先一覧
			if(target===undefined || target==='gridholder'){
				var rowsAreahsptn	 = $('#'+'gridholder').datagrid('getRows');
				var txt_tencd		 = $('#'+$.id_inp.txt_tencd).textbox('getValue');

				for (var i=0; i<rowsAreahsptn.length; i++){
					var rowDate = {
							F1	 : txt_tencd,
							F2	 : rowsAreahsptn[i]["F3"],
							F3	 : rowsAreahsptn[i]["F5"],
							F4	 : rowsAreahsptn[i]["F6"],
							F5	 : rowsAreahsptn[i]["F7"],
							F6	 : rowsAreahsptn[i]["F8"],
							F7	 : rowsAreahsptn[i]["F9"],
							F8	 : rowsAreahsptn[i]["F10"],
							F9	 : rowsAreahsptn[i]["F11"],
							F10  : rowsAreahsptn[i]["F16"],
							idx  : rowsAreahsptn[i]["F1"],
					};
					targetRows.push(rowDate);
				}
				data['gridholder'] = targetRows;
			}

			if(target===undefined || target==='gridholder_date'){
				var rowsAreahsptn	 = $('#'+'gridholder').datagrid('getRows');
				var txt_tencd		 = $('#'+$.id_inp.txt_tencd).textbox('getValue');

				for (var i=0; i<rowsAreahsptn.length; i++){
					var rowDate = {
							F12	 : rowsAreahsptn[i]["F12"],
							F13	 : rowsAreahsptn[i]["F13"],
							F14	 : rowsAreahsptn[i]["F14"],

					};
					targetRows.push(rowDate);
				}
				data['gridholder_date'] = targetRows;
			}
			return data;
		},
		getMergeGridDate: function(target){
			// 保持したデータと入力データ比較を比較する。
			var that = this;

			var newrows = that.getGridData(target)[target];		// 変更データ
			var oldrows = [];
			var targetRows= [];

			if(target===undefined || target==='gridholder'){
				oldrows = that.gridData
				for (var i=0; i<newrows.length; i++){
					if( newrows[i]['F1'] !== oldrows[i]['F1']
					|| newrows[i]['F2'] !== oldrows[i]['F2']
					|| newrows[i]['F3'] !== oldrows[i]['F3']
					|| newrows[i]['F4'] !== oldrows[i]['F4']
					|| newrows[i]['F5'] !== oldrows[i]['F5']
					|| newrows[i]['F6'] !== oldrows[i]['F6']
					|| newrows[i]['F7'] !== oldrows[i]['F7']
					|| newrows[i]['F8'] !== oldrows[i]['F8']
					|| newrows[i]['F9'] !== oldrows[i]['F9']
					){
						if(newrows[i]['F1'] && newrows[i]['F2'] ){
							var rowDate = {
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
									idx	 : newrows[i]["idx"]
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

			// 店舗曜日別発注部門
			if(target===undefined || target==='gridholder'){
				that.gridData =  data['gridholder'];
			}
		},
		getRecord: function(){		// （必須）レコード件数を戻す
			var data = $($.id.gridholder).datagrid('getData');
			if (data == null) {t
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
			var sendJSON = JSON.parse( JSON.stringify( that.jsonString ) );
			$.setJSONObject(sendJSON, 'sendBtnid', btnId, $('#'+btnId).attr("title"));		// 実行ボタン情報保持

			// 選択行
			var row = $($.id.gridholder).datagrid("getSelected");

			// 実行ボタン別処理
			switch (btnId) {
			case 'btn_return':
				// 転送先情報
				index = 1;
				childurl = href[index];

				break;
			case $.id.btn_new:

				// 転送先情報
				index = 2;
				childurl = href[index];

				break;
			case $.id.btn_sel_change:
				if(!row){
					$.showMessage('E00008');
					return false;
				}

				// 転送先情報
				index = 2;
				childurl = href[index];

				// オブジェクト作成
				$.setJSONObject(sendJSON, $.id_inp.txt_readtmptn, row.F1, row.F1);

				break;
			case $.id.btn_cancel:
			case $.id.btn_back:
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
		/*changeInputboxFunc:function(that, id, newValue, obj){
			var parentObj = $('#'+that.focusRootId);
			var txt_bmncd = $.getInputboxValue($('#txt_f3_'));

			if(id+"_"===obj.attr('id') && that.focusGridId!==""){
				parentObj = $('#'+that.focusGridId).datagrid('getPanel');
			}

			// DB問い合わせ系
			if($('[for_inp^='+id+'_]').length > 0){
				var param = that.getInputboxParams(that, id, newValue);
				$.getsetInputboxRowData(that.name, 'for_inp', id, param, that, parentObj);
			}

			if(id=='txt_f3'){
				var txt_bmncd_chk = $.getInputboxData(that.name, $.id.action_check,  $.id_inp.txt_bmncd, [{KEY:"MST_CNT",value:txt_bmncd}]);
				if(txt_bmncd_chk == "0"){
					$.showMessage('E11044');
					return false;
				}
				var param = that.getInputboxParams(that,id , newValue);
					$.getsetInputboxRowData(that.name, 'for_inp',$.id_inp.txt_bmncd, param, that, parentObj);

				}
 		},*/
		getInputboxParams: function(that, id, newValue){
			// 情報取得
			var values = {};
			values["value"] = newValue;
			values["TABLEKBN"] = that.baseTablekbn;



			// 情報設定
			return [values];
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
				$.getsetInputboxRowData(that.name, 'for_inp', id, param, that, parentObj);
			}

			// 検索、入力後特殊処理
			if(that.queried){
				var msgid = null;

				// 店舗コード
				/*if(id===$.id_inp.txt_tencd){
					msgid = that.checkInputboxFunc(id, newValue, '');
				}*/

				// 部門コード
				if(id=='txt_f3'){
					//msgid = that.checkInputboxFunc($.id_inp.txt_bmncd, newValue, '');

					var param = that.getInputboxParams(that,id , newValue);
					$.getsetInputboxRowData(that.name, 'for_inp',$.id_inp.txt_bmncd, param, that, parentObj);

				}

				if(msgid !==null){
					$.showMessage(msgid, undefined, func_focus );
					return false;
				}
			}
		},
		checkInputboxFunc:function(id, newValue, kbn, record, isNew){
			var that = this;
			var sdt, edt;

			// 納入期間
			if(id===$.id_inp.txt_nnstdt||id===$.id_inp.txt_nneddt){

			}

			// 部門コード
			if(id===$.id_inp.txt_bmncd){
				if(newValue !== '' && newValue){

					// 部門マスタ
					var param = {};
					param["KEY"] =  "MST_CNT";
					param["value"] = newValue;
					var chk_cnt = $.getInputboxData(that.name, $.id.action_check, $.id_inp.txt_bmncd, [param]);
					if(chk_cnt==="" || chk_cnt==="0"){
						return "E11097";
					}
				}
			}

			// 店舗コード
			if(id===$.id_inp.txt_tencd){
				if(newValue !== '' && newValue){

					// 店舗マスタ
					var param = {};
					param["KEY"] =  "MST_CNT";
					param["value"] = newValue;
					var chk_cnt = $.getInputboxData(that.name, $.id.action_check, $.id_inp.txt_tencd, [param]);
					if(chk_cnt==="" || chk_cnt==="0"){
						return "E11096";
					}
				}
			}
			return null;
		},
		getInputboxParams: function(that, id, newValue){
			// 情報取得
			var values = {};
			values["value"] = newValue;
			values["TABLEKBN"] = that.baseTablekbn;

			// 店コード
			if(id===$.id_inp.txt_tencd){
				values["TENCD"] = $.getInputboxValue($('#'+$.id_inp.txt_tencd));
			}

			// 情報設定
			return [values];
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