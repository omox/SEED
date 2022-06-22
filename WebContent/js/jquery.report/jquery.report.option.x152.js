/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_Reportx152',			// （必須）レポートオプションの確認
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
		dedefaultObjNum:	71,// 初期化オブジェクト数
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
		grd_hsptn_data:[],					// グリッド情報
		grd_ehsptn_data:[],					// グリッド情報
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

			// 個別レイアウト調整：単品管理区分
			//$('#'+$.id_mei.kbn425).combobox({panelWidth:200,})

			// 店グループ（仕入）
			that.setEditableGrid(that, reportno, $.id.grd_hsptn+'_list');
			that.setEditableGrid(that, reportno, $.id.grd_ehsptn+'_list');

			that.onChangeReport = true;

			// ワッペン（配送パターン　デフォルト設定）
			that.setWappenHptnD(that, reportno, $.id_mei.kbn412+'_hptnd', isUpdateReport);

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

			that.setMeisyoCombo(that, reportno, 'grd_sel_tembetsudenpyoflg', 		isUpdateReport);
			that.setMeisyoCombo(that, reportno, 'grd_sel_keisancenter',				isUpdateReport);
			that.setMeisyoCombo(that, reportno, 'grd_sel_unyokbn', 					isUpdateReport);
			that.setMeisyoCombo(that, reportno, 'grd_sel_denpyokbn', 				isUpdateReport);
			that.setMeisyoCombo(that, reportno, 'grd_sel_shukeihyo1', 				isUpdateReport);
			that.setMeisyoCombo(that, reportno, 'grd_sel_pickingdata', 				isUpdateReport);
			that.setMeisyoCombo(that, reportno, 'grd_sel_pickinglist', 				isUpdateReport);
			that.setMeisyoCombo(that, reportno, 'grd_sel_wappen', 					isUpdateReport);
			that.setMeisyoCombo(that, reportno, 'grd_sel_ikkatsudenpyo', 			isUpdateReport);
			that.setMeisyoCombo(that, reportno, 'grd_sel_kakoshiji', 				isUpdateReport);
			that.setMeisyoCombo(that, reportno, 'grd_sel_ryutsukbn', 				isUpdateReport);
			that.setMeisyoCombo(that, reportno, 'grd_sel_zaikochiwake_denpyokbn', 	isUpdateReport);
			that.setMeisyoCombo(that, reportno, 'grd_sel_zaikochiwake_shukeihyo', 	isUpdateReport);
			that.setMeisyoCombo(that, reportno, 'grd_sel_zaikochiwake_pickingdata', isUpdateReport);
			that.setMeisyoCombo(that, reportno, 'grd_sel_zaikochiwake_pickinglist', isUpdateReport);
			that.setMeisyoCombo(that, reportno, 'grd_sel_yokomochisaki_kenshukbn', 	isUpdateReport);
			that.setMeisyoCombo(that, reportno, 'grd_sel_yokomochisaki_denpyokbn', 	isUpdateReport);
			that.setMeisyoCombo(that, reportno, 'grd_sel_yokomochisaki_shukeihyo', 	isUpdateReport);

			// Load処理回避
			// サブウインドウの初期化
			if(that.reportYobiInfo()!=='1'){
				$.win004.init(that);	// 配送パターン
				$.win005.init(that);	// 店舗一覧
				$.win008.init(that);	// エリア別配送パターン
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

			var reportYobi1 = $.getJSONValue(that.jsonHidden, "reportYobi1");
			if(!reportYobi1){
				reportYobi1 = $('#reportYobi1').val();
			}
			$('#reportYobi1').val(reportYobi1);

			// 各種遷移ボタン
			$('#'+$.id.btn_cancel).on("click", $.pushChangeReport);

			if(that.sendBtnid===$.id.btn_new){
				$.initReportInfo("SI002", "仕入先　新規登録", "新規");

			}else if(that.sendBtnid===$.id.btn_sel_change || that.reportYobiInfo()==='2'){
				$.initReportInfo("SI002", "仕入先　変更", "変更");
				$.setInputBoxDisable($("#"+$.id_inp.txt_sircd));

			}else if(that.sendBtnid===$.id.btn_sel_refer || that.reportYobiInfo()==='1'){
				$.initReportInfo("SI002", "仕入先　参照", "参照");
				$.setInputBoxDisable($("#"+$.id_inp.txt_sircd));

				$.setInputBoxDisable($("#"+$.id.btn_hsptn));
				$.setInputBoxDisable($("#btn_ehsptn_list"));
				$.setInputBoxDisable($("#"+$.id.btn_tenpo));
				//$.setInputBoxDisable($("#"+$.id.btn_maker));


				// 参照時、項目を入力不可にする。
				$('#'+that.focusRootId).find('[col^=F]').each(function(){
					$.setInputBoxDisable($(this));
					/*var col = $(this).attr('col');
					if(rows[0][col]){
						$.setInputboxValue($(this), rows[0][col]);
					}*/
				});
				// 参照項目(ref_editor)を入力不可に設定
				$.setInputBoxDisable($("#"+$.id_inp.txt_hsptn));
				$.setInputBoxDisable($("#"+$.id_inp.txt_tengpcd));
				$.setInputBoxDisable($("#"+$.id_inp.txt_rsircd));
			}

			if(that.reportYobiInfo()==='1'){
				$.setInputBoxDisable($($.id.hiddenChangedIdx));
				$('#'+$.id.btn_upd).linkbutton('disable');
				$('#'+$.id.btn_upd).attr('disabled', 'disabled').hide();
				$('#'+$.id.btn_cancel).linkbutton('disable');
				$('#'+$.id.btn_cancel).attr('disabled', 'disabled').hide();
			}

			if(that.sendBtnid===$.id.btn_new){
				$("#disp_record_info").hide();
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
			var txt_sircd		= $.getJSONObject(this.jsonString, $.id_inp.txt_sircd).value;		// 検索部門コード

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$.appendMaskMsg();

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,		// レポート名
					SIRCD:			txt_sircd,
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

					//var id = $.id_inp.txt_hsptn;
					//that.changeInputboxFunc( that, id, $.getInputboxValue($('#'+id)), $('#'+id));

					// 状態保存
					$.saveState2(reportno, that.getJSONString());

					// 初期状態で空白になる為、再描画
					//$('#'+$.id.grd_hsptn+'_list').datagrid('reload');
					//$('#'+$.id.grd_hsptn+'_list').datagrid('load', {} );

					// ログ出力
					$.log(that.timeData, 'loaded:');
				}
			);
		},
		setGridData: function (data, target){
			var that = this;

			// 基本データ
			/*if(target===undefined || target==="grd_data"){
				that.grd_data =  data["grd_data"];
			}*/

			// 配送パターン
			if(target===undefined || target===$.id.grd_hsptn+'_list'){
				that.grd_hsptn_data =  data;
			}

			// エリア別配送パターン
			if(target===undefined || target===$.id.grd_ehsptn+'_list'){
				that.grd_ehsptn_data =  data;
			}

			return true;
		},
		getCheckParam: function (row, id, def){
			var that = this;

			var param
			/*var paramId ={
					TEMBETSUDENPYOFLG:'',
					TEMBETSUDENPYOFLG:'',
					TEMBETSUDENPYOFLG:''

			}*/

			if ($.isEmptyVal(def)) {
				def = true;
			}

			if(!row[id] || (row[id] && row[id].trim() == '')){
				// 値未設定の場合デフォルトを参照する。
				if (def) {
					var palentId = 'sel_'+id.toLowerCase()
					param = $.getInputboxValue($('#'+palentId));
				} else {
					param = '';
				}
			}else if(row[id]){
				// 設定された値を返す
				param = row[id].split("-")[0]
			}

			return param;
		},
		updValidation: function (){	// （必須）批准

			//配送パターングリッドの編集を終了する。
			var row = $('#'+$.id.grd_hsptn+'_list').datagrid("getSelected");
			var rowIndex = $('#'+$.id.grd_hsptn+'_list').datagrid("getRowIndex", row);
			$('#'+$.id.grd_hsptn+'_list').datagrid('endEdit',rowIndex);

			// エリア別配送パターングリッドの編集を終了する。
			var row = $('#'+$.id.grd_ehsptn+'_list').datagrid("getSelected");
			var rowIndex = $('#'+$.id.grd_ehsptn+'_list').datagrid("getRowIndex", row);
			$('#'+$.id.grd_ehsptn+'_list').datagrid('endEdit',rowIndex);


			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform).form('validate');
			if(!rt){
				$.addErrState(that, $('.validatebox-invalid').eq(0), false);
				return rt;
			}
			//return rt;

			var sircd		 = $.getInputboxValue($('#'+$.id_inp.txt_sircd));		// 仕入先コード
			var hsptn		 = $.getInputboxValue($('#'+$.id_inp.txt_hsptn));		// 配送パターン
			var kbn404		 = $.getInputboxValue($('#'+$.id_mei.kbn404));			// いなげや在庫
			var kbn405		 = $.getInputboxValue($('#'+$.id_mei.kbn405));			// 買掛区分
			var dsircd		 = $.getInputboxValue($('#'+$.id_inp.txt_dsircd));		// 代表仕入先
			var df_rsircd	 = $.getInputboxValue($('#'+$.id_inp.txt_df_rsircd));	// デフォルト_実仕入先コード
			var kbn406		 = $.getInputboxValue($('#'+$.id_mei.kbn406));			// デフォルト設定_計算センター
			var kbn407		 = $.getInputboxValue($('#'+$.id_mei.kbn407));			// デフォルト設定_運用区分
			var kbn408		 = $.getInputboxValue($('#'+$.id_mei.kbn408));			// デフォルト設定_伝票区分
			var kbn409		 = $.getInputboxValue($('#'+$.id_mei.kbn409));			// デフォルト設定_集計表
			var kbn410		 = $.getInputboxValue($('#'+$.id_mei.kbn410));			// デフォルト設定_ピッキングデータ
			var kbn411		 = $.getInputboxValue($('#'+$.id_mei.kbn411));			// デフォルト設定_ピッキングリスト
			var kbn412		 = $.getInputboxValue($('#'+$.id_mei.kbn412+'_hptnd'));		// デフォルト設定_ワッペン
			var kbn413		 = $.getInputboxValue($('#'+$.id_mei.kbn413));			// デフォルト設定_一括伝票
			var kbn414		 = $.getInputboxValue($('#'+$.id_mei.kbn414));			// デフォルト設定_加工指示
			var kbn415		 = $.getInputboxValue($('#'+$.id_mei.kbn415));			// デフォルト設定_流通区分
			var kbn421		 = $.getInputboxValue($('#'+$.id_mei.kbn421));			// デフォルト設定_在庫内訳_集計表

			var kbn416		 = $.getInputboxValue($('#'+$.id_mei.kbn416));			// デフォルト設定_在庫内訳_伝票区分
			var kbn417		 = $.getInputboxValue($('#'+$.id_mei.kbn417));			// デフォルト設定_在庫内訳_集計表
			var kbn418		 = $.getInputboxValue($('#'+$.id_mei.kbn418));			// デフォルト設定_在庫内訳_ピッキングデータ
			var kbn419		 = $.getInputboxValue($('#'+$.id_mei.kbn419));			// デフォルト設定_在庫内訳_ピッキングリスト

			// 存在チェック：支払先マスタ
			if(sircd && sircd != ""){
				var param = {};
				param["KEY"] =  "MST_CNT";
				param["value"] = sircd;
				var chk_cnt = $.getInputboxData(that.name, $.id.action_check, 'MSTSIHARAI', [param]);
				if(chk_cnt==="" || chk_cnt==="0"){
					$.showMessage('E11087');
					return false;

				}
			}

			// チェック処理①
			// エラーチェック：いなげや在庫
			if(kbn404 ==='1'){
				if(kbn405 !== '1'){
					$.showMessage('E11051');
					return false;
				}
			}

			// チェック処理①
			// エラーチェック：計算センターOFF
			if(kbn406 ==='0'){
				if(kbn407 === '1'){
					// デフォルト設定_運用区分 設定あり
					$.showMessage('E11059');
					return false;
				}
				if(kbn408 === '1'){
					// デフォルト設定_伝票区分 設定あり
					$.showMessage('E11057');
					return false;
				}
				if(kbn409 === '1'){
					// デフォルト設定_集計表 設定あり
					$.showMessage('E11058');
					return false;
				}
				if(kbn410 === '1'){
					// デフォルト設定_ピッキングデータ 設定あり
					$.showMessage('E11060');
					return false;
				}
				if(kbn411 === '1'){
					// デフォルト設定_ピッキングリスト 設定あり
					$.showMessage('E11062');
					return false;
				}
				if(kbn412 === '1'){
					// デフォルト設定_ワッペン 設定あり
					$.showMessage('E11063');
					return false;
				}
				if(kbn413 === '1'){
					// デフォルト設定_一括伝票 設定あり
					$.showMessage('E11064');
					return false;
				}
				if(kbn414 === '1'){
					// デフォルト設定_加工指示 設定あり
					$.showMessage('E11065');
					return false;
				}
			}

			// チェック処理③
			// エラーチェック：計算センターOFF
			if(kbn406 ==='1' || kbn406 ==='7'){
				if(kbn408 == '0'
					&& kbn409 == '0'
					&& kbn410 == '0'
					&& kbn411 == '0'
					&& kbn412 == '0'
					&& kbn413 == '0'
					&& kbn414 == '0'){
					$.showMessage('E11056');
					return false;
				}
			}

			// チェック処理④
			// エラーチェック：デフォルト流通区分
			if(kbn415 !=='1'){
				if(kbn416 =='1'
					|| kbn417 =='1'
					|| kbn418 =='1'
					|| kbn419 =='1'
				){
					$.showMessage('EX1101');
					return false;
				}
			}

			// エラーチェック：代表仕入先
			//var param = $.getInputboxValue($('#'+$.id_inp.txt_htdt));
			if(dsircd || dsircd!==''){
				var msgid = that.checkInputboxFunc($.id_inp.txt_dsircd, dsircd , '');
				if(msgid !==null){
					$.showMessage(msgid,['代表仕入先コード','仕入先マスタ']);
					return false;
				}
			}

			// 配送パターン一覧
			var hsptns	= [];
			var hsptn	= '';
			var rsircd	= '';

			var targetRowsHsptn = $('#'+$.id.grd_hsptn+'_list').datagrid('getRows');
			for (var i=0; i<targetRowsHsptn.length; i++){
				if(targetRowsHsptn[i]["HSPTN"]){
					hsptn = targetRowsHsptn[i]["HSPTN"];
					hsptns.push(targetRowsHsptn[i]["HSPTN"]);

					// 存在チェック
					// 配送パターン
					if(hsptn || hsptn!==''){
						var msgid = that.checkInputboxFunc($.id_inp.txt_hsptn, hsptn , '');
						if(msgid !==null){
							$.showMessage(msgid,['【配送P】配送パターン','配送パターンマスタ']);
							return false;
						}
					}

					// 実仕入先コード
					rsircd = targetRowsHsptn[i]["RSIRCD"] ? targetRowsHsptn[i]["RSIRCD"] : '';
					if(rsircd || rsircd!==''){
						var msgid = that.checkInputboxFunc($.id_inp.txt_rsircd, rsircd , '');
						if(msgid !==null){
							$.showMessage(msgid,['【配送P】実仕入先コード','仕入先マスタ']);
							return false;
						}
					}

					// チェック処理①
					// エラーチェック：HP計算センターOFF
					//var testval = that.getCheckParam(targetRowsHsptn[i], "KEISANCENTER");

					if(that.getCheckParam(targetRowsHsptn[i], "KEISANCENTER") === '0'){

						if(that.getCheckParam(targetRowsHsptn[i], "UNYOKBN") === '1'){
							$.showMessage('E11068');
							return false;
						}
						if(that.getCheckParam(targetRowsHsptn[i], "DENPYOKBN") === '1'){
							$.showMessage('E11069');
							return false;
						}
						if(that.getCheckParam(targetRowsHsptn[i], "SHUKEIHYO1") === '1'){
							$.showMessage('E11070');
							return false;
						}
						if(that.getCheckParam(targetRowsHsptn[i], "PICKINGDATA") === '1'){
							$.showMessage('E11071');
							return false;
						}
						if(that.getCheckParam(targetRowsHsptn[i], "PICKINGLIST") === '1'){
							$.showMessage('E11072');
							return false;
						}
						if(that.getCheckParam(targetRowsHsptn[i], "WAPPEN") === '1'){
							$.showMessage('E11073');
							return false;
						}
						if(that.getCheckParam(targetRowsHsptn[i], "IKKATSUDENPYO") === '1'){
							$.showMessage('E11074');
							return false;
						}
						if(that.getCheckParam(targetRowsHsptn[i], "KAKOSHIJI") === '1'){
							$.showMessage('E11075');
							return false;
						}
					}

					// チェック処理②
					// エラーチェック：HP計算センターON
					if(that.getCheckParam(targetRowsHsptn[i],"KEISANCENTER") === '1'
						|| that.getCheckParam(targetRowsHsptn[i],"KEISANCENTER") === '7'){

						var updflg	= false;
						//var string = ['', '0'];

						if(that.getCheckParam(targetRowsHsptn[i],"DENPYOKBN") !== '0'){
							updflg = true;
						}
						if(that.getCheckParam(targetRowsHsptn[i], "SHUKEIHYO1") !== '0'){
							updflg = true;
						}
						if(that.getCheckParam(targetRowsHsptn[i],"PICKINGLIST") !== '0'){
							updflg = true;
						}
						if(that.getCheckParam(targetRowsHsptn[i],"PICKINGDATA") !== '0'){
							updflg = true;
						}
						if(that.getCheckParam(targetRowsHsptn[i],"WAPPEN") !== '0'){
							updflg = true;
						}
						if(that.getCheckParam(targetRowsHsptn[i],"IKKATSUDENPYO") !== '0'){
							updflg = true;
						}
						if(that.getCheckParam(targetRowsHsptn[i],"KAKOSHIJI") !== '0'){
							updflg = true;
						}

						if(!updflg){
							$.showMessage('E11067');
							return false;
						}
					}

					// チェック処理③
					// エラーチェック：HP流通区分
					if(that.getCheckParam(targetRowsHsptn[i],"RYUTSUKBN") !== '1'){
						if(that.getCheckParam(targetRowsHsptn[i],"ZAIKOCHIWAKE_DENPYOKBN") === '1'
							|| that.getCheckParam(targetRowsHsptn[i],"ZAIKOCHIWAKE_SHUKEIHYO") === '1'
							|| that.getCheckParam(targetRowsHsptn[i],"ZAIKOCHIWAKE_PICKINGDATA") === '1'
							|| that.getCheckParam(targetRowsHsptn[i],"ZAIKOCHIWAKE_PICKINGLIST") === '1' ){
							$.showMessage('E11076',undefined, function(){$.addErrState(that, $('#'+$.id.grd_hsptn+'_list'), true, {NO:i,
								ID:'grd_' + $.id_mei.kbn415 +
									',grd_' + $.id_mei.kbn416 +
									',grd_' + $.id_mei.kbn417 +
									',grd_' + $.id_mei.kbn418 +
									',grd_' + $.id_mei.kbn419
							})});
							return false;
						}
					}

					// エラーチェック：HP実仕入先
					if(targetRowsHsptn[i]["RSIRCD"]){
						if(targetRowsHsptn[i]["RSIRCD"] === sircd){
							$.showMessage('E11077');
							return false;
						}
					}
				}else{
					// キーコード未入力チェック
					if(targetRowsHsptn[i]["TEMBETSUDENPYOFLG"]				&& targetRowsHsptn[i]["TEMBETSUDENPYOFLG"].trim() !== ''
						|| targetRowsHsptn[i]["KEISANCENTER"]				&& targetRowsHsptn[i]["KEISANCENTER"].trim()!== ''
						|| targetRowsHsptn[i]["UNYOKBN"]					&& targetRowsHsptn[i]["UNYOKBN"].trim()!== ''
						|| targetRowsHsptn[i]["DENPYOKBN"]					&& targetRowsHsptn[i]["DENPYOKBN"].trim()!== ''
						|| targetRowsHsptn[i]["SHUKEIHYO1"]					&& targetRowsHsptn[i]["SHUKEIHYO1"].trim()!== ''
						|| targetRowsHsptn[i]["PICKINGDATA"]				&& targetRowsHsptn[i]["PICKINGDATA"].trim()!== ''
						|| targetRowsHsptn[i]["PICKINGLIST"]				&& targetRowsHsptn[i]["PICKINGLIST"].trim()!== ''
						|| targetRowsHsptn[i]["WAPPEN"]						&& targetRowsHsptn[i]["WAPPEN"].trim()!== ''
						|| targetRowsHsptn[i]["IKKATSUDENPYO"]				&& targetRowsHsptn[i]["IKKATSUDENPYO"].trim()!== ''
						|| targetRowsHsptn[i]["KAKOSHIJI"]					&& targetRowsHsptn[i]["KAKOSHIJI"].trim()!== ''
						|| targetRowsHsptn[i]["RYUTSUKBN"]					&& targetRowsHsptn[i]["RYUTSUKBN"].trim()!== ''
						|| targetRowsHsptn[i]["ZAIKOCHIWAKE_DENPYOKBN"]		&& targetRowsHsptn[i]["ZAIKOCHIWAKE_DENPYOKBN"].trim()!== ''
						|| targetRowsHsptn[i]["ZAIKOCHIWAKE_SHUKEIHYO"]		&& targetRowsHsptn[i]["ZAIKOCHIWAKE_SHUKEIHYO"].trim()!== ''
						|| targetRowsHsptn[i]["ZAIKOCHIWAKE_PICKINGDATA"]	&& targetRowsHsptn[i]["ZAIKOCHIWAKE_PICKINGDATA"].trim()!== ''
						|| targetRowsHsptn[i]["ZAIKOCHIWAKE_PICKINGLIST"]	&& targetRowsHsptn[i]["ZAIKOCHIWAKE_PICKINGLIST"].trim()!== ''
						|| targetRowsHsptn[i]["RSIRCD"]						&& targetRowsHsptn[i]["RSIRCD"].trim()!== ''
						|| targetRowsHsptn[i]["YOKOMOCHISAKI_KENSHUKBN"]	&& targetRowsHsptn[i]["YOKOMOCHISAKI_KENSHUKBN"].trim()!== ''
						|| targetRowsHsptn[i]["YOKOMOCHISAKI_DENPYOKBN"]	&& targetRowsHsptn[i]["YOKOMOCHISAKI_DENPYOKBN"].trim()!== ''
						|| targetRowsHsptn[i]["YOKOMOCHISAKI_SHUKEIHYO"]	&& targetRowsHsptn[i]["YOKOMOCHISAKI_SHUKEIHYO"].trim()!== ''

					){
						$.showMessage("EX1103", ['配送パターン']);
						return false;
					}
				}
			}




			// エリア別配送パターン一覧
			var ehsptns = [];				// 配送パターンタブとの整合性チェック用
			var ehsptnTengp	= [];			// 重複チェック用
			var rsircd	= '';

			var targetRowsEhsptn = $('#'+$.id.grd_ehsptn+'_list').datagrid('getRows');
			for (var i=0; i<targetRowsEhsptn.length; i++){
				if(targetRowsEhsptn[i]["HSPTN"]){
					ehsptns.push(targetRowsEhsptn[i]["HSPTN"]);
					ehsptnTengp.push(Number(targetRowsEhsptn[i]["HSPTN"]) + '' + Number(targetRowsEhsptn[i]["TENGPCD"]));
					var hsptn = targetRowsEhsptn[i]["HSPTN"]

					// 存在チェック
					if(hsptn || hsptn!==''){
						var msgid = that.checkInputboxFunc($.id_inp.txt_hsptn, hsptn , '');
						if(msgid !==null){
							$.showMessage(msgid,['【ｴﾘｱ別配送P】配送パターン','配送パターンマスタ']);
							return false;
						}
					}

					// 実仕入先コード
					rsircd = targetRowsEhsptn[i]["RSIRCD"] ? targetRowsEhsptn[i]["RSIRCD"] : '';
					if(rsircd || rsircd!==''){
						var msgid = that.checkInputboxFunc($.id_inp.txt_rsircd, rsircd , '');
						if(msgid !==null){
							$.showMessage(msgid,['【ｴﾘｱ別配送P】実仕入先コード','仕入先マスタ']);
							return false;
						}
					}

					// キー項目入力チェック
					if(!targetRowsEhsptn[i]["TENGPCD"] || targetRowsEhsptn[i]["TENGPCD"] == ''){
						$.showMessage('EX1103',['店グループコード']);
						return false;
					}

					// チェック処理①
					// エラーチェック：HP計算センターOFF
					//var testval = that.getCheckParam(targetRowsEhsptn[i], "KEISANCENTER");

					if(that.getCheckParam(targetRowsEhsptn[i], "KEISANCENTER", false) === '0'){

						if(that.getCheckParam(targetRowsEhsptn[i], "UNYOKBN", false) === '1'){
							$.showMessage('EX1105');
							return false;
						}
						if(that.getCheckParam(targetRowsEhsptn[i], "DENPYOKBN", false) === '1'){
							$.showMessage('EX1106');
							return false;
						}
						if(that.getCheckParam(targetRowsEhsptn[i], "SHUKEIHYO1", false) === '1'){
							$.showMessage('EX1107');
							return false;
						}
						if(that.getCheckParam(targetRowsEhsptn[i], "PICKINGDATA", false) === '1'){
							$.showMessage('EX1108');
							return false;
						}
						if(that.getCheckParam(targetRowsEhsptn[i], "PICKINGLIST", false) === '1'){
							$.showMessage('EX1109');
							return false;
						}
						if(that.getCheckParam(targetRowsEhsptn[i], "WAPPEN", false) === '1'){
							$.showMessage('EX1110');
							return false;
						}
						if(that.getCheckParam(targetRowsEhsptn[i], "IKKATSUDENPYO", false) === '1'){
							$.showMessage('EX1111');
							return false;
						}
						if(that.getCheckParam(targetRowsEhsptn[i], "KAKOSHIJI", false) === '1'){
							$.showMessage('EX1112');
							return false;
						}
					}

					// チェック処理②
					// エラーチェック：HP計算センターON
					if(that.getCheckParam(targetRowsEhsptn[i],"KEISANCENTER", false) === '1'
						|| that.getCheckParam(targetRowsEhsptn[i],"KEISANCENTER", false) === '7'){

						var updflg	= false;
						//var string = ['', '0'];

						if(that.getCheckParam(targetRowsEhsptn[i],"DENPYOKBN", false) !== '0'){
							updflg = true;
						}
						if(that.getCheckParam(targetRowsEhsptn[i],"PICKINGLIST", false) !== '0'){
							updflg = true;
						}
						if(that.getCheckParam(targetRowsEhsptn[i],"PICKINGDATA", false) !== '0'){
							updflg = true;
						}
						if(that.getCheckParam(targetRowsEhsptn[i],"WAPPEN", false) !== '0'){
							updflg = true;
						}
						if(that.getCheckParam(targetRowsEhsptn[i],"IKKATSUDENPYO", false) !== '0'){
							updflg = true;
						}
						if(that.getCheckParam(targetRowsEhsptn[i],"KAKOSHIJI", false) !== '0'){
							updflg = true;
						}

						if(!updflg){
							$.showMessage('EX1104');
							return false;
						}
					}

					// チェック処理③
					// エラーチェック：HP流通区分
					if(that.getCheckParam(targetRowsEhsptn[i],"RYUTSUKBN", false) !== '1'){
						if(that.getCheckParam(targetRowsEhsptn[i],"ZAIKOCHIWAKE_DENPYOKBN", false) === '1'
							|| that.getCheckParam(targetRowsEhsptn[i],"ZAIKOCHIWAKE_SHUKEIHYO", false) === '1'
							|| that.getCheckParam(targetRowsEhsptn[i],"ZAIKOCHIWAKE_PICKINGDATA", false) === '1'
							|| that.getCheckParam(targetRowsEhsptn[i],"ZAIKOCHIWAKE_PICKINGLIST", false) === '1' ){
							$.showMessage('EX1116',undefined, function(){$.addErrState(that, $('#'+$.id.grd_ehsptn+'_list'), true, {NO:i,
								ID:'grd_' + $.id_mei.kbn415 +
									',grd_' + $.id_mei.kbn416 +
									',grd_' + $.id_mei.kbn417 +
									',grd_' + $.id_mei.kbn418 +
									',grd_' + $.id_mei.kbn419
							})});
							return false;

						}
					}
				}else{
					// キーコード未入力チェック
					if(targetRowsEhsptn[i]["TEMBETSUDENPYOFLG"]				&& targetRowsEhsptn[i]["TEMBETSUDENPYOFLG"].trim() !== ''
						|| targetRowsEhsptn[i]["KEISANCENTER"]				&& targetRowsEhsptn[i]["KEISANCENTER"].trim()!== ''
						|| targetRowsEhsptn[i]["UNYOKBN"]					&& targetRowsEhsptn[i]["UNYOKBN"].trim()!== ''
						|| targetRowsEhsptn[i]["DENPYOKBN"]					&& targetRowsEhsptn[i]["DENPYOKBN"].trim()!== ''
						|| targetRowsEhsptn[i]["SHUKEIHYO1"]				&& targetRowsEhsptn[i]["SHUKEIHYO1"].trim()!== ''
						|| targetRowsEhsptn[i]["PICKINGDATA"]				&& targetRowsEhsptn[i]["PICKINGDATA"].trim()!== ''
						|| targetRowsEhsptn[i]["PICKINGLIST"]				&& targetRowsEhsptn[i]["PICKINGLIST"].trim()!== ''
						|| targetRowsEhsptn[i]["WAPPEN"]					&& targetRowsEhsptn[i]["WAPPEN"].trim()!== ''
						|| targetRowsEhsptn[i]["IKKATSUDENPYO"]				&& targetRowsEhsptn[i]["IKKATSUDENPYO"].trim()!== ''
						|| targetRowsEhsptn[i]["KAKOSHIJI"]					&& targetRowsEhsptn[i]["KAKOSHIJI"].trim()!== ''
						|| targetRowsEhsptn[i]["RYUTSUKBN"]					&& targetRowsEhsptn[i]["RYUTSUKBN"].trim()!== ''
						|| targetRowsEhsptn[i]["ZAIKOCHIWAKE_DENPYOKBN"]	&& targetRowsEhsptn[i]["ZAIKOCHIWAKE_DENPYOKBN"].trim()!== ''
						|| targetRowsEhsptn[i]["ZAIKOCHIWAKE_SHUKEIHYO"]	&& targetRowsEhsptn[i]["ZAIKOCHIWAKE_SHUKEIHYO"].trim()!== ''
						|| targetRowsEhsptn[i]["ZAIKOCHIWAKE_PICKINGDATA"]	&& targetRowsEhsptn[i]["ZAIKOCHIWAKE_PICKINGDATA"].trim()!== ''
						|| targetRowsEhsptn[i]["ZAIKOCHIWAKE_PICKINGLIST"]	&& targetRowsEhsptn[i]["ZAIKOCHIWAKE_PICKINGLIST"].trim()!== ''
						|| targetRowsEhsptn[i]["RSIRCD"]					&& targetRowsEhsptn[i]["RSIRCD"].trim()!== ''
						|| targetRowsEhsptn[i]["YOKOMOCHISAKI_KENSHUKBN"]	&& targetRowsEhsptn[i]["YOKOMOCHISAKI_KENSHUKBN"].trim()!== ''
						|| targetRowsEhsptn[i]["YOKOMOCHISAKI_DENPYOKBN"]	&& targetRowsEhsptn[i]["YOKOMOCHISAKI_DENPYOKBN"].trim()!== ''
						|| targetRowsEhsptn[i]["YOKOMOCHISAKI_SHUKEIHYO"]	&& targetRowsEhsptn[i]["YOKOMOCHISAKI_SHUKEIHYO"].trim()!== ''
						|| targetRowsEhsptn[i]["TENGPCD"]					&& targetRowsEhsptn[i]["TENGPCD"].trim()!== ''

					){
						$.showMessage("EX1103", ['配送パターン']);
						return false;
					}
				}

				// エラーチェック：EHP計算センター
				/*if(targetRowsEhsptn[i]["KEISANCENTER"] === '1' || targetRowsEhsptn[i]["KEISANCENTER"] === '7'){
					if(targetRowsEhsptn[i]["DENPYOKBN"] === '0'
							&& targetRowsEhsptn[i]["PICKINGDATA"]	 === '0'
							&& targetRowsEhsptn[i]["PICKINGLIST"]	 === '0'
							&& targetRowsEhsptn[i]["WAPPEN"]		 === '0'
							&& targetRowsEhsptn[i]["IKKATSUDENPYO"]	 === '0'
							&& targetRowsEhsptn[i]["KAKOSHIJI"]		 === '0' ){
						$.showMessage('EX1102');
						return false;
					}
				}
				if(targetRowsEhsptn[i]["KEISANCENTER"] === '0'){

					if(targetRowsEhsptn[i]["UNYOKBN"]		 === '1'){
						$.showMessage('E11068');
						return false;
					}
					if(targetRowsEhsptn[i]["DENPYOKBN"]		 === '1'){
						$.showMessage('E11069');
						return false;
					}
					if(targetRowsEhsptn[i]["SHUKEIHYO1"]	 === '1'){
						$.showMessage('E11070');
						return false;
					}
					if(targetRowsEhsptn[i]["PICKINGDATA"]	 === '1'){
						$.showMessage('E11071');
						return false;
					}
					if(targetRowsEhsptn[i]["PICKINGLIST"]	 === '1'){
						$.showMessage('E11072');
						return false;
					}
					if(targetRowsEhsptn[i]["WAPPEN"]		 === '1'){
						$.showMessage('E11073');
						return false;
					}
					if(targetRowsEhsptn[i]["IKKATSUDENPYO"]	 === '1'){
						$.showMessage('E11074');
						return false;
					}
					if(targetRowsEhsptn[i]["KAKOSHIJI"]		 === '1'){
						$.showMessage('E11075');
						return false;
					}
				}

				// エラーチェック：EHP流通区分
				if(targetRowsHsptn[i]["RYUTSUKBN"] == '1'){
					if(targetRowsHsptn[i]["DENPYOKBN"]			 === '1'
						|| targetRowsHsptn[i]["SHUKEIHYO1"]		 === '1'
						|| targetRowsHsptn[i]["PICKINGDATA"]	 === '1'
						|| targetRowsHsptn[i]["PICKINGLIST"]	 === '1' ){
						$.showMessage('E11061');
						return false;

					}
				}*/

				// エラーチェック：EHP実仕入先
				if(targetRowsEhsptn[i]["RSIRCD"]){
					if(targetRowsEhsptn[i]["RSIRCD"] === sircd){
						$.showMessage('EX1114');
						return false;
					}
				}
			}

			// エリア別配送パターングリッドに入力された配送パターンが、配送パターングリッドが入力されているかチェック
			for (var i=0; i<ehsptns.length; i++){
				if($.inArray(ehsptns[i], hsptns)===-1){
					//alert($.inArray(ehsptns, hsptns[i]))
					$.showMessage('E11084');
					return false;
				}
			}

			// 重複チェック：配送パターン
			var hsptn = hsptns.filter(function (element, index, self) { return self.indexOf(element)*1 === index; });
			if(hsptns.length !== hsptn.length){
				$.showMessage('E11082');
				return false;
			}

			// 重複チェック：エリア別配送パターン
			var ehsptn = ehsptnTengp.filter(function (element, index, self) { return self.indexOf(element)*1 === index; });
			if(ehsptnTengp.length !== ehsptn.length){
				$.showMessage('E11083');
				return false;
			}

			// 入力エラーなしの場合に検索条件を格納
			//if (rt == true) that.jsonString = that.jsonTemp.slice(0);

			// 子テーブル情報を変数に格納
			//if (rt == true) that.setGridData(gridData);

			// 入力チェック用の配列をクリア
			that.jsonTemp = [];

			return rt;
		},
		updSuccess: function(id){	// validation OK時 の update処理
			var that = this;

			// 変更行情報取得
			var changedIndex = $($.id.hiddenChangedIdx).val().split(",");

			var txt_sircd		 = $('#'+$.id_inp.txt_sircd).numberbox('getValue');			// 入力仕入先コード

			var targetDatas = that.getGridData('',"data");									// 変更データ

			// 入力データ：配送パターン
			var targetRowsHsptn = that.getGridData(txt_sircd, $.id.grd_hsptn+'_list');

			// 入力データ：エリア別配送パターン
			var targetRowsEhsptn = that.getGridData(txt_sircd, $.id.grd_ehsptn+'_list');


			var targetRowsHsptn_del = that.getMergeGridDate(txt_sircd, $.id.grd_hsptn + '_list', 'del');
			var targetRowsEhsptn_del = that.getMergeGridDate(txt_sircd, $.id.grd_ehsptn + '_list', 'del');


			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$.appendMaskMsg();

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,							// レポート名
					action:			$.id.action_update,					// 実行処理情報
					obj:			id,									// 実行オブジェクト
					SENDBTNID:		that.sendBtnid,
					SIRCD:			txt_sircd,
//					IDX:			$($.id.hiddenChangedIdx).val(),		// 更新対象Index
//					DATA:			JSON.stringify(targetRows),			// 更新対象情報
					DATA:			JSON.stringify(targetDatas),		// 更新対象情報
					DATA_HSPTN:		JSON.stringify(targetRowsHsptn),	// 個別データグリッド:配送パターン
					DATA_EHSPTN:	JSON.stringify(targetRowsEhsptn),	// 個別データグリッド:エリア別配送パターン
					DATA_HSPTN_DEL:	JSON.stringify(targetRowsHsptn_del),
					DATA_EHSPTN_DEL:JSON.stringify(targetRowsEhsptn_del),

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
			// 仕入先コード
			this.jsonTemp.push({
				id:		$.id_inp.txt_sircd,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_sircd),
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
			TEMBETSUDENPYOFLG			: "grd_sel_tembetsudenpyoflg"				// 最低発注数
			,KEISANCENTER				: "grd_sel_keisancenter"					// 通常原価
			,UNYOKBN					: "grd_sel_unyokbn"							// 原価
			,DENPYOKBN					: "grd_sel_denpyokbn"						// A総売価
			,SHUKEIHYO1					: "grd_sel_shukeihyo1"						// A本体売価
			,PICKINGDATA				: "grd_sel_pickingdata"						// Aランク
			,PICKINGLIST				: "grd_sel_pickinglist"						// A値入率
			,WAPPEN						: "grd_sel_wappen"							// B総売価
			,IKKATSUDENPYO				: "grd_sel_ikkatsudenpyo"					// B本体売価
			,KAKOSHIJI					: "grd_sel_kakoshiji"						// Bランク
			,RYUTSUKBN					: "grd_sel_ryutsukbn"						// B値入率
			,ZAIKOCHIWAKE_DENPYOKBN		: "grd_sel_zaikochiwake_denpyokbn"			// C総売価
			,ZAIKOCHIWAKE_SHUKEIHYO		: "grd_sel_zaikochiwake_shukeihyo"			// C本体売価
			,ZAIKOCHIWAKE_PICKINGDATA	: "grd_sel_zaikochiwake_pickingdata"		// Cランク
			,ZAIKOCHIWAKE_PICKINGLIST	: "grd_sel_zaikochiwake_pickinglist"		// C値入率
			,YOKOMOCHISAKI_KENSHUKBN	: "grd_sel_yokomochisaki_kenshukbn"			// 枚数
			,YOKOMOCHISAKI_DENPYOKBN	: "grd_sel_yokomochisaki_denpyokbn"			// 売価
			,YOKOMOCHISAKI_SHUKEIHYO	: "grd_sel_yokomochisaki_shukeihyo"			// 売価
		},
		setEditableGrid: function(that, reportno, id){		// データ表示
			var index = -1;

			var init = true;
			var init = true;
			// ページサイズ定義取得
			var pageList = $.fn.pagination.defaults.pageList;
			//var pageSize = $.getJSONValue(this.jsonHidden, $.id.pageSize);
			var pageSize = 30;
			if (pageSize==="") pageSize=$.fn.pagination.defaults.pageSize;
			pageSize = $.getDefaultPageSize(pageSize, pageList);

			var columns = that.getGridColumns(that, id);
			var fcolumns = that.getGridFcolumns(that, id);
			var funcClickRow = $.fn.datagrid.defaults.onClickRow;
			var funcBeginEdit= $.fn.datagrid.defaults.onBeginEdit;
			var funcEndEdit= $.fn.datagrid.defaults.onEndEdit;
			var funcAfterEdit= $.fn.datagrid.defaults.onAfterEdit;

			var formatterLPad = function(value){return $.getFormatLPad(value, 6);};

			if(that.reportYobiInfo()!=='1'){
				that.editRowIndex[id] = -1;
				funcClickRow = function(index,field){$.clickEditableDatagridCell(that,id, index)};
				funcBeginEdit = function(index,row){

					var list = []
					list = that.initedObject.filter(function(item, index){
						if(item.indexOf('grd_sel_') >= 0){
							return item;
						}
					});
					list.filter(function(item, index){
						var id = item + '_'
						if($('#' + id)){

							// 初期化処理
							$('#'+id).combobox('setText', ' ');
							var data = $('#'+id).combobox('getData');

							var val = -1;
							var initvalue = -1

							// 初期値取得
							var field = item.replace("grd_sel_", "").toUpperCase();
							if(row[field] && row[field].trim() != ""){
								initvalue = row[field]
								if(initvalue != -1){
									for (var i=0; i<data.length; i++){
										if (data[i].TEXT == initvalue){
											// 初期値適用
											val = data[i].VALUE;
											break;
										}
									}
								}
							}

							if (val){
								$('#'+id).combobox('setValue', val);
							}

							// フォーカスアウトのタイミングの動作
							// グリッド内のコンボグリッドでは、参照オブジェクトからonLoadSuccessのイベントを継承していない為、
							// reloadによる再読み込み処理を追加する必要がある。
							$($('#'+id)).next().on('focusout', function(e){
								var obj = $(this).prev();
								if (!$.setComboReload(obj,false)) {
									var val = -1;
									if (val){
										var data = obj.combobox('getData');
										var val = null;
										if (that.initedObject && $.inArray(id, that.initedObject) < 0){
											var init = $.getJSONValue(that.jsonHidden, id);
											for (var i=0; i<data.length; i++){
												if (data[i].VALUE == init){
													val = init;
													break;
												}
											}
										}
										obj.combobox('setValue', val);
									}
								}
							});

							$($('#'+id)).on('focus', function(e) {
								alert();
								var obj = $(this).prev();
								obj.combobox('setValue', 0);
							});
						}
					});
					$.beginEditDatagridRow(that,id, index, row)
				};
				funcEndEdit = function(index,row,changes){$.endEditDatagridRow(that, id, index, row);};
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
				url:$.reg.easy,
				frozenColumns:[[]],
				//view:scrollview,
				//pageSize:pageSize,
				//pageList:pageList,
				frozenColumns:fcolumns,
				columns:columns,
				//fitColumns:false,	// 指定カラム幅を適用する場合、false 指定。
				onBeforeLoad:function(param){
					index = -1;
					var values = {};
					var sircd	 = $.getJSONValue(that.jsonHidden, $.id_inp.txt_sircd);	// 仕入先コード
					//var sircd	 = $('#'+$.id_inp.txt_sircd).numberbox('getValue');		// 企画No

					values["callpage"]	 = $($.id.hidden_reportno).val()				// 呼出元レポート名
					values["SIRCD"]		 = sircd										// 企画No
					//var json = that.getGridParams(that, id);

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
					if(index===-1){
						index=1;
						// 情報保持
						var sircd	 = $.getJSONValue(that.jsonHidden, $.id_inp.txt_sircd);	// 仕入先コード
						var gridData = that.getGridData(sircd, id);
						that.setGridData(gridData, id);
					}
				},
				onClickCell:function(rowIndex, field, value){
					// 列名保持
					that.columnName = field;
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
			var formatterLPad = function(value){
				return $.getFormatLPad(value, 6);
			};
			columnBottom.push({field:'TEMBETSUDENPYOFLG'		,title:'店別伝票フラグ'				,width:100 ,halign:'center',editor:{type:'combobox',options:{panelWidth:1200}}});
			columnBottom.push({field:'KEISANCENTER'				,title:'計算センター'				,width:100 ,halign:'center',editor:'combobox'});
			columnBottom.push({field:'UNYOKBN'					,title:'運用区分'					,width:100 ,halign:'center',editor:'combobox'});
			columnBottom.push({field:'DENPYOKBN'				,title:'伝票区分'					,width:100 ,halign:'center',editor:'combobox'});
			columnBottom.push({field:'SHUKEIHYO1'				,title:'集計表'						,width:100 ,halign:'center',editor:'combobox'});
			columnBottom.push({field:'PICKINGDATA'				,title:'ピッキングデータ'			,width:110 ,halign:'center',editor:'combobox'});
			columnBottom.push({field:'PICKINGLIST'				,title:'ピッキングリスト'			,width:110 ,halign:'center',editor:'combobox'});
			columnBottom.push({field:'WAPPEN'					,title:'ワッペン'					,width:100 ,halign:'center',editor:'combobox'});
			columnBottom.push({field:'IKKATSUDENPYO'			,title:'一括伝票'					,width:100 ,halign:'center',editor:'combobox'});
			columnBottom.push({field:'KAKOSHIJI'				,title:'加工指示'					,width:100 ,halign:'center',editor:'combobox'});
			columnBottom.push({field:'RYUTSUKBN'				,title:'流通区分'					,width:100 ,halign:'center',editor:'combobox'});
			columnBottom.push({field:'ZAIKOCHIWAKE_DENPYOKBN'	,title:'在庫内訳_伝票区分'			,width:100 ,halign:'center',editor:'combobox'});
			columnBottom.push({field:'ZAIKOCHIWAKE_SHUKEIHYO'	,title:'在庫内訳_集計表'			,width:100 ,halign:'center',editor:'combobox'});
			columnBottom.push({field:'ZAIKOCHIWAKE_PICKINGDATA'	,title:'在庫内訳_ピッキングデータ'	,width:100 ,halign:'center',editor:'combobox'});
			columnBottom.push({field:'ZAIKOCHIWAKE_PICKINGLIST'	,title:'在庫内訳_ピッキングリスト'	,width:100 ,halign:'center',editor:'combobox'});
			columnBottom.push({field:'RSIRCD'					,title:'実仕入先コード'				,width:60  ,halign:'center',editor:'numberbox',formatter:formatterLPad});
			columnBottom.push({field:'SIRKN_R'					,title:'実仕入先名称'				,width:300 ,halign:'center',editor:{type:'textbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}});
			columnBottom.push({field:'YOKOMOCHISAKI_KENSHUKBN'	,title:'横持先_検収区分'			,width:100 ,halign:'center',editor:'combobox'});
			columnBottom.push({field:'YOKOMOCHISAKI_DENPYOKBN'	,title:'横持先_伝票区分'			,width:100 ,halign:'center',editor:'combobox'});
			columnBottom.push({field:'YOKOMOCHISAKI_SHUKEIHYO'	,title:'横持先_集計表'				,width:100 ,halign:'center',editor:'combobox'});
			columns.push(columnBottom);
			return columns;
		},
		getGridFcolumns:function(that, id){
			var columns = [];
			var columnBottom=[];

			var cstyler =function(value,row,index){return 'color: red;font-weight: bold;';};
			var cformatter =function(value,row,index){return value && value===$.id.value_on?$.id.text_on:$.id.text_off;};
			var pomptFormatter =function(value){return $.getFormatPrompt(value, '####-####');};
			var formatterLPad = function(value){
				return $.getFormatLPad(value, 6);
			};

			columnBottom.push({field:'HSPTN'					,title:'配送パターン'				,width:90  ,halign:'center',editor:'numberbox',formatter:function(value){var targetId = $.id_inp.txt_hsptn;var check = $('#'+targetId).attr('check') ? JSON.parse('{'+$('#'+targetId).attr('check')+'}'): JSON.parse('{}');return $.getFormatLPad(value, check.maxlen);}});
			columnBottom.push({field:'HSPTNKN'					,title:'配送パターン名称'			,width:200 ,halign:'center',editor:{type:'textbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}});
			columnBottom.push({field:'CENTERCD'					,title:'センターコード'				,width:100 ,halign:'center',editor:{type:'textbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}});
			columnBottom.push({field:'YCENTERCD'				,title:'横持先コード'				,width:100 ,halign:'center',editor:{type:'textbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}});
			if(id === $.id.grd_ehsptn+'_list'){
				columnBottom.push({field:'TENGPCD'					,title:'店グループ'					,width:100 ,halign:'center',editor:'numberbox',formatter:function(value){return $.getFormatLPad(value, 4);}});
				columnBottom.push({field:'TENGPKN'					,title:'店グループ名称'				,width:200 ,halign:'center',editor:{type:'textbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}});
			}
			columns.push(columnBottom);
			return columns;
		},
		getGridData: function (txt_sircd, target){
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

				for (var i=0; i<targetDatas.length; i++){
					var rowDate = {
							F1	 : targetDatas[i]["F1"],			// 仕入先コード
							F2	 : targetDatas[i]["F3"],			// 仕入先名（カナ
							F3	 : targetDatas[i]["F2"],			// 仕入先名（漢字）
							F4	 : targetDatas[i]["F6"],			// 住所_都道府県（漢字）
							F5	 : targetDatas[i]["F7"],			// 住所_市区町村（漢字）
							F6	 : targetDatas[i]["F8"],			// 住所_町字（漢字）
							F7	 : targetDatas[i]["F9"],			// 住所_番地（漢字）
							F8	 : targetDatas[i]["F10"],			// 部署名（漢字
							F9	 : targetDatas[i]["F4"],			// 郵便番号_上桁
							F10	 : targetDatas[i]["F5"],			// 郵便番号_下桁
							F11	 : targetDatas[i]["F11"],			// 電話番号
							F12	 : targetDatas[i]["F12"],			// 内線番号
							F13	 : targetDatas[i]["F13"],			// FAX番号
							F14	 : targetDatas[i]["F22"],			// 代表仕入先コード
							F15	 : '',								// 伝送先親仕入先コード
							F16	 : targetDatas[i]["F14"],			// 開始日
							F17	 : targetDatas[i]["F17"],			// EDI受信
							F18	 : '',								// EDI送信
							F19	 : targetDatas[i]["F15"],			// 仕入先用途
							F20	 : targetDatas[i]["F16"],			// いなげや在庫
							F21	 : targetDatas[i]["F18"],			// 買掛区分
							F22	 : targetDatas[i]["F20"],			// 納税者番号
							F23	 : targetDatas[i]["F19"],			// 処理単価
							F24	 : '',								// 基本料金
							F25	 : targetDatas[i]["F29"],			// 取引停止
							F26	 : '',								// 同報配信先コード
							F27	 : targetDatas[i]["F24"],			// 同報配信先_伝票区分
							F28	 : targetDatas[i]["F25"],			// 同報配信先_集計表
							F29	 : targetDatas[i]["F26"],			// 同報配信先_ワッペン
							F30	 : targetDatas[i]["F21"],			// デフォルト一括区分
							F31	 : targetDatas[i]["F32"],			// デフォルト設定_店別伝票フラグ
							F32	 : targetDatas[i]["F30"],			// デフォルト設定_計算センター
							F33	 : targetDatas[i]["F34"],			// デフォルト設定_運用区分
							F34	 : targetDatas[i]["F31"],			// デフォルト設定_伝票区分
							F35	 : targetDatas[i]["F33"],			// デフォルト設定_集計表
							F36	 : targetDatas[i]["F35"],			// デフォルト設定_ピッキングデータ
							F37	 : targetDatas[i]["F37"],			// デフォルト設定_ピッキングリスト
							F38	 : targetDatas[i]["F38"],			// デフォルト設定_ワッペン
							F39	 : targetDatas[i]["F39"],			// デフォルト設定_一括伝票
							F40	 : targetDatas[i]["F40"],			// デフォルト設定_加工指示
							F41	 : targetDatas[i]["F36"],			// デフォルト設定_流通区分
							F42	 : targetDatas[i]["F43"],			// 在庫内訳_伝票区分
							F43	 : targetDatas[i]["F44"],			// 在庫内訳_集計表
							F44	 : targetDatas[i]["F45"],			// 在庫内訳_ピッキングデータ
							F45	 : targetDatas[i]["F46"],			// 在庫内訳_ピッキングリスト
							F46	 : targetDatas[i]["F41"],			// 実仕入先コード
							F47	 : targetDatas[i]["F47"],			// 横持先センター_検収区分
							F48	 : targetDatas[i]["F48"],			// 横持先センター_伝票区分
							F49	 : targetDatas[i]["F49"],			// 横持先センター_集計表
							F50	 : targetDatas[i]["F27"],			// BMS対象区分
							F51	 : targetDatas[i]["F28"],			// 自動検収区分
					};
					if(rowDate){
						targetRows.push(rowDate);
					}
				}
				data["data"] = targetRows;
			}

			// 配送パターン
			if(target===undefined || target===$.id.grd_hsptn+'_list'){
				var rowsHsptn= $('#'+$.id.grd_hsptn+'_list').datagrid('getRows');

				var txt_sircd		 = $('#'+$.id_inp.txt_sircd).numberbox('getValue');					// 入力仕入先コード

				for (var i=0; i<rowsHsptn.length; i++){
					if(rowsHsptn[i]["HSPTN"] == "" || rowsHsptn[i]["HSPTN"] == null ){

					}else{
						var rowDate = {
							F1	 : 	txt_sircd,
							F2	 : rowsHsptn[i]["HSPTN"],
							F3	 : rowsHsptn[i]["TEMBETSUDENPYOFLG"]		 ? rowsHsptn[i]["TEMBETSUDENPYOFLG"].trim().split("-")[0]:"",
							F4	 : rowsHsptn[i]["KEISANCENTER"]				 ? rowsHsptn[i]["KEISANCENTER"].trim().split("-")[0]:"",
							F5	 : rowsHsptn[i]["UNYOKBN"]					 ? rowsHsptn[i]["UNYOKBN"].trim().split("-")[0]:"",
							F6	 : rowsHsptn[i]["DENPYOKBN"]				 ? rowsHsptn[i]["DENPYOKBN"].trim().split("-")[0]:"",
							F7	 : rowsHsptn[i]["SHUKEIHYO1"]				 ? rowsHsptn[i]["SHUKEIHYO1"].trim().split("-")[0]:"",
							F8	 : rowsHsptn[i]["PICKINGDATA"]				 ? rowsHsptn[i]["PICKINGDATA"].trim().split("-")[0]:"",
							F9	 : rowsHsptn[i]["PICKINGLIST"]				 ? rowsHsptn[i]["PICKINGLIST"].trim().split("-")[0]:"",
							F10	 : rowsHsptn[i]["WAPPEN"] 					 ? rowsHsptn[i]["WAPPEN"].trim().split("-")[0]:"",
							F11	 : rowsHsptn[i]["IKKATSUDENPYO"]			 ? rowsHsptn[i]["IKKATSUDENPYO"].trim().split("-")[0]:"",
							F12	 : rowsHsptn[i]["KAKOSHIJI"]				 ? rowsHsptn[i]["KAKOSHIJI"].trim().split("-")[0]:"",
							F13	 : rowsHsptn[i]["RYUTSUKBN"]				 ? rowsHsptn[i]["RYUTSUKBN"].trim().split("-")[0]:"",
							F14	 : rowsHsptn[i]["ZAIKOCHIWAKE_DENPYOKBN"]	 ? rowsHsptn[i]["ZAIKOCHIWAKE_DENPYOKBN"].trim().split("-")[0]:"",
							F15	 : rowsHsptn[i]["ZAIKOCHIWAKE_SHUKEIHYO"]	 ? rowsHsptn[i]["ZAIKOCHIWAKE_SHUKEIHYO"].trim().split("-")[0]:"",
							F16	 : rowsHsptn[i]["ZAIKOCHIWAKE_PICKINGDATA"]	 ? rowsHsptn[i]["ZAIKOCHIWAKE_PICKINGDATA"].trim().split("-")[0]:"",
							F17	 : rowsHsptn[i]["ZAIKOCHIWAKE_PICKINGLIST"]	 ? rowsHsptn[i]["ZAIKOCHIWAKE_PICKINGLIST"].trim().split("-")[0]:"",
							F18	 : rowsHsptn[i]["RSIRCD"] ? ("000000"+rowsHsptn[i]["RSIRCD"]).slice( -6 ) : "      ",
							F19	 : rowsHsptn[i]["YOKOMOCHISAKI_KENSHUKBN"]	 ? rowsHsptn[i]["YOKOMOCHISAKI_KENSHUKBN"].trim().split("-")[0]:"",
							F20	 : rowsHsptn[i]["YOKOMOCHISAKI_DENPYOKBN"]	 ? rowsHsptn[i]["YOKOMOCHISAKI_DENPYOKBN"].trim().split("-")[0]:"",
							F21	 : rowsHsptn[i]["YOKOMOCHISAKI_SHUKEIHYO"]	 ? rowsHsptn[i]["YOKOMOCHISAKI_SHUKEIHYO"].trim().split("-")[0]:"",
						};
						targetRows.push(rowDate);
					}
				}
			}

			// エリア配送パターン
			if(target===undefined || target===$.id.grd_ehsptn+'_list'){

				var txt_sircd		 = $('#'+$.id_inp.txt_sircd).numberbox('getValue');					// 入力仕入先コード

				var rowsEhsptn= $('#'+$.id.grd_ehsptn+'_list').datagrid('getRows');
				for (var i=0; i<rowsEhsptn.length; i++){
					if(rowsEhsptn[i]["HSPTN"] == "" || rowsEhsptn[i]["HSPTN"] == null ){

					}else{
						var rowDate = {
								F1	 : txt_sircd,
								F2	 : rowsEhsptn[i]["HSPTN"],
								F3	 : rowsEhsptn[i]["TENGPCD"],
								F4	 : rowsEhsptn[i]["TEMBETSUDENPYOFLG"]		 ? rowsEhsptn[i]["TEMBETSUDENPYOFLG"].trim().split("-")[0]:"",
								F5	 : rowsEhsptn[i]["KEISANCENTER"]			 ? rowsEhsptn[i]["KEISANCENTER"].trim().split("-")[0]:"",
								F6	 : rowsEhsptn[i]["UNYOKBN"]					 ? rowsEhsptn[i]["UNYOKBN"].trim().split("-")[0]:"",
								F7	 : rowsEhsptn[i]["DENPYOKBN"]				 ? rowsEhsptn[i]["DENPYOKBN"].trim().split("-")[0]:"",
								F8	 : rowsEhsptn[i]["SHUKEIHYO1"]				 ? rowsEhsptn[i]["SHUKEIHYO1"].trim().split("-")[0]:"",
								F9	 : rowsEhsptn[i]["PICKINGDATA"]				 ? rowsEhsptn[i]["PICKINGDATA"].trim().split("-")[0]:"",
								F10	 : rowsEhsptn[i]["PICKINGLIST"]				 ? rowsEhsptn[i]["PICKINGLIST"].trim().split("-")[0]:"",
								F11	 : rowsEhsptn[i]["WAPPEN"] 					 ? rowsEhsptn[i]["WAPPEN"].trim().split("-")[0]:"",
								F12	 : rowsEhsptn[i]["IKKATSUDENPYO"]			 ? rowsEhsptn[i]["IKKATSUDENPYO"].trim().split("-")[0]:"",
								F13	 : rowsEhsptn[i]["KAKOSHIJI"]				 ? rowsEhsptn[i]["KAKOSHIJI"].trim().split("-")[0]:"",
								F14	 : rowsEhsptn[i]["RYUTSUKBN"]				 ? rowsEhsptn[i]["RYUTSUKBN"].trim().split("-")[0]:"",
								F15	 : rowsEhsptn[i]["ZAIKOCHIWAKE_DENPYOKBN"]	 ? rowsEhsptn[i]["ZAIKOCHIWAKE_DENPYOKBN"].trim().split("-")[0]:"",
								F16	 : rowsEhsptn[i]["ZAIKOCHIWAKE_SHUKEIHYO"]	 ? rowsEhsptn[i]["ZAIKOCHIWAKE_SHUKEIHYO"].trim().split("-")[0]:"",
								F17	 : rowsEhsptn[i]["ZAIKOCHIWAKE_PICKINGDATA"] ? rowsEhsptn[i]["ZAIKOCHIWAKE_PICKINGDATA"].trim().split("-")[0]:"",
								F18	 : rowsEhsptn[i]["ZAIKOCHIWAKE_PICKINGLIST"] ? rowsEhsptn[i]["ZAIKOCHIWAKE_PICKINGLIST"].trim().split("-")[0]:"",
								F19	 : rowsEhsptn[i]["RSIRCD"] ? ("000000"+rowsEhsptn[i]["RSIRCD"]).slice( -6 ) : "      ",
								F20	 : rowsEhsptn[i]["YOKOMOCHISAKI_KENSHUKBN"]	 ? rowsEhsptn[i]["YOKOMOCHISAKI_KENSHUKBN"].trim().split("-")[0]:"",
								F21	 : rowsEhsptn[i]["YOKOMOCHISAKI_DENPYOKBN"]	 ? rowsEhsptn[i]["YOKOMOCHISAKI_DENPYOKBN"].trim().split("-")[0]:"",
								F22	 : rowsEhsptn[i]["YOKOMOCHISAKI_SHUKEIHYO"]	 ? rowsEhsptn[i]["YOKOMOCHISAKI_SHUKEIHYO"].trim().split("-")[0]:"",
						};
						targetRows.push(rowDate);
					}
				}
			}
			return targetRows;
		},
		getMergeGridDate: function(sircd, target, del){
			// 保持したデータと入力データ比較を比較する。
			var that = this;

			var newrows = that.getGridData(sircd, target);		// 変更データ
			var oldrows = [];
			var targetRows= [];

			// 基本情報
			if(target===undefined || target==='data'){

			}

			if(target===undefined || target===$.id.grd_hsptn+'_list'){
				if(del && del === 'del'){
					// 削除データ
					oldrows = that.grd_hsptn_data ? that.grd_hsptn_data : [];
					if(oldrows.length > newrows.length){
						// 初期検索結果の方が多い場合

						for (var i = 0; i < oldrows.length; i++) {
							var countFlg = false

							var newLines = newrows.filter(function(item, index){
								if((item.F1).indexOf(oldrows[i]['F1']) >= 0){
									countFlg = true;
								}
							});
							if(!countFlg){
								//存在しなかった場合
								targetRows.push(oldrows[i]);
							}
						}
					}
				}
			}

			if(target===undefined || target===$.id.grd_ehsptn+'_list'){
				if(del && del === 'del'){
					// 削除データ
					oldrows = that.grd_ehsptn_data ? that.grd_ehsptn_data : [];
					if(oldrows.length > newrows.length){
						// 初期検索結果の方が多い場合

						for (var i = 0; i < oldrows.length; i++) {
							var countFlg = false

							var newLines = newrows.filter(function(item, index){
								if((item.F1).indexOf(oldrows[i]['F1']) >= 0){
									countFlg = true;
								}
							});
							if(!countFlg){
								//存在しなかった場合
								targetRows.push(oldrows[i]);
							}
						}
					}
				}
			}



			return targetRows;
		},
		setWappenHptnD: function(that, reportno, id, isUpdateReport){
			// 名称区分が重複する入力項目が存在する為、専用のcomboboxを作成するfunctionを使用する。
			var idx = -1;

			var tag_options = $('#'+id).attr('data-options');
			if(tag_options){
				tag_options = '\"'+tag_options.replace(/'/g, '').replace(/,/g, '\",\"').replace(/:/g, '\":\"')+'\"';
			}
			var options = tag_options ? JSON.parse('{'+tag_options+'}'): JSON.parse('{}');

			var required = options && options.required;
			var topBlank = !required;
			var panelWidth = options && options.panelWidth ? options.panelWidth : null;
			var panelHeight = options && options.panelHeight ? options.panelHeight :'auto';
			var suffix = that.suffix ? that.suffix : '';
			var changeFunc1 = null;
			if(isUpdateReport){
				changeFunc1 = function(){
					if(idx > 0 && that.queried){
						$($.id.hiddenChangedIdx+suffix).val("1");
					}
				};
			}
			// 当項目を変更した際に、値をセットする項目がある場合の処理（セット先の項目は、HTMLタグとして、for_inp=当項目のID_列名）
			var changeFunc2 = null;
			if($.isFunction(that.changeInputboxFunc)){
				changeFunc2 = function(newValue, obj){
					that.changeInputboxFunc(that, id, newValue, obj);
				};
			}else{
				if($('[for_inp^='+id+'_]').length > 0){
					changeFunc2 = function(newValue){
						var param = [{"value":newValue}];
						$.getsetInputboxRowData(reportno, 'for_inp', id, param, that);
					};
				}
			}

			var editable = true;
			if ($('#'+id).parent().is(".ref_editor")) {
				editable = false;
			}

			// フォーカスアウトのタイミングの動作
			$('#'+id).next().on('focusout', function(e){
				var obj = $(this).prev();

				if (!$.setComboReload(obj,false)) {
					obj.combobox('reload');
				}
			});

			$('#'+id).combobox({
				url:$.reg.easy,
				required: required,
				editable: editable,
				autoRowHeight:false,
				panelWidth:panelWidth,
				panelHeight:panelHeight,
				valueField:'VALUE',
				textField:'TEXT',
				multiple :false,
				prompt: '',
				keyHandler: {
					up: $.fn.combobox.defaults.keyHandler.up,
					down: $.fn.combobox.defaults.keyHandler.down,
					left: $.fn.combobox.defaults.keyHandler.left,
					right: $.fn.combobox.defaults.keyHandler.right,
					enter: function(e){
						$('#'+id).combobox('hidePanel');
						e.preventDefault();
					},
					query: $.fn.combobox.defaults.keyHandler.query
				},
				onBeforeLoad:function(param){
					// 情報設定
					var json = [{
						DUMMY: 'DUMMY'
					}];
					if(topBlank){json[0]['TOPBLANK'] = topBlank;}

					param.page		=	reportno;
					param.obj		=	$.id_mei.kbn412;
					param.sel		=	(new Date()).getTime();
					param.target	=	$.id_mei.kbn412;
					param.action	=	$.id.action_init;
					param.json		=	JSON.stringify(json);
					param.datatype	=	"combobox";
				},
				onLoadSuccess:function(data){
					// 初期化
					var val = null;
					if (that.initedObject && $.inArray(id, that.initedObject) < 0){
						that.initedObject.push(id);
						var init = $.getJSONValue(that.jsonHidden, id);
						for (var i=0; i<data.length; i++){
							if (data[i].VALUE == init){
								val = init;
								break;
							}
						}
					}
					if (val === null && data.length>0){
						val = data[0].VALUE;
					}
					if (val){
						$('#'+id).combobox('setValue', val);
					}
					idx = 1;
					// ログ出力
					$.log(that.timeData, id+' init:');
					if(suffix===''){
						if(isUpdateReport){
							// 初期表示処理
							$.initialDisplay(that);
						}else{
//							// 検索ボタン有効化
//							$.setButtonState('#'+$.id.btn_search, true, id);
							// 初期表示検索処理
							$.initialSearch(that);
						}
					}
				},
				onChange:function(newValue, oldValue, obj){
					if(changeFunc1!==null){ changeFunc1();}
					if(changeFunc2!==null){ changeFunc2(newValue, $(this));}

					if(obj===undefined){obj = $(this);}
					that.getComboErr(obj,false,newValue,oldValue);
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

				if(that.reportYobiInfo()==='1'){
					index = 2;
				}else{
					index = 1;
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
		getComboErr: function (obj,editable,newValue,oldValue) {
			var data = obj.combobox('getData');

			if (!obj.hasClass('datagrid-editable-input')) {
				if (!$.setComboReload(obj,true) && !editable) {
					$.showMessage("E11302",["入力値"],function () {$.addErrState(this,obj,false)});
					obj.combobox('reload');
					obj.combobox('hidePanel');
				} else if ($.isEmptyVal(newValue)) {
					obj.combobox('setValue',obj.combobox('getData')[0].VALUE);
				} else if ($.isEmptyVal(oldValue)) {
					if (obj.next().find('[tabindex=1]').length===1) {
						obj.combo("textbox").focus();
					}
				}
			}
		},
		// コンボボックス(特にid_mei宣言の入力項目)を共通で設定する
		setMeisyoCombo: function(that, reportno, id, isUpdateReport){
			var idx = -1;
			if($('#'+id).is(".easyui-combobox_")){
				$('#'+id).removeClass("easyui-combobox_").addClass("easyui-combobox");
			}

			// 更新項目で参照表示かどうか
			var isRefer = $.isReferUpdateInput(that, $('#'+id), isUpdateReport);
			var readonly = isRefer;
			var onShowPanel = $.fn.combobox.defaults.onShowPanel;
			if (isRefer) {
				onShowPanel = function(){
					$('#'+id).combobox('hidePanel');
				};
			}

			var tag_options = $('#'+id).attr('data-options');
			if(tag_options){
				tag_options = '\"'+tag_options.replace(/'/g, '').replace(/,/g, '\",\"').replace(/([a-zA-Z0-9]+):/g, '$1\":\"')+'\"';
			}
			var options = tag_options ? JSON.parse('{'+tag_options+'}'): JSON.parse('{}');

			var required = options && options.required;
			var editable = true;
			if ($('#'+id).parent().is(".ref_editor")) {
				// editable = false;
			}
			var editableCheck = options && options.editable? true:false;

			var topBlank = !required;
			var panelWidth = options && options.panelWidth ? options.panelWidth : null;
			var panelHeight = options && options.panelHeight ? options.panelHeight :'auto';
			var suffix = that.suffix ? that.suffix : '';
			var changeFunc1 = null;
			if(isUpdateReport){
				changeFunc1 = function(){
					if(idx > 0 && that.queried){
						$($.id.hiddenChangedIdx+suffix).val("1");
					}
				};
			}
			// 当項目を変更した際に、値をセットする項目がある場合の処理（セット先の項目は、HTMLタグとして、for_inp=当項目のID_列名）
			var changeFunc2 = null;
			if($.isFunction(that.changeInputboxFunc)){
				changeFunc2 = function(newValue, obj){
					that.changeInputboxFunc(that, id, newValue, obj);
				};
			}else{
				if($('[for_inp^='+id+'_]').length > 0){
					changeFunc2 = function(newValue){
						var param = [{"value":newValue}];
						$.getsetInputboxRowData(reportno, 'for_inp', id, param, that);
					};
				}
			}

			// フォーカスアウトのタイミングの動作
			$('#'+id).next().on('focusout', function(e){
				var obj = $(this).prev();
				if (!$.setComboReload(obj,false)) {
					obj.combobox('reload');
				}
			});

			var gridId = id.replace(suffix, "").replace('grd_', "")

			$('#'+id).combobox({
				url:$.reg.easy,
				required: required,
				readonly:readonly,
				editable: editable,
				autoRowHeight:false,
				panelWidth:panelWidth,
				panelHeight:panelHeight,
				valueField:'VALUE',
				textField:'TEXT',
				multiple :false,
				prompt: '',
				keyHandler: {
					up: $.fn.combobox.defaults.keyHandler.up,
					down: $.fn.combobox.defaults.keyHandler.down,
					left: $.fn.combobox.defaults.keyHandler.left,
					right: $.fn.combobox.defaults.keyHandler.right,
					enter: function(e){
						$(this).combobox('hidePanel');
						e.preventDefault();
					},
					query: $.fn.combobox.defaults.keyHandler.query
				},
				onShowPanel:onShowPanel,
				onBeforeLoad:function(param){
					// 情報設定
					var json = [{
						DUMMY: 'DUMMY'
					}];
					if(topBlank){json[0]['TOPBLANK'] = topBlank;}

					param.page		=	reportno;
					param.obj		=	gridId;
					param.sel		=	(new Date()).getTime();
					param.target	=	gridId;
					param.action	=	$.id.action_init;
					param.json		=	JSON.stringify(json);
					param.datatype	=	"combobox";
					$('#'+gridId).combobox('setText', ' ');
				},
				onLoadSuccess:function(data){
					// 初期化
					var val = null;
					if (that.initedObject && $.inArray(id, that.initedObject) < 0){
						that.initedObject.push(id);
						var init = $.getJSONValue(that.jsonHidden, id);
						for (var i=0; i<data.length; i++){
							if (data[i].VALUE == init){
								val = init;
								break;
							}
						}
					}
					if (val === null && data.length>0){
						val = data[0].VALUE;
					}
					if (val){
						$('#'+id).combobox('setValue', val);
					}
					idx = 1;
					// ログ出力
					$.log(that.timeData, id+' init:');
					if(suffix===''){
						if(isUpdateReport){
							// 初期表示処理
							$.initialDisplay(that);
						}else{
//							// 検索ボタン有効化
//							$.setButtonState('#'+$.id.btn_search, true, id);
							// 初期表示検索処理
							$.initialSearch(that);
						}
					}
				},
				onChange:function(newValue, oldValue, obj){
					if(obj===undefined){obj = $(this);}

					if(changeFunc1!==null){ changeFunc1();}
					if(changeFunc2!==null){ changeFunc2(newValue, obj);}
					if(idx > 0){
						$.removeErrState();
					}

					if (!$.setComboReload(obj,true) && !editableCheck) {
						$.showMessage("E11302",["入力値"],function () {$.addErrState(this,obj,false)});
						obj.combobox('reload');
						obj.combobox('hidePanel');
					} else if (newValue!=='-1' && !$.isEmptyVal(newValue)) {
						setTimeout(function(){
							if(obj.combobox('panel')){
								if (obj.combobox('panel')[0].scrollHeight!==0) {
									obj.combo("textbox").focus();
								}
							}

						},1000);
					} else if ($.isEmptyVal(newValue)) {
						obj.combobox('setValue',obj.combobox('getData')[0].VALUE);
						obj.combo("textbox").focus();
					}
				}
			});
			if(isRefer){ $.setInputStateRefer(that, $('#'+id)); }
		},
		changeInputboxFunc:function(that, id, newValue, obj){

			var parentObj = $('#'+that.focusRootId);
			var msgParam  = [];
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

				// 代表仕入先コード、実仕入先コード
				if(id===$.id_inp.txt_dsircd || id===$.id_inp.txt_df_rsircd){
					msgid = that.checkInputboxFunc(id, newValue, '');
					if(msgid !== null){
						msgParam = ['代表仕入先コード','仕入先マスタ'];
					}
				}

				// 配送パターン
				if(that.focusGridId === $.id.grd_hsptn+'_list' && id===$.id_inp.txt_hsptn){
					var rows	 = $('#'+that.focusGridId).datagrid('getRows');
					var row		 = $('#'+that.focusGridId).datagrid("getSelected");
					var editIdx	 = $('#'+that.focusGridId).datagrid("getRowIndex", row);		// 編集行のIndex
					if(rows){
						for (var i=0; i<rows.length; i++){
							if(rows[i]['HSPTN']){
								if(i != editIdx){
									// 入力中の行以外のデータとの重複しているかチェックする。
									if(String(newValue) === rows[i]['HSPTN']){
										$.showMessage('E11082');
									}
								}
							}
						}
					}

					/*var hsptns = [];
					var targetRowsHsptn = $('#'+that.focusGridId).datagrid('getRows');
					for (var i=0; i<targetRowsHsptn.length; i++){
						if(targetRowsHsptn[i]["HSPTN"]){
							hsptns.push(targetRowsHsptn[i]["HSPTN"]);
						}
					}
					// 重複チェック：配送パターン
					var hsptn = hsptns.filter(function (element, index, self) { return self.indexOf(element)*1 === index; });
					if(hsptns.length !== hsptn.length){
						$.showMessage('E11082');
					}*/
				}

				// エリア別配送パターン
				if(that.focusGridId === $.id.grd_ehsptn+'_list'){
					if(id===$.id_inp.txt_hsptn || id===$.id_inp.txt_tengpcd){
						// 配送パターン、店グループ入力時
						var rows = $('#'+that.focusGridId).datagrid('getRows');
						var row		 = $('#'+that.focusGridId).datagrid("getSelected");
						var editIdx	 = $('#'+that.focusGridId).datagrid("getRowIndex", row);		// 編集行のIndex
						var tengpcd	 = "";
						var hsptn	 = "";

						if(id===$.id_inp.txt_hsptn ){
							hsptn	 = newValue;
							tengpcd	 = $.getInputboxValue($('#'+$.id_inp.txt_tengpcd + '_'));
						}else if(id===$.id_inp.txt_tengpcd ){
							hsptn	 = $.getInputboxValue($('#'+$.id_inp.txt_hsptn + '_'));
							tengpcd	 = newValue;
						}

						if(rows){
							for (var i=0; i<rows.length; i++){

								if(i != editIdx){
									// 入力中の行以外のデータとの重複しているかチェックする。
									if(rows[i]['HSPTN']){
										if(String(hsptn) === rows[i]['HSPTN']){
											if(rows[i]['TENGPCD']){
												if(String(tengpcd) === rows[i]['TENGPCD']){
													$.showMessage('E11083');
												}
											}
										}
									}
								}
							}
						}
					}

					/*var ehsptns = [];
					var tengpcd = [];
					var	mergeStrings = [];
					var targetRowsEhsptn = $('#'+$.id.grd_ehsptn+'_list').datagrid('getRows');
					for (var i=0; i<targetRowsEhsptn.length; i++){
						if(targetRowsEhsptn[i]["HSPTN"]){
							ehsptns.push(targetRowsEhsptn[i]["HSPTN"]);
							tengpcd.push(targetRowsEhsptn[i]["TENGPCD"]);
							mergeStrings.push((ehsptns[i] + tengpcd[i]));
						}
					}

					// 重複チェック：配送パターン
					var mergeString = mergeStrings.filter(function (element, index, self) { return self.indexOf(element)*1 === index; });
					if(mergeStrings.length !== mergeString.length){
						$.showMessage('E11083');
					}*/
				}

				if(msgid !==null){
					if(msgParam.length > 0){
						$.showMessage(msgid, msgParam, func_focus );
					}else{
						$.showMessage(msgid, undefined, func_focus );
					}
					return false;
				}
			}
		},
		// IDとvalueでチェック処理を実施
		checkInputboxFunc:function(id, newValue, kbn, record, isNew){
			var that = this;
			var sdt, edt;

			// 仕入先コード
			if(id===$.id_inp.txt_dsircd || id===$.id_inp.txt_rsircd){
				if(newValue !== '' && newValue){

					if(id===$.id_inp.txt_dsircd){
						if(that.reportYobiInfo()==='0'){
							// 新規登録時は入力仕入先コードと等しい場合チェックを行わない。
							var txt_sircd		 = $.getInputboxValue($('#'+$.id_inp.txt_sircd));

							if(txt_sircd == newValue){
								return null;
							}
						}
					}

					// 仕入先コード
					var param = {};
					param["KEY"] =  "MST_CNT";
					param["value"] = newValue;
					var chk_cnt = $.getInputboxData(that.name, $.id.action_check, $.id_inp.txt_sircd, [param]);
					if(chk_cnt==="" || chk_cnt==="0"){
						//return "E11099";
						return "EX1100";

					}

					// 店舗部門マスタ
					/*var param = {};
					param["KEY"] =  "MST_CNT";
					param["value"] = newValue.substring(0, 2);
					var chk_cnt = $.getInputboxData(that.name, $.id.action_check, 'MSTTENBMN', [param]);
					if(chk_cnt==="" || chk_cnt==="0"){
						return "E20219";
					}*/
				}
			}

			// 配送パターン
			if(id===$.id_inp.txt_hsptn){
				if(newValue !== '' && newValue){
					// 仕入先コード
					var param = {};
					//param["KEY"] =  "MST_CNT";
					param["value"] = newValue;

					var rows = $.getSelectListData(that.name, $.id.action_change, id, [{value:newValue}]);

					//var chk_cnt = $.getInputboxData(that.name, $.id.action_change, $.id_inp.txt_hsptn, [param]);
					if(rows.length == 0){
						//return "E11099";
						//return "E11100";
						return "EX1100";
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

			// 配送パターン
			if(id===$.id_inp.txt_hsptn){
				values["HSPTN"] = $.getInputboxValue($('#'+$.id_inp.txt_hsptn));
			}

			// 代表仕入先
			if(id===$.id_inp.txt_dsircd){
				values["DSIRCD"] = $.getInputboxValue($('#'+$.id_inp.txt_dsircd));
			}

			// 実仕入先
			if(id===$.id_inp.txt_rsircd){
				values["RSIRCD"] = parseInt($.getInputboxValue($('#'+$.id_inp.txt_rsircd)));
			}

			// デフォルト_実仕入先
			if(id===$.id_inp.txt_rsircd){
				values["DF_RSIRCD"] = $.getInputboxValue($('#'+$.id_inp.txt_rsircd));
			}

			// 店グループ
			if(id===$.id_inp.txt_tengpcd){
				values["EHSPTN"] = $.getInputboxValue($('#'+$.id_inp.txt_hsptn+'_'));
			}

			// 情報設定
			return [values];
		},
		keyEventInputboxFunc:function(e, code, that, obj){
			// *** Enter or Tab ****
			if(code === 13 || code === 9){
				obj = obj.parent().prev();
				if(obj.hasClass('easyui-combobox')) {
					if (!$.setComboReload(obj,false)) {
						obj.combobox('reload');
					}
				}
			}
		}
	} });
})(jQuery);