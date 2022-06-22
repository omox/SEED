/**
 * jquery sub window option
 * 数値展開方法(ST020)
 * TG016からのみ呼び出す想定
 */
;(function($) {

$.extend({

	winST020: {
		name: 'Out_ReportwinST020',
		prefix:'_tenkai',
		suffix:'_winST020',
		initializes: true,		// 初期化フラグ（全体）
		initializesCond: true,	// 初期化フラグ（条件用）
		lastIndex: -1,			// 編集位置（行）
		sortName: '',			// ソート項目名
		sortOrder: '',			// ソート順
		timeData : (new Date()).getTime(),
		callreportno:"",		// 呼出し元レポートNo
		callBtnid: "",			// 呼出ボタンID情報
		callreportHidden:[],	// 呼出し元レポートからの引き継ぎ情報
		focusRootId:"_winST020",// （キー移動時必須）キー移動イベントのルートとなるパネルのID
		focusParentId:"",		// （キー移動時必須）現在フォーカスがあたっている項目の親となるパネルのID
		focusGridId:"",			// （キー移動時必須）現在フォーカスがあたっているDataGridのID
		updateFlg:false,		// 登録ボタン押下済
		befData:{},				// 検索結果保持用
		repKbn:'',				// 帳票タイプ
		judgeRepType: {
			ref 		: false		// 参照
		},
		init: function(js, canUpdate) {
			var that = this;
			if(!that.initializes) return false;

			// 呼出し元情報取得
			that.callreportno = js.name;
			that.callreportHidden = js.jsonHidden;
			that.judgeRepType.ref = js.judgeRepType.ref;
			that.getData(js);
			// 呼出しボタンイベント設定
			$('[id^=btn'+that.prefix+']').each(function(){
				var id = $(this).attr('id');
				$('#'+id).click(function() { that.Open(this); });
			});
			// キャンセル
			$('#'+$.id.btn_cancel+that.suffix).on("click", that.Cancel);
			if(that.judgeRepType.ref){
				$.setInputBoxDisable($('#'+$.id.btn_select+that.suffix)).hide();
			}else{
				// 選択
				$('#'+$.id.btn_select+that.suffix).on("click", that.Select);
			}
			$.setRadioInit2({}, $.id.rad_tenkaikbn+that.suffix, that);
			$.setRadioInit2({}, $.id.rad_jskptnsyukbn+that.suffix, that);
			$.setRadioInit2({}, $.id.rad_jskptnznenmkbn+that.suffix, that);
			$.setRadioInit2({}, $.id.rad_jskptnznenwkbn+that.suffix, that);
			that.setData(that.befData);

			// ｳｲﾝﾄﾞｳ設定
			$('#'+that.suffix).window({
				iconCls:'icon-search',
				modal:true,
				collapsible:false,
				minimizable:false,
				maximizable:false,
				closed:true,
				cinline:false,
				zIndex:90000,
				onBeforeOpen:function(){
					// ウインドウ展開中リサイズイベント無効化
					$.reg.resize = false;
					js.focusParentId = that.suffix;
					that.getData(js);
				},
				onOpen:function(){
					$.setFocusFirst($('#'+that.focusRootId));
				},
				onBeforeClose:function(){
					// ウインドウ展開中リサイズイベント有効化
					$.reg.resize = true;
					if(that.updateFlg){
						$('#'+js.focusRootId).find("[col=F154]").val($.getInputboxValue($('[name='+$.id.rad_tenkaikbn+that.suffix+']')));		// F46	TENKAIKBN	展開方法
						$('#'+js.focusRootId).find("[col=F157]").val($.getInputboxValue($('[name='+$.id.rad_jskptnsyukbn+that.suffix+']')));	// F47	JSKPTNSYUKBN	実績率パタン数値

						var radio = $.getInputboxValue($('[name='+$.id.rad_jskptnznenmkbn+that.suffix+']'));
						if ($.isEmptyVal(radio)) {
							$('#'+js.focusRootId).find("[col=F158]").val('0');	// F48	JSKPTNZNENMKBN	実績率パタン前年同月
						} else {
							$('#'+js.focusRootId).find("[col=F158]").val(radio);	// F48	JSKPTNZNENMKBN	実績率パタン前年同月
						}

						radio = $.getInputboxValue($('[name='+$.id.rad_jskptnznenwkbn+that.suffix+']'));
						if ($.isEmptyVal(radio)) {
							$('#'+js.focusRootId).find("[col=F159]").val('0');	// F49	JSKPTNZNENWKBN	実績率パタン前年同週
						} else {
							$('#'+js.focusRootId).find("[col=F159]").val(radio);	// F49	JSKPTNZNENWKBN	実績率パタン前年同週
						}

						$($.id.hiddenChangedIdx).val("1");	// 元の帳票に更新フラグ
						if($.isFunction(js.changeInputboxFunc)){
							js.changeInputboxFunc(js, that.name, that.befData, $('#'+that.focusRootId));
						}
					}else{
						that.Clear();						// 変更内容初期化
					}
					js.focusParentId = js.focusRootId;
				},
				onClose:function(){
					$('#'+js.focusParentId).find('#'+that.callBtnid).focus();
				}
			});

			// チェックボックスの設定
			$.initCheckboxCss($("#"+that.focusRootId));
			// キーイベントの設定
			$.initKeyEvent(that);

			that.initializes = !that.initializes;
		},
		Open: function(obj) {
			if ($(obj).is("disabled","disabled"))	return false;
			var that = $.winST020;
			that.callBtnid = $(obj).attr('id');

			// 画面情報表示
			$('#'+that.focusRootId).find('[id]').filter('span').each(function(){
				var refid = $(this).attr('id').replace(that.suffix, '');
				if($('#'+refid)){
					$(this).text($.getInputboxText($('#'+refid)));
				}
			});

			// window 表示
			$('#'+that.suffix).window('open');
		},
		Clear:function(){
			var that = $.winST020;
			that.initializesCond = true;
			that.updateFlg =false;
			that.setData(that.befData);
			that.initializesCond = false;
		},
		Cancel:function(){
			var that = $.winST020;

			$('#'+that.suffix).window('close');
			return true;
		},
		Select: function(){
			var that = $.winST020;
			that.updateFlg =true;
			$('#'+that.suffix).window('close');
			return true;
		},
		setData: function(row){		// データ表示
			var that =  $.winST020;
			var columns = Object.getOwnPropertyNames(row);
			for ( var col in columns ) {
				var targets = $('#'+that.focusRootId).find('[name='+('rad_'+columns[col]).toLowerCase()+that.suffix+']');
				if(targets.length > 0){
					$(targets).prop('checked', false).closest("label").removeClass("selected_radio");
					if(row[columns[col]]*1 > 0){
						$.setInputboxValue(targets.eq(0), row[columns[col]]);
					}
				}
			}
		},
		getData: function(js){		// データ取得
			var that = $.winST020;
			that.befData = {
				TENKAIKBN:		js.getColValue("F154"),			// F46	TENKAIKBN	展開方法
				JSKPTNSYUKBN: 	js.getColValue("F157"),			// F47	JSKPTNSYUKBN	実績率パタン数値	TODO
				JSKPTNZNENMKBN:	js.getColValue("F158"),			// F48	JSKPTNZNENMKBN	実績率パタン前年同月	TODO
				JSKPTNZNENWKBN:	js.getColValue("F159"),			// F49	JSKPTNZNENWKBN	実績率パタン前年同週	TODO
			};
		},
		changeInputboxFunc:function(that, id, newValue, obj, all){
			if(obj.attr("name")==='rad_jskptnznenwkbn'+that.suffix){
				$('#'+that.focusRootId).find('[name='+'rad_jskptnznenmkbn'+that.suffix+']').prop('checked', false).closest("label").removeClass("selected_radio");
			}
			if(obj.attr("name")==='rad_jskptnznenmkbn'+that.suffix){
				$('#'+that.focusRootId).find('[name='+'rad_jskptnznenwkbn'+that.suffix+']').prop('checked', false).closest("label").removeClass("selected_radio");
			}
		}
	}
});

})(jQuery);