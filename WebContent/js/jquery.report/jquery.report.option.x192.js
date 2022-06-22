/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_Reportx192',			// （必須）レポートオプションの確認
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
		dedefaultObjNum:	10,	// 初期化オブジェクト数
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
		searchCondition:[],					// 検索条件
		openMsg:true,						// クリア実行メッセージ表示フラグ
		gridData:[],						// 検索結果
		gridTitle:[],						// 検索結果
		data:[],							// 基本入力情報
		grd_tenpo_data:[],					// グリッド情報:店舗一覧
		gpkbn:"",							// グループ区分
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

			// ラジオボタン
			//that.setRadioAreakbn(that, $.id.rad_areakbn);
			$.setRadioInit2(that.jsonHidden, $.id.rad_areakbn, that);
			// チェックボックス設定：
			$.setCheckboxInit2(that.jsonHidden, 'chk_torihiki', that);
			// グループ区分
			$.setMeisyoCombo(that, reportno, $.id_mei.kbn140, isUpdateReport);

			var count = 2;
			// 入力テキストボックス系
			var inputbox = Object.getOwnPropertyNames($.id_inp);
			for ( var sel in inputbox ) {
				if($('#'+$.id_inp[inputbox[sel]]).length > 0){
					$.setInputbox(that, reportno, $.id_inp[inputbox[sel]], isUpdateReport);
					count++;
				}
			}

			// 店グループコード：2桁表示用
			$.setInputbox(that, reportno, $.id_inp.txt_tengpcd+'_2', isUpdateReport);

			// 部門
			$.setMeisyoCombo(that, reportno, $.id.SelBumon, isUpdateReport);

			// 店舗一覧
			that.setEditableGrid(that, reportno, $.id.grd_tenpo);

			// 初期化終了
			this.initializes =! this.initializes;

			// 店グループ項目の表示設定
			if(that.sendBtnid === $.id.btn_sel_change || that.sendBtnid===$.id.btn_sel_copy || that.sendBtnid===$.id.btn_sel_refer){
				var rad_areakbn		= $("input[name="+$.id.rad_areakbn+"]:checked").val();			// グループ区分

				that.setDispOption(rad_areakbn, $.id.grd_tenpo);
			}else{
				that.setDispOption('1', $.id.grd_tenpo);
			}

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

			// 保持検索条件の初期化
			that.searchCondition[0] = {
					'rad_areakbn'	:''
			}

			// 各種遷移ボタン
			$('#'+$.id.btn_new).on("click", $.pushChangeReport);
			$('#'+$.id.btn_copy).on("click", $.pushChangeReport);
			$('#'+$.id.btn_cancel).on("click", $.pushChangeReport);

			if(that.reportYobiInfo()==='1'){
				$.setInputBoxDisable($($.id.hiddenChangedIdx));
				$('#'+$.id.btn_upd).linkbutton('disable');
				$('#'+$.id.btn_upd).attr('disabled', 'disabled').hide();
				$('#'+$.id.btn_cancel).linkbutton('disable');
				$('#'+$.id.btn_cancel).attr('disabled', 'disabled').hide();
				$('#'+$.id.btn_del).linkbutton('disable');
				$('#'+$.id.btn_del).attr('disabled', 'disabled').hide();
				$.setInputBoxDisable($("#"+$.id_inp.txt_tengpcd));
				$.setInputBoxDisable($("#"+$.id_mei.kbn140));
				$.setInputBoxDisable($("#"+$.id.SelBumon));
				$.setInputBoxDisable($("input[name="+$.id.rad_areakbn+"]"));

			}
			// 店舗一覧を非表示にする。
			if(that.sendBtnid === $.id.btn_sel_change || that.sendBtnid===$.id.btn_sel_copy){
				var sel_gpkbn		= $.getJSONObject(this.jsonHidden, $.id_mei.kbn140).value;		// グループ区分
				if(sel_gpkbn === "1"){
					$("#gf").hide();
					$("#"+$.id.grd_tenpo).attr('tabindex', -1).hide();
				}
			}

			// アイテム数項目(常にDisable状態)
			$.setInputBoxDisable($("#txt_countItem"));

			if(that.sendBtnid===$.id.btn_new || that.sendBtnid===$.id.btn_sel_copy){
				$.initReportInfo("IT022", "商品店グループ　新規", "新規");
				$("#"+$.id.btn_del).linkbutton('disable');
				$("#"+$.id.btn_del).attr('tabindex', -1).hide();
				$("#disp_record_info").hide();
				$("#Items").hide();

				if( that.sendBtnid===$.id.btn_sel_copy){
					$.setInputBoxDisable($("#"+$.id_mei.kbn140));
					$.setInputBoxDisable($("#"+$.id.SelBumon));
					$.setInputBoxDisable($("input[name="+$.id.rad_areakbn+"]"));

					// 新規コピー登録時は入力前から登録可能する。
					$($.id.hiddenChangedIdx).val("1");
				}
			}else if(that.sendBtnid===$.id.btn_sel_change){
				$.initReportInfo("IT023", "商品店グループ　変更", "変更");
				$.setInputBoxDisable($("#"+$.id_inp.txt_tengpcd));
				$.setInputBoxDisable($("#"+$.id_inp.txt_tengpcd + '_2'));
				$.setInputBoxDisable($("#"+$.id_mei.kbn140));
				$.setInputBoxDisable($("#"+$.id.SelBumon));
				$.setInputBoxDisable($("input[name="+$.id.rad_areakbn+"]"));

			}else if(that.sendBtnid===$.id.btn_sel_refer){
				$.initReportInfo("IT023", "商品店グループ　参照", "参照");
				$.setInputBoxDisable($("#"+$.id_inp.txt_tengpcd));
				$.setInputBoxDisable($("#"+$.id_inp.txt_tengpcd + '_2'));
				// 新規登録時は入力前から登録可能する。
				$($.id.hiddenChangedIdx).val("1");

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
			var txt_tengpcd		= $.getJSONObject(this.jsonString, $.id_inp.txt_tengpcd).value;	// 店グループ
			var sel_gpkbn		= $.getJSONObject(this.jsonString, $.id_mei.kbn140).value;		// グループ区分
			var SelBumon		= $.getJSONObject(this.jsonString, $.id.SelBumon).value;		// 部門
			var rad_areakbn		= $("input[name="+$.id.rad_areakbn+"]:checked").val();			// グループ区分


			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$.appendMaskMsg();

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,		// レポート名
					TENGPCD:		txt_tengpcd,
					BUMON:			SelBumon,
					GPKBN:			sel_gpkbn,
					AREAKBN:		rad_areakbn,
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

					// 初回検索条件保持
					var areakbn	 = $("input[name="+$.id.rad_areakbn+"]:checked").val();
					that.searchCondition[0] = {
							'rad_areakbn'	:areakbn
					}

					// 名称を検索するため、フォーカスを動かす。
					/*var newval = $('#'+$.id_mei.kbn140).combobox('getValue');
					$('#'+$.id_mei.kbn140).combobox('reset').combobox('setValue', newval);*/

					// グリッド再描画
					$('#'+$.id.grd_tenpo).datagrid('reload');

					// 状態保存
					$.saveState2(reportno, that.getJSONString());

					// グループ区分の初回値を保持
					var sel_gpkbn		= $.getInputboxValue($('#'+$.id_mei.kbn140));				// グループ区分
					that.gpkbn = sel_gpkbn;

					// 基本入力初期値保持
					/*var Data = that.getGridData('data');
					that.setGridData(Data, 'data');*/

					// ログ出力
					$.log(that.timeData, 'loaded:');
				}
			);
		},
		updValidation: function (){	// （必須）批准
			var that = this;

			// 入力編集を終了する。
			var row = $('#'+$.id.grd_tenpo).datagrid("getSelected");
			var rowIndex = $('#'+$.id.grd_tenpo).datagrid("getRowIndex", row);
			$('#'+$.id.grd_tenpo).datagrid('endEdit',rowIndex);

			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform).form('validate');
			if(!rt){
				$.addErrState(that, $('.validatebox-invalid').eq(0), false);
				return rt;
			}

			// 必須入力項目チェック
			var gpkbn			 = $.getInputboxValue($('#'+$.id_mei.kbn140));				// グループ区分
			var selBumon		 = $.getInputboxValue($('#'+$.id.SelBumon));				// 部門
			var areakbn			 = $("input[name="+$.id.rad_areakbn+"]:checked").val();		// エリア区分

			if(!gpkbn || gpkbn == '-1'){
				$.showMessage('EX1103', ['グループ区分']);
				return false;
			}

			if(!selBumon || selBumon == '-1'){
				$.showMessage('EX1103', ['部門']);
				return false;
			}

			if(!areakbn || areakbn == ''){
				$.showMessage('EX1103', ['エリア区分']);
				return false;
			}

			// 店舗一覧
			var targetdate = [];
			var targetRows = $('#'+$.id.grd_tenpo).datagrid('getRows');
			for (var i=0; i<targetRows.length; i++){
				if(targetRows[i]["TENCD"]){
					if(targetRows[i]["TENCD"] !== ''){
						// 存在チェック：店舗コード
						var msgid = that.checkInputboxFunc($.id_inp.txt_tencd, targetRows[i]["TENCD"] , '');
						if(msgid !==null){
							$.showMessage(msgid);
							return false;

						}else{
							targetdate.push(targetRows[i]["TENCD"]);
						}
					}
				}
			}

			// 店グループ
			var rad_areakbn		= $("input[name="+$.id.rad_areakbn+"]:checked").val();			// エリア区分

			// 入力範囲チェック
			if(rad_areakbn == '0'){
				var tengpcd = $.getInputboxValue($('#'+$.id_inp.txt_tengpcd+'_2'));					// 店グループ(2桁)

				// 店グル―プが1～9の範囲以外の数値だった場合
				if(tengpcd && tengpcd != ""){
					if(Number(tengpcd) < 1 || 9 < Number(tengpcd)){
						$.showMessage('E11233', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_tengpcd+'_2'), true)});
						return false;
					}
				}

				// 存在チェック：店グループコード
				var msgid = that.checkInputboxFunc($.id_inp.txt_tengpcd, tengpcd , '');
				if(msgid !==null){
					$.showMessage(msgid, undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_tengpcd+'_2'), true)});
					return false;
				}
			}else if(rad_areakbn == '1'){
				var tengpcd = $.getInputboxValue($('#'+$.id_inp.txt_tengpcd));						// 店グループ(4桁)
				if(Number(tengpcd) < 10){
					$.showMessage('E30012', ['店グループは10番以上'], function(){$.addErrState(that, $('#'+$.id_inp.txt_tengpcd), true)});
					return false;
				}
			}

			// 重複チェック：店舗一覧
			var targetdateF = targetdate.filter(function (element, index, self) { return self.indexOf(element)*1 === index; });
			if(targetdate.length !== targetdateF.length){
				$.showMessage('E11141');
				return false;
			}

			// グリッド入力チェック
			var sel_gpkbn		= $.getInputboxValue($('#'+$.id_mei.kbn140));
			var areakbn			= $("input[name="+$.id.rad_areakbn+"]:checked").val();			// エリア区分
			var targetDatas_tenpo = that.getMergeGridDate($.id.grd_tenpo);

			var rows		 = $('#'+$.id.grd_tenpo).datagrid('getRows');
			var cuntRows	 = 0;
			for (var i=0; i<rows.length; i++){
				if(rows[i].TENCD && rows[i].TENCD !== ''){
					cuntRows += 1;
				}
			}

			if(areakbn ==='0' || sel_gpkbn === '1'){
				if(cuntRows > 0){
					$.showMessage('EX1043');
					return false;
				}
			}else{
				if(cuntRows == 0){
					$.showMessage('EX1044');
					return false;
				}
			}
			if(that.sendBtnid===$.id.btn_sel_copy){
				if(sel_gpkbn === '1' && areakbn ==='1'){
					$.showMessage('EX1044');
					return false;
				}
			}
			return true;
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
			var targetDatas = that.getGridData("data")["data"];		// 変更データ
			//var targetDatas = that.getMergeGridDate("data");

			// 店舗一覧の登録データを取得
			var targetDatas_tenpo = [];
			targetDatas_tenpo = that.getMergeGridDate($.id.grd_tenpo);

			// 店舗一覧の削除用データを取得
			var targetDatas_tenpo_del = [];
			targetDatas_tenpo_del = that.getMergeGridDate($.id.grd_tenpo, true);

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
//					DATA:			JSON.stringify(targetRows),		// 更新対象情報
					DATA:			JSON.stringify(targetDatas),	// 更新対象情報
					DATA_TENPO:		JSON.stringify(targetDatas_tenpo),
					//DATA_TENPO_DEL:	JSON.stringify(targetDatas_tenpo_del),
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
			// 店グループ
			this.jsonTemp.push({
				id:		$.id_inp.txt_tengpcd,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_tengpcd),
				text:	''
			});
			// グループ区分
			this.jsonTemp.push({
				id:		$.id_mei.kbn140,
				value:	$.getJSONValue(this.jsonHidden, $.id_mei.kbn140),
				text:	''
			});
			// 部門
			this.jsonTemp.push({
				id:		$.id.SelBumon,
				value:	$.getJSONValue(this.jsonHidden, $.id.SelBumon),
				text:	''
			});
		},

		setData: function(rows, opts){		// データ表示
			var that = this;

			if(rows.length > 0){
				$('#'+that.focusRootId).find('[col^=F]').each(function(){
					var col = $(this).attr('col');
					if(rows[0][col]){
						if(col == 'F11'){
							$.setInputboxValue($(this), $.getFormat(rows[0][col], '#,##0'));
						}else{
							$.setInputboxValue($(this), rows[0][col]);
						}
					}
				});
			}

			// colが同一の項目は使用項目のみデータを入れる。
			if(that.sendBtnid!==$.id.btn_new){
				var areakbn = $("input[name="+$.id.rad_areakbn+"]:checked").val();			// グループ区分
				if(areakbn == '0'){
					$.setInputboxValue($("#"+$.id_inp.txt_tengpcd), '');
				}else{
					$.setInputboxValue($("#"+$.id_inp.txt_tengpcd+'_2'), '');
				}
			}
		},
		setDispOption: function(kbn, gridId){
			var that = this;

			var target	 = $("#"+$.id_inp.txt_tengpcd);					// 店グループ(4桁)
			var target2	 = $("#"+$.id_inp.txt_tengpcd+'_2');			// 店グループ(2桁)

			if(kbn == "0"){
				// 非表示：4桁
				$("#tengp1").hide();
				if(that.sendBtnid!==$.id.btn_new){
					$.setInputBoxDisable(target);
				}
				target.textbox('textbox').attr('tabindex', -1);
				target.textbox('textbox').attr('readonly', 'readonly');
				target.textbox('textbox').attr('disabled', 'disabled');

				// 表示：2桁
				$("#tengp2").show();
				if(that.sendBtnid!==$.id.btn_sel_change && that.sendBtnid!==$.id.btn_sel_refer){
					target2.textbox('textbox').attr('readonly',false);
					target2.textbox('textbox').attr('tabIndex', 3);
					target2.textbox('textbox').removeAttr('disabled');
				}

				// 必須入力設定
				target.numberbox('options').required = false;
				target.numberbox('textbox').validatebox('options').required = false;
				target.numberbox('validate');

				target2.numberbox('options').required = true;
				target2.numberbox('textbox').validatebox('options').required = true;
				target2.numberbox('validate');

				// 入力値クリア
				$.setInputboxValue($("#"+$.id_inp.txt_tengpcd), '');
				$.setInputboxValue($("#"+$.id_inp.txt_tengpcd+'_2'), '');

				if(that.sendBtnid!==$.id.btn_new){
					$("#gf").hide();
					$("#"+$.id.grd_tenpo).attr('tabindex', -1).hide();
				}

				//
				$("#"+gridId).datagrid("reload");

			}else{
				// 非表示：2桁
				$("#tengp2").hide();
				if(that.sendBtnid!==$.id.btn_new){
					$.setInputBoxDisable(target2);
				}
				target2.textbox('textbox').attr('tabindex', -1);
				target2.textbox('textbox').attr('readonly', 'readonly');
				target2.textbox('textbox').attr('disabled', 'disabled');
				// 入力値クリア
				$.setInputboxValue($("#"+$.id_inp.txt_tengpcd), '');
				$.setInputboxValue($("#"+$.id_inp.txt_tengpcd+'_2'), '');

				// 表示：4桁
				$("#tengp1").show();
				if(that.sendBtnid!==$.id.btn_sel_change && that.sendBtnid!==$.id.btn_sel_refer){
					target.textbox('textbox').attr('readonly',false);
					target.textbox('textbox').attr('tabIndex', 3);
					target.textbox('textbox').removeAttr('disabled');
				}

				// 必須入力設定
				target.numberbox('options').required = true;
				target.numberbox('textbox').validatebox('options').required = true;
				target.numberbox('validate');

				target2.numberbox('options').required = false;
				target2.numberbox('textbox').validatebox('options').required = false;
				target2.numberbox('validate');

				$("#"+gridId).datagrid("reload");

				// データグリッドを表示にする。
				$("#gf").children().show();
			}
			var sel_gpkbn		= $.getInputboxValue($('#'+$.id_mei.kbn140));				// グループ区分
			// グループ区分を保持
			that.gpkbn = sel_gpkbn;
			// 変更値を保持
			that.searchCondition[0][$.id.rad_areakbn] = kbn;

		},
		setRadioAreakbn: function(reportno, id){
			var that = this;
			var idx = -1;

			var id = id;
			// Radio 要素の初期化
			// 初期化情報取得
			var json = $.getJSONObject(that.jsonHidden, id);
			if (json){
				// 初期化
				$('input[name="'+id+'"]').val([json.value]);
			}
			$('input[name="'+id+'"]').change(function() {
				if(idx > 0 && that.queried){
					$($.id.hiddenChangedIdx).val("1");

					$("#"+id).datagrid("reload");

					that.editRowIndex[id] = -1;
				}
			});

			if(that){
				if ($.inArray(id, that.initedObject) < 0){
					that.initedObject.push(id);
				}
				// 初期表示処理
				$.initialDisplay(that);
			}

			idx = 1;
		},
		setEditableGrid: function(that, reportno, id){		// データ表示
			//that.editRowIndex[id] = -1;
			var index = -1;

			var funcClickRow = $.fn.datagrid.defaults.onClickRow;
			var funcBeginEdit= $.fn.datagrid.defaults.onBeginEdit;
			var funcEndEdit= $.fn.datagrid.defaults.onEndEdit;

			if(that.reportYobiInfo()!=='1'){
				that.editRowIndex[id] = -1;
				funcClickRow = function(index,field){$.clickEditableDatagridCell(that,id, index)};
				funcBeginEdit = function(index,row){$.beginEditDatagridRow(that,id, index, row)};
				funcEndEdit = function(index,row,changes){$.endEditDatagridRow(that, id, index, row);};

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
				fit:true,
				rownumbers:true,
				singleSelect:true,
				checkOnSelect:false,
				selectOnCheck:false,
				url:$.reg.easy,
				onBeforeLoad:function(param){
					index = -1;
					var values = {};
					var txt_tengpcd		 = $('#'+$.id_inp.txt_tengpcd).numberbox('getValue');	// 配送グループコード
					var kbn140			 = $('#'+$.id_mei.kbn140).combogrid('getValue');		// グループ区分
					var SelBumon		 = $('#'+$.id.SelBumon).combogrid('getValue');			// 部門

					values["callpage"]	 = $($.id.hidden_reportno).val()						// 呼出元レポート名
					values["TENGPCD"]	 = txt_tengpcd											// 店グループ
					values["GPKBN"]		 = kbn140												// グループ区分
					values["BMNCD"]		 = SelBumon												// 部門

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

					var rad_areakbn		= $("input[name="+$.id.rad_areakbn+"]:checked").val();			// グループ区分
					if(rad_areakbn == "0"){
						// データグリッドを非表示にする。
						$("#gf").children().hide();
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
				var rad_areakbn		= $("input[name="+$.id.rad_areakbn+"]:checked").val();			// グループ区分
				var tengpcd = ""																	// 店グループコード

				var targetDatas = [{}];
				$('#'+that.focusRootId).find('[col^=F]').each(function(){
					var col = $(this).attr('col');
					targetDatas[0][col] = $.getInputboxValue($(this));
				});

				if(rad_areakbn === '0'){
					// 1-仕入グループ選択時
					tengpcd = $.getInputboxValue($('#'+$.id_inp.txt_tengpcd+'_2'))		// 店グループコード(2桁)

				}else{
					tengpcd = $.getInputboxValue($('#'+$.id_inp.txt_tengpcd))			// 店グループコード(4桁)
				}

				for (var i=0; i<targetDatas.length; i++){
					var rowDate = {
							F1	 : targetDatas[i]["F1"],
							F2	 : targetDatas[i]["F2"],
							F3	 : rad_areakbn,
							F4	 : tengpcd,
							F5	 : targetDatas[i]["F4"],
							F6	 : targetDatas[i]["F5"],
							F7	 : targetDatas[i]["F6"],
					};
					if(rowDate){
						targetRows.push(rowDate);
					}
				}
				data["data"] = targetRows;
			}

			// 店舗一覧
			if(target===undefined || target===$.id.grd_tenpo){
				var rowsTenpo		 = $('#'+$.id.grd_tenpo).datagrid('getRows');
				var txt_tengpcd		 = $('#'+$.id_inp.txt_tengpcd).textbox('getValue');
				var kbn140			 = $('#'+$.id_mei.kbn140).combogrid('getValue');
				var SelBumon		 = $('#'+$.id.SelBumon).combogrid('getValue');

				for (var i=0; i<rowsTenpo.length; i++){
					var rowDate = {
							F1	 : kbn140,
							F2	 : SelBumon,
							F3	 : txt_tengpcd,
							F4	 : rowsTenpo[i]["TENCD"],
					};
					targetRows.push(rowDate);
				}
				data[$.id.grd_tenpo] = targetRows;
			}
			return data;
		},
		getMergeGridDate: function(target,gridDel){
			// 保持したデータと入力データ比較を比較する。
			var that = this;

			var newrows = that.getGridData(target)[target];		// 変更データ
			var oldrows = [];
			var targetRows= [];


			/*if(target===undefined || target==="data"){
				// 基本情報
				oldrows = that.data
				for (var i=0; i<newrows.length; i++){
					if( newrows[i]['F1'] !== oldrows[i]['F1']
						|| newrows[i]['F2'] !== oldrows[i]['F2']
						|| newrows[i]['F3'] !== oldrows[i]['F3']
						|| newrows[i]['F4'] !== oldrows[i]['F4']
						|| newrows[i]['F5'] !== oldrows[i]['F5']
						|| newrows[i]['F6'] !== oldrows[i]['F6']
						|| newrows[i]['F7'] !== oldrows[i]['F7']){
						var rowDate = {
								F1	 : newrows[i]["F1"],
								F2	 : newrows[i]["F2"],
								F3	 : newrows[i]["F3"],
								F4	 : newrows[i]["F4"],
								F5	 : newrows[i]["F5"],
								F6	 : newrows[i]["F6"],
								F7	 : newrows[i]["F7"],
						};
						if(rowDate){
							targetRows.push(rowDate);
						}
					}
				}
			}*/

			if(target===undefined || target===$.id.grd_tenpo){
				// 店舗一覧
				oldrows = that.grd_tenpo_data
				if(gridDel){
					// 削除用グリッドデータ取得
					for (var i=0; i<newrows.length; i++){
						if( newrows[i]['F4'] !== oldrows[i]['F4'] ){
							if((oldrows[i]['F4'] ? oldrows[i]['F4'] : "") !== ""
								&& (newrows[i]['F4'] ? newrows[i]['F4'] : "") === "" ){
								// 旧データ入力有で新データ入力無しの場合
								var rowDate = {
										F1	 : oldrows[i]["F1"],
										F2	 : oldrows[i]["F2"],
										F3	 : oldrows[i]["F3"],
										F4	 : oldrows[i]["F4"],
								};
								if(rowDate){
									targetRows.push(rowDate);
								}
							}
						}
					}
				}else{
					// 登録用グリッドデータ取得
					for (var i=0; i<newrows.length; i++){
						if(newrows[i]['F4']){
							var rowDate = {
									F1	 : newrows[i]["F1"],
									F2	 : newrows[i]["F2"],
									F3	 : newrows[i]["F3"],
									F4	 : newrows[i]["F4"],
							};
							if(rowDate){
								targetRows.push(rowDate);
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
			if(target===undefined || target==="data"){
				that.data =  data["data"];
			}

			// 店舗一覧
			if(target===undefined || target===$.id.grd_tenpo){
				that.grd_tenpo_data =  data[$.id.grd_tenpo];
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
			$.setJSONObject(sendJSON, 'sendBtnid', btnId, $('#'+btnId).attr("title"));		// 実行ボタン情報保持

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
				if(that.reportYobiInfo()==='1'){
					index = 2;
				}else{
					index = 1;
				}
				childurl = href[index];

				/*if(that.sendBtnid == 'btn_new'){
					// 新規登録時には再検索を行わない。
					sendMode = 1;
					$.setJSONObject(sendJSON, 'sendBtnid', '', '');		// 実行ボタン情報保持
				}*/

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

			// 検索、入力後特殊処理
			if(that.queried){
				// 特殊処理

				// エリア区分
				if(id==='rad_areakbn'){
					if(newValue !== "" && newValue ){
						if(that.openMsg){

							if(!that.searchCondition[0][$.id.rad_areakbn] || that.searchCondition[0][$.id.rad_areakbn] == ''){
								// 初期設定値が空の場合、newValueが'0'の場合はメッセージを表示しない。(デフォルト店グループが2桁設定の為)
								if(newValue == '0'){
									that.searchCondition[0][$.id.rad_areakbn] = newValue;
								}

							}else{

								var dispMsg = true;

								if(newValue == '0'){
									// [店グループ]→[エリア]に変更した場合

									// Grid内の入力内容を確認
									var targetRows = $('#'+$.id.grd_tenpo).datagrid('getRows');
									var tenList = []
									for (var i=0; i<targetRows.length; i++){
										if(targetRows[i]["TENCD"] && targetRows[i]["TENCD"] !== ''){
											tenList.push(targetRows[i]["TENCD"])
										}
									}

									var tengpcd = $.getInputboxValue($('#'+$.id_inp.txt_tengpcd))			// 店グループコード(4桁)

									if(tenList.length == 0 && tengpcd == ""){
										// グリッド内に入力値が存在しない場合、店グループが未入力の場合は、変更を行ってもメッセージを表示しない。
										that.setDispOption(newValue, $.id.grd_tenpo);
										// 変更値を保持
										that.searchCondition[0][$.id.rad_areakbn] = newValue;

										dispMsg = false
									}
								}else if(newValue == '1'){
									// [エリア]→[店グループ]に変更した場合
									var tengpcd = $.getInputboxValue($('#'+$.id_inp.txt_tengpcd+'_2'))		// 店グループコード(2桁)

									if(tengpcd == ""){
										// 店グループが未入力の場合は、変更を行ってもメッセージを表示しない。
										that.setDispOption(newValue, $.id.grd_tenpo);
										// 変更値を保持
										that.searchCondition[0][$.id.rad_areakbn] = newValue;

										dispMsg = false
									}
								}

								var func_ok = function(){
									// 店グループコードの桁数切替
									that.setDispOption(newValue, $.id.grd_tenpo);
									// 変更値を保持
									that.searchCondition[0][$.id.rad_areakbn] = newValue;
								}

								var func_no = function(){
									// 前回入力値設定によるメッセージ再表示を防ぐため、表示フラグをOFFにする。
									that.openMsg = false;
									// エリア区分を前回の値に戻す。
									$.setInputboxValue($("input[name="+$.id.rad_areakbn+"]"), that.searchCondition[0][$.id.rad_areakbn]);		// グループ区分
								}
								if(dispMsg){
									// メッセージを表示
									that.confirmReportUnregist(func_ok,func_no);
								}
							}
						}else{
							// キャンセル選択時に前回入力データの設定により、changeInputFunctionが動くため、メッセージを表示せずに表示フラグをtrueに戻す
							that.openMsg = true;
						}
					}
				}
			}
			var msgid = null;
			if(msgid !==null){
				$.showMessage(msgid);
				return false;
			}
		},
		// IDとvalueでチェック処理を実施
		checkInputboxFunc:function(id, newValue, kbn, record, isNew){
			var that = this;

			// 店舗コード
			if(id===$.id_inp.txt_tencd){
				if(newValue !== '' && newValue){
					// 店舗コード
					var param = {};
					param["KEY"] =  "MST_CNT";
					param["value"] = newValue;
					var chk_cnt = $.getInputboxData(that.name, $.id.action_check, $.id_inp.txt_tencd, [param]);
					if(chk_cnt==="" || chk_cnt==="0"){
						return "E11096";
					}
				}
			}

			// 店舗部門マスタ
			if(id===$.id_inp.txt_tengpcd){
				if(newValue !== '' && newValue){
					var bmncd = $.getInputboxValue($('#'+$.id.SelBumon)); 			// 商品コード

					var param = {};
					param["KEY"] =  "MST_CNT";
					param["value"] = newValue + ',' + bmncd;
					var chk_cnt = $.getInputboxData(that.name, $.id.action_check, 'MSTTENBMN', [param]);
					if(chk_cnt==="" || chk_cnt==="0"){
						return "E11030";
					}
				}
			}
			return null;
		},
		getInputboxParams: function(that, id, newValue){
			// 情報取得
			var values = {};
			values["value"] = newValue;

			// 情報設定
			return [values];
		},
		/**
		 * 帳票移動の警告処理
		 * @param func	- OKボタンが押下されたのちに実行される処理
		 * @param msgid	- 警告メッセージID
		 */
		confirmReportUnregist : function(func_ok, func_no, msgid){
			// 未登録の警告メッセージが必要な場合
			if($.getConfirmUnregistFlg($($.id.hiddenChangedIdx))){
				if(msgid===undefined) msgid = 'E11025';
				$.showMessage(msgid, undefined, func_ok, func_no);
			}else if (typeof func === 'function'){
				func();
			}
		},
	} });
})(jQuery);