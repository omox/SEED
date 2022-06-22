/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_ReportBT002',			// （必須）レポートオプションの確認
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
		dedefaultObjNum:	40,	// 初期化オブジェクト数
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
		reportYobiInfo_bf:'',				// 参照情報(前画面)
		updConfirmMsg:"",
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
			//$.setCheckboxInit2(that.jsonHidden, $.id.chk_del, that);

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

			// ランクNo初期化
			$.setInputbox(that, reportno, $.id_inp.txt_tencd+'_add_F1', isUpdateReport);	// 追加店ランクNo
			$.setInputbox(that, reportno, $.id_inp.txt_tencd+'_del_F1', isUpdateReport);	// 除外店ランクNo

			// PLU送信日
			$.setInputbox(that, reportno, 'txt_plukbn', isUpdateReport);

			$.winBM015.init(that);	// 対象店確認
			$.winST007.init(that);	// ランクNo
			$.winST008.init(that);	// ランク情報
			$.winST009.init(that);	// 対象店

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

			// 当帳票を「参照」で開いた場合
			if(that.reportYobiInfo()==='1'){
				$.initReportInfo("BT006", "分類割引　参照　明細", "参照");
				$.setInputBoxDisable($($.id.hiddenChangedIdx));
				$("#"+$.id.btn_upd).linkbutton('disable');
				$("#"+$.id.btn_upd).attr('tabindex', -1).hide();
				$("#"+$.id.btn_cancel).linkbutton('disable');
				$("#"+$.id.btn_cancel).attr('tabindex', -1).hide();
				$("#"+$.id.btn_del).linkbutton('disable');
				$("#"+$.id.btn_del).attr('tabindex', -1).hide();
				$("#"+$.id.btn_rankno+'_add').linkbutton('disable');
				$("#"+$.id.btn_rankno+'_add').attr('tabindex', -1);
				$("#"+$.id.btn_rankno+'_del').linkbutton('disable');
				$("#"+$.id.btn_rankno+'_del').attr('tabindex', -1);

				// 参照時、項目を入力不可にする。
				$('#'+that.focusRootId).find('[col^=F]').each(function(){
					$.setInputBoxDisable($(this));
				});

			}else{
				if(that.sendBtnid === $.id.btn_new){
					$.initReportInfo("BT002", "分類割引　新規　明細", "新規");
					$("#"+$.id.btn_del).linkbutton('disable');
					$("#"+$.id.btn_del).attr('tabindex', -1).hide();
					$("#disp_record_info").hide();

					// 追加必須入力項目
					var options = null;
					options = $("#"+$.id_inp.txt_rankno_add).numberbox('options')
					options.required = true;

				}else{
					$.initReportInfo("BT006", "分類割引　変更　明細", "変更");

					$("#"+$.id.btn_rankno+'_add').linkbutton('disable');
					$("#"+$.id.btn_rankno+'_add').attr('tabindex', -1);
					$("#"+$.id.btn_rankno+'_del').linkbutton('disable');
					$("#"+$.id.btn_rankno+'_del').attr('tabindex', -1);

					$.setInputBoxDisable($("#"+$.id_inp.txt_moyscd));
					$.setInputBoxDisable($("#"+$.id_inp.txt_rankno_add));
					$.setInputBoxDisable($("#"+$.id_inp.txt_rankno_del));
					$.setInputBoxDisable($("#"+$.id_inp.txt_bmncd));

				}
			}

			// 入力不可項目
			$.setInputBoxDisable($("#"+$.id_inp.txt_moykn));		// 催し名称
			$.setInputBoxDisable($("#"+$.id_inp.txt_moysstdt));		// 催し開始日
			$.setInputBoxDisable($("#"+$.id_inp.txt_moyseddt));		// 催し終了日

			// 各種遷移ボタン
			$('#'+$.id.btn_cancel).on("click", $.pushChangeReport);
			$.setInputBoxDisable($("#"+$.id_inp.txt_tenkn));
			$("#btn_suryo").on('click',function(e){
				var suryo	 = $('#'+$.id_inp.txt_suryo).numberbox('getValue');
				that.setSuryo(that, $.id.grd_tenpo_sk, suryo)

			})

		},
		setSuryo: function(that, id, suryo){		// 数量コピー押下時に、入力された数量をグリッドに反映させる。
			var rows = [];
			rows = $('#'+id).datagrid('getRows');

			if(rows.length > 0){
				for (var i=0; i<rows.length; i++){
					if((rows[i]['SHNCD'] ? rows[i]['SHNCD'] : "") !== ""){
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
			var txt_kkkno		= $.getJSONObject(this.jsonString, $.id_inp.txt_kkkno).value;	// 企画No

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$.appendMaskMsg();

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,		// レポート名
					KKKNO:			txt_kkkno,
					SENDBTNID:		that.sendBtnid,
					t:				(new Date()).getTime(),
					sortable:		sortable,
					sortName:		that.sortName,
					sortOrder:		that.sortOrder,
					rows:			1	// 表示可能レコード数
				},
				function(json){
					// 検索処理エラー判定
					//if($.searchError(json)) return false;

					if($.searchError(json)){
						return false;
					}

					// ログ出力
					$.log(that.timeData, 'query:');

					// 検索データ（想定）
					that.gridData = JSON.parse(json).rows;
					that.gridTitle = JSON.parse(json).titles;

					var opts = JSON.parse(json).opts

					// メインデータ表示
					that.setData(that.gridData, opts);
					// 店確認一覧にデータをセット
					that.getsetConfTenpo();

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
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform).form('validate');
			if(!rt){
				$.addErrState(that, $('.validatebox-invalid').eq(0), false);
				return rt;
			}

			// 変更の場合確認メッセージを表示
			that.updConfirmMsg = "W00001";
			if (that.sendBtnid===$.id.btn_sel_change) {
				var param = {};
				param["KEY"] =  "CNT";
				param["value"] = $.getInputboxValue($('#'+$.id_inp.txt_moyscd));
				var chk_cnt = $.getInputboxData(that.name, $.id.action_check, $.id_inp.txt_moyscd,[param]);
				if(chk_cnt==="1"){

					// 分類割引 課題No.12 登録可能な条件の追加
					var chk_endday = parseInt($.getInputboxValue($('#'+$.id_inp.txt_hbeddt)));	// 販売期間_終了日

					param = {};			// 処理日付
					param["KEY"] =  "SHORIDT";
					param["value"] = "";
					var chk_shoriday = parseInt($.getInputboxData(that.name, $.id.action_check, $.id_inp.txt_moyscd,[param]));

					param = {};			// PLU配信日
					param["KEY"] =  "PLUSDDT";
					param["value"] = $.getInputboxValue($('#'+$.id_inp.txt_moyscd));
					var chk_pluday = parseInt($.getInputboxData(that.name, $.id.action_check, $.id_inp.txt_moyscd,[param]));

					if (chk_shoriday > chk_endday){	// 処理日付 > 販売期間_終了日 ならワーニング
						that.updConfirmMsg = "E20271";
					}
				}
			}

			// 新規の場合、分類割引_企画の重複チェック確認メッセージを表示
			if (that.sendBtnid===$.id.btn_new) {

				param = {};			// 重複が有るか
				param["KEY"] =  "NEWDEPLICATECHEACK";
				param["value"] = $.getInputboxValue($('#'+$.id_inp.txt_moyscd));
				param["value2"] = $.getInputboxValue($('#'+$.id_inp.txt_bmncd));
				param["value3"] = $.getInputboxValue($('#'+$.id_inp.txt_daicd));
				param["value4"] = $.getInputboxValue($('#'+$.id_inp.txt_chucd));
				var newchk_deplicate = parseInt($.getInputboxData(that.name, $.id.action_check, $.id_inp.txt_moyscd,[param]));

				if (newchk_deplicate != 0) {
					$.showMessage('E11040',['同一の分類コードで「催し区分」「催し開始日」']);
					return false;
				}
			}

			// 催しコード
			var param =  $.getInputboxValue($('#'+$.id_inp.txt_moyscd)); 		// 催しコード
			var msgid = that.checkInputboxFunc($.id_inp.txt_moyscd, param , '');
			if(msgid !==null || msgid){
				$.showMessage(msgid,undefined,function () { $.addErrState(that, $('#'+$.id_inp.txt_moyscd),true) });
				return false;
			}

			// 部門
			var param =  $.getInputboxValue($('#'+$.id_inp.txt_bmncd)); 		// 部門コード
			var msgid = that.checkInputboxFunc($.id_inp.txt_bmncd, param , '');
			if(msgid !==null || msgid){
				$.showMessage(msgid,undefined,function () { $.addErrState(that, $('#'+$.id_inp.txt_bmncd),true) });
				return false;
			}

			// 大分類
			var param =  $.getInputboxValue($('#'+$.id_inp.txt_daicd)); 		// 大分類コード
			var msgid = that.checkInputboxFunc($.id_inp.txt_daicd, param , '');
			if(msgid !==null || msgid){
				$.showMessage(msgid,undefined,function () { $.addErrState(that, $('#'+$.id_inp.txt_daicd),true) });
				return false;
			}

			// 中分類
			var param =  $.getInputboxValue($('#'+$.id_inp.txt_chucd)); 		// 中分類コード
			var msgid = that.checkInputboxFunc($.id_inp.txt_chucd, param , '');
			if(msgid !==null || msgid){
				$.showMessage(msgid,undefined,function () { $.addErrState(that, $('#'+$.id_inp.txt_chucd),true) });
				return false;
			}

			// 販売開始日
			var param =  $.getInputboxValue($('#'+$.id_inp.txt_hbstdt)); 		// 販売開始日
			if(param || param!==''){
				var msgid = that.checkInputboxFunc($.id_inp.txt_hbstdt, param , '');
				if(msgid !==null || msgid){
					$.showMessage(msgid,undefined,function () { $.addErrState(that, $('#'+$.id_inp.txt_hbstdt),true) });
					return false;
				}
			}

			// 販売終了日
			var param =  $.getInputboxValue($('#'+$.id_inp.txt_hbeddt)); 		// 販売終了日
			if(param || param!==''){
				var msgid = that.checkInputboxFunc($.id_inp.txt_hbeddt, param , '');
				if(msgid !==null || msgid){
					$.showMessage(msgid,undefined,function () { $.addErrState(that, $('#'+$.id_inp.txt_hbeddt),true) });
					return false;
				}
			}

			// タイムサービス_開始時間
			var param =  $.getInputboxValue($('#'+$.id_inp.txt_promo_bgm_tm)); 		// 販売終了日
			if(param || param!==''){
				var msgid = that.checkInputboxFunc($.id_inp.txt_promo_bgm_tm, param , '');
				if(msgid !==null || msgid){
					$.showMessage(msgid,undefined,function () { $.addErrState(that, $('#'+$.id_inp.txt_promo_bgm_tm),true) });
					return false;
				}
			}

			// タイムサービス_終了時間
			var param =  $.getInputboxValue($('#'+$.id_inp.txt_promo_end_tm)); 		// 販売終了日
			if(param || param!==''){
				var msgid = that.checkInputboxFunc($.id_inp.txt_promo_end_tm, param , '');
				if(msgid !==null || msgid){
					$.showMessage(msgid,undefined,function () { $.addErrState(that, $('#'+$.id_inp.txt_promo_end_tm),true) });
					return false;
				}
			}

			// 対象店ランク
			var rankAdd =  $.getInputboxValue($('#'+$.id_inp.txt_rankno_add)); 		// ランクNo
			if(rankAdd || rankAdd!==''){
				var msgid = that.checkInputboxFunc($.id_inp.txt_rankno, rankAdd , '');
				if(msgid !==null || msgid){
					$.showMessage(msgid,undefined,function () { $.addErrState(that, $('#'+$.id_inp.txt_rankno_add),true) });
					return false;
				}
			}

			// 除外店ランク
			var rankDel =  $.getInputboxValue($('#'+$.id_inp.txt_rankno_del)); 		// ランクNo
			if(rankDel || rankDel!==''){
				var msgid = that.checkInputboxFunc($.id_inp.txt_rankno, rankDel , '');
				if(msgid !==null || msgid){
					$.showMessage(msgid,undefined,function () { $.addErrState(that, $('#'+$.id_inp.txt_rankno_del),true) });
					return false;
				}
			}

			// 対象・除外店ランク№に同一の値が入力された場合エラー
			if(rankAdd == rankDel){
				$.showMessage('E20016',undefined,function () { $.addErrState(that, $('#'+$.id_inp.txt_rankno_add),true) });
				return false;

			}
			return rt;
		},
		updSuccess: function(id){	// validation OK時 の update処理
			var that = this;

			// 変更行情報取得
			//var changedIndex = $($.id.hiddenChangedIdx).val().split(",");

			// 基本入力情報取得
			var targetDatas = that.getGridData("data")["data"];

			var moyscdArray	 = $.getInputboxText($('#'+$.id_inp.txt_moyscd)).split("-");	// 催しコード
			var moysstdt	 = $.getInputboxValue($('#'+$.id_inp.txt_moysstdt));			// 催し開始日
			var moyseddt	 = $.getInputboxValue($('#'+$.id_inp.txt_moyseddt));			// 催し終了日

			/*var targetDatas = [{}];
			$('#'+that.focusRootId).find('[col^=F]').each(function(){
				var col = $(this).attr('col');
				targetDatas[0][col] = $.getInputboxValue($(this));
			});*/

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
					MOYSKBN	:		moyscdArray[0],
					MOYSSTDT:		moysstdt,
					MOYSRBAN:		moyscdArray[2],
					MOYSEDDT:		moyseddt,
//					IDX:			$($.id.hiddenChangedIdx).val(),	// 更新対象Index
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
		updConfirm: function(func){	// validation OK時 の update処理
			var that = this;
			var msgId = that.updConfirmMsg;

			// 分類割引 課題No.12 登録可能な条件の追加
			var chk_endday = parseInt($.getInputboxValue($('#'+$.id_inp.txt_hbeddt)));	// 販売期間_終了日

			param = {};			// 処理日付
			param["KEY"] =  "SHORIDT";
			param["value"] = "";
			var chk_shoriday = parseInt($.getInputboxData(that.name, $.id.action_check, $.id_inp.txt_moyscd,[param]));

			param = {};			// PLU配信日
			param["KEY"] =  "PLUSDDT";
			param["value"] = $.getInputboxValue($('#'+$.id_inp.txt_moyscd));
			var chk_pluday = parseInt($.getInputboxData(that.name, $.id.action_check, $.id_inp.txt_moyscd,[param]));

			if (chk_shoriday > chk_endday){	// 処理日付 > 販売期間_終了日 ならワーニング
				// PLU配信済フラグ
				var txt_plusflg = $.getInputboxValue($('#txt_plusflg'));

				//「催しの店舗配信済みのため、店舗に反映されません。登録しますか？」（警告）を「はい」「いいえ」のダイアログで表示し、処理続行を確認する。
				if(txt_plusflg == '1'){
					// W20059	催しの店舗配信済みのため、店舗に反映されません。登録しますか？	 	4	 	Q
					msgId = ("E20059");
				}
			}

			$.showMessage(msgId, undefined, func);
		},
		delValidation: function (){	// （必須）批准
			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = true;

			// 入力チェック用の配列をクリア
			that.jsonTemp = [];

			// 催しコード
			var param =  $.getInputboxValue($('#'+$.id_inp.txt_moyscd)); 		// 催しコード
			var msgid = that.checkInputboxFunc($.id_inp.txt_moyscd, param , '' , '', false);
			if(msgid !==null || msgid){
				$.showMessage(msgid);
				return false;
			}

			return rt;
		},
		delSuccess: function(id){
			var that = this;

			var txt_kkkno		= $.getJSONObject(this.jsonString, $.id_inp.txt_kkkno).value;	// 企画No

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$.appendMaskMsg();

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,			// レポート名
					action:			$.id.action_delete,	// 実行処理情報
					obj:			id,					// 実行オブジェクト
					KKKNO:			txt_kkkno,			// 企画No
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
				id:		$.id_inp.txt_kkkno,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_kkkno),
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

			// 入力制限チェック
			var txt_plukbn = $.getInputboxValue($('#txt_plukbn'));		// PLU送信フラグ

			if(txt_plukbn && txt_plukbn != ""){
				if(txt_plukbn==='1'){
					// PLU配信日 < 処理日付 の場合、項目の入力制限を行う
					$.setInputBoxDisable($("#"+$.id_inp.txt_btkn));		// 分類割引名称
					$.setInputBoxDisable($("#"+$.id_inp.txt_hbstdt));	// 販売期間_開始日
					$.setInputBoxDisable($("#"+$.id_inp.txt_hbeddt));	// 販売期間_終了日
					$.setInputBoxDisable($("#"+$.id_inp.txt_daicd));	// 大分類コード
					$.setInputBoxDisable($("#"+$.id_inp.txt_chucd));	// 中分類コード

					// 追加、削除店コード
					for (var i=0; i<10; i++){
						$.setInputBoxDisable($("#"+$.id_inp.txt_tencd+'_add_'+(i+1)));
						$.setInputBoxDisable($("#"+$.id_inp.txt_tencd+'_del_'+(i+1)));
					}
					// フォーカスを再設定する。
					$.setFocusFirst();
				}
			}
		},
		getsetConfTenpo: function(){
			var that = this
			// 店確認一覧画面のデータを取得する

			var values 	= {};
			var kkkno	=  $.getJSONObject(this.jsonString, $.id_inp.txt_kkkno).value;	// 企画No
			var id 		= $.id.grd_adten;

			values["callpage"]	 = $($.id.hidden_reportno).val()				// 呼出元レポート名
			values["KKKNO"]		 = kkkno										// 企画No

			var json = [values];
			$.ajax({
				url: $.reg.easy,
				type: 'POST',
				async: true,
				data: {
					page	: that.name,
					obj		: id,
					sel		: (new Date()).getTime(),
					target	: id,
					action	: $.id.action_init,
					json	: JSON.stringify(json),
					datatype: "datagrid"
				},
				success: function(json){
					var value = "";
					var rows = [];

					if(json !==""){
						rows = JSON.parse(json).rows
						for (var i=0; i<rows.length; i++){
							$.setInputboxValue($('#'+$.id_inp.txt_tencd+'_add_'+(i+1)), rows[i].ADDTEN);
							$.setInputboxValue($('#'+$.id_inp.txt_tencd+'_del_'+(i+1)), rows[i].DELTEN);
						}
					}
					that.queried = true;
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

				var txt_kkkno		= $.getJSONObject(this.jsonString, $.id_inp.txt_kkkno).value;	// 企画No
				var moyscdArray		= $.getInputboxText($('#'+$.id_inp.txt_moyscd)).split("-");		// 催しコード

				for (var i=0; i<targetDatas.length; i++){
					var rowDate = {
							F1	 : txt_kkkno,					// 企画No
							F2	 : targetDatas[i]["F5"],		// 分類割引名称
							F3	 : targetDatas[i]["F6"],		// 販売期間_開始日
							F4	 : targetDatas[i]["F7"],		// 販売期間_終了日
							F5	 : moyscdArray[0],		 		// 催し区分
							F6	 : moyscdArray[1], 				// 催し開始日
							F7	 : moyscdArray[2],				// 催し連番
							F8	 : targetDatas[i]["F8"],		// 部門
							F9	 : targetDatas[i]["F9"],		// 大分類
							F10	 : targetDatas[i]["F10"],		// 中分類
							F11	 : '',							// 小分類
							F12	 : targetDatas[i]["F18"],		// 割引額(開始時間)
							F13	 : targetDatas[i]["F19"],		// 一律額(終了時間)
							F14	 : targetDatas[i]["F11"],		// 割引率
							F15	 : targetDatas[i]["F12"],		// 対象店ランク
							F16	 : targetDatas[i]["F13"],		// 除外店ランク
							F17	 : '',							// 店扱いフラグ配列
							F18	 : '',							// 排他用項目
							F19	 : targetDatas[i]["F21"],		// 対象店
							F20	 : targetDatas[i]["F22"],		// 対象店
							F21	 : targetDatas[i]["F23"],		// 対象店
							F22	 : targetDatas[i]["F24"],		// 対象店
							F23	 : targetDatas[i]["F25"],		// 対象店
							F24	 : targetDatas[i]["F26"],		// 対象店
							F25	 : targetDatas[i]["F27"],		// 対象店
							F26	 : targetDatas[i]["F28"],		// 対象店
							F27	 : targetDatas[i]["F29"],		// 対象店
							F28	 : targetDatas[i]["F30"],		// 対象店
							F29	 : targetDatas[i]["F31"],		// 除外店
							F30	 : targetDatas[i]["F32"],		// 除外店
							F31	 : targetDatas[i]["F33"],		// 除外店
							F32	 : targetDatas[i]["F34"],		// 除外店
							F33	 : targetDatas[i]["F35"],		// 除外店
							F34	 : targetDatas[i]["F36"],		// 除外店
							F35	 : targetDatas[i]["F37"],		// 除外店
							F36	 : targetDatas[i]["F38"],		// 除外店
							F37	 : targetDatas[i]["F39"],		// 除外店
							F38	 : targetDatas[i]["F40"],		// 除外店
					};
					if(rowDate){
						targetRows.push(rowDate);
					}
				}
				data["data"] = targetRows;
			}

			// 追加店一覧
			if(target===undefined || target==='tenpo_list'){

			}

			// 追加対象店一覧
			if(target===undefined || target==='tenpo_list_add'){
				var targetDatas = [{}];
				var kkkno = "";
				var tencd = "";

				$('#'+that.focusRootId).find('[id^='+ $.id_inp.txt_tencd +'_add_F]').each(function(){
					tencd = $.getInputboxValue($(this));
					var rowDate = {
							F1	 : kkkno,						// 企画No
							F2	 : tencd,						// 店舗コード
					};
					if(rowDate){
						targetRows.push(rowDate);
					}
				});
				data["tenpo_list_add"] = targetRows;
			}

			// 追加除外店一覧
			if(target===undefined || target==='tenpo_list_del'){
				var targetDatas = [{}];
				var kkkno = "";
				var tencd = "";

				$('#'+that.focusRootId).find('[id^='+ $.id_inp.txt_tencd +'_del_F]').each(function(){
					tencd = $.getInputboxValue($(this));
					var rowDate = {
							F1	 : kkkno,						// 企画No
							F2	 : tencd,						// 店舗コード
					};
					if(rowDate){
						targetRows.push(rowDate);
					}
				});
				data["tenpo_list_del"] = targetRows;
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
				oldrows = that.grd_tenpo_yh_data
				for (var i=0; i<newrows.length; i++){
					if((oldrows[i]['F5'] ? oldrows[i]['F5'] : "") !== (newrows[i]['F5'] ? newrows[i]['F5'] : "")
					 	|| (oldrows[i]['F6'] ? oldrows[i]['F6'] : "") !== (newrows[i]['F6'] ? newrows[i]['F6'] : "")
					){
						if(newrows[i]["F1"]){
							var rowDate = {
									F1	 : newrows[i]["F1"],
									F2	 : newrows[i]["F2"],
									F3	 : newrows[i]["F3"],
									F4	 : newrows[i]["F4"],
									F5	 : newrows[i]["F5"] ? newrows[i]["F5"] : shoridt,
									F6	 : newrows[i]["F6"],
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
			/*if(target===undefined || target==="data"){
				that.data =  data["data"];
			}*/

			// 商品一覧
			if(target===undefined || target===$.id.grd_tenpo_yh){
				that.grd_tenpo_yh_data =  data[$.id.grd_tenpo_yh];
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
			case $.id.btn_cancel:
			case $.id.btn_back:
			case "btn_return":
				// 転送先情報
				if(that.reportYobiInfo()==='1'){
					index = 2;
				}else{
					index = 1;
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
			var func_focus = function () { $.addErrState(that, $('#'+id),true) };
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

			// 店配列取得
			$.winBM015.getTenArr(id);

			// 催しコード
			/*if(id===$.id_inp.txt_moyscd){
				// 入力制限チェック
				var txt_plukbn = $.getInputboxValue($('#txt_plukbn'));		// PLU送信フラグ
				var shoridt  = $('#'+$.id.txt_shoridt).val();				// 処理日付

				if(txt_plukbn && txt_plukbn != ""){
					if(Number(txt_plukbn) < Number(shoridt)){
						var isTemporary = false
						if(that.queried){
							isTemporary = true
						}

						$.setInputBoxDisableVariable($('#'+$.id_inp.txt_btkn),isTemporary);
						$.setInputBoxDisableVariable($('#'+$.id_inp.txt_hbstdt),isTemporary);
						$.setInputBoxDisableVariable($('#'+$.id_inp.txt_hbeddt),isTemporary);
						if(!that.queried){
							// 初回検索時に入力制限された場合は、フォーカスを再設定する。
							$.setFocusFirst();
						}
					}else{
						$.setInputBoxEnableVariable($('#'+$.id_inp.txt_btkn), true);
						$.setInputBoxEnableVariable($('#'+$.id_inp.txt_hbstdt), true);
						$.setInputBoxEnableVariable($('#'+$.id_inp.txt_hbeddt), true);
					}
				}
			}*/

			// 検索、入力後特殊処理
			if(that.queried){
				var msgid = null;

				// 催しコード
				if(id===$.id_inp.txt_moyscd){
					msgid = that.checkInputboxFunc(id, newValue, '');
				}

				// 部門コード
				if(id===$.id_inp.txt_bmncd){
					msgid = that.checkInputboxFunc(id, newValue, '');
				}

				// 大分類コード
				if(id===$.id_inp.txt_daicd){
					msgid = that.checkInputboxFunc(id, newValue, '');
				}

				// 中分類コード
				if(id===$.id_inp.txt_chucd){
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

			// 催しコード
			if(id===$.id_inp.txt_moyscd){
				if(newValue && newValue != ""){
					if(newValue.substr(0,1) != "1"
						&& newValue.substr(0,1) != "2"
						&& newValue.substr(0,1) != "3" ){
						// 催し区分が1、2、3以外の場合
						return "E20004";
					}
				}

				var param = {};
				param["KEY"] =  "MST_CNT";
				param["value"] = newValue;
				var chk_cnt = $.getInputboxData(that.name, $.id.action_check, $.id_inp.txt_moyscd, [param]);
				if(chk_cnt==="" || chk_cnt==="0"){
					return "E20100";
				} else {
					var param = {};
					param["KEY"] =  "CNT";
					param["value"] = $.getInputboxValue($('#'+$.id_inp.txt_moyscd));
					var chk_cnt = $.getInputboxData(that.name, $.id.action_check, $.id_inp.txt_moyscd,[param]);
					if(chk_cnt==="1"){
						if (!$.isEmptyVal(isNew) && !isNew) {
							// 分類割引 課題No.12 削除が可能な条件の追加
							var txt_kkkno		= $.getJSONObject(this.jsonString, $.id_inp.txt_kkkno).value;	// 企画No

							var param = {};		// 販売期間_終了日
							param["KEY"] =  "HBEDDT";
							param["value"] = txt_kkkno;
							var chk_endday = parseInt($.getInputboxData(that.name, $.id.action_check, $.id_inp.txt_moyscd,[param]));

							param = {};			// 処理日付
							param["KEY"] =  "SHORIDT";
							param["value"] = "";
							var chk_shoriday = parseInt($.getInputboxData(that.name, $.id.action_check, $.id_inp.txt_moyscd,[param]));

							param = {};			// PLU配信日
							param["KEY"] =  "PLUSDDT";
							param["value"] = $.getInputboxValue($('#'+$.id_inp.txt_moyscd));
							var chk_pluday = parseInt($.getInputboxData(that.name, $.id.action_check, $.id_inp.txt_moyscd,[param]));

							if (chk_shoriday > chk_endday) {	// 処理日付 > 分類割引_企画.販売期間_終了日ならワーニング
								return "E20510";
							}

						} else if (that.sendBtnid===$.id.btn_new) {
							/*return "E20272";*/
							/*return "E20059";*/
						}
					}
				}
			}

			// ランクNo
			if(id===$.id_inp.txt_rankno){
				var bmncd	 =  $.getInputboxValue($('#'+$.id_inp.txt_bmncd)); 			// 部門コード
				var param = {};

				if((newValue ? newValue : "") !== "" && (bmncd ? bmncd : "") !== ""){
					param["KEY"] =  "MST_CNT";
					param["value"] = bmncd +',' + newValue;
					var chk_cnt = $.getInputboxData(that.name, $.id.action_check, $.id_inp.txt_rankno, [param]);
					if(chk_cnt==="" || chk_cnt==="0"){
						return "E20057";
					}
				}
			}

			// 部門コード
			if(id===$.id_inp.txt_bmncd){
				var param = {};
				param["KEY"] =  "MST_CNT";
				param["value"] = newValue;
				var chk_cnt = $.getInputboxData(that.name, $.id.action_check, $.id_inp.txt_bmncd, [param]);
				if(chk_cnt==="" || chk_cnt==="0"){
					return "E11044";
				}
			}

			// 大分類コード
			if(id===$.id_inp.txt_daicd){
				var bmncd	 =  $.getInputboxValue($('#'+$.id_inp.txt_bmncd)); 			// 部門コード
				var param = {};

				// マスタ存在チェック
				if((newValue ? newValue : "") !== "" && (bmncd ? bmncd : "") !== ""){
					param["KEY"] =  "MST_CNT";
					param["value"] = bmncd + ','+ newValue;
					var chk_cnt = $.getInputboxData(that.name, $.id.action_check, $.id_inp.txt_daicd, [param]);
					if(chk_cnt==="" || chk_cnt==="0"){
						return "E11135";
					}
				}
			}

			// 中分類コード
			if(id===$.id_inp.txt_chucd){
				var bmncd	 =  $.getInputboxValue($('#'+$.id_inp.txt_bmncd)); 			// 部門コード
				var daicd	 =  $.getInputboxValue($('#'+$.id_inp.txt_daicd)); 			// 大分類コード
				var param = {};

				if((newValue ? newValue : "") !== "" && (bmncd ? bmncd : "") !== "" && (daicd ? daicd : "") !== ""){
					param["KEY"] =  "MST_CNT";
					param["value"] = bmncd +','+ daicd +',' + newValue;
					var chk_cnt = $.getInputboxData(that.name, $.id.action_check, $.id_inp.txt_chucd, [param]);
					if(chk_cnt==="" || chk_cnt==="0"){
						return "E11136";
					}
				}

				// 中分類入力時チェック
				if((newValue ? newValue : "") !== ""){
					if((daicd ? daicd : "") === ""){
						return "EX1088";
					}
				}
			}

			// タイムサービス_開始時間
			if(id===$.id_inp.txt_promo_bgm_tm){
				var endTime	 =  $.getInputboxValue($('#'+$.id_inp.txt_promo_end_tm)); 	// 終了時間

				if(newValue ? newValue : "" !== "" && endTime ? endTime : "" !== "" ){
					sdt = Number(newValue);
					edt = Number(endTime);
					if(sdt >= edt){	// 期間が逆の場合
						return "E20148";
					}
				}
			}

			// タイムサービス_終了時間
			if(id===$.id_inp.txt_promo_end_tm){
				var strTime	 =  $.getInputboxValue($('#'+$.id_inp.txt_promo_bgm_tm)); 	// 開始時間

				if(newValue ? newValue : "" !== "" && strTime ? strTime : "" !== "" ){
					sdt = Number(strTime);
					edt = Number(newValue);
					if(sdt >= edt){	// 期間が逆の場合
						return "E20148";
					}
				}
			}

			// 販売開始日
			if(id===$.id_inp.txt_hbstdt){
				var hbeddt	 =  $.getInputboxValue($('#'+$.id_inp.txt_hbeddt)); 		// 販売終了日
				var moysstdt =  $.getInputboxValue($('#'+$.id_inp.txt_moysstdt));		// 催し開始日

				// 販売終了日と比較
				if(newValue ? newValue : "" !== "" && hbeddt ? hbeddt : "" !== "" ){
					sdt = $.convDate(newValue, true);
					edt = $.convDate(hbeddt, true);
					if(sdt.getTime() > edt.getTime()){	// 期間が逆の場合
						return "E33031";
					}
				}

				// 催し開始日と比較
				if(newValue ? newValue : "" !== "" && moysstdt ? moysstdt : "" !== "" ){
					sdt = $.convDate(newValue, true);
					edt = $.convDate(moysstdt, true);
					if(sdt.getTime() < edt.getTime()){	// 期間が逆の場合
						return "E40016";
					}
				}
			}

			// 販売終了日
			if(id===$.id_inp.txt_hbeddt){
				var hbstdt =  $.getInputboxValue($('#'+$.id_inp.txt_hbstdt)); 			// 販売開始日
				var moyseddt =  $.getInputboxValue($('#'+$.id_inp.txt_moyseddt));		// 催し終了日

				// 販売開始日と比較
				if(newValue ? newValue : "" !== "" && hbstdt ? hbstdt : "" !== "" ){
					sdt = $.convDate(hbstdt, true);
					edt = $.convDate(newValue, true);
					if(sdt.getTime() > edt.getTime()){	// 期間が逆の場合
						return "E33031";
					}
				}

				// 催し開始日と比較
				if(newValue ? newValue : "" !== "" && moyseddt ? moyseddt : "" !== "" ){
					sdt = $.convDate(moyseddt, true);
					edt = $.convDate(newValue, true);
					if(sdt.getTime() < edt.getTime()){	// 期間が逆の場合
						return "E40016";
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

			// 大分類コード
			if(id===$.id_inp.txt_daicd){
				values["bmncd"] = $.getInputboxValue($('#'+$.id_inp.txt_bmncd));
			}

			// 中分類コード
			if(id===$.id_inp.txt_chucd){
				values["bmncd"] = $.getInputboxValue($('#'+$.id_inp.txt_bmncd));
				values["daicd"] = $.getInputboxValue($('#'+$.id_inp.txt_daicd));
			}

			// 情報設定
			return [values];
		},
	} });
})(jQuery);