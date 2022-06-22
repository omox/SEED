/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_ReportTM005',			// （必須）レポートオプションの確認
		jsonTemp:	[],							// （必須）検索条件情報_入力チェック前
		jsonString:	[],							// （必須）検索条件情報
		jsonHidden: [],							// （必須）親画面からの引き継ぎ情報
		jsonInit: [],							// （必須）検索条件初期情報
		caption: function(){					// （必須）タイトル
			return $('#reportname').val();
		},
		sortName: '',							// ソート項目名
		sortOrder: '',							// ソート順
		timeData : (new Date()).getTime(),
		dedefaultObjNum:	20,					// 初期化オブジェクト数
		initObjNum:	-1,
		initedObject: [],
		maxMergeCell: 1,
		onChangeFlag : false,
		onChangeFlag2 : false,
		columnName:'',	// OnClickRowの列名
		queried : false,
		initializes : true,
		onChangeReport: false,
		reportYobiInfo: function(){				// （必須）レポートメニューに登録された予備情報＝帳票をどういった表示にしたいかの情報
			return $('#reportYobi1').val();
		},
		sendBtnid: "",						// （必須）呼出ボタンID情報
		pushBtnid: "",						// 実行ボタンID情報(検索系で利用)
		focusRootId:"cc",						// （キー移動時必須）キー移動イベントのルートとなるパネルのID
		focusParentId:"",						// （キー移動時必須）現在フォーカスがあたっている項目の親となるパネルのID
		focusGridId:"",							// （キー移動時必須）現在フォーカスがあたっているDataGridのID
		editRowIndex:{},					// グリッド編集行保持
		grd_saiyou_data:[],					// グリッド情報:採用情報
		initialize: function (reportno){		// （必須）初期化
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

			// 編集可能データグリッドの共通処理設定
			// 編集エディターの機能拡張（非表示で参照用のオブジェクトが準備してある前提、参照オブジェクトはグリッドのフィールド名を元に判断）
			$.extendDatagridEditor();

			// 初期検索可能
			that.onChangeReport = true;

			that.queried = true;


			// 入力テキストボックス系
			var inputbox = Object.getOwnPropertyNames($.id_inp);
			var isSearchId = [$.id_inp.txt_stym, $.id_inp.txt_enym];
			for ( var sel in inputbox ) {
				if($('#'+$.id_inp[inputbox[sel]]).length > 0){
					var isUpdate = isSearchId.indexOf(inputbox[sel]) === -1;
					$.setInputbox(that, reportno, $.id_inp[inputbox[sel]], isUpdate);
				}
			}

			that.setGrid($.id.gridholder.replace('#', ''), reportno);

			// 初期表示時に検索処理を通らない為フラグをtrueに
			that.queried = true;
			$.setCheckboxInit2(that.jsonHidden, "chk_del", that);
			$.setCheckboxInit2(that.jsonHidden, "chk_seisen", that);
			$.setCheckboxInit2(that.jsonHidden, "chk_nninfo", that);
			$.setCheckboxInit2(that.jsonHidden, "chk_hbinfo", that);
			$.setCheckboxInit2(that.jsonHidden, "chk_ksdaibrui", that);
			$.setCheckboxInit2(that.jsonHidden, "chk_kschubrui", that);
			$.setCheckboxInit2(that.jsonHidden, "chk_dsuexrtptn", that);
			$.setCheckboxInit2(that.jsonHidden, "chk_dsuexsuptn", that);
			$.setCheckboxInit2(that.jsonHidden, "chk_dsuexjrtptn", that);
			$.setCheckboxInit2(that.jsonHidden, "chk_drtexuri", that);
			$.setCheckboxInit2(that.jsonHidden, "chk_drtexten", that);
			$.setCheckboxInit2(that.jsonHidden, "chk_dznendsdai", that);
			$.setCheckboxInit2(that.jsonHidden, "chk_dznendschu", that);
			$.setCheckboxInit2(that.jsonHidden, "chk_ddnendsdai", that);
			$.setCheckboxInit2(that.jsonHidden, "chk_ddnendschu", that);
			$.setCheckboxInit2(that.jsonHidden, "chk_dcutex", that);
			$.setCheckboxInit2(that.jsonHidden, "chk_dchiras", that);
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
			if(sendBtnid && sendBtnid.length > 0){
				that.onChangeReport = true;
				$.removeMaskMsg();
			}
			$.setInputBoxDisable($("#"+$.id_inp.txt_bmnkn));

			$($.id.buttons).show();
			// 各種遷移ボタン
			$.initReportInfo("TM005", "催し　部門情報一覧" ,'一覧');
			$('#'+$.id.btn_cancel).on("click", $.pushChangeReport);
			$('#'+$.id.btn_back).on("click", $.pushChangeReport);

//			$($.id.hiddenChangedIdx).val("");						// 変更行Index

		},
		initCondition: function (){	// 条件初期値セット
			var that = this;
			// 初期化項目
		},
		clear:function(){
			// 隠し情報初期化
			$($.id.hiddenChangedIdx).val("");						// 変更行Index
//			// グリッド初期化
//			this.success(this.name, false);
		},
		validation: function (){	// （必須）批准
//			var that = this;
//			// EasyUI のフォームメソッド 'validate' 実施
//			var rt = $($.id.toolbarform).form('validate');
//			// 入力エラーなしの場合に検索条件を格納
//			if (rt == true) that.jsonString = that.jsonTemp.slice(0);
//			// 入力チェック用の配列をクリア
//			that.jsonTemp = [];
			return true;
		},
		success: function(reportno, sortable){	// （必須）正処理
			if (sortable) sortable=1; else sortable=0;
			var that = this;

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
					report:			that.name,		// レポート名
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

					that.queried = true;
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
		updValidation: function (id){	// （必須）批准

			// 入力編集を終了する。
			var row = $($.id.gridholder).datagrid("getSelected");
			var rowIndex = $($.id.gridholder).datagrid("getRowIndex", row);
			$($.id.gridholder).datagrid('endEdit',rowIndex);

			var that = this;
			var errMsg= "";
			var rt = true;

			// 入力チェック
			var targetRowsMoydef = that.getMergeGridDate($.id.gridholder);
			var alldata = that.getGridData2($.id.gridholder);
			var targetdate = [];
			for(var i = 0; i < alldata.length; i++){
				if(alldata[i]["F2"] && alldata[i]["F2"] !=="" ){
					targetdate.push(Number(alldata[i]["F2"]));
				}
			}
			var targetdateF = targetdate.filter(function (element, index, self) { return self.indexOf(element)*1 === index; });
			if(targetdate.length !== targetdateF.length){
				$.showMessage('E11040',['部門コード']);
				return false;
			}

			var chirasCount = 0;
			for (var j = 0; j < targetRowsMoydef.length; j++) {
				var msgid = that.checkInputboxFunc($.id_inp.txt_bmncd, targetRowsMoydef[j]["F2"] , '');
				if(msgid !==null){
					$.showMessage(msgid, undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_bmncd), true)});
					return false;
				}
				if (targetRowsMoydef[j]["F2"] === "") {														// 部門
					$.showMessage('EX1047', ["部門"]);
					return false;
				}
				var msgid = that.checkInputboxFunc($.id_inp.txt_szkcd, targetRowsMoydef[j]["F4"] , '');
				if(msgid !==null){
					$.showMessage(msgid, undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_szkcd), true)});
					return false;
				}
				if (targetRowsMoydef[j]["F4"] === "") {														// 所属コード
					$.showMessage('EX1047', ["所属コード"]);
					return false;
				}
				if ((targetRowsMoydef[j]["F8"] === "0" && targetRowsMoydef[j]["F9"] === "0")
						|| (targetRowsMoydef[j]["F8"] === "1" && targetRowsMoydef[j]["F9"] === "1")) {			// 検証の括り
					$.showMessage('EX1090', ["大分類、中分類"]);
					return false;
				}
				if ((targetRowsMoydef[j]["F10"] === "0" && targetRowsMoydef[j]["F11"] === "0" && targetRowsMoydef[j]["F12"] === "0")
						|| (targetRowsMoydef[j]["F10"] === "1" && targetRowsMoydef[j]["F11"] === "1")
						|| (targetRowsMoydef[j]["F10"] === "1" && targetRowsMoydef[j]["F12"] === "1")
						|| (targetRowsMoydef[j]["F11"] === "1" && targetRowsMoydef[j]["F12"] === "1")) {	// デフォルト_数展開
					$.showMessage('EX1090', ["通常率パターン、数量パターン、実績率パターン"]);
					return false;
				}
				if ((targetRowsMoydef[j]["F13"] === "0" && targetRowsMoydef[j]["F14"] === "0")
						|| (targetRowsMoydef[j]["F13"] === "1" && targetRowsMoydef[j]["F14"] === "1")) {	// デフォルト_実績率パタン数値
					$.showMessage('EX1090', ["売上、点数"]);
					return false;
				}
				var count = 0;
				if(targetRowsMoydef[j]["F15"] === "1"){
					count ++;
				}
				if(targetRowsMoydef[j]["F16"] === "1"){
					count ++;
				}
				if(targetRowsMoydef[j]["F17"] === "1"){
					count ++;
				}
				if(targetRowsMoydef[j]["F18"] === "1"){
					count ++;
				}
				if(count != "1"){
					$.showMessage('EX1090', ["大（前Ｎ同週）、中（前Ｎ同週）、大（前Ｎ同月）、中（前Ｎ同月）"]);
					return false;
				}

				if (targetRowsMoydef[j]["F20"] === "1") {													// チラシのみ
					chirasCount++;
				}
				if (chirasCount > 20) {
					$.showMessage('E20039');
					return false;
				}
			}

			return rt;
		},
		updSuccess: function(id){	// validation OK時 の update処理
			var that = this;

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$($.id.gridholder).datagrid('loading');

			var targetData = that.getMergeGridDate($.id.gridholder);

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
//						 $($.id.gridholder).datagrid('getPanel').find("[datagrid-row-index='"+index+"']").css('color', 'blue');
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
			// 部門
			this.jsonTemp.push({
				id:		$.id_inp.txt_bmncd,
				value:	$('#'+$.id_inp.txt_bmncd).textbox('getValue'),
				text:	$('#'+$.id_inp.txt_bmncd).textbox('getText')
			});
			// 部門名
			this.jsonTemp.push({
				id:		$.id_inp.txt_bmnkn,
				value:	$('#'+$.id_inp.txt_bmnkn).textbox('getValue'),
				text:	$('#'+$.id_inp.txt_bmnkn).textbox('getText')
			});
			// 所属コード
			this.jsonTemp.push({
				id:		$.id_inp.txt_szkcd,
				value:	$('#'+$.id_inp.txt_szkcd).numberbox('getValue'),
				text:	$('#'+$.id_inp.txt_szkcd).numberbox('getText')
			});

		},
		setObjectState: function(){	// 軸の選択内容による制御

		},
		extenxDatagridEditorIds:{

			F21	: "chk_del"				// チェックボックス（削除)
			,F2		: "txt_bmncd"			// テキスト（部門)
			,F3		: "txt_bmnkn"			// テキスト（部門名)
			,F4		: "txt_szkcd"			// テキスト（所属コード)
			,F5		: "chk_seisen"			// チェックボックス（生鮮)
			,F6		: "chk_nninfo"			// チェックボックス（納入情報)
			,F7		: "chk_hbinfo"			// チェックボックス（販売情報)
			,F8		: "chk_ksdaibrui"		// チェックボックス（大分)
			,F9		: "chk_kschubrui"		// チェックボックス（中分)
			,F10	: "chk_dsuexrtptn"		// チェックボックス（率P)
			,F11	: "chk_dsuexsuptn"		// チェックボックス（数P)
			,F12	: "chk_dsuexjrtptn"		// チェックボックス（実P)
			,F13	: "chk_drtexuri"		// チェックボックス（売上)
			,F14	: "chk_drtexten"		// チェックボックス（点数)
			,F15	: "chk_dznendsdai"		// チェックボックス（大(前N同週))
			,F16	: "chk_dznendschu"		// チェックボックス（中(前N同週))
			,F17	: "chk_ddnendsdai"		// チェックボックス（大(同N同月))
			,F18	: "chk_ddnendschu"		// チェックボックス（中(同N同月))
			,F19	: "chk_dcutex"			// チェックボックス（しない)
			,F20	: "chk_dchiras"			// チェックボックス（ちらしのみ)
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
			var columnBottomt1=[];
			var columnBottomt2=[];

			var bcstyler =function(value,row,index){return 'background-color:#f5f5f5;';};
			var cstyler=function(value,row,index){return 'color: red;font-weight: bold;';};
			var cformatter =function(value,row,index){return value && value===$.id.value_on?$.id.text_on:$.id.text_off;};

			columnBottomt1.push({title:'　', colspan:10,rowspan:1});
			columnBottomt1.push({title:'特売スポット計画　商品情報　画面　デフォルト', colspan:10,rowspan:1});
			columnBottomt1.push({title:'　', colspan:1,rowspan:1});
			columns.push(columnBottomt1);

			columnBottomt2.push({title:'　', colspan:5});
			columnBottomt2.push({title:'部門区分', colspan:1});
			columnBottomt2.push({title:'1日遅スライドしない', colspan:2});
			columnBottomt2.push({title:'検証の括り', colspan:2});
			columnBottomt2.push({title:'数展開', colspan:3});
			columnBottomt2.push({title:'率pt数値', colspan:2});
			columnBottomt2.push({title:'前n同週', colspan:2});
			columnBottomt2.push({title:'前n同月', colspan:2});
			columnBottomt2.push({title:'カット展', colspan:1});
			columnBottomt2.push({title:'チラシのみ', colspan:1});
			columns.push(columnBottomt2);

			columnBottom.push({field:'F1',	title:'',	hidden:true});
			columnBottom.push({field:'F21',	title:'削除',			width:40,	halign:'center',align:'center',	formatter:cformatter,	editor:{type:'checkbox'},	styler:cstyler});
			columnBottom.push({field:'F2',	title:'部門',			width:40,	halign:'center',align:'left',							editor:{type:'numberbox'},formatter:function(value,row,index){ return $.getFormatLPad(value, $.len.bmncd);}});
			columnBottom.push({field:'F3',	title:'部門名',			width:120,	halign:'center',align:'left',							editor:{type:'textbox'},	styler:bcstyler});
			columnBottom.push({field:'F4',	title:'所属コード',		width:80,	halign:'center',align:'left',							editor:{type:'numberbox'}});
			columnBottom.push({field:'F5',	title:'生鮮',			width:40,	halign:'center',align:'center',	formatter:cformatter,	editor:{type:'checkbox'},	styler:cstyler});
			columnBottom.push({field:'F6',	title:'納入情報',		width:40,	halign:'center',align:'center',	formatter:cformatter,	editor:{type:'checkbox'},	styler:cstyler});
			columnBottom.push({field:'F7',	title:'販売情報',		width:40,	halign:'center',align:'center',	formatter:cformatter,	editor:{type:'checkbox'},	styler:cstyler});
			columnBottom.push({field:'F8',	title:'大分',			width:40,	halign:'center',align:'center',	formatter:cformatter,	editor:{type:'checkbox'},	styler:cstyler});
			columnBottom.push({field:'F9',	title:'中分',			width:40,	halign:'center',align:'center',	formatter:cformatter,	editor:{type:'checkbox'},	styler:cstyler});
			columnBottom.push({field:'F10',	title:'率P',			width:40,	halign:'center',align:'center',	formatter:cformatter,	editor:{type:'checkbox'},	styler:cstyler});
			columnBottom.push({field:'F11',	title:'数P',			width:40,	halign:'center',align:'center',	formatter:cformatter,	editor:{type:'checkbox'},	styler:cstyler});
			columnBottom.push({field:'F12',	title:'実P',			width:40,	halign:'center',align:'center',	formatter:cformatter,	editor:{type:'checkbox'},	styler:cstyler});
			columnBottom.push({field:'F13',	title:'売上',			width:40,	halign:'center',align:'center',	formatter:cformatter,	editor:{type:'checkbox'},	styler:cstyler});
			columnBottom.push({field:'F14',	title:'点数',			width:40,	halign:'center',align:'center',	formatter:cformatter,	editor:{type:'checkbox'},	styler:cstyler});
			columnBottom.push({field:'F15',	title:'大',				width:40,	halign:'center',align:'center',	formatter:cformatter,	editor:{type:'checkbox'},	styler:cstyler});
			columnBottom.push({field:'F16',	title:'中',				width:40,	halign:'center',align:'center',	formatter:cformatter,	editor:{type:'checkbox'},	styler:cstyler});
			columnBottom.push({field:'F17',	title:'大',				width:40,	halign:'center',align:'center',	formatter:cformatter,	editor:{type:'checkbox'},	styler:cstyler});
			columnBottom.push({field:'F18',	title:'中',				width:40,	halign:'center',align:'center',	formatter:cformatter,	editor:{type:'checkbox'},	styler:cstyler});
			columnBottom.push({field:'F19',	title:'しない',			width:40,	halign:'center',align:'center',	formatter:cformatter,	editor:{type:'checkbox'},	styler:cstyler});
			columnBottom.push({field:'F20',	title:'',				width:60,	halign:'center',align:'center',	formatter:cformatter,	editor:{type:'checkbox'},	styler:cstyler});
			columns.push(columnBottom);

			var funcEnter = function(e){
				if ($.endEditingDatagrid(that)){
					$.pushUpd(e);
				}
			};
			var funcClickRow = $.fn.datagrid.defaults.onClickRow;
			var funcBeginEdit= $.fn.datagrid.defaults.onBeginEdit;
			var funcEndEdit= $.fn.datagrid.defaults.onEndEdit;
			var funcAfterEdit= $.fn.datagrid.defaults.onAfterEdit;
//			if(that.reportYobiInfo()!=='1'){
				that.editRowIndex[id] = -1;
				funcClickRow = function(index,field){$.clickEditableDatagridCell(that,id, index)};
				funcBeginEdit = function(index,row){
//					if(row["UPDDT"] === row["UPDDT"]){
//						$.setInputBoxDisable($('#'+$.id.chk_sel+"_"));
//					}
					$.beginEditDatagridRow(that,id, index, row)
				};
				funcEndEdit = function(index,row,changes){
					$.endEditDatagridRow(that, id, index, row);
					row.SEL = $.id.value_off;
				};
				funcAfterEdit = function(index,row,changes){
					var rowobj = $('#'+id).datagrid('getPanel').find("[datagrid-row-index='"+index+"']");
					// チェックボックスの再追加（EndEdit時に削除されるため）
					$.afterEditAddCheckbox(rowobj);
					// ボタンオブジェクトの再追加（EndEdit時に削除されるため）
					rowobj.find(".easyui-linkbutton").on("click", $.pushUpd).linkbutton({ width:  45, height: 18});
				};

				// 編集可能データグリッドの共通処理設定
				// 編集エディターの機能拡張（非表示で参照用のオブジェクトが準備してある前提、参照オブジェクトはグリッドのフィールド名を元に判断）
				$.extendDatagridEditor(that);
//			}

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
					// View内の入出力項目を調整
					if(that.reportYobiInfo()!=='1'){
						var inputs = $('#'+id).datagrid('getPanel').find('.datagrid-row .easyui-linkbutton');
						// 各行内InputをEasyUI形式に変換（class指定のInput作成だけだと普通のInputになったため）
						inputs.on("click", $.pushUpd).linkbutton({ width:  45, height: 18});
					}
					var gridData = that.getGridData('#'+id);
					that.setGridData(gridData, '#'+id);

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
				onSelect:function(index,row){
					//選択をチェックする。
					row.SEL = $.id.value_on;
						var col2 = $('#'+id).datagrid('getColumnOption', 'F2');

						if(row["F22"]=="1"){
							col2.editor = false
						}else{
							col2.editor = {type:'numberbox'}

						}
				},
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
		setGridData: function (data, target){
			var that = this;

			// 商品一覧
			if(target===undefined || target===$.id.gridholder){
				that.grd_saiyou_data =  data[$.id.gridholder];
			}
		},
		getGridData: function (target){
			var data = {};
			var targetRows= [];
			var rt = false;

			if(target===undefined || target===$.id.gridholder){
				var rowsMoydef= $($.id.gridholder).datagrid('getRows');
				for (var i=0; i<rowsMoydef.length; i++){

					if(!(rowsMoydef[i]["F2"] == "" && rowsMoydef[i]["F2"] == null) ){
						var rowDate = {
								F1	 : rowsMoydef[i]["F1"],				// F1 : idx
								F2	 : rowsMoydef[i]["F2"],				// F2 : 部門
								F3	 : rowsMoydef[i]["F3"],				// F3 : 部門名
								F4	 : rowsMoydef[i]["F4"],				// F4 : 所属コード
								F5	 : rowsMoydef[i]["F5"],				// F5 : 生鮮
								F6	 : rowsMoydef[i]["F6"],				// F6 : 納入情報
								F7	 : rowsMoydef[i]["F7"],				// F7 : 販売情報
								F8	 : rowsMoydef[i]["F8"],				// F8 : 大分
								F9	 : rowsMoydef[i]["F9"],				// F9 : 中分
								F10	 : rowsMoydef[i]["F10"],			// F10 : 率P
								F11	 : rowsMoydef[i]["F11"],			// F11 : 数P
								F12	 : rowsMoydef[i]["F12"],			// F12 : 実P
								F13	 : rowsMoydef[i]["F13"],			// F13 : 売上
								F14	 : rowsMoydef[i]["F14"],			// F14 : 点数
								F15	 : rowsMoydef[i]["F15"],			// F15 : 大
								F16	 : rowsMoydef[i]["F16"],			// F16 : 中
								F17	 : rowsMoydef[i]["F17"],			// F17 : 大
								F18	 : rowsMoydef[i]["F18"],			// F18 : 中
								F19	 : rowsMoydef[i]["F19"],			// F19 : しない
								F20	 : rowsMoydef[i]["F20"],			// F20 : ''
								F21	 : rowsMoydef[i]["F21"],			// F21 : 削除
							};
						targetRows.push(rowDate);
					}
				}
				data[$.id.gridholder] = targetRows;
			}
			return data;
		},
		getMergeGridDate: function(target){
			// 保持したデータと入力データ比較を比較する。
			var that = this;

			var newrows = that.getGridData(target)[target];		// 変更データ
			var oldrows = [];
			var targetRows= [];

			if(target===undefined || target===$.id.gridholder){
				// 催しコード一覧
				oldrows = that.grd_saiyou_data
				for (var i=0; i<newrows.length; i++){
					if(newrows[i]['F1'] != null && newrows[i]['F1'] != ""){
						if( newrows[i]['F1'] != oldrows[i]['F1'] || newrows[i]['F2']  != oldrows[i]['F2'] || newrows[i]['F3']  != oldrows[i]['F3'] || newrows[i]['F4']  != oldrows[i]['F4'] || newrows[i]['F5']  != oldrows[i]['F5']
						|| newrows[i]['F6']  != oldrows[i]['F6'] || newrows[i]['F7']  != oldrows[i]['F7'] || newrows[i]['F8']  != oldrows[i]['F8'] || newrows[i]['F9']  != oldrows[i]['F9'] || newrows[i]['F10'] != oldrows[i]['F10']
						|| newrows[i]['F11'] != oldrows[i]['F11']|| newrows[i]['F12'] != oldrows[i]['F12']|| newrows[i]['F13'] != oldrows[i]['F13']|| newrows[i]['F14'] != oldrows[i]['F14']|| newrows[i]['F15'] != oldrows[i]['F15']||newrows[i]['F16'] != oldrows[i]['F16']
						|| newrows[i]['F17'] != oldrows[i]['F17']|| newrows[i]['F18'] != oldrows[i]['F18']|| newrows[i]['F19'] != oldrows[i]['F19']|| newrows[i]['F20'] != oldrows[i]['F20']|| newrows[i]['F21'] != oldrows[i]['F21'] ){
							if(!(newrows[i]['F2']  == ""  && newrows[i]['F3']  == ""  && newrows[i]['F4']  == ""  && newrows[i]['F5']  == "0"
								&& newrows[i]['F6']  == "0" && newrows[i]['F7']  == "0" && newrows[i]['F8']  == "0" && newrows[i]['F9']  == "0" && newrows[i]['F10'] == "0"
									&& newrows[i]['F11'] == "0" && newrows[i]['F12'] == "0" && newrows[i]['F13'] == "0" && newrows[i]['F14'] == "0" && newrows[i]['F15'] == "0" &&newrows[i]['F16'] == "0"
										&& newrows[i]['F17'] == "0" && newrows[i]['F18'] == "0" && newrows[i]['F19'] == "0" && newrows[i]['F20'] == "0" && newrows[i]['F21'] == "0" )){
								var rowDate = {
										F1	 : newrows[i]["F1"],				// F1 : idx
										F2	 : newrows[i]["F2"],				// F2 : 部門
										F3	 : newrows[i]["F3"],				// F3 : 部門名
										F4	 : newrows[i]["F4"],				// F4 : 所属コード
										F5	 : newrows[i]["F5"],				// F5 : 生鮮
										F6	 : newrows[i]["F6"],				// F6 : 納入情報
										F7	 : newrows[i]["F7"],				// F7 : 販売情報
										F8	 : newrows[i]["F8"],				// F8 : 大分
										F9	 : newrows[i]["F9"],				// F9 : 中分
										F10	 : newrows[i]["F10"],			// F10 : 率P
										F11	 : newrows[i]["F11"],			// F11 : 数P
										F12	 : newrows[i]["F12"],			// F12 : 実P
										F13	 : newrows[i]["F13"],			// F13 : 売上
										F14	 : newrows[i]["F14"],			// F14 : 点数
										F15	 : newrows[i]["F15"],			// F15 : 大
										F16	 : newrows[i]["F16"],			// F16 : 中
										F17	 : newrows[i]["F17"],			// F17 : 大
										F18	 : newrows[i]["F18"],			// F18 : 中
										F19	 : newrows[i]["F19"],			// F19 : しない
										F20	 : newrows[i]["F20"],			// F20 : ''
										F21	 : newrows[i]["F21"],			// F21 : 削除
								};
								if(rowDate){
									targetRows.push(rowDate);
								}
							}
						}
					}
				}
			}
			return targetRows;
		},
		getGridData2: function (target){

			var targetRows= [];
			var rt = false;

			if(target===undefined || target===$.id.gridholder){
				var rowsMoydef= $($.id.gridholder).datagrid('getRows');
				for (var i=0; i<rowsMoydef.length; i++){

					if((rowsMoydef[i]["F2"] != "" && rowsMoydef[i]["F2"] != null)
						|| (rowsMoydef[i]["F4"] != "" && rowsMoydef[i]["F4"] != null)
						|| (rowsMoydef[i]["F5"] != "" && rowsMoydef[i]["F5"] != null)
						|| (rowsMoydef[i]["F6"] != "" && rowsMoydef[i]["F6"] != null)
						|| (rowsMoydef[i]["F7"] != "" && rowsMoydef[i]["F7"] != null)
						|| (rowsMoydef[i]["F8"] != "" && rowsMoydef[i]["F8"] != null)
						|| (rowsMoydef[i]["F9"] != "" && rowsMoydef[i]["F9"] != null)
						|| (rowsMoydef[i]["F10"] != "" && rowsMoydef[i]["F10"] != null)
						|| (rowsMoydef[i]["F11"] != "" && rowsMoydef[i]["F11"] != null)
						|| (rowsMoydef[i]["F12"] != "" && rowsMoydef[i]["F12"] != null)
						|| (rowsMoydef[i]["F13"] != "" && rowsMoydef[i]["F13"] != null)
						|| (rowsMoydef[i]["F14"] != "" && rowsMoydef[i]["F14"] != null)
						|| (rowsMoydef[i]["F15"] != "" && rowsMoydef[i]["F15"] != null)
						|| (rowsMoydef[i]["F16"] != "" && rowsMoydef[i]["F16"] != null)
						|| (rowsMoydef[i]["F17"] != "" && rowsMoydef[i]["F17"] != null)
						|| (rowsMoydef[i]["F18"] != "" && rowsMoydef[i]["F18"] != null)
						|| (rowsMoydef[i]["F19"] != "" && rowsMoydef[i]["F19"] != null)
						|| (rowsMoydef[i]["F20"] != "" && rowsMoydef[i]["F20"] != null)
						|| (rowsMoydef[i]["F21"] != "" && rowsMoydef[i]["F21"] != null)
					){
						var rowDate = {
								F1	 : rowsMoydef[i]["F1"],				// F1 : idx
								F2	 : rowsMoydef[i]["F2"],				// F2 : 部門
								F3	 : rowsMoydef[i]["F3"],				// F3 : 部門名
								F4	 : rowsMoydef[i]["F4"],				// F4 : 所属コード
								F5	 : rowsMoydef[i]["F5"],				// F5 : 生鮮
								F6	 : rowsMoydef[i]["F6"],				// F6 : 納入情報
								F7	 : rowsMoydef[i]["F7"],				// F7 : 販売情報
								F8	 : rowsMoydef[i]["F8"],				// F8 : 大分
								F9	 : rowsMoydef[i]["F9"],				// F9 : 中分
								F10	 : rowsMoydef[i]["F10"],			// F10 : 率P
								F11	 : rowsMoydef[i]["F11"],			// F11 : 数P
								F12	 : rowsMoydef[i]["F12"],			// F12 : 実P
								F13	 : rowsMoydef[i]["F13"],			// F13 : 売上
								F14	 : rowsMoydef[i]["F14"],			// F14 : 点数
								F15	 : rowsMoydef[i]["F15"],			// F15 : 大
								F16	 : rowsMoydef[i]["F16"],			// F16 : 中
								F17	 : rowsMoydef[i]["F17"],			// F17 : 大
								F18	 : rowsMoydef[i]["F18"],			// F18 : 中
								F19	 : rowsMoydef[i]["F19"],			// F19 : しない
								F20	 : rowsMoydef[i]["F20"],			// F20 : ''
								F21	 : rowsMoydef[i]["F21"],			// F21 : 削除
							};
						targetRows.push(rowDate);
					}else{

					}
				}
			}
			return targetRows;
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

			// タブ要素(a)取得
			var elems = $('#tabContent', window.parent.document).map(
				function(i,e) {
					return e;
				}).get();
			var href = elems[0].value.split(',');

			// JSON Object Clone ()
			var sendJSON = [];
			$.setJSONObject(sendJSON, 'sendBtnid', btnId, $('#'+btnId).attr("title"));		// 実行ボタン情報保持

			// 実行ボタン別処理
			switch (btnId) {
			case $.id.btn_back:
			case $.id.btn_cancel:
				// 転送先情報
				childurl = parent.$('#hdn_menu_path').val();
				break;
			case 'btn_return':
				// 転送先情報
				index = 1;
				childurl = href[index];

				break;
			default:
				break;
			}

			$.SendForm({
				type: 'post',
				url: childurl,
				data: {
					sendMode:	1,
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

			var targetRowsMoydef = that.getGridData($.id.gridholder);
			for (var j=0; j < targetRowsMoydef.length; j++) {
				if (targetRowsMoydef[j]["F2"] === newValue) {
					$.showMessage('E11040', ["部門"]);
					return false;
				}
			}

			// DB問い合わせ系
			if($('[for_inp^='+id+'_]').length > 0){
				var param = that.getInputboxParams(that, id, newValue);
				$.getsetInputboxRowData(that.name, 'for_inp', id, param, that, parentObj);
			}

			// グリッド編集系
			if(that.focusGridId!==undefined && that.editRowIndex[that.focusGridId] > -1){

				// テキスト（部門)
				if(id===$.id_inp.txt_bmncd && newValue != ""){
					var msgid = that.checkInputboxFunc($.id_inp.txt_bmncd, newValue , '');
					if(msgid !==null){
						$.showMessage(msgid, undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_bmncd), true)});
						return false;
					}
				}
				// テキスト（所属コード)
				if(id===$.id_inp.txt_szkcd && newValue != ""){
					var msgid = that.checkInputboxFunc($.id_inp.txt_szkcd, newValue , '');
					if(msgid !==null){
						$.showMessage(msgid, undefined, function(){$.addErrState(that, $('#'+$.id_inp.txt_szkcd), true)});
						return false;
					}
				}
				$($.id.hiddenChangedIdx).val("1")
			}

		},
		// IDとvalueでチェック処理を実施
		checkInputboxFunc:function(id, newValue, kbn, record, isNew){
			var that = this;

			// 部門
			if(id===$.id_inp.txt_bmncd){
				// 存在チェック
				var param = {};
				param["KEY"] =  "MST_CNT";
				param["value"] = newValue;
				var chk_cnt = $.getInputboxData(that.name, $.id.action_check, $.id_inp.txt_bmncd, [param]);
				if(chk_cnt==="" || chk_cnt==="0"){
					return "E11044";
				}
			}
			// 所属コード
			if(id===$.id_inp.txt_szkcd){
				var param = {};
				param["KEY"] =  "MST_CNT";
				param["value"] = newValue;
				var chk_cnt = $.getInputboxData(that.name, $.id.action_check, $.id_inp.txt_szkcd, [param]);
				if(chk_cnt==="" || chk_cnt==="0"){
					return "E20041";
				}
			}

			return null;
		},
		getInputboxParams: function(that, id, newValue){
			// 情報取得
			var values = {};
			values["value"] = newValue;
			values["TABLEKBN"] = that.baseTablekbn;



			// 情報設定
			return [values];
		},
		keyEventInputboxFunc:function(e, code, that, obj){

			return false;

		}
	} });
})(jQuery);