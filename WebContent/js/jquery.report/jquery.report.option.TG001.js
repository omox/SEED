/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_ReportTG001',			// （必須）レポートオプションの確認
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
		dedefaultObjNum:	16,	// 初期化オブジェクト数
		initObjNum:	-1,
		initedObject: [],
		maxMergeCell: 1,
		onChangeFlag : false,
		onChangeFlag2 : false,
		columnName:'',	// OnClickRowの列名
		queried : false,
		queried2: false,
		initializes : true,
		onChangeReport: false,
		reportYobiInfo: function(){			// （必須）レポートメニューに登録された予備情報＝帳票をどういった表示にしたいかの情報
			return $('#reportYobi1').val();
		},
		pushBtnid: "",						// 実行ボタンID情報(検索系で利用)
		focusRootId:"cc",					// （キー移動時必須）キー移動イベントのルートとなるパネルのID
		focusParentId:"",					// （キー移動時必須）現在フォーカスがあたっている項目の親となるパネルのID
		focusGridId:"",						// （キー移動時必須）現在フォーカスがあたっているDataGridのID
		editRowIndex:{},					// グリッド編集行保持

		updateRowIdx:undefined,				// 登録行Index
		grd_data:[],						// メイン情報：基本の処理対象情報
		grd_data_other:[],					// 補足情報：その他、テーブルに登録しない情報などを保持
		initialize: function (reportno){	// （必須）初期化
			var that = this;
			// 引き継ぎ情報
			this.jsonHidden = $.getTargetValue();
			// 画面の初回基本設定
			this.setInitObjectState();

			// 初期検索条件設定
			this.jsonInit = $.getInitValue();
			// 処理日付取得
			$('#'+$.id.txt_shoridt).val($.getInputboxData(that.name, $.id.action_init, $.id.txt_shoridt,[{}]));
			// データ表示エリア初期化
			that.setGrid($.id.gridholder.replace('#', ''), reportno);

			// 初期化するオブジェクト数設定
			this.initObjNum = this.dedefaultObjNum;
			// 入力テキストボックス系
			var inputbox = Object.getOwnPropertyNames($.id_inp);
			var isSearchId = [$.id_inp.txt_stym, $.id_inp.txt_enym];
			for ( var sel in inputbox ) {
				if($('#'+$.id_inp[inputbox[sel]]).length > 0){
					var isUpdate = isSearchId.indexOf(inputbox[sel]) === -1;
					$.setInputbox(that, reportno, $.id_inp[inputbox[sel]], isUpdate);
				}
			}
			var upd_chk_ids = [$.id.chk_hbokureflg,$.id.chk_gtsimeflg,$.id.chk_jlstcreflg,$.id.chk_hnctlflg,$.id.chk_tpng1flg,$.id.chk_tpng2flg,$.id.chk_tpng3flg];
			for ( var id in upd_chk_ids ) {
				if($('#'+upd_chk_ids[id]).length > 0){
					$.setCheckboxInit2(that.jsonHidden, upd_chk_ids[id], that);
				}
			}

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
			if(sendBtnid && sendBtnid.length > 0){
				that.sendBtnid = sendBtnid;
				$.reg.search = true;
				that.onChangeReport = true;
			}
			var reportYobi1 = $.getJSONValue(that.jsonHidden, "reportYobi1");
			if(!reportYobi1){
				reportYobi1 = $('#reportYobi1').val();
			}
			$('#reportYobi1').val(reportYobi1);

			var hidobjids = [];	// 非表示
			// 当帳票を「参照」で開いた場合
			if(that.reportYobiInfo()==='1'){
				hidobjids = hidobjids.concat([$.id.btn_cancel,$.id.btn_sel_change+2]);

				$.initReportInfo("TG001", "月間販売計画（チラシ計画）　催し　一覧（参照）");
			}else{
				// 各種ボタン
				$("#"+$.id.btn_cancel).on("click", $.pushChangeReport);
				$('#'+$.id.btn_sel_change+2).on("click", $.pushChangeReport);
				$.initReportInfo("TG001", "月間販売計画（チラシ計画）　催し　一覧");
			}

			// 非表示化
			for (var i = 0; i < hidobjids.length; i++) {
				$.setInputBoxDisable($('#'+hidobjids[i])).hide();
			}
			$($.id.buttons).show();
			$('#'+$.id.btn_sel_change+1).on("click", $.pushChangeReport);
			$('#'+$.id.btn_sel_change+3).on("click", $.pushChangeReport);
			$('#'+$.id.btn_csv+1).on("click", that.pushCsv);
			$('#'+$.id.btn_csv+2).on("click", that.pushCsv);
			$('#'+$.id.btn_csv+3).on("click", that.pushCsv);
			$('#'+$.id.btn_csv+4).on("click", that.pushCsv);
		},
		initCondition: function (){	// 条件初期値セット
			var that = this;
			// 初期化項目
		},
		clear:function(){
			// 隠し情報初期化
			$($.id.hiddenChangedIdx).val("");						// 変更行Index
//			// グリッド初期化
//			this.success(this.name, false);
		},
		validation: function (btnId){	// （必須）批准
			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform).form('validate');

			//3.1.1．検索条件の表示年月チェック：
			// ①	表示年月Fromと表示年月To未入力の場合、全てのレコードを検索する。
			// ②	表示年月From未入力、表示年月Toだけ入力の場合、エラー。
			// ③	表示年月Fromだけ入力、表示年月To未入力の場合、>=表示年月Fromの内容を検索。
			// ④	表示年月From >　表示年月Toの場合、エラー。
			var szStym		= $.getInputboxValue($('#'+$.id_inp.txt_stym), "", true);	// 表示年月From
			var szEnym		= $.getInputboxValue($('#'+$.id_inp.txt_enym), "", true);	// 表示年月To
			if(rt){
				if(szStym === "" && szEnym !== ""){			// ②
					$.showMessage('E20277', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_stym), true)});
					rt = false;
				}
			}
			if(rt){
				if (szEnym !== "" && szStym > szEnym){
					$.showMessage('E20137', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_stym), true)});
					rt = false;
				}
			}

			// 入力エラーなしの場合に検索条件を格納
			if (rt == true) that.jsonString = that.jsonTemp.slice(0);
			// 入力チェック用の配列をクリア
			that.jsonTemp = [];
			return rt;
		},
		success: function(reportno, sortable, btnId, updateidx){	// （必須）正処理
			if (sortable) sortable=1; else sortable=0;
			var that = this;
			// 検索実行
			var szStym		= $.getJSONObject(this.jsonString, $.id_inp.txt_stym).value;	// 表示年月From
			var szEnym		= $.getJSONObject(this.jsonString, $.id_inp.txt_enym).value;	// 表示年月To
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
					STYM:			szStym,			// 表示年月From
					ENYM:			szEnym,			// 表示年月To
					t:				(new Date()).getTime(),
					sortable:		sortable,
					sortName:		that.sortName,
					sortOrder:		that.sortOrder,
					rows:			0	// 表示可能レコード数
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
					that.queried2= true;
					that.pushBtnid = btnId;
					$($.id.hiddenChangedIdx).val("");						// 変更行Index

					// Load処理回避
					$.tryChangeURL($.reg.jqeasy);

					// グリッド再描画（easyui 1.4.2 対応）
					$($.id.gridholder).datagrid('load', {} );
					$.removeMask();
					$.removeMaskMsg();

					// ログ出力
					$.log(that.timeData, 'loaded:');
				}
			);
		},
		convertTable:{
			F1 : "F26",		//F1	MOYSKBN		催し区分
			F2 : "F27",		//F2	MOYSSTDT	催し開始日
			F3 : "F28",		//F3	MOYSRBAN	催し連番
			F4 : "F5",		//F4	HBOKUREFLG	販売日1日遅許可フラグ
			F5 : "F7",		//F5	GTSIMEDT	月締日
			F6 : "F9",		//F6	GTSIMEFLG	月締フラグ
			F7 : "F10",		//F7	LSIMEDT		最終締日
			F8 : "F12",		//F8	QAYYYYMM	アンケート月度
			F9 : "F13",		//F9	QAENO		アンケート月度枝番
			F10: "F14"	,	//F10	QACREDT		アンケート作成日
			F11: "F16",		//F11	QARCREDT	アンケート再作成日
			F12: "F20",		//F12	JLSTCREFLG	事前発注リスト作成済フラグ
			F13: "F21",		//F13	HNCTLFLG	本部コントロールフラグ
			F14: "F22",		//F14	TPNG1FLG	店不採用禁止フラグ
			F15: "F23",		//F15	TPNG2FLG	店売価選択禁止フラグ
			F16: "F24",		//F16	TPNG3FLG	店商品選択禁止フラグ
			//F17: "",		//F17	SIMEFLG1_LD	仮締フラグ_リーダー店
			//F18: "",		//F18	SIMEFLG2_LD	本締フラグ_リーダー店
			//F19: "",		//F19	SIMEFLG_MB	本締フラグ_各店
			F20: "F18",		//F20	QADEVSTDT	アンケート取込開始日
			F21: "F32",		//F21	UPDKBN	更新区分
			//F22: "",			// F22	SENDFLG	送信フラグ
			//F23: "",			// F23	OPERATOR	オペレータ
			//F24: "",			// F24	ADDDT	登録日
			F25: "F31",			// F25	UPDDT	更新日

			ChangedIdx: "F33"	// F33	変更フラグ
		},
		getGridData: function (index, isCsv, target){
			var that = this;

			var data = {};

			// 基本情報
			if(target===undefined || target==="grd_data"){
				var targetData =[];
				var rows = $($.id.gridholder).datagrid('getRows');
				var s = index ? index : 0;
				var e = index ? index*1+1 : rows.length;
				for (var i = s; i < e; i++){
					if(isCsv && rows[i]["CSV"]!==$.id.value_on) continue;
					var rowData = {
						F1 : ""+rows[i][that.convertTable.F1],				//F1	MOYSKBN	催し区分
						F2 : ""+rows[i][that.convertTable.F2],				//F2	MOYSSTDT	催し開始日
						F3 : ""+rows[i][that.convertTable.F3],				//F3	MOYSRBAN	催し連番
						F4 : ""+rows[i][that.convertTable.F4],				//F4	HBOKUREFLG	販売日1日遅許可フラグ
						F5 : $.getParserDt(rows[i][that.convertTable.F5]),	//F5	GTSIMEDT	月締日
						F6 : ""+rows[i][that.convertTable.F6],				//F6	GTSIMEFLG	月締フラグ
						F7 : $.getParserDt(rows[i][that.convertTable.F7]),	//F7	LSIMEDT	最終締日
						F8 : $.getParserYm(rows[i][that.convertTable.F8]),	//F8	QAYYYYMM	アンケート月度
						F9 : ""+rows[i][that.convertTable.F9],				//F9	QAENO	アンケート月度枝番
						F10: $.getParserDt(rows[i][that.convertTable.F10]),	//F10	QACREDT	アンケート作成日
						F11: $.getParserDt(rows[i][that.convertTable.F11]),	//F11	QARCREDT	アンケート再作成日
						F12: ""+rows[i][that.convertTable.F12],				//F12	JLSTCREFLG	事前発注リスト作成済フラグ
						F13: ""+rows[i][that.convertTable.F13],				//F13	HNCTLFLG	本部コントロールフラグ
						F14: ""+rows[i][that.convertTable.F14],				//F14	TPNG1FLG	店不採用禁止フラグ
						F15: ""+rows[i][that.convertTable.F15],				//F15	TPNG2FLG	店売価選択禁止フラグ
						F16: ""+rows[i][that.convertTable.F16],				//F16	TPNG3FLG	店商品選択禁止フラグ
//							F17: ""+rows[i][i][that.convertTable.F17],				//F17	SIMEFLG1_LD	仮締フラグ_リーダー店
//							F18: ""+row[that.convertTable.F18],				//F18	SIMEFLG2_LD	本締フラグ_リーダー店
//							F19: ""+rows[i][trows[i]s.convertTable.F19],				//F19	SIMEFLG_MB	本締フラグ_各店
						F20: $.getParserDt(rows[i][that.convertTable.F20]),	//F20	QADEVSTDT	アンケート取込開始日
//							F21: ""+rows[i][that.convertTable.F21],				//F21	UPDKBN	更新区分
//							F22: ""+rows[i][that.convertTable.F22],				//F22	SENDFLG	送信フラグ
//							F23: ""+rows[i][that.convertTable.F23],				//F23	OPERATOR	オペレータ
//							F24: ""+rows[i][that.convertTable.F24],				//F24	ADDDT	登録日
						F25: ""+rows[i][that.convertTable.F25],				//F25	UPDDT	更新日

						RNO : i					// 行番号(チェック用に保持)
					};
					targetData.push(rowData);
				}
				data["grd_data"] = targetData;
			}


			// 補足情報(テーブルに登録しない情報などを保持)
			if(target===undefined || target==="grd_data_other"){
				var targetData = [{}];
				data["grd_data_other"] = targetData;
			}

			return data;
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

			return true;
		},
		updValidation: function (id){	// （必須）批准
			var that = this;
			var errMsg= "";

			var index = id.replace("btn_enter_", "");

			// 行 の 'validate' 実施
			var rt = $($.id.gridholder).datagrid('validateRow', index);
			if(!rt){
				$.showMessage('E00001');
				return rt;
			}
			rt = $.endEditingDatagrid(that);
			if(!rt){
				$.showMessage('E00001');
				return rt;
			}

			// 登録行の内容チェック
			var record		=$($.id.gridholder).datagrid('getRows')[index];	// 検証用情報取得
			if(record[that.convertTable.ChangedIdx]!=='1'){
				$.showMessage('E20582');
				return false;
			}

			var moyskbn		=record[that.convertTable.F1];	// 催し区分
			var moysstdt	=record[that.convertTable.F2];	// 催し開始日
			var moysrban	=record[that.convertTable.F3];	// 催し連番
			// 必須チェック
			var targetOId = [$.id_inp.txt_gtsimedt,$.id_inp.txt_lsimedt,$.id_inp.txt_qayyyym,$.id_inp.txt_qacredt,$.id_inp.txt_qadevstdt];
			var targetCId = ["F7","F10","F12","F14","F18"];
			for (var j = 0; j < targetOId.length; j++){
				if($.isEmptyVal(record[targetCId[j]])){
					$.showMessage('E00001', undefined, function(){$.addErrState(that, $($.id.gridholder), true, {NO:index, ID:targetOId[j]})});
					return false;
				}
			}

			// 共通チェック
			targetOId = [$.id_inp.txt_gtsimedt,$.id_inp.txt_lsimedt,$.id_inp.txt_qayyyymm,$.id_inp.txt_qaend,$.id_inp.txt_qacredt,$.id_inp.txt_qarcredt,$.id_inp.txt_qadevstdt,$.id.chk_hnctlflg,$.id.chk_tpng1flg];
			targetCId = ["F7","F10","F12","F13","F14","F16","F18","F21","F22"];
			for (var j = 0; j < targetOId.length; j++){
				msgid = that.checkInputboxFunc(targetOId[j], record[targetCId[j]], record, index, moyskbn, moysstdt, moysrban);
				if(msgid !==null){
					$.showMessage(msgid, undefined, function(){$.addErrState(that, $($.id.gridholder), true, {NO:index, ID:targetOId[j]})});
					return false;
				}
			}

			// 入力情報を変数に格納
			if (rt == true) that.setGridData(that.getGridData(index));	// 更新用情報取得

			return rt;
		},
		updSuccess: function(id){	// validation OK時 の update処理
			var that = this;

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$($.id.gridholder).datagrid('loading');

			var index = id.replace("btn_enter_", "");

			// 変更行情報取得
			var changedIndex = $($.id.hiddenChangedIdx).val().split(",");

			// 基本情報
			var targetData = that.grd_data;
			// 補足情報
			var targetDataOther = that.grd_data_other;

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,			// レポート名
					action:			$.id.action_update,	// 実行処理情報
					obj:			id,					// 実行オブジェクト
					SENDBTNID:		that.sendBtnid,
					DATA:			JSON.stringify(targetData),				// 更新対象情報
					DATA_OTHER:		JSON.stringify(targetDataOther),		// 更新対象補足情報
					t:				(new Date()).getTime()
				},
				function(data){
					// 検索処理エラー判定
					if($.updError(id, data)) return false;
					var afterFunc = function(){
						// 初期化
						that.updateRowIdx = index;
						that.success(that.name, false, id);
					};
					$.updNormal(data, afterFunc);

					// ログ出力
					$.log(that.timeData, 'loaded:');
				}
			);
		},
		/**
		 * CSV出力ボタンイベント
		 * @param {Object} e
		 */
		pushCsv : function(e){

			// TODO：仮
			alert("現在CSV出力機能は停止中です。");
			return false;


			if ($(this).linkbutton('options').disabled)	return false;

			// レポート番号取得
			var reportno=$($.id.hidden_reportno).val();
			var id = $(this).attr('id');

			// レポート定義位置
			var reportNumber = $.getReportNumber(reportno);
			if (typeof(reportNumber) !== 'number') { alert("レポート定義が見つかりません。"); return false;}

			// マスク削除
			$.removeMask();

//			　　3.4.2．出力手順：
//			① 【画面】.「チラシ原稿CSV出力」、【画面】.「POP原稿CSV出力」、【画面】.「アンケートCSV出力」ボタンを押下時に、「昨日までの情報でデータを作成します。よろしいですか？」の「はい、いいえ」ダイアログを表示する。「はい」を選択した場合、処理を続ける。「いいえ」を選択した場合、処理を中止する。
//			② 「CSV対象」欄チェック状態の行を出力する。
//			③ CSV出力開始メッセージを表示する。
//			④ CSV出力を開始する。
			if ($.report[reportNumber].csvValidation(id)) {

				var func_ok = function(){
					// マスク追加
					$.appendMask();

					// セッションタイムアウト、利用時間外の確認
					var isTimeout = $.checkIsTimeout();
					if (! isTimeout) {
						// ログ保持
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
								$.appendMaskMsg($.getMessage("I00004"));	// I00004	CSV出力開始しました。	 	0	 	I

								// CSV 出力
								$.report[reportNumber].srccsv(reportno, id);
							}
						});
					}
				};


				// ① 【画面】.「チラシ原稿CSV出力」、【画面】.「POP原稿CSV出力」、【画面】.「アンケートCSV出力」ボタンを押下時に、「昨日までの情報でデータを作成します。よろしいですか？」の「はい、いいえ」ダイアログを表示する。「はい」を選択した場合、処理を続ける。「いいえ」を選択した場合、処理を中止する。
				var msgid = id===$.id.btn_csv+4 ? "W20015" : "W20006";
				// W20015	CSVデータを出力します。よろしいですか？	 	4	 	Q
				// W20006	昨日までの情報でデータを作成します。	よろしいですか？	4	 	Q
				$.showMessage(msgid, undefined, func_ok);
			} else {
				return false;
			}
		},
		csvValidation: function (btnId){	// （必須）批准
			var that = this;

			var rt = $.endEditingDatagrid(that);
			if(!rt){
				$.showMessage('E00001');
				return rt;
			}


			var gridData = that.getGridData(undefined, true);	// 更新用情報取得

			if(gridData["grd_data"].length === 0){
				// E20282	「CSV対象」をチェックしてください。	 	0	 	E
				$.showMessage('E20282');
				return false;
			}


			//　　3.4.1．出力チェック：
			//① 【画面】.「アンケートCSV出力」を押した場合、催しを複数選択したら各催しの「アンケート作成日」が同じでなくてはならない。
			if(btnId===$.id.btn_csv+3){
				var keys = [];
				for (var i=0; i<gridData["grd_data"].length; i++){
					keys.push(gridData["grd_data"][i]["F10"]);
				}
				if(keys.filter(function (element, index, self) { return self.indexOf(element)*1 === index; }).length > 1){
					// E20283	各催しのアンケート作成日は同じでなくてはなりません。	 	0	 	E
					$.showMessage('E20283');
					return false;
				}
			}

			// 入力情報を変数に格納
			if (rt == true) that.setGridData(gridData);

			return rt;
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

			// 表示年月From
			this.jsonTemp.push({
				id:		$.id_inp.txt_stym,
				value:	$.getInputboxValue($('#'+$.id_inp.txt_stym), undefined, true),
				text:	$('#'+$.id_inp.txt_stym).numberbox('getText')
			});
			// 表示年月To
			this.jsonTemp.push({
				id:		$.id_inp.txt_enym,
				value:	$.getInputboxValue($('#'+$.id_inp.txt_enym), undefined, true),
				text:	$('#'+$.id_inp.txt_enym).numberbox('getText')
			});
		},
		setObjectState: function(){	// 軸の選択内容による制御

		},
		extenxDatagridEditorIds:{
			 F5		: "chk_hbokureflg"		// チェックボックス（1日遅パターン有)
			,F7		: "txt_gtsimedt"		// テキスト（月締め)
			,F9		: "chk_gtsimeflg"		// チェックボックス（月締)
			,F10	: "txt_lsimedt"			// テキスト（最終締)
			,F12	: "txt_qayyyymm"		// テキスト（アンケート月度)
			,F13	: "txt_qaend"			// テキスト（アンケート月度枝番)
			,F14	: "txt_qacredt"			// テキスト（アンケート作成日)
			,F16	: "txt_qarcredt"			// テキスト（アンケート再作成日)
			,F18	: "txt_qadevstdt"		// テキスト（アンケート取込開始日)
			,F20	: "chk_jlstcreflg"		// チェックボックス（事発リスト作成済)
			,F21	: "chk_hnctlflg"		// チェックボックス（アンケート本部ctl)
			,F22	: "chk_tpng1flg"		// チェックボックス（店不採用禁止)
			,F23	: "chk_tpng2flg"		// チェックボックス（店売価選択禁)
			,F24	: "chk_tpng3flg"		// チェックボックス（店商品選択禁止)
		},
		setGrid: function (id, reportNumber){	// グリッドの構築
			var that = this;
			var init = true;
			// ページサイズ定義取得
			var pageList = $.fn.pagination.defaults.pageList;
			var pageSize = $.getJSONValue(this.jsonHidden, $.id.pageSize);
			if (pageSize==="") pageSize=$.fn.pagination.defaults.pageSize;
			pageSize = $.getDefaultPageSize(pageSize, pageList);

			var fColumns= [], fColumnBottom=[];
			var columns = [], columnBottom=[];

			var bcstyler =function(value,row,index){return 'background-color:#f5f5f5;';};
			var dformatter =function(value){ return $.getFormatDt(value, true);};
			var cstyler =function(value,row,index){return 'color: red;font-weight: bold;';};
			var cstyler2=function(value,row,index){return 'color: red;font-weight: bold;background-color:#f5f5f5;';};
			var cformatter =function(value,row,index){return value && value===$.id.value_on?$.id.text_on:$.id.text_off;};

			var f13_check = $('#'+that.extenxDatagridEditorIds["F13"]).attr("check") ? JSON.parse('{'+$('#'+that.extenxDatagridEditorIds["F13"]).attr("check")+'}'): JSON.parse('{}');

			var editor_n=undefined,editor_c=undefined;
			if(that.reportYobiInfo()!=='1'){
				fColumnBottom.push({field:'ENTER',	title:'　',				width:55,	halign:'center',align:'center',	formatter:function(value,row,index){ return '<a href="#" title="登録" id="btn_enter_'+index+'" class="easyui-linkbutton"><span>登録</span></a>'}});
				editor_n = 'numberbox';
				editor_c = 'checkbox';
			}
			fColumnBottom.push({field:'SEL',	title:'選択',				width:35,	halign:'center',align:'center',	formatter:cformatter,	editor:{type:'checkbox'},	styler:cstyler2});
			fColumnBottom.push({field:'CSV',	title:'CSV対象',			width:35,	halign:'center',align:'center',	formatter:cformatter,	editor:{type:'checkbox'},	styler:cstyler});
			fColumnBottom.push({field:'F1',	title:'変申請',					width:45,	halign:'center',align:'right',	styler:bcstyler});
			fColumnBottom.push({field:'F2',	title:'店G設定',				width:35,	halign:'center',align:'center',	formatter:cformatter,	styler:cstyler2});
			fColumnBottom.push({field:'F3',	title:'催しコード',				width:95,	halign:'center',align:'left',	styler:bcstyler});
			fColumnBottom.push({field:'F4',	title:'催し名称',				width:270,	halign:'center',align:'left',	styler:bcstyler});
			fColumnBottom.push({field:'F5',	title:'1日遅<br>ﾊﾟﾀﾝ有',		width:50,	halign:'center',align:'center',	formatter:cformatter,	editor:{type:editor_c},	styler:cstyler});
			fColumnBottom.push({field:'F6',	title:'催し期間',				width:180,	halign:'center',align:'left',	styler:bcstyler});
			columnBottom.push({field:'F7',	title:'月締め',					width:72,	halign:'center',align:'left',	formatter:dformatter,	editor:{type:editor_n}});
			columnBottom.push({field:'F8',	title:'　',						width:30,	halign:'center',align:'center',	styler:bcstyler});
			columnBottom.push({field:'F9',	title:'月締',					width:30,	halign:'center',align:'center',	formatter:cformatter,	editor:{type:editor_c},	styler:cstyler});
			columnBottom.push({field:'F10',	title:'最終締',					width:72,	halign:'center',align:'left',	formatter:dformatter,	editor:{type:editor_n}});
			columnBottom.push({field:'F11',	title:'　',						width:30,	halign:'center',align:'center',	styler:bcstyler});
			columnBottom.push({field:'F12',	title:'アンケート月度',			width:72,	halign:'center',align:'left',	formatter:dformatter,	editor:{type:editor_n}});
			columnBottom.push({field:'F13',	title:'月度枝番',				width:35,	halign:'center',align:'left',	formatter:function(value,row,index){ return $.getFormatLPad(value, f13_check.maxlen);},	editor:{type:editor_n}});
			columnBottom.push({field:'F14',	title:'アンケート作成日',		width:72,	halign:'center',align:'left',	formatter:dformatter,	editor:{type:editor_n}});
			columnBottom.push({field:'F15',	title:'　',						width:30,	halign:'center',align:'center',	styler:bcstyler});
			columnBottom.push({field:'F16',	title:'アンケート再作成日',		width:72,	halign:'center',align:'left',	formatter:dformatter,	editor:{type:editor_n}});
			columnBottom.push({field:'F17',	title:'　',						width:30,	halign:'center',align:'center',	styler:bcstyler});
			columnBottom.push({field:'F18',	title:'アンケート取込開始日',	width:72,	halign:'center',align:'left',	formatter:dformatter,	editor:{type:editor_n}});
			columnBottom.push({field:'F19',	title:'　',						width:30,	halign:'center',align:'center',	styler:bcstyler});
			columnBottom.push({field:'F20',	title:'事発リスト作成済',		width:60,	halign:'center',align:'center',	formatter:cformatter,	editor:{type:editor_c},	styler:cstyler});
			columnBottom.push({field:'F21',	title:'本部Ctrl',				width:35,	halign:'center',align:'center',	formatter:cformatter,	editor:{type:editor_c},	styler:cstyler});
			columnBottom.push({field:'F22',	title:'店不採用<br>禁止',		width:60,	halign:'center',align:'center',	formatter:cformatter,	editor:{type:editor_c},	styler:cstyler});
			columnBottom.push({field:'F23',	title:'店売価<br>選択禁',		width:60,	halign:'center',align:'center',	formatter:cformatter,	editor:{type:editor_c},	styler:cstyler});
			columnBottom.push({field:'F24',	title:'店商品<br>選択禁止',		width:60,	halign:'center',align:'center',	formatter:cformatter,	editor:{type:editor_c},	styler:cstyler});
			columnBottom.push({field:'F25',	title:'入力数',					width:50,	halign:'center',align:'right',	styler:bcstyler});
			fColumns.push(fColumnBottom);
			columns.push(columnBottom);

			var funcClickRow = $.fn.datagrid.defaults.onClickRow;
			var funcBeginEdit= $.fn.datagrid.defaults.onBeginEdit;
			var funcEndEdit= $.fn.datagrid.defaults.onEndEdit;
			var funcAfterEdit= $.fn.datagrid.defaults.onAfterEdit;

			that.editRowIndex[id] = -1;
			funcClickRow = function(index,field){$.clickEditableDatagridCell(that,id, index)};
			funcBeginEdit = function(index,row){
				// 3.1.3．表示制御：
				// 3.1.3.1．【画面】.当行の「アンケート作成日」< 処理日付.処理日付の場合、【画面】.当行の「アンケート再作成日」以外の入力項目（CSV対象を除く）を編集不可にする。
				// 3.1.3.2．【画面】.当行の「アンケート取込開始日」<= 処理日付.処理日付の場合、【画面】.当行の「CSV対象」以外の入力項目を編集不可にする。
				var notDisableEds = undefined, disabledEds;
				if(that.reportYobiInfo()!=='1'){
					var txt_shoridt = $('#'+$.id.txt_shoridt).val();	// 処理日付.処理日付
					var txt_qacredt = $.getParserDt(row["F14"]);		// アンケート作成日
					var txt_qadevstdt = $.getParserDt(row["F18"]);		// アンケート取込開始日
					if(!$.isEmptyVal(txt_qacredt) && txt_qacredt < txt_shoridt){		// 3.1.3.1
						notDisableEds = [$.id.chk_sel, $.id.chk_csv, $.id_inp.txt_qarcredt];
					}
					if(!$.isEmptyVal(txt_qadevstdt) && txt_qadevstdt <= txt_shoridt){	// 3.1.3.2
						notDisableEds = [$.id.chk_sel, $.id.chk_csv];
					}
					// 1日遅パターン有:数値1桁。当催しの販売開始日と販売終了日が同じの場合（販売期間が１日の場合）、チェック不可
					disabledEds = [$.id.chk_hbokureflg];	// 1日遅パターン有
				}else{
					notDisableEds = [$.id.chk_csv];
				}

				if(notDisableEds){
					$($('#'+id).datagrid('getEditors', index)).each(function(){
						var refid = $.getExtendDatagridEditorRefid(this.target);
						if(notDisableEds.indexOf(refid) === -1){
							$.setInputBoxDisable($(this.target));
						}
					});
				}else if(disabledEds){
					$($('#'+id).datagrid('getEditors', index)).each(function(){
						var refid = $.getExtendDatagridEditorRefid(this.target);
						if(disabledEds.indexOf(refid) !== -1){
							$.setInputBoxDisable($(this.target));
						}
					});
				}
				$.beginEditDatagridRow(that,id, index, row)
			};
			funcEndEdit = function(index,row,changes){
				$.endEditDatagridRow(that, id, index, row);
				row.SEL = $.id.value_off;
			};
			funcAfterEdit = function(index,row,changes){
				var rowobj = $('#'+id).datagrid('getPanel').find("[datagrid-row-index='"+index+"']");
				// チェックボックスの再追加（EndEdit時に削除されるため）
				$.afterEditAddCheckbox(rowobj);
				// ボタンオブジェクトの再追加（EndEdit時に削除されるため）
				rowobj.find(".easyui-linkbutton").on("click", $.pushUpd).linkbutton({ width:  45, height: 18});
			};

			// 編集可能データグリッドの共通処理設定
			// 編集エディターの機能拡張（非表示で参照用のオブジェクトが準備してある前提、参照オブジェクトはグリッドのフィールド名を元に判断）
			$.extendDatagridEditor(that);

			$('#'+id).datagrid({
				nowrap: true,
				border: true,
				striped: false,
				collapsible:false,
				remoteSort: true,
				rownumbers:false,
				fit:true,
				view:scrollview,
				pageSize:pageSize,
				pageList:pageList,
				frozenColumns:fColumns,
				columns:columns,
				fitColumns:false,	// 指定カラム幅を適用する場合、false 指定。
				onBeforeLoad:function(param){
					param.report = that.name;
				},
				onLoadSuccess:function(data){
					var panel = $('#'+id).datagrid('getPanel');

					// View内の入出力項目を調整
					if(that.reportYobiInfo()!=='1'){
						var inputs = panel.find('.datagrid-row .easyui-linkbutton');
						// 各行内InputをEasyUI形式に変換（class指定のInput作成だけだと普通のInputになったため）
						inputs.on("click", $.pushUpd).linkbutton({ width:  45, height: 18});
					}

					// 更新後表示の場合
					if(that.updateRowIdx){
						panel.find("[datagrid-row-index='"+that.updateRowIdx+"']").css('color', 'blue');
						that.updateRowIdx = undefined;
					}

					// 検索後、初回のみ処理
					if (that.queried2){
						that.queried2= false;	// 検索後、初回のみ処理

						// 状態保存
						$.saveState(reportNumber, that.getJSONString(), '#'+id);
						// 警告
						$.showWarningMessage(data);
					}

					// 前回選択情報をGridに反映
					var getRowIndex = $.getJSONValue(that.jsonHidden, "scrollToIndex_"+'#'+id);
					var rows = $('#'+id).datagrid('getRows');
					getRowIndex = $.isEmptyVal(getRowIndex) && rows.length !== 0 ? 0 : getRowIndex;

					if(getRowIndex !== ""){
						$('#'+id).datagrid('scrollTo', {
							index: getRowIndex,
							callback: function(index){
								$('#'+id).datagrid('selectRow', index);
								$('#'+id).datagrid('beginEdit', index);
							}
						});

						// 検索可能な一覧画面では、検索時に前回選択情報が適用されてしまう為、
						// 一度適用したら要素を削除する。
						var targetName = "scrollToIndex_"+'#'+id;
						that.jsonHidden.some(function(v, i){
						    if (v.id==targetName) that.jsonHidden.splice(i,1);
						});
					}
				},
				onClickCell:function(rowIndex, field, value){
					// 列名保持
					that.columnName = field;
				},
				onSelect:function(index,row){
					//選択をチェックする。
					row.SEL = $.id.value_on;
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
			var newrepinfos = $.getBackJSON(that, states, true);
			$.setJSONObject(sendJSON, 'repinfo', newrepinfos, '');

			// 選択行
			var row = $($.id.gridholder).datagrid("getSelected");

			// 実行ボタン別処理
			switch (btnId) {
			case $.id.btn_sel_change+1:	// 選択(商品一覧)
			case $.id.btn_sel_change+2:	// 選択(変更許可)
			case $.id.btn_sel_change+3:	// 選択(店グループ)
				if(!row){
					$.showMessage('E00008');
					return false;
				}

				// 転送先情報
				index = that.repgrpInfo.TG008.idx;
				if(btnId===$.id.btn_sel_change+3){index = that.repgrpInfo.TG002.idx;}	// 店グループ

				childurl = href[index];
				sendMode = 1;
				// オブジェクト作成
				$.setJSONObject(sendJSON, $.id_inp.txt_moyskbn,  row.F26, row.F26);
				$.setJSONObject(sendJSON, $.id_inp.txt_moysstdt, row.F27, row.F27);
				$.setJSONObject(sendJSON, $.id_inp.txt_moysrban, row.F28, row.F28);
				break;
			case $.id.btn_back:
			case $.id.btn_cancel:
				// 転送先情報
				childurl = parent.$('#hdn_menu_path').val();

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
		srccsv: function(reportno, btnId){	// ここではCsv出力
			var that = this;
			// 処理時間計測用
			that.timeData = (new Date()).getTime();

			var that = this;
			// 検索実行


			// 基本情報
			var targetData = that.grd_data;
			// 補足情報
			var targetDataOther = that.grd_data_other;

			if(!btnId) btnId = $.id.btn_search;

			var kbn = 0;
			var data = {
				report:			that.name,		// レポート名
				'kbn':			 kbn,
				'type':			'fix',			// 固定長
				BTN:			btnId,
				DATA:			JSON.stringify(targetData),				// 更新対象情報
				DATA_OTHER:		JSON.stringify(targetDataOther),		// 更新対象補足情報
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
				// ログ出力
				$.log(that.timeData, 'srcexcel:');
			});
		},
		focusInputboxFunc:function(that, id, obj){
			// グリッド編集系処理
			if(that.focusGridId!==undefined && that.editRowIndex[that.focusGridId] > -1){
				var record = $('#'+that.focusGridId).datagrid("getRows")[that.editRowIndex[that.focusGridId]];
				var moysstdt	=record[that.convertTable.F2];	// 催し開始日
				// アンケート月度:フォーカスインのタイミングで、催しコードがあると本項目がNULLの場合のみ、【画面】.「アンケート月度」と【画面】.「月度枝番」に初期表示を行う。
				if(id===$.id_inp.txt_qayyyymm){
					var txt_qayyyymm = $('#'+$.id_inp.txt_qayyyymm+"_").numberbox('getValue');	// アンケート月度
					if($.isEmptyVal(txt_qayyyymm)){
						$('#'+$.id_inp.txt_qayyyymm+"_").numberbox('setValue', moysstdt.substr(0, 4));
						$('#'+$.id_inp.txt_qaend+"_").numberbox('setValue', '00');

						record[that.convertTable.ChangedIdx] = '1';		// CHANGE_IDX
					}
				}
			}
		},
		changeInputboxFunc:function(that, id, newValue, obj){

			var parentObj = $('#'+that.focusRootId);
			var func_focus = function(){$.addErrState(that, $('#'+obj.attr('id')), true)};

			// DB問い合わせ系
			if($('[for_inp^='+id+'_]').length > 0){
				var param = that.getInputboxParams(that, id, newValue);
				$.getsetInputboxRowData(that.name, 'for_inp', id, param, that, parentObj);
			}

			// グリッド編集系
			if(that.focusGridId!==undefined && that.editRowIndex[that.focusGridId] > -1){
				var wfield = "";
				// テキスト（月締め)
				if(id===$.id_inp.txt_gtsimedt){ wfield = "F8"; }
				// テキスト（最終締)
				if(id===$.id_inp.txt_lsimedt){  wfield = "F11"; }
				// テキスト（アンケート作成日)
				if(id===$.id_inp.txt_qacredt){  wfield = "F15"; }
				// テキスト（アンケート再作成日)
				if(id===$.id_inp.txt_qarcredt){ wfield = "F17"; }
				// テキスト（アンケート取込開始日)
				if(id===$.id_inp.txt_qadevstdt){wfield = "F19"; }
				if(wfield!==""){
					var row   = $('#'+that.focusGridId).datagrid('getRows')[that.editRowIndex[that.focusGridId]];
					var rowobj= $('#'+that.focusGridId).datagrid('getPanel').find("[datagrid-row-index="+that.editRowIndex[that.focusGridId]+"]");
					row[wfield] = $.getFormatWeek(newValue);
					rowobj.find('[field='+wfield+']').find('div').text(row[wfield]);
				}
			}

			// 検索、入力後特殊処理
			if(that.queried){
				var msgid = null;

				// グリッド編集系処理
				if(that.focusGridId!==undefined && that.editRowIndex[that.focusGridId] > -1){
					var record = $('#'+that.focusGridId).datagrid("getRows")[that.editRowIndex[that.focusGridId]];
					var moyskbn		=record[that.convertTable.F1];	// 催し区分
					var moysstdt	=record[that.convertTable.F2];	// 催し開始日
					var moysrban	=record[that.convertTable.F3];	// 催し連番

					// グリッド編集系チェック処理
					msgid = that.checkInputboxFunc(id, newValue, undefined, that.editRowIndex[that.focusGridId], moyskbn, moysstdt, moysrban);
					if(msgid !==null){
						$.showMessage(msgid, undefined, func_focus );
						return false;
					}

					// グリッド編集系変更処理
					record[that.convertTable.ChangedIdx] = '1';		// CHANGE_IDX
				}


			}
		},
		// IDとvalueでチェック処理を実施
		checkInputboxFunc:function(id, newValue, record, index, moyskbn, moysstdt, moysrban){
			var that = this;
			var sdt, edt;

			// 各種グリッドチェック
			var txt_gtsimedt	// 月締め
			var txt_lsimedt;	// 最終締
			var txt_qayyyymm;	// アンケート月度
			var txt_qaend;		// アンケート月度枝番
			var txt_qacredt;	// アンケート作成日
			var txt_qarcredt;	// アンケート再作成日
			var txt_qadevstdt;	// アンケート取込開始日
			var chk_hnctlflg;	// アンケート本部ctl
			var chk_tpng1flg;	// 店不採用禁止
			if(record){
				txt_gtsimedt	=record["F7"];	// 月締め
				txt_lsimedt		=record["F10"];	// 最終締
				txt_qayyyymm	=record["F12"];	// アンケート月度
				txt_qaend		=record["F13"];	// アンケート月度枝番
				txt_qacredt		=record["F14"];	// アンケート作成日
				txt_qarcredt	=record["F16"];	// アンケート再作成日
				txt_qadevstdt	=record["F18"];	// アンケート取込開始日
				chk_hnctlflg	=record["F21"];	// アンケート本部ctl
				chk_tpng1flg	=record["F22"];	// 店不採用禁止
			}else{
				txt_gtsimedt	=id===$.id_inp.txt_gtsimedt	?newValue : $('#'+$.id_inp.txt_gtsimedt+"_").numberbox('getValue');		// 月締め
				txt_lsimedt		=id===$.id_inp.txt_lsimedt	?newValue : $('#'+$.id_inp.txt_lsimedt+"_").numberbox('getValue');		// 最終締
				txt_qayyyymm	=id===$.id_inp.txt_qayyyymm	?newValue : $('#'+$.id_inp.txt_qayyyymm+"_").numberbox('getValue');		// アンケート月度
				txt_qaend		=id===$.id_inp.txt_qaend	?newValue : $('#'+$.id_inp.txt_qaend+"_").numberbox('getValue');		// アンケート月度枝番
				txt_qacredt		=id===$.id_inp.txt_qacredt	?newValue : $('#'+$.id_inp.txt_qacredt+"_").numberbox('getValue');		// アンケート作成日
				txt_qarcredt	=id===$.id_inp.txt_qarcredt	?newValue : $('#'+$.id_inp.txt_qarcredt+"_").numberbox('getValue');		// アンケート再作成日
				txt_qadevstdt	=id===$.id_inp.txt_qadevstdt?newValue : $('#'+$.id_inp.txt_qadevstdt+"_").numberbox('getValue');	// アンケート取込開始日
				chk_hnctlflg	=id===$.id.chk_hnctlflg		?newValue : $.getInputboxValue($('#'+$.id.chk_hnctlflg+"_"));			// アンケート本部ctl
				chk_tpng1flg	=id===$.id.chk_tpng1flg		?newValue : $.getInputboxValue($('#'+$.id.chk_tpng1flg+"_"));			// 店不採用禁止
			}
			var shoridt = $('#'+$.id.txt_shoridt).val();

			// *** 必須項目チェック ***
			// 【画面】.当行の「月締め」、【画面】.当行の「最終締」、【画面】.当行の「アンケート月度」、【画面】.当行の「アンケート作成日」、【画面】.当行の「アンケート取込開始日」何れかに入力があったら、五つの項目がすべて入力必須で処理する。
			if(record && (txt_gtsimedt+txt_lsimedt+txt_qayyyymm+txt_qacredt+txt_qadevstdt).length !== (6*4) + (4*1)){
				// E00001	必須入力項目が入力されていません。                                                                	 	0	 	E
				return "E00001";
			}

			// *** 関連項目チェック ***
			// 1.当催しの催し開始日>【画面】.当行の「アンケート取込開始日」> 【画面】.当行の「アンケート再作成日」 >【画面】.当行の「アンケート作成日」>【画面】.当行の「最終締」>【画面】.当行の「月締め」。
			if(id===$.id_inp.txt_qadevstdt){	// アンケート取込開始日
				if(!(moysstdt > txt_qadevstdt)){
					// E20183	 催し開始日 > アンケート取込開始日の条件で入力してください。	 	0	 	E
					return "E20183";
				}
				if(!$.isEmptyVal(txt_qarcredt) && !(txt_qadevstdt > txt_qarcredt)){
					// E20184	アンケート取込開始日 > アンケート再作成日の条件で入力してください。	 	0	 	E
					return "E20184";
				}
				if(!$.isEmptyVal(txt_qacredt) && !(txt_qadevstdt > txt_qacredt)){
					// E20186	アンケート取込開始日 > アンケート作成日の条件で入力してください。	 	0	 	E
					return "E20186";
				}
			}
			if(id===$.id_inp.txt_qarcredt){		// アンケート再作成日
				if(!$.isEmptyVal(txt_qarcredt) && !(txt_qarcredt > txt_qacredt)){
					// E20185	アンケート再作成日 > アンケート作成日の条件で入力してください。	 	0	 	E
					return "E20185";
				}
			}
			if(id===$.id_inp.txt_qacredt){		// アンケート作成日
				if(!$.isEmptyVal(txt_lsimedt) && !(txt_qacredt > txt_lsimedt)){
					// E20171	アンケート作成日 > 最終締の条件で入力してください。	 	0	 	E
					return "E20171";
				}
			}
			if(id===$.id_inp.txt_gtsimedt){		// 最終締
				if(!$.isEmptyVal(txt_lsimedt) && !(txt_lsimedt > txt_gtsimedt)){
					// E20172	最終締 > 月締めの条件で入力してください。	 	0	 	E
					return "E20172";
				}
			}

			// 2.【画面】.当行の「アンケート作成日」< 処理日付の場合は、【画面】.当行の「アンケート再作成日」>= 処理日付。
			if(id===$.id_inp.txt_qarcredt){		// アンケート再作成日
				if(!$.isEmptyVal(txt_qacredt) && $.getParserDt(txt_qacredt) < shoridt ){
					if(!$.isEmptyVal(txt_qarcredt) && !($.getParserDt(txt_qarcredt) >= shoridt) ){
						// E20366	アンケート再作成日 ≧ 処理日付の条件で入力してください。	 	0	 	E
						return "E20366";
					}
				}
			}

			var param = {};
			param["KEY"]		= "CNT";
			param["QAYYYYMM"]	= $.getParserYm(txt_qayyyymm);	// アンケート月度
			param["QAENO"]		= txt_qaend;					// アンケート月度枝番
			param["QACREDT"]	= $.getParserDt( txt_qacredt);	// アンケート作成日
			param["QADEVSTDT"]	= $.getParserDt(txt_qadevstdt);	// アンケート取込開始日
			// 3.「アンケート月度枝番」と「アンケート作成日」チェック
			if(id===$.id_inp.txt_qacredt && !$.isEmptyVal(txt_qaend) && !$.isEmptyVal(txt_qacredt)){
				//  3.1.全店特売（アンケート有）_基本テーブルに、【画面】.当行対応のレコード以外に、【画面】.当行と同じ「アンケート月度枝番」を持つレコードがある場合、【画面】.当行の「アンケート作成日」と異なったら、エラー。
				//  3.2.全店特売（アンケート有）_基本テーブルに、【画面】.当行対応のレコード以外に、【画面】.当行と同じ「アンケート作成日」を持つレコードがある場合、【画面】.当行の「アンケート月度枝番」と異なったら、エラー。
				var chk_rows = $.getSelectListData(that.name, $.id.action_check, $.id_inp.txt_qacredt, [param]);
				// エラーの場合は、エラー情報が返ってくる
				if(chk_rows[0]["ID"]){
					return chk_rows[0]["ID"];
				}
			}

			// 4.「アンケート月度枝番」と「アンケート取込開始日」チェック
			if(id===$.id_inp.txt_qadevstdt && !$.isEmptyVal(txt_qaend) && !$.isEmptyVal(txt_qadevstdt)){
				//  4.1.全店特売（アンケート有）_基本テーブルに、【画面】.当行対応のレコード以外に、【画面】.当行と同じ「アンケート月度枝番」を持つレコードがある場合、【画面】.当行の「アンケート取込開始日」と異なったら、エラー。
				//  4.2.全店特売（アンケート有）_基本テーブルに、【画面】.当行対応のレコード以外に、【画面】.当行と同じ「アンケート取込開始日」を持つレコードがある場合、【画面】.当行の「アンケート月度枝番」と異なったら、エラー。
				var chk_rows = $.getSelectListData(that.name, $.id.action_check, $.id_inp.txt_qadevstdt, [param]);
				// エラーの場合は、エラー情報が返ってくる
				if(chk_rows[0]["ID"]){
					return chk_rows[0]["ID"];
				}
			}

			// 5.【画面】.当行の「本部Ctrl」がチェックされた場合、【画面】.当行の「店不採用禁止」がチェックされないと、エラー。
			if(id===$.id.chk_hnctlflg){		// アンケート本部Ctrl
				if( chk_hnctlflg===$.id.value_on && chk_tpng1flg !== $.id.value_on ){
					// E20178	本部Ctrlをチェックした場合、店不採用禁止をチェックしてください。	 	0	 	E
					return "E20178";
				}
			}
			return null;
		},
		// 販売期間開始日のデフォルト値算出
		calcDefHbstdt: function(newValue, kbn){
			if(newValue.length === 0){ return ''}
			return newValue;
		},
		// 納入期間開始日のデフォルト値算出
		calcDefNnstdt: function(tshuflg, newValue){
			if(newValue.length === 0){ return ''}
			if(tshuflg!==$.id.value_on){
				var dObj = $.convDate(newValue, true);
				for (var i = 0; i < 7; i++){
					if(dObj.getDay() === 0){
						break;
					}else{
						dObj.setDate(dObj.getDate() - 1);
					}
				}
				return $.dateFormat(dObj);
			}
			return newValue;
		},
		// ＰＬＵ配信日のデフォルト値算出
		calcDefPlusddt: function(newValue){
			var dObj = $.convDate(newValue, true);
			dObj.setDate(dObj.getDate() - 7);
			return $.dateFormat(dObj);
		},
		getInputboxParams: function(that, id, newValue){
			// 情報取得
			var values = {};
			values["value"] = newValue;
			// 情報設定
			return [values];
		}
	} });
})(jQuery);