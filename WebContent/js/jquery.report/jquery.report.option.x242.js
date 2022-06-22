/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_Reportx242',			// （必須）レポートオプションの確認
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
		dedefaultObjNum:	6,	// 初期化オブジェクト数
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
		gridData:[],						// 検索結果
		gridTitle:[],						// 検索結果
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

			var isUpdateReport = true;

			// 初期検索可能
			that.onChangeReport = true;

			// 権限
			this.setAuth(reportno, 'sel_auth_ms');
			this.setAuth(reportno, 'sel_auth_tk');
			this.setAuth(reportno, 'sel_auth_tn');
			// 店舗コード
			this.setTenpo(reportno, $.id.SelTenpo);

			// ID
			$.setInputbox(that, reportno, 'txt_user_id', isUpdateReport);
			// パスワード
			$.setInputbox(that, reportno, 'txt_pass', isUpdateReport);
			// 姓
			$.setInputbox(that, reportno, 'txt_nm_family', isUpdateReport);
			// 名
			$.setInputbox(that, reportno, 'txt_nm_name', isUpdateReport);
			// 有効期限
			$.setInputbox(that, reportno, 'DT_PW_TERM', isUpdateReport);
			// 更新ユーザー
			$.setInputbox(that, reportno, 'NM_UPDATE', isUpdateReport);
			// 更新日
			$.setInputbox(that, reportno, 'DT_UPDATE', isUpdateReport);

			$.initReportInfo("x242", "ユーザーマスタ　変更" ,'変更');

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
			$.reg.search = true;	// 当画面はデフォルト検索
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

			if(that.reportYobiInfo()==='1'){
				$('#'+that.focusRootId).find('[id^=btn_]').each(function(){
					$(this).linkbutton('disable');
					$(this).attr('disabled', 'disabled').hide();
				});
				$('#'+js.focusRootId).find('.easyui-combobox').each(function(){
					$($(this).combobox('textbox')).attr('tabindex', -1).attr('readonly', 'readonly');
					$(this).attr('tabindex', -1).combobox('disable');
				});
				$('#'+js.focusRootId).find('.easyui-textbox').each(function(){
					$($(this).textbox('textbox')).attr('tabindex', -1).attr('readonly', 'readonly');
					$(this).attr('tabindex', -1).textbox('disable');
				});
				$('#'+js.focusRootId).find('.easyui-numberbox').each(function(){
					$($(this).numberbox('textbox')).attr('tabindex', -1).attr('readonly', 'readonly');
					$(this).attr('tabindex', -1).numberbox('disable');
				});

			}else{
				$($.id.buttons).show();
				// 各種遷移ボタン
				$('#'+$.id.btn_cancel).on("click", $.pushChangeReport);

				// ID
				$.setInputBoxDisable($('#txt_user_id'));
				// 姓
				$.setInputBoxDisable($('#txt_nm_family'));
				// 名
				$.setInputBoxDisable($('#txt_nm_name'));
				// 有効期限
				$.setInputBoxDisable($('#DT_PW_TERM'));
				// 更新ユーザー
				$.setInputBoxDisable($('#NM_UPDATE'));
				// 更新日
				$.setInputBoxDisable($('#DT_UPDATE'));
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
			var txt_user_cd	= $.getJSONObject(this.jsonString, 'txt_user_cd').value;		// ユーザーコード

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$.appendMaskMsg();

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,		// レポート名
					USERCD:			txt_user_cd,
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

					// 検索データ（想定）
					that.gridData = JSON.parse(json).rows;
					that.gridTitle = JSON.parse(json).titles;

					var opts = JSON.parse(json).opts

					// メインデータ表示
					that.setData(that.gridData, opts);
					that.queried = true;

					// 変更
					$($.id.hiddenChangedIdx).val('');

					// 状態保存
					$.saveState2(reportno, that.getJSONString());

					// ログ出力
					$.log(that.timeData, 'loaded:');
				}
			);
		},
		updValidation: function (){	// （必須）批准
			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform).form('validate');
			if(!rt){
				$.addErrState(that, $('.validatebox-invalid').eq(0), false);
				return rt;
			}
			return rt;
		},
		updSuccess: function(id){	// validation OK時 の update処理
			var that = this;

			var txt_user_cd	= $.getJSONObject(this.jsonString, 'txt_user_cd').value;		// ユーザーコード
			var txt_pass	= $.getInputboxValue($('#txt_pass'));

			var sel_auth_ms	= $.getInputboxValue($('#sel_auth_ms'));
			var sel_auth_tk	= $.getInputboxValue($('#sel_auth_tk'));
			var sel_auth_tn	= $.getInputboxValue($('#sel_auth_tn'));

			var txt_pass_old	= that.gridData[0]['F2'];
			var hdn_upddt		= that.gridData[0]['F12'];

			var yobi6 = '';
			var yobi7 = '';
			var yobi8 = '';
			var yobi9 = $.getInputboxValue($('#'+$.id.SelTenpo)).length >=2 ? $.getInputboxText($('#'+$.id.SelTenpo)) : '';

			if (sel_auth_ms==='2') {
				yobi6 = '1';
				yobi7 = '1';
			} else if (sel_auth_ms==='3') {
				yobi6 = '1';
			}

			if (sel_auth_tk==='2' ) {
				yobi8 = '1';
			}

			var txt_user_id	= $.getInputboxValue($('#txt_user_id'));
			var txt_szk		= that.gridData[0]['F5'];
			var infTbleKbn	= '1';
			if (txt_user_id===txt_pass && txt_szk==='本部') {
				infTbleKbn = '0';
			}

			// 変更行情報取得
			var changedIndex = $($.id.hiddenChangedIdx).val().split(",");

			var targetDatas = [{}];
			$('#'+that.focusRootId).find('[col^=F]').each(function(){
				var col = $(this).attr('col');
				if (col === 'F5') {
					targetDatas[0][col] = txt_szk;
				} else if ($(this).hasClass('easyui-combobox')){
					targetDatas[0][col] = $.getInputboxValue($(this)).replace('-1','');
				}else{
					targetDatas[0][col] = $.getInputboxValue($(this))
				}
			});

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$.appendMaskMsg();

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,		// レポート名
					action:			$.id.action_update,	// 実行処理情報
					obj:			id,								// 実行オブジェクト
					SENDBTNID:		that.sendBtnid,
					USERCD:			txt_user_cd,
					PASS:			txt_pass,
					PASSOLD:		txt_pass_old,
					AUTHMS:			sel_auth_ms,
					AUTHTK:			sel_auth_tk,
					AUTHTN:			sel_auth_tn,
					YOBI6:			yobi6,
					YOBI7:			yobi7,
					YOBI8:			yobi8,
					YOBI9:			yobi9,
					INFTBLEKBN:		infTbleKbn,
					HDN_UPDDT:		hdn_upddt,
					DATA:			JSON.stringify(targetDatas),	// 更新対象情報
					t:				(new Date()).getTime()
				},
				function(data){
					// 検索処理エラー判定
					if($.updError(id, data)) return false;

					var afterFunc = function(){
						// 初期化
						that.getEasyUI();
						that.jsonString = that.jsonTemp.slice(0);
						that.clear();
						that.changeReport(that.name, 'btn_return');
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
			// 部門コード
			this.jsonTemp.push({
				id:		'txt_user_cd',
				value:	$.getJSONValue(this.jsonHidden, 'txt_user_cd'),
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
		setAuth: function(reportno, id){	// 権限
			var that = this;
			var idx = -1;
			var onChange=false;
			var onPanel=false;

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
				required: false,
				editable: true,
				autoRowHeight:false,
				valueField:'VALUE',
				textField:'TEXT',
				multiple :false,
				prompt: '',
				icons: [],
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
					var json = [{}];
					param.page		=	reportno;
					param.obj		=	id;
					param.sel		=	(new Date()).getTime();
					param.target	=	id;
					param.action	=	$.id.action_init;
					param.json		=	JSON.stringify(json);
					param.datatype	=	"combobox";
				},
				onLoadSuccess:function(data){
					// 初期化
					var val = null;
					var init = $.getJSONValue(that.jsonHidden, id);
					if (that.initedObject && $.inArray(id, that.initedObject) < 0){
						that.initedObject.push(id);
						for (var i=0; i<data.length; i++){
							if (data[i].VALUE == init){
								val = init;
								break;
							}
						}
					}
					if (val === null && data.length>0){
						val = data[0].VALUE;
					}
					if (val){
						$('#'+id).combobox('setValue', val);
					}
					idx = 1;
					// ログ出力
					$.log(that.timeData, id+' init:');
					that.onChangeFlag = false;
				},
				onShowPanel: function(){
					$.setScrollComboBox(id);
					that.onChangeFlag=false;
					onChange=false;
					onPanel=true;
				},
				onHidePanel: function(){
					if (!onChange){
						that.onChangeFlag=true;
					}
					onChange=false;
					onPanel=false;
				},
				onChange:function(newValue, oldValue, obj){
					if(obj===undefined){obj = $(this);}

					if(idx > 0 && that.queried && $($.id.hiddenChangedIdx).is(':enabled')){
						$($.id.hiddenChangedIdx).val("1");
					}

					onChange=true;
					that.getComboErr(obj,false,newValue,oldValue);
				}
			});
		},
		setTenpo: function(reportno, id){	// 所属
			var that = this;
			var idx = -1;
			var onChange=false;
			var onPanel=false;

			$('#'+id).combobox({
				panelWidth:250,
				url:$.reg.easy,
				required: true,
				editable: false,
				autoRowHeight:false,
				valueField:'VALUE',
				textField:'TEXT',
				multiple :true,
				prompt: '',
				icons: [],
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
					var json = [{REQUIRED: 'REQUIRED'}];
					param.page		=	reportno;
					param.obj		=	id;
					param.sel		=	(new Date()).getTime();
					param.target	=	id;
					param.action	=	$.id.action_init;
					param.json		=	JSON.stringify(json);
					param.datatype	=	"combobox";
				},
				onLoadSuccess:function(data){
					// 初期化
					var val = null;
					var init = that.gridData.length !== 0 ? that.gridData[0]['F5'] : "";
					if (that.initedObject && $.inArray(id, that.initedObject) < 0){
						that.initedObject.push(id);
						for (var i=0; i<data.length; i++){
							if (data[i].VALUE == init){
								val = init;
								break;
							}
						}
					}
					if (val === null && data.length>0){
						val = data[0].VALUE;
					}
					if (val){
						$('#'+id).combobox('setValue', val);
					}
					idx = 1;
					// ログ出力
					$.log(that.timeData, id+' init:');
					that.onChangeFlag = false;
				},
				onShowPanel: function(){
					$.setScrollComboBox(id);
					that.onChangeFlag=false;
					onChange=false;
					onPanel=true;
				},
				onHidePanel: function(){
					if (!onChange){
						that.onChangeFlag=true;
					}
					onChange=false;
					onPanel=false;
				},
				onChange:function(newValue, oldValue, obj){
					if(obj===undefined){obj = $(this);}

					if(idx > 0 && that.queried && $($.id.hiddenChangedIdx).is(':enabled')){
						$($.id.hiddenChangedIdx).val("1");
					}

					if (newValue.length !== 0) {
						if (newValue[0]==='本部') {
							$('#'+id).combobox('setValue', '-1');
							$('#'+id).combobox('setText', '本部');
							$.setInputBoxDisable($('#'+id));
							return;
						}

						if (newValue[0].split(',').length >= 2) {
							newValue = newValue[0].split(',');
							$('#'+id).combobox('setValues', newValue);
						}
					}

					onChange=true;

					var text = '';
					for (var i = 1; i < newValue.length; i++) {
						if (i === 1) {
							text += newValue[i-1] + ',' + newValue[i];
						} else {
							text += ',' + newValue[i];
						}
					}

					if (!$.isEmptyVal(text)) {
						$('#'+id).combobox('setText', text);
					}
				}
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
			var sendMode = 1;

			// タブ要素(a)取得
			var elems = $('#tabContent', window.parent.document).map(
				function(i,e) {
					return e;
				}).get();
			var href = elems[0].value.split(',');

			// JSON Object Clone ()
			var sendJSON = [];
			$.setJSONObject(sendJSON, 'sendBtnid', btnId, $('#'+btnId).attr("title"));						// 実行ボタン情報保持
			$.setJSONObject(sendJSON, 'callpage', that.name, that.name);									// 呼出し元レポート情報

			// 戻る実行時用に現在の画面情報を保持する
			var states = $.getBackBaseJSON(that);
			// 各種グリッド情報を設定
			var newrepinfos = $.getBackJSON(that, states);
			$.setJSONObject(sendJSON, 'repinfo', newrepinfos, '');

			// 呼出別処理
			switch (btnId) {
			case $.id.btn_new:

				// 転送先情報
				index = 2;
				childurl = href[index];

				// オブジェクト作成
				$.setJSONObject(sendJSON, $.id.txt_sel_bmncd,'', '');

				break;

			case $.id.btn_cancel:
			case $.id.btn_back:
				// 転送先情報
				index = 1;
				childurl = href[index];
				sendMode = 2;

				break;
			case "btn_return":
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

		},
		checkInputboxFunc: function(id, newValue){

			var that = this;

			// 現パスワード存在チェック
			if(id==='txt_pass_old'){
				if(newValue !== '' && newValue){
					var txt_user_id = $.getInputboxValue($('#txt_user_id'));
					var txt_user_cd	= $.getJSONObject(this.jsonString, 'txt_user_cd').value;		// ユーザーコード
					var param = {};
					param["KEY"] =  "MST_CNT";
					param["value"] = newValue + ',' + txt_user_id + ',' + txt_user_cd;
					var chk_cnt = $.getInputboxData(that.name, $.id.action_check, id, [param]);
					if(chk_cnt==="0"){
						return "EX1077";
					}
				}
			}

			// 新パスワード過去5世代再利用不可チェック
			if(id==='txt_pass_new'){
				if(newValue !== '' && newValue){
					var txt_user_id = $.getInputboxValue($('#txt_user_id'));
					var txt_user_cd	= $.getJSONObject(this.jsonString, 'txt_user_cd').value;		// ユーザーコード
					var param = {};
					param["KEY"] =  "MST_CNT";
					param["value"] = newValue + ',' + txt_user_id + ',' + txt_user_cd;
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