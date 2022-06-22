/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_ReportYH001',			// （必須）レポートオプションの確認
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
		gridData:[],						// 検索結果
		gridTitle:[],						// 検索結果
		data:[],							// 基本入力情報
		grd_shohin_data:[],					// グリッド情報:店舗一覧
		scrollToId:[],						// 戻り時にフォーカス行を指定したい場合(gridholder以外)は指定
		beforRowIndex: -1,					// 前回編集行Index
		updConfirmMsg:"",
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

			// 処理日付取得
			$.getsetInputboxData(reportno, $.id.txt_shoridt, [{}], $.id.action_init);

			var isUpdateReport = true;

			// 初期検索可能
			that.onChangeReport = true;

			// チェックボックス設定
			$.setCheckboxInit2(that.jsonHidden, $.id.chk_del, that);
			$.setCheckboxInit2(that.jsonHidden, $.id.chk_sel, that);
			$.setCheckboxInit2(that.jsonHidden, 'f1', that);

			//$('#'+$.id.chk_del).change(function() {
			$('#'+$.id_inp.txt_kkkcd).change(function() {
				$($.id.hiddenChangedIdx+suffix).val("1");
			});

			var count = 2;
			// 入力テキストボックス系
			var inputbox = Object.getOwnPropertyNames($.id_inp);
			for ( var sel in inputbox ) {
				if($('#'+$.id_inp[inputbox[sel]]).length > 0){
					$.setInputbox(that, reportno, $.id_inp[inputbox[sel]], isUpdateReport);
					count++;
				}
			}

			// 編集可能データグリッドの共通処理設定
			// 編集エディターの機能拡張（非表示で参照用のオブジェクトが準備してある前提、参照オブジェクトはグリッドのフィールド名を元に判断）
			$.extendDatagridEditor();

			// 店舗一覧
			that.setEditableGrid(that, reportno, $.id.grd_shohin);


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

			/*var reportYobi1 = $.getJSONValue(that.jsonHidden, "reportYobi1");
			if(!reportYobi1){
				reportYobi1 = $('#reportYobi1').val();
			}
			$('#reportYobi1').val(reportYobi1);*/

			// 当帳票を「参照」で開いた場
			var sendBtnid = $.getJSONValue(that.jsonHidden, "sendBtnid");
			if(sendBtnid && sendBtnid.length > 0){
				$.reg.search = true;
			}

			if(that.reportYobiInfo()==='0'){
				$.initReportInfo("YH001", "予約発注　新規　企画情報", "新規");
				$("#"+$.id.btn_new).linkbutton('disable');
				$("#"+$.id.btn_new).attr('tabindex', -1).hide();
				$("#"+$.id.btn_del).linkbutton('disable');
				$("#"+$.id.btn_del).attr('tabindex', -1).hide();
				$("#"+$.id.btn_sel_change).linkbutton('disable');
				$("#"+$.id.btn_sel_change).attr('tabindex', -1).hide();
				$("#gf").hide();
				$("#"+$.id.grd_shohin).attr('tabindex', -1);
				$("#disp_record_info").hide();
				$("#inp_kkkcd").attr('tabindex', -1).hide();
				$.setInputBoxDisable($("#"+$.id_inp.txt_kkkcd));

			}else if(that.reportYobiInfo()==='2'){
				$.initReportInfo("YH001", "予約発注　変更　企画情報", "変更");
				$("#"+$.id.btn_new).linkbutton('disable');
				$("#"+$.id.btn_new).attr('tabindex', -1).hide();
				$("#"+$.id.btn_sel_change).linkbutton('disable');
				$("#"+$.id.btn_sel_change).attr('tabindex', -1).hide();
				$("#gf").hide();
				$("#"+$.id.grd_shohin).attr('tabindex', -1);
				$.setInputBoxDisable($("#"+$.id_inp.txt_kkkcd));

			}else if(that.reportYobiInfo()==='1'){
				$.initReportInfo("YH001", "予約発注　新規・変更　商品一覧", "一覧");
				$.setInputBoxDisable($("#"+$.id_inp.txt_kkkcd));
				$.setInputBoxDisable($("#"+$.id_inp.txt_kkkkm));
				$.setInputBoxDisable($("#"+$.id_inp.txt_nnstdt));
				$.setInputBoxDisable($("#"+$.id_inp.txt_nneddt));
				$('#'+$.id.btn_sel_change).on("click", $.pushChangeReport);
				$('#'+$.id.btn_new).on("click", $.pushChangeReport);

				$('#reportYobi1').val('3')
			}

			// 当帳票を「参照」で開いた場合
			/*if(that.reportYobiInfo()==='1'){
				$.initReportInfo("YH100", "予約発注　参照　企画一覧", "参照");
				$("#"+$.id.btn_new).linkbutton('disable');
				$("#"+$.id.btn_new).attr('tabindex', -1).hide();
				$("#"+$.id.btn_del).linkbutton('disable');
				$("#"+$.id.btn_del).attr('tabindex', -1).hide();
				$("#"+$.id.btn_sel_change).linkbutton('disable');
				$("#"+$.id.btn_sel_change).attr('tabindex', -1).hide();

			}else if(that.reportYobiInfo()==='2'){
				$.initReportInfo("YH200", "予約発注　修正　企画一覧", "修正");
				$("#"+$.id.btn_new).linkbutton('disable');
				$("#"+$.id.btn_new).attr('tabindex', -1).hide();
				$("#"+$.id.btn_del).linkbutton('disable');
				$("#"+$.id.btn_del).attr('tabindex', -1).hide();
				$("#"+$.id.btn_sel_change).linkbutton('disable');
				$("#"+$.id.btn_sel_change).attr('tabindex', -1).hide();

			}else if(that.reportYobiInfo()==='0'){
				$.initReportInfo("YH000", "予約発注　新規・変更　企画一覧", "一覧");
				// 各種遷移ボタン
				$('#'+$.id.btn_new).on("click", $.pushChangeReport);
				$('#'+$.id.btn_sel_change+'_shn').on("click", $.pushChangeReport);
				$('#'+$.id.btn_sel_change).on("click", $.pushChangeReport);

			}*/

			// 各種遷移ボタン
			$('#'+$.id.btn_new).on("click", $.pushChangeReport);
			$('#'+$.id.btn_copy).on("click", $.pushChangeReport);
			$('#'+$.id.btn_cancel).on("click", $.pushChangeReport);

			// 変更
			//$.setInputBoxDisable($('#'+$.id.chk_del));
			$.setInputBoxDisable($("#txt_countItem"));

			/*if(that.sendBtnid===$.id.btn_new || that.sendBtnid===$.id.btn_sel_copy){
				$.initReportInfo("IT022", "商品店グループ　新規", "新規");
				$("#"+$.id.btn_del).linkbutton('disable');
				$("#"+$.id.btn_del).attr('tabindex', -1).hide();

			}else if(that.sendBtnid===$.id.btn_sel_change){
				$.initReportInfo("IT023", "商品店グループ　変更", "変更");
				$.setInputBoxDisable($("#"+$.id_inp.txt_tengpcd));
				$.setInputBoxDisable($("#"+$.id_mei.kbn140));
				$.setInputBoxDisable($("#"+$.id.SelBumon));
				$.setInputBoxDisable($("#"+$.id.rad_areakbn));
			}*/
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
			// EasyUI のフォームメソッド 'validate' 実施
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
			var txt_kkkcd		= $.getJSONObject(this.jsonString, $.id_inp.txt_kkkcd).value;	// 店グループ

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$.appendMaskMsg();

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,		// レポート名
					KKKCD:			txt_kkkcd,
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

					// グリッド再描画
					$('#'+$.id.grd_tenpo).datagrid('reload');

					// 状態保存
					$.saveState2(reportno, that.getJSONString());

					// 基本入力初期値保持
					var Data = that.getGridData('data');
					that.setGridData(Data, 'data');

					// ログ出力
					$.log(that.timeData, 'loaded:');
				}
			);
		},
		updConfirm: function(func){	// validation OK時 の update処理
			var that = this;
			var msgId = that.updConfirmMsg;
			$.showMessage(msgId, undefined, func);
		},
		updValidation: function (){	// （必須）批准
			var that = this;

			//配送点グループグリッドの編集を終了する。
			var row = $('#'+$.id.grd_shohin).datagrid("getSelected");
			var rowIndex = $('#'+$.id.grd_shohin).datagrid("getRowIndex", row);
			$('#'+$.id.grd_shohin).datagrid('endEdit',rowIndex);


			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform).form('validate');
			if(!rt){
				$.addErrState(that, $('.validatebox-invalid').eq(0), false);
				return rt;
			}

			if(that.reportYobiInfo()==='3'){
				// 商品一覧の時

				// 店舗入力開始日
				var shoridt = $('#'+$.id.txt_shoridt).val();
				var rows = $('#'+ $.id.grd_shohin).datagrid('getRows')
				for(var i = 0; i < rows.length; i++){
					if(rows[i].DEL == '1'){
						// 削除フラグ= '1'のとき
						var TENISTDT  = rows[i].F12;		 // 店舗入力開始日

						var sdt = $.convDate(TENISTDT, true);
						var edt = $.convDate(shoridt , true);
						if(sdt.getTime() <= edt.getTime()){	// 店舗入力開始日 <= 処理日付
							$.showMessage("E20209");
							return false;
						}
					}
				}
			}else{

				var nnstdt = $('#'+$.id_inp.txt_nnstdt).numberbox('getValue');	// 納入開始日
				var msgid = that.checkInputboxFunc($.id_inp.txt_nnstdt, nnstdt , '');
				if(msgid !==null){
					$.showMessage(msgid);
					return false;
				}

				var nneddt = $('#'+$.id_inp.txt_nneddt).numberbox('getValue');	// 納入終了日
				var msgid = that.checkInputboxFunc($.id_inp.txt_nneddt, nneddt , '');
				if(msgid !==null){
					$.showMessage(msgid);
					return false;
				}
			}

			if(!rt){
				$.showMessage('E00001');
				return rt;
			}

			// 変更の場合確認メッセージを表示
			that.updConfirmMsg = "W00001";

			return rt;
		},
		updSuccess: function(id){	// validation OK時 の update処理
			var that = this;

			// 変更行情報取得
			var targetDatas = [{}];
			$('#'+that.focusRootId).find('[col^=F]').each(function(){
				var col = $(this).attr('col');
				targetDatas[0][col] = $.getInputboxValue($(this));
			});

			// 商品一覧のデータを取得
			var targetDatasShn = that.getMergeGridDate($.id.grd_shohin);

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
//					IDX:			$($.id.hiddenChangedIdx).val(),	// 更新対象Index
					DATA_SHN:		JSON.stringify(targetDatasShn),	// 更新対象情報
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

			var txt_tengpcd		= $.getJSONObject(this.jsonString, $.id_inp.txt_tengpcd).value;	// 店グループ
			var sel_gpkbn		= $.getJSONObject(this.jsonString, $.id_mei.kbn140).value;		// グループ区分
			var SelBumon		= $.getJSONObject(this.jsonString, $.id.SelBumon).value;		// 部門
			var rad_areakbn		= $("input[name="+$.id.rad_areakbn+"]:checked").val();			// グループ区分

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
					TENGPCD:		txt_tengpcd,
					BUMON:			SelBumon,
					GPKBN:			sel_gpkbn,
					AREAKBN:		rad_areakbn,
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
			// 企画No
			this.jsonTemp.push({
				id:		$.id_inp.txt_kkkcd,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_kkkcd),
				text:	''
			});
			// 企画名称
			this.jsonTemp.push({
				id:		$.id_inp.txt_kkkkm,
				value:	$('#'+$.id_inp.txt_kkkkm).textbox('getValue'),
				text:	$('#'+$.id_inp.txt_kkkkm).textbox('getText')
			});

			/*// 納入開始日
			this.jsonTemp.push({
				id:		$.id_inp.txt_nnstdt,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_nnstdt),
				text:	''
			});
			// 納入終了日
			this.jsonTemp.push({
				id:		$.id_inp.txt_nneddt,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_nneddt),
				text:	''
			});*/

			// 納入開始日
			this.jsonTemp.push({
				id:		$.id_inp.txt_nnstdt,
				value:	$('#'+$.id_inp.txt_nnstdt).textbox('getValue'),
				text:	$('#'+$.id_inp.txt_nnstdt).textbox('getText')
			});
			// 納入終了
			this.jsonTemp.push({
				id:		$.id_inp.txt_nneddt,
				value:	$('#'+$.id_inp.txt_nneddt).textbox('getValue'),
				text:	$('#'+$.id_inp.txt_nneddt).textbox('getText')
			});

			// 表示用：企画名称
			/*this.jsonTemp.push({
				id:		'',
				value:	$('#').textbox('getValue'),
				text:	$('#').textbox('getText')
			});
			// 表示用：納入期間
			this.jsonTemp.push({
				id:		'',
				value:	$('#').textbox('getValue'),
				text:	$('#').textbox('getText')
			});*/
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
		setEditableGrid: function(that, reportno, id){		// データ表示
			var that = this;
			var init = true;
			// ページサイズ定義取得
			var pageList = $.fn.pagination.defaults.pageList;
			var pageSize = $.getJSONValue(this.jsonHidden, $.id.pageSize);
			var cstyler =function(value,row,index){return 'color: red;font-weight: bold;';};
			var dformatter =function(value){
				var add20 = value && value.length===6;
				var addweek = 1;	// フラグ用仮パラメータ(週まで表示したい際に使用)
				return $.getFormatDt(value, add20, addweek);
			};
			var cstyler2=function(value,row,index){return 'color: red;font-weight: bold;background-color:#f5f5f5;';};
			var cformatter =function(value,row,index){return value && value===$.id.value_on?$.id.text_on:$.id.text_off;};
			var wformatter =function(value,row,index){return $.getFormatWeek(row[this.field.replace("_W", "")]);};
			var iformatter = function(value,row,index){ return $.getFormat(value, '#,##0');};

			that.scrollToId[0] = id;

			if (pageSize==="") pageSize=$.fn.pagination.defaults.pageSize;
			that.editRowIndex[id] = -1;
			var index = -1;
			$('#'+id).datagrid({
				nowrap: true,
				border: true,
				striped: true,
				collapsible:false,
				remoteSort: true,
				rownumbers:true,
				singleSelect:true,
				checkOnSelect:false,
				selectOnCheck:false,
				frozenColumns:[[
							{field:'SEL',		title:'選択',				editor:{type:'checkbox'}, styler:cstyler, formatter:cformatter,	width:  50,halign:'center',align:'center'},
							{field:'DEL',		title:'削除',				editor:{type:'checkbox'}, styler:cstyler, formatter:cformatter,	width:  50,halign:'center',align:'center'},
							{field:'F1',		title:'納入日セット', 		editor:{type:'checkbox'}, styler:cstyler, formatter:cformatter,	width:  50,halign:'center',align:'center'},
							{field:'F2',		title:'カタログ番号',		width:  60,halign:'center',align:'left'},
							{field:'F3',		title:'商品コード',			width: 100,halign:'center',align:'left'},
							{field:'F4',		title:'商品名称（漢字）',	width: 300,halign:'center',align:'left'},
							]],
				columns:[[
							{field:'F5',		title:'発注日',				width: 100,halign:'center',align:'left',formatter:dformatter},
							{field:'F6',		title:'受付期間',			width: 200,halign:'center',align:'left',formatter:dformatter},
							{field:'F7',		title:'店舗入力期間',		width: 200,halign:'center',align:'left'},
							{field:'F8',		title:'予定数',				width: 100,halign:'center',align:'right',formatter:iformatter},
							{field:'F9',		title:'限度数',				width: 100,halign:'center',align:'right',formatter:iformatter},
							]],
				url:$.reg.easy,
				onBeforeLoad:function(param){
					index = -1;
					$.setInputBoxDisable($('#'+$.id.chk_sel+"_"));
					var values = {};
					var kkkcd			 = $('#'+$.id_inp.txt_kkkcd).numberbox('getValue');		// 配送グループコード

					values["callpage"]	 = $($.id.hidden_reportno).val()						// 呼出元レポート名
					values["KKKCD"]	 = kkkcd													// 企画No

					var json = [values];
					// 情報設定
					param.page		=	reportno;
					param.obj		=	id;
					param.sel		=	(new Date()).getTime();
					param.target	=	id;
					param.action	=	$.id.action_init;
					param.json		=	JSON.stringify(json);
					param.datatype	=	"datagrid";
				},
				onLoadSuccess:function(data){
					// 各グリッドの値を保持する
					var gridData = that.getGridData(id);
					that.setGridData(gridData, id);

					// チェックボックスの設定
					$.initCheckboxCss($('#'+id).datagrid('getPanel').find('.datagrid-body'));

					var getRowIndex = $.getJSONValue(that.jsonHidden, "scrollToIndex_"+id);
					if(getRowIndex !== ""){
						$('#'+id).datagrid('scrollTo', {
							index: getRowIndex,
							callback: function(index){
								$('#'+id).datagrid('selectRow', index);
								$('#'+id).datagrid('beginEdit', index);
							}
						});
					}
				},
				onSelect:function(index,row){
					//選択をチェックする。
					row.SEL = $.id.value_on;
					if(that.beforRowIndex !== -1 && that.beforRowIndex !== index){
						$('#'+id).datagrid('unselectRow',that.beforRowIndex)
					}
					that.beforRowIndex = index;
				},
				onUnselect:function(index,row){
					if(row){
						// グリッドの入力を終了する。
						$('#'+$.id.grd_shohin).datagrid('endEdit',index);

						// 選択チェック解除
						row.SEL = $.id.value_off;
						$('#'+$.id.grd_shohin).datagrid('refreshRow',index);
					}
				},
				onClickRow: function(index,field){
					//$('#'+$.id.grd_shohin).datagrid('selectRow',index);
					$.clickEditableDatagridCell(that,id, index)
				},
				onClickCell:function(rowIndex, field, value){
					// 列名保持
					that.columnName = field;
				},
				onBeginEdit:function(index,row){
					$.setInputBoxDisable($('#'+$.id.chk_sel+"_"));
					$.setInputBoxDisable($('#chk_f1_'));
					$.beginEditDatagridRow(that,id, index, row)
				},
				onEndEdit: function(index,row,changes){
					$.endEditDatagridRow(that, id, index, row)
				},
				onAfterEdit: function(index,row,changes){
					// チェックボックスの再追加（EndEdit時に削除されるため）
					$.afterEditAddCheckbox($('#'+id).datagrid('getPanel').find("[datagrid-row-index='"+index+"']"));
				}
			});
		},
		getGridData: function (target){
			var that = this;

			var data = {};
			var targetRows= [];

			// 商品一覧
			if(target===undefined || target===$.id.grd_shohin){
				var rows	 = $('#'+$.id.grd_shohin).datagrid('getRows');			// 商品一覧
				var kkkcd	 = $('#'+$.id_inp.txt_kkkcd).numberbox('getValue');		// 企画No

				for (var i=0; i<rows.length; i++){
					var rowDate = {
							F1	 : kkkcd,
							F2	 : rows[i]["F3"].replace("-", ""),
							F3	 : rows[i]["DEL"],
					};
					targetRows.push(rowDate);
				}
				data[$.id.grd_shohin] = targetRows;
			}
			return data;
		},
		getMergeGridDate: function(target){
			// 保持したデータと入力データ比較を比較する。
			var that = this;

			var newrows = that.getGridData(target)[target];		// 変更データ
			var oldrows = [];
			var targetRows= [];


			if(target===undefined || target===$.id.grd_shohin){
				// 商品一覧
				oldrows = that.grd_shohin_data
				for (var i=0; i<newrows.length; i++){
					if((oldrows[i]['F3'] ? oldrows[i]['F3'] : "") !== (newrows[i]['F3'] ? newrows[i]['F3'] : "")
							|| (oldrows[i]['F4'] ? oldrows[i]['F4'] : "") !== (newrows[i]['F4'] ? newrows[i]['F4'] : "")){
						if(newrows[i]["F1"] && newrows[i]["F2"]){
							var rowDate = {
									F1	 : newrows[i]["F1"],
									F2	 : newrows[i]["F2"],
									F3	 : newrows[i]["F3"],
							};
						}
						if(rowDate){
							targetRows.push(rowDate);
						}
					}
				}
			}
			return targetRows;
		},
		setGridData: function (data, target){
			var that = this;

			// 商品一覧
			if(target===undefined || target===$.id.grd_shohin){
				that.grd_shohin_data =  data[$.id.grd_shohin];
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
			$.setJSONObject(sendJSON, 'sendBtnid', btnId, $('#'+btnId).attr("title"));						// 実行ボタン情報保持
			//$.setJSONObject(sendJSON, 'reportYobi1', that.reportYobiInfo(), that.reportYobiInfo());			// 参照情報保持
			$.setJSONObject(sendJSON, 'callpage', that.name, that.name);									// 呼出し元レポート情報

			// 戻る実行時用に現在の画面情報を保持する
			var states = $.getBackBaseJSON(that);
			// 各種グリッド情報を設定
			var newrepinfos = $.getBackJSON(that, states);
			$.setJSONObject(sendJSON, 'repinfo', newrepinfos, '');

			// 選択行
			var row = $('#'+$.id.grd_shohin).datagrid("getSelected");

			// 呼出別処理
			switch (btnId) {
			case $.id.btn_new:
				// 転送先情報
				index = 7;
				childurl = href[index];
				sendMode = 1;

				var txt_kkkcd		= $.getJSONObject(this.jsonString, $.id_inp.txt_kkkcd).value;	// 店グループ

				$.setJSONObject(sendJSON, $.id_inp.txt_kkkcd, txt_kkkcd, txt_kkkcd);				// 企画No

				break;
			case $.id.btn_sel_change:

				if(!row){
					$.showMessage('E00008');
					return false;
				}

				// 転送先情報
				index = 7;
				childurl = href[index];
				sendMode = 1;

				var txt_kkkcd		= $.getJSONObject(this.jsonString, $.id_inp.txt_kkkcd).value;	// 店グループ

				$.setJSONObject(sendJSON, $.id_inp.txt_kkkcd, txt_kkkcd, txt_kkkcd);				// 企画No
				$.setJSONObject(sendJSON, $.id_inp.txt_shncd, row.F3, row.F3);						// 企画No

				break;
			case $.id.btn_cancel:
			case $.id.btn_back:
				// 転送先情報
				sendMode = 2;

				index = 1;
				childurl = href[index];

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


				// 納入開始日、納入終了日
				if(id===$.id_inp.txt_nnstdt || id===$.id_inp.txt_nneddt){
					if(id===$.id_inp.txt_nnstdt){
						if($('#'+$.id_inp.txt_nneddt).textbox('getValue')){
							msgid = that.checkInputboxFunc(id, newValue, '');
						}
					}else if(id===$.id_inp.txt_nneddt){
						if($('#'+$.id_inp.txt_nnstdt).textbox('getValue')){
							msgid = that.checkInputboxFunc(id, newValue, '');
						}
					}
				}
				if(msgid !==null){
					$.showMessage(msgid, undefined, func_focus );
					return false;
				}

				// グリッド編集系
				if(that.focusGridId!==undefined && that.editRowIndex[that.focusGridId] > -1){
					// その他の入力項目のエラーチェック
					var moyskbn = $.id.value_moykbn_r*1;
					if(that.focusGridId === $.id.grd_moycd_s){
						moyskbn = $.id.value_moykbn_s*1;
					}else if(that.focusGridId === $.id.grd_moycd_t){
						moyskbn = $.id.value_moykbn_t*1;
					}
					var row = $('#'+that.focusGridId).datagrid('getRows')[that.editRowIndex[that.focusGridId]];
					msgid = that.checkInputboxFunc(id, newValue, moyskbn, undefined, row["UPDDT"]===undefined);
					if(msgid !==null){
						$.showMessage(msgid, undefined, func_focus );
						return false;
					}
				}
			}
		},
		// IDとvalueでチェック処理を実施
		checkInputboxFunc:function(id, newValue, kbn, record, isNew){
			var that = this;
			var sdt, edt;

			// 納入期間
			if(id===$.id_inp.txt_nnstdt||id===$.id_inp.txt_nneddt){

				var htdt	 = $.getInputboxValue($('#'+$.id_inp.txt_htdt)); 			// 発注日(企画期間内最大値)
				var nndt_min = $.getInputboxValue($('#'+$.id_inp.txt_nndt+'_min')); 	// 納入日(企画期間内最小値)
				var nndt_max = $.getInputboxValue($('#'+$.id_inp.txt_nndt+'_max')); 	// 納入日(企画期間内最大値)
				var shoridt	 = $('#'+$.id.txt_shoridt).val();

				// 納入期間開始日
				if(id===$.id_inp.txt_nnstdt){
					// 販売期間終了日との比較
					var nneddt	 = $('#'+$.id_inp.txt_nneddt).numberbox('getValue');

					// 納入開始日 <= 処理日付
					sdt = $.convDate(newValue, true);
					edt = $.convDate(shoridt, true);
					if(sdt.getTime() <= edt.getTime()){	// 開始日付が処理日付より未来の場合
						return "E20034";
					}

					// 納入開始日 < 発注日
					if(htdt && htdt !== ''){
						sdt = $.convDate(newValue, true);
						edt = $.convDate(htdt, true);
						if(sdt.getTime() <= edt.getTime()){
							return "E20226";
						}
					}

					// 納入日(企画期間内最小値) < 納入開始日
					if(nndt_min && nndt_min !== ''){
						sdt = $.convDate(nndt_min, true);
						edt = $.convDate(newValue, true);
						if(sdt.getTime() < edt.getTime()){
							return "E20022";
						}
					}

					// 入力期間チェック準備
					sdt = $.convDate(newValue, true);
					edt = $.convDate(nneddt, true);
				}

				// 納入期間終了日
				if(id===$.id_inp.txt_nneddt){
					// 販売期間終了日との比較
					var nnstdt;
					nnstdt = $('#'+$.id_inp.txt_nnstdt).numberbox('getValue');

					// 納入終了日 < 納入日(企画期間内最大値)
					if(nndt_max && nndt_max !== ''){
						sdt = $.convDate(newValue, true);
						edt = $.convDate(nndt_max, true);
						if(sdt.getTime() < edt.getTime()){
							return "E20270";
						}
					}

					sdt = $.convDate(nnstdt, true);
					edt = $.convDate(newValue , true);
					if(sdt.getTime() > edt.getTime()){	// 期間が逆の場合
						return "E20301";
					}
				}

				// 期間日数チェック
				if(sdt && edt){
					var days = $.getDateDiffDay(sdt, edt) + 1;
					if(days > 10){
						return "E20068";
					}
				}
			}
			return null;
		},
	} });
})(jQuery);