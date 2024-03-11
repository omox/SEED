/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_Reportx002',			// （必須）レポートオプションの確認
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
		reportYobiInfo: function(){			// （必須）レポートメニューに登録された予備情報＝帳票をどういった表示にしたいかの情報
			return $('#reportYobi1').val();
		},
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

			// 処理日付取得
			$.getsetInputboxData(reportno, $.id.txt_shoridt, [{}], $.id.action_init);

			var isUpdateReport = true;
			// 商品コード
			$.setInputbox(that, reportno, $.id.txt_sel_shncd, isUpdateReport);
			// 商品名
			$.setInputbox(that, reportno, $.id.txt_sel_shnkn, isUpdateReport);
			// リードタイムパターン
			$.setMeisyoCombo(that, reportno, $.id.sel_readtmptn, isUpdateReport);
			// 税率区分
			$.setMeisyoCombo(that, reportno, $.id.sel_zeirtkbn, isUpdateReport);
			// 旧税率区分
			$.setMeisyoCombo(that, reportno, $.id.sel_zeirtkbn_old, isUpdateReport);

			// ラジオボタン
			that.setRadioAreakbn(that, $.id.rad_areakbn, $.id.value_gpkbn_shina);
			that.setRadioAreakbn(that, $.id.rad_areakbn, $.id.value_gpkbn_baika);
			that.setRadioAreakbn(that, $.id.rad_areakbn, $.id.value_gpkbn_sir);
			that.setRadioAreakbn(that, $.id.rad_areakbn, $.id.value_gpkbn_tbmn);

			// チェックボックス
			$('#'+that.focusRootId).find(':checkbox').each(function(){
				$.setCheckboxInit2(that.jsonHidden, $(this).attr("id"), that);
			});

			// グループ分類名
			that.setGrpkn(that, reportno, $.id_inp.sel_grpkn);
			// 売価グループ総売価
			$.setInputbox(that, reportno,$.id.txt_bg_soubaika, isUpdateReport);
			// 売価グループ値入
			$.setInputbox(that, reportno,$.id.txt_bg_neire, isUpdateReport);

			// 名称マスタ参照系
			var meisyoSelect = Object.getOwnPropertyNames($.id_mei);
			for ( var sel in meisyoSelect ) {
				if($('#'+$.id_mei[meisyoSelect[sel]]).length > 0){
					$.setMeisyoCombo(that, reportno, $.id_mei[meisyoSelect[sel]], isUpdateReport);
				}
			}
			$.setMeisyoCombo(that, reportno, $.id_mei.kbn152+"_r", isUpdateReport);			// 選択リスト(デリカワッペン区分_レギュラ)
			$.setMeisyoCombo(that, reportno, $.id_mei.kbn152+"_h", isUpdateReport);			// 選択リスト(デリカワッペン区分_販促)

			// 入力テキストボックス系
			var inputbox = Object.getOwnPropertyNames($.id_inp);
			var notTargetId = [$.id_inp.sel_grpkn];
			for ( var sel in inputbox ) {
				if($('#'+$.id_inp[inputbox[sel]]).length > 0 && notTargetId.indexOf(inputbox[sel]) === -1){
					$.setInputbox(that, reportno, $.id_inp[inputbox[sel]], isUpdateReport);
				}
			}

			// 検索実行
			that.onChangeReport = true;

			// Load処理回避
			//$.tryChangeURL(null);

			// 初期化終了
			this.initializes =! this.initializes;

			// ログ出力
			$.log(that.timeData, 'initialize:');
		},
		searched_initialize: function (reportno){	// 検索結果を受けての初期化
			var that = this;

			if(!that.judgeRepType.ref){
				// 編集可能データグリッドの共通処理設定
				// 編集エディターの機能拡張（非表示で参照用のオブジェクトが準備してある前提、参照オブジェクトはグリッドのフィールド名を元に判断）
				$.extendDatagridEditor();
			}

			// ***個別データグリッド設定
			// ソースコード
			that.setSrccdGrid(that, reportno, $.id.grd_srccd);
			// 店グループ（品揃え）
			that.setTengpGrid(that, reportno, $.id.grd_tengp+$.id.value_gpkbn_shina, $.id.value_gpkbn_shina);
			// 店グループ（売価）
			that.setTengpGrid(that, reportno, $.id.grd_tengp+$.id.value_gpkbn_baika, $.id.value_gpkbn_baika);
			// 店グループ（仕入）
			that.setTengpGrid(that, reportno, $.id.grd_tengp+$.id.value_gpkbn_sir, $.id.value_gpkbn_sir);
			// 店グループ（店別異部門）
			that.setTengpGrid(that, reportno, $.id.grd_tengp+$.id.value_gpkbn_tbmn, $.id.value_gpkbn_tbmn);
			// アレルギー
			that.setEasyGrid(that, reportno, $.id.grd_allergy , true);
			if(that.judgeRepType.ref){
				// 添加物
				that.setEasyGrid(that, reportno, $.id.grd_tenkabutsu);
				// グループ分類名
				that.setEasyGrid(that, reportno, $.id.grd_group);
			}else{
				// 添加物
				that.setEditableGrid(that, reportno, $.id.grd_tenkabutsu);
				// グループ分類名
				that.setEditableGrid(that, reportno, $.id.grd_group);
			}

			// サブウインドウの初期化
			setTimeout(function(){
				// 参照系サブウインドウ
				$.win001.init(that);	// メーカー
				$.win002.init(that);	// 仕入先
				$.win003.init(that);	// 店グループ
				$.win004.init(that);	// 配送パターン
				// 更新系サブウインドウ
				var canUpdate = $("#"+$.id.btn_upd).is(":visible") && !$("#"+$.id.btn_upd).linkbutton('options').disabled;
				$.winIT031.init(that, canUpdate);	// ソースコード
			},50);

			// チェックボックスの設定
			$.initCheckboxCss($("#"+that.focusRootId));
			// キーイベントの設定
			$.initKeyEvent(that);

			// ログ出力
			$.log(that.timeData, 'initialize:');
		},
		judgeRepType: {
			sei				: false,	// 正
			sei_new 		: false,	// 正 -新規
			sei_upd 		: false,	// 正 -更新
			sei_ref 		: false,	// 正 -参照

			yyk				: false,	// 予
			yyk1			: false,	// 予1
			yyk1_new		: false,	// 予1-新規
			yyk1_upd		: false,	// 予1-更新
			yyk1_ref		: false,	// 予1-参照
			yyk2			: false,	// 予2
			yyk2_new		: false,	// 予2-新規
			yyk2_upd		: false,	// 予2-更新
			yyk2_ref		: false,	// 予2-参照

			err				: false,	// エラー
			err_sei			: false,	// エラー正
			err_sei_new		: false,	// エラー正 -新規
			err_sei_upd		: false,	// エラー正 -更新
			err_yyk1		: false,	// エラー予1
			err_yyk1_new	: false,	// エラー予1-新規
			err_yyk1_upd	: false,	// エラー予1-更新

			ref				: false		// 参照
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
			// 新規：新規・新規コピー・選択コピーボタン押下時
			if(that.judgeRepType.sei_new){
				hidobjids = hidobjids.concat([$.id.btn_yoyaku1,$.id.btn_yoyaku2,$.id.btn_del]);

				$("#disp_record_info").hide();

				$.initReportInfo("IT002", "商品マスタ　新規", "新規");

			// 変更：検索・変更・正ボタン押下時
			}else if(that.judgeRepType.sei_upd){
				if(that.reportYobiInfo()==='1'){
					$.initReportInfo("IT003", "商品マスタ　参照", "参照");
				}else{
					$.initReportInfo("IT003", "商品マスタ　変更", "変更");
				}
			}else if(that.judgeRepType.sei_ref){
				$.initReportInfo("IT003", "商品マスタ　参照", "参照");

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
					$.initReportInfo("IT004", "商品マスタ　参照", "参照");
				}else{
					$.initReportInfo("IT004", "商品マスタ　変更", "変更");
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
				$.initReportInfo("IT028", "商品マスタ　CSV取り込み　エラー修正", "エラー修正");
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
				hidobjids = hidobjids.concat([$.id.btn_maker,$.id.btn_sir,$.id.btn_hsptn,$.id.btn_tengp+1,$.id.btn_tengp+2,$.id.btn_tengp+4
								,$.id.btn_sir+$.id.grd_tengp+1,$.id.btn_hsptn+$.id.grd_tengp+1]);
			}

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
				// クリア
				$.setInputboxValue($('#'+$.id.txt_sel_shncd), '');

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
			}
		},
		initCondition: function (){	// 条件初期値セット
			var that = this;
			// 初期化項目
		},
		clear:function(){
			// 隠し情報初期化
			$($.id.hiddenChangedIdx).val("");						// 変更行Index
		},
		validation: function (){	// （必須）批准
			var that = this;
			var rt = true;
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
		// 引数に渡した処理を同期的に実行する。ただし、引数の関数に戻り値がない場合はrejectが実行されてしまう。
		// 呼び出した本関数の後に.then(function(){第二実行関数})を記述することで、本関数の引数で渡した関数が非同期処理であっても
		// 処理終了後に第二実行関数が実行される。
		// IEで使用不可
		syncExecution:function(func){
			return new Promise(function(resolve, reject){
				if(func){
					resolve(func);
				}else{
					reject('NotFunction');
				}
			});
		},
		getInputboxNumVal: function (id){
			var val = $.getInputboxValue($("#"+id)).replace(",", "").replace("%", "");
			return val.length===0 ? 0 : val*1;
		},
		getNumVal: function (val){
			return val.length===0 ? 0 : val*1;
		},
		getGridData: function (txt_shncd, txt_yoyakudt, target){
			var that = this;

			var data = {};

			// 基本情報
			if(target===undefined || target==="grd_data"){
				var emptyToZeroKey = ["F2", "F3"];
				var targetData = [{}];
				$('#'+that.focusRootId).find('[col^=F]').each(function(){
					var col = $(this).attr('col');
					var val = $.getInputboxValue($(this));
					if($(this).hasClass('easyui-combobox') && val==="-1"){ val = "";}			// 先頭空白"-1"の場合からに変換
					if(emptyToZeroKey.indexOf(col)!==-1 && $.isEmptyVal(val)){ val = "0"; }		// 空白の場合0変換項目
					targetData[0][col] = val;
				});
				data["grd_data"] = targetData;
			}

			// 補足情報(テーブルに登録しない情報などを保持)
			if(target===undefined || target==="grd_data_other"){
				var targetData = [{}];
				targetData[0]["KETAKBN"] = $.getInputboxValue($('#'+$.id_mei.kbn143));	// 桁指定
				targetData[0]["SHNCD_NEW"] = that.data_other["SHNCD_NEW"];		// 新規商品コード
				targetData[0]["MAKERCD_NEW"]=that.data_other["MAKERCD_NEW"];	// 新規メーカーコード
				targetData[0]["URICD_NEW"] = that.data_other["URICD_NEW"];		// 新規販売コード
				data["grd_data_other"] = targetData;
			}

			// 子テーブル用情報
			if($.isEmptyVal(txt_yoyakudt)){
				txt_yoyakudt = "0";
			}

			// ソースコード取得
			if(target===undefined || target===$.id.grd_srccd){
				var targetRowsSrccd =[];
				var rowsSrccd = $('#'+$.id.grd_srccd).datagrid('getRows');
				var idx = 1;
				for (var i=0; i<rowsSrccd.length; i++){
					if(!$.isEmptyVal(rowsSrccd[i]["SRCCD"])){
						var kbn = "-1";
						if (!$.isEmptyVal(rowsSrccd[i]["SOURCEKBN"])) {
							kbn = rowsSrccd[i]["SOURCEKBN"].split("-")[0];
						}
						if(kbn==="-1"){ kbn = "";}
						var rowData = {
							F1 : ""+txt_shncd,						// 1	商品コード	SHNCD
							F2 : ""+rowsSrccd[i]["SRCCD"],			// 2	ソースコード	SRCCD
							F3 : txt_yoyakudt,						// 3	マスタ変更予定日	YOYAKUDT
							F4 : ""+rowsSrccd[i]["SEQNO"],			// 4	入力順番	SEQNO
							F5 : ""+kbn,							// 5	ソース区分	SOURCEKBN
							F10: ""+$.getParserDt(rowsSrccd[i]["YUKO_STDT"]),	// 10	有効開始日	YUKO_STDT
							F11: ""+$.getParserDt(rowsSrccd[i]["YUKO_EDDT"]),	// 11	有効終了日	YUKO_EDDT
							RNO : i								// 行番号(チェック用に保持)
						};
						targetRowsSrccd.push(rowData);
						idx++;
					}
				}
				data[$.id.grd_srccd] = targetRowsSrccd;
			}
			if(target===undefined || target===$.id.grd_srccd+"_win"){
				var targetRowsSrccd =[];
				var rowsSrccd = $('[id^='+$.id.grd_srccd+"_win]").datagrid('getRows');
				var idx = 1;
				for (var i=0; i<rowsSrccd.length; i++){
					if(!$.isEmptyVal(rowsSrccd[i]["SRCCD"])&&$.isEmptyVal(rowsSrccd[i]["DEL"], true)){
						var kbn = "-1";
						if (!$.isEmptyVal(rowsSrccd[i]["SOURCEKBN"])) {
							kbn = rowsSrccd[i]["SOURCEKBN"].split("-")[0];
						}
						if(kbn==="-1"){ kbn = "";}
						var rowData = {
							F1 : ""+txt_shncd,						// 1	商品コード	SHNCD
							F2 : ""+rowsSrccd[i]["SRCCD"],			// 2	ソースコード	SRCCD
							F3 : txt_yoyakudt,						// 3	マスタ変更予定日	YOYAKUDT
							F4 : ""+rowsSrccd[i]["SEQNO"],			// 4	入力順番	SEQNO
							F5 : ""+kbn,							// 5	ソース区分	SOURCEKBN
							F10: ""+$.getParserDt(rowsSrccd[i]["YUKO_STDT"]),	// 10	有効開始日	YUKO_STDT
							F11: ""+$.getParserDt(rowsSrccd[i]["YUKO_EDDT"]),	// 11	有効終了日	YUKO_EDDT
							RNO : i								// 行番号(チェック用に保持)
						};
						targetRowsSrccd.push(rowData);
						idx++;
					}
				}
				data[$.id.grd_srccd+"_win"] = targetRowsSrccd;
			}

			// 品揃えグループ
			var gpkbn = $.id.value_gpkbn_shina;
			if(target===undefined || target===$.id.grd_tengp+gpkbn){
				var targetRowsTengp3= [];
				var rowsTengp3= $('#'+$.id.grd_tengp+gpkbn).datagrid('getRows');
				var areakbn = $("input[name="+$.id.rad_areakbn+gpkbn+"]:checked").val();
				for (var i=0; i<rowsTengp3.length; i++){
					if(!$.isEmptyVal(rowsTengp3[i]["TENGPCD"])){
						var kbn = rowsTengp3[i]["ATSUKKBN"].split("-")[0];
						if(kbn==="-1"){ kbn = "";}
						var rowData = {
							F1 : txt_shncd,						// 1	商品コード	SHNCD
							F2 : rowsTengp3[i]["TENGPCD"],		// 2	店グループ	TENGPCD
							F3 : txt_yoyakudt,					// 3	マスタ変更予定日	YOYAKUDT
							F4 : areakbn,						// 4	エリア区分	AREAKBN
							F5 : kbn,							// 5	扱い区分	ATSUKKBN
							X1 : rowsTengp3[i]["TENGPKN"]		// 6	店グループ名(チェック用に保持)
							,RNO : i								// 行番号(チェック用に保持)
						};
						targetRowsTengp3.push(rowData);
					}
				}
				data[$.id.grd_tengp+gpkbn] = targetRowsTengp3;
			}

			// 売価グループ
			gpkbn = $.id.value_gpkbn_baika;
			// レギュラー取扱フラグのチェックがない場合、この売価コントロール部分は設定不可。
			if(target===undefined || target===$.id.grd_tengp+gpkbn){
				var targetRowsTengp2 = [];
				var rowsTengp2= $('#'+$.id.grd_tengp+gpkbn).datagrid('getRows');
				var areakbn = $("input[name="+$.id.rad_areakbn+gpkbn+"]:checked").val();
				for (var i=0; i<rowsTengp2.length; i++){
					if(!$.isEmptyVal(rowsTengp2[i]["TENGPCD"])){
						var rowData = {
							F1 : txt_shncd,						// 1	商品コード	SHNCD
							F2 : rowsTengp2[i]["TENGPCD"],		// 2	店グループ	TENGPCD
							F3 : txt_yoyakudt,					// 3	マスタ変更予定日	YOYAKUDT
							F4 : areakbn,						// 4	エリア区分	AREAKBN
							F5 : rowsTengp2[i]["GENKAAM"],		// 5	原価	GENKAAM
							F6 : rowsTengp2[i]["BAIKAAM"],		// 6	売価	BAIKAAM
							F7 : rowsTengp2[i]["IRISU"],		// 7	店入数	IRISU
							X1 : rowsTengp2[i]["TENGPKN"]		// 		店グループ名(チェック用に保持)
							,RNO : i								// 行番号(チェック用に保持)
						};
						targetRowsTengp2.push(rowData);
					}
				}
				data[$.id.grd_tengp+gpkbn] = targetRowsTengp2;
			}

			// 仕入グループ
			gpkbn = $.id.value_gpkbn_sir;
			if(target===undefined || target===$.id.grd_tengp+gpkbn){
				var targetRowsTengp1=  [];
				var rowsTengp1= $('#'+$.id.grd_tengp+gpkbn).datagrid('getRows');
				var areakbn = $("input[name="+$.id.rad_areakbn+gpkbn+"]:checked").val();
				for (var i=0; i<rowsTengp1.length; i++){
					if(!$.isEmptyVal(rowsTengp1[i]["TENGPCD"])){
						var rowData = {
							F1 : txt_shncd,						// 1	商品コード	SHNCD
							F2 : rowsTengp1[i]["TENGPCD"],		// 2	店グループ	TENGPCD
							F3 : txt_yoyakudt,					// 3	マスタ変更予定日	YOYAKUDT
							F4 : areakbn,						// 4	エリア区分	AREAKBN
							F5 : rowsTengp1[i]["SSIRCD"],		// 5	仕入先コード	SIRCD
							F6 : rowsTengp1[i]["HSPTN"],		// 6	配送パターン	HSPTN
							X1 : rowsTengp1[i]["TENGPKN"],		// 7	店グループ名(チェック用に保持)
							X2 : rowsTengp1[i]["SIRKN"],		// 8	仕入先コード名(チェック用に保持)
							X3 : rowsTengp1[i]["HSPTNKN"]		// 9	配送パターン名(チェック用に保持)
							,RNO : i								// 行番号(チェック用に保持)
						};
						targetRowsTengp1.push(rowData);
					}
				}
				data[$.id.grd_tengp+gpkbn] = targetRowsTengp1;
			}

			// 店別異部門
			var gpkbn = $.id.value_gpkbn_tbmn;
			if(target===undefined || target===$.id.grd_tengp+gpkbn){
				var targetRowsTengp4= [];
				var rowsTengp4= $('#'+$.id.grd_tengp+gpkbn).datagrid('getRows');
				var areakbn = $("input[name="+$.id.rad_areakbn+gpkbn+"]:checked").val();
				for (var i=0; i<rowsTengp4.length; i++){
					if(!$.isEmptyVal(rowsTengp4[i]["TENSHNCD"])||!$.isEmptyVal(rowsTengp4[i]["TENGPCD"])){
						var rowData = {
							F1 : txt_shncd,						// 1	商品コード				SHNCD
							F2 : rowsTengp4[i]["TENSHNCD"],		// 2	店別異部門商品コード	TENSHNCD
							F3 : rowsTengp4[i]["TENGPCD"],		// 3	店グループ				TENGPCD
							F4 : txt_yoyakudt,					// 4	マスタ変更予定日		YOYAKUDT
							F5 : areakbn,						// 5	エリア区分				AREAKBN
							F10: rowsTengp4[i]["SRCCD"],		// 10	ソースコード			SRCCD
							X1 : rowsTengp4[i]["TENGPKN"]		// 6	店グループ名(チェック用に保持)
							,RNO : i							// 行番号(チェック用に保持)
						};
						targetRowsTengp4.push(rowData);
					}
				}
				data[$.id.grd_tengp+gpkbn] = targetRowsTengp4;
			}

			// 添加物
			if(target===undefined || target===$.id.grd_tenkabutsu){
				var targetRowsTenkabutsu = [];
				var rowsTenkabutsu =$('#'+$.id.grd_tenkabutsu).datagrid('getRows');
				for (var i=0; i<rowsTenkabutsu.length; i++){
					if(!$.isEmptyVal(rowsTenkabutsu[i]["TENKABCD"])&&rowsTenkabutsu[i]["TENKABCD"]!=="-1"){
						var rowData = {
							F1 : txt_shncd,										// 商品コード
							F2 : '1',											// 添加物区分 : 01―その他画面の添加物部分
							F3 : rowsTenkabutsu[i]["TENKABCD"].split("-")[0],	// 添加物コード : 名称マスタの名称コード
							F4 : txt_yoyakudt									// マスタ変更予定日=添付資料（MD03111002）を参照。
							,RNO : i											// 行番号(チェック用に保持)
						};
						targetRowsTenkabutsu.push(rowData);
					}
				}
				data[$.id.grd_tenkabutsu] = targetRowsTenkabutsu;
			}

			// 添加物-アレルギー
			if(target===undefined || target===$.id.grd_allergy){
				var targetRowsAllergy = [];
				var rowsAllergy =$('#'+$.id.grd_allergy).datagrid('getChecked');
				for (var i=0; i<rowsAllergy.length; i++){
					var rowData = {
						F1 : txt_shncd,									// 商品コード
						F2 : '2',										// 添加物区分 : 02―その他画面のアレルギー部分
						F3 : rowsAllergy[i]["VALUE"].split("-")[0],		// 添加物コード : 名称マスタの名称コード
						F4 : txt_yoyakudt								// マスタ変更予定日=添付資料（MD03111002）を参照。
						,RNO : i								// 行番号(チェック用に保持)
					};
					targetRowsAllergy.push(rowData);
				}
				data[$.id.grd_allergy] = targetRowsAllergy;
			}


			// グループ分類
			if(target===undefined || target===$.id.grd_group){
				var targetRowsGroup = [];
				var rowsGroup =$('#'+$.id.grd_group).datagrid('getRows');
				var idx = 0;
				for (var i=0; i<rowsGroup.length; i++){
					if(!$.isEmptyVal(rowsGroup[i]["GRPKN"])){
						var rowData = {
							F1 : txt_shncd,									// SHNCD	: 商品コード
							F3 : txt_yoyakudt,								// YOYAKUDT	: マスタ変更予定日
							F4 : ''+idx,									// SEQNO	: 入力順
							F9 : rowsGroup[i]["GRPKN"]						// GRPKN	: グループ分類名
							,RNO : i								// 行番号(チェック用に保持)
						};
						targetRowsGroup.push(rowData);
					}
				}
				data[$.id.grd_group] = targetRowsGroup;
			}

			// 自動発注区分
			if(target===undefined || target==$.id.grd_sub+"_winIT032"){
				var targetRowsAhskb =[];
				var rowsAhskb = $('#'+$.id.grd_sub+"_winIT032").datagrid('getRows');
				var idx = 0;
				for (var i=0; i<rowsAhskb.length; i++){
					if(!$.isEmptyVal(rowsAhskb[i]["TENCD"])){
						var rowData = {
							F1 : ""+txt_shncd,						// 1	商品コード	SHNCD
							F2 : ""+rowsAhskb[i]["TENCD"],			// 2	店コード	TENCD
							F3 : txt_yoyakudt,						// 3	マスタ変更予定日	YOYAKUDT
							F4 : ""+rowsAhskb[i]["AHSKB"]			// 4	自動発注区分	AHSKB
							,RNO : i								// 行番号(チェック用に保持)
						};
						targetRowsAhskb.push(rowData);
						idx++;
					}
				}
				data[$.id.grd_sub+"_winIT032"] = targetRowsAhskb;
			}

			return data;
		},
		// 商品マスタと予約マスタの両方に同一商品がある場合の予約変更方法
		getShnDataMD03111701: function (editData, refDatas){
			var that = this;
			var datas = [];

			var ruleOutItems = ["F1", "F2", "F3", "F110", "F111", "F112", "F113", "F114", "F115"];

			// 正-予約1-予約2
			if(refDatas.length === 3){
				var editFlg1 = false,  editFlg2 = false;
				var newData1 = refDatas[1];
				var newData2 = refDatas[2];
				var cols = Object.getOwnPropertyNames(refDatas[0]);
				for ( var idx in cols ) {
					var col = cols[idx];
					var val0 = refDatas[0][col];	// 正
					var val1 = refDatas[1][col];	// 予約1
					var val2 = refDatas[2][col];	// 予約2

					// 同一商品コードじゃない場合は実施しない
					if(col==="F1" && val0 !== val1 && val1 !== val2){
						return datas;
					}

					// 同一項目更新対象外、Daoで固定値設定する項目を除外
					if(ruleOutItems.indexOf(col)!==-1){
						continue;
					}

					//変更があった場合
					if(isFinite(val0)){
						if(val0*1 !== editData[col]*1){
							if(val0*1===val1*1){
								newData1[col] = editData[col];
								editFlg1 = true;
							}
							if(val0*1===val1*1 && val1*1===val2*1){
								newData2[col] = editData[col];
								editFlg1 = true;
							}
						}
					}else{
						if(val0 !== editData[col]){
							if(val0===val1){
								newData1[col] = editData[col];
								editFlg1 = true;
							}
							if(val0===val1 && val1===val2){
								newData2[col] = editData[col];
								editFlg1 = true;
							}
						}
					}
				}
				if(editFlg1 = true){
					datas.push(newData1);
				}
				if(editFlg2 = true){
					datas.push(newData2);
				}
			}

			// 正-予約1, 予約1-予約2
			if(refDatas.length === 2){
				var editFlg1 = false;
				var newData1 = refDatas[1];			// 予約2
				var cols = Object.getOwnPropertyNames(refDatas[0]);
				for ( var idx in cols ) {
					var col = cols[idx];
					var val0 = refDatas[0][col];	// 予約1
					var val1 = refDatas[1][col];	// 予約2

					// 同一商品コードじゃない場合は実施しない
					if(col==="F1" && val0 !== val1){
						return datas;
					}

					// 同一項目更新対象外、Daoで固定値設定する項目を除外
					if(ruleOutItems.indexOf(col)!==-1){
						continue;
					}

					//変更があった場合
					if(isFinite(val0)){
						if(val0*1 !== editData[col]*1){
							if(val0*1===val1*1){
								newData1[col] = editData[col];
								editFlg1 = true;
							}
						}
					}else{
						if(val0 !== editData[col]){
							if(val0===val1){
								newData1[col] = editData[col];
								editFlg1 = true;
							}
						}
					}
				}
				if(editFlg1 = true){
					datas.push(newData1);
				}
			}

			return datas;
		},

		setGridData: function (data, target){
			var that = this;

			// 基本データ
			if(target===undefined || target==="grd_data"){
				that.grd_data =  data["grd_data"];
			}

			// 補足データ
			if(target===undefined || target==="grd_data_other"){
				that.grd_data_other =  data["grd_data_other"];
			}

			// ソースコード
			if(target===undefined || target===$.id.grd_srccd){
				that.grd_srccd_data =  data[$.id.grd_srccd];
			}
			if(target===undefined || target===$.id.grd_srccd+"_win"){
				that.grd_srccd_win_data =  data[$.id.grd_srccd+"_win"];
			}

			// 品揃えグループ
			if(target===undefined || target===$.id.grd_tengp+$.id.value_gpkbn_shina){
				that.grd_tengp3_data =  data[$.id.grd_tengp+$.id.value_gpkbn_shina];
			}

			// 売価グループ
			if(target===undefined || target===$.id.grd_tengp+$.id.value_gpkbn_baika){
				that.grd_tengp2_data =  data[$.id.grd_tengp+$.id.value_gpkbn_baika];
			}

			// 仕入グループ
			if(target===undefined || target===$.id.grd_tengp+$.id.value_gpkbn_sir){
				that.grd_tengp1_data =  data[$.id.grd_tengp+$.id.value_gpkbn_sir];
			}

			// 店別異部門
			if(target===undefined || target===$.id.grd_tengp+$.id.value_gpkbn_tbmn){
				that.grd_tengp4_data =  data[$.id.grd_tengp+$.id.value_gpkbn_tbmn];
			}

			// 添加物
			if(target===undefined || target===$.id.grd_tenkabutsu){
				that.grd_tenkabutsu_data = data[$.id.grd_tenkabutsu];
			}

			// 添加物-アレルギー
			if(target===undefined || target===$.id.grd_allergy){
				that.grd_allergy_data = data[$.id.grd_allergy];
			}

			// グループ分類
			if(target===undefined || target===$.id.grd_group){
				that.grd_group_data = data[$.id.grd_group];
			}

			// 自動発注区分
			if(target===undefined || target===$.id.grd_sub+"_winIT032"){
				that.grd_ahs_win_data =  data[$.id.grd_sub+"_winIT032"];
			}

			return true;
		},
		updValidation: function (id){	// （必須）批准
			var that = this;

			//JAVA側検証用
//			var txt_shncd = $.getInputboxValue($('#'+$.id_inp.txt_shncd));
//			var txt_yoyakudt = $.getInputboxValue($('#'+$.id_inp.txt_yoyakudt), "0");
//			var gridData = that.getGridData(txt_shncd, txt_yoyakudt);
//			that.setGridData(gridData);
//			return true;

			return that.updValidationIn(id);
		},
		updValidationIn: function (id){	// （必須）批准
			var that = this;
			var errMsg= "";

			// 新規(正)：新規・新規コピー・選択コピーボタン押下時
			var isNew = that.judgeRepType.sei_new || that.judgeRepType.err_sei_new;
			// 変更(正)：検索・変更・正ボタン押下時
			var isChange = that.judgeRepType.sei_upd || that.judgeRepType.err_sei_upd;

			// 新規(予1)：予1ボタン押下時、かつ予約数0
			var isNewY1 = that.judgeRepType.yyk1_new || that.judgeRepType.err_yyk1_new;
			// 変更(予1)：予1ボタン押下時、かつ予約数1以上
			var isChangeY1 = that.judgeRepType.yyk1_upd || that.judgeRepType.err_yyk1_upd;

			// 新規(予2)：予2ボタン押下時、かつ予約数1
			var isNewY2 = that.judgeRepType.yyk2_new;
			// 変更(予2)：予2ボタン押下時、かつ予約数2以上
			var isChangeY2 = that.judgeRepType.yyk2_upd;

			var txt_shncd = $.getInputboxValue($('#'+$.id_inp.txt_shncd));
			var txt_yoyakudt = $.getInputboxValue($('#'+$.id_inp.txt_yoyakudt), "0");
			var txt_tenbaikadt = $.getInputboxValue($('#'+$.id_inp.txt_tenbaikadt), "0");

			var shoridt  = $('#'+$.id.txt_shoridt).val();					// 処理日付
			var sysdate = shoridt;							// 比較用処理日付

			// 現在の画面情報を変数に格納
			var gridData01 = that.getGridData(txt_shncd, txt_yoyakudt, "grd_data");
			var inpdata = gridData01["grd_data"][0];				// 画面入力項目

			// 新規(正) 1.1　必須入力項目チェックを行う。
			// 変更(正) 1.1　必須入力項目チェックを行う。
			// CSV修正  1.1　必須入力項目チェックを行う。
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform).form('validate');
			if(!rt){
				$.addErrState(that, $('.validatebox-invalid').eq(0), false);
				return rt;
			}
			$.endEditingDatagrid(that);	// grid系end

			// CSV修正(予約) 1.2　商品_予約ﾃｰﾌﾞﾙに同じ商品ｺｰﾄﾞで異なるﾏｽﾀ変更予定日、店売価実施日の組み合わせを持つﾚｺｰﾄﾞがあれば、ｴﾗｰ。
			if(that.judgeRepType.err_yyk1_upd){
				if(that.yoyakuData.length > 1){
					$.showMessage('EX1010');
					return false;
				}
				if(!(that.yoyakuData[0]["F2"]===txt_yoyakudt && that.yoyakuData[0]["F3"]===txt_tenbaikadt)){
					$.showMessage('EX1010');
					return false;
				}
			}

			var txt_bmncd = inpdata["F12"];
			var txt_daicd = inpdata["F13"];
			var txt_chucd = inpdata["F14"];
			var txt_shocd = inpdata["F15"];
			var txt_sshocd= inpdata["F16"];

			var txt_shncd_new = txt_shncd;
			if(isNew){

				// 新規(正) 1.2　添付資料（MD03100901）の商品コード付番規則によって、入力された商品コードを処理し、8桁の商品コード番号を取得する。
				// F1-商品コード：入力内容の桁数と桁指定項目の選択内容と合わない場合、エラー。
				if ($.reportOption.name==='Out_Reportx002') {
					var kbn143 = $.getInputboxValue($('#'+$.id_mei.kbn143));
					if((kbn143==="0"&&txt_shncd_new.length!==0)
					 ||(kbn143==="1"&&txt_shncd_new.length!==8)
					 ||(kbn143==="2"&&txt_shncd_new.length!==4)){
						$.showMessage('E11154', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_shncd), true)});
						return false;
					}
				}

				// コード整合性チェック：チェックデジット算出コード取得
				if(txt_shncd_new.length===8){
					var shncd_row2 = $.getSelectListData(that.name, $.id.action_check, $.id_inp.txt_shncd, [{KEY:"CHK_DGT",value:txt_shncd_new}]);
					// コードがおかしい場合は、エラー情報が返ってくる
					if(shncd_row2[0]["ID"]){
						$.showMessage(shncd_row2[0]["ID"], undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_shncd), true)});
						return false;
					}
				}

				// 添付資料（MD03100901）の商品コード付番機能
				// 処理内で、付番、登録、入力チェックを行っている→エラーで中断する場合は取り消し処理を行うこと
				// 新規(正) 1.3　取得できなかったらエラー
				// 新規(正) 1.3　取得された商品コードが商品マスタテーブルに存在する場合、エラー。
