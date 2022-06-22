/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_ReportST008',			// （必須）レポートオプションの確認
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
		sortBtnid_: "",						// 並び替えボタンID情報
		sendBtnid: "",						// （必須）呼出ボタンID情報
		focusRootId:"cc",					// （キー移動時必須）キー移動イベントのルートとなるパネルのID
		focusParentId:"",					// （キー移動時必須）現在フォーカスがあたっている項目の親となるパネルのID
		focusGridId:"",						// （キー移動時必須）現在フォーカスがあたっているDataGridのID
		editRowIndex:{},					// グリッド編集行保持
		gridData:[],						// 検索結果
		baseData:[],						// 検索結果保持用
		updData:[],							// 検索結果保持用
		scrollToId:[],						// 戻り時にフォーカス行を指定したい場合(gridholder以外)は指定
		initData:[],
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

//			// データ表示エリア初期化
//			that.setGrid($.id.gridholder, reportno);

			var isUpdateReport = true;

			// 編集可能データグリッドの共通処理設定
			// 編集エディターの機能拡張（非表示で参照用のオブジェクトが準備してある前提、参照オブジェクトはグリッドのフィールド名を元に判断）
			$.extendDatagridEditor();

			// 初期検索可能
			that.onChangeReport = true;

//			var count = 2;
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

			// チェックボックス
			$.setCheckboxInit(that.jsonHidden, 'chk_rinji', that);


			// コピーボタン押下後、本画面に戻った際に前回入力情報を再設定する。
			var inputInfo = $.getJSONObject(that.jsonHidden, "inputInfo");
			if(inputInfo && inputInfo.value.length > 0 ){
				var txt_rankno 	= inputInfo.value[0].txt_rankno					// ランクNo
				var txt_rankkn 	= inputInfo.value[0].txt_rankkn;				// ランク名称

				$('#'+$.id_inp.txt_rankno).numberbox("setValue",txt_rankno);
				$('#'+$.id_inp.txt_rankkn).textbox("setValue",txt_rankkn);
				$($.id.hiddenChangedIdx).val("1");

			}

//			that.sortBtnid = "TENCD";
//			that.sortOrder = "ASC";
			// 店情報
			that.setEditableGrid(that, reportno, $.id.grd_teninfo+'_list');

			// 店番一括入力
			that.setEditableGrid2(that, reportno, $.id.grd_tencdiinput+'_list');

