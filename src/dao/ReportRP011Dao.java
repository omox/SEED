/**
 *
 */
package dao;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.JDBCType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import authentication.bean.User;
import authentication.defines.Consts;
import common.CmnDate;
import common.DefineReport;
import common.DefineReport.DataType;
import common.Defines;
import common.FileList;
import common.ItemList;
import common.MessageUtility;
import common.MessageUtility.Msg;
import common.MessageUtility.MsgKey;
import dao.ReportRP006Dao.BUNPAIRTLayout;
import dao.ReportRP006Dao.TOKRTPTNLayout;
import dto.JQEasyModel;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 */
public class ReportRP011Dao extends ItemDao {

	boolean isTest = true;


	/** 最大処理件数 */
	public static int MAX_ROW = 10000;

	/**
	 * インスタンスを生成します。
	 * @param source
	 */
	public ReportRP011Dao(String JNDIname) {
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

		JSONArray msgList = new JSONArray();

		String strMsg = null;
		String report = map.get("report");
		String sysdate = (String)session.getAttribute(Consts.STR_SES_LOGINDT);

		StringBuffer		sbSQL	= new StringBuffer();
		ItemList			iL		= new ItemList();
		JSONArray			dbDatas	= new JSONArray();
		ArrayList<String>	paramData = new ArrayList<String>();
		JSONObject			getData	= new JSONObject();

	    Object[]			valueData	= new Object[]{};
		String				values		= "";
		ArrayList<String>	prmData		= new ArrayList<String>();

		// DB検索用パラメータ
		String sqlWhere		= "";

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
			msgList = this.checkFileLayout(dL, idxHeader);

			if (msgList.size() != 0) {
				this.setMessage(msgList.toString());
				strMsg = this.getMessage();
			}

			// CVSトランSEQ情報
			String seq = this.getCSVTOK_SEQ();

			// データ加工 + 最新情報取得
			ArrayList<JSONObject> dataList = new ArrayList<JSONObject>();
			// CSVを分解、画面上と同じように登録用のデータの形にする
			JSONArray akey = new JSONArray(),aval = new JSONArray();
			JSONObject okey = new JSONObject(), oval = new JSONObject(), ooth = new JSONObject();

			// ファイル内レコード件数
			int cnt = 0;

			// 重複確認用
			Map<String,String> rtPtnMap		= new HashMap<String,String>();
			Map<String,String> newFlieMap	= new HashMap<String,String>();
			HashMap<String,String> kanrinoArr	= new HashMap<String,String>();
			String kanrino	= "";
			String moyskbn	= "";
			String moysstdt	= "";
			String moysrban	= "";
			String moyscd	= "";

			int errCount = 0;
			// ヘッダ登録用SQL作成
			ReportRP006Dao dao = new ReportRP006Dao(super.JNDIname);

			if(StringUtils.isEmpty(strMsg)){
				// 全てのデータに対する単純なチェック

				// データ加工
				for (int i = idxHeader; i < dL.size(); i++) {
					Object[] data = dL.get(i);

					for(FileLayout itm :FileLayout.values()){
						String val = StringUtils.trim((String) data[itm.getNo()-1]);

						// 更新区分はチェック対象外
						if (itm.getCol().equals(FileLayout.UPDKBN.getCol())) {
							continue;

						// 店コード、店名称、分配率
						} else if (itm.getCol().equals(FileLayout.TENCD.getCol()) || itm.getCol().equals(FileLayout.BUNPAIRT.getCol())) {
							oval.put(itm.getCol(),val);
							if (!oval.containsKey(BUNPAIRTLayout.TENKN.getCol())) {
								oval.put(BUNPAIRTLayout.TENKN.getCol(),"");
							}

						// 部門、パターン
						} else {
							okey.put(itm.getCol(),val);
						}

						if (itm.getCol().equals(FileLayout.BUNPAIRT.getCol())) {
							break;
						}
					}
					akey.add(okey);
					aval.add(oval);
					okey = new JSONObject();
					oval = new JSONObject();
				}

				// チェック
				// 詳細情報チェック
				msgList = dao.checkData(
						mu,
						this.selectTOKRTPTN_CK(akey),
						this.selectBUNPAIRT_CK(aval),
						DefineReport.Button.UPLOAD.getObj() // 画面と共通のチェックメソッドを使用している為、UPLOADを設定
				);

				if (msgList.size() != 0) {
					this.setMessage(msgList.toString());
					strMsg = this.getMessage();
				}
			}

			if(StringUtils.isEmpty(strMsg)){
				// 全てのデータに対する単純なチェック

				// 店発注数配列作成の為、データの再作成
				for (int i = idxHeader; i < dL.size(); i++) {
					Object[] data = dL.get(i);

					// keyとなる項目
					String f1	= StringUtils.trim(String.valueOf(data[FileLayout.BMNCD.getNo()-1]));
					String f2	= StringUtils.trim(String.valueOf(data[FileLayout.RTPTNNO.getNo()-1]));

					// valueとなる項目
					String f3	= StringUtils.trim(String.valueOf(data[FileLayout.RTPTNKN.getNo()-1]));
					String f4	= StringUtils.trim(String.valueOf(data[FileLayout.TENCD.getNo()-1]));
					String f5	= StringUtils.trim(String.valueOf(data[FileLayout.BUNPAIRT.getNo()-1]));

					// key:部門-率 val:名称:店-分配率,店-分配率,店-分配率,店-分配率…
					String key = f1 + "-" + f2;
					String value = f3 + ":" + f4 + "-" + f5;
					if (rtPtnMap.containsKey(key)) {
						String getVal = rtPtnMap.get(key);
						// 同一の名称は後勝ち
						if (getVal.split(":")[0].equals(f3)) {
							getVal += "," + f4 + "-" + f5;
						} else {
							getVal = f3 + ":" + getVal.split(":")[1] + "," + f4 + "-" + f5;
						}
						rtPtnMap.replace(key, getVal);
					} else {
						rtPtnMap.put(key, value);
					}
				}

				if (msgList.size() != 0) {
					this.setMessage(msgList.toString());
					strMsg = this.getMessage();
				}
			}

			if(StringUtils.isEmpty(strMsg) && StringUtils.isEmpty(this.getMessage())){

				// key:部門-率-名称 val:店-分配率,店-分配率,店-分配率…
				for(HashMap.Entry<String, String> getKeyVal : rtPtnMap.entrySet()) {
					String key = getKeyVal.getKey();
					String val = getKeyVal.getValue();

					// 店重複チェック
				    Map<String,String> tenRtMap = new HashMap<String,String>();
				    int len = val.split(":")[1].split(",").length;

				    for (int i = 0; i < len; i++) {
				    	String ten = val.split(":")[1].split(",")[i].split("-")[0];
				    	String rt = val.split(":")[1].split(",")[i].split("-")[1];

				    	if (tenRtMap.containsKey(ten)) {
				    		tenRtMap.replace(ten, rt);
				    	} else {
				    		tenRtMap.put(ten, rt);
				    	}
				    }

					if (!newFlieMap.containsKey(key)) {
						key += "-" + val.split(":")[0];
						val = "";
						for(HashMap.Entry<String, String> getTenRt : tenRtMap.entrySet()) {

							if (StringUtils.isEmpty(val)) {
								val = getTenRt.getKey() + "-" + getTenRt.getValue();
							} else {
								val += "," + getTenRt.getKey() + "-" + getTenRt.getValue();
							}
						}
						newFlieMap.put(key, val);
					}
				}

				for(HashMap.Entry<String, String> getKeyVal : newFlieMap.entrySet()) {

					String key = getKeyVal.getKey();
					String val = getKeyVal.getValue();

					// 部門、率、パターンを作成
					okey.put(TOKRTPTNLayout.BMNCD,key.split("-")[0]);
					okey.put(TOKRTPTNLayout.RTPTNNO,key.split("-")[1]);
					okey.put(TOKRTPTNLayout.RTPTNKN,key.split("-")[2]);
					akey.add(okey);

					// 店舗情報作成
				    Map<String,String> tenRtMap = new HashMap<String,String>();
				    int len = val.split(",").length;

				    for (int i = 0; i < len; i++) {
				    	String ten = val.split(",")[i].split("-")[0];
				    	String rt = val.split(",")[i].split("-")[1];

				    	if (tenRtMap.containsKey(ten)) {
				    		tenRtMap.replace(ten, rt);
				    	} else {
				    		tenRtMap.put(ten, rt);
				    	}
				    }

					// 分配率作成
					for (int i = 1; i <= 400; i++) {

						String ten = String.valueOf(i);

						oval.put(BUNPAIRTLayout.TENCD,ten);
						oval.put(BUNPAIRTLayout.TENKN,"");
						if (tenRtMap.containsKey(ten)) {
							oval.put(BUNPAIRTLayout.BUNPAIRT,tenRtMap.get(ten));
						} else {
							oval.put(BUNPAIRTLayout.BUNPAIRT,"0");
						}
						aval.add(oval);
					}

					// エラーが一件も存在しない場合更新処理を実行
					// 最新の情報取得
					// データ取得先判断
					JSONObject rt = this.createCommandUpdateData(
							dao,
							userInfo,
							this.selectTOKRTPTN(akey),
							this.selectBUNPAIRT(aval),
							sysdate
					);

					this.sqlList.addAll(dao.sqlList);
					this.prmList.addAll(dao.prmList);
					this.lblList.addAll(dao.lblList);

					okey = new JSONObject();
					oval = new JSONObject();
					akey = new JSONArray();
					aval = new JSONArray();
				}
			}

			if (!StringUtils.isEmpty(this.getMessage())) {
				strMsg = this.getMessage();
			} else if (StringUtils.isEmpty(strMsg)) {
				JSONObject rtn = this.updateData();

				if (rtn.containsKey(MsgKey.E.getKey())) {
					strMsg = rtn.get(MsgKey.E.getKey()).toString();
				} else {
					strMsg = this.getMessage();
				}

				// ヘッダ登録用SQL作成
				int userId = userInfo.getCD_user();
				String szCommentkn	= "";		// コメント
				String datakind = "";			// データ種別	// TODO
				JSONObject hdrtn = this.createSqlCSVTOKHEAD(userId, sysdate, seq, szCommentkn, datakind);
			}

			// 更新成功
			String status = "処理";

			if (errCount == 0 && StringUtils.isEmpty(strMsg)) {
				status += "が終了しました";
				cnt = dL.size();
			} else {
				status += "を中断しました";
				cnt = 0;
			}

			// 実行トラン情報をJSに戻す
			option.put(DefineReport.Text.SEQ.getObj(), "");
			option.put(DefineReport.Text.STATUS.getObj(), status);
			option.put(DefineReport.Text.UPD_NUMBER.getObj(), cnt);
			option.put(DefineReport.Text.ERR_NUMBER.getObj(), errCount);

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

		if (msgList.size() != 0) {
			option.put(DefineReport.Text.STATUS.getObj(), "失敗");
			option.put(MsgKey.E.getKey(), msgList.get(0));
		}

		// 実行結果のメッセージを設定
		json.setMessage(strMsg);
		return json;
	}