//				var param = that.getInputboxParams(that, $.id_inp.txt_shncd, txt_shncd);
//				param[0]["KEY"] =  "MD03100901_ADD";
//				var shncd_row = $.getSelectListData(that.name, $.id.action_change, $.id_inp.txt_shncd, param);
//				// 新規コードがない場合は、エラー情報が返ってくる
//				if(shncd_row[0]["ID"]){
//					$.showMessage(shncd_row[0]["ID"], undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_shncd), true)});
//					return false;
//				}
//				txt_shncd_new = shncd_row[0]["VALUE"];

				that.data_other["SHNCD_NEW"] = txt_shncd_new;

				// 商品種類が"通常商品"かつＰＯＰ名称が空の場合、ＰＯＰ名称に商品名（漢字）の値をセットする
				if(inpdata["F29"]==="0" && inpdata["F23"].length === 0){
					inpdata["F23"] = inpdata["F21"];
					$.setInputboxValue($('#'+$.id_inp.txt_popkn),inpdata["F23"]);
				}
			}
			// 現在の画面情報を変数に格納
			var gridData = that.getGridData(txt_shncd_new, txt_yoyakudt);

			var txt_ssircd = inpdata["F31"];
			var kbn105 = inpdata["F29"];		// 商品種類

			// 新規(正) 1.4　入力内容相関チェックを行う（入出力データ仕様のチェック内容を参照）。
			// 変更(正) 1.2　画面各項目間の入力内容相関チェックを行う（入出力データ仕様のチェック内容を参照）。

			// 親商品コード
			// ①商品コードと同じ場合、エラー。商品マスタに存在しない場合、エラー。
			var txt_parentcd = inpdata["F98"];
			if(txt_shncd_new != "" && txt_parentcd===txt_shncd_new){
				$.showMessage('E11102', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_parentcd), true)});
				return false;
			}
			// ②商品マスタに存在しない場合、エラー。
			if(!$.isEmptyVal(txt_parentcd, false)){
				var txt_parentcd_chk = $.getInputboxData(that.name, $.id.action_check,  $.id_inp.txt_shncd, [{KEY:"MST_CNT",value:txt_parentcd}]);
				if(txt_parentcd_chk === "0"){
					$.showMessage('E11134', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_parentcd), true)});
					return false;
				}
			}

			// 標準-部門コード
			var param = {};
			param["KEY"] =  "SEL";
			param["BMNCD"] = txt_bmncd;			// 部門
			param["DAICD"] = txt_daicd;
			param["CHUCD"] = txt_chucd;
			param["SHOCD"] = txt_shocd;
			param["SSHOCD"]= "";
			var bumon_row = $.getSelectListData(that.name, $.id.action_check,  $.id_inp.txt_bmncd, [param]);
			// ①部門マスタに無い場合エラー
			
			if(bumon_row[0][1].length===0){
				$.showMessage('E11044', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_bmncd), true)});
				return false;
			}
			// ②頭2桁は商品コードと一致しないと、エラー。
			if ($.reportOption.name==='Out_Reportx002') {
				if(txt_shncd_new != "" && txt_shncd_new.substr(0, 2)*1 !== txt_bmncd*1){
					if(isNew){
						$.showMessage('E11162', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_bmncd), true)});
					}else{
						$.showMessage('E11205', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_bmncd), true)});
					}
					return false;
				}
			}
			// 標準-大分類コード：大分類マスタに無い場合エラー
			if(!$.isEmptyVal(txt_daicd)&&bumon_row[0][2].length===0){
				$.showMessage('E11135', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_daicd), true)});
				return false;
			}
			// 標準-中分類コード：中分類マスタに無い場合エラー
			if(!$.isEmptyVal(txt_chucd)&&bumon_row[0][3].length===0){
				$.showMessage('E11136', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_chucd), true)});
				return false;
			}
			// 標準-小分類コード：小分類マスタに無い場合エラー
			if(!$.isEmptyVal(txt_shocd)&&bumon_row[0][4].length===0){
				$.showMessage('E11137', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_shocd), true)});
				return false;
			}

			param = {};
			param["KEY"] =  "SEL";
			param["BMNCD"] = inpdata["F4"];
			param["DAICD"] = inpdata["F5"];
			param["CHUCD"] = inpdata["F6"];
			param["SHOCD"] = inpdata["F7"];
			param["SSHOCD"]= "";
			bumon_row = $.getSelectListData(that.name, $.id.action_check,  $.id_inp.txt_yot_bmncd, [param]);
			// 用途-部門コード：部門マスタに無い場合エラー
			if(!$.isEmptyVal(inpdata["F4"],true)&&bumon_row[0][1].length===0){
			$.showMessage('E11044', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_yot_bmncd), true)});
				return false;
			}

			// 部門に0以外のコードが入力された場合
			if (!$.isEmptyVal(inpdata["F4"],true)) {
				// 用途-大分類コード：大分類マスタに無い場合エラー
				if(!$.isEmptyVal(inpdata["F5"])&&bumon_row[0][2].length===0){
					$.showMessage('E11135', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_yot_daicd), true)});
					return false;
				}
				// 用途-中分類コード：中分類マスタに無い場合エラー
				if(!$.isEmptyVal(inpdata["F6"])&&bumon_row[0][3].length===0){
					$.showMessage('E11136', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_yot_chucd), true)});
					return false;
				}
				// 用途-小分類コード：小分類マスタに無い場合エラー
				if(!$.isEmptyVal(inpdata["F7"])&&bumon_row[0][4].length===0){
					$.showMessage('E11137', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_yot_shocd), true)});
					return false;
				}

			// 部門コードに0が入力された場合
			} else if (!$.isEmptyVal(inpdata["F4"])){
				// 用途-大分類コード：大分類マスタに無い場合エラー
				if(!$.isEmptyVal(inpdata["F5"],true)&&bumon_row[0][2].length===0){
					$.showMessage('E11135', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_yot_daicd), true)});
					return false;
				}
				// 用途-中分類コード：中分類マスタに無い場合エラー
				if(!$.isEmptyVal(inpdata["F6"],true)&&bumon_row[0][3].length===0){
					$.showMessage('E11136', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_yot_chucd), true)});
					return false;
				}
				// 用途-小分類コード：小分類マスタに無い場合エラー
				if(!$.isEmptyVal(inpdata["F7"],true)&&bumon_row[0][4].length===0){
					$.showMessage('E11137', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_yot_shocd), true)});
					return false;
				}

				// 全て0なら未入力に
				if ($.isEmptyVal(inpdata["F5"],true) &&
						$.isEmptyVal(inpdata["F6"],true) &&
						$.isEmptyVal(inpdata["F7"],true)) {
					$.setInputboxValue($('#'+$.id_inp.txt_yot_bmncd),'');
					$.setInputboxValue($('#'+$.id_inp.txt_yot_chucd),'');
					$.setInputboxValue($('#'+$.id_inp.txt_yot_shocd),'');
				}

			// 部門コード未入力は全て未入力に
			} else if ($.isEmptyVal(inpdata["F4"])) {
				$.setInputboxValue($('#'+$.id_inp.txt_yot_daicd),'');
				$.setInputboxValue($('#'+$.id_inp.txt_yot_daicd),'');
				$.setInputboxValue($('#'+$.id_inp.txt_yot_chucd),'');
				$.setInputboxValue($('#'+$.id_inp.txt_yot_shocd),'');
			}
			param = {};
			param["KEY"] =  "SEL";
			param["BMNCD"] = inpdata["F8"];
			param["DAICD"] = inpdata["F9"];
			param["CHUCD"] = inpdata["F10"];
			param["SHOCD"] = inpdata["F11"];
			param["SSHOCD"]= "";
			bumon_row = $.getSelectListData(that.name, $.id.action_check,  $.id_inp.txt_uri_bmncd, [param]);
			// 売場-部門コード：部門マスタに無い場合エラー
			if(!$.isEmptyVal(inpdata["F8"],true)&&bumon_row[0][1].length===0){
				$.showMessage('E11044', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_uri_bmncd), true)});
				return false;
			}

			// 部門に0以外のコードが入力された場合
			if (!$.isEmptyVal(inpdata["F8"],true)) {
				// 売場-大分類コード：大分類マスタに無い場合エラー
				if(!$.isEmptyVal(inpdata["F9"])&&bumon_row[0][2].length===0){
					$.showMessage('E11135', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_uri_daicd), true)});
					return false;
				}
				// 売場-中分類コード：中分類マスタに無い場合エラー
				if(!$.isEmptyVal(inpdata["F10"])&&bumon_row[0][3].length===0){
					$.showMessage('E11136', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_uri_chucd), true)});
					return false;
				}
				// 売場-小分類コード：小分類マスタに無い場合エラー
				if(!$.isEmptyVal(inpdata["F11"])&&bumon_row[0][4].length===0){
					$.showMessage('E11137', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_uri_shocd), true)});
					return false;
				}

			// 部門コードに0が入力された場合
			} else if (!$.isEmptyVal(inpdata["F8"])) {
				// 売場-大分類コード：大分類マスタに無い場合エラー
				if(!$.isEmptyVal(inpdata["F9"],true)&&bumon_row[0][2].length===0){
					$.showMessage('E11135', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_uri_daicd), true)});
					return false;
				}
				// 売場-中分類コード：中分類マスタに無い場合エラー
				if(!$.isEmptyVal(inpdata["F10"],true)&&bumon_row[0][3].length===0){
					$.showMessage('E11136', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_uri_chucd), true)});
					return false;
				}
				// 売場-小分類コード：小分類マスタに無い場合エラー
				if(!$.isEmptyVal(inpdata["F11"],true)&&bumon_row[0][4].length===0){
					$.showMessage('E11137', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_uri_shocd), true)});
					return false;
				}

				// 全て0なら未入力に
				if ($.isEmptyVal(inpdata["F9"],true) &&
						$.isEmptyVal(inpdata["F10"],true) &&
						$.isEmptyVal(inpdata["F11"],true)) {
					$.setInputboxValue($('#'+$.id_inp.txt_uri_bmncd),'');
					$.setInputboxValue($('#'+$.id_inp.txt_uri_daicd),'');
					$.setInputboxValue($('#'+$.id_inp.txt_uri_chucd),'');
					$.setInputboxValue($('#'+$.id_inp.txt_uri_shocd),'');
				}

			// 部門コード未入力は全て未入力に
			} else if ($.isEmptyVal(inpdata["F8"])) {
				$.setInputboxValue($('#'+$.id_inp.txt_uri_daicd),'');
				$.setInputboxValue($('#'+$.id_inp.txt_uri_chucd),'');
				$.setInputboxValue($('#'+$.id_inp.txt_uri_shocd),'');
			}

			// ユニットプライス:
			var txt_up_yoryosu = inpdata["F47"];
			var txt_up_tyoryosu = inpdata["F48"];
			var kbn113 = inpdata["F49"];		// ユニット単位
			var isAllInput = (!$.isEmptyVal(txt_up_yoryosu, true)&&!$.isEmptyVal(txt_up_tyoryosu, true)&&(!$.isEmptyVal(kbn113, false)));
			var isAllEmpty = ($.isEmptyVal(txt_up_yoryosu, true)&&$.isEmptyVal(txt_up_tyoryosu, true)&&($.isEmptyVal(kbn113, false)));
			if(!isAllInput && !isAllEmpty){
				$.showMessage('E11105', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_up_yoryosu), true)});
				return false;
			}

			// ソースコード系
			// ソースコード取得
			var srccds = [];
			var targetRowsSrccd = gridData[$.id.grd_srccd+"_win"];
			for (var i=0; i<targetRowsSrccd.length; i++){
				srccds.push(targetRowsSrccd[i]["F2"]);
			}

			// ソースコード重複チェック_CSV取込時
			if(that.judgeRepType.err){
				if(that.srcCodeValidation($.id.grd_srccd, targetRowsSrccd) == false){
					return false;
				};
			}

			// ①NON-PLUの場合にソースコードに入力がある場合はエラー。
			// ②商品種類6または部門コード02,09,15,04,05,06,20,23,43,08,12,13,26,27の場合にNON-PLUにして良い、それ以外は1を入力してはならない。→削除：共通の商品種類チェックを優先
			var kbn117 = inpdata["F57"];		// 定計区分
			if(kbn117==="1"){
				if(targetRowsSrccd.length > 0){
					$.showMessage('E11106', undefined, function(){$.addErrState(that, $('#'+$.id_mei.kbn117), true)});
					return false;
				}

//				var canNonPluBmn = [2,9,15,4,5,6,20,23,43,8,12,13,26,27];
//				if(kbn105!=="6"||canNonPluBmn.indexOf(txt_bmncd)===-1){
//					if(kbn105!=="6"){
//						$.showMessage('E11107', undefined, function(){$.addErrState(that, $('#'+$.id_mei.kbn117), true)});
//						return false;
//					}
//					if(canNonPluBmn.indexOf(txt_bmncd)===-1){
//						$.showMessage('E11108', undefined, function(){$.addErrState(that, $('#'+$.id_mei.kbn117), true)});
//						return false;
//					}
//				}
			}
			// ①ソース区分2(2行目)が1(JAN13) or 2(JAN8)の場合、ソース区分1(1行目)が3(EAN13), 4(EAN8), 5(UPC-A), 6(UPC-E)はエラー
			// ②ソースコードの1行目に入力がなく、2行目に設定があった場合は1行目に移す。
			var targetRowsMainSrccd = gridData[$.id.grd_srccd];
			if (targetRowsMainSrccd.length >= 2){
				if(targetRowsMainSrccd[0]["F2"].length !== 0 && targetRowsMainSrccd[1]["F2"].length !== 0){
					var kbn1 = targetRowsMainSrccd[0]["F5"].split("-")[0];
					var kbn2 = targetRowsMainSrccd[1]["F5"].split("-")[0];
					var errKbns = ["3", "4", "5", "6"];
					if((kbn2==="1"||kbn2==="2")&&errKbns.indexOf(kbn1)!==-1){
						$.showMessage('E11111', undefined, function(){$.addErrState(that, $('#'+$.id.btn_srccd), false)});
						return false;
					}
				}
			}

			// 店別異部門
			gpkbn = $.id.value_gpkbn_tbmn;
			var tengp4s = [], tengp4keys = [];
			var targetRowsTengp4 = gridData[$.id.grd_tengp+gpkbn];
			for (var i=0; i<targetRowsTengp4.length; i++){
				tengp4s.push(targetRowsTengp4[i]["F3"]);
				tengp4keys.push(targetRowsTengp4[i]["F2"]+"-"+targetRowsTengp4[i]["F3"]);
			}
			for (var i=0; i<targetRowsTengp4.length; i++){
				var tenshncd = targetRowsTengp4[i]["F2"];
				var tengpcd4 = targetRowsTengp4[i]["F3"];
				// 店グループに入力がある場合、仕入先コード、配送パターンは必須入力。
				isAllInput = !$.isEmptyVal(tenshncd, true)&&!$.isEmptyVal(tengpcd4, true);
				isAllEmpty = $.isEmptyVal(tenshncd, true)&&$.isEmptyVal(tengpcd4, true);
				if(!isAllInput && !isAllEmpty){
					$.showMessage("EX1047", ["商品コード、店グループ"], function(){$.addErrState(that, $("#"+$.id.grd_tengp+gpkbn), true, {NO:targetRowsTengp4[i]["RNO"], ID:$.id_inp.txt_tengpcd})});
					return false;
				}
				// 商品マスタに存在しない場合、エラー。
				var txt_tenshncd_chk = $.getInputboxData(that.name, $.id.action_check,  $.id_inp.txt_shncd, [{KEY:"MST_CNT",value:tenshncd}]);
				if(txt_tenshncd_chk === "0"){
					$.showMessage("E11098", undefined, function(){$.addErrState(that, $("#"+$.id.grd_tengp+gpkbn), true, {NO:targetRowsTengp4[i]["RNO"], ID:$.id_inp.txt_tenshncd})});
					return false;
				}
				// 商品店グループに存在しないコードはエラー
				if(targetRowsTengp4[i]["X1"]===""){
					$.showMessage("E11140", undefined, function(){$.addErrState(that, $("#"+$.id.grd_tengp+gpkbn), true, {NO:targetRowsTengp4[i]["RNO"], ID:$.id_inp.txt_tengpcd})});
					return false;
				}
				// 商品コードが主の商品コードと同じ場合エラー
				if(txt_shncd === tenshncd){
					$.showMessage("EX1047", ["基本情報と異なる商品コード"], function(){$.addErrState(that, $("#"+$.id.grd_tengp+gpkbn), true, {NO:targetRowsTengp4[i]["RNO"], ID:$.id_inp.txt_tenshncd})});
					return false;
				}
				// ソースコードが設定しているソースコードの中に無い場合エラー
				var srccd = targetRowsTengp4[i]["F10"];
				if(srccds.indexOf(srccd)===-1){
					$.showMessage("EX1047", ["ソースコードにあるJANコード"], function(){$.addErrState(that, $("#"+$.id.grd_tengp+gpkbn), true, {NO:targetRowsTengp4[i]["RNO"], ID:$.id_inp.txt_srccd})});
					return false;
				}
			}
			// 重複がある場合、エラー。
			var tengp4keys_ = tengp4keys.filter(function (element, index, self) { return self.indexOf(element)*1 === index; });
			if(tengp4keys.length !== tengp4keys_.length){
				$.showMessage('E11112', undefined, function(){$.addErrState(that, $('#'+$.id.grd_tengp+gpkbn), true, {NO:0, ID:$.id_inp.txt_tenshncd})});
				return false;
			}
			// 店グループを選択したら10番以上で登録しなければならない
			if($.getInputboxValue($("input[name="+$.id.rad_areakbn+gpkbn+"]")) === "1"){	// 店グループ
				tengp4s.sort(function(a,b){ return b-a; });
				if(tengp4s.length > 0 && tengp4s[0] < 10){
					$.showMessage('E11038', undefined, function(){$.addErrState(that, $('#'+$.id.grd_tengp+gpkbn), true, {NO:0, ID:$.id_inp.txt_tengpcd})});
					return false;
				}
			}

			// 品揃えグループ
			var gpkbn = $.id.value_gpkbn_shina;
			var tengp3s = [];
			var targetRowsTengp3 = gridData[$.id.grd_tengp+gpkbn];
			// 店グループ:商品店グループに存在しないコードはエラー。
			// 扱い区分:店グループに入力がある場合、扱い区分未入力はエラー
			for (var i=0; i<targetRowsTengp3.length; i++){
				tengp3s.push(targetRowsTengp3[i]["F2"]);
				// 商品店グループに存在しないコードはエラー
				if(targetRowsTengp3[i]["X1"]===""){
					$.showMessage('E11140');
					return false;
				}
				// 店グループに入力がある場合、扱い区分未入力はエラー
				if(targetRowsTengp3[i]["F5"]===""){
					// TODO:メッセージ不明
					$.showMessage('EX1001');
					return false;
				}

			}
			// 画面に同じ店グループがある場合、エラー。
			var tengp3s_ = tengp3s.filter(function (element, index, self) { return self.indexOf(element)*1 === index; });
			if(tengp3s.length !== tengp3s_.length){
				$.showMessage('E11112');
				return false;
			}
			// 店グループを選択したら10番以上で登録しなければならない.
			if($.getInputboxValue($("input[name="+$.id.rad_areakbn+gpkbn+"]")) === "1"){	// 店グループ
				tengp3s_.sort(function(a,b){ return b-a; });
				if(tengp3s_.length > 0 && tengp3s_[0] < 10){
					$.showMessage('E11038');
					return false;
				}
			}

			// 取扱期間
			//①取扱開始日0000/00/00　取扱終了日YYYY/MM/DD　NG
			//②取扱開始日YYYY/MM/DD　取扱終了日0000/00/00　NG
			//③取扱開始日YYYY/MM/DD >=　取扱終了日YYYY/MM/DD　NG"
			var txt_atsuk_stdt = inpdata["F17"];
			var txt_atsuk_eddt = inpdata["F18"];
			isAllInput = (!$.isEmptyVal(txt_atsuk_stdt, true)&&!$.isEmptyVal(txt_atsuk_eddt, true));
			isAllEmpty = ($.isEmptyVal(txt_atsuk_stdt, true)&&$.isEmptyVal(txt_atsuk_eddt, true));
			if(!isAllInput && !isAllEmpty){
				$.showMessage('E11114', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_atsuk_stdt), true)});
				return false;
			}
			if(isAllInput && (txt_atsuk_stdt >= txt_atsuk_eddt)){
				$.showMessage('E11115', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_atsuk_stdt), true)});
				return false;
			}

			// 商品種類に基づくチェック：MD03121701
			//  部門:選択可部門
			var isAbleBmncd = function(typ, bmncd){
				var msg = "";
				// E11143	商品種類と部門のチェックエラーです。                                          　　　　　	 	0	 	E
				if(typ==="2"){					//
					//部門:選択可部門
					//02,09,15,04,05,06,20,23,43部門
					var ableBmn = [2,9,15,4,5,6,20,23,43,70,71,72,73,74,75,76,77,78,79];
					if(ableBmn.indexOf(bmncd)===-1){
						msg = "E11143";
					}
				}else if(typ==="3"){
					if(bmncd!=="88"){
						msg = "E11143";
					}
				}else if(bmncd==="88"){
					msg = "E11143";
				}
				return msg;
			}
			errMsg = isAbleBmncd(kbn105, txt_bmncd)
			if(errMsg.length > 0){
				$.showMessage(errMsg, undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_bmncd), true)});
				return false;
			}
			//  原売価値入ﾁｪｯｸ TODO:商品種類と値入チェックはこれでいいのか
			var isAbleNeire = function(typ, val){
				var msg = "";
				if(typ==="0"||typ==="1"){
					if(val >= 98){					//
						msg = 'E11120';
					}
				}
				return msg;
			}
			//  ﾚｷﾞｭﾗｰ原売価:値入ﾁｪｯｸ
			errMsg = isAbleNeire(kbn105, that.getInputboxNumVal($.id.txt_rg_neire));
			if(errMsg.length > 0){
				$.showMessage('E11144', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_rg_genkaam), true)});
				return false;
			}
			//  販促原売価:値入ﾁｪｯｸ
			errMsg = isAbleNeire(kbn105, that.getInputboxNumVal($.id.txt_hs_neire));
			if(errMsg.length > 0){
				$.showMessage('E11145', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_hs_genkaam), true)});
				return false;
			}

			//  定計区分:”１”を許可するチェック
			//  種類：0		許可部門 02,09,15,04,05,06,20,23,43,08,12,13,26,27部門
			//  種類：0以外	許可部門 全部門
			var isAbleTeikei = function(typ, bmn, val){
				var msg = "";
				if(val==="1"){
					if(typ==="0"){
						//部門:選択可部門
						//02,09,15,04,05,06,20,23,43,08,12,13,26,27部門
						var ableBmn = [2,9,15,4,5,6,20,23,43,8,12,13,26,27,70,71,72,73,74,75,76,77,78,79];
						if(ableBmn.indexOf(bmn)===-1){
							msg = "E11148";
						}
					}
				}
				return msg;
			}
			errMsg = isAbleTeikei(kbn105, txt_bmncd, kbn117);
			if(errMsg.length > 0){
				$.showMessage(errMsg, undefined, function(){$.addErrState(that, $('#'+$.id_mei.kbn117), true)});
				return false;
			}

			//  定貫区分:”０”を許可するチェック
			//  種類：0		許可部門 02,09,15,04,05,06,20,23,43,08,12,13,26,27部門
			//  種類：0以外	許可部門 全部門
			var isAbleTeikan = function(typ, bmn, val){
				var msg = "";
				if(val==="0"){
					if(typ==="0"){
						//部門:選択可部門
						//02,09,15,04,05,06,20,23,43,08,12,13,26,27部門
						var ableBmn = [2,9,15,4,5,6,20,23,43,8,12,13,26,27,70,71,72,73,74,75,76,77,78,79];
						if(ableBmn.indexOf(bmn)===-1){
							msg = "E11149";
						}
					}

					//E11117:商品種類は6以外の場合に定貫不定貫区分は0に入力できません。                                        　
					//E11118:部門コード02,09,15,04,05,06,20,23,43,08,12,13,26,27以外の場合に定貫不定貫区分は0に入力できません。　

				}
				return msg;
			}
			var kbn121 = inpdata["F71"];	// 定貫不定貫区分
			errMsg = isAbleTeikan(kbn105, txt_bmncd, kbn121);
			if(errMsg.length > 0){
				$.showMessage(errMsg, undefined, function(){$.addErrState(that, $('#'+$.id_mei.kbn121), true)});
				return false;
			}
			//POP名称:省略可不可
			//  種類：0		不可
			//  種類：0以外	可
			if(kbn105==="0" && inpdata["F23"].length === 0){
				$.showMessage('E11150', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_popkn), true)});
				return false;
			}
			//標準分類（大分類以下）:標準分類省略可不可
			if([4,6,5].indexOf(kbn105)===-1 && ($.isEmptyVal(txt_daicd)||$.isEmptyVal(txt_chucd)||$.isEmptyVal(txt_shocd))){
				$.showMessage('E11151', undefined, function(){$.addErrState(that, $('#'+$.id_mei.kbn105), true)});
				return false;
			}

			// PC区分
			// ①1を指定して良いのは部門コードが04,05,06,43の場合のみ。
			// ②標準仕入先コードから仕入先マスタのデフォルト加工指示を参照し、'1'の場合、PC区分'1'が可能です。
			var kbn102 = inpdata["F26"];
			if(kbn102==="1" && [4,5,6,43,20,23,70,71,72,73,74,75,76,77,78,79].indexOf(txt_bmncd)===-1){
				$.showMessage('E11116', undefined, function(){$.addErrState(that, $('#'+$.id_mei.kbn102), true)});
				return false;
			}
			if([4,5,6,43].indexOf(txt_bmncd)!==-1){
				var mstsir_rows = $.getSelectListData(that.name, $.id.action_check, $.id_inp.txt_ssircd, [{KEY:"SEL",value:txt_ssircd}]);;

				var err = false;

				if (mstsir_rows.length !==1) {
					err = true;
				} else {
					var dfKakoSjKbn = mstsir_rows[0]["DF_KAKOSJKBN"];

					if (dfKakoSjKbn!=='0' && dfKakoSjKbn!=='1') {
						err = true;
					} else if (kbn102!=='0' && kbn102!=='1') {
						err = true;
					} else if (dfKakoSjKbn!==kbn102) {
						err = true;
					}
				}

				if(err){
					$.showMessage('E11326', undefined, function(){$.addErrState(that, $('#'+$.id_mei.kbn102), true)});
					return false;
				}
			}

			// レギュラー
			isAllInput = (!$.isEmptyVal(inpdata["F34"], true)&&!$.isEmptyVal(inpdata["F35"], true)&&!$.isEmptyVal(inpdata["F36"], true));
			isAllEmpty = ($.isEmptyVal(inpdata["F34"], true)&&$.isEmptyVal(inpdata["F35"], true)&&$.isEmptyVal(inpdata["F36"], true));
			if((!isAllInput && !isAllEmpty) || ($('#'+"chk_rg_atsukflg").is(':checked') && isAllEmpty)){
				$.showMessage('E11119', undefined, function(){$.addErrState(that, $('#'+"chk_rg_atsukflg"), true)});
				return false;
			}
			// 販促
			isAllInput = (!$.isEmptyVal(inpdata["F40"], true)&&!$.isEmptyVal(inpdata["F41"], true)&&!$.isEmptyVal(inpdata["F42"], true));
			isAllEmpty = ($.isEmptyVal(inpdata["F40"], true)&&$.isEmptyVal(inpdata["F41"], true)&&$.isEmptyVal(inpdata["F42"], true));
			if((!isAllInput && !isAllEmpty) || ($('#'+"chk_hs_atsukflg").is(':checked') && isAllEmpty)){
				$.showMessage('E11119', undefined, function(){$.addErrState(that, $('#'+"chk_hs_atsukflg"), true)});
				return false;
			}

			// 税区分系
			var kbn120 = inpdata["F66"];
			// 税率変更日: 税区分が3の場合、設定不可。　TODO:メッセージは２、３
			if((kbn120==="2"||kbn120==="3") && !$.isEmptyVal(inpdata["F69"], true)){
				$.showMessage('E11180', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_zeirthenkodt), true)});
				return false;
			}
			// 税率区分: 税区分が0,1の場合、税率区分に登録がないとエラー。税区分が2,3の場合、税率区分は設定不可。
			var sel_zeirtkbn = inpdata["F67"];
			if((kbn120==="0"||kbn120==="1") && sel_zeirtkbn===""){
				$.showMessage('E11152', undefined, function(){$.addErrState(that, $('#'+$.id.sel_zeirtkbn), true)});
				return false;
			}else if((kbn120==="2"||kbn120==="3") && sel_zeirtkbn!==""){
				$.showMessage('E11153', undefined, function(){$.addErrState(that, $('#'+$.id.sel_zeirtkbn), true)});
				return false;
			}
			// 旧税率区分: 税区分が3の場合、設定不可。2.非課税は選択不可　TODO:メッセージは税率区分と一緒
			var sel_zeirtkbn_old = inpdata["F68"];
			if((kbn120==="0"||kbn120==="1") && sel_zeirtkbn_old===""){
				$.showMessage('E11185', undefined, function(){$.addErrState(that, $('#'+$.id.sel_zeirtkbn_old), true)});
				return false;
			}else if((kbn120==="2"||kbn120==="3") && sel_zeirtkbn_old!==""){
				$.showMessage('E11181', undefined, function(){$.addErrState(that, $('#'+$.id.sel_zeirtkbn_old), true)});
				return false;
			}

			// ITFコード
			// コード整合性チェック：チェックデジット算出コード取得
			var txt_itfcd = inpdata["F120"];
			if(!$.isEmptyVal(txt_itfcd, false)){
				var itfcd_row = $.getSelectListData(that.name, $.id.action_check, $.id_inp.txt_itfcd, [{KEY:"CHK_DGT",value:txt_itfcd}]);
				// 新規コードがない場合は、エラー情報が返ってくる
				if(itfcd_row[0]["ID"]){
					$.showMessage(itfcd_row[0]["ID"], ["ITFコード"], function(){$.addErrState(that, $('#'+$.id_inp.txt_itfcd), true)});
					return false;
				}
			}

			// 売価グループ
			// レギュラー取扱フラグのチェックがない場合、この売価コントロール部分は設定不可。
			gpkbn = $.id.value_gpkbn_baika;
			var tengp2s = [];
			var targetRowsTengp2 = gridData[$.id.grd_tengp+gpkbn];
			for (var i=0; i<targetRowsTengp2.length; i++){
				tengp2s.push(targetRowsTengp2[i]["F2"]);
			}
			var areakbn = $.getInputboxValue($("input[name="+$.id.rad_areakbn+gpkbn+"]"));
			for (var i=0; i<targetRowsTengp2.length; i++){
				// 店グループに入力がある場合、原価、売価、店入数のすべてが未入力だとエラー。
				isAllEmpty = $.isEmptyVal(targetRowsTengp2[i]["F5"], true)&&$.isEmptyVal(targetRowsTengp2[i]["F6"], true)&&$.isEmptyVal(targetRowsTengp2[i]["F7"], true);
				if(isAllEmpty){
					$.showMessage('E11122', undefined, function(){$.addErrState(that, $('#'+$.id.grd_tengp+gpkbn), true, {NO:targetRowsTengp2[i]["RNO"], ID:$.id_inp.txt_tengpcd})});
					return false;
				}
				// 商品店グループに存在しないコードはエラー
				if(targetRowsTengp2[i]["X1"]===""){
					$.showMessage('E11140', undefined, function(){$.addErrState(that, $('#'+$.id.grd_tengp+gpkbn), true, {NO:targetRowsTengp2[i]["RNO"], ID:$.id_inp.txt_tengpcd})});
					return false;
				}
				// 店グループを選択したら10番以上で登録しなければならない
				if(areakbn === "1"){	// 店グループ
					if(!$.isEmptyVal(targetRowsTengp2[i]["F2"], true)&& targetRowsTengp2[i]["F2"] < 10){
						$.showMessage('E11038', undefined, function(){$.addErrState(that, $('#'+$.id.grd_tengp+gpkbn), true, {NO:targetRowsTengp2[i]["RNO"], ID:$.id_inp.txt_tengpcd})});
						return false;
					}
				}
			}
			// 画面に同じ店グループがある場合、エラー。
			var tengp2s_ = tengp2s.filter(function (element, index, self) { return self.indexOf(element)*1 === index; });
			if(tengp2s.length !== tengp2s_.length){
				$.showMessage('E11112', undefined, function(){$.addErrState(that, $('#'+$.id.grd_tengp+gpkbn), true, {NO:0, ID:$.id_inp.txt_tengpcd})});
				return false;
			}

			// 仕入グループ
			gpkbn = $.id.value_gpkbn_sir;
			var tengp1s = [], tengp1keys = [], sircdList = [];
			var targetRowsTengp1 = gridData[$.id.grd_tengp+gpkbn];
			for (var i=0; i<targetRowsTengp1.length; i++){
				tengp1s.push(targetRowsTengp1[i]["F2"]);	// 店グループ
				tengp1keys.push(targetRowsTengp1[i]["F5"]+"-"+targetRowsTengp1[i]["F6"]);	// 仕入先コード + 配送パターン
				sircdList.push(targetRowsTengp1[i]["F5"]);	// 仕入先コード
			}
			for (var i=0; i<targetRowsTengp1.length; i++){
				// 店グループに入力がある場合、仕入先コード、配送パターンは必須入力。
				isAllInput = !$.isEmptyVal(targetRowsTengp1[i]["F5"], true)&&!$.isEmptyVal(targetRowsTengp1[i]["F6"], true);
				if(!$.isEmptyVal(targetRowsTengp1[i]["F2"], true) && !isAllInput){
					$.showMessage('E11123', undefined, function(){$.addErrState(that, $('#'+$.id.grd_tengp+gpkbn), true, {NO:targetRowsTengp1[i]["RNO"], ID:$.id_inp.txt_tengpcd})});
					return false;
				}
				// 商品店グループに存在しないコードはエラー
				if(targetRowsTengp1[i]["X1"]===""){
					$.showMessage('E11140', undefined, function(){$.addErrState(that, $('#'+$.id.grd_tengp+gpkbn), true, {NO:targetRowsTengp1[i]["RNO"], ID:$.id_inp.txt_tengpcd})});
					return false;
				}
			}
			// 画面に同じ店グループがある場合、エラー。
			var tengp1s_ = tengp1s.filter(function (element, index, self) { return self.indexOf(element)*1 === index; });
			if(tengp1s.length !== tengp1s_.length){
				$.showMessage('E11112', undefined, function(){$.addErrState(that, $('#'+$.id.grd_tengp+gpkbn), true, {NO:0, ID:$.id_inp.txt_tengpcd})});
				return false;
			}
			if(tengp1s_.length > 0){
				// 店グループで展開し、店コードが重複する場合はエラー。
				var param = {};
				param["KEY"] =  "CNT";
				param["value"] = tengp1s_.join(",");
				param["GPKBN"] = gpkbn;
				param["BMNCD"] = txt_bmncd;			// 部門
				param["AREAKBN"] = $.getInputboxValue($("input[name="+$.id.rad_areakbn+gpkbn+"]"));	// エリア区分
				var grd_tengp1_ten_cnt = $.getInputboxData(that.name, $.id.action_check,  $.id.grd_tengp+gpkbn, [param]);
				if(grd_tengp1_ten_cnt!==""&&grd_tengp1_ten_cnt!=="0"){
					$.showMessage('E11141', undefined, function(){$.addErrState(that, $('#'+$.id.grd_tengp+gpkbn), true, {NO:0, ID:$.id_inp.txt_tengpcd})});
					return false;
				}

				// 仕入先コードで仕入先マスタの存在チェック
				for (var i=0; i<sircdList.length; i++){
					var param = {};
					var sircd = sircdList[i]
					param["KEY"] =  "MST_CNT";
					param["value"] = sircd;
					var sircdLis_mst_cnt = $.getInputboxData(that.name, $.id.action_check,  $.id_inp.txt_sircd, [param]);
					if(sircdLis_mst_cnt===""||sircdLis_mst_cnt === "0"){
						$.showMessage('E11099', undefined, function(){$.addErrState(that, $('#'+$.id.grd_tengp+gpkbn), true, {NO:i, ID:$.id_inp.txt_ssircd})});
						return false;
					}
				}

				// 仕入先コード、配送パターンで配送パターン仕入先マスタの存在チェック
				var param = {};
				var tengp1keys_ = tengp1keys.filter(function (element, index, self) { return self.indexOf(element)*1 === index; });
				param["KEY"] =  "MST_CNT";
				param["value"] = tengp1keys_.join(",");
				var grd_tengp1_mst_cnt = $.getInputboxData(that.name, $.id.action_check,  "MSTHSPTNSIR", [param]);
				if(grd_tengp1_mst_cnt !== ""+tengp1keys_.length){
					$.showMessage('E11142', undefined, function(){$.addErrState(that, $('#'+$.id.grd_tengp+gpkbn), true, {NO:0, ID:$.id_inp.txt_tengpcd})});
					return false;
				}
			}
			// 店グループを選択したら10番以上で登録しなければならない
			if($.getInputboxValue($("input[name="+$.id.rad_areakbn+gpkbn+"]")) === "1"){	// 店グループ
				tengp1s_.sort(function(a,b){ return b-a; });
				if(tengp1s_.length > 0 && tengp1s_[0] < 10){
					$.showMessage('E11038', undefined, function(){$.addErrState(that, $('#'+$.id.grd_tengp+gpkbn), true, {NO:0, ID:$.id_inp.txt_tengpcd})});
					return false;
				}
			}

			// 仕入先コードで仕入先マスタの存在チェック
			if(!$.isEmptyVal(txt_ssircd, false)){
				var param = {};
				param["KEY"] =  "MST_CNT";
				param["value"] = txt_ssircd;
				var mst_cnt = $.getInputboxData(that.name, $.id.action_check,  $.id_inp.txt_sircd, [param]);
				if(mst_cnt === "0"){
					$.showMessage('E11099', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_ssircd), true)});
					return false;
				}
			}

			// 標準仕入先・配送パターン：仕入先コード、配送パターンで配送パターン仕入先マスタの存在チェック
			var txt_hsptn = inpdata["F32"];
			if(!$.isEmptyVal(txt_ssircd, false) && !$.isEmptyVal(txt_hsptn, false)){
				var param = {};
				param["KEY"] =  "MST_CNT";
				param["value"] = txt_ssircd + "-" + txt_hsptn;
				var mst_cnt = $.getInputboxData(that.name, $.id.action_check,  "MSTHSPTNSIR", [param]);
				if(mst_cnt === "0"){
					$.showMessage('E11142', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_ssircd), true)});
					return false;
				}
			}

			// リードタイムパターン

			// 必須入力チェック
			if(!inpdata["F107"] || inpdata["F107"] == ""){
				var bmnlist = [1, 3, 7, 8, 12, 14, 42, 44, 46, 47, 54]
				if(bmnlist.indexOf(Number(txt_bmncd))===-1){
					$.showMessage('E00001', undefined, function(){$.addErrState(that, $('#'+$.id.sel_readtmptn), true)});
					return false;
				}
			}

			var isAbleReadtmptn = function(data, bmn){
//				var ableRykBmn = [2,9,15,4,5,6,10,11,20,23,34,43,88,13,26,27];
				var typ = data["F107"];
//				if(ableRykBmn.indexOf(bmn)===-1 && ["10","20","21","30"].indexOf(typ)!==-1){
//					return 'EX1005';
//				}
				// CCRが本処理に対応できない為、要望によりMDMでは全ての部門の入力可とする。

				var msg = "";
				var chk_hat_frikbn = data["F104"];
				var chk_hat_satkbn = data["F105"];
				var chk_hat_sunkbn = data["F106"];
				if(typ==="10" && chk_hat_sunkbn!=="1"){
					msg = 'E11230';
				}else if(typ==="20" && chk_hat_satkbn!=="1"){
					msg = 'E11230';
				}else if(typ==="21" && chk_hat_sunkbn!=="1"){
					msg = 'E11230';
				}else if(typ==="30" && chk_hat_frikbn!=="1"){
					msg = 'E11230';
				}
				return msg;
			}
			errMsg = isAbleReadtmptn(inpdata, txt_bmncd);
			if(errMsg.length > 0){
				$.showMessage(errMsg, undefined, function(){$.addErrState(that, $('#'+$.id.sel_readtmptn), true)});
				return false;
			}

			// 締め回数
			//②2を設定して良いのは11,34部門である。
			if(inpdata["F108"]==="2" && [11,34].indexOf(txt_bmncd)===-1){
				$.showMessage('E11125', undefined, function(){$.addErrState(that, $('#'+$.id_mei.kbn134), true)});
				return false;
			}
			// 便
			//②2を設定して良いのは02,09,15,04,05,06,20,23,43,10,11,34部門である。
			if(inpdata["F99"]==="2" && [2,9,15,4,5,6,20,23,43,10,11,34].indexOf(txt_bmncd)===-1){
				$.showMessage('E11126', undefined, function(){$.addErrState(that, $('#'+$.id_mei.kbn132), true)});
				return false;
			}
			// 一括伝票フラグ
			// ②定貫不定貫区分が0の場合、1.センター経由の一括のみ許可する
			// ③PC区分が1の場合、1.センター経由の一括のみ許可する
			var txt_rg_idenflg = inpdata["F37"];
			if((kbn121==="0" && txt_rg_idenflg!=="1")||(kbn102==="1" && txt_rg_idenflg!=="1")){
				$.showMessage('E11127', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_rg_idenflg), true)});
				return false;
			}

			// 酒級:部門コード03,44以外は選択不可。
			var kbn129 = inpdata["F86"];
			if(!$.isEmptyVal(kbn129, true) && [3,44].indexOf(txt_bmncd)===-1){
				$.showMessage('E11129', undefined, function(){$.addErrState(that, $('#'+$.id_mei.kbn129), true)});
				return false;
			}
			// 度数：酒級に登録がある場合、必ず入力。スペース、0は未入力と処理する。　部門コード03,44以外は選択不可
			var txt_dosu = inpdata["F87"];
			if(!$.isEmptyVal(kbn129, true) && $.isEmptyVal(txt_dosu, true)){
				$.showMessage('E11130', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_dosu), true)});
				return false;
			}
			if(!$.isEmptyVal(txt_dosu, true) && [3,44].indexOf(txt_bmncd)===-1){
				$.showMessage('E11131', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_dosu), true)});
				return false;
			}

			// 入荷期限 0は未入力と処理する。入荷期限≧値引期限。0より大きい。
			// 値引期限 0以上を登録可能とする。
			var txt_ods_nyukasu = inpdata["F62"];		// 入荷期限
			var txt_ods_nebikisu= inpdata["F63"];		// 値引期限
			isAllEmpty = ($.isEmptyVal(txt_ods_nyukasu, true)&&$.isEmptyVal(txt_ods_nebikisu, false));
			// 入荷期限≧値引期限かチェック
			if(!isAllEmpty && txt_ods_nyukasu*1 < txt_ods_nebikisu*1){
				$.showMessage('E11132', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_ods_nyukasu), true)});
				return false;
			}

			// 添加物
			var tenkabcds = []
			var targetRowsTenkabutsu = gridData[$.id.grd_tenkabutsu];
			for (var i=0; i<targetRowsTenkabutsu.length; i++){
				tenkabcds.push(targetRowsTenkabutsu[i]["F3"].split("-")[0]);
			}
			var tenkabcds_ = tenkabcds.filter(function (element, index, self) { return self.indexOf(element)*1 === index; });
			if(tenkabcds_.length !== tenkabcds.length){
				$.showMessage('E11133');
				return false;
			}

			// グループ分類名
			var groups = []
			var targetRowsGroup = gridData[$.id.grd_group];
			for (var i=0; i<targetRowsGroup.length; i++){
				groups.push(targetRowsGroup[i]["F9"]);
			}
			var groups_ = groups.filter(function (element, index, self) { return self.indexOf(element)*1 === index; });
			if(groups_.length !== groups.length){
				$.showMessage('EX1012', undefined, function(){$.addErrState(that, $('#'+$.id.grd_group), true, {NO:0, ID:$.id_inp.sel_grpkn})});
				return false;
			}


			// 変更(正) 1.3　添付資料（MD03112701）の“テーブル削除の整理”と添付資料の（MD03111002）の“商品マスタ登録時　マスタ変更予定日、店売価実施日のチェック”を参照。
			// 新規(予) 1.4　添付資料（MD03111002）の“商品マスタ登録時　マスタ変更予定日、店売価実施日のチェック”を参照。
			// 変更(予) 1.3　添付資料（MD03112701）の“テーブル削除の整理”と添付資料の（MD03111002）の“商品マスタ登録時　マスタ変更予定日、店売価実施日のチェック”を参照。
			if(!isNew){

				// *　店売価実施日-４日＜ﾏｽﾀｰ変更日＜店売価実施日＆処理日付＜ﾏｽﾀｰ変更日＆処理日付＜店売価実施日
				// 送信日=店売価実施日-４日
				var senddate = $.convertDate(txt_tenbaikadt, -4);

				// ④予1.新規の場合
				if(isNewY1){
					// マスタ変更予定日:処理日付＜ﾏｽﾀｰ変更日　
					if(!(sysdate*1 < txt_yoyakudt*1)){
						$.showMessage('E11190', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_yoyakudt), true)});
						return false;
					}
					// 店売価実施日:処理日付＜店売価実施日
					if(!(sysdate*1 < txt_tenbaikadt*1)){
						$.showMessage('E11191', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_tenbaikadt), true)});
						return false;
					}
					// 店売価実施日-４日＜ﾏｽﾀｰ変更日＜店売価実施日
					if(!(senddate*1 < txt_yoyakudt*1 && txt_yoyakudt*1 < txt_tenbaikadt*1)){
						$.showMessage('E11192', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_yoyakudt), true)});
						return false;
					}

					// 店売価実施日:処理日付＜店売価実施日-４日
					if(!(sysdate*1 <= senddate*1)){
						$.showMessage('E11191', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_tenbaikadt), true)});
						return false;
					}
				// ⑦予2.新規の場合
				}else if(isNewY2){
					// マスタ変更予定日:処理日付＜ﾏｽﾀｰ変更日
					if(!(sysdate*1 < txt_yoyakudt*1)){
						$.showMessage('E11190', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_yoyakudt), true)});
						return false;
					}
					// 店売価実施日:処理日付＜店売価実施日
					if(!(sysdate*1 < txt_tenbaikadt*1)){
						$.showMessage('E11191', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_tenbaikadt), true)});
						return false;
					}
					// 店売価実施日-４日＜ﾏｽﾀｰ変更日＜店売価実施日
					if(!(senddate*1 < txt_yoyakudt*1 && txt_yoyakudt*1 < txt_tenbaikadt*1)){
						$.showMessage('E11192', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_yoyakudt), true)});
						return false;
					}

					// マスタ変更予定日:予約2のﾏｽﾀ変更日 >予約1のﾏｽﾀ変更日+4日
					var txt_yoyakudt_1 = "";		// 予約1のﾏｽﾀ変更日
					if(that.baseData.length > 0){
						txt_yoyakudt_1 = $.convertDate(that.baseData[0]["F2"], 4);
					}
					if(!(txt_yoyakudt*1 > txt_yoyakudt_1*1)){
						$.showMessage('E11193', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_yoyakudt), true)});
						return false;
					}

				// 予約の変更
				}else if( isChangeY1 || isChangeY2 ){
					var txt_yoyakudt_ = "";		// 検索実行時のﾏｽﾀ変更日
					var txt_tenbaikadt_ = "";	// 検索実行時の店売価実施日
					if(that.baseData.length > 0){
						txt_yoyakudt_ = $.getParserDt(that.baseData[0]["F2"]);
						txt_tenbaikadt_ = $.getParserDt(that.baseData[0]["F3"]);
					}
					if(txt_yoyakudt*1 !== txt_yoyakudt_*1){
						$.showMessage('E11195', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_yoyakudt), true)});
						return false;
					}
					if(txt_tenbaikadt*1 !== txt_tenbaikadt_*1){
						$.showMessage('E11196', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_yoyakudt), true)});
						return false;
					}
				}
			}


			// 新規(正) 1.5　商品登録数は商品登録限度数テーブルの登録限度数を超えた場合は、エラー。
			if(isNew){
				var txt_shncd_cnt = $.getInputboxData(that.name, $.id.action_check,  $.id_inp.txt_shncd, [{KEY:"CNT"}]);
				if(txt_shncd_cnt===undefined){
					$.showMessage('E11241', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_shncd), true)});
					return false;
				}
			}


			// 新規(正) 1.6　入力されたメーカーコードがメーカーマスタに存在しない、かつ、ソースコードから取得したメーカーコードとも異なる場合は、エラー。
			// 　　　　　　　空白の場合はソースコードから取得した値を登録する。設定されている場合は、その値を登録する。
			// 変更(正) 1.4　入力されたメーカーコードがメーカーマスタに存在しない、かつ、ソースコードから取得したメーカーコードとも異なる場合は、エラー。
			// 　　　　　　　また、ソースコードが入力されている場合に、メーカーコードが空白の場合、エラー。ただし、衣料使い回しフラグが１の場合はメーカーコードが空白の場合もエラーとしない。（7/14）
			// 新規(予) 1.5　入力されたメーカーコードがメーカーマスタに存在しない、かつ、ソースコードから取得したメーカーコードとも異なる場合は、エラー。
			// 　　　　　　　また、ソースコードが入力されている場合に、メーカーコードが空白の場合、エラー。
			var txt_makercd = inpdata["F72"];	// 入力されたメーカーコード
			var txt_makercd_new = txt_makercd;
			if(targetRowsSrccd.length > 0){
				// ソースコードからメーカーコードの取得
				// 添付資料（MD03112501）のメーカーコードの取得方法
				var value = targetRowsSrccd[0]["F2"];
				var kbn = targetRowsSrccd[0]["F5"];
				var txt_makercd_src = "";									// ソースコードから取得したメーカーコード
				if(value.length > 0 && kbn.length > 0 && kbn !== -1){
					var param = {};
					param["1"] = value;
					param["2"] = txt_bmncd;			// 部門
					param["3"] = kbn.split("-")[0];
					param["4"] =  "MD03112501";
					txt_makercd_src = $.getInputboxData(that.name, $.id.action_change,  $.id_inp.txt_makercd, [param]);
				}
				// メーカーコードが入力有の場合
				if(txt_makercd.length > 0){
					// メーカーコードの存在チェック
					var txt_makercd_chk = $.getInputboxData(that.name, $.id.action_check,  $.id_inp.txt_makercd, [{KEY:"MST_CNT",value:txt_makercd}]);

					// 入力されたメーカーコードがメーカーマスタに存在しない、かつ、ソースコードから取得したメーカーコードとも異なる場合
					if(txt_makercd_chk==="0" && txt_makercd_src!==txt_makercd){
						$.showMessage('E11320', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_makercd), true)});
						return false;
					}

				}
				// メーカーコードが空白の場合
				var chk_iryoreflg = inpdata["F109"];
				if(txt_makercd.length === 0){
					$.showMessage('E11321', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_makercd), true)});
					return false;
				}
				// ソースコードより取得したメーカーコードを設定。
				if(txt_makercd_src.length > 0){
					txt_makercd_new = txt_makercd_src;
				}

			}else{
				// ソースコード未入力時
				if(txt_makercd.length === 0){
					// メーカーコードに入力値がない場合、デフォルト値を設定する。
					var value = $.getInputboxText($('#'+$.id_inp.txt_bmncd)) + '00001';
					txt_makercd_new = value;
				}
			}

			// ①標準仕入先コードが「1」の場合のみ、「保温区分」、「デリカワッペン」、「取扱区分」は選択不可
			// ②標準仕入先コードが「1」以外の場合、「保温区分」、「デリカワッペン」、「取扱区分」は必須入力。
			var sel_k_honkb		 = inpdata["F116"];		// 保温区分
			var sel_k_wapnflg	 = inpdata["F117"];		// デリカワッペン
			var sel_k_torikb	 = inpdata["F119"];		// 取扱区分
			if(txt_bmncd == "20" || txt_bmncd == "23" || txt_bmncd == "31" || txt_bmncd == "70" || txt_bmncd == "73"){
				if(txt_ssircd.trim() == "1"){
					var errId = "";
					if(sel_k_honkb != ""){
						errId = $.id_mei.kbn151
					}else if(sel_k_wapnflg != ""){
						errId = $.id_mei.kbn152 + "_r"
					}else if(sel_k_torikb != ""){
						errId = $.id_mei.kbn153
					}

					if(errId != ""){
						$.showMessage('E30012', ["標準仕入先コードが「000001」以外の場合、「保温区分」、「デリカワッペン」、「取扱区分」は空欄"], function(){$.addErrState(that, $('#'+errId), true)});
						return false
					}
				}else{
					var errId = "";
					if(sel_k_honkb == ""){
						errId = $.id_mei.kbn151
					}else if(sel_k_wapnflg == ""){
						errId = $.id_mei.kbn152 + "_r"
					}else if(sel_k_torikb == ""){
						errId = $.id_mei.kbn153
					}

					if(errId != ""){
						$.showMessage('E30012', ["標準仕入先コードが「000001」以外の場合、「保温区分」、「デリカワッペン」、「取扱区分」"], function(){$.addErrState(that, $('#'+errId), true)});
						return false
					}
				}
			}

			// 変更(正) 1.5　部門マスタの評価方法区分が”２”（売価還元法）の部門で、「レギュラー本体売価」の変更があった場合、
			// ①衣料使いまわしフラグ”１”以外の場合、「マスタ更新可」ユーザー（「管理者」ユーザーを含む）だったら、登録ボタン押下時、「レギュラー売価が直接変更されました。本当に変更してよろしいですか？」のようなメッセージと「はい」「いいえ」のボタンをあわせて表示して更新を実行させる。
			//  「いいえ」なら、キャンセル。「はい」なら更新を実施。カーソルのデフォルトは「いいえ」に当てておく。
			//   ※エラー条件の優先度は、他のチェックを終えてから最後に追加。
			// ②．衣料使いまわし区分＝１の時、
			//   全チェックがＯＫだった場合、登録日、変更日を本日日付にする
			that.updConfirmMsg = "";
			if(isChange){					// 変更(正)
				if($.getInputboxValue($('#'+$.id.txt_hyokakbn)) ==="2"
				&& inpdata["F35"] !== that.baseData[0]["F35"]){
					// マスタ更新可ユーザーの制御は、メニューの制御で利用不可にするので不要
					if(chk_iryoreflg!=="1"){
						that.updConfirmMsg = "W20037";
					}else{
						gridData["grd_data"][0]["F114"] = login_dt;
						gridData["grd_data"][0]["F115"] = login_dt;
					}
				}
			}

			// 取得商品コードを設定
			gridData["grd_data"][0]["F1"] = txt_shncd_new;
			// 取得メーカーコードを設定
			gridData["grd_data"][0]["F72"] = txt_makercd_new;

			// 入力情報を変数に格納
			if (rt == true) that.setGridData(gridData);

