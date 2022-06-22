/**
 * jquery sub window option
 * 販売店/納入店確認(TG018/TG019)
 */
;(function($) {

$.extend({

	winTG018: {
		name: 'Out_ReportwinTG018',
		prefix:'_sub',
		suffix:'_winTG018',
		initializes: true,		// 初期化フラグ（全体）
		initializesCond: true,	// 初期化フラグ（条件用）
		lastIndex: -1,			// 編集位置（行）
		sortName: '',			// ソート項目名
		sortOrder: '',			// ソート順
		timeData : (new Date()).getTime(),
		callreportno:"",		// 呼出し元レポートNo
		callBtnid: "",			// 呼出ボタンID情報
		callreportHidden:[],	// 呼出し元レポートからの引き継ぎ情報
		focusRootId:"_winTG018",// （キー移動時必須）キー移動イベントのルートとなるパネルのID
		focusParentId:"",		// （キー移動時必須）現在フォーカスがあたっている項目の親となるパネルのID
		focusGridId:"",			// （キー移動時必須）現在フォーカスがあたっているDataGridのID
		init: function(js) {
			var that = this;
			if(!that.initializes) return false;

			// 呼出し元情報取得
			that.callreportno = js.name;
			that.callreportHidden = js.jsonHidden;

			// 呼出しボタンイベント設定
			$('[id^=btn'+that.prefix+that.suffix+']').each(function(){
				var id = $(this).attr('id');
				$('#'+id).click(function() { that.Open(this, js); });
			});
			// 戻る
			$('#'+$.id.btn_back+that.suffix).on("click", that.Back);

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
		Open: function(obj, js) {
			if ($(obj).is("disabled","disabled"))	return false;
			var that = $.winTG018;
			that.callBtnid = $(obj).attr('id');
			var kbn = that.callBtnid==='btn'+that.prefix+that.suffix+'_h' ? 'h':'n';

			if(kbn==='h'){
				$('#'+that.suffix).window({title: '月間販売計画(チラシ計画)　販売店確認(TG018)'});

			}else{
				$('#'+that.suffix).window({title: '月間販売計画(チラシ計画)　納入店確認(TG019)'});
				var chks =$('input[name="'+$.id.rad_sel+'"]:checked');
				if(chks.length===0){
					$.showMessage('E40081');
					return false;
				}
			}
			// 画面情報表示
			$('#'+that.focusRootId).find('[id]').filter('span').each(function(){
				var refid = $(this).attr('id').replace(that.suffix, '');
				if($('#'+refid)){
					$(this).text($.getInputboxText($('#'+refid)));
				}
			});
			if(kbn==='n'){
				// 納入日設定
				var col = 'N91'+chks.eq(0).attr("id").replace($.id.rad_sel, "_");
				$('#'+$.id_inp.txt_nndt+that.suffix).text($.getInputboxText($('[col='+col+']')));
			}

			$('#'+that.focusRootId).find('[for_kbn]').each(function(){
				var kbns = $(this).attr('for_kbn').split(",");
				if(kbns.indexOf(kbn) === -1){
					$(this).hide();
				}else{
					$(this).show();
				}
			});

			// dataGrid 初期化
			this.setDataGrid('#grd'+that.prefix+that.suffix);

			that.Search();

			// window 表示
			$('#'+that.suffix).window('open');
		},
		Clear:function(){
			var that = $.winTG018;
			that.initializesCond = true;
			// グリッド初期化
			$('#grd'+that.prefix+that.suffix).datagrid('clearSelections').datagrid({data: []}).datagrid('getPager').pagination('select', 1);	// ページ初期化
			that.initializesCond = false;
		},
		Search: function(){
			var that = $.winTG018;

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
		Back:function(){
			var that = $.winTG018;
			$('#'+that.suffix).window('close');
			return true;
		},
		Select: function(){
			var that = $.winTG018;

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
		validation: function (){	// （必須）批准
			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform+that.suffix).form('validate');
			return rt;
		},
		success: function(id){	// 検索処理
			var that = $.winTG018;
			// 処理時間計測用
			that.timeData = (new Date()).getTime();

			// グリッド初期化&ローディング
			$('#'+id).datagrid('loading');

			var nndt = "";
			if(that.callBtnid==='btn'+that.prefix+that.suffix+'_n'){
				nndt = $.getParserDt($('#'+$.id_inp.txt_nndt+that.suffix).text(),true);
			}

			// 情報設定
			var json = [{
				callpage:	$($.id.hidden_reportno).val(),										// 呼出元レポート名
				MOYSKBN:	$.getJSONValue(that.callreportHidden, $.id_inp.txt_moyskbn),		// 催し区分
				MOYSSTDT:	$.getJSONValue(that.callreportHidden, $.id_inp.txt_moysstdt),		// 催し区分
				MOYSRBAN:	$.getJSONValue(that.callreportHidden, $.id_inp.txt_moysrban),		// 催し連番
				BMNCD:		$.getJSONValue(that.callreportHidden, $.id_inp.txt_bmncd),			// 部門コード
				KANRINO:	$.getJSONValue(that.callreportHidden, $.id_inp.txt_kanrino),		// 管理No.
				KANRIENO:	$.getJSONValue(that.callreportHidden, $.id_inp.txt_kanrieno),		// 管理No.枝番
				NNDT:		nndt,				// 納入日
				BTNID:		that.callBtnid		// 呼出ボタン
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
			switch (that.callBtnid) {
			case 'btn'+that.prefix+that.suffix+'_h':
				columns = [[
							{field:'F1', title:'店番',		width: 80,halign:'center',align:'left',formatter:function(value,row,index){ return $.getFormatLPad(value, $.len.tencd);}},
							{field:'F2', title:'店舗名称',	width:400,halign:'center',align:'left'},
							{field:'F3', title:'ランク',	width: 60,halign:'center',align:'left'}
						]];
				break;
			case 'btn'+that.prefix+that.suffix+'_n':
				columns = [[
							{field:'F1', title:'店番',		width: 80,halign:'center',align:'left',formatter:function(value,row,index){ return $.getFormatLPad(value, $.len.tencd);}},
							{field:'F2', title:'店舗名称',	width:400,halign:'center',align:'left'}
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