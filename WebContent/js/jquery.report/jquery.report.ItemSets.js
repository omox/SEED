/**
 * jquery report ItemSets
 * 商品入力
 */

;(function($) {

$.extend({
	ItemSets: {

		initializes: false,		// 初期化フラグ（商品グループ）
		initializesCond: true,	// 初期化フラグ（条件用）
		lastIndex: -1,			// 編集位置（行）
		selCategoryText: '',	// 商品グループ初期化時の選択商品グループ一時保存
		limitCategory:100,		// 商品グループの最大登録数
		limitSyohin:300,		// 商品グループに保存する商品の最大登録数

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
			$('#ddtext').textbox({
				width: '286',
				height: '226',
				prompt: '\n商品コードを入力してください。\n複数指定する場合は改行してください。',
				multiline: true
			});
			// 複数追加の追加ボタン
			$('#bbadd').on('click', that.AddMultiple);

			// 商品グループ
			$('#itemCell').combogrid({
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
					param.obj		=	$.id.btn_Input;
					param.sel		=	(new Date()).getTime();
					param.action	=	$.id.action_items;
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
						$('#itemCell').combogrid('grid').datagrid('selectRow',num);
						// 表示ボタン・クリック
						$('#sb1').trigger('click');
						that.initializes = !that.initializes;
					}
				},
				onClickRow:function(rowIndex, rowData){
					// テキスト情報を取得
					if(rowData != null){
						if(that.selCategoryText != rowData.TEXT){
							that.selCategoryText = rowData.TEXT;
							// 表示ボタン・クリック
							$('#sb1').trigger('click');
						}
					}
				},
				onChange:function(newValue, oldValue){
					// 適用ボタン無効化
					$.setButtonState('#qt', false, 'itemCell');
				}
			});

			// dataGrid 初期化
			this.setDataGrid('#tt');

			// 拡張設定
			this.ExtendSetting();

			// メニュー設定
			$('#sb1').on("click", this.Expand);
			$('#mmExpand').on("click", this.Expand);
			$('#mmDelete').on("click", this.Delete);

			// 商品入力
			$('#'+$.id.btn_Input).on("click", this.Open);
			// 適用処理
			$('#qt').on("click", this.Search);
			// 適用ボタン不可
			$.setButtonState('#qt', false, 'init');

			$('#win').panel({
				onBeforeClose:function(){
					// リサイズイベント有効化
					$.reg.resize = true;
				}
			});
		},

		condInit: function() {
			var that = this;

			// 初期化（商品グループ初期値セット）
			that.initializes = true;
			that.selCategoryText = $('#'+$.id.SelCategory).combogrid('getText');
			$('#itemCell').combogrid('grid').datagrid('load',{});

			that.initializesCond = !that.initializesCond;
		},

		Open: function() {
			if ($(this).linkbutton('options').disabled)	return false;

			if($.ItemSets.initializesCond){
				$.ItemSets.condInit();
			}

			// ウインドウ展開中リサイズイベント無効化
			$.reg.resize = false;
			// 商品入力 window 表示
			$('#win').window('open');
		},

		Search: function(){
			// 「適用」処理
			if ($(this).linkbutton('options').disabled)	return false;

			// 商品入力の情報取得
			var json = [{
				"CD_CTG"	:	$.ItemSets.getSecureCombogrid('itemCell', 'getValue'),
				"NM_CTG"	:	$.ItemSets.getSecureCombogrid('itemCell', 'getText')
			}];
			if (json[0].CD_CTG==='' || !json[0].CD_CTG.match(/^[0-9]+$/)){
				alert('商品グループを選択してください。');
				return false;
			}

			// 商品に設定
			$('#'+$.id.SelSyohin)
				.combogrid('setValue',json[0].NM_CTG)
				.combogrid('setText', json[0].NM_CTG);

			// 商品グループ名の設定
			$('#'+$.id.SelCategory)
				.combogrid('setValue',json[0].CD_CTG)
				.combogrid('setText', json[0].NM_CTG);

			var $datagrid = $('#'+$.id.SelSyohin).combogrid('grid');
			$datagrid.datagrid('loadData',[]);

			// 検索ボタン有効化
			$.setButtonState('#'+$.id.btn_search, true, $.id.SelCategory);
			// 商品入力 window 閉じる
			$('#win').window('close');
		},

		Expand: function(){
			// 「表示」処理
			var selVal = $.ItemSets.getSecureCombogrid('itemCell', 'getValue');

			// ブランク選択時
			if(selVal == ""){
				$('#tt').datagrid('clearSelections').datagrid({data: []});
				// 適用ボタン無効化
				$.setButtonState('#qt', false, 'itemCell');
				return false;
			}

			// 商品入力の情報取得
			var json = [{
				"CD_CTG" :	selVal
			}];
			if (json[0].CD_CTG==='' || !json[0].CD_CTG.match(/^[0-9]+$/)){
				alert('商品グループを選択してください。');
				return false;
			}

			// 情報更新
			$.post(
				$.reg.easy,
				{
					"page"		:	'' ,
					"obj"		:	$.id.btn_call ,
					"sel"		:	(new Date()).getTime(),
					"action"	:	$.id.action_items,
					"json"		:	JSON.stringify( json )
				} ,
				function(json){
					// JSONに変換
					var data = JSON.parse(json);
					// 選択クリア
					$('#tt').datagrid('clearSelections');
					// 結果表示
					$('#tt').datagrid('loadData',data.rows);
					// 適用ボタン有効化
					$.setButtonState('#qt', true, 'Expand');
				}
			);
		},

		Delete: function(){
			// 「削除」処理

			// 商品入力の情報取得
			var json = [{
				"CD_USER"	:	$($.id.hiddenUser).val(),
				"CD_CTG"	:	$.ItemSets.getSecureCombogrid('itemCell', 'getValue')
			}];
			if (json[0].CD_CTG==='' || !json[0].CD_CTG.match(/^[0-9]+$/)){
				alert('商品グループを選択してください。');
				return false;
			}

			// 確認
			if (confirm('商品グループを削除してもよろしいでしょうか？')){
				$.post(
					$.reg.easy,
					{
						"page"		:	'' ,
						"obj"		:	$.id.btn_delete ,
						"sel"		:	(new Date()).getTime(),
						"action"	:	$.id.action_items,
						"json"		:	JSON.stringify( json )
					},
					function(json){
						// 適用ボタン不可
						$.setButtonState('#qt', false, 'Delete');
						// 一覧クリア
						$('#tt').datagrid('clearSelections').datagrid({data: []});
						// 商品セット load
						$.ItemSets.initializes = true;
						$.ItemSets.selCategoryText = "";
						$('#itemCell').combogrid('clear').combogrid('grid').datagrid('load',{q:''});

						// 商品に設定
						$('#'+$.id.SelSyohin)
							.combogrid('setValue','')
							.combogrid('setText', '');

						// 商品グループ名の設定
						$('#'+$.id.SelCategory)
							.combogrid('setValue','')
							.combogrid('setText', '');

						alert('削除しました。');
					}
				);
			}
		},

		AddMultiple: function(){	// 複数追加
			var that = this;
			var added = true;

			// 入力欄の内容取得
			var input = $('#ddtext').textbox('getText');
			if (input.length===0){
				alert('商品コードを入力してください。');
				return;
			}

			var _$tt = $('#tt');
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
				if(rowsLength > $.ItemSets.limitSyohin){
					// 中断処理
					alert('商品グループに登録可能な商品件数の上限に達しています。\n商品は１商品グループに対し、最大'+$.ItemSets.limitSyohin+'件まで登録可能です。');
					return false;
				}

				// 追加処理
				_$tt.datagrid('appendRow',{
					F1:('00000000'+array[i]).slice(-8),	// 前0埋め8桁調整
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
			$('#ddtext').textbox('clear');
			// 事後処理
			alert('追加作業終了しました。');
		},

		ExtendSetting: function(){
			// 機能拡張
			var that = this;

			$.extend($.fn.datagrid.defaults.editors, {
				combogrid: {
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
							param.action = $.id.action_items;
							param.json = JSON.stringify( json );
							param.t = (new Date()).getTime();
						};
						opts['onSelect'] = function(rowIndex, rowData){
							// リスト選択後、商品名称の更新を実施
							if (that.lastIndex===-1) return false;
							var row = $('#tt').datagrid('getRows');
							if (row.length===0) return false;
							row[that.lastIndex].F2 = rowData.TEXT;
						};
						opts['onHidePanel'] = function(rowIndex, rowData){
							// リストが隠れた後、編集モードの解除することにより商品名称を適用
							$('#tt').datagrid('endEdit', that.lastIndex);
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
					{field:'F1', title:'商品コード', width:110, resizable:true,
						editor:{ type:'combogrid', options:{required:false} }
					},
					{field:'F2', title:'商品名', width:400}
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
							alert('商品グループに登録可能な商品件数の上限に達しています。\n商品は１商品グループに対し、最大'+that.limitSyohin+'件まで登録可能です。');
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
						$.setButtonState('#qt', false, 'itemCell');
					}
				},{
					text:'複数追加',
					iconCls:'icon-add',
					handler:function(){
						$('#dd').window('open');
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
						$.setButtonState('#qt', false, 'itemCell');
					}
				},'-',{
					text:'保存',
					iconCls:'icon-save',
					handler:function(){
						// 商品グループ名文字数チェック
						var name = $.trim($.ItemSets.getSecureCombogrid('itemCell','getText'));
						if (name.length < 1 || $.checkByte(name, 100) == false){
							alert('商品グループ名は全角50文字以内で入力してください。');
							return false;
						}
						// カテゴリ新規保存時は、最大カテゴリ数チェック
						var ctgGrid = $('#itemCell').combogrid("grid");
						if(ctgGrid.datagrid("getSelected") == undefined || name != ctgGrid.datagrid("getSelected").TEXT){
							if(ctgGrid.datagrid("getRows").length > that.limitCategory){
								alert('登録可能な商品グループ数の上限に達しています。\n商品グループは１利用者に対し、最大'+that.limitCategory+'件まで登録可能です。');
								return false;
							}
						}

						// 登録情報の構築
						var json = [{
							"CD_USER"	:	$($.id.hiddenUser).val(),
							"NM_CTG"	:	name
						}];

						// 商品コードを配列に保持
						var currentRows = $(id).datagrid('getRows');
						var rowsLength = currentRows.length;
						var jancodes = [];
						for (var index=0; index<rowsLength; index++){
							var code = currentRows[index].F1;
							if (code.length < 5 || code.length > 20 || ! code.match(/^[0-9]+[ ]*$/)){
								// 商品コード 入力エラー
								alert('商品コードの入力が正しくありません。');
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
							alert('商品グループに登録可能な商品件数の上限に達しています。\n商品は１商品グループに対し、最大' + String(that.limitSyohin).replace(/(\d)(?=(\d\d\d)+(?!\d))/g, '$1,') + '件まで登録可能です。\n登録商品の絞込みを行ってください。');
							return false;
						}

						$(id).datagrid('acceptChanges');

						// 保存処理
						$.post(
							$.reg.easy,
							{
								"page"		:	'' ,
								"obj"		:	$.id.btn_entry,
								"sel"		:	(new Date()).getTime(),
								"action"	:	$.id.action_items,
								"json"		:	JSON.stringify( json )
							},
							function(data){
								// 商品セット load
								that.initializes = true;
								that.selCategoryText = name;
								$('#itemCell').combogrid('grid').datagrid('load', {});
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