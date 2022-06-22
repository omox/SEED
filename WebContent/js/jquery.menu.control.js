/**
 * @author Omoto_Yuki
 */
$(function() {

	var loadChart = function(){
		$.post('../Custom.do',{"ACTION":"GET"},function(result){
			// 初期値有無で展開レベル調整
			var optiondDefaultDepth = -1;
			if (result.length === 0){
				optiondDefaultDepth = 2;
			}
			$("#org").jOrgChart({
				chartElement : '#chart',
				dragAndDrop  : !$('#on_off').is(':checked'),
				defaultDepth : optiondDefaultDepth,
				source		 : result
			});
			inst = $("#org").getjOrgChart();
			if (inst) {
				// 初期表示時の階層展開レベルをクリア
				inst.options.defaultDepth = -1;
			}
		});
	};
	loadChart();

	// 保存鈕機能
	$('#bt_store').on("click", function(){
		$.post('../Custom.do',
			{
				"ACTION":"STORE",
				"MENUDATA":	$("#chart").html().replace(/(\n|\r|\t)/g,"")
				   + "\t" + $("#org").html().replace(/(\n|\r|\t)/g,"")
			}, function(result){
				$.messager.alert('お知らせ','メニュー情報を保存しました。');  
			} );
	});

	// 削除鈕機能
	$('#bt_delete').on("click", function(){
		$.messager.confirm("確認", "初期化しますか？", function(b){
			if (!b) return;
			$.post('../Custom.do', { "ACTION":"DELETE" }, function(result){
				$.messager.alert('お知らせ','メニュー情報を初期化しました。画面の更新を行ってください。');  
			} );
		});
	});


	// ロック機能
	$('#on_off').iphoneStyle({
		onChange: function(elem, value){
			// get jOrgChart instant
			var inst = $("#org").getjOrgChart();
			if (inst){
				inst.rebuild({
					chartElement : '#chart',
					dragAndDrop  : !$('#on_off').is(':checked'),
					defaultDepth : -1,
					source		 : $("#chart").html().replace(/(\n|\r|\t)/g,"")	
						  + "\t" + $("#org").html().replace(/(\n|\r|\t)/g,"")
				});
			}
		}
    });

	// レポート表示押下処理
	$('#chart').on("click", "a", function(){
		$.post('../Custom.do',
			{
				"ACTION":"SET",
				"MENUDATA":	$("#chart").html().replace(/(\n|\r|\t)/g,"")
				   + "\t" + $("#org").html().replace(/(\n|\r|\t)/g,"")
			}, function(result){} );
	});
});