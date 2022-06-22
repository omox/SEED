/**
 * jquery sub window option
 */
;(function($) {

$.extend({

	win006: {
		name: 'Out_Reportwin006',
		prefix:'_shncd',
		suffix:'_win006',
		initializes: true,		// 初期化フラグ（全体）
		initializesCond: true,	// 初期化フラグ（条件用）
		lastIndex: -1,			// 編集位置（行）
		sortName: '',			// ソート項目名
		sortOrder: '',			// ソート順
		timeData : (new Date()).getTime(),
		callreportno:"",		// 呼出し元レポートNo
		callBtnid: "",			// 呼出ボタンID情報
		focusRootId:"_win006",	// （キー移動時必須）キー移動イベントのルートとなるパネルのID
		focusParentId:"",		// （キー移動時必須）現在フォーカスがあたっている項目の親となるパネルのID
		focusGridId:"",			// （キー移動時必須）現在フォーカスがあたっているDataGridのID
		onChangeFlag : false,
		isMstRep:false,			// 本部マスタ系画面かいなか
		oldBmn:"",
		oldDai:"",
		init: function(js) {
			var that = this;
			if(!that.initializes) return false;

			that.callreportno = js.name;

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
			$('#'+$.id.btn_select+that.suffix+"_shncd").on("click", that.SelectShn);
			// 選択
			$('#'+$.id.btn_select+that.suffix).on("click", that.Select);

			var title = "商品コード履歴検索　商品選択（SH001）";
			if(that.callreportno==='Out_Reportx092'){
				that.isMstRep = true;
				title = "商品番号検索";
			}

			// ｳｲﾝﾄﾞｳ設定
			$('#'+that.suffix).window({
				title:title,
				iconCls:'icon-search',
				modal:true,
				collapsible:false,
				minimizable:false,
				maximizable:false,
				closed:true,
				cinline:false,
				zIndex:7000,
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

			that.initializes = !that.initializes;
		},
		Open: function(obj) {
			if ($(obj).is("disabled","disabled"))	return false;
			var that = $.win006;
			that.callBtnid = $(obj).attr('id');

			if(that.initializesCond){

				// dataGrid 初期化
				this.setDataGrid('#grd'+that.prefix+that.suffix);

				// 商品コード
				$.setInputbox(that, that.callreportno, $.id_inp.txt_shncd+that.suffix, false);
				// 商品名（漢字）
				$.setInputbox(that, that.callreportno, $.id_inp.txt_shnkn+that.suffix, false);
				// 仕入先コード
				$.setInputbox(that, that.callreportno, $.id_inp.txt_ssircd+that.suffix, false);
				// メーカーコード
				$.setInputbox(that, that.callreportno, $.id_inp.txt_makercd+that.suffix, false);
				// 定貫不定貫区分
				$.setMeisyoCombo(that, that.callreportno, $.id_mei.kbn121+that.suffix, false);
				// 定計区分
				$.setMeisyoCombo(that, that.callreportno, $.id_mei.kbn117+that.suffix, false);
				// 商品種類
				$.setMeisyoCombo(that, that.callreportno, $.id_mei.kbn105+that.suffix, false);
				// 大分類
				this.setDaiBun(that.callreportno, $.id.SelDaiBun+that.suffix);
				// 中分類
				this.setChuBun(that.callreportno, $.id.SelChuBun+that.suffix);

				if (that.isMstRep) {
					$('#el'+that.suffix).layout('panel', 'north').panel('resize', {height:145});
					$("#"+$.id_inp.txt_shncd+that.suffix).attr('tabindex', -1);
					$('#'+$.id.btn_select+that.suffix+"_shncd").attr('tabindex', -1);
					$(".inp_hide").hide();
				}

				$.win001.init(that);	// メーカー
				$.win002.init(that);	// メーカー

				// チェックボックスの設定
				// $.initCheckboxCss($("#"+that.focusRootId));
				// キーイベントの設定
				$.initKeyEvent(that);

				that.initializesCond = false;
			}
			// 部門
			var defbmncd=null;
			if (that.callreportno==="Out_ReportTG016"){	// 特売スポット帳票の場合 デフォルト設定
				var reportNumber = $.getReportNumber(that.callreportno);
				if($.report[reportNumber].judgeRepType.st){
					defbmncd = $.getJSONValue($.report[reportNumber].jsonHidden, $.id_inp.txt_bmncd);
				}
			}
			this.setBumon(that.callreportno, $.id.SelBumon+that.suffix, defbmncd);

			// window 表示
			$('#'+that.suffix).window('open');
		},
		Clear:function(){
			var that = $.win006;
			that.initializesCond = true;
			// グリッド初期化
			$('#grd'+that.prefix+that.suffix).datagrid('clearSelections').datagrid({data: []}).datagrid('getPager').pagination('select', 1);	// ページ初期化
			that.initializesCond = false;
		},
		Search: function(){
			var that = $.win006;

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
			var that = $.win006;
			$('#'+that.suffix).window('close');
			return true;
		},
		Select: function(){
			var that = $.win006;

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
		SelectShn: function(){
			var that = $.win006;

			var txt_shncd = $('#'+$.id_inp.txt_shncd+that.suffix).textbox('getValue');

			if (txt_shncd===null || txt_shncd==="" || txt_shncd===undefined) {
				$.showMessage('EX1033');
				return false;
			}

			// マスタ存在チェック
			var param = {};
			param["KEY"] =  "MST_CNT";
			param["value"] = txt_shncd;

			var chk_cnt = $.getInputboxData(that.name, $.id.action_check, $.id_inp.txt_shncd, [param]);
			if(chk_cnt==="" || chk_cnt==="0"){
				$.showMessage('E11046');
				return false;
			} else {

				$("body").find('[for_btn^='+that.callBtnid+'_]').each(function(){
					$.setInputboxValue($(this), txt_shncd);
				});

				$('#'+that.suffix).window('close');
			}
			return true;
		},
		validation: function (){	// （必須）批准
			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform+that.suffix).form('validate');

			if(!that.isMstRep){
				var szShncd		= $.getInputboxValue($('#'+$.id_inp.txt_shncd+that.suffix));	// 商品コード
				if(rt){
					if(szShncd.length > 0 && szShncd.length !== $.len.shncd*1){
						$.showMessage('EX1006', undefined, function(){$.addErrState(that, +$('#'+$.id_inp.txt_shncd), true)});
						rt = false;
					}
				}

				if(rt && szShncd.length === 0){
					var szSelBumon	= $.getInputboxValue($('#'+$.id.SelBumon+that.suffix));			// 部門
					var szDaiBun	= $.getInputboxValue($('#'+$.id.SelDaiBun+that.suffix));		// 大分類
					if((szSelBumon === $.id.valueSel_Head)||(szDaiBun === $.id.valueSel_Head)){
						$.showMessage('E20254', undefined, function(){$.addErrState(that, $('#'+$.id.SelBumon+that.suffix), true)});
						rt = false;
					}
				}
			}
			return rt;
		},
		success: function(id){	// 検索処理
			var that = $.win006;
			// 処理時間計測用
			that.timeData = (new Date()).getTime();

			// グリッド初期化&ローディング
			$('#'+id).datagrid('loading');


			// 情報設定
			var json = [{}];

			// 呼出し元別処理
			switch (that.callreportno) {
			case 'Out_ReportXXX':
				break;
			default:
				// オブジェクト作成
				json = [{
					callpage:	$($.id.hidden_reportno).val(),										// 呼出元レポート名
					SHNCD		:	$('#'+$.id_inp.txt_shncd+that.suffix).textbox('getValue'),		// 商品コード
					SHNKN		:	$('#'+$.id_inp.txt_shnkn+that.suffix).textbox('getValue'),		// 商品（漢字）
					SSIRCD		:	$('#'+$.id_inp.txt_ssircd+that.suffix).textbox('getValue'),		// 仕入先コード
					MAKER		:	$('#'+$.id_inp.txt_makercd+that.suffix).textbox('getValue'),	// メーカーコード
					KBN121		:	$('#'+$.id_mei.kbn121+that.suffix).combobox('getValue'),		// 定貫不定貫区分
					KBN117		:	$('#'+$.id_mei.kbn117+that.suffix).combobox('getValue'),		// 定計区分
					KBN105		:	$('#'+$.id_mei.kbn105+that.suffix).combobox('getValue'),		// 商品種類
					BUMON		:	$('#'+$.id.SelBumon+that.suffix).combobox('getValue'),			// 部門
					DAI_BUN		:	$('#'+$.id.SelDaiBun+that.suffix).combobox('getValue'),			// 大分類
					CHU_BUN		:	$('#'+$.id.SelChuBun+that.suffix).combobox('getValue'),			// 中分類
				}];
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
					var dg =$('#'+id);
					if(data!==""){
						var limit = 1001;
						var size = JSON.parse(data)["total"];
						if(size >= limit){
							$.showMessage('E00010');
						} else if(size == 0){
							$.showMessage('E11003');
						}
						// 仕入先コード
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
		getComboErr: function (obj,editable,newValue,oldValue) {
			var data = obj.combobox('getData');

			if (!obj.hasClass('datagrid-editable-input')) {
				if (!$.setComboReload(obj,true) && !editable) {
					$.showMessage("E11302",["入力値"],function () {$.addErrState(this,obj,false)});
					obj.combobox('reload');
					obj.combobox('hidePanel');
				} else if ($.isEmptyVal(newValue)) {
					obj.combobox('setValue',obj.combobox('getData')[0].VALUE);
				} else if ($.isEmptyVal(oldValue)) {
					if (obj.next().find('[tabindex=1]').length===1) {
						obj.combo("textbox").focus();
					}
				}
			}
		},
		getChange: function (id) {
			var that = this;
			var newVal = $.getInputboxValue($('#'+id))*1;
			var oldVal = id===$.id.SelBumon+that.suffix ? that.oldBmn:that.oldDai;

			if (!$.isEmptyVal(oldVal) && oldVal===newVal) {
				return false;
			} else {
				return true;
			}
		},
		setBumon: function(reportno, id, init){		// 部門
			var that = this;
			var idx = -1;
			var onChange=false;
			var onPanel=false;

			// フォーカスアウトのタイミングの動作
			$('#'+id).next().on('focusout', function(e){

				// 変更があったか
				if (!that.getChange(id)) {
					return false;
				} else {
					that.oldBmn=$.getInputboxValue($('#'+id))*1;
				}

				var obj = $(this).prev();

				if (!$.setComboReload(obj,false)) {
					obj.combobox('reload');
				}
			});

			$('#'+id).combobox({
				panelWidth:250,
				url:$.reg.easy,
				required: false,
				editable: true,
				autoRowHeight:false,
				valueField:'VALUE',
				textField:'TEXT',
				multiple :false,
				prompt: '',
				icons: [],
				keyHandler: {
					up: $.fn.combobox.defaults.keyHandler.up,
					down: $.fn.combobox.defaults.keyHandler.down,
					left: $.fn.combobox.defaults.keyHandler.left,
					right: $.fn.combobox.defaults.keyHandler.right,
					enter: function(e){
						$('#'+id).combobox('hidePanel');
						e.preventDefault();
					},
					query: $.fn.combobox.defaults.keyHandler.query
				},
				onBeforeLoad:function(param){
					idx = -1;
					// 情報設定
					var json = [{}];
					param.page		=	reportno;
					param.obj		=	id;
					param.sel		=	(new Date()).getTime();
					param.target	=	id;
					param.action	=	$.id.action_init;
					param.json		=	JSON.stringify(json);
					param.datatype	=	"combobox";
				},
				onLoadSuccess:function(data){
					// 選択値設定
					var val = null;

					if (init!==null) {
						for (var i=0; i<data.length; i++){
							if (data[i].VALUE == init){
								val = init;
								break;
							}
						}
					}

					if (val === null && data.length>0){
						val = data[0].VALUE;
					}
					if (val){
						$('#'+id).combobox('setValues',val);
					}
					idx = 1;
					// ログ出力
					$.log(that.timeData, id+' init:');
					that.onChangeFlag = false;
					// 大分類
					that.tryLoadMethods('#'+$.id.SelDaiBun+that.suffix);
				},
				onShowPanel: function(){
					$.setScrollComboBox(id);
					that.onChangeFlag=false;
					onChange=false;
					onPanel=true;
				},
				onHidePanel: function(){
					if (onChange){
						// 変更があったか
						if (that.getChange(id)) {
							// 大分類
							that.tryLoadMethods('#'+$.id.SelDaiBun+that.suffix);
						}
					}else{
						that.onChangeFlag=true;
					}
					onChange=false;
					onPanel=false;
				},
				onChange:function(newValue, oldValue, obj){
					// 変更があったか
					if (!that.getChange(id)) {
						return false;
					};

					if(obj===undefined){obj = $(this);}

					if(idx > 0 && that.onChangeFlag){
						// 大分類
						that.tryLoadMethods('#'+$.id.SelDaiBun+that.suffix);
					}
					if(idx > 0){
						$.removeErrState();
					}
					onChange=true;
					that.getComboErr(obj,false,newValue,oldValue);
				}
			});
		},
		setDaiBun: function(reportno, id){		// 大分類
			var that = this;
			var idx = -1;
			var onChange=false;
			var onPanel=false;

			// フォーカスアウトのタイミングの動作
			$('#'+id).next().on('focusout', function(e){
				// 変更があったか
				if (!that.getChange(id)) {
					return false;
				} else {
					that.oldDai=$.getInputboxValue($('#'+id))*1;
				}

				var obj = $(this).prev();

				if (!$.setComboReload(obj,false)) {
					obj.combobox('reload');
				}
			});

			$('#'+id).combobox({
				panelWidth:250,
				url:$.reg.easy,
				required: false,
				editable: true,
				autoRowHeight:false,
				valueField:'VALUE',
				textField:'TEXT',
				multiple :false,
				prompt:'',
				icons:[],
				keyHandler: {
					up: $.fn.combobox.defaults.keyHandler.up,
					down: $.fn.combobox.defaults.keyHandler.down,
					left: $.fn.combobox.defaults.keyHandler.left,
					right: $.fn.combobox.defaults.keyHandler.right,
					enter: function(e){
						$('#'+id).combobox('hidePanel');
						e.preventDefault();
					},
					query: $.fn.combobox.defaults.keyHandler.query
				},
				onBeforeLoad:function(param){
					idx = -1;
					// 初期化しない
					if (that.initializes) return false;
					// 情報設定
					var json = [{
						BUMON: $('#'+$.id.SelBumon+that.suffix).combobox('getValue')
					}];

					param.page		=	reportno;
					param.obj		=	id;
					param.sel		=	(new Date()).getTime();
					param.target	=	id;
					param.action	=	$.id.action_init;
					param.json		=	JSON.stringify(json);
					param.datatype	=	"combobox";
				},
				onLoadSuccess:function(data){
					// 初期化
					var val = null;
					if (that.initedObject && $.inArray(id, that.initedObject) < 0){
						that.initedObject.push(id);
						var init = $.getJSONValue(that.jsonHidden, id);
						for (var i=0; i<data.length; i++){
							if (data[i].VALUE == init){
								val = init;
								break;
							}
						}
					}
					if (val === null && data.length>0){
						val = data[0].VALUE;
					}
					if (val){
						$('#'+id).combobox('setValue', val);
					}
					idx = 1;
					// ログ出力
					$.log(that.timeData, id+' init:');
					that.onChangeFlag = false;
					$.ajaxSettings.async = true;
					// 中分類
					that.tryLoadMethods('#'+$.id.SelChuBun+that.suffix);
				},
				onShowPanel: function(){
					$.setScrollComboBox(id);
					that.onChangeFlag=false;
					onChange=false;
					onPanel=true;
				},
				onHidePanel: function(){
					if (onChange){
						// 変更があったか
						if (that.getChange(id)) {
							// 中分類
							that.tryLoadMethods('#'+$.id.SelChuBun+that.suffix);
						}
					}else{
						that.onChangeFlag=true;
					}
					onChange=false;
					onPanel=false;
				},
				onChange:function(newValue, oldValue, obj){
					// 変更があったか
					if (!that.getChange(id)) {
						return false;
					}
					if(obj===undefined){obj = $(this);}

					if(idx > 0 && that.onChangeFlag){
						// 上位変更時、下位更新は常に同期
						$.ajaxSettings.async = false;
						that.onChangeFlag = false;
						// 中分類
						that.tryLoadMethods('#'+$.id.SelChuBun+that.suffix);
					}
					if(idx > 0){
						$.removeErrState();
					}
					onChange=true;
					that.getComboErr(obj,false,newValue,oldValue);
				}
			});
		},
		setChuBun: function(reportno, id){		// 中分類
			var that = this;
			var idx = -1;

			// フォーカスアウトのタイミングの動作
			$('#'+id).next().on('focusout', function(e){
				var obj = $(this).prev();

				if (!$.setComboReload(obj,false)) {
					obj.combobox('reload');
				}
			});

			$('#'+id).combobox({
				panelWidth:250,
				url:$.reg.easy,
				required: false,
				editable: true,
				autoRowHeight:false,
				valueField:'VALUE',
				textField:'TEXT',
				multiple :false,
				prompt: '',
				icons:[],
				keyHandler: {
					up: $.fn.combobox.defaults.keyHandler.up,
					down: $.fn.combobox.defaults.keyHandler.down,
					left: $.fn.combobox.defaults.keyHandler.left,
					right: $.fn.combobox.defaults.keyHandler.right,
					enter: function(e){
						$('#'+id).combobox('hidePanel');
						e.preventDefault();
					},
					query: $.fn.combobox.defaults.keyHandler.query
				},
				onBeforeLoad:function(param){
					idx = -1;
					// 初期化しない
					if (that.initializes) return false;

					// 情報設定
					var json = [{
						BUMON: $('#'+$.id.SelBumon+that.suffix).combobox('getValue'),
						DAI_BUN: $('#'+$.id.SelDaiBun+that.suffix).combobox('getValue')
					}];

					param.page		=	reportno;
					param.obj		=	id;
					param.sel		=	(new Date()).getTime();
					param.target	=	id;
					param.action	=	$.id.action_init;
					param.json		=	JSON.stringify(json);
					param.datatype	=	"combobox";
				},
				onLoadSuccess:function(data){
					// 初期化
					var val = null;
					if (val === null && data.length>0){
						val = data[0].VALUE;
					}
					if (val){
						$('#'+id).combobox('setValue', val);
					}
					idx = 1;
					// ログ出力
					$.log(that.timeData, id+' init:');
					that.onChangeFlag = true;
					$.ajaxSettings.async = true;
				},
				onShowPanel:function(){
					$.setScrollComboBox(id);
				},
				onChange:function(newValue, oldValue, obj){
					if(idx > 0){
						$.removeErrState();
					}

					if(obj===undefined){obj = $(this);}
					that.getComboErr(obj,false,newValue,oldValue);
				}
			});
		},
		/**
		 * 中分類（中分類が利用不可の場合、すべて）変換
		 */
		convertBumonChuBun: function(value){
			var that = this;
			// 中分類（中分類が利用不可の場合、すべて）
			if ($('#'+$.id.SelChuBun+that.suffix).combobox('options').disabled){
				value = ['-1'];
			}
			return value;
		},
		setDataGrid: function(id) {
			var that = this;

			// 呼出し元別処理
			var columns = null;
			switch (that.callreportno) {
			case 'Out_Reportx092':
			case 'Out_ReportJU012':
			case 'Out_ReportJU013':
			case 'Out_ReportJU032':
			case 'Out_ReportTR001':
			case 'Out_ReportTG016':
				// オブジェクト作成
				columns = [[
							{field:'F1', title:'商品コード',		width: 100, halign:'center',align:'left'},
							{field:'F2', title:'商品名',			width: 400, halign:'center',align:'left'}
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