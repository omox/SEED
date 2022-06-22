/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_ReportTG003',			// （必須）レポートオプションの確認
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
		dedefaultObjNum:	8,	// 初期化オブジェクト数
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
		editRowIndex:{},					// グリッド編集行保持

		baseTablekbn:"",					// 検索結果のテーブル区分：0-正/1-予約(※予約1の新規→正を参照しているので正、予約2の新規→予約1を参照しているので予)
		baseData:[],						// 検索結果保持用
		subData:[],							// 検索結果保持用(グリッド情報)

		grd_data:[],						// メイン情報：商品マスタ
		grd_tencd_data:[],					// グリッド情報：店コード
		initialize: function (reportno){	// （必須）初期化
			var that = this;
			// 引き継ぎ情報
			this.jsonHidden = $.getTargetValue();
			// 画面の初回基本設定
			this.setInitObjectState();
			// 初期検索条件設定
			this.jsonInit = $.getInitValue();
			// 処理日付取得
			$.getsetInputboxData(reportno, $.id.txt_shoridt, [{}], $.id.action_init);

			// 初期化するオブジェクト数設定
			this.initObjNum = this.dedefaultObjNum;
			// 検索実行
			that.onChangeReport = true;

			var isUpdateReport = true;

			// 入力テキストボックス系
			var inputbox = Object.getOwnPropertyNames($.id_inp);
			for ( var sel in inputbox ) {
				if($('#'+$.id_inp[inputbox[sel]]).length > 0){
					$.setInputbox(that, reportno, $.id_inp[inputbox[sel]], isUpdateReport);
				}
			}
			$.setInputbox(that, reportno, $.id_inp.txt_tencd+"leader", isUpdateReport);
			$.setRadioInit2(that.jsonHidden, $.id.rad_qasyukbn, that)

			// Load処理回避
			//$.tryChangeURL(null);

			// 初期化終了
			this.initializes =! this.initializes;

			// ログ出力
			$.log(that.timeData, 'initialize:');
		},
		searched_initialize: function (reportno, opts){	// 検索結果を受けての初期化
			var that = this;

			// 強制グループチェック
			if($("[col=F4]").text().length > 0){
				$("#tbl_qacredt").show();
				that.judgeRepType.kyosei = true;
			}else{
				// 非表示化
				$("#tbl_qacredt").find("[tabindex]").filter("[tabindex!=-1]").filter('[disabled!=disabled]').each(function(){
					$.setInputBoxDisable($(this));
				});
			}

			// ***個別データグリッド設定
			// 店コード
			if(!that.judgeRepType.ref){
				// 編集可能データグリッドの共通処理設定
				// 編集エディターの機能拡張（非表示で参照用のオブジェクトが準備してある前提、参照オブジェクトはグリッドのフィールド名を元に判断）
				$.extendDatagridEditor(that);
			}
			that.setGrid(that, reportno, $.id.grd_tenpo);

			// チェックボックスの設定
			$.initCheckboxCss($("#"+that.focusRootId));
			// キーイベントの設定
			$.initKeyEvent(that);

			that.queried = true;

			// ログ出力
			$.log(that.timeData, 'searched_initialize:');
		},
		judgeRepType: {
			ins 		: false,	// 新規
			upd 		: false,	// 更新
			ref 		: false,	// 参照

			kyosei		: false		// 強制グループ
		},
		repgrpInfo: {
			TG001:{idx:1},		// 月間販売計画 新規・変更
			TG001_1:{idx:2},	// 月間販売計画 参照
			TG002:{idx:3},		// 月間販売計画 店舗グループ一覧
			TG003:{idx:4},		// 月間販売計画 店舗グループ店情報
			TG008:{idx:5},		// 月間販売計画 商品一覧
			TG040:{idx:6},		// 月間販売計画 コピー元店舗グループ一覧
			TG016:{idx:7}		// 商品情報
		},
		setInitObjectState: function(){	// 画面初期化時の項目制御
			var that = this;
			// 引き継ぎ情報セット
			var sendBtnid = $.getJSONValue(that.jsonHidden, "sendBtnid");
			if(!sendBtnid){
				sendBtnid = $('#sendBtnid').val();
			}
			// 呼出し画面情報
			var repstatesBef = $.getJSONObject(that.jsonHidden, "repinfo");
			var repstates = [];
			if(repstatesBef){ repstates = repstates.concat(repstatesBef.value);}
			for (var i = 0; i < repstates.length; i++) {
				if(repstates[i].id===that.name && repstates[i].value && repstates[i].value.SENDBTNID){
					sendBtnid = repstates[i].value.SENDBTNID;
					break;
				}
			}
			$('#sendBtnid').val(sendBtnid);
			that.sendBtnid = sendBtnid;

			var reportYobi1 = $.getJSONValue(that.jsonHidden, "reportYobi1");
			if(!reportYobi1){
				reportYobi1 = $('#reportYobi1').val();
			}
			$('#reportYobi1').val(reportYobi1);

			// 帳票タイプ判断：ボタン情報と予備情報のみで判断
			if(that.sendBtnid===$.id.btn_new||that.sendBtnid===$.id.btn_new+2){
				that.judgeRepType.ins = true;
			}else if(that.sendBtnid===$.id.btn_sel_change && that.reportYobiInfo()!=='1'){
				that.judgeRepType.upd = true;
			}else{
				that.judgeRepType.ref = true;
			}

			var hidobjids = [];	// 非表示
			$("#"+$.id.btn_cancel).on("click", $.pushChangeReport);
			// 新規：
			if(that.judgeRepType.ins){
				hidobjids = hidobjids.concat([$.id.btn_del]);
				$("#disp_record_info").hide();
				$.setInputboxValue($('[name='+$.id.rad_qasyukbn+"]"),'1');

				$.initReportInfo("TG003", "月間販売計画（チラシ計画）　店舗グループ　店情報", "新規");
			// 変更：
			}else if(that.judgeRepType.upd){
				var btn_txt = "更新";
				$("#"+$.id.btn_upd).attr("title", btn_txt).linkbutton({text:btn_txt});
				$.setInputBoxDisable($("#"+$.id_inp.txt_tengpcd));
				$.setInputBoxDisable($("#"+$.id_inp.txt_tengpkn));

				$.initReportInfo("TG003", "月間販売計画（チラシ計画）　店舗グループ　店情報", "変更");
			// 参照：
			}else if(that.judgeRepType.ref){
				hidobjids = hidobjids.concat([$.id.btn_cancel,$.id.btn_upd,$.id.btn_del]);
				$.setInputBoxDisable($($.id.hiddenChangedIdx));

				$.initReportInfo("TG003", "月間販売計画（チラシ計画）　店舗グループ　店情報", "参照");
			}
			// 非表示化
			for (var i = 0; i < hidobjids.length; i++) {
				$.setInputBoxDisable($('#'+hidobjids[i])).hide();
			}
			$($.id.buttons).show();
			// 変更
			$($.id.hiddenChangedIdx).val('');
		},
		clear:function(){
			// 隠し情報初期化
			$($.id.hiddenChangedIdx).val("");						// 変更行Index
		},
		validation: function (btnId){	// （必須）批准
			var that = this;
			var rt = true;
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
			var szMoyskbn	= $.getJSONObject(this.jsonString, $.id_inp.txt_moyskbn).value;		// 催し区分
			var szMoysstdt	= $.getJSONObject(this.jsonString, $.id_inp.txt_moysstdt).value;	// 催しコード（催し開始日）
			var szMoysrban	= $.getJSONObject(this.jsonString, $.id_inp.txt_moysrban).value;	// 催し連番
			var szTengpcd	= $.getJSONObject(this.jsonString, $.id_inp.txt_tengpcd).value;		// 店グループ

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$.appendMaskMsg();

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,		// レポート名
					MOYSKBN:		szMoyskbn,		// 催し区分
					MOYSSTDT:		szMoysstdt,		// 催しコード（催し開始日）
					MOYSRBAN:		szMoysrban,		// 催し連番
					TENGPCD:		szTengpcd,		// 店グループ
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

					if(opts && opts.rows_){
						that.subData = opts.rows_;
					}

					// メインデータ表示
					that.setData(that.baseData, opts);

					// 検索結果をうけての子テーブルマスタ項目などの初期化設定
					that.searched_initialize(reportno, opts);

					// 現在情報を変数に格納(追加した情報については個別にロード成功時に実施)
					that.setGridData(that.getGridData("grd_data"), "grd_data");

					// 隠し情報初期化
					$($.id.hiddenChangedIdx).val("");						// 変更行Index

					// 状態保存
					$.saveState2(reportno, that.getJSONString());

					// ログ出力
					$.log(that.timeData, 'loaded:');
				}
			);
		},
		getGridData: function (target){
			var that = this;
			var data = {};

			var szMoyskbn	= $.getJSONObject(this.jsonString, $.id_inp.txt_moyskbn).value;		// 催し区分
			var szMoysstdt	= $.getJSONObject(this.jsonString, $.id_inp.txt_moysstdt).value;	// 催しコード（催し開始日）
			var szMoysrban	= $.getJSONObject(this.jsonString, $.id_inp.txt_moysrban).value;	// 催し連番
			var tengpcd		= $.getInputboxValue($('#'+$.id_inp.txt_tengpcd));					// 店グループ
			var kyoseiflg	= that.judgeRepType.kyosei?$.id.value_on:$.id.value_off;			// 強制フラグ

			// 基本情報
			if(target===undefined || target==="grd_data"){
				var targetData = [];
				var rowData = {
					F1 : szMoyskbn,				// F1 : 催し区分	MOYSKBN
					F2 : szMoysstdt,			// F2 : 催し開始日	MOYSSTDT
					F3 : szMoysrban,			// F3 : 催し連番	MOYSRBAN
					F4 : tengpcd,				// F4	TENGPCD	店グループ
					F5 : $.getInputboxValue($('#'+$.id_inp.txt_tengpkn)),			// F5	TENGPKN	店グループ名称
					F6 : kyoseiflg,				// F6	KYOSEIFLG	強制グループフラグ
					F7 : $.getInputboxValue($('[name='+$.id.rad_qasyukbn+"]")),		// F7	QASYUKBN	アンケート種類
					F8 : $.getInputboxValue($('#'+$.id_inp.txt_qacredt)),			// F8	QACREDT_K	アンケート作成日_強制
					F9 : $.getInputboxValue($('#'+$.id_inp.txt_qarcredt)),			// F9	QARCREDT_K	アンケート再作成日_強制
					F14: $.getInputboxValue($('#hiddenUpddt')),						// F14	UPDDT	更新日
				};
				targetData.push(rowData);
				data["grd_data"] = targetData;
			}

			// 店コード
			if(target===undefined || target===$.id.grd_tenpo){
				var targetRowsTen= [];
				var rowsTen= $('#'+$.id.grd_tenpo).datagrid('getRows');
				var tencd_l 	= $.getInputboxValue($("#"+$.id_inp.txt_tencd+"leader"));			// リーダー店

				for (var i=0; i<rowsTen.length; i++){
					if(!$.isEmptyVal(rowsTen[i]["TENCD"])){
						var ldtenkbn = tencd_l===rowsTen[i]["TENCD"]?$.id.value_on:$.id.value_off;
						var rowData = {
							F1 : szMoyskbn,					// F1 : 催し区分	MOYSKBN
							F2 : szMoysstdt,				// F2 : 催し開始日	MOYSSTDT
							F3 : szMoysrban,				// F3 : 催し連番	MOYSRBAN
							F4 : rowsTen[i]["TENCD"],		// F4 : 店コード	TENCD
							F5 : kyoseiflg,					// F5 : 強制グループフラグ	KYOSEIFLG
							F6 : tengpcd,					// F6 : 店グループ	TENGPCD
							F7 : ldtenkbn					// F7 : リーダー店区分	LDTENKBN
							,RNO:i
						};
						targetRowsTen.push(rowData);
					}
				}
				data[$.id.grd_tenpo] = targetRowsTen;
			}

			return data;
		},
		setGridData: function (data, target){
			var that = this;

			// 基本データ
			if(target===undefined || target==="grd_data"){
				that.grd_data =  data["grd_data"];
			}

			// 店コード
			if(target===undefined || target===$.id.grd_tenpo){
				that.grd_tencd_data =  data[$.id.grd_tenpo];
			}
			return true;
		},
		updValidation: function (id){	// （必須）批准
			var that = this;
			var errMsg= "";

			// 新規(正)：新規・新規コピー・選択コピーボタン押下時
			var isNew = that.judgeRepType.ins;
			// 変更(正)：検索・変更・正ボタン押下時
			var isChange = that.judgeRepType.upd;

			var login_dt = parent.$('#login_dt').text().replace(/\//g, "");	// 処理日付
			var sysdate = login_dt.substr(2, 6);							// 比較用処理日付

			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform).form('validate');
			if(!rt){
				$.addErrState(that, $('.validatebox-invalid').eq(0), false);
				return rt;
			}

			rt = $.endEditingDatagrid(that);
			if(!rt){
				$.showMessage('E00001');
				return rt;
			}

			// 入力項目チェック
			var shoridt = $('#'+$.id.txt_shoridt).val();

			var msgid = null, id = null;
			var targetOId = [$.id_inp.txt_tengpcd, $.id_inp.txt_tencd+"leader"];
			for (var j = 0; j < targetOId.length; j++){
				id = targetOId[j];
				msgid = that.checkInputboxFunc(targetOId[j], $.getInputboxValue($('#'+id)));
				if(msgid !==null){
					$.showMessage(msgid, undefined, function(){$.addErrState(that, $('#'+id), true)});
					return false;
				}
			}

			// 現在の画面情報を変数に格納
			var gridData = that.getGridData();	// 検証用情報取得

			var targetRows = gridData[$.id.grd_tenpo];
			var targetOId = [$.id_inp.txt_tencd];
			var targetCId = ["F4"];
			for (var i=0; i<targetRows.length; i++){
				for (var j = 0; j < targetOId.length; j++){
					msgid = that.checkInputboxFunc(targetOId[j], targetRows[i][targetCId[j]]);
					if(msgid !==null){
						$.showMessage(msgid, undefined, function(){$.addErrState(that, $('#'+$.id.grd_tenpo), true, {NO:targetRows[i]["RNO"], ID:targetOId[j]})});
						return false;
					}
				}
			}

			// 全体のチェック
			msgid = that.checkInputboxFunc(undefined, undefined, true);
			if(msgid !==null){
				$.showMessage(msgid);
				return false;
			}

			// 入力情報を変数に格納
			if (rt == true) that.setGridData(gridData);	// 更新用情報取得

			return rt;
		},
		updSuccess: function(id){	// validation OK時 の update処理
			var that = this;

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$.appendMaskMsg();

			// 変更行情報取得
			var changedIndex = $($.id.hiddenChangedIdx).val().split(",");

			// 基本情報
			var targetData = that.grd_data;
			// 店コード
			var targetRowsTencd= that.grd_tencd_data;

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,			// レポート名
					action:			$.id.action_update,	// 実行処理情報
					obj:			id,					// 実行オブジェクト
					SENDBTNID:		that.sendBtnid,
					DATA:			JSON.stringify(targetData),				// 更新対象情報
					DATA_TENCD:		JSON.stringify(targetRowsTencd),		// 個別データグリッド:店コード
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
		delValidation: function (id){	// （必須）批准
			var that = this;
			var rt = true;

			var shoridt = $('#'+$.id.txt_shoridt).val();

			// 3.3.1．「削除」ボタンチェック：
			if(!that.judgeRepType.kyosei){	// ◇通常グループの場合
				var txt_qacredt = $.getParserDt(that.baseData[0]["F11"], false);
				// ◇処理日付<=全特（ア有）_基本.アンケート作成日の場合		常に削除可能
				if(!(shoridt <= txt_qacredt)){
					var txt_qadevstdt = $.getParserDt(that.baseData[0]["F21"], false);
					// ◇全特（ア有）_基本.アンケート作成日<処理日付<全特（ア有）_基本.アンケート取込開始日の場合
					if(txt_qacredt < shoridt && shoridt < txt_qadevstdt){
						var txt_qarcredt = $.getParserDt(that.baseData[0]["F13"], false);
						//   ◇全特（ア有）_基本.アンケート再作成日<>NULLの場合		削除可能
						//   ◇全特（ア有）_基本.アンケート再作成日=NULLの場合		削除ボタンクリック時エラー
						if($.isEmptyVal(txt_qarcredt)){
							// E20577	通常グループでアンケート作成日<処理日付<アンケート取込開始日の場合、	アンケート再作成日が入力されていないと削除できません。	0	 	E
							$.showMessage("E20577");
							return false;
						}
					// ◇全特（ア有）_基本.アンケート取込開始日=<処理日付の場合	削除ボタンクリック時エラー
					}else if(txt_qadevstdt <= shoridt){
						// E20092	事前発注リスト作成済のため、変更できません。	 	0	 	E
						$.showMessage("E20092");
						return false;
					}
				}
			}else{							// ◇強制グループの場合
				var txt_jlstcreflg = that.baseData[0]["F20"];
				// ◇全特（ア有）_基本.事前発注リスト作成済フラグ<>1の場合	常に削除可能
				// ◇上記以外												削除ボタンクリック時エラー
				if(!(txt_jlstcreflg!=="1")){
					// E20092	事前発注リスト作成済のため、変更できません。	 	0	 	E
					$.showMessage("E20092");
					return false;
				}
			}

			// 入力情報を変数に格納
			if (rt == true) that.setGridData(that.getGridData("grd_data"));	// 更新用情報取得

			return rt;
		},
		delSuccess: function(id){	// validation OK時 の update処理
			var that = this;

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$.appendMaskMsg();

			// 変更行情報取得
			var changedIndex = $($.id.hiddenChangedIdx).val().split(",");

			// 基本情報
			var targetData = that.grd_data;

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,			// レポート名
					action:			$.id.action_delete,	// 実行処理情報
					obj:			id,					// 実行オブジェクト
					SENDBTNID:		that.sendBtnid,
					DATA:			JSON.stringify(targetData),				// 更新対象情報
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
		getEasyUI: function(){	// （必須）情報の取得
			// 初期化
			this.jsonTemp = [];

			// レポート名
			this.jsonTemp.push({
				id:		"reportname",
				value:	this.caption(),
				text:	this.caption()
			});
			// *** hidden情報 ***
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
			// 強制フラグ
			this.jsonTemp.push({
				id:		$.id_mei.kbn10610,
				value:	$.getJSONValue(this.jsonHidden, $.id_mei.kbn10610),
				text:	''
			});
			// 店グループ
			this.jsonTemp.push({
				id:		$.id_inp.txt_tengpcd,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_tengpcd),
				text:	''
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
		setGrid: function(that, reportno, id){		// データ表示
			var index = -1;

			var funcClickRow = $.fn.datagrid.defaults.onClickRow;
			var funcBeginEdit= $.fn.datagrid.defaults.onBeginEdit;
			var funcEndEdit= $.fn.datagrid.defaults.onEndEdit;
			var funcAfterEdit= $.fn.datagrid.defaults.onAfterEdit;
			if(!that.judgeRepType.ref){
				that.editRowIndex[id] = -1;
				funcClickRow = function(index,field){$.clickEditableDatagridCell(that,id, index)};
				funcBeginEdit = function(index,row){
					$.beginEditDatagridRow(that,id, index, row)
				};
				funcEndEdit = function(index,row,changes){$.endEditDatagridRow(that, id, index, row)};
				funcAfterEdit = function(index,row,changes){
					var rows = that.getGridData(id)[id];
					$.setInputboxValue($("[col=F9]"), rows.length);
				};
			}

			$('#'+id).datagrid({
				url:$.reg.easy,
				nowrap: true,
				singleSelect:true,
				rownumbers:false,
				fit:true,
				fitColumns:false,	// 指定カラム幅を適用する場合、false 指定。
				frozenColumns:[[]],
				columns:[[
					{field:'TENCD',title:'店コード',width: 70,halign:'center',align:'left',editor:'numberbox'
						,formatter:function(value){return $.getFormatLPad(value, $.len.tencd);}},
					{field:'TENKN',title:'店舗名',width:370,halign:'center',align:'left',editor:{type:'textbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:true}}
						,styler:function(value,row,index){return 'background-color:#f5f5f5;';}}
				]],
				onBeforeLoad:function(param){
					index = -1;
					var json = [{
						MOYSKBN:	$.getJSONObject(that.jsonString, $.id_inp.txt_moyskbn).value,	// 催し区分
						MOYSSTDT:	$.getJSONObject(that.jsonString, $.id_inp.txt_moysstdt).value,	// 催しコード（催し開始日）
						MOYSRBAN:	$.getJSONObject(that.jsonString, $.id_inp.txt_moysrban).value,	// 催し連番
						KYOSEIFLG:	that.judgeRepType.kyosei?$.id.value_on:$.id.value_off,			// 強制フラグ
						TENGPCD :	$.getInputboxValue($("#"+$.id_inp.txt_tengpcd))					// 店グループ
					}];

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
						var gridData = that.getGridData(id);
						that.setGridData(gridData, id);
					}
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

			// 実行ボタン別処理
			switch (btnId) {
			case $.id.btn_back:
			case $.id.btn_cancel:
			case $.id.btn_upd:
			case $.id.btn_del:
				// 転送先情報
				index = that.repgrpInfo.TG002.idx;		// 月間販売計画 店舗グループ一覧
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
		changeInputboxFunc:function(that, id, newValue, obj){

			var parentObj = $('#'+that.focusRootId);
			var func_focus = function(){$.addErrState(that, $('#'+obj.attr('id')), true)};

			// DB問い合わせ系
			if($('[for_inp^='+id+'_]').length > 0){
				var param = that.getInputboxParams(that, id, newValue);
				$.getsetInputboxRowData(that.name, 'for_inp', id, [param], that, parentObj);
			}

			// 検索、入力後特殊処理
			if(that.queried){
				if(that.focusGridId === $.id.grd_tenpo && that.editRowIndex[$.id.grd_tenpo] >= 0){
					var rows = that.getGridData($.id.grd_tenpo)[$.id.grd_tenpo];
					$.setInputboxValue($("[col=F9]"), rows.length);
				}
			}
		},
		// IDとvalueでチェック処理を実施
		checkInputboxFunc:function(id, newValue, isUpdate){
			var that = this;
			var shoridt = $('#'+$.id.txt_shoridt).val();

			// 3.1.1．Aモード（新規）チェック
			// 3.2.1．Bモード（変更）チェック

			// 3.1.1.1．必須入力項目チェック。
			// 3.1.1.2．全店特売（アンケート有）_店グループに入力店グループがすでに存在すると、エラー。
			if(id===$.id_inp.txt_tengpcd && that.judgeRepType.ins){
				var param = that.getInputboxParams(that, id, newValue);
				param["KEY"]		= "REP_CHK_DB";
				var chk_rows = $.getSelectListData(that.name, $.id.action_check, id, [param]);
				// エラーの場合は、エラー情報が返ってくる
				if(chk_rows[0]["ID"]){
					return chk_rows[0]["ID"];
				}
			}

			// 3.1.1.3．リーダー店は画面の店舗リスト部分に存在しない場合、エラー。
			// 3.2.1.2．リーダー店は画面の店舗リスト部分に存在しない場合、エラー。
			if(id===$.id_inp.txt_tencd+"leader"){
				var rows = that.getGridData($.id.grd_tenpo)[$.id.grd_tenpo];
				var isExists = false;
				for (var i=0; i<rows.length; i++){
					if(rows[i]["F4"]===newValue){
						isExists = true;
						break;
					}
				}
				if(!isExists){
					return "E20167";
				}
			}

			if(id===$.id_inp.txt_tencd){
				var rows = that.getGridData($.id.grd_tenpo)[$.id.grd_tenpo];
				var tencds = [];
				for (var i=0; i<rows.length; i++){
					tencds.push(rows[i]["F4"]);
				}
				// 3.1.1.4．画面の店舗リストに同じ店コードが複数ある場合、エラー。
				// 3.2.1.3．画面の店舗リストに同じ店コードが複数ある場合、エラー。
				var tencds_ = tencds.filter(function (element, index, self) { return self.indexOf(element)*1 === index; });
				if(tencds.length !== tencds_.length){
					return "E20024";
				}

				// 3.1.1.5．店コードがすでに当催しのある店グループ（当店グループ以外）に属する場合、エラー。
				// 3.2.1.4．店コードがすでに当催しのある店グループ（当店グループ以外）に属する場合、エラー。
				var param = that.getInputboxParams(that, id, newValue);
				param["KEY"]		= "REP_CHK_DB";
				param["TENGPCD"]	= $.getInputboxValue($("#"+$.id_inp.txt_tengpcd));
				param["KYOSEIFLG"]	= that.judgeRepType.kyosei?$.id.value_on:$.id.value_off;	// 強制フラグ
				var chk_rows = $.getSelectListData(that.name, $.id.action_check, id, [param]);
				// エラーの場合は、エラー情報が返ってくる
				if(chk_rows[0]["ID"]){
					return chk_rows[0]["ID"];
				}
			}

			// 登録／更新時チェック
			if(id===undefined && isUpdate){
				var txt_qacredt = $.getParserDt(that.baseData[0]["F11"], false);
				var txt_qarcredt= $.getParserDt(that.baseData[0]["F13"], false);
				if(that.judgeRepType.ins){
					// 3.1.1.6．全店特売（アンケート有）_基本.アンケート作成日<処理日付.処理日付and通常グループandアンケート再作成日=NULLの登録／更新の場合エラー。
					if(!that.judgeRepType.kyosei && txt_qacredt < shoridt && $.isEmptyVal(txt_qarcredt)){
						// E20290	催しのアンケート作成日を過ぎており、かつアンケート再作成日が入力されていない為エラー	 	0	 	E
						return "E20290";
					}
				}else{
					// 3.2.1.5．通常グループの場合は、全店特売（アンケート有）_基本.アンケート作成日<処理日付.処理日付andアンケート再作成日=NULLの更新の場合エラー。　
					// 　強制グループの場合は、全店特売（アンケート有）_店グループ.アンケート作成日_強制<処理日付.処理日付andアンケート再作成日=NULLの更新の場合エラー。
					if(!that.judgeRepType.kyosei){
						if(txt_qacredt < shoridt  && $.isEmptyVal(txt_qarcredt)){
							// E20290	催しのアンケート作成日を過ぎており、かつアンケート再作成日が入力されていない為エラー	 	0	 	E
							return "E20290";
						}
					}else{
						txt_qacredt = $.getInputboxValue($("#"+$.id_inp.txt_qacredt), undefined, false);
						txt_qarcredt= $.getInputboxValue($("#"+$.id_inp.txt_qarcredt), undefined, false);
						if(txt_qacredt < shoridt  && $.isEmptyVal(txt_qarcredt)){
							// E20290	催しのアンケート作成日を過ぎており、かつアンケート再作成日が入力されていない為エラー	 	0	 	E
							return "E20290";
						}
					}
				}

				var txt_jlstcreflg = that.baseData[0]["F20"];
				// 3.1.1.7．全店特売（アンケート有）_基本.事前発注リスト作成済フラグ=1:済の場合、登録／更新時にエラー。
				// 3.2.1.6．全店特売（アンケート有）_基本.事前発注リスト作成済フラグ=1:済の場合、登録／更新時にエラー。
				if(txt_jlstcreflg === $.id.value_on){
					// E20205	事前発注リスト作成済フラグ作成済みのため登録更新できません。	 	0	 	E
					return "E20205";
				}
			}

			if(id===$.id_inp.txt_tencd||id===$.id_inp.txt_tencd+"leader"){
				// 3.1.1.8．【画面】.「リーダー店コード」>=401番or【画面】.「店コード」>=401番の場合エラー。
				// 3.2.1.7．【画面】.「リーダー店コード」>=401番or【画面】.「店コード」>=401番の場合エラー。　
				if(!$.isEmptyVal(newValue) && newValue*1 >= 401){
					// E20292	店コードに401以上は入力できません。	 	0	 	E
					return "E20292";
				}
				// 3.1.1.9．【画面】.「リーダー店コード」or【画面】.「店コード」で店舗基本マスタを検索し、店運用区分が9(廃店)の場合はエラー。
				// 3.2.1.8．【画面】.「リーダー店コード」or【画面】.「店コード」で店舗基本マスタを検索し、店運用区分が9(廃店)の場合はエラー。　
				var param = that.getInputboxParams(that, id, newValue);
				param["KEY"] = "MST_CNT";
				var chk_val = $.getInputboxData(that.name, $.id.action_check,  $.id_inp.txt_tencd, [param]);
				if(chk_val == "0"){
					// E20229	廃店は入力できません。	 	0	 	E
					return "EX1077";
				}
			}

			// 3.1.1.10．前の画面の「強制グループ」よりの場合の関連項目チェック：
			// 3.2.1.9．当グループは強制グループ（強制フラグ = 1：強制）の場合の関連項目チェック
			if(id===undefined && that.judgeRepType.kyosei){
				var txt_qacredt = $.getInputboxValue($("#"+$.id_inp.txt_qacredt), undefined, false);
				var txt_qarcredt= $.getInputboxValue($("#"+$.id_inp.txt_qarcredt),undefined, false);
				var txt_lsimedt = that.baseData[0]["F19"];
				// 　　3.1.1.10.1．【画面】.アンケート再作成日の「本強制グループ」  > 【画面】.アンケート作成日の「本強制グループ」 > 全店特売(アンケート有)_基本.最終締日
				if(!$.isEmptyVal(txt_qarcredt) && !(txt_qarcredt > txt_qacredt)){
					// E20185	アンケート再作成日 > アンケート作成日の条件で入力してください。	 	0	 	E
					return "E20185";
				}
				if(!(txt_qacredt > txt_lsimedt)){
					// E20171	アンケート作成日 > 最終締の条件で入力してください。	 	0	 	E
					return "E20171";
				}

				// 　　3.1.1.10.2．【画面】.アンケート作成日の「本強制グループ」 >= 処理日付。
				// 　　【画面】.アンケート作成日の「本強制グループ」< 処理日付.処理日付の場合は、【画面】. アンケート再作成日の「本強制グループ」>= 処理日付。
				if(!(txt_qacredt >= shoridt)){
					if($.isEmptyVal(txt_qarcredt)){
						// E20515	アンケート作成日の「本強制グループ」 ≧ 処理日付の条件で入力してください。	 	0	 	E
						return "E20515";
					}else if(!(txt_qarcredt >= shoridt)){
						// E20366	アンケート再作成日 ≧ 処理日付の条件で入力してください。	 	0	 	E
						return "E20366";
					}
				}
			}

			return null;
		},
		getInputboxParams: function(that, id, newValue){
			// 情報取得
			var values = {};
			values["value"]		= newValue;
			values["MOYSKBN"]	= $.getJSONObject(that.jsonString, $.id_inp.txt_moyskbn).value;		// 催し区分
			values["MOYSSTDT"]	= $.getJSONObject(that.jsonString, $.id_inp.txt_moysstdt).value;	// 催しコード（催し開始日）
			values["MOYSRBAN"]	= $.getJSONObject(that.jsonString, $.id_inp.txt_moysrban).value;	// 催し連番
			values["SENDBTNID"]	= that.sendBtnid;
			// 情報設定
			return values;
		}
	} });
})(jQuery);