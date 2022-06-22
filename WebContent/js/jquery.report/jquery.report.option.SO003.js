/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_ReportSO003',			// （必須）レポートオプションの確認
		jsonTemp:	[],							// （必須）検索条件情報_入力チェック前
		jsonString:	[],							// （必須）検索条件情報
		jsonStringCsv:	[],						// （CSV出力用）検索条件情報
		jsonHidden: [],							// （必須）親画面からの引き継ぎ情報
		jsonInit: [],							// （必須）検索条件初期情報
		caption: function(){					// （必須）タイトル
			return $('#reportname').val();
		},
		sortName: '',	// ソート項目名
		sortOrder: '',	// ソート順
		timeData : (new Date()).getTime(),
		dedefaultObjNum:	28,	// 初期化オブジェクト数
		initObjNum:	-1,
		initedObject: [],
		maxMergeCell: 1,
		onChangeFlag : false,
		onChangeFlag2 : false,
		columnName:'',	// OnClickRowの列名
		queried : false,
		initializes : true,
		onChangeReport: true,
		reportYobiInfo: function(){			// （必須）レポートメニューに登録された予備情報＝帳票をどういった表示にしたいかの情報
			return $('#reportYobi1').val();},
		reportYobiInfo2: function(){
			return $('#reportYobi2').val();},
		pushBtnId: "",						// （必須）実行ボタンID情報(検索系で利用)
		focusRootId:"cc",					// （キー移動時必須）キー移動イベントのルートとなるパネルのID
		focusParentId:"",					// （キー移動時必須）現在フォーカスがあたっている項目の親となるパネルのID
		focusGridId:"",						// （キー移動時必須）現在フォーカスがあたっているDataGridのID
		editRowIndex:{},					// グリッド編集行保持
		gridData:[],						// 検索結果
		gridTitle:[],						// 検索結果
		tortalRows:0,						// 検索結果件数
		btn_enter_index:null,
		editable:true,
		isTest:true,
		dispType:"",
		checkFlg:true,
		initialize: function (reportno){	// （必須）初期化
			var that = this;

			// 引き継ぎ情報
			this.jsonHidden = $.getTargetValue();
			// 初期検索条件設定
			this.jsonInit = $.getInitValue();
			// 画面の初回基本設定
			this.setInitObjectState();

			// データ表示エリア初期化
			//that.setGrid($.id.gridholder, reportno);
			that.setGrid('gridholder', reportno);

			var isUpdate = true;

			// 初期化するオブジェクト数設定
			this.initObjNum = this.dedefaultObjNum;
			// 入力テキストボックス系
			var inputbox = Object.getOwnPropertyNames($.id_inp);
			var isSearchId = [$.id_inp.txt_stym, $.id_inp.txt_enym];
			for ( var sel in inputbox ) {
				if($('#'+$.id_inp[inputbox[sel]]).length > 0){
					$.setInputbox(that, reportno, $.id_inp[inputbox[sel]], isUpdate);
				}
			}

			var isUpdate = true;

			// 各値入率
			$.setInputbox(that, reportno, 'txt_hs_neire_a', isUpdate);
			$.setInputbox(that, reportno, 'txt_hs_neire_b', isUpdate);
			$.setInputbox(that, reportno, 'txt_hs_neire_c', isUpdate);

			// 各本売価
			$.setInputbox(that, reportno, 'txt_tok_honbaika_a', isUpdate);
			$.setInputbox(that, reportno, 'txt_tok_honbaika_b', isUpdate);
			$.setInputbox(that, reportno, 'txt_tok_honbaika_c', isUpdate);

			$.setCheckboxInit2(that.jsonHidden, $.id.chk_del, that);


			// Load処理回避
			//$.tryChangeURL(null);

			// 初期化終了
			this.initializes =! this.initializes;

			$.initialDisplay(that);

			// キーイベントの設定
			$.initKeyEvent(that);

			// ログ出力
			$.log(that.timeData, 'initialize:');
		},
		setInitObjectState: function(){	// 画面初期化時の項目制御
			var that = this;
			// 引き継ぎ情報セット
			var sendBtnid = $.getJSONValue(that.jsonHidden, "sendBtnid");
			if(sendBtnid && sendBtnid.length > 0){
				that.sendBtnid = sendBtnid;
				$.reg.search = true;
			}

			var reportYobi1 = $.getJSONValue(that.jsonHidden, "reportYobi1");
			if(!reportYobi1){
				reportYobi1 = $('#reportYobi1').val();
			}
			$('#reportYobi1').val(reportYobi1);

			// 各種ボタン
			$('#'+$.id.btn_new).on("click", $.pushChangeReport);
			$('#'+$.id.btn_sel_change).on("click", $.pushChangeReport);
			$('#'+$.id.btn_sel_refer).on("click", $.pushChangeReport);

			var callpage = $.getJSONValue(that.jsonHidden, "callpage");
			// 転送先情報
			if(callpage==='Out_ReportSO006' || callpage==='Out_ReportSO004'){
				$.initReportInfo("SO005", "CSV取込　エラー修正", "修正");
				that.dispType = "SO005"

				$("#"+$.id.btn_del).linkbutton('disable');
				$("#"+$.id.btn_del).attr('tabindex', -1).hide();
				$("#selectLastrow").linkbutton('disable');
				$("#selectLastrow").attr('tabindex', -1).hide();

				$("#"+$.id.btn_csv).linkbutton('disable');
				$("#"+$.id.btn_csv).attr('tabindex', -1).hide();
				$("#"+$.id.btn_cancel).linkbutton('disable');
				$("#"+$.id.btn_cancel).attr('tabindex', -1).hide();



			}else if(that.reportYobiInfo()==='1'){
				$.initReportInfo("SO003", "生活応援　参照　商品一覧", "参照");
				that.dispType = "SO003_REF"
				$("#"+$.id.btn_del).linkbutton('disable');
				$("#"+$.id.btn_del).attr('tabindex', -1).hide();
				$("#selectLastrow").linkbutton('disable');
				$("#selectLastrow").attr('tabindex', -1).hide();

				$('#'+$.id.btn_cancel).on("click", $.pushChangeReport);

			}else if(that.reportYobiInfo()==='0'){
				$.initReportInfo("SO003", "生活応援　新規・変更　商品一覧", "新規・登録");
				that.dispType = "SO003"
				// 各種遷移ボタン
				$('#'+$.id.btn_cancel).on("click", $.pushChangeReport);

			}

			// 最下行ボタン
			$('#selectLastrow').on("click", function(e){
				var maxIndex = (that.tortalRows - 1);

				var row = $($.id.gridholder).datagrid('getData').firstRows[maxIndex];
				if(row.F1 && row.F1 != ""){
					// 新規行が入力済みだった場合は、新規行を追加する。
					$($.id.gridholder).datagrid('appendRow',{});
					that.tortalRows += 1;
					maxIndex += 1
				}

				$($.id.gridholder).datagrid('scrollTo', {
					index: maxIndex,
					callback: function(index){
						$($.id.gridholder).datagrid('selectRow', index);
						$($.id.gridholder).datagrid('beginEdit', index);
					}
				});
			});

			// CSV出力イベント設定
			//$('#'+$.id.btn_csv).on("click", that.pushCsv_test);
			$('#'+$.id.btn_csv).on("click", function(e){alert('現在CSV出力機能は停止中です。');});

		},

		initCondition: function (){	// 条件初期値セット
			var that = this;
			// 初期化項目
		},
		clear:function(){
			// 隠し情報初期化
			$($.id.hiddenChangedIdx).val("");						// 変更行Index
			// グリッド初期化
			//this.success(this.name, false);
		},
		validation: function (btnId){	// （必須）批准
			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform).form('validate');
			// 入力エラーなしの場合に検索条件を格納
			//if (rt == true) that.jsonString = that.jsonTemp.slice(0);

			// 入力エラーなしの場合に検索条件を格納
			if(btnId===$.id.btn_csv){
				if (rt == true) that.jsonStringCsv = that.jsonTemp.slice(0);
			}else{
				if (rt == true) that.jsonString = that.jsonTemp.slice(0);
			}

			// 入力チェック用の配列をクリア
			that.jsonTemp = [];
			return rt;
		},
		success: function(reportno, sortable){	// （必須）正処理
			if (sortable) sortable=1; else sortable=0;
			var that = this;
			// 検索実行
			var szMoyskbn	= $.getJSONObject(this.jsonString, $.id_inp.txt_moyskbn).value;		// 催し区分
			var szMoysstdt	= $.getJSONObject(this.jsonString, $.id_inp.txt_moysstdt).value;	// 催しコード（催し開始日）
			var szMoysrban	= $.getJSONObject(this.jsonString, $.id_inp.txt_moysrban).value;	// 催し連番
			var szBmncd		= $.getJSONObject(this.jsonString, $.id_inp.txt_bmncd).value;		// 部門コード
			var szSeq		= $.getJSONObject(this.jsonString, $.id.txt_seq).value;				// SEQ


			// initialDisplayでのMaskMsgを削除
			$.removeMaskMsg();

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			$($.id.gridholder).datagrid('loading');

			// grid.options 取得
			var options = $($.id.gridholder).datagrid('options');
			that.sortName	= options.sortName;
			that.sortOrder	= options.sortOrder;

			$.post(
				$.reg.jqgrid ,
				{
					report			:that.name,					// レポート名
					SENDBTNID		:that.sendBtnid,
					YOBIINFO		:that.reportYobiInfo(),
					MOYSKBN:		szMoyskbn,		// 催し区分
					MOYSSTDT:		szMoysstdt,		// 催しコード（催し開始日）
					MOYSRBAN:		szMoysrban,		// 催し連番
					BMNCD:			szBmncd,		// 部門コード
					SEQ:			szSeq,			// SEQ
					t				:(new Date()).getTime(),
					sortable		:sortable,
					sortName		:that.sortName,
					sortOrder		:that.sortOrder,
					rows			:0				// 表示可能レコード数
				},
				function(json){
					// 検索処理エラー判定
					if($.searchError(json)) return false;

					var jsonp = JSON.parse(json);
					// 検索件数保持
					that.tortalRows = jsonp.total;

					// ログ出力
					$.log(that.timeData, 'query:');

					// Load処理回避
					$.tryChangeURL(null);

					var opts = JSON.parse(json).opts
					if(opts && opts.rows_){
						// 基本データ表示
						that.setData(opts.rows_, opts);
					}

					if (sortable===0){
						var options = $($.id.gridholder).datagrid('options');
						// 初期検索時に並び替え情報のリセット
						options.sortName = null;
						options.sortOrder = null;
					}

					// Load処理回避
					$.tryChangeURL($.reg.jqeasy);

					// グリッド再描画（easyui 1.4.2 対応）
					$($.id.gridholder).datagrid('load', {} );
					$.removeMask();

					that.queried = true;

					// 状態保存
					$.saveState2(reportno, that.getJSONString());

					// ログ出力
					$.log(that.timeData, 'loaded:');
				}
			);
		},
		updValidation: function (id){	// （必須）批准
			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform).form('validate');
			if(!rt){
				$.addErrState(that, $('.validatebox-invalid').eq(0), false);
				return rt;
			}

			// 入力編集を終了する。
			var row = $($.id.gridholder).datagrid("getSelected");
			var rowIndex = $($.id.gridholder).datagrid("getRowIndex", row);
			$($.id.gridholder).datagrid('endEdit',rowIndex);

			// Grid編集行情報を初期化
			that.editRowIndex['gridholder'] = -1;

			var index = id.replace("btn_enter_", "");
			that.btn_enter_index = index
			var row = $($.id.gridholder).datagrid('getRows')[that.btn_enter_index];
			// TODO
			// 表示行数が多すぎる場合、getRowsでは取得できない為、getDataから取得を行う。
			if(!row){
				row = $($.id.gridholder).datagrid('getData').firstRows[that.btn_enter_index]
			}
			//var row = $($.id.gridholder).datagrid("getSelected");
			that.checkFlg = true
			if(row.DEL == '1'){
				that.checkFlg = false
			}

			if(that.checkFlg){
				// 行 の 'validate' 実施
				var rt = $($.id.gridholder).datagrid('validateRow', index);
				if(!rt){
					$.addErrState(that, $('.validatebox-invalid').eq(0), false);
					return rt;
				}

				// 商品一覧
				var targetdate = [];
				var targetRows = $($.id.gridholder).datagrid('getRows');
				if(!targetRows[that.btn_enter_index]){
					// TODO
					// 表示行数が多すぎる場合、getRowsでは取得できない為、getDataから取得を行う。
					// targetRows = $($.id.gridholder).datagrid('getData').firstRows[that.btn_enter_index]
					targetRows = $($.id.gridholder).datagrid('getData').firstRows
				}
				for (var i=0; i<targetRows.length; i++){
					targetdate.push(targetRows[i]["F1"]);
				}
				// 重複チェック：商品コード
				var targetdateF = []
				targetdate.filter(function (x, i, self) {
					if(x && x != ""){
			            if(Number(self.indexOf(x)) !== i || Number(self.lastIndexOf(x)) !== i){
			            	targetdateF.push(i)
			            };
					}
		        });

				// グリッド内の塗り潰し状態をクリアする。
				$.removeErrStateGrid('gridholder');

				if(targetdateF.length > 0){
					// 重複箇所を塗り潰し
					var targetColIndex = 3		// 商品コードの項目順番
					$.addErrStateGrid('gridholder', targetdateF, [targetColIndex]);
					$.showMessage('E20258');
					return false;
				}

				// 存在チェック：商品マスタ
				var shncd = row.F1		// 商品コード
				var msgid = that.checkInputboxFunc($.id_inp.txt_shncd, shncd , '');
				if(msgid !==null){
					$.showMessage(msgid,undefined, function(){$.addErrState(that, $('#'+that.focusGridId), true, {NO:that.btn_enter_index, ID:$.id_inp.txt_shncd + '_'})});
					return false;
				}

				// 部門コードとの相互チェック
				var bmncd		 = $.getInputboxValue($('#'+$.id_inp.txt_bmncd)).split("-")[0];		// 部門コード
				if(shncd.substring(0, 2) != bmncd){
					$.showMessage("E11162",undefined, function(){$.addErrState(that, $('#'+that.focusGridId), true, {NO:that.btn_enter_index, ID:$.id_inp.txt_shncd + '_'})});
					return false;
				}

				// 必須入力チェック
				var shncd	 = row.F1			// 商品コード
				var shnkn	 = row.F3.trim()	// 商品名称
				var irisu	 = row.F6			// 生活応援入数
				var minsu	 = row.F7			// 最低発注数
				var genka	 = row.F9			// 生活応援原価
				var a_baika	 = row.F10			// A総売価
				var b_baika	 = row.F14			// B総売価
				var c_baika	 = row.F18			// C総売価
				var a_rank	 = row.F12			// Aランク
				var b_rank	 = row.F16			// Bランク
				var c_rank	 = row.F20			// Cランク
				var popcd	 = row.F22			// POPコード
				var popsize	 = row.F23			// POPサイズ
				var popsu	 = row.F24			// 枚数
				var rg_baika = row.F35			// レギュラー標準売価

				if(!shncd || shncd == ''){
					$.showMessage('EX1103',['商品コード'], function(){$.addErrState(that, $('#'+that.focusGridId), true, {NO:that.btn_enter_index, ID:$.id_inp.txt_shncd + '_'})});
					return false;
				}
				if(!shnkn || shnkn == ''){
					$.showMessage('EX1103',['商品名称'], function(){$.addErrState(that, $('#'+that.focusGridId), true, {NO:that.btn_enter_index, ID:$.id_inp.txt_shnkn + '_'})});
					return false;
				}
				if(!irisu || irisu == ''){
					$.showMessage('EX1103',['生活応援入数'], function(){$.addErrState(that, $('#'+that.focusGridId), true, {NO:that.btn_enter_index, ID:$.id_inp.txt_irisu + '_'})});
					return false;
				}
				if(!minsu || minsu == ''){
					$.showMessage('EX1103',['最低発注数'], function(){$.addErrState(that, $('#'+that.focusGridId), true, {NO:that.btn_enter_index, ID:$.id_inp.txt_minsu + '_'})});
					return false;
				}
				if(!genka || genka == ''){
					$.showMessage('EX1103',['生活応援原価'], function(){$.addErrState(that, $('#'+that.focusGridId), true, {NO:that.btn_enter_index, ID:$.id_inp.txt_genkaam + '_'})});
					return false;
				}
				if(!a_baika || a_baika == ''){
					$.showMessage('EX1103',['A総売価'], function(){$.addErrState(that, $('#'+that.focusGridId), true, {NO:that.btn_enter_index, ID:$.id_inp.txt_a_baikaam + '_'})});
					return false;
				}
				if(!a_rank || a_rank == ''){
					$.showMessage('EX1103',['Aランク'], function(){$.addErrState(that, $('#'+that.focusGridId), true, {NO:that.btn_enter_index, ID:$.id_inp.txt_a_rankno + '_'})});
					return false;
				}

				// 存在チェック
				// Aランク
				if(a_rank && a_rank !== ''){
					var msgid = that.checkInputboxFunc($.id_inp.txt_a_rankno, a_rank , '');
					if(msgid !==null){
						$.showMessage(msgid,undefined, function(){$.addErrState(that, $('#'+that.focusGridId), true, {NO:that.btn_enter_index, ID:$.id_inp.txt_a_rankno + '_'})});
						return false;
					}
				}
				// Bランク
				if(b_rank && b_rank !== ''){
					var msgid = that.checkInputboxFunc($.id_inp.txt_b_rankno, b_rank , '');
					if(msgid !==null){
						$.showMessage(msgid,undefined, function(){$.addErrState(that, $('#'+that.focusGridId), true, {NO:that.btn_enter_index, ID:$.id_inp.txt_b_rankno + '_'})});
						return false;
					}
				}
				// Cランク
				if(c_rank && c_rank !== ''){
					var msgid = that.checkInputboxFunc($.id_inp.txt_c_rankno, c_rank , '');
					if(msgid !==null){
						$.showMessage(msgid,undefined, function(){$.addErrState(that, $('#'+that.focusGridId), true, {NO:that.btn_enter_index, ID:$.id_inp.txt_c_rankno + '_'})});
						return false;
					}
				}

				// 相互入力チェック
				// B総売価
				if(b_baika && b_baika !== ''){
					if(!b_rank || b_rank == ''){
						$.showMessage('E20049',undefined, function(){$.addErrState(that, $('#'+that.focusGridId), true, {NO:that.btn_enter_index, ID:$.id_inp.txt_b_rankno + '_'})});
						return false;
					}
				}
				// Bランク
				if(b_rank && b_rank !== ''){
					if(!b_baika || b_baika == ''){
						$.showMessage('E20049',undefined, function(){$.addErrState(that, $('#'+that.focusGridId), true, {NO:that.btn_enter_index, ID:$.id_inp.txt_b_baikaam + '_'})});
						return false;
					}
				}
				// C総売価
				if(c_baika && c_baika !== ''){
					if(!c_rank || c_rank == ''){
						$.showMessage('E20054',undefined, function(){$.addErrState(that, $('#'+that.focusGridId), true, {NO:that.btn_enter_index, ID:$.id_inp.txt_c_rankno + '_'})});
						return false;
					}
				}
				// Cランク
				if(c_rank && c_rank !== ''){
					if(!c_baika || c_baika == ''){
						$.showMessage('E20054',undefined, function(){$.addErrState(that, $('#'+that.focusGridId), true, {NO:that.btn_enter_index, ID:$.id_inp.txt_c_baikaam + '_'})});
						return false;
					}
					if(!b_rank || b_rank == ''){
						// Bランク未入力の場合
						$.showMessage('E20045',undefined, function(){$.addErrState(that, $('#'+that.focusGridId), true, {NO:that.btn_enter_index, ID:$.id_inp.txt_b_rankno + '_'})});
						return false;
					}
				}

				rg_baika = rg_baika == "" ? 0 : Number(rg_baika);
				a_baika	 = a_baika == "" ? 0 : Number(a_baika);
				b_baika	 = b_baika == "" ? 0 : Number(b_baika);
				c_baika	 = c_baika == "" ? 0 : Number(c_baika);

				if(rg_baika < a_baika){
					$.showMessage('E20564',undefined, function(){$.addErrState(that, $('#'+that.focusGridId), true, {NO:that.btn_enter_index, ID:$.id_inp.txt_a_baikaam + '_'})});
					return false;
				}

				if(a_baika < b_baika || b_baika < c_baika){
					$.showMessage('E20052',undefined, function(){$.addErrState(that, $('#'+that.focusGridId), true, {NO:that.btn_enter_index, ID:$.id_inp.txt_a_baikaam + '_'})});
					return false;
				}

				if(a_baika == b_baika && b_baika == c_baika){
					$.showMessage('E20053',undefined, function(){$.addErrState(that, $('#'+that.focusGridId), true, {NO:that.btn_enter_index, ID:$.id_inp.txt_a_baikaam + '_'})});
					return false;
				}

				// POP系項目
				if((popcd && popcd != '') || (popsize && popsize != '')){
					// POPコード、POPサイズのいずれかが入力された場合
					if(popsu && popsu !== ''){
						if(Number(popsu) < 1){
							$.showMessage('E20531',undefined, function(){$.addErrState(that, $('#'+that.focusGridId), true, {NO:that.btn_enter_index, ID:$.id_inp.txt_popsu + '_'})});
							return false;
						}
					}else{
						$.showMessage('E20531',undefined, function(){$.addErrState(that, $('#'+that.focusGridId), true, {NO:that.btn_enter_index, ID:$.id_inp.txt_popsu + '_'})});
						return false;
					}
				}

				if((!popcd || popcd == '') && (!popsize || popsize == '')){
					// POPコード、POPサイズのどちらも入力がない場合
					if(popsu && popsu !== ''){
						$.showMessage('E20055',undefined, function(){$.addErrState(that, $('#'+that.focusGridId), true, {NO:that.btn_enter_index, ID:$.id_inp.txt_popsu + '_'})});
						return false;
					}
				}
			}
			return rt;
		},
		updConfirm: function(func){
			var that = this;

			if(!that.checkFlg){
				$.showMessage('W00001', undefined, func);
				return false;
			}

			var row = $($.id.gridholder).datagrid('getRows')[that.btn_enter_index];		// 登録データ
			// TODO
			// 表示行数が多すぎる場合、getRowsでは取得できない為、getDataから取得を行う。
			if(!row){
				row = $($.id.gridholder).datagrid('getData').firstRows[that.btn_enter_index]
			}
			var plusflg	 = $.getInputboxValue($('#txt_plusflg'));						// PLU配信済フラグ

			var msgids	 = [];
			var param	 = [];
			//「生活応援入数がレギュラー入数と異なっています。登録しますか?」（警告）を「はい」「いいえ」のダイアログで表示し、処理続行を確認する。
			if(row.F5 !== row.F6){
				// W20035	生活応援入数がレギュラー入数と異なっています。登録しますか?	 	4	 	Q
				msgids.push("W20035");
				param.push(undefined);
			}
			//「催しの店舗配信済みのため、店舗に反映されません。登録しますか？」（警告）を「はい」「いいえ」のダイアログで表示し、処理続行を確認する。
			if(row.F30 == '' && plusflg == '1'){
				// W20017	催しの店舗配信済みのため、店舗に反映されません。登録しますか？	 	4	 	Q
				msgids.push("E20059");
				param.push(undefined);
			}
			//「催しの店舗配信済みのため、変更しても店舗に反映されません。更新しますか？」（警告）を「はい」「いいえ」のダイアログで表示し、処理続行を確認する。
			if(row.F30 == '0' && plusflg == '1'){
				// E20271	催しの店舗配信済みのため、変更しても店舗に反映されません。更新しますか？	 	4	 	Q
				msgids.push("E20271");
				param.push("");
			}

			var neir_a = row.F13;
			var neir_b = row.F17;
			var neir_c = row.F21;
			var shincd = row.F1

			if(neir_a && neir_a !== ""){
				if( Number(neir_a) < 5 || Number(neir_a) > 90){
					msgids.push("W20024");
					param.push([shincd]);
				}
			}
			if(neir_b && neir_b !== ""){
				if( Number(neir_b) < 5 || Number(neir_b) > 90){
					msgids.push("W20025");
					param.push([shincd]);
				}
			}
			if(neir_c && neir_c !== ""){
				if( Number(neir_c) < 5 || Number(neir_c) > 90){
					msgids.push("W20026");
					param.push([shincd]);
				}
			}

			var func_ok = null;
			if(msgids.length === 0){
				msgId = 'W00001';
				func_ok = func;
			}else if(msgids.length === 1){
				msgId = msgids[0];
				func_ok = func;
			}else if(msgids.length === 2){
				msgId = msgids[0];
				func_ok = function(r){
					$.showMessage(msgids[1], [param[1]], func);
				};
			}else if(msgids.length === 3){
				msgId = msgids[0];
				func_ok = function(r){
					var func_ok_ = function(r){
						$.showMessage(msgids[2], param[2], func);
					};
					$.showMessage(msgids[1], param[1], func_ok_);
				};
			}else if(msgids.length === 4){
				msgId = msgids[0];
				func_ok = function(r){
					var func_ok_ = function(r){
						var func_ok_ = function(r){

							$.showMessage(msgids[3], param[3], func);
						}
						$.showMessage(msgids[2], param[2], func_ok_);
					};
					$.showMessage(msgids[1], param[1], func_ok_);
				};
			}else if(msgids.length === 5){
				msgId = msgids[0];
				func_ok = function(r){
					var func_ok_ = function(r){
						var func_ok_ = function(r){
							var func_ok_ = function(r){
								$.showMessage(msgids[4], param[4], func);
							};
							$.showMessage(msgids[3], param[3], func_ok_);
						};
						$.showMessage(msgids[2], param[2], func_ok_);
					};
					$.showMessage(msgids[1], param[1], func_ok_);
				};
			}else if(msgids.length === 6){
				msgId = msgids[0];
				func_ok = function(r){
					var func_ok_ = function(r){
						var func_ok_ = function(r){
							var func_ok_ = function(r){
								var func_ok_ = function(r){
									$.showMessage(msgids[5], param[5], func);
								};
								$.showMessage(msgids[4], param[4], func_ok_);
							};
							$.showMessage(msgids[3], param[3], func_ok_);
						};
						$.showMessage(msgids[2], param[2], func_ok_);
					};
					$.showMessage(msgids[1], param[1], func_ok_);
				};
			}
			$.showMessage(msgId, param[0], func_ok);
		},
		delConfirm: function(func){
			var that = this;

			var plusflg	 = $.getInputboxValue($('#txt_plusflg'));			// PLU配信済フラグ

			var msgids = [];
			//「催しの店舗配信済みのため、変更しても店舗に反映されません。削除しますか？」（警告）を「はい」「いいえ」のダイアログで表示し、処理続行を確認する。
			if(plusflg == '1'){
				// W20019	催しの店舗配信済みのため、変更しても店舗に反映されません。削除しますか？	 	4	 	Q
				msgids.push("W20019");
			}

			var func_ok = null;
			if(msgids.length === 0){
				msgId = 'W00001';
				func_ok = func;
			}else if(msgids.length === 1){
				msgId = msgids[0];
				func_ok = func;
			}else if(msgids.length === 2){
				msgId = msgids[0];
				func_ok = function(r){
					$.showMessage(msgids[1], undefined, func);
				};
			}else if(msgids.length === 3){
				msgId = msgids[0];
				func_ok = function(r){
					var func_ok_ = function(r){
						$.showMessage(msgids[2], undefined, func);
					};
					$.showMessage(msgids[1], undefined, func_ok_);
				};
			}
			$.showMessage(msgId, undefined, func_ok);
		},
		updSuccess: function(id){	// validation OK時 の update処理
			var that = this;

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$($.id.gridholder).datagrid('loading');

			var index = id.replace("btn_enter_", "");

			// 変更行情報取得
			var changedIndex = $($.id.hiddenChangedIdx).val().split(",");

			// 基本入力情報取得
			var targetData = that.getGridData("data")["data"];

			// 商品一覧
			var targetDataShn = that.getGridData(id)[id];

			// 商品一覧(削除)
			var targetDataShnDel = that.getGridData(id,"del")[id];

			// CSV修正削除データ
			var targetDataCSVDel = that.getGridDataSO005(id)[id]

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,			// レポート名
					action:			$.id.action_update,	// 実行処理情報
					obj:			id,					// 実行オブジェクト
					SENDBTNID:		that.sendBtnid,
					DISPTYPE:		that.dispType,
					DATA:			JSON.stringify(targetData),				// 更新対象情報
					DATA_SHN:		JSON.stringify(targetDataShn),			// 更新対象情報
					DATA_SHN_DEL:	JSON.stringify(targetDataShnDel),		// 更新対象情報
					DATA_CSV_DEL:	JSON.stringify(targetDataCSVDel),
					t:				(new Date()).getTime()
				},
				function(data){
					// 検索処理エラー判定
					if($.updError(id, data)) return false;

					// 登録完了後に、登録日、更新日、オペレーターなどの画面上に再度反映が必要な情報を設定する。
					that.setAfterUpdateInfo(data);

					// Grid編集行情報を初期化
					that.editRowIndex['gridholder'] = -1;

					var afterFunc = function(){
						// 初期化
						that.clear();
						$($.id.gridholder).datagrid('getPanel').find("[datagrid-row-index='"+index+"']").css('color', 'blue');

						var rows = $($.id.gridholder).datagrid('getRows');
						var row = rows[index];
						if(!row){
							row = $($.id.gridholder).datagrid('getData').firstRows[index]
						}

						if(row.DEL == '1'){
							that.setEnptyRows(index, 'gridholder');
						}
					};
					$.updNormal(data, afterFunc);

					// ログ出力
					$.log(that.timeData, 'loaded:');
				}
			);
		},
		// 登録後に更新されたデータを画面に反映する
		setAfterUpdateInfo: function(data){
			var that = this;

			var json = JSON.parse(data);
			var row		 = $($.id.gridholder).datagrid("getSelected");
			var index	 = $($.id.gridholder).datagrid("getRowIndex", row);

			// 登録成功後、更新日時を再設定する。
			var upddt = json.opts.UPDDT;
			$.setInputboxValue($('#hiddenUpddt'), upddt);

			// 登録成功後、登録件数をを再設定する。
			var count = json.opts.COUNT_ROWS;
			that.setCountRows(count)

			// 登録成功後、更新したレコードの登録日、更新日、オペレーター項目の変更を反映する。
			var newData = json.opts.NEWDATA;

			if(newData){
				row.F25 = newData[0].F25	// F25	:登録日
				row.F26 = newData[0].F26	// F26	:更新日
				row.F27 = newData[0].F27	// F27	:オペレーター
				$($.id.gridholder).datagrid('refreshRow', index);
				var inputs = $($.id.gridholder).datagrid('getPanel').find('.datagrid-row .easyui-linkbutton');
				// 各行内InputをEasyUI形式に変換（class指定のInput作成だけだと普通のInputになったため）
				inputs.on("click", $.pushUpd).linkbutton({ width:  45, height: 18});
			}

			// 新規登録の場合は、登録成功後に管理番号がサーバー側から送られるので、画面のレコードに反映する。
			var kanriNo = json.opts.KANRINO;
			if(kanriNo && kanriNo != ""){
				row.F28 = kanriNo			// F28	:管理番号
				row.F30 = '0'				// F30	:更新区分
				$($.id.gridholder).datagrid('refreshRow', index);
				var inputs = $($.id.gridholder).datagrid('getPanel').find('.datagrid-row .easyui-linkbutton');
				// 各行内InputをEasyUI形式に変換（class指定のInput作成だけだと普通のInputになったため）
				inputs.on("click", $.pushUpd).linkbutton({ width:  45, height: 18});
			}
		},
	    hideRow: function(jq, index){
	        return jq.each(function(){
	            var opts = $(this).datagrid('options');
	            opts.finder.getTr(this, index).hide();
	        })
	    },
		delValidation: function (){	// （必須）批准
			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = true;

			var row = $($.id.gridholder).datagrid("getSelected");
			/*if(!row){
					$.showMessage('E00008');
				return false;
			}*/
			return rt;
		},
		delSuccess: function(id){
			var that = this;

			// 基本入力情報取得
			var targetData = that.getGridData("data")["data"];

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			//$.appendMaskMsg();

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,		// レポート名
					action:			$.id.action_delete,	// 実行処理情報
					obj:			id,								// 実行オブジェクト
					//IDX:			$($.id.hiddenChangedIdx).val(),	// 更新対象Index
					DATA:			JSON.stringify(targetData),		// 更新対象情報
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
			// SEQ
			this.jsonTemp.push({
				id:		$.id.txt_seq,
				value:	$.getJSONValue(this.jsonHidden, $.id.txt_seq),
				text:	''
			});
			// 部門コード
			this.jsonTemp.push({
				id:		$.id_inp.txt_bmncd,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_bmncd),
				text:	''
			});
		},
		extenxDatagridEditorIds:{
			 F1		: "txt_shncd"			// 商品コード
			,F2		: "txt_makerkn"			// メーカー名称
			,F3		: "txt_shnkn"			// 商品名称
			,F4		: "txt_kikkn"			// 規格名称
			,F5		: "txt_rg_irisu"		// レギュラー情報_店入数
			,F6		: "txt_irisu"			// 入数
			,F7		: "txt_minsu"			// 最低発注数
			,F8		: "txt_rg_genkaam"		// 通常原価
			,F9		: "txt_genkaam"			// 原価
			,F10	: "txt_a_baikaam"		// A総売価
			,F11	: "txt_tok_honbaika_a"	// A本体売価
			,F12	: "txt_a_rankno"		// Aランク
			,F13	: "txt_hs_neire_a"		// A値入率
			,F14	: "txt_b_baikaam"		// B総売価
			,F15	: "txt_tok_honbaika_b"	// B本体売価
			,F16	: "txt_b_rankno"		// Bランク
			,F17	: "txt_hs_neire_b"		// B値入率
			,F18	: "txt_c_baikaam"		// C総売価
			,F19	: "txt_tok_honbaika_c"	// C本体売価
			,F20	: "txt_c_rankno"		// Cランク
			,F21	: "txt_hs_neire_c"		// C値入率
			,F22	: "txt_popcd"			// POPコード
			,F23	: "txt_popsz"			// POPサイズ
			,F24	: "txt_popsu"			// 枚数
			,F31	: "txt_baikaam"			// 売価
			,F35	: "txt_rg_baikaam"		// レギュラー標準売価
		},
		setGrid: function (id, reportNumber){	// グリッドの構築
			var that = this;
			var init = true;
			// ページサイズ定義取得
			var pageList = $.fn.pagination.defaults.pageList;
			//var pageSize = $.getJSONValue(this.jsonHidden, $.id.pageSize);
			var pageSize = 100;
			if (pageSize==="") pageSize=$.fn.pagination.defaults.pageSize;
			pageSize = $.getDefaultPageSize(pageSize, pageList);

			var fcolumns = [];
			var columns = [];
			var fcolumnBottom=[];
			var columnBottom=[];

			var bcstyler =function(value,row,index){return 'background-color:#f5f5f5;';};
			var dformatter =function(value){ return $.getFormatDt(value, true);};
			var cstyler =function(value,row,index){return 'color: red;font-weight: bold;';};
			var cstyler2=function(value,row,index){return 'color: red;font-weight: bold;background-color:#f5f5f5;';};
			var cformatter =function(value,row,index){return value && value===$.id.value_on?$.id.text_on:$.id.text_off;};
			var pomptFormatter =function(value){return $.getFormatPrompt(value, '####-####');};
			var iformatter	 = function(value,row,index){ return $.getFormat(value, '#,##0');};
			var iformatter_s = function(value,row,index){ return $.getFormat(value, '#,##0,00');};
			var pcrmatter = function(value,row,index){
				if(value && value !==""){
					if(value == '0' || value == 0){
						return null;
					}else{
						return $.getFormat(value, '#,##0.00')+'%';
					}
				}
			};

			var setNeirrt = function(value, row){
				var baika = row.F11;
				var genka = row.F9;
				var NeireRit = that.calcNeireRit(baika, genka);

				return NeireRit;
			};

			var setHonbaika = function(value, row){
				var sobaika = row.F10;
				var baika = $.getInputboxData(that.name, $.id.action_change, $.id.txt_tok_honbaika, that.getInputboxParams(that, $.id.txt_tok_honbaika, sobaika));

				return  $.getFormat(baika, '#,###0');
			};
			var setneir =function(value,row,index){
				return value && value===$.id.value_on?$.id.text_on:$.id.text_off;

			};

			if(that.reportYobiInfo()!=='1'){
				fcolumnBottom.push({field:'ENTER',	title:'　',			width:55,	halign:'center',align:'center',	formatter:function(value,row,index){ return '<a href="#" title="登録" id="btn_enter_'+index+'" class="easyui-linkbutton"><span>登録</span></a>'}});
			}

			//fcolumnBottom.push({field:'DEL',	title:'削除',				checkbox:true,	width:  90,halign:'center',align:'center',	styler:cstyler});
			fcolumnBottom.push({field:'DEL',	title:'削除',					width:  35,halign:'center',align:'center', formatter:cformatter, editor:{type:'checkbox'},	styler:cstyler});
			fcolumnBottom.push({field:'F1',	title:'商品コード',				width: 100,halign:'center',align:'left', formatter:pomptFormatter, editor:{type:'numberbox'}});
			fcolumnBottom.push({field:'F2',	title:'メーカー名',				width: 200,halign:'center',align:'left', editor:{type:'textbox'}});
			fcolumnBottom.push({field:'F3',	title:'商品名称',				width: 300,halign:'center',align:'left', editor:{type:'textbox'}});

			columnBottom.push({field:'F4',	title:'規格',					width: 100,halign:'center',align:'left', editor:{type:'textbox'}});
			columnBottom.push({field:'F5',	title:'レギュラー入数',			width:  80,halign:'center',align:'right', formatter:iformatter,editor:{type:'numberbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}});
			columnBottom.push({field:'F6',	title:'生活応援入数',			width:  82,halign:'center',align:'right', formatter:iformatter,editor:{type:'numberbox'}});
			columnBottom.push({field:'F7',	title:'最低発注数',				width:  80,halign:'center',align:'right', formatter:iformatter,editor:{type:'numberbox'}});
			columnBottom.push({field:'F8',	title:'通常原価',				width:  80,halign:'center',align:'right', formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0.00');}, editor:{type:'numberbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}});
			columnBottom.push({field:'F9',	title:'生活応援原価',			width:  82,halign:'center',align:'right', formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0.00');}, editor:{type:'numberbox'}});
			columnBottom.push({field:'F10',	title:'A総売価',				width:  80,halign:'center',align:'right', formatter:iformatter, editor:{type:'numberbox'}});
			columnBottom.push({field:'F11',	title:'A本売価',				width:  80,halign:'center',align:'right', formatter:iformatter, editor:{type:'numberbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}});
			columnBottom.push({field:'F12',	title:'Aランク',				width:  80,halign:'center',align:'right', formatter:function(value,row,index){ return $.getFormatLPad(value, $.len.rankno);}, editor:{type:'numberbox'}});
			columnBottom.push({field:'F13',	title:'A値入率',				width:  80,halign:'center',align:'right', formatter:pcrmatter, editor:{type:'textbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}});
			columnBottom.push({field:'F14',	title:'B総売価',				width:  80,halign:'center',align:'right', formatter:iformatter, editor:{type:'numberbox'}});
			columnBottom.push({field:'F15',	title:'B本売価',				width:  80,halign:'center',align:'right', formatter:iformatter, editor:{type:'numberbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}});
			columnBottom.push({field:'F16',	title:'Bランク',				width:  80,halign:'center',align:'right', formatter:function(value,row,index){ return $.getFormatLPad(value, $.len.rankno);}, editor:{type:'numberbox'}});
			columnBottom.push({field:'F17',	title:'B値入率',				width:  80,halign:'center',align:'right', editor:{type:'textbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}});
			columnBottom.push({field:'F18',	title:'C総売価',				width:  80,halign:'center',align:'right', formatter:iformatter, editor:{type:'numberbox'}});
			columnBottom.push({field:'F19',	title:'C本売価',				width:  80,halign:'center',align:'right', formatter:iformatter, editor:{type:'numberbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}});
			columnBottom.push({field:'F20',	title:'Cランク',				width:  80,halign:'center',align:'right', formatter:function(value,row,index){ return $.getFormatLPad(value, $.len.rankno);}, editor:{type:'numberbox'}});
			columnBottom.push({field:'F21',	title:'C値入率',				width:  80,halign:'center',align:'right', editor:{type:'textbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}});
			columnBottom.push({field:'F22',	title:'POPコード',				width:  80,halign:'center',align:'right', editor:{type:'numberbox'}});
			columnBottom.push({field:'F23',	title:'POPサイズ',				width:  80,halign:'center',align:'left', editor:{type:'textbox'}});
			columnBottom.push({field:'F24',	title:'枚数',					width:  80,halign:'center',align:'right', editor:{type:'numberbox'}});
			columnBottom.push({field:'F25',	title:'登録',					width:  80,halign:'center',align:'left',styler:function(value,row,index){return 'background-color:#f5f5f5;';}});
			columnBottom.push({field:'F26',	title:'更新',					width:  80,halign:'center',align:'left',styler:function(value,row,index){return 'background-color:#f5f5f5;';}});
			columnBottom.push({field:'F27',	title:'オペレーター',			width:  100,halign:'center',align:'left',styler:function(value,row,index){return 'background-color:#f5f5f5;';}});
			columnBottom.push({field:'F28',	title:'管理番号',				width:  80,halign:'center',align:'right',hidden:true});
			columnBottom.push({field:'F29',	title:'PLG配信済フラグ',		width:  80,halign:'center',align:'right',hidden:true});
			columnBottom.push({field:'F30',	title:'更新区分',				width:  80,halign:'center',align:'right',hidden:true});
			columnBottom.push({field:'F31',	title:'売価',					width:  80,halign:'center',align:'right', editor:{type:'numberbox'},hidden:true});
			columnBottom.push({field:'F32',	title:'SEQ',					width:  80,halign:'center',align:'right',hidden:true});
			columnBottom.push({field:'F33',	title:'入力番号',				width:  80,halign:'center',align:'right',hidden:true});
			columnBottom.push({field:'F34',	title:'',						width:  80,halign:'center',align:'right',hidden:true});
			columnBottom.push({field:'F35',	title:'',						width:  80,halign:'center',align:'right', editor:{type:'numberbox'},hidden:true});

			columns.push(columnBottom);
			fcolumns.push(fcolumnBottom);

			var funcEnter = function(e){
				if ($.endEditingDatagrid(that)){
					$.pushUpd(e);
				}
			};
			var funcClickRow = $.fn.datagrid.defaults.onClickRow;
			var funcBeginEdit= $.fn.datagrid.defaults.onBeginEdit;
			var funcEndEdit= $.fn.datagrid.defaults.onEndEdit;
			var funcAfterEdit= $.fn.datagrid.defaults.onAfterEdit;
			if(that.reportYobiInfo()!=='1' && that.editable){
				that.editRowIndex[id] = -1;
				funcClickRow = function(index,field){

					$.clickEditableDatagridCell(that,id, index)
				};
				funcBeginEdit = function(index,row){
					// 先頭の削除チェックにフォーカスを合わせない。
					$('#'+$.id.chk_del+"_").attr('tabindex', -1);

					if (!row) {
						row = $('#'+id).datagrid('getData').firstRows[index-1];
					}

					if(row.F30 == '0'){
						if(that.dispType == 'SO003'){
							// 既存商品コードは編集不可
							$.setInputBoxDisable($('#'+$.id_inp.txt_shncd+"_"));
						}
					}else{
						// 新規行は削除チェック不可
						$.setInputBoxDisable($('#'+$.id.chk_del+"_"));
					}
					$.beginEditDatagridRow(that,id, index, row)
				};
				funcEndEdit = function(index,row,changes){$.endEditDatagridRow(that, id, index, row)};
				funcAfterEdit = function(index,row,changes){
					var rowobj = $('#'+id).datagrid('getPanel').find("[datagrid-row-index='"+index+"']");
					// チェックボックスの再追加（EndEdit時に削除されるため）
					$.afterEditAddCheckbox(rowobj);
					// ボタンオブジェクトの再追加（EndEdit時に削除されるため）
					rowobj.find(".easyui-linkbutton").on("click", $.pushUpd).linkbutton({ width:  45, height: 18});

					// 重複行塗り潰し処理
					var rows	 = $('#gridholder').datagrid('getRows');
					var shnData = []

					for (var i = 0; i < rows.length; i++){
						var row	  = rows[i]
						var shncd = row['F1']
						var value = "";

						if(shncd){
							value = shncd
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
					$.removeErrStateGrid('gridholder');
					if(shnData_.length > 0){
						var targetColIndex = 3		// 商品コードの項目順番
						// グリッド内の重複箇所を塗り潰し
						$.addErrStateGrid('gridholder', shnData_, [targetColIndex]);
					}
				};

				// 編集可能データグリッドの共通処理設定
				// 編集エディターの機能拡張（非表示で参照用のオブジェクトが準備してある前提、参照オブジェクトはグリッドのフィールド名を元に判断）
				$.extendDatagridEditor(that);

			}

			$('#'+id).datagrid({
				nowrap: true,
				border: true,
				striped: false,
				collapsible:false,
				remoteSort: true,
				rownumbers:true,
				fit:true,
				view:scrollview,
				pageSize:pageSize,
				pageList:pageList,
				frozenColumns:fcolumns,
				//checkOnSelect:false,
				//selectOnCheck:false,
				columns:columns,
				fitColumns:false,	// 指定カラム幅を適用する場合、false 指定。
				onBeforeLoad:function(param){
					param.report = that.name;
				},
				/*onSelect:function(index){
					if(that.reportYobiInfo()!=='1' && that.editable){
						var rows = $('#'+id).datagrid('getRows');
						var col = $('#'+id).datagrid('getColumnOption', 'F1');
						if(rows[index]["F30"]=='0'){
							col.editor = {
									type:'numberbox',
									options:{cls:'labelInput',editable:false,disabled:true,readonly:true},
									styler:function(value,row,index){return 'background-color:#f5f5f5;';},
									formatter:pomptFormatter
								}

						}else{
							col.editor = {
									type:'numberbox',
									options:{cls:'labelInput',editable:true,disabled:false,readonly:false},
									formatter:pomptFormatter
								}

						}
					}
				},*/
				onLoadSuccess:function(data){
					// View内の入出力項目を調整
					if(that.reportYobiInfo()!=='1'){
						var inputs = $('#'+id).datagrid('getPanel').find('.datagrid-row .easyui-linkbutton');
						// 各行内InputをEasyUI形式に変換（class指定のInput作成だけだと普通のInputになったため）
						inputs.on("click", $.pushUpd).linkbutton({ width:  45, height: 18});
					}

					if(init){
						init = false;
						var data = $($.id.gridholder).datagrid('getData');
						var total = data.total;
						var panel = $($.id.gridholder).datagrid('getPanel');

						//$($.id.gridholder).datagrid('loadData', data);
					}

					var rows	 = $($.id.gridholder).datagrid('getRows');

					/*var data = $($.id.gridholder).datagrid('getData');
					var nodesArray = new Array();
					for (var i = 0; i < data.total; i++) {
					    nodesArray.push(data.rows[i]);
					};*/
				},
				onClickCell:function(rowIndex, field, value){
					// 列名保持
					that.columnName = field;
				},
				/*loadFilter:function(data){
					if (typeof data.length == 'number' && typeof data.splice == 'function'){	// is array
						data = {
							total: data.length,
							rows: data
						};
					}
					var dg = $(this);
					var opts = dg.datagrid('options');
					that.createPagenation(that, dg, opts, data);
					if (!data.originalRows){
						data.originalRows = (data.rows);
					}
					var start = (opts.pageNumber-1)*parseInt(opts.pageSize);
					var end = start + parseInt(opts.pageSize);
					data.rows = (data.originalRows.slice(start, end));
					return data;
				},*/
				onClickRow: funcClickRow,
				onBeginEdit:funcBeginEdit,
				onEndEdit: funcEndEdit,
				onAfterEdit: funcAfterEdit,
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
		setEnptyRows: function(index ,id){
			var rows = $('#'+id).datagrid('getRows');
			var row = rows[index];
			var rowLength = Object.keys(row).length;
			var rowobj = $('#'+id).datagrid('getPanel').find("[datagrid-row-index='"+index+"']");

			// 指定行を空にする。
			for (var i=1; i <= rowLength; i++){
				row["F"+i] = "";
			}
			// チェクボックスからチェックを外す。
			row["DEL"] = "0"

			$('#'+id).datagrid('refreshRow',  Number(index));
			// ボタンオブジェクトの再追加（EndEdit時に削除されるため）
			rowobj.find(".easyui-linkbutton").on("click", $.pushUpd).linkbutton({ width:  45, height: 18});

		},
		getGridData: function (target, del){
			var that = this;

			var data = {};
			var targetRows= [];

			// 基本情報
			if(target===undefined || target==="data"){

				if(that.dispType == 'SO005'){
					var myoscd		 = $.getInputboxValue($('#'+$.id_inp.txt_moyscd)).split("-");
					var szMoyskbn	 = myoscd[0];														// 催し区分
					var szMoysstdt	 = myoscd[1];														// 催しコード（催し開始日）
					var szMoysrban	 = myoscd[2];														// 催し連番
					var szBmncd		 = $.getInputboxValue($('#'+$.id_inp.txt_bmncd)).split("-")[0];		// 部門コード
					var szUpddt		= $.getInputboxValue($("#hiddenUpddt"));							// 更新日：排他チェック用

				}else{
					var szMoyskbn	= $.getJSONObject(this.jsonString, $.id_inp.txt_moyskbn).value;		// 催し区分
					var szMoysstdt	= $.getJSONObject(this.jsonString, $.id_inp.txt_moysstdt).value;	// 催しコード（催し開始日）
					var szMoysrban	= $.getJSONObject(this.jsonString, $.id_inp.txt_moysrban).value;	// 催し連番
					var szBmncd		= $.getJSONObject(this.jsonString, $.id_inp.txt_bmncd).value;		// 部門コード
					var szUpddt		= $.getInputboxValue($("#hiddenUpddt"));							// 更新日：排他チェック用
				}
				var rowDate = {
						F1	 : szMoyskbn,					// 催し区分
						F2	 : szMoysstdt,					// 催し開始日
						F3	 : szMoysrban,					// 催し連番
						F4	 : szBmncd,						// 部門
						F5	 : szUpddt,						// 更新日：排他チェック用
				};
				if(rowDate){
					targetRows.push(rowDate);
				}
				data["data"] = targetRows;
			}

			// 商品一覧
			if(target===undefined || target.indexOf('btn_enter_')===0){

				var index = target.replace("btn_enter_", "");
				// 変更行情報取得
				var changedIndex = $($.id.hiddenChangedIdx).val().split(",");
				// 基本情報
				var row = $($.id.gridholder).datagrid('getData').firstRows[index]

				if(that.dispType == 'SO005'){
					var myoscd		 = $.getInputboxValue($('#'+$.id_inp.txt_moyscd)).split("-");
					var szMoyskbn	 = myoscd[0];														// 催し区分
					var szMoysstdt	 = myoscd[1];														// 催しコード（催し開始日）
					var szMoysrban	 = myoscd[2];														// 催し連番
					var szBmncd		 = $.getInputboxValue($('#'+$.id_inp.txt_bmncd)).split("-")[0];		// 部門コード

				}else{
					var szMoyskbn	= $.getJSONObject(this.jsonString, $.id_inp.txt_moyskbn).value;		// 催し区分
					var szMoysstdt	= $.getJSONObject(this.jsonString, $.id_inp.txt_moysstdt).value;	// 催しコード（催し開始日）
					var szMoysrban	= $.getJSONObject(this.jsonString, $.id_inp.txt_moysrban).value;	// 催し連番
					var szBmncd		= $.getJSONObject(this.jsonString, $.id_inp.txt_bmncd).value;		// 部門コード
				}

				if(del && del === 'del'){
					if(row["DEL"] === "1" && that.dispType == 'SO003'){
						var rowDate = {
								F1	 : szMoyskbn,								// 催し区分
								F2	 : szMoysstdt,								// 催し開始日
								F3	 : szMoysrban,								// 催し連番
								F4	 : szBmncd,									// 部門
								F5	 : row["F28"],								// 管理番号
						};
						targetRows.push(rowDate);
					}
					data[target] = targetRows;
				}else{
					if((row['F1'] ? row['F1'] : '') !== '' && row["DEL"] == '0' ){
						var rowDate = {
								F1	 : szMoyskbn,								// 催し区分
								F2	 : szMoysstdt,								// 催し開始日
								F3	 : szMoysrban,								// 催し連番
								F4	 : szBmncd,									// 部門
								F5	 : row["F28"],								// 管理番号
								F6	 : row["F1"],								// 商品コード
								F7	 : row["F2"],								// メーカー名称
								F8	 : row["F3"],								// 商品名称
								F9	 : row["F4"],								// 規格名称
								F10	 : row["F6"],								// 入数
								F11	 : row["F7"],								// 最低発注数
								F12	 : row["F9"],								// 原価
								F13	 : row["F10"],								// A売価
								F14	 : row["F14"],								// B売価
								F15	 : row["F18"],								// C売価
								F16	 : row["F12"],								// Aランク
								F17	 : row["F16"],								// Bランク
								F18	 : row["F20"],								// Cランク
								F19	 : row["F22"],								// POPコード
								F20	 : row["F23"],								// POPサイズ
								F21	 : row["F24"],								// 枚数
						};

						if(that.dispType == 'SO005'){

							// シーケンス情報を追加
							rowDate.F22 = row["F32"];							// SEQ
							rowDate.F23 = row["F33"];							// 入力番号
							rowDate.F24 = row["F34"];							// CSV登録区分
						}

						rowDate.F25 = '';										// 店扱フラグ配列用空白値

						targetRows.push(rowDate);
					}
					data[target] = targetRows;
				}
			}

			// CSV出力データ(画面上の新規入力データ)
			if(target===undefined || target==="OutputCSV"){

				// 基本情報
				var rows = $($.id.gridholder).datagrid('getRows');

				var szMoyskbn	= $.getJSONObject(this.jsonString, $.id_inp.txt_moyskbn).value;		// 催し区分
				var szMoysstdt	= $.getJSONObject(this.jsonString, $.id_inp.txt_moysstdt).value;	// 催しコード（催し開始日）
				var szMoysrban	= $.getJSONObject(this.jsonString, $.id_inp.txt_moysrban).value;	// 催し連番
				var szBmncd		= $.getJSONObject(this.jsonString, $.id_inp.txt_bmncd).value;		// 部門コード
				//var szMoyscd	= szMoyskbn + szMoysstdt + ('000'+szMoysrban).substring(0,2);		// 催しコード
				var szMoyscd	= $.getInputboxValue($('#'+$.id_inp.txt_moyscd));					// 催しコード
				//var szMoyscd	= $.getJSONObject(this.jsonString, $.id_inp.txt_moyscd).value;		// 催しコード

				var regExp = new RegExp( '-', "g" ) ;
				szMoyscd	= szMoyscd.replace(regExp,  '');

				for (var i=0; i<rows.length; i++){
					var row = rows[i]
					if(row.F30 !== '0' && row.F1 && row.F1 !== ''){
						var rowDate = {
								F1	 : szMoyscd,								// 催しコード
								F2	 : szBmncd,									// 部門
								F3	 : row["F28"],								// 管理番号
								F4	 : row["F1"],								// 商品コード
								F5	 : row["F2"],								// メーカー名称
								F6	 : row["F3"],								// 商品名称
								F7	 : row["F4"],								// 規格名称
								F8	 : row["F6"],								// 入数
								F9	 : row["F7"],								// 最低発注数
								F10	 : row["F9"],								// 原価
								F11	 : row["F10"],								// A売価
								F12	 : row["F14"],								// B売価
								F13	 : row["F18"],								// C売価
								F14	 : row["F12"],								// Aランク
								F15	 : row["F16"],								// Bランク
								F16	 : row["F20"],								// Cランク
								F17	 : row["F22"],								// POPコード
								F18	 : row["F24"],								// 枚数
								F19	 : row["F23"],								// POPサイズ

						};
						targetRows.push(rowDate);
					}
				}
				data[target] = targetRows;

				/*if(del && del === 'del'){
					if(row["DEL"] === "1" && that.dispType == 'SO003'){
						var rowDate = {
								F1	 : szMoyskbn,								// 催し区分
								F2	 : szMoysstdt,								// 催し開始日
								F3	 : szMoysrban,								// 催し連番
								F4	 : szBmncd,									// 部門
								F5	 : row["F28"],								// 管理番号
						};
						targetRows.push(rowDate);
					}
					data[target] = targetRows;
				}*/
			}
			return data;
		},
		// SO005 エラー修正画面用 更新データ取得function
		getGridDataSO005: function (target){
			var that = this;

			var data = {};
			var targetRows= [];

			// 商品一覧
			if(target===undefined || target.indexOf('btn_enter_')===0){

				var index = target.replace("btn_enter_", "");
				// 変更行情報取得
				var changedIndex = $($.id.hiddenChangedIdx).val().split(",");
				// 基本情報
				var row = $($.id.gridholder).datagrid('getData').firstRows[index]

				var szMoyskbn	= $.getJSONObject(this.jsonString, $.id_inp.txt_moyskbn).value;		// 催し区分
				var szMoysstdt	= $.getJSONObject(this.jsonString, $.id_inp.txt_moysstdt).value;	// 催しコード（催し開始日）
				var szMoysrban	= $.getJSONObject(this.jsonString, $.id_inp.txt_moysrban).value;	// 催し連番
				var szBmncd		= $.getJSONObject(this.jsonString, $.id_inp.txt_bmncd).value;		// 部門コード

				if(row["DEL"] === "1" && that.dispType == "SO005"){
					var rowDate = {
							F1	 : row["F32"],								// SEQ
							F2	 : row["F33"],								// 入力番号
					};
					targetRows.push(rowDate);
				}
				data[target] = targetRows;
			}
			return data;

		},
		setData: function(rows, opts){		// データ表示
			var that = this;
			if(rows.length > 0){
				$('#'+that.focusRootId).find('[col^=F]').each(function(){
					var col = $(this).attr('col');
					if(rows[0][col]){
						if(col == 'F5'){
							//$.setInputboxValue($(this), rows[0][col]);
							$.setInputboxValue($(this), $.getFormat(rows[0][col], '#,##0'));
						}else{
							$.setInputboxValue($(this), rows[0][col]);
						}

					}
				});
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
		repgrpInfo: {
			MM001:{idx:1},		// 催し検索 変更 催し一覧
			MM001_1:{idx:2},	// 催し検索 参照 催し一覧
			MM002:{idx:3}		// 催し検索 商品一覧
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

			// 戻る実行時用に現在の画面情報を保持する
			var states = $.getBackBaseJSON(that);
			// 各種グリッド情報を設定
			var newrepinfos = $.getBackJSON(that, states);
			$.setJSONObject(sendJSON, 'repinfo', newrepinfos, '');

			// 選択行
			var row = $($.id.gridholder).datagrid("getSelected");

			// 実行ボタン別処理
			switch (btnId) {
			case $.id.btn_sel_change:
			case $.id.btn_sel_refer:

				if(!row){
					$.showMessage('E00008');
					return false;
				}
				index = 4;
				childurl = href[index];
				sendMode = 1;
				$.setJSONObject(sendJSON, $.id_inp.txt_inputno, row.F1, row.F1);				// 企画No
				break;
			case $.id.btn_back:
			case $.id.btn_cancel:
			case'btn_return':

				sendMode = 2;
				var callpage = $.getJSONValue(that.jsonHidden, "callpage");
				// 転送先情報
				if(callpage==='Out_ReportSO001'){
					if(that.reportYobiInfo()==='1'){
						index = 2;
					}else {
						index = 1;
					}
				}else if(callpage==='Out_ReportSO002'){
					index = 9;
				}else if(callpage==='Out_ReportSO004'){
					index = 5;
					// シーケンスを送り返す。
					/*var szSeq		= $.getJSONObject(this.jsonString, $.id.txt_seq).value;				// SEQ
					if(szSeq && szSeq != ""){
						sendMode = 1;
						$.setJSONObject(sendJSON, $.id.txt_seq, szSeq, szSeq);
					}*/
				}else if(callpage==='Out_ReportSO006'){
					index = 7;
				}else if(callpage==='Out_ReportMM001'){
					var reportYobi1 = $.getJSONValue(that.jsonHidden, "reportYobi1");
					if (reportYobi1 === '0') {
						index = that.repgrpInfo.MM001.idx;
					} else {
						index = that.repgrpInfo.MM001_1.idx;
					}
				}else if(callpage==='Out_ReportMM002'){
					index = that.repgrpInfo.MM002.idx;
				}

				//index = 4;
				childurl = href[index]

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
		csv: function(reportno){	// Csv出力
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
				'kbn'	: kbn,
				'type'	: 'csv'
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
		/**
		 * CSV出力ボタンイベント
		 * @param {Object} e
		 */
		pushCsv : function(e){
			if ($(this).linkbutton('options').disabled)	return false;

			// レポート番号取得
			var reportno=$($.id.hidden_reportno).val();
			var id = $(this).attr('id');

			// レポート定義位置
			var reportNumber = $.getReportNumber(reportno);
			if (typeof(reportNumber) !== 'number') { alert("レポート定義が見つかりません。"); return false;}

			// マスク削除
			$.removeMask();

			// フォーム情報取得
			$.report[reportNumber].getEasyUI();

			if ($.report[reportNumber].validation(id)) {

				var func_ok = function(){
					// マスク追加
					$.appendMask();

					// セッションタイムアウト、利用時間外の確認
					var isTimeout = $.checkIsTimeout();
					if (! isTimeout) {
						// 検索条件保持
						$.ajax({
							url: $.reg.easy,
							type: 'POST',
							async: false,
							data: {
								"page"	: reportno,
								"obj"	: $.id.btn_search,
								"sel"	: "json",
								"userid": $($.id.hidden_userid).val(),
								"user"	: $($.id.hiddenUser).val(),
								"report": $($.id.hiddenReport).val(),
								"json"	: JSON.stringify($.report[reportNumber].jsonStringCsv)
							},
							success: function(json){
								// Excel 出力
								$.report[reportNumber].srccsv(reportno, id);
							}
						});
					}
				};

				// CSVデータを出力します。よろしいですか？
				$.showMessage("W20030", undefined, func_ok);
			} else {
				return false;
			}
		},
		pushCsv_test : function(e){
			if ($(this).linkbutton('options').disabled)	return false;

			// レポート番号取得
			var reportno=$($.id.hidden_reportno).val();
			var id = $(this).attr('id');

			// レポート定義位置
			var reportNumber = $.getReportNumber(reportno);
			if (typeof(reportNumber) !== 'number') { alert("レポート定義が見つかりません。"); return false;}

			// マスク削除
			$.removeMask();

			// フォーム情報取得
			$.report[reportNumber].getEasyUI();

			if ($.report[reportNumber].validation(id)) {

				var func_ok = function(){
					// マスク追加
					$.appendMask();

					// セッションタイムアウト、利用時間外の確認
					var isTimeout = $.checkIsTimeout();
					if (! isTimeout) {
						// 検索条件保持
						$.ajax({
							url: $.reg.easy,
							type: 'POST',
							async: false,
							data: {
								"page"	: reportno,
								"obj"	: $.id.btn_search,
								"sel"	: "json",
								"userid": $($.id.hidden_userid).val(),
								"user"	: $($.id.hiddenUser).val(),
								"report": $($.id.hiddenReport).val(),
								"json"	: JSON.stringify($.report[reportNumber].jsonStringCsv)
							},
							success: function(json){
								// Excel 出力
								//$.report[reportNumber].srccsv(reportno, id);
								$.showMessage("I00005");
								$.removeMask();
							}
						});
					}
				};

				// CSVデータを出力します。よろしいですか？
				$.showMessage("W20030", undefined, func_ok);
			} else {
				return false;
			}
		},
		setCountRows: function(index){
			var that = this;
			//that.tortalRows = index;
			$.setInputboxValue($('#count_row'), $.getFormat(index, '#,##0'));
		},
		srccsv: function(reportno, btnId){	// ここではCsv出力
			var that = this;
			// 処理時間計測用
			that.timeData = (new Date()).getTime();

			var that = this;
			// 検索実行
			var szMoyskbn		= $.getJSONObject(this.jsonStringCsv, $.id_inp.txt_moyskbn).value;		// 催しコード
			var szMoysstdt		= $.getJSONObject(this.jsonStringCsv, $.id_inp.txt_moysstdt).value;		// 催し開始日
			var szMoysrban		= $.getJSONObject(this.jsonStringCsv, $.id_inp.txt_moysrban).value;		// 催し連番
			var selBumon		= $.getJSONObject(this.jsonStringCsv, $.id_inp.txt_bmncd).value;		// 部門

			// 入力編集を終了する。
			var row = $($.id.gridholder).datagrid("getSelected");
			var rowIndex = $($.id.gridholder).datagrid("getRowIndex", row);
			$($.id.gridholder).datagrid('endEdit',rowIndex);

			var targetData = that.getGridData("OutputCSV")["OutputCSV"];


			if(!btnId) btnId = $.id.btn_search;

			var kbn = 0;
			var data = {
				report:			that.name,						// レポート名
				'kbn':			 kbn,
				'type':			'csv',
				BTN:			btnId,
				MOYSKBN:		szMoyskbn,						// 催し区分
				MOYSSTDT:		szMoysstdt,						// 催し開始日
				MOYSRBAN:		szMoysrban,						// 催し連番
				BMNCD:			selBumon,						// 部門
				NEW_DATA:		JSON.stringify(targetData),		// 更新対象情報

				t:				(new Date()).getTime(),
				rows:			0	// 表示可能レコード数
			};

			// 転送
			$.ajax({
				url: $.reg.srcexcel,
				type: 'POST',
				data: data,
				async: true
			})
			.done(function(){
				// Excel出力
				$.outputSearchExcel(reportno, 0);
			})
			.fail(function(){
				// Excel出力エラー
				$.outputSearchExcelError();
			})
			.always(function(){
				// 通信完了
				// ログ出力
				$.log(that.timeData, 'srcexcel:');
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

				// 商品コード
				if(id===$.id_inp.txt_shncd){
					msgid = that.checkInputboxFunc(id, newValue, '');

					// 売価取得
					var baika = $.getInputboxValue($('#'+$.id_inp.txt_baikaam+'_'));

					// 総売価
					var sobaika = $.getInputboxData(that.name, $.id.action_change, $.id.txt_tok_soubaika, that.getInputboxParams(that, $.id.txt_tok_soubaika, baika));
					$.setInputboxValue($('#'+$.id_inp.txt_a_baikaam+'_'), sobaika);


					// 重複行塗り潰し処理
					var rows	 = $('#gridholder').datagrid('getRows');
					var shnData = []

					var row = $('#gridholder').datagrid("getSelected");
					var rowIndex = $('#gridholder').datagrid("getRowIndex", row);

					for (var i = 0; i < rows.length; i++){
						var row	  = rows[i]
						var shncd = row['F1']
						var value = "";

						if(i == rowIndex){
							shncd = newValue
						}

						if(shncd){
							value = shncd
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
					$.removeErrStateGrid('gridholder');
					if(shnData_.length > 0){
						var targetColIndex = 3		// 商品コードの項目順番
						// グリッド内の重複箇所を塗り潰し
						$.addErrStateGrid('gridholder', shnData_, [targetColIndex]);
					}
				}

				// ランクNo
				if(id===$.id_inp.txt_a_rankno || id===$.id_inp.txt_b_rankno || id===$.id_inp.txt_c_rankno){
					if(newValue && newValue !== ''){
						msgid = that.checkInputboxFunc(id, newValue, '');
					}
				}

				// 原価
				if(id===$.id_inp.txt_genkaam){
					var genka = newValue;
					var baika_a = $.getInputboxValue($('#'+$.id.txt_tok_honbaika+'_a_'));
					var baika_b = $.getInputboxValue($('#'+$.id.txt_tok_honbaika+'_b_'));
					var baika_c = $.getInputboxValue($('#'+$.id.txt_tok_honbaika+'_c_'));

					if(baika_a && baika_a!==""){
						$.setInputboxValue($('#'+'txt_hs_neire_a_'), that.calcNeireRit(baika_a, genka));
					}
					if(baika_b && baika_b!==""){
						$.setInputboxValue($('#'+'txt_hs_neire_b_'), that.calcNeireRit(baika_b, genka));
					}
					if(baika_c && baika_c!==""){
						$.setInputboxValue($('#'+'txt_hs_neire_c_'), that.calcNeireRit(baika_c, genka));
					}
				}

				// 本体売価
				if(id===$.id_inp.txt_a_baikaam){
					var baika = $.getInputboxData(that.name, $.id.action_change, $.id.txt_tok_honbaika, that.getInputboxParams(that, $.id.txt_tok_honbaika, newValue));
					$.setInputboxValue($('#'+$.id.txt_tok_honbaika+'_a_'), baika);
				}
				if(id===$.id_inp.txt_b_baikaam){
					var baika = $.getInputboxData(that.name, $.id.action_change, $.id.txt_tok_honbaika, that.getInputboxParams(that, $.id.txt_tok_honbaika, newValue));
					$.setInputboxValue($('#'+$.id.txt_tok_honbaika+'_b_'), baika);
				}
				if(id===$.id_inp.txt_c_baikaam){
					var baika = $.getInputboxData(that.name, $.id.action_change, $.id.txt_tok_honbaika, that.getInputboxParams(that, $.id.txt_tok_honbaika, newValue));
					$.setInputboxValue($('#'+$.id.txt_tok_honbaika+'_c_'), baika);
				}

				// 値入率
				if(id===$.id.txt_tok_honbaika+'_a'){
					var baika = newValue;
					var genka	 = $.getInputboxValue($('#'+$.id_inp.txt_genkaam+'_'));
					$.setInputboxValue($('#'+'txt_hs_neire_a_'), that.calcNeireRit(baika, genka));
				}
				if(id===$.id.txt_tok_honbaika+'_b'){
					var baika = newValue;
					var genka	 = $.getInputboxValue($('#'+$.id_inp.txt_genkaam+'_'));
					$.setInputboxValue($('#'+'txt_hs_neire_b_'), that.calcNeireRit(baika, genka));
				}
				if(id===$.id.txt_tok_honbaika+'_c'){
					var baika = newValue;
					var genka	 = $.getInputboxValue($('#'+$.id_inp.txt_genkaam+'_'));
					$.setInputboxValue($('#'+'txt_hs_neire_c_'), that.calcNeireRit(baika, genka));
				}

				if(msgid !==null){
					$.showMessage(msgid, undefined, func_focus );
					return false;
				}

				// グリッド編集系
				/*if(that.focusGridId!==undefined && that.editRowIndex[that.focusGridId] > -1){
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
				}*/
			}
		},
		// IDとvalueでチェック処理を実施
		checkInputboxFunc:function(id, newValue, kbn, record, isNew){
			var that = this;
			var sdt, edt;

			// 商品コード
			if(id===$.id_inp.txt_shncd){
				if(newValue !== '' && newValue){

					// 店舗部門マスタ
					var param = {};
					param["KEY"] =  "MST_CNT";
					param["value"] = newValue.substring(0, 2);
					var chk_cnt = $.getInputboxData(that.name, $.id.action_check, 'MSTTENBMN', [param]);
					if(chk_cnt==="" || chk_cnt==="0"){
						return "E20219";
					}

					// 商品コード
					var param = {};
					param["KEY"] =  "MST_CNT";
					param["value"] = newValue;
					var chk_cnt = $.getInputboxData(that.name, $.id.action_check, $.id_inp.txt_shncd, [param]);
					if(chk_cnt==="" || chk_cnt==="0"){
						return "E11046";
					}
				}
			}

			// ランクNo
			if(id===$.id_inp.txt_a_rankno || id===$.id_inp.txt_b_rankno || id===$.id_inp.txt_c_rankno){
				// ランクパターン

				var shncd	 = $.getInputboxValue($('#'+$.id_inp.txt_shncd+'_')); 									// 商品コード
				var moyscd	 = $.getInputboxValue($('#'+$.id_inp.txt_moyscd)).replace('-','').replace('-','');		// 催しコード
				var param = {};

				if(!shncd || shncd == ''){
					// フォーカスアウト時チェックではない場合、getInputboxValueではデータを取得できないので、
					// 選択中のrowから取得する。
					var row = $($.id.gridholder).datagrid("getSelected");
					if(row){
						shncd = row.F1
					}
				}

				if(shncd && shncd != ''){
					if(Number(newValue) >= 900 ){
						// 臨時ランクマスタ検索
						var param = {};
						param["KEY"] =  "MST_CNT";
						param["value"] = shncd.substring(0, 2) + ',' + newValue + ',' + moyscd;
						var chk_cnt = $.getInputboxData(that.name, $.id.action_check,  $.id_inp.txt_rankno + '_EX', [param]);
						if(chk_cnt==="" || chk_cnt==="0"){
							return "E20466";
						}

					}else{
						// ランクマスタ検索
						var param = {};
						param["KEY"] =  "MST_CNT";
						param["value"] = shncd.substring(0, 2) + ',' + newValue;
						var chk_cnt = $.getInputboxData(that.name, $.id.action_check,  $.id_inp.txt_rankno, [param]);
						if(chk_cnt==="" || chk_cnt==="0"){
							return "E20057";
						}
					}
				}
			}

			// 発注日
			/*if(id===$.id_inp.txt_htdt){
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
			}*/
			return null;
		},
		getInputboxParams: function(that, id, newValue){
			// 情報取得
			var values = {};
			values["value"] = newValue;

			if(that.dispType == "SO005"){
				// 基本情報

				var myoscd =  $.getInputboxValue($('#'+$.id_inp.txt_moyscd)).split("-");
				values["MOYSKBN"] = myoscd[0];
				values["MOYSSTDT"] = myoscd[1];
				values["MOYSRBAN"] = myoscd[2];

			}else{
				// 基本情報
				values["MOYSKBN"] = $.getJSONValue(this.jsonHidden, $.id_inp.txt_moyskbn);
				values["MOYSSTDT"] = $.getJSONValue(this.jsonHidden, $.id_inp.txt_moysstdt);
				values["MOYSRBAN"] = $.getJSONValue(this.jsonHidden, $.id_inp.txt_moysrban);
			}

			if(id===$.id.txt_tok_honbaika||id===$.id.txt_tok_soubaika){
				values["SHNCD"] = $.getInputboxValue($('#'+$.id_inp.txt_shncd+'_'));
				//var shncd = $('#'+$.id_inp.txt_shncd+'_').textbox('getValue');
				//values["SHNCD"] = shncd;

			}

			// 情報設定
			return [values];
		},
		calcNeireRit: function(baika, genka){
			if(baika.length===0) return "";
			if(genka.length===0) return "";
			baika = baika.replace(",", "")*1;
			genka = genka.replace(",", "")*1;

			// （本体売価－原価）÷本体売価で、小数点以下3位切り捨て, 第2位まで求める。上限98%
			// ただし、商品種別で包材、消耗品、コメント、催事テナントの時はチェックしない。
			//var value = Math.floor(Math.floor((baika-genka)/baika*10000)/100);
			var value = Math.floor((baika-genka)/baika*10000)/100;
			return $.getFormat(value, '#,##0.00');
		},
		outputFtp:function(e){
			//
//			// TODO：仮
//			alert("現在チェックリスト出力機能は停止中です。");
//			return false;


			if ($(this).linkbutton('options').disabled)	return false;

			// レポート番号取得
			var reportno=$($.id.hidden_reportno).val();
			// レポート定義位置
			var reportNumber = $.getReportNumber(reportno);
			if (typeof(reportNumber) !== 'number') { alert("レポート定義が見つかりません。"); return false;}

			// JS情報取得
			var that = $.report[reportNumber];
			var id = $(this).attr('id');

			// チェック・確認処理
			var rtn = false;

			if($.isFunction(that.outputFtpValidation)) { rtn = that.outputFtpValidation(id);}
			if(rtn){
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
								that.outputFtpSuccess(id,reportno);
							}
						});
					}
					return true;
				};
				$.showMessage("W20030", undefined, func_ok);
			}
		},
		outputFtpValidation: function (){	// （必須）批准
			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = true;

			var that = this;

			var rt = true;

			// ① 部門：数値2桁。チェックリスト系のボタンを押す場合のみ必須
			/*var bmncd = $.getInputboxValue($('#'+$.id.SelBumon));
			if(bmncd == '-1'){
				// E20037	部門コードを選択してください。	 	0	 	E
				$.showMessage("E20037", undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_bmncd), true)});
				return false;
			}*/

			// 選択行
			/*var row = $($.id.gridholder).datagrid("getSelected");

			// ② 画面に選択した1行の催し情報を出力する。何も選択しないと、エラー。
			if(!row){
				$.showMessage('E00008');
				return false;
			}*/

			return rt;
		},
		outputFtpSuccess: function(id,reportno){

			var that = this;

			var szMoyskbn	= $.getJSONObject(this.jsonString, $.id_inp.txt_moyskbn).value;		// 催し区分
			var szMoysstdt	= $.getJSONObject(this.jsonString, $.id_inp.txt_moysstdt).value;	// 催しコード（催し開始日）
			var szMoysrban	= $.getJSONObject(this.jsonString, $.id_inp.txt_moysrban).value;	// 催し連番
			var szBmncd		= $.getJSONObject(this.jsonString, $.id_inp.txt_bmncd).value;		// 部門コード

			var fileName ="MDCR009";
			var datalen  = 243;

			var title = 'ファイル名：';
			var br = '<br>'

			var json = [{
				"callpage":that.name,
				"FILE":fileName,
				"DREQKIND":1,			// ≒SQL実行回数
				"REQLEN":datalen,
				"MOYSKBN":szMoyskbn,
				"MOYSSTDT":szMoysstdt,
				"MOYSRBAN":szMoysrban,
				"BMNCD":szBmncd,
				"BTN":id
			}];

			// 処理時間計測用
			that.timeData = (new Date()).getTime();

			// Loading表示
			var msgCreate = '<font size="4px">　　送信中</font>';
			msgCreate += br+'<font size="2px">しばらくお待ちください</font>';
			msgCreate += br+br+'<font size="2px">'+title+fileName+'</font>';

			var panel = parent.$("#container");
			var msg=$("<div class=\"datagrid-mask-msg\" style=\"display:block;left:50%;\"></div>").html(msgCreate).appendTo(panel);
			msg._outerHeight(120);
			msg._outerWidth(200);
			msg.css({marginLeft:(-msg.outerWidth()/2),lineHeight:("25px")});

			$.ajax({
				url: $.reg.ftp,
				type: 'POST',
				async: false,
				data: {
					"page"	: reportno ,
					"obj"	: id,
					"sel"	: new Date().getTime(),
					"userid": $($.id.hidden_userid).val(),
					"user"	: $($.id.hiddenUser).val(),
					"report": $($.id.hiddenReport).val(),
					"json"	: JSON.stringify(json)
				},
				success: function(json){
					if (JSON.parse(json).length > 0) {
						$.removeMask();
						$.removeMaskMsg();

						// 正常終了の場合
						if (JSON.parse(json)[0].status==='0') {
							$.showMessage('IX1074',['',br,br+br+title+fileName]);
						} else if (JSON.parse(json)[0].code!=='530') {
							$.showMessage('EX1075',['',JSON.parse(json)[0].com,br+br+title+fileName]);
						} else {
							$.showMessage('EX1076',['',JSON.parse(json)[0].com,br+br+title+fileName]);
						}
					}
				}
			});
		},
		/**
		 * ローカルフィルター使用時のページネーション作成処理
		 * @param that js
		 * @param dg datagrid
		 * @param opts datagridのoption
		 * @param data
		 */
		createPagenation:function(that, dg, opts, data){
			var pager = dg.datagrid('getPager');
			pager.pagination({
				showPageList:false,
				showRefresh:false,
				total:data.total,
				onSelectPage:function(pageNum, pageSize){
					if (that.grdValidation()) {
						opts.pageNumber = pageNum;
						opts.pageSize = pageSize;
						pager.pagination('refresh',{
							pageNumber:pageNum,
							pageSize:pageSize
						});
						dg.datagrid('loadData',data);
					}else{
						pager.pagination('refresh',{
							pageNumber:opts.pageNumber,
							pageSize:opts.pageSize
						});
					}
				}
			});
		},
	} });
})(jQuery);