$(function(){
	/**
	 * 読み込み完了時に行う処理
	 */
	var init = false;
	/**
	 * タブ設定
	 * @param {Object} title
	 */
	$('#tabs').tabs({
		fit:true,
		border:false,
		showHeader:false,
		onSelect: function(title){
			if (init){
				// タブ選択（初期化処理時は対象外）
				// href 情報
				var href = $('#tabContent').val().split(',');

				// tabs 情報
				var tabs = $(this).tabs('tabs');
				// シリアル番号取得（title一致）
				for (var index=0; index<tabs.length; index++){
					if (tabs[index].panel('options').title === title){
						break;
					}
				}
				// ロケーション変更（hrefは、1スタートのため+1設定※href[0]は空白
				// ※非表示タブがカレントタブとして表示している場合は、無効
				if (href[index+1] !== '#'){
					location.href = href[index+1];
					/**
					 * IE6 限定：a （アンカー）のhref="javascript:void(0);"
					 * かつonClickイベントにて location.href を使用した場合は、
					 * 画面が更新されない。通常対処は、return false;であるが、
					 * この場合は、下記のコマンドでしか対応できない。（注意）
					 */
					window.event.returnValue=false;
				}
			}
			return false;
		}
	});

	// contents の高さ調整
	var baseHeight=1;

	if ($('#tabView').val() !== ''){
		$('div.tabs-header').css({"height":"0","display":"block"});	// タブ切替非表示
		baseHeight=1;	// 高さ調整
	}

	(function() {

		// タブ要素取得（class = hide 指定の要素を非表示）
		var tabs = $('.tabs-panels .panel .panel-body').map(
			function(i, e){
				if (e.className.match('hide')) {
					return true;
				} else {
					return false;
				};
			}).get();
		var elems = $('.tabs li').map(
			function(i,e) {
				if (tabs[i]){
					$(this).toggle();
				}
				return e;
			}).get();

		/**
		 * 初期選択タブの設定
		 * default = 0 のため、最初のタブは何もしない
		 */
		var index = $('#tabSelected').val();

		var t = $('#tabs');
		var tabs = t.tabs('tabs');

		if (window.chrome){
 			// 各タブの選択(xp版chrome)
			for (var count=0; count<tabs.length; count++){
				t.tabs('select', tabs[count].panel('options').title);
			}
			t.tabs('select', tabs[index].panel('options').title);
		} else {
			if (index!=0) {
				t.tabs('select', tabs[index].panel('options').title);
			}
		}

	})();

	init=true;

	/**
	 * resize Event
	 */
	$(window).resize(function() {
		$('#content').height($(window).height()-$('#top_title').height()-baseHeight);
		$('#tabs').trigger('_resize');
	});
	$(window).resize();

});
