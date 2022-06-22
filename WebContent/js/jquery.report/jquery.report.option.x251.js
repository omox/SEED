/**
 * jquery report option
 */
;(function($) {

	// 商品マスタのfunctionをコピー
	$.x002 = $.reportOption;

	$.extend({
		reportOption: {
		name:		'Out_Reportx251',			// （必須）レポートオプションの確認
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
		setSearchObjectState:	$.x002.setSearchObjectState,
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
		setData:				$.x002.setData,
		setRadioAreakbn:		$.x002.setRadioAreakbn,
		setGrpkn:				$.x002.setGrpkn,
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
		updValidationIn:		$.x002.updValidationIn,
		srcCodeValidation:		$.x002.srcCodeValidation,
		updConfirm:				$.x002.updConfirm,
		delValidation:			$.x002.delValidation,
		setEasyGrid:			$.x002.setEasyGrid,
		setEditableGrid:		$.x002.setEditableGrid,
		setSrccdGrid:			$.x002.setSrccdGrid,
		setTengpGrid:			$.x002.setTengpGrid,
		getGridParams:			$.x002.getGridParams,

		/**
		 * ここから下はx251のオリジナルfunctionを利用しているもの
		 * 商品マスタでの仕様変更が発生した場合はこちらにも同様の改修を適用する必要性がある
		 */
		userYobi1Info: function(){
			return $('#userYobi1').val();
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
			}

			var hidobjids = [];	// 非表示

			that.baseTablekbn = $.id.value_tablekbn_sei;						// 情報取得先
			// 新規：新規・新規コピー・選択コピーボタン押下時
			if(that.judgeRepType.sei_new){
				hidobjids = hidobjids.concat([$.id.btn_yoyaku1,$.id.btn_yoyaku2,$.id.btn_del]);

				$("#disp_record_info").hide();

				$.initReportInfo("x251", "仕掛商品マスタ　新規", "新規");

			// 変更：検索・変更・正ボタン押下時
			}else if(that.judgeRepType.sei_upd){
				if(that.reportYobiInfo()==='1'){
					$.initReportInfo("x251", "仕掛商品マスタ　参照", "参照");
				}else{
					$.initReportInfo("x251", "仕掛商品マスタ　変更", "変更");
				}
			}else if(that.judgeRepType.sei_ref){
				var szMode = $.getJSONValue(that.jsonHidden, 'Mode');
				if(szMode === "Teian"){
					$.initReportInfo("x251", "提案商品マスタ　参照", "参照");
				}else{
					$.initReportInfo("x251", "仕掛商品マスタ　参照", "参照");
				}

				$("#"+$.id.btn_cancel).css({"display":"none"});
				$("#"+$.id.btn_upd).css({"display":"none"});
				$("#"+$.id.btn_del).css({"display":"none"});

			}

			// 全体処理
			if(that.reportYobiInfo()==='1'){
				that.judgeRepType.ref = true;
				hidobjids = hidobjids.concat([$.id.btn_cancel,$.id.btn_upd,$.id.btn_del]);
				$('#'+$.id.btn_sei).on("click", $.pushChangeReport);
				$.setInputBoxDisable($($.id.hiddenChangedIdx));
			}else{
				$('#'+$.id.btn_sei).on("click", $.pushChangeReport);
				$('#'+$.id.btn_cancel).on("click", $.pushChangeReport);
			}

			$.setInputboxValue($('#'+$.id.txt_teian_no),$.getInputboxValue($('#'+$.id.hiddenNoTeian)));

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
		success: function(reportno, sortable){	// （必須）正処理
			if (sortable) sortable=1; else sortable=0;

			// 状態
			this.setStcdShikakari(reportno, $.id.SelStcdShikakari);

			var that = this;
			// 検索実行
			var txt_sel_shncd		= $.getJSONObject(this.jsonString, $.id.txt_sel_shncd).value;		// 検索商品コード
			var txt_shncd			= $.getJSONObject(this.jsonString, $.id_inp.txt_shncd).value;		// 入力商品コード
			var txt_seq				= $.getJSONObject(this.jsonString, $.id.txt_seq).value;				// CSVエラー.SEQ
			var txt_inputno			= $.getJSONObject(this.jsonString, $.id.txt_inputno).value;			// CSVエラー.入力番号
			var txt_csv_updkbn		= $.getJSONObject(this.jsonString, $.id.txt_csv_updkbn).value;		// CSVエラー.CSV登録区分
			var txt_yoyakudt = "";
			var txt_tenbaikadt = "";

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$.appendMask();
			$.appendMaskMsg();

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,			// レポート名
					SEL_SHNCD:		txt_sel_shncd.replace("-", ""),		// 検索商品コード
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
					rows:			1,	// 表示可能レコード数
					MODE: 			$.getJSONValue(that.jsonHidden, 'Mode')
				},
				function(json){
					// 検索処理エラー判定
					if($.searchError(json)) return false;

					// ログ出力
					$.log(that.timeData, 'query:');


					var opts = JSON.parse(json).opts


					// 検索結果を保持
					that.baseData = JSON.parse(json).rows;

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
			var txt_yoyakudt = "";
			var txt_tenbaikadt = "";

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
					t:				(new Date()).getTime(),
					TEIAN:			$('#'+$.id.hiddenNoTeian).val(),			// 提案件名No
					STATE_S:			$.getInputboxValue($('#'+$.id.SelStcdShikakari)),
					SHNKN:		$.getInputboxValue($('#'+$.id_inp.txt_shnkn)),					// 商品名
					BMNCD:		$.getInputboxValue($('#'+$.id_inp.txt_bmncd)),					// 部門
					DAICD:			$.getInputboxValue($('#'+$.id_inp.txt_daicd)),					// 大分類
					CHUCD:		$.getInputboxValue($('#'+$.id_inp.txt_chucd)),					// 中分類
					MODE: 			$.getJSONValue(that.jsonHidden, 'Mode')
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
			var txt_yoyakudt = "";
			var txt_tenbaikadt = "";

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
			var oldVal = id===$.id.SelBumon ? that.oldBmn:that.oldDai;

			if (!$.isEmptyVal(oldVal) && oldVal===newVal) {
				return false;
			} else {
				return true;
			}
		},
		setStcdShikakari: function(reportno, id){		// 状態_仕掛商品
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


			var data = [
				{"VALUE":"-1","TEXT":"　"},
				{"VALUE":"01","TEXT":"作成中"},
				{"VALUE":"02","TEXT":"確定"},
			];

			var callpage = $.getJSONValue(that.jsonHidden, "callpage");
			console.log("callpage=",callpage);
			if(callpage === "Out_Reportx249"){
				data.push({"VALUE":"03","TEXT":"仕掛"});
			}
			if(callpage === "Out_Reportx249" || that.userYobi1Info() === "0000002"){
				data.push({"VALUE":"04","TEXT":"承認"});
			}
			data.push({"VALUE":"09","TEXT":"却下"});

			$('#'+id).combobox({
				panelWidth:85,
				panelHeight:'auto',
				url:$.reg.easy,
				required: true,
				editable: true,
				autoRowHeight:false,
				valueField:'VALUE',
				textField:'TEXT',
				data: data,
				multiple :false,
				prompt: '',
				icons: [{
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
					// 情報設定
					var json = [{
						DUMMY: 'DUMMY'
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
					val = new Array();
					for (var i=0; i<data.length; i++){
						if ($.inArray(data.value)!=-1){
							val.push(data[i].VALUE);
						}
					}
					if (val.length===data.length || val.length===0){
						val = null;
					}

					if (val){
						$('#'+id).combobox('setValues',val);
					} else {
						$('#'+id).combobox('setValues','-1');
					}

					var STATE = $.getJSONValue(that.jsonHidden, 'hiddenTeianStcd');
					var status = "-1";

					if(STATE === "作成中"){
						status = "01";
					}else if(STATE === "確定"){
						status = "02";
					}else if(STATE === "仕掛"){
						status = "03";
					}else if(STATE === "承認" || STATE === "完了"){
						status = "04";
					}else if(STATE === "却下"){
						status = "09";
					}
					$('#'+id).combobox('select', status);

					idx = 1;

					// ログ出力
					$.log(that.timeData, id+' init:');
					// 検索ボタン有効化
					$.setButtonState('#'+$.id.btn_search, true, id);
					that.onChangeFlag = false;
				},
				onShowPanel: function(){
					$.setScrollComboBox(id);
					that.onChangeFlag=false;
					onChange=false;
					onPanel=true;
				},
				onHidePanel: function(){
					if (onChange){
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
					$($.id.hiddenChangedIdx).val("1");

					if(obj===undefined){obj = $(this);}
					$.removeErrState();
					// 検索ボタン有効化
					//$.setButtonState('#'+$.id.btn_search, true, id);
					onChange=true;
					that.getComboErr(obj,false,newValue,oldValue);
				}
			});
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
				if(callpage==='Out_Reportx250'){
					index = 2;
				}
				sendMode = 2;
				childurl = href[index];
				break;
			case $.id.btn_cancel:
			case $.id.btn_upd:
			case $.id.btn_del:
				var callpage = $.getJSONValue(that.jsonHidden, "callpage");
				// 転送先情報
				index = 1;
				if(that.reportYobiInfo()==='1'){
					index = 2;
				}
				if(callpage==='Out_Reportx250'){
					index = 2;
				}
				sendMode = 2;
				childurl = href[index];
				break;
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


		}
	} });
})(jQuery);