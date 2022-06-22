/**
 * jquery report TenpoSets
 * 店舗グループ
 */

;(function($) {

$.extend({
	TenpoSets: {

		initializes: false,		// 初期化フラグ（店舗グループ）
		initializesCond: true,	// 初期化フラグ（条件用）
		lastIndex: -1,			// 編集位置（行）
		selCategoryText: '',	// 店舗グループ初期化時の選択店舗グループ一時保存
		limitCategory:30,		// 店舗グループの最大登録数
		limitSyohin:200,		// 店舗グループに保存する店舗の最大登録数

		getSecureCombogrid: function(id, method){
			// easyui.combogrid から情報取得
			var value = '';
			var localid = id||'';
			if (!id.match('/^#/')){
				localid = '#'+id;
			}
			try {
				value = $(localid).combogrid(method);
			} catch (e) {
				// alert(e);
			}
			return value;
		},

		init: function() {
			var that = this;

			// 複数追加入力欄
			$('#ddtext-tg').textbox({
				width: '286',
				height: '226',
				prompt: '\n店舗コードを入力してください。\n複数指定する場合は改行してください。',
				multiline: true
			});
			// 複数追加の追加ボタン
			$('#bbadd-tg').on('click', this.AddMultiple);

			// 店舗グループ
			$('#itemCell-tg').combogrid({
				panelWidth:305,
				url:$.reg.easy,
				required: false,
				striped: true,
				mode: 'local',
				showHeader:false,
				idField:'VALUE',
				textField:'TEXT',
				columns:[[
					{field:'TEXT',	title:'名称',	width:300}
				]],
				fitColumns: true,
				delay:200,		// 変更即検索対応時はある程度余裕を持たせる
				onShowPanel:function(){
					$.setScrollGrid(this);
				},
				onBeforeLoad:function(param){
					if(!that.initializes) return false;

					// 情報設定
					var json = [{
						"CD_USER"	:	$($.id.hiddenUser).val()
					}];
					param.page		=	'';
					param.obj		=	$.id.btn_Input_TenpoG;
					param.sel		=	(new Date()).getTime();
					param.action	=	$.id.action_tenpo;
					param.json		=	JSON.stringify(json);
				},
				onLoadSuccess:function(data){
					if(data.rows.length > 0 ){
						// 情報を選択
						var num = 0;
						var txt = that.selCategoryText;
						for (var i=0; i<data.rows.length; i++){
							if (data.rows[i].TEXT == txt){
								num = i;
								break;
							}
						}
						$('#itemCell-tg').combogrid('grid').datagrid('selectRow',num);
						// 表示ボタン・クリック
						$('#sb1-tg').trigger('click');
						that.initializes = !that.initializes;
					}
				},
				onClickRow:function(rowIndex, rowData){
					// テキスト情報を取得
					if(rowData != null){
						if(that.selCategoryText != rowData.TEXT){
							that.selCategoryText = rowData.TEXT;
							// 表示ボタン・クリック
							$('#sb1-tg').trigger('click');
						}
					}
				},
				onChange:function(newValue, oldValue){
					// 適用ボタン無効化
					$.setButtonState('#qt-tg', false, 'itemCell');
				}
			});

			// dataGrid 初期化
			this.setDataGrid('#tt-tg');

			// 拡張設定
			this.ExtendSetting();

			// メニュー設定
			$('#sb1-tg').on("click", this.Expand);
			$('#mmExpand-tg').on("click", this.Expand);
			$('#mmDelete-tg').on("click", this.Delete);

			// 店舗グループ
			$('#'+$.id.btn_Input_TenpoG).on("click", this.Open);
			// 適用処理
			$('#qt-tg').on("click", this.Search);
			// 適用ボタン不可
			$.setButtonState('#qt-tg', false, 'init');

			$('#win-tg').panel({
				onBeforeClose:function(){
					// リサイズイベント有効化
					$.reg.resize = true;
				}
			});
		},

		condInit: function() {
			var that = this;

			// 初期化（店舗グループ初期値セット）
			that.initializes = true;
			that.selCategoryText = $('#'+$.id.SelTenpoG).combogrid('getText');
			$('#itemCell-tg').combogrid('grid').datagrid('load',{});

			that.initializesCond = !that.initializesCond;
		},

		Open: function() {
			if ($(this).linkbutton('options').disabled)	return false;

			if($.TenpoSets.initializesCond){
				$.TenpoSets.condInit();
			}

			// ウインドウ展開中リサイズイベント無効化
			$.reg.resize = false;
			// 店舗グループ window 表示
			$('#win-tg').window('open');
		},

		Search: function(){
			// 「適用」処理
			if ($(this).linkbutton('options').disabled)	return false;

			// 店舗グループの情報取得
			var json = [{
				"CD_CTG"	:	$.TenpoSets.getSecureCombogrid('itemCell-tg', 'getValue'),
				"NM_CTG"	:	$.TenpoSets.getSecureCombogrid('itemCell-tg', 'getText')
			}];
			if (json[0].CD_CTG==='' || !json[0].CD_CTG.match(/^[0-9]+$/)){
				alert('店舗グループを選択してください。');
				return false;
			}
			// 店舗グループの更新
			$.TenpoSets.Refresh(json);

			// 検索ボタン有効化
			$.setButtonState('#'+$.id.btn_search, true, $.id.SelCategory);
			// 店舗グループ window 閉じる
			$('#win-tg').window('close');
		},

		Expand: function(){
			// 「表示」処理
			var selVal = $.TenpoSets.getSecureCombogrid('itemCell-tg', 'getValue');

			// ブランク選択時
			if(selVal == ""){
				$('#tt-tg').datagrid('clearSelections').datagrid({data: []});
				// 適用ボタン無効化
				$.setButtonState('#qt-tg', false, 'itemCell-tg');
				return false;
			}

			// 店舗グループの情報取得
			var json = [{
				"CD_CTG" :	selVal
			}];
			if (json[0].CD_CTG==='' || !json[0].CD_CTG.match(/^[0-9]+$/)){
				alert('店舗グループを選択してください。');
				return false;
			}

			// 情報更新
			$.post(
				$.reg.easy,
				{
					"page"		:	'' ,
					"obj"		:	$.id.btn_call_tg ,
					"sel"		:	(new Date()).getTime(),
					"action"	:	$.id.action_tenpo,
					"json"		:	JSON.stringify( json )
				} ,
				function(json){
					// JSONに変換
					var data = JSON.parse(json);
					// 選択クリア
					$('#tt-tg').datagrid('clearSelections');
					// 結果表示
					$('#tt-tg').datagrid('loadData',data.rows);
					// 適用ボタン有効化
					$.setButtonState('#qt-tg', true, 'Expand-tg');
				}
			);
		},

		Delete: function(){
			// 「削除」処理

			// 店舗グループの情報取得
			var json = [{
				"CD_USER"	:	$($.id.hiddenUser).val(),
				"CD_CTG"	:	$.TenpoSets.getSecureCombogrid('itemCell-tg', 'getValue')
			}];
			if (json[0].CD_CTG==='' || !json[0].CD_CTG.match(/^[0-9]+$/)){
				alert('店舗グループを選択してください。');
				return false;
			}

			// 確認
			if (confirm('店舗グループを削除してもよろしいでしょうか？')){
				$.post(
					$.reg.easy,
					{
						"page"		:	'' ,
						"obj"		:	$.id.btn_delete_tg ,
						"sel"		:	(new Date()).getTime(),
						"action"	:	$.id.action_tenpo,
						"json"		:	JSON.stringify( json )
					},
					function(json){
						// 適用ボタン不可
						$.setButtonState('#qt-tg', false, 'Delete');
						// 一覧クリア
						$('#tt-tg').datagrid('clearSelections').datagrid({data: []});
						// 商品セット load
						$.TenpoSets.initializes = true;
						$.TenpoSets.selCategoryText = "";
						$('#itemCell-tg').combogrid('clear').combogrid('grid').datagrid('load',{q:''});

						// 店舗グループの情報取得
						var json = [{
								"CD_CTG"	:	'',
								"NM_CTG"	:	''
							}];
						$.TenpoSets.Refresh(json);

						alert('削除しました。');
					}
				);
			}
		},

		Refresh: function(json){	// 店舗グループの更新
			// レポート番号取得
			var reportno=$($.id.hidden_reportno).val();

			// レポート定義位置
			var reportNumber = $.getReportNumber(reportno);
			if (typeof(reportNumber) !== 'number') { alert("レポート定義が見つかりません。"); return false;}

			// 初期化配列から削除
			var index = $.inArray($.id.SelTenpoG, $.report[reportNumber].initedObject);
			if ( index !== -1) {
				$.report[reportNumber].initedObject.splice( index, 1 );
			}
			// 店舗グループ設定
			$.setJSONObject($.report[reportNumber].jsonHidden,$.id.SelTenpoG,json[0].CD_CTG,json[0].NM_CTG);

			// 店舗グループリロード
			var $datagrid = $('#'+$.id.SelTenpoG).combogrid('grid');
			$datagrid.datagrid('load');
		},
		AddMultiple: function(){	// 複数追加
			var that = this;
			var added = true;

			// 入力欄の内容取得
			var input = $('#ddtext-tg').textbox('getText');
			if (input.length===0){
				alert('店舗コードを入力してください。');
				return;
			}

			var _$tt = $('#tt-tg');
			// カテゴリに保存する商品の最大商品数チェック
			var rowsLength = _$tt.datagrid('getRows').length;

			// 内容スプリット
			var array = input.split(/\r\n|\r|\n/);
			_$tt.datagrid('endEdit', that.lastIndex);
			for (i = 0; i < array.length; i++) {

				// 空文字は除外
				if (array[i].length===0) continue;

				// 上限確認
				rowsLength++;
				if(rowsLength > $.TenpoSets.limitSyohin){
					// 中断処理
					alert('店舗グループに登録可能な商品件数の上限に達しています。\n商品は１店舗グループに対し、最大'+$.TenpoSets.limitSyohin+'件まで登録可能です。');
					return false;
				}

				// 追加処理
				_$tt.datagrid('appendRow',{
					F1:('000'+array[i]).slice(-3),	// 前0埋め3桁調整
					F2:'',
					F3:new Date().getTime()
				});

				if (added) {
					added = false;
					// 適用ボタン無効化
					$.setButtonState('#qt', false, 'itemCell');
				}
			}
			that.lastIndex = _$tt.datagrid('getRows').length-1;
			_$tt.datagrid('beginEdit', that.lastIndex);
			$('#ddtext-tg').textbox('clear');
			// 事後処理
			alert('追加作業終了しました。');
		},

		ExtendSetting: function(){
			// 機能拡張
			var that = this;

			$.extend($.fn.datagrid.defaults.editors, {
				combogridT: {
					init: function(container, options){
						var input = $('<input class="datagrid-editable-input">').appendTo(container);

						var opts=options||{};
						opts['panelWidth']=500;
						opts['striped']=true;
						opts['mode']='remote';
						opts['idField']='VALUE';
						opts['textField']='VALUE';
						opts['showHeader']=false;
						opts['columns']=[[
								{field:'VALUE',	title:'',	width:110},
								{field:'TEXT',	title:'',	width:385}
							]];
						opts['fitColumns']=true;
						opts['hasDownArrow']=false;
						opts['onBeforeLoad'] = function(param){
							// 情報設定
							var json = [{
								"DUMMY"	:	"DUMMY"
							}];
							// パラメータ
							param.action = $.id.action_tenpo;
							param.json = JSON.stringify( json );
							param.t = (new Date()).getTime();
						};
						opts['onSelect'] = function(rowIndex, rowData){
							// リスト選択後、名称の更新を実施
							if (that.lastIndex===-1) return false;
							var row = $('#tt-tg').datagrid('getRows');
							if (row.length===0) return false;
							row[that.lastIndex].F2 = rowData.TEXT;
						};
						opts['onHidePanel'] = function(rowIndex, rowData){
							// リストが隠れた後、編集モードの解除することにより名称を適用
							$('#tt-tg').datagrid('endEdit', that.lastIndex);
							that.lastIndex = -1;
						};
						input.combogrid(opts);
						return input;
					},
					destroy: function(target){
						// 解除処理
						$(target).combogrid('destroy');
					},
					getValue: function(target){
						// 編集解除時に導入値を戻す
						return $(target).combogrid('getValue');
					},
					setValue: function(target, value){
						// 編集時に期待値の設定
						$(target).combogrid('setValue', value);
						$(target).combogrid('grid').datagrid('options').url = $.reg.easy;
						// 候補リストの更新
						$(target).combogrid('grid').datagrid('load',{q:value});
					},
					resize: function(target, width){
						// カラム幅の resize
						$(target).combogrid('resize', width);
					}
				}
			});
		},

		setDataGrid : function(id) {
			// DataGrid定義
			var that = this;

			$(id).datagrid({
				fit:true,
				nowrap: true,
				striped: true,
				collapsible:false,
				sortOrder: 'desc',
				remoteSort: true,
				idField:'F3',
				pageSize:10,
				columns:[[
					{field:'ck',	checkbox:true},
					{field:'F1', title:'店舗', width:90, resizable:true,
						editor:{ type:'combogridT', options:{required:false} }
					},
					{field:'F2', title:'店舗名', width:385}
				]],
				fitColumns:false,	// 指定カラム幅を適用する場合、false 指定。
				pagination:false,
				rownumbers:true,
				toolbar:[{
					text:'追加',
					iconCls:'icon-add',
					handler:function(){
						// カテゴリに保存する商品の最大商品数チェック
						var rowsLength = $(id).datagrid('getRows').length;
						if(rowsLength >= that.limitSyohin){
							alert('店舗グループに登録可能な商品件数の上限に達しています。\n商品は１店舗グループに対し、最大'+that.limitSyohin+'件まで登録可能です。');
							return false;
						}

						$(id).datagrid('endEdit', that.lastIndex);
						$(id).datagrid('appendRow',{
							F1:'',
							F2:'',
							F3:new Date().getTime()
						});
						that.lastIndex = $(id).datagrid('getRows').length-1;
						$(id).datagrid('beginEdit', that.lastIndex);

						// 適用ボタン無効化
						$.setButtonState('#qt-tg', false, 'itemCell');
					}
				},{
					text:'複数追加',
					iconCls:'icon-add',
					handler:function(){
						$('#dd-tg').window('open');
					}
				},'-',{
					text:'削除',
					iconCls:'icon-remove',
					handler:function(){
						var seledRows = $(id).datagrid('getChecked');
						while(seledRows.length){
							var idx =$(id).datagrid('getRowIndex',seledRows[0]);
							$(id).datagrid('deleteRow',idx);
						}
						$(id).datagrid('clearSelections');

						// 適用ボタン無効化
						$.setButtonState('#qt-tg', false, 'itemCell');
					}
				},'-',{
					text:'保存',
					iconCls:'icon-save',
					handler:function(){
						// 店舗グループ名文字数チェック
						var name = $.trim($.TenpoSets.getSecureCombogrid('itemCell-tg','getText'));
						if (name.length < 1 || $.checkByte(name, 100) == false){
							alert('店舗グループ名は全角50文字以内で入力してください。');
							return false;
						}
						// カテゴリ新規保存時は、最大カテゴリ数チェック
						var ctgGrid = $('#itemCell-tg').combogrid("grid");
						if(ctgGrid.datagrid("getSelected") == undefined || name != ctgGrid.datagrid("getSelected").TEXT){
							if(ctgGrid.datagrid("getRows").length >= that.limitCategory){
								alert('登録可能な店舗グループ数の上限に達しています。\n店舗グループは１利用者に対し、最大'+that.limitCategory+'件まで登録可能です。');
								return false;
							}
						}

						// 登録情報の構築
						var json = [{
							"CD_USER"	:	$($.id.hiddenUser).val(),
							"NM_CTG"	:	name
						}];

						// 店舗コードを配列に保持
						var currentRows = $(id).datagrid('getRows');
						var rowsLength = currentRows.length;
						var jancodes = [];
						for (var index=0; index<rowsLength; index++){
							var code = currentRows[index].F1;
							if (code.length < 1 || code.length > 3 || ! code.match(/^[0-9]+[ ]*$/)){
								// 店舗コード 入力エラー
								alert('店舗コードの入力が正しくありません。');
								return false;
							}
							// 重複は除外
							if ($.inArray(code, jancodes) >= 0) {
								continue;
							}
							jancodes.push(code);
							json.push({	"CD_ITEM" : code});
						}

						// カテゴリに保存する商品の最大商品数チェック
						if(jancodes.length > that.limitSyohin){
							alert('店舗グループに登録可能な商品件数の上限に達しています。\n店舗は１店舗グループに対し、最大' + String(that.limitSyohin).replace(/(\d)(?=(\d\d\d)+(?!\d))/g, '$1,') + '件まで登録可能です。\n店舗の絞込みを行ってください。');
							return false;
						}

						$(id).datagrid('acceptChanges');

						// 保存処理
						$.post(
							$.reg.easy,
							{
								"page"		:	'' ,
								"obj"		:	$.id.btn_entry_tg,
								"sel"		:	(new Date()).getTime(),
								"action"	:	$.id.action_tenpo,
								"json"		:	JSON.stringify( json )
							},
							function(data){
								// 商品セット load
								that.initializes = true;
								that.selCategoryText = name;
								$('#itemCell-tg').combogrid('grid').datagrid('load', {});
								alert('保存しました。');
							}
						);

						$(id).datagrid('rejectChanges');
						that.lastIndex=-1;
					}
				},'-',{
					text:'戻す',
					iconCls:'icon-undo',
					handler:function(){
						$(id).datagrid('rejectChanges');
						that.lastIndex=-1;
					}
				}],
				onBeforeLoad:function(){
					$(this).datagrid('rejectChanges');
					that.lastIndex=-1;
				},
				onClickRow:function(rowIndex, rowData){
					if (that.lastIndex != rowIndex){
						// 最後の編集レコードと異なるレコードを選択した場合
						$(id).datagrid('endEdit', that.lastIndex);
						$(id).datagrid('beginEdit', rowIndex);
						that.lastIndex = rowIndex;
					} else {
						// 最後の編集レコードを再度選択した場合
						$(id).datagrid('endEdit', that.lastIndex);
						that.lastIndex = -1;
					}
				}
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