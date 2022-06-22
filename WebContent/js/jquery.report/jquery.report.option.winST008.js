/**
 * jquery sub window option
 */
;(function($) {

$.extend({

	winST008: {
		name: 'Out_ReportwinST008',
		prefix:'_teninfo',
		suffix:'_winST008',
		initializes: true,		// 初期化フラグ（全体）
		initializesCond: true,	// 初期化フラグ（条件用）
		lastIndex: -1,			// 編集位置（行）
		sortName: '',			// ソート項目名
		sortOrder: '',			// ソート順
		timeData : (new Date()).getTime(),
		callreportno:"",		// 呼出し元レポートNo
		callBtnid: "",			// 呼出ボタンID情報
		sortBtnid: "",			// 並び替えボタンID情報
		focusRootId:"_winST008",	// （キー移動時必須）キー移動イベントのルートとなるパネルのID
		focusParentId:"",		// （キー移動時必須）現在フォーカスがあたっている項目の親となるパネルのID
		focusGridId:"",			// （キー移動時必須）現在フォーカスがあたっているDataGridのID
		editRowIndex:{},					// グリッド編集行保持
		init: function(js) {
			var that = this;
			if(!that.initializes) return false;

			that.callreportno = js.name;
			that.callreportHidden = js.jsonHidden;

			$.extendDatagridEditor(that);

			// dataGrid 初期化
			// メイングリッド
			this.setDataGrid('grd_subwindow'+that.prefix+that.suffix, that);
			// 店番一括入力
			this.setDataGrid2('grd_tencdiinput_list'+that.suffix, that);

			$.setInputBoxDisable($('#'+$.id.chk_rinji+that.suffix));

			// 入力テキストボックス系
			var isUpdateReport = true;
			var inputbox = Object.getOwnPropertyNames($.id_inp);
			for ( var sel in inputbox ) {
				if($('#'+$.id_inp[inputbox[sel]]+that.suffix).length > 0){
					$.setInputbox(that, that.callreportno, $.id_inp[inputbox[sel]]+that.suffix, isUpdateReport);
				}
			}

			// 検索条件初期化
			// TODO
			//$.setInputbox(that, that.callreportno, $.id_inp.txt_bmncd+that.suffix, false);
			// 検索条件初期化
			$.setInputbox(that, that.callreportno, $.id_inp.txt_moyscd+that.suffix, false);
			// チェックボックス
			$.setCheckboxInit(that.jsonHidden, 'chk_rinji'+that.suffix, false);

			// 呼出しボタンイベント設定
			// ST007の場合
			$('[id^=btn'+that.prefix+']').each(function(){
				var id = $(this).attr('id');
				$('#'+id).click(function() { that.Open(this); });
			});

			//$('#'+$.id.btn_upd+that.suffix).click(function() { that.updSuccess('grd_subwindow'+that.prefix+that.suffix); });
			// 登録(DB更新処理) クリックイベント
			$('#'+$.id.btn_upd+that.suffix).on("click", that.pushUpd);


			// 検索
			$('#'+$.id.btn_search+that.suffix).on("click", that.Search);
			// キャンセル
			$('#'+$.id.btn_cancel+that.suffix).on("click", that.Cancel);
			// 選択
			$('#'+$.id.btn_select+that.suffix).on("click", that.Select);

			// 実績参照
			$('#'+$.id.btn_jissekirefer+that.suffix).on("click", function(e){
				that.Change()
			});

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
					$('#'+js.focusParentId).find('[tabindex]').filter("[tabindex!=-1]").filter('[disabled!=disabled]').filter(":visible").eq(0).focus();
				},
				onBeforeClose:function(){
					// ウインドウ展開中リサイズイベント有効化
					$.reg.resize = true;
					that.Clear();
					js.focusParentId = js.focusRootId;
				},
				onClose:function(){
					$('#'+js.focusParentId).find('#'+that.callBtnid).focus();
				}
			});
			// 店番順ボタン
			$('#'+$.id.btn_tennoorder+that.suffix).on("click", function(e){
				//$('#grd_subwindow_teninfo'+that.suffix).datagrid('loading');
				if (that.sortOrder==="" || (that.sortBtnid_==="TENNO" && that.sortOrder==="ASC")) {
					that.sortOrder = "DESC";
				} else {
					that.sortOrder = "ASC";
				}
				that.sortBtnid_ = "TENNO";
				that.sortGridRows('#grd_subwindow_teninfo'+that.suffix, that.sortBtnid_, that.sortOrder);
				//$('#grd_subwindow_teninfo'+that.suffix).datagrid('loaded');
			});
			// ランク順ボタン
			$('#'+$.id.btn_rankorder+that.suffix).on("click", function(e){
				var count = 0;
				var rows = $('#grd_subwindow_teninfo'+that.suffix).datagrid('getRows');
				for (var j=0; j<rows.length; j++){
					var rank = rows[j]["RANK"]
					if (rank != undefined && rank != null && rank != "" && rank != " ") {
						count++;
						break;
					}
				}
				if (count > 0) {
					if (that.sortBtnid_==="RANKNO" && that.sortOrder==="ASC") {
						that.sortOrder = "DESC";
					} else {
						that.sortOrder = "ASC";
					}
					that.sortBtnid_ = "RANKNO";
					that.sortGridRows('#grd_subwindow_teninfo'+that.suffix, that.sortBtnid_, that.sortOrder);
				}
			});
			// 実績順ボタン
			$('#'+$.id.btn_jissekiorder+that.suffix).on("click", function(e){
				var count = 0;
				var rows = $('#grd_subwindow_teninfo'+that.suffix).datagrid('getRows');
				for (var j=0; j<rows.length; j++){
					var hbj = rows[j]["SANKOUHBJ"]
					if (hbj != undefined && hbj != null && hbj != "" && hbj != " ") {
						count++;
						break;
					}
				}
				if (count > 0) {
					if (that.sortBtnid_==="SANKOUHBJ" && that.sortOrder==="ASC") {
						that.sortOrder = "DESC";
					} else {
						that.sortOrder = "ASC";
					}
					that.sortBtnid_ = "SANKOUHBJ";
					that.sortGridRows('#grd_subwindow_teninfo'+that.suffix, that.sortBtnid_, that.sortOrder);
				}
			});
			// 設定ボタン
			$('#'+$.id.btn_set+that.suffix).on("click", function(e){
				var row = $('#'+$.id.grd_tencdiinput+'_list'+that.suffix).datagrid("getSelected");
				var rowIndex = $('#'+$.id.grd_tencdiinput+'_list'+that.suffix).datagrid("getRowIndex", row);
				$('#'+$.id.grd_tencdiinput+'_list'+that.suffix).datagrid('endEdit',rowIndex);
				// 入力チェック
				var txt_rankiinput	= $('#'+$.id_inp.txt_rankiinput+that.suffix).textbox('getValue');
				if(!txt_rankiinput) {
					$.showMessage('E20121', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_rankiinput+that.suffix), true)});
					return false;
				}
				var targetTencdiinput = that.getGridData2($.id.grd_tencdiinput+'_list'+that.suffix);
				if (targetTencdiinput.length == 0) {
					$.showMessage('E20120', undefined, function(){$.addErrState(that, $('#'+$.id_inp.targetTencdiinput+that.suffix), true)});
					return false;
				}
				// 存在チェック：店コード
				for (var j=0; j < targetTencdiinput.length; j++) {
					var target = targetTencdiinput[j]["F1"];
					var msgid = that.checkInputboxFunc($.id_inp.txt_tencd+that.suffix, target , '');
					if(msgid !==null){
						$.showMessage(msgid);
						return false;
					}
				}
				var txt_rankiinput	 = $('#'+$.id_inp.txt_rankiinput+that.suffix).textbox('getValue');
				that.setGridData(that, $.id.grd_tencdiinput+that.suffix, txt_rankiinput);
				that.getTenNumber(that, $.id.grd_teninfo);

				// 初期化
				$('#'+$.id.grd_tencdiinput + '_list').datagrid('reload');
			});

			// チェックボックスの設定
			$.initCheckboxCss($("#"+that.focusRootId));
			// キーイベントの設定
			$.initKeyEvent(that);

			that.initializes = !that.initializes;
		},
		getTenNumber: function (that, id){

			// 入力中の場合、編集を終了させる。
			var row = $('#grd_subwindow_teninfo'+that.suffix).datagrid("getSelected");
			if(row){
				var rowIndex = $('#grd_subwindow_teninfo'+that.suffix).datagrid("getRowIndex", row);
				$('#grd_subwindow_teninfo'+that.suffix).datagrid('endEdit',rowIndex);
			}

			var target = $('#'+$.id_inp.txt_ten_number+that.suffix);
			var count = 0;
			var targetRowsTeninfo = that.getGridData($.id.grd_teninfo+'_list'+that.suffix);

			for (var i=0; i<targetRowsTeninfo.length; i++){
				var rank = targetRowsTeninfo[i]["F3"]
				if (rank != undefined && rank != null && rank != "" && rank != " ") {
					count++;
				}
			}
			$.setInputboxValue(target, count, "");			// 店舗数
		},
		setGridData: function (that, id, newValue){
			var that = this;
			var rows = [];
			rows = that.getGridData($.id.grd_teninfo+'_list'+that.suffix);

			if(id===undefined || id===$.id.grd_tencdiinput+that.suffix){						// 一括入力

				var inputRows = that.getGridData2($.id.grd_tencdiinput+'_list'+that.suffix);
				var txt_rankiinput	 = $('#'+$.id_inp.txt_rankiinput+that.suffix).textbox('getValue');
				for (var i=0; i<rows.length; i++){
					var rt = false;
					for (var j=0; j < inputRows.length; j++) {
						if (rows[i]["F1"] == ('000' + inputRows[j]["F1"]).slice( -3 )) {
							rt = true;
							break;
						}
					}
					if (rt) {
						// データグリッドを更新
						$('#grd_subwindow_teninfo'+that.suffix).datagrid('updateRow',{
							index: i,
							row: { RANK:newValue }
						})
					}
				}
			}
			if (id===undefined || id===$.id_inp.txt_rank) {										// ランク入力

				if (newValue == "") {
					newValue = " ";
				}
				for (var i=0; i<rows.length; i++){
					var idx = that.editRowIndex[that.focusGridId] + 1;

					if (rows[i]["F1"] ==  ('000' + idx).slice( -3 )) {
						// データグリッドを更新
						$('#'+$.id.grd_teninfo + '_list').datagrid('updateRow',{
							index: i,
							row: { RANK:newValue }
						})
					}
				}
			}
			that.updData = that.getGridData($.id.grd_teninfo+'_list');
		},
		checkInputboxFunc:function(id, newValue, kbn, record, isNew){
			var that = this;

			// 店番
			if(id===$.id_inp.txt_tencd+that.suffix){
				var param = {};
				param["KEY"] =  "MST_CNT";
				param["value"] = newValue;
				var chk_cnt = $.getInputboxData(that.name, $.id.action_check, $.id_inp.txt_tencd, [param]);
				if(chk_cnt!=="" && chk_cnt =="0"){
					return "EX1077";
				}
			}
			return null;
		},
		Open: function(obj) {
			if ($(obj).is("disabled","disabled"))	return false;
			var that = $.winST008;
			that.callBtnid = $(obj).attr('id');

			if(that.callBtnid === 'btn'+that.prefix){
				// ST007の時

			}

			var txt_bmncd = $.getJSONValue(that.callreportHidden, $.id_inp.txt_bmncd);
			txt_bmncd = ('00' + txt_bmncd).slice(-2);

			var rankno = $.getJSONValue(that.callreportHidden, $.id_inp.txt_rankno);

			// 入力がなければ新規
			if ($.isEmptyVal(rankno)) {
				$.setInputBoxEnableVariable($('#'+$.id_inp.txt_rankno+that.suffix));
				$.setInputBoxEnableVariable($('#'+$.id_inp.txt_rankkn+that.suffix));
			} else {
				$.setInputBoxDisableVariable($('#'+$.id_inp.txt_rankno+that.suffix));
				$.setInputBoxDisableVariable($('#'+$.id_inp.txt_rankkn+that.suffix));
			}

			// 検索条件初期化
			$.setInputboxValue($('#'+$.id_inp.txt_bmncd+that.suffix), txt_bmncd);																// 部門コード
			$.setInputboxValue($('#'+$.id_inp.txt_rankno+that.suffix), rankno);																	// ランク№
			$.setInputboxValue($('#'+$.id_inp.txt_rankkn+that.suffix), $.getJSONValue(that.callreportHidden, $.id_inp.txt_rankkn));				// ランク名称
			$.setInputboxValue($('#'+$.id_inp.txt_ten_number+that.suffix), $.getJSONValue(that.callreportHidden, $.id_inp.txt_ten_number));		// 店舗数
			$.setInputboxValue($('#'+$.id.chk_rinji+that.suffix), $.getJSONValue(that.callreportHidden, $.id.chk_rinji));						// 臨時
			$.setInputboxValue($('#'+$.id_inp.txt_moyscd+that.suffix), $('#'+$.id_inp.txt_moyscd).textbox('getText'));							// 催しコード

			that.Search();

			// window 表示
			$('#'+that.suffix).window('open');
		},
		Clear:function(){
			var that = $.winST008;
			that.initializesCond = true;
			// グリッド初期化
			$('#grd_subwindow'+that.prefix+that.suffix).datagrid('clearSelections').datagrid({data: []}).datagrid('getPager').pagination('select', 1);	// ページ初期化
			that.initializesCond = false;
		},
		Search: function(){
			var that = $.winST008;

			// validate=falseの場合何もしない
			if(!that.validation()){ return false; }

			// セッションタイムアウト、利用時間外の確認
			var isTimeout = $.checkIsTimeout();
			if (! isTimeout) {
				// 検索実行
				//that.success("grd_subwindow"+that.prefix+that.suffix);
				that.success("grd_subwindow"+that.prefix);
				that.success2("grd_tencdiinput_list");
			}
			return true;
		},
		Cancel:function(){
			var that = $.winST008;
			$('#'+that.suffix).window('close');
			return true;
		},
		Select: function(){
			var that = $.winST008;

			var row = $("#grd_subwindow"+that.prefix+that.suffix).datagrid("getSelected");
			if(!row){
				$.showMessage('E00008');
				return false;
			}

			// 取得した情報を、オブジェクトに設定する
			// 設定先の判定：オブジェクトに for_btn,for_inpタグなどを使用して呼出し元(呼出しボタン名)と列名が設定されている項目
			var isSet = $.setInputboxRowData('for_btn', that.callBtnid, row);
			if(isSet){
				$('#'+that.suffix).window('close');
			}
			return true;
		},
		Change:function (){
			var that = $.winST008;

			// 呼出し元別処理
			switch (that.callreportno) {
			case 'Out_ReportJU012':
			case 'Out_ReportJU032':
				// ST008へ遷移
				var rinji		 = $.getInputboxValue($('#'+$.id.chk_rinji+that.suffix));
				var bmncd		 = $.getInputboxValue($('#'+$.id_inp.txt_bmncd+that.suffix));
				var rankno		 = $.getInputboxValue($('#'+$.id_inp.txt_rankno+that.suffix));		// ランクNo
				var rankkn		 = $.getInputboxValue($('#'+$.id_inp.txt_rankkn+that.suffix));		// ランク名称
				var moyscd		 = $.getJSONValue(that.callreportHidden, $.id_inp.txt_moyscd);		// 催しコード

				$.setJSONObject(that.callreportHidden, $.id_inp.txt_rankno,	rankno,	rankno);		// ランクNo
				$.setJSONObject(that.callreportHidden, $.id_inp.txt_rankkn,	rankkn,	rankkn);		// ランク名称
				$.setJSONObject(that.callreportHidden, $.id_inp.txt_bmncd,	bmncd,	bmncd);			// 部門
				$.setJSONObject(that.callreportHidden, $.id.chk_rinji, rinji, rinji);				// 臨時
				$.setJSONObject(that.callreportHidden, $.id_inp.txt_moyscd, moyscd, moyscd);		// 催しコード
				$.setJSONObject(that.callreportHidden, 'changeBtnid',	that.changeBtnid,	that.changeBtnid);

				$('#btn_zitref_winST008').click();
				break;
			default:
				break;
			}
		},
		validation: function (){	// （必須）批准
			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform+that.suffix).form('validate');
			return rt;
		},
		success: function(id){	// 検索処理
			var that = $.winST008;

			// 処理時間計測用
			that.timeData = (new Date()).getTime();

			// グリッド初期化&ローディング
			$('#'+id).datagrid('loading');

			// 情報設定
			var json = [{}];

			// 呼出し元別処理
			switch (that.callreportno) {
			//case 'Out_ReportST007':
			case 'Out_ReportJU012':
			case 'Out_ReportJU032':
				// オブジェクト作成
				//var abc = $('#'+$.id_inp.txt_shncd).textbox('getValue');
				var chk_rinji	 = $.getInputboxValue($('#'+$.id.chk_rinji+that.suffix));
				var txt_bmncd	 = $.getJSONValue(that.callreportHidden, $.id_inp.txt_bmncd);
				var txt_rankno	 = $.getJSONValue(that.callreportHidden, $.id_inp.txt_rankno);
				var moyscdArray	 = $('#'+$.id_inp.txt_moyscd).textbox('getText').split("-");
				var sortBtnId	 = that.sortBtnid;
				var changeBtnid	 = 'btn_' + $.getJSONValue(that.callreportHidden, 'changeBtnid');
				var txt_tenten_arr	 = $.getJSONValue(that.callreportHidden, $.id_inp.txt_tenten_arr);	// 点数配列

				json = [{
					callpage:	$($.id.hidden_reportno).val(),										// 呼出元レポート名
					BTN:		'test',
					MOYSCD:		$('#'+$.id_inp.txt_moyscd).textbox('getValue'),		// 催しコード
					RINJI		:chk_rinji,
					BMNCD		:txt_bmncd,
					RANKNO		:txt_rankno,
					MOYSKBN		:moyscdArray[0],
					MOYSSTDT	:moyscdArray[1],
					MOYSRBAN	:moyscdArray[2],
					TENTENARR	:txt_tenten_arr,
					SORTBTN		:sortBtnId,
					SENDBTNID	:changeBtnid,
					//MOYSCD		:txt_moyscd,
					MODE:'1',

				}];
				break;
			default:
				break;
			}

			$.post(
				$.reg.easy,
				{
					page	:	that.name,										// レポート名
					//page	:	'Out_ReportwinST008',
					obj		:	id,
					//obj		:	$.id.grd_teninfo+'_list',
					sel		:	(new Date()).getTime(),
					target	:	id,
					action	:	$.id.action_init,
					json	:	JSON.stringify(json),
					datatype:	'datagrid'
				},
				function(data){
					// ログ出力
					$.log(that.timeData, 'query:');
					var dg =$('#'+id+that.suffix);
					if(data!==""){
						// JSONに変換
						var json = JSON.parse(data);

						// 結果表示
						dg.datagrid('loadData', json.rows);
					}
					dg.datagrid('loaded');
					// ログ出力
					$.log(that.timeData, 'loaded:');
				}
			);
		},
		success2: function(id){	// 検索処理_一括入力画面
			var that = $.winST008;

			// 処理時間計測用
			that.timeData = (new Date()).getTime();

			// グリッド初期化&ローディング
			$('#'+id).datagrid('loading');

			// 情報設定
			var json = [{}];

			// 呼出し元別処理
			switch (that.callreportno) {
			//case 'Out_ReportST007':
			case 'Out_ReportJU012':
			case 'Out_ReportJU032':
				// オブジェクト作成
				//var abc = $('#'+$.id_inp.txt_shncd).textbox('getValue');
				var chk_rinji	 = $.getInputboxValue($('#'+$.id.chk_rinji+that.suffix));
				var txt_bmncd	 = $.getJSONValue(that.callreportHidden, $.id_inp.txt_bmncd);
				var txt_rankno	 = $.getJSONValue(that.callreportHidden, $.id_inp.txt_rankno);
				var moyscdArray	 = $('#'+$.id_inp.txt_moyscd).textbox('getText').split("-");
				var sortBtnId	 = that.sortBtnid;

				json = [{
					callpage:	$($.id.hidden_reportno).val(),										// 呼出元レポート名
					/*BTN:		'test',
					BMNCD:		abc.substring(0, 2),		//
					MOYSCD:		$('#'+$.id_inp.txt_moyscd).textbox('getValue'),		//
					*/
					RINJI		:chk_rinji,
					//BMNCD		:txt_bmncd,
					//RANKNO	:txt_rankno,
					BMNCD		:'1',
					RANKNO		:'1',
					MOYSKBN		:moyscdArray[0],
					MOYSSTDT	:moyscdArray[1],
					MOYSRBAN	:moyscdArray[2],
					SORTBTN		:sortBtnId,
					//MOYSCD		:txt_moyscd,
					MODE:'1',

				}];
				break;
			default:
				break;
			}

			$.post(
				$.reg.easy,
				{
					page	:	that.name,										// レポート名
					//page	:	'Out_ReportwinST008',
					obj		:	id,
					//obj		:	$.id.grd_teninfo+'_list',
					sel		:	(new Date()).getTime(),
					target	:	id,
					action	:	$.id.action_init,
					json	:	JSON.stringify(json),
					datatype:	'datagrid'
				},
				function(data){
					// ログ出力
					$.log(that.timeData, 'query:');
					var dg =$('#'+id+that.suffix);
					if(data!==""){
						// JSONに変換
						var json = JSON.parse(data);

						// 結果表示
						dg.datagrid('loadData', json.rows);
					}
					dg.datagrid('loaded');
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
			// 仕入先コード
			this.jsonTemp.push({
				id:		$.id_inp.txt_moyscd,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_moyscd),
				text:	''
			});
		},
		extenxDatagridEditorIds:{
			RANK		: "txt_rank_winST008"			// ランク
		},
		setDataGrid: function(id, that) {
			var that = this;
			var funcClickRow = $.fn.datagrid.defaults.onClickRow;
			var funcBeginEdit= $.fn.datagrid.defaults.onBeginEdit;
			var funcEndEdit= $.fn.datagrid.defaults.onEndEdit;

			// 呼出し元別処理
			var columns = null;
			switch (that.callreportno) {
			case 'Out_ReportST007':
			case 'Out_ReportBT002':
			case 'Out_ReportJU012':
			case 'Out_ReportJU032':
			case 'Out_ReportGM003':
				// 入力可能に設定
				that.editRowIndex[id] = -1;
				funcClickRow = function(index,field){$.clickEditableDatagridCell(that,id, index)};
				funcBeginEdit = function(index,row){$.beginEditDatagridRow(that,id, index, row)}
				funcEndEdit = function(index,row,changes){$.endEditDatagridRow(that, id, index, row)};

				// オブジェクト作成
				columns = [[
							{field:'TENCD',		title:'店番',			width: 70 	,halign:'center',align:'left'},
							{field:'TENKN',		title:'店舗名',			width: 200 	,halign:'center',align:'left'},
							{field:'RANK',		title:'ランク',			width: 70 	,halign:'center',align:'left',editor:{type:'textbox'}},
							{field:'SANKOUHBJ',	title:'参考販売実績',	width: 100 	,halign:'center',align:'right'},
							{field:'AREACD',	title:'エリア',			width: 70 	,halign:'center',align:'left'},
						]];
				break;
			default:
				break;
			}

			$('#'+id).datagrid({
				nowrap: true,
				border: true,
				striped: true,
				collapsible:false,
				remoteSort: true,
				rownumbers:true,
				fit:true,
				//view:scrollview,
				frozenColumns:[[]],
				columns:columns,
				fitColumns:false,	// 指定カラム幅を適用する場合、false 指定。
				rowStyler:function(index, row){
				},
				onLoadSuccess:function(data){
					that.getTenNumber(that, $.id.grd_teninfo);
				},
				onClickRow: funcClickRow,
				onBeginEdit:funcBeginEdit,
				onEndEdit: funcEndEdit,
				onBeforeEdit:function(index,row){
					var changeBtnid	 = $.getJSONValue(that.callreportHidden, 'changeBtnid');

					if(changeBtnid ==='new'){
						if(row.EDITFLG !='1'){
							// 次の行に移るか、次の項目に移るかする
							var adds = that.editRowIndex[that.focusGridId] > index ? -1:1;
							var nextindex = index + adds;
							if(nextindex >= 0 && nextindex < $('#'+id).datagrid('getRows').length){
								//$('#'+id).datagrid('endEdit', index);

								$('#'+id).datagrid('selectRow', nextindex);
								$('#'+id).datagrid('beginEdit', nextindex);
								$('#'+id).datagrid('updateRow',{
									index: index,
									row: {
										RANK:''
	                                }
								})

							}else{
								that.editRowIndex[that.focusGridId] = -9;	// 次の項目に移動するために未編集状態と違うインデックス設定
								var evt = $.Event('keydown');
								evt.keyCode = 13;
								evt.shiftKey = adds === -1;
								$('#'+id).parents('.datagrid').eq(0).trigger(evt);
							}
							//$('#'+$.id_inp.txt_rank+"_").val("");
							return false;
						}
					}
				},
				autoRowHeight:false,
				pagination:false,
				pagePosition:'bottom',
				singleSelect:true
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
		setDataGrid2: function(id, that) {	// 店番一括入力グリッド用
			var that = this;
			var funcClickRow = $.fn.datagrid.defaults.onClickRow;
			var funcBeginEdit= $.fn.datagrid.defaults.onBeginEdit;
			var funcEndEdit= $.fn.datagrid.defaults.onEndEdit;

			var targetId = $.id_inp.txt_tencd;
			var check = $('#'+targetId).attr("check") ? JSON.parse('{'+$('#'+targetId).attr("check")+'}'): JSON.parse('{}');		// 店コードのcheck要素を取得
			var formatterLPad = function(value){
				return $.getFormatLPad(value, check.maxlen);
			};

			// 呼出し元別処理
			var columns = null;
			switch (that.callreportno) {
			case 'Out_ReportST007':
			case 'Out_ReportBT002':
			case 'Out_ReportJU012':
			case 'Out_ReportJU032':
			case 'Out_ReportGM003':
				// 入力可能に設定
				that.editRowIndex[id] = -1;
				funcClickRow = function(index,field){$.clickEditableDatagridCell(that,id, index)};
				funcBeginEdit = function(index,row){$.beginEditDatagridRow(that,id, index, row)}
				funcEndEdit = function(index,row,changes){$.endEditDatagridRow(that, id, index, row)};

				// オブジェクト作成
				columns = [[
							{field:'TENCD',	title:'店番',	width: 70 	,halign:'center',align:'left',editor:{type:'numberbox'},formatter:formatterLPad},
						]];
				break;
			default:
				break;
			}

			$('#'+id).datagrid({
				nowrap: true,
				border: true,
				striped: true,
				collapsible:false,
				remoteSort: true,
				rownumbers:true,
				fit:true,
				//view:scrollview,
				frozenColumns:[[]],
				columns:columns,
				fitColumns:false,	// 指定カラム幅を適用する場合、false 指定。
				rowStyler:function(index, row){
				},
				onLoadSuccess:function(data){
				},
				onClickRow: funcClickRow,
				onBeginEdit:funcBeginEdit,
				onEndEdit: funcEndEdit,
				autoRowHeight:false,
				pagination:false,
				pagePosition:'bottom',
				singleSelect:true
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
		updValidation: function (){	// （必須）批准
			var that = $.winST008;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform + '_' + that.suffix).form('validate');
			$.endEditingDatagrid(that);	// grid系end
			if(!rt){
				$.addErrState(that, $('.validatebox-invalid').eq(2), false);
				return rt;
			}

			var txt_rankno		= $('#'+$.id_inp.txt_rankno+that.suffix).textbox('getValue');							// ランクNo.
			var chk_rinji		= $('#'+$.id.chk_rinji+that.suffix).is(':checked') ? $.id.value_on : $.id.value_off; 	// 臨時
			var txt_rankkn		= $('#'+$.id_inp.txt_rankkn+that.suffix).textbox('getValue');							// ランク名称
			var txt_moyscd		= $.getJSONValue(that.callreportHidden, $.id_inp.txt_moyscd);

			var txt_moyscd		= $('#'+$.id_inp.txt_moyscd).textbox('getValue'); 							// 催しコード

			// 入力チェック
			if (rt) {
				if (!txt_rankno) {
					$.showMessage('EX1086', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_rankno), true)});
					rt = false;
				}
			}
			if (rt) {
				if (chk_rinji==="1" && txt_rankno < 900) {
					$.showMessage('EX1085', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_rankno), true)});
					rt = false;
				}
			}
			if (rt) {
				if (chk_rinji==="0" && txt_rankno >= 900) {
					$.showMessage('EX1066', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_rankno), true)});
					rt = false;
				}
			}
			if (rt) {
				if (chk_rinji==="1" && !txt_moyscd) {
					$.showMessage('EX1026', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_moyscd), true)});
					rt = false;
				}
			}
			if (rt) {
				if (!txt_rankkn) {
					$.showMessage('EX1087', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_rankkn), true)});
					rt = false;
				}
			}
			if (rt) {	// ランクの空白チェック
				var count = 0;
				var targetRowsTeninfo = that.getGridData($.id.grd_teninfo+'_list'+that.suffix);
				for (var i=0; i<targetRowsTeninfo.length; i++){
					var rank = targetRowsTeninfo[i]["F3"]
					if (rank != undefined && rank != null && rank.trim() != "") {
						count++;
					}
				}
				if (count == 0) {
					$.showMessage('E20121');
					rt = false;
				}
			}
			// 更新データ存在チェック
			//that.checkUpdData(that);
//			var chk = that.checkUpdData(that);
//			if (!chk) {
////				$($.id.hiddenChangedIdx).val('');
//				rt = false;
//			}

			return rt;
		},
		updSuccess: function(id, that){	// validation OK時 の update処理
			var that = $.winST008;

			var targetDatas	 = [{}];
			var chk_rinji	 = $.getInputboxValue($('#'+$.id.chk_rinji+that.suffix));		// 臨時

			if(chk_rinji == '1'){
				// 臨時チェック有り
				var bmncd		 = $.getInputboxValue($('#'+$.id_inp.txt_bmncd+that.suffix));		// 部門
				var rankno		 = $.getInputboxValue($('#'+$.id_inp.txt_rankno+that.suffix));		// ランクNo
				var rankkn		 = $.getInputboxValue($('#'+$.id_inp.txt_rankkn+that.suffix));		// ランク名称
				var myoscd		 = $.getJSONValue(that.callreportHidden, $.id_inp.txt_moyscd);		// 催しコード
				var szMoyskbn	 = myoscd.substring(0,1);											// 催し区分
				var szMoysstdt	 = myoscd.substring(1,7);											// 催しコード（催し開始日）
				var szMoysrban	 = myoscd.substring(7,10);											// 催し連番

				targetDatas = [{
					F1:		bmncd,
					F2:		szMoyskbn,
					F3:		szMoysstdt,
					F4:		szMoysrban,
					F5:		rankno,
					F6:		rankkn,
					F7:		"",
				}]
			}else{
				$('#'+that.focusRootId).find('[col^=F]').each(function(){
					var col = $(this).attr('col');
					targetDatas[0][col] = $.getInputboxValue($(this));
				});
			}

			//var targetRowsTeninfo = [];
			var targetRowsTeninfo = that.getGridData($.id.grd_teninfo+'_list'+that.suffix);


			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
//			$.appendMaskMsg();

			$.post(
				$.reg.jqgrid ,
				{
					report:			'Out_ReportST008',					// レポート名
					action:			$.id.action_update,					// 実行処理情報
					obj:			id,									// 実行オブジェクト
					//SENDBTNID:		that.sendBtnid,
					DATA:			JSON.stringify(targetDatas),		// 更新対象情報
					DATA_TENINFO:	JSON.stringify(targetRowsTeninfo),	// 更新対象情報
					t:				(new Date()).getTime()
				},
				function(data){
					// 検索処理エラー判定
					if($.updError(id, data)) return false;

					var afterFunc = function(){
						// 初期化
						//that.clear();
						//that.changeReport(that.name, $.id.btn_back);
						// windowを閉じる
						var that = $.winST008;
						$('#'+that.suffix).window('close');
						if($.winST010){
							$.winST010.Search();
						}
					};
					$.updNormal(data, afterFunc);

					// ログ出力
					$.log(that.timeData, 'loaded:');
				}
			);
		},
		getGridData: function (target){
			var that = $.winST008;
			var targetRows= [];
			if(target===undefined || target===$.id.grd_teninfo+'_list'+that.suffix){
				var rowsTeninfo= $('#grd_subwindow_teninfo'+that.suffix).datagrid('getRows');
				for (var i=0; i<rowsTeninfo.length; i++){
					if(rowsTeninfo[i]["TENCD"] == "" || rowsTeninfo[i]["TENCD"] == null ){

					}else{
						var rowDate = {
								F1	 : rowsTeninfo[i]["TENCD"],
								F2	 : rowsTeninfo[i]["TENKN"],
								F3	 : rowsTeninfo[i]["RANK"],
								F4	 : rowsTeninfo[i]["SANKOUHBJ"],
								F5	 : rowsTeninfo[i]["AREACD"],
							};
						targetRows.push(rowDate);
					}
				}
			}
			return targetRows;
		},
		getGridData2: function (target){
			var that = $.winST008;
			var targetRows= [];
			if(target===undefined || target===$.id.grd_tencdiinput+'_list'+that.suffix){
				var rowsTencdiinput= $('#'+$.id.grd_tencdiinput+'_list' + that.suffix).datagrid('getRows');
				for (var i=0; i<rowsTencdiinput.length; i++){
					if(rowsTencdiinput[i]["TENCD"] == "" || rowsTencdiinput[i]["TENCD"] == null ){

					}else{
						var rowDate = {
								F1	 : rowsTencdiinput[i]["TENCD"],
							};
						targetRows.push(rowDate);
					}
				}
			}
			return targetRows;
		},
		/**
		 * 登録(DB更新)ボタンイベント
		 * @param {Object} e
		 */
		pushUpd:function(e){
			var that = $.winST008;
			if ($(this).linkbutton('options').disabled)	return false;

			// レポート番号取得
			var reportno=$($.id.hidden_reportno).val();
			// レポート定義位置
			var reportNumber = $.getReportNumber(reportno);
			if (typeof(reportNumber) !== 'number') { alert("レポート定義が見つかりません。"); return false;}

			// JS情報取得
			//var that = $.report[reportNumber];
			var id = $(this).attr('id');

			// チェック・確認処理
			var rtn = true;
			if($.isFunction(that.updValidation)) { rtn = that.updValidation(id);}
			// 変更情報チェック
			if(rtn && !$.getConfirmUnregistFlg($($.id.hiddenChangedIdx))){
				$.showMessage('E20582');
				return false;
			}

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
								that.updSuccess(id);
							}
						});
					}
					return true;
				};
				if($.isFunction(that.updConfirm)) {
					that.updConfirm(func_ok);
				}else{
					$.showMessage("W00001", undefined, func_ok);
				}
			}
		},
		sortGridRows: function (id, sortBtnid, sortOrder){
			var taht =this;
			var rows = $(id).datagrid('getRows');
			var count = 0;
			if(rows){
				if (sortBtnid==="TENNO") {
					if (sortOrder==="ASC") {
						rows.sort(function(a,b){
						    if(a.TENCD>b.TENCD) return 1;
						    if(a.TENCD<b.TENCD) return -1;
						    return 0;
						});
					} else {
						rows.sort(function(a,b){
						    if(a.TENCD<b.TENCD) return 1;
						    if(a.TENCD>b.TENCD) return -1;
						    return 0;
						});
					}
					for (var i=0; i<rows.length; i++){
						$(id).datagrid('updateRow',{
							index: i,
							row: { TENCD:rows[i] }
						})
					}
				}
				if (sortBtnid==="RANKNO") {
					if (sortOrder==="ASC") {
						rows.sort(function(a,b){
						    if(String(a.RANK?a.RANK:"") == "" && String(b.RANK?b.RANK:"") == ""){
						    	if(a.TENCD>b.TENCD) return 1;
							    if(a.TENCD<b.TENCD) return -1;

						    }else if(String(a.RANK?a.RANK:"") != "" && String(b.RANK?b.RANK:"") == ""){
						    	return -1;

						    }else if(String(a.RANK?a.RANK:"") == "" && String(b.RANK?b.RANK:"") != ""){
						    	return 1;

						    }else {
							    if(String(a.RANK)>String(b.RANK))return 1;
							    if(String(a.RANK)<String(b.RANK))return -1;
							    if(a.TENCD>b.TENCD) return 1;
							    if(a.TENCD<b.TENCD) return -1;
						    }
						    return 0;
						});
					} else {
						rows.sort(function(a,b){
							//if(String(a.SORTKBN)>String(b.SORTKBN))return 1;
						    //if(String(a.SORTKBN)<String(b.SORTKBN))return -1;
						    if(String(a.RANK)<String(b.RANK))return 1;
						    if(String(a.RANK)>String(b.RANK))return -1;
						    if(String(a.RANK) == String(b.RANK)){
						    	if(a.TENCD>b.TENCD) return 1;
							    if(a.TENCD<b.TENCD) return -1;
						    }
						    return 0;
						});
					}
					for (var i=0; i<rows.length; i++){
						$(id).datagrid('updateRow',{
							index: i,
							row: { RANK:rows[i] }
						})
					}
				}
				if (sortBtnid==="SANKOUHBJ") {
					if (sortOrder==="ASC") {
						rows.sort(function(a,b){
							if(String(a.SORTKBN2)>String(b.SORTKBN2))return 1;
						    if(String(a.SORTKBN2)<String(b.SORTKBN2))return -1;
						    if(parseInt(a.SANKOUHBJ)>parseInt(b.SANKOUHBJ)) return 1;
						    if(parseInt(a.SANKOUHBJ)<parseInt(b.SANKOUHBJ)) return -1;
						    return 0;
						});
					} else {
						rows.sort(function(a,b){
							if(String(a.SORTKBN2)>String(b.SORTKBN2))return 1;
						    if(String(a.SORTKBN2)<String(b.SORTKBN2))return -1;
						    if(parseInt(a.SANKOUHBJ)<parseInt(b.SANKOUHBJ)) return 1;
						    if(parseInt(a.SANKOUHBJ)>parseInt(b.SANKOUHBJ)) return -1;
						    return 0;
						});
					}
					for (var i=0; i<rows.length; i++){
						$(id).datagrid('updateRow',{
							index: i,
							row: { TENCD:rows[i] }
						})
					}
				}
			}
		},
		changeInputboxFunc:function(that, id, newValue, obj){

			var parentObj = $('#'+that.focusRootId);
			if(id+"_"===obj.attr('id') && that.focusGridId!==""){
				parentObj = $('#'+that.focusGridId).datagrid('getPanel');
			}
			// ランクNo
			if(id===$.id_inp.txt_rank + that.suffix){
				//that.setGridData(that, $.id_inp.txt_rank, newValue);
				that.getTenNumber(that, id);
			}
		},
	},
});

})(jQuery);