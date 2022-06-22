/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_Reportx004',			// （必須）レポートオプションの確認
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
		dedefaultObjNum:	3,	// 初期化オブジェクト数
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
		sendBtnid: "",						// （必須）呼出ボタンID情報
		focusRootId:"cc",					// （キー移動時必須）キー移動イベントのルートとなるパネルのID
		focusParentId:"",					// （キー移動時必須）現在フォーカスがあたっている項目の親となるパネルのID
		focusGridId:"",						// （キー移動時必須）現在フォーカスがあたっているDataGridのID
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

			// アップロードボタン押下時処理
			//$('#'+$.id.btn_upload).on("click", that.pushUpload);

			// IFrame読み込み時
			$('#if').on('load', function (e) {
				try {
					var contents = $(this).contents();
					var data = contents.find('body')[0].innerHTML;
					var json = JSON.parse(data);
					that.queried = true;
					var msgid = "";
					if(json.opts.E_MSG === undefined){
						// ログ情報の格納
						$.post(
							$.reg.easy ,
							{
								"page"	: $($.id.hidden_reportno).val() ,
								"obj"	: $.id.btn_upload,
								"sel"	: new Date().getTime(),
								"userid": $($.id.hidden_userid).val(),
								"user"	: $($.id.hiddenUser).val(),
								"report": $($.id.hiddenReport).val(),
								"json"	: ""
							},
							function(json){}
						);


						$('#'+$.id.txt_seq).val(json.opts[$.id.txt_seq]);
						$('#'+$.id.txt_status).textbox('setValue', json.opts[$.id.txt_status]);
						$('#'+$.id.txt_upd_number).textbox('setValue', json.opts[$.id.txt_upd_number]);
						$('#'+$.id.txt_err_number).textbox('setValue', json.opts[$.id.txt_err_number]);

						that.setUploadObjectState();
						$.showMessage("I00001");
					}else{
						$('#'+$.id.txt_status).textbox('setValue', json.opts[$.id.txt_status]);
						if($.isArray(json.opts.E_MSG)){
							if(json.opts.E_MSG.length === 1 && $.messageList[json.opts.E_MSG[0].ID] ){
								$.showMessage(json.opts.E_MSG[0].ID, json.opts.E_MSG[0].PRM);
							}else{
								var msg = "";
								$.each(json.opts.E_MSG, function() {
									msg += this.MSG + "\n";
								});
								$.messager.alert({title: "エラー", icon:"error", msg: msg});
							}
						}else{
							if($.messageList[json.opts.E_MSG.ID]){
								if(json.opts.E_MSG.PRM){
									$.showMessage(json.opts.E_MSG.ID, json.opts.E_MSG.PRM);
								}else{
									$.showMessage(json.opts.E_MSG.ID);
								}
							}else{
								$.messager.alert({title: "エラー", icon:"error", msg: json.opts.E_MSG});
							}
						}
					}
				}catch(exception){
					$.showMessage("E00014");
					console.log(exception.message);
				}
				$.removeMask();
			});


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
		judgeRepType: {
			sei				: false,	// 正
			yyk				: false,	// 予
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

			// 呼出し画面情報
			var callbtnid = that.sendBtnid;
			var repstatesBef = $.getJSONObject(that.jsonHidden, "repinfo");
			var repstates = [];
			if(repstatesBef){ repstates = repstates.concat(repstatesBef.value);}
			for (var i = 0; i < repstates.length; i++) {
				if(repstates[i].id==='Out_Reportx004' && repstates[i].value && repstates[i].value.SENDBTNID){
					callbtnid = repstates[i].value.SENDBTNID;
					that.sendBtnid = callbtnid;
					break;
				}
			}

			// 正：CSV取込ボタン押下時
			if(callbtnid===$.id.btn_csv_import){
				$('#'+$.id.txt_tablekbn).val($.id.value_tablekbn_sei);

				$("#"+$.id_inp.txt_yoyakudt).numberbox('setValue', "0");
				$("#"+$.id_inp.txt_tenbaikadt).numberbox('setValue', "0");
				$.setInputBoxDisable($("#"+$.id_inp.txt_yoyakudt));
				$.setInputBoxDisable($("#"+$.id_inp.txt_tenbaikadt));

				that.judgeRepType.sei = true;
			// 予：CSV予約取込ボタン押下時
			}else if(callbtnid===$.id.btn_csv_import_yyk){
				$('#'+$.id.txt_tablekbn).val($.id.value_tablekbn_yyk);

				$("#"+$.id_inp.txt_tenbaikadt).closest("table").css('visibility','visible');

				that.judgeRepType.yyk = true;
			}

			// ファイルテキスト
			$("#"+$.id.txt_file).on("change", function (e) {
				$("#"+$.id.txt_file+"_").textbox('setValue', $(this).val())
				return false;
			});
			// アップロードボタン
			$('#'+$.id.btn_upload).on("click", that.pushUpload);

			// アップロード処理実行後に利用可能
			$('#'+$.id.btn_err_list).on("click", $.pushChangeReport);
			$('#'+$.id.btn_err_change).on("click", $.pushChangeReport);

			$.initReportInfo("IT024", "商品マスタ　CSV取込", "取込");

		},
		setUploadObjectState: function(){	// 登録時の項目制御
			var that = this;

			var isAbleChange = $('#'+$.id.txt_err_number).val()!=="0";

			if(isAbleChange){
				$('#'+$.id.btn_err_list).linkbutton('enable').attr('tabindex', 6);
				$('#'+$.id.btn_err_change).linkbutton('enable').attr('tabindex', 7);
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
		pushUpload:function(e){
			if ($(this).linkbutton('options').disabled)	return false;

			// レポート番号取得
			var reportno=$($.id.hidden_reportno).val();

			// レポート定義位置
			var reportNumber = $.getReportNumber(reportno);
			if (typeof(reportNumber) !== 'number') { alert("レポート定義が見つかりません。"); return false;}

			// フォーム情報取得
			$.report[reportNumber].getEasyUI(true);

			if ($.report[reportNumber].upValidation()) {
				// セッションタイムアウト、利用時間外の確認
				var isTimeout = $.checkIsTimeout();
				if (! isTimeout) {
					// マスク追加
					$.appendMask();

					$('#'+$.id.txt_status).textbox('setValue', "取込中");

					// パラメータをセット
					$($.id.uploadform).append('<input type="hidden" id="report" name="report" value="'+$.report[reportNumber].name+'">');

					// EasyUiの中身をパラメータとしてセット
					for(var i=0;i<$.report[reportNumber].jsonString.length;i++){
						var id = $.report[reportNumber].jsonString[i].id;
						var val = $.report[reportNumber].jsonString[i].value;

						$($.id.uploadform).append('<input type="hidden" name="'+id+'" value="'+val+'">');
					}

					// アップロード実行
					var frm = $($.id.uploadform)[0];
					frm.action = $.reg.upload;
					frm.submit();
				}
				return true;
			} else {
				return false;
			}
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
			var that = this;
			// 検索実行
			var szSeq		= $.getJSONObject(this.jsonString, $.id.txt_seq).value;			// テキストボックス：SEQ
			if(!btnId) btnId = $.id.btn_search;

			$.removeMask();
			$.removeMaskMsg();
		},
		upValidation: function (id){	// （必須）批准
			var that = this;

			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.uploadform).form('validate');
			if(!rt){
				$.addErrState(that, $('.validatebox-invalid').eq(0), false);
				return rt;
			}
			// ファイルチェック
			if (rt == true) {
				var file	= $('#'+$.id.txt_file).val();	// ファイル
				if (!file.match(/\.(csv)$/i)){
					rt = false;
					$('#'+$.id.txt_status).textbox('setValue', "");
					$.showMessage("E11012",["ファイル種類","CSVファイルを選択してください。"]);
				}
			}

			if (rt == true && that.judgeRepType.yyk) {
				var txt_yoyakudt = $.getInputboxValue($('#'+$.id_inp.txt_yoyakudt), "0");
				var txt_tenbaikadt = $.getInputboxValue($('#'+$.id_inp.txt_tenbaikadt), "0");

				var login_dt = parent.$('#login_dt').text().replace(/\//g, "");	// 処理日付
				var sysdate = login_dt;											// 比較用処理日付

				// *　店売価実施日-４日＜ﾏｽﾀｰ変更日＜店売価実施日＆処理日付＜ﾏｽﾀｰ変更日＆処理日付＜店売価実施日
				// 送信日=店売価実施日-４日
				var senddate = $.convertDate(txt_tenbaikadt, -4);

				// ④予1.新規の場合
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
			}

			// 入力エラーなしの場合に検索条件を格納
			if (rt == true) that.jsonString = that.jsonTemp.slice(0);

			// 入力チェック用の配列をクリア
			that.jsonTemp = [];

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

			// ボタンID
			this.jsonTemp.push({
				id:		"SENDBTNID",
				value:	this.sendBtnid,
				text:	this.sendBtnid
			});

			// テーブル区分
			this.jsonTemp.push({
				id:		$.id.txt_tablekbn,
				value:	$('#'+$.id.txt_tablekbn).val(),
				text:	$('#'+$.id.txt_tablekbn).val()
			});
			// SEQ
			this.jsonTemp.push({
				id:		$.id.txt_seq,
				value:	$('#'+$.id.txt_seq).val(),
				text:	$('#'+$.id.txt_seq).val()
			});
			// コメント
			this.jsonTemp.push({
				id:		$.id_inp.txt_commentkn,
				value:	$('#'+$.id_inp.txt_commentkn).textbox('getValue'),
				text:	$('#'+$.id_inp.txt_commentkn).textbox('getText')
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
		setObjectState: function(){	// 軸の選択内容による制御

		},
		setInputbox: function(id){	// 入力項目の初期値を設定する
			var that = this;

			// 初期化情報取得
			var json = $.getJSONObject(that.jsonHidden, id);
			var idx = -1;

			if($('#'+id).is(".easyui-textbox")){
				if (json && json.value.length > 0){ $('#'+id).textbox("setValue",json.value); }
			}else if($('#'+id).is(".easyui-numberbox")){
				var idx = -1;
				var formatter = $.fn.numberbox.defaults.formatter;
				var parser = $.fn.numberbox.defaults.parser;
				var options = $('#'+id).numberbox('options');
				if(options.prompt){
					var format = options.prompt.replace(/_/g, '#');
					formatter = function(value){
						return $.getFormatPrompt(value, format);
					};
					parser= function(value){
						return $.getParserPrompt(value);
					};
				}
				$('#'+id).numberbox({
					formatter:formatter,
					parser:parser
				});
				if (json && json.value.length > 0){ $('#'+id).numberbox("setValue",json.value); }
			}else{
				if (json && json.value.length > 0){ $('#'+id).val(json.value); }

			}
			if (that.initedObject && $.inArray(id, that.initedObject) < 0){
				that.initedObject.push(id);
				$.initialDisplay(that);
			}
			idx = 1;
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

			// 戻る実行時用に現在の画面情報を保持する
			that.jsonString = [];	// 当画面は戻る際に一切戻したくないため

			var states = $.getBackBaseJSON(that);
			var newrepinfos = $.getBackJSON(that, states, false);
			$.setJSONObject(sendJSON, 'repinfo', newrepinfos, '');


			// 実行ボタン別処理
			switch (btnId) {
			case $.id.btn_back:
				// 転送先情報
				index = 1;
				childurl = href[index];
				sendMode = 2;
				break;
			case $.id.btn_err_list:
			case $.id.btn_err_change:
				var szSeq = $('#'+$.id.txt_seq).val();
				if(szSeq.length===0){
					$.showMessage('E00008');
					return false;
				}
				$.setJSONObject(sendJSON, $.id.txt_seq, szSeq, szSeq);

				// 転送先情報
				index = 7;
				if(btnId === $.id.btn_err_change){
					index = 5;
				}
				childurl = href[index];
				sendMode = 1;
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