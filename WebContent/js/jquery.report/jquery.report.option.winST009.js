/**
 * jquery sub window option
 */
;(function($) {

$.extend({

	winST009: {
		name: 'Out_ReportwinST009',
		prefix:'_rankno',
		suffix:'_winST009',
		initializes: true,		// 初期化フラグ（全体）
		initializesCond: true,	// 初期化フラグ（条件用）
		lastIndex: -1,			// 編集位置（行）
		sortName: '',			// ソート項目名
		sortOrder: '',			// ソート順
		timeData : (new Date()).getTime(),
		callreportno:"",		// 呼出し元レポートNo
		callBtnid: "",			// 呼出ボタンID情報
		callreportHidden:[],	// 呼出し元レポートからの引き継ぎ情報
		focusRootId:"_winST009",// （キー移動時必須）キー移動イベントのルートとなるパネルのID
		focusParentId:"",		// （キー移動時必須）現在フォーカスがあたっている項目の親となるパネルのID
		focusGridId:"",			// （キー移動時必須）現在フォーカスがあたっているDataGridのID
		init: function(js) {
			var that = this;
			if(!that.initializes) return false;

			that.callreportno = js.name;
			that.callreportHidden = js.jsonHidden;

			// dataGrid 初期化
			this.setDataGrid('#grd'+that.prefix+that.suffix);

			// 呼出しボタンイベント設定
			$('[id^=btn'+that.prefix+']').each(function(){
				var id = $(this).attr('id');
				$('#'+id).click(function() { that.Open(this); });
			});
			// 検索
			$('#'+$.id.btn_search+that.suffix).on("click", that.Search);
			// キャンセル
			$('#'+$.id.btn_cancel+that.suffix).on("click", that.Cancel);
			// 選択
			$('#'+$.id.btn_select+that.suffix).on("click", that.Select);
			// 選択(表示)
			$('#'+$.id.btn_sel_view+that.suffix).on("click", function(){that.SelectView(this)});

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

			// チェックボックスの設定
			$.initCheckboxCss($("#"+that.focusRootId));
			// キーイベントの設定
			$.initKeyEvent(that);

			that.initializes = !that.initializes;
		},
		Open: function(obj) {
			if ($(obj).is("disabled","disabled"))	return false;
			if ($(obj).linkbutton('options').disabled)	return false;

			var that = $.winST009;
			that.callBtnid = $(obj).attr('id');

			var txt_moyscd = "";
			var txt_bmncd = "";
			var message = "";
			var chk_rinji = $.id.value_off;

			// 呼出し元別処理
			switch (that.callreportno) {
			case 'Out_ReportTG016':
			case 'Out_ReportBT002':
			case 'Out_ReportBM006':
			case 'Out_ReportGM003':
				if(that.callreportno === 'Out_ReportBT002'){
					$('#'+that.suffix).window({title: 'ランクNo.選択（BT009）'});
				} else if (that.callreportno === 'Out_ReportBM006') {
					$('#'+that.suffix).window({title: 'B/M別送信情報　ランクNo.選択（CP009）'});
				}

				// 検索条件初期化
				txt_moyscd = $.getInputboxText($('#'+$.id_inp.txt_moyscd));
				txt_bmncd = $.getInputboxValue($('#'+$.id_inp.txt_bmncd));
				message = "E20125";
				break;
			case 'Out_ReportJU012':
			case 'Out_ReportJU032':
				txt_moyscd = $.getInputboxText($('#'+$.id_inp.txt_moyscd));
				var shncd = $.getInputboxValue($('#'+$.id_inp.txt_shncd));
				txt_bmncd = shncd.substring(0, $.len.bmncd);
				message = "EX1033";
				break;
			default:
				// 選択(表示)ボタンを非表示に設定
				$('#'+$.id.btn_sel_view+that.suffix).hide();
				break;
			}

			var re1 = new RegExp("^[0-9]{1," + $.len.bmncd + "}$");
			var re2 = new RegExp("^[0-9]{1}-[0-9]{6}-[0-9]{1,3}$");

			if(!txt_bmncd.match(re1)){
				$.showMessage(message);
				return false;
			}

			if(!txt_moyscd.match(re2)){
				$.showMessage('E30012',['催しコード']);
				return false;
			}

			txt_bmncd = ('00' + txt_bmncd).slice(-2);
			$.setInputboxValue($('#'+$.id_inp.txt_moyscd+that.suffix),txt_moyscd);			// 催しコード連結
			$.setInputboxValue($('#'+$.id_inp.txt_bmncd+that.suffix), txt_bmncd);			// 部門コード
			$.setInputboxValue($('#'+$.id.chk_rinji+that.suffix), chk_rinji);				// 臨時

			that.Search();
			// window 表示
			$('#'+that.suffix).window('open');
		},
		Clear:function(){
			var that = $.winST009;
			that.initializesCond = true;
			// グリッド初期化
			$('#grd'+that.prefix+that.suffix).datagrid('clearSelections').datagrid({data: []}).datagrid('getPager').pagination('select', 1);	// ページ初期化
			that.initializesCond = false;
		},
		Search: function(){
			var that = $.winST009;

			// validate=falseの場合何もしない
			if(!that.validation()){ return false; }

			// セッションタイムアウト、利用時間外の確認
			var isTimeout = $.checkIsTimeout();
			if (! isTimeout) {
				// 検索実行
				that.success("grd"+that.prefix+that.suffix);
			}

			return true;
		},
		Cancel:function(){
			var that = $.winST009;
			$('#'+that.suffix).window('close');
			return true;
		},
		Select: function(){
			var that = $.winST009;

			var row = $("#grd"+that.prefix+that.suffix).datagrid("getSelected");
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
		SelectView:function (obj){
			if ($(obj).is("disabled","disabled"))	return false;
			var that = $.winST009;

			var row = $("#grd"+that.prefix+that.suffix).datagrid("getSelected");
			if(!row){
				$.showMessage('E00008');
				return false;
			}

			var data = [];
			$.setJSONObject(data, $.id_inp.txt_moyscd,		$.getInputboxText($('#'+$.id_inp.txt_moyscd+that.suffix)),	'');	// 催しコード連結
			$.setJSONObject(data, $.id_inp.txt_bmncd,		$.getInputboxText($('#'+$.id_inp.txt_bmncd+that.suffix)),	'');	// 部門コード
			$.setJSONObject(data, $.id.chk_rinji,			$.getInputboxValue($('#'+$.id.chk_rinji+that.suffix)),	'');		// 臨時
			$.setJSONObject(data, $.id_inp.txt_rankno,		row.F1,	row.F1);		// ランクNo
			$.setJSONObject(data, $.id_inp.txt_rankkn,		row.F2,	row.F2);		// ランク名称
			$.setJSONObject(data, $.id_inp.txt_ten_number,	row.F3,	row.F3);		// 店舗数

			$.winST007.Open(obj, data);
		},
		validation: function (){	// （必須）批准
			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform+that.suffix).form('validate');
			return rt;
		},
		success: function(id){	// 検索処理
			var that = $.winST009;
			// 処理時間計測用
			that.timeData = (new Date()).getTime();

			// グリッド初期化&ローディング
			$('#'+id).datagrid('loading');

			var txt_moyscd = $.getInputboxText($('#'+$.id_inp.txt_moyscd+that.suffix));

			// オブジェクト作成
			var json = [{
				callpage:	$($.id.hidden_reportno).val(),										// 呼出元レポート名
				MOYSKBN:	txt_moyscd.split("-")[0],		// 催し区分
				MOYSSTDT:	txt_moyscd.split("-")[1],		// 催し開始日
				MOYSRBAN:	txt_moyscd.split("-")[2],		// 催し連番
				BMNCD:		$.getInputboxValue($('#'+$.id_inp.txt_bmncd+that.suffix)),			// 部門コード
				RINJI:		$.getInputboxValue($('#'+$.id.chk_rinji+that.suffix))				// 臨時
			}];

			$.post(
				$.reg.easy,
				{
					page	:	that.name,										// レポート名
					obj		:	id,
					sel		:	(new Date()).getTime(),
					target	:	id,
					action	:	$.id.action_init,
					json	:	JSON.stringify(json),
					datatype:	'datagrid'
				},
				function(data){
					// ログ出力
					$.log(that.timeData, 'query:');
					var dg =$('#'+id);
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
		setDataGrid: function(id) {
			var that = this;

			// 呼出し元別処理
			var columns = null;
			switch (that.callreportno) {
			case 'Out_ReportXXXXX':
				columns = [[]];
				break;
			default:
				// オブジェクト作成
				columns = [[
							{field:'F1', title:'ランクNo',		width: 70,halign:'center',align:'left'},
							{field:'F2', title:'ランク名称',	width:400,halign:'center',align:'left'},
							{field:'F3', title:'店舗数',		width: 69,halign:'center',align:'right'}
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