	/**
	 * CSV取込トラン_特売ヘッダINSERT処理
	 *
	 * @param userId
	 * @param data
	 * @param tbl
	 *
	 * @throws Exception
	 */
	public JSONObject createSqlCSVTOKHEAD(int userId, String sysdate, String seq, String commentkn, String datakind) {
		JSONObject result = new JSONObject();
		String dbsysdate = CmnDate.dbDateFormat(sysdate);
		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();

		LinkedList<String> valList = new LinkedList<String>();
		valList.add("");	//

		String values = "";
		values += seq;							// F1 : SEQ
		if(isTest){
			values += ",'" + datakind+"'";		// F2 : データ種別
		}else{
			values += ",?";
			prmData.add(datakind);
		}
		values += "," + userId;					// F3 : オペレータ
		values += ",'" + dbsysdate+"'";			// F4 : 取込日時
		if(isTest){
			values += ",'" + commentkn+"'";		// F5 : コメント
		}else{
			values += ",?";
			prmData.add(commentkn);
		}

		StringBuffer sbSQL;
		sbSQL = new StringBuffer();
		sbSQL.append("insert into INATK.CSVTOKHEAD(SEQ, DATAKIND, OPERATOR, INPUT_DATE, COMMENTKN)");
		sbSQL.append("values("+values+")");
		if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		this.sqlList.add(sbSQL.toString());
		this.prmList.add(prmData);
		this.lblList.add("CSV取込トラン_特売ヘッダ");
		return result;
	}

