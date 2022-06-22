/**
 * jQuery : 1.6.2
 * レポート情報の初期化と共通イベント定義
 * 個別レポートに対応したイベントを作成する場合は、別ファイルに定義してください。
 */
$(function(){

	/**
	 * 初期化
	 * 事前読込 jquery.report.nn.js , jquery.report.control.js
	 * @param {Object} $.reportOption
	 */
	(function() {

		// レポートオプションの設定
		$.report($.reportOption);

		// レポート番号取得
		var reportno = $($.id.hidden_reportno).val();

		// メッセージ一覧取得
		$.initMessageListData(reportno);

		// 禁止文字取得
		$.initProhibitedListData(reportno);

		// レポート定義位置
		var reportNumber = $.getReportNumber(reportno);
		if (typeof(reportNumber) !== 'number') { alert("レポート定義が見つかりません。"); return false;}


		// マスク追加
		$.appendMask();

		// initialize
		if(typeof $.report[reportNumber].initialize != "function") return true;
		$.report[reportNumber].initialize(reportno);

		// 	コールバック関数の紐付け
		// 検索  クリックイベント
		$('#'+$.id.btn_search).on("click", $.pushSearch);
		// Excel クリックイベント
		$('#'+$.id.btn_excel).on("click", $.pushExcel);

		// 登録(DB更新処理) クリックイベント
		$('#'+$.id.btn_upd).on("click", $.pushUpd);
		// 削除(DB更新処理) クリックイベント
		$('#'+$.id.btn_del).on("click", $.pushDel);

		// 画面遷移：戻る クリックイベント
		$('#'+$.id.btn_back).on("click", $.pushChangeReport);

		// 検索  クリックイベント
		$('#'+$.id.btn_clear).on("click", $.pushClear);

//		// 条件リセット：戻る クリックイベント
//		$('#'+$.id.btn_reset).on("click", $.pushReset);
//
//		// 更新 クリックイベント
//		$('#'+$.id.btn_reload).on("click", $.pushReload);
//		// 追加 クリックイベント
//		$('#'+$.id.btn_add).on("click", $.pushAdd);
		// 削除 クリックイベント
		$('#'+$.id.btn_delete).on("click", $.pushDelete);
		// 保存 クリックイベント
		$('#'+$.id.btn_entry).on("click", $.pushEntry);
//		// 戻す クリックイベント
//		$('#'+$.id.btn_undo).on("click", $.pushUndo);
//
//		// 定義保存：適用ボタン クリックイベント
//		$('#'+$.id.btn_view_shiori).on("click", $.pushViewShiori);
//		// 定義保存：保存ボタン クリックイベント
//		$('#'+$.id.btn_entry_shiori).on("click", $.pushEntryShiori);
//		// 定義保存：削除ボタン クリックイベント
//		$('#'+$.id.btn_delete_shiori).on("click", $.pushDeleteShiori);

//		// 店別数量展開 クリックイベント
//		$('#'+$.id.btn_tenbetusu).on("click", $.pushChangeReport);

		// 登録処理系での帳票移動の警告メッセージ
		// ヘッダー内リンク(閉じる除く)
		$('#header a', window.parent.document).filter("[id!='hlnk_close']").click(function(){
			var rt = true;
			// 登録系の場合、変更があった場合に確認メッセージ
			var obj = $($.id.hiddenChangedIdx);
			if(obj.length===0){obj = $(this).parent($.id.hiddenChangedIdx);}
			// 未登録の警告メッセージが必要かどうかチェック
			if($.getConfirmUnregistFlg(obj)){
				rt = confirm($.getMessage('E11239'));
			}
			return rt;
		});

		// タブ移動の警告メッセージ
		$.changeReportByTabs($.report[reportNumber]);

		// ラジオボタン
		$(":radio").on("change",
			function(e){
				$('input[name=' + e.target.name + ']').closest("label").removeClass("selected_radio");
				$(e.target).closest("label").addClass("selected_radio");
		});
		$(":radio:checked").closest("label").addClass("selected_radio");

		// 特定の帳票のみ、ショートカット機能追加
		if(['Out_Reportx002'].indexOf(reportno) !== -1){
			shortcut.add("F1",function() {
				$('#'+$.id.btn_cancel).focus().click();
			});

			shortcut.add("F12",function() {
				var tag_options = $('#'+$.id.btn_upd+'_winIT031').attr('data-options');
				if(tag_options){
					tag_options = '\"'+tag_options.replace(/'/g, '').replace(/,/g, '\",\"').replace(/([a-zA-Z0-9]+):/g, '$1\":\"')+'\"';
				}
				var options = tag_options ? JSON.parse('{'+tag_options+'}'): JSON.parse('{}');
				var abc = options.winIT031;
				if(abc == "false"){
					$('#'+$.id.btn_upd).focus().click();
				}else{
					$('#'+$.id.btn_upd+'_winIT031').focus().click();
				}
			});
		}

		// EasyUI Combobox focus時にキャレット位置を先頭に設定
		$('.textbox-text.validatebox-text').on('focus', function() {
			if ($(this).attr('readonly')) {
				var pos = 0;
				var item = $(this).get(0);
				if (item.setSelectionRange) {  // Firefox, Chrome
					item.focus();
					item.setSelectionRange(pos, pos);
				} else if (item.createTextRange) { // IE
					var range = item.createTextRange();
					range.collapse(true);
					range.moveEnd("character", pos);
					range.moveStart("character", pos);
					range.select();
				}
			} else {
				$(this).select();
			}
		});

	})();

	/**
	 * resize Event
	 */
	var ResizeWindows=function(){
		// レポート番号取得
		var reportno=$($.id.hidden_reportno).val();

		// レポート定義位置
		var reportNumber = $.getReportNumber(reportno);
		if (typeof(reportNumber) !== 'number') { alert("レポート定義が見つかりません。"); return false;}

		// リサイズ処理
		$.report[reportNumber].setResize();
	};
	var resizeTimer = null;
	$(window).on('resize', function() {
		if ($.reg.resize) {
			if (resizeTimer) clearTimeout(resizeTimer);
			resizeTimer = setTimeout(ResizeWindows, 200);
		}
	});

	// backspaceキーによる「戻る」機能の無効化
	var ctrlBackSpace = function(e) {
		var code = e.which ? e.which : e.keyCode;
		if (code === 8) {
			var target = $(e.target);
			if ((!target.is('input:text') && !target.is('input:password') && !target.is('textarea')) || target.attr('readonly') || target.is(':disabled')) {
				return false;
			}
		}

		if (code === 27) {
			return false;
		}

		return true;
	};
	$(document).keydown(function(e) {
		return ctrlBackSpace(e);
	});
	$(window.parent.document).keydown(function(e) {
		return ctrlBackSpace(e);
	});
});

//EasyUI Combobox focus時にキャレット位置を先頭に設定
function ctrlFocus(obj){
	if ($(obj).attr('readonly')) {
		var pos = 0;
		var item = $(obj).get(0);
		if (item.setSelectionRange) {  // Firefox, Chrome
			item.focus();
			item.setSelectionRange(pos, pos);
		} else if (item.createTextRange) { // IE
			var range = item.createTextRange();
			range.collapse(obj);
			range.moveEnd("character", pos);
			range.moveStart("character", pos);
			range.select();
		}
	} else {
		$(obj).select();
	}
}
