/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_ReportRP008',			// （必須）レポートオプションの確認
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
		sendBtnid: "",						// （必須）呼出ボタンID情報
		focusRootId:"cc",					// （キー移動時必須）キー移動イベントのルートとなるパネルのID
		focusParentId:"",					// （キー移動時必須）現在フォーカスがあたっている項目の親となるパネルのID
		focusGridId:"",						// （キー移動時必須）現在フォーカスがあたっているDataGridのID
		editRowIndex:{},					// グリッド編集行保持
		baseData:[],						// 検索結果保持用
		updData:[],							// 検索結果保持用
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

			var isUpdateReport = false;

			// 入力テキストボックス系
			var inputbox = Object.getOwnPropertyNames($.id_inp);
			for ( var sel in inputbox ) {
				if($('#'+$.id_inp[inputbox[sel]]).length > 0){
					$.setInputbox(that, reportno, $.id_inp[inputbox[sel]], isUpdateReport);
				}
			}

			// ラジオボタン系
			$.setRadioInit(that.jsonHidden, $.id.rad_ptnnokbn, that);

			// 検索実行
			that.onChangeReport = false;

			// Load処理回避
			//$.tryChangeURL(null);

			// 初期化終了
			this.initializes =! this.initializes;

//			// チェックボックスの設定
//			$.initCheckboxCss($("#"+that.focusRootId));
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
				//$.reg.search = true;
			}
			$($.id.buttons).show();
			// 各種遷移ボタン
			$('#'+$.id.btn_cancel).on("click", $.pushChangeReport);
			$('#'+$.id.btn_tenbetusu).on("click", $.pushChangeReport);
			$.initReportInfo("RP008", "数量計算");
		},
		initCondition: function (){	// 条件初期値セット
			var that = this;
			// 初期化項目
		},
		clear:function(){
//			// 隠し情報初期化
//			$($.id.hiddenChangedIdx).val("");						// 変更行Index
		},
		validation: function (){	// （必須）批准
			var that = this;

			// 入力チェック
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform).form('validate');
			if(!rt){
				$.messager.alert($.message.ID_MESSAGE_TITLE_WARN,'入力内容を確認してください。','warning');
				return rt;
			}
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

			var txt_bmncd			= $('#'+$.id_inp.txt_bmncd).textbox('getValue');			// 部門コード
			var txt_rankno			= $('#'+$.id_inp.txt_rankno).textbox('getValue');			// ランクNo.
			var rad_ptnnokbn		= $("input[name="+$.id.rad_ptnnokbn+"]:checked").val();		// パターンNo.区分
			var txt_sryptnno		= $('#'+$.id_inp.txt_sryptnno).textbox('getValue');			// 数量ﾊﾟﾀｰﾝ№
			var txt_rtptnno			= $('#'+$.id_inp.txt_rtptnno).textbox('getValue');			// 通常率ﾊﾟﾀｰﾝ№
			var txt_jrtptnno		= $('#'+$.id_inp.txt_jrtptnno).textbox('getValue');			// 実績率ﾊﾟﾀｰﾝ№

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
//			$.appendMaskMsg();

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,		// レポート名
					BMNCD:			txt_bmncd,		// 部門
					RANKNO:			txt_rankno,		// ランクNo.
					PTNNOKBN:		rad_ptnnokbn,	// パターンNo.区分
					SRYPTNNO:		txt_sryptnno,	// 数量ﾊﾟﾀｰﾝ№
					RTPTNNO:		txt_rtptnno,	// 通常率ﾊﾟﾀｰﾝ№
					JRTPTNNO:		txt_jrtptnno,	// 実績率ﾊﾟﾀｰﾝ№
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

