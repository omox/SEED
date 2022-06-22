/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_ReportTM004',			// （必須）レポートオプションの確認
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
		dedefaultObjNum:	10,	// 初期化オブジェクト数
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
		subData:[],							// 検索結果保持用(グリッド情報)

		grd_data:[],						// メイン情報：商品マスタ
		grd_data_other:[],					// 補足情報：その他、テーブルに登録しない情報などを保持

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

			// 新規以外の場合、検索処理を実施
			if(!that.judgeRepType.sei_new){
				// 検索実行
				that.onChangeReport = true;
			}

			// 処理日付取得
			$.getsetInputboxData(reportno, $.id.txt_shoridt, [{}], $.id.action_init);

			var isUpdateReport = true;
			// 催し区分
			$.setInputbox(that, reportno, $.id_inp.txt_moyskbn, isUpdateReport);
			// 催し開始日
			$.setInputbox(that, reportno, $.id_inp.txt_moysstdt, isUpdateReport);
			// 催し連番
			$.setInputbox(that, reportno, $.id_inp.txt_moysrbaninp, isUpdateReport);
			// 販売開始日
			$.setInputbox(that, reportno, $.id_inp.txt_hbstdt, isUpdateReport);
			// 販売終了日
			$.setInputbox(that, reportno, $.id_inp.txt_hbeddt, isUpdateReport);
			// 納入開始日
			$.setInputbox(that, reportno, $.id_inp.txt_nnstdt, isUpdateReport);
			// 納入終了日
			$.setInputbox(that, reportno, $.id_inp.txt_nneddt, isUpdateReport);
			// PLU配信日
			$.setInputbox(that, reportno, $.id_inp.txt_plusddt, isUpdateReport);
			// 催し名称（漢字）
			$.setInputbox(that, reportno, $.id_inp.txt_moykn, isUpdateReport);
			// 催し名称（カナ）
			$.setInputbox(that, reportno, $.id_inp.txt_moyan, isUpdateReport);

			// 新規の場合、検索を実行しないので、検索結果を受けての初期化を直接呼出し
			if(that.judgeRepType.sei_new){
				that.searched_initialize(reportno);
			}

			// Load処理回避
			//$.tryChangeURL(null);

			// 初期化終了
			this.initializes =! this.initializes;

			// ログ出力
			$.log(that.timeData, 'initialize:');
		},
		searched_initialize: function (reportno, opts){	// 検索結果を受けての初期化
			var that = this;

			// 処理日付
			if(that.judgeRepType.sei_upd){
				var shoridt = $('#'+$.id.txt_shoridt).val();
				var hbstdt =  $.getInputboxValue($('#'+$.id_inp.txt_hbstdt));
				var plusddt = $.getInputboxValue($('#'+$.id_inp.txt_plusddt));
				if(shoridt!==''
				&&((hbstdt!=='' && hbstdt*1 <= shoridt*1)
				|| (plusddt!=='' && plusddt < shoridt*1 ))){

					$("#"+$.id.btn_upd).linkbutton('disable');
					$("#"+$.id.btn_upd).attr('tabindex', -1).hide();
					$("#"+$.id.btn_del).linkbutton('disable');
					$("#"+$.id.btn_del).attr('tabindex', -1).hide();
				}

				// PLU配信日
				// 催しコード.PLU配信済みフラグ=1の場合、編集不可
				if(that.baseData && that.baseData[0] && that.baseData[0]["F15"]==="1"){
					$.setInputBoxDisableVariable($('#'+$.id_inp.txt_plusddt), true);
				}
			}

			// チェックボックスの設定
			$.initCheckboxCss($("#"+that.focusRootId));
			// キーイベントの設定
			$.initKeyEvent(that);

			that.queried = true;

			// ログ出力
			$.log(that.timeData, 'searched_initialize:');
		},
		judgeRepType: {
			sei_new 		: false,	// 正 -新規
			sei_upd 		: false,	// 正 -更新
			sei_ref 		: false,	// 正 -参照
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

			// 帳票タイプ判断：ボタン情報のみで判断
			if(that.sendBtnid===$.id.btn_new){
				that.judgeRepType.sei_new = true;
			}else if(that.sendBtnid===$.id.btn_sel_change){
				that.judgeRepType.sei_upd = true;
			}else if(that.sendBtnid===$.id.btn_sel_refer){
				that.judgeRepType.sei_ref= true;
			}

			that.baseTablekbn = $.id.value_tablekbn_sei;						// 情報取得先
			// 新規：
			if(that.judgeRepType.sei_new){
				$("#disp_record_info").hide();

				$.setInputBoxDisable($("#"+$.id.btn_del)).hide();

				$.initReportInfo("TM004", "催しコード　その他催し　新規", "新規");
			// 変更：
			}else if(that.judgeRepType.sei_upd){
				// 催し区分
				$.setInputBoxDisable($("#"+$.id_inp.txt_moyskbn));
				// 催し開始日
				$.setInputBoxDisable($("#"+$.id_inp.txt_moysstdt));
				// 催し連番
				$.setInputBoxDisable($("#"+$.id_inp.txt_moysrbaninp));
				// 販売開始日
				$.setInputBoxDisable($("#"+$.id_inp.txt_hbstdt));

				$.initReportInfo("TM004", "催しコード　その他催し　変更", "変更");
			}
			$('#'+$.id.btn_cancel).on("click", $.pushChangeReport);
			$($.id.buttons).show();
			// 変更
			$($.id.hiddenChangedIdx).val('');
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
			var txt_moyskbn		= $.getJSONObject(this.jsonString, $.id_inp.txt_moyskbn).value;		// 催し区分
			var txt_moysstdt	= $.getJSONObject(this.jsonString, $.id_inp.txt_moysstdt).value;	// 催し開始日
			var txt_moysrban	= $.getJSONObject(this.jsonString, $.id_inp.txt_moysrban).value;	// 催し連番

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$.appendMaskMsg();

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,			// レポート名
					MOYSKBN:		txt_moyskbn,		// 催し区分
					MOYSSTDT:		txt_moysstdt,		// 催し開始日
					MOYSRBAN:		txt_moysrban,		// 催し連番
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

					// メインデータ表示
					that.setData(that.baseData, opts);
					// 催し区分による状態制御
					that.setObjectState($.getInputboxValue($('#'+$.id_inp.txt_moyskbn)));
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
		getGridData: function (target,  delFlg){
			var that = this;

			var data = {};

			// 基本情報
			if(target===undefined || target==="grd_data"){
				var targetData = [{}];
				$('#'+that.focusRootId).find('[col^=F]').each(function(){
					var col = $(this).attr('col');
					var val = $.getInputboxValue($(this));
					if($(this).hasClass('easyui-combobox') && val==="-1"){ val = "";}
					targetData[0][col] = val;
				});
				data["grd_data"] = targetData;
			}

			// 補足情報(テーブルに登録しない情報などを保持)
			if(target===undefined || target==="grd_data_other"){
				var targetData = [{}];
				//targetData[0]["KETAKBN"] = $.getInputboxValue($('#'+$.id_mei.kbn143));	// 桁指定
				data["grd_data_other"] = targetData;
			}

			return data;
		},
		setGridData: function (data, target, delFlg){
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

			// 新規(正)：新規・新規コピー・選択コピーボタン押下時
			var isNew = that.judgeRepType.sei_new;
			// 変更(正)：検索・変更・正ボタン押下時
			var isChange = that.judgeRepType.sei_upd;

			var login_dt = parent.$('#login_dt').text().replace(/\//g, "");	// 処理日付
			var sysdate = login_dt.substr(2, 6);							// 比較用処理日付

			// 新規(正) 1.1　必須入力項目チェックを行う。
			// 変更(正) 1.1　必須入力項目チェックを行う。
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform).form('validate');
			if(!rt){
				$.showMessage('E00001');
				return rt;
			}

			var moyskbn = $('#'+$.id_inp.txt_moyskbn).numberbox('getValue');
			var msgid = that.checkInputboxFunc($.id_inp.txt_moyskbn, moyskbn);
			if(msgid !==null){
				$.showMessage(msgid);
				return false;
			}

			if(!isChange){
				var targetOId = [$.id_inp.txt_moysstdt,$.id_inp.txt_moysrbaninp ,$.id_inp.txt_hbstdt,$.id_inp.txt_hbeddt,$.id_inp.txt_nnstdt,$.id_inp.txt_nneddt,$.id_inp.txt_plusddt];
				for (var i = 0; i < targetOId.length; i++){
					msgid = that.checkInputboxFunc(targetOId[i], $('#'+targetOId[i]).numberbox('getValue'), moyskbn);
					if(msgid !==null){
						$.showMessage(msgid);
						return false;
					}
				}
			}

			var hbs = $('#'+$.id_inp.txt_hbstdt).numberbox('getValue');

			var shoridt = $('#'+$.id.txt_shoridt).val();
			if(shoridt!=='' && ('20'+hbs)*1 <= shoridt*1 ){
				$.showMessage("EX1124");
				return false;
			}


			if(moyskbn == 8 || moyskbn == 9){
				var hbe = $('#'+$.id_inp.txt_hbeddt).numberbox('getValue');
				var nst = $('#'+$.id_inp.txt_nnstdt).numberbox('getValue');
				var nne = $('#'+$.id_inp.txt_nneddt).numberbox('getValue');

				if(hbs != nst){
					$.showMessage("E20086");
					return false;
				}
				if(hbe != nne){
					$.showMessage("E20087");
					return false;
				}
			}else{
				var plusddt = $('#'+$.id_inp.txt_plusddt).numberbox('getValue');
				if(shoridt!=='' && ('20'+plusddt)*1 < shoridt*1 ){
					$.showMessage("E20013");
					return false;
				}
			}
			if(moyskbn != 2){
				var nst = $('#'+$.id_inp.txt_nnstdt).numberbox('getValue');
				var nne = $('#'+$.id_inp.txt_nneddt).numberbox('getValue');
				if(nst > nne){	// 期間が逆の場合
					$.showMessage("E20301");
					return false;
				}
			}

			var gridData = that.getGridData();

			// 入力情報を変数に格納
			if (rt == true) that.setGridData(gridData);

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
			// 補足情報
			var targetDataOther = that.grd_data_other;


			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$.appendMaskMsg();
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
			var isChange = that.judgeRepType.sei_upd;

			var moyskbn = $('#'+$.id_inp.txt_moyskbn).numberbox('getValue');

			var param = {};
			param["KEY"] =  "CNT";
			param["MOYSKBN"] = $('#'+$.id_inp.txt_moyskbn).numberbox('getValue');;
			param["MOYSSTDT"] = $('#'+$.id_inp.txt_moysstdt).numberbox('getValue');
			param["MOYSRBAN"] = $('#'+$.id_inp.txt_moysrbaninp).numberbox('getValue');
			var chk_cnt = $.getInputboxData(that.name, $.id.action_check, 'MOYOKBN'+moyskbn, [param]);

			if(chk_cnt!==""&&chk_cnt!=="0"){
				$.showMessage('E20067');
				return false;
			}

			if(!isChange){
				return false;
			}

			return rt;
		},
		delSuccess: function(id){
			var that = this;
			var is_warning = false;

			// 基本情報
			var targetData = that.grd_data;

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
					SENDBTNID:		that.sendBtnid,
					DATA:			JSON.stringify(targetData),			// 更新対象情報
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

			// 催し区分
			this.jsonTemp.push({
				id:		$.id_inp.txt_moyskbn,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_moyskbn),
				text:	''
			});
			// 催し開始日
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
				index = 2;
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
		setObjectState: function(kbn){	// 催し区分による制御
			var disablekbn_nn = [2];
			var disablekbn_plu= [7, 8, 9];

			if(disablekbn_nn.indexOf(kbn*1)!==-1){	// 利用可能かチェック
				$.setInputBoxDisableVariable($('#'+$.id_inp.txt_nnstdt), true);
				$.setInputBoxDisableVariable($('#'+$.id_inp.txt_nneddt), true);
			}else{
				$.setInputBoxEnableVariable($('#'+$.id_inp.txt_nnstdt));
				$.setInputBoxEnableVariable($('#'+$.id_inp.txt_nneddt));
			}
			// PLU配信日
			if(disablekbn_plu.indexOf(kbn*1)!==-1){// 利用可能かチェック
				$.setInputBoxDisableVariable($('#'+$.id_inp.txt_plusddt), true);
			}else{
				$.setInputBoxEnableVariable($('#'+$.id_inp.txt_plusddt));
			}
		},
		changeInputboxFunc:function(that, id, newValue, obj){

			var parentObj = $('#'+that.focusRootId);
			var func_focus = function(){setTimeout(function(){
				var target = $.getInputboxTextbox($('#'+id));
				target.focus();
			},50);};

			// DB問い合わせ系
			if($('[for_inp^='+id+'_]').length > 0){
				var param = that.getInputboxParams(that, id, newValue);
				$.getsetInputboxRowData(that.name, 'for_inp', id, param, that, parentObj);
			}

			// 検索、入力後特殊処理
			if(that.queried){
				var msgid = null;

				// 催し区分
				if(id===$.id_inp.txt_moyskbn){
					msgid = that.checkInputboxFunc(id, newValue);
					if(msgid !==null){
						$.showMessage(msgid, undefined, func_focus );
						return false;
					}
					that.queried = false;
					$('#'+$.id_inp.txt_moysstdt).numberbox('setValue',"");	// 催しコード
					$('#'+$.id_inp.txt_hbeddt).numberbox('setValue',"");
					$('#'+$.id_inp.txt_hbstdt).numberbox('setValue',"");
					$('#'+$.id_inp.txt_nneddt).numberbox('setValue',"");
					$('#'+$.id_inp.txt_nnstdt).numberbox('setValue',"");
					$('#'+$.id_inp.txt_plusddt).numberbox('setValue',"");
					that.queried = true;
					that.setObjectState(newValue);
				}

				// その他の入力項目のエラーチェック
				var moyskbn = $('#'+$.id_inp.txt_moyskbn).numberbox('getValue')*1;
				msgid = that.checkInputboxFunc(id, newValue, moyskbn);
				if(msgid !==null){
					$.showMessage(msgid, undefined, func_focus );
					return false;
				}

				// データのデフォルト値設定
				// 催し開始日(催しコード)
				if(id===$.id_inp.txt_moysstdt){
					// ●販売期間のデフォルト値 ※終了日から直す
					var txt_hbstdt = that.calcDefHbstdt(newValue, moyskbn);
					$('#'+$.id_inp.txt_hbeddt).numberbox('setValue',"");
					$('#'+$.id_inp.txt_hbstdt).numberbox('setValue',txt_hbstdt);

					// ●納入期間のデフォルト値 ※終了日から直す
					$('#'+$.id_inp.txt_nneddt).numberbox('setValue',"");
					$('#'+$.id_inp.txt_nnstdt).numberbox('setValue',that.calcDefNnstdt(txt_hbstdt, moyskbn));

					// ●ＰＬＵ配信日のデフォルト値
					$('#'+$.id_inp.txt_plusddt).numberbox('setValue',that.calcDefPlusddt(txt_hbstdt, moyskbn));
				}
				// 販売期間終了日
				if(id===$.id_inp.txt_hbeddt){
					var oldvalue =  $.getInputboxValue($('#'+$.id_inp.txt_nneddt));
					if(oldvalue.length===0){
						// ●納入期間のデフォルト値
						$('#'+$.id_inp.txt_nneddt).numberbox('setValue',that.calcDefNneddt(newValue, moyskbn));
					}
				}
			}
		},
		// IDとvalueでチェック処理を実施
		checkInputboxFunc:function(id, newValue, kbn){
			var that = this;
			// 催し区分
			if(id===$.id_inp.txt_moyskbn){
				if([2, 3, 5, 7, 8, 9].indexOf(newValue*1)===-1){
					return "E20078";
				}
			}

			var sdt, edt;

			// 催しコード
			if(id===$.id_inp.txt_moysstdt||id===$.id_inp.txt_moysrbaninp){
				var param = {};
				param["KEY"] =  "CNT";

				var moyskbn = $('#'+$.id_inp.txt_moyskbn).numberbox('getValue');
				var moysstdt = $('#'+$.id_inp.txt_moysstdt).numberbox('getValue');
				var moysrbaninp = $('#'+$.id_inp.txt_moysrbaninp).numberbox('getValue');
				if(moysstdt!=""&&moysrbaninp!=""&&moyskbn!=""){
					param["MOYSKBN"] = moyskbn;
					param["MOYSSTDT"] = moysstdt;
					param["MOYSRBAN"] = moysrbaninp;
					var chk_cnt = $.getInputboxData(that.name, $.id.action_check, 'MOYOKBN', [param]);
					if(chk_cnt!==""&&chk_cnt!=="0"){
						return "E20275";
					}
				}
			}

			if(id===$.id_inp.txt_moysrbaninp){
				if(newValue>49){
					return "E20141";
				}
			}

			// 販売期間
			if(id===$.id_inp.txt_hbstdt||id===$.id_inp.txt_hbeddt){
				// 販売期間開始日
				if(id===$.id_inp.txt_hbstdt){
					var moysstdt = $('#'+$.id_inp.txt_moysstdt).numberbox('getValue');
					if([2, 3, 7, 8, 9].indexOf(kbn)!==-1){
						if(moysstdt!==newValue){
							return "E20080";
						}
					}else if([5].indexOf(newValue*1)!==-1){
						sdt = $.convDate(moysstdt, true);
						edt = $.convDate(newValue, true);
						if($.getDateDiffDay(sdt, edt) > 0 ){
							return "E20081";
						}
					}
					// 処理日付
					if(that.judgeRepType.sei_new){
						var shoridt = $('#'+$.id.txt_shoridt).val();
						if(shoridt!=='' && ('20'+newValue)*1 <= shoridt*1 ){
							return "E20249";
						}
					}

					// 終了日が未入力の場合、比較は終了
					var hbeddt = $('#'+$.id_inp.txt_hbeddt).numberbox('getValue');
					if(hbeddt.length < 6){
						return null;
					}
					sdt = $.convDate(newValue, true);
					edt = $.convDate(hbeddt, true);
				}

				// 販売期間終了日
				if(id===$.id_inp.txt_hbeddt){
					sdt = $.convDate( $('#'+$.id_inp.txt_hbstdt).numberbox('getValue'), true);
					edt = $.convDate(newValue, true);
				}

				if(sdt.getTime() > edt.getTime()){	// 期間が逆の場合
					return "E20006";
				}

				// 期間日数チェック
				var days = $.getDateDiffDay(sdt, edt);
				if([2].indexOf(kbn)!==-1 && days >= 125){
					return "E20082";
				}
				if([3].indexOf(kbn)!==-1 && days >= 11){
					return "E20068";
				}
				if(([5].indexOf(kbn)!==-1 && days >= 94)
				 ||([7, 8, 9].indexOf(kbn)!==-1 && days >= 94)
				){
					return "E20084";
				}
			}

			// 納入期間
			if(id===$.id_inp.txt_nnstdt||id===$.id_inp.txt_nneddt){
				// 催し区分＝２の時、登録時入力あったらエラー
				if([2].indexOf(kbn)!==-1 && !$.isEmptyVal(newValue)){
					// E20044	催し区分2は、納入開始日または納入終了日にデータがある場合、編集できません。	 	0	 	E
					return "E20044";
				}

				// 納入期間開始日
				if(id===$.id_inp.txt_nnstdt){
					// 販売期間開始日との比較
					var hbstdt = $('#'+$.id_inp.txt_hbstdt).numberbox('getValue');
					if([3, 5, 7].indexOf(kbn)!==-1){
						sdt = $.convDate(hbstdt, true);
						edt = $.convDate(newValue, true);
						if($.getDateDiffDay(sdt, edt) < -7 ){
							return "E20069";
						}
					}else if([8, 9].indexOf(kbn)!==-1){
						if(hbstdt!==newValue){
							return "E20086";
						}
					}
					// 販売期間終了日との比較
					var hbeddt = $('#'+$.id_inp.txt_hbeddt).numberbox('getValue');
					if(hbeddt.length < 6){
						return null;
					}
					if([3].indexOf(kbn)!==-1){
						sdt = $.convDate(hbeddt, true);
						edt = $.convDate(newValue, true);
						if($.getDateDiffDay(sdt, edt) < -10 ){
							return "E20070";
						}
					}

					// 終了日が未入力の場合、比較は終了
					var nneddt = $('#'+$.id_inp.txt_nneddt).numberbox('getValue');
					if(nneddt.length < 6){
						return null;
					}
					sdt = $.convDate(newValue, true);
					edt = $.convDate(hbeddt, true);
				}

				// 納入期間終了日
				if(id===$.id_inp.txt_nneddt){
					// 販売期間開始日との比較
					var hbstdt = $('#'+$.id_inp.txt_hbstdt).numberbox('getValue');
					if([3].indexOf(kbn)!==-1){
						sdt = $.convDate(hbstdt, true);
						edt = $.convDate(newValue, true);
						if($.getDateDiffDay(sdt, edt) > 10 ){
							return "E20072";
						}
					}
					// 販売期間終了日との比較
					var hbeddt = $('#'+$.id_inp.txt_hbeddt).numberbox('getValue');
					if(hbeddt.length < 6){
						return null;
					}
					if([3].indexOf(kbn)!==-1){
						sdt = $.convDate(hbeddt, true);
						edt = $.convDate(newValue, true);
						if($.getDateDiffDay(sdt, edt) > 1 ){
							return "E20071";
						}
					}else if([5,7].indexOf(kbn)!==-1){
						sdt = $.convDate(hbeddt, true);
						edt = $.convDate(newValue, true);
						if($.getDateDiffDay(sdt, edt) > 2 ){
							return "E20085";
						}
					}else if([8,9].indexOf(kbn)!==-1){
						if(hbeddt!==newValue){
							return "E20087";
						}
					}
					sdt = $.convDate($('#'+$.id_inp.txt_nnstdt).numberbox('getValue'), true);
					edt = $.convDate(newValue , true);
				}

				if(sdt.getTime() > edt.getTime()){	// 期間が逆の場合
					return "E20301";
				}

				// 期間日数チェック
				var days = $.getDateDiffDay(sdt, edt);
				if([3].indexOf(kbn)!==-1 && days >= 11){
					return "E20068";
				}
			}
			// PLU配信日
			if(id===$.id_inp.txt_plusddt){
				var hbstdt = $('#'+$.id_inp.txt_hbstdt).numberbox('getValue');
				if([2, 3, 5].indexOf(kbn)!==-1){
					sdt = $.convDate(hbstdt, true);
					edt = $.convDate(newValue, true);
					if($.getDateDiffDay(sdt, edt) < -14
							|| $.getDateDiffDay(sdt, edt) > -2){
						return "E20075";
					}
					var shoridt = $('#'+$.id.txt_shoridt).val();
					if(shoridt!=='' && ('20'+newValue)*1 < shoridt*1 ){
						return "E20013";
					}
				}

				// 処理日付
				//if(that.judgeRepType.sei_new){

				//}
			}

			return null;
		},
		// 販売期間開始日のデフォルト値算出
		calcDefHbstdt: function(newValue, kbn){
			if(newValue.length === 0){ return ''}

			if([5].indexOf(kbn)!==-1){
				var dObj = $.convDate(newValue, true);
				dObj.setDate(dObj.getDate() - 1);
				return $.dateFormat(dObj);
			}
			return newValue;
		},
		// 納入期間開始日のデフォルト値算出
		calcDefNnstdt: function(newValue, kbn){
			if(newValue.length === 0){ return ''}
			// 販売期間開始日以前の直近の日曜日　（＊２）
			if([3].indexOf(kbn)!==-1){
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
			// 販売期間開始日
			if([5, 8, 9].indexOf(kbn)!==-1){
				return newValue;
			}
			// なし
			if([7].indexOf(kbn)!==-1){
				return '';
			}
			return '';
		},
		// 納入期間終了日のデフォルト値算出
		calcDefNneddt: function(newValue, kbn){
			if(newValue.length === 0){ return ''}
			// 販売期間開始日
			if([3, 5, 8, 9].indexOf(kbn)!==-1){
				return newValue;
			}
			// なし
			if([7].indexOf(kbn)!==-1){
				return '';
			}
			return '';
		},
		// ＰＬＵ配信日のデフォルト値算出
		calcDefPlusddt: function(newValue, kbn){
			if(newValue.length === 0){ return ''}
			// 販売期間開始日-7
			if([2, 3, 5].indexOf(kbn)!==-1){
				var dObj = $.convDate(newValue, true);
				dObj.setDate(dObj.getDate() - 7);
				return $.dateFormat(dObj);
			}
			return '';
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