/**
 * jquery report option
 */
;(function($) {

	$.extend({
		reportOption: {
		name:		'TenSelect',				// （必須）レポートオプションの確認
		jsonTemp:	[],							// （必須）検索条件情報_入力チェック前
		jsonString:	[],							// （必須）検索条件情報
		jsonHidden: [],							// （必須）親画面からの引き継ぎ情報
		jsonInit: [],							// （必須）検索条件初期情報
		caption: function(){					// （必須）タイトル
			return $('#reportname').val();
		},
		timeData : (new Date()).getTime(),
		dedefaultObjNum:	1,					// 初期化オブジェクト数
		initObjNum:	-1,
		initedObject: [],
		initializes : true,
		focusRootId:"cc",						// （キー移動時必須）キー移動イベントのルートとなるパネルのID
		initialize: function (reportno){		// （必須）初期化
			var that = this;
			// 引き継ぎ情報
			this.jsonHidden = $.getTargetValue();

			// 画面の初回基本設定
			this.setInitObjectState();

			// メッセージ一覧取得
			$.initMessageListData(reportno);

			// 初期検索条件設定
			//this.jsonInit = $.getInitValue();

			// 初期化するオブジェクト数設定
			this.initObjNum = this.dedefaultObjNum;

			var isUpdateReport = false;

			// 店舗コード
			$.setMeisyoCombo(that, reportno, $.id.SelTenpo, isUpdateReport);

			// 初期化終了
			this.initializes =! this.initializes;

			$('#btn_search').on("click", function(){
				var tencd = $.getInputboxValue($('#'+$.id.SelTenpo));
				if(!tencd || tencd == "-1"){
					$.showMessage('EX1119',["店舗コード"]);
					return false;
				}else{
					$('#login2').submit()
				}
			});

			// キーイベントの設定
			$.initKeyEvent(that);

			// ログ出力
			$.log(that.timeData, 'initialize:');
		},
		setInitObjectState: function(){	// 画面初期化時の項目制御
			var that = this;
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