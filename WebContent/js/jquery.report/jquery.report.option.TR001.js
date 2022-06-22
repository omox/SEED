/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_ReportTR001',			// （必須）レポートオプションの確認
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
		dedefaultObjNum:	1,	// 初期化オブジェクト数
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
		clickBtnid: "",						// 当画面で押下されたボタンIDを保持
		focusRootId:"cc",					// （キー移動時必須）キー移動イベントのルートとなるパネルのID
		focusParentId:"",					// （キー移動時必須）現在フォーカスがあたっている項目の親となるパネルのID
		focusGridId:"",						// （キー移動時必須）現在フォーカスがあたっているDataGridのID
		editRowIndex:{},					// グリッド編集行保持
		gridData:[],						// 検索結果
		gridTitle:[],						// 検索結果
		kbnList:{},
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

			that.queried = true;


			// 入力テキストボックス系
			var inputbox = Object.getOwnPropertyNames($.id_inp);
			for ( var sel in inputbox ) {
				if($('#'+$.id_inp[inputbox[sel]]).length > 0){
					$.setInputbox(that, reportno, $.id_inp[inputbox[sel]], isUpdateReport);
				}
			}

			// 配送店グループ
			that.setEditableGrid(that, reportno, $.id.grd_hatstrshnten+'_list');

			var count = 1;
			// 名称マスタ参照系
			var meisyoSelect = Object.getOwnPropertyNames($.id_mei);
			for ( var sel in meisyoSelect ) {
				if($('#'+$.id_mei[meisyoSelect[sel]]).length > 0){
					$.setMeisyoCombo(that, reportno, $.id_mei[meisyoSelect[sel]], isUpdateReport);
					count++;
				}
			}

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

			// 当帳票を「新規」で開いた場合
			$.initReportInfo("TR001", "定量　通常　新規", "新規");

			// 各種遷移ボタン
			$('#'+$.id.btn_cancel).on("click", $.pushChangeReport);

			$.setInputBoxDisable($('#'+$.id_inp.txt_binkbn));
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
			var txt_hsgpcd = $.getJSONObject(this.jsonString, $.id_inp.txt_hsgpcd).value;	// 配送グループコード

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$.appendMaskMsg();

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,		// レポート名
					HSGPCD:			txt_hsgpcd,
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

					$.win001.init(that);	// 仕入先
					$.win002.init(that);	// メーカー
					$.win006.init(that);	// 商品コード

					// ログ出力
					$.log(that.timeData, 'loaded:');
				}
			);
		},
		updValidation: function (){	// （必須）批准

			//配送点グループグリッドの編集を終了する。
			var row = $('#'+$.id.grd_hatstrshnten+'_list').datagrid("getSelected");
			var rowIndex = $('#'+$.id.grd_hatstrshnten+'_list').datagrid("getRowIndex", row);
			$('#'+$.id.grd_hatstrshnten+'_list').datagrid('endEdit',rowIndex);

			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform).form('validate');

			// 必須入力チェック：商品コード
			var shncd = $.getInputboxValue($('#'+$.id_inp.txt_shncd));
			if(!shncd || shncd == ""){
				$.showMessage("EX1033");
				return false;
			}

			if(!rt){
				$.addErrState(that, $('.validatebox-invalid').eq(0), false);
				return rt;
			}
			return rt;

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

			var targetDatas = [{}];
			$('#'+that.focusRootId).find('[col^=F]').each(function(){
				var col = $(this).attr('col');
				targetDatas[0][col] = $.getInputboxValue($(this));
			});

			// 入力データ：正規定量_商品
			var targetRowsHstgp = that.getGridData($.id.grd_hatstrshnten+'_list');

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$.appendMaskMsg();
			$.appendMask();

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,		// レポート名
					action:			$.id.action_update,	// 実行処理情報
					obj:			id,								// 実行オブジェクト
					SENDBTNID:		that.sendBtnid,
//					IDX:			$($.id.hiddenChangedIdx).val(),		// 更新対象Index
//					DATA:			JSON.stringify(targetRows),			// 更新対象情報
					DATA:			JSON.stringify(targetDatas),		// 更新対象情報(商品・便)
					DATA_HTSS:		JSON.stringify(targetRowsHstgp),	// 更新対象情報(正規定量_商品)
					AREA:			$("input[name="+$.id.rad_areakbn+"]:checked").val(), // エリア区分
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
			var row = $('#'+$.id.grd_hatstrshnten+'_list').datagrid("getSelected");

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$.appendMaskMsg();
			$.appendMask();

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,						// レポート名
					action:			$.id.action_delete,				// 実行処理情報
					obj:			id,								// 実行オブジェクト
					DATA:			JSON.stringify(targetDatas),	// 更新対象情報(配送グループ)
					HSTENGPCD:		row.HSTENGPCD,					// 配送グループ店コード
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
			$('#'+id).datagrid({
				url:$.reg.easy,
				rownumbers:false,
				columns:[[
							{field:'WEEK',			title:'',				width: 30,	halign:'center',editor:{options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;text-align:center;';}},
							{field:'TEISEIKBN4',	title:'訂正区分',		width:130,	halign:'center',editor:{type:'combobox'}
								,formatter:function(value,row,index){
									if (undefined===value || null===value || ''===value) return '';
									if(value.indexOf('-')===-1){
										if(that.kbnList.length===undefined){
											var datas = $('#'+$.id_mei.kbn10501+that.suffix).combobox('getData');
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
							{field:'SURYO',			title:'全店同一数量',	width: 90,	align:'right',halign:'center',editor:'numberbox'},
						]],
				onBeforeLoad:function(param){
					index = -1;
					var json = [{"callpage":"Out_ReportTR001"}];
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
				onClickRow: function(index,field){
					//$.clickEditableDatagridCell(that,id, index)
					setTimeout(function(){
						$.clickEditableDatagridCell(that,id, index)
						//$.setFocusFirst($('#'+id).datagrid('getPanel').find("[datagrid-row-index='"+index+"']"));
					},200);
				},
				onBeginEdit:function(index,row){
					//$.beginEditDatagridRow(that,id, index, row)
					setTimeout(function(){
						$.beginEditDatagridRow(that,id, index, row)
						//$.setFocusFirst($('#'+id).datagrid('getPanel').find("[datagrid-row-index='"+index+"']"));
					},200);
				},
				onEndEdit: function(index,row,changes){$.endEditDatagridRow(that, id, index, row)}
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
					sendMode:	1,
					sendParam:	JSON.stringify( sendJSON )
				}
			});
		},
		getGridData: function (target){

			var targetRows= [];

			// 配送店グループ
			if(target===undefined || target===$.id.grd_hatstrshnten+'_list'){
				var rowsHstgp= $('#'+$.id.grd_hatstrshnten+'_list').datagrid('getRows');
				for (var i=0; i<rowsHstgp.length; i++){
					if(rowsHstgp[i]["WEEK"] == "" || rowsHstgp[i]["WEEK"] == null ){

					}else{
						var kbn = rowsHstgp[i]["TEISEIKBN4"].split("-")[0];
						if(kbn==="-1"){ kbn = "";}
						var rowDate = {
								F1	 : rowsHstgp[i]["WEEK"],
								F2	 : kbn,
								F3	 : rowsHstgp[i]["SURYO"],
							};
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
				var targetRowsHstgp	= $('#'+$.id.grd_hatstrshnten+'_list').datagrid('getRows');

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

					if (i===that.editRowIndex[$.id.grd_hatstrshnten+'_list']) {
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