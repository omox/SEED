/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_ReportTM002',			// （必須）レポートオプションの確認
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

		baseTablekbn:"",					// 検索結果のテーブル区分：0-正/1-予約(※予約1の新規→正を参照しているので正、予約2の新規→予約1を参照しているので予)
		baseData:[],						// 検索結果保持用
		subData:[],							// 検索結果保持用(グリッド情報)

		grd_data:[],						// メイン情報：商品マスタ
		grd_data_other:[],					// 補足情報：その他、テーブルに登録しない情報などを保持
		grd_moycd_r_data:[],				// グリッド情報：催しコード_レギュラー
		grd_moycd_s_data:[],				// グリッド情報：催しコード_スポット
		grd_moycd_t_data:[],				// グリッド情報：催しコード_特売
		grd_moycd_r_del_data:[],			// グリッド情報：催しコード_レギュラー
		grd_moycd_s_del_data:[],			// グリッド情報：催しコード_スポット
		grd_moycd_t_del_data:[],			// グリッド情報：催しコード_特売
		grd_tokchirasbmn_data:[],			// グリッド情報：ちらしのみ部門(催しコード_特売関連情報)
		grd_toktg_data:[],					// グリッド情報：全店特売（アンケート有）(催しコード_特売関連情報)
		grd_toktg_del_data:[],				// グリッド情報：全店特売（アンケート有）(催しコード_特売関連情報)
		grd_del_tokmoysyu:'1',				// 催し週テーブル 論理削除:1 更新:0

		shuno_list:{},						// 週Noの情報を保持（Init時に取得）
		selectShuno:"",
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
			// 検索実行
			that.onChangeReport = true;

			// 処理日付取得
			$.getsetInputboxData(reportno, $.id.txt_shoridt, [{}], $.id.action_init);

			// 週№
			$.setMeisyoCombo(that, reportno, $.id.sel_shuno, false);

			// 特別週
			$.setCheckboxInit2(that.jsonHidden, $.id.chk_tshuflg, that);
			$.setCheckboxInit2(that.jsonHidden, $.id.chk_nenmatkbn, that);
			$.setCheckboxInit2(that.jsonHidden, $.id.chk_del, that);
			$.setCheckboxInit2(that.jsonHidden, $.id.chk_use, that);
			// 入力テキストボックス系
			var inputbox = Object.getOwnPropertyNames($.id_inp);
			var notTargetId = [$.id_inp.txt_shuno];
			for ( var sel in inputbox ) {
				if($('#'+$.id_inp[inputbox[sel]]).length > 0 && notTargetId.indexOf(inputbox[sel]) === -1){
					$.setInputbox(that, reportno, $.id_inp[inputbox[sel]], true);
				}
			}

			// 初期化終了
			this.initializes =! this.initializes;

			// ログ出力
			$.log(that.timeData, 'initialize:');
		},
		searched_initialize: function (reportno, opts){	// 検索結果を受けての初期化
			var that = this;

			// 編集可能データグリッドの共通処理設定
			// 編集エディターの機能拡張（非表示で参照用のオブジェクトが準備してある前提、参照オブジェクトはグリッドのフィールド名を元に判断）
			$.extendDatagridEditor(that);

			// ***個別データグリッド設定
			// 催しコード_レギュラー
			that.setGrid(that, reportno, $.id.grd_moycd_r);
			// 催しコード_スポット
			that.setGrid(that, reportno, $.id.grd_moycd_s);
			// 催しコード_特売
			that.setGrid(that, reportno, $.id.grd_moycd_t);

			// チェックボックスの設定
			$.initCheckboxCss($("#"+that.focusRootId));
			// キーイベントの設定
			$.initKeyEvent(that);

			that.queried = true;

			// フォーカスアウトのタイミングの動作
			if(that.judgeRepType.sei_new){
				$('#'+$.id.sel_shuno).next().on('focusout click', function(e){
					var obj = $(this).prev();
					var val = obj.next().children('.textbox-value').val();
					var chk_tshuflg = $.getInputboxValue($('#'+$.id.chk_tshuflg));
					var relatedTarget = !$.isEmptyVal(e.relatedTarget) ? e.relatedTarget : '';

					if (!$.isEmptyVal(relatedTarget) && relatedTarget.className.indexOf('btn') > -1 && !(relatedTarget.innerText.indexOf('登') > -1)) {
						return;
					}

					if(!$.isEmptyVal(relatedTarget) && relatedTarget.tabIndex!==2) {
						if (val==='-1') {
							var rt = $($.id.toolbarform).form('validate');
							if(!rt){
								$.addErrState(that, $('.validatebox-invalid').eq(0), false);
								return rt;
							}
						}
					}

					if (val!=='-1') {
						$($.id.hiddenChangedIdx).val("1");
					} else {
						obj.combobox('enableValidation');
					}

					$.setInputboxValue($('#'+$.id_inp.txt_shuno),val);
					that.selectShuno = "";
					that.changeInputboxFunc(that, $.id_inp.txt_shuno, val, $('#'+$.id.sel_shuno));
				});

				$('#'+$.id.sel_shuno).combobox('panel').on('click', function(e){
					var newValue = $.getInputboxValue($('#'+$.id.sel_shuno));

					if (newValue!=='-1') {
						$($.id.hiddenChangedIdx).val("1");
					} else {
						$('#'+$.id.sel_shuno).combobox('enableValidation');
					}

					$.setInputboxValue($('#'+$.id_inp.txt_shuno),newValue);
					that.selectShuno = "";
					that.changeInputboxFunc(that, $.id.sel_shuno, newValue, $('#'+$.id.sel_shuno));
				});

			} else {
				var val = $.getInputboxValue($('#'+$.id.sel_shuno)) === '-1' ? $.getInputboxText($('#'+$.id.sel_shuno)) : $.getInputboxValue($('#'+$.id.sel_shuno));
				$.setInputboxValue($('#'+$.id_inp.txt_shuno),val);
			}
			// ログ出力
			$.log(that.timeData, 'searched_initialize:');
		},
		judgeRepType: {
			sei_new 		: false,	// 正 -新規
			sei_upd 		: false,	// 正 -更新
			sei_ref 		: false,	// 正 -参照
		},
		setInitObjectState: function(){	// 画面初期化時の項目制御
			var that = this;
			// 引き継ぎ情報セット
			var sendBtnid = $.getJSONValue(that.jsonHidden, "sendBtnid");
			if(!sendBtnid){
				sendBtnid = $('#sendBtnid').val();
			}
			$('#sendBtnid').val(sendBtnid);
			that.sendBtnid = sendBtnid;

			// 帳票タイプ判断：ボタン情報のみで判断
			if(that.sendBtnid===$.id.btn_new){
				that.judgeRepType.sei_new = true;
			}else if(that.sendBtnid===$.id.btn_sel_change){
				that.judgeRepType.sei_upd = true;
			}else if(that.sendBtnid===$.id.btn_sel_refer){
				that.judgeRepType.sei_ref= true;
			}

			that.baseTablekbn = $.id.value_tablekbn_sei;						// 情報取得先
			// 新規：
			if(that.judgeRepType.sei_new){

				// 新規の場合のみ必須項目
				$.setInputBoxRequired($("#"+$.id_inp.txt_shuno));

				$('#'+$.id.btn_cancel).on("click", $.pushChangeReport);

				$("#disp_record_info").hide();
				$($.id.buttons).show();

				$.initReportInfo("TM002", "催しコード　週間催し　新規", "新規");
			// 変更：
			}else if(that.judgeRepType.sei_upd){

				$('#'+$.id.btn_cancel).on("click", $.pushChangeReport);

				$.setInputBoxDisable($("#"+$.id.sel_shuno));
				$.setInputBoxDisable($("#"+$.id_inp.txt_shuno));
				$.setInputBoxDisable($("#"+$.id.chk_tshuflg));
				$($.id.buttons).show();

				$.initReportInfo("TM002", "催しコード　週間催し　変更", "変更");
			// 参照：
			}else if(that.judgeRepType.sei_ref){

				$.setInputBoxDisable($("#"+$.id.sel_shuno));
				$.setInputBoxDisable($("#"+$.id_inp.txt_shuno));
				$.setInputBoxDisable($("#"+$.id.chk_tshuflg));

				$.setInputBoxDisable($("#"+$.id.btn_cancel)).hide();
				$.setInputBoxDisable($("#"+$.id.btn_upd)).hide();
				$.initReportInfo("TM002", "催しコード　週間催し　参照", "参照");
			}
			// 変更
			$($.id.hiddenChangedIdx).val('');
		},
		clear:function(){
			// 隠し情報初期化
			$($.id.hiddenChangedIdx).val("");						// 変更行Index
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
			var txt_shuno		= $.getJSONObject(this.jsonString, $.id_inp.txt_shuno).value;	// 引継週№
			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$.appendMask();
			$.appendMaskMsg();

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,			// レポート名
					SHUNO:			txt_shuno,			// 引継週№
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


					var opts = JSON.parse(json).opts


					// 検索結果を保持
					that.baseData = JSON.parse(json).rows;

					if(opts && opts.rows_y){
						that.yoyakuData = opts.rows_y;
					}

					// メインデータ表示
					that.setData(that.baseData);

					// 検索結果をうけての子テーブルマスタ項目などの初期化設定
					that.searched_initialize(reportno, opts);

					// 現在情報を変数に格納(追加した情報については個別にロード成功時に実施)
					that.setGridData(that.getGridData("", "grd_data"), "grd_data");

					// 隠し情報初期化
					$($.id.hiddenChangedIdx).val("");						// 変更行Index

					// 状態保存
					$.saveState2(reportno, that.getJSONString());

					// ログ出力
					$.log(that.timeData, 'loaded:');
				}
			);
		},
		getGridData: function (shuno, target, delFlg, add20){
			var that = this;
			var data = {};

			if(add20===undefined) add20 = false;

			// 基本情報
			if(target===undefined || target==="grd_data"){
				var targetData = [{}];
				$('#'+that.focusRootId).find('[col^=F]').each(function(){
					var col = $(this).attr('col');
					var val = $.getInputboxValue($(this));
					if($(this).hasClass('easyui-combobox') && val==="-1"){ val = $.getInputboxText($(this));}
					targetData[0][col] = val;
				});
				data["grd_data"] = targetData;
			}

			// 補足情報(テーブルに登録しない情報などを保持)
			if(target===undefined || target==="grd_data_other"){
				var targetData = [{}];
				//targetData[0]["KETAKBN"] = $.getInputboxValue($('#'+$.id_mei.kbn143));	// 桁指定
				data["grd_data_other"] = targetData;
			}

			// 更新対象データがない場合論理削除対象
			data['DEL_TOKMOYSYU'] = '1';

			// 催しコード_レギュラー
			if(target===undefined || target===$.id.grd_moycd_r){
				var targetRowsMoycdR= [],targetRowsMoycdRDel= [];
				var rowsMoycdR= $('#'+$.id.grd_moycd_r).datagrid('getRows');
				for (var i=0; i<rowsMoycdR.length; i++){
					if(!$.isEmptyVal(rowsMoycdR[i]["MOYSSTDT"])){
						var rowData = {
								F1 : rowsMoycdR[i]["MOYSKBN"],						// F1 : 催し区分	MOYSKBN
								F2 : rowsMoycdR[i]["MOYSSTDT"],						// F2 : 催し開始日	MOYSSTDT
								F3 : rowsMoycdR[i]["MOYSRBAN"],						// F3 : 催し連番	MOYSRBAN
								F4 : shuno,											// F4 : 週№		SHUNO
								F5 : rowsMoycdR[i]["MOYKN"],						// F5 : 催し名称（漢字）	MOYKN
								F6 : rowsMoycdR[i]["MOYAN"],						// F6 : 催し名称（カナ）	MOYAN
								F7 : rowsMoycdR[i]["NENMATKBN"],					// F7 : 年末区分	NENMATKBN
								F8 : $.getParserDt(rowsMoycdR[i]["HBSTDT"],add20),	// F8 : 販売開始日	HBSTDT
								F9 : $.getParserDt(rowsMoycdR[i]["HBEDDT"],add20),	// F9 : 販売終了日	HBEDDT
								F10: $.getParserDt(rowsMoycdR[i]["NNSTDT"],add20),	// F10: 納入開始日	NNSTDT
								F11: $.getParserDt(rowsMoycdR[i]["NNEDDT"],add20),	// F11: 納入終了日	NNEDDT

								UPDDT:rowsMoycdR[i]["UPDDT"]						// 検証用
						};
						if(delFlg && rowsMoycdR[i]["DEL"]===$.id.value_on){
							targetRowsMoycdRDel.push(rowData);
						}else{
							targetRowsMoycdR.push(rowData);

							// 1件以上更新対象データがある場合論理削除対象外
							data['DEL_TOKMOYSYU'] = '0';
						}
					}
				}
				data[$.id.grd_moycd_r] = targetRowsMoycdR;
				data[$.id.grd_moycd_r+'_DEL'] = targetRowsMoycdRDel;
			}

			// 催しコード_スポット
			// レギュラー取扱フラグのチェックがない場合、この売価コントロール部分は設定不可。
			if(target===undefined || target===$.id.grd_moycd_s){
				var targetRowsMoycdS = [],targetRowsMoycdSDel= [];
				var rowsMoycdS= $('#'+$.id.grd_moycd_s).datagrid('getRows');
				for (var i=0; i<rowsMoycdS.length; i++){
					if(!$.isEmptyVal(rowsMoycdS[i]["MOYSSTDT"])){
						var rowData = {
							F1 : rowsMoycdS[i]["MOYSKBN"],						// F1 : 催し区分	MOYSKBN
							F2 : rowsMoycdS[i]["MOYSSTDT"],						// F2 : 催し開始日	MOYSSTDT
							F3 : rowsMoycdS[i]["MOYSRBAN"],						// F3 : 催し連番	MOYSRBAN
							F4 : shuno,											// F4 : 週№		SHUNO
							F5 : rowsMoycdS[i]["MOYKN"],						// F5 : 催し名称（漢字）	MOYKN
							F6 : rowsMoycdS[i]["MOYAN"],						// F6 : 催し名称（カナ）	MOYAN
							F7 : rowsMoycdS[i]["NENMATKBN"],					// F7 : 年末区分	NENMATKBN
							F8 : $.getParserDt(rowsMoycdS[i]["HBSTDT"],add20),	// F8 : 販売開始日	HBSTDT
							F9 : $.getParserDt(rowsMoycdS[i]["HBEDDT"],add20),	// F9 : 販売終了日	HBEDDT
							F10: $.getParserDt(rowsMoycdS[i]["NNSTDT"],add20),	// F10: 納入開始日	NNSTDT
							F11: $.getParserDt(rowsMoycdS[i]["NNEDDT"],add20),	// F11: 納入終了日	NNEDDT
							F14: $.getParserDt(rowsMoycdS[i]["PLUSDDT"],add20),	// F14: PLU配信日	PLUSDDT

							UPDDT:rowsMoycdS[i]["UPDDT"]						// 検証用
						};
						if(delFlg && rowsMoycdS[i]["DEL"]===$.id.value_on){
							targetRowsMoycdSDel.push(rowData);
						}else{
							targetRowsMoycdS.push(rowData);

							// 1件以上更新対象データがある場合論理削除対象外
							data['DEL_TOKMOYSYU'] = '0';
						}
					}
				}
				data[$.id.grd_moycd_s] = targetRowsMoycdS;
				data[$.id.grd_moycd_s+'_DEL'] = targetRowsMoycdSDel;
			}

			// 催しコード_特売
			gpkbn = $.id.value_gpkbn_sir;
			if(target===undefined || target===$.id.grd_moycd_t){
				var targetRowsMoycdT=  [],targetRowsMoycdTDel= [],targetRowsToktg= [],targetRowsToktgDel= [],targetRowsTokchirasbmn=[];
				var rowsMoycdT= $('#'+$.id.grd_moycd_t).datagrid('getRows');
				for (var i=0; i<rowsMoycdT.length; i++){
					if(!$.isEmptyVal(rowsMoycdT[i]["MOYSSTDT"])){
						var rowData = {
							F1 : rowsMoycdT[i]["MOYSKBN"],						// F1 : 催し区分	MOYSKBN
							F2 : rowsMoycdT[i]["MOYSSTDT"],						// F2 : 催し開始日	MOYSSTDT
							F3 : rowsMoycdT[i]["MOYSRBAN"],						// F3 : 催し連番	MOYSRBAN
							F4 : shuno,											// F4 : 週№		SHUNO
							F5 : rowsMoycdT[i]["MOYKN"],						// F5 : 催し名称（漢字）	MOYKN
							F6 : rowsMoycdT[i]["MOYAN"],						// F6 : 催し名称（カナ）	MOYAN
							F7 : rowsMoycdT[i]["NENMATKBN"],					// F7 : 年末区分	NENMATKBN
							F8 : $.getParserDt(rowsMoycdT[i]["HBSTDT"],add20),	// F8 : 販売開始日	HBSTDT
							F9 : $.getParserDt(rowsMoycdT[i]["HBEDDT"],add20),	// F9 : 販売終了日	HBEDDT
							F10: $.getParserDt(rowsMoycdT[i]["NNSTDT"],add20),	// F10: 納入開始日	NNSTDT
							F11: $.getParserDt(rowsMoycdT[i]["NNEDDT"],add20),	// F11: 納入終了日	NNEDDT
							F14: $.getParserDt(rowsMoycdT[i]["PLUSDDT"],add20),	// F14: PLU配信日	PLUSDDT

							UPDDT:rowsMoycdT[i]["UPDDT"]						// 検証用
						};

						if(delFlg && rowsMoycdT[i]["DEL"]===$.id.value_on){
							targetRowsMoycdTDel.push(rowData);
							if(rowsMoycdT[i]["MOYSRBAN"]*1 >= 50){
								targetRowsToktgDel.push(rowData);
							}
						}else{
							targetRowsMoycdT.push(rowData);
							if(rowsMoycdT[i]["MOYSRBAN"]*1 >= 50){
								targetRowsToktg.push(rowData);
							}
							// チラシのみ部門
							for (var j=1; j<=20; j++){
								var id = "BMNCD_" + j;
								if(!$.isEmptyVal(rowsMoycdT[i][id])){
									var rowData2 = {
											F1 : rowsMoycdT[i]["MOYSKBN"],					// F1 : 催し区分	MOYSKBN
											F2 : rowsMoycdT[i]["MOYSSTDT"],					// F2 : 催し開始日	MOYSSTDT
											F3 : rowsMoycdT[i]["MOYSRBAN"],					// F3 : 催し連番	MOYSRBAN
											F4 : rowsMoycdT[i][id]							// F4 : 部門	BMNCD
										};
									targetRowsTokchirasbmn.push(rowData2);
								}
							}

							// 1件以上更新対象データがある場合論理削除対象外
							data['DEL_TOKMOYSYU'] = '0';
						}
					}
				}
				data[$.id.grd_moycd_t] = targetRowsMoycdT;
				data[$.id.grd_moycd_t+'_DEL'] = targetRowsMoycdTDel;
				data['TOKCHIRASBMN'] = targetRowsTokchirasbmn;
				data['TOKTG'] = targetRowsToktg;
				data['TOKTG_DEL'] = targetRowsToktgDel;
			}

			return data;
		},
		setGridData: function (data, target, delFlg){
			var that = this;

			// 基本データ
			if(target===undefined || target==="grd_data"){
				that.grd_data =  data["grd_data"];
			}

			// 補足データ
			if(target===undefined || target==="grd_data_other"){
				that.grd_data_other =  data["grd_data_other"];
			}

			// 催しコード_レギュラー
			if(target===undefined || target===$.id.grd_moycd_r){
				that.grd_moycd_r_data =  data[$.id.grd_moycd_r];
			}

			// 催しコード_スポット
			if(target===undefined || target===$.id.grd_moycd_s){
				that.grd_moycd_s_data =  data[$.id.grd_moycd_s];
			}

			// 催しコード_特売
			if(target===undefined || target===$.id.grd_moycd_t){
				that.grd_moycd_t_data =  data[$.id.grd_moycd_t];
				that.grd_tokchirasbmn_data = data['TOKCHIRASBMN'];
				that.grd_toktg_data = data['TOKTG'];
			}

			// 催しコード_レギュラー
			if(target===undefined || target===$.id.grd_moycd_r){
				that.grd_moycd_r_del_data =  data[$.id.grd_moycd_r+'_DEL'];
			}

			// 催しコード_スポット
			if(target===undefined || target===$.id.grd_moycd_s){
				that.grd_moycd_s_del_data =  data[$.id.grd_moycd_s+'_DEL'];
			}

			// 催しコード_特売
			if(target===undefined || target===$.id.grd_moycd_t){
				that.grd_moycd_t_del_data =  data[$.id.grd_moycd_t+'_DEL'];
				that.grd_toktg_del_data =  data['TOKTG_DEL'];
			}

			// 催し週テーブルの論理削除フラグ
			that.grd_del_tokmoysyu = data['DEL_TOKMOYSYU'];

			return true;
		},
		updValidation: function (id){	// （必須）批准
			var that = this;
			var errMsg= "";

			var rowr = $('#'+$.id.grd_moycd_r).datagrid("getSelected");
			var rows = $('#'+$.id.grd_moycd_s).datagrid("getSelected");
			var rowt = $('#'+$.id.grd_moycd_t).datagrid("getSelected");
			var rowIndexr = $('#'+$.id.grd_moycd_r).datagrid("getRowIndex", rowr);
			var rowIndexs = $('#'+$.id.grd_moycd_s).datagrid("getRowIndex", rows);
			var rowIndext = $('#'+$.id.grd_moycd_t).datagrid("getRowIndex", rowt);
			$('#'+$.id.grd_moycd_r).datagrid('endEdit',rowIndexr);
			$('#'+$.id.grd_moycd_s).datagrid('endEdit',rowIndexs);
			$('#'+$.id.grd_moycd_t).datagrid('endEdit',rowIndext);

//			JAVA側検証用
//			var txt_shncd = $.getInputboxValue($('#'+$.id_inp.txt_shncd));
//			var txt_yoyakudt = $.getInputboxValue($('#'+$.id_inp.txt_yoyakudt), "0");
//			var gridData = that.getGridData(txt_shncd, txt_yoyakudt);
//			that.setGridData(gridData);
//			return true;

			// 新規(正)：新規・新規コピー・選択コピーボタン押下時
			var isNew = that.judgeRepType.sei_new;
			// 変更(正)：検索・変更・正ボタン押下時
			var isChange = that.judgeRepType.sei_upd;

			var login_dt = parent.$('#login_dt').text().replace(/\//g, "");	// 処理日付
			var sysdate = login_dt;											// 比較用処理日付

			// 新規(正) 1.1　必須入力項目チェックを行う。
			// 変更(正) 1.1　必須入力項目チェックを行う。
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform).form('validate');
			if(!rt){
				$.addErrState(that, $('.validatebox-invalid').eq(0), false);
				return rt;
			}

			// 週No.
			var shuno = $('#'+$.id_inp.txt_shuno).numberbox('getValue');
			var msgid = that.checkInputboxFunc($.id_inp.txt_shuno, shuno , '');
			if(msgid !==null){
				if (msgid==="E20207" || msgid==="EX1123") {
					id=$.id.chk_tshuflg;
				} else {
					id=$.id.sel_shuno;
				}
				$.showMessage(msgid, undefined, function(){$.addErrState(that, $('#'+id), true)});
				return false;
			}

			// 現在の画面情報を変数に格納
			var gridData = that.getGridData(shuno, undefined, isChange, false);	// 検証用情報取得
			var targetOId = [$.id_inp.txt_moysstdt,$.id_inp.txt_moysrbaninp,$.id_inp.txt_moykn,$.id_inp.txt_moyan,$.id_inp.txt_hbstdt,$.id_inp.txt_hbeddt,$.id_inp.txt_nnstdt,$.id_inp.txt_nneddt,$.id_inp.txt_plusddt];
			var targetCId = ["F2","F3","F5","F6","F8","F9","F10","F11","F14"];

			// 催しコード_レギュラー
			var moyskbn = $.id.value_moykbn_r*1;
			var targetRows = gridData[$.id.grd_moycd_r];
			var moyschk = [];
			for (var i=0; i<targetRows.length; i++){
				for (var j = 0; j < targetOId.length; j++){
					msgid = that.checkInputboxFunc(targetOId[j], targetRows[i][targetCId[j]], moyskbn, targetRows[i], targetRows[i]["UPDDT"]===undefined);
					if(msgid !==null){
						if (msgid.split(",").length===2) {
							$.showMessage(msgid.split(",")[0], [msgid.split(",")[1]], function(){$.addErrState(that, $('#'+that.focusGridId), true, {NO:that.editRowIndex[that.focusGridId], ID:id})});
						} else {
							$.showMessage(msgid, undefined, function(){$.addErrState(that, $('#'+that.focusGridId), true, {NO:that.editRowIndex[that.focusGridId], ID:id})});
						}
						return false;
					}
				}
				// 重複チェック用
				var tmp = targetRows[i]["F1"] + "-" + targetRows[i]["F2"] + "-" + ("0" + targetRows[i]["F3"]).substr(-2);
				moyschk.push(tmp);
			}
			// 重複チェック
			var moyschk_ = moyschk.filter(function (element, index, self) { return index !== self.lastIndexOf(element); });
			if(moyschk_.length !== 0){
				$.showMessage('E20572', ["催しコード " + moyschk_[0] + " 催しコード"]);
				return false;
			}
			moyschk = [];
			// 催しコード_スポット
			moyskbn = $.id.value_moykbn_s*1;
			targetRows = gridData[$.id.grd_moycd_s];
			for (var i=0; i<targetRows.length; i++){
				for (var j = 0; j < targetOId.length; j++){
					msgid = that.checkInputboxFunc(targetOId[j], targetRows[i][targetCId[j]], moyskbn, targetRows[i], targetRows[i]["UPDDT"]===undefined);
					if(msgid !==null){
						if (msgid.split(",").length===2) {
							$.showMessage(msgid.split(",")[0], [msgid.split(",")[1]], function(){$.addErrState(that, $('#'+that.focusGridId), true, {NO:that.editRowIndex[that.focusGridId], ID:id})});
						} else {
							$.showMessage(msgid, undefined, function(){$.addErrState(that, $('#'+that.focusGridId), true, {NO:that.editRowIndex[that.focusGridId], ID:id})});
						}
						return false;
					}
				}
				// 重複チェック用
				var tmp = targetRows[i]["F1"] + "-" + targetRows[i]["F2"] + "-" + ("0" + targetRows[i]["F3"]).substr(-2);
				moyschk.push(tmp);
			}
			// 重複チェック
			moyschk_ = moyschk.filter(function (element, index, self) { return index !== self.lastIndexOf(element); });
			if(moyschk_.length !== 0){
				$.showMessage('E20572', ["催しコード " + moyschk_[0] + " 催しコード"]);
				return false;
			}
			moyschk = [];
			// 催しコード_特売
			moyskbn = $.id.value_moykbn_t*1;
			targetRows = gridData[$.id.grd_moycd_t];
			for (var i=0; i<targetRows.length; i++){
				for (var j = 0; j < targetOId.length; j++){
					msgid = that.checkInputboxFunc(targetOId[j], targetRows[i][targetCId[j]], moyskbn, targetRows[i], targetRows[i]["UPDDT"]===undefined);
					if(msgid !==null){
						if (msgid.split(",").length===2) {
							$.showMessage(msgid.split(",")[0], [msgid.split(",")[1]], function(){$.addErrState(that, $('#'+that.focusGridId), true, {NO:that.editRowIndex[that.focusGridId], ID:id})});
						} else {
							$.showMessage(msgid, undefined, function(){$.addErrState(that, $('#'+that.focusGridId), true, {NO:that.editRowIndex[that.focusGridId], ID:id})});
						}
						return false;
					}
				}
				// 重複チェック用
				var tmp = targetRows[i]["F1"] + "-" + targetRows[i]["F2"] + "-" + ("0" + targetRows[i]["F3"]).substr(-2);
				moyschk.push(tmp);
			}
			// 重複チェック
			moyschk_ = moyschk.filter(function (element, index, self) { return index !== self.lastIndexOf(element); });
			if(moyschk_.length !== 0){
				$.showMessage('E20572', ["催しコード " + moyschk_[0] + " 催しコード"]);
				return false;
			}

			var rowsMoycdT= $('#'+$.id.grd_moycd_t).datagrid('getRows');
			var bmncds = [];
			for (var i=0; i<rowsMoycdT.length; i++){
				if(!$.isEmptyVal(rowsMoycdT[i]["MOYSSTDT"])){
					// チラシのみ部門
					for (var j=1; j<=20; j++){
						var id = "BMNCD_" + j;
						if(!$.isEmptyVal(rowsMoycdT[i][id])){
							// 重複チェック用
							bmncds.push(rowsMoycdT[i][id]);
						}
					}
					// 重複チェック
					var bmncds_ = bmncds.filter(function (element, index, self) { return self.indexOf(element)*1 === index; });
					if(bmncds.length !== bmncds_.length){
						$.showMessage('E11040', ["部門"]);
						return false;
					}
					bmncds = [];
				}
			}

			// 入力情報を変数に格納
			if (rt == true) that.setGridData(that.getGridData(shuno, undefined, isChange, true));	// 更新用情報取得

			return rt;
		},

		updSuccess: function(id){	// validation OK時 の update処理
			var that = this;

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$.appendMask();
			$.appendMaskMsg();

			// 変更行情報取得
			var changedIndex = $($.id.hiddenChangedIdx).val().split(",");

			// 基本情報
			var targetData = that.grd_data;
			// 補足情報
			var targetDataOther = that.grd_data_other;

			// **** 個別データグリッド
			// 催しコード_レギュラー
			var targetRowsMoycdR= that.grd_moycd_r_data;
			// 催しコード_スポット
			var targetRowsMoycdS= that.grd_moycd_s_data;
			// 催しコード_特売
			var targetRowsMoycdT= that.grd_moycd_t_data;
			// ちらしのみ部門
			var targetRowsTokchirasbmn= that.grd_tokchirasbmn_data;
			// 全店特売（アンケート有）_基本
			var targetRowsToktg= that.grd_toktg_data;


			// 催しコード_レギュラー
			var targetRowsMoycdRDel= that.grd_moycd_r_del_data;
			// 催しコード_スポット
			var targetRowsMoycdSDel= that.grd_moycd_s_del_data;
			// 催しコード_特売
			var targetRowsMoycdTDel= that.grd_moycd_t_del_data;

			var targetRowsMoycdD = that.grd_moycd_r_del_data.concat(that.grd_moycd_s_del_data).concat(that.grd_moycd_t_del_data);

			// 全店特売（アンケート有）
			var targetRowsToktgDel= that.grd_toktg_del_data;


			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$.appendMaskMsg();
			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,			// レポート名
					action:			$.id.action_update,	// 実行処理情報
					obj:			id,					// 実行オブジェクト
					SENDBTNID:		that.sendBtnid,
					DATA:			JSON.stringify(targetData),				// 更新対象情報
					DATA_OTHER:		JSON.stringify(targetDataOther),		// 更新対象補足情報
					DATA_MOYCD_R:	JSON.stringify(targetRowsMoycdR),		// 個別データグリッド:催しコード-登録-レギュラー
					DATA_MOYCD_S:	JSON.stringify(targetRowsMoycdS),		// 個別データグリッド:催しコード-登録-スポット
					DATA_MOYCD_T:	JSON.stringify(targetRowsMoycdT),		// 個別データグリッド:催しコード-登録-特売
					DATA_MOYCD_D:	JSON.stringify(targetRowsMoycdD),		// 個別データグリッド:催しコード-削除
					DATA_TOKCHIRASBMN:JSON.stringify(targetRowsTokchirasbmn),	// 個別データグリッド:ちらしのみ部門-登録
					DATA_TOKTG:		JSON.stringify(targetRowsToktg),		// 個別データグリッド:全店特売（アンケート有）-登録
					DATA_TOKTG_D:	JSON.stringify(targetRowsToktgDel),		// 個別データグリッド:全店特売（アンケート有）-削除
					DEL_TOKMOYSYU:	that.grd_del_tokmoysyu,
					t:				(new Date()).getTime()
				},
				function(data){
					// 検索処理エラー判定
					if($.updError(id, data)) return false;

					var afterFunc = function(){
						// 初期化
						that.clear();
						that.changeReport(that.name, $.id.btn_upd);
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

			// 引き継ぎ週№
			this.jsonTemp.push({
				id:		$.id_inp.txt_shuno,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_shuno),
				text:	''
			})
		},
		setData: function(rows){		// データ表示
			var that = this;
			if(rows.length > 0){
				$('#'+that.focusRootId).find('[col^=F]').each(function(){
					var col = $(this).attr('col');
					if(rows[0][col]){
						if (["F1"].indexOf(col)!==-1) {
							if (rows[0]["F2"] && rows[0]["F2"]==='1') {
								$('[col='+col+']').combobox('setText',rows[0][col]);
							} else {
								var isExist = false;
								$('[col='+col+']').combobox('getData').filter(function(item, index){
									if(item.VALUE == rows[0][col]){
										isExist = true
									}
								});
								if(isExist){
									$.setInputboxValue($(this), rows[0][col]);
								}else{
									// 選択欄に値が存在しない場合は、値をテキストとして表示する
									$('[col='+col+']').combobox('loadData',[{"VALUE":rows[0][col],"TEXT":rows[0]["F9"]}]);
									$.setInputboxValue($(this), rows[0][col]);
								}
							}
						} else {
							$.setInputboxValue($(this), rows[0][col]);
						}
					}
				});
			}
		},
		setGrid: function(that, reportno, id, chk){		// データ表示
			var index = -1;
			var columns = that.getGridColumns(that, id);

			var funcBeforeLoad = function(param){
				index = -1;
				var json = that.getGridParams(that, id);
				// 情報設定
				param.page		=	reportno;
				param.obj		=	id;
				param.sel		=	(new Date()).getTime();
				param.target	=	id;
				param.action	=	$.id.action_init;
				param.json		=	JSON.stringify(json);
				param.datatype	=	"datagrid";
			};

			if(that.judgeRepType.sei_new || that.judgeRepType.sei_upd){
				that.editRowIndex[id] = -1;
				$('#'+id).datagrid({
					url:$.reg.easy,
					columns:columns,
					fit:true,
					singleSelect:true,
					checkOnSelect:false,
					selectOnCheck:false,
					onBeforeLoad:funcBeforeLoad,
					onLoadSuccess:function(data){
						if(index===-1){
							index=1;

							// メインデータ表示
							that.setData(that.baseData);

							if(chk && data !== undefined && data.rows !== undefined){
								for (var i=0; i<data.rows.length; i++){
									if(data.rows[i].SEL === $.id.value_on){
										$('#'+id).datagrid('checkRow', i);
									}
								}
							}
							// 情報保持
							var sel_shuno_ = $.getInputboxValue($('#'+$.id.sel_shuno));
							var gridData = that.getGridData(sel_shuno_, id);
							that.setGridData(gridData, id);
						}
						// チェックボックスの設定
						$.initCheckboxCss($('#'+id).datagrid('getPanel').find('.datagrid-body'));
					},
					onSelect:function(index){
						var rowr = $('#'+$.id.grd_moycd_r).datagrid("getSelected");
						var rows = $('#'+$.id.grd_moycd_s).datagrid("getSelected");
						var rowt = $('#'+$.id.grd_moycd_t).datagrid("getSelected");
						var rowIndexr = $('#'+$.id.grd_moycd_r).datagrid("getRowIndex", rowr);
						var rowIndexs = $('#'+$.id.grd_moycd_s).datagrid("getRowIndex", rows);
						var rowIndext = $('#'+$.id.grd_moycd_t).datagrid("getRowIndex", rowt);
						$('#'+$.id.grd_moycd_r).datagrid('endEdit',rowIndexr);
						$('#'+$.id.grd_moycd_s).datagrid('endEdit',rowIndexs);
						$('#'+$.id.grd_moycd_t).datagrid('endEdit',rowIndext);
						if(id ===$.id.grd_moycd_r){
							that.editRowIndex[$.id.grd_moycd_s] = -1;
							that.editRowIndex[$.id.grd_moycd_t] = -1;
						}
						if(id ===$.id.grd_moycd_s){
							that.editRowIndex[$.id.grd_moycd_r] = -1;
							that.editRowIndex[$.id.grd_moycd_t] = -1;
						}
						if(id ===$.id.grd_moycd_t){
							that.editRowIndex[$.id.grd_moycd_s] = -1;
							that.editRowIndex[$.id.grd_moycd_r] = -1;
						}
					},
					onClickRow: function(index,field){

						$.clickEditableDatagridCell(that,id, index)},
					onBeginEdit:function(index,row){
						$.beginEditDatagridRow(that,id, index, row);
						var param = {};

						if(!row){
							if($('#'+id).datagrid("getSelected")){
								row = $('#'+id).datagrid("getSelected");
							}else{
								return false;
							}
						}

						param["KEY"] =  "CNT";
						param["MOYSKBN"] = row["MOYSKBN"];
						param["MOYSSTDT"] = row["MOYSSTDT"];
						param["MOYSRBAN"] = row["MOYSRBAN"];
						for(var i = 0;i<20;i++){
								if(row["BMNCD_"+i]){
								param["BMNCD_"+i] = row["BMNCD_"+i];
							}
						}

						var chk_cnt = $.getInputboxData(that.name, $.id.action_check, $.id_inp.txt_bmncd, [param]);
						if(chk_cnt>0){
							$('#'+id).datagrid('getPanel').find("[datagrid-row-index='"+index+"']").find("[id^='"+$.id_inp.txt_bmncd+"_"+"']").each(function(){
								$.setInputBoxDisable($(this), true);
							})
							$.setInputBoxDisable($('#'+$.id.chk_del+"_"));
						}
						if(id===$.id.grd_moycd_t){
							if(row["MOYSRBAN"]*1 < 50 && row["MOYSRBAN"]!=""){
								$('#'+id).datagrid('getPanel').find("[datagrid-row-index='"+index+"']").find("[id^='"+$.id_inp.txt_bmncd+"_"+"']").each(function(){
									$.setInputBoxDisable($(this), true);
								})
							}
						}

						// 更新時使用不可
						if(that.judgeRepType.sei_upd){
							var isChange = false;
							if(row["UPDDT"]){
								// 催し開始日
								$.setInputBoxDisable($('#'+$.id_inp.txt_moysstdt+"_"));
								// 催し連番
								$.setInputBoxDisable($('#'+$.id_inp.txt_moysrbaninp+"_"));
							}
							if(row["USE"] === $.id.value_on || row["UPDDT"]===undefined){
								// 催し連番
								$.setInputBoxDisable($('#'+$.id.chk_del+"_"));
							}
						}
						$.setFocusFirst($('#'+id).datagrid('getPanel').find("[datagrid-row-index='"+index+"']"));
					},
					onEndEdit: function(index,row,changes){
						$.endEditDatagridRow(that, id, index, row)

						// IE使用時に共通処理setReadyKeyEvent内のendEditingDatagridが実行される前にvalidation処理が実行されてしまう為、
						// endEdit時にeditRowIndexを-1に設定する。
						that.editRowIndex[that.focusGridId] = -1;
					},
					onAfterEdit: function(index,row,changes){
						// チェックボックスの再追加（EndEdit時に削除されるため）
						$.afterEditAddCheckbox($('#'+id).datagrid('getPanel').find("[datagrid-row-index='"+index+"']"));
					}
				});
			}else{
				$('#'+id).datagrid({
					url:$.reg.easy,
					columns:columns,
					fit:true,
					singleSelect:true,
					checkOnSelect:false,
					selectOnCheck:false,
					onBeforeLoad:funcBeforeLoad,
					onLoadSuccess:function(data){
						if(chk){
							// チェックボックスの設定
							$.initCheckboxCss($('#'+id).datagrid('getPanel').find('.datagrid-body'));
						}
					},
				});
			}
		},
		getGridParams:function(that, id){
			var values = {};
			values["callpage"]	= $($.id.hidden_reportno).val();										// 呼出元レポート名
			values["SEL_SHUNO"]	= $.getJSONObject(this.jsonString, $.id_inp.txt_shuno).value;
			var moyskbn = $.id.value_moykbn_r;
			if(id===$.id.grd_moycd_s){
				moyskbn = $.id.value_moykbn_s;
			}else if(id===$.id.grd_moycd_t){
				moyskbn = $.id.value_moykbn_t;
			}
			values["MOYSKBN"]	= moyskbn;
			return [values];
		},
		extenxDatagridEditorIds:{
			MOYSRBAN		: "txt_moysrbaninp"		// テキスト（催し連番)
		},
		getGridColumns:function(that, id){
			var columns = [];
			var columnBottom=[];

			var fields = ["MOYSKBN","MOYSSTDT","MOYSRBAN","HBSTDT","HBSTDT_W","HBEDDT","HBEDDT_W","NNSTDT","NNSTDT_W","NNEDDT","NNEDDT_W","NENMATKBN","PLUSDDT","PLUSDDT_W","MOYKN","MOYAN"];
			var titles = ["","催しコード","","販売開始日","","販売終了日","","納入開始日","","納入終了日","","年末区分","PLU配信日","","催し名称（漢字）","催し名称（カナ）"];
			var bcstyler =function(value,row,index){return 'background-color:#f5f5f5;';};
			var dformatter =function(value){
				var add20 = value && value.length===6;
				return $.getFormatDt(value, add20);
			};
			var dparser = function(value){ return $.getParserDt(value, false);};
			var wformatter =function(value,row,index){return $.getFormatWeek(row[this.field.replace("_W", "")]);};
			var cstyler =function(value,row,index){return 'color: red;font-weight: bold;';};
			var cformatter =function(value,row,index){return value && value===$.id.value_on?$.id.text_on:$.id.text_off;};
			if(that.judgeRepType.sei_new||that.judgeRepType.sei_upd){
				if(that.judgeRepType.sei_upd){
					columnBottom.push({field:'DEL',	title:'削除',	width:35,	align:'center',	formatter:cformatter,	editor:{type:'checkbox'},	styler:cstyler});
					columnBottom.push({field:'USE',	title:'使用',	width:35,	align:'center',	formatter:cformatter,	styler:function(value,row,index){return 'background-color:#f5f5f5;color:red;font-weight: bold;';}});
				}
				columnBottom.push({field:fields[0],	title:titles[0],	width:20,	align:'center',	styler:bcstyler});
				columnBottom.push({field:fields[1],	title:titles[1],	width:72,	align:'left',	editor:{type:'numberbox'}});
				columnBottom.push({field:fields[2],	title:titles[2],	width:30,	align:'left',	formatter:function(value,row,index){ return $.getFormatLPad(value, $.len.rban);},	editor:{type:'numberbox'}});
				columnBottom.push({field:fields[3],	title:titles[3],	width:72,	align:'left',	formatter:dformatter,	styler:bcstyler,	editor:{type:'numberbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:true}}});
				columnBottom.push({field:fields[4],	title:titles[4],	width:30,	align:'left',	formatter:wformatter,	styler:bcstyler});
				columnBottom.push({field:fields[5],	title:titles[5],	width:72,	align:'left',	formatter:dformatter,	editor:{type:'numberbox'}});
				columnBottom.push({field:fields[6],	title:titles[6],	width:30,	align:'center',	formatter:wformatter,	styler:bcstyler});
				columnBottom.push({field:fields[7],	title:titles[7],	width:72,	align:'left',	formatter:dformatter,	editor:{type:'numberbox'}});
				columnBottom.push({field:fields[8],	title:titles[8],	width:30,	align:'center',	formatter:wformatter,	styler:bcstyler});
				columnBottom.push({field:fields[9],	title:titles[9],	width:72,	align:'left',	formatter:dformatter,	editor:{type:'numberbox'}});
				columnBottom.push({field:fields[10],title:titles[10],	width:30,	align:'center',	formatter:wformatter,	styler:bcstyler});
				columnBottom.push({field:fields[11],title:titles[11],	width:35,	align:'center',	formatter:cformatter,	editor:{type:'checkbox'},	styler:cstyler});
				if(id===$.id.grd_moycd_s||id===$.id.grd_moycd_t){
					columnBottom.push({field:fields[12],title:titles[12],width:72,	align:'left',	formatter:dformatter,	editor:{type:'numberbox'}});
					columnBottom.push({field:fields[13],title:titles[13],width:30,	align:'center',	formatter:wformatter,	styler:bcstyler});
				}
				columnBottom.push({field:fields[14],title:titles[14],	width:270,	halign:'center',	editor:{type:'textbox'}});
				columnBottom.push({field:fields[15],title:titles[15],	width:200,	halign:'center',	editor:{type:'textbox'}});
				if(id===$.id.grd_moycd_t){
					var colnum = 20;
					for (var i=1; i<=colnum; i++){
						columnBottom.push({field:'BMNCD_'+i,title:'部門'+i,width:50,	halign:'center',	formatter:function(value,row,index){ return $.getFormatLPad(value, $.len.bmncd);},	editor:{type:'numberbox'}});
					}
				}
			}else{
				columnBottom.push({field:'USE',	title:'使用',		width:35,	align:'left',	formatter:cformatter,	styler:function(value,row,index){return 'background-color:#f5f5f5;color:red;font-weight: bold;';}});
				columnBottom.push({field:fields[0],	title:titles[0],	width:20,	align:'left',	styler:bcstyler});
				columnBottom.push({field:fields[1],	title:titles[1],	width:72,	align:'left'});
				columnBottom.push({field:fields[2],	title:titles[2],	width:30,	align:'left',	formatter:function(value,row,index){ return $.getFormatLPad(value, $.len.rban);}});
				columnBottom.push({field:fields[3],	title:titles[3],	width:72,	align:'left',	formatter:dformatter,	styler:bcstyler});
				columnBottom.push({field:fields[4],	title:titles[4],	width:30,	align:'left',	formatter:wformatter,	styler:bcstyler});
				columnBottom.push({field:fields[5],	title:titles[5],	width:72,	align:'left',	formatter:dformatter});
				columnBottom.push({field:fields[6],	title:titles[6],	width:30,	align:'left',	formatter:wformatter,	styler:bcstyler});
				columnBottom.push({field:fields[7],	title:titles[7],	width:72,	align:'left',	formatter:dformatter});
				columnBottom.push({field:fields[8],	title:titles[8],	width:30,	align:'left',	formatter:wformatter,	styler:bcstyler});
				columnBottom.push({field:fields[9],	title:titles[9],	width:72,	align:'left',	formatter:dformatter});
				columnBottom.push({field:fields[10],title:titles[10],	width:30,	align:'left',	formatter:wformatter,	styler:bcstyler});
				columnBottom.push({field:fields[11],title:titles[11],	width:40,	align:'left',	formatter:cformatter,	styler:cstyler});
				if(id===$.id.grd_moycd_s||id===$.id.grd_moycd_t){
					columnBottom.push({field:fields[12],title:titles[12],width:72,	halign:'center',	formatter:dformatter});
					columnBottom.push({field:fields[13],title:titles[13],width:30,	halign:'center',	formatter:wformatter,	styler:bcstyler});
				}
				columnBottom.push({field:fields[14],title:titles[14],	width:270,	halign:'center'});
				columnBottom.push({field:fields[15],title:titles[15],	width:200,	halign:'center'});
				if(id===$.id.grd_moycd_t){
					var colnum = 20;
					for (var i=1; i<=colnum; i++){
						columnBottom.push({field:'BMNCD'+i,title:'部門'+i,width:60,	halign:'center',	formatter:function(value,row,index){ return $.getFormatLPad(value, $.len.bmncd);}});
					}
				}
			}
			columns.push(columnBottom);
			return columns;

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
			var sendMode = "";		// 遷移後に、前回検索条件を表示したい場合、""のまま、ここで設定した条件を表示したい場合"1"

			// タブ要素(a)取得
			var elems = $('#tabContent', window.parent.document).map(
				function(i,e) {
					return e;
				}).get();
			var href = elems[0].value.split(',');

			// JSON Object Clone ()
			var sendJSON = [];
			$.setJSONObject(sendJSON, 'sendBtnid', btnId, $('#'+btnId).attr("title"));					// 実行ボタン情報保持
			$.setJSONObject(sendJSON, 'callpage', that.name, that.name);								// 呼出し元レポート情報

			// 戻る実行時用に現在の画面情報を保持する
			var states = $.getBackBaseJSON(that);
			var newrepinfos = $.getBackJSON(that, states, false);
			$.setJSONObject(sendJSON, 'repinfo', newrepinfos, '');

			// 実行ボタン別処理
			switch (btnId) {
			case $.id.btn_back:
			case $.id.btn_cancel:
			case $.id.btn_upd:
			case $.id.btn_del:
				// 転送先情報
				index = 1;
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
			if(id+"_"===obj.attr('id') && that.focusGridId!==""){
				parentObj = $('#'+that.focusGridId).datagrid('getPanel');
			}

			// DB問い合わせ系
			if($('[for_inp^='+id+'_]').length > 0){
				var param = that.getInputboxParams(that, id, newValue);
				$.getsetInputboxRowData(that.name, 'for_inp', id, param, that, parentObj);
			}

			// 検索、入力後特殊処理
			if(that.queried){
				var msgid = null;


				// 週No.
				if(id===$.id_inp.txt_shuno){
					if(that.selectShuno!==newValue) {
						that.selectShuno=newValue;
						if ($('#'+$.id.sel_shuno).combobox('panel')[0].scrollHeight===0) {
							msgid = that.checkInputboxFunc(id, newValue, '');
							id=$.id.sel_shuno;
						}
					}
				}

				// 週No.
				if(id===$.id.sel_shuno && newValue!=='-1' && newValue.length >= 4){
					if((that.selectShuno!==newValue)) {
						that.selectShuno=newValue;
						if ($('#'+id).combobox('panel')[0].scrollHeight===0) {
							msgid = that.checkInputboxFunc($.id_inp.txt_shuno, newValue, '');
							id=$.id.sel_shuno;
						}
					}
				}

				// 特別週フラグ
				if(id===$.id.chk_tshuflg){
					msgid = that.checkInputboxFunc(id, newValue, '');
				}
				if(msgid !==null){

					if (msgid==="E20207" || msgid==="EX1123") {
						id=$.id.chk_tshuflg;
					}

					$.showMessage(msgid, undefined, function(){$.addErrState(that, $('#'+id), true)});
					return false;
				}

//				// グリッド切換
//				if(id===$.id.sel_shuno||id===$.id_inp.txt_shuno){
//					$('#'+ $.id.grd_moycd_r).datagrid('reload');
//					$('#'+ $.id.grd_moycd_s).datagrid('reload');
//					$('#'+ $.id.grd_moycd_t).datagrid('reload');
//				}

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
						if (msgid.split(",").length===2) {
							$.showMessage(msgid.split(",")[0], [msgid.split(",")[1]], function(){$.addErrState(that, $('#'+that.focusGridId), true, {NO:that.editRowIndex[that.focusGridId], ID:id})});
						} else {
							$.showMessage(msgid, undefined, function(){$.addErrState(that, $('#'+that.focusGridId), true, {NO:that.editRowIndex[that.focusGridId], ID:id})});
						}
						return false;
					}
					var rowobj= $('#'+that.focusGridId).datagrid('getPanel').find("[datagrid-row-index="+that.editRowIndex[that.focusGridId]+"]");

					// 催し開始日(催しコード)
					if(id===$.id_inp.txt_moysstdt){
						var tshuflg = $.getInputboxValue($('#'+$.id.chk_tshuflg), true);

						if(that.focusGridId === $.id.grd_moycd_r){			// 催しコード_レギュラー
							row["MOYSKBN"] = $.id.value_moykbn_r;
							// ●販売期間のデフォルト値
							$('#'+$.id_inp.txt_hbeddt+'_').numberbox('setValue',"");
							$('#'+$.id_inp.txt_hbstdt+'_').numberbox('setValue',newValue);
							// ●納入期間のデフォルト値
							$('#'+$.id_inp.txt_nneddt+'_').numberbox('setValue',"");
							$('#'+$.id_inp.txt_nnstdt+'_').numberbox('setValue',that.calcDefNnstdt(tshuflg, newValue));
						}else if(that.focusGridId === $.id.grd_moycd_s){	// 催しコード_スポット
							row["MOYSKBN"] = $.id.value_moykbn_s;
							// ●販売期間のデフォルト値
							$('#'+$.id_inp.txt_hbeddt+'_').numberbox('setValue',"");
							$('#'+$.id_inp.txt_hbstdt+'_').numberbox('setValue',newValue);
							// ●納入期間のデフォルト値
							$('#'+$.id_inp.txt_nneddt+'_').numberbox('setValue',"");
							$('#'+$.id_inp.txt_nnstdt+'_').numberbox('setValue',that.calcDefNnstdt(tshuflg, newValue));
							// ●ＰＬＵ配信日のデフォルト値
							$('#'+$.id_inp.txt_plusddt+'_').numberbox('setValue',that.calcDefPlusddt(newValue));

						}else if(that.focusGridId === $.id.grd_moycd_t){	// 催しコード_特売
							row["MOYSKBN"] = $.id.value_moykbn_t;
							// ●販売期間のデフォルト値
							$('#'+$.id_inp.txt_hbeddt+'_').numberbox('setValue',"");
							$('#'+$.id_inp.txt_hbstdt+'_').numberbox('setValue',newValue);
							// ●納入期間のデフォルト値
							$('#'+$.id_inp.txt_nneddt+'_').numberbox('setValue',"");
							$('#'+$.id_inp.txt_nnstdt+'_').numberbox('setValue',that.calcDefNnstdt(tshuflg, newValue));
							// ●ＰＬＵ配信日のデフォルト値
							$('#'+$.id_inp.txt_plusddt+'_').numberbox('setValue',that.calcDefPlusddt(newValue));
						}
						rowobj.find('[field=MOYSKBN]').find('div').text(row["MOYSKBN"]);
					}

					// 販売期間終了日
					if(id===$.id_inp.txt_hbeddt){
						var oldvalue =  $.getInputboxValue($('#'+$.id_inp.txt_nneddt+'_'));
						if(oldvalue.length===0){
							if(that.focusGridId === $.id.grd_moycd_r){			// 催しコード_レギュラー
								// ●納入期間のデフォルト値
								$('#'+$.id_inp.txt_nneddt+'_').numberbox('setValue',newValue);
							}else if(that.focusGridId === $.id.grd_moycd_s){	// 催しコード_スポット
								// ●納入期間のデフォルト値
								$('#'+$.id_inp.txt_nneddt+'_').numberbox('setValue',newValue);

							}else if(that.focusGridId === $.id.grd_moycd_t){	// 催しコード_特売
								// ●納入期間のデフォルト値
								$('#'+$.id_inp.txt_nneddt+'_').numberbox('setValue',newValue);
							}
						}
					}

					// 催し連番
					if(id===$.id_inp.txt_moysrbaninp){
						if(that.focusGridId === $.id.grd_moycd_t){	// 催しコード_特売
							var oldvalue =  $.getInputboxValue($('#'+$.id_inp.txt_bmncd+'_1_'));
							if(oldvalue.length===0){
								var rows = $.getSelectListData(that.name, $.id.action_change, id, [{}]);
								for (var i = 0; i < rows.length; i++){
									$('#'+$.id_inp.txt_bmncd+'_'+(i+1)+'_').numberbox('setValue',rows[i]["VALUE"]);
								}
							}

							if(newValue*1 >= 50){
								$('#'+$.id.grd_moycd_t).datagrid('getPanel').find("[datagrid-row-index='"+that.editRowIndex[that.focusGridId]+"']").find("[id^='"+$.id_inp.txt_bmncd+"_"+"']").each(function(){
									$.setInputBoxEnable($(this));
								})

							}else if(newValue*1 < 50){
								$('#'+$.id.grd_moycd_t).datagrid('getPanel').find("[datagrid-row-index='"+that.editRowIndex[that.focusGridId]+"']").find("[id^='"+$.id_inp.txt_bmncd+"_"+"']").each(function(){
									$.setInputboxValue($(this),'');
									$.setInputBoxDisable($(this), true);
								})
							}
						}
					}
					// 曜日設定系
					var objids = [$.id_inp.txt_hbstdt,$.id_inp.txt_hbeddt,$.id_inp.txt_nnstdt,$.id_inp.txt_nneddt,$.id_inp.txt_plusddt];
					var fields = ["HBSTDT_W","HBEDDT_W","NNSTDT_W","NNEDDT_W","PLUSDDT_W"];
					if(objids.indexOf(id) > -1){
						var wfield = fields[objids.indexOf(id)];
						if(wfield!==""){
							row[wfield] = $.getFormatWeek(newValue, true);
							rowobj.find('[field='+wfield+']').find('div').text(row[wfield]);
						}
					}
				}
			}
		},
		// IDとvalueでチェック処理を実施
		checkInputboxFunc:function(id, newValue, kbn, record, isNew){
			var that = this;
			var sdt, edt;

			// 週No./特別週フラグ
			if(id===$.id_inp.txt_shuno||id===$.id.chk_tshuflg){
				var txt_shuno = id===$.id_inp.txt_shuno ? newValue : $('#'+$.id_inp.txt_shuno).numberbox('getValue')+'';
				var chk_tshuflg = id===$.id.chk_tshuflg ? newValue : $.getInputboxValue($('#'+$.id.chk_tshuflg));
				var obj = $('#'+$.id.sel_shuno);

				// チェックの必要なし
				if(!(!$.isEmptyVal(txt_shuno) && txt_shuno!=='-1')){
					return null;
				}

				// 入力がおかしい場合、比較は終了
				if(txt_shuno.length > 4){
					return "EX1121";
				}

				if(id===$.id_inp.txt_shuno && that.judgeRepType.sei_new){
					// マスタ存在チェック
					var param = {};
					param["KEY"] =  "MST_CNT";
					param["value"] = newValue;

					var chk_cnt = $.getInputboxData(that.name, $.id.action_check, id, [param]);
					if(!(chk_cnt==="" || chk_cnt==="0")){
						// 既存週Noが週マスタに存在します。
						obj.combobox('disableValidation');
						return "E20364";
					}
				}

				if(chk_tshuflg===$.id.value_on && txt_shuno.substr(2,2)*1 < 54){
					return "E20207";
				}else if(chk_tshuflg===$.id.value_off && txt_shuno.substr(2,2)*1 >= 54){
					return "EX1123";
				}

				if (chk_tshuflg===$.id.value_off) {
					that.getComboSel(that);
				}

				return null;
			}


			// 各種グリッドチェック
			var moysstdt;
			var hbstdt, hbeddt;
			var nnstdt, nneddt;
			if(record){
				moysstdt=record["F2"];
				hbstdt = record["F8"];
				hbeddt = record["F9"];
				nnstdt = record["F10"];
				nneddt = record["F11"];
			}else{
				moysstdt=id===$.id_inp.txt_moysstdt?newValue : $('#'+$.id_inp.txt_moysstdt+"_").numberbox('getValue');
				hbstdt = id===$.id_inp.txt_hbstdt ? newValue : $('#'+$.id_inp.txt_hbstdt+"_").numberbox('getValue');
				hbeddt = id===$.id_inp.txt_hbeddt ? newValue : $('#'+$.id_inp.txt_hbeddt+"_").numberbox('getValue');
				nnstdt = id===$.id_inp.txt_nnstdt ? newValue : $('#'+$.id_inp.txt_nnstdt+"_").numberbox('getValue');
				nneddt = id===$.id_inp.txt_nneddt ? newValue : $('#'+$.id_inp.txt_nneddt+"_").numberbox('getValue');
			}

			// 催しコード
			if((id===$.id_inp.txt_moysstdt||id===$.id_inp.txt_moysrbaninp)&& isNew){
				var param = {};
				param["KEY"] =  "CNT";
				var moysstdt = "";
				var moysrbaninp = "";
				if(record){
					moysstdt=record["F2"];
					moysrbaninp = record["F3"];
				}else{
					moysstdt = $('#'+$.id_inp.txt_moysstdt+"_").numberbox('getValue');
					moysrbaninp = $('#'+$.id_inp.txt_moysrbaninp+"_").numberbox('getValue');
				}
				if(moysstdt!=""&&moysrbaninp!=""){
					param["MOYSKBN"] = kbn;
					param["MOYSSTDT"] = moysstdt;
					param["MOYSRBAN"] = moysrbaninp;
					var chk_cnt = $.getInputboxData(that.name, $.id.action_check, 'MOYOKBN', [param]);
					if(chk_cnt!==""&&chk_cnt!=="0"){
						return "E20275";
					}
				}
			}

			// 催し連番
			if(id===$.id_inp.txt_moysrbaninp && isNew){
				if(newValue.length === 0){
					return "E00001";
				}
				if(kbn !== $.id.value_moykbn_t*1){
					if(newValue.length === 0 || newValue*1 < 0 || newValue*1 > 49 ){
						return "E20141";
					}
				}
			}

			// 必須項目
			if(id===$.id_inp.txt_moykn||id===$.id_inp.txt_moyan){
				if(newValue.length === 0){
					return "E00001";
				}
			}

			// 販売期間
			if(id===$.id_inp.txt_hbstdt||id===$.id_inp.txt_hbeddt){
				// 販売期間開始日
				if(id===$.id_inp.txt_hbstdt){
					if(moysstdt!==hbstdt){
						return "E20080";
					}
					// 処理日付
					if(isNew){
						var shoridt = $('#'+$.id.txt_shoridt).val();
						if(shoridt!=='' && ('20'+hbstdt)*1 <= shoridt*1 ){
							return "EX1124";
						}
					}
					// 終了日が未入力の場合、比較は終了
					if(hbeddt.length < 6){ return null; }
				}

				// 販売期間終了日
				if(id===$.id_inp.txt_hbeddt){
					// 開始日が未入力の場合、比較は終了
					if(hbstdt.length < 6){ return null; }
				}

				sdt = $.convDate(hbstdt, true);
				edt = $.convDate(hbeddt, true);
				if(sdt.getTime() > edt.getTime()){	// 期間が逆の場合
					return "E20006";
				}

				// 期間日数チェック
				var days = $.getDateDiffDay(sdt, edt);
				if(days >= 11){
					return "E20068,販売期間";
				}
			}

			// 納入期間
			if(id===$.id_inp.txt_nnstdt||id===$.id_inp.txt_nneddt){
				// 納入期間開始日
				if(id===$.id_inp.txt_nnstdt){
					// 販売期間開始日との比較
					if(hbstdt.length >= 6){
						sdt = $.convDate(hbstdt, true);
						edt = $.convDate(nnstdt, true);
						if($.getDateDiffDay(sdt, edt) < -7 ){
							return "E20069";
						}
					}
					// 販売期間終了日との比較
					if(hbeddt.length >= 6){
						sdt = $.convDate(hbeddt, true);
						edt = $.convDate(nnstdt, true);
						if($.getDateDiffDay(sdt, edt) < -10 ){
							return "E20070";
						}
					}
					// 終了日が未入力の場合、比較は終了
					if(nneddt.length < 6){ return null; }
				}

				// 納入期間終了日
				if(id===$.id_inp.txt_nneddt){
					// 販売期間終了日との比較
					if(hbeddt.length >= 6){
						sdt = $.convDate(nnstdt, true);
						edt = $.convDate(nneddt, true);

						// 期間日数チェック
						var days = $.getDateDiffDay(sdt, edt);
						if(days >= 11){
							return "E20068,納入期間";
						}

						sdt = $.convDate(hbeddt, true);
						edt = $.convDate(nneddt, true);

						var sa = $.getDateDiffDay(sdt, edt);
						if($.getDateDiffDay(sdt, edt) > 1 ){
							return "E20071";
						}
					}
					// 販売期間開始日との比較
					if(hbstdt.length >= 6){
						sdt = $.convDate(hbstdt, true);
						edt = $.convDate(nneddt, true);
						var sa = $.getDateDiffDay(sdt, edt);
						if($.getDateDiffDay(sdt, edt) > 10 ){
							return "E20072";
						}
					}
					// 開始日が未入力の場合、比較は終了
					if(nnstdt.length < 6){ return null;}
				}

				sdt = $.convDate(nnstdt, true);
				edt = $.convDate(nneddt, true);

				if(sdt.getTime() > edt.getTime()){	// 期間が逆の場合
					return "E20301";
				}

				// 期間日数チェック
				var days = $.getDateDiffDay(sdt, edt);
				if(days >= 11){
					return "E20068";
				}

				// ③≦納入期間開始日の翌週火曜日　（＊１）　…③
				var tshuflg = $.getInputboxValue($('#'+$.id.chk_tshuflg), true);
				if(tshuflg!==$.id.value_on){
					var chkdt = that.calcChkNneddtObj(nnstdt);
					if(chkdt.getTime() < edt.getTime()){
						return "E20073";
					}
				}
			}
			// PLU配信日
			if(id===$.id_inp.txt_plusddt && kbn !== $.id.value_moykbn_r*1){
				var plusddt = record ? record["F14"] : newValue;
				if(hbstdt.length >= 6){
					sdt = $.convDate(hbstdt, true);
					edt = $.convDate(plusddt, true);
					if($.getDateDiffDay(sdt, edt) < -14
					|| $.getDateDiffDay(sdt, edt) > -2){
						return "E20075";
					}
				}
				// 処理日付
				if(isNew){
					var shoridt = $('#'+$.id.txt_shoridt).val();
					if(shoridt!=='' && ('20'+plusddt)*1 < shoridt*1 ){
						return "E20013";
					}
				}
			}
			return null;
		},
		// 納入期間開始日のデフォルト値算出
		calcDefNnstdt: function(tshuflg, newValue){
			if(newValue.length === 0){ return ''}
			if(tshuflg!==$.id.value_on){
				// 販売期間開始日以前の直近の日曜日　（＊２）
				var dObj = $.convDate(newValue, true);
				for (var i = 0; i < 7; i++){
					if(dObj.getDay() === 0){
						break;
					}else{
						dObj.setDate(dObj.getDate() - 1);
					}
				}
				return $.dateFormat(dObj);
			}
			return newValue;
		},
		// 納入期間終了日のチェック用日付算出
		calcChkNneddtObj: function(newValue){
			// 納入期間開始日の翌週火曜日（（＊１）　但し、納入期間開始日が日曜日の場合は、翌々週の火曜日）
			var dObj = $.convDate(newValue, true);
			var max = dObj.getDay() === 0 ? 14 : 7;
			dObj.setDate(dObj.getDate() + max);
			if(dObj.getDay() == 1){
				dObj.setDate(dObj.getDate() + 6);
			}
			for (var i = 0; i < 7; i++){
				if(dObj.getDay() === 2){
					break;
				}
				dObj.setDate(dObj.getDate() - 1);
			}
			return dObj;
		},
		// ＰＬＵ配信日のデフォルト値算出
		calcDefPlusddt: function(newValue){
			var dObj = $.convDate(newValue, true);
			dObj.setDate(dObj.getDate() - 7);
			return $.dateFormat(dObj);
		},
		getInputboxParams: function(that, id, newValue){
			// 情報取得
			var values = {};
			values["value"] = newValue;
			// 情報設定
			return [values];
		},
		getComboSel:function(that){

			var obj = $('#'+$.id.sel_shuno);
			var val = obj.next().children('.textbox-value').val();
			var data = obj.combobox('getData');
			var txt = obj.combobox('getText');
			var flg = false;

			for (var i = 0; i < data.length; i++) {

				var dataVal = data[i].VALUE;
				if (val*1===dataVal*1) {
					val = dataVal;
				}

				if((data[i].VALUE === val)) {
					flg = true;
					break;
				}
			}


			var txt_shuno = $('#'+$.id_inp.txt_shuno).numberbox('getValue')+'';
			if (!flg || (flg && val==='-1')) {
				obj.combobox('enableValidation');
				if (val!=='-1') {
					$.showMessage("E11302",["入力値"],function () {$.addErrState(this,obj,false)});
					obj.combobox('reload');
					obj.combobox('hidePanel');
				}
			} else {
				obj.combobox('disableValidation');
			}
		}
	} });
})(jQuery);