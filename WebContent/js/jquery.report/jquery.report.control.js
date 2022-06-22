/**
 * レポート情報の共通部分と固有定義の初期化
 * $.exDate は、exdate.js の読み込みが必須。
 */

/**
 * デバッグ用
 **/
if (!('console' in window)) {
	window.console = {};
	window.console.log = function(str){
		return str;
	};
}

// 配列用 indexOf() 実装（IE8）
if (!Array.indexOf) {
	Array.prototype.indexOf = function(o) {
		for (var i in this) {
			if (this[i] == o) {
				return i;
			}
		}
		return -1;
	}
}

var blueLog = {};//console.log.bind(console, "%c%s", "color:blue;font-weight:bold;");

;(function($) {

$.report = function(options) {

	var defaults = {
		reg : {
			excel	:	"../ExcelGenerate",	// Excel出力用
			jqgrid	:	"../JQGridJSON",	// jqGrid 用
			jqeasy	:	"../JQEasyJSON",	// jquery.easyUI 用
			jqutil	:	"../JQEasyUtil",	// jqGrid util 用
			easy	:	"../EasyToJSON",	// jquery.easyUI 用
			srcexcel:	"../SearchExcelGenerate",	// Excel出力用(直接検索)
			upload	:	"../Upload",			// Upload処理用
			history	:	"../History",		// 画面履歴用
			ftp		:	"../FileWriterForFtp",
			debug 	:	true,				// デバッグ（有効:true, 無効:false)
			search	:	false,				// 初期表示時、検索実行（有効:true, 無効:false)
			resize	:	true,				// 画面リサイズ処理(有効:true, 無効:false)
			changeReportByTabs 	:	false,	// タブ遷移時の条件引継ぎ機能（有効:true, 無効:false)
			TestLog	:	false
		},

		_ua : (function(){
			// version support
			return {
				ltIE6:typeof window.addEventListener == "undefined" && typeof document.documentElement.style.maxHeight == "undefined",
				ltIE7:typeof window.addEventListener == "undefined" && typeof document.querySelectorAll == "undefined",
				ltIE8:typeof window.addEventListener == "undefined" && typeof document.getElementsByClassName == "undefined",
				ie:document.uniqueID,
				firefox:window.globalStorage,
				opera:window.opera,
				webkit:!document.uniqueID && !window.opera && !window.globalStorage && window.localStorage,
				mobile:/android|iphone|ipad|ipod/i.test(navigator.userAgent.toLowerCase())
			};
		})(),

		_setDatagridNoIE : function (id, list){
			// ブラウザのUAを小文字で取得
			var userAgent = window.navigator.userAgent.toLowerCase();

			// IEの判定
			var isIE = (userAgent.indexOf('msie') >= 0 || userAgent.indexOf('trident') >= 0);
			if (!isIE){
				// chrome で全選択後の再表示時に各項目の✔が消える現象対応
				var dg = $('#'+id).combogrid('grid');
				var data = dg.datagrid('getData');
				var vals = $('#'+id).combogrid('getValues');
				if (data.total===vals.length){
					var data = dg.datagrid('getData');
					for(var i=0;i<data.total;i++){
						// データをセットする
						data.rows[i]['TY'] = $.id.checkBoxOnData;
						if ($.inArray(data.rows[i]['VALUE'], list)===-1){
							data.rows[i]['TP'] = $.id.checkBoxOnData;
						}
						data.rows[i]['ZY'] = $.id.checkBoxOnData;
						if ($.inArray(data.rows[i]['VALUE'], list)===-1){
							data.rows[i]['ZP'] = $.id.checkBoxOnData;
						}
						data.rows[i]['ZC'] = $.id.checkBoxOnData;
						data.rows[i]['ZS'] = $.id.checkBoxOnData;
						dg.datagrid('refreshRow', i);	// 変更内容を反映する
					}
				}
			}
		},

		log : function(baseTime, comment){
			if ($.reg.debug) {
				var message = comment + ((new Date()).getTime() - baseTime) + ' ms';
				status = message;
				console.log(message);
			}
		},
		log2 : function() {
			if (window.console && console.log) {
				return console.log.bind(console);
			}
		},
		/* Web商談 */
		trim2 : function(str){
			return str.replace(/(^[ 　]+|[ 　]+$)/g, '');
		},

		getReportNumber : function (reportName) {
			// レポート名からレポート定義配列の位置取得
			for (var i=0; i < this.report.length; i++) {
				if ($.report[i].name === reportName) {
					return i;
				}
			}
		},

		paddingLeft : function(val, len, pad){
			if (undefined===len) return val;
			if (undefined===pad) pad = "0";
			var leftval = "";
			for(;leftval.length < len;leftval+=pad);
			return (leftval+val).slice(-len);
		},

		// 小数点切り捨て：小数点第n位まで残す
		floorDecimal : function(val, n){
			return Math.floor( val * Math.pow( 10, n ) ) / Math.pow( 10, n );
		},
		// 小数点切り上げ：小数点第n位まで残す
		ceilDecimal : function(val, n){
			return Math.ceil( val * Math.pow( 10, n ) ) / Math.pow( 10, n );
		},
		// 小数点四捨五入：小数点第n位まで残す
		roundDecimal : function(val, n){
			return Math.round( val * Math.pow( 10, n ) ) / Math.pow( 10, n );
		},

		convertDate : function(strdt, addday){
			var val = strdt.replace(/\//g, "");
			var addstr = '';
			if(val.length===6){
				addstr = '20';
				if(val.substr(0,2)*1 > 50){
					addstr = '19';
				}
			}
			var dt = $.exDate(addstr+val, 'yyyymmdd');
			dt.setDate(dt.getDate() + addday);
			return dt.toChar('yyyymmdd');
		},
		convDate: function(dt, add20){
			dt = $.getParserDt(dt, add20);
			return new Date( dt.substr(0,4), dt.substr(4,2)*1-1, dt.substr(6,2) );
		},
		dateFormat: function(dObj, format, sep){
			if(sep===undefined){sep="";}
			var y = dObj.getFullYear()+'';
			var m = ('0'+(dObj.getMonth()+1)).substr(-2);
			var d = ('0'+dObj.getDate()).substr(-2);
			if(format==='yyyymmdd'){
				return y+sep+m+sep+d;
			}else if(format==='mmdd'){
				return m+sep+d;
			}
			return y.substr(-2)+sep+m+sep+d;
		},
		getDateDiffDay: function(sdate, edate){
			return Math.ceil((edate - sdate) / 86400000);
		},
		// YYYYMMDD前提
		getDateAddDay: function(dt, addday){
			var dObj = $.convDate(dt , false);
			dObj.setDate(dObj.getDate() + 1);
			return $.dateFormat(dObj, 'yyyymmdd');
		},

		getFormat : function(value, format){
			// 数値->文字フォーマット変換
			if (value == null || value.length < 1) return '';
			// 形式未指定時の初期値
			if (format == null) format='#,###';
			return $.formatNumber(value, {format:format, locale:"jp"});
		},

		getFormatDt : function(value, add20, addWeek){
			// 数値->文字フォーマット変換
			if (undefined===add20) add20 = false;
			if (undefined===addWeek) addWeek = false;
			if (undefined===value || null===value || ''===value) return '';
			var dt = null;
			var val = '';
			if(add20){
				if(value.match(/(^[0-9]{4}$)/)){
					val = value.substr(0,2) + "/" + value.substr(2,2);
				}else if(value.match(/(^[0-9]{6}$)/)){
					val = value.substr(0,2) + "/" + value.substr(2,2) + "/" + value.substr(4,2);
					dt = $.getParserDt(value, true);
				}else{
					val = value;
				}
			}else{
				if(value.match(/(^[0-9]{6}$)/)){
					val = value.substr(2,2) + "/" + value.substr(4,2);
				}else if(value.match(/(^[0-9]{8}$)/)){
					val = value.substr(2,2) + "/" + value.substr(4,2) + "/" + value.substr(6,2);
					dt = value;
				}else if(value.match(/(^[0-9]{14}$)/)){
					val = value.substr(2,2) + "/" + value.substr(4,2) + "/" + value.substr(6,2) + " " + value.substr(8,2) + ":" + value.substr(10,2);
					dt = value.substr(0,8);
				}else{
					val = value;
				}
			}
			if(dt!==null && addWeek){
				var sWeek = [ "(日)", "(月)", "(火)", "(水)", "(木)", "(金)", "(土)" ];
				var dObj = $.convDate(dt);
				var wDay = dObj.getDay();
				val += sWeek[wDay];
			}
			return val;
		},
		getFormatDt2: function(value, addWeek){
			// 数値->文字フォーマット変換
			if (undefined===addWeek) addWeek = false;
			if (undefined===value || null===value || ''===value) return '';
			var dt = null;
			var val = '';
			if(/(^[0-9]{6}$)/.test(value)){
				val = value.substr(0,4) + "/" + value.substr(4,2);
			}else if(/(^[0-9]{8}$)/.test(value)){
				val = value.substr(0,4) + "/" + value.substr(4,2) + "/" + value.substr(6,2);
				dt = value;
			}else if(/(^[0-9]{14}$)/.test(value)){
				val = value.substr(0,4) + "/" + value.substr(4,2) + "/" + value.substr(6,2) + " " + value.substr(8,2) + ":" + value.substr(10,2);
				dt = value.substr(0,8);
			}else{
				val = value;
			}
			if(dt!==null && addWeek){
				var sWeek = [ "(日)", "(月)", "(火)", "(水)", "(木)", "(金)", "(土)" ];
				var dObj = $.convDate(dt);
				var wDay = dObj.getDay();
				val += sWeek[wDay];
			}
			return val;
		},
		getFormatWeek : function(value, add20){
			// 数値->文字フォーマット変換
			if (undefined===add20) add20 = true;
			if (undefined===value || null===value || ''===value) return '';
			var dt = null;
			var val = '';
			if(add20){
				if(value.match(/(^[0-9]{6}$)/)){
					dt = $.getParserDt(value, true);
				}
			}else{
				if(value.match(/(^[0-9]{8}$)/)){
					dt = value;
				}else if(value.match(/(^[0-9]{14}$)/)){
					dt = value.substr(0,8);
				}
			}
			if(dt!==null){
				var sWeek = [ "日", "月", "火", "水", "木", "金", "土" ];
				var dObj = $.convDate(dt);
				var wDay = dObj.getDay();
				val += sWeek[wDay];
			}
			return val;
		},
		getParserDt : function(str, add20){
			if (undefined===str || null===str || ''===str) return '';
			if (undefined===add20) add20 = true;
			var val = str.replace(/\//g, "");
			if(add20){
				var addstr = '20';
				if(val.substr(0,2)*1 > 50){
					addstr = '19';
				}
				if(val.match(/^([0-9]{2})(0[1-9]|1[012])(0[1-9]|[12][0-9]|3[01])$/)){
					return addstr+val;
				}else if(val.match(/^([0-9]{2})(0[1-9]|1[012])(0[1-9]|[12][0-9]|3[01])\((日|月|火|水|木|金|土)\)$/)){
					return  (addstr+val).substr(0, 8);
				}
			}else{
				if(val.match(/^([0-9]{2})(0[1-9]|1[012])(0[1-9]|[12][0-9]|3[01])$/)){
					return val;
				}else if(val.match(/^([0-9]{2})(0[1-9]|1[012])(0[1-9]|[12][0-9]|3[01])\((日|月|火|水|木|金|土)\)$/)){
					return  val.substr(0, 6);
				}
			}
			if(val.match(/^([0-9]{4})(0[1-9]|1[012])(0[1-9]|[12][0-9]|3[01])\((日|月|火|水|木|金|土)\)$/)){
				return  (val).substr(0, 8);
			}
			return val.replace(/[\_\(\)]/g, '');
		},
		getFormatYm : function(value){
			// 数値->文字フォーマット変換
			if (undefined===value || null===value || ''===value) return '';
			var val = '';
			if(value.match(/(^[0-9]{4}$)/)){
				val = value.substr(0,2) + "/" + value.substr(2,2);
			}else if(value.match(/(^[0-9]{6}$)/)){
				val = value.substr(2,2) + "/" + value.substr(4,2);
			}else{
				val = value;
			}
			return val;
		},
		getParserYm : function(str, add20){
			if (undefined===str || null===str || ''===str) return '';
			if (undefined===add20) add20 = true;
			var val = str.replace(/\//g, "");
			var addstr = "";
			if(add20){
				addstr = '20';
				if(val.substr(0,2)*1 > 50){
					addstr = '19';
				}
			}
			if(val.match(/^([0-9]{2})(0[1-9]|1[012])$/)){
				return addstr + val;
			}
			return val;
		},
		getFormatMmdd : function(value){
			// 数値->文字フォーマット変換
			if (undefined===value || null===value || ''===value) return '';
			var val = '';
			if(value.match(/(^[0-9]{4}$)/)){
				val = value.substr(0,2) + "/" + value.substr(2,2);
			}else{
				val = value;
			}
			return val;
		},
		getParserMmdd : function(value){
			if (undefined===value || null===value || ''===value) return '';
			return value.replace(/\//g, "");
		},

		getFormatHhmm : function(value){
			// 数値->文字フォーマット変換
			if (undefined===value || null===value || ''===value) return '';
			var val = '';
			if(value.match(/(^[0-9]{1}$)/)){
				val = "0" + value + ":00";

			}else if(value.match(/(^[0-9]{2}$)/)){
				val = value + ":00";

			}else if(value.match(/(^[0-9]{3}$)/)){
//				val = value.substr(0,2) + ":" + value.substr(2,1) + "0";
				val = "0" + value.substr(0,1) + ":" + value.substr(1,2);

			}else if(value.match(/(^[0-9]{4}$)/)){
				val = value.substr(0,2) + ":" + value.substr(2,2);

			}else{
				val = value;
			}
			return val;
		},
		getParserHhmm : function(value){
			if (undefined===value || null===value || ''===value) return '';
			return value.replace(/:/g, "");
		},

		getFormatPrompt : function(value, prompt){
			if (undefined===value || null===value || ''===value) return '';
			var newValue = '';
			var j=0;
			for (var i=0; i<prompt.length; i++){
				var c = prompt.substr(i, 1);
				if (c==="#"){
					newValue = newValue + value.substr(j, 1);
					j++;
				} else {
					newValue  = newValue + c;
				}
			}
			return newValue;
		},
		// numberbox前提
		getParserPrompt:function(value){
			if (undefined===value || null===value || ''===value) return '';
			if (value===null) return '';
			if (value==='') return '';
			return value.replace(/[^0-9\.]/g,"");
		},
		getParserCode:function(value){
			if (undefined===value || null===value || ''===value) return '';
			if (value===null) return '';
			if (value==='') return '';
			return value.replace(/[^0-9]/g,"");
		},
		getFormatLPad : function(value, len){
			if (undefined===value || null===value || ''===value) return '';
			if (! (''+value).match(/^[0-9]*$/)) return '';
			if ((''+value).length > len*1) return value;
			return $.paddingLeft(value, len);
		},
		getParserLPad : function(value){
			if (undefined===value || null===value || ''===value) return '';
			if (! (''+value).match(/^[0-9]*$/)) return '';
			if(value*1===0){
				return '0';
			}
			return value*1;
		},

		getJSONObject : function (jsonArray, idValue) {
			// JSON 配列から指定されたIDのオブジェクトを戻す
			// 該当がない場合は、該当なし。
			// jsonArray 配列判定
			if(jsonArray) {
				for (var i = 0; i < jsonArray.length; i++) {
					if (jsonArray[i].id === idValue) {
						return json = jsonArray[i];
					}
				}
			}
		},

		getJSONValue : function(jsonArray, id){
			var json = $.getJSONObject(jsonArray, id);
			if(json){
				return json.value;
			}
			return "";
		},

		getJSONText : function(jsonArray, id){
			var json = $.getJSONObject(jsonArray, id);
			if(json){
				return json.text;
			}
			return "";
		},

		setJSONObject : function (jsonArray, idValue, value, text) {
			// JSON 配列から指定されたIDにデータをセット
			// 該当がない場合は、追加する。
			// jsonArray 配列判定
			var flag = true;
			if(jsonArray) {
				for (var i = 0; i < jsonArray.length; i++) {
					if (jsonArray[i].id === idValue) {
						jsonArray[i].value = value;
						jsonArray[i].text = text;
						flag = false;
						break;
					}
				}
				if (flag) {
					// 存在しない場合に作成
					var json = {
						"id"	:	idValue,
						"value"	:	value,
						"text"	:	text
					};
					jsonArray.push(json);
				}
			}
		},

		// ラジオボタン初期化：検索画面用
		setRadioInit : function(jsonHidden, id, that) {
			// Radio 要素の初期化
			// 初期化情報取得
			var json = $.getJSONObject(jsonHidden, id);
			if (json){
				// 初期化
				$('input[name="'+id+'"]').val([json.value]);
			}
			if(that){
				$('input[name="'+id+'"]').change(function() {
					$.removeErrState();
				});
				if ($.inArray(id, that.initedObject) < 0){
					that.initedObject.push(id);
				}
				// 初期表示検索処理
				$.initialSearch(that);
			}
		},
		// ラジオボタン初期化：更新画面用
		setRadioInit2 : function(jsonHidden, id, that) {
			// Radio 要素の初期化
			// 初期化情報取得
			var json = $.getJSONObject(jsonHidden, id);
			if (json){
				// 初期化
				$('input[name="'+id+'"]').val([json.value]);
			}
			if(that){
				var suffix = that.suffix ? that.suffix : '';
				// 更新項目で参照表示かどうか
				var isRefer = $.isReferUpdateInput(that, $('input[name="'+id+'"]'), true);
				// 当項目を変更した際に、実行する基本の処理
				var changeFunc1 = null;
				if(!isRefer){
					changeFunc1 = function(obj){
						if(that.queried && $($.id.hiddenChangedIdx).is(':enabled')){
							$($.id.hiddenChangedIdx+suffix).val("1");
						}
					};
				}
				$('input[name="'+id+'"]').change(function(e) {
					var obj = $(e.target);
					if(changeFunc1!==null){ changeFunc1(obj);}
					$.removeErrState();
					// 当項目を変更した際に、値をセットする項目がある場合の処理（セット先の項目は、HTMLタグとして、for_inp=当項目のID_列名）
					if($.isFunction(that.changeInputboxFunc)){
						var newValue = $.getInputboxValue(obj);
						that.changeInputboxFunc(that, id, newValue, obj);
					}
				});
				if(isRefer){ $.setInputStateRefer(that, $('input[name="'+id+'"]')); }

				if(suffix===''){
					if ($.inArray(id, that.initedObject) < 0){
						that.initedObject.push(id);
					}
					$.initialDisplay(that);
				}
			}
		},

		// チェックボックス初期化：検索画面用
		setCheckboxInit : function(jsonHidden, id, that) {
			// Checkbox 要素の初期化
			// 初期化情報取得
			var json = $.getJSONObject(jsonHidden, id);
			if (json && json.value === $.id.value_on){
				// 初期化
				$('#'+id).prop("checked",true);
			}else{
				$('#'+id).prop("checked",false);
			}
			$('#'+id).change(function() {
				$.removeErrState();
			});

			$('input[name="'+id+'"]').change(function(e) {
				var obj = $(e.target);
				// 当項目を変更した際に、値をセットする項目がある場合の処理（セット先の項目は、HTMLタグとして、for_inp=当項目のID_列名）
				if($.isFunction(that.changeInputboxFunc)){
					var newValue = $.getInputboxValue(obj);
					that.changeInputboxFunc(that, id, newValue, obj);
				}
			});

			if(that){
				if ($.inArray(id, that.initedObject) < 0){
					that.initedObject.push(id);
				}
				// 初期表示検索処理
				$.initialSearch(that);
			}
		},
		// チェックボックス初期化：登録項目用
		setCheckboxInit2 : function(jsonHidden, id, that) {
			// Checkbox 要素の初期化
			// 初期化情報取得
			var json = $.getJSONObject(jsonHidden, id);
			if (json && json.value === $.id.value_on){
				// 初期化
				$('#'+id).prop("checked",true);
			}else{
				$('#'+id).prop("checked",false);
			}
			if(that){
				// 更新項目で参照表示かどうか
				var isRefer = $.isReferUpdateInput(that, $('#'+id), true);

				var suffix = that.suffix ? that.suffix : '';
				// 当項目を変更した際に、実行する基本の処理
				var changeFunc1 = null;
				if(!isRefer){
					changeFunc1 = function(obj){
						if(that.queried && $($.id.hiddenChangedIdx).is(':enabled')){
							$($.id.hiddenChangedIdx+suffix).val("1");
						}
					};
				}
				$('#'+id).change(function(e) {
					var obj = $(e.target);
					if(changeFunc1!==null){ changeFunc1(obj);}
					$.removeErrState();
					// 当項目を変更した際に、値をセットする項目がある場合の処理（セット先の項目は、HTMLタグとして、for_inp=当項目のID_列名）
					if($.isFunction(that.changeInputboxFunc)){
						var newValue = $.getInputboxValue(obj);
						that.changeInputboxFunc(that, id, newValue, obj);
					}
				});
				if(isRefer){ $.setInputStateRefer(that, $('#'+id)); }

				if(suffix===''){
					if ($.inArray(id, that.initedObject) < 0){
						that.initedObject.push(id);
					}
					$.initialDisplay(that);
				}
			}
		},

		// 各帳票の情報を共通箇所に表示
		initReportInfo : function(id, title, tabtxt) {
			if(id){
				parent.$("#disp_report_id").text(id);
			}
			if(title){
				parent.$("#content").panel({"title":title});
			}
			if(tabtxt){
				parent.$("#tabs").find('.tabs-header .tabs li').filter('.tabs-selected').find('.tabs-title').text(tabtxt);
			}
		},


		initCheckboxCss : function(parentobj, dispOnly) {
			// チェックボックス
			// 親要素の追加
			//parentobj.find(":checkbox").wrap("<span class='chk_parent'/>");
			parentobj.find(".datagrid-cell-check").wrap("<span class='chk_parent'/>");

			if(dispOnly){
				parentobj.find(":checkbox").filter(":visible").after("<span class='chk_dummy'/>");
			}else{
				parentobj.find(":checkbox").after("<span class='chk_dummy'/>");
			}
			parentobj.find(".chk_dummy").on("click", function(e){
				var chkbox = $(this).prev(":checkbox").eq(0);
				if(chkbox.is("[disabled=disabled]")||chkbox.is("[readonly=readonly]")) return false;
				chkbox.click();
			});
		},

		afterEditAddCheckbox : function(parentobj) {
			// 行編集後のチェックボックス追加
			parentobj.find(".datagrid-cell-check").wrap("<span class='chk_parent'/>");	// 親要素の追加
			parentobj.find(":checkbox").after("<span class='chk_dummy'/>");
		},

		// 更新項目が参照として表示すべきか否か判断
		isReferUpdateInput: function(that, target, isUpdateReport){
			var isRefer = false;	// 更新項目で参照タイプ
			if(isUpdateReport){
				if($('#reportYobi1').val()==='1'){						// 予備情報1(reportYobi1)が1の場合
					isRefer = true;
				}else if(that.judgeRepType && that.judgeRepType.ref){	// 帳票タイプがあり、参照がtrueの場合
					isRefer = true;
				}else if(target && !target.is(":enabled")){					// 項目の設定がそもそも使用不可の場合
					isRefer = true;
				}
			}
			return isRefer;
		},
		setInputStateRefer: function(that, target){
			var obj = $.getInputboxTextbox(target);
			target.attr('readonly', 'readonly');
			obj.attr('readonly', 'readonly');
			if(target.is(':radio')){
				target.attr('disabled', 'disabled');
				target.click(function(e) {
					return false;
				});
			}else if(target.is(':checkbox')){
				target.attr('disabled', 'disabled');
				target.click(function(e) {
					return false;
				});
			}
		},
		// id_inp宣言の入力項目を共通で設定する
		setInputbox: function(that, reportno, id, isUpdateReport){
			var idx=-1;
			// 初期化情報取得
			var json = $.getJSONObject(that.jsonHidden, id);
			var suffix = that.suffix ? that.suffix : '';

			// 更新項目で参照表示かどうか
			var isRefer = $.isReferUpdateInput(that, $('#'+id), isUpdateReport);

			// 当項目を変更した際に、実行する基本の処理
			var changeFunc1 = null;
			if(!isRefer && isUpdateReport){
				changeFunc1 = function(obj){
					if(idx > 0 && that.queried && $(obj).attr("readonly")!=="readonly"){
						$($.id.hiddenChangedIdx+suffix).val("1");
					}
				};
			}
			// 当項目を変更した際に、値をセットする項目がある場合の処理（セット先の項目は、HTMLタグとして、for_inp=当項目のID_列名）
			// ※これに関しては参照系でも初期値の場合などに動作の必要あり
			var changeFunc2 = null;
			if($.isFunction(that.changeInputboxFunc)){
				changeFunc2 = function(newValue, obj){
					that.changeInputboxFunc(that, id, newValue, obj);
				};
			}else{
				if($('[for_inp^='+id+'_]').length > 0){
					changeFunc2 = function(newValue, obj){
						var param = [{"value":newValue}];
						$.getsetInputboxRowData(reportno, 'for_inp', id, param, that);
					};
				}
			}
			// 下位項目がある場合の処理
			var changeFunc3 = null;
			if(!isRefer){
				var childId = null;
				var prefix = /(^\w+_)/g.exec(id)[0];
				if(id === prefix+$.id_inp_suffix.bmncd){
					childId = prefix + $.id_inp_suffix.daicd;
				}else if(id === prefix+$.id_inp_suffix.daicd){
					childId = prefix + $.id_inp_suffix.chucd;
				}else if(id === prefix+$.id_inp_suffix.chucd){
					childId = prefix + $.id_inp_suffix.shocd;
				}else if(id === prefix+$.id_inp_suffix.shocd){
					childId = prefix + $.id_inp_suffix.sshocd;
				}
				if(childId !== null){
					changeFunc3 = function(){
						if(idx > 0){
							$.setInputboxValue($('#'+childId), "");
						}
					};
				}
			}

			// htmlのcheck条件を参照
			var check = $('#'+id).attr("check") ? JSON.parse('{'+$('#'+id).attr("check")+'}'): JSON.parse('{}');
			var datatyp = check.datatyp;
			if($('#'+id).is(".easyui-textbox")||$('#'+id).is(".easyui-textbox_")){
				if($('#'+id).is(".easyui-textbox_")){
					$('#'+id).removeClass("easyui-textbox_").addClass("easyui-textbox");
				}
				var validType = $.fn.textbox.defaults.validType;
				if(check.datatyp=="tel"){
					validType = 'tel';
				}else if(check.datatyp=="fax"){
					validType = 'fax';
				}else if(check.datatyp=="suuji_text"){
					validType = 'onlySuuji['+check.maxlen+']';
				}else if(check.datatyp=="suujispace_text"){
					validType = 'onlySuujiSpace['+check.maxlen+']';
				}else if(check.datatyp=="suujihaihun_text"){
					validType = 'onlySuujihaihun['+check.maxlen+']';
				}else if(check.datatyp=="kana_text"){
					validType = 'onlyHalfChar['+check.maxlen+']';
				}else if(check.datatyp=="alpha_text"){
					validType = 'onlyHalfChar['+check.maxlen+']';
				}else if(check.datatyp=="zen_text"){
					validType = 'onlyFullChar['+check.maxlen+']';
				}else if(check.datatyp=="AlphaL_text"){
					validType = 'onlyCharAlphaL['+check.maxlen+']';
				}else if(check.maxlen){
					validType = 'maxLen['+check.maxlen+']'
				}

				$('#'+id).textbox({
					type:'text',
					validType:validType,
					inputEvents:$.extend({},$.fn.textbox.defaults.inputEvents,{
						keydown:function(e){
							if(check.datatyp=="kana_text" || check.datatyp=="alpha_text"){
								var code = e.which ? e.which : e.keyCode;
								// [,"']の入力を制御
								if (e.originalEvent.key===',' || e.originalEvent.key==='"' || e.originalEvent.key==='\'') {
									return false;
								}
							}
						}
					}),
					onChange:function(newValue, oldValue, obj){
						if(obj===undefined){obj = $(this);}

						if(changeFunc1!==null){ changeFunc1(obj);}
						if(idx > 0 && obj.textbox('isValid')){
							if(changeFunc2!==null){ changeFunc2(newValue, obj);}
							if(changeFunc3!==null){ changeFunc3();}
							$.removeErrState();
						}

						if (!$.isEmptyVal($.prohibitedList[check.datatyp]) && !$.isEmptyVal(newValue)) {
							var strLen	= $.prohibitedList[check.datatyp].TEXT.split('').length;
							var str2Len	= $.prohibitedList[check.datatyp].TEXT2.split('').length

							for (var i=0; i < strLen; i++) {
								var str = $.prohibitedList[check.datatyp].TEXT.split('')[i];
								var str2 = i < str2Len ? $.prohibitedList[check.datatyp].TEXT2.split('')[i]:'';
								newValue = newValue.replace(str,str2);
							}

							if(check.datatyp==="zen_text"){
								var param = {};
								param["KEY"] =  "PROHIBITED";
								param["value"] = newValue;
								newValue = $.getInputboxData(that.name, $.id.action_check, id, [param]);
							}
							$.setInputboxValue(obj,newValue);
						}
					}
				});
				if (json && json.value.length > 0){ $('#'+id).textbox("setValue",json.value); }
				idx = 1;
			}else if($('#'+id).is(".easyui-numberbox")||$('#'+id).is(".easyui-numberbox_")){
				if($('#'+id).is(".easyui-numberbox_")){
					$('#'+id).removeClass("easyui-numberbox_").addClass("easyui-numberbox");
				}
				var validType = $.fn.numberbox.defaults.validType;
				var formatter = $.fn.numberbox.defaults.formatter;
				var parser = $.fn.numberbox.defaults.parser;
				var precision = $.fn.numberbox.defaults.precision;
				var groupSeparator = $.fn.numberbox.defaults.groupSeparator;
				var min = $.fn.numberbox.defaults.min;
				var max = $.fn.numberbox.defaults.max;

				var tag_options = $('#'+id).attr('data-options');
				if(tag_options){
					tag_options = '\"'+tag_options.replace(/'/g, '').replace(/,/g, '\",\"').replace(/([a-zA-Z0-9]+):/g, '$1\":\"')+'\"';
				}
				var options = tag_options ? JSON.parse('{'+tag_options+'}'): JSON.parse('{}');

				if(check.datatyp=="date"){
					validType = 'yymmdd';
					min = 101;
					if(options.prompt){
						var addWeek = /\(_\)$/.test(options.prompt);
						var isYY = /^__\/__/.test(options.prompt);
						if(isYY){
							formatter = function(value){return $.getFormatDt(value, isYY, addWeek);};
						}else{
							validType = 'yyyymmdd';
							formatter = function(value){return $.getFormatDt2(value, addWeek);};
						}
						parser= function(value){return $.getParserDt(value, false);};
					}
				}else if(check.datatyp=="yymm"){
					validType = 'ym';
					formatter = function(value){return $.getFormatYm(value);};
					parser = function(value){return $.getParserYm(value, false);};
				}else if(check.datatyp=="mmdd"){
					validType = 'mmdd';
					formatter = function(value){return $.getFormatMmdd(value);};
					parser = function(value){return $.getParserMmdd(value);};
				}else if(check.datatyp=="hhmm"){
					validType = 'hhmm';
					formatter = function(value){return $.getFormatHhmm(value);};
					parser = function(value){return $.getParserHhmm(value);};
				}else if(check.datatyp && check.datatyp.indexOf("text") != -1){
					min = 0;
					if(options.prompt){
						var format = options.prompt.replace(/_/g, '#');
						formatter = function(value){
							return $.getFormatPrompt(value, format);
						};
						parser= function(value){
							return $.getParserPrompt(value);
						};
						if(check.maxlen){
							validType = 'formatMaxLen['+check.maxlen+',"'+format+'"]'
						}
					}else if(check.datatyp==='lpadzero_text'){
						formatter = function(value){
							return $.getFormatLPad(value, check.maxlen);
						};
						parser= function(value){
							return $.getParserLPad(value);
						};
						if(options.min){
							min=options.min;
						} else {
							min = null;
						}
						if(options.max){
							max=options.max;
						} else {
							max = null;
						}
						if(min!==null&&max!==null){
							validType = 'intMaxLenAndMinMaxEqVal['+check.maxlen+','+min+','+max+']';
						}else if(min!==null&&max===null){
							validType = 'intMaxLenAndMinEqVal['+check.maxlen+','+min+']';
						} else if(check.maxlen){
							validType = 'intMaxLen['+check.maxlen+']';
						}
					}else if(check.datatyp==='suuji_text'){
						parser= function(value){
							return $.getParserCode(value);
						};
						if(check.maxlen){ validType = 'intMaxLen['+check.maxlen+']'}
					}else{
						if(check.maxlen){ validType = 'intMaxLen['+check.maxlen+']'}
					}
				}else if(check.datatyp=="decimal"){
					if(options.min){min=options.min;}
					if(options.max){max=options.max;}
					if(check.maxlen1 && check.maxlen2){
						if(min!==null&&max!==null){
							validType = 'floatMaxLenAndMinMaxEqVal['+check.maxlen1+','+check.maxlen2+','+min+','+max+']';
						}else if(min!==null&&max===null){
							validType = 'floatMaxLenAndMinEqVal['+check.maxlen1+','+check.maxlen2+','+min+']';
						}else if(min===null&&max!==null){
							validType = 'floatMaxLenAndMaxEqVal['+check.maxlen1+','+check.maxlen2+','+max+']';
						}else{
							validType = 'floatMaxLen['+check.maxlen1+','+check.maxlen2+']';
						}
						precision = check.maxlen2;
					}
					groupSeparator = ",";

				}else{
					if(options.min){min=options.min;}
					if(options.max){max=options.max;}
					if(check.maxlen){
						if(min!==null&&max!==null){
							validType = 'intMaxLenAndMinMaxEqVal['+check.maxlen+','+min+','+max+']';
						}else if(min!==null&&max===null){
							validType = 'intMaxLenAndMinEqVal['+check.maxlen+','+min+']';
						}else if(min===null&&max!==null){
							validType = 'intMaxLenAndMaxEqVal['+check.maxlen+','+max+']';
						}else{
							if(options.numonly){
								validType = 'intMaxLenNumberOnly['+check.maxlen+']';
							}else{
								validType = 'intMaxLen['+check.maxlen+']';
							}
						}
					}
					groupSeparator = ",";
				}
				$('#'+id).numberbox({
					validType:validType,
					precision:precision,
					groupSeparator:groupSeparator,
					formatter:formatter,
					parser:parser,
					min:min,
					max:max,
					onChange:function(newValue, oldValue, obj){
						if(obj===undefined){obj = $(this);}

						if(changeFunc1!==null){ changeFunc1(obj);}
						if(idx > 0 && obj.textbox('isValid')){
							if(changeFunc2!==null){ changeFunc2(newValue, obj);}
							if(changeFunc3!==null){ changeFunc3();}
							$.removeErrState();
						}
					},
//					inputEvents:$.extend({},$.fn.numberbox.defaults.inputEvents,{
//						keydown:function(e){
//							var code = e.which ? e.which : e.keyCode;
//							// "-"の入力を制御
//							if (code === 189) {
//								return false;
//							}
//						}
//					})
				});
//				function keyDownTextField(e) {
//					var code = e.which ? e.which : e.keyCode;
//					// "-"の入力を制御
//					if (code === 189) {
//						return false;
//					}
//				}
//				$('#'+id)[0].addEventListener("onkeydown", keyDownTextField, false);
				if (json && json.value.length > 0){ $('#'+id).numberbox("setValue",json.value); }
				idx = 1;
			}else{
				$('#'+id).change(function(e, obj) {
					if(obj===undefined){obj = $(this);}

					if(changeFunc1!==null){ changeFunc1();}
					if(idx > 0){
						var newValue = $('#'+id).val();
						if(changeFunc2!==null){ changeFunc2(newValue, obj);}
						$.removeErrState();
					}
				});
				idx = 1;
			}
			if(isRefer){ $.setInputStateRefer(that, $('#'+id)); }

			if (that.initedObject && $.inArray(id, that.initedObject) < 0){
				that.initedObject.push(id);
			}
			if(suffix===''){
				if(isUpdateReport){
					// 初期表示処理
					$.initialDisplay(that);
				}else{
					// 初期表示検索処理
					$.initialSearch(that);
				}
			}
		},
		// コンボボックス(特にid_mei宣言の入力項目)を共通で設定する
		setMeisyoCombo: function(that, reportno, id, isUpdateReport){
			var idx = -1;
			if($('#'+id).is(".easyui-combobox_")){
				$('#'+id).removeClass("easyui-combobox_").addClass("easyui-combobox");
			}

			// 更新項目で参照表示かどうか
			var isRefer = $.isReferUpdateInput(that, $('#'+id), isUpdateReport);
			var readonly = isRefer;
			var onShowPanel = $.fn.combobox.defaults.onShowPanel;
			if (isRefer) {
				onShowPanel = function(){
					$('#'+id).combobox('hidePanel');
				};
			}

			var tag_options = $('#'+id).attr('data-options');
			if(tag_options){
				tag_options = '\"'+tag_options.replace(/'/g, '').replace(/,/g, '\",\"').replace(/([a-zA-Z0-9]+):/g, '$1\":\"')+'\"';
			}
			var options = tag_options ? JSON.parse('{'+tag_options+'}'): JSON.parse('{}');

			var required = options && options.required;
			var editable = true;
			if ($('#'+id).parent().is(".ref_editor")) {
				editable = false;
			}
			var editableCheck = options && options.editable? true:false;
			var topBlank = !required;

			if (required==='false') {
				topBlank = true;
			}

			var panelWidth = options && options.panelWidth ? options.panelWidth : null;
			var panelHeight = options && options.panelHeight ? options.panelHeight :'auto';
			var suffix = that.suffix ? that.suffix : '';
			var changeFunc1 = null;
			if(isUpdateReport){
				changeFunc1 = function(){
					if(idx > 0 && that.queried && $($.id.hiddenChangedIdx).is(':enabled')){
						$($.id.hiddenChangedIdx+suffix).val("1");
					}
				};
			}
			// 当項目を変更した際に、値をセットする項目がある場合の処理（セット先の項目は、HTMLタグとして、for_inp=当項目のID_列名）
			var changeFunc2 = null;
			if($.isFunction(that.changeInputboxFunc)){
				changeFunc2 = function(newValue, obj){
					that.changeInputboxFunc(that, id, newValue, obj);
				};
			}else{
				if($('[for_inp^='+id+'_]').length > 0){
					changeFunc2 = function(newValue){
						var param = [{"value":newValue}];
						$.getsetInputboxRowData(reportno, 'for_inp', id, param, that);
					};
				}
			}

			// フォーカスアウトのタイミングの動作
			$('#'+id).next().on('focusout', function(e){
				var obj = $(this).prev();

				if (!$.setComboReload(obj,false)) {
					obj.combobox('reload');
				}
			});

			var filter = function(q,row){
				var opts=$(this).combobox("options");
				return row[opts.textField].toLowerCase().indexOf(q.toLowerCase())>=0;
			};

			if (options && options.filter) {
				filter = function(q,row){
					var opts=$(this).combobox("options");
					return row[opts.textField].toLowerCase().indexOf(q.toLowerCase())===0;
				}
			}

			$('#'+id).combobox({
				url:$.reg.easy,
				required: required,
				readonly:readonly,
				editable: editable,
				autoRowHeight:false,
				panelWidth:panelWidth,
				panelHeight:panelHeight,
				valueField:'VALUE',
				textField:'TEXT',
				multiple :false,
				prompt: '',
				keyHandler: {
					up: $.fn.combobox.defaults.keyHandler.up,
					down: $.fn.combobox.defaults.keyHandler.down,
					left: $.fn.combobox.defaults.keyHandler.left,
					right: $.fn.combobox.defaults.keyHandler.right,
					enter: function(e){
						$(this).combobox('hidePanel');
						e.preventDefault();
					},
					query: $.fn.combobox.defaults.keyHandler.query
				},
				filter:filter,
				onShowPanel:onShowPanel,
				onBeforeLoad:function(param){
					// 情報設定
					var json = [{
						DUMMY: 'DUMMY'
					}];
					if(topBlank){json[0]['TOPBLANK'] = topBlank;}

					param.page		=	reportno;
					param.obj		=	id.replace(suffix, "");
					param.sel		=	(new Date()).getTime();
					param.target	=	id;
					param.action	=	$.id.action_init;
					param.json		=	JSON.stringify(json);
					param.datatype	=	"combobox";
					$('#'+id).combobox('setText', ' ');
				},
				onLoadSuccess:function(data){
					// 初期化
					var val = null;
					if (that.initedObject && $.inArray(id, that.initedObject) < 0){
						that.initedObject.push(id);
						var init = $.getJSONValue(that.jsonHidden, id);
						for (var i=0; i<data.length; i++){
							if (data[i].VALUE == init){
								val = init;
								break;
							}
						}
					}
					if (val === null && data.length>0){
						val = data[0].VALUE;
					}
					$('#'+id).combobox('setValue', val);

					idx = 1;
					// ログ出力
					$.log(that.timeData, id+' init:');
					if(suffix===''){
						if(isUpdateReport){
							// 初期表示処理
							$.initialDisplay(that);
						}else{
//							// 検索ボタン有効化
//							$.setButtonState('#'+$.id.btn_search, true, id);
							// 初期表示検索処理
							$.initialSearch(that);
						}
					}
				},
				onChange:function(newValue, oldValue, obj){
					if(obj===undefined){obj = $(this);}

					if(changeFunc1!==null){ changeFunc1();}
					if(changeFunc2!==null){ changeFunc2(newValue, obj);}
					if(idx > 0){
						$.removeErrState();
					}

					var data = obj.combobox('getData');

					if (!obj.hasClass('datagrid-editable-input')) {
						if (!$.setComboReload(obj,true) && !editableCheck) {
							$.showMessage("E11302",["入力値"],function () {$.addErrState(this,obj,false)});
							obj.combobox('reload');
							obj.combobox('hidePanel');
						} else if ($.isEmptyVal(newValue)) {
							obj.combobox('setValue',obj.combobox('getData')[0].VALUE);
						} else if ($.isEmptyVal(oldValue)) {
							if (obj.next().find('[tabindex=1]').length===1) {
								obj.combo("textbox").focus();
							}
						}
					}
				},
				onClick:function(record) {
					$('#'+id).combo("textbox").focus();
				}
			});
			if(isRefer){ $.setInputStateRefer(that, $('#'+id)); }
		},
		setComboReload: function (target,check) {

			var tag_options = target.attr('data-options');
			if(tag_options){
				tag_options = '\"'+tag_options.replace(/'/g, '').replace(/,/g, '\",\"').replace(/([a-zA-Z0-9]+):/g, '$1\":\"')+'\"';
			}
			var options = tag_options ? JSON.parse('{'+tag_options+'}'): JSON.parse('{}');
			var editable = options && options.editable;

			var data = target.combobox('getData');
			var val = target.next().children('.textbox-value').val();
			var txt = target.combobox('getText');

			for (var i = 0; i < data.length; i++) {

				var dataVal = data[i].VALUE;
				var dataText = data[i].TEXT;
				if (val*1===dataVal*1) {
					val = dataVal;
				}

				if (check && (!(data[i].VALUE==='-1' && (val==='1'||val==='-')) && (data[i].VALUE.indexOf(val) >= 0 || data[i].TEXT.indexOf(txt) >= 0))) {
					return true;
				} else if (!check && (data[i].VALUE === val)) {
					if (data[0].VALUE!=='-1' && data[0].VALUE===val) {
						return false;
					}

					// 初期表示時に先頭フォーカスがcomboboxだった場合の制御用
					var chg		= $($.id.hiddenChangedIdx).val();
					var oldVal	= target.combobox('getValue');
					var oldText	= target.combobox('getText');
					if (chg !== '1' && (val===oldVal && dataText===oldText)) {
						return true;
					}

					target.combobox('setValue',data[0].VALUE);
					target.combobox('setValue',val);
					return true;
				}
			}
			return editable;
		},
		getInputboxValue: function(target, defEmptyVal, add20){
			var value = "";
			if(target.hasClass('easyui-combobox')){
				var options = target.combobox('options');
				if(options.multiple){
					value = target.combobox('getValues');
				}else{
					value = target.combobox('getValue');
				}
			}else if(target.hasClass('easyui-textbox')){
				value = target.textbox('getValue');
			}else if(target.hasClass('easyui-numberbox')){
				var options = target.numberbox('options');
				if(options.validType == 'yymmdd' && options.prompt){
					value = $.getParserDt(target.numberbox('getValue'), add20);
				}else if(options.validType == 'ym' && options.prompt){
					value = $.getParserYm(target.numberbox('getValue'), add20);
				}else{
					value = target.numberbox('getValue');
				}
			}else if(target.is(":radio")){
				value = $('input[name="'+target.attr('name')+'"]:checked').val()
			}else if(target.is(":checkbox")){
				value = target.is(':checked') ? $.id.value_on : $.id.value_off;
			}else if(target.is(":input")){
				value = target.val();
			}else{
				value = target.text();
			}
			// 空白時のデフォルト値設定がある場合、
			if($.isEmptyVal(value) && defEmptyVal){
				value = defEmptyVal;
			}
			return value;
		},
		setInputboxValue: function(target, value, zeroEmpty){
			// 0を空白扱い
			if(!target.is(":radio") && !target.is(":checkbox") && !target.hasClass('easyui-combobox')){
				if(zeroEmpty){
					if($.isEmptyVal(value, true)){
						value = "";
					}
				}
			}

			if(target.hasClass('easyui-combobox')){
				var options = target.combobox('options');
				if(options.multiple){
					var val = new Array();
					val.push(value);
					target.combobox('setValues', val);
				}else{
					var data = target.combobox('getData');
					for (var i=0; i<data.length; i++){
						if (data[i].VALUE == value){
							target.combobox('setValue', value);
							break;
						}
					}
				}
			}else if(target.hasClass('easyui-textbox')){
				target.textbox('setValue', value);
			}else if(target.hasClass('easyui-numberbox')){
				target.numberbox('setValue', value);
			}else if(target.is(":radio")){
				$('input[name="'+target.attr('name')+'"]').val([value]);
				$('input[name="'+target.attr('name')+'"]:checked').change();
			}else if(target.is(":checkbox")){
				target.prop("checked", value.length > 0 && $.id.value_on === value).change();
			}else if(target.is(":input")){
				target.val(value)
			}else{
				target.text(value)
			}
			return true;
		},
		getInputboxText: function(target, defEmptyVal, add20){
			var value = "";
			if(target.hasClass('easyui-combobox')){
				value = target.combobox('getText');
			}else if(target.hasClass('easyui-textbox')){
				value = target.textbox('getValue');
			}else if(target.hasClass('easyui-numberbox')){
				value = target.numberbox('getText');
			}else if(target.is(":radio")){
				value = $('input[name="'+target.attr('name')+'"]:checked').text()
			}else if(target.is(":checkbox")){
				value = target.is(':checked') ? $.id.text_on : $.id.text_off;
			}else if(target.is(":input")){
				value = target.val();
			}else{
				value = target.text();
			}
			// 空白時のデフォルト値設定がある場合、
			if($.isEmptyVal(value) && defEmptyVal){
				value = defEmptyVal;
			}
			return value;
		},
		// パラメータを元にDBに問い合わせた結果を取得、画面上に設定する
		getsetInputboxData: function(reportno, id, param, action){
			if(action===undefined) action = $.id.action_change;
			idx = -1;
			// 情報設定
			$.ajax({
				url: $.reg.easy,
				type: 'POST',
				async: true,
				data: {
					page	: reportno,
					obj		: id,
					sel		: (new Date()).getTime(),
					target	: id,
					action	: action,
					json	: JSON.stringify(param)
				},
				success: function(json){
					var isSet = false;
					var value = "";
					if(json !=="" &&  JSON.parse(json).rows.length > 0){
						value = JSON.parse(json).rows[0].VALUE;
					}
					isSet = $.setInputboxValue($('#'+id), value);
				}
			});
			idx = 1;
		},
		// パラメータを元にDBに問い合わせた結果を取得、画面上に設定する
		getsetInputboxRowData: function(reportno, tag, id, param, that, parentobj, action){
			if(action===undefined) action = $.id.action_change;
			var async = true;
			if(parentobj && that.focusGridId!==""){
				async = false;
			}
			// 情報設定
			$.ajax({
				url: $.reg.easy,
				type: 'POST',
				async: async,
				data: {
					page	: reportno,
					obj		: id,
					sel		: (new Date()).getTime(),
					target	: id,
					action	: action,
					json	: JSON.stringify(param)
				},
				success: function(json){
					var isSet = false;
					var row = "";
					if(json !=="" &&  JSON.parse(json).rows.length > 0){
						row = JSON.parse(json).rows[0];
					}
					isSet = $.setInputboxRowData(tag, id, row, that, parentobj);
				}
			});

		},
		// 取得結果をもとに、画面上に値を設定する
		setInputboxRowData: function(tag, id, row, that, parentobj){
			if(!parentobj){
				if(that){
					parentobj = $("#"+that.focusRootId);
				}else{
					parentobj = $("body");
				}
			}
			var isSet = false;
			if(row===""){
				parentobj.find('['+tag+'^='+id+'_]').each(function(){
					$.setInputboxValue($(this), "");
				});
			}else{
				// 取得した情報を、オブジェクトに設定する
				// 設定先の判定：オブジェクトに for_btn,for_inpタグなどを使用して呼出し元と列名が設定されている項目
				var columns = Object.getOwnPropertyNames(row);
				for ( var col in columns ) {
					if(parentobj.find('['+tag+'='+id+'_'+columns[col]+']').length > 0){
						var target = parentobj.find('['+tag+'='+id+'_'+columns[col]+']').eq(0);
						$.setInputboxValue(target, row[columns[col]]);
						isSet=true;
					}
				}
			}
			return isSet;
		},
		// パラメータを元にDBに問い合わせた結果を取得
		getInputboxData: function(reportno, action, id, param){
			var value = "";
			// 情報設定
			$.ajax({
				url: $.reg.easy,
				type: 'POST',
				async: false,
				data: {
					page	: reportno,
					obj		: id,
					sel		: (new Date()).getTime(),
					target	: id,
					action	: action,
					json	: JSON.stringify(param)
				},
				success: function(json){
					if(json !=="" &&  JSON.parse(json).rows.length > 0){
						value = JSON.parse(json).rows[0].VALUE;
					}
				}
			});
			return value;
		},
		// パラメータを元にDBに問い合わせた結果を取得
		getSelectListData: function(reportno, action, id, param){
			var rows = [];
			// 情報取得
			$.ajax({
				url: $.reg.easy,
				type: 'POST',
				async: false,
				data: {
					page	: reportno,
					obj		: id,
					sel		: (new Date()).getTime(),
					target	: id,
					action	: action,
					json	: JSON.stringify(param)
				},
				success: function(json){
					var isSet = false;
					var value = "";
					if(json !=="" &&  JSON.parse(json).rows.length > 0){
						rows = JSON.parse(json).rows;
					}
				}
			});
			return rows;
		},
		setInputBoxEnable:function(target){
			if(target.hasClass('easyui-combobox')){
				target.combobox('enable');
			}else if(target.hasClass('easyui-textbox')){
				target.textbox('enable');
			}else if(target.hasClass('easyui-numberbox')){
				target.numberbox('enable');
			}else if(target.hasClass('easyui-linkbutton')){
				target.css('pointer-events','auto');
				target.linkbutton('enable');

			}else if(target.is(":radio")){
				$('input[name="'+target.attr('name')+'"]').removeAttr('disabled');
			}
			target.removeAttr('readonly');
			target.removeAttr('disabled');
			return target;
		},
		setInputBoxEnableVariable:function(target, required, tabindex){
			// 入力可、入力不可を切り替える項目に使用
			if(required===undefined){ required = false;} // 未設定の場合、必須項目として扱わない。
			if(target.hasClass('easyui-textbox') || target.hasClass('easyui-numberbox')){
				target.textbox('textbox').attr('readonly',false);
				if(tabindex){
					// tabindexを設定する
					target.textbox('textbox').attr('tabIndex', tabindex);
				}
				target.textbox('textbox').removeAttr('disabled');

				if(required){
					if(target.hasClass('easyui-textbox')){
						// 必須入力項目として設定をする
						target.textbox('options').required = true;
						target.textbox('textbox').validatebox('options').required = true;
						target.textbox('validate');

					}else if(target.hasClass('easyui-numberbox')){
						// 必須入力項目として設定をする
						target.numberbox('options').required = true;
						target.numberbox('textbox').validatebox('options').required = true;
						target.numberbox('validate');
					}
				}
			}else{
				// textbox系以外の項目
				if(target.hasClass('easyui-combobox')){
					target.combobox('enable');
					target.removeAttr('disabled');
					if(target.combo('textbox')){
						// 入力可能なcomboboxの場合
						//target.combo("textbox")[0].readOnly = false;
						target.combo('enable')
						target.combo('textbox').editable = true;
						target.combo('textbox').attr('readonly',false);
					}

				}else if(target.is(":radio")){
					$('input[name="'+target.attr('name')+'"]').removeAttr('disabled');

				}else if(target.hasClass('easyui-linkbutton')){
					target.css('pointer-events','auto');
					target.linkbutton('enable');
					target.removeAttr('disabled');

				}else{
					target.removeAttr('disabled');
				}
				target.removeAttr('readonly');
			}
			return target;
		},
		setInputBoxDisable:function(target, isTemporary){
			if(isTemporary===undefined){ isTemporary = false;}

			if(target.hasClass('easyui-combobox')){
				target.combobox('disable');
			}else if(target.hasClass('easyui-textbox')){
				target.textbox('disable');
			}else if(target.hasClass('easyui-numberbox')){
				target.numberbox('disable');
			}else if(target.hasClass('easyui-linkbutton')){
				target.css('pointer-events','none');
				target.linkbutton('disable');
			}else if(target.is(":radio")){
				$('input[name="'+target.attr('name')+'"]').attr('disabled', 'disabled');
			}
			target.attr('readonly', 'readonly');
			target.attr('disabled', 'disabled');
			if(!isTemporary){
				target.attr('tabindex', -1);
			}
			return target;
		},
		setInputBoxDisableVariable:function(target, isTemporary){
			if(isTemporary===undefined){ isTemporary = false;}
			// 入力可、入力不可を切り替える項目に使用
			if(target.hasClass('easyui-textbox') || target.hasClass('easyui-numberbox')){
				if(!isTemporary){
					target.textbox('textbox').attr('tabindex', -1);
				}
				target.textbox('textbox').attr('readonly', 'readonly');
				target.textbox('textbox').attr('disabled', 'disabled');

				if(target.hasClass('easyui-textbox')){
					if(target.textbox('options').required){
						// 必須入力項目設定を解除する
						target.textbox('options').required = false;
						target.textbox('textbox').validatebox('options').required = false;
						target.textbox('validate');
					}
				}else if(target.hasClass('easyui-numberbox')){
					if(target.numberbox('options').required){
						// 必須入力項目設定を解除する
						target.numberbox('options').required = false;
						target.numberbox('textbox').validatebox('options').required = false;
						target.numberbox('validate');
					}
				}
			}else {
				// textbox系以外の項目
				if(target.hasClass('easyui-combobox')){
					target.combobox('disable');

				}else if(target.hasClass('easyui-linkbutton')){
					target.css('pointer-events','none');
					target.linkbutton('disable');
					target.attr('disabled', 'disabled');

				}else if(target.is(":radio")){
					$('input[name="'+target.attr('name')+'"]').attr('disabled', 'disabled');

				}else{
					target.attr('disabled', 'disabled');
				}
				target.attr('readonly', 'readonly');

				if(!isTemporary){
					target.attr('tabindex', -1);
				}
			}
			return target;
		},
		setInputBoxRequired:function(target){
			if(target.hasClass('easyui-combobox')){
				target.combobox('options').required = true;
				target.combobox('textbox').validatebox('options').required = true;
			}else if(target.hasClass('easyui-textbox')){
				target.textbox('options').required = true;
				target.textbox('textbox').validatebox('options').required = true;
			}else if(target.hasClass('easyui-numberbox')){
				target.numberbox('options').required = true;
				target.numberbox('textbox').validatebox('options').required = true;
			}else if(target.hasClass('easyui-combobox_')||target.hasClass('easyui-textbox_')||target.hasClass('easyui-numberbox_')){
				// 初期化前の場合
				var tag_options = target.attr('data-options');
				if(tag_options){
					tag_options = tag_options.replace(";", "") + ",required:true";
				}else{
					tag_options = "required:true";
				}
				target.attr('data-options', tag_options)
			}

			return target;
		},
//		setInputBoxValid:function(target){
//			var options = null;
//			isValid
//			if(target.hasClass('easyui-combobox')){
//				options = target.combobox('options');
//			}else if(target.hasClass('easyui-textbox')){
//				options = target.textbox('options');
//			}else if(target.hasClass('easyui-numberbox')){
//				options = target.numberbox('options');
//			}
//			if(options){
//				options.required = true;
//			}
//			return target;
//		},
		getInputboxTextbox: function(obj){
			if(obj.hasClass('easyui-combobox')){
				return obj.combobox('textbox');
			}else if(obj.hasClass('easyui-textbox')){
				return obj.numberbox('textbox');
			}else if(obj.hasClass('easyui-numberbox')){
				return obj.textbox('textbox');
			}else{
				return obj;
			}
		},

		// 基本のメッセージ一覧情報利用のための処理
		// メッセージ一覧情報保持
		messageList:{},
		// メッセージ一覧情報取得
		initMessageListData: function(reportno){
			$.messageList = {};
			var rows = $.getSelectListData(reportno, $.id.action_init, "msg_list", [{}]);;
			for (var i=0; i<rows.length; i++){
				$.messageList[rows[i]["MSGCD"]] = rows[i];
			}
			return true;
		},
		// 基本のメッセージ一覧情報利用のための処理
		// メッセージ一覧情報保持
		prohibitedList:{},
		// メッセージ一覧情報取得
		initProhibitedListData: function(reportno){
			$.prohibitedList = {};
			var rows = $.getSelectListData(reportno, $.id.action_init, "prohibited_list", [{}]);;
			for (var i=0; i<rows.length; i++){
				$.prohibitedList[rows[i]["MOJI"]] = rows[i];
			}
			return true;
		},
		// パラメータを元にDBに問い合わせた結果を取得
		getMessageObj: function(key){
			return $.messageList[key];
		},
		// パラメータを元にDBに問い合わせた結果を取得
		getMessage: function(key, add){
			if(add===undefined) add = [];
			var add1 = "", add2 = "", add3 = "";
			if(add.length > 0) add1 = add[0];
			if(add.length > 1) add2 = add[1];
			if(add.length > 2) add3 = add[2];

			var msgObj = $.getMessageObj(key);
			var msg1 = msgObj["MSGTXT1"]?msgObj["MSGTXT1"]:"";
			var msg2 = msgObj["MSGTXT2"]?msgObj["MSGTXT2"]:"";
			if((msg1+msg2).indexOf('%') !== -1){
				return (msg1+msg2).replace('%1', add1).replace('%2', add2).replace('%3', add3).replace(/^\s+|\s+$/g,'');
			}else{
				return (add1 + msg1.trim() + add2 + msg2.trim() + add3).replace(/^\s+|\s+$/g,'');
			}
		},
		// メッセージを表示、key,msgは必須です。
		showMessage:function(key, add, func_ok, func_no){
			var msgObj = $.getMessageObj(key);
			var msg = $.getMessage(key, add);
			$.showMessageIn(key, msg, undefined, func_ok, msgObj["BTNKBN"], msgObj["DEFBTN"], msgObj["ICONKBN"], func_no);
		},
		showMessageIn:function(key, msg, title, func_ok, btnkbn, defbtn, iconkbn, func_no){
			var width = 500;
			var title_tail = key.length > 2 ? " － " + key : "";

			// メッセージ内の使用できない文字を変換する。
			if(msg){
				if(key == 'E35000'){
					// TODO
					// 改行コードを<br>に置換
					msg = msg.substr(0,20) + "<br>" + msg.substr(24);
				}
			}

			// はい、いいえボタンの場合
			if(btnkbn==="4"){
				// ウィンドウタイトル指定
				if(title===undefined){
					title = "確認" + title_tail;
				}
				var idx = 0;
				if(defbtn.match(/^([1-9]{1})$/)){
					idx = defbtn*1 - 1;
				}
				if(func_ok===undefined){
					func_ok = $.setFocusFirst;
				}
				if(func_no===undefined){
					func_no = $.setFocusFirst;
				}
				$.messager.confirm({
					title: title,
					msg: msg,
					fn: function(r){
						if(r){
							func_ok();
						}else{
							func_no();
						}
					},
					width:width,
					zIndex:100000,
					onOpen:function(){
						setTimeout(function(){
							$(".messager-window").find(".l-btn").eq(idx).focus();
						},50);
					}
				});

			// 他はOKのみ
			}else{
				// ウィンドウタイトル指定
				if(title===undefined){
					if(key.match(/^E/)){
						title = "エラー"+title_tail;
					}
					if(key.match(/^W/)){
						title = "警告" + title_tail;
					}
					if(key.match(/^I/)){
						title = "情報" + title_tail;
					}
				}
				var icon = '';
				if(iconkbn!==undefined){
					if(iconkbn.match(/E/)){
						icon = "error";
					}
					if(iconkbn.match(/I/)){
						icon = "info";
					}
					if(iconkbn.match(/W/)){
						icon = "warning";
					}
					if(iconkbn.match(/Q/)){
						icon = "question";
					}
				}
				if(func_ok===undefined){
					func_ok = $.setFocusFirst;
				}
				var idx = 0;
				$.messager.alert({
					title: title,
					icon:icon,
					msg: msg,
					fn: func_ok,
					width:width,
					zIndex:100000,
					onOpen:function(){
						setTimeout(function(){
							$(".messager-window").find(".l-btn").eq(idx).focus();
						},50);
					}
				});
			}

		},



		setCombogridValue : function(jsonArray, id) {

			var num = 0;
			var rows = $('#'+id).combogrid('grid').datagrid('getRows');
			var val = $.getJSONValue(jsonArray, id);
			for (var i=0; i<rows.length; i++){
				if (rows[i].VALUE == val){	// 値比較
					num = i;
					break;
				}
			}
			$('#'+id).combogrid('grid').datagrid('selectRow', num);
		},

		setComboboxValue : function(jsonArray, id) {
			var val = $.getJSONValue(jsonArray, id);
			$('#'+id).combobox('setValue', val);
		},

		setCombogrid : function(id, val) {

			var num = 0;
			var rows = $('#'+id).combogrid('grid').datagrid('getRows');
			for (var i=0; i<rows.length; i++){
				if (rows[i].VALUE == val){	// 値比較
					num = i;
					break;
				}
			}
			$('#'+id).combogrid('grid').datagrid('selectRow', num);
		},

		setCombobox : function(id, val) {
			$('#'+id).combobox('setValue', val);
		},

		setCombogridMultiple : function(jsonArray, id) {

			var dg = $('#'+id).combogrid('grid');
			dg.datagrid('uncheckAll');
			var data = dg.datagrid('getData');

			// 選択値設定
			var val = null;
			if ($.inArray(id, jsonArray) < 0){
				var json = $.getJSONObject(jsonArray, id);
				if(json && json.value!=""){
					val = new Array();
					for (var i=0; i<data.rows.length; i++){
						if ($.inArray(data.rows[i].VALUE, json.value)!=-1){
							val.push(data.rows[i].VALUE);
						}
					}
				}
			}
			if (val){
				$('#'+id).combogrid('setValues',val);
			}else{
				dg.datagrid('checkAll');
			}
		},

		setCombogridData : function(jsonArray, id) {

			var dg = $('#'+id).combogrid('grid');
			dg.datagrid('uncheckAll');
			var oldData = $.getJSONObject(jsonArray, id+'DATA');
			var data = dg.datagrid('getData');

			for(var i=0;i<oldData.value.total;i++){
				// データをセットする
				if (data.rows[i]['TY'] !== undefined) data.rows[i]['TY'] = oldData.value.rows[i]['TY']==='' ? '' : $.id.checkBoxOnData;
				if (data.rows[i]['ZY'] !== undefined) data.rows[i]['ZY'] = oldData.value.rows[i]['ZY']==='' ? '' : $.id.checkBoxOnData;
				if (data.rows[i]['ZC'] !== undefined) data.rows[i]['ZC'] = oldData.value.rows[i]['ZC']==='' ? '' : $.id.checkBoxOnData;
				if (data.rows[i]['ZS'] !== undefined) data.rows[i]['ZS'] = oldData.value.rows[i]['ZS']==='' ? '' : $.id.checkBoxOnData;

				if (data.rows[i]['TP'] !== undefined) data.rows[i]['TP'] = oldData.value.rows[i]['TP']==='' ? '' : $.id.checkBoxOnData;
				if (data.rows[i]['ZP'] !== undefined) data.rows[i]['ZP'] = oldData.value.rows[i]['ZP']==='' ? '' : $.id.checkBoxOnData;

				dg.datagrid('refreshRow', i);	// 変更内容を反映する
			}
		},

		/**
		 * Web商談
		 * TextBox共通作成用
		 * @param {Object} that
		 * @param {Object} id
		 * @param {Object} required
		 * @param {Object} validType
		 * @param {Object} isSearch
		 */
		setTextBox : function(that, id, required, validType, isSearch){
			if(that){
				// 初期化
				$('#'+id).val($.getJSONValue(that.jsonHidden, id));
			}
			$('#'+id).textbox({
				required:required,
				validType:validType,
				onChange:function(newValue,oldValue){
					if(that.prefix === undefined && isSearch){
						// 検索ボタン有効化
						$.setButtonState('#'+$.id.btn_search, true, id);
					}
				}
			});
			if($('#'+id).attr('maxlength') != undefined){
				$('#'+id).combo('textbox').attr('maxlength', $('#'+id).attr('maxlength'));
			}
			if(that){
				if (that.initedObject != undefined && $.inArray(id, that.initedObject) < 0){
					that.initedObject.push(id);
				}
				// ログ出力
				$.log(that.timeData, id + ' init:');
				if(that.prefix === undefined){
					// 検索ボタン有効化
					$.setButtonState('#'+$.id.btn_search, true, id);
					// 初期表示検索処理
					$.initialSearch(that);
				}
			}
		},
		/**
		 * Web商談
		 * ロードフィルター使用時のページネーション作成処理
		 * @param that js
		 * @param dg datagrid
		 * @param opts datagridのoption
		 * @param data
		 */
		createPagenation:function(that, dg, opts, data){
			var pager = dg.datagrid('getPager');
			pager.pagination({
				showPageList:true,
				showRefresh:false,
				total:data.total,
				onSelectPage:function(pageNum, pageSize){
					opts.pageNumber = pageNum;
					opts.pageSize = pageSize;
					pager.pagination('refresh',{
						pageNumber:pageNum,
						pageSize:pageSize
					});
					dg.datagrid('loadData',data);
				}
			});
		},

		// DataGrid内編集エディターの機能拡張
		// 非表示で参照用のオブジェクトが準備してある前提、参照オブジェクトはグリッドのフィールド名を元に判断
		extendDatagridEditorInitializeId: {},
		extendDatagridEditor:function(that){
			$.extend($.fn.datagrid.defaults.editors, {
				combobox: {
					init: function(container, options){
						var input = $('<input class="datagrid-editable-input">').appendTo(container);
						var field = $(container).closest('td[field]').attr('field');
						var refId = 'sel_'+field.toLowerCase();
						if(that && that.extenxDatagridEditorIds && that.extenxDatagridEditorIds[field]){
							refId = that.extenxDatagridEditorIds[field];
						}
						var gfrmid = $(container).parents('form[id^='+$.id.gridform.replace('#', '')+']').eq(0).attr('id');
						var suffix = gfrmid!==undefined ? gfrmid.replace($.id.gridform.replace('#', '') ,'') : '';
						if(suffix!==''){
							refId += suffix;
						}
						var refObject = $('#'+refId);
						if($(refObject).length===0){ refObject = $('#'+refId.replace(/\_[0-9]+$/,"")); }
						// 参照項目から必要な情報をコピーする
						input.attr("id", refId+"_");	// 参照元ID＋"_"をIDとしてセット
						input.addClass(refObject.attr("class"));		// 基本クラス
						var tags = ["for_inp", "for_btn"];
						for (var i=0; i<tags.length; i++){
							if(refObject.is("["+tags[i]+"]")){
								input.attr(tags[i], refObject.attr(tags[i]));
							}
						}
						var func_change_base = refObject.combobox('options').onChange;
						var rowIndex = $(container).closest('tr[datagrid-row-index]').attr('datagrid-row-index');
						var func_change = function(newValue,oldValue){
							if($.extendDatagridEditorInitializeId[rowIndex]){
								if($.isFunction(func_change_base)) func_change_base(newValue,oldValue, $(this));
							}
						};
						input.combobox({
							panelWidth:refObject.combobox('options').panelWidth,
							panelHeight:refObject.combobox('options').panelHeight,
							hasDownArrow:!refObject.combobox('options').readonly,
							required:options.required?options.required:refObject.combobox('options').required,
							editable:options.editable?options.editable:refObject.combobox('options').editable,
							disabled:options.disabled?options.disabled:refObject.combobox('options').disabled,
							readonly:options.readonly?options.readonly:refObject.combobox('options').readonly,
							autoRowHeight:refObject.combobox('options').autoRowHeight,
							valueField:refObject.combobox('options').valueField,
							textField:refObject.combobox('options').textField,
							data:refObject.combobox('getData'),
							multiple :refObject.combobox('options').multiple,
							keyHandler: refObject.combobox('options').keyHandler,
							onChange:func_change
						});
						return input;
					},
					destroy: function(target){
						// 解除処理
						$(target).combobox('destroy');
					},
					getValue: function(target){
						// 編集解除時に導入値を戻す
						return $(target).combobox('getValue');
					},
					setValue: function(target, value){
						// 編集時に期待値の設定
						var data = $(target).combobox('getData');
						var val=value;
						for (var i=0; i<data.length; i++){
							if (data[i].VALUE=== value || data[i].TEXT === value){
								val = data[i].VALUE;
								break;
							}
						}
						$(target).combobox('setValue', val);
					},
					resize: function(target, width){
						// カラム幅の resize
						$(target).combobox('resize', width);
					}
				},
				textbox: {
					init: function(container, options){
						var input = $('<input class="datagrid-editable-input">').appendTo(container);
						var field = $(container).closest('td[field]').attr('field');
						var refId = 'txt_'+field.toLowerCase();
						if(that && that.extenxDatagridEditorIds && that.extenxDatagridEditorIds[field]){
							refId = that.extenxDatagridEditorIds[field];
						}
						var gfrmid = $(container).parents('form[id^='+$.id.gridform.replace('#', '')+']').eq(0).attr('id');
						var suffix = gfrmid!==undefined ? gfrmid.replace($.id.gridform.replace('#', '') ,'') : '';
						if(suffix!==''){
							refId += suffix;
						}
						var refObject = $('#'+refId);
						if($(refObject).length===0){ refObject = $('#'+refId.replace(/\_[0-9]+$/,"")); }
						// 参照項目から必要な情報をコピーする
						input.attr("id", refId+"_");					// 参照元ID＋"_"をIDとしてセット
						input.addClass(refObject.attr("class"));		// 基本クラス
						var tags = ["for_inp", "for_btn"];
						for (var i=0; i<tags.length; i++){
							if(refObject.is("["+tags[i]+"]")){
								input.attr(tags[i], refObject.attr(tags[i]));
							}
						}
						var func_change_base = refObject.textbox('options').onChange;
						var check = refObject.attr("check") ? JSON.parse('{'+refObject.attr("check")+'}'): JSON.parse('{}');
						var rowIndex = $(container).closest('tr[datagrid-row-index]').attr('datagrid-row-index');
						var func_change = function(newValue,oldValue){
							if($.extendDatagridEditorInitializeId[rowIndex]){
								if($.isFunction(func_change_base)) func_change_base(newValue,oldValue, $(this));
							}
						};
						input.textbox({
							cls:options.cls?options.cls:refObject.textbox('options').cls,
							prompt:options.prompt?options.prompt:refObject.textbox('options').prompt,
							type:options.type?options.type:refObject.textbox('options').type,
							editable:options.editable?options.editable:refObject.textbox('options').editable,
							disabled:options.disabled?options.disabled:refObject.textbox('options').disabled,
							readonly:options.readonly?options.readonly:refObject.textbox('options').readonly,
							validType:refObject.textbox('options').validType,
							onChange:func_change,
							inputEvents:$.extend({},$.fn.textbox.defaults.inputEvents,{
								keydown:function(e){
									if(check.datatyp=="kana_text" || check.datatyp=="alpha_text"){
										var code = e.which ? e.which : e.keyCode;
										// [,"']の入力を制御
										if (e.originalEvent.key===',' || e.originalEvent.key==='"' || e.originalEvent.key==='\'') {
											return false;
										}
									}
								}
							})
						});
						return input;
					},
					destroy: function(target){
						// 解除処理
						$(target).textbox('destroy');
					},
					getValue: function(target){
						// 編集解除時に導入値を戻す
						return $(target).textbox('getValue');
					},
					setValue: function(target, value){
						// 編集時に期待値の設定
						$(target).textbox('setValue', value);
					},
					resize: function(target, width){
						// カラム幅の resize
						$(target).textbox('resize', width);
					}
				},
				numberbox: {
					init: function(container, options){
						var input = $('<input class="datagrid-editable-input">').appendTo(container);
						var field = $(container).closest('td[field]').attr('field');
						var refId = 'txt_'+field.toLowerCase();
						if(that && that.extenxDatagridEditorIds && that.extenxDatagridEditorIds[field]){
							refId = that.extenxDatagridEditorIds[field];
						}
						var gfrmid = $(container).parents('form[id^='+$.id.gridform.replace('#', '')+']').eq(0).attr('id');
						var suffix = gfrmid!==undefined ? gfrmid.replace($.id.gridform.replace('#', '') ,'') : '';
						if(suffix!==''){
							refId += suffix;
						}
						var refObject = $('#'+refId);
						if($(refObject).length===0){ refObject = $('#'+refId.replace(/\_[0-9]+$/,"")); }
						// 参照項目から必要な情報をコピーする
						input.attr("id", refId+"_");	// 参照元ID＋"_"をIDとしてセット
						input.addClass(refObject.attr("class"));		// 基本クラス
						var tags = ["for_inp", "for_btn"];
						for (var i=0; i<tags.length; i++){
							if(refObject.is("["+tags[i]+"]")){
								input.attr(tags[i], refObject.attr(tags[i]));
							}
						}
						var func_change_base = refObject.numberbox('options').onChange;
						var rowIndex = $(container).closest('tr[datagrid-row-index]').attr('datagrid-row-index');
						var func_change = function(newValue,oldValue){
							if($.extendDatagridEditorInitializeId[rowIndex]){
								if($.isFunction(func_change_base)) func_change_base(newValue,oldValue, $(this));
							}
						};
						input.numberbox({
							cls:options.cls?options.cls:refObject.numberbox('options').cls,
							prompt:options.prompt?options.prompt:refObject.numberbox('options').prompt,
							editable:options.editable?options.editable:refObject.numberbox('options').editable,
							disabled:options.disabled?options.disabled:refObject.numberbox('options').disabled,
							readonly:options.readonly?options.readonly:refObject.numberbox('options').readonly,
							validType:refObject.numberbox('options').validType,
							formatter:refObject.numberbox('options').formatter,
							parser:refObject.numberbox('options').parser,
							precision:refObject.numberbox('options').precision,
							groupSeparator:refObject.numberbox('options').groupSeparator,
							onChange:func_change,
//							inputEvents:$.extend({},$.fn.numberbox.defaults.inputEvents,{
//								keydown:function(e){
//									var code = e.which ? e.which : e.keyCode;
//									// "-"の入力を制御
//									if (code === 189) {
//										return false;
//									}
//								}
//							})
						});
						return input;
					},
					destroy: function(target){
						// 解除処理
						$(target).numberbox('destroy');
					},
					getValue: function(target){
						// 編集解除時に導入値を戻す
						return $(target).numberbox('getValue');
					},
					setValue: function(target, value){
						// 編集時に期待値の設定
						$(target).numberbox('setValue', value);
					},
					resize: function(target, width){
						// カラム幅の resize
						$(target).numberbox('resize', width);
					}
				},
				checkbox: {
					init: function(container, options){
						var input = $('<input type="checkbox">').appendTo(container);
						var field = $(container).closest('td[field]').attr('field');
						var refId = 'chk_'+field.toLowerCase();
						if(that && that.extenxDatagridEditorIds && that.extenxDatagridEditorIds[field]){
							refId = that.extenxDatagridEditorIds[field];
						}
						var gfrmid = $(container).parents('form[id^='+$.id.gridform.replace('#', '')+']').eq(0).attr('id');
						var suffix = gfrmid!==undefined ? gfrmid.replace($.id.gridform.replace('#', '') ,'') : '';
						if(suffix!==''){
							refId += suffix;
						}
						$.initCheckboxCss($(container));
						var refObject = $('#'+refId);
						if($(refObject).length===0){ refObject = $('#'+refId.replace(/\_[0-9]+$/,"")); }
						var func_change = null;
						var events = $._data(refObject.get(0)).events;
						$.each(events.change,function(){ func_change = this.handler; });
						// 参照項目から必要な情報をコピーする
						input.attr("id", refId+"_");	// 参照元ID＋"_"をIDとしてセット
						input.addClass(refObject.attr("class"));		// 基本クラス
						input.attr('disabled', refObject.attr("disabled"));
						input.attr('readonly', refObject.attr("readonly"));
						input.change(function(e){
							if($.isFunction(func_change)) func_change(e);
						});
						return input;
					},
					getValue: function(target){
						return $(target).prop('checked');
					},
					setValue: function(target, value){
						// 編集時に期待値の設定
						$(target).prop('checked', value===$.id.value_on);
					}
				}
			});
		},
		// 作り出した編集項目の参照元項目IDを取得
		getExtendDatagridEditorRefid :function(ed){
			var id = $(ed).attr("id");
			return id.substr(0, id.length -1 );
		},
		endEditingDatagrid:function(that){
			//if($.reg.TestLog) $.log(that.timeData, 'call:endEditingDatagrid'+that.focusGridId+'-'+that.editRowIndex[that.focusGridId]+':');
			if($.reg.TestLog) $.log(that.timeData, 'call:endEditingDatagrid');

			var index = that.editRowIndex[that.focusGridId];
			if (index === undefined || index === -1){return true}
			if ($('#'+that.focusGridId).datagrid('validateRow', index)){
				that.editRowIndex[that.focusGridId] = -1;
				$.extendDatagridEditorInitializeId[index]=false;
				if($.reg.TestLog) blueLog( '→endEdit');
				$('#'+that.focusGridId).datagrid('endEdit', index);
				return true;
			} else {
				return false;
			}
		},
		appendDatagridRow:function(that, id, defrow){
			if(!that.initializes){
				that.focusGridId = id;
				if ($.endEditingDatagrid(that)){
					var row = {};
					if(defrow){
						row = $.extend(true, {}, defrow);
					}else{
						var columns = $('#'+that.focusGridId).datagrid('getColumnFields');
						for (var i=0; i<columns.length; i++){
							row[columns[i]] = null;
						}
					}
					$('#'+that.focusGridId).datagrid('appendRow',row);
					that.editRowIndex[that.focusGridId] = $('#'+that.focusGridId).datagrid('getRows').length-1;
					$('#'+that.focusGridId).datagrid('selectRow', that.editRowIndex[that.focusGridId]);
					setTimeout(function(){
						$('#'+that.focusGridId).datagrid('beginEdit', that.editRowIndex[that.focusGridId]);
					},0);
				}
			}
		},
		clickEditableDatagridCell:function(that, gridId, index){
			if($.reg.TestLog) blueLog( 'call:clickEditableDatagridCell');

			if(that.columnName && that.columnName != ""){
				that.columnName += '_'+index;
			}

			that.focusGridId = gridId;
			if (that.editRowIndex[that.focusGridId] !== index){
				if ($.endEditingDatagrid(that)){
					that.editRowIndex[that.focusGridId] = index;
					$('#'+that.focusGridId).datagrid('selectRow', index);
					if($.reg.TestLog) blueLog( '→selectRow');

					setTimeout(function(){
						$('#'+that.focusGridId).datagrid('beginEdit', index);
						if($.reg.TestLog) blueLog( '→beginEdit');

					},0);
				} else {
					if($.reg.TestLog) blueLog( '→selectRow');
					$('#'+that.focusGridId).datagrid('selectRow', that.editRowIndex[that.focusGridId]);
				}
			}
		},
		endEditDatagridRow:function(that, gridId, index, row){
			if($.reg.TestLog) blueLog( 'call:endEditDatagridRow');

			var opts = $('#'+gridId).datagrid('options');
			if (opts.view.type == 'scrollview'){
				that.editRowIndex[that.focusGridId] = -1;
				$.extendDatagridEditorInitializeId[index]=false;
			}

			var eds = $('#'+gridId).datagrid('getEditors', index);
			for (var i=0; i<eds.length; i++){
				var ed = eds[i];
				var field = ed.field;
				if(ed.type==='combobox'){
					row[ed.field] = $(ed.target).combobox('getText');
				}else if(ed.type==='numberbox'){
					row[ed.field] = $(ed.target).numberbox('getValue');
				}else if(ed.type==='textbox'){
					row[ed.field] = $(ed.target).textbox('getText');
				}else if(ed.type==='checkbox'){
					row[ed.field] = $(ed.target).is(':checked')?$.id.value_on:$.id.value_off;
				}else if($(ed.target).is(":input")){
					row[ed.field] = $(ed.target).val();
				}else{
					row[ed.field] = $(ed.target).text();
				}
			}

			// 選択項目データが存在する場合、値をクリアする。
			if(that.columnName && that.columnName != ""){
				if(index == Number(that.columnName.split("_")[1])){
					that.columnName = ''
				}
			}
		},
		beginEditDatagridRow:function(that, gridId, index, row){
			// グリッド内の行を編集中のキー移動対応
/*
			// すでに別の行を編集中の場合、別の行の編集を終わらせる
			if (that.focusGridId === gridId && that.editRowIndex[that.focusGridId] !== -1 && that.editRowIndex[that.focusGridId] !== index){
				var rt = $.endEditingDatagrid(that);
			}
*/

			if($.reg.TestLog) blueLog( 'call:beginEditDatagridRow');

			that.focusGridId = gridId;
			that.editRowIndex[that.focusGridId] = index;
			// 対象の行の入力項目をすべて取得し、
			var eds = $('#'+that.focusGridId).datagrid('getEditors', index);
			var focusTarget=null;
			var exTarget = ['chk_updkbn_']; // フォーカスイン対象外IDをここで定義
			$.extendDatagridEditorInitializeId = {};
			for (var i=0; i<eds.length; i++){
				var ed = eds[i];
				var target = undefined;
				if(ed.type==='combobox'){
					target = $(ed.target).combobox('textbox');
				}else if(ed.type==='numberbox'){
					target = $(ed.target).numberbox('textbox');
				}else if(ed.type==='textbox'){
					target = $(ed.target).textbox('textbox');
				} else if (ed.type==='checkbox') {
					var flg = true;
					for (var j = 0; j < exTarget.length; j++) {
						if ($(ed.target).attr("id")===exTarget[j]) {
							target = $(ed.target).attr("tabindex", -1);
							flg = false;
							break;
						}
					}
					if (flg) {
						target = $(ed.target);
					}
				}else{
					target = $(ed.target);
				}
				$(target).attr("tabindex",function () { if ($(this).is("[tabindex=-1]")) { return -1; } return i; }).on('keydown', function(e){

					var code = e.which ? e.which : e.keyCode;
					if($.reg.TestLog) blueLog( 'beginEditDatagridRow' + '/' + e.type + '(' + gridId + "/" + $(this).attr("id") +'=' + e.target + $(this).get(0).className + '):');

					// *** ↓ ***
					if(code === 40){
						if($(this).parent('span').is('.combo')){
							var id = $(this).parent('span').prev(".easyui-combobox").attr("id");
							if($('#'+id).is('[readonly!=readonly]')){
								$('#'+id).combobox('showPanel');
							}
						}
					}

					// *** Enter or Tab ****
					if(code === 13 || code === 9){
						// 現在グリッド上で表示されている編集項目を取得
						var targetsAll = $('#'+gridId).datagrid('getPanel').find("[tabindex]").filter("[tabindex!=-1]").filter('[disabled!=disabled]').filter(":visible").sort(function(a, b) {
							return parseInt($(a).attr('tabIndex'), 10) - parseInt($(b).attr('tabIndex'), 10);
						});
						var criteria = e.shiftKey ? ":lt(" + targetsAll.index($(this)) + "):last" : ":gt(" + targetsAll.index($(this)) + "):first";
						if(targetsAll.filter(criteria).length==0) {
							// 編集行内に移動先がない場合
							var befindex = index;

							var parentElement	 = this.parentElement.previousElementSibling
							var oldElement		 = null
							var targetId		 = parentElement.id;

							var newLine = eds.filter(function(item, index){
								  if (item.target.get(0).id == targetId){
									  oldElement = item
									  return true;
								  }
							});

							var isChage			 = false;

							if(oldElement.oldHtml != this.value){
								isChage = true;
								$(this).blur()
							}
							setTimeout(function(){
								if(isChage){
									while(true){
										var value = parentElement.value
										if(oldElement.oldHtml != value){
											// changeの変更値が反映されるまで待つ
											break;
										}
									}
								}

								if ($.endEditingDatagrid(that)){
									that.editRowIndex[that.focusGridId] = befindex;	// endEditに成功すると、現在行がクリアされるので、現在どこを選択していたかを戻しておく
									// 現在の編集行の編集が終了できた場合、次の行に移るか、次の項目に移るかする
									var adds = e.shiftKey ? -1:1;
									var nextindex = befindex + adds;
									if(nextindex >= 0 && nextindex < $('#'+gridId).datagrid('getData').total){
										$('#'+gridId).datagrid('selectRow', nextindex);
										$('#'+gridId).datagrid('beginEdit', nextindex);
									}else{
										that.editRowIndex[that.focusGridId] = -9;	// 次の項目に移動するために未編集状態と違うインデックス設定
										var evt = $.Event('keydown');
										evt.keyCode = 13;
										evt.shiftKey = e.shiftKey;
										setTimeout(function(){
											$('#'+gridId).parents('.datagrid').eq(0).trigger(evt);
										},500);
									}
								}
							},0);
						}else{
							var nextTarget = targetsAll.filter(criteria);
							nextTarget.focus();
							if(nextTarget.is(':text')&&!nextTarget.hasClass('validatebox-readonly')){ nextTarget.select(); }
						}
						e.preventDefault();
					}
				});
				// 選択されたフィールド情報が存在する場合、その項目にフォーカスを合わせる。
				if(that.columnName && that.columnName != ""){
					if(ed.field + '_' + index == that.columnName){
						if($(ed.target).is(":enabled")&&$(target).is("[tabindex!=-1]")){
							focusTarget = target
						}

						// 選択された項目がcheckBoxの場合、チェックする
						if(target[0].type == 'checkbox'){
							if($(target).attr("readonly")!=="readonly" && $(target).attr("disabled")!=="disabled"){
								target.click();
								$.setChangeIdx(index);
							}
						}
					}
				}
				if($(ed.target).is(":enabled")&&$(target).is("[tabindex!=-1]")){
					if(focusTarget===null) focusTarget = target;

					// フォーカス時イベント追加
					if(ed.type==='numberbox'){
						$(target).on('focus', function(e){
							var id = $(this).parent('span').prev(".datagrid-editable-input").attr("id").replace(/_$/,"");
							if($.reg.TestLog) blueLog( 'editorfocus' + '/' + e.type + '(' + gridId + "/" + id +'=' + e.target + '):');
							if($.isFunction(that.focusInputboxFunc)){
								that.focusInputboxFunc(that, id, $(this));
							}
						})
					}
				}
			}
			$.extendDatagridEditorInitializeId[index]=true;
			if(focusTarget!==null){
				if($.reg.TestLog) blueLog( '→focus("ID:'+focusTarget[0].id+'")');

				$(focusTarget).focus();
				if($(focusTarget).is(':text')&&!$(focusTarget).hasClass('validatebox-readonly')){ $(focusTarget).select(); }
			}
		},

		// 親項目を指定し、最初の入力可能項目にフォーカスを設定（キーイベント用）
		setFocusFirst : function(parent, that){

			if($.reg.TestLog) blueLog( 'call:setFocusFirst');

			if(parent===undefined) parent = $('.window').filter(':visible').length > 0 ? $('.window').filter(':visible').filter(':first'):$('body');
			var obj = parent.find('[tabindex]').filter("[tabindex!=-1]").filter('[disabled!=disabled]').filter(":visible").sort(function(a, b) {
				return parseInt($(a).attr('tabIndex'), 10) - parseInt($(b).attr('tabIndex'), 10);
			}).eq(0);

			// 選択項目データが存在する場合、選択された項目にフォーカスを設定する。
			if(that && that.columnName && that.columnName != "" && that.focusGridId){
				var edsIdx = parent[0].rowIndex;
				var eds = $('#'+that.focusGridId).datagrid('getEditors', edsIdx);
				for (var i=0; i<eds.length; i++){
					var ed = eds[i];

					if(ed.field + '_' + edsIdx == that.columnName){
						obj = ed.target
					}
				}
			}

			obj.focus();
			return obj;
		},
		// エラー入力項目に対する処理を行う
		addErrState :function(that, obj, setCss, opt){
			var invalid = "like_validatebox_invalid";
			var target = $.getInputboxTextbox(obj);
			// 次の項目が非表示時にはタブによる非表示と考え、タブ表示処理を行う
			if(target.is(":hidden")){
				var tabpanels = target.parents('.tabs-panels > .panel').filter(':hidden');
				for(var i=tabpanels.length-1;i>=0;i--){
					if(!tabpanels.eq(i).filter(':hidden')){break;}
					var tab = tabpanels.eq(i).closest(".easyui-tabs");
					var tabs = tab.tabs('tabs');
					for(var j=0;j<tabs.length;j++){
						if(tabs[j].find(target).length > 0){
							tab.tabs('select', j);
							break;
						}
					}
				}
			}
			if(opt){
				setTimeout(function(){
				obj.datagrid('beginEdit', opt.NO);
					setTimeout(function(){
						var jsonId = opt.ID.split(',');
						for (var i = 0; i < jsonId.length; i++) {
							target = $.getInputboxTextbox(obj.datagrid('getPanel').find("[id^="+jsonId[i]+"]"));
							if(setCss){
								target.parents('span.textbox').addClass(invalid);
								target.addClass(invalid);
							}

							if (i===0) {
								target.focus();
								if(target.is(':text')&&!target.hasClass('validatebox-readonly')){ target.select(); }
							}
						}
					},50);
				},50);
			}else{
				setTimeout(function(){
					if(setCss){
						target.parents('span.textbox').addClass(invalid);
						target.addClass(invalid);
					}
					target.focus();
					if(target.is(':text')&&!target.hasClass('validatebox-readonly')){ target.select(); }
					target.mouseover();
				},50);
			}
		},
		// 指定された行の項目のセルを赤で塗りつぶす
		addErrStateGrid: function (id, indexArray, colindexArray){
			var that = this;
			var backGroundColer = "#ff0000";
			var borderColor		= "#ffa8a8";
			var invalid			= "errGridCel";

			// 配列ではなく、数値で入力された場合は配列に変換する
			if(Array.isArray(indexArray) == false){indexArray = [indexArray]}
			if(Array.isArray(colindexArray) == false){colindexArray = [colindexArray]}

			var isScrollview = false	// スクロールビュー設定有無
			if($('#'+id).datagrid('getData').firstRows){
				isScrollview = true;
			}

			var isFrozenColumns = false	// 固定列設定有無
			var options = $('#'+id).datagrid('options')
			if(options.frozenColumns && options.frozenColumns[0].length > 0){
				isFrozenColumns = true
			}

			// グリッドのテーブル情報を取得
			var tabledata = {}
			var view1 = $('#'+id).parent().find('.datagrid-view1').find('.datagrid-btable')[0].rows
			var view2 = $('#'+id).parent().find('.datagrid-view2').find('.datagrid-btable')[0].rows

			if(isFrozenColumns){
				// 固定列が存在する場合は、通常列情報とマージする。
				var tabledataAf = []
				for (var i=0; i<view2.length; i++){
					var cells = []
					var id = view2[i].id
					var cells_v1 = view1[i].cells
					var cells_v2 = view2[i].cells

					for (var j=0; j<cells_v1.length; j++){
						cells.push( cells_v1[j] );
					}
					for (var j=0; j<cells_v2.length; j++){
						cells.push( cells_v2[j] );
					}

					tabledataAf.push({
						"id"	:id,
						"cells"	:cells
					});
				}
				tabledata = tabledataAf;
			}else{
				tabledata = view2
			}


			// セルにスタイルを設定する
			for (var i=0; i<indexArray.length; i++){
				var index = indexArray[i]
				var celldata = {}

				if(isScrollview){
					// スクロールビューの場合
					for (var k=0; k<tabledata.length; k++){
						var tr = tabledata[k]
						var id = tr.id.split('-')
						id = id[id.length -1]

						if(Number(id) == Number(index)){
							//IDにindexが含まれる列を取得
							celldata = tr.cells
							break;
						}
					}
				}else{
					var celldata = tabledata[index].cells
				}

				for(var j=0; j<colindexArray.length; j++){
					var colindex = colindexArray[j]
					var td = celldata[colindex]

					//td.classList.add(invalid);
					$(td).addClass(invalid);

					// 該当するセルにスタイルを追加する
					// td.style["backgroundColor"]	 = backGroundColer
				}
			}
			$('.'+invalid).css('backgroundColor', backGroundColer);
		},
		removeErrState :function(){
			var invalid = "like_validatebox_invalid";
			$('.'+invalid).each(function(){
				var target = $.getInputboxTextbox($(this));
				target.parents('span.textbox').removeClass(invalid);
				target.removeClass(invalid);
			});
		},
		removeErrStateGrid :function(id){
			var backGroundColer = "";
			var invalid = "errGridCel";
			// 行の色を設定を無しにする。
			$('.'+invalid).css('backgroundColor', backGroundColer);
			$('#'+id).parent().find('.'+invalid).each(function(){
				$(this).removeClass(invalid);
			});
		},
		// キーイベント初期設定
		initKeyEvent : function(that) {
			// キー移動イベントの設定
			that.focusParentId = that.focusRootId;
			$.setReadyKeyEvent(that);	// 初期化したオブジェクトに対し、キーイベントの準備を行う
			$('#'+that.focusRootId).find('[tabindex]').each(function(){ $.setKeyEvent(that, $(this)); });	// tabindexが設定された項目に対し、キーイベントの設定を行う
			$.setFocusFirst($('#'+that.focusRootId));

			// サブ画面設定処理
			// 入力項目にフォーカス時、値を全選択する設定を追加
			$.ctrlFocusSubWin(that);
		},
		prevEvent:"",
		setReadyKeyEvent : function(js) {
			// グリッドからフォーカスが外れた場合の処理
			var func_focusout_editgrid = function(e){
				if($.reg.TestLog) blueLog( 'func_focusout_editgrid ' + '/' + e.type + '(' + $(this).attr("id") + "/" + $(this).attr("id") +'=' + e.target + $(this).get(0).className + '):');
				if(js.focusGridId !==''){
					if($.reg.TestLog) blueLog( '→ExistingfocusGridId');

					if(js.editRowIndex!==undefined && js.editRowIndex[js.focusGridId]!==undefined){
						if(js.editRowIndex[js.focusGridId]!==-1){
							//if($.reg.TestLog) blueLog( '→→endEditingDatagrid' + '):');
							$.endEditingDatagrid(js);
						}
					}
				}
			};

			// マウス操作等のタブの遷移時のフォーカス指定
			$('#'+js.focusRootId).find('.easyui-tabs').each(function(){
				var id = $(this).attr('id');
				$('#'+id).tabs({
					onSelect: function(title,index){
						if($.reg.TestLog) blueLog( 'tab_onSelect:'+id+title+'('+index+')');
						if($('#'+id).attr('for_enter_target')){
							var targetid = $('#'+id).attr('for_enter_target');
							$('#'+id).removeAttr('for_enter_target');
							$('#'+targetid).focus();
						}else{
							$.setFocusFirst($($('#'+id).tabs('getTab', index)));
						}
					},
					onUnselect: function(title,index){
						if($.reg.TestLog) blueLog( 'tab_onUnselect:'+id+title+'('+index+')');
						if(js && $.isFunction(js.changeInputboxFunc)){
							js.changeInputboxFunc(js, id, index, $('#'+id));
						}
					}
				});
			});
			// easyui系の項目は、HTMLから実際画面上で操作する項目を作成するので、キーイベント用に項目の設定を行う
			$('#'+js.focusRootId).find('.easyui-datagrid_').removeClass("easyui-datagrid_").addClass("easyui-datagrid");
			$('#'+js.focusRootId).find('.easyui-datagrid').each(function(){
				var that = this;
				// targetに設定をコピー
				var target = $(that).parents('.datagrid').eq(0);
				target.attr('tabindex', $(that).attr('tabindex'));
				// キーイベントの対象となってしまうのでコピー元にtabindex=-1設定
				$(that).attr('tabindex', -1);
				// gridのキー移動は、該当gridのID指定が前提になっているため、フォーカス処理で、グリッドIDを保持、一行目選択
				target.on('mousedown focus', function(e){
					var gridId = $(that).attr('id');
					if($.reg.TestLog) blueLog('setReadyKeyEvent-easyui-datagrid-focus' + '/' + e.type + '(' + gridId + "/" + $(this).attr("id") +'=' + e.target + $(this).get(0).className + '):');

					// フォーカスがあたったグリッドと編集中グリッドが異なる場合は編集終了確認追加
					if(js.focusGridId !=='' && js.focusGridId!==gridId){
						func_focusout_editgrid(e);
					}
					if($(this).is("[tabindex!=-1]")){
						// 行がある場合、各種処理
						js.focusGridId = gridId;
						if($('#'+gridId).datagrid('getRows').length > 0){
							var doEnter = false;	// EnterKeyイベントを起こすかいなか（編集系はBeginEditするために必要、表示系はselectする場合必要）
							if(js.editRowIndex!==undefined && js.editRowIndex[js.focusGridId]!==undefined){
								doEnter = true;
							}else{
								doEnter = $('#'+js.focusGridId).datagrid('getSelected')===null;
							}
							if(doEnter){
								var type = e.handleObj["type"];

								if (type==='mousedown') {
									this.prevEvent = type;
								} else if ($.isEmptyVal(this.prevEvent)) {
									var evt = $.Event('keydown');
									evt.keyCode = 13;
									setTimeout(function(){
										$('#'+gridId).parents('.datagrid').eq(0).trigger(evt);
									},0);
								} else {
									this.prevEvent = '';
									var mouseOver = $('#'+gridId).parent().children('.datagrid-view2').find('.datagrid-row-over');
									if (mouseOver.length !== 0) {
										var newIndex = mouseOver.attr('datagrid-row-index')
										var evt = $.Event('keydown');
										evt.keyCode = 13;
										setTimeout(function(){
											$('#'+gridId).parents('.datagrid').eq(newIndex).trigger(evt);
										},0);
									}
								}
							}
						}
					}
				});
			});
			$('#'+js.focusRootId).find('.easyui-combobox_').removeClass("easyui-combobox_").addClass("easyui-combobox");
			$('#'+js.focusRootId).find('.easyui-combobox').each(function(){
				var that = this;
				// targetに設定をコピー
				var target = $(that).combobox('textbox');
				target.attr('orizinid', $(that).attr('id'));
				target.attr('tabindex', $(that).attr('tabindex'));
				target.on('focus', function(e){
					func_focusout_editgrid(e);
					$(this).parents('span.textbox').addClass("textbox-focused");
				}).on('blur', function(e){
					$(this).parents('span.textbox').removeClass("textbox-focused");
				});
				// キーイベントの対象となってしまうのでコピー元にtabindex=-1設定
				$(that).attr('tabindex', -1);
			});
			$('#'+js.focusRootId).find('.easyui-textbox_').removeClass("easyui-textbox_").addClass("easyui-textbox");
			$('#'+js.focusRootId).find('.easyui-textbox').each(function(){
				var that = this;
				// targetに設定をコピー
				var target = $(that).textbox('textbox');
				target.attr('orizinid', $(that).attr('id'));
				target.attr('tabindex', $(that).attr('tabindex'));
				target.on('focus', function(e){
					func_focusout_editgrid(e);
					$(this).select();
				});
				// キーイベントの対象となってしまうのでコピー元にtabindex=-1設定
				$(that).attr('tabindex', -1);
			});
			$('#'+js.focusRootId).find('.easyui-numberbox_').removeClass("easyui-numberbox_").addClass("easyui-numberbox");
			$('#'+js.focusRootId).find('.easyui-numberbox').each(function(){
				var that = this;
				// targetに設定をコピー
				var target = $(that).numberbox('textbox');
				target.attr('orizinid', $(that).attr('id'));
				target.attr('tabindex', $(that).attr('tabindex'));
				target.on('focus', function(e){
					func_focusout_editgrid(e);
					$(this).select();
				});
				// キーイベントの対象となってしまうのでコピー元にtabindex=-1設定
				$(that).attr('tabindex', -1);
			});
			// 編集グリッドがある場合は編集終了確認追加
			if(js.editRowIndex!==undefined){
				// ボタン
				$('#'+js.focusRootId).find('.easyui-linkbutton').filter("[tabindex!=-1]").filter('[disabled!=disabled]').each(function(){
					$(this).on('focus', function(e){ func_focusout_editgrid(e); });
				});
				// チェックボックス
				$('#'+js.focusRootId).find(':checkbox').filter("[tabindex!=-1]").filter('[disabled!=disabled]').each(function(){
					$(this).on('focus', function(e){ func_focusout_editgrid(e); });
				});
				// ラジオボタン
				$('#'+js.focusRootId).find(':radio').filter("[tabindex!=-1]").filter('[disabled!=disabled]').each(function(){
					$(this).on('focus', function(e){ func_focusout_editgrid(e); });
				});
			}
		},
		setKeyEvent : function(that, obj) {
			// ※キー移動について：タブなどで非表示項目なども対象になるため、意図的に非表示との区別がつかなかった。意図的に非表示の場合は、disabledを併せて設定することでキー移動の対象から除外したい。
			// readonly=readbnly、diabled=disabledの書き方前提
			obj.on('keydown', function(e){
				var code = e.which ? e.which : e.keyCode;

				// 帳票の特殊処理がある場合実行
				if(that&&$.isFunction(that.keyEventInputboxFunc)){
					that.keyEventInputboxFunc(e, code, that, obj);
				}

				if($.reg.TestLog) blueLog( 'setKeyEvent' + '/' + e.type + '(' + $(obj).attr("id") + "/" + $(this).attr("id") +'=' + e.target + $(this).get(0).className + '):');
				// *** space ****
				if(code === 32){
					if(($(this).is('a') && $(this).is("[id^='btn']")) || this.type === 'button' || this.type === 'submit'){
						// ボタン系はspaceでも実行可能とする
						if($(this).is('[disabled!=disabled]')){
							$(this).click();
							e.preventDefault();
						}
					}else if($(this).is('.datagrid') && that.focusGridId != undefined && that.focusGridId != "" && $('#'+that.focusGridId).datagrid('getRows').length > 0){
						// 編集系か確認
						var isEditing = undefined;
						if(that.editRowIndex!==undefined && that.editRowIndex[that.focusGridId]!==undefined){
							isEditing = that.editRowIndex[that.focusGridId] >= 0;
						}
						if(isEditing === undefined){
							// チェックボックス列有のdatagridはspaseで選択の切り替えを可能とする（selectOnCheck=false設定前提）
							if($('#'+that.focusGridId).datagrid('getPanel').find('.datagrid-header-check').length === 1){
								var checkedRows = $('#'+that.focusGridId).datagrid('getChecked');
								var selIdx = 0;
								if($('#'+that.focusGridId).datagrid('getSelected')!==null){
									selIdx = $('#'+that.focusGridId).datagrid('getRowIndex', $('#'+that.focusGridId).datagrid('getSelected'));
								}
								if(checkedRows.indexOf($('#'+that.focusGridId).datagrid('getRows')[selIdx])!==-1){
									$('#'+that.focusGridId).datagrid('uncheckRow', selIdx);
								}else{
									$('#'+that.focusGridId).datagrid('checkRow', selIdx);
								}
							}
						}
					}
				}

				// *** ↓ ***
				if(code === 40){
					if($.reg.TestLog) blueLog( '→action:combobox showPanel');
					if($(this).parent('span').is('.combo')){
						var id = $(this).parent('span').prev(".easyui-combobox").attr("id");
						if($('#'+id).is('[readonly!=readonly]')){
							$('#'+id).combobox('showPanel');
						}
					}
				}

				// *** Enter ****
				var isSearch = false;
				if(code === 13 ){
					if(($(this).is('a') && $(this).is('a[id^='+$.id.btn_search+']'))){
						if($.reg.TestLog) blueLog( '→action:Search');
						// ボタン系はspaceでも実行可能とする
						if($(this).is('[disabled!=disabled]')){
							$(this).click();
							e.preventDefault();
							isSearch = true;
						}
					}
				}

				// *** Enter or Tab ****
				if(!isSearch && (code === 13 || code === 9)){
					var adds = e.shiftKey ? -1:1;

					// データグリッド選択中の処理
					var isSet = false;
					if($(this).is('.datagrid') && that.focusGridId != undefined && that.focusGridId != "" && $('#'+that.focusGridId).datagrid('getRows').length > 0){
						// ログ
						if($.reg.TestLog) blueLog( '→focus:Datagrid');

						// 編集系か確認
						var isEditing = undefined;
						if(that.editRowIndex!==undefined && that.editRowIndex[that.focusGridId]!==undefined){
							isEditing = that.editRowIndex[that.focusGridId] >= 0;
						}

						// 編集系の編集中は編集行内のEnterイベント優先
						if(isEditing===true){
							if($.reg.TestLog) blueLog( '→→EditGrid　Editing');
							isSet = true;

						// 編集系の未編集は今自分にきたのかぬけたのか
						}else if(isEditing===false){
							if($.reg.TestLog) blueLog( '→→EditGrid　NotEditing');
							if(that.editRowIndex[that.focusGridId]===-1){
								if($.reg.TestLog) blueLog( '→→→EditGrid EnterIn');
								var rowindex = 0;
								if($('#'+that.focusGridId).datagrid('getSelected')!==null){
									rowindex = $('#'+that.focusGridId).datagrid('getRowIndex', $('#'+that.focusGridId).datagrid('getSelected'));
								}else{
									if($.reg.TestLog) blueLog( '→→→EditGrid selectrow');
									$('#'+that.focusGridId).datagrid('selectRow', rowindex);
								}
								setTimeout(function(){
									if($.reg.TestLog) blueLog( '→→→EditGrid beginEdit:');
									$('#'+that.focusGridId).datagrid('beginEdit', rowindex);
								},0);
								isSet = true;
							}else if(that.editRowIndex[that.focusGridId]===-9){
								if($.reg.TestLog) blueLog( '→→→EditGrid EnterOut');
								that.editRowIndex[that.focusGridId]=-1;
								isSet = false;
							}
						// 編集できないGrid
						}else if(isEditing===undefined){
							if($.reg.TestLog) blueLog( '→→NotEditGrid');

							var selIdx = 0;
							if($('#'+that.focusGridId).datagrid('getSelected')!==null){
								selIdx = $('#'+that.focusGridId).datagrid('getRowIndex', $('#'+that.focusGridId).datagrid('getSelected'));
								selIdx = selIdx + adds;
							}
							if(selIdx >= 0 && selIdx < $('#'+that.focusGridId).datagrid('getRows').length){
								if($.reg.TestLog) blueLog( '→NotEditGrid→→selectrow');
								$('#'+that.focusGridId).datagrid('selectRow', selIdx);
								$('#'+that.focusGridId).datagrid('scrollTo', selIdx);
								isSet = true;
							}
						}
					}

					// その他項目選択中の処理
					if(!isSet && this.type !== 'submit' && ($(this).is(':input')||$(this).is('a')||$(this).is('.datagrid'))){
						if($.reg.TestLog) blueLog( '→focus:DefInput');

						that.focusGridId = "";
						// tabIndexが設定されている有効な項目すべてを取得
						var targetsAll = $('#'+that.focusParentId).find("[tabindex]").filter("[tabindex!=-1]").filter('[disabled!=disabled]').sort(function(a, b) {
							return parseInt($(a).attr('tabIndex'), 10) - parseInt($(b).attr('tabIndex'), 10);
						});
						if(targetsAll.length < 2){
							$(this).focus();
						}else{
							// tabIndex有効項目と画面上の表示項目の数が一致している場合、単純に表示項目のみ前提の遷移を行う
							if(targetsAll.length === targetsAll.filter(":visible").length){
								var index = targetsAll.index(this);
								var criteria = e.shiftKey ? ":lt(" + index + "):last" : ":gt(" + index + "):first";
								if(targetsAll.filter(criteria).length==0) criteria = e.shiftKey ? ":last" : ":eq(0)";
								targetsAll.filter(criteria).focus();
							}else{	// tabIndex有効項目と画面上の表示項目の数が一致しない場合、タブによる非表示項目ありと考え、タブ表示を考慮した遷移を行う
								var index = targetsAll.index(this);
								var criteria = e.shiftKey ? ":lt(" + index + "):last" : ":gt(" + index + "):first";
								if(targetsAll.filter(criteria).length==0) criteria = e.shiftKey ? ":last" : ":eq(0)";
								var target = targetsAll.filter(criteria);
								// 次の項目が非表示時にはタブによる非表示と考え、タブ表示処理を行う
								if(target.is(":hidden")){
									var targetid = target.attr('id');
									if(!targetid && target.is('.datagrid')){
										targetid = target.find('.easyui-datagrid').attr('id');
									}
									var tabid = target.parents('.easyui-tabs').filter(':first').attr('id');
									if($(this).parents('.easyui-tabs').filter(':first').attr('id')===tabid){
										var tabs = $('#'+tabid).tabs('tabs');
										var panelIndex = $('#'+tabid).tabs('getTabIndex', $('#'+tabid).tabs('getSelected'));
										// 現在フォーカス中の項目の親タブと遷移先項目の親タブが一致した場合、次のパネルに移動
										panelIndex = panelIndex + adds;
										// 次のパネルのIndexが存在しない場合、パネルの最初か最後に移動
										if(panelIndex < 0){ panelIndex = tabs.length - 1;}
										if(panelIndex >= tabs.length){ panelIndex = 0;}

										$('#'+tabid).attr('for_enter_target', target.attr('id'));
										$('#'+tabid).tabs('select', panelIndex);
									}else{
										// 現在フォーカス中の項目の親タブと遷移先項目の親タブが一致しない場合、次の項目が存在するパネルに移動したい
										var isselect = false;
										$(target.parents('.easyui-tabs').get().reverse()).each(function(){
											var tabid = $(this).attr('id');
											var tabs = $('#'+tabid).tabs('tabs');
											for(var i=0;i<tabs.length;i++){
												if(tabs[i].find(target).length > 0){
													if($(tabs[i]).is(':hidden')){
														$('#'+tabid).attr('for_enter_target', target.attr('id'));
														$('#'+tabid).tabs('select', i);
														isselect = true;
													}
													break;
												}
											}
											return !isselect;
										});
									}
								}else{
									// 次の項目をフォーカス
									if($.reg.TestLog) blueLog( '→→NexstFocus');
									target.focus();
								}
							}
						}
						isSet=true;
					}
					if(isSet){
						e.preventDefault();
					}
				}
			});
		},

		getTargetValue : function() {
			// 指定要素の初期化
			try{
				var sendParam = $($.id.hiddenSendParam).val();
				if (typeof sendParam === 'string') {
					return JSON.parse(sendParam);
				}
			} catch(e){
			}
		},

		getInitValue : function() {
			// 指定要素の初期化
			try{
				var initParam = $($.id.hiddenInit).val();
				if (typeof initParam === 'string') {
					return JSON.parse(initParam);
				}
			} catch(e){
			}
		},

		getBackJSON : function (that, states, isStartpage, notBreak) {
			var newrepinfos = [];
			var flag = true;
			notBreak = notBreak ? notBreak : false;
			var repstatesBef = $.getJSONObject(that.jsonHidden, "repinfo");
			if(!isStartpage&&repstatesBef){
				var repinfos = [];
				repinfos = repinfos.concat(repstatesBef.value);
				for (var i = 0; i < repinfos.length; i++) {
					if (repinfos[i].id === that.name) {
						newrepinfos.push({id:that.name, value:states});
						flag = false;
						if (!notBreak) {
							break;
						}
					}else{
						newrepinfos.push({id:repinfos[i].id, value:repinfos[i].value});
					}
				}
			}
			if (flag) {
				newrepinfos.push({id:that.name, value:states});
			}
			return newrepinfos;
		},
		getBackBaseJSON : function (that) {
			that.getEasyUI();	// 現時点の項目の値取得

			var id = $.id.gridholder;
			var row = "";
			var rowIndex = "";
			var length = 1;
			var idArry = {};
			idArry[0] = $.id.gridholder;

			if (!$.isEmptyVal(that.scrollToId)) {
				length += that.scrollToId.length
				for (var i = 0; i < that.scrollToId.length; i++) {
					idArry[i+1] = that.scrollToId[i];
				}
			}

			for (i = 0; i < length; i++) {
				id = i === 0 ? idArry[i] : '#'+idArry[i];
				if ($(id).length!==0) {
					row = $(id).datagrid("getSelected");
					rowIndex = $(id).datagrid("getRowIndex", row);
					if (!row) {
						rowIndex = "";
					}
				}

				if (!$.isEmptyVal(rowIndex)) {
					// 選択行
					that.jsonTemp.push({
						id:		"scrollToIndex_" + idArry[i],
						value:	rowIndex,
						text:	rowIndex
					});
				}
				rowIndex = "";
			}

			var states = {};
			states["SRCCOND"] = that.jsonString;	// 検索時点の画面項目の値
			states["TMPCOND"] = that.jsonTemp;		// 現在時点の画面項目の値
			states["SENDBTNID"]=that.sendBtnid;		// 呼出ボタンID情報（当画面の呼出ボタン）
			states["PUSHBTNID"]=that.pushBtnid;		// 実行ボタンID情報（当画面で実行ボタン）
			return states;
		},

		tryClickSearch : function(){
			// 初期表示時に検索実行
			if ($.reg.search) {
				// 検索ボタン押下
				$('#'+$.id.btn_search).trigger('click');
			}
			// 検索ボタンにフォーカス
			$('#'+$.id.btn_search).focus();
		},

		tryShowToolbar : function(id,that){
			// （オプション）ツールバーの表示 Event
			// ツールバーが非表示の場合、1度だけ表示＆リサイズ
			// combbox,combgrid 構築時の要素変動を非表示に設定
			var toolbar = $(id);
			if ((toolbar.is(':hidden'))) {
				toolbar.show();
				that.setResize();
			}
		},

		tryChangeURL : function(url){
			// datagrid.url 定義
			// Load処理回避
			var options = $($.id.gridholder).datagrid('options');
			if(options) options.url = url;
		},

		setButtonState : function(id,status,call){
			// EasyUIのLinkButton enable / disable 切替
			$.log((new Date()).getTime(), call + '(' +id + '=' + status + '):');
			if (status) {
				// status == true >> enable
				$(id).linkbutton('enable');
			} else {
				// status == false >> disable
				$(id).linkbutton('disable');
			}
		},

		getDefaultPageSize: function(pageSize, pageList) {
			// 指定頁サイズが頁リストに存在するか確認
			if (pageSize==='' || !isFinite(pageSize)){
				pageSize = 0;
			}
			if ($.inArray(pageSize, pageList)===-1) {
				pageSize=pageList[0];
			}
			return pageSize;
		},

		/**
		 * datagrid sortable=ture のタイトルにアンダーバー
		 * @param id datagrid
		 * @param SortName field情報
		 * @param SortOrder "asc" | "desc" (default : asc)
		 */
		getDecorationUnderline : function(Title){
			if (Title === null || Title === undefined) {
				return '';
			}
			return "<span style='text-decoration:underline;'>"+Title+"</span>";
		},

		/**
		 * datagridの初期ソートアイコン表示
		 * @param id datagrid
		 * @param SortName field情報
		 * @param SortOrder "asc" | "desc" (default : asc)
		 */
		setDefaultSortColumnCSS : function (id, SortName, SortOrder) {
			// カラム情報取得
			var header = $(".datagrid-header-row").find('[field="'+SortName+'"]');
			if (header.length===0) return false;

			// 並び替え名称の確認
			if ($(id).datagrid('options').sortName === null) {
				// 初期ソートカラムのアイコン追加
				SortOrder=SortOrder||"asc";
				var cls='datagrid-sort-'+SortOrder;
				$(header).addClass(cls);
			} else {
				// 初期ソートカラムのアイコン削除
				$(header).removeClass("datagrid-sort-asc datagrid-sort-desc");
			}
		},

		/**
		 * Excel出力用タイトル作成
		 * @param {Object} title
		 * @param {Object} columns
		 * @return {Object} rtn
		 */
		outputExcelTitle : function(title, columns){
			var rtn = [];

			// カラム情報の読込
			var colIdx = -2;	// 列番号
			var flag = true;	// 次の列の処理続行フラグ
			var esc = [];		// 行単位で、作業済みのcolIdx+1を保持
			var org = [];		// 行単位で、columns配列indexを保持

			while (flag){
				colIdx++;
				flag = false;

				for (var rowIdx=0; rowIdx<columns.length; rowIdx++){
					if (colIdx == -1){
						// 初期設定
						flag = true;
						esc[rowIdx] = 0;
						org[rowIdx] = 0;
						rtn[rowIdx] = [];

						// title配列が空でない場合、先にrtn配列に入れておく
						if (title.length){
							if (rowIdx < title.length){
								rtn[rowIdx] = title[rowIdx].slice(0);
							} else {
								rtn[rowIdx] = title[title.length-1].slice(0);
							}
						}

					} else if (org[rowIdx] < columns[rowIdx].length && esc[rowIdx] <= colIdx){
						flag = true;

						if (columns[rowIdx][org[rowIdx]].title != null){
							var str = columns[rowIdx][org[rowIdx]].title.replace(/\<[\/]?(div|span|a)[^\<]*\>/g, '').replace(/\<br[^\<]*\>/g, '\n');
							// data 属性に置換
							if (columns[rowIdx][org[rowIdx]].data){
								str = columns[rowIdx][org[rowIdx]].data;
							}
							var colspan = columns[rowIdx][org[rowIdx]].colspan;
							var rowspan = columns[rowIdx][org[rowIdx]].rowspan;
							rtn[rowIdx].push(str);
							esc[rowIdx] += 1;
							org[rowIdx] += 1;

							if (colspan != null && rowspan != null){
								for (var col=1; col<colspan; col++){
									rtn[rowIdx].push(str);
									esc[rowIdx] += 1;
								}
								for (var row=1; row<rowspan && rowIdx+row<columns.length; row++){
									for (var col=0; col<colspan; col++){
										rtn[rowIdx+row].push(str);
										esc[rowIdx+row] += 1;
									}
								}
							} else if (colspan != null){
								for (var col=1; col<colspan; col++){
									rtn[rowIdx].push(str);
									esc[rowIdx] += 1;
								}
							} else if (rowspan != null){
								for (var row=1; row<rowspan && rowIdx+row<columns.length; row++){
									rtn[rowIdx+row].push(str);
									esc[rowIdx+row] += 1;
								}
							}
						}
					}
				}
			}
			return rtn;
		},

		/**
		 * Excel出力用レコード作成
		 * @param {Object} loadData
		 * @param {Object} options
		 * @param {Object} level
		 * @param {Object} frow
		 * @param {Object} crow
		 * @return {Object} data
		 */
		outputExcelRows : function(loadData, options, level, frow, crow){
			var data = [];
			var levelSpace=new Array(level).join('　');
			for ( var row in loadData ) {
				if (row.match(/^[0-9]+$/)) {
					var rowData = [];
					// 固定カラム情報
					for (var column in options.frozenColumns[frow]) {
						if (column.match(/^[0-9]+$/)) {
							if (options.treeField===options.frozenColumns[frow][column].field){
								rowData.push(levelSpace+loadData[row][options.frozenColumns[frow][column].field]);
							} else {
								rowData.push(loadData[row][options.frozenColumns[frow][column].field]);
							}
						}
					}
					// カラム情報
					for (var column in options.columns[crow]) {
						if (column.match(/^[0-9]+$/)) {
							rowData.push(loadData[row][options.columns[crow][column].field]);
						}
					}
					data.push(rowData);
				}
				// 下層情報
				if (loadData[row]['children']!=undefined){
					var rows = $.outputExcelRows(loadData[row]['children'],options, (level+1), frow, crow);
					for (var i=0, l=rows.length; i<l; i++) {
						data.push(rows[i]);
					}
				}
			}
			return data;
		},


		/**
		 * Excel出力用中段テーブルレコード作成
		 * @param {Object} loadData
		 * @param {Object} options
		 * @param {Object} level
		 * @param {Object} frow
		 * @param {Object} crow
		 * @return {Object} data
		 */
		outputExcelAddDataTable : function(selectors){
			var data = [];
			var rows = $(selectors);
			rows.each(function(i, row){
				var rowData = [];
				var colIdx = 0;
				$(row).find("td").each(function(j, col){
					var typ = "";
					var cellType = "";
					var format="";
					var val = "";
					var colspan = 0, rowspan = 0;
					if($(col).is(".header")){
						typ = "title";
						format = "@";
						val = $(col).text();
					}else if($(col).find("span").length > 0){
						typ = "data";
						format = $(col).find("span").attr('format');
						if(!format) format = "@";
						val = $(col).find("span").text();
						if(format!=="@"){
							val = val.replace(/,/g, '').replace("%", "");
						}
					}else if($(col).find("textarea").length > 0){
						typ = "data";
						format = "@\n";
						val = $(col).find("textarea").val();
					}else if($(col).find(":input").length > 0){
						typ = "data";
						format = $(col).find(":input").attr('format');
						if(!format) format = "@";
						val = $(col).find(":input").val();
						if(format!=="@"){
							val = val.replace(/,/g, '').replace("%", "");
						}
					}
					colspan = $(col).attr("colspan");
					rowspan = $(col).attr("rowspan");
					if (!colspan) colspan = 1;
					if (!rowspan) rowspan = 1;
					rowData.push({
						colNo:	colIdx,
						type:	typ,
						value:	val,
						format:	format,
						colspan:colspan,
						rowspan:rowspan
					});
					colIdx++;
				});
				data.push({
					rowNo:	i,
					data:	rowData
				});
			});
			return data;
		},

		/**
		 * タイトル数が規定値（255）超えていないか確認
		 * @param title タイトル情報
		 * @return true: 規定数を超えている、false: 規定数以内
		 */
		checkExcelTitle: function(title){
			return false;
//			if (this.getExcelTitle(title)>255){
//				alert("列数が255を超えているためExcel出力できません。");
//				return true;
//			}else{
//				return false;
//			}
		},
		/**
		 * タイトル数取得
		 */
		getExcelTitle: function(title){
			var titles = title.length;
			if (titles===0) return 0;
			return title[titles-1].length;
		},
		/**
		 * （必須）初期表示完了時の検索実行処理
		 * @param {Object} that	- 各画面用Js
		 */
		initialSearch : function(that){
			console.log(that.initedObject.length + ' = ' + that.initedObject[that.initedObject.length-1] + ", that.initObjNum="+that.initObjNum);
			if (that.initedObject.length == that.initObjNum){
				that.initObjNum = -1;
				// パネル表示状況取得
				var panelState = $.getJSONValue(that.jsonHidden, $.id.panelState);
				if (panelState===""){
					panelState=true;
				}
				if (!panelState) {
					// 検索条件エリアを縮小
					$($.id.toolbar).panel('collapse', false);
				} else {
					// ツールバー表示
					$.tryShowToolbar($.id.toolbar, that);
				}
				if (typeof that.setInitSets === 'function'){
					that.setInitSets();	// 期間初期値取得
				}

				// 自動検索＝有効またはChangeReport経由の場合
				if($.reg.search || that.onChangeReport){
					that.onChangeReport = true;
					setTimeout(function(){
						// 検索ボタン押下
						$.tryClickSearch();
					}, 100);
				}else{
					// マスク削除
					$.removeMask();
				}
			}
		},
		/**
		 * （必須）初期表示完了時の処理
		 * @param {Object} that	- 各画面用Js
		 */
		initialDisplay : function(that){
			console.log(that.initedObject.length + ' = ' + that.initedObject[that.initedObject.length-1] + ", that.initObjNum="+that.initObjNum);
			if (that.initedObject.length == that.initObjNum){
				that.initObjNum = -1;
				// パネル表示状況取得
				var panelState = $.getJSONValue(that.jsonHidden, $.id.panelState);
				if (panelState===""){
					panelState=true;
				}
				if (!panelState) {
					// 検索条件エリアを縮小
					$($.id.toolbar).panel('collapse', false);
				} else {
					// ツールバー表示
					$.tryShowToolbar($.id.toolbar, that);
				}
				if (typeof that.setInitSets === 'function'){
					that.setInitSets();	// 期間初期値取得
				}

				// ChangeReport経由の場合
				if(that.onChangeReport){
					that.onChangeReport = true;
					setTimeout(function(){

						// レポート番号取得
						var reportno=$($.id.hidden_reportno).val();

						// レポート定義位置
						var reportNumber = $.getReportNumber(reportno);
						if (typeof(reportNumber) !== 'number') { alert("レポート定義が見つかりません。"); return false;}

						// マスク削除
						$.removeMaskMsg();
						$.removeMask();

						// フォーム情報取得
						$.report[reportNumber].getEasyUI();

						if ($.report[reportNumber].validation()) {
							// マスク追加
							$.appendMaskMsg();
							$.appendMask();

							// セッションタイムアウト、利用時間外の確認
							var isTimeout = $.checkIsTimeout();
							if (! isTimeout) {
								// 検索実行
								$.report[reportNumber].success(reportno);
							}
						}
						return true;
					}, 100);
				}else{
					// マスク削除
					$.removeMaskMsg();
					$.removeMask();
				}
			}
		},

		/**
		 * （必須）セッションタイムアウト、利用時間外の確認
		 * @param {Object} that	- 各画面用Js
		 */
		checkIsTimeout : function(that){
			if(that != null && that.initObjNum != null && that.initObjNum > 0){
				// 帳票初期表示時は、確認しない
				return false;
			}
			var rt = false;
			$.ajax({
				url: $.reg.easy,
				type: 'POST',
				async: false,
				data: {
					"page"	: $($.id.hidden_reportno).val(),
					"action": $.id.action_get,
					"sel"	: (new Date()).getTime(),
					"json"	: "",
					"obj"	: $.id.btn_search,
					"userid": $($.id.hidden_userid).val()
				},
				success: function(json){
					var data = JSON.parse(json);
					if (data.rows[0][1]==="1") {
						// 頁のリフレッシュ
						window.parent.location = window.parent.location;
						rt = true;
					}
				}
			});
			return rt;
		},

		/**
		 * 検索ボタンイベント
		 * @param {Object} e
		 */
		pushSearch : function(e){
			if ($(this).linkbutton('options').disabled)	return false;

			// レポート番号取得
			var reportno=$($.id.hidden_reportno).val();

			// レポート定義位置
			var reportNumber = $.getReportNumber(reportno);
			if (typeof(reportNumber) !== 'number') { alert("レポート定義が見つかりません。"); return false;}

			// マスク削除
			$.removeMask();

			// フォーム情報取得
			$.report[reportNumber].getEasyUI();

			if ($.report[reportNumber].validation()) {
//				// 検索ボタン無効化
//				$.setButtonState('#'+$.id.btn_search, false, 'success');

				// マスク追加
				$.appendMask();

				// セッションタイムアウト、利用時間外の確認
				var isTimeout = $.checkIsTimeout();
				if (! isTimeout) {
					// 検索条件保持
					$.ajax({
						url: $.reg.easy,
						type: 'POST',
						async: false,
						data: {
							"page"	: reportno,
							"obj"	: $.id.btn_search,
							"sel"	: "json",
							"userid": $($.id.hidden_userid).val(),
							"user"	: $($.id.hiddenUser).val(),
							"report": $($.id.hiddenReport).val(),
							"json"	: JSON.stringify($.report[reportNumber].getJSONString())
						},
						success: function(json){
							// 検索実行
							$.report[reportNumber].success(reportno, 0, $.id.btn_search);
						}
					});
				}
				return true;
			} else {
				return false;
			}
		},

		/**
		 * 検索処理エラー判定
		 * @param {String} json
		 */
		searchError : function(json, gridid, that){
			gridid = gridid===undefined?$.id.gridholder:'#'+gridid;
			var isErr = false;
			if (json == null || json.length < 1){
				// メッセージ表示
				$.showMessage('E00011');
				isErr = true;
			}else{
				// TODO:いずれは全体に適応
				if(that && ['Out_Reportx001', 'Out_ReportTG040', 'Out_Reportx151', 'Out_Reportx003', 'Out_Reportx244', 'Out_Reportx249','Out_Reportx250','Out_Reportx280'].indexOf(that.name) !== -1 ){
					var jsonp = JSON.parse(json);
					if(jsonp.total == null || jsonp.total === 0){
						$.showMessage('E11003');
						isErr = false;
					}
				}
			}
			if(isErr){
				// マスク削除
				$.removeMask();
				if($(gridid).hasClass("datagrid-f")){
					$(gridid).datagrid('loaded');
				}else{
					$.removeMaskMsg();
				}
				return true;
			}
			return false;
		},

		/**
		 * 帳票の状態保持（ページサイズ、パネル表示on/off、検索条件）
		 */
		saveState : function(reportno, jsonString, datagrid){
			if (jsonString == null) return false;
			// ページサイズの取得
			var options = $(datagrid).datagrid('options');
			// ページサイズの保持
			var pageSize = $.getJSONValue(jsonString, $.id.pageSize);
			if (pageSize===""){
				// 未定義の場合、新規追加
				jsonString.push({
					id:		$.id.pageSize,
					value:	options.pageSize,
					text:	options.pageSize
				});
			} else {
				// 定義済の場合、最新情報の設定
				$.getJSONObject(jsonString, $.id.pageSize).value = options.pageSize;
			}

			// 検索条件パネルの表示状況
			var panelState = this.getToolbarState($.id.toolbar);
			var collapsible = $.getJSONValue(jsonString, $.id.panelState);
			if (collapsible===""){
				// 未定義の場合、新規追加
				jsonString.push({
					id:		$.id.panelState,
					value:	panelState,
					text:	panelState
				});
			} else {
				// 定義済の場合、最新情報の設定
				$.getJSONObject(jsonString, $.id.panelState).value = panelState;
			}

			// 検索条件保持
			$.ajax({
				url: $.reg.easy,
				type: 'POST',
				async: false,
				data: {
					"page"	: reportno,
					"obj"	: $.id.btn_search,
					"target":"state",
					"sel"	: "json",
					"userid": $($.id.hidden_userid).val(),
					"user"	: $($.id.hiddenUser).val(),
					"report": $($.id.hiddenReport).val(),
					"json"	: JSON.stringify(jsonString)
				},
				success: function(){
					// マスク削除
					$.removeMask();
				},
				error: function(){
					// マスク削除
					$.removeMask();
				}
			});
		},

		/**
		 * 帳票の状態保持（ページサイズ、パネル表示on/off、検索条件）
		 */
		saveState2 : function(reportno, jsonString){
			if (jsonString == null) return false;

			// 検索条件保持
			$.ajax({
				url: $.reg.easy,
				type: 'POST',
				async: false,
				data: {
					"page"	: reportno,
					"obj"	: $.id.btn_search,
					"target":"state",
					"sel"	: "json",
					"userid": $($.id.hidden_userid).val(),
					"user"	: $($.id.hiddenUser).val(),
					"report": $($.id.hiddenReport).val(),
					"json"	: JSON.stringify(jsonString)
				},
				success: function(){
					// Loading非表示
					$.removeMaskMsg();
					// マスク削除
					$.removeMask();
				},
				error: function(){
					// Loading非表示
					$.removeMaskMsg();
					// マスク削除
					$.removeMask();
				}
			});
		},

		/**
		 * マスク表示
		 */
		appendMask: function(){
			// マスク追加
			var _1ac=parent.$("#container");
			$("<div class=\"datagrid-mask\" style=\"display:block\"></div>").appendTo(_1ac);
		},

		/**
		 * マスク削除
		 */
		removeMask: function(){
			// マスク追加
			var _1ad=parent.$("#container");
			_1ad.children("div.datagrid-mask").remove();
		},

		/**
		 * マスクメッセージ表示(datagridが無いページ用)
		 */
		appendMaskMsg: function(loadMsg){
			if( loadMsg===undefined || loadMsg===null ){
				loadMsg = $.fn.datagrid.defaults.loadMsg;
			}
			var panel = parent.$("#container");
			var msg=$("<div class=\"datagrid-mask-msg\" style=\"display:block;left:50%;font-size: 12px;\"></div>").html(loadMsg).appendTo(panel);
			msg._outerHeight(40);
			msg.css({marginLeft:(-msg.outerWidth()/2),lineHeight:(msg.height()+"px")});
		},

		/**
		 * マスクメッセージ削除(datagridが無いページ用)
		 */
		removeMaskMsg: function(){
			var panel = parent.$("#container");
			if(panel.children("div.datagrid-mask-msg").length > 0){
				panel.children("div.datagrid-mask-msg").remove();
			}
		},

		getKikanYM: function(id,target,flag){
			// grid 取得
			var grid = $(id).combogrid('grid');
			if (grid == undefined)	return false;
			var data = grid.datagrid('getData');
			if (flag){
				for(var i=0;i<data.rows.length;i++){
					if(data.rows[i].NENDO_Y===target){
						return data.rows[i].VALUE;
					}
				}
			}else{
				for(var i=data.rows.length-1;i>=0;i--){
					if(data.rows[i].NENDO_Y===target){
						return data.rows[i].VALUE;
					}
				}
			}
			return "";
		},
		/**
		 * ツールバーの表示状況
		 */
		getToolbarState : function(id){
			// ツールバーが表示中は、true, 非表示中は、false
			return !$(id).is(':hidden');
		},

		/**
		 * showPanel action
		 */
		setScrollGrid : function(that){
			// grid 取得
			var grid = $(that).combogrid('grid');
			if (grid == undefined)	return false;

			// 選択済情報取得
			var row = grid.datagrid('getSelected');
			if (row == undefined)	return false;
			var rows = grid.datagrid('getRows');

			// DataGrid の onSelect イベントを無効化（塗りつぶし）
			var onSelect = grid.datagrid('options').onSelect;
			grid.datagrid('options').onSelect = function(){};

			// DataGrid の onChange イベントを無効化（塗りつぶし）
			var onChange = $(that).combo('options').onChange;
			$(that).combo('options').onChange = function(){};

			// DataGrid の onSelect イベントを元に戻す
			grid.datagrid('options').onSelect = onSelect;

			// Panel 表示後に選択
			grid.datagrid('scrollTo', grid.datagrid('getRowIndex', row[0]));

			// DataGrid の onChange イベントを元に戻す
			$(that).combo('options').onChange = onChange;
		},

		/**
		 * showPanel action
		 */
		setScrollGridOutput : function(that){
			// grid 取得
			var grid = $(that).combogrid('grid');
			if (grid == undefined)	return false;

			// 選択済情報取得
			var rows = grid.datagrid('getRows');
			var scroll = 0;
			for (var i=0;i<rows.length;i++){
				if (rows[i]['TY']!==''){	scroll=i;break;	}
				if (rows[i]['TP']!==''){	scroll=i;break;	}
				if (rows[i]['ZY']!==''){	scroll=i;break;	}
				if (rows[i]['ZP']!==''){	scroll=i;break;	}
				if (rows[i]['ZC']!==''){	scroll=i;break;	}
				if (rows[i]['ZS']!==''){	scroll=i;break;	}
			}

			// DataGrid の onSelect イベントを無効化（塗りつぶし）
			var onSelect = grid.datagrid('options').onSelect;
			grid.datagrid('options').onSelect = function(){};

			// DataGrid の onChange イベントを無効化（塗りつぶし）
			var onChange = $(that).combo('options').onChange;
			$(that).combo('options').onChange = function(){};

			// DataGrid の onSelect イベントを元に戻す
			grid.datagrid('options').onSelect = onSelect;

			// Panel 表示後に選択
			grid.datagrid('scrollTo', scroll);

			// DataGrid の onChange イベントを元に戻す
			$(that).combo('options').onChange = onChange;
		},

		/**
		 * showPanel action
		 */
		setScrollComboBox : function(id){
			// grid 取得
			var _$id = $('#'+id);
			if (_$id == undefined)	return false;

			// 選択済情報取得
			var row = _$id.combobox('getValues');
			if (row == undefined)	return false;
			var rows = _$id.combobox('getData');
			var scroll = 0;
			if (row.length>0){
				for (var i=0; i<rows.length;i++){
					if (row[0]===rows[i].VALUE){
						scroll = i;
						break;
					}
				}
			}

			// combobox の onChange イベントを無効化（塗りつぶし）
			var onChange = _$id.combo('options').onChange;
			_$id.combo('options').onChange = function(){};

			// Panel 表示後に選択
			_$id.combobox('scrollTo', scroll);

			// combobox の onChange イベントを元に戻す
			_$id.combo('options').onChange = onChange;
		},

		/**
		 * form.submit() 画面遷移用
		 */
		SendForm : function(s){
			var func = function(){
				var def = {
					type: 'get',
					url: location.href,
					data: {}
				};

				s = jQuery.extend(true, s, jQuery.extend(true, {}, def, s));

				var form = $('<form style="display:none;">')
					.attr({
						'method': s.type,
						'action': s.url
					})
					.appendTo(top.document.body);

				for (var a in s.data) {
					$('<input>')
						.attr({
							'name': a,
							'value': s.data[a]
						})
						.appendTo(form[0]);
				};
				form[0].submit();
			};
			$.confirmReportUnregist(func);
		},

		/**
		 * タブによる帳票移動（検索条件保持）
		 * @param that
		 */
		changeReportByTabs : function(that){
			if ($.reg.changeReportByTabs) {
				$('#tabs a', window.parent.document).click(function(){
					// タブ要素(a)取得
					var elems = $('#tabContent', window.parent.document).map(
						function(i,e) {
							return e;
						}).get();
					var href = elems[0].value.split(',');

					// 遷移判定
					var index = $('#tabs a', window.parent.document).index(this);
					var childurl = href[index+1];

					// JSON Object Clone ()
					var sendJSON = JSON.parse( JSON.stringify( that.jsonString ) );
					$.setJSONObject(sendJSON, 'sendMode',	0,	0);	// 日付初期化のため

					$.SendForm({
						type: 'post',
						url: childurl,
						data: {
							sendMode:	0,
							sendParam:	JSON.stringify( sendJSON )
						}
					});

					return false;
				});
			}else{

				$('#tabs a', window.parent.document).click(function(){
					return $.confirmUnregist();
				});

				// TODO:遷移時もEasyUiのメッセージを使えるようにする
//				$('#tabs').tabs({
//					onSelect:function(title){
//						var obj = $($.id.hiddenChangedIdx);
//						if(obj.length===0){obj = $(this).parent($.id.hiddenChangedIdx);}
//						// 	未登録の警告メッセージが必要かどうかチェック
//						if($.getConfirmUnregistFlg(obj)){
//							var func = function(){
//								$(obj).val("");
//								$('#tabs').tabs('select', title);
//							};
//							// 登録系の場合、変更があった場合に確認メッセージ
//							$.showMessage('E11025', undefined, func);
//							return false;
//						}
//					}
//				});
//				var tabobj = $('#tabs', window.parent.document);
//				$('#tabs a', window.parent.document).each(function(){
//					var target = $(this);
//					var title = target.text();
//					target.on('click', function(e) {
//						var obj = $($.id.hiddenChangedIdx);
//						if(obj.length===0){obj = $(this).parent($.id.hiddenChangedIdx);}
//						// 	未登録の警告メッセージが必要かどうかチェック
//						if($.getConfirmUnregistFlg(obj)){
//							var func = function(){
//								$(obj).val("");
//								tabobj.tabs('select', title);
//							};
//							// 登録系の場合、変更があった場合に確認メッセージ
//							$.showMessage('E11025', undefined, func);
//							return false;
//						}
//					});
//				});

				// TODO:遷移時もEasyUiのメッセージを使えるようにする
//				$('#tabs').tabs({
//					onSelect:function(title){
//						var obj = $($.id.hiddenChangedIdx);
//						if(obj.length===0){obj = $(this).parent($.id.hiddenChangedIdx);}
//						// 	未登録の警告メッセージが必要かどうかチェック
//						if($.getConfirmUnregistFlg(obj)){
//							var func = function(){
//								$(obj).val("");
//								$('#tabs').tabs('select', title);
//							};
//							// 登録系の場合、変更があった場合に確認メッセージ
//							$.showMessage('E11025', undefined, func);
//							return false;
//						}
//					}
//				});
//				var tabobj = $('#tabs', window.parent.document);
//				$('#tabs a', window.parent.document).each(function(){
//					var target = $(this);
//					var title = target.text();
//					target.on('click', function(e) {
//						var obj = $($.id.hiddenChangedIdx);
//						if(obj.length===0){obj = $(this).parent($.id.hiddenChangedIdx);}
//						// 	未登録の警告メッセージが必要かどうかチェック
//						if($.getConfirmUnregistFlg(obj)){
//							var func = function(){
//								$(obj).val("");
//								tabobj.tabs('select', title);
//							};
//							// 登録系の場合、変更があった場合に確認メッセージ
//							$.showMessage('E11025', undefined, func);
//							return false;
//						}
//					});
//				});
			}
		},


		/**
		 * 未登録の警告メッセージが必要かどうかチェック
		 * @param obj 未登録チェック用HiddenObject
		 */
		getConfirmUnregistFlg : function(obj){
			// チェック用OBJが存在しない場合、登録系画面ではないと判断し、不要と判断
			if(obj.length===0){
				return false;
			}
			if(obj.is(":disabled")){
				return false;
			}

			// チェック用OBJが存在した場合、値をチェック
			var changedIdxVal = obj.val();
			if(changedIdxVal !== undefined && changedIdxVal !== ""){
				return true;
			}
			return false;
		},

		/**
		 * 帳票移動の警告処理
		 * @param func	- OKボタンが押下されたのちに実行される処理
		 * @param msgid	- 警告メッセージID
		 */
		confirmReportUnregist : function(func, msgid){
			// 未登録の警告メッセージが必要な場合
			if($.getConfirmUnregistFlg($($.id.hiddenChangedIdx))){
				if(msgid===undefined) msgid = 'E11025';
				$.showMessage(msgid, undefined, func);
			}else if (typeof func === 'function'){
				func();
			}
		},

		/**
		 * 帳票移動の警告処理
		 * @param func	- OKボタンが押下されたのちに実行される処理
		 * @param msgid	- 警告メッセージID
		 */
		confirmUnregist : function(){
			var rt = true;
			var obj = $($.id.hiddenChangedIdx);
			if(obj.length===0){obj = $(this).parent($.id.hiddenChangedIdx);}
			// 未登録の警告メッセージが必要かどうかチェック
			if($.getConfirmUnregistFlg(obj)){
				rt = confirm($.getMessage('E11239'));
				// 登録系の場合、変更があった場合に確認メッセージ
			}
			return rt;
		},

		/**
		 * 変更
		 * @param msg
		 */
		setChangeIdx:function (index){		// 変更行Index保持
			// 変更行Index保持
			var changedIndex = $($.id.hiddenChangedIdx).val().split(",");
			if($.inArray(index+'',changedIndex)===-1){
				changedIndex.push(index);
				$($.id.hiddenChangedIdx).val(changedIndex.join(",").replace(/^,/,""));
				$($.id.hiddenChangedIdx).change();
			}
		},


		/**
		 * 処理 - 成功
		 * @param {String} json
		 */
		cmnNormal : function(data, func, key, add, gridid){
			gridid = gridid===undefined?$.id.gridholder:'#'+gridid;
			// メッセージ削除
			if($(gridid).hasClass("datagrid-f")){
				$(gridid).datagrid('loaded');
			}
			$.removeMaskMsg();

			var json = JSON.parse(data);
			if(json && json.opts.S_MSG !== undefined){
				if(json.opts.S_MSG.ID && $.messageList[json.opts.S_MSG.ID]){
					$.showMessage(json.opts.S_MSG.ID, add, func);
				}else{
					$.showMessageIn("I", json.opts.S_MSG, undefined, func, undefined, undefined, "I");
				}
			}else if(key){
				$.showMessage(key, add, func);
			}
		},

		/**
		 * 処理エラー判定
		 * @param {String} json
		 */
		cmnError : function(data, key, add, gridid){
			gridid = gridid===undefined?$.id.gridholder:'#'+gridid;
			var isErr = false;
			var json = JSON.parse(data);
			if (json == null || json.length < 1){
				// メッセージ表示
				if(key){
					$.showMessage(key, add);
				}else{
					$.showMessageIn("E", "処理に失敗しました。", undefined, undefined, undefined, undefined, "E");
				}
				isErr = true;
			}else if(json.opts.E_MSG !== undefined){
				if($.isArray(json.opts.E_MSG)){
					if(json.opts.E_MSG.length === 1 && $.messageList[json.opts.E_MSG[0].ID] ){
						$.showMessage(json.opts.E_MSG[0].ID, json.opts.E_MSG[0].PRM);
					}else{
						var msg = "";
						$.each(json.opts.E_MSG, function() {
							msg += this.MSG + "\n";
						});
						$.messager.alert({title: "エラー", icon:"error", msg: msg});
					}
				}else{
					if($.messageList[json.opts.E_MSG.ID]){
						if(json.opts.E_MSG.PRM){
							$.showMessage(json.opts.E_MSG.ID, json.opts.E_MSG.PRM);
						}else{
							$.showMessage(json.opts.E_MSG.ID);
						}
					}else{
						$.messager.alert({title: "エラー", icon:"error", msg: json.opts.E_MSG});
					}
				}
				isErr = true;
			}else if(json.message !== undefined && json.message !== ''){
				$.showMessageIn("E", json.message, undefined, undefined, undefined, undefined, "E");
				isErr = true;
			}
			if(isErr){
				// マスク削除
				$.removeMask();
				if($(gridid).hasClass("datagrid-f")){
					$(gridid).datagrid('loaded');
				}
				$.removeMaskMsg();
				return true;
			}
			return false;
		},


		/**
		 * Excelボタンイベント
		 * @param {Object} e
		 */
		pushExcel : function(e){
			if ($('#'+$.id.btn_excel).linkbutton('options').disabled) return false;
			// レポート番号取得
			var reportno=$($.id.hidden_reportno).val();

			// レポート定義位置
			var reportNumber = $.getReportNumber(reportno);
			if (typeof(reportNumber) !== 'number') { alert("レポート定義が見つかりません。"); return false;}

			// 検索結果の再確認
			if ($.report[reportNumber].getRecord() <= 0){
				alert($.message.ID_MESSAGE_WARNING_EXCEL_OUTPUT);
				return false;
			}
			// 変更内容の確認
//			var rt = $.confirmUnregist2('未登録の内容は出力されませんが、実行してもよろしいでしょうか？');
//			if(!rt){ return false;}

			// Excel出力ボタン無効化
			$.setButtonState('#'+$.id.btn_excel, false, $.id.btn_excel);
			// マスク追加
			$.appendMask();
			$.appendMaskMsg();

			// ログ情報の格納
			$.post(
				$.reg.easy ,
				{
					"page"	: reportno ,
					"obj"	: $.id.btn_excel ,
					"sel"	: new Date().getTime(),
					"userid": $($.id.hidden_userid).val(),
					"user"	: $($.id.hiddenUser).val(),
					"report": $($.id.hiddenReport).val(),
					"json"	: ""
				},
				function(json){}
			);

			// Excel 出力
			if ($.report[reportNumber].excel != undefined) {
				$.report[reportNumber].excel(reportno);
			}else{
				$.outputExcel(reportno);
			}
			return false;
		},

		/**
		 * Excel出力実行
		 * @param {Object} reportno
		 * @param {Object} kbn
		 */
		outputExcel : function(reportno, kbn){
			if(kbn == null) kbn = 0;
			window.parent.frames['blank'].open($.reg.excel+'?report='+reportno+'&kbn='+kbn+'&ts='+(new Date()).getTime(),'_self','width=400, height=300, menubar=no, toolbar=no, scrollbars=yes');
			// Excel出力ボタン有効化
			$.setButtonState('#'+$.id.btn_excel, true, $.id.btn_excel);
			// マスク削除
			$.removeMask();
			$.removeMaskMsg();
			return false;
		},

		/**
		 * Excel出力エラー
		 */
		outputExcelError : function(){
			alert("Excel出力に失敗しました。");
			// Excel出力ボタン有効化
			$.setButtonState('#'+$.id.btn_excel, true, $.id.btn_excel);
			// マスク削除
			$.removeMask();
			$.removeMaskMsg();
		},

		/**
		 * 検索Excelボタンイベント
		 * @param {Object} e
		 */
		pushSearchExcel : function(e){
			if ($(this).linkbutton('options').disabled)	return false;

			// レポート番号取得
			var reportno=$($.id.hidden_reportno).val();

			// レポート定義位置
			var reportNumber = $.getReportNumber(reportno);
			if (typeof(reportNumber) !== 'number') { alert("レポート定義が見つかりません。"); return false;}

			// マスク削除
			$.removeMask();

			// フォーム情報取得
			$.report[reportNumber].getEasyUI();

			if ($.report[reportNumber].validation()) {
				// マスク追加
				$.appendMask();

				// セッションタイムアウト、利用時間外の確認
				var isTimeout = $.checkIsTimeout();
				if (! isTimeout) {
					// 検索条件保持
					$.ajax({
						url: $.reg.easy,
						type: 'POST',
						async: false,
						data: {
							"page"	: reportno,
							"obj"	: $.id.btn_search,
							"sel"	: "json",
							"userid": $($.id.hidden_userid).val(),
							"user"	: $($.id.hiddenUser).val(),
							"report": $($.id.hiddenReport).val(),
							"json"	: JSON.stringify($.report[reportNumber].getJSONString())
						},
						success: function(json){
							// Excel 出力
							if ($.report[reportNumber].srcexcel != undefined) {
								$.report[reportNumber].srcexcel(reportno);
							}else{
								$.outputSearchExcel(reportno);
							}
						}
					});
				}
			}
			return false;
		},

		/**
		 * 検索Excel出力実行
		 * @param {Object} reportno
		 * @param {Object} kbn
		 */
		outputSearchExcel : function(reportno, kbn){
			if(kbn == null) kbn = 0;
			window.parent.frames['blank'].open($.reg.srcexcel+'?report='+reportno+'&kbn='+kbn+'&ts='+(new Date()).getTime(),'_self','width=400, height=300, menubar=no, toolbar=no, scrollbars=yes');
			// マスク削除
			$.removeMask();
			$.removeMaskMsg();
			return false;
		},

		/**
		 * 検索Excel出力エラー
		 */
		outputSearchExcelError : function(){
			$.showMessage('E00011');
			// マスク削除
			$.removeMask();
			$.removeMaskMsg();
			return false;
		},

		/**
		 * 登録(DB更新)ボタンイベント
		 * @param {Object} e
		 */
		pushUpd:function(e){
			if ($(this).linkbutton('options').disabled)	return false;

			// レポート番号取得
			var reportno=$($.id.hidden_reportno).val();
			// レポート定義位置
			var reportNumber = $.getReportNumber(reportno);
			if (typeof(reportNumber) !== 'number') { alert("レポート定義が見つかりません。"); return false;}

			// JS情報取得
			var that = $.report[reportNumber];
			var id = $(this).attr('id');

			// チェック・確認処理
			var rtn = true;
			if($.isFunction(that.updValidation)) { rtn = that.updValidation(id);}
			// 変更情報チェック
			if(rtn && !$.getConfirmUnregistFlg($($.id.hiddenChangedIdx))){
				$.showMessage('E20582');
				return false;
			}

			if(rtn){
				var func_ok = function(r){
					// セッションタイムアウト、利用時間外の確認
					var isTimeout = $.checkIsTimeout();
					if (! isTimeout) {
						// ログの書き込み
						$.ajax({
							url: $.reg.easy,
							type: 'POST',
							async: false,
							data: {
								"page"	: reportno ,
								"obj"	: id,
								"sel"	: new Date().getTime(),
								"userid": $($.id.hidden_userid).val(),
								"user"	: $($.id.hiddenUser).val(),
								"report": $($.id.hiddenReport).val(),
								"json"	: ""
							},
							success: function(json){
								that.updSuccess(id);
							}
						});
					}
					return true;
				};
				if($.isFunction(that.updConfirm)) {
					that.updConfirm(func_ok);
				}else{
					$.showMessage("W00001", undefined, func_ok);
				}
			}
		},

		/**
		 * 登録(DB更新)処理 - 成功
		 * @param {String} json
		 */
		updNormal : function(data, func, gridid){
			gridid = gridid===undefined?$.id.gridholder:'#'+gridid;
			// マスク削除
			$.removeMask();
			// メッセージ削除
			if($(gridid).hasClass("datagrid-f")){
				$(gridid).datagrid('loaded');
			}else{
				$.removeMaskMsg();
			}
			var json = JSON.parse(data);
			if(json && json.opts.S_MSG !== undefined){
				if(json.opts.S_MSG.ID && $.messageList[json.opts.S_MSG.ID]){
					var param = json.opts.S_MSG.PRM ? json.opts.S_MSG.PRM : undefined;
					$.showMessage(json.opts.S_MSG.ID, param, func);
				}else{
					$.showMessageIn("I", json.opts.S_MSG, undefined, func, undefined, undefined, "I");
				}
			}else{
				$.showMessage("I00001", undefined, func);
			}
		},

		/**
		 * 登録(DB更新)処理エラー判定
		 * @param {String} json
		 */
		updError : function(id, data, gridid){
			gridid = gridid===undefined?$.id.gridholder:'#'+gridid;
			var isErr = false;
			var json = JSON.parse(data);
			if (json == null || json.length < 1 || json.opts === null || json.opts === undefined || (json.opts.E_MSG === undefined && json.opts.S_MSG === undefined)){
				// メッセージ表示
				$.showMessage('E00005');
				isErr = true;
			}else if(json.opts.E_MSG !== undefined){
				var msgObj = null;
				if($.isArray(json.opts.E_MSG)){
					if(json.opts.E_MSG.length === 1 && $.messageList[json.opts.E_MSG[0].ID] ){
						$.showMessage(json.opts.E_MSG[0].ID, json.opts.E_MSG[0].PRM);
					}else{
						var msg = "";
						$.each(json.opts.E_MSG, function() {
							msg += this.MSG + "\n";
						});
						$.messager.alert({title: "エラー", icon:"error", width:500, msg: msg});
					}
				}else{
					if($.messageList[json.opts.E_MSG.ID]){
						if(json.opts.E_MSG.PRM){
							$.showMessage(json.opts.E_MSG.ID, json.opts.E_MSG.PRM);
						}else{
							$.showMessage(json.opts.E_MSG.ID);
						}
					}else{
						$.messager.alert({title: "エラー", icon:"error", width:500, msg: json.opts.E_MSG});
					}
				}
				isErr = true;
			}
			if(isErr){
				// マスク削除
				$.removeMask();
				if($(gridid).hasClass("datagrid-f")){
					$(gridid).datagrid('loaded');
				}else{
					$.removeMaskMsg();
				}
				return true;
			}
			return false;
		},

		/**
		 * メッセージ
		 * @param data メッセージ内容
		 * @param func メッセージ表示後処理
		 * @param gridid マスククリア対象のdatagrid
		 */
		ExMessage : function(data, func, gridid){
			gridid = gridid===undefined?$.id.gridholder:'#'+gridid;
			// マスク削除
			$.removeMask();
			// メッセージ削除
			if($(gridid).hasClass("datagrid-f")){
				$(gridid).datagrid('loaded');
			}else{
				$.removeMaskMsg();
			}
			var json = JSON.parse(data);
			if(json && json.opts.S_MSG !== undefined){	// 成功時
				if(json.opts.S_MSG.ID && $.messageList[json.opts.S_MSG.ID]){
					var param = json.opts.S_MSG.PRM ? json.opts.S_MSG.PRM : undefined;
					$.showMessage(json.opts.S_MSG.ID, param, func);
				}else{
					$.showMessageIn("I", json.opts.S_MSG, undefined, func, undefined, undefined, "I");
				}
			}else if(json.opts.E_MSG !== undefined){		// 失敗時
				var msgObj = null;
				if($.isArray(json.opts.E_MSG)){
					if(json.opts.E_MSG.length === 1 && $.messageList[json.opts.E_MSG[0].ID] ){
						$.showMessage(json.opts.E_MSG[0].ID, json.opts.E_MSG[0].PRM, func);
					}else{
						var msg = "";
						$.each(json.opts.E_MSG, function() {
							msg += this.MSG + "\n";
						});
						$.messager.alert({title: "エラー", icon:"error", width:500, msg: msg});
					}
				}else{
					if($.messageList[json.opts.E_MSG.ID]){
						if(json.opts.E_MSG.PRM){
							$.showMessage(json.opts.E_MSG.ID, json.opts.E_MSG.PRM, func);
						}else{
							$.showMessage(json.opts.E_MSG.ID, undefined, func);
						}
					}else{
						$.messager.alert({title: "エラー", icon:"error", width:500, msg: json.opts.E_MSG});
					}
				}
			}else{
				$.showMessage("I00001", undefined, func);
			}
		},

		/**
		 * 削除ボタンイベント
		 * @param {Object} e
		 */
		pushDel:function(e){
			if ($(this).linkbutton('options').disabled)	return false;

			// レポート番号取得
			var reportno=$($.id.hidden_reportno).val();
			// レポート定義位置
			var reportNumber = $.getReportNumber(reportno);
			if (typeof(reportNumber) !== 'number') { alert("レポート定義が見つかりません。"); return false;}

			// JS情報取得
			var that = $.report[reportNumber];
			var id = $(this).attr('id');

			// チェック・確認処理
			var rtn = false;

			if($.isFunction(that.delValidation)) { rtn = that.delValidation(id);}
			if(rtn){
				var func_ok = function(r){
					// セッションタイムアウト、利用時間外の確認
					var isTimeout = $.checkIsTimeout();
					if (! isTimeout) {
						// ログの書き込み
						$.ajax({
							url: $.reg.easy,
							type: 'POST',
							async: false,
							data: {
								"page"	: reportno ,
								"obj"	: id,
								"sel"	: new Date().getTime(),
								"userid": $($.id.hidden_userid).val(),
								"user"	: $($.id.hiddenUser).val(),
								"report": $($.id.hiddenReport).val(),
								"json"	: ""
							},
							success: function(json){
								that.delSuccess(id);
							}
						});
					}
					return true;
				};
				if($.isFunction(that.delConfirm)) {
					that.delConfirm(func_ok);
				}else{
					$.showMessage("W00002", undefined, func_ok);
				}
			}
		},

		/**
		 * 登録(DB更新)処理 - 成功
		 * @param {String} json
		 */
		delNormal : function(data, func, gridid){
			gridid = gridid===undefined?$.id.gridholder:'#'+gridid;
			// マスク削除
			$.removeMask();
			// メッセージ削除
			if($(gridid).hasClass("datagrid-f")){
				$(gridid).datagrid('loaded');
			}else{
				$.removeMaskMsg();
			}
			var json = JSON.parse(data);
			if(json && json.opts.S_MSG !== undefined){
				if(json.opts.S_MSG.ID && $.messageList[json.opts.S_MSG.ID]){
					$.showMessage(json.opts.S_MSG.ID, undefined, func);
				}else{
					$.showMessageIn("I", json.opts.S_MSG, undefined, func, undefined, undefined, "I");
				}
			}else{
				$.showMessage("I00003", undefined, func);
			}
		},

		/**
		 * 削除(DB更新)処理エラー判定
		 * @param {String} json
		 */
		delError : function(id, data, gridid){
			gridid = gridid===undefined?$.id.gridholder:'#'+gridid;
			var isErr = false;
			var json = JSON.parse(data);
			if (json == null || json.length < 1 || json.opts === null || json.opts === undefined || (json.opts.E_MSG === undefined && json.opts.S_MSG === undefined)){
				// メッセージ表示
				$.showMessage('E00005');
				isErr = true;
			}else if(json.opts.E_MSG !== undefined){
				var msgObj = null;
				if($.isArray(json.opts.E_MSG)){
					if(json.opts.E_MSG.length === 1 && $.messageList[json.opts.E_MSG[0].ID] ){
						$.showMessage(json.opts.E_MSG[0].ID, json.opts.E_MSG[0].PRM);
					}else{
						var msg = "";
						$.each(json.opts.E_MSG, function() {
							msg += this.MSG + "\n";
						});
						$.messager.alert({title: "エラー", icon:"error", msg: msg});
					}
				}else{
					if($.messageList[json.opts.E_MSG.ID]){
						if(json.opts.E_MSG.PRM){
							$.showMessage(json.opts.E_MSG.ID, json.opts.E_MSG.PRM);
						}else{
							$.showMessage(json.opts.E_MSG.ID);
						}
					}else{
						$.messager.alert({title: "エラー", icon:"error", msg: json.opts.E_MSG});
					}
				}
				isErr = true;
			}
			if(isErr){
				// マスク削除
				$.removeMask();
				if($(gridid).hasClass("datagrid-f")){
					$(gridid).datagrid('loaded');
				}else{
					$.removeMaskMsg();
				}
				return true;
			}
			return false;
		},

		/**
		 * 更新ボタンイベント
		 * @param {Object} e
		 */
		pushReload:function(e){

			// レポート番号取得
			var reportno=$($.id.hidden_reportno).val();

			// レポート定義位置
			var reportNumber = $.getReportNumber(reportno);
			if (typeof(reportNumber) !== 'number') { alert("レポート定義が見つかりません。"); return false;}

			// ボタン押下処理
			$.report[reportNumber].pushReload(e.id);

		},
		/**
		 * 追加ボタンイベント
		 * @param {Object} e
		 */
		pushAdd:function(e){

			// レポート番号取得
			var reportno=$($.id.hidden_reportno).val();

			// レポート定義位置
			var reportNumber = $.getReportNumber(reportno);
			if (typeof(reportNumber) !== 'number') { alert("レポート定義が見つかりません。"); return false;}

			// ボタン押下処理
			$.report[reportNumber].pushAdd(e.id);

		},
		/**
		 * 削除ボタンイベント
		 * @param {Object} e
		 */
		pushDelete:function(e){

			// レポート番号取得
			var reportno=$($.id.hidden_reportno).val();

			// レポート定義位置
			var reportNumber = $.getReportNumber(reportno);
			if (typeof(reportNumber) !== 'number') { alert("レポート定義が見つかりません。"); return false;}

//			// メッセージ表示
//			alert('削除の確定は保存ボタンを押してください。\r\n \r\n戻るボタンで取消が出来ます。\r\n');
			// ボタン押下処理
			$.report[reportNumber].pushDelete(e.id);

		},
		/**
		 * 保存ボタンイベント
		 * @param {Object} e
		 */
		pushEntry:function(e){

			// レポート番号取得
			var reportno=$($.id.hidden_reportno).val();

			// レポート定義位置
			var reportNumber = $.getReportNumber(reportno);
			if (typeof(reportNumber) !== 'number') { alert("レポート定義が見つかりません。"); return false;}

			// ボタン押下処理
			$.report[reportNumber].pushEntry(e.id);

		},
		/**
		 * 戻すボタンイベント
		 * @param {Object} e
		 */
		pushUndo:function(e){

			// レポート番号取得
			var reportno=$($.id.hidden_reportno).val();

			// レポート定義位置
			var reportNumber = $.getReportNumber(reportno);
			if (typeof(reportNumber) !== 'number') { alert("レポート定義が見つかりません。"); return false;}

			// ボタン押下処理
			$.report[reportNumber].pushUndo(e.id);

		},


		/**
		 * 定義保存：適用ボタンイベント
		 * @param {Object} e
		 */
		pushViewShiori:function(e){

			if ($(this).linkbutton('options').disabled)	return false;

			// レポート番号取得
			var reportno=$($.id.hidden_reportno).val();

			// レポート定義位置
			var reportNumber = $.getReportNumber(reportno);
			if (typeof(reportNumber) !== 'number') { alert("レポート定義が見つかりません。"); return false;}

			// ボタン押下処理
			$.report[reportNumber].pushViewShiori(e.id);

		},
		/**
		 * 定義保存：保存ボタンイベント
		 * @param {Object} e
		 */
		pushEntryShiori:function(e){

			if ($(this).linkbutton('options').disabled)	return false;

			// レポート番号取得
			var reportno=$($.id.hidden_reportno).val();

			// レポート定義位置
			var reportNumber = $.getReportNumber(reportno);
			if (typeof(reportNumber) !== 'number') { alert("レポート定義が見つかりません。"); return false;}

			// フォーム情報取得
			$.report[reportNumber].getEasyUI();

			// ボタン押下処理
			$.report[reportNumber].pushEntryShiori(e.id);

		},
		/**
		 * 定義保存：削除ボタンイベント
		 * @param {Object} e
		 */
		pushDeleteShiori:function(e){

			// レポート番号取得
			var reportno=$($.id.hidden_reportno).val();

			// レポート定義位置
			var reportNumber = $.getReportNumber(reportno);
			if (typeof(reportNumber) !== 'number') { alert("レポート定義が見つかりません。"); return false;}

			// ボタン押下処理
			$.report[reportNumber].pushDeleteShiori(e.id);

		},

		/**
		 * 条件リセット：戻るボタンイベント
		 * @param {Object} e
		 */
		pushReset:function(e){

			// レポート番号取得
			var reportno=$($.id.hidden_reportno).val();

			// レポート定義位置
			var reportNumber = $.getReportNumber(reportno);
			if (typeof(reportNumber) !== 'number') { alert("レポート定義が見つかりません。"); return false;}

			// 条件エリア表示
			$.tryShowToolbar($.id.toolbar, $.report[reportNumber]);

			// 条件初期値セット処理
			$.report[reportNumber].initCondition(e.id);
		},

		/**
		 * 帳票移動ボタンイベント
		 * @param {Object} e
		 */
		pushChangeReport:function(e){
			if ($(this).linkbutton('options').disabled)	return false;

			// レポート番号取得
			var reportno=$($.id.hidden_reportno).val();
			// レポート定義位置
			var reportNumber = $.getReportNumber(reportno);
			if (typeof(reportNumber) !== 'number') { alert("レポート定義が見つかりません。"); return false;}

			// JS情報取得
			var that = $.report[reportNumber];
			var id = $(this).attr('id');

			// フォーム情報取得
//			$.report[reportNumber].getEasyUI();

			// セッションタイムアウト、利用時間外の確認
			var isTimeout = $.checkIsTimeout();
			if (! isTimeout) {
				// ログの書き込み
				$.ajax({
					url: $.reg.easy,
					type: 'POST',
					async: false,
					data: {
						"page"	: reportno ,
						"obj"	: id,
						"sel"	: new Date().getTime(),
						"userid": $($.id.hidden_userid).val(),
						"user"	: $($.id.hiddenUser).val(),
						"report": $($.id.hiddenReport).val(),
						"json"	: ""
					},
					success: function(json){
						that.changeReport(reportno, id);
					}
				});
				return true;
			}else{
				return false;
			}
		},

		/**
		 * ページクリアボタンイベント
		 * @param {Object} e
		 */
		pushClear:function(e){
			if ($(this).linkbutton('options').disabled)	return false;

			// レポート番号取得
			var reportno=$($.id.hidden_reportno).val();
			// レポート定義位置
			var reportNumber = $.getReportNumber(reportno);
			if (typeof(reportNumber) !== 'number') { alert("レポート定義が見つかりません。"); return false;}

			// JS情報取得
			var that = $.report[reportNumber];
			var id = $(this).attr('id');
			var func_ok = function(r){
				// セッションタイムアウト、利用時間外の確認
				var isTimeout = $.checkIsTimeout();
				if (! isTimeout) {
					// 検索条件保持
					$.ajax({
						url: $.reg.easy,
						type: 'POST',
						async: false,
						data: {
							"page"	: reportno,
							"obj"	: $.id.btn_search,
							"sel"	: "json",
							"userid": $($.id.hidden_userid).val(),
							"user"	: $($.id.hiddenUser).val(),
							"report": $($.id.hiddenReport).val(),
							"json"	: JSON.stringify($.report[reportNumber].getJSONString())
						},
						success: function(json){
							// 検索実行
							$.report[reportNumber].success(reportno, 0, $.id.btn_search);
						}
					});
				}
				return true;
			};

			// 変更情報チェック
			if($.getConfirmUnregistFlg($($.id.hiddenChangedIdx))){
				$.showMessage("W30002", undefined, func_ok);
			}

		},

		/**
		 * Combobox、Combogridにフォーカスを移動時、全選択状態にする
		 * @param {Object} id
		 */
		setFocusEvent : function(id){
			var combotext = $('#'+id).combo('textbox');
			combotext.focus(function(){
				$(this).select();
			});
			combotext.mouseup(function(e){
				e.preventDefault();
			});
		},

		/**
		 * サブ画面にてCombobox、Combogridにフォーカスを移動時、全選択状態にする
		 * @param {Object} that
		 */
		ctrlFocusSubWin : function(that){
			if(that.suffix && that.suffix != ''){
				// suffixに設定値がある場合(サブ画面の場合)
				$('#'+that.focusRootId).find('.textbox-text.validatebox-text').on('focus', function() {
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
			}
		},

		/**
		 * ComboBox共通作成用
		 * @param {Object} jsonHidden
		 * @param {Object} id
		 */
		setCombobox : function(jsonHidden, id, that){
			$('#'+id).combobox({
				required:false,
				editable:false,
				panelHeight:'auto',
				onLoadSuccess:function(data){
					// 初期化
					var num = 0;
					var val = $.getJSONValue(jsonHidden, id);
					for (var i=0; i<data.length; i++){
						if (data[i].value == val){
							num = i;
							break;
						}
					}
					if (num > 0){
						$(this).combobox('setValue',val);
					}
					if(that){
						if ($.inArray(id, that.initedObject) < 0){
							that.initedObject.push(id);
						}
						// 初期表示検索処理
						$.initialSearch(that);
					}
				}
//				,onSelect:function(record){
//					// 検索ボタン有効化
//					$.setButtonState('#'+$.id.btn_search, true, id);
//				}
			});
		},

		/**
		 * NumberSpinner共通作成用
		 * @param {Object} jsonHidden
		 * @param {Object} id
		 * @param {Object} min
		 * @param {Object} max
		 */
		setNumberspinner : function(jsonHidden, id, min, max, that){
			$('#'+id).numberspinner({
				required:true,
				editable:true,
				min: min,
				max: max
//				,onSpinUp:function(){
//					// 検索ボタン有効化
//					$.setButtonState('#'+$.id.btn_search, true, id);
//				}
//				,onSpinDown:function(){
//					// 検索ボタン有効化
//					$.setButtonState('#'+$.id.btn_search, true, id);
//				}
//				,onChange:function(newValue,oldValue){
//					// 検索ボタン有効化
//					$.setButtonState('#'+$.id.btn_search, true, id);
//				}
			});
			// 初期化
			var json = $.getJSONObject(jsonHidden, id);
			if (json){
				$('#'+id).numberspinner('setValue',json.value);
			}
			if(that){
				if ($.inArray(id, that.initedObject) < 0){
					that.initedObject.push(id);
				}
				// 初期表示検索処理
				$.initialSearch(that);
			}
		},

		/**
		 * データテーブル（ヘッダー/フッターなど）値セット
		 * @param {Object} selectors	- 設定エリアのセレクター
		 * @param {Object} prefix		- 値設定SPANのID
		 * @param {Object} data			- データ
		 * @param {Object} len			- データのサイズ
		 */
		setDataTable : function(selectors, prefix, data, len){
			// 値セット
			var target = $(selectors + ' *[id^="' + prefix + '"]');
			target.text("");
			if(!data) return false;
			if(Object.keys(data).length===0) return false;
			for(var i=1; i <= len; i++){
				var obj = target.filter('#'+prefix+i);
				if(obj.length === 0){ continue; }
				if(obj.is("span") && prefix+i in data){
					var format = obj.attr('format');
					if(format!==undefined){
						if(format==="date"){
							obj.text($.getFormatDt(data[prefix+i]));
						}else{
							obj.text($.getFormat(data[prefix+i], format));
						}
					}else{
						obj.text(data[prefix+i]);
					}
				}
			}
		},
		/**
		 * 閏年チェック
		 * @param y
		 * @returns {Boolean}
		 */
		isLeapYear : function isLeapYear(y) {
			return !(y % 4) && (y % 100) || !(y % 400) ? true : false;
		},
		/**
		 * 数値空白チェック
		 * @param y
		 * @returns {Boolean}
		 */
		isEmptyVal: function (val, zeroEmpty){
			if(val === undefined || val === null){
				return true;
			}
			if(val.length === 0){
				return true;
			}
			if(zeroEmpty === true){
				return isFinite(val) && val*1 === 0;
			}
			return false;
		},

		/**
		 * 期間指定チェック
		 * @param kikanFrom
		 * @param kikanTo
		 * @param kbnKikan
		 * @returns {Boolean}
		 */
		checkKikan : function(kikanFrom, kikanTo, kbnKikan){
			if (kikanFrom > kikanTo){
				alert('検索期間を正しく指定してください。');
				return false;
			}
			if (kbnKikan === $.id.valueKikan_FYear){
				// 年
				var limit = 7;
				var dt1 = kikanFrom.substring(0,4) * 1;
				var dt2 = kikanTo.substring(0,4) * 1;
				if (dt2 - dt1 > limit - 1) {
					alert('検索期間は' + limit + '年以内で指定してください。');
					return false;
				}
			} else if (kbnKikan === $.id.valueKikan_Month){
				// 月
				var limit =26;	// 2016/05/30 要望により変更 13 → 26
				var dt1 = kikanFrom.substring(0,4) * 12 + kikanFrom.substring(4,6) * 1;
				var dt2 = kikanTo.substring(0,4) * 12 + kikanTo.substring(4,6) * 1;
				if (dt2 - dt1 > limit - 1) {
					alert('検索期間は' + limit + 'ヶ月以内で指定してください。');
					return false;
				}
			} else if (kbnKikan === $.id.valueKikan_Week){
				// 週 (日数で判定する)
				var limit = 55;	// 2016/05/30 要望により変更 30 → 55週
				var dt1 = new Date(kikanFrom.substring(0,4), kikanFrom.substring(4,6) - 1, kikanFrom.substring(6,8));
				var dt2 = new Date(kikanTo.substring(0,4), kikanTo.substring(4,6) - 1, kikanTo.substring(6,8));
				var diff = (dt2.getTime() - dt1.getTime()) / (24 * 3600 * 1000);
				if (diff > limit * 7 - 1) {
					alert('検索期間は' + limit + '週間以内で指定してください。');
					return false;
				}
			} else {
				// 日
				var limit = 62;	// 2016/05/30 要望により変更 31 → 42 → 62
				var dt1 = new Date(kikanFrom.substring(0,4), kikanFrom.substring(4,6) - 1, kikanFrom.substring(6,8));
				var dt2 = new Date(kikanTo.substring(0,4), kikanTo.substring(4,6) - 1, kikanTo.substring(6,8));
				var diff = (dt2.getTime() - dt1.getTime()) / (24 * 3600 * 1000);
				if (diff > limit - 1) {
					alert('検索期間は' + limit + '日以内で指定してください。');
					return false;
				}
			}
			return true;
		},

		/**
		 * 店舗指定チェック
		 * @param Tenpo_ck
		 * @returns {Boolean}
		 */
		checkTenpo : function(Tenpo_ck){
			var arryTenpo = String(Tenpo_ck).split(",");
			var num = 0;
			for (var i = 0; i < arryTenpo.length; ++i ) {
				if(arryTenpo[i] < 0){
					num = num +1;
				}
			}
			if (num >= 1 && arryTenpo.length > 1 ){
				alert('全店,既存店等指定時は複数指定できません。');
				return false;
			}
			return true;
		},

		/**
		 * 店舗指定チェック
		 * @param Tenpo_ck
		 * @returns {Boolean}
		 */
		checkTenpoRfm : function(Tenpo_ck){
			var limit = 10;
			var arryTenpo = String(Tenpo_ck).split(",");
			if ( arryTenpo.length > limit ){
				alert('店舗は' + limit + '店舗以内で指定してください。');
				return false;
			}
			return true;
		},

		/**
		 * セルの縦マージ
		 * @param id
		 * @param data
		 * @param column
		 */
		mergeVerticallCells : function(id, data, column){
			var $id = $(id);
			// 開始位置
			var data = $id.datagrid('getRows');
			var startIndex = $id.datagrid('getRowIndex', data[0]);
			// セルのマージ
			var befData = "";
			var cnt = 0;
			for(i = 0, len = data.length; i <= len; i++){
				if(i!=0){ befData = $.trim(data[i-1][column]);}
				if(i==len || (i!=0 && befData != $.trim(data[i][column]))){
					$id.datagrid('mergeCells',{index:startIndex+i-cnt,field:column,rowspan:cnt});
					cnt = 0;
				}
				cnt++;
			}
		},
		/**
		 * セルの縦マージ（縦軸１：商品、縦軸2：なし、横軸：表示項目）
		 * @param id
		 * @param data
		 * @param column
		 */
		mergeVerticallCells2Item : function(id, data, column, cehckColumn){
			var $id = $(id);
			try{
				var d=data.rows[0][cehckColumn];
			}catch(e){
				return;
			}
			// 開始位置
			var data = $id.datagrid('getRows');
			var startIndex = $id.datagrid('getRowIndex', data[0]);
			// セルのマージ
			var befData = "";
			var nowData = "";
			var cnt = 1;
			for(var i = 0, len = data.length; i <= len; i++){
				if(i!=0){// 前行値
					befData = $.trim(data[i-1][column]);
				}
				if (i<len){	// 現行値＆商品名列の「計」確認
					nowData = $.trim(data[i][column]);
				}
				// ページの最終行　又は　前行値の異なる
				if( (nowData!=='' && befData != nowData) || i===len){
					$id.datagrid('mergeCells',{index:startIndex+i-cnt,field:column,rowspan:cnt});
					cnt = 0;
				}
				cnt++;
			}
		},
		/** 店舗グループの選択値によるラベル切替
		 * val 店舗グループの選択値
		 */
		getLabelTenpoG: function(val){
			switch (val) {
			case $.id.valueTenpoG_Han:	// 販売統括部選択
				return $.id.labelHonbu;
			case $.id.valueTenpoG_Sei:	// 青果市場選択
				return $.id.labelIchiba;
			default:	// その他選択時
				return "　　　　";
			}
		},
		/**
		 * row の最大カラム番号取得
		 */
		getMaxColumnNo: function(row){
			var columnNo = -1;
			var dummy="";
			for (var i=1;i<1048;i++){
				try {
					if (typeof row['F'+i] === "undefined") {
						break;
					}
					columnNo = i;
				} catch(e) {
					break;
				}
			}
			return columnNo;
		},
		/**
		 * 列最大チェック
		 * @param ids 項目のID(※上段→下段となるように指定すること) もしくは 数値
		 * @param msg
		 * @returns {Boolean}
		 */
		checkColumnSize : function(that, ids){
			var limit = 1000;
			var size = 1;
			// 可変列算出
			for (var i = 0; i < ids.length; ++i ) {
				var num = 1;
				if(isFinite(ids[i])){									// 列数直接指定
					num = ids[i]*1;
				}else if($('#'+ids[i]).combogrid('options').multiple){	// 複数選択可
					num = $('#'+ids[i]).combogrid('getValues').length;
					// 最上段は総合計考慮
					if(i==0) num = num + 1;
				}else if($('#'+ids[i]).combogrid('getValue')==""){		// 複数選択不可で「全て」選択
					num = $('#'+ids[i]).combogrid('grid').datagrid('getRows').length;
					// 最上段は総合計考慮
					if(i==0) num = num - 1;
				}else if(i==0){											// 複数選択不可で「全て」以外選択、かつ最上段の場合
					num = 2;
				}
				size = size * num;
			}
			// レポートごとの特殊計算
			if(that.name=="livins_rep09"){	// 加盟店別商品別在庫表
				var row = $('#'+ids[1]).combogrid('getValues');
				if($.inArray("1",row)!= -1) size = size -1;
				if($.inArray("2",row)!= -1) size = size -1;
				if($.inArray("3",row)!= -1) size = size -1;
				if($.inArray("4",row)!= -1) size = size -1;
			}
			// 固定列情報取得
			var fColumns = $($.id.gridholder).datagrid('options').frozenColumns;
			if(fColumns){
				size = size + fColumns[0].length;
			}
			if(size > limit){
				// Load処理回避
				$.tryChangeURL(null);
				$($.id.gridholder).datagrid({data: []});
				alert('列が最大表示(' + limit + '列)を超えています。\n検索条件を指定していただき、表示件数の絞込みを行ってください。');
				return false;
			}
			return true;
		},

		/**
		 * 行最大チェック
		 * @param total
		 * @returns {Boolean}
		 */
		checkRowSize : function(total){
			var limit = 60000;
			if(total > limit){
				// Load処理回避
				$.tryChangeURL(null);
				$($.id.gridholder).datagrid({data: []});
				alert('検索結果が最大表示(' + limit + '件)を超えています。\n検索条件を指定していただき、表示件数の絞込みを行ってください。');
				return false;
			}
			return true;
		},

		/**
		 * combogrid 入力値チェック
		 * @param array
		 * @param id
		 * @param msg
		 * @returns {Boolean}
		 */
		checkCombogrid : function(array, id, msg){
			var row = $('#'+id).combogrid('grid').datagrid('getSelected');
			var val = $.getJSONObject(array, id).value;
			var txt = $.getJSONObject(array, id).text;

			if (val == '' && txt == ''){
				return true;

			} else if (row != null && row.VALUE == val && row.TEXT == txt){
				return true;

			} else {
				alert(msg + 'を正しく指定してください。');
				return false;
			}
		},

		/**
		 * 文字バイト数チェック
		 * @param str
		 * @param byte
		 * @returns {Boolean}
		 */
		checkByte : function(str, byte){
			var num = 0;
			for (var i = 0; i < str.length; i++){
				var c = str.charCodeAt(i);
				if ( (c >= 0x0 && c < 0x81) || (c == 0xf8f0) || (c >= 0xff61 && c < 0xffa0) || (c >= 0xf8f1 && c < 0xf8f4)){
					num += 1;
				} else {
					num += 2;
				}

				if (num > byte){
					return false;
				}
			}

			return true;
		},

		/**
		 * 半角数字、桁数チェック
		 * @param str
		 * @param len
		 * @param fixed
		 * @returns {Boolean}
		 */
		checkNumericLen : function(str, len, fixed){
			if (str.length > len){
				return false;
			} else if (fixed == true && str.length != len){
				return false;
			}
			if (str.match(/[^0-9]/)){
				return false;
			}
			return true;
		},

		/**
		 * 日付チェック
		 * @param str
		 * @returns {Boolean}
		 */
		checkDate : function(str){
			// 正規表現による書式チェック
			if(!str.match(/^\d{8}$/)){
				return false;
			}
			var vYear = str.substr(0, 4) - 0;
			var vMonth = str.substr(4, 2) - 1; // Javascriptは、0-11で表現
			var vDay = str.substr(6, 2) - 0;
			// 月、日の妥当性チェック
			if(vMonth < 0 || vMonth > 11 || vDay < 1 || vDay > 31){
				return false;
			}
			var vDt = new Date(vYear, vMonth, vDay);
			if(isNaN(vDt)){
				return false;
			}
			if(vDt.getFullYear() != vYear || vDt.getMonth() != vMonth || vDt.getDate() != vDay){
				return false;
			}
			return true;
		},
		/**
		 * 選択変換
		 */
		convertComboGrid: function(json, id){
			var g = $('#'+id).combogrid('grid');
			var value = $.getJSONObject(json, id).value;
			var text  = $.getJSONObject(json, id).value;

			// 分類（全選択の場合や未選択を「すべて」）
			if (g.datagrid('getSelections').length === g.datagrid('getRows').length || value.length===0){
				value = ['-1'];
				text  = 'すべて';
				// 情報更新
				$.setJSONObject(json,id,value,text)
			}
		},
		convertComboBox: function(json, id){
			var _$id = $('#'+id);
			var value = $.getJSONObject(json, id).value;
			var text  = $.getJSONObject(json, id).value;

			// 分類（全選択の場合や未選択を「すべて」）
			if (_$id.combobox('getData').length === _$id.combobox('getValues').length || value.length===0){
				value = ['-1'];
				text  = 'すべて';
				// 情報更新
				$.setJSONObject(json,id,value,text)
			}
		},
		/**
		 * 大分類（大分類が全選択の場合、すべて）変換
		 */
		convertBumonDaibun: function(value){
			// 大分類（大分類が全選択の場合、すべて）
			var g = $('#'+$.id.SelDaiBun).combogrid('grid');
			if (g.datagrid('getSelections').length === g.datagrid('getRows').length){
				value = ['-1'];
			}
			return value;
		},
		convertBumonDaibunCombobox: function(value){
			// 大分類（大分類が全選択の場合、すべて）
			var _$id = $('#'+$.id.SelDaiBun);
			if (_$id.combobox('getData').length === _$id.combobox('getValues').length || value.length===0){
				value = ['-1'];
			}
			return value;
		},
		/**
		 * 警告（上限等）
		 */
		showWarningMessage: function(data){
			if (data && data.total > 65000){
				alert('検索結果件数が65,000件を超えました。\n検索条件の絞込みを行ってください。');
			} else if (data && typeof data.message !== 'undefined' && data.message !== ''){
				alert(data.message);
			}
		},
		/**
		 * 商品コードの全角→半角変換
		 */
		convertSyohinCode: function(values){
			if (values.length===1){
				if (values[0]===""){
				 	values=[];
				}else{
					// 全角スペース→半角スペース置換
					values[0] = values[0].replace(/　/g," ");
					// 全角数値→半角数値置換
					values[0] = values[0].replace(/[０-９]/g, function(s) {
						return String.fromCharCode(s.charCodeAt(0) - 65248);
					});

					values = values[0].split(" ");
					for(var i=0;i<values.length;i++){
						values[i] = ("00000000"+values[i]).slice(-8);
					}
				}
			}
			return values;
		},

		/**
		 * 統括部門（属する部門）の選択
		 */
		setToukatsuBumon: function(e){
			var _$id = $(e.data.target);
			var grid = _$id.combogrid('grid');
			var data = grid.datagrid("getData");
			var select = [];
			for (var i=0; i<data.rows.length; i++){
				if (data.rows[i].TKCDS!=='XX'){
					select.push(data.rows[i].VALUE);
				}
			}
			_$id.combogrid('setValues',select);
		},

		/**
		 * 統括部門（属する部門）の選択
		 */
		setToukatsuBumonCombobox: function(e){
			var _$id = $(e.data.target);
			var data = _$id.combobox("getData");
			var select = [];
			for (var i=0; i<data.length; i++){
				if (data[i].TKCDS!=='XX'){
					select.push(data[i].VALUE);
				}
			}
			_$id.combobox('setValues',select);
		},

		/**
		 * Comboboxの全解除
		 */
		unselectAllCombobox: function(e){
			var _$id = $(e.data.target);
			_$id.combobox('clear');
		},
		setToolbarHeight: function(){
			// toolbar の高さ調整
			$($.id.toolbar).height($($.id.toolbar).get(0).scrollHeight>$($.id.toolbar).get(0).offsetHeight?$($.id.toolbar).get(0).scrollHeight:$($.id.toolbar).get(0).offsetHeight);
		},

		/**
		 * Web商談
		 * 全選択イベントボタン
		 * @param prefix
		 */
		pushAllSelect:function(prefix){
			if(prefix === undefined) prefix = '';
			var dg = $($.id.gridholder + prefix);
			var rows = dg.datagrid('getData').originalRows;
			if(rows){
				var index = [];
				for(var i=0; i<rows.length; i++){
					index.push(i);
				}
				$($.id.hiddenChangedIdx + prefix).val(index.join(',').replace(/^,/,''));

				var inputs = dg.datagrid('getPanel').find('.datagrid-row :input[id^="ck_"]').filter(':enabled');
				inputs.prop('checked', true);
			}
		},

		/**
		 * Web商談
		 * 全選択解除イベントボタン
		 * @param prefix
		 */
		pushAllUnSelect:function(prefix){
			if(prefix === undefined) prefix = '';
			var dg = $($.id.gridholder + prefix);
			$($.id.hiddenChangedIdx + prefix).val('');

			var inputs = dg.datagrid('getPanel').find('.datagrid-row :input[id^="ck_"]');
			inputs.prop('checked', false);
		},

		/**
		 * Web商談
		 * DateBox共通作成用
		 * @param {Object} that
		 * @param {Object} id
		 * @param {Object} required
		 * @param {Object} validType
		 * @param {Object} isSearch
		 * @param {Object} setHiddenValue
		 */
		setDateBox: function(that, id, required, validType, isSearch, setHiddenValue){
			var preVal = '';
			var idx = -1;
			if(setHiddenValue === undefined) setHiddenValue = false;
			if(setHiddenValue) $('#'+id).val($.getJSONValue(that.jsonHidden, id));

			var buttons = $.extend([], $.fn.datebox.defaults.buttons);
			if(! required){
				buttons.splice(1, 0, {
					text: '消去',
					handler: function(target){
						$(target).datebox('clear');
					}
				});
			}

			$('#'+id).datebox({
				required: required,
				validType:validType,
				editable: true,
				delay: 10,
				buttons:buttons,
				formatter:function(date){
					if(!$('#'+id).datebox('isValid')){
						return $('#'+id).datebox('getValue');
					}
					var y = date.getFullYear();
					var m = date.getMonth()+1;
					var d = date.getDate();
					return y*10000 + m*100 + d*1;
				},
				parser:function(s){
					if (!s) return new Date();
					var y = parseInt((s+'').substr(0,4),10);
					var m = parseInt((s+'').substr(4,2),10);
					var d = parseInt((s+'').substr(6,2),10);
					if (!isNaN(y) && !isNaN(m) && !isNaN(d)){
						return new Date(y,m-1,d);
					} else {
						return new Date();
					}
				},
				onSelect: function(date){
					var y = date.getFullYear();
					var m = date.getMonth()+1;
					var d = date.getDate();
					$('#'+id).combo('setText', y*10000 + m*100 + d*1);
					$('#'+id).datebox('setValue', y*10000 + m*100 + d*1);
					$('#'+id).combo('textbox').blur();
				},
				onChange:function(newValue, oldValue){
					if(that.prefix === undefined && isSearch){
						// 検索ボタン有効化
						$.setButtonState('#'+$.id.btn_search, true, id);
					}
				}
			});
			$('#'+id).combo('textbox').blur(function(){
				if(required){
					// 必須入力を一時解除 <-- エラー判定で不具合が出てしまうため
					$('#'+id).combo('textbox').validatebox('options').required = false;
				}
				if($('#'+id).combo('textbox').val() == ''){
					preVal = '';
				}else if(! $('#'+id).datebox('isValid')){
					if(idx > 0) alert($.data(this,'validatebox').message);
					if(required){
						$('#'+id).combo('setText', preVal);
						$('#'+id).datebox('setValue', preVal);
					}else{
						$('#'+id).combo('setText', '');
						$('#'+id).datebox('setValue', '');
					}
					if(idx > 0) setTimeout(function(){ $('#'+id).combo('textbox').focus(); }, 10);
				}else{
					preVal = $('#'+id).datebox('getValue');
				}
				idx = 1;
				if(required){
					// 必須入力を元に戻す
					$('#'+id).combo('textbox').validatebox('options').required = true;
				}
			}).blur();	// 初期値を保持するためにイベントを実行
			if(that){
				if (that.initedObject != undefined && $.inArray(id, that.initedObject) < 0){
					that.initedObject.push(id);
				}
				// ログ出力
				$.log(that.timeData, id + ' init:');
				if(that.prefix === undefined){
					// 検索ボタン有効化
					$.setButtonState('#'+$.id.btn_search, true, id);
					// 初期表示検索処理
					$.initialSearch(that);
				}
			}
			$('#'+id).combo('textbox').css('ime-mode', 'disabled').attr('maxlength', 8);
			if($('#'+id).attr('tabindex')){
				$('#'+id).combo('textbox').attr('tabindex', $('#'+id).attr('tabindex'));
			}
		},


		/**
		 * Web商談
		 * 詳細 grid load時選択制御処理
		 * @param prefix
		 */
		onInitAllSelect:function(prefix){
			if(prefix === undefined) prefix = '';
			var dg = $($.id.gridholder + prefix);
			if($($.id.hiddenSelectIdx+prefix).val() !== ''){
				var opts = dg.datagrid('options');
				var totalIdx = (opts.pageNumber - 1) * parseInt(opts.pageSize);
				var index = $($.id.hiddenSelectIdx + prefix).val().split(',');
				var len =  dg.datagrid('getRows').length;
				var i = 0;
				while(i < len) {
					if($.inArray((totalIdx + i)+'', index) !== -1){
						var input = dg.datagrid('getPanel').find('.datagrid-row :input[id^="ck_'+i+'"]');
						if(input.is(':enabled')){
							input.prop('checked', true);
						}
					}
					i++;
				}
			}
			dg.datagrid('getPanel').find('.datagrid-header-check input:checkbox').hide();
		},

		/**
		 * Web商談
		 * 詳細 grid 選択処理
		 * @param rowIndex
		 * @param prefix
		 */
		onCheck_grid:function (rowIndex, prefix){
			if(prefix === undefined) prefix = '';
			var dg = $($.id.gridholder + prefix);
			var opts = dg.datagrid('options');
			var totalIdx = $.getTotalIdx(opts.pageNumber, opts.pageSize, rowIndex);
			var index = $($.id.hiddenSelectIdx + prefix).val().split(',');
			if($.inArray(totalIdx+'', index) === -1){
				index.push(totalIdx);
				$($.id.hiddenSelectIdx + prefix).val(index.join(',').replace(/^,/,''));
			}
		},

		/**
		 * Web商談
		 * 詳細 grid 選択解除処理
		 * @param rowIndex
		 * @param prefix
		 */
		onUnCheck_grid:function (rowIndex, prefix){
			if(prefix === undefined) prefix = '';
			if($($.id.hiddenSelectdIdx+prefix).val() !== ''){
				var dg = $($.id.gridholder + prefix);
				var opts = dg.datagrid('options');
				var totalIdx = $.getTotalIdx(opts.pageNumber, opts.pageSize, rowIndex);
				var index = $($.id.hiddenSelectIdx + prefix).val().split(',');
				if($.inArray(totalIdx+'', index) !== -1){
					index.splice($.inArray(totalIdx+'', index), 1);
					$($.id.hiddenSelectIdx + prefix).val(index.join(',').replace(/^,/,''));
				}
			}
		},

		/**
		 * Web商談
		 *  詳細 grid 内 入力項目変更時処理
		 * @param id
		 * @param value
		 * @param prefix
		 */
		onChangeCell:function (id, value, prefix){
			if(prefix === undefined) prefix = '';
			var field = id.split('_')[0];
			var index = id.split('_')[1];
			$($.id.gridholder + prefix).datagrid('getRows')[index][field] = value;
			// 変更行Index保持
			$.setChangeIdx_grid(index, prefix);
		},

		/**
		 * Web商談
		 * 変更行Index保持
		 * @param rowIndex
		 * @param prefix
		 */
		setChangeIdx_grid:function (rowIndex, prefix){
			if(prefix === undefined) prefix = '';
			var dg = $($.id.gridholder + prefix);
			if(dg){
				var opts = dg.datagrid('options');
				var totalIdx = $.getTotalIdx(opts.pageNumber, opts.pageSize, rowIndex);
				var index = $($.id.hiddenChangedIdx + prefix).val().split(',');
				if($.inArray(totalIdx+'', index) === -1){
					index.push(totalIdx);
					$($.id.hiddenChangedIdx + prefix).val(index.join(',').replace(/^,/,''));
				}
			}else{
				$($.id.hiddenChangedIdx + prefix).val(rowIndex);
			}
		},

		/**
		 * Web商談
		 * 詳細 grid 全データ内の行indexを取得
		 * @param pageNumber
		 * @param pageSize
		 * @param index
		 */
		getTotalIdx:function(pageNumber, pageSize, index){
			return (parseInt(pageNumber) - 1) * parseInt(pageSize) + parseInt(index);

		},

		len : {
			shncd	: 8,
			sircd	: 6,
			tengp	: 4,
			tencd	: 3,
			hsptn	: 3,
			ptnno	: 3,
			bmncd	: 2,
			rban	: 2,
			rankno	: 3
		},

		id : {
			 setHeight				:	8				// オフセット
			,action_default			:	"run"			// action パラメータの初期値
			,action_get				:	"get"			// action パラメータ データ取得用
			,action_init			:	"init"			// action パラメータ 項目初期化
			,action_change			:	"change"		// action パラメータ 項目変更
			,action_check			:	"check"			// action パラメータ 項目チェック
			,action_items			:	"items"			// action パラメータ 商品取得
			,action_tenpo			:	"tenpo"			// action パラメータ 店舗グループ
			,action_shire			:	"shire"			// action パラメータ 仕入先グループ
			,action_maker			:	"maker"			// action パラメータ メーカーグループ
			,action_shiori			:	"shiori"		// action パラメータ 定義保存
			,action_store			:	"store"			// action パラメータ 保存
			,action_update			:	"update"		// action パラメータ 更新
			,action_delete			:	"delete"		// action パラメータ 削除
			,send_mode				:	"sendMode"		// 転送モード

			,valueKikan_Month		:	"1"				// 期間：月
			,valueKikan_Week		:	"2"				// 期間：週
			,valueKikan_Day			:	"3"				// 期間：日
			,valueKikan_FYear		:	"4"				// 期間：年度

			,valueSel_Head			:	"-1" 			// 選択リストヘッド(値)
			,valueSel_HeadTxt		:	"すべて" 		// 選択リストヘッド(文言)
			,valueSel_MultTxt		:	"複数" 			// 選択リスト複数(文言)

			,value_on				:	"1"				// チェックon（選択）
			,value_off				:	"0"				// チェックoff（未選択）
			,text_on				:	"✔"			// チェックon（選択）
			,text_off				:	""				// チェックoff（未選択）

			,value_gpkbn_sir		:	"1"				// グループ区分：仕入グループ
			,value_gpkbn_baika		:	"2"				// グループ区分：売価グループ
			,value_gpkbn_shina		:	"3"				// グループ区分：品揃グループ
			,value_gpkbn_tbmn		:	"4"				// グループ区分：店別異部門
			,value_tablekbn_sei		:	"0"				// テーブル区分：正
			,value_tablekbn_yyk		:	"1"				// テーブル区分：予約
			,value_tablekbn_csv		:	"9"				// テーブル区分：CSV　※処理用に追加
			,value_csvupdkbn_new	:	"A"				// CSV登録区分：新規
			,value_csvupdkbn_upd	:	"U"				// CSV登録区分：変更
			,value_csvupdkbn_yyk	:	"Y"				// CSV登録区分：予約1変更
			,value_csvupdkbn_ydel	:	"D"				// CSV登録区分：予約1取り消し

			,value_moykbn_r			:	"0"				// 催し区分：レギュラー
			,value_moykbn_t			:	"1"				// 催し区分：特売
			,value_moykbn_s			:	"2"				// 催し区分：スポット
			,value_code_shn			:	"1"				// コード：商品コード
			,value_code_src			:	"2"				// コード：ソースコード
			,value_code_han			:	"3"				// コード：販売コード
			,value_tenkaikbn_tr		:	"1"				// 展開方法：通常率パターン
			,value_tenkaikbn_ts		:	"2"				// 展開方法：通常数量パターン
			,value_tenkaikbn_jr		:	"3"				// 展開方法：実績率パターン
			,value_addshukbn_1		:	"1"				// 登録種別：全品割引
			,value_addshukbn_2		:	"2"				// 登録種別：ﾄﾞﾗｲ
			,value_addshukbn_3		:	"3"				// 登録種別：青果
			,value_addshukbn_4		:	"4"				// 登録種別：鮮魚
			,value_addshukbn_5		:	"5"				// 登録種別：精肉
			,value_addshukbn_4		:	"4"				// 登録種別：鮮魚
			,value_addshukbn_5		:	"5"				// 登録種別：精肉

			,hidden_reportno		:	"#reportno"			// レポート番号
			,hidden_userid			:	"#userid"			// ユーザーID
			,hidden_userTenpo		:	"#userTenpo"		// ユーザー店舗
			,hidden_userBumon		:	"#userBumon"		// ユーザー部門情報
			,hiddenUser				:	"#hiddenUser"		// ユーザーCD
			,hiddenReport			:	"#hiddenReport"		// レポートCD
			,hiddenSendParam		:	"#hiddenParam"		// パラメータ
			,hiddenInit				:	"#hiddenInit"		// 初期検索条件
			,hiddenChangedIdx		:	"#hiddenChangedIdx"	// 変更があった箇所のIndexを保持
			,hiddenSelectIdx		:	"#hiddenSelectIdx"	// 選択した箇所のIndexを保持

			/* Web商談 */
			,hiddenNoTeian			: "hiddenNoTeian"			// 非表示（件名No）
			,hiddenTorihiki			: "hiddenTorihiki"			//
			,hiddenShohin			: "hiddenShohin"			// 非表示（代表スキャニング）
			,hiddenStcdShikakari	: "hiddenStcdShikakari"	// 非表示（状態_仕掛商品）
			,hiddenStcdTeian	    : "hiddenStcdTeian"	// 非表示（状態_仕掛商品）

			,btn_search				:	"btn_search"		// 検索ボタン
			,btn_excel				:	"btn_excel"			// EXCELボタン
			,btn_reset				:	"btn_reset"			// 条件リセットボタン
			,btn_back				:	"btn_back"			// 条件リセットボタン
			,btn_clear				:	"btn_clear"			// ページクリアボタン

			,btn_view_shiori		:	"btn_view_shiori"	// 定義保存：適用ボタン
			,btn_entry_shiori		:	"btn_entry_shiori"	// 定義保存：保存ボタン
			,btn_delete_shiori		:	"btn_delete_shiori"	// 定義保存：削除ボタン
			,btn_Input				:	"btn_input"			// 商品入力
			,btn_entry				:	"btn_entry"			// 登録ボタン
			,btn_call				:	"btn_call"			// 呼出ボタン
			,btn_delete				:	"btn_delete"		// 削除ボタン
			,btn_add				:	"btn_add"			// 追加ボタン
			,btn_undo				:	"btn_undo"			// 戻すボタン
			,btn_reload				:	"btn_reload"		// 更新ボタン
			,btn_Input_TenpoG		:	"btn_input_TenpoG"		// 店舗グループ
			,btn_entry_subGyotai	:	"btn_entry_subGyotai"	// 保存ボタン（サブ業態）
			,btn_entry_tg			:	"btn_entry-tg"		// 登録ボタン（店舗グループ）
			,btn_call_tg			:	"btn_call-tg"		// 呼出ボタン（店舗グループ）
			,btn_delete_tg			:	"btn_delete-tg"		// 削除ボタン（店舗グループ）
			,btn_upload				:	"btn_upload"		// アップロード
			,btn_download			:	"btn_download"		// ダウンロード
			,btn_srcexcel			:	"btn_srcexcel"		// 検索＋Excel出力


			,btn_upd				:	"btn_upd"			// F12:登録ボタン
			,btn_del				:	"btn_del"			// 削除ボタン
			,btn_cancel				:	"btn_cancel"		// キャンセルボタン
			,btn_select				:	"btn_select"		// 選択ボタン
			,btn_copy				:	"btn_copy"			// 新規コピー
			,btn_csv				:	"btn_csv"			// CSV出力
			,btn_new				:	"btn_new"			// 新規
			,btn_sel_copy			:	"btn_sel_copy"		// 選択(新規コピー)
			,btn_sel_change			:	"btn_sel_change"	// 選択(変更)
			,btn_sel_refer			:	"btn_sel_refer"		// 選択(参照)
			,btn_sel_kakutei		:	"btn_sel_kakutei"	// 選択(確定
			,btn_sel_view			:	"btn_sel_view"		// 選択(表示)
			,btn_sel_tenbetubrt		:	"btn_sel_tenbetubrt"	// 選択(店別分配率)
			,btn_sel_shninfo		:	"btn_sel_shninfo"	// 選択(商品情報)
			,btn_all_del			:	"btn_all_del"		// 全削除
			,btn_start				:	"btn_start"			// 開始
			,btn_stop				:	"btn_stop"			// 停止
			,btn_kyoka				:	"btn_kyoka"			// 許可

			,btn_sel_csverr			:	"btn_sel_csverr"	// CSVエラー選択
			,btn_csv_import			:	"btn_csv_import"	// CSV取込
			,btn_csv_import_yyk		:	"btn_csv_import_yyk"	// CSV予約取込
			,btn_yoyaku1			:	"btn_yoyaku1"		// 予約1
			,btn_yoyaku2			:	"btn_yoyaku2"		// 予約2
			,btn_sei				:	"btn_sei"			// 正
			,btn_file				:	"btn_file"			// ファイル選択
			,btn_calc				:	"btn_calc"			// 計算
			,btn_err_list			:	"btn_err_list"		// エラーリスト出力
			,btn_err_change			:	"btn_err_change"	// エラー修正
			,btn_futai_search		:	"btn_futai_search"	// 風袋検索
			,btn_tab1				:	"btn_tab1"			// タブ別登録
			,btn_tab2				:	"btn_tab2"			// タブ別登録
			,btn_cancel2			:	"btn_cancel2"		// タブ別キャンセル
			,btn_back2				:	"btn_back2"			// タブ別バック

			// サブウインドウ呼出し系
			,btn_sub				:	"btn_sub"			// サブウインドウ汎用
			,btn_maker				:	"btn_maker"			// メーカー検索
			,btn_sir				:	"btn_sir"			// 仕入先検索
			,btn_tengp				:	"btn_tengp"
			,btn_hsptn				:	"btn_hsptn"
			,btn_srccd				:	"btn_srccd"
			,btn_tenkabutsu			:	"btn_tenkabutsu"
			,btn_allergy			:	"btn_allergy"
			,btn_group				:	"btn_group"
			,btn_tenpo				:	"btn_tenpo"
			,btn_tenbetusu			:	"btn_tenbetusu"		// 店別数量展開(/ST021)
			,btn_rankno				:	"btn_rankno"		// ランクNo.
			,btn_ptn				:	"btn_ptn"			// パターン(ST012/ST013)
			,btn_hbten				:	"btn_hbten"			// 販売店確認(TG018)
			,btn_nnten				:	"btn_nnten"			// 納入店確認(TG019)
			,btn_tenkai				:	"btn_tenkai"		// 数値展開方法(ST020)
			,btn_shncd				:	"btn_shncd"			// 商品番号検索

			// プライスカード
			,btn_sakubaikakb1		:	"btn_sakubaikakb1"	// 作成売価区分 1:標準売価
			,btn_sakubaikakb2		:	"btn_sakubaikakb2"	// 作成売価区分 2:店別売価

			// 店グループ一覧
			,btn_sel_refer_teng		:	"btn_sel_refer_teng"	// 選択(店グループ一覧)

			// 催し別送信情報
			,btn_checklist			:	"btn_checklist"			// チェックリスト
			,btn_taisyoten			:	"btn_taisyoten"			// 対象店
			,btn_jyogai				:	"btn_jyogai"			// 除外店
			,btn_tenkakunin			:	"btn_tenkakunin"		// 店確認

			// ランクマスタ
			,btn_tenbetusu			:	"btn_tenbetusu"		// 店別数量展開
			,btn_jissekirefer		:	"btn_jissekirefer"	// 実績参照
			,btn_tennoorder			:	"btn_tennoorder"	// 店番順
			,btn_rankorder			:	"btn_rankorder"		// ランク順
			,btn_jissekiorder		:	"btn_jissekiorder"	// 実績順
			,btn_set				:	"btn_set"			// 設定

			// 催し検索
			,btn_tennoview			:	"btn_tennoview"		// 店番表示

			// 事前発注
			,btn_kouseihi			:	"btn_kouseihi"		// 構成比
			,btn_bunruimeisai		:	"btn_bunruimeisai"	// 分類明細
			,btn_saikeisan			:	"btn_saikeisan"		// 再計算
			,btn_addline			:	"btn_addline"		// 行追加

			// Web商談
			,btn_prev				: "btn_prev"		// 作成中
			,btn_next				: "btn_next"		// 確定
			,btn_shikakari		: "btn_shikakari" // 仕掛
			,btn_return			:	"btn_return"		// 戻るボタン

			,SelKikan				:	"SelKikan"		// 選択リスト（期間）
			,SelKikanF				:	"SelKikanF"		// 選択リスト（期間FROM）
			,SelKikanT				:	"SelKikanT"		// 選択リスト（期間TO）
			,SelYmdF				:	"SelYmdF"		// 選択リスト（年月日FROM）
			,SelYmdT				:	"SelYmdT"		// 選択リスト（年月日TO）
			,SelYmF					:	"SelYmF"		// 選択リスト（年月FROM）
			,SelYmT					:	"SelYmT"		// 選択リスト（年月TO）
			,SelYearF				:	"SelYearF"		// 選択リスト（年FROM）
			,SelYearT				:	"SelYearT"		// 選択リスト（年TO）
			,SelWeekF				:	"SelWeekF"		// 選択リスト（週FROM）
			,SelWeekT				:	"SelWeekT"		// 選択リスト（週TO）
			,SelFYearF				:	"SelFYearF"		// 選択リスト（年度FROM）
			,SelFYearT				:	"SelFYearT"		// 選択リスト（年度TO）
			,SelKikanF2				:	"SelKikanF2"	// 選択リスト（比較期間FROM）
			,SelKikanT2				:	"SelKikanT2"	// 選択リスト（比較期間TO）
			,SelYmdF2				:	"SelYmdF2"		// 選択リスト（比較年月日FROM）
			,SelYmdT2				:	"SelYmdT2"		// 選択リスト（比較年月日TO）
			,SelYmF2				:	"SelYmF2"		// 選択リスト（比較年月FROM）
			,SelYmT2				:	"SelYmT2"		// 選択リスト（比較年月TO）
			,SelYearF2				:	"SelYearF2"		// 選択リスト（比較年FROM）
			,SelYearT2				:	"SelYearT2"		// 選択リスト（比較年TO）
			,SelWeekF2				:	"SelWeekF2"		// 選択リスト（比較週FROM）
			,SelWeekT2				:	"SelWeekT2"		// 選択リスト（比較週TO）
			,SelFYearF2				:	"SelFYearF2"	// 選択リスト（比較年度FROM）
			,SelFYearT2				:	"SelFYearT2"	// 選択リスト（比較年度TO）
			,SelTenpoG				:	"SelTenpoG"		// 選択リスト（店舗グループ）
			,SelTenpo				:	"SelTenpo"		// 選択リスト（店舗）
			,SelKigyo				:	"SelKigyo"		// 選択リスト（企業）
			,SelHanToubu			:	"SelHanToubu"	// 選択リスト（販売統括部門）
			,SelHanbaibu			:	"SelHanbaibu"	// 選択リスト（販売部門）
			,SelBumonG				:	"SelBumonG"		// 選択リスト（部門グループ）
			,SelBumon				:	"SelBumon"		// 選択リスト（部門）
			,SelDaiBun				:	"SelDaiBun"		// 選択リスト（大分類）
			,SelChuBun				:	"SelChuBun"		// 選択リスト（中分類）
			,SelShoBun				:	"SelShoBun"		// 選択リスト（小分類）
			,SelSyohin				:	"SelSyohin"		// 選択リスト（商品）
			,SelShiori				:	"SelShiori"		// 選択リスト（定義保存）
			,SelWhere				:	"SelWhere"		// 選択リスト（条件）
			,SelCategory			:	"SelCategory"	// 選択リスト（商品カテゴリ）

			,sel_zeirtkbn			:	"sel_zeirtkbn"		// 選択リスト（税率区分)
			,sel_zeirtkbn_old		:	"sel_zeirtkbn_old"	// 選択リスト（旧税率区分)
			,sel_bnnruikbn			:	"sel_bnnruikbn"		// 選択リスト（分類区分)
			,sel_readtmptn			:	"sel_readtmptn"		// 選択リスト（リードタイムパターン)
			,sel_shuno				:	"sel_shuno"			// 選択リスト（週№)
			,sel_bycd				:	"sel_bycd"			// 選択リスト（BY)
			,sel_tenkn				:	"sel_tenkn"			// 選択リスト（店舗名称(漢字))
			,sel_moyscd				:	"sel_moyscd"		// 選択リスト（催しコード)
			,sel_center_orr			:	"sel_center_orr"	// 選択リスト（センターコード）
			,sel_supplyno_orr		:	"sel_supplyno_orr"	// 選択リスト（便コード）
			,sel_shunoperiod		:	"sel_shunoperiod"	// 選択リスト（週№)
			,sel_display			:	"sel_display"		// 表示する・しない

			// Web商談
			,SelLine						: "SelLine"					// ライン
			,SelClass					: "SelClass"				// クラス
			,SelTorihiki				: "SelTorihiki"				// 取引先
			,SelTeian					: "SelTeian"				// 提案件名
			,SelHatyu					: "SelHatyu"				// 発注先
			,SelStcdKenmei			: "SelStcdKenmei"		// 状態_提案件名
			,SelStcdTeian				: "SelStcdTeian"			// 状態_提案商品
			,SelStcdShikakari		: "SelStcdShikakari"	// 状態_仕掛商品

			,rad_sel				:	"rad_sel"			// ラジオボタン（選択）
			,rad_areakbn			:	"rad_areakbn"	// ラジオボタン（エリア区分）
			,rad_code				:	"rad_code"		// ラジオボタン（コード）
			,rad_pcardsz1			:	"rad_pcardsz1"		// プライスカードサイズ
			,rad_pcardsz2			:	"rad_pcardsz2"		// プライスカードサイズ
			,rad_maisuhohokb1		:	"rad_maisuhohokb1"	// 枚数指定方法１
			,rad_maisuhohokb2		:	"rad_maisuhohokb2"	// 枚数指定方法２
			,rad_sakubaikakb		:	"rad_sakubaikakb"	// 作成売価区分
			,rad_qasyukbn			:	"rad_qasyukbn"		// アンケート種類
			,rad_adopt				:	"rad_adopt"		// アンケート種類
			,rad_auriaselkbn		:	"rad_auriaselkbn"		// A総売価
			,rad_bmtyp				:	"rad_bmtyp"			// B/Mタイプ

			,rad_mstkbn				:	"rad_mstkbn"	// ラジオボタン（マスター区分）
			,rad_datakbn			:	"rad_datakbn"	// ラジオボタン（データ指定区分）
			,rad_ptnnokbn			:	"rad_ptnnokbn"	// ラジオボタン（パターンNo.区分）
			,rad_jissekibun			:	"rad_jissekibun"	// ラジオボタン（実績分類）
			,rad_wwmmflg			:	"rad_wwmmflg"	// ラジオボタン（週月フラグ）

			,rad_tkanplukbn			:	"rad_tkanplukbn"	// ラジオボタン（PLU商品・定貫商品　／　不定貫商品)
			,rad_dsuexkbn			:	"rad_dsuexkbn"		// ラジオボタン(デフォルト_数展開)
			,rad_drtexkbn			:	"rad_drtexkbn"		// ラジオボタン(デフォルト_実績率パタン数値)
			,rad_dznendskbn			:	"rad_dznendskbn"	// ラジオボタン(デフォルト_前年同週)
			,rad_ddnendskbn			:	"rad_ddnendskbn"	// ラジオボタン(デフォルト_同年同月)
			,rad_tenkaikbn			:	"rad_tenkaikbn"			// ラジオボタン展開方法)
			,rad_jskptnsyukbn		:	"rad_jskptnsyukbn"		// ラジオボタン(実績率パタン数値)
			,rad_jskptnznenmkbn		:	"rad_jskptnznenmkbn"	// ラジオボタン(実績率パタン前年同月)
			,rad_jskptnznenwkbn		:	"rad_jskptnznenwkbn"	// ラジオボタン(績率パタン前年同週)


			,ChkKei					:	"ChkKei"		// チェックボックス（小計・合計表示）
			,ChkKiz					:	"ChkKiz"		// チェックボックス（既存店小計表示）
			,ChkTan					:	"ChkTan"		// チェックボックス（単日データ表示）
			,ChkRui					:	"ChkRui"		// チェックボックス（累計データ表示）
			,ChkShiori				:	"ChkShiori"		// チェックボックス（定義保存の共有）
			,ChkSys					:	"ChkSys"		// チェックボックス（詳細表示）

			,chk_del				:	"chk_del"		// チェックボックス（削除)
			,chk_use				:	"chk_use"		// チェックボックス（使用)
			,chk_sel				:	"chk_sel"		// チェックボックス（選択)
			,chk_csv				:	"chk_csv"		// チェックボックス（CSV対象)
			,chk_updkbn				:	"chk_updkbn"	// チェックボックス（削除)

			,chk_kariflg			:	"chk_kariflg"		// チェックボックス（仮フラグ)
			,chk_giftcd				:	"chk_giftcd"		// チェックボックス（ギフトコード)

			,chk_dmakercd			:	"chk_dmakercd"	// チェックボックス（代表メーカー)
			,chk_makercd			:	"chk_makercd"	// チェックボックス（メーカー)
			,chk_tshuflg			:	"chk_tshuflg"	// チェックボックス（特別週)
			,chk_nenmatkbn			:	"chk_nenmatkbn"	// チェックボックス（年末区分)
			,chk_hnctlflg			:	"chk_hnctlflg"	// チェックボックス（アンケート本部ctl)
			,chk_hbokureflg			:	"chk_hbokureflg"// チェックボックス（1日遅パターン有)
			,chk_gtsimeflg			:	"chk_gtsimeflg"	// チェックボックス（月締)
			,chk_jlstcreflg			:	"chk_jlstcreflg"// チェックボックス（事発リスト作成済)
			,chk_tpng1flg			:	"chk_tpng1flg"	// チェックボックス（店不採用禁止)
			,chk_tpng2flg			:	"chk_tpng2flg"	// チェックボックス（店売価選択禁)
			,chk_tpng3flg			:	"chk_tpng3flg"	// チェックボックス（店商品選択禁止)
			,chk_sime1flg			:	"chk_sime1flg"	// チェックボックス（リーダー仮締)
			,chk_sime2flg			:	"chk_sime2flg"	// チェックボックス（リーダー本締)
			,chk_simeflg			:	"chk_simeflg"	// チェックボックス（各店締)
			,mbsy_flg				:	"mbsy_flg"	// チェックボックス（採用区分）
			,urisel_flg				:   "urisel_flg" //チェックボックス（総売価）
			,hbstrt_flg				:	"hbstrt_flg"	// チェックボックス（通常開始）
			,kyosei_flg 			:	"kyosei_flg"   // チェックボックス（強制フラグ）
			,chk_rinji				:	"chk_rinji"		// チェックボックス（臨時)

			,chk_hbslideflg			:	"chk_hbslideflg"	// チェックボックス（一日遅スライドしない-販売)
			,chk_nhslideflg			:	"chk_nhslideflg"	// チェックボックス（一日遅スライドしない-納入)
			,chk_higawrflg			:	"chk_higawrflg"		// チェックボックス（日替)
			,chk_chirasflg			:	"chk_chirasflg"		// チェックボックス（チラシ未掲載)
			,chk_htgenbaikaflg		:	"chk_htgenbaikaflg"	// チェックボックス（発注原売価適用しない)
			,chk_plusndflg			:	"chk_plusndflg"		// チェックボックス（PLU配信しない)
			,chk_yoriflg			:	"chk_yoriflg"		// チェックボックス（よりどり)
			,chk_namanetukbn		:	"chk_namanetukbn"	// チェックボックス（生食・加熱)
			,chk_kaitoflg			:	"chk_kaitoflg"		// チェックボックス（解凍)
			,chk_yoshokuflg			:	"chk_yoshokuflg"	// チェックボックス（養殖)
			,chk_juflg				:	"chk_juflg"			// チェックボックス（事前打出(チェック))
			,chk_cuttenflg			:	"chk_cuttenflg"		// チェックボックス（カット店展開しない)
			,chk_shudenflg			:	"chk_shudenflg"		// チェックボックス（週次伝送flg)
			,chk_pckbn				:	"chk_pckbn"			// チェックボックス（PC区分)
			,chk_hbdt				:	"chk_hbdt"			// チェックボックス（販売日)
			,chk_nndt				:	"chk_nndt"			// チェックボックス（納入日)
			,chk_seiki				:	"chk_seiki"			// チェックボックス（正規)
			,chk_jisyu				:	"chk_jisyu"			// チェックボックス（次週)

			,chk_seisen				:	"chk_seisen"		// チェックボックス（生鮮)
			,chk_nninfo				:	"chk_nninfo"		// チェックボックス（納入情報)
			,chk_hbinfo				:	"chk_hbinfo"		// チェックボックス（販売情報)
			,chk_ksdaibrui			:	"chk_ksdaibrui"		// チェックボックス（検証-大分類)
			,chk_kschubrui			:	"chk_kschubrui"		// チェックボックス（検証-中分類)
			,chk_dsuexrtptn			:	"chk_dsuexrtptn"	// チェックボックス（デフォルト_数展開-率P)
			,chk_dsuexsuptn			:	"chk_dsuexsuptn"	// チェックボックス（デフォルト_数展開-数P)
			,chk_dsuexjrtptn		:	"chk_dsuexjrtptn"	// チェックボックス（デフォルト_数展開-実P)
			,chk_drtexuri			:	"chk_drtexuri"		// チェックボックス（デフォルト_実績率パターン数値-売上)
			,chk_drtexten			:	"chk_drtexten"		// チェックボックス（デフォルト_実績率パターン数値-点数)
			,chk_dznendsdai			:	"chk_dznendsdai"	// チェックボックス（デフォルト_前年同週-大)
			,chk_dznendschu			:	"chk_dznendschu"	// チェックボックス（デフォルト_前年同週-中)
			,chk_ddnendsdai			:	"chk_ddnendsdai"	// チェックボックス（デフォルト_同年同月-大)
			,chk_ddnendschu			:	"chk_ddnendschu"	// チェックボックス（デフォルト_同年同月-中)
			,chk_dcutex				:	"chk_dcutex"		// チェックボックス（デフォルト_カット店展開)
			,chk_dchiras			:	"chk_dchiras"		// チェックボックス（デフォルト_ちらしのみ)

			,TxtNumber				:	"TxtNumber"		// テキスト（件数）

			,txt_pass				:	"txt_pass"			// テキスト（パスワード）
			,txt_file				:	"txt_file"			// テキスト（ファイル）
			,txt_sel_shncd			:	"txt_sel_shncd"		// テキスト（選択商品コード)
			,txt_sel_shnkn			:	"txt_sel_shnkn"		// テキスト（選択商品名（漢字）)
			,txt_sel_bmncd			:	"txt_sel_bmncd"		// テキスト（選択部門コード)
			,txt_sel_sircd			:	"txt_sel_sircd"		// テキスト（選択部門コード)
			,txt_rg_soubaika		:	"txt_rg_soubaika"	// テキスト（レギュラー総売価)
			,txt_rg_neire			:	"txt_rg_neire"		// テキスト（レギュラー値入率)
			,txt_hs_soubaika		:	"txt_hs_soubaika"	// テキスト（販促総売価)
			,txt_hs_neire			:	"txt_hs_neire"		// テキスト（販促値入率)
			,txt_bg_soubaika		:	"txt_bg_soubaika"	// テキスト（売価グループ総売価)
			,txt_bg_neire			:	"txt_bg_neire"		// テキスト（売価グループ値入率)
			,txt_hyokakbn			:	"txt_hyokakbn"		// テキスト（評価方法区分(kbn504))
			,txt_yoyaku				:	"txt_yoyaku"		// テキスト（予約件数)
			,txt_adddt				:	"txt_adddt"			// テキスト（登録日)
			,txt_upddt				:	"txt_upddt"			// テキスト（更新日)
			,txt_updtm				:	"txt_updtm"			// テキスト（更新時刻)
			,txt_operator			:	"txt_operator"		// テキスト（オペレータ)
			,txt_code				:	"txt_code"			// テキスト（コード)
			,txt_meishokbnkn		:	"txt_meishokbnkn"	// テキスト（名称コード区分名称)
			,txt_meishokbn			:	"txt_meishokbn"		// テキスト（名称コード区分)
			,txt_status				:	"txt_status"		// テキスト（ステータス)
			,txt_upd_number			:	"txt_upd_number"	// テキスト（取込件数)
			,txt_err_number			:	"txt_err_number"	// テキスト（エラー件数)
			,txt_ten_number			:	"txt_ten_number"	// テキスト（店舗数)
			,txt_tablekbn			:	"txt_tablekbn"		// テキスト（テーブル区分)
			,txt_seq				:	"txt_seq"			// テキスト（SEQ)
			,txt_inputno			:	"txt_inputno"		// テキスト（入力番号)
			,txt_csv_updkbn			:	"txt_csv_updkbn"	// テキスト（CSV登録区分)
			,txt_shoridt			:	"txt_shoridt"		// テキスト（処理日付)
			,txt_shoridtweek		:	"txt_shoridtweek"	// テキスト（処理日付曜日)
			,txt_shunoperiod		:	"txt_shunoperiod"	// テキスト（週№期間）
			,txt_areacd				:	"txt_areacd"		// テキスト（エリアコード）
			,txt_areakbn			:	"txt_areakbn"		// テキスト (エリア区分)

			,txt_tok_soubaika		: 	"txt_tok_soubaika"	// テキスト（特売総売価）
			,txt_tok_honbaika		: 	"txt_tok_honbaika"	// テキスト（特売本体売価）

			// Web商談
			,txt_teian_no					: "txt_teian_no"					// テキスト（件名No）
			,txt_teian						: "txt_teian"						// テキスト（提案件名）
			,txt_torihiki					: "txt_torihiki"					// テキスト（取引先）
			,txt_scan						: "txt_scan"						// テキスト（代表スキャニング）
			,txt_shohin					: "txt_shohin"					// テキスト（商品名）
			,txt_toroku					: "txt_toroku"					// テキスト（登録者）
			,txt_hatyu						: "txt_hatyu"						// テキスト（発注先）
			,txt_ymdf				:	"txt_ymdf"		// テキスト（年月日FROM）
			,txt_ymdt				:	"txt_ymdt"		// テキスト（年月日TO）

			,grd_sub				:	"grd_sub"
			,grd_maker				:	"grd_maker"
			,grd_tengp				:	"grd_tengp"
			,grd_tenpo				:	"grd_tenpo"
			,grd_hsptn				:	"grd_hsptn"
			,grd_ehsptn				:	"grd_ehsptn"
			,grd_srccd				:	"grd_srccd"
			,grd_allergy			:	"grd_allergy"
			,grd_tenkabutsu			:	"grd_tenkabutsu"
			,grd_futai				:	"grd_futai"
			,grd_kryofutai			:	"grd_kryofutai"
			,grd_group				:	"grd_group"
			,grd_moycd_r			:	"grd_moycd_r"		// グリッド：催しコード_レギュラー
			,grd_moycd_s			:	"grd_moycd_s"		// グリッド：催しコード_スポット
			,grd_moycd_t			:	"grd_moycd_t"		// グリッド：催しコード_特売
			,grd_hstgp				:	"grd_hstgp"
			,grd_hstengp			:	"grd_hstengp"
			,grd_gpten				:	"grd_gpten"
			,grd_tenpo_m			:	"grd_tenpo_m"
			,grd_shohin				:	"grd_shohin"		// グリッド：予約発注_商品一覧
			,grd_nohin				:	"grd_nohin"			// グリッド：予約発注_納品日一覧
			,grd_tenpo_yh			:	"grd_tenpo_yh"		// グリッド：予約発注_店舗一覧
			,grd_tenpo_sk			:	"grd_tenpo_sk"		// グリッド：新店改装発注_店舗一覧
			,grd_pcardsu			:	"grd_pcardsu"
			,grd_coursemt			:	"grd_coursemt"
			,grd_bmnno				:	"grd_bmnno"
			,grd_setno				:	"grd_setno"
			,grd_bmshn				:	"grd_bmshn"
			,grd_hatstrshnten		:	"grd_hatstrshnten"
			,grd_rankno				:	"grd_rankno"
			,grd_ranksuryo			:	"grd_ranksuryo"
			,grd_tenbetusu			:	"grd_tenbetusu"
			,grd_jrtptntenbetubrt	:	"grd_jrtptntenbetubrt"
			,grd_rtptntenbetubrt	:	"grd_rtptntenbetubrt"
			,grd_teninfo			:	"grd_teninfo"
			,grd_tencdiinput		:	"grd_tencdiinput"
			,grd_subwindow_runkTenTnfo:	"grd_subwindow_runkTenTnfo"
			,grd_bumonyosan			:	"grd_bumonyosan"
			,grd_genryo				:	"grd_genryo"
			,grd_subwindow_zitref	:	"grd_subwindow_zitref"
			,grd_set				:	"grd_set"
			,grd_set2				:	"grd_set2"

			,grd_adten				:	"grd_adten"

			,listtable				:	"#list"			// jqGrid 用 table id
			,toolbarform			:	"#ff"			// toolbarform
			,gridform				:	"#gf"			// gridform
			,toolbar				:	"#tb"			// toolbar
			,placeholder			:	"#placeholder"	// placeholder
			,gridholder				:	"#gridholder"	// gridholder
			,compholder				:	"#compholder"	// gridholder
			,buttons				:	"#buttons"		// buttons
			,reference				:	'#reference'	// reference：参照情報エリア
			,uploadform				:	"#uf"			// uploadform

			, header_tenpo			:	1
			, column_class			:	2
			, pageSize				:	"pageSize"
			, panelState			:	"panelState"

			, textCollapse			:	"条件表示"
			, textExpand			:	"条件非表示"
			, checkBoxOnData		:	"&#10004;"

			, TxtHanSei				:	"#TxtHanSei"
			, labelHonbu			:	"販売本部"
			, labelIchiba			:	"青果市場"


			, separator				:	"-"				// コンボボックス区切り文字
		},

		// 似たようなのがある入力項目の名称の共通箇所抜粋
		id_inp_suffix: {
			bmncd : "bmncd",
			daicd : "daicd",
			chucd : "chucd",
			shocd : "shocd",
			sshocd : "sshocd",
			sircd : "sircd"
		},

		// 画面遷移時に更新データを保持する画面のID
		id_update:{
			Reportx031 : "targetRows_x031",
			Reportx032 : "targetRows_x032",
			Reportx033 : "targetRows_x033",
			Reportx034 : "targetRows_x034",

		},

		// 入力項目のID
		id_inp: {
			// ----- 汎用 -----
			 txt_stym			: "txt_stym"			// 開始年月
			,txt_enym			: "txt_enym"			// 終了年月


			// ----- マスタ -----
			,txt_shncd			:	"txt_shncd"			// テキスト（商品コード)
			,txt_shnkn			:	"txt_shnkn"			// テキスト（商品名（漢字）)
			,txt_shnan			:	"txt_shnan"			// テキスト（商品名（カナ）)
			,txt_srccd			:	"txt_srccd"			// テキスト（ソースコード)
			,txt_ssircd			:	"txt_ssircd"		// テキスト（標準仕入先コード)
			,txt_sircd			:	"txt_sircd"			// テキスト（仕入先コード)
			,txt_sirkn			:	"txt_sirkn"			// テキスト（標準仕入先名（漢字）)
			,txt_siran			:	"txt_siran"			// テキスト（標準仕入先名（カナ）)
			,txt_makercd		:	"txt_makercd"		// テキスト（メーカーコード)
			,txt_makerkn		:	"txt_makerkn"		// テキスト（メーカー名（漢字）)
			,txt_makeran		:	"txt_makeran"		// テキスト（メーカー名（カナ）)
			,txt_jancd			:	"txt_jancd"			// テキスト（JANコード)
			,txt_csvshncd		:	"txt_csvshncd"		// テキスト（CSV出力用商品コード)
			,txt_upddtf			:	"txt_upddtf"		// テキスト（更新日from)
			,txt_upddtt			:	"txt_upddtt"		// テキスト（更新日to)
			,txt_bmncd			:	"txt_bmncd"			// テキスト（標準分類:部門コード)
			,txt_bmnan			:	"txt_bmnan"			// テキスト（部門名称（カナ）)
			,txt_bmkan			:	"txt_bmkan"			// テキスト（部門名称（漢字）)
			,txt_daicd			:	"txt_daicd"			// テキスト（標準分類:大分類コード)
			,txt_daibruian		:	"txt_daibruian"		// テキスト（大分類名称（カナ）)
			,txt_daibruikn		:	"txt_daibruikn"		// テキスト（大分類名称（漢字）)
			,txt_chucd			:	"txt_chucd"			// テキスト（標準分類:中分類コード)
			,txt_daibruian		:	"txt_chubruikn"		// テキスト（中分類名称（カナ）)
			,txt_daibruikn		:	"txt_chubruian"		// テキスト（中分類名称（漢字）)
			,txt_shocd			:	"txt_shocd"			// テキスト（標準分類:小分類コード)
			,txt_shobruian		:	"txt_shobruian"		// テキスト（小分類名称（カナ）)
			,txt_shobruikn		:	"txt_shobruikn"		// テキスト（小分類名称（漢字）)
			,txt_sshocd			:	"txt_sshocd"		// テキスト（標準分類:小小分類コード)
			,txt_sshobruian		:	"txt_sshobruian"	// テキスト（小小分類名称（カナ）)
			,txt_sshobruikn		:	"txt_sshobruikn"	// テキスト（小小分類名称（漢字）)
			,txt_yot_bmncd		:	"txt_yot_bmncd"		// テキスト（用途分類:部門コード)
			,txt_yot_daicd		:	"txt_yot_daicd"		// テキスト（用途分類:大分類コード)
			,txt_yot_chucd		:	"txt_yot_chucd"		// テキスト（用途分類:中分類コード)
			,txt_yot_shocd		:	"txt_yot_shocd"		// テキスト（用途分類:小分類コード)
			,txt_yot_sshocd		:	"txt_yot_sshocd"	// テキスト（用途分類:小小分類コード)
			,txt_uri_bmncd		:	"txt_uri_bmncd"		// テキスト（売場分類:部門コード)
			,txt_uri_daicd		:	"txt_uri_daicd"		// テキスト（売場分類:大分類コード)
			,txt_uri_chucd		:	"txt_uri_chucd"		// テキスト（売場分類:中分類コード)
			,txt_uri_shocd		:	"txt_uri_shocd"		// テキスト（売場分類:小分類コード)
			,txt_uri_sshocd		:	"txt_uri_sshocd"	// テキスト（売場分類:小小分類コード)
			,txt_nez_bmncd		:	"txt_nez_bmncd"		// テキスト（値付分類:部門コード)
			,txt_nez_daicd		:	"txt_nez_daicd"		// テキスト（値付分類:大分類コード)
			,txt_nez_chucd		:	"txt_nez_chucd"		// テキスト（値付分類:中分類コード)
			,txt_nez_shocd		:	"txt_nez_shocd"		// テキスト（値付分類:小分類コード)
			,txt_receiptan		:	"txt_receiptan"		// テキスト（レシートカナ名)
			,txt_receiptkn		:	"txt_receiptkn"		// テキスト（レシート漢字名)
			,txt_pcardkn		:	"txt_pcardkn"		// テキスト（プライスカード商品名称)
			,txt_sanchikn		:	"txt_sanchikn"		// テキスト（メーカー・産地)
			,txt_popkn			:	"txt_popkn"			// テキスト（POP名称)
			,txt_uricd			:	"txt_uricd"			// テキスト（販売コード)
			,txt_salescomkn		:	"txt_salescomkn"	// テキスト（商品コメント・セールスコピー)
			,txt_parentcd		:	"txt_parentcd"		// テキスト（親商品コード)
			,txt_kikkn			:	"txt_kikkn"			// テキスト（規格)
			,txt_kikan			:	"txt_kikan"			// テキスト（規格(カナ))
			,txt_up_yoryosu		:	"txt_up_yoryosu"	// テキスト（容量)
			,txt_up_tyoryosu	:	"txt_up_tyoryosu"	// テキスト（単位容量)
			,txt_makerkn		:	"txt_makerkn"		// テキスト（メーカー名称)
			,txt_futaiedaban	:	"txt_futaiedaban"	// テキスト（風袋枝番)
			,txt_futaikn		:	"txt_futaikn"		// テキスト（風袋名称(漢字))
			,txt_futaian		:	"txt_futaian"		// テキスト（風袋名称(カナ))
			,txt_jryo			:	"txt_jryo"			// テキスト（重量)
			,txt_tengpcd		:	"txt_tengpcd"		// テキスト（店グループ)
			,txt_tengpkn		:	"txt_tengpkn"		// テキスト（店グループ名称(漢字))
			,txt_tengpan		:	"txt_tengpan"		// テキスト（店グループ名称(カナ))
			,txt_atsuk_stdt		:	"txt_atsuk_stdt"	// テキスト（取扱期間From)
			,txt_atsuk_eddt		:	"txt_atsuk_eddt"	// テキスト（取扱期間To)
			,txt_genkaam		:	"txt_genkaam"		// テキスト（原価)
			,txt_genkart		:	"txt_genkart"		// テキスト（原価率)
			,txt_baikaam		:	"txt_baikaam"		// テキスト（本体売価)
			,txt_irisu			:	"txt_irisu"			// テキスト（店入数)
			,txt_rg_genkaam		:	"txt_rg_genkaam"	// テキスト（レギュラー原価)
			,txt_rg_baikaam		:	"txt_rg_baikaam"	// テキスト（レギュラー本体売価)
			,txt_rg_irisu		:	"txt_rg_irisu"		// テキスト（レギュラー店入数)
			,txt_hs_genkaam		:	"txt_hs_genkaam"	// テキスト（販促原価)
			,txt_hs_baikaam		:	"txt_hs_baikaam"	// テキスト（販促本体売価)
			,txt_hs_irisu		:	"txt_hs_irisu"		// テキスト（販促店入数)
			,txt_zeirthenkodt	:	"txt_zeirthenkodt"	// テキスト（税率変更日)
			,txt_itfcd			:	"txt_itfcd"			// テキスト（ITFコード)
			,txt_center_irisu	:	"txt_center_irisu"	// テキスト（センター入数)
			,txt_edi_rkbn		:	"txt_edi_rkbn"		// テキスト（EDIあり)
			,txt_hsptn			:	"txt_hsptn"			// テキスト（配送パターンコード)
			,txt_hsptnkn		:	"txt_hsptnkn"		// テキスト（配送パターン名)
			,txt_centercd		:	"txt_centercd"		// テキスト（センターコード)
			,txt_ycentercd		:	"txt_ycentercd"		// テキスト（横持先センターコード)
			,txt_readtmptn		:	"txt_readtmptn"		// テキスト（リードタイムパターン)
			,txt_readtmptnkn	:	"txt_readtmptnkn"	// テキスト（リードタイム名称)
			,txt_readtm_mon		:	"txt_readtm_mon"	// テキスト（リードタイム_月)
			,txt_readtm_tue		:	"txt_readtm_tue"	// テキスト（リードタイム_火)
			,txt_readtm_wed		:	"txt_readtm_wed"	// テキスト（リードタイム_水)
			,txt_readtm_thu		:	"txt_readtm_thu"	// テキスト（リードタイム_木)
			,txt_readtm_fri		:	"txt_readtm_fri"	// テキスト（リードタイム_金)
			,txt_readtm_sat		:	"txt_readtm_sat"	// テキスト（リードタイム_土)
			,txt_readtm_sun		:	"txt_readtm_sun"	// テキスト（リードタイム_日)
			,txt_rg_idenflg		:	"txt_rg_idenflg"	// テキスト（一括伝票フラグ)
			,txt_nmkn			:	"txt_nmkn"			// テキスト（一括伝票内容)
			,txt_hs_spotminsu	:	"txt_hs_spotminsu"	// テキスト（スポット最低発注数)
			,txt_htsu			:	"txt_htsu"			// テキスト（発注数)
			,txt_siwakekbn		:	"txt_siwakekbn"		// テキスト（仕分区分)
			,txt_hzi_yoto		:	"txt_hzi_yoto"		// テキスト（包材用途)
			,txt_hzi_zaishitu	:	"txt_hzi_zaishitu"	// テキスト（包材材質)
			,txt_seizogennisu	:	"txt_seizogennisu"	// テキスト（製造限度日数)
			,txt_taishonensu	:	"txt_taishonensu"	// テキスト（対象年齢)
			,txt_caloriesu		:	"txt_caloriesu"	// テキスト（カロリー表示)
			,txt_dosu			:	"txt_dosu"		// テキスト（度数)
			,txt_shntatesz		:	"txt_shntatesz"	// テキスト（縦商品サイズ)
			,txt_shnyokosz		:	"txt_shnyokosz"	// テキスト（横商品サイズ)
			,txt_shnokusz		:	"txt_shnokusz"	// テキスト（奥行商品サイズ)
			,txt_shnjryosz		:	"txt_shnjryosz"	// テキスト（重量商品サイズ)
			,txt_ods_harusu		:	"txt_ods_harusu"	// テキスト（春賞味期限)
			,txt_ods_natsusu	:	"txt_ods_natsusu"	// テキスト（夏賞味期限)
			,txt_ods_akisu		:	"txt_ods_akisu"		// テキスト（秋賞味期限)
			,txt_ods_fuyusu		:	"txt_ods_fuyusu"	// テキスト（冬賞味期限)
			,txt_ods_nyukasu	:	"txt_ods_nyukasu"	// テキスト（入荷期限)
			,txt_ods_nebikisu	:	"txt_ods_nebikisu"	// テキスト（値引期限)
			,txt_chinretucd		:	"txt_chinretucd"	// テキスト（陳列形式コード)
			,txt_dantumicd		:	"txt_dantumicd"		// テキスト（段積み形式コード)
			,txt_kasanaricd		:	"txt_kasanaricd"	// テキスト（重なりコード)
			,txt_kasanarisz		:	"txt_kasanarisz"	// テキスト（重なりサイズ)
			,txt_asshukurt		:	"txt_asshukurt"		// テキスト（圧縮率)
			,txt_shubetucd		:	"txt_shubetucd"		// テキスト（種別コード)
			,txt_yoyakudt		:	"txt_yoyakudt"		// テキスト（マスタ変更予定日)
			,txt_tenbaikadt		:	"txt_tenbaikadt"	// テキスト（店売価実施日)
			,txt_corpbmncd		:	"txt_corpbmncd"		// テキスト（全社部門コード)
			,txt_uribmncd		:	"txt_uribmncd"		// テキスト（売上計上部門コード)
			,txt_hoganbmncd		:	"txt_hoganbmncd"	// テキスト（包含部門コード)
			,txt_jyogenam		:	"txt_jyogenam"		// テキスト（上限金額)
			,txt_jyogensu		:	"txt_jyogensu"		// テキスト（上限数量)
			,txt_uriketakbn		:	"txt_uriketakbn"	// テキスト（売上金額最大桁数)
			,txt_kensaku		:	"txt_kensaku"		// テキスト（検索文字)
			,txt_delkijyunsu	:	"txt_delkijyunsu"	// テキスト（削除基準日数)
			,txt_commentkn		:	"txt_commentkn"		// テキスト（コメント)
			,txt_tenshncd		:	"txt_tenshncd"		// テキスト（店別異部門商品コード)

			,txt_addrkn_t 		: "txt_addrkn_t" 		// テキスト（住所_都道府県（漢字）)
			,txt_addrkn_s 		: "txt_addrkn_s"  		// テキスト（住所_市区町村（漢字）)
			,txt_addrkn_m  		: "txt_addrkn_m"  		// テキスト（住所_町字（漢字）)
			,txt_addr_b  		: "txt_addr_b"  		// テキスト（住所_番地（漢字）)
			,txt_bushokn  		: "txt_bushokn"  		// テキスト（部署名（漢字）)
			,txt_yubinno_u  	: "txt_yubinno_u"  		// テキスト（郵便番号_上桁)
			,txt_yubinno_s  	: "txt_yubinno_s"  		// テキスト（郵便番号_下桁)
			,txt_tel   			: "txt_tel"  			// テキスト（電話番号)
			,txt_naisen  		: "txt_naisen"  		// テキスト（内線番号)
			,txt_fax  			: "txt_fax"  			// テキスト（FAX番号)
			,txt_dsircd  		: "txt_dsircd"  		// テキスト（代表仕入先コード)
			,txt_doyasircd 		: "txt_doyasircd"  		// テキスト（伝送先親仕入先コード)
			,txt_startdt  		: "txt_startdt"  		// テキスト（開始日)
			,txt_nozeishano  	: "txt_nozeishano"  	// テキスト（納税者番号)
			,txt_syortankaam  	: "txt_syortankaam"  	// テキスト（処理単価)
			,txt_khnryokinam  	: "txt_khnryokinam"  	// テキスト（基本料金)
			,txt_stopflg  		: "txt_stopflg"  		// テキスト（取引停止フラグ)
			,txt_dohocd  		: "txt_dohocd"  		// テキスト（同報配信先コード)
			,txt_df_rsircd  	: "txt_df_rsircd"  		// テキスト（デフォルト_実仕入先コード)
			,txt_rsircd			: "txt_rsircd"  		// テキスト（実仕入先コード)
			,txt_sirkn_Z		: "txt_sirkn_Z"  		// テキスト（実仕入先名称)
			,txt_hsgpcd			: "txt_hsgpcd"			// テキスト（配送グループ)
			,txt_hsgpkn			: "txt_hsgpkn"			// テキスト（配送グループ名称（漢字）)
			,txt_hsgpan			: "txt_hsgpan"			// テキスト（配送グループ名称（カナ）)
			,txt_tencd			: "txt_tencd"			// テキスト（店コード)
			,txt_tenkyudt		: "txt_tenkyudt"		// テキスト（店休日)
			,txt_tenkyustdt		: "txt_tenkyustdt"		// テキスト（店休日FROM)
			,txt_tenkyuendt		: "txt_tenkyuendt"		// テキスト（店休日TO)
			,txt_tenkn			: "txt_tenkn"			// テキスト（店舗名称（漢字）)
			,txt_tenan			: "txt_tenan"			// テキスト（店舗名称（カナ）)
			,txt_tenatr5		: "txt_tenatr5"			// テキスト（店舗属性5)
			,txt_tenatr6		: "txt_tenatr6"			// テキスト（店舗属性6)
			,txt_tenopendt		: "txt_tenopendt"		// テキスト（開設日)
			,txt_tenclosedt		: "txt_tenclosedt"		// テキスト（閉鎖日)
			,txt_kaisodt1		: "txt_kaisodt1"		// テキスト（改装日（1）)
			,txt_kaisodt2		: "txt_kaisodt2"		// テキスト（改装日（2）)
			,txt_kaisodt3		: "txt_kaisodt3"		// テキスト（改装日（3）)
			,txt_dtorikomidt	: "txt_dtorikomidt"		// テキスト（データ取込開設日)
			,txt_egyotm1_stmd	: "txt_egyotm1_stmd"	// テキスト（営業時間1_開始月日)
			,txt_egyotm1_edmd	: "txt_egyotm1_edmd"	// テキスト（営業時間1_終了月日)
			,txt_egyotm1_sthm	: "txt_egyotm1_sthm"	// テキスト（営業時間1_開始時間)
			,txt_egyotm1_edhm	: "txt_egyotm1_edhm"	// テキスト（営業時間1_終了時間)
			,txt_egyotm2_stmd	: "txt_egyotm2_stmd"	// テキスト（営業時間2_開始月日)
			,txt_egyotm2_edmd	: "txt_egyotm2_edmd"	// テキスト（営業時間2_終了月日)
			,txt_egyotm2_sthm	: "txt_egyotm2_sthm"	// テキスト（営業時間2_開始時間)
			,txt_egyotm2_edhm	: "txt_egyotm2_edhm"	// テキスト（営業時間2_終了時間)
			,txt_egyotm3_stmd	: "txt_egyotm3_stmd"	// テキスト（営業時間3_開始月日)
			,txt_egyotm3_edmd	: "txt_egyotm3_edmd"	// テキスト（営業時間3_終了月日)
			,txt_egyotm3_sthm	: "txt_egyotm3_sthm"	// テキスト（営業時間3_開始時間)
			,txt_egyotm3_edhm	: "txt_egyotm3_edhm"	// テキスト（営業時間3_終了時間)
			,txt_atsuk_cd		: "txt_atsuk_cd"		// テキスト（取扱区分_CD)
			,txt_atsuk_credit	: "txt_atsuk_credit"	// テキスト（取扱区分_クレジットカード)
			,txt_atsuk_coless	: "txt_atsuk_coless"	// テキスト（取扱区分_コインレスカード)
			,txt_atsuk_point	: "txt_atsuk_point"		// テキスト（取扱区分_ポイントカード)
			,txt_staffsu		: "txt_staffsu"			// テキスト（実働人員)
			,txt_addrkn_b		: "txt_addrkn_b"		// テキスト（住所_番地（漢字）)
			,txt_addran_t		: "txt_addran_t"		// テキスト（住所_都道府県（カナ）)
			,txt_addran_s		: "txt_addran_s"		// テキスト（住所_市町村（カナ）)
			,txt_addran_m		: "txt_addran_m"		// テキスト（住所_町字（カナ）)
			,txt_addran_b		: "txt_addran_b"		// テキスト（住所_番地（カナ）)
			,txt_uriaeracd		: "txt_uriaeracd"		// テキスト（エリア)
			,txt_chiikicd		: "txt_chiikicd"		// テキスト（地域)
			,txt_moyoriekikn	: "txt_moyoriekikn"		// テキスト（最寄り駅)
			,txt_busstopkn		: "txt_busstopkn"		// テキスト（バス停)
			,txt_modelten		: "txt_modelten"		// テキスト（モデル店)
			,txt_tel1			: "txt_tel1"			// テキスト（電話番号1)
			,txt_tel2			: "txt_tel2"			// テキスト（電話番号2)
			,txt_tel3			: "txt_tel3"			// テキスト（電話番号3)
			,txt_tel4			: "txt_tel4"			// テキスト（電話番号4)
			,txt_tel5			: "txt_tel5"			// テキスト（電話番号5)
			,txt_fax1			: "txt_fax1"			// テキスト（FAX番号1)
			,txt_fax2			: "txt_fax2"			// テキスト（FAX番号2)
			,txt_ten1			: "txt_ten1"			// テキスト（競合店1)
			,txt_ten2			: "txt_ten2"			// テキスト（競合店2)
			,txt_ten3			: "txt_ten3"			// テキスト（競合店3)
			,txt_ten4			: "txt_ten4"			// テキスト（競合店4)
			,txt_ten5			: "txt_ten5"			// テキスト（競合店5)
			,txt_tenant1_syubetu	: "txt_tenant1_syubetu"		// テキスト（テナント1　種別)
			,txt_tenant2_syubetu	: "txt_tenant2_syubetu"		// テキスト（テナント2　種別)
			,txt_tenant3_syubetu	: "txt_tenant3_syubetu"		// テキスト（テナント3　種別)
			,txt_tenant4_syubetu	: "txt_tenant4_syubetu"		// テキスト（テナント4　種別)
			,txt_tenant5_syubetu	: "txt_tenant5_syubetu"		// テキスト（テナント5　種別)
			,txt_tenant6_syubetu	: "txt_tenant6_syubetu"		// テキスト（テナント6　種別)
			,txt_tenant7_syubetu	: "txt_tenant7_syubetu"		// テキスト（テナント7　種別)
			,txt_tenant8_syubetu	: "txt_tenant8_syubetu"		// テキスト（テナント8　種別)
			,txt_tenant9_syubetu	: "txt_tenant9_syubetu"		// テキスト（テナント9　種別)
			,txt_tenant10_syubetu	: "txt_tenant10_syubetu"	// テキスト（テナント10　種別)
			,txt_tenant11_syubetu	: "txt_tenant11_syubetu"	// テキスト（テナント11　種別)
			,txt_tenant12_syubetu	: "txt_tenant12_syubetu"	// テキスト（テナント12　種別)
			,txt_tenant13_syubetu	: "txt_tenant13_syubetu"	// テキスト（テナント13　種別)
			,txt_tenant14_syubetu	: "txt_tenant14_syubetu"	// テキスト（テナント14　種別)
			,txt_tenant15_syubetu	: "txt_tenant15_syubetu"	// テキスト（テナント15　種別)
			,txt_tenant16_syubetu	: "txt_tenant16_syubetu"	// テキスト（テナント16　種別)
			,txt_tenant17_syubetu	: "txt_tenant17_syubetu"	// テキスト（テナント17　種別)
			,txt_tenant18_syubetu	: "txt_tenant18_syubetu"	// テキスト（テナント18　種別)
			,txt_tenant19_syubetu	: "txt_tenant19_syubetu"	// テキスト（テナント19　種別)
			,txt_tenant20_syubetu	: "txt_tenant20_syubetu"	// テキスト（テナント20　種別)
			,txt_tenant1_syamei	: "txt_tenant1_syamei"		// テキスト（テナント1　社名)
			,txt_tenant2_syamei	: "txt_tenant2_syamei"		// テキスト（テナント2　社名)
			,txt_tenant3_syamei	: "txt_tenant3_syamei"		// テキスト（テナント3　社名)
			,txt_tenant4_syamei	: "txt_tenant4_syamei"		// テキスト（テナント4　社名)
			,txt_tenant5_syamei	: "txt_tenant5_syamei"		// テキスト（テナント5　社名)
			,txt_tenant6_syamei	: "txt_tenant6_syamei"		// テキスト（テナント6　社名)
			,txt_tenant7_syamei	: "txt_tenant7_syamei"		// テキスト（テナント7　社名)
			,txt_tenant8_syamei	: "txt_tenant8_syamei"		// テキスト（テナント8　社名)
			,txt_tenant9_syamei	: "txt_tenant9_syamei"		// テキスト（テナント9　社名)
			,txt_tenant10_syamei	: "txt_tenant10_syamei"	// テキスト（テナント10　社名)
			,txt_tenant11_syamei	: "txt_tenant11_syamei"	// テキスト（テナント11　社名)
			,txt_tenant12_syamei	: "txt_tenant12_syamei"	// テキスト（テナント12　社名)
			,txt_tenant13_syamei	: "txt_tenant13_syamei"	// テキスト（テナント13　社名)
			,txt_tenant14_syamei	: "txt_tenant14_syamei"	// テキスト（テナント14　社名)
			,txt_tenant15_syamei	: "txt_tenant15_syamei"	// テキスト（テナント15　社名)
			,txt_tenant16_syamei	: "txt_tenant16_syamei"	// テキスト（テナント16　社名)
			,txt_tenant17_syamei	: "txt_tenant17_syamei"	// テキスト（テナント17　社名)
			,txt_tenant18_syamei	: "txt_tenant18_syamei"	// テキスト（テナント18　社名)
			,txt_tenant19_syamei	: "txt_tenant19_syamei"	// テキスト（テナント19　社名)
			,txt_tenant20_syamei	: "txt_tenant20_syamei"	// テキスト（テナント20　社名)
			,txt_syakusuu1		: "txt_syakusuu1"	// テキスト（尺数1)
			,txt_syakusuu2		: "txt_syakusuu2"	// テキスト（尺数2)
			,txt_syakusuu3		: "txt_syakusuu3"	// テキスト（尺数3)
			,txt_syakusuu4		: "txt_syakusuu4"	// テキスト（尺数4)
			,txt_syakusuu5		: "txt_syakusuu5"	// テキスト（尺数5)
			,txt_syakusuu6		: "txt_syakusuu6"	// テキスト（尺数6)
			,txt_syakusuu7		: "txt_syakusuu7"	// テキスト（尺数7)
			,txt_syakusuu8		: "txt_syakusuu8"	// テキスト（尺数8)
			,txt_syakusuu9		: "txt_syakusuu9"	// テキスト（尺数9)
			,txt_syakusuu10		: "txt_syakusuu10"	// テキスト（尺数10)
			,txt_syakusuu11		: "txt_syakusuu11"	// テキスト（尺数11)
			,txt_syakusuu12		: "txt_syakusuu12"	// テキスト（尺数12)
			,txt_syakusuu13		: "txt_syakusuu13"	// テキスト（尺数13)
			,txt_syakusuu15		: "txt_syakusuu15"	// テキスト（尺数15)
			,txt_syakusuu20		: "txt_syakusuu20"	// テキスト（尺数20)
			,txt_syakusuu23		: "txt_syakusuu23"	// テキスト（尺数23)
			,txt_syakusuu34		: "txt_syakusuu34"	// テキスト（尺数34)
			,txt_syakusuu43		: "txt_syakusuu43"	// テキスト（尺数43)
			,txt_syakusuu44		: "txt_syakusuu44"	// テキスト（尺数44)
			,txt_syakusuu54		: "txt_syakusuu54"	// テキスト（尺数54)
			,txt_area_ba		: "txt_area_ba"			// テキスト（敷地面積)
			,txt_area_kentiku	: "txt_area_kentiku"	// テキスト（建築面積)
			,txt_aera_b1yuka	: "txt_aera_b1yuka"		// テキスト（敷地面積_B1_床面積)
			,txt_area_b1uriba	: "txt_area_b1uriba"	// テキスト（敷地面積_B1_売場面積)
			,txt_area_1fyuka	: "txt_area_1fyuka"		// テキスト（敷地面積_1F_床面積)
			,txt_area_furiba	: "txt_area_furiba"		// テキスト（敷地面積_1F_売場面積)
			,txt_area_2fyuka	: "txt_area_2fyuka"		// テキスト（敷地面積_2F_床面積)
			,txt_area_2furiba	: "txt_area_2furiba"	// テキスト（敷地面積_2F_売場面積)
			,txt_area_3fyuka	: "txt_area_3fyuka"		// テキスト（敷地面積_3F_床面積)
			,txt_area_3furiba	: "txt_area_3furiba"	// テキスト（敷地面積_3F_売場面積)
			,txt_area_4fyuka	: "txt_area_4fyuka"		// テキスト（敷地面積_4F_床面積)
			,txt_area_4furiba	: "txt_area_4furiba"	// テキスト（敷地面積_4F_売場面積)
			,txt_park_nm_ba		: "txt_park_nm_ba"		// テキスト（駐車台数_普通車_敷地内)
			,txt_park_nm_yane	: "txt_park_nm_yane"	// テキスト（駐車台数_普通車_屋上)
			,txt_park_nm_tobi	: "txt_park_nm_tobi"	// テキスト（駐車台数_普通車_飛地)
			,txt_park_lt_ba		: "txt_park_lt_ba"		// テキスト（駐車台数_軽_敷地内)
			,txt_park_lt_yane	: "txt_park_lt_yane"	// テキスト（駐車台数_軽_屋上)
			,txt_park_lt_tobi	: "txt_park_lt_tobi"	// テキスト（駐車台数_軽_飛地)
			,txt_park_hc_ba		: "txt_park_hc_ba"		// テキスト（駐車台数_障害者_敷地内)
			,txt_heikinkaitenritu	: "txt_heikinkaitenritu"	// テキスト（平均回転数)
			,txt_hituyoudaisuu		: "txt_hituyoudaisuu"		// テキスト（必要台数)
			,txt_park_hc_yane	: "txt_park_hc_yane"	// テキスト（駐車台数_障害者_屋上)
			,txt_park_hc_tobi	: "txt_park_hc_tobi"	// テキスト（駐車台数_障害者_飛地)
			,txt_ownt_nmkn		: "txt_ownt_nmkn"		// テキスト（オーナー（店）_名前)
			,txt_ownt_addrkn_t	: "txt_ownt_addrkn_t"	// テキスト（オーナー（店）_住所_都道府県)
			,txt_ownt_addrkn_s	: "txt_ownt_addrkn_s"	// テキスト（オーナー（店）_住所_市町村)
			,txt_ownt_addrkn_m	: "txt_ownt_addrkn_m"	// テキスト（オーナー（店）_住所_町字)
			,txt_ownt_addrkn_b	: "txt_ownt_addrkn_b"	// テキスト（オーナー（店）_住所_番地)
			,txt_ownp_nmkn		: "txt_ownp_nmkn"		// テキスト（オーナー（駐車場）_名前)
			,txt_ownp_addrkn_t	: "txt_ownp_addrkn_t"	// テキスト（オーナー（駐車場）_住所_都道府県)
			,txt_ownp_addrkn_s	: "txt_ownp_addrkn_s"	// テキスト（オーナー（駐車場）_住所_市町村)
			,txt_ownp_addrkn_m	: "txt_ownp_addrkn_m"	// テキスト（オーナー（駐車場）_住所_町字)
			,txt_ownp_addrkn_b	: "txt_ownp_addrkn_b"	// テキスト（オーナー（駐車場）_住所_番地)
			,txt_owno_nmkn		: "txt_owno_nmkn"		// テキスト（オーナー（その他）_名前)
			,txt_owno_addrkn_t	: "txt_owno_addrkn_t"	// テキスト（オーナー（その他）_住所_都道府県)
			,txt_owno_addrkn_s	: "txt_owno_addrkn_s"	// テキスト（オーナー（その他）_住所_市町村)
			,txt_owno_addrkn_m	: "txt_owno_addrkn_m"	// テキスト（オーナー（その他）_住所_町村)
			,txt_owno_addrkn_b	: "txt_owno_addrkn_b"	// テキスト（オーナー（その他）_住所_番地)
			,txt_yobidashicd	: "txt_yobidashicd"	// テキスト（呼出コード)
			,txt_shnmeijyo		: "txt_shnmeijyo"		// テキスト（商品名　上段)
			,txt_shnmeige		: "txt_shnmeige"		// テキスト（商品名　下段)
			,txt_bruicd			: "txt_bruicd"		// テキスト（値付分類コード)
			,txt_tenkabutsuno	: "txt_tenkabutsuno"	// テキスト（添加物番号)
			,txt_hozonondono	: "txt_hozonondono"	// テキスト（保存温度番号)
			,txt_hozonhohono	: "txt_hozonhohono"	// テキスト（保存方法番号)
			,txt_sanchino		: "txt_sanchino"		// テキスト（産地名番号)
			,txt_free1mstno		: "txt_free1mstno"	// テキスト（フリー1マスター番号)
			,txt_free2mstno		: "txt_free2mstno"	// テキスト（フリー2マスター番号)
			,txt_free3mstno		: "txt_free3mstno"	// テキスト（フリー3マスター番号)
			,txt_free4mstno		: "txt_free4mstno"	// テキスト（フリー4マスター番号)
			,txt_free5mstno		: "txt_free5mstno"	// テキスト（フリー5マスター番号)
			,txt_kakogentenno	: "txt_kakogentenno"	// テキスト（加工元店番号)
			,txt_zeikinno		: "txt_zeikinno"		// テキスト（税金番号)
			,txt_omoteformno	: "txt_omoteformno"	// テキスト（表フォーマット番号)
			,txt_uraformno		: "txt_uraformno"		// テキスト（裏フォーマット番号)
			,txt_jyogenjryo		: "txt_jyogenjryo"	// テキスト（上限重量)
			,txt_kagenjryo		: "txt_kagenjryo"		// テキスト（下限重量)
			,txt_futaijryo		: "txt_futaijryo"		// テキスト（風袋重量)
			,txt_codetaikei		: "txt_codetaikei"	// テキスト（コード体系)
			,txt_tonerno		: "txt_tonerno"		// テキスト（トレー番号)

			,txt_yuko_stdt		: "txt_yuko_stdt"		// テキスト（有効開始日)
			,txt_yuko_eddt		: "txt_yuko_eddt"		// テキスト（有効終了日)
			,sel_grpkn			: "sel_grpkn"			// テキスト（グループ分類名)
			,txt_storecd		: "txt_storecd"			// テキスト（店舗コード）
			,txt_seqno			: "txt_seqno"			// テキスト（入力順番）
			,txt_shoridt		: "txt_shoridt"			// テキスト（基準日）
			,txt_ahskb			: "txt_ahskb"			// テキスト（自動発注区分）
			,txt_utray			: "txt_utray"			// テキスト（使用トレイ）
			,txt_konpou			: "txt_konpou"			// テキスト（包装形態）
			,txt_futai			: "txt_futai"			// テキスト（風袋）
			,txt_naikn			: "txt_naikn"			// テキスト（内容量）
			,txt_callcd			: "txt_callcd"			// テキスト（呼出コード）
			,txt_maxsu			: "txt_maxsu"			// テキスト（登録限度数）
			,txt_de_maxsu		: "txt_de_maxsu"		// テキスト（デフォルト_登録限度数）

			// ----- 販促 -----
			,txt_shuno			: "txt_shuno"			// テキスト（週№)
			,txt_moyskbn		: "txt_moyskbn"			// テキスト（催し区分)
			,txt_moysstdt		: "txt_moysstdt"		// テキスト（催しコード（催し開始日）)
			,txt_moysrban		: "txt_moysrban"		// テキスト（催し連番)
			,txt_moysrbaninp	: "txt_moysrbaninp"		// テキスト（催し連番(入力用))
			,txt_bmnno			: "txt_bmnno"			// テキスト（B/M番号）
			,txt_setno			: "txt_setno"			// テキスト（セット番号）
			,txt_bmnmkn			: "txt_bmnmkn"			// テキスト（B/M名称（漢字））
			,txt_bmnman			: "txt_bmnman"			// テキスト（B/M名称（カナ））
			,txt_bd_kosu1		: "txt_bd_kosu1"		// テキスト（1個売り総売価1）
			,txt_baikaan1		: "txt_baikaan1"		// テキスト（1個売り総売価1金額）
			,txt_bd_kosu2		: "txt_bd_kosu2"		// テキスト（1個売り総売価2）
			,txt_baikaan2		: "txt_baikaan2"		// テキスト（1個売り総売価2金額）
			,txt_moyscd			: "txt_moyscd"			// テキスト（催しコード（連結フォーマット）)
			,txt_moyseddt		: "txt_moyseddt"		// テキスト（催し終了日）
			,txt_hbstdt			: "txt_hbstdt"			// テキスト（販売開始日)
			,txt_hbeddt			: "txt_hbeddt"			// テキスト（販売終了日)
			,txt_hbperiod		: "txt_hbperiod"		// テキスト（販売期間）
			,txt_moyperiod		: "txt_moyperiod"		// テキスト（催し期間）
			,txt_nnstdt			: "txt_nnstdt"			// テキスト（納入開始日)
			,txt_nneddt			: "txt_nneddt"			// テキスト（納入終了日)
			,txt_nnperiod		: "txt_nnperiod"		// テキスト（納入期間）
			,txt_plusddt		: "txt_plusddt"			// テキスト（PLU配信日)
			,txt_moykn			: "txt_moykn"			// テキスト（催し名称（漢字）)
			,txt_moyan			: "txt_moyan"			// テキスト（催し名称(半角カナ))
			,txt_ten_number		: "txt_ten_number"		// テキスト（店舗数）
			,txt_kkkcd			: "txt_kkkcd"			// テキスト（企画コード)
			,txt_kkkkm			: "txt_kkkkm"			// テキスト（企画名称)
			,txt_kkkno			: "txt_kkkno"			// テキスト（企画No)
			,txt_hstengpcd		: "txt_hstengpcd"		// テキスト（配送店グループコード）
			,txt_hstengpcd_a	: "txt_hstengpcd_a"		// テキスト（配送店グループコード（エリア））
			,txt_hstengpcd_t	: "txt_hstengpcd_t"		// テキスト（配送店グループコード（店））
			,txt_coman			: "txt_coman"			// テキスト（プライスカード発行トラン コメント）
			,txt_mst_yoyakudt	: "txt_mst_yoyakudt"	// テキスト（プライスカード発行トラン 商品マスタ予約日）
			,txt_copysu			: "txt_copysu"			// テキスト（プライスカード発行トラン コピー枚数）
			,txt_kosepage		: "txt_kosepage"		// テキスト（プライスカード発行トラン 構成枚数）
			,txt_maisu			: "txt_maisu"			// テキスト（プライスカード発行トラン 枚数）
			,txt_tenitemsu		: "txt_tenitemsu"		// テキスト（取扱アイテム数）
			,txt_tenplusu		: "txt_tenplusu"		// テキスト（PLUレコード数日）
			,txt_catalgno		: "txt_catalgno"  		// テキスト（カタログ番号)
			,txt_htdt			: "txt_htdt"  			// テキスト（発注日)
			,txt_ukestdt		: "txt_ukestdt"  		// テキスト（受付開始日)
			,txt_ukeeddt		: "txt_ukeeddt"  		// テキスト（受付終了日)
			,txt_tenistdt		: "txt_tenistdt" 		// テキスト（店舗入力開始日)
			,txt_tenieddt		: "txt_tenieddt" 	 	// テキスト（店舗入力終了日)
			,txt_yoteisu		: "txt_yoteisu"  		// テキスト（予定数)
			,txt_gendosu		: "txt_gendosu"  		// テキスト（限度数)
			,txt_ngflg			: "txt_ngflg"  			// テキスト（入力不可フラグ)
			,txt_updkbn			: "txt_updkbn"  		// テキスト（更新区分)
			,txt_nndt			: "txt_nndt"			// テキスト（納入日)
			,txt_htsu			: "txt_htsu"			// テキスト（発注数)
			,txt_qayyyymm		: "txt_qayyyymm"		// テキスト（アンケート月度)
			,txt_qaend			: "txt_qaend"			// テキスト（アンケート月度枝番)
			,txt_gtsimedt		: "txt_gtsimedt"		// テキスト（月締め)
			,txt_lsimedt		: "txt_lsimedt"			// テキスト（最終締)
			,txt_qacredt		: "txt_qacredt"			// テキスト（アンケート作成日)
			,txt_qarcredt		: "txt_qarcredt"		// テキスト（アンケート再作成日)
			,txt_qadevstdt		: "txt_qadevstdt"		// テキスト（アンケート取込開始日)
			,txt_gyono			: "txt_gyono"			// テキスト（行番号）
			,txt_errfld			: "txt_errfld"			// テキスト（エラー箇所）
			,txt_msgtxt1		: "txt_msgtxt1"			// テキスト（エラー理由）
			,txt_errvl			: "txt_errvl"			// テキスト（エラー値）

			,txt_addshukbn		: "txt_addshukbn"		// テキスト（登録種別)
			,txt_a_baikaam		: "txt_a_baikaam"		// テキスト（A総売価)
			,txt_a_baikaam_100g	: "txt_a_baikaam_100g"	// テキスト（A総売価)
			,txt_a_baikaam_pack	: "txt_a_baikaam_pack"	// テキスト（P総売価)
			,txt_a_genkaam_1kg	: "txt_a_genkaam_1kg"	// テキスト（1Kg総売価)
			,txt_a_writukbn		: "txt_a_writukbn"		// テキスト（A総売価)
			,txt_b_baikaam		: "txt_b_baikaam"		// テキスト（B総売価)
			,txt_b_baikaam_100g	: "txt_b_baikaam_100g"	// テキスト（B総売価)
			,txt_b_baikaam_pack	: "txt_b_baikaam_pack"	// テキスト（P総売価)
			,txt_b_genkaam_1kg	: "txt_b_genkaam_1kg"	// テキスト（1Kg総売価)
			,txt_b_writukbn		: "txt_b_writukbn"		// テキスト（B総売価)
			,txt_bd1_a_baikaan	: "txt_bd1_a_baikaan"	// テキスト（総売価1A)
			,txt_bd1_b_baikaan	: "txt_bd1_b_baikaan"	// テキスト（総売価1B)
			,txt_bd1_c_baikaan	: "txt_bd1_c_baikaan"	// テキスト（総売価1C)
			,txt_bd1_tensu		: "txt_bd1_tensu"		// テキスト（点数1)
			,txt_bd2_a_baikaan	: "txt_bd2_a_baikaan"	// テキスト（総売価２A)
			,txt_bd2_b_baikaan	: "txt_bd2_b_baikaan"	// テキスト（総売価２B)
			,txt_bd2_c_baikaan	: "txt_bd2_c_baikaan"	// テキスト（総売価２C)
			,txt_bd2_tensu		: "txt_bd2_tensu"		// テキスト（点数2)
			,txt_bdenkbn		: "txt_bdenkbn"			// テキスト（別伝区分)
			,txt_binkbn			: "txt_binkbn"			// テキスト（便区分)
			,txt_c_baikaam		: "txt_c_baikaam"		// テキスト（C総売価)
			,txt_c_baikaam_100g	: "txt_c_baikaam_100g"	// テキスト（C総売価)
			,txt_c_baikaam_pack	: "txt_c_baikaam_pack"	// テキスト（P総売価)
			,txt_c_genkaam_1kg	: "txt_c_genkaam_1kg"	// テキスト（1Kg総売価)
			,txt_c_writukbn		: "txt_c_writukbn"		// テキスト（C総売価)
			,txt_chldno			: "txt_chldno"			// テキスト（子No.)
			,txt_comment_hgw	: "txt_comment_hgw"		// テキスト（その他日替コメント)
			,txt_comment_pop	: "txt_comment_pop"		// テキスト（POPコメント)
			,txt_comment_tb		: "txt_comment_tb"		// テキスト（特売コメント)
			,txt_genkaam_1kg	: "txt_genkaam_1kg"		// テキスト（1Kg原価)
			,txt_genkaam_ato	: "txt_genkaam_ato"		// テキスト（原価)
			,txt_genkaam_mae	: "txt_genkaam_mae"		// テキスト（原価)
			,txt_genkaam_pack	: "txt_genkaam_pack"	// テキスト（P原価)
			,txt_hbokureflg		: "txt_hbokureflg"		// テキスト（一日遅パタン)
			,txt_hbyoteisu		: "txt_hbyoteisu"		// テキスト（予定数)
			,txt_htasu			: "txt_htasu"			// テキスト（発注総数)
			,txt_irisu			: "txt_irisu"			// テキスト（入数)
			,txt_juhtdt			: "txt_juhtdt"			// テキスト（事前打出(日付))
			,txt_ko_a_baikaan	: "txt_ko_a_baikaan"	// テキスト（総売価A)
			,txt_ko_b_baikaan	: "txt_ko_b_baikaan"	// テキスト（総売価B)
			,txt_ko_c_baikaan	: "txt_ko_c_baikaan"	// テキスト（総売価C)
			,txt_parno			: "txt_parno"			// テキスト（グループNo.)
			,txt_popcd			: "txt_popcd"			// テキスト（POPコード)
			,txt_popsu			: "txt_popsu"			// テキスト（枚数)
			,txt_popsz			: "txt_popsz"			// テキスト（POPサイズ)
			,txt_ptnno			: "txt_ptnno"			// テキスト（パターンNo.)
			,txt_rankno_add		: "txt_rankno_add"		// テキスト（対象店)
			,txt_rankno_add_a	: "txt_rankno_add_a"	// テキスト（対象店)
			,txt_rankno_add_b	: "txt_rankno_add_b"	// テキスト（B売店)
			,txt_rankno_add_c	: "txt_rankno_add_c"	// テキスト（C売店)
			,txt_rankno_del		: "txt_rankno_del"		// テキスト（除外店)
			,txt_segn_1kosu		: "txt_segn_1kosu"		// テキスト（一人)
			,txt_segn_ninzu		: "txt_segn_ninzu"		// テキスト（先着人数)
			,txt_shncolor		: "txt_shncolor"		// テキスト（商品色)
			,txt_shnsize		: "txt_shnsize"			// テキスト（商品サイズ)
			,txt_tenkaikbn		: "txt_tenkaikbn"		// テキスト（展開区分)
			,txt_tenkaisu		: "txt_tenkaisu"		// テキスト（展開数)
			,txt_tenrank		: "txt_tenrank"			// テキスト（ランク（1～10）)
			,txt_tenrank_2		: "txt_tenrank_2"		// テキスト（ランク（1～10）)
			,txt_tpsu			: "txt_tpsu"			// テキスト（店舗数)
			,txt_tseikbn		: "txt_tseikbn"			// テキスト（訂正区分)
			,txt_wappnkbn		: "txt_wappnkbn"		// テキスト（ワッペン区分)
			,txt_tencd_add_1	: "txt_tencd_add_1"		// テキスト（対象店コード1)
			,txt_tencd_add_2	: "txt_tencd_add_2"		// テキスト（対象店コード2)
			,txt_tencd_add_3	: "txt_tencd_add_3"		// テキスト（対象店コード3)
			,txt_tencd_add_4	: "txt_tencd_add_4"		// テキスト（対象店コード4)
			,txt_tencd_add_5	: "txt_tencd_add_5"		// テキスト（対象店コード5)
			,txt_tencd_add_6	: "txt_tencd_add_6"		// テキスト（対象店コード6)
			,txt_tencd_add_7	: "txt_tencd_add_7"		// テキスト（対象店コード7)
			,txt_tencd_add_8	: "txt_tencd_add_8"		// テキスト（対象店コード8)
			,txt_tencd_add_9	: "txt_tencd_add_9"		// テキスト（対象店コード9)
			,txt_tencd_add_10	: "txt_tencd_add_10"	// テキスト（対象店コード10)
			,txt_tencd_del_1	: "txt_tencd_del_1"		// テキスト（除外店コード1)
			,txt_tencd_del_2	: "txt_tencd_del_2"		// テキスト（除外店コード2)
			,txt_tencd_del_3	: "txt_tencd_del_3"		// テキスト（除外店コード3)
			,txt_tencd_del_4	: "txt_tencd_del_4"		// テキスト（除外店コード4)
			,txt_tencd_del_5	: "txt_tencd_del_5"		// テキスト（除外店コード5)
			,txt_tencd_del_6	: "txt_tencd_del_6"		// テキスト（除外店コード6)
			,txt_tencd_del_7	: "txt_tencd_del_7"		// テキスト（除外店コード7)
			,txt_tencd_del_8	: "txt_tencd_del_8"		// テキスト（除外店コード8)
			,txt_tencd_del_9	: "txt_tencd_del_9"		// テキスト（除外店コード9)
			,txt_tencd_del_10	: "txt_tencd_del_10"	// テキスト（除外店コード10)

			,txt_standarddate		: "txt_standarddate"		// テキスト（コースマスタ 基準日）
			,txt_effectivestartdate	: "txt_effectivestartdate"	// テキスト（コースマスタ 有効開始日）
			,txt_effectiveenddate	: "txt_effectiveenddate"	// テキスト（コースマスタ 有効終了日）
			,txt_handleenddate		: "txt_handleenddate"		// テキスト（コースマスタ 取扱終了日）
			,txt_meishokn			: "txt_meishokn"			// テキスト（名称）
			,txt_dummycd			: "txt_dummycd"				// テキスト（ダミーコード）

			,txt_cptotenno		: "txt_cptotenno"		// テキスト（コピー先店番)
			,txt_cpfromtenno	: "txt_cpfromtenno"		// テキスト（コピー元店番)
			,txt_ranknost		: "txt_ranknost"		// テキスト（ランク№開始)
			,txt_ranknoed		: "txt_ranknoed"		// テキスト（ランク№終了)
			,txt_rtptnnost		: "txt_rtptnnost"		// テキスト（通常率ﾊﾟﾀｰﾝ№開始)
			,txt_rtptnnoed		: "txt_rtptnnoed"		// テキスト（通常率ﾊﾟﾀｰﾝ№終了)
			,txt_jrtptnnost		: "txt_jrtptnnost"		// テキスト（実績率ﾊﾟﾀｰﾝ№開始)
			,txt_jrtptnnoed		: "txt_jrtptnnoed"		// テキスト（実績率ﾊﾟﾀｰﾝ№終了)
			,txt_jrtptnnoed		: "txt_jrtptnnoed"		// テキスト（実績率ﾊﾟﾀｰﾝ№終了)
			,txt_rankno			: "txt_rankno"			// テキスト（ランク№)
			,txt_a_rankno		: "txt_a_rankno"		// テキスト（Aランク№)
			,txt_b_rankno		: "txt_b_rankno"		// テキスト（Bランク№)
			,txt_c_rankno		: "txt_c_rankno"		// テキスト（Cランク№)
			,txt_sryptnno		: "txt_sryptnno"		// テキスト（数量ﾊﾟﾀｰﾝ№)
			,txt_rtptnbmncd		: "txt_rtptnbmncd"		// テキスト（部門(通常率ﾊﾟﾀｰﾝ))
			,txt_rtptnno		: "txt_rtptnno"			// テキスト（通常率ﾊﾟﾀｰﾝ№)
			,txt_rtsousu		: "txt_rtsousu"			// テキスト（総数量(通常率))
			,txt_jrtptnbmncd	: "txt_jrtptnbmncd"		// テキスト（部門(実績率ﾊﾟﾀｰﾝ))
			,txt_jrtptnno		: "txt_jrtptnno"		// テキスト（実績率ﾊﾟﾀｰﾝ№)
			,txt_jrtsousu		: "txt_jrtsousu"		// テキスト（総数量(実績率))
			,txt_rankkn			: "txt_rankkn"			// テキスト（ランク名称)
			,txt_sryptnkn		: "txt_sryptnkn"		// テキスト（数量パターン名称)
			,txt_rtptnkn		: "txt_rtptnkn"			// テキスト（通常率パターン名称)
			,txt_jrtptnkn		: "txt_jrtptnkn"		// テキスト（実績率パターン名称)
			,txt_sousu			: "txt_sousu"			// テキスト（総数量)
			,txt_goukeisu		: "txt_goukeisu"		// テキスト（合計数)
			,txt_bunpairt		: "txt_bunpairt"		// テキスト（分配率)
			,txt_yymm			: "txt_yymm"			// テキスト（年月)
			,txt_yyww			: "txt_yyww"			// テキスト（年月(週No.))
			,txt_rank			: "txt_rank"			// テキスト（ランク)
			,txt_sankouhbj		: "txt_sankouhbj"		// テキスト（参考販売実績)
			,txt_tenrank_arr	: "txt_tenrank_arr"		// テキスト（店ランク配列)
			,txt_kanrino		: "txt_kanrino"			// テキスト（管理番号)
			,txt_kanrieno		: "txt_kanrieno"		// テキスト（管理番号枝番)
			,txt_suryo			: "txt_suryo"			// テキスト（数量)
			,txt_shnkbn			: "txt_shnkbn"			// テキスト（商品区分)
			,txt_kspage			: "txt_kspage"			// テキスト（構成ページ)
			,txt_inputno		: "txt_inputno"			// テキスト（入力番号)
			,txt_tenhtsu_arr	: "txt_tenhtsu_arr"		// テキスト（数量)
			,txt_jukbn			: "txt_jukbn"			// テキスト（区分)
			,txt_rankcd_add		: "txt_rankcd_add"		// テキスト（ランク)
			,txt_suryoptn		: "txt_suryoptn"		// テキスト（数量パターン)
			,txt_all_suryo_mon	: "txt_all_suryo_mon"	// テキスト（全店同一数量＿月）
			,txt_all_suryo_tue	: "txt_all_suryo_tue"	// テキスト（全店同一数量＿火）
			,txt_all_suryo_wed	: "txt_all_suryo_wed"	// テキスト（全店同一数量＿水）
			,txt_all_suryo_thu	: "txt_all_suryo_thu"	// テキスト（全店同一数量＿木）
			,txt_all_suryo_fri	: "txt_all_suryo_fri"	// テキスト（全店同一数量＿金）
			,txt_all_suryo_sat	: "txt_all_suryo_sat"	// テキスト（全店同一数量＿土）
			,txt_all_suryo_sun	: "txt_all_suryo_sun"	// テキスト（全店同一数量＿日）
			,txt_suryo_mon		: "txt_suryo_mon"		// テキスト（数量＿月）
			,txt_suryo_tue		: "txt_suryo_tue"		// テキスト（数量＿火）
			,txt_suryo_wed		: "txt_suryo_wed"		// テキスト（数量＿水）
			,txt_suryo_thu		: "txt_suryo_thu"		// テキスト（数量＿木）
			,txt_suryo_fri		: "txt_suryo_fri"		// テキスト（数量＿金）
			,txt_suryo_sat		: "txt_suryo_sat"		// テキスト（数量＿土）
			,txt_suryo_sun		: "txt_suryo_sun"		// テキスト（数量＿日）
			,txt_bmn_genkart	: "txt_bmn_genkart"		// テキスト（部門原価率）
			,txt_tenantcd		: "txt_tenantcd"		// テキスト（テナントコード）
			,txt_shukeicd		: "txt_shukeicd"		// テキスト（集計CD）
			,txt_bmn_atr1		: "txt_bmn_atr1"		// テキスト（部門属性1）
			,txt_bmn_atr2		: "txt_bmn_atr2"		// テキスト（部門属性2）
			,txt_bmn_atr3		: "txt_bmn_atr3"		// テキスト（部門属性3）
			,txt_bmn_atr4		: "txt_bmn_atr4"		// テキスト（部門属性4）
			,txt_bmn_atr5		: "txt_bmn_atr5"		// テキスト（部門属性5）
			,txt_bmnreceiptkn	: "txt_bmnreceiptkn"	// テキスト（部門レシート名称（漢字））
			,txt_bmnreceiptan	: "txt_bmnreceiptan"	// テキスト（部門レシート名称（カナ））
			,txt_groupno		: "txt_groupno"			// テキスト（グループ）
			,txt_bmnkn			: "txt_bmnkn"			// テキスト（部門（テナント）名称（漢字））
			,txt_btkn			: "txt_btkn"			// テキスト（分類割引名称)
			,txt_warirt			: "txt_warirt"			// テキスト（割引率)
			,txt_bmflg			: "txt_bmflg"			// テキスト（B/Mフラグ)

			,txt_qasmdt			: "txt_qasmdt"			// テキスト（店舗アンケート締切日)
			,txt_lstno			: "txt_lstno"			// テキスト（リスト№)

			,txt_avgptankaam	: "txt_avgptankaam"		// テキスト（平均パック単価)
			,txt_hs_avgptankaam	: "txt_hs_avgptankaam"	// テキスト（平均パック単価(販促))
			,txt_jancd1			: "txt_jancd1"			// テキスト（JANコード1)
			,txt_jancd2			: "txt_jancd2"			// テキスト（JANコード2)
			,txt_title			: "txt_title"			// テキスト（タイトル)
			,txt_tenten_arr		: "txt_tenten_arr"		// テキスト（点数配列)
			,txt_rankiinput		: "txt_rankiinput"		// テキスト（ランク(店番一括入力))
			,txt_tencdiinput	: "txt_tencdiinput"		// テキスト（店番(店番一括入力))

			,txt_szkcd			: "txt_szkcd"			// テキスト（所属コード）

			,txt_mbansflg		: "txt_mbansflg"		// テキスト（各店回答フラグ)
			,txt_tenten_arr		: "txt_tenten_arr"		// テキスト（点数配列)
			,txt_rankiinput		: "txt_rankiinput"		// テキスト（ランク(店番一括入力))
			,txt_tencdiinput	: "txt_tencdiinput"		// テキスト（店番(店番一括入力))
			,txt_urichgam1		: "txt_urichgam1"		// テキスト（売価差替1）
			,txt_urichgam2		: "txt_urichgam2"		// テキスト（売価差替2）
			,txt_urichgam3		: "txt_urichgam3"		// テキスト（売価差替3）
			,txt_tennensu		: "txt_tennensu"		// テキスト（店舗年齢）
			,txt_nissyou		: "txt_nissyou"			// テキスト（日商）
			,txt_uriagezenhi	: "txt_uriagezenhi"		// テキスト（売上前比）
			,txt_arariritu		: "txt_arariritu"		// テキスト（荒利率）
			,txt_reisetu		: "txt_reisetu"			// テキスト（冷設）
			,txt_souzai			: "txt_souzai"			// テキスト（惣菜）
			,txt_gondora		: "txt_gondora"			// テキスト（ゴンドラ）
			,txt_aed			: "txt_aed"				// テキスト（AED）
			,txt_minsu			: "txt_minsu"			// テキスト（最低発注数)
			,txt_promo_bgm_tm	: "txt_promo_bgm_tm"	// テキスト（タイムサービス_開始時間）
			,txt_promo_end_tm	: "txt_promo_end_tm"	// テキスト（タイムサービス_終了時間）
			,txt_stmn			: "txt_stmn"			// テキスト（セット名称）
			,txt_estgk			: "txt_estgk"			// テキスト（成立価格）
			,txt_stno2			: "txt_stno2"			// テキスト（セット番号）

			// ユーザー履歴・アイテム履歴
			,txt_fromdate	: "txt_fromdate"		// テキスト（期間（開始日））
			,txt_todate		: "txt_todate"			// テキスト（期間（終了日））
			,txt_shohinkn		: "txt_shohinkn"			// テキスト（商品名（漢字））

		},

		// 名称マスタより作成するコンボボックス用のID
		id_mei: {
			 kbn101	:"sel_teishikbn"								// 選択リスト(取引停止)
			,kbn102	:"sel_pckbn"									// 選択リスト(PC区分)
			,kbn103	:"sel_kakokbn"									// 選択リスト(加工区分)
			,kbn104	:"sel_ichibakbn"								// 選択リスト(市場区分)
			,kbn105	:"sel_shnkbn"									// 選択リスト(商品種類)
			,kbn106	:"sel_rg_atsukflg"								// 選択リスト(レギュラー情報_取扱フラグ)
			,kbn107	:"sel_rg_idenflg"								// 選択リスト(レギュラー情報_一括伝票フラグ)
			,kbn108	:"sel_rg_wapnflg"								// 選択リスト(レギュラー情報_ワッペン)
			,kbn109	:"sel_hs_atsukflg"								// 選択リスト(販促情報_取扱フラグ)
			,kbn110	:"sel_hs_wapnflg"								// 選択リスト(販促情報_ワッペン)
			,kbn111	:"sel_hp_swapnflg"								// 選択リスト(販促情報_特売ワッペン)
			,kbn112	:"sel_kikaku_tani"	// 選択リスト(規格_単位)	TODO：IDを列名準拠の命名に修正
			,kbn113	:"sel_up_tanikbn"								// 選択リスト(ユニットプライス_ユニット単位)
			,kbn114	:"sel_pbkbn"									// 選択リスト(PB区分)
			,kbn115	:"sel_komonokbm"								// 選択リスト(小物区分)
			,kbn116	:"sel_tanaorokbn"								// 選択リスト(棚卸区分)
			,kbn117	:"sel_teikeikbn"								// 選択リスト(定計区分)
			,kbn118	:"sel_pcard_shukbn"								// 選択リスト(プライスカード_種類)
			,kbn119	:"sel_pcard_irokbn"								// 選択リスト(プライスカード_色)
			,kbn120	:"sel_zeikbn"									// 選択リスト(税区分_商品)
			,kbn121	:"sel_teikankbn"								// 選択リスト(定貫不定貫区分)
			,kbn122	:"sel_importkbn"								// 選択リスト(輸入区分)
			,kbn123	:"sel_henpinkbn"								// 選択リスト(返品区分)
			,kbn124	:"sel_flgjoho_elp"	// 選択リスト(フラグ情報_ELP)	TODO：IDを列名準拠の命名に修正
			,kbn125	:"sel_flgjoho_berumark"	// 選択リスト(フラグ情報_ベルマーク)	TODO：IDを列名準拠の命名に修正
			,kbn126	:"sel_flgjoho_risaikuru"	// 選択リスト(フラグ情報_リサイクル)	TODO：IDを列名準拠の命名に修正
			,kbn127	:"sel_flgjoho_ekomark"	// 選択リスト(フラグ情報_エコマーク)	TODO：IDを列名準拠の命名に修正
			,kbn128	:"sel_kikan"	// 選択リスト(期間)	TODO：IDを列名準拠の命名に修正
			,kbn129	:"sel_shukyukbn"								// 選択リスト(酒級)
			,kbn130	:"sel_urabarikbn"								// 選択リスト(裏貼)
			,kbn131	:"sel_pricecardshutsuryokumu"	// 選択リスト(プライスカ−ド出力有無)	TODO：IDを列名準拠の命名に修正
			,kbn132	:"sel_binkbn"									// 選択リスト(便区分)
			,kbn133	:"sel_hachuyobi"	// 選択リスト(発注曜日)	TODO：IDを列名準拠の命名に修正
			,kbn134	:"sel_simekaisu"								// 選択リスト(締め回数)
			,kbn135	:"sel_ariakbn"	// 選択リスト(エリア区分)	TODO：IDを列名準拠の命名に修正
			,kbn136	:"sel_sourcekbn"	// 選択リスト(ソ−ス区分)	TODO：IDを列名準拠の命名に修正
			,kbn137	:"sel_tenkabutsukbn"	// 選択リスト(添加物区分)	TODO：IDを列名準拠の命名に修正
			,kbn138	:"sel_tenkabcd"									// 選択リスト(添加物コード)
			,kbn139	:"sel_atsukkbn"									// 選択リスト(扱い区分)
			,kbn140	:"sel_gpkbn"									// 選択リスト(グル−プ区分)
			,kbn141	:"sel_kikaku_kosutani"	// 選択リスト(規格_個数単位)	TODO：IDを列名準拠の命名に修正
			,kbn142	:"sel_iryoreflg"								// 選択リスト(衣料使い回しフラグ)
			,kbn143	:"sel_ketashitei"	// 選択リスト(桁指定)	TODO：IDを列名準拠の命名に修正
			,kbn144	:"sel_pricecard_shurui2"	// 選択リスト(プライスカード_種類)	TODO：IDを列名準拠の命名に修正
			,kbn145	:"sel_pricecard_iro2"	// 選択リスト(プライスカード_色)	TODO：IDを列名準拠の命名に修正
			,kbn146	:"sel_allergycd"	// 選択リスト(アレルギーコード)	TODO：IDを列名準拠の命名に修正
			,kbn301	:"sel_seikakibokbn"								// 選択リスト（青果センターエリア)
			,kbn302	:"sel_sengyokbn"								// 選択リスト（鮮魚区分)
			,kbn303	:"sel_seinikukbn"								// 選択リスト（精肉区分)
			,kbn304	:"sel_sc_sflg"									// 選択リスト（S/C送信フラグ)
			,kbn305	:"sel_kosei_sflg"								// 選択リスト（構成マスタ送信フラグ)
			,kbn306	:"sel_bunrui_sflg"								// 選択リスト（分類マスタ送信フラグ)
			,kbn307	:"sel_bmn_sflg"									// 選択リスト（部門マスタ送信フラグ)
			,kbn308	:"sel_yobi1_sflg"								// 選択リスト（予備1送信フラグ)
			,kbn309	:"sel_yobi2_sflg"								// 選択リスト（予備2送信フラグ)
			,kbn310	:"sel_yosankbn"									// 選択リスト（予算区分)
			,kbn311	:"sel_elevtrflg"								// 選択リスト（エレベータ)
			,kbn312	:"sel_escaltrflg"								// 選択リスト（エスカレータ)
			,kbn313	:"sel_miseunyokbn"								// 選択リスト（店運用区分)
			,kbn314	:"sel_seikacd"									// 選択リスト（青果市場コード)
			,kbn315	:"sel_yobikbn1"	// 選択リスト(曜日区分)	TODO：IDを列名準拠の命名に修正
			,kbn316	:"sel_tenkyuflg"	// 選択リスト(店休フラグ)	TODO：IDを列名準拠の命名に修正
			,kbn317	:"sel_miokbn"	// 選択リスト(ＭＩＯ区分)	TODO：IDを列名準拠の命名に修正
			,kbn318	:"sel_waribikikbn"	// 選択リスト(割引区分)	TODO：IDを列名準拠の命名に修正
			,kbn319	:"sel_jishatenant"	// 選択リスト(自社テナント)	TODO：IDを列名準拠の命名に修正
			,kbn320	:"sel_losbunsekitaisho"	// 選択リスト(ロス分析対象)	TODO：IDを列名準拠の命名に修正
			,kbn321	:"sel_yosankbn_bumon"	// 選択リスト(予算区分_部門)	TODO：IDを列名準拠の命名に修正
			,kbn322	:"sel_tanaoroshitaishokbn"	// 選択リスト(棚卸対象区分)	TODO：IDを列名準拠の命名に修正
			,kbn323	:"sel_uriageflg"	// 選択リスト(売上フラグ)	TODO：IDを列名準拠の命名に修正
			,kbn324	:"sel_hanbaibucd"								// 選択リスト（販売部)
			,kbn325	:"sel_chikucd"									// 選択リスト（地区)
			,kbn330	:"sel_depokbn"									// 選択リスト（デポ区分)
			,kbn331	:"sel_ingfan"									// 選択リスト（ingfanカード)
			,KBN332	:"sel_water"									// 選択リスト（ピュアウォーター)
			,kbn333	:"sel_atm"										// 選択リスト（ATM)
			,kbn334	:"sel_rezi"										// 選択リスト（お客様お会計レジ)
			,kbn335	:"sel_dryice"									// 選択リスト（ドライアイス)
			,kbn336	:"sel_photo"									// 選択リスト（証明写真)
			,kbn337	:"sel_dpe"										// 選択リスト（DPE)
			,kbn338	:"sel_otodokeservice"							// 選択リスト（お届けサービス)
			,kbn339	:"sel_densimoney"								// 選択リスト（電子マネー)
			,kbn340	:"sel_petgenyouki"								// 選択リスト（ペット減容器)
			,kbn341	:"sel_kuturogispace"							// 選択リスト（くつろぎスペース)
			,kbn401	:"sel_edijushin"	// 選択リスト(ＥＤＩ受信)	TODO：IDを列名準拠の命名に修正
			,kbn402	:"sel_edisoshin"	// 選択リスト(ＥＤＩ送信)	TODO：IDを列名準拠の命名に修正
			,kbn403	:"sel_shiiresakiyoto"	// 選択リスト(仕入先用途)	TODO：IDを列名準拠の命名に修正
			,kbn404	:"sel_inageyazaiko"	// 選択リスト(いなげや在庫)	TODO：IDを列名準拠の命名に修正
			,kbn405	:"sel_kaikakekbn"	// 選択リスト(買掛区分)	TODO：IDを列名準拠の命名に修正
			,kbn406	:"sel_keisancenter"	// 選択リスト(計算センター)	TODO：IDを列名準拠の命名に修正
			,kbn407	:"sel_unyokbn"	// 選択リスト(運用区分)	TODO：IDを列名準拠の命名に修正
			,kbn408	:"sel_denpyokbn"	// 選択リスト(伝票区分)	TODO：IDを列名準拠の命名に修正
			,kbn409	:"sel_shukeihyo1"	// 選択リスト(集計表)	TODO：IDを列名準拠の命名に修正
			,kbn410	:"sel_pickingdata"	// 選択リスト(ピッキングデータ)	TODO：IDを列名準拠の命名に修正
			,kbn411	:"sel_pickinglist"	// 選択リスト(ピッキングリスト)	TODO：IDを列名準拠の命名に修正
			,kbn412	:"sel_wappen"	// 選択リスト(ワッペン)	TODO：IDを列名準拠の命名に修正
			,kbn413	:"sel_ikkatsudenpyo"	// 選択リスト(一括伝票)	TODO：IDを列名準拠の命名に修正
			,kbn414	:"sel_kakoshiji"	// 選択リスト(加工指示)	TODO：IDを列名準拠の命名に修正
			,kbn415	:"sel_ryutsukbn"	// 選択リスト(流通区分)	TODO：IDを列名準拠の命名に修正
			,kbn416	:"sel_zaikochiwake_denpyokbn"	// 選択リスト(在庫内訳_伝票区分)	TODO：IDを列名準拠の命名に修正
			,kbn417	:"sel_zaikochiwake_shukeihyo"	// 選択リスト(在庫内訳_集計表)	TODO：IDを列名準拠の命名に修正
			,kbn418	:"sel_zaikochiwake_pickingdata"	// 選択リスト(在庫内訳_ピッキングデータ)	TODO：IDを列名準拠の命名に修正
			,kbn419	:"sel_zaikochiwake_pickinglist"	// 選択リスト(在庫内訳_ピッキングリスト)	TODO：IDを列名準拠の命名に修正
			,kbn420	:"sel_dohohaishinsaki_denpyokbn"	// 選択リスト(同報配信先_伝票区分)	TODO：IDを列名準拠の命名に修正
			,kbn421	:"sel_dohohaishinsaki_shukeihyo"	// 選択リスト(同報配信先_集計表)	TODO：IDを列名準拠の命名に修正
			,kbn422	:"sel_yokomochisaki_kenshukbn"	// 選択リスト(横持先_検収区分)	TODO：IDを列名準拠の命名に修正
			,kbn423	:"sel_yokomochisaki_denpyokbn"	// 選択リスト(横持先_伝票区分)	TODO：IDを列名準拠の命名に修正
			,kbn424	:"sel_yokomochisaki_shukeihyo"	// 選択リスト(横持先_集計表)	TODO：IDを列名準拠の命名に修正
			,kbn425	:"sel_tembetsudenpyoflg"	// 選択リスト(店別伝票フラグ)	TODO：IDを列名準拠の命名に修正
			,kbn426	:"sel_torihikiteishiflg"	// 選択リスト(取引停止フラグ)	TODO：IDを列名準拠の命名に修正
			,kbn427	:"sel_def_ikkatsukbn"	// 選択リスト(デフォルト_一括区分)	TODO：IDを列名準拠の命名に修正
			,kbn428	:"sel_bmskbn"	// 選択リスト(BMS対象区分)	TODO：IDを列名準拠の命名に修正
			,kbn429	:"sel_autokbn"	// 選択リスト(自動検収区分)	TODO：IDを列名準拠の命名に修正
			,kbn430	:"sel_kakokbn2"	// 選択リスト(生鮮・加工食品区分)	TODO：IDを列名準拠の命名に修正
			,kbn501	:"sel_bumonkbn"	// 選択リスト(部門区分)	TODO：IDを列名準拠の命名に修正
			,kbn502	:"sel_zeikbn_bumon"	// 選択リスト(税区分_部門)	TODO：IDを列名準拠の命名に修正
			,kbn503	:"sel_orderbookshutsuryokukbn"	// 選択リスト(オーダーブック出力区分)	TODO：IDを列名準拠の命名に修正
			,kbn504	:"sel_hyokakbn"									// 選択リスト(評価方法区分)
			,kbn505	:"sel_tanaoroshitiming"	// 選択リスト(棚卸タイミング)	TODO：IDを列名準拠の命名に修正
			,kbn506	:"sel_posbaihentaishokbn"	// 選択リスト(ＰＯＳ売変対象区分)	TODO：IDを列名準拠の命名に修正
			,kbn507	:"sel_keihitaishokbn"	// 選択リスト(経費対象区分)	TODO：IDを列名準拠の命名に修正
			,kbn508	:"sel_tanpinkanrikbn"	// 選択リスト(単品管理区分)	TODO：IDを列名準拠の命名に修正
			,kbn509	:"sel_nebikijogaiflg"	// 選択リスト(値引除外フラグ)	TODO：IDを列名準拠の命名に修正
			,kbn510	:"sel_shohin"	// 選択リスト(商品/非商品)	TODO：IDを列名準拠の命名に修正
			,kbn511	:"sel_hanbaiseigenflg"	// 選択リスト(販売制限フラグ)	TODO：IDを列名準拠の命名に修正
			,kbn601	:"sel_kakobiinji"	// 選択リスト(加工日印字)	TODO：IDを列名準拠の命名に修正
			,kbn602	:"sel_kakojiinji"	// 選択リスト(加工時印字)	TODO：IDを列名準拠の命名に修正
			,kbn603	:"sel_kakoji_sentaku"	// 選択リスト(加工時選択)	TODO：IDを列名準拠の命名に修正
			,kbn604	:"sel_shohibiinji"	// 選択リスト(消費日印字)	TODO：IDを列名準拠の命名に修正
			,kbn605	:"sel_shohijiinji"	// 選択リスト(消費時印字)	TODO：IDを列名準拠の命名に修正
			,kbn606	:"sel_omotehari_sentaku"	// 選択リスト(表貼選択)	TODO：IDを列名準拠の命名に修正
			,kbn607	:"sel_uraharihakko_sentaku"	// 選択リスト(裏貼発行選択)	TODO：IDを列名準拠の命名に修正
			,kbn608	:"sel_urahari_sentaku"	// 選択リスト(裏貼選択)	TODO：IDを列名準拠の命名に修正
			,kbn609	:"sel_eyecatchlabelhakko"	// 選択リスト(アイキャッチラベル発行)	TODO：IDを列名準拠の命名に修正
			,kbn610	:"sel_fukulabelhakko"	// 選択リスト(副ラベル発行)	TODO：IDを列名準拠の命名に修正
			,kbn611	:"sel_hyobar_inji"	// 選択リスト(表バー印字)	TODO：IDを列名準拠の命名に修正
			,kbn612	:"sel_hakkomode"	// 選択リスト(発行モード)	TODO：IDを列名準拠の命名に修正
			,kbn613	:"sel_jidokenchi"	// 選択リスト(自動検知)	TODO：IDを列名準拠の命名に修正
			,kbn614	:"sel_jissekishushu"	// 選択リスト(実績収集)	TODO：IDを列名準拠の命名に修正
			,kbn615	:"sel_hososokudo"	// 選択リスト(包装速度)	TODO：IDを列名準拠の命名に修正
			,kbn616	:"sel_cdtaikei"	// 選択リスト(コード体系)	TODO：IDを列名準拠の命名に修正
			,kbn617	:"sel_futai"	// 選択リスト(風袋)	TODO：IDを列名準拠の命名に修正
			,kbn701	:"sel_pricecardhakkosize"	// 選択リスト(プライスカード発行サイズ)	TODO：IDを列名準拠の命名に修正
			,kbn702	:"sel_maisushiteihoho"	// 選択リスト(枚数指定方法)	TODO：IDを列名準拠の命名に修正
			,kbn703	:"sel_sakuseibaikakbn"	// 選択リスト(作成売価区分)	TODO：IDを列名準拠の命名に修正
			,kbn801	:"sel_kengenkbn_mst"	// 選択リスト(権限区分_マスタ)	TODO：IDを列名準拠の命名に修正
			,kbn802	:"sel_kengenkbn_hatchutokubai"	// 選択リスト(権限区分_発注特売)	TODO：IDを列名準拠の命名に修正
			,kbn803	:"sel_kanrishaflg"	// 選択リスト(管理者フラグ)	TODO：IDを列名準拠の命名に修正
			,kbn10001	:"sel_tshuflg"								// 選択リスト(特別週フラグ)
			,kbn10002	:"sel_moyskbn"								// 選択リスト(催し区分)
			,kbn10003	:"sel_nenmatkbn"							// 選択リスト(年末区分)
			,kbn10004	:"sel_okureslide_hanbai"	// 選択リスト(1遅れスライド_販売)	TODO：IDを列名準拠の命名に修正
			,kbn10005	:"sel_okureslide_nonyu"	// 選択リスト(1遅れスライド_納入)	TODO：IDを列名準拠の命名に修正
			,kbn10006	:"sel_kenshokukuri"	// 選択リスト(検証の括り)	TODO：IDを列名準拠の命名に修正
			,kbn10007	:"sel_def_sutenkai"	// 選択リスト(デフォルト_数展開)	TODO：IDを列名準拠の命名に修正
			,kbn10008	:"sel_def_jissekiritsupatansuchi"	// 選択リスト(デフォルト_実績率パタン数値)	TODO：IDを列名準拠の命名に修正
			,kbn10009	:"sel_def_zennendoshu"	// 選択リスト(デフォルト_前年同週)	TODO：IDを列名準拠の命名に修正
			,kbn10010	:"sel_def_donendoshu"	// 選択リスト(デフォルト_同年同週)	TODO：IDを列名準拠の命名に修正
			,kbn10011	:"sel_def_cuttentenkai"	// 選択リスト(デフォルト_カット店展開)	TODO：IDを列名準拠の命名に修正
			,kbn10012	:"sel_bmflg"	// 選択リスト(B/Mフラグ)	TODO：IDを列名準拠の命名に修正
			,kbn10013	:"sel_def_bumonzokusei"	// 選択リスト(デフォルト_部門属性)	TODO：IDを列名準拠の命名に修正
			,kbn10101	:"sel_shutsukiflg"	// 選択リスト(週月フラグ)	TODO：IDを列名準拠の命名に修正
			,kbn10201	:"sel_taishojogaiflg1"	// 選択リスト(対象除外フラグ)	TODO：IDを列名準拠の命名に修正
			,kbn10202	:"sel_bm_typ"	// 選択リスト(B/Mタイプ)	TODO：IDを列名準拠の命名に修正
			,kbn10302	:"sel_waribikiritsukbn1"	// 選択リスト(割引率区分)	TODO：IDを列名準拠の命名に修正
			,kbn10303	:"sel_seiki_cutkbn"	// 選択リスト(正規・カット区分)	TODO：IDを列名準拠の命名に修正
			,kbn10304	:"sel_taishojogaiflg2"	// 選択リスト(対象除外フラグ)	TODO：IDを列名準拠の命名に修正
			,kbn10305	:"sel_shohinkbn1"	// 選択リスト(商品区分)	TODO：IDを列名準拠の命名に修正
			,kbn10306	:"sel_teiseikbn1"	// 選択リスト(訂正区分)	TODO：IDを列名準拠の命名に修正
			,kbn10307	:"sel_jizenkbn1"	// 選択リスト(事前区分)	TODO：IDを列名準拠の命名に修正
			,kbn10308	:"sel_wappenkbn1"	// 選択リスト(ワッペン区分)	TODO：IDを列名準拠の命名に修正
			,kbn10309	:"sel_tenkaihoho1"	// 選択リスト(展開方法)	TODO：IDを列名準拠の命名に修正
			,kbn10310	:"sel_shohinkbn2"	// 選択リスト(商品区分)	TODO：IDを列名準拠の命名に修正
			,kbn10311	:"sel_teiseikbn2"	// 選択リスト(訂正区分)	TODO：IDを列名準拠の命名に修正
			,kbn10312	:"sel_jizenkbn2"	// 選択リスト(事前区分)	TODO：IDを列名準拠の命名に修正
			,kbn10313	:"sel_wappenkbn2"	// 選択リスト(ワッペン区分)	TODO：IDを列名準拠の命名に修正
			,kbn10314	:"sel_tenkaihoho2"	// 選択リスト(展開方法)	TODO：IDを列名準拠の命名に修正
			,kbn10401	:"sel_jisshihoho"	// 選択リスト(実施方法)	TODO：IDを列名準拠の命名に修正
			,kbn10402	:"sel_size"	// 選択リスト(サイズ)	TODO：IDを列名準拠の命名に修正
			,kbn10403	:"sel_yoshimuki"	// 選択リスト(用紙向き)	TODO：IDを列名準拠の命名に修正
			,kbn10404	:"sel_chizuumu"	// 選択リスト(地図有無)	TODO：IDを列名準拠の命名に修正
			,kbn10405	:"sel_boshu_fp"	// 選択リスト(募集_FP)	TODO：IDを列名準拠の命名に修正
			,kbn10406	:"sel_shoninkbn1"	// 選択リスト(承認区分)	TODO：IDを列名準拠の命名に修正
			,kbn10407	:"sel_leadertenflg"	// 選択リスト(リーダー店フラグ)	TODO：IDを列名準拠の命名に修正
			,kbn10408	:"sel_yoridoriflg1"	// 選択リスト(よりどりフラグ)	TODO：IDを列名準拠の命名に修正
			,kbn10409	:"sel_hatchuhoho"	// 選択リスト(発注方法)	TODO：IDを列名準拠の命名に修正
			,kbn10410	:"sel_waribikiritsukbn2"	// 選択リスト(割引率区分)	TODO：IDを列名準拠の命名に修正
			,kbn10411	:"sel_seishokukanetsukbn1"	// 選択リスト(生食加熱区分)	TODO：IDを列名準拠の命名に修正
			,kbn10412	:"sel_kaitoflg1"	// 選択リスト(解凍フラグ)	TODO：IDを列名準拠の命名に修正
			,kbn10413	:"sel_yoshokuflg1"	// 選択リスト(養殖フラグ)	TODO：IDを列名準拠の命名に修正
			,kbn10414	:"sel_tkanplukbn"								// 選択リスト(定貫PLU・不定貫区分)
			,kbn10416	:"sel_jisshihoho_instore"	// 選択リスト(実施方法_インストア)	TODO：IDを列名準拠の命名に修正
			,kbn10417	:"sel_jisshihoho_centerpack"	// 選択リスト(実施方法_センターパック)	TODO：IDを列名準拠の命名に修正
			,kbn10418	:"sel_shoninkbn2"	// 選択リスト(承認区分)	TODO：IDを列名準拠の命名に修正
			,kbn10419	:"sel_boshu_np"	// 選択リスト(募集_NP)	TODO：IDを列名準拠の命名に修正
			,kbn10420	:"sel_boshu_yp"	// 選択リスト(募集_YP)	TODO：IDを列名準拠の命名に修正
			,kbn10421	:"sel_teiseikbn3"	// 選択リスト(訂正区分)	TODO：IDを列名準拠の命名に修正
			,kbn10501	:"sel_teiseikbn4"	// 選択リスト(訂正区分)	TODO：IDを列名準拠の命名に修正
			,kbn10502	:"sel_shohinkbn"	// 選択リスト(商品区分)	TODO：IDを列名準拠の命名に修正
			,kbn10503	:"sel_yobikbn2"	// 選択リスト(曜日区分)	TODO：IDを列名準拠の命名に修正
			,kbn10601	:"sel_hanbaibi1nichichikyokaflg"	// 選択リスト(販売日1日遅許可フラグ)	TODO：IDを列名準拠の命名に修正
			,kbn10602	:"sel_tsukijimeflg"	// 選択リスト(月締フラグ)	TODO：IDを列名準拠の命名に修正
			,kbn10603	:"sel_honbucontrolflg"	// 選択リスト(本部コントロールフラグ)	TODO：IDを列名準拠の命名に修正
			,kbn10604	:"sel_tenfusaiyokinshiflg"	// 選択リスト(店不採用禁止フラグ)	TODO：IDを列名準拠の命名に修正
			,kbn10605	:"sel_tenbaika_sentakukinshiflg"	// 選択リスト(店売価選択禁止フラグ)	TODO：IDを列名準拠の命名に修正
			,kbn10606	:"sel_tenshohin_sentakukinshiflg"	// 選択リスト(店商品選択禁止フラグ)	TODO：IDを列名準拠の命名に修正
			,kbn10607	:"sel_karishimeflg_leaderten"	// 選択リスト(仮締フラグ_リーダー店)	TODO：IDを列名準拠の命名に修正
			,kbn10608	:"sel_honshimeflg_leaderten"	// 選択リスト(本締フラグ_リーダー店)	TODO：IDを列名準拠の命名に修正
			,kbn10609	:"sel_honshimeflg_kakuten"	// 選択リスト(本締フラグ_各店)	TODO：IDを列名準拠の命名に修正
			,kbn10610	:"sel_kyoseiflg"								// 選択リスト(強制グループフラグ)
			,kbn10611	:"sel_qasyukbn"									// 選択リスト(アンケート種類)
			,kbn10612	:"sel_leadertenkbn"	// 選択リスト(リーダー店区分)	TODO：IDを列名準拠の命名に修正
			,kbn10613	:"sel_leadertensaiyoflg"	// 選択リスト(リーダー店採用フラグ)	TODO：IDを列名準拠の命名に修正
			,kbn10614	:"sel_baikaikkatsu_sentaku1"	// 選択リスト(売価一括選択)	TODO：IDを列名準拠の命名に修正
			,kbn10615	:"sel_baikaikkatsu_sentaku2"	// 選択リスト(売価一括選択)	TODO：IDを列名準拠の命名に修正
			,kbn10616	:"sel_schedule_kaitoflg"	// 選択リスト(スケジュール回答フラグ)	TODO：IDを列名準拠の命名に修正
			,kbn10617	:"sel_item_kaitoflg"	// 選択リスト(アイテム回答フラグ)	TODO：IDを列名準拠の命名に修正
			,kbn10618	:"sel_kakutensaiyoflg"	// 選択リスト(各店採用フラグ)	TODO：IDを列名準拠の命名に修正
			,kbn10619	:"sel_hanbaikaishiflg"	// 選択リスト(販売開始フラグ)	TODO：IDを列名準拠の命名に修正
			,kbn10620	:"sel_kakuten_kaitoflg"	// 選択リスト(各店回答フラグ)	TODO：IDを列名準拠の命名に修正
			,kbn10621	:"sel_baika_sentaku"	// 選択リスト(売価選択)	TODO：IDを列名準拠の命名に修正
			,kbn10622	:"sel_shohin_sentaku"	// 選択リスト(商品選択)	TODO：IDを列名準拠の命名に修正
			,kbn10651	:"sel_nichiokureslide_hanbai"	// 選択リスト(1日遅スライド_販売)	TODO：IDを列名準拠の命名に修正
			,kbn10652	:"sel_nichiokureslide_nonyu"	// 選択リスト(1日遅スライド_納入)	TODO：IDを列名準拠の命名に修正
			,kbn10653	:"sel_hitaiflg"	// 選択リスト(日替フラグ)	TODO：IDを列名準拠の命名に修正
			,kbn10654	:"sel_chirashimikeisai"	// 選択リスト(チラシ未掲載)	TODO：IDを列名準拠の命名に修正
			,kbn10655	:"sel_hatchuharabaikatekiyoflg"	// 選択リスト(発注原売価適用フラグ)	TODO：IDを列名準拠の命名に修正
			,kbn10656	:"sel_writukbn"									// 選択リスト(割引率区分)
			,kbn10657	:"sel_teikanplu_futeikankbn2"	// 選択リスト(定貫PLU・不定貫区分)	TODO：IDを列名準拠の命名に修正
			,kbn10658	:"sel_pluhaishinflg"	// 選択リスト(PLU配信フラグ)	TODO：IDを列名準拠の命名に修正
			,kbn10659	:"sel_yoridoriflg2"	// 選択リスト(よりどりフラグ)	TODO：IDを列名準拠の命名に修正
			,kbn10660	:"sel_medamakbn"								// 選択リスト(目玉区分)
			,kbn10661	:"sel_seishokukanetsukbn2"	// 選択リスト(生食加熱区分)	TODO：IDを列名準拠の命名に修正
			,kbn10662	:"sel_kaitoflg2"	// 選択リスト(解凍フラグ)	TODO：IDを列名準拠の命名に修正
			,kbn10663	:"sel_yoshokuflg2"	// 選択リスト(養殖フラグ)	TODO：IDを列名準拠の命名に修正
			,kbn10664	:"sel_cuttentenkaiflg"	// 選択リスト(カット店展開フラグ)	TODO：IDを列名準拠の命名に修正
			,kbn10665	:"sel_binkbn"									// 選択リスト(便区分)
			,kbn10666	:"sel_wappnkbn"									// 選択リスト(ワッペン区分)
			,kbn10668	:"sel_shutsugishiiresakidensoflg"	// 選択リスト(週次仕入先伝送フラグ)	TODO：IDを列名準拠の命名に修正
			,kbn10669	:"sel_teiseikbn5"	// 選択リスト(訂正区分)	TODO：IDを列名準拠の命名に修正
			,kbn10670	:"sel_segn_gentei"								// 選択リスト(制限ー限定表現)
			,kbn10671	:"sel_segn_1kosutni"							// 選択リスト(制限ー単位)
			,kbn10672	:"sel_tsukishimehenkoriyu"	// 選択リスト(月締変更理由)	TODO：IDを列名準拠の命名に修正
			,kbn10803	:"sel_nohinkeitai"	// 選択リスト(納品形態)	TODO：IDを列名準拠の命名に修正
			,kbn10804	:"sel_teiseikbn6"	// 選択リスト(訂正区分)	TODO：IDを列名準拠の命名に修正
			,kbn10805	:"sel_moyooshikbn"	// 選択リスト(催し区分)	TODO：IDを列名準拠の命名に修正
			,kbn30001	:"sel_it024_torokukoshincsv_torikomikensujogen"	// 選択リスト(画面ＩＴ０２４：登録更新ＣＳＶ取込件数上限)	TODO：IDを列名準拠の命名に修正
			,kbn910005  :"sel_center"	//選択リスト(センター)
			,kbn910006	:"sel_supplyno"	//選択リスト(便)
			,kbn910009	:"sel_errordiv"		// 選択リスト（エラー区分)

			,kbn105012	:"sel_teiseikbn7"	// 選択リスト(訂正区分)	TODO：IDを列名準拠の命名に修正
			,kbn105013	:"sel_teiseikbn8"	// 選択リスト(訂正区分)	TODO：IDを列名準拠の命名に修正
			,kbn105014	:"sel_teiseikbn9"	// 選択リスト(訂正区分)	TODO：IDを列名準拠の命名に修正
			,kbn105015	:"sel_teiseikbn10"	// 選択リスト(訂正区分)	TODO：IDを列名準拠の命名に修正
			,kbn105016	:"sel_teiseikbn11"	// 選択リスト(訂正区分)	TODO：IDを列名準拠の命名に修正
			,kbn105017	:"sel_teiseikbn12"	// 選択リスト(訂正区分)	TODO：IDを列名準拠の命名に修正

			// イートン追加
			,kbn151		:"sel_k_honkb"				// 選択リスト(保温区分)
			,kbn152		:"sel_k_wapnflg"			// 選択リスト(デリカワッペン区分_レギュラ/販促)
			,kbn153		:"sel_k_torikb"				// 選択リスト(取扱区分)
		},

		// Web商談 状態
		SEL_DATA : {
			KENMEI_STATE_DATA : [{'VALUE': "", 'TEXT': ""}, {'VALUE': 1, 'TEXT': "作成中"}, {'VALUE': 2, 'TEXT': "確定"}, {'VALUE': 3, 'TEXT': "仕掛"}, {'VALUE': 4, 'TEXT': "完了"}],
			TEIAN_SHN_STATE_DATA: [{'VALUE': 1, 'TEXT': "作成中"}, {'VALUE': 2, 'TEXT': "確定"}],
		},

		message : {
			 ID_MESSAGE_WARNING_EXCEL_OUTPUT		:	"照会後のみExcelの表示は可能です。"
			,ID_MESSAGE_WARNING_REPORT_OUTPUT		:	"照会後に実行してください。"
			,ID_MESSAGE_WARNING_SELECT_CATEGORY		:	"商品カテゴリを指定してください。"
			,ID_MESSAGE_VALIDATION_SELECT_ITEM		:	"商品を選択してください。"
			,ID_MESSAGE_VALIDATION_SELECT_ITEM_EX	:	"横軸に「商品」を表示する場合、「小分類」まで指定してください。"
			,ID_MESSAGE_VALIDATION_SELECT_COLUMN	:	"表示項目を指定してください。"
		},

		report: []

	};

	var plugin = this;

	plugin.settings = {};

	var init = function() {
		// push Array report Option
		defaults.report.push(options);
		// extend
		$.extend(defaults);
	};

	plugin.PublicMethod = function() {
		// code goes here
	};

	plugin.PrivateMethod = function() {
		// code goes here
	};

	init();

};

})(jQuery);