	private JSONArray checkFileLayout(ArrayList<Object[]> eL, int idxHeader) {

		JSONArray msg = new JSONArray();

		// 1.件数チェック
		if(eL.size() <= idxHeader){
			//return "ファイルにデータがありません。";
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
	private JSONObject createCommandUpdateData(
			ReportRP006Dao dao, User userInfo,
			JSONArray dataArray, JSONArray dataArrayRtptn, String sysdate
		) throws Exception {

		JSONObject option = new JSONObject();

		// パラメータ確認
		JSONObject result = dao.updateData(dataArray, dataArrayRtptn, userInfo, sysdate);

		return option;
	}

	/**
	 * 子テーブルマスタ共通SQL作成処理
	 *
	 * @param dataArray	CSV抜粋データ
	 * @param layout	マスタレイアウト
	 * @return
	 *
	 * @throws Exception
	 */
	private String setSelectCommandMST(JSONArray dataArray, MSTLayout[] layouts, ArrayList<String> prmData) {
		String values = "", names = "", rows = "";
		for(int j=0; j < dataArray.size(); j++){
			values = "";
			names = "";
			for(MSTLayout itm : layouts){
				String col = itm.getCol();
				String val = dataArray.optJSONObject(j).optString(col);

				if(StringUtils.isEmpty(val)){
					values += ", null";
				}else{
					if(isTest){
						values += ", '"+val+"'";
					}else{
						prmData.add(val);
						values += ", ?";
					}
				}
				names  += ", "+col;
			}
			rows += ",("+StringUtils.removeStart(values, ",")+")";
		}
		rows = StringUtils.removeStart(rows, ",");
		names = StringUtils.removeStart(names, ",");

		// 共通SQL
		StringBuffer sbSQLIn = new StringBuffer();
		sbSQLIn.append(" select ");
		for(MSTLayout itm :layouts){
			if(itm.getNo() > 1){ sbSQLIn.append(","); }
			if(itm.isText()){
				sbSQLIn.append(itm.getCol() + " as "+itm.getCol());
			}else{
				sbSQLIn.append("cast("+ itm.getCol() + " as " + itm.getTyp()  + ") as "+itm.getCol());
			}
		}
		sbSQLIn.append(" from (values"+rows+") as T1("+names+")");

		// 基本Select文
		StringBuffer sbSQL = new StringBuffer();
		sbSQL.append(" select ");
		for(MSTLayout itm :layouts){
			if(itm.getNo() > 1){ sbSQL.append(","); }
			sbSQL.append("RE."+itm.getCol() + " as "+itm.getId());
		}
		sbSQL.append(" from (" + sbSQLIn.toString() + ") RE");

		return sbSQL.toString();
	}

	/**
	 * 子テーブルマスタ共通SQL作成処理
	 *
	 * @param dataArray	CSV抜粋データ
	 * @param layout	マスタレイアウト
	 * @return
	 *
	 * @throws Exception
	 */
	private String checkSelectCommandMST(JSONArray dataArray, MSTLayout[] layouts, ArrayList<String> prmData) {
		String values = "", names = "", rows = "";
		for(int j=0; j < dataArray.size(); j++){
			values = "";
			names = "";
			for(MSTLayout itm : layouts){
				String col = itm.getCol();
				String val = dataArray.optJSONObject(j).optString(col);

				if(StringUtils.isEmpty(val)){
					values += ", null";
				}else{
					if(isTest){
						values += ", '"+val+"'";
					}else{
						prmData.add(val);
						values += ", ?";
					}
				}
				names  += ", "+col;
			}
			rows += ",("+StringUtils.removeStart(values, ",")+")";
		}
		rows = StringUtils.removeStart(rows, ",");
		names = StringUtils.removeStart(names, ",");

		// 共通SQL
		StringBuffer sbSQLIn = new StringBuffer();
		sbSQLIn.append(" select ");
		for(MSTLayout itm :layouts){
			if(itm.getNo() > 1){ sbSQLIn.append(","); }
			sbSQLIn.append(itm.getCol() + " as "+itm.getCol());
		}
		sbSQLIn.append(" from (values"+rows+") as T1("+names+")");

		// 基本Select文
		StringBuffer sbSQL = new StringBuffer();
		sbSQL.append(" select ");
		for(MSTLayout itm :layouts){
			if(itm.getNo() > 1){ sbSQL.append(","); }
			sbSQL.append("RE."+itm.getCol() + " as "+itm.getId());
		}
		sbSQL.append(" from (" + sbSQLIn.toString() + ") RE");

		return sbSQL.toString();
	}

	/**
	 * 通常率登録チェック
	 *
	 * @param isSei	正/予約
	 * @param isNew	新規/更新
	 * @param data	CSV抜粋データ
	 *
	 * @throws Exception
	 */
	private JSONArray selectTOKRTPTN_CK(JSONArray dataArray) {
		ArrayList<String> prmData = new ArrayList<>();
		String sqlCommand = this.checkSelectCommandMST(dataArray, TOKRTPTNLayout.values(), prmData);
		if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sqlCommand);

		ItemList iL = new ItemList();
		@SuppressWarnings("static-access")
		JSONArray rtnArray = iL.selectJSONArray(sqlCommand, prmData, Defines.STR_JNDI_DS);
		return rtnArray;
	}

	/**
	 * 通常率登録
	 *
	 * @param isSei	正/予約
	 * @param isNew	新規/更新
	 * @param data	CSV抜粋データ
	 *
	 * @throws Exception
	 */
	private JSONArray selectTOKRTPTN(JSONArray dataArray) {
		ArrayList<String> prmData = new ArrayList<>();
		String sqlCommand = this.setSelectCommandMST(dataArray, TOKRTPTNLayout.values(), prmData);
		if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sqlCommand);

		ItemList iL = new ItemList();
		@SuppressWarnings("static-access")
		JSONArray rtnArray = iL.selectJSONArray(sqlCommand, prmData, Defines.STR_JNDI_DS);
		return rtnArray;
	}

