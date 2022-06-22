$(function() {

	/**
	 * 戻るボタン
	 */
	$("#btn_back").click(function(){
		if ($(this).linkbutton('options').disabled)	return false;
		// 同期通信
		$.ajax({
			url: "../History",
			type: 'POST',
			async: false,
			data: {
				action :	"BACK",
				url :	window.location.href,
				sel :	(new Date()).getTime()
			},
			success: function(url){
				if (url !== null) {
					parent.document.location = url;
				}
			}
		});
	});

	/**
	 * 進むボタン
	 */
	$("#btn_fowrward").click(function(){
		if ($(this).linkbutton('options').disabled)	return false;
		// 同期通信
		$.ajax({
			url: "../History",
			type: 'POST',
			async: false,
			data: {
				action :	"FOWRWARD",
				url :	window.location.href,
				sel :	(new Date()).getTime()
			},
			success: function(url){
				if (url !== null) {
					parent.document.location = url;
				}
			}
		});
	});

	// 同期通信
	$.ajax({
		url: "../History",
		type: 'POST',
		async: false,
		data: {
			action :	"STATUS",
			url :	window.location.href,
			sel :	(new Date()).getTime()
		},
		success: function(data){
			switch (data) {
			case "0":
				$('#btn_back').linkbutton('disable');
				$('#btn_fowrward').linkbutton('disable');
				break;
			case "1":
				$('#btn_back').linkbutton('disable');
				$('#btn_fowrward').linkbutton('enable');
				break;
			case "2":
				$('#btn_back').linkbutton('enable');
				$('#btn_fowrward').linkbutton('enable');
				break;
			case "3":
				$('#btn_back').linkbutton('enable');
				$('#btn_fowrward').linkbutton('enable');
				break;
			case "4":
				$('#btn_back').linkbutton('enable');
				$('#btn_fowrward').linkbutton('disable');
				break;
			default:
				$('#btn_back').linkbutton('disable');
				$('#btn_fowrward').linkbutton('disable');
				break;
			}
		}
	});

});
