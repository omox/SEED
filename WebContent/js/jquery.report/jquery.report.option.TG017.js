/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_ReportTG017',			// （必須）レポートオプションの確認
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
		pushBtnid: "",						// 実行ボタンID情報(検索系で利用)
		sendBtnid: "",						// 呼出ボタンID情報
		focusRootId:"cc",					// （キー移動時必須）キー移動イベントのルートとなるパネルのID
		focusParentId:"",					// （キー移動時必須）現在フォーカスがあたっている項目の親となるパネルのID
		focusGridId:"",						// （キー移動時必須）現在フォーカスがあたっているDataGridのID
		editRowIndex:{},					// グリッド編集行保持
		initialize: function (reportno){	// （必須）初期化
			var that = this;
			// 引き継ぎ情報
			this.jsonHidden = $.getTargetValue();
			// 画面の初回基本設定
			this.setInitObjectState();
			// 初期検索条件設定
			this.jsonInit = $.getInitValue();
			// 処理日付取得
			$('#'+$.id.txt_shoridt).val($.getInputboxData(that.name, $.id.action_init, $.id.txt_shoridt,[{}]));
			// データ表示エリア初期化
			that.setGrid($.id.gridholder.replace('#', ''), reportno);

			// 初期化するオブジェクト数設定
			this.initObjNum = this.dedefaultObjNum;
			// 入力テキストボックス系
			$.setInputbox(that, reportno, $.id_inp.txt_stym, false);
			$.setMeisyoCombo(that, reportno, $.id.sel_shuno, false);
			$.setMeisyoCombo(that, reportno, $.id_mei.kbn10002, false);
			$.setInputbox(that, reportno, $.id_inp.txt_bmncd, false);

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
			TG017:{idx:1},		// 特売・スポット計画 新規・変更
			TG017_1:{idx:2},	// 特売・スポット計画 参照
			ST022:{idx:3},		// 特売・スポット計画 CSV取込
			ST024:{idx:4},		// 特売・スポット計画 店一括数量CSV取込
			ST016:{idx:5},		// 特売・スポット計画 商品一覧
			ST024:{idx:6},		// 特売・スポット計画 CSV取込
			ST019:{idx:7},		// 特売・スポット計画 コピー元商品選択
			TG020:{idx:9},		// 特売・スポット計画 店別数量訂正
		},
		setInitObjectState: function(){	// 画面初期化時の項目制御
			var that = this;
			// 引き継ぎ情報セット
			var sendBtnid = $.getJSONValue(that.jsonHidden, "sendBtnid");
			if(sendBtnid && sendBtnid.length > 0){
				that.sendBtnid = sendBtnid;
				$.reg.search = true;
				that.onChangeReport = true;
			}
			var reportYobi1 = $.getJSONValue(that.jsonHidden, "reportYobi1");
			if(!reportYobi1){
				reportYobi1 = $('#reportYobi1').val();
			}
			$('#reportYobi1').val(reportYobi1);

			// 当帳票を「参照」で開いた場合
			if(that.reportYobiInfo()==='1'){
				$.setInputBoxDisable($('#'+$.id.btn_csv+1)).css('visibility', 'hidden');
				$.setInputBoxDisable($('#'+$.id.btn_csv+2)).css('visibility', 'hidden');
				$.setInputBoxDisable($('#'+$.id.btn_checklist+6)).hide();
				$.initReportInfo("TG017", "特売・スポット計画　催し　一覧（参照）");
			}else{
				$('#'+$.id.btn_csv+1).on("click", that.pushCsv);
				$('#'+$.id.btn_csv+2).on("click", that.pushCsv);
				$.initReportInfo("TG017", "特売・スポット計画　催し　一覧");
			}
			$($.id.buttons).show();
			$('#'+$.id.btn_sel_change).on("click", $.pushChangeReport);
			$('#'+$.id.btn_sel_change+'2').on("click", $.pushChangeReport);
			$('#'+$.id.btn_checklist+1).linkbutton('disable');
			$('#'+$.id.btn_checklist+2).linkbutton('disable');
			$('#'+$.id.btn_checklist+3).linkbutton('disable');
			$('#'+$.id.btn_checklist+4).linkbutton('disable');
			$('#'+$.id.btn_checklist+5).linkbutton('disable');
			$('#'+$.id.btn_checklist+6).linkbutton('disable');
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
		validation: function (btnId){	// （必須）批准
			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform).form('validate');

			var szMoyskbn	= $.getJSONObject(this.jsonTemp, $.id_mei.kbn10002).value;	// 催し区分
			var szShuno		= $.getInputboxValue($('#'+$.id.sel_shuno));					// 週No.
			var szStym		= $.getInputboxValue($('#'+$.id_inp.txt_stym));				// 表示年月

			if(rt){
				if (($.isEmptyVal(szShuno) || szShuno==='-1') && $.isEmptyVal(szStym)) {
					$.showMessage('E30032', ["週№もしくは表示年月は"], function(){$.addErrState(that, $('#'+$.id.sel_shuno), true)});
					rt = false;
				}
			}

			if (rt) {
				if(szMoyskbn === $.id.valueSel_Head){
					$.showMessage('E00007', ["催し区分"], function(){$.addErrState(that, $('#'+$.id_mei.kbn10002), true)});
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
			var szShuno		= $.getJSONObject(this.jsonString, $.id.sel_shuno).value;		// 週No.
			var szStym		= $.getJSONObject(this.jsonString, $.id_inp.txt_stym).value;	// 表示年月
			var szMoyskbn	= $.getJSONObject(this.jsonString, $.id_mei.kbn10002).value;	// 催し区分
			var dtMoyskbn	= $.getJSONObject(this.jsonString, $.id_mei.kbn10002+'DATA').value;	// 催し区分のDATA
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
					SHUNO:			szShuno,		// 週No
					STYM:			szStym,			// 表示年月
					MOYSKBN:		szMoyskbn,		// 催し区分
					MOYSKBN_DATA:	JSON.stringify(dtMoyskbn),		// 催し区分のDATA
					t:				(new Date()).getTime(),
					sortable:		sortable,
					sortName:		that.sortName,
					sortOrder:		that.sortOrder,
					rows:			0	// 表示可能レコード数
				},
				function(json){
					// 検索処理エラー判定
					if($.searchError(json)) return false;

					// ログ出力
					$.log(that.timeData, 'query:');

					// Load処理回避
					$.tryChangeURL(null);

					var size = JSON.parse(json)["total"];
					if(size == 0){
						$.showMessage('E11003');
					}

					if (sortable===0){
						var options = $($.id.gridholder).datagrid('options');
						// 初期検索時に並び替え情報のリセット
						options.sortName = null;
						options.sortOrder = null;
					}

					that.queried = true;
					that.pushBtnid = btnId;

					$($.id.hiddenChangedIdx).val("");						// 変更行Index

					// Load処理回避
					$.tryChangeURL($.reg.jqeasy);

					// グリッド再描画（easyui 1.4.2 対応）
					$($.id.gridholder).datagrid('load', {} );
					$.removeMaskMsg();
					$.removeMask();

					// ログ出力
					$.log(that.timeData, 'loaded:');
				}
			);
		},
		getGridData: function (row, target){
			var that = this;

			var data = {};

			// 基本情報
			if(target===undefined || target==="grd_data"){
				var targetData =[];
				var rows = [row];
				if(!row){
					rows = $($.id.gridholder).datagrid('getRows');
				}
				for (var i = 0; i < rows.length; i++){
					var rowData = {
						F1 : ""+rows[i]["F10"],				//F1	MOYSKBN	催し区分
						F2 : ""+rows[i]["F11"],				//F2	MOYSSTDT	催し開始日
						F3 : ""+rows[i]["F12"]				//F3	MOYSRBAN	催し連番
					};
					targetData.push(rowData);
				}
				data["grd_data"] = targetData;
			}

			return data;
		},
		/**
		 * CSV出力ボタンイベント
		 * @param {Object} e
		 */
		pushCsv : function(e){

			// TODO：仮
			alert("現在CSV出力機能は停止中です。");
			return false;

			if ($(this).linkbutton('options').disabled)	return false;

			// レポート番号取得
			var reportno=$($.id.hidden_reportno).val();
			// JS情報取得
			var id = $(this).attr('id');

			// レポート定義位置
			var reportNumber = $.getReportNumber(reportno);
			var that = $.report[reportNumber];
			if (typeof(reportNumber) !== 'number') { alert("レポート定義が見つかりません。"); return false;}

			// マスク削除
			$.removeMask();

//			　　3.4.1．出力手順：
//			① 催し区分＝0の場合、POP原稿CSV出力、B/MCSV出力共にエラーとする。
//			② 画面に選択した1行の催し情報を出力する。何も選択しないと、エラー。
//			③ CSV出力開始メッセージを表示する。
//			④ CSV出力を開始する。
			if ($.report[reportNumber].csvValidation(id)) {

				var func_ok = function(){
					// マスク追加
					$.appendMask();

					// セッションタイムアウト、利用時間外の確認
					var isTimeout = $.checkIsTimeout();
					if (! isTimeout) {
						// ログ保持
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
								that.checkListSuccess(id,reportno);
							}
						});
					}
				};

				// ① 【画面】.「POP原稿CSV出力」ボタンを押下時に、「昨日までの情報でデータを作成します。よろしいですか？」の「はい、いいえ」ダイアログを表示する。「はい」を選択した場合、処理を続ける。「いいえ」を選択した場合、処理を中止する。
				var msgid = id===$.id.btn_csv+1 ? "W20015" : "W20006";
				// W20015	CSVデータを出力します。よろしいですか？	 	4	 	Q
				// W20006	昨日までの情報でデータを作成します。	よろしいですか？	4	 	Q
				$.showMessage(msgid, undefined, func_ok);
			} else {
				return false;
			}
		},
		csvValidation: function (btnId){	// （必須）批准
			var that = this;

			var rt = true;

			// ① 催し区分＝0の場合、POP原稿CSV出力、B/MCSV出力共にエラーとする。
			// ※催し区分検索条件は必須条件なので検索条件でOK
			var moykbn = $('#'+$.id_mei.kbn10002).combobox('getValue');
			if(moykbn === $.id.value_moykbn_r){
				//E20486	催し区分＝0の場合、POP原稿CSVを出力できません	 	0	 	E
				//E20487	催し区分＝0の場合、B/MCSVを出力できません	 	0	 	E
				var msgid = btnId===$.id.btn_csv+1 ? "E20487" : "E20486";
				$.showMessage(msgid, undefined, function(){$.addErrState(that, $('#'+$.id_mei.kbn10002), true)});
				return false;
			}

			// 選択行
			var row = $($.id.gridholder).datagrid("getSelected");

			// ② 画面に選択した1行の催し情報を出力する。何も選択しないと、エラー。
			if(!row){
				$.showMessage('E00008');
				return false;
			}

			return rt;
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

			// 週No.
			 this.jsonTemp.push({
				id:		$.id.sel_shuno,
				value:	$('#'+$.id.sel_shuno).combobox('getValue'),
				text:	$('#'+$.id.sel_shuno).combobox('getText')
			});
			// 表示年月From
			this.jsonTemp.push({
				id:		$.id_inp.txt_stym,
				value:	$('#'+$.id_inp.txt_stym).numberbox('getValue'),
				text:	$('#'+$.id_inp.txt_stym).numberbox('getText')
			});
			// 催し区分
			 this.jsonTemp.push({
				id:		$.id_mei.kbn10002,
				value:	$('#'+$.id_mei.kbn10002).combobox('getValue'),
				text:	$('#'+$.id_mei.kbn10002).combobox('getText')
			});

			// 催し区分(DATA)
			var dtMoykbn = $('#'+$.id_mei.kbn10002).combobox('getData');
			var dataMoykbn = [];
			for(var i=0;i<dtMoykbn.length;i++){
				if(dtMoykbn[i].VALUE!=='-1'){
					dataMoykbn.push(dtMoykbn[i].VALUE);
				}
			}
			this.jsonTemp.push({
				id:		$.id_mei.kbn10002+'DATA',
				value:	dataMoykbn,
				text:	'全催し区分情報'
			});
		},
		setObjectState: function(){	// 軸の選択内容による制御

		},
		setGrid: function (id, reportNumber){	// グリッドの構築
			var that = this;
			var init = true;
			// ページサイズ定義取得
			var pageList = $.fn.pagination.defaults.pageList;
			var pageSize = $.getJSONValue(this.jsonHidden, $.id.pageSize);
			if (pageSize==="") pageSize=$.fn.pagination.defaults.pageSize;
			pageSize = $.getDefaultPageSize(pageSize, pageList);

			var columns = [];
			var columnBottom=[];

			var iformatter =function(value){ return $.getFormat(value, '#,##0');};
			var dformatter =function(value){ return $.getFormatDt(value, true);};
			var cstyler =function(value,row,index){return 'color: red;font-weight: bold;';};
			var cformatter =function(value,row,index){return value && value===$.id.value_on?$.id.text_on:$.id.text_off;};

			columnBottom.push({field:'F1',	title:'催しコード',				width:95,	halign:'center',align:'left'});
			columnBottom.push({field:'F2',	title:'催し名称',				width:280,	halign:'center',align:'left'});
			columnBottom.push({field:'F3',	title:'1日遅<br>ﾊﾟﾀﾝ有',		width:50,	halign:'center',align:'left'});
			columnBottom.push({field:'F4',	title:'催し期間',				width:180,	halign:'center',align:'left'});
			columnBottom.push({field:'F5',	title:'月締め',					width:72,	halign:'center',align:'left',	formatter:dformatter});
			columnBottom.push({field:'F6',	title:'月締',					width:30,	halign:'center',align:'center',	formatter:cformatter,	styler:cstyler});
			columnBottom.push({field:'F7',	title:'最終締',					width:72,	halign:'center',align:'left',	formatter:dformatter});
			columnBottom.push({field:'F8',	title:'入力数',					width:50,	halign:'center',align:'right',	formatter:iformatter});
			columnBottom.push({field:'F9',	title:'変申請',					width:45,	halign:'center',align:'right',	formatter:iformatter});
			columns.push(columnBottom);


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
				frozenColumns:[[]],
				columns:columns,
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

					// 前回選択情報をGridに反映
					var getRowIndex = $.getJSONValue(that.jsonHidden, "scrollToIndex_"+'#'+id);
					var rows = $($.id.gridholder).datagrid('getRows');
					getRowIndex = $.isEmptyVal(getRowIndex) && rows.length !== 0 ? 0 : getRowIndex;

					if(getRowIndex !== ""){
						$('#'+id).datagrid('scrollTo', {
							index: getRowIndex,
							callback: function(index){
								$('#'+id).datagrid('selectRow', index);
							}
						});

						// 検索可能な一覧画面では、検索時に前回選択情報が適用されてしまう為、
						// 一度適用したら要素を削除する。
						var targetName = "scrollToIndex_"+'#'+id;
						that.jsonHidden.some(function(v, i){
						    if (v.id==targetName) that.jsonHidden.splice(i,1);
						});
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
		},
		getRecord: function(){		// （必須）レコード件数を戻す
			var data = $($.id.gridholder).datagrid('getData');
			if (data == null) {
				return 0;
			} else {
				return data.total;
			}
		},
		pushCheckList:function(e){

			// TODO：仮
			alert("現在チェックリスト出力機能は停止中です。");
			return false;


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

			if($.isFunction(that.checkListValidation)) { rtn = that.checkListValidation(id);}
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
								that.checkListSuccess(id,reportno);
							}
						});
					}
					return true;
				};
				var row = $($.id.gridholder).datagrid("getSelected");
				var message = '催しコード　' + row.F1;
				$.showMessage("W20031", [message, "",""], func_ok);
			}
		},

		checkListValidation: function (){	// （必須）批准
			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = true;



			var that = this;

			var rt = true;

			// ① 部門：数値2桁。チェックリスト系のボタンを押す場合のみ必須
			var bmncd = $.getInputboxValue($('#'+$.id_inp.txt_bmncd));
			if($.isEmptyVal(bmncd, true)){
				// E20125	部門コードを入力してください。	 	0	 	E
				$.showMessage("E20125", undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_bmncd), true)});
				return false;
			}

			// 選択行
			var row = $($.id.gridholder).datagrid("getSelected");

			// ② 画面に選択した1行の催し情報を出力する。何も選択しないと、エラー。
			if(!row){
				$.showMessage('E00008');
				return false;
			}

			return rt;
		},
		/**  FTPファイル情報() */
		FtpFileInfo : {
			CSV1_TG	:[1,"btn_csv1"		,"B/M CSV出力"			, "MDCR018", 1281],
			CSV1_SP	:[2,"btn_csv1"		,"B/M CSV出力"			, "MDCR018", 658],
			CSV2	:[3,"btn_csv2"		,"POP原稿 CSV出力"		, "MDCR017", 46],
			CHK1	:[4,"btn_checklist1","管理NO順"				, "MDCR002", 1062],
			CHK2	:[5,"btn_checklist2","商品コード順"			, "MDCR004", 1062],
			CHK3	:[6,"btn_checklist3","販促用チェックリスト"	, "MDCR006", 187],
			CHK4	:[7,"btn_checklist4","納入期間順"			, "MDCR003", 1062],
			CHK5	:[8,"btn_checklist5","分類順"				, "MDCR005", 1062],
			CHK6	:[9,"btn_checklist6","週間特売原稿"			, "MDCR001", 1062]
		},
		checkListSuccess: function(id,reportno){

			var that = this;

			// 選択行
			var row = $($.id.gridholder).datagrid("getSelected");
			var targetData = that.getGridData($($.id.gridholder).datagrid("getSelected"))["grd_data"];
			var szBmncd		= $.getInputboxValue($('#'+$.id_inp.txt_bmncd));	// 部門コード

			var fileName ="";
			var datalen  = 0;
			var ffis = Object.getOwnPropertyNames(that.FtpFileInfo);
			for ( var i in ffis ) {
				var ffi = that.FtpFileInfo[ffis[i]];
				if(ffi[1]===id){
					fileName = ffi[3];
					datalen  = ffi[4];
					break;
				}
			}

			var title = 'ファイル名：';
			var br = '<br>'

			var json = [{
				"callpage":that.name,
				"FILE":fileName,
				"DREQKIND":1,			// ≒SQL実行回数
				"REQLEN":datalen,
				"MOYSKBN":row.F10,
				"MOYSSTDT":row.F11,
				"MOYSRBAN":row.F12,
				"BMNCD":szBmncd,
				"BTN":id,
				"DATA": JSON.stringify(targetData)	// 更新対象情報
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
			$.setJSONObject(sendJSON, 'sendBtnid', btnId, $('#'+btnId).attr("title"));					// 実行ボタン情報保持
			$.setJSONObject(sendJSON, 'reportYobi1', that.reportYobiInfo(), that.reportYobiInfo());		// 参照情報保持
			$.setJSONObject(sendJSON, 'callpage', that.name, that.name);								// 呼出し元レポート情報

			// 戻る実行時用に現在の画面情報を保持する
			var states = $.getBackBaseJSON(that);
			var newrepinfos = $.getBackJSON(that, states, true);
			$.setJSONObject(sendJSON, 'repinfo', newrepinfos, '');

			// 選択行
			var row = $($.id.gridholder).datagrid("getSelected");

			// 実行ボタン別処理
			switch (btnId) {
			case $.id.btn_sel_change:	// 選択(商品一覧)
				if(!row){
					$.showMessage('E00008');
					return false;
				}

				// 転送先情報
				index = that.repgrpInfo.ST016.idx;
				childurl = href[index];
				sendMode = 1;
				// オブジェクト作成
				$.setJSONObject(sendJSON, $.id_inp.txt_moyskbn,  row.F10, row.F10);
				$.setJSONObject(sendJSON, $.id_inp.txt_moysstdt, row.F11, row.F11);
				$.setJSONObject(sendJSON, $.id_inp.txt_moysrban, row.F12, row.F12);
				break;
			case $.id.btn_sel_change + '2':	// 選択(店別数量訂正)
				if(!row){
					$.showMessage('E00008');
					return false;
				}

				// 転送先情報
				index = that.repgrpInfo.TG020.idx;
				childurl = href[index];
				sendMode = 1;
				// オブジェクト作成
				$.setJSONObject(sendJSON, $.id_inp.txt_moyskbn,  row.F10, row.F10);
				$.setJSONObject(sendJSON, $.id_inp.txt_moysstdt, row.F11, row.F11);
				$.setJSONObject(sendJSON, $.id_inp.txt_moysrban, row.F12, row.F12);
				break;
			case $.id.btn_back:
				// 転送先情報
				childurl = parent.$('#hdn_menu_path').val();

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
		srccsv: function(reportno, btnId){	// ここではCsv出力
			var that = this;
			// 処理時間計測用
			that.timeData = (new Date()).getTime();

			var that = this;
			// 検索実行


			// 基本情報
			var targetData = that.getGridData($($.id.gridholder).datagrid("getSelected"))["grd_data"];

			if(!btnId) btnId = $.id.btn_search;

			var kbn = 0;
			var data = {
				report:			that.name,		// レポート名
				'kbn':			 kbn,
				'type':			'fix',			// 固定長
				BTN:			btnId,
				DATA:			JSON.stringify(targetData),				// 更新対象情報
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
				// ログ出力
				$.log(that.timeData, 'srcexcel:');
			});
		}
	} });
})(jQuery);