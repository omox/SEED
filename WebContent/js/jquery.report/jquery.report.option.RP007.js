/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_ReportRP007',			// （必須）レポートオプションの確認
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
		dedefaultObjNum:	11,	// 初期化オブジェクト数
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

			var isUpdateReport = true;

//			// 初期検索可能
//			that.onChangeReport = true;

//			// 名称マスタ参照系
//			var meisyoSelect = Object.getOwnPropertyNames($.id_mei);
//			for ( var sel in meisyoSelect ) {
//				if($('#'+$.id_mei[meisyoSelect[sel]]).length > 0){
//					$.setMeisyoCombo(that, reportno, $.id_mei[meisyoSelect[sel]], isUpdateReport);
//				}
//			}
			// 入力テキストボックス系
			var inputbox = Object.getOwnPropertyNames($.id_inp);
			for ( var sel in inputbox ) {
				if($('#'+$.id_inp[inputbox[sel]]).length > 0){
					$.setInputbox(that, reportno, $.id_inp[inputbox[sel]], isUpdateReport);
				}
			}
			// ラジオボタン系
			$.setRadioInit2(that.jsonHidden, $.id.rad_mstkbn, that);
			$.setRadioInit2(that.jsonHidden, $.id.rad_datakbn+"_1", that);
			$.setRadioInit2(that.jsonHidden, $.id.rad_datakbn+"_2", that);
			$.setRadioInit2(that.jsonHidden, $.id.rad_datakbn+"_3", that);

			// 検索実行
//			that.onChangeReport = true;

			// Load処理回避
			//$.tryChangeURL(null);

			// 初期化終了
			this.initializes =! this.initializes;
			var newval = $.getInputboxValue($('#'+$.id.rad_mstkbn));
			$.setInputboxValue($('#'+$.id.rad_mstkbn), newval);

//			// チェックボックスの設定
//			$.initCheckboxCss($("#"+that.focusRootId));
			// キーイベントの設定
			$.initKeyEvent(that);
			// ログ出力
			$.log(that.timeData, 'initialize:');

			that.queried = true;
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

			// 各種遷移ボタン
			$('#'+$.id.btn_cancel).on("click", $.pushChangeReport);

			$.initReportInfo("RP007", "ランク　店コピー");

			$('input[name="'+$.id.rad_mstkbn+'"]').change(function() {
				var rad_mstkbn = $.getInputboxValue($('#'+$.id.rad_mstkbn));
				var rad_datakbn1 = $("input[name="+$.id.rad_datakbn+"_1]");
				var rad_datakbn2 = $("input[name="+$.id.rad_datakbn+"_2]");
				var rad_datakbn3 = $("input[name="+$.id.rad_datakbn+"_3]");
				var txt_ranknost = $("#"+$.id_inp.txt_ranknost).textbox('textbox');
				var txt_ranknoed = $("#"+$.id_inp.txt_ranknoed).textbox('textbox');
				var txt_rtptnnost = $("#"+$.id_inp.txt_rtptnnost).textbox('textbox');
				var txt_rtptnnoed = $("#"+$.id_inp.txt_rtptnnoed).textbox('textbox');
				var txt_jrtptnnost = $("#"+$.id_inp.txt_jrtptnnost).textbox('textbox');
				var txt_jrtptnnoed = $("#"+$.id_inp.txt_jrtptnnoed).textbox('textbox');

				if(rad_mstkbn === '1'){
					that.setRadioBtnEnable(rad_datakbn1, 4);
					that.setRadioBtnDisable(rad_datakbn2);
					that.setRadioBtnDisable(rad_datakbn3);
					that.setInputTextEnable(txt_ranknost, 7);
					that.setInputTextEnable(txt_ranknoed, 8);
					that.setInputTextDisable(txt_rtptnnost);
					that.setInputTextDisable(txt_rtptnnoed);
					that.setInputTextDisable(txt_jrtptnnost);
					that.setInputTextDisable(txt_jrtptnnoed);

				}else if (rad_mstkbn === '2') {

					that.setRadioBtnEnable(rad_datakbn2, 9);
					that.setRadioBtnDisable(rad_datakbn1);
					that.setRadioBtnDisable(rad_datakbn3);
					that.setInputTextEnable(txt_rtptnnost, 12);
					that.setInputTextEnable(txt_rtptnnoed, 13);
					that.setInputTextDisable(txt_ranknost);
					that.setInputTextDisable(txt_ranknoed);
					that.setInputTextDisable(txt_jrtptnnost);
					that.setInputTextDisable(txt_jrtptnnoed);

				} else {
					that.setRadioBtnEnable(rad_datakbn3, 14);
					that.setRadioBtnDisable(rad_datakbn1);
					that.setRadioBtnDisable(rad_datakbn2);
					that.setInputTextEnable(txt_jrtptnnost, 17);
					that.setInputTextEnable(txt_jrtptnnoed, 18);
					that.setInputTextDisable(txt_ranknost);
					that.setInputTextDisable(txt_ranknoed);
					that.setInputTextDisable(txt_rtptnnost);
					that.setInputTextDisable(txt_rtptnnoed);
				}
			});

		},

		initCondition: function (){	// 条件初期値セット
			var that = this;
			// 初期化項目
		},
		clear:function(){
//			// 隠し情報初期化
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

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$.appendMaskMsg();

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,		// レポート名
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

					// メインデータ表示
//					that.setData(that.baseData, opts);
//					that.queried = true;

					// 状態保存
					$.saveState2(reportno, that.getJSONString());

					// ログ出力
					$.log(that.timeData, 'loaded:');
				}
			);
		},
		setGridData: function (data, target){
			var that = this;

			return true;
		},
		isEnpty: function (val){
			var that = this

			if(val && val !== ""){
				return false
			}else{
				return true
			}
		},
		updValidation: function (){	// （必須）批准
			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform).form('validate');
			if(!rt){
				$.addErrState(that, $('.validatebox-invalid').eq(0), false);
				return rt;
			}
