/**
 * jquery sub window option
 */
;(function($) {

$.extend({

	JU017: {
		name: 'Out_ReportJU017',
		prefix:'_suryo',
		suffix:'_JU017',
		initializes: true,		// 初期化フラグ（全体）
		initializesCond: true,	// 初期化フラグ（条件用）
		lastIndex: -1,			// 編集位置（行）
		sortName: '',			// ソート項目名
		sortOrder: '',			// ソート順
		timeData : (new Date()).getTime(),
		callreportno:"",		// 呼出し元レポートNo
		callBtnid: "",			// 呼出ボタンID情報
		focusRootId:"_JU017",	// （キー移動時必須）キー移動イベントのルートとなるパネルのID
		focusParentId:"",		// （キー移動時必須）現在フォーカスがあたっている項目の親となるパネルのID
		focusGridId:"",			// （キー移動時必須）現在フォーカスがあたっているDataGridのID
		init: function(js) {
			var that = this;
			if(!that.initializes) return false;

			that.callreportno = js.name;

			// dataGrid 初期化
			this.setDataGrid('#grd_subwindow'+that.prefix+that.suffix);

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
			var that = $.JU017;
			that.callBtnid = $(obj).attr('id');

			// 呼出し元別処理
			switch (that.callreportno) {
			case 'Out_ReportJU012':
				// オブジェクト作成
				$('#'+that.suffix).window({title: '事前打出し　数量パターン選択(JU017)'});
				break;
			case 'Out_ReportJU032':
				// オブジェクト作成
				$('#'+that.suffix).window({title: '店舗アンケート付き送付け　数量パターン選択(JU036)'});
				break;
			default:
				break;
			}

			var shncd = $.getInputboxValue($('#'+$.id_inp.txt_shncd));
			var txt_bmncd = shncd.substring(0, $.len.bmncd);
			var txt_rankno = $.getInputboxValue($('#'+$.id_inp.txt_tenrank+"_2"));

			var re1 = new RegExp("^[0-9]{1," + $.len.bmncd + "}$");
			var re2 = new RegExp("^[0-9]{1," + $.len.rankno + "}$");

			if(!txt_bmncd.match(re1)){
				$.showMessage('EX1033');
				return false;
			}

			if(!txt_rankno.match(re2)){
				$.showMessage('EX1086');
				return false;
			}

			$.setInputboxValue($('#'+$.id_inp.txt_bmncd+that.suffix), txt_bmncd);

			that.Search();

			// window 表示
			$('#'+that.suffix).window('open');
		},
		Clear:function(){
			var that = $.JU017;
			that.initializesCond = true;
			// グリッド初期化
			$('#grd_subwindow'+that.prefix+that.suffix).datagrid('clearSelections').datagrid({data: []}).datagrid('getPager').pagination('select', 1);	// ページ初期化
			that.initializesCond = false;
		},
		Search: function(){
			var that = $.JU017;

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
			var that = $.JU017;
			$('#'+that.suffix).window('close');
			return true;
		},
		Select: function(){
			var that = $.JU017;

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
			var that = $.JU017;

			// 処理時間計測用
			that.timeData = (new Date()).getTime();

			// グリッド初期化&ローディング
			$('#'+id).datagrid('loading');

			// 情報設定
			var json = [{}];

			var txt_moyscd	= $.getInputboxValue($('#'+$.id_inp.txt_moyscd));
			var txt_shncd	= $.getInputboxValue($('#'+$.id_inp.txt_shncd));

			// 呼出し元別処理
			switch (that.callreportno) {
			case 'Out_ReportJU012':
			case 'Out_ReportJU032':
				// オブジェクト作成

				json = [{
					callpage:	$($.id.hidden_reportno).val(),							// 呼出元レポート名
					BTN:		'test',
					BMNCD:		txt_shncd.substring(0, 2),								// 部門コード
					RANKNO:		$('#'+$.id_inp.txt_tenrank+"_2").textbox('getValue'),	// 商品コード
					MOYSCD:	 	txt_moyscd,												// 催しコード
				}];
				break;
			default:
				break;
			}

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
					var dg =$('#'+id+that.suffix);
					if(data!==""){
						// JSONに変換
						var json = JSON.parse(data);

						// 結果表示
						dg.datagrid('loadData', json.rows);
					} else {
						$.showMessage('E11003');
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
			case 'Out_ReportJU012':
			case 'Out_ReportJU032':

				var targetId = $.id_inp.txt_sryptnno;
				var check = $('#'+targetId).attr("check") ? JSON.parse('{'+$('#'+targetId).attr("check")+'}'): JSON.parse('{}');	// コードのcheck要素を取得
				var formatterLPad = function(value){
					return $.getFormatLPad(value, check.maxlen);
				};
				var iformatter = function(value,row,index){ if (value!=='店舗数　') {return $.getFormat(value, '#,###,##0');} else {return '店舗数　';}};
				var iformatter2 = function(value,row,index){ return $.getFormat(value, '##,##0');};

				// オブジェクト作成
				columns = [[
							{field:'F1',	title:'数パターンNo',			width: 90,halign:'center',align:'left',formatter:formatterLPad},
							{field:'F2',	title:'データ名称',			width: 150,halign:'center',align:'left'},
							{field:'F3',	title:'合計',				width: 75,halign:'center',align:'right',formatter:iformatter},
							{field:'F4',	title:'A',					width: 55,halign:'center',align:'right',formatter:iformatter2},
							{field:'F5',	title:'B',					width: 55,halign:'center',align:'right',formatter:iformatter2},
							{field:'F6',	title:'C',					width: 55,halign:'center',align:'right',formatter:iformatter2},
							{field:'F7',	title:'D',					width: 55,halign:'center',align:'right',formatter:iformatter2},
							{field:'F8',	title:'E',					width: 55,halign:'center',align:'right',formatter:iformatter2},
							{field:'F9',	title:'F',					width: 55,halign:'center',align:'right',formatter:iformatter2},
							{field:'F10',	title:'G',					width: 55,halign:'center',align:'right',formatter:iformatter2},
							{field:'F11',	title:'H',					width: 55,halign:'center',align:'right',formatter:iformatter2},
							{field:'F12',	title:'I',					width: 55,halign:'center',align:'right',formatter:iformatter2},
							{field:'F13',	title:'J',					width: 55,halign:'center',align:'right',formatter:iformatter2},
							{field:'F14',	title:'K',					width: 55,halign:'center',align:'right',formatter:iformatter2},
							{field:'F15',	title:'L',					width: 55,halign:'center',align:'right',formatter:iformatter2},
							{field:'F16',	title:'M',					width: 55,halign:'center',align:'right',formatter:iformatter2},
							{field:'F17',	title:'N',					width: 55,halign:'center',align:'right',formatter:iformatter2},
							{field:'F18',	title:'O',					width: 55,halign:'center',align:'right',formatter:iformatter2},
							{field:'F19',	title:'P',					width: 55,halign:'center',align:'right',formatter:iformatter2},
							{field:'F20',	title:'Q',					width: 55,halign:'center',align:'right',formatter:iformatter2},
							{field:'F21',	title:'R',					width: 55,halign:'center',align:'right',formatter:iformatter2},
							{field:'F22',	title:'S',					width: 55,halign:'center',align:'right',formatter:iformatter2},
							{field:'F23',	title:'T',					width: 55,halign:'center',align:'right',formatter:iformatter2},
							{field:'F24',	title:'U',					width: 55,halign:'center',align:'right',formatter:iformatter2},
							{field:'F25',	title:'V',					width: 55,halign:'center',align:'right',formatter:iformatter2},
							{field:'F26',	title:'W',					width: 55,halign:'center',align:'right',formatter:iformatter2},
							{field:'F27',	title:'X',					width: 55,halign:'center',align:'right',formatter:iformatter2},
							{field:'F28',	title:'Y',					width: 55,halign:'center',align:'right',formatter:iformatter2},
							{field:'F29',	title:'Z',					width: 55,halign:'center',align:'right',formatter:iformatter2},
						]];
				break;
			default:
				break;
			}

			$(id).datagrid({
				nowrap: true,
				border: true,
				striped: true,
				collapsible:false,
				remoteSort: true,
				rownumbers:false,
				fit:true,
				view:scrollview,
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