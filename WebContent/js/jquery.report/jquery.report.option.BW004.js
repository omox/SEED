/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_ReportBW004',			// （必須）レポートオプションの確認
		jsonTemp:	[],							// （必須）検索条件情報_入力チェック前
		jsonString:	[],							// （必須）検索条件情報
		jsonStringCsv:	[],						// （CSV出力用）検索条件情報
		jsonHidden: [],							// （必須）親画面からの引き継ぎ情報
		jsonInit: [],							// （必須）検索条件初期情報
		caption: function(){					// （必須）タイトル
			return $('#reportname').val();
		},
		sortName: '',	// ソート項目名
		sortOrder: '',	// ソート順
		timeData : (new Date()).getTime(),
		dedefaultObjNum:	18,	// 初期化オブジェクト数
		initObjNum:	-1,
		initedObject: [],
		editRowIndex:{},						// グリッド編集行保持
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
		pushBtnId: "",						// 実行ボタンID情報(検索系で利用)
		focusRootId:"cc",					// （キー移動時必須）キー移動イベントのルートとなるパネルのID
		focusParentId:"",					// （キー移動時必須）現在フォーカスがあたっている項目の親となるパネルのID
		focusGridId:"",						// （キー移動時必須）現在フォーカスがあたっているDataGridのID
		delSelect:false,
		SUMIKANRINO:"",
		hbstdt:"",
		dummycd:"",
		waribiki:"",
		seisi:"123",
		grd_shohin_data:[],					// グリッド情報:採用情報
		initialize: function (reportno){	// （必須）初期化
			var that = this;
			// 引き継ぎ情報
			this.jsonHidden = $.getTargetValue();
			// 初期検索条件設定
			this.jsonInit = $.getInitValue();
			// 画面の初回基本設定
			this.setInitObjectState();

			// データ表示エリア初期化
			$.extendDatagridEditor(that);
			that.setGrid('gridholder', reportno);

			// 初期化するオブジェクト数設定
			this.initObjNum = this.dedefaultObjNum;
			var isUpdateReport = true;

			// 処理日付取得
			$.getsetInputboxData(reportno, $.id.txt_shoridt, [{}], $.id.action_init);

			// 初期表示処理
			that.onChangeReport = true;

			// 名称マスタ参照系
			var meisyoSelect = Object.getOwnPropertyNames($.id_mei);
			for ( var sel in meisyoSelect ) {
				if($('#'+$.id_mei[meisyoSelect[sel]]).length > 0){
					$.setMeisyoCombo(that, reportno, $.id_mei[meisyoSelect[sel]], isUpdateReport);
				}
			}
			// 入力テキストボックス系
			var inputbox = Object.getOwnPropertyNames($.id_inp);
			for ( var sel in inputbox ) {
				if($('#'+$.id_inp[inputbox[sel]]).length > 0){
					$.setInputbox(that, reportno, $.id_inp[inputbox[sel]], isUpdateReport);
				}
			}
			//$.setInputbox(that, reportno, 'txt_waribiki', isUpdateReport);
			//$.setInputbox(that, reportno, 'txt_seisi', isUpdateReport);
			// Load処理回避
			//$.tryChangeURL(null);

			// 初期化終了
			this.initializes =! this.initializes;

			$.initialDisplay(that);

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
			if(!sendBtnid){
				sendBtnid = $('#sendBtnid').val();
			}
			$('#sendBtnid').val(sendBtnid);
			var reportYobi1 = $.getJSONValue(that.jsonHidden, "reportYobi1");
			if(!reportYobi1){
				reportYobi1 = $('#reportYobi1').val();
			}
			$('#reportYobi1').val(reportYobi1);

			$($.id.buttons).show();
			// 各種ボタン
			$.initReportInfo("BW004", "冷凍食品 CSV取込 エラー修正", "エラー修正");
			that.delSelect = false;
			$('#'+$.id.btn_cancel).on("click", $.pushChangeReport);
			$('#'+$.id.btn_sel_change).on("click", $.pushChangeReport);

		},
		initCondition: function (){	// 条件初期値セット
			var that = this;
			// 初期化項目
		},
		clear:function(){
			// 隠し情報初期化
			$($.id.hiddenChangedIdx).val("");						// 変更行Index
//			// グリッド初期化
//			this.success(this.name, false);
		},
		validation: function (btnId){	// （必須）批准
			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			//var rt = $($.id.toolbarform).form('validate');
			var rt = true;
			// 入力エラーなしの場合に検索条件を格納
			if (rt == true) that.jsonString = that.jsonTemp.slice(0);
			// 入力チェック用の配列をクリア
			that.jsonTemp = [];
			if (rt == true) that.jsonStringCsv = that.jsonTemp.slice(0);
			return rt;
		},
		success: function(reportno, sortable, btnId){	// （必須）正処理
			if (sortable) sortable=1; else sortable=0;
			var that = this;
			// 検索実行
			/*var szBmncd		= $.getJSONValue(this.jsonHidden, $.id_inp.txt_bmncd)	// 部門コード
			var szHbstdt	= $.getJSONValue(this.jsonHidden, $.id_inp.txt_hbstdt);	// 催し開始日
			var szDummycd	= $.getJSONValue(this.jsonHidden, $.id_inp.txt_dummycd);// ダミーコード
			var szWaribiki	= $.getJSONValue(this.jsonHidden, 'txt_waribiki2');		// 割引率区分
			var szSeisi		= $.getJSONValue(this.jsonHidden, 'txt_seisi2');		// 正規カット
*/			var szSeq		= $.getJSONValue(this.jsonHidden, $.id.txt_seq);		// 催し連番

			if(!btnId) btnId = $.id.btn_search;

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			$($.id.gridholder).datagrid('loading');

			// grid.options 取得
			var options = $($.id.gridholder).datagrid('options');
			that.sortName	= options.sortName;
			that.sortOrder	= options.sortOrder;
			var sendBtnid = $.getJSONValue(that.jsonHidden, "sendBtnid");
			if(!sendBtnid){
				sendBtnid = $('#sendBtnid').val();
			}
			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,		// レポート名
					SENDBTNID:		sendBtnid,
					/*BMNCD:			szBmncd,		// 催し区分
					HBSTDT:			szHbstdt,		// 催しコード（催し開始日）
					DUMMYCD:		szDummycd,		// 催し連番
					WARIBIKI:		szWaribiki,		// 割引率区分
					SEISI:			szSeisi,		// 店コード
*/					SEQ:			szSeq,			// SEQ
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

					var opts = JSON.parse(json).opts
					if(opts && opts.rows_){
						// 基本データ表示
						that.setData(opts.rows_, opts);
					}

					// 冷凍食品＿企画(親データ)でエラー登録がなかった場合は
					// ヘッダー部を入力不可にする。
					var inputFlg = $.getInputboxValue($('#hiddenInputFlg')); // 入力フラグ
					if(inputFlg == '0' || inputFlg == '' || !inputFlg){
						$.setInputBoxDisable($("#"+$.id_inp.txt_hbstdt));
						$.setInputBoxDisable($("#"+$.id_inp.txt_bmncd));
						$.setInputBoxDisable($("#"+$.id_inp.txt_meishokn));
						$.setInputBoxDisable($("#"+$.id_mei.kbn10302));
						$.setInputBoxDisable($("#"+$.id_mei.kbn10303));
						$.setInputBoxDisable($("#"+$.id_inp.txt_dummycd));
					}

					that.queried = true;
					that.pushBtnId = btnId;
					// Load処理回避
					$.tryChangeURL($.reg.jqeasy);

					// グリッド再描画（easyui 1.4.2 対応）
					$($.id.gridholder).datagrid('load', {} );
					$.removeMask();
					$.removeMaskMsg();

					// ログ出力
					$.log(that.timeData, 'loaded:');
				}
			);
		},
		updValidation: function (){	// （必須）批准
			var that = this;
			// Grid編集中の場合は入力を終了する。
			var row = $($.id.gridholder).datagrid("getSelected");
			var rowIndex = $($.id.gridholder).datagrid("getRowIndex", row);
			$($.id.gridholder).datagrid('endEdit',rowIndex);

			var rt = $($.id.toolbarform).form('validate');

			var targetDatasShn = that.getMergeGridDate($.id.gridholder);

			for (var i=0; i<targetDatasShn.length; i++){
				var inpdata = targetDatasShn[0];
				var txt_shncd = inpdata["F1"];

				// 新規(正) 1.3　取得された商品コードが商品マスタテーブルに存在しない場合、エラー。
				var txt_shncd_chk = $.getInputboxData(that.name, $.id.action_check,  $.id_inp.txt_shncd, [{KEY:"MST_CNT",value:txt_shncd}]);
				if(txt_shncd_chk == "0"){
					$.showMessage('E20160');
					return false;
				}
			}

			var targetdate = [];											// 重複チェク用変数：商品コード
			var targetRows = $($.id.gridholder).datagrid('getRows');
			for (var i=0; i<targetRows.length; i++){
				if(targetRows[i]["F2"].trim()){
					targetdate.push(targetRows[i]["F2"].trim());
				}
			}
			// 重複チェック：商品コード
			var targetdateF = targetdate.filter(function (element, index, self) { return self.indexOf(element)*1 === index; });
			if(targetdate.length !== targetdateF.length){
				$.showMessage('E20572', ['商品コード']);
				return false;
			}

			// 入力チェック：ダミーコード
			var szDummycd	= $.getInputboxValue($('#'+ $.id_inp.txt_dummycd));	// ダミーコード
			if(szDummycd == "00000000"){
				$.showMessage('EX1047', ['ダミーコードは[00000000]以外の値']);
				return false;
			}

			// 存在チェック:部門コード
			var szBmncd	= $.getInputboxValue($('#'+$.id_inp.txt_bmncd));
			if(szBmncd && szBmncd != ""){
				var param = {};
				param["KEY"] =  "MST_CNT";
				param["value"] = szBmncd;
				var chk_cnt = $.getInputboxData(that.name, $.id.action_check, $.id_inp.txt_bmncd, [param]);
				if(chk_cnt==="" || chk_cnt==="0"){
					$.showMessage("E11044");
					return false;
				}
			}

			// 販売開始日
			var shoridt = $('#'+$.id.txt_shoridt).val();						// 処理日付
			var szHbstdt	= $.getInputboxValue($('#'+$.id_inp.txt_hbstdt));	// 販売開始日
			if(shoridt && shoridt != "" && szHbstdt && szHbstdt != ""){
				var sdt = $.convDate(szHbstdt, true);
				var edt = $.convDate(shoridt , true);
			}
			if(sdt.getTime() <= edt.getTime()){	// 処理日付 < 販売開始日
				$.showMessage("EX1124");
				return false;
			}

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
			var szHbstdt	= targetDatas[0]["F1"];								// 催し開始日
			var szBmncd		= targetDatas[0]["F2"];								// 部門コード
			var szMeishokn	= targetDatas[0]["F3"];								// 名称漢字名
			var szDummycd	= targetDatas[0]["F6"];								// ダミーコード
			//var szWaribiki	= targetDatas[0]["F8"];								// 割引率区分
			//var szSeisi		= targetDatas[0]["F9"];								// 正規・カット
			var szWaribiki	= targetDatas[0]["F4"];								// 割引率区分
			var szSeisi		= targetDatas[0]["F5"];								// 正規・カット
			var szSeq		= $.getJSONValue(this.jsonHidden, $.id.txt_seq);	// 催し連番
			// 商品一覧のデータを取得
			var targetDatasShn = that.getMergeGridDate($.id.gridholder);

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			//$.appendMaskMsg();
			that.SUMIKANRINO 	= $.getInputboxData(that.name, $.id.action_init,  "SUMI_KANRINO", [{HBSTDT:szHbstdt,BMNCD:szBmncd,WRITUKBN:szWaribiki,SEICUTKBN:szSeisi,DUMMYCD:szDummycd}]);
			if(that.SUMIKANRINO==0){
				that.SUMIKANRINO = '0001'
			}
			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,				// レポート名
					action:			$.id.action_update,		// 実行処理情報
					obj:			id,						// 実行オブジェクト
					SENDBTNID:		that.sendBtnid,
					BMNCD:			szBmncd,				// 部門コード
					HBSTDT:			szHbstdt,				// 催し開始日
					DUMMYCD:		szDummycd,				// ダミーコード
					WARIBIKI:		szWaribiki,				// 割引率区分
					MEISHOKN:		szMeishokn,				// 名称漢字名
					SEISI:			szSeisi,				// 正視・カット
					SEQ:			szSeq,					// 番号
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
		delValidation: function (){	// （必須）批准
			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = true;
			var row = $($.id.gridholder).datagrid("getSelected");
			if(!row && that.delSelect == true){
				alert("データが選択されていません。");
				return false;
			}
			return rt;
		},
		delSuccess: function(id){

			var that = this;
			var targetDatas = [{}];
			$('#'+that.focusRootId).find('[col^=F]').each(function(){
				var col = $(this).attr('col');
				targetDatas[0][col] = $.getInputboxValue($(this));
			});
			var row = $($.id.gridholder).datagrid("getSelected");
			var szSeq		= $.getJSONValue(this.jsonHidden, $.id.txt_seq);	// 催し連番
			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$.appendMaskMsg();

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,							// レポート名
					action:			$.id.action_delete,					// 実行処理情報
					obj:			id,									// 実行オブジェクト
					DATA:			JSON.stringify(targetDatas),		// 更新対象情報(プライスカード)
					SEQ:			szSeq,								// 番号
					INPUTNO:		row.F14,							// 入力番号
					t:				(new Date()).getTime()
				},
				function(data){
					// 検索処理エラー判定
					if($.delError(id, data)) return false;

					var afterFunc = function(){
						// 初期化
						that.clear();
						that.changeReport(that.name, 'btn_del');
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
			// レポート名
			this.jsonTemp.push({
				id:		$.id.txt_seq,
				value:	$.getJSONValue(this.jsonHidden, $.id.txt_seq),
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
		setObjectState: function(){	// 軸の選択内容による制御

		},
		extenxDatagridEditorIds:{
			F2		: "txt_shncd",
			F3		: "txt_makerkn",
			F4		: "txt_shnkn",
			F5		: "txt_kikkn",
			F6		: "txt_irisu",
			F7		: "txt_sougakubaika",
			F8		: "txt_rg_baikaam",
			F9		: "txt_rg_genkaam",
			F10		: "txt_baikaam",
			F11		: "txt_hontaibaika",
			F12		: "txt_genkaam",
			F13		: "txt_shobruikn",
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
				var sendBtnid = $.getJSONValue(that.jsonHidden, "sendBtnid");
				var shnFormatter = function(value,row,index){
					return $.getFormatPrompt(row.F2, '####-####');
			}
				$('#'+id).datagrid({
					nowrap: true,
					border: true,
					striped: true,
					collapsible:false,
					remoteSort: true,
					rownumbers:true,
					fit:true,
					view:scrollview,
					singleSelect:true,
					pageSize:pageSize,
					pageList:pageList,
					checkOnSelect:false,
					selectOnCheck:false,
					frozenColumns:[[
							{field:'F2',	title:'商品コード',							width: 80 ,halign:'center',align:'left',	editor:{type:'numberbox'},formatter:shnFormatter},
							{field:'F3',	title:'メーカー名',							width:230,	halign:'center',align:'left',	editor:{type:'textbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}},
							{field:'F4',	title:'商品名',								width:250,	halign:'center',align:'left',	editor:{type:'textbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}},

					                ]],
					columns:[[
					          //{field:'F2',	title:'商品コード',							width:100,	halign:'center',align:'left',	editor:{type:'numberbox'},formatter:function(value, rowData, rowIndex) {return $.getFormatPrompt(value, '####-####');}},
					          {field:'F5',	title:'規格',								width:250,	halign:'center',align:'left',	editor:{type:'textbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}},
					          {field:'F6',	title:'入数',								width:50,	halign:'center',align:'right',	editor:{type:'numberbox'},formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0');}},
					          {field:'F7',	title:'標準総額売価',						width:90,	halign:'center',align:'right',	editor:{type:'numberbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';},formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0');}},
					          {field:'F8',	title:'標準本体売価',	    				width:90,	halign:'center',align:'right',	editor:{type:'numberbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';},formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0');}},
					          {field:'F9',	title:'標準原価',		    				width:90,	halign:'center',align:'right',	editor:{type:'numberbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';},formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0.00');}},
					          {field:'F10',	title:'割引総額売価',						width:90,	halign:'center',align:'right',	editor:{type:'numberbox'},formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0');}},
					          {field:'F11',	title:'割引本体売価',	    				width:90,	halign:'center',align:'right',	editor:{type:'numberbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';},formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0');}},
					          {field:'F12',	title:'割引原価',							width:90,	halign:'center',align:'right',	editor:{type:'numberbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';},formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0.00');}},
					          {field:'F13',	title:'分類',	    						width:200,	halign:'center',align:'left',	editor:{type:'textbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}},
					          ]],
					          fitColumns:false,	// 指定カラム幅を適用する場合、false 指定。
					          rowStyler:function(index, row){
					          },
					          onLoadSuccess:function(data){
					        	  // 各グリッドの値を保持する
					        	  var gridData = that.getGridData('#'+id);
					        	  that.setGridData(gridData, '#'+id);

					        	  // チェックボックスの設定
					        	  $.initCheckboxCss($('#'+id).datagrid('getPanel').find('.datagrid-body'));
					          },
					          onClickCell:function(rowIndex, field, value){
					        	  // 列名保持
					        	  that.columnName = field;
					          },
					          onClickRow: function(index,field){$.clickEditableDatagridCell(that,id, index)},
					          onBeginEdit:function(index,row){$.beginEditDatagridRow(that,id, index, row)},
					          onEndEdit: function(index,row,changes){$.endEditDatagridRow(that, id, index, row)},
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
		getGridData: function (target){
			var that = this;

			var data = {};
			var targetRows= [];

			// 商品一覧
			if(target===undefined || target===$.id.gridholder){
				var rows	 = $($.id.gridholder).datagrid('getRows');			// 商品一覧

				for (var i=0; i<rows.length; i++){
					var rowDate = {
							F1	 : rows[i]["F2"],//商品コード
							F2	 : rows[i]["F4"],//商品名
							F3	 : rows[i]["F5"],//規格
							F4	 : rows[i]["F6"],//入数
							F5	 : rows[i]["F10"],//割引総額売価
							F6	 : rows[i]["F12"],//割引原価
							F7	 : rows[i]["F3"],
							F8	 : rows[i]["F16"],
							F9	 : rows[i]["F18"],
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
				// 商品一覧
				oldrows = that.grd_saiyou_data
				for (var i=0; i<newrows.length; i++){
					if(newrows[i]['F1'] && newrows[i]['F1'] !== ""){
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
						};
						if(rowDate){
							targetRows.push(rowDate);
						}
					}

					/*if( newrows[i]['F1'] != oldrows[i]['F1'] || newrows[i]['F4'] != oldrows[i]['F4']|| newrows[i]['F5'] != oldrows[i]['F5'] ){
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
							};
						if(rowDate){
							targetRows.push(rowDate);
						}
					}*/
				}
			}
			return targetRows;
		},
		setGridData: function (data, target){
			var that = this;

			// 商品一覧
			if(target===undefined || target===$.id.gridholder){
				that.grd_saiyou_data =  data[$.id.gridholder];
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
			var sendJSON = JSON.parse( JSON.stringify( that.jsonHidden ) );
			$.setJSONObject(sendJSON, 'sendBtnid', btnId, $('#'+btnId).attr("title"));					// 実行ボタン情報保持
			$.setJSONObject(sendJSON, 'reportYobi1', that.reportYobiInfo(), that.reportYobiInfo());		// 参照情報保持
			$.setJSONObject(sendJSON, 'callpage', that.name, that.name);								// 呼出し元レポート情報

			// 戻る実行時用に現在の画面情報を保持する
			var states = $.getBackBaseJSON(that);
			// 各種グリッド情報を設定
			var newrepinfos = $.getBackJSON(that, states);
			$.setJSONObject(sendJSON, 'repinfo', newrepinfos, '');

			// 実行ボタン別処理
			switch (btnId) {
			case $.id.btn_sel_change:
				// 選択行
				var row = $($.id.gridholder).datagrid("getSelected");
				if(!row){
					$.showMessage('E00008');
					return false;
				}
				// 転送先情報
				index = 4;
				childurl = href[index];
				sendMode = 1;
				// オブジェクト作成
				$.setJSONObject(sendJSON, $.id_inp.txt_tencd,row.F4, row.F4);
				break;
			case $.id.btn_back:
			case $.id.btn_return:
			case $.id.btn_cancel:
				// 転送先情報
				sendMode = 2;
				index = 1;
				//sendMode = 1;
				if(that.reportYobiInfo()=='1'){
					index = 2;
				}else if(that.reportYobiInfo()=='2'||that.reportYobiInfo()=='0'){
					index = 4;
				}else if(that.reportYobiInfo()=='3'){
					index = 5;
				}
				childurl = href[index];
				break;
			case $.id.btn_upd:
				// 転送先情報
				index = 1;
				//sendMode = 1;
				if(that.reportYobiInfo()=='1'){
					index = 2;
				}else if(that.reportYobiInfo()=='2'||that.reportYobiInfo()=='0'){
					index = 4;
				}else if(that.reportYobiInfo()=='3'){
					index = 5;
				}
				childurl = href[index];
				break;
			case $.id.btn_del:
			case $.id.btn_all_del:
				// 転送先情報
				index = 1;
				//sendMode = 1;
				if(that.reportYobiInfo()=='1'){
					index = 2;
				}else if(that.reportYobiInfo()=='2'||that.reportYobiInfo()=='0'){
					index = 4;
				}else if(that.reportYobiInfo()=='3'){
					index = 5;
				}
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
			if(id != $.id_inp.txt_baikaam){
				if($('[for_inp^='+id+'_]').length > 0){
					var param = that.getInputboxParams(that, id, newValue);
					$.getsetInputboxRowData(that.name, 'for_inp', id, param, that, parentObj);
				}
			}

			// 検索、入力後特殊処理
			if(that.queried){
				var msgid = null;

				// 商品コード
				if(id===$.id_inp.txt_shncd){
					var row = $($.id.gridholder).datagrid("getSelected");
					if(newValue && newValue != ""){
						// 存在チェック：商品コード
						var txt_shncd_chk = $.getInputboxData(that.name, $.id.action_check,  $.id_inp.txt_shncd, [{KEY:"MST_CNT",value:newValue}]);
						if(txt_shncd_chk == "0"){
							$.showMessage('E20160', undefined, func_focus );
							return false;
						}

						// 存在チェック：商品区分
						var txt_shncd_kbn = $.getInputboxData(that.name, $.id.action_check,  $.id_inp.txt_shnkbn, [{KEY:"MST_CNT",value:newValue}]);
						if(txt_shncd_kbn == "0"){
							$.showMessage('E20488', undefined, func_focus );
							return false;
						}

						// 相互チェック:部門コード、商品コード頭2桁
						var topShn2 	= newValue.slice(0,2);								// 商品コード
						var szBmncd = $.getInputboxValue($('#'+$.id_inp.txt_bmncd));		// 部門コード

						if(Number(topShn2) != Number(szBmncd)){
							$.showMessage('E11162', undefined, func_focus );
							return false;
						}
					}else{
						// 商品コードがクリアされた場合
						var baikaam = $.getInputboxValue($('#'+$.id_inp.txt_baikaam+'_'));

						// 売価項目のクリアを行う
						$('#'+$.id_inp.txt_baikaam+'_').textbox('setValue','');				//
						$('#txt_hontaibaika_').textbox('setValue','');						//
						$('#'+$.id_inp.txt_genkaam+'_').textbox('setValue','');				//

					}
				}

				if(id=="txt_baikaam"){
					var shn = $.getInputboxValue($('#'+$.id_inp.txt_shncd+'_'));

					if(shn && shn != ""){
						// DB問い合わせ系
						// 書き方が正しいか後程確認
						if(id.length > 0){
							var param = that.getInputboxParams2(that, id, newValue,shn);
							$.getsetInputboxRowData(that.name, 'for_inp', id, param, that, parentObj);
						}

						// 存在チェック：商品コード
						var txt_shncd_chk = $.getInputboxData(that.name, $.id.action_check,  $.id_inp.txt_shncd, [{KEY:"MST_CNT",value:shn}]);
						if(txt_shncd_chk == "0"){
							$.showMessage('E20160');
							return false;
						}
					}
				}

				if(msgid !==null){
					$.showMessage(msgid, undefined, func_focus );
					return false;
				}
			}
		},
		/*changeInputboxFunc:function(that, id, newValue, obj){
			var row = $($.id.gridholder).datagrid("getSelected");
			if(row!=null){

				var upd = row["F17"];

				// 新規(正) 1.3　取得された商品コードが商品マスタテーブルに存在場合、エラー。
				if(id=="txt_shncd"){
					var txt_shncd_chk = $.getInputboxData(that.name, $.id.action_check,  $.id_inp.txt_shncd, [{KEY:"MST_CNT",value:newValue}]);
					if(txt_shncd_chk == "0"){
						$.showMessage('E20160');
						return false;
					}
					if(upd==""){
						var parentObj = $('#'+that.focusRootId);
						if(id+"_"===obj.attr('id') && that.focusGridId!==""){
							parentObj = $('#'+that.focusGridId).datagrid('getPanel');
						}
						var size = $('[for_inp^='+id+'_]').length ;
						// DB問い合わせ系
						// 書き方が正しいか後程確認
						if(id.length > 0){
							var param = that.getInputboxParams(that, id, newValue);
							$.getsetInputboxRowData(that.name, 'for_inp', id, param, that, parentObj);
						}
						//var txt_moyscd = $.getInputboxValue($('#'+$.id_inp.txt_moyscd_));

						// 検索、入力後特殊処理
						if(that.queried){
							// 特殊処理
							//var bmncd = $.getInputboxValue($('#'+$.id_inp.txt_bmncd));
							if(that.focusGridId === $.id.grd_srccd && that.editRowIndex[$.id.grd_srccd] === 0 && bmncd.length > 0 && $.getInputboxValue($('#'+$.id_inp.txt_makercd)).length === 0){

							}

							// 新規：新規・新規コピー・選択コピーボタン押下時
							if(that.sendBtnid===$.id.btn_new||that.sendBtnid===$.id.btn_copy||that.sendBtnid===$.id.btn_sel_copy){

							}

							// 存在チェック：店舗コード系
							if(id===$.id_inp.txt_centercd ||id===$.id_inp.txt_ycentercd){

							}
						}
					}
				}else if(id=="txt_baikaam"){
					var shn = row["F2"];
					var txt_shncd_chk = $.getInputboxData(that.name, $.id.action_check,  $.id_inp.txt_shncd, [{KEY:"MST_CNT",value:shn}]);
					if(txt_shncd_chk == "0"){
						$.showMessage('E20160');
						return false;
					}
					var parentObj = $('#'+that.focusRootId);
					if(id+"_"===obj.attr('id') && that.focusGridId!==""){
						parentObj = $('#'+that.focusGridId).datagrid('getPanel');
					}
					var size = $('[for_inp^='+id+'_]').length ;
					// DB問い合わせ系
					// 書き方が正しいか後程確認
					if(id.length > 0){
						var param = that.getInputboxParams2(that, id, newValue,shn);
						$.getsetInputboxRowData(that.name, 'for_inp', id, param, that, parentObj);
					}
					//var txt_moyscd = $.getInputboxValue($('#'+$.id_inp.txt_moyscd_));

					// 検索、入力後特殊処理
					if(that.queried){
						// 特殊処理
						//var bmncd = $.getInputboxValue($('#'+$.id_inp.txt_bmncd));
						if(that.focusGridId === $.id.grd_srccd && that.editRowIndex[$.id.grd_srccd] === 0 && bmncd.length > 0 && $.getInputboxValue($('#'+$.id_inp.txt_makercd)).length === 0){

						}

						// 新規：新規・新規コピー・選択コピーボタン押下時
						if(that.sendBtnid===$.id.btn_new||that.sendBtnid===$.id.btn_copy||that.sendBtnid===$.id.btn_sel_copy){

						}

						// 存在チェック：店舗コード系
						if(id===$.id_inp.txt_centercd ||id===$.id_inp.txt_ycentercd){

						}
					}
				}
			}
		},*/
		getInputboxParams: function(that, id, newValue){
			// 情報取得
			var values = {};
			values["value"]		 = newValue;
			values["TABLEKBN"]	 = that.baseTablekbn;

			// 商品コード
			if(id===$.id_inp.txt_shncd){
				var szHbstdt	= $.getInputboxValue($('#'+$.id_inp.txt_hbstdt));	// 販売開始日
				values["HBSTDT"]	 = szHbstdt;
			}

			// ダミーコード
			if(id===$.id_inp.txt_dummycd){
				values["SHNCD"] = parseInt($.getInputboxValue($('#'+$.id_inp.txt_dummycd)));
			}

			// 情報設定
			return [values];
		},
		getInputboxParams2: function(that, id, newValue,shn){
			// 情報取得
			var values = {};
			values["value"]		 = newValue;
			values["TABLEKBN"]	 = that.baseTablekbn;

			if(id===$.id_inp.txt_baikaam){
				var szHbstdt	= $.getInputboxValue($('#'+$.id_inp.txt_hbstdt));	// 販売開始日
				var szBmncd		= $.getInputboxValue($('#'+$.id_inp.txt_bmncd));	// 部門コード
				var selWaribiki 	= $.getInputboxValue($('#'+$.id_mei.kbn10302))	// 割引率区分
				var selWaribiki 	= $.getInputboxValue($('#'+$.id_mei.kbn10303))	// 正規・カット
				var szDummycd	= $.getInputboxValue($('#'+$.id_inp.txt_dummycd));	// ダミーコード

				values["HBSTDT"]	 = szHbstdt;			// 販売開始日
				values["BMNCD"]		 = szBmncd;				// 部門
				values["WARIBIKI"]	 = selWaribiki;			// 割引率区分
				values["SEISI"]		 = selWaribiki;			// 正規・カット
				values["DUMMYCD"]	 = szDummycd;			// ダミーコード
				values["SHNCD"]		 = shn;
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
		},
		csv: function(reportno){	// Csv出力
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
				'kbn'	: kbn,
				'type'	: 'csv'
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