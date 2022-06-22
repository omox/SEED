/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_ReportRP006',			// （必須）レポートオプションの確認
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
		dedefaultObjNum:	9,	// 初期化オブジェクト数
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

			// 編集可能データグリッドの共通処理設定
			// 編集エディターの機能拡張（非表示で参照用のオブジェクトが準備してある前提、参照オブジェクトはグリッドのフィールド名を元に判断）
			$.extendDatagridEditor();

			// 初期検索可能
			that.onChangeReport = true;

//			// 名称マスタ参照系
//			var meisyoSelect = Object.getOwnPropertyNames($.id_mei);
//			for ( var sel in meisyoSelect ) {
//				if($('#'+$.id_mei[meisyoSelect[sel]]).length > 0){
//					$.setMeisyoCombo(that, reportno, $.id_mei[meisyoSelect[sel]], isUpdateReport);
//					count++;
//				}
//			}
			// 入力テキストボックス系
			var inputbox = Object.getOwnPropertyNames($.id_inp);
			for ( var sel in inputbox ) {
				if($('#'+$.id_inp[inputbox[sel]]).length > 0){
					$.setInputbox(that, reportno, $.id_inp[inputbox[sel]], isUpdateReport);
				}
			}

			that.setEditableGrid(that, reportno, $.id.grd_rtptntenbetubrt+'_list');
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

			var callpage = $.getJSONValue(that.jsonHidden, "callpage");

			if(callpage=='Out_ReportST011'){
				// 実績率の場合
				$("#RTPTN").hide();
				$("#"+$.id.btn_cancel).linkbutton('disable');
				$("#"+$.id.btn_cancel).attr('tabindex', -1).hide();
				$("#"+$.id.btn_upd).linkbutton('disable');
				$("#"+$.id.btn_upd).attr('tabindex', -1).hide();
				$("#"+$.id.btn_del).linkbutton('disable');
				$("#"+$.id.btn_del).attr('tabindex', -1).hide();
				$.setInputBoxDisable($("#"+$.id_inp.txt_jrtptnbmncd));
				$.setInputBoxDisable($("#"+$.id_inp.txt_jrtptnno));
				$.setInputBoxDisable($("#"+$.id_inp.txt_jrtptnkn));
				$.initReportInfo("RP006", "実績率パターンマスタ　店別分配率");

			}else  {
				// 通常率の場合
				$("#JRTPTN").hide();
				$.setInputBoxDisable($("#"+$.id_inp.txt_jrtptnbmncd));
				$.setInputBoxDisable($("#"+$.id_inp.txt_jrtptnno));
				$.setInputBoxDisable($("#"+$.id_inp.txt_jrtptnkn));

				if(that.sendBtnid==$.id.btn_new){
					$('#'+$.id.btn_cancel).on("click", $.pushChangeReport);
					$("#"+$.id.btn_del).linkbutton('disable');
					$("#"+$.id.btn_del).attr('tabindex', -1).hide();
					$.setInputBoxDisable($("#"+$.id_inp.txt_rtptnbmncd));
					$.initReportInfo("RP006", "通常率パターンマスタ　店別分配率　新規", "新規");

				}else if (that.sendBtnid==$.id.btn_sel_change) {
					$('#'+$.id.btn_cancel).on("click", $.pushChangeReport);
					$.setInputBoxDisable($("#"+$.id_inp.txt_rtptnbmncd));
					$.setInputBoxDisable($("#"+$.id_inp.txt_rtptnno));
					$.setInputBoxDisable($("#"+$.id_inp.txt_rtptnkn));
					$.initReportInfo("RP006", "通常率パターンマスタ　店別分配率　変更", "変更");

				}else if (that.sendBtnid==$.id.btn_sel_refer) {
					$("#"+$.id.btn_upd).linkbutton('disable');
					$("#"+$.id.btn_upd).attr('tabindex', -1).hide();
					$("#"+$.id.btn_del).linkbutton('disable');
					$("#"+$.id.btn_del).attr('tabindex', -1).hide();
					$.setInputBoxDisable($("#"+$.id_inp.txt_rtptnbmncd));
					$.setInputBoxDisable($("#"+$.id_inp.txt_rtptnno));
					$.setInputBoxDisable($("#"+$.id_inp.txt_rtptnkn));
					$.setInputBoxDisable($("#"+$.id_inp.txt_bunpairt));
					$.initReportInfo("RP006", "通常率パターンマスタ　店別分配率　参照", "参照");
				}
			}

			// 各種遷移ボタン
			$('#'+$.id.btn_cancel).on("click", $.pushChangeReport);
			$('#'+$.id.btn_back).on("click", $.pushChangeReport);

