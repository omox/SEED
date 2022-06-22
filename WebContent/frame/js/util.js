// getElementById拡張
// @param ids id
// @return obj
function get(ids){
    if(document.getElementById)         //e5,e6,n6,n7,m1,o6,o7,s1
    return document.getElementById(ids)
    else if(document.all)               //e4
    return document.all(ids)
    else if(document.layers)            //n4
    return document.layers[ids]
}
//------------------------------------------------------------------------------------------------
// 別ウインドウを表示する
// パラメータはそのままget用に付与するのみ
// @param strUrl 別ウインドウURL
// @param w 別ウインドウの横幅
// @param h 別ウインドウの縦幅
// @param parm 別ウインドウURLへ付与するパラメータ文字列
//------------------------------------------------------------------------------------------------
function openWindow(strUrl,w,h,parm){
  var style,url;
  style = '';
  url = '';
  style +='left=0,top=0';
  style +=',width='+w;
  style +=',height='+h;
  style +=',scrollbars=yes,resizable=yes';
  url += strUrl;
  url += (parm.length>0)?parm:'';
  window.open(url,'',style);
}
//------------------------------------------------------------------------------------------------
// 基本情報画面より、道路を選択する別ウインドウを表示する
// @param val_id 選択した道路idを設定する要素id
// @param text_id 選択した道路名を設定する要素id
//------------------------------------------------------------------------------------------------
function openRoadListWindow(val_id,text_id){
  var parm = '';
  var call_mode = 'road';
  parm += '?call_mode='+call_mode;
  parm += '&val_id='+val_id;
  parm += '&text_id='+text_id;
  openWindow('institution_basic_listwindow.php',500,500,parm);
}
//------------------------------------------------------------------------------------------------
// 基本情報画面より、施設を選択する別ウインドウを表示する
// @param road 道路idを格納するhiddenの要素id
// @param val_id 選択した施設idを設定する要素id
// @param text_id 選択した施設名を設定する要素id
//------------------------------------------------------------------------------------------------
function openInstututionListWindow(road,val_id,text_id){
  var parm = '';
  var call_mode = 'institutuion';
  var road_id = get(road).value; //get(road).value;

  parm += '?call_mode='+call_mode;
  parm += '&val_id='+val_id;
  parm += '&text_id='+text_id;
  parm += '&road_id='+road_id;
  openWindow('institution_basic_listwindow.php',500,500,parm);
}

//------------------------------------------------------------------------------------------------
// 基本情報画面より、反対方向の施設を選択する別ウインドウを表示する
// @param road 施設idを格納するhiddenの要素id
// @param val_id 選択した施設idを設定する要素id
// @param text_id 選択した施設名を設定する要素id
//------------------------------------------------------------------------------------------------
function openReverseInstututionListWindow(reverse_ins,val_id,text_id){
  var parm = '';
  var call_mode = 'reverse';
  var rev_id = reverse_ins;

  parm += '?call_mode='+call_mode;
  parm += '&val_id='+val_id;
  parm += '&text_id='+text_id;
  parm += '&institution_id='+rev_id;
  openWindow('institution_basic_listwindow.php',500,500,parm);
}

