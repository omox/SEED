/**
 * jquery sub window option
 */
;(function($) {

$.extend({

	winTJ010: {
		name: 'Out_ReportwinTJ010',
		prefix:'_keikaku',
		suffix:'_winTJ010',
		initializes: true,		// 初期化フラグ（全体）
		initializesCond: true,	// 初期化フラグ（条件用）
		lastIndex: -1,			// 編集位置（行）
		sortName: '',			// ソート項目名
		sortOrder: '',			// ソート順
		timeData : (new Date()).getTime(),
		callreportno:"",		// 呼出し元レポートNo
		callBtnid: "",			// 呼出ボタンID情報
		callreportHidden:[],	// 呼出し元レポートからの引き継ぎ情報
		focusRootId:"_winTJ010",// （キー移動時必須）キー移動イベントのルートとなるパネルのID
		focusParentId:"",		// （キー移動時必須）現在フォーカスがあたっている項目の親となるパネルのID
		focusGridId:"",			// （キー移動時必須）現在フォーカスがあたっているDataGridのID
		palentUpdateData:[],	// 親画面側の変更データ
		init: function(js) {
			var that = this;
			if(!that.initializes) return false;

			that.callreportno = js.name;
			that.callreportHidden = js.jsonHidden;
			that.palentUpdateData = js.updateData

			// 呼出しボタンイベント設定
			$('[id^=btn'+that.prefix+']').each(function(){
				var id = $(this).attr('id');

				$('#'+id).click(function() {
					// 呼出し元画面(TJ009)の変更データ抽出処理(setUpdateData)を実行する。
					js.setUpdateData('gridholder')	// 変更情報の取得

					that.Open(this);
				});
			});

			// キャンセル
			$('#'+$.id.btn_cancel+that.suffix).on("click", that.Cancel);

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
			var that = $.winTJ010;
			that.callBtnid = $(obj).attr('id');

			// 呼出し元別処理
			switch (that.callreportno) {
			case 'Out_ReportTJ009':
			}

			// window 表示
			$('#'+that.suffix).window('open');

			// 検索実行
			that.Search();
		},
		setWindowData: function(rows) {
			var that = $.winTJ010;

			// var rows = $($.id.gridholder).datagrid('getRows');

			var shnkbn = 'SHNKBN_';
			var htsu = 'HTSU_';

			var yosan = 0;//予算/売価計①
			var ararigaku = 0;//予算/構成比
			var reBaikakei = 0;
			var reUriagekouseihi = 0;
			var reUrikouseihi = 0;
			var reArarigaku = 0;
			var reArarikouseihi = 0;

			var tBaikakei = 0;//特売/売価計
			var tZizengenkakei = 0;//特売/売価計
			var tUriagekouseihi = 0;
			var tArarigaku = 0;
			var tArarikousei = 0;

			var yBaikakei = 0;//山積み/売価計
			var yZizenbaikakei = 0;//山積み/売価計
			var yUriagekouseihi = 0;
			var yArarigaku = 0;
			var yArarikousei = 0;

			var kaUriagekouseihi = 0;//過不足/売価計
			var kaUrikouseihi = 0;//過不足/売上構成比
			var kaArarigaku = 0;//過不足/荒利額
			var kaArarikouseihi = 0;//過足/荒利構成比

			// 部門予算取得
			if(rows.length > 0){
				if(rows[0]['BMNYSANAM_KEI']){
					yosan = Number(rows[0]['BMNYSANAM_KEI'])
				}
			}

			reBaikakei			 = Math.round(((yosan * $.getInputboxValue($('#txt_kouseihi1')))/100)*100)/100;//⑪
			reUriagekouseihi	 = Math.round($.getInputboxValue($('#txt_kouseihi1'))*100)/100;//⑫
			reArarigaku			 = (yosan * $.getInputboxValue($('#txt_kouseihi2')))/100;//⑬
			reArarikouseihi		 = Math.round($.getInputboxValue($('#txt_kouseihi2'))*100)/100;//⑭
			ararigaku			 = Math.round((yosan * $.getInputboxValue($('#txt_kouseihi3')))/100);//②


			if(rows.length > 0){
				for(var i = 0;i<rows.length;i++){

					var tokubaiHtsu	 = 0;
					var sonotaHtsu	 = 0;
					var irisu		 = 0;   // 入数_特売
					var tokhonbik	 = 0;	// 本体売価
					var zizengenjka	 = 0;	// 事前原価

					for(var j = 1;j<=10;j++){
						var htsu =  rows[i][('HTSU_'+('0'+j).slice(-2))];
						if(htsu){
							if(Number(htsu) == 99999){
								// デフォルト値の場合は0として扱う
								htsu = 0
							}

							if(rows[i]['SHNKBN_'+('0'+j).slice(-2)]==1){
								tokubaiHtsu += Number(htsu);
							}else{
								sonotaHtsu += Number(htsu);
							}
						}
					}

					// 本体売価取得
					if(rows[i]['IRISU_TB']){
						tokhonbik = Number(rows[i]['BAIKAAM_TB'])
					}

					// 事前原価取得
					if(rows[i]['GENKAAM_MAE']){
						zizengenjka = Number(rows[i]['GENKAAM_MAE'])
					}

					// 入数取得取得
					if(rows[i]['IRISU_TB']){
						irisu = Number(rows[i]['IRISU_TB'])
					}

					// 発注数に入数をか掛ける
					if(tokubaiHtsu != 0){
						tokubaiHtsu = tokubaiHtsu * irisu
					}
					if(sonotaHtsu != 0){
						sonotaHtsu = sonotaHtsu * irisu
					}

					// 総売価金額(千円単位)計算
					tBaikakei		 += (Number(tokubaiHtsu) * Number(tokhonbik)/1000);//③
					yBaikakei		 += (Number(sonotaHtsu) * Number(tokhonbik)/1000);//⑦

					// 粗利金額(千円単位)計算
					tArarigaku += (Number(tokhonbik) - Number(zizengenjka)) * Number(tokubaiHtsu) / 1000 //⑤
					yArarigaku += (Number(tokhonbik) - Number(zizengenjka)) * Number(sonotaHtsu) / 1000 //⑨

					tokubaiHtsu = 0;
					sonotaHtsu = 0;

				}
			}

			// 集計した項目の数値を丸める
			tBaikakei = Math.round(tBaikakei*100)/100;
			yBaikakei = Math.round(yBaikakei*100)/100;

			tArarigaku = Math.round(tArarigaku*100)/100;
			yArarigaku = Math.round(yArarigaku*100)/100;

			if(Number(yosan)>0){
				tUriagekouseihi = Math.round((Number(tBaikakei) / Number(yosan) * 100)*100)/100;//④
			}else{
				tUriagekouseihi = 0;
			}

			if(Number(ararigaku)>0){
				tArarikousei = Math.round((Number(tArarigaku)/Number(ararigaku)*100)*100)/100;//⑥
			}else{
				tArarikousei = 0;
			}

			if(Number(yosan)>0){
				yUriagekouseihi = Math.round((Number(yBaikakei) / Number(yosan) * 100)*100)/100;//⑧
			}else{
				yUriagekouseihi = 0;
			}

			if(Number(ararigaku)>0){
				yArarikousei = Math.round((Number(yArarigaku)/Number(ararigaku)*100)*100)/100;//⑩
			}else{
				yArarikousei = 0;
			}

			kaUriagekouseihi = (Number(tBaikakei)+Number(yBaikakei)+Number(reBaikakei))-Number(yosan);//⑮
			if(Number(yosan)>0){
				kaUrikouseihi = Math.round(Number(kaUriagekouseihi)/Number(yosan)*100*100)/100;//⑯
			}else{
				kaUrikouseihi = 0;
			}

			kaArarigaku = Number(tArarigaku)+Number(yArarigaku)+Number(reArarigaku)-Number(ararigaku);//⑰
			if(Number(ararigaku)>0){
				kaArarikouseihi =Math.round(Number(kaArarigaku)/Number(ararigaku)*100*100)/100//⑱
			}else{
				kaArarikouseihi = 0;
			}

			$.setInputboxValue($('#txt_yosankei1')	, Math.round(yosan));
			$.setInputboxValue($('#txt_yosankei2')	, Math.round(ararigaku));
			$.setInputboxValue($('#txt_tokubai1'), tBaikakei);
			$.setInputboxValue($('#txt_tokubai2'), tUriagekouseihi);
			$.setInputboxValue($('#txt_tokubai3'), tArarigaku);
			$.setInputboxValue($('#txt_tokubai4'), tArarikousei);
			$.setInputboxValue($('#txt_yamadumi1'), yBaikakei);
			$.setInputboxValue($('#txt_yamadumi2'), yUriagekouseihi);
			$.setInputboxValue($('#txt_yamadumi3'), yArarigaku);
			$.setInputboxValue($('#txt_yamadumi4'), yArarikousei);
			$.setInputboxValue($('#txt_regular1'), reBaikakei);
			$.setInputboxValue($('#txt_regular2'), reUriagekouseihi);
			$.setInputboxValue($('#txt_regular3'), reArarigaku);
			$.setInputboxValue($('#txt_regular4'), reArarikouseihi);
			$.setInputboxValue($('#txt_kabusoku1'), kaUriagekouseihi);
			$.setInputboxValue($('#txt_kabusoku2'), kaUrikouseihi);
			$.setInputboxValue($('#txt_kabusoku3'), kaArarigaku);
			$.setInputboxValue($('#txt_kabusoku4'), kaArarikouseihi);

			var txt_moyscd = "";
			var txt_bmncd = "";
			var chk_rinji = $.id.value_off;

		},
		Clear:function(){
			var that = $.winTJ010;
			that.initializesCond = true;

			that.initializesCond = false;
		},
		Search: function(){
			var that = $.winTJ010;

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
		Cancel:function(){
			var that = $.winTJ010;
			$('#'+that.suffix).window('close');
			return true;
		},
		validation: function (){	// （必須）批准
			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform+that.suffix).form('validate');
			return rt;
		},
		success: function(id){	// 検索処理
			var that = $.winTJ010;
			// 処理時間計測用
			that.timeData = (new Date()).getTime();

			// グリッド初期化&ローディング
			$('#'+id).datagrid('loading');

			var szBmncd	= $.getJSONValue(that.callreportHidden, $.id.SelBumon);			// 部門コード
			var szLstno	= $.getJSONValue(that.callreportHidden, $.id_inp.txt_lstno);	// リスト№

			// 情報設定
			var json = [{
				callpage:	$($.id.hidden_reportno).val(),		// 呼出元レポート名
				BMNCD:		szBmncd.split("-")[0],
				LSTNO:		szLstno,
				BTNID:		that.callBtnid						// 呼出ボタン
			}];

			$.post(
				$.reg.easy,
				{
					page	:	that.name,					// レポート名
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
					var dg =$('#'+id);

					var rows = []

					if(data!==""){
						// JSONに変換
						var json = JSON.parse(data);

						// 結果表示
						rows = json.rows
					}

					// 画面上で変更されたデータを検索値にマージする。
					if(that.palentUpdateData.length > 0){
						for (var i=0; i< that.palentUpdateData.length; i++){
							var data = that.palentUpdateData[i]

							if(data.type =='shnData'){
								var dispNo = data.rows.F3
							}

							var newLines = rows.filter(function(item, index){
								if(item.HYOSEQNO == dispNo){
									// 変更データと同じ主キーを持つデータが存在する場合
									// 変更対象行のデータを上書きする
									rows[index].HTSU_01	= data.rows.F5		// 数量1
									rows[index].HTSU_02	= data.rows.F6		// 数量2
									rows[index].HTSU_03 = data.rows.F7		// 数量3
									rows[index].HTSU_04 = data.rows.F8		// 数量4
									rows[index].HTSU_05 = data.rows.F9		// 数量5
									rows[index].HTSU_06 = data.rows.F10		// 数量6
									rows[index].HTSU_07 = data.rows.F11		// 数量7
									rows[index].HTSU_08 = data.rows.F12		// 数量8
									rows[index].HTSU_09 = data.rows.F13		// 数量9
									rows[index].HTSU_10 = data.rows.F14		// 数量10

									$($.id.gridholder).datagrid('refreshRow', index);
								}
							});
						}
					}

					// 検索結果をもとに計算結果を表示する。
					that.setWindowData(rows)

					// dg.datagrid('loaded');
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
			case 'Out_ReportXXXXX':
				columns = [[]];
				break;
			default:
				// オブジェクト作成
				columns = [[
							{field:'F1', title:'ランクNo',		width: 70,halign:'center',align:'left'},
							{field:'F2', title:'ランク名称',	width:400,halign:'center',align:'left'},
							{field:'F3', title:'店舗数',		width: 69,halign:'center',align:'right'}
						]];
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
		}
	}
});

})(jQuery);