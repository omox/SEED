// ■ Dateオブジェクトハック
// ----------------------------------------------------------------------------------------------------------------
/**
 * 入力エリアデータ送信 <br />
 *
 * @param frm 対象フォーム <br />
 * @param param 対象アクション名 <br />
 */
function entry_submit(frm,param){
    frm.entry.value = param;
    frm.submit();
}

/**
 * レポートエリアデータ送信 <br />
 *
 * @param frm 対象フォーム <br />
 * @param param 対象アクション名 <br />
 */
function report_submit(frm,param){
    frm.entry.value = param;
    frm.action = "../Servlet/Report.do";
    frm.submit();
}

/**
 * 選択エリアデータ送信 <br />
 *
 * @param frm 対象フォーム <br />
 * @param param 選択番号 <br />
 */
function select_submit(frm,param){
    frm.select.value = param;
    frm.submit();
}
/**
 * コンボデータ送信 <br />
 *
 * @param frm 対象フォーム <br />
 */
function cmbchange(frm,cmb,id){
    frm.action = "../Servlet/Report.do";
    document.getElementById(id).value = cmb.options[cmb.selectedIndex].text;
    frm.submit();
}
function form_submit(frm){
    frm.action = "../Servlet/Report.do";
    frm.submit();
}
/**
 * コンボボックスの選択値変更
 *
 * @param cmb 対象コンボ <br />
 * @param val 対象の値 <br />
 */
 function cmbSelecter(cmb,val){
    for (i=0; i < cmb.options.length; i++) {
         if (cmb.options[i].value == val){
             cmb.options[i].selected = true;
             break;
         }
     }
 }

/**
 * Enterキー無効化 <br />
 */
function control_keydown(){
    var src = window.event.srcElement;

    if(event.keyCode == 13) {
        if (src.type == '' ) {
            src.click();
        } else if (src.type != 'submit'
            && src.type != 'button'
            && src.type != 'textarea' ) {
            //return false;
        }
    }
}

/* スクロールバーの位置取得 */
function getScrollPosition(frm) {
  var obj = new Object();
  obj.x = document.documentElement.scrollLeft || document.body.scrollLeft;
  obj.y = document.documentElement.scrollTop || document.body.scrollTop;
  frm.slxpos.value = obj.x;
  frm.slypos.value = obj.y;
}

// ■ Dateオブジェクトハック
// ----------------------------------------------------------------------------------------------------------------
/**
 * 連想配列で返す
 * @return Array[]
 */
Date.prototype.toHashArray = function() {
  var dateArray = new Object();
  dateArray["year"]  = this.getFullYear();
  dateArray["month"]  = this.getMonth();
  dateArray["date"]  = this.getDate();
  dateArray["day"]  = this.getDay();
  dateArray["hour"]  = this.getHours();
  dateArray["minute"]  = this.getMinutes();
  dateArray["second"]  = this.getSeconds();

  return dateArray;
}
/**
 * 当年が閏年かどうか
 * @return boolean ( true:閏年 false:それ以外 )
*/
Date.prototype.isLeapYear = function() {
  var year = this.getFullYear();
  return (((year % 4 == 0) && (year % 100 != 0)) || (year % 400 == 0));
}

/**
 * 当月の日数を取得 閏年対応
 * @return int
 */
Date.prototype.getMonthDayCount = function() {
  var year = this.getFullYear();
  var month = this.getMonth();

  var dayNumber =
    (month == 0 || month == 2 || month == 4 || month == 6 || month == 7 || month == 9 || month == 11) ? 31 :
    (month == 3 || month == 5 || month == 8 || month == 10) ? 30 :
    (this.isLeapYear()) ? 29 :
    28;

  return dayNumber;
}
/**
 * yyyy/mm/dd形式で表示
 * @return String
 */
Date.prototype.toDefaultString = function() {
  var arr = this.toHashArray();
  var dispMonth = arr["month"] + 1;
  return arr["year"] + '/' + dispMonth + '/' + arr["date"];
}

/**
 * 当日付と対象日付が同じかどうか
 * @param d 対象日付
 * @return boolean ( true:同じ false:それ以外 )
 */
Date.prototype.isSameDate = function(d){
  var arr    = this.toHashArray();
  var arr2  = d.toHashArray();

  return ((arr["year"] == arr2["year"]) && (arr["month"] == arr2["month"]) && (arr["date"] == arr2["date"]));
}
/**
 * 当日付より対象日付が大きいか
 * @param d 対象日付
 * @return boolean ( true:大きい false:それ以外 )
 */
Date.prototype.compDate = function(d){
  var arr = this.toHashArray();
  var arr2 = d.toHashArray();
  return ( (arr2["year"] > arr["year"]) ||  ((arr2["year"] == arr["year"]) && (arr2["month"] > arr["month"])) || ((arr2["year"] == arr["year"]) && (arr2["month"] == arr["month"]) && (arr2["date"] > arr["date"])) );
}

/**
 * 当日付に対象月を加算
 * 計算後の日が末日の場合、調整される
 * @param d 対象月
 */
Date.prototype.addMonth = function(d){
  var arr = this.toHashArray();
  var year = parseInt(d/12);
  var month = d - (year * 12);
  var dd = this.getDate();

  this.setYear(this.getFullYear() + year);
  this.setMonth(this.getMonth() + month);

  var dayLast = this.getMonthDayCount();
  this.setDate((dd > dayLast) ? dayLast : dd);
}
// ----------------------------------------------------------------------------------------------------------------

 /**
  * 0で始まる数値を10進数に変換
  * @param n
  * @return val
  */
function get10Num(n){
  return (n.substr(0,1)=="0")? n.substr(1):n;
}
/**
 * FromToが逆か
 * @param cmb1 期間From
 * @param cmb2 期間to
 * @return boolean ( true:大丈夫 false:それ以外 )
 */
function getYMFromToRevers(cmb1,cmb2){
  var sd1 = cmb1.options[cmb1.selectedIndex].text;
  var sd2 = cmb2.options[cmb2.selectedIndex].text;
  var dd1 = new Date(sd1.substr(0,4),parseInt(get10Num(sd1.substr(5,sd1.length - 6)))-1);
  var dd2 = new Date(sd2.substr(0,4),parseInt(get10Num(sd2.substr(5,sd2.length - 6)))-1);

  return dd1.compDate(dd2) || dd1.isSameDate(dd2);
}
