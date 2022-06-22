/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_Reportx172',			// （必須）レポートオプションの確認
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
		searchCondition:[],					// 検索条件
		gridData:[],						// 検索結果
		gridTitle:[],						// 検索結果
		grd_areahsptn_data:[],				// グリッド情報:配送パターンマスタ
		refreshRows:false,					// グリッドクリア
		openMsg:true,						// クリア実行メッセージ表示フラグ
		searchGrid:true,					// グリッド検索機能使用フラグ
		existChild:false,					// 子要素存在フラグ
		initialize: function (reportno){	// （必須）初期化
			var that = this;
			// 引き継ぎ情報
			this.jsonHidden = $.getTargetValue();

			// 初期検索条件設定
			this.jsonInit = $.getInitValue();

			// 画面の初回基本設定
			this.setInitObjectState();

			// 初期化するオブジェクト数設定
			this.initObjNum = this.dedefaultObjNum;

			var isUpdateReport = true;

			// 初期検索可能
			that.onChangeReport = true;

			var count = 2;
			// 入力テキストボックス系
			var inputbox = Object.getOwnPropertyNames($.id_inp);
			for ( var sel in inputbox ) {
				if($('#'+$.id_inp[inputbox[sel]]).length > 0){
					$.setInputbox(that, reportno, $.id_inp[inputbox[sel]], isUpdateReport);
					count++;
				}
			}

			$.setRadioInit2(that.jsonHidden, $.id.rad_areakbn, that);
			$.setCheckboxInit2(that.jsonHidden, 'chk_sel', that);
			$.setInputbox(that, reportno, $.id_inp.txt_tengpkn, isUpdateReport);
			$.setInputbox(that, reportno, $.id_inp.txt_tenkn+'_center', isUpdateReport);
			$.setInputbox(that, reportno, $.id_inp.txt_tenkn+'_ycenter', isUpdateReport);


			$('#sel').change(function(e) {
				var obj = $(this);
				if($($.id.hiddenChangedIdx).is(':enabled')){
					$($.id.hiddenChangedIdx+suffix).val("1");
				};
				// 当項目を変更した際に、値をセットする項目がある場合の処理（セット先の項目は、HTMLタグとして、for_inp=当項目のID_列名）
				if($.isFunction(that.changeInputboxFunc)){
					var newValue = $.getInputboxValue(obj);
					that.changeInputboxFunc(that, 'sel', newValue, obj);
				}
			});

			// 編集可能データグリッドの共通処理設定
			// 編集エディターの機能拡張（非表示で参照用のオブジェクトが準備してある前提、参照オブジェクトはグリッドのフィールド名を元に判断）
			$.extendDatagridEditor(that);

			// 店グループ（仕入）
			that.setEditableGrid(that, reportno, $.id.grd_ehsptn+'_hp012');

			// サブウインドウの初期化
			$.win009.init(that);	// 配送グループ

			// 初期化終了
			this.initializes =! this.initializes;

			var newval = $('#'+$.id_inp.txt_hsptn).numberbox('getValue');
			$('#'+$.id_inp.txt_hsptn).numberbox('reset').numberbox('setValue', newval);

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

			// 各種遷移ボタン
			$('#'+$.id.btn_new).on("click", $.pushChangeReport);
			$('#'+$.id.btn_copy).on("click", $.pushChangeReport);
			$('#'+$.id.btn_cancel).on("click", $.pushChangeReport);

			if(that.reportYobiInfo()==='1'){
				$.setInputBoxDisable($($.id.hiddenChangedIdx));
			}

			var test = that.reportYobiInfo();

			if(that.sendBtnid===$.id.btn_new){
				$.initReportInfo("HP011", "配送パターンマスタ　新規登録", "新規");
				$("#disp_record_info").hide();
				$("#"+$.id.btn_del).linkbutton('disable');
				$("#"+$.id.btn_del).attr('tabindex', -1).hide();
			}else{
				$.initReportInfo("HP012", "配送パターンマスタ　変更", "変更");
				$.setInputBoxDisable($("#"+$.id_inp.txt_hsptn));
			}

			$.setInputBoxDisable($("#"+$.id_inp.txt_hsgpkn + '_YCENTER'));

			// 検索ボタン
			$('#btn_search_grid').on("click", function(e){
				if(that.searchGrid){
					var hsgpcd		 = $.getInputboxValue($('#'+$.id_inp.txt_hsgpcd));			// 配送グループ
					var areakbn		 = $("input[name="+$.id.rad_areakbn+"]:checked").val();		// エリア区分

					if((!areakbn || areakbn == '') || (!hsgpcd || hsgpcd == '')){
						$.showMessage('EX1047', ['エリア区分,配送グループ']);
						return false;

					}else{
						var msgid = that.checkInputboxFunc($.id_inp.txt_hsgpcd, hsgpcd , '');
						if(msgid !==null){
							$.showMessage(msgid,"");
							return false;
						}
						// 検索実行
						$('#'+$.id.grd_ehsptn + '_hp012').datagrid('reload');
					}
				}
			});

			$.setInputBoxDisable($("#"+$.id_inp.txt_tenkn + '_center'));
			$.setInputBoxDisable($("#"+$.id_inp.txt_tenkn + '_ycenter'));
			$.setInputBoxDisable($("#"+$.id_inp.txt_tenkn + '_center_g'));
			$.setInputBoxDisable($("#"+$.id_inp.txt_tenkn + '_ycenter_g'));
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
		endUpdate:function (){

			// レポート番号取得
			var reportno = $($.id.hidden_reportno).val();
			// レポート定義位置
			var reportNumber = $.getReportNumber(reportno);
			this.changeReport(reportNumber, 'btn_return')

		},
		validation: function (){	// （必須）批准
			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
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
			var txt_hsptn		= $.getJSONObject(this.jsonString, $.id_inp.txt_hsptn).value;		// 配送パターン

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$.appendMaskMsg();

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,		// レポート名
					HSPTN:			txt_hsptn,
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

					// 検索データ（想定）
					that.gridData = JSON.parse(json).rows;
					that.gridTitle = JSON.parse(json).titles;

					var opts = JSON.parse(json).opts

					// メインデータ表示
					that.setData(that.gridData, opts);

					// 入力ボタン制御
					if(that.gridData[0]){
						if(that.gridData[0]['F11'] && that.gridData[0]['F11'] !== ''){
							// エリア別配送パターン仕入先で使用されている場合。
							// 検索機能使用不可に設定
							that.searchGrid = false;
							// 初回検索で配送グループが空の場合、検索ボタン入力不可
							$("#btn_search_grid").linkbutton('disable');
							$("#btn_search_grid").attr('tabindex', -1);
							$("#btn_hsgp").linkbutton('disable');
							$("#btn_hsgp").attr('tabindex', -1);
							$.setInputBoxDisable($("input[name="+$.id.rad_areakbn+"]"));
							$.setInputBoxDisable($('#'+$.id_inp.txt_hsgpcd));
						}
					}

					// 初回検索条件保持
					var hsgpcd	 = $.getInputboxValue($('#'+$.id_inp.txt_hsgpcd));
					var areakbn	 = $("input[name="+$.id.rad_areakbn+"]:checked").val();
					that.searchCondition[0] = {
							'txt_hsgpcd'	:hsgpcd,
							'rad_areakbn'	:areakbn
					}

					// 子要素存在フラグの設定
					if(hsgpcd && hsgpcd !== ''){
						that.existChild = true;
					}

					that.queried = true;
					// グリッド再描画
					$('#'+$.id.grd_ehsptn + '_hp012').datagrid('reload');

					// 状態保存
					$.saveState2(reportno, that.getJSONString());

					// ログ出力
					$.log(that.timeData, 'loaded:');
				}
			);
		},
		updValidation: function (){	// （必須）批准
			var that = this;

			// 入力編集を終了する。
			var gridid = $.id.grd_ehsptn+'_hp012';
			var row = $('#'+gridid).datagrid("getSelected");
			var rowIndex = $('#'+gridid).datagrid("getRowIndex", row);
			$('#'+gridid).datagrid('endEdit',rowIndex);

			var rt = $($.id.toolbarform).form('validate');
			if(!rt){
				$.addErrState(that, $('.validatebox-invalid').eq(0), false);
				return rt;
			}

			var txt_centercd	 = $.getInputboxValue($('#'+$.id_inp.txt_centercd));		// センターコード
			var txt_ycentercd	 = $.getInputboxValue($('#'+$.id_inp.txt_ycentercd));		// 横持先センターコード
			var txt_hsptn		 = $.getInputboxValue($('#'+$.id_inp.txt_hsptn));			// 配送パターン

			// センターコード
			if(txt_centercd || txt_centercd!==''){
				var msgid = that.checkInputboxFunc($.id_inp.txt_centercd, txt_centercd , '');
				if(msgid !==null){
					$.showMessage(msgid,['センターコード','基本店舗マスタ']);
					return false;
				}
			}

			// 横持センターコード
			if(txt_ycentercd || txt_ycentercd!==''){
				var msgid = that.checkInputboxFunc($.id_inp.txt_ycentercd, txt_ycentercd , '');
				if(msgid !==null){
					$.showMessage(msgid,['横持センターコード','基本店舗マスタ']);
					return false;
				}
			}

			// 配送パターン一覧
			var targetdate = [];

			var targetRows = $('#'+$.id.grd_ehsptn+'_hp012').datagrid('getRows');
			for (var i=0; i<targetRows.length; i++){
				if(targetRows[i]["TENGPCD"]){
					targetdate.push(targetRows[i]["TENGPCD"]);
				}
			}

			// 重複チェック：配送パターン
			var targetdateF = targetdate.filter(function (element, index, self) { return self.indexOf(element)*1 === index; });
			if(targetdate.length !== targetdateF.length){
				$.showMessage('E11112');
				return false;
			}

			// 登録データチェック
			var gridid = $.id.grd_ehsptn + '_hp012'
			var updaterows = that.getGridData(txt_hsptn, gridid)[gridid];		// 変更データ
			for (var i=0; i<updaterows.length; i++){
				if(updaterows[i]['CHK']=='1'){

					// 店グループコード入力チェック
					if(updaterows[i]['F2'] === ''){
						$.showMessage('EX1103', ['店グループコード']);
						return false;
					}

					// 店グループコード存在チェック
					if(updaterows[i]['F2'] && updaterows[i]['F2'] !== ''){
						var msgid = that.checkInputboxFunc($.id_inp.txt_tengpcd, updaterows[i]['F2'] , '');
						if(msgid !==null){
							$.showMessage(msgid,['店グループ','配送店グループマスタ']);
							return false;
						}
					}

					// センターコード存在チェック
					if(updaterows[i]['F5'] && updaterows[i]['F5'] !== ''){
						var msgid = that.checkInputboxFunc($.id_inp.txt_centercd, updaterows[i]['F5'] , '');
						if(msgid !==null){
							$.showMessage(msgid,['センターコード','基本店舗マスタ']);
							return false;
						}
					}

					// 横持センターコード存在チェック
					if(updaterows[i]['F6'] && updaterows[i]['F6'] !== ''){
						var msgid = that.checkInputboxFunc($.id_inp.txt_ycentercd, updaterows[i]['F6'] , '');
						if(msgid !==null){
							$.showMessage(msgid,['横持センターコード','基本店舗マスタ']);
							return false;
						}
					}
				}
			}


			// 削除データチェック:エリア別配送パターンマスタ
			var delDatas = [];
			delDatas = that.getMergeGridDate(txt_hsptn, $.id.grd_ehsptn + '_hp012', 'del');
			for (var i=0; i<delDatas.length; i++){
				// エリア配送パターン
				var param = {};
				param["KEY"] =  "MST_CNT";
				param["value"] = delDatas[i]['F1']+','+delDatas[i]['F2'];
				var chk_cnt = $.getInputboxData(that.name, $.id.action_check, 'area'+$.id_inp.txt_hsptn, [param]);
				if(chk_cnt!=="0"){
					$.showMessage('EX1042');
					return false;
				}
			}
			var hsgpcd		 = $.getInputboxValue($('#'+$.id_inp.txt_hsgpcd));
			if(hsgpcd!=""){
				var msgid = that.checkInputboxFunc($.id_inp.txt_hsgpcd, hsgpcd , '');
				if(msgid !==null){
					$.showMessage('EX1100', ['配送グループコード','配送グループマスタ']);
					return false;
				}
			}
			return rt;
		},
		updSuccess: function(id){	// validation OK時 の update処理
			var that = this;

			// 変更行情報取得
			var changedIndex = $($.id.hiddenChangedIdx).val().split(",");

			var targetDatas = [{}];
			$('#'+that.focusRootId).find('[col^=F]').each(function(){
				var col = $(this).attr('col');
				targetDatas[0][col] = $.getInputboxValue($(this));
			});

			// 新規登録時には配送パターン
			/*if(that.sendBtnid =  $.id.btn_sel_change){
				var enptyrows = [];
				targetDatas = enptyrows;
			}*/

			// エリア別設定グリッド(登録)のデータを取得
			var targetDatas_Ahsptn = [];
			var txt_hsptn = $('#'+$.id_inp.txt_hsptn).textbox('getValue');
			targetDatas_Ahsptn = that.getMergeGridDate(txt_hsptn, $.id.grd_ehsptn + '_hp012');

			// エリア別設定グリッド(削除)のsデータを取得
			var targetDatas_Ahsptn_del = [];
			targetDatas_Ahsptn_del = that.getMergeGridDate(txt_hsptn, $.id.grd_ehsptn + '_hp012', 'del');

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$.appendMaskMsg();

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,						// レポート名
					action:			$.id.action_update,				// 実行処理情報
					obj:			id,								// 実行オブジェクト
					SENDBTNID:		that.sendBtnid,
//					IDX:			$($.id.hiddenChangedIdx).val(),	// 更新対象Index
					HSPTN:			txt_hsptn,						// 配送パターン
					DATA_HSPTN:		JSON.stringify(targetDatas),	// 更新対象情報
					DATA_AHSPTN:	JSON.stringify(targetDatas_Ahsptn),
					DATA_AHSPTN_DEL:JSON.stringify(targetDatas_Ahsptn_del),
					t:				(new Date()).getTime()
				},
				function(data){
					// 検索処理エラー判定
					if($.updError(id, data)) return false;

					var afterFunc = function(){
						// 初期化
						that.clear();
						that.changeReport(that.name, 'btn_return');
					};
					$.updNormal(data, afterFunc);

					// ログ出力
					$.log(that.timeData, 'loaded:');

				}
			);
		},
		delValidation: function (){	// （必須）批准
			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = true;

			// 仕入先コード
			var txt_hsptn = $.getInputboxValue($('#'+$.id_inp.txt_hsptn));

			// エリア配送パターン
			var param = {};
			param["KEY"] =  "MST_CNT";
			param["value"] = txt_hsptn;
			var chk_cnt = $.getInputboxData(that.name, $.id.action_check, 'MSTHSPTNSIR', [param]);
			if(chk_cnt!=="0"){
				$.showMessage('E00006');
				return false;
			}

			// 入力チェック用の配列をクリア
			that.jsonTemp = [];

			return rt;
		},
		delSuccess: function(id){
			var that = this;

			var txt_hsptn		= $.getJSONObject(this.jsonString, $.id_inp.txt_hsptn).value;		// 配送パターン

			var targetDatas = [{}];
			$('#'+that.focusRootId).find('[col^=F]').each(function(){
				var forId = $(this).attr('col');
				targetDatas[0][forId] = $.getInputboxValue($(this));
			});

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			$.appendMaskMsg();

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,		// レポート名
					action:			$.id.action_delete,	// 実行処理情報
					obj:			id,								// 実行オブジェクト
					//IDX:			$($.id.hiddenChangedIdx).val(),	// 更新対象Index
					//DATA:			JSON.stringify(targetRows),		// 更新対象情報
					HSPTN:			txt_hsptn,
					DATA:			JSON.stringify(targetDatas),	// 更新対象情報
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
			// 配送パターン
			this.jsonTemp.push({
				id:		$.id_inp.txt_hsptn,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_hsptn),
				text:	''
			});
			// 店グループ
			this.jsonTemp.push({
				id:		$.id_inp.txt_tengpcd,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_tengpcd),
				text:	''
			});
			// 配送グループ
			this.jsonTemp.push({
				id:		$.id_inp.txt_hsgpcd,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_hsgpcd),
				text:	''
			});
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

				// radioボタンの値を設定
				if(that.sendBtnid===$.id.btn_sel_change){
					if(rows[0]['F5']){
						$('input[name="'+$.id.rad_areakbn+'"]').val([rows[0]['F5']]);
						$('input[name="'+$.id.rad_areakbn+'"]:checked').change();
					}
				}
			}
		},
		setRadio: function(reportno, name){
			var that = this;
			var idx = -1;

			var id = name;
			// Radio 要素の初期化
			// 初期化情報取得
			var json = $.getJSONObject(that.jsonHidden, id);
			if (json){
				// 初期化
				$('input[name="'+id+'"]').val([json.value]);
			}
			$('input[name="'+id+'"]').change(function() {
				if(idx > 0 && that.queried){
					$($.id.hiddenChangedIdx).val("1");

					//$("#"+$.id.grd_ehsptn+'_hp012').datagrid("reload");
					//that.editRowIndex[$.id.grd_tengp+gpkbn] = -1;
				}
			});

			if(that){
				if ($.inArray(id, that.initedObject) < 0){
					that.initedObject.push(id);
				}
				// 初期表示処理
				$.initialDisplay(that);
			}

			idx = 1;
		},
		setEditableGrid: function(that, reportno, id){		// データ表示
			var that = this;
			var init = true;
			// ページサイズ定義取得
			var pageList = $.fn.pagination.defaults.pageList;
			var pageSize = $.getJSONValue(this.jsonHidden, $.id.pageSize);
			var cstyler =function(value,row,index){return 'color: red;font-weight: bold;';};
			var dformatter =function(value){
				var add20 = value && value.length===6;
				var addweek = 1;	// フラグ用仮パラメータ(週まで表示したい際に使用)
				return $.getFormatDt(value, add20, addweek);
			};
			var cstyler =function(value,row,index){return 'color: red;font-weight: bold;';};
			var cformatter =function(value,row,index){return value && value===$.id.value_on?$.id.text_on:$.id.text_off;};
			var check = $('#'+$.id_inp.txt_tengpcd).attr("check") ? JSON.parse('{'+$('#'+$.id_inp.txt_tengpcd).attr("check")+'}'): JSON.parse('{}');
			var check2 = $('#'+$.id_inp.txt_centercd).attr("check") ? JSON.parse('{'+$('#'+$.id_inp.txt_centercd).attr("check")+'}'): JSON.parse('{}');

			var formatterLPad = function(value){
				return $.getFormatLPad(value, 2);
			};
			var formatterLPad2 = function(value){
				return $.getFormatLPad(value, check2.maxlen);
			};
			var parserLPad= function(value){
				return $.getParserLPad(value);
			};

			var myCheckEditor = {
					type:'checkbox',
					checked: 'onchange="alert()"',
			};

			if (pageSize==="") pageSize=$.fn.pagination.defaults.pageSize;
			that.editRowIndex[id] = -1;
			var index = -1;
			$('#'+id).datagrid({
				nowrap: true,
				border: true,
				striped: true,
				collapsible:false,
				remoteSort: true,
				rownumbers:true,
				singleSelect:true,
				checkOnSelect:false,
				selectOnCheck:false,
				frozenColumns:[[]],
				columns:[[

							{field:'SEL',				title:'選択',					editor:myCheckEditor, styler:cstyler, formatter:cformatter,	width:  35,halign:'center',align:'center',change:"alert()"},
							{field:'TENGPCD',			title:'店グループ', 			width: 90	,halign:'center',align:'left'	,editor:{type:'numberbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},formatter:formatterLPad},
							{field:'TENGPKN',			title:'店グループ名称',			width: 200	,halign:'center',align:'left'	,editor:{type:'textbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}},
							{field:'CENTERCD',			title:'センターコード',			width: 90	,halign:'center',align:'left'	,editor:{type:'numberbox'},formatter:formatterLPad2},
							{field:'TENKN_CENTER_G',	title:'センターコード名称',		width: 200	,halign:'center',align:'left'	,editor:{type:'textbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}},
							{field:'YCENTERCD',			title:'横持センターコード',		width: 90	,halign:'center',align:'left'	,editor:{type:'numberbox'},formatter:formatterLPad2},
							{field:'TENKN_YCENTER_G',	title:'横持センターコード名称',	width: 200	,halign:'center',align:'left'	,editor:{type:'textbox',options:{cls:'labelInput',editable:false,disabled:true,readonly:true}},styler:function(value,row,index){return 'background-color:#f5f5f5;';}},
							{field:'SELCHECK',			title:'店グループ名称',			width: 200	,halign:'center',align:'left'	,hidden:true},

							]],
				url:$.reg.easy,
				onBeforeLoad:function(param){
					//index = -1;
					var values = {};

					if(that.refreshRows){
						// グリッドクリア
						that.refreshRows = false;

					}else{
						// 検索ボタン押下時
						var rad_areakbn		 = $("input[name="+$.id.rad_areakbn+"]:checked").val();					// エリア区分
						var txt_hsgpcd		 = $('#'+$.id_inp.txt_hsgpcd).numberbox('getValue');					// 配送グループコード

						if(that.sendBtnid===$.id.btn_sel_change){
							// 変更画面はエリア別配送パターンとマッチングを行うため、検索用の配送パターンを送る
							var txt_hsptn		 = $('#'+$.id_inp.txt_hsptn).numberbox('getValue');					// 配送パターン
							values["HSPTN"]	 = txt_hsptn

							if(init){
								// 初回検索処理
								if(that.queried){
									if(!that.existChild){
										// 初回検索かつ子要素が存在しない場合
										values["NMLSEARCH"]	 = '0'
									}else{
										values["NMLSEARCH"]	 = '1'
									}
									init = false;
								}
							}else{
								values["NMLSEARCH"]	 = '1'
							}
						}

						values["callpage"]	 = $($.id.hidden_reportno).val()										// 呼出元レポート名
						values["AREAKBN"]	 = rad_areakbn															// 配送パターン
						values["HSGPCD"]	 = txt_hsgpcd															// 配送グループコード
					}

					var json = [values];
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
					that.changeInputboxFunc( that, id, null, $('#'+id), true);
					// 計算項目算出のため、変更時処理呼出
					// チェックボックスの設定
					$.initCheckboxCss($('#'+id).datagrid('getPanel').find('.datagrid-body'));
					if(that.queried){
						if(index===-1){
							index=1;
							// 初回検索情報保持
							var txt_hsptn = $.getInputboxValue($('#'+$.id_inp.txt_hsptn));
							var gridData = that.getGridData(txt_hsptn, id);
							that.setGridData(gridData, id);
						}
					}
				},
				onClickCell:function(rowIndex, field, value){
					// 列名保持
					that.columnName = field;
				},
				onClickRow: function(index,field){$.clickEditableDatagridCell(that,id, index)},
				onBeforeEdit:function(index,row){
					var editFlg = true
					var txt_tengpcd = row.TENGPCD;
					if(!txt_tengpcd || txt_tengpcd == "" ){
						editFlg = false
					}

					if(!editFlg){
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
							evt.shiftKey = adds === -1;
							$('#'+id).parents('.datagrid').eq(0).trigger(evt);
						}
						return false;
					}
				},
				onBeginEdit:function(index,row){
					var txt_tengpcd = row.TENGPCD;
					if(!txt_tengpcd || txt_tengpcd == "" ){
						$.setInputBoxDisable($('#'+'chk_sel'+"_"));
					}

					$.beginEditDatagridRow(that,id, index, row);
					if(row.SEL===$.id.value_on){
						$('#'+id).datagrid('getPanel').find("[datagrid-row-index='"+index+"']").find("[id^='"+$.id_inp.txt_tengpcd+"_"+"']").each(function(){
							var that = this;
							var target = $(that).textbox('textbox');
							if(target){
								target.removeAttr('disabled')
								target.attr('readonly',false);
								target.attr('tabindex', 1);
							}
						});
						$('#'+id).datagrid('getPanel').find("[datagrid-row-index='"+index+"']").find("[id^='"+$.id_inp.txt_centercd+"_"+"']").each(function(){
							var that = this;
							var target = $(that).textbox('textbox');
							if(target){
								target.removeAttr('disabled')
								target.attr('readonly',false);
								target.attr('tabindex', 2);
							}
						});
						$('#'+id).datagrid('getPanel').find("[datagrid-row-index='"+index+"']").find("[id^='"+$.id_inp.txt_ycentercd+"_"+"']").each(function(){
							var that = this;
							var target = $(that).textbox('textbox');
							if(target){
								target.removeAttr('disabled')
								target.attr('readonly',false);
								target.attr('tabindex', 3);
							}
						});
					}else{
						$('#'+id).datagrid('getPanel').find("[datagrid-row-index='"+index+"']").find("[id^='"+$.id_inp.txt_tengpcd+"_"+"']").each(function(){
							var that = this;
							var target = $(that).textbox('textbox');
							target.attr('tabindex', -1);
							target.attr('readonly', 'readonly');
							target.attr('disabled', 'disabled');
						});

						$('#'+id).datagrid('getPanel').find("[datagrid-row-index='"+index+"']").find("[id^='"+$.id_inp.txt_centercd+"_"+"']").each(function(){
							var that = this;
							var target = $(that).textbox('textbox');
							target.attr('tabindex', -1);
							target.attr('readonly', 'readonly');
							target.attr('disabled', 'disabled');
						});
						$('#'+id).datagrid('getPanel').find("[datagrid-row-index='"+index+"']").find("[id^='"+$.id_inp.txt_ycentercd+"_"+"']").each(function(){
							var that = this;
							var target = $(that).textbox('textbox');
							target.attr('tabindex', -1);
							target.attr('readonly', 'readonly');
							target.attr('disabled', 'disabled');
						});
					}
				},
				onEndEdit: function(index,row,changes){$.endEditDatagridRow(that, id, index, row)},
				onAfterEdit: function(index,row,changes){
					// チェックボックスの再追加（EndEdit時に削除されるため）
					$.afterEditAddCheckbox($('#'+id).datagrid('getPanel').find("[datagrid-row-index='"+index+"']"));
				}
			});
		},
		getGridData: function (hsptn, target){
			var that = this;

			var data = {};
			var targetRows= [];

			// エリア別設定一覧
			if(target===undefined || target===$.id.grd_ehsptn + '_hp012'){
				var rowsAreahsptn	 = $('#'+$.id.grd_ehsptn + '_hp012').datagrid('getRows');
				var txt_hsgpcd		 = $('#'+$.id_inp.txt_hsgpcd).textbox('getValue');

				for (var i=0; i<rowsAreahsptn.length; i++){
					var rowDate = {
							F1	 : hsptn,
							F2	 : rowsAreahsptn[i]["TENGPCD"],
							F3	 : txt_hsgpcd,
							F4	 : $("input[name="+$.id.rad_areakbn+"]:checked").val(),
							F5	 : rowsAreahsptn[i]["CENTERCD"],
							F6	 : rowsAreahsptn[i]["YCENTERCD"],
							CHK	 : rowsAreahsptn[i]["SEL"],
					};
					targetRows.push(rowDate);
				}
				data[$.id.grd_ehsptn + '_hp012'] = targetRows;
			}
			return data;
		},
		getMergeGridDate: function(hsptn, target, del){
			// 保持したデータと入力データ比較を比較する。
			var that = this;

			var newrows = that.getGridData(hsptn, target)[target];		// 変更データ
			var oldrows = [];
			var targetRows= [];

			if(target===undefined || target===$.id.grd_ehsptn + '_hp012'){
				if(del && del === 'del'){
					// 削除データ
					oldrows = that.grd_areahsptn_data

					for (var i=0; i<oldrows.length; i++){
						var delFlg	 = true;
						var oldvalue = oldrows[i]['F2']			// 店グループコード(初回検索時)
						var chK = oldrows[i]['CHK']				// 選択チェック
						if(!chK || chK == '0'){
							// 初期検索結果にてチェックなしのデータは登録されているデータではないので、チェックから除外する。
							oldvalue = undefined;
						}

						if(oldvalue){
							var newLines = newrows.filter(function(item, index){
								if(item.F2){
									if((item.F2).indexOf(oldvalue) == -1){
										// 新規登録データ内に、初回検索データの店グループが存在しない場合

									}else{
										if(item.CHK !== '0'){
											// 新規登録データ内に、初回検索データの店グループは存在するがチェックが外されている場合
											delFlg = false;
										}
									}
								}
							});
							if(delFlg){
								var rowDate = {
										F1	 : oldrows[i]["F1"],
										F2	 : oldrows[i]["F2"],
								};
								if(rowDate){
									targetRows.push(rowDate);
								}
							}
						}
					}

					/*for (var i=0; i<newrows.length; i++){
						if(newrows[i]["CHK"]==='0' && (newrows[i]['F2'] ? newrows[i]['F2'] : "") !== '' ){
							if(newrows[i]["F2"]){
								var rowDate = {
										F1	 : newrows[i]["F1"],
										F2	 : newrows[i]["F2"],
								};
								if(rowDate){
									targetRows.push(rowDate);
								}
							}
						}
					}*/
				}else{
					oldrows = that.grd_areahsptn_data
					for (var i=0; i<newrows.length; i++){
						// 登録データ
						if(newrows[i]["CHK"]==='1'){
							if(newrows[i]["F2"]){
								var rowDate = {
										F1	 : newrows[i]["F1"],
										F2	 : newrows[i]["F2"],
										F3	 : newrows[i]["F3"],
										F4	 : newrows[i]["F4"],
										F5	 : newrows[i]["F5"],
										F6	 : newrows[i]["F6"],
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
		setGridData: function (data, target){
			var that = this;

			// 実仕入先一覧
			if(target===undefined || target===$.id.grd_ehsptn + '_hp012'){
				that.grd_areahsptn_data =  data[$.id.grd_ehsptn + '_hp012'];
			}
		},
		setObjectState: function(){	// 軸の選択内容による制御

		},
		getGridParams:function(that, id){
			var values = {};
			values["callpage"] = $($.id.hidden_reportno).val()										// 呼出元レポート名

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
			var sendMode = "";

			// タブ要素(a)取得
			var elems = $('#tabContent', window.parent.document).map(
				function(i,e) {
					return e;
				}).get();
			var href = elems[0].value.split(',');

			// JSON Object Clone ()
			var sendJSON = [];
			$.setJSONObject(sendJSON, 'sendBtnid', btnId, $('#'+btnId).attr("title"));		// 実行ボタン情報保持

			// 戻る実行時用に現在の画面情報を保持する
			var states = $.getBackBaseJSON(that);
			// TODO 各種グリッド情報を設定
			var newrepinfos = $.getBackJSON(that, states);
			$.setJSONObject(sendJSON, 'repinfo', newrepinfos, '');

			// 呼出別処理
			switch (btnId) {
			case $.id.btn_cancel:
			case $.id.btn_back:
			case "btn_return":
				// 転送先情報
				sendMode = 2;

				if(that.reportYobiInfo()==='1'){
					index = 2;
				}else{
					index = 1;
				}
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
		changeInputboxFunc:function(that, id, newValue, obj){

			var parentObj = $('#'+that.focusRootId);
			var msgParam  = [];
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

			// 配送グループ名称取得
			if(id===$.id_inp.txt_hsgpcd || id===$.id.rad_areakbn){
				 that.checkInputboxFunc(id, newValue, '');
			}


			// 検索、入力後特殊処理
			if(that.queried){
				var msgid = null;

				// エリア別設定グリッドクリア
				if(id===$.id_inp.txt_hsgpcd || id===$.id.rad_areakbn){
					var clearFlg	 = true
					var doClear		 = false;
					var saveData	 = true;
					if(id===$.id_inp.txt_hsgpcd){
						var rad_areakbn = $("input[name="+$.id.rad_areakbn+"]:checked").val();
						if(!rad_areakbn || rad_areakbn == ''){
							clearFlg = false;
						}
					}else{
						var txt_hsgpcd = $.getInputboxValue($('#'+$.id_inp.txt_hsgpcd));
						if(!txt_hsgpcd || txt_hsgpcd == ''){
							clearFlg = false;
						}
					}

					if(clearFlg){
						// グリッドに入力内容があるか確認する
						var rowsAreahsptn	 = $('#'+$.id.grd_ehsptn + '_hp012').datagrid('getRows');
						for (var i=0; i<rowsAreahsptn.length; i++){
							if( rowsAreahsptn[i]["SEL"] ? rowsAreahsptn[i]["SEL"] : '' !==''
								|| rowsAreahsptn[i]["TENGPCD"] ? rowsAreahsptn[i]["TENGPCD"] : '' !==''
								|| rowsAreahsptn[i]["CENTERCD"] ? rowsAreahsptn[i]["CENTERCD"] : '' !==''
								|| rowsAreahsptn[i]["YCENTERCD"] ? rowsAreahsptn[i]["YCENTERCD"] : '' !==''){
								doClear = true;
							}
						}
					}

					// 確認メッセージを表示
					if(doClear){
						if(that.openMsg){
							//that.openMsg = false;
							var func_ok = function(){
								// グリッドクリア実行
								var rowsAreahsptn	 = $('#'+$.id.grd_ehsptn + '_hp012').datagrid('getRows');
								var rowsAreahsptn	 = [];
								that.refreshRows = true;
								$('#'+$.id.grd_ehsptn + '_hp012').datagrid('load',{});
								// 入力データを保持
								that.searchCondition[0][id] = newValue;
								//that.openMsg = true;
							}
							var func_no = function(){
								// 前回入力値設定によるメッセージ再表示を防ぐため、表示フラグをOFFにする。
								that.openMsg = false;
								// 前回入力値に戻す。
								if(id == $.id.rad_areakbn){
									// エリア区分の場合Object取得方法が異なる。
									$.setInputboxValue($("input[name="+id+"]"), that.searchCondition[0][id]);
								}else{
									$.setInputboxValue($('#'+id), that.searchCondition[0][id]);
								}
							}
							that.confirmReportUnregist(func_ok,func_no);
						}else{
							// キャンセル選択時に前回入力データの設定により、changeInputFunctionが動くため、メッセージを表示せずに表示フラグをtrueに戻す
							that.openMsg = true;
						}
					}else{
						// メッセージ表示以外の値変更
						// 入力データを保持
						that.searchCondition[0][id] = newValue;
					}
				}

				// グリッドの入力切替
				if(id==='chk_sel'){

					if(newValue=== '1'){
						$('#'+that.focusGridId).datagrid('getPanel').find("[datagrid-row-index='"+that.editRowIndex[that.focusGridId]+"']").find("[id^='"+$.id_inp.txt_tengpcd+"_"+"']").each(function(){
							var that = this;
							var target = $(that).textbox('textbox');
							if(target){
								target.attr('readonly',false);
								target.attr('tabindex', 1);
								target.removeAttr('disabled')
								//target = $(that).textbox('enable');
								//target.textbox('enable');
							}
						});
						$('#'+that.focusGridId).datagrid('getPanel').find("[datagrid-row-index='"+that.editRowIndex[that.focusGridId]+"']").find("[id^='"+$.id_inp.txt_centercd+"_"+"']").each(function(){
							var that = this;
							var target = $(that).textbox('textbox');
							if(target){
								target.attr('readonly',false);
								target.attr('tabindex', 2);
								target.removeAttr('disabled')
								//target = $(that).textbox('enable');
								//target.textbox('enable');
							}
						});
						$('#'+that.focusGridId).datagrid('getPanel').find("[datagrid-row-index='"+that.editRowIndex[that.focusGridId]+"']").find("[id^='"+$.id_inp.txt_ycentercd+"_"+"']").each(function(){
							var that = this;
							var target = $(that).textbox('textbox');
							if(target){
								target.attr('readonly',false);
								target.attr('tabindex', 3);
								target.removeAttr('disabled')
								//target = $(that).textbox('enable');
								//target.textbox('enable');
							}
						});

					}else{

						$('#'+that.focusGridId).datagrid('getPanel').find("[datagrid-row-index='"+that.editRowIndex[that.focusGridId]+"']").find("[id^='"+$.id_inp.txt_tengpcd+"_"+"']").each(function(){
							var that = this;
							var target = $(that).textbox('textbox');
							target.attr('tabindex', -1);
							target.attr('readonly', 'readonly');
							target.attr('disabled', 'disabled');
						});
						$('#'+that.focusGridId).datagrid('getPanel').find("[datagrid-row-index='"+that.editRowIndex[that.focusGridId]+"']").find("[id^='"+$.id_inp.txt_centercd+"_"+"']").each(function(){
							var that = this;
							var target = $(that).textbox('textbox');
							target.attr('tabindex', -1);
							target.attr('readonly', 'readonly');
							target.attr('disabled', 'disabled');
						});
						$('#'+that.focusGridId).datagrid('getPanel').find("[datagrid-row-index='"+that.editRowIndex[that.focusGridId]+"']").find("[id^='"+$.id_inp.txt_ycentercd+"_"+"']").each(function(){
							var that = this;
							var target = $(that).textbox('textbox');
							target.attr('tabindex', -1);
							target.attr('readonly', 'readonly');
							target.attr('disabled', 'disabled');
						});
					}
				}

				if(msgid !==null){
					if(msgParam.length > 0){
						$.showMessage(msgid, msgParam, func_focus );
					}else{
						$.showMessage(msgid, undefined, func_focus );
					}
					return false;
				}
			}
		},
		// IDとvalueでチェック処理を実施
		checkInputboxFunc:function(id, newValue, kbn, record, isNew){
			var that = this;
			var sdt, edt;

			// 配送グループ名称検索
			if(id===$.id_inp.txt_hsgpcd || id===$.id.rad_areakbn){
				if(newValue !== '' && newValue){
					var param = {};

					if(id===$.id_inp.txt_hsgpcd){
						// 配送グループコード変更時
						param["value"] = newValue;
						param["AREAKBN"] = $("input[name="+$.id.rad_areakbn+"]:checked").val();
					}else{
						// エリア区分変更時
						param["value"] =  $.getInputboxValue($('#'+$.id_inp.txt_hsgpcd));
						param["AREAKBN"] = newValue;
					}

					var rows = $.getSelectListData(that.name, $.id.action_change, $.id_inp.txt_hsgpcd, [param]);
					//var chk_cnt = $.getInputboxData(that.name, $.id.action_change, $.id_inp.txt_hsptn, [param]);
					if(rows.length > 0){
						$.setInputboxValue($('#'+$.id_inp.txt_hsgpkn+'_YCENTER'), rows[0]['F2']);
					}else{
						$.setInputboxValue($('#'+$.id_inp.txt_hsgpkn+'_YCENTER'), '');
						return 'E11035'
					}
				}
			}

			// 店グループコード
			if(id===$.id_inp.txt_tengpcd){
				if(newValue !== '' && newValue){
					var hsgpcd = $.getInputboxValue($('#'+$.id_inp.txt_hsgpcd));
					var param = {};
					//param["KEY"] =  "MST_CNT";
					param["value"] = newValue;
					param["HSGPCD"] = hsgpcd;
					var rows = $.getSelectListData(that.name, $.id.action_change, id, [param]);
					if(rows.length == 0){
						return "EX1100";
					}
				}
			}

			// エリア配送パターン
			/*if(id=== 'area' + $.id_inp.txt_hsptn || id===$.id_inp.txt_hsptn){
				if(newValue !== '' && newValue){
					// エリア配送パターン
					var param = {};
					param["KEY"] =  "MST_CNT";
					param["value"] = newValue;
					var chk_cnt = $.getInputboxData(that.name, $.id.action_check, $.id_inp.txt_hsptn, [param]);
					if(chk_cnt==="" || chk_cnt==="0"){
						return "EX1100";
					}
				}
			}*/

			// センターコード
			if(id===$.id_inp.txt_centercd || id===$.id_inp.txt_ycentercd){
				if(newValue !== '' && newValue){
					// センターコード
					var param = {};
					param["KEY"] =  "MST_CNT";
					param["value"] = newValue;
					var chk_cnt = $.getInputboxData(that.name, $.id.action_check, $.id_inp.txt_tencd, [param]);
					if(chk_cnt==="" || chk_cnt==="0"){
						return "EX1100";
					}
				}
			}

			return null;
		},
		getInputboxParams: function(that, id, newValue){
			// 情報取得
			var values = {};
			values["value"] = newValue;
			values["TABLEKBN"] = that.baseTablekbn;

			// 仕入先
			if(id===$.id_inp.txt_hsptn){
				values["HSPTN"] = $.getInputboxValue($('#'+$.id_inp.txt_hsptn));
			}
			// グループコード
			if(id===$.id_inp.txt_tengpcd){
				//values["TENGPCD"] = $.getInputboxValue($('#'+$.id_inp.txt_tengpcd));
				values["HSGPCD"] = $.getInputboxValue($('#'+$.id_inp.txt_hsgpcd));
			}
			// センターコード
			if(id===$.id_inp.txt_centercd){
				values["CENTERCD"] = $.getInputboxValue($('#'+$.id_inp.txt_centercd));
			}
			// 横持先センターコード
			if(id===$.id_inp.txt_ycentercd){
				values["YCENTERCD"] = $.getInputboxValue($('#'+$.id_inp.txt_ycentercd));
			}
			// 配送グループ
			/*if(id===$.id_inp.txt_hsgpcd){
				values["AREAKBN"] = $("input[name="+$.id.rad_areakbn+"]:checked").val();
			}*/

			// 情報設定
			return [values];
		},
		/**
		 * 帳票移動の警告処理
		 * @param func	- OKボタンが押下されたのちに実行される処理
		 * @param msgid	- 警告メッセージID
		 */
		confirmReportUnregist : function(func_ok, func_no, msgid){
			// 未登録の警告メッセージが必要な場合
			if($.getConfirmUnregistFlg($($.id.hiddenChangedIdx))){
				if(msgid===undefined) msgid = 'E11025';
				$.showMessage(msgid, undefined, func_ok, func_no);
			}else if (typeof func === 'function'){
				func();
			}
		},
	} });
})(jQuery);