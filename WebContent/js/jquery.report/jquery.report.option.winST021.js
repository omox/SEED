/**
 * jquery sub window option
 * 店別数量(ST021)
 * TG016からのみ呼び出す想定
 */
;(function($) {

$.extend({

	winST021: {
		name: 'Out_ReportwinST021',
		name2:'Out_ReportST021',
		prefix:'_tenbetusu',
		suffix:'_winST021',
		initializes: true,		// 初期化フラグ（全体）
		initializesCond: true,	// 初期化フラグ（条件用）
		lastIndex: -1,			// 編集位置（行）
		sortName: '',			// ソート項目名
		sortOrder: '',			// ソート順
		timeData : (new Date()).getTime(),
		queried : false,
		callreportno:"",		// 呼出し元レポートNo
		callBtnid: "",			// 呼出ボタンID情報
		callreportHidden:[],	// 呼出し元レポートからの引き継ぎ情報
		focusRootId:"_winST021",// （キー移動時必須）キー移動イベントのルートとなるパネルのID
		focusParentId:"",		// （キー移動時必須）現在フォーカスがあたっている項目の親となるパネルのID
		focusGridId:"",			// （キー移動時必須）現在フォーカスがあたっているDataGridのID
		editRowIndex:{},		// グリッド編集行保持

		changeInputInfo:[],		// 変更された入力項目の情報保持

		cmnParam:{},			// 基本パラメータ
		nndtData:[],			// 納入日基本データ
		baseData:[],			// 検索結果

		grd_data:[],			// メイン情報：

		judgeRepType: {
			toktg			: false,	// アンケート有
			toksp			: false,	// アンケート無

			ref				: false		// 参照
		},
		init: function(js) {
			var that = this;
			if(!that.initializes) return false;

			that.callreportno = js.name;
			that.callreportHidden = js.jsonHidden;
			// 帳票タイプ判断：引継情報で判断
			that.judgeRepType.toktg = js.judgeRepType.toktg;
			that.judgeRepType.toksp = js.judgeRepType.toksp;
			that.judgeRepType.ref = js.judgeRepType.ref;

			// 呼出しボタンイベント設定
			$('[id^=btn'+that.prefix+']').each(function(){
				var id = $(this).attr('id');
				$('#'+id).click(function() { that.Open(this, js); });
			});

			// 戻る
			$('#'+$.id.btn_back+that.suffix).on("click", that.Back);

			if(js.reportYobiInfo()==='1'){
				$.setInputBoxDisable($('#'+$.id.btn_cancel+that.suffix)).hide();
				$.setInputBoxDisable($('#'+$.id.btn_upd+that.suffix)).hide();
				$.setInputBoxDisable($($.id.hiddenChangedIdx+that.suffix));
			}else{
				$('#'+$.id.btn_cancel+that.suffix).on("click", that.Back);	// キャンセル
				$('#'+$.id.btn_upd+that.suffix).on("click", that.Update);	// 更新
			}

			// ｳｲﾝﾄﾞｳ設定
			$('#'+that.suffix).window({
				iconCls:'icon-search',
				modal:true,
				collapsible:false,
				minimizable:false,
				maximizable:false,
				closed:true,
				cinline:false,
				zIndex:90000,
				height:600,
				title:'特売・スポット計画　店別数量(ST021)',
				onBeforeOpen:function(){
					// ウインドウ展開中リサイズイベント無効化
					$.reg.resize = false;
					js.focusParentId = that.suffix;
				},
				onOpen:function(){
					$('#'+that.focusRootId).find('[tabindex]').filter("[tabindex!=-1]").filter('[disabled!=disabled]').filter(":visible").eq(0).focus();
				},
				onBeforeClose:function(){
					// ウインドウ展開中リサイズイベント有効化
					$.reg.resize = true;
					that.Clear();
					js.focusParentId = js.focusRootId;
				},
				onClose:function(){
					$('#'+js.focusParentId).find('#'+that.callBtnid).focus();
				}
			});

			that.initializes = !that.initializes;
		},
		Open: function(obj, js) {
			if ($(obj).is("disabled","disabled"))	return false;
			var that = $.winST021;
			that.callBtnid = $(obj).attr('id');

			//
			var chks =$('input[name="'+$.id.rad_sel+'"]:checked');
			if(chks.length===0){
				$.showMessage('E40081');
				return false;
			}

			// 3.12.1.1．新たにチェックをつけた納入日においては店別数量画面へ遷移出来ない。※UPDATE前提
			// 必須パラメータ：催しコード、部門、管理番号、枝番、納入日
			var isError = false;
			// 納入日
			var idx = chks.eq(0).attr("id").replace($.id.rad_sel, "");
			// 情報設定
			that.cmnParam = {
				MOYSKBN:	$.getJSONValue(that.callreportHidden, $.id_inp.txt_moyskbn),		// 催し区分
				MOYSSTDT:	$.getJSONValue(that.callreportHidden, $.id_inp.txt_moysstdt),		// 催し区分
				MOYSRBAN:	$.getJSONValue(that.callreportHidden, $.id_inp.txt_moysrban),		// 催し連番
				BMNCD:		$.getJSONValue(that.callreportHidden, $.id_inp.txt_bmncd),			// 部門コード
				KANRINO:	$.getJSONValue(that.callreportHidden, $.id_inp.txt_kanrino),		// 管理No.
				KANRIENO:	$.getJSONValue(that.callreportHidden, $.id_inp.txt_kanrieno),		// 管理No.枝番
				NNDT:		$.getInputboxText($('[col=N90_'+idx+']')),							// 納入日
				SHNCD:		$.getInputboxValue($('#'+$.id_inp.txt_shncd)),						// 商品
				BINKBN:		$.getInputboxValue($('#'+$.id_inp.txt_binkbn))						// 便区分
			};
			var paramnms = Object.getOwnPropertyNames(that.cmnParam);
			for ( var nm in paramnms ) {
				if($.isEmptyVal(that.cmnParam[paramnms[nm]])){
					isError = true;
					break;
				}
			}
			// 納入日テーブル存在チェック
			if(!isError){
				isError = $.isEmptyVal($.getInputboxText($('[col=N50_'+idx+']')));
			}
			if(isError){
				//E20376	新たにチェックをつけた納入日は、店別数量画面へ遷移できません。	 	0	 	E
				$.showMessage('E20376');
				return false;
			}

			if(that.initializesCond){
				// 複数項目
				$('#'+that.focusRootId).find('[id^='+$.id_inp.txt_htasu+that.suffix+']').each(function(){
					var id = $(this).attr('id');
					$.setInputbox(that, that.name, id , true);
				});
				// チェックボックスの設定
				// $.initCheckboxCss($("#"+that.focusRootId));
				// キーイベントの設定
				$.initKeyEvent(that);

				that.initializesCond = false;
			}

			// 画面情報表示
			$('#'+that.focusRootId).find('[id]').filter('span').each(function(){
				var refid = $(this).attr('id').replace(that.suffix, '');
				if($('#'+refid)){
					$(this).text($.getInputboxText($('#'+refid)));
				}
			});
			$.setInputboxValue($('#'+$.id_inp.txt_nndt+that.suffix),$.getInputboxText($('[col=N91_'+idx+']')));		// 納入日設定
			var suryo = $.isEmptyVal($.getInputboxText($('[col=N5_'+idx+']'))) ? '0' : $.getInputboxText($('[col=N5_'+idx+']'));
			$.setInputboxValue($('#'+$.id_inp.txt_suryo+1+that.suffix),suryo);	// 発注総数設定

			// dataGrid 初期化
			//this.setDataGrid('grd'+that.prefix+that.suffix);

			that.Search();

			// window 表示
			$('#'+that.suffix).window('open');
		},
		Clear:function(){
			var that = $.winST021;
			that.initializesCond = true;

			$('#'+that.focusRootId).find('[col^=HTASU]').each(function(){
				$.setInputboxValue($(this), '');
			});

			$($.id.hiddenChangedIdx+that.suffix).val("");
			// グリッド初期化
			//$('#grd'+that.prefix+that.suffix).datagrid('clearSelections').datagrid({data: []}).datagrid('getPager').pagination('select', 1);	// ページ初期化
			that.initializesCond = false;
		},
		Search: function(){
			var that = $.winST021;

			// validate=falseの場合何もしない
			if(!that.validation()){ return false; }

			// セッションタイムアウト、利用時間外の確認
			var isTimeout = $.checkIsTimeout();
			if (! isTimeout) {
				// 検索実行
				that.success("grd"+that.prefix+that.suffix);
			}

			return true;
		},
		Update: function(){
			var that = $.winST021;

			// validate=falseの場合何もしない
			if(!that.updValidation()){ return false; }

			// 変更情報チェック
			if(!$.getConfirmUnregistFlg($($.id.hiddenChangedIdx+that.suffix))){
				$.showMessage('E20582');
				return false;
			}

			var func_ok = function(r){
				// セッションタイムアウト、利用時間外の確認
				var isTimeout = $.checkIsTimeout();
				if (! isTimeout) {
					that.updSuccess("grd"+that.prefix+that.suffix);
				}
				return true;
			};
			// W20014	戻った画面のキャンセルで取り消しできません。登録しますか？	 	4	 	E
			$.showMessage("W20014", undefined, func_ok);

			return true;
		},
		Back:function(){
			var that = $.winST021;
			if($.getConfirmUnregistFlg($($.id.hiddenChangedIdx+that.suffix))){
				var func_ok = function(r){
					$('#'+that.suffix).window('close');
					return true;
				};
				$.showMessage("E11025", undefined, func_ok);
			}else{
				$('#'+that.suffix).window('close');
			}
			return true;
		},
		Select: function(){
			var that = $.winST021;

			var row = $("#grd"+that.prefix+that.suffix).datagrid("getSelected");
			if(!row){
				$.showMessage('E00008');
				return false;
			}

			// 取得した情報を、オブジェクトに設定する
			// 設定先の判定：オブジェクトに for_btn,for_inpタグなどを使用して呼出し元(呼出しボタン名)と列名が設定されている項目
			var isSet = $.setInputboxRowData('for_btn', that.callBtnid, row);
			if(isSet){
				$('#'+that.suffix).window('close');
			}
			return true;
		},
		validation: function (){	// （必須）批准
			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform+that.suffix).form('validate');
			return rt;
		},
		success: function(id){	// 検索処理
			var that = $.winST021;
			// 処理時間計測用
			that.timeData = (new Date()).getTime();

			// グリッド初期化&ローディング
			$('#'+id).datagrid('loading');

			// 基本情報再取得
			var json1 = that.getGridParams(that, "KEY", "BASE");
			$.post(
				$.reg.easy,
				{
					page	:	that.name,										// レポート名
					obj		:	id,
					sel		:	(new Date()).getTime(),
					target	:	id,
					action	:	$.id.action_init,
					json	:	JSON.stringify(json1)
				},
				function(data){
					if(data!==""){
						// JSONに変換
						var json = JSON.parse(data);
						var rows = json.rows;
						if(rows.length > 0){
							var i = 0;
							that.nndtData = {
									MOYSKBN			: rows[i]["MOYSKBN"],			// MOYSKBN	催し区分
									MOYSSTDT		: rows[i]["MOYSSTDT"],			// MOYSSTDT	催し開始日
									MOYSRBAN		: rows[i]["MOYSRBAN"],			// MOYSRBAN	催し連番
									BMNCD			: rows[i]["BMNCD"],				// BMNCD	部門
									KANRINO			: rows[i]["KANRINO"],			// KANRINO	管理番号
									KANRIENO		: rows[i]["KANRIENO"],			// KANRIENO	枝番
									NNDT			: rows[i]["NNDT"],				// NNDT	納入日
									TENHTSU_ARR		: rows[i]["TENHTSU_ARR"],		// TENHTSU_ARR	店発注数配列
									TENCHGFLG_ARR	: rows[i]["TENCHGFLG_ARR"],		// TENCHGFLG_ARR	店変更フラグ配列
									HTASU			: rows[i]["HTASU"],				// HTASU	発注総数
									PTNNO			: rows[i]["PTNNO"],				// PTNNO	パターン№
									TSEIKBN			: rows[i]["TSEIKBN"],			// TSEIKBN	訂正区分
									TPSU			: rows[i]["TPSU"],				// TPSU	店舗数
									TENKAISU		: rows[i]["TENKAISU"],			// TENKAISU	展開数
									ZJSKFLG			: rows[i]["ZJSKFLG"],			// ZJSKFLG	前年実績フラグ
									WEEKHTDT		: rows[i]["WEEKHTDT"],			// WEEKHTDT	週間発注処理日
									UPDDT			: rows[i]["HDN_UPDDT"],			// UPDDT	更新日
								};
						}
					}
				}
			);

			// 情報設定
			var json = that.getGridParams(that);
			$.post(
				$.reg.easy,
				{
					page	:	that.name,										// レポート名
					obj		:	id,
					sel		:	(new Date()).getTime(),
					target	:	id,
					action	:	$.id.action_init,
					json	:	JSON.stringify(json),
					datatype:	'datagrid'
				},
				function(data){
					// ログ出力
					$.log(that.timeData, 'query:');

					if(data!==""){
						// JSONに変換
						var json = JSON.parse(data);
						var rows = json.rows;
						for (var i = 0; i < rows.length; i++){
							if(rows[i].IDX==='1'){
								$('#'+that.focusRootId).find('[col^=HTASU'+i+'_]').each(function(){
									var col = $(this).attr('col').replace('HTASU'+i, 'HTASU');
									$(this).text($.getFormatLPad(rows[i][col], $.len.tencd));
								});
							}else if(rows[i].IDX==='2'){
								$('#'+that.focusRootId).find('[col^=HTASU'+i+'_]').each(function(){
									var col1 = $(this).attr('col').replace('HTASU'+i, 'HTASU');
									var col2 = col1.replace("HTASU","FLG");
									$.setInputboxValue($(this), rows[i][col1]);
								});
							}
						}
						that.baseData = rows;
					}

					that.queried = true;
					// 隠し情報初期化
					$($.id.hiddenChangedIdx+that.suffix).val("");						// 変更行Index

					// ログ出力
					$.log(that.timeData, 'loaded:');
				}
			);
		},
		getGridData: function (target){
			var that = this;

			var data = {};

			var moyskbn  = that.cmnParam.MOYSKBN;
			var moysstdt = that.cmnParam.MOYSSTDT;
			var moysrban = that.cmnParam.MOYSRBAN;
			var bmncd    = that.cmnParam.BMNCD;
			var kanrino  = that.cmnParam.KANRINO;
			var kanrieno = that.cmnParam.KANRIENO;
			var nndt     = that.cmnParam.NNDT;
			var shncd    = that.cmnParam.SHNCD;
			var binkbn   = that.cmnParam.BINKBN;

			// 基本情報
			if(target===undefined || target==="grd_data"){
				var targetData = [];

				// 配列作成
				var len = 5;
				var tenhtsu_arr = "";			// TENHTSU_ARR		店発注数配列
				var tenchgflg_arrs = [];		// TENCHGFLG_ARR	店変更フラグ配列
				var tenchgflg_arr = "";

				if (!$.isEmptyVal(that.nndtData["TENCHGFLG_ARR"]) && that.nndtData["TENCHGFLG_ARR"].split("").length !== 0) {
					tenchgflg_arrs = that.nndtData["TENCHGFLG_ARR"].split("");
				}

				for (var i=0; i<400; i++){
					var id = $.id_inp.txt_htasu+that.suffix+'_'+(i+1);
					var tenhtsu = $.getInputboxValue($('#'+id));
					if($.isEmptyVal(tenhtsu)){
						tenhtsu_arr += $.paddingLeft(tenhtsu, len, " ");
						tenchgflg_arr += " ";
					}else{
						tenhtsu_arr += $.paddingLeft(tenhtsu, len, "0");
						tenchgflg_arr += "1";
					}
				}

				// 2.2.5.1．登録内容：
				// 全店特売(アンケート有/無)_納入日.店発注数配列
				// 全店特売(アンケート有/無)_納入日.店変更フラグ配列（本画面で発注数を修正した店舗のみ1をUPDATE（注意：更新処理前の配列を取得し、それに対して更新店のみ1をセットする事））
				var targetRow = that.nndtData;
				var rowData = null;
				if(that.judgeRepType.toktg){
					rowData = {
						F1 : ""+moyskbn,			// F1	MOYSKBN	催し区分
						F2 : ""+moysstdt,			// F2	MOYSSTDT	催し開始日
						F3 : ""+moysrban,			// F3	MOYSRBAN	催し連番
						F4 : ""+bmncd,				// F4	BMNCD	部門
						F5 : ""+kanrino,			// F5	KANRINO	管理番号
						F6 : ""+kanrieno,			// F6	KANRIENO	枝番
						F7 : ""+nndt,				// F7	NNDT		納入日
						F8 : tenhtsu_arr,			// F8	TENHTSU_ARR	店発注数配列
						F9 : tenchgflg_arr,			// F9	TENCHGFLG_ARR	店変更フラグ配列
						F10: targetRow.HTASU,		// F10	HTASU		発注総数
						F11: targetRow.PTNNO,		// F11	PTNNO		パターン№
						F12: targetRow.TSEIKBN,		// F12	TSEIKBN		訂正区分
						F13: targetRow.TPSU,		// F13	TPSU		店舗数
						F14: targetRow.TENKAISU,	// F14	TENKAISU	展開数
						F15: targetRow.ZJSKFLG,		// F15	ZJSKFLG		前年実績フラグ
						F16: targetRow.WEEKHTDT,	// F16	WEEKHTDT	週間発注処理日
						F20: targetRow.UPDDT,
						F21: ""+shncd
					};
					targetData.push(rowData);
				}else{
					rowData = {
						F1 : ""+moyskbn,			// F1	MOYSKBN	催し区分
						F2 : ""+moysstdt,			// F2	MOYSSTDT	催し開始日
						F3 : ""+moysrban,			// F3	MOYSRBAN	催し連番
						F4 : ""+bmncd,				// F4	BMNCD	部門
						F5 : ""+kanrino,			// F5	KANRINO	管理番号
						F6 : ""+kanrieno,			// F6	KANRIENO	枝番
						F7 : ""+nndt,				// F7	NNDT		納入日
						F8 : tenhtsu_arr,			// F8	TENHTSU_ARR	店発注数配列
						F9 : targetRow.HTASU,		// F9	HTASU	発注総数
						F10: targetRow.PTNNO,		// F10	PTNNO	パターン№
						F11: targetRow.TSEIKBN,		// F11	TSEIKBN	訂正区分
						F12: targetRow.TPSU,		// F12	TPSU	店舗数
						F13: targetRow.TENKAISU,	// F13	TENKAISU	展開数
						F14: targetRow.ZJSKFLG,		// F14	ZJSKFLG	前年実績フラグ
						F15: targetRow.WEEKHTDT,	// F15	WEEKHTDT	週間発注処理日
						F19: targetRow.UPDDT,
						F20: ""+shncd,
						F21: ""+binkbn
					};
					targetData.push(rowData);
				}
				data["grd_data"] = targetData;
			}

			return data;
		},
		setGridData: function (data, target, delFlg){
			var that = this;

			// 基本データ
			if(target===undefined || target==="grd_data"){
				that.grd_data =  data["grd_data"];
			}

			return true;
		},
		updValidation: function (id){	// （必須）批准
			var that = this;
			var errMsg= "";
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.gridform+that.suffix).form('validate');

			// 変更行情報取得
			var changedIndex = $($.id.hiddenChangedIdx+that.suffix).val().split(",");

			// ①店舗基本マスタ.店運用区分=9の場合、その店の発注数<>NULLでエラー。
			// ②店舗基本マスタにデータ更新区分=0のレコードが無い場合、その店の発注数<>NULLでエラー。
			// ③週間発注済チェック：
			// 全店特売（アンケート有・無）_商品.週間発注処理日<>NULLの場合かつTG016の【画面】週次伝送=1 AND 店別数量を変更の場合：「事前発注が済んでいる為に変更出来ません。」（禁止）を表示し、処理を中止する。
			// ④1店舗も数量>=0の店舗がない場合はエラー
			var inputExsitsFlg = false;
			var row = 0;
			var ten = 0;
			for (var i=0; i<400; i++){
				var id = $.id_inp.txt_htasu+that.suffix+'_'+(i+1);
				var tenhtsu = $.getInputboxValue($('#'+id));
				if(!$.isEmptyVal(tenhtsu)){
					if(that.baseData[row]['HTASU_'+ten]*1===(i+1) && that.baseData[row+1]['FLG_'+ten]==='1'){
						// ①②E20521	廃店は入力できません。	 	0	 	E
						var target = $('#'+that.focusRootId).find('[col^=HTASU'+(row+1)+'_'+ten+']');
						$.showMessage('E20521', undefined, function(){$.addErrState(that, target, true)});
						return false;
					}
					inputExsitsFlg = true;
				}
				if (ten===14) {
					ten = 0;
					row += 2;
				} else {
					ten++;
				}
			}
			if(!$.isEmptyVal($.getInputboxValue($("[col=F165]")))&&$.getInputboxValue($("[col=F146]"))===$.id.value_on && $.getConfirmUnregistFlg($($.id.hiddenChangedIdx+that.suffix))){
				// ③E20541	事前発注が済んでいる為に変更できません。	 	0	 	E
				$.showMessage('E20541');
				return false;
			}
			if(!inputExsitsFlg){
				// ④E20550	数量 ≧ 0の店舗が存在しない為、登録できません。	 	0	 	E
				$.showMessage('E20550');
				return false;
			}

			// 入力情報を変数に格納
			if (rt == true) that.setGridData(that.getGridData());
			return rt;
		},
		updSuccess: function(id){	// validation OK時 の update処理
			var that = this;

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$.appendMaskMsg();

			// 変更行情報取得
			var changedIndex = $($.id.hiddenChangedIdx+that.suffix).val().split(",");

			// 基本情報
			var targetData = that.grd_data;

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name2,			// レポート名
					action:			$.id.action_update,	// 実行処理情報
					obj:			id,					// 実行オブジェクト
					DATA:			JSON.stringify(targetData),				// 更新対象情報
					t:				(new Date()).getTime()
				},
				function(data){
					// 検索処理エラー判定
					if($.updError(id, data, id)) return false;

					// 親画面へ更新日時とユーザーを設定
					$.setInputboxValue($('#'+$.id.txt_upddt),JSON.parse(data).opts.sysdate);
					$.setInputboxValue($('#'+$.id.txt_operator),JSON.parse(data).opts.user);

					var afterFunc = function(){
						// 初期化
						that.Clear();

						// 親画面でのチェックに使用
						$.setInputboxValue($($.id.hiddenChangedIdx+that.suffix+'_upd'),'1');

						that.Back();
					};
					$.updNormal(data, afterFunc, id);
				}
			);
		},
		changeInputboxFunc:function(that, id, newValue, obj, all){

			// 今回発注数、数量差設定
			var sum=0;
			$('[id^='+$.id_inp.txt_htasu+that.suffix+']').each(function(){
				var id = $(this).attr('id');
				var val = $.getInputboxValue($(this));
				sum += val*1;
			});
			var sa = $.getInputboxValue($('#'+$.id_inp.txt_suryo+1+that.suffix), "0").replace(/,/g,"")*1 - sum;
			$.setInputboxValue($('#'+$.id_inp.txt_suryo+2+that.suffix), sum);
			$.setInputboxValue($('#'+$.id_inp.txt_suryo+3+that.suffix), sa);

			if(that.queried){
				that.changeInputInfo.push(id);
			}
		},
		getGridParams: function(that, key, val){
			// 情報取得
			var values = {};

			// 基本情報
			var paramnms = Object.getOwnPropertyNames(that.cmnParam);
			for ( var nm in paramnms ) {
				values[paramnms[nm]] = that.cmnParam[paramnms[nm]];
			}

			// 追加情報
			if(key){
				values[key] = val;
			}

			return [values];
		},
		setDataGrid: function(id) {
			var that = this;

			// 呼出し元別処理
			var columns = [];
			var columnBottom=[];
			for (var i=1; i<=15; i++){
				columnBottom.push({field:'HTASU_'+(i-1),title:'F'+i,width:57,halign:'center',align:'right',editor:{type:'numberbox'}
					,formatter:function(value,row,index){
						return row.IDX==='1'?$.getFormatLPad(value, $.len.tencd):$.getFormat(value, '#,##0');
					}
					,styler:function(value,row,index){
						return row.IDX==='1'?'background-color:#f5f5f5;text-align:center;':'';
					}
				});
			}
			columns.push(columnBottom);
			that.editRowIndex[id] = -1;
			$('#'+id).datagrid({
				nowrap: true,
				border: true,
				striped: false,
				collapsible:false,
				remoteSort: true,
				rownumbers:false,
				showHeader:false,
				fit:true,
				//view:scrollview,
				frozenColumns:[[]],
				columns:columns,
				fitColumns:false,	// 指定カラム幅を適用する場合、false 指定。
				rowStyler:function(index, row){
				},
				onLoadSuccess:function(data){
					// ログ出力
					$.log(that.timeData, 'query:');
				},
				onClickRow: function(index,row){
					$.clickEditableDatagridCell(that,id, index);
				},
				onBeforeEdit:function(index,row){
					if(row.IDX==='1'){
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
							evt.shiftKey = e.shiftKey;
							$('#'+id).parents('.datagrid').eq(0).trigger(evt);
						}
						return false;
					}
				},
				onBeginEdit:function(index,row){
					$.beginEditDatagridRow(that,id, index, row);
				},
				onEndEdit: function(index,row,changes){
					$.endEditDatagridRow(that, id, index, row)
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
		keyEventInputboxFunc:function(e, code, that, obj){
			// 38:↑キー押下 40:↓キー押下
			if (code === 38 || code === 40) {
				var id = $(obj).attr("orizinid");

				if (!$.isEmptyVal(id) && id.split("_").length >= 4) {

					// 現在フォーカスのあるid
					var num = id.split("_")[3];
					var id = id.split("_")[0] + '_' + id.split("_")[1] + '_' + id.split("_")[2] + '_';

					var plus = 15;
					if (code===38) {
						plus = -15;
					}
					that.setFocus(id,num,plus);
				}
			}
		},
		setFocus:function(id,num,plus) {

			num = (num*1) + plus;
			if (num*1 < 1 || num*1 >= 400) {
				return false;
			}

			var focus = false;

			while (!focus) {

				var opt = $('#'+ id + num).numberbox("options");

				if(!opt.disabled){
					$('#'+ id + num).next().children('.textbox-text').focus();
					focus = true;
				} else {
					num = (num*1) - 1;
					if (num*1 < 1) {
						return false;
					}
				}
			}
		}
	}
});

})(jQuery);