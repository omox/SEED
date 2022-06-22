/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_ReportMI001',			// （必須）レポートオプションの確認
		jsonTemp:	[],							// （必須）検索条件情報_入力チェック前
		jsonString:	[],							// （必須）検索条件情報
		jsonHidden: [],							// （必須）親画面からの引き継ぎ情報
		jsonInit: [],							// （必須）検索条件初期情報
		caption: function(){					// （必須）タイトル
			return $('#reportname').val();
		},
		sortName: '',							// ソート項目名
		sortOrder: '',							// ソート順
		timeData : (new Date()).getTime(),
		dedefaultObjNum:	22,					// 初期化オブジェクト数
		initObjNum:	-1,
		initedObject: [],
		maxMergeCell: 1,
		onChangeFlag : false,
		onChangeFlag2 : false,
		columnName:'',	// OnClickRowの列名
		queried : false,
		initializes : true,
		onChangeReport: false,
		reportYobiInfo: function(){				// （必須）レポートメニューに登録された予備情報＝帳票をどういった表示にしたいかの情報
			return $('#reportYobi1').val();
		},
		focusRootId:"cc",						// （キー移動時必須）キー移動イベントのルートとなるパネルのID
		focusParentId:"",						// （キー移動時必須）現在フォーカスがあたっている項目の親となるパネルのID
		focusGridId:"",							// （キー移動時必須）現在フォーカスがあたっているDataGridのID
		initialize: function (reportno){		// （必須）初期化
			var that = this;

			// 引き継ぎ情報
			this.jsonHidden = $.getTargetValue();
			// 画面の初回基本設定
			this.setInitObjectState();

			// 初期検索条件設定
			this.jsonInit = $.getInitValue();
//			// データ表示エリア初期化
//			that.setGrid($.id.gridholder, reportno);

			// 初期化するオブジェクト数設定
			this.initObjNum = this.dedefaultObjNum;

			var isUpdateReport = false;

			// 初期検索可能
			that.onChangeReport = false;

//			var count = 1;
//			// 名称マスタ参照系
//			var meisyoSelect = Object.getOwnPropertyNames($.id_mei);
//			for ( var sel in meisyoSelect ) {
//				if($('#'+$.id_mei[meisyoSelect[sel]]).length > 0){
//					$.setMeisyoCombo(that, reportno, $.id_mei[meisyoSelect[sel]], isUpdateReport);
//					count++;
//				}
//			}
			// 入力テキストボックス系
			var inputbox = Object.getOwnPropertyNames($.id_inp);
			for ( var sel in inputbox ) {
				if($('#'+$.id_inp[inputbox[sel]]).length > 0){
					$.setInputbox(that, reportno, $.id_inp[inputbox[sel]], isUpdateReport);
//					count++;
				}
			}

			// 初期化終了
			this.initializes =! this.initializes;

			//$.initialSearch(that);

			// チェックボックスの設定
			$.initCheckboxCss($("#"+that.focusRootId));
			// キーイベントの設定
			$.initKeyEvent(that);

			// ログ出力
			$.log(that.timeData, 'initialize:');
		},
		setInitObjectState: function(){	// 画面初期化時の項目制御
			var that = this;

			$('#'+$.id.btn_back).on("click", $.pushChangeReport);
			$.initReportInfo("MI001", "商品マスタ検索");

			var kbn = "1";
			that.setDispOption(kbn);	// 表示用入力テキストボックス設定
		},
		initCondition: function (){	// 条件初期値セット
			var that = this;
			// 初期化項目
		},
		clear:function(){
//			// 隠し情報初期化
//			$($.id.hiddenChangedIdx).val("");						// 変更行Index
//			// グリッド初期化
//			this.success(this.name, false);
		},
		validation: function (){	// （必須）批准
			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform).form('validate');

			var txt_shncd		= $('#'+$.id_inp.txt_shncd).textbox('getValue')							// 商品コード
			var txt_srccd		= $('#'+$.id_inp.txt_srccd).textbox('getValue')							// JANコード

			if (txt_shncd && txt_srccd) {
				$.showMessage('E11090');
//				$.showMessage('E11090', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_shncd), true)});
				$.addErrState(that, $('#'+$.id_inp.txt_shncd), true);
				$.addErrState(that, $('#'+$.id_inp.txt_srccd), true);
				var kbn = "1";
				that.setDispOption(kbn);	// 表示用入力テキストボックス設定
				return false;
			}
			// 入力エラーなしの場合に検索条件を格納
			if (rt == true) that.jsonString = that.jsonTemp.slice(0);
			// 入力チェック用の配列をクリア
			that.jsonTemp = [];
			return rt;
		},
		success: function(reportno, sortable){	// （必須）正処理
			if (sortable) sortable=1; else sortable=0;
			var that = this;

			var txt_shncd		= $.getJSONObject(that.jsonString, $.id_inp.txt_shncd).value;			// 商品コード
			var txt_srccd		= $.getJSONObject(that.jsonString, $.id_inp.txt_srccd).value;			// JANコード

			// 処理時間計測用
			that.timeData = (new Date()).getTime();

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,		// レポート名
					SHNCD:			txt_shncd,		// 商品コード
					SRCCD:			txt_srccd,		// JANコード
					t:				(new Date()).getTime(),
					sortable:		sortable,
					sortName:		that.sortName,
					sortOrder:		that.sortOrder,
					rows:			1	// 表示可能レコード数
				},
				function(json){
					// 検索処理エラー判定
					if($.searchError(json)) return false;
					var jsonp = JSON.parse(json);
					var count = jsonp["total"];
					if(count === 0){
						$.showMessage('I30000');
						var kbn = "1";
						that.setDispOption(kbn);	// 表示用入力テキストボックス設定
					} else {
						var kbn = "2";
						that.setDispOption(kbn);	// 表示用入力テキストボックス設定
					}

					// ログ出力
					$.log(that.timeData, 'query:');

					// 検索データ（想定）
					that.gridData = JSON.parse(json).rows;
					that.gridTitle = JSON.parse(json).titles;

					var opts = JSON.parse(json).opts

					// メインデータ表示
					that.setData(that.gridData, opts);
					that.queried = true;

					// 状態保存
					$.saveState2(reportno, that.getJSONString());

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
			// 商品コード
			this.jsonTemp.push({
				id:		$.id_inp.txt_shncd,
				value:	$('#'+$.id_inp.txt_shncd).textbox('getValue'),
				text:	$('#'+$.id_inp.txt_shncd).textbox('getText')
			});
			// JANコード
			this.jsonTemp.push({
				id:		$.id_inp.txt_srccd,
				value:	$('#'+$.id_inp.txt_srccd).textbox('getValue'),
				text:	$('#'+$.id_inp.txt_srccd).textbox('getText')
			});
		},
		setData: function(rows, opts){		// データ表示
			var that = this;

			if(rows.length > 0){
				$('#'+that.focusRootId).find('[col^=F]').each(function(){
					var col = $(this).attr('col');
//					// JANコード1、JANコード2のみ空白を許可
//					if(col === 'F6' || col === 'F7'){
//						$.setInputboxValue($(this), rows[0][col]);
//					}else{
//						if(rows[0][col]){
//							$.setInputboxValue($(this), rows[0][col]);
//						}
//					}
					$.setInputboxValue($(this), rows[0][col]);
				});
			}
		},
		setData2: function(){		// データ表示
			var that = this;

			$('#'+that.focusRootId).find('[col^=F]').each(function(){
				var col = $(this).attr('col');
//				// JANコード1、JANコード2のみ空白を許可
//				if(col === 'F6' || col === 'F7'){
//					$.setInputboxValue($(this), rows[0][col]);
//				}else{
//					if(rows[0][col]){
//						$.setInputboxValue($(this), rows[0][col]);
//					}
//				}
				$.setInputboxValue($(this), null);
			});
		},
		getRecord: function(){		// （必須）レコード件数を戻す
			var data = $($.id.gridholder).datagrid('getData');
			if (data == null) {
				return 0;
			} else {
				return data.total;
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
			var sendMode = "";

			// タブ要素(a)取得
			var elems = $('#tabContent', window.parent.document).map(
				function(i,e) {
					return e;
				}).get();
			var href = elems[0].value.split(',');

			// JSON Object Clone ()
			var sendJSON = [];
			$.setJSONObject(sendJSON, 'sendBtnid', btnId, $('#'+btnId).attr("title"));					// 実行ボタン情報保持
			$.setJSONObject(sendJSON, 'reportYobi1', that.reportYobiInfo(), that.reportYobiInfo());		// 参照情報保持
			$.setJSONObject(sendJSON, 'callpage', that.name, that.name);								// 呼出し元レポート情報

			// 実行ボタン別処理
			switch (btnId) {
			case $.id.btn_back:
				// 転送先情報
				childurl = parent.$('#hdn_menu_path').val();
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
		setDispOption: function(kbn){
			var that = this;
			that.setDispParam(kbn, $("#"+$.id_inp.txt_dummycd), 3);
			that.setDispParam(kbn, $("#"+$.id_inp.txt_shnkn), 4);
			that.setDispParam(kbn, $("#"+$.id_inp.txt_kikkn), 5);
			that.setDispParam(kbn, $("#"+$.id_inp.txt_shnan), 6);
			that.setDispParam(kbn, $("#"+$.id_inp.txt_kikan), 7);
			that.setDispParam(kbn, $("#"+$.id_inp.txt_jancd1), 8);
			that.setDispParam(kbn, $("#"+$.id_inp.txt_jancd2), 9);
			that.setDispParam(kbn, $("#"+$.id_inp.txt_daicd), 10);
			that.setDispParam(kbn, $("#"+$.id_inp.txt_chucd), 11);
			that.setDispParam(kbn, $("#"+$.id_inp.txt_shocd), 12);
			that.setDispParam(kbn, $("#"+$.id_inp.txt_makercd), 13);
			that.setDispParam(kbn, $("#"+$.id_inp.txt_ssircd), 14);
			that.setDispParam(kbn, $("#"+$.id_inp.txt_rg_genkaam), 15);
			that.setDispParam(kbn, $("#"+$.id_inp.txt_rg_baikaam), 16);
			that.setDispParam(kbn, $("#"+$.id_inp.txt_rg_irisu), 17);
			that.setDispParam(kbn, $("#"+$.id_inp.txt_avgptankaam), 18);
			that.setDispParam(kbn, $("#"+$.id_inp.txt_hs_genkaam), 19);
			that.setDispParam(kbn, $("#"+$.id_inp.txt_hs_baikaam), 20);
			that.setDispParam(kbn, $("#"+$.id_inp.txt_hs_irisu), 21);
			that.setDispParam(kbn, $("#"+$.id_inp.txt_hs_avgptankaam), 22);
		},
		setDispParam: function(kbn, target, tabindex){
			var that = this;
			if (kbn === '1') {
				$.setInputBoxDisable(target);
				target.textbox('textbox').attr('tabindex', -1);
				target.textbox('textbox').attr('readonly', 'readonly');
				target.textbox('textbox').attr('disabled', 'disabled');
			} else if (kbn === '2') {
				$.setInputBoxEnable(target);
//				target.textbox('textbox').attr('readonly',false);
				target.textbox('textbox').attr('readonly', 'readonly');
				target.textbox('textbox').attr('tabIndex', tabindex);
				target.textbox('textbox').removeAttr('disabled');
			}
		},
		keyEventInputboxFunc:function(e, code, that, obj){

			that.initObjNum = that.dedefaultObjNum;

			var id = $(obj).attr("orizinid");

			// *** Enter or Tab ****
//			if(code === 13 || code === 9){
			if(code === 13){
				if(id===$.id_inp.txt_shncd){
					var value = $.getInputboxValue($('#'+id));
					var otherValue = $.getInputboxValue($('#'+$.id_inp.txt_srccd));
					if(!$.isEmptyVal(value)){
						if(value.length < 8 ){
							value = ('00000000'+value).substr(-8);
							$.setInputboxValue($('#'+id), value);
						}else if(value.length > 8 ){
							value = value.substr(0, 8);
							$.setInputboxValue($('#'+id), value);
						}
						// 表示内容をクリア
						that.setData2();
//						$.initialDisplay(that);
						// 検索ボタン押下
						$('#'+$.id.btn_search).trigger('click');
						e.preventDefault();
						return false;
					} else if (!$.isEmptyVal(otherValue)) {
						// 表示内容をクリア
						that.setData2();
						// 検索ボタン押下
						$('#'+$.id.btn_search).trigger('click');
						e.preventDefault();
						return false;

					} else {
						// 表示内容をクリア
						that.setData2();
						var kbn = "1";
						that.setDispOption(kbn);	// 表示用入力テキストボックス設定
					}
				} else if (id===$.id_inp.txt_srccd) {
					var value = $.getInputboxValue($('#'+id));
					var otherValue = $.getInputboxValue($('#'+$.id_inp.txt_shncd));
					if(!$.isEmptyVal(value)){
						// 表示内容をクリア
						that.setData2();
						// 検索ボタン押下
						$('#'+$.id.btn_search).trigger('click');
						e.preventDefault();
						return false;
					} else if (!$.isEmptyVal(otherValue)) {
						if(value.length < 8 ){
							value = ('00000000'+otherValue).substr(-8);
							$.setInputboxValue($('#'+$.id_inp.txt_shncd), otherValue);
						}else if(value.length > 8 ){
							value = value.substr(0, 8);
							$.setInputboxValue($('#'+$.id_inp.txt_shncd), otherValue);
						}
						// 表示内容をクリア
						that.setData2();
//						$.initialDisplay(that);
						// 検索ボタン押下
						$('#'+$.id.btn_search).trigger('click');
						e.preventDefault();
						return false;
					} else {
						// 表示内容をクリア
						that.setData2();
						var kbn = "1";
						that.setDispOption(kbn);	// 表示用入力テキストボックス設定
					}
				}
//				// 商品コード
//				if(id===$.id_inp.txt_shncd){
//					var value = $.getInputboxValue($('#'+id));
//					if(!$.isEmptyVal(value)){
//						if(value.length < 8 ){
//							value = ('00000000'+value).substr(-8);
//							$.setInputboxValue($('#'+id), value);
//						}else if(value.length > 8 ){
//							value = value.substr(0, 8);
//							$.setInputboxValue($('#'+id), value);
//						}
//						// 表示内容をクリア
//						that.setData2();
////						$.initialDisplay(that);
//						// 検索ボタン押下
//						$('#'+$.id.btn_search).trigger('click');
//						e.preventDefault();
//						return false;
//					}
//				} else if (id===$.id_inp.txt_srccd) {
//					var value = $.getInputboxValue($('#'+id));
//					if(!$.isEmptyVal(value)){
////						if(value.length < 14 ){
////							value = ('00000000000000'+value).substr(-14);
////							$.setInputboxValue($('#'+id), value);
////						}else if(value.length > 14 ){
////							value = value.substr(0, 14);
////							$.setInputboxValue($('#'+id), value);
////						}
////						$.initialDisplay(that);
//						// 表示内容をクリア
//						that.setData2();
//						// 検索ボタン押下
//						$('#'+$.id.btn_search).trigger('click');
//						e.preventDefault();
//						return false;
//					}
//				}
			}
		}
	} });
})(jQuery);