/**
 * jquery sub window option
 */
;(function($) {

$.extend({

	winST010: {
		name: 'Out_ReportwinST010',
		prefix:'_rinzirank',
		suffix:'_winST010',
		initializes: true,		// 初期化フラグ（全体）
		initializesCond: true,	// 初期化フラグ（条件用）
		lastIndex: -1,			// 編集位置（行）
		sortName: '',			// ソート項目名
		sortOrder: '',			// ソート順
		timeData : (new Date()).getTime(),
		callreportno:"",		// 呼出し元レポートNo
		callBtnid: "",			// 呼出ボタンID情報
		changeBtnid: "",
		focusRootId:"_winST010",	// （キー移動時必須）キー移動イベントのルートとなるパネルのID
		focusParentId:"",		// （キー移動時必須）現在フォーカスがあたっている項目の親となるパネルのID
		focusGridId:"",			// （キー移動時必須）現在フォーカスがあたっているDataGridのID
		init: function(js) {
			var that = this;
			if(!that.initializes) return false;

			that.callreportno = js.name;
			that.callreportHidden = js.jsonHidden;

			// dataGrid 初期化
			this.setDataGrid('#grd_subwindow'+that.prefix+that.suffix);

			// 検索条件初期化
			$.setInputbox(that, that.callreportno, $.id_inp.txt_bmncd+that.suffix, false);
			// 検索条件初期化
			$.setInputbox(that, that.callreportno, $.id_inp.txt_moyscd+that.suffix, false);
			// チェックボックス
			$.setCheckboxInit(that.jsonHidden, 'chk_rinji'+that.suffix, false);

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

			// 新規
			$('#'+$.id.btn_new+that.suffix).on("click", function(e){
				that.changeBtnid = 'new';
				that.Change()
			});

			// 選択(変更)
			$('#'+$.id.btn_sel_change+that.suffix).on("click", function(e){
				that.changeBtnid = 'upd';
				that.Change()
			});

			// 選択(参照)
			$('#'+$.id.btn_sel_refer+that.suffix).on("click", function(e){
				that.changeBtnid = 'ref';
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

			// チェックボックスの設定
			$.initCheckboxCss($("#"+that.focusRootId));
			// キーイベントの設定
			$.initKeyEvent(that);

			that.initializes = !that.initializes;
		},
		Open: function(obj) {
			if ($(obj).is("disabled","disabled"))	return false;
			var that = $.winST010;
			that.callBtnid = $(obj).attr('id');
			var txt_moyscd = "";
			var txt_bmncd = "";
			//var row = $('#'+beforWinNumber).datagrid('getSelected');
			var shncd = $('#'+$.id_inp.txt_shncd).textbox('getValue');
			var txt_moyscd = $.getInputboxText($('#'+$.id_inp.txt_moyscd));
			txt_bmncd = shncd.substring(0, $.len.bmncd);
			var re1 = new RegExp("^[0-9]{1," + $.len.bmncd + "}$");
			var re2 = new RegExp("^[0-9]{1}-[0-9]{6}-[0-9]{1,3}$");
			if(!txt_bmncd.match(re1)||!txt_moyscd.match(re2)){
				$.showMessage('EX1033');
				return false;
			}

			txt_bmncd = ('00' + txt_bmncd).slice(-2);
			$.setInputboxValue($('#'+$.id_inp.txt_moyscd+that.suffix), $('#'+$.id_inp.txt_moyscd).textbox('getValue'));
			$.setInputboxValue($('#'+$.id_inp.txt_bmncd+that.suffix), txt_bmncd);
			$.setInputboxValue($('#'+$.id.chk_rinji+that.suffix), '1');
			//this.areakbn = row['F8'];

			that.Search();

			// window 表示
			$('#'+that.suffix).window('open');
		},
		Clear:function(){
			var that = $.winST010;
			that.initializesCond = true;
			// グリッド初期化
			$('#grd_subwindow'+that.prefix+that.suffix).datagrid('clearSelections').datagrid({data: []}).datagrid('getPager').pagination('select', 1);	// ページ初期化
			that.initializesCond = false;
		},
		Search: function(){
			var that = $.winST010;

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
			var that = $.winST010;
			$('#'+that.suffix).window('close');
			return true;
		},
		Select: function(){
			var that = $.winST010;

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
			var that = $.winST010;

			if(that.changeBtnid === 'new'){

			}else{
				var row = $("#grd_subwindow"+that.prefix+that.suffix).datagrid("getSelected");
				if(!row){
					$.showMessage('E00008');
					return false;
				}
			}

			// 呼出し元別処理
			switch (that.callreportno) {
			case 'Out_ReportJU012':
			case 'Out_ReportJU032':
				// ST008へ遷移
				var rinji = $.getInputboxValue($('#'+$.id.chk_rinji+that.suffix));
				var bmncd = $.getInputboxValue($('#'+$.id_inp.txt_bmncd+that.suffix));

				if(that.changeBtnid === 'new'){
					// 新規ボタン押下時
					$.setJSONObject(that.callreportHidden, $.id_inp.txt_rankno,	'',	'');	// ランクNo
					$.setJSONObject(that.callreportHidden, $.id_inp.txt_rankkn,	'',	'');	// ランク名称
				}else {
					// 変更、参照時ボタン押下時
					$.setJSONObject(that.callreportHidden, $.id_inp.txt_rankno,	row.F1,	row.F1);		// ランクNo
					$.setJSONObject(that.callreportHidden, $.id_inp.txt_rankkn,	row.F2,	row.F2);		// ランク名称
					$.setJSONObject(that.callreportHidden, $.id_inp.txt_bmncd,	bmncd,	bmncd);			// 部門
					$.setJSONObject(that.callreportHidden, $.id_inp.txt_ten_number,	row.F3,	row.F3);	// 店舗数
				}
				$.setJSONObject(that.callreportHidden, $.id_inp.txt_bmncd,	bmncd,	bmncd);			// 部門
				$.setJSONObject(that.callreportHidden, $.id.chk_rinji,	rinji,	rinji);				// 臨時
				$.setJSONObject(that.callreportHidden, 'changeBtnid',	that.changeBtnid,	that.changeBtnid);

				if(that.changeBtnid === 'ref'){
					$('#btn_runkTenTnfo').click();
				} else {
					$('#btn_teninfo').click();
				}
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
			var that = $.winST010;

			// 処理時間計測用
			that.timeData = (new Date()).getTime();

			// グリッド初期化&ローディング
			$('#'+id).datagrid('loading');

			// 情報設定
			var json = [{}];

			// 呼出し元別処理
			switch (that.callreportno) {
			case 'Out_ReportJU012':
			case 'Out_ReportJU032':
				// オブジェクト作成
				var abc = $('#'+$.id_inp.txt_shncd).textbox('getValue');
				json = [{
					callpage:	$($.id.hidden_reportno).val(),										// 呼出元レポート名
					BTN:		'test',
					BMNCD:		abc.substring(0, 2),		//
					MOYSCD:		$('#'+$.id_inp.txt_moyscd).textbox('getValue'),		//
					RINJI:		'1',		//

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

						if(Number(json.total) === 0){
							$.showMessage('E11003');
						}

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
			case 'Out_ReportJU012':
			case 'Out_ReportJU032':
				// オブジェクト作成
				columns = [[
							{field:'F1',	title:'ランクNo.',				width: 100,halign:'center',align:'left'},
							{field:'F2',	title:'ランク名称',				width: 300,halign:'center',align:'left'},
							{field:'F3',	title:'店舗数',					width: 100,halign:'center',align:'right'}
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
				rownumbers:true,
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