//			// 変更
//			$($.id.hiddenChangedIdx).val('');
		},
		initCondition: function (){	// 条件初期値セット
			var that = this;
			// 初期化項目
		},
		clear:function(){
			// 隠し情報初期化
			$($.id.hiddenChangedIdx).val("");						// 変更行Index
//			// グリッド初期化
//			this.success(this.name, false);
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
			var txt_bmncd		= $.getJSONObject(this.jsonString, $.id_inp.txt_bmncd).value;		// 部門
			var txt_rtptnno		= $.getJSONObject(this.jsonString, $.id_inp.txt_rtptnno).value;		// 通常率パターンNo.
			var rad_wwmmflg		= $.getJSONObject(this.jsonString, $.id.rad_wwmmflg).value;			// 週月フラグ
			var txt_yymm		= $.getJSONObject(this.jsonString, $.id_inp.txt_yymm).value;		// 年月(週No.)
			var txt_daicd		= $.getJSONObject(this.jsonString, $.id_inp.txt_daicd).value;		// 大分類
			var txt_chucd		= $.getJSONObject(this.jsonString, $.id_inp.txt_chucd).value;		// 中分類
			var rad_jissekibun	= $.getJSONObject(this.jsonString, $.id.rad_jissekibun).value;		// 実績分類

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$.appendMaskMsg();

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,		// レポート名
					BMNCD:			txt_bmncd,		// 部門
					RTPTNNO:		txt_rtptnno,	// 通常率パターンNo.
					WWMMFLG:		rad_wwmmflg,	// 週月フラグ
					YYMM:			txt_yymm,		// 年月(週No.)
					DAICD:			txt_daicd,		// 大分類
					CHUCD:			txt_chucd,		// 中分類
					JISSEKIBUN:		rad_jissekibun,		// 実績分類

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

			var row = $('#'+$.id.grd_rtptntenbetubrt+'_list').datagrid("getSelected");
			var rowIndex = $('#'+$.id.grd_rtptntenbetubrt+'_list').datagrid("getRowIndex", row);
			$('#'+$.id.grd_rtptntenbetubrt+'_list').datagrid('endEdit',rowIndex);
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform).form('validate');

			var txt_rtptnno			= $('#'+$.id_inp.txt_rtptnno).textbox('getValue');			// 通常率パターンNo.
			var txt_rtptnkn			= $('#'+$.id_inp.txt_rtptnkn).textbox('getValue');			// 通常率パターン名称

			var targetRowsrtptn = that.getGridData($.id.grd_rtptntenbetubrt+'_list');

			var allFlg = false;
			for(var i = 0;i<targetRowsrtptn.length;i++){
				if(targetRowsrtptn[i]["F3"]!=0){
					allFlg = true;
					break;
				}
			}

			if(allFlg==false){
				$.showMessage('E20580');
				return false;
			}

			if (rt) {
				if (that.sendBtnid==$.id.btn_new) {
					if (!txt_rtptnno) {
						$.showMessage('EX1069', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_rtptnno), true)});
						rt = false;
					}
				}
			}
			if (rt) {
				if (!txt_rtptnkn) {
					$.showMessage('EX1084', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_rtptnno), true)});
					return false;
				}
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

			// 入力データ：率パターン
			var targetRowsrtptn = that.getGridData($.id.grd_rtptntenbetubrt+'_list');

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
					DATA:			JSON.stringify(targetDatas),		// 更新対象情報
					DATA_RTPTN:		JSON.stringify(targetRowsrtptn),	// 更新対象情報
					t:				(new Date()).getTime()
				},
				function(data){
					// 検索処理エラー判定
					if($.updError(id, data)) return false;

					var afterFunc = function(){
						// 初期化
						that.clear();
						that.changeReport(that.name, $.id.btn_back);
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
			// 入力チェック用の配列をクリア
			that.jsonTemp = [];

			return rt;
		},
		delSuccess: function(id){
			var that = this;

			var targetDatas = [{}];
			$('#'+that.focusRootId).find('[col^=F]').each(function(){
				var col = $(this).attr('col');
				targetDatas[0][col] = $.getInputboxValue($(this));
			});

			// 入力データ：率パターン
			var targetRowsrtptn = that.getGridData($.id.grd_rtptntenbetubrt+'_list');

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
					//IDX:			$($.id.hiddenChangedIdx).val(),	// 更新対象Index
					//DATA:			JSON.stringify(targetRows),		// 更新対象情報
					DATA:			JSON.stringify(targetDatas),	// 更新対象情報
					DATA_RTPTN:		JSON.stringify(targetRowsrtptn),	// 更新対象情報
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
			// 部門
			this.jsonTemp.push({
				id:		$.id_inp.txt_bmncd,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_bmncd),
				text:	''
			});
			// 通常率パターンNo.
			this.jsonTemp.push({
				id:		$.id_inp.txt_rtptnno,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_rtptnno),
				text:	''
			});
			// 週月フラグ
			this.jsonTemp.push({
				id:		$.id.rad_wwmmflg,
				value:	$.getJSONValue(this.jsonHidden, $.id.rad_wwmmflg),
				text:	''
			});
			// 年月(週No.)
			this.jsonTemp.push({
				id:		$.id_inp.txt_yymm,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_yymm),
				text:	''
			});
			// 大分類
			this.jsonTemp.push({
				id:		$.id_inp.txt_daicd,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_daicd),
				text:	''
			});
			// 中分類
			this.jsonTemp.push({
				id:		$.id_inp.txt_chucd,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_chucd),
				text:	''
			});
			// 実績分類
			this.jsonTemp.push({
				id:		$.id.rad_jissekibun,
				value:	$.getJSONValue(this.jsonHidden, $.id.rad_jissekibun),
				text:	''
			});
			// 通常率パターンNo.
			this.jsonTemp.push({
				id:		$.id_inp.txt_rtptnno,
				value:	$('#'+$.id_inp.txt_rtptnno).textbox('getValue'),
				text:	$('#'+$.id_inp.txt_rtptnno).textbox('getText')
			});
			// 通常率パターン名称
			this.jsonTemp.push({
				id:		$.id_inp.txt_rtptnkn,
				value:	$('#'+$.id_inp.txt_rtptnkn).textbox('getValue'),
				text:	$('#'+$.id_inp.txt_rtptnkn).textbox('getText')
			});
			// 分配率
			this.jsonTemp.push({
				id:		$.id_inp.txt_bunpairt,
				value:	$('#'+$.id_inp.txt_bunpairt).textbox('getValue'),
				text:	$('#'+$.id_inp.txt_bunpairt).textbox('getText')
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
		setEditableGrid: function(that, reportno, id){		// データ表示
			that.editRowIndex[id] = -1;
			var index = -1;
			if (that.reportYobiInfo()==='1') {
				var columns = that.getGridColumns1(that, id);
			}else if (that.reportYobiInfo()==='0' && that.sendBtnid==$.id.btn_sel_refer) {
				var columns = that.getGridColumns2(that, id);
			} else {
				var columns = that.getGridColumns3(that, id);
			}
//			var columns = that.getEditableGridColumns(that, id);
			$('#'+id).datagrid({
				url:$.reg.easy,
				columns:columns,
				onBeforeLoad:function(param){
					index = -1;
					if(that.reportYobiInfo()==='1'){
						var sendBtnid	 = that.sendBtnid;
						var txt_bmncd = $.getJSONValue(that.jsonHidden, $.id_inp.txt_bmncd);		// 部門
						var rad_wwmmflg = $.getJSONValue(that.jsonHidden, $.id.rad_wwmmflg);		// 週月フラグ
						var txt_yymm = $.getJSONValue(that.jsonHidden, $.id_inp.txt_yymm);			// 年月(週No.)
						var txt_daicd = $.getJSONValue(that.jsonHidden, $.id_inp.txt_daicd);		// 大分類
						var txt_chucd = $.getJSONValue(that.jsonHidden, $.id_inp.txt_chucd);		// 中分類
						var json = [{"callpage":"Out_ReportRP010","SENDBTNID":sendBtnid,"BMNCD":txt_bmncd,"WWMMFLG":rad_wwmmflg,"YYMM":txt_yymm,"DAICD":txt_daicd,"CHUCD":txt_chucd}];

					}else if(that.reportYobiInfo()==='0'){
						var sendBtnid	 = that.sendBtnid;
						var txt_bmncd = $.getJSONValue(that.jsonHidden, $.id_inp.txt_bmncd);		// 部門
//						var txt_bmncd	 = $.getInputboxValue($('#'+$.id_inp.txt_rtptnbmncd));							// 部門
						var txt_rtptnno	 = $.getInputboxValue($('#'+$.id_inp.txt_rtptnno));							// 通常率パターンNo.
						var json = [{"callpage":"Out_ReportRP006","SENDBTNID":sendBtnid,"BMNCD":txt_bmncd,"RTPTNNO":txt_rtptnno}];
					}
					// 情報設定
					param.page		=	reportno;
					param.obj		=	id;
					param.sel		=	(new Date()).getTime();
					param.target	=	id;
					param.action	=	$.id.action_init;
					param.json		=	JSON.stringify(json);
					param.datatype	=	"datagrid";
					param.report = that.name;
				},
				onLoadSuccess:function(data){},
				onClickRow: function(index,field){$.clickEditableDatagridCell(that,id, index)},
				onBeginEdit:function(index,row){$.beginEditDatagridRow(that,id, index, row)},
				onEndEdit: function(index,row,changes){$.endEditDatagridRow(that, id, index, row)},
				onBeforeEdit:function(index,row){
					var allRows	 = $('#'+id).datagrid('getRows');
					var rows	 =  $('#'+id).datagrid('getRows');				// 現在表示されているデータ
					var rowsLength = rows.length
					var isEdit = false

					var RefleshRangeMin = $('#'+id).datagrid("getRowIndex", rows[0]);
					var RefleshRangeMax = $('#'+id).datagrid("getRowIndex", rows[rowsLength-1]);

					if(!row){
						row = allRows[index]
					}

					if(row.EDITFLG !='1'){

						// 次の行に移るか、次の項目に移るかする
						var adds = that.editRowIndex[that.focusGridId] > index ? -1:1;
						var nextindex = index + adds;
						if(nextindex >= 0 && nextindex < $('#'+id).datagrid('getRows').length){
							$('#'+id).datagrid('selectRow', nextindex);
							$('#'+id).datagrid('beginEdit', nextindex);
						}else{
							that.editRowIndex[that.focusGridId] = -9;	// 次の項目に移動するために未編集状態と違うインデックス設定
							var evt = $.Event('keydown');
							evt.keyCode = 13;
							evt.shiftKey = adds === -1;
							$('#'+id).parents('.datagrid').eq(0).trigger(evt);
						}
						return false;
					}
				}
			});
		},
		getGridColumns1:function(that, id){
			var columns = [];
			var columnBottom=[];
			var iformatter	 = function(value,row,index){ return $.getFormat(value, '#,##0');};
			columnBottom.push({field:'TENCD',	title:'店番',		width: 70,halign:'center',align:'left'});
			columnBottom.push({field:'TENKN',	title:'店舗名',		width: 500,halign:'center',align:'left'});
			columnBottom.push({field:'URIAGE',	title:'売上',		width: 100,halign:'center',align:'right', formatter:iformatter});
			columnBottom.push({field:'TENSU',	title:'点数',		width: 100,halign:'center',align:'right', formatter:iformatter});
			columns.push(columnBottom);
			return columns;
		},
		getGridColumns2:function(that, id){
			var columns = [];
			var columnBottom=[];
			columnBottom.push({field:'TENCD',		title:'店番',	width: 70,halign:'center',align:'left'});
			columnBottom.push({field:'TENKN',		title:'店舗名',	width: 500,halign:'center',align:'left'});
			columnBottom.push({field:'BUNPAIRT',	title:'分配率',	width:  100,halign:'center',align:'right'});
			columns.push(columnBottom);
			return columns;
		},
		getGridColumns3:function(that, id){
			var columns = [];
			var columnBottom=[];
			columnBottom.push({field:'TENCD',		title:'店番',	width: 70,halign:'center',align:'left'});
			columnBottom.push({field:'TENKN',		title:'店舗名',	width: 500,halign:'center',align:'left'});
			columnBottom.push({field:'BUNPAIRT',	title:'分配率',	width:  100,halign:'center',align:'right',editor:{type:'numberbox'}});
			columns.push(columnBottom);
			return columns;
		},
		getGridData: function (target){

			var targetRows= [];

			if(target===undefined || target===$.id.grd_rtptntenbetubrt+'_list'){
				var rowsRtptn= $('#'+$.id.grd_rtptntenbetubrt+'_list').datagrid('getRows');
				for (var i=0; i<rowsRtptn.length; i++){
					if(rowsRtptn[i]["TENCD"] == "" || rowsRtptn[i]["TENCD"] == null ){

					}else{
						var rowDate = {
								F1	 : rowsRtptn[i]["TENCD"],
								F2	 : rowsRtptn[i]["TENKN"],
								F3	 : rowsRtptn[i]["BUNPAIRT"],
							};
						targetRows.push(rowDate);
					}
				}
			}
			return targetRows;
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
			$.setJSONObject(sendJSON, 'reportYobi1', that.reportYobiInfo(), that.reportYobiInfo());		// 参照情報保持
			$.setJSONObject(sendJSON, 'callpage', that.name, that.name);								// 呼出し元レポート情報

			// 戻る実行時用に現在の画面情報を保持する
			var states = $.getBackBaseJSON(that);
			var newrepinfos = $.getBackJSON(that, states, false);
			$.setJSONObject(sendJSON, 'repinfo', newrepinfos, '');

			// 呼出別処理
			switch (btnId) {
			case $.id.btn_back:
			case $.id.btn_cancel:
			case'btn_return':

				// 元画面情報
				var callpage = $.getJSONValue(that.jsonHidden, "callpage");
				// 転送先情報
				if (callpage=='Out_ReportST011') {
					index = 4;
				} else {
					index = 5;
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

		}
	} });
})(jQuery);