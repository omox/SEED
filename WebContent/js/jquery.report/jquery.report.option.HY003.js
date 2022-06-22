/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_ReportHY003',			// （必須）レポートオプションの確認
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
		maxMergeCell: 1,
		onChangeFlag : false,
		onChangeFlag2 : false,
		columnName:'',	// OnClickRowの列名
		queried : false,
		initializes : true,
		onChangeReport: false,
		reportYobiInfo: function(){			// （必須）レポートメニューに登録された予備情報＝帳票をどういった表示にしたいかの情報
			return $('#reportYobi1').val();
		},
		pushBtnId: "",						// （必須）実行ボタンID情報(検索系で利用)
		focusRootId:"cc",					// （キー移動時必須）キー移動イベントのルートとなるパネルのID
		focusParentId:"",					// （キー移動時必須）現在フォーカスがあたっている項目の親となるパネルのID
		focusGridId:"",						// （キー移動時必須）現在フォーカスがあたっているDataGridのID
		editRowIndex:{},					// グリッド編集行保持
		grd_hatsu:[],						// グリッド情報:発注数一覧
		data_yh_nndt:[],					// 更新情報:予約発注_納入日
		initialize: function (reportno){	// （必須）初期化
			var that = this;

			// 引き継ぎ情報
			this.jsonHidden = $.getTargetValue();
			// 画面の初回基本設定
			this.setInitObjectState();
			// 初期検索条件設定
			this.jsonInit = $.getInitValue();
			// 処理日付取得
			that.getsetInputboxData(reportno, $.id.txt_shoridt, [{}], $.id.action_init);

			// 初期化するオブジェクト数設定
			this.initObjNum = this.dedefaultObjNum;

			var isUpdateReport = true;

			that.onChangeReport = true;

			var count = 2;
			// 入力テキストボックス系
			var inputbox = Object.getOwnPropertyNames($.id_inp);
			for ( var sel in inputbox ) {
				if($('#'+$.id_inp[inputbox[sel]]).length > 0){
					$.setInputbox(that, reportno, $.id_inp[inputbox[sel]], isUpdateReport);
					count++;
				}
			}

			// Load処理回避
			//$.tryChangeURL(null);
			$.initialDisplay(that);

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
			if(sendBtnid && sendBtnid.length > 0){
				$.reg.search = true;
			}
			$.initReportInfo("HY003", "予約発注　発注数登録");
			$('#'+$.id.btn_sel_change).on("click", $.pushChangeReport);

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

			// initialDisplayでのMaskMsgを削除
			$.removeMaskMsg();

			// 検索実行
			var shoridt			= $('#'+$.id.txt_shoridt).val();									// 処理日付
			var txt_kkkcd		= $.getJSONObject(this.jsonString, $.id_inp.txt_kkkcd).value;		// 企画No
			var txt_shncd		= $.getJSONObject(this.jsonString, $.id_inp.txt_shncd).value;		// 商品コード
			var txt_catalgno	= $.getJSONObject(this.jsonString, $.id_inp.txt_catalgno).value;	// カタログNo

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			$($.id.gridholder).datagrid('loading');

			// grid.options 取得
			var options = $($.id.gridholder).datagrid('options');
			that.sortName	= options.sortName;
			that.sortOrder	= options.sortOrder;

			$.post(
				$.reg.jqgrid ,
				{
					report			:that.name,					// レポート名
					SENDBTNID		:that.sendBtnid,
					SHORIDT			:shoridt,
					KKKCD			:txt_kkkcd,
					SHNCD			:txt_shncd,
					CATALGNO		:txt_catalgno,
					t				:(new Date()).getTime(),
					sortable		:sortable,
					sortName		:that.sortName,
					sortOrder		:that.sortOrder,
					rows			:0							// 表示可能レコード数
				},
				function(json){
					// 検索処理エラー判定
					if($.searchError(json)) return false;

					// ログ出力
					$.log(that.timeData, 'query:');

					// Load処理回避
					$.tryChangeURL(null);

					var opts = JSON.parse(json).opts
					if(opts && opts.rows_){
						// 基本データ表示
						that.setData(opts.rows_, opts);
					}

					if (sortable===0){
						var options = $($.id.gridholder).datagrid('options');
						// 初期検索時に並び替え情報のリセット
						options.sortName = null;
						options.sortOrder = null;
					}

					that.queried = true;
					// Load処理回避
					$.tryChangeURL($.reg.jqeasy);

					// グリッド再描画（easyui 1.4.2 対応）
					$($.id.gridholder).datagrid('load', {} );
					$.removeMask();

					// ログ出力
					$.log(that.timeData, 'loaded:');
				}
			);
		},
		updValidation: function (){	// （必須）批准

			//配送点グループグリッドの編集を終了する。
			var row = $($.id.gridholder).datagrid("getSelected");
			var rowIndex = $($.id.gridholder).datagrid("getRowIndex", row);
			$($.id.gridholder).datagrid('endEdit',rowIndex);

			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform).form('validate');

			if(!rt){
				$.addErrState(that, $('.validatebox-invalid').eq(0), false);
				return rt;
			}

//			//予約発注_店舗の更新データを取得
//			var targetDatas = [];
//			targetDatas = that.getMergeGridDate( 'gridholder');
//			for (var i=0; i<targetDatas.length; i++){
//				var data = targetDatas[i]
//				nnstdt = data.F3
//
//				// 入力不可フラグ = "1"の場合
//				if(data.F8 == '1'){
//					$.showMessage('E20222');
//					return false;
//				}
//			}
			return rt;
		},
		updSuccess: function(id){	// validation OK時 の update処理
			var that = this;

			// 変更行情報取得
			/*var changedIndex = $($.id.hiddenChangedIdx).val().split(",");

			var targetDatas = [{}];
			$('#'+that.focusRootId).find('[col^=F]').each(function(){
				var col = $(this).attr('col');
				targetDatas[0][col] = $.getInputboxValue($(this));
			});

			// 新規登録時には配送パターン
			if(that.sendBtnid =  $.id.btn_sel_change){
				var enptyrows = [];
				targetDatas = enptyrows;
			}

			// エリア別設定グリッド(登録)のデータを取得
			var targetDatas_Ahsptn = [];
			var txt_hsptn = $('#'+$.id_inp.txt_hsptn).textbox('getValue');
			targetDatas_Ahsptn = that.getMergeGridDate(txt_hsptn, $.id.grd_ehsptn + '_hp012');

			// エリア別設定グリッド(削除)のsデータを取得
			var targetDatas_Ahsptn_del = [];
			targetDatas_Ahsptn_del = that.getMergeGridDate(txt_hsptn, $.id.grd_ehsptn + '_hp012', 'del');*/


			//予約発注_店舗の更新データを取得
			var targetDatas_YHTEN = [];
			targetDatas_YHTEN = that.getMergeGridDate( 'gridholder');

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			//$.appendMaskMsg();

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,		// レポート名
					action:			$.id.action_update,	// 実行処理情報
					obj:			id,								// 実行オブジェクト
					SENDBTNID:		that.sendBtnid,
//					IDX:			$($.id.hiddenChangedIdx).val(),	// 更新対象Index
					DATA:			JSON.stringify(that.data_yh_nndt),	// 更新対象情報
					DATA_TEN:		JSON.stringify(targetDatas_YHTEN),  // 更新対象情報(予約発注_店舗)
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
		setData: function(rows, opts){		// データ表示
			var that = this;

			if(rows.length > 0){
				$('#'+that.focusRootId).find('[col^=F]').each(function(){
					var col = $(this).attr('col');
					if(rows[0][col]){
						//$.setInputboxValue($(this), rows[0][col]);
						if(col == 'F5' || col == 'F6'){
							//$.setInputboxValue($(this), rows[0][col]);
							$.setInputboxValue($(this), $.getFormat(rows[0][col], '#,##0'));
						}else if(col == 'F4'){
							$.setInputboxValue($(this), $.getFormat(rows[0][col], '#,##0.00'));
						}else{
							$.setInputboxValue($(this), rows[0][col]);
						}
					}
				});
			}
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
			// カタログNo
			this.jsonTemp.push({
				id:		$.id_inp.txt_catalgno,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_catalgno),
				text:	''
			});
		},
		extenxDatagridEditorIds:{
			 F3		: "txt_htsu",		// 発注数
			 F4		: "txt_sousu"		// 発注数

		},
		setGrid2: function (id, reportNumber){	// グリッドの構築
			var that = this;
			var init = true;

			var dformatter =function(value){ return $.getFormatDt(value, true);};
			var cstyler=function(value,row,index){return 'color: red;font-weight: bold;';};
			var cformatter =function(value,row,index){return value && value===$.id.value_on?$.id.text_on:$.id.text_off;};
			var iformatter = function(value,row,index){ return $.getFormat(value, '#,##0');};

			var columns = [];
			var columnBottom=[];

			columnBottom.push({field:'F1',	title:'納入日',				width:100,halign:'center',align:'left'});
			columnBottom.push({field:'F2',	title:'前日までの数量',	width:100,halign:'center',align:'right', formatter:iformatter});
			columnBottom.push({field:'F3',	title:'本日分',				width: 80,halign:'center',align:'right', formatter:iformatter,editor:{type:'numberbox'}});
			columnBottom.push({field:'F4',	title:'総合計',				width: 80,halign:'center',align:'right', formatter:iformatter,editor:{type:'numberbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}});
			columnBottom.push({field:'F5',	title:'入力フラグ',			width: 80,halign:'center',align:'right',hidden:true});
			columnBottom.push({field:'F6',	title:'更新日付',			width: 80,halign:'center',align:'right',hidden:true});
			columnBottom.push({field:'F7',	title:'納品日(登録処理用)',	width: 80,halign:'center',align:'right',hidden:true});


			columns.push(columnBottom);

			var funcClickRow = $.fn.datagrid.defaults.onClickRow;
			var funcBeginEdit= $.fn.datagrid.defaults.onBeginEdit;
			var funcEndEdit= $.fn.datagrid.defaults.onEndEdit;
			var funcAfterEdit= $.fn.datagrid.defaults.onAfterEdit;
			if(that.reportYobiInfo()!=='1'){
				that.editRowIndex[id] = -1;
				funcClickRow = function(index,field){$.clickEditableDatagridCell(that,id, index)};
				funcBeginEdit = function(index,row){
					$.beginEditDatagridRow(that,id, index, row);
					/*if(row.F5==='1'){
						$.setInputBoxDisable($("#"+$.id_inp.txt_htsu + '_'));
						$.setInputBoxDisable($("#"+$.id_inp.txt_sousu + '_'));

						$('#'+id).datagrid('getPanel').find("[datagrid-row-index='"+index+"']").find("[id^='"+$.id_inp.txt_htsu+"_"+"']").each(function(){
							var that = this;
							var target = $(that).textbox('textbox');
							target.attr('tabindex', -1);
							target.attr('readonly', 'readonly');
							target.attr('disabled', 'disabled');
						});
					}*/
				};
				funcEndEdit = function(index,row,changes){$.endEditDatagridRow(that, id, index, row)};
				funcAfterEdit = function(index,row,changes){
					var rowobj = $('#'+id).datagrid('getPanel').find("[datagrid-row-index='"+index+"']");
					// チェックボックスの再追加（EndEdit時に削除されるため）
					$.afterEditAddCheckbox(rowobj);
				};
				// 編集可能データグリッドの共通処理設定
				// 編集エディターの機能拡張（非表示で参照用のオブジェクトが準備してある前提、参照オブジェクトはグリッドのフィールド名を元に判断）
				$.extendDatagridEditor(that);
			}

			$('#'+id).datagrid({
				nowrap: true,
				border: true,
				striped: true,
				collapsible:false,
				remoteSort: true,
				rownumbers:false,
				fit:true,
				view:scrollview,
				frozenColumns:[[]],
				columns:columns,
				fitColumns:false,	// 指定カラム幅を適用する場合、false 指定。
				rowStyler:function(index, row){
				},
				onSortColumn:function(sort, order){
					if (that.jsonString.length===0) return false;
					// カラム並び替え
					that.success(that.name, true);
				},
				onLoadSuccess:function(data){
					// 検索後、初回のみ処理
					if (that.queried){
						//that.queried = false;	// 検索後、初回のみ処理
						// 状態保存
						$.saveState(reportNumber, that.getJSONString(), '#'+id);
						// 警告
						$.showWarningMessage(data);
					}

					// 検索結果を保持
					var gridData = that.getGridData(id);
					that.setGridData(gridData, id);
				},
				onClickCell:function(rowIndex, field, value){
					// 列名保持
					that.columnName = field;
				},
				onBeforeEdit:function(index,row){
					var shoridt			= $('#'+$.id.txt_shoridt).val();									// 処理日付
					if(row.F5==='1'){
						// 次の行に移るか、次の項目に移るかする
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
				},
				onClickRow: funcClickRow,
				onBeginEdit:funcBeginEdit,
				onEndEdit: funcEndEdit,
				onAfterEdit: funcAfterEdit,
				autoRowHeight:false,
				pagination:false,
				singleSelect:true
			});
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

					// 編集可能データグリッドの共通処理設定
					// 編集エディターの機能拡張（非表示で参照用のオブジェクトが準備してある前提、参照オブジェクトはグリッドのフィールド名を元に判断）
					$.extendDatagridEditor();

					// 発注数量一覧
					//that.setGrid($.id.gridholder, reportno);
					//that.setEditableGrid(that, reportno, 'gridholder');
					that.setGrid2('gridholder', reportno);
				}
			});
			idx = 1;
		},
		getGridData: function (target, del){
			var that = this;

			var data = {};
			var targetRows= [];

			// 発注数一覧
			if(target===undefined || target==='gridholder'){

				var txt_kkkcd		= $.getJSONObject(this.jsonString, $.id_inp.txt_kkkcd).value;		// 企画No
				var txt_shncd		= $.getJSONObject(this.jsonString, $.id_inp.txt_shncd).value;		// 商品コード
				var txt_shoridt		= $('#'+$.id.txt_shoridt).val();									// 処理日付

				var rows	 = $('#'+target).datagrid('getRows');
				for (var i=0; i<rows.length; i++){
					var rowDate = {
							F1	 : txt_kkkcd,									// 企画No
							F2	 : txt_shncd,									// 商品コード
							F3	 : rows[i]["F7"],								// 納入日
							F4	 : txt_shoridt,									// 処理日付
							F5	 : rows[i]["F3"],								// 発注数
							F6	 : rows[i]["F6"],								// 更新日(排他チェック用)
							F7	 : rows[i]["F5"],								// 入力不可フラグ(入力禁止チェック用)
					};
					targetRows.push(rowDate);
				}
				data[target] = targetRows;
			}
			return data;
		},
		setGridData: function (data, target){
			var that = this;

			// 基本情報
			/*if(target===undefined || target==="data"){
				that.data =  data["data"];
			}*/

			// 商品一覧
			if(target===undefined || target==='gridholder'){
				that.grd_hatsu =  data['gridholder'];
			}
		},
		getMergeGridDate: function(target, del){
			// 保持したデータと入力データ比較を比較する。
			var that = this;

			var newrows			 = that.getGridData(target)[target] ? that.getGridData(target)[target] : [];		// 変更データ
			var oldrows			 = [];
			var targetRows		 = [];
			that.data_yh_nndt	 = [];

			// 発注数一覧
			if(target===undefined || target==='gridholder'){
				oldrows = that.grd_hatsu
				for (var i=0; i<newrows.length; i++){

					if((oldrows[i]['F5'] ? oldrows[i]['F5'] : "") !== (newrows[i]['F5'] ? newrows[i]['F5'] : "")){
						if((oldrows[i]['F1'] && oldrows[i]['F1'] !== "")
								&& (oldrows[i]['F2'] && oldrows[i]['F2'] !== "")
								&& (oldrows[i]['F3'] && oldrows[i]['F3'] !== "")
								&& (oldrows[i]['F4'] && oldrows[i]['F4'] !== "")
						){
							var rowDate = {
									F1	 : newrows[i]["F1"],							// 企画No
									F2	 : newrows[i]["F2"],							// 商品コード
									F3	 : newrows[i]["F3"],							// 納入日
									F4	 : newrows[i]["F4"],							// 処理日付
									F5	 : '',											// 担当店舗(DaoのyuserInfより設定を行う)
									F6	 : newrows[i]["F5"],							// 発注数
									F7	 : newrows[i]["F6"],							// 更新日(排他チェック用)
									F8	 : newrows[i]["F7"],							// 入力禁止フラグ(入力禁止チェック用)
							};
							var rowDateParent = {
									F1	 : newrows[i]["F1"],							// 企画No
									F2	 : newrows[i]["F2"],							// 商品コード
									F3	 : newrows[i]["F3"],							// 納入日
							};
							if(rowDate){
								targetRows.push(rowDate);
								// 予約発注_納入日(親データ)の更新情報
								that.data_yh_nndt.push(rowDateParent);
							}
						}
					}
				}
			}
			return targetRows;
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
			$.setJSONObject(sendJSON, 'sendBtnid', btnId, $('#'+btnId).attr("title"));					// 実行ボタン情報保持
			$.setJSONObject(sendJSON, 'reportYobi1', that.reportYobiInfo(), that.reportYobiInfo());		// 参照情報保持
			$.setJSONObject(sendJSON, 'callpage', that.name, that.name);								// 呼出し元レポート情報

			// 戻る実行時用に現在の画面情報を保持する
			var states = $.getBackBaseJSON(that);
			// 各種グリッド情報を設定
			var newrepinfos = $.getBackJSON(that, states);
			$.setJSONObject(sendJSON, 'repinfo', newrepinfos, '');// 呼出し元レポート情報

			// 選択行
			var row = $($.id.gridholder).datagrid("getSelected");

			// 戻る実行時用に現在の画面情報を保持する
			var states = $.getBackBaseJSON(that);
			// 各種グリッド情報を設定
			var newrepinfos = $.getBackJSON(that, states);
			$.setJSONObject(sendJSON, 'repinfo', newrepinfos, '');


			// 実行ボタン別処理
			switch (btnId) {
			case $.id.btn_back:
			case 'btn_return':
				// 転送先情報
				index = 4;
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
		csv: function(reportno){	// Csv出力
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
				'kbn'	: kbn,
				'type'	: 'csv'
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


				// 発注数
				if(id===$.id_inp.txt_htsu){
					var rows	 = $($.id.gridholder).datagrid("getSelected");						// 商品一覧
					var hatsus	 = rows["F2"];			// 前日までの発注数量
					var sohatsus = Number(hatsus) + Number(newValue);

					$.setInputboxValue($('#'+$.id_inp.txt_sousu+'_'), sohatsus);

					// $.getInputboxTextbox($('#'+$.id_inp.hatsu+'_'));
					/*$($.id.gridholder).datagrid('updateRow',{
						index: that.editRowIndex['gridholder'],
						//F1: sohatsus,
						row: {
							F3: newValue,
							F4: sohatsus,
						}
					})*/
				}
				if(msgid !==null){
					$.showMessage(msgid, undefined, func_focus );
					return false;
				}

				// グリッド編集系
				if(that.focusGridId!==undefined && that.editRowIndex[that.focusGridId] > -1){
					// その他の入力項目のエラーチェック
					/*var moyskbn = $.id.value_moykbn_r*1;
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
					}*/
				}
			}
		},
		checkInputboxFunc:function(id, newValue, kbn, record, isNew){
			var that = this;
			var sdt, edt;

			// 発注数
			if(id===$.id_inp.txt_htsu && (newValue!=='' && newValue)){
				var rows	 = $($.id.gridholder).datagrid('getSelected');
				var nnstdt = rows.F7;									// 納入日
				var nneddt = $('#'+$.id.txt_shoridt).val();				// 処理日付
				sdt = $.convDate(nnstdt, true);
				edt = $.convDate(nneddt, true);

				if(rows.F5 == '1'){
					// 入力不可フラグ="1"の時
					return "E20273";
				}else if(sdt.getTime() < edt.getTime()){
					// 店舗締切日 < 処理日付の時
					return "E20273";
				}
			}
			return null;
		},
	} });
})(jQuery);