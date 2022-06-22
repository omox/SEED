/**
 * jquery report option
 */
;(function($) {

	// 商品マスタのfunctionをコピー
	$.x002 = $.reportOption;

	$.extend({
		reportOption: {
		name:		'Out_Reportx247',			// （必須）レポートオプションの確認
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
		dedefaultObjNum:	141,	// 初期化オブジェクト数
		initObjNum:	-1,
		initedObject: [],
		maxMergeCell: 0,
		onChangeFlag : false,
		onChangeFlag2 : false,
		columnName:'',	// OnClickRowの列名
		queried : false,
		initializes : true,
		onChangeReport: false,
		sendBtnid: "",						// （必須）呼出ボタンID情報
		focusRootId:"cc",					// （キー移動時必須）キー移動イベントのルートとなるパネルのID
		focusParentId:"",					// （キー移動時必須）現在フォーカスがあたっている項目の親となるパネルのID
		focusGridId:"",						// （キー移動時必須）現在フォーカスがあたっているDataGridのID
		editRowIndex:{},					// グリッド編集行保持

		baseTablekbn:"",					// 検索結果のテーブル区分：0-正/1-予約(※予約1の新規→正を参照しているので正、予約2の新規→予約1を参照しているので予)
		baseData:[],						// 検索結果保持用
		yoyakuData:[],						// 検索結果保持用(予約情報)
		data_other:{						// 採番結果など保持用
			SHNCD_NEW:"",
			URICD_NEW:""
		},

		grd_data:[],						// メイン情報：商品マスタ
		grd_data_other:[],					// 補足情報：その他、テーブルに登録しない情報などを保持
		grd_srccd_data:[],					// グリッド情報：ソースコード
		grd_srccd_win_data:[],				// グリッド情報：ソースコード(サブウインドウ)
		grd_tengp1_data:[],					// グリッド情報：仕入グループ
		grd_tengp2_data:[],					// グリッド情報：売価コントロール
		grd_tengp3_data:[],					// グリッド情報：品揃グループ
		grd_tengp4_data:[],					// グリッド情報：店別異部門
		grd_tenkabutsu_data:[],				// グリッド情報：添加物
		grd_allergy_data:[],				// グリッド情報：アレルギー
		grd_group_data:[],					// グリッド情報：グループ分類名
		grd_ahs_win_data:[],				// グリッド情報：自動発注区分(サブウインドウ)

		updConfirmMsg:"",
		/**
		 * ここから下はx002のfunctionを利用しているもの
		 * 差別化する為に別ソースとfunctionの順番が異なる為、注意
		 */
		reportYobiInfo:			$.x002.reportYobiInfo,
		initialize:				$.x002.initialize,
		searched_initialize:	$.x002.searched_initialize,
		judgeRepType:			$.x002.judgeRepType,
		initCondition:			$.x002.initCondition,
		clear:					$.x002.clear,
		validation:				$.x002.validation,
		syncExecution:			$.x002.syncExecution,
		getInputboxNumVal:		$.x002.getInputboxNumVal,
		getNumVal:				$.x002.getNumVal,
		getGridData:			$.x002.getGridData,
		getShnDataMD03111701:	$.x002.getShnDataMD03111701,
		setGridData:			$.x002.setGridData,
		updValidation:			$.x002.updValidation,
		updValidationIn:		$.x002.updValidationIn,
		updConfirm:				$.x002.updConfirm,
		delValidation:			$.x002.delValidation,
		getEasyUI:				$.x002.getEasyUI,
		setData:				$.x002.setData,
		setRadioAreakbn:		$.x002.setRadioAreakbn,
		setGrpkn:				$.x002.setGrpkn,
		setEasyGrid:			$.x002.setEasyGrid,
		setEditableGrid:		$.x002.setEditableGrid,
		setSrccdGrid:			$.x002.setSrccdGrid,
		setTengpGrid:			$.x002.setTengpGrid,
		getGridParams:			$.x002.getGridParams,
		setObjectState:			$.x002.setObjectState,
		getRecord:				$.x002.getRecord,
		setResize:				$.x002.setResize,
		getJSONString:			$.x002.getJSONString,
		tryLoadMethods:			$.x002.tryLoadMethods,
		excel:					$.x002.excel,
		changeInputboxFunc:		$.x002.changeInputboxFunc,
		getInputboxParams:		$.x002.getInputboxParams,
		calcNeireRit:			$.x002.calcNeireRit,
		setDefHatkbnByReadtmptn:$.x002.setDefHatkbnByReadtmptn,
		keyEventInputboxFunc:	$.x002.keyEventInputboxFunc,
		srcCodeValidation:		$.x002.srcCodeValidation,

		/**
		 * ここから下はx247のオリジナルfunctionを利用しているもの
		 * 商品マスタでの仕様変更が発生した場合はこちらにも同様の改修を適用する必要性がある
		 */
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


			// 帳票タイプ判断①：ボタン情報のみで判断
			if(that.sendBtnid===$.id.btn_new||that.sendBtnid===$.id.btn_copy||that.sendBtnid===$.id.btn_sel_copy){
				that.judgeRepType.sei = true;
				that.judgeRepType.sei_new = true;
			}else if(that.sendBtnid===$.id.btn_search||that.sendBtnid===$.id.btn_sel_change||that.sendBtnid===$.id.btn_sei){
				that.judgeRepType.sei = true;
				that.judgeRepType.sei_upd = true;
			}else if(that.sendBtnid===$.id.btn_sel_refer){
				that.judgeRepType.sei = true;
				that.judgeRepType.sei_ref = true;
				that.judgeRepType.ref = true;
			}else if(that.sendBtnid===$.id.btn_yoyaku1){
				that.judgeRepType.yyk= true;
				that.judgeRepType.yyk1= true;
			}else if(that.sendBtnid===$.id.btn_yoyaku2){
				that.judgeRepType.yyk= true;
				that.judgeRepType.yyk2= true;
			}else if(that.sendBtnid===$.id.btn_err_change){
				that.judgeRepType.err = true;
			}


			var hidobjids = [];	// 非表示

			that.baseTablekbn = $.id.value_tablekbn_sei;						// 情報取得先
			hidobjids = hidobjids.concat([$.id.btn_yoyaku1,$.id.btn_yoyaku2]);
			// 新規：新規・新規コピー・選択コピーボタン押下時
			if(that.judgeRepType.sei_new){
				hidobjids = hidobjids.concat([$.id.btn_del]);

				$("#disp_record_info").hide();

				$.initReportInfo("x247", "提案商品登録", "新規");

			// 変更：検索・変更・正ボタン押下時
			}else if(that.judgeRepType.sei_upd){
				if(that.reportYobiInfo()==='1'){
					$.initReportInfo("x247", "提案商品　参照", "参照");
				}else{
					$.initReportInfo("x247", "提案商品　変更", "変更");
				}
			}else if(that.judgeRepType.sei_ref){
				$.initReportInfo("x247", "提案商品　参照", "参照");

			// 予約：予約1・予約2ボタン押下時
			}else if(that.judgeRepType.yyk){
				var yoyaku_del_txt = "予約取消";
				$("#"+$.id.btn_del).attr("title", yoyaku_del_txt).linkbutton({text:yoyaku_del_txt});

				// 予約ボタン1
				if(that.judgeRepType.yyk1){

					$("#"+$.id.btn_sei).linkbutton('enable');
					$("#"+$.id.btn_sei).attr('tabindex', $("#"+$.id.btn_yoyaku1).attr('tabindex')).show();
					hidobjids = hidobjids.concat([$.id.btn_yoyaku1]);
				// 予約ボタン2
				}else if(that.judgeRepType.yyk2){
					that.baseTablekbn = $.id.value_tablekbn_yyk;

					$("#"+$.id.btn_sei).linkbutton('enable');
					$("#"+$.id.btn_sei).attr('tabindex', $("#"+$.id.btn_yoyaku2).attr('tabindex')).show();
					hidobjids = hidobjids.concat([$.id.btn_yoyaku2]);
				}
				if(that.reportYobiInfo()==='1'){
					$.initReportInfo("x247", "提案商品マスタ　参照", "参照");
				}else{
					$.initReportInfo("x247", "提案商品マスタ　変更", "変更");
				}
			}else if(that.judgeRepType.err){
				// 特殊引き継ぎ情報設定
				$('#'+$.id.txt_seq).val($.getJSONValue(that.jsonHidden, $.id.txt_seq));
				$('#'+$.id.txt_inputno).val($.getJSONValue(that.jsonHidden, $.id.txt_inputno));
				var txt_csv_updkbn = $.getJSONValue(that.jsonHidden, $.id.txt_csv_updkbn);
				$('#'+$.id.txt_csv_updkbn).val(txt_csv_updkbn);

				hidobjids = hidobjids.concat([$.id.btn_yoyaku1,$.id.btn_yoyaku2]);

				// 正の場合
				if($.id.value_csvupdkbn_new === txt_csv_updkbn||$.id.value_csvupdkbn_upd === txt_csv_updkbn){
					$("#"+$.id_inp.txt_tenbaikadt).closest("span").css('visibility','hidden');
					that.judgeRepType.err_sei = true;
				}else{
					that.judgeRepType.err_yyk1= true;
				}
				$.initReportInfo("x247", "提案商品　CSVエラー修正", "エラー修正");
			}

			// 全体処理
			if(that.reportYobiInfo()==='1'){
				that.judgeRepType.ref = true;

				hidobjids = hidobjids.concat([$.id.btn_cancel,$.id.btn_upd,$.id.btn_del]);

				$('#'+$.id.btn_yoyaku1).on("click", $.pushChangeReport);
				$('#'+$.id.btn_yoyaku2).on("click", $.pushChangeReport);
				$('#'+$.id.btn_sei).on("click", $.pushChangeReport);

				$.setInputBoxDisable($($.id.hiddenChangedIdx));
			}else{
				$('#'+$.id.btn_yoyaku1).on("click", $.pushChangeReport);
				$('#'+$.id.btn_yoyaku2).on("click", $.pushChangeReport);
				$('#'+$.id.btn_sei).on("click", $.pushChangeReport);
				$('#'+$.id.btn_cancel).on("click", $.pushChangeReport);
			}

			// 参照系の場合
			if(that.judgeRepType.ref){
				// サブウインドウ系非表示
				hidobjids = hidobjids.concat([$.id.btn_maker,$.id.btn_hsptn,$.id.btn_tengp+1,$.id.btn_tengp+2,$.id.btn_tengp+4
								,$.id.btn_sir+$.id.grd_tengp+1,$.id.btn_hsptn+$.id.grd_tengp+1]);
			}

			//商品コード、取引先コードが編集不可にする
			//hidobjids = hidobjids.concat([$.id.btn_sir]);
			// 非表示化
			for (var i = 0; i < hidobjids.length; i++) {
				$.setInputBoxDisable($('#'+hidobjids[i])).hide();
			}
			$($.id.buttons).show();
			// 変更
			$($.id.hiddenChangedIdx).val('');
		},
		setSearchObjectState: function(){	// 検索結果をうけての項目制御
			var that = this;

			var login_dt = parent.$('#login_dt').text().replace(/\//g, "");	// 処理日付
			var sysdate = login_dt;											// 比較用処理日付
			var num_yoyaku = $('#'+$.id.txt_yoyaku).val()*1;				// 予約件数

			var txt_yoyakudt = $.getInputboxValue($('#'+$.id_inp.txt_yoyakudt));
			var txt_tenbaikadt = $.getInputboxValue($('#'+$.id_inp.txt_tenbaikadt));

			// 帳票タイプ判断②:ボタンと予約情報で判断
			if(that.judgeRepType.yyk1){
				if(that.reportYobiInfo()==='1'){
					that.judgeRepType.yyk1_ref= true;
				}else{
					// 予約件数が0件の場合、予約1登録がない
					if(num_yoyaku===0){
						that.judgeRepType.yyk1_new= true;
					}else if(num_yoyaku > 0){
						that.judgeRepType.yyk1_upd= true;
					}
				}
			}else if(that.judgeRepType.yyk2){
				if(that.reportYobiInfo()==='1'){
					that.judgeRepType.yyk2_ref= true;
				}else{
					// 予約件数が1件の場合、予約2登録がない
					if(num_yoyaku===1){
						that.judgeRepType.yyk2_new= true;
						$.setInputboxValue($('#'+$.id_inp.txt_yoyakudt),'');
						$.setInputboxValue($('#'+$.id_inp.txt_tenbaikadt),'');
					}else if(num_yoyaku > 1){
						that.judgeRepType.yyk2_upd= true;
					}
				}
			}else if(that.judgeRepType.err){
				if(that.judgeRepType.err_sei){
					if($.isEmptyVal(txt_yoyakudt, true) && $.isEmptyVal(txt_tenbaikadt, true)){
						var txt_csv_updkbn = $('#'+$.id.txt_csv_updkbn).val();
						if($.id.value_csvupdkbn_new === txt_csv_updkbn){
							that.judgeRepType.err_sei_new = true;
						}else{
							that.judgeRepType.err_sei_upd = true;
						}
					}
				}

				if(that.judgeRepType.err_yyk1){
					// 予1-新規
					if(that.yoyakuData.length === 0){
						that.judgeRepType.err_yyk1_new = true;
					// 予1-変更
					}else if(that.yoyakuData.length === 1 && that.yoyakuData[0]["F2"]===txt_yoyakudt && that.yoyakuData[0]["F3"]===txt_tenbaikadt){
						that.judgeRepType.err_yyk1_upd = true;
					}
				}
			} else {
				if(num_yoyaku!==0){
					$.setInputBoxDisable($("#"+$.id_inp.txt_receiptan));
					$.setInputBoxDisable($("#"+$.id_inp.txt_receiptkn));
					$.setInputBoxDisable($("#"+$.id_mei.kbn117));
					$.setInputBoxDisable($('#'+$.id.btn_srccd));
				}
			}

			// 子テーブルのデータ取得先判断
			that.baseTablekbn = $.id.value_tablekbn_sei;
			if(that.judgeRepType.yyk1_upd||that.judgeRepType.yyk1_ref||that.judgeRepType.yyk2){
				that.baseTablekbn = $.id.value_tablekbn_yyk;
			}else if(that.judgeRepType.err){
				that.baseTablekbn = $.id.value_tablekbn_csv;
			}

			var msg = "";

			// 項目利用可不可制御①：ボタンと予約情報の数で判断
			// 新規：新規・新規コピー・選択コピーボタン押下時
			if(that.judgeRepType.sei_new||that.judgeRepType.err_sei_new){
				// 使用不可
				$.setInputBoxDisable($("#"+$.id_inp.txt_yoyakudt));
				$.setInputBoxDisable($("#"+$.id_inp.txt_tenbaikadt)).closest("span").css('visibility','hidden');
				$("#"+$.id_mei.kbn143).closest("div").removeClass("lbl_box").addClass("inp_box");

				// 新規の場合のみ必須項目 ※商品種類との兼ね合いなのでここでは必須にしない
//				$.setInputBoxRequired($("#"+$.id_inp.txt_daicd));
//				$.setInputBoxRequired($("#"+$.id_inp.txt_chucd));
//				$.setInputBoxRequired($("#"+$.id_inp.txt_shocd));
			}else if(that.judgeRepType.sei_upd||that.judgeRepType.err_sei_upd){
				// 使用不可
				$.setInputBoxDisable($("#"+$.id_inp.txt_yoyakudt));
				$.setInputBoxDisable($("#"+$.id_inp.txt_tenbaikadt)).closest("span").css('visibility','hidden');
				$.setInputBoxDisable($("#"+$.id_inp.txt_shncd));
				$.setInputBoxDisable($("#"+$.id_mei.kbn143)).closest("span").hide();

				// 予約件数が0件の場合、予約2ボタンは使用不可(まず予約1で登録)
				if(num_yoyaku===0){
					$.setInputBoxDisable($("#"+$.id.btn_yoyaku2));
				// 予約がある場合は正は削除不可
//				}else{
//					$.setInputBoxDisable($("#"+$.id.btn_del));
				}
			}else if(that.judgeRepType.sei_ref){
				$.setInputBoxDisable($("#"+$.id.btn_del)).hide();
				$.setInputBoxDisable($("#"+$.id.btn_cancel)).hide();
				// 使用不可
				$.setInputBoxDisable($("#"+$.id_inp.txt_yoyakudt));
				$.setInputBoxDisable($("#"+$.id_inp.txt_tenbaikadt)).closest("span").css('visibility','hidden');
				$.setInputBoxDisable($("#"+$.id_inp.txt_shncd));
				$.setInputBoxDisable($("#"+$.id_mei.kbn143)).closest("span").hide();

				// 予約件数が0件の場合、予約2ボタンは使用不可(まず予約1で登録)
				if(num_yoyaku===0){
					$.setInputBoxDisable($("#"+$.id.btn_yoyaku1));
					$.setInputBoxDisable($("#"+$.id.btn_yoyaku2));
				}else if(num_yoyaku===1){
					$.setInputBoxDisable($("#"+$.id.btn_yoyaku2));
				}
			}else if(that.judgeRepType.yyk1_new){
				// 予約件数が0件の場合、予約1登録がないため、予約2ボタンは使用不可(まず予約1で登録)
				$.setInputBoxDisable($("#"+$.id.btn_yoyaku2));

				$.setInputBoxDisable($("#"+$.id.btn_del)).hide();
			}else if(that.judgeRepType.yyk1_upd){
				$.setInputBoxDisable($("#"+$.id_inp.txt_yoyakudt));
				$.setInputBoxDisable($("#"+$.id_inp.txt_tenbaikadt));
			}else if(that.judgeRepType.yyk1_ref){
				$.setInputBoxDisable($("#"+$.id_inp.txt_yoyakudt));
				$.setInputBoxDisable($("#"+$.id_inp.txt_tenbaikadt));
				if(num_yoyaku===1){
					$.setInputBoxDisable($("#"+$.id.btn_yoyaku2));
				}
			}else if(that.judgeRepType.yyk2_new){
				// クリア
				$.setInputboxValue($('#'+$.id_inp.txt_yoyakudt), '');
				$.setInputboxValue($('#'+$.id_inp.txt_tenbaikadt), '');

				$.setInputBoxDisable($("#"+$.id.btn_del)).hide();
			}else if(that.judgeRepType.yyk2_upd){
				$.setInputBoxDisable($("#"+$.id_inp.txt_yoyakudt));
				$.setInputBoxDisable($("#"+$.id_inp.txt_tenbaikadt));
			}else if(that.judgeRepType.yyk2_ref){
				$.setInputBoxDisable($("#"+$.id_inp.txt_yoyakudt));
				$.setInputBoxDisable($("#"+$.id_inp.txt_tenbaikadt));
			}
			// 大枠
			if(that.judgeRepType.yyk){
				$.setInputBoxDisable($("#"+$.id_inp.txt_shncd));
				$.setInputBoxDisable($("#"+$.id_mei.kbn143)).closest("span").hide();
				$.setInputBoxDisable($("#"+$.id_inp.txt_receiptan));
				$.setInputBoxDisable($("#"+$.id_inp.txt_receiptkn));
				$.setInputBoxDisable($("#"+$.id_mei.kbn117));
				$.setInputBoxDisable($("#"+$.id_mei.kbn136));
			}
			if(that.judgeRepType.err){
				$.setInputBoxDisable($("#"+$.id_inp.txt_yoyakudt));
				$.setInputBoxDisable($("#"+$.id_inp.txt_tenbaikadt));
				if(that.judgeRepType.err_sei){
					$("#"+$.id_inp.txt_tenbaikadt).closest("span").css('visibility','hidden');
				}else{
					$.setInputBoxDisable($("#"+$.id_inp.txt_receiptan));
					$.setInputBoxDisable($("#"+$.id_inp.txt_receiptkn));
					$.setInputBoxDisable($("#"+$.id_mei.kbn117));
				}
				// 正の新規以外
				if(!that.judgeRepType.err_sei_new){
					$.setInputBoxDisable($("#"+$.id_inp.txt_shncd));
					$.setInputBoxDisable($("#"+$.id_mei.kbn143)).closest("span").hide();
				}
			}
			// 使用不可
			$.setInputBoxDisable($("#"+"chk_iryoreflg"));


			// 予約ボタン色変更
			if(num_yoyaku===1){
				$("#"+$.id.btn_yoyaku1+ " span:first-child").addClass('btn_warn');
			}else if(num_yoyaku===2){
				$("#"+$.id.btn_yoyaku1+ " span:first-child").addClass('btn_warn');
				$("#"+$.id.btn_yoyaku2+ " span:first-child").addClass('btn_warn');
			}

			// 項目利用可不可制御②：ボタンと、正・予約情報の内容で判断
			var isAbleChange = true;
			// 正 .変更
			if(that.judgeRepType.sei_upd||that.judgeRepType.err_sei_upd){
				// 予約1がある場合
				if( num_yoyaku > 0 && that.yoyakuData.length > 0) {
					// 予約1の送信日（店売価実施日－4日） >= 処理日付であれば変更可
					var senddate = $.convertDate(that.yoyakuData[0]["F3"], -4);
					isAbleChange = sysdate*1 <= senddate*1;
				}

			// 予1.変更
			}else if(that.judgeRepType.yyk1_upd||that.judgeRepType.err_yyk1_upd){
				// 処理日付＝＜送信日（店売価実施日-4日）　であれば可能
				var senddate = $.convertDate(txt_tenbaikadt, -4);
				isAbleChange = sysdate*1 <= senddate*1;

			// 予2.変更
			}else if(that.judgeRepType.yyk2_upd){
				// 処理日付＝＜送信日（店売価実施日-4日）　であれば可能
				var senddate = $.convertDate(txt_tenbaikadt, -4);
				isAbleChange = sysdate*1 <= senddate*1;
			}

			// CSVエラー修正の場合
			if(that.judgeRepType.err){
				if(!that.judgeRepType.err_sei_new && !that.judgeRepType.err_sei_upd && !that.judgeRepType.err_yyk1_new && !that.judgeRepType.err_yyk1_upd){
					isAbleChange = false;
				}
			}

			// 参照の場合
			if(that.judgeRepType.sei_ref||that.judgeRepType.yyk1_ref||that.judgeRepType.yyk2_ref){
				isAbleChange = false;
			}

			if(!isAbleChange){
				$("#"+$.id.btn_upd).linkbutton('disable');
				$("#"+$.id.btn_upd).attr('tabindex', -1);
				$("#"+$.id.btn_upd).hide();
			}
		},
		success: function(reportno, sortable){	// （必須）正処理
			if (sortable) sortable=1; else sortable=0;
			var that = this;

			// WEB商談　状態
			this.setCombogrid(reportno, $.id.SelStcdKenmei);
			$.setInputboxValue($('#'+$.id.txt_sel_shncd), '');

			// 検索実行
			var txt_sel_shncd		= $.getJSONObject(this.jsonString, $.id.txt_sel_shncd).value;		// 検索商品コード
			var txt_shncd			= $.getJSONObject(this.jsonString, $.id_inp.txt_shncd).value;		// 入力商品コード
			var txt_seq				= $.getJSONObject(this.jsonString, $.id.txt_seq).value;				// CSVエラー.SEQ
			var txt_inputno			= $.getJSONObject(this.jsonString, $.id.txt_inputno).value;			// CSVエラー.入力番号
			var txt_csv_updkbn		= $.getJSONObject(this.jsonString, $.id.txt_csv_updkbn).value;		// CSVエラー.CSV登録区分
			var txt_yoyakudt		= $.getJSONObject(this.jsonString, $.id_inp.txt_yoyakudt).value;	// CSVエラー用.マスタ変更予定日
			var txt_tenbaikadt		= $.getJSONObject(this.jsonString, $.id_inp.txt_tenbaikadt).value;	// CSVエラー用.店売価実施日

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$.appendMask();
			$.appendMaskMsg();

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,			// レポート名
					SEL_SHNCD:		txt_sel_shncd,		// 検索商品コード
					SHNCD:			txt_shncd,			// 入力商品コード
					SEQ:			txt_seq,			// CSVエラー.SEQ
					INPUTNO:		txt_inputno,		// CSVエラー.入力番号
					CSV_UPDKBN:		txt_csv_updkbn,		// CSVエラー.CSV登録区分
					YOYAKUDT:		txt_shncd,			// CSVエラー用.マスタ変更予定日
					TENBAIKADT:		txt_tenbaikadt,		// CSVエラー用.店売価実施日
					SENDBTNID:		that.sendBtnid,
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


					var opts = JSON.parse(json).opts


					// 検索結果を保持
					that.baseData = JSON.parse(json).rows;

					if(opts && opts.rows_y){
						that.yoyakuData = opts.rows_y;
					}

					// for_inp項目_初期検索時に参照値を適用しない項目からfor_inpを削除する。
					var notSetTargetList  = [$.id_inp.txt_rg_idenflg, $.id_inp.txt_edi_rkbn]
					for (var i=0; i<notSetTargetList.length; i++){
						var onjId = notSetTargetList[i]
						var obj = $('#'+onjId)
						if(obj){
							var for_inp = obj[0].getAttribute('for_inp')
							obj[0].setAttribute("for_inp_not",for_inp);
							$('#'+onjId).removeAttr('for_inp');
						}
					}

					// メインデータ表示
					that.setData(that.baseData, opts);
					// 検索結果をうけての商品マスタ項目の制御、各種フラグ判断
					that.setSearchObjectState();

					// 検索結果をうけての子テーブルマスタ項目などの初期化設定
					that.searched_initialize(reportno);

					// 計算項目算出のため、変更時処理呼出
					var cals = [$.id_inp.txt_rg_baikaam, $.id_inp.txt_hs_baikaam];
					for (var i=0; i<cals.length; i++){
						var id = cals[i];
						that.changeInputboxFunc( that, id, $.getInputboxValue($('#'+id)), $('#'+id), true);
					}

					// 現在情報を変数に格納(追加した情報については個別にロード成功時に実施)
					that.setGridData(that.getGridData("", "", "grd_data"), "grd_data");


					// 状態保存
					$.saveState2(reportno, that.getJSONString());

					// 隠し情報初期化
					// 新規(正)：新規・新規コピー・選択コピーボタン押下時
					var isNew = that.judgeRepType.sei_new || that.judgeRepType.err_sei_new;
					// 新規(予1)：予1ボタン押下時、かつ予約数0
					var isNewY1 = that.judgeRepType.yyk1_new || that.judgeRepType.err_yyk1_new;
					// 新規(予2)：予2ボタン押下時、かつ予約数1
					var isNewY2 = that.judgeRepType.yyk2_new;
					if(isNew){
						$($.id.hiddenChangedIdx).val("1");						// 変更行Index

					}else if(that.judgeRepType.err){
						// エラー修正画面では変更箇所が存在しない場合でも登録可能とする。
						$($.id.hiddenChangedIdx).val("1");						// 変更行Index

					}else{
						$($.id.hiddenChangedIdx).val("");						// 変更行Index
					}

					if (isNew) {
						// TODO
						// 仕様書に新規コピー登録時の初期値についての詳細な記載がない。
						// 暫定で新規コピー時の初期値はマスタの値とする。

						if(that.sendBtnid == $.id.btn_new){
							// 新規登録時にのみデフォルト値適用処理を実行し、新規コピー登録、CSVエラー修正新規登録時には実行しない
							$('#'+$.id_mei.kbn120).combobox('setValue','3');	// 税区分
							$('#'+$.id_mei.kbn121).combobox('setValue','1');	// 定貫・不定貫項目
						}
					}

					// that.queried = true;

					that.setTorihiki(reportno);
					// 予約ボタンでの遷移、かつマスタ変更予定日が使用可能の場合
					if((that.sendBtnid===$.id.btn_yoyaku1 || that.sendBtnid===$.id.btn_yoyaku2)
							&& $('#'+$.id_inp.txt_yoyakudt).attr('disabled') == undefined){
						var target = $.getInputboxTextbox($('#'+$.id_inp.txt_yoyakudt));
						target.focus();
					}

					// ログ出力
					$.log(that.timeData, 'loaded:');
				}
			);
		},
		updSuccess: function(id){	// validation OK時 の update処理
			var that = this;

			// 前提情報
			var txt_sel_shncd		= $.getJSONObject(this.jsonString, $.id.txt_sel_shncd).value;		// 検索商品コード
			var txt_shncd			= $.getJSONObject(this.jsonString, $.id_inp.txt_shncd).value;		// 入力商品コード
			var txt_seq				= $.getJSONObject(this.jsonString, $.id.txt_seq).value;				// CSVエラー.SEQ
			var txt_inputno			= $.getJSONObject(this.jsonString, $.id.txt_inputno).value;			// CSVエラー.入力番号
			var txt_csv_updkbn		= $.getJSONObject(this.jsonString, $.id.txt_csv_updkbn).value;		// CSVエラー.CSV登録区分
			var txt_yoyakudt		= $.getJSONObject(this.jsonString, $.id_inp.txt_yoyakudt).value;	// CSVエラー用.マスタ変更予定日
			var txt_tenbaikadt		= $.getJSONObject(this.jsonString, $.id_inp.txt_tenbaikadt).value;	// CSVエラー用.店売価実施日

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$.appendMask();
			$.appendMaskMsg();

			// 変更行情報取得
			var changedIndex = $($.id.hiddenChangedIdx).val().split(",");

			// 基本情報
			var targetData = that.grd_data;
			// 補足情報
			var targetDataOther = that.grd_data_other;

			var targetAddData = [];
			// 新規：新規・新規コピー・選択コピーボタン押下時
			if(that.sendBtnid===$.id.btn_new||that.sendBtnid===$.id.btn_copy||that.sendBtnid===$.id.btn_sel_copy){
				// マスタ変更予定日=添付資料（MD03111002）を参照。
				// 店売価実施日=添付資料（MD03111002）を参照。
				// 販売コード=添付資料（MD03100902）の”販売コード付番”を参照。
				// 種別コード=画面種別コードに何も入力がない場合は、半角スペースを登録する。
				// 新規・変更・予約で共通、かつチェックもないのでJAVA側で処理
				// 登録元=0
				// 更新区分=０：通常
				// メーカーコード：空白の場合はソースコードから取得した値を登録する。設定されている場合は、その値を登録する。
				// validation時に値を取得、設定
			// 変更：検索・変更・正ボタン押下時
			}else if(that.sendBtnid===$.id.btn_search||that.sendBtnid===$.id.btn_sel_change||that.sendBtnid===$.id.btn_sei){

				// 商品マスタと予約マスタの両方に同一商品がある場合の予約変更方法
				var refDatas = [];
				refDatas.push(that.baseData[0]);
				for (var i=0; i<that.yoyakuData.length; i++){
					refDatas.push(that.yoyakuData[i]);
				}
				targetAddData = that.getShnDataMD03111701(targetData[0], refDatas);

			// 予約1：予約1押下時
			}else if(that.sendBtnid===$.id.btn_yoyaku1){

				// 商品マスタと予約マスタの両方に同一商品がある場合の予約変更方法
				var refDatas = [];
				for (var i=0; i<that.yoyakuData.length; i++){
					refDatas.push(that.yoyakuData[i]);
				}
				targetAddData = that.getShnDataMD03111701(targetData[0], refDatas);

			// 予約2：予約2ボタン押下時
			}else if(that.sendBtnid===$.id.btn_yoyaku2){

			}

			// **** 個別データグリッド
			// ソースコード
			var targetRowsSrccd = that.grd_srccd_win_data;

			// 店別異部門
			var targetRowsTengp4= that.grd_tengp4_data;

			// 品揃えグループ
			var targetRowsTengp3= that.grd_tengp3_data;

			// 売価グループ
			var targetRowsTengp2= that.grd_tengp2_data;

			// 仕入グループ
			var targetRowsTengp1= that.grd_tengp1_data;

			// 添加物
			var targetRowsTenkabutsu = that.grd_tenkabutsu_data.concat(that.grd_allergy_data);

			// グループ分類名
			var targetRowsGroup= that.grd_group_data;

			// 自動発注
			var targetRowsAhs= that.grd_ahs_win_data;
			// truncate leading zeros
			var targetDataF1 = targetData[0]["F1"];
			targetData[0]["F1"] = targetDataF1;

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,		// レポート名
					action:			$.id.action_update,	// 実行処理情報
					obj:			id,					// 実行オブジェクト
					SEL_SHNCD:		txt_sel_shncd,		// 検索商品コード
					SHNCD:			txt_shncd,			// 入力商品コード
					SEQ:			txt_seq,			// CSVエラー.SEQ
					INPUTNO:		txt_inputno,		// CSVエラー.入力番号
					CSV_UPDKBN:		txt_csv_updkbn,		// CSVエラー.CSV登録区分
					YOYAKUDT:		txt_shncd,			// CSVエラー用.マスタ変更予定日
					TENBAIKADT:		txt_tenbaikadt,		// CSVエラー用.店売価実施日
					SENDBTNID:		that.sendBtnid,
//					IDX:			$($.id.hiddenChangedIdx).val(),	// 更新対象Index
//					DATA:			JSON.stringify(targetRows),		// 更新対象情報
					DATA:				JSON.stringify(targetData),				// 更新対象情報
					DATA_OTHER:			JSON.stringify(targetDataOther),		// 更新対象補足情報
					DATA_ADD:			JSON.stringify(targetAddData),			// 更新対象追加情報(MD03111701:予約同一項目変更用の追加データ)
					DATA_SRCCD:			JSON.stringify(targetRowsSrccd),		// 個別データグリッド:ソースコード
					DATA_TENGP4:		JSON.stringify(targetRowsTengp4),		// 個別データグリッド:店別異部門
					DATA_TENGP3:		JSON.stringify(targetRowsTengp3),		// 個別データグリッド:品揃えグループ
					DATA_TENGP2:		JSON.stringify(targetRowsTengp2),		// 個別データグリッド:売価グループ
					DATA_TENGP1:		JSON.stringify(targetRowsTengp1),		// 個別データグリッド:仕入グループ
					DATA_TENKABUTSU:	JSON.stringify(targetRowsTenkabutsu),	// 個別データグリッド:添加物
					DATA_GROUP:			JSON.stringify(targetRowsGroup),		// 個別データグリッド:グループ分類名
					DATA_AHS:			JSON.stringify(targetRowsAhs),			// 個別データグリッド:自動発注
					t:				(new Date()).getTime(),
					TEIAN:			$('#'+$.id.hiddenNoTeian).val(),				// 提案件名No
					STATUS:		$('#'+$.id.SelStcdKenmei).combogrid('getValue'),	// 状態
				},
				function(data){
					// 検索処理エラー判定
					if($.updError(id, data)) return false;

					var afterFunc = function(){
						// 初期化
						that.clear();
						that.changeReport(that.name, $.id.btn_upd);
					};
					$.updNormal(data, afterFunc);

					// ログ出力
					$.log(that.timeData, 'loaded:');
				}
			);
		},
		delSuccess: function(id){
			var that = this;
			var is_warning = false;

			// 前提情報
			var txt_sel_shncd		= $.getJSONObject(this.jsonString, $.id.txt_sel_shncd).value;		// 検索商品コード
			var txt_shncd			= $.getJSONObject(this.jsonString, $.id_inp.txt_shncd).value;		// 入力商品コード
			var txt_seq				= $.getJSONObject(this.jsonString, $.id.txt_seq).value;				// CSVエラー.SEQ
			var txt_inputno			= $.getJSONObject(this.jsonString, $.id.txt_inputno).value;			// CSVエラー.入力番号
			var txt_csv_updkbn		= $.getJSONObject(this.jsonString, $.id.txt_csv_updkbn).value;		// CSVエラー.CSV登録区分
			var txt_yoyakudt		= $.getJSONObject(this.jsonString, $.id_inp.txt_yoyakudt).value;	// CSVエラー用.マスタ変更予定日
			var txt_tenbaikadt		= $.getJSONObject(this.jsonString, $.id_inp.txt_tenbaikadt).value;	// CSVエラー用.店売価実施日


			// 基本情報
			var targetData = that.grd_data;

			// **** 個別データグリッド
			// ソースコード
			var targetRowsSrccd = that.grd_srccd_win_data;

			// 店別異部門
			var targetRowsTengp4= that.grd_tengp4_data;

			// 品揃えグループ
			var targetRowsTengp3= that.grd_tengp3_data;

			// 売価グループ
			var targetRowsTengp2= that.grd_tengp2_data;

			// 仕入グループ
			var targetRowsTengp1= that.grd_tengp1_data;

			// 添加物
			var targetRowsTenkabutsu = that.grd_tenkabutsu_data.concat(that.grd_allergy_data);

			// グループ分類名
			var targetRowsGroup= that.grd_group_data;

			// 自動発注
			var targetRowsAhs= that.grd_ahs_win_data;

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$.appendMaskMsg();

			// truncate leading zeros
			var targetDataF1 = targetData[0]["F1"];
			targetData[0]["F1"] = targetDataF1.replace(/^0+/, '');

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,		// レポート名
					action:			$.id.action_delete,	// 実行処理情報
					obj:			id,								// 実行オブジェクト
					SEL_SHNCD:		txt_sel_shncd,		// 検索商品コード
					SHNCD:			txt_shncd,			// 入力商品コード
					SEQ:			txt_seq,			// CSVエラー.SEQ
					INPUTNO:		txt_inputno,		// CSVエラー.入力番号
					CSV_UPDKBN:		txt_csv_updkbn,		// CSVエラー.CSV登録区分
					YOYAKUDT:		txt_shncd,			// CSVエラー用.マスタ変更予定日
					TENBAIKADT:		txt_tenbaikadt,		// CSVエラー用.店売価実施日
					SENDBTNID:		that.sendBtnid,
					//IDX:			$($.id.hiddenChangedIdx).val(),	// 更新対象Index
					//DATA:			JSON.stringify(targetRows),		// 更新対象情報
					DATA:				JSON.stringify(targetData),				// 更新対象情報
					DATA_SRCCD:			JSON.stringify(targetRowsSrccd),		// 個別データグリッド:ソースコード
					DATA_TENGP4:		JSON.stringify(targetRowsTengp4),		// 個別データグリッド:店別異部門
					DATA_TENGP3:		JSON.stringify(targetRowsTengp3),		// 個別データグリッド:品揃えグループ
					DATA_TENGP2:		JSON.stringify(targetRowsTengp2),		// 個別データグリッド:売価グループ
					DATA_TENGP1:		JSON.stringify(targetRowsTengp1),		// 個別データグリッド:仕入グループ
					DATA_TENKABUTSU:	JSON.stringify(targetRowsTenkabutsu),	// 個別データグリッド:添加物
					DATA_GROUP:			JSON.stringify(targetRowsGroup),		// 個別データグリッド:グループ分類名
					DATA_AHS:			JSON.stringify(targetRowsAhs),			// 個別データグリッド:自動発注
					t:				(new Date()).getTime(),
					TEIAN:			$('#'+$.id.hiddenNoTeian).val(),			// 提案件名No
					STATUS:		$('#'+$.id.SelStcdKenmei).combogrid('getValue'),			// 状態
				},
				function(data){
					// 検索処理エラー判定
					if($.delError(id, data)) return false;

					var afterFunc = function(){
						// 初期化
						that.clear();
						that.changeReport(that.name, $.id.btn_del);
					};
					$.delNormal(data, afterFunc);

					// ログ出力
					$.log(that.timeData, 'loaded:');
				}
			);
		},
		setCombogrid: function(reportno, id){
			var that = this;
			var idx = -1;
			var teianState = parseInt($('#'+$.id.hiddenStcdTeian).val());
			var readonly = (teianState != 1);
			$('#'+id).combogrid({
				panelWidth:120,
				panelHeight:null,
				panelMaxHeight:200,
				required: false,
				editable: false,
				readonly: readonly,
				showHeader: false,
				idField:'VALUE',
				textField:'TEXT',
				columns:[[
					{field:'TEXT',	title:'',	width:120}
				]],
				fitColumns: true,
				onShowPanel:function(){
					$.setScrollGrid(this);
				},
				onChange:function(newValue, oldValue){
					// 検索ボタン有効化
					$.setButtonState('#'+$.id.btn_search, true, id);
					var curVal = $('#'+id).combogrid('getValue');
					if (curVal != teianState) {
						$($.id.hiddenChangedIdx).val("1");
					} else {
						$($.id.hiddenChangedIdx).val("");
					}
				},
				onLoadSuccess:function(data){
					// 初期化
					if (data.rows.length > 0){
						$('#'+id).combogrid('grid').datagrid('selectRow', teianState - 1);
					}
				}
			});
			$('#'+id).combogrid('grid').datagrid('loadData', $.SEL_DATA.TEIAN_SHN_STATE_DATA);
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
			var sendJSON = JSON.parse( JSON.stringify( that.jsonString ) );
			$.setJSONObject(sendJSON, 'sendBtnid', btnId, $('#'+btnId).attr("title"));					// 実行ボタン情報保持
			$.setJSONObject(sendJSON, 'reportYobi1', that.reportYobiInfo(), that.reportYobiInfo());		// 参照情報保持
			$.setJSONObject(sendJSON, 'callpage', that.name, that.name);								// 呼出し元レポート情報
			$.setJSONObject(sendJSON, $.id.txt_yoyaku, $('#'+$.id.txt_yoyaku).val(), $('#'+$.id.txt_yoyaku).val());

			// 戻る実行時用に現在の画面情報を保持する
			var states = $.getBackBaseJSON(that);
			var newrepinfos = $.getBackJSON(that, states, false);
			$.setJSONObject(sendJSON, 'repinfo', newrepinfos, '');

			var callpage = $.getJSONValue(that.jsonHidden, "callpage");
			// 実行ボタン別処理
			switch (btnId) {
				case $.id.btn_back:
					// 元画面情報
					// 転送先情報
					index = 2;
					if (callpage==='Out_Reportx253') {
						index = 6;
					} else if (callpage==='Out_Reportx280') {
						index = 7;
					}
	//				if(that.reportYobiInfo()==='1'){
	//					index = 2;
	//				}
//					// 元画面情報
//					for (var i = 0; i < newrepinfos.length; i++) {
//						var callpage = newrepinfos[i].id;
//						alert(callpage);
//						if(callpage==='Out_Reportx005'){
//							index = 5;
//						}
//					}
					sendMode = 2;
					childurl = href[index];
					break;
				case $.id.btn_cancel:
				case $.id.btn_upd:
				case $.id.btn_del:
					// 転送先情報
					index = 2;
	//				if(that.reportYobiInfo()==='1'){
	//					index = 2;
	//				}
	//				// 元画面情報
	//				for (var i = 0; i < newrepinfos.length; i++) {
	//					var callpage = newrepinfos[i].id;
	//					if(callpage==='Out_Reportx005'){
	//						index = 5;
	//					}
	//				}
					if (callpage==='Out_Reportx253') {
						index = 6;
					} else if (callpage==='Out_Reportx280') {
						index = 7;
					}
					sendMode = 2;
					childurl = href[index];
					break;
				case $.id.btn_yoyaku1:
				case $.id.btn_yoyaku2:
				case $.id.btn_sei:
					// 転送先情報
					index = 3;
					childurl = href[index];
					sendMode = 1;
					var txt_sel_shncd		= $.getJSONValue(that.jsonHidden, $.id.txt_sel_shncd);		// 検索商品コード
					var txt_sel_shnkn		= $.getJSONValue(that.jsonHidden, $.id.txt_sel_shnkn);		// 検索商品名
					var txt_shncd			= $.getInputboxValue($('#'+$.id_inp.txt_shncd));			// 入力商品コード

					// オブジェクト作成
					$.setJSONObject(sendJSON, $.id.txt_sel_shncd, txt_sel_shncd, txt_sel_shncd);
					$.setJSONObject(sendJSON, $.id.txt_sel_shnkn, txt_sel_shnkn, txt_sel_shnkn);
					$.setJSONObject(sendJSON, $.id_inp.txt_shncd, txt_shncd, txt_shncd);
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
		setTorihiki:function(reportno) {
			$.ajax({
				url: $.reg.easy,
				type: 'POST',
				async: false,
				data: {
					"page"	: reportno,
					"obj"	: $.id.SelTorihiki,
					"sel"	: (new Date()).getTime(),
					"target": $.id.SelTorihiki,
					"action": $.id.action_init,
					"json"	: "",
				},
				success: function(json){
					var data = JSON.parse(json);
					if(data.rows[0]['VALUE'] != '-1' && data.rows[0]['VALUE'] != '0'){
						$("#"+$.id_inp.txt_ssircd).textbox("setValue", data.rows[0]['VALUE']);
						$.setInputBoxDisable($('#'+$.id.btn_sir)).hide();
					}
				}
			});
		}
	} });
})(jQuery);