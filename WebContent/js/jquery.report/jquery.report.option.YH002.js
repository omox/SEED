/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_ReportYH002',			// （必須）レポートオプションの確認
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
		dedefaultObjNum:	16,	// 初期化オブジェクト数
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
		grd_nohin_data:[],					// グリッド情報:店舗一覧
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

			// チェックボックス設定：削除
			$.setCheckboxInit2(that.jsonHidden, $.id.chk_del, that);

			//$('#'+$.id.chk_del).change(function() {
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

			// 編集可能データグリッドの共通処理設定
			// 編集エディターの機能拡張（非表示で参照用のオブジェクトが準備してある前提、参照オブジェクトはグリッドのフィールド名を元に判断）
			$.extendDatagridEditor();

			// 店舗一覧
			that.setEditableGrid(that, reportno, $.id.grd_nohin);


			// 初期化終了
			this.initializes =! this.initializes;

			// 名称を検索するため、フォーカスを動かす。
			var newval = $('#'+$.id_inp.txt_shncd).numberbox('getValue');
			$('#'+$.id_inp.txt_shncd).numberbox('reset').numberbox('setValue', newval);

			// チェックボックスの設定
			$.initCheckboxCss($("#"+that.focusRootId));
			// キーイベントの設定
			$.initKeyEvent(that);
			// ログ出力
			$.log(that.timeData, 'initialize:');
		},
		setInitObjectState: function(){	// 画面初期化時の項目制御
			var that = this;

			var test = that.reportYobiInfo();

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

			var sendBtnid = $.getJSONValue(that.jsonHidden, "sendBtnid");
			if(sendBtnid && sendBtnid.length > 0){
				$.reg.search = true;
			}

			if(sendBtnid === $.id.btn_sel_change){
				$.initReportInfo("YH002", "予約発注　変更　商品情報");

				// 処理日付
				/*var shoridt  = $('#'+$.id.txt_shoridt).val();
				var htdt	 = $.getInputboxValue($('#'+$.id_inp.txt_htdt));	// 発注日

				var tenistdt	 = $.getInputboxValue($('#'+$.id_inp.txt_tenistdt));
				var txt_tenieddt	 = $.getInputboxValue($('#'+$.id_inp.txt_tenieddt));

				var sdt = $.convDate(shoridt, true);
				var edt = $.convDate(htdt, true);
				if(sdt.getTime() >= edt.getTime()){
					// 処理日 > 発注日
					$.showMessage("E20019");
					return false;
				}

				var sdt = $.convDate(tenistdt, true);
				var edt = $.convDate(txt_tenieddt, true);
				if(sdt.getTime() >= edt.getTime()){
					// 処理日 >= 店入力開始日
					$.showMessage("E20019");
					return false;
				}

				var sdt = $.convDate(tenistdt, true);
				var edt = $.convDate(htdt, true);
				if(sdt.getTime() >= edt.getTime()){
					// 処理日 >= 店入力終了日
					$.showMessage("E20019");
					return false;
				}*/


				/*$.setInputBoxDisable($("#"+$.id_inp.txt_kkkcd));
				$.setInputBoxDisable($("#"+$.id_inp.txt_shnkn));
				$.setInputBoxDisable($("#"+$.id_inp.txt_shncd));
				$.setInputBoxDisable($("#"+$.id_inp.txt_htdt));
				$.setInputBoxDisable($("#"+$.id_inp.txt_tenistdt));
				$.setInputBoxDisable($("#"+$.id_inp.txt_tenieddt));*/
				$.setInputBoxDisable($("#"+$.id_inp.txt_shnkn));
				$.setInputBoxDisable($("#"+$.id_inp.txt_shncd));
				$.setInputBoxDisable($("#kikan_dummy"));
				$.setInputBoxDisable($("#kikaku_dummy"));

			}else if(sendBtnid === $.id.btn_new){
				$.initReportInfo("YH002", "予約発注　新規　商品情報");
				$.setInputBoxDisable($("#"+$.id_inp.txt_kkkcd));
				$.setInputBoxDisable($("#"+$.id_inp.txt_shnkn));
				$.setInputBoxDisable($("#kikaku_dummy"));
				$.setInputBoxDisable($("#kikan_dummy"));
				$("#disp_record_info").hide();
			}

			$.setInputBoxDisable($('#'+$.id_inp.txt_shnkn));
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

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$.appendMaskMsg();

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,		// レポート名
					KKKCD:			txt_kkkcd,
					SHNCD:			txt_shncd.replace('-', ''),
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
			var tenistdt	 = $.getInputboxValue($('#'+$.id_inp.txt_tenistdt));	// 店舗入力開始日
			var tenieddt	 = $.getInputboxValue($('#'+$.id_inp.txt_tenieddt));	// 店舗入力終了日

			if(htdt !== ''){
				var sdt = $.convDate(htdt, true);
				var edt = $.convDate(shoridt, true);
				if(sdt.getTime() <= edt.getTime()){
					// 発注日 <= 処理日付
					$.setInputBoxDisable($("#"+$.id_inp.txt_htdt));
				}
			}

			if(tenistdt !== ''){
				var sdt = $.convDate(tenistdt, true);
				var edt = $.convDate(shoridt, true);
				if(sdt.getTime() <= edt.getTime()){
					// 店舗入力開始日 <= 処理日付
					$.setInputBoxDisable($("#"+$.id_inp.txt_tenistdt));
				}
			}

			if(tenieddt !== ''){
				var sdt = $.convDate(tenieddt, true);
				var edt = $.convDate(shoridt, true);
				if(sdt.getTime() < edt.getTime()){
					// 店舗入力終了日 < 処理日付
					$.setInputBoxDisable($("#"+$.id_inp.txt_tenieddt));
				}
			}
		},
		updValidation: function (){	// （必須）批准
			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform).form('validate');
			if(!rt){
				$.addErrState(that, $('.validatebox-invalid').eq(0), false);
				return rt;
			}

			$.id.grd_nohin
			var row = $('#'+$.id.grd_nohin).datagrid("getSelected");
			var rowIndex = $('#'+$.id.grd_nohin).datagrid("getRowIndex", row);
			$('#'+$.id.grd_nohin).datagrid('endEdit',rowIndex);

			// 商品コード
			var param = $('#'+$.id_inp.txt_shncd).numberbox('getValue');
			if(param || param!==''){
				var msgid = that.checkInputboxFunc($.id_inp.txt_shncd, param , '');
				if(msgid !==null){
					$.showMessage(msgid, undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_shncd), true)});
					//$.showMessage(msgid);
					return false;
				}
			}

			// 受付開始日
			var param = $('#'+$.id_inp.txt_ukestdt).numberbox('getValue');
			if(param || param!==''){
				var msgid = that.checkInputboxFunc($.id_inp.txt_ukestdt, param , '');
				if(msgid !==null || msgid){
					$.showMessage(msgid, undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_ukestdt), true)});
					//$.showMessage(msgid);
					return false;
				}
			}

			// 受付終了日
			var param = $('#'+$.id_inp.txt_ukeeddt).numberbox('getValue');
			if(param || param!==''){
				var msgid = that.checkInputboxFunc($.id_inp.txt_ukeeddt, param , '');
				if(msgid !==null){
					$.showMessage(msgid, undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_ukeeddt), true)});
					//$.showMessage(msgid);
					return false;
				}
			}

			// 店舗入力開始日
			var param = $('#'+$.id_inp.txt_tenistdt).numberbox('getValue');
			if(param || param!==''){
				var msgid = that.checkInputboxFunc($.id_inp.txt_tenistdt, param , '');
				if(msgid !==null){
					//$.showMessage(msgid);
					$.showMessage(msgid, undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_tenistdt), true)});
					return false;
				}
			}

			// 店舗入力終了日
			var param = $('#'+$.id_inp.txt_tenieddt).numberbox('getValue');
			if(param || param!==''){
				var msgid = that.checkInputboxFunc($.id_inp.txt_tenieddt, param , '');
				if(msgid !==null){
					$.showMessage(msgid, undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_tenieddt), true)});
					//$.showMessage(msgid);
					return false;
				}
			}

			// 発注日
			var param = $('#'+$.id_inp.txt_htdt).numberbox('getValue');
			if(param || param!==''){
				var msgid = that.checkInputboxFunc($.id_inp.txt_htdt, param , '');
				if(msgid !==null){
					$.showMessage(msgid, undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_htdt), true)});
					//$.showMessage(msgid);
					return false;
				}
			}

			// グリッド項目：納入日
			var gridData = that.getGridData($.id.grd_nohin);	// 検証用情報取得
			var targetOId = [$.id_inp.txt_nndt];
			var targetCId = ["F3"];
			var targetRows = gridData[$.id.grd_nohin];

			var yoteisu = $.getInputboxValue($('#'+$.id_inp.txt_yoteisu));	// 設定予定数
			var gendosu = $.getInputboxValue($('#'+$.id_inp.txt_gendosu));	// 設定限度数

			var yoteisuKei = 0	// グリッド内予定数計
			var gendosuKei = 0	// グリッド内限度数計

			for (var i=0; i<targetRows.length; i++){
				for (var j = 0; j < targetOId.length; j++){
					var value = targetRows[i][targetCId[j]]
					if(!value || value !== ""){
						msgid = that.checkInputboxFunc(targetOId[j], targetRows[i][targetCId[j]], '');
						if(msgid !==null){
							$.showMessage(msgid,undefined, function(){$.addErrState(that, $('#'+that.focusGridId), true, {NO:i, ID:targetOId[j] + '_'})});
							return false;
						}
					}
				}

				// 予定数未入力チェック、合計値計算
				if(targetRows[i]["F3"] && targetRows[i]["F3"] !=''){
					// 納入日の入力がある場合
					if(targetRows[i]["F4"] && targetRows[i]["F4"] !=''){
						yoteisuKei += Number(targetRows[i]["F4"])
					}else{
						if(yoteisu && yoteisu !== ""){
							if(Number(yoteisu) != 0){
								$.showMessage("EX1047",['一覧表示部の｢予定数｣'], function(){$.addErrState(that, $('#'+that.focusGridId), true, {NO:i, ID:$.id_inp.txt_yoteisu + '_'})});
								return false;
							}
						}
					}
				}

				// 限度数未入力チェック、合計値計算
				if(targetRows[i]["F3"] && targetRows[i]["F3"] !=''){
					// 納入日の入力がある場合
					if(targetRows[i]["F5"] && targetRows[i]["F5"] !=''){
						gendosuKei += Number(targetRows[i]["F5"])
					}else{
						if(gendosu && gendosu !== ""){
							if(Number(gendosu) != 0){
								$.showMessage("EX1047",['一覧表示部の｢限度数｣'], function(){$.addErrState(that, $('#'+that.focusGridId), true, {NO:i, ID:$.id_inp.txt_gendosu + '_'})});
								return false;
							}
						}
					}
				}
			}

			// 予定数
			if(yoteisu && yoteisu !== ""){
				// 予定数と、一覧表予定数の合計値が等しくない場合
				if(Number(yoteisu) !== yoteisuKei){
					$.showMessage("E20216", undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_yoteisu), true)});
					return false;
				}
			}else{
				if(yoteisuKei > 0){
					// 予定数未設定でグリッド内に入力がある場合
					$.showMessage("E20216", undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_yoteisu), true)});
					return false;
				}
			}

			// 限度数
			if(gendosu && gendosu !== ""){
				// 限度数と、一覧表限度数の合計値が等しくない場合
				if(Number(gendosu) !== gendosuKei){
					$.showMessage("E20217", undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_gendosu), true)});
					return false;
				}
			}else{
				if(gendosuKei > 0){
					// 限度数未設定でグリッド内に入力がある場合
					$.showMessage("E20217", undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_gendosu), true)});
					return false;
				}
			}

			// 納入日
			var targetdate = [];
			var targetRows = $('#'+$.id.grd_nohin).datagrid('getRows');
			for (var i=0; i<targetRows.length; i++){
				targetdate.push(targetRows[i]["NNDT"]);
			}

			// 重複チェック：納入日
			if(targetdate.length > 0){
				var targetdateF = []
				targetdate.filter(function (x, i, self) {
					if(x && x != ""){
			            if(Number(self.indexOf(x)) !== i || Number(self.lastIndexOf(x)) !== i){
			            	targetdateF.push(i)
			            };
					}
		        });

				// グリッド内の塗り潰し状態をクリアする。
				$.removeErrStateGrid($.id.grd_nohin);

				if(targetdateF.length > 0){
					// 重複箇所を塗り潰し
					var targetColIndex = 0		// 商品コードの項目順番
					$.addErrStateGrid($.id.grd_nohin, targetdateF, [targetColIndex]);
					$.showMessage('E20023');
					return false;
				}
			}else {
				// グリッド内に入力が1件もない場合
				$.showMessage('EX1047',["納入日"]);
				return false;
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

			/*var targetDatas = [{}];
			$('#'+that.focusRootId).find('[col^=F]').each(function(){
				var col = $(this).attr('col');
				targetDatas[0][col] = $.getInputboxValue($(this));
			});*/

			// 基本入力情報取得
			var targetDatas = that.getGridData("data")["data"];

			// 新規登録時には配送パターン
			/*if(that.sendBtnid =  $.id.btn_sel_change){
				var enptyrows = [];
				targetDatas = enptyrows;
			}*/

			// 納品日一覧のデータを取得
			var targetDatasNhn = that.getMergeGridDate($.id.grd_nohin);

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
					DATA_NHN:		JSON.stringify(targetDatasNhn),	// 更新対象情報
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
			var dformatter =function(value){ return $.getFormatDt(value, true);};
			var cformatter =function(value,row,index){return value && value===$.id.value_on?$.id.text_on:$.id.text_off;};
			var wformatter =function(value,row,index){return $.getFormatWeek(row[this.field.replace("_W", "")]);};
			var bcstyler =function(value,row,index){return 'background-color:#f5f5f5;';};
			var iformatter = function(value,row,index){ return $.getFormat(value, '#,##0');};

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
				frozenColumns:[[]],
				columns:[[
							{field:'NNDT',		title:'納入日',		width:  70,halign:'center',align:'left', formatter:dformatter,editor:{type:'numberbox'}},
							{field:'WEE',		title:'',			width: 	40,halign:'center',align:'center', styler:bcstyler},
							{field:'YOTEISU',	title:'予定数',		width:  70,halign:'center',align:'right', formatter:iformatter, editor:{type:'numberbox'}},
							{field:'GENDOSU',	title:'限度数',		width:  70,halign:'center',align:'right', formatter:iformatter, editor:{type:'numberbox'}},
							]],
				url:$.reg.easy,
				onBeforeLoad:function(param){
					index = -1;
					var values = {};
					var kkkcd			 = $('#'+$.id_inp.txt_kkkcd).numberbox('getValue');		// 企画No
					var shncd			 = $('#'+$.id_inp.txt_shncd).numberbox('getValue');		// 商品コード

					values["callpage"]	 = $($.id.hidden_reportno).val()						// 呼出元レポート名
					values["KKKCD"]		 = kkkcd												// 企画No
					values["SHNCD"]	 	 = shncd												// 商品コード

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
				onBeforeEdit:function(index){
					var rows		 = $('#'+id).datagrid('getRows');
					var shoridt		 = $('#'+$.id.txt_shoridt).val();
					var tenistdt	 = $.getInputboxValue($('#'+$.id_inp.txt_tenistdt));
					var col_F1		 = $('#'+id).datagrid('getColumnOption', 'NNDT');

					var sdt = $.convDate(shoridt, true);
					var edt = $.convDate(tenistdt , true);
					if(sdt.getTime() < edt.getTime()){
						col_F1.editor = {
								type:'numberbox',
								formatter:dformatter
							}
					}else{
						col_F1.editor = false
					}
				},
				onClickRow: function(index,field){$.clickEditableDatagridCell(that,id, index)},
				onBeginEdit:function(index,row){$.beginEditDatagridRow(that,id, index, row)},
				onEndEdit: function(index,row,changes){$.endEditDatagridRow(that, id, index, row)},
				onAfterEdit: function(index,row,changes){
					// 重複行塗り潰し処理
					var rows	 = $('#'+$.id.grd_nohin).datagrid('getRows');
					var nndtData = []

					for (var i = 0; i < rows.length; i++){
						var row	  = rows[i]
						var nndt = row['NNDT']
						var value = "";

						if(nndt){
							value = nndt
						}

						nndtData.push(value);
					}

					var nndtData_ = []
					nndtData.filter(function (x, i, self) {
						if(x && x != ""){
				            if(Number(self.indexOf(x)) !== i || Number(self.lastIndexOf(x)) !== i){
				            	nndtData_.push(i)
				            };
						}
			        });

					// グリッド内の塗り潰し状態をクリアする。
					$.removeErrStateGrid($.id.grd_nohin);
					if(nndtData_.length > 0){
						var targetColIndex = 0		// 商品コードの項目順番
						// グリッド内の重複箇所を塗り潰し
						$.addErrStateGrid($.id.grd_nohin, nndtData_, [targetColIndex]);
					}
				}
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
							F1	 : targetDatas[i]["F2"],
							F2	 : targetDatas[i]["F7"],
							F3	 : targetDatas[i]["F6"],
							F4	 : targetDatas[i]["F8"],
							F5	 : targetDatas[i]["F9"],
							F6	 : targetDatas[i]["F10"],
							F7	 : targetDatas[i]["F11"],
							F8	 : targetDatas[i]["F12"],
							F9	 : targetDatas[i]["F13"],
							F10	 : targetDatas[i]["F14"],
							F11	 : targetDatas[i]["F15"],
							F17	 : targetDatas[i]["F19"],	// 入力不可フラグ
					};
					if(rowDate){
						targetRows.push(rowDate);
					}
				}
				data["data"] = targetRows;
			}

			// 納品日一覧
			if(target===undefined || target===$.id.grd_nohin){
				var rows	 = $('#'+$.id.grd_nohin).datagrid('getRows');			// 商品一覧
				var kkkcd	 = $('#'+$.id_inp.txt_kkkcd).numberbox('getValue');		// 企画No
				var shncd	 = $('#'+$.id_inp.txt_shncd).numberbox('getValue');		// 企画No

				for (var i=0; i<rows.length; i++){
					var rowDate = {
							F1	 : kkkcd,
							F2	 : shncd,
							F3	 : rows[i]["NNDT"],
							F4	 : rows[i]["YOTEISU"],
							F5	 : rows[i]["GENDOSU"],
					};
					targetRows.push(rowDate);
				}
				data[$.id.grd_nohin] = targetRows;
			}
			return data;
		},
		getMergeGridDate: function(target){
			// 保持したデータと入力データ比較を比較する。
			var that = this;

			var newrows = that.getGridData(target)[target];		// 変更データ
			var oldrows = [];
			var targetRows= [];


			if(target===undefined || target===$.id.grd_nohin){
				// 納品日一覧
				oldrows = that.grd_nohin_data
				/*for (var i=0; i<newrows.length; i++){
					if((oldrows[i]['F3'] ? oldrows[i]['F3'] : "") !== (newrows[i]['F3'] ? newrows[i]['F3'] : "")
					 	|| (oldrows[i]['F4'] ? oldrows[i]['F4'] : "") !== (newrows[i]['F4'] ? newrows[i]['F4'] : "")
						|| (oldrows[i]['F5'] ? oldrows[i]['F5'] : "") !== (newrows[i]['F5'] ? newrows[i]['F5'] : "")
					){
						if(newrows[i]["F3"] && newrows[i]["F3"].indexOf('_') == -1){
							var rowDate = {
									F1	 : newrows[i]["F1"],
									F2	 : newrows[i]["F2"],
									F3	 : newrows[i]["F3"],
									F4	 : newrows[i]["F4"],
									F5	 : newrows[i]["F5"],
							};
							if(rowDate){
								targetRows.push(rowDate);
							}
						}
					}
				}*/

				for (var i=0; i<newrows.length; i++){
					if(newrows[i]['F3'] && newrows[i]['F3'] !== ""){
						var rowDate = {
								F1	 : newrows[i]["F1"],
								F2	 : newrows[i]["F2"],
								F3	 : newrows[i]["F3"],
								F4	 : newrows[i]["F4"],
								F5	 : newrows[i]["F5"],
						};
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

			// 基本情報
			/*if(target===undefined || target==="data"){
				that.data =  data["data"];
			}*/

			// 商品一覧
			if(target===undefined || target===$.id.grd_nohin){
				that.grd_nohin_data =  data[$.id.grd_nohin];
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
				sendMode = 2;

				index = 14;
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

			// グリッド編集系
			if(that.focusGridId!==undefined && that.editRowIndex[that.focusGridId] > -1){
				var wfield = "";
				// テキスト（月締め)
				if(id===$.id_inp.txt_nndt){
					wfield = "WEE";
				}
				if(wfield!==""){
					var row   = $('#'+that.focusGridId).datagrid('getRows')[that.editRowIndex[that.focusGridId]];
					var rowobj= $('#'+that.focusGridId).datagrid('getPanel').find("[datagrid-row-index="+that.editRowIndex[that.focusGridId]+"]");
					row[wfield] = $.getFormatWeek(newValue);
					rowobj.find('[field='+wfield+']').find('div').text(row[wfield]);
				}
			}

			// 検索、入力後特殊処理
			if(that.queried){
				var msgid = null;

				// 店コード
				if(id===$.id_inp.txt_shncd){
					msgid = that.checkInputboxFunc(id, newValue, '');
				}

				// 納入日
				if(id===$.id_inp.txt_nndt){

					// 重複行塗り潰し処理
					var rows	 = $('#'+$.id.grd_nohin).datagrid('getRows');
					var nndtData = []

					var row = $('#'+$.id.grd_nohin).datagrid("getSelected");
					var rowIndex = $('#'+$.id.grd_nohin).datagrid("getRowIndex", row);

					for (var i = 0; i < rows.length; i++){
						var row	  = rows[i]
						var nndt = row['NNDT']
						var value = "";

						if(i == rowIndex){
							nndt = newValue
						}

						if(nndt){
							value = nndt
						}

						nndtData.push(value);
					}

					var nndtData_ = []
					nndtData.filter(function (x, i, self) {
						if(x && x != ""){
				            if(Number(self.indexOf(x)) !== i || Number(self.lastIndexOf(x)) !== i){
				            	nndtData_.push(i)
				            };
						}
			        });

					// グリッド内の塗り潰し状態をクリアする。
					$.removeErrStateGrid($.id.grd_nohin);
					if(nndtData_.length > 0){
						var targetColIndex = 0		// 商品コードの項目順番
						// グリッド内の重複箇所を塗り潰し
						$.addErrStateGrid($.id.grd_nohin, nndtData_, [targetColIndex]);
					}
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
		getInputboxParams: function(that, id, newValue){
			// 情報取得
			var values = {};
			values["value"] = newValue;
			values["TABLEKBN"] = that.baseTablekbn;

			// 商品コード
			if(id===$.id_inp.txt_hsptn){
				values["SHNCD"] = $.getInputboxValue($('#'+$.id_inp.txt_shncd));
			}

			// 情報設定
			return [values];
		},
		// IDとvalueでチェック処理を実施
		checkInputboxFunc:function(id, newValue, kbn, record, isNew){
			var that = this;
			var sdt, edt;

			// 商品コード
			if(id===$.id_inp.txt_shncd){
				var param = {};
				param["KEY"] =  "MST_CNT";
				param["value"] = newValue;
				var chk_cnt = $.getInputboxData(that.name, $.id.action_check, $.id_inp.txt_shncd, [param]);
				if(chk_cnt==="" || chk_cnt==="0"){
					return "E11046";
				}
			}

			// 受付期間
			if(id===$.id_inp.txt_ukestdt || id===$.id_inp.txt_ukeeddt && (newValue!=='' && newValue)){
				// 納入期間開始日
				if(id===$.id_inp.txt_ukestdt && (newValue!=='' && newValue)){
					// 受付開始日
					var ukeeddt = $('#'+$.id_inp.txt_ukeeddt).numberbox('getValue');
					sdt = $.convDate(newValue, true);
					if(ukeeddt || ukeeddt !== ''){
						edt = $.convDate(ukeeddt, true);
					}
				}

				if(id===$.id_inp.txt_ukeeddt && (newValue!=='' && newValue)){
					// 受付終了日
					var ukestdt = $('#'+$.id_inp.txt_ukestdt).numberbox('getValue');
					if(ukeeddt || ukeeddt !== ''){
						sdt = $.convDate(ukestdt, true);
					}
					edt = $.convDate(newValue , true);
				}
				if(sdt && edt && sdt !=='' && edt !==''){
					if(sdt.getTime() >= edt.getTime()){	// 期間が逆の場合
						return "E20017";
					}
				}
			}

			// 店舗入力開始日、店舗入力終了日
			if((id===$.id_inp.txt_tenistdt || id===$.id_inp.txt_tenieddt) &&  (newValue!=='' && newValue)){
				if(id===$.id_inp.txt_tenistdt){
					// 受付開始日と比較
					var ukestdt = $('#'+$.id_inp.txt_ukestdt).numberbox('getValue');
					if(ukestdt){
						sdt = $.convDate(newValue, true);
						edt = $.convDate(ukestdt, true);
						if(sdt || edt){
							if(sdt.getTime() < edt.getTime()){	// 期間が逆の場合
								return "E20020";
							}
						}
					}

					// 処理日付と比較
					var shoridt = $('#'+$.id.txt_shoridt).val();
					if(shoridt){
						sdt = $.convDate(shoridt, true);
						edt = $.convDate(newValue, true);
						if(sdt || edt){
							if(sdt.getTime() >= edt.getTime()){	// 期間が逆の場合
								return "E20215";
							}
						}
					}

					var tenieddt = $('#'+$.id_inp.txt_tenieddt).numberbox('getValue');
					sdt = $.convDate(newValue, true);
					if(tenieddt || tenieddt !== ''){
						edt = $.convDate(tenieddt, true);
					}
				}

				if(id===$.id_inp.txt_tenieddt && (newValue!=='' && newValue)){
					// 受付終了日と比較
					var ukeeddt = $('#'+$.id_inp.txt_ukeeddt).numberbox('getValue');
					if(ukeeddt){
						sdt = $.convDate(newValue, true);
						edt = $.convDate(ukeeddt, true);
						if(sdt || edt){
							if(sdt.getTime() < edt.getTime()){	// 期間が逆の場合
								return "E20021";
							}
						}
					}

					var tenistdt = $('#'+$.id_inp.txt_tenistdt).numberbox('getValue');
					if(tenistdt || tenistdt !== ''){
						sdt = $.convDate(tenistdt, true);
					}
					edt = $.convDate(newValue , true);
				}

				if(newValue && ukeeddt && newValue !=='' && ukeeddt !==''){
					if(sdt.getTime() >= edt.getTime()){	// 期間が逆の場合
						return "E20018";
					}
				}
			}

			// 発注日
			if(id===$.id_inp.txt_htdt && (newValue!=='' && newValue)){
				var tenieddt	 = $('#'+$.id_inp.txt_tenieddt).numberbox('getValue');	// 店舗入力終了日
				var nnstdt		 = $('#'+$.id_inp.txt_nnstdt).numberbox('getValue');	// 納入開始日

				sdt = $.convDate(tenieddt, true);
				edt = $.convDate(newValue, true);
				if(sdt.getTime() >= edt.getTime()){
					// 店舗入力終了日 > 発注日
					return "E20019";
				}

				sdt = $.convDate(newValue, true);
				edt = $.convDate(nnstdt, true);
				if(sdt.getTime() >= edt.getTime()){
					// 発注日 > 納入開始日
					return "E20019";
				}
			}

			// 納入日
			if(id===$.id_inp.txt_nndt && (newValue!=='' && newValue)){
				var nnstdt = $('#'+$.id_inp.txt_nnstdt).numberbox('getValue');
				var nneddt = $('#'+$.id_inp.txt_nneddt).numberbox('getValue');

				sdt = $.convDate(nnstdt, true);
				edt = $.convDate(newValue, true);
				if(sdt.getTime() > edt.getTime()){
					return "E20274";
				}

				sdt = $.convDate(newValue, true);
				edt = $.convDate(nneddt, true);
				if(sdt.getTime() > edt.getTime()){
					return "E20274";
				}
			}

			// 納入期間
			if(id===$.id_inp.txt_nnstdt||id===$.id_inp.txt_nneddt){
				 if( newValue!=='' && newValue){
					// 納入期間開始日
						if(id===$.id_inp.txt_nnstdt){
							// 販売期間終了日との比較
							var nneddt = $('#'+$.id_inp.txt_nneddt).numberbox('getValue');
							var shoridt = $('#'+$.id.txt_shoridt).val();

							sdt = $.convDate(newValue, true);
							edt = $.convDate(shoridt, true);

							if(sdt.getTime() < edt.getTime()){	// 期間が逆の場合
								return "E20034";
							}

							sdt = $.convDate(newValue, true);
							edt = $.convDate(nneddt, true);
						}

						// 納入期間終了日
						if(id===$.id_inp.txt_nneddt){
							// 販売期間終了日との比較
							var nnstdt;
							nnstdt = $('#'+$.id_inp.txt_nnstdt).numberbox('getValue');

							sdt = $.convDate(nnstdt, true);
							edt = $.convDate(newValue , true);
						}

						if(sdt.getTime() >= edt.getTime()){	// 期間が逆の場合
							return "E20301";
						}

						// 期間日数チェック
						var days = $.getDateDiffDay(sdt, edt);
						if(days >= 11){
							return "E20068";
						}
				 }
			}
			return null;
		},
	} });
})(jQuery);