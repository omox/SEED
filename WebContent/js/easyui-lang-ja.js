var byteNum = 2;
if ($.fn.pagination){
	$.fn.pagination.defaults.beforePageText = '頁';
	$.fn.pagination.defaults.afterPageText = '/ {pages}';
	$.fn.pagination.defaults.displayMsg = '{total} 行中 {from} ～ {to} 表示 ';
	$.fn.pagination.defaults.pageSize = 200;
	$.fn.pagination.defaults.pageList = [10,20,30,50,100,200];
}
if ($.fn.datagrid){
	$.fn.datagrid.defaults.loadMsg = 'しばらくお待ちください ...';
}
if ($.fn.treegrid && $.fn.datagrid){
	$.fn.treegrid.defaults.loadMsg = $.fn.datagrid.defaults.loadMsg;
}
if ($.messager){
	$.messager.defaults.ok = 'Ok';
	$.messager.defaults.cancel = 'キャンセル';
}
if ($.fn.validatebox){
	$.fn.validatebox.defaults.missingMessage = 'この項目は必須入力項目です。';
	$.fn.validatebox.defaults.rules.email.message = 'Please enter a valid email address.';
	$.fn.validatebox.defaults.rules.url.message = 'Please enter a valid URL.';
	$.fn.validatebox.defaults.rules.length.message = '{0} から {1} の文字数を入力してください。';
	$.fn.validatebox.defaults.rules.remote.message = 'Please fix this field.';
	$.extend($.fn.validatebox.defaults.rules, {
		maxLen: {
			validator: function(value, param){
				var len = param[0]*1;
				fullCharLen = Math.floor(len/byteNum);
				$.fn.validatebox.defaults.rules.maxLen.message = "全角"+fullCharLen+"桁、半角"+len+"桁以下の値を入力してください。";
				if(getByte(value) <= len){
					return true;
				}
				return false;
			},
			message: '{0}桁以下の値を入力してください。'
		},
		intMaxLen: {
			validator: function(value, param){
				var str = value.replace(/-|,/g,"");
				var re = new RegExp("^[0-9]{0," + param[0] + "}$");
				if(str.match(re)){
					return true;
				}
				return false;
			},
			message: '{0}桁以下の半角数字を入力してください。'
		},
		// 不等号等の数値以外の項目の入力を許可しない
		intMaxLenNumberOnly: {
			validator: function(value, param){
				var str = value.replace(/,/g,"");
				var re = new RegExp("^[0-9]{0," + param[0] + "}$");
				if(str.match(re)){
					return true;
				}
				return false;
			},
			message: '{0}桁以下の半角数字を入力してください。'
		},
		intMaxLenAndMinEqVal: {
			validator: function(value, param){
				var str = value.replace(/-|,/g,"");
				var re = new RegExp("^[0-9]{0," + param[0] + "}$");
				if(str.match(re)){
					var str2 = value.replace(/,/g,"");
					if(isFinite(str2)){
						return parseInt(str2) >= param[1];
					}
				}
				return false;
			},
			message: '{0}桁以下で、{1}以上の半角数字を入力してください。'
		},
		intMaxLenAndMaxEqVal: {
			validator: function(value, param){
				var str = value.replace(/-|,/g,"");
				var re = new RegExp("^[0-9]{0," + param[0] + "}$");
				if(str.match(re)){
					var str2 = value.replace(/,/g,"");
					if(isFinite(str2)){
						return parseInt(str2) <= param[1];
					}
				}
				return false;
			},
			message: '{0}桁以下で、{1}以下の半角数字を入力してください。'
		},
		intMaxLenAndMinMaxEqVal: {
			validator: function(value, param){
				var str = value.replace(/-|,/g,"");
				var re = new RegExp("^[0-9]{0," + param[0] + "}$");
				if(str.match(re)){
					var str2 = value.replace(/,/g,"");
					if(isFinite(str2)){
						if(parseInt(str2) >= param[1]){
							return parseInt(str2) <= param[2];
						}
					}
				}
				return false;
			},
			message: '{0}桁以下で、{1}～{2}の半角数字を入力してください。'
		},
		floatMaxLen: {
			validator: function(value, param){
				var str = value.replace(/-|,/g,"");
				// 少数点込み
				var re1 = new RegExp("^[0-9]{1," + param[0] + "}(\.[0-9]{1," + param[1] + "})?$");
				// 整数のみ
				var re2 = new RegExp("^[0-9]{0," + param[0] + "}$");
				return str.match(re1) && str.split('.')[0].match(re2);
			},
			message: '整数部{0}桁、小数部{1}桁の半角数字を入力してください。'
		},
		floatMaxLenAndMinEqVal: {
			validator: function(value, param){
				var str = value.replace(/-|,/g,"");
				// 少数点込み
				var re1 = new RegExp("^[0-9]{1," + param[0] + "}(\.[0-9]{1," + param[1] + "})?$");
				// 整数のみ
				var re2 = new RegExp("^[0-9]{0," + param[0] + "}$");
				if(str.match(re1) && str.split('.')[0].match(re2)){
					var str2 = value.replace(/,/g,"");
					if(isFinite(str2)){
						return parseFloat(str2) >= param[2];
					}
				}
				return false;
			},
			message: '整数部{0}桁、小数部{1}で、{2}以上の半角数字を入力してください。'
		},
		floatMaxLenAndMaxEqVal: {
			validator: function(value, param){
				var str = value.replace(/-|,/g,"");
				// 少数点込み
				var re1 = new RegExp("^[0-9]{1," + param[0] + "}(\.[0-9]{1," + param[1] + "})?$");
				// 整数のみ
				var re2 = new RegExp("^[0-9]{0," + param[0] + "}$");
				if(str.match(re1) && str.split('.')[0].match(re2)){
					var str2 = value.replace(/,/g,"");
					if(isFinite(str2)){
						return parseFloat(str2) <= param[2];
					}
				}
				return false;
			},
			message: '整数部{0}桁、小数部{1}で、{2}以下の半角数字を入力してください。'
		},
		floatMaxLenAndMinMaxEqVal: {
			validator: function(value, param){
				var str = value.replace(/-|,/g,"");
				// 少数点込み
				var re1 = new RegExp("^[0-9]{1," + param[0] + "}(\.[0-9]{1," + param[1] + "})?$");
				// 整数のみ
				var re2 = new RegExp("^[0-9]{0," + param[0] + "}$");
				if(str.match(re1) && str.split('.')[0].match(re2)){
					var str2 = value.replace(/,/g,"");
					if(isFinite(str2)){
						if(parseFloat(str2) >= param[2]){
							return parseFloat(str2) <= param[3];
						}
					}
				}
				return false;
			},
			message: '整数部{0}桁、小数部{1}で、{2}～{3}の半角数字を入力してください。'
		},
		formatMaxLen: {
			validator: function(value, param){
				if (undefined===value) return true;
				if (value==='') return true;
				var newValue = '';
				/*for (var i=0; i<param[1].length; i++){
					var c = param[1].substr(i, 1);
					if (c==="#"){
						newValue = newValue + value.substr(i, 1);
					}
				}*/
				var str = param[1].replace(/#/g, '');
				for(var i=0; i<value.length; i++){
					var c = value.substr(i, 1);
					if ( str.indexOf(c) == -1) {
						newValue = newValue + value.substr(i, 1);
					}
				}
				if(newValue.length <= param[0]*1){
					return true;
				}
				return false;
			},
			message: '{0}桁以下の値を入力してください。'
		},
		// 不等号等の記号を除き、数値のみの入力のみを許可する
		formatMaxLenNumberOnly: {
			validator: function(value, param){
				if (undefined===value) return true;
				if (value==='') return true;
				var newValue = '';
				var str = param[1].replace(/#/g, '');

				if(str.match(/^[0-9]+$/)){
					return true;
				}
				for(var i=0; i<value.length; i++){
					var c = value.substr(i, 1);
					if ( str.indexOf(c) == -1) {
						newValue = newValue + value.substr(i, 1);
					}
				}
				if(newValue.length <= param[0]*1){
					return true;
				}
				return false;
			},
			message: '{0}桁以下の値を入力してください。'
		},
		// 表示上の値をformmaterで変えて、parserで整形してるnumberbox用
		nboxLen: {
			validator: function(value, param){
				value = $('#'+param[1]).numberbox('getValue');
				if(value.length === param[0]*1){
					return true;
				}
				return false;
			},
			message: '{0}桁の値を入力してください。'
		},
		ym: {
			validator: function(value){
				return chkYm(value);
			},
			message: '正しい年月を西暦（YYMM）で入力してください。'
		},
		yymmdd: {
			validator: function(value){
				return chkYmd(value);
			},
			message: '正しい日付を西暦（YYMMDD）で入力してください。'
		},
		yyyymmdd: {
			validator: function(value){
				return chkYmd2(value);
			},
			message: '正しい日付を西暦（YYYYMMDD）で入力してください。'
		},
		yymmddAndNow: {
			validator: function(value){
				var rt = chkYmd(value);
				if(rt){
					rt = parseInt(value) >= parseInt(getToday().substr(2,6));
				}
				return rt;
			},
			message: '本日以降の日付を西暦（YYMMDD）で正しく入力してください。'
		},
		yymmddAndPast: {
			validator: function(value){
				var rt = chkYmd(value);
				if(rt){
					rt = parseInt(value) <= parseInt(getToday().substr(2,6));
				}
				return rt;
			},
			message: '本日以前の日付を西暦（YYMMDD）で正しく入力してください。'
		},
		yymmddAndPastZ: {
			validator: function(value){
				var rt = chkYmd(value);
				if(rt){
					rt = parseInt(value) < parseInt(getToday().substr(2,6));
				}
				return rt;
			},
			message: '前日以前の日付を西暦（YYMMDD）で正しく入力してください。'
		},
		mmdd: {
			validator: function(value){
				return chkMmdd(value);
			},
			message: '正しい月日（MMDD）を入力してください。'
		},
		hhmm: {
			validator: function(value){
				return chkHhmm(value);
			},
			message: '正しい時間（HHMM）を入力してください。'
		},
		intMinVal: {
			validator: function(value, param){
				var str = value.replace(/,/g,"");
				if(str.match(/^[0-9]+$/)){
					return parseInt(str) > param[0];
				}
				return false;
			},
			message: '{0}より大きい半角数字を入力してください。'
		},
		intMinEqVal: {
			validator: function(value, param){
				var str = value.replace(/,/g,"");
				if(str.match(/^[0-9]+$/)){
					return parseInt(str) >= param[0];
				}
				return false;
			},
			message: '{0}以上の値を入力してください。'
		},
		int_range: {
			validator: function(value, param){
				var str = value.replace(/,/g,"");
				if(str.match(/^[0-9]+$/)){
					return parseInt(str) >= param[0] &&parseInt(str) <= param[1];
				}
				return false;
			},
			message: '{0}～{1}の範囲の値を入力してください。'
		},
		onlySuuji: {
			validator: function(value, param){
				if(value.match(/^[0-9]+$/)){
					if(getByte(value) <= param[0]*1){
						return true;
					}
				}
				return false;
			},
			message: '{0}桁以下の半角数字を入力してください。'
		},
		// JANコードのデータに半角スペースが含まれている為、数字と半角スペースの入力をOKにする。
		onlySuujiSpace: {
			validator: function(value, param){
				if(value.match(/^[0-9 ]+$/)){
					if(getByte(value) <= param[0]*1){
						return true;
					}
				}
				return false;
			},
			message: '{0}桁以下の半角数字を入力してください。'
		},
		onlySuujihaihun: {
			validator: function(value, param){
				if(value.match(/^[0-9-]+$/)){
					if(getByte(value) <= param[0]*1){
						return true;
					}
				}
				return false;
			},
			message: '{0}桁以下の半角数字を入力してください。'
		},
		onlyHalfChar: {
			validator: function(value, param){
				if(checkHalfChar(value)){
					if(getByte(value) <= param[0]*1){
						return true;
					}
				}
				return false;
			},
			message: '{0}桁以下の半角文字を入力してください。'
		},
		onlyFullChar: {
			validator: function(value, param){
				var len = param[0]*1;
				var fullCharLen = Math.floor(len/byteNum);
				$.fn.validatebox.defaults.rules.onlyFullChar.message = fullCharLen+"桁以下の全角文字を入力してください。";
				if(checkFullChar(value)){
					if(value.length <= fullCharLen){
						return true;
					}
				}
				return false;
			},
			message: '{0}桁以下の全角文字を入力してください。'
		},
		// 大文字の半角英字の入力を許す
		onlyCharAlphaL: {
			validator: function(value, param){
				var len = param[0]*1;
				$.fn.validatebox.defaults.rules.onlyFullChar.message = len+"桁以下の大文字半角英字を入力してください。";
				if(checkFullCharAlpha(value)){
					if(value.length <= len){
						return true;
					}
				}
				return false;
			},
			message: '{0}桁以下の大文字半角英字を入力してください。'
		},
		tel: {
			validator: function(value){
				return chkTel(value);
			},
			message: 'ハイフンありの正しい電話番号を入力してください。'
		},
		fax: {
			validator: function(value){
				return chkTel(value);
			},
			message: 'ハイフンありの正しいFAX番号を入力してください。'
		}
	});
}
if ($.fn.textbox){
	$.fn.textbox.defaults.missingMessage = 'この項目は、必須指定です。';
}
if ($.fn.numberbox){
	$.fn.numberbox.defaults.missingMessage = 'この項目は、必須指定です。';
}
if ($.fn.numberspinner){
	$.fn.numberspinner.defaults.missingMessage = 'この項目は、必須指定です。';
}
if ($.fn.combobox){
	$.fn.combobox.defaults.missingMessage = 'この項目は、必須指定です。';
}
if ($.fn.combotree){
	$.fn.combotree.defaults.missingMessage = 'この項目は、必須指定です。';
}
if ($.fn.combogrid){
	$.fn.combogrid.defaults.missingMessage = 'この項目は、必須指定です。';
}
if ($.fn.calendar){
	$.fn.calendar.defaults.weeks = ['日','月','火','水','木','金','土'];
	$.fn.calendar.defaults.months = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'];
}
if ($.fn.datebox){
	$.fn.datebox.defaults.currentText = '今日';
	$.fn.datebox.defaults.closeText = '閉じる';
	$.fn.datebox.defaults.okText = '適用';
	$.fn.datebox.defaults.missingMessage = 'この項目は、必須指定です。';
	$.fn.datebox.defaults.formatter = function(date){
		var y = date.getFullYear();
		var m = date.getMonth()+1;
		var d = date.getDate();
		return y+''+(m<10?('0'+m):m)+''+(d<10?('0'+d):d);
	};
	$.fn.datebox.defaults.parser = function(s){
		try {
			if (s.length == 8){
				var y = parseInt(s.substr(0,4),10);
				var m = parseInt(s.substr(4,2),10);
				var d = parseInt(s.substr(6,2),10);
				if (!isNaN(y) && !isNaN(m) && !isNaN(d)){
					return new Date(y,m-1,d);
				}
			}
		} catch(e){}
		return new Date();
	};
}
if ($.fn.datetimebox && $.fn.datebox){
	$.extend($.fn.datetimebox.defaults,{
		currentText: $.fn.datebox.defaults.currentText,
		closeText: $.fn.datebox.defaults.closeText,
		okText: $.fn.datebox.defaults.okText,
		missingMessage: $.fn.datebox.defaults.missingMessage
	});
}

//年月入力値チェック
function chkYm(str) {
	try{
		// 正規表現による書式チェック
		if(str.match(/^([0-9]{2})\/(0[1-9]|1[012])$/)){
			// 妥当性チェック
			str = str.replace(/\//g,"")
		}
		if(str.match(/^([0-9]{2})(0[1-9]|1[012])$/)){
			// 妥当性チェック
			return checkDate(('20'+str+'01'));
		}
	} catch(e) {
		return false;
	}
	return false;
}
//年月日入力値チェック
function chkYmd(str) {
	try{
		// 正規表現による書式チェック
		if(str.match(/^([0-9]{2})\/(0[1-9]|1[012])\/(0[1-9]|[12][0-9]|3[01])$/)
		|| str.match(/^([0-9]{2})\/(0[1-9]|1[012])\/(0[1-9]|[12][0-9]|3[01])\((日|月|火|水|木|金|土)\)$/)){
			str = str.replace(/\//g,"");
		}
		if(str.match(/^([0-9]{2})(0[1-9]|1[012])(0[1-9]|[12][0-9]|3[01])$/)){
			// 妥当性チェック
			return checkDate(('20'+str));
		}
		if(str.match(/^([0-9]{2})(0[1-9]|1[012])(0[1-9]|[12][0-9]|3[01])\((日|月|火|水|木|金|土)\)$/)){
			// 妥当性チェック
			return checkDate(('20'+str).substr(0, 8));
		}
	} catch(e) {
		return false;
	}

	return false;
}
//年月日入力値チェック
function chkYmd2(str) {
	try{
		// 正規表現による書式チェック
		if(str.match(/^([0-9]{4})\/(0[1-9]|1[012])\/(0[1-9]|[12][0-9]|3[01])$/)
		|| str.match(/^([0-9]{4})\/(0[1-9]|1[012])\/(0[1-9]|[12][0-9]|3[01])\((日|月|火|水|木|金|土)\)$/)){
			str = str.replace(/\//g,"");
		}
		if(str.match(/^([0-9]{4})(0[1-9]|1[012])(0[1-9]|[12][0-9]|3[01])$/)){
			// 妥当性チェック
			return checkDate(str);
		}
		if(str.match(/^([0-9]{4})(0[1-9]|1[012])(0[1-9]|[12][0-9]|3[01])\((日|月|火|水|木|金|土)\)$/)){
			// 妥当性チェック
			return checkDate(str.substr(0, 8));
		}
	} catch(e) {
		return false;
	}

	return false;
}
//月日入力値チェック
function chkMmdd(str) {
	try{
		// 正規表現による書式チェック
		if(str.match(/^(0[1-9]|1[012])\/(0[1-9]|[12][0-9]|3[01])$/)){
			// 妥当性チェック
			str = str.replace(/\//g,"")
		}
		if(str.match(/^(0[1-9]|1[012])(0[1-9]|[12][0-9]|3[01])$/)){
			// 妥当性チェック
			return checkDate(('2001'+str));
		}
	} catch(e) {
		return false;
	}
	return false;
}
//時分入力値チェック
function chkHhmm(str) {
	try{
		// 正規表現による書式チェック
		if(str.match(/^([01][0-9]|2[0-3]):([0-5][0-9])$/)){
			str = str.replace(/:/g,"");
		}
		if(str.match(/^([01][0-9]|2[0-3])([0-5][0-9])$/)){
			// 妥当性チェック
			return true;
		}
	} catch(e) {
		return false;
	}

	return false;
}
/**
 * 日付の妥当性チェック
 * year 年
 * month 月
 * day 日
 */
function checkDate(s) {
	var y = parseInt(s.substr(0,4),10);
	var m = parseInt(s.substr(4,2),10);
	var d = parseInt(s.substr(6,2),10);
	var dt = new Date(y, m-1, d);
	if(dt === null || dt.getFullYear() !== y || dt.getMonth() + 1 !== m || dt.getDate() !== d) {
		return false;
	}
	return true;
}

/**
 * システム日付
 */
function getToday() {
	var dt = new Date();
	var y = dt.getFullYear();
	var m = dt.getMonth() + 1;
	var d = dt.getDate();
	return y + '' + $.ex.lpad(m, 2,'0') + '' + $.ex.lpad(d, 2,'0');
}
/**
 * 文字列のバイト数取得
 * text 対象文字列
 */
function getByte(text)
{
	count = 0;
	for (var i=0; i<text.length; i++){
		var c = text.charCodeAt(i);
		if ( (c >= 0x0 && c < 0x81) || (c == 0xf8f0) || (c >= 0xff61 && c < 0xffa0) || (c >= 0xf8f1 && c < 0xf8f4)){
			count += 1;
		} else {
			count += byteNum;
		}
	}
	return count;
}

//半角文字入力チェック
function checkHalfChar(text){
	for (var i=0; i<text.length; i++){
		var c = text.charCodeAt(i);
		if ( (c >= 0x0 && c < 0x81) || (c == 0xf8f0) || (c >= 0xff61 && c < 0xffa0) || (c >= 0xf8f1 && c < 0xf8f4)){

		} else {
			return false;
		}
	}
	return true;
}

//全角文字入力チェック
function checkFullChar(text){
//	for (var i=0; i<text.length; i++){
//		var c = text.charCodeAt(i);
//		if ( (c >= 0x0 && c < 0x81) || (c == 0xf8f0) || (c >= 0xff61 && c < 0xffa0) || (c >= 0xf8f1 && c < 0xf8f4)){
//			return false;
//		}
//	}
//	return true;
	 return text.match(/^[^ -~｡-ﾟ]*$/);
}
function checkFullCharAlpha(text){
//	for (var i=0; i<text.length; i++){
//		var c = text.charCodeAt(i);
//		if ( (c >= 0x0 && c < 0x81) || (c == 0xf8f0) || (c >= 0xff61 && c < 0xffa0) || (c >= 0xf8f1 && c < 0xf8f4)){
//			return false;
//		}
//	}
//	return true;
	 return text.match(/^[A-Z]+$/);
}
//TEL入力値チェック
function chkTel(str) {
	try{
		if((''+str).length > 13){
			return false;
		}
		if(str.match(/^[0-9]{2,5}\-[0-9]{1,4}\-[0-9]{1,4}$/)){
			// 妥当性チェック
			return true;
		}
	} catch(e) {
		return false;
	}

	return false;
}
