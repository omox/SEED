$(function() {

	/** メンテナンス画面 */
	// プレビュー押下時
	$("#info_open").click(
		function(){
			$("#infoView_title").val($("#txt_title").val());
			$("#infoView_info").val($("#txt_info").val());

			var win = window.open(
				"about:blank",
				"InfoWindow",
				"width=600,height=500,resizable=yes,scrollbars=yes,stastu");

			var targetForm = document.forms["infoView"];

			targetForm.target = "InfoWindow";
			targetForm.action = "../Servlet/InfoView.do";
			targetForm.method = "post";
			targetForm.submit();

			win.focus();
			return false;
		}
	);

	/** ログイン画面、メニュー画面 */
	// お知らせの折り畳み
	$('a.info_mark').click(
		function() {
			$(this).next('a.info_title').click();
		}
	);

	// お知らせの折り畳み
	$('a.info_title').toggle(
		function() {
			$(this).next('div.info_content').hide();
			$(this).prev('a.info_mark').html("▼");
		},
		function() {
			$(this).next('div.info_content').show();
			$(this).prev('a.info_mark').html("▲");
		}
	);

	/** メニュー画面 */
	// お知らせエリアの高さ調整
	$(window).on('resize', function() {
		if ($("#menu_info").length && $("#info_scr").length) {
			var changeHeighet = $(window).height() * 0.4;
			$("#info_scr").css("max-height", changeHeighet + "px");
		}
	});

	var trees = $('.easyui-tree');
	if($.isFunction(trees.size)){
		for (var index=0; index<trees.size(); index++) {
			var treeChildren = $(trees[index]).tree('getChildren');
			if (treeChildren.length===1){
				$(trees[index]).tree('collapse',treeChildren[0].target);
			}
		}
	}
});