//------------------------------------------------------------------------------------------------
// 複数ある子要素のうち、
// 指定された要素idをセレクトボックス以外の子要素セレクトボックスを非表示にする
// @param parrent_id 子要素に複数セレクトボックスを持つ要素id
// @param target_id 表示したい要素id
//------------------------------------------------------------------------------------------------
function changeDisplayList(parrent_id,target_id){
  var parrent = get(parrent_id);
  var target = get(target_id);
  for (var i=0;i<parrent.childNodes.length;i++){
    if(parrent.childNodes[i].childNodes.length>0 || parrent.childNodes[i].nodeType==1){
      parrent.childNodes[i].style.display = 'none';
    }
  }
  target.style.display = '';
}
//------------------------------------------------------------------------------------------------
// 複数ある子要素のうち、
// 表示されている子セレクトボックスの値を返す
// @param parrent_id 子要素に複数セレクトボックスを持つ要素id
// @return {value,text} セレクトボックスのvalue,textを連想配列で返す
// @return 未選択の場合は空白を返す
//------------------------------------------------------------------------------------------------
function getSelectedDisplayListId(parrent_id){
  var parrent = get(parrent_id);
  var chooselist_id = '';
  for (var i=0;i<parrent.childNodes.length;i++){
    if(parrent.childNodes[i].childNodes.length>0){
      if(!(parrent.childNodes[i].style.display == 'none')){
        chooselist_id = parrent.childNodes[i].id;
        break;
      }
    }
  }
  if (chooselist_id != ''){
    var chi = get(chooselist_id);
    if (chi.selectedIndex == -1){ return ''; }
    return { value: chi.options[chi.selectedIndex].value ,
             text : chi.options[chi.selectedIndex].text };
  }else{
    return '';
  }
}
//------------------------------------------------------------------------------------------------
// 選択したセレクトボックスの値を親ウインドウの要素に設定し、閉じる
// @param parrent_id 子要素に複数セレクトボックスを持つ要素id
// @param val_id 選択したセレクトボックスのvalueを設定する親ウインドウの要素id
// @param text_id 選択したセレクトボックスのtextを設定する親ウインドウの要素id
// @return 正常:ウインドウを閉じる エラー:メッセージ表示
//------------------------------------------------------------------------------------------------
function setOpenerid(parrent_id,val_id,text_id){
  var parrent = get(parrent_id);
  var set_val = window.opener.get(val_id);
  var set_txt = window.opener.get(text_id);

  //選択項目を取得
  var selectedItem = getSelectedDisplayListId(parrent_id);
  if(selectedItem!=''){
    set_val.value = selectedItem['value'];
    set_txt.value = selectedItem['text'];
    return window.close();
  }else{
    return alert('リストから項目を選択してください。');
  }
}

//------------------------------------------------------------------------------------------------
// フォームのmodeを指定してサブミットする。
// @param mode_id ﾌｫｰﾑの処理を指定するmodeのid
// @param mode_val ﾌｫｰﾑの処理名称を指定
//------------------------------------------------------------------------------------------------
function setModeSubmit(mode_id,mode_val){
  var mode = get(mode_id);
  mode.value=mode_val;
  document.forms[0].submit();
}
//------------------------------------------------------------------------------------------------
// 指定フィールドへ空白設定
// @param elm 対象エレメント
//------------------------------------------------------------------------------------------------
function setBlanks(elm){
  with(elm){
    if (type == 'text' || type == 'textarea' || type == 'hidden' || type == 'file'){
      value ='';
    }else if(type == 'select-one') {
      //selectedIndex = -1; // notSelect
      selectedIndex = 0;
    }else if(type == 'radio' || type == 'checkbox'){
      checked = false;
    }else{
      nodeValue = '';
    }
  }
}
//------------------------------------------------------------------------------------------------
// 指定フィールドへ空白設定
// @param ids[] 対象id配列
//------------------------------------------------------------------------------------------------
function setReset(ids){
  for(var i=0;i<ids.length;i++){
    setBlanks(get(ids[i]));
  }
}
//------------------------------------------------------------------------------------------------
// 指定フィールドの表示非表示切り替え
// @param ids 対象id
//------------------------------------------------------------------------------------------------
function showItem(sel,val,tar){
  var div = get(tar);
  var caller = get(sel);
  var visible = div.style.display;
  if (visible == ''){
    //list 非表示
    div.style.display = 'none';
    if (val['visible'].length>0){
      caller.innerHTML=val['visible'];
    }
  }else{
    //list 表示
    div.style.display = '';
    if (val['invisible'].length>0){
      caller.innerHTML=val['invisible'];
    }
  }
}
//------------------------------------------------------------------------------------------------
// 指定フィールドのListを表示非表示切り替え
// @param ids 対象id
//------------------------------------------------------------------------------------------------
function showListItem(sel,tar){
  showItem(sel,{visible:'▼他の項目を表示',invisible:'△折りたたむ'},tar);
}