//			if(!rt){
//				$.messager.alert($.message.ID_MESSAGE_TITLE_WARN,'入力内容を確認してください。','warning');
//				return rt;
//			}
			var rt = $($.id.toolbarform).form('validate');
			var txt_bmncd			= $('#'+$.id_inp.txt_bmncd).textbox('getValue');			// 部門
			var txt_cptotenno		= $('#'+$.id_inp.txt_cptotenno).textbox('getValue');		// コピー先店番
			var txt_cpfromtenno		= $('#'+$.id_inp.txt_cpfromtenno).textbox('getValue');		// コピー元店番
			var rad_mstkbn			= $("input[name="+$.id.rad_mstkbn+"]:checked").val();		// マスター区分
			var rad_datakbn			= $("input[name="+$.id.rad_datakbn+"]:checked").val();		// データ指定区分
			var txt_ranknost		= $('#'+$.id_inp.txt_ranknost).textbox('getValue');			// ランク№開始
			var txt_ranknoed		= $('#'+$.id_inp.txt_ranknoed).textbox('getValue');			// ランク№終了
			var txt_rtptnnost		= $('#'+$.id_inp.txt_rtptnnost).textbox('getValue');		// 通常率ﾊﾟﾀｰﾝ№開始
			var txt_rtptnnoed		= $('#'+$.id_inp.txt_rtptnnoed).textbox('getValue');		// 通常率ﾊﾟﾀｰﾝ№終了
			var txt_jrtptnnost		= $('#'+$.id_inp.txt_jrtptnnost).textbox('getValue');		// 実績率ﾊﾟﾀｰﾝ№開始
			var txt_jrtptnnoed		= $('#'+$.id_inp.txt_jrtptnnoed).textbox('getValue');		// 実績率ﾊﾟﾀｰﾝ№終了

			//必須入力チェック
			if(!rad_mstkbn || rad_mstkbn == ""){
				$.showMessage('EX1119', ['ランクマスター、通常率パターンマスタ―、実績率パターンマスタ―のいずれかを'], function(){$.addErrState(that, $('#'+$.id.rad_mstkbn), true)});
				return false;
			}else{
				var rad_datakbn = "";

				if(rad_mstkbn == "1"){
					rad_datakbn = $.getInputboxValue($("input[name="+$.id.rad_datakbn+"_1]"))
					if(rad_datakbn == "1"){
						if(that.isEnpty(txt_ranknost)){
							$.showMessage('EX1103', ['ランク№開始'], function(){$.addErrState(that, $('#'+$.id_inp.txt_ranknost), true)});
							return false;
						}

						if(that.isEnpty(txt_ranknoed)){
							$.showMessage('EX1103', ['ランク№終了'], function(){$.addErrState(that, $('#'+$.id_inp.txt_ranknoed), true)});
							return false;
						}

						if(Number(txt_ranknost) > Number(txt_ranknoed)){
							$.showMessage('E20105', ['ランク№'], function(){$.addErrState(that, $('#'+$.id_inp.txt_ranknost), true)});
							return false;
						}
					}
				}else if(rad_mstkbn == "2"){
					rad_datakbn = $.getInputboxValue($("input[name="+$.id.rad_datakbn+"_2]"))
					if(rad_datakbn == "1"){
						if(that.isEnpty(txt_rtptnnost)){
							$.showMessage('EX1103', ['通常率ﾊﾟﾀｰﾝ№開始'], function(){$.addErrState(that, $('#'+$.id_inp.txt_rtptnnost), true)});
							return false;
						}

						if(that.isEnpty(txt_rtptnnoed)){
							$.showMessage('EX1103', ['通常率ﾊﾟﾀｰﾝ№終了'], function(){$.addErrState(that, $('#'+$.id_inp.txt_rtptnnoed), true)});
							return false;
						}

						if(Number(txt_rtptnnost) > Number(txt_rtptnnoed)){
							$.showMessage('E20105', ['通常率ﾊﾟﾀｰﾝ№'], function(){$.addErrState(that, $('#'+$.id_inp.txt_ranknost), true)});
							return false;
						}
					}

				}else if(rad_mstkbn == "3"){
					rad_datakbn = $.getInputboxValue($("input[name="+$.id.rad_datakbn+"_3]"))
					if(rad_datakbn == "1"){
						if(that.isEnpty(txt_jrtptnnost)){
							$.showMessage('EX1103', ['実績率ﾊﾟﾀｰﾝ№開始'], function(){$.addErrState(that, $('#'+$.id_inp.txt_jrtptnnost), true)});
							return false;
						}

						if(that.isEnpty(txt_jrtptnnoed)){
							$.showMessage('EX1103', ['実績率ﾊﾟﾀｰﾝ№終了'], function(){$.addErrState(that, $('#'+$.id_inp.txt_jrtptnnoed), true)});
							return false;
						}

						if(Number(txt_jrtptnnost) > Number(txt_jrtptnnoed)){
							$.showMessage('EX1065', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_jrtptnnost), true)});
							return false;
						}
					}
				}
			}

			// 存在チェック：部門
			if (txt_bmncd) {
				var msgid = that.checkInputboxFunc($.id_inp.txt_bmncd, txt_bmncd , '');
				if(msgid !==null){
					$.showMessage(msgid, undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_bmncd), true)});
					rt = false;
				}
			}

			// 入力チェック
			if (rt) {
				if (txt_cptotenno == txt_cpfromtenno) {
					$.showMessage('E20107', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_cpfromtenno), true)});
					rt = false;
				}
			}
			if (rt) {
				var msgid = that.checkInputboxFunc($.id_inp.txt_tencd, txt_cptotenno , '');
				if(msgid !==null){
					$.showMessage(msgid, undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_cptotenno), true)});
					rt = false;
				}
			}
			if (rt) {
				var msgid = that.checkInputboxFunc($.id_inp.txt_tencd, txt_cpfromtenno , '');
				if(msgid !==null){
					$.showMessage(msgid, undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_cpfromtenno), true)});
					rt = false;
				}
			}
			// ランクマスターが選択された場合
			if (rad_mstkbn == "1" && rad_datakbn == "1") {
				if (rt) {
					if (!txt_ranknost) {
						$.showMessage('EX1057', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_ranknost), true)});
						rt = false;
					}
				}
				if (rt) {
					if (!txt_ranknoed) {
						$.showMessage('EX1058', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_ranknoed), true)});
						rt = false;
					}
				}
				if (rt) {
					if (txt_ranknost > txt_ranknoed) {
						$.showMessage('EX1059', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_ranknoed), true)});
						rt = false;
					}
				}
			}
			// 通常率パターンマスターが選択された場合
			if (rad_mstkbn == "2" && rad_datakbn == "1") {
				if (rt) {
					if (!txt_rtptnnost) {
						$.showMessage('EX1060', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_rtptnnost), true)});
						rt = false;
					}
				}
				if (rt) {
					if (!txt_rtptnnoed) {
						$.showMessage('EX1061', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_rtptnnoed), true)});
						rt = false;
					}
				}
				if (rt) {
					if (txt_rtptnnost > txt_rtptnnoed) {
						$.showMessage('EX1062', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_rtptnnoed), true)});
						rt = false;
					}
				}
			}
			// 実績率パターンマスターが選択された場合
			if (rad_mstkbn == "3" && rad_datakbn == "1") {
				if (rt) {
					if (!txt_jrtptnnost) {
						$.showMessage('EX1063', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_jrtptnnost), true)});
						rt = false;
					}
				}
				if (rt) {
					if (!txt_jrtptnnoed) {
						$.showMessage('EX1064', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_jrtptnnoed), true)});
						return false;
					}
				}
				if (rt) {
					if (txt_jrtptnnost > txt_jrtptnnoed) {
						$.showMessage('EX1065', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_jrtptnnoed), true)});
						return false;
					}
				}
			}
			return rt;
		},
		updSuccess: function(id){	// validation OK時 の update処理
			var that = this;

			var rad_mstkbn = $.getInputboxValue($('#'+$.id.rad_mstkbn));
			var rad_datakbn = ""

			if(rad_mstkbn == "1"){
				rad_datakbn = $.getInputboxValue($("input[name="+$.id.rad_datakbn+"_1]"));
			}else if(rad_mstkbn == "2"){
				rad_datakbn = $.getInputboxValue($("input[name="+$.id.rad_datakbn+"_2]"));
			}else if(rad_mstkbn == "3"){
				rad_datakbn = $.getInputboxValue($("input[name="+$.id.rad_datakbn+"_3]"));
			}

			var targetDatas = [{}];
			$('#'+that.focusRootId).find('[col^=F]').each(function(){
				var col = $(this).attr('col');
				targetDatas[0][col] = $.getInputboxValue($(this));
			});
			targetDatas[0]["F5"] = rad_datakbn

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$.appendMaskMsg();

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,							// レポート名
					action:			$.id.action_update,					// 実行処理情報
					obj:			id,									// 実行オブジェクト
					DATA:			JSON.stringify(targetDatas),		// 更新対象情報
					t:				(new Date()).getTime()
				},
				function(data){
					// 検索処理エラー判定
					if($.updError(id, data)) return false;

					var afterFunc = function(){
						// 初期化
						that.clear();
						that.changeReport(that.name, 'btn_return');
						//that.changeReport(that.name, $.id.btn_upd);
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
			// 部門
			this.jsonTemp.push({
				id:		$.id_inp.txt_bmncd,
				value:	$('#'+$.id_inp.txt_bmncd).textbox('getValue'),
				text:	$('#'+$.id_inp.txt_bmncd).textbox('getText')
			});
			// コピー先店番
			this.jsonTemp.push({
				id:		$.id_inp.txt_cptotenno,
				value:	$('#'+$.id_inp.txt_cptotenno).textbox('getValue'),
				text:	$('#'+$.id_inp.txt_cptotenno).textbox('getText')
			});
			// コピー元店番
			this.jsonTemp.push({
				id:		$.id_inp.txt_cpfromtenno,
				value:	$('#'+$.id_inp.txt_cpfromtenno).textbox('getValue'),
				text:	$('#'+$.id_inp.txt_cpfromtenno).textbox('getText')
			});
			// マスター区分
			this.jsonTemp.push({
				id:		'rad_mstkbn',
				value:	$("input[name="+'rad_mstkbn'+"]:checked").val(),
				text:	''
			});
			// データ指定区分
			this.jsonTemp.push({
				id:		'rad_datakbn',
				value:	$("input[name="+'rad_datakbn'+"]:checked").val(),
				text:	''
			});
			// ランク№開始
			this.jsonTemp.push({
				id:		$.id_inp.txt_ranknost,
				value:	$('#'+$.id_inp.txt_ranknost).textbox('getValue'),
				text:	$('#'+$.id_inp.txt_ranknost).textbox('getText')
			});
			// ランク№終了
			this.jsonTemp.push({
				id:		$.id_inp.txt_ranknoed,
				value:	$('#'+$.id_inp.txt_ranknoed).textbox('getValue'),
				text:	$('#'+$.id_inp.txt_ranknoed).textbox('getText')
			});
			// 通常率ﾊﾟﾀｰﾝ№開始
			this.jsonTemp.push({
				id:		$.id_inp.txt_rtptnnost,
				value:	$('#'+$.id_inp.txt_rtptnnost).textbox('getValue'),
				text:	$('#'+$.id_inp.txt_rtptnnost).textbox('getText')
			});
			// 通常率ﾊﾟﾀｰﾝ№終了
			this.jsonTemp.push({
				id:		$.id_inp.txt_rtptnnoed,
				value:	$('#'+$.id_inp.txt_rtptnnoed).textbox('getValue'),
				text:	$('#'+$.id_inp.txt_rtptnnoed).textbox('getText')
			});
			// 実績率ﾊﾟﾀｰﾝ№開始
			this.jsonTemp.push({
				id:		$.id_inp.txt_jrtptnnost,
				value:	$('#'+$.id_inp.txt_jrtptnnost).textbox('getValue'),
				text:	$('#'+$.id_inp.txt_jrtptnnost).textbox('getText')
			});
			// 実績率ﾊﾟﾀｰﾝ№終了
			this.jsonTemp.push({
				id:		$.id_inp.txt_jrtptnnoed,
				value:	$('#'+$.id_inp.txt_jrtptnnoed).textbox('getValue'),
				text:	$('#'+$.id_inp.txt_jrtptnnoed).textbox('getText')
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

			// 呼出別処理
			switch (btnId) {
			case $.id.btn_back:
			case $.id.btn_cancel:
			case "btn_return":
				// 転送先情報
				childurl = parent.$('#hdn_menu_path').val();
				break
//			case $.id.btn_cancel:
//				// 元画面情報
//				var callpage = $.getJSONValue(that.jsonHidden, "callpage");
//				// 転送先情報
//				index = 1;
//				if(that.reportYobiInfo()==='1'){
//					index = 2;
//				}
//				childurl = href[index];
//				break;
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
		setInputBoxDisable:function(target, isTemporary){

			$('input[name="'+target.attr('name')+'"]').attr('disabled', 'disabled');

		},
		setRadioBtnDisable:function(target){
			$('input[name="'+target.attr('name')+'"]').attr('disabled', 'disabled');
			target.attr('readonly', 'readonly');
			target.attr('disabled', 'disabled');
		},
		setRadioBtnEnable:function(target, tabindex){
			$('input[name="'+target.attr('name')+'"]').removeAttr('disabled');
			target.removeAttr('readonly');
			target.removeAttr('disabled');
			target.attr('tabindex', tabindex);
		},
		setInputTextDisable:function(target){
			target.attr('tabindex', -1);
			target.attr('readonly', 'readonly');
			target.attr('disabled', 'disabled')
		},
		setInputTextEnable:function(target, tabindex){
			target.attr('readonly',false);
			target.attr('tabindex', tabindex);
			target.removeAttr('disabled')
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
				var msgid = null;

				/*// 商品コード
				if(id===$.id_inp.txt_tencd){
					msgid = that.checkInputboxFunc(id, newValue, '');
				}

				// 商品コード
				if(id===$.id_inp.txt_shncd){
					msgid = that.checkInputboxFunc(id, newValue, '');
				}*/
				if(id===$.id.rad_mstkbn){
					if(newValue == "1"){
						$("input[name="+$.id.rad_datakbn+"_1]")[0].checked = true ;
						$("input[name="+$.id.rad_datakbn+"_1]").parent()[0].className = 'rad_lbl selected_radio'
					}else{
						$("input[name="+$.id.rad_datakbn+"_1]")[0].checked = false ;
						$("input[name="+$.id.rad_datakbn+"_1]")[1].checked = false ;
						$("input[name="+$.id.rad_datakbn+"_1]").parent()[0].className = 'rad_lbl'
						$("input[name="+$.id.rad_datakbn+"_1]").parent()[1].className = 'rad_lbl'
					}

					if(newValue == "2"){
						$("input[name="+$.id.rad_datakbn+"_2]")[0].checked = true ;
						$("input[name="+$.id.rad_datakbn+"_2]").parent()[0].className = 'rad_lbl selected_radio'
					}else{
						$("input[name="+$.id.rad_datakbn+"_2]")[0].checked = false ;
						$("input[name="+$.id.rad_datakbn+"_2]")[1].checked = false ;
						$("input[name="+$.id.rad_datakbn+"_2]").parent()[0].className = 'rad_lbl'
						$("input[name="+$.id.rad_datakbn+"_2]").parent()[1].className = 'rad_lbl'
					}

					if(newValue == "3"){
						$("input[name="+$.id.rad_datakbn+"_3]")[0].checked = true ;
						$("input[name="+$.id.rad_datakbn+"_3]").parent()[0].className = 'rad_lbl selected_radio'
					}else{
						$("input[name="+$.id.rad_datakbn+"_3]")[0].checked = false ;
						$("input[name="+$.id.rad_datakbn+"_3]")[1].checked = false ;
						$("input[name="+$.id.rad_datakbn+"_3]").parent()[0].className = 'rad_lbl'
						$("input[name="+$.id.rad_datakbn+"_3]").parent()[1].className = 'rad_lbl'
					}

					/*if(newValue == "1"){
						$("input[name="+$.id.rad_datakbn+"_1]")[0].checked = true ;
					}else if(newValue == "2"){
						$("input[name="+$.id.rad_datakbn+"_2]")[0].checked = true ;
					}else if(newValue == "3"){
						$("input[name="+$.id.rad_datakbn+"_3]")[0].checked = true ;
					}*/
				}

				if(id===$.id.rad_datakbn + '_1'){
					var target	 = $("#"+$.id_inp.txt_ranknost);
					var target2	 = $("#"+$.id_inp.txt_ranknoed);
					if(newValue == "1"){
						target.numberbox('textbox').validatebox('options').required = true;
					}else{
						target.numberbox('textbox').validatebox('options').required = false;
					}
				}




				if(msgid !==null){
					$.showMessage(msgid, undefined, func_focus );
					return false;
				}
			}
		},
		// IDとvalueでチェック処理を実施
		checkInputboxFunc:function(id, newValue, kbn, record, isNew){
			var that = this;

			// 店コード
			if(id===$.id_inp.txt_tencd){
				// 範囲チェック
				if(Number(newValue) < 1 || Number(newValue) > 400){
					return "E20520";
				}

				// 存在チェック
				var param = {};
				param["KEY"] =  "MST_CNT";
				param["value"] = newValue;
				var chk_cnt = $.getInputboxData(that.name, $.id.action_check, $.id_inp.txt_tencd, [param]);
				if(chk_cnt==="" || chk_cnt==="0"){
					return "EX1077";
				}
			}

			// 部門コード
			if(id===$.id_inp.txt_bmncd){
				var param = {};
				param["KEY"] =  "MST_CNT";
				param["value"] = newValue;
				var chk_cnt = $.getInputboxData(that.name, $.id.action_check, $.id_inp.txt_bmncd, [param]);
				if(chk_cnt==="" || chk_cnt==="0"){
					return "E11044";
				}
			}
			return null;
		},
		// ラジオボタン初期化：検索画面用
		setRadioInit : function(jsonHidden, id, that) {
			// Radio 要素の初期化
			// 初期化情報取得
			var json = $.getJSONObject(jsonHidden, id);
			if (json){
				// 初期化
				$('input[name="'+id+'"]').val([json.value]);
			}
			if(that){
				$('input[name="'+id+'"]').change(function() {
					$.removeErrState();
				});
				if ($.inArray(id, that.initedObject) < 0){
					that.initedObject.push(id);
				}
			}
		},
	} });
})(jQuery);