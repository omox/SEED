/**
 * jquery report option
 */
;(function($) {

	// 商品マスタのfunctionをコピー
	$.x001 = $.reportOption;

	$.extend({
		reportOption: $.x001
	});

	$.reportOption.name = 'Out_Reportx280';
	$.reportOption.initialize = function (reportno){	// （必須）初期化
		var that = this;
		// 引き継ぎ情報
		this.jsonHidden = $.getTargetValue();
		// 画面の初回基本設定
		this.setInitObjectState();

		// 初期検索条件設定
		this.jsonInit = $.getInitValue();
		// データ表示エリア初期化
		that.setGrid($.id.gridholder, reportno);

		// 初期化するオブジェクト数設定
		this.initObjNum = this.dedefaultObjNum;

		var isUpdateReport = false;
		// 名称マスタ参照系
		var meisyoSelect = Object.getOwnPropertyNames($.id_mei);
		for ( var sel in meisyoSelect ) {
			if($('#'+$.id_mei[meisyoSelect[sel]]).length > 0){
				$.setMeisyoCombo(that, reportno, $.id_mei[meisyoSelect[sel]], isUpdateReport);
			}
		}
		// 入力テキストボックス系
		var inputbox = Object.getOwnPropertyNames($.id_inp);
		for ( var sel in inputbox ) {
			if($('#'+$.id_inp[inputbox[sel]]).length > 0){
				$.setInputbox(that, reportno, $.id_inp[inputbox[sel]], isUpdateReport);
			}
		}
		// 中分類
		this.setChuBun(reportno, $.id.SelChuBun);
		// 大分類
		this.setDaiBun(reportno, $.id.SelDaiBun);
		// 部門
		this.setBumon(reportno, $.id.SelBumon);

		// 取引先取得
		$.getsetInputboxData(reportno, $.id_inp.txt_ssircd, [{}], $.id.action_init);

		// サブウインドウの初期化
		$.win001.init(that);	// メーカー
		$.win002.init(that);	// 仕入先

		// 初期化終了
		this.initializes =! this.initializes;

		// チェックボックスの設定
		$.initCheckboxCss($("#"+that.focusRootId));
		// キーイベントの設定
		$.initKeyEvent(that);

		// ログ出力
		$.log(that.timeData, 'initialize:');
	};

	$.reportOption.setInitObjectState = function(){	// 画面初期化時の項目制御
		var that = this;
		// 引き継ぎ情報セット
		var sendBtnid = $.getJSONValue(that.jsonHidden, "sendBtnid");
		if(sendBtnid && sendBtnid.length > 0){
			that.sendBtnid = sendBtnid;
			$.reg.search = true;
		}

		$($.id.buttons).show();

		$.setInputBoxDisable($("#"+$.id.btn_copy)).hide();
		$.setInputBoxDisable($("#"+$.id.btn_csv)).hide();
		$.setInputBoxDisable($("#"+$.id.btn_sel_change)).hide();
		$.setInputBoxDisable($("#"+$.id.btn_sel_copy)).hide();
		$.setInputBoxDisable($("#"+$.id.btn_sel_csverr)).hide();
		$.setInputBoxDisable($("#"+$.id.btn_csv_import)).hide();
		$.setInputBoxDisable($("#"+$.id.btn_csv_import_yyk)).hide();
		$.setInputBoxDisable($("#"+$.id.btn_sir)).hide();
		$.setInputBoxDisable($("#"+$.id_inp.txt_makercd)).hide();
		$.setInputBoxDisable($("#"+$.id.btn_maker)).hide();
		$.setInputBoxDisable($("#"+$.id_inp.txt_ssircd));
		$("#"+$.id.btn_sir).parent().next().next().hide();
		$("#"+$.id.btn_sir).parent().next().hide();
		// 各種ボタン
		$('#'+$.id.btn_new).on("click", $.pushChangeReport);

		$.setInputBoxDisable($("#"+$.id.btn_sel_refer)).hide();
		$.initReportInfo("x280", "商品マスタ　一覧");
	};

	$.reportOption.changeReport = function(reportno, btnId){	// 画面遷移
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

		// JSON Object Clone ()
		var sendJSON = [];//JSON.parse( JSON.stringify( that.jsonString ) );
		$.setJSONObject(sendJSON, 'sendBtnid', btnId, $('#'+btnId).attr("title"));					// 実行ボタン情報保持
		$.setJSONObject(sendJSON, 'reportYobi1', that.reportYobiInfo(), that.reportYobiInfo());		// 参照情報保持
		$.setJSONObject(sendJSON, 'callpage', that.name, that.name);								// 呼出し元レポート情報

		// 戻る実行時用に現在の画面情報を保持する
		var states = $.getBackBaseJSON(that);
		var newrepinfos = $.getBackJSON(that, states, true);
		$.setJSONObject(sendJSON, 'repinfo', newrepinfos, '');

		var torihiki = $.getInputboxValue($('#'+$.id_inp.txt_ssircd));

		// 選択行
		var row = $($.id.gridholder).datagrid("getSelected");

		// 実行ボタン別処理
		switch (btnId) {
		case $.id.btn_new:
		case $.id.btn_search:
			if(!row){
				$.showMessage('E00008');
				return false;
			}

			// 転送先情報
			index = 3;
			childurl = href[index];
			$.setJSONObject(sendJSON, $.id.txt_sel_shncd, row.F18, row.F18);
			$.setJSONObject(sendJSON, $.id.txt_sel_shnkn, row.F5, row.F5);
			$.setJSONObject(sendJSON, $.id_inp.txt_shncd, row.F18, row.F18);
			$.setJSONObject(sendJSON, $.id.hiddenTorihiki, torihiki, torihiki);	// 取引先
			$.setJSONObject(sendJSON, 'sendBtnid', $.id.btn_new, '新規');		// 実行ボタン情報保持
			$.setJSONObject(sendJSON, $.id.hiddenStcdTeian, 1, 1);				// 提案状態、デフォルトは作成中
			$.setJSONObject(sendJSON, $.id.hiddenNoTeian, 0, 0);				// 件名No
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

	};

})(jQuery);