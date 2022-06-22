/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Pass',						// （必須）レポートオプションの確認
		jsonTemp:	[],							// （必須）検索条件情報_入力チェック前
		jsonString:	[],							// （必須）検索条件情報
		jsonHidden: [],							// （必須）親画面からの引き継ぎ情報
		jsonInit: [],							// （必須）検索条件初期情報
		caption: function(){					// （必須）タイトル
			return $('#reportname').val();
		},
		timeData : (new Date()).getTime(),
		dedefaultObjNum:	3,					// 初期化オブジェクト数
		initObjNum:	-1,
		initedObject: [],
		initializes : true,
		queried : false,
		focusRootId:"cc",						// （キー移動時必須）キー移動イベントのルートとなるパネルのID
		initialize: function (reportno){		// （必須）初期化
			var that = this;
			// 引き継ぎ情報
			this.jsonHidden = $.getTargetValue();

			// メッセージ一覧取得
			$.initMessageListData(reportno);

			var isUpdateReport = true;
			that.queried = true;

			// 入力テキストボックス系
			$.setInputbox(that, reportno, 'txt_pass_old', isUpdateReport);
			$.setInputbox(that, reportno, 'txt_pass_new', isUpdateReport);
			$.setInputbox(that, reportno, 'txt_pass_change', isUpdateReport);

			// 初期化するオブジェクト数設定
			this.initObjNum = this.dedefaultObjNum;

			// 登録(DB更新処理) クリックイベント
			$('#'+$.id.btn_upd).on("click", that.pushUpd);

			// 画面遷移：戻る クリックイベント
			$('#'+$.id.btn_back).on("click", function(){
				// 初期化
				if ($.isEmptyVal($('#menukbn').val())) {
					window.location.href="../Servlet/Login.do?";
				} else {
					window.location.href="../Servlet/Login.do?MenuKbn="+$('#menukbn').val();
				}
			});

			// 初期化終了
			this.initializes =! this.initializes;

			// キーイベントの設定
			$.initKeyEvent(that);

			// ログ出力
			$.log(that.timeData, 'initialize:');
		},
		validation: function (){	// （必須）批准
			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform).form('validate');

			// 入力エラーなしの場合に検索条件を格納
			if (rt == true) that.jsonString = that.jsonTemp.slice(0);
			// 入力チェック用の配列をクリア
			that.jsonTemp = [];

			return rt;
		},
		success: function(reportno, sortable, btnId){	// （必須）正処理
			if (sortable) sortable=1; else sortable=0;
			var that = this;
		},
		updValidation: function (){	// （必須）批准

			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform).form('validate');
			if(!rt){
				$.addErrState(that, $('.validatebox-invalid').eq(0), false);
				return rt;
			}

			var msgid = null;
			var txt_pass_old	 = $.getInputboxValue($('#txt_pass_old'));
			var txt_pass_new	 = $.getInputboxValue($('#txt_pass_new'));
			var txt_pass_check	 = $.getInputboxValue($('#txt_pass_check'));

			// 現パスワードと新パスワードが同じ場合エラー
			if (txt_pass_old===txt_pass_new) {
				$.showMessage('E30012',["現在のパスワードと異なるパスワード"],function () {$.addErrState(this,$('#txt_pass_new'),true)});
				return false;
			}

			// 新パスワードは12～20文字以内
			if (txt_pass_new.length < 12 || txt_pass_new.length > 20) {
				$.showMessage('E30012',["新パスワードは12～20文字以内"],function () {$.addErrState(this,$('#txt_pass_new'),true)});
				return false;
			} else {
				var az = false;
				var num = false;
				for (var i=0; i < txt_pass_new.length; i++) {
					var val = txt_pass_new.slice(i,(i+1));
					if (!az && /^[A-Za-z]*$/.test(val)) {
						az=true;
					}

					if (!num && /^[0-9]*$/.test(val)) {
						num=true;
					}

					if (az && num) {
						break;
					}
				}
				if (!az || !num) {
					$.showMessage('E30012',["新パスワードは英数字を含んだ文字"],function () {$.addErrState(this,$('#txt_pass_new'),true)});
					return false;
				}
			}

			// 現パスワードの存在チェック
			msgid = that.checkInputboxFunc('txt_pass_old', txt_pass_old, '');
			if(msgid !== null){
				$.showMessage('E30012',["存在するパスワード"],function () {$.addErrState(this,$('#txt_pass_old'),true)});
				return false;
			}

			// 新パスワードの履歴チェック
			msgid = that.checkInputboxFunc('txt_pass_new', txt_pass_new, '');
			if(msgid !== null){
				$.showMessage('E30012',["過去5世代と異なるパスワード"],function () {$.addErrState(this,$('#txt_pass_new'),true)});
				return false;
			}

			// 新パスワードと確認用パスワードの不一致
			if (txt_pass_new!==txt_pass_check) {
				$.showMessage('E30012',["新パスワードと一致する文字"],function () {$.addErrState(this,$('#txt_pass_check'),true)});
				return false;
			}
			return rt;
		},
		updSuccess: function(id){	// validation OK時 の update処理
			var that = this;

			var pass	= $.getInputboxValue($('#txt_pass_new'));
			var passOld	= $.getInputboxValue($('#txt_pass_old'));

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$.appendMaskMsg();
			$.appendMask();

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,				// レポート名
					action:			$.id.action_update,		// 実行処理情報
					obj:			id,						// 実行オブジェクト
					PASS:			pass,
					PASSOLD:		passOld,
					SENDBTNID:		that.sendBtnid,
					t:				(new Date()).getTime()
				},
				function(data){
					// 検索処理エラー判定
					if($.updError(id, data)) {
						$('#login').submit();
					} else {
						var afterFunc = function(){
							// 初期化
							if ($.isEmptyVal($('#menukbn').val())) {
								window.location.href="../Servlet/Login.do?MenuKbn=-1&User="+$($.id.hidden_userid).val()+"&Pass="+pass;
							} else {
								window.location.href="../Servlet/Login.do?MenuKbn="+$('#menukbn').val()+"&User="+$($.id.hidden_userid).val()+"&Pass="+pass;
							}
						};
						$.updNormal(data, afterFunc);
					}
					// ログ出力
					$.log(that.timeData, 'loaded:');
				}
			);
		},
		/**
		 * 登録(DB更新)ボタンイベント
		 * @param {Object} e
		 */
		pushUpd:function(e){

			// レポート番号取得
			var reportno=$($.id.hidden_reportno).val();
			// レポート定義位置
			var reportNumber = $.getReportNumber(reportno);
			if (typeof(reportNumber) !== 'number') { alert("レポート定義が見つかりません。"); return false;}

			// JS情報取得
			var that = $.report[reportNumber];
			var id = $(this).attr('id');

			// チェック・確認処理
			var rtn = true;
			if($.isFunction(that.updValidation)) { rtn = that.updValidation(id);}
			// 変更情報チェック
			if(rtn && !$.getConfirmUnregistFlg($($.id.hiddenChangedIdx))){
				$.showMessage('E20582');
				return false;
			}

			if(rtn){
				var func_ok = function(r){
					// セッションタイムアウト、利用時間外の確認
					var isTimeout = $.checkIsTimeout();
					if (! isTimeout) {
						// ログの書き込み
						$.ajax({
							url: $.reg.easy,
							type: 'POST',
							async: false,
							data: {
								"page"	: $($.id.hidden_reportno).val() ,
								"obj"	: id,
								"sel"	: new Date().getTime(),
								"userid": $($.id.hidden_userid).val(),
								"user"	: $($.id.hiddenUser).val(),
								"report": $($.id.hiddenReport).val(),
								"json"	: ""
							},
							success: function(json){
								that.updSuccess(id);
							}
						});
					}
					return true;
				};
				$.showMessage("W00001", undefined, func_ok);
			}
		},
		checkInputboxFunc: function(id, newValue){

			var that = this;

			// 現パスワード存在チェック
			if(id==='txt_pass_old'){
				if(newValue !== '' && newValue){
					var param = {};
					param["KEY"] =  "MST_CNT";
					param["value"] = newValue;
					var chk_cnt = $.getInputboxData(that.name, $.id.action_check, id, [param]);
					if(chk_cnt==="0"){
						return "EX1077";
					}
				}
			}

			// 新パスワード過去5世代再利用不可チェック
			if(id==='txt_pass_new'){
				if(newValue !== '' && newValue){
					var param = {};
					param["KEY"] =  "MST_CNT";
					param["value"] = newValue;
					var chk_cnt = $.getInputboxData(that.name, $.id.action_check, id, [param]);
					if(chk_cnt==="0"){
						return "E11044";
					}
				}
			}

			return null;
		},
	} });
})(jQuery);