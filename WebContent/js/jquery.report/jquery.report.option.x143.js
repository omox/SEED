/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
			name:		'Out_Reportx143',			// （必須）レポートオプションの確認
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

				// 検索実行
				that.onChangeReport = true;

				var count = 2;
				// 名称マスタ参照系
				var meisyoSelect = Object.getOwnPropertyNames($.id_mei);
				for ( var sel in meisyoSelect ) {
					if($('#'+$.id_mei[meisyoSelect[sel]]).length > 0){
						$.setMeisyoCombo(that, reportno, $.id_mei[meisyoSelect[sel]], isUpdateReport);
						count++;
					}
				}
				// 入力テキストボックス系
				var inputbox = Object.getOwnPropertyNames($.id_inp);
				for ( var sel in inputbox ) {
					if($('#'+$.id_inp[inputbox[sel]]).length > 0){
						$.setInputbox(that, reportno, $.id_inp[inputbox[sel]], isUpdateReport);
						count++;
					}
				}

//				$('#'+$.id_inp.txt_tencd).attr('readonly', 'readonly')
//				$('#'+$.id_inp.txt_tencd).attr('tabindex', -1).textbox('disable');
//				$('#'+$.id_inp.txt_tenkyudt).attr('readonly', 'readonly')
//				$('#'+$.id_inp.txt_tenkyudt).attr('tabindex', -1).textbox('disable');
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

				$('#'+$.id.btn_cancel).on("click", $.pushChangeReport);
				that.judgeRepType.sei = true;
				that.judgeRepType.sei_upd = true;

				if (that.sendBtnid===$.id.btn_sel_refer) {
					//$('#'+$.id.btn_upd).hide();
					//$('#'+$.id.btn_del).hide();

					$('#'+$.id.btn_upd).linkbutton('disable');
					$('#'+$.id.btn_upd).attr('disabled', 'disabled').hide();
					$('#'+$.id.btn_del).linkbutton('disable');
					$('#'+$.id.btn_del).attr('disabled', 'disabled').hide();
					$('#'+$.id.btn_cancel).linkbutton('disable');
					$('#'+$.id.btn_cancel).attr('disabled', 'disabled').hide();
					$.initReportInfo("TP012", "店舗休日マスタ　参照", "参照");
				}else{
					$.initReportInfo("TP012", "店舗休日マスタ　変更", "変更");
//					$('#'+$.id_inp.txt_tencd).attr('readonly', 'readonly')
//					$('#'+$.id_inp.txt_tencd).attr('tabindex', -1).combobox('disable');
				}
				$('#'+$.id_inp.txt_tencd).attr('readonly', 'readonly')
				$('#'+$.id_inp.txt_tencd).attr('tabindex', -1).combobox('disable');
				$('#'+$.id_inp.txt_tenkyudt).attr('readonly', 'readonly')
				$('#'+$.id_inp.txt_tenkyudt).attr('tabindex', -1).combobox('disable');
				// 変更
				$($.id.hiddenChangedIdx).val('');
			},
			judgeRepType: {
				sei				: false,	// 正
				sei_new 		: false,	// 正 -新規
				sei_upd 		: false,	// 正 -更新
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
			endUpdate:function (){

				// レポート番号取得
				var reportno = $($.id.hidden_reportno).val();
				// レポート定義位置
				var reportNumber = $.getReportNumber(reportno);
				this.changeReport(reportNumber, 'btn_return')
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
				var txt_tenkyudt	= $('#'+$.id_inp.txt_tenkyudt).textbox('getValue');		// コピー元店コード
				var txt_tencd		= $('#'+$.id_inp.txt_tencd).textbox('getValue');		// コピー元店コード
				$.post(
						$.reg.jqgrid ,
						{
							report:			 that.name,		// レポート名
							SENDBTNID:		 that.sendBtnid,
							t:				 (new Date()).getTime(),
							TENCD:			 txt_tencd,
							TENKYUDT:		 txt_tenkyudt,
							sortable:		 sortable,
							sortName:		 that.sortName,
							sortOrder:		 that.sortOrder,
							rows:			 1	// 表示可能レコード数
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

							// 状態保存
							$.saveState2(reportno, that.getJSONString());

							$('#'+$.id_mei.kbn316).combo("textbox").focus();

							// ログ出力
							$.log(that.timeData, 'loaded:');
						}
				);
			},
			isEmptyVal: function (val, zeroEmpty){
				if(val === undefined){
					return true;
				}
				if(val.length === 0){
					return true;
				}
				if(zeroEmpty === true && val&""==="0"){
					return true;
				}
				return false;
			},
			getGridData: function ( target){
				var that = this;

				var data = {};

				// 基本情報
				if(target===undefined || target==="grd_data"){
					var targetData = [{}];
					$('#'+that.focusRootId).find('[col^=F]').each(function(){
						var col = $(this).attr('col');
						targetData[0][col] = $.getInputboxValue($(this));
					});
					data["grd_data"] = targetData;
				}
				return data;
			},
			updValidation: function (){	// （必須）批准
				var that = this;
				var isNew = that.judgeRepType.sei_new;
				// EasyUI のフォームメソッド 'validate' 実施
				var rt = $($.id.toolbarform).form('validate');
				if(!rt){
					$.showMessage('E00001');
					return rt;
				}

				return rt;
			},
			updSuccess: function(id){	// validation OK時 の update処理
				var that = this;

				// 基本情報
				var targetData = that.grd_data;

				// 変更行情報取得
				var changedIndex = $($.id.hiddenChangedIdx).val().split(",");

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
							action:			$.id.action_update,	// 実行処理情報
							obj:			id,								// 実行オブジェクト
							SENDBTNID:		that.sendBtnid,
//							IDX:			$($.id.hiddenChangedIdx).val(),	// 更新対象Index
							DATA:			JSON.stringify(targetDatas),	// 更新対象情報
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
				// EasyUI のフォームメソッド 'validate' 実施
				var gridData01 = that.getGridData( "grd_data");
				var inpdata = gridData01["grd_data"][0];
				var txt_futaiedaban = inpdata["F2"];

				// 新規(正) 1.3　取得された商品コードが商品マスタテーブルに存在する場合、エラー。
				var txt_shncd_chk = $.getInputboxData(that.name, $.id.action_check,  $.id_inp.txt_futaiedaban, [{KEY:"MST_CNT",value:txt_futaiedaban}]);
				if(txt_shncd_chk != "0"){
					$.showMessage('E00006');
					return false;
				}

				return rt;
			},
			delSuccess: function(id){
				var that = this;
				var is_warning = false;

				var targetDatas = [{}];
				$('#'+that.focusRootId).find('[col^=F]').each(function(){
					var forId = $(this).attr('col');
					targetDatas[0][forId] = $.getInputboxValue($(this));
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
				//日付
				this.jsonTemp.push({
					value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_tenkyudt),
					text:	''
				});
				//店コード
				this.jsonTemp.push({
					value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_tencd),
					text:	''
				});
				//店休フラグ
				this.jsonTemp.push({
					value:	$.getJSONValue(this.jsonHidden, $.id_mei.sel_tenkyuflg),
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
			getGridParams:function(that, id){
				var values = {};
				values["callpage"] = $($.id.hidden_reportno).val()										// 呼出元レポート名

				if(id.indexOf($.id.grd_tengp) > -1){
					var gpkbn = id.replace($.id.grd_tengp, "");
					values["GPKBN"] = gpkbn;
					values["AREAKBN"] = $("input[name="+$.id.rad_areakbn+gpkbn+"]:checked").val();
					values["BMNCD"] = $.getInputboxValue($('#'+$.id_inp.txt_bmncd));			// 部門
				}
				return [values];

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
				var sendJSON = [];
				$.setJSONObject(sendJSON, 'sendBtnid', btnId, $('#'+btnId).attr("title"));					// 実行ボタン情報保持
				$.setJSONObject(sendJSON, 'callpage', that.name, that.name);								// 呼出し元レポート情報

				// 戻る実行時用に現在の画面情報を保持する
				var states = $.getBackBaseJSON(that);
				// TODO 各種グリッド情報を設定
				var newrepinfos = $.getBackJSON(that, states);
				$.setJSONObject(sendJSON, 'repinfo', newrepinfos, '');

				// 呼出別処理
				switch (btnId) {
				case $.id.btn_new:
					sendMode = 2;
					// 転送先情報
					index = 2;
					childurl = href[index];

					// オブジェクト作成
					$.setJSONObject(sendJSON, $.id.txt_sel_bmncd,'', '');

					break;
				case $.id.btn_cancel:
					// 転送先情報
					index = 1;
					sendMode = 2;
					if(that.reportYobiInfo()==='1'){
						index = 4;
					}
					childurl = href[index];
					break;
				case "btn_return":
					// 転送先情報
					index = 1;
					sendMode = 2;
					if(that.reportYobiInfo()==='1'){
						index = 4;
					}
					childurl = href[index];
					break;
				case $.id.btn_upd:
					// 転送先情報
					sendMode = 2;
					index = 1;
					if(that.reportYobiInfo()==='1'){
						index = 4;
					}
					childurl = href[index];
					break;
				case $.id.btn_back:
					sendMode = 2;
					// 転送先情報
					index = 1;
					if(that.reportYobiInfo()==='1'){
						index = 4;
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
		} });
})(jQuery);