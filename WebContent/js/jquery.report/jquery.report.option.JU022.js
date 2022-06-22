/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
			name:		'Out_ReportJU022',			// （必須）レポートオプションの確認
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
				this.setMoyscd(that, reportno, $.id.sel_moyscd, that);
				$.setInputbox(that, reportno, 'kikan_dummy', isUpdateReport);

				// 処理日付取得
				that.getsetInputboxData(reportno, $.id.txt_shoridt, [{}], $.id.action_init);
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

				if (that.sendBtnid===$.id.btn_new) {
					$('#'+$.id.btn_del).linkbutton('disable').attr('disabled', 'disabled').hide();
					$.initReportInfo("JU022", "店舗アンケート付き送付け 新規 催し", "新規");
				}else{
					$.initReportInfo("JU023", "店舗アンケート付き送付け 変更 催し", "変更");
					$('#'+$.id.sel_moyscd ).attr('readonly', 'readonly')
					$('#'+$.id.sel_moyscd ).attr('tabindex', -1).combobox('disable');

				}
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
				var moyscd = "";
				var moysstdt = "";
				var moysrban = "";

				if (that.sendBtnid!==$.id.btn_new) {
					var txt_moyscd	= $.getJSONObject(this.jsonString, $.id.sel_moyscd).value;	// 催しコード
					moyscd = txt_moyscd.split("-")[0];
					moysstdt = txt_moyscd.split("-")[1];
					moysrban = txt_moyscd.split("-")[2];
				}

				// 検索実行
				// 処理時間計測用
				that.timeData = (new Date()).getTime();
				// Loading表示
				$.appendMaskMsg();

				$.post(
						$.reg.jqgrid ,
						{
							report:			 that.name,		// レポート名
							MOYSKBN:		 moyscd,		// 催し区分
							MOYSSTDT:		 moysstdt,		// 催し開始日
							MOYSRBAN:		 moysrban,		// 催し連番
							SENDBTNID:		 that.sendBtnid,
							t:				 (new Date()).getTime(),
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
					$.addErrState(that, $('.validatebox-invalid').eq(0), false);
					return rt;
				}
				return rt;
			},
			updSuccess: function(id){	// validation OK時 の update処理
				var that = this;

				var targetDatas = [{}];
				$('#'+that.focusRootId).find('[col^=F]').each(function(){
					var col = $(this).attr('col');
					targetDatas[0][col] = $.getInputboxValue($(this));
				});

				var txt_moyscd	= $.getInputboxValue($('#'+$.id.sel_moyscd));	// 催しコード
				var shoriDt		= $.getInputboxValue($('#'+$.id.txt_shoridt));

				// 処理時間計測用
				that.timeData = (new Date()).getTime();
				// Loading表示
				$.appendMaskMsg();

				// 検索処理エラー判定
				$.post(
						$.reg.jqgrid ,
						{
							report:			that.name,		// レポート名
							action:			$.id.action_update,	// 実行処理情報
							obj:			id,								// 実行オブジェクト
							SENDBTNID:		that.sendBtnid,
//							IDX:			$($.id.hiddenChangedIdx).val(),	// 更新対象Index
							DATA:			JSON.stringify(targetDatas),	// 更新対象情報
							MOYSKBN:		txt_moyscd.split("-")[0],		// 催し区分
							MOYSSTDT:		txt_moyscd.split("-")[1],		// 催し開始日
							MOYSRBAN:		txt_moyscd.split("-")[2],		// 催し連番
							SHORIDATE:		shoriDt,						// 処理日付
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
			delValidation: function (){	// （必須）批准
				var that = this;
				// EasyUI のフォームメソッド 'validate' 実施
				var rt = true;

				return rt;
			},
			delSuccess: function(id){
				var that = this;

				var targetDatas = [{}];
				$('#'+that.focusRootId).find('[col^=F]').each(function(){
					var forId = $(this).attr('col');
					targetDatas[0][forId] = $.getInputboxValue($(this));
				});

				var txt_moyscd	= $.getInputboxValue($('#'+$.id.sel_moyscd));	// 催しコード

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
							MOYSKBN:		txt_moyscd.split("-")[0],		// 催し区分
							MOYSSTDT:		txt_moyscd.split("-")[1],		// 催し開始日
							MOYSRBAN:		txt_moyscd.split("-")[2],		// 催し連番
							SENDBTNID:		$.id.btn_del,
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
				//催しコード
				this.jsonTemp.push({
					id:		$.id.sel_moyscd,
					value:	$.getJSONValue(this.jsonHidden, $.id.sel_moyscd),
					text:	''
				});

			},
			setData: function(rows, opts){		// データ表示
				var that = this;

				if(rows.length > 0){
					$('#'+that.focusRootId).find('[col^=F]').each(function(){
						var col = $(this).attr('col');
						if(rows[0][col] && col!=="F1"){
							$.setInputboxValue($(this), rows[0][col]);
						}
					});
				}
			},
			// パラメータを元にDBに問い合わせた結果を取得、画面上に設定する
			getsetInputboxData: function(reportno, id, param, action){
				var that = this
				if(action===undefined) action = $.id.action_change;
				idx = -1;
				// 情報設定
				$.ajax({
					url: $.reg.easy,
					type: 'POST',
					async: false,
					data: {
						page	: reportno,
						obj		: id,
						sel		: (new Date()).getTime(),
						target	: id,
						action	: action,
						json	: JSON.stringify(param)
					},
					success: function(json){
						var value = "";
						if(json !=="" &&  JSON.parse(json).rows.length > 0){
							value = JSON.parse(json).rows[0].VALUE;
						}
						$.setInputboxValue($('#'+id), value);
					}
				});
				idx = 1;
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
			// コンボボックス(特にid_mei宣言の入力項目)を共通で設定する
			setMoyscd: function(that, reportno, id, isUpdateReport){
				var idx = -1;
				if($('#'+id).is(".easyui-combobox_")){
					$('#'+id).removeClass("easyui-combobox_").addClass("easyui-combobox");
				}
				var moyo = $.getJSONValue(that.jsonHidden, $.id.sel_moyscd);
				var tag_options = $('#'+id).attr('data-options');
				if(tag_options){
					tag_options = '\"'+tag_options.replace(/'/g, '').replace(/,/g, '\",\"').replace(/([a-zA-Z0-9]+):/g, '$1\":\"')+'\"';
				}
				var options = tag_options ? JSON.parse('{'+tag_options+'}'): JSON.parse('{}');

				var required = options && options.required;
				var topBlank = !required;
				var panelWidth = options && options.panelWidth ? options.panelWidth : null;
				var panelHeight = options && options.panelHeight ? options.panelHeight :'auto';
				var suffix = that.suffix ? that.suffix : '';
				var changeFunc1 = null;
				if(isUpdateReport){
					changeFunc1 = function(){
						if(idx > 0 && that.queried){
							$($.id.hiddenChangedIdx+suffix).val("1");
						}
					};
				}
				// 当項目を変更した際に、値をセットする項目がある場合の処理（セット先の項目は、HTMLタグとして、for_inp=当項目のID_列名）
				var changeFunc2 = null;
				if($.isFunction(that.changeInputboxFunc)){
					changeFunc2 = function(newValue, obj){
						that.changeInputboxFunc(that, id, newValue, obj);
					};
				}else{
					if($('[for_inp^='+id+'_]').length > 0){
						changeFunc2 = function(newValue){
							var param = [{"value":newValue}];
							$.getsetInputboxRowData(reportno, 'for_inp', id, param, that);
						};
					}
				}

				// フォーカスアウトのタイミングの動作
				$('#'+id).next().on('focusout', function(e){
					var obj = $(this).prev();

					if (!$.setComboReload(obj,false)) {
						obj.combobox('reload');
					}
				});

				$('#'+id).combobox({
					url:$.reg.easy,
					required: required,
					editable: true,
					autoRowHeight:false,
					panelWidth:panelWidth,
					panelHeight:panelHeight,
					valueField:'VALUE',
					textField:'TEXT',
					multiple :false,
					prompt: '',
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
						// 情報設定
						var json = [{
							DUMMY:  	moyo		//アンケート種類
						}];
						if(topBlank){json[0]['TOPBLANK'] = topBlank;}

						param.page		=	reportno;
						param.obj		=	id.replace(suffix, "");
						param.sel		=	(new Date()).getTime();
						param.target	=	id;
						param.action	=	$.id.action_init;
						param.json		=	JSON.stringify(json);
						param.datatype	=	"combobox";
					},
					onLoadSuccess:function(data){
						// 初期化
						var val = null;
						if (that.initedObject && $.inArray(id, that.initedObject) < 0){
							that.initedObject.push(id);
							var init = $.getJSONValue(that.jsonHidden, id);
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
						if(suffix===''){
							if(isUpdateReport){
								// 初期表示処理
								$.initialDisplay(that);
							}else{
//								// 検索ボタン有効化
//								$.setButtonState('#'+$.id.btn_search, true, id);
								// 初期表示検索処理
								$.initialSearch(that);
							}
						}
					},
					onChange:function(newValue, oldValue, obj){
						if(obj===undefined){obj = $(this);}

						if(changeFunc1!==null){ changeFunc1();}
						if(changeFunc2!==null){ changeFunc2(newValue, obj);}

						that.getComboErr(obj,false,newValue,oldValue);
					}
				});
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
				$.setJSONObject(sendJSON, 'reportYobi1', that.reportYobiInfo(), that.reportYobiInfo());		// 参照情報保持
				$.setJSONObject(sendJSON, 'callpage', that.name, that.name);								// 呼出し元レポート情報

				// 戻る実行時用に現在の画面情報を保持する
				var states = $.getBackBaseJSON(that);
				// 各種グリッド情報を設定
				var newrepinfos = $.getBackJSON(that, states);
				$.setJSONObject(sendJSON, 'repinfo', newrepinfos, '');

				// 呼出別処理
				switch (btnId) {
				case "btn_return":

					// 転送先情報
					if (that.sendBtnid===$.id.btn_new) {
						index = 5;
						sendMode = 1;
					} else {
						index = 1;
						sendMode = 2;
					}

					childurl = href[index];
					var txt_moyscd		= $.getInputboxValue($('#'+$.id.sel_moyscd));		// 催しコード
					var txt_moykn		= $.getInputboxValue($('#'+$.id_inp.txt_moykn));	// 催し名（漢字）
					var kikan_dummy		= $.getInputboxValue($('#'+'kikan_dummy'));			// 納入期間
					var txt_qasmdt		= $.getInputboxText($('#'+$.id_inp.txt_qasmdt));	// 店舗締切日
					var nnstdt			= $.getInputboxValue($('#'+$.id_inp.txt_nnstdt));
					var nneddt			= $.getInputboxValue($('#'+$.id_inp.txt_nneddt));
					$.setJSONObject(sendJSON, $.id_inp.txt_moyscd,txt_moyscd, txt_moyscd);
					$.setJSONObject(sendJSON, $.id_inp.txt_moykn,txt_moykn, txt_moykn);
					$.setJSONObject(sendJSON, 'kikan_dummy',kikan_dummy, kikan_dummy);
					$.setJSONObject(sendJSON, $.id_inp.txt_qasmdt,txt_qasmdt, txt_qasmdt);
					$.setJSONObject(sendJSON, $.id_inp.txt_nnstdt, nnstdt, nnstdt);
					$.setJSONObject(sendJSON, $.id_inp.txt_nneddt, nneddt, nneddt);
					break;
				case $.id.btn_cancel:
				case $.id.btn_back:
					// 転送先情報
					index = 1;
					if(that.reportYobiInfo()==='1'){
						index = 4;
					}
					sendMode = 2;
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
			changeInputboxFunc:function(that, id, newValue, obj){
				var that = this;

				var parentObj = $('#'+that.focusRootId);
				var sel_moyscd = $.getInputboxValue($('#'+$.id.sel_moyscd));

				if(id+"_"===obj.attr('id') && that.focusGridId!==""){
					parentObj = $('#'+that.focusGridId).datagrid('getPanel');
				}

				var size = sel_moyscd.length ;


				// DB問い合わせ系
				if(size > 0){
					var param = that.getInputboxParams(that, id, newValue);
					var rows = $.getSelectListData(that.name, $.id.action_change, id, param);
					var row = rows.length > 0 ? rows[0]:"";
					$.setInputboxRowData('for_inp', id, row, that, parentObj);
				}
				// 検索、入力後特殊処理
				if(that.queried){
					if(id==='sel_moyscd'){
						// 取得された部門コードが部門マスタテーブルに存在しない場合、エラー。
						var sel_moyscd_chk = $.getInputboxData(that.name, $.id.action_check,  $.id.sel_moyscd, [{KEY:"MST_CNT",value:sel_moyscd}]);
						if(sel_moyscd_chk == "0"){
							$.showMessage('E11044');
							return false;
						}
					}

				}

			},
			getInputboxParams: function(that, id, newValue){
				// 情報取得
				var values = {};
				values["value"] = newValue;
				values["TABLEKBN"] = that.baseTablekbn;



				// 情報設定
				return [values];
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