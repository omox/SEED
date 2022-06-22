/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_Reportx211',			// （必須）レポートオプションの確認
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
		dedefaultObjNum:	2,	// 初期化オブジェクト数
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
		pushBtnId: "",						// （必須）実行ボタンID情報(検索系で利用)
		focusRootId:"cc",					// （キー移動時必須）キー移動イベントのルートとなるパネルのID
		focusParentId:"",					// （キー移動時必須）現在フォーカスがあたっている項目の親となるパネルのID
		focusGridId:"",						// （キー移動時必須）現在フォーカスがあたっているDataGridのID
		initialize: function (reportno){	// （必須）初期化
			var that = this;

			// 引き継ぎ情報
			this.jsonHidden = $.getTargetValue();
			// 画面の初回基本設定
			this.setInitObjectState();
			// 初期検索条件設定
			this.jsonInit = $.getInitValue();

			// 初期化するオブジェクト数設定
			this.initObjNum = this.dedefaultObjNum;

			var isUpdateReport = false;

			// 入力テキストボックス系
			var inputbox = Object.getOwnPropertyNames($.id_inp);
			for ( var sel in inputbox ) {
				if($('#'+$.id_inp[inputbox[sel]]).length > 0){
					$.setInputbox(that, reportno, $.id_inp[inputbox[sel]], isUpdateReport);
				}
			}

			// ラジオボタン系
			$.setRadioInit2(that.jsonHidden, $.id.rad_maisuhohokb1, that);
			$.setRadioInit2(that.jsonHidden, $.id.rad_maisuhohokb2, that);
			$.setRadioInit2(that.jsonHidden, $.id.rad_pcardsz1, that);
			$.setRadioInit2(that.jsonHidden, $.id.rad_pcardsz2, that);

			// Load処理回避
			//$.tryChangeURL(null);

			// 初期化終了
			this.initializes =! this.initializes;

			// チェックボックスの設定
			$.initCheckboxCss($("#"+that.focusRootId));
			// キーイベントの設定
			this.initKeyEvent(that);

			// ログ出力
			$.log(that.timeData, 'initialize:');
		},
		// キーイベント初期設定
		initKeyEvent : function(that) {
			// キー移動イベントの設定
			that.focusParentId = that.focusRootId;
			$.setReadyKeyEvent(that);	// 初期化したオブジェクトに対し、キーイベントの準備を行う
			$('#'+that.focusRootId).find('[tabindex]').each(function(){ $.setKeyEvent(that, $(this)); });	// tabindexが設定された項目に対し、キーイベントの設定を行う

			// 前回フォーカスが合っていた項目を再選択する。
			var focusbtnId = $.getJSONValue(that.jsonHidden, "focusbtnId");
			if(focusbtnId && focusbtnId != ""){
				$('#' + focusbtnId).focus();
			}else{
				$.setFocusFirst($('#'+that.focusRootId));
			}
			// サブ画面設定処理
			// 入力項目にフォーカス時、値を全選択する設定を追加
			$.ctrlFocusSubWin(that);
		},
		setInitObjectState: function(){	// 画面初期化時の項目制御
			var that = this;
			$.reg.search = false;

			var sendBtnid = $.getJSONValue(that.jsonHidden, "sendBtnid");
			if(!sendBtnid){
				sendBtnid = $('#sendBtnid').val();
			}
			$('#sendBtnid').val(sendBtnid);
			that.sendBtnid = sendBtnid;

			// 各種遷移ボタン
			$('#'+$.id.btn_new).on("click", $.pushChangeReport);
			$('#'+$.id.btn_sakubaikakb1).on("click", $.pushChangeReport);
			$('#'+$.id.btn_sakubaikakb2).on("click", $.pushChangeReport);
			$.initReportInfo("PC001", "プライスカード　新規");

			// 前回入力値を設定
			if (!$.isEmptyVal(that.jsonHidden[0].value[0].value)) {
				var array = that.jsonHidden[0].value[0].value["SRCCOND"];
				$.setInputboxValue($('#'+$.id_inp.txt_coman), $.getJSONValue(array,$.id_inp.txt_coman));
				$.setInputboxValue($('#'+$.id_inp.txt_mst_yoyakudt), $.getJSONValue(array,$.id_inp.txt_mst_yoyakudt));
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
		validation: function (){	// （必須）批准
			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform).form('validate');
			// 入力エラーなしの場合に検索条件を格納
			if (rt == true) that.jsonString = that.jsonTemp.slice(0);
			// 入力チェック用の配列をクリア
			that.jsonTemp = [];
			return rt;
		},
		success: function(reportno, sortable){	// （必須）正処理
			if (sortable) sortable=1; else sortable=0;
			var that = this;

			// 検索実行
			var txt_hsgpkn	= $.getJSONObject(this.jsonString, $.id_inp.txt_hsgpkn).value;		// 配送グループ名称（漢字）

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			$($.id.gridholder).datagrid('loading');

			// grid.options 取得
			var options = $($.id.gridholder).datagrid('options');
			that.sortName	= options.sortName;
			that.sortOrder	= options.sortOrder;

			$.post(
				$.reg.jqgrid ,
				{
					report			:that.name,		// レポート名
					HSGPKN			:txt_hsgpkn,
					SENDBTNID		:that.sendBtnid,
					t				:(new Date()).getTime(),
					sortable		:sortable,
					sortName		:that.sortName,
					sortOrder		:that.sortOrder,
					rows			:0					// 表示可能レコード数
				},
				function(json){
					// 検索処理エラー判定
					if($.searchError(json)) return false;

					// ログ出力
					$.log(that.timeData, 'query:');

					// Load処理回避
					$.tryChangeURL(null);

					if (sortable===0){
						var options = $($.id.gridholder).datagrid('options');
						// 初期検索時に並び替え情報のリセット
						options.sortName = null;
						options.sortOrder = null;
					}

					that.queried = true;
					// Load処理回避
					$.tryChangeURL($.reg.jqeasy);

					// グリッド再描画（easyui 1.4.2 対応）
					$($.id.gridholder).datagrid('load', {} );
					$.removeMask();

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
			// コメント
			this.jsonTemp.push({
				id:		$.id_inp.txt_coman,
				value:	$('#'+$.id_inp.txt_coman).textbox('getValue'),
				text:	$('#'+$.id_inp.txt_coman).textbox('getText')
			});

			// 枚数指定方法１
			this.jsonTemp.push({
				id:		$.id.rad_maisuhohokb1,
				value:	$("input[name="+$.id.rad_maisuhohokb1+"]:checked").val(),
				text:	$("input[name="+$.id.rad_maisuhohokb1+"]:checked").val()
			});

			// 枚数指定方法２
			this.jsonTemp.push({
				id:		$.id.rad_maisuhohokb2,
				value:	$("input[name="+$.id.rad_maisuhohokb2+"]:checked").val(),
				text:	$("input[name="+$.id.rad_maisuhohokb2+"]:checked").val()
			});

			// プライスカードサイズ１
			this.jsonTemp.push({
				id:		$.id.rad_pcardsz1,
				value:	$("input[name="+$.id.rad_pcardsz1+"]:checked").val(),
				text:	$("input[name="+$.id.rad_pcardsz1+"]:checked").val()
			});

			// プライスカードサイズ２
			this.jsonTemp.push({
				id:		$.id.rad_pcardsz2,
				value:	$("input[name="+$.id.rad_pcardsz2+"]:checked").val(),
				text:	$("input[name="+$.id.rad_pcardsz2+"]:checked").val()
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
		checkInputboxFunc: function(id, newValue){

			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform).form('validate');
			if(!rt){
				$.addErrState(that, $('.validatebox-invalid').eq(0), false);
				return rt;
			}

			return null;
		},
		changeReport:function(reportno, btnId){				// 画面遷移
			var that = this;

			// 遷移判定
			var index = 0;
			var childurl = "";

			// タブ要素(a)取得
			var elems = $('#tabContent', window.parent.document).map(
				function(i,e) {
					return e;
				}).get();
			var href = elems[0].value.split(',');

			// 遷移前にvalidation、入力値の保持を実行する。
			if(btnId == $.id.btn_sakubaikakb1 || btnId == $.id.btn_sakubaikakb2){
				// 入力情報を保持する
				that.getEasyUI();

				// EasyUI のフォームメソッド 'validate' 実施
				var rt = $($.id.toolbarform).form('validate');
				// 入力エラーなしの場合に検索条件を格納
				if (rt == false){
					return false
				}else{
					that.jsonString = that.jsonTemp.slice(0);

					// 入力チェック用の配列をクリア
					that.jsonTemp = [];
				}
			}

			// JSON Object Clone ()
			var sendJSON = [];
			$.setJSONObject(sendJSON, 'sendBtnid', btnId, $('#'+btnId).attr("title"));				// 実行ボタン情報保持
			$.setJSONObject(sendJSON, 'reportYobi1', that.reportYobiInfo(), that.reportYobiInfo());	// 参照情報保持
			$.setJSONObject(sendJSON, 'callpage', that.name, that.name);							// 呼出し元レポート情報

				that.jsonString.push({
					id:		"focusbtnId",
					value:	btnId,
					text:	btnId
				});

			// 戻る実行時用に現在の画面情報を保持する
			var states = $.getBackBaseJSON(that);
			// TODO 各種グリッド情報を設定
			var newrepinfos = $.getBackJSON(that, states);
			$.setJSONObject(sendJSON, 'repinfo', newrepinfos, '');

			// 実行ボタン別処理
			switch (btnId) {
			case $.id.btn_sakubaikakb1:

				var msgid = that.checkInputboxFunc();
				if(msgid !==null){
					$.showMessage(msgid);
					return false;
				}

				var rad_maisuhohokb1 = $("input[name="+$.id.rad_maisuhohokb1+"]:checked").val();

				// 転送先情報
				// 枚数指定方法 1:枚数指定
				if (rad_maisuhohokb1 === "1") {
					index = 3;

				// 枚数指定方法 2:同一枚数
				} else if (rad_maisuhohokb1 === "2") {
					index = 4;
				}

				childurl = href[index];

				var txt_coman			= $.getInputboxValue($('#'+$.id_inp.txt_coman));		// コメント
				var txt_mst_yoyakudt	= $.getInputboxValue($('#'+$.id_inp.txt_mst_yoyakudt));	// 商品マスタ予約日付
				var rad_pcardsz1		= $("input[name="+$.id.rad_pcardsz1+"]:checked").val();	// PCサイズ

				// オブジェクト作成
				$.setJSONObject(sendJSON, $.id_inp.txt_coman, txt_coman, txt_coman);
				$.setJSONObject(sendJSON, $.id_inp.txt_mst_yoyakudt, txt_mst_yoyakudt.substr(2), txt_mst_yoyakudt.substr(2));
				$.setJSONObject(sendJSON, $.id.rad_pcardsz1, rad_pcardsz1, rad_pcardsz1);
				$.setJSONObject(sendJSON, $.id.txt_inputno, "", "");
				break;
			case $.id.btn_sakubaikakb2:

				var msgid = that.checkInputboxFunc();
				if(msgid !==null){
					$.showMessage(msgid);
					return false;
				}

				var rad_maisuhohokb2 = $("input[name="+$.id.rad_maisuhohokb2+"]:checked").val();

				// 転送先情報
				// 枚数指定方法 3:店、構成ページ、枚数指定
				if (rad_maisuhohokb2 === "3") {
					index = 5;

				// 枚数指定方法 4:店、部門、枚数指定
				} else if (rad_maisuhohokb2 === "4") {
					index = 6;

				// 枚数指定方法 5:店指定
				} else if (rad_maisuhohokb2 === "5") {
					index = 7;

				// 枚数指定方法 6:全店
				} else if (rad_maisuhohokb2 === "6") {
					index = 8;
				}

				childurl = href[index];

				var txt_coman			= $.getInputboxValue($('#'+$.id_inp.txt_coman));		// コメント
				var txt_mst_yoyakudt	= $.getInputboxValue($('#'+$.id_inp.txt_mst_yoyakudt));	// 商品マスタ予約日付
				var rad_pcardsz2		= $("input[name="+$.id.rad_pcardsz2+"]:checked").val();	// PCサイズ

				// オブジェクト作成
				$.setJSONObject(sendJSON, $.id_inp.txt_coman, txt_coman, txt_coman);
				$.setJSONObject(sendJSON, $.id_inp.txt_mst_yoyakudt, txt_mst_yoyakudt.substr(2), txt_mst_yoyakudt.substr(2));
				$.setJSONObject(sendJSON, $.id.rad_pcardsz2, rad_pcardsz2, rad_pcardsz2);
				break;
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
					sendMode:	1,
					sendParam:	JSON.stringify( sendJSON )
				}
			});
		}
	} });
})(jQuery);