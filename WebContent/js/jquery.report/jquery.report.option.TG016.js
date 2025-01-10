/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_ReportTG016',			// （必須）レポートオプションの確認
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
		dedefaultObjNum:	151,	// 初期化オブジェクト数
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
		callpage:"",						// （必須）呼出画面情報
		sendBtnid: "",						// （必須）呼出ボタンID情報
		focusRootId:"cc",					// （キー移動時必須）キー移動イベントのルートとなるパネルのID
		focusParentId:"",					// （キー移動時必須）現在フォーカスがあたっている項目の親となるパネルのID
		focusGridId:"",						// （キー移動時必須）現在フォーカスがあたっているDataGridのID
		editRowIndex:{},					// グリッド編集行保持

		requiredInputIds:[],				// 登録前で必須チェックを行う入力項目情報
		changeInputInfo:[],					// 変更された入力項目の情報保持
		editableInputIdsTabHB:[],			// 編集可能な入力項目の情報（販売タブ）
		editableInputIdsTabNN:[],			// 編集可能な入力項目の情報（納入タブ）

		baseData:[],						// 検索結果保持用
		moycdData:[],						// 検索結果保持用(催しコード/催し基本)
		nndtData:[],						// 検索結果保持用(納入日)
		hbdtData:[],						// 検索結果保持用(販売日)

		copy1Data:[],						// 前複写1情報保持(販売日/納入日)
		copy2Data:[],						// 前複写2情報保持(対象店/除外店)
		copy3Data1:[],						// 前複写3(「納入情報」タブ部分)
		copy3Data2:[],						// 前複写3(「納入情報」タブ部分)

		grd_data:[],						// メイン情報：商品マスタ
		grd_data_other:[],					// 補足情報：その他、テーブルに登録しない情報などを保持
		grd_data_tjten:[],					// 関連テーブル情報
		grd_data_nndt:[],					// 関連テーブル情報
		grd_data_hb:[],						// 関連テーブル情報

		selGentei:[],
		selTni:[],

		updConfirmMsg:[],
		jskptn:"",
		oldTkanplukbn:"",
		initialize: function (reportno){	// （必須）初期化
			var that = this;
			// 引き継ぎ情報
			this.jsonHidden = $.getTargetValue();
			// 画面の初回基本設定
			this.setInitObjectState();
			// 初期検索条件取得
			this.jsonInit = $.getInitValue();
			// 処理日付取得
			$.getsetInputboxData(reportno, $.id.txt_shoridt, [{}], $.id.action_init);

			// 初期化するオブジェクト数設定
			this.initObjNum = this.dedefaultObjNum;
			that.onChangeReport = true;

			var isUpdateReport = true;
			// 名称マスタ参照系
			var meisyoSelect = Object.getOwnPropertyNames($.id_mei);
			for ( var sel in meisyoSelect ) {
				var target = $('#'+$.id_mei[meisyoSelect[sel]]);
				if(target.length > 0 && target.is(':input')){
					$.setMeisyoCombo(that, reportno, $.id_mei[meisyoSelect[sel]], isUpdateReport);
				}
			}
			// 入力テキストボックス系
			var inputbox = Object.getOwnPropertyNames($.id_inp);
			for ( var sel in inputbox ) {
				var target = $('#'+$.id_inp[inputbox[sel]]);
				if(target.length > 0 && target.is(':input')){
					$.setInputbox(that, reportno, $.id_inp[inputbox[sel]], isUpdateReport);
				}
			}

			var reportYobi1 = $.getJSONValue(that.jsonHidden, "reportYobi1");
			if(!reportYobi1){
				reportYobi1 = $('#reportYobi1').val();
			}
			$('#reportYobi1').val(reportYobi1);

			$.setMeisyoCombo(that, reportno, $.id_mei.kbn10656+"_a", isUpdateReport);
			$.setMeisyoCombo(that, reportno, $.id_mei.kbn10656+"_b", isUpdateReport);
			$.setMeisyoCombo(that, reportno, $.id_mei.kbn10656+"_c", isUpdateReport);

			// BYCD
			that.setBycd(that, reportno, $.id.sel_bycd, that.judgeRepType.sei_new);

			// 複数項目
			for (var i = 1; i <= 3; i++){
				$.setInputbox(that, reportno, $.id_inp.txt_a_baikaam+i , isUpdateReport);
				$.setInputbox(that, reportno, $.id_inp.txt_b_baikaam+i , isUpdateReport);
				$.setInputbox(that, reportno, $.id_inp.txt_c_baikaam+i , isUpdateReport);
				$.setInputbox(that, reportno, $.id_inp.txt_irisu+i , isUpdateReport);			// 入数
			}

			for (var i = 1; i <= 10; i++){
				$.setInputbox(that, reportno, $.id_inp.txt_tencd+"_add"+i , isUpdateReport);
				$.setInputbox(that, reportno, $.id_inp.txt_tenrank+i , isUpdateReport);
				$.setInputbox(that, reportno, $.id_inp.txt_tencd+"_del"+i , isUpdateReport);
				$.setCheckboxInit2(that.jsonHidden, $.id.chk_nndt+i , that);					// 納入日チェックボックス
				$.setInputbox(that, reportno, $.id_inp.txt_htasu+i , isUpdateReport);			// 発注数
				$.setInputbox(that, reportno, $.id_inp.txt_ptnno+i , isUpdateReport);			// パターンNo.
				$.setInputbox(that, reportno, $.id_inp.txt_tseikbn+i , isUpdateReport);			// 訂正区分
			}

			// ラジオボタン系
			$.setRadioInit2(that.jsonHidden, $.id.rad_tkanplukbn , that);		// PLU

			// チェックボックス系
			$.setCheckboxInit2(that.jsonHidden, $.id.chk_hbslideflg , that);	// 一日遅スライドしない-販売
			$.setCheckboxInit2(that.jsonHidden, $.id.chk_nhslideflg , that);	// 一日遅スライドしない-納入
			$.setCheckboxInit2(that.jsonHidden, $.id.chk_higawrflg , that);		// 日替
			$.setCheckboxInit2(that.jsonHidden, $.id.chk_chirasflg , that);		// チラシ未掲載
			$.setCheckboxInit2(that.jsonHidden, $.id.chk_htgenbaikaflg , that);	// 発注原売価適用しない
			$.setCheckboxInit2(that.jsonHidden, $.id.chk_plusndflg , that);		// PLU配信しない
			$.setCheckboxInit2(that.jsonHidden, $.id.chk_yoriflg , that);		// よりどり
			$.setCheckboxInit2(that.jsonHidden, $.id.chk_namanetukbn+1 , that);	// 生食・加熱-生食
			$.setCheckboxInit2(that.jsonHidden, $.id.chk_namanetukbn+2 , that);	// 生食・加熱-加熱
			$.setCheckboxInit2(that.jsonHidden, $.id.chk_kaitoflg , that);		// 解凍
			$.setCheckboxInit2(that.jsonHidden, $.id.chk_yoshokuflg , that);	// 養殖
			$.setCheckboxInit2(that.jsonHidden, $.id.chk_juflg , that);			// 事前打出(チェック)
			$.setCheckboxInit2(that.jsonHidden, $.id.chk_cuttenflg , that);		// カット店展開しない"
			$.setCheckboxInit2(that.jsonHidden, $.id.chk_shudenflg , that);		// 週次伝送flg


			$('#'+$.id_inp.txt_tenkaikbn).change(function(e) {
				var obj = $(this);
				if($($.id.hiddenChangedIdx).is(':enabled')){
					$($.id.hiddenChangedIdx).val("1");
				};
				var newValue = $.getInputboxValue(obj);
				that.changeInputboxFunc(that, $.id_inp.txt_tenkaikbn, newValue, obj);
			});
			$('#'+$.id_inp.txt_shncd).next().on('focusout', function(e){
				var obj = $(this).find('[orizinid='+$.id_inp.txt_shncd+']');
				var code = e.which ? e.which : e.keyCode;
				if(code !== 13 && code !== 9){
					that.keyEventInputboxFunc(e, 13, that, obj);

				}
			});

			// 対象除外店部分のタブインデックスを-1に
			var target = '';
			for (var i = 23; i <= 52; i++) {
				target += "[col=F" + i + "]";
				if (i!==52) {
					target += ',';
				}
			}

			$(target).next().on('focusin focusout', function(e){
				var type = e.handleObj["type"];
				var obj = $(target);

				obj.each(function(){
					if (type==='focusout') {
						$(this).textbox('textbox').attr('tabindex',-1);
					} else {
						$(this).textbox('textbox').attr('tabindex',$(this).attr('tabindex-bk'));
					}
				});
			});

			// 初期化終了
			this.initializes =! this.initializes;

			// ログ出力
			$.log(that.timeData, 'initialize:');
		},
		judgeRepType: {
			// メニュー
			tg				: false,	// 月間販売計画
			st				: false,	// 特売・スポット

			// 催し種類
			toktg			: false,	// アンケート有
			toksp			: false,	// アンケート無
			toktg_t			: false,	// アンケート有(チラシのみ)
			toktg_h			: false,	// アンケート有(販売・納入)

			// 画面モード
			isModeA			: false,	// TG008更新
			isModeB			: false,	// TG008参照
			isModeC			: false,	// TG009(更新のみ)
			isModeD			: false,	// ST016の新規・新規（全品割引）
			isModeE			: false,	// ST016の選択（販売・納品情報）とMM001の選択
			isModeF			: false,	// ST016の月締後新規・新規（全品割引）	→機能廃止
			isModeG			: false,	// ST016の月締後今の内容を修正			→機能廃止
			isModeH			: false,	// ST019の選択（確定）
			isModeI			: false,	// 参照モード

			// 登録種別
			frm1			: false,	// ドライ
			frm2			: false,	// 精肉
			frm3			: false,	// 鮮魚
			frm4			: false,	// 青果
			frm5			: false,	// 全品割引

			// 処理タイプ
			sei_new 		: false,	// 正 -新規
			sei_upd 		: false,	// 正 -更新
			sei_ref 		: false,	// 正 -参照
			ref				: false,	// 参照

			gtsime			: false,	// 月締め後

			tabhb			: true,		// 販売情報タブ表示
			tabnn			: true,		// 納入情報タブ表示
		},
		repgrpInfo: {
			TG001:{idx:1},		// 月間販売計画 新規・変更
			TG001_1:{idx:2},	// 月間販売計画 参照
			TG002:{idx:3},		// 月間販売計画 店舗グループ一覧
			TG003:{idx:4},		// 月間販売計画 店舗グループ店情報
			TG008:{idx:5},		// 月間販売計画 商品一覧
			TG040:{idx:6},		// 月間販売計画 コピー元店舗グループ一覧
			ST016:{idx:5},		// 特売・スポット計画 商品一覧
			ST019:{idx:7},		// 特売・スポット計画 コピー元商品選択
			TG016:{idx:7},		// 商品情報
			MM001:{idx:1},		// 催し検索 変更 催し一覧
			MM001_1:{idx:2},	// 催し検索 参照 催し一覧
			MM002:{idx:3}		// 催し検索 商品一覧
		},
		searched_initialize: function (reportno, opts){	// 検索結果を受けての初期化
			var that = this;

			// 月締め後判断
			if(that.judgeRepType.tg && that.moycdData[0]["GTSIMEFLG"]===$.id.value_on){
				that.judgeRepType.gtsime = true;
			}

			// ***** 項目制御 *****
			var disobjids = [];	// 無効
			var hidobjids = [];	// 非表示
			var reqobjids = [];	// 必須

			// *** 取得情報に基づく処理別制御 ***
			// 2.1．画面OPEN時チェック等の初期処理：
			var errFunc = function(){
				that.changeReport(that.name, $.id.btn_back);
			};
			// 2.1.1．アンケート有の場合、アンケート月度チェック：全店特売（アンケート有）_基本.アンケート月度 = NULLの場合、TG016のOPEN前処理でエラーとする。
			if(that.judgeRepType.toktg){
				if($.isEmptyVal(that.moycdData[0]["QAYYYYMM"])){
					// E20372	アンケート月度未設定の場合、本画面を使用できません。	 	0	 	E
					$.showMessage("E20372", undefined, errFunc);
					return false;
				}
			}
			// 2.1.2．当催しの催し区分=0：レギュラーの場合、TG016_5（全品割引）のOPEN前処理でエラーとする。
			var txt_moyskbn = $.getJSONValue(this.jsonHidden, $.id_inp.txt_moyskbn);
			if(that.judgeRepType.frm5&&txt_moyskbn===$.id.value_moykbn_r){
				// E20373	レギュラーの場合、本画面を使用できません。	 	0	 	E
				$.showMessage("E20373", undefined, errFunc);
				return false;
			}
			// 2.1.3．月締後新規（新規全割）登録チェック：アンケート有（月締後）の催しにおいて、ST016の新規・新規（全品割引）よりの場合、TG016のOPEN前処理でエラー。
			// アンケート有（月締後）の催しは、ST016の「月締後変更の選択」部分より遷移しなければいけない。
			if(that.judgeRepType.gtsime && that.judgeRepType.isModeD){
				// E20374	月締め後の場合、月締後（新規、新規（全品割引））より遷移してください。	 	0	 	E
				$.showMessage("E20374", undefined, errFunc);
				return false;
			}

			if (that.judgeRepType.toksp && that.judgeRepType.frm4 && that.judgeRepType.isModeD) {
				$.showMessage("E40091", undefined, errFunc);
				return false;
			}

			// 登録種別＋画面モード＋検索結果による項目制御判断
			// 入出力データ仕様(遷移仕様)TG016_1～5参照
			// 月締後共通：ST016の選択（販売・納品情報）とMM001の選択
			if(that.judgeRepType.tg && that.moycdData[0]["GTSIMEFLG"]===$.id.value_on && that.judgeRepType.isModeE){
				if(that.judgeRepType.frm1){
					hidobjids = hidobjids.concat([$.id.txt_status,$.id.btn_copy+1,$.id.btn_copy+2,$.id.btn_kyoka,$.id.btn_del,$.id.btn_shncd,$.id.btn_copy+3]);
					// 【画面一般情報部分】
					disobjids = disobjids.concat([$.id.chk_hbslideflg,$.id_inp.txt_shncd,$.id_inp.txt_parno,$.id_inp.txt_chldno,$.id.chk_higawrflg,$.id_inp.txt_hbstdt, $.id_inp.txt_hbeddt, $.id_inp.txt_nnstdt, $.id_inp.txt_nneddt]);
					disobjids = disobjids.concat([$.id.chk_chirasflg,$.id_inp.txt_rankno_add,$.id.btn_rankno+"_add_a", $.id_inp.txt_rankno_del,$.id.btn_rankno+"_del",$.id_inp.txt_rankno_add_b,$.id.btn_rankno+"_add_b",$.id_inp.txt_rankno_add_c,$.id.btn_rankno+"_add_c"]);
					disobjids = disobjids.concat([$.id_inp.txt_a_baikaam+1,$.id_inp.txt_b_baikaam+1,$.id_inp.txt_c_baikaam+1]);
					// 【販売情報部分】
					disobjids = disobjids.concat([$.id_inp.txt_makerkn,$.id_inp.txt_popkn,$.id_inp.txt_kikkn,$.id_inp.txt_segn_ninzu,$.id_mei.kbn10670,$.id_inp.txt_segn_1kosu,$.id_mei.kbn10671,$.id.chk_yoriflg]);
					disobjids = disobjids.concat($('#spread_ko').find(":input").map(function(index, element){ return $(this).attr("id"); }).get());		// 一個売り部分
					disobjids = disobjids.concat($('#spread_bd1').find(":input").map(function(index, element){ return $(this).attr("id"); }).get());	// バンドル1部分
					disobjids = disobjids.concat($('#spread_bd2').find(":input").map(function(index, element){ return $(this).attr("id"); }).get());	// バンドル2部分
					disobjids = disobjids.concat($('#sspread_chipo').find(":input").map(function(index, element){ return $(this).attr("id"); }).get());	// チラシ・POP情報
				}else if(that.judgeRepType.frm2||that.judgeRepType.frm3){
					hidobjids = hidobjids.concat([$.id.txt_status,$.id.btn_copy+1,$.id.btn_copy+2,$.id.btn_kyoka,$.id.btn_del,$.id.btn_shncd,$.id.btn_copy+3]);
					// 【画面一般情報部分】
					disobjids = disobjids.concat([$.id.chk_hbslideflg,$.id_inp.txt_shncd,$.id_inp.txt_parno,$.id_inp.txt_chldno,$.id.chk_higawrflg,$.id_inp.txt_hbstdt, $.id_inp.txt_hbeddt, $.id_inp.txt_nnstdt, $.id_inp.txt_nneddt]);
					disobjids = disobjids.concat([$.id.chk_chirasflg,$.id_inp.txt_rankno_add,$.id.btn_rankno+"_add_a", $.id_inp.txt_rankno_del,$.id.btn_rankno+"_del",$.id_inp.txt_rankno_add_b,$.id.btn_rankno+"_add_b",$.id_inp.txt_rankno_add_c,$.id.btn_rankno+"_add_c"]);
					disobjids = disobjids.concat([$.id.rad_tkanplukbn+1, $.id.rad_tkanplukbn+2,$.id_inp.txt_a_baikaam+1,$.id_inp.txt_b_baikaam+1,$.id_inp.txt_c_baikaam+1,$.id_inp.txt_a_baikaam+2,$.id_inp.txt_b_baikaam+2,$.id_inp.txt_c_baikaam+2]);
					// 【販売情報部分】
					disobjids = disobjids.concat([$.id_inp.txt_makerkn,$.id_inp.txt_popkn,$.id_inp.txt_kikkn,$.id_inp.txt_segn_ninzu,$.id_mei.kbn10670,$.id_inp.txt_segn_1kosu,$.id_mei.kbn10671,$.id.chk_yoriflg]);
					disobjids = disobjids.concat($('#spread_ko').find(":input").map(function(index, element){ return $(this).attr("id"); }).get());		// 一個売り部分
					disobjids = disobjids.concat($('#spread_bd1').find(":input").map(function(index, element){ return $(this).attr("id"); }).get());	// バンドル1部分
					disobjids = disobjids.concat($('#spread_bd2').find(":input").map(function(index, element){ return $(this).attr("id"); }).get());	// バンドル2部分
					disobjids = disobjids.concat($('#spread_100g').find(":input").map(function(index, element){ return $(this).attr("id"); }).get());	// 100g相当
					disobjids = disobjids.concat($('#sspread_chipo').find(":input").map(function(index, element){ return $(this).attr("id"); }).get());	// チラシ・POP情報
				}else if(that.judgeRepType.frm4){
					hidobjids = hidobjids.concat([$.id.txt_status,$.id.btn_copy+1,$.id.btn_copy+2,$.id.btn_kyoka,$.id.btn_del,$.id.btn_shncd]);
					// 【画面一般情報部分】
					disobjids = disobjids.concat([$.id.chk_hbslideflg,$.id_inp.txt_shncd,$.id_inp.txt_parno,$.id_inp.txt_chldno,$.id.chk_higawrflg,$.id_inp.txt_hbstdt, $.id_inp.txt_hbeddt, $.id_inp.txt_nnstdt, $.id_inp.txt_nneddt]);
					disobjids = disobjids.concat([$.id.chk_chirasflg,$.id_inp.txt_rankno_add,$.id.btn_rankno+"_add_a", $.id_inp.txt_rankno_del,$.id.btn_rankno+"_del",$.id_inp.txt_rankno_add_b,$.id.btn_rankno+"_add_b",$.id_inp.txt_rankno_add_c,$.id.btn_rankno+"_add_c"]);
					disobjids = disobjids.concat($('#spread_sobaika').find(":input").map(function(index, element){ return $(this).attr("id"); }).get());	// 総売価部分
					// 【販売情報部分】
					disobjids = disobjids.concat([$.id_inp.txt_sanchikn,$.id_inp.txt_makerkn,$.id_inp.txt_popkn,$.id_inp.txt_kikkn,$.id_inp.txt_segn_ninzu,$.id_mei.kbn10670,$.id_inp.txt_segn_1kosu,$.id_mei.kbn10671,$.id.chk_yoriflg]);
					disobjids = disobjids.concat($('#spread_bd1').find(":input").map(function(index, element){ return $(this).attr("id"); }).get());	// バンドル1部分
					disobjids = disobjids.concat($('#spread_bd2').find(":input").map(function(index, element){ return $(this).attr("id"); }).get());	// バンドル2部分
					disobjids = disobjids.concat($('#sspread_chipo').find(":input").map(function(index, element){ return $(this).attr("id"); }).get());	// チラシ・POP情報
				}else if(that.judgeRepType.frm5){
					hidobjids = hidobjids.concat([$.id.txt_status,$.id.btn_copy+1,$.id.btn_copy+2,$.id.btn_kyoka,$.id.btn_del,$.id.btn_shncd]);
					// 【画面一般情報部分】
					disobjids = disobjids.concat([$.id.chk_hbslideflg,$.id_inp.txt_shncd,$.id_inp.txt_parno,$.id_inp.txt_chldno,$.id.chk_higawrflg,$.id_inp.txt_hbstdt, $.id_inp.txt_hbeddt, $.id_inp.txt_nnstdt, $.id_inp.txt_nneddt]);
					disobjids = disobjids.concat([$.id.chk_chirasflg,$.id_inp.txt_rankno_add,$.id.btn_rankno+"_add_a", $.id_inp.txt_rankno_del,$.id.btn_rankno+"_del",$.id_inp.txt_rankno_add_b,$.id.btn_rankno+"_add_b",$.id_inp.txt_rankno_add_c,$.id.btn_rankno+"_add_c"]);
					disobjids = disobjids.concat($('#spread_sobaika').find(":input").map(function(index, element){ return $(this).attr("id"); }).get());	// 総売価部分
					// 【販売情報部分】
					disobjids = disobjids.concat([$.id_inp.txt_popkn,$.id_inp.txt_segn_ninzu,$.id_mei.kbn10670,$.id_inp.txt_segn_1kosu,$.id_mei.kbn10671]);
					disobjids = disobjids.concat($('#sspread_chipo').find(":input").map(function(index, element){ return $(this).attr("id"); }).get());	// チラシ・POP情報
				}
			} else if (that.judgeRepType.st || that.judgeRepType.toktg || that.judgeRepType.isModeG || that.judgeRepType.isModeE) {

				// ***** 項目制御 *****
				var disobjidsSt = [];	// 無効

				// 特売・スポット初期表示時は以下の項目を非活性
				disobjidsSt = disobjidsSt.concat([$.id_inp.txt_rankno_add_b,$.id.btn_rankno+"_add_b",$.id_inp.txt_rankno_add_c,$.id.btn_rankno+"_add_c"]);

				// 無効化
				for (var i = 0; i < disobjidsSt.length; i++) {
					$.setInputBoxDisableVariable($('#'+disobjidsSt[i]),true);
				}

				if (that.judgeRepType.sei_new) {
					$.setInputBoxDisable($('#'+$.id.btn_tenbetusu));
				}
			}

			if (!(!$.isEmptyVal(that.baseData[0]["F16"])&&!$.isEmptyVal(that.baseData[0]["F17"]))) {
				hidobjids = hidobjids.concat([$.id.btn_sub+"_winTG018_h"]);
			}

			if (!(!$.isEmptyVal(that.baseData[0]["F18"])&&!$.isEmptyVal(that.baseData[0]["F19"]))) {
				hidobjids = hidobjids.concat([$.id.btn_sub+"_winTG018_n"]);
			}

			// 無効化
			for (var i = 0; i < disobjids.length; i++) {
				$.setInputBoxDisable($('#'+disobjids[i]));
			}
			// 非表示化
			for (var i = 0; i < hidobjids.length; i++) {
				$.setInputBoxDisable($('#'+hidobjids[i])).hide();
			}

			// 編集可能な入力項目の情報（販売タブ）
			if(that.judgeRepType.tabhb){
				that.editableInputIdsTabHB = that.editableInputIdsTabHB.concat($('#tt_1_hb').find(":input").filter(":enabled").map(function(index, element){ return $(this).attr("id"); }).get());
			}
			// 編集可能な入力項目の情報（納入タブ）
			if(that.judgeRepType.tabnn){
				that.editableInputIdsTabNN = that.editableInputIdsTabNN.concat($('#tt_1_nn').find(":input").filter(":enabled").map(function(index, element){ return $(this).attr("id"); }).get());
			}

			// メインデータ表示	※一部でDisableの時初期値設定しないという指定があるため、ここでメインデータ設定
			that.setData(that.baseData);
			that.setNndtData(that.nndtData, "N");

			// 販売タブ、納入タブの項目使用不可設定
			if(!(!$.isEmptyVal(that.baseData[0]["F16"])&&!$.isEmptyVal(that.baseData[0]["F17"]))){
				$.each(that.editableInputIdsTabHB, function(){
					$.setInputBoxDisableVariable($("#" + this),true);
				});

				var stdt = $.getInputboxValue($('#'+$.id_inp.txt_hbstdt));
				var eddt = $.getInputboxValue($('#'+$.id_inp.txt_hbeddt));
				if(!$.isEmptyVal(stdt)&&!$.isEmptyVal(eddt)){
					if (!that.judgeRepType.frm5 || (that.judgeRepType.frm5 && !that.judgeRepType.toktg_t)) {
						if (!that.judgeRepType.isModeI) {
							$.setInputBoxEnableVariable($("#" + $.id.chk_plusndflg),true);
						}
						$.setInputboxValue($("#" + $.id.chk_plusndflg), $.id.value_off);
					}
				}else{
					if (!that.judgeRepType.frm5 || (that.judgeRepType.frm5 && !that.judgeRepType.toktg_t)) {
						$.setInputboxValue($("#" + $.id.chk_plusndflg), $.id.value_on);
						$.setInputBoxDisableVariable($("#" + $.id.chk_plusndflg),true);
					}
				}
			}

			if (!that.judgeRepType.frm5 && that.judgeRepType.toktg_t) {
				$.setInputboxValue($("#" + $.id.chk_plusndflg), $.id.value_on);
				$.setInputBoxDisableVariable($("#" + $.id.chk_plusndflg),true);
			}

			if(!(!$.isEmptyVal(that.baseData[0]["F18"])&&!$.isEmptyVal(that.baseData[0]["F19"]))){
				$.each(that.editableInputIdsTabNN, function(){
					$.setInputBoxDisableVariable($("#" + this),true);
				});
			}

			// サブウインドウの初期化
			setTimeout(function(){
				$.win006.init(that);	// 商品コード
				$.winST007.init(that);	// ランクNo.
				$.winST009.init(that);	// ランクNo.
				$.winST012.init(that);	// 率パターン
				$.winST020.init(that);	// 数値展開
				$.winST021.init(that);	// 店別数量
				$.winTG018.init(that);	// 販売店/納入店確認
			},50);


			// チェックボックスの設定
			$.initCheckboxCss($("#"+that.focusRootId), false);

			// 複写ボタンを強制的に実行
			if (that.judgeRepType.sei_new && "Out_ReportST019"!==that.callpage){
				$('#'+$.id.btn_copy+1).click();
				that.changeInputboxFunc(that, $.id_inp.txt_hbstdt, $.getInputboxValue($('#'+$.id_inp.txt_hbstdt)), $('#'+$.id_inp.txt_hbstdt), true);
				that.changeInputboxFunc(that, $.id_inp.txt_nnstdt, $.getInputboxValue($('#'+$.id_inp.txt_nnstdt)), $('#'+$.id_inp.txt_nnstdt), true);
			}

			if(that.judgeRepType.frm4){
				$('#'+$.id.chk_nhslideflg).next().hide();
			}

			if (!that.judgeRepType.sei_new || (!$.isEmptyVal(that.baseData[0]["F18"])&&!$.isEmptyVal(that.baseData[0]["F19"]))) {
				that.changeInputboxFunc(that, $.id_inp.txt_nnstdt, $.getInputboxValue($("#"+$.id_inp.txt_nnstdt)), $("#"+$.id_inp.txt_nnstdt), true);
			}


			// 更新時入数を非活性にするか判定
			if (that.judgeRepType.sei_upd && !that.judgeRepType.frm4 && !that.judgeRepType.frm5) {
				if(!$.isEmptyVal(that.baseData[0].F163,true) || !$.isEmptyVal(that.baseData[0].F177,true) ||
						!$.isEmptyVal(that.baseData[0].F164,true) ||
						!$.isEmptyVal(that.baseData[0].F165,true)){
					that.changeInputboxFunc(that, $.id.rad_tkanplukbn);
				}
			}

			// 納入タブ再計算イベント
			if(that.judgeRepType.tabnn){
				that.setCalc(that);
			}

			// クックサン部門特殊処理
			var txt_bmncd = $.getJSONValue(this.jsonHidden, $.id_inp.txt_bmncd);
			if (that.getColValue("F173")==="1" && (txt_bmncd==='20' || txt_bmncd==='23')) {
				if (!that.judgeRepType.isModeI) {
					$.setInputBoxEnableVariable($("#" + $.id_inp.txt_binkbn));
				}
			}

			// キーイベントの設定
			$.initKeyEvent(that);

			// ログ出力
			$.log(that.timeData, 'searched_initialize:');
		},
		setInitObjectState: function(){	// 画面初期化時の項目制御
			var that = this;
			// 引き継ぎ情報セット
			that.callpage = $.getJSONValue(that.jsonHidden, "callpage");
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
			if ("Out_ReportTG009"===that.callpage && sendBtnid===$.id.btn_sel_change) {
				reportYobi1 = "1";
			}
			$('#reportYobi1').val(reportYobi1);

			// メニュー判断：月間販売計画or特売・スポット
			var report_nm = "";
			if(that.callpage.indexOf('Out_ReportST') !==-1){
				report_nm = "特売・スポット計画";
				that.judgeRepType.st = true;
			} else if (that.callpage.indexOf('Out_ReportMM') !==-1) {
				report_nm = "催し検索";
				that.judgeRepType.st = true;
			}else{
				report_nm = "月間販売計画（チラシ計画）";
				that.judgeRepType.tg = true;
			}

			// 処理タイプ判断：新規・変更・参照
			if(that.sendBtnid===$.id.btn_new||that.sendBtnid===$.id.btn_new+1||that.sendBtnid===$.id.btn_new+2||that.sendBtnid===$.id.btn_sel_kakutei){
				that.judgeRepType.sei_new = true;
			}else if((that.sendBtnid===$.id.btn_sel_change && that.reportYobiInfo()!=='1')){
				that.judgeRepType.sei_upd = true;
			} else if (that.reportYobiInfo()!=='1' &&
					(("Out_ReportMM001"===that.callpage && sendBtnid===$.id.btn_select) || ("Out_ReportMM002"===that.callpage && that.sendBtnid===$.id.btn_sel_shninfo))) {
				that.judgeRepType.sei_upd = true;
			}else{
				that.judgeRepType.sei_ref= true;
				that.judgeRepType.ref= true;
			}

			// 画面モード判断：遷移画面＋実行ボタンに基づき画面モードを判断
			that.judgeRepType.isModeA = ("Out_ReportTG008"===that.callpage && sendBtnid===$.id.btn_sel_change);		// TG008更新
			that.judgeRepType.isModeB = ("Out_ReportTG008"===that.callpage && sendBtnid===$.id.btn_sel_refer);		// TG008参照
			that.judgeRepType.isModeC = ("Out_ReportTG009"===that.callpage && sendBtnid===$.id.btn_sel_change);		// TG009(更新のみ)
			that.judgeRepType.isModeD = ("Out_ReportST016"===that.callpage &&(sendBtnid===$.id.btn_new+1||sendBtnid===$.id.btn_new+2));	// ST016の新規・新規（全品割引）
			that.judgeRepType.isModeE = ("Out_ReportST016"===that.callpage && sendBtnid===$.id.btn_sel_change)
									 || (("Out_ReportMM001"===that.callpage && sendBtnid===$.id.btn_select) || ("Out_ReportMM002"===that.callpage && that.sendBtnid===$.id.btn_sel_shninfo));			// ST016の選択（販売・納品情報）とMM001の選択
			that.judgeRepType.isModeF = ("Out_ReportST016"===that.callpage && (false));								// ST016の月締後新規・新規（全品割引）	→機能廃止
			that.judgeRepType.isModeG = ("Out_ReportST016"===that.callpage && (false));								// ST016の月締後今の内容を修正			→機能廃止
			that.judgeRepType.isModeH = ("Out_ReportST019"===that.callpage && sendBtnid===$.id.btn_sel_kakutei);	// ST019の選択（確定）
			that.judgeRepType.isModeI = (!that.judgeRepType.isModeA&&!that.judgeRepType.isModeB&&!that.judgeRepType.isModeC&&!that.judgeRepType.isModeD&&!that.judgeRepType.isModeE&&!that.judgeRepType.isModeF&&!that.judgeRepType.isModeG&&!that.judgeRepType.isModeH);													// 参照モード

			if(!that.judgeRepType.sei_new && !that.judgeRepType.sei_upd && that.judgeRepType.isModeE) {
				that.judgeRepType.isModeI  =true;
			}

			// 催し種類判断：全店特売（アンケート有(チラシのみ/販売・納入)/無）
			var txt_moyskbn = $.getJSONValue(this.jsonHidden, $.id_inp.txt_moyskbn);
			var txt_moysrban= $.getJSONValue(this.jsonHidden, $.id_inp.txt_moysrban);
			var txt_bmncd   = $.getJSONValue(this.jsonHidden, $.id_inp.txt_bmncd);
			if(txt_moyskbn !== $.id.value_moykbn_t || txt_moysrban < 50){
				that.judgeRepType.toksp = true;
			}else{
				that.judgeRepType.toktg = true;

				// チラシのみ部門判断
				var param = that.getInputboxParams(that, "", txt_bmncd, "MST_CNT");
				var bmn_chk = $.getInputboxData(that.name, $.id.action_check, "TOKCHIRASBMN", param);
				that.judgeRepType.toktg_t = bmn_chk!=="0";
				that.judgeRepType.toktg_h = bmn_chk==="0";
			}

			// 登録種別判断：
			var txt_addshukbn = $.getJSONValue(this.jsonHidden, $.id_inp.txt_addshukbn);
			var txt_addshukbn_nm = "";
			var frm_no = "";
			if(txt_addshukbn==="1"){
				txt_addshukbn_nm = "／全品割引";
				frm_no = "_5";
				that.judgeRepType.frm5 = true;
			}else if(txt_addshukbn==="2"){
				txt_addshukbn_nm = "／ドライ";
				frm_no = "_1";
				that.judgeRepType.frm1 = true;
			}else if(txt_addshukbn==="3"){
				txt_addshukbn_nm = "／青果";
				frm_no = "_4";
				that.judgeRepType.frm4 = true;
			}else if(txt_addshukbn==="4"){
				txt_addshukbn_nm = "／鮮魚";
				frm_no = "_3";
				that.judgeRepType.frm3 = true;
			}else if(txt_addshukbn==="5"){
				txt_addshukbn_nm = "／精肉";
				frm_no = "_2";
				that.judgeRepType.frm2 = true;
			}

			// ***** 項目制御 *****
			var disobjids = [];	// 無効
			var hidobjids = [];	// 非表示
			var reqobjids = [];	// 必須

			// 登録種別に基づく項目制御実施 - 入出力データ仕様(TG016共通)参照（納入情報まるごとは最後に実施）
			$('#'+that.focusRootId).find('[for_kbn]').each(function(){
				var kbns = $(this).attr('for_kbn').split(",");
				// 非表示化
				if(kbns.indexOf(txt_addshukbn) === -1){
					if($(this).is(".panel-body")){
						var obj = $(this).parents('.easyui-tabs').eq(0);
						var idx = obj.tabs('getTabIndex', $(this));
						obj.tabs('close', idx);
					}else{
						$(this).hide();
					}
					$(this).find("[tabindex]").filter("[tabindex!=-1]").filter('[disabled!=disabled]').each(function(){
						$.setInputBoxDisable($(this));
					});
				}
			});

			// 催し種類(アンケート有/無)に基づく項目制御判断
			if(that.judgeRepType.toktg){			// アンケート有
				disobjids = disobjids.concat([$.id_inp.txt_rankno_del]);
				disobjids = disobjids.concat([$.id.btn_rankno+"_del"]);

			}else if(that.judgeRepType.toksp){		// アンケート無
				disobjids = disobjids.concat([$.id.chk_chirasflg]);
			}
			if(txt_moyskbn===$.id.value_moykbn_r){
				// 15.販売期間From/16.販売期間To
				//  ⑤ 催し区分=0：レギュラーの場合、【画面】.「販売期間」編集不可。
				disobjids = disobjids.concat([$.id_inp.txt_hbstdt, $.id_inp.txt_hbeddt]);
			}

			// 登録種別＋メニュー(月間販売or特売・スポット)＋催し種類による項目制御判断
			// 入出力データ仕様(スポットTG016_1～5共通)参照
			if(that.judgeRepType.frm1){
				if(that.judgeRepType.tg){		// メニュー：月間販売計画
					// 【画面一般情報部分】
					hidobjids = hidobjids.concat([$.id.btn_copy+1,$.id.btn_copy+2]);		// 前複写ボタン
					disobjids = disobjids.concat([$.id.chk_hbslideflg, $.id.chk_nhslideflg, $.id.sel_bycd, $.id_inp.txt_shncd, $.id_inp.txt_hbstdt, $.id_inp.txt_hbeddt, $.id_inp.txt_nnstdt, $.id_inp.txt_nneddt]);
					disobjids = disobjids.concat([$.id_inp.txt_rankno_add,$.id.btn_rankno+"_add_a", $.id_inp.txt_rankno_del,$.id.btn_rankno+"_del", $.id_inp.txt_hbyoteisu]);
					disobjids = disobjids.concat($('#spread_tjten').find(":input").map(function(index, element){ return $(this).attr("id"); }).get());	// 対象除外マスタ部分
					disobjids = disobjids.concat($('#spread_mae').find(":input").map(function(index, element){ return $(this).attr("id"); }).get());	// 特売事前行
					disobjids = disobjids.concat($('#spread_ato').find(":input").map(function(index, element){ return $(this).attr("id"); }).get());	// 特売追加行
					disobjids = disobjids.concat($('#spread_b').find(":input").map(function(index, element){ return $(this).attr("id"); }).get());		// B部分
					disobjids = disobjids.concat($('#spread_c').find(":input").map(function(index, element){ return $(this).attr("id"); }).get());		// C部分
					// 【販売情報部分】
					disobjids = disobjids.concat([$.id.chk_plusndflg,$.id.chk_yoriflg]);
					disobjids = disobjids.concat($('#spread_ko').find(":input").map(function(index, element){ return $(this).attr("id"); }).get());		// 一個売り部分
					disobjids = disobjids.concat($('#spread_bd1').find(":input").map(function(index, element){ return $(this).attr("id"); }).get());	// バンドル1部分
					disobjids = disobjids.concat($('#spread_bd2').find(":input").map(function(index, element){ return $(this).attr("id"); }).get());	// バンドル2部分
					// 【納入情報部分】
					that.judgeRepType.tabnn = false;					// 納入情報

				}else if(that.judgeRepType.st){	// メニュー：特売・スポット計画
					if(that.judgeRepType.toktg_t){			// 催し種類：アンケート有(チラシのみ)
						// 【画面一般情報部分】
						reqobjids = reqobjids.concat([$.id.sel_bycd, $.id_inp.txt_shncd, $.id_inp.txt_hbstdt, $.id_inp.txt_hbeddt,$.id_inp.txt_a_baikaam+1, $.id_inp.txt_irisu+1]);
						disobjids = disobjids.concat([$.id.chk_hbslideflg, $.id.chk_nhslideflg, $.id_inp.txt_nnstdt, $.id_inp.txt_nneddt]);
						disobjids = disobjids.concat([$.id_inp.txt_rankno_add,$.id.btn_rankno+"_add_a"]);
						disobjids = disobjids.concat($('#spread_tjten').find(":input").map(function(index, element){ return $(this).attr("id"); }).get());	// 対象除外マスタ部分
						// 【販売情報部分】
						reqobjids = reqobjids.concat([$.id_inp.txt_popkn]);
						disobjids = disobjids.concat([$.id.chk_plusndflg]);
						// 【納入部分】
						that.judgeRepType.tabnn = false;				// 納入情報

					}else if(that.judgeRepType.toktg_h){	// 催し種類：アンケート有(販売納入)
						// 【画面一般情報部分】
						reqobjids = reqobjids.concat([$.id.sel_bycd, $.id_inp.txt_shncd, $.id_inp.txt_hbstdt, $.id_inp.txt_hbeddt, $.id_inp.txt_rankno_add, $.id_inp.txt_a_baikaam+1, $.id_inp.txt_irisu+1]);
						disobjids = disobjids.concat([$.id.chk_hbslideflg, $.id.chk_nhslideflg]);
						// 【販売情報部分】
						reqobjids = reqobjids.concat([$.id_inp.txt_popkn]);
						// 【納入情報部分】
						reqobjids = reqobjids.concat([$.id_inp.txt_binkbn,$.id_inp.txt_bdenkbn,$.id_inp.txt_wappnkbn,$.id.chk_shudenflg]);

					}else{									// 催し種類：アンケート無
						// 【画面一般情報部分】
						reqobjids = reqobjids.concat([$.id.sel_bycd, $.id_inp.txt_shncd, $.id_inp.txt_rankno_add, $.id_inp.txt_a_baikaam+1, $.id_inp.txt_irisu+1]);
						disobjids = disobjids.concat([$.id.chk_hbslideflg, $.id.chk_nhslideflg]);
						// 【販売情報部分】
						reqobjids = reqobjids.concat([$.id_inp.txt_popkn]);
						// 【納入情報部分】
						reqobjids = reqobjids.concat([$.id_inp.txt_binkbn,$.id_inp.txt_bdenkbn,$.id_inp.txt_wappnkbn,$.id.chk_shudenflg]);
						disobjids = disobjids.concat([$.id.chk_cuttenflg]);
					}
				}
			}else if(that.judgeRepType.frm2||that.judgeRepType.frm3){
				if(that.judgeRepType.tg){		// メニュー：月間販売計画
					// 【画面一般情報部分】
					hidobjids = hidobjids.concat([$.id.btn_copy+1,$.id.btn_copy+2]);		// 前複写ボタン
					disobjids = disobjids.concat([$.id.chk_hbslideflg, $.id.chk_nhslideflg, $.id.sel_bycd, $.id_inp.txt_shncd, $.id_inp.txt_hbstdt, $.id_inp.txt_hbeddt, $.id_inp.txt_nnstdt, $.id_inp.txt_nneddt]);
					disobjids = disobjids.concat([$.id_inp.txt_rankno_add,$.id.btn_rankno+"_add_a", $.id_inp.txt_rankno_del,$.id.btn_rankno+"_del", $.id_inp.txt_hbyoteisu, $.id.rad_tkanplukbn+1, $.id.rad_tkanplukbn+2]);
					disobjids = disobjids.concat($('#spread_tjten').find(":input").map(function(index, element){ return $(this).attr("id"); }).get());	// 対象除外マスタ部分
					disobjids = disobjids.concat($('#spread_mae').find(":input").map(function(index, element){ return $(this).attr("id"); }).get());	// 特売事前行
					disobjids = disobjids.concat($('#spread_ato').find(":input").map(function(index, element){ return $(this).attr("id"); }).get());	// 特売追加行
					disobjids = disobjids.concat($('#spread_b').find(":input").map(function(index, element){ return $(this).attr("id"); }).get());		// B部分
					disobjids = disobjids.concat($('#spread_c').find(":input").map(function(index, element){ return $(this).attr("id"); }).get());		// C部分
					disobjids = disobjids.concat($('#spread_a_sobaika').find(":input").map(function(index, element){ return $(this).attr("id"); }).get());		// A総売価
					disobjids = disobjids.concat($('#spread_b_sobaika').find(":input").map(function(index, element){ return $(this).attr("id"); }).get());		// B総売価
					disobjids = disobjids.concat($('#spread_c_sobaika').find(":input").map(function(index, element){ return $(this).attr("id"); }).get());		// C総売価
					// 【販売情報部分】
					disobjids = disobjids.concat([$.id.chk_plusndflg,$.id.chk_yoriflg,$.id.chk_namanetukbn+1,$.id.chk_namanetukbn+2,$.id.chk_kaitoflg,$.id.chk_yoshokuflg]);
					disobjids = disobjids.concat($('#spread_ko').find(":input").map(function(index, element){ return $(this).attr("id"); }).get());		// 一個売り部分
					disobjids = disobjids.concat($('#spread_bd1').find(":input").map(function(index, element){ return $(this).attr("id"); }).get());	// バンドル1部分
					disobjids = disobjids.concat($('#spread_bd2').find(":input").map(function(index, element){ return $(this).attr("id"); }).get());	// バンドル2部分
					disobjids = disobjids.concat($('#spread_100g').find(":input").map(function(index, element){ return $(this).attr("id"); }).get());	// 100g相当
					// 【納入情報部分】
					that.judgeRepType.tabnn = false;					// 納入情報

				}else if(that.judgeRepType.st){	// メニュー：特売・スポット計画
					hidobjids = hidobjids.concat([$.id.btn_kyoka]);
					if(that.judgeRepType.toktg_t){			// 催し種類：アンケート有(チラシのみ)
						// 【画面一般情報部分】
						reqobjids = reqobjids.concat([$.id.sel_bycd, $.id_inp.txt_shncd, $.id_inp.txt_hbstdt, $.id_inp.txt_hbeddt]);
						disobjids = disobjids.concat([$.id.chk_hbslideflg, $.id.chk_nhslideflg, $.id_inp.txt_nnstdt, $.id_inp.txt_nneddt]);
						disobjids = disobjids.concat([$.id_inp.txt_rankno_add,$.id.btn_rankno+"_add_a"]);
						disobjids = disobjids.concat($('#spread_tjten').find(":input").map(function(index, element){ return $(this).attr("id"); }).get());	// 対象除外マスタ部分
						// 【販売情報部分】
						reqobjids = reqobjids.concat([$.id_inp.txt_popkn]);
						disobjids = disobjids.concat([$.id.chk_plusndflg]);
						// 【納入部分】
						that.judgeRepType.tabnn = false;				// 納入情報

					}else if(that.judgeRepType.toktg_h){	// 催し種類：アンケート有(販売納入)
						// 【画面一般情報部分】
						reqobjids = reqobjids.concat([$.id.sel_bycd, $.id_inp.txt_shncd, $.id_inp.txt_hbstdt, $.id_inp.txt_hbeddt, $.id_inp.txt_rankno_add]);
						disobjids = disobjids.concat([$.id.chk_hbslideflg, $.id.chk_nhslideflg]);
						// 【販売情報部分】
						reqobjids = reqobjids.concat([$.id_inp.txt_popkn]);
						// 【納入情報部分】
						reqobjids = reqobjids.concat([$.id_inp.txt_binkbn,$.id_inp.txt_bdenkbn,$.id_inp.txt_wappnkbn,$.id.chk_shudenflg]);

					}else{									// 催し種類：アンケート無
						// 【画面一般情報部分】
						reqobjids = reqobjids.concat([$.id.sel_bycd, $.id_inp.txt_shncd, $.id_inp.txt_rankno_add]);
						disobjids = disobjids.concat([$.id.chk_hbslideflg, $.id.chk_nhslideflg]);
						// 【販売情報部分】
						// 【納入情報部分】
						reqobjids = reqobjids.concat([$.id_inp.txt_binkbn,$.id_inp.txt_bdenkbn,$.id_inp.txt_wappnkbn,$.id.chk_shudenflg]);
						disobjids = disobjids.concat([$.id.chk_cuttenflg]);
					}
				}
			}else if(that.judgeRepType.frm4){
				that.judgeRepType.tabnn = false;	// 納入情報表示
				if(that.judgeRepType.tg){		// メニュー：月間販売計画
					// 【画面一般情報部分】
					hidobjids = hidobjids.concat([$.id.btn_copy+1,$.id.btn_copy+2]);		// 前複写ボタン
					disobjids = disobjids.concat([$.id.sel_bycd, $.id_inp.txt_shncd, $.id_inp.txt_hbstdt, $.id_inp.txt_hbeddt, $.id_inp.txt_nnstdt, $.id_inp.txt_nneddt]);
					disobjids = disobjids.concat([$.id_inp.txt_rankno_add,$.id.btn_rankno+"_add_a", $.id_inp.txt_rankno_del,$.id.btn_rankno+"_del", $.id_inp.txt_hbyoteisu]);
					disobjids = disobjids.concat($('#spread_tjten').find(":input").map(function(index, element){ return $(this).attr("id"); }).get());			// 対象除外マスタ部分
					disobjids = disobjids.concat($('#spread_sobaika').find(":input").map(function(index, element){ return $(this).attr("id"); }).get());		// 総売価
					disobjids = disobjids.concat([$.id_inp.txt_rankno_add_b,$.id.btn_rankno+"_add_b",$.id_inp.txt_rankno_add_c,$.id.btn_rankno+"_add_c"]);
					disobjids = disobjids.concat([$.id.chk_hbslideflg]);
					// 【販売情報部分】
					disobjids = disobjids.concat([$.id.chk_plusndflg,$.id.chk_yoriflg]);
					disobjids = disobjids.concat($('#spread_ko').find(":input").map(function(index, element){ return $(this).attr("id"); }).get());		// 一個売り部分
					disobjids = disobjids.concat($('#spread_bd1').find(":input").map(function(index, element){ return $(this).attr("id"); }).get());	// バンドル1部分
					disobjids = disobjids.concat($('#spread_bd2').find(":input").map(function(index, element){ return $(this).attr("id"); }).get());	// バンドル2部分

				}else if(that.judgeRepType.st){	// メニュー：特売・スポット計画
					// 【画面一般情報部分】
					reqobjids = reqobjids.concat([$.id.sel_bycd, $.id_inp.txt_shncd, $.id_inp.txt_hbstdt, $.id_inp.txt_hbeddt,$.id_inp.txt_a_baikaam+3]);
					disobjids = disobjids.concat([$.id_inp.txt_nnstdt, $.id_inp.txt_nneddt]);
					disobjids = disobjids.concat([$.id_inp.txt_rankno_add,$.id.btn_rankno+"_add_a", $.id_inp.txt_rankno_del,$.id.btn_rankno+"_del", $.id_inp.txt_hbyoteisu]);
					disobjids = disobjids.concat($('#spread_tjten').find(":input").map(function(index, element){ return $(this).attr("id"); }).get());			// 対象除外マスタ部分
					disobjids = disobjids.concat([$.id.chk_hbslideflg]);
					// 【販売情報部分】
					reqobjids = reqobjids.concat([$.id_inp.txt_popkn]);
					disobjids = disobjids.concat([$.id.chk_plusndflg]);
				}
			}else if(that.judgeRepType.frm5){
				that.judgeRepType.tabnn = false;	// 納入情報表示
				if(that.judgeRepType.tg){		// メニュー：月間販売計画
					// 【画面一般情報部分】
					hidobjids = hidobjids.concat([$.id.btn_copy+1,$.id.btn_copy+2]);		// 前複写ボタン

					disobjids = disobjids.concat([$.id.chk_hbslideflg, $.id.chk_nhslideflg, $.id.sel_bycd, $.id_inp.txt_shncd, $.id_inp.txt_parno,$.id_inp.txt_chldno,$.id_inp.txt_hbstdt, $.id_inp.txt_hbeddt, $.id_inp.txt_nnstdt, $.id_inp.txt_nneddt]);

					disobjids = disobjids.concat([$.id_inp.txt_rankno_add,$.id.btn_rankno+"_add_a", $.id_inp.txt_rankno_del,$.id.btn_rankno+"_del", $.id_inp.txt_hbyoteisu, $.id.rad_tkanplukbn+1, $.id.rad_tkanplukbn+2]);
					disobjids = disobjids.concat($('#spread_tjten').find(":input").map(function(index, element){ return $(this).attr("id"); }).get());			// 対象除外マスタ部分
					disobjids = disobjids.concat($('#spread_sobaika').find(":input").map(function(index, element){ return $(this).attr("id"); }).get());		// 総売価
					disobjids = disobjids.concat([$.id_inp.txt_rankno_add,$.id.btn_rankno+"_add_a",$.id.chk_htgenbaikaflg]);
					// 【販売情報部分】
					disobjids = disobjids.concat([$.id.chk_plusndflg]);

				}else if(that.judgeRepType.st){	// メニュー：特売・スポット計画
					if(that.judgeRepType.toktg_t){			// 催し種類：アンケート有(チラシのみ)
						// 【画面一般情報部分】
						reqobjids = reqobjids.concat([$.id.sel_bycd, $.id_inp.txt_shncd, $.id_inp.txt_hbstdt, $.id_inp.txt_hbeddt,$.id_mei.kbn10656+'_a']);
						disobjids = disobjids.concat([$.id_inp.txt_parno,$.id_inp.txt_chldno]);
						disobjids = disobjids.concat([$.id.chk_hbslideflg, $.id.chk_nhslideflg]);
						// 【販売情報部分】
						reqobjids = reqobjids.concat([$.id_inp.txt_popkn]);
						disobjids = disobjids.concat([$.id.chk_plusndflg]);
					}else if(that.judgeRepType.toktg_h){	// 催し種類：アンケート有(販売納入)
						// 【画面一般情報部分】
						reqobjids = reqobjids.concat([$.id.sel_bycd, $.id_inp.txt_shncd, $.id_inp.txt_hbstdt, $.id_inp.txt_hbeddt, $.id_inp.txt_rankno_add,$.id_mei.kbn10656+'_a']);
						disobjids = disobjids.concat([$.id_inp.txt_parno,$.id_inp.txt_chldno]);
						disobjids = disobjids.concat([$.id.chk_hbslideflg, $.id.chk_nhslideflg]);
						// 【販売情報部分】
						reqobjids = reqobjids.concat([$.id_inp.txt_popkn]);

					}else{									// 催し種類：アンケート無
						// 【画面一般情報部分】
						reqobjids = reqobjids.concat([$.id.sel_bycd, $.id_inp.txt_shncd, $.id_inp.txt_hbstdt, $.id_inp.txt_hbeddt, $.id_inp.txt_rankno_add,$.id_mei.kbn10656+'_a']);
						disobjids = disobjids.concat([$.id_inp.txt_parno,$.id_inp.txt_chldno]);
						disobjids = disobjids.concat([$.id.chk_hbslideflg, $.id.chk_nhslideflg]);
						// 【販売情報部分】
						reqobjids = reqobjids.concat([$.id_inp.txt_popkn]);
					}

					// 発注原売価適用しない
					if(that.judgeRepType.toktg_t){			// 催し種類：アンケート有(チラシのみ)
						//  ① 新規初期値は1：適用しないに設置する。
						//  ② 部門=11の時のみ変更可。部門=11以外は変更不可。
						if (that.judgeRepType.sei_new) {
							$.setInputboxValue($("#" + $.id.chk_htgenbaikaflg), $.id.value_on);
						} else if (!$('#'+$.id.chk_htgenbaikaflg).is(":checked")) {
							disobjids = disobjids.concat($('#spread_tjten').find(":input").map(function(index, element){ return $(this).attr("id"); }).get());	// 対象除外マスタ部分
						}

						if(txt_bmncd!=="11"){
							disobjids = disobjids.concat([$.id.chk_htgenbaikaflg]);
						}
					}else{
						//  ① 部門=11の時、新規初期値は0：適用するに設置し、変更不可。
						//  ② 部門=11以外は、新規初期値は1：適用しないに設置し、変更可。
						if (txt_bmncd==="11" && that.judgeRepType.sei_new) {
							$.setInputboxValue($("#" + $.id.chk_htgenbaikaflg), $.id.value_off);
							disobjids = disobjids.concat([$.id.chk_htgenbaikaflg]);
						} else if (txt_bmncd==="11" && that.judgeRepType.sei_new) {
							$.setInputboxValue($("#" + $.id.chk_htgenbaikaflg), $.id.value_on);
						}
					}
				}
			}


			// 登録種別＋画面モードによる項目制御判断
			// 入出力データ仕様(遷移仕様)TG016_1～5参照
			if(that.judgeRepType.frm1){
				if(that.judgeRepType.isModeA){	// TG008更新
					hidobjids = hidobjids.concat([$.id.txt_status,$.id.btn_kyoka,$.id.btn_del,$.id.btn_shncd]);
				}
				if(that.judgeRepType.isModeB){	// TG008参照
					hidobjids = hidobjids.concat([$.id.txt_status,$.id.btn_cancel,$.id.btn_upd,$.id.btn_kyoka,$.id.btn_del,$.id.btn_shncd]);
				}
				if(that.judgeRepType.isModeC){	// TG009(更新のみ)
					hidobjids = hidobjids.concat([$.id.btn_upd,$.id.btn_del,$.id.btn_shncd]);
				}
				if(that.judgeRepType.isModeD){	// ST016の新規・新規（全品割引）
					hidobjids = hidobjids.concat([$.id.txt_status,$.id.btn_sub+"_winTG018_h",$.id.btn_sub+"_winTG018_n",$.id.btn_kyoka,$.id.btn_del]);
				}
				if(that.judgeRepType.isModeE){	// ST016の選択（販売・納品情報）とMM001の選択
					hidobjids = hidobjids.concat([$.id.txt_status,$.id.btn_copy+1,$.id.btn_copy+2,$.id.btn_kyoka,$.id.btn_copy+3]);
					// 【画面一般情報部分】
					disobjids = disobjids.concat([$.id_inp.txt_hbstdt, $.id_inp.txt_hbeddt, $.id_inp.txt_nnstdt, $.id_inp.txt_nneddt]);
					disobjids = disobjids.concat([$.id_inp.txt_rankno_add,$.id.btn_rankno+"_add_a", $.id_inp.txt_rankno_del,$.id.btn_rankno+"_del"]);
				}
				if(that.judgeRepType.isModeF){	// ST016の月締後新規・新規（全品割引）	→機能廃止
					hidobjids = hidobjids.concat([$.id.btn_sub+"_winTG018_h",$.id.btn_sub+"_winTG018_n",$.id.btn_kyoka,$.id.btn_del]);
				}
				if(that.judgeRepType.isModeG){	// ST016の月締後今の内容を修正			→機能廃止
					hidobjids = hidobjids.concat([$.id.btn_copy+1,$.id.btn_copy+2,$.id.btn_kyoka,$.id.btn_copy+3]);
					// 【画面一般情報部分】
					disobjids = disobjids.concat([$.id_inp.txt_hbstdt, $.id_inp.txt_hbeddt, $.id_inp.txt_nnstdt, $.id_inp.txt_nneddt]);
					disobjids = disobjids.concat([$.id_inp.txt_rankno_add,$.id.btn_rankno+"_add_a", $.id_inp.txt_rankno_del,$.id.btn_rankno+"_del"]);
				}
				if(that.judgeRepType.isModeH){	// ST019の選択（確定）
					hidobjids = hidobjids.concat([$.id.txt_status,$.id.btn_sub+"_winTG018_h",$.id.btn_sub+"_winTG018_n",$.id.btn_kyoka,$.id.btn_del]);
				}
				if(that.judgeRepType.isModeI){	// 参照モード
					hidobjids = hidobjids.concat([$.id.txt_status,$.id.btn_cancel,$.id.btn_upd,$.id.btn_kyoka,$.id.btn_del,$.id.btn_shncd,$.id.btn_calc,$.id.btn_sub+"_winST012"]);
				}
			}else if(that.judgeRepType.frm2||that.judgeRepType.frm3){
				if(that.judgeRepType.isModeA){	// TG008更新
					hidobjids = hidobjids.concat([$.id.txt_status,$.id.btn_kyoka,$.id.btn_del,$.id.btn_shncd]);
				}
				if(that.judgeRepType.isModeB){	// TG008参照
					hidobjids = hidobjids.concat([$.id.txt_status,$.id.btn_cancel,$.id.btn_upd,$.id.btn_kyoka,$.id.btn_del,$.id.btn_shncd]);
				}
				if(that.judgeRepType.isModeC){	// TG009(更新のみ)
					hidobjids = hidobjids.concat([$.id.btn_upd,$.id.btn_del,$.id.btn_shncd]);
				}
				if(that.judgeRepType.isModeD){	// ST016の新規・新規（全品割引）
					hidobjids = hidobjids.concat([$.id.txt_status,$.id.btn_sub+"_winTG018_h",$.id.btn_sub+"_winTG018_n",$.id.btn_kyoka,$.id.btn_del]);
				}
				if(that.judgeRepType.isModeE){	// ST016の選択（販売・納品情報）とMM001の選択
					hidobjids = hidobjids.concat([$.id.txt_status,$.id.btn_copy+1,$.id.btn_copy+2,$.id.btn_kyoka,$.id.btn_copy+3]);
					// 【画面一般情報部分】
					disobjids = disobjids.concat([$.id_inp.txt_hbstdt, $.id_inp.txt_hbeddt, $.id_inp.txt_nnstdt, $.id_inp.txt_nneddt]);
					disobjids = disobjids.concat([$.id_inp.txt_rankno_add,$.id.btn_rankno+"_add_a", $.id_inp.txt_rankno_del,$.id.btn_rankno+"_del"]);
				}
				if(that.judgeRepType.isModeF){	// ST016の月締後新規・新規（全品割引）	→機能廃止
					hidobjids = hidobjids.concat([$.id.btn_sub+"_winTG018_h",$.id.btn_sub+"_winTG018_n",$.id.btn_kyoka,$.id.btn_del]);
				}
				if(that.judgeRepType.isModeG){	// ST016の月締後今の内容を修正			→機能廃止
					hidobjids = hidobjids.concat([$.id.btn_copy+1,$.id.btn_copy+2,$.id.btn_kyoka,$.id.btn_copy+3]);
					// 【画面一般情報部分】
					disobjids = disobjids.concat([$.id_inp.txt_hbstdt, $.id_inp.txt_hbeddt, $.id_inp.txt_nnstdt, $.id_inp.txt_nneddt]);
					disobjids = disobjids.concat([$.id_inp.txt_rankno_add,$.id.btn_rankno+"_add_a", $.id_inp.txt_rankno_del,$.id.btn_rankno+"_del"]);
				}
				if(that.judgeRepType.isModeH){	// ST019の選択（確定）
					hidobjids = hidobjids.concat([$.id.txt_status,$.id.btn_sub+"_winTG018_h",$.id.btn_sub+"_winTG018_n",$.id.btn_kyoka,$.id.btn_del]);
				}
				if(that.judgeRepType.isModeI){	// 参照モード
					hidobjids = hidobjids.concat([$.id.txt_status,$.id.btn_cancel,$.id.btn_upd,$.id.btn_kyoka,$.id.btn_del,$.id.btn_shncd,$.id.btn_calc,$.id.btn_sub+"_winST012"]);
				}
			}else if(that.judgeRepType.frm4){
				if(that.judgeRepType.isModeA){	// TG008更新
					hidobjids = hidobjids.concat([$.id.txt_status,$.id.btn_kyoka,$.id.btn_del,$.id.btn_shncd]);
				}
				if(that.judgeRepType.isModeB){	// TG008参照
					hidobjids = hidobjids.concat([$.id.txt_status,$.id.btn_cancel,$.id.btn_upd,$.id.btn_kyoka,$.id.btn_del,$.id.btn_shncd]);
				}
				if(that.judgeRepType.isModeC){	// TG009(更新のみ)
					hidobjids = hidobjids.concat([$.id.btn_upd,$.id.btn_del,$.id.btn_shncd]);
				}
				if(that.judgeRepType.isModeD){	// ST016の新規・新規（全品割引）
					hidobjids = hidobjids.concat([$.id.txt_status,$.id.btn_sub+"_winTG018_h",$.id.btn_sub+"_winTG018_n",$.id.btn_kyoka,$.id.btn_del]);
				}
				if(that.judgeRepType.isModeE){	// ST016の選択（販売・納品情報）とMM001の選択
					hidobjids = hidobjids.concat([$.id.txt_status,$.id.btn_copy+1,$.id.btn_copy+2,$.id.btn_kyoka]);
					// 【画面一般情報部分】
					disobjids = disobjids.concat([$.id_inp.txt_hbstdt, $.id_inp.txt_hbeddt, $.id_inp.txt_nnstdt, $.id_inp.txt_nneddt]);
					disobjids = disobjids.concat([$.id_inp.txt_rankno_add,$.id.btn_rankno+"_add_a", $.id_inp.txt_rankno_del,$.id.btn_rankno+"_del"]);
				}
				if(that.judgeRepType.isModeF){	// ST016の月締後新規・新規（全品割引）	→機能廃止
					hidobjids = hidobjids.concat([$.id.btn_sub+"_winTG018_h",$.id.btn_sub+"_winTG018_n",$.id.btn_kyoka,$.id.btn_del]);
				}
				if(that.judgeRepType.isModeG){	// ST016の月締後今の内容を修正			→機能廃止
					hidobjids = hidobjids.concat([$.id.btn_copy+1,$.id.btn_copy+2,$.id.btn_kyoka]);
					// 【画面一般情報部分】
					disobjids = disobjids.concat([$.id_inp.txt_hbstdt, $.id_inp.txt_hbeddt, $.id_inp.txt_nnstdt, $.id_inp.txt_nneddt]);
					disobjids = disobjids.concat([$.id_inp.txt_rankno_add,$.id.btn_rankno+"_add_a", $.id_inp.txt_rankno_del,$.id.btn_rankno+"_del"]);
				}
				if(that.judgeRepType.isModeH){	// ST019の選択（確定）
					hidobjids = hidobjids.concat([$.id.txt_status,$.id.btn_sub+"_winTG018_h",$.id.btn_sub+"_winTG018_n",$.id.btn_kyoka,$.id.btn_del]);
				}
				if(that.judgeRepType.isModeI){	// 参照モード
					hidobjids = hidobjids.concat([$.id.txt_status,$.id.btn_cancel,$.id.btn_upd,$.id.btn_kyoka,$.id.btn_del,$.id.btn_shncd]);
				}
			}else if(that.judgeRepType.frm5){
				if(that.judgeRepType.isModeA){	// TG008更新
					hidobjids = hidobjids.concat([$.id.txt_status,$.id.btn_kyoka,$.id.btn_del,$.id.btn_shncd]);
				}
				if(that.judgeRepType.isModeB){	// TG008参照
					hidobjids = hidobjids.concat([$.id.txt_status,$.id.btn_cancel,$.id.btn_upd,$.id.btn_kyoka,$.id.btn_del,$.id.btn_shncd]);
				}
				if(that.judgeRepType.isModeC){	// TG009(更新のみ)
					hidobjids = hidobjids.concat([$.id.btn_upd,$.id.btn_del,$.id.btn_shncd]);
				}
				if(that.judgeRepType.isModeD){	// ST016の新規・新規（全品割引）
					hidobjids = hidobjids.concat([$.id.txt_status,$.id.btn_sub+"_winTG018_h",$.id.btn_kyoka,$.id.btn_del]);
				}
				if(that.judgeRepType.isModeE){	// ST016の選択（販売・納品情報）とMM001の選択
					hidobjids = hidobjids.concat([$.id.txt_status,$.id.btn_copy+1,$.id.btn_copy+2,$.id.btn_kyoka]);
					// 【画面一般情報部分】
					disobjids = disobjids.concat([$.id_inp.txt_hbstdt, $.id_inp.txt_hbeddt, $.id_inp.txt_nnstdt, $.id_inp.txt_nneddt]);
					disobjids = disobjids.concat([$.id_inp.txt_rankno_add,$.id.btn_rankno+"_add_a", $.id_inp.txt_rankno_del,$.id.btn_rankno+"_del"]);
				}
				if(that.judgeRepType.isModeF){	// ST016の月締後新規・新規（全品割引）	→機能廃止
					hidobjids = hidobjids.concat([$.id.btn_sub+"_winTG018_h",$.id.btn_kyoka,$.id.btn_del]);
				}
				if(that.judgeRepType.isModeG){	// ST016の月締後今の内容を修正			→機能廃止
					hidobjids = hidobjids.concat([$.id.btn_copy+1,$.id.btn_copy+2,$.id.btn_kyoka]);
					// 【画面一般情報部分】
					disobjids = disobjids.concat([$.id_inp.txt_hbstdt, $.id_inp.txt_hbeddt, $.id_inp.txt_nnstdt, $.id_inp.txt_nneddt]);
					disobjids = disobjids.concat([$.id_inp.txt_rankno_add,$.id.btn_rankno+"_add_a", $.id_inp.txt_rankno_del,$.id.btn_rankno+"_del"]);
				}
				if(that.judgeRepType.isModeH){	// ST019の選択（確定）
					hidobjids = hidobjids.concat([$.id.txt_status,$.id.btn_sub+"_winTG018_h",$.id.btn_kyoka,$.id.btn_del]);
				}
				if(that.judgeRepType.isModeI){	// 参照モード
					hidobjids = hidobjids.concat([$.id.txt_status,$.id.btn_cancel,$.id.btn_upd,$.id.btn_kyoka,$.id.btn_del,$.id.btn_shncd]);
				}
			}

			// 処理タイプによる項目制御判断
			// 新規：
			if(that.judgeRepType.sei_new){
				// 非表示
				$("#disp_record_info").hide();

				// タイトル
				$.initReportInfo("TG016"+frm_no, report_nm + "　商品情報" + txt_addshukbn_nm, "新規");
			// 変更：
			}else if(that.judgeRepType.sei_upd){
				var btn_upd_txt = "更新";
				$("#"+$.id.btn_upd).attr("title", btn_upd_txt).linkbutton({text:btn_upd_txt});

				disobjids = disobjids.concat([$.id_inp.txt_shncd]);

				// タイトル
				$.initReportInfo("TG016"+frm_no, report_nm + "　商品情報" + txt_addshukbn_nm, "変更");
			// 参照：
			}else{
				// 無効
				$.setInputBoxDisable($($.id.hiddenChangedIdx));

				// タイトル
				$.initReportInfo("TG016"+frm_no, report_nm + "　参照　商品情報" + txt_addshukbn_nm, "参照");
			}

			// 各種ボタンイベント設定
			if(disobjids.indexOf($.id.btn_cancel)===-1&&hidobjids.indexOf($.id.btn_cancel)===-1){$('#'+$.id.btn_cancel).on("click", $.pushChangeReport);}	// キャンセル
			if(disobjids.indexOf($.id.btn_kyoka)===-1&&hidobjids.indexOf($.id.btn_kyoka)===-1){$('#'+$.id.btn_kyoka).on("click", that.pushUpdTg016);}		// 許可
			// 【画面一般情報部分】
			if(disobjids.indexOf($.id.btn_copy+1)===-1&&hidobjids.indexOf($.id.btn_copy+1)===-1){$('#'+$.id.btn_copy+1).on("click", that.pushCopy);}		// 前複写1
			if(disobjids.indexOf($.id.btn_copy+2)===-1&&hidobjids.indexOf($.id.btn_copy+2)===-1){$('#'+$.id.btn_copy+2).on("click", that.pushCopy);}		// 前複写2
			// 【納入情報部分】
			if(that.judgeRepType.tabnn){
				if(disobjids.indexOf($.id.btn_copy+3)===-1&&hidobjids.indexOf($.id.btn_copy+3)===-1){$('#'+$.id.btn_copy+3).on("click", that.pushCopy);}	// 前複写3
				if(disobjids.indexOf($.id.btn_calc)===-1&&hidobjids.indexOf($.id.btn_calc)===-1){$('#'+$.id.btn_calc).on("click", that.pushCalc);}			// 再計算
			}

			// 納入情報非表示化
			if(!that.judgeRepType.tabnn){
				hidobjids = hidobjids.concat([$.id.btn_sub+"_winTG018_n"]);
				var tab = $('#tt_1');
				var obj = $('#tt_1_nn');
				tab.tabs('close', tab.tabs('getTabIndex', obj));
				obj.hide();
				obj.find("[tabindex]").filter("[tabindex!=-1]").filter('[disabled!=disabled]').each(function(){
					$.setInputBoxDisable($(this));
				});
			}
			// 無効化
			for (var i = 0; i < disobjids.length; i++) {
				$.setInputBoxDisable($('#'+disobjids[i]));
			}
			// 非表示化
			for (var i = 0; i < hidobjids.length; i++) {
				$.setInputBoxDisable($('#'+hidobjids[i])).hide();
			}
			// 必須化
			for (var i = 0; i < reqobjids.length; i++) {
				var target = $('#'+reqobjids[i]);
				if(target.hasClass('easyui-textbox_')||target.hasClass('easyui-numberbox_') || reqobjids[i]===$.id.chk_shudenflg){
					$.setInputBoxRequired(target);
				}else{	// numberbox,textbox以外は登録前チェックで実施
					that.requiredInputIds.push(reqobjids[i]);
				}
			}

			$($.id.buttons).show();
			// 変更
			$($.id.hiddenChangedIdx).val('');
		},
		clear:function(){
			// 隠し情報初期化
			$($.id.hiddenChangedIdx).val("");						// 変更行Index
		},
		/**
		 * 登録(DB更新)ボタンイベント
		 * @param {Object} e
		 */
		pushUpdTg016:function(e){
			if ($(this).linkbutton('options').disabled)	return false;

			// レポート番号取得
			var reportno=$($.id.hidden_reportno).val();
			// レポート定義位置
			var reportNumber = $.getReportNumber(reportno);
			if (typeof(reportNumber) !== 'number') { alert("レポート定義が見つかりません。"); return false;}

			// JS情報取得
			var that = $.report[reportNumber];
			var id = $(this).attr('id');

			var gridData = that.getGridData();

			// 入力情報を変数に格納
			that.setGridData(gridData);
			var func_ok = function(r){
				// セッションタイムアウト、利用時間外の確認
				var isTimeout = $.checkIsTimeout();
				if (! isTimeout) {
					// ログの書き込み
					$.ajax({
						url: $.reg.easy,
						type: 'POST',
						async: false,
						data: {
							"page"	: reportno ,
							"obj"	: id,
							"sel"	: new Date().getTime(),
							"userid": $($.id.hidden_userid).val(),
							"user"	: $($.id.hiddenUser).val(),
							"report": $($.id.hiddenReport).val(),
							"json"	: ""
						},
						success: function(json){
							that.updSuccess(id);
						}
					});
				}
				return true;
			};
			if($.isFunction(that.updConfirm)) {
				that.updConfirm(func_ok);
			}else{
				$.showMessage("W00001", undefined, func_ok);
			}
		},
		/**
		 * 前複写ボタンイベント
		 * @param {Object} e
		 */
		pushCopy : function(e){
			if ($(this).linkbutton('options').disabled)	return false;

			// レポート番号取得
			var reportno=$($.id.hidden_reportno).val();
			var id = $(this).attr('id');

			// レポート定義位置
			var reportNumber = $.getReportNumber(reportno);
			if (typeof(reportNumber) !== 'number') { alert("レポート定義が見つかりません。"); return false;}

			var that = $.report[reportNumber];

			// セッションタイムアウト、利用時間外の確認
			var isTimeout = $.checkIsTimeout();
			if (! isTimeout) {
				// 前複写1(販売日/納入日)
				if(id===$.id.btn_copy+1){
					// 3.1.2．「前複写」ボタンクリック時処理１（データ取得）
					// 3.1.3．「前複写」ボタンクリック時処理２（データ表示）
					// 3.1.3.1．画面の対象項目をクリアし、3.1.2.で取得した値を画面上に表示する。
					// 3.1.3.2．再クリックされたら、再度画面クリアと保存値の表示を行う。
					if(that.copy1Data.length === 0){
						var param = that.getInputboxParams(that, id, id);
						if (!that.queried) {
							that.copy1Data = $.getSelectListData(that.name, $.id.action_init, id+'_2', param);
							that.setData(that.copy1Data);
							that.copy1Data = [];
						} else {
							that.copy1Data = $.getSelectListData(that.name, $.id.action_init, id, param);
							if(that.copy1Data.length === 0 && that.queried){
								$.showMessage("E11005");
								return false;
							}
							that.setData(that.copy1Data);
						}
					}else{
						that.setData(that.copy1Data);
					}
				}
				// 前複写2(対象店/除外店)
				if(id===$.id.btn_copy+2){
					// 3.2.2．「前複写」ボタンクリック時処理１（データ取得）
					// 3.2.3．「前複写」ボタンクリック時処理２（データ表示）
					// 3.2.3.1．画面の対象項目をクリアし、3.2.2.で取得した値を画面上に表示する。
					// 3.2.3.2．再クリックされたら、再度画面クリアと保存値の表示を行う。
					if(that.copy2Data.length === 0){
						var param = that.getInputboxParams(that, id, id);
						that.copy2Data = $.getSelectListData(that.name, $.id.action_init, id, param);
						if(that.copy2Data.length === 0){
							$.showMessage("E11005");
							return false;
						}
						that.setData(that.copy2Data);
					}else{
						that.setData(that.copy2Data);
					}
				}
				// 前複写3(「納入情報」タブ部分)
				if(id===$.id.btn_copy+3){
					// 3.9.2．「前複写」ボタンクリック時処理１（データ取得）
					// テーブルより【画面】.納入情報タブ上の全入力項目（数値展開方法、店別数量等の子画面情報は含まない）データを取得する。
					// 3.9.3．「前複写」ボタンクリック時処理２（データ表示）
					// 3.9.3.1．画面の対象項目をクリアし、3.9.2.で取得した値を画面上に表示する。
					// 3.9.3.2．再クリックされたら、再度画面クリアと保存値の表示を行う。
					if(that.copy3Data.length === 0){
						var param = that.getInputboxParams(that, id, id);
						that.copy3Data1 = $.getSelectListData(that.name, $.id.action_init, id+"_1", param);
						that.copy3Data2 = $.getSelectListData(that.name, $.id.action_init, id+"_2", param);
						if(that.copy3Data1.length === 0){
							$.showMessage("E11005");
							return false;
						}
						that.setData(that.copy3Data1);
						that.setNndtData(that.copy3Data2, "N");
					}else{
						that.setData(that.copy3Data1);
						that.setNndtData(that.copy3Data2, "N");
					}
				}
			}
		},
		/**
		 * 再計算ボタンイベント
		 * @param {Object} e
		 */
		pushCalc : function(e){
			if ($(this).linkbutton('options').disabled)	return false;

			// レポート番号取得
			var reportno=$($.id.hidden_reportno).val();
			var id = $(this).attr('id');

			// レポート定義位置
			var reportNumber = $.getReportNumber(reportno);
			if (typeof(reportNumber) !== 'number') { alert("レポート定義が見つかりません。"); return false;}

			var that = $.report[reportNumber];

			// エラーチェック
			var msgData = that.checkCalc(that);
			if (!$.isEmptyVal(msgData)) {
				$.showMessage(msgData["msg"], [msgData["prm"]],function(){$.addErrState(that, msgData["target"], true)});
				return false;
			}
			that.setCalc(that);

			// セッションタイムアウト、利用時間外の確認
			var isTimeout = $.checkIsTimeout();
			if (! isTimeout) {
				that.calcRe();
			}
		},
		setCalc:function(that){
			// 情報取得
			var values = {};

			var ptn				= that.getColValue("F154");	// F46	TENKAIKBN		展開方法
			var jskptnsyukbn	= that.getColValue("F157");	// F47	JSKPTNSYUKBN	実績率パタン数値		TODO
			var jskptnznenmkbn	= that.getColValue("F158");	// F48	JSKPTNZNENMKBN	実績率パタン前年同月	TODO
			var jskptnznenwkbn	= that.getColValue("F159");	// F49	JSKPTNZNENWKBN	実績率パタン前年同週	TODO

			values["ZNENKBNM"]	= jskptnznenmkbn;
			values["ZNENKBNW"]	= jskptnznenwkbn;

			var wwmm = '1'; // 週月フラグ 1:週 2:月
			if (jskptnznenmkbn==='1' || jskptnznenmkbn==='2' || jskptnznenmkbn==='3') {
				wwmm = '2';
			}

			var kbn = $.getInputboxValue($('[name='+$.id.rad_tkanplukbn+']').eq(0));	// that.grd_data['F57']

			// 基本情報
			values["SHNCD"]		= that.getColValue("F10");
			values["BMNCD"]		= $.getJSONValue(that.jsonHidden, $.id_inp.txt_bmncd);
			values["PTN"] 		= ptn;	// 1:実績率パターン 2:通常率パターン 3:数量パターン
			values["SHORIDT"]	= $.getInputboxValue($('#'+$.id.txt_shoridt));

			// 対象店作成用情報
			var rankNoAdd = that.getColValue("F21");
			var rankNoDel = that.getColValue("F22");

			values["MOYSKBN"]	= $.getJSONValue(that.jsonHidden, $.id_inp.txt_moyskbn);	// 催し区分
			values["MOYSSTDT"]	= $.getJSONValue(that.jsonHidden, $.id_inp.txt_moysstdt);	// 催し開始日
			values["MOYSRBAN"]	= $.getJSONValue(that.jsonHidden, $.id_inp.txt_moysrban);	// 催し連番
			values["KANRINO"]	= $.getJSONValue(this.jsonHidden, $.id_inp.txt_kanrino);	// 管理番号
			values["KANRIENO"]	= $.getJSONValue(this.jsonHidden, $.id_inp.txt_kanrieno);	// 枝番

			var upd = '0';
			if (!that.queried && !that.judgeRepType.sei_new) {
				upd = '1';
			} else if (!that.judgeRepType.sei_new && $.isEmptyVal(that.getColValue("F163"),true) && $.isEmptyVal(that.baseData[0].F177,true) &&
					$.isEmptyVal(that.getColValue("F164"),true) &&
					$.isEmptyVal(that.getColValue("F165"),true)) {
				upd = '2';
			}

			values["UPD"]		= upd;
			values["RANKNOADD"]	= rankNoAdd;												// ランク№(追加)
			values["RANKNODEL"]	= rankNoDel;												// ランク№(除外)

			// 対象店、対象ランク、除外店作成
			var tencdAdds = [], rankAdds = [], tencdDels = [];
			for (var i = 0; i < 10; i++){
				var add = $.getInputboxValue($('#'+$.id_inp.txt_tencd+"_add"+(i+1)));
				if(!$.isEmptyVal(add)){
					tencdAdds.push(add);
					rankAdds.push($.getInputboxValue($('#'+$.id_inp.txt_tenrank+(i+1))));
				}
				var del = $.getInputboxValue($('#'+$.id_inp.txt_tencd+"_del"+(i+1)));
				if(!$.isEmptyVal(del)){
					tencdDels.push(del);
				}
			}

			values["TENCDADDS"]			= tencdAdds;				// 対象店
			values["RANKADDS"]			= rankAdds;					// 対象ランク
			values["TENCDDELS"]			= tencdDels;				// 除外店

			// 選択パターンによってパラメータの設定を変更
			if (ptn==='2') {

				values["SAVETENRANKARR"]	= that.getColValue("F162");	// 店ランク配列
			} else {
				values["WWMM"]		= wwmm;	// 週月フラグ

				if (wwmm==='2') {
					values["SHUNO"]		= that.nndtData[0]["N93"];	// 週№
				} else {
					values["SHUNO"]		= that.nndtData[0]["N92"];	// 週№
				}

				values["SYUKBN"]	= jskptnsyukbn;				// 実績率パタン数値

				if (wwmm==='1') {
					values["ZNENKBN"] = jskptnznenwkbn; // F49	JSKPTNZNENWKBN	実績率パタン前年同週	TODO
				} else {
					values["ZNENKBN"] = jskptnznenmkbn; // F48	JSKPTNZNENMKBN	実績率パタン前年同月	TODO
				}
			}

			var param = "";
			var value = "";

			for (var i = 0; i < 10; i++) {

				var row = i + 1;
				var chk = $.getInputboxValue($('#'+$.id.chk_nndt+row));
				var ptnno = $.getInputboxValue($('#'+$.id_inp.txt_ptnno + row));
				var htasu = $.getInputboxValue($('#'+$.id_inp.txt_htasu + row));
				var nndt = that.getColValue("N90_"+row);

				values["PTNNO"]	= ptnno;
				values["HTASU"]	= htasu;
				values["NNDT"]	= nndt;

				// 情報設定
				param = [values];
				value = $.getSelectListData(that.name, $.id.action_change, $.id.btn_calc, param);

				if (value.length!==0) {
					$.setInputboxValue($("[col=N9_" + row + "]"),value[0]["F2"]);
					$.setInputboxValue($("[col=N10_" + row + "]"),value[0]["F3"]);
					$.setInputboxValue($("[col=N94_" + row + "]"),value[0]["F1"]);
				}
			}
		},
		checkCalc: function(that){

			var msgData = null;
			var ptn = that.getColValue("F154");	// F46	TENKAIKBN		展開方法
			var msg = "";

			var moyskbn  = $.getJSONValue(this.jsonHidden, $.id_inp.txt_moyskbn);
			var moysstdt = $.getJSONValue(this.jsonHidden, $.id_inp.txt_moysstdt);
			var moysrban = $.getJSONValue(this.jsonHidden, $.id_inp.txt_moysrban);

			// 必須項目＋数値型MIN-MAX
			// EasyUI のフォームメソッド 'validate' 実施
			$('.validatebox-invalid').each(function(){
				var id = $(this).attr('orizinid');
				if (id.indexOf($.id_inp.txt_htasu) !== -1) {
					$.addErrState(that, $(this), false);
					return false;
				}
			});

			// 選択パターンによってパラメータの設定を変更
			if (ptn==='2') {

				var rankNoAdd = that.getColValue("F21");
				var rankNoDel = that.getColValue("F22");
				var bmncd = $.getJSONObject(this.jsonString, $.id_inp.txt_bmncd).value

				// ランク№が未入力
				if (rankNoAdd==="" || rankNoAdd===undefined || rankNoAdd===null) {
					msgData = {msg:"EX1086",prm:"",target:$("[col=F21]")};
					return msgData;
				}

				// 存在しないランク
				var param = {};
				param["KEY"] =  "MST_CNT";
				param["value"] = bmncd + ',' + rankNoAdd;
				var sendId = $.id_inp.txt_rankno;
				if (rankNoAdd*1 >= 900) {
					sendId += '_EX';
					param["value"] = bmncd + ',' + rankNoAdd + ',' + moyskbn + moysstdt + moysrban;
				}
				var chk_cnt = $.getInputboxData(that.name, $.id.action_check, sendId, [param]);
				if(chk_cnt==="" || chk_cnt==="0"){
					msgData = {msg:"E20014",prm:"",target:$("[col=F21]")};
					return msgData;
				}

				if (rankNoDel!=="" && rankNoDel!==undefined && rankNoDel!==null) {
					param["KEY"] =  "MST_CNT";
					param["value"] = bmncd + ',' + rankNoDel;
					sendId = $.id_inp.txt_rankno;
					if (rankNoDel*1 >= 900) {
						sendId += '_EX';
						param["value"] = bmncd + ',' + rankNoDel + ',' + moyskbn + moysstdt + moysrban;
					}
					var chk_cnt = $.getInputboxData(that.name, $.id.action_check, sendId, [param]);
					if(chk_cnt==="" || chk_cnt==="0"){
						msgData = {msg:"E20015",prm:"",target:$("[col=F22]")};
						return msgData;
					}
				}
			}

			var param = "";
			var value = "";

			for (var i = 0; i < 10; i++) {

				var row = i + 1;
				var chk = $.getInputboxValue($('#'+$.id.chk_nndt+row));
				var ptnno = $.getInputboxValue($('#'+$.id_inp.txt_ptnno + row));
				var htasu = $.getInputboxValue($('#'+$.id_inp.txt_htasu + row));
				var bmncd = $.getJSONObject(this.jsonString, $.id_inp.txt_bmncd).value

				if(!$.isEmptyVal(chk, true)) {
					// チェックがありかつ発注総数、パターンがない場合、再計算はエラー
					if (ptn!=='2' && $.isEmptyVal(htasu)) {
						msgData = {msg:"E30012",prm:"発注総数",target:$('#'+$.id_inp.txt_htasu + row)};
						return msgData;
					}
					if (ptn!=='2' && $.isEmptyVal(htasu)) {
						msgData = {msg:"E20360",prm:"発注総数は",target:$('#'+$.id_inp.txt_htasu + row)};
						return msgData;
					}
					if (ptn!=='3' && $.isEmptyVal(ptnno, false)) {
						msgData = {msg:"E30012",prm:"パターン№",target:$('#'+$.id_inp.txt_ptnno + row)};
						return msgData;
					}
				} else {
					// チェックが無いかつ発注総数、パターンがある場合、再計算はエラー
					if (!$.isEmptyVal(htasu, true)) {
						msgData = {msg:"E20357",prm:"",target:$('#'+$.id_inp.txt_htasu + row)};
						return msgData;
					}
					if (!$.isEmptyVal(ptnno, true)) {
						msgData = {msg:"E20358",prm:"",target:$('#'+$.id_inp.txt_ptnno + row)};
						return msgData;
					}
				}

				var param = {};

				if (!$.isEmptyVal(ptnno)) {
					param["KEY"] =  "MST_CNT";
					param["value"] = bmncd + "," + ptnno + "," + moyskbn + "," + moysstdt + "," + moysrban;

					if (ptn==='2') {
						// 数量パターンNo.
						var chk_cnt = $.getInputboxData(that.name, $.id.action_check, $.id_inp.txt_sryptnno, [param]);
						if(chk_cnt==="" || chk_cnt==="0"){
							msgData = {msg:"EX1079",prm:"",target:$('#'+$.id_inp.txt_sryptnno + row)};
							return msgData;
						}
					} else if (ptn==='1') {
						// 通常率パターンNo.
						var chk_cnt = $.getInputboxData(that.name, $.id.action_check, $.id_inp.txt_rtptnno, [param]);
						if(chk_cnt==="" || chk_cnt==="0"){
							msgData = {msg:"EX1080",prm:"",target:$('#'+$.id_inp.txt_rtptnno + row)};
							return msgData;
						}
					} else if (ptn==='3') {

						var jskptnznenmkbn=$("input[col=F158]").val();	// F48	JSKPTNZNENMKBN	実績率パタン前年同月	TODO
						var jskptnznenwkbn=$("input[col=F159]").val();	// F49	JSKPTNZNENWKBN	実績率パタン前年同週	TODO
						if(jskptnznenwkbn*1 > 0){

							if (ptnno*1 <= 3) {
								msgData = {msg:"EX1121",prm:"",target:$('#'+$.id_inp.txt_rtptnno + row)};
								return msgData;
							}

							param["value"] = ptnno;
							var chk_cnt = $.getInputboxData(that.name, $.id.action_check, $.id_inp.txt_shuno, [param]);
							if(chk_cnt==="" || chk_cnt==="0"){
								msgData = {msg:"E20536",prm:"",target:$('#'+$.id_inp.txt_rtptnno + row)};
								return msgData;
							}
						}else if(jskptnznenmkbn*1 > 0 && !chkYm(ptnno)){
							msgData = {msg:"E11012",prm:"年月",target:$('#'+$.id_inp.txt_rtptnno + row)};
							return msgData;
						}

						// 実績率パターンNo.
						param["KEY"] =  "MST_CNT";

						var wwmm = '1'; // 週月フラグ 1:週 2:月
						if (jskptnznenmkbn==='1' || jskptnznenmkbn==='2' || jskptnznenmkbn==='3') {
							wwmm = '2';
						}
						param["value"] = bmncd + "," + ( '000' + bmncd ).slice( -3 ) + wwmm + ptnno + ( '00' + that.getColValue("F160")).slice( -2 ) + ( '00' + that.getColValue("F161")).slice( -2 );

						var chk_cnt = $.getInputboxData(that.name, $.id.action_check, $.id_inp.txt_jrtptnno, [param]);
						if(chk_cnt==="" || chk_cnt==="0"){
							msgData = {msg:"E11035",prm:"",target:$('#'+$.id_inp.txt_ptnno + row)};
							return msgData;
						}
					}
				}
			}
		},
		validation: function (btnId){	// （必須）批准
			var that = this;
			var rt = true;
			// 入力エラーなしの場合に検索条件を格納
			if (rt == true) that.jsonString = that.jsonTemp.slice(0);
			// 入力チェック用の配列をクリア
			that.jsonTemp = [];
			return rt;
		},
		success: function(reportno, sortable, btnId){	// （必須）正処理
			if (sortable) sortable=1; else sortable=0;
			var that = this;
			// 検索実行
			var txt_moyskbn		= $.getJSONObject(this.jsonString, $.id_inp.txt_moyskbn).value;		// 催し区分
			var txt_moysstdt	= $.getJSONObject(this.jsonString, $.id_inp.txt_moysstdt).value;	// 催し開始日
			var txt_moysrban	= $.getJSONObject(this.jsonString, $.id_inp.txt_moysrban).value;	// 催し連番
			var txt_bmncd		= $.getJSONObject(this.jsonString, $.id_inp.txt_bmncd).value;		// 部門コード
			var txt_kanrino		= $.getJSONObject(this.jsonString, $.id_inp.txt_kanrino).value;		// 管理No.
			var txt_kanrieno	= $.getJSONObject(this.jsonString, $.id_inp.txt_kanrieno).value;	// 管理No.枝番	※月間チラシ・特売スポット遷移時キー
			var txt_shncd		= $.getJSONObject(this.jsonString, $.id_inp.txt_shncd).value;		// 商品コード	※催し送信遷移時検索キー
			var txt_addshukbn	= $.getJSONObject(this.jsonString, $.id_inp.txt_addshukbn).value;	// 登録種別
			var txt_moyskbn_c	= $.getJSONObject(this.jsonString, $.id_inp.txt_moyskbn+"_C").value;	// 催し区分
			var txt_moysstdt_c	= $.getJSONObject(this.jsonString, $.id_inp.txt_moysstdt+"_C").value;	// 催し開始日
			var txt_moysrban_c	= $.getJSONObject(this.jsonString, $.id_inp.txt_moysrban+"_C").value;	// 催し連番
			var txt_bmncd_c		= $.getJSONObject(this.jsonString, $.id_inp.txt_bmncd+"_C").value;		// 部門コード
			var txt_kanrino_c	= $.getJSONObject(this.jsonString, $.id_inp.txt_kanrino+"_C").value;	// 管理No.
			var txt_kanrieno_c	= $.getJSONObject(this.jsonString, $.id_inp.txt_kanrieno+"_C").value;	// 管理No.枝番	※月間チラシ・特売スポット遷移時キー


			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$.appendMaskMsg();

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,			// レポート名
					MOYSKBN:		txt_moyskbn,		// 催し区分
					MOYSSTDT:		txt_moysstdt,		// 催し開始日
					MOYSRBAN:		txt_moysrban,		// 催し連番
					BMNCD:			txt_bmncd,			// 部門コード
					KANRINO:		txt_kanrino,		// 管理No.
					KANRIENO:		txt_kanrieno,		// 管理No.枝番	※月間チラシ・特売スポット遷移時キー
					SHNCD:			txt_shncd,			// 商品コード	※催し送信遷移時検索キー
					ADDSHUKBN:		txt_addshukbn,		// 登録種別
					MOYSKBN_C:		txt_moyskbn_c,		// 催し区分
					MOYSSTDT_C:		txt_moysstdt_c,		// 催し開始日
					MOYSRBAN_C:		txt_moysrban_c,		// 催し連番
					BMNCD_C:		txt_bmncd_c,		// 部門コード
					KANRINO_C:		txt_kanrino_c,		// 管理No.
					KANRIENO_C:		txt_kanrieno_c,		// 管理No.枝番	※月間チラシ・特売スポット遷移時キー
					SENDBTNID:		that.sendBtnid,
					PAGEID:			that.callpage,
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
					if(opts){
						that.moycdData	= opts.rows_;
						that.nndtData	= opts.rows_nndt;
						that.hbdtData	= opts.rows_hbdt;
					}

					// 検索結果をうけての初期化設定
					that.searched_initialize(reportno, opts);

					// 計算項目算出のため、変更時処理呼出
					var cals = ["F59"];
					for (var i=0; i<cals.length; i++){
						that.changeInputboxFunc( that, cals[i], that.getParserNum($.getInputboxValue($("[col="+cals[i]+"]"))), $("[col="+cals[i]+"]"), true);
					}
					var cals = [$.id_inp.txt_hbyoteisu,$.id_inp.txt_a_baikaam+"1",$.id_inp.txt_genkaam_ato];
					for (var i=0; i<cals.length; i++){
						that.changeInputboxFunc( that, cals[i], $.getInputboxValue($("#"+cals[i])), $("#"+cals[i]), true);
					}

					that.calcRe();

					var kbn = $.getInputboxValue($('[name='+$.id.rad_tkanplukbn+']').eq(0));	// that.grd_data['F57']
					if ((that.judgeRepType.frm2 || that.judgeRepType.frm3) && (kbn==='1' || kbn==='2')) {
						that.changeInputboxFunc(that, $.id.rad_tkanplukbn);
					}

					// 現在情報を変数に格納(追加した情報については個別にロード成功時に実施)
					that.setGridData(that.getGridData("grd_data"), "grd_data");

					// 隠し情報初期化
					$($.id.hiddenChangedIdx).val("");						// 変更行Index
					that.queried = true;
					// 状態保存
					$.saveState2(reportno, that.getJSONString());

					// ログ出力
					$.log(that.timeData, 'loaded:');
				}
			);
		},
		getParserNum:function (val){
			return val.replace(/,/g,"");
		},
		getColValue:function (id, isN){
			var that = this;
			var ids = id.split(",");
			var colid = null;
			var visible=false;
			if(ids.length > 1){
				var kbn = $.getInputboxValue($('[name='+$.id.rad_tkanplukbn+']').eq(0));	// that.grd_data['F57']
				if (that.judgeRepType.frm1) {
					kbn = '1';
				}

				if (kbn==='2') {
					colid=ids[ids.length-1];
				} else {
					for (var i=0; i<ids.length; i++){
						$("[col="+ids[i]+"]").each(function(){
							var textbox = $.getInputboxTextbox($(this));
							if(textbox.is(":visible")){
								colid=ids[i];
								visible=true;
							}
						});

						if(visible){
							break;
						}
					}
				}

				if(colid===null && visible){
					colid=ids[0];
				}
			}else{
				colid = id;
			}
			var val = "";
			if($("[col="+colid+"]").length !== 0){
				if (["F101","F102","F108","F110","F131"].indexOf(colid)!==-1) {
					if (colid!=='F108' && colid!=='F110') {
						if ($.getInputboxValue($("[col="+colid+"]"))==='-1') {
							val="";
						} else {
							val=$.getInputboxValue($("[col="+colid+"]"));
						}
					} else if (colid==='F108') {
						val = that.selGentei;
					} else {
						val = that.selTni;
					}
				} else if ($("[col="+colid+"]").length >= 2) {
					$("[col="+colid+"]").each(function(){
						var textbox = $.getInputboxTextbox($(this));
						var getCol = $(this).attr('col');
						if (textbox[0].tagName==='SPAN' && getCol.slice(0,1)==='F') {
							if (getCol.slice(1)*1 >=54 && getCol.slice(1)*1 <=98) {
								val = $.getInputboxValue($(this)).replace(',','');
							}
						} else {
							if(textbox.is(":visible")){
								val = $.getInputboxValue($(this));
							}
						}
					});
				} else {
					if (!$.isEmptyVal(colid) && ($("[col="+colid+"]").attr('type')==='hidden' || colid.substring(0,1)==='N')) {
						val = $.getInputboxValue($("[col="+colid+"]"));
					} else {
						var textbox = $.getInputboxTextbox($("[col="+colid+"]"));
						if (textbox[0].tagName==='SPAN' && colid.slice(0,1)==='F') {
							if (colid.slice(1)*1 >=54 && colid.slice(1)*1 <=98) {
								val = $.getInputboxValue($("[col="+colid+"]")).replace(',','');
							}
						} else {
							if(textbox.is(":visible")){
								val = $.getInputboxValue($("[col="+colid+"]"));
							} else if(that.judgeRepType.tabnn && ($("[col="+colid+"]").parents('#tt_1_nn').length===1 || $("[col="+colid+"]").parents('#tt_1_hb').length===1)){
								val = $.getInputboxValue($("[col="+colid+"]"));
							}
						}
					}
				}
			} else if (!$.isEmptyVal(colid)) {
				val = this.baseData[0][colid];
			}
			if(isN){
				return that.getParserNum(val);
			}
			return val;
		},
		getGridData: function (target,  delFlg){
			var that = this;

			var data = {};

			var moyskbn  = $.getJSONValue(this.jsonHidden, $.id_inp.txt_moyskbn);
			var moysstdt = $.getJSONValue(this.jsonHidden, $.id_inp.txt_moysstdt);
			var moysrban = $.getJSONValue(this.jsonHidden, $.id_inp.txt_moysrban);
			var bmncd    = $.getJSONValue(this.jsonHidden, $.id_inp.txt_bmncd);
			var kanrino  = $.getJSONValue(this.jsonHidden, $.id_inp.txt_kanrino);
			var kanrieno = $.getJSONValue(this.jsonHidden, $.id_inp.txt_kanrieno);
			var addshukbn= $.getJSONValue(this.jsonHidden, $.id_inp.txt_addshukbn);

			var flg = that.getColValue("F171");
			var sendBtnid = $.getJSONValue(that.jsonHidden, "sendBtnid");
			if ("Out_ReportTG009"===that.callpage && sendBtnid===$.id.btn_sel_change) {
				flg = "1";
			}

			// 基本情報
			if(target===undefined || target==="grd_data"){
				var targetData = [];
				var rowData = null;
				// 生食・加熱区分
				var chk_namanetukbn = $("[name="+$.id.chk_namanetukbn+"]:checked").eq(0).val();

				var genkaam_mae = that.getColValue("F63", true);
				var genkaam_ato = that.getColValue("F68", true)
				if(that.judgeRepType.frm1||that.judgeRepType.frm2||that.judgeRepType.frm3){
					//  16_1,16_2,16_3
					//   ① 【画面】.特売事前行の「原価」と【画面】.特売追加行の「原価」どちらか必須。入力がない場合、【画面】.特売事前行の「原価」をコピーする。
					if($.isEmptyVal(genkaam_mae)){
						genkaam_mae = genkaam_ato;
					}
					if($.isEmptyVal(genkaam_ato)){
						genkaam_ato = genkaam_mae;
					}
				}

				var ko_a_baikaan = that.getColValue("F113");
				var ko_b_baikaan = that.getColValue("F114");
				var a_baikaam = that.getColValue("F64,F81");
				var b_baikaam = that.getColValue("F73,F87");

				if (that.judgeRepType.frm4 && !$.isEmptyVal(that.getColValue("F117"))) {
					ko_a_baikaan = a_baikaam
				}

				if (that.judgeRepType.frm4 && !$.isEmptyVal(that.getColValue("F118"))) {
					ko_b_baikaan = b_baikaam
				}

				var parNo = $.isEmptyVal(that.getColValue("F13").trim()) ? that.getColValue("F13").trim():('   '+that.getColValue("F13")).slice(-3);
				var juFlg = $.id.value_on===that.getColValue("F139") ? '2':that.getColValue("F139"); // 0:OFF 2:ON

				if(that.judgeRepType.toktg){
					rowData = {
						F1 : ""+moyskbn,			// F1	MOYSKBN	催し区分
						F2 : ""+moysstdt,			// F2	MOYSSTDT	催し開始日
						F3 : ""+moysrban,			// F3	MOYSRBAN	催し連番
						F4 : ""+bmncd,				// F4	BMNCD	部門
						F5 : ""+kanrino,			// F5	KANRINO	管理番号
						F6 : ""+kanrieno,			// F6	KANRIENO	枝番
						F7 : ""+addshukbn,			// F7	ADDSHUKBN	登録種別
						F8 : that.getColValue("F6"),			// F8	HBSLIDEFLG	1日遅スライド_販売
						F9 : that.getColValue("F7"),			// F9	NHSLIDEFLG	1日遅スライド_納品
						F10: that.getColValue("F9"),			// F10	BYCD	BYコード
						F11: that.getColValue("F10"),			// F11	SHNCD	商品コード
						F12: parNo,								// F12	PARNO	親No
						F13: that.getColValue("F14"),			// F13	CHLDNO	子No
						F14: that.getColValue("F15"),			// F14	HIGAWRFLG	日替フラグ
						F15: that.getColValue("F16"),			// F15	HBSTDT	販売期間_開始日
						F16: that.getColValue("F17"),			// F16	HBEDDT	販売期間_終了日
						F17: that.getColValue("F18"),			// F17	NNSTDT	納入期間_開始日
						F18: that.getColValue("F19"),			// F18	NNEDDT	納入期間_終了日
						F19: that.getColValue("F20"),			// F19	CHIRASFLG	チラシ未掲載
						F20: that.getColValue("F21"),			// F20	RANKNO_ADD	対象店ランク
						F21: that.getColValue("F53"),			// F21	HBYOTEISU	販売予定数
						F22: genkaam_mae,		// F22	GENKAAM_MAE	原価_特売事前
						F23: genkaam_ato,		// F23	GENKAAM_ATO	原価_特売追加
						F24: that.getColValue("F64,F81"),		// F24	A_BAIKAAM	A売価（100ｇ）
						F25: that.getColValue("F73,F87"),		// F25	B_BAIKAAM	B売価（100ｇ）
						F26: that.getColValue("F77,F93"),		// F26	C_BAIKAAM	C売価（100ｇ）
						F27: that.getColValue("F66,F86"),		// F27	IRISU	入数
						F28: that.getColValue("F99"),			// F28	HTGENBAIKAFLG	発注原売価適用フラグ
						F29: that.getColValue("F100"),			// F29	A_WRITUKBN	A売価_割引率区分
						F30: that.getColValue("F101"),			// F30	B_WRITUKBN	B売価_割引率区分
						F31: that.getColValue("F102"),			// F31	C_WRITUKBN	C売価_割引率区分
						F32: that.getColValue("F57"),			// F32	TKANPLUKBN	定貫PLU・不定貫区分
						F33: that.getColValue("F83"),			// F33	A_GENKAAM_1KG	A売価_1㎏
						F34: that.getColValue("F89"),			// F34	B_GENKAAM_1KG	B売価_1㎏
						F35: that.getColValue("F95"),			// F35	C_GENKAAM_1KG	C売価_1㎏
						F36: that.getColValue("F84"),			// F36	GENKAAM_PACK	パック原価
						F37: that.getColValue("F85"),			// F37	A_BAIKAAM_PACK	A売価_パック
						F38: that.getColValue("F91"),			// F38	B_BAIKAAM_PACK	B売価_パック
						F39: that.getColValue("F97"),			// F39	C_BAIKAAM_PACK	C売価_パック
						F40: that.getColValue("F124"),			// F40	A_BAIKAAM_100G	A売価_100ｇ相当
						F41: that.getColValue("F125"),			// F41	B_BAIKAAM_100G	B売価_100ｇ相当
						F42: that.getColValue("F126"),			// F42	C_BAIKAAM_100G	C売価_100ｇ相当
						F43: that.getColValue("F82"),			// F43	GENKAAM_1KG	原価_1㎏
						F44: that.getColValue("F111"),			// F44	PLUSNDFLG	PLU配信フラグ
						F45: that.getColValue("F154"),			// F45	TENKAIKBN	展開方法
						F46: that.getColValue("F157"),			// F46	JSKPTNSYUKBN	実績率パタン数値 TODO
						F47: that.getColValue("F158"),			// F47	JSKPTNZNENMKBN	実績率パタン前年同月 TODO
						F48: that.getColValue("F159"),			// F48	JSKPTNZNENWKBN	実績率パタン前年同週 TODO
						F49: that.getColValue("F160"),			// F49	DAICD	大分類 TODO
						F50: that.getColValue("F161"),			// F50	CHUCD	中分類 TODO
						F51: that.getColValue("F103"),			// F51	SANCHIKN	産地
						F52: that.getColValue("F104"),			// F52	MAKERKN	メーカー名
						F53: that.getColValue("F105"),			// F53	POPKN	POP名称
						F54: that.getColValue("F106"),			// F54	KIKKN	規格名称
						F55: that.getColValue("F107"),			// F55	SEGN_NINZU	制限_先着人数
						F56: that.getColValue("F108"),			// F56	SEGN_GENTEI	制限_限定表現
						F57: that.getColValue("F109"),			// F57	SEGN_1KOSU	制限_一人当たり個数
						F58: that.getColValue("F110"),			// F58	SEGN_1KOSUTNI	制限_一人当たり個数単位
						F59: that.getColValue("F112"),			// F59	YORIFLG	よりどりフラグ
						F60: that.getColValue("F116"),			// F60	BD1_TENSU	点数_バンドル1
						F61: that.getColValue("F120"),			// F61	BD2_TENSU	点数_バンドル2
						F62: ko_a_baikaan,						// F62	KO_A_BAIKAAN	A売価_1個売り
						F63: that.getColValue("F117"),			// F63	BD1_A_BAIKAAN	A売価_バンドル1
						F64: that.getColValue("F121"),			// F64	BD2_A_BAIKAAN	A売価_バンドル2
						F65: ko_b_baikaan,						// F65	KO_B_BAIKAAN	B売価_1個売り
						F66: that.getColValue("F118"),			// F66	BD1_B_BAIKAAN	B売価_バンドル1
						F67: that.getColValue("F122"),			// F67	BD2_B_BAIKAAN	B売価_バンドル2
						F68: that.getColValue("F115"),			// F68	KO_C_BAIKAAN	C売価_1個売り
						F69: that.getColValue("F119"),			// F69	BD1_C_BAIKAAN	C売価_バンドル1
						F70: that.getColValue("F123"),			// F70	BD2_C_BAIKAAN	C売価_バンドル2
						F71: that.getColValue("F131"),			// F71	MEDAMAKBN	目玉区分
						F72: that.getColValue("F132"),			// F72	POPCD	POPコード
						F73: that.getColValue("F133"),			// F73	POPSZ	POPサイズ
						F74: that.getColValue("F134"),			// F74	POPSU	POP枚数
						F75: that.getColValue("F135"),			// F75	SHNSIZE	商品サイズ
						F76: that.getColValue("F136"),			// F76	SHNCOLOR	商品色
						F77: that.getColValue("F137"),			// F77	COMMENT_HGW	その他日替わりコメント
						F78: that.getColValue("F138"),			// F78	COMMENT_POP	POPコメント
						F79: chk_namanetukbn,					// F79	NAMANETUKBN	生食加熱区分
						F80: that.getColValue("F129"),			// F80	KAITOFLG	解凍フラグ
						F81: that.getColValue("F130"),			// F81	YOSHOKUFLG	養殖フラグ
						F82: juFlg,								// F82	JUFLG	事前打出フラグ
						F83: that.getColValue("F140"),			// F83	JUHTDT	事前打出日付
						F84: that.getColValue("F141"),			// F84	COMMENT_TB	特売コメント
						F85: that.getColValue("F142"),			// F85	CUTTENFLG	カット店展開フラグ
						F86: that.getColValue("F143"),			// F86	BINKBN	便区分
						F87: that.getColValue("F144"),			// F87	BDENKBN	別伝区分
						F88: that.getColValue("F145"),			// F88	WAPPNKBN	ワッペン区分
						F89: that.getColValue("F146"),			// F89	SHUDENFLG	週次仕入先伝送フラグ
						F90: that.getColValue("F162"),			// F90	TENRANK_ARR	店ランク配列
						F91: that.getColValue("F170"),			// F91	GTSIMECHGKBN	月締変更理由
						F92: flg,								// F92	GTSIMEOKFLG	月締変更許可フラグ
						F93: that.getColValue("F163"),			// F93	JLSTCREDT	事前発注リスト出力日
						F94: that.getColValue("F164"),			// F94	JHTSUINDT	事前発注数量取込日
						F95: that.getColValue("F165"),			// F95	WEEKHTDT	週間発注処理日
						F96: that.getColValue("F166"),			// F96	MYOSHBSTDT	催し販売開始日
						F97: that.getColValue("F167"),			// F97	MYOSHBEDDT	催し販売終了日
						F98: that.getColValue("F168"),			// F98	MYOSNNSTDT	催し納入開始日
						F99: that.getColValue("F169"),			// F99	MYOSNNEDDT	催し納入終了日
						F104:that.getColValue("F153"),			// F104	UPDDT		更新日時
						F105:that.getColValue("F175"),			// F105	SHOCD		小分類コード
					}
				}else{
					rowData = {
						F1 : ""+moyskbn,			// F1	MOYSKBN	催し区分
						F2 : ""+moysstdt,			// F2	MOYSSTDT	催し開始日
						F3 : ""+moysrban,			// F3	MOYSRBAN	催し連番
						F4 : ""+bmncd,				// F4	BMNCD	部門
						F5 : ""+kanrino,			// F5	KANRINO	管理番号
						F6 : ""+kanrieno,			// F6	KANRIENO	枝番
						F7 : ""+addshukbn,			// F7	ADDSHUKBN	登録種別
						F8 : that.getColValue("F9"),			// F8	BYCD	BYコード
						F9 : that.getColValue("F10"),			// F9	SHNCD	商品コード
						F10: parNo,								// F10	PARNO	親No
						F11: that.getColValue("F14"),			// F11	CHLDNO	子No
						F12: that.getColValue("F15"),			// F12	F15	日替フラグ
						F13: that.getColValue("F16"),			// F13	HBSTDT	販売期間_開始日
						F14: that.getColValue("F17"),			// F14	HBEDDT	販売期間_終了日
						F15: that.getColValue("F18"),			// F15	NNSTDT	納入期間_開始日
						F16: that.getColValue("F19"),			// F16	NNEDDT	納入期間_終了日
						F17: that.getColValue("F20"),			// F17	CHIRASFLG	チラシ未掲載
						F18: that.getColValue("F21"),			// F18	RANKNO_ADD_A	対象店ランク_A売価
						F19: that.getColValue("F76"),			// F19	RANKNO_ADD_B	対象店ランク_B売価
						F20: that.getColValue("F80"),			// F20	RANKNO_ADD_C	対象店ランク_C売価
						F21: that.getColValue("F22"),			// F21	RANKNO_DEL	除外店ランク
						F22: that.getColValue("F53"),			// F22	HBYOTEISU	販売予定数
						F23: genkaam_mae,			// F23	GENKAAM_MAE	原価_特売事前
						F24: genkaam_ato,			// F24	GENKAAM_ATO	原価_特売追加
						F25: that.getColValue("F64,F81"),		// F25	A_BAIKAAM	A売価（100ｇ）
						F26: that.getColValue("F73,F87"),		// F26	B_BAIKAAM	B売価（100ｇ）
						F27: that.getColValue("F77,F93"),		// F27	C_BAIKAAM	C売価（100ｇ）
						F28: that.getColValue("F66,F86"),		// F28	IRISU	入数
						F29: that.getColValue("F99"),			// F29	HTGENBAIKAFLG	発注原売価適用フラグ
						F30: that.getColValue("F100"),			// F30	A_WRITUKBN	A売価_割引率区分
						F31: that.getColValue("F101"),			// F31	B_WRITUKBN	B売価_割引率区分
						F32: that.getColValue("F102"),			// F32	C_WRITUKBN	C売価_割引率区分
						F33: that.getColValue("F57"),			// F33	TKANPLUKBN	定貫PLU・不定貫区分
						F34: that.getColValue("F83"),			// F34	A_GENKAAM_1KG	A売価_1㎏
						F35: that.getColValue("F89"),			// F35	B_GENKAAM_1KG	B売価_1㎏
						F36: that.getColValue("F95"),			// F36	C_GENKAAM_1KG	C売価_1㎏
						F37: that.getColValue("F84"),			// F37	GENKAAM_PACK	パック原価
						F38: that.getColValue("F85"),			// F38	A_BAIKAAM_PACK	A売価_パック
						F39: that.getColValue("F91"),			// F39	B_BAIKAAM_PACK	B売価_パック
						F40: that.getColValue("F97"),			// F40	C_BAIKAAM_PACK	C売価_パック
						F41: that.getColValue("F124"),			// F41	A_BAIKAAM_100G	A売価_100ｇ相当
						F42: that.getColValue("F125"),			// F42	B_BAIKAAM_100G	B売価_100ｇ相当
						F43: that.getColValue("F126"),			// F43	C_BAIKAAM_100G	C売価_100ｇ相当
						F44: that.getColValue("F82"),			// F44	GENKAAM_1KG	原価_1㎏
						F45: that.getColValue("F111"),			// F45	PLUSNDFLG	PLU配信フラグ
						F46: that.getColValue("F154"),			// F46	TENKAIKBN	展開方法
						F47: that.getColValue("F157"),			// F47	JSKPTNSYUKBN	実績率パタン数値	TODO
						F48: that.getColValue("F158"),			// F48	JSKPTNZNENMKBN	実績率パタン前年同月	TODO
						F49: that.getColValue("F159"),			// F49	JSKPTNZNENWKBN	実績率パタン前年同週	TODO
						F50: that.getColValue("F160"),			// F50	DAICD	大分類	TODO
						F51: that.getColValue("F161"),			// F51	CHUCD	中分類	TODO
						F52: that.getColValue("F103"),			// F52	SANCHIKN	産地
						F53: that.getColValue("F104"),			// F53	MAKERKN	メーカー名
						F54: that.getColValue("F105"),			// F54	POPKN	POP名称
						F55: that.getColValue("F106"),			// F55	KIKKN	規格名称
						F56: that.getColValue("F107"),			// F56	SEGN_NINZU	制限_先着人数
						F57: that.getColValue("F108"),			// F57	SEGN_GENTEI	制限_限定表現
						F58: that.getColValue("F109"),			// F58	SEGN_1KOSU	制限_一人当たり個数
						F59: that.getColValue("F110"),			// F59	SEGN_1KOSUTNI	制限_一人当たり個数単位
						F60: that.getColValue("F112"),			// F60	YORIFLG	よりどりフラグ
						F61: that.getColValue("F116"),			// F61	BD1_TENSU	点数_バンドル1
						F62: that.getColValue("F120"),			// F62	BD2_TENSU	点数_バンドル2
						F63: ko_a_baikaan,						// F63	KO_A_BAIKAAN	A売価_1個売り
						F64: that.getColValue("F117"),			// F64	BD1_A_BAIKAAN	A売価_バンドル1
						F65: that.getColValue("F121"),			// F65	BD2_A_BAIKAAN	A売価_バンドル2
						F66: ko_b_baikaan,						// F66	KO_B_BAIKAAN	B売価_1個売り
						F67: that.getColValue("F118"),			// F67	BD1_B_BAIKAAN	B売価_バンドル1
						F68: that.getColValue("F122"),			// F68	BD2_B_BAIKAAN	B売価_バンドル2
						F69: that.getColValue("F115"),			// F69	KO_C_BAIKAAN	C売価_1個売り
						F70: that.getColValue("F119"),			// F70	BD1_C_BAIKAAN	C売価_バンドル1
						F71: that.getColValue("F123"),			// F71	BD2_C_BAIKAAN	C売価_バンドル2
						F72: that.getColValue("F131"),			// F72	MEDAMAKBN	目玉区分
						F73: that.getColValue("F132"),			// F73	POPCD	POPコード
						F74: that.getColValue("F133"),			// F74	POPSZ	POPサイズ
						F75: that.getColValue("F134"),			// F75	POPSU	POP枚数
						F76: that.getColValue("F135"),			// F76	SHNSIZE	商品サイズ
						F77: that.getColValue("F136"),			// F77	SHNCOLOR	商品色
						F78: that.getColValue("F137"),			// F78	COMMENT_HGW	その他日替わりコメント
						F79: that.getColValue("F138"),			// F79	COMMENT_POP	POPコメント
						F80: chk_namanetukbn,					// F80	NAMANETUKBN	生食加熱区分
						F81: that.getColValue("F129"),			// F81	KAITOFLG	解凍フラグ
						F82: that.getColValue("F130"),			// F82	YOSHOKUFLG	養殖フラグ
						F83: juFlg,								// F83	JUFLG	事前打出フラグ
						F84: that.getColValue("F140"),			// F84	JUHTDT	事前打出日付
						F85: that.getColValue("F141"),			// F85	COMMENT_TB	特売コメント
						F86: that.getColValue("F142"),			// F86	CUTTENFLG	カット店展開フラグ
						F87: that.getColValue("F143"),			// F87	BINKBN	便区分
						F88: that.getColValue("F144"),			// F88	BDENKBN	別伝区分
						F89: that.getColValue("F145"),			// F89	WAPPNKBN	ワッペン区分
						F90: that.getColValue("F146"),			// F90	SHUDENFLG	週次仕入先伝送フラグ
						F91: that.getColValue("F162"),			// F91	TENRANK_ARR	店ランク配列
						F92: that.getColValue("F163"),			// F92	JLSTCREDT	事前発注リスト出力日
						F93: that.getColValue("F164"),			// F93	JHTSUINDT	事前発注数量取込日
						F94: that.getColValue("F165"),			// F94	WEEKHTDT	週間発注処理日
						F95: that.getColValue("F166"),			// F95	MYOSHBSTDT	催し販売開始日
						F96: that.getColValue("F167"),			// F96	MYOSHBEDDT	催し販売終了日
						F97: that.getColValue("F168"),			// F97	MYOSNNSTDT	催し納入開始日
						F98: that.getColValue("F169"),			// F98	MYOSNNEDDT	催し納入終了日
						F103:that.getColValue("F153"),			// F104	UPDDT		更新日時
					}
				}
				targetData.push(rowData);
				data["grd_data"] = targetData;
			}

			// 補足情報(テーブルに登録しない情報などを保持)
			if(target===undefined || target==="grd_data_other"){
				var targetData = [];
				var rowData = null;
				rowData = {};

				targetData.push(rowData);
				data["grd_data_other"] = targetData;
			}

			// 関連テーブルデータ
			if(target===undefined || target==="grd_data_tjten"){
				var targetData = [];

				// 変更前の対象除外店情報取得用
				var addTen = 23;
				var addRank = 33;
				var delTen = 43;
				var updKbn = "";

				// 対象店情報
				for (var i = 0; i < 10; i++){
					var idx = i+1;
					var tencd  = $.getInputboxValue($('#'+$.id_inp.txt_tencd+"_add"+idx));
					var tenrank= $.getInputboxValue($('#'+$.id_inp.txt_tenrank+idx), "9");	// ランク空白時9をセット
					var tencd_old = that.baseData[0]["F"+addTen];
					var tenrank_old = that.baseData[0]["F"+addRank];

					if (!$.isEmptyVal(tencd) && $.isEmptyVal(tencd_old)) {
						updKbn = "A";
					} else if (!$.isEmptyVal(tencd_old) && $.isEmptyVal(tencd)) {
						updKbn = "D";
						tencd = tencd_old;
					} else if (!$.isEmptyVal(tencd) && !$.isEmptyVal(tenrank)) {
						updKbn = "U";
					} else {
						updKbn = "";
					}

					if(!$.isEmptyVal(updKbn)){
						var rowData = {
							F1 : ""+moyskbn,				// F1	MOYSKBN	催し区分
							F2 : ""+moysstdt,				// F2	MOYSSTDT	催し開始日
							F3 : ""+moysrban,				// F3	MOYSRBAN	催し連番
							F4 : ""+bmncd,					// F4	BMNCD	部門
							F5 : ""+kanrino,				// F5	KANRINO	管理番号
							F6 : ""+kanrieno,				// F6	KANRIENO	枝番
							F7 : ""+tencd,					// F7	TENCD	店コード
							F8 : 1,							// F8	TJFLG	対象除外フラグ
							F9 : ""+tenrank,				// F9	TENRANK	店ランク
							F10: updKbn
						};
						targetData.push(rowData);
					}
					addTen++;
					addRank++;
				};
				// 除外店情報
				for (var i = 0; i < 10; i++){
					var idx = i+1;
					var tencd  = $.getInputboxValue($('#'+$.id_inp.txt_tencd+"_del"+idx));
					var tencd_old = that.baseData[0]["F"+delTen];

					if (!$.isEmptyVal(tencd) && $.isEmptyVal(tencd_old)) {
						updKbn = "A";
					} else if ($.isEmptyVal(tencd) && !$.isEmptyVal(tencd_old)) {
						updKbn = "D";
						tencd = tencd_old;
					} else if (!$.isEmptyVal(tencd)) {
						updKbn = "U";
					} else {
						updKbn = "";
					}

					if(!$.isEmptyVal(updKbn)){						var rowData = {
							F1 : ""+moyskbn,				// F1	MOYSKBN	催し区分
							F2 : ""+moysstdt,				// F2	MOYSSTDT	催し開始日
							F3 : ""+moysrban,				// F3	MOYSRBAN	催し連番
							F4 : ""+bmncd,					// F4	BMNCD	部門
							F5 : ""+kanrino,				// F5	KANRINO	管理番号
							F6 : ""+kanrieno,				// F6	KANRIENO	枝番
							F7 : ""+tencd,					// F7	TENCD	店コード
							F8 : 2,							// F8	TJFLG	対象除外フラグ
							F9 : " ",						// F9	TENRANK	店ランク
							F10: updKbn
						};
						targetData.push(rowData);
					}
					delTen++;
				};
				data["grd_data_tjten"] = targetData;
			}

			// 関連テーブルデータ
			if(target==="grd_data_tjten_del"){
				var targetData = [];

				// 変更前の対象除外店情報取得用
				var addTen = 23;
				var addRank = 33;
				var delTen = 43;
				var updKbn = "";

				// 対象店情報
				for (var i = 0; i < 10; i++){
					var idx = i+1;
					var tencd = that.baseData[0]["F"+addTen];
					var tenrank = that.baseData[0]["F"+addRank];

					if (!$.isEmptyVal(tencd) && !$.isEmptyVal(tenrank)) {
						updKbn = "D";
					} else {
						updKbn = "";
					}

					if(!$.isEmptyVal(updKbn)){
						var rowData = {
							F1 : ""+moyskbn,				// F1	MOYSKBN	催し区分
							F2 : ""+moysstdt,				// F2	MOYSSTDT	催し開始日
							F3 : ""+moysrban,				// F3	MOYSRBAN	催し連番
							F4 : ""+bmncd,					// F4	BMNCD	部門
							F5 : ""+kanrino,				// F5	KANRINO	管理番号
							F6 : ""+kanrieno,				// F6	KANRIENO	枝番
							F7 : ""+tencd,					// F7	TENCD	店コード
							F8 : 1,							// F8	TJFLG	対象除外フラグ
							F9 : ""+tenrank,				// F9	TENRANK	店ランク
							F10: updKbn
						};
						targetData.push(rowData);
					}
					addTen++;
					addRank++;
				};
				// 除外店情報
				for (var i = 0; i < 10; i++){
					var idx = i+1;
					var tencd = that.baseData[0]["F"+delTen];

					if (!$.isEmptyVal(tencd)) {
						updKbn = "D";
					} else {
						updKbn = "";
					}

					if(!$.isEmptyVal(updKbn)){						var rowData = {
							F1 : ""+moyskbn,				// F1	MOYSKBN	催し区分
							F2 : ""+moysstdt,				// F2	MOYSSTDT	催し開始日
							F3 : ""+moysrban,				// F3	MOYSRBAN	催し連番
							F4 : ""+bmncd,					// F4	BMNCD	部門
							F5 : ""+kanrino,				// F5	KANRINO	管理番号
							F6 : ""+kanrieno,				// F6	KANRIENO	枝番
							F7 : ""+tencd,					// F7	TENCD	店コード
							F8 : 2,							// F8	TJFLG	対象除外フラグ
							F9 : " ",						// F9	TENRANK	店ランク
							F10: updKbn
						};
						targetData.push(rowData);
					}
					delTen++;
				};
				data["grd_data_tjten"] = targetData;
			}

			if(target===undefined || target==="grd_data_nndt"){

				var stdt = $.getInputboxValue($('#'+$.id_inp.txt_nnstdt));
				var eddt = $.getInputboxValue($('#'+$.id_inp.txt_nneddt));
				var oldBin = that.baseData[0]["F143"];
				var targetData = [];

				if(!$.isEmptyVal(stdt)&&!$.isEmptyVal(eddt)){
					for (var i = 0; i < 10; i++){
						var idx = i+1;
						var targetRow = that.nndtData[i];
						var rowData = null;
						var nndt = that.getColValue("N90_"+idx);
						if($.isEmptyVal(nndt, true)){ continue; }

						if(that.judgeRepType.toktg){
							rowData = {
								F1 : ""+moyskbn,			// F1	MOYSKBN	催し区分
								F2 : ""+moysstdt,			// F2	MOYSSTDT	催し開始日
								F3 : ""+moysrban,			// F3	MOYSRBAN	催し連番
								F4 : ""+bmncd,				// F4	BMNCD	部門
								F5 : ""+kanrino,			// F5	KANRINO	管理番号
								F6 : ""+kanrieno,			// F6	KANRIENO	枝番
								F7 : ""+nndt,				// F7	NNDT		納入日
								F8 : $.getInputboxValue($("[col=N94_"+idx+"]")),	// F8	TENHTSU_ARR	店発注数配列
								//F9 : $.getInputboxValue($("[col=N90_"+idx+"]")),	// F9	TENCHGFLG_ARR	店変更フラグ配列	不要
								F10: $.getInputboxValue($("[col=N5_"+idx+"]")),		// F10	HTASU		発注総数
								F11: $.getInputboxValue($("[col=N7_"+idx+"]")),		// F11	PTNNO		パターン№
								F12: $.getInputboxValue($("[col=N8_"+idx+"]")),		// F12	TSEIKBN		訂正区分
								F13: $.getInputboxValue($("[col=N9_"+idx+"]")),		// F13	TPSU		店舗数
								F14: $.getInputboxValue($("[col=N10_"+idx+"]")),	// F14	TENKAISU	展開数
								F15: targetRow.ZJSKFLG,		// F15	ZJSKFLG		前年実績フラグ
								F16: targetRow.WEEKHTDT,	// F16	WEEKHTDT	週間発注処理日
								F17: $.getInputboxValue($("[col=N4_"+idx+"]"))
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
								F8 : $.getInputboxValue($("[col=N94_"+idx+"]")),		// F8	TENHTSU_ARR	店発注数配列
								F9 : $.getInputboxValue($("[col=N5_"+idx+"]")),			// F9	HTASU	発注総数
								F10: $.getInputboxValue($("[col=N7_"+idx+"]")),			// F10	PTNNO	パターン№
								F11: $.getInputboxValue($("[col=N8_"+idx+"]")),			// F11	TSEIKBN	訂正区分
								F12: $.getInputboxValue($("[col=N9_"+idx+"]")),			// F12	TPSU	店舗数
								F13: $.getInputboxValue($("[col=N10_"+idx+"]")),		// F13	TENKAISU	展開数
								F14: targetRow.ZJSKFLG,		// F14	ZJSKFLG	前年実績フラグ
								F15: targetRow.WEEKHTDT,	// F15	WEEKHTDT	週間発注処理日
								F16: $.getInputboxValue($("[col=N4_"+idx+"]")),
								F17: oldBin
							};
							targetData.push(rowData);
						}
					}
				}
				data["grd_data_nndt"] = targetData;
			}
			if(target===undefined || target==="grd_data_hb"){
				var targetData = [];
				if(that.judgeRepType.toksp){

					var arr = '';
					if (that.hbdtData.length!==0) {
						arr = that.hbdtData[0].TENATSUK_ARR;
					}

					var rowData = {
							F1 : ""+moyskbn,			// F1	MOYSKBN	催し区分
							F2 : ""+moysstdt,			// F2	MOYSSTDT	催し開始日
							F3 : ""+moysrban,			// F3	MOYSRBAN	催し連番
							F4 : ""+bmncd,				// F4	BMNCD	部門
							F5 : ""+kanrino,			// F5	KANRINO	管理番号
							F6 : ""+kanrieno,			// F6	KANRIENO	枝番
							F7 : arr,					// F7	TENATSUK_ARR	店扱いフラグ配列	TODO
						};
						targetData.push(rowData);
				}
				data["grd_data_hb"] = targetData;
			}
			if(target==="grd_data_nndt_noupd"){

				var stdt = $.getInputboxValue($('#'+$.id_inp.txt_nnstdt));
				var eddt = $.getInputboxValue($('#'+$.id_inp.txt_nneddt));
				var oldBin = that.baseData[0]["F143"];
				var targetData = [];

				if(!$.isEmptyVal(stdt)&&!$.isEmptyVal(eddt)){
					for (var i = 0; i < 10; i++){
						var idx = i+1;
						var targetRow = that.nndtData[i];
						var rowData = null;
						var nndt = that.getColValue("N90_"+idx);
						if($.isEmptyVal(nndt, true)){ continue; }

						if(that.judgeRepType.toktg){
							rowData = {
								F1 : ""+moyskbn,			// F1	MOYSKBN	催し区分
								F2 : ""+moysstdt,			// F2	MOYSSTDT	催し開始日
								F3 : ""+moysrban,			// F3	MOYSRBAN	催し連番
								F4 : ""+bmncd,				// F4	BMNCD	部門
								F5 : ""+kanrino,			// F5	KANRINO	管理番号
								F6 : ""+kanrieno,			// F6	KANRIENO	枝番
								F7 : ""+nndt,				// F7	NNDT		納入日
								F8 : $.getInputboxValue($("[col=N94_"+idx+"]")),	// F8	TENHTSU_ARR	店発注数配列
								F10: targetRow.HTASU,		// F10	HTASU		発注総数
								F11: targetRow.PTNNO,		// F11	PTNNO		パターン№
								F12: $.getInputboxValue($("[col=N8_"+idx+"]")),		// F12	TSEIKBN		訂正区分
								F13: targetRow.TPSU,		// F13	TPSU		店舗数
								F14: targetRow.TENKAISU,	// F14	TENKAISU	展開数
								F15: targetRow.ZJSKFLG,		// F15	ZJSKFLG		前年実績フラグ
								F16: targetRow.WEEKHTDT,	// F16	WEEKHTDT	週間発注処理日
								F17: targetRow.N4
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
								F8 : $.getInputboxValue($("[col=N94_"+idx+"]")),	// F8	TENHTSU_ARR	店発注数配列
								F9 : targetRow.HTASU,		// F9	HTASU	発注総数
								F10: targetRow.PTNNO,		// F10	PTNNO	パターン№
								F11: $.getInputboxValue($("[col=N8_"+idx+"]")),		// F11	TSEIKBN	訂正区分
								F12: targetRow.TPSU,		// F12	TPSU	店舗数
								F13: targetRow.TENKAISU,	// F13	TENKAISU	展開数
								F14: targetRow.ZJSKFLG,		// F14	ZJSKFLG	前年実績フラグ
								F15: targetRow.WEEKHTDT,	// F15	WEEKHTDT	週間発注処理日
								F16: targetRow.N4,
								F17: oldBin
							};
							targetData.push(rowData);
						}
					}
				}
				data["grd_data_nndt"] = targetData;
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

			// 関連テーブルデータ
			if(target===undefined || target==="grd_data_tjten"){
				that.grd_data_tjten =  data["grd_data_tjten"];
			}
			if(target===undefined || target==="grd_data_nndt"){
				that.grd_data_nndt =  data["grd_data_nndt"];
			}
			if(target===undefined || target==="grd_data_hb"){
				that.grd_data_hb =  data["grd_data_hb"];
			}
			return true;
		},
		updValidation: function (id){	// （必須）批准
			var that = this;

			// 必須項目＋数値型MIN-MAX
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform).form('validate');
			if(!rt){
				$.addErrState(that, $('.validatebox-invalid').eq(0), false);
				return rt;
			}

			// 変更の場合確認メッセージを表示
			var confirmMsg = "";
			that.updConfirmMsg = [];
			// 催しコード.PLU配信済フラグ=1（配信済み）の場合、「催し店舗配信済みのため、変更しても店舗には反映されません。」（警告）を「はい」「いいえ」のダイアログで表示し、処理続行を確認する。
			if(that.moycdData[0]["PLUSFLG"]==="1"){
				confirmMsg = "E20059";
			}

			var nndt = $.getInputboxValue($('#'+$.id_inp.txt_nnstdt));

			if (!$.isEmptyVal(nndt)) {
				var shoriDt = $.getInputboxValue($('#'+$.id.txt_shoridt));

				var dObj = $.convDate(nndt);
				dObj.setDate(dObj.getDate() - 14);
				var wDay = dObj.getDay();

				if (wDay < 4) {
					dObj.setDate(dObj.getDate() + (4-wDay));
				} else if (wDay > 4) {
					dObj.setDate(dObj.getDate() - (wDay-4));
				}
				nndt = $.dateFormat(dObj, 'yyyymmdd');

				if (shoriDt >= nndt) {

					if ($.isEmptyVal(confirmMsg)) {
						confirmMsg = "W20018";
					} else {
						confirmMsg += ",W20018";
					}
				}
			}

			if(!$.isEmptyVal(that.getColValue("F163"),true) || !$.isEmptyVal(that.baseData[0].F177,true) ||
				!$.isEmptyVal(that.getColValue("F164"),true) ||
				!$.isEmptyVal(that.getColValue("F165"),true)){

				var msg = false;

				var emsg = {
						E20465:"",	// E20465:事前発注が済んでいる為に変更できません。
						E20464:"",	// E20464:店変更済みのための納入日、発注数量、パターンNoの変更はできません
				};

				var wmsg = {
						W1:"",	// W20023:事前発注が済んでいますがよろしいですか？
						W2:"",	// W20017:事前発注データと不整合が生ずる可能性がありますが良いですか？
						W3:"",	// W20016:事前発注リストと不整合が発生する可能性がありますがいいですか？
						W4:""	// W20021:事前発注リストの数量とずれが生じますが、よろしいですか？
				};

				// 定貫で総売価(A,B,C総売価)、事前原価、追加原価、入数、訂正区分
				// または不定貫でA総売価行(100g総売価、1kg原価、1kg総売価、P原価、P総売価、入数)、B総売価行・C総売価行(100g総売価、1kg総売価、P総売価)、訂正区分を変更の場合
				var kbn = $.getInputboxValue($('[name='+$.id.rad_tkanplukbn+']').eq(0));	// that.grd_data['F57']
				if (!(that.judgeRepType.frm3 || that.judgeRepType.frm2)) {
					kbn = '1';
				}

				// 定貫:総売価(A,B,C総売価)、事前原価、追加原価、入数、訂正区分
				if (kbn==='1') {

					// 総売価(A,B,C総売価)、事前原価、追加原価、入数
					var genkaam_mae	= $.isEmptyVal(that.baseData[0].F63*1) ? '':that.baseData[0].F63*1; // 原価
					var a_baikaam	= $.isEmptyVal(that.baseData[0].F64*1) ? '':that.baseData[0].F64*1; // A総売価
					var b_baikaam	= $.isEmptyVal(that.baseData[0].F73*1) ? '':that.baseData[0].F73*1; // B総売価
					var c_baikaam	= $.isEmptyVal(that.baseData[0].F77*1) ? '':that.baseData[0].F77*1; // C総売価
					var genkaam_ato	= $.isEmptyVal(that.baseData[0].F68*1) ? '':that.baseData[0].F68*1; // 追加原価
					var irisu		= $.isEmptyVal(that.baseData[0].F66*1) ? '':that.baseData[0].F66*1; // 入数

					// 変更が発生していたら
					if (genkaam_mae!==that.getColValue("F63")*1 ||
							a_baikaam!==that.getColValue("F64")*1 ||
							b_baikaam!==that.getColValue("F73")*1 ||
							c_baikaam!==that.getColValue("F77")*1 ||
							genkaam_ato!==that.getColValue("F68")*1 ||
							irisu!==that.getColValue("F66")*1) {
						msg = true;
					}
				// 不定貫:A総売価行(100g総売価、1kg原価、1kg総売価、P原価、P総売価、入数)、B総売価行・C総売価行(100g総売価、1kg総売価、P総売価)、訂正区分
				} else {

					// A総売価行(100g総売価、1kg原価、1kg総売価、P原価、P総売価、入数)、B総売価行・C総売価行(100g総売価、1kg総売価、P総売価)
					var a_baikaam		= $.isEmptyVal(that.baseData[0].F81*1) ? '':that.baseData[0].F81*1; // 100g総売価(A総売価)
					var b_baikaam		= $.isEmptyVal(that.baseData[0].F87*1) ? '':that.baseData[0].F87*1; // 100g総売価(B総売価)
					var c_baikaam		= $.isEmptyVal(that.baseData[0].F93*1) ? '':that.baseData[0].F93*1; // 100g総売価(C総売価)
					var a_genkaam_1kg	= $.isEmptyVal(that.baseData[0].F83*1) ? '':that.baseData[0].F83*1; // 1kg総売価(A1kg総売価)
					var b_genkaam_1kg	= $.isEmptyVal(that.baseData[0].F89*1) ? '':that.baseData[0].F89*1; // 1kg総売価(B1kg総売価)
					var c_genkaam_1kg	= $.isEmptyVal(that.baseData[0].F95*1) ? '':that.baseData[0].F95*1; // 1kg総売価(C1kg総売価)
					var a_baikaam_pack	= $.isEmptyVal(that.baseData[0].F85*1) ? '':that.baseData[0].F85*1; // P総売価(AP総売価)
					var b_baikaam_pack	= $.isEmptyVal(that.baseData[0].F91*1) ? '':that.baseData[0].F91*1; // P総売価(BP総売価)
					var c_baikaam_pack	= $.isEmptyVal(that.baseData[0].F97*1) ? '':that.baseData[0].F97*1; // P総売価(CP総売価)
					var genkaam_1kg		= $.isEmptyVal(that.baseData[0].F82*1) ? '':that.baseData[0].F82*1; // 1kg原価(A1kg原価)
					var genkaam_pack	= $.isEmptyVal(that.baseData[0].F84*1) ? '':that.baseData[0].F84*1; // P原価
					var irisu			= $.isEmptyVal(that.baseData[0].F86*1) ? '':that.baseData[0].F86*1; // 入数

					// 変更が発生していたら
					if (a_baikaam!==that.getColValue("F81")*1 ||
							b_baikaam!==that.getColValue("F87")*1 ||
							c_baikaam!==that.getColValue("F93")*1 ||
							a_genkaam_1kg!==that.getColValue("F83")*1 ||
							b_genkaam_1kg!==that.getColValue("F89")*1 ||
							c_genkaam_1kg!==that.getColValue("F95")*1 ||
							a_baikaam_pack!==that.getColValue("F85")*1 ||
							b_baikaam_pack!==that.getColValue("F91")*1 ||
							c_baikaam_pack!==that.getColValue("F97")*1 ||
							genkaam_1kg!==that.getColValue("F82")*1 ||
							genkaam_pack!==that.getColValue("F84")*1 ||
							irisu!==that.getColValue("F86")*1) {
						msg = true;
					}
				}

				if (msg) {
					// 全店特売（アンケート有/無）_商品.事前発注処理日<>NULLの場合、「事前発注が済んでいますがよろしいですか？」（警告）を「はい」「いいえ」のダイアログで表示し、処理続行を確認する。
					if (!$.isEmptyVal(that.getColValue("F165"),true)) {
						wmsg.W1 = "W20023";
					}
					// 全店特売（アンケート有/無）_商品.事前発注数量取込日<>NULLの場合、「事前発注データと不整合が生ずる可能性がありますが良いですか？」（警告）を「はい」「いいえ」のダイアログで表示し、処理続行を確認する。
					if (!$.isEmptyVal(that.getColValue("F164"),true)) {
						wmsg.W2 = "W20017";
					}
					// 全店特売（アンケート有/無）_商品.事前発注リスト出力日<>NULLの場合、「事前発注リストと不整合が発生する可能性がありますがいいですか？」（警告）を「はい」「いいえ」のダイアログで表示し、処理続行を確認する。
					if (!$.isEmptyVal(that.getColValue("F163"),true) || !$.isEmptyVal(that.baseData[0].F177,true)) {
						wmsg.W3 = "W20016";
					}
				}

				// 納入タブ
				for (var i = 0; i < 10; i++) {

					var colNo = i+1;
					var tseikbn = $.isEmptyVal(that.nndtData[i].N8) ? '':that.nndtData[i].N8;
					var nndt	= $.isEmptyVal(that.nndtData[i].N4) ? '':that.nndtData[i].N4;
					var htasu	= $.isEmptyVal(that.nndtData[i].N5) ? '':that.nndtData[i].N5;
					var ptnno	= $.isEmptyVal(that.nndtData[i].N7) ? '':that.nndtData[i].N7;
					var inpNndt = $.isEmptyVal($.getInputboxValue($("[col=N4_"+colNo+"]"))) ? '0':$.getInputboxValue($("[col=N4_"+colNo+"]"));

					// E20465 事前発注が済んでいる為に変更できません。
					if (!$.isEmptyVal(that.getColValue("F165"),true)) {

						var shudenflg = $.isEmptyVal(that.baseData[0].F146) ? '':that.baseData[0].F146; // 週次伝送フラグ

						if (shudenflg!=='1') {
							if (nndt!==inpNndt) {
								emsg.E20465 = $("[col=N4_"+colNo+"]");
							}
							if (htasu!==$.getInputboxValue($("[col=N5_"+colNo+"]"))) {
								emsg.E20465 = $("[col=N5_"+colNo+"]");
							}
							if (ptnno!==$.getInputboxValue($("[col=N7_"+colNo+"]"))) {
								emsg.E20465 = $("[col=N7_"+colNo+"]");
							}
						} else {

							var bdenkbn		= $.isEmptyVal(that.baseData[0].F144) ? '':that.baseData[0].F144; // 別伝区分
							var wappnkbn	= $.isEmptyVal(that.baseData[0].F145) ? '':that.baseData[0].F145; // ワッペン区分
							var chgFlg		= $.isEmptyVal($($.id.hiddenChangedIdx+'_winST021_upd')) ? '':$($.id.hiddenChangedIdx+'_winST021_upd'); // 店別数量変更

							if (nndt!==inpNndt) {
								emsg.E20465 = $("[col=N4_"+colNo+"]");
							}
							if (htasu!==$.getInputboxValue($("[col=N5_"+colNo+"]"))) {
								emsg.E20465 = $("[col=N5_"+colNo+"]");
							}
							if (ptnno!==$.getInputboxValue($("[col=N7_"+colNo+"]"))) {
								emsg.E20465 = $("[col=N7_"+colNo+"]");
							}
							if (bdenkbn!==that.getColValue("F144")) {
								emsg.E20465 = $("[col=F144]");
							}
							if (wappnkbn!==that.getColValue("F145")) {
								emsg.E20465 = $("[col=F145]");
							}
							if ($.getConfirmUnregistFlg($($.id.hiddenChangedIdx)) && chgFlg==='1') {
								$.showMessage("E20465");
								return false;
							}
						}
					}

					// E20464 店変更済みのための納入日、発注数量、パターンNoの変更はできません
					if (!$.isEmptyVal(that.getColValue("F164"),true)) {
						if (nndt!==inpNndt) {
							emsg.E20464 = $("[col=N4_"+colNo+"]");
						}
						if (htasu!==$.getInputboxValue($("[col=N5_"+colNo+"]"))) {
							emsg.E20464 = $("[col=N5_"+colNo+"]");
						}
						if (ptnno!==$.getInputboxValue($("[col=N7_"+colNo+"]"))) {
							emsg.E20464 = $("[col=N7_"+colNo+"]");
						}
					}

					// W20021 事前発注リストの数量とずれが生じますが、よろしいですか？
					if (!$.isEmptyVal(that.getColValue("F163"),true) || !$.isEmptyVal(that.baseData[0].F177,true)) {
						if (tseikbn!==$.getInputboxValue($("[col=N8_"+colNo+"]")) ||
								nndt!==inpNndt ||
								htasu!==$.getInputboxValue($("[col=N5_"+colNo+"]")) ||
								ptnno!==$.getInputboxValue($("[col=N7_"+colNo+"]"))) {
							wmsg.W4 = "W20021";
						}
					}

					// 全店特売（アンケート有/無）_商品.事前発注数量取込日<>NULLの場合、「事前発注データと不整合が生ずる可能性がありますが良いですか？」（警告）を「はい」「いいえ」のダイアログで表示し、処理続行を確認する。
					if (kbn==='1') {
						if (!$.isEmptyVal(that.getColValue("F164"),true)) {
							if (tseikbn!==$.getInputboxValue($("[col=N8_"+colNo+"]"))) {
								wmsg.W2 = "W20017";
							}
						}

						// 全店特売（アンケート有/無）_商品.事前発注処理日<>NULLの場合、「事前発注が済んでいますがよろしいですか？」（警告）を「はい」「いいえ」のダイアログで表示し、処理続行を確認する。
						if (!$.isEmptyVal(that.getColValue("F165"),true)) {
							if (tseikbn!==$.getInputboxValue($("[col=N8_"+colNo+"]"))) {
								wmsg.W1 = "W20023";
							}
						}
					}
				}

				if (!$.isEmptyVal(emsg.E20465)) {
					$.showMessage("E20465", undefined, function(){$.addErrState(that, emsg.E20465, true)});
					return false;
				} else if (!$.isEmptyVal(emsg.E20464)) {
					$.showMessage("E20464", undefined, function(){$.addErrState(that, emsg.E20464, true)});
					return false;
				}

				msg = "";

				Object.keys(wmsg).forEach(function (key) {
					var code = wmsg[key];
					if (!$.isEmptyVal(code)) {
						if ($.isEmptyVal(msg)) {
							msg = code;
						} else {
							msg += ","+code;
						}
					}
				});

				if (!$.isEmptyVal(msg)) {
					if ($.isEmptyVal(confirmMsg)) {
						confirmMsg = msg;
					} else {
						confirmMsg += ","+msg;
					}
				}
			}

			if (!$.isEmptyVal(confirmMsg)) {
				that.updConfirmMsg = that.updConfirmMsg.concat([confirmMsg]);
			} else {
				that.updConfirmMsg = "W00001";
			}

			for (var i = 0; i < that.requiredInputIds.length; i++){
				var target = $('#'+that.requiredInputIds[i]);
				if($.isEmptyVal($.getInputboxValue(target), true)){
					$.showMessage("E00001", undefined, function(){$.addErrState(that, target, true)});
					return false;
				}
			}

			// 変更可能項目に対し、入力チェック
			var msgid= null;

			// 入力テキストボックス系
			var inputbox = Object.getOwnPropertyNames($.id_inp);
			for ( var sel in inputbox ) {
				var target = $('#'+$.id_inp[inputbox[sel]]);
				if(target.length > 0 && target.is(':input')){
					msgid = that.checkInputboxFunc($.id_inp[inputbox[sel]], $.getInputboxValue(target), true);
					if(msgid !==null){
						if (msgid==='E20549') {
							$.showMessage(msgid);
						} else if (msgid==='E20304') {
							$.showMessage(msgid, undefined, function(){$.addErrState(that, $('[name="genka"]'), true)});
						} else {
							$.showMessage(msgid, undefined, function(){$.addErrState(that, target, true)});
						}
						return false;
					}
				}
			}
			// 名称マスタ参照系
			var meisyoSelect = Object.getOwnPropertyNames($.id_mei);
			for ( var sel in meisyoSelect ) {
				var target = $('#'+$.id_mei[meisyoSelect[sel]]);
				if(target.length > 0 && target.is(':input')){
					msgid = that.checkInputboxFunc($.id_mei[meisyoSelect[sel]],  $.getInputboxValue(target), true);
					if(msgid !==null){
						$.showMessage(msgid, undefined, function(){$.addErrState(that, target, true)});
						return false;
					}
				}
			}
			// ラジオボタン・チェックボックス系
			var checkOId = [$.id.rad_tkanplukbn, $.id.chk_hbslideflg, $.id.chk_nhslideflg, $.id.chk_higawrflg, $.id.chk_chirasflg, $.id.chk_htgenbaikaflg, $.id.chk_plusndflg, $.id.chk_yoriflg
			 				, $.id.chk_namanetukbn+1, $.id.chk_namanetukbn+2, $.id.chk_kaitoflg, $.id.chk_yoshokuflg, $.id.chk_juflg, $.id.chk_cuttenflg, $.id.chk_shudenflg];
			// 複数存在し、まとめてチェックする項目
			var groupOId = [$.id_inp.txt_tencd+"_add",$.id_inp.txt_tenrank,$.id_inp.txt_tencd+"_del",$.id.chk_nndt,$.id_inp.txt_htasu,$.id_inp.txt_ptnno,$.id_inp.txt_tseikbn];
			// その他
			var targetOId = [$.id_mei.kbn10656+"_a",$.id_mei.kbn10656+"_b", $.id_mei.kbn10656+"_c", $.id.sel_bycd
				, $.id_inp.txt_a_baikaam+1, $.id_inp.txt_b_baikaam+1, $.id_inp.txt_c_baikaam+1, $.id_inp.txt_irisu+1, $.id_inp.txt_a_baikaam+2, $.id_inp.txt_b_baikaam+2, $.id_inp.txt_c_baikaam+2, $.id_inp.txt_irisu+2];
			var ids = checkOId.concat(groupOId).concat(targetOId);

			for (var i = 0; i < ids.length; i++){
				msgid = that.checkInputboxFunc(ids[i], $.getInputboxValue($('#'+ids[i])), true);
				if(msgid !==null){
					var target = $('#'+ids[i]);
					if (msgid.split(",").length >= 2) {
						$.showMessage(msgid.split(",")[0], [msgid.split(",")[1]], function(){$.addErrState(that, target, true)});
					} else {
						$.showMessage(msgid, undefined, function(){$.addErrState(that, target, true)});
					}
					return false;
				}
			}

			// 再計算チェック
			// エラーチェック
			var nndtUpFlg = $.isEmptyVal(that.getColValue("F164"),true) && $.isEmptyVal(that.getColValue("F165"),true);
			if(that.judgeRepType.tabnn){
				var msgData = that.checkCalc(that);
				if (!$.isEmptyVal(msgData)) {
					$.showMessage(msgData["msg"], [msgData["prm"]],function(){$.addErrState(that, msgData["target"], true)});
					return false;
				}
				that.setCalc(that);
			}

			var gridData = that.getGridData();

			// 入力情報を変数に格納
			if (rt == true) that.setGridData(gridData);

			if (!nndtUpFlg) {
				that.grd_data_nndt = [];
				gridData = that.getGridData("grd_data_nndt_noupd");
				that.setGridData(gridData,"grd_data_nndt");
			} else {
				that.calcRe();
			}
			return rt;
		},
		updConfirmShowMessage: function(len, next, func) {
			var that = this;
			if (len===(next+1)) {
				return function(r){
					$.showMessage(that.updConfirmMsg[0].split(',')[next], undefined, func);
				}
			} else {
				return function(r){
					$.showMessage(that.updConfirmMsg[0].split(',')[next], undefined, that.updConfirmShowMessage(len,next+1,func));
				}
			}
		},
		updConfirm: function(func){	// validation OK時 の update処理
			var that = this;
			var len = that.updConfirmMsg[0].split(',').length;
			if (len >= 2) {
				$.showMessage(that.updConfirmMsg[0].split(',')[0], undefined, that.updConfirmShowMessage(len,1,func));
			} else {
				var msgId = that.updConfirmMsg;
				$.showMessage(msgId, undefined, func);
			}
		},
		updSuccess: function(id){	// validation OK時 の update処理
			var that = this;

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$.appendMaskMsg();

			// 変更行情報取得
			var changedIndex = $($.id.hiddenChangedIdx).val().split(",");

			// 基本情報
			var targetData = that.grd_data;
			// 補足情報
			var targetDataOther = that.grd_data_other;
			// 関連テーブルデータ
			var targetDataTjten = that.grd_data_tjten;
			var targetDataNndt = that.grd_data_nndt;
			var targetDataHb = that.grd_data_hb;
			var hbokureflg = $.getInputboxValue($('#'+$.id_inp.txt_hbokureflg)).split('-')[0];

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,			// レポート名
					action:			$.id.action_update,	// 実行処理情報
					obj:			id,					// 実行オブジェクト
					SENDBTNID:		that.sendBtnid,
					PAGEID:			that.callpage,
					DATA:			JSON.stringify(targetData),				// 更新対象情報
					DATA_TJTEN:		JSON.stringify(targetDataTjten),		// 更新対象補足情報
					DATA_NNDT:		JSON.stringify(targetDataNndt),			// 更新対象補足情報
					DATA_HB:		JSON.stringify(targetDataHb),			// 更新対象補足情報
					HBOKUREFLG:		hbokureflg,
					TGFLG:			that.judgeRepType.tg,
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
		delValidation: function (){	// （必須）批准
			var that = this;
			var rt = true;

			// 変更(正)：検索・変更・正ボタン押下時
			var isChange = that.judgeRepType.sei_upd;
			var gridData = that.getGridData();

			// 入力情報を変数に格納
			if (rt == true) that.setGridData(gridData);

			that.grd_data_tjten = [];
			gridData = that.getGridData("grd_data_tjten_del");
			that.setGridData(gridData,"grd_data_tjten");

			return rt;
		},
		delConfirm: function(func){
			var that = this;

			var msgids = [];
			// 催しコード.PLU配信済フラグ=1（配信済み）の場合、「催しの店舗配信済みのため、変更しても店舗に反映されません。削除しますか？」（警告）を「はい」「いいえ」のダイアログで表示し、処理続行を確認する。
			if(that.moycdData[0]["PLUSFLG"]==="1"){
				// W20019	催しの店舗配信済みのため、変更しても店舗に反映されません。削除しますか？	 	4	 	Q
				msgids.push("W20019");
			}
			// 全店特売（アンケート有/無）_商品.週間発注処理日<>NULLの場合、「事前発注が済んでいます。削除してもよろしいですか？」（警告）を「はい」「いいえ」のダイアログで表示し、処理続行を確認する。
			if(!$.isEmptyVal(that.getColValue("F165"))){
				// W20036	事前発注が済んでいます。削除してもよろしいですか？	 	4	 	Q
				msgids.push("W20036");
			}
			// 全店特売（アンケート有/無）_商品.事前発注数量取込日<>NULLの場合、「事前発注データと不整合が生ずる可能性がありますが良いですか？」（警告）を「はい」「いいえ」のダイアログで表示し、処理続行を確認する。
			if(!$.isEmptyVal(that.getColValue("F164"))){
				// W20017	事前発注データと不整合が生ずる可能性がありますが、よろしいですか？	 	4	 	Q
				msgids.push("W20017");
			}
			// 全店特売（アンケート有/無）_商品.事前発注リスト出力日<>NULLの場合、「事前発注リストと不整合が発生する可能性がありますがいいですか？」（警告）を「はい」「いいえ」のダイアログで表示し、処理続行を確認する。
			if(!$.isEmptyVal(that.getColValue("F163")) || !$.isEmptyVal(that.baseData[0].F177)){
				// W20016	事前発注リストと不整合が発生する可能性がありますが、よろしいですか？	 	4	 	Q
				msgids.push("W20016");
			}

			var func_ok = null;
			var msgId = '';
			func_ok = func;
			if(msgids.length === 0){
				msgId = 'W00001';
			}else{
				msgId = msgids[0];
				if(msgids.length === 2){
					func_ok = function(r){
						$.showMessage(msgids[1], undefined, func);
					};
				}else if(msgids.length === 3){
					func_ok = function(r){
						var func_ok_ = function(r){
							$.showMessage(msgids[2], undefined, func);
						};
						$.showMessage(msgids[1], undefined, func_ok_);
					};
				}else if(msgids.length === 4){
					func_ok = function(r){
						var func_ok_2 = function(r){
							$.showMessage(msgids[3], undefined, func);
						};
						var func_ok_1 = function(r){
							$.showMessage(msgids[2], undefined, func_ok_2);
						};
						$.showMessage(msgids[1], undefined, func_ok_1);
					};
				}
			}
			$.showMessage(msgId, undefined, func_ok);
		},
		delSuccess: function(id){
			var that = this;
			var is_warning = false;

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$.appendMaskMsg();

			// 基本情報
			var targetData = that.grd_data;
			// 補足情報
			var targetDataOther = that.grd_data_other;
			// 関連テーブルデータ
			var targetDataTjten = that.grd_data_tjten;
			var targetDataNndt = that.grd_data_nndt;
			var targetDataHb = that.grd_data_hb;

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,		// レポート名
					action:			$.id.action_delete,	// 実行処理情報
					obj:			id,								// 実行オブジェクト
					SENDBTNID:		that.sendBtnid,
					PAGEID:			that.callpage,
					DATA:			JSON.stringify(targetData),				// 更新対象情報
					DATA_OTHER:		JSON.stringify(targetDataOther),		// 更新対象補足情報
					DATA_TJTEN:		JSON.stringify(targetDataTjten),		// 更新対象補足情報
					DATA_NNDT:		JSON.stringify(targetDataNndt),			// 更新対象補足情報
					DATA_HB:		JSON.stringify(targetDataHb),			// 更新対象補足情報
					t:				(new Date()).getTime()
				},
				function(data){
					// 検索処理エラー判定
					if($.delError(id, data)) return false;

					var afterFunc = function(){
						// 初期化
						that.clear();
						that.changeReport(that.name, $.id.btn_del);
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
			// *** hidden情報 ***
			// 催し区分
			this.jsonTemp.push({
				id:		$.id_inp.txt_moyskbn,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_moyskbn),
				text:	''
			});
			// 催しコード（催し開始日）
			this.jsonTemp.push({
				id:		$.id_inp.txt_moysstdt,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_moysstdt),
				text:	''
			});
			// 催し連番
			this.jsonTemp.push({
				id:		$.id_inp.txt_moysrban,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_moysrban),
				text:	''
			});
			// 部門コード
			this.jsonTemp.push({
				id:		$.id_inp.txt_bmncd,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_bmncd),
				text:	''
			});
			// 管理No.
			this.jsonTemp.push({
				id:		$.id_inp.txt_kanrino,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_kanrino),
				text:	''
			});
			// 管理No.枝番
			this.jsonTemp.push({
				id:		$.id_inp.txt_kanrieno,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_kanrieno),
				text:	''
			});
			// 商品コード
			this.jsonTemp.push({
				id:		$.id_inp.txt_shncd,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_shncd),
				text:	''
			});
			// 登録種別
			this.jsonTemp.push({
				id:		$.id_inp.txt_addshukbn,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_addshukbn),
				text:	''
			});

			// 催し区分
			this.jsonTemp.push({
				id:		$.id_inp.txt_moyskbn+"_C",
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_moyskbn+"_C"),
				text:	''
			});
			// 催しコード（催し開始日）
			this.jsonTemp.push({
				id:		$.id_inp.txt_moysstdt+"_C",
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_moysstdt+"_C"),
				text:	''
			});
			// 催し連番
			this.jsonTemp.push({
				id:		$.id_inp.txt_moysrban+"_C",
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_moysrban+"_C"),
				text:	''
			});
			// 部門コード
			this.jsonTemp.push({
				id:		$.id_inp.txt_bmncd+"_C",
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_bmncd+"_C"),
				text:	''
			});
			// 管理No.
			this.jsonTemp.push({
				id:		$.id_inp.txt_kanrino+"_C",
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_kanrino+"_C"),
				text:	''
			});
			// 管理No.枝番
			this.jsonTemp.push({
				id:		$.id_inp.txt_kanrieno+"_C",
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_kanrieno+"_C"),
				text:	''
			});
		},
		getComboErr: function (obj,editable,newValue,oldValue) {
			var data = obj.combobox('getData');

			if (!obj.hasClass('datagrid-editable-input')) {
				$.setComboReload(obj,true)
				if ($.isEmptyVal(newValue)) {
					obj.combobox('setValue',obj.combobox('getData')[0].VALUE);
				} else if ($.isEmptyVal(oldValue)) {
					if (obj.next().find('[tabindex=1]').length===1) {
						obj.combo("textbox").focus();
					}
				}
			}
		},
		setByComboReload: function (target,check) {

			var tag_options = target.attr('data-options');
			if(tag_options){
				tag_options = '\"'+tag_options.replace(/'/g, '').replace(/,/g, '\",\"').replace(/([a-zA-Z0-9]+):/g, '$1\":\"')+'\"';
			}
			var options = tag_options ? JSON.parse('{'+tag_options+'}'): JSON.parse('{}');
			var editable = options && options.editable;

			var data = target.combobox('getData');
			var val = target.next().children('.textbox-value').val();
			var txt = target.combobox('getText');

			for (var i = 0; i < data.length; i++) {

				var dataVal = data[i].TEXT3;
				if (val*1===dataVal*1) {
					val = dataVal;
				}

				if (check && (!(data[i].VALUE==='-1' && (val==='1'||val==='-')) && (data[i].VALUE.indexOf(val) >= 0 || data[i].TEXT.indexOf(txt) >= 0))) {
					return true;
				} else if (!check && ((data[i].TEXT3 === val) || (data[i].VALUE===target.next().children('.textbox-value').val()))) {
					target.combobox('setValue',data[0].VALUE);
					target.combobox('setValue',data[i].VALUE);
					return true;
				}
			}
			return editable;
		},
		setBycd: function(that, reportno, id, topBlank){
			var idx = -1;
			var tag_options = $('#'+id).attr('data-options');
			if(tag_options){
				tag_options = '\"'+tag_options.replace(/'/g, '').replace(/,/g, '\",\"').replace(/([a-zA-Z0-9]+):/g, '$1\":\"')+'\"';
			}

			// 更新項目で参照表示かどうか
			var isRefer = $.isReferUpdateInput(that, $('#'+id), true);
			var readonly = isRefer;
			var onShowPanel = $.fn.combobox.defaults.onShowPanel;
			var editable = true;
			if (isRefer) {
				onShowPanel = function(){
					$('#'+id).combobox('hidePanel');
				};
				editable = false;
			}

			var check = $('#'+id).attr("check") ? JSON.parse('{'+$('#'+id).attr("check")+'}'): JSON.parse('{}');
			var validType = $.fn.textbox.defaults.validType;

			if(check.maxlen){ validType = 'intMaxLen['+check.maxlen+']'}

			$('#'+id).combobox({
				validType:validType,
				url:$.reg.easy,
				//loader: myloader,
				required: true,
				editable: editable,
				autoRowHeight:false,
				panelWidth:150,
				panelHeight:300,
				hasDownArrow: true,
				valueField:'VALUE',
				textField:'TEXT',
				multiple :false,
				prompt: '',
				onShowPanel:onShowPanel,
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
					idx=-1;

					// 情報設定
					var json = [{
						BUMON: $.getJSONValue(that.jsonHidden, $.id_inp.txt_bmncd)
					}];
					if(topBlank){json[0]['TOPBLANK'] = topBlank;}

					param.page		=	reportno;
					param.obj		=	id;
					param.sel		=	(new Date()).getTime();
					param.target	=	id;
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
						if (val==='-1') {
							$('#'+id).combobox('setText', '');
						}
					}
					idx = 1;
					// ログ出力
					$.log(that.timeData, id+' init:');
					// 初期表示処理
					$.initialDisplay(that);
				},
				onChange:function(newValue, oldValue, obj){
					if(obj===undefined){obj = $(this);}

					if(idx > 0 && that.queried && $($.id.hiddenChangedIdx).is(':enabled')){
						$($.id.hiddenChangedIdx).val("1");
					}
					that.changeInputboxFunc(that, id, newValue, obj);
					if(idx > 0){
						$.removeErrState();
					}
					that.getComboErr(obj,false,newValue,oldValue);
				}
			});
			$('#'+id).combobox('panel').addClass("bycd");
			idx = 1;
		},
		setData: function(rows){		// データ表示
			var that = this;
			if(rows.length > 0){
				var intcol = ["F59","F60","F61","F62"];
				var deccol = ["F58"];
				// 設定先の判定：オブジェクトに for_btn,for_inpタグなどを使用して呼出し元と列名が設定されている項目
				var columns = Object.getOwnPropertyNames(rows[0]);
				for ( var idx in columns ) {
					var col = columns[idx];
					var obj = $('#'+that.focusRootId).find('[col='+col+']');
					if(obj){
						// 2.1.4.2.3．催し_デフォルト設定.一日遅スライド_販売、一日遅スライド_納入、カット店展開を参照し、画面に初期表示を行う。ただし、以下の点に注意。
						// ・画面初期表示がDISABLEの場合は初期値設定を行わない
						// ・一日遅れスライドしない（販売、納入）はアンケート有りで、その催しが販売日一日遅れ許可されている場合のみ初期値設定を行う。
						if(that.judgeRepType.sei_new&&["F6","F7"].indexOf(col)!==-1){
							if($(obj).is(":disabled")){ continue; }
							if(that.judgeRepType.tg&&$.getInputboxValue($('#'+$.id_inp.txt_hbokureflg)).split('-')[0]!=='1'){ continue; }
						}

						var val = rows[0][col];
						if(intcol.indexOf(col) !== -1){
							val = $.getFormat(val, '#,##0');
						}else if(deccol.indexOf(col) !== -1){
							val = $.getFormat(val, '#,##0.00');
						}
						if (["F108","F110"].indexOf(col)!==-1) {
							$('[col='+col+']').combobox('setText',val);
						} else if (["F9"].indexOf(col)!==-1) {
							$('[col='+col+']').combobox('setValue',val);
						} else {
							$.setInputboxValue(obj, val);
						}
					}
				}
			}
		},
		setNndtData: function(rows, prefix){		// データ表示
			var that = this;
			for (var i = 0; i < rows.length; i++){
				$('#'+that.focusRootId).find('[col^='+prefix+']').filter('[col$=_'+(i+1)+']').each(function(){
					var col = $(this).attr('col').split('_')[0];
					if(rows[i][col]){
						var val = rows[i][col];
						if(col==='N3'){
							val = val===$.id.value_on?$.id.text_on: $.id.text_off
						}
						$.setInputboxValue($(this), val);
					}
				});
			}

			// 展開方法
			that.changeInputboxFunc(that, $.id_inp.txt_tenkaikbn, $('#'+$.id_inp.txt_tenkaikbn).val(), $('#'+$.id_inp.txt_tenkaikbn));
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
			var callpage = $.getJSONValue(that.jsonHidden, "callpage");
			$.setJSONObject(sendJSON, 'callpage', callpage, callpage);									// 呼出し元レポート情報
			$.setJSONObject(sendJSON, 'reportYobi1', that.reportYobiInfo(), that.reportYobiInfo());		// 参照情報保持

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
				$.setJSONObject(sendJSON, 'sendBtnid', $.id.btn_back, $('#'+$.id.btn_back).attr("title"));					// 実行ボタン情報保持
				// 転送先情報
				for (var i = 0; i < newrepinfos.length; i++) {
					var callpage = newrepinfos[i].id;
					if(callpage==='Out_ReportST016'){
						index = that.repgrpInfo.ST016.idx;
					}
					if(callpage==='Out_ReportST018'){
						index = that.repgrpInfo.ST018.idx;
					}
					if (callpage==='Out_ReportST019') {
						index = that.repgrpInfo.ST019.idx;
					}
					if (callpage==='Out_ReportMM001') {
						var reportYobi1 = $.getJSONValue(that.jsonHidden, "reportYobi1");
						if (reportYobi1 === '0') {
							index = that.repgrpInfo.MM001.idx;
						} else {
							index = that.repgrpInfo.MM001_1.idx;
						}
					}
					if (callpage==='Out_ReportMM002') {
						index = that.repgrpInfo.MM002.idx;
					}
					if (callpage==='Out_ReportTG008') {
						index = that.repgrpInfo.TG008.idx;
					}
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
		keyEventInputboxFunc:function(e, code, that, obj){

			// *** Enter or Tab ****
			if(code === 13 || code === 9){
				var newValue = obj.val();
				var id = $(obj).attr("orizinid");

				if(id===$.id_inp.txt_hbstdt||id===$.id_inp.txt_hbeddt){
					var stdt = id===$.id_inp.txt_hbstdt ? $.getParserDt(newValue): $.getInputboxValue($('#'+$.id_inp.txt_hbstdt));
					var eddt = id===$.id_inp.txt_hbeddt ? $.getParserDt(newValue): $.getInputboxValue($('#'+$.id_inp.txt_hbeddt));
					if(!$.isEmptyVal(stdt)&&!$.isEmptyVal(eddt)){
						$.each(that.editableInputIdsTabHB, function(){
							var option = "";
							var val = "";
							var len = 0;

							if (!$.isEmptyVal($("#" + this)[0].dataset) && !$.isEmptyVal($("#" + this)[0].dataset.options)) {
								len = $("#" + this)[0].dataset.options.split(",").length
							}

							for (var i = 0; i < len; i++) {

								if ($("#" + this)[0].dataset.options.split(",")[i].split(":").length !== 2) {
									continue;
								}

								option = $("#" + this)[0].dataset.options.split(",")[i].split(":")[0];
								if (option === 'required') {
									val = $("#" + this)[0].dataset.options.split(",")[i].split(":")[1];
								}
							}
							if (!that.judgeRepType.isModeI) {
								$.setInputBoxEnableVariable($("#" + this),val);
							}
						});

						$('#'+$.id_mei.kbn10670).combobox('setValue','1');
						$('#'+$.id_mei.kbn10670).combobox('setValue','-1');
						$('#'+$.id_mei.kbn10671).combobox('setValue','1');
						$('#'+$.id_mei.kbn10671).combobox('setValue','-1');

						if (!(!that.judgeRepType.frm5 && that.judgeRepType.toktg_t)) {
							if (!that.judgeRepType.frm5 || (that.judgeRepType.frm5 && !that.judgeRepType.toktg_t)
									|| (that.judgeRepType.frm5 && that.judgeRepType.toktg_t && !$('#'+$.id.chk_htgenbaikaflg).is(":checked"))) {
								if (!that.judgeRepType.isModeI) {
									$.setInputBoxEnableVariable($("#" + $.id.chk_plusndflg),true);
								}
								$.setInputboxValue($("#" + $.id.chk_plusndflg), $.id.value_off);
							}
						}
					}else{
						$.each(that.editableInputIdsTabHB, function(){
							if ($.isEmptyVal(stdt) && $.isEmptyVal(eddt)) {
								if (!$.isEmptyVal($.getInputboxValue($('#'+$.id_inp.txt_shncd)))){
									if (["F104","F105","F106"].indexOf($("#" + this).attr("col"))==-1 &&
											["F104","F105","F106"].indexOf($("#" + this).parent().prev().attr("col"))==-1)
									{
										$.setInputboxValue($("#" + this), "");
									}
								} else {
									$.setInputboxValue($("#" + this), "");
								}
							}
							$.setInputBoxDisableVariable($("#" + this),true);
						});

						if (!(!that.judgeRepType.frm5 && that.judgeRepType.toktg_t)) {
							if (!that.judgeRepType.frm5 || (that.judgeRepType.frm5 && !that.judgeRepType.toktg_t)) {
								$.setInputboxValue($("#" + $.id.chk_plusndflg), $.id.value_on);
								$.setInputBoxDisableVariable($("#" + $.id.chk_plusndflg),true);
							}
						}
					}
				}

				if (id===$.id_mei.kbn10670 || id===$.id_mei.kbn10671 || id===$.id_mei.kbn10660) {
					obj = obj.parent().prev();
					if (!$.setComboReload(obj,false)) {
						obj.combobox('setValue',newValue);
					}
				}

				if (id===$.id_inp.txt_shncd) {
					// DB問い合わせ系
					if($.isEmptyVal(newValue)){
						var rows = $.getSelectListData(that.name, $.id.action_change, id, that.getInputboxParams(that, id, newValue));
						that.setData(rows);
						that.changeInputboxFunc(that, "F59", that.getColValue("F59"), $("[col=F59]"), true);
					}
					that.changeInputboxFunc(that, "F173", that.getColValue("F173", true), $("[col=F173]"), true);
				}

				// フォーカスアウトのタイミングの動作
				if (id===$.id.sel_bycd) {
					var obj = $('#'+id);
					var val = obj.next().children('.textbox-value').val();
					var data = obj.combobox('getData');
					var txt = obj.combobox('getText');
					var flg = false;

					for (var i = 0; i < data.length; i++) {
						var dataVal = data[i].TEXT3;
						if (val*1===dataVal*1) {
							val = dataVal;
						}

						if((data[i].TEXT3 === val) || (data[i].VALUE===obj.next().children('.textbox-value').val())) {
							flg = true;
							break;
						}
					}

					if (!flg || (flg && val==='-1')) {
						obj.combobox('enableValidation');
						if (flg && val==='-1') {
							obj.combobox('setText', '');
						}
					} else {
						obj.combobox('disableValidation');
					}

					that.setByComboReload(obj,false);
				}

				// 100g総売価の計算
				if(id===$.id_inp.txt_a_genkaam_1kg){
					var val = $.getInputboxValue($('#'+id));
					if (!$.isEmptyVal(val)) {
						$.setInputboxValue($('#'+$.id_inp.txt_a_baikaam+2),Math.round(val/10.0));
					} else {
						$.setInputboxValue($('#'+$.id_inp.txt_a_baikaam+2),'');
					}
				}
			}
		},
		changeInputboxFunc:function(that, id, newValue, obj, all){
			var parentObj = $('#'+that.focusRootId);

			// DB問い合わせ系
			if($('[for_inp^='+id+'_]').length > 0){
				var param = that.getInputboxParams(that, id, newValue);
				$.getsetInputboxRowData(that.name, 'for_inp', id, param, that, parentObj);
			}

			if(id==="Out_ReportwinST020"){
				var befData = newValue;

				var tenkaikbn = $("input[col=F154]").val();		// F46	TENKAIKBN	展開方法
				var jskptnsyukbn = $("input[col=F157]").val();	// F47	JSKPTNSYUKBN	実績率パタン数値	TODO
				var jskptnznenmkbn=$("input[col=F158]").val();	// F48	JSKPTNZNENMKBN	実績率パタン前年同月	TODO
				var jskptnznenwkbn=$("input[col=F159]").val();	// F49	JSKPTNZNENWKBN	実績率パタン前年同週	TODO
				var val = "";
				var ptn = "";
				if(tenkaikbn===$.id.value_tenkaikbn_tr){
					val = '通常率pt';
				}else if(tenkaikbn===$.id.value_tenkaikbn_ts){
					val = '通常数pt';
				}else if(tenkaikbn===$.id.value_tenkaikbn_jr){
					var jskptnkbntxt = {"1":"大","2":"中","3":"部"};
					var jskptnsyukbntxt = {"1":"売","2":"点"};
					if(jskptnznenwkbn*1 > 0){
						val = that.nndtData[0]["N92"] + "(" + jskptnkbntxt[jskptnznenwkbn]+jskptnsyukbntxt[jskptnsyukbn] + ")";
						that.jskptn = that.nndtData[0]["N92"].replace("週","");
					}else if(jskptnznenmkbn*1 > 0){
						val = that.nndtData[0]["N93"] + "(" + jskptnkbntxt[jskptnznenmkbn]+jskptnsyukbntxt[jskptnsyukbn] + ")";
						that.jskptn = that.nndtData[0]["N93"].replace("月","");
					}
				}

				var stdt = id===$.id_inp.txt_nnstdt ? $.getParserDt(newValue): $.getInputboxValue($('#'+$.id_inp.txt_nnstdt));
				var eddt = id===$.id_inp.txt_nneddt ? $.getParserDt(newValue): $.getInputboxValue($('#'+$.id_inp.txt_nneddt));
				var enable = false;
				if(!$.isEmptyVal(stdt)&&!$.isEmptyVal(eddt)){
					enable = true;
				}

				$('#'+that.focusRootId).find('[col^=N6_]').text(val);
				$('#'+that.focusRootId).find('[col^=N5_]').each(function(){
					$.setInputboxValue($(this),'')
					if(tenkaikbn===$.id.value_tenkaikbn_ts){
						$.setInputBoxDisableVariable($(this),true);
					}else if (enable) {
						if (!that.judgeRepType.isModeI) {
							$.setInputBoxEnableVariable($(this));
						}
					}
				});

				// クリア
				$("[col^=N5_]").not("[col=N5_11]").each(function(){
					$.setInputboxValue($(this), '');
				});
				$('#'+that.focusRootId).find('[col^=N7_]').each(function(){
					if (enable) {
						if (!that.judgeRepType.isModeI) {
							$.setInputBoxEnableVariable($(this),true);
						}
						$.setInputBoxRequired($(this));
						if(tenkaikbn===$.id.value_tenkaikbn_jr){
							$(this).numberbox('textbox').validatebox('options').validType = 'intMaxLen[4]';
							$.setInputboxValue($(this),that.jskptn);
						} else {
							$(this).numberbox('textbox').validatebox('options').validType = 'intMaxLen[3]';
							$.setInputboxValue($(this),'');
						}
					}
				});
				$("[col^=N9_]").not("[col=N9_11]").each(function(){
					$.setInputboxValue($(this), '');
				});
				$("[col^=N10_]").not("[col=N10_11]").each(function(){
					$.setInputboxValue($(this), '');
				});

				$('#'+that.focusRootId).find('[col^=N4_]').each(function(){
					that.changeInputboxFunc(that, $(this).attr('id'), $.getInputboxValue($(this)), $(this), true);
				});

				that.calcRe();
			}

			// フォーカスアウトのタイミングの動作
			if (id===$.id.sel_bycd) {
				var obj = $('#'+id);
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

				if (!flg || (flg && val==='-1')) {
					obj.combobox('enableValidation');
					if (flg && val==='-1') {
						obj.combobox('setText', '');
					}
				} else {
					obj.combobox('disableValidation');
				}
			}

			var msgid = null;
			if(that.queried){
				if(that.changeInputInfo.indexOf(id.replace(/[0-9]+$/,""))===-1){
					that.changeInputInfo.push(id.replace(/[0-9]+$/,""));
				}

				// ボタン制御：各種ボタン機能遷移条件に基づきボタンを遷移不能にする
				if(that.changeInputInfo.indexOf($.id_inp.txt_tencd+"_add")!==-1
				 ||that.changeInputInfo.indexOf($.id_inp.txt_tenrank)!==-1
				 ||that.changeInputInfo.indexOf($.id_inp.txt_tencd+"_del")!==-1){
					if(that.judgeRepType.sei_upd ){
						$.setInputBoxDisable($('#'+$.id.btn_sub+"_winTG018_n"));
						$.setInputBoxDisable($('#'+$.id.btn_sub+"_winTG018_h"));
					}
				}
				if(that.changeInputInfo.indexOf($.id_inp.txt_htasu)!==-1){
					if(that.judgeRepType.sei_upd ){
						$.setInputBoxDisable($('#'+$.id.btn_sub+"_winTG018_n"));
					}
				}

				// チェック処理（変更時）
				msgid = that.checkInputboxFunc(id, newValue, false);

				if(msgid !==null){
					var target = $('#'+id);
					if (msgid.split(",").length >= 2) {
						$.showMessage(msgid.split(",")[0], [msgid.split(",")[1]], function(){$.addErrState(that, target, true)});
					} else {
						$.showMessage(msgid, undefined, function(){$.addErrState(that, target, true)});
					}
					return false;
				}
			}

			/* 定貫・不定貫選択時の必須項目切り替え */
			if (id===$.id.rad_tkanplukbn) {
				var kbn = $.getInputboxValue($('[name='+$.id.rad_tkanplukbn+']').eq(0));	// that.grd_data['F57']
				if (that.judgeRepType.frm1) {
					kbn = '1';
				}

				var reqobjids = [];	// 必須
				var notreqobjids = [];	// 任意

				// 不定貫選択
				if (kbn==='2') {
					reqobjids = reqobjids.concat([$.id_inp.txt_genkaam_1kg,$.id_inp.txt_a_genkaam_1kg]);
					notreqobjids = notreqobjids.concat([$.id_inp.txt_a_baikaam+1, $.id_inp.txt_irisu+1]);
				// 定貫選択
				} else if (kbn==='1') {
					reqobjids = reqobjids.concat([$.id_inp.txt_a_baikaam+1, $.id_inp.txt_irisu+1]);
					notreqobjids = notreqobjids.concat([$.id_inp.txt_genkaam_1kg,$.id_inp.txt_a_genkaam_1kg]);
				}

				// 必須化
				for (var i = 0; i < reqobjids.length; i++) {
					if (!that.judgeRepType.isModeI) {
						var target = $('#'+reqobjids[i]);
						$.setInputBoxDisableVariable(target,true);
						$.setInputBoxEnableVariable(target,true);
					}
				}

				// 必須解除
				for (var i = 0; i < notreqobjids.length; i++) {
					if (!that.judgeRepType.isModeI) {
						var target = $('#'+notreqobjids[i]);
						$.setInputBoxDisableVariable(target,true);
						$.setInputBoxEnableVariable(target,false);
					}
				}

				var a_baikaam	= $.getInputboxValue($('#'+$.id_inp.txt_a_baikaam+kbn));
				var b_baikaam	= $.getInputboxValue($('#'+$.id_inp.txt_b_baikaam+kbn));
				var c_baikaam	= $.getInputboxValue($('#'+$.id_inp.txt_c_baikaam+kbn));
				var irisu		= $.getInputboxValue($('#'+$.id_inp.txt_irisu+kbn));

				$.setInputboxValue($('#'+$.id_inp.txt_a_baikaam+kbn),'');
				$.setInputboxValue($('#'+$.id_inp.txt_b_baikaam+kbn),'');
				$.setInputboxValue($('#'+$.id_inp.txt_c_baikaam+kbn),'');
				$.setInputboxValue($('#'+$.id_inp.txt_irisu+kbn),'');
				$.setInputboxValue($('#'+$.id_inp.txt_a_baikaam+kbn),a_baikaam);
				$.setInputboxValue($('#'+$.id_inp.txt_b_baikaam+kbn),b_baikaam);
				$.setInputboxValue($('#'+$.id_inp.txt_c_baikaam+kbn),c_baikaam);
				$.setInputboxValue($('#'+$.id_inp.txt_irisu+kbn),irisu);

				if (kbn==='2') {

					$.each($('#spread_reg').children('td').children('[col]'), function(){
						$.setInputboxValue($(this),'');
					});

					$.each($('#spread_mae').children('td').children('[col]'), function(){
						$.setInputboxValue($(this),'');
					});

					$.each($('#spread_ato').children('td').children('[col]'), function(){
						$.setInputboxValue($(this),'');
					});

					$.each($('#spread_sobaika').children('td').children('[col]'), function(){
						$.setInputboxValue($(this),'');
					});

					for ( var i = 73; i <= 80 ; i++ ) {
						if (!(i === 76 && !$.isEmptyVal($.getInputboxValue($("[col=F87]")))) &&
								!(i === 80 && !$.isEmptyVal($.getInputboxValue($("[col=F93]"))))) {
							$.setInputboxValue($("[col=F"+i+"]"),'');
						}
					}
				} else if (kbn==='1') {

					$.each($('#spread_b').children('td').children('[col]'), function(){
						$.setInputboxValue($(this),'');
					});

					$.each($('#like_datagrid_head').children('td').children('[col]'), function(){
						$.setInputboxValue($(this),'');
					});

					$.each($('#spread_c').children('input'), function(){
						$.setInputboxValue($(this),'');
					});

					$.each($('#spread_a_sobaika').children('td').children('[col]'), function(){
						$.setInputboxValue($(this),'');
					});

					$.each($('#spread_b_sobaika').children('td').children('[col]'), function(){
						$.setInputboxValue($(this),'');
					});

					$.each($('#spread_c_sobaika').children('td').children('[col]'), function(){
						$.setInputboxValue($(this),'');
					});

					if ($.isEmptyVal($.getInputboxValue($('#'+$.id_inp.txt_b_baikaam+1)))) {
						$.setInputboxValue($("[col=F76]"),'');
					}

					if ($.isEmptyVal($.getInputboxValue($('#'+$.id_inp.txt_c_baikaam+1)))) {
						$.setInputboxValue($("[col=F80]"),'');
					}

					if (that.oldTkanplukbn==='2') {
						that.changeInputboxFunc(that, $.id_inp.txt_shncd, $.getInputboxValue($('#'+$.id_inp.txt_shncd)), $("#" + $.id_inp.txt_shncd), true);
					}
				}
				// ラジオの変更か否かの判断に使用
				that.oldTkanplukbn = kbn;

				// 更新時入数を非活性にするか判定
				if (that.judgeRepType.sei_upd && !that.judgeRepType.frm4 && !that.judgeRepType.frm5) {
					if(!$.isEmptyVal(that.getColValue("F163"),true) || !$.isEmptyVal(that.baseData[0].F177,true) ||
							!$.isEmptyVal(that.getColValue("F164"),true) ||
							!$.isEmptyVal(that.getColValue("F165"),true)){
						if (kbn==='1') {
							$.setInputboxValue($('#'+$.id_inp.txt_irisu+1),that.baseData[0].F66);
						} else {
							$.setInputboxValue($('#'+$.id_inp.txt_irisu+2),that.baseData[0].F86);
						}
						$.setInputBoxDisableVariable($('#'+$.id_inp.txt_irisu+1));
						if (!that.judgeRepType.frm1) {
							$.setInputBoxDisableVariable($('#'+$.id_inp.txt_irisu+2));
						}
					}
				}
			}

			// 特売・スポットかつ青果以外の場合、B売店、C売店の入力を制御
			if (that.judgeRepType.st && that.judgeRepType.toksp && !that.judgeRepType.frm4) {


				var bcFlg = ""; // 1:b活性 2:b非活性 3:c活性 4:c非活性

				if (that.judgeRepType.frm5) {
					if (id.indexOf($.id_mei.kbn10656+"_b") != -1) {
						if (!$.isEmptyVal(newValue) && newValue !== '-1') {
							bcFlg = '1';
						} else {
							bcFlg = '2';
						}
					}

					if (id.indexOf($.id_mei.kbn10656+"_c") != -1) {
						if (!$.isEmptyVal(newValue) && newValue !== '-1') {
							bcFlg = '3';
						} else {
							bcFlg = '4';
						}
					}
				} else {
					if (id.indexOf($.id_inp.txt_b_baikaam+'1') != -1 || id.indexOf($.id_inp.txt_b_baikaam+'2') != -1) {
						if (!$.isEmptyVal(newValue)) {
							bcFlg = '1';
						} else {
							bcFlg = '2';
						}
					}

					if (id.indexOf($.id_inp.txt_c_baikaam+'1') != -1 || id.indexOf($.id_inp.txt_c_baikaam+'2') != -1) {
						if (!$.isEmptyVal(newValue)) {
							bcFlg = '3';
						} else {
							bcFlg = '4';
						}
					}
				}

				if (bcFlg==='1' && !that.judgeRepType.isModeI) {
					$.setInputBoxEnableVariable($('#'+$.id_inp.txt_rankno_add_b),true);
					$.setInputBoxEnableVariable($('#'+$.id.btn_rankno+"_add_b"));
				} else if (bcFlg==='2') {
					var rankno_add_b = $.getInputboxValue($('#'+$.id_inp.txt_rankno_add_b));
					$.setInputboxValue($('#'+$.id_inp.txt_rankno_add_b),'');
					$.setInputboxValue($('#'+$.id_inp.txt_rankno_add_b),rankno_add_b);
					$.setInputBoxDisableVariable($('#'+$.id_inp.txt_rankno_add_b),true);
					$.setInputBoxDisableVariable($('#'+$.id.btn_rankno+"_add_b"),true);
				} else if (bcFlg==='3' && !that.judgeRepType.isModeI) {
					$.setInputBoxEnableVariable($('#'+$.id_inp.txt_rankno_add_c),true);
					$.setInputBoxEnableVariable($('#'+$.id.btn_rankno+"_add_c"));
				} else if (bcFlg==='4') {
					var rankno_add_c = $.getInputboxValue($('#'+$.id_inp.txt_rankno_add_c));
					$.setInputboxValue($('#'+$.id_inp.txt_rankno_add_c),'');
					$.setInputboxValue($('#'+$.id_inp.txt_rankno_add_c),rankno_add_c);
					$.setInputBoxDisableVariable($('#'+$.id_inp.txt_rankno_add_c),true);
					$.setInputBoxDisableVariable($('#'+$.id.btn_rankno+"_add_c"),true);
				}
			}

			if (id===$.id.chk_htgenbaikaflg) {
				if (that.judgeRepType.frm5 && !$('#'+$.id.chk_htgenbaikaflg).is(":checked")) {
					if (!that.judgeRepType.isModeI) {
						$.setInputBoxEnableVariable($('#'+$.id_inp.txt_nnstdt),true);
						$.setInputBoxEnableVariable($('#'+$.id_inp.txt_nneddt),true);
					}
					// チラシのみかつチェックなしの場合
					if (that.judgeRepType.toktg_t) {
						$('#spread_tjten').find(":input").each(function(){
							if (!that.judgeRepType.isModeI) {
								$.setInputBoxEnableVariable($(this));
							}
						});
					}

					var stdt = $.getInputboxValue($('#'+$.id_inp.txt_hbstdt));
					var eddt = $.getInputboxValue($('#'+$.id_inp.txt_hbeddt));
					if(!$.isEmptyVal(stdt)&&!$.isEmptyVal(eddt)){
						if (!that.judgeRepType.isModeI) {
							$.setInputBoxEnableVariable($("#" + $.id.chk_plusndflg),true);
						}
						$.setInputboxValue($("#" + $.id.chk_plusndflg), $.id.value_off);
					}
				} else if (that.judgeRepType.frm5 && $('#'+$.id.chk_htgenbaikaflg).is(":checked")) {
					$.setInputboxValue($('#'+$.id_inp.txt_nnstdt),'');
					$.setInputboxValue($('#'+$.id_inp.txt_nneddt),'');
					$.setInputBoxDisableVariable($('#'+$.id_inp.txt_nnstdt),true);
					$.setInputBoxDisableVariable($('#'+$.id_inp.txt_nneddt),true);

					// チラシのみかつチェックありの場合
					if (that.judgeRepType.toktg_t) {
						$('#spread_tjten').find(":input").each(function(){
							$.setInputboxValue($(this),'');
							$.setInputBoxDisableVariable($(this),true);
						});
					}
					$.setInputboxValue($("#" + $.id.chk_plusndflg), $.id.value_on);
					$.setInputBoxDisableVariable($("#" + $.id.chk_plusndflg),true);
				}
			}

			// 100g総売価の計算
			if(id===$.id_inp.txt_a_genkaam_1kg){
				var val = $.getInputboxValue($('#'+id));
				if (!$.isEmptyVal(val)) {
					$.setInputboxValue($('#'+$.id_inp.txt_a_baikaam+2),Math.round(val/10.0));
				} else {
					$.setInputboxValue($('#'+$.id_inp.txt_a_baikaam+2),'');
				}
			}

			// 検索、入力後特殊処理
			if(that.queried||all){
				// 1.月締後変更処理	月締後変更処理と固定表示する。
				// 2.週No.	前の画面から。
				// 3.催しコード	前の画面から。
				// 4.催し名称
				// 5.一日遅パタン アンケート無の場合、NULLを表示。
				// 6.一日遅スライドしない-販売
				//  ① 【画面】.「一日遅れパタン有り」でないと、チェック不可能。	check
				//  ② 【画面】.「販売期間From」 = 【画面】.「販売期間To」 AND 【画面】.「販売期間From」 = 当催しの催し開始日の場合のみチェック可能。	check
				//  ③ 新規の場合、初期値は非チェック状態に設置する。	java
				// 7.一日遅スライドしない-納入
				//  ① 【画面】.「一日遅れパタン」有りでないと、チェック不可能。	check
				//  ② 【画面】.「販売期間From」 = 【画面】.「販売期間To」 AND 【画面】.「販売期間From」 = 当催しの催し開始日の場合のみチェック可能。	check
				//  ③ 【画面】.「一日遅スライドしない-販売」がチェックでないとチェック不可。	check
				//  ④ 新規の場合、初期値は非チェック状態に設置する。	java
				// 8.部門	前の画面から。
				// 9.BY
				//  ① 【画面】.「部門」で催し_デフォルト設定テーブルから所属コードを取得する。	java
				//  ② 所属コードでログイン管理テーブルから社員レコードを取得。職員氏名順に連番を振り、"XX社員名漢字"を【画面】.「BY」リストに保存する。（XX：連番）java　
				//  ③ 新規の場合、初期値は空白行に設置する。	java
				//  ④ 更新の場合、全店特売（アンケート有/無）_商品のBYコードは②のりストに存在しなければ、 BYコード（コードと表示名を同じにする）を【画面】.「BY」リストに加え、画面に表示する。
				//  ⑤ 手入力可でロストフォーカス時に連番と一致するリストを表示する。	js
				//  ⑥フォントサイズを9とする	js
				// 10.商品コード
				//  ① フォーカスアウト時のチェック：先頭2桁が【画面】.「部門」項目と一致する。	check
				//  ② フォーカスアウト時のチェック：商品マスタの存在チェック（商品マスタ.更新区分=0 AND 【画面】.「部門」=商品マスタ.商品コードの頭2桁　だけ）。
				//  ③ 新規の場合、初期値はNULLに設置する。	java
				if(id===$.id_inp.txt_shncd){
					var rows = $.getSelectListData(that.name, $.id.action_change, id, that.getInputboxParams(that, id, newValue));
					that.setData(rows);
					that.changeInputboxFunc(that, "F59", that.getColValue("F59", true), $("[col=F59]"), true);
					that.changeInputboxFunc(that, "F173", that.getColValue("F173", true), $("[col=F173]"), true);
				}
				// 11.商品マスタ名称
				// 12.グループNo.
				//  ① 半角。	easyui
				//  ② 新規の場合、初期値はNULLに設置する。	java
				// 13.子No.
				//  ① 入力範囲00～99。	easyui
				//  ② 同一催し内の同じ「グループNo.」中でユニーク。	check
				//  ③ 新規の場合、初期値はNULLに設置する。	java
				// 14.日替 新規の場合、初期値は非チェック状態に設置する。	java
				// 15.販売期間From/16.販売期間To
				//  ① 入力範囲：2003/01/01～9999/12/31。	check
				//  ② 【画面】.「販売期間From」≧当催しの販売開始日 AND 【画面】.「販売期間To」≦当催しの販売終了日。	check
				//  ③ 【画面】.「販売期間From」と【画面】.「販売期間To」の両方入力　OR　両方NULL
				//  ④ 【画面】.「販売期間From」≦【画面】.「販売期間To」
				//  ⑤ 催し区分=0：レギュラーの場合、【画面】.「販売期間」編集不可。
				//  ⑥ 更新の時は編集不可。
				//  ⑦ 新規の場合、初期値はNULLに設置する。
				//  ⑧ 【画面】.「販売期間From」と【画面】.「販売期間To」が全て入力されたら、販売情報タブを編集可能にする。クリアされた場合は販売情報タブを編集不可にし、値をクリアする。編集不可状態の販売情報タブの値はDBへ保存しないが、メーカー名、POP名、規格は編集不可でもDBへ保存する。
				if(id===$.id_inp.txt_hbstdt||id===$.id_inp.txt_hbeddt){
					var stdt = id===$.id_inp.txt_hbstdt ? $.getParserDt(newValue): $.getInputboxValue($('#'+$.id_inp.txt_hbstdt));
					var eddt = id===$.id_inp.txt_hbeddt ? $.getParserDt(newValue): $.getInputboxValue($('#'+$.id_inp.txt_hbeddt));
					if(!$.isEmptyVal(stdt)&&!$.isEmptyVal(eddt)){
						$.each(that.editableInputIdsTabHB, function(){
							var option = "";
							var val = "";
							var len = 0;

							if (!$.isEmptyVal($("#" + this)[0].dataset) && !$.isEmptyVal($("#" + this)[0].dataset.options)) {
								len = $("#" + this)[0].dataset.options.split(",").length
							}

							for (var i = 0; i < len; i++) {

								if ($("#" + this)[0].dataset.options.split(",")[i].split(":").length !== 2) {
									continue;
								}

								option = $("#" + this)[0].dataset.options.split(",")[i].split(":")[0];
								if (option === 'required') {
									val = $("#" + this)[0].dataset.options.split(",")[i].split(":")[1];
								}
							}
							if (!that.judgeRepType.isModeI) {
								$.setInputBoxEnableVariable($("#" + this),val);
							}
						});
						if (!(!that.judgeRepType.frm5 && that.judgeRepType.toktg_t) && that.queried) {
							if (!that.judgeRepType.frm5 || (that.judgeRepType.frm5 && !that.judgeRepType.toktg_t)
									|| (that.judgeRepType.frm5 && that.judgeRepType.toktg_t && !$('#'+$.id.chk_htgenbaikaflg).is(":checked"))) {
								if (!that.judgeRepType.isModeI) {
									$.setInputBoxEnableVariable($("#" + $.id.chk_plusndflg),true);
								}
								$.setInputboxValue($("#" + $.id.chk_plusndflg), $.id.value_off);
							}
						}
					}else{
						$.each(that.editableInputIdsTabHB, function(){
							if ($.isEmptyVal(stdt) && $.isEmptyVal(eddt)) {
								if (!$.isEmptyVal($.getInputboxValue($('#'+$.id_inp.txt_shncd)))){
									if (["F104","F105","F106"].indexOf($("#" + this).attr("col"))==-1 &&
											["F104","F105","F106"].indexOf($("#" + this).parent().prev().attr("col"))==-1)
									{
										$.setInputboxValue($("#" + this), "");
									}
								} else {
									$.setInputboxValue($("#" + this), "");
								}
							}
							$.setInputBoxDisableVariable($("#" + this),true);
						});

						if (!(!that.judgeRepType.frm5 && that.judgeRepType.toktg_t)) {
							if (!that.judgeRepType.frm5 || (that.judgeRepType.frm5 && !that.judgeRepType.toktg_t)) {
								$.setInputboxValue($("#" + $.id.chk_plusndflg), $.id.value_on);
								$.setInputBoxDisableVariable($("#" + $.id.chk_plusndflg),true);
							}
						}
					}
				}
				// 18.納入期間From/19.納入期間To
				//  ① 入力範囲：2003/01/01～9999/12/31。
				//  ② 【画面】.「納入期間From」≧当催しの納入開始日 AND 【画面】.「納入期間To」≦当催しの納入終了日。
				//  ③ 【画面】.「納入期間From」と【画面】.「納入期間To」の両方入力　OR　両方NULL。
				//  ④ 【画面】.「納入期間From」≦【画面】.「納入期間To」
				//  ⑤ 更新の時は編集不可。
				//  ⑥ 新規の場合、初期値はNULLに設置する。
				//  ⑦ 【画面】.「納入期間From」と【画面】.「納入期間To」が全て入力されたら、納入情報タブを編集可能にする。また、入力された納入期間に応じて納入情報タブの納入対象にチェックをつける。クリアされた場合は納入情報タブを編集不可にし、値をクリアする。
				if(id===$.id_inp.txt_nnstdt||id===$.id_inp.txt_nneddt || id===$.id_inp.txt_hbstdt || id===$.id_inp.txt_hbeddt){
					var stdt = id===$.id_inp.txt_nnstdt ? $.getParserDt(newValue): $.getInputboxValue($('#'+$.id_inp.txt_nnstdt));
					var eddt = id===$.id_inp.txt_nneddt ? $.getParserDt(newValue): $.getInputboxValue($('#'+$.id_inp.txt_nneddt));
					if(!$.isEmptyVal(stdt)&&!$.isEmptyVal(eddt)){
						var tenkaikbn = $("input[col=F154]").val();		// F46	TENKAIKBN	展開方法
						$.each(that.editableInputIdsTabNN, function(){
							var option = "";
							var val = "";
							var len = 0;

							if (!$.isEmptyVal($("#" + this)[0].dataset) && !$.isEmptyVal($("#" + this)[0].dataset.options)) {
								len = $("#" + this)[0].dataset.options.split(",").length
							}

							for (var i = 0; i < len; i++) {

								if ($("#" + this)[0].dataset.options.split(",")[i].split(":").length !== 2) {
									continue;
								}

								option = $("#" + this)[0].dataset.options.split(",")[i].split(":")[0];
								if (option === 'required') {
									val = $("#" + this)[0].dataset.options.split(",")[i].split(":")[1];
								}
							}

							var getId = $("#" + this).attr('id');
							if (getId===$.id_inp.txt_binkbn) {
								if ($.isEmptyVal($.getInputboxValue($('#'+getId)))){
									$.setInputboxValue($("#" + this), "1");
								}
								if (!that.judgeRepType.isModeI) {
									$.setInputBoxEnableVariable($("#" + this),val);
								}
							} else if ((getId===$.id_inp.txt_bdenkbn || getId===$.id_inp.txt_wappnkbn) && $.isEmptyVal($.getInputboxValue($('#'+getId)))) {
								if ($.isEmptyVal($.getInputboxValue($('#'+getId)))){
									$.setInputboxValue($("#" + this), "0");
								}
								if (!that.judgeRepType.isModeI) {
									$.setInputBoxEnableVariable($("#" + this),val);
								}
							} else if (getId===$.id.chk_shudenflg && that.getColValue("F173")==="1") {
								$.setInputboxValue($("#" + this), $.id.value_on);
							} else if (getId.indexOf($.id_inp.txt_ptnno)!==-1) {
								var chkId = $.id.chk_nndt+getId.replace($.id_inp.txt_ptnno,'');
								if ($.getInputboxValue($('#'+chkId))===$.id.value_on) {
									$.setInputBoxRequired($("#" + this));
								}
								if (!that.judgeRepType.isModeI) {
									$.setInputBoxEnableVariable($("#" + this),val);
								}
								if (tenkaikbn===$.id.value_tenkaikbn_jr) {
									$("#" + this).numberbox('textbox').validatebox('options').validType = 'intMaxLen[4]';
									if (!$.isEmptyVal(that.jskptn)) {
										$.setInputboxValue($("#" + this), that.jskptn);
									} else {
										$.setInputboxValue($("#" + this),$.getInputboxValue($("#" + this)));
									}
								} else {
									$("#" + this).numberbox('textbox').validatebox('options').validType = 'intMaxLen[3]';
									$.setInputboxValue($("#" + this),$.getInputboxValue($("#" + this)));
								}
							} else if (!that.judgeRepType.isModeI) {
								$.setInputBoxEnableVariable($("#" + this),val);
							}
						});

						// クックサン部門特殊処理
						var txt_bmncd = $.getJSONValue(this.jsonHidden, $.id_inp.txt_bmncd);
						if (that.getColValue("F173")==="1" && (txt_bmncd==='20' || txt_bmncd==='23')) {
							if (!that.judgeRepType.isModeI) {
								$.setInputBoxEnableVariable($("#" + $.id_inp.txt_binkbn));
							}
						} else if (that.judgeRepType.frm1) {
							if (that.getColValue("F173")!=="1") {
								$.setInputboxValue($("#" + $.id_inp.txt_binkbn), "1");
							}
							$.setInputBoxDisableVariable($("#" + $.id_inp.txt_binkbn),true);
						}

						for (var i = 1; i <= 10; i++){
							var nndt = that.getColValue("N90_"+i);
							var hbstdt = id===$.id_inp.txt_hbstdt ? $.getParserDt(newValue): $.getInputboxValue($('#'+$.id_inp.txt_hbstdt));
							var hbeddt = id===$.id_inp.txt_hbeddt ? $.getParserDt(newValue): $.getInputboxValue($('#'+$.id_inp.txt_hbeddt));
							if(hbstdt <= nndt && nndt <= hbeddt){
								$.setInputboxValue($('[col=N3_'+i+']'), $.id.text_on);
							} else {
								$.setInputboxValue($('[col=N3_'+i+']'), $.id.text_off);
							}

							if ($('#'+$.id_inp.txt_nnstdt).next().find('[disabled=disabled]').length===0) {
								var nnstdt = id===$.id_inp.txt_nnstdt ? $.getParserDt(newValue): $.getInputboxValue($('#'+$.id_inp.txt_nnstdt));
								var nneddt = id===$.id_inp.txt_nneddt ? $.getParserDt(newValue): $.getInputboxValue($('#'+$.id_inp.txt_nneddt));

								if(nnstdt <= nndt && nndt <= nneddt){
									$.setInputboxValue($('[col=N4_'+i+']'), $.id.value_on);
								} else {
									$.setInputboxValue($('[col=N4_'+i+']'), $.id.value_off);
								}
							}
						}

						if(tenkaikbn===$.id.value_tenkaikbn_ts){
							val = '通常数pt';
							$('#'+that.focusRootId).find('[col^=N5_]').each(function(){
								$.setInputBoxDisableVariable($(this), true);
							});
						}
					}else{
						$.each(that.editableInputIdsTabNN, function(){
							$.setInputboxValue($("#" + this), "");
							$.setInputBoxDisableVariable($("#" + this),true);
						});
					}
				}

				if(id===$.id_inp.txt_nnstdt||id===$.id_inp.txt_nneddt || id==="Out_ReportwinST020" || id===$.id_inp.txt_hbstdt || id===$.id_inp.txt_hbeddt){

					var nnstdt = id===$.id_inp.txt_nnstdt ? $.getParserDt(newValue): $.getInputboxValue($('#'+$.id_inp.txt_nnstdt));
					var nneddt = id===$.id_inp.txt_nneddt ? $.getParserDt(newValue): $.getInputboxValue($('#'+$.id_inp.txt_nneddt));

					if(!$.isEmptyVal(nnstdt)&&!$.isEmptyVal(nneddt)){

						var moysnnstdt = that.nndtData[0]["N90"];
						var tenkaikbn = $("input[col=F154]").val();	// F46	TENKAIKBN	展開方法

						for (var i = 1; i <= 10; i++) {

							if (!$.isEmptyVal(moysnnstdt) && i !== 1) {
								var dObj = $.convDate(moysnnstdt);
								dObj.setDate(dObj.getDate() + 1);
								moysnnstdt = $.dateFormat(dObj, 'yyyymmdd');
							}

							if (!(moysnnstdt*1 >= nnstdt*1 && moysnnstdt*1 <= nneddt*1)) {
								$("#"+$.id.rad_sel+i).attr('disabled', 'disabled');
								$("#"+$.id.rad_sel+i).attr('readonly', 'readonly');
								$.setInputboxValue($("[col=N4_"+i+"]"), "");
								$.setInputBoxDisableVariable($("[col=N4_"+i+"]"),true);
								$.setInputboxValue($("[col=N5_"+i+"]"), "");
								$.setInputBoxDisableVariable($("[col=N5_"+i+"]"),true);
								$.setInputboxValue($("[col=N7_"+i+"]"), "");
								$.setInputBoxDisableVariable($("[col=N7_"+i+"]"),true);
								$.setInputboxValue($("[col=N8_"+i+"]"), "");
								$.setInputBoxDisableVariable($("[col=N8_"+i+"]"),true);
							} else if (!that.judgeRepType.sei_new) {
								if(!$.isEmptyVal(that.getColValue("F163"),true) || !$.isEmptyVal(that.baseData[0].F177,true) ||
										!$.isEmptyVal(that.getColValue("F164"),true) ||
										!$.isEmptyVal(that.getColValue("F165"),true)){
									if ($.getInputboxValue($("[col=N4_"+i+"]"))===$.id.value_off) {
										$.setInputBoxDisableVariable($("[col=N8_"+i+"]"),true);
									}
									$.setInputBoxDisableVariable($("[col=N4_"+i+"]"),true);
									$.setInputBoxDisableVariable($("[col=N5_"+i+"]"),true);
									$.setInputBoxDisableVariable($("[col=N7_"+i+"]"),true);
								}
							}
						}

						if(!$.isEmptyVal(that.getColValue("F163"),true) || !$.isEmptyVal(that.baseData[0].F177,true) ||
								!$.isEmptyVal(that.getColValue("F164"),true) ||
								!$.isEmptyVal(that.getColValue("F165"),true)){
							$.setInputBoxDisable($('#'+$.id.btn_tenkai));
							$.setInputBoxDisable($('#'+$.id.btn_sub+"_winST012"));
						}
					}
				}

				if (id.indexOf($.id.chk_nndt) !== -1) {

					var chk = $.getInputboxValue($('#'+id));
					var idx = id.replace(/[^0-9]/g, "");
					var kbn = $.getInputboxValue($("[col=N8_"+idx+"]"));

					if(!$.isEmptyVal(chk, true) && $.isEmptyVal(kbn)) {
						$.setInputBoxRequired($("[col=N7_"+idx+"]"));
						if ($.isEmptyVal($.getInputboxValue($("[col=N7_"+idx+"]")))) {
							$.setInputboxValue($("[col=N7_"+idx+"]"),"");
						}
						$.setInputboxValue($("[col=N8_"+idx+"]"), '0');
					} else if ($.isEmptyVal(chk, true) && !$.isEmptyVal(chk)) {
						$.setInputBoxDisableVariable($("[col=N7_"+idx+"]"),true);
						if (!that.judgeRepType.isModeI) {
							$.setInputBoxEnableVariable($("[col=N7_"+idx+"]"));
						}
						$.setInputboxValue($("[col=N8_"+idx+"]"), '');
					}
				}

				// 20.チラシ未掲載
				//  ① 商品マスタ.商品種別=1（原材料）の場合、チェックしないとエラー。
				//  ② アンケート無の場合、デフォルトでチェックを付け、編集不可。
				//  ③ 新規の場合、上記の①と②以外のデフォルトはチェックしない状態にする。
				// 21.対象店
				//  ① 入力範囲：001～999。
				//  ② 更新の時は編集不可。
				//  ③ 新規の場合、初期値はNULLに設置する。
				// 22.除外店
				//  ① 入力範囲：001～999。
				//  ② 更新の時は編集不可。
				//  ③ 新規の場合、初期値はNULLに設置する。
				//  ④ アンケート有の場合、編集不可。
				// 23.追加（1～10）/ 33.ランク（1～10）
				//  ① 全店特売（アンケート有/無）_対象除外店.対象除外フラグ=1：対象で保存する。
				//  ② 入力範囲：店番号001～400、ランクA～Z。
				//  ③ 【画面】.「追加」に店番を入力したら、当列の【画面】.「ランク」は必須とする。
				//  ④ 新規の場合、初期値はNULLに設置する。
				// 43.除外（1～10）
				//  ① 全店特売（アンケート有/無）_対象除外店.対象除外フラグ=2：除外で保存する。
				//  ② 入力範囲：001～400。
				//  ③ 新規の場合、初期値はNULLに設置する。

				// *** 特売事前行 ***
				// 42.原価
				//  ① 入力範囲：1～999,999.99。
				//  ② 『特売共通仕様書 原価・売価・入数のデフォルト表示』部分参照。
				//  ③ 通常は【画面】.特売追加行の「原価」 = 【画面】.特売事前行の「原価」でないとエラー。
				//  ④ 但し、ドライの場合は【画面】.特売追加行の「原価」 >= 【画面】.特売事前行の「原価」を許す。また、ドライ以外で【画面】.納入情報タブの「週次伝送flg」 = 1 and 【画面】.納入情報タブの「PC区分」 = 0なら、【画面】.特売追加行の「原価」 >= 【画面】.特売事前行の「原価」を許す。
				// 43.A総売価
				//  ① 入力範囲：1～999,999。
				//  ② 【画面】.レギュラー行の「A総売価」>=【画面】.特売事前行の「A総売価」。但し、【画面】.レギュラー行の「A総売価」=0orNULLの場合と催し区分=0の場合、あるいは精肉、鮮魚の場合は本チェックを行わない。
				//  ③ 【画面】.特売事前行の「A総売価」>=【画面】.B部分の「B総売価」>=【画面】.C部分の「C総売価」。
				//  ④ 【画面】.特売事前行の「A総売価」=【画面】.B部分の「B総売価」=【画面】.C部分の「C総売価」は不可。
				//  ⑤ 『特売共通仕様書 原価・売価・入数のデフォルト表示』部分参照。
				// 44.本体売価	【画面】.特売事前行の「A総売価」より税抜き計算。『特売共通仕様書 本体売価算出方法』部分参照。
				// 45.入数
				//  ① 入力範囲：1～999。
				//  ② 『特売共通仕様書 原価・売価・入数のデフォルト表示』部分参照。
				// 46.値入
				//  ① （【画面】.特売事前行の「本体売価」－【画面】.特売事前行の「原価」）／【画面】.特売事前行の「本体売価」*100。
				//  ② 小数点以下3位切り捨て, 第2位まで求める。
				if(id===$.id_inp.txt_genkaam_mae){
					// 特売事前_A総売価取得後、本体売価、値入算出
					var baika = $.getInputboxValue($('[col=F65]'));
					var genka = newValue;
					$.setInputboxValue($('[col=F67]'), that.calcNeireRit(baika, genka));
				}
				if(id===$.id_inp.txt_a_baikaam+"1"){
					var baika = $.getInputboxData(that.name, $.id.action_change, $.id.txt_tok_honbaika, that.getInputboxParams(that, $.id.txt_tok_honbaika, newValue));
					var genka = $.getInputboxValue($('#'+$.id_inp.txt_genkaam_mae));
					var genka2= $.getInputboxValue($('#'+$.id_inp.txt_genkaam_ato));
					$.setInputboxValue($('[col=F65]'), $.getFormat(baika, '#,##0'));
					$.setInputboxValue($('[col=F67]'), that.calcNeireRit(baika, genka));
					$.setInputboxValue($('[col=F72]'), that.calcNeireRit(baika, genka2));
				}

				// *** 予定数 ***
				// ① 計算式の要素（予定数、特売事前原価、P原価、特売事前本体売価、P総売価）が入力､変更されたタイミングで計算し表示する。
				// ② 必要要素がそろっていない場合は、NULLを表示。
				// ③ 100の位を切り捨て千円単位で表示
				// 29.予定数
				//  ① 入力範囲：0～999,999。
				//  ② 新規の場合、初期値はNULLに設置する。
				// 30.仕入額
				//  ① 全特（ア有/無）_商品.定貫PLU不定貫区分=1:定貫・PLUの場合、【画面】.「予定数」×【画面】.特売事前行の「原価」×【画面】.「特売事前入数」。
				//  ② 全特（ア有/無）_商品.定貫PLU不定貫区分=2:不定貫の場合、【画面】.「予定数」×【画面】.A総売価行「P原価」×【画面】.「A総売価行入数」。
				//  ③ 結果は小数切り捨てで表示する。
				// 31.販売額
				//  ① 全特（ア有/無）_商品.定貫PLU不定貫区分=1:定貫・PLUの場合、【画面】.「予定数」×【画面】.特売事前行の「本体売価」×【画面】.「特売事前入数」。
				//  ② 全特（ア有/無）_商品.定貫PLU不定貫区分=2:不定貫の場合、【画面】.「予定数」×（【画面】.A総売価行の「P総売価」-税額）×【画面】.「A総売価行入数」。税額の計算は、『特売共通仕様書 総額売価計算方法』部分参照。
				// 32.荒利額	【画面】.「販売額」-【画面】.「仕入額」。
				if(id===$.id.rad_tkanplukbn+1||id===$.id.rad_tkanplukbn+2		// 定貫PLU不定貫区分
				 ||id===$.id_inp.txt_hbyoteisu			//【画面】.「予定数」
				 ||id===$.id_inp.txt_genkaam_mae		//【画面】.特売事前行の「原価」
				 ||id===$.id_inp.txt_a_baikaam+"1"		//【画面】.特売事前行の「本体売価」算出
				 ||id===$.id_inp.txt_irisu+"1"			//【画面】.「特売事前入数」
				 ||id===$.id_inp.txt_genkaam_pack		//【画面】.A総売価行「P原価」
				 ||id===$.id_inp.txt_a_baikaam+"2"		//【画面】.「A総売価行入数」
				 ||id===$.id_inp.txt_a_baikaam_pack		//【画面】.A総売価行の「P総売価」
				){
					var sir = null,han = null,ara = null;
					var kbn = $.getInputboxValue($('[name='+$.id.rad_tkanplukbn+']').eq(0));	// that.grd_data['F57']
					if (that.judgeRepType.frm1) {
						kbn = '1';
					}
					var txt_hbyoteisu = $.getInputboxValue($('#'+$.id_inp.txt_hbyoteisu));
					if(kbn==='1'){
						var txt_genkaam_mae = $.getInputboxValue($('#'+$.id_inp.txt_genkaam_mae));
						var txt_irisu1 = $.getInputboxValue($('#'+$.id_inp.txt_irisu+1));
						var txt_F65 = $.getInputboxValue($('[col=F65]'));
						txt_F65 = txt_F65.replace(',','');

						// 仕入額:① 全特（ア有/無）_商品.定貫PLU不定貫区分=1:定貫・PLUの場合、【画面】.「予定数」×【画面】.特売事前行の「原価」×【画面】.「特売事前入数」。
						if(!$.isEmptyVal(txt_hbyoteisu)&&!$.isEmptyVal(txt_genkaam_mae)&&!$.isEmptyVal(txt_irisu1)){
							sir = Math.floor((txt_hbyoteisu*txt_genkaam_mae*txt_irisu1)/1000);
						}
						// 販売額:① 全特（ア有/無）_商品.定貫PLU不定貫区分=1:定貫・PLUの場合、【画面】.「予定数」×【画面】.特売事前行の「本体売価」×【画面】.「特売事前入数」。
						if(!$.isEmptyVal(txt_hbyoteisu)&&!$.isEmptyVal(txt_F65)&&!$.isEmptyVal(txt_irisu1)){
							han = Math.floor((txt_hbyoteisu*txt_F65*txt_irisu1)/1000);
						}

					}else if(kbn==='2'){
						var txt_genkaam_pack = $.getInputboxValue($('#'+$.id_inp.txt_genkaam_pack));
						var txt_irisu2 = $.getInputboxValue($('#'+$.id_inp.txt_irisu+2));
						var txt_a_baikaam_pack = $.getInputboxValue($('#'+$.id_inp.txt_a_baikaam_pack));

						// 仕入額:② 全特（ア有/無）_商品.定貫PLU不定貫区分=2:不定貫の場合、【画面】.「予定数」×【画面】.A総売価行「P原価」×【画面】.「A総売価行入数」。
						if(!$.isEmptyVal(txt_hbyoteisu)&&!$.isEmptyVal(txt_genkaam_pack)&&!$.isEmptyVal(txt_irisu2)){
							sir = Math.floor((txt_hbyoteisu*txt_genkaam_pack*txt_irisu2)/1000);
						}
						// 販売額:② 全特（ア有/無）_商品.定貫PLU不定貫区分=2:不定貫の場合、【画面】.「予定数」×（【画面】.A総売価行の「P総売価」-税額）×【画面】.「A総売価行入数」。税額の計算は、『特売共通仕様書 総額売価計算方法』部分参照。
						if(!$.isEmptyVal(txt_hbyoteisu)&&!$.isEmptyVal(txt_a_baikaam_pack)&&!$.isEmptyVal(txt_irisu2)){
							// 【画面】.A総売価行の「P総売価」-税額
							var baika = $.getInputboxData(that.name, $.id.action_change, $.id.txt_tok_honbaika, that.getInputboxParams(that, $.id.txt_tok_honbaika, txt_a_baikaam_pack));
							han = Math.floor((txt_hbyoteisu*baika*txt_irisu2)/1000);
						}
					}
					// 荒利額：【画面】.「販売額」-【画面】.「仕入額」。
					if(!$.isEmptyVal(sir)&&!$.isEmptyVal(han)){
						ara = han - sir;
					}
					$.setInputboxValue($('[col=F54]'), $.getFormat(sir, '#,##0'));
					$.setInputboxValue($('[col=F55]'), $.getFormat(han, '#,##0'));
					$.setInputboxValue($('[col=F56]'), $.getFormat(ara, '#,##0'));
				}

				// *** レギュラー行 ***
				// ① 【画面】.「商品コード」を入力したタイミングで表示。
				// ② 【画面】.「商品コード」をクリアしたタイミングでクリア。
				// 36.原価
				// 37.A総売価	商品マスタ.レギュラー情報_売価より税込み計算。『特売共通仕様書 総額売価計算方法』部分参照。
				// 38.本体売価	【画面】.レギュラー行の「A総売価」より税抜き計算。『特売共通仕様書 本体売価算出方法』部分参照。
				// 39.入数
				// 40.値入
				//  ① （【画面】.レギュラー行の「本体売価」－商品マスタ.レギュラー情報_原価）／【画面】.レギュラー行の「本体売価」*100。
				//  ② 小数点以下3位切り捨て, 第2位まで求める。
				// レギュラー_A総売価取得後、本体売価、値入算出
				if(id==="F59"){
					var baika = $.getInputboxData(that.name, $.id.action_change, $.id.txt_tok_honbaika, that.getInputboxParams(that, $.id.txt_tok_honbaika, newValue));
					var genka = $.getInputboxValue($('[col=F58]'));
					$.setInputboxValue($('[col=F60]'), $.getFormat(baika, '#,##0'));
					$.setInputboxValue($('[col=F62]'), that.calcNeireRit(baika, genka));
				}

				if(id==="F173"){
					var stdt = id===$.id_inp.txt_nnstdt ? $.getParserDt(newValue): $.getInputboxValue($('#'+$.id_inp.txt_nnstdt));
					var eddt = id===$.id_inp.txt_nneddt ? $.getParserDt(newValue): $.getInputboxValue($('#'+$.id_inp.txt_nneddt));
					if(!$.isEmptyVal(stdt)&&!$.isEmptyVal(eddt)){
						if (newValue==="1") {
							$.setInputboxValue($("#" + $.id.chk_shudenflg), $.id.value_on);
							$.setInputBoxDisableVariable($("#" + $.id.chk_shudenflg),true);
						} else {
							if (!that.judgeRepType.isModeI) {
								$.setInputBoxEnableVariable($("#" + $.id.chk_shudenflg),true);
							}
						}

						// クックサン部門特殊処理
						var txt_bmncd = $.getJSONValue(this.jsonHidden, $.id_inp.txt_bmncd);
						if (newValue==="1" && (txt_bmncd==='20' || txt_bmncd==='23')) {
							if (!that.judgeRepType.isModeI) {
								$.setInputBoxEnableVariable($("#" + $.id_inp.txt_binkbn));
							}
						} else if (that.judgeRepType.frm1) {
							if (newValue!=="1") {
								$.setInputboxValue($("#" + $.id_inp.txt_binkbn), "1");
							}
							$.setInputBoxDisableVariable($("#" + $.id_inp.txt_binkbn),true);
						}
					}
				}

				// *** 特売追加行 ***
				// 48.原価
				//  ① 入力範囲：1～999,999.99。
				//  ② 『特売共通仕様書 原価・売価・入数のデフォルト表示』部分参照。
				// 49.A総売価	表示・入力不可
				// 50.本体売価	表示・入力不可
				// 51.入数	表示・入力不可
				// 52.値入
				//  ① （【画面】.特売事前行の「本体売価」－【画面】.特売追加行の「原価」）／【画面】.特売事前行の「本体売価」*100。
				//  ② 小数点以下3位切り捨て, 第2位まで求める。
				// 特売追加_A総売価取得後、本体売価、値入算出
				if(id===$.id_inp.txt_genkaam_ato){
					var baika = $.getInputboxValue($('[col=F65]'));
					var genka = newValue;
					$.setInputboxValue($('[col=F72]'), that.calcNeireRit(baika, genka));
				}

				// *** B部分 ***
				// 54.B総売価
				//  ① 入力範囲：1～999,999。
				//  ② 新規の場合、初期値はNULLに設置する。
				//  ③ 【画面】.特売事前行の「A総売価」>=【画面】.B部分の「B総売価」>=【画面】.C部分の「C総売価」
				//  ④ 【画面】.特売事前行の「A総売価」=【画面】.B部分の「B総売価」=【画面】.C部分の「C総売価」は不可
				// 55.本体売価	【画面】.B部分の「B総売価」より税抜き計算。『特売共通仕様書 本体売価算出方法』部分参照。
				// 56.値入
				//  ① （【画面】.B部分の「本体売価」－【画面】.特売事前行の「原価」）／【画面】.B部分の「本体売価」*100。
				//  ② 小数点以下3位切り捨て, 第2位まで求める。
				if(id===$.id_inp.txt_b_baikaam+"1"){
					var baika = $.getInputboxData(that.name, $.id.action_change, $.id.txt_tok_honbaika, that.getInputboxParams(that, $.id.txt_tok_honbaika, newValue));
					var genka = $.getInputboxValue($('#'+$.id_inp.txt_genkaam_mae));
					$.setInputboxValue($('[col=F74]'), $.getFormat(baika, '#,##0'));
					$.setInputboxValue($('[col=F75]'), that.calcNeireRit(baika, genka));
				}
				// 57.B売店
				//  ① 入力範囲：001～999。
				//  ② 新規の場合、初期値はNULLに設置する。
				//  ③ アンケート有の場合、編集不可。

				// *** C部分 ***
				// 60.C総売価
				//  ① 入力範囲：1～999,999。
				//  ② 新規の場合、初期値はNULLに設置する。
				//  ③ 【画面】.特売事前行の「A総売価」>=【画面】.B部分の「B総売価」>=【画面】.C部分の「C総売価」
				//  ④ 【画面】.特売事前行の「A総売価」=【画面】.B部分の「B総売価」=【画面】.C部分の「C総売価」は不可
				//  ⑤ 【画面】.B部分の[B総売価]が入力されいている場合のみ入力可
				// 61.本体売価	【画面】.C部分の「C総売価」より税抜き計算。『特売共通仕様書 本体売価算出方法』部分参照。
				// 62.値入
				//  ① （【画面】.C部分の「本体売価」－【画面】.特売事前行の「原価」）／【画面】.C部分の「本体売価」*100。
				//  ② 小数点以下3位切り捨て, 第2位まで求める。
				if(id===$.id_inp.txt_c_baikaam+"1"){
					var baika = $.getInputboxData(that.name, $.id.action_change, $.id.txt_tok_honbaika, that.getInputboxParams(that, $.id.txt_tok_honbaika, newValue));
					var genka = $.getInputboxValue($('#'+$.id_inp.txt_genkaam_mae));
					$.setInputboxValue($('[col=F78]'), $.getFormat(baika, '#,##0'));
					$.setInputboxValue($('[col=F79]'), that.calcNeireRit(baika, genka));
				}
				// 63.C売店
				//  ① 入力範囲：001～999。
				//  ② 新規の場合、初期値はNULLに設置する。
				//  ③ アンケート有の場合、編集不可。

				// 65.発注原売価適用しない
				// 66.PLU商品・定貫商品　／　不定貫商品
				// PLU商品・定貫商品をクリック時：
				//  ① レギュラー行、特売事前行、特売追加行部分の項目をデフォルト表示する。
				//  ② 不定貫部分のA総売価、B総売価、C総売価部分の項目をクリアする。
				// 不定貫商品をクリック時：
				//  ① レギュラー行、特売事前行、特売追加行部分の項目をクリアする。
				//  ② デフォルト表示なし。

				// *** A総売価行 ***
				// 98.100g総売価
				//  ① 入力範囲：1～999,999。
				//  ② 新規の場合、初期値はNULLに設置する。
				//  ③ 【画面】.A総売価行の「100g総売価」>=【画面】.B総売価行の「100g総売価」>=【画面】.C総売価行の「100g総売価」。
				//  ④ 【画面】.A総売価行の「100g総売価」=【画面】.B総売価行の「100g総売価」=【画面】.C総売価行の「100g総売価」は不可。
				// 99.1Kg原価
				//  ① 入力範囲：1～999,999.99。
				//  ② 新規の場合、初期値はNULLに設置する。
				// 100.1Kg総売価
				//  ① 入力範囲：1～999,999.99。
				//  ② 新規の場合、初期値はNULLに設置する。
				//  ③ 【画面】.A総売価行の「1kg総売価」>=【画面】.B総売価行の「1kg総売価」>=【画面】.C総売価行の「1kg総売価」。
				//  ④ 【画面】.A総売価行の「1kg総売価」=【画面】.B総売価行の「1kg総売価」=【画面】.C総売価行の「1kg総売価」は不可。
				// 101.P原価
				//  ① 入力範囲：1～999,999.99。
				//  ② 新規の場合、初期値はNULLに設置する。
				// 102.P総売価
				//  ① 入力範囲：1～999,999。
				//  ② 新規の場合、初期値はNULLに設置する。
				//  ③ 【画面】.A総売価行の「P総売価」>=【画面】.B総売価行の「P総売価」>=【画面】.C総売価行の「P総売価」。
				//  ④ 【画面】.A総売価行の「P総売価」=【画面】.B総売価行の「P総売価」=【画面】.C総売価行の「P総売価」は不可。
				// 103.入数
				//  ① 入力範囲：1～999。
				//  ② 新規の場合、初期値はNULLに設置する。


				// *** B総売価行 ***
				// 105.100g総売価
				// ① 入力範囲：1～999,999。
				// ② 新規の場合、初期値はNULLに設置する。
				// ③ 【画面】.A総売価行の「100g総売価」>=【画面】.B総売価行の「100g総売価」>=【画面】.C総売価行の「100g総売価」。
				// ④ 【画面】.A総売価行の「100g総売価」=【画面】.B総売価行の「100g総売価」=【画面】.C総売価行の「100g総売価」は不可。
				// 106.1Kg原価	表示・入力不可
				// 107.1Kg総売価
				//  ① 入力範囲：1～999,999.99。
				//  ② 新規の場合、初期値はNULLに設置する。
				//  ③ 【画面】.A総売価行の「1kg総売価」>=【画面】.B総売価行の「1kg総売価」>=【画面】.C総売価行の「1kg総売価」。
				//  ④ 【画面】.A総売価行の「1kg総売価」=【画面】.B総売価行の「1kg総売価」=【画面】.C総売価行の「1kg総売価」は不可。
				// 108.P原価	表示・入力不可
				// 109.P総売価
				//  ① 入力範囲：1～999,999。
				//  ② 新規の場合、初期値はNULLに設置する。
				//  ③ 【画面】.A総売価行の「P総売価」>=【画面】.B総売価行の「P総売価」>=【画面】.C総売価行の「P総売価」。
				//  ④ 【画面】.A総売価行の「P総売価」=【画面】.B総売価行の「P総売価」=【画面】.C総売価行の「P総売価」は不可。
				// 110.入数	表示・入力不可

				// *** C総売価行 ***
				// 112.100g総売価
				//  ① 入力範囲：1～999,999。
				//  ② 新規の場合、初期値はNULLに設置する。
				//  ③ 【画面】.A総売価行の「100g総売価」>=【画面】.B総売価行の「100g総売価」>=【画面】.C総売価行の「100g総売価」。
				//  ④ 【画面】.A総売価行の「100g総売価」=【画面】.B総売価行の「100g総売価」=【画面】.C総売価行の「100g総売価」は不可。
				//  ⑤ 【画面】.B総売価行の「100g総売価」を入力しないと【画面】.C総売価行の「100g総売価」を入力できない。
				// 113.1Kg原価	表示・入力不可
				// 114.1Kg総売価
				// ① 入力範囲：1～999,999.99。
				// ② 新規の場合、初期値はNULLに設置する。
				// ③ 【画面】.A総売価行の「1kg総売価」>=【画面】.B総売価行の「1kg総売価」>=【画面】.C総売価行の「1kg総売価」
				// ④ 【画面】.A総売価行の「1kg総売価」=【画面】.B総売価行の「1kg総売価」=【画面】.C総売価行の「1kg総売価」は不可
				// ⑤ 【画面】.B総売価行の「1kg総売価」を入力しないと【画面】.C総売価行の「1kg総売価」を入力できない。
				// 115.P原価	表示・入力不可
				// 116.P総売価
				// ① 入力範囲：1～999,999。
				// ② 新規の場合、初期値はNULLに設置する。
				// ③ 【画面】.A総売価行の「P総売価」>=【画面】.B総売価行の「P総売価」>=【画面】.C総売価行の「P総売価」
				// ④ 【画面】.A総売価行の「P総売価」=【画面】.B総売価行の「P総売価」=【画面】.C総売価行の「P総売価」は不可
				// ⑤ 【画面】.B総売価行の「P総売価」を入力しないと【画面】.C総売価行の「P総売価」を入力できない。
				// 117.入数	表示・入力不可

				// *** 総売価部分 ***
				// 119.A総売価
				//  ① 入力範囲：1～999,999。
				//  ②TG016_4：【画面】.総売価部分の「A総売価」>=「B総売価」>=「C総売価」	→ A総売価1で実施
				//    TG016_5：【画面】.総売価部分の「A総売価」<=「B総売価」<=「C総売価」
				//  ③ 【画面】.総売価部分の「A総売価」=【画面】.総売価部分の「B総売価」=【画面】.総売価部分の「C総売価」は不可。
				//  ④ 新規初期値：NULL。
				//  ⑤ TG016_5の場合は名称マスタより名称コード区分=10656でリストを作成する。
				// 120.B総売価
				//  ① 入力範囲：1～999,999。
				//  ②TG016_4：【画面】.総売価部分の「A総売価」>=「B総売価」>=「C総売価」	→ A総売価1で実施
				//    TG016_5：【画面】.総売価部分の「A総売価」<=「B総売価」<=「C総売価」
				//  ③ 【画面】.総売価部分の「A総売価」=【画面】.総売価部分の「B総売価」=【画面】.総売価部分の「C総売価」は不可
				//  ④ 新規の場合、初期値はNULLに設置する。
				//  ⑤ TG016_5の場合は名称マスタより名称コード区分=10656でリストを作成する。
				// 121.C総売価
				//  ① 入力範囲：1～999,999。
				//  ②TG016_4：【画面】.総売価部分の「A総売価」>=「B総売価」>=「C総売価」	→ A総売価1で実施
				//    TG016_5：【画面】.総売価部分の「A総売価」<=「B総売価」<=「C総売価」
				//  ③ 【画面】.総売価部分の「A総売価」=【画面】.総売価部分の「B総売価」=【画面】.総売価部分の「C総売価」は不可
				//  ④ 【画面】.総売価部分の[B総売価]が入力されいている場合のみ入力可
				//  ⑤ 新規の場合、初期値はNULLに設置する。
				//  ⑥ TG016_5の場合は名称マスタより名称コード区分=10656でリストを作成する。


				// *** 生食・加熱/解凍/養殖 ***
				// 153.生食・加熱
				//  ① 生食、加熱はどちらか一方にしかチェックをつけれない。	※
				//  ② 新規の場合、初期値は非チェックに設置する。
				// 154.解凍 新規の場合、初期値は非チェックに設置する。
				// 155.養殖 新規の場合、初期値は非チェックに設置する。
				if(id===$.id.chk_namanetukbn+1){
					that.queried = false;
					$.setInputboxValue($('#'+$.id.chk_namanetukbn+2), $.id.value_off);
					that.queried = true;
				}
				if(id===$.id.chk_namanetukbn+2){
					that.queried = false;
					$.setInputboxValue($('#'+$.id.chk_namanetukbn+1), $.id.value_off);
					that.queried = true;
				}
			}
		},
		// IDとvalueでチェック処理を実施
		checkInputboxFunc:function(id, newValue, allcheck){
			var that = this;

			// 基本情報
			var moyskbn  = $.getJSONValue(this.jsonHidden, $.id_inp.txt_moyskbn);
			var moysstdt = $.getJSONValue(this.jsonHidden, $.id_inp.txt_moysstdt);
			var moysrban = $.getJSONValue(this.jsonHidden, $.id_inp.txt_moysrban);
			var bmncd    = $.getJSONValue(this.jsonHidden, $.id_inp.txt_bmncd);
			var addshukbn= $.getJSONValue(this.jsonHidden, $.id_inp.txt_addshukbn);

			// 1.月締後変更処理	月締後変更処理と固定表示する。
			// 2.週No.	前の画面から。
			// 3.催しコード	前の画面から。
			// 4.催し名称
			// 5.一日遅パタン アンケート無の場合、NULLを表示。
			// 6.一日遅スライドしない-販売
			//  ① 【画面】.「一日遅れパタン有り」でないと、チェック不可能。	check
			//  ② 【画面】.「販売期間From」 = 【画面】.「販売期間To」 AND 【画面】.「販売期間From」 = 当催しの催し開始日の場合のみチェック可能。	check
			//  ③ 新規の場合、初期値は非チェック状態に設置する。	java
			if(id===$.id.chk_hbslideflg && !$.isEmptyVal(newValue, true)){
				// ①E20293	 「一日遅れパタン有り」がチェックされていないと、一日遅スライドしない-販売はチェックできません。	 	0	 	E
				if($.getInputboxValue($("#"+$.id_inp.txt_hbokureflg)).split("-")[0]!=="1"){
					return "E20293";
				}
				// ②E20387	「販売期間From」 = 「販売期間To」かつ「販売期間From」 = 当催しの催し開始日の場合のみチェックできます	 	0	 	E
				var stdt = $.getInputboxValue($('#'+$.id_inp.txt_hbstdt)).substr(2,8);
				var eddt = $.getInputboxValue($('#'+$.id_inp.txt_hbeddt)).substr(2,8);
				if(!(stdt === eddt && stdt === moysstdt)){
					return "E20387";
				}
			}
			// 7.一日遅スライドしない-納入
			//  ① 【画面】.「一日遅れパタン」有りでないと、チェック不可能。	check
			//  ② 【画面】.「販売期間From」 = 【画面】.「販売期間To」 AND 【画面】.「販売期間From」 = 当催しの催し開始日の場合のみチェック可能。	check
			//  ③ 【画面】.「一日遅スライドしない-販売」がチェックでないとチェック不可。	check
			//  ④ 新規の場合、初期値は非チェック状態に設置する。	java
			if(id===$.id.chk_nhslideflg && !$.isEmptyVal(newValue, true)){
				// ①E20294	「一日遅れパタン有り」がチェックされていないと、一日遅スライドしない-納入はチェックできません。	 	0	 	E
				if($.getInputboxValue($("#"+$.id_inp.txt_hbokureflg)).split("-")[0]!=="1"){
					return "E20294";
				}
				// ②E20387	「販売期間From」 = 「販売期間To」かつ「販売期間From」 = 当催しの催し開始日の場合のみチェックできます	 	0	 	E
				var stdt = $.getInputboxValue($('#'+$.id_inp.txt_hbstdt)).substr(2,8);
				var eddt = $.getInputboxValue($('#'+$.id_inp.txt_hbeddt)).substr(2,8);
				if(!(stdt === eddt && stdt === moysstdt)){
					return "E20387";
				}
				// ③E20295	「一日遅スライドしない-販売」がチェックされていないと、一日遅スライドしない-納入はチェックできません	 	0	 	E
				if($.isEmptyVal($.getInputboxValue($("#"+$.id.chk_hbslideflg)), true)){
					return "E20295";
				}
				// 16_5  【画面】.「発注原売価適用」非チェックの場合のみチェック可。
				if(that.judgeRepType.frm5 && that.judgeRepType.st){
					// E20426	「発注原売価適用」のチェックがある場合、納入期間は入力不可。	 	0	 	E
					if($('#'+$.id.chk_htgenbaikaflg).is(":checked")){
						return "E20426";
					}
				}
			}
			// 8.部門	前の画面から。
			// 9.BY
			//  ① 【画面】.「部門」で催し_デフォルト設定テーブルから所属コードを取得する。	java
			//  ② 所属コードでログイン管理テーブルから社員レコードを取得。職員氏名順に連番を振り、"XX社員名漢字"を【画面】.「BY」リストに保存する。（XX：連番）java　
			//  ③ 新規の場合、初期値は空白行に設置する。	java
			//  ④ 更新の場合、全店特売（アンケート有/無）_商品のBYコードは②のりストに存在しなければ、 BYコード（コードと表示名を同じにする）を【画面】.「BY」リストに加え、画面に表示する。
			//  ⑤ 手入力可でロストフォーカス時に連番と一致するリストを表示する。	js
			//  ⑥フォントサイズを9とする	js
			// 10.商品コード
			//  ① フォーカスアウト時のチェック：先頭2桁が【画面】.「部門」項目と一致する。	check
			//  ② フォーカスアウト時のチェック：商品マスタの存在チェック（商品マスタ.更新区分=0 AND 【画面】.「部門」=商品マスタ.商品コードの頭2桁　だけ）。TODO：頭二けただけ？？	check
			//  ③ 新規の場合、初期値はNULLに設置する。	java
			if(id===$.id_inp.txt_shncd && !$.isEmptyVal(newValue)){
				// ① E20240	部門に属さない商品コードです。	 	0	 	E
				if((('00' + bmncd).slice(-2))!==newValue.substr(0,2)){
					return "E20240";
				}
				// ② E20257	商品マスタに存在しません。	 	0	 	E
				var code_chk = $.getInputboxData(that.name, $.id.action_check, id, [{KEY:"MST_CNT",value:newValue}]);
				if(code_chk === "0"){
					return "E20257";
				}

				// 16_5  ① 商品マスタ.商品種類=5のみ。
				if(allcheck && that.judgeRepType.frm5 && that.judgeRepType.st){
					// E20488	ダミーコード（商品種類5）以外は入力できません。	 	0	 	E
					if(!$.isEmptyVal(that.getColValue("F174")) && that.getColValue("F174")!=="5"){
						return "E20488";
					}
				}

			}
			// 11.商品マスタ名称
			// 12.グループNo.
			//  ① 半角。	easyui
			//  ② 新規の場合、初期値はNULLに設置する。	java
			// 13.子No.
			//  ① 入力範囲00～99。	easyui
			//  ② 同一催し内の同じ「グループNo.」中でユニーク。	check
			//  ③ 新規の場合、初期値はNULLに設置する。	java
			if(allcheck && id===$.id_inp.txt_chldno && !$.isEmptyVal(newValue)){

				var parno = $.getInputboxValue($('#'+$.id_inp.txt_parno));
				var param = {};

				if (!$.isEmptyVal(parno)) {
					// ②E20484	子No.は同じグループNo.中でユニークでなければいけません。	 	0	 	E
					param["KEY"] =  "MST_CNT";
					if (that.judgeRepType.toktg) {
						param["value"] = moyskbn + ',' + moysstdt + ',' + moysrban + ',' + parno + ',' + newValue + ',1';
					} else {
						param["value"] = moyskbn + ',' + moysstdt + ',' + moysrban + ',' + parno + ',' + newValue + ',2';
					}

					var code_chk = $.getInputboxData(that.name, $.id.action_check, $.id_inp.txt_chldno, [param]);

					if(code_chk !== "0"){
						if (!that.judgeRepType.sei_new) {
							var bmncd    = $.getJSONValue(this.jsonHidden, $.id_inp.txt_bmncd);
							var kanrino  = $.getJSONValue(this.jsonHidden, $.id_inp.txt_kanrino);
							var kanrieno = $.getJSONValue(this.jsonHidden, $.id_inp.txt_kanrieno);

							if (that.judgeRepType.toktg) {
								param["value"] = moyskbn + ',' + moysstdt + ',' + moysrban + ',' + parno + ',' + newValue + ',1,' + bmncd + ',' + kanrino + ',' + kanrieno;
							} else {
								param["value"] = moyskbn + ',' + moysstdt + ',' + moysrban + ',' + parno + ',' + newValue + ',2,' + bmncd + ',' + kanrino + ',' + kanrieno;
							}
							code_chk = $.getInputboxData(that.name, $.id.action_check, $.id_inp.txt_chldno, [param]);

							if (code_chk !== "1") {
								return "E20484";
							}
						} else {
							return "E20484";
						}
					}
				}
			}
			// 14.日替 新規の場合、初期値は非チェック状態に設置する。	java
			// 15.販売期間From/16.販売期間To
			//  ① 入力範囲：2003/01/01～9999/12/31。	check
			//  ② 【画面】.「販売期間From」≧当催しの販売開始日 AND 【画面】.「販売期間To」≦当催しの販売終了日。	check
			//  ③ 【画面】.「販売期間From」と【画面】.「販売期間To」の両方入力　OR　両方NULL
			//  ④ 【画面】.「販売期間From」≦【画面】.「販売期間To」
			//  ⑤ 催し区分=0：レギュラーの場合、【画面】.「販売期間」編集不可。
			//  ⑥ 更新の時は編集不可。
			//  ⑦ 新規の場合、初期値はNULLに設置する。
			//  ⑧ 【画面】.「販売期間From」と【画面】.「販売期間To」が全て入力されたら、販売情報タブを編集可能にする。クリアされた場合は販売情報タブを編集不可にし、値をクリアする。編集不可状態の販売情報タブの値はDBへ保存しないが、メーカー名、POP名、規格は編集不可でもDBへ保存する。
			if(allcheck && (id===$.id_inp.txt_hbstdt||id===$.id_inp.txt_hbeddt)){
				var stdt = id===$.id_inp.txt_hbstdt ? $.getParserDt(newValue): $.getInputboxValue($('#'+$.id_inp.txt_hbstdt));
				var eddt = id===$.id_inp.txt_hbeddt ? $.getParserDt(newValue): $.getInputboxValue($('#'+$.id_inp.txt_hbeddt));
				if(!$.isEmptyVal(newValue)){
					// ①E20296	販売期間の入力可能範囲は2003/01/01から9999/12/31です。	 	0	 	E
					if(stdt*1 < 20030101 && eddt*1 > 99991231){
						return "E20387";
					}
					// ②E20481	「販売期間From」と「販売期間To」は当催しの販売期間内で入力してください。	 	0	 	E
					if(!(stdt >= that.moycdData[0]["HBSTDT"] && eddt <= that.moycdData[0]["HBEDDT"])){
						return "E20481";
					}
					// ④E20298	販売期間From ≦ 販売期間Toの条件で入力してください。	 	0	 	E
					if(!(stdt <= eddt)){
						return "E20298";
					}
				}
				// ③E20297	販売期間Fromと販売期間Toの両方入力または両方未入力としてください。	 	0	 	E
				if((stdt.legnth > 0 && eddt.legnth === 0)||(stdt.legnth === 0 && eddt.legnth > 0)){
					return "E20297";
				}
			}
			// 17.納入期間From/18.納入期間To
			//  ① 入力範囲：2003/01/01～9999/12/31。
			//  ② 【画面】.「納入期間From」≧当催しの納入開始日 AND 【画面】.「納入期間To」≦当催しの納入終了日。
			//  ③ 【画面】.「納入期間From」と【画面】.「納入期間To」の両方入力　OR　両方NULL。
			//  ④ 【画面】.「納入期間From」≦【画面】.「納入期間To」
			//  ⑤ 更新の時は編集不可。
			//  ⑥ 新規の場合、初期値はNULLに設置する。
			//  ⑦ 【画面】.「納入期間From」と【画面】.「納入期間To」が全て入力されたら、納入情報タブを編集可能にする。また、入力された納入期間に応じて納入情報タブの納入対象にチェックをつける。クリアされた場合は納入情報タブを編集不可にし、値をクリアする。
			if(allcheck && (id===$.id_inp.txt_nnstdt||id===$.id_inp.txt_nneddt)){
				var stdt = id===$.id_inp.txt_nnstdt ? $.getParserDt(newValue): $.getInputboxValue($('#'+$.id_inp.txt_nnstdt));
				var eddt = id===$.id_inp.txt_nneddt ? $.getParserDt(newValue): $.getInputboxValue($('#'+$.id_inp.txt_nneddt));
				if(!$.isEmptyVal(newValue)){
					// ①E20299	納入期間の入力可能範囲は2003/01/01から9999/12/31です。	 	0	 	E
					if(stdt*1 < 20030101 && eddt*1 > 99991231){
						return "E20299";
					}

					// ②E20482	「納入期間From」と「納入期間To」は当催しの納入期間内で入力してください。	 	0	 	E
					// 生鮮およびデイリーについては納入開始日+1～納入終了日まで
					if(!(stdt >= that.moycdData[0]["CHK_NNSTDT"] && eddt <= that.moycdData[0]["CHK_NNEDDT"])){
						return "E20482";
					}
					// ④E20301	納入期間From ≦ 納入期間Toの条件で入力してください。	 	0	 	E
					if(!(stdt <= eddt)){
						return "E20301";
					}
				}
				// ③E20300	納入期間Fromと納入期間Toの両方入力または両方未入力としてください。	 	0	 	E
				if((stdt.legnth > 0 && eddt.legnth === 0)||(stdt.legnth === 0 && eddt.legnth > 0)){
					return "E20300";
				}

				// 16_5  【画面】.「発注原売価適用」非チェックの場合必須。それ以外は入力不可。
				if(that.judgeRepType.frm5 && that.judgeRepType.st){
					// E20426	「発注原売価適用」のチェックがある場合、納入期間は入力不可。	 	0	 	E
					// E20427	「発注原売価適用」非チェックの場合、納入期間必須。	 	0	 	E
					if($('#'+$.id.chk_htgenbaikaflg).is(":checked") && !($.isEmptyVal(stdt)||$.isEmptyVal(eddt))){
						return "E20426";
					}else if(!$('#'+$.id.chk_htgenbaikaflg).is(":checked") && ($.isEmptyVal(stdt)||$.isEmptyVal(eddt))){
						return "E20427";
					}
				}
			}
			// 20.チラシ未掲載
			//  ① 商品マスタ.商品種別=1（原材料）の場合、チェックしないとエラー。
			//  ② アンケート無の場合、デフォルトでチェックを付け、編集不可。
			//  ③ 新規の場合、上記の①と②以外のデフォルトはチェックしない状態にする。
			if(allcheck && id===$.id.chk_chirasflg){
				// ①E20454	商品種別=1（原材料）の場合、チェックが必須です。	 	0	 	E
				if(that.getColValue("F174") === '1' && $.isEmptyVal(newValue, true)){
					return "E20454";
				}
			}
			// 21.対象店
			//  ① 入力範囲：001～999。
			//  ② 更新の時は編集不可。
			//  ③ 新規の場合、初期値はNULLに設置する。
			if(allcheck && id===$.id_inp.txt_rankno_add){
				// 16_5  【画面】.「発注原売価適用」非チェックの場合必須。それ以外は入力不可。
				if(that.judgeRepType.frm5 && that.judgeRepType.st && that.judgeRepType.toktg_t){
					// E20428	「発注原売価適用」のチェックがある場合は対象店が入力不可。	 	0	 	E
					// E20429	「発注原売価適用」のチェックが無い場合対象店必須。	 	0	 	E
					if($('#'+$.id.chk_htgenbaikaflg).is(":checked") && !$.isEmptyVal(newValue, true)){
						return "E20428";
					}else if(!$('#'+$.id.chk_htgenbaikaflg).is(":checked") && $.isEmptyVal(newValue, true)){
						return "E20429";
					}
				}

				var rankNoAdd = that.getColValue("F21");
				var rankNoDel = that.getColValue("F22");

				// 対象・除外店ランク№に同一の値が入力された場合エラー
				if (!$.isEmptyVal(rankNoAdd) && !$.isEmptyVal(rankNoDel) && rankNoAdd===rankNoDel) {
					return 'E20016'; //対象店ランクNo.と除外店ランクNo.が同じです。
				}

				// 対象店・除外店を作成
				var tencds = [];
				var inpTenAddArr = [];
				var inpTenDelArr = [];

				for (var i = 0; i < 10; i++) {
					var tenCd = $.getInputboxValue($('#'+$.id_inp.txt_tencd+'_add' + (i+1))).trim();
					if (tenCd!=="" && tenCd!==null && tenCd!==undefined) {
						inpTenAddArr.push(tenCd)
						tencds.push(tenCd);
					}

					tenCd = $.getInputboxValue($('#'+$.id_inp.txt_tencd+'_del' + (i+1))).trim();
					if (tenCd!=="" && tenCd!==null && tenCd!==undefined) {
						inpTenDelArr.push(tenCd)
						tencds.push(tenCd);
					}

					// 重複チェック
					var tencds_ = tencds.filter(function (element, index, self) { return self.indexOf(element)*1 === index; });
					if(tencds.length !== tencds_.length){
						return 'E11141';
					}
				}

				if (!$.isEmptyVal(rankNoAdd)) {
					// 存在しないランク
					var param = {};
					var addRankArr = "";
					var delRankArr = "";
					var kanrino  = $.getJSONValue(this.jsonHidden, $.id_inp.txt_kanrino);

					param["KEY"] =  "MST_CNT";
					var sendId = $.id_inp.txt_rankno + '_ARR';
					param["value"] = bmncd + ',' + rankNoAdd + ',' + moyskbn + moysstdt + moysrban + ',' + kanrino;

					addRankArr = $.getInputboxData(that.name, $.id.action_check, sendId, [param]);

					if (rankNoDel!=="" && rankNoDel!==undefined && rankNoDel!==null && $.isEmptyVal(kanrino)) {
						param["KEY"] =  "MST_CNT";
						sendId = $.id_inp.txt_rankno + '_ARR';
						param["value"] = bmncd + ',' + rankNoDel + ',' + moyskbn + moysstdt + moysrban + ',' + kanrino;
						delRankArr = $.getInputboxData(that.name, $.id.action_check, sendId, [param]);
					}


					// 対象店・除外店の入力がない場合チェック不要
					if (!$.isEmptyVal(addRankArr)) {
						var tenAddErr = [];
						var tenRankAddArrSplit = addRankArr.split("");

						// ランクマスタから取得した対象店と入力された対象店に重複があった場合エラー
						for (var i = 0; i < inpTenAddArr.length; i++) {
							if (tenRankAddArrSplit.length >= inpTenAddArr[i] && tenRankAddArrSplit[inpTenAddArr[i]-1].trim()!=="") {

								// 対象店のみの指定の場合エラー、それ以外の場合はエラー店舗を一度保持
								if ($.isEmptyVal(delRankArr) && inpTenDelArr.length === 0) {
									return 'E20025'; // 既に対象店となっている店を追加しようとしました。
								} else {
									// ランクマスタから取得した除外店に同一の店舗があった場合OKの為一度保持②
									tenAddErr.push(inpTenAddArr[i]);
								}
							}
						}

						if (!$.isEmptyVal(kanrino)) {

							// ランクマスタから取得した対象店と入力された対象店に重複があった場合エラー
							for (var i = 0; i < inpTenDelArr.length; i++) {
								if (tenRankAddArrSplit.length >= inpTenDelArr[i] && tenRankAddArrSplit[inpTenDelArr[i]-1].trim()==="") {
									var err = true;
									for (var j = 0; j < inpTenAddArr.length; j++) {
										if (inpTenDelArr[i]===inpTenAddArr[j]) {
											err = false;
											break;
										}
									}

									if (err) {
										return 'E20026'; // 既に対象店ではない店を除外しようとしました。
									}
								}

								for (var j = 0; j < tenAddErr.length; j++) {
									if (inpTenDelArr[i]===tenAddErr[j]) {
										tenAddErr.splice(j,1);
									}
								}
							}

							if (tenAddErr.length !== 0) {
								return 'E20025'; // 既に対象店となっている店を追加しようとしました。
							}
						} else {
							if (!$.isEmptyVal(delRankArr) || inpTenDelArr.length !== 0) {
								var tenRankDelArrSplit = delRankArr.split("");
								var err = true;

								// ランクマスタから取得した除外店と入力された除外店に重複があった場合エラー
								for (var i = 0; i < inpTenDelArr.length; i++) {
									if (tenRankDelArrSplit.length >= inpTenDelArr[i] && tenRankDelArrSplit[inpTenDelArr[i]-1].trim()!=="") {
										return 'E20026'; // 既に対象店ではない店を除外しようとしました。
									}
								}

								// ランクマスタから取得した除外店と②に重複がなかった場合エラー
								for (var i = 0; i < tenAddErr.length; i++) {
									if (tenRankDelArrSplit.length >= tenAddErr[i] && tenRankDelArrSplit[tenAddErr[i]-1].trim()==="") {
										err = true;
										for (var j = 0; j < inpTenDelArr.length; j++) {
											if (inpTenDelArr[j]===tenAddErr[i]) {
												err = false;
												break;
											}
										}
										if (err) {
											return 'E20025'; // 既に対象店となっている店を追加しようとしました。
										}
									}
								}
							}
						}
					}

					var values = {};
					values["BMNCD"]		= $.getJSONValue(that.jsonHidden, $.id_inp.txt_bmncd);
					values["MOYSKBN"]	= $.getJSONValue(that.jsonHidden, $.id_inp.txt_moyskbn);	// 催し区分
					values["MOYSSTDT"]	= $.getJSONValue(that.jsonHidden, $.id_inp.txt_moysstdt);	// 催し開始日
					values["MOYSRBAN"]	= $.getJSONValue(that.jsonHidden, $.id_inp.txt_moysrban);	// 催し連番
					values["RANKNOADD"]	= rankNoAdd;												// ランク№(追加)
					values["RANKNODEL"]	= rankNoDel;												// ランク№(除外)
					values["TENCDADDS"]	= inpTenAddArr;												// 対象店
					values["TENCDDELS"]	= inpTenDelArr;												// 除外店

					var chk_cnt = $.getInputboxData(that.name, $.id.action_check, id, [values]);
					if (chk_cnt==='0') {
						return 'E20027';
					}
				}
			}
			// 22.除外店
			//  ① 入力範囲：001～999。
			//  ② 更新の時は編集不可。
			//  ③ 新規の場合、初期値はNULLに設置する。
			//  ④ アンケート有の場合、編集不可。
			// 23.追加（1～10）
			// 33.ランク（1～10）
			//  ① 全店特売（アンケート有/無）_対象除外店.対象除外フラグ=1：対象で保存する。
			//  ② 入力範囲：店番号001～400、ランクA～Z。
			//  ③ 【画面】.「追加」に店番を入力したら、当列の【画面】.「ランク」は必須とする。
			//  ④ 新規の場合、初期値はNULLに設置する。
			if(allcheck && id===$.id_inp.txt_tencd+"_add"){
				for (var i = 1; i <= 10; i++){
					var add = $.getInputboxValue($('#'+$.id_inp.txt_tencd+"_add"+i));
					var rnk = $.getInputboxValue($('#'+$.id_inp.txt_tenrank+i));
					// ③E20453	「追加」に店番を入力した場合、対応する「ランク」は必須です。	 	0	 	E
					if(!$.isEmptyVal(add)&&$.isEmptyVal(rnk)){
						return "E20453";
					}

					if (add*1 > 400) {
						return "E20292";
					}
				}
				if(that.judgeRepType.frm5 && that.judgeRepType.st && that.judgeRepType.toktg_t){
					// 16_5  ① 【画面】.「発注原売価適用」のチェックが無い場合任意。② それ以外は入力不可。
					if($('#'+$.id.chk_htgenbaikaflg).is(":checked")){
						for (var i = 1; i <= 10; i++){
							var val = $.getInputboxValue($('#'+$.id_inp.txt_tencd+"_add"+i));
							// E20430	「発注原売価適用」のチェックがある場合は追加が入力不可。	 	0	 	E
							if(!$.isEmptyVal(val, true)){
								return "E20430";
							}
						}
					}
				}
			}
			if(allcheck && id===$.id_inp.txt_tenrank){
				if(that.judgeRepType.frm5 && that.judgeRepType.st && that.judgeRepType.toktg_t){
					// 16_5  ① 【画面】.「発注原売価適用」のチェックが無い場合任意。② それ以外は入力不可。
					if($('#'+$.id.chk_htgenbaikaflg).is(":checked")){
						for (var i = 1; i <= 10; i++){
							var val = $.getInputboxValue($('#'+$.id_inp.txt_tenrank+i));
							// E20431	「発注原売価適用」のチェックがある場合はランクが入力不可。	 	0	 	E
							if(!$.isEmptyVal(val, true)){
								return "E20431";
							}
						}
					}
				}
			}
			// 43.除外（1～10）
			//  ① 全店特売（アンケート有/無）_対象除外店.対象除外フラグ=2：除外で保存する。
			//  ② 入力範囲：001～400。
			//  ③ 新規の場合、初期値はNULLに設置する。
			if(id===$.id_inp.txt_tencd+"_del"){

				for (var i = 1; i <= 10; i++){
					var val = $.getInputboxValue($('#'+$.id_inp.txt_tencd+"_del"+i));

					if (val*1 > 400) {
						return "E20292";
					}
				}

				if(that.judgeRepType.frm5 && that.judgeRepType.st && that.judgeRepType.toktg_t){
					// 16_5  ① 【画面】.「発注原売価適用」のチェックが無い場合任意。② それ以外は入力不可。
					if($('#'+$.id.chk_htgenbaikaflg).is(":checked")){
						for (var i = 1; i <= 10; i++){
							var val = $.getInputboxValue($('#'+$.id_inp.txt_tencd+"_del"+i));
							// E20432	「発注原売価適用」のチェックがある場合は除外が入力不可。	 	0	 	E
							if(!$.isEmptyVal(val, true)){
								return "E20432";
							}

							if (val*1 > 400) {
								return "E20292";
							}
						}
					}
				}
			}

			// *** 予定数 ***
			// 29.予定数
			//  ① 入力範囲：0～999,999。
			//  ② 新規の場合、初期値はNULLに設置する。
			// 30.仕入額
			//  ① 全特（ア有/無）_商品.定貫PLU不定貫区分=1:定貫・PLUの場合、【画面】.「予定数」×【画面】.特売事前行の「原価」×【画面】.「特売事前入数」。
			//  ② 全特（ア有/無）_商品.定貫PLU不定貫区分=2:不定貫の場合、【画面】.「予定数」×【画面】.A総売価行「P原価」×【画面】.「A総売価行入数」。
			//  ③ 結果は小数切り捨てで表示する。
			// 31.販売額
			//  ① 全特（ア有/無）_商品.定貫PLU不定貫区分=1:定貫・PLUの場合、【画面】.「予定数」×【画面】.特売事前行の「本体売価」×【画面】.「特売事前入数」。
			//  ② 全特（ア有/無）_商品.定貫PLU不定貫区分=2:不定貫の場合、【画面】.「予定数」×（【画面】.A総売価行の「P総売価」-税額）×【画面】.「A総売価行入数」。税額の計算は、『特売共通仕様書 総額売価計算方法』部分参照。
			// 32.荒利額	【画面】.「販売額」-【画面】.「仕入額」。

			// *** レギュラー行 ***
			// ① 【画面】.「商品コード」を入力したタイミングで表示。
			// ② 【画面】.「商品コード」をクリアしたタイミングでクリア。
			// 36.原価
			// 37.A総売価	商品マスタ.レギュラー情報_売価より税込み計算。『特売共通仕様書 総額売価計算方法』部分参照。
			// 38.本体売価	【画面】.レギュラー行の「A総売価」より税抜き計算。『特売共通仕様書 本体売価算出方法』部分参照。
			// 39.入数
			// 40.値入
			//  ① （【画面】.レギュラー行の「本体売価」－商品マスタ.レギュラー情報_原価）／【画面】.レギュラー行の「本体売価」*100。
			//  ② 小数点以下3位切り捨て, 第2位まで求める。

			// *** 特売事前行 ***
			// 42.原価
			//  ① 入力範囲：1～999,999.99。
			//  ② 『特売共通仕様書 原価・売価・入数のデフォルト表示』部分参照。
			//  ③ 通常は【画面】.特売追加行の「原価」 = 【画面】.特売事前行の「原価」でないとエラー。
			//  ④ 但し、ドライの場合は【画面】.特売追加行の「原価」 >= 【画面】.特売事前行の「原価」を許す。また、ドライ以外で【画面】.納入情報タブの「週次伝送flg」 = 1 and 【画面】.納入情報タブの「PC区分」 = 0なら、【画面】.特売追加行の「原価」 >= 【画面】.特売事前行の「原価」を許す。
			if(allcheck && id===$.id_inp.txt_genkaam_mae){
				var genkaam_mae = id===$.id_inp.txt_genkaam_mae ? newValue: $.getInputboxValue($('#'+$.id_inp.txt_genkaam_mae));
				var genkaam_ato = id===$.id_inp.txt_genkaam_ato ? newValue: $.getInputboxValue($('#'+$.id_inp.txt_genkaam_ato));

				var kbn = "";

				if (that.judgeRepType.frm3 || that.judgeRepType.frm2) {
					kbn = $.getInputboxValue($('[name='+$.id.rad_tkanplukbn+']').eq(0));	// that.grd_data['F57']
				}

				if(!(that.judgeRepType.frm1 || (!that.judgeRepType.frm1 && $('#'+$.id.chk_shudenflg).is(":checked") && that.getColValue("F173")==="0"))){
					if (kbn!=='2') {
						// ③E20540	通常は特売追加行の「原価」= 特売事前行の「原価」の条件で入力してください。	 	0	 	E
						if(!$.isEmptyVal(genkaam_mae) && !$.isEmptyVal(genkaam_ato) && !(genkaam_mae === genkaam_ato)){
							return "E20540";
						}
					}
				}
				if(that.judgeRepType.frm1||that.judgeRepType.frm2||that.judgeRepType.frm3){

					if (that.judgeRepType.frm3 || that.judgeRepType.frm2) {
						if ($.isEmptyVal(kbn)) {
							return "E20549";
						}
					}

					//  16_1,16_2,16_3
					//   ① 【画面】.特売事前行の「原価」と【画面】.特売追加行の「原価」どちらか必須。入力がない場合、【画面】.特売事前行の「原価」をコピーする。
					//   ② 新規初期値：『特売共通仕様書 原価・売価・入数のデフォルト表示』部分参照。
					// E20304	特売事前行の「原価」と 特売追加行の「原価」のいずれかを入力してください。	 	0	 	E
					if($.isEmptyVal(genkaam_mae) && $.isEmptyVal(genkaam_ato) && ($.isEmptyVal(kbn) || kbn==='1')){
						return "E20304";

					// 特売追加行の「A総売価」 と 特売事前行の「A総売価」の大小関係が不正です
					} else if (!$.isEmptyVal(genkaam_mae) && !$.isEmptyVal(genkaam_ato) && ($.isEmptyVal(kbn) || (!$.isEmptyVal(kbn) && kbn!=='2')) && (genkaam_mae*1 > genkaam_ato*1)) {
						return "E20388";
					}
				}
			}
			// 43.A総売価
			//  ① 入力範囲：1～999,999。
			//  ② 【画面】.レギュラー行の「A総売価」>=【画面】.特売事前行の「A総売価」。但し、【画面】.レギュラー行の「A総売価」=0orNULLの場合と催し区分=0の場合、あるいは精肉、鮮魚の場合は本チェックを行わない。
			//  ③ 【画面】.特売事前行の「A総売価」>=【画面】.B部分の「B総売価」>=【画面】.C部分の「C総売価」。
			//  ④ 【画面】.特売事前行の「A総売価」=【画面】.B部分の「B総売価」=【画面】.C部分の「C総売価」は不可。
			//  ⑤ 『特売共通仕様書 原価・売価・入数のデフォルト表示』部分参照。
			if(allcheck && id===$.id_inp.txt_a_baikaam+1 && !$.isEmptyVal(newValue)){
				var kbn = "";

				if (that.judgeRepType.frm3 || that.judgeRepType.frm2) {
					kbn = $.getInputboxValue($('[name='+$.id.rad_tkanplukbn+']').eq(0));	// that.grd_data['F57']
				}

				if (kbn!=='2') {
					var r_baikaam = that.getColValue("F59").replace(',','');
					if (!$.isEmptyVal(r_baikaam)) {
						r_baikaam = Number(r_baikaam);
					}
					var a_baikaam = id===$.id_inp.txt_a_baikaam+1 ? newValue: $.getInputboxValue($('#'+$.id_inp.txt_a_baikaam+1));
					var b_baikaam = id===$.id_inp.txt_b_baikaam+1 ? newValue: $.getInputboxValue($('#'+$.id_inp.txt_b_baikaam+1));
					var c_baikaam = id===$.id_inp.txt_c_baikaam+1 ? newValue: $.getInputboxValue($('#'+$.id_inp.txt_c_baikaam+1));

					// ②E20305	レギュラー行の「A総売価」 ≧ 特売事前行の「A総売価」の条件で入力してください。	 	0	 	E
					if(!($.isEmptyVal(r_baikaam, true) || moyskbn===$.id.value_moykbn_r || that.judgeRepType.frm2 || that.judgeRepType.frm3)){
						if(!(r_baikaam >= a_baikaam*1)){
							return "E20305";
						}
					}
					// ③E20306	特売事前行の「A総売価」 ≧ B部分の「B総売価」 ≧ C部分の「C総売価」の条件で入力してください。	 	0	 	E
					if(!(a_baikaam*1 >= b_baikaam*1 && b_baikaam*1 >= c_baikaam*1)){
						return "E20306";
					}
					// ④E20307	特売事前行の「A総売価」= B部分の「B総売価」= C部分の「C総売価」はエラーです。	 	0	 	E
					if(a_baikaam*1 == b_baikaam*1 && b_baikaam*1 == c_baikaam*1){
						return "E20307";
					}
				}
			}
			// 44.本体売価	【画面】.特売事前行の「A総売価」より税抜き計算。『特売共通仕様書 本体売価算出方法』部分参照。
			// 45.入数
			//  ① 入力範囲：1～999。
			//  ② 『特売共通仕様書 原価・売価・入数のデフォルト表示』部分参照。
			// 46.値入
			//  ① （【画面】.特売事前行の「本体売価」－【画面】.特売事前行の「原価」）／【画面】.特売事前行の「本体売価」*100。
			//  ② 小数点以下3位切り捨て, 第2位まで求める。

			// *** 特売追加行 ***
			// 48.原価
			//  ① 入力範囲：1～999,999.99。
			//  ② 『特売共通仕様書 原価・売価・入数のデフォルト表示』部分参照。
			// 49.A総売価	表示・入力不可
			// 50.本体売価	表示・入力不可
			// 51.入数	表示・入力不可
			// 52.値入
			//  ① （【画面】.特売事前行の「本体売価」－【画面】.特売追加行の「原価」）／【画面】.特売事前行の「本体売価」*100。
			//  ② 小数点以下3位切り捨て, 第2位まで求める。

			// *** B部分 ***
			// 54.B総売価
			//  ① 入力範囲：1～999,999。
			//  ② 新規の場合、初期値はNULLに設置する。
			//  ③ 【画面】.特売事前行の「A総売価」>=【画面】.B部分の「B総売価」>=【画面】.C部分の「C総売価」
			//  ④ 【画面】.特売事前行の「A総売価」=【画面】.B部分の「B総売価」=【画面】.C部分の「C総売価」は不可
			if(allcheck && id===$.id_inp.txt_b_baikaam+1 && !$.isEmptyVal(newValue)){
				var a_baikaam = id===$.id_inp.txt_a_baikaam+1 ? newValue: $.getInputboxValue($('#'+$.id_inp.txt_a_baikaam+1));
				var b_baikaam = id===$.id_inp.txt_b_baikaam+1 ? newValue: $.getInputboxValue($('#'+$.id_inp.txt_b_baikaam+1));
				var c_baikaam = id===$.id_inp.txt_c_baikaam+1 ? newValue: $.getInputboxValue($('#'+$.id_inp.txt_c_baikaam+1));

				var kbn = "";

				if (that.judgeRepType.frm3 || that.judgeRepType.frm2) {
					kbn = $.getInputboxValue($('[name='+$.id.rad_tkanplukbn+']').eq(0));	// that.grd_data['F57']
				}

				if (kbn!=='2') {
					// ③E20306	特売事前行の「A総売価」 ≧ B部分の「B総売価」 ≧ C部分の「C総売価」の条件で入力してください。	 	0	 	E
					if(!(a_baikaam*1 >= b_baikaam*1 && b_baikaam*1 >= c_baikaam*1)){
						return "E20306";
					}
					// ④E20307	特売事前行の「A総売価」= B部分の「B総売価」= C部分の「C総売価」はエラーです。	 	0	 	E
					if(a_baikaam*1 == b_baikaam*1 && b_baikaam*1 == c_baikaam*1){
						return "E20307";
					}
				}
			}

			// 55.本体売価	【画面】.B部分の「B総売価」より税抜き計算。『特売共通仕様書 本体売価算出方法』部分参照。
			// 56.値入
			//  ① （【画面】.B部分の「本体売価」－【画面】.特売事前行の「原価」）／【画面】.B部分の「本体売価」*100。
			//  ② 小数点以下3位切り捨て, 第2位まで求める。
			// 57.B売店
			//  ① 入力範囲：001～999。
			//  ② 新規の場合、初期値はNULLに設置する。
			//  ③ アンケート有の場合、編集不可。
			if(allcheck && id===$.id_inp.txt_rankno_add_b){
				var kbn = "";

				if (that.judgeRepType.frm3 || that.judgeRepType.frm2) {
					kbn = $.getInputboxValue($('[name='+$.id.rad_tkanplukbn+']').eq(0));	// that.grd_data['F57']
				}

				if((that.judgeRepType.frm1) && that.judgeRepType.st && that.judgeRepType.toksp && kbn!=='2'){
					// 16_1 【画面】B部分の「B総売価」が入力されたら、必須となる。入力がなかったら、編集不可。
					var baika = $.getInputboxValue($('#'+$.id_inp.txt_b_baikaam+1));
					// E20389	B部分の「B総売価」の入力がない場合、B売店入カはできません。	 	0	 	E
					// E20390	B部分の「B総売価」が入力された場合、Ｂ売店は必須です。	 	0	 	E
					if($.isEmptyVal(baika) && !$.isEmptyVal(newValue, true)){
						return "E20389";
					}else if(!$.isEmptyVal(baika)  && $.isEmptyVal(newValue, true)){
						return "E20390";
					}
				}
				if((that.judgeRepType.frm2||that.judgeRepType.frm3) && that.judgeRepType.st && that.judgeRepType.toksp){
					// 16_2,16_3【画面】B部分の「B総売価」　OR　【画面】B総売価行の「100ｇ総売価」の入力がある場合必須。全てに入力がなかったら、編集不可。
					var baika1 = $.getInputboxValue($('#'+$.id_inp.txt_b_baikaam+1));
					var baika2 = $.getInputboxValue($('#'+$.id_inp.txt_b_baikaam+2));
					// E20309	B部分の「B総売価」あるいはB総売価行の「100ｇ総売価」に入力がなかったら	B売店は入力できません。	0	 	E
					// E20310	B部分の「B総売価」あるいはB総売価行の「100ｇ総売価」の入力がある場合はＢ売店が必須です。	 	0	 	E
					if(($.isEmptyVal(baika1) && $.isEmptyVal(baika2)) && !$.isEmptyVal(newValue, true)){
						return "E20309";
					}else if(!($.isEmptyVal(baika1) && $.isEmptyVal(baika2)) && $.isEmptyVal(newValue, true)){
						return "E20310";
					}
				}
				if((that.judgeRepType.frm5) && that.judgeRepType.st && that.judgeRepType.toksp && kbn!=='2'){
					// 16_5 【画面】B部分の「B総売価」が入力されたら、必須となる。入力がなかったら、編集不可。
					var baika = $.getInputboxValue($('#'+$.id_mei.kbn10656+"_b"));

					if (baika==='-1') {
						baika = '';
					}

					// E20389	B部分の「B総売価」の入力がない場合、B売店入カはできません。	 	0	 	E
					// E20390	B部分の「B総売価」が入力された場合、Ｂ売店は必須です。	 	0	 	E
					if($.isEmptyVal(baika) && !$.isEmptyVal(newValue, true)){
						return "E20389";
					}else if(!$.isEmptyVal(baika)  && $.isEmptyVal(newValue, true)){
						return "E20390";
					}
				}
				if((that.judgeRepType.frm4) && that.judgeRepType.st && that.judgeRepType.toksp && kbn!=='2'){
					// 16_5 【画面】B部分の「B総売価」が入力されたら、必須となる。入力がなかったら、編集不可。
					var baika = $.getInputboxValue($('#'+$.id_inp.txt_b_baikaam+3));

					// E20389	B部分の「B総売価」の入力がない場合、B売店入カはできません。	 	0	 	E
					// E20390	B部分の「B総売価」が入力された場合、Ｂ売店は必須です。	 	0	 	E
					if($.isEmptyVal(baika) && !$.isEmptyVal(newValue, true)){
						return "E20389";
					}else if(!$.isEmptyVal(baika)  && $.isEmptyVal(newValue, true)){
						return "E20390";
					}
				}
			}

			// *** C部分 ***
			// 60.C総売価
			//  ① 入力範囲：1～999,999。
			//  ② 新規の場合、初期値はNULLに設置する。
			//  ③ 【画面】.特売事前行の「A総売価」>=【画面】.B部分の「B総売価」>=【画面】.C部分の「C総売価」
			//  ④ 【画面】.特売事前行の「A総売価」=【画面】.B部分の「B総売価」=【画面】.C部分の「C総売価」は不可
			//  ⑤ 【画面】.B部分の[B総売価]が入力されいている場合のみ入力可
			if(allcheck && id===$.id_inp.txt_c_baikaam+1 && !$.isEmptyVal(newValue)){
				var a_baikaam = id===$.id_inp.txt_a_baikaam+1 ? newValue: $.getInputboxValue($('#'+$.id_inp.txt_a_baikaam+1));
				var b_baikaam = id===$.id_inp.txt_b_baikaam+1 ? newValue: $.getInputboxValue($('#'+$.id_inp.txt_b_baikaam+1));
				var c_baikaam = id===$.id_inp.txt_c_baikaam+1 ? newValue: $.getInputboxValue($('#'+$.id_inp.txt_c_baikaam+1));

				var kbn = "";

				if (that.judgeRepType.frm3 || that.judgeRepType.frm2) {
					kbn = $.getInputboxValue($('[name='+$.id.rad_tkanplukbn+']').eq(0));	// that.grd_data['F57']
				}

				if (kbn!=='2') {
					// ③E20306	特売事前行の「A総売価」 ≧ B部分の「B総売価」 ≧ C部分の「C総売価」の条件で入力してください。	 	0	 	E
					if(!(a_baikaam*1 >= b_baikaam*1 && b_baikaam*1 >= c_baikaam*1)){
						return "E20306";
					}
					// ④E20307	特売事前行の「A総売価」= B部分の「B総売価」= C部分の「C総売価」はエラーです。	 	0	 	E
					if(a_baikaam*1 == b_baikaam*1 && b_baikaam*1 == c_baikaam*1){
						return "E20307";
					}
					// ⑤E20544	総売価部分の「B総売価」が入力されいている場合のみ「C総売価」は入力できます。	 	0	 	E
					if($.isEmptyVal(b_baikaam) && !$.isEmptyVal(c_baikaam)){
						return "E20544";
					}
				}
			}
			// 61.本体売価	【画面】.C部分の「C総売価」より税抜き計算。『特売共通仕様書 本体売価算出方法』部分参照。
			// 62.値入
			//  ① （【画面】.C部分の「本体売価」－【画面】.特売事前行の「原価」）／【画面】.C部分の「本体売価」*100。
			//  ② 小数点以下3位切り捨て, 第2位まで求める。
			// 63.C売店
			//  ① 入力範囲：001～999。
			//  ② 新規の場合、初期値はNULLに設置する。
			//  ③ アンケート有の場合、編集不可。
			if(allcheck && id===$.id_inp.txt_rankno_add_c){

				var kbn = "";

				if (that.judgeRepType.frm3 || that.judgeRepType.frm2) {
					kbn = $.getInputboxValue($('[name='+$.id.rad_tkanplukbn+']').eq(0));	// that.grd_data['F57']
				}

				if (kbn!=='2') {
					if((that.judgeRepType.frm1) && that.judgeRepType.st && that.judgeRepType.toksp){
						// 16_1 【画面】C部分の「C総売価」が入力されたら、必須となる。入力がなっかたら、編集不可。
						var baika = $.getInputboxValue($('#'+$.id_inp.txt_c_baikaam+1));
						// E20391	C部分の「C総売価」の入力がない場合、C売店は入カできません。	 	0	 	E
						// E20392	C部分の「C総売価」が入力された場合、Ｃ売店は必須です。	 	0	 	E
						if($.isEmptyVal(baika) && !$.isEmptyVal(newValue, true)){
							return "E20391";
						}else if(!$.isEmptyVal(baika)  && $.isEmptyVal(newValue, true)){
							return "E20392";
						}
					}
				}
				if((that.judgeRepType.frm2||that.judgeRepType.frm3) && that.judgeRepType.st && that.judgeRepType.toksp){
					// 16_2,16_3 【画面】C部分の「C総売価」　OR　【画面】C総売価行の「100ｇ総売価」の入力がある場合必須。全てに入力がなかったら、編集不可。
					var baika1 = $.getInputboxValue($('#'+$.id_inp.txt_c_baikaam+1));
					var baika2 = $.getInputboxValue($('#'+$.id_inp.txt_c_baikaam+2));
					// E20311	C部分の「C総売価」あるいはC総売価行の「100ｇ総売価」に入力がなかったら	C売店は入力できません。	0	 	E
					// E20312	C部分の「C総売価」あるいはC総売価行の「100ｇ総売価」の入力がある場合は、Ｃ売店が必須です。	 	0	 	E
					if(($.isEmptyVal(baika1) && $.isEmptyVal(baika2)) && !$.isEmptyVal(newValue, true)){
						return "E20311";
					}else if(!($.isEmptyVal(baika1) && $.isEmptyVal(baika2)) && $.isEmptyVal(newValue, true)){
						return "E20312";
					}
				}
				if (kbn!=='2') {
					if((that.judgeRepType.frm5) && that.judgeRepType.st && that.judgeRepType.toksp){
						// 16_5 【画面】C部分の「C総売価」が入力されたら、必須となる。入力がなっかたら、編集不可。
						var baika = $.getInputboxValue($('#'+$.id_mei.kbn10656+"_c"));

						if (baika==='-1') {
							baika = '';
						}

						// E20391	C部分の「C総売価」の入力がない場合、C売店は入カできません。	 	0	 	E
						// E20392	C部分の「C総売価」が入力された場合、Ｃ売店は必須です。	 	0	 	E
						if($.isEmptyVal(baika) && !$.isEmptyVal(newValue, true)){
							return "E20391";
						}else if(!$.isEmptyVal(baika)  && $.isEmptyVal(newValue, true)){
							return "E20392";
						}
					}
					if((that.judgeRepType.frm4) && that.judgeRepType.st && that.judgeRepType.toksp){
						// 16_5 【画面】B部分の「B総売価」が入力されたら、必須となる。入力がなかったら、編集不可。
						var baika = $.getInputboxValue($('#'+$.id_inp.txt_c_baikaam+3));

						// E20391	C部分の「C総売価」の入力がない場合、C売店は入カできません。	 	0	 	E
						// E20392	C部分の「C総売価」が入力された場合、Ｃ売店は必須です。	 	0	 	E
						if($.isEmptyVal(baika) && !$.isEmptyVal(newValue, true)){
							return "E20391";
						}else if(!$.isEmptyVal(baika)  && $.isEmptyVal(newValue, true)){
							return "E20392";
						}
					}
				}
			}

			// 65.発注原売価適用しない
			// 66.PLU商品・定貫商品　／　不定貫商品
			// PLU商品・定貫商品をクリック時：
			//  ① レギュラー行、特売事前行、特売追加行部分の項目をデフォルト表示する。
			//  ② 不定貫部分のA総売価、B総売価、C総売価部分の項目をクリアする。
			// 不定貫商品をクリック時：
			//  ① レギュラー行、特売事前行、特売追加行部分の項目をクリアする。
			//  ② デフォルト表示なし。

			var chkKbn = "";

			if (that.judgeRepType.frm3 || that.judgeRepType.frm2) {
				chkKbn = $.getInputboxValue($('[name='+$.id.rad_tkanplukbn+']').eq(0));	// that.grd_data['F57']
			}
			if (chkKbn==='2') {

				// *** A総売価行 ***
				// 98.100g総売価
				//  ① 入力範囲：1～999,999。
				//  ② 新規の場合、初期値はNULLに設置する。
				//  ③ 【画面】.A総売価行の「100g総売価」>=【画面】.B総売価行の「100g総売価」>=【画面】.C総売価行の「100g総売価」。
				//  ④ 【画面】.A総売価行の「100g総売価」=【画面】.B総売価行の「100g総売価」=【画面】.C総売価行の「100g総売価」は不可。
				if(allcheck && id===$.id_inp.txt_a_baikaam+2 && !$.isEmptyVal(newValue)){
					var a_baikaam = id===$.id_inp.txt_a_baikaam+2 ? newValue: $.getInputboxValue($('#'+$.id_inp.txt_a_baikaam+2));
					var b_baikaam = id===$.id_inp.txt_b_baikaam+2 ? newValue: $.getInputboxValue($('#'+$.id_inp.txt_b_baikaam+2));
					var c_baikaam = id===$.id_inp.txt_c_baikaam+2 ? newValue: $.getInputboxValue($('#'+$.id_inp.txt_c_baikaam+2));
					// ③E20546	A総売価行の「100g総売価」 ≧ B総売価行の「100g総売価」 ≧ C総売価行の「100g総売価」	の条件で入力してください。	0	 	E
					if(!(a_baikaam*1 >= b_baikaam*1 && b_baikaam*1 >= c_baikaam*1)){
						return "E20546";
					}
					// ④E20547	A総売価行の「100g総売価」 = B総売価行の「100g総売価」 = C総売価行の「100g総売価」は入力できません。	 	0	 	E
					if(a_baikaam*1 == b_baikaam*1 && b_baikaam*1 == c_baikaam*1){
						return "E20547";
					}
				}
				// 99.1Kg原価
				//  ① 入力範囲：1～999,999.99。
				//  ② 新規の場合、初期値はNULLに設置する。
				// 100.1Kg総売価
				//  ① 入力範囲：1～999,999.99。
				//  ② 新規の場合、初期値はNULLに設置する。
				//  ③ 【画面】.A総売価行の「1kg総売価」>=【画面】.B総売価行の「1kg総売価」>=【画面】.C総売価行の「1kg総売価」。
				//  ④ 【画面】.A総売価行の「1kg総売価」=【画面】.B総売価行の「1kg総売価」=【画面】.C総売価行の「1kg総売価」は不可。
				if(allcheck && id===$.id_inp.txt_a_genkaam_1kg && !$.isEmptyVal(newValue)){
					var a_baikaam = id===$.id_inp.txt_a_genkaam_1kg ? newValue: $.getInputboxValue($('#'+$.id_inp.txt_a_genkaam_1kg));
					var b_baikaam = id===$.id_inp.txt_b_genkaam_1kg ? newValue: $.getInputboxValue($('#'+$.id_inp.txt_b_genkaam_1kg));
					var c_baikaam = id===$.id_inp.txt_c_genkaam_1kg ? newValue: $.getInputboxValue($('#'+$.id_inp.txt_c_genkaam_1kg));
					// ③E20556	A総売価行の「1Kg総価」 ≧ B総売価行の「1Kg総価」 ≧ C総売価行の「1Kg総価」	の条件で入力してください。	0	 	E
					if(!(a_baikaam*1 >= b_baikaam*1 && b_baikaam*1 >= c_baikaam*1)){
						return "E20556";
					}
					// ④E20557	A総売価行の「1Kg総価」 = B総売価行の「1Kg総価」 = C総売価行の「1Kg総価」は入力できません。	 	0	 	E
					if(a_baikaam*1 == b_baikaam*1 && b_baikaam*1 == c_baikaam*1){
						return "E20557";
					}
				}
				// 101.P原価
				//  ① 入力範囲：1～999,999.99。
				//  ② 新規の場合、初期値はNULLに設置する。
				if(allcheck && id===$.id_inp.txt_genkaam_pack){
					if((that.judgeRepType.frm2||that.judgeRepType.frm3) && that.judgeRepType.st && (that.judgeRepType.toktg_h||that.judgeRepType.toksp)){
						// 16_2,16_3【画面】.「納入期間」入力時、必須。
						var stdt = $.getInputboxValue($('#'+$.id_inp.txt_nnstdt));
						var eddt = $.getInputboxValue($('#'+$.id_inp.txt_nneddt));
						// E20313	「納入期間」入力時、P原価は必須です。	 	0	 	E
						if(!($.isEmptyVal(stdt) && $.isEmptyVal(stdt)) && $.isEmptyVal(newValue, true)){
							return "E20313";
						}
					}
				}
				// 102.P総売価
				//  ① 入力範囲：1～999,999。
				//  ② 新規の場合、初期値はNULLに設置する。
				//  ③ 【画面】.A総売価行の「P総売価」>=【画面】.B総売価行の「P総売価」>=【画面】.C総売価行の「P総売価」。
				//  ④ 【画面】.A総売価行の「P総売価」=【画面】.B総売価行の「P総売価」=【画面】.C総売価行の「P総売価」は不可。
				if(allcheck && id===$.id_inp.txt_a_baikaam_pack){
					var a_baikaam = id===$.id_inp.txt_a_baikaam_pack ? newValue: $.getInputboxValue($('#'+$.id_inp.txt_a_baikaam_pack));
					var b_baikaam = id===$.id_inp.txt_b_baikaam_pack ? newValue: $.getInputboxValue($('#'+$.id_inp.txt_b_baikaam_pack));
					var c_baikaam = id===$.id_inp.txt_c_baikaam_pack ? newValue: $.getInputboxValue($('#'+$.id_inp.txt_c_baikaam_pack));
					// ③E20559	A総売価行の「P総価」 ≧ B総売価行の「P総価」 ≧ C総売価行の「P総価」	の条件で入力してください。	0	 	E
					if(!$.isEmptyVal(newValue) && !(a_baikaam*1 >= b_baikaam*1 && b_baikaam*1 >= c_baikaam*1)){
						return "E20559";
					}
					// ④E20560	A総売価行の「P総価」 = B総売価行の「P総価」 = C総売価行の「P総価」は入力できません。	 	0	 	E
					if(!$.isEmptyVal(newValue) && a_baikaam*1 == b_baikaam*1 && b_baikaam*1 == c_baikaam*1){
						return "E20560";
					}
					if((that.judgeRepType.frm2||that.judgeRepType.frm3) && that.judgeRepType.st && (that.judgeRepType.toktg_h||that.judgeRepType.toksp)){
						// 16_2,16_3 【画面】.「納入期間」入力時、必須。
						var stdt = $.getInputboxValue($('#'+$.id_inp.txt_nnstdt));
						var eddt = $.getInputboxValue($('#'+$.id_inp.txt_nneddt));
						// E20314	「納入期間」入力時、P総売価は必須です。	 	0	 	E
						if(!($.isEmptyVal(stdt) && $.isEmptyVal(stdt)) && $.isEmptyVal(newValue, true)){
							return "E20314";
						}
					}
				}
				// 103.入数
				//  ① 入力範囲：1～999。
				//  ② 新規の場合、初期値はNULLに設置する。
				if(allcheck && id===$.id_inp.txt_irisu+"2"){
					if((that.judgeRepType.frm2||that.judgeRepType.frm3) && that.judgeRepType.st && (that.judgeRepType.toktg_h||that.judgeRepType.toksp)){
						// 16_2,16_3 【画面】.「納入期間」入力時、必須。
						var stdt = $.getInputboxValue($('#'+$.id_inp.txt_nnstdt));
						var eddt = $.getInputboxValue($('#'+$.id_inp.txt_nneddt));
						// E20315	「納入期間」入力時、入数は必須です。	 	0	 	E
						if(!($.isEmptyVal(stdt) && $.isEmptyVal(stdt)) && $.isEmptyVal(newValue, true)){
							return "E20315";
						}
					}
				}

				// *** B総売価行 ***
				// 105.100g総売価
				// ① 入力範囲：1～999,999。
				// ② 新規の場合、初期値はNULLに設置する。
				// ③ 【画面】.A総売価行の「100g総売価」>=【画面】.B総売価行の「100g総売価」>=【画面】.C総売価行の「100g総売価」。
				// ④ 【画面】.A総売価行の「100g総売価」=【画面】.B総売価行の「100g総売価」=【画面】.C総売価行の「100g総売価」は不可。
				if(allcheck && id===$.id_inp.txt_b_baikaam+2 && !$.isEmptyVal(newValue)){
					var a_baikaam = id===$.id_inp.txt_a_baikaam+2 ? newValue: $.getInputboxValue($('#'+$.id_inp.txt_a_baikaam+2));
					var b_baikaam = id===$.id_inp.txt_b_baikaam+2 ? newValue: $.getInputboxValue($('#'+$.id_inp.txt_b_baikaam+2));
					var c_baikaam = id===$.id_inp.txt_c_baikaam+2 ? newValue: $.getInputboxValue($('#'+$.id_inp.txt_c_baikaam+2));
					// ③E20546	A総売価行の「100g総売価」 ≧ B総売価行の「100g総売価」 ≧ C総売価行の「100g総売価」	の条件で入力してください。	0	 	E
					if(!(a_baikaam*1 >= b_baikaam*1 && b_baikaam*1 >= c_baikaam*1)){
						return "E20546";
					}
					// ④E20547	A総売価行の「100g総売価」 = B総売価行の「100g総売価」 = C総売価行の「100g総売価」は入力できません。	 	0	 	E
					if(a_baikaam*1 == b_baikaam*1 && b_baikaam*1 == c_baikaam*1){
						return "E20547";
					}
				}
				// 106.1Kg原価	表示・入力不可
				// 107.1Kg総売価
				//  ① 入力範囲：1～999,999.99。
				//  ② 新規の場合、初期値はNULLに設置する。
				//  ③ 【画面】.A総売価行の「1kg総売価」>=【画面】.B総売価行の「1kg総売価」>=【画面】.C総売価行の「1kg総売価」。
				//  ④ 【画面】.A総売価行の「1kg総売価」=【画面】.B総売価行の「1kg総売価」=【画面】.C総売価行の「1kg総売価」は不可。
				if(allcheck && id===$.id_inp.txt_b_genkaam_1kg){
					var a_baikaam = id===$.id_inp.txt_a_genkaam_1kg ? newValue: $.getInputboxValue($('#'+$.id_inp.txt_a_genkaam_1kg));
					var b_baikaam = id===$.id_inp.txt_b_genkaam_1kg ? newValue: $.getInputboxValue($('#'+$.id_inp.txt_b_genkaam_1kg));
					var c_baikaam = id===$.id_inp.txt_c_genkaam_1kg ? newValue: $.getInputboxValue($('#'+$.id_inp.txt_c_genkaam_1kg));
					// ③E20556	A総売価行の「1Kg総価」 ≧ B総売価行の「1Kg総価」 ≧ C総売価行の「1Kg総価」	の条件で入力してください。	0	 	E
					if(!$.isEmptyVal(newValue) && !(a_baikaam*1 >= b_baikaam*1 && b_baikaam*1 >= c_baikaam*1)){
						return "E20556";
					}
					// ④E20557	A総売価行の「1Kg総価」 = B総売価行の「1Kg総価」 = C総売価行の「1Kg総価」は入力できません。	 	0	 	E
					if(!$.isEmptyVal(newValue) && a_baikaam*1 == b_baikaam*1 && b_baikaam*1 == c_baikaam*1){
						return "E20557";
					}
					if((that.judgeRepType.frm2||that.judgeRepType.frm3) && that.judgeRepType.st && (that.judgeRepType.toktg_h||that.judgeRepType.toksp)){
						// 16_2,16_3 ① 【画面】.「納入期間」入力時で、【画面】.B総売価行の「100g総売価」に入力がある場合、必須。② それ以外入力不可。
						var stdt = $.getInputboxValue($('#'+$.id_inp.txt_nnstdt));
						var eddt = $.getInputboxValue($('#'+$.id_inp.txt_nneddt));
						var baika= $.getInputboxValue($('#'+$.id_inp.txt_b_baikaam+2));
						if(!($.isEmptyVal(stdt) && $.isEmptyVal(stdt)) && !$.isEmptyVal(baika)){
							// E20320	「納入期間」入力時、B総売価行の「100g総売価」に入力がある場合、1Kg総売価は必須です。	 	0	 	E
							if($.isEmptyVal(newValue, true)){
								return "E20320";
							}
						}else{
							// E20316	「納入期間」入力時、B総売価行の「100g総売価」に入力時以外1Kg総売価は入力できません。	 	0	 	E
							if(!$.isEmptyVal(newValue, true)){
								return "E20316";
							}
						}
					}
				}
				// 108.P原価	表示・入力不可
				// 109.P総売価
				//  ① 入力範囲：1～999,999。
				//  ② 新規の場合、初期値はNULLに設置する。
				//  ③ 【画面】.A総売価行の「P総売価」>=【画面】.B総売価行の「P総売価」>=【画面】.C総売価行の「P総売価」。
				//  ④ 【画面】.A総売価行の「P総売価」=【画面】.B総売価行の「P総売価」=【画面】.C総売価行の「P総売価」は不可。
				if(allcheck && id===$.id_inp.txt_b_baikaam_pack){
					var a_baikaam = id===$.id_inp.txt_a_baikaam_pack ? newValue: $.getInputboxValue($('#'+$.id_inp.txt_a_baikaam_pack));
					var b_baikaam = id===$.id_inp.txt_b_baikaam_pack ? newValue: $.getInputboxValue($('#'+$.id_inp.txt_b_baikaam_pack));
					var c_baikaam = id===$.id_inp.txt_c_baikaam_pack ? newValue: $.getInputboxValue($('#'+$.id_inp.txt_c_baikaam_pack));
					// ③E20559	A総売価行の「P総価」 ≧ B総売価行の「P総価」 ≧ C総売価行の「P総価」	の条件で入力してください。	0	 	E
					if(!$.isEmptyVal(newValue) && !(a_baikaam*1 >= b_baikaam*1 && b_baikaam*1 >= c_baikaam*1)){
						return "E20559";
					}
					// ④E20560	A総売価行の「P総価」 = B総売価行の「P総価」 = C総売価行の「P総価」は入力できません。	 	0	 	E
					if(!$.isEmptyVal(newValue) && a_baikaam*1 == b_baikaam*1 && b_baikaam*1 == c_baikaam*1){
						return "E20560";
					}
					if((that.judgeRepType.frm2||that.judgeRepType.frm3) && that.judgeRepType.st && (that.judgeRepType.toktg_h||that.judgeRepType.toksp)){
						// 16_2,16_3 ① 【画面】.「納入期間」入力時で、【画面】.B総売価行の「100g総売価」に入力がある場合、必須。② それ以外入力不可。
						var stdt = $.getInputboxValue($('#'+$.id_inp.txt_nnstdt));
						var eddt = $.getInputboxValue($('#'+$.id_inp.txt_nneddt));
						var baika= $.getInputboxValue($('#'+$.id_inp.txt_b_baikaam+2));
						if(!($.isEmptyVal(stdt) && $.isEmptyVal(stdt)) && !$.isEmptyVal(baika)){
							// E20321	「納入期間」入力時、B総売価行の「100g総売価」に入力がある場合、P総売価は必須です。	 	0	 	E
							if($.isEmptyVal(newValue, true)){
								return "E20321";
							}
						}else{
							// E20317	「納入期間」入力時、B総売価行の「100g総売価」に入力時以外P総売価は入力できません。	 	0	 	E
							if(!$.isEmptyVal(newValue, true)){
								return "E20317";
							}
						}
					}
				}
				// 110.入数	表示・入力不可

				// *** C総売価行 ***
				// 112.100g総売価
				//  ① 入力範囲：1～999,999。
				//  ② 新規の場合、初期値はNULLに設置する。
				//  ③ 【画面】.A総売価行の「100g総売価」>=【画面】.B総売価行の「100g総売価」>=【画面】.C総売価行の「100g総売価」。
				//  ④ 【画面】.A総売価行の「100g総売価」=【画面】.B総売価行の「100g総売価」=【画面】.C総売価行の「100g総売価」は不可。
				//  ⑤ 【画面】.B総売価行の「100g総売価」を入力しないと【画面】.C総売価行の「100g総売価」を入力できない。
				if(allcheck && id===$.id_inp.txt_c_baikaam+2 && !$.isEmptyVal(newValue)){
					var a_baikaam = id===$.id_inp.txt_a_baikaam+2 ? newValue: $.getInputboxValue($('#'+$.id_inp.txt_a_baikaam+2));
					var b_baikaam = id===$.id_inp.txt_b_baikaam+2 ? newValue: $.getInputboxValue($('#'+$.id_inp.txt_b_baikaam+2));
					var c_baikaam = id===$.id_inp.txt_c_baikaam+2 ? newValue: $.getInputboxValue($('#'+$.id_inp.txt_c_baikaam+2));
					// ③E20546	A総売価行の「100g総売価」 ≧ B総売価行の「100g総売価」 ≧ C総売価行の「100g総売価」	の条件で入力してください。	0	 	E
					if(!(a_baikaam*1 >= b_baikaam*1 && b_baikaam*1 >= c_baikaam*1)){
						return "E20546";
					}
					// ④E20547	A総売価行の「100g総売価」 = B総売価行の「100g総売価」 = C総売価行の「100g総売価」は入力できません。	 	0	 	E
					if(a_baikaam*1 == b_baikaam*1 && b_baikaam*1 == c_baikaam*1){
						return "E20547";
					}
				}
				// 113.1Kg原価	表示・入力不可
				// 114.1Kg総売価
				// ① 入力範囲：1～999,999.99。
				// ② 新規の場合、初期値はNULLに設置する。
				// ③ 【画面】.A総売価行の「1kg総売価」>=【画面】.B総売価行の「1kg総売価」>=【画面】.C総売価行の「1kg総売価」
				// ④ 【画面】.A総売価行の「1kg総売価」=【画面】.B総売価行の「1kg総売価」=【画面】.C総売価行の「1kg総売価」は不可
				// ⑤ 【画面】.B総売価行の「1kg総売価」を入力しないと【画面】.C総売価行の「1kg総売価」を入力できない。
				if(allcheck && id===$.id_inp.txt_c_genkaam_1kg && !$.isEmptyVal(newValue)){
					var a_baikaam = id===$.id_inp.txt_a_genkaam_1kg ? newValue: $.getInputboxValue($('#'+$.id_inp.txt_a_genkaam_1kg));
					var b_baikaam = id===$.id_inp.txt_b_genkaam_1kg ? newValue: $.getInputboxValue($('#'+$.id_inp.txt_b_genkaam_1kg));
					var c_baikaam = id===$.id_inp.txt_c_genkaam_1kg ? newValue: $.getInputboxValue($('#'+$.id_inp.txt_c_genkaam_1kg));
					// ③E20556	A総売価行の「1Kg総価」 ≧ B総売価行の「1Kg総価」 ≧ C総売価行の「1Kg総価」	の条件で入力してください。	0	 	E
					if(!(a_baikaam*1 >= b_baikaam*1 && b_baikaam*1 >= c_baikaam*1)){
						return "E20556";
					}
					// ④E20557	A総売価行の「1Kg総価」 = B総売価行の「1Kg総価」 = C総売価行の「1Kg総価」は入力できません。	 	0	 	E
					if(a_baikaam*1 == b_baikaam*1 && b_baikaam*1 == c_baikaam*1){
						return "E20557";
					}
					if((that.judgeRepType.frm2||that.judgeRepType.frm3) && that.judgeRepType.st && (that.judgeRepType.toktg_h||that.judgeRepType.toksp)){
						// 16_2,16_3 ① 【① 【画面】.「納入期間」入力時で、【画面】.C総売価行の「100g総売価」に入力がある場合、必須。② それ以外入力不可。
						var stdt = $.getInputboxValue($('#'+$.id_inp.txt_nnstdt));
						var eddt = $.getInputboxValue($('#'+$.id_inp.txt_nneddt));
						var baika= $.getInputboxValue($('#'+$.id_inp.txt_c_baikaam+2));
						if(!($.isEmptyVal(stdt) && $.isEmptyVal(stdt)) && !$.isEmptyVal(baika)){
							// E20322	「納入期間」入力時、C総売価行の「100g総売価」に入力がある場合、1Kg総売価は必須です。	 	0	 	E
							if($.isEmptyVal(newValue, true)){
								return "E20322";
							}
						}else{
							// E20318	「納入期間」入力時、C総売価行の「100g総売価」に入力時以外1Kg総売価は入力できません。	 	0	 	E
							if(!$.isEmptyVal(newValue, true)){
								return "E20318";
							}
						}
					}
				}
				// 115.P原価	表示・入力不可
				// 116.P総売価
				// ① 入力範囲：1～999,999。
				// ② 新規の場合、初期値はNULLに設置する。
				// ③ 【画面】.A総売価行の「P総売価」>=【画面】.B総売価行の「P総売価」>=【画面】.C総売価行の「P総売価」
				// ④ 【画面】.A総売価行の「P総売価」=【画面】.B総売価行の「P総売価」=【画面】.C総売価行の「P総売価」は不可
				// ⑤ 【画面】.B総売価行の「P総売価」を入力しないと【画面】.C総売価行の「P総売価」を入力できない。
				if(allcheck && id===$.id_inp.txt_c_baikaam_pack && !$.isEmptyVal(newValue)){
					var a_baikaam = id===$.id_inp.txt_a_baikaam_pack ? newValue: $.getInputboxValue($('#'+$.id_inp.txt_a_baikaam_pack));
					var b_baikaam = id===$.id_inp.txt_b_baikaam_pack ? newValue: $.getInputboxValue($('#'+$.id_inp.txt_b_baikaam_pack));
					var c_baikaam = id===$.id_inp.txt_c_baikaam_pack ? newValue: $.getInputboxValue($('#'+$.id_inp.txt_c_baikaam_pack));
					// ③E20559	A総売価行の「P総価」 ≧ B総売価行の「P総価」 ≧ C総売価行の「P総価」	の条件で入力してください。	0	 	E
					if(!(a_baikaam*1 >= b_baikaam*1 && b_baikaam*1 >= c_baikaam*1)){
						return "E20559";
					}
					// ④E20560	A総売価行の「P総価」 = B総売価行の「P総価」 = C総売価行の「P総価」は入力できません。	 	0	 	E
					if(a_baikaam*1 == b_baikaam*1 && b_baikaam*1 == c_baikaam*1){
						return "E20560";
					}
					if((that.judgeRepType.frm2||that.judgeRepType.frm3) && that.judgeRepType.st && (that.judgeRepType.toktg_h||that.judgeRepType.toksp)){
						// 16_2,16_3 ① 【画面】.「納入期間」入力時で、【画面】.C総売価行の「100g総売価」に入力がある場合、必須。② それ以外入力不可。
						var stdt = $.getInputboxValue($('#'+$.id_inp.txt_nnstdt));
						var eddt = $.getInputboxValue($('#'+$.id_inp.txt_nneddt));
						var baika= $.getInputboxValue($('#'+$.id_inp.txt_c_baikaam+2));
						if(!($.isEmptyVal(stdt) && $.isEmptyVal(stdt)) && !$.isEmptyVal(baika)){
							// E20323	「納入期間」入力時、【画面】.C総売価行の「100g総売価」に入力がある場合、P総売価は必須です。	 	0	 	E
							if($.isEmptyVal(newValue, true)){
								return "E20323";
							}
						}else{
							// E20319	「納入期間」入力時、C総売価行の「100g総売価」に入力時以外C総売価は入力できません。	 	0	 	E
							if(!$.isEmptyVal(newValue, true)){
								return "E20319";
							}
						}
					}
				}
			}

			// 117.入数	表示・入力不可

			// *** 総売価部分 ***
			// 16_5
			// ① リスト内容：名称マスタの割引率区分（名称コード区分＝10656）を取得する。　
			// ② 新規の場合、初期値は空白行に設置する。
			// ③ 商品コード＋販売開始日で冷凍食品をチェック。『特売共通仕様書 全品割引商品登録時のチェック』部分参照。

			// 119.A総売価
			//  ① 入力範囲：1～999,999。
			//  ②TG016_4：【画面】.総売価部分の「A総売価」>=「B総売価」>=「C総売価」	→ A総売価1で実施
			//    TG016_5：【画面】.総売価部分の「A総売価」<=「B総売価」<=「C総売価」
			//  ③ 【画面】.総売価部分の「A総売価」=【画面】.総売価部分の「B総売価」=【画面】.総売価部分の「C総売価」は不可。
			//  ④ 新規初期値：NULL。
			//  ⑤ TG016_5の場合は名称マスタより名称コード区分=10656でリストを作成する。
			if(allcheck && id===$.id_mei.kbn10656+"_a" && !$.isEmptyVal(newValue) &&
					(that.judgeRepType.frm4 || that.judgeRepType.frm5)
			){
				var a_baikaam = id===$.id_mei.kbn10656+"_a" ? newValue: $.getInputboxValue($('#'+$.id_mei.kbn10656+"_a"));
				var b_baikaam = id===$.id_mei.kbn10656+"_b" ? newValue: $.getInputboxValue($('#'+$.id_mei.kbn10656+"_b"));
				var c_baikaam = id===$.id_mei.kbn10656+"_c" ? newValue: $.getInputboxValue($('#'+$.id_mei.kbn10656+"_c"));

				if (that.judgeRepType.frm4) {
					a_baikaam = id===$.id_inp.txt_a_baikaam+3 ? newValue: $.getInputboxValue($('#'+$.id_inp.txt_a_baikaam+3));
					b_baikaam = id===$.id_inp.txt_b_baikaam+3 ? newValue: $.getInputboxValue($('#'+$.id_inp.txt_b_baikaam+3));
					c_baikaam = id===$.id_inp.txt_c_baikaam+3 ? newValue: $.getInputboxValue($('#'+$.id_inp.txt_c_baikaam+3));
				}

				if (that.judgeRepType.frm5 && a_baikaam==='-1') {
					a_baikaam = '';
				}

				if (that.judgeRepType.frm5 && b_baikaam==='-1') {
					b_baikaam = '';
				}

				if (that.judgeRepType.frm5 && c_baikaam==='-1') {
					c_baikaam = '';
				}

				if (!$.isEmptyVal(a_baikaam,true) && !$.isEmptyVal(b_baikaam,true) && !$.isEmptyVal(c_baikaam,true)) {
					// ②E20542	総売価部分の「A総売価」 ≦ 総売価部分の「B総売価」 ≦  総売価部分の「C総売価」	の条件で入力してください。	0	 	E
					if(that.judgeRepType.frm5 && !(a_baikaam*1 <= b_baikaam*1 && b_baikaam*1 <= c_baikaam*1)){
						return "E20542";
					}

					if(that.judgeRepType.frm4 && !(c_baikaam*1 <= b_baikaam*1 && b_baikaam*1 <= a_baikaam*1)){
						return "E20050";
					}

					// ③E20053	A総売価＝B総売価＝C総売価は不可です。	 	0	 	E
					if(a_baikaam*1 == b_baikaam*1 && b_baikaam*1 == c_baikaam*1){
						return "E20053";
					}
				}

				// 総売価部分 16_5 ③
				if(that.judgeRepType.frm5){
					if (newValue === '-1') {
						return "E20030,A総売価";
					} else {
						// ③E20220	冷凍食品企画に登録されていません。	 	0	 	E
						var chk_cnt = $.getInputboxData(that.name, $.id.action_check, "TOKRS_KKK", that.getInputboxParams(that, "TOKRS_KKK", newValue, "CNT"));
						if(chk_cnt===""||chk_cnt==="0"){
							return "E20220";
						}
					}
				}
			}
			// 120.B総売価
			//  ① 入力範囲：1～999,999。
			//  ②TG016_4：【画面】.総売価部分の「A総売価」>=「B総売価」>=「C総売価」	→ A総売価1で実施
			//  　TG016_5：【画面】.総売価部分の「A総売価」<=「B総売価」<=「C総売価」
			//  ③ 【画面】.総売価部分の「A総売価」=【画面】.総売価部分の「B総売価」=【画面】.総売価部分の「C総売価」は不可
			//  ④ 新規の場合、初期値はNULLに設置する。
			//  ⑤ TG016_5の場合は名称マスタより名称コード区分=10656でリストを作成する。
			if(allcheck && id===$.id_mei.kbn10656+"_b" && !$.isEmptyVal(newValue) &&
					(that.judgeRepType.frm4 || that.judgeRepType.frm5)
			){
				var a_baikaam = id===$.id_mei.kbn10656+"_a" ? newValue: $.getInputboxValue($('#'+$.id_mei.kbn10656+"_a"));
				var b_baikaam = id===$.id_mei.kbn10656+"_b" ? newValue: $.getInputboxValue($('#'+$.id_mei.kbn10656+"_b"));
				var c_baikaam = id===$.id_mei.kbn10656+"_c" ? newValue: $.getInputboxValue($('#'+$.id_mei.kbn10656+"_c"));

				if (that.judgeRepType.frm4) {
					a_baikaam = id===$.id_inp.txt_a_baikaam+3 ? newValue: $.getInputboxValue($('#'+$.id_inp.txt_a_baikaam+3));
					b_baikaam = id===$.id_inp.txt_b_baikaam+3 ? newValue: $.getInputboxValue($('#'+$.id_inp.txt_b_baikaam+3));
					c_baikaam = id===$.id_inp.txt_c_baikaam+3 ? newValue: $.getInputboxValue($('#'+$.id_inp.txt_c_baikaam+3));
				}

				if (that.judgeRepType.frm5 && a_baikaam==='-1') {
					a_baikaam = '';
				}

				if (that.judgeRepType.frm5 && b_baikaam==='-1') {
					b_baikaam = '';
				}

				if (that.judgeRepType.frm5 && c_baikaam==='-1') {
					c_baikaam = '';
				}

				if (!$.isEmptyVal(a_baikaam,true) && !$.isEmptyVal(b_baikaam,true) && !$.isEmptyVal(c_baikaam,true)) {
					// ②E20542	総売価部分の「A総売価」 ≦ 総売価部分の「B総売価」 ≦  総売価部分の「C総売価」	の条件で入力してください。	0	 	E
					if(that.judgeRepType.frm5 && !(a_baikaam*1 <= b_baikaam*1 && b_baikaam*1 <= c_baikaam*1)){
						return "E20542";
					}

					if(that.judgeRepType.frm4 && !(c_baikaam*1 <= b_baikaam*1 && b_baikaam*1 <= a_baikaam*1)){
						return "E20050";
					}

					// ③E20053	A総売価＝B総売価＝C総売価は不可です。	 	0	 	E
					if(a_baikaam*1 == b_baikaam*1 && b_baikaam*1 == c_baikaam*1){
						return "E20053";
					}
				}
				// 総売価部分 16_5 ③
				if(that.judgeRepType.frm5 && newValue !== '-1'){
					// ③E20220	冷凍食品企画に登録されていません。	 	0	 	E
					var chk_cnt = $.getInputboxData(that.name, $.id.action_check, "TOKRS_KKK", that.getInputboxParams(that, "TOKRS_KKK", newValue, "CNT"));
					if(chk_cnt===""||chk_cnt==="0"){
						return "E20220";
					}
				}
			}
			// 121.C総売価
			//  ① 入力範囲：1～999,999。
			//  ② TG016_4：【画面】.総売価部分の「A総売価」>=「B総売価」>=「C総売価」	→ A総売価1で実施
			//   　TG016_5：【画面】.総売価部分の「A総売価」<=「B総売価」<=「C総売価」
			//  ③ 【画面】.総売価部分の「A総売価」=【画面】.総売価部分の「B総売価」=【画面】.総売価部分の「C総売価」は不可
			//  ④ 【画面】.総売価部分の[B総売価]が入力されいている場合のみ入力可
			//  ⑤ 新規の場合、初期値はNULLに設置する。
			//  ⑥ TG016_5の場合は名称マスタより名称コード区分=10656でリストを作成する。
			if(allcheck && id===$.id_mei.kbn10656+"_c" && !$.isEmptyVal(newValue) &&
					(that.judgeRepType.frm4 || that.judgeRepType.frm5)
			){
				var a_baikaam = id===$.id_mei.kbn10656+"_a" ? newValue: $.getInputboxValue($('#'+$.id_mei.kbn10656+"_a"));
				var b_baikaam = id===$.id_mei.kbn10656+"_b" ? newValue: $.getInputboxValue($('#'+$.id_mei.kbn10656+"_b"));
				var c_baikaam = id===$.id_mei.kbn10656+"_c" ? newValue: $.getInputboxValue($('#'+$.id_mei.kbn10656+"_c"));

				if (that.judgeRepType.frm4) {
					a_baikaam = id===$.id_inp.txt_a_baikaam+3 ? newValue: $.getInputboxValue($('#'+$.id_inp.txt_a_baikaam+3));
					b_baikaam = id===$.id_inp.txt_b_baikaam+3 ? newValue: $.getInputboxValue($('#'+$.id_inp.txt_b_baikaam+3));
					c_baikaam = id===$.id_inp.txt_c_baikaam+3 ? newValue: $.getInputboxValue($('#'+$.id_inp.txt_c_baikaam+3));
				}

				if (that.judgeRepType.frm5 && a_baikaam==='-1') {
					a_baikaam = '';
				}

				if (that.judgeRepType.frm5 && b_baikaam==='-1') {
					b_baikaam = '';
				}

				if (that.judgeRepType.frm5 && c_baikaam==='-1') {
					c_baikaam = '';
				}

				if (!$.isEmptyVal(a_baikaam,true) && !$.isEmptyVal(b_baikaam,true) && !$.isEmptyVal(c_baikaam,true)) {
					// ②E20542	総売価部分の「A総売価」 ≦ 総売価部分の「B総売価」 ≦  総売価部分の「C総売価」	の条件で入力してください。	0	 	E
					if(that.judgeRepType.frm5 && !(a_baikaam*1 <= b_baikaam*1 && b_baikaam*1 <= c_baikaam*1)){
						return "E20542";
					}

					if(that.judgeRepType.frm4 && !(c_baikaam*1 <= b_baikaam*1 && b_baikaam*1 <= a_baikaam*1)){
						return "E20050";
					}

					// ③E20053	A総売価＝B総売価＝C総売価は不可です。	 	0	 	E
					if(a_baikaam*1 == b_baikaam*1 && b_baikaam*1 == c_baikaam*1){
						return "E20053";
					}
				}
				// ④E20544	総売価部分の「B総売価」が入力されいている場合のみ「C総売価」は入力できます。	 	0	 	E
				if($.isEmptyVal(b_baikaam) && !$.isEmptyVal(c_baikaam)){
					return "E20544";
				}
				// 総売価部分 16_5 ③
				if(that.judgeRepType.frm5 && newValue !== '-1'){
					// ③E20220	冷凍食品企画に登録されていません。	 	0	 	E
					var chk_cnt = $.getInputboxData(that.name, $.id.action_check, "TOKRS_KKK", that.getInputboxParams(that, "TOKRS_KKK", newValue, "CNT"));
					if(chk_cnt===""||chk_cnt==="0"){
						return "E20220";
					}
				}
			}

			// 【販売情報部分】
			// 123.産地
			//  ① 全角20文字。
			//  ② 新規の場合、初期値はNULLに設置する。
			// 124.メーカー名
			//  ① 新規の初期値：商品マスタ.メーカーコードでメーカーマスタ.メーカー名（漢字）を取得する。メーカー名（漢字）の前28桁を使う。
			//  ② 全角14文字。
			//  ③ 【画面】.「商品コード」を入力したタイミングで上書き表示。
			// 125.POP名称
			//  ① 新規の初期値：商品マスタ.POP名称を取得する。
			//  ② 全角20文字。
			//  ③ 【画面】.「商品コード」を入力したタイミングで上書き表示。
			if(allcheck && id===$.id_inp.txt_popkn && !$.isEmptyVal(newValue)){
				if((that.judgeRepType.frm2||that.judgeRepType.frm3) && that.judgeRepType.st && (that.judgeRepType.toksp)){
					// 16_2,16_3【画面】.「販売期間」入力があれば、必須。
					var stdt = $.getInputboxValue($('#'+$.id_inp.txt_hbstdt));
					var eddt = $.getInputboxValue($('#'+$.id_inp.txt_hbeddt));
					// E20324	「販売期間」入力時、ＰＯＰ名称は必須です。	 	0	 	E
					if(!($.isEmptyVal(stdt) && $.isEmptyVal(stdt)) && $.isEmptyVal(newValue)){
						return "E20324";
					}
				}
			}
			// 126.規格
			//  ① 新規の初期値：商品マスタ.規格を取得する。
			//  ② 全角23文字。
			//  ③ 【画面】.「商品コード」を入力したタイミングで上書き表示。
			// 127.制限部分
			// 128.先着人数
			//  ① 入力範囲：1～99,999。
			//  ② 新規の場合、初期値はNULLに設置する。
			// 129.限定表現
			//  ① 全角10文字。
			//  ② 新規画面：
			//  　名称マスタ（名称コード区分=10670）よりリストを作成。初期値は空白行に設置
			//  　リスト選択＋手入力も可能。DBへは画面上選択したテキストを保持
			//  ③ 変更画面：
			//  　名称マスタ（名称コード区分=10670）と全店特売（アンケート有/無）_商品.制限_限定表現よりリストを作成。初期値はDB内容に設置する
			//  　リスト選択＋手入力も可能。DBへは画面上選択したテキストを保持
			if(allcheck && id===$.id_mei.kbn10670 && !$.isEmptyVal(newValue)){
				// ①E20479	限定表現のは全角10文字以内で入力してください。	 	0	 	E
				// ※直接入力時チェック
				var selvalue = $.getInputboxText($('#'+$.id_mei.kbn10670));
				if (isNaN(newValue)) {
					if(!(newValue.length <= 10 && checkFullChar(newValue))){
						return "E20479";
					}
				} else {
					newValue = selvalue.replace(newValue,'');
					newValue = newValue.replace('-','');
				}
				that.selGentei = newValue.trim();
			}
			// 130.一人
			//  ① 入力範囲：1～999。
			//  ② 新規の場合、初期値はNULLに設置する。
			// 131.単位
			//  ① 全角5文字。
			//  ② 【画面】.制限部分の「一人」が入力された場合は必須。入力がなかったら入力不可。
			//  ③ 新規画面：
			//  　名称マスタ（名称コード区分=10671）よりリストを作成。初期値は空白行に設置
			//  　リスト選択＋手入力も可能。DBへは画面上選択したテキストを保持
			//  ④ 変更画面：
			//  　名称マスタ（名称コード区分=10671）と全店特売（アンケート有/無）_商品.制限_一人当り個数単位よりリストを作成。初期値はDB内容に設置する
			//  　リスト選択＋手入力も可能。DBへは画面上選択したテキストを保持
			if(allcheck && id===$.id_mei.kbn10671){
				// ①E20480	単位は全角5文字以内で入力してください。	 	0	 	E
				// ※直接入力時チェック
				var selvalue = $.getInputboxText($('#'+$.id_mei.kbn10671));

				if (isNaN(newValue)) {
					if(!(newValue.length <= 5 && checkFullChar(newValue))){
						return "E20480";
					}
				} else {
					newValue = selvalue.replace(newValue,'');
					newValue = newValue.replace('-','');
				}

				that.selTni = newValue.trim();

				var segn_1kosu = $.getInputboxValue($('#'+$.id_inp.txt_segn_1kosu));
				// ② E20483	「一人」が入力された場合は「単位」は必須です。	 	0	 	E
				// ② E20349	制限部分の「一人」が入力がなかったら単位入力できません。	 	0	 	E
				if ($.isEmptyVal(that.selTni) && !$.isEmptyVal(segn_1kosu)) {
					return "E20483";
				} else if (!$.isEmptyVal(that.selTni) && $.isEmptyVal(segn_1kosu)) {
					return "E20349";
				}
			}
			// 132.PLU配信しない
			// 【画面】.「販売期間」に入力があれば、デフォルト未チェック状態に設置する。入力がなかったら、デフォルトチェック状態に設置し、編集不可。TODO:???
			if(id===$.id.chk_plusndflg){
				if ($.getInputboxValue($('#'+$.id_inp.txt_binkbn))==='2' && $.getInputboxValue($('#'+id))===$.id.value_off) {
					return "E20449";
				}
			}
			// 133.よりどり 新規の場合、初期値は非チェック状態に設置する。
			if(allcheck && id===$.id.chk_yoriflg){
				if((that.judgeRepType.frm1||that.judgeRepType.frm2||that.judgeRepType.frm3) && that.judgeRepType.st){
					// 【画面】.バンドル1部分の「総売価1A」の入力がある場合のみチェック可。
					var baika1 = $.getInputboxValue($('#'+$.id_inp.txt_bd1_a_baikaan));
					if($.isEmptyVal(baika1)&& !$.isEmptyVal(newValue, true)){
						// E20325	バンドル1部分の「総売価1A」の入力がある場合のみ、よりどりがチェック可能です。	 	0	 	E
						return "E20325";
					}
					if((that.judgeRepType.frm2||that.judgeRepType.frm3) && that.judgeRepType.st && (that.judgeRepType.toksp)){
						// 【画面】.「販売期間」入力時のみ入力可能。
						var stdt = $.getInputboxValue($('#'+$.id_inp.txt_hbstdt));
						var eddt = $.getInputboxValue($('#'+$.id_inp.txt_hbeddt));
						// E20397	「販売期間」に入力がある場合のみ、入力できます。	 	0	 	E
						if(($.isEmptyVal(stdt) && $.isEmptyVal(eddt)) && !$.isEmptyVal(newValue, true)){
							return "E20397";
						}
					}
				}
			}

			// *** 一個売り部分 ***
			// 135.総売価A
			//  ① 入力範囲：1～999,999。
			//  ② 商品マスタ.レギュラー情報_売価より税込み計算>=【画面】.一個売り部分の「総売価A」(『特売共通仕様書 総額売価計算方法』部分参照。)
			//     但し、【画面】.レギュラー行の「A総売価」=0orNULLの場合と催し区分=0の場合、あるいは精肉、鮮魚の場合はは本チェックを行わない。
			//  ③ 新規の場合、初期値はNULLに設置する。
			if(allcheck && id===$.id_inp.txt_ko_a_baikaan){
				var r_baika = that.getColValue("F59");
				// ②E20489	一個売り部分の「総売価A」 ≦ レギュラー行の「A売価」の条件で入力してください。	 	0	 	E
				if(!$.isEmptyVal(newValue) && !($.isEmptyVal(r_baika, true) || moyskbn===$.id.value_moykbn_r || that.judgeRepType.frm2 || that.judgeRepType.frm3)){
					if(!(r_baika*1 >= newValue*1)){
						return "E20489";
					}
				}
				if((that.judgeRepType.frm1) && that.judgeRepType.st){
					// 16_1【画面】.バンドル1部分の「総売価1A」の入力がある場合のみ必須。入力がなかったら入力不可。
					var baika = $.getInputboxValue($('#'+$.id_inp.txt_bd1_a_baikaan));
					// E20410 バンドル1部分の「総売価1A」の入力がある場合のみ総売価A入力可。
					// E20393	バンドル1部分の「総売価1A」の入力がある場合は、一個売り部分の総売価Aが必須です。	 	0	 	E
					if($.isEmptyVal(baika) && !$.isEmptyVal(newValue)){
						return "E20410";
					}else if(!$.isEmptyVal(baika)  && $.isEmptyVal(newValue)){
						return "E20393";
					}
				}
				if((that.judgeRepType.frm2||that.judgeRepType.frm3) && that.judgeRepType.st){
					// 16_2,16_3 ①【画面】.「PLU商品・定貫商品」選択時で、【画面】.バンドル1部分の「総売価1A」の入力がある場合のみ必須。② 上記以外入力不可。
					var baika = $.getInputboxValue($('#'+$.id_inp.txt_bd1_a_baikaan));
					if($('#'+$.id.rad_tkanplukbn+1).is(":checked") && !$.isEmptyVal(baika)){
						// E20332	「PLU商品・定貫商品」選択時で、バンドル1部分の「総売価1A」の入力がある場合のみ、総売価Aが必須です。	 	0	 	E
						if($.isEmptyVal(newValue)){
							return "E20332";
						}
					}else{
						// E20326	「PLU商品・定貫商品」選択時で、バンドル1部分の「総売価1A」の入力時以外、総売価Aは入力できません。	 	0	 	E
						if(!$.isEmptyVal(newValue)){
							return "E20326";
						}
					}
					if((that.judgeRepType.frm2||that.judgeRepType.frm3) && that.judgeRepType.st && (that.judgeRepType.toksp)){
						// 【画面】.「販売期間」入力時のみ入力可能。
						var stdt = $.getInputboxValue($('#'+$.id_inp.txt_hbstdt));
						var eddt = $.getInputboxValue($('#'+$.id_inp.txt_hbeddt));
						// E20397	「販売期間」に入力がある場合のみ、入力できます。	 	0	 	E
						if(($.isEmptyVal(stdt) && $.isEmptyVal(eddt)) && !$.isEmptyVal(newValue, true)){
							return "E20397";
						}
					}
				}
			}

			// 136.総売価B
			//  ① 入力範囲：1～999,999。
			//  ② 新規の場合、初期値はNULLに設置する。
			if(allcheck && id===$.id_inp.txt_ko_b_baikaan){
				if((that.judgeRepType.frm1) && that.judgeRepType.st){
					//【画面】.バンドル1部分の「総売価1B」の入力がある場合のみ必須。入力がなかったら入力不可。
					var baika = $.getInputboxValue($('#'+$.id_inp.txt_bd1_b_baikaan));
					// E20411  バンドル1部分の「総売価1B」の入力がある場合のみ総売価B入力可。
					// E20394	バンドル1部分の「総売価1B」の入力がある場合は、一個売り部分の総売価Bが必須です。	 	0	 	E
					if($.isEmptyVal(baika) && !$.isEmptyVal(newValue, true)){
						return "E20411";
					}else if(!$.isEmptyVal(baika)  && $.isEmptyVal(newValue, true)){
						return "E20394";
					}
				}
				if((that.judgeRepType.frm2||that.judgeRepType.frm3) && that.judgeRepType.st){
					// 16_2,16_3 ① 【画面】.「PLU商品・定貫商品」選択時で、【画面】.バンドル1部分の「総売価１B」の入力がある場合のみ必須。② 上記以外入力不可。
					var baika = $.getInputboxValue($('#'+$.id_inp.txt_bd1_b_baikaan));
					if($('#'+$.id.rad_tkanplukbn+1).is(":checked") && !$.isEmptyVal(baika)){
						// E20333	「PLU商品・定貫商品」選択時で、バンドル1部分の「総売価１B」の入力がある場合のみ、総売価Bが必須です。	 	0	 	E
						if($.isEmptyVal(newValue)){
							return "E20333";
						}
					}else{
						// E20328	「PLU商品・定貫商品」選択時で、バンドル1部分の「総売価1B」の入力以外、総売価Bは入力できません。	 	0	 	E
						if(!$.isEmptyVal(newValue)){
							return "E20328";
						}
					}
					if((that.judgeRepType.frm2||that.judgeRepType.frm3) && that.judgeRepType.st && (that.judgeRepType.toksp)){
						// 【画面】.「販売期間」入力時のみ入力可能。
						var stdt = $.getInputboxValue($('#'+$.id_inp.txt_hbstdt));
						var eddt = $.getInputboxValue($('#'+$.id_inp.txt_hbeddt));
						// E20397	「販売期間」に入力がある場合のみ、入力できます。	 	0	 	E
						if(($.isEmptyVal(stdt) && $.isEmptyVal(eddt)) && !$.isEmptyVal(newValue, true)){
							return "E20397";
						}
					}
				}
			}
			// 137.総売価C
			//  ① 入力範囲：1～999,999。
			//  ② 新規の場合、初期値はNULLに設置する。
			if(allcheck && id===$.id_inp.txt_ko_c_baikaan){
				if((that.judgeRepType.frm1) && that.judgeRepType.st){
					//【画面】.バンドル1部分の「総売価1C」の入力がある場合のみ必須。入力がなかったら入力不可。
					var baika = $.getInputboxValue($('#'+$.id_inp.txt_bd1_c_baikaan));
					// E20412 バンドル1部分の「総売価1C」の入力がある場合のみ総売価C入力可。
					// E20395	バンドル1部分の「総売価1C」の入力がある場合は、一個売り部分の総売価Cが必須です。
					if($.isEmptyVal(baika) && !$.isEmptyVal(newValue, true)){
						return "E20412";
					}else if(!$.isEmptyVal(baika)  && $.isEmptyVal(newValue, true)){
						return "E20395";
					}
				}
				if((that.judgeRepType.frm2||that.judgeRepType.frm3) && that.judgeRepType.st){
					// 16_2,16_3 ① 【画面】.「PLU商品・定貫商品」選択時で、【画面】.バンドル1部分の「総売価１C」の入力がある場合のみ必須。② 上記以外入力不可。
					var baika = $.getInputboxValue($('#'+$.id_inp.txt_bd1_c_baikaan));
					if($('#'+$.id.rad_tkanplukbn+1).is(":checked") && !$.isEmptyVal(baika)){
						//E20334	「PLU商品・定貫商品」選択時で、バンドル1部分の「総売価１C」の入力がある場合のみ、総売価Cが必須です。	 	0	 	E
						if($.isEmptyVal(newValue)){
							return "E20334";
						}
					}else{
						//E20330	「PLU商品・定貫商品」選択時で、バンドル1部分の「総売価1C」の入力以外、総売価Cは入力できません。	 	0	 	E
						if(!$.isEmptyVal(newValue)){
							return "E20330";
						}
					}
					if((that.judgeRepType.frm2||that.judgeRepType.frm3) && that.judgeRepType.st && (that.judgeRepType.toksp)){
						// 【画面】.「販売期間」入力時のみ入力可能。
						var stdt = $.getInputboxValue($('#'+$.id_inp.txt_hbstdt));
						var eddt = $.getInputboxValue($('#'+$.id_inp.txt_hbeddt));
						// E20397	「販売期間」に入力がある場合のみ、入力できます。	 	0	 	E
						if(($.isEmptyVal(stdt) && $.isEmptyVal(eddt)) && !$.isEmptyVal(newValue, true)){
							return "E20397";
						}
					}
				}
			}

			// *** バンドル1部分 ***
			// 139.点数1
			//  ① 入力範囲：1～999。
			//  ② 新規の場合、初期値はNULLに設置する。
			if(allcheck && id===$.id_inp.txt_bd1_tensu){
				// 16_1【画面】.バンドル1部分の「総売価1A」の入力がある場合2以上が可。
				// 16_4【画面】.バンドル1部分の「総売価1A」の入力がある場合2以上が可。
				if((that.judgeRepType.frm1||that.judgeRepType.frm4) && that.judgeRepType.st){
					var baika = $.getInputboxValue($('#'+$.id_inp.txt_bd1_a_baikaan));

					// 16_1【画面】.バンドル1部分の「総売価1A」の入力がある場合のみ必須。入力がなかったら入力不可。
					if($.isEmptyVal(baika) && !$.isEmptyVal(newValue)){
						return "E20413";
					}

					// E20419	バンドル1部分の「総売価1A」の入力がある場合、点数1に2以上を入力してください。	 	0	 	E
					if(!$.isEmptyVal(newValue) && !$.isEmptyVal(baika) && !(newValue*1 >=2)){
						return "E20419";
					}
				}

				if((that.judgeRepType.frm2||that.judgeRepType.frm3) && that.judgeRepType.st){
					// 16_2,16_3 【画面】.「PLU商品・定貫商品」選択時で、【画面】.バンドル1部分の「総売価1A」の入力がある場合2以上可。
					var baika = $.getInputboxValue($('#'+$.id_inp.txt_bd1_a_baikaan));

					if($('#'+$.id.rad_tkanplukbn+1).is(":checked") && !$.isEmptyVal(newValue)) {

						if ($.isEmptyVal(baika)) {
							return "E20413";
						}

						// E20336	「PLU商品・定貫商品」選択時で、バンドル1部分の「総売価1A」の入力がある場合点数1が2以上可。	 	0	 	E
						if(!(newValue*1 >=2)){
							return "E20336";
						}
					}

					if((that.judgeRepType.frm2||that.judgeRepType.frm3) && that.judgeRepType.st && (that.judgeRepType.toksp)){
						// 【画面】.「販売期間」入力時のみ入力可能。
						var stdt = $.getInputboxValue($('#'+$.id_inp.txt_hbstdt));
						var eddt = $.getInputboxValue($('#'+$.id_inp.txt_hbeddt));
						// E20397	「販売期間」に入力がある場合のみ、入力できます。	 	0	 	E
						if(($.isEmptyVal(stdt) && $.isEmptyVal(eddt)) && !$.isEmptyVal(newValue, true)){
							return "E20397";
						}
					}
				}
			}
			// 140.総売価1A
			//  ① 入力範囲：1～999,999。
			//  ② 新規の場合、初期値はNULLに設置する。
			//  ③ 1個売り部分「総売価A」 ＜ バンドル1部分「総売価1A」
			//  ④ 1個売り部分「総売価A」 ≧ バンドル1部分「総売価1A」÷点数1
			if(allcheck && id===$.id_inp.txt_bd1_a_baikaan && !$.isEmptyVal(newValue)){
				var ko_baika = $.getInputboxValue($('#'+$.id_inp.txt_ko_a_baikaan));
				if (that.judgeRepType.frm4) {
					ko_baika = $.getInputboxValue($('#'+$.id_inp.txt_a_baikaam+3))
				}
				if (!$.isEmptyVal(ko_baika)) {
					// ③E20584	バンドル総売価１Ａに1個売り総売価Ａ以下の値が入力されています。	 	0	 	E
					if(!(ko_baika*1 < newValue*1)){
						return "E20584";
					}
					// ④E20585	バンドル総売価１Ａの平均売価（＝円/個）が1個売り総売価Ａより大きくなっています。	 	0	 	E
					var tensu = $.getInputboxValue($('#'+$.id_inp.txt_bd1_tensu));
					if ($.isEmptyVal(tensu)) {
						tensu = '1';
					}
					if(!(ko_baika*1 >= that.calcAvg(newValue, tensu))){
						return "E20585";
					}
				}

				if((that.judgeRepType.frm1||that.judgeRepType.frm4)&& that.judgeRepType.st){
					// 16_1,16_4【画面】.「販売期間」に入力がある場合のみ、入力可。
					var stdt = $.getInputboxValue($('#'+$.id_inp.txt_hbstdt));
					var eddt = $.getInputboxValue($('#'+$.id_inp.txt_hbeddt));
					// E20397	「販売期間」に入力がある場合のみ、入力できます。	 	0	 	E
					if($.isEmptyVal(stdt)||$.isEmptyVal(eddt)){
						return "E20397";
					}
				}
				if((that.judgeRepType.frm2||that.judgeRepType.frm3) && that.judgeRepType.st){
					// 16_2,16_3【画面】.「販売期間」入力時のみで【画面】.「PLU商品・定貫商品」選択時入力可。
					var stdt = $.getInputboxValue($('#'+$.id_inp.txt_hbstdt));
					var eddt = $.getInputboxValue($('#'+$.id_inp.txt_hbeddt));
					if(!(!($.isEmptyVal(stdt) && $.isEmptyVal(stdt)) && $('#'+$.id.rad_tkanplukbn+1).is(":checked"))){
						// E20337	「販売期間」入力、かつ「PLU商品・定貫商品」選択時総売価1Aが入力できます。	 	0	 	E
						if(!$.isEmptyVal(newValue)){
							return "E20337";
						}
					}
				}
			}
			// 141.総売価1B
			//  ① 入力範囲：1～999,999。
			//  ② 新規の場合、初期値はNULLに設置する。
			//  ③ 1個売り部分「総売価B」 ＜ バンドル1部分「総売価1B」
			//  ④ 1個売り部分「総売価B」 ≧ バンドル1部分「総売価1B」÷点数1
			if(allcheck && id===$.id_inp.txt_bd1_b_baikaan){
				var ko_baika = $.getInputboxValue($('#'+$.id_inp.txt_ko_b_baikaan));
				if (that.judgeRepType.frm4) {
					ko_baika = $.getInputboxValue($('#'+$.id_inp.txt_b_baikaam+3))
				}
				// ③E20588	バンドル総売価１Ｂに1個売り総売価Ｂ以下の値が入力されています。	 	0	 	E
				if(!$.isEmptyVal(newValue) && !$.isEmptyVal(ko_baika) && ko_baika*1 >= newValue*1){
					return "E20588";
				}
				// ④E20589	バンドル総売価１Ｂの平均売価（＝円/個）が1個売り総売価Ｂより大きくなっています。	 	0	 	E
				var tensu = $.getInputboxValue($('#'+$.id_inp.txt_bd1_tensu));
				if ($.isEmptyVal(tensu)) {
					tensu = '1';
				}
				if(!$.isEmptyVal(newValue) && !$.isEmptyVal(ko_baika) && ko_baika*1 < that.calcAvg(newValue, tensu)){
					return "E20589";
				}

				if((that.judgeRepType.frm1||that.judgeRepType.frm2||that.judgeRepType.frm3||that.judgeRepType.frm4) && that.judgeRepType.st){
					// 16_1,16_2,16_3,16_4【画面】.バンドル1部分の「総売価1A」の入力があり、【画面】.B部分の「B総売価」に入力がある場合のみ必須。それ以外は入力不可。
					var baika1 = $.getInputboxValue($('#'+$.id_inp.txt_bd1_a_baikaan));
					var baika2 = $.getInputboxValue($('#'+$.id_inp.txt_b_baikaam+1));
					if (that.judgeRepType.frm4) {
						baika2 = $.getInputboxValue($('#'+$.id_inp.txt_b_baikaam+3));
					}
					if(!$.isEmptyVal(baika1) && !$.isEmptyVal(baika2)){
						// E20396 バンドル1部分の「総売価1A」の入力があり、B部分の「B総売価」に入力がある場合は総売価1Bが必須。
						if($.isEmptyVal(newValue)){
							return "E20396";
						}
					}else{
						// E20338	バンドル1部分の「総売価1A」の入力があり、B部分の「B総売価」に入力時以外は総売価1Bは入力できません。	 	0	 	E
						if(!$.isEmptyVal(newValue)){
							return "E20338";
						}
					}
					if((that.judgeRepType.frm2||that.judgeRepType.frm3) && that.judgeRepType.st && (that.judgeRepType.toksp)){
						// 【画面】.「販売期間」入力時のみ入力可能。
						var stdt = $.getInputboxValue($('#'+$.id_inp.txt_hbstdt));
						var eddt = $.getInputboxValue($('#'+$.id_inp.txt_hbeddt));
						// E20397	「販売期間」に入力がある場合のみ、入力できます。	 	0	 	E
						if(($.isEmptyVal(stdt) && $.isEmptyVal(eddt)) && !$.isEmptyVal(newValue, true)){
							return "E20397";
						}
					}
				}
			}
			// 142.総売価1C
			//  ① 入力範囲：1～999,999。
			//  ② 新規の場合、初期値はNULLに設置する。
			//  ③ 1個売り部分「総売価C」 ＜ バンドル1部分「総売価1C」
			//  ④ 1個売り部分「総売価C」 ≧ バンドル1部分「総売価1C」÷点数1
			if(allcheck && id===$.id_inp.txt_bd1_c_baikaan && !$.isEmptyVal(newValue)){
				var ko_baika = $.getInputboxValue($('#'+$.id_inp.txt_ko_c_baikaan));
				if (that.judgeRepType.frm4) {
					ko_baika = $.getInputboxValue($('#'+$.id_inp.txt_c_baikaam+3))
				}
				// ③E20592	バンドル総売価１Ｃに1個売り総売価Ｃ以下の値が入力されています。	 	0	 	E
				if(!$.isEmptyVal(ko_baika) && ko_baika*1 >= newValue*1){
					return "E20592";
				}
				// ④E20593	バンドル総売価１Ｃの平均売価（＝円/個）が1個売り総売価Ｃより大きくなっています。	 	0	 	E
				var tensu = $.getInputboxValue($('#'+$.id_inp.txt_bd1_tensu));
				if ($.isEmptyVal(tensu)) {
					tensu = '1';
				}
				if(!$.isEmptyVal(ko_baika) && ko_baika*1 < that.calcAvg(newValue, tensu)){
					return "E20593";
				}
			}

			if(allcheck && id===$.id_inp.txt_bd1_c_baikaan){
				if((that.judgeRepType.frm1||that.judgeRepType.frm2||that.judgeRepType.frm3||that.judgeRepType.frm4) && that.judgeRepType.st){
					// 16_1,16_2,16_3,16_4【画面】.バンドル1部分の「総売価1B」の入力があり、【画面】.C部分の「C総売価」に入力がある場合のみ必須。それ以外は入力不可。
					var baika1 = $.getInputboxValue($('#'+$.id_inp.txt_bd1_b_baikaan));
					var baika2 = $.getInputboxValue($('#'+$.id_inp.txt_c_baikaam+1));
					if (that.judgeRepType.frm4) {
						baika2 = $.getInputboxValue($('#'+$.id_inp.txt_c_baikaam+3));
					}
					if(!$.isEmptyVal(baika1) && !$.isEmptyVal(baika2)){
						//E20399	バンドル1部分の「総売価1B」の入力があり、C部分の「C総売価」に入力がある場合は総売価1Cが必須。	 	0	 	E
						if($.isEmptyVal(newValue)){
							return "E20399";
						}
					}else{
						//E20340 バンドル1部分の「総売価1B」の入力があり、C部分の「C総売価」に入力時以外は総売価1Cは入力できません｡
						if(!$.isEmptyVal(newValue)){
							return "E20340";
						}
					}
					if((that.judgeRepType.frm2||that.judgeRepType.frm3) && that.judgeRepType.st && (that.judgeRepType.toksp)){
						// 【画面】.「販売期間」入力時のみ入力可能。
						var stdt = $.getInputboxValue($('#'+$.id_inp.txt_hbstdt));
						var eddt = $.getInputboxValue($('#'+$.id_inp.txt_hbeddt));
						// E20397	「販売期間」に入力がある場合のみ、入力できます。	 	0	 	E
						if(($.isEmptyVal(stdt) && $.isEmptyVal(eddt)) && !$.isEmptyVal(newValue, true)){
							return "E20397";
						}
					}
				}
			}

			// *** バンドル2部分 ***
			// 144.点数2
			//  ① 入力範囲：1～999。
			//  ② 新規の場合、初期値はNULLに設置する。
			if(allcheck && id===$.id_inp.txt_bd2_tensu){
				if((that.judgeRepType.frm1||that.judgeRepType.frm4) && that.judgeRepType.st){
					// 16_1,16_4【画面】.バンドル2部分の「総売価２A」の入力がある場合、 【画面】.バンドル1部分の「点数1」より大きい。
					var tensu = $.getInputboxValue($('#'+$.id_inp.txt_bd1_tensu));
					var baika = $.getInputboxValue($('#'+$.id_inp.txt_bd2_a_baikaan));
					// E20342 バンドル2部分の「総売価２A」の入力がある場合、 バンドル2部分の「点数2」がバンドル1部分の「点数1」より大きい。
					if(!$.isEmptyVal(newValue) && !$.isEmptyVal(baika) && !$.isEmptyVal(tensu) && tensu*1 >= newValue*1){
						return "E20342";
					}
				}
				if((that.judgeRepType.frm2||that.judgeRepType.frm3) && that.judgeRepType.st){
					// 16_2,16_3【画面】.「PLU商品・定貫商品」選択時で、【画面】.バンドル2部分の「総売価2A」の入力がある場合、 【画面】.バンドル1部分の「点数1」より大きい値入力可能。
					var tensu = $.getInputboxValue($('#'+$.id_inp.txt_bd1_tensu));
					var baika = $.getInputboxValue($('#'+$.id_inp.txt_bd2_a_baikaan));
					if(!($('#'+$.id.rad_tkanplukbn+1).is(":checked") && !$.isEmptyVal(baika))){
						// E20441	「PLU商品・定貫商品」選択時で、バンドル1部分の「総売価1B」の入力以外総売価Bが入力不可。	 	0	 	E
						if(!$.isEmptyVal(newValue)){
							return "E20441";
						}
					}
					if((that.judgeRepType.frm2||that.judgeRepType.frm3) && that.judgeRepType.st && (that.judgeRepType.toksp)){
						// 【画面】.「販売期間」入力時のみ入力可能。
						var stdt = $.getInputboxValue($('#'+$.id_inp.txt_hbstdt));
						var eddt = $.getInputboxValue($('#'+$.id_inp.txt_hbeddt));
						// E20397	「販売期間」に入力がある場合のみ、入力できます。	 	0	 	E
						if(($.isEmptyVal(stdt) && $.isEmptyVal(eddt)) && !$.isEmptyVal(newValue, true)){
							return "E20397";
						}
					}
				}
			}
			// 145.総売価２A
			//  ① 入力範囲：1～999,999。
			//  ② 新規の場合、初期値はNULLに設置する。
			//  ③ バンドル1部分「総売価1A」 ＜ バンドル2部分「総売価2A」
			//  ④ バンドル1部分「総売価1A」 ≧ バンドル2部分「総売価2A」÷点数2
			if(allcheck && id===$.id_inp.txt_bd2_a_baikaan && !$.isEmptyVal(newValue)){
				var baika = $.getInputboxValue($('#'+$.id_inp.txt_bd1_a_baikaan));
				// ③E20586	バンドル総売価２Ａにバンドル総売価１Ａ以下の値が入力されています。	 	0	 	E
				if(!(baika*1 < newValue*1)){
					return "E20586";
				}
				// ④E20587	バンドル総売価２Ａの平均売価≦バンドル総売価１Ａの平均売価の範囲で入力してください。	 	0	 	E
				var tensu1 = $.getInputboxValue($('#'+$.id_inp.txt_bd1_tensu));
				var tensu2 = $.getInputboxValue($('#'+$.id_inp.txt_bd2_tensu));
				if ($.isEmptyVal(tensu1)) {
					tensu1 = '1';
				}

				if ($.isEmptyVal(tensu2)) {
					tensu2 = '1';
				}

				if(!$.isEmptyVal(baika) && !(that.calcAvg(baika, tensu1) >= that.calcAvg(newValue, tensu2))){
					return "E20587";
				}

				if((that.judgeRepType.frm1||that.judgeRepType.frm2||that.judgeRepType.frm3||that.judgeRepType.frm4) && that.judgeRepType.st){
					// 16_1,16_2,16_3,16_4【画面】.バンドル1部分の「総売価1A」に入力がある場合のみ入力可。
					var baika = $.getInputboxValue($('#'+$.id_inp.txt_bd1_a_baikaan));
					// E20410	バンドル1部分の「総売価1A」の入力がある場合のみ総売価A入力可。	 	0	 	E
					if($.isEmptyVal(baika) && !$.isEmptyVal(newValue)){
						return "E20344";
					}
					if((that.judgeRepType.frm2||that.judgeRepType.frm3) && that.judgeRepType.st && (that.judgeRepType.toksp)){
						// 【画面】.「販売期間」入力時のみ入力可能。
						var stdt = $.getInputboxValue($('#'+$.id_inp.txt_hbstdt));
						var eddt = $.getInputboxValue($('#'+$.id_inp.txt_hbeddt));
						// E20397	「販売期間」に入力がある場合のみ、入力できます。	 	0	 	E
						if(($.isEmptyVal(stdt) && $.isEmptyVal(eddt)) && !$.isEmptyVal(newValue, true)){
							return "E20397";
						}
					}
				}
			}

			// 146.総売価２B
			//  ① 入力範囲：1～999,999。
			//  ② 新規の場合、初期値はNULLに設置する。
			//  ③ バンドル1部分「総売価1B」 ＜ バンドル2部分「総売価2B」
			//  ④ バンドル1部分「総売価1B」 ≧ バンドル2部分「総売価2B」÷点数2
			if(allcheck && id===$.id_inp.txt_bd2_b_baikaan){
				var baika = $.getInputboxValue($('#'+$.id_inp.txt_bd1_b_baikaan));
				// ③ E20590	バンドル総売価２Ｂにバンドル総売価１Ｂ以下の値が入力されています。	 	0	 	E
				if(!$.isEmptyVal(newValue) && !(baika*1 < newValue*1)){
					return "E20590";
				}
				// ④ E20591	バンドル総売価２Ｂの平均売価≦バンドル総売価１Ｂの平均売価の範囲で入力してください。	 	0	 	E
				var tensu1 = $.getInputboxValue($('#'+$.id_inp.txt_bd1_tensu));
				var tensu2 = $.getInputboxValue($('#'+$.id_inp.txt_bd2_tensu));
				if ($.isEmptyVal(tensu1)) {
					tensu1 = '1';
				}

				if ($.isEmptyVal(tensu2)) {
					tensu2 = '1';
				}

				if(!$.isEmptyVal(newValue) && !$.isEmptyVal(baika) && !(that.calcAvg(baika, tensu1) >= that.calcAvg(newValue, tensu2))){
					return "E20591";
				}

				if((that.judgeRepType.frm1||that.judgeRepType.frm2||that.judgeRepType.frm3||that.judgeRepType.frm4) && that.judgeRepType.st){
					// 16_1,16_2,16_3,16_4【画面】.バンドル2部分の「総売価2A」の入力があり、【画面】.B部分の「B総売価」に入力がある場合のみ必須。それ以外は入力不可。
					var baika1 = $.getInputboxValue($('#'+$.id_inp.txt_bd2_a_baikaan));
					var baika2 = $.getInputboxValue($('#'+$.id_inp.txt_b_baikaam+1));
					if (that.judgeRepType.frm4) {
						baika2 = $.getInputboxValue($('#'+$.id_inp.txt_b_baikaam+3));
					}
					if(!$.isEmptyVal(baika1) && !$.isEmptyVal(baika2)){
						// E20402	バンドル2部分の「総売価2A」の入力があり、B部分の「B総売価」に入力が	ある場合は総売価2Bが必須。	0	 	E
						if($.isEmptyVal(newValue)){
							return "E20402";
						}
					}else{
						// E20346	バンドル2部分の「総売価2A」の入力があり、B部分の「B総売価」に入力時以外は総売価２Bは入力できません。	 	0	 	E
						if(!$.isEmptyVal(newValue)){
							return "E20346";
						}
					}
					if((that.judgeRepType.frm2||that.judgeRepType.frm3) && that.judgeRepType.st && (that.judgeRepType.toksp)){
						// 【画面】.「販売期間」入力時のみ入力可能。
						var stdt = $.getInputboxValue($('#'+$.id_inp.txt_hbstdt));
						var eddt = $.getInputboxValue($('#'+$.id_inp.txt_hbeddt));
						// E20397	「販売期間」に入力がある場合のみ、入力できます。	 	0	 	E
						if(($.isEmptyVal(stdt) && $.isEmptyVal(eddt)) && !$.isEmptyVal(newValue, true)){
							return "E20397";
						}
					}
				}
			}
			// 147.総売価２C
			//  ① 入力範囲：1～999,999。
			//  ② 新規の場合、初期値はNULLに設置する。
			//  ③ バンドル1部分「総売価1C」 ＜ バンドル2部分「総売価2C」
			//  ④ バンドル1部分「総売価1C」 ≧ バンドル2部分「総売価2C」÷点数2
			if(allcheck && id===$.id_inp.txt_bd2_c_baikaan){
				var baika = $.getInputboxValue($('#'+$.id_inp.txt_bd1_c_baikaan));
				// ③ E20594	バンドル総売価２Ｃにバンドル総売価１Ｃ以下の値が入力されています。	 	0	 	E
				if(!$.isEmptyVal(newValue) && !(baika*1 < newValue*1)){
					return "E20594";
				}
				// ④ E20595	バンドル総売価２Ｃの平均売価≦バンドル総売価１Ｃの平均売価の範囲で入力してください。	 	0	 	E
				var tensu1 = $.getInputboxValue($('#'+$.id_inp.txt_bd1_tensu));
				var tensu2 = $.getInputboxValue($('#'+$.id_inp.txt_bd2_tensu));
				if ($.isEmptyVal(tensu1)) {
					tensu1 = '1';
				}

				if ($.isEmptyVal(tensu2)) {
					tensu2 = '1';
				}

				if(!$.isEmptyVal(newValue) && !$.isEmptyVal(baika) && !(that.calcAvg(baika, tensu1) >= that.calcAvg(newValue, tensu2))){
					return "E20595";
				}

				if((that.judgeRepType.frm1||that.judgeRepType.frm2||that.judgeRepType.frm3||that.judgeRepType.frm4)  && that.judgeRepType.st){
					// 16_1,16_2,16_3,16_4【画面】.バンドル2部分の「総売価2B」の入力があり、【画面】.C部分の「C総売価」に入力がある場合のみ必須。それ以外は入力不可。
					var baika1 = $.getInputboxValue($('#'+$.id_inp.txt_bd2_b_baikaan));
					var baika2 = $.getInputboxValue($('#'+$.id_inp.txt_c_baikaam+1));
					if (that.judgeRepType.frm4) {
						baika2 = $.getInputboxValue($('#'+$.id_inp.txt_c_baikaam+3));
					}
					if(!$.isEmptyVal(baika1) && !$.isEmptyVal(baika2)){
						// E20403	バンドル2部分の「総売価2B」の入力があり、C部分の「C総売価」に入力が	ある場合は総売価2Cが必須。	0	 	E
						if($.isEmptyVal(newValue)){
							return "E20403";
						}
					}else{
						//E20507	バンドル2部分の「総売価2B」の入力があり、C部分の「C総売価」に入力がある場合以外は、	総売価２Cは入力できません。	0	 	E
						if(!$.isEmptyVal(newValue)){
							return "E20507";
						}
					}
					if((that.judgeRepType.frm2||that.judgeRepType.frm3) && that.judgeRepType.st && (that.judgeRepType.toksp)){
						// 【画面】.「販売期間」入力時のみ入力可能。
						var stdt = $.getInputboxValue($('#'+$.id_inp.txt_hbstdt));
						var eddt = $.getInputboxValue($('#'+$.id_inp.txt_hbeddt));
						// E20397	「販売期間」に入力がある場合のみ、入力できます。	 	0	 	E
						if(($.isEmptyVal(stdt) && $.isEmptyVal(eddt)) && !$.isEmptyVal(newValue, true)){
							return "E20397";
						}
					}
				}
			}

			// *** 100g相当部分 ***
			// 149.A総売価
			//  ① 入力範囲：1～999,999。
			//  ② 新規の場合、初期値はNULLに設置する。
			//  ③ 【画面】.100g相当部分の「A総売価」>=【画面】.100g相当部分の「B総売価」>=【画面】.100g相当部分の「C総売価」
			//  ④ 【画面】.100g相当部分の「A総売価」=【画面】.100g相当部分の「B総売価」=【画面】.100g相当部分の「A総売価」は不可
			//  ⑤ 【画面】.不定貫商品が選択されている場合に入力されていたらエラー
			if(allcheck && id===$.id_inp.txt_a_baikaam_100g && !$.isEmptyVal(newValue)){
				var a_baikaam = id===$.id_inp.txt_a_baikaam_100g ? newValue: $.getInputboxValue($('#'+$.id_inp.txt_a_baikaam_100g));
				var b_baikaam = id===$.id_inp.txt_b_baikaam_100g ? newValue: $.getInputboxValue($('#'+$.id_inp.txt_b_baikaam_100g));
				var c_baikaam = id===$.id_inp.txt_c_baikaam_100g ? newValue: $.getInputboxValue($('#'+$.id_inp.txt_c_baikaam_100g));
				// ③E20562	販売情報でA総売価行の「100g相当」 ≧ B総売価行の「100g相当」 ≧ C総売価行の「100g相当」	の条件で入力してください。	0	 	E
				if(!(a_baikaam*1 >= b_baikaam*1 && b_baikaam*1 >= c_baikaam*1)){
					return "E20562";
				}
				// ④E20563	販売情報でA総売価行の「100g相当」 = B総売価行の「100g相当」 = C総売価行の「100g相当」	は入力できません。	0	 	E
				if(a_baikaam*1 == b_baikaam*1 && b_baikaam*1 == c_baikaam*1){
					return "E20563";
				}
				// ⑤E20600	「不定貫商品」選択時にA総売価「100g相当」は入力できません。	 	0	 	E
				if($('#'+$.id.rad_tkanplukbn+2).is(":checked")){
					return "E20600";
				}
			}
			// 150.B総売価
			//  ① 入力範囲：1～999,999。
			//  ② 新規の場合、初期値はNULLに設置する。
			//  ③ 【画面】.100g相当部分の「A総売価」>=【画面】.100g相当部分の「B総売価」>=【画面】.100g相当部分の「C総売価」
			//  ④ 【画面】.100g相当部分の「A総売価」=【画面】.100g相当部分の「B総売価」=【画面】.100g相当部分の「A総売価」は不可
			//  ⑤ 【画面】.不定貫商品が選択されている場合に入力されていたらエラー
			if(allcheck && id===$.id_inp.txt_b_baikaam_100g){
				var a_baikaam = id===$.id_inp.txt_a_baikaam_100g ? newValue: $.getInputboxValue($('#'+$.id_inp.txt_a_baikaam_100g));
				var b_baikaam = id===$.id_inp.txt_b_baikaam_100g ? newValue: $.getInputboxValue($('#'+$.id_inp.txt_b_baikaam_100g));
				var c_baikaam = id===$.id_inp.txt_c_baikaam_100g ? newValue: $.getInputboxValue($('#'+$.id_inp.txt_c_baikaam_100g));
				// ③E20562	販売情報でA総売価行の「100g相当」 ≧ B総売価行の「100g相当」 ≧ C総売価行の「100g相当」	の条件で入力してください。	0	 	E
				if(!$.isEmptyVal(newValue) && !(a_baikaam*1 >= b_baikaam*1 && b_baikaam*1 >= c_baikaam*1)){
					return "E20562";
				}
				// ④E20563	販売情報でA総売価行の「100g相当」 = B総売価行の「100g相当」 = C総売価行の「100g相当」	は入力できません。	0	 	E
				if(!$.isEmptyVal(newValue) && a_baikaam*1 == b_baikaam*1 && b_baikaam*1 == c_baikaam*1){
					return "E20563";
				}
				if((that.judgeRepType.frm2) && that.judgeRepType.st){
					// 16_2 定貫時：【画面】.B部分の「B総売価」に入力がある場合のみ入力可能。不定貫時：【画面】.B総売価行の「100g総売価」に入力がある場合のみ入力可能。
					// E20447	B部分の「B総売価」に入力がある場合のみB総売価が入力可能。
					if(!$.isEmptyVal(newValue) && $('#'+$.id.rad_tkanplukbn+1).is(":checked") && $.isEmptyVal($.getInputboxValue($('#'+$.id_inp.txt_b_baikaam+1)))){
						return "E20447";
					}
					// E20505	B総売価行の「100g総売価」に入力がある場合のみB総売価が入力できます。	 	0	 	E
					if(!$.isEmptyVal(newValue) && $('#'+$.id.rad_tkanplukbn+2).is(":checked") && $.isEmptyVal($.getInputboxValue($('#'+$.id_inp.txt_b_baikaam+2)))){
						return "E20505";
					}
				}else{
					// ⑤E20601	「不定貫商品」選択時にB総売価「100g相当」は入力できません。	 	0	 	E
					if(!$.isEmptyVal(newValue) && $('#'+$.id.rad_tkanplukbn+2).is(":checked")){
						return "E20601";
					}
				}
			}
			// 151.C総売価
			//  ① 入力範囲：1～999,999。
			//  ② 新規の場合、初期値はNULLに設置する。
			//  ③ 【画面】.100g相当部分の「A総売価」>=【画面】.100g相当部分の「B総売価」>=【画面】.100g相当部分の「C総売価」
			//  ④ 【画面】.100g相当部分の「A総売価」=【画面】.100g相当部分の「B総売価」=【画面】.100g相当部分の「A総売価」は不可
			//  ⑤ 【画面】.100g相当部分の「B総売価」が入力されていないと入力不可
			//  ⑥ 【画面】.不定貫商品が選択されている場合に入力されていたらエラー
			if(allcheck && id===$.id_inp.txt_c_baikaam_100g && !$.isEmptyVal(newValue)){
				var a_baikaam = id===$.id_inp.txt_a_baikaam_100g ? newValue: $.getInputboxValue($('#'+$.id_inp.txt_a_baikaam_100g));
				var b_baikaam = id===$.id_inp.txt_b_baikaam_100g ? newValue: $.getInputboxValue($('#'+$.id_inp.txt_b_baikaam_100g));
				var c_baikaam = id===$.id_inp.txt_c_baikaam_100g ? newValue: $.getInputboxValue($('#'+$.id_inp.txt_c_baikaam_100g));
				// ③E20562	販売情報でA総売価行の「100g相当」 ≧ B総売価行の「100g相当」 ≧ C総売価行の「100g相当」	の条件で入力してください。	0	 	E
				if(!(a_baikaam*1 >= b_baikaam*1 && b_baikaam*1 >= c_baikaam*1)){
					return "E20562";
				}
				// ④E20563	販売情報でA総売価行の「100g相当」 = B総売価行の「100g相当」 = C総売価行の「100g相当」	は入力できません。	0	 	E
				if(a_baikaam*1 == b_baikaam*1 && b_baikaam*1 == c_baikaam*1){
					return "E20563";
				}
				if((that.judgeRepType.frm2) && that.judgeRepType.st){
					// 16_2 定貫時：【画面】.C部分の「C総売価」に入力がある場合のみ入力可能。不定貫時：【画面】.C総売価行の「100g総売価」に入力がある場合のみ入力可能。
					// E20448	C部分の「C総売価」に入力がある場合のみC総売価が入力可能。	 	0	 	E
					if(!$.isEmptyVal(newValue) && $('#'+$.id.rad_tkanplukbn+1).is(":checked") && $.isEmptyVal($.getInputboxValue($('#'+$.id_inp.txt_c_baikaam+1)))){
						return "E20448";
					}
					// E20506	C総売価行の「100g総売価」に入力がある場合のみC総売価が入力できます。	 	0	 	E
					if(!$.isEmptyVal(newValue) && $('#'+$.id.rad_tkanplukbn+2).is(":checked") && $.isEmptyVal($.getInputboxValue($('#'+$.id_inp.txt_c_baikaam+2)))){
						return "E20506";
					}
				}else{
					// ⑤E20602	「不定貫商品」選択時にC総売価「100g相当」は入力できません。	 	0	 	E
					if($('#'+$.id.rad_tkanplukbn+2).is(":checked")){
						return "E20602";
					}
				}
			}

			// *** 生食・加熱/解凍/養殖 ***
			// 153.生食・加熱
			//  ① 生食、加熱はどちらか一方にしかチェックをつけれない。	※
			//  ② 新規の場合、初期値は非チェックに設置する。
			// 154.解凍 新規の場合、初期値は非チェックに設置する。
			// 155.養殖 新規の場合、初期値は非チェックに設置する。

			// *** チラシ・ＰＯＰ情報 ***
			// 156.目玉情報
			//  ① リスト内容：名称コード区分=10660で名称マスタから取得する。
			//  ② 新規の場合、初期値は空白行に設置する。
			// 157.POPコード
			//  ① 入力範囲：1～9999999999。
			//  ② 新規の場合、初期値はNULLに設置する。
			// 158.POPサイズ 新規の場合、初期値はNULLに設置する。
			// 159.枚数
			//  ① 入力範囲：1～99。
			//  ② 新規の場合、初期値はNULLに設置する。
			//  ③ 【画面】.「POPコード」または【画面】.「POPサイズ」が入力されたら１以上必須。それ以外入力不可。
			if(allcheck && id===$.id_inp.txt_popsu){
				// ③E20531	「POPコード」または「POPサイズ」が入力されたら、	「枚数」は1以上を入力してください。	0	 	E
				if($.isEmptyVal(newValue, true) && (!$.isEmptyVal($.getInputboxValue($('#'+$.id_inp.txt_popcd)))||!$.isEmptyVal($.getInputboxValue($('#'+$.id_inp.txt_popsz))))){
					return "E20531";
				}
			}
			// 160.商品サイズ
			//  ① 全角20文字。
			//  ② 新規の場合、初期値はNULLに設置する。
			// 161.商品色
			//  ① 全角10文字。
			//  ② 新規の場合、初期値はNULLに設置する。
			// 162.その他日替コメント
			//  ① 全角50文字。
			//  ② 新規の場合、初期値はNULLに設置する。
			// 163.POPコメント
			//  ① 全角50文字。
			//  ② 新規の場合、初期値はNULLに設置する。


			// 【納入情報部分】

			var stdt = id===$.id_inp.txt_nnstdt ? $.getParserDt(newValue): $.getInputboxValue($('#'+$.id_inp.txt_nnstdt));
			var eddt = id===$.id_inp.txt_nneddt ? $.getParserDt(newValue): $.getInputboxValue($('#'+$.id_inp.txt_nneddt));
			if(!$.isEmptyVal(stdt)&&!$.isEmptyVal(eddt)){
				// 170.事前打出(チェック) 新規の場合、初期値は非チェック状態に設置する。
				if(allcheck && id===$.id.chk_juflg){
					if((that.judgeRepType.frm1||that.judgeRepType.frm2||that.judgeRepType.frm3) && that.judgeRepType.st && (that.judgeRepType.toktg_h||that.judgeRepType.toksp)){
						// 16_1,16_2,16_3
						//  ① 【画面】.「納入期間」入力がある時のみ入力可。 TODO:
						//  ② 商品マスタ.PC区分=1の場合、チェック不可。
						// E20350	PC商品の場合、事前打出(チェック)チェックはできません。	 	0	 	E
						if(that.getColValue("F173")==="1" && !$.isEmptyVal(newValue, true)){
							return "E20350";
						}
					}
				}
				// 171.事前打出(日付)
				//  ① 入力範囲：2003/01/01～9999/12/31
				//  ② 【画面】.納入情報部分の「事前打出（チェック）」がＯＮの時のみ入力可。それ以外は入力不可。
				//  ③ 処理日付が「事前打出（日付）」の前日まで修正可能。
				//  ④ 処理日付けの翌日以降の日付を入力可能。
				//  ⑤ 新規の場合、初期値はNULLに設置する。
				if(id===$.id_inp.txt_juhtdt){
					// ②E20352	納入情報部分の「事前打出（チェック）」がＯＮの時のみ事前打出(日付)が入力できます。	 	0	 	E
					if(!$('#'+$.id.chk_juflg).is(":checked") && !$.isEmptyVal(newValue)){
						return "E20352";
					}

					var shoriDt = $.getInputboxValue($('#'+$.id.txt_shoridt));

					// ④E20530	事前打出日付は明日以降の日付しか入力できません。	 	0	 	E
					// 入力時は6桁（YYMMDD）   例.210107   ※YY=YYYYの下２桁
					// 登録時は8桁（YYYYMMDD） 例.20210107 ※20210121 追加
					if(!$.isEmptyVal(newValue) && (
						  (newValue.length===6 && shoriDt.substr(2,6) >= newValue)
						||(newValue.length===8 && shoriDt >= newValue)
							)){
						return "E20530";
					}

					if((that.judgeRepType.frm1||that.judgeRepType.frm2||that.judgeRepType.frm3) && that.judgeRepType.st && (that.judgeRepType.toktg_h||that.judgeRepType.toksp)){
						// 16_1,16_2,16_3【画面】.「事前打出（チェック）」がチェックされた時のみ必須。
						// E20353	「事前打出（チェック）」がチェックされた時のみ事前打出(日付)が必須です。	 	0	 	E
						if($('#'+$.id.chk_juflg).is(":checked") && $.isEmptyVal(newValue)){
							return "E20353";
						}
					}
				}
				// 172.特売コメント
				//  ① 全角30文字。
				//  ② 新規の場合、初期値はNULLに設置する。
				// 173.カット店展開しない
				//  ① アンケート有の場合、
				//  　新規画面では、常に登録ボタンを押すまでは変更可能（前複写の場合も）。
				//  　変更画面では、全店特売（アンケート有）_基本.アンケート取込開始日<=処理日付の時、DISABLE。
				//  ② 新規の場合、初期値は非チェック状態に設置する。
				// 175.便区分
				//  ① 新規の初期値は1に設置する。
				//  ② DB登録・更新時に、名称コード区分=10665で名称マスタとの整合チェックを行う。
				if(allcheck && id===$.id_inp.txt_binkbn && !$.isEmptyVal(newValue)){
					// E20455	[便区分]整合チェックエラー	 	0	 	E
					var code_chk = $.getInputboxData(that.name, $.id.action_check, id, that.getInputboxParams(that, id, newValue, "MST_CNT"));
					if(code_chk === "0"){
						return "E20455";
					}
					// 16_1 ① 必須。② 1を設置し、変更不可。

					if((that.judgeRepType.frm1||that.judgeRepType.frm2||that.judgeRepType.frm3) && that.judgeRepType.st && (that.judgeRepType.toktg_h||that.judgeRepType.toksp)){
						// 16_2,16_3 ① 必須。② 【画面】.納入情報部分の「PC区分」=０の場合、2は選べない。	「PC区分」=０通常商品　便区分　１：１便、２：2便
						// E20355	PC商品でない場合、2は選択できません。
						if(that.getColValue("F173")==="0" && newValue==="2"){
							return "E20355";
						}
					}
				}
				// 176.別伝区分
				//  ① 入力範囲：0～8
				//  ② 新規の初期値は0である。
				if(allcheck && id===$.id_inp.txt_bdenkbn && !$.isEmptyVal(newValue)){
					// ①E20356	0 ≦ 別伝区分の入力範囲 ≦ 8の条件で入力してください。	 	0	 	E
					if(!(0<=newValue*1 && newValue*1 <=8)){
						return "E20356";
					}
				}
				// 177.ワッペン区分
				//  ① 新規の初期値は0である。
				//  ② DB登録・更新時に、名称コード区分=10666で名称マスタとの整合チェックを行う。
				if(allcheck && id===$.id_inp.txt_wappnkbn && !$.isEmptyVal(newValue)){
					// ②E20456	[ワッペン区分]整合チェックエラー	 	0	 	E
					var code_chk = $.getInputboxData(that.name, $.id.action_check, id, that.getInputboxParams(that, id, newValue, "MST_CNT"));
					if(code_chk === "0"){
						return "E20456";
					}
				}
				// 178.週次伝送flg
				//  ① 新規の場合、初期値は非チェック状態に設置する。
				//  ② PC区分=1の場合、チェック状態で編集不可とする。
				if(allcheck && id===$.id.chk_shudenflg && !$.isEmptyVal(newValue)){
					// E20351	PC商品の場合、週次伝送flgはチェックできません。	 	0	 	E
				}
				// 179.PC区分
				//  ① 【画面】.「商品コード」を入力したタイミングで表示。
				//  ② 【画面】.「商品コード」をクリアしたタイミングでクリア。

				// *** 詳細部分 ***
				// 182.日付
				//  ① 開始日：納入開始日。
				//  ② 終了日：納入終了日 OR 販売終了日の大きい方。ただし、開始日～終了日が11日以上となる場合は11日以降をカットする（最大10日間）。
				//  ③ 納入開始/終了日と販売開始/終了日の定義は、『機能概要説明書』の補足処理部分を参照(5.)する。
				//  ④ 表示フォーマット：MM/DD。
				// 183.曜日	同列【画面】.「日付」の曜日
				// 184.販売日
				// 185.納入日 新規の場合、初期値は非チェック状態に設置する。
				if(allcheck && id===$.id.chk_nndt){
					if((that.judgeRepType.frm1||that.judgeRepType.frm2||that.judgeRepType.frm3) && that.judgeRepType.st && (that.judgeRepType.toktg_h||that.judgeRepType.toksp)){
						//  16_1,16_2,16_3
						//  ① 【画面】.「納入期間」入力がある時のみ入力可。
						//  ② 1日はチェックが必要。
						//  ③ 【画面】.「納入期間」の範囲内
						// E20363	「納入日」に1日はチェックが必要です。	 	0	 	E
						var stdt = $.getInputboxValue($('#'+$.id_inp.txt_nnstdt));
						var eddt = $.getInputboxValue($('#'+$.id_inp.txt_nneddt));
						if($("[id^="+$.id.chk_nndt+"]:checked").length === 0 && (!$.isEmptyVal(stdt) && !$.isEmptyVal(eddt))){
							return "E20363";
						}
					}
				}
				// 186.発注総数
				//  ① 入力範囲：0～99,999。
				//  ② 【画面】.「予定数」とのチェック無し。
				//  ③ 新規の場合、初期値はNULLに設置する。
				if(allcheck && id===$.id_inp.txt_htasu){
					if((that.judgeRepType.frm1||that.judgeRepType.frm2||that.judgeRepType.frm3) && that.judgeRepType.st && (that.judgeRepType.toktg_h||that.judgeRepType.toksp)){
						// 16_1,16_2,16_3
						//  ① 【画面】.納入情報タブの「納入日」チェック時 and 数値展開方法が通常率or実績率パタンの時必須。
						//  ② それ以外は入力不可。
		 				for (var i = 1; i <= 10; i++){
							var chk = $.getInputboxValue($('#'+$.id.chk_nndt+i));
							var val = $.getInputboxValue($('#'+$.id_inp.txt_htasu+i));
							var tenkaikbn = $("input[col=F154]").val();		// F46	TENKAIKBN	展開方法
							if(!$.isEmptyVal(chk, true) && (tenkaikbn===$.id.value_tenkaikbn_tr||tenkaikbn===$.id.value_tenkaikbn_jr)){
								//  E20360	「納入日」がチェックされ、かつ 数値展開方法が通常率or実績率パタンの時、必須です。	 	0	 	E
								if($.isEmptyVal(val)){
									return "E20360";
								}
							}else{
								//  E20357	「納入日」がチェックされた日以外、発注総数は入力できません。	 	0	 	E
								if($.isEmptyVal(chk, true) && !$.isEmptyVal(val)){
									return "E20357";
								}
							}
						}
					}
				}
				// 187.数量計	【画面】.納入情報部分の「発注総数」の期間計。
				// 188.パターン年月表示	『機能概要説明』の「納入情報タブ上のパターンNOと年月表示の制御」（2.1.4.）を参照する。
				// 189.パターンNo.
				if(allcheck && id===$.id_inp.txt_ptnno){
					if((that.judgeRepType.frm1||that.judgeRepType.frm2||that.judgeRepType.frm3) && that.judgeRepType.st && (that.judgeRepType.toktg_h||that.judgeRepType.toksp)){
						// 16_1,16_2,16_3
						//  ① 【画面】.納入情報タブの「納入日」がチェック時必須。
						//  ② それ以外は入力不可。
		 				for (var i = 1; i <= 10; i++){
							var chk = $.getInputboxValue($('#'+$.id.chk_nndt+i));
							var val = $.getInputboxValue($('#'+$.id_inp.txt_ptnno+i));
							if(!$.isEmptyVal(chk, true) && $.isEmptyVal(val)){
								// E20361	「納入日」がチェックされた場合、パターンNo.は必須です。	 	0	 	E
								if(!$.isEmptyVal(val)){
									return "E20361";
								}
							}else if($.isEmptyVal(chk, true) && !$.isEmptyVal(val)){
								//  E20358	「納入日」がチェックされた日以外、パターンNo.は入力できません。	 	0	 	E
								if($.isEmptyVal(val)){
									return "E20358";
								}
							}
						}
					}
				}

				// 190.訂正区分
				//  ① 入力範囲：0～4。
				//  ② 新規の場合、初期値は0に設置する。
				if(allcheck && id===$.id_inp.txt_tseikbn){
					if((that.judgeRepType.frm1||that.judgeRepType.frm2||that.judgeRepType.frm3) && that.judgeRepType.st && (that.judgeRepType.toktg_h||that.judgeRepType.toksp)){
						// 16_1,16_2,16_3
						//  ① 【画面】.納入情報タブの「納入日」がチェック時必須。
						//  ② それ以外は入力不可。
		 				for (var i = 1; i <= 10; i++){
							var chk = $.getInputboxValue($('#'+$.id.chk_nndt+i));
							var val = $.getInputboxValue($('#'+$.id_inp.txt_tseikbn+i));
							if(!$.isEmptyVal(chk, true) && $.isEmptyVal(val)){
								// E20362	「納入日」がチェックされた場合、訂正区分は必須です。 	 	0	 	E
								if(!$.isEmptyVal(val)){
									return "E20362";
								}
							}else if($.isEmptyVal(chk, true) && !$.isEmptyVal(val)){
								//  E20492	納入期間が設定されていない場合、訂正区分は入力できません。	 	0	 	E
								if($.isEmptyVal(val)){
									return "E20492";
								}
							}
						}
					}
				}
			}

			if (allcheck && id===$.id.rad_tkanplukbn && (that.judgeRepType.frm3 || that.judgeRepType.frm2)) {
				var kbn = $.getInputboxValue($('[name='+$.id.rad_tkanplukbn+']').eq(0));	// that.grd_data['F57']

				if ($.isEmptyVal(kbn)) {
					return "E20549";
				}

				if (!$.isEmptyVal(that.getColValue("F10")) && kbn!==that.getColValue("F176")) {
					return "E20605";
				}
			}


			// 191.店舗数 計算方法は、『機能概要説明』の"「納入情報」タブの「再計算」ボタン機能説明"を参照する。
			// 192.展開数
			// 193.数量差
			// 194.原価計
			// 195.売価計
			// 196.本売価計
			// 197.荒利額
			// 198.期間計	【画面】.当行左部分の累計値。

			return null;
		},
		// 3.10．「納入情報」タブの「再計算」ボタン機能
		calcRe: function(){
			var that = this;
			if(!that.judgeRepType.tabnn) return false;
			// 3.10.1．「店舗数」、「展開数」、「数量差」、「原価計」、「本売価計」、「荒利額計」を再計算して表示 納入数展開等の処理を行うが、DBへの保存は行わない
			// 3.10.2．展開方法は、『特売共通仕様書 数量計算方法』部分を参照
			// 3.10.3．計算項目：
			// ①「店舗数」：展開後の発注数≧0の店舗数（計算ボタンをクリックにて、新規、更新処理の対象店舗取得処理を行う）
			var sum = 0;
			$("[col^=N5_]").not("[col=N5_11]").each(function(){
				var col = $(this).attr('col');
				var val = that.getColValue(col,true)*1;
				if (!$.isEmptyVal(val)) {
					sum += val;
				}
			});
			$.setInputboxValue($("[col^=N5_11]"), $.getFormat(sum, '#,##0'));

			// ②「展開数」：展開後の店別発注数の合計。「店別数量」で修正した場合はその値の合計（「店別数量」で修正した場合はその値を用い、展開関連値（発注総数、パターンNo.）を修正した場合は店別数量を使わず再展開を行い、納入日毎に合計値を求める。）
			// TODO
			sum = 0;

			$("[col^=N10_]").not("[col=N10_11]").each(function(){
				var col = $(this).attr('col');
				var val = that.getColValue(col,true)*1;
				if (!$.isEmptyVal(val)) {
					sum += val;
				}
			});
			$.setInputboxValue($("[col^=N10_11]"), $.getFormat(sum, '#,##0'));

			// ③「数量差」：「発注総数」－「展開数」。
			sum = 0;
			$("[col^=N11_]").not("[col=N11_11]").each(function(){
				var idx = $(this).attr("col").replace("N11_","");
				var val = "";
				if(that.getColValue("N10_"+idx)!==""){
					val = that.getColValue("N5_"+idx, true)*1 - that.getColValue("N10_"+idx, true)*1;
					$.setInputboxValue($(this), $.getFormat(val, '#,##0'));
					sum += val;
				} else {
					$.setInputboxValue($(this), val);
				}
			});
			$.setInputboxValue($('[col=N11_11]'), $.getFormat(sum, '#,##0'));
			// ④「原価計」：【画面】.特売事前行の「原価」（精肉・鮮魚画面に「不定貫商品」を選択した場合、原価は【画面】.A総売価行の「Ｐ原価」を使用）×展開数×【画面】.「特売事前入数」（不定貫時は【画面】.「A総売価行入数」）
			// ⑤「本売価計」：【画面】.特売事前行の「本体売価」（精肉・鮮魚画面に「不定貫商品」を選択した場合、売価は【画面】. A総売価行の「Ｐ総売価」－税額を使用）×展開数×【画面】.「特売事前入数」（不定貫時は【画面】.「A総売価行入数」）
			// 　（税額の計算は、『特売共通仕様書 総額売価計算方法』部分参照）
			var tkanplukbn =  that.getColValue("F57");
			var genka,baika,irisu, sum;
			if(tkanplukbn==='2' && (that.judgeRepType.frm2||that.judgeRepType.frm3)){
				genka = that.getColValue("F84", true)*1;
				baika = that.getColValue("F85", true)*1;
				irisu = that.getColValue("F86", true)*1;
			}else{
				genka = that.getColValue("F63", true)*1;
				baika = that.getColValue("F65", true)*1;
				irisu = that.getColValue("F66", true)*1;
			}
			sum = 0;
			$("[col^=N12_]").not("[col=N12_11]").each(function(){
				var idx = $(this).attr("col").replace("N12_","");
				var val = "";
				if(that.getColValue("N10_"+idx)!==""){
					val = genka*that.getColValue("N10_"+idx, true)*irisu;
					$.setInputboxValue($(this), $.getFormat(val, '#,##0'));
					sum += val;
				} else {
					$.setInputboxValue($(this), val);
				}
			});
			$.setInputboxValue($('[col=N12_11]'), $.getFormat(sum, '#,##0'));
			sum = 0;
			$("[col^=N13_]").not("[col=N13_11]").each(function(){
				var idx = $(this).attr("col").replace("N13_","");
				var val = "";
				if(that.getColValue("N10_"+idx)!==""){
					val = baika*that.getColValue("N10_"+idx, true)*irisu;
					$.setInputboxValue($(this), $.getFormat(val, '#,##0'));
					sum += val;
				} else {
					$.setInputboxValue($(this), val);
				}
			});
			$.setInputboxValue($('[col=N13_11]'), $.getFormat(sum, '#,##0'));
			// ⑥ 「荒利額」：「本売価計」－「原価計」。
			sum = 0;
			$("[col^=N14_]").not("[col=N14_11]").each(function(){
				var idx = $(this).attr("col").replace("N14_","");
				var val = "";
				if(that.getColValue("N10_"+idx)!==""){
					val = that.getColValue("N13_"+idx, true)*1 - that.getColValue("N12_"+idx, true)*1;
					$.setInputboxValue($(this), $.getFormat(val, '#,##0'));
					sum += val;
				} else {
					$.setInputboxValue($(this), val);
				}
			});
			$.setInputboxValue($('[col=N14_11]'), $.getFormat(sum, '#,##0'));
		},
		calcNeireRit: function(baika, genka){
			if(baika.length===0) return "";
			if(genka.length===0) return "";
			baika = baika.replace(/,/g,"")*1;
			genka = genka.replace(/,/g,"")*1;

			// （本体売価－原価）÷本体売価で、小数点以下3位切り捨て, 第2位まで求める。上限98%
			// ただし、商品種別で包材、消耗品、コメント、催事テナントの時はチェックしない。
			var value = $.floorDecimal((baika-genka)/baika*100, 2);
			return $.getFormat(value, '#,##0.00');
		},
		calcAvg: function(baika, tensu){
			if(baika.length===0) return "";
			if(tensu.length===0) return "";
			baika = baika.replace(/,/g,"")*1;
			tensu = tensu.replace(/,/g,"")*1;
			var value = $.floorDecimal(baika/tensu, 2);
			return value;
		},
		getInputboxParams: function(that, id, newValue, key){
			// 情報取得
			var values = {};
		  values["value"] = newValue;
			//values["value"] = that.jsonHidden[9].value;
			if(key){
				values["KEY"] = key;
			}
			// 基本情報
			values["MOYSKBN"] = $.getJSONValue(this.jsonHidden, $.id_inp.txt_moyskbn);
			values["MOYSSTDT"] = $.getJSONValue(this.jsonHidden, $.id_inp.txt_moysstdt);
			values["MOYSRBAN"] = $.getJSONValue(this.jsonHidden, $.id_inp.txt_moysrban);
			values["BMNCD"] = $.getJSONValue(this.jsonHidden, $.id_inp.txt_bmncd);
			values["ADDSHUKBN"] = $.getJSONValue(this.jsonHidden, $.id_inp.txt_addshukbn);

			if(id===$.id.txt_tok_honbaika||id===$.id.txt_tok_soubaika){
				values["SHNCD"] = $.getInputboxValue($('#'+$.id_inp.txt_shncd));
			}
			// 全店割引
			if(id==="TOKRS_KKK"){
				values["SHNCD"] = $.getInputboxValue($('#'+$.id_inp.txt_shncd));
				values["HBSTDT"]= $.getInputboxValue($('#'+$.id_inp.txt_hbstdt));
			}

			// 情報設定
			return [values];
		}
	} });
})(jQuery);