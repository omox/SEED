/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_Reportx112',			// （必須）レポートオプションの確認
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
		dedefaultObjNum:	111,	// 初期化オブジェクト数
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

			// 検索実行
			that.onChangeReport = true;

			var isUpdateReport = true;

			// 名称マスタ参照系
			var meisyoSelect = Object.getOwnPropertyNames($.id_mei);
			for ( var sel in meisyoSelect ) {
				if($('#'+$.id_mei[meisyoSelect[sel]]).length > 0){
					$.setMeisyoCombo(that, reportno, $.id_mei[meisyoSelect[sel]], isUpdateReport);
				}
			}
			// 入力テキストボックス系
			var inputbox = Object.getOwnPropertyNames($.id_inp);
			for ( var sel in inputbox ) {
				if($('#'+$.id_inp[inputbox[sel]]).length > 0){
					$.setInputbox(that, reportno, $.id_inp[inputbox[sel]], isUpdateReport);
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

			// 各種遷移ボタン
			$('#'+$.id.btn_cancel).on("click", $.pushChangeReport);

			if(that.sendBtnid===$.id.btn_new){
				$.initReportInfo("TP002", "店舗基本マスタ　新規登録", "新規");

			}else if(that.sendBtnid===$.id.btn_sel_change){
				$.setInputBoxDisable($("#"+$.id_inp.txt_tencd));
				$.initReportInfo("TP002", "店舗基本マスタ　変更", "変更");

			}else {
				$("#"+$.id.btn_cancel).linkbutton('disable');
				$("#"+$.id.btn_cancel).attr('tabindex', -1).hide();
				$("#"+$.id.btn_upd).linkbutton('disable');
				$("#"+$.id.btn_upd).attr('tabindex', -1).hide();

				$.setInputBoxDisable($("#"+$.id_inp.txt_tencd));
				$.initReportInfo("TP002", "店舗基本マスタ　参照", "参照");

				$.setInputBoxDisable($($.id.hiddenChangedIdx));
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
			var txt_tencd		= $.getJSONObject(this.jsonString, $.id_inp.txt_tencd).value;		// 店コード

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$.appendMaskMsg();

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,		// レポート名
					TENCD:			txt_tencd,
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
					that.setData(that.baseData, opts);
					that.queried = true;
					//if(that.baseData.length > 0){
						if (that.baseData[0]["F115"]==="") {
							$.setInputBoxDisable($("#"+$.id_inp.txt_tenant1_syamei));
						}
						if (that.baseData[0]["F116"]==="") {
							$.setInputBoxDisable($("#"+$.id_inp.txt_tenant2_syamei));
						}
						if (that.baseData[0]["F117"]==="") {
							$.setInputBoxDisable($("#"+$.id_inp.txt_tenant3_syamei));
						}
						if (that.baseData[0]["F118"]==="") {
							$.setInputBoxDisable($("#"+$.id_inp.txt_tenant4_syamei));
						}
						if (that.baseData[0]["F119"]==="") {
							$.setInputBoxDisable($("#"+$.id_inp.txt_tenant5_syamei));
						}
						if (that.baseData[0]["F120"]==="") {
							$.setInputBoxDisable($("#"+$.id_inp.txt_tenant6_syamei));
						}
						if (that.baseData[0]["F121"]==="") {
							$.setInputBoxDisable($("#"+$.id_inp.txt_tenant7_syamei));
						}
						if (that.baseData[0]["F122"]==="") {
							$.setInputBoxDisable($("#"+$.id_inp.txt_tenant8_syamei));
						}
						if (that.baseData[0]["F123"]==="") {
							$.setInputBoxDisable($("#"+$.id_inp.txt_tenant9_syamei));
						}
						if (that.baseData[0]["F124"]==="") {
							$.setInputBoxDisable($("#"+$.id_inp.txt_tenant10_syamei));
						}
						if (that.baseData[0]["F125"]==="") {
							$.setInputBoxDisable($("#"+$.id_inp.txt_tenant11_syamei));
						}
						if (that.baseData[0]["F126"]==="") {
							$.setInputBoxDisable($("#"+$.id_inp.txt_tenant12_syamei));
						}
						if (that.baseData[0]["F127"]==="") {
							$.setInputBoxDisable($("#"+$.id_inp.txt_tenant13_syamei));
						}
						if (that.baseData[0]["F128"]==="") {
							$.setInputBoxDisable($("#"+$.id_inp.txt_tenant14_syamei));
						}
						if (that.baseData[0]["F129"]==="") {
							$.setInputBoxDisable($("#"+$.id_inp.txt_tenant15_syamei));
						}
						if (that.baseData[0]["F130"]==="") {
							$.setInputBoxDisable($("#"+$.id_inp.txt_tenant16_syamei));
						}
						if (that.baseData[0]["F131"]==="") {
							$.setInputBoxDisable($("#"+$.id_inp.txt_tenant17_syamei));
						}
						if (that.baseData[0]["F132"]==="") {
							$.setInputBoxDisable($("#"+$.id_inp.txt_tenant18_syamei));
						}
						if (that.baseData[0]["F133"]==="") {
							$.setInputBoxDisable($("#"+$.id_inp.txt_tenant19_syamei));
						}
						if (that.baseData[0]["F134"]==="") {
							$.setInputBoxDisable($("#"+$.id_inp.txt_tenant20_syamei));
						}
					//}
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
		updValidation: function (){	// （必須）批准
			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform).form('validate');
			if(!rt){
				return rt;
			}

			that.updData = [];
			var inpData = {};

			$('#'+that.focusRootId).find('[col^=F]').each(function(){
				var col = $(this).attr('col');
				inpData[col] = $.getInputboxValue($(this));
			});

			// 開設日、閉鎖日チェック
			if((inpData['F5']+'') !== '' && (inpData['F6']+'') !== '' && inpData['F5']*1 > inpData['F6']*1){
				$.showMessage('E11240');
				return false;
			}

			if(rt){
				that.updData.push(inpData);
			}

			return rt;
		},
		updSuccess: function(id){	// validation OK時 の update処理
			var that = this;

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
					SENDBTNID:		that.sendBtnid,
					DATA:			JSON.stringify(that.updData),		// 更新対象情報
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
			// 店コード
			this.jsonTemp.push({
				id:		$.id_inp.txt_tencd,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_tencd),
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
			// 戻る実行時用に現在の画面情報を保持する
			var states = $.getBackBaseJSON(that);
			// TODO 各種グリッド情報を設定
			var newrepinfos = $.getBackJSON(that, states);
			$.setJSONObject(sendJSON, 'repinfo', newrepinfos, '');
			$.setJSONObject(sendJSON, 'sendBtnid', btnId, $('#'+btnId).attr("title"));					// 実行ボタン情報保持
			$.setJSONObject(sendJSON, 'callpage', that.name, that.name);								// 呼出し元レポート情報

			// 呼出別処理
			switch (btnId) {
			case $.id.btn_back:
			case $.id.btn_cancel:
			case $.id.btn_upd:
				sendMode = 2;
				// 元画面情報
				var callpage = $.getJSONValue(that.jsonHidden, "callpage");
				// 転送先情報
				index = 1;
				if(that.reportYobiInfo()==='1'){
					index = 2;
				}
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

		}
	} });
})(jQuery);