/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'Login2',				// （必須）レポートオプションの確認
		jsonTemp:	[],							// （必須）検索条件情報_入力チェック前
		jsonString:	[],							// （必須）検索条件情報
		jsonHidden: [],							// （必須）親画面からの引き継ぎ情報
		jsonInit: [],							// （必須）検索条件初期情報
		caption: function(){					// （必須）タイトル
			return $('#reportname').val();
		},
		timeData : (new Date()).getTime(),
		dedefaultObjNum:	0,					// 初期化オブジェクト数
		initObjNum:	-1,
		initedObject: [],
		initializes : true,
		focusRootId:"cc",						// （キー移動時必須）キー移動イベントのルートとなるパネルのID
		initialize: function (reportno){		// （必須）初期化
			var that = this;

			// 初期化するオブジェクト数設定
			this.initObjNum = this.dedefaultObjNum;

			// 初期化終了
			this.initializes =! this.initializes;

			// キーイベントの設定
			$.initKeyEvent(that);

			$('#btn_type_master').on("click", function(){
				window.location.href="../Servlet/Login2.do?MenuKbn=4";
			});
			$('#btn_type_tokhat').on("click", function(){
				window.location.href="../Servlet/Login2.do?MenuKbn=5";
			});

			// ログ出力
			$.log(that.timeData, 'initialize:');
		},
		initCondition: function (){	// 条件初期値セット
			var that = this;
			// 初期化項目
		},
		validation: function (){	// （必須）批准
			var that = this;
			// EasyUI のフォームメソッド 'validate' 実施
			var rt = $($.id.toolbarform).form('validate');

			// 入力エラーなしの場合に検索条件を格納
			if (rt == true) that.jsonString = that.jsonTemp.slice(0);
			// 入力チェック用の配列をクリア
			that.jsonTemp = [];

			return rt;
		},
		success: function(reportno, sortable, btnId){	// （必須）正処理
			if (sortable) sortable=1; else sortable=0;
			var that = this;
		},
		loadSuccessFunc:function(id, data){				// 画面遷移
			var that = this;

		},
		getRecord: function(){		// （必須）レコード件数を戻す
			var data = $($.id.gridholder).datagrid('getData');
			if (data == null) {
				return 0;
			} else {
				return data.total;
			}
		},
		getJSONString : function(){		// （必須）JSON形式の文字列
			return this.jsonString;
		},
	} });
})(jQuery);