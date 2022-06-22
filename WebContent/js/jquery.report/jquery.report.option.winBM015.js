/**
 * jquery sub window option
 */
;(function($) {

$.extend({

	winBM015: {
		name: 'Out_ReportwinBM015',
		prefix:'_tenkakunin',
		suffix:'_winBM015',
		initializes: true,		// 初期化フラグ（全体）
		initializesCond: true,	// 初期化フラグ（条件用）
		lastIndex: -1,			// 編集位置（行）
		sortName: '',			// ソート項目名
		sortOrder: '',			// ソート順
		timeData : (new Date()).getTime(),
		callreportno:"",		// 呼出し元レポートNo
		callBtnid: "",			// 呼出ボタンID情報
		focusRootId:"_winBM015",	// （キー移動時必須）キー移動イベントのルートとなるパネルのID
		focusParentId:"",		// （キー移動時必須）現在フォーカスがあたっている項目の親となるパネルのID
		focusGridId:"",			// （キー移動時必須）現在フォーカスがあたっているDataGridのID
		inpTenAddArr:"",
		inpTenDelArr:"",
		init: function(js) {
			var that = this;
			if(!that.initializes) return false;

			that.callreportno = js.name;

			// dataGrid 初期化
			this.setDataGrid('#grd_subwindow'+that.prefix+that.suffix);

			// 検索条件初期化
			$.setInputbox(that, that.callreportno, $.id_inp.txt_makerkn+that.suffix, false);

			// 呼出しボタンイベント設定
			$('[id^=btn'+that.prefix+']').each(function(){
				var id = $(this).attr('id');
				$('#'+id).click(function() { that.Open(this); });
			});
			// 検索
			$('#'+$.id.btn_search+that.suffix).on("click", that.Search);
			// キャンセル
			$('#'+$.id.btn_cancel+that.suffix).on("click", that.Cancel);
			// 選択
			$('#'+$.id.btn_select+that.suffix).on("click", that.Select);

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
				onBeforeOpen:function(){
					// ウインドウ展開中リサイズイベント無効化
					$.reg.resize = false;
					js.focusParentId = that.suffix;
				},
				onOpen:function(){
					$('#'+js.focusParentId).find('[tabindex]').filter("[tabindex!=-1]").filter('[disabled!=disabled]').filter(":visible").eq(0).focus();
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

			// チェックボックスの設定
			$.initCheckboxCss($("#"+that.focusRootId));
			// キーイベントの設定
			$.initKeyEvent(that);

			that.initializes = !that.initializes;
		},
		Open: function(obj) {
			if ($(obj).is("disabled","disabled"))	return false;
			var that = $.winBM015;
			var txt_moyscd = "";
			var txt_bmncd = "";
			var txt_rankno_add = "";
			var txt_rankno_del = "";
			that.callBtnid = $(obj).attr('id');

			// 呼出し元別処理
			switch (that.callreportno) {
			case 'Out_ReportBT002':
				// 検索条件初期化
				txt_moyscd = $.getInputboxText($('#'+$.id_inp.txt_moyscd));
				txt_bmncd = $.getInputboxValue($('#'+$.id_inp.txt_bmncd));
				txt_rankno_add = $.getInputboxValue($('#'+$.id_inp.txt_rankno_add));
				txt_rankno_del = $.getInputboxValue($('#'+$.id_inp.txt_rankno_del));
				// オブジェクト作成
				$('#'+that.suffix).window({title: '分類割引　対象店確認(BT010)'});

				// 入力チェック
				if(!txt_moyscd || txt_moyscd == ""){
					$.showMessage('E30012',['催しコード']);
					return false;
				}

				break;
			case 'Out_ReportBM006':
				// 検索条件初期化
				txt_moyscd = $.getInputboxText($('#'+$.id_inp.txt_moyscd));
				txt_bmncd = $.getInputboxValue($('#'+$.id_inp.txt_bmncd));
				txt_rankno_add = $.getInputboxValue($('#'+$.id_inp.txt_rankno_add));
				txt_rankno_del = $.getInputboxValue($('#'+$.id_inp.txt_rankno_del));

				// オブジェクト作成
				$('#'+that.suffix).window({title: 'B/M別送信情報　対象店確認(BM015)'});
				break;
			case 'Out_ReportGM003':
				// 検索条件初期化
				txt_moyscd = $.getInputboxText($('#'+$.id_inp.txt_moyscd));
				txt_bmncd = $.getInputboxValue($('#'+$.id_inp.txt_bmncd));
				txt_rankno_add = $.getInputboxValue($('#'+$.id_inp.txt_rankno_add));
				txt_rankno_del = $.getInputboxValue($('#'+$.id_inp.txt_rankno_del));

				// オブジェクト作成
				$('#'+that.suffix).window({title: 'セット販売　対象店確認(GM003)'});
				break;
			default:
				break;
			}

			var re1 = new RegExp("^[0-9]{1," + $.len.bmncd + "}$");
			var re2 = new RegExp("^[0-9]{1}-[0-9]{6}-[0-9]{1,3}$");
			var re3 = new RegExp("^[0-9]{1," + $.len.rankno + "}$");
			var err = false;

			// 対象・除ランク№のどちらかに900以上が入力された場合は催しコードが必須となる①
			if ((txt_rankno_add >= 900 || txt_rankno_del >= 900) && !txt_moyscd.match(re2)) {
				err = true;
			}

			// ①にあてはまらない場合部門、対象店ランク№は必須
			if(!txt_bmncd.match(re1)){
				$.showMessage('E20125');
				return false;
			}

			if(!txt_rankno_add.match(re3)){
				$.showMessage('EX1086');
				return false;
			}

			if(err){
				$.showMessage('E30012',['催しコード']);
				return false;
			}

			// 対象・除外店ランク№に同一の値が入力された場合エラー
			if (txt_rankno_add===txt_rankno_del) {
				$.showMessage('E20016'); //対象店ランクNo.と除外店ランクNo.が同じです。
				return false;
			}

			// 対象店・除外店を作成
			var tencds = [];
			that.inpTenAddArr = [];
			that.inpTenDelArr = [];

			for (var i = 0; i < 10; i++) {
				var tenCd = $.getInputboxValue($('#'+$.id_inp.txt_tencd+'_add_' + (i+1))).trim();
				if (tenCd!=="" && tenCd!==null && tenCd!==undefined) {
					var msgid = that.checkMstTenFunc(tenCd);
					if(msgid!==""){
						$.showMessage(msgid);
						return false;
					}
					that.inpTenAddArr.push(tenCd)
					tencds.push(tenCd);
				}

				tenCd = $.getInputboxValue($('#'+$.id_inp.txt_tencd+'_del_' + (i+1)));
				if (tenCd!=="" && tenCd!==null && tenCd!==undefined) {
					var msgid = that.checkMstTenFunc(tenCd);
					if(msgid!==""){
						$.showMessage(msgid);
						return false;
					}
					that.inpTenDelArr.push(tenCd);
					tencds.push(tenCd);
				}
			}

			// 重複チェック
			var tencds_ = tencds.filter(function (element, index, self) { return self.indexOf(element)*1 === index; });
			if(tencds.length !== tencds_.length){
				$.showMessage('E11141');
				return false;
			}

			// hidden項目に配列を保持(部門_対象店ランク店配列_除外店ランク店配列の形式)
			var tenRankArr = $.getInputboxValue($('#'+$.id_inp.txt_rankno_add + '_arr')).split('_');

			// 対象店・除外店の入力がない場合チェック不要
			if (tenRankArr.length >= 2) {
				var tenAddErr = [];
				var tenRankAddArrSplit = tenRankArr[1].split("");

				if (tenRankAddArrSplit.length===0) {
					$.showMessage('E20027'); // 対象店舗が1つも存在しません。
					return false;
				}

				// ランクマスタから取得した対象店と入力された対象店に重複があった場合エラー
				for (var i = 0; i < that.inpTenAddArr.length; i++) {
					if (tenRankAddArrSplit.length >= that.inpTenAddArr[i] && tenRankAddArrSplit[that.inpTenAddArr[i]-1].trim()!=="") {

						// 対象店のみの指定の場合エラー、それ以外の場合はエラー店舗を一度保持
						if (tenRankArr.length===2) {
							$.showMessage('E20025'); // 既に対象店となっている店を追加しようとしました。
							return false;
						} else {
							// ランクマスタから取得した除外店に同一の店舗があった場合OKの為一度保持②
							tenAddErr.push(that.inpTenAddArr[i]);
						}
					}
				}

				// 除外店の指定が存在する場合以下をチェック
				if (tenRankArr.length===3) {
					var tenRankDelArrSplit = tenRankArr[2].split("");
					var err = true;

					// 部門・対象・除外店ランク№入力時点で取得した配列を展開し対象店が存在していることを確認
					for (var i = 0; i < tenRankAddArrSplit.length; i++) {
						if (tenRankDelArrSplit.length >= (i+1) && (tenRankAddArrSplit[i].trim()!=="" && tenRankDelArrSplit[i].trim()==="")) {
							// 一件でも対象店が存在すればOK
							err = false;
							break;
						}
					}

					if (err) {
						$.showMessage('E20027'); // 対象店舗が1つも存在しません。
						return false;
					}

					// ランクマスタから取得した除外店と入力された除外店に重複があった場合エラー
					for (var i = 0; i < that.inpTenDelArr.length; i++) {
						if (tenRankDelArrSplit.length >= that.inpTenDelArr[i] && tenRankDelArrSplit[that.inpTenDelArr[i]-1].trim()!=="") {
							$.showMessage('E20026'); // 既に対象店ではない店を除外しようとしました。
							return false;
						}
					}

					// ランクマスタから取得した除外店と②に重複がなかった場合エラー
					for (var i = 0; i < tenAddErr.length; i++) {
						if (tenRankDelArrSplit.length >= tenAddErr[i] && tenRankDelArrSplit[tenAddErr[i]-1].trim()==="") {
							$.showMessage('E20025'); // 既に対象店となっている店を追加しようとしました。
							return false;
						}
					}
				}
			} else {
				$.showMessage('E20014'); // 対象店ランクNo.が存在しません。
				return false;
			}

			that.Search();

			// window 表示
			$('#'+that.suffix).window('open');
		},
		Clear:function(){
			var that = $.winBM015;
			that.initializesCond = true;
			// グリッド初期化
			$('#grd_subwindow'+that.prefix+that.suffix).datagrid('clearSelections').datagrid({data: []}).datagrid('getPager').pagination('select', 1);	// ページ初期化
			that.initializesCond = false;
		},
		Search: function(){
			var that = $.winBM015;

			// validate=falseの場合何もしない
			if(!that.validation()){ return false; }

			// セッションタイムアウト、利用時間外の確認
			var isTimeout = $.checkIsTimeout();
			if (! isTimeout) {
				// 検索実行
				//that.success("grd_subwindow"+that.prefix+that.suffix);
				that.success("grd_subwindow"+that.prefix);
			}

			return true;
		},
		Cancel:function(){
			var that = $.winBM015;
			$('#'+that.suffix).window('close');
			return true;
		},
		Select: function(){
			var that = $.winBM015;

			var row = $("#grd_subwindow"+that.prefix+that.suffix).datagrid("getSelected");
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
			var that = $.winBM015;
			// 処理時間計測用
			that.timeData = (new Date()).getTime();

			// グリッド初期化&ローディング
			$('#'+id).datagrid('loading');

			// 情報設定
			var json = [{}];

			// 呼出し元別処理
			switch (that.callreportno) {
			case 'Out_ReportBT002':
			case 'Out_ReportBM006':
			case 'Out_ReportGM003':

				// hidden項目に配列を保持(部門_対象店ランク店配列_除外店ランク店配列の形式)
				var tenRankArr = $.getInputboxValue($('#'+$.id_inp.txt_rankno_add + '_arr'));

				// オブジェクト作成
				json = [{
					callpage:	$($.id.hidden_reportno).val(),									// 呼出元レポート名
					BMNCD:		$.getInputboxValue($('#'+$.id_inp.txt_bmncd)),					// 部門
					MOYSKBN:	$.getInputboxText($('#'+$.id_inp.txt_moyscd)).split("-")[0],	// 催し区分
					MOYSSTDT:	$.getInputboxText($('#'+$.id_inp.txt_moyscd)).split("-")[1],	// 催し開始日
					MOYSRBAN:	$.getInputboxText($('#'+$.id_inp.txt_moyscd)).split("-")[2],	// 催し連番
					RANKNOADD:	$.getInputboxValue($('#'+$.id_inp.txt_rankno_add)),				// 対象店ランク№
					RANKNODEL:	$.getInputboxValue($('#'+$.id_inp.txt_rankno_del)),				// 除外店ランク№
					TJTENADD:	that.inpTenAddArr,												// 対象店
					TJTENDEL:	that.inpTenDelArr,												// 除外店
					TENRANK_ARR:	tenRankArr,													// 対象店ランク配列
				}];
				break;
			default:
				break;
			}

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
					var dg =$('#'+id+that.suffix);
					if(data!==""){
						// JSONに変換
						var json = JSON.parse(data);

						// 結果表示
						dg.datagrid('loadData', json.rows);
					}
					dg.datagrid('loaded');

					// メインデータ表示
					that.setData(json.rows);

					// ログ出力
					$.log(that.timeData, 'loaded:');
				}
			);
		},
		setDataGrid: function(id) {
			var that = this;

			// 呼出し元別処理
			var columns = null;
			switch (that.callreportno) {
			case 'Out_ReportBT002':
			case 'Out_ReportBM006':
			case 'Out_ReportGM003':
				var targetIdTen = $.id_inp.txt_tencd+that.suffix;
				var checkTen = $('#'+targetIdTen).attr("check") ? JSON.parse('{'+$('#'+targetIdTen).attr("check")+'}'): JSON.parse('{}');	// コードのcheck要素を取得
				var formatterLPadTen = function(value){
					return $.getFormatLPad(value, checkTen.maxlen);
				};

				// オブジェクト作成
				columns = [[
							{field:'TENPOSU',	title:'店舗数',			hidden:true},
							{field:'TENCD',		title:'店番',			width: 50,halign:'center',align:'left',formatter:formatterLPadTen},
							{field:'TENKN',		title:'店舗名称',		width: 300,halign:'center',align:'left'},
							{field:'AREACD',	title:'エリア',			width: 50,halign:'center',align:'left'},
						]];
				break;
			default:
				break;
			}

			$(id).datagrid({
				nowrap: true,
				border: true,
				striped: true,
				collapsible:false,
				remoteSort: true,
				rownumbers:true,
				fit:true,
				view:scrollview,
				frozenColumns:[[]],
				columns:columns,
				fitColumns:false,	// 指定カラム幅を適用する場合、false 指定。
				rowStyler:function(index, row){
				},
				onLoadSuccess:function(data){
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
		setData: function(rows, opts){		// データ表示
			var that = this;
			if(rows.length > 0){
				$('#'+that.focusRootId).find('[col^=F]').each(function(){
					//var col = $(this).attr('col');
					var col = 'TENPOSU'
					if(rows[0][col]){
						$.setInputboxValue($(this), rows[0][col]);
					}
				});
			}
		},
		getTenArr: function(id){
			if (id===$.id_inp.txt_rankno_add || id===$.id_inp.txt_rankno_del || id===$.id_inp.txt_bmncd) {
				var that = this;
				var params = {};
				params["ID"] = id + '_arr';
				params["BMNCD"] = $.getInputboxValue($('#'+$.id_inp.txt_bmncd));
				params["MOYSCD"] = $.getInputboxValue($('#'+$.id_inp.txt_moyscd));
				params["RANKNOADD"] = $.getInputboxValue($('#'+$.id_inp.txt_rankno_add));
				params["RANKNODEL"] = $.getInputboxValue($('#'+$.id_inp.txt_rankno_del));

				// 入力値に紐づく店配列を取得・設定(部門_対象店ランク№配列_除外店ランク№配列の形式)
				$.getsetInputboxData(that.callreportno, $.id_inp.txt_rankno_add+'_arr', [params], $.id.action_change);
			}
		},
		checkMstTenFunc: function(val){
			var that = this;
			// 店コード存在チェック
			if (val !== null && val !== '' && val !== undefined) {

				// 店コードが401以上のものはエラー
				if (val > 400) {
					return "E20520";
				}

				// マスタ存在チェック
				var param = {};
				param["KEY"] =  "MST_CNT";
				param["value"] = val;

				var chk_cnt = $.getInputboxData(that.name, $.id.action_check, $.id_inp.txt_tencd, [param]);
				if(chk_cnt==="" || chk_cnt==="0"){
					return "E11096";
				}
			}
			return "";
		}
	}
});

})(jQuery);