/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_ReportBW002',			// （必須）レポートオプションの確認
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
		dedefaultObjNum:	17,	// 初期化オブジェクト数
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
		hbstdt1:"",
		hbstdt2:"",
		dummycd1:"",
		dummycd2:"",
		waribiki1:"",
		waribiki2:"",
		bmncd:"",
		seiki1:"",
		seiki2:"",
		seisi:"123",
		grd_shohin_data:[],					// グリッド情報:採用情報
		initialize: function (reportno){	// （必須）初期化
			var that = this;
			// 引き継ぎ情報
			this.jsonHidden = $.getTargetValue();

			that.hbstdt1 = $.getJSONValue(this.jsonHidden, 'txt_hbstdt2');
			that.hbstdt2 = $.getJSONValue(this.jsonHidden, 'txt_hbstdt3');
			that.dummycd1 = $.getJSONValue(this.jsonHidden, $.id_inp.txt_dummycd);
			that.dummycd2 = $.getJSONValue(this.jsonHidden, 'txt_dummycd2');
			that.waribiki1 = $.getJSONValue(this.jsonHidden, 'txt_waribiki');
			that.waribiki2 = $.getJSONValue(this.jsonHidden, 'txt_waribiki2');
			that.bmncd = $.getJSONValue(this.jsonHidden, $.id_inp.txt_bmncd);
			that.seiki1 = $.getJSONValue(this.jsonHidden, 'txt_seisi');
			that.seiki2 = $.getJSONValue(this.jsonHidden, 'txt_seisi2');


			// 画面の初回基本設定
			this.setInitObjectState();

			// 初期検索条件設定
			this.jsonInit = $.getInitValue();
			// データ表示エリア初期化

			// 初期化するオブジェクト数設定
			this.initObjNum = this.dedefaultObjNum;
			var isUpdateReport = true;

			// 初期表示処理
			that.onChangeReport = true;

			// 入力テキストボックス系
			var inputbox = Object.getOwnPropertyNames($.id_inp);
			for ( var sel in inputbox ) {
				if($('#'+$.id_inp[inputbox[sel]]).length > 0){
					$.setInputbox(that, reportno, $.id_inp[inputbox[sel]], isUpdateReport);
				}
			}
			$.setInputbox(that, reportno, 'txt_waribiki', isUpdateReport);
			$.setInputbox(that, reportno, 'txt_seisi', isUpdateReport);
			$.setCheckboxInit2(that.jsonHidden, $.id.chk_del, that);
			// Load処理回避
			//$.tryChangeURL(null);
			$.extendDatagridEditor(that);
			that.setGrid('gridholder', reportno);
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
			$('#'+$.id.btn_new).on("click", $.pushChangeReport);
			$('#'+$.id.btn_copy).on("click", $.pushChangeReport);
			$('#'+$.id.btn_new+2).on("click", $.pushChangeReport);
			if(sendBtnid  == $.id.btn_sel_change || sendBtnid  == $.id.btn_upd){
				$.initReportInfo("BW002", "冷凍食品 新規・変更 商品一覧", "一覧");
				$("#searchCombo").hide();
				$("#del").hide();
				that.delSelect = false;
			}else if (sendBtnid  == $.id.btn_sel_refer){
				$.initReportInfo("BW002", "冷凍食品 参照 商品一覧", "一覧");
				$('#'+$.id.btn_upd).hide();
				$('#'+$.id.btn_upd).attr('disabled', 'disabled').hide();
				$("#del").hide();
				$('#'+$.id.btn_del).hide();
				$('#'+$.id.btn_del).attr('disabled', 'disabled').hide();
				$('#'+$.id.btn_cancel).hide();
				$('#'+$.id.btn_cancel).attr('disabled', 'disabled').hide();
			}
			$('#'+$.id.btn_cancel).on("click", $.pushChangeReport);
			$('#'+$.id.btn_sel_change).on("click", $.pushChangeReport);
			//$('#'+$.id.btn_csv).on("click", that.pushCsv);
			//$('#'+$.id.btn_csv).on("click", function(e){alert('現在CSV出力機能は停止中です。');});
			//$('#'+$.id.btn_csv).on("click",that.pushCsv_test);
			$('#'+$.id.btn_csv).on("click", function(e){alert('現在CSV出力機能は停止中です。');});


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
			var szBmncd		= $.getJSONValue(this.jsonHidden, $.id_inp.txt_bmncd)		// 部門コード
			var szHbstdt	= $.getInputboxValue($('#'+$.id_inp.txt_hbstdt));			// 催し開始日
			var szDummycd	= $.getJSONValue(this.jsonHidden, $.id_inp.txt_dummycd);	// ダミーコード
			var szWaribiki	= $.getJSONValue(this.jsonHidden, 'txt_waribiki2');			// 割引率区分
			var szSeisi		= $.getJSONValue(this.jsonHidden, 'txt_seisi2');			// 正規カット
			var szSeq		= $.getJSONValue(this.jsonHidden, $.id.txt_seq);			// 催し連番

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
					BMNCD:			szBmncd,		// 催し区分
					HBSTDT:			szHbstdt,		// 催しコード（催し開始日）
					DUMMYCD:		szDummycd,		// 催し連番
					WARIBIKI:		szWaribiki,		// 割引率区分
					SEISI:			szSeisi,		// 店コード
					SEQ:			szSeq,			// SEQ
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

			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform).form('validate');
			if(!rt){
				$.addErrState(that, $('.validatebox-invalid').eq(0), false);
				return rt;
			}

			rt = $($.id.gridform).form('validate');
			if(!rt){
				$.addErrState(that, $('.validatebox-invalid').eq(0), false);
				return rt;
			}

			var targetDatasShn = that.getMergeGridDate($.id.gridholder);
			for (var i=0; i<targetDatasShn.length; i++){
				var inpdata = targetDatasShn[i];
				var txt_shncd = inpdata["F1"];
				var txt_shnkn = inpdata["F2"];
				var txt_kikak = inpdata["F3"];
				var txt_irisu = inpdata["F4"];
				var txt_sougaku = inpdata["F5"];
				var txt_genka = inpdata["F6"];

				if(txt_shncd != null&&txt_shncd != ""){
					// 新規(正) 1.3　取得された商品コードが商品マスタテーブルに存在しない場合、エラー。
					var txt_shncd_chk = $.getInputboxData(that.name, $.id.action_check,  $.id_inp.txt_shncd, [{KEY:"MST_CNT",value:txt_shncd}]);
					if(txt_shncd_chk == "0"){
						$.showMessage('E20160');
						return false;
					}
					if(txt_shnkn == null||txt_shnkn == ""||txt_kikak == null||txt_kikak == ""||txt_irisu == null||txt_irisu == ""||txt_sougaku == null||txt_sougaku == ""||txt_genka == null||txt_genka == ""){
						$.showMessage('E00001');
						return false;
					}

					// 相互チェック:部門コード、商品コード頭2桁
					var msgid = that.checkInputboxFunc($.id_inp.txt_shncd, txt_shncd , '');
					if(msgid !==null){
						$.showMessage(msgid);
						return false;
					}
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
			var szDummycd	= $.getJSONValue(this.jsonHidden, $.id_inp.txt_dummycd);	// ダミーコード
			if(szDummycd == "00000000"){
				$.showMessage('EX1047', ['ダミーコードは[00000000]以外の値']);
				return false;
			}

			// 「1日遅開始」「通常開始」が両方とも１の場合エラー
			return rt;
		},
		// IDとvalueでチェック処理を実施
		checkInputboxFunc:function(id, newValue, kbn, record, isNew){
			var that = this;
			var sdt, edt;

			// 商品コード
			if(id===$.id_inp.txt_shncd){
				if(newValue !== '' && newValue){
					// 相互チェック:部門コード、商品コード頭2桁
					var topShn2 	= newValue.slice(0,2);
					var szBmncd		= $.getJSONValue(that.jsonHidden, $.id_inp.txt_bmncd);//部門コード
					if(topShn2 != szBmncd){
						return "E11162";
					}
				}
			}
			return null;
		},
		updSuccess: function(id){	// validation OK時 の update処理
			var that = this;

			// 変更行情報取得
			var targetDatas = [{}];
			$('#'+that.focusRootId).find('[col^=F]').each(function(){
				var col = $(this).attr('col');
				targetDatas[0][col] = $.getInputboxValue($(this));
			});
			var szBmncd		= $.getJSONValue(this.jsonHidden, $.id_inp.txt_bmncd);//部門コード
			var szHbstdt	= '20' + $.getJSONValue(this.jsonHidden, $.id_inp.txt_hbstdt);	// 催し開始日
			 szHbstdt =  $.getInputboxValue($('#'+$.id_inp.txt_hbstdt));
			var szDummycd	= $.getJSONValue(this.jsonHidden, $.id_inp.txt_dummycd);	// ダミーコード
			var szWaribiki	= $.getJSONValue(this.jsonHidden, 'txt_waribiki2');	// 割引率区分
			var szSeisi		= $.getJSONValue(this.jsonHidden, 'txt_seisi2');	// 正規・カット
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
			var szBmncd		= $.getJSONValue(this.jsonHidden, $.id_inp.txt_bmncd);//部門コード
			var szHbstdt	= '20' + $.getJSONValue(this.jsonHidden, $.id_inp.txt_hbstdt);	// 催し開始日
			var szDummycd	= $.getJSONValue(this.jsonHidden, $.id_inp.txt_dummycd);	// ダミーコード
			var szWaribiki	= $.getJSONValue(this.jsonHidden, 'txt_waribiki2');	// 割引率区分
			var szSeisi		= $.getJSONValue(this.jsonHidden, 'txt_seisi2');	// 正規・カット
			var szSeq		= $.getJSONValue(this.jsonHidden, $.id.txt_seq);	// 催し連番
			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			//$.appendMaskMsg();

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,							// レポート名
					action:			$.id.action_delete,					// 実行処理情報
					obj:			id,									// 実行オブジェクト
					DATA:			JSON.stringify(targetDatas),		// 更新対象情報(プライスカード)
					BMNCD:			szBmncd,				// 部門コード
					HBSTDT:			szHbstdt,				// 催し開始日
					DUMMYCD:		szDummycd,				// ダミーコード
					WARIBIKI:		szWaribiki,				// 割引率区分
					SEISI:			szSeisi,				// 正視・カット
					SEQ:			szSeq,					// 番号
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
			F20		: "chk_del"		// チェックボックス（店不採用禁止)
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
				if(sendBtnid  == $.id.btn_sel_change || sendBtnid  == $.id.btn_upd){
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
					          {field:'F20',	title:'削除',								styler:cstyler, formatter:cformatter,editor:{type:'checkbox'},width: 40,halign:'center',align:'center'},
							  {field:'F2',	title:'商品コード',							width: 80 ,halign:'center',align:'left',editor:{type:'numberbox'},formatter:shnFormatter},
							  {field:'F3',	title:'メーカー名',							width:230,	halign:'center',align:'left',	editor:{type:'textbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}},
							  {field:'F4',	title:'商品名',								width:250,	halign:'center',align:'left',	editor:{type:'textbox'}},
					           ]],
					columns:[[
					          {field:'F5',	title:'規格',								width:250,	halign:'center',align:'left',	editor:{type:'textbox'}},
					          {field:'F6',	title:'入数',								width:50,	halign:'center',align:'right',	editor:{type:'numberbox'},formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0');}},
					          {field:'F7',	title:'標準総額売価',						width:90,	halign:'center',align:'right',	editor:{type:'numberbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';},formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0');}},
					          {field:'F8',	title:'標準本体売価',	    				width:90,	halign:'center',align:'right',	editor:{type:'numberbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';},formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0');}},
					          {field:'F9',	title:'標準原価',		    				width:90,	halign:'center',align:'right',	editor:{type:'numberbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';},formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0.00');}},
					          {field:'F10',	title:'割引総額売価',						width:90,	halign:'center',align:'right',	editor:{type:'numberbox'},formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0');}},
					          {field:'F11',	title:'割引本体売価',	    				width:90,	halign:'center',align:'right',	editor:{type:'numberbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';},formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0');}},
					          {field:'F12',	title:'割引原価',							width:90,	halign:'center',align:'right',	editor:{type:'numberbox'},formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0.00');}},
					          {field:'F13',	title:'分類',	    						width:200,	halign:'center',align:'left',	editor:{type:'textbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}},
					          {field:'F14',	title:'登録日',	    						width:90,	halign:'center',align:'left'},
					          {field:'F15',	title:'更新日',	    						width:90,	halign:'center',align:'left'},
					          {field:'F16',	title:'オペレーター',  						width:90,	halign:'center',align:'left'},
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

									// チェックボックスの設定
									$.initCheckboxCss($('#'+id).datagrid('getPanel').find('.datagrid-body'));
								},
								onClickCell:function(rowIndex, field, value){
									// 列名保持
									that.columnName = field;
								},
								onClickRow: function(index,field){$.clickEditableDatagridCell(that,id, index)},
								onBeginEdit:function(index,row){
									$.beginEditDatagridRow(that,id, index, row)
								},
								onSelect:function(index,row){
									var col2 = $('#'+id).datagrid('getColumnOption', 'F2');
									var col16 = $('#'+id).datagrid('getColumnOption', 'F20');

									if(row["F18"]==""){
										col2.editor = {type:'numberbox'}
										col16.editor = false
									}else{
										col2.editor = false
										col16.editor = {type:'checkbox'}
									}
								},
								onEndEdit: function(index,row,changes){
									$.endEditDatagridRow(that, id, index, row)
									row.CHK_SEL = $.id.value_off;
								},
								onAfterEdit: function(index,row,changes){
									// チェックボックスの再追加（EndEdit時に削除されるため）
									$.afterEditAddCheckbox($('#'+id).datagrid('getPanel').find("[datagrid-row-index='"+index+"']"));
								},
							});
				}else if (sendBtnid  == $.id.btn_sel_refer){
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
						checkOnSelect:false,
						selectOnCheck:false,
						frozenColumns:[[]],
						columns:[[
						          {field:'F20',	title:'削除',								styler:cstyler, formatter:cformatter,width: 40,halign:'center',align:'center'},
						          {field:'F2',	title:'商品コード',							width:100,	halign:'center',align:'left',	editor:{type:'textbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},formatter:function(value, rowData, rowIndex) {return $.getFormatPrompt(value, '####-####');}},
						          {field:'F3',	title:'メーカー名',							width:230,	halign:'center',align:'left'	,editor:{type:'textbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}},
						          {field:'F4',	title:'商品名',								width:250,	halign:'center',align:'left',	editor:{type:'textbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:true}}},
						          {field:'F5',	title:'規格',								width:250,	halign:'center',align:'left',	editor:{type:'textbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:true}}},
						          {field:'F6',	title:'入数',								width:50,	halign:'center',align:'right',	editor:{type:'numberbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0');}},//入り数
						          {field:'F7',	title:'標準総額売価',						width:90,	halign:'center',align:'right',	editor:{type:'numberbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';},formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0');}},
						          {field:'F8',	title:'標準本体売価',	    				width:90,	halign:'center',align:'right',	editor:{type:'numberbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';},formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0');}},
						          {field:'F9',	title:'標準原価',		    				width:90,	halign:'center',align:'right',	editor:{type:'numberbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';},formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0.00');}},
						          {field:'F10',	title:'割引総額売価',						width:90,	halign:'center',align:'right',	editor:{type:'numberbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0');}},
						          {field:'F11',	title:'割引本体売価',	    				width:90,	halign:'center',align:'right',	editor:{type:'numberbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';},formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0');}},
						          {field:'F12',	title:'割引原価',							width:90,	halign:'center',align:'right',	editor:{type:'numberbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0.00');}},
						          {field:'F13',	title:'分類',	    						width:200,	halign:'center',align:'left',editor:{type:'textbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:true}}},
						          {field:'F14',	title:'登録日',	    						width:90,	halign:'center',align:'left'},
						          {field:'F15',	title:'更新日',	    						width:90,	halign:'center',align:'left'},
						          {field:'F16',	title:'オペレーター',  						width:90,	halign:'center',align:'left'},
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

										// チェックボックスの設定
										$.initCheckboxCss($('#'+id).datagrid('getPanel').find('.datagrid-body'));
									},
									onClickCell:function(rowIndex, field, value){
										// 列名保持
										that.columnName = field;
									},
									onClickRow: function(index,field){$.clickEditableDatagridCell(that,id, index)},
									onBeginEdit:function(index,row){
										$.beginEditDatagridRow(that,id, index, row)


									},
									onEndEdit: function(index,row,changes){
										$.endEditDatagridRow(that, id, index, row)
										row.CHK_SEL = $.id.value_off;
									},
									onAfterEdit: function(index,row,changes){
										// チェックボックスの再追加（EndEdit時に削除されるため）
										$.afterEditAddCheckbox($('#'+id).datagrid('getPanel').find("[datagrid-row-index='"+index+"']"));
									},
								});
				}
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
							F8	 : rows[i]["F20"],
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
					if(newrows[i]['F1'] != null && newrows[i]['F1'] != ""){
						if( newrows[i]['F1'] != oldrows[i]['F1']
						|| newrows[i]['F2'] != oldrows[i]['F2']
						|| newrows[i]['F3'] != oldrows[i]['F3']
						|| newrows[i]['F4'] != oldrows[i]['F4']
						|| newrows[i]['F5'] != oldrows[i]['F5']
						|| parseFloat(newrows[i]['F6']) != parseFloat(oldrows[i]['F6'])
						|| parseFloat(newrows[i]['F8']) == "1" ){
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
					}
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
			case $.id.btn_new:
			case $.id.btn_new+2:
				var row = $($.id.gridholder).datagrid("getSelected");
				// 転送先情報
				index = 3;
				childurl = href[index];
				sendMode = 1;
				// オブジェクト作成
				$.setJSONObject(sendJSON, $.id_inp.txt_tengpcd,row.F1, row.F1);
				$.setJSONObject(sendJSON, $.id_inp.txt_moyskbn,row.F18, row.F18);
				$.setJSONObject(sendJSON, $.id_inp.txt_moysstdt,row.F19, row.F19);
				$.setJSONObject(sendJSON, $.id_inp.txt_moysrban,row.F20, row.F20);
				break;
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
				$.setJSONObject(sendJSON, 'kyosei_flg',row.F22, row.F22);
				break;
			case $.id.btn_copy:
				// 転送先情報
				index = 6;
				childurl = href[index];
				sendMode = 1;
				// オブジェクト作成
				$.setJSONObject(sendJSON, $.id_inp.txt_tengpcd, row.F20, row.F20);	// 店グループ
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
				}else if(that.reportYobiInfo()=='2'){
					index = 4;
				}else if(that.reportYobiInfo()=='3'){
					index = 5;
				}
				childurl = href[index];
				break;
			case $.id.btn_upd:
				// 転送先情報
				sendMode = 2;
				index = 1;
				//sendMode = 1;
				if(that.reportYobiInfo()=='1'){
					index = 2;
				}else if(that.reportYobiInfo()=='2'){
					index = 4;
				}else if(that.reportYobiInfo()=='3'){
					index = 5;
				}
				childurl = href[index];
				break;
			case $.id.btn_del:
			case $.id.btn_all_del:
				// 転送先情報
				sendMode = 2;
				index = 1;
				//sendMode = 1;
				if(that.reportYobiInfo()=='1'){
					index = 2;
				}else if(that.reportYobiInfo()=='2'){
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
			if(id != $.id_inp.txt_shncd && id != $.id_inp.txt_baikaam){
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
					var upd = row["F17"];

					if(upd==""){
						// DB問い合わせ系
						// 書き方が正しいか後程確認
						if(id.length > 0){
							var param = that.getInputboxParams(that, id, newValue);
							$.getsetInputboxRowData(that.name, 'for_inp', id, param, that, parentObj);
						}
					}

					if(newValue && newValue != ""){
						// 存在チェック：商品コード
						var txt_shncd_chk = $.getInputboxData(that.name, $.id.action_check,  $.id_inp.txt_shncd, [{KEY:"MST_CNT",value:newValue}]);
						if(txt_shncd_chk == "0"){
							$.showMessage('E20160');
							return false;
						}

						// 存在チェック：商品区分
						var txt_shncd_kbn = $.getInputboxData(that.name, $.id.action_check,  $.id_inp.txt_shnkbn, [{KEY:"MST_CNT",value:newValue}]);
						if(txt_shncd_kbn == "0"){
							$.showMessage('E20488');
							return false;
						}

						// 相互チェック:部門コード、商品コード頭2桁
						var topShn2 	= newValue.slice(0,2);
						var szBmncd		= $.getJSONValue(this.jsonHidden, $.id_inp.txt_bmncd);//部門コード
						if(topShn2 != szBmncd){
							$.showMessage('E11162');
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
					/*var shn = "";
					var row = $($.id.gridholder).datagrid("getSelected");
					if(row["F2"]){
						shn = row["F2"];
					}else{
						shn = $.getInputboxValue($('#'+$.id_inp.txt_shncd+'_'));
					}*/
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

			var row = $($.id.gridholder).datagrid("getSelected");
			var upd = row["F17"];

			// 新規(正) 1.3　取得された商品コードが商品マスタテーブルに存在場合、エラー。
			if(id=="txt_shncd"){
				var txt_shncd_chk = $.getInputboxData(that.name, $.id.action_check,  $.id_inp.txt_shncd, [{KEY:"MST_CNT",value:newValue}]);
				if(txt_shncd_chk == "0"){
					$.showMessage('E20160');
					return false;
				}
				var txt_shncd_kbn = $.getInputboxData(that.name, $.id.action_check,  $.id_inp.txt_shnkbn, [{KEY:"MST_CNT",value:newValue}]);
				if(txt_shncd_kbn == "0"){
					$.showMessage('E20488');
					return false;
				}

				var topShn2 	= newValue.slice(0,2);
				var szBmncd		= $.getJSONValue(this.jsonHidden, $.id_inp.txt_bmncd);//部門コード

				if(topShn2 != szBmncd){
					$.showMessage('E11162');
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
				var shn = $.getInputboxValue($('#'+$.id_inp.txt_shncd+'_'));
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
		},*/
		getInputboxParams: function(that, id, newValue){
			// 情報取得
			var values = {};
			values["HBSTDT"] = '20' + $.getJSONValue(this.jsonHidden, $.id_inp.txt_hbstdt);
			values["BMNCD"] = $.getJSONValue(this.jsonHidden, $.id_inp.txt_bmncd);			// 部門
			values["WARIBIKI"] = $.getJSONValue(this.jsonHidden, 'txt_waribiki2');	// 割引率区分
			values["SEISI"] = $.getJSONValue(this.jsonHidden, 'txt_seisi2');	// 正規・カット
			values["DUMMYCD"] = $.getJSONValue(this.jsonHidden, $.id_inp.txt_dummycd);
			values["value"] = newValue;
			values["TABLEKBN"] = that.baseTablekbn;

			// 情報設定
			return [values];
		},
		getInputboxParams2: function(that, id, newValue,shn){
			// 情報取得
			var values = {};
			values["HBSTDT"] = '20' + $.getJSONValue(this.jsonHidden, $.id_inp.txt_hbstdt);
			values["BMNCD"] = $.getJSONValue(this.jsonHidden, $.id_inp.txt_bmncd);			// 部門
			values["WARIBIKI"] = $.getJSONValue(this.jsonHidden, 'txt_waribiki2');	// 割引率区分
			values["SEISI"] = $.getJSONValue(this.jsonHidden, 'txt_seisi2');	// 正規・カット
			values["DUMMYCD"] = $.getJSONValue(this.jsonHidden, $.id_inp.txt_dummycd);
			values["SHNCD"] = shn;
			values["value"] = newValue;
			values["TABLEKBN"] = that.baseTablekbn;

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
		srccsv: function(reportno, btnId){	// ここではCsv出力
			var that = this;
			// 処理時間計測用
			that.timeData = (new Date()).getTime();

			var that = this;
			// 検索実行
			var szBmncd		= $.getJSONValue(this.jsonHidden, $.id_inp.txt_bmncd)	// 部門コード
			var szHbstdt	= '20'+$.getJSONValue(this.jsonHidden, $.id_inp.txt_hbstdt);	// 催し開始日
			var szDummycd	= $.getJSONValue(this.jsonHidden, $.id_inp.txt_dummycd);// ダミーコード
			var szWaribiki	= $.getJSONValue(this.jsonHidden, 'txt_waribiki2');		// 割引率区分
			var szSeisi		= $.getJSONValue(this.jsonHidden, 'txt_seisi2');		// 正規カット


			if(!btnId) btnId = $.id.btn_search;

			var kbn = 0;
			var data = {
				report:			that.name,		// レポート名
				'kbn':			 kbn,
				'type':			'csv',
				BTN:			btnId,
				BMNCD:			szBmncd,		// 部門コード
				HBSTDT:			szHbstdt,		// 催しコード
				DUMMYCD:		szDummycd,		// ダミーコード
				WARIBIKI:		szWaribiki,		// 割引率区分
				SEISI:			szSeisi,		// 正視カット
				t:				(new Date()).getTime(),
				rows:			0	// 表示可能レコード数
			};

			// 転送
			$.ajax({
				url: $.reg.srcexcel,
				type: 'POST',
				data: data,
				async: true
			})
			.done(function(){
				// Excel出力
				$.outputSearchExcel(reportno, 0);
			})
			.fail(function(){
				// Excel出力エラー
				$.outputSearchExcelError();
			})
			.always(function(){
				// 通信完了
				// ログ出力
				$.log(that.timeData, 'srcexcel:');
			});
		},
		/**
		 * CSV出力ボタンイベント
		 * @param {Object} e
		 */
		pushCsv : function(e){
			var that = this;
			if ($(that).linkbutton('options').disabled)	return false;

			// レポート番号取得
			var reportno=$($.id.hidden_reportno).val();
			var id = $(that).attr('id');

			// レポート定義位置
			var reportNumber = $.getReportNumber(reportno);
			if (typeof(reportNumber) !== 'number') { alert("レポート定義が見つかりません。"); return false;}

			// マスク削除
			$.removeMask();

			// フォーム情報取得
			$.report[reportNumber].getEasyUI();

			if ($.report[reportNumber].validation(id)) {

				var func_ok = function(){
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
								"json"	: JSON.stringify($.report[reportNumber].jsonStringCsv)
							},
							success: function(json){
								// Excel 出力
								$.report[reportNumber].srccsv(reportno, id);
							}
						});
					}
				};


				// CSVデータを出力します。よろしいですか？
				//$.showMessage("W20015", undefined, func_ok);
				var message = "販売開始日、部門、割引率、正規カット、ダミーコード";
				$.showMessage("EX1097", [message], func_ok);
			} else {
				return false;
			}
		},
		pushCsv_test : function(e){
			//
//			// TODO：仮
//			alert("現在チェックリスト出力機能は停止中です。");
//			return false;


			if ($(this).linkbutton('options').disabled)	return false;

			// レポート番号取得
			var reportno=$($.id.hidden_reportno).val();
			// レポート定義位置
			var reportNumber = $.getReportNumber(reportno);
			if (typeof(reportNumber) !== 'number') { alert("レポート定義が見つかりません。"); return false;}

			// JS情報取得
			var that = $.report[reportNumber];
			var id = $(this).attr('id');

			// チェック・確認処理
			var rtn = false;

			if($.isFunction(that.outputFtpValidation)) { rtn = that.outputFtpValidation(id);}
			if(rtn){
				var func_ok = function(r){
					// セッションタイムアウト、利用時間外の確認
					var isTimeout = $.checkIsTimeout();
					if (! isTimeout) {
						// ログの書き込み
						$.ajax({
							url: $.reg.easy,
							type: 'POST',
							async: false,
							data: {
								"page"	: reportno ,
								"obj"	: id,
								"sel"	: new Date().getTime(),
								"userid": $($.id.hidden_userid).val(),
								"user"	: $($.id.hiddenUser).val(),
								"report": $($.id.hiddenReport).val(),
								"json"	: ""
							},
							success: function(json){
								that.outputFtpSuccess(id,reportno);
							}
						});
					}
					return true;
				};
				var message = "販売開始日"+that.hbstdt2+"、部門"+that.bmncd + "、割引率"+that.waribiki1 + "、正規カット"+that.seiki1 + "、ダミーコード"+that.dummycd2;
				$.showMessage("EX1097", [message], func_ok);
				//$.showMessage("W20030", undefined, func_ok);
			}
		},
		outputFtp:function(e){
			//
//			// TODO：仮
			alert("現在チェックリスト出力機能は停止中です。");
			return false;


			if ($(this).linkbutton('options').disabled)	return false;

			// レポート番号取得
			var reportno=$($.id.hidden_reportno).val();
			// レポート定義位置
			var reportNumber = $.getReportNumber(reportno);
			if (typeof(reportNumber) !== 'number') { alert("レポート定義が見つかりません。"); return false;}

			// JS情報取得
			var that = $.report[reportNumber];
			var id = $(this).attr('id');

			// チェック・確認処理
			var rtn = false;

			if($.isFunction(that.outputFtpValidation)) { rtn = that.outputFtpValidation(id);}
			if(rtn){
				var func_ok = function(r){
					// セッションタイムアウト、利用時間外の確認
					var isTimeout = $.checkIsTimeout();
					if (! isTimeout) {
						// ログの書き込み
						$.ajax({
							url: $.reg.easy,
							type: 'POST',
							async: false,
							data: {
								"page"	: reportno ,
								"obj"	: id,
								"sel"	: new Date().getTime(),
								"userid": $($.id.hidden_userid).val(),
								"user"	: $($.id.hiddenUser).val(),
								"report": $($.id.hiddenReport).val(),
								"json"	: ""
							},
							success: function(json){
								that.outputFtpSuccess(id,reportno);
							}
						});
					}
					return true;
				};
				$.showMessage("W20030", undefined, func_ok);
			}
		},
		outputFtpValidation: function (){	// （必須）批准
			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = true;

			var that = this;

			var rt = true;

			// ① 部門：数値2桁。チェックリスト系のボタンを押す場合のみ必須
			/*var bmncd = $.getInputboxValue($('#'+$.id.SelBumon));
			if(bmncd == '-1'){
				// E20037	部門コードを選択してください。	 	0	 	E
				$.showMessage("E20037", undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_bmncd), true)});
				return false;
			}*/

			// 選択行
			/*var row = $($.id.gridholder).datagrid("getSelected");

			// ② 画面に選択した1行の催し情報を出力する。何も選択しないと、エラー。
			if(!row){
				$.showMessage('E00008');
				return false;
			}*/

			return rt;
		},
		outputFtpSuccess: function(id,reportno){

			var that = this;

			var row = $($.id.gridholder).datagrid("getSelected");



			var fileName ="MDCR010";
			var datalen  = 251;

			var title = 'ファイル名：';
			var br = '<br>'

			var json = [{
				"callpage":that.name,
				"FILE":fileName,
				"DREQKIND":1,			// ≒SQL実行回数
				"REQLEN":datalen,
				"HBSSTDT":that.hbstdt1,
				"BMNCD":that.bmncd,
				"WARIBIKI":that.waribiki2,
				"SEIKI":that.seiki2,
				"DUMMY":that.dummycd1,
				"BTN":id
			}];

			// 処理時間計測用
			that.timeData = (new Date()).getTime();

			// Loading表示
			var msgCreate = '<font size="4px">　　送信中</font>';
			msgCreate += br+'<font size="2px">しばらくお待ちください</font>';
			msgCreate += br+br+'<font size="2px">'+title+fileName+'</font>';

			var panel = parent.$("#container");
			var msg=$("<div class=\"datagrid-mask-msg\" style=\"display:block;left:50%;\"></div>").html(msgCreate).appendTo(panel);
			msg._outerHeight(120);
			msg._outerWidth(200);
			msg.css({marginLeft:(-msg.outerWidth()/2),lineHeight:("25px")});

			$.ajax({
				url: $.reg.ftp,
				type: 'POST',
				async: false,
				data: {
					"page"	: reportno ,
					"obj"	: id,
					"sel"	: new Date().getTime(),
					"userid": $($.id.hidden_userid).val(),
					"user"	: $($.id.hiddenUser).val(),
					"report": $($.id.hiddenReport).val(),
					"json"	: JSON.stringify(json)
				},
				success: function(json){
					if (JSON.parse(json).length > 0) {
						$.removeMask();
						$.removeMaskMsg();

						// 正常終了の場合
						if (JSON.parse(json)[0].status==='0') {
							$.showMessage('IX1074',['',br,br+br+title+fileName]);
						} else if (JSON.parse(json)[0].code!=='530') {
							$.showMessage('EX1075',['',JSON.parse(json)[0].com,br+br+title+fileName]);
						} else {
							$.showMessage('EX1076',['',JSON.parse(json)[0].com,br+br+title+fileName]);
						}
					}
				}
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