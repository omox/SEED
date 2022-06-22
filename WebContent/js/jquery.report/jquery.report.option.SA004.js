/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Out_ReportSA004',			// （必須）レポートオプションの確認
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
		dedefaultObjNum:	8,	// 初期化オブジェクト数
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
		SAIYOUKBN:"0",
		initialize: function (reportno){	// （必須）初期化
			var that = this;
			// 引き継ぎ情報
			this.jsonHidden = $.getTargetValue();
			// 画面の初回基本設定
			this.setInitObjectState();
			var reportYobi1 = $('#reportYobi1').val();
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
			// 部門
			$.setMeisyoCombo(that, reportno, $.id.SelBumon, that);
			that.setGroup(that, reportno, $.id.sel_tenkn, that);
			// ラジオボタン系
			$.setRadioInit2(that.jsonHidden, $.id.rad_adopt, that);
			$.setRadioInit2(that.jsonHidden, $.id.rad_auriaselkbn, that);
			$.setCheckboxInit2(that.jsonHidden, $.id.mbsy_flg, that);
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

			$.initReportInfo("SA004", "特売アンケート状況　売価・商品選択", "一覧");

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

			// 検索ボタン押下時に部門コードが未選択の場合はエラー
			if (that.pushBtnId===$.id.btn_search) {
				if ($.getInputboxValue($('#'+$.id.SelBumon))==='-1') {
					$.showMessage('E20037');
					return false;
				}
			}

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
			var szSelBumon	= $.getJSONObject(this.jsonString, $.id.SelBumon).value;				// 部門

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
					BMN:			szSelBumon,		// 部門
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
					var count 	= that.getInputboxData2(that.name, $.id.action_init,  "BAIKACOUNT", [{MOYSKBN:szMoyskbn,MOYSSTDT:szMoysstdt,MOYSRBAN:szMoysrban,TENGPCD:that.TENGPCD}]);

					if(count[0]["VALUE1"]==0){
						that.setInputBoxDisable2($("#baika2"));
					}
					if(count[0]["VALUE2"]==0){
						that.setInputBoxDisable2($("#baika3"));
					}
					var opts = JSON.parse(json).opts
					if(opts && opts.rows_){
						// 基本データ表示
						that.setData(opts.rows_, opts);
					}
					if(opts.rows_[0]["F9"]==1){
						that.setInputBoxDisable2($("#baika1"));
						that.setInputBoxDisable2($("#baika2"));
						that.setInputBoxDisable2($("#baika3"));
					}
					if(opts.rows_[0]["F10"]==1){
						$.setInputBoxDisable($('[name='+$.id.rad_adopt+"]"));
					}
					that.SAIYOUKBN = opts.rows_[0]["F10"];
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
					// ログ出力
					$.log(that.timeData, 'loaded:');
				}
			);
		},
		getInputboxData2: function(reportno, action, id, param){
			var value =[{}];
			// 情報設定
			$.ajax({
				url: $.reg.easy,
				type: 'POST',
				async: false,
				data: {
					page	: reportno,
					obj		: id,
					sel		: (new Date()).getTime(),
					target	: id,
					action	: action,
					json	: JSON.stringify(param)
				},
				success: function(json){
					if(json !=="" &&  JSON.parse(json).rows.length > 0){
						value = [{
							VALUE1:JSON.parse(json).rows[0].VALUE1,VALUE2:JSON.parse(json).rows[0].VALUE2
						}];
					}
				}
			});
			return value;
		},
		setInputBoxDisable2:function(target, isTemporary){
			if(isTemporary===undefined){ isTemporary = false;}

			if(target.is(":radio")){
				$('input[id="'+target.attr('id')+'"]').attr('disabled', 'disabled');
			}
			target.attr('readonly', 'readonly');
			target.attr('disabled', 'disabled');
			if(!isTemporary){
				target.attr('tabindex', -1);
			}
			return target;
		},
		updValidation: function (){	// （必須）批准
			var that = this;

			var row = $($.id.gridholder).datagrid("getSelected");
			var rowIndex = $($.id.gridholder).datagrid("getRowIndex", row);
			$($.id.gridholder).datagrid('endEdit',rowIndex);

			var rt = $($.id.toolbarform).form('validate');
			return rt;
		},
		updSuccess: function(id){	// validation OK時 の update処理
			var that = this;
			var szMoyskbn		= $.getJSONObject(this.jsonString, $.id_inp.txt_moyskbn).value;		// 催し区分
			var szMoysstdt		= $.getJSONObject(this.jsonString, $.id_inp.txt_moysstdt).value;	// 催しコード（催し開始日）
			var szMoysrban		= $.getJSONObject(this.jsonString, $.id_inp.txt_moysrban).value;	// 催し連番
			var szTencd			= $.getJSONObject(this.jsonString, $.id_inp.txt_tencd).value;		// 催し連番
			var chk_kyosei		= $("input[id=kyosei_flg]:checked").val();							// メーカー名無し
			var qasyukbn		= $.getJSONValue(that.jsonHidden, $.id_inp.sel_qasyukbn);			//アンケート種類
			var szSelBumon		= $.getJSONObject(this.jsonString, $.id.SelBumon).value;				// 部門
			var szAdopt		= $("input[name="+$.id.rad_adopt+"]:checked").val();		// マスター区分
			var szAuriaselkbn1	= $("input[name="+$.id.rad_auriaselkbn+"]:checked").val();		// データ指定区分
			var szSaiyou = that.SAIYOUKBN;//採用区分
			// 変更行情報取得
			var targetDatas = [{}];
			$('#'+that.focusRootId).find('[col^=F]').each(function(){
				var col = $(this).attr('col');
				targetDatas[0][col] = $.getInputboxValue($(this));
			});
//			that.changeReport(that.name, 'btn_upd');
//			return false;
			// 商品一覧のデータを取得
			var targetDatasShn = that.getMergeGridDate($.id.gridholder);

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
					//TENCD:			szTencd,		// 店コード
					QASYUKBN:		qasyukbn,			// アンケート種類
					BMN:			szSelBumon,			// 部門
					ADOPT:			szAdopt,			// 採用区分
					AURIASELKBN:	szAuriaselkbn1,		// 総売価
					SAIYOUKBN:		szSaiyou,
					TENGPCD:		that.TENGPCD,
//					IDX:			$($.id.hiddenChangedIdx).val(),	// 更新対象Index
					SHNDATA:			JSON.stringify(targetDatasShn),	// 更新対象情報
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
			// 部門
			this.jsonTemp.push({
				id:		$.id.SelBumon,
				value:	$('#'+$.id.SelBumon).combobox('getValues'),
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
			 F1		: "mbsy_flg"		// チェックボックス（店不採用禁止)
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
				frozenColumns:[[]],
				columns:[[
				    {field:'F1',	title:'採用<br>区分',					styler:cstyler, formatter:cformatter,editor:{type:'checkbox'},width: 40,halign:'center',align:'center'},
					{field:'F2',	title:'商品コード',						width: 80,	halign:'center',align:'left'},
					{field:'F3',	title:'商品名',							width: 400,	halign:'center',align:'left'},
					{field:'F4',	title:'チラシ<br>未掲載',				width: 50,	halign:'center',align:'center',	formatter:cformatter,	styler:cstyler},
					{field:'F5',	title:'1日<br>スラ',					width: 40,	halign:'center',align:'center',	formatter:cformatter,	styler:cstyler},
					{field:'F6',	title:'販売期間',						width: 180,	halign:'center',align:'left'},
					{field:'F7',	title:'原価',							width: 90,	halign:'center',align:'right',
						formatter:function(value, rowData, rowIndex) {return $.getFormat(value, '#,##0.00');}},
					{field:'F8',	title:'単',	    						width: 60,	halign:'center',align:'center'},
					{field:'F9',	title:'A総売価',						width: 60,	halign:'center',align:'right'},
					{field:'F10',	title:'A総売価<br>（バンドル１）',		width: 100,	halign:'center',align:'right'},
					{field:'F11',	title:'A総売価<br>（バンドル２）',		width: 100,	halign:'center',align:'right'},
					{field:'F12',	title:'B総売価',						width: 60,	halign:'center',align:'right'},
					{field:'F13',	title:'B総売価<br>（バンドル１）',		width: 100,	halign:'center',align:'right'},
					{field:'F14',	title:'B総売価<br>（バンドル２）',		width: 100,	halign:'center',align:'right'},
					{field:'F15',	title:'C総売価',						width: 60,	halign:'center',align:'right'},
					{field:'F16',	title:'C総売価<br>（バンドル１）',		width: 100,	halign:'center',align:'right'},
					{field:'F17',	title:'C総売価<br>（バンドル２）',		width: 100,	halign:'center',align:'right'}
				]],
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
				onSelect:function(index,row){
					var col1 = $('#'+id).datagrid('getColumnOption', 'F1');

					if(that.SAIYOUKBN=="0"){
						col1.editor = {type:'checkbox'}
					}else{
						col1.editor = false
					}
				},
				onClickRow: function(index,field){$.clickEditableDatagridCell(that,id, index)},
				onBeginEdit:function(index,row){$.beginEditDatagridRow(that,id, index, row)},
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
				var rows	 = $('#'+'gridholder').datagrid('getRows');			// 商品一覧

				for (var i=0; i<rows.length; i++){
					var rowDate = {
							F1	 : rows[i]["F1"],
							F7	 : rows[i]["F18"],
							F8	 : rows[i]["F19"],
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
					if( newrows[i]['F1'] != oldrows[i]['F1']){
							var rowDate = {
									F1	 : newrows[i]["F1"],
									F7	 : newrows[i]["F7"],
									F8	 : newrows[i]["F8"],
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
			case $.id.btn_upd:
				// 転送先情報
				index = 4;
				sendMode = 1;
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
		}
	} });
})(jQuery);