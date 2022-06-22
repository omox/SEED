/**
 * jquery sub window option
 * 店別数量(IT031)
 */
;(function($) {

$.extend({

	winIT031: {
		name: 'Out_ReportwinIT031',
		name2:'Out_ReportIT031',
		prefix:'_srccd',
		suffix:'_winIT031',
		initializes: true,		// 初期化フラグ（全体）
		initializesCond: true,	// 初期化フラグ（条件用）
		lastIndex: -1,			// 編集位置（行）
		sortName: '',			// ソート項目名
		sortOrder: '',			// ソート順
		timeData : (new Date()).getTime(),
		queried : false,
		callreportno:"",		// 呼出し元レポートNo
		callBtnid: "",			// 呼出ボタンID情報
		callreportHidden:[],	// 呼出し元レポートからの引き継ぎ情報
		callTablekbn:"",		// 呼出し元レポートからの引き継ぎ情報(テーブル区分)
		focusRootId:"_winIT031",// （キー移動時必須）キー移動イベントのルートとなるパネルのID
		focusParentId:"",		// （キー移動時必須）現在フォーカスがあたっている項目の親となるパネルのID
		focusGridId:"",			// （キー移動時必須）現在フォーカスがあたっているDataGridのID
		editRowIndex:{},		// グリッド編集行保持
		updateFlg:false,		// 登録ボタン押下済
		baseData:[],			// 検索結果保持用
		filterRows:[],			// 検索結果保持用(ソースコード1,2保持用)
		judgeRepType: {
			yyk			: false,	// 予約
			ref 		: false		// 参照
		},
		kbnList:{},
		initedObject: [],
		addRow:undefined,
		addSeqNo:'1',
		init: function(js, canUpdate) {
			var that = this;
			if(!that.initializes) return false;

			// 呼出し元情報取得
			that.callreportno = js.name;
			that.callreportHidden = js.jsonHidden;
			switch (that.callreportno) {
			case 'Out_ReportXXXXX':
				break;
			default:
				that.callTablekbn = js.baseTablekbn;
				that.judgeRepType.yyk = js.judgeRepType.yyk||js.judgeRepType.err_yyk1||!canUpdate
				that.judgeRepType.ref = js.reportYobiInfo()==='1'||js.judgeRepType.ref;
				that.judgeRepType.err = js.judgeRepType.err;

				break;
			}

			var gridid = 'grd'+that.prefix+that.suffix;

			// ボタン設定
			$('[id^=btn'+that.prefix+']').each(function(){
				var id = $(this).attr('id');
				$('#'+id).click(function() { that.Open(this); });
			});
			$('#'+$.id.btn_back+that.suffix).on("click", that.Back);
			if(that.judgeRepType.ref){
				$.setInputBoxDisable($('#'+$.id.btn_new+that.suffix)).hide();
				$.setInputBoxDisable($('#'+$.id.btn_upd+that.suffix)).hide();
				$.setInputBoxDisable($('#'+$.id.btn_csv_import+that.suffix)).hide();
				$.setInputBoxDisable($($.id.hiddenChangedIdx+that.suffix));
			}else if(that.judgeRepType.yyk){
				$.setInputBoxDisable($('#'+$.id.btn_new+that.suffix));
				$.setInputBoxDisable($('#'+$.id.btn_upd+that.suffix));
				$.setInputBoxDisable($('#'+$.id.btn_csv_import+that.suffix));
				$.setInputBoxDisable($($.id.hiddenChangedIdx+that.suffix));
			}else{
				$('#'+$.id.btn_new+that.suffix).on("click", function(){
					var arr = $('#'+gridid).datagrid('getRows');

					if (arr.length >= 2) {
						$.showMessage("E11241");
						return false;
					}

					if(that.addRow===undefined){
						that.addRow = {};
						var columns = $('#'+gridid).datagrid('getColumnFields');
						for (var i=0; i<columns.length; i++){
							var val = null;
							if(columns[i]==="YUKO_STDT"){
								val = $.getDateAddDay($.exDate().toChar('yyyymmdd'), 1).substr(-6);
							}else if(columns[i]==="YUKO_EDDT"){
								val = '501231';
							}
							that.addRow[columns[i]] = val;
						}
					}

					for (var i = 0; i < arr.length; i++) {
						var obj = arr[i];
						if (obj.SEQNO==='1') {
							that.addSeqNo = '2';
						} else if (obj.SEQNO==='2') {
							that.addSeqNo = '';
						}
					}

					that.addRow["SEQNO"] = that.addSeqNo;

					$.appendDatagridRow(that, gridid, that.addRow);
				});		// 新規行追加
				$('#'+$.id.btn_upd+that.suffix).on("click", that.Update);	// 更新
				// ファイル選択ボタン
				$('#'+$.id.btn_csv_import+that.suffix).on("click", function (e) {
					$("#"+$.id.txt_file+that.suffix).click();
					return false; // must!
				});
				// ファイルテキスト
				$("#"+$.id.txt_file+that.suffix).on("change", function (e) {
					if($(this).val().length == 0){return false;}
					// パラメータをセット
					$($.id.uploadform+that.suffix).append('<input type="hidden" id="report" name="report" value="'+that.name+'">');
					$($.id.uploadform+that.suffix).append('<input type="hidden" name="'+$.id.txt_file+'" value="'+$(this).val()+'">');
					// アップロード実行
					var frm = $($.id.uploadform+that.suffix)[0];
					frm.action = $.reg.upload;
					frm.submit();
					return true;
				});
				// IFrame読み込み時
				$('#if'+that.suffix).on('load', function (e) {
					// ログ出力
					$.log(that.timeData, 'loaded:');

					$("#"+$.id.txt_file+that.suffix).val("");
					try {
						var contents = $(this).contents();
						var data = contents.find('body')[0].innerHTML;
						// 検索処理エラー判定
						if($.cmnError(data)) return false;
						var json = JSON.parse(data);
						var rows = $("#"+gridid).datagrid('getData').rows.concat(json.rows);
						$('#'+gridid).datagrid('loadData', rows);
						$($.id.hiddenChangedIdx+that.suffix).val("1");
						$.cmnNormal(data, undefined, "I00001", [],gridid);
					}catch(exception){
						$.showMessage("E00014");
						console.log(exception.message);
					}
				});
			}
			// 初期化
			$.setCheckboxInit2({}, $.id.chk_del+that.suffix, that);
			$.setInputbox(that, that.callreportno, $.id_inp.txt_srccd+that.suffix, true);
			that.setMeisyoCombo(that, that.callreportno, $.id_mei.kbn136+that.suffix, true);
			$.setInputbox(that, that.callreportno, $.id_inp.txt_yuko_stdt+that.suffix, true);
			$.setInputbox(that, that.callreportno, $.id_inp.txt_yuko_eddt+that.suffix, true);
			$.setInputbox(that, that.callreportno, $.id_inp.txt_seqno+that.suffix, true);

			// dataGrid 初期化
			this.setDataGrid(gridid);

			// ｳｲﾝﾄﾞｳ設定
			$('#'+that.suffix).window({
				iconCls:'icon-search',
				modal:true,
				collapsible:false,
				minimizable:false,
				maximizable:false,
				closed:true,
				cinline:false,
				zIndex:90000,
				title:'ソースコードマスタ(IT031)',
				onBeforeOpen:function(){
					// ウインドウ展開中リサイズイベント無効化
					$.reg.resize = false;
					js.focusParentId = that.suffix;
				},
				onBeforeClose:function(){
					// ウインドウ展開中リサイズイベント有効化
					$.reg.resize = true;
					if(that.updateFlg){
						$($.id.hiddenChangedIdx).val("1");	// 元の帳票に更新フラグ
						if($.isFunction(js.changeInputboxFunc)){
							js.changeInputboxFunc(that, gridid, that.baseData, $('#'+gridid));
						}
					}else{
						if($.getConfirmUnregistFlg($($.id.hiddenChangedIdx+that.suffix))){
							var func_ok = function(r){
								$($.id.hiddenChangedIdx+that.suffix).val("");	// 変更行Index
								$('#'+that.suffix).window('close');
								return true;
							};
							$.showMessage("E11025", undefined, func_ok);
							return false;
						}else{
							that.Clear();										// 変更内容初期化
						}
					}
					js.focusParentId = js.focusRootId;
				},
				onClose:function(){
					$('#'+js.focusParentId).find('#'+that.callBtnid).focus();
					$('#'+$.id.btn_upd+'_winIT031').attr('data-options', "winIT031:false");
				}
			});

			// 画面情報表示
			var txt_shncd = "";
			// 呼出し元別処理
			switch (that.callreportno) {
			case 'Out_ReportXXXXX':
				break;
			default:
				// 元画面より情報を取得し、初期化
				txt_shncd = $.getInputboxText($('#'+$.id.txt_sel_shncd));
				break;
			}
			$.setInputboxValue($('#'+$.id_inp.txt_shncd + that.suffix), txt_shncd);		// 商品コード

			// チェックボックスの設定
			$.initCheckboxCss($("#"+that.focusRootId));
			// キーイベントの設定
			$.initKeyEvent(that);

			// 初回検索
			that.Search();

			that.initializes = !that.initializes;
		},
		Open: function(obj) {
			if ($(obj).is("disabled","disabled"))	return false;
			var that = $.winIT031;
			$('#'+$.id.btn_upd+'_winIT031').attr('data-options', "winIT031:true");
			that.callBtnid = $(obj).attr('id');

			that.updateFlg=false;

			// window 表示
			$('#'+that.suffix).window('open');
		},
		Clear:function(){
			var that = $.winIT031;
			that.initializesCond = true;
			if(!that.judgeRepType.ref){
				$.endEditingDatagrid(that);	// grid系end
				var dg =$('#'+'grd'+that.prefix+that.suffix);
				if(that.baseData && that.baseData.total){
					dg.datagrid('loadData', jQuery.extend(true, {}, that.baseData));
				}else{
					dg.datagrid('loadData', []);
				}
			}
			that.queried = true;
			// 隠し情報初期化
			$($.id.hiddenChangedIdx+that.suffix).val("");						// 変更行Index
			that.initializesCond = false;
		},
		Search: function(){
			var that = $.winIT031;
			// validate=falseの場合何もしない
			if(!that.validation()){ return false; }

			// セッションタイムアウト、利用時間外の確認
			var isTimeout = $.checkIsTimeout();
			if (! isTimeout) {
				// 検索実行
				that.success("grd"+that.prefix+that.suffix);
			}
			return true;
		},
		Update: function(){
			var that = $.winIT031;
			var id = "grd"+that.prefix+that.suffix;
			// validate=falseの場合何もしない
			if(!that.updValidation(id)){ return false; }

			// 変更情報チェック
			if(!$.getConfirmUnregistFlg($($.id.hiddenChangedIdx+that.suffix))){
				$.showMessage('E20582', undefined, function(){$.addErrState(that, $('#'+$.id.btn_upd+that.suffix), false)});
				return false;
			}

			var func_ok = function(r){
				// セッションタイムアウト、利用時間外の確認
				var isTimeout = $.checkIsTimeout();
				if (! isTimeout) {
					that.updSuccess(id);
				}
				return true;
			};
			$.showMessage("W00001", undefined, func_ok);
			return true;
		},
		Back:function(){
			var that = $.winIT031;
			$('#'+that.suffix).window('close');
			that.addRow		= undefined;
			that.addSeqNo	= '1';
			return true;
		},
		validation: function (){	// （必須）批准
			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform+that.suffix).form('validate');
			return rt;
		},
		success: function(id, afterUpd){	// 検索処理
			var that = $.winIT031;
			// 処理時間計測用
			that.timeData = (new Date()).getTime();

			// グリッド初期化&ローディング
			$('#'+id).datagrid('loading');

			// 情報設定
			var json =  [{
				callpage:	$($.id.hidden_reportno).val(),	// 呼出元レポート名
				TABLEKBN:	that.callTablekbn,				// テーブル区分
				SHNCD:		$.getInputboxText($('#'+$.id_inp.txt_shncd+that.suffix)).replace("-", ""),	// 商品コード
				SEQ:		$.getJSONValue(that.callreportHidden, $.id.txt_seq),						// CSVエラー.SEQ
				INPUTNO:	$.getJSONValue(that.callreportHidden, $.id_inp.txt_inputno),				// CSVエラー.入力番号
				YOYAKUDT:	$.getInputboxValue($('#'+$.id_inp.txt_yoyakudt)),							// マスタ変更予定日
				UPDATEFLG:	that.updateFlg					// 登録ボタン押下済みかいなか
			}];

			$.post(
				$.reg.easy,
				{
					page	:	that.name,										// レポート名
					obj		:	id,
					sel		:	(new Date()).getTime(),
					target	:	id,
					action	:	$.id.action_init,
					json	:	JSON.stringify(json),
					datatype:	'datagrid'
				},
				function(data){
					// ログ出力
					$.log(that.timeData, that.suffix+'query:');
					var dg =$('#'+id);
					if(data!==""){
						// JSONに変換
						var json = JSON.parse(data);
						// 結果表示
						dg.datagrid('loadData', json.rows);
						that.setData(id);
					}
					dg.datagrid('loaded');

					that.queried = true;
					// 隠し情報初期化
					$($.id.hiddenChangedIdx+that.suffix).val("");

					// ログ出力
					$.log(that.timeData, that.suffix+'loaded:');
				}
			);

		},
		updValidation: function (id){	// （必須）批准
			var that = this;
			var errMsg= "";
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.gridform+that.suffix).form('validate');
			$.endEditingDatagrid(that);	// grid系end

			// ソースコード系
			// ソースコード取得
			var srccds = [],seqnos = [];

			var gridid = 'grd'+that.prefix+that.suffix;
			var targetRowsSrccd =  $('#'+gridid).datagrid('getRows');
			var csv_updkbn = $('#'+$.id.txt_csv_updkbn).val()				// CSV更新区分
			// ソースコード１、２取得
			var row1 = null, row2=null;
			for (var i=0; i<targetRowsSrccd.length; i++){
				var txt_srccd = targetRowsSrccd[i]["SRCCD"];
				if(!$.isEmptyVal(txt_srccd, false)&&$.isEmptyVal(targetRowsSrccd[i]["DEL"], true)){
					if($.isEmptyVal(targetRowsSrccd[i]["SOURCEKBN2"], true) || (that.judgeRepType.err && csv_updkbn == $.id.value_csvupdkbn_new)){
						var chk_cnt = $.getInputboxData(that.name, $.id.action_check,$.id_inp.txt_srccd, [{KEY:"MST_CNT",SRCCD:txt_srccd}]);
						if(chk_cnt!==""&&chk_cnt!=="0"){
							$.showMessage('E11139', undefined, function(){$.addErrState(that, $('#'+gridid), true, {NO:i, ID:$.id_inp.txt_srccd})});
							return false;
						}
					}
					var kbn = targetRowsSrccd[i]["SOURCEKBN"].split("-")[0].trim();
					if($.isEmptyVal(kbn, false)){
						$.showMessage('EX1047', ["ソース区分"], function(){$.addErrState(that, $('#'+gridid), true, {NO:i, ID:$.id_mei.kbn136})});
						return false;
					} else {
						if (kbn==='0') {
							$.showMessage('E11223', undefined, function(){$.addErrState(that, $('#'+gridid), true, {NO:i, ID:$.id_mei.kbn136})});
							return false;
						} else if ((kbn==='1' || kbn==='3') && txt_srccd.length!==13) {
							$.showMessage('E11302', ["ソースコードの桁数","。ソース区分が"+kbn+"の場合は13桁で入力してください。"], function(){$.addErrState(that, $('#'+gridid), true, {NO:i, ID:$.id_inp.txt_srccd})});
							return false;
						} else if ((kbn==='2' || kbn==='4') && txt_srccd.length!==8) {
							$.showMessage('E11302', ["ソースコードの桁数","。ソース区分が"+kbn+"の場合は8桁で入力してください。"], function(){$.addErrState(that, $('#'+gridid), true, {NO:i, ID:$.id_inp.txt_srccd})});
							return false;
						}
					}
					var seqno = targetRowsSrccd[i]["SEQNO"];
					if(["","1","2","9"].indexOf(seqno)===-1){
						$.showMessage('EX1051', ["ソースコードの順位は、"], function(){$.addErrState(that, $('#'+gridid), true, {NO:i, ID:$.id_inp.txt_seqno})});
						return false;
					}
					seqno = seqno*1;
					if((seqno === 1||seqno === 2) && seqnos.indexOf(seqno)!==-1){
						$.showMessage('EX1051', ["ソースコードの順位は、"], function(){$.addErrState(that, $('#'+gridid), true, {NO:i, ID:$.id_inp.txt_seqno})});
						return false;
					}
					// 有効期間チェック
					var txt_yuko_stdt = targetRowsSrccd[i]["YUKO_STDT"];
					var txt_yuko_eddt = targetRowsSrccd[i]["YUKO_EDDT"];
					if(seqno === 9){
						if($.isEmptyVal(txt_yuko_stdt, true)){
							$.showMessage('EX1050', undefined, function(){$.addErrState(that, $('#'+gridid), true, {NO:i, ID:$.id_inp.txt_yuko_stdt})});
							return false;
						}
					}else{
						if($.isEmptyVal(txt_yuko_stdt, true)){
							$.showMessage('EX1049', undefined, function(){$.addErrState(that, $('#'+gridid), true, {NO:i, ID:$.id_inp.txt_yuko_stdt})});
							return false;
						}
						if($.isEmptyVal(txt_yuko_eddt, true)){
							$.showMessage('EX1049', undefined, function(){$.addErrState(that, $('#'+gridid), true, {NO:i, ID:$.id_inp.txt_yuko_eddt})});
							return false;
						}
					}
					// 日付妥当性
					if(!$.isEmptyVal(txt_yuko_stdt, true)&&!$.isEmptyVal(txt_yuko_eddt, true)&&txt_yuko_stdt > txt_yuko_eddt){
						$.showMessage('E11020', undefined, function(){$.addErrState(that, $('#'+gridid), true, {NO:i, ID:$.id_inp.txt_yuko_eddt})});
						return false;
					}
					// 重複チェック
					if(srccds.indexOf(txt_srccd)!==-1){
						$.showMessage('E11109', undefined, function(){$.addErrState(that, $('#'+gridid), true, {NO:i, ID:$.id_inp.txt_srccd})});
						return false;
					}
					// ソース1,2整合性
					if(seqno === 1){ row1 = targetRowsSrccd[i]; }
					if(seqno === 2){ row2 = targetRowsSrccd[i]; }
					if(row1!==null && row2!==null){
						// ①ソース区分2(2行目)が1(JAN13) or 2(JAN8)の場合、ソース区分1(1行目)が3(EAN13), 4(EAN8), 5(UPC-A), 6(UPC-E)はエラー
						var kbn1 = row1["SOURCEKBN"].split("-")[0];
						var kbn2 = row2["SOURCEKBN"].split("-")[0];
						var errKbns = ["3", "4", "5", "6"];
						if((kbn2==="1"||kbn2==="2")&&errKbns.indexOf(kbn1)!==-1){
							$.showMessage('E11111', undefined, function(){$.addErrState(that, $('#'+gridid), true, {NO:i, ID:$.id_mei.kbn136})});
							return false;
						}
					}
					// コード整合性チェック：チェックデジット算出コード取得
					var srccd_row = $.getSelectListData(that.name, $.id.action_check, $.id_inp.txt_srccd, [{KEY:"CHK_DGT",value:txt_srccd, SOURCEKBN:kbn}]);
					// ソースコードに問題がある場合は、エラー情報が返ってくる（E11165,E11167,E11168,E11169,E11171,E11172,E11224）
					if(srccd_row[0]["ID"]){
						$.showMessage(srccd_row[0]["ID"], ["ソースコード"], function(){$.addErrState(that, $('#'+gridid), true, {NO:i, ID:$.id_inp.txt_srccd})});
						return false;
					}
					srccds.push(txt_srccd);
					if(seqno === 1||seqno === 2){
						seqnos.push(seqno);
					}
				} else if ($.isEmptyVal(txt_srccd)) {
					var kbn = targetRowsSrccd[i]["SOURCEKBN"].split("-")[0].trim();
					if (!$.isEmptyVal(kbn) && kbn!=='0') {
						$.showMessage('E00001', ["ソースコード"], function(){$.addErrState(that, $('#'+gridid), true, {NO:i, ID:$.id_inp.txt_srccd})});
						return false;
					}
				}
			}
			// ソース登録があるにもかかわらず1指定がない場合エラーとする
			if(srccds.length > 0 && seqnos.indexOf(1)===-1){
				$.showMessage('E11110', undefined, function(){$.addErrState(that, $('#'+gridid), true, {NO:0, ID:$.id_inp.txt_seqno})});
				return false;
			}
			return rt;
		},
		updSuccess: function(id){	// validation OK時 の update処理
			var that = this;

			// 初期化
			that.setData(id);
			that.updateFlg = true;
			$($.id.hiddenChangedIdx+that.suffix).val("");
			$('#'+that.suffix).window('close');

		},
		setDataGrid: function(id) {
			var that = this;

			var dformatter =function(value){
				if (undefined===value || null===value || ''===value) return '';
				var add20 = value.length == 6;
				return $.getFormatDt(value, add20);
			};
			var cstyler=function(value,row,index){return 'color: red;font-weight: bold;';};
			var cformatter =function(value,row,index){return value && value===$.id.value_on?$.id.text_on:$.id.text_off;};

			// 呼出し元別処理
			var columns = [[]];
			switch (that.callreportno) {
			case 'Out_ReportXXXXX':
				break;
			default:
				columns = [[
					{field:'DEL',		title:'削除',			width: 35,halign:'center',align:'center'	,formatter:cformatter,styler:cstyler,editor:{type:'checkbox'}},
					{field:'SRCCD',		title:'ソースコード',	width:145,halign:'center',align:'left'		,editor:{type:'textbox'}},
					{field:'SOURCEKBN',	title:'ソース区分',		width:100,halign:'center',align:'left'		,editor:{type:'combobox'}
						,formatter:function(value,row,index){
							if (undefined===value || null===value || ''===value) return '';
							if(value.indexOf('-')===-1){
								if(that.kbnList.length===undefined){
									var datas = $('#'+$.id_mei.kbn136+that.suffix).combobox('getData');
									for (var i=0; i<datas.length; i++){
										that.kbnList[datas[i]["VALUE"]] = datas[i];
									}
								}
								if(that.kbnList[value]!==undefined){
									return that.kbnList[value]["TEXT"];
								}
							}
							return value;
						}
					},
					{field:'YUKO_STDT',	title:'有効開始日',		width: 80,halign:'center',align:'center'	,formatter:dformatter,editor:{type:'numberbox'}},
					{field:'YUKO_EDDT',	title:'有効終了日',		width: 80,halign:'center',align:'center'	,formatter:dformatter,editor:{type:'numberbox'}},
					{field:'SEQNO',		title:'順位',			width: 60,halign:'center',align:'right'		,editor:{type:'numberbox'}}
				]];
				break;
			}

			var funcClickRow = $.fn.datagrid.defaults.onClickRow;
			var funcBeginEdit= $.fn.datagrid.defaults.onBeginEdit;
			var funcEndEdit= $.fn.datagrid.defaults.onEndEdit;
			var funcAfterEdit= $.fn.datagrid.defaults.onAfterEdit;
			if(!that.judgeRepType.ref){
				that.editRowIndex[id] = -1;
				funcClickRow = function(index,field){
					//$.log(that.timeData, that.suffix+'funcClickRow-start-'+id+'　'+index+':');
					$.clickEditableDatagridCell(that,id, index);
					//$.log(that.timeData, that.suffix+'funcClickRow-end-'+id+'　'+index+':');
				};
				funcBeginEdit = function(index,row){
					var list = []
					list = that.initedObject.filter(function(item, index){
						if(item.indexOf('sel_') >= 0){
							return item;
						}
					});
					list.filter(function(item, index){
						var id = item + '_'
						if($('#' + id)){

							// 初期化処理
							$('#'+id).combobox('setText', ' ');
							var data = $('#'+id).combobox('getData');

							var val = -1;
							var initvalue = -1

							// 初期値取得
							var field = item.replace("sel_", "").replace(that.suffix, "").toUpperCase();
							if(row[field] && row[field].trim() != ""){
								initvalue = row[field]
								if(initvalue != -1){
									for (var i=0; i<data.length; i++){
										if (data[i].TEXT == initvalue){
											// 初期値適用
											val = data[i].VALUE;
											break;
										}
									}
								}
							}

							if (val){
								$('#'+id).combobox('setValue', val);
							}

							// フォーカスアウトのタイミングの動作
							// グリッド内のコンボグリッドでは、参照オブジェクトからonLoadSuccessのイベントを継承していない為、
							// reloadによる再読み込み処理を追加する必要がある。
							$($('#'+id)).next().on('focusout', function(e){
								var obj = $(this).prev();
								if (!$.setComboReload(obj,false)) {
									var val = -1;
									if (val){
										var data = obj.combobox('getData');
										var val = null;
										if (that.initedObject && $.inArray(id, that.initedObject) < 0){
											var init = $.getJSONValue(that.jsonHidden, id);
											for (var i=0; i<data.length; i++){
												if (data[i].VALUE == init){
													val = init;
													break;
												}
											}
										}
										obj.combobox('setValue', val);
									}
								}
							});
						}
					});
					$.beginEditDatagridRow(that,id, index, row)
				};
				funcEndEdit = function(index,row,changes){
					//$.log(that.timeData, that.suffix+'funcEndEdit-start-'+id+'　'+index+':');
					$.endEditDatagridRow(that, id, index, row);
					//$.log(that.timeData, that.suffix+'funcEndEdit-end-'+id+'　'+index+':');
				};
				funcAfterEdit = function(index,row,changes){
					//$.log(that.timeData, that.suffix+'funcAfterEdit-start-'+id+'　'+index+':');
					var rowobj = $('#'+id).datagrid('getPanel').find("[datagrid-row-index='"+index+"']");
					// チェックボックスの再追加（EndEdit時に削除されるため）
					$.afterEditAddCheckbox(rowobj);
					//$.log(that.timeData, that.suffix+'funcAfterEdit-end-'+id+'　'+index+':');
				};
				that.editRowIndex[id] = -1;
			}
			$('#'+id).datagrid({
				nowrap: true,
				border: true,
				striped: false,
				collapsible:false,
				remoteSort: true,
				rownumbers:true,
				showHeader:true,
				fit:true,
				frozenColumns:[[]],
				columns:columns,
				fitColumns:false,	// 指定カラム幅を適用する場合、false 指定。
				rowStyler:function(index, row){
				},
				onLoadSuccess:function(data){
					// ログ出力
					$.log(that.timeData, that.suffix+'query-'+id+':');// チェックボックスの設定
					var panel = $('#'+id).datagrid('getPanel').find('.datagrid-body');
					$.initCheckboxCss(panel);
					$.setFocusFirst(panel);
				},
				onClickRow: funcClickRow,
				onBeginEdit:funcBeginEdit,
				onEndEdit: funcEndEdit,
				onAfterEdit: funcAfterEdit,
				autoRowHeight:false,
				pagination:false,
				pagePosition:'bottom',
				singleSelect:true
			});
		},
		setData: function (id){
			var that = this;

			that.baseData = jQuery.extend(true, {}, $('#'+id).datagrid('getData'));

			// ソースコード1,2を取得
			var targetRows = [];
			var targetRowsSrccd = $('#'+id).datagrid('getRows');
			var newTargetRowsSrccd = [];
			for (var i=0; i<targetRowsSrccd.length; i++){
				if(!$.isEmptyVal(targetRowsSrccd[i]["SRCCD"])&&$.isEmptyVal(targetRowsSrccd[i]["DEL"], true)){
					var seqno = targetRowsSrccd[i]["SEQNO"];
					if(targetRowsSrccd[i]["SEQNO"] === "1"){
						targetRows.push(jQuery.extend(true, {}, targetRowsSrccd[i]));
					} else {
						newTargetRowsSrccd.push(jQuery.extend(true, {}, targetRowsSrccd[i]));
					}
				}
			}
			for (var i=0; i<newTargetRowsSrccd.length; i++){
				if(!$.isEmptyVal(newTargetRowsSrccd[i]["SRCCD"])&&$.isEmptyVal(newTargetRowsSrccd[i]["DEL"], true)){
					var seqno = newTargetRowsSrccd[i]["SEQNO"];
					if(newTargetRowsSrccd[i]["SEQNO"] === "2"){
						targetRows.push(jQuery.extend(true, {}, newTargetRowsSrccd[i]));
						break;
					}
				}
			}

			// 入力されたソースコードが2行以上ありかつ順位が1or2以外だった場合の考慮
			if (newTargetRowsSrccd.length >= 1 && targetRows.length===1) {
				var minStDtRow	= '';
				var minSrcRow	= '';
				var sysdate = $.getDateAddDay($.exDate().toChar('yyyymmdd'), 1).substr(-6)

				if (newTargetRowsSrccd.length === 1) {
					minStDtRow = 0;
				} else {

					for (var i=0; i<newTargetRowsSrccd.length; i++){
						if (i!==0) {

							var now = newTargetRowsSrccd[i]["YUKO_STDT"]*1;
							var min = !$.isEmptyVal(minStDtRow) ? newTargetRowsSrccd[minStDtRow]["YUKO_STDT"]*1 : newTargetRowsSrccd[i-1]["YUKO_STDT"]*1;

							// 有効開始日の最小値を取得
							if (now < min && now >= sysdate) {
								minStDtRow = i;
							} else if (now > min && min >= sysdate) {
								minStDtRow = !$.isEmptyVal(minStDtRow) ? minStDtRow : i-1;
							} else if (now===min) {
								now = newTargetRowsSrccd[i]["SRCCD"]*1;
								min = !$.isEmptyVal(minStDtRow) ? newTargetRowsSrccd[minStDtRow]["SRCCD"]*1 : newTargetRowsSrccd[i-1]["SRCCD"]*1;

								// 有効開始日が全て同じだった場合の為にソースコードの最小値を保持
								if (now < min) {
									minStDtRow = i;
								} else if (now > min) {
									minStDtRow = !$.isEmptyVal(minStDtRow) ? minStDtRow : i-1;
								}
							} else {
								now = newTargetRowsSrccd[i]["SRCCD"]*1;
								min = !$.isEmptyVal(minSrcRow) ? newTargetRowsSrccd[minSrcRow]["SRCCD"]*1 : newTargetRowsSrccd[i-1]["SRCCD"]*1;

								// 有効開始日が全て同じだった場合の為にソースコードの最小値を保持
								if (now < min) {
									minSrcRow = i;
								} else if (now > min) {
									minSrcRow = !$.isEmptyVal(minSrcRow) ? minSrcRow : i-1;
								}
							}
						}
					}
				}

				if (!$.isEmptyVal(minStDtRow)) {
					targetRows.push(jQuery.extend(true, {}, newTargetRowsSrccd[minStDtRow]));
				} else {
					targetRows.push(jQuery.extend(true, {}, newTargetRowsSrccd[minSrcRow]));
				}
			}

			that.filterRows = targetRows;

			if(that.filterRows.length > 0 && !$.isEmptyVal(that.filterRows[0]["SRCCD"])){
				$.setInputboxValue($('#'+$.id_inp.txt_srccd+1+that.suffix), that.filterRows[0]["SRCCD"]);	// ソースコード1
			}
			if(that.filterRows.length > 1 && !$.isEmptyVal(that.filterRows[1]["SRCCD"])){
				$.setInputboxValue($('#'+$.id_inp.txt_srccd+2+that.suffix), that.filterRows[1]["SRCCD"]);	// ソースコード2
			}
		},
		changeInputboxFunc:function(that, id, newValue, obj, all){

			var parentObj = $('#'+that.focusRootId);
			if(id+"_"===obj.attr('id') && that.focusGridId!==""){
				parentObj = $('#'+that.focusGridId).datagrid('getPanel');
			}

			// SEQNO
			if(id===$.id_inp.txt_seqno){

			}
		},
		// コンボボックス(特にid_mei宣言の入力項目)を共通で設定する
		setMeisyoCombo: function(that, reportno, id, isUpdateReport){
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

			var required = options && options.required;
			var editable = true;
			var editableCheck = options && options.editable? true:false;
			var topBlank = !required;

			if (required==='false') {
				topBlank = true;
			}

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

			var filter = function(q,row){
				var opts=$(this).combobox("options");
				return row[opts.textField].toLowerCase().indexOf(q.toLowerCase())>=0;
			};

			if (options && options.filter) {
				filter = function(q,row){
					var opts=$(this).combobox("options");
					return row[opts.textField].toLowerCase().indexOf(q.toLowerCase())===0;
				}
			}

			$('#'+id).combobox({
				url:$.reg.easy,
				required: required,
				readonly:readonly,
				editable: editable,
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
						$(this).combobox('hidePanel');
						e.preventDefault();
					},
					query: $.fn.combobox.defaults.keyHandler.query
				},
				filter:filter,
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

					var data = obj.combobox('getData');

					if (!$.setComboReload(obj,true) && !editableCheck) {
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
				},
				onClick:function(record) {
					$('#'+id).combo("textbox").focus();
				}
			});
			if(isRefer){ $.setInputStateRefer(that, $('#'+id)); }
		}
	}
});

})(jQuery);