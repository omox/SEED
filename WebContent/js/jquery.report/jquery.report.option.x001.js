/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_Reportx001',			// （必須）レポートオプションの確認
		jsonTemp:	[],							// （必須）検索条件情報_入力チェック前
		jsonString:	[],							// （必須）検索条件情報
		jsonStringCsv:	[],						// （CSV出力用）検索条件情報
		jsonHidden: [],							// （必須）親画面からの引き継ぎ情報
		jsonInit: [],							// （必須）検索条件初期情報
		caption: function(){					// （必須）タイトル
			return $('#reportname').val();
		},
		sortName: '',	// ソート項目名
		sortOrder: '',	// ソート順
		timeData : (new Date()).getTime(),
		dedefaultObjNum:	14,	// 初期化オブジェクト数
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
		sendBtnid: "",						// 呼出ボタンID情報
		pushBtnid: "",						// 実行ボタンID情報(検索系で利用)
		focusRootId:"cc",					// （キー移動時必須）キー移動イベントのルートとなるパネルのID
		focusParentId:"",					// （キー移動時必須）現在フォーカスがあたっている項目の親となるパネルのID
		focusGridId:"",						// （キー移動時必須）現在フォーカスがあたっているDataGridのID
		oldBmn:"",
		oldDai:"",
		initialize: function (reportno){	// （必須）初期化
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

			// サブウインドウの初期化
			$.win001.init(that);	// メーカー
			$.win002.init(that);	// 仕入先

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
			if(sendBtnid && sendBtnid.length > 0){
				that.sendBtnid = sendBtnid;
				$.reg.search = true;
			}

			// 当帳票を「参照」で開いた場合
			if(that.reportYobiInfo()==='1'){
				$.setInputBoxDisable($("#"+$.id.btn_copy)).hide();
				//$.setInputBoxDisable($("#"+$.id.btn_csv)).hide();
				$('#'+$.id.btn_csv).on("click", that.pushCsv);		// CSV出力ボタンのみ表示する
				$.setInputBoxDisable($("#"+$.id.btn_new)).hide();
				$.setInputBoxDisable($("#"+$.id.btn_sel_copy)).hide();
				$.setInputBoxDisable($("#"+$.id.btn_sel_change)).hide();
				$.setInputBoxDisable($("#"+$.id.btn_sel_csverr)).hide();
				$.setInputBoxDisable($("#"+$.id.btn_csv_import)).hide();
				$.setInputBoxDisable($("#"+$.id.btn_csv_import_yyk)).hide();
				$('#'+$.id.btn_sel_refer).on("click", $.pushChangeReport);

				$.initReportInfo("IT001", "商品マスタ　一覧　参照");
			}else{
				$($.id.buttons).show();
				// 各種ボタン
				$('#'+$.id.btn_copy).on("click", that.pushCopy);
				$('#'+$.id.btn_csv).on("click", that.pushCsv);
				$('#'+$.id.btn_new).on("click", $.pushChangeReport);
				$('#'+$.id.btn_sel_copy).on("click", $.pushChangeReport);
				$('#'+$.id.btn_sel_change).on("click", $.pushChangeReport);
				// TODO：お客様開示の場合使用不可にする
				if(true){
					$('#'+$.id.btn_sel_csverr).on("click", $.pushChangeReport);
					$('#'+$.id.btn_csv_import).on("click", $.pushChangeReport);
					$('#'+$.id.btn_csv_import_yyk).on("click", $.pushChangeReport);
				}else{
					$.setInputBoxDisable($("#"+$.id.btn_sel_csverr));
					$.setInputBoxDisable($("#"+$.id.btn_csv_import));
					$.setInputBoxDisable($("#"+$.id.btn_csv_import_yyk));
				}
				$.setInputBoxDisable($("#"+$.id.btn_sel_refer)).hide();
				$.initReportInfo("IT001", "商品マスタ　一覧");
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
		/**
		 * 新規コピー/ボタンイベント
		 * @param {Object} e
		 */
		pushCopy : function(e){
			if ($(this).linkbutton('options').disabled)	return false;

			// レポート番号取得
			var reportno=$($.id.hidden_reportno).val();
			var id = $(this).attr('id');

			// レポート定義位置
			var reportNumber = $.getReportNumber(reportno);
			if (typeof(reportNumber) !== 'number') { alert("レポート定義が見つかりません。"); return false;}

			// マスク削除
			$.removeMask();


			// フォーム情報取得
			$.report[reportNumber].getEasyUI();

			if ($.report[reportNumber].validation(id)) {
//				// 検索ボタン無効化
//				$.setButtonState('#'+$.id.btn_search, false, 'success');

				// マスク追加
				$.appendMask();

				// セッションタイムアウト、利用時間外の確認
				var isTimeout = $.checkIsTimeout();
				if (! isTimeout) {
					// 検索条件保持
					$.ajax({
						url: $.reg.easy,
						type: 'POST',
						async: false,
						data: {
							"page"	: reportno,
							"obj"	: $.id.btn_search,
							"sel"	: "json",
							"userid": $($.id.hidden_userid).val(),
							"user"	: $($.id.hiddenUser).val(),
							"report": $($.id.hiddenReport).val(),
							"json"	: JSON.stringify($.report[reportNumber].getJSONString())
						},
						success: function(json){
							// 検索実行
							$.report[reportNumber].success(reportno, undefined, id);
						}
					});
				}
				return true;
			} else {
				return false;
			}
		},
		/**
		 * CSV出力ボタンイベント
		 * @param {Object} e
		 */
		pushCsv : function(e){
			if ($(this).linkbutton('options').disabled)	return false;

			// レポート番号取得
			var reportno=$($.id.hidden_reportno).val();
			var id = $(this).attr('id');

			// レポート定義位置
			var reportNumber = $.getReportNumber(reportno);
			if (typeof(reportNumber) !== 'number') { alert("レポート定義が見つかりません。"); return false;}

			// マスク削除
			$.removeMask();

			// フォーム情報取得
			$.report[reportNumber].getEasyUI();

			if ($.report[reportNumber].validation(id)) {

				var func_ok = function(){
					// マスク追加
					$.appendMask();

					// セッションタイムアウト、利用時間外の確認
					var isTimeout = $.checkIsTimeout();
					if (! isTimeout) {
						// 検索条件保持
						$.ajax({
							url: $.reg.easy,
							type: 'POST',
							async: false,
							data: {
								"page"	: reportno,
								"obj"	: $.id.btn_search,
								"sel"	: "json",
								"userid": $($.id.hidden_userid).val(),
								"user"	: $($.id.hiddenUser).val(),
								"report": $($.id.hiddenReport).val(),
								"json"	: JSON.stringify($.report[reportNumber].jsonStringCsv)
							},
							success: function(json){
								// Excel 出力
								$.report[reportNumber].srccsv(reportno, id);
							}
						});
					}
				};

				// CSVデータを出力します。よろしいですか？
				$.showMessage("W20015", undefined, func_ok);
			} else {
				return false;
			}
		},
		validation: function (btnId){	// （必須）批准
			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform).form('validate');
			if(!rt){
				$.addErrState(that, $('.validatebox-invalid').eq(0), false);
				return rt;
			}

			var szShncd			= $.getJSONObject(this.jsonTemp, $.id_inp.txt_shncd).value;		// 商品コード
			if(rt){
				var check = JSON.parse('{'+$('#'+$.id_inp.txt_shncd).attr("check")+'}');
				if(szShncd.length > 0 && szShncd.length !== check.maxlen*1){
					$.showMessage('EX1006', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_shncd), true)});
					rt = false;
				}
			}
			if(rt){
				if(btnId === $.id.btn_copy && szShncd.length === 0){
					$.showMessage('EX1007', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_shncd), true)});
					rt = false;
				}
			}
			var szSrccd			= $.getJSONObject(this.jsonTemp, $.id_inp.txt_srccd).value;		// ソースコード
			var szSelBumon		= $.getJSONObject(this.jsonTemp, $.id.SelBumon).value;			// 部門
			if(rt){
				if(btnId===$.id.btn_csv){
					var szCsvShncd			= $.getJSONObject(this.jsonTemp, $.id_inp.txt_csvshncd).value;		// 商品コード
					if(szCsvShncd.length === 0 && szSrccd.length === 0 && (szSelBumon.length === 1 && szSelBumon[0] === $.id.valueSel_Head)){
						$.showMessage('E11091', ["CSV"], function(){$.addErrState(that, $('#'+$.id_inp.txt_csvshncd), true)});
						rt = false;
					}
					if(rt){
						if(szCsvShncd.length !== 0 && szSrccd.length !== 0){
							$.showMessage('E11090', ["CSV"], function(){$.addErrState(that, $('#'+$.id_inp.txt_csvshncd), true)});
							rt = false;
						}
					}
				}else{
					if ($.reportOption.name==='Out_Reportx001') {
						if(szShncd.length === 0 && szSrccd.length === 0 && (szSelBumon.length === 1 && szSelBumon[0] === $.id.valueSel_Head)){
							$.showMessage('E11091', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_shncd), true)});
							rt = false;
						}
						if(rt){
							if(szShncd.length !== 0 && szSrccd.length !== 0){
								$.showMessage('E11090', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_shncd), true)});
								rt = false;
							}
						}
					}
				}
			}
			if(rt){
				var szIryoreflg = $.getJSONObject(this.jsonTemp, $.id_mei.kbn142).value;			// 衣料使い回しフラグ
				if(szSelBumon.indexOf("13") !== -1 || szSelBumon.indexOf("27") !== -1 ){
					for(var i=0; i < szSelBumon.length; i++){
						if(["13","27"].indexOf(szSelBumon[i])===-1){
							$.showMessage('EX1008', undefined, function(){$.addErrState(that, $('#'+$.id.SelBumon), true)});
							rt= false;
							break;
						}
					}
					if(rt &&  szIryoreflg===$.id.valueSel_Head){
						$.showMessage('E11092', undefined, function(){$.addErrState(that, $('#'+$.id_mei.kbn142), true)});
						rt = false;
					}
				}else{
					if(szIryoreflg!==$.id.valueSel_Head){
						$.showMessage('E11093', undefined, function(){$.addErrState(that, $('#'+$.id_mei.kbn142), true)});
						rt = false;
					}
				}
			}
			// 入力エラーなしの場合に検索条件を格納
			if(btnId===$.id.btn_csv){
				if (rt == true) that.jsonStringCsv = that.jsonTemp.slice(0);
			}else{
				if (rt == true) that.jsonString = that.jsonTemp.slice(0);
			}

			// 入力チェック用の配列をクリア
			that.jsonTemp = [];
			return rt;
		},
		success: function(reportno, sortable, btnId){	// （必須）正処理
			if (sortable) sortable=1; else sortable=0;
			var that = this;
			// 検索実行
			var szShncd			= $.getJSONObject(this.jsonString, $.id_inp.txt_shncd).value;		// 商品コード
			var szShnkn			= $.getJSONObject(this.jsonString, $.id_inp.txt_shnkn).value;		// 商品名（漢字）
			var szSrccd			= $.getJSONObject(this.jsonString, $.id_inp.txt_srccd).value;		// ソースコード
			var szSelBumon		= $.getJSONObject(this.jsonString, $.id.SelBumon).value;			// 部門
			var dtSelBumon		= $.getJSONObject(this.jsonString, $.id.SelBumon+'DATA').value;		// 部門のDATA
			var szSelDaiBun		= $.getJSONObject(this.jsonString, $.id.SelDaiBun).value;			// 大分類
			var dtSelDaiBun		= $.getJSONObject(this.jsonString, $.id.SelDaiBun+'DATA').value;	// 大分類のDATA
			var szSelChuBun		= $.getJSONObject(this.jsonString, $.id.SelChuBun).value;			// 中分類
			var dtSelChuBun		= $.getJSONObject(this.jsonString, $.id.SelChuBun+'DATA').value;	// 中分類のDATA
			var szSsircd		= $.getJSONObject(this.jsonString, $.id_inp.txt_ssircd).value;		// 仕入先コード
			var szMakercd		= $.getJSONObject(this.jsonString, $.id_inp.txt_makercd).value;		// メーカーコード
			var szCsvshncd		= $.getJSONObject(this.jsonString, $.id_inp.txt_csvshncd).value;	// CSV出力用商品コード
			var szTeikankbn		= $.getJSONObject(this.jsonString, $.id_mei.kbn121).value;			// 定貫不定貫区分
			var szTeikeikbn		= $.getJSONObject(this.jsonString, $.id_mei.kbn117).value;			// 定計区分
			var szUpddtf		= $.getJSONObject(this.jsonString, $.id_inp.txt_upddtf).value;		// 更新日from
			var szUpddtt		= $.getJSONObject(this.jsonString, $.id_inp.txt_upddtt).value;		// 更新日to
			var szIryoreflg		= $.getJSONObject(this.jsonString, $.id_mei.kbn142).value;			// 衣料使い回しフラグ
			if(!btnId) btnId = $.id.btn_search;

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
					report:			that.name,		// レポート名
					BTN:			btnId,
					SHNCD:			szShncd,		// 商品コード
					SHNKN:			szShnkn,		// 商品名(漢字)
					SRCCD:			szSrccd,		// ソースコード
					BUMON:			JSON.stringify(szSelBumon),		// 部門
					BUMON_DATA:		JSON.stringify(dtSelBumon),		// 部門のDATA
					DAI_BUN:		JSON.stringify(szSelDaiBun),	// 大分類
					DAI_BUN_DATA:	JSON.stringify(dtSelDaiBun),	// 大分類のDATA
					CHU_BUN:		JSON.stringify(szSelChuBun),	// 中分類
					CHU_BUN_DATA:	JSON.stringify(dtSelChuBun),	// 中分類のDATA
					SSIRCD:			szSsircd,						// 仕入先コード
					MAKERCD:		szMakercd,						// メーカーコード
					CSVSHNCD:		szCsvshncd,						// CSV出力用商品コード
					TEIKANKBN:		szTeikankbn,					// 定貫不定貫区分
					TEIKEIKBN:		szTeikeikbn,					// 定計区分
					UPDDTF:			szUpddtf,						// 更新日FROM
					UPDDTT:			szUpddtt,						// 更新日TO
					IRYOREFLG:		szIryoreflg,					// 衣料使い回しフラグ
					t:				(new Date()).getTime(),
					sortable:		sortable,
					sortName:		that.sortName,
					sortOrder:		that.sortOrder,
					rows:			0	// 表示可能レコード数
				},
				function(json){
					// 検索処理エラー判定
					if($.searchError(json, undefined, that)) return false;

					// ログ出力
					$.log(that.timeData, 'query:');

					// Load処理回避
					$.tryChangeURL(null);

					var limit = 1000;
					var size = JSON.parse(json)["total"];
					if(size > limit){
						$.showMessage('E00010');
					}

					if (sortable===0){
						var options = $($.id.gridholder).datagrid('options');
						// 初期検索時に並び替え情報のリセット
						options.sortName = null;
						options.sortOrder = null;
					}

					that.queried = true;
					that.pushBtnid = btnId;
					// Load処理回避
					$.tryChangeURL($.reg.jqeasy);

					// 前画面からの遷移時商品コードはクリア
					if ($.reg.search) {
						$.setInputboxValue($('#'+$.id_inp.txt_shncd),"");
					}

					// グリッド再描画（easyui 1.4.2 対応）
					$($.id.gridholder).datagrid('load', {} );
					$.removeMask();

/*					// 検索ボタン無効化
					$.setButtonState('#'+$.id.btn_search, false, 'success');
*/
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
				value:	$('#'+$.id_inp.txt_shncd).numberbox('getValue'),
				text:	$('#'+$.id_inp.txt_shncd).numberbox('getText')
			});
			// 商品名（漢字）
			this.jsonTemp.push({
				id:		$.id_inp.txt_shnkn,
				value:	$('#'+$.id_inp.txt_shnkn).textbox('getValue'),
				text:	$('#'+$.id_inp.txt_shnkn).textbox('getText')
			});
			// ソースコード
			this.jsonTemp.push({
				id:		$.id_inp.txt_srccd,
				value:	$('#'+$.id_inp.txt_srccd).numberbox('getValue'),
				text:	$('#'+$.id_inp.txt_srccd).numberbox('getText')
			});

			// 部門
			this.jsonTemp.push({
				id:		$.id.SelBumon,
				value:	$('#'+$.id.SelBumon).combobox('getValues'),
				text:	$('#'+$.id.SelBumon).combobox('getText')
			});
			// 全選択or未選択=「すべて」
			$.convertComboBox(this.jsonTemp,$.id.SelBumon);

			// 部門(DATA)
			var dtBumon = $('#'+$.id.SelBumon).combobox('getData');
			var dataBumon = [];
			for(var i=0;i<dtBumon.length;i++){
				dataBumon.push(dtBumon[i].VALUE);
			}
			this.jsonTemp.push({
				id:		$.id.SelBumon+'DATA',
				value:	dataBumon,
				text:	'全部門情報'
			});

			// 大分類
			this.jsonTemp.push({
				id:		$.id.SelDaiBun,
				value:	$('#'+$.id.SelDaiBun).combobox('getValues'),
				text:	$('#'+$.id.SelDaiBun).combobox('getText')
			});
			// 全選択or未選択=「すべて」
			$.convertComboBox(this.jsonTemp,$.id.SelDaiBun);
			// 大分類(DATA)
			var dtDaibun = $('#'+$.id.SelDaiBun).combobox('getData');
			var dataDaibun = [];
			for(var i=0;i<dtDaibun.length;i++){
				dataDaibun.push(dtDaibun[i].VALUE);
			}
			this.jsonTemp.push({
				id:		$.id.SelDaiBun+'DATA',
				value:	dataDaibun,
				text:	'全大分類情報'
			});

			// 中分類
			this.jsonTemp.push({
				id:		$.id.SelChuBun,
				value:	$('#'+$.id.SelChuBun).combobox('getValues'),
				text:	$('#'+$.id.SelChuBun).combobox('getText')
			});
			// 全選択or未選択=「すべて」
			$.convertComboBox(this.jsonTemp,$.id.SelChuBun);
			// 中分類(DATA)
			var dtChuBun = $('#'+$.id.SelChuBun).combobox('getData');
			var dataChuBun = [];
			for(var i=0;i<dtChuBun.length;i++){
				dataChuBun.push(dtChuBun[i].VALUE);
			}
			this.jsonTemp.push({
				id:		$.id.SelChuBun+'DATA',
				value:	dataChuBun,
				text:	'全中分類情報'
			});

			// 仕入先コード
			this.jsonTemp.push({
				id:		$.id_inp.txt_ssircd,
				value:	$('#'+$.id_inp.txt_ssircd).numberbox('getValue'),
				text:	$('#'+$.id_inp.txt_ssircd).numberbox('getText')
			});
			// メーカーコード
			this.jsonTemp.push({
				id:		$.id_inp.txt_makercd,
				value:	$('#'+$.id_inp.txt_makercd).numberbox('getValue'),
				text:	$('#'+$.id_inp.txt_makercd).numberbox('getText')
			});
			// CSV出力用商品コード
			this.jsonTemp.push({
				id:		$.id_inp.txt_csvshncd,
				value:	$('#'+$.id_inp.txt_csvshncd).numberbox('getValue'),
				text:	$('#'+$.id_inp.txt_csvshncd).numberbox('getText')
			});
			// 定貫不定貫区分
			 this.jsonTemp.push({
				id:		$.id_mei.kbn121,
				value:	$('#'+$.id_mei.kbn121).combobox('getValue'),
				text:	$('#'+$.id_mei.kbn121).combobox('getText')
			});
			// 定計区分
			 this.jsonTemp.push({
				id:		$.id_mei.kbn117,
				value:	$('#'+$.id_mei.kbn117).combobox('getValue'),
				text:	$('#'+$.id_mei.kbn117).combobox('getText')
			});
			// 更新日from
			this.jsonTemp.push({
				id:		$.id_inp.txt_upddtf,
				value:	$('#'+$.id_inp.txt_upddtf).numberbox('getValue'),
				text:	$('#'+$.id_inp.txt_upddtf).numberbox('getText')
			});
			// 更新日to
			this.jsonTemp.push({
				id:		$.id_inp.txt_upddtt,
				value:	$('#'+$.id_inp.txt_upddtt).numberbox('getValue'),
				text:	$('#'+$.id_inp.txt_upddtt).numberbox('getText')
			});
			// 衣料使い回し
			 this.jsonTemp.push({
				id:		$.id_mei.kbn142,
				value:	$('#'+$.id_mei.kbn142).combobox('getValue'),
				text:	$('#'+$.id_mei.kbn142).combobox('getText')
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
		setBumon: function(reportno, id){		// 部門
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
				required: true,
				editable: true,
				autoRowHeight:false,
				valueField:'VALUE',
				textField:'TEXT',
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
						REQUIRED: 'REQUIRED'
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
					if (val){
						$('#'+id).combobox('setValues',val);
					} else {
						$('#'+id).combobox('setValues','-1');
					}
					idx = 1;
					// ログ出力
					$.log(that.timeData, id+' init:');
					that.onChangeFlag = false;
					// 大分類
					that.tryLoadMethods('#'+$.id.SelDaiBun);
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
							that.tryLoadMethods('#'+$.id.SelDaiBun);
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

					if(idx > 0){ $.removeErrState(); }
					if(idx > 0 && that.onChangeFlag){
						// 大分類
						that.tryLoadMethods('#'+$.id.SelDaiBun);
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
				required: true,
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
					if (that.initializes) return false;
					// 情報設定
					var json = [{
						REQUIRED: 'REQUIRED',
						BUMON: $('#'+$.id.SelBumon).combobox('getValue')
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
					if (val){
						$('#'+id).combobox('setValues',val);
					} else {
						$('#'+id).combobox('setValues','-1');
					}
					idx = 1;
					// ログ出力
					$.log(that.timeData, id+' init:');
					that.onChangeFlag = false;
					$.ajaxSettings.async = true;
					// 中分類
					that.tryLoadMethods('#'+$.id.SelChuBun);
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
							that.tryLoadMethods('#'+$.id.SelChuBun);
						};
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

					if(idx > 0){ $.removeErrState(); }
					if(idx > 0 && that.onChangeFlag){
						// 上位変更時、下位更新は常に同期
						$.ajaxSettings.async = false;
						that.onChangeFlag = false;
						// 中分類
						that.tryLoadMethods('#'+$.id.SelChuBun);
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
				required: true,
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
					if (that.initializes) return false;

					// 情報設定
					var json = [{
						REQUIRED: 'REQUIRED',
						BUMON: $('#'+$.id.SelBumon).combobox('getValue'),
						DAI_BUN: $('#'+$.id.SelDaiBun).combobox('getValue')
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
							if (val.length===data.length){
								val = null;
							}
							if ($.isArray(val) && val.length===0){	// 旧コード対応
								val = null;
							}
						}
					}
					if (val){
						$('#'+id).combobox('setValues',val);
					} else {
						$('#'+id).combobox('setValues','-1');
					}
					idx = 1;
					// ログ出力
					$.log(that.timeData, id+' init:');
					that.onChangeFlag = true;
					$.ajaxSettings.async = true;
//					// 検索ボタン有効化
//					$.setButtonState('#'+$.id.btn_search, true, id);
					// 初期表示検索処理
					$.initialSearch(that);
				},
				onShowPanel:function(){
					$.setScrollComboBox(id);
				}
				,onChange:function(newValue, oldValue,obj){
					if(idx > 0){ $.removeErrState(); }

					if(obj===undefined){obj = $(this);}
					that.getComboErr(obj,false,newValue,oldValue);
				}
			});
		},
		/**
		 * 中分類（中分類が利用不可の場合、すべて）変換
		 */
		convertBumonChuBun: function(value){
			// 中分類（中分類が利用不可の場合、すべて）
			if ($('#'+$.id.SelChuBun).combobox('options').disabled){
				value = ['-1'];
			}
			return value;
		},
		setObjectState: function(){	// 軸の選択内容による制御

		},
		setGrid: function (id, reportNumber){	// グリッドの構築
			var that = this;
			var init = true;
			// ページサイズ定義取得
			var pageList = $.fn.pagination.defaults.pageList;
			var pageSize = $.getJSONValue(this.jsonHidden, $.id.pageSize);
			if (pageSize==="") pageSize=$.fn.pagination.defaults.pageSize;
			pageSize = $.getDefaultPageSize(pageSize, pageList);
			$(id).datagrid({
				nowrap: true,
				border: true,
				striped: true,
				collapsible:false,
				remoteSort: true,
				rownumbers:true,
				fit:true,
				view:scrollview,
				pageSize:pageSize,
				pageList:pageList,
				frozenColumns:[[]],
				columns:[[
					{field:'F1',	title:'予約',				width: 40,halign:'center',align:'right'},
					{field:'F2',	title:'商品コード',			width: 80,halign:'center',align:'left'},
					{field:'F3',	title:'ソースコード1',		width:120,halign:'center',align:'left'},
					{field:'F4',	title:'販売コード',			width: 70,halign:'center',align:'left'},
					{field:'F5',	title:'商品名',				width:300,halign:'center',align:'left'},
					{field:'F6',	title:'扱<br>区分',			width: 40,halign:'center',align:'left'},
					{field:'F7',	title:'原価',				width: 70,halign:'center',align:'right',
						formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0.00');}},
					{field:'F8',	title:'本体売価',			width: 70,halign:'center',align:'right',
						formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0');}},
					{field:'F9',	title:'総額売価',			width: 70,halign:'center',align:'right',
						formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0');}},
					{field:'F10',	title:'店入数',				width: 70,halign:'center',align:'right',
						formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0');}},
					{field:'F11',	title:'親コード',			width: 80,halign:'center',align:'left'},
					{field:'F12',	title:'ワッペン区分',		width: 60,halign:'center',align:'left'},
					{field:'F13',	title:'一括区分',			width: 40,halign:'center',align:'left'},
					{field:'F14',	title:'標準仕入先',			width: 70,halign:'center',align:'left'},
					{field:'F15',	title:'分類コード',			width:100,halign:'center',align:'left'},
					{field:'F16',	title:'更新日',				width: 70,halign:'center',align:'center'},
					{field:'F17',	title:'衣料使い回し',		width: 50,halign:'center',align:'center'},
				]],
				fitColumns:false,	// 指定カラム幅を適用する場合、false 指定。
				rowStyler:function(index, row){
				},
				onSortColumn:function(sort, order){
					if (that.jsonString.length===0) return false;
					// カラム並び替え
					that.success(that.name, true);
				},
				onLoadSuccess:function(data){
					// 検索後、初回のみ処理
					if (that.queried){
						that.queried = false;	// 検索後、初回のみ処理
						// 状態保存
						$.saveState(reportNumber, that.getJSONString(), id);
						// 警告
						$.showWarningMessage(data);
						that.loadSuccessFunc(id, data);

						if($.getJSONValue(that.jsonHidden, "scrollToIndex_"+id) == ""){
							$.setJSONObject(that.jsonHidden, "scrollToIndex_"+id, 0, 0);
						}
					}

					// 前回選択情報をGridに反映
					var test = $.getJSONValue(that.jsonHidden, "scrollToIndex_"+id)
					test = test * 1
					var getRowIndex = data.total===0 ? '':$.getJSONValue(that.jsonHidden, "scrollToIndex_"+id);
					if (data.total !== 0 && (data.total-1) < getRowIndex) {
						getRowIndex = getRowIndex-1;
					}
					if(getRowIndex !== ""){
						$(id).datagrid('scrollTo', {
							index: getRowIndex,
							callback: function(index){
								$(id).datagrid('selectRow', index);
							}
						});

						// 検索可能な一覧画面では、検索時に前回選択情報が適用されてしまう為、
						// 一度適用したら要素を削除する。
						var targetName = "scrollToIndex_"+id;
						that.jsonHidden.some(function(v, i){
						    if (v.id==targetName) that.jsonHidden.splice(i,1);
						});

					}
					// 商品コードにフォーカス
					var target = $.getInputboxTextbox($('#'+$.id_inp.txt_shncd));
					target.focus();
				},
				onClickCell:function(rowIndex, field, value){
					// 列名保持
					that.columnName = field;
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
		loadSuccessFunc:function(id, data){				// 画面遷移
			var that = this;
			// 画面遷移による検索以外の場合
			if(that.sendBtnid.length === 0){
				// 1件のみの場合、遷移
				if(that.pushBtnid===$.id.btn_search
				|| that.pushBtnid===$.id.btn_copy){
					if(data.total===1){
						setTimeout(function(){
							$(id).datagrid('selectRow', 0);
							that.changeReport(that.name, that.pushBtnid);
						},0);
					}
				}
			}else{
				// 初回以外は移動OKのため、初期化
				that.sendBtnid = "";
			}
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


			// 選択行
			var row = $($.id.gridholder).datagrid("getSelected");

			// 実行ボタン別処理
			switch (btnId) {
			case $.id.btn_new:
				// 転送先情報
				index = 3;
				childurl = href[index];

				// オブジェクト作成
				$.setJSONObject(sendJSON, $.id.txt_sel_shncd,'', '');
				$.setJSONObject(sendJSON, $.id.txt_sel_shnkn,'', '');
				$.setJSONObject(sendJSON, $.id_inp.txt_shncd, '', '');
				break;
			case $.id.btn_copy:
			case $.id.btn_sel_copy:
				if(!row){
					$.showMessage('E00008');
					return false;
				}

				// 転送先情報
				index = 3;
				childurl = href[index];

				// オブジェクト作成
				$.setJSONObject(sendJSON, $.id.txt_sel_shncd, row.F18, row.F18);
				$.setJSONObject(sendJSON, $.id.txt_sel_shnkn, row.F5, row.F5);
				$.setJSONObject(sendJSON, $.id_inp.txt_shncd, '', '');
				break;
			case $.id.btn_search:
			case $.id.btn_sel_change:
			case $.id.btn_sel_refer:
				if(!row){
					$.showMessage('E00008');
					return false;
				}

				// 転送先情報
				index = 3;
				childurl = href[index];

				// オブジェクト作成
				$.setJSONObject(sendJSON, $.id.txt_sel_shncd, row.F18, row.F18);
				$.setJSONObject(sendJSON, $.id.txt_sel_shnkn, row.F5, row.F5);
				$.setJSONObject(sendJSON, $.id_inp.txt_shncd, row.F18, row.F18);
				break;
			case $.id.btn_csv_import:
			case $.id.btn_csv_import_yyk:
				// 転送先情報
				index = 4;
				childurl = href[index];

				break;
			case $.id.btn_sel_csverr:
				// 転送先情報
				index = 6;
				childurl = href[index];

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
		srccsv: function(reportno, btnId){	// ここではCsv出力
			var that = this;
			// 処理時間計測用
			that.timeData = (new Date()).getTime();

			var that = this;
			// 検索実行
			var szShncd			= $.getJSONObject(this.jsonStringCsv, $.id_inp.txt_shncd).value;		// 商品コード
			var szShnkn			= $.getJSONObject(this.jsonStringCsv, $.id_inp.txt_shnkn).value;		// 商品名（漢字）
			var szSrccd			= $.getJSONObject(this.jsonStringCsv, $.id_inp.txt_srccd).value;		// ソースコード
			var szSelBumon		= $.getJSONObject(this.jsonStringCsv, $.id.SelBumon).value;			// 部門
			var dtSelBumon		= $.getJSONObject(this.jsonStringCsv, $.id.SelBumon+'DATA').value;		// 部門のDATA
			var szSelDaiBun		= $.getJSONObject(this.jsonStringCsv, $.id.SelDaiBun).value;			// 大分類
			var dtSelDaiBun		= $.getJSONObject(this.jsonStringCsv, $.id.SelDaiBun+'DATA').value;	// 大分類のDATA
			var szSelChuBun		= $.getJSONObject(this.jsonStringCsv, $.id.SelChuBun).value;			// 中分類
			var dtSelChuBun		= $.getJSONObject(this.jsonStringCsv, $.id.SelChuBun+'DATA').value;	// 中分類のDATA
			var szSsircd		= $.getJSONObject(this.jsonStringCsv, $.id_inp.txt_ssircd).value;		// 仕入先コード
			var szMakercd		= $.getJSONObject(this.jsonStringCsv, $.id_inp.txt_makercd).value;		// メーカーコード
			var szCsvshncd		= $.getJSONObject(this.jsonStringCsv, $.id_inp.txt_csvshncd).value;	// CSV出力用商品コード
			var szTeikankbn		= $.getJSONObject(this.jsonStringCsv, $.id_mei.kbn121).value;			// 定貫不定貫区分
			var szTeikeikbn		= $.getJSONObject(this.jsonStringCsv, $.id_mei.kbn117).value;			// 定計区分
			var szUpddtf		= $.getJSONObject(this.jsonStringCsv, $.id_inp.txt_upddtf).value;		// 更新日from
			var szUpddtt		= $.getJSONObject(this.jsonStringCsv, $.id_inp.txt_upddtt).value;		// 更新日to
			var szIryoreflg		= $.getJSONObject(this.jsonStringCsv, $.id_mei.kbn142).value;			// 衣料使い回しフラグ
			if(!btnId) btnId = $.id.btn_search;

			// タイトル部
			var title = [["更新区分","商品コード","商品コード桁数指定","定計区分","ソース区分_1","ソースコード_1","標準仕入先コード",
			              "定貫不定貫区分","標準分類コード_部門","標準分類コード_大","標準分類コード_中","標準分類コード_小","標準分類コード_小小",
			              "ＰＣ区分","商品種類","親商品コード","商品名（カナ）","商品名（漢字）","レシート名（カナ）","レシート名（漢字）",
			              "プライスカード商品名称（漢字）","商品コメント・セールスコピー（漢字）","ＰＯＰ名称（漢字）","規格",
			              "レギュラー情報_取扱フラグ","レギュラー情報_原価","レギュラー情報_売価","レギュラー情報_店入数",
			              "レギュラー情報_一括伝票フラグ","レギュラー情報_ワッペン","販促情報_取扱フラグ","販促情報_原価","販促情報_売価",
			              "販促情報_店入数","販促情報_ワッペン","販促情報_特売ワッペン","便区分","締め回数","小物区分","仕分区分","棚卸区分","期間",
			              "ODS_賞味期限_春","ODS_賞味期限_夏","ODS_賞味期限_秋","ODS_賞味期限_冬","ODS_入荷期限","ODS_値引期限",
			              "販促情報_スポット最低発注数","製造限度日数","リードタイムパターン","発注曜日_月","発注曜日_火","発注曜日_水",
			              "発注曜日_木","発注曜日_金","発注曜日_土","発注曜日_日","配送パターン","ユニットプライス_容量","ユニットプライス_単位容量",
			              "ユニットプライス_ユニット単位","商品サイズ_縦","商品サイズ_横","商品サイズ_奥行","商品サイズ_重量","取扱期間_開始日",
			              "取扱期間_終了日","陳列形式コード","段積み形式コード","重なりコード","重なりサイズ","圧縮率","マスタ変更予定日",
			              "店売価実施日","用途分類コード_部門","用途分類コード_大","用途分類コード_中","用途分類コード_小","売場分類コード_部門",
			              "売場分類コード_大","売場分類コード_中","売場分類コード_小","エリア区分","店グループ（エリア）_1","仕入先コード_1",
			              "配送パターン_1","店グループ（エリア）_2","仕入先コード_2","配送パターン_2","店グループ（エリア）_3","仕入先コード_3",
			              "配送パターン_3","店グループ（エリア）_4","仕入先コード_4","配送パターン_4","店グループ（エリア）_5","仕入先コード_5",
			              "配送パターン_5","店グループ（エリア）_6","仕入先コード_6","配送パターン_6","店グループ（エリア）_7","仕入先コード_7",
			              "配送パターン_7","店グループ（エリア）_8","仕入先コード_8","配送パターン_8","店グループ（エリア）_9","仕入先コード_9",
			              "配送パターン_9","店グループ（エリア）_10","仕入先コード_10","配送パターン_10","エリア区分","店グループ（エリア）_1",
			              "原価_1","売価_1","店入数_1","店グループ（エリア）_2","原価_2","売価_2","店入数_2","店グループ（エリア）_3","原価_3",
			              "売価_3","店入数_3","店グループ（エリア）_4","原価_4","売価_4","店入数_4","店グループ（エリア）_5","原価_5","売価_5",
			              "店入数_5","エリア区分","店グループ（エリア）_1","扱い区分_1","店グループ（エリア）_2","扱い区分_2",
			              "店グループ（エリア）_3","扱い区分_3","店グループ（エリア）_4","扱い区分_4","店グループ（エリア）_5","扱い区分_5",
			              "店グループ（エリア）_6","扱い区分_6","店グループ（エリア）_7","扱い区分_7","店グループ（エリア）_8","扱い区分_8",
			              "店グループ（エリア）_9","扱い区分_9","店グループ（エリア）_10","扱い区分_10","平均パック単価","ソース区分_2",
			              "ソースコード_2","プライスカード出力有無","プライスカード_種類","プライスカード_色","税区分","税率区分","旧税率区分",
			              "税率変更日","取扱停止","市場区分","ＰＢ区分","返品区分","輸入区分","裏貼","対象年齢","カロリー表示","加工区分",
			              "産地（漢字）","酒級","度数","包材用途","包材材質","包材リサイクル対象","フラグ情報_ＥＬＰ","フラグ情報_ベルマーク",
			              "フラグ情報_リサイクル","フラグ情報_エコマーク","メーカーコード","販売コード","添加物_1","添加物_2","添加物_3","添加物_4",
			              "添加物_5","添加物_6","添加物_7","添加物_8","添加物_9","添加物_10","アレルギー_1","アレルギー_2","アレルギー_3",
			              "アレルギー_4","アレルギー_5","アレルギー_6","アレルギー_7","アレルギー_8","アレルギー_9","アレルギー_10","アレルギー_11",
			              "アレルギー_12","アレルギー_13","アレルギー_14","アレルギー_15","アレルギー_16","アレルギー_17","アレルギー_18",
			              "アレルギー_19","アレルギー_20","アレルギー_21","アレルギー_22","アレルギー_23","アレルギー_24","アレルギー_25",
			              "アレルギー_26","アレルギー_27","アレルギー_28","アレルギー_29","アレルギー_30","種別コード","衣料使い回しフラグ",
			              "登録元","オペレータ","登録日","更新日","保温区分","デリカワッペン区分_レギュラー","取扱区分"]];

			var kbn = 0;
			var data = {
				report:			that.name,		// レポート名
				'kbn':			 kbn,
				'type':			'csv',
				'header':	JSON.stringify(title),
				BTN:			btnId,
				SHNCD:			szShncd,		// 商品コード
				SHNKN:			szShnkn,		// 商品名(漢字)
				SRCCD:			szSrccd,		// ソースコード
				BUMON:			JSON.stringify(szSelBumon),		// 部門
				BUMON_DATA:		JSON.stringify(dtSelBumon),		// 部門のDATA
				DAI_BUN:		JSON.stringify(szSelDaiBun),	// 大分類
				DAI_BUN_DATA:	JSON.stringify(dtSelDaiBun),	// 大分類のDATA
				CHU_BUN:		JSON.stringify(szSelChuBun),	// 中分類
				CHU_BUN_DATA:	JSON.stringify(dtSelChuBun),	// 中分類のDATA
				SSIRCD:			szSsircd,						// 仕入先コード
				MAKERCD:		szMakercd,						// メーカーコード
				CSVSHNCD:		szCsvshncd,						// CSV出力用商品コード
				TEIKANKBN:		szTeikankbn,					// 定貫不定貫区分
				TEIKEIKBN:		szTeikeikbn,					// 定計区分
				UPDDTF:			szUpddtf,						// 更新日FROM
				UPDDTT:			szUpddtt,						// 更新日TO
				IRYOREFLG:		szIryoreflg,					// 衣料使い回しフラグ
				t:				(new Date()).getTime(),
				rows:			0	// 表示可能レコード数
			};

			// 転送
			$.ajax({
				url: $.reg.srcexcel,
				type: 'POST',
				data: data,
				async: true
			})
			.done(function(){
				// Excel出力
				$.outputSearchExcel(reportno, 0);
			})
			.fail(function(){
				// Excel出力エラー
				$.outputSearchExcelError();
			})
			.always(function(){
				// 通信完了
				// ログ出力
				$.log(that.timeData, 'srcexcel:');
			});
		},
		keyEventInputboxFunc:function(e, code, that, obj){

			var id = $(obj).attr("orizinid");

			// *** Enter or Tab ****
			if(code === 13 || code === 9){
				// 商品コード
				if(id===$.id_inp.txt_shncd){
					var value = $.getInputboxValue($('#'+id));
					if(!$.isEmptyVal(value)){
						if(value.length < 8 ){
							value = ('00000000'+value).substr(-8);
							$.setInputboxValue($('#'+id), value);
						}else if(value.length > 8 ){
							value = value.substr(0, 8);
							$.setInputboxValue($('#'+id), value);
						}
						// 検索ボタン押下
						$('#'+$.id.btn_search).trigger('click');
						e.preventDefault();
						return false;
					}
				}
			}
		}
	} });
})(jQuery);