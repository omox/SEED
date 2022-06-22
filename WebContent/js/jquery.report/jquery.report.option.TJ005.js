/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_ReportTJ005',			// （必須）レポートオプションの確認
		jsonTemp:	[],							// （必須）検索条件情報_入力チェック前
		jsonString:	[],							// （必須）検索条件情報
		jsonHidden: [],							// （必須）親画面からの引き継ぎ情報
		jsonInit: [],							// （必須）検索条件初期情報
		caption: function(){					// （必須）タイトル
			return $('#reportname').val();
		},
		sortName: '',							// ソート項目名
		sortOrder: '',							// ソート順
		timeData : (new Date()).getTime(),
		dedefaultObjNum:	22,					// 初期化オブジェクト数
		initObjNum:	-1,
		initedObject: [],
		maxMergeCell: 1,
		onChangeFlag : false,
		onChangeFlag2 : false,
		columnName:'',	// OnClickRowの列名
		queried : false,
		initializes : true,
		onChangeReport: false,
		reportYobiInfo: function(){				// （必須）レポートメニューに登録された予備情報＝帳票をどういった表示にしたいかの情報
			return $('#reportYobi1').val();
		},
		sendBtnid: "",							// （必須）呼出ボタンID情報
		focusRootId:"cc",						// （キー移動時必須）キー移動イベントのルートとなるパネルのID
		focusParentId:"",						// （キー移動時必須）現在フォーカスがあたっている項目の親となるパネルのID
		focusGridId:"",							// （キー移動時必須）現在フォーカスがあたっているDataGridのID
		editRowIndex:{},						// グリッド編集行保持
		gridData:[],							// 検索結果
		grd_fsirt_data:[],						// グリッド情報
		gridTitle:[],							// 検索結果
		grd_shn_data:[],						// グリッド情報:商品情報
		grd_ysn_data:[],						// グリッド情報:予算情報
		grd_day_data:[],						// グリッド情報:日付情報
		grd_all_data:[],
		updateData:[],							// 更新対象のrowを保持
		updateDataAdd:[],						// 更新対象のrow(新規追加)を保持
		dispAddRows:[],							// 表示用追加商品row(通常のページングでは追加行情報を保持できない為、javascriptで管理を行う)
		totalRows:0,
		thisPageNumber:0,						// ページ番号
		addShinNum:0,							// 追加商品データ数
		maxPageNumber:0,						// 最終ページ番号
		endGridLoad: false,						// グリッドのロード状態フラグ
		initialize: function (reportno){		// （必須）初期化
			var that = this;
			// 引き継ぎ情報
			this.jsonHidden = $.getTargetValue();
			// 画面の初回基本設定
			this.setInitObjectState();

			// 初期検索条件設定
			this.jsonInit = $.getInitValue();
			// データ表示エリア初期化

			// 初期化するオブジェクト数設定
			this.initObjNum = this.dedefaultObjNum;
			var isUpdateReport = true;

			// 初期表示処理
			that.onChangeReport = true;

			// 入力テキストボックス系
			var inputbox = Object.getOwnPropertyNames($.id_inp);
			for ( var sel in inputbox ) {
				if($('#'+$.id_inp[inputbox[sel]]).length > 0){
					$.setInputbox(that, reportno, $.id_inp[inputbox[sel]], isUpdateReport);
				}
			}
			$.setInputbox(that, reportno, 'txt_htsu1', isUpdateReport);
			$.setInputbox(that, reportno, 'txt_htsu2', isUpdateReport);
			$.setInputbox(that, reportno, 'txt_htsu3', isUpdateReport);
			$.setInputbox(that, reportno, 'txt_htsu4', isUpdateReport);
			$.setInputbox(that, reportno, 'txt_htsu5', isUpdateReport);
			$.setInputbox(that, reportno, 'txt_htsu6', isUpdateReport);
			$.setInputbox(that, reportno, 'txt_htsu7', isUpdateReport);
			$.setInputbox(that, reportno, 'txt_htsu8', isUpdateReport);
			$.setInputbox(that, reportno, 'txt_htsu9', isUpdateReport);
			$.setInputbox(that, reportno, 'txt_htsu10', isUpdateReport);
			$.setInputbox(that, reportno, 'txt_yosan1', isUpdateReport);
			$.setInputbox(that, reportno, 'txt_yosan2', isUpdateReport);
			$.setInputbox(that, reportno, 'txt_yosan3', isUpdateReport);
			$.setInputbox(that, reportno, 'txt_yosan4', isUpdateReport);
			$.setInputbox(that, reportno, 'txt_yosan5', isUpdateReport);
			$.setInputbox(that, reportno, 'txt_yosan6', isUpdateReport);
			$.setInputbox(that, reportno, 'txt_yosan7', isUpdateReport);
			$.setInputbox(that, reportno, 'txt_yosan8', isUpdateReport);
			$.setInputbox(that, reportno, 'txt_yosan9', isUpdateReport);
			$.setInputbox(that, reportno, 'txt_yosan10', isUpdateReport);

			$.winTJ006.init(that);	// 構成比
			$.winTJ007.init(that);	// 分類明細

			// Load処理回避
			//$.tryChangeURL(null);
			$.extendDatagridEditor(that);
			that.setEditableGrid2('gridholder', reportno);

			// 初期化終了
			this.initializes =! this.initializes;

			$.initialDisplay(that);

			// チェックボックスの設定
			$.initCheckboxCss($("#"+that.focusRootId));
			// キーイベントの設定
			$.initKeyEvent(that);

			// ログ出力
			$.log(that.timeData, 'initialize:');
		},
		setInitObjectState: function(){	// 画面初期化時の項目制御
			var that = this;
			// 当帳票を「参照」で開いた場合
			var sendBtnid = $.getJSONValue(that.jsonHidden, "sendBtnid");
			if(!sendBtnid){
				sendBtnid = $('#sendBtnid').val();
			}
			$('#sendBtnid').val(sendBtnid);
			var reportYobi1 = $.getJSONValue(that.jsonHidden, "reportYobi1");
			if(!reportYobi1){
				reportYobi1 = $('#reportYobi1').val();
			}
			$('#reportYobi1').val(reportYobi1);

			$($.id.buttons).show();

			$($.id.buttons).show();
			// 各種ボタン
			$.initReportInfo("TJ011", "特売販売計画＆事前発注");
			$('#'+$.id.btn_addline).bind('click', function(){
				that.addLine();
		    });

			//$('#'+$.id.btn_saikeisan).on("click", that.setKeisan);
			$('#'+$.id.btn_saikeisan).on("click", that.setKeisan2);
			$.initReportInfo("TJ005", "特売販売計画＆事前発注");
			$('#'+$.id.btn_cancel).on("click", $.pushChangeReport);

			// 行追加ボタンはデフォルトで非表示
			$('#'+$.id.btn_addline).attr('tabindex', -1).hide();

			// クリアボタン押下時
			$('#'+$.id.btn_clear).on("click", function(e){

				// 編集中の行がある場合はEndEditを行う
				var row = $($.id.gridholder).datagrid("getSelected");
				var rowIndex = $($.id.gridholder).datagrid("getRowIndex", row);
				$($.id.gridholder).datagrid('endEdit',rowIndex);
			});
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
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform).form('validate');
			// 入力エラーなしの場合に検索条件を格納
			if (rt == true)
			that.jsonString = that.jsonTemp.slice(0);
			// 入力チェック用の配列をクリア
			that.jsonTemp = [];
			return true;
		},
		success: function(reportno, sortable){	// （必須）正処理
			if (sortable) sortable=1; else sortable=0;
			var that = this;
			// 検索実行
			var szBmncd	= $.getJSONValue(this.jsonHidden, $.id.SelBumon);			// 部門コード
			var szLstno	= $.getJSONValue(this.jsonHidden, $.id_inp.txt_lstno);		// リスト№

			// 処理時間計測用
			that.timeData = (new Date()).getTime();

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,		// レポート名
					t:				(new Date()).getTime(),
					sortable:		sortable,
					BMNCD:			szBmncd,
					LSTNO:			szLstno,
					sortName:		that.sortName,
					sortOrder:		that.sortOrder,
					rows:			75
				},
				function(json){
					// 検索処理エラー判定
					if($.searchError(json)) return false;
					// ログ出力
					$.log(that.timeData, 'query:');

					// 検索データ（想定）
					that.gridData = JSON.parse(json).rows;
					that.gridTitle = JSON.parse(json).titles;
					that.totalRows = JSON.parse(json).total;

					var opts = JSON.parse(json).opts
					if(opts && opts.rows_){
						// 基本データ表示
						that.setData(opts.rows_, opts);
						that.setData(opts.rows_2, opts);
						that.setData2(opts.rows_2, opts);
						that.setData3(opts.rows_3, opts);
						that.grd_all_data = opts.allRows;
						that.addShinNum = opts.countAddShn
					}
					// メインデータ表示
					that.queried = true;

					var titles = JSON.parse(json).titles;
					/** Colomns設定(不要の場合は除去) ※DataGrid用 */
					// 列表示切替
					if(titles != undefined && titles.length > 0){
						$($.id.gridholder).datagrid('options').columns[0][3].title = titles[0];
						// datagrid のタイトル再設定
						$($.id.gridholder).datagrid({ columns:$($.id.gridholder).datagrid('options').columns });
					}

					// 更新データのクリア
					if(that.updateData.length > 0){
						that.updateData = [];
					}
					if(that.grd_shn_data.length > 0){
						that.grd_shn_data = [];
					}

					// Load開始
					that.endGridLoad = false;

					// 変更情報をクリア
					$($.id.hiddenChangedIdx).val("");

					that.queried = true;
					// Load処理回避
					$.tryChangeURL($.reg.jqeasy);

					// グリッド再描画（easyui 1.4.2 対応）
					$($.id.gridholder).datagrid('load', {} );
					$.removeMask();

					// 状態保存
					$.saveState2(reportno, that.getJSONString());

					// ログ出力
					$.log(that.timeData, 'loaded:');
				}
			);
		},
		updValidation: function (){	// （必須）批准
			var that = this;

			// 編集中の行がある場合はEndEditを行う
			var row = $($.id.gridholder).datagrid("getSelected");
			var rowIndex = $($.id.gridholder).datagrid("getRowIndex", row);
			$($.id.gridholder).datagrid('endEdit',rowIndex);

			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform).form('validate');
			if(!rt){
				$.addErrState(that, $('.validatebox-invalid').eq(0), false);
				return rt;
			}
			// グリッド 内の 'validate' 実施
			var rt = $($.id.gridform).form('validate');
			if(!rt){
				$.addErrState(that, $('.validatebox-invalid').eq(0), false);
				return rt;
			}

			var targetDatasShn 		= that.getMergeGridDate($.id.gridholder);		// 登録データ：事前発注_店舗
			var targetDatasShn2 	= that.getMergeGridDate3($.id.gridholder);		// 登録データ：事前発注_追加商品


			// 入力チェック：商品コード
			that.setUpdateData('gridholder', "isUpdate")	// 変更情報の取得
			if(that.updateData){
				for(var i = 0; i < that.updateData.length; i ++){
					if(that.updateData[i].type == 'addShnData'){
						var data = that.updateData[i].rows;
						var errPage =[that.updateData[i].page + "ﾍﾟｰｼﾞ："]

						var shncd	 = data.F1			// 商品コード
						var binKbn	 = data.F2			// 便区分

						// 部門コードとの相互チェック
						var bmncd		 = $.getJSONValue(this.jsonHidden, $.id.SelBumon).split("-")[0];		// 部門コード
						if(shncd.substring(0, 2) != bmncd){
							$.showMessage("E11162", errPage);
							return false;
						}

						// 存在チェック
						var msgid = that.checkInputboxFunc($.id_inp.txt_shncd, shncd , '');
						if(msgid !==null){
							$.showMessage(msgid, errPage);
							return false;
						}

						if(!binKbn || binKbn == ""){
							// 便区分コード未入力の場合
							$.showMessage("EX1047",["便区分"]);
							return false;

						}else{
							if(binKbn != '1' && binKbn != '2'){
								$.showMessage("E35002");
								return false;
							}
						}
					}
				}
			}

			// 入力チェック用の配列をクリア
			that.jsonTemp = [];

			return rt;
		},
		updSuccess: function(id){	// validation OK時 の update処理
			var that = this;

			var szBmncd	= $.getJSONValue(this.jsonHidden, $.id.SelBumon);				// 部門コード

			// 変更行情報取得
			var changedIndex = $($.id.hiddenChangedIdx).val().split(",");

			var targetDatas = [{}];
			$('#'+that.focusRootId).find('[col^=F]').each(function(){
				var col = $(this).attr('col');
				targetDatas[0][col] = $.getInputboxValue($(this));
			});

			// 明細データ(既存商品の変更情報)の取得
			var targetDatasShn = [];
			that.setUpdateData('gridholder' ,"isUpdate")	// 変更情報の取得
			if(that.updateData){
				for(var i = 0; i < that.updateData.length; i ++){
					if(that.updateData[i].type == 'shnData'){
						targetDatasShn.push(that.updateData[i].rows)
					}
				}
			}

			// 明細データ(追加商品の新規登録情報)の取得
			var targetDatasShn2 = [];
			if(that.updateData){
				for(var i = 0; i < that.updateData.length; i ++){
					if(that.updateData[i].type == 'addShnData'){
						targetDatasShn2.push(that.updateData[i].rows)
					}
				}
			}

			var szLstno		= $.getJSONValue(this.jsonHidden, $.id_inp.txt_lstno);		// リスト№

			// 部門予算データの取得
			var targetDataYsn= [];

			// 処理時間計測用
			that.timeData = (new Date()).getTime();

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,							// レポート名
					action:			$.id.action_update,					// 実行処理情報
					obj:			id,									// 実行オブジェクト
					SENDBTNID:		that.sendBtnid,
					DATA:			JSON.stringify(targetDatas),		// 更新対象情報
					DATA_YSN:		JSON.stringify(targetDataYsn),		// 事前発注_部門予算
					DATA_SHN:		JSON.stringify(targetDatasShn),		// 事前発注_店舗
					DATA_SHN2:		JSON.stringify(targetDatasShn2),	// 事前発注_追加商品
					DATA_KOUSEIHI:	JSON.stringify([]),					// 事前発注_構成比
					LSTNO:			szLstno,
					BMNCD:			szBmncd.split("-")[0],
					rows:			75,									// 表示可能レコード数(時間外利用時に登録時、行情報を保持しなければページング処理に影響が出る為、行情報を送る必要がある)
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

					// マスク削除
					$.removeMaskMsg();

					// ログ出力
					$.log(that.timeData, 'loaded:');
				}
			);
		},
		updWorkTable: function(id, init){	// validation OK時 の update処理
			var that = this;

			var szBmncd	= $.getJSONValue(this.jsonHidden, $.id.SelBumon);				// 部門コード
			var szLstno	= $.getJSONValue(this.jsonHidden, $.id_inp.txt_lstno);			// リスト№

			// 変更行情報取得
			var changedIndex = $($.id.hiddenChangedIdx).val().split(",");

			// 明細データ(事前発注_発注明細wk)
			var data = {};
			var rowDate = {};
			var targetRows= [];

			var rows	 = $("#"+id).datagrid('getRows');			// 商品一覧
			var pageNum  = 0										// ページ番号
			var teiseiKbn = 1;										// 追加訂正区分
			var btnid = '';

			if(init){
				pageNum  = $('#'+id).datagrid('options').pageNumber	// ページ番号
				btnid = 'Init';
				rows = that.grd_all_data;
			}else{
				pageNum  = that.thisPageNumber
			}

			var shncd = "";

			for (var i=0; i<rows.length; i++){
				var row = rows[i];

				if(init){
					pageNum = row['F49'];
				}

				if(row['F1'] == '1'){
					rowDate = {}	// 初期化

					rowDate['F1']	 = row['F19']	// F1	リストNo
					rowDate['F2']	 = row['F34']	// F2	店コード
					rowDate['F3']	 = row['F20']	// F3	部門
					rowDate['F4']	 = row['F21']	// F4	表示順番
					rowDate['F5']	 = teiseiKbn	// F5	追加訂正区分
					rowDate['F12']	 = row['F18']	// F12	重量計

				}else if(row['F1'] == '2'){
					rowDate['F6']	 = row['F2']	// F6	商品コード
					rowDate['F7']	 = ''			// F7	大分類

					shncd = "";
					shncd = row['F2']

				}else if(row['F1'] == '3'){
					rowDate['F9']	 = row['F4']	// F9	入数_特売
					rowDate['F10']	 = row['F5']	// F10	事前原価
					rowDate['F13']	 = row['F39']	// F13	商品区分_01
					rowDate['F14']	 = row['F40']	// F14	商品区分_02
					rowDate['F15']	 = row['F41']	// F15	商品区分_03
					rowDate['F16']	 = row['F42']	// F16	商品区分_04
					rowDate['F17']	 = row['F43']	// F17	商品区分_05
					rowDate['F18']	 = row['F44']	// F18	商品区分_06
					rowDate['F19']	 = row['F45']	// F19	商品区分_07
					rowDate['F20']	 = row['F46']	// F20	商品区分_08
					rowDate['F21']	 = row['F47']	// F21	商品区分_09
					rowDate['F22']	 = row['F48']	// F22	商品区分_10
					rowDate['F23']	 = row["F23"]	// F23	訂正区分_01
					rowDate['F24']	 = row["F24"]	// F24	訂正区分_02
					rowDate['F25']	 = row["F25"]	// F25	訂正区分_03
					rowDate['F26']	 = row["F26"]	// F26	訂正区分_04
					rowDate['F27']	 = row["F27"]	// F27	訂正区分_05
					rowDate['F28']	 = row["F28"]	// F28	訂正区分_06
					rowDate['F29']	 = row["F29"]	// F29	訂正区分_07
					rowDate['F30']	 = row["F30"]	// F30	訂正区分_08
					rowDate['F31']	 = row["F31"]	// F31	訂正区分_09
					rowDate['F32']	 = row["F32"]	// F32	訂正区分_10
					rowDate['F33']	 = row['F8']	// F33	ケース数1
					rowDate['F34']	 = row['F9']	// F34	ケース数2
					rowDate['F35']	 = row['F10']	// F35	ケース数3
					rowDate['F36']	 = row['F11']	// F36	ケース数4
					rowDate['F37']	 = row['F12']	// F37	ケース数5
					rowDate['F38']	 = row['F13']	// F38	ケース数6
					rowDate['F39']	 = row['F14']	// F39	ケース数7
					rowDate['F40']	 = row['F15']	// F40	ケース数8
					rowDate['F41']	 = row['F16']	// F41	ケース数9
					rowDate['F42']	 = row['F17']	// F42	ケース数10

				}else if(row['F1'] == '4'){
					rowDate['F8']	 = row['F4']	// F8	便区分
					rowDate['F11']	 = row['F6']	// F11	特売本体売価
					rowDate['F43']	 = pageNum		// F43	ページ番号
					rowDate['F44']	 = row['F38']	// F44	納品形態

					if(shncd && shncd !== ""){
						targetRows.push(rowDate);	// リストに追加
					}
				}
			}

			if(targetRows.length == 0){
				return false;
			}

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			var isTimeout = $.checkIsTimeout();
			if (! isTimeout) {
				$.post(
						$.reg.jqgrid ,
						{
							report:			that.name,					// レポート名
							action:			$.id.action_update,			// 実行処理情報
							obj:			id,							// 実行オブジェクト
							SENDBTNID:		'workTable'+btnid,
							DATA:			JSON.stringify(targetRows),	// 更新対象情報(事前発注_発注明細wk)
							LSTNO:			szLstno,
							BMNCD:			szBmncd.split("-")[0],
							rows:			75,							// 表示可能レコード数(遷移毎にサーブレット経由で行情報が上書きされてしまう為、常に行情報を送る必要がある)
							t:				(new Date()).getTime()
						},
						function(data){

						}
				);
			}
		},
		updWorkTableDel: function(id){	// 戻るボタン押下時の際にワークテーブルの値を削除する。
			var that = this;

			var szBmncd	= $.getJSONValue(this.jsonHidden, $.id.SelBumon);				// 部門コード
			var szLstno	= $.getJSONValue(this.jsonHidden, $.id_inp.txt_lstno);			// リスト№

			that.timeData = (new Date()).getTime();
			var isTimeout = $.checkIsTimeout();
			if (! isTimeout) {
				$.post(
						$.reg.jqgrid ,
						{
							report:			that.name,					// レポート名
							action:			$.id.action_update,			// 実行処理情報
							obj:			id,							// 実行オブジェクト
							SENDBTNID:		'workTableDel',
							DATA:			JSON.stringify([{row:""}]),	// 更新対象情報(事前発注_発注明細wk)
							LSTNO:			szLstno,
							BMNCD:			szBmncd.split("-")[0],
							t:				(new Date()).getTime()
						},
						function(data){

						}
				);
			}
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
			// 催しコード
			this.jsonTemp.push({
				id:		$.id_inp.txt_moyscd,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_moyscd),
				text:	''
			});
			// 管理番号
			this.jsonTemp.push({
				id:		$.id_inp.txt_kanrino,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_kanrino),
				text:	''
			});
			// リスト番号
			this.jsonTemp.push({
				id:		$.id_inp.txt_lstno,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_lstno),
				text:	''
			});
			// 部門
			this.jsonTemp.push({
				id:		$.id.SelBumon,
				value:	$.getJSONValue(this.jsonHidden, $.id.SelBumon),
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

				// 登録ボタンの非活性制御
				if (rows[0]['F10']==='1') {
					$.setInputBoxDisable($("#"+$.id.btn_upd));
				}
			}
		},
		setData2: function(rows, opts){		// データ表示
			var that = this;
			if(rows.length > 0){
				for(var i = 0;i<10; i++){
					var j = i+1;
					if(rows.length>=j){
						$.setInputboxValue($('#txt_yosan'+j), rows[i]["V1"]);
						var rowDate = {
								F1	 :rows[i]["V1"],
						}
					}else{
						var rowDate = {
								F1	 :"",
						}
					}
					that.grd_ysn_data.push(rowDate);
				}
			}
		},
		setData3: function(rows, opts){		// データ表示
			var that = this;
			if(rows.length > 0){

				var options = $($.id.gridholder).datagrid('options');
				var columns = options.columns;

				for(var i = 0;i<10; i++){
					var j = i+1;
						$.setInputboxValue($('#txt_day'+j), rows[0]["V"+j]);
						$.setInputboxValue($('#txt_youbi'+j), rows[0]["W"+j]);

						columns[0][i+7].title = rows[0]["V"+j];
						columns[2][i+7].title = rows[0]["W"+j];

					var rowDate = {
							F1	 :rows[0]["X"+j],
					}
					that.grd_day_data.push(rowDate);
				}
				// datagrid のタイトル再設定
				$($.id.gridholder).datagrid({ columns:columns });
			}
		},
		// 本画面では5行単位で1商品データを表示して居る為、登録などの際に扱いやすいように一行にまとめる。
		roundUpRowsDate: function(rows){
			var that = this;

			var RoundRows = []
			var rowBf = {}

			if(rows.length > 0){
				// 5行単位でデータの為、
				var hyoseqno = []		// 表示番号
				for (var i=0; i<rows.length; i++){
					var row = rows[i];
					if(hyoseqno.indexOf(row["F21"]) == -1 || (rows.length == i + 1)){
						hyoseqno.push(row["F21"])		// 表示番号を保持
						if(Object.keys(rowBf).length > 0){
							// 成形したデータを格納
							RoundRows.push(rowBf)
						}
						rowBf = {}						// 行情報を初期化
					}

					if(row['F1'] == '1'){
						rowBf['F1'] = row['F19']	// F1	リストNo
						rowBf['F2'] = row['F20']	// F2	部門
						rowBf['F3'] = row['F21']	// F3	表示順番
						rowBf['F4'] = row['F34']	// F4	店コード

					}else if(row['F1'] == '2'){
						rowBf['F15'] = row['F2']	// F15	商品コード
						rowBf['F16'] = row['F22']	// F16	追加商品フラグ

					}else if(row['F1'] == '3'){
						rowBf['F5']	 = row['F8']	// F5	ケース数1
						rowBf['F6']	 = row['F9']	// F6	ケース数2
						rowBf['F7'] = row['F10']	// F7	ケース数3
						rowBf['F8'] = row['F11']	// F8	ケース数4
						rowBf['F9'] = row['F12']	// F9	ケース数5
						rowBf['F10'] = row['F13']	// F10	ケース数6
						rowBf['F11'] = row['F14']	// F11	ケース数7
						rowBf['F12'] = row['F15']	// F12	ケース数8
						rowBf['F13'] = row['F16']	// F13	ケース数9
						rowBf['F14'] = row['F17']	// F14	ケース数10

					}else if(row['F1'] == '4'){
						rowBf['F17'] = row['F4']	// F17	便区分
						rowBf['F18'] = row['F49']	// F18	ページ番号
					}
				}
			}
			return RoundRows
		},
		getMergeGridDate: function( target){
			// 保持したデータと入力データ比較を比較する。
			var that = this;

			var allRows = $($.id.gridholder).datagrid('getData').rows;

			var newrows = that.roundUpRowsDate(allRows);					// 変更データ
			var oldrows = [];
			var targetRows= [];

			if(target===undefined || target===$.id.gridholder){
				//var oldpk = [];
				oldrows = that.grd_shn_data;
				for (var i=0; i<newrows.length; i++){
					if(newrows[i]['F16'] == "0"){
						if(newrows[i]['F5'] != oldrows[i]['F5']
						|| newrows[i]['F6'] != oldrows[i]['F6']
						|| newrows[i]['F7'] != oldrows[i]['F7']
						|| newrows[i]['F8'] != oldrows[i]['F8']
						|| newrows[i]['F9'] != oldrows[i]['F9']
						|| newrows[i]['F10'] != oldrows[i]['F10']
						|| newrows[i]['F11'] != oldrows[i]['F11']
						|| newrows[i]['F12'] != oldrows[i]['F12']
						|| newrows[i]['F13'] != oldrows[i]['F13']
						|| newrows[i]['F14'] != oldrows[i]['F14']){
							var rowDate = {
									F1	 : newrows[i]["F1"],	// リストNo
									F2	 : newrows[i]["F2"],	// 部門
									F3	 : newrows[i]["F3"],	// 表示順
									F4	 : newrows[i]["F4"],	// 店コード
									F5	 : newrows[i]["F5"],	// ケース数1
									F6	 : newrows[i]["F6"],	// ケース数2
									F7	 : newrows[i]["F7"],	// ケース数3
									F8	 : newrows[i]["F8"],	// ケース数4
									F9	 : newrows[i]["F9"],	// ケース数5
									F10	 : newrows[i]["F10"],	// ケース数6
									F11	 : newrows[i]["F11"],	// ケース数7
									F12	 : newrows[i]["F12"],	// ケース数8
									F13	 : newrows[i]["F13"],	// ケース数9
									F14	 : newrows[i]["F14"],	// ケース数10
							};
							if(rowDate){
								targetRows.push(rowDate);
							}
						}
					}
				}
			}
			return targetRows;
		},
		getMergeGridDate2: function(){
			// 保持したデータと入力データ比較を比較する。
			var that = this;
			var newrows = [];		// 変更データ

			for(var i = 0;i<10; i++){
				var j = i+1;
				var ysnDate = {
						F1	 : $.getInputboxValue($('#txt_yosan'+j)),
						F2	 : $.getInputboxValue($('#txt_day'+j)),
						F3	 : $.getInputboxValue($('#txt_youbi'+j))
				}
				newrows.push(ysnDate);
			}

			var oldrows = [];
			var oldrows2 = [];
			var targetRows= [];

			//var oldpk = [];
			oldrows = that.grd_ysn_data;
			oldrows2 = that.grd_day_data;
			for (var i=0; i<newrows.length; i++){
				if((newrows[i]['F1'] ? newrows[i]['F1'] : "") != (oldrows[i]['F1'] ? oldrows[i]['F1'] : "")){
					var rowDate = {
							F1	 : newrows[i]["F1"],
							F2	 : oldrows2[i]["F1"],
					};
					if(rowDate){
						targetRows.push(rowDate);
					}
				}
			}
			return targetRows;
		},
		getMergeGridDate3: function( target){
			// 保持したデータと入力データ比較を比較する。
			var that = this;

			var allRows = $($.id.gridholder).datagrid('getData').rows;

			var newrows = that.roundUpRowsDate(allRows);						// 変更データ
			var dayrows = that.grd_day_data;
			var oldrows = [];
			var targetRows= [];

			if(target===undefined || target===$.id.gridholder){
				//var oldpk = [];
				oldrows = that.grd_shn_data;
				for (var i=0; i<newrows.length; i++){
					if(newrows[i]['F16'] == "2"){
						// 新規追加商品
						if(newrows[i]["F15"] && newrows[i]["F15"] != ""){
							var param = {};
							param["KEY"] =  "MST_CNT";
							param["value1"] = newrows[i]["F15"];// 商品コード
							param["value2"] = "5";
							var txt_shncd_chk = $.getInputboxData(that.name, $.id.action_check,  "ADDSHNCD", [param]);
							var rowDate = {
									F1	 : newrows[i]["F15"],//商品コード
									F2	 : newrows[i]["F17"],//便区分
									F3	 : newrows[i]["F3"],//表示順番
									F4	 : newrows[i]["F5"],//発注数_01
									F5	 : newrows[i]["F6"],//発注数_02
									F6	 : newrows[i]["F7"],//発注数_03
									F7	 : newrows[i]["F8"],//発注数_04
									F8	 : newrows[i]["F9"],//発注数_05
									F9	 : newrows[i]["F10"],//発注数_06
									F10	 : newrows[i]["F11"],//発注数_07
									F11	 : newrows[i]["F12"],//発注数_08
									F12	 : newrows[i]["F13"],//発注数_09
									F13	 : newrows[i]["F14"],//発注数_10
									F14	 : dayrows[0]["F1"],//日付_01
									F15	 : dayrows[1]["F1"],//日付_02
									F16	 : dayrows[2]["F1"],//日付_03
									F17	 : dayrows[3]["F1"],//日付_04
									F18	 : dayrows[4]["F1"],//日付_05
									F19	 : dayrows[5]["F1"],//日付_06
									F20	 : dayrows[6]["F1"],//日付_07
									F21	 : dayrows[7]["F1"],//日付_08
									F22	 : dayrows[8]["F1"],//日付_09
									F23	 : dayrows[9]["F1"],//日付_10
									F24	 : newrows[i]["F18"],// ページ番号
									F25	 : newrows[i]["F16"],//追加商品フラグ
							};
							if(rowDate){
								targetRows.push(rowDate);
							}
						}
					}else if(newrows[i]['F16'] == "1"){
						// 既存追加商品
						if(newrows[i]['F5'] != oldrows[i]['F5']
						|| newrows[i]['F6'] != oldrows[i]['F6']
						|| newrows[i]['F7'] != oldrows[i]['F7']
						|| newrows[i]['F8'] != oldrows[i]['F8']
						|| newrows[i]['F9'] != oldrows[i]['F9']
						|| newrows[i]['F10'] != oldrows[i]['F10']
						|| newrows[i]['F11'] != oldrows[i]['F11']
						|| newrows[i]['F12'] != oldrows[i]['F12']
						|| newrows[i]['F13'] != oldrows[i]['F13']
						|| newrows[i]['F14'] != oldrows[i]['F14']){
						var rowDate = {
								F1	 : newrows[i]["F15"],//商品コード
								F2	 : newrows[i]["F17"],//便区分
								F3	 : newrows[i]["F3"],//表示順番
								F4	 : newrows[i]["F5"],//発注数_01
								F5	 : newrows[i]["F6"],//発注数_02
								F6	 : newrows[i]["F7"],//発注数_03
								F7	 : newrows[i]["F8"],//発注数_04
								F8	 : newrows[i]["F9"],//発注数_05
								F9	 : newrows[i]["F10"],//発注数_06
								F10	 : newrows[i]["F11"],//発注数_07
								F11	 : newrows[i]["F12"],//発注数_08
								F12	 : newrows[i]["F13"],//発注数_09
								F13	 : newrows[i]["F14"],//発注数_10
								F14	 : dayrows[0]["F1"],//日付_01
								F15	 : dayrows[1]["F1"],//日付_02
								F16	 : dayrows[2]["F1"],//日付_03
								F17	 : dayrows[3]["F1"],//日付_04
								F18	 : dayrows[4]["F1"],//日付_05
								F19	 : dayrows[5]["F1"],//日付_06
								F20	 : dayrows[6]["F1"],//日付_07
								F21	 : dayrows[7]["F1"],//日付_08
								F22	 : dayrows[8]["F1"],//日付_09
								F23	 : dayrows[9]["F1"],//日付_10
								F24	 : newrows[i]["F18"],// ページ番号
								F25	 : newrows[i]["F16"],//追加商品フラグ
						};
						if(rowDate){
							targetRows.push(rowDate);
						}
						}
					}
				}
			}
			return targetRows;
		},
		// ページング処理時に、現在のページで変更されたデータを保持する
		setUpdateData:function(id, btId){
			var that = this;
			var updateRows		 = that.getMergeGridDate($.id.gridholder);	// グリッドの更新データ(通常商品)の取得
			var updateRows_add	 = that.getMergeGridDate3($.id.gridholder);	// グリッドの更新データ(追加商品)の取得
			var options			 = $('#'+id).datagrid('options');
			var page			 = options.pageNumber	// 現在のページ番号
			var pushButtonId = btId ? btId : "isChange"

			// 通常商品の場合
			if(updateRows && updateRows.length > 0){
				for (var i=0; i< updateRows.length; i++){
					var rows = updateRows[i]
					var isExist	 = false;
					var updIndex	= -1
					var dispNo	 = rows['F3']	// 表示順

					// 変更データ
					var data = {
							page:that.thisPageNumber,
							type:'shnData',
							rows:rows
					}

					// 既に変更されたデータか確認を行う
					var newLines = that.updateData.filter(function(item, index){
						if(item.type == 'shnData'){
							if(item.rows.F3 == dispNo){
								// 変更データと同じ主キーを持つデータが存在する場合
								isExist = true
								updIndex = index
							}
						}
					});

					if(isExist){
						// 既に変更されたデータの為、既存の変更データを上書きする
						if(updIndex != -1){
							that.updateData[updIndex] = data
						}
					}else{
						// 新規変更データの為追加する
						that.updateData.push(data)
					}
				}
			}

			// 追加商品の場合
			if(updateRows_add && updateRows_add.length > 0){
				for (var i=0; i< updateRows_add.length; i++){
					var rows		= updateRows_add[i]
					var isExist		= false;
					var updIndex	= -1
					var dispNo		= rows['F3']	// 表示順
					var addShnFlg	= rows['F25']	// 追加商品フラグ

					// 変更データ
					var data = {
							page:that.thisPageNumber,
							type:'addShnData',
							rows:rows
					}

					// 既に変更されたデータか確認を行う
					var newLines = that.updateData.filter(function(item, index){
						if(dispNo != ""){
							if(item.type == 'addShnData'){
								if(item.rows.F3 == dispNo){
									// 変更データと同じ主キーを持つデータが存在する場合
									isExist = true
									updIndex = index
								}
							}
						}
					});

					if(isExist){
						// 既に変更されたデータの為、既存の変更データを上書きする
						if(updIndex != -1){
							that.updateData[updIndex] = data
						}
					}else{
						// 新規変更データの為追加する
						that.updateData.push(data)
					}
				}

				//移動時は新規追加商品の情報は保持しない。(ページングでは追加した行は保持されない)
				if(pushButtonId == 'isChange'){
					var updateDataAf = []
					for (var i=0; i< that.updateData.length; i++){
						var data = that.updateData[i]
						var type = data.type
						var row	 = data.rows

						if(!(type == 'addShnData' && row.F25 == '2')){
							// 新規追加商品以外のデータを保持
							updateDataAf.push(data)
						}
					}
					that.updateData = updateDataAf
				}
			}
		},
		// ページング処理時に、前回編集済みのデータがある場合、Gridに情報をセットする
		setGridRows:function(){
			var that = this
			var thisRows = $($.id.gridholder).datagrid('getRows');
			var isExist = false
			var editLine = ['3']

			if(that.updateData && that.updateData.length > 0){
				for (var i=0; i< that.updateData.length; i++){
					var data = that.updateData[i]
					if(that.thisPageNumber == data.page){
						// 現在表示しているページと同じページの変更データが存在した場合

						if(data.type =='shnData'){
							// 通常商品の場合
							var dispNo = data.rows.F3
							var newLines = thisRows.filter(function(item, index){
								if(item.F21 == dispNo){
									// 変更データと同じ主キーを持つデータが存在する場合
									if(editLine.indexOf(item.F1) != -1){

										// 変更対象行のデータを上書きする
										thisRows[index].F8	= data.rows.F5		// 数量1
										thisRows[index].F9	= data.rows.F6		// 数量2
										thisRows[index].F10 = data.rows.F7		// 数量3
										thisRows[index].F11 = data.rows.F8		// 数量4
										thisRows[index].F12 = data.rows.F9		// 数量5
										thisRows[index].F13 = data.rows.F10		// 数量6
										thisRows[index].F14 = data.rows.F11		// 数量7
										thisRows[index].F15 = data.rows.F12		// 数量8
										thisRows[index].F16 = data.rows.F13		// 数量9
										thisRows[index].F17 = data.rows.F14		// 数量10
										thisRows[index].CHANGE = "1"			// 値変更フラグ

										$($.id.gridholder).datagrid('refreshRow', index);

										if(!isExist){
											isExist = true
										}
									}
								}
							});
						}

						if(data.type =='addShnData'){
							// 追加商品の場合
							var dispNo = data.rows.F3
							var newLines = thisRows.filter(function(item, index){
								if(item.F21 == dispNo){
									// 変更データと同じ主キーを持つデータが存在する場合
									if(editLine.indexOf(item.F1) != -1){

										// 変更対象行のデータを上書きする
										thisRows[index].F8	= data.rows.F4		// 数量1
										thisRows[index].F9	= data.rows.F5		// 数量2
										thisRows[index].F10 = data.rows.F6		// 数量3
										thisRows[index].F11 = data.rows.F7		// 数量4
										thisRows[index].F12 = data.rows.F8		// 数量5
										thisRows[index].F13 = data.rows.F9		// 数量6
										thisRows[index].F14 = data.rows.F10		// 数量7
										thisRows[index].F15 = data.rows.F11		// 数量8
										thisRows[index].F16 = data.rows.F12		// 数量9
										thisRows[index].F17 = data.rows.F13		// 数量10
										thisRows[index].CHANGE = "1"			// 値変更フラグ

										$($.id.gridholder).datagrid('refreshRow', index);

										if(!isExist){
											isExist = true
										}
									}
								}
							});
						}
					}
				}
			}
		},
		extenxDatagridEditorIds:{
			F2		: "txt_shncd",
			F4		: "txt_binkbn",
			F8		: "txt_htsu1",
			F9		: "txt_htsu2",
			F10		: "txt_htsu3",
			F11		: "txt_htsu4",
			F12		: "txt_htsu5",
			F13		: "txt_htsu6",
			F14		: "txt_htsu7",
			F15		: "txt_htsu8",
			F16		: "txt_htsu9",
			F17		: "txt_htsu10",
		},
		setEditableGrid2: function (id, reportNumber){	// グリッドの構築
			var that = this;
			var init = true;
			var initPagination = false;
			var pageList = [75];
			var pageSize = 100;
			if (pageSize==="") pageSize=$.fn.pagination.defaults.pageSize;
			pageSize = $.getDefaultPageSize(pageSize, pageList);

			var fcolumns = [];
			var columns = [];
			var fcolumnBottom=[];
			var columnBottom=[];

			var bcstyler =function(value,row,index){return 'background-color:#f5f5f5;';};
			var dformatter =function(value){ return $.getFormatDt(value, true);};
			var cformatter =function(value,row,index){return value && value===$.id.value_on?$.id.text_on:$.id.text_off;};
			var pomptFormatter =function(value){return $.getFormatPrompt(value, '####-####');};
			var iformatter	 = function(value,row,index){ return $.getFormat(value, '#,##0');};
			var iformatter_s = function(value,row,index){ return $.getFormat(value, '#,##0,00');};
			var pcrmatter = function(value,row,index){
				if(value && value !==""){
					if(value == '0' || value == 0){
						return null;
					}else{
						return $.getFormat(value, '#,##0.00')+'%';
					}
				}
			};
			var formatterF2 = function(value,row,index){
				if(row.F1 === '2'){
					return $.getFormatPrompt(value, '####-####');
				}else{
					return value
				}
			}
			var formatterCasesu = function(value,row,index){

				if(row.F1 === '1' || row.F1 === '2'){
					var keys = this.field	// 項目のKeyを取得
					if(keys && keys != ""){
						if(8 <= Number(keys.slice(1)) && Number(keys.slice(1)) <= 17){
							// 文字部分のみ中央寄せを行う

							return '<div style = "width:100%;text-align:center;">' + (value==undefined ? "":value)  + '</div>'
						}
					}
					return value
				}else if(row.F1 === '3'){
					var keys = this.field	// 項目のKeyを取得

					if(keys && keys != ""){
						if(8 <= Number(keys.slice(1)) && Number(keys.slice(1)) <= 17){
							// 本項目がケース数の場合
							var keysTsiKbn = "F" + (Number(keys.slice(1)) + 15)	// ケース数に対応する訂正区分のKeyを取得
							if((!row[keysTsiKbn] || row[keysTsiKbn] == "") && row.F22 == "0"){
								// 訂正区分に設定がない場合、ケース数を非表示にする
								return "";
							}
						}
					}
					return $.getFormat(value, '#,##0');
				}else if(row.F1 === '4'){
					return $.getFormat(value, '#,##0');
				}else if(row.F1 === '5'){
					return $.getFormat(value, '#,##0');
				}else{
					return value
				}
			}

			var formatterCasesu_F4 = function(value,row,index){
				if(row.F1 === '3'){
					return $.getFormat(value, '#,##0');
				}else if(row.F1 === '4'){
					if(value && value != ""){
						return value + "便";
					}
				}else if(row.F1 === '5'){
					return $.getFormat(value, '#,##0');
				}else{
					return value
				}
			}

			var formatterCasesu_F6 = function(value,row,index){
				if(row.F1 === '1'){
					return value;
				}else{
					return $.getFormat(value, '#,##0');
				}
			}

			var cstyler1 =function(value,row,index){
				if(row.F1 === '1'){
					return 'background-color:#BBBBBB;';
				}
			};
			var cstyler2 =function(value,row,index){
				if(row.F1 === '1'){
					return 'background-color:#BBBBBB;';
				}else if(row.F1 === '2'){
					return 'background-color:#eaf2ff;border-bottom:0px;';
				}else if(row.F1 === '3'){
					return 'background-color:#eaf2ff;';
				}else if(row.F1 === '4'){
					return 'background-color:#eaf2ff;';
				}else if(row.F1 === '5'){
					return 'background-color:#eaf2ff;';
				};
			};

			columnBottom.push({field:'F1',	title:'',			width:  0,halign:'center',align:'right', hidden:true});
			columnBottom.push({field:'F2',	title:'アイテム',	width:  80,halign:'center',align:'left',styler:cstyler2, formatter:formatterF2,editor:{type:'numberbox'}});
			columnBottom.push({field:'F3',	title:'',			width:  50,halign:'center',align:'right',styler:cstyler2});
			columnBottom.push({field:'F4',	title:'',			width:  50,halign:'center',align:'right',styler:cstyler2, formatter:formatterCasesu_F4});
			columnBottom.push({field:'F5',	title:'',			width:  70,halign:'center',align:'right',styler:cstyler2, formatter:iformatter});
			columnBottom.push({field:'F6',	title:'納品形態',	width:  70,halign:'center',align:'right',styler:cstyler2, formatter:formatterCasesu_F6});
			columnBottom.push({field:'F7',	title:'日付',		width:  60,halign:'center',align:'right',rowspan:2,styler:cstyler2});
			columnBottom.push({field:'F8',	title:'',			width:  55,halign:'center',align:'right',rowspan:2,styler:cstyler1, formatter:formatterCasesu,editor:{type:'numberbox'}});
			columnBottom.push({field:'F9',	title:'',			width:  55,halign:'center',align:'right',rowspan:2,styler:cstyler1, formatter:formatterCasesu,editor:{type:'numberbox'}});
			columnBottom.push({field:'F10',	title:'',			width:  55,halign:'center',align:'right',rowspan:2,styler:cstyler1, formatter:formatterCasesu,editor:{type:'numberbox'}});
			columnBottom.push({field:'F11',	title:'',			width:  55,halign:'center',align:'right',rowspan:2,styler:cstyler1, formatter:formatterCasesu,editor:{type:'numberbox'}});
			columnBottom.push({field:'F12',	title:'',			width:  55,halign:'center',align:'right',rowspan:2,styler:cstyler1, formatter:formatterCasesu,editor:{type:'numberbox'}});
			columnBottom.push({field:'F13',	title:'',			width:  55,halign:'center',align:'right',rowspan:2,styler:cstyler1, formatter:formatterCasesu,editor:{type:'numberbox'}});
			columnBottom.push({field:'F14',	title:'',			width:  55,halign:'center',align:'right',rowspan:2,styler:cstyler1, formatter:formatterCasesu,editor:{type:'numberbox'}});
			columnBottom.push({field:'F15',	title:'',			width:  55,halign:'center',align:'right',rowspan:2,styler:cstyler1, formatter:formatterCasesu,editor:{type:'numberbox'}});
			columnBottom.push({field:'F16',	title:'',			width:  55,halign:'center',align:'right',rowspan:2,styler:cstyler1, formatter:formatterCasesu,editor:{type:'numberbox'}});
			columnBottom.push({field:'F17',	title:'',			width:  55,halign:'center',align:'right',rowspan:2,styler:cstyler1, formatter:formatterCasesu,editor:{type:'numberbox'}});
			columnBottom.push({field:'F18',	title:'重量計',		width:  70,halign:'center',align:'right',styler:cstyler1, formatter:formatterCasesu});

			columns.push(columnBottom);
			columns.push([
			   {field:'F1',title:'', hidden:true},
			   {field:'F2',title:''},
			   {field:'F3',title:'単価'},
			   {field:'F4',title:'入数'},
			   {field:'F5',title:'事前原価'},
			   {field:'F6',title:'特売総売'},
			   {field:'F18',title:'数量計'}
			]);
			columns.push([
			   {field:'F1',title:'', hidden:true},
			   {field:'F2',title:'産地'},
			   {field:'F3',title:''},
			   {field:'F4',title:'便'},
			   {field:'F5',title:'追加原価'},
			   {field:'F6',title:'特売本買'},
			   {field:'F7',title:'曜日',rowspan:2},
			   {field:'F8',title:'',rowspan:2},
			   {field:'F9',title:'',rowspan:2},
			   {field:'F10',title:'',rowspan:2},
			   {field:'F11',title:'',rowspan:2},
			   {field:'F12',title:'',rowspan:2},
			   {field:'F13',title:'',rowspan:2},
			   {field:'F14',title:'',rowspan:2},
			   {field:'F15',title:'',rowspan:2},
			   {field:'F16',title:'',rowspan:2},
			   {field:'F17',title:'',rowspan:2},
			   {field:'F18',title:'納品売価計'}
			]);

			columns.push([
			   {field:'F1',title:'', hidden:true},
			   {field:'F2',title:'コメント'},
			   {field:'F3',title:''},
			   {field:'F4',title:''},
			   {field:'F5',title:'パック原価'},
			   {field:'F6',title:'パック売価'},
			   {field:'F18',title:'納品原価計'}
			]);

			fcolumns.push(fcolumnBottom);

			var funcEnter = function(e){
				if ($.endEditingDatagrid(that)){
					$.pushUpd(e);
				}
			};
			var funcClickRow = $.fn.datagrid.defaults.onClickRow;
			var funcBeginEdit= $.fn.datagrid.defaults.onBeginEdit;
			var funcEndEdit= $.fn.datagrid.defaults.onEndEdit;
			var funcAfterEdit= $.fn.datagrid.defaults.onAfterEdit;
			if(that.reportYobiInfo()!=='1'){
				that.editRowIndex[id] = -1;
				funcClickRow = function(index,field){$.clickEditableDatagridCell(that,id, index)};
				funcBeginEdit = function(index,row){$.beginEditDatagridRow(that,id, index, row)};
				funcEndEdit = function(index,row,changes){
					$.endEditDatagridRow(that, id, index, row)
				};
				funcAfterEdit = function(index,row,changes){
					var rowobj = $('#'+id).datagrid('getPanel').find("[datagrid-row-index='"+index+"']");
					// チェックボックスの再追加（EndEdit時に削除されるため）
					$.afterEditAddCheckbox(rowobj);
					// ボタンオブジェクトの再追加（EndEdit時に削除されるため）
					rowobj.find(".easyui-linkbutton").on("click", $.pushUpd).linkbutton({ width:  45, height: 18});

					// グリッドの入力項目に枠を描画する。
					that.setGridColer(id, [row], index);
				};

				// 編集可能データグリッドの共通処理設定
				// 編集エディターの機能拡張（非表示で参照用のオブジェクトが準備してある前提、参照オブジェクトはグリッドのフィールド名を元に判断）
				$.extendDatagridEditor(that);

			}

			$('#'+id).datagrid({
				nowrap: true,
				border: true,
				striped: false,
				collapsible:false,
				remoteSort: true,
				rownumbers:false,
				fit:true,
				pageSize:pageSize,
				pageList:pageList,
				pagePosition:'bottom',
				frozenColumns:fcolumns,
				columns:columns,
				fitColumns:false,	// 指定カラム幅を適用する場合、false 指定。
				onBeforeLoad:function(param){
					param.report = that.name;

					initPagination = true;

					// 編集中の行がある場合はEndEditを行う
					var row = $('#'+id).datagrid("getSelected");
					if(row != null){
						var rowIndex = $('#'+id).datagrid("getRowIndex", row);
						$('#'+id).datagrid('endEdit',rowIndex);
					}

					// 更新データの保持
					if(that.endGridLoad){
						that.setUpdateData(id)
					}

					// 追加行情報を保持
					that.saveAppendRows();

					// Grid編集行情報を初期化
					that.editRowIndex[id] = -1;

				},
				onLoadSuccess:function(data){

					// 初回表示時にワークテーブルを更新する。
					// TODO 暫定
					// 現行画面の仕様が仕様書と異なる
					// 現行画面に合わせてワークテーブルの登録処理を一時停止
					that.updWorkTable(id, init);

					if(init){
						var rows	 = $($.id.gridholder).datagrid('getRows');
						var allRows	 = $($.id.gridholder).datagrid('getData').rows;

						init = false
					}

					// グリッドの入力項目に枠を描画する。
					that.setGridColer(id, data.rows);

					// ページ番号を保持
					that.thisPageNumber	 =  $('#'+id).datagrid('options').pageNumber										// 現在のページの番号
					that.maxPageNumber	 = Math.ceil(parseFloat(that.totalRows / $('#'+id).datagrid('options').pageSize))	// 最終のページの番号

					// 表示行のみを設定
					if(initPagination){
						initPagination = false

						if(that.totalRows != data.total){

							if(that.thisPageNumber == that.maxPageNumber){
								// 最終ページの場合行追加処理を行う
								for(var i = 0; i < that.dispAddRows.length; i++){
									var addData = that.dispAddRows[i]
									if(addData.page == that.thisPageNumber){
										// 本ページと同様のデータが含まれていた場合
										data.rows.push(addData.row);	// 追加商品の行データを追加する(既存のページング機能ではappendRowによる追加行の情報を保持できない為)
									}
								}
							}
							$($.id.gridholder).datagrid("loadData",data);
						}
					}

					// ヘッダーのレイアウト調整
					// 全タイトルを左寄せにする
					$(".datagrid-header td").children().css('text-align','left');

					// 項目ごとのスタイルを設定
					var headerColums = $(".datagrid-header-row").children();
					headerColums.each(function(){
						var field = $(this)[0].attributes[0].value
						var index = this.cellIndex

						// ヘッダーボーダー変更
						if((0 <= index  && index <= 5) || index == 17){
							$(this).css('border-top-style','none');
							$(this).css('border-bottom-style','none');
						}

						// ヘッダー背景色変更
						if((0 <= index  && index <= 6) || index == 17){
							$(this).css('background-color','#eaf2ff');
						}

						// ヘッダー部の日付、曜日内容を中央寄せにする
						if(7 <= index  && index <= 16){
							$(this).children().css('text-align','center');
						}
					});

					if(that.thisPageNumber == that.maxPageNumber){
						// 最終ページの場合
						$('#'+$.id.btn_addline).attr('tabindex', 15).show();	// 行追加ボタンを再表示
					}else{
						$('#'+$.id.btn_addline).attr('tabindex', -1).hide();	// 行追加ボタンを非表示
					}

					// ページング系　表示設定
					$('.pagination-page-list').hide()
					$('.pagination-load').hide()

					// 変更された値を設定
					that.setGridRows();

					// 初回検索データを保持
					var allRows = $($.id.gridholder).datagrid('getData').rows;
					var gridData = that.roundUpRowsDate(allRows);
					that.setGridData2(gridData, '#'+id);

					// 各項目欄の高さの設定
					$(".datagrid-cell").css('height', 'auto')

					// 改行を行う項目の設定
					$(".datagrid-cell-c3-F2").css('white-space','normal');
					$(".datagrid-cell-c3-F2").css('word-wrap','break-word');

					// セルマージ処理用の変数取得
					var state = $.data($('#'+id)[0], 'datagrid');
					var opts = state.options;
					var dc = state.dc;
					var top = $(dc.body2).scrollTop() + opts.deltaTopHeight;
					var index = Math.floor(top/opts.rowHeight);
					var page = Math.floor(index/opts.pageSize) + 1;
					var pageSize = $('#'+id).datagrid('options').pageSize

					// セルのマージ
					var data = $('#'+id).datagrid('getRows');
					var befData = "";
					var cnt = 0;
					var offset = (page-1)*pageSize;
					for (var i=0; i<data.length; i++){
						if(data[i]["F1"]=="1"){
							$('#'+id).datagrid('mergeCells',{index:i+(offset),field:'F2',colspan:4});
						}else if(data[i]["F1"]=="5"){
							$('#'+id).datagrid('mergeCells',{index:i+(offset),field:'F2',colspan:3});
						}
					}

					$('#'+id).datagrid('scrollTo', {
						index: 0,
						callback: function(index){
							$('#'+id).datagrid('beginEdit', index);
						}
					});

					// ログ出力
					$.log(that.timeData, 'query:');

					// Load完了
					that.endGridLoad = true;
				},
				onClickCell:function(rowIndex, field, value){
					// 列名保持
					that.columnName = field;
				},
				onBeforeEdit:function(index,row){
					var selectFlg = false

					if(row.F1=='2'){
						// 商品コード入力列の場合
						//if(!row.F21 || row.F21 == ""){
						if(row.F22 == "2"){
							// 追加商品フラグ = 2 の場合(追加商品)
							selectFlg = true

							// 発注数以外の入力設定を設定する
							$('#'+id).datagrid('getColumnOption', 'F2').editor = {type:'numberbox'}

							// 商品コード以外の入力設定を解除する
							$('#'+id).datagrid('getColumnOption', 'F4').editor = false;
							$('#'+id).datagrid('getColumnOption', 'F8').editor = false;
							$('#'+id).datagrid('getColumnOption', 'F9').editor = false;
							$('#'+id).datagrid('getColumnOption', 'F10').editor = false;
							$('#'+id).datagrid('getColumnOption', 'F11').editor = false;
							$('#'+id).datagrid('getColumnOption', 'F12').editor = false;
							$('#'+id).datagrid('getColumnOption', 'F13').editor = false;
							$('#'+id).datagrid('getColumnOption', 'F14').editor = false;
							$('#'+id).datagrid('getColumnOption', 'F15').editor = false;
							$('#'+id).datagrid('getColumnOption', 'F16').editor = false;
							$('#'+id).datagrid('getColumnOption', 'F17').editor = false;
						}
					}else if(row.F1=='3'){

						if(row.F21 && row.F21 != ""){
						// ケース数入力列の場合
						selectFlg = true

						// 発注数以外の入力設定を設定する
						var count = 0	// 入力不可なケース数項目をカウントする。
						for(var i = 0; i < 10; i++){
							var key = 'F'+(23 + i);	// F24～F33までが訂正区分
							if(row[key] == '0'
								|| row[key] == '1'
								|| row[key] == '2'
								|| row.F22  != "0"
							){
								// 訂正区分0,1,2の時、追加商品の時、新規登録データの時ケース数の入力が可能
								$('#'+id).datagrid('getColumnOption', 'F'+(8+i)).editor	 = {type:'numberbox'}
							}else{
								count += 1;
								$('#'+id).datagrid('getColumnOption', 'F'+(8+i)).editor = false;
							}
						}
						if(count == 10){
							// 全項目入力不可の場合は、その行を選択せずにスキップする
							selectFlg = false
						}

						// 発注数以外の入力設定を解除する
						$('#'+id).datagrid('getColumnOption', 'F2').editor = false;
						$('#'+id).datagrid('getColumnOption', 'F4').editor = false;
						}

					}else if(row.F1=='4'){
						// 便区分入力列の場合
						if(row.F22 == "2"){
							// 追加商品フラグ = 2 の場合(追加商品)
							selectFlg = true

							$('#'+id).datagrid('getColumnOption', 'F4').editor = {type:'numberbox'}
							$('#'+id).datagrid('getColumnOption', 'F2').editor = false;
							$('#'+id).datagrid('getColumnOption', 'F8').editor = false;
							$('#'+id).datagrid('getColumnOption', 'F9').editor = false;
							$('#'+id).datagrid('getColumnOption', 'F10').editor = false;
							$('#'+id).datagrid('getColumnOption', 'F11').editor = false;
							$('#'+id).datagrid('getColumnOption', 'F12').editor = false;
							$('#'+id).datagrid('getColumnOption', 'F13').editor = false;
							$('#'+id).datagrid('getColumnOption', 'F14').editor = false;
							$('#'+id).datagrid('getColumnOption', 'F15').editor = false;
							$('#'+id).datagrid('getColumnOption', 'F16').editor = false;
							$('#'+id).datagrid('getColumnOption', 'F17').editor = false;
						}
					}

					if(!selectFlg){
						// 次の行に移るか、次の項目に移るかする
						var adds = that.editRowIndex[that.focusGridId] > index ? -1:1;
						var nextindex = index + adds;
						if(nextindex >= 0 && nextindex < $('#'+id).datagrid('getRows').length){
							$('#'+id).datagrid('selectRow', nextindex);
							$('#'+id).datagrid('beginEdit', nextindex);
						}else{
							that.editRowIndex[that.focusGridId] = -9;	// 次の項目に移動するために未編集状態と違うインデックス設定
							var evt = $.Event('keydown');
							evt.keyCode = 13;
							evt.shiftKey = adds === -1;
							$('#'+id).parents('.datagrid').eq(0).trigger(evt);
						}
						return false;
					}
				},
				onClickRow: funcClickRow,
				onBeginEdit:funcBeginEdit,
				onEndEdit: funcEndEdit,
				onAfterEdit: funcAfterEdit,
				autoRowHeight:false,
				pagination:true,
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
		setGridColer: function (id, rows, index){
			var that = this;
			var backGroundColer = "#38b2ff"
			// var tabledata = $('#'+id).data('datagrid').dc.body1[0].children[1].rows
			var tabledata = $('.datagrid-view2').find('.datagrid-btable')[0].rows

			if(!rows){
				return false
			}

			if(index){
				// 行指定時は配列内にある行データが単一である事が前提
				if(rows.length != 1){
					return false
				}
			}

			for (var i=0; i<rows.length; i++){
				var row = rows[i];
				var celldata = tabledata[i].cells
				if(index){
					celldata = tabledata[index].cells
				}


				if(row.F1=='2'){
					// 商品コード入力列の場合
					//if(!row.F21 || row.F21 == ""){
					if(row.F22 == "2"){
						// 追加商品フラグ = 2 の場合(追加商品)
						// 商品コードが

						celldata[Number("F2".replace("F",""))-1].style["borderColor"] = backGroundColer
						celldata[Number("F2".replace("F",""))-1].style["border-width"] = "0.1px"

					}
				}else if(row.F1=='3'){
					if(row.F21 && row.F21 != ""){
						// 発注数以外の入力設定を設定する
						var count = 0	// 入力不可なケース数項目をカウントする。
						for(var j = 0; j < 10; j++){
							var key = 'F'+(23 + j);	// F24～F33までが訂正区分
							if(row[key] == '0'
								|| row[key] == '1'
								|| row[key] == '2'
								|| row.F22  != "0"
							){

								var td = celldata[(7+j)]
								td.style["borderColor"] = backGroundColer
								td.style["border-width"] = "0.1px"

								var div = document.createElement('div');
								div.id = 'edit_col';
								td.appendChild(div);
							}
						}
					}
				}else if(row.F1=='4'){
					// 便区分入力列の場合
					if(row.F22 == "2"){
						// 追加商品フラグ = 2 の場合(追加商品)
						celldata[Number("F4".replace("F",""))-1].style["borderColor"] = backGroundColer
						celldata[Number("F4".replace("F",""))-1].style["border-width"] = "0.1px"
					}
				}

			}
		},
		getGridData: function (sircd, target){
			var that = this;
			var data = {};
			var targetRows= [];

			if(target===undefined || target===$.id.gridholder){
				var rows	 = $($.id.gridholder).datagrid('getRows');			// 商品一覧
				for (var i=0; i<rows.length; i++){
					var rowDate = {
							F1	 : rows[i]["F1"],
							F39	 : rows[i]["F39"],
							F40	 : rows[i]["F40"],
							F41	 : rows[i]["F41"],
							F42	 : rows[i]["F42"],
							F43	 : rows[i]["F43"],
							F44	 : rows[i]["F44"],
							F45	 : rows[i]["F45"],
							F46	 : rows[i]["F46"],
							F47	 : rows[i]["F47"],
							F48	 : rows[i]["F48"]
					};
					targetRows.push(rowDate);
				}

				data[$.id.gridholder] = targetRows;
			}
			return data;
		},
		getGridData2: function (target){
			var that = this;

			var data = {};
			var targetRows= [];

			if(target===undefined || target===$.id.gridholder){
				var rows	 = $($.id.gridholder).datagrid('getRows');			// 商品一覧
				for (var i=0; i<rows.length; i++){
					var rowDate = {
							F1	 : rows[i]["F1"],		// IDX
							F16	 : rows[i]["F16"],		// 商品コード
							F35	 : rows[i]["F35"],		//
							F82	 : rows[i]["F82"],		// リストNo
							F83	 : rows[i]["F83"],		// 部門
							F84	 : rows[i]["F84"],		// 表示順
							F87	 : rows[i]["F87"],		// 追加商品フラグ
							F88	 : rows[i]["F88"],		// 訂正区分_1
							F39	 : rows[i]["F39"],		// 発注数_1
							F40	 : rows[i]["F40"],		// 発注数_2
							F41	 : rows[i]["F41"],		// 発注数_3
							F42	 : rows[i]["F42"],		// 発注数_4
							F43	 : rows[i]["F43"],		// 発注数_5
							F44	 : rows[i]["F44"],		// 発注数_6
							F45	 : rows[i]["F45"],		// 発注数_7
							F46	 : rows[i]["F46"],		// 発注数_8
							F47	 : rows[i]["F47"],		// 発注数_9
							F48	 : rows[i]["F48"],		// 発注数_10
							F98	 : rows[i]["F98"]
					};
					targetRows.push(rowDate);
				}

				data[$.id.gridholder] = targetRows;
			}
			return data;
		},
		setGridData: function (data, target){
			var that = this;

			// 複数仕入先店舗
			if(target===undefined || target==='grd_bumonyosan'){
				that.grd_bumonyosan =  data['grd_bumonyosan'];
			}
		},
		setGridData2: function (data, target){
			var that = this;

			// 商品一覧
			if(target===undefined || target===$.id.gridholder){
				//that.grd_shn_data =  data[$.id.gridholder];
				that.grd_shn_data =  data;
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
		changeInputboxFunc:function(that, id, newValue, obj){

			var parentObj = $('#'+that.focusRootId);
			var func_focus = function(){setTimeout(function(){
				var target = $.getInputboxTextbox($('#'+id));
				target.focus();
			},50);};
			if(id+"_"===obj.attr('id') && that.focusGridId!==""){
				parentObj = $('#'+that.focusGridId).datagrid('getPanel');
				func_focus = function(){setTimeout(function(){
					var target = $.getInputboxTextbox($('#'+id+'_'));
					target.focus();
				},50);};
			}

			// DB問い合わせ系
			if($('[for_inp^='+id+'_]').length > 0){
				var param = that.getInputboxParams(that, id, newValue);
				$.getsetInputboxRowData(that.name, 'for_inp', id, param, that, parentObj);
			}

			// 検索、入力後特殊処理
			if(that.queried){
				var row = $($.id.gridholder).datagrid("getSelected");
				if(row!=null){

					// 新規(正) 1.3　取得された商品コードが商品マスタテーブルに存在場合、エラー。

					if(id=="txt_shncd"){
						if(newValue !== '' && newValue){
							// 商品マスタ―への存在チェック
							var msgid = that.checkInputboxFunc($.id_inp.txt_shncd, newValue , '');
							if(msgid !==null){
								$.showMessage(msgid);
								return false;
							}

							// 部門コードとの相互チェック
							var bmncd		 = $.getJSONValue(this.jsonHidden, $.id.SelBumon).split("-")[0];		// 部門コード
							if(newValue.substring(0, 2) != bmncd){
								$.showMessage("E11162");
								return false;
							}
						}
					}

					if(id.indexOf('txt_htsu') === 0){
						// 発注数が変更された場合
						row.F22 != "2"
						if(row.F22 != "2"){
							// 新規追加商品以外の場合
							row.CHANGE = 1		// 変更フラグをたてる
						}
					}
				}
			}
		},
		getInputboxParams: function(that, id, newValue,row){
			// 情報取
			var values = {};
			values["HTSU_01"] = $.getInputboxValue($('#'+'txt_htsu1'+'_'));
			values["HTSU_02"] = $.getInputboxValue($('#'+'txt_htsu2'+'_'));
			values["HTSU_03"] = $.getInputboxValue($('#'+'txt_htsu3'+'_'));
			values["HTSU_04"] = $.getInputboxValue($('#'+'txt_htsu4'+'_'));
			values["HTSU_05"] = $.getInputboxValue($('#'+'txt_htsu5'+'_'));
			values["HTSU_06"] = $.getInputboxValue($('#'+'txt_htsu6'+'_'));
			values["HTSU_07"] = $.getInputboxValue($('#'+'txt_htsu7'+'_'));
			values["HTSU_08"] = $.getInputboxValue($('#'+'txt_htsu8'+'_'));
			values["HTSU_09"] = $.getInputboxValue($('#'+'txt_htsu9'+'_'));
			values["HTSU_10"] = $.getInputboxValue($('#'+'txt_htsu10'+'_'));
			values["IRISU"] = row["F18"];
			values["TABLEKBN"] = that.baseTablekbn;

			// 情報設定
			return [values];
		},
		setKeisan2: function(){
			var that = this;

			// Gridの編集を終了する。
			var row = $($.id.gridholder).datagrid("getSelected");
			var rowIndex = $($.id.gridholder).datagrid("getRowIndex", row);
			$($.id.gridholder).datagrid('endEdit',rowIndex);

			var update = false;	// 再計算実行フラグ

			// 再計算項目
			var NHBIK = 0		// 納品売価
			var NHGNK = 0		// 納品原価
			var NHBIK_SUM = 0	// 納品売価
			var NHGNK_SUM = 0	// 納品原価
			var JRYO = ""		// 重量
			var SURYO = 0		// 数量(ケース数計)

			// 計算用変数
			var BAIKA = 0		// 売価
			var GENKA = 0		// 原価
			var IRISU = 0		// 入数
			var TANNI = "Kg";	// 単位
			var FirstLineIdx = 0;	// 先頭行のIdx

			var row_L1 = {};	// 1行目のデータ
			var row_L2 = {};	// 2行目のデータ
			var row_L3 = {};	// 3行目のデータ
			var row_L4 = {};	// 4行目のデータ
			var row_L5 = {};	// 5行目のデータ

			var allRowsAf = [];

			var allRows = $($.id.gridholder).datagrid('getData').rows;	// 全件データ
			var rows =  $($.id.gridholder).datagrid('getRows');				// 現在表示されているデータ
			var rowsLength = rows.length

			var RefleshRangeMin = $($.id.gridholder).datagrid("getRowIndex", rows[0]);
			var RefleshRangeMax = $($.id.gridholder).datagrid("getRowIndex", rows[rowsLength-1]);

			for (var i=0; i<allRows.length; i++){
				var row = allRows[i];

				if(!row){
					break;
				}

				// 変更フラグの確認を行い、変更があった行のみをUPDATEする。
				if(row.F1 == '1'){
					update = false;			// 実行フラグ初期化
					var chnageFlg = allRows[i+2].CHANGE;
					if(chnageFlg && chnageFlg == "1"){
						// 変更有の場合
						update = true;
					}
				}

				if(update){
					if(row.F1 == '1'){
						row_L1 = {};
						row_L1 = row;
						FirstLineIdx = i;

					}else if(row.F1 == '2'){
						row_L2 = {};
						row_L2 = row;

					}else if(row.F1 == '3'){
						row_L3 = {};
						row_L3 = row;

						IRISU = 0
						IRISU = row_L3.F4		// 入数

						// 計算：数量
						SURYO = 0;
						for (var j=0; j<10; j++){
							var value = row["F"+(8+j)]
							if(value && value != ""){
								if(row_L3["F"+(j+23)] && row_L3["F"+(j+23)] != ""){
									// 訂正区分未設定の列は計算を行わない
									SURYO += (Number(value) * Number(IRISU))
								}
							}
						}
						row_L3.F18 = SURYO;

					}else if(row.F1 == '4'){
						row_L4 = {};
						row_L4 = row;

						BAIKA = 0
						if(Number(row.F35) == '0'){
							// パック売価=0の時
							BAIKA = row_L4.F6;		// 特本売価
						}else{
							BAIKA = row.F35;		// パック売価
						}

						// 計算：納品売価、納品売価計
						NHBIK_SUM = 0
						for (var j=0; j<10; j++){
							NHBIK = 0
							if((BAIKA && BAIKA != "") && (IRISU && IRISU != "")){
								if(row_L4["F"+(j+23)] && row_L4["F"+(j+23)] != ""){
									// 訂正区分未設定の列は計算を行わない
									NHBIK = Number(BAIKA) * Number(IRISU) * Number(row_L3["F"+(j+8)])
									row_L4["F" + (j+8)] = NHBIK;
									NHBIK_SUM += NHBIK;
								}
							}
						}
						NHBIK_SUM = Math.round(parseFloat(NHBIK_SUM) / 1000 );		// 小数点以下で四捨五入
						row_L4.F18 = NHBIK_SUM;

					}else if(row.F1 == '5'){
						row_L5 = {};
						row_L5 = row;

						GENKA = 0
						if(Number(row.F36) == 0){
							// パック原価=0の時
							GENKA = row_L4.F5;		// 特本原価
						}else{
							GENKA = row.F36;		// パック売価
						}

						// 計算：納品原価、納品売原価
						NHGNK_SUM = 0
						for (var j=0; j<10; j++){
							NHGNK = 0
							if((GENKA && GENKA != "") && (IRISU && IRISU != "")){
								if(row_L5["F"+(j+23)] && row_L5["F"+(j+23)] != ""){
									// 訂正区分未設定の列は計算を行わない
									NHGNK = Number(GENKA) * Number(IRISU) * Number(row_L3["F"+(j+8)])
									row_L5["F" + (j+8)] = NHGNK;
									NHGNK_SUM += NHGNK;
								}
							}
						}
						NHGNK_SUM = Math.round(parseFloat(NHGNK_SUM) / 1000 );		// 小数点以下で四捨五入
						row_L5.F18 = NHGNK_SUM;

						// 計算：重量
						JRYO = ""
						if(row_L5.F38 == '2'){
							JRYO = Math.round(parseFloat((Number(row_L5.F37) * Number(IRISU) * Number(row_L3.F18)))) + TANNI;
						}else{
							JRYO = ""
						}
						row_L1.F18 = JRYO;

						// rowsを上書きする。
						allRows[FirstLineIdx]		 = row_L1
						allRows[FirstLineIdx + 1]	 = row_L2
						allRows[FirstLineIdx + 2]	 = row_L3
						allRows[FirstLineIdx + 3]	 = row_L4
						allRows[FirstLineIdx + 4]	 = row_L5

						// 更新行をリフレッシュする
						for (var j=0; j<5; j++){
							var fefleshRowIdx = FirstLineIdx + j
							if(RefleshRangeMin <= fefleshRowIdx  && fefleshRowIdx <= RefleshRangeMax ){
								// 表示されている範囲の場合はrefreshRowを行う。それ以外の場合はscroll時に読み込まれる。
								$($.id.gridholder).datagrid('refreshRow', fefleshRowIdx);
							}
						}
						// セルのマージ
						$($.id.gridholder).datagrid('mergeCells',{index:FirstLineIdx,field:'F2',colspan:4});
						$($.id.gridholder).datagrid('mergeCells',{index:FirstLineIdx + 4,field:'F2',colspan:3});
					}
				}
			}
		},
		addLine: function(){		// 行追加ボタン押下時に新規行を追加
			var that = this;
			var maxRowLength = 15 * 5
			var v = 0;
			var data =  $($.id.gridholder).datagrid('getData');
			var rows =  data.rows;
			for(var i = 0; i<rows.length;i++ ){
				if(rows[i]["F1"]=="1" ){
					if(rows[i]["F22"]=="1" || rows[i]["F22"]=="2"){
						// 追加商品、新規追加商品の場合
						v++;
					}
				}
			}
			if(v>=35){
			//if(v>=1){
				$.showMessage('E35000');
				return false;
			}

			var allRows = $($.id.gridholder).datagrid('getData').rows;
			var dispNo = 'add_' + (allRows.length + 1);	// 暫定の表示番号(登録用データは更新時にDaoにて発行)
			var maxPageSize = Math.ceil((data.total + 5) / maxRowLength)

			$($.id.gridholder).datagrid('appendRow',{ F1:"1",F7:"販売日"	,F21:dispNo	,F22:'2', F49:maxPageSize});
			$($.id.gridholder).datagrid('appendRow',{ F1:"2",F7:"発注"		,F21:dispNo	,F22:'2', F49:maxPageSize});
			$($.id.gridholder).datagrid('appendRow',{ F1:"3",F7:"ケース数"	,F21:dispNo	,F22:'2', F49:maxPageSize});
			$($.id.gridholder).datagrid('appendRow',{ F1:"4",F7:"納品売価"	,F21:dispNo	,F22:'2', F49:maxPageSize});
			$($.id.gridholder).datagrid('appendRow',{ F1:"5",F7:"納品原価"	,F21:dispNo	,F22:'2', F49:maxPageSize});

			// 追加行が生じた為、グリッドの入力項目に枠を描画する。
			that.setGridColer("gridholder", allRows);
		},
		// 行情報から対象の行のページ数を取得する
		getPageNum:function(id, index){
			var that = this
			var maxRowLength = 15 * 5	// 1ページ内の最大表示行数(1ページ当たり15商品まで表示、1商品につき5行使用している)
			var thisPage = that.thisPageNumber	// 現在のページ番号

			if(index <= maxRowLength){
				return thisPage;
			}else{
				return thisPage;
			}
		},
		// 表示用の追加行情報を保持する
		saveAppendRows:function(){
			var that = this;
			var rows = $($.id.gridholder).datagrid('getRows');

			for(var i = 0; i < rows.length; i ++ ){
				var row = rows[i]
				if(row.F22 == '2'){
					// 追加商品フラグ=2(新規追加商品)の場合
					var pageNum= that.getPageNum("gridholder" , (i+1));
					var data = {
							page	: pageNum,	// ページ
							row		: row		// 追加行情報
					}

					var isExist = false;
					var updIndex = -1;
					var newLines = that.dispAddRows.filter(function(item, index){
						if(!isExist){
							// 変更データと同じ主キーを持つデータが存在する場合
							if(item.page == pageNum
								&& item.row.F1 == row.F1
								&& item.row.F21 == row.F21){

								updIndex = index
								isExist = true
							}
						}
					});

					if(isExist && updIndex != -1){
						that.dispAddRows[updIndex] = data
					}else{
						// 行情報を保持
						that.dispAddRows.push(data);
					}
				}
			}
		},
		checkInputboxFunc:function(id, newValue, kbn, record, isNew){
			var that = this;
			var sdt, edt;

			// 商品コード
			if(id===$.id_inp.txt_shncd){
				if(newValue !== '' && newValue){

					// 商品コード
					var param = {};
					param["KEY"] =  "MST_CNT";
					param["value"] = newValue;
					var chk_cnt = $.getInputboxData(that.name, $.id.action_check, $.id_inp.txt_shncd, [param]);
					if(chk_cnt==="" || chk_cnt==="0"){
						return "E11046";
					}
				}
			}
			return null;
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

			// 戻る実行時用に現在の画面情報を保持する
			var states = $.getBackBaseJSON(that);
			// 各種グリッド情報を設定
			var newrepinfos = $.getBackJSON(that, states);
			$.setJSONObject(sendJSON, 'repinfo', newrepinfos, '');

			// JSON Object Clone ()
			var sendJSON = JSON.parse( JSON.stringify( that.jsonHidden ) );
			// 呼出別処理
			switch (btnId) {
			case $.id.btn_back:
			case $.id.btn_cancel:
			case $.id.btn_upd:
			case $.id.btn_clear:
			case $.id.btn_saikeisan:
				var txt_lstno		= $.getJSONObject(that.jsonString, $.id_inp.txt_lstno).value;	// 月度
				// オブジェクト作成
				$.setJSONObject(sendJSON, 'reportYobi1', that.reportYobiInfo(), that.reportYobiInfo());		// 参照情報保持
				// 転送先情報
				index = 8;
				childurl = href[index];
				if(btnId == $.id.btn_back || btnId == $.id.btn_cancel){
					// 画面遷移時にワークテーブルのデータを破棄する。
					that.updWorkTableDel("gridholder");
					sendMode = 2;
				}

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
		}
	} });
})(jQuery);