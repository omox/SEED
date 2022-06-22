/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
			name:		'Out_Reportx122',			// （必須）レポートオプションの確認
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
			dedefaultObjNum:	29,	// 初期化オブジェクト数
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

				$('#btn_search').click(function() {
					that.sendBtnid = 'btn_search';
				});

				// 初期検索可能
				that.onChangeReport = true;

				$.setInputbox(that, reportno, $.id_inp.txt_tencd+'_ten_org', isUpdateReport);
				$.setInputbox(that, reportno, $.id_inp.txt_bmncd+'_bmn_org', isUpdateReport);
				$.setInputbox(that, reportno, $.id_inp.txt_tencd+'_ten_copy', isUpdateReport);
				$.setInputbox(that, reportno, $.id_inp.txt_bmncd+'_bmn_copy', isUpdateReport);

				// リードタイムパターン
				this.setMeisyo(that, reportno, $.id.sel_readtmptn, isUpdateReport);
				var count = 2;
				// 名称マスタ参照系
				var meisyoSelect = Object.getOwnPropertyNames($.id_mei);
				for ( var sel in meisyoSelect ) {
					if($('#'+$.id_mei[meisyoSelect[sel]]).length > 0){
						this.setMeisyo(that, reportno, $.id_mei[meisyoSelect[sel]], isUpdateReport);
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

				if(that.sendBtnid===$.id.btn_new){
					// 表示制限：削除ボタン
					$('#'+$.id.btn_del).linkbutton('disable');
					$('#'+$.id.btn_del).attr('disabled', 'disabled').hide();
					// 表示制限：店舗部門実績
					$("#updLegend").hide();
					$("#disp_record_info").hide();
					$.initReportInfo("TP006", "店舗部門マスタ　新規" ,'新規');
					that.judgeRepType.sei_new = true;
					$.setInputboxValue($('#'+$.id_inp.txt_tenantcd),'0');
					$.setInputboxValue($('#'+$.id_inp.txt_bmn_atr1),'0');
					$.setInputboxValue($('#'+$.id_inp.txt_bmn_atr2),'0');
					$.setInputboxValue($('#'+$.id_inp.txt_bmn_atr3),'0');
					$.setInputboxValue($('#'+$.id_inp.txt_bmn_atr4),'0');
					$.setInputboxValue($('#'+$.id_inp.txt_bmn_atr5),'0');
				} else if (that.sendBtnid===$.id.btn_sel_refer) {
					$('#'+$.id.btn_upd).hide();
					$('#'+$.id.btn_upd).attr('disabled', 'disabled').hide();
					$('#'+$.id.btn_del).hide();
					$('#'+$.id.btn_del).attr('disabled', 'disabled').hide();
					// 表示制限：店舗部門マスタコピー
					$('#'+$.id_inp.txt_tencd+'_ten_org').attr('tabindex', -1).combobox('disable');
					$('#'+$.id_inp.txt_bmncd+'_bmn_org').attr('tabindex', -1).combobox('disable');
					$('#'+$.id_inp.txt_tencd+'_ten_copy').attr('tabindex', -1).combobox('disable');
					$('#'+$.id_inp.txt_bmncd+'_bmn_copy').attr('tabindex', -1).combobox('disable');
					$('#'+$.id_inp.txt_tencd).attr('readonly', 'readonly')
					$('#'+$.id_inp.txt_tencd).attr('tabindex', -1).combobox('disable');
					$('#'+$.id_inp.txt_bmncd).attr('readonly', 'readonly')
					$('#'+$.id_inp.txt_bmncd).attr('tabindex', -1).combobox('disable');
					$('#'+$.id.btn_search).linkbutton('disable');
					$('#'+$.id.btn_search).attr('disabled', 'disabled').hide();
					$("#newLegend").hide();
					$.setInputBoxDisable($("input[name="+$.id.rad_areakbn+"]"));
					$.initReportInfo("TP006", "店舗部門マスタ　参照", "参照");
				}else{
					// 表示制限：店舗部門マスタコピー
					$('#'+$.id_inp.txt_tencd+'_ten_org').attr('tabindex', -1).combobox('disable');
					$('#'+$.id_inp.txt_bmncd+'_bmn_org').attr('tabindex', -1).combobox('disable');
					$('#'+$.id_inp.txt_tencd+'_ten_copy').attr('tabindex', -1).combobox('disable');
					$('#'+$.id_inp.txt_bmncd+'_bmn_copy').attr('tabindex', -1).combobox('disable');
					$('#'+$.id_inp.txt_tencd).attr('readonly', 'readonly')
					$('#'+$.id_inp.txt_tencd).attr('tabindex', -1).combobox('disable');
					$('#'+$.id_inp.txt_bmncd).attr('readonly', 'readonly')
					$('#'+$.id_inp.txt_bmncd).attr('tabindex', -1).combobox('disable');
					$('#'+$.id.btn_search).linkbutton('disable');
					$('#'+$.id.btn_search).attr('disabled', 'disabled').hide();
					$("#newLegend").hide();
					$.initReportInfo("TP006", "店舗部門マスタ　更新" ,'更新');
					that.judgeRepType.sei_upd = true;
				}

				// 各種遷移ボタン
				$('#'+$.id.btn_new).on("click", $.pushChangeReport);
				$('#'+$.id.btn_copy).on("click", $.pushChangeReport);
				$('#'+$.id.btn_cancel).on("click", $.pushChangeReport);
				// 全体処理
				if(that.reportYobiInfo()==='1'){
					$.setInputBoxDisable($($.id.hiddenChangedIdx));
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
				var msgid= "";
				// EasyUI のフォームメソッド 'validate' 実施
				var rt = true;
				var txt_tenorg		= $('#'+$.id_inp.txt_tencd+'_ten_org').textbox('getValue');				// コピー元店コード
				var txt_bmnorg		= $('#'+$.id_inp.txt_bmncd+'_bmn_org').textbox('getValue'); 			// コピー元部門コード
				var txt_tencopy		= $('#'+$.id_inp.txt_tencd+'_ten_copy').textbox('getValue'); 			// コピー先店コード
				var txt_bmncopy		= $('#'+$.id_inp.txt_bmncd+'_bmn_copy').textbox('getValue'); 			// コピー先部門コード
				var search = true;


				msgid = that.checkInputboxFunc(search);
				if(msgid != null){
					$.showMessage(msgid);
					return false;
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
				var txt_tenorg		= $('#'+$.id_inp.txt_tencd+'_ten_org').textbox('getValue');		// コピー元店コード
				var txt_bmnorg		= $('#'+$.id_inp.txt_bmncd+'_bmn_org').textbox('getValue');		// コピー元部門コード
				var txt_tencopy		= $('#'+$.id_inp.txt_tencd+'_ten_copy').textbox('getValue');		// コピー先店コード
				var txt_bmncopy		= $('#'+$.id_inp.txt_bmncd+'_bmn_copy').textbox('getValue');		// コピー先部門コード
				var txt_bmncd		= $('#'+$.id_inp.txt_bmncd).textbox('getValue');		// 部門コード
				var txt_tencd		= $('#'+$.id_inp.txt_tencd).textbox('getValue');		// 店コード

				// 処理時間計測用
				that.timeData = (new Date()).getTime();
				// Loading表示
				$.appendMaskMsg();

				$.post(
						$.reg.jqgrid ,
						{
							report:			that.name,		// レポート名
							TENORG:			txt_tenorg,
							BMNORG:			txt_bmnorg,
							TENCOPY:		txt_tencopy,
							BMNCOPY:		txt_bmncopy,
							BMNCD:			txt_bmncd,
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
			updValidation: function (){	// （必須）批准
				var that = this;
				var errMsg= "";
				var isNew = that.judgeRepType.sei_new;
				var isupd = that.judgeRepType.sei_upd;
				var search = false;
				var textArray = [];
				var checkCol='';
				var selCheck			 = $.getInputboxValue($('#'+$.id.sel_readtmptn));				// グループ区分
				if(selCheck == -1){
					checkCol = 'リードタイムパターン';
				}
				var selCheck			 = $.getInputboxValue($('#'+$.id_mei.kbn317));				// グループ区分
				if(selCheck == -1){
					if(checkCol.length>0){
						checkCol = checkCol + '、MIO区分';
					}else{
						checkCol = 'MIO区分'
					}
				}
				var selCheck			 = $.getInputboxValue($('#'+$.id_mei.kbn318));				// グループ区分
				if(selCheck == -1){
					if(checkCol.length>0){
						checkCol = checkCol + '、割引区分';
					}else{
						checkCol = '割引区分'
					}
				}
				var selCheck			 = $.getInputboxValue($('#'+$.id_mei.kbn319));				// グループ区分
				if(selCheck == -1){
					if(checkCol.length>0){
						checkCol = checkCol + '、自社テナント';
					}else{
						checkCol = '自社テナント'
					}
				}
				var selCheck			 = $.getInputboxValue($('#'+$.id_mei.kbn320));				// グループ区分
				if(selCheck == -1){
					if(checkCol.length>0){
						checkCol = checkCol + '、ロス分析対象';
					}else{
						checkCol = 'ロス分析対象'
					}
				}
				var selCheck			 = $.getInputboxValue($('#'+$.id_mei.kbn321));				// グループ区分
				if(selCheck == -1){
					if(checkCol.length>0){
						checkCol = checkCol + '、予算区分';
					}else{
						checkCol = '予算区分'
					}
				}
				var selCheck			 = $.getInputboxValue($('#'+$.id_mei.kbn322));				// グループ区分
				if(selCheck == -1){
					if(checkCol.length>0){
						checkCol = checkCol + '、棚卸対象区分';
					}else{
						checkCol = '棚卸対象区分'
					}
				}
				var selCheck			 = $.getInputboxValue($('#'+$.id_mei.kbn118));				// グループ区分
				if(selCheck == -1){
					if(checkCol.length>0){
						checkCol = checkCol + '、プライスカード種類';
					}else{
						checkCol = 'プライスカード種類'
					}
				}
				var selCheck			 = $.getInputboxValue($('#'+$.id_mei.kbn119));				// グループ区分
				if(selCheck == -1){
					if(checkCol.length>0){
						checkCol = checkCol + '、プライスカード色';
					}else{
						checkCol = 'プライスカード色'
					}
				}
				var selCheck			 = $.getInputboxValue($('#'+$.id_mei.kbn323));				// グループ区分
				if(selCheck == -1){
					if(checkCol.length>0){
						checkCol = checkCol + '、売上げフラグ';
					}else{
						checkCol = '売上げフラグ'
					}
				}

				if(checkCol!=''){
					textArray.push(checkCol);
					$.showMessage('E00001',textArray);
					return false;
				}

				// EasyUI のフォームメソッド 'validate' 実施
				var rt = $($.id.toolbarform).form('validate');
				if(!rt){
					$.addErrState(that, $('.validatebox-invalid').eq(0), false);
					return false;
				}

				errMsg = that.checkInputboxFunc(search);
				if(errMsg !==null){
					$.showMessage(errMsg);
					return false;
				}

				return rt;
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
			setMeisyo: function(that, reportno, id, isUpdateReport){
				var idx = -1;
				if($('#'+id).is(".easyui-combobox_")){
					$('#'+id).removeClass("easyui-combobox_").addClass("easyui-combobox");
				}

				// 更新項目で参照表示かどうか
				var isRefer = $.isReferUpdateInput(that, $('#'+id), isUpdateReport);
				var readonly = isRefer;
				var onShowPanel = $.fn.combobox.defaults.onShowPanel;
				if (isRefer) {
					onShowPanel = function(){
						$('#'+id).combobox('hidePanel');
					};
				}

				var tag_options = $('#'+id).attr('data-options');
				if(tag_options){
					tag_options = '\"'+tag_options.replace(/'/g, '').replace(/,/g, '\",\"').replace(/([a-zA-Z0-9]+):/g, '$1\":\"')+'\"';
				}
				var options = tag_options ? JSON.parse('{'+tag_options+'}'): JSON.parse('{}');
				if(that.sendBtnid===$.id.btn_new){
					var required;
				}else{
					var required = options && options.required;
				}

				var editable = options && options.editable? true:false;
				var topBlank = !required;
				var panelWidth = options && options.panelWidth ? options.panelWidth : null;
				var panelHeight = options && options.panelHeight ? options.panelHeight :'auto';
				var suffix = that.suffix ? that.suffix : '';
				var changeFunc1 = null;
				if(isUpdateReport){
					changeFunc1 = function(){
						if(idx > 0 && that.queried && $($.id.hiddenChangedIdx).is(':enabled')){
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
					readonly:readonly,
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
					onShowPanel:onShowPanel,
					onBeforeLoad:function(param){
						// 情報設定
						var json = [{
							DUMMY: 'DUMMY'
						}];
						if(topBlank){json[0]['TOPBLANK'] = topBlank;}

						param.page		=	reportno;
						param.obj		=	id.replace(suffix, "");
						param.sel		=	(new Date()).getTime();
						param.target	=	id;
						param.action	=	$.id.action_init;
						param.json		=	JSON.stringify(json);
						param.datatype	=	"combobox";
						$('#'+id).combobox('setText', ' ');
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
						$('#'+id).combobox('setValue', val);

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
						if(idx > 0){
							$.removeErrState();
						}

						that.getComboErr(obj,false,newValue,oldValue);
					}
				});
				if(isRefer){ $.setInputStateRefer(that, $('#'+id)); }
			},
			// IDとvalueでチェック処理を実施
			checkInputboxFunc:function(search){
				var that = this;
				var sdt, edt;
				var param = {};
				var isNew = that.judgeRepType.sei_new;
				if(search == true){
					if(that.sendBtnid == 'btn_search'){
						var bmn_o = $.getInputboxValue($('#'+$.id_inp.txt_bmncd+'_bmn_org'));
						var ten_o = $.getInputboxValue($('#'+$.id_inp.txt_tencd+'_ten_org'));
						var bmn_c = $.getInputboxValue($('#'+$.id_inp.txt_bmncd+'_bmn_copy'));
						var ten_c = $.getInputboxValue($('#'+$.id_inp.txt_tencd+'_ten_copy'));
						if(bmn_o == bmn_c && ten_o == ten_c ){
							return "EX1021";
						}
					}
				}else{
					if(isNew){
							param["KEY"] =  "MST_CNT",
							param["TENCD"] = $.getInputboxValue($('#'+$.id_inp.txt_tencd)),
							param["BMNCD"] = $.getInputboxValue($('#'+$.id_inp.txt_bmncd));
							var rt1 = $.getInputboxData(that.name, $.id.action_check,  "MSTTENBMN", [param]);
							if(rt1!=""&&rt1!="0"){
								return "EX1023";
							}
							param = {};
							param["KEY"] =  "MST_CNT",
							param["value"] = $.getInputboxValue($('#'+$.id_inp.txt_tencd));
							var rt2 = $.getInputboxData(that.name, $.id.action_check,  $.id_inp.txt_tencd, [param]);
							if(rt2 !="1"){
								return "E11096";
							}

							param = {};
							param["KEY"] =  "MST_CNT",
							param["value"] = $.getInputboxValue($('#'+$.id_inp.txt_bmncd));
							var txt_bmncd_chk = $.getInputboxData(that.name, $.id.action_check,  $.id_inp.txt_bmncd, [param]);
							if(txt_bmncd_chk == "0" || txt_bmncd_chk == ""){
								return "E11044";
							}
					}
				}

				return null;
			},
			updSuccess: function(id){	// validation OK時 の update処理
				var that = this;

				//var txt_sel_bmncd		= $.getJSONObject(this.jsonString, $.id.txt_sel_bmncd).value;		// 検索部門コード

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
//							DATA:			JSON.stringify(targetRows),		// 更新対象情報
							DATA:			JSON.stringify(targetDatas),	// 更新対象情報
							t:				(new Date()).getTime()
						},
						function(data){
							// 検索処理エラー判定
							if($.updError(id, data)) return false;

							var afterFunc = function(){
								// 初期化
								that.clear();
								that.changeReport(that.name, 'btn_return');
							};
							$.updNormal(data, afterFunc);

							// ログ出力
							$.log(that.timeData, 'loaded:');

							/*alert(JSON.parse(data).opts.S_MSG);
							// 初期化
							that.clear();

							// ログ出力
							$.log(that.timeData, 'loaded:');
							// 一覧画面へ戻る
							that.endUpdate()*/
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
				var txt_bmncd		= $('#'+$.id_inp.txt_bmncd).textbox('getValue');		// 部門コード
				var txt_tencd		= $('#'+$.id_inp.txt_tencd).textbox('getValue');		// 店コード
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
							BMNCD:			txt_bmncd,
							TENCD:			txt_tencd,
							DATA:			JSON.stringify(targetDatas),	// 更新対象情報
							t:				(new Date()).getTime()
						},
						function(data){
							// 検索処理エラー判定
							if($.delError(id, data)) return false;

							alert(JSON.parse(data).opts.S_MSG);
							// 初期化
							that.clear();

							// ログ出力
							$.log(that.timeData, 'loaded:');
							// 一覧画面へ戻る
							that.endUpdate();
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

				// 店コードコピー元
				this.jsonTemp.push({
					id:		$.id_inp.txt_tencd+'_ten_org',
					value:	$('#'+$.id_inp.txt_tencd+'_ten_org').textbox('getValue'),
					text:	''
				});
				// 部門コードコピー元
				this.jsonTemp.push({
					id:		$.id_inp.txt_bmncd+'_bmn_org',
					value:	$('#'+$.id_inp.txt_bmncd+'_bmn_org').textbox('getValue'),
					text:	''
				});
				// 店コードコピー先
				this.jsonTemp.push({
					id:		$.id_inp.txt_tencd+'_ten_copy',
					value:	$('#'+$.id_inp.txt_tencd+'_ten_copy').textbox('getValue'),
					text:	''
				});
				// 部門コードコピー先
				this.jsonTemp.push({
					id:		$.id_inp.txt_bmncd+'_bmn_copy',
					value:	$('#'+$.id_inp.txt_bmncd+'_bmn_copy').textbox('getValue'),
					text:	''
				});
				// 部門コード
				this.jsonTemp.push({
					id:		$.id_inp.txt_bmncd,
					value:	$('#'+$.id_inp.txt_bmncd).textbox('getValue'),
					text:	''
				});
				// 店コード
				this.jsonTemp.push({
					id:		$.id_inp.txt_tencd,
					value:	$('#'+$.id_inp.txt_tencd).textbox('getValue'),
					text:	''
				});
			},
			setData: function(rows, opts){		// データ表示
				var that = this;

				if(rows.length > 0){
					$('#'+that.focusRootId).find('[col^=F]').each(function(){
						var col = $(this).attr('col');
						if(rows[0][col] != undefined){
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

//					// window 幅取得
//					var changeWidth  = $(window).width();

//					// toolbar の調整
//					$($.id.toolbar).panel('resize',{width:changeWidth});

//					// toolbar の高さ調整
//					$.setToolbarHeight();

//					// DataGridの高さ
//					var gridholderHeight = 0;
//					var placeholderHeight = 0;

//					if ($($.id.gridholder).datagrid('options') != 'undefined') {
//					// tb
//					placeholderHeight = $($.id.toolbar).panel('panel').height() + $($.id.buttons).height();

//					// datagrid の格納された panel の高さ
//					gridholderHeight = $(window).height() - placeholderHeight;
//					}

//					$($.id.gridholder).datagrid('resize', {
//					width:	changeWidth,
//					height:	gridholderHeight
//					});
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
				$.setJSONObject(sendJSON, 'sendBtnid', btnId, $('#'+btnId).attr("title"));		// 実行ボタン情報保持
				// 呼出別処理
				// 戻る実行時用に現在の画面情報を保持する
				var states = $.getBackBaseJSON(that);
				// TODO 各種グリッド情報を設定
				var newrepinfos = $.getBackJSON(that, states);
				$.setJSONObject(sendJSON, 'repinfo', newrepinfos, '');
				switch (btnId) {
				case $.id.btn_back:
					sendMode = 2;
					// 転送先情報
					index = 1;
					if(that.reportYobiInfo()==='1'){
						index = 2;
					}
					childurl = href[index];
					break;
				case $.id.btn_cancel:
					sendMode = 2;
					// 転送先情報
					index = 1;
					if(that.reportYobiInfo()==='1'){
						index = 2;
					}
					childurl = href[index];
					break;
				case "btn_return":
					sendMode = 2;
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