	/**
	 * 分配率登録チェック
	 *
	 * @param isSei	正/予約
	 * @param isNew	新規/更新
	 * @param data	CSV抜粋データ
	 *
	 * @throws Exception
	 */
	private JSONArray selectBUNPAIRT_CK(JSONArray dataArray) {
		ArrayList<String> prmData = new ArrayList<>();
		String sqlCommand = this.checkSelectCommandMST(dataArray, BUNPAIRTLayout.values(), prmData);
		if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sqlCommand);

		ItemList iL = new ItemList();
		@SuppressWarnings("static-access")
		JSONArray rtnArray = iL.selectJSONArray(sqlCommand, prmData, Defines.STR_JNDI_DS);
		return rtnArray;
	}

	/**
	 * 分配率登録
	 *
	 * @param isSei	正/予約
	 * @param isNew	新規/更新
	 * @param data	CSV抜粋データ
	 *
	 * @throws Exception
	 */
	private JSONArray selectBUNPAIRT(JSONArray dataArray) {
		ArrayList<String> prmData = new ArrayList<>();
		String sqlCommand = this.setSelectCommandMST(dataArray, BUNPAIRTLayout.values(), prmData);
		if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sqlCommand);

		ItemList iL = new ItemList();
		@SuppressWarnings("static-access")
		JSONArray rtnArray = iL.selectJSONArray(sqlCommand, prmData, Defines.STR_JNDI_DS);
		return rtnArray;
	}

	/**
	 * SEQ情報取得処理
	 *
	 * @throws Exception
	 */
	public String getCSVTOK_SEQ() {
		// 関連情報取得
		ItemList iL = new ItemList();
		String sqlColCommand = "VALUES NEXTVAL FOR INAMS.SEQ010";
		@SuppressWarnings("static-access")
		JSONArray array = iL.selectJSONArray(sqlColCommand, null, Defines.STR_JNDI_DS);
		String value = "";
		if(array.size() > 0){
			value = array.optJSONObject(0).optString("1");
		}
		return value;
	}

	/**  File出力項目の参照テーブル */
	public enum RefTable {
		/** 通常率パターン */
		RTPTN(1,"通常率パターン");
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
		/** 通常率パターン */
		/** 入力№ */
		UPDKBN(1,"更新区分", RefTable.RTPTN,"UPDKBN", "CHARACTER(1)"),
		/** 部門 */
		BMNCD(2,"部門", RefTable.RTPTN,"BMNCD", "SMALLINT"),
		/** パターンNo. */
		RTPTNNO(3,"パターンNo.", RefTable.RTPTN,"RTPTNNO", "CHARACTER(3)"),
		/** パターン名称 */
		RTPTNKN(4,"パターン名称", RefTable.RTPTN,"RTPTNKN", "CHARACTER(40)"),
		/** 店コード */
		TENCD(5,"店コード", RefTable.RTPTN,"TENCD", "SMALLINT"),
		/** 分配率 */
		BUNPAIRT(6,"分配率", RefTable.RTPTN,"BUNPAIRT", "SMALLINT"),
		/** オペレータ */
		OPERATOR(7,"オペレータ", RefTable.RTPTN,"OPERATOR", "CHARACTER(20)"),
		/** 登録日 */
		ADDDT(8,"登録日", RefTable.RTPTN,"ADDDT", "INTEGER"),
		/** 更新日 */
		UPDDT(9,"更新日", RefTable.RTPTN,"UPDDT", "INTEGER");

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
