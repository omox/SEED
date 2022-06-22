/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_Reportx204',			// （必須）レポートオプションの確認
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
		dedefaultObjNum:	4,	// 初期化オブジェクト数
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

			// 初期検索可能
			that.onChangeReport = true;

			// 入力テキストボックス系
			var inputbox = Object.getOwnPropertyNames($.id_inp);
			for ( var sel in inputbox ) {
				if($('#'+$.id_inp[inputbox[sel]]).length > 0){
					$.setInputbox(that, reportno, $.id_inp[inputbox[sel]], isUpdateReport);
				}
			}

			// 配送グループ店
			that.setEditableGrid(that, reportno, $.id.grd_gpten+'_list');

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

			// 当帳票を「新規」で開いた場合
			if(that.sendBtnid===$.id.btn_new){
				$('#'+$.id.btn_del).linkbutton('disable').attr('disabled', 'disabled').hide();
				$.initReportInfo("SI035", "配送グループ　店グループ一覧　新規", "新規");
			} else if (that.sendBtnid===$.id.btn_sel_refer) {
				$('#'+$.id.btn_del).linkbutton('disable').attr('disabled', 'disabled').hide();
				$('#'+$.id.btn_upd).linkbutton('disable').attr('disabled', 'disabled').hide();
				$('#'+$.id.btn_cancel).linkbutton('disable').attr('disabled', 'disabled').hide();
				$.initReportInfo("SI036", "配送グループ　店グループ一覧　参照", "参照");
			}else{
				$.initReportInfo("SI036", "配送グループ　店グループ一覧　変更", "変更");
			}

			// 各種遷移ボタン
			$('#'+$.id.btn_cancel).on("click", $.pushChangeReport);

			// 更新非対象項目は非活性に
			$.setInputBoxDisable($('#'+$.id_inp.txt_hsgpcd));
			$.setInputBoxDisable($('#'+$.id_inp.txt_hsgpkn));
			$.setInputBoxDisable($('#'+$.id_inp.txt_hstengpcd));
			$.setInputBoxDisable($('#'+$.id_inp.txt_tengpkn));

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
			var txt_hsgpcd		= $.getJSONObject(this.jsonString, $.id_inp.txt_hsgpcd).value;		// 配送グループコード
			var txt_hstengpcd	= $.getJSONObject(this.jsonString, $.id_inp.txt_hstengpcd).value;	// 配送店グループコード

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$.appendMaskMsg();

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,		// レポート名
					HSGPCD:			txt_hsgpcd,
					HSTENGPCD:		txt_hstengpcd,
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

					// データグリッド初期化
					that.setEditableGrid(that, reportno, $.id.grd_gpten+'_list');

					// 状態保存
					$.saveState2(reportno, that.getJSONString());

					// ログ出力
					$.log(that.timeData, 'loaded:');
				}
			);
		},
		updValidation: function (){	// （必須）批准

			//配送グループ店グリッドの編集を終了する。
			var row = $('#'+$.id.grd_gpten+'_list').datagrid("getSelected");
			var rowIndex = $('#'+$.id.grd_gpten+'_list').datagrid("getRowIndex", row);
			$('#'+$.id.grd_gpten+'_list').datagrid('endEdit',rowIndex);

			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform).form('validate');
			if(!rt){
				$.addErrState(that, $('.validatebox-invalid').eq(0), false);
				return rt;
			}

			// TODO: 背景を赤くする対応を追加
			//var msgid = that.checkInputboxFunc($.id_inp.txt_tencd);
			var msgid = null;
			if(msgid !==null){
				$.showMessage(msgid);
				return false;
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

			// 入力データ：配送グループ店
			var targetRowsHsgpt = that.getGridData($.id.grd_gpten+'_list');

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
					DATA:			JSON.stringify(targetDatas),		// 更新対象情報(配送グループ)
					DATA_HSGPT:		JSON.stringify(targetRowsHsgpt),	// 更新対象情報(配送店グループ)
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

					// 一覧画面へ戻る
					//this.endUpdate();
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
				var col = $(this).attr('col');
				targetDatas[0][col] = $.getInputboxValue($(this));
			});

			// 入力データ：配送店グループ
			var targetRowsHsgpt = that.getGridData($.id.grd_gpten+'_list');

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$.appendMaskMsg();

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,							// レポート名
					action:			$.id.action_delete,					// 実行処理情報
					obj:			id,									// 実行オブジェクト
					DATA:			JSON.stringify(targetDatas),		// 更新対象情報(配送グループ)
					DATA_HSGPT:		JSON.stringify(targetRowsHsgpt),	// 更新対象情報(配送グループ店)
					t:			(new Date()).getTime()
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
			// 配送グループコード
			this.jsonTemp.push({
				id:		$.id_inp.txt_hsgpcd,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_hsgpcd),
				text:	''
			});
			// 配送店グループコード
			this.jsonTemp.push({
				id:		$.id_inp.txt_hstengpcd,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_hstengpcd),
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
			var index = -1;
			var targetId = $.id_inp.txt_tencd;
			var check = $('#'+targetId).attr("check") ? JSON.parse('{'+$('#'+targetId).attr("check")+'}'): JSON.parse('{}');	// コードのcheck要素を取得
			var formatterLPad = function(value){
				return $.getFormatLPad(value, check.maxlen);
			};

			var funcEnter = function(e){
				if ($.endEditingDatagrid(that)){
					$.pushUpd(e);
				}
			};
			var funcClickRow = $.fn.datagrid.defaults.onClickRow;
			var funcBeginEdit= $.fn.datagrid.defaults.onBeginEdit;
			var funcEndEdit= $.fn.datagrid.defaults.onEndEdit;
			var funcAfterEdit= $.fn.datagrid.defaults.onAfterEdit;
			if(that.sendBtnid!==$.id.btn_sel_refer){
				that.editRowIndex[id] = -1;
				funcClickRow = function(index,field){$.clickEditableDatagridCell(that,id, index)};
				funcBeginEdit = function(index,row){
					$.beginEditDatagridRow(that,id, index, row)
				};
				funcEndEdit = function(index,row,changes){
					$.endEditDatagridRow(that, id, index, row);
				};
				funcAfterEdit = function(index,row,changes){
					var rowobj = $('#'+id).datagrid('getPanel').find("[datagrid-row-index='"+index+"']");
					// チェックボックスの再追加（EndEdit時に削除されるため）
					$.afterEditAddCheckbox(rowobj);
					// ボタンオブジェクトの再追加（EndEdit時に削除されるため）
					rowobj.find(".easyui-linkbutton").on("click", $.pushUpd).linkbutton({ width:  45, height: 18});
				};

				// 編集可能データグリッドの共通処理設定
				// 編集エディターの機能拡張（非表示で参照用のオブジェクトが準備してある前提、参照オブジェクトはグリッドのフィールド名を元に判断）
				$.extendDatagridEditor();
			}

			$('#'+id).datagrid({
				url:$.reg.easy,
				columns:[[
							{field:'TENCD',	title:'店番'				,width: 50  ,halign:'center',align:'left',formatter:formatterLPad,editor:{type:'numberbox'}},
							{field:'TENKN',	title:'店舗名称'	,width: 279 ,halign:'center',align:'left',editor:{type:'textbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}}
						]],
				onBeforeLoad:function(param){
					index = -1;
					var txt_hsgpcd		= $.getInputboxValue($('#'+$.id_inp.txt_hsgpcd));
					var txt_hstengpcd	= $.getInputboxValue($('#'+$.id_inp.txt_hstengpcd));

					var json = [{"callpage":"Out_Reportx204","HSGPCD":txt_hsgpcd,"HSTENGPCD":txt_hstengpcd}];
					// 情報設定
					param.page		=	reportno;
					param.obj		=	id;
					param.sel		=	(new Date()).getTime();
					param.target	=	id;
					param.action	=	$.id.action_init;
					param.json		=	JSON.stringify(json);
					param.datatype	=	"datagrid";
				},
				onLoadSuccess:function(data){},
				onClickRow: funcClickRow,
				onBeginEdit:funcBeginEdit,
				onEndEdit: funcEndEdit,
				onAfterEdit: funcAfterEdit
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
			var sendMode = "1";	// 遷移後に、前回検索条件を表示したい場合、""のまま、ここで設定した条件を表示したい場合"1"

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

			// 戻る実行時用に現在の画面情報を保持する
			var states = $.getBackBaseJSON(that);
			// 各種グリッド情報を設定
			var newrepinfos = $.getBackJSON(that, states);
			$.setJSONObject(sendJSON, 'repinfo', newrepinfos, '');

			// 呼出別処理
			switch (btnId) {
			case $.id.btn_cancel:
			case $.id.btn_back:
			case "btn_return":

				// 転送先情報
				sendMode = 2;

				// 転送先情報
				index = 4;
				childurl = href[index];

				// オブジェクト作成
				var txt_hsgpcd = $.getInputboxValue($('#'+$.id_inp.txt_hsgpcd))
				$.setJSONObject(sendJSON, $.id_inp.txt_hsgpcd, txt_hsgpcd, txt_hsgpcd);

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
			if(id+"_"===obj.attr('id') && that.focusGridId!==""){
				parentObj = $('#'+that.focusGridId).datagrid('getPanel');
			}

			// DB問い合わせ系
			if($('[for_inp^='+id+'_]').length > 0){
				var param = that.getInputboxParams(that, id, newValue);
				var rows = $.getSelectListData(that.name, $.id.action_change, id, param);
				var row = rows.length > 0 ? rows[0]:"";
				$.setInputboxRowData('for_inp', id, row, that, parentObj);
			}

			// TODO: 背景を赤くする対応を追加
			//var msgid = that.checkInputboxFunc(id,newValue);
			var msgid = null;
			if(msgid !==null){
				$.showMessage(msgid);
				return false;
			}
		},
		getInputboxParams: function(that, id, newValue){
			// 情報取得
			var values = {};
			values["value"] = newValue;

			// 情報設定
			return [values];
		},
		getGridData: function (target){

			var targetRows= [];

			// 配送グループ店
			if(target===undefined || target===$.id.grd_gpten+'_list'){
				var rowsHstgp= $('#'+$.id.grd_gpten+'_list').datagrid('getRows');
				for (var i=0; i<rowsHstgp.length; i++){
					if(rowsHstgp[i]["TENCD"] == "" || rowsHstgp[i]["TENCD"] == null ){

					}else{
						var rowDate = {
								F1	 : rowsHstgp[i]["TENCD"],
								F2	 : rowsHstgp[i]["TENGPKN"],
							};
						targetRows.push(rowDate);
					}
				}
			}
			return targetRows;
		},
		checkInputboxFunc: function(id, newValue){

			var that = this;

			// 店コード重複・マスタ存在チェック
			if (id===$.id_inp.txt_tencd) {

				if (newValue !== null && newValue !== '' && newValue !== undefined) {
					// マスタ存在チェック
					var param = {};
					param["KEY"] =  "MST_CNT";
					param["value"] = newValue;

					var chk_cnt = $.getInputboxData(that.name, $.id.action_check, $.id_inp.txt_tencd, [param]);
					if(chk_cnt==="" || chk_cnt==="0"){
						return "E11096";
					}
				}

				// 配送店グループ一覧
				var gptencd		= "";
				var tenkn		= "";
				var gptencds	= [];
				var errFlg		= true; // グリッドの入力チェックに使用
				var targetRows	= $('#'+$.id.grd_gpten+'_list').datagrid('getRows');

				for (var i=0; i<targetRows.length; i++){
					// 配送店グループコード、店名を格納
					gptencd	= targetRows[i]["TENCD"];
					tenkn	= targetRows[i]["TENKN"];

					// 店コードに入力がされた場合
					if (newValue !== null && newValue !== undefined) {

						// 入力があった場合更新対象
						errFlg = false;

						if (i===that.editRowIndex[$.id.grd_gpten+'_list']) {

							// 重複チェック用
							if (newValue !== '') {
								gptencds.push(newValue);
							}
						} else if (gptencd !== null && gptencd !== undefined) {

							// 名称の取得ができていない場合
							if (gptencd !== '') {
								// 重複チェック用
								gptencds.push(gptencd);

								// 店コードに入力があった場合比較
								if (tenkn === '' || tenkn === null || tenkn === undefined) {
									return "E11096";
								}
							}
						}

					} else {

						// 店舗コードに入力がある場合
						if (gptencd !== '' && gptencd !== null && gptencd !== undefined) {

							// 入力があった場合更新対象
							errFlg = false;

							// 名称の取得ができていない場合
							if (tenkn === '' || tenkn === null || tenkn === undefined) {
								return "E11096";
							}

							// 重複チェック用
							gptencds.push(gptencd);
						}
					}
				}

				// 入力が存在しなかった場合
				if (errFlg) {
					return 'EX1017';
				}

				// 重複チェック
				var gptencds_ = gptencds.filter(function (element, index, self) { return self.indexOf(element)*1 === index; });
				if(gptencds.length !== gptencds_.length){
					return 'E11141';
				}
			}
			return null;
		},
	} });
})(jQuery);