/**
 * jquery sub window option
 */
;(function($) {

$.extend({

	winIT032: {
		name: 'Out_ReportwinIT032',
		prefix:'_sub',
		suffix:'_winIT032',
		initializes: true,		// 初期化フラグ（全体）
		initializesCond: true,	// 初期化フラグ（条件用）
		lastIndex: -1,			// 編集位置（行）
		sortName: '',			// ソート項目名
		sortOrder: '',			// ソート順
		timeData : (new Date()).getTime(),
		queried : false,
		callreportno:"",		// 呼出し元レポートNo
		callBtnid: "",			// 呼出ボタンID情報
		callreportHidden:[],	// 呼出し元レポートからの引き継ぎ情報
		callTablekbn:"",		// 呼出し元レポートからの引き継ぎ情報(テーブル区分)
		focusRootId:"_winIT032",// （キー移動時必須）キー移動イベントのルートとなるパネルのID
		focusParentId:"",		// （キー移動時必須）現在フォーカスがあたっている項目の親となるパネルのID
		focusGridId:"",			// （キー移動時必須）現在フォーカスがあたっているDataGridのID
		editRowIndex:{},		// グリッド編集行保持
		updateFlg:false,		// 登録ボタン押下済
		baseData:[],			// 検索結果保持用
		judgeRepType: {
			yyk			: false,	// 予約
			ref 		: false		// 参照
		},
		init: function(js, canUpdate) {
			var that = this;
			if(!that.initializes) return false;

			// 呼出し元情報取得
			that.callreportno = js.name;
			that.callreportHidden = js.jsonHidden;
			switch (that.callreportno) {
			case 'Out_ReportXXXXX':
				break;
			default:
				that.callTablekbn = js.baseTablekbn;
				that.judgeRepType.yyk = js.judgeRepType.yyk||js.judgeRepType.err_yyk1||!canUpdate
				that.judgeRepType.ref = js.reportYobiInfo()==='1'||js.judgeRepType.ref;
				break;
			}

			var gridid = 'grd'+that.prefix+that.suffix;
			var gridid2= $.id.grd_tencdiinput+that.suffix;

			// ボタン設定
			$('[id^=btn'+that.prefix+that.suffix+']').each(function(){
				var id = $(this).attr('id');
				$('#'+id).click(function() { that.Open(this); });
			});
			$('#'+$.id.btn_back+that.suffix).on("click", that.Back);
			if(that.judgeRepType.ref){
				$.setInputBoxDisable($('#'+$.id.btn_cancel+that.suffix)).hide();
				$.setInputBoxDisable($('#'+$.id.btn_upd+that.suffix)).hide();
				$.setInputBoxDisable($('#'+$.id.btn_csv_import+that.suffix)).hide();
				$.setInputBoxDisable($('#'+$.id.btn_stop+that.suffix)).hide();
				$.setInputBoxDisable($('#'+$.id.btn_start+that.suffix)).hide();
				$.setInputBoxDisable($('#'+$.id.btn_set+that.suffix)).hide();
				$.setInputBoxDisable($($.id.hiddenChangedIdx+that.suffix));
			}else if(that.judgeRepType.yyk){
				$.setInputBoxDisable($('#'+$.id.btn_cancel+that.suffix));
				$.setInputBoxDisable($('#'+$.id.btn_upd+that.suffix));
				$.setInputBoxDisable($('#'+$.id.btn_csv_import+that.suffix));
				$.setInputBoxDisable($('#'+$.id.btn_stop+that.suffix));
				$.setInputBoxDisable($('#'+$.id.btn_start+that.suffix));
				$.setInputBoxDisable($('#'+$.id.btn_set+that.suffix));
				$.setInputBoxDisable($($.id.hiddenChangedIdx+that.suffix));
			}else{
				$('#'+$.id.btn_cancel+that.suffix).on("click", that.Back);	// キャンセル
				$('#'+$.id.btn_upd+that.suffix).on("click", that.Update);	// 更新
				// ファイル選択ボタン
				$('#'+$.id.btn_csv_import+that.suffix).on("click", function (e) {
					$("#"+$.id.txt_file+that.suffix).click();
					return false; // must!
				});
				// 全停止ボタン
				$('#'+$.id.btn_stop+that.suffix).on("click", function (e) {
					that.setData2(gridid, $.id.value_off);
					$($.id.hiddenChangedIdx+that.suffix).val("1");
				});
				// 全実施ボタン
				$('#'+$.id.btn_start+that.suffix).on("click", function (e) {
					that.setData2(gridid, $.id.value_on);
					$($.id.hiddenChangedIdx+that.suffix).val("1");
				});
				// 設定ボタン
				$('#'+$.id.btn_set+that.suffix).on("click", function (e) {
					var row = $('#'+$.id.grd_tencdiinput+that.suffix).datagrid("getSelected");
					var rowIndex = $('#'+$.id.grd_tencdiinput+that.suffix).datagrid("getRowIndex", row);
					$('#'+$.id.grd_tencdiinput+that.suffix).datagrid('endEdit',rowIndex);
					var rows = $('#'+gridid2).datagrid('getRows');
					var data = [];
					for (var i=0; i<rows.length; i++){
						if(rows[i]["TENCD"] && rows[i]["TENCD"].length > 0){
							data.push({"TENCD":rows[i]["TENCD"], "AHSKB":$.id.value_on});
						}
					}
					that.setData(gridid, data);
					$($.id.hiddenChangedIdx+that.suffix).val("1");
				});
				// ファイルテキスト
				$("#"+$.id.txt_file+that.suffix).on("change", function (e) {
					if($(this).val().length == 0){return false;}
					// パラメータをセット
					$($.id.uploadform+that.suffix).append('<input type="hidden" id="report" name="report" value="'+that.name+'">');
					$($.id.uploadform+that.suffix).append('<input type="hidden" name="'+$.id.txt_file+'" value="'+$(this).val()+'">');
					// アップロード実行
					var frm = $($.id.uploadform+that.suffix)[0];
					frm.action = $.reg.upload;
					frm.submit();
					return true;
				});
				// IFrame読み込み時
				$('#if'+that.suffix).on('load', function (e) {
					// ログ出力
					$.log(that.timeData, 'loaded:');

					$("#"+$.id.txt_file+that.suffix).val("");
					try {
						var contents = $(this).contents();
						var data = contents.find('body')[0].innerHTML;
						// 検索処理エラー判定
						if($.cmnError(data)) return false;
						var json = JSON.parse(data);
						that.setData(gridid, json.rows);
						$($.id.hiddenChangedIdx+that.suffix).val("1");
						$.cmnNormal(data, undefined, "I00001", [],gridid);
					}catch(exception){
						$.showMessage("E00014");
						console.log(exception.message);
					}
				});
			}
			// 初期化
			$.setInputbox(that, reportno, $.id_inp.txt_shncd+that.suffix, false);	// 商品コード
			$.setInputbox(that, reportno, $.id_inp.txt_shnan+that.suffix, true);	// 商品名
			$.setInputbox(that, reportno, $.id_inp.txt_shnkn+that.suffix, true);	// 商品名
			$.setInputbox(that, reportno, $.id_inp.txt_ahskb+that.suffix, true);	// フラグ
			$.setInputbox(that, reportno, $.id_inp.txt_tencd+that.suffix, true);	// 店コード

			// dataGrid 初期化
			this.setDataGrid(gridid);
			// 店番一括入力
			this.setDataGrid2(gridid2);

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
				title:'自動発注停止（店別）(IT032)',
				onBeforeOpen:function(){
					// ウインドウ展開中リサイズイベント無効化
					$.reg.resize = false;
					js.focusParentId = that.suffix;
				},
				onOpen:function(){
					$.setFocusFirst($('#'+that.focusRootId));
				},
				onBeforeClose:function(){
					// ウインドウ展開中リサイズイベント有効化
					$.reg.resize = true;
					if(that.updateFlg){
						$($.id.hiddenChangedIdx).val("1");	// 元の帳票に更新フラグ
						if($.isFunction(js.changeInputboxFunc)){
							js.changeInputboxFunc(that, gridid, that.baseData, $('#'+gridid));
						}
					}else{
						that.Clear();						// 変更内容初期化
					}
					js.focusParentId = js.focusRootId;
				},
				onClose:function(){
					$('#'+js.focusParentId).find('#'+that.callBtnid).focus();
				}
			});

			// 画面情報表示
			var txt_shncd = "";
			// 呼出し元別処理
			switch (that.callreportno) {
			case 'Out_ReportXXXXX':
				break;
			default:
				// 元画面より情報を取得し、初期化
				txt_shncd = $.getInputboxText($('#'+$.id.txt_sel_shncd));
				break;
			}
			$.setInputboxValue($('#'+$.id_inp.txt_shncd + that.suffix), txt_shncd);		// 商品コード

			// チェックボックスの設定
			$.initCheckboxCss($("#"+that.focusRootId));
			// キーイベントの設定
			$.initKeyEvent(that);

			// 初回検索
			that.Search();

			that.initializes = !that.initializes;
		},
		Open: function(obj) {
			if ($(obj).is("disabled","disabled"))	return false;
			var that = $.winIT032;
			that.callBtnid = $(obj).attr('id');

			// 画面情報表示
			var txt_shnkn = "",txt_shnan = "";
			// 呼出し元別処理
			switch (that.callreportno) {
			case 'Out_ReportXXXXX':
				break;
			default:
				// 元画面より情報を取得し、初期化
				txt_shnan = $.getInputboxValue($('#'+$.id_inp.txt_shnan));
				txt_shnkn = $.getInputboxValue($('#'+$.id_inp.txt_shnkn));
				break;
			}
			$.setInputboxValue($('#'+$.id_inp.txt_shnan + that.suffix), txt_shnan);
			$.setInputboxValue($('#'+$.id_inp.txt_shnkn + that.suffix), txt_shnkn);

			that.Search();

			// window 表示
			$('#'+that.suffix).window('open');
		},
		Clear:function(){
			var that = $.winIT032;
			that.initializesCond = true;
			$($.id.hiddenChangedIdx+that.suffix).val("");
			that.initializesCond = false;
		},
		Search: function(){
			var that = $.winIT032;
			// validate=falseの場合何もしない
			if(!that.validation()){ return false; }

			// セッションタイムアウト、利用時間外の確認
			var isTimeout = $.checkIsTimeout();
			if (! isTimeout) {
				// 検索実行
				that.success("grd"+that.prefix+that.suffix);
				that.success2($.id.grd_tencdiinput+that.suffix);
			}
			return true;
		},
		Update: function(){
			var that = $.winIT032;

			// validate=falseの場合何もしない
			if(!that.updValidation()){ return false; }

			// 変更情報チェック
			if(!$.getConfirmUnregistFlg($($.id.hiddenChangedIdx+that.suffix))){
				$.showMessage('E20582');
				return false;
			}

			var func_ok = function(r){
				// セッションタイムアウト、利用時間外の確認
				var isTimeout = $.checkIsTimeout();
				if (! isTimeout) {
					that.updateFlg = true;
					$($.id.hiddenChangedIdx+that.suffix).val("");
					that.baseData = jQuery.extend(true, {}, $('#'+'grd'+that.prefix+that.suffix).datagrid('getData'));
					$('#'+that.suffix).window('close');
				}
				return true;
			};
			$.showMessage("W00001", undefined, func_ok);
			return true;
		},
		Back:function(){
			var that = $.winIT032;
			if($.getConfirmUnregistFlg($($.id.hiddenChangedIdx+that.suffix))){
				var func_ok = function(r){
					$('#'+that.suffix).window('close');
					return true;
				};
				$.showMessage("E11025", undefined, func_ok);
			}else{
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
			var that = $.winIT032;
			// 処理時間計測用
			that.timeData = (new Date()).getTime();

			// グリッド初期化&ローディング
			$('#'+id).datagrid('loading');

			// 登録ボタン押下済み
			if(that.updateFlg){

				// ログ出力
				$.log(that.timeData, 'query:');
				var dg =$('#'+id);
				dg.datagrid('loadData', jQuery.extend(true, {}, that.baseData));
				dg.datagrid('loaded');

				that.queried = true;
				// 隠し情報初期化
				$($.id.hiddenChangedIdx+that.suffix).val("");						// 変更行Index

				// ログ出力
				$.log(that.timeData, 'loaded:');

			}else {
				// 情報設定
				var json =  [{
					callpage:	$($.id.hidden_reportno).val(),	// 呼出元レポート名
					TABLEKBN:	that.callTablekbn,				// テーブル区分
					SHNCD:		$.getInputboxText($('#'+$.id_inp.txt_shncd+that.suffix)).replace("-", ""),	// 商品コード
					SEQ:		$.getJSONValue(that.callreportHidden, $.id.txt_seq),						// CSVエラー.SEQ
					INPUTNO:	$.getJSONValue(that.callreportHidden, $.id_inp.txt_inputno)					// CSVエラー.入力番号
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
							that.baseData = jQuery.extend(true, {}, json.rows);
						}
						dg.datagrid('loaded');

						that.queried = true;
						// 隠し情報初期化
						$($.id.hiddenChangedIdx+that.suffix).val("");						// 変更行Index

						// ログ出力
						$.log(that.timeData, 'loaded:');
					}
				);
			}
		},
		success2: function(id){	// 検索処理_一括入力画面
			var that = $.winIT032;

			// 処理時間計測用
			that.timeData = (new Date()).getTime();

			// グリッド初期化&ローディング
			$('#'+id).datagrid('loading');

			// 情報設定
			var json =  [{
				callpage:	$($.id.hidden_reportno).val(),	// 呼出元レポート名
				TABLEKBN:	that.callTablekbn,				// テーブル区分
				SHNCD:		$.getInputboxText($('#'+$.id_inp.txt_shncd+that.suffix)).replace("-", ""),	// 商品コード
				SEQ:		$.getJSONValue(that.callreportHidden, $.id.txt_seq),						// CSVエラー.SEQ
				INPUTNO:	$.getJSONValue(that.callreportHidden, $.id_inp.txt_inputno)					// CSVエラー.入力番号
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
		updValidation: function (id){	// （必須）批准
			var that = this;
			var errMsg= "";
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.gridform+that.suffix).form('validate');

			var row = $('#'+$.id.grd_sub+that.suffix).datagrid("getSelected");
			var rowIndex = $('#'+$.id.grd_sub+that.suffix).datagrid("getRowIndex", row);
			$('#'+$.id.grd_sub+that.suffix).datagrid('endEdit',rowIndex);

			var gridid = 'grd'+that.prefix+that.suffix;

			// 自動発注区分
			var targetRowsAhskb = $('#'+gridid).datagrid('getRows');
			for (var i=0; i<targetRowsAhskb.length; i++){
				// 自動発注区分は0,1で登録しなければならない
				if(targetRowsAhskb[i]["AHSKB"]!==$.id.value_on && targetRowsAhskb[i]["AHSKB"]!==$.id.value_off){
					$.showMessage('E11012', ["自動発注区分"], function(){$.addErrState(that, $('#'+gridid), true, {NO:i, ID:$.id_inp.txt_ahskb})});
					return false;
				}
			}

			// 入力情報を変数に格納
			// if (rt == true) that.setGridData(that.getGridData());

			return rt;
		},
		setDataGrid: function(id) {
			var that = this;

			// 呼出し元別処理
			var columns = [[]];

			var tenformatter = function(value,row,index){ return $.getFormatLPad(value, $.len.tencd);};
			var bcstyler =function(value,row,index){return 'background-color:#URIAERACDURIAERACDURIAERACD;';};
			switch (that.callreportno) {
			case 'Out_ReportXXXXX':
				break;
			default:
				if(that.judgeRepType.ref){
					columns = [[
						{field:'TENCD',		title:'店番',			width: 70,halign:'center',align:'left'		,formatter:tenformatter},
						{field:'TENKN',		title:'店舗名',			width:200,halign:'center',align:'left'},
						{field:'AHSKB',		title:'フラグ',			width: 70,halign:'center',align:'center'},
						{field:'TENITEMSU',	title:'参考販売実績',	width: 90,halign:'center',align:'right'		,formatter:function(value,row,index){ return $.getFormat(value, '#,##0');}},
						{field:'URIAERACD',	title:'エリア',			width: 70,halign:'center',align:'right'},
					]];

				}else{
					columns = [[
						{field:'TENCD',		title:'店番',			width: 70,halign:'center',align:'left'		,styler:bcstyler,formatter:tenformatter},
						{field:'TENKN',		title:'店舗名',			width:200,halign:'center',align:'left'		,styler:bcstyler},
						{field:'AHSKB',		title:'フラグ',			width: 70,halign:'center',align:'center'	,editor:{type: 'numberbox'}},
						{field:'TENITEMSU',	title:'参考販売実績',	width: 90,halign:'center',align:'right'		,styler:bcstyler,formatter:function(value,row,index){ return $.getFormat(value, '#,##0');}},
						{field:'URIAERACD',	title:'エリア',			width: 70,halign:'center',align:'right'		,styler:bcstyler},
					]];
				}
				break;
			}

			var funcClickRow = $.fn.datagrid.defaults.onClickRow;
			var funcBeginEdit= $.fn.datagrid.defaults.onBeginEdit;
			var funcEndEdit= $.fn.datagrid.defaults.onEndEdit;
			var funcAfterEdit= $.fn.datagrid.defaults.onAfterEdit;
			if(!that.judgeRepType.ref){
				that.editRowIndex[id] = -1;
				funcClickRow = function(index,field){$.clickEditableDatagridCell(that,id, index)};
				funcBeginEdit = function(index,row){
					$.beginEditDatagridRow(that,id, index, row);
				};
				funcEndEdit = function(index,row,changes){$.endEditDatagridRow(that, id, index, row)};
				that.editRowIndex[id] = -1;
			}
			$('#'+id).datagrid({
				nowrap: true,
				border: true,
				striped: false,
				collapsible:false,
				remoteSort: true,
				rownumbers:true,
				showHeader:true,
				fit:true,
				frozenColumns:[[]],
				columns:columns,
				fitColumns:false,	// 指定カラム幅を適用する場合、false 指定。
				rowStyler:function(index, row){
				},
				onLoadSuccess:function(data){
					if (data !== null) {
						$.setInputboxValue($('#'+$.id.txt_ten_number+that.suffix), data.total);
					}
					// ログ出力
					$.log(that.timeData, 'query:');
				},
				onClickRow: funcClickRow,
				onBeginEdit:funcBeginEdit,
				onEndEdit: funcEndEdit,
				onAfterEdit: funcAfterEdit,
				autoRowHeight:false,
				pagination:false,
				pagePosition:'bottom',
				singleSelect:true
			});
		},
		setDataGrid2: function(id) {
			var that = this;

			// 呼出し元別処理
			var columns = [[]];

			var tenformatter = function(value,row,index){ return $.getFormatLPad(value, $.len.tencd);};
			switch (that.callreportno) {
			case 'Out_ReportXXXXX':
				break;
			default:
				if(that.judgeRepType.ref){
					columns = [[
						{field:'TENCD',title:'店番',			width: 70,halign:'center',align:'left'		,formatter:tenformatter},
					]];

				}else{
					columns = [[
						{field:'TENCD',title:'店番',			width: 70,halign:'center',align:'left'		,formatter:tenformatter,editor:{type: 'numberbox'}},
					]];
				}
				break;
			}

			var funcClickRow = $.fn.datagrid.defaults.onClickRow;
			var funcBeginEdit= $.fn.datagrid.defaults.onBeginEdit;
			var funcEndEdit= $.fn.datagrid.defaults.onEndEdit;
			if(!that.judgeRepType.ref){
				that.editRowIndex[id] = -1;
				funcClickRow = function(index,field){$.clickEditableDatagridCell(that,id, index)};
				funcBeginEdit = function(index,row){
					$.beginEditDatagridRow(that,id, index, row);
				};
				funcEndEdit = function(index,row,changes){$.endEditDatagridRow(that, id, index, row)};
				that.editRowIndex[id] = -1;
			}
			$('#'+id).datagrid({
				nowrap: true,
				border: true,
				striped: false,
				collapsible:false,
				remoteSort: true,
				rownumbers:true,
				showHeader:true,
				fit:true,
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
		},
		setData: function(id , data) {
			var that = this;
			var rows = $("#"+id).datagrid('getRows');
			for (var i=0; i<rows.length; i++){
				for (var j=data.length-1; j>=0; j--){
					if(rows[i]["TENCD"]===data[j]["TENCD"]){
						rows[i]["AHSKB"] = data[j]["AHSKB"];
						data.splice(j, 1);
						$("#"+id).datagrid('refreshRow', i);
					}
				}
			}
		},
		setData2: function(id , flg) {
			var that = this;
			var rows = $("#"+id).datagrid('getRows');
			for (var i=0; i<rows.length; i++){
				rows[i]["AHSKB"] = flg;
			}
			$("#"+id).datagrid('loadData', rows);
		}
	}
});

})(jQuery);