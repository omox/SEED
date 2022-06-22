/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_ReportTG020',			// （必須）レポートオプションの確認
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
		dedefaultObjNum:	12,	// 初期化オブジェクト数
		initObjNum:	-1,
		initedObject: [],
		maxMergeCell: 1,
		onChangeFlag : false,
		onChangeFlag2 : false,
		columnName:'',	// OnClickRowの列名
		initqueried : false,
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
		editRowIndex:{},					// グリッド編集行
		baseData:[],						// 検索結果保持用
		tenData:[],							// 店運用区分
		grd_data:[],						// メイン情報：
		cmnParam:{},						// 基本パラメータ
		nndtData:[],						// 納入日基本データ
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

			// 正処理を実行するためにフラグ変更
			that.onChangeReport = true;

			var isUpdateReport = true;
			// 入力テキストボックス系
			var inputbox = Object.getOwnPropertyNames($.id_inp);
			for ( var sel in inputbox ) {
				if($('#'+$.id_inp[inputbox[sel]]).length > 0){
					$.setInputbox(that, reportno, $.id_inp[inputbox[sel]], isUpdateReport);
				}
			}
			$.setInputbox(that, reportno, $.id_inp.txt_shnkbn + '_hid', isUpdateReport);

			// 初期化終了
			this.initializes =! this.initializes;

			// キーイベントの設定
			$.initKeyEvent(that);

			// ログ出力
			$.log(that.timeData, 'initialize:');
		},
		searched_initialize: function (reportno, opts){	// 検索結果を受けての初期化
			var that = this;

			var obj = $('#'+$.id_inp.txt_tenhtsu_arr);
			that.changeInputboxFunc(that, $.id_inp.txt_tenhtsu_arr, $.getInputboxValue(obj), obj);
			// ログ出力
			$.log(that.timeData, 'searched_initialize:');
		},
		judgeRepType: {
			toktg			: false,	// アンケート有
			toksp			: false,	// アンケート無

			ref				: false		// 参照
		},
		repgrpInfo: {
			TG017:{idx:1},		// 特売・スポット計画 新規・変更
			TG017_1:{idx:2},	// 特売・スポット計画 参照
			ST022:{idx:3},		// 特売・スポット計画 CSV取込
			ST024:{idx:4},		// 特売・スポット計画 店一括数量CSV取込
			ST016:{idx:5},		// 特売・スポット計画 商品一覧
			ST024:{idx:6},		// 特売・スポット計画 CSV取込
			ST019:{idx:7},		// 特売・スポット計画 コピー元商品選択
			TG016:{idx:8}		// 月間販売計画 商品情報
		},
		setInitObjectState: function(){	// 画面初期化時の項目制御
			var that = this;
			// 引き継ぎ情報セット
			$.reg.search = true;	// 当画面ではヘッダー情報のため、検索は常に行う
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

			// 画面情報
			var repstatesBef = $.getJSONObject(that.jsonHidden, "repinfo");
			var repstates = [];
			if(repstatesBef){ repstates = repstates.concat(repstatesBef.value);}
			for (var i = 0; i < repstates.length; i++) {
				if(repstates[i].id===that.name && repstates[i].value){
					if(repstates[i].value.SENDBTNID){
						that.sendBtnid = repstates[i].value.SENDBTNID;
					}
					if(repstates[i].value.PUSHBTNID){
						that.pushBtnid = repstates[i].value.PUSHBTNID;
					}
					break;
				}
			}

			// 当帳票を「参照」で開いた場合
			if(that.reportYobiInfo()==='1'){
				$.setInputBoxDisable($('#'+$.id.btn_new+1)).hide();
				$.setInputBoxDisable($('#'+$.id.btn_copy)).hide();
				$.setInputBoxDisable($('#'+$.id.btn_new+2)).hide();
				$.setInputBoxDisable($('#'+$.id.btn_select)).hide();
				$.setInputBoxDisable($('#'+$.id.btn_csv+1)).hide();

				that.judgeRepType.ST016_ref = true;
				$.initReportInfo("ST016", "特売・スポット計画　商品一覧（参照）", "参照");
			}else{
				that.judgeRepType.ST016_sei = true;
				$.initReportInfo("TG020", "特売・スポット計画　店別数量訂正", "");
			}

			// 各種遷移ボタン
			$('#'+$.id.btn_cancel).on("click", $.pushChangeReport);
		},
		initCondition: function (){	// 条件初期値セット
			var that = this;
			// 初期化項目
		},
		clear:function(){
			// 隠し情報初期化
			$($.id.hiddenChangedIdx).val("");	// 変更行Index
		},
		validation: function (btnId){	// （必須）批准
			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform).form('validate');
			// 入力エラーなしの場合に検索条件を格納
			if (rt || (!rt && !that.initqueried)){
				that.jsonString = that.jsonTemp.slice(0);
				rt=true;
			}
			// 入力チェック用の配列をクリア
			that.jsonTemp = [];
			return rt;
		},
		success: function(reportno, sortable, btnId){	// （必須）正処理
			if (sortable) sortable=1; else sortable=0;
			var that = this;
			// 検索実行
			var szMoyskbn	= $.getJSONObject(this.jsonString, $.id_inp.txt_moyskbn).value;		// 催し区分
			var szMoysstdt	= $.getJSONObject(this.jsonString, $.id_inp.txt_moysstdt).value;	// 催しコード（催し開始日）
			var szMoysrban	= $.getJSONObject(this.jsonString, $.id_inp.txt_moysrban).value;	// 催し連番
			var szShncd		= $.getInputboxValue($('#'+$.id_inp.txt_shncd));
			var szNndt		= $.getInputboxValue($('#'+$.id_inp.txt_nndt));
			var szBmncd		= $.getInputboxValue($('#'+$.id_inp.txt_bmncd));
			var szShnkbn	= $.getInputboxValue($('#'+$.id_inp.txt_shnkbn + '_hid'));
			var szTenCd		= $.getInputboxValue($('#'+$.id_inp.txt_tencd));

			// 処理時間計測用
			that.timeData = (new Date()).getTime();

			// 最初回は、ヘッダー情報取得のための自動検索のため、一覧検索を行わない
			if(that.initqueried){
				that.pushBtnid = btnId;
			}

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,		// レポート名
					SENDBTNID:		that.sendBtnid,
					PUSHBTNID:		that.pushBtnid,
					MOYSKBN:		szMoyskbn,		// 催し区分
					MOYSSTDT:		szMoysstdt,		// 催しコード（催し開始日）
					MOYSRBAN:		szMoysrban,		// 催し連番
					SHNCD:			szShncd,		// 商品コード
					NNDT:			szNndt,			// 納入日
					BMNCD:			szBmncd,		// 部門
					SHNKBN:			szShnkbn,		// 商品区分
					TENCD:			szTenCd,		// 店コード
					sortable:		sortable,
					sortName:		that.sortName,
					sortOrder:		that.sortOrder,
					rows:			1,	// 表示可能レコード数
					t:				(new Date()).getTime()
				},
				function(json){
					// 検索処理エラー判定
					if($.searchError(json)) return false;

					// ログ出力
					$.log(that.timeData, 'query:');

					var total = JSON.parse(json).total;
					if (total!==1 && that.initqueried) {
						that.clearAndDisableFunc(false);
						if (total == 0) {
							$.showMessage('E20038');
						} else {
							$.showMessage('E20610');
						}
					} else {
						var opts = JSON.parse(json).opts
						var rows = JSON.parse(json).rows;
						if(opts){
							if (opts.rows_) {
								// 基本データ表示
								that.setData(opts.rows_, opts, '', 'F5');
								that.baseData = opts.rows_;
							}

							// 検索結果
							if (JSON.parse(json).rows) {
								that.setData(JSON.parse(json).rows, opts, 'F5', '');
							}

							// 店運用区分
							if (opts.rows_ten) {
								that.tenData = opts.rows_ten;
							}
						}

						if(that.initqueried){

							// 情報設定
							that.cmnParam = {
								MOYSKBN:	szMoyskbn,			// 催し区分
								MOYSSTDT:	szMoysstdt,			// 催し区分
								MOYSRBAN:	szMoysrban,			// 催し連番
								BMNCD:		szBmncd,			// 部門コード
								KANRINO:	rows[0]["F6"],		// 管理No.
								KANRIENO:	rows[0]["F7"],		// 管理No.枝番
								NNDT:		szNndt,				// 納入日
								SHNCD:		szShncd,			// 商品
								BINKBN:		$.getInputboxValue($('#'+$.id_inp.txt_binkbn)),	// 便区分
								SHUDENFLG:	rows[0]["F16"]		// 週次伝送フラグ
							};

							that.nndtData = {
								MOYSKBN			: szMoyskbn,		// MOYSKBN	催し区分
								MOYSSTDT		: szMoysstdt,		// MOYSSTDT	催し開始日
								MOYSRBAN		: szMoysrban,		// MOYSRBAN	催し連番
								BMNCD			: szBmncd,			// BMNCD	部門
								KANRINO			: rows[0]["F6"],	// KANRINO	管理番号
								KANRIENO		: rows[0]["F7"],	// KANRIENO	枝番
								NNDT			: szNndt,			// NNDT	納入日
								TENHTSU_ARR		: rows[0]["F5"],	// TENHTSU_ARR	店発注数配列
								TENCHGFLG_ARR	: rows[0]["F8"],	// TENCHGFLG_ARR	店変更フラグ配列
								HTASU			: rows[0]["F9"],	// HTASU	発注総数
								PTNNO			: rows[0]["F10"],	// PTNNO	パターン№
								TSEIKBN			: rows[0]["F11"],	// TSEIKBN	訂正区分
								TPSU			: rows[0]["F12"],	// TPSU	店舗数
								TENKAISU		: rows[0]["F13"],	// TENKAISU	展開数
								ZJSKFLG			: rows[0]["F14"],	// ZJSKFLG	前年実績フラグ
								WEEKHTDT		: rows[0]["F15"],	// WEEKHTDT	週間発注処理日
								UPDDT			: rows[0]["F17"],	// UPDDT	更新日
							};
						}

						// 検索結果をうけての子テーブルマスタ項目などの初期化設定
						that.searched_initialize(reportno, opts);
					}

					$.removeMask();
					$.removeMaskMsg();

					// ログ出力
					$.log(that.timeData, 'loaded:');
				}
			);
		},
		getGridData: function (target){
			var that = this;

			var data = {};

			var moyskbn  = that.cmnParam.MOYSKBN;
			var moysstdt = that.cmnParam.MOYSSTDT;
			var moysrban = that.cmnParam.MOYSRBAN;
			var bmncd    = that.cmnParam.BMNCD;
			var kanrino  = that.cmnParam.KANRINO;
			var kanrieno = that.cmnParam.KANRIENO;
			var nndt     = that.cmnParam.NNDT;
			var shncd    = that.cmnParam.SHNCD;
			var binkbn   = that.cmnParam.BINKBN;

			// 基本情報
			if(target===undefined || target==="grd_data"){
				var targetData = [];

				// 配列作成
				var len = 5;
				var tenhtsu_arrs = [];			// TENHTSU_ARR		店発注数配列
				var tenchgflg_arrs = "";		// TENCHGFLG_ARR	店変更フラグ配列

				if (!$.isEmptyVal(that.nndtData["TENCHGFLG_ARR"]) && that.nndtData["TENCHGFLG_ARR"].split("").length !== 0) {
					tenchgflg_arrs = that.nndtData["TENCHGFLG_ARR"].split("");
				}

				if (!$.isEmptyVal(that.nndtData["TENHTSU_ARR"]) && that.nndtData["TENHTSU_ARR"].split("").length !== 0) {
					var digit = 0;
					var tenhtsu = $.getInputboxValue($('#'+$.id_inp.txt_htsu));
					var ten = $.getInputboxValue($('#'+$.id_inp.txt_tencd));
					for (var i = 0; i < 400; i++) {
						if (ten*1===(i+1)) {
							tenhtsu_arrs[i] = $.isEmptyVal(tenhtsu) ? '     ':('00000' + tenhtsu).slice(-5);
							tenchgflg_arrs[i] = "1"
						} else {
							tenhtsu_arrs[i] = that.nndtData["TENHTSU_ARR"].slice(digit,digit+5);
						}
						digit+=5;
					}
				}

				var tenhtsu_arr = "";
				var tenchgflg_arr = "";

				if (!$.isEmptyVal(tenhtsu_arrs)) {
					tenhtsu_arr = tenhtsu_arrs.join("");
				}
				if (!$.isEmptyVal(tenchgflg_arrs)) {
					tenchgflg_arr = tenchgflg_arrs.join("");
				}

				// 2.2.5.1．登録内容：
				// 全店特売(アンケート有/無)_納入日.店発注数配列
				// 全店特売(アンケート有/無)_納入日.店変更フラグ配列（本画面で発注数を修正した店舗のみ1をUPDATE（注意：更新処理前の配列を取得し、それに対して更新店のみ1をセットする事））
				var targetRow = that.nndtData;
				var rowData = null;

				// 催し種類判断：全店特売（アンケート有(チラシのみ/販売・納入)/無）
				if(moyskbn !== $.id.value_moykbn_t || moysrban < 50){
					that.judgeRepType.toksp = true;
				}else{
					that.judgeRepType.toktg = true;
				}

				if(that.judgeRepType.toktg){
					rowData = {
						F1 : ""+moyskbn,			// F1	MOYSKBN	催し区分
						F2 : ""+moysstdt,			// F2	MOYSSTDT	催し開始日
						F3 : ""+moysrban,			// F3	MOYSRBAN	催し連番
						F4 : ""+bmncd,				// F4	BMNCD	部門
						F5 : ""+kanrino,			// F5	KANRINO	管理番号
						F6 : ""+kanrieno,			// F6	KANRIENO	枝番
						F7 : ""+nndt,				// F7	NNDT		納入日
						F8 : tenhtsu_arr,			// F8	TENHTSU_ARR	店発注数配列
						F9 : tenchgflg_arr,			// F9	TENCHGFLG_ARR	店変更フラグ配列
						F10: targetRow.HTASU,		// F10	HTASU		発注総数
						F11: targetRow.PTNNO,		// F11	PTNNO		パターン№
						F12: targetRow.TSEIKBN,		// F12	TSEIKBN		訂正区分
						F13: targetRow.TPSU,		// F13	TPSU		店舗数
						F14: targetRow.TENKAISU,	// F14	TENKAISU	展開数
						F15: targetRow.ZJSKFLG,		// F15	ZJSKFLG		前年実績フラグ
						F16: targetRow.WEEKHTDT,	// F16	WEEKHTDT	週間発注処理日
						F20: targetRow.UPDDT,
					};
					targetData.push(rowData);
				}else{
					rowData = {
						F1 : ""+moyskbn,			// F1	MOYSKBN	催し区分
						F2 : ""+moysstdt,			// F2	MOYSSTDT	催し開始日
						F3 : ""+moysrban,			// F3	MOYSRBAN	催し連番
						F4 : ""+bmncd,				// F4	BMNCD	部門
						F5 : ""+kanrino,			// F5	KANRINO	管理番号
						F6 : ""+kanrieno,			// F6	KANRIENO	枝番
						F7 : ""+nndt,				// F7	NNDT		納入日
						F8 : tenhtsu_arr,			// F8	TENHTSU_ARR	店発注数配列
						F9 : targetRow.HTASU,		// F9	HTASU	発注総数
						F10: targetRow.PTNNO,		// F10	PTNNO	パターン№
						F11: targetRow.TSEIKBN,		// F11	TSEIKBN	訂正区分
						F12: targetRow.TPSU,		// F12	TPSU	店舗数
						F13: targetRow.TENKAISU,	// F13	TENKAISU	展開数
						F14: targetRow.ZJSKFLG,		// F14	ZJSKFLG	前年実績フラグ
						F15: targetRow.WEEKHTDT,	// F15	WEEKHTDT	週間発注処理日
						F19: targetRow.UPDDT,
						F20: ""+shncd,
						F21: ""+binkbn
					};
					targetData.push(rowData);
				}
				data["grd_data"] = targetData;
			}

			return data;
		},
		updValidation: function (id){	// （必須）批准
			var that = this;
			var errMsg= "";

			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform).form('validate');
			if(!rt){
				$.addErrState(that, $('.validatebox-invalid').eq(0), false);
				return rt;
			}

			// ①店舗基本マスタ.店運用区分=9の場合、その店の発注数<>NULLでエラー。
			// ②店舗基本マスタにデータ更新区分=0のレコードが無い場合、その店の発注数<>NULLでエラー。
			// ③週間発注済チェック：
			// 全店特売（アンケート有・無）_商品.週間発注処理日<>NULLの場合かつTG016の【画面】週次伝送=1 AND 店別数量を変更の場合：「事前発注が済んでいる為に変更出来ません。」（禁止）を表示し、処理を中止する。
			// ④1店舗も数量>=0の店舗がない場合はエラー
			if(that.tenData[0]['CNT']==='0'){
				// ①②E20521	廃店は入力できません。	 	0	 	E
				var target = $('#'+$.id_inp.txt_tencd);
				$.showMessage('E20521', undefined, function(){$.addErrState(that, target, true)});
				return false;
			}

			if(!$.isEmptyVal(that.nndtData.WEEKHTDT) && that.cmnParam.SHUDENFLG===$.id.value_on){
				// ③E20541	事前発注が済んでいる為に変更できません。	 	0	 	E
				$.showMessage('E20541');
				return false;
			}

			var tenhtsu = $.getInputboxValue($('#'+$.id_inp.txt_htsu));
			var inputExsitsFlg = false;
			if($.isEmptyVal(tenhtsu)){
				if (!$.isEmptyVal(that.nndtData["TENHTSU_ARR"]) && that.nndtData["TENHTSU_ARR"].split("").length !== 0) {
					var digit = 0;
					var ten = $.getInputboxValue($('#'+$.id_inp.txt_tencd));
					for (var i = 0; i < 400; i++) {
						if (!(ten*1===(i+1)) && !$.isEmptyVal(that.nndtData["TENHTSU_ARR"].slice(digit,digit+5).trim())) {
							inputExsitsFlg = true;
							break;
						}
						digit+=5;
					}
				}
			} else {
				inputExsitsFlg = true;
			}

			if(!inputExsitsFlg){
				// ④E20550	数量 ≧ 0の店舗が存在しない為、登録できません。	 	0	 	E
				$.showMessage('E20550');
				return false;
			}

			// 入力情報を変数に格納
			if (rt == true) that.setGridData(that.getGridData());
			return rt;
		},
		setGridData: function (data, target, delFlg){
			var that = this;

			// 基本データ
			if(target===undefined || target==="grd_data"){
				that.grd_data =  data["grd_data"];
			}

			return true;
		},
		updSuccess: function(id){	// validation OK時 の update処理
			var that = this;

			// 基本情報
			var targetData = that.grd_data;

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$.appendMaskMsg();
			$.appendMask();

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,						// レポート名
					action:			$.id.action_update,				// 実行処理情報
					obj:			id,								// 実行オブジェクト
					SENDBTNID:		that.sendBtnid,
					DATA:			JSON.stringify(targetData),		// 更新対象情報
					t:				(new Date()).getTime()
				},
				function(data){
					// 検索処理エラー判定
					if($.updError(id, data)) return false;


					var afterFunc = function(){
						// 初期化
						that.getEasyUI();
						that.jsonString = that.jsonTemp.slice(0);
						that.clearAndDisableFunc(true);
						that.clear();
					};
					$.updNormal(data, afterFunc);

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

			// *** 引継情報 ***
			// 催し区分
			this.jsonTemp.push({
				id:		$.id_inp.txt_moyskbn,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_moyskbn),
				text:	''
			});
			// 催しコード（催し開始日）
			this.jsonTemp.push({
				id:		$.id_inp.txt_moysstdt,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_moysstdt),
				text:	''
			});
			// 催し連番
			this.jsonTemp.push({
				id:		$.id_inp.txt_moysrban,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_moysrban),
				text:	''
			});

		},
		setData: function(rows, opts, startCol, endCol){		// データ表示
			var that = this;
			if(rows.length > 0){
				var count = 0;
				$('#'+that.focusRootId).find('[col^=F]').each(function(){
					var col = $(this).attr('col');
					if (!$.isEmptyVal(endCol)) {
						if (col===endCol) {
							count = -1;
						}
					}
					if (!$.isEmptyVal(startCol)) {
						if (col===startCol || count!==0) {
							count += 1;
						}
						col = 'F' + count;
					}
					if(count!==-1 && rows[0][col]){
						$.setInputboxValue($(this), rows[0][col]);
					}
				});
			}
		},
		setObjectState: function(){	// 軸の選択内容による制御

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
		changeInputboxFunc:function(that, id, newValue, obj){
			var that = this;
			var parentObj = $('#'+that.focusRootId);
			if(id+"_"===obj.attr('id') && that.focusGridId!==""){
				parentObj = $('#'+that.focusGridId).datagrid('getPanel');
			}

			// DB問い合わせ系
			if($('[for_inp^='+id+'_]').length > 0){
				var param = that.getInputboxParams(that, id, newValue);
				var rows = $.getSelectListData(that.name, $.id.action_change, id, param);
				var row = rows.length > 0 ? rows[0]:"";
				$.setInputboxRowData('for_inp', id, row, that, parentObj);
			}

			if (id===$.id_inp.txt_tenhtsu_arr) {
				$.setInputboxValue($('#'+id), '');
			}

			// 検索条件がクリアされた場合
			if (id===$.id_inp.txt_nndt ||
					id===$.id_inp.txt_tencd ||
					id===$.id_inp.txt_shncd
			) {
				if ($("#"+$.id.btn_upd).linkbutton('options').disabled)	return false;
				that.clearAndDisableFunc(false);
			}

			// TODO: 背景を赤くする対応を追加
			var msgid = that.checkInputboxFunc(id,newValue);
			if(msgid !==null){
				$.showMessage(msgid);
				return false;
			}
		},
		getInputboxParams: function(that, id, newValue){
			// 情報取得
			var values = {};
			values["value"] = newValue;
			if (id===$.id_inp.txt_tenhtsu_arr) {
				values["tencd"] = $.getInputboxValue($('#'+$.id_inp.txt_tencd));
			}

			// 情報設定
			return [values];
		},
		// IDとvalueでチェック処理を実施
		checkInputboxFunc:function(id, newValue, kbn, record, isNew){
			var that = this;

			// 商品コード
			if(id===$.id_inp.txt_shncd){
				if(newValue !== '' && newValue){
					// 商品コード
					if(newValue.length < 8){
						return "EX1006";
					}

					var param = {};
					param["KEY"] =  "MST_CNT";
					param["value"] = newValue;
					var chk_cnt = $.getInputboxData(that.name, $.id.action_check, id, [param]);
					if(chk_cnt==="" || chk_cnt==="0"){
						return "E11046";
					}
				}
			}

			// 発注数
			if(id===$.id_inp.txt_tenhtsu_arr){
				if (!that.initqueried) {
					that.initqueried = true;
					that.queried = true;
					$($.id.hiddenChangedIdx).val("");	// 変更行Index
					$.setInputBoxDisableVariable($("#"+$.id.btn_upd),true);
				} else {
					if ($.isEmptyVal($.getInputboxValue($('#'+$.id_inp.txt_htsu)))) {
						that.clearAndDisableFunc(true);
						return "E20611";
					} else {
						$.setInputBoxEnableVariable($("#"+$.id.btn_upd));
					}
				}
			}
			return null;
		},
		clearAndDisableFunc:function(tencd) {
			var that = this;

			$.setInputboxValue($('#'+that.focusRootId).find('[col^=F5]'), '');
			$.setInputboxValue($('#'+that.focusRootId).find('[col^=F6]'), '');
			$.setInputboxValue($('#'+that.focusRootId).find('[col^=F7]'), '');
			$.setInputboxValue($('#'+that.focusRootId).find('[col^=F8]'), '');
			$.setInputboxValue($('#'+that.focusRootId).find('[col^=F9]'), '');
			$.setInputboxValue($('#'+$.id_inp.txt_htsu), '');
			$.setInputBoxDisableVariable($("#"+$.id.btn_upd),true);

			if (tencd) {
				var target = $('#'+$.id_inp.txt_tencd)
				$.setInputboxValue(target, '');
				target = $.getInputboxTextbox(target);
				target.focus();
			}
		},
		changeReport:function(reportno, btnId){				// 画面遷移
			var that = this;

			// 遷移判定
			var index = 0;
			var childurl = "";
			var sendMode = "";		// 遷移後に、前回検索条件を表示したい場合、""のまま、ここで設定した条件を表示したい場合"1"

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

			// 呼出別処理
			switch (btnId) {
			case $.id.btn_cancel:
			case $.id.btn_back:
				// 転送先情報
				index = 1;
				childurl = href[index];
				sendMode = 2;
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