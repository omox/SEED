/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_ReportBM014',			// （必須）レポートオプションの確認
		jsonTemp:	[],							// （必須）検索条件情報_入力チェック前
		jsonString:	[],							// （必須）検索条件情報
		jsonHidden: [],							// （必須）親画面からの引き継ぎ情報
		jsonInit: [],							// （必須）検索条件初期情報
		caption: function(){					// （必須）タイトル
			return $('#reportname').val();
		},
		sortName: '',	// ソート項目名
		sortOrder: '',	// ソート順
		timeData : (new Date()).getTime(),
		dedefaultObjNum:	17,	// 初期化オブジェクト数
		initObjNum:	-1,
		initedObject: [],
		maxMergeCell: 1,
		onChangeFlag : false,
		onChangeFlag2 : false,
		columnName:'',	// OnClickRowの列名
		queried : false,
		initializes : true,
		onChangeReport: false,
		reportYobiInfo: function(){			// （必須）レポートメニューに登録された予備情報＝帳票をどういった表示にしたいかの情報
			return $('#reportYobi1').val();
		},
		sendBtnid: "",						// （必須）呼出ボタンID情報
		focusRootId:"cc",					// （キー移動時必須）キー移動イベントのルートとなるパネルのID
		focusParentId:"",					// （キー移動時必須）現在フォーカスがあたっている項目の親となるパネルのID
		focusGridId:"",						// （キー移動時必須）現在フォーカスがあたっているDataGridのID
		gridData:[],						// 検索結果
		gridTitle:[],						// 検索結果
		initialize: function (reportno){	// （必須）初期化
			var that = this;
			// 引き継ぎ情報
			this.jsonHidden = $.getTargetValue();

			$.setMeisyoCombo(that, reportno, $.id.sel_bnnruikbn)

			// 画面の初回基本設定
			this.setInitObjectState();

			// 初期検索条件設定
			this.jsonInit = $.getInitValue();

			// 初期化するオブジェクト数設定
			this.initObjNum = this.dedefaultObjNum;

			/*
			var isUpdateReport = true;
			// 入力テキストボックス系
			var inputbox = Object.getOwnPropertyNames($.id_inp);
			for ( var sel in inputbox ) {
				if($('#'+$.id_inp[inputbox[sel]]).length > 0){
					$.setInputbox(that, reportno, $.id_inp[inputbox[sel]], isUpdateReport);
				}
			}
			*/

			// 検索を実行するためにフラグ変更
			that.onChangeReport = true;
			that.setLabelbox($.id.txt_seq);				// テキスト（SEQ) 必須
			that.setLabelbox($.id.txt_operator);		// テキスト（オペレータ)
			that.setLabelbox($.id.txt_upddt);			// テキスト（更新日)
			that.setLabelbox($.id.txt_updtm);			// テキスト（更新時刻)
			that.setLabelbox($.id_inp.txt_commentkn);	// テキスト（コメント)
			that.setLabelbox($.id_inp.txt_moyscd);		// テキスト（催しコード)
			that.setLabelbox($.id_inp.txt_moykn);		// テキスト（催し名称)
			that.setLabelbox($.id_inp.txt_moyperiod);	// テキスト（催し期間)
			that.setLabelbox($.id_inp.txt_bmnno);		// テキスト（B/M番号)
			that.setLabelbox($.id_inp.txt_bmnmkn);		// テキスト（B/M名称)
			that.setLabelbox($.id_inp.txt_hbstdt);		// テキスト（販売期間)
			that.setLabelbox($.id_inp.txt_gyono);		// テキスト（行番号)
			that.setLabelbox($.id_inp.txt_shncd);		// テキスト（商品コード)
			that.setLabelbox($.id_inp.txt_shnkn);		// テキスト（商品名)
			that.setLabelbox($.id_inp.txt_errfld);		// テキスト（エラー箇所)
			that.setLabelbox($.id_inp.txt_msgtxt1);		// テキスト（エラー理由)
			that.setLabelbox($.id_inp.txt_errvl);		// テキスト（エラー値)

			// Load処理回避
			//$.tryChangeURL(null);

			// 初期化終了
			this.initializes =! this.initializes;

			// チェックボックスの設定
			$.initCheckboxCss($("#"+that.focusRootId));
			// キーイベントの設定
			$.initKeyEvent(that);

			// ログ出力
			$.log(that.timeData, 'initialize:');
		},
		setInitObjectState: function(){	// 画面初期化時の項目制御
			var that = this;

			// 引き継ぎ情報セット
			var sendBtnid = $.getJSONValue(that.jsonHidden, "sendBtnid");
			if(!sendBtnid){
				sendBtnid = $('#sendBtnid').val();
			}
			$('#sendBtnid').val(sendBtnid);
			that.sendBtnid = sendBtnid;

			var reportYobi1 = $.getJSONValue(that.jsonHidden, "reportYobi1");
			if(!reportYobi1){
				reportYobi1 = $('#reportYobi1').val();
			}
			$('#reportYobi1').val(reportYobi1);


			$($.id.buttons).show();
			// 各種ボタン
			$('#'+$.id.btn_err_change).on("click", $.pushChangeReport);

			// 当帳票を「参照」で開いた場合
			if(that.reportYobiInfo()==='1'){
				$.initReportInfo("BM014", "B/M別送信情報　新規・変更　CSV取込　エラーリスト", "");
			}else{
				$.initReportInfo("BM014", "B/M別送信情報　新規・変更　CSV取込　エラーリスト", "");
			}

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
		validation: function (btnId){	// （必須）批准
			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform).form('validate');

			// 入力エラーなしの場合に検索条件を格納
			if (rt == true) that.jsonString = that.jsonTemp.slice(0);
			// 入力チェック用の配列をクリア
			that.jsonTemp = [];
			return rt;
		},
		success: function(reportno, sortable, btnId){	// （必須）正処理
			if (sortable) sortable=1; else sortable=0;
			var that = this;
			// 検索実行
			var szSeq		= $.getJSONObject(this.jsonString, $.id.txt_seq).value;			// テキストボックス：SEQ
			if(!btnId) btnId = $.id.btn_search;

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// initialDisplayだと別にメッセージがでるので削除
			// $($.id.gridholder).datagrid('loading');

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,		// レポート名
					SEQ:			szSeq,
					BTN:			btnId,
					t:				(new Date()).getTime(),
					sortable:		sortable,
					sortName:		that.sortName,
					sortOrder:		that.sortOrder,
					rows:			1	// 表示可能レコード数
				},
				function(json){
					// 検索処理エラー判定
					if($.searchError(json)) return false;

					// ログ出力
					$.log(that.timeData, 'query:');

					// 検索データ（想定）
					that.gridData = JSON.parse(json).rows;
					that.gridTitle = JSON.parse(json).titles;

					var opts = JSON.parse(json).opts

					// メインデータ表示
					that.setData(that.gridData, opts);

					that.queried = true;
					that.pushBtnId = btnId;

					$.removeMask();
					$.removeMaskMsg();

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

			// SEQ
			this.jsonTemp.push({
				id:		$.id.txt_seq,
				value:	$('#'+$.id.txt_seq).val(),
				text:	$('#'+$.id.txt_seq).val()
			});
		},
		setData: function(rows, opts){		// データ表示
			var that = this;
			if(rows.length > 0){
				$('#'+that.focusRootId).find('[col^=F]').each(function(){
					var col = $(this).attr('col');
					if(rows[0][col]){
						$.setInputboxValue($(this), rows[0][col]);
					}
				});
			}
		},
		setObjectState: function(){	// 軸の選択内容による制御

		},
		setLabelbox: function(id){	// 入力項目の初期値を設定する
			var that = this;

			// 初期化情報取得
			var json = $.getJSONObject(that.jsonHidden, id);
			var idx = -1;

			if($('#'+id).is(".easyui-textbox")){
				if (json && json.value.length > 0){ $('#'+id).textbox("setValue",json.value); }
			}else if($('#'+id).is(".easyui-numberbox")){
				var idx = -1;
				var formatter = $.fn.numberbox.defaults.formatter;



				if (id===$.id_inp.txt_bmnno) {
					var check = $('#'+id).attr("check") ? JSON.parse('{'+$('#'+id).attr("check")+'}'): JSON.parse('{}');	// コードのcheck要素を取得
					formatter = function(value){
						return $.getFormatLPad(value, check.maxlen);
					};
				}



				var parser = $.fn.numberbox.defaults.parser;
				var options = $('#'+id).numberbox('options');
				if(options.prompt){
					var format = options.prompt.replace(/_/g, '#');
					formatter = function(value){
						return $.getFormatPrompt(value, format);
					};
					parser= function(value){
						return $.getParserPrompt(value);
					};
				}
				$('#'+id).numberbox({
					formatter:formatter,
					parser:parser
				});
				if (json && json.value.length > 0){ $('#'+id).numberbox("setValue",json.value); }
			}else{
				if (json && json.value.length > 0){ $('#'+id).val(json.value); }

			}
			if (that.initedObject && $.inArray(id, that.initedObject) < 0){
				that.initedObject.push(id);
				$.initialDisplay(that);
			}
			idx = 1;
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
			var sendMode = "2";

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

			// 戻る実行時用に現在の画面情報を保持する
			var states = $.getBackBaseJSON(that);
			// 各種グリッド情報を設定
			var newrepinfos = $.getBackJSON(that, states);
			$.setJSONObject(sendJSON, 'repinfo', newrepinfos, '');

			// 実行ボタン別処理
			switch (btnId) {
			case $.id.btn_back:
				// 元画面情報
				var callpage = $.getJSONValue(that.jsonHidden, "callpage");
				// 転送先情報
				index = 1;
				if(callpage==='Out_ReportBM013'){
					index = 6;
				} else {
					index = 5;
				}
				childurl = href[index];
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

		}
	} });
})(jQuery);