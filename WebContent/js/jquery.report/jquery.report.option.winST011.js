/**
 * jquery sub window option
 */
;(function($) {

$.extend({

	winST011: {
		name: 'Out_ReportwinST011',
		prefix:'_zitref',
		suffix:'_winST011',
		initializes: true,		// 初期化フラグ（全体）
		initializesCond: true,	// 初期化フラグ（条件用）
		lastIndex: -1,			// 編集位置（行）
		initedObject: [],
		sortName: '',			// ソート項目名
		sortOrder: '',			// ソート順
		timeData : (new Date()).getTime(),
		callreportno:"",		// 呼出し元レポートNo
		callBtnid: "",			// 呼出ボタンID情報
		changeBtnid: "",
		focusRootId:"_winST011",	// （キー移動時必須）キー移動イベントのルートとなるパネルのID
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
			// 選択(確定)
			$('#'+$.id.btn_sel_kakutei+that.suffix).on("click", that.Select);
			// 選択
			//$('#'+$.id.btn_select+that.suffix).on("click", that.Select);

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

			// 大分類
			this.setDaiBun(that,that.callreportno, $.id.SelDaiBun+that.suffix, true);

			// 入力不可：部門コード
			$.setInputBoxDisable($('#'+$.id_inp.bmncd+that.suffix));

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
			var that = $.winST011;
			that.callBtnid = $(obj).attr('id');

			var bmncd		 = $.getJSONValue(that.callreportHidden, $.id_inp.txt_bmncd)
			var rinji		 = $.getJSONValue(that.callreportHidden, $.id.chk_rinji)
			var rankno		 = $.getJSONValue(that.callreportHidden, $.id_inp.txt_rankno)		// ランクNo
			var rankkn		 = $.getJSONValue(that.callreportHidden, $.id_inp.txt_rankkn)		// ランク名称
			var moyscd		 = $.getJSONValue(that.callreportHidden, $.id_inp.txt_moyscd)		// 催しコード

			$.setInputboxValue($('#'+$.id_inp.txt_bmncd+that.suffix)	, bmncd);				// 部門コード
			$.setInputboxValue($('#'+$.id_inp.txt_rankno+that.suffix)	, rankno);				// ランク№
			$.setInputboxValue($('#'+$.id_inp.txt_rankkn+that.suffix)	, rankkn);				// ランク名称
			$.setInputboxValue($('#'+$.id_inp.txt_moyscd+that.suffix)	, moyscd);				// 催しコード
			$.setInputboxValue($('#'+$.id.chk_rinji+that.suffix)		, rinji);				// 臨時

			// 大分類
			that.tryLoadMethods('#'+$.id.SelDaiBun+that.suffix);

			// window 表示
			$('#'+that.suffix).window('open');
		},
		Clear:function(){
			var that = $.winST011;
			that.initializesCond = true;
			// グリッド初期化
			$('#grd_subwindow'+that.prefix+that.suffix).datagrid('clearSelections').datagrid({data: []}).datagrid('getPager').pagination('select', 1);	// ページ初期化
			that.initializesCond = false;
		},
		Search: function(){
			var that = $.winST011;

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
			var that = $.winST011;
			$('#'+that.suffix).window('close');
			return true;
		},
		Select: function(){
			var that = $.winST011;

			var row = $("#grd_subwindow"+that.prefix+that.suffix).datagrid("getSelected");
			if(!row){
				$.showMessage('E00008');
				return false;
			}
			$.setJSONObject(that.callreportHidden, $.id_inp.txt_tenten_arr,	row.F4,	row.F4);	// 点数配列

			// 取得した情報を、オブジェクトに設定する
			// 設定先の判定：オブジェクトに for_btn,for_inpタグなどを使用して呼出し元(呼出しボタン名)と列名が設定されている項目
			/*var isSet = $.setInputboxRowData('for_btn', that.callBtnid, row);
			if(isSet){
				$('#'+that.suffix).window('close');
				//that.success("grd_subwindow_teninfo"+that.prefix);
				$.winST008.success("grd_subwindow_teninfo");
			}*/
			var isSet = $.setInputboxRowData('for_btn', that.callBtnid, row);
			if(isSet){
				$('#'+that.suffix).window('close');
			}

			// 再検索を行う
			$.winST008.success("grd_subwindow_teninfo");
			$('#'+that.suffix).window('close');
			return true;
		},
		Change:function (){
			var that = $.winST011;

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
			case 'Out_ReportST007':
			case 'Out_ReportBT002':
			case 'Out_ReportJU012':
			case 'Out_ReportJU032':
				// ST008へ遷移
				var rinji = $.getInputboxValue($('#'+$.id.chk_rinji+that.suffix));
				var bmncd = $.getInputboxValue($('#'+$.id_inp.txt_bmncd+that.suffix));

				if(that.changeBtnid === 'new'){
					// 新規ボタン押下時
					$.setJSONObject(that.callreportHidden, $.id_inp.txt_bmncd,	bmncd,	bmncd);			// 部門
				}else {
					// 変更、参照時ボタン押下時
					$.setJSONObject(that.callreportHidden, $.id_inp.txt_rankno,	row.F1,	row.F1);		// ランクNo
					$.setJSONObject(that.callreportHidden, $.id_inp.txt_rankkn,	row.F2,	row.F2);		// ランク名称
					$.setJSONObject(that.callreportHidden, $.id_inp.txt_bmncd,	bmncd,	bmncd);			// 部門
					$.setJSONObject(that.callreportHidden, $.id_inp.txt_ten_number,	row.F3,	row.F3);	// 店舗数
				}
				$.setJSONObject(that.callreportHidden, $.id.chk_rinji,	rinji,	rinji);				// 臨時
				$.setJSONObject(that.callreportHidden, 'changeBtnid',	that.changeBtnid,	that.changeBtnid);

				$('#btn_teninfo').click();
				break;
			default:
				break;
			}
		},
		validation: function (){	// （必須）批准
			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform+that.suffix).form('validate');
			var txt_bmncd		= $('#'+$.id_inp.txt_bmncd+that.suffix).textbox('getValue');					// 部門
			var rad_jissekibun	= $("input[name="+'rad_jissekibun'+that.suffix+"]:checked").val(); 				// 実績分類
			var SelDaiBun		= $('#'+$.id.SelDaiBun+that.suffix).combobox('getValue')						// 大分類
			var rad_wwmmflg		= $("input[name="+'rad_wwmmflg'+that.suffix+"]:checked").val(); 				// 週月フラグ
			var txt_yyww		= $('#'+$.id_inp.txt_yyww+that.suffix).textbox('getValue');						// 年月(週No.)
			var txt_yymm		= $('#'+$.id_inp.txt_yymm+that.suffix).textbox('getValue');						// 年月(

			if (!txt_bmncd){
				$.showMessage('EX1025');
				rt = false;
			}
			if (rt) {
				if (rad_jissekibun === "1" && SelDaiBun) {
					$('#'+$.id.SelDaiBun+that.suffix).combobox('setValue','');
					$.showMessage('EX1052');
					rt = false;
				} else if (rad_jissekibun === "2" && SelDaiBun) {
					$('#'+$.id.SelDaiBun+that.suffix).combobox('setValue','');
					$.showMessage('EX1053');
					rt = false;
				} else if (rad_jissekibun === "3" && !SelDaiBun) {
					$.showMessage('EX1054', undefined, function(){$.addErrState(that, $('#'+$.id.SelDaiBun+that.suffix), true)});
					rt = false;
				}
			}
			if (rt) {
				if (rad_wwmmflg === "1" && !txt_yyww) {
					$.showMessage('EX1055', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_yyww+that.suffix), true)});
					rt = false;
				}
			}
			if (rt) {
				if (rad_wwmmflg === "2" && !txt_yymm) {
					$.showMessage('EX1056', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_yymm+that.suffix), true)});
					rt = false;
				}
			}
			// 入力エラーなしの場合に検索条件を格納
			//if (rt == true) that.jsonString = that.jsonTemp.slice(0);
			// 入力チェック用の配列をクリア
			that.jsonTemp = [];
			return rt;
		},
		success: function(id){	// 検索処理
			var that = $.winST011;

			// 処理時間計測用
			that.timeData = (new Date()).getTime();

			// グリッド初期化&ローディング
			$('#'+id).datagrid('loading');

			// 情報設定
			var json = [{}];

			// 呼出し元別処理
			switch (that.callreportno) {
			case 'Out_ReportST007':
			case 'Out_ReportBT002':
			case 'Out_ReportJU012':
			case 'Out_ReportJU032':
				// オブジェクト作成
				var txt_bmncd		= $('#'+$.id_inp.txt_bmncd+that.suffix).textbox('getValue');				// 部門
				var rad_jissekibun	= $("input[name="+'rad_jissekibun'+that.suffix+"]:checked").val(); 			// 実績分類
				var SelDaiBun		= $('#'+$.id.SelDaiBun+that.suffix).combobox('getValue')					// 大分類
				var rad_wwmmflg		= $("input[name="+'rad_wwmmflg'+that.suffix+"]:checked").val(); 			// 週月フラグ
				var txt_yyww		= $('#'+$.id_inp.txt_yyww+that.suffix).textbox('getValue');					// 年月(週No.)
				var txt_yymm		= $('#'+$.id_inp.txt_yymm+that.suffix).textbox('getValue');					// 年月(



				json = [{
					callpage:	$($.id.hidden_reportno).val(),										// 呼出元レポート名
					BMNCD:			txt_bmncd,		// 部門
					JISSEKIBUN:		rad_jissekibun,	// 実績分類
					DAIBUN:			SelDaiBun,		// 大分類選択
					WWMMFLG:		rad_wwmmflg,	// 週月フラグ
					YYWW:			txt_yyww,		// 年月(週No.)
					YYMM:			txt_yymm,		// 年月
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
			case 'Out_ReportBT002':
			case 'Out_ReportJU012':
			case 'Out_ReportJU032':
				// オブジェクト作成
				columns = [[
					          {title:'実績率パターンNo.', colspan:2,rowspan:1},
					          {title:'　', colspan:2,rowspan:1},
					          ],[
						{field:'F1',	title:'大分類',			width: 100,halign:'center',align:'left'},
						{field:'F2',	title:'中分類',			width: 100,halign:'center',align:'left'},
						{field:'F3',	title:'分類名称',		width: 300,halign:'center',align:'left'},
						{field:'F4',	title:'点数配列',		hidden:true},
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
		},
		getComboErr: function (obj,editable,newValue,oldValue) {
			var data = obj.combobox('getData');

			if (!obj.hasClass('datagrid-editable-input')) {
				if (!$.setComboReload(obj,true) && !editable) {
					$.showMessage("E11302",["入力値"],function () {$.addErrState(this,obj,false)});
					obj.combobox('reload');
					obj.combobox('hidePanel');
				} else if ($.isEmptyVal(newValue)) {
					if ($.getInputboxValue($('#'+$.id.rad_jissekibun))==='3') {
						obj.combobox('setValue',obj.combobox('getData')[0].VALUE);
					}
				} else if ($.isEmptyVal(oldValue)) {
					if (obj.next().find('[tabindex=1]').length===1) {
						obj.combo("textbox").focus();
					}
				}
			}
		},
		setDaiBun: function(that,reportno, id, isUpdateReport){		// 大分類
			var that = this;
			var idx = -1;
			var onChange=false;
			var onPanel=false;

			var changeFunc1 = null;

			// フォーカスアウトのタイミングの動作
			$('#'+id).next().on('focusout', function(e){

				var obj = $(this).prev();

				if (!$.setComboReload(obj,false)) {
					obj.combobox('reload');
				}

				if ($.getInputboxValue($('#'+$.id.rad_jissekibun+that.suffix))!=='3') {
					obj.combobox('setValue','');
					return false;
				}
			});

			// 当項目を変更した際に、値をセットする項目がある場合の処理（セット先の項目は、HTMLタグとして、for_inp=当項目のID_列名）
			var changeFunc2 = null;
			if($.isFunction(that.changeInputboxFunc)){
				changeFunc2 = function(newValue, obj){
					that.changeInputboxFunc(that, id, newValue, obj);
				};
			}else{
				if($('[for_inp^='+id+'_]').length > 0){
					changeFunc2 = function(newValue){
						var param = [{"value":newValue}];
						$.getsetInputboxRowData(reportno, 'for_inp', id, param, that);
					};
				}
			}


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
				icons:[{
				}],
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
					//if (that.initializes) return false;

					// 情報設定
					var json = [{
						REQUIRED: 'REQUIRED',
						BUMON: $('#'+$.id_inp.txt_bmncd+that.suffix).textbox('getValue')
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
					// 選択値設定
					var val = null;
					if ($.inArray(id, that.initedObject) < 0){
						that.initedObject.push(id);
						var json = $.getJSONObject(that.jsonHidden, id);
						if(json && json.value!=""){
							val = new Array();
							for (var i=0; i<data.length; i++){
								if ($.inArray(data[i].VALUE, json.value)!=-1){
									val.push(data[i].VALUE);
								}
							}
							if (val.length===data.length || val.length===0){
								val = null;
							}
						}
					}
					$('#'+id).combobox('setValue',val);
					idx = 1;
					// ログ出力
					$.log(that.timeData, id+' init:');
					that.onChangeFlag = false;
					$.ajaxSettings.async = true;
				},
				onShowPanel: function(){
					$.setScrollComboBox(id);
					that.onChangeFlag=false;
					onChange=false;
					onPanel=true;
				},
				onHidePanel: function(){
					if (!onChange){
						that.onChangeFlag=true;
					}
					onChange=false;
					onPanel=false;
				},
				onChange:function(newValue, oldValue, obj){
					if(obj===undefined){obj = $(this);}

					if(changeFunc1!==null){ changeFunc1();}
					if(changeFunc2!==null){ changeFunc2(newValue, obj);}
					if(idx > 0){
						$.removeErrState();
					}
					onChange=true;

					that.getComboErr(obj,false,newValue,oldValue);
				}
			});
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
	}
});

})(jQuery);