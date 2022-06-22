/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_ReportSK003',			// （必須）レポートオプションの確認
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
		dedefaultObjNum:	11,	// 初期化オブジェクト数
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
		displayModo:"",						// 表示モード
		tencdKspg:"",						// 検索条件：店コード
		kspg:"",							// 検索条件:構成ページ
		data:[],							// 基本入力情報
		grd_tenpo_sk_data:[],				// グリッド情報:店舗一覧
		reportYobiInfo_bf:'',				// 参照情報(前画面)
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

			// 商品区分
			$.setMeisyoCombo(that, reportno, 'sel_shnkbn2', isUpdateReport);

			// 入力テキストボックス系
			var inputbox = Object.getOwnPropertyNames($.id_inp);
			for ( var sel in inputbox ) {
				if($('#'+$.id_inp[inputbox[sel]]).length > 0){
					$.setInputbox(that, reportno, $.id_inp[inputbox[sel]], isUpdateReport);
				}
			}

			// 処理日付取得
			that.getsetInputboxData(reportno, $.id.txt_shoridt, [{}], $.id.action_init);

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

			var reportYobiInfo_bf = $.getJSONValue(that.jsonHidden, "reportYobi1_bf");
			if(reportYobiInfo_bf){
				that.reportYobiInfo_bf = reportYobiInfo_bf
			}

			that.reportYobiInfo();

			// 当帳票を「参照」で開いた場合
			if(that.reportYobiInfo_bf === '0'){
				if(that.reportYobiInfo()==='0'){
					$.initReportInfo("SK003", "新改店発注商品別　新規", "新規");
					$("#disp_record_info").hide();
					$("#"+$.id.btn_del).linkbutton('disable');
					$("#"+$.id.btn_del).attr('tabindex', -1).hide();
					that.displayModo="TypeA";

				}else if(that.reportYobiInfo()==='1'){
					$.initReportInfo("SK009", "新改店発注商品別　参照", "参照");
					$.setInputBoxDisable($($.id.hiddenChangedIdx));
					$("#"+$.id.btn_upd).linkbutton('disable');
					$("#"+$.id.btn_upd).attr('tabindex', -1).hide();
					$("#"+$.id.btn_cancel).linkbutton('disable');
					$("#"+$.id.btn_cancel).attr('tabindex', -1).hide();
					$("#"+$.id.btn_del).linkbutton('disable');
					$("#"+$.id.btn_del).attr('tabindex', -1).hide();
					$.setInputBoxDisable($("#"+$.id_inp.txt_tencd));

					// 数量ボタン非表示
					$("#suryo").hide();
					$("#btn_suryo").linkbutton('disable');
					$("#btn_suryo").attr('tabindex', -1).hide();
					$.setInputBoxDisable($("#txt_suryo"));

					that.displayModo="TypeE";

				}else if(that.reportYobiInfo()==='2'){
					$.initReportInfo("SK008", "新改店発注商品別　変更", "変更");
					that.displayModo="TypeC";
					$.setInputBoxDisable($("#"+$.id_inp.txt_tencd));
				}

				// 構成ページ項目を非表示にする。
				$("#kspage").attr('tabindex', -1).hide();
				$("#"+$.id.btn_search+'_kspage').linkbutton('disable');
				$("#"+$.id.btn_search+'_kspage').attr('tabindex', -1).hide();
				$("#"+$.id.btn_search+'_kspage').linkbutton('disable');
				$.setInputBoxDisable($("#"+$.id_inp.txt_kspage));

			}else if(that.reportYobiInfo_bf === '2'){
				if(that.reportYobiInfo()==='0'){
					$.initReportInfo("SK006", "新改店発注構成別　新規", "新規");
					$("#disp_record_info").hide();
					$("#"+$.id.btn_del).linkbutton('disable');
					$("#"+$.id.btn_del).attr('tabindex', -1).hide();
					$("#"+$.id.btn_search+'_kspage').on('click',function(e){

						var kspage	 =  $.getInputboxValue($('#'+$.id_inp.txt_kspage));
						var tencd	 =  $.getInputboxValue($('#'+$.id_inp.txt_tencd));

						// 入力チェック：構成ページ
						if(kspage && kspage !== ""){
							var msgid = that.checkInputboxFunc($.id_inp.txt_kspage, kspage , '');
							if(msgid !==null){
								$.showMessage(msgid,["構成ページ","構成ページマスタ"]);
								return false;
							}
						}else{
							$.showMessage("E30012", ["構成ページ"]);
							return false;
						}

						// 入力チェック：店コード
						if(tencd && tencd !== ""){
							var msgid = that.checkInputboxFunc($.id_inp.txt_tencd, tencd , '');
							if(msgid !==null){
								$.showMessage(msgid.split(",")[0],[msgid.split(",")[1]]);
								return false;
							}
						}else{
							$.showMessage("E30012", ["店コード"]);
						}

						// 検索条件を保持
						that.tencdKspg	 = tencd
						that.kspg		 = kspage;

						// 検索実行
						$('#'+$.id.grd_tenpo_sk).datagrid('reload');
					})
					that.displayModo="TypeB";

				}else if(that.reportYobiInfo()==='1'){
					$.initReportInfo("SK011", "新改店発注構成別　参照", "参照");
					$.setInputBoxDisable($($.id.hiddenChangedIdx));
					$("#"+$.id.btn_upd).linkbutton('disable');
					$("#"+$.id.btn_upd).attr('tabindex', -1).hide();
					$("#"+$.id.btn_cancel).linkbutton('disable');
					$("#"+$.id.btn_cancel).attr('tabindex', -1).hide();
					$("#"+$.id.btn_del).linkbutton('disable');
					$("#"+$.id.btn_del).attr('tabindex', -1).hide();
					$("#"+$.id.btn_search+'_kspage').linkbutton('disable');
					$("#"+$.id.btn_search+'_kspage').attr('tabindex', -1).hide();
					$.setInputBoxDisable($("#"+$.id_inp.txt_tencd));
					$.setInputBoxDisable($("#"+$.id_inp.txt_kspage));

					// 数量ボタン非表示
					$("#suryo").hide();
					$("#btn_suryo").linkbutton('disable');
					$("#btn_suryo").attr('tabindex', -1).hide();
					$.setInputBoxDisable($("#txt_suryo"));

					that.displayModo="TypeF";

				}else if(that.reportYobiInfo()==='2'){
					$.initReportInfo("SK010", "新改店発注構成別　変更", "変更");
					$("#"+$.id.btn_search+'_kspage').linkbutton('disable');
					$("#"+$.id.btn_search+'_kspage').attr('tabindex', -1).hide();
					$.setInputBoxDisable($("#"+$.id_inp.txt_tencd));
					$.setInputBoxDisable($("#"+$.id_inp.txt_kspage));

					that.displayModo="TypeD";

				}

				// 入力No項目を非表示にする。
				$("#inputno").attr('tabindex', -1).hide();
				$.setInputBoxDisable($("#"+$.id_inp.txt_inputno));
				//$.setInputBoxDisable($("#"+$.id_inp.txt_shnkn));
				//$("#"+$.id_inp.txt_inputno).attr('tabindex', -1).hide();
			}

			// 新規登録時
			if(that.sendBtnid === $.id.btn_new){
				// 入力No項目を非表示にする。
				$("#inputno").attr('tabindex', -1).hide();
				$.setInputBoxDisable($("#"+$.id_inp.txt_inputno));
			}else{
				$.setInputBoxDisable($("#"+$.id_inp.txt_inputno));
			}

			// 新規の場合以外はキャンセルボタンを非表示にする。
			if(that.displayModo=="TypeC" || that.displayModo=="TypeD" || that.displayModo=="TypeE" || that.displayModo=="TypeF"){
				$("#"+$.id.btn_cancel).linkbutton('disable');
				$("#"+$.id.btn_cancel).attr('tabindex', -1).hide();
			}

			// 各種遷移ボタン
			$('#'+$.id.btn_cancel).on("click", $.pushChangeReport);
			$.setInputBoxDisable($("#"+$.id_inp.txt_tenkn));

			// 数量コピーボタン
			$("#btn_suryo").on('click',function(e){

				// EasyUI のフォームメソッド 'validate' 実施
				var rt = $($.id.toolbarform).form('validate');
				if(!rt){
					$.addErrState(that, $('.validatebox-invalid').eq(0), false);
					return rt;
				}

				var suryo	 = $('#'+$.id_inp.txt_suryo).numberbox('getValue');

				// 入力無しの場合
				if(!suryo || suryo == ""){
					$.showMessage("E30012", ["数量"]);
					return false;
				}

				// 一覧にデータ無しの場合
				var rows	 = $('#'+$.id.grd_tenpo_sk).datagrid('getRows');
				var ieErr	 = true;
				for (var i = 0; i < rows.length; i++){
					var val =rows[i].SHNCD;

					if(val && val !== ""){
						ieErr = false;
						break;
					}
				}
				if(ieErr){
					$.showMessage("E20028");
					return false;
				}

				that.setSuryo(that, $.id.grd_tenpo_sk, suryo)
			});

			// レイアウト調整
			$("#suryo").parent().css( 'display', 'table-cell' );
			$("#suryo").parent().css( 'vertical-align', 'bottom' );
			$("#suryo").parent().css( 'left', '580px' );


			//$('.panel layout-panel layout-panel-center').width('680px');

		},
		setSuryo: function(that, id, suryo){		// 数量コピー押下時に、入力された数量をグリッドに反映させる。
			var rows = [];
			rows = $('#'+id).datagrid('getRows');

			if(rows.length > 0){
				for (var i=0; i<rows.length; i++){
					if((rows[i]['SHNCD'] ? rows[i]['SHNCD'] : "") !== ""){
						if(rows[i]['SHNCD'] !== "99999994"){
							// データグリッドを更新
							$('#'+id).datagrid('updateRow',{
								index: Number(rows[i]['IDX']-1),
								row: {
									//name: 'new name',
									SURYO: suryo
								}
							})
						}
					}
				}
				$($.id.hiddenChangedIdx).val("1")
			}
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
			var txt_inputno		= $.getJSONObject(this.jsonString, $.id_inp.txt_inputno).value;	// 企画No

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$.appendMaskMsg();

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,		// レポート名
					INPUTNO:		txt_inputno,
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

					// 全削除ボタン制御
					var shoridt	 = $('#'+$.id.txt_shoridt).val();
					var txt_htdt = $.getInputboxValue($('#'+$.id_inp.txt_htdt));

					var sdt = $.convDate(txt_htdt, true);
					var edt = $.convDate(shoridt, true);
					if(sdt.getTime() < edt.getTime()){
						// 発注日 < 処理日付のとき、削除ボタンをDisableにする。
						$.setInputBoxDisable($("#"+$.id.btn_del));
					}

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


			// 入力編集を終了する。
			var row = $('#'+$.id.grd_tenpo_sk).datagrid("getSelected");
			var rowIndex = $('#'+$.id.grd_tenpo_sk).datagrid("getRowIndex", row);
			$('#'+$.id.grd_tenpo_sk).datagrid('endEdit',rowIndex);

			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform).form('validate');
			if(!rt){
				$.addErrState(that, $('.validatebox-invalid').eq(0), false);
				return rt;
			}

			// 存在、範囲チェック：店コード
			var param = $('#'+$.id_inp.txt_tencd).numberbox('getValue');
			if(param || param!==''){
				var msgid = that.checkInputboxFunc($.id_inp.txt_tencd, param , '');
				if(msgid !==null){
					$.showMessage(msgid.split(",")[0], [msgid.split(",")[1]], function(){$.addErrState(that, $('#'+$.id_inp.txt_tencd), true)});
					return false;
				}
			}

			// 範囲チェック：別伝区分
			var param = $('#'+$.id_inp.txt_bdenkbn).numberbox('getValue');
			if(param || param!==''){
				var msgid = that.checkInputboxFunc($.id_inp.txt_bdenkbn, param , '');
				if(msgid !==null){
					$.showMessage(msgid, undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_bdenkbn), true)});
					return false;
				}
			}

			// グリッド内データ存在チェック
			if(that.displayModo==="TypeB" || that.displayModo==="TypeD"){
				var gridData = that.getGridData($.id.grd_tenpo_sk);	// 検証用情報取得
				var targetRows = gridData[$.id.grd_tenpo_sk];
				var isNonData = true
				for (var i=0; i<targetRows.length; i++){
					if(targetRows[i].F1 && targetRows[i].F1 !== ""){
						if(isNonData){
							isNonData = false;
							break;
						}
					}
				}
				if(isNonData){
					$.showMessage("E11316");
					return false;
				}
			}

			if(that.displayModo==="TypeA" || that.displayModo==="TypeC" || that.displayModo==="TypeB" || that.displayModo==="TypeD"){
				// グリッド内エラーチェック
				// 現在の画面情報を変数に格納
				var gridData = that.getGridData($.id.grd_tenpo_sk);	// 検証用情報取得
				var targetOId = [$.id_inp.txt_shncd];
				var targetCId = ["F1","F2",];
				// 店舗部門マスタ
				var moyskbn = $.id.value_moykbn_r*1;
				var targetRows = gridData[$.id.grd_tenpo_sk];
				var shncd = [];
				for (var i=0; i<targetRows.length; i++){
					for (var j = 0; j < targetOId.length; j++){
						var value = targetRows[i][targetCId[j]]
						if(value && value !== ""){
							var msgid = that.checkInputboxFunc(targetOId[j], targetRows[i][targetCId[j]], '');
							//msgid = that.checkInputboxFunc(id, newValue, '');

							if(msgid !==null){
								$.showMessage(msgid,undefined, function(){$.addErrState(that, $('#'+that.focusGridId), true, {NO:i, ID:targetOId[j] + '_'})});
								return false;
							}
						}else{
							// 商品コード未入力時
							if(targetRows[i].F2 && targetRows[i].F2 !== ""){
								$.showMessage("EX1047",["商品コード"], function(){$.addErrState(that, $('#'+that.focusGridId), true, {NO:i, ID:targetOId[j] + '_'})});
								return false;
							}
						}

						// 重複チェック用
						var checkValue = ""
						if(value && value.trim() != "99999994"){
							// 数量0の商品コードは重複チェック対象外
							var suryo = targetRows[i][targetCId[j+1]];
							if (!$.isEmptyVal(suryo) && suryo==='0') {
								checkValue = value.trim() + '_' + i;
							} else {
								// 99999994は商品コードとしてカウントしない
								checkValue = value.trim();
							}
						}
						shncd.push(checkValue);
					}
				}

				// 重複チェック
				var shncds_index = []
				shncd.filter(function (x, i, self) {
					if(x && x != ""){
			            if(Number(self.indexOf(x)) !== i || Number(self.lastIndexOf(x)) !== i){
			            	shncds_index.push(i)
			            };
					}
		        });

				// グリッド内の塗り潰し状態をクリアする。
				$.removeErrStateGrid($.id.grd_tenpo_sk);

				if(shncds_index.length > 0){
					var targetColIndex = 0		// 商品コードの項目順番
					// グリッド内の重複箇所を塗り潰し
					$.addErrStateGrid($.id.grd_tenpo_sk, shncds_index, [targetColIndex]);
					$.showMessage('E20572', ['行データ'], function(){});
					return false;
				}
			}

			if(that.displayModo==="TypeA" || that.displayModo==="TypeB" || that.displayModo==="TypeC" || that.displayModo==="TypeD"){
				// 発注日
				var param = $.getInputboxValue($('#'+$.id_inp.txt_htdt));
				if(param || param!==''){
					var msgid = that.checkInputboxFunc($.id_inp.txt_htdt, param , '');
					if(msgid !==null){
						$.showMessage(msgid, undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_htdt), true)});
						return false;
					}
				}

				// 納入日
				var param = $.getInputboxValue($('#'+$.id_inp.txt_nndt));
				if(param || param!==''){
					var msgid = that.checkInputboxFunc($.id_inp.txt_nndt, param , '');
					if(msgid !==null){
						$.showMessage(msgid, undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_nndt), true)});
						return false;
					}
				}
			}

			if(that.displayModo==="TypeB"){
				// 店コード
				if(that.tencdKspg !==''){
					if(that.tencdKspg !== $.getInputboxValue($('#'+$.id_inp.txt_tencd))){
						$.showMessage('E20459', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_tencd), true)});
						return false;

					}
				}

				// 構成ページ
				if(that.kspg !==''){
					if(that.kspg !== $.getInputboxValue($('#'+$.id_inp.txt_kspage))){
						$.showMessage('E20460', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_kspage), true)});
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

			// 基本入力情報取得
			var targetDatas = that.getGridData("data")["data"];

			// 店舗一覧のデータを取得
			var targetDatasTen = that.getMergeGridDate($.id.grd_tenpo_sk);

			// 店舗一覧の削除データを取得
			//var targetDatasTenDel = that.getGridData($.id.grd_tenpo_sk, 'del')[$.id.grd_tenpo_sk];

			var targetDatasTenDel = that.getMergeGridDate($.id.grd_tenpo_sk, 'del');

			// 削除チェック数と一覧表のデータ数を比較
			var allCheck = '0';
			if(that.displayModo==="TypeC" || that.displayModo==="TypeD"){
				var targetdate =[];
				var targetRows = $('#'+$.id.grd_tenpo_sk).datagrid('getRows');
				for (var i=0; i<targetRows.length; i++){
					if(targetRows[i]["SHNCD"] ? targetRows[i]["SHNCD"] : "" !== "" ){
						targetdate.push(targetRows[i]["SHNCD"]);
					}
				}
				if(targetdate.length === targetDatasTenDel.length){
					allCheck = '1';
				}
			}

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
					DISOMODE:		that.displayModo,