//			// 検索実行
//			that.onChangeReport = true;

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
				$.reg.search = true;
			}
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

			// データグリッドのIDを変更。
			//$('#'+$.id.grd_teninfo + '_list').attr('id', "gridholder");

			that.updData = $.getJSONValue(that.jsonHidden, 'updData');						// 参照情報保持(グリッド一覧)
			that.initData = $.getJSONValue(that.jsonHidden, 'initData');					// 初回参照情報保持(グリッド一覧)

			// 非活性
			$.setInputBoxDisable($("#"+$.id_inp.txt_bmncd));								// 部門
			$.setInputBoxDisable($("#"+$.id_inp.txt_ten_number));							// 店舗数

			// 各種遷移ボタン
			$('#'+$.id.btn_jissekirefer).on("click", $.pushChangeReport);					// 実績参照ボタン

			// 店番順ボタン
			$('#'+$.id.btn_tennoorder).on("click", function(e){
				if (that.sortOrder==="" || (that.sortBtnid_==="TENNO" && that.sortOrder==="ASC")) {
					that.sortOrder = "DESC";
				} else {
					that.sortOrder = "ASC";
				}
				that.sortBtnid_ = "TENNO";
				that.sortGridRows('#'+$.id.grd_teninfo + '_list', that.sortBtnid_, that.sortOrder);
			});
			// ランク順ボタン
			$('#'+$.id.btn_rankorder).on("click", function(e){
				var count = 0;
				var rows = $('#'+$.id.grd_teninfo + '_list').datagrid('getRows');
				for (var j=0; j<rows.length; j++){
					var rank = rows[j]["RANK"]
					if (rank != undefined && rank != null && rank != "" && rank != " ") {
						count++;
						break;
					}
				}
				if (count > 0) {
					if (that.sortBtnid_==="RANKNO" && that.sortOrder==="ASC") {
						that.sortOrder = "DESC";
					} else {
						that.sortOrder = "ASC";
					}
					that.sortBtnid_ = "RANKNO";
					that.sortGridRows('#'+$.id.grd_teninfo + '_list', that.sortBtnid_, that.sortOrder);
				}
			});
			// 実績順ボタン
			$('#'+$.id.btn_jissekiorder).on("click", function(e){
				var count = 0;
				var rows = $('#'+$.id.grd_teninfo + '_list').datagrid('getRows');
				for (var j=0; j<rows.length; j++){
					var hbj = rows[j]["SANKOUHBJ"]
					if (hbj != undefined && hbj != null && hbj != "" && hbj != " ") {
						count++;
						break;
					}
				}
				if (count > 0) {
					if (that.sortBtnid_==="SANKOUHBJ" && that.sortOrder==="ASC") {
						that.sortOrder = "DESC";
					} else {
						that.sortOrder = "ASC";
					}
					that.sortBtnid_ = "SANKOUHBJ";
					that.sortGridRows('#'+$.id.grd_teninfo + '_list', that.sortBtnid_, that.sortOrder);
				}
			});
			// 設定ボタン
			$('#'+$.id.btn_set).on("click", function(e){
				// EasyUI のフォームメソッド 'validate' 実施
				var rt = $($.id.gridform).form('validate');
				if(!rt){
					$.addErrState(that, $('.validatebox-invalid').eq(0), false);
					return rt;
				}

				var row = $('#'+$.id.grd_tencdiinput+'_list').datagrid("getSelected");
				var rowIndex = $('#'+$.id.grd_tencdiinput+'_list').datagrid("getRowIndex", row);
				$('#'+$.id.grd_tencdiinput+'_list').datagrid('endEdit',rowIndex);

				// 入力チェック
				var txt_rankiinput	= $('#'+$.id_inp.txt_rankiinput).textbox('getValue');
				if(!txt_rankiinput) {
					$.showMessage('E20121', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_rankiinput), true)});
					return false;
				}
				var targetTencdiinput = that.getGridData2($.id.grd_tencdiinput+'_list');
				if (targetTencdiinput.length == 0) {
					$.showMessage('E20120', undefined, function(){$.addErrState(that, $('#'+$.id_inp.targetTencdiinput), true)});
					return false;
				}
				// 存在チェック：店コード
				for (var j=0; j < targetTencdiinput.length; j++) {
					var target = targetTencdiinput[j]["F1"];
					var msgid = that.checkInputboxFunc($.id_inp.txt_tencd, target , '');
					if(msgid !==null){
						if (msgid.split(",").length >= 2) {
							$.showMessage(msgid.split(",")[0],[msgid.split(",")[1]]);
						} else {
							$.showMessage(msgid);
						}
						return false;
					}
				}
				var txt_rankiinput	 = $('#'+$.id_inp.txt_rankiinput).textbox('getValue');
				that.setGridData(that, $.id.grd_tencdiinput, txt_rankiinput);
				that.getTenNumber(that, $.id.grd_teninfo);

				// 初期化
				$('#'+$.id.grd_tencdiinput + '_list').datagrid('reload');
			});

			$('#'+$.id.btn_cancel).on("click", $.pushChangeReport);				// キャンセル
			$('#'+$.id.btn_copy).on("click", $.pushChangeReport);				// コピー
			$.setInputBoxDisable($("#"+$.id.chk_rinji));						// 臨時
			$.setInputBoxDisable($("#"+$.id_inp.txt_moyscd));					// 催しコード

			if (that.sendBtnid===$.id.btn_new) {
				$.setInputBoxDisable($('#'+$.id.btn_del)).css('visibility', 'hidden');
				$.initReportInfo("ST008", "ランクマスタ　店情報　新規", "新規");

			} else if (that.sendBtnid===$.id.btn_sel_change) {
				$.setInputBoxDisable($("#"+$.id_inp.txt_rankno));
				$.initReportInfo("ST008", "ランクマスタ　店情報　変更", "変更");
			} else {
				if(reportYobi1==='2'){
					that.sendBtnid = $.id.btn_new;
					$.initReportInfo("ST008", "ランクマスタ　店情報　新規", "新規");
				}else {
					that.sendBtnid = $.id.btn_sel_change;
					$.setInputBoxDisable($("#"+$.id_inp.txt_rankno));
					$.initReportInfo("ST008", "ランクマスタ　店情報　変更", "変更");
				}
			}
			// 変更
			$($.id.hiddenChangedIdx).val('');
		},

		initCondition: function (){	// 条件初期値セット
			var that = this;
			// 初期化項目
		},
		clear:function(){
			// 隠し情報初期化
			$($.id.hiddenChangedIdx).val("");						// 変更行Index
			// グリッド初期化
			this.success(this.name, false);
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
			var txt_bmncd		= $.getJSONValue(that.jsonHidden, $.id_inp.txt_bmncd);		// 部門
			var chk_rinji		= $.getJSONValue(that.jsonHidden, $.id.chk_rinji);			// 臨時
			var txt_moyscd		= $.getJSONValue(that.jsonHidden, $.id_inp.txt_moyscd);		// 催しコード
			var txt_rankno		= $.getJSONValue(that.jsonHidden, $.id_inp.txt_rankno);		// ランクNo.

			// コピーからの遷移時対応
			var txt_bmncd_ini	= $.getJSONValue(that.jsonHidden, $.id_inp.txt_bmncd+'_ini');	// 部門
			var chk_rinji_ini	= $.getJSONValue(that.jsonHidden, $.id.chk_rinji+'_ini');		// 臨時
			var txt_moyscd_ini	= $.getJSONValue(that.jsonHidden, $.id_inp.txt_moyscd+'_ini');	// 催しコード
			var txt_rankno_ini	= $.getJSONValue(that.jsonHidden, $.id_inp.txt_rankno+'_ini');	// ランクNo.
			var txt_rankkn_ini	= $.getJSONValue(that.jsonHidden, $.id_inp.txt_rankkn+'_ini');	// ランク名称
			if ($.isEmptyVal(txt_bmncd_ini)) {
				var txt_bmncd_ini	= $.getJSONObject(this.jsonString, $.id_inp.txt_bmncd).value;		// 部門
				var chk_rinji_ini	= $.getJSONObject(this.jsonString, $.id.chk_rinji).value;			// 臨時
				var txt_moyscd_ini	= $.getJSONObject(this.jsonString, $.id_inp.txt_moyscd).value;		// 催しコード
				var txt_rankno_ini	= $.getJSONObject(this.jsonString, $.id_inp.txt_rankno).value;		// ランクNo.
			}

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
//			$.appendMaskMsg();

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,		// レポート名
					BMNCD:			txt_bmncd,
					MOYSCD:			txt_moyscd,
					RINJI:			chk_rinji,
					RANKNO:			txt_rankno,
					TENNUMBER:		that.tenNumber,
					BMNCD_INI:		txt_bmncd_ini,
					MOYSCD_INI:		txt_moyscd_ini,
					RINJI_INI:		chk_rinji_ini,
					RANKNO_INI:		txt_rankno_ini,
					RANKKN_INI:		txt_rankkn_ini,
					SENDBTNID:		that.sendBtnid,
					SORTBTN:		that.sortBtnid_,
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
					}

					// ログ出力
					$.log(that.timeData, 'query:');

					var opts = JSON.parse(json).opts

					// 検索結果を保持
					//that.baseData = JSON.parse(json).rows;

					// メインデータ表示
					that.setData(opts.rows_, opts);
					that.queried = true;

					// 状態保存
					$.saveState2(reportno, that.getJSONString());

					// ログ出力
					$.log(that.timeData, 'loaded:');
				}
			);
		},
		setGridData: function (that, id, newValue){
			var that = this;
			var rows = [];
			rows = that.getGridData($.id.grd_teninfo+'_list');

			if(id===undefined || id===$.id.grd_tencdiinput){									// 一括入力

				var inputRows = that.getGridData2($.id.grd_tencdiinput+'_list');
				var txt_rankiinput	 = $('#'+$.id_inp.txt_rankiinput).textbox('getValue');
				for (var i=0; i<rows.length; i++){
					var rt = false;
					for (var j=0; j < inputRows.length; j++) {
						if (rows[i]["F1"] == ('000' + inputRows[j]["F1"]).slice( -3 )) {
							rt = true;
							break;
						}
					}
					if (rt) {
						// データグリッドを更新
						$('#'+$.id.grd_teninfo + '_list').datagrid('updateRow',{
							index: i,
							row: { RANK:newValue }
						})
					}
				}
			}
			if (id===undefined || id===$.id_inp.txt_rank) {										// ランク入力

				if (newValue == "") {
					newValue = " ";
				}
				for (var i=0; i<rows.length; i++){
					var idx = that.editRowIndex[that.focusGridId] + 1;

					if (rows[i]["F1"] ==  ('000' + idx).slice( -3 )) {
						// データグリッドを更新
						$('#'+$.id.grd_teninfo + '_list').datagrid('updateRow',{
							index: i,
							row: { RANK:newValue }
						})
					}
				}
			}
			that.updData = that.getGridData($.id.grd_teninfo+'_list');
		},
		updValidation: function (){	// （必須）批准
			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform).form('validate');
			if(!rt){
				$.addErrState(that, $('.validatebox-invalid').eq(0), false);
				return rt;
			}

			var rt = $($.id.gridform).form('validate');
			if(!rt){
				var palentObj = $('.validatebox-invalid').eq(0)[0].parentElement
				var paletntId = palentObj.previousElementSibling.id
				var SubGridRef = [$.id_inp.txt_rankiinput, $.id_inp.txt_tencdiinput + '_'];

				if(SubGridRef.indexOf(paletntId) == -1){
					$.addErrState(that, $('.validatebox-invalid').eq(0), false);
					return rt;
				}else{
					rt = true;
				}
			}

			// 存在チェック：店コード
			var rowsTeninfo = that.getGridData($.id.grd_teninfo+'_list');
			for (var j=0; j < rowsTeninfo.length; j++) {
				if(rowsTeninfo[j]["F3"] && rowsTeninfo[j]["F3"] != ""){
					var target = rowsTeninfo[j]["F1"];
					var msgid = that.checkInputboxFunc($.id_inp.txt_tencd, target , '');
					if(msgid !==null){
						if (msgid.split(",").length >= 2) {
							$.showMessage(msgid.split(",")[0],[msgid.split(",")[1]]);
						} else {
							$.showMessage(msgid);
						}
						return false;
					}
				}
			}

			var txt_rankno		= $('#'+$.id_inp.txt_rankno).textbox('getValue');							// ランクNo.
			var chk_rinji		= $('#'+$.id.chk_rinji).is(':checked') ? $.id.value_on : $.id.value_off; 	// 臨時
			var txt_rankkn		= $('#'+$.id_inp.txt_rankkn).textbox('getValue');							// ランク名称
			var txt_moyscd		= $('#'+$.id_inp.txt_moyscd).textbox('getValue'); 							// 催しコード

			// 入力チェック
			if (rt) {
				if (!txt_rankno) {
					$.showMessage('EX1086', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_rankno), true)});
					rt = false;
				}
			}
			if (rt) {
				if (chk_rinji==="1" && txt_rankno < 900) {
					$.showMessage('EX1085', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_rankno), true)});
					rt = false;
				}
			}
			if (rt) {
				if (chk_rinji==="0" && txt_rankno >= 900) {
					$.showMessage('EX1066', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_rankno), true)});
					rt = false;
				}
			}
			if (rt) {
				if (chk_rinji==="1" && !txt_moyscd) {
					$.showMessage('EX1026', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_moyscd), true)});
					rt = false;
				}
			}
			if (rt) {
				if (!txt_rankkn) {
					$.showMessage('EX1087', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_rankkn), true)});
					rt = false;
				}
			}
			if (rt) {	// ランクの空白チェック
				var count = 0;
				var targetRowsTeninfo = that.getGridData($.id.grd_teninfo+'_list');
				for (var i=0; i<targetRowsTeninfo.length; i++){
					var rank = targetRowsTeninfo[i]["F3"]
					if (rank != undefined && rank != null && rank.trim() != "") {
						count++;
					}
				}
				if (count == 0) {
					$.showMessage('E20121');
					rt = false;
				}
			}
			return rt;
		},
		updSuccess: function(id){	// validation OK時 の update処理
			var that = this;

			var targetDatas	 = [{}];
			var chk_rinji	 = $.getInputboxValue($('#'+$.id.chk_rinji));		// 臨時

			if(chk_rinji == '1'){
				// 臨時チェック有り
				var bmncd		 = $.getInputboxValue($('#'+$.id_inp.txt_bmncd));					// 部門
				var rankno		 = $.getInputboxValue($('#'+$.id_inp.txt_rankno));					// ランクNo
				var rankkn		 = $.getInputboxValue($('#'+$.id_inp.txt_rankkn));					// ランク名称
				var myoscd		 = $.getInputboxValue($('#'+$.id_inp.txt_moyscd));					// 催しコード
				var szMoyskbn	 = myoscd.substring(0,1);											// 催し区分
				var szMoysstdt	 = myoscd.substring(1,7);											// 催しコード（催し開始日）
				var szMoysrban	 = myoscd.substring(7,10);											// 催し連番

				targetDatas = [{
					F1:		bmncd,
					F2:		szMoyskbn,
					F3:		szMoysstdt,
					F4:		szMoysrban,
					F5:		rankno,
					F6:		rankkn,
					F7:		"",
				}]
			}else{
				$('#'+that.focusRootId).find('[col^=F]').each(function(){
					var col = $(this).attr('col');
					targetDatas[0][col] = $.getInputboxValue($(this));
				});
			}

			var targetRowsTeninfo = that.getGridData($.id.grd_teninfo+'_list');


			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
//			$.appendMaskMsg();

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,							// レポート名
					action:			$.id.action_update,					// 実行処理情報
					obj:			id,									// 実行オブジェクト
					SENDBTNID:		that.sendBtnid,
					DATA:			JSON.stringify(targetDatas),		// 更新対象情報
					DATA_TENINFO:	JSON.stringify(targetRowsTeninfo),	// 更新対象情報
					t:				(new Date()).getTime()
				},
				function(data){
					// 検索処理エラー判定
					if($.updError(id, data)) return false;

					var afterFunc = function(){
						// 初期化
						that.clear();
						that.changeReport(that.name, $.id.btn_back);
					};
					$.updNormal(data, afterFunc);

					// ログ出力
					$.log(that.timeData, 'loaded:');
				}
			);
		},
		delValidation: function (){	// （必須）批准
			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = true;
			// 入力チェック用の配列をクリア
			that.jsonTemp = [];

			return rt;
		},
		delSuccess: function(id){
			var that = this;

			var targetDatas = [{}];
			$('#'+that.focusRootId).find('[col^=F]').each(function(){
				var col = $(this).attr('col');
				targetDatas[0][col] = $.getInputboxValue($(this));
			});

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
					//IDX:			$($.id.hiddenChangedIdx).val(),	// 更新対象Index
					//DATA:			JSON.stringify(targetRows),		// 更新対象情報
					DATA:			JSON.stringify(targetDatas),	// 更新対象情報
					t:				(new Date()).getTime()
				},
				function(data){
					// 検索処理エラー判定
					if($.delError(id, data)) return false;

					var afterFunc = function(){
						// 初期化
						that.clear();
						that.changeReport(that.name, 'btn_return');
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
			// 部門
			this.jsonTemp.push({
				id:		$.id_inp.txt_bmncd,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_bmncd),
				text:	''
			});
			// ランクNo.
			/*this.jsonTemp.push({
				id:		$.id_inp.txt_rankno,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_rankno),
				text:	''
			});*/
			// ランクNo.
			this.jsonTemp.push({
				id:		$.id_inp.txt_rankno,
				value:	$('#'+$.id_inp.txt_rankno).textbox('getValue'),
				text:	$('#'+$.id_inp.txt_rankno).textbox('getText')
			});
			// ランク名称
			this.jsonTemp.push({
				id:		$.id_inp.txt_rankkn,
				value:	$('#'+$.id_inp.txt_rankkn).textbox('getValue'),
				text:	$('#'+$.id_inp.txt_rankkn).textbox('getText')
			});
			// 臨時
			this.jsonTemp.push({
				id:		$.id.chk_rinji,
				value:	$.getJSONValue(this.jsonHidden, $.id.chk_rinji),
				text:	''
			});
			// 催しコード
			this.jsonTemp.push({
				id:		$.id_inp.txt_moyscd,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_moyscd),
				text:	''
			});
			// ランク
			this.jsonTemp.push({
				id:		$.id_inp.txt_rank,
				value:	$('#'+$.id_inp.txt_rank).textbox('getValue'),
				text:	$('#'+$.id_inp.txt_rank).textbox('getText')
			});
			// ランク(店番一括入力)
			this.jsonTemp.push({
				id:		$.id_inp.txt_rankiinput,
				value:	$('#'+$.id_inp.txt_rankiinput).textbox('getValue'),
				text:	$('#'+$.id_inp.txt_rankiinput).textbox('getText')
			});
			// 店番(店番一括入力)
			this.jsonTemp.push({
				id:		$.id_inp.txt_tencdiinput,
				value:	$('#'+$.id_inp.txt_tencdiinput).textbox('getValue'),
				text:	$('#'+$.id_inp.txt_tencdiinput).textbox('getText')
			});
		},
		setData: function(rows, opts){		// データ表示
			var that = this;

			if(rows.length > 0){
				$('#'+that.focusRootId).find('[col^=F]').each(function(){
					var col = $(this).attr('col');
					if (col==='F5') {
						if(rows[0]['F4']==='0'){
							$.setInputboxValue($(this), '');
						} else {
							if(rows[0][col]){
								$.setInputboxValue($(this), rows[0][col]);
							}
						}
					} else {
						if(rows[0][col]){
							$.setInputboxValue($(this), rows[0][col]);
						}
					}
				});
			}
		},
		setEditableGrid: function(that, reportno, id){		// 一覧
			//that.editRowIndex[id] = undefined;
			that.editRowIndex[id] = -1;
			var init  = true
			var index = -1;

			// ページサイズ定義取得
			var pageList = $.fn.pagination.defaults.pageList;
			var pageList = [10,20,30,50,400,200];
			var pageSize = 50;

			// 選択行を保持するGridのIDを保持(ID=gridholderの場合は保持不要)
			that.scrollToId[0] = id;

			if (pageSize==="") pageSize=$.fn.pagination.defaults.pageSize;
			pageSize = $.getDefaultPageSize(pageSize, pageList);


			if (that.updData === "") {
				that.updData = that.getGridData($.id.grd_teninfo+'_list');
			}
			$('#'+id).datagrid({
			    //remoteSort:false,
//				view:scrollview,
//				pageSize:pageSize,
//				pageList:pageList,
				url:$.reg.easy,
				columns:[[
							{field:'TENCD',		title:'店番',			width: 70 	,halign:'center',align:'left'},
							{field:'TENKN',		title:'店舗名',			width: 300 	,halign:'center',align:'left'},
							{field:'RANK',		title:'ランク',			width: 70 	,halign:'center',align:'left',editor:{type:'textbox'}},
							{field:'SANKOUHBJ',	title:'参考販売実績',	width: 100 	,halign:'center',align:'right'},
							{field:'AREACD',	title:'エリア',			width: 70 	,halign:'center',align:'left'},
						]],
				onBeforeLoad:function(param){
					index = -1;
					if(init){
						var sendBtnid		 = that.sendBtnid;
						var sortBtnid		 = that.sortBtnid_;
						var sortOrder		 = that.sortOrder;
						var txt_bmncd		 = $.getJSONValue(that.jsonHidden, $.id_inp.txt_bmncd);				// 部門
						var txt_rankno		 = $.getJSONValue(that.jsonHidden, $.id_inp.txt_rankno);			// ランクNo
						var chk_rinji		 = $.getJSONValue(that.jsonHidden, $.id.chk_rinji);					// 臨時
						var txt_moyscd		 = $.getJSONValue(that.jsonHidden, $.id_inp.txt_moyscd);			// 催しコード
						var txt_tenrank_arr	 = $.getJSONValue(that.jsonHidden, $.id_inp.txt_tenrank_arr);		// 店ランク配列
						var txt_tenten_arr	 = $.getJSONValue(that.jsonHidden, $.id_inp.txt_tenten_arr);		// 点数配列
						var updData			 = that.updData;
						var json = [{"callpage":"Out_ReportST008"
									,"SENDBTNID":sendBtnid
									,"SORTBTN":sortBtnid
									,"SORTORDER":sortOrder
									,"BMNCD":txt_bmncd
									,"RANKNO":txt_rankno
									,"RINJI":chk_rinji
									,"MOYSCD":txt_moyscd
									,"TENRANKARR":txt_tenrank_arr
									,"TENTENARR":txt_tenten_arr
									,"GRDTENINFO":JSON.stringify(updData)
						}];
						// 情報設定
						param.page		=	reportno;
						param.obj		=	id;
						param.sel		=	(new Date()).getTime();
						param.target	=	id;
						param.action	=	$.id.action_init;
						param.json		=	JSON.stringify(json);
						param.datatype	=	"datagrid";
						param.report = that.name;

						init = false;
					}
				},
				onLoadSuccess:function(data){
					that.getTenNumber(that, $.id.grd_teninfo);

					var getRowIndex = $.getJSONValue(that.jsonHidden, "scrollToIndex_"+id);
					if(getRowIndex !== ""){
						$('#'+id).datagrid('scrollTo', {
							index: getRowIndex,
							callback: function(index){
								$('#'+id).datagrid('selectRow', index);
								$('#'+id).datagrid('beginEdit', index);
							}
						});
					}
				},
				onClickRow: function(index,field){$.clickEditableDatagridCell(that,id, index)},
				onBeforeEdit:function(index,row){
					if(that.sendBtnid===$.id.btn_new || that.reportYobiInfo ==='2'){

						//var allRows	 = $('#'+id).datagrid('getData').firstRows;
						var allRows	 = $('#'+id).datagrid('getRows');
						var rows	 =  $('#'+id).datagrid('getRows');				// 現在表示されているデータ
						var rowsLength = rows.length
						var isEdit = false

						var RefleshRangeMin = $('#'+id).datagrid("getRowIndex", rows[0]);
						var RefleshRangeMax = $('#'+id).datagrid("getRowIndex", rows[rowsLength-1]);

						if(!row){
							row = allRows[index]
						}

						if(row.EDITFLG !='1'){
							var adds = that.editRowIndex[that.focusGridId] > index ? -1:1;
							var nextindex = 0;
							//var nextindex = index + adds;

							// 次のEdit可能な行を探す。
							for(var i = index; i < allRows.length; i++ ){
								var nextRow = allRows[i]
								if(nextRow.EDITFLG =='1'){
									nextindex = $('#'+id).datagrid("getRowIndex", nextRow);
									isEdit = true;
									break;
								}
							}
							if(index == (allRows.length-1)){
								// 最終行が選択された場合
								nextindex = index +1

							}else if(nextindex == 0){
								// Edit可能な行が存在しなかった場合
								nextindex = (allRows.length-1);

							}

							// 次の行に移るか、次の項目に移るかする
							if(nextindex >= 0 && nextindex < $('#'+id).datagrid('getRows').length){
								//$('#'+id).datagrid('endEdit', index);

								// 次の行が画面外の場合、スクロールを行う
//								if(nextindex < RefleshRangeMin || RefleshRangeMax < nextindex){
//									$('#'+id).datagrid('scrollTo', {
//										index: nextindex,
//									});
//								}
								$('#'+id).datagrid('selectRow', nextindex);
								$('#'+id).datagrid('beginEdit', nextindex);
							}else{
								that.editRowIndex[that.focusGridId] = -9;	// 次の項目に移動するために未編集状態と違うインデックス設定
								var evt = $.Event('keydown');
								evt.keyCode = 13;
								evt.shiftKey = adds === -1;
								$('#'+id).parents('.datagrid').eq(0).trigger(evt);
							}
							//$('#'+$.id_inp.txt_rank+"_").val("");
							return false;
						}
					}
				},
				onBeginEdit:function(index,row){$.beginEditDatagridRow(that,id, index, row)},
				onEndEdit: function(index,row,changes){
					$.endEditDatagridRow(that, id, index, row);
				},

			});
		},
		setEditableGrid2: function(that, reportno, id){		// 一括入力
			that.editRowIndex[id] = -1;
			var index = -1;
			$('#'+id).datagrid({
				url:$.reg.easy,
				columns:[[
							{field:'TENCDIINPUT',	title:'店番',	width: 70 ,halign:'center',align:'left',editor:{type:'textbox'},
								formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '000');}},
						]],
				onBeforeLoad:function(param){
					index = -1;
					var txt_rankiinput	= $('#'+$.id_inp.txt_rankiinput).textbox('getValue');	// ランク(店番一括入力)
					var txt_tencdiinput	= $('#'+$.id_inp.txt_tencdiinput).textbox('getValue');	// 店番(店番一括入力)
					var json = [{"callpage":"Out_ReportST008","RANK":txt_rankiinput,"TENCD":txt_tencdiinput}];
					// 情報設定
					param.page		=	reportno;
					param.obj		=	id;
					param.sel		=	(new Date()).getTime();
					param.target	=	id;
					param.action	=	$.id.action_init;
					param.json		=	JSON.stringify(json);
					param.datatype	=	"datagrid";
					param.report = that.name;
				},
				onLoadSuccess:function(data){},
				onClickRow: function(index,field){$.clickEditableDatagridCell(that,id, index)},
				onBeginEdit:function(index,row){$.beginEditDatagridRow(that,id, index, row)},
				onEndEdit: function(index,row,changes){$.endEditDatagridRow(that, id, index, row)}
			});
		},
		getGridData: function (target){
			var targetRows= [];
			if(target===undefined || target===$.id.grd_teninfo+'_list'){
				var rowsTeninfo= $('#'+$.id.grd_teninfo+'_list').datagrid('getRows');
				for (var i=0; i<rowsTeninfo.length; i++){
					if(rowsTeninfo[i]["TENCD"] == "" || rowsTeninfo[i]["TENCD"] == null ){

					}else{
						var rowDate = {
								F1	 : rowsTeninfo[i]["TENCD"],
								F2	 : rowsTeninfo[i]["TENKN"],
								F3	 : rowsTeninfo[i]["RANK"],
								F4	 : rowsTeninfo[i]["SANKOUHBJ"],
								F5	 : rowsTeninfo[i]["AREACD"],
							};
						targetRows.push(rowDate);
					}
				}
			}
			return targetRows;
		},
		getGridData2: function (target){
			var targetRows= [];
			if(target===undefined || target===$.id.grd_tencdiinput+'_list'){
				var rowsTencdiinput= $('#'+$.id.grd_tencdiinput+'_list').datagrid('getRows');
				for (var i=0; i<rowsTencdiinput.length; i++){
					if(rowsTencdiinput[i]["TENCDIINPUT"] == "" || rowsTencdiinput[i]["TENCDIINPUT"] == null ){

					}else{
						var rowDate = {
								F1	 : rowsTencdiinput[i]["TENCDIINPUT"],
							};
						targetRows.push(rowDate);
					}
				}
			}
			return targetRows;
		},
		sortGridRows: function (id, sortBtnid, sortOrder){
			var taht =this;
			var rows = $(id).datagrid('getRows');
			var count = 0;
			if(rows){
				if (sortBtnid==="TENNO") {
					if (sortOrder==="ASC") {
						rows.sort(function(a,b){
						    if(a.TENCD>b.TENCD) return 1;
						    if(a.TENCD<b.TENCD) return -1;
						    return 0;
						});
					} else {
						rows.sort(function(a,b){
						    if(a.TENCD<b.TENCD) return 1;
						    if(a.TENCD>b.TENCD) return -1;
						    return 0;
						});
					}
					//$(id).datagrid('reload');

					$(id).datagrid({
						data:rows
					});

				}
				if (sortBtnid==="RANKNO") {
					if (sortOrder==="ASC") {
						rows.sort(function(a,b){
						    if(String(a.RANK?a.RANK:"") == "" && String(b.RANK?b.RANK:"") == ""){
						    	if(a.TENCD>b.TENCD) return 1;
							    if(a.TENCD<b.TENCD) return -1;

						    }else if(String(a.RANK?a.RANK:"") != "" && String(b.RANK?b.RANK:"") == ""){
						    	return -1;

						    }else if(String(a.RANK?a.RANK:"") == "" && String(b.RANK?b.RANK:"") != ""){
						    	return 1;

						    }else {
							    if(String(a.RANK)>String(b.RANK))return 1;
							    if(String(a.RANK)<String(b.RANK))return -1;
							    if(a.TENCD>b.TENCD) return 1;
							    if(a.TENCD<b.TENCD) return -1;
						    }
						    return 0;
						});
					} else {
						rows.sort(function(a,b){
							//if(String(a.SORTKBN)>String(b.SORTKBN))return 1;
						    //if(String(a.SORTKBN)<String(b.SORTKBN))return -1;
						    if(String(a.RANK)<String(b.RANK))return 1;
						    if(String(a.RANK)>String(b.RANK))return -1;
						    if(String(a.RANK) == String(b.RANK)){
						    	if(a.TENCD>b.TENCD) return 1;
							    if(a.TENCD<b.TENCD) return -1;
						    }
						    return 0;
						});
					}
					$(id).datagrid({
						data:rows
					});
				}
				if (sortBtnid==="SANKOUHBJ") {
					if (sortOrder==="ASC") {
						rows.sort(function(a,b){
							if(String(a.SORTKBN2)>String(b.SORTKBN2))return 1;
						    if(String(a.SORTKBN2)<String(b.SORTKBN2))return -1;
						    if(parseInt(a.SANKOUHBJ)>parseInt(b.SANKOUHBJ)) return 1;
						    if(parseInt(a.SANKOUHBJ)<parseInt(b.SANKOUHBJ)) return -1;
						    return 0;
						});
					} else {
						rows.sort(function(a,b){
							if(String(a.SORTKBN2)>String(b.SORTKBN2))return 1;
						    if(String(a.SORTKBN2)<String(b.SORTKBN2))return -1;
						    if(parseInt(a.SANKOUHBJ)<parseInt(b.SANKOUHBJ)) return 1;
						    if(parseInt(a.SANKOUHBJ)>parseInt(b.SANKOUHBJ)) return -1;
						    return 0;
						});
					}
					$(id).datagrid({
						data:rows
					});
				}
			}
			//$(id).datagrid('acceptChanges');
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
			var sendMode = "";		// 遷移後に、前回検索条件を表示したい場合、""のまま、ここで設定した条件を表示したい場合"1"

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
			var newrepinfos = $.getBackJSON(that, states, false);
			$.setJSONObject(sendJSON, 'repinfo', newrepinfos, '');

			that.updData = that.getGridData($.id.grd_teninfo+'_list');
			$.setJSONObject(sendJSON, 'updData', that.updData, that.updData);							// 参照情報保持(グリッド一覧)
			$.setJSONObject(sendJSON, 'initData', that.initData, that.initData);						// 初回情報保持(グリッド一覧)

			// 呼出別処理
			switch (btnId) {
			case $.id.btn_copy:

				sendMode = 1;
				// 元画面情報
				var callpage = $.getJSONValue(that.jsonHidden, "callpage");
				// 転送先情報
				index = 8;		// ST015 ランクマスタ コピー元ランクＮＯ選択
				if(that.reportYobiInfo()==='1'){
					index = 2;
				}
				childurl = href[index];
				var txt_bmncd	= $.getInputboxValue($('#'+$.id_inp.txt_bmncd));	// 部門
				var txt_rankno	= $.getInputboxValue($('#'+$.id_inp.txt_rankno));	// ランクNo
				var txt_rankkn	= $.getInputboxValue($('#'+$.id_inp.txt_rankkn));	// ランク名称
				var chk_rinji	= $.getInputboxValue($('#'+$.id.chk_rinji));		// 臨時
				var txt_moyscd	= $.getInputboxValue($('#'+$.id_inp.txt_moyscd));	// 催しコード

				// オブジェクト作成
				$.setJSONObject(sendJSON, $.id_inp.txt_bmncd, txt_bmncd, txt_bmncd);					// 部門
				$.setJSONObject(sendJSON, $.id_inp.txt_rankno, txt_rankno, txt_rankno);					// ランクNo
				$.setJSONObject(sendJSON, $.id_inp.txt_rankkn, txt_rankkn, txt_rankkn);					// ランク名称
				$.setJSONObject(sendJSON, $.id.chk_rinji, chk_rinji, chk_rinji);						// 臨時
				$.setJSONObject(sendJSON, $.id_inp.txt_moyscd, txt_moyscd, txt_moyscd);					// 催しコード

				$($.id.hiddenChangedIdx).val('');														// 画面遷移時にメッセージを表示させない
				break;
			case $.id.btn_jissekirefer:	// 実績参照
				$($.id.hiddenChangedIdx).val('');
				sendMode = 1;
				var callpage = $.getJSONValue(that.jsonHidden, "callpage");
				// 転送先情報
				index = 4;	// ST011 (ランクマスタ実績参照)
				if(that.reportYobiInfo()==='1'){
					index = 2;
				}
				childurl = href[index];
				var txt_bmncd = $.getJSONValue(that.jsonHidden, $.id_inp.txt_bmncd);					// 部門
				var txt_rankno = $.getJSONValue(that.jsonHidden, $.id_inp.txt_rankno);					// ランクNo
				var txt_rankkn = $.getJSONValue(that.jsonHidden, $.id_inp.txt_rankkn);					// ランク名称
				var chk_rinji = $.getJSONValue(that.jsonHidden, $.id.chk_rinji);						// 臨時
				var txt_moyscd = $.getJSONValue(that.jsonHidden, $.id_inp.txt_moyscd);					// 催しコード
				// オブジェクト作成
				$.setJSONObject(sendJSON, $.id_inp.txt_bmncd, txt_bmncd, txt_bmncd);					// 部門
				$.setJSONObject(sendJSON, $.id_inp.txt_rankno, txt_rankno, txt_rankno);					// ランクNo
				$.setJSONObject(sendJSON, $.id_inp.txt_rankkn, txt_rankkn, txt_rankkn);					// ランク名称
				$.setJSONObject(sendJSON, $.id.chk_rinji, chk_rinji, chk_rinji);						// 臨時
				$.setJSONObject(sendJSON, $.id_inp.txt_moyscd, txt_moyscd, txt_moyscd);					// 催しコード

				$($.id.hiddenChangedIdx).val('');														// 画面遷移時にメッセージを表示させない
				break;
			case $.id.btn_back:
			case $.id.btn_cancel:
//				sendMode = 1;
				// 元画面情報
				var callpage = $.getJSONValue(that.jsonHidden, "callpage");
				// 転送先情報
				index = 1;
				// 元画面情報
				for (var i = 0; i < newrepinfos.length; i++) {
					var callpage = newrepinfos[i].id;
					if(callpage==='Out_ReportST010'){
						index = 1;
					}
				}
				sendMode = 2;
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
			if(id+"_"===obj.attr('id') && that.focusGridId!==""){
				parentObj = $('#'+that.focusGridId).datagrid('getPanel');
			}
			if(id===$.id_inp.txt_rank){
				that.getTenNumber(that, id);
			}
		},
		getTenNumber: function (that, id){

			// 入力中の場合、編集を終了させる。
			var row = $('#'+$.id.grd_teninfo+'_list').datagrid("getSelected");
			if(row){
				var rowIndex = $('#'+$.id.grd_teninfo+'_list').datagrid("getRowIndex", row);
				$('#'+$.id.grd_teninfo+'_list').datagrid('endEdit',rowIndex);
			}

			var target = $('#'+$.id_inp.txt_ten_number);
			var count = 0;
			var targetRowsTeninfo = that.getGridData($.id.grd_teninfo+'_list');

			for (var i=0; i<targetRowsTeninfo.length; i++){
				var rank = targetRowsTeninfo[i]["F3"]
				if (rank != undefined && rank != null && rank != "" && rank != " ") {
					count++;
				}
			}
			$.setInputboxValue(target, count, "");			// 店舗数
		},
		checkInputboxFunc:function(id, newValue, kbn, record, isNew){
			var that = this;

			// 店番
			if(id===$.id_inp.txt_tencd){
				var param = {};
				param["KEY"] =  "MST_CNT";
				param["value1"] = newValue;
				param["value2"] = $.getJSONObject(that.jsonString, $.id_inp.txt_bmncd).value;
				var chk_cnt = $.getInputboxData(that.name, $.id.action_check, 'MSTTENBMN', [param]);
				if(chk_cnt =="" || chk_cnt=="0"){
					return "E20229,"+newValue+"店　";
				}
			}
			return null;
		},
		checkUpdData:function(that){
			var initData = that.initData;
			var updData = that.updData;
			//$($.id.hiddenChangedIdx).val('');
			if (updData.length == 0) {
				return;
			}
			for (var i=0; i < initData.length; i++) {

				if (initData[i]["F3"] != updData[i]["F3"]) {	// ランク
					$($.id.hiddenChangedIdx).val('1');
					return;
				}
				if (initData[i]["F4"] != updData[i]["F4"]) {	// 参考販売実績
					$($.id.hiddenChangedIdx).val('1');
					return;
				}
				if (initData[i]["F5"] != updData[i]["F5"]) {	// エリア
					$($.id.hiddenChangedIdx).val('1');
					return;
				}
			}
			return;
		}
	} });
})(jQuery);