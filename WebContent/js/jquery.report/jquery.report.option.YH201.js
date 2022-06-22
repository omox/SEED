/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_ReportYH201',			// （必須）レポートオプションの確認
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
		dedefaultObjNum:	6,	// 初期化オブジェクト数
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
			that.getsetInputboxData(reportno, $.id.txt_shoridt, [{}], $.id.action_init);

			var isUpdateReport = true;

			// 初期検索可能
			that.onChangeReport = true;

			// チェックボックス設定：削除
			$.setCheckboxInit2(that.jsonHidden, $.id.chk_use, that);
			$.setCheckboxInit2(that.jsonHidden, $.id.chk_sel, that);
			//$.setCheckboxInit2(that.jsonHidden, $.id.chk_del, that);

			//$('#'+$.id.chk_use).change(function() {
			$('#'+$.id_inp.txt_kkkcd).change(function() {
				alert();
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

			// 当帳票を「参照」で開いた場合
			if(that.reportYobiInfo()==='1'){
				$.initReportInfo("YH101", "予約発注　参照　商品一覧", "参照");
				$("#"+$.id.btn_new).linkbutton('disable');
				$("#"+$.id.btn_new).attr('tabindex', -1).hide();
				$("#"+$.id.btn_upd).linkbutton('disable');
				$("#"+$.id.btn_upd).attr('tabindex', -1).hide();
				$("#"+$.id.btn_cancel).linkbutton('disable');
				$("#"+$.id.btn_cancel).attr('tabindex', -1).hide();

			}else if(that.reportYobiInfo()==='2'){
				$.initReportInfo("YH201", "予約発注　修正　商品一覧", "修正");
				$("#"+$.id.btn_new).linkbutton('disable');
				$("#"+$.id.btn_new).attr('tabindex', -1).hide();

			}

			// 各種遷移ボタン
			$('#'+$.id.btn_sel_change).on("click", $.pushChangeReport);
			$('#'+$.id.btn_cancel).on("click", $.pushChangeReport);

			// 変更
			$($.id.hiddenChangedIdx).val('');

			$.setInputBoxDisable($("#txt_countItem"));

			$.setInputBoxDisable($("#kikaku_dummy"));
			$.setInputBoxDisable($("#kikan_dummy"));
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

			// 店舗入力開始日
			var shoridt = $('#'+$.id.txt_shoridt).val();
			var rows = $('#'+ $.id.grd_shohin).datagrid('getRows')
			for(var i = 0; i < rows.length; i++){
				if(rows[i].F1 == '1'){
					// 入力制限= '1'のとき
					var TENIEDDT  = $.convertDate(rows[i].F14 , 0)*1;		 // 店舗入力終了
					if(TENIEDDT < (shoridt*1)){	// 店舗入力終了 <= 処理日付
						$.showMessage("E20222");
						return false;
					}
				}
			}

			if(!rt){
				$.showMessage('E00001');
				return rt;
			}
			return rt;
		},
		updSuccess: function(id){	// validation OK時 の update処理
			var that = this;

			// 変更行情報取得
			//var changedIndex = $($.id.hiddenChangedIdx).val().split(",");

			var targetDatas = [{}];
			$('#'+that.focusRootId).find('[col^=F]').each(function(){
				var col = $(this).attr('col');
				targetDatas[0][col] = $.getInputboxValue($(this));
			});

			// 基本入力情報取得
			//var targetDatas = that.getMergeGridDate("data");

			// 新規登録時には配送パターン
			/*if(that.sendBtnid =  $.id.btn_sel_change){
				var enptyrows = [];
				targetDatas = enptyrows;
			}*/

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
			// 納入開始日
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
		extenxDatagridEditorIds:{
			F1					: 'chk_use'			// 商品コード
			//,F1					: $.id.chk_sel
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
			var cformatter =function(value,row,index){return value && value===$.id.value_on?$.id.text_on:$.id.text_off;};
			var wformatter =function(value,row,index){return $.getFormatWeek(row[this.field.replace("_W", "")]);};
			var iformatter = function(value,row,index){ return $.getFormat(value, '#,##0');};
			if (pageSize==="") pageSize=$.fn.pagination.defaults.pageSize;

			var funcClickRow = $.fn.datagrid.defaults.onClickRow;
			var funcBeginEdit= $.fn.datagrid.defaults.onBeginEdit;
			var funcEndEdit= $.fn.datagrid.defaults.onEndEdit;
			var funcAfterEdit= $.fn.datagrid.defaults.onAfterEdit;

			that.editRowIndex[id] = -1;
			funcClickRow = function(index,field){$.clickEditableDatagridCell(that,id, index)};

			funcBeginEdit = function(index,row){
				var notDisableEds = undefined;
				if(that.reportYobiInfo()=='1'){
					notDisableEds = [$.id.chk_sel];
				}

				if(notDisableEds){
					$($('#'+id).datagrid('getEditors', index)).each(function(){
						var refid = $.getExtendDatagridEditorRefid(this.target);
						if(notDisableEds.indexOf(refid) === -1){
							$.setInputBoxDisable($(this.target));
						}
					});
				}

				$.beginEditDatagridRow(that,id, index, row)
			};
			funcEndEdit = function(index,row,changes){
				$.endEditDatagridRow(that, id, index, row);
				row.SEL = $.id.value_off;
			};
			funcAfterEdit = function(index,row,changes){
				// チェックボックスの再追加（EndEdit時に削除されるため）
				$.afterEditAddCheckbox($('#'+id).datagrid('getPanel').find("[datagrid-row-index='"+index+"']"));
			};
			// 編集可能データグリッドの共通処理設定
			// 編集エディターの機能拡張（非表示で参照用のオブジェクトが準備してある前提、参照オブジェクトはグリッドのフィールド名を元に判断）
			$.extendDatagridEditor(that);

			that.scrollToId[0] = id;

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
				loadMsg:false,
				frozenColumns:[[
							{field:'SEL',		title:'選択',				editor:{type:'checkbox'}, styler:cstyler, formatter:cformatter,	width:  40,halign:'center',align:'center'},
							{field:'F1',		title:'入力制限',			editor:{type:'checkbox'}, styler:cstyler, formatter:cformatter,	width:  40,halign:'center',align:'center'},
							{field:'F2',		title:'商品コード',			width: 100,halign:'center',align:'left'},
							{field:'F3',		title:'商品名称（漢字）',	width: 300,halign:'center',align:'left'},
							]],
				columns:[[
							{field:'F4',		title:'発注日',				width: 100,halign:'center',align:'left',formatter:dformatter},
							{field:'F15',		title:'納入期間',			width: 200,halign:'center',align:'left'},
							{field:'F5',		title:'受付期間',			width: 200,halign:'center',align:'left'},
							{field:'F6',		title:'店舗入力期間',		width: 200,halign:'center',align:'left'},
							{field:'F7',		title:'前日までの発注数',	width: 75,halign:'center',align:'right',formatter:iformatter},
							{field:'F8',		title:'当日数',				width: 100,halign:'center',align:'right',formatter:iformatter},
							{field:'F9',		title:'予定数',				width: 100,halign:'center',align:'right',formatter:iformatter},
							{field:'F10',		title:'限度数',				width: 100,halign:'center',align:'right',formatter:iformatter},

							]],
				url:$.reg.easy,
				onBeforeLoad:function(param){
					index = -1;
					var values = {};
					var kkkcd	 = $('#'+$.id_inp.txt_kkkcd).numberbox('getValue');	// 配送グループコード
					var shoridt	 = $('#'+$.id.txt_shoridt).val();
					//var shoridt = $('#'+$.id.txt_shoridt).val();

					values["callpage"]	 = $($.id.hidden_reportno).val()						// 呼出元レポート名
					values["KKKCD"]		 = kkkcd												// 企画No
					values["SHORIDT"]	 = shoridt												// 処理日付

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
				onClickRow: funcClickRow,
				onBeginEdit:funcBeginEdit,
				onEndEdit: funcEndEdit,
				onAfterEdit: funcAfterEdit,
			});
		},
		getGridData: function (target){
			var that = this;

			var data = {};
			var targetRows= [];

			// 基本情報
			/*if(target===undefined || target==="data"){
				var targetDatas = [{}];
				$('#'+that.focusRootId).find('[col^=F]').each(function(){
					var col = $(this).attr('col');
					targetDatas[0][col] = $.getInputboxValue($(this));
				});

				for (var i=0; i<targetDatas.length; i++){
					var rowDate = {
							F1	 : targetDatas[i]["F1"],
							F2	 : targetDatas[i]["F2"],
							F4	 : targetDatas[i]["F3"],
							F5	 : targetDatas[i]["F4"],
							F6	 : targetDatas[i]["F5"],
							F7	 : targetDatas[i]["F6"],
					};
					if(rowDate){
						targetRows.push(rowDate);
					}
				}
				data["data"] = targetRows;
			}*/

			// 商品一覧
			if(target===undefined || target===$.id.grd_shohin){
				var rows	 = $('#'+$.id.grd_shohin).datagrid('getRows');			// 商品一覧
				var kkkcd	 = $('#'+$.id_inp.txt_kkkcd).numberbox('getValue');		// 企画No

				for (var i=0; i<rows.length; i++){
					var rowDate = {
							F1	 : kkkcd,
							F2	 : rows[i]["F2"].replace("-", ""),
							F3	 : rows[i]["F1"],
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
					if((oldrows[i]['F3'] ? oldrows[i]['F3'] : "") !== (newrows[i]['F3'] ? newrows[i]['F3'] : "")){
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
		// パラメータを元にDBに問い合わせた結果を取得、画面上に設定する
		getsetInputboxData: function(reportno, id, param, action){
			var that = this
			if(action===undefined) action = $.id.action_change;
			idx = -1;
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
					//$.extendDatagridEditor();

					// 店舗一覧
					that.setEditableGrid(that, reportno, $.id.grd_shohin);
				}
			});
			idx = 1;
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

			// 選択行
			var row = $('#'+$.id.grd_shohin).datagrid("getSelected");

			// 呼出別処理
			switch (btnId) {
			case $.id.btn_new:
				// 転送先情報
				if(that.reportYobiInfo()==='2'){
					index = 8;

				}else if(that.reportYobiInfo()==='1'){
					index = 9;

				}
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
				if(that.reportYobiInfo()==='2'){
					index = 8;

				}else if(that.reportYobiInfo()==='1'){
					index = 9;

				}
				childurl = href[index];
				sendMode = 1;

				var txt_kkkcd		= $.getJSONObject(this.jsonString, $.id_inp.txt_kkkcd).value;	// 店グループ

				$.setJSONObject(sendJSON, $.id_inp.txt_kkkcd, txt_kkkcd, txt_kkkcd);				// 企画No
				$.setJSONObject(sendJSON, $.id_inp.txt_shncd, row.F2, row.F2);						// 商品コード

				break;
			case $.id.btn_cancel:
			case $.id.btn_back:
			case "btn_return":
				// 転送先情報
				sendMode = 2;

				var reportYobi1 = $.getJSONValue(that.jsonHidden, "reportYobi1");

				if(that.reportYobiInfo()==='2'){
					index = 2;

				}else if(that.reportYobiInfo()==='1'){
					index = 3;

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