//					IDX:			$($.id.hiddenChangedIdx).val(),	// 更新対象Index
					ALLCHECK:		allCheck,						// チェック数確認フラグ
					DATA:			JSON.stringify(targetDatas),	// 更新対象情報
					DATA_SHN:		JSON.stringify(targetDatasTen),	// 更新対象情報
					DATA_SHN_DEL:	JSON.stringify(targetDatasTenDel),// 更新対象情報

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

			// 入力チェック用の配列をクリア
			that.jsonTemp = [];

			return rt;
		},
		delSuccess: function(id){
			var that = this;

			var targetDatas = that.getGridData("data")["data"];


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
			// 入力No
			this.jsonTemp.push({
				id:		$.id_inp.txt_inputno,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_inputno),
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
			var columns = that.getGridColumns(that, id);
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

			if (pageSize==="") pageSize=$.fn.pagination.defaults.pageSize;


			var funcClickRow = $.fn.datagrid.defaults.onClickRow;
			var funcBeginEdit= $.fn.datagrid.defaults.onBeginEdit;
			var funcEndEdit= $.fn.datagrid.defaults.onEndEdit;
			var funcAfterEdit= $.fn.datagrid.defaults.onAfterEdit;

			if(that.reportYobiInfo()!=='1'){
				that.editRowIndex[id] = -1;
				funcClickRow = function(index,field){$.clickEditableDatagridCell(that,id, index)};
				funcBeginEdit = function(index,row){
					$.beginEditDatagridRow(that,id, index, row)};
				funcEndEdit = function(index,row,changes){
					$.endEditDatagridRow(that, id, index, row);
				};
				funcAfterEdit = function(index,row,changes){
					// チェックボックスの再追加（EndEdit時に削除されるため）
					$.afterEditAddCheckbox($('#'+id).datagrid('getPanel').find("[datagrid-row-index='"+index+"']"));

					// 重複行塗り潰し処理
					var rows	 = $('#'+$.id.grd_tenpo_sk).datagrid('getRows');
					var shnData = []

					for (var i = 0; i < rows.length; i++){
						var row	  = rows[i]
						var shncd = row['SHNCD']
						var suryo = row['SURYO']
						var value = "";

						if(shncd && shncd.trim() != "99999994"){
							if (!$.isEmptyVal(suryo) && suryo!=='0') {
								value = shncd.trim()
							}
						}
						shnData.push(value);
					}

					var shnData_ = []
					shnData.filter(function (x, i, self) {
						if(x && x != ""){
				            if(Number(self.indexOf(x)) !== i || Number(self.lastIndexOf(x)) !== i){
				            	shnData_.push(i)
				            };
						}
			        });

					// グリッド内の塗り潰し状態をクリアする。
					$.removeErrStateGrid($.id.grd_tenpo_sk);
					if(shnData_.length > 0){
						var targetColIndex = 0		// 商品コードの項目順番
						// グリッド内の重複箇所を塗り潰し
						$.addErrStateGrid($.id.grd_tenpo_sk, shnData_, [targetColIndex]);
					}
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
				fit:true,
				singleSelect:true,
				checkOnSelect:false,
				selectOnCheck:false,
				frozenColumns:[[]],
				columns:columns,
				url:$.reg.easy,
				onBeforeLoad:function(param){
					//index = -1;
					var values = {};
					var inputno	 = $('#'+$.id_inp.txt_inputno).numberbox('getValue');		// 企画No
					var tencd		 = '';														// 店コード
					var shncd		 = '';														// 商品コード
					var kspage		 = '';														// 構成ページ

					if(that.reportYobiInfo_bf === '2' && that.reportYobiInfo() === '0'){
						// 構成別かつ新規の時
						if(that.queried){
							tencd	 = $.getInputboxValue($('#'+$.id_inp.txt_tencd));
							kspage	 = $.getInputboxValue($('#'+$.id_inp.txt_kspage));
						}
					}

					values["callpage"]	 = $($.id.hidden_reportno).val()				// 呼出元レポート名
					values["INPUTNO"]	 = inputno										// 企画No
					values["TENCD"]		 = tencd										// 店コード
					values["KSPAGE"]	 = kspage										// 構成ページ
					values["DISPMODO"]	 = that.displayModo;							// 表示タイプ

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
					if(index===-1){
						index=1;
						var gridData = that.getGridData(id);
						that.setGridData(gridData, id);
					}
				},
				onSelect:function(index, row){
					var rows = $('#'+id).datagrid('getRows');
					var col = $('#'+id).datagrid('getColumnOption', 'SURYO');
					if(row.SHNCD != "99999994"){
						col.editor = {
								type:'numberbox',
								options:{cls:'labelInput',editable:true,disabled:false,readonly:false},
								//formatter:formatterLPad
							}
					} else if (that.displayModo=="TypeB" || that.displayModo=="TypeC") {
						col.editor = false
					}
				},
				onBeforeEdit:function(index,row){
					if (that.displayModo=="TypeB" || that.displayModo=="TypeD") {

						//var allRows	 = $('#'+id).datagrid('getData').firstRows;
						var allRows		 = $('#'+id).datagrid('getRows');
						var rows		 = $('#'+id).datagrid('getRows');				// 現在表示されているデータ
						var rowsLength	 = rows.length
						var isEdit		 = false

						var RefleshRangeMin = $('#'+id).datagrid("getRowIndex", rows[0]);
						var RefleshRangeMax = $('#'+id).datagrid("getRowIndex", rows[rowsLength-1]);

						if(!row){
							row = allRows[index]
						}

						if(row.SHNCD =='99999994'){
							var adds = that.editRowIndex[that.focusGridId] > index ? -1:1;
							var nextindex = 0;
							//var nextindex = index + adds;

							// 次のEdit可能な行を探す。
							for(var i = index; i < allRows.length; i++ ){
								var nextRow = allRows[i]
								if(nextRow.SHNCD !='99999994'){
									nextindex = $('#'+id).datagrid("getRowIndex", nextRow);
									isEdit = true;
									break;
								}
							}
							if(index == (allRows.length-1)){
								// 最終行が選択された場合
								nextindex = index +1

							}else if(nextindex == 0){
								// Edit可能な行が存在しなかった場合
								nextindex = (allRows.length-1);

							}

							// 次の行に移るか、次の項目に移るかする
							if(nextindex >= 0 && nextindex < $('#'+id).datagrid('getRows').length){
								//$('#'+id).datagrid('endEdit', index);

								// 次の行が画面外の場合、スクロールを行う
//								if(nextindex < RefleshRangeMin || RefleshRangeMax < nextindex){
//									$('#'+id).datagrid('scrollTo', {
//										index: nextindex,
//									});
//								}
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
				},

				onClickRow: funcClickRow,
				onBeginEdit:funcBeginEdit,
				onEndEdit: funcEndEdit,
				onAfterEdit: funcAfterEdit,
			});
		},
		getGridColumns:function(that, id){
			var columns = [];
			var columnBottom=[];

			var cstyler =function(value,row,index){return 'color: red;font-weight: bold;';};
			var cformatter =function(value,row,index){return value && value===$.id.value_on?$.id.text_on:$.id.text_off;};
			var pomptFormatter =function(value){return $.getFormatPrompt(value, '####-####');};

			var suryoFormatter =function(value,row){
				if(row.SHNCD && row.SHNCD == '99999994'){
					// 商品コード99999994の時はデフォルト数量値が登録されていても表示しない。
					return "";
				}
				return  $.getFormat(value, '##,##0');
			};

			if(that.displayModo==="TypeB" || that.displayModo==="TypeD" || that.displayModo==="TypeF"){
				columnBottom.push({field:'SHNCD',		title:'商品コード',				width:  80,halign:'center',align:'left',formatter:pomptFormatter,editor:{type:'numberbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}});
			}else{
				columnBottom.push({field:'SHNCD',		title:'商品コード',				width:  80,halign:'center',align:'left',formatter:pomptFormatter,editor:{type:'numberbox'}});
			}
			columnBottom.push({field:'SURYO',		title:'数量',					width:  60,halign:'center',align:'right',formatter:suryoFormatter,editor:{type:'numberbox'}});
			columnBottom.push({field:'SHNKN',		title:'商品名',					width: 400,halign:'center',align:'left',editor:{type:'textbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}});
			columnBottom.push({field:'KANRINO',		title:'管理番号',				width:  60,halign:'center',align:'right',hidden:true});


			columns.push(columnBottom);
			return columns;

		},
		getGridData: function (target, del){
			var that = this;

			var data = {};
			var targetRows= [];

			// 基本情報
			if(target===undefined || target==="data"){

				var txt_inputno	 = $('#'+$.id_inp.txt_inputno).numberbox('getValue');		// 企画No

				var targetDatas = [{}];
				$('#'+that.focusRootId).find('[col^=F]').each(function(){
					var col = $(this).attr('col');
					targetDatas[0][col] = $.getInputboxValue($(this));
				});

				for (var i=0; i<targetDatas.length; i++){
					var rowDate = {
							F1	 : txt_inputno,					// 企画No
							F2	 : targetDatas[i]["F2"],		// 商品コード
							F3	 : targetDatas[i]["F4"],		// 発注日
							F4	 : targetDatas[i]["F5"],		// 納入日
							F5	 : targetDatas[i]["F6"],		// 商品区分
							F6	 : targetDatas[i]["F12"],		// 構成ページ
							F7	 : targetDatas[i]["F7"],		// 別伝区分
							F8	 : targetDatas[i]["F11"],		// 更新日付
					};
					if(rowDate){
						targetRows.push(rowDate);
					}
				}
				data["data"] = targetRows;
			}

			// 商品一覧
			if(target===undefined || target===$.id.grd_tenpo_sk){

				var txt_inputno	 = $('#'+$.id_inp.txt_inputno).numberbox('getValue');		// 企画No

				if(del && del === 'del'){
					var rows	 = $('#'+target).datagrid('getRows');
					for (var i=0; i<rows.length; i++){
						if(rows[i]['SEL']==='1' && (rows[i]['SHNCD'] ? rows[i]['SHNCD'] : "") !== '' ){
							if(that.displayModo!=="TypeB"){
								var rowDate = {
										F1	 : txt_inputno,									// 商品コード
										F2	 : rows[i]["KANRINO"],							// 管理番号

								};
								targetRows.push(rowDate);
							}
						}
					}
					data[$.id.grd_tenpo_sk] = targetRows;

				}else{
					var rows	 = $('#'+target).datagrid('getRows');					// 商品一覧

					for (var i=0; i<rows.length; i++){
						var rowDate = {
								F1	 : rows[i]["SHNCD"],				// 商品コード
								F2	 : rows[i]["SURYO"],				// 数量
								F3	 : rows[i]["KANRINO"],				// 管理番号
								F4	 : rows[i]["SEL"],					// 削除

						};
						targetRows.push(rowDate);
					}
					data[$.id.grd_tenpo_sk] = targetRows;
				}
			}
			return data;
		},
		getMergeGridDate: function(target, del){
			// 保持したデータと入力データ比較を比較する。
			var that = this;

			var newrows = that.getGridData(target)[target] ? that.getGridData(target)[target] : [];		// 変更データ
			var oldrows = [];
			var targetRows= [];


			if(target===undefined || target===$.id.grd_tenpo_sk){

				var txt_inputno	 = $('#'+$.id_inp.txt_inputno).numberbox('getValue');		// 企画No

				// 店舗一覧
				var shoridt	 = $('#'+$.id.txt_shoridt).val();						// 処理日付
				oldrows = that.grd_tenpo_sk_data

				if(del && del === 'del'){
					for (var i=0; i<newrows.length; i++){
						if(newrows[i]['F4']==='1'
						){
							if((oldrows[i]['F1'] ? oldrows[i]['F1'] : "") === (newrows[i]['F1'] ? newrows[i]['F1'] : "")){
								if((oldrows[i]['F1'] ? oldrows[i]['F1'] : "") !== ""){
									var rowDate = {
											F1	 : txt_inputno,
											F2	 : newrows[i]["F3"],
											//F2	 : 1,
											F3	 : newrows[i]["F1"],
											F4	 : newrows[i]["F2"],
									};
									if(rowDate){
										targetRows.push(rowDate);
									}
								}
							}
						}
					}

				}else{
					for (var i=0; i<newrows.length; i++){
						if((newrows[i]['F1'] ? newrows[i]['F1'] : "") !== ''){

							// 商品コード"99999994"は更新対象として扱わない
							if(newrows[i]["F1"] != "99999994"){

								if(newrows[i].F2 && Number(newrows[i].F2) >= 0){
									var rowDate = {
											F1	 : txt_inputno,
											F2	 : newrows[i]["F3"],
											F3	 : newrows[i]["F1"],
											F4	 : newrows[i]["F2"],
									};
									if(rowDate){
										targetRows.push(rowDate);
									}
								}
							}
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
			if(target===undefined || target===$.id.grd_tenpo_sk){
				that.grd_tenpo_sk_data =  data[$.id.grd_tenpo_sk];
			}
		},
		getsetInputboxData: function(reportno, id, param, action){
			var that = this
			if(action===undefined) action = $.id.action_change;
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

					// 納品日一覧
					that.setEditableGrid(that, reportno, $.id.grd_tenpo_sk);
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
			var sendMode = 1;

			// タブ要素(a)取得
			var elems = $('#tabContent', window.parent.document).map(
				function(i,e) {
					return e;
				}).get();
			var href = elems[0].value.split(',');

			// JSON Object Clone ()
			var sendJSON = [];
			$.setJSONObject(sendJSON, 'sendBtnid', btnId, $('#'+btnId).attr("title"));						// 実行ボタン情報保持
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

				$.setJSONObject(sendJSON, $.id_inp.txt_kkkcd, row.F1, row.F1);				// 企画No
				break;
			case $.id.btn_cancel:
			case $.id.btn_back:
			case "btn_return":
				// 転送先情報
				if(that.reportYobiInfo_bf === '0'){
					index = 1;

				}else if(that.reportYobiInfo_bf === '2'){
					index = 2;

				}
				childurl = href[index];
				sendMode = 2;

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

				// 店コード
				if(id===$.id_inp.txt_tencd){
					msgid = that.checkInputboxFunc(id, newValue, '');
					if(msgid !==null){
						$.showMessage(msgid.split(",")[0],[msgid.split(",")[1]]), function(){$.addErrState(that, $('#'+$.id_inp.txt_tencd), true)};
						return false;
					}
				}

				// 商品コード
				if(id===$.id_inp.txt_shncd){
					msgid = that.checkInputboxFunc(id, newValue, '');
					if(msgid !==null){
						$.showMessage(msgid,undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_shncd+'_'), true)});
						return false;
					}

					// 重複行塗り潰し処理
					var rows	 = $('#'+$.id.grd_tenpo_sk).datagrid('getRows');
					var shnData = []

					var row = $('#'+$.id.grd_tenpo_sk).datagrid("getSelected");
					var rowIndex = $('#'+$.id.grd_tenpo_sk).datagrid("getRowIndex", row);

					for (var i = 0; i < rows.length; i++){
						var row	  = rows[i]
						var shncd = row['SHNCD']
						var suryo = row['SURYO']
						var value = "";

						if(i == rowIndex){
							shncd = newValue
						}

						if(shncd && shncd.trim() != "99999994"){
							if (!$.isEmptyVal(suryo) && suryo!=='0') {
								value = shncd.trim()
							}
						}
						shnData.push(value);
					}

					var shnData_ = []
					shnData.filter(function (x, i, self) {
						if(x && x != ""){
				            if(Number(self.indexOf(x)) !== i || Number(self.lastIndexOf(x)) !== i){
				            	shnData_.push(i)
				            };
						}
			        });

					// グリッド内の塗り潰し状態をクリアする。
					$.removeErrStateGrid($.id.grd_tenpo_sk);
					if(shnData_.length > 0){
						var targetColIndex = 0		// 商品コードの項目順番
						// グリッド内の重複箇所を塗り潰し
						$.addErrStateGrid($.id.grd_tenpo_sk, shnData_, [targetColIndex]);
					}
				}
			}else{
				// 検索前の初期値設定時
				if(id===$.id_inp.txt_htdt){
					if(newValue !== '' && newValue){
						var sdt, edt;
						var shoridt = $('#'+$.id.txt_shoridt).val();

						sdt = $.convDate(newValue, true);
						edt = $.convDate(shoridt, true);
						if(sdt.getTime() <= edt.getTime()){
							$.setInputBoxDisableVariable($("#"+$.id.btn_upd),true);
						} else {
							$.setInputBoxEnableVariable($("#"+$.id.btn_upd));
						}
					}
				}
			}
		},
		// IDとvalueでチェック処理を実施
		checkInputboxFunc:function(id, newValue, kbn, record, isNew){
			var that = this;
			var sdt, edt;

			// 店コード
			if(id===$.id_inp.txt_tencd){
				if(newValue !== '' && newValue){

					// 範囲チェック
					if(Number(newValue) < 1 || Number(newValue) > 400){
						return "E20110,店舗";
					}

					// 存在チェック
					var param = {};
					param["KEY"] =  "MST_CNT";
					param["value"] = newValue;
					var chk_cnt = $.getInputboxData(that.name, $.id.action_check, $.id_inp.txt_tencd, [param]);
					if(chk_cnt =="0"){
						return "E20291,";
					}
				}
			}

			// 構成ページ
			if(id===$.id_inp.txt_kspage){
				if(newValue !== '' && newValue){
					var tencd = $.getInputboxValue($('#'+$.id_inp.txt_tencd));		// 店コード

					if(tencd !== '' && tencd){
						// 存在チェック
						var param = {};
						param["KEY"] =  "MST_CNT";
						param["value"] = newValue + "," +tencd;
						var chk_cnt = $.getInputboxData(that.name, $.id.action_check, $.id_inp.txt_kspage, [param]);
						if(chk_cnt =="0"){
							return "EX1100";
						}
					}
				}
			}

			// 別伝区分
			if(id===$.id_inp.txt_bdenkbn){
				if(newValue !== '' && newValue){
					// 範囲チェック
					if(Number(newValue) < 0 || Number(newValue) > 8){
						return "E20356";
					}

				}
			}


			// 商品コード
			if(id===$.id_inp.txt_shncd){
				if(newValue !== '' && newValue){

					if((that.displayModo=="TypeB" || that.displayModo=="TypeC") && newValue === "99999994"){
						$.setInputboxValue($('#'+$.id_inp.txt_shnkn+'_'), "棚段変わります");

					}else{
						if(newValue != "99999994"){
							// 商品マスタ
							var param = {};
							param["KEY"] =  "MST_CNT";
							param["value"] = newValue;
							var chk_cnt = $.getInputboxData(that.name, $.id.action_check, $.id_inp.txt_shncd, [param]);
							if(chk_cnt==="" || chk_cnt==="0"){
								return "E11046";
							}
						}
					}
				}
			}

			// 数量
			if(id===$.id_inp.txt_suryo){
				if(newValue !== '' && newValue){
					var shncd = $.getInputboxValue($('#'+$.id_inp.txt_shncd+'_'));
					if(shncd === "99999994"){
						$.setInputboxValue($('#'+$.id_inp.txt_suryo+'_'), "");
					}
				}
			}

			// 発注日
			if(id===$.id_inp.txt_htdt){
				if(newValue !== '' && newValue){
					var sdt, edt;
					var shoridt = $('#'+$.id.txt_shoridt).val();

					sdt = $.convDate(newValue, true);
					edt = $.convDate(shoridt, true);
					if(sdt.getTime() <= edt.getTime()){
						return "E20127";
					}
				}
			}

			// 納入日
			if(id===$.id_inp.txt_nndt){
				if(newValue !== '' && newValue){
					var sdt, edt;
					var param = $.getInputboxValue($('#'+$.id_inp.txt_htdt));

					sdt = $.convDate(newValue, true);
					edt = $.convDate(param, true);
					if(sdt.getTime() <= edt.getTime()){
						return "E20264";
					}
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