//			// 入力エラーなしの場合に検索条件を格納
//			if (rt == true) that.jsonString = that.jsonTemp.slice(0);
//
//			// 入力チェック用の配列をクリア
//			that.jsonTemp = [];

			return rt;
		},
		// CSVエラー修正時には、トランからの読み込みサブ画面を起動せずにエラー値が入力された状態で更新が可能な為、
		// 親画面のvalidationでもチェックを行う必要がある。
		srcCodeValidation: function (id, dataRows){	// （必須）批准
			var that = this;
			var errMsg= "";
			// EasyUI のフォームメソッド 'validate' 実施

			// ソースコード系
			// ソースコード取得
			var srccds = [],seqnos = [];

			var gridid = id;
			var targetRowsSrccd =  dataRows;
			var csv_updkbn = $('#'+$.id.txt_csv_updkbn).val()
			// ソースコード１、２取得
			var row1 = null, row2=null;
			for (var i=0; i<targetRowsSrccd.length; i++){
				var txt_srccd = targetRowsSrccd[i]["F2"];				// 2	ソースコード
				if(!$.isEmptyVal(txt_srccd, false)){
					if(csv_updkbn == $.id.value_csvupdkbn_new){	// csv_updkbn = "A"
						// 新規登録時にのみマスタ重複チェックを行う
						var chk_cnt = $.getInputboxData(that.name, $.id.action_check,$.id_inp.txt_srccd, [{KEY:"MST_CNT",SRCCD:txt_srccd}]);
						if(chk_cnt!==""&&chk_cnt!=="0"){
							$.showMessage('E11139');
							return false;
						}
					}

					var kbn = targetRowsSrccd[i]["F5"].split("-")[0].trim();	// 5	ソース区分
					if($.isEmptyVal(kbn, false)){
						$.showMessage('EX1047', ["ソース区分"]);
						return false;
					}

					var seqno = targetRowsSrccd[i]["F4"];				// 4	入力順番
					if(["","1","2","9"].indexOf(seqno)===-1){
						$.showMessage('EX1051', ["ソースコードの順位は、"]);
						return false;
					}

					seqno = seqno*1;
					if((seqno === 1||seqno === 2) && seqnos.indexOf(seqno)!==-1){
						$.showMessage('EX1051', ["ソースコードの順位は、"]);
						return false;
					}
					// 有効期間チェック
					var txt_yuko_stdt = targetRowsSrccd[i]["F10"];		// 10	有効開始日
					var txt_yuko_eddt = targetRowsSrccd[i]["F11"];		// 11	有効終了日
					if(seqno === 9){
						if($.isEmptyVal(txt_yuko_stdt, true)){
							$.showMessage('EX1050', ["ソースコード"]);
							return false;
						}
					}else{
						if($.isEmptyVal(txt_yuko_stdt, true)){
							$.showMessage('EX1049', ["ソースコード"]);
							return false;
						}
						if($.isEmptyVal(txt_yuko_eddt, true)){
							$.showMessage('EX1049', ["ソースコード"]);
							return false;
						}
					}
					// 日付妥当性
					if(!$.isEmptyVal(txt_yuko_stdt, true)&&!$.isEmptyVal(txt_yuko_eddt, true)&&txt_yuko_stdt > txt_yuko_eddt){
						$.showMessage('E11020', ["ソースコードの"]);
						return false;
					}
					// 重複チェック
					if(srccds.indexOf(txt_srccd)!==-1){
						$.showMessage('E11109');
						return false;
					}
					// ソース1,2整合性
					if(seqno === 1){ row1 = targetRowsSrccd[i]; }
					if(seqno === 2){ row2 = targetRowsSrccd[i]; }
					if(row1!==null && row2!==null){
						// ①ソース区分2(2行目)が1(JAN13) or 2(JAN8)の場合、ソース区分1(1行目)が3(EAN13), 4(EAN8), 5(UPC-A), 6(UPC-E)はエラー
						var kbn1 = row1["F5"].split("-")[0];				// 5	ソース区分
						var kbn2 = row2["F5"].split("-")[0];				// 5	ソース区分
						var errKbns = ["3", "4", "5", "6"];
						if((kbn2==="1"||kbn2==="2")&&errKbns.indexOf(kbn1)!==-1){
							$.showMessage('E11111');
							return false;
						}
					}
					// コード整合性チェック：チェックデジット算出コード取得
					var srccd_row = $.getSelectListData(that.name, $.id.action_check, $.id_inp.txt_srccd, [{KEY:"CHK_DGT",value:txt_srccd, SOURCEKBN:kbn}]);
					// ソースコードに問題がある場合は、エラー情報が返ってくる（E11165,E11167,E11168,E11169,E11171,E11172,E11224）
					if(srccd_row[0]["ID"]){
						$.showMessage(srccd_row[0]["ID"], ["ソースコード"]);
						return false;
					}
					srccds.push(txt_srccd);
					if(seqno === 1||seqno === 2){
						seqnos.push(seqno);
					}
				}
			}
			// ソース登録があるにもかかわらず1指定がない場合エラーとする
			if(srccds.length > 0 && seqnos.indexOf(1)===-1){
				$.showMessage('E11110');
				return false;
			}
			return true;
		},
		updConfirm: function(func){	// validation OK時 の update処理
			var that = this;
			var msgId = 'W00001';
			if(that.updConfirmMsg!==""){
				msgId = that.updConfirmMsg;
			}
			$.showMessage(msgId, undefined, func);
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
					t:				(new Date()).getTime()
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
		delValidation: function (){	// （必須）批准
			var that = this;
			var rt = true;

			// 変更(正)：検索・変更・正ボタン押下時
			var isChange = that.judgeRepType.sei_upd || that.judgeRepType.err_sei_upd;
			// 変更(予1)：予1ボタン押下時、かつ予約数1以上
			var isChangeY1 = that.judgeRepType.yyk1_upd || that.judgeRepType.err_yyk1_upd;
			// 変更(予2)：予2ボタン押下時、かつ予約数2以上
			var isChangeY2 = that.judgeRepType.yyk2_upd;

			var txt_shncd = $.getInputboxValue($('#'+$.id_inp.txt_shncd));
			var txt_yoyakudt = $.getInputboxValue($('#'+$.id_inp.txt_yoyakudt), "0");
			var txt_tenbaikadt = $.getInputboxValue($('#'+$.id_inp.txt_tenbaikadt), "0");

			// 現在の画面情報を変数に格納
			var gridData = that.getGridData(txt_shncd, txt_yoyakudt);
			var inpdata = gridData["grd_data"][0];					// 画面入力項目

			var login_dt = parent.$('#login_dt').text().replace(/\//g, "");	// 処理日付
			var sysdate = login_dt;											// 比較用処理日付


			// 変更(正) 1.3　添付資料（MD03112701）の“テーブル削除の整理”と添付資料の（MD03111002）の“商品マスタ登録時　マスタ変更予定日、店売価実施日のチェック”を参照。
			// 変更(予) 1.3　添付資料（MD03112701）の“テーブル削除の整理”と添付資料の（MD03111002）の“商品マスタ登録時　マスタ変更予定日、店売価実施日のチェック”を参照。
			// 前提：CSVエラー修正の場合、エラー情報を削除するので常に削除可
			// 正 .変更
			if(that.judgeRepType.sei_upd){
				// 予約1がある場合
				if(that.yoyakuData.length > 0) {
					// E11179	予約に登録がある場合は削除できません。                                                              	 	0	 	E
					$.showMessage('E11179');
					return false;
				}
			// 予1.変更
			}else if(that.judgeRepType.yyk1_upd){
				// 予約2がある場合
				if(that.yoyakuData.length > 1) {
					// E11231	予約2に登録がある場合は削除できません。 	 	0	 	E
					$.showMessage('E11231');
					return false;
				}
			}

			// 予1.変更、予2.変更共通
			if(that.judgeRepType.yyk1_upd||that.judgeRepType.yyk2_upd){
				// 処理日付＝＜送信日（店売価実施日-4日）　であれば可能
				var senddate = $.convertDate(txt_tenbaikadt, -4);
				if(!(sysdate*1 <= senddate*1)){
					// E11157	商品予約削除可能期間以外の場合、                                                                    	削除操作は出来ません。	0	 	E
					$.showMessage('E11157');
					return false;
				}

				// 変更にもかかわらず、キー項目が変更されていたらエラー
				var txt_yoyakudt_ = "";		// 検索実行時のﾏｽﾀ変更日
				var txt_tenbaikadt_ = "";	// 検索実行時の店売価実施日
				if(that.baseData.length > 0){
					txt_yoyakudt_ = $.getParserDt(that.baseData[0]["F2"]);
					txt_tenbaikadt_ = $.getParserDt(that.baseData[0]["F3"]);
				}
				if(txt_yoyakudt*1 !== txt_yoyakudt_*1){
					// E11195	予約のマスタ変更日変更不可                                                                          	 	0	 	E
					$.showMessage('E11195');
					return false;
				}
				if(txt_tenbaikadt*1 !== txt_tenbaikadt_*1){
					// E11196	予約の店売価実施日変更不可                                                                          	 	0	 	E
					$.showMessage('E11196');
					return false;
				}
			}

			return rt;
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
					t:				(new Date()).getTime()
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
		getEasyUI: function(){	// （必須）情報の取得
			// 初期化
			this.jsonTemp = [];

			// レポート名
			this.jsonTemp.push({
				id:		"reportname",
				value:	this.caption(),
				text:	this.caption()
			});

			// 検索商品コード
			this.jsonTemp.push({
				id:		$.id.txt_sel_shncd,
				value:	$.getJSONValue(this.jsonHidden, $.id.txt_sel_shncd),
				text:	''
			});
			// 検索商品名（漢字）
			this.jsonTemp.push({
				id:		$.id.txt_sel_shnkn,
				value:	$.getJSONValue(this.jsonHidden, $.id.txt_sel_shnkn),
				text:	''
			});

			// 入力商品コード
			this.jsonTemp.push({
				id:		$.id_inp.txt_shncd,
				value:	$('#'+$.id_inp.txt_shncd).textbox('getValue'),
				text:	$('#'+$.id_inp.txt_shncd).textbox('getText')
			});

			// CSVエラー.SEQ
			this.jsonTemp.push({
				id:		$.id.txt_seq,
				value:	$.getJSONValue(this.jsonHidden, $.id.txt_seq),
				text:	''
			});
			// CSVエラー.入力番号
			this.jsonTemp.push({
				id:		$.id.txt_inputno,
				value:	$.getJSONValue(this.jsonHidden, $.id.txt_inputno),
				text:	''
			});
			// CSVエラー.CSV登録区分
			this.jsonTemp.push({
				id:		$.id.txt_csv_updkbn,
				value:	$.getJSONValue(this.jsonHidden, $.id.txt_csv_updkbn),
				text:	''
			});
			// マスタ変更予定日
			this.jsonTemp.push({
				id:		$.id_inp.txt_yoyakudt,
				value:	$.getInputboxValue($('#'+$.id_inp.txt_yoyakudt), "0"),
				text:	$('#'+$.id_inp.txt_yoyakudt).numberbox('getText')
			});
			// 店売価実施日
			this.jsonTemp.push({
				id:		$.id_inp.txt_tenbaikadt,
				value:	$.getInputboxValue($('#'+$.id_inp.txt_tenbaikadt), "0"),
				text:	$('#'+$.id_inp.txt_tenbaikadt).numberbox('getText')
			});
		},
		setData: function(rows, opts){		// データ表示
			var that = this;

			if(rows.length > 0){
				var notZeroEmpty = ['F4','F5','F6','F7','F8','F9','F10','F11','F12','F13','F14','F15','F16','F37','F132'];
				$('#'+that.focusRootId).find('[col^=F]').each(function(){
					var col = $(this).attr('col');
					if(rows[0][col]){
						$.setInputboxValue($(this), rows[0][col], $.inArray(col, notZeroEmpty) === -1);
					}
				});
			}

			// 初期値入力後にフラグを立てる。
			that.queried = true;
		},
		setRadioAreakbn: function(reportno, name, gpkbn){
			var that = this;
			var idx = -1;

			var id = name + gpkbn;
			// Radio 要素の初期化
			// 初期化情報取得
			var json = $.getJSONObject(that.jsonHidden, id);
			if (json){
				// 初期化
				$('input[name="'+id+'"]').val([json.value]);
			}
			// 更新項目で参照表示かどうか
			var isRefer = $.isReferUpdateInput(that, $('input[name="'+id+'"]'), true);
			$('input[name="'+id+'"]').change(function() {
				if(idx > 0){ $.removeErrState(); }
				if(isRefer && idx > 0 && that.queried){
					$($.id.hiddenChangedIdx).val("1");

					$("#"+$.id.grd_tengp+gpkbn).datagrid("reload");

					that.editRowIndex[$.id.grd_tengp+gpkbn] = -1;
				}
			});
			if(isRefer){ $.setInputStateRefer(that, $('input[name="'+id+'"]')); }

			if(that){
				if ($.inArray(id, that.initedObject) < 0){
					that.initedObject.push(id);
				}
				// 初期表示処理
				$.initialDisplay(that);
			}

			idx = 1;
		},
		setGrpkn: function(that, reportno, id, isUpdateReport){
			var idx = -1;
			var topBlank = true;

			$('#'+id).combobox({
				 url:$.reg.easy,
				//loader: myloader,
				required: false,
				editable: true,
				autoRowHeight:false,
				panelWidth:null,
				panelHeight:'auto',
				hasDownArrow: false,
				valueField:'TEXT',
				textField:'TEXT',
				multiple :false,
				prompt: '',
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
					idx=-1;
					// 情報設定
					var json = [{
						DUMMY: 'DUMMY'
					}];
					if(topBlank){json[0]['TOPBLANK'] = topBlank;}

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
					idx = 1;
					// ログ出力
					if($('#'+id+"_")){
						$('#'+id+"_").combobox("loadData", data);
					}
					if (that.queried) {
						$('#'+id+"_").textbox('textbox').validatebox('options').validType = 'onlyFullChar[100]';
					}
				},
				onChange:function(newValue, oldValue){
					if(idx > 0){ $.removeErrState(); }
					if($(this).attr('id')!==id){
						$('#'+id).combobox('reload', {q:newValue});
					}
				}
			});
			idx = 1;
		},
		setEasyGrid: function(that, reportno, id, chk){		// データ表示
			var index = -1;
			$('#'+id).datagrid({
				url:$.reg.easy,
				onBeforeLoad:function(param){
					index = -1;
					var json = that.getGridParams(that, id);
					// 情報設定
					param.page		=	reportno;
					param.obj		=	id;
					param.sel		=	(new Date()).getTime();
					param.target	=	id;
					param.action	=	$.id.action_init;
					param.json		=	JSON.stringify(json);
					param.datatype	=	"datagrid";
				},
				onLoadSuccess:function(data){
					if(index===-1){
						index=1;
						if(chk && data !== undefined && data.rows !== undefined){
							for (var i=0; i<data.rows.length; i++){
								if(data.rows[i].SEL === '1'){
									$('#'+id).datagrid('checkRow', i);
								}
							}
						}
						// 情報保持
						var txt_shncd_ = $.getInputboxValue($('#'+$.id_inp.txt_shncd));
						var txt_yoyakudt_ = $.getInputboxValue($('#'+$.id_inp.txt_yoyakudt));
						var gridData = that.getGridData(txt_shncd_, txt_yoyakudt_, id);
						that.setGridData(gridData, id);
					}
					if(chk){
						// チェックボックスの設定
						$.initCheckboxCss($('#'+id).datagrid('getPanel').find('.datagrid-body'));
					}
				},
			});
		},
		setEditableGrid: function(that, reportno, id){		// データ表示
			that.editRowIndex[id] = -1;
			var index = -1;
			$('#'+id).datagrid({
				url:$.reg.easy,
				onBeforeLoad:function(param){
					index = -1;
					var json = that.getGridParams(that, id);
					// 情報設定
					param.page		=	reportno;
					param.obj		=	id;
					param.sel		=	(new Date()).getTime();
					param.target	=	id;
					param.action	=	$.id.action_init;
					param.json		=	JSON.stringify(json);
					param.datatype	=	"datagrid";
				},
				onLoadSuccess:function(data){
					if(index===-1){
						index=1;
						// 情報保持
						var txt_shncd_ = $.getInputboxValue($('#'+$.id_inp.txt_shncd));
						var txt_yoyakudt_ = $.getInputboxValue($('#'+$.id_inp.txt_yoyakudt));
						var gridData = that.getGridData(txt_shncd_, txt_yoyakudt_, id);
						that.setGridData(gridData, id);
					}
				},
				onClickRow: function(index,field){$.clickEditableDatagridCell(that,id, index)},
				onBeginEdit:function(index,row){$.beginEditDatagridRow(that,id, index, row)},
				onEndEdit: function(index,row,changes){$.endEditDatagridRow(that, id, index, row)}
			});
		},
		setSrccdGrid: function(that, reportno, id){		// データ表示
			var index = -1;
			$('#'+id).datagrid({
				columns:[[
					{field:'SRCCD',		title:'ソースコード',	width:130,halign:'center',align:'left'		},
					{field:'SOURCEKBN',	title:'ソース区分',		width:110,halign:'center',align:'left'		},
					{field:'YUKO_STDT',	title:'有効開始日',		width: 70,halign:'center',align:'center'	,formatter:function(value){
						return $.getFormatDt(value, true);
					}},
					{field:'YUKO_EDDT',	title:'有効終了日',		width: 70,halign:'center',align:'center'	,formatter:function(value){
						return $.getFormatDt(value, true);
					}}
				]],
				singleSelect:true,
				rownumbers:true,
				fit:true,
				onLoadSuccess:function(data){
					if(index=1){
						// 情報保持
						var txt_shncd_ = $.getInputboxValue($('#'+$.id_inp.txt_shncd));
						var txt_yoyakudt_ = $.getInputboxValue($('#'+$.id_inp.txt_yoyakudt));
						var gridData = that.getGridData(txt_shncd_, txt_yoyakudt_, id);
						that.setGridData(gridData, id);
					}
				}
			});
			index = 1;
			// 情報設定
			var json = that.getGridParams(that, id);
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
				}
			);
		},
		setTengpGrid: function(that, reportno, id, gpkbn){		// データ表示
			var index = -1;
			var columns = null;

			var tgpformatter = function(value,row,index){ return $.getFormatLPad(value, $.len.tengp);};
			var iformatter = function(value,row,index){ return $.getFormat(value, '#,##0');};
			var dformatter = function(value,row,index){ return $.getFormat(value, '#,##0.00');};
			var pformatter = function(value,row,index){
				var val = $.getFormat(value, '#,##0');
				if(val==='') return val;
				return val+'%';
			};

			var editor_n=undefined,editor_t=undefined,editor_c=undefined;
			if(!that.judgeRepType.ref){
				editor_n = 'numberbox';
				editor_t = 'textbox';
				editor_c = 'combobox';
			}

			var funcClickCell = function(rowIndex, field, value){
				// 列名保持
				that.columnName = field;
			};

			// 店グループ（品揃え）
			if(gpkbn===$.id.value_gpkbn_shina){
				columns = [[
					{field:'TENGPCD', title:'店グループ',	width: 70,halign:'center',align:'left',formatter:tgpformatter,editor:editor_n},
					{field:'TENGPKN', title:'店グループ名称',width:279,halign:'center',align:'left'	,editor:{type:editor_t,options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}},
					{field:'ATSUKKBN', title:'扱い区分',	width: 90,halign:'center',align:'right'	,editor:editor_c},
					{field:'AREAKBN', hidden:true}
				]];
			// 店グループ（売価）
			}else if(gpkbn===$.id.value_gpkbn_baika){
				columns = [[
					{field:'TENGPCD', title:'店グループ',	width: 70,halign:'center',align:'left',formatter:tgpformatter,editor:editor_n},
					{field:'TENGPKN', title:'店グループ名称',width:279,halign:'center',align:'left'	,editor:{type:editor_t,options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}},
					{field:'GENKAAM', title:'原価',			width: 70,halign:'center',align:'right'	,formatter:dformatter,editor:editor_n},
					{field:'BAIKAAM', title:'本体売価',		width: 70,halign:'center',align:'right'	,formatter:iformatter,editor:editor_n},
					{field:'BG_SOUBAIKA', title:'総売価',	width: 70,halign:'center',align:'right'	,formatter:dformatter,editor:{type:editor_t,options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}},
					{field:'BG_NEIRE', title:'値入率',		width: 70,halign:'center',align:'right'	,formatter:pformatter,editor:{type:editor_t,options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}},
					{field:'IRISU', title:'店入数',			width: 70,halign:'center',align:'right'	,formatter:iformatter,editor:editor_n},
					{field:'AREAKBN', hidden:true}
				]];
			// 店グループ（仕入）
			}else if(gpkbn===$.id.value_gpkbn_sir){
				columns = [[
					{field:'TENGPCD', title:'店グループ'		,width: 70,halign:'center',align:'left'	,formatter:tgpformatter,editor:editor_n},
					{field:'TENGPKN', title:'店グループ名称'	,width:250,halign:'center',align:'left'		,editor:{type:editor_t,options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}},
					{field:'SSIRCD', title:'仕入先コード'		,width: 84,halign:'center',align:'left'	,formatter:function(value,row,index){ return $.getFormatLPad(value, $.len.sircd);},editor:editor_n},
					{field:'SIRKN', title:'仕入先名称'			,width:240,halign:'center',align:'left'		,editor:{type:editor_t,options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}},
					{field:'HSPTN', title:'配送パターン'		,width: 84,halign:'center',align:'left'	,formatter:function(value,row,index){ return $.getFormatLPad(value, $.len.hsptn);},editor:editor_n},
					{field:'HSPTNKN', title:'配送パターン名称'	,width:240,halign:'center',align:'left'		,editor:{type:editor_t,options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}},
					{field:'AREAKBN', hidden:true}
				]];
				// 店グループ（店別異部門）
			}else if(gpkbn===$.id.value_gpkbn_tbmn){
				var tenshncd_opt = $('#'+$.id_inp.txt_tenshncd).numberbox("options");
				var format = tenshncd_opt.prompt.replace(/_/g, '#');
				columns = [[
					{field:'TENSHNCD',title:'商品コード'		,width: 75,halign:'center',align:'left'	,formatter:function(value,row,index){
						return $.getFormatPrompt(value, format);},editor:editor_n},
					{field:'SRCCD',   title:'JANコード'			,width:120,halign:'center',align:'left'		,editor:editor_t},
					{field:'TENGPCD', title:'店グループ'		,width: 70,halign:'center',align:'left'		,formatter:tgpformatter,editor:editor_n},
					{field:'TENGPKN', title:'店グループ名称'	,width:174,halign:'center',align:'left'		,editor:{type:editor_t,options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}},
					{field:'AREAKBN', hidden:true}
				]];
			}

			var funcClickRow = $.fn.datagrid.defaults.onClickRow;
			var funcBeginEdit= undefined;
			var funcEndEdit= function(index,row){ return false; };
			if(!that.judgeRepType.ref){
				that.editRowIndex[id] = -1;
				funcClickRow = function(index,field){
					$.clickEditableDatagridCell(that,id, index);
				};
				funcBeginEdit = function(index,row){
					$.beginEditDatagridRow(that,id, index, row);
				};
				funcEndEdit = function(index,row,changes){
					$.endEditDatagridRow(that, id, index, row);
				};

				// 編集可能データグリッドの共通処理設定
				// 編集エディターの機能拡張（非表示で参照用のオブジェクトが準備してある前提、参照オブジェクトはグリッドのフィールド名を元に判断）
				$.extendDatagridEditor(that);
			}

			$('#'+id).datagrid({
				url:$.reg.easy,
				columns:columns,
				singleSelect:true,
				rownumbers:true,
				fit:true,
				onBeforeLoad:function(param){
					index = -1;
					var json = that.getGridParams(that, id);
					// 情報設定
					param.page		=	reportno;
					param.obj		=	id;
					param.sel		=	(new Date()).getTime();
					param.target	=	id;
					param.action	=	$.id.action_init;
					param.json		=	JSON.stringify(json);
					param.datatype	=	"datagrid";
				},
				onLoadSuccess:function(data){
					if(index===-1){
						if(gpkbn===$.id.value_gpkbn_baika){
							// 計算項目算出のため、変更時処理呼出
							that.changeInputboxFunc( that, id, null, $('#'+id), true);
						}
						index=1;
						// 情報保持
						var txt_shncd_ = $.getInputboxValue($('#'+$.id_inp.txt_shncd));
						var txt_yoyakudt_ = $.getInputboxValue($('#'+$.id_inp.txt_yoyakudt));
						var gridData = that.getGridData(txt_shncd_, txt_yoyakudt_, id);
						that.setGridData(gridData, id);
					}
				},
				onClickRow: funcClickRow,
				onBeginEdit: funcBeginEdit,
				onEndEdit: funcEndEdit,
				onClickCell:funcClickCell
			});
		},
		getGridParams:function(that, id){
			var values = {};
			values["callpage"] = $($.id.hidden_reportno).val();										// 呼出元レポート名
			values["SEL_SHNCD"] = $.getJSONValue(that.jsonHidden, $.id.txt_sel_shncd);				// 参照商品コード
			values["SHNCD"] = $.getInputboxValue($('#'+$.id_inp.txt_shncd));						// 入力商品コード
			values["TABLEKBN"] = that.baseTablekbn;													// 基本テーブル区分

			var txt_yoyakudt = "";
			// マスタ変更予定日　※予約参照時のキーとなる
			if(that.judgeRepType.yyk2_new){
				txt_yoyakudt = that.yoyakuData[0]["F2"];							// 引継
			}else{
				txt_yoyakudt = $.getInputboxValue($('#'+$.id_inp.txt_yoyakudt));	// 自データ
			}
			values["YOYAKUDT"] = txt_yoyakudt;

			if(that.judgeRepType.err){
				// CSVエラー.SEQ
				values["SEQ"] = $.getJSONValue(this.jsonHidden, $.id.txt_seq);
				// CSVエラー.入力番号
				values["INPUTNO"] = $.getJSONValue(this.jsonHidden, $.id.txt_inputno);
			}
			if(id.indexOf($.id.grd_tengp) > -1){
				var gpkbn = id.replace($.id.grd_tengp, "");
				values["GPKBN"] = gpkbn;
				values["AREAKBN"] = $("input[name="+$.id.rad_areakbn+gpkbn+"]:checked").val();
				values["BMNCD"] = $.getInputboxValue($('#'+$.id_inp.txt_bmncd));			// 部門
			}
			if(id===$.id.grd_srccd){
				values["ROWNUM"] = 2;
			}
			return [values];

		},
		setObjectState: function(){	// 軸の選択内容による制御

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
			var sendJSON = JSON.parse( JSON.stringify( that.jsonString ) );
			$.setJSONObject(sendJSON, 'sendBtnid', btnId, $('#'+btnId).attr("title"));					// 実行ボタン情報保持
			$.setJSONObject(sendJSON, 'reportYobi1', that.reportYobiInfo(), that.reportYobiInfo());		// 参照情報保持
			$.setJSONObject(sendJSON, 'callpage', that.name, that.name);								// 呼出し元レポート情報
			$.setJSONObject(sendJSON, $.id.txt_yoyaku, $('#'+$.id.txt_yoyaku).val(), $('#'+$.id.txt_yoyaku).val());

			// 戻る実行時用に現在の画面情報を保持する
			var states = $.getBackBaseJSON(that);
			var newrepinfos = $.getBackJSON(that, states, false);
			$.setJSONObject(sendJSON, 'repinfo', newrepinfos, '');


			// 実行ボタン別処理
			switch (btnId) {
			case $.id.btn_back:
				// 元画面情報
				var callpage = $.getJSONValue(that.jsonHidden, "callpage");
				// 転送先情報
				index = 1;
				if(that.reportYobiInfo()==='1'){
					index = 2;
				}
				// 元画面情報
				for (var i = 0; i < newrepinfos.length; i++) {
					var callpage = newrepinfos[i].id;
					if(callpage==='Out_Reportx005'){
						index = 5;
					}
				}
				sendMode = 2;
				childurl = href[index];
				break;
			case $.id.btn_cancel:
			case $.id.btn_upd:
			case $.id.btn_del:
				// 転送先情報
				index = 1;
				if(that.reportYobiInfo()==='1'){
					index = 2;
				}
				// 元画面情報
				for (var i = 0; i < newrepinfos.length; i++) {
					var callpage = newrepinfos[i].id;
					if(callpage==='Out_Reportx005'){
						index = 5;
					}
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
		changeInputboxFunc:function(that, id, newValue, obj, all){

			var parentObj = $('#'+that.focusRootId);
			if(id+"_"===obj.attr('id') && that.focusGridId!==""){
				parentObj = $('#'+that.focusGridId).datagrid('getPanel');
			}

			// DB問い合わせ系
			if($('[for_inp^='+id+'_]').length > 0){

				// for_inpが無効状態の項目を有効状態に設定する。
				if(that.queried){

					// DBへの問い合わせを実施するか否か
					var getDbFlg = true;

					if(id == $.id_inp.txt_ssircd){
						var getObj = $('#'+$.id_inp.txt_rg_idenflg);
						if (!$.isEmptyVal($.getInputboxValue(getObj))) {
							getDbFlg = false;

							var for_inp = getObj[0].getAttribute('for_inp');
							if (!$.isEmptyVal(for_inp)) {
								getObj[0].setAttribute("for_inp_not",for_inp);
								getObj.removeAttr('for_inp');
							}
						}
					}

					parentObj.find('[for_inp_not]').each(function(){
						var obj = $(this)
						if(obj){
							var onjId = obj[0].id
							if ((onjId===$.id_inp.txt_rg_idenflg && getDbFlg) || onjId!==$.id_inp.txt_rg_idenflg) {
								var for_inp = obj[0].getAttribute('for_inp_not')
								obj[0].setAttribute("for_inp",for_inp);
								obj.removeAttr('for_inp_not');
							}
						}
					})
				}

				var param = that.getInputboxParams(that, id, newValue);
				$.getsetInputboxRowData(that.name, 'for_inp', id, param, that, parentObj);
			}


			// 部門コード
			if(id===$.id_inp.txt_bmncd){

				// disabled化
				if([20,23,31,70,73].indexOf(newValue) === -1){
					$.setInputBoxDisable($('#'+$.id_mei.kbn151), true);			// 選択リスト(保温区分)
					$.setInputBoxDisable($('#'+$.id_mei.kbn152+"_r"), true);	// 選択リスト(デリカワッペン区分_レギュラ)
					$.setInputBoxDisable($('#'+$.id_mei.kbn152+"_h"), true);	// 選択リスト(デリカワッペン区分_販促)
					$.setInputBoxDisable($('#'+$.id_mei.kbn153), true);			// 選択リスト(取扱区分)
				}else{
					$.setInputBoxEnable($('#'+$.id_mei.kbn151));				// 選択リスト(保温区分)
					$.setInputBoxEnable($('#'+$.id_mei.kbn152+"_r"));			// 選択リスト(デリカワッペン区分_レギュラ)
					$.setInputBoxEnable($('#'+$.id_mei.kbn152+"_h"));			// 選択リスト(デリカワッペン区分_販促)
					$.setInputBoxEnable($('#'+$.id_mei.kbn153));				// 選択リスト(取扱区分)
				}
				// 非表示化
				$('#'+that.focusRootId).find('[for_bmn]').each(function(){
					var bmns = $(this).attr('for_bmn').split(",");
					if(bmns.indexOf(newValue) === -1){
						$(this).hide();
					}else{
						$(this).show();
					}
				});
			}

			// 検索、入力後特殊処理
			if(that.queried||all){
				// 特殊処理
				var bmncd = $.getInputboxValue($('#'+$.id_inp.txt_bmncd));

				// ソースコード変更
				if(id.indexOf($.id.grd_srccd+"_win") !== -1){
					// 第一、第二ソースコードを取得し、本画面に表示
					$('#'+$.id.grd_srccd).datagrid('loadData', $.winIT031.filterRows);
					var targetRowsSrccd = $('#'+id).datagrid('getRows');

					// 添付資料（MD03112501）のメーカーコードの取得方法
					if(bmncd.length > 0){
						if(targetRowsSrccd.length > 0){
							// ソースコードが削除された場合は、メーカーコード欄をクリアする。
							if(targetRowsSrccd[0]['DEL'] == '1'){
								$.setInputboxValue($('#'+$.id_inp.txt_makercd), "");

							}else{
								// ソースコードの新規・変更登録の場合
								var value = targetRowsSrccd[0]['SRCCD'];
								var kbn = targetRowsSrccd[0]["SOURCEKBN"].split("-")[0];
								if(value.length > 0 && kbn.length > 0 && kbn !== -1){
									var param = {};
									param["value"] = value;
									param["TABLEKBN"] = that.baseTablekbn;
									param["BMNCD"] = bmncd;			// 部門
									param["KBN"] = kbn;
									param["KEY"] =  "MD03112501";
									$.getsetInputboxData(that.name, $.id_inp.txt_makercd, [param]);
								}
							}
						}else{
							var value = $.getInputboxText($('#'+$.id_inp.txt_bmncd)) + '00001';
							$.setInputboxValue($('#'+$.id_inp.txt_makercd), value);
						}
					}
				}

				// リードタイムパターン
				if(id===$.id.sel_readtmptn){
					that.setDefHatkbnByReadtmptn(bmncd, newValue);
				}

				// 添加物リスト切換
				if(id===$.id_inp.txt_kensaku){			// 検索文字
					// 現在グリッド編集中の場合のみ対象
					if(that.focusGridId != undefined && that.focusGridId != ""){
						// 添加物コードリスト再読み込み
						var val = obj.textbox("getValue");
						var rows = $('#'+$.id_mei.kbn138).combobox('getData');
						if(val!==""){
							rows = $.getSelectListData(that.name, $.id.action_change, id, [{value:val}]);
						}
						$('#'+$.id_mei.kbn138+'_').combobox('loadData', rows);
						$('#'+$.id_mei.kbn138+'_').combobox('setValue', "");

					}
				}

				// グループ分類名
				if(id===$.id_inp.txt_grpkn){			// 検索文字
					// 添加物コードリスト再読み込み
					var val = obj.textbox("getValue");
					rows = $.getSelectListData(that.name, $.id.action_change, id, [{value:val}]);
					$('#'+$.id_inp.txt_grpkn+'_').combobox('loadData', rows);
					$('#'+$.id_inp.txt_grpkn+'_').combobox('setValue', "");
				}

				// レギュラー値入率
				if(id===$.id_inp.txt_rg_genkaam				// レギュラー原価
				|| id===$.id_inp.txt_rg_baikaam){			// レギュラー本体売価
					// 値入率算出
					var genka = $.getInputboxValue($('#'+$.id_inp.txt_rg_genkaam));
					var baika = $.getInputboxValue($('#'+$.id_inp.txt_rg_baikaam));
					$.setInputboxValue($('#'+$.id.txt_rg_neire), that.calcNeireRit(baika, genka)+'%');
				}

				// 販促値入率
				if(id===$.id_inp.txt_hs_genkaam				// 販促原価
				|| id===$.id_inp.txt_hs_baikaam){			// 販促本体売価
					// 値入率算出
					var genka = $.getInputboxValue($('#'+$.id_inp.txt_hs_genkaam));
					var baika = $.getInputboxValue($('#'+$.id_inp.txt_hs_baikaam));
					$.setInputboxValue($('#'+$.id.txt_hs_neire), that.calcNeireRit(baika, genka)+'%');
				}

				// 売価グループ値入率
				if(that.focusGridId === $.id.grd_tengp+$.id.value_gpkbn_baika && that.editRowIndex[$.id.grd_tengp+$.id.value_gpkbn_baika] >= 0
					&&(id===$.id_inp.txt_genkaam				// 原価
					 ||id===$.id_inp.txt_baikaam)){				// 本体売価
					// 値入率算出
					var genka = $.getInputboxValue($('#'+$.id_inp.txt_genkaam+'_'));
					var baika = $.getInputboxValue($('#'+$.id_inp.txt_baikaam+'_'));

					//var rows = $('#'+$.id.grd_tengp+$.id.value_gpkbn_baika).datagrid('getRows');
					//rows[that.editRowIndex[$.id.grd_tengp+$.id.value_gpkbn_baika]]["BG_NEIRE"] = that.calcNeireRit(baika, genka);
					//$('#'+$.id.grd_tengp+$.id.value_gpkbn_baika).datagrid('refreshRow');
					$.setInputboxValue($('#'+$.id.txt_bg_neire+'_'), that.calcNeireRit(baika, genka));
				}


				// 添付資料（MD03111301）の 総売価
				if(id===$.id_inp.txt_rg_baikaam
				|| id===$.id_inp.txt_hs_baikaam
				||(id===$.id_inp.txt_baikaam && that.focusGridId === $.id.grd_tengp+$.id.value_gpkbn_baika && that.editRowIndex[$.id.grd_tengp+$.id.value_gpkbn_baika] >= 0)
				|| id===$.id_mei.kbn120
				|| id===$.id.sel_zeirtkbn
				|| id===$.id.sel_zeirtkbn_old){
					var target = new Array(), values = new Array();
					var targetRowIdx = new Array();
					var rows = $('#'+$.id.grd_tengp+$.id.value_gpkbn_baika).datagrid('getRows');
					if(id===$.id_inp.txt_rg_baikaam){
						target[0] = $.id.txt_rg_soubaika;
						values[0] = $.getInputboxValue($('#'+$.id_inp.txt_rg_baikaam));
					}else if(id===$.id_inp.txt_hs_baikaam){
						target[0] = $.id.txt_hs_soubaika;
						values[0] = $.getInputboxValue($('#'+$.id_inp.txt_hs_baikaam));
					}else if(id===$.id_inp.txt_baikaam){
						target[0] = $.id.txt_bg_soubaika+'_';
						values[0] = $.getInputboxValue($('#'+$.id_inp.txt_baikaam+'_'));
					}else{
						target[0] = $.id.txt_rg_soubaika;
						values[0] = $.getInputboxValue($('#'+$.id_inp.txt_rg_baikaam));
						target[1] = $.id.txt_hs_soubaika;
						values[1] = $.getInputboxValue($('#'+$.id_inp.txt_hs_baikaam));
						for (var i=0; i<rows.length; i++){
							var val = rows[i]["BAIKAAM"];
							if(!$.isEmptyVal(val)){
								targetRowIdx.push(i);
								values.push(val);
							}
						}
					}
					var idx = 0;
					for (var i=0; i<target.length; i++){
						var param = {};
						param["KEY"] =  "MD03111301";
						param["value"] = values[i]+"";
						param["TABLEKBN"] = that.baseTablekbn;
						param["BMNCD"] = $.getInputboxValue($('#'+$.id_inp.txt_bmncd));					// 部門
						param[$.id_mei.kbn120] = $.getInputboxValue($('#'+$.id_mei.kbn120));						// 税区分
						param[$.id.sel_zeirtkbn] = $.getInputboxValue($('#'+$.id.sel_zeirtkbn));					// 税率区分
						param[$.id.sel_zeirtkbn_old] = $.getInputboxValue($('#'+$.id.sel_zeirtkbn_old));			// 旧税率区分
						param[$.id_inp.txt_zeirthenkodt] = $.getInputboxValue($('#'+$.id_inp.txt_zeirthenkodt));	// 税率変更日
						param[$.id_inp.txt_tenbaikadt] = $.getInputboxValue($('#'+$.id_inp.txt_tenbaikadt));		// 店売価実施日
						var val = $.getInputboxData(that.name, $.id.action_change, target[i], [param]);
						$.setInputboxValue($('#'+target[i]), $.getFormat(val, '#,##0.00'));
						idx++;
					}
					for (var i=0; i<targetRowIdx.length; i++){
						var param = {};
						param["KEY"] =  "MD03111301";
						param["value"] = values[idx]+"";
						param["TABLEKBN"] = that.baseTablekbn;
						param["BMNCD"] = $.getInputboxValue($('#'+$.id_inp.txt_bmncd));					// 部門
						param[$.id_mei.kbn120] = $.getInputboxValue($('#'+$.id_mei.kbn120));						// 税区分
						param[$.id.sel_zeirtkbn] = $.getInputboxValue($('#'+$.id.sel_zeirtkbn));					// 税率区分
						param[$.id.sel_zeirtkbn_old] = $.getInputboxValue($('#'+$.id.sel_zeirtkbn_old));			// 旧税率区分
						param[$.id_inp.txt_zeirthenkodt] = $.getInputboxValue($('#'+$.id_inp.txt_zeirthenkodt));	// 税率変更日
						param[$.id_inp.txt_tenbaikadt] = $.getInputboxValue($('#'+$.id_inp.txt_tenbaikadt));		// 店売価実施日
						var val = $.getInputboxData(that.name, $.id.action_change, $.id.txt_bg_soubaika, [param]);
						rows[targetRowIdx[i]]["BG_SOUBAIKA"] = $.getFormat(val, '#,##0.00');
						$('#'+$.id.grd_tengp+$.id.value_gpkbn_baika).datagrid('refreshRow', targetRowIdx[i]);
						idx++;
					}
				}

				if(id===$.id.grd_tengp+$.id.value_gpkbn_baika){
					var target = new Array(), values = new Array();
					var targetRowIdx = new Array();
					var rows = $('#'+$.id.grd_tengp+$.id.value_gpkbn_baika).datagrid('getRows');
					for (var i=0; i<rows.length; i++){
						var baika = rows[i]["BAIKAAM"];
						var genka = rows[i]["GENKAAM"];
						if(!$.isEmptyVal(baika)){
							// 総売価
							var param = {};
							param["KEY"] =  "MD03111301";
							param["value"] = baika+"";
							param["TABLEKBN"] = that.baseTablekbn;
							param["BMNCD"] = $.getInputboxValue($('#'+$.id_inp.txt_bmncd));								// 部門
							param[$.id_mei.kbn120] = $.getInputboxValue($('#'+$.id_mei.kbn120));						// 税区分
							param[$.id.sel_zeirtkbn] = $.getInputboxValue($('#'+$.id.sel_zeirtkbn));					// 税率区分
							param[$.id.sel_zeirtkbn_old] = $.getInputboxValue($('#'+$.id.sel_zeirtkbn_old));			// 旧税率区分
							param[$.id_inp.txt_zeirthenkodt] = $.getInputboxValue($('#'+$.id_inp.txt_zeirthenkodt));	// 税率変更日
							param[$.id_inp.txt_tenbaikadt] = $.getInputboxValue($('#'+$.id_inp.txt_tenbaikadt));		// 店売価実施日
							var val = $.getInputboxData(that.name, $.id.action_change, $.id.txt_bg_soubaika, [param]);
							rows[i]["BG_SOUBAIKA"] = val;
							// 値入率算出
							rows[i]["BG_NEIRE"] = that.calcNeireRit(baika, genka);
							$('#'+$.id.grd_tengp+$.id.value_gpkbn_baika).datagrid('refreshRow', i);
						}
					}

				}
			}

			if (id===$.id_mei.kbn108) {
				if (newValue==='-1') {
					$('#'+id).combobox('setText', '');
				}
			}
		},
		getInputboxParams: function(that, id, newValue){
			// 情報取得
			var values = {};
			values["value"] = newValue;
			values["TABLEKBN"] = that.baseTablekbn;

			// 特殊項目
			// 部門系
			var prefix = /(^\w+_)/g.exec(id)[0];
			if(id.search(new RegExp("_"+$.id_inp_suffix.daicd+"$")) > 0
			|| id.search(new RegExp("_"+$.id_inp_suffix.chucd+"$")) > 0
			|| id.search(new RegExp("_"+$.id_inp_suffix.shocd+"$")) > 0
			|| id.search(new RegExp("_"+$.id_inp_suffix.sshocd+"$")) > 0){
				values[$.id_inp_suffix.bmncd] = $.getInputboxValue($('#'+prefix+$.id_inp_suffix.bmncd));
			}
			if(id.search(new RegExp("_"+$.id_inp_suffix.chucd+"$")) > 0
			|| id.search(new RegExp("_"+$.id_inp_suffix.shocd+"$")) > 0
			|| id.search(new RegExp("_"+$.id_inp_suffix.sshocd+"$")) > 0){
				values[$.id_inp_suffix.daicd] = $.getInputboxValue($('#'+prefix+$.id_inp_suffix.daicd));
			}
			if(id.search(new RegExp("_"+$.id_inp_suffix.shocd+"$")) > 0
			|| id.search(new RegExp("_"+$.id_inp_suffix.sshocd+"$")) > 0){
				values[$.id_inp_suffix.chucd] = $.getInputboxValue($('#'+prefix+$.id_inp_suffix.chucd));
			}
			if(id.search(new RegExp("_"+$.id_inp_suffix.sshocd+"$")) > 0){
				values[$.id_inp_suffix.shocd] = $.getInputboxValue($('#'+prefix+$.id_inp_suffix.shocd));
			}

			// 店グループ系
			if(id.search(new RegExp("^"+$.id_inp.txt_tengpcd) > 0)){
				var gpkbn = that.focusGridId.replace($.id.grd_tengp, "");
				values["GPKBN"] = gpkbn;
				values["AREAKBN"] = $("input[name="+$.id.rad_areakbn+gpkbn+"]:checked").val();
				values["BMNCD"] = $.getInputboxValue($('#'+$.id_inp.txt_bmncd));			// 部門
			}

			// 商品コード
			if(id===$.id_inp.txt_shncd){
				values["KETA"] = $.getInputboxValue($('#'+$.id_mei.kbn143));				// 桁数
				values["BMNCD"] = $.getInputboxValue($('#'+$.id_inp.txt_bmncd));			// 部門
			}

			// 情報設定
			return [values];
		},
		calcNeireRit: function(baika, genka){
			if(baika.length===0) return "";
			if(genka.length===0) return "";

			// （本体売価－原価）÷本体売価で、小数点以下3位切り捨て, 第2位まで求める。上限98%
			// ただし、商品種別で包材、消耗品、コメント、催事テナントの時はチェックしない。
			var value = ((baika*100)-(genka*100))/100;
			value = Math.floor(Math.floor((value/baika)*10000)/100);
			if(!isFinite(value)) return "";
			return $.getFormat(value, '#,##0');
		},
		// リードタイムパターン
		setDefHatkbnByReadtmptn : function(bmn, typ){
			// var ableRykBmn = [2,9,15,4,5,6,10,11,20,23,34,43,88,13,26,27];
			// CCRが本処理に対応できない為、要望によりMDMでは全ての部門の入力可とする。

			$.setInputboxValue($('#'+'chk_hat_monkbn'), $.id.value_off);
			$.setInputboxValue($('#'+'chk_hat_tuekbn'), $.id.value_off);
			$.setInputboxValue($('#'+'chk_hat_wedkbn'), $.id.value_off);
			$.setInputboxValue($('#'+'chk_hat_thukbn'), $.id.value_off);
			if(typ==="10"){
				$.setInputboxValue($('#'+'chk_hat_frikbn'), $.id.value_off);
				$.setInputboxValue($('#'+'chk_hat_satkbn'), $.id.value_off);
				$.setInputboxValue($('#'+'chk_hat_sunkbn'), $.id.value_on);
			}else if(typ==="20"){
				$.setInputboxValue($('#'+'chk_hat_frikbn'), $.id.value_off);
				$.setInputboxValue($('#'+'chk_hat_satkbn'), $.id.value_on);
				$.setInputboxValue($('#'+'chk_hat_sunkbn'), $.id.value_off);
			}else if(typ==="21"){
				$.setInputboxValue($('#'+'chk_hat_frikbn'), $.id.value_off);
				$.setInputboxValue($('#'+'chk_hat_satkbn'), $.id.value_off);
				$.setInputboxValue($('#'+'chk_hat_sunkbn'), $.id.value_on);
			}else if(typ==="30"){
				$.setInputboxValue($('#'+'chk_hat_frikbn'), $.id.value_on);
				$.setInputboxValue($('#'+'chk_hat_satkbn'), $.id.value_off);
				$.setInputboxValue($('#'+'chk_hat_sunkbn'), $.id.value_off);
			} else {
				$.setInputboxValue($('#'+'chk_hat_frikbn'), $.id.value_off);
				$.setInputboxValue($('#'+'chk_hat_satkbn'), $.id.value_off);
				$.setInputboxValue($('#'+'chk_hat_sunkbn'), $.id.value_off);
			}
		},
		keyEventInputboxFunc:function(e, code, that, obj){
			// *** Enter or Tab ****
			if(code === 13 || code === 9){

				var newValue = obj.val();
				var id = $(obj).attr("orizinid");
				if(id===$.id_inp.txt_rg_idenflg && $.isEmptyVal(newValue)){
					$.setInputboxValue($('#'+$.id_inp.txt_nmkn),'');
				}

				obj = obj.parent().prev();
				if(obj.hasClass('easyui-combobox')) {
					if (!$.setComboReload(obj,false)) {
						obj.combobox('reload');
					}
				}
			}
		}
	} });
})(jQuery);