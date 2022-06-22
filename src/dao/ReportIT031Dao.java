/**
 *
 */
package dao;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.JDBCType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import authentication.bean.User;
import authentication.defines.Consts;
import common.DefineReport;
import common.DefineReport.DataType;
import common.FileList;
import common.InputChecker;
import common.MessageUtility;
import common.MessageUtility.MsgKey;
import dto.JQEasyModel;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 */
public class ReportIT031Dao extends ItemDao {

	boolean isTest = true;


	/** 最大処理件数 */
	public static int MAX_ROW = 10000;

	/**
	 * インスタンスを生成します。
	 * @param source
	 */
	public ReportIT031Dao(String JNDIname) {
		super(JNDIname);
	}

	/**
	 * ファイル変換
	 * @param userInfo
	 *
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws Exception
	 */
	public JQEasyModel convert(HttpSession session, User userInfo, HashMap<String, String> map, String file) {
		// jqEasy 用 JSON モデル作成
		JQEasyModel json = new JQEasyModel();
		JSONObject option = new JSONObject();

		String sysdate = (String)session.getAttribute(Consts.STR_SES_LOGINDT);
		String strMsg = null;

		// メッセージ情報取得
		MessageUtility mu = new MessageUtility();
		try {

			// ユーザー情報設定
			setUserInfo(userInfo);

			// *** 情報取得 ***
			FileList fl = new FileList();
			ArrayList<Object[]> dL = null;
			// ファイル読み込み
			dL = fl.readCsv(session, file);

			// *** 情報チェック ***
			int idxHeader = 0;
			// ファイルレイアウトチェック
			strMsg = this.checkFileLayout(dL, idxHeader);

			// データ加工 + 最新情報取得
			ArrayList<JSONObject> dataList = new ArrayList<JSONObject>();
			if(strMsg==null){
				// データ詳細チェック
				List<JSONObject> msgList = this.checkData( map, userInfo, sysdate, mu, dL);
				if(msgList.size() > 0){
					option.put(MsgKey.E.getKey(), msgList);
				}else{
					// データ加工
					FileLayout[] dateItm = new FileLayout[]{FileLayout.YUKO_STDT,FileLayout.YUKO_EDDT};
					for (int i = idxHeader; i < dL.size(); i++) {
						Object[] data = dL.get(i);

						// CSVを分解、画面上と同じように登録用のデータの形にする
						JSONObject osrc = new JSONObject();
						for(FileLayout itm :FileLayout.values()){
							String val = StringUtils.trim((String) data[itm.getNo()-1]);
							if(StringUtils.startsWith(val, "\"")&&StringUtils.endsWith(val, "\"")){
								val = StringUtils.removeEnd(StringUtils.removeStart(val, "\""), "\"");
							}
							// 日付数値を入力形式の長さに変換
							if(ArrayUtils.contains(dateItm, itm)){
								val = StringUtils.right(val, 6);
							}
							osrc.put(itm.getCol(), val);
						}
						dataList.add(osrc);
					}
					json.setRows(dataList);
					json.setTotal(dataList.size());
				}
			}
			json.setOpts(option);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			//msg = MessageUtility.getMsgText(Msg.E00003.getVal(), "\n" + e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			//msg = MessageUtility.getMsgText(Msg.E00003.getVal(), "\n" + e.getMessage());
		}
		// 実行結果のメッセージを設定
		json.setMessage(strMsg);
		return json;
	}

	private List<JSONObject> checkData(
			HashMap<String, String> map, User userInfo, String sysdate1, MessageUtility mu,
			ArrayList<Object[]> dL
			) {
		JSONArray msg = new JSONArray();
		int cnt = 1;

		// DB最新情報再取得
		// 2.ソースマスタ
		for (int i = 0; i < dL.size(); i++) {
			Object[] data = dL.get(i);

			String reqNo = String.valueOf(cnt) + "行目：";
			cnt++;

			for(FileLayout itm :FileLayout.values()){
				String val = StringUtils.trim((String) data[itm.getNo()-1]);

				if(StringUtils.startsWith(val, "\"")&&StringUtils.endsWith(val, "\"")){
					val = StringUtils.removeEnd(StringUtils.removeStart(val, "\""), "\"");
				}

				if(StringUtils.isNotEmpty(val)){
					DataType dtype = null;
					int[] digit = null;
					try {
						DefineReport.InpText inpsetting = DefineReport.InpText.valueOf(itm.getCol());
						dtype = inpsetting.getType();
						digit = new int[]{inpsetting.getDigit1(), inpsetting.getDigit2()};
					}catch (IllegalArgumentException e){
						dtype = itm.getDataType();
						digit = itm.getDigit();
					}
					// ①データ型による文字種チェック
					if(!InputChecker.checkDataType(dtype, val)){
						JSONObject o = mu.getDbMessageObjDataTypeErr(dtype, new String[]{reqNo+itm.getTxt()+"は"});
						msg.add(o);
						return msg;
					}
					// ②データ桁チェック
					if(!InputChecker.checkDataLen(dtype, val, digit)){
						JSONObject o = mu.getDbMessageObjLen(dtype, new String[]{reqNo+itm.getTxt()+"は"});
						msg.add(o);
						return msg;
					}
				}
			}
		}
		return msg;
	}

	private String checkFileLayout(ArrayList<Object[]> eL, int idxHeader) {
		// 1.件数チェック
		if(eL.size() <= idxHeader){
			return "ファイルにデータがありません。";
		}else if(eL.size() > MAX_ROW + idxHeader){
			return "データが多すぎます。当画面では、"+MAX_ROW+"件以下のデータを想定しています。";
		}
//		// 2.ヘッダーチェック
//		Object[] inpHeader = eL.get(idxHeader - 1);
//		for(FileLayout item : FileLayout.values()){
//			if(item.getNo()-1 < inpHeader.length){
//				if(!StringUtils.equals(item.getTxt(), (String) inpHeader[item.getNo()-1])){
//					return "\nファイルの列情報が異なります。正しいCSVファイルかどうか確認してください。";
//				}
//			}
//		}
		// 3.項目数チェック
		if(FileLayout.values().length != eL.get(idxHeader).length){
			return "\nファイルの列情報が異なります。正しいCSVファイルかどうか確認してください。";
		}
		return null;
	}

	/**  File出力項目の参照テーブル */
	public enum RefTable {
		/** ソースコード管理マスタ */
		MSTSRCCD(2,"ソースコード管理マスタ");
		private final Integer col;
		private final String txt;
		/** 初期化 */
		private RefTable(Integer col, String txt) {
			this.col = col;
			this.txt = txt;
		}
		/** @return col 列番号 */
		public Integer getCol() { return col; }
		/** @return txt 表示名称 */
		public String getTxt() { return txt; }
	}

	/**  Fileレイアウト */
	public enum FileLayout {
		/** ソースコード */
		SRCCD(1,"ソースコード",RefTable.MSTSRCCD,"SRCCD","CHARACTER(14)"),
		/** ソース区分 */
		SOURCEKBN(2,"ソース区分",RefTable.MSTSRCCD,"SOURCEKBN","SMALLINT"),
		/** 有効開始日 */
		YUKO_STDT(3,"有効開始日",RefTable.MSTSRCCD,"YUKO_STDT","INTEGER"),
		/** 有効終了日 */
		YUKO_EDDT(4,"有効終了日",RefTable.MSTSRCCD,"YUKO_EDDT","INTEGER"),
		/** 順位（入力順番） */
		SEQNO(5,"順位",RefTable.MSTSRCCD,"SEQNO","SMALLINT");

		private final Integer no;
		private final String txt;
		private final RefTable tbl;
		private final String col;
		private final String typ;
		/** 初期化 */
		private FileLayout(Integer no, String txt, RefTable tbl, String col, String typ) {
			this.no = no;
			this.txt = txt;
			this.tbl = tbl;
			this.col = col;
			this.typ = typ;
		}
		/** @return col 列番号 */
		public Integer getNo() { return no; }
		/** @return txt 表示名称 */
		public String getTxt() { return txt; }
		/** @return tbl 参照テーブル */
		public RefTable getTbl() { return tbl; }
		/** @return tbl 参照列 */
		public String getCol() { return col; }

		/** @return datatype データ型のみ */
		public DataType getDataType() {
			if(typ.indexOf("INT") != -1){ return DefineReport.DataType.INTEGER;}
			if(typ.indexOf("DEC") != -1){	return DefineReport.DataType.DECIMAL;}
			if(typ.indexOf("DATE")!= -1){return DefineReport.DataType.DATE;}
			return DefineReport.DataType.TEXT;
		}
		/** @return boolean */
		public boolean isText() {
			return getDataType() == DefineReport.DataType.TEXT;
		}
		/** @return digit 桁数 */
		public int[] getDigit() {
			int digit1 = 0, digit2 = 0;
 			if(typ.indexOf(",") > 0){
				String[] sDigit = typ.substring(typ.indexOf("(")+1, typ.indexOf(")")).split(",");
				digit1 = NumberUtils.toInt(sDigit[0]);
				digit2 = NumberUtils.toInt(sDigit[1]);
 			}else if(typ.indexOf("(") > 0){
 				digit1 = NumberUtils.toInt(typ.substring(typ.indexOf("(")+1, typ.indexOf(")")));
			}else if(StringUtils.equals(typ, JDBCType.SMALLINT.getName())){
				digit1 = dbNumericTypeInfo.SMALLINT.getDigit();
			}else if(StringUtils.equals(typ, JDBCType.INTEGER.getName())){
				digit1 = dbNumericTypeInfo.INTEGER.getDigit();
			}
			return new int[]{digit1, digit2};
		}
	}
}
