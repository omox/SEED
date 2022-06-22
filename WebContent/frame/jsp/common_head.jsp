<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="MS932"%>
<LINK HREF="../css/common.css" REL="stylesheet" TYPE="text/css">
<script type="text/javascript">
<!--
function $(layName){
	if(document.getElementById)         //e5,e6,n6,n7,m1,o6,o7,s1用
		return document.getElementById(layName);
	else if(document.all)               //e4用
		return document.all(layName);
	else if(document.layers)            //n4用
		return document.layers[layName];
}

function getINNERHEIGHT(){
	if(window.opera)
		return window.innerHeight         //o6,o7
	else if(document.all)
		return document.body.clientHeight //ie4,ie5,ie6
	else if(document.layers)
		return  window.innerHeight        //n4
	else if(document.getElementById)
		return window.innerHeight         //n6,n7,m1,s1
	return null
}

function setScroller(tg){
	$(tg).style.height=getINNERHEIGHT()/1000*800;
}

function submit_forward(action,nm,val){
	var frm = document.forms[0];
 	var query = document.createElement("input");
	query.type = "hidden";
	query.name = nm;
	query.value = val;
	frm.appendChild(query);
	frm.action = action;

	try {
		frm.submit();
	} catch(e) {
		if (e.number == -2147024891) {
			alert("ファイル名をご確認下さい。");
		}
	}
}



function setCheckBoxs(obj,val){
  if(val=='0'){
  	if(obj[0]=='undefined'){ setBlanks(obj); return;}
	  for(var i=0;i<obj.length;i++){
	    setBlanks($(obj[i]));
	  }
  }else{
  	if(obj[0]=='undefined'){ setValue(obj,val); return;}
	  for(var i=0;i<obj.length;i++){
	    setValue($(obj[i]),val);
	  }
  }
}
function setSelfRepParms(repside,reparm){
	var frm = document.forms[1];
	if(frm==undefined){return;}
	var side = document.createElement("input");
	side.type = "hidden";
	side.name = "ReportSide";
	side.value = repside;
	frm.appendChild(side);
	var no = document.createElement("input");
	no.type = "hidden";
	no.name = "ReportNo";
	no.value = reparm;
	frm.appendChild(no);
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

/*
window.document.onkeydown = function() {
	if (event.keyCode == 116) {
       event.keyCode = null;
       return false;
   }
}
*/

//-->
</script>