//					// ログ出力
//					$.log(that.timeData, 'query:');
//
//					var opts = JSON.parse(json).opts
//
//					// 検索結果を保持
//					that.baseData = JSON.parse(json).rows;
//
//					// メインデータ表示
////					that.setData(that.baseData, opts);
////					that.queried = true;
//
//					// 状態保存
//					$.saveState2(reportno, that.getJSONString());

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
				id:		$.id_inp.txt_bmncd,
				value:	$('#'+$.id_inp.txt_bmncd).textbox('getValue'),
				text:	$('#'+$.id_inp.txt_bmncd).textbox('getText')
			});
			// ランクNo.
			this.jsonTemp.push({
				id:		$.id_inp.txt_rankno,
				value:	$('#'+$.id_inp.txt_rankno).textbox('getValue'),
				text:	$('#'+$.id_inp.txt_rankno).textbox('getText')
			});
			// パターンNo.区分
			this.jsonTemp.push({
				id:		'rad_ptnnokbn',
				value:	$("input[name="+'rad_ptnnokbn'+"]:checked").val(),
				text:	''
			});
			// 数量ﾊﾟﾀｰﾝ№
			this.jsonTemp.push({
				id:		$.id_inp.txt_sryptnno,
				value:	$('#'+$.id_inp.txt_sryptnno).textbox('getValue'),
				text:	$('#'+$.id_inp.txt_sryptnno).textbox('getText')
			});
			// 通常率ﾊﾟﾀｰﾝ№
			this.jsonTemp.push({
				id:		$.id_inp.txt_rtptnno,
				value:	$('#'+$.id_inp.txt_rtptnno).textbox('getValue'),
				text:	$('#'+$.id_inp.txt_rtptnno).textbox('getText')
			});
			// 総数量(通常率)
			this.jsonTemp.push({
				id:		$.id_inp.txt_rtsousu,
				value:	$('#'+$.id_inp.txt_rtsousu).textbox('getValue'),
				text:	$('#'+$.id_inp.txt_rtsousu).textbox('getText')
			});
			// 実績率ﾊﾟﾀｰﾝ№
			this.jsonTemp.push({
				id:		$.id_inp.txt_jrtptnno,
				value:	$('#'+$.id_inp.txt_jrtptnno).textbox('getValue'),
				text:	$('#'+$.id_inp.txt_jrtptnno).textbox('getText')
			});
			// 総数量(実績率)
			this.jsonTemp.push({
				id:		$.id_inp.txt_jrtsousu,
				value:	$('#'+$.id_inp.txt_jrtsousu).textbox('getValue'),
				text:	$('#'+$.id_inp.txt_jrtsousu).textbox('getText')
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

			// 遷移先から再び本画面に戻る為、疑似検索(validation処理)を行い入力値を検索条件として保持する。
			that.getEasyUI();
			that.validation();

			// 戻る実行時用に現在の画面情報を保持する
			var states = $.getBackBaseJSON(that);
			// 各種グリッド情報を設定
			var newrepinfos = $.getBackJSON(that, states);
			$.setJSONObject(sendJSON, 'repinfo', newrepinfos, '');

			// 呼出別処理
			switch (btnId) {
			case $.id.btn_tenbetusu:
				var txt_bmncd			= $.getInputboxValue($('#'+$.id_inp.txt_bmncd));				// 部門コード
				var txt_rankno			= $.getInputboxValue($('#'+$.id_inp.txt_rankno));				// ランクNo.
				var rad_ptnnokbn		= $("input[name="+$.id.rad_ptnnokbn+"]:checked").val();			// パターンNo.区分
				var txt_sryptnno		= $.getInputboxValue($('#'+$.id_inp.txt_sryptnno));				// 数量ﾊﾟﾀｰﾝ№
				var txt_rtptnno			= $.getInputboxValue($('#'+$.id_inp.txt_rtptnno));				// 通常率ﾊﾟﾀｰﾝ№
				var txt_rtsousu			= $.getInputboxValue($('#'+$.id_inp.txt_rtsousu));				// 総数量(通常率)
				var txt_jrtptnno		= $.getInputboxValue($('#'+$.id_inp.txt_jrtptnno));				// 実績率ﾊﾟﾀｰﾝ№
				var txt_jrtsousu		= $.getInputboxValue($('#'+$.id_inp.txt_jrtsousu));				// 総数量(実績率)

				var txt_jrtptnno = ( '000000000000' + txt_jrtptnno ).slice( -12 );

				// EasyUI のフォームメソッド 'validate' 実施
				var rt = $($.id.toolbarform).form('validate');
				if(!rt){
					$.messager.alert($.message.ID_MESSAGE_TITLE_WARN,'入力内容を確認してください。','warning');
					return false;
				}
//				if (!txt_bmncd) {
//					$.showMessage('EX1025', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_bmncd), true)});
//					return false;
//				}
//				if (!txt_rankno) {
//					$.showMessage('E20057');	// TODO ランクNoは必須入力です。
//					$.showMessage('EX1025', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_bmncd), true)});
//					return false;
//				}
				if (Number(txt_rankno) >= 900) {
					$.showMessage('EX1066', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_rankno), true)});
					return false;
				}
				var msgid = that.checkInputboxFunc($.id_inp.txt_rankno, txt_bmncd, txt_rankno, '');
				if(msgid !==null){
					$.showMessage(msgid, undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_rankno), true)});
					return false;
				}
				if (rad_ptnnokbn == "1") {
					// 数量パターンが選択された場合
					if (!txt_sryptnno) {
						$.showMessage('EX1067', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_sryptnno), true)});
						return false;
					}
					if (Number(txt_sryptnno) >= 900) {
						$.showMessage('E11035', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_sryptnno), true)});
						return false;
					}
					var msgid = that.checkInputboxFunc($.id_inp.txt_sryptnno, txt_bmncd, txt_sryptnno, '');
					if(msgid !==null){
						$.showMessage(msgid, undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_sryptnno), true)});
						return false;
					}
				} else if (rad_ptnnokbn == "2") {
					// 通常率パターンが選択された場合
					if (!txt_rtptnno) {
						$.showMessage('EX1069', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_rtptnno), true)});
						return false;
					}
					if (Number(txt_rtptnno) >= 900) {
						$.showMessage('E20101', ['通常率パターン'], function(){$.addErrState(that, $('#'+$.id_inp.txt_rtptnno), true)});
						return false;
					}
					var msgid = that.checkInputboxFunc($.id_inp.txt_rtptnno, txt_bmncd, txt_rtptnno, '');
					if(msgid !==null){
						$.showMessage(msgid, undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_rtptnno), true)});
						return false;
					}
					if (!txt_rtsousu) {
						$.showMessage('EX1072', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_rtsousu), true)});
						return false;
					}
				} else if (rad_ptnnokbn == "3") {
					// 実績率パターンが選択された場合
					if (!txt_jrtptnno) {
						$.showMessage('EX1071', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_jrtptnno), true)});
						return false;
					}
					var msgid = that.checkInputboxFunc($.id_inp.txt_jrtptnno, txt_bmncd, txt_jrtptnno, '');
					if(msgid !==null){
						$.showMessage(msgid, undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_jrtptnno), true)});
						return false;
					}
					if (!txt_jrtsousu) {
						$.showMessage('EX1072', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_jrtsousu), true)});
						return false;
					}
				}else{
					//$.showMessage('EX1072', undefined, function(){$.addErrState(that, $("input[name="+$.id.rad_ptnnokbn+"]:checked"), true)});
					$.showMessage('EX1119', ['数量パターン、通常率パターン、実績率パターンのいずれかを'], function(){$.addErrState(that, $('#'+$.id.rad_ptnnokbn), true)});

					return false;
				}
				// 転送先情報
				index = 10;		// RP009 店別数量展開
				childurl = href[index];
				sendMode = 1;

				// 数量ﾊﾟﾀｰﾝ№が選択された場合
				if (rad_ptnnokbn == "1") {
					var txt_ptnno = txt_sryptnno;														// 数量ﾊﾟﾀｰﾝ№
					var txt_sousu = "";
				// 通常率ﾊﾟﾀｰﾝ№が選択された場合
				} else if (rad_ptnnokbn == "2") {
					var txt_ptnno = txt_rtptnno;														// 通常率ﾊﾟﾀｰﾝ№
					var txt_sousu = txt_rtsousu;														// 総数量(通常率)
				// 実績率ﾊﾟﾀｰﾝ№が選択された場合
				} else if (rad_ptnnokbn == "3") {
					var txt_ptnno = txt_jrtptnno;														// 実績率ﾊﾟﾀｰﾝ№
					var txt_sousu = txt_jrtsousu;														// 総数量(実績率)
				}
				// オブジェクト作成
				$.setJSONObject(sendJSON, $.id_inp.txt_bmncd,txt_bmncd, txt_bmncd);						// 部門
				$.setJSONObject(sendJSON, $.id_inp.txt_rankno,txt_rankno, txt_rankno);					// ランクNo.
				$.setJSONObject(sendJSON, $.id.rad_ptnnokbn,rad_ptnnokbn, rad_ptnnokbn);				// パターンNo.区分
				$.setJSONObject(sendJSON, $.id_inp.txt_ptnno,txt_ptnno, txt_ptnno);						// ﾊﾟﾀｰﾝ№
				$.setJSONObject(sendJSON, $.id_inp.txt_sousu,txt_sousu, txt_sousu);						// 総数量

				// 入力値保持
				that.getEasyUI();
				var rt = true;
				// 入力エラーなしの場合に検索条件を格納
				if (rt == true) that.jsonString = that.jsonTemp.slice(0);
				// 入力チェック用の配列をクリア
				that.jsonTemp = [];
				// 状態保存
				$.saveState2(that.name, that.jsonString);
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
		// IDとvalueでチェック処理を実施
		checkInputboxFunc:function(id,param1, param2, kbn, record, isNew){
			var that = this;

			// ランクNo.
			if(id===$.id_inp.txt_rankno){
				var param = {};
				param["KEY"] =  "MST_CNT";
				param["value"] = param1 + "," + param2;
				var chk_cnt = $.getInputboxData(that.name, $.id.action_check, $.id_inp.txt_rankno, [param]);
				if(chk_cnt==="" || chk_cnt==="0"){
					return "E20057";
				}
			} else if(id===$.id_inp.txt_sryptnno){
				// 数量パターンNo.
				var param = {};
				param["KEY"] =  "MST_CNT";
				param["value"] = param1 + "," + param2;
				var chk_cnt = $.getInputboxData(that.name, $.id.action_check, $.id_inp.txt_sryptnno, [param]);
				if(chk_cnt==="" || chk_cnt==="0"){
					return "EX1079";
				}
			} else if(id===$.id_inp.txt_rtptnno){
				// 通常率パターンNo.
				var param = {};
				param["KEY"] =  "MST_CNT";
				param["value"] = param1 + "," + param2;
				var chk_cnt = $.getInputboxData(that.name, $.id.action_check, $.id_inp.txt_rtptnno, [param]);
				if(chk_cnt==="" || chk_cnt==="0"){
					return "EX1080";
				}
			} else if(id===$.id_inp.txt_jrtptnno){
				// 実績率パターンNo.
				var param = {};
				param["KEY"] =  "MST_CNT";
				param["value"] = param1 + "," + param2;
				var chk_cnt = $.getInputboxData(that.name, $.id.action_check, $.id_inp.txt_jrtptnno, [param]);
				if(chk_cnt==="" || chk_cnt==="0"){
					return "EX1081";
				}
			}
			return null;
		}
	} });
})(jQuery);