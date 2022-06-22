/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_ReportYH203',			// （必須）レポートオプションの確認
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
		dedefaultObjNum:	14,	// 初期化オブジェクト数
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
		grd_tenpo_yh_data:[],				// グリッド情報:店舗一覧
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

			// 処理日付取得、データグリッド設定
			that.getsetInputboxData(reportno, $.id.txt_shoridt, [{}], $.id.action_init);

			var isUpdateReport = true;

			// 初期検索可能
			that.onChangeReport = true;

			// チェックボックス設定：削除
			$.setCheckboxInit2(that.jsonHidden, $.id.chk_del, that);

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

			// Editor参照用定義項目
			$.setInputbox(that, reportno, 'txt_f1', isUpdateReport);
			$.setInputbox(that, reportno, 'txt_f2', isUpdateReport);
			$.setInputbox(that, reportno, 'txt_f3', isUpdateReport);

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

			var sendBtnid = $.getJSONValue(that.jsonHidden, "sendBtnid");
			if(sendBtnid && sendBtnid.length > 0){
				$.reg.search = true;
			}

			//$.setInputBoxDisable($("#kikaku_dummy"));
			$.setInputBoxDisable($("#kikan_dummy"));
			$.setInputBoxDisable($("#nndt_dummy"));

			var reportYobi1Bf	 = $.getJSONValue(that.jsonHidden, "reportYobi1");
			var txt_nndt		 = $.getJSONValue(that.jsonHidden,  $.id_inp.txt_nndt);

			if(reportYobi1Bf === '1' && txt_nndt === '合計'){
				$.initReportInfo("YH104", "予約発注　参照　店別発注数量(期間計)", "参照");
				// レイアウト調整
				$('#nndt_dummy').textbox({
					width:230
				});

				$("#"+$.id.btn_upd).linkbutton('disable');
				$("#"+$.id.btn_upd).attr('tabindex', -1).hide();

			}else if(that.reportYobiInfo()==='1'){
				$.initReportInfo("YH103", "予約発注　参照　店別発注数量", "参照");
				$("#"+$.id.btn_upd).linkbutton('disable');
				$("#"+$.id.btn_upd).attr('tabindex', -1).hide();

			}else if(that.reportYobiInfo()==='2'){
				$.initReportInfo("YH203", "予約発注　修正　店別発注数量", "修正");

			}
			$.setInputBoxDisable($("#kikaku_dummy"));
			$.setInputBoxDisable($("#kikan_dummy"));
			$.setInputBoxDisable($("#nndt_dummy"));

			// 各種遷移ボタン
			$('#'+$.id.btn_cancel).on("click", $.pushChangeReport);

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
			var txt_kkkcd		= $.getJSONObject(this.jsonString, $.id_inp.txt_kkkcd).value;	// 企画No
			var txt_shncd		= $.getJSONObject(this.jsonString, $.id_inp.txt_shncd).value;	// 商品コード
			var txt_nndt		= $.getJSONObject(this.jsonString, $.id_inp.txt_nndt).value;	// 納入日

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$.appendMaskMsg();

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,		// レポート名
					KKKCD:			txt_kkkcd,
					SHNCD:			txt_shncd,
					NNDT:			txt_nndt,
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

					// 入力制御項目の設定
					that.setDispOption();

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
		setDispOption:function(){
			var that = this;
			var shoridt  = $('#'+$.id.txt_shoridt).val();							// 処理日付

			var htdt		 = $.getInputboxValue($('#'+$.id_inp.txt_htdt));		// 発注日

			if(htdt !== ''){
				var sdt = $.convDate(htdt, true);
				var edt = $.convDate(shoridt, true);
				if(sdt.getTime() <= edt.getTime()){
					// 発注日 <= 処理日付
					//$.setInputBoxDisable($("#"+$.id_inp.txt_htdt));

					$.setInputBoxDisable($("#"+$.id.btn_upd));
					//$("#"+$.id.btn_upd).linkbutton('disable');
					//$("#"+$.id.btn_upd).attr('tabindex', -1).hide();
				}
			}
		},
		updValidation: function (){	// （必須）批准
			var that = this;

			// 入力処理中の場合、編集を終了する。
			var row = $('#'+$.id.grd_tenpo_yh).datagrid("getSelected");
			var rowIndex = $('#'+$.id.grd_tenpo_yh).datagrid("getRowIndex", row);
			$('#'+$.id.grd_tenpo_yh).datagrid('endEdit',rowIndex);

			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform).form('validate');
			if(!rt){
				$.addErrState(that, $('.validatebox-invalid').eq(0), false);
				return rt;
			}

			// 現在の画面情報を変数に格納
			var gridData = that.getGridData($.id.grd_tenpo_yh);	// 検証用情報取得
			var targetOId = [$.id_inp.txt_tencd];
			var targetCId = ["F4",];
			// 催しコード_レギュラー
			var moyskbn = $.id.value_moykbn_r*1;
			var targetRows = gridData[$.id.grd_tenpo_yh];

			var gendosu		 = $.getInputboxValue($('#'+$.id_inp.txt_gendosu));		// 発注限度数
			var hatsu		 = 0													// 一覧表合計発注数

			for (var i=0; i<targetRows.length; i++){
				for (var j = 0; j < targetOId.length; j++){
					var value = targetRows[i][targetCId[j]]
					if(value && value !== ""){
						var msgid = that.checkInputboxFunc(targetOId[j], targetRows[i][targetCId[j]], moyskbn, '');
						if(msgid !==null){
							$.showMessage(msgid, undefined, function(){$.addErrState(that, $('#'+that.focusGridId), true, {NO:i, ID:$.id_inp.txt_tencd + '_'})});
							return false;
						}

						if(targetRows[i]["F6"] && targetRows[i]["F6"] !== ''){
							// 入力された発注数の合計
							hatsu += Number(targetRows[i]["F6"])

						}
					}
				}

				if(!targetRows[i].F4 || targetRows[i].F4 ==""){
					// 店舗コード未入力時
					if(targetRows[i].F6 && targetRows[i].F6 !== ""){
						$.showMessage('EX1047', ['店コード'], function(){$.addErrState(that, $('#'+that.focusGridId), true, {NO:i, ID:$.id_inp.txt_tencd + '_'})});
						return false;
					}
				}
			}

			// 発注限度数チェック
			if(gendosu && gendosu != ""){
				// 限度数未設定の場合はチェックを行わない
				if(Number(gendosu) < hatsu){
					// 限度数 < 発注合計
					$.showMessage('E11241');
					return false;
				}
			}
			return rt;
		},
		updSuccess: function(id){	// validation OK時 の update処理
			var that = this;

			// 変更行情報取得
			//var changedIndex = $($.id.hiddenChangedIdx).val().split(",");

			var shoridt	 = $('#'+$.id.txt_shoridt).val();						// 処理日付

			// 基本入力情報取得
			var targetDatas = that.getGridData("data")["data"];

			// 店舗一覧のデータを取得
			var targetDatasTen = that.getMergeGridDate($.id.grd_tenpo_yh);

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
					SHORIDT:		shoridt,						// 処理日付
//					IDX:			$($.id.hiddenChangedIdx).val(),	// 更新対象Index
					DATA:			JSON.stringify(targetDatas),	// 更新対象情報
					DATA_TEN:		JSON.stringify(targetDatasTen),	// 更新対象情報
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
			// 商品コード
			this.jsonTemp.push({
				id:		$.id_inp.txt_shncd,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_shncd),
				text:	''
			});
			// 納入日
			this.jsonTemp.push({
				id:		$.id_inp.txt_nndt,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_nndt),
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
		setEditableGrid: function(that, reportno, id){		// データ表示
			var that = this;
			var init = true;
			// ページサイズ定義取得
			var pageList = $.fn.pagination.defaults.pageList;
			var pageSize = $.getJSONValue(this.jsonHidden, $.id.pageSize);
			var cstyler =function(value,row,index){return 'color: red;font-weight: bold;';};
			var dformatter =function(value){
				var add20 = value && value.length===6;
				return $.getFormatDt(value, add20);
			};

			var cformatter =function(value,row,index){return value && value===$.id.value_on?$.id.text_on:$.id.text_off;};
			var wformatter =function(value,row,index){return $.getFormatWeek(row[this.field.replace("_W", "")]);};
			var iformatter = function(value,row,index){ return $.getFormat(value, '#,##0');};

			var targetId = $.id_inp.txt_tencd;
			var check = $('#'+targetId).attr("check") ? JSON.parse('{'+$('#'+targetId).attr("check")+'}'): JSON.parse('{}');		// 大分類コードのcheck要素を取得
			var formatterLPad = function(value){
				return $.getFormatLPad(value, check.maxlen);
			};
			if (pageSize==="") pageSize=$.fn.pagination.defaults.pageSize;

			var cformatter =function(value,row,index){return value && value===$.id.value_on?$.id.text_on:$.id.text_off;};
			var wformatter =function(value,row,index){return $.getFormatWeek(row[this.field.replace("_W", "")]);};
			if (pageSize==="") pageSize=$.fn.pagination.defaults.pageSize;

			var funcClickRow = $.fn.datagrid.defaults.onClickRow;
			var funcBeginEdit= $.fn.datagrid.defaults.onBeginEdit;
			var funcEndEdit= $.fn.datagrid.defaults.onEndEdit;
			var funcAfterEdit= $.fn.datagrid.defaults.onAfterEdit;
			if(that.reportYobiInfo()!=='1'){
				that.editRowIndex[id] = -1;
				funcClickRow = function(index,field){$.clickEditableDatagridCell(that,id, index)};
				funcBeginEdit = function(index,row){$.beginEditDatagridRow(that,id, index, row)};
				funcEndEdit = function(index,row,changes){$.endEditDatagridRow(that, id, index, row);};
				funcAfterEdit = function(index,row,changes){
					// チェックボックスの再追加（EndEdit時に削除されるため）
					$.afterEditAddCheckbox($('#'+id).datagrid('getPanel').find("[datagrid-row-index='"+index+"']"));
				};
				// 編集可能データグリッドの共通処理設定
				// 編集エディターの機能拡張（非表示で参照用のオブジェクトが準備してある前提、参照オブジェクトはグリッドのフィールド名を元に判断）
				$.extendDatagridEditor(that);
			}
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
				frozenColumns:[[]],
				loadMsg:false,
				columns:[[
							{field:'TENCD',		title:'店コード',				width:  70,halign:'center',align:'left',editor:{type:'numberbox'},formatter:formatterLPad},
							//{field:'TENCD',		title:'店コード',				width:  70,halign:'center',align:'left',formatter:formatterLPad},
							{field:'TENKN',		title:'店舗名',					width: 350,halign:'center',align:'left',editor:{type:'textbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}},
							{field:'HTSU',		title:'前日までの発注数',		width:  60,halign:'center',align:'right', formatter:iformatter,editor:{type:'numberbox'}},
							{field:'HTSU_T',	title:'当日数',					width:  60,halign:'center',align:'right', formatter:iformatter},
							//{field:'SINKFLG',	title:'新規行フラグ',			width:  60,halign:'center',align:'left',hidden:true},
							]],
				url:$.reg.easy,
				onBeforeLoad:function(param){
					index = -1;
					var values = {};
					var kkkcd	 = $('#'+$.id_inp.txt_kkkcd).numberbox('getValue');		// 企画No
					var shncd	 = $('#'+$.id_inp.txt_shncd).numberbox('getValue');		// 商品コード
					var nndt	 = $('#'+$.id_inp.txt_nndt).numberbox('getValue');		// 納品日
					var shoridt	 = $('#'+$.id.txt_shoridt).val();						// 処理日付

					values["callpage"]	 = $($.id.hidden_reportno).val()				// 呼出元レポート名
					values["KKKCD"]		 = kkkcd										// 企画No
					values["SHNCD"]		 = shncd										// 商品コード
					values["NNDT"]		 = nndt											// 納入日
					values["SHORIDT"]	 = shoridt										// 処理日付

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
				},
				onSelect:function(index){
					var rows = $('#'+id).datagrid('getRows');
					var col = $('#'+id).datagrid('getColumnOption', 'TENCD');


					if(rows[index].SINKFLG && rows[index].SINKFLG == '1'){
						col.editor = {
								type			:'numberbox',
								options:{cls	:'labelInput',editable:true,disabled:false,readonly:false},
								formatter		:formatterLPad
						}
					}else{
						// 店コード入力済みの場合は編集不可
						col.editor = false
					}
				},
				onClickRow: funcClickRow,
				onBeginEdit:funcBeginEdit,
				onEndEdit: funcEndEdit,
			});
		},
		getGridData: function (target){
			var that = this;

			var data = {};
			var targetRows= [];

			// 基本情報
			if(target===undefined || target==="data"){
				var targetDatas = [{}];
				$('#'+that.focusRootId).find('[col^=F]').each(function(){
					var col = $(this).attr('col');
					targetDatas[0][col] = $.getInputboxValue($(this));
				});

				for (var i=0; i<targetDatas.length; i++){
					var rowDate = {
							F1	 : targetDatas[i]["F3"],		// 企画No
							F2	 : targetDatas[i]["F4"],		// 商品コード
							F3	 : targetDatas[i]["F6"],		// 納入日
							F4	 : targetDatas[i]["F7"],		// 排他用更新日
					};
					if(rowDate){
						targetRows.push(rowDate);
					}
				}
				data["data"] = targetRows;
			}

			// 納品日一覧
			if(target===undefined || target===$.id.grd_tenpo_yh){
				var rows	 = $('#'+$.id.grd_tenpo_yh).datagrid('getRows');		// 商品一覧
				var kkkcd	 = $('#'+$.id_inp.txt_kkkcd).numberbox('getValue');		// 企画No
				var shncd	 = $('#'+$.id_inp.txt_shncd).numberbox('getValue');		// 商品コード
				var nndt	 = $.getInputboxValue($('#'+$.id_inp.txt_nndt));		// 納入日

				for (var i=0; i<rows.length; i++){
					var rowDate = {
							F1	 : kkkcd,
							F2	 : shncd,
							F3	 : nndt,
							F4	 : rows[i]["TENCD"],				// 店コード
							F5	 : rows[i]["INPUTDT"],				// 入力日
							F6	 : rows[i]["HTSU"],					// 発注数
					};
					targetRows.push(rowDate);
				}
				data[$.id.grd_tenpo_yh] = targetRows;
			}
			return data;
		},
		getMergeGridDate: function(target){
			// 保持したデータと入力データ比較を比較する。
			var that = this;

			var newrows = that.getGridData(target)[target];		// 変更データ
			var oldrows = [];
			var targetRows= [];


			if(target===undefined || target===$.id.grd_tenpo_yh){
				// 店舗一覧
				var shoridt	 = $('#'+$.id.txt_shoridt).val();						// 処理日付
				var shoridtBf = $.convertDate(shoridt, -1);							// 処理前日日付

				oldrows = that.grd_tenpo_yh_data
				for (var i=0; i<newrows.length; i++){
					if(newrows[i]["F4"] && newrows[i]["F4"] !== ''){
						var rowDate = {
								F1	 : newrows[i]["F1"],
								F2	 : newrows[i]["F2"],
								F3	 : newrows[i]["F3"],
								F4	 : newrows[i]["F4"],
								//F5	 : newrows[i]["F5"] ? newrows[i]["F5"] : shoridt,
								F5	 : shoridtBf,
								F6	 : newrows[i]["F6"],
						};
						if(rowDate){
							targetRows.push(rowDate);
						}
					}
					/*if((oldrows[i]['F5'] ? oldrows[i]['F5'] : "") !== (newrows[i]['F5'] ? newrows[i]['F5'] : "")
					 	|| (oldrows[i]['F6'] ? oldrows[i]['F6'] : "") !== (newrows[i]['F6'] ? newrows[i]['F6'] : "")
					){
						if(newrows[i]["F1"]){
							var rowDate = {
									F1	 : newrows[i]["F1"],
									F2	 : newrows[i]["F2"],
									F3	 : newrows[i]["F3"],
									F4	 : newrows[i]["F4"],
									//F5	 : newrows[i]["F5"] ? newrows[i]["F5"] : shoridt,
									F5	 : shoridtBf,
									F6	 : newrows[i]["F6"],
							};
							if(rowDate){
								targetRows.push(rowDate);
							}
						}
					}*/
				}
			}
			return targetRows;
		},
		setGridData: function (data, target){
			var that = this;

			// 基本情報
			/*if(target===undefined || target==="data"){
				that.data =  data["data"];
			}*/

			// 商品一覧
			if(target===undefined || target===$.id.grd_tenpo_yh){
				that.grd_tenpo_yh_data =  data[$.id.grd_tenpo_yh];
			}
		},
		getsetInputboxData: function(reportno, id, param, action){
			var that = this
			if(action===undefined) action = $.id.action_change;
			// 情報設定
			$.ajax({
				url: $.reg.easy,
				type: 'POST',
				async: true,
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

					// 編集可能データグリッドの共通処理設定
					// 編集エディターの機能拡張（非表示で参照用のオブジェクトが準備してある前提、参照オブジェクトはグリッドのフィールド名を元に判断）
					$.extendDatagridEditor();

					// 納品日一覧
					that.setEditableGrid(that, reportno, $.id.grd_tenpo_yh);
				}
			});
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
			$.setJSONObject(sendJSON, 'reportYobi1', that.reportYobiInfo(), that.reportYobiInfo());			// 参照情報保持
			$.setJSONObject(sendJSON, 'callpage', that.name, that.name);									// 呼出し元レポート情報

			// 戻る実行時用に現在の画面情報を保持する
			var states = $.getBackBaseJSON(that);
			// 各種グリッド情報を設定
			var newrepinfos = $.getBackJSON(that, states);
			$.setJSONObject(sendJSON, 'repinfo', newrepinfos, '');

			// 呼出別処理
			switch (btnId) {
			case $.id.btn_new:
				// 転送先情報
				index = 7;
				childurl = href[index];

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

				$.setJSONObject(sendJSON, $.id_inp.txt_kkkcd, row.F1, row.F1);				// 企画No
				break;
			case $.id.btn_cancel:
			case $.id.btn_back:
			case "btn_return":
				// 転送先情報
				//index = 4;
				//index = 8;
				sendMode = 2;

				var reportYobi1Bf	 = $.getJSONValue(that.jsonHidden, "reportYobi1");
				if(reportYobi1Bf === '1'){
					index = 9;
				}else if(reportYobi1Bf === '2'){
					index = 8;
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
				var rows = $.getSelectListData(that.name, $.id.action_change, id, param);
				var row = rows.length > 0 ? rows[0]:"";
				$.setInputboxRowData('for_inp', id, row, that, parentObj);
			}

			// 検索、入力後特殊処理
			if(that.queried){
				var msgid = null;

				// 店コード
				if(id===$.id_inp.txt_tencd){
					msgid = that.checkInputboxFunc(id, newValue, '');
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

			}

			// 店舗コード
			if(id===$.id_inp.txt_tencd && (newValue!=='' && newValue)){
				var param = {};
				param["KEY"] =  "MST_CNT";
				param["value"] = newValue;
				var chk_cnt = $.getInputboxData(that.name, $.id.action_check, $.id_inp.txt_tencd, [param]);
				if(chk_cnt==="" || chk_cnt==="0"){
					return "E11096";
				}
			}
			return null;
		},
		getInputboxParams: function(that, id, newValue){
			// 情報取得
			var values = {};
			values["value"] = newValue;
			values["TABLEKBN"] = that.baseTablekbn;

			// 店コード
			if(id===$.id_inp.txt_tencd){
				values["TENCD"] = $.getInputboxValue($('#'+$.id_inp.txt_tencd));
			}

			// 情報設定
			return [values];
		},
	} });
})(jQuery);