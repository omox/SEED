/**
 * jquery sub window option
 */
;(function($) {

$.extend({

	winST007: {
		name: 'Out_ReportwinST007',
		prefix:'_runkTenTnfo',
		prefix2:'grd_teninfo',
		suffix:'_winST007',
		initializes: true,		// 初期化フラグ（全体）
		initializesCond: true,	// 初期化フラグ（条件用）
		lastIndex: -1,			// 編集位置（行）
		sortName: '',			// ソート項目名
		sortOrder: '',			// ソート順
		timeData : (new Date()).getTime(),
		callreportno:"",		// 呼出し元レポートNo
		callBtnid: "",			// 呼出ボタンID情報
		sortBtnid: "",			// 並び替えボタンID情報
		sortAz: "",				// 並び替え降順・昇順
		focusRootId:"_winST007",	// （キー移動時必須）キー移動イベントのルートとなるパネルのID
		focusParentId:"",		// （キー移動時必須）現在フォーカスがあたっている項目の親となるパネルのID
		focusGridId:"",			// （キー移動時必須）現在フォーカスがあたっているDataGridのID
		init: function(js) {
			var that = this;
			if(!that.initializes) return false;

			that.callreportno = js.name;
			that.callreportHidden = js.jsonHidden;

			// dataGrid 初期化
			this.setDataGrid('#grd_subwindow'+that.prefix+that.suffix);

			//btn_jissekiorder
			$('#'+$.id.btn_jissekiorder).linkbutton('disable');
			$('#'+$.id.btn_jissekiorder).attr('disabled', 'disabled').hide();

			$.setInputBoxDisable($('#'+$.id.chk_rinji+that.suffix));

			// 検索条件初期化
			// TODO
			//$.setInputbox(that, that.callreportno, $.id_inp.txt_bmncd+that.suffix, false);
			// 検索条件初期化
			$.setInputbox(that, that.callreportno, $.id_inp.txt_moyscd+that.suffix, false);
			// チェックボックス
			$.setCheckboxInit(that.jsonHidden, 'chk_rinji'+that.suffix, false);

			// 呼出しボタンイベント設定
			$('[id^=btn'+that.prefix+']').each(function(){
				var id = $(this).attr('id');
				$('#'+id).click(function() { that.Open(this); });
			});
			// winST009から呼ばれる前提
			//$('#'+$.id.btn_sel_view+'_winST009').click(function() { that.Open(this); });

			// 検索
			$('#'+$.id.btn_search+that.suffix).on("click", that.Search);
			// キャンセル
			$('#'+$.id.btn_cancel+that.suffix).on("click", that.Cancel);
			// 選択
			$('#'+$.id.btn_select+that.suffix).on("click", that.Select);

			// ｳｲﾝﾄﾞｳ設定
			$('#'+that.suffix).window({
				iconCls:'icon-search',
				modal:true,
				collapsible:false,
				minimizable:false,
				maximizable:false,
				closed:true,
				cinline:false,
				zIndex:99000,
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

			// 並び替え：店昇順
			$('#'+$.id.btn_tennoorder+that.suffix).on("click", function(e){
				that.sortAz = $.getInputboxValue($('#'+$.id.btn_tennoorder+'_az'+that.suffix));
				that.sortBtnid = "TENNO";
				that.success("grd_subwindow"+that.prefix);

				if (that.sortAz==='0') {
					$.setInputboxValue($('#'+$.id.btn_tennoorder+'_az'+that.suffix),'1');
				} else {
					$.setInputboxValue($('#'+$.id.btn_tennoorder+'_az'+that.suffix),'0');
				}
			});

			// 並び替え：ランク順
			$('#'+$.id.btn_rankorder+that.suffix).on("click", function(e){
				that.sortAz = $.getInputboxValue($('#'+$.id.btn_rankorder+'_az'+that.suffix));
				that.sortBtnid = "RANKNO";
				that.success("grd_subwindow"+that.prefix);

				if (that.sortAz==='0') {
					$.setInputboxValue($('#'+$.id.btn_rankorder+'_az'+that.suffix),'1');
				} else {
					$.setInputboxValue($('#'+$.id.btn_rankorder+'_az'+that.suffix),'0');
				}
			});

			// チェックボックスの設定
			$.initCheckboxCss($("#"+that.focusRootId));
			// キーイベントの設定
			$.initKeyEvent(that);

			that.initializes = !that.initializes;
		},
		Open: function(obj, jsondata) {
			if ($(obj).is("disabled","disabled"))	return false;
			var that = $.winST007;
			that.callBtnid = $(obj).attr('id');

			// 呼出し元別処理
			switch (that.callreportno) {
			case 'Out_ReportST007':
				break;

			default:
				if(that.callreportno === 'Out_ReportBT002'){
					$('#'+that.suffix).window({title: 'ランク店情報（BT008）'});
				} else if (that.callreportno === 'Out_ReportBM006') {
					$('#'+that.suffix).window({title: 'B/M別送信情報　ランク店情報（CP008）'});
				}

				// 検索条件初期化
				var txt_bmncd = $.isEmptyVal($.getJSONValue(jsondata, $.id_inp.txt_bmncd))?$.getJSONValue(that.callreportHidden, $.id_inp.txt_bmncd):$.getJSONValue(jsondata, $.id_inp.txt_bmncd);
				txt_bmncd = ('00' + txt_bmncd).slice(-2);
				var rankno = $.isEmptyVal($.getJSONValue(jsondata, $.id_inp.txt_rankno))?$.getJSONValue(that.callreportHidden, $.id_inp.txt_rankno):$.getJSONValue(jsondata, $.id_inp.txt_rankno);
				var rankkn = $.isEmptyVal($.getJSONValue(jsondata, $.id_inp.txt_rankkn))?$.getJSONValue(that.callreportHidden, $.id_inp.txt_rankkn):$.getJSONValue(jsondata, $.id_inp.txt_rankkn);
				var tennum = $.isEmptyVal($.getJSONValue(jsondata, $.id_inp.txt_ten_number))?$.getJSONValue(that.callreportHidden, $.id_inp.txt_ten_number):$.getJSONValue(jsondata, $.id_inp.txt_ten_number);
				var rinji  = $.isEmptyVal($.getJSONValue(jsondata, $.id.chk_rinji))?$.getJSONValue(that.callreportHidden, $.id.chk_rinji):$.getJSONValue(jsondata, $.id.chk_rinji);
				var moyscd = $.isEmptyVal($.getJSONValue(jsondata, $.id_inp.txt_moyscd))?$('#'+$.id_inp.txt_moyscd).textbox('getText'):$.getJSONValue(jsondata, $.id_inp.txt_moyscd);

				$.setInputboxValue($('#'+$.id_inp.txt_bmncd+that.suffix), txt_bmncd);		// 部門コード
				$.setInputboxValue($('#'+$.id_inp.txt_rankno+that.suffix), rankno);			// ランク№
				$.setInputboxValue($('#'+$.id_inp.txt_rankkn+that.suffix), rankkn);			// ランク名称
				$.setInputboxValue($('#'+$.id_inp.txt_ten_number+that.suffix), tennum);		// 店舗数
				$.setInputboxValue($('#'+$.id.chk_rinji+that.suffix), rinji);				// 臨時

				if (rinji!=='0') {
					$.setInputboxValue($('#'+$.id_inp.txt_moyscd+that.suffix), moyscd);		// 催しコード
				} else {
					$.setInputboxValue($('#'+$.id_inp.txt_moyscd+that.suffix), '');			// 催しコード
				}

				// 初期表示は店の昇順(前回保持していた結果をクリア)
				that.sortBtnid = "";
				if ($.getInputboxValue($('#'+$.id.btn_tennoorder+'_az'+that.suffix))==='0') {
					$.setInputboxValue($('#'+$.id.btn_tennoorder+'_az'+that.suffix),'1');
				}
				if ($.getInputboxValue($('#'+$.id.btn_rankorder+'_az'+that.suffix))==='1') {
					$.setInputboxValue($('#'+$.id.btn_rankorder+'_az'+that.suffix),'0');
				}

				break;
			}

			that.Search();

			// window 表示
			$('#'+that.suffix).window('open');
		},
		Clear:function(){
			var that = $.winST007;
			that.initializesCond = true;
			// グリッド初期化
			$('#grd_subwindow'+that.prefix+that.suffix).datagrid('clearSelections').datagrid({data: []}).datagrid('getPager').pagination('select', 1);	// ページ初期化
			that.initializesCond = false;
		},
		Search: function(){
			var that = $.winST007;

			// validate=falseの場合何もしない
			if(!that.validation()){ return false; }

			// セッションタイムアウト、利用時間外の確認
			var isTimeout = $.checkIsTimeout();
			if (! isTimeout) {
				// 検索実行
				//that.success("grd_subwindow"+that.prefix+that.suffix);
				that.success("grd_subwindow"+that.prefix);
			}
			return true;
		},
		Cancel:function(){
			var that = $.winST007;
			$('#'+that.suffix).window('close');
			return true;
		},
		Select: function(){
			var that = $.winST007;

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
		validation: function (){	// （必須）批准
			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform+that.suffix).form('validate');
			return rt;
		},
		success: function(id){	// 検索処理
			var that = $.winST007;

			// 処理時間計測用
			that.timeData = (new Date()).getTime();

			// グリッド初期化&ローディング
			$('#'+id).datagrid('loading');

			// 情報設定
			var json = [{}];

			// 呼出し元別処理
			switch (that.callreportno) {
			case 'Out_ReportST007':
				break;
			default:
				// オブジェクト作成
				var chk_rinji	 = $.getInputboxValue($('#'+$.id.chk_rinji+that.suffix));
				var txt_bmncd  = $.isEmptyVal($.getInputboxValue($('#'+$.id_inp.txt_bmncd+that.suffix)))?$.getJSONValue(that.callreportHidden, $.id_inp.txt_bmncd):$.getInputboxValue($('#'+$.id_inp.txt_bmncd+that.suffix));
				var txt_rankno = $.isEmptyVal($.getInputboxValue($('#'+$.id_inp.txt_rankno+that.suffix)))?$.getJSONValue(that.callreportHidden, $.id_inp.txt_rankno):$.getInputboxValue($('#'+$.id_inp.txt_rankno+that.suffix));
				var sortBtnId  = that.sortBtnid + '-' + that.sortAz;
				var moyscd = '';
				for (var i = 0; i < $.getInputboxValue($('#'+$.id_inp.txt_moyscd)).split('-').length; i++) {
					moyscd+=$.getInputboxValue($('#'+$.id_inp.txt_moyscd)).split('-')[i];
				}

				json = [{
					callpage:	$($.id.hidden_reportno).val(),						// 呼出元レポート名
					RINJI		:chk_rinji,		//
					BMNCD		:txt_bmncd,
					RANKNO		:txt_rankno,
					MOYSCD		:moyscd,		// 催しコード
					SORTBTN		:sortBtnId
				}];
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
		setDataGrid: function(id) {
			var that = this;

			// 呼出し元別処理
			var columns = null;
			switch (that.callreportno) {
			case 'Out_ReportST007':
				// オブジェクト作成
				columns = [[]];
				break;
			default:
				// オブジェクト作成
				columns = [[
							{field:'F1',		title:'店番',			width: 70 	,halign:'center',align:'left'},
							{field:'F2',		title:'店舗名',			width: 280 	,halign:'center',align:'left'},
							{field:'F3',		title:'ランク',			width: 70 	,halign:'center',align:'left'},
							{field:'F4',		title:'エリア',			width: 70 	,halign:'center',align:'left'},
						]];
				break;
			}

			// ページサイズ定義取得
			var pageList = $.fn.pagination.defaults.pageList;
			var pageSize = $.getJSONValue(this.jsonHidden, $.id.pageSize);
			if (pageSize==="") pageSize=$.fn.pagination.defaults.pageSize;
			//pageSize = $.getDefaultPageSize(pageSize, pageList);
			pageSize = 30;

			$(id).datagrid({
				nowrap: true,
				border: true,
				striped: true,
				collapsible:false,
				remoteSort: true,
				rownumbers:true,
				fit:true,
				pageSize:pageSize,
				pageList:pageList,
				frozenColumns:[[]],
				columns:columns,
				fitColumns:false,	// 指定カラム幅を適用する場合、false 指定。
				rowStyler:function(index, row){
				},
				onLoadSuccess:function(data){
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
		}
	}
});

})(jQuery);