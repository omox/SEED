/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_Reportx221',			// （必須）レポートオプションの確認
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
		dedefaultObjNum:	5,	// 初期化オブジェクト数
		initObjNum:	-1,
		initedObject: [],
		maxMergeCell: 4,
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
		pushBtnId: "",						// （必須）実行ボタンID情報(検索系で利用)
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

			// 編集可能データグリッドの共通処理設定
			// 編集エディターの機能拡張（非表示で参照用のオブジェクトが準備してある前提、参照オブジェクトはグリッドのフィールド名を元に判断）
			$.extendDatagridEditor(that);

			// 初期検索可能
			that.onChangeReport = false;

			// 入力テキストボックス系
			var inputbox = Object.getOwnPropertyNames($.id_inp);
			for ( var sel in inputbox ) {
				if($('#'+$.id_inp[inputbox[sel]]).length > 0){
					$.setInputbox(that, reportno, $.id_inp[inputbox[sel]], isUpdateReport);
				}
			}

			// 業務日付を基準日に設定
			$.getsetInputboxData(reportno, $.id_inp.txt_shoridt, [{}], $.id.action_init);

			// センター
			this.setCenter(that, reportno, $.id.sel_center_orr);

			// 便コード
			this.setSupplyNo(that, reportno, $.id.sel_supplyno_orr);

			// 有効開始・終了、取扱終了
			$.setInputBoxDisableVariable($('#'+$.id_inp.txt_effectivestartdate));
			$.setInputBoxDisableVariable($('#'+$.id_inp.txt_effectiveenddate));
			$.setInputBoxDisableVariable($('#'+$.id_inp.txt_handleenddate));

			// コースマスタ
			that.setEditableGrid(that, reportno, $.id.gridholder);

			// 初期表示時に検索処理を通らない為フラグをtrueに
			that.queried = true;

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

			if (sendBtnid && sendBtnid.length > 0) {
				$.reg.search = true;
			}

			var reportYobi1 = $.getJSONValue(that.jsonHidden, "reportYobi1");
			if(!reportYobi1){
				reportYobi1 = $('#reportYobi1').val();
			}
			$('#reportYobi1').val(reportYobi1);

			// 更新非対象項目は非活性に
			$('#'+$.id.btn_del).linkbutton('disable').attr('disabled', 'disabled').hide();
			$('#'+$.id.btn_upd).linkbutton('disable').attr('disabled', 'disabled').hide();
			$('#'+$.id.btn_cancel).linkbutton('disable').attr('disabled', 'disabled').hide();

			$(".inp_hide1").hide();

			// 更新ユーザー情報表示ラン
			$("#disp_record_info").hide();

			// 各種遷移ボタン
			$('#'+$.id.btn_cancel).on("click", $.pushChangeReport);

			$.initReportInfo("DC001", "コースマスタ　検索・登録", "検索");

			// 全体処理
			if(that.reportYobiInfo()==='1'){
				$.setInputBoxDisable($($.id.hiddenChangedIdx));
			}
		},
		initCondition: function (){	// 条件初期値セット
			var that = this;
			// 初期化項目
		},
		clear:function(){
			// 隠し情報初期化
			$($.id.hiddenChangedIdx).val("");	// 変更行Index

			// グリッド初期化
			this.success(this.name, false);
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
		success: function(reportno, sortable){	// （必須）正処理

			if (sortable) sortable=1; else sortable=0;
			var that = this;

			// 検索実行
			var sel_center_orr		= $.getJSONObject(that.jsonString, $.id.sel_center_orr).value;		// センター
			var sel_supplyno_orr	= $.getJSONObject(that.jsonString, $.id.sel_supplyno_orr).value;	// 便
			var txt_shoridt			= $.getJSONObject(that.jsonString, $.id_inp.txt_shoridt).value;		// 基準日

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$.appendMaskMsg();

			$.post(
				$.reg.jqgrid ,
				{
					report			:that.name,		// レポート名
					CENTERCD		:sel_center_orr,
					SUPPLYNO		:sel_supplyno_orr,
					STANDARDDATE	:txt_shoridt,
					SENDBTNID		:that.sendBtnid,
					t				:(new Date()).getTime(),
					sortable		:sortable,
					sortName		:that.sortName,
					sortOrder		:that.sortOrder,
					rows			:1	// 表示可能レコード数
				},
				function(json){
					// 検索処理エラー判定
					if($.searchError(json)) return false;

					// ログ出力
					$.log(that.timeData, 'query:');

					// Load処理回避
					$.tryChangeURL(null);

					// 検索データ（想定）
					that.gridData = JSON.parse(json).rows;
					that.gridTitle = JSON.parse(json).titles;

					var opts = JSON.parse(json).opts

					// メインデータ表示
					that.setData(that.gridData, opts);
					that.queried = true;

					// データグリッド初期化
					that.setEditableGrid(that, reportno, $.id.gridholder);

					if (sortable===0){
						var options = $($.id.gridholder).datagrid('options');
						// 初期検索時に並び替え情報のリセット
						options.sortName = null;
						options.sortOrder = null;

						var titles = JSON.parse(json).titles;
						/** Colomns設定(不要の場合は除去) ※DataGrid用 */
						// 列表示切替
						if(titles != undefined && titles.length > 0){
							// 可変列作成
							var columnTop		= [];	// 期間
							var columnBottom	= [];	// 項目名称
							var filed = options.frozenColumns[0].length+1;	// 可変列開始位置
							var targetId = $.id_inp.txt_storecd;
							var check = $('#'+targetId).attr("check") ? JSON.parse('{'+$('#'+targetId).attr("check")+'}'): JSON.parse('{}');	// コードのcheck要素を取得
							var formatterLPad = function(value){
								return $.getFormatLPad(value, 3);
							};

							// コースの入力
							options.frozenColumns[0][7].title = "コ<br>|<br>ス";

							// 列情報を取得
							var tenpos = new Array();
							var items = new Array();
							for (var i=0; i<titles.length; i++){
								var tit = titles[i].split(",");
								if($.inArray(tit[0],tenpos)==-1){
									tenpos.push(tit[0]);
									items[tit[0]] = [tit[1]];
								}else{
									items[tit[0]].push(tit[1]);
								}
							}

							// 配送順序ヘッダー列作成
							var tit = '<div style="text-align:left;"><span>配送順序</span></div>';
							columnTop.push({title:tit,colspan:titles.length});
							for (var i=0; i<titles.length; i++){
								columnBottom.push({field:'F'+filed,  title:titles[i].split(","),  width:40,  halign:'center', align:'left', formatter:formatterLPad,editor:{type:'numberbox'}});
								that.extenxDatagridEditorIds["F"+filed]="txt_storecd";
								filed++;
							}
							that.extenxDatagridEditorIds["F"+filed]="txt_storecd";

							// datagrid のタイトル再設定
							var columns = [];
							columns.push(columnTop);
							columns.push(columnBottom);

							// datagrid のタイトル再設定
							$($.id.gridholder).datagrid({ columns:columns });
						}
					}

					that.queried = true;
					// Load処理回避
					$.tryChangeURL($.reg.jqeasy);

					// グリッド再描画（easyui 1.4.2 対応）
					$($.id.gridholder).datagrid('load', {} );
					$.removeMask();

					// 状態保存
					$.saveState2(reportno, that.getJSONString());

					// 検索条件欄非活性・非表示
					// センター非活性
					$.setInputBoxDisable($('#'+$.id.sel_center_orr));

					// 便非活性
					$.setInputBoxDisable($('#'+$.id.sel_supplyno_orr));

					// 基準日非活性
					$.setInputBoxDisable($('#'+$.id_inp.txt_shoridt));

					// 検索ボタン非活性
					$('#'+$.id.btn_del).linkbutton('disable').attr('disabled', 'disabled').hide();
					$("#"+$.id.btn_search).linkbutton('disable');
					$("#"+$.id.btn_search).attr('tabindex', -1).hide();
					$(".inp_hide2").hide();

					// 入力欄表示・活性
					// 有効開始日
					$.setInputBoxEnableVariable($('#'+$.id_inp.txt_effectivestartdate), true);

					// 取扱終了日
					$.setInputBoxEnableVariable($('#'+$.id_inp.txt_handleenddate));

					// 有効終了日に設定がない場合新規
					var edd = $.getInputboxValue($('#'+$.id_inp.txt_effectiveenddate));
					if (edd===null || edd==="" || edd===undefined) {
						$.setInputboxValue($('#'+$.id_inp.txt_effectiveenddate), '50/12/31');
						$.setInputboxValue($('#'+$.id_inp.txt_handleenddate), '50/12/31');
						$.setInputBoxEnableVariable($('#'+$.id_inp.txt_effectiveenddate), true);
					} else {
						$('#'+$.id.btn_del).linkbutton('disable').attr('disabled', false);
						$('#'+$.id.btn_del).linkbutton('enable').attr('enable', true).show();
						$("#disp_record_info").show();
					}
					$(".inp_hide1").show();

					// キャンセル・更新ボタン表示
					$('#'+$.id.btn_cancel).linkbutton('disable').attr('disabled', false);
					$('#'+$.id.btn_cancel).linkbutton('enable').attr('enable', true).show();
					$('#'+$.id.btn_upd).linkbutton('disable').attr('disabled', false);
					$('#'+$.id.btn_upd).linkbutton('enable').attr('enable', true).show();

					// 有効開始日にフォーカス
					var target = $.getInputboxTextbox($('#'+$.id_inp.txt_effectivestartdate));
					target.focus();
					//$('#'+$.id_inp.txt_effectivestartdate).focus();

					// ログ出力
					$.log(that.timeData, 'loaded:');
				}
			);
		},
		updValidation: function (){	// （必須）批准

			//コースマスタの編集を終了する。
			var row = $($.id.gridholder).datagrid("getSelected");
			var rowIndex = $($.id.gridholder).datagrid("getRowIndex", row);
			$($.id.gridholder).datagrid('endEdit',rowIndex);

			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform).form('validate');
			if(!rt){
				$.addErrState(that, $('.validatebox-invalid').eq(0), false);
				return rt;
			}

			// TODO: 背景を赤くする対応を追加
			//var msgid = that.checkInputboxFunc($.id_inp.txt_hstengpcd);
			var msgid = null;
			if(msgid !==null){
				$.showMessage(msgid);
				return false;
			}

			return rt;
		},
		updSuccess: function(id){	// validation OK時 の update処理
			var that = this;

			// 検索実行
			var sel_center_orr			= $.getInputboxValue($('#'+$.id.sel_center_orr));				// センター
			var sel_supplyno_orr		= $.getInputboxValue($('#'+$.id.sel_supplyno_orr));				// 便
			var txt_effectivestartdate	= $.getInputboxValue($('#'+$.id_inp.txt_effectivestartdate));	// 有効開始日
			var txt_effectiveenddate	= $.getInputboxValue($('#'+$.id_inp.txt_effectiveenddate));		// 有効終了日
			var txt_handleenddate		= $.getInputboxValue($('#'+$.id_inp.txt_handleenddate));		// 取扱終了日
			var hiddenUpddt				= $.getInputboxValue($('#hiddenUpddt'));						// 更新日付（排他チェック用）

			// 入力データ：コースマスタ情報一覧
			var targetRowsCourseMs = that.getGridData($.id.gridholder);

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$.appendMaskMsg();

			$.post(
				$.reg.jqgrid ,
				{
					report:		that.name,								// レポート名
					action:		$.id.action_update,						// 実行処理情報
					obj:		id,										// 実行オブジェクト
					CENTERCD	:sel_center_orr,						// センター
					SUPPLYNO	:sel_supplyno_orr,						// 便
					STD			:txt_effectivestartdate,				// 有効開始日
					EDD			:txt_effectiveenddate,					// 有効終了日
					HANDLEEDD	:txt_handleenddate,						// 取扱終了日
					UPDD		:hiddenUpddt,							// 更新日付（排他チェック用）
					DATA		:JSON.stringify(targetRowsCourseMs),	// 更新対象情報(コースマスタ)
					SENDBTNID	:that.sendBtnid,
					t:			(new Date()).getTime()
				},
				function(data){
					// 検索処理エラー判定
					if($.updError(id, data)){
						$.removeMaskMsg();
						return false;
					}

					var afterFunc = function(){
						// 初期化
						that.getEasyUI();
						that.jsonString = that.jsonTemp.slice(0);
						that.clear();
						that.changeReport(that.name, 'btn_return');
					};
					$.updNormal(data, afterFunc);
					$.removeMaskMsg();

					// ログ出力
					$.log(that.timeData, 'loaded:');
				}
			);
		},
		delValidation: function (){	// （必須）批准
			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = true;

			/*
			// 選択された配送店グループコード
			var row = $($.id.gridholder).datagrid("getSelected");

			if (row == undefined || row == null) {
				$.showMessage('E00008');
				return false;
			}

			// 店グループの入力が存在しなかった場合
			if (row.HSTENGPCD == '' || row.HSTENGPCD == null) {
				$.showMessage('E00008');
				return false;
			}
			*/

			return rt;
		},
		delSuccess: function(id){

			var that = this;

			// 検索実行
			var sel_center_orr			= $.getInputboxValue($('#'+$.id.sel_center_orr));				// センター
			var sel_supplyno_orr		= $.getInputboxValue($('#'+$.id.sel_supplyno_orr));				// 便
			var txt_shoridt				= $.getInputboxValue($('#'+$.id_inp.txt_shoridt));				// 基準日
			var txt_effectivestartdate	= $.getInputboxValue($('#'+$.id_inp.txt_effectivestartdate));	// 有効開始日
			var txt_effectiveenddate	= $.getInputboxValue($('#'+$.id_inp.txt_effectiveenddate));		// 有効終了日
			var txt_handleenddate		= $.getInputboxValue($('#'+$.id_inp.txt_handleenddate));		// 取扱終了日
			var hiddenUpddt				= $.getInputboxValue($('#hiddenUpddt'));						// 更新日付（排他チェック用）

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$.appendMaskMsg();

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,				// レポート名
					action:			$.id.action_delete,		// 実行処理情報
					obj:			id,						// 実行オブジェクト
					CENTERCD	:sel_center_orr,			// センター
					SUPPLYNO	:sel_supplyno_orr,			// 便
					SHORIDT		:txt_shoridt,				// 処理日付
					STD			:txt_effectivestartdate,	// 有効開始日
					EDD			:txt_effectiveenddate,		// 有効終了日
					HANDLEEDD	:txt_handleenddate,			// 取扱終了日
					UPDD		:hiddenUpddt,				// 更新日付（排他チェック用）
					t:			(new Date()).getTime()
				},
				function(data){
					// 検索処理エラー判定
					if($.delError(id, data)){
						$.removeMaskMsg();
						return false;
					}

					var afterFunc = function(){
						// 初期化
						that.clear();
						that.changeReport(that.name, 'btn_return');
					};
					$.delNormal(data, afterFunc);
					$.removeMaskMsg();

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
			// センター
			this.jsonTemp.push({
				id:		$.id.sel_center_orr,
				value:	$('#'+$.id.sel_center_orr).combobox('getValue'),
				text:	$('#'+$.id.sel_center_orr).combobox('getText')
			});
			// 便
			this.jsonTemp.push({
				id:		$.id.sel_supplyno_orr,
				value:	$('#'+$.id.sel_supplyno_orr).combobox('getValue'),
				text:	$('#'+$.id.sel_supplyno_orr).combobox('getText')
			});
			// 基準日
			this.jsonTemp.push({
				id:		$.id_inp.txt_shoridt,
				value:	$.getInputboxValue($('#'+$.id_inp.txt_shoridt)),
				text:	''
			});
		},
		setData: function(rows, opts){		// データ表示
			var that = this;
			if(rows.length > 0){
				$('#'+that.focusRootId).find('[col^=F]').each(function(){
					var col = $(this).attr('col');
					$.setInputboxValue($(this), rows[0][col]);
				});
			}
		},
		setCenter: function(that, reportno, id){		// データ表示
			var that = this;
			var idx = -1;
			var onChange=false;
			var onPanel=false;

			var check = $('#'+id).attr("check") ? JSON.parse('{'+$('#'+id).attr("check")+'}'): JSON.parse('{}');
			var datatyp = check.datatyp;

			var validType = $.fn.textbox.defaults.validType;

			if(check.datatyp==='lpadzero_text'){
				if(check.maxlen){ validType = 'intMaxLen['+check.maxlen+']'}
			}

			$('#'+id).combobox({
				panelWidth:250,
				url:$.reg.easy,
				validType:validType,
				required: true,
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
						e.preventDefault();
					},
					query: $.fn.combobox.defaults.keyHandler.query
				},
				onBeforeLoad:function(param){
					idx = -1;
					// 情報設定
					var txt_shoridt = $.getInputboxValue($('#'+$.id_inp.txt_shoridt));
					var json = [{REQUIRED:'REQUIRED',SHORIDT:txt_shoridt}];
					param.page		=	reportno;
					param.obj		=	id;
					param.sel		=	(new Date()).getTime();
					param.target	=	id;
					param.action	=	$.id.action_init;
					param.json		=	JSON.stringify(json);
					param.datatype	=	"combobox";
				},
				onLoadSuccess:function(data){
					// 選択値設定
					var val = null;
					if ($.inArray(id, that.initedObject) < 0){
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
					idx = 1;
					// ログ出力
					$.log(that.timeData, id+' init:');
					that.onChangeFlag = false;
					// 便コード
					that.tryLoadMethods('#'+$.id.sel_supplyno_orr);
				},
				onShowPanel: function(){
					$.setScrollComboBox(id);
					that.onChangeFlag=false;
					onChange=false;
					onPanel=true;
				},
				onHidePanel: function(){
					if (onChange){
						// 便コード
						that.tryLoadMethods('#'+$.id.sel_supplyno_orr);
					} else {
						that.onChangeFlag=true;
					}
					onChange=false;
					onPanel=false;
				},
				onChange:function(newValue, oldValue){
					if(idx > 0 && that.onChangeFlag){
						// 便コード
						that.tryLoadMethods('#'+$.id.sel_supplyno_orr);
					}
					onChange=true;
				}
			});
		},
		keyEventInputboxFunc:function(e, code, that, obj){

			// *** Enter or Tab ****
			if(code === 13 || code === 9){
				var newValue = obj.val();
				var id = $(obj).attr("orizinid");

				// センターコードマスタ存在チェック
				if (id===$.id.sel_center_orr) {

					if (newValue !== null && newValue !== '' && newValue !== undefined && !isNaN(Number(newValue))) {

						// マスタ存在チェック
						var param = {};
						param["KEY"] =  "MST_CNT";
						param["value"] = newValue;

						var chk_cnt = $.getInputboxData(that.name, $.id.action_check, $.id_inp.txt_tencd, [param]);
						if(chk_cnt==="" || chk_cnt==="0"){
							$.showMessage("EX1100",["センターコード","店舗基本マスタ"],function () { $.addErrState(that, $('#'+id),true) });
						} else {
							that.setComboValue(id,newValue);
							$.removeErrState();
						}

						that.setComboValue(id,newValue);
					}
				} else if (id===$.id.sel_supplyno_orr) {
					that.setComboValue(id,newValue);
				}
			}
		},
		setComboValue:function(id,newValue){
			var target = $('.inp_box [orizinid="'+id+'"]');

			if(id===$.id.sel_center_orr){

				if (newValue!=="" && !isNaN(Number(newValue))) {
					newValue = ('000'+newValue).substr(-3);
				} else {
					newValue = "";
				}
				$.setInputboxValue(target,newValue);
			} else if (id===$.id.sel_supplyno_orr) {
				$.setInputboxValue(target,newValue);
			}

			setTimeout(function(){
				$('#'+id).combobox('hidePanel');
			},300);
		},
		setSupplyNo: function(that, reportno, id){		// データ表示
			var that = this;
			var idx = -1;
			var onChange=false;
			var onPanel=false;

			var check = $('#'+id).attr("check") ? JSON.parse('{'+$('#'+id).attr("check")+'}'): JSON.parse('{}');
			var validType = $.fn.textbox.defaults.validType;

			if(check.maxlen){ validType = 'intMaxLen['+check.maxlen+']'}

			$('#'+id).combobox({
				validType:validType,
				panelWidth:250,
				url:$.reg.easy,
				required: true,
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
						e.preventDefault();
					},
					query: $.fn.combobox.defaults.keyHandler.query
				},
				onBeforeLoad:function(param){
					idx = -1;
					// 情報設定
					var sel_center	= $.getInputboxValue($('#'+$.id.sel_center_orr));
					var txt_shoridt	= $.getInputboxValue($('#'+$.id_inp.txt_shoridt));
					var json = [{
							REQUIRED:'REQUIRED',
							CENTER:sel_center,
							SHORIDT:txt_shoridt
					}];
					param.page		=	reportno;
					param.obj		=	id;
					param.sel		=	(new Date()).getTime();
					param.target	=	id;
					param.action	=	$.id.action_init;
					param.json		=	JSON.stringify(json);
					param.datatype	=	"combobox";
				},
				onLoadSuccess:function(data){
					// 選択値設定
					var val = null;
					if ($.inArray(id, that.initedObject) < 0){
						that.initedObject.push(id);
						var json = $.getJSONObject(that.jsonHidden, id);
						if(json && json.value!=""){
							val = new Array();
							for (var i=0; i<data.length; i++){
								if ($.inArray(data[i].VALUE, json.value)!=-1){
									val.push(data[i].VALUE);
								}
							}
							if (val.length===data.length || val.length===0){
								val = null;
							}
						}
					}
					idx = 1;
					// ログ出力
					$.log(that.timeData, id+' init:');
					that.onChangeFlag = false;
					$.ajaxSettings.async = true;
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
				onChange:function(newValue, oldValue){
					if(idx > 0 && that.onChangeFlag){
						// 上位変更時、下位更新は常に同期
						$.ajaxSettings.async = false;
						that.onChangeFlag = false;
					}
					onChange=true;
				}
			});
		},
		setEditableGrid: function(that, reportno, id){		// データ表示
			that.editRowIndex['gridholder'] = -1;
			var index = -1;
			$(id).datagrid({
				rownumbers:false,
				frozenColumns:[[
					{field:'F1',	title:'',	hidden:true},
					{field:'F2',	title:'',	hidden:true},
					{field:'F3',	title:'',	hidden:true},
					{field:'F4',	title:'',	hidden:true},
					{field:'F5',	title:'',	hidden:true},
					{field:'F6',	title:'',	hidden:true},
					{field:'F7',	title:'',	hidden:true},
					{field:'F8',	title:'',	width :40, halign:'center', align:'center'	,editor:{options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}},
				]],
				columns:[[
				]],
				onBeforeLoad:function(param){
					param.report = that.name;
				},
				onLoadSuccess:function(data){
					for (var i=1;i<=that.maxMergeCell;i++){
						// セルの縦マージ
						$.mergeVerticallCells($.id.gridholder, data, 'F'+i);
					}
				},
				onClickRow: function(index,field){$.clickEditableDatagridCell(that,'gridholder', index)},
				onBeginEdit:function(index,row){$.beginEditDatagridRow(that,'gridholder', index, row)},
				onEndEdit: function(index,row,changes){$.endEditDatagridRow(that, 'gridholder', index, row)},
				onAfterEdit: function(data){
					for (var i=1;i<=that.maxMergeCell;i++){
						// セルの縦マージ
						$.mergeVerticallCells($.id.gridholder, data, 'F'+i);
					}
				}
			});
		},
		extenxDatagridEditorIds:{},
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
			var sendMode = "";	// 遷移後に、前回検索条件を表示したい場合、""のまま、ここで設定した条件を表示したい場合"1"

			// タブ要素(a)取得
			var elems = $('#tabContent', window.parent.document).map(
				function(i,e) {
					return e;
				}).get();
			var href = elems[0].value.split(',');

			// JSON Object Clone ()
			var sendJSON = [];
			$.setJSONObject(sendJSON, 'sendBtnid', btnId, $('#'+btnId).attr("title"));				// 実行ボタン情報保持
			$.setJSONObject(sendJSON, 'reportYobi1', that.reportYobiInfo(), that.reportYobiInfo());	// 参照情報保持

			// 呼出別処理
			switch (btnId) {
			case $.id.btn_cancel:

				// 転送先情報
				index = 1;
				childurl = href[index];

				break;
			case $.id.btn_back:

				// 転送先情報
				childurl = parent.$('#hdn_menu_path').val();

				break;
			case "btn_return":
				// 転送先情報
				index = 1;
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
		getGridData: function (target){

			var targetRows= [];

			// 配送店グループ
			if(target===undefined || target===$.id.gridholder){
				var rowsCm= $($.id.gridholder).datagrid('getRows');
				for (var i=0; i<rowsCm.length; i++){
					if(rowsCm[i]["F1"] == "" || rowsCm[i]["F1"] == null ){

					}else{
						var rowDate = {};

						for (var j=0; j<Object.keys(rowsCm[i]).length; j++) {

							var filed = "F"+ (j+1);
							var val = rowsCm[i][filed];

							rowDate[filed]=val;
						}

						targetRows.push(rowDate);
					}
				}
			}
			return targetRows;
		},
		changeInputboxFunc:function(that, id, newValue, obj){

			var that = this;
			var parentObj = $('#'+that.focusRootId);
			if(id+"_"===obj.attr('id') && that.focusGridId!==""){
				parentObj = $('#'+that.focusGridId).datagrid('getPanel');
			}

			if (id===$.id_inp.txt_shoridt) {
				that.tryLoadMethods('#'+$.id.sel_center_orr);
			}

			// TODO: 背景を赤くする対応を追加
			//var msgid = that.checkInputboxFunc(id,newValue);
			var msgid = null;
			if(msgid !==null){
				$.showMessage(msgid);
				return false;
			}
		},
		checkInputboxFunc: function(id, newValue){

			var that = this;

			// 店グループコード重複チェック
			if (id===$.id_inp.txt_hstengpcd) {

				if (newValue === null || newValue === '' || newValue === undefined) {
					return null;
				}

				// 配送店グループ一覧
				var hstgps			= [];
				var hstengpcd		= "";
				var errFlg			= true; // グリッドの入力チェックに使用
				var targetRowsHstgp	= $($.id.gridholder).datagrid('getRows');

				for (var i=0; i<targetRowsHstgp.length; i++){

					// 配送店グループコードを格納
					hstengpcd = targetRowsHstgp[i]["HSTENGPCD"];

					// 配送店グループの情報を必ず1行は入力
					if ((errFlg && (hstengpcd != '' && hstengpcd != null)) || (newValue !== null && newValue !== '' && newValue !== undefined)) {
						errFlg = false;
					}

					// エリア区分が0の場合店舗部門マスタの存在チェック(ここでは桁数のチェックのみ)
					if ($("input[name="+$.id.rad_areakbn+"]:checked").val() === '0') {
						if (parseInt(hstengpcd) > 99) {
							return 'E11041';
						}

					// エリア区分が1の場合数値チェック(10番以上での登録)
					} else {
						if (parseInt(hstengpcd) < 10) {
							return 'E11038';
						}
					}

					if (i===that.editRowIndex[$.id.gridholder]) {
						hstgps.push(newValue);
					} else {
						if (hstengpcd != null && hstengpcd != '' && hstengpcd !== undefined) {
							// 重複チェック用
							hstgps.push(hstengpcd);
						}
					}
				}

				// 店グループの入力が存在しなかった場合
				if (errFlg) {
					return 'EX1017';
				}

				// 重複チェック
				var hstgps_ = hstgps.filter(function (element, index, self) { return self.indexOf(element)*1 === index; });
				if(hstgps.length !== hstgps_.length){
					return 'E11141';
				}
			}
			return null;
		},
	} });
})(jQuery);
