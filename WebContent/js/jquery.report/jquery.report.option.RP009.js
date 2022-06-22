/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_ReportRP009',			// （必須）レポートオプションの確認
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
		maxMergeCell: 1,
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
		baseData:[],						// 検索結果保持用
		updData:[],							// 検索結果保持用
		initialize: function (reportno){	// （必須）初期化
			var that = this;
			// 引き継ぎ情報
			this.jsonHidden = $.getTargetValue();
			// 画面の初回基本設定
			this.setInitObjectState();
			// 初期検索条件設定
			this.jsonInit = $.getInitValue();

			// データ表示エリア初期化
			that.setGrid($.id.gridholder, reportno);

			// 初期化するオブジェクト数設定
			this.initObjNum = this.dedefaultObjNum;

			var isUpdateReport = true;

			// 初期検索可能
			that.onChangeReport = true;

			var count = 1;
//			// 名称マスタ参照系
//			var meisyoSelect = Object.getOwnPropertyNames($.id_mei);
//			for ( var sel in meisyoSelect ) {
//				if($('#'+$.id_mei[meisyoSelect[sel]]).length > 0){
//					$.setMeisyoCombo(that, reportno, $.id_mei[meisyoSelect[sel]], isUpdateReport);
//					count++;
//				}
//			}
			// 入力テキストボックス系
			var inputbox = Object.getOwnPropertyNames($.id_inp);
			for ( var sel in inputbox ) {
				if($('#'+$.id_inp[inputbox[sel]]).length > 0){
					$.setInputbox(that, reportno, $.id_inp[inputbox[sel]], isUpdateReport);
					count++;
				}
			}

			// 初期化終了
			this.initializes =! this.initializes;

			$.initialDisplay(that);

//			// チェックボックスの設定
//			$.initCheckboxCss($("#"+that.focusRootId));
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

			if (sendBtnid && sendBtnid.length > 0) {
				$.reg.search = true;
			}

			// 各種ボタン
//			$('#'+$.id.btn_cancel).on("click", $.pushChangeReport);

			// 更新非対象項目は非活性に
			$.setInputBoxDisable($('#'+$.id_inp.txt_bmncd));
			$.setInputBoxDisable($('#'+$.id_inp.txt_rankno));
			$.setInputBoxDisable($('#'+$.id_inp.txt_rankkn));
			$.setInputBoxDisable($('#'+$.id_inp.txt_sryptnno));
			$.setInputBoxDisable($('#'+$.id_inp.txt_sryptnkn));
			$.setInputBoxDisable($('#'+$.id_inp.txt_rtptnno));
			$.setInputBoxDisable($('#'+$.id_inp.txt_rtptnkn));
			$.setInputBoxDisable($('#'+$.id_inp.txt_jrtptnno));
			$.setInputBoxDisable($('#'+$.id_inp.txt_jrtptnkn));
			$.setInputBoxDisable($('#'+$.id_inp.txt_sousu));
			$.setInputBoxDisable($('#'+$.id_inp.txt_goukeisu));

			$.initReportInfo("RP009", "数量計算　店別数量展開", "店別数量展開");

			// 変更
//			$($.id.hiddenChangedIdx).val('');

		},
		initCondition: function (){	// 条件初期値セット
			var that = this;
			// 初期化項目
		},
		clear:function(){
//			// 隠し情報初期化
//			$($.id.hiddenChangedIdx).val("");						// 変更行Index
//			// グリッド初期化
//			this.success(this.name, false);
		},
		validation: function (){	// （必須）批准
			var that = this;
			var rt = true;

			var txt_bmncd		= $('#'+$.id_inp.txt_bmncd).textbox('getValue');							// 部門
			var txt_rankno		= $('#'+$.id_inp.txt_rankno).textbox('getValue');							// ランクNo.

			var msgid = that.checkInputboxFunc($.id_inp.txt_rankkn, txt_bmncd, txt_rankno, '');
			if(msgid !==null){
				$.showMessage(msgid, undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_rankno), true)});
				rt = false;
			}

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
			var txt_bmncd		= $.getJSONObject(this.jsonString, $.id_inp.txt_bmncd).value;		// 部門
			var txt_rankno		= $.getJSONObject(this.jsonString, $.id_inp.txt_rankno).value;		// ランクNo.
			var rad_ptnnokbn	= $.getJSONObject(this.jsonString, $.id.rad_ptnnokbn).value;		// パターンNo.区分
			var txt_ptnno		= $.getJSONObject(this.jsonString, $.id_inp.txt_ptnno).value;		// パターンNo.
			var txt_sousu		= $.getJSONObject(this.jsonString, $.id_inp.txt_sousu).value;		// 総数量

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
					report:			that.name,		// レポート名
					BMNCD:			txt_bmncd,		// 部門コード
					RANKNO:			txt_rankno,		// ランクNo.
					PTNNOKBN:		rad_ptnnokbn,	// パターンNo.区分
					PTNNO:			txt_ptnno,		// パターンNo.
					SOUSU:			txt_sousu,		// 総数量
					t:				(new Date()).getTime(),
					sortable:		sortable,
					sortName:		that.sortName,
					sortOrder:		that.sortOrder,
					rows:			0	// 表示可能レコード数
				},
				function(json){
					// 検索処理エラー判定
					if($.searchError(json)) return false;

					var opts = JSON.parse(json).opts
					var rows = opts.rows_;
					var tensuryo = rows[0].F12;
					var limit = parseInt(txt_sousu);
					if (rad_ptnnokbn === '2' || rad_ptnnokbn === '3') {
						if(limit < tensuryo){
							$.showMessage('I30000');	// TODO
						}
					}
					// ログ出力
					$.log(that.timeData, 'query:');

					// Load処理回避
					$.tryChangeURL(null);


					var opts = JSON.parse(json).opts
					if(opts && opts.rows_){
						// 基本データ表示
						that.setData(opts.rows_, opts);
					}

					that.queried = true;
//					that.pushBtnId = btnId;
					// Load処理回避
					$.tryChangeURL($.reg.jqeasy);

					// グリッド再描画（easyui 1.4.2 対応）
					$($.id.gridholder).datagrid('load', {} );
					$.removeMask();
					$.removeMaskMsg();

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

			// 部門コード
			this.jsonTemp.push({
				id:		$.id_inp.txt_bmncd,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_bmncd),
				text:	''
			});
			// ランクNo.
			this.jsonTemp.push({
				id:		$.id_inp.txt_rankno,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_rankno),
				text:	''
			});
			// パターンNo.区分
			this.jsonTemp.push({
				id:		$.id.rad_ptnnokbn,
				value:	$.getJSONValue(this.jsonHidden, $.id.rad_ptnnokbn),
				text:	''
			});
			// パターンNo.
			this.jsonTemp.push({
				id:		$.id_inp.txt_ptnno,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_ptnno),
				text:	''
			});
			// 総数量
			this.jsonTemp.push({
				id:		$.id_inp.txt_sousu,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_sousu),
				text:	''
			});
		},
		setObjectState: function(){	// 軸の選択内容による制御

		},
		setGrid: function (id, reportNumber){	// グリッドの構築
			var that = this;
			var init = true;
			var columns = that.getGridColumns(that, id);
			// ページサイズ定義取得
			var pageList = $.fn.pagination.defaults.pageList;
			var pageSize = $.getJSONValue(this.jsonHidden, $.id.pageSize);
			if (pageSize==="") pageSize=$.fn.pagination.defaults.pageSize;
			pageSize = $.getDefaultPageSize(pageSize, pageList);
//			var dformatter =function(value){ return $.getFormatDt(value, true);};
//			var cstyler=function(value,row,index){return 'color: red;font-weight: bold;';};
//			var cformatter =function(value,row,index){return value && value===$.id.value_on?$.id.text_on:$.id.text_off;};
			$(id).datagrid({
				nowrap: true,
				border: true,
				striped: true,
				collapsible:false,
				remoteSort: true,
				rownumbers:true,
				fit:true,
//				view:scrollview,
//				pageSize:pageSize,
//				pageList:pageList,
				frozenColumns:[[]],
				columns:columns,
				fitColumns:false,	// 指定カラム幅を適用する場合、false 指定。
				rowStyler:function(index, row){
				},
				onSortColumn:function(sort, order){
					if (that.jsonString.length===0) return false;
					// カラム並び替え
					that.success(that.name, true);
				},
				onBeforeLoad:function(param){
					param.report = that.name;
				},
				onLoadSuccess:function(data){
					// 検索後、初回のみ処理
					if (that.queried){
						that.queried = false;	// 検索後、初回のみ処理
						// 状態保存
						$.saveState(reportNumber, that.getJSONString(), id);
						// 警告
						$.showWarningMessage(data);
					}

					if(init){
						init = false;

						// 総数量とGrid内数量系の差分を埋める。
						var rad_ptnnokbn	= 	$.getJSONValue(that.jsonHidden, $.id.rad_ptnnokbn);		// パターンNo.区分
						if(rad_ptnnokbn == '2' || rad_ptnnokbn == '3'){
							// 通常率パターンと実績率パターンの場合のみ、差分を埋める。
							that.addGridData(id, data.rows);
						}
					}
				},
				onClickCell:function(rowIndex, field, value){
					// 列名保持
					that.columnName = field;
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
		// グリッド内の数量を合計して、総数量と比較し、差分を検索結果に追加、削除する
		addGridData:function(id, rows){
			var rowsAf = []
			var suryoKei = 0
			var sosuryo =  $.getInputboxValue($('#'+$.id_inp.txt_sousu))		// 総数量

			for(var i = 0; i < rows.length; i++ ){
				var row = rows[i];
				suryoKei += Number(row.F3)	// 数量計
				if(Number(row.F3) != 0){
					// 数量の存在するデータのみを保持
					var rowDate = {
							IDX	 : i,
							TENCD	 : row.F1,	// 店番
							SURYO	 : row.F3,	// 数量
							BPRT	 : row.F4,	// 分配率
						};
					rowsAf.push(rowDate);
				}
			}
			// 分配率でソートを行う
			if(Number(sosuryo) > suryoKei){
				// グリッド内の数量計が指定した総数量よりも小さい場合
				// 降順でソート
				rowsAf.sort(function(a,b){
				    if(Number(a.BPRT)<Number(b.BPRT)) return 1;
				    if(Number(a.BPRT)>Number(b.BPRT)) return -1;
				    return 0;
				});

				// 数量の差分を追加する。
				var idx = 0
				var param = Math.abs(Number(sosuryo) - suryoKei);
				while(param > 0){
					if(idx == rowsAf.length - 1){
						// 最終行まで加算を行った為、再度先頭行から加算し、差分が0になるまで続ける。
						idx = 0;	// idxの初期化
					}
					rowsAf[idx].SURYO = Number(rowsAf[idx].SURYO) + 1;		// 現在の数量に1加算する。
					idx++;
					param--;	// 1加算を行った為、差分を1減らす
				}
			}else if(Number(sosuryo) < suryoKei){
				// 昇順でソート
				rowsAf.sort(function(a,b){
				    if(a.BPRT>b.BPRT) return 1;
				    if(a.BPRT<b.BPRT) return -1;
				    return 0;
				});

				// 数量の差分を減算する。
				var idx = 0
				var param = Math.abs(Number(sosuryo) - suryoKei);
				while(param > 0){
					if(idx == rowsAf.length - 1){
						// 最終行まで加算を行った為、再度先頭行から加算し、差分が0になるまで続ける。
						idx = 0;	// idxの初期化
					}
					rowsAf[idx].SURYO = Number(rowsAf[idx].SURYO) - 1;		// 現在の数量に1減算する。
					idx++;
					param--;	// 1加算を行った為、差分を1減らす
				}
			}

			// 計算したデータをGridに反映させる
			for(var i = 0; i < rowsAf.length; i++ ){
				var rowAf = rowsAf[i]
				var idx = Number(rowAf.IDX);
				rows[idx].F3 = rowAf.SURYO;			// 数量を更新する。
				$(id).datagrid('refreshRow', idx);	// 行を更新
			}
		},
		getGridColumns:function(that, id){
			var columns = [];
			var columnBottom=[];

			var iformatter	 = function(value,row,index){ return $.getFormat(value, '#,##0');};
			columnBottom.push({field:'F1',	title:'店番',				width: 100 ,halign:'center',align:'left'});
			columnBottom.push({field:'F2',	title:'店舗名',				width: 500 ,halign:'center',align:'left'});
			columnBottom.push({field:'F3',	title:'数量',				width: 100 ,halign:'center',align:'right', formatter:iformatter});
			columns.push(columnBottom);
			return columns;

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
			var sendJSON = JSON.parse( JSON.stringify( that.jsonString ) );
			$.setJSONObject(sendJSON, 'sendBtnid', btnId, $('#'+btnId).attr("title"));					// 実行ボタン情報保持
			$.setJSONObject(sendJSON, 'reportYobi1', that.reportYobiInfo(), that.reportYobiInfo());		// 参照情報保持
			$.setJSONObject(sendJSON, 'callpage', that.name, that.name);								// 呼出し元レポート情報

			// 戻る実行時用に現在の画面情報を保持する
			var states = $.getBackBaseJSON(that);
			var newrepinfos = $.getBackJSON(that, states, false);
			$.setJSONObject(sendJSON, 'repinfo', newrepinfos, '');

			// 実行ボタン別処理
			switch (btnId) {
			case $.id.btn_back:
				// 転送先情報
				index = 3;
				for (var i = 0; i < newrepinfos.length; i++) {
					var callpage = newrepinfos[i].id;
					if(callpage==='Out_ReportRP008'){
						index = 3;
					}
				}
				sendMode = 2;
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
		// IDとvalueでチェック処理を実施
		checkInputboxFunc:function(id, txt_bmncd, txt_rankno, kbn, record, isNew){
			var that = this;

			// ランク名称
			if(id===$.id_inp.txt_rankkn){
				var param = {};
				param["KEY"] =  "MST_CNT";
				param["BMNCD"] = txt_bmncd;
				param["RANKNO"] = txt_rankno;
				var chk_cnt = $.getInputboxData(that.name, $.id.action_check, $.id_inp.txt_rankkn, [param]);
				if(chk_cnt==="" || chk_cnt==="0"){
					return "EX1082";
				}
			}
			return null;
		}
	} });
})(jQuery);