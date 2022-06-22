/**
 *
 */
package dao;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;

import authentication.bean.User;
import authentication.defines.Consts;
import common.CmnDate;
import common.DefineReport;
import common.Defines;
import common.FileList;
import common.ItemList;
import common.MessageUtility;
import common.MessageUtility.Msg;
import common.MessageUtility.MsgKey;
import dao.ReportSK003Dao.HATSKLayout;
import dao.ReportSK003Dao.HATSK_SHNLayout;
import dto.JQEasyModel;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 */
public class ReportSK000Dao extends ItemDao {

	boolean isTest = false;


	/** 最大処理件数 */
	public static int MAX_ROW = 10000;

	/**
	 * インスタンスを生成します。
	 * @param source
	 */
	public ReportSK000Dao(String JNDIname) {
		super(JNDIname);
	}

	/**
	 * ファイルアップロード
	 * @param userInfo
	 *
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws Exception
	 */
	public JQEasyModel upload(HttpSession session, User userInfo, HashMap<String, String> map, String file) {
		// jqEasy 用 JSON モデル作成
		JQEasyModel json = new JQEasyModel();
		JSONObject option = new JSONObject();

		String sysdate = (String)session.getAttribute(Consts.STR_SES_LOGINDT);
		String strMsg = null;

		String sendBtnid	= map.get("SENDBTNID");		// 呼出しボタン
		int countdata		= 0;						// 登録件数

		// 正・予約判断
		boolean isSei = DefineReport.Button.CSV_IMPORT.getObj().equals(sendBtnid);

		// メッセージ情報取得
		MessageUtility mu = new MessageUtility();
		JSONArray errMsgList = new JSONArray();

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
			errMsgList = this.checkFileLayout(dL, idxHeader);

			ReportSK003Dao dao = new ReportSK003Dao(super.JNDIname);

			// CVSトランSEQ情報
			//String seq = this.getCSVSHN_SEQ();

			// データ加工 + 最新情報取得
			ArrayList<JSONObject> dataList = new ArrayList<JSONObject>();
			JSONArray array_hatsk		 = new JSONArray()
					,array_hatsk_shn	 = new JSONArray();

			if(errMsgList.size()==0){
				// データ加工
				for (int i = idxHeader; i < dL.size(); i++) {
					Object[] data = dL.get(i);
					JSONObject obj_hatsk	 = new JSONObject()
							, obj_hatsk_shn	 = new JSONObject();

					String shncd = "";
					for(FileLayout itm :FileLayout.values()){
						String val = StringUtils.trim((String) data[itm.getNo()-1]);
						// TODO
						// 値にダブルコートが入ってしまう。
						val = val.replace("\"", "");
						// 基本情報
						if(itm.getTbl() == RefTable.HATSK){
							obj_hatsk.put(itm.getCol(), val);
						// 新店改装店発注＿商品
						}else if(itm.getTbl() == RefTable.HATSK_SHN){
							obj_hatsk_shn.put(itm.getCol(), val);
						}

						// 一番最後の項目だったら行番号を追加
						if (itm.getCol().equals(FileLayout.UPDDT.getCol())) {
							obj_hatsk.put(HATSKLayout.GYONO.getCol(), i+1);
							obj_hatsk_shn.put(HATSK_SHNLayout.GYONO.getCol(), i+1);
						}
					}
					array_hatsk.add(obj_hatsk);
					array_hatsk_shn.add(obj_hatsk_shn);
				}

				// 入力情報チェック
				List<JSONObject> msgList = new ArrayList<JSONObject>(){};
				List<JSONObject> msgListB = this.checkData(dao, map, userInfo, mu, array_hatsk, array_hatsk_shn);
				JSONArray dataArray = new JSONArray();
				JSONArray dataArray_shn = new JSONArray();

				if(msgListB.size()==0){
					dataArray		 = this.selectHATSK(array_hatsk);
					dataArray_shn	 = this.selectHATSK_SHN(array_hatsk_shn);
					msgList = dao.checkData(map, userInfo, mu, dataArray, dataArray_shn);

				}else{
					msgList.addAll(msgListB);
				}

				boolean isError = msgList.size()!=0;

				if(isError){
					//strMsg = msgList.toString();
					errMsgList.add(msgList);

				}else{
					JSONObject objset = new JSONObject();
					objset.put("DATA", dataArray);						// 対象情報（主要な更新情報）
					objset.put("DATA_SHN", dataArray_shn);				// 対象情報（新店改装店発注＿商品）
					dataList.add(objset);
				}

				/*if(msgList.size()==0){
					// 1.新店改装店発注
					// データをDaoに渡せる形式に変更
					JSONArray dataArray		 = this.selectHATSK(array_hatsk);
					JSONArray dataArray_shn	 = this.selectHATSK_SHN(array_hatsk_shn);
					JSONObject objset = new JSONObject();
					objset.put("DATA", dataArray);						// 対象情報（主要な更新情報）
					objset.put("DATA_SHN", dataArray_shn);				// 対象情報（新店改装店発注＿商品）
					dataList.add(objset);

				}else{
					// エラーデータにエラーメッセージをセット。
					strMsg = msgList.toString();
					dataList.add(msgList.get(0));
				}*/
			}

			int updCount = 0;
			int errCount = 0;

			// *** 詳細情報チェック＋情報登録用SQL作成 ***
			if(errMsgList.size()==0){

				int userId = userInfo.getCD_user();


				// 各テーブル登録用のSQL作成
				for (int i = 0; i < dataList.size(); i++) {

					JSONObject objset = dataList.get(i);

					//String shncd = objset.optString(HATSKLayout.SHNCD.getCol());
					HashMap<String, String> sendmap = new HashMap<String, String>();
					sendmap.put("SENDBTNID", sendBtnid);		// 呼出しボタン

					// 1.新店改装店発注
					JSONArray dataArray = objset.optJSONArray("DATA");
					// 2.新店改装店発注_商品
					JSONArray dataArray_shn = objset.optJSONArray("DATA_SHN");

					countdata = dataArray.size();

					// 主要情報以外のデータを格納
					sendmap.put("DATA_SHN", dataArray_shn.toString());

					// 詳細情報チェック
					List<JSONObject> msgList = new ArrayList<JSONObject>(){};
					msgList = dao.check(sendmap, userInfo, sysdate);

					boolean isError = msgList.size()!=0;
					if(isError){
						errCount++;
					}else{
						updCount++;
					}

					// 登録処理
					if(msgList.size() > 0){
						this.createCommandUpdateData(dao, sendmap, userInfo, dataArray);
					}

					this.sqlList.addAll(dao.sqlList);
					this.prmList.addAll(dao.prmList);
					this.lblList.addAll(dao.lblList);
				}
			}

			// 実行結果のメッセージを取得
			strMsg = this.getMessage();
			if(errMsgList.size()==0){
				JSONObject rtn = this.updateData();

				// 実行トラン情報をJSに戻す
				//option.put(DefineReport.Text.SEQ.getObj(), seq);
				option.put(DefineReport.Text.STATUS.getObj(), "完了");
				option.put(DefineReport.Text.UPD_NUMBER.getObj(), countdata);
				option.put(DefineReport.Text.ERR_NUMBER.getObj(), errCount);
			}else{
				option.put(MsgKey.E.getKey(), strMsg);
			}

			//option.put("result", success);
			json.setOpts(option);


		} catch (FileNotFoundException e) {
			e.printStackTrace();
			errMsgList.add(MessageUtility.getDbMessageIdObj("E20251", new String[]{}));
		} catch (Exception e) {
			e.printStackTrace();
			errMsgList.add(MessageUtility.getDbMessageIdObj("E00014", new String[]{}));
		}
		if(errMsgList.size()!=0){
			option.put(DefineReport.Text.STATUS.getObj(), "失敗");

			option.put(MsgKey.E.getKey(), errMsgList.get(0));
		}
		json.setOpts(option);
		json.setMessage(strMsg);
		return json;
	}

	private List<JSONObject> checkData(ReportSK003Dao dao,HashMap<String, String> map, User userInfo, MessageUtility mu,
			JSONArray dataArray,			// 対象情報（主要な更新情報）
			JSONArray dataArrayShn			// 対象情報（追加更新情報）
			) {

		JSONArray msg	 = new JSONArray();
		Set<String> tencds = new TreeSet<String>();

		if(dataArray.size() > 0){
			JSONObject data	 = new JSONObject();

			for (int j = 0; j < dataArray.size(); j++) {
				data = dataArray.getJSONObject(j);
				if(data.isEmpty()){
					continue;
				}
				if(StringUtils.isNotEmpty(data.optString(FileLayout.TENNO.getCol()))){

					if (tencds.size() == 0) {
						tencds.add(data.optString(FileLayout.TENNO.getCol()));
					} else if (!tencds.contains(data.optString(FileLayout.TENNO.getCol()))) {
						JSONObject o = mu.getDbMessageObj("E11302", new String[]{String.valueOf(j+1)+"行目：店コードの組み合わせ","","(同一ファイル内の複数店舗は不可)"});
						msg.add(o);
						return msg;
					}
				}
			}
		}
		if(dataArrayShn.size() > 0){
			JSONObject data	 = new JSONObject();
			HashSet<String> checkPramsSHNCD_2CHAR = new HashSet<String>();

			for (int j = 0; j < dataArrayShn.size(); j++) {
				data = dataArrayShn.getJSONObject(j);
				if(data.isEmpty()){
					continue;
				}
				if(StringUtils.isNotEmpty(data.optString(FileLayout.SHNCD.getCol()))){
					checkPramsSHNCD_2CHAR.add(data.optString(FileLayout.SHNCD.getCol()).substring(0, 2));
				}
			}
		}
		return msg;
	}

	private JSONArray checkFileLayout(ArrayList<Object[]> eL, int idxHeader) {
		JSONArray msg = new JSONArray();

		// 1.件数チェック
		if(eL.size() <= idxHeader){
			JSONObject o = MessageUtility.getDbMessageIdObj("E40001", new String[]{});
			msg.add(o);
			return msg;
		}else if(eL.size() > MAX_ROW + idxHeader){
			JSONObject o = MessageUtility.getDbMessageIdObj("E11234", new String[]{});
			msg.add(o);
			return msg;
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
			JSONObject o = MessageUtility.getDbMessageIdObj("E40008", new String[]{});
			msg.add(o);
			return msg;
		}
		return msg;
	}


	/** SQLリスト保持用変数 */
	ArrayList<String> sqlList = new ArrayList<String>();
	/** SQLのパラメータリスト保持用変数 */
	ArrayList<ArrayList<String>> prmList = new ArrayList<ArrayList<String>>();
	/** SQLログ用のラベルリスト保持用変数 */
	ArrayList<String> lblList = new ArrayList<String>();

	/**
	 * 更新処理実行
	 * @return
	 *
	 * @throws Exception
	 */
	private JSONObject updateData() throws Exception {

		JSONObject option = new JSONObject();

		ArrayList<Integer> countList  = new ArrayList<Integer>();
		if(sqlList.size() > 0){
			countList =  super.executeSQLs(sqlList, prmList);
		}

		if(StringUtils.isEmpty(getMessage())){
			int count = 0;
			for (int i = 0; i < countList.size(); i++) {
				count += countList.get(i);
				if (DefineReport.ID_DEBUG_MODE) System.out.println(MessageUtility.getMessage(Msg.S00006.getVal(), new String[]{lblList.get(i), Integer.toString(countList.get(i))}));
			}
			if(count==0){
				option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E10000.getVal()));
			}else{
				option.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00001.getVal()));
			}
		}else{
			option.put(MsgKey.E.getKey(), getMessage());
		}

		return option;
	}


	/**
	 * 更新処理実行
	 * @param updCount
	 * @return
	 *
	 * @throws Exception
	 */
	private JSONObject createCommandUpdateData(ReportSK003Dao dao, HashMap<String, String> map, User userInfo,JSONArray	 dataArray) throws Exception {

		JSONObject option = new JSONObject();

		map.put("SENDBTNID", DefineReport.Button.NEW.getObj());		// 呼出しボタンを新規に変更
		//map.put("DATA_SHN", new JSONArray().toString());			// 空データ:新店改装店発注＿商品
		map.put("DATA_SHN_DEL", new JSONArray().toString());		// 空データ:新店改装店発注＿商品削除

		JSONObject data = dataArray.optJSONObject(0);				// 親テーブルの為１件文のデータのみを送る。

		// --- 01.新店改装店発注、新店改装店発注＿商品
		// 基本登録情報
		if(data.size() > 0){
			dao.createSqlSTKTHAT(data,map,userInfo);
		}

		return option;
	}

	/**
	 * CSV取込トラン_商品ヘッダINSERT処理
	 *
	 * @param userId
	 * @param data
	 * @param tbl
	 *
	 * @throws Exception
	 */
	public JSONObject createSqlCSVSHNHEAD(int userId, String sysdate, String seq, String commentkn) {
		JSONObject result = new JSONObject();
		String dbsysdate = CmnDate.dbDateFormat(sysdate);
		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();

		LinkedList<String> valList = new LinkedList<String>();
		valList.add("");	//

		String values = "";
		values += " " + seq;
		values += "," + userId;
		values += ",'" + dbsysdate+"'";
		if(isTest){
			values += ",'" + commentkn+"'";
		}else{
			values += ",?";
			prmData.add(commentkn);
		}

		StringBuffer sbSQL;
		sbSQL = new StringBuffer();
		sbSQL.append("insert into INAMS.CSVSHNHEAD(SEQ, OPERATOR, INPUT_DATE, COMMENTKN)");
		sbSQL.append("values("+values+")");
		if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		this.sqlList.add(sbSQL.toString());
		this.prmList.add(prmData);
		this.lblList.add("CSV取込トラン_商品ヘッダ");
		return result;
	}


	/**
	 * 新店改装店発注情報取得処理
	 *
	 * @param isSei	正/予約
	 * @param isNew	新規/更新
	 * @param data	CSV抜粋データ
	 *
	 * @throws Exception
	 */
	public JSONArray selectHATSK(JSONArray array) {
		JSONObject dataAf = new JSONObject();
		JSONObject dataBf = new JSONObject();
		JSONArray	dataArray	= new JSONArray();	// 対象情報（主要な更新情報）
		//String key = "";
		String regex = "(?i)null";
		Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

		for (int j = 0; j < array.size(); j++) {
			dataBf = array.getJSONObject(j);
			dataAf = new JSONObject();
			if(dataBf.isEmpty()){
				continue;
			}
			for(HATSKLayout itm :HATSKLayout.values()){
				if(dataBf.containsKey(itm.getCol()) && StringUtils.isNotEmpty(dataBf.optString(itm.getCol()))){
					String value = dataBf.getString(itm.getCol());
					//key = "F"+itm.getNo();

					/*if(key.equals("F1")){
						value = "";
					}*/
					// Nullが含まれている場合は置き換える。
					Matcher m = p.matcher(value);
					if (m.find()){
						value = "";
					}

					if(itm.getNo().equals(1)){
						dataAf.put("F1", "");						// F1:更新区分

					}else if(itm.getNo().equals(2)){
						dataAf.put("F2", value);					// F2:店コード

					}else if(itm.getNo().equals(3)){
						dataAf.put("F3", value);					// F3:発注日

					}else if(itm.getNo().equals(4)){
						dataAf.put("F4", value);					// F4:納入日

					}else if(itm.getNo().equals(5)){
						dataAf.put("F5", value);					// F5:商品区分

					}else if(itm.getNo().equals(7)){
						dataAf.put("F7", value);					// F7:別伝区分

					} else if(itm.getNo().equals(13)){
						dataAf.put("F13", value);					// F13:行番号
					}

					// F6:構成ページ
					//dataAf.put("F6", "");


					//dataAf.put(key, value);
				}else{
					//dataAf.put("F"+itm.getCol(), "");
				}
			}
			dataArray.add(dataAf);
		}
		return dataArray;
	}


	/**
	 * 新店改装店発注情報取得処理
	 *
	 * @param isSei	正/予約
	 * @param isNew	新規/更新
	 * @param data	CSV抜粋データ
	 *
	 * @throws Exception
	 */
	public JSONArray selectHATSK_SHN(JSONArray array) {
		JSONObject dataAf = new JSONObject();
		JSONObject dataBf = new JSONObject();
		JSONArray	dataArray	= new JSONArray();	// 対象情報（主要な更新情報）
		//String key = "";
		String regex = "(?i)null";
		Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

		for (int j = 0; j < array.size(); j++) {
			dataBf = array.getJSONObject(j);
			dataAf = new JSONObject();
			if(dataBf.isEmpty()){
				continue;
			}
			for(HATSK_SHNLayout itm :HATSK_SHNLayout.values()){
				if(dataBf.containsKey(itm.getCol()) && StringUtils.isNotEmpty(dataBf.optString(itm.getCol()))){
					String value = dataBf.getString(itm.getCol());

					// Nullが含まれている場合は置き換える。
					Matcher m = p.matcher(value);
					if (m.find()){
						value = "";
					}

					if(itm.getNo().equals(3)){
						dataAf.put("F3", value);					// F3:店コード

					}else if(itm.getNo().equals(4)){
						dataAf.put("F4", value);					// F4:商品コード

					} else if(itm.getNo().equals(9)){
						dataAf.put("F9", value);					// F9:行番号
					}


				}else{
					//dataAf.put("F"+itm.getCol(), "");
				}
			}
			dataArray.add(dataAf);
		}
		return dataArray;
	}

	/**
	 * SEQ情報取得処理
	 *
	 * @throws Exception
	 */
	public String getCSVSHN_SEQ() {
		// 関連情報取得
		ItemList iL = new ItemList();
		String sqlColCommand = "VALUES NEXTVAL FOR INAMS.SEQ004";
		@SuppressWarnings("static-access")
		JSONArray array = iL.selectJSONArray(sqlColCommand, null, Defines.STR_JNDI_DS);
		String value = "";
		if(array.size() > 0){
			value = array.optJSONObject(0).optString("1");
		}
		return value;
	}

	/**
	 * SEQ情報取得処理
	 *
	 * @throws Exception
	 */
	public String getJNLSHN_SEQ_NOW() {
		// 関連情報取得
		ItemList iL = new ItemList();
		String sqlColCommand = "select nvl(max(seq), 0) as SEQ from INAAD.JNLSHN";
		@SuppressWarnings("static-access")
		JSONArray array = iL.selectJSONArray(sqlColCommand, null, Defines.STR_JNDI_DS);
		String value = "";
		if(array.size() > 0){
			value = array.optJSONObject(0).optString("SEQ");
		}
		return value;
	}




	/**  File出力項目の参照テーブル */
	public enum RefTable {
		/** 商品マスタ */
		HATSK(1,"新店改装店発注"),
		/** ソースコード管理マスタ */
		HATSK_SHN(2,"新店改装店発注＿商品"),
		/** 仕入グループ商品 */
		SYSSK(3,"新店改装店発注＿内部管理テーブル");
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
		/** 新店改装店発注 */
		/** 入力№ */
		UPDKBN(1,"更新区分", RefTable.HATSK,"UPDKBN"),
		/** 部門 */
		BMNCD(2,"部門", RefTable.HATSK,"BMNCD"),
		/** 店コード */
		TENNO(3,"店コード", RefTable.HATSK,"TENNO"),
		/** 発注日 */
		HTDT(4,"発注日", RefTable.HATSK,"HTDT"),
		/** 納入日 */
		NNDT(5,"納入日", RefTable.HATSK,"NNDT"),
		/** 商品区分 */
		SHNKBN(6,"商品区分", RefTable.HATSK,"SHNKBN"),
		/** 別伝区分 */
		BDENKBN(7,"別伝区分", RefTable.HATSK,"BDENKBN"),
		/** 商品コード */
		SHNCD(8,"商品コード", RefTable.HATSK_SHN,"SHNCD"),
		/** 数量 */
		SENDFLG(9,"数量", RefTable.HATSK_SHN,"SURYO"),
		/** オペレータ */
		OPERATOR(10,"オペレータ", RefTable.HATSK,"OPERATOR"),
		/** 登録日 */
		ADDDT(11,"登録日", RefTable.HATSK,"ADDDT"),
		/** 更新日 */
		UPDDT(12,"更新日", RefTable.HATSK,"UPDDT");


		private final Integer no;
		private final String txt;
		private final RefTable tbl;
		private final String col;
		/** 初期化 */
		private FileLayout(Integer no, String txt, RefTable tbl, String col) {
			this.no = no;
			this.txt = txt;
			this.tbl = tbl;
			this.col = col;
		}
		/** @return col 列番号 */
		public Integer getNo() { return no; }
		/** @return txt 表示名称 */
		public String getTxt() { return txt; }
		/** @return tbl 参照テーブル */
		public RefTable getTbl() { return tbl; }
		/** @return tbl 参照列 */
		public String getCol() { return col; }
	}
}
