/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_Reportx151',			// （必須）レポートオプションの確認
		jsonTemp:	[],							// （必須）検索条件情報_入力チェック前
		jsonString:	[],							// （必須）検索条件情報
		jsonHidden: [],							// （必須）親画面からの引き継ぎ情報
		jsonInit: [],							// （必須）検索条件初期情報
		caption: function(){					// （必須）タイトル
			return $('#reportname').val();
		},
		sortName: '',							// ソート項目名
		sortOrder: '',							// ソート順
		timeData : (new Date()).getTime(),
		dedefaultObjNum:	4,					// 初期化オブジェクト数
		initObjNum:	-1,
		initedObject: [],
		maxMergeCell: 1,
		onChangeFlag : false,
		onChangeFlag2 : false,
		columnName:'',	// OnClickRowの列名
		queried : false,
		initializes : true,
		onChangeReport: false,
		reportYobiInfo: function(){				// （必須）レポートメニューに登録された予備情報＝帳票をどういった表示にしたいかの情報
			return $('#reportYobi1').val();
		},
		sendBtnid: "",							// 呼出ボタンID情報
		pushBtnId: "",							// 実行ボタンID情報(検索系で利用)
		focusRootId:"cc",						// （キー移動時必須）キー移動イベントのルートとなるパネルのID
		focusParentId:"",						// （キー移動時必須）現在フォーカスがあたっている項目の親となるパネルのID
		focusGridId:"",							// （キー移動時必須）現在フォーカスがあたっているDataGridのID
		initialize: function (reportno){		// （必須）初期化
			var that = this;
			// 引き継ぎ情報
			this.jsonHidden = $.getTargetValue();

			// 画面の初回基本設定
			this.setInitObjectState();

			// 初期検索条件設定
			this.jsonInit = $.getInitValue();
			// データ表示エリア初期化
			that.setGrid($.id.gridholder, reportno);

			// 初期化するオブジェクト数設定
			this.initObjNum = this.dedefaultObjNum;

			var isUpdateReport = false;

			// 仕入先コード
			$.setInputbox(that, reportno, $.id.txt_sel_sircd, isUpdateReport);

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

			// 初期化終了
			this.initializes =! this.initializes;

			$.initialSearch(that);

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
				that.sendBtnid = sendBtnid;
				$.reg.search = true;
			}
			// 当帳票を「参照」で開いた場合
			if(that.reportYobiInfo()==='1'){
				// 新規登録ボタン非表示
				$.initReportInfo("SI001", "仕入先一覧(参照)");
				$('#'+$.id.btn_new).linkbutton('disable');
				$('#'+$.id.btn_new).attr('disabled', 'disabled').hide();
				$('#btn_id_change').linkbutton('disable');
				$('#btn_id_change').attr('disabled', 'disabled').hide();
				$('#btn_id_sel_refer').on("click", function(){

					var txt_sircd		 = $.getInputboxValue($('#'+$.id.txt_sel_sircd));
					var msgid = that.checkInputboxFunc($.id_inp.txt_sircd, txt_sircd, '');
					if(msgid !== null){
						$.showMessage(msgid, undefined);
						return false;
					}else{
						that.changeReport(that.name, 'btn_id_sel_refer');
					}
				});
				$('#'+$.id.btn_sel_change).on("click", $.pushChangeReport);
				$('#'+$.id.btn_sel_refer).on("click", $.pushChangeReport);
				$('#'+$.id.btn_sel_change).linkbutton('disable').attr('disabled', 'disabled').hide();

			}else{
				$.initReportInfo("SI001", "仕入先一覧");
				$($.id.buttons).show();
				// 各種遷移ボタン
				$('#'+$.id.btn_new).on("click", $.pushChangeReport);
				$('#'+$.id.btn_sel_change).on("click", $.pushChangeReport);
				$('#btn_id_change').on("click", function(){
					var txt_sircd		 = $.getInputboxValue($('#'+$.id.txt_sel_sircd));
					var msgid = that.checkInputboxFunc($.id_inp.txt_sircd, txt_sircd, '');
					if(msgid !== null){
						$.showMessage(msgid, undefined);
						return false;
					}else{
						that.changeReport(that.name, 'btn_id_change')
					}
				});
				$('#'+$.id.btn_sel_refer).linkbutton('disable').attr('disabled', 'disabled').hide();
				$('#btn_id_sel_refer').linkbutton('disable');
				$('#btn_id_sel_refer').attr('disabled', 'disabled').hide();
				// レポート番号取得
				var reportno = $($.id.hidden_reportno).val();
				// レポート定義位置
				var reportNumber = $.getReportNumber(reportno);
				//$('#btn_id_change').on("click", this.changeReport(reportno, 'btn_id_change'));

			}
			//$.initReportInfo("SI001", "仕入先一覧");
		},
		initCondition: function (){	// 条件初期値セット
			var that = this;
			// 初期化項目
		},
		clear:function(){
//			// 隠し情報初期化
//			$($.id.hiddenChangedIdx).val("");						// 変更行Index
//			// グリッド初期化
//			this.success(this.name, false);
		},
		validation: function (){	// （必須）批准
			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform).form('validate');
			var txt_sircd		= $('#'+$.id.txt_sel_sircd).textbox('getValue');			// 仕入先コード
			var txt_sirkn		= $('#'+$.id_inp.txt_sirkn).textbox('getValue'); 			// 仕入先名称
			var kbn403			= $('#'+$.id_mei.kbn403).combobox('getValue');				// 仕入先用途
			var kbn404			= $('#'+$.id_mei.kbn404).combobox('getValue');				// いなげや在庫

			if(!txt_sircd && !txt_sirkn && kbn403 == '-1' && kbn404 == '-1'){
				$.showMessage('EX1013');
				rt = false;
			}

			// 入力エラーなしの場合に検索条件を格納
			if (rt == true) that.jsonString = that.jsonTemp.slice(0);
			// 入力チェック用の配列をクリア
			that.jsonTemp = [];


			return rt;
		},
		success: function(reportno, sortable, btnId){	// （必須）正処理
			if (sortable) sortable=1; else sortable=0;
			var that = this;

			var txt_sircd		= $.getJSONObject(this.jsonString, $.id.txt_sel_sircd).value;		// 仕入先コード
			var txt_sirkn		= $.getJSONObject(this.jsonString, $.id_inp.txt_sirkn).value;		// 仕入先名称
			var kbn403			= $.getJSONObject(this.jsonString, $.id_mei.kbn403).value;			// 仕入先用途
			var kbn404			= $.getJSONObject(this.jsonString, $.id_mei.kbn404).value;			// いなげや在庫
			if(!btnId) btnId = $.id.btn_search;

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
					report:			that.name,		// レポート名
					SIRCD:			txt_sircd,
					SIRKN:			txt_sirkn,
					SHIIRESAKIYOTO:	kbn403,
					INAGEYAZAIKO:	kbn404,
					t:				(new Date()).getTime(),
					sortable:		sortable,
					sortName:		that.sortName,
					sortOrder:		that.sortOrder,
					rows:			1		// 表示可能レコード数
				},
				function(json){
					// 検索処理エラー判定
					if($.searchError(json, undefined, that)) return false;

					// ログ出力
					$.log(that.timeData, 'query:');

					// Load処理回避
					$.tryChangeURL(null);

					var limit = 1000;
					var size = JSON.parse(json)["total"];
					if(size > limit){
						$.showMessage('E00010');
					}

					if (sortable===0){
						var options = $($.id.gridholder).datagrid('options');
						// 初期検索時に並び替え情報のリセット
						options.sortName = null;
						options.sortOrder = null;
					}

					that.queried = true;
					that.pushBtnId = btnId;
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
		getEasyUI: function(){	// （必須）情報の取得
			// 初期化
			this.jsonTemp = [];

			// レポート名
			this.jsonTemp.push({
				id:		"reportname",
				value:	this.caption(),
				text:	this.caption()
			});
			// 仕入先コード
			this.jsonTemp.push({
				id:		$.id.txt_sel_sircd,
				value:	$('#'+$.id.txt_sel_sircd).textbox('getValue'),
				text:	$('#'+$.id.txt_sel_sircd).textbox('getText')
			});
			// 仕入先名称
			this.jsonTemp.push({
				id:		$.id_inp.txt_sirkn,
				value:	$('#'+$.id_inp.txt_sirkn).textbox('getValue'),
				text:	$('#'+$.id_inp.txt_sirkn).textbox('getText')
			});
			// 仕入先用途
			this.jsonTemp.push({
				id:		$.id_mei.kbn403,
				value:	$('#'+$.id_mei.kbn403).combobox('getValue'),
				text:	$('#'+$.id_mei.kbn403).combobox('getText')
			});
			// いなげや在庫
			this.jsonTemp.push({
				id:		$.id_mei.kbn404,
				value:	$('#'+$.id_mei.kbn404).combobox('getValue'),
				text:	$('#'+$.id_mei.kbn404).combobox('getText')
			});
		},
		setGrid: function (id, reportNumber){	// グリッドの構築
			var that = this;
			var init = true;
			// ページサイズ定義取得
			var pageList = $.fn.pagination.defaults.pageList;
			var pageSize = $.getJSONValue(this.jsonHidden, $.id.pageSize);
			if (pageSize==="") pageSize=$.fn.pagination.defaults.pageSize;
			pageSize = $.getDefaultPageSize(pageSize, pageList);
			$(id).datagrid({
				nowrap: true,
				border: true,
				striped: true,
				collapsible:false,
				remoteSort: true,
				rownumbers:true,
				fit:true,
				//view:scrollview,
				pageSize:pageSize,
				pageList:pageList,
				frozenColumns:[[]],
				columns:[[
				    {field:'F1',	title:'仕入先コード',				width: 100,halign:'center',align:'left'},
				    {field:'F2',	title:'仕入先名称',					width: 300,halign:'center',align:'left'},
					{field:'F3',	title:'登録日',						width:  70,halign:'center',align:'left'},
					{field:'F4',	title:'更新日',						width:  70,halign:'center',align:'left'},
				]],
				fitColumns:false,	// 指定カラム幅を適用する場合、false 指定。
				rowStyler:function(index, row){
				},
				onSortColumn:function(sort, order){
					if (that.jsonString.length===0) return false;
					// カラム並び替え
					that.success(that.name, true);
				},
				onBeforeLoad:function(param){
					param.report = that.name;
				},
				onLoadSuccess:function(data){
					// 検索後、初回のみ処理
					if (that.queried){
						that.queried = false;	// 検索後、初回のみ処理
						// 状態保存
						$.saveState(reportNumber, that.getJSONString(), id);
						// 警告
						$.showWarningMessage(data);
						that.loadSuccessFunc(id, data);
					}

					// 前回選択情報をGridに反映
					var getRowIndex = $.getJSONValue(that.jsonHidden, "scrollToIndex_"+id);
					if(getRowIndex !== ""){
						$(id).datagrid('scrollTo', {
							index: getRowIndex,
							callback: function(index){
								$(id).datagrid('selectRow', index);
							}
						});

						// 検索可能な一覧画面では、検索時に前回選択情報が適用されてしまう為、
						// 一度適用したら要素を削除する。
						var targetName = "scrollToIndex_"+id;
						that.jsonHidden.some(function(v, i){
						    if (v.id==targetName) that.jsonHidden.splice(i,1);
						});
					}
				},
				onClickCell:function(rowIndex, field, value){
					// 列名保持
					that.columnName = field;
				},
				onClickRow:function(rowIndex, rowData){
					// ドリルリンク
					//that.changeReport($.id.column_class, that.columnName, rowData);
				},
				autoRowHeight:false,
				pagination:false,
				pagePosition:'bottom',
				singleSelect:true
			});
			if (	(!jQuery.support.opacity)
				&&	(!jQuery.support.style)
				&&	(typeof document.documentElement.style.maxHeight == "undefined")
				) {
				// ページリストに select を利用している。IE6  のバグで z-index が適用されない。
				// modalダイアログを利用する場合は、表示なしにする必要あり。
				$.fn.pagination.defaults.showPageList = false;
			}
		},
		loadSuccessFunc:function(id, data){				// 画面遷移
			var that = this;

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

			// タブ要素(a)取得
			var elems = $('#tabContent', window.parent.document).map(
				function(i,e) {
					return e;
				}).get();
			var href = elems[0].value.split(',');

			// JSON Object Clone ()
			var sendJSON = [];
			$.setJSONObject(sendJSON, 'sendBtnid', btnId, $('#'+btnId).attr("title"));		// 実行ボタン情報保持

			// 戻る実行時用に現在の画面情報を保持する
			var states = $.getBackBaseJSON(that);
			// TODO 各種グリッド情報を設定
			var newrepinfos = $.getBackJSON(that, states,true);
			$.setJSONObject(sendJSON, 'repinfo', newrepinfos, '');

			// 選択行
			var row = $($.id.gridholder).datagrid("getSelected");

			// 実行ボタン別処理
			switch (btnId) {
			case $.id.btn_new:
				// 転送先情報
				index = 3;
				childurl = href[index];

				$.setJSONObject(sendJSON, 'reportYobi1', that.reportYobiInfo(), that.reportYobiInfo());		// 参照情報保持
				break;
			case $.id.btn_sel_change:
			case $.id.btn_sel_refer:
				if(!row){
					$.showMessage('E00008');
					return false;
				}

				// 転送先情報
				index = 3;
				childurl = href[index];

				// オブジェクト作成
				$.setJSONObject(sendJSON, $.id_inp.txt_sircd, row.F1, row.F1);
				$.setJSONObject(sendJSON, 'reportYobi1', that.reportYobiInfo(), that.reportYobiInfo());		// 参照情報保持

				break;
			case 'btn_id_change':
			case 'btn_id_sel_refer':
				// 変更(コード指定)ボタン押下時
				var txt_sircd = $('#'+$.id.txt_sel_sircd).textbox('getValue');

				if(txt_sircd === ""){
					$.showMessage('E00007');
					return false;
				}

				if(btnId === 'btn_id_change' || btnId === 'btn_id_sel_refer'){
					that.getEasyUI();
					that.jsonString = that.jsonTemp.slice(0);
					$.saveState(that.name, that.getJSONString(), $.id.gridholder);
				}

				if(that.reportYobiInfo() !== '1'){
					$('#reportYobi1').val('2');
				}

				// 転送先情報
				index = 3;
				childurl = href[index];

				// オブジェクト作成
				$.setJSONObject(sendJSON, $.id_inp.txt_sircd, txt_sircd, txt_sircd);
				$.setJSONObject(sendJSON, 'reportYobi1', that.reportYobiInfo(), that.reportYobiInfo());		// 参照情報保持
				break;
			case $.id.btn_back:
				// 転送先情報
				childurl = parent.$('#hdn_menu_path').val();
				break;
			default:
				break;
			}

			$.SendForm({
				type: 'post',
				url: childurl,
				data: {
					sendMode:	1,
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
		keyEventInputboxFunc:function(e, code, that, obj){

			var id = $(obj).attr("orizinid");

			// *** Enter or Tab ****
			if(code === 13 || code === 9){
				// 仕入先コード
				if(id===$.id.txt_sel_sircd){

					var value = $.getInputboxValue($('#'+id));

					// 仕入先コード、配送パターンで配送パターン仕入先マスタの存在チェック
					var param = {};
					if(value !== ""){
						param["KEY"] =  "MST_CNT";
						param["value"] = value;
						var sircd = $.getInputboxData(that.name, $.id.action_check,  $.id_inp.txt_sircd, [param]);
						if(sircd===""||sircd === "0"){
							$.showMessage('E11021');
							return false;

						}else {
							if(!$.isEmptyVal(value)){
								that.changeReport(that.name, 'btn_id_change');
								return false;
							}
						}
					}
				}
			}
		},
		// IDとvalueでチェック処理を実施
		checkInputboxFunc:function(id, newValue, kbn, record, isNew){
			var that = this;
			var sdt, edt;

			// 仕入先コード
			if(id===$.id_inp.txt_sircd){
				if(newValue !== '' && newValue){

					/*if(that.reportYobiInfo()==='0'){
						// 新規登録時は入力仕入先コードと等しい場合チェックを行わない。
						var txt_sircd		 = $.getInputboxValue($('#'+$.id_inp.txt_sircd));

						if(txt_sircd == newValue){
							return null;
						}
					}*/

					// 仕入先コード
					var param = {};
					param["KEY"] =  "MST_CNT";
					param["value"] = newValue;
					var chk_cnt = $.getInputboxData(that.name, $.id.action_check, $.id_inp.txt_sircd, [param]);
					if(chk_cnt==="" || chk_cnt==="0"){
						return "E11099";

					}
				}
			}
			return null;
		},
	} });
})(jQuery);