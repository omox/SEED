/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_ReportTG040',			// （必須）レポートオプションの確認
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
		dedefaultObjNum:	4,	// 初期化オブジェクト数
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
		sendBtnid: "",						// 呼出ボタンID情報
		pushBtnid: "",						// 実行ボタンID情報(検索系で利用)
		focusRootId:"cc",					// （キー移動時必須）キー移動イベントのルートとなるパネルのID
		focusParentId:"",					// （キー移動時必須）現在フォーカスがあたっている項目の親となるパネルのID
		focusGridId:"",						// （キー移動時必須）現在フォーカスがあたっているDataGridのID
		editRowIndex:{},					// グリッド編集行保持

		baseTablekbn:"",					// 検索結果のテーブル区分：0-正/1-予約(※予約1の新規→正を参照しているので正、予約2の新規→予約1を参照しているので予)
		baseData:[],						// 検索結果保持用
		subData:[],							// 検索結果保持用(グリッド情報)

		grd_data:[],						// メイン情報：商品マスタ
		grd_tencd_data:[],					// グリッド情報：店コード
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
			// 入力テキストボックス系
			$.setInputbox(that, reportno, $.id_inp.txt_moyscd, false);
			$.setInputbox(that, reportno, $.id_inp.txt_moykn,  false);
			$.setInputbox(that, reportno, $.id_inp.txt_hbstdt, false);
			$.setInputbox(that, reportno, $.id_inp.txt_hbeddt, false);
			// 初期条件設定
			that.initCondition();

			// データ表示エリア初期化
			that.setGrid($.id.gridholder.replace('#', ''), reportno);
			that.setCopyGrid($.id.grd_tengp, reportno);

			// Load処理回避
			//$.tryChangeURL(null);

			// 初期化終了
			this.initializes =! this.initializes;

			// チェックボックスの設定
			$.initCheckboxCss($("#"+that.focusRootId));
			// キーイベントの設定
			$.initKeyEvent(that);

			// ログ出力
			$.log(that.timeData, 'initialize:');
		},
		repgrpInfo: {
			TG001:{idx:1},		// 月間販売計画 新規・変更
			TG001_1:{idx:2},	// 月間販売計画 参照
			TG002:{idx:3},		// 月間販売計画 店舗グループ一覧
			TG003:{idx:4},		// 月間販売計画 店舗グループ店情報
			TG008:{idx:5},		// 月間販売計画 商品一覧
			TG040:{idx:6},		// 月間販売計画 コピー元店舗グループ一覧
			TG016:{idx:7}		// 商品情報
		},
		setInitObjectState: function(){	// 画面初期化時の項目制御
			var that = this;
			// 引き継ぎ情報セット
			var sendBtnid = $.getJSONValue(that.jsonHidden, "sendBtnid");
			if(!sendBtnid){
				sendBtnid = $('#sendBtnid').val();
			}
			$('#sendBtnid').val(sendBtnid);
			var reportYobi1 = $.getJSONValue(that.jsonHidden, "reportYobi1");
			if(!reportYobi1){
				reportYobi1 = $('#reportYobi1').val();
			}
			$('#reportYobi1').val(reportYobi1);

			// 当帳票を「参照」で開いた場合
			if(that.reportYobiInfo()==='1'){
				$.initReportInfo("TG040", "月間販売計画（チラシ計画）　コピー元店舗グループ　選択（参照）", "参照");
			}else{
				// 各種ボタン
				$('#'+$.id.btn_sel_change).on("click", function(e){
					var row = $($.id.gridholder).datagrid("getSelected");
					if(row){
						$('#'+$.id.grd_tengp).datagrid('reload');
					}
				});
				$.initReportInfo("TG040", "月間販売計画（チラシ計画）　コピー元店舗グループ　選択", "選択");
			}
			$($.id.buttons).show();

			// 変更
			$($.id.hiddenChangedIdx).val('');
		},
		initCondition: function (){	// 条件初期値セット
			var that = this;

			// 初期化項目
			$.setInputboxValue($("#"+$.id_inp.txt_moyscd), $.getJSONValue(that.jsonInit, $.id_inp.txt_moyscd));
		},
		clear:function(){
			// 隠し情報初期化
			$($.id.hiddenChangedIdx).val("");						// 変更行Index
		},
		validation: function (btnId){	// （必須）批准
			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform).form('validate');

			// 2.1.1．【画面】.「催しコード」のみ入力する場合：
			// ①    催しコード入力内容チェック：催しコードが10桁以外の場合、エラー。
			var txt_moyscd		= $.getInputboxValue($('#'+$.id_inp.txt_moyscd));	// 催しコード
			if(rt){
				if(!$.isEmptyVal(txt_moyscd) && txt_moyscd.length !== 10){
					// E20138	催しコードは10桁数字を入力してください。	 	0	 	E
					$.showMessage('E20138', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_hbstdt), true)});
					rt = false;
				}
			}

			// 2.1.2.1．【画面】.「催し期間From」と【画面】.「催し期間To」項目入力チェック：
			// ①	表示年月Fromと表示年月To未入力の場合、全てのレコードを検索する。
			// ②	表示年月From未入力、表示年月Toだけ入力の場合、エラー。
			// ③	表示年月Fromだけ入力、表示年月To未入力の場合、>=表示年月Fromの内容を検索。
			// ④	表示年月From >　表示年月Toの場合、エラー。
			var txt_hbstdt		= $.getInputboxValue($('#'+$.id_inp.txt_hbstdt), "", true);	// 表示年月From
			var txt_hbeddt		= $.getInputboxValue($('#'+$.id_inp.txt_hbeddt), "", true);	// 表示年月To
			if(rt){
				if(txt_hbstdt === "" && txt_hbeddt !== ""){			// ②
					// E30012	催し開始日Toのみの入力はできません。催し開始日Fromを入力してください	 	0	 	E
					$.showMessage('E30012', ["催し開始日Toのみの入力はできません。催し開始日From"], function(){$.addErrState(that, $('#'+$.id_inp.txt_hbstdt), true)});
					rt = false;
				}
			}
			if(rt){
				if (txt_hbeddt !== "" && txt_hbstdt > txt_hbeddt){
					// E20139	催し開始日From ≦ 催し開始日Toの条件で入力してください。	 	0	 	E
					$.showMessage('E20139', undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_hbstdt), true)});
					rt = false;
				}
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
			var szMoyscd	= $.getJSONObject(this.jsonString, $.id_inp.txt_moyscd).value;	// 催しコード
			var szMoykn		= $.getJSONObject(this.jsonString, $.id_inp.txt_moykn).value;	// 催し名称
			var szHbstdt	= $.getJSONObject(this.jsonString, $.id_inp.txt_hbstdt).value;	// 催し期間From
			var szHbeddt	= $.getJSONObject(this.jsonString, $.id_inp.txt_hbeddt).value;	// 催し期間To
			if(!btnId) btnId = $.id.btn_search;

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
					BTN:			btnId,
					MOYSCD:			szMoyscd,		// 催しコード
					MOYSKN:			szMoykn,		// 催し名称
					HBSTDT:			szHbstdt,		// 催し期間From
					HBEDDT:			szHbeddt,		// 催し期間To
					t:				(new Date()).getTime(),
					sortable:		sortable,
					sortName:		that.sortName,
					sortOrder:		that.sortOrder,
					rows:			999	// 表示可能レコード数
				},
				function(json){
					// 検索処理エラー判定
					if($.searchError(json, undefined, that)) return false;

					// ログ出力
					$.log(that.timeData, 'query:');

					// Load処理回避
					$.tryChangeURL(null);

					if (sortable===0){
						var options = $($.id.gridholder).datagrid('options');
						// 初期検索時に並び替え情報のリセット
						options.sortName = null;
						options.sortOrder = null;
					}

					that.queried = true;
					that.pushBtnid = btnId;
					// Load処理回避
					$.tryChangeURL($.reg.jqeasy);

					// グリッド再描画（easyui 1.4.2 対応）
					$($.id.gridholder).datagrid('load', {} );
					$.removeMask();

					// ログ出力
					$.log(that.timeData, 'loaded:');
				}
			);
		},
		getGridData: function (target){
			var that = this;
			var data = {};

			var szMoyskbn	= $.getJSONObject(this.jsonString, $.id_inp.txt_moyskbn).value;		// 催し区分
			var szMoysstdt	= $.getJSONObject(this.jsonString, $.id_inp.txt_moysstdt).value;	// 催しコード（催し開始日）
			var szMoysrban	= $.getJSONObject(this.jsonString, $.id_inp.txt_moysrban).value;	// 催し連番

			// 基本情報
			if(target===undefined || target==="grd_data"){
				var targetData = [];
				var rows = $('#'+$.id.grd_tengp).datagrid('getRows');
				for (var i=0; i<rows.length; i++){
					if(rows[i]["KYOSEIFLG"]!==$.id.value_on){
						var rowData = {
								F1 : szMoyskbn,				// F1 : 催し区分	MOYSKBN
								F2 : szMoysstdt,			// F2 : 催し開始日	MOYSSTDT
								F3 : szMoysrban,			// F3 : 催し連番	MOYSRBAN
								F4 : rows[i]["TENGPCD"],	// F4	TENGPCD		店グループ
								F5 : rows[i]["TENGPKN"],	// F5	TENGPKN		店グループ名称
								F6 : rows[i]["KYOSEIFLG"],	// F6	KYOSEIFLG	強制グループフラグ
								F7 : rows[i]["QASYUKBN"],	// F7	QASYUKBN	アンケート種類
								F8 : rows[i]["QACREDT_K"],	// F8	QACREDT_K	アンケート作成日_強制
								F9 : rows[i]["QARCREDT_K"],	// F9	QARCREDT_K	アンケート再作成日_強制
								F10: rows[i]["UPDKBN"]		// F10	UPDKBN	更新区分

								,F1C: rows[i]["MOYSKBN"]	// F1 : 催し区分	MOYSKBN
								,F2C: rows[i]["MOYSSTDT"]	// F2 : 催し開始日	MOYSSTDT
								,F3C: rows[i]["MOYSRBAN"]	// F3 : 催し連番	MOYSRBAN
								,F4C: rows[i]["TENGPCD"]	// F4 : 店グループ	TENGPCD
								,RNO:i
							};
						targetData.push(rowData);
					}
				}
				data["grd_data"] = targetData;
			}

			return data;
		},
		setGridData: function (data, target){
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

			var login_dt = parent.$('#login_dt').text().replace(/\//g, "");	// 処理日付
			var sysdate = login_dt.substr(2, 6);							// 比較用処理日付

			// 新規(正) 1.1　必須入力項目チェックを行う。
			// 変更(正) 1.1　必須入力項目チェックを行う。
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform).form('validate');
			if(!rt){
				$.showMessage('E00001');
				return rt;
			}
			// 入力項目チェック
			var shoridt = $('#'+$.id.txt_shoridt).val();

			// E20259	コピー対象の以下の店舗が廃店となっています。	コピー処理終了後、TG003店舗グループ店情報画面で以下の店舗を削除してください。	0	 	E


			// 2.3.1．【画面】.「選択した催しのグループ確認」部分の各店グループ毎に：
			var targetRows = $('#'+$.id.grd_tengp).datagrid('getRows');
			for (var i=0; i<targetRows.length; i++){
				// 2.3.1.1．コピー対象グループに属する全店舗をチェックし、廃店（店舗基本.店運用区分=9）がある場合はワーニングを表示する。
				var param = that.getInputboxParams(that);
				param["KEY"]		= "REP_CHK_DB";
				param["MOYSKBN"]	= targetRows[i]["MOYSKBN"];
				param["MOYSSTDT"]	= targetRows[i]["MOYSSTDT"];
				param["MOYSRBAN"]	= targetRows[i]["MOYSRBAN"];
				param["TENGPCD"]	= targetRows[i]["TENGPCD"];
				var chk_rows = $.getSelectListData(that.name, $.id.action_check, undefined, [param]);
				// エラーの場合は、エラー情報が返ってくる
				// E20259	コピー対象の以下の店舗が廃店となっています。	コピー処理終了後、TG003店舗グループ店情報画面で以下の店舗を削除してください。	0	 	E
				if(chk_rows[0]["ID"]){
					$.showMessage(chk_rows[0]["ID"], chk_rows[0]["PRM"]);
					return false;
				}
			}

			// 入力情報を変数に格納
			if (rt == true) that.setGridData(that.getGridData());	// 更新用情報取得

			return rt;
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

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,			// レポート名
					action:			$.id.action_update,	// 実行処理情報
					obj:			id,					// 実行オブジェクト
					SENDBTNID:		that.sendBtnid,
					DATA:			JSON.stringify(targetData),				// 更新対象情報
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

			// *** 検索条件情報 ***
			// 催しコード
			this.jsonTemp.push({
				id:		$.id_inp.txt_moyscd,
				value:	$('#'+$.id_inp.txt_moyscd).numberbox('getValue'),
				text:	$('#'+$.id_inp.txt_moyscd).numberbox('getText')
			});
			// 催し名称
			this.jsonTemp.push({
				id:		$.id_inp.txt_moykn,
				value:	$('#'+$.id_inp.txt_moykn).textbox('getValue'),
				text:	$('#'+$.id_inp.txt_moykn).textbox('getText')
			});
			// 催し期間From
			this.jsonTemp.push({
				id:		$.id_inp.txt_hbstdt,
				value:	$.getInputboxValue($('#'+$.id_inp.txt_hbstdt)),
				text:	$('#'+$.id_inp.txt_hbstdt).numberbox('getText')
			});
			// 催し期間To
			this.jsonTemp.push({
				id:		$.id_inp.txt_hbeddt,
				value:	$.getInputboxValue($('#'+$.id_inp.txt_hbeddt)),
				text:	$('#'+$.id_inp.txt_hbeddt).numberbox('getText')
			});
		},
		setGrid: function (id, reportNumber){	// グリッドの構築
			var that = this;
			var init = true;
			// ページサイズ定義取得
			var pageList = $.fn.pagination.defaults.pageList;
			var pageSize = $.getJSONValue(this.jsonHidden, $.id.pageSize);
			if (pageSize==="") pageSize=$.fn.pagination.defaults.pageSize;
			pageSize = $.getDefaultPageSize(pageSize, pageList);

			$('#'+id).datagrid({
				nowrap: true,
				border: true,
				striped: true,
				collapsible:false,
				remoteSort: true,
				rownumbers:false,
				fit:true,
				view:scrollview,
				pageSize:pageSize,
				pageList:pageList,
				frozenColumns:[[]],
				columns:[[
					{field:'F1',	title:'催しコード',	width:100,halign:'center',align:'left'},
					{field:'F2',	title:'催し名称',	width:200,halign:'center',align:'left'},
					{field:'F3',	title:'催し期間',	width:180,halign:'center',align:'left'}
				]],
				fitColumns:false,	// 指定カラム幅を適用する場合、false 指定。
				onBeforeLoad:function(param){
					param.report = that.name;
				},
				onLoadSuccess:function(data){
					// 検索後、初回のみ処理
					if (that.queried){
						that.queried = false;	// 検索後、初回のみ処理
						// 状態保存
						$.saveState(reportNumber, that.getJSONString(), '#'+id);
						// 警告
						$.showWarningMessage(data);
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
		setCopyGrid: function (id, reportno){	// グリッドの構築
			var that = this;
			var init = true;
			// ページサイズ定義取得
			var pageList = $.fn.pagination.defaults.pageList;
			var pageSize = $.getJSONValue(this.jsonHidden, $.id.pageSize);
			if (pageSize==="") pageSize=$.fn.pagination.defaults.pageSize;
			pageSize = $.getDefaultPageSize(pageSize, pageList);


			var dformatter =function(value){ return $.getFormatDt(value, true);};
			var cstyler=function(value,row,index){return 'color: red;font-weight: bold;';};
			var cformatter =function(value,row,index){return value && value===$.id.value_on?$.id.text_on:$.id.text_off;};

			$('#'+id).datagrid({
				url:$.reg.easy,
				frozenColumns:[[]],
				columns:[[
					{field:'F2',	title:'グループNo.',		width: 70,	halign:'center',align:'left'},
					{field:'F3',	title:'グループ名称',		width:270,	halign:'center',align:'left'},
					{field:'F4',	title:'リーダー店No.',		width: 60,	halign:'center',align:'left'},
					{field:'F5',	title:'リーダー店',			width:270,	halign:'center',align:'left'},
					{field:'F6',	title:'店数',				width: 50,	halign:'center',align:'right'},
					{field:'F7',	title:'売価選択（一括）',	width: 60,	halign:'center',align:'center',	formatter:cformatter,	styler:cstyler},
					{field:'F8',	title:'売価選択（商品別）',	width: 70,	halign:'center',align:'center',	formatter:cformatter,	styler:cstyler},
					{field:'F9',	title:'売価差替',			width: 35,	halign:'center',align:'center',	formatter:cformatter,	styler:cstyler},
					{field:'F10',	title:'売価商品選択',		width: 60,	halign:'center',align:'center',	formatter:cformatter,	styler:cstyler},
					{field:'F11',	title:'不参加',				width: 45,	halign:'center',align:'center',	formatter:cformatter,	styler:cstyler}

				]],
				fit:true,
				singleSelect:true,
				checkOnSelect:false,
				selectOnCheck:false,
				onBeforeLoad:function(param){
					index = -1;
					if(init) {
						init = false;
						return false;
					}
					$($.id.hiddenChangedIdx).val("");

					var row = $($.id.gridholder).datagrid("getSelected");
					var json = [{
						MOYSKBN:	row["F4"],		// 催し区分
						MOYSSTDT:	row["F5"],		// 催しコード（催し開始日）
						MOYSRBAN:	row["F6"]		// 催し連番
					}];
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
					if(data && data.rows){
						if(data.rows.length > 0){
							$($.id.hiddenChangedIdx).val("1");
						}
					}
				}
			});

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
			case $.id.btn_cancel:
			case $.id.btn_upd:
			case $.id.btn_del:
				// 転送先情報
				index = that.repgrpInfo.TG002.idx;		// 月間販売計画 店舗グループ一覧
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
			var func_focus = function(){$.addErrState(that, $('#'+obj.attr('id')), true)};

			// DB問い合わせ系
			if($('[for_inp^='+id+'_]').length > 0){
				var param = that.getInputboxParams(that, id, newValue);
				$.getsetInputboxRowData(that.name, 'for_inp', id, [param], that, parentObj);
			}
		},
		getInputboxParams: function(that, id, newValue){
			// 情報取得
			var values = {};
			if(newValue){
				values["value"]		= newValue;
			}
			values["MOYSKBN"]	= $.getJSONObject(that.jsonHidden, $.id_inp.txt_moyskbn).value;		// 催し区分
			values["MOYSSTDT"]	= $.getJSONObject(that.jsonHidden, $.id_inp.txt_moysstdt).value;	// 催しコード（催し開始日）
			values["MOYSRBAN"]	= $.getJSONObject(that.jsonHidden, $.id_inp.txt_moysrban).value;	// 催し連番
			values["SENDBTNID"]	= that.sendBtnid;
			// 情報設定
			return values;
		}
	} });
})(jQuery);