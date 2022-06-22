/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
			name:		'Out_ReportTG015',			// （必須）レポートオプションの確認
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
			dedefaultObjNum:	9,	// 初期化オブジェクト数
			initObjNum:	-1,
			initedObject: [],
			maxMergeCell: 1,
			onChangeFlag : false,
			onChangeFlag2 : false,
			columnName:'',	// OnClickRowの列名
			queried : false,
			initializes : true,
			editRowIndex:{},						// グリッド編集行保持
			onChangeReport: false,
			reportYobiInfo: function(){			// （必須）レポートメニューに登録された予備情報＝帳票をどういった表示にしたいかの情報
				return $('#reportYobi1').val();
			},
			returnPageInfo: function(){			// （必須）レポートメニューに登録された予備情報＝帳票をどういった表示にしたいかの情報
				return $('#returnPageInfo1').val();
			},
			pushBtnId: "",						// （必須）実行ボタンID情報(検索系で利用)
			focusRootId:"cc",					// （キー移動時必須）キー移動イベントのルートとなるパネルのID
			focusParentId:"",					// （キー移動時必須）現在フォーカスがあたっている項目の親となるパネルのID
			focusGridId:"",						// （キー移動時必須）現在フォーカスがあたっているDataGridのID
			MOYOKBN:"",						// 催し区分
			MOYODT:"",						// 催し開始日
			MOYOREN:"",						// 催し連番
			NUMBER:"",						// index
			TG015Flag : false,
			grd_tenpo_data:[],				// グリッド情報:店舗一覧
			initialize: function (reportno){	// （必須）初期化
				var that = this;
				// 引き継ぎ情報
				this.jsonHidden = $.getTargetValue();
				// 画面の初回基本設定
				this.setInitObjectState();
				// 初期検索条件設定
				this.jsonInit = $.getInitValue();
				var reportYobi1 = $('#reportYobi1').val();
				// データ表示エリア初期化
				that.MOYOKBN = $.getJSONValue(this.jsonHidden, $.id_inp.txt_moyskbn);
				that.MOYODT = $.getJSONValue(this.jsonHidden, $.id_inp.txt_moysstdt);
				that.MOYOREN = $.getJSONValue(this.jsonHidden, $.id_inp.txt_moysrban);
				// 初期化するオブジェクト数設定
				this.initObjNum = this.dedefaultObjNum;

				var isUpdateReport = true;

				// 入力テキストボックス系
				var inputbox = Object.getOwnPropertyNames($.id_inp);
				for ( var sel in inputbox ) {
					if($('#'+$.id_inp[inputbox[sel]]).length > 0){
						$.setInputbox(that, reportno, $.id_inp[inputbox[sel]], isUpdateReport);
					}
				}
				$.setInputbox(that, reportno, $.id_inp.txt_mbansflg, isUpdateReport);
				$.setCheckboxInit2(that.jsonHidden, 'kyosei_flg', that);
				$.setCheckboxInit2(that.jsonHidden, $.id.mbsy_flg, that);
				$.setCheckboxInit2(that.jsonHidden, $.id.hbstrt_flg+1, that);
				$.setCheckboxInit2(that.jsonHidden, $.id.hbstrt_flg+2, that);
				var sendBtnid = $.getJSONValue(that.jsonHidden, "sendBtnid");
				if(!sendBtnid){
					sendBtnid = $('#sendBtnid').val();
				}
				that.sendBtnid = sendBtnid;
				if (that.sendBtnid!==$.id.btn_sel_change+2 && that.sendBtnid!==$.id.btn_sel_change+3) {
					that.setGrid($.id.gridholder, reportno);
				}else{
					// 編集エディターの機能拡張（非表示で参照用のオブジェクトが準備してある前提、参照オブジェクトはグリッドのフィールド名を元に判断）
					$.extendDatagridEditor(that);
					// データ表示エリア初期化
					that.setGrid('gridholder', reportno);
				}

				// Load処理回避
				//$.tryChangeURL(null);

				// 初期化終了
				this.initializes =! this.initializes;

				var newval = $('#'+$.id_inp.txt_tencd).numberbox('getValue');
				$('#'+$.id_inp.txt_tencd).numberbox('reset').numberbox('setValue', newval);

				if (that.sendBtnid!==$.id.btn_sel_change+2 && that.sendBtnid!==$.id.btn_sel_change+3) {
					$('#cc').layout('panel', 'north').panel('resize', {height:65});
				}
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

				// 当帳票を「参照」で開いた場合
				if(that.sendBtnid ==$.id.btn_new ){
					$.initReportInfo("TG015", "特売アンケート状況 グループ・催し別", "一覧");
					$("#kaitouName").hide();
					$("#miseban").hide();
					$('#'+$.id.btn_upd).linkbutton('disable');
					$('#'+$.id.btn_upd).attr('disabled', 'disabled').hide();
					$('#'+$.id.btn_cancel).linkbutton('disable');
					$('#'+$.id.btn_cancel).attr('disabled', 'disabled').hide();
				}else if(that.sendBtnid ==$.id.btn_sel_change || that.sendBtnid ==$.id.btn_back || that.sendBtnid ==$.id.btn_cancel || that.sendBtnid ==$.id.btn_upd){
					// 初期表示処理
					that.onChangeReport = true;
					$.initReportInfo("TG015", "特売アンケート状況 グループ・催し別", "一覧");
					$("#kaitouName").hide();
					$("#miseban").hide();
					$('#'+$.id.btn_upd).linkbutton('disable');
					$('#'+$.id.btn_upd).attr('disabled', 'disabled').hide();
					$('#'+$.id.btn_cancel).linkbutton('disable');
					$('#'+$.id.btn_cancel).attr('disabled', 'disabled').hide();
				}else if(that.sendBtnid ==$.id.btn_sel_change+2 ){
					$.initReportInfo("SA006", "特売アンケート状況　各店参加", "更新");
					that.TG015Flag = true;
					$("#leaderTen").hide();
					$('#'+$.id.btn_sel_change).linkbutton('disable');
					$('#'+$.id.btn_sel_change).attr('disabled', 'disabled').hide();
				}else if(that.sendBtnid ==$.id.btn_sel_change+3 ){
					// 初期表示処理
					that.onChangeReport = true;
					$.initReportInfo("SA006", "特売アンケート状況　各店参加", "更新");
					that.TG015Flag = true;
					$("#leaderTen").hide();
					$('#'+$.id.btn_sel_change).linkbutton('disable');
					$('#'+$.id.btn_sel_change).attr('disabled', 'disabled').hide();
				}
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
				// グリッド初期化
				this.success(this.name, false);
			},
			validation: function (){	// （必須）批准
				var that = this;
				// EasyUI のフォームメソッド 'validate' 実施
				var rt = $($.id.toolbarform).form('validate');
				// 入力エラーなしの場合に検索条件を格納
				that.jsonString = that.jsonTemp.slice(0);
				// 入力チェック用の配列をクリア
				that.jsonTemp = [];
				return rt;
			},
			success: function(reportno, sortable){	// （必須）正処理
				if (sortable) sortable=1; else sortable=0;
				var that = this;

				// 検索実行
				var txt_qayyyymm	= $.getInputboxValue($('#'+$.id_inp.txt_qayyyymm));		// 月度（左）
				var txt_qaend		= $.getInputboxValue($('#'+$.id_inp.txt_qaend));		// 月度（右）
				var txt_tencd		= $.getInputboxValue($('#'+$.id_inp.txt_tencd));		// 店コード
				var txt_tenkn		= $.getInputboxValue($('#'+$.id_inp.txt_tenkn));		// 店舗名（漢字）
				var txt_mbansflg	= $.getInputboxValue($('#'+$.id_inp.txt_mbansflg));		// 店舗名（漢字）
				var chk_kyosei		= $("input[id=kyosei_flg]:checked").val();				// 強制フラグ

				// 処理時間計測用
				that.timeData = (new Date()).getTime();
				$($.id.gridholder).datagrid('loading');
				$.appendMaskMsg();
				// 処理時間計測用
				that.timeData = (new Date()).getTime();
				$($.id.gridholder).datagrid('loading');

				// grid.options 取得
				var options = $($.id.gridholder).datagrid('options');
				that.sortName	= options.sortName;
				that.sortOrder	= options.sortOrder;
				var sendBtnid = $.getJSONValue(that.jsonHidden, "sendBtnid");
				$.post(
						$.reg.jqgrid ,
						{
							report			:that.name,					// レポート名
							QAYYYYMM		:txt_qayyyymm,
							QAEND			:txt_qaend,
							TENCD			:txt_tencd,
							CHK_KYOSEI		:chk_kyosei,
							TENKN			:txt_tenkn,
							MBANSFLG		:txt_mbansflg,
							SENDBTNID		:sendBtnid,
							PUSHBTNID:		that.pushBtnId,
							t				:(new Date()).getTime(),
							sortable		:sortable,
							sortName		:that.sortName,
							sortOrder		:that.sortOrder,
							rows			:0							// 表示可能レコード数
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

							var opts = JSON.parse(json).opts
							if(opts && opts.rows_){
								// 基本データ表示
								that.setData(opts.rows_, opts);
							}

							that.queried = true;
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
			updValidation: function (){	// （必須）批准
				var that = this;
				var row = $($.id.gridholder).datagrid("getSelected");
				var rowIndex = $($.id.gridholder).datagrid("getRowIndex", row);
				$($.id.gridholder).datagrid('endEdit',rowIndex);

				var rt = $($.id.toolbarform).form('validate');
				// 「1日遅開始」「通常開始」が両方とも１の場合エラー

				var targetDatasShn = that.getMergeGridDate($.id.gridholder);

				for (var i=0; i<targetDatasShn.length; i++){
					var count = 0;
					var inpdata = targetDatasShn[i];
					var val1 = inpdata["F5"];
					var val2 = inpdata["F6"];
					var val3 = inpdata["F7"];
					var val4 = inpdata["F8"];

					if(val1=="1"){
						count ++;
					}
					if(val2=="1"){
						count ++;
					}
					if(count!=1){
						$.showMessage('E20568');
						return false;
					}
					if(val3!=0&&val4!=1){
						$.showMessage('EX1078');
						return false;
					}
				}
				return rt;
			},
			updSuccess: function(id){	// validation OK時 の update処理
				var that = this;

				// 変更行情報取得
				var targetDatas = [{}];
				$('#'+that.focusRootId).find('[col^=F]').each(function(){
					var col = $(this).attr('col');
					targetDatas[0][col] = $.getInputboxValue($(this));
				});

				// 商品一覧のデータを取得
				var targetDatasShn = that.getMergeGridDate($.id.gridholder);
				var chk_kyosei		= $("input[id=kyosei_flg]:checked").val();
				var chk_mbsyflg		= $("input[id="+$.id.mbsy_flg+"]:checked").val();
				var txt_tencd		= $.getInputboxValue($('#'+$.id_inp.txt_tencd));;		// 店コード
				// 処理時間計測用
				that.timeData = (new Date()).getTime();
				// Loading表示
				//$.appendMaskMsg();

				$.post(
					$.reg.jqgrid ,
					{
						report:			that.name,		// レポート名
						action:			$.id.action_update,	// 実行処理情報
						obj:			id,								// 実行オブジェクト
						SENDBTNID:		that.sendBtnid,
						KYOUSEI:		chk_kyosei,
						TENCD:			txt_tencd,
						MBSYFLG:		chk_mbsyflg,
//						IDX:			$($.id.hiddenChangedIdx).val(),	// 更新対象Index
						DATA:			JSON.stringify(targetDatasShn),	// 更新対象情報
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
				// 月度(左)
				this.jsonTemp.push({
					id:		$.id_inp.txt_qayyyymm,
					value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_qayyyymm),
					text:	''
				});
				// 月度（右）
				this.jsonTemp.push({
					id:		$.id_inp.txt_qaend,
					value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_qaend),
					text:	''
				});
				// リーダー店コード
				this.jsonTemp.push({
					id:		$.id_inp.txt_tencd,
					value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_tencd),
					text:	''
				});
				// 催し区分
				this.jsonTemp.push({
					id:		$.id_inp.txt_tencd,
					value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_moyskbn),
					text:	''
				});
				// 催し開始日
				this.jsonTemp.push({
					id:		$.id_inp.txt_tencd,
					value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_moysstdt),
					text:	''
				});
				// 催し連番
				this.jsonTemp.push({
					id:		$.id_inp.txt_tencd,
					value:	$.getJSONValue(this.jsonHidden, $.id_inp.txt_moysrban),
					text:	''
				});

				// 強制
				this.jsonTemp.push({
					id:		'kyosei_flg',
					value:	$('#kyosei_flg').is(':checked') ? $.id.value_on : $.id.value_off,
							text:	''
				});
			},
			extenxDatagridEditorIds:{
				 F1		: "mbsy_flg"		// チェックボックス（店不採用禁止)
				,F3		: "hbstrt_flg1"		// チェックボックス（店不採用禁止)
				,F4		: "hbstrt_flg2"		// チェックボックス（店不採用禁止)
			},
			setGrid: function (id, reportNumber){	// グリッドの構築
				var that = this;
				var init = true;
				// ページサイズ定義取得
				if(that.TG015Flag == false){
					var pageList = $.fn.pagination.defaults.pageList;
					var pageSize = $.getJSONValue(this.jsonHidden, $.id.pageSize);
					if (pageSize==="") pageSize=$.fn.pagination.defaults.pageSize;
					pageSize = $.getDefaultPageSize(pageSize, pageList);
					var cstyler=function(value,row,index){return 'color: red;font-weight: bold;';};
					var cformatter =function(value,row,index){return value && value===$.id.value_on?$.id.text_on:$.id.text_off;};
					$(id).datagrid({
						nowrap: true,
						border: true,
						striped: true,
						collapsible:false,
						remoteSort: true,
						rownumbers:true,
						fit:true,
						view:scrollview,
						pageSize:pageSize,
						pageList:pageList,
						frozenColumns:[[]],
						columns:[[
						          {title:'　', colspan:3},
						          {title:'売価回答', colspan:8},
						          ],[
						             {field:'F1',	title:'催しコード',				width: 90,halign:'center',align:'left'},
						             {field:'F2',	title:'催し名称',				width: 300,halign:'center',align:'left'},
						             {field:'F3',	title:'参加／不参加',			width: 100,halign:'center',align:'center'},
						             {field:'F4',	title:'売価選択(一括)',			width: 100,halign:'center',align:'center',formatter:cformatter,	styler:cstyler},
						             {field:'F5',	title:'',						width: 40,halign:'center',align:'center'},
						             {field:'F6',	title:'売価選択(商品別)',		width: 130,halign:'center',align:'center',formatter:cformatter,	styler:cstyler},
						             {field:'F7',	title:'',						width: 40,halign:'center',align:'center'},
						             {field:'F8',	title:'売価差替',				width: 100,halign:'center',align:'center',formatter:cformatter,	styler:cstyler},
						             {field:'F9',	title:'',						width: 40,halign:'center',align:'center'},
						             {field:'F10',	title:'売価商品選択',			width: 100,halign:'center',align:'center',formatter:cformatter,	styler:cstyler},
						             {field:'F11',	title:'',						width: 40,halign:'center',align:'center'},
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
						            	 // 検索後、初回のみ処理
						            	 if (that.queried){
						            		 that.queried = false;	// 検索後、初回のみ処理
						            		 // 状態保存
						            		 $.saveState(reportNumber, that.getJSONString(), id);
						            		 // 警告
						            		 $.showWarningMessage(data);
						            	 }

						            	 // 前回選択情報をGridに反映
						            	 var getRowIndex = $.getJSONValue(that.jsonHidden, "scrollToIndex_"+id);
						            	 if(getRowIndex !== ""){
						            		 $(id).datagrid('scrollTo', {
						            			 index: getRowIndex,
						            			 callback: function(index){
						            				 $(id).datagrid('selectRow', index);
						            			 }
						            		 });

						            		 // 検索可能な一覧画面では、検索時に前回選択情報が適用されてしまう為、
						            		 // 一度適用したら要素を削除する。
						            		 var targetName = "scrollToIndex_"+id;
						            		 that.jsonHidden.some(function(v, i){
						            			 if (v.id==targetName) that.jsonHidden.splice(i,1);
						            		 });
						            	 }
						             },
						             onClickCell:function(rowIndex, field, value){
						            	 // 列名保持
						            	 that.columnName = field;
						             },
						             onClickRow:function(rowIndex, rowData){
						            	 // ドリルリンク
						            	 //that.changeReport($.id.column_class, that.columnName, rowData);
						             },
						             autoRowHeight:false,
						             pagination:false,
						             pagePosition:'bottom',
						             singleSelect:true
					});
				}else{
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
						singleSelect:true,
						checkOnSelect:false,
						selectOnCheck:false,
						frozenColumns:[[]],
						columns:[[
						          {field:'F1',	title:'採用区分',				styler:cstyler, formatter:cformatter,editor:{type:'checkbox'},width: 60,halign:'center',align:'center'},
						          {field:'F2',	title:'店不採用禁止',			width: 60,halign:'center',align:'center',formatter:cformatter,	styler:cstyler},
						          {field:'F3',	title:'通常開始',				styler:cstyler, formatter:cformatter,editor:{type:'checkbox'},width: 60,halign:'center',align:'center'},
						          {field:'F4',	title:'1日遅<br>開始',			styler:cstyler, formatter:cformatter,editor:{type:'checkbox'},width: 60,halign:'center',align:'center'},
						          {field:'F5',	title:'１日遅<br>パターン',		width: 60,halign:'center',align:'center'},
						          {field:'F6',	title:'売価選択<br>設定状況',	width: 60,halign:'center',align:'center'},
						          {field:'F7',	title:'店売価選択禁止',			width: 80,halign:'center',align:'center',formatter:cformatter,	styler:cstyler},
						          {field:'F8',	title:'催しコード',				width: 95,halign:'center',align:'left'},
						          {field:'F9',	title:'販売期間',				width: 180,halign:'center',align:'left'},
						          {field:'F10',	title:'催し名称',				width: 300,halign:'center',align:'left'},
						          {field:'F11',	title:'登録日',					width: 70,halign:'center',align:'left'},
						          {field:'F12',	title:'更新日',					width: 70,halign:'center',align:'left'},
						          {field:'F13',	title:'オペレータ',				width: 80,halign:'center',align:'left'},
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
										var tenpo =that.grd_tenpo_data
										for (var i=0; i<tenpo.length; i++){
											if(tenpo[i]['F1']==that.MOYOKBN&&tenpo[i]['F2']==that.MOYODT&&tenpo[i]['F3']==that.MOYOREN){
												$($.id.gridholder).datagrid('getPanel').find("[datagrid-row-index='"+i+"']").css('color', 'red');
												that.number = i;
											}
										}
										// チェックボックスの設定
										$.initCheckboxCss($('#'+id).datagrid('getPanel').find('.datagrid-body'));
									},
									onClickCell:function(rowIndex, field, value){
										// 列名保持
										that.columnName = field;
									},
									onClickRow: function(index,field){$.clickEditableDatagridCell(that,id, index)},
									onBeginEdit:function(index,row){

										var obj = $('#'+id).datagrid('getPanel').find("[datagrid-row-index='"+index+"']");

										if(row.F2==='1'){
											obj.find("[id^='"+$.id.mbsy_flg+"_"+"']").each(function(){
												$.setInputBoxDisable($('#'+$.id.mbsy_flg+"_"));
											});
										}
										if(row.F5==='無'){
											obj.find("[id^='"+$.id.hbstrt_flg+1+"_"+"']").each(function(){
												$.setInputBoxDisable($('#'+$.id.hbstrt_flg+1+"_"));
											});
										}
										$.setInputBoxDisable($('#'+$.id.hbstrt_flg+2+"_"));

										var length = obj.find('[field="'+that.columnName.split('_')[0]+'"]').find('[disabled=disabled]').length;
										if (length!==0) {
											that.columnName = '';
										}
										$.beginEditDatagridRow(that,id, index, row)
									},
									onEndEdit: function(index,row,changes){
										$.endEditDatagridRow(that, id, index, row)
										row.CHK_SEL = $.id.value_off;
									},
									onAfterEdit: function(index,row,changes){
										// チェックボックスの再追加（EndEdit時に削除されるため）
										$.afterEditAddCheckbox($('#'+id).datagrid('getPanel').find("[datagrid-row-index='"+index+"']"));
										$($.id.gridholder).datagrid('getPanel').find("[datagrid-row-index='"+that.number+"']").css('color', 'red');
									},
								});
				}
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
								F1	 : rows[i]["F14"],
								F2	 : rows[i]["F15"],
								F3	 : rows[i]["F16"],
								F4	 : rows[i]["F1"],
								F5	 : rows[i]["F3"],
								F6	 : rows[i]["F4"],
								F7	 : rows[i]["F17"],
								F8	 : rows[i]["F18"],
								F9	 : rows[i]["F19"],

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
					oldrows = that.grd_tenpo_data
					for (var i=0; i<newrows.length; i++){
						if( newrows[i]['F4'] != oldrows[i]['F4']|| newrows[i]['F5'] != oldrows[i]['F5']
						|| newrows[i]['F6'] != oldrows[i]['F6']){
								var rowDate = {
										F1	 : newrows[i]["F1"],
										F2	 : newrows[i]["F2"],
										F3	 : newrows[i]["F3"],
										F4	 : newrows[i]["F4"],
										F5	 : newrows[i]["F5"],
										F6	 : newrows[i]["F6"],
										F7	 : newrows[i]["F7"],
										F8	 : newrows[i]["F8"],
										F9	 : newrows[i]["F9"],
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
					that.grd_tenpo_data =  data[$.id.gridholder];
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

//					// window 幅取得
//					var changeWidth  = $(window).width();

//					// toolbar の調整
//					$($.id.toolbar).panel('resize',{width:changeWidth});

//					// toolbar の高さ調整
//					$.setToolbarHeight();

//					// DataGridの高さ
//					var gridholderHeight = 0;
//					var placeholderHeight = 0;

//					if ($($.id.gridholder).datagrid('options') != 'undefined') {
//					// tb
//					placeholderHeight = $($.id.toolbar).panel('panel').height() + $($.id.buttons).height();

//					// datagrid の格納された panel の高さ
//					gridholderHeight = $(window).height() - placeholderHeight;
//					}

//					$($.id.gridholder).datagrid('resize', {
//					width:	changeWidth,
//					height:	gridholderHeight
//					});
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
				var sendJSON = JSON.parse( JSON.stringify( that.jsonHidden ) );
				$.setJSONObject(sendJSON, 'sendBtnid', btnId, $('#'+btnId).attr("title"));					// 実行ボタン情報保持
				$.setJSONObject(sendJSON, 'reportYobi1', that.reportYobiInfo(), that.reportYobiInfo());		// 参照情報保持
				$.setJSONObject(sendJSON, 'callpage', that.name, that.name);								// 呼出し元レポート情報

				// 戻る実行時用に現在の画面情報を保持する
				var states = $.getBackBaseJSON(that);
				// 各種グリッド情報を設定
				var newrepinfos = $.getBackJSON(that, states);
				$.setJSONObject(sendJSON, 'repinfo', newrepinfos, '');// 呼出し元レポート情報

				// 選択行
				var row = $($.id.gridholder).datagrid("getSelected");
				//var row = $(that.jsonHidden);
				// 実行ボタン別処理
				switch (btnId) {
				case $.id.btn_sel_change:
					sendMode = 1;

					if(!row){
						$.showMessage('E00008');
						return false;
					}
					index = 5;
					if(row.F16 == "4"){
						index = 6;
					}else if(row.F16 == "3"){
						index = 7;
					}else if(row.F16 == "2"){
						index = 8;
					}
					// 転送先情報
					childurl = href[index];
					var chk_kyosei		= $("input[id=kyosei_flg]:checked").val();
					// オブジェクト作成
					$.setJSONObject(sendJSON, $.id_inp.txt_moyskbn,row.F12, row.F12);
					$.setJSONObject(sendJSON, $.id_inp.txt_moysstdt,row.F13, row.F13);
					$.setJSONObject(sendJSON, $.id_inp.txt_moysrban,row.F14, row.F14);
					$.setJSONObject(sendJSON, $.id_inp.txt_tencd,row.F15, row.F15);
					$.setJSONObject(sendJSON, $.id_mei.sel_qasyukbn,row.F16, row.F16);
					$.setJSONObject(sendJSON, 'kyosei_flg',row.F17, row.F17);

					// 検索条件に指定されている項目は遷移前に変更さている可能性がある為
					// jsonHiddenの値を使用しない。
					var searchCol = []
					searchCol.push($.id_inp.txt_qayyyymm)	// 月度（左）
					searchCol.push($.id_inp.txt_qaend)		// 月度（右）
					searchCol.push($.id_inp.txt_tencd)		// 店コード
					sendJSON.some(function(w, j){
						if (searchCol.indexOf(w.id) != -1){
							var newValue = $.getInputboxValue($('#'+w.id))
							if(newValue != w.value){
								var newText = $.getInputboxText($('#'+w.id))
								w.value	 = newValue
								w.text	 = newText
							}
						}
					});

					break;
				case $.id.btn_upd:
					var backpage = $.getJSONValue(that.jsonHidden, "returnPageInfo1");
					if(backpage == 'TG012'){
						index = 1;
					}else if(backpage == 'TG013'){
						index = 2;
					}else if(backpage == 'TG014'){
						index = 3;
					}
					sendMode = 1;
					// 転送先情報
					childurl = href[index];
					break;
				case $.id.btn_back:
				case $.id.btn_cancel:
					var backpage = $.getJSONValue(that.jsonHidden, "returnPageInfo1");
					if(backpage == 'TG012'){
						index = 1;
					}else if(backpage == 'TG013'){
						index = 2;
					}else if(backpage == 'TG014'){
						index = 3;
					}

					// 送信情報に前回選択行情報を追加
					that.setSelectRowIndex(sendJSON, newrepinfos,"Out_Report"+backpage, $.id.gridholder);

					sendMode = 1;
					// 転送先情報
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
			// 送信情報[sendJSON]に前回選択行情報を設定する。
			setSelectRowIndex:function(sendJSON, newrepinfos, dispId, gridId){
				var index = -1
				var that = this;

				var searchCol = []
				searchCol.push($.id_inp.txt_qayyyymm)	// 月度（左）
				searchCol.push($.id_inp.txt_qaend)		// 月度（右）
				searchCol.push($.id_inp.txt_tencd)		// 店コード

				newrepinfos.some(function(v, i){
					if (v.id==dispId){
						var TMPCOND =newrepinfos[i].value.TMPCOND;
						var IndexId = "scrollToIndex_" + gridId;
						TMPCOND.some(function(w, j){
							if (w.id==IndexId){
								index = w.value;
								if(index != -1){
									$.setJSONObject(sendJSON, IndexId, index, index);
								}
							}else if(searchCol.indexOf(w.id) != -1){
								// 検索条件に指定されている項目は遷移前に変更さている可能性がある為
								// jsonHiddenの値を使用しない。
								var newValue = "";
								that.jsonHidden.some(function(x, k){
									if(x.id == w.id){
										newValue = x.value
									}
								});

								if(newValue != w.value){
									sendJSON.some(function(x, k){
										if(x.id == w.id){
											newValue = x.value

											// sendJSONの値をrepinfoの値に上書きする。
											x.value	 = w.value
											x.text	 = w.text
										}
									});
								}
							}
						});
					}
				});
			},
			changeInputboxFunc:function(that, id, newValue, obj){
				var that = this;

				var parentObj = $('#'+that.focusRootId);
				var txt_bmncd = $.getInputboxValue($('#'+$.id_inp.txt_tencd));

				if(id+"_"===obj.attr('id') && that.focusGridId!==""){
					parentObj = $('#'+that.focusGridId).datagrid('getPanel');
				}

				var size = $('[for_inp^='+id+'_]').length ;


				// DB問い合わせ系
				if($('[for_inp^='+id+'_]').length > 0){
					var param = that.getInputboxParams(that, id, newValue);
					var rows = $.getSelectListData(that.name, $.id.action_change, id, param);
					var row = rows.length > 0 ? rows[0]:"";
					$.setInputboxRowData('for_inp', id, row, that, parentObj);
				}
				// 検索、入力後特殊処理
//				if(that.queried){
//					if(id==='txt_tencd'){
//						// 取得された部門コードが部門マスタテーブルに存在しない場合、エラー。
//						var txt_bmncd_chk = $.getInputboxData(that.name, $.id.action_check,  $.id_inp.txt_bmncd, [{KEY:"MST_CNT",value:txt_bmncd}]);
//						if(txt_bmncd_chk == "0"){
//							$.showMessage('E11044');
//							return false;
//						}
//					}
//
//				}

			},
			getInputboxParams: function(that, id, newValue){
				// 情報取得
				var values = {};
				values["value"] = newValue;
				values["TABLEKBN"] = that.baseTablekbn;

				// 情報設定
				return [values];
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
			}
		} });
})(jQuery);