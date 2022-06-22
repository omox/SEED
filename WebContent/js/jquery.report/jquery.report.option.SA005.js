/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_ReportSA005',			// （必須）レポートオプションの確認
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
		dedefaultObjNum:	7,	// 初期化オブジェクト数
		initObjNum:	-1,
		initedObject: [],
		maxMergeCell: 1,
		onChangeFlag : false,
		onChangeFlag2 : false,
		columnName:'',	// OnClickRowの列名
		queried : false,
		editRowIndex:{},						// グリッド編集行保持
		initializes : true,
		onChangeReport: false,
		reportYobiInfo: function(){			// （必須）レポートメニューに登録された予備情報＝帳票をどういった表示にしたいかの情報
			return $('#reportYobi1').val();
		},
		returnPageInfo: function(){			// （必須）レポートメニューに登録された予備情報＝帳票をどういった表示にしたいかの情報
			return $('#returnPageInfo1').val();
		},
		sendBtnid: "",						// 呼出ボタンID情報
		pushBtnId: "",						// 実行ボタンID情報(検索系で利用)
		focusRootId:"cc",					// （キー移動時必須）キー移動イベントのルートとなるパネルのID
		focusParentId:"",					// （キー移動時必須）現在フォーカスがあたっている項目の親となるパネルのID
		focusGridId:"",						// （キー移動時必須）現在フォーカスがあたっているDataGridのID
		grd_saiyou_data:[],					// グリッド情報:採用情報
		TENGPCD:"",
		TPNG2FLG:"",
		initialize: function (reportno){	// （必須）初期化
			var that = this;
			// 引き継ぎ情報
			this.jsonHidden = $.getTargetValue();
			// 画面の初回基本設定
			this.setInitObjectState();

			// 初期検索条件設定
			this.jsonInit = $.getInitValue();
			// データ表示エリア初期化

			// 初期化するオブジェクト数設定
			this.initObjNum = this.dedefaultObjNum;
			var isUpdateReport = true;

			// 初期表示処理
			that.onChangeReport = true;

			// 入力テキストボックス系
			var inputbox = Object.getOwnPropertyNames($.id_inp);
			for ( var sel in inputbox ) {
				if($('#'+$.id_inp[inputbox[sel]]).length > 0){
					$.setInputbox(that, reportno, $.id_inp[inputbox[sel]], isUpdateReport);
				}
			}
			// 店グループ
			that.setGroup(that, reportno, $.id.sel_tenkn, that);
			// ラジオボタン系
			$.setRadioInit2(that.jsonHidden, $.id.rad_adopt, that);

			// Load処理回避
			//$.tryChangeURL(null);

			// 編集エディターの機能拡張（非表示で参照用のオブジェクトが準備してある前提、参照オブジェクトはグリッドのフィールド名を元に判断）
			$.extendDatagridEditor(that);
			// データ表示エリア初期化
			that.setGrid('gridholder', reportno);

			// 初期化終了
			this.initializes =! this.initializes;
			var qasyukbn	= $.getJSONValue(that.jsonHidden, $.id_inp.sel_qasyukbn);

			//$('#cc').layout('panel', 'north').panel('resize', {height:135});

			$.initialDisplay(that);

			// チェックボックスの設定
			$.initCheckboxCss($("#"+that.focusRootId));
			// キーイベントの設定
			$.initKeyEvent(that);

			// ログ出力
			$.log(that.timeData, 'initialize:');
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

			$($.id.buttons).show();
			// 各種ボタン
			$('#'+$.id.btn_new).on("click", $.pushChangeReport);
			$('#'+$.id.btn_copy).on("click", $.pushChangeReport);
			$('#'+$.id.btn_new+2).on("click", $.pushChangeReport);
			var qasyukbn	= $.getJSONValue(that.jsonHidden, $.id_inp.sel_qasyukbn);

			$.initReportInfo("SA005", "特売アンケート状況　売価差替", "一覧");

			$('#'+$.id.btn_sel_change).on("click", $.pushChangeReport);
			$('#'+$.id.btn_cancel).on("click", $.pushChangeReport);

		},
		initCondition: function (){	// 条件初期値セット
			var that = this;
			// 初期化項目
		},
		clear:function(){
			// 隠し情報初期化
			$($.id.hiddenChangedIdx).val("");						// 変更行Index
//			// グリッド初期化
			this.success(this.name, false);
		},
		validation: function (btnId){	// （必須）批准
			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			//var rt = $($.id.toolbarform).form('validate');
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
			var szMoyskbn	= $.getJSONObject(this.jsonString, $.id_inp.txt_moyskbn).value;		// 催し区分
			var szMoysstdt	= $.getJSONObject(this.jsonString, $.id_inp.txt_moysstdt).value;	// 催しコード（催し開始日）
			var szMoysrban	= $.getJSONObject(this.jsonString, $.id_inp.txt_moysrban).value;	// 催し連番
			var szTencd		= $.getJSONObject(this.jsonString, $.id_inp.txt_tencd).value;	// 催し連番
			var chk_kyosei	= $("input[id=kyosei_flg]:checked").val();							// メーカー名無し
			var qasyukbn	= $.getJSONValue(that.jsonHidden, $.id_inp.sel_qasyukbn);			//アンケート種類

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
					PUSHBTNID:		that.pushBtnId,
					MOYSKBN:		szMoyskbn,		// 催し区分
					MOYSSTDT:		szMoysstdt,		// 催しコード（催し開始日）
					MOYSRBAN:		szMoysrban,		// 催し連番
					TENCD:			szTencd,		// 店コード
					QASYUKBN:		qasyukbn,		// アンケート種類
					TENGPCD:		that.TENGPCD,	// 店グループコード
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
					that.TPNG2FLG = opts.rows_[0]["F9"];
					if(opts.rows_[0]["F10"]==1){
						$.setInputBoxDisable($('[name='+$.id.rad_adopt+"]"));
					}

					that.queried = true;
					that.pushBtnId = btnId;
					// Load処理回避
					$.tryChangeURL($.reg.jqeasy);

					// グリッド再描画（easyui 1.4.2 対応）
					$($.id.gridholder).datagrid('load', {} );
					$.removeMask();
					$.removeMaskMsg();
					// 基本入力初期値保持　データを保持保持
					var Data = that.getGridData('data');
					that.setGridData(Data, 'data');
					$($.id.hiddenChangedIdx).val('');
					if(opts.rows_[0]["F9"]==1){
						$($.id.hiddenChangedIdx).val("1");
					}
					// ログ出力
					$.log(that.timeData, 'loaded:');
				}
			);
		},
		updValidation: function (){	// （必須）批准
			var that = this;

			var row = $($.id.gridholder).datagrid("getSelected");
			var rowIndex = $($.id.gridholder).datagrid("getRowIndex", row);
			$($.id.gridholder).datagrid('endEdit',rowIndex);

			var rt = $($.id.toolbarform).form('validate');

			var targetDatas = [{}];
			$('#'+that.focusRootId).find('[col^=F]').each(function(){
				var col = $(this).attr('col');
				targetDatas[0][col] = $.getInputboxValue($(this));
			});
//			that.changeReport(that.name, 'btn_upd');
//			return false;
			// 商品一覧のデータを取得

			if(targetDatas[0]["F13"] =="1"){
				$.showMessage("E20205");
				return false;
			}


			if(targetDatas[0]["F9"] ==1){
				var targetDatasShn = that.getGridData($.id.gridholder)[$.id.gridholder];
			}else{
				var targetDatasShn = that.getMergeGridDate($.id.gridholder);
			}

			for (var i=0; i<targetDatasShn.length; i++){
				var URICHGAM1 = targetDatasShn[i]['F1']//希望総売価（1売り）
				var URICHGAM2 = targetDatasShn[i]['F2']//希望総売価　バンドル1
				var URICHGAM3 = targetDatasShn[i]['F3']//希望総売価　バンドル2
				var ABAIKAAM = targetDatasShn[i]['F4']//A総売価
				var BD1ABAIKAAN = targetDatasShn[i]['F11']//A総売価（バンドル１）
				var BD2ABAIKAAN = targetDatasShn[i]['F12']//A総売価（バンドル２）
				var ADDSHUKBN = targetDatasShn[i]['F13']//登録種別
				var BD1TENSU = targetDatasShn[i]['F14']//点数_バンドル１
				var BD2TENSU = targetDatasShn[i]['F15']//点数_バンドル２
				var BBAIKAAM = targetDatasShn[i]['F16']//B総売価
				var CBAIKAAM = targetDatasShn[i]['F17']//C総売価
				var TPNG2FLG = targetDatasShn[i]['F18']//売価選択禁止フラグ
				var WARIBIKIA = targetDatasShn[i]['F20']//割引率区分A
				var WARIBIKIB = targetDatasShn[i]['F21']//割引率区分B
				var WARIBIKIC = targetDatasShn[i]['F22']//割引率区分C
				//var IDX = targetDatasShn[i]['F19']//売価選択禁止フラグ
				if(ADDSHUKBN==1){
					if(URICHGAM1!=WARIBIKIA&&URICHGAM1!=WARIBIKIB&&URICHGAM1!=WARIBIKIC){
						$.showMessage('EX1047', ["A総売価orB総売価orC総売価のいずれか"], function(){});
						return false;
					}
				}
				if(TPNG2FLG=="1"||!(BBAIKAAM.length==0 && CBAIKAAM.length==0)){
					if(URICHGAM1 < 1){
						//$.showMessage("E20467", undefined, function(){$.addErrState(that, $('#'+'gridholder'), true, {NO:IDX, ID:$.id_inp.txt_urichgam1})});
						$.showMessage("E20467");
						return false;
					}
				}

				// 希望総売価(バンドル)の入力チェック有無判定
				var urichgamB1 = true		// 入力可フラグ：希望総売価　バンドル1
				var urichgamB2 = true		// 入力可フラグ：希望総売価　バンドル2
				if(TPNG2FLG == "1" || ADDSHUKBN == "1" || ( BBAIKAAM.length==0 && CBAIKAAM.length==0)){
					// ○下記条件に当てはまる場合は希望総売価に入力を行えない
					// 売価選択禁止フラグ ==1
					// 登録種別 == 1
					// B総売価、C総売価が未入力の場合
					urichgamB1 = false
					urichgamB2 = false
				}
				if(BD1ABAIKAAN==null || BD1ABAIKAAN==""){
					// A総売価（バンドル１）が未入力の場合
					urichgamB1 = false
				}
				if(BD2ABAIKAAN==null || BD2ABAIKAAN==""){
					// A総売価（バンドル３）が未入力の場合
					urichgamB2 = false
				}

				if(URICHGAM2!=""){
					if(urichgamB1 && URICHGAM2 < 1){
						//$.showMessage("E20468", undefined, function(){$.addErrState(that, $('#'+'gridholder'), true, {NO:IDX, ID:$.id_inp.txt_urichgam2})});
						$.showMessage("E20468");
						//希望総売価(バンドル1)は1以上を入力してください。
						return false;
					}
				}else if(BD1ABAIKAAN.length>0 && BD1ABAIKAAN!="0"){
					//$.showMessage("E20094", undefined, function(){$.addErrState(that, $('#'+'gridholder'), true, {NO:IDX, ID:$.id_inp.txt_urichgam2})});
					$.showMessage("E20094");
					//A総売価（バンドル１）が入力されている場合、希望総売価(バンドル１)は必須入力です。
					return false;
				}
				if(URICHGAM3!=""){
					if(urichgamB2 && URICHGAM3 < 1 ){
						//$.showMessage("E20469", undefined, function(){$.addErrState(that, $('#'+'gridholder'), true, {NO:IDX, ID:$.id_inp.txt_urichgam3})});
						$.showMessage("E20469");
						//希望総売価(バンドル2)は1以上を入力してください。
						return false;
					}
				}else if(BD2ABAIKAAN.length>0 && BD2ABAIKAAN!="0"){
					//$.showMessage("E20095", undefined, function(){$.addErrState(that, $('#'+'gridholder'), true, {NO:IDX, ID:$.id_inp.txt_urichgam3})});
					$.showMessage("E20095");
					//A総売価（バンドル２）が入力されている場合、希望総売価(バンドル２)は必須入力です。
					return msg;
				}
				if( ADDSHUKBN!="1" && BD1ABAIKAAN.length>0 && BD1ABAIKAAN!="0"
					&& BD2ABAIKAAN.length>0 && BD2ABAIKAAN!="0" ){
					if(parseInt(URICHGAM1) >= parseInt(URICHGAM2) || parseInt(URICHGAM2) >= parseInt(URICHGAM3)){
						$.showMessage("E20097");
						//希望総売価(1売り)＜希望総売価（バンドル１）＜希望総売価（バンドル２）です。
						return false;
					}
					if(parseInt(URICHGAM1) < parseInt(URICHGAM2/BD1TENSU)){
						$.showMessage("E20603");
						//希望総売価（バンドル１）の平均売価（＝円/個）が希望総売価(1売り)より大きくなっています。
						return false;
					}else if(parseInt(URICHGAM2/BD1TENSU) < parseInt(URICHGAM3/BD2TENSU)){
						$.showMessage("E20604");
						//希望総売価（バンドル２）の平均売価≦希望総売価（バンドル１）の平均売価の範囲で入力してください。
						return false;
					}
				}
				if( ADDSHUKBN!="1" && BD1ABAIKAAN.length>0 && BD1ABAIKAAN!="0" ){
					if(URICHGAM1 >= URICHGAM2){
						$.showMessage("E20470");
						return msg;
					}
					if(parseInt(URICHGAM1) < parseInt(URICHGAM2/BD1TENSU)){
						$.showMessage("E20603");
						return msg;
					}
				}
			}

			return rt;
		},
		updSuccess: function(id){	// validation OK時 の update処理
			var that = this;
			var szMoyskbn		= $.getJSONObject(this.jsonString, $.id_inp.txt_moyskbn).value;		// 催し区分
			var szMoysstdt		= $.getJSONObject(this.jsonString, $.id_inp.txt_moysstdt).value;	// 催しコード（催し開始日）
			var szMoysrban		= $.getJSONObject(this.jsonString, $.id_inp.txt_moysrban).value;	// 催し連番
			var chk_kyosei		= $("input[id=kyosei_flg]:checked").val();							// メーカー名無し
			var qasyukbn		= $.getJSONValue(that.jsonHidden, $.id_inp.sel_qasyukbn);			//アンケート種類
			var szAdopt		= $("input[name="+$.id.rad_adopt+"]:checked").val();		// マスター区分
			// 変更行情報取得
			var targetDatas = [{}];
			$('#'+that.focusRootId).find('[col^=F]').each(function(){
				var col = $(this).attr('col');
				targetDatas[0][col] = $.getInputboxValue($(this));
			});
//			that.changeReport(that.name, 'btn_upd');
//			return false;
			// 商品一覧のデータを取得
			if(targetDatas[0]["F9"] ==1){
				var targetDatasShn = that.getGridData($.id.gridholder)[$.id.gridholder];
			}else{
				var targetDatasShn = that.getMergeGridDate($.id.gridholder);
			}

			// 処理時間計測用
			that.timeData = (new Date()).getTime();
			// Loading表示
			//$.appendMaskMsg();

			$.post(
				$.reg.jqgrid ,
				{
					report:			that.name,		// レポート名
					action:			$.id.action_update,	// 実行処理情報
					obj:			id,					// 実行オブジェクト
					MOYSKBN:		szMoyskbn,			// 催し区分
					MOYSSTDT:		szMoysstdt,			// 催しコード（催し開始日）
					MOYSRBAN:		szMoysrban,			// 催し連番
					QASYUKBN:		qasyukbn,			// アンケート種類
					ADOPT:			szAdopt,			// 採用区分
					TENGPCD:		that.TENGPCD,
					SHNDATA:		JSON.stringify(targetDatasShn),	// 更新対象情報
					DATA:			JSON.stringify(targetDatas),	// 更新対象情報
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
			// 店コード
			this.jsonTemp.push({
				id:		$.id_inp.txt_tencd,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_tencd),
				text:	''
			});
			// 店コード
			this.jsonTemp.push({
				id:		$.id_mei.sel_qasyukbn,
				value:	$.getJSONValue(this.jsonHidden, $.id_inp.sel_qasyukbn),
				text:	''
			});
			// 強制
			this.jsonTemp.push({
				id:		'kyosei_flg',
				value:	$('#kyosei_flg').is(':checked') ? $.id.value_on : $.id.value_off,
				text:	''
			});
		},
		setGroup: function(that, reportno, id, isUpdateReport){
			var idx = -1;
			if($('#'+id).is(".easyui-combobox_")){
				$('#'+id).removeClass("easyui-combobox_").addClass("easyui-combobox");
			}

			var tag_options = $('#'+id).attr('data-options');
			if(tag_options){
				tag_options = '\"'+tag_options.replace(/'/g, '').replace(/,/g, '\",\"').replace(/([a-zA-Z0-9]+):/g, '$1\":\"')+'\"';
			}
			var options = tag_options ? JSON.parse('{'+tag_options+'}'): JSON.parse('{}');

			var required = options && options.required;
			var topBlank = !required;
			var panelWidth = options && options.panelWidth ? options.panelWidth : null;
			var panelHeight = options && options.panelHeight ? options.panelHeight :'auto';
			var suffix = that.suffix ? that.suffix : '';
			var changeFunc1 = null;
			if(isUpdateReport){
				changeFunc1 = function(){
					if(idx > 0 && that.queried && $($.id.hiddenChangedIdx).is(':enabled')){
						$($.id.hiddenChangedIdx+suffix).val("1");
					}
				};
			}
			// 当項目を変更した際に、値をセットする項目がある場合の処理（セット先の項目は、HTMLタグとして、for_inp=当項目のID_列名）
			var changeFunc2 = null;
			if($.isFunction(that.changeInputboxFunc)){
				changeFunc2 = function(newValue, obj){
					that.changeInputboxFunc(that, id, newValue, obj);
				};
			}else{
				if($('[for_inp^='+id+'_]').length > 0){
					changeFunc2 = function(newValue){
						var param = [{"value":newValue}];
						$.getsetInputboxRowData(reportno, 'for_inp', id, param, that);
					};
				}
			}
			var szMoyskbn	= $.getJSONObject(this.jsonHidden, $.id_inp.txt_moyskbn).value;		// 催し区分
			var szMoysstdt	= $.getJSONObject(this.jsonHidden, $.id_inp.txt_moysstdt).value;	// 催しコード（催し開始日）
			var szMoysrban	= $.getJSONObject(this.jsonHidden, $.id_inp.txt_moysrban).value;	// 催し連番
			var szTencd		= $.getJSONObject(this.jsonHidden, $.id_inp.txt_tencd).value;	// 催し連番
			var chk_kyosei	= $.getJSONObject(this.jsonHidden, 'kyosei_flg').value;
			that.TENGPCD 	= $.getInputboxData(that.name, $.id.action_init,  $.id_inp.txt_tengpcd, [{MOYSKBN:szMoyskbn,MOYSSTDT:szMoysstdt,MOYSRBAN:szMoysrban,TENCD:szTencd,KYOSEIFLG:chk_kyosei}]);
			$('#'+id).combobox({
				url:$.reg.easy,
				required: required,
				editable: false,
				autoRowHeight:false,
				panelWidth:panelWidth,
				panelHeight:panelHeight,
				valueField:'VALUE',
				textField:'TEXT',
				multiple :false,
				prompt: '',
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
					// 情報設定
					var json = [{
						MOYSKBN:szMoyskbn,MOYSSTDT:szMoysstdt,MOYSRBAN:szMoysrban,TENGPCD:that.TENGPCD,KYOSEIFLG:chk_kyosei
					}];
					if(topBlank){json[0]['TOPBLANK'] = topBlank;}

					param.page		=	reportno;
					param.obj		=	id.replace(suffix, "");
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
					}
					idx = 1;
					// ログ出力
					$.log(that.timeData, id+' init:');
					if(suffix===''){
						if(isUpdateReport){
							// 初期表示処理
							$.initialDisplay(that);
						}else{
//							// 検索ボタン有効化
//							$.setButtonState('#'+$.id.btn_search, true, id);
							// 初期表示検索処理
							$.initialSearch(that);
						}
					}
				},
				onChange:function(newValue, oldValue, obj){
					if(obj===undefined){obj = $(this);}

					if(changeFunc1!==null){ changeFunc1();}
					if(changeFunc2!==null){ changeFunc2(newValue, obj);}
					if(idx > 0){
						$.removeErrState();
					}
				}
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
			}
		},
		setObjectState: function(){	// 軸の選択内容による制御

		},
		extenxDatagridEditorIds:{
			  F17		: "txt_urichgam1"		// チェックボックス（店不採用禁止)
			, F18		: "txt_urichgam2"		// チェックボックス（店不採用禁止)
			, F19		: "txt_urichgam3"		// チェックボックス（店不採用禁止)
		},
		setGrid: function (id, reportNumber){	// グリッドの構築
			var that = this;
			var init = true;
			// ページサイズ定義取得
			var pageList = $.fn.pagination.defaults.pageList;
			var pageSize = $.getJSONValue(this.jsonHidden, $.id.pageSize);
			var cstyler =function(value,row,index){return 'color: red;font-weight: bold;';};
			if (pageSize==="") pageSize=$.fn.pagination.defaults.pageSize;
			pageSize = $.getDefaultPageSize(pageSize, pageList);
			var cformatter =function(value,row,index){return value && value===$.id.value_on?$.id.text_on:$.id.text_off;};

			var pcrmatterNotDisp0 = function(value,row,index){
				if(value && value !==""){
					if(value == '0' || value == 0){
						return null;
					}else{
						return $.getFormat(value, '#,##0');
					}
				}
			};
			var pcrmatter = function(value,row,index){return $.getFormat(value, '#,##0');};

			that.editRowIndex[id] = -1;
			$('#'+id).datagrid({
				nowrap: true,
				border: true,
				striped: true,
				collapsible:false,
				remoteSort: true,
				rownumbers:true,
				fit:true,
				view:scrollview,
				singleSelect:true,
				checkOnSelect:false,
				selectOnCheck:false,
				frozenColumns:[[
				    {field:'F1',	title:'商品コード',						width: 80,	halign:'center',align:'left'},
				]],
				columns:[[
					{field:'F2',	title:'商品名',							width: 400,	halign:'center',align:'left'},
					{field:'F3',	title:'チラシ<br>未掲載',				width: 90,	halign:'center',align:'center',	formatter:cformatter,	styler:cstyler},
					{field:'F4',	title:'1日<br>スラ',					width: 40,	halign:'center',align:'center',	formatter:cformatter,	styler:cstyler},
					{field:'F5',	title:'販売期間',						width: 180,	halign:'center',align:'left'},
					{field:'F6',	title:'原価',							width: 90,	halign:'center',align:'right',
						formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0.00');}},
					{field:'F7',	title:'単',	    						width: 60,	halign:'center',align:'center'},
					{field:'F8',	title:'A総売価',						width: 60,	halign:'center',align:'right'},
					{field:'F9',	title:'A総売価<br>（バンドル１）',		width: 100,	halign:'center',align:'right'},
					{field:'F10',	title:'A総売価<br>（バンドル２）',		width: 100,	halign:'center',align:'right'},
					{field:'F11',	title:'B総売価',						width: 60,	halign:'center',align:'right'},
					{field:'F12',	title:'B総売価<br>（バンドル１）',		width: 100,	halign:'center',align:'right'},
					{field:'F13',	title:'B総売価<br>（バンドル２）',		width: 100,	halign:'center',align:'right'},
					{field:'F14',	title:'C総売価',		        		width: 60,	halign:'center',align:'right'},
					{field:'F15',	title:'C総売価<br>（バンドル１）',		width: 100,	halign:'center',align:'right'},
					{field:'F16',	title:'C総売価<br>（バンドル２）',		width: 100,	halign:'center',align:'right'},
					{field:'F17',	title:'希望総売価<br>（1売り）',		width: 100,	halign:'center',align:'right',editor:{type:'numberbox'},formatter:pcrmatterNotDisp0},
					{field:'F18',	title:'希望総売価<br>（バンドル１）',	width: 100,	halign:'center',align:'right',editor:{type:'numberbox'},formatter:pcrmatterNotDisp0},
					{field:'F19',	title:'希望総売価<br>（バンドル２）',	width: 100,	halign:'center',align:'right',editor:{type:'numberbox'},formatter:pcrmatterNotDisp0},
				]],
				fitColumns:false,	// 指定カラム幅を適用する場合、false 指定。
				rowStyler:function(index, row){
				},
				onSortColumn:function(sort, order){
					if (that.jsonString.length===0) return false;
					// カラム並び替え
					that.success(that.name, true);
				},
				onBeforeEdit:function(index,row){

						var adds = that.editRowIndex[that.focusGridId] > index ? -1:1;
						that.editRowIndex[that.focusGridId] = -9;	// 次の項目に移動するために未編集状態と違うインデックス設定
					if(that.TPNG2FLG=="1"){
						var evt = $.Event('keydown');
						evt.keyCode = 13;
						evt.shiftKey = adds === -1;
						$('#'+id).parents('.datagrid').eq(0).trigger(evt);
						return false;
					}
				},
				onBeforeLoad:function(param){
					param.report = that.name;
				},
				onLoadSuccess:function(data){
					// 各グリッドの値を保持する
					var gridData = that.getGridData('#'+id);
					that.setGridData(gridData, '#'+id);

					// チェックボックスの設定
					$.initCheckboxCss($('#'+id).datagrid('getPanel').find('.datagrid-body'));
				},
				onClickCell:function(rowIndex, field, value){
					// 列名保持
					that.columnName = field;
				},
				onClickRow: function(index,field){$.clickEditableDatagridCell(that,id, index)},
				onBeforeEdit:function(index,row){
					var EditCol = 3;	// 入力可能な項目数

					if(row.F25=="1"
						||( row.F11.length==0 && row.F14.length==0)){
						$('#'+id).datagrid('getColumnOption', 'F17').editor = false;
						$('#'+id).datagrid('getColumnOption', 'F17').formatter = pcrmatterNotDisp0
						EditCol -= 1;

					}else{
						$('#'+id).datagrid('getColumnOption', 'F17').editor = {type:'numberbox'}
						$('#'+id).datagrid('getColumnOption', 'F17').formatter = pcrmatter
					}

					if((row.F20==null || row.F20=="")
							|| row.F25=="1"
							|| row.F26=="1"
							||( row.F11.length==0 && row.F14.length==0)){
						$('#'+id).datagrid('getColumnOption', 'F18').editor = false;
						$('#'+id).datagrid('getColumnOption', 'F18').formatter = pcrmatterNotDisp0
						EditCol -= 1;

					}else{
						$('#'+id).datagrid('getColumnOption', 'F18').editor = {type:'numberbox'}
						$('#'+id).datagrid('getColumnOption', 'F18').formatter = pcrmatter
					}

					if((row.F21==null || row.F21=="")
							|| row.F25=="1"
							|| row.F26=="1"
							||( row.F11.length==0 && row.F14.length==0)){
						$('#'+id).datagrid('getColumnOption', 'F19').editor = false;
						$('#'+id).datagrid('getColumnOption', 'F19').formatter = pcrmatterNotDisp0
						EditCol -= 1;

					}else{
						$('#'+id).datagrid('getColumnOption', 'F19').editor = {type:'numberbox'}
						$('#'+id).datagrid('getColumnOption', 'F19').formatter = pcrmatter
					}

					if(EditCol == 0){
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
					$.beginEditDatagridRow(that,id, index, row)
				},
				onEndEdit: function(index,row,changes){
					$.endEditDatagridRow(that, id, index, row)
					row.CHK_SEL = $.id.value_off;
				},
				onAfterEdit: function(index,row,changes){
					// チェックボックスの再追加（EndEdit時に削除されるため）
					$.afterEditAddCheckbox($('#'+id).datagrid('getPanel').find("[datagrid-row-index='"+index+"']"));
				},
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
		//ここからマージ　確認
		getGridData: function (target){
			var that = this;

			var data = {};
			var targetRows= [];

			// 商品一覧
			if(target===undefined || target===$.id.gridholder){
				var rows	 = $($.id.gridholder).datagrid('getRows');			// 商品一覧

				for (var i=0; i<rows.length; i++){
					var rowDate = {
							F1	 : rows[i]["F17"],
							F2	 : rows[i]["F18"],
							F3	 : rows[i]["F19"],
							F4	 : rows[i]["F8"],
							F5	 : rows[i]["F9"],
							F6	 : rows[i]["F10"],
							F8	 : rows[i]["F22"],
							F9	 : rows[i]["F23"],
							F10	 : rows[i]["F24"],
							F11	 : rows[i]["F20"],
							F12	 : rows[i]["F21"],
							F13	 : rows[i]["F26"],
							F14	 : rows[i]["F27"],
							F15	 : rows[i]["F28"],
							F16	 : rows[i]["F11"],
							F17	 : rows[i]["F14"],
							F18	 : rows[i]["F25"],
							F19	 : i,
							F20	 : rows[i]["F31"],
							F21	 : rows[i]["F32"],
							F22	 : rows[i]["F33"]
					};
					targetRows.push(rowDate);
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
					if( newrows[i]['F1'] != oldrows[i]['F1'] || newrows[i]['F2'] != oldrows[i]['F2'] || newrows[i]['F3'] != oldrows[i]['F3']){
							var rowDate = {
									F1	 : newrows[i]["F1"],
									F2	 : newrows[i]["F2"],
									F3	 : newrows[i]["F3"],
									F4	 : newrows[i]["F4"],
									F5	 : newrows[i]["F5"],
									F6	 : newrows[i]["F6"],
									F8	 : newrows[i]["F8"],
									F9	 : newrows[i]["F9"],
									F10	 : newrows[i]["F10"],
									F11	 : newrows[i]["F11"],
									F12	 : newrows[i]["F12"],
									F13	 : newrows[i]["F13"],
									F14	 : newrows[i]["F14"],
									F15	 : newrows[i]["F15"],
									F16	 : newrows[i]["F16"],
									F17	 : newrows[i]["F17"],
									F18	 : newrows[i]["F18"],
									F19	 : newrows[i]["F19"],
									F20	 : newrows[i]["F20"],
									F21	 : newrows[i]["F21"],
									F22	 : newrows[i]["F22"],
							};
						if(rowDate){
							targetRows.push(rowDate);
						}
					}
				}
			}
			return targetRows;
		},
		setGridData: function (data, target){
			var that = this;

			// 商品一覧
			if(target===undefined || target===$.id.gridholder){
				that.grd_saiyou_data =  data[$.id.gridholder];
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
			var targetDatas = [{}];
			$('#'+that.focusRootId).find('[col^=F]').each(function(){
				var col = $(this).attr('col');
				targetDatas[0][col] = $.getInputboxValue($(this));
			});
			if(targetDatas[0]["F9"] ==1){
				$($.id.hiddenChangedIdx).val("");
			}
			// JSON Object Clone ()
			var sendJSON = JSON.parse( JSON.stringify( that.jsonHidden ) );
			$.setJSONObject(sendJSON, 'sendBtnid', btnId, $('#'+btnId).attr("title"));					// 実行ボタン情報保持
			$.setJSONObject(sendJSON, 'reportYobi1', that.reportYobiInfo(), that.reportYobiInfo());		// 参照情報保持
			$.setJSONObject(sendJSON, 'callpage', that.name, that.name);								// 呼出し元レポート情報

			// 戻る実行時用に現在の画面情報を保持する
			var states = $.getBackBaseJSON(that);
			// 各種グリッド情報を設定
			var newrepinfos = $.getBackJSON(that, states);
			$.setJSONObject(sendJSON, 'repinfo', newrepinfos, '');// 呼出し元レポート情報

			// 実行ボタン別処理
			switch (btnId) {
			case $.id.btn_new:
			case $.id.btn_new+2:
				var row = $($.id.gridholder).datagrid("getSelected");
				// 転送先情報
				index = 3;
				childurl = href[index];
				sendMode = 1;
				// オブジェクト作成
				$.setJSONObject(sendJSON, $.id_inp.txt_tengpcd,row.F1, row.F1);
				$.setJSONObject(sendJSON, $.id_inp.txt_moyskbn,row.F18, row.F18);
				$.setJSONObject(sendJSON, $.id_inp.txt_moysstdt,row.F19, row.F19);
				$.setJSONObject(sendJSON, $.id_inp.txt_moysrban,row.F20, row.F20);
				break;
			case $.id.btn_sel_change:
				// 選択行
				var row = $($.id.gridholder).datagrid("getSelected");
				if(!row){
					$.showMessage('E00008');
					return false;
				}
				// 転送先情報
				index = 4;
				childurl = href[index];
				sendMode = 1;
				// オブジェクト作成
				$.setJSONObject(sendJSON, $.id_inp.txt_tencd,row.F3, row.F3);
				$.setJSONObject(sendJSON, 'kyosei_flg',row.F21, row.F21);
				break;
			case $.id.btn_copy:
				// 転送先情報
				index = 6;
				childurl = href[index];
				sendMode = 1;
				// オブジェクト作成
				$.setJSONObject(sendJSON, $.id_inp.txt_tengpcd, row.F15, row.F15);	// 店グループ
				break;
			case $.id.btn_back:
			case $.id.btn_cancel:
			case $.id.btn_upd:
				// 転送先情報
				index = 4;
				sendMode = 1;
				childurl = href[index];

				// 本画面ではsendMode=2での移動を想定していない為、
				// 前回選択行の情報をrepinfoから取り出し設定を送信情報に設定する。
				var targetId = "Out_ReportTG015";
				newrepinfos.some(function(v, i){
					if (v.id==targetId){
						var TMPCOND =newrepinfos[i].value.TMPCOND;
						var innerTargetId = "scrollToIndex_#gridholder"
							TMPCOND.some(function(w, j){
								if (w.id==innerTargetId){
									$.setJSONObject(sendJSON, innerTargetId, w.value, w.text);
								}
							});
					}
				});

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
		}
	} });
})(jQuery);