/**
 *
 */
package dao;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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
import dao.ReportBM006Dao.CSVTOKLayout;
import dao.ReportBM006Dao.TOKBMLayout;
import dao.ReportBM006Dao.TOKBMSHNLayout;
import dao.ReportBM006Dao.TOKBMTJTENLayout;
import dto.JQEasyModel;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 */
public class ReportBM007Dao extends ItemDao {

	/** 最大処理件数 */
	public static int MAX_ROW = 200;

	/**
	 * インスタンスを生成します。
	 * @param source
	 */
	public ReportBM007Dao(String JNDIname) {
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
		String szCommentkn	= map.get(DefineReport.InpText.COMMENTKN.getObj());		// コメント

		String seq = "";
		String status = "";
		int errCount = 0;
		int cnt = 0;

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

			// 実行結果のメッセージを設定
			if (errMsgList.size()!=0) {

				// 実行トラン情報をJSに戻す
				option.put(DefineReport.Text.SEQ.getObj(), "-1");
				option.put(DefineReport.Text.STATUS.getObj(), "");
				option.put(DefineReport.Text.UPD_NUMBER.getObj(), "");
				option.put(DefineReport.Text.ERR_NUMBER.getObj(), "");
				json.setOpts(option);

				String id = errMsgList.optJSONObject(0).getString(MessageUtility.ID);
				strMsg = mu.getDbMessageObj(id, new String[]{}).get(MessageUtility.MSG).toString();
				json.setMessage(strMsg);
				return json;
			}

			// CVSトランSEQ情報
			seq = this.getCSVTOK_SEQ();

			// データ加工 + 最新情報取得
			ArrayList<JSONObject> dataList = new ArrayList<JSONObject>();
			// CSVを分解、画面上と同じように登録用のデータの形にする
			JSONArray atokbm = new JSONArray(), atokbmtj = new JSONArray(), atokbmshn = new JSONArray();
			JSONObject otokbm = new JSONObject(), otokbmtj = new JSONObject(), otokbmshn = new JSONObject(), ooth = new JSONObject();
			Set<String> mapMoysCd = new TreeSet<String>();

			if(strMsg==null){

				// データ加工
				for (int i = idxHeader; i < dL.size(); i++) {
					Object[] data = dL.get(i);

					String inputno = Integer.toString(i+1);

					String moyskbn	= "";
					String moysstdt	= "";
					String moysrban	= "";

					for(FileLayout itm :FileLayout.values()){
						String val = StringUtils.trim((String) data[itm.getNo()-1]);

						// 1.""で囲まれていた場合除去 2.末尾空白除去
						val = StringUtils.stripEnd(val.replaceFirst("^\"(.*)\"$", "$1"), null);

						if(itm.getTbl() == RefTable.TOKBM){
							if(otokbm.containsKey(itm.getCol())){
								atokbm.add(otokbm);
								otokbm = new JSONObject();
							}
							if (itm.getCol().equals(TOKBMLayout.MOYSCD.getCol())) {

								// 催しコードが複数存在する場合
								if (mapMoysCd.size() != 0 && !mapMoysCd.contains(val)) {
									// 実行トラン情報をJSに戻す
									option.put(DefineReport.Text.SEQ.getObj(), "-1");
									option.put(DefineReport.Text.STATUS.getObj(), "");
									option.put(DefineReport.Text.UPD_NUMBER.getObj(), "");
									option.put(DefineReport.Text.ERR_NUMBER.getObj(), "");
									json.setOpts(option);

									JSONObject o = mu.getDbMessageObj("E40004", new String[]{});
									strMsg = o.optString(MessageUtility.MSG);

									json.setMessage(strMsg);
									return json;
								} else if (!mapMoysCd.contains(val)) {
									mapMoysCd.add(val);
								}

								if (val.length() > 7) {
									moyskbn		= val.substring(0, 1);
									moysstdt	= val.substring(1, 7);
									moysrban	= val.substring(7);
									val = String.format("%01d", Integer.valueOf(val.substring(0, 1))) +
											String.format("%06d", Integer.valueOf(val.substring(1, 7))) +
											String.format("%03d", Integer.valueOf(val.substring(7)));
								}
								otokbm.put(itm.getCol(), val);
								otokbm.put(TOKBMLayout.MOYSKBN.getCol(), moyskbn);
								otokbm.put(TOKBMLayout.MOYSSTDT.getCol(), moysstdt);
								otokbm.put(TOKBMLayout.MOYSRBAN.getCol(), moysrban);
							} else {
								otokbm.put(itm.getCol(), val);
							}
						} else if(itm.getTbl() == RefTable.TOKBM_SHN){
							if(otokbmshn.containsKey(itm.getCol())){
								atokbmshn.add(otokbmshn);
								otokbmshn = new JSONObject();
							}

							otokbmshn.put(itm.getCol(), val);
						} else if(itm.getTbl() == RefTable.TOKBM_TJTEN){
							if(otokbmtj.containsKey(itm.getCol())){
								atokbmtj.add(otokbmtj);
								otokbmtj = new JSONObject();
							}
							otokbmtj.put(itm.getCol(), val);
						} else if(itm.getTbl() == RefTable.OTHER){
							ooth.put(itm.getCol(), val);
						}
					}
					atokbm.add(otokbm);
					atokbmshn.add(otokbmshn);
					atokbmtj.add(otokbmtj);
					otokbm = new JSONObject();
					otokbmshn = new JSONObject();
					otokbmtj = new JSONObject();

					// 4.その他
					ooth.put(CSVTOKLayout.SEQ.getCol(), seq);				// CSV用.SEQ
					ooth.put(CSVTOKLayout.INPUTNO.getCol(), inputno);		// CSV用.入力番号
				}
			}

			int updCount = 0;
			errCount = 0;

			// *** 詳細情報チェック＋情報登録用SQL作成 ***
			if(strMsg==null){

				String userId = userInfo.getId();

				// ヘッダ登録用SQL作成
				JSONObject hdrtn = this.createSqlCSVTOKHEAD(userId, sysdate, seq, szCommentkn);
				ReportBM006Dao dao = new ReportBM006Dao(super.JNDIname);

				// 詳細情報チェック
				List<JSONObject> msgList = new ArrayList<JSONObject>(){};
				msgList = dao.checkData(
						mu,
						this.selectTOKBM(atokbm,true),
						this.selectTOKBMSHN(atokbmshn,true),
						this.selectTOKBMTJ(atokbmtj,true),
						ooth				// その他
				);

				boolean isError = msgList.size()!=0;

				// CSV用情報セット
				String csvUpdkbn = DefineReport.ValCsvUpdkbn.NEW.getVal();
				dao.csvtok_add_data[CSVTOKLayout.SEQ.getNo()-1] = ooth.optString(CSVTOKLayout.SEQ.getCol());			// CSV用.SEQ
				dao.csvtok_add_data[CSVTOKLayout.INPUTNO.getNo()-1] = ooth.optString(CSVTOKLayout.INPUTNO.getCol());	// CSV用.入力番号
				dao.csvtok_add_data[CSVTOKLayout.CSV_UPDKBN.getNo()-1] = csvUpdkbn;	// CSV_UPDKBN

				if(isError){
					dao.csvtok_add_data[CSVTOKLayout.ERRCD.getNo()-1]		= msgList.get(0).optString(CSVTOKLayout.ERRCD.getCol());		// ERRCD
					dao.csvtok_add_data[CSVTOKLayout.ERRFLD.getNo()-1]		= msgList.get(0).optString(CSVTOKLayout.ERRFLD.getCol());	// ERRFLD
					dao.csvtok_add_data[CSVTOKLayout.ERRVL.getNo()-1]		= msgList.get(0).optString(CSVTOKLayout.ERRVL.getCol());		// ERRVL
					dao.csvtok_add_data[CSVTOKLayout.ERRTBLNM.getNo()-1]	= msgList.get(0).optString(CSVTOKLayout.ERRTBLNM.getCol());	// ERRTBLNM
					dao.csvtok_add_data[CSVTOKLayout.INPUTNO.getNo()-1]		= msgList.get(0).optString(CSVTOKLayout.INPUTNO.getCol());	// ERRTBLNM

					dao.csvtok_add_data[CSVTOKLayout.MOYSKBN.getNo()-1]		= msgList.get(0).optString(CSVTOKLayout.MOYSKBN.getCol());	// MOYSKBN
					dao.csvtok_add_data[CSVTOKLayout.MOYSSTDT.getNo()-1]	= msgList.get(0).optString(CSVTOKLayout.MOYSSTDT.getCol());	// MOYSSTDT
					dao.csvtok_add_data[CSVTOKLayout.MOYSRBAN.getNo()-1]	= msgList.get(0).optString(CSVTOKLayout.MOYSRBAN.getCol());	// MOYSRBAN
					dao.csvtok_add_data[CSVTOKLayout.BMNNO.getNo()-1]		= msgList.get(0).optString(CSVTOKLayout.BMNNO.getCol());	// BMNNO
					dao.csvtok_add_data[CSVTOKLayout.BMNMAN.getNo()-1]		= msgList.get(0).optString(CSVTOKLayout.BMNMAN.getCol());	// BMNMAN
					dao.csvtok_add_data[CSVTOKLayout.HBSTDT.getNo()-1]		= msgList.get(0).optString(CSVTOKLayout.HBSTDT.getCol());	// HBSTDT
					dao.csvtok_add_data[CSVTOKLayout.HBEDDT.getNo()-1]		= msgList.get(0).optString(CSVTOKLayout.HBEDDT.getCol());	// HBEDDT
					dao.csvtok_add_data[CSVTOKLayout.SHNCD.getNo()-1]		= msgList.get(0).optString(CSVTOKLayout.SHNCD.getCol());	// SHNCD

					errCount++;
					this.setMessage(msgList.toString());

					// CSV取込トラン_催し別送信情報更新
					JSONObject bmrtn = this.createSqlCSVTOKBM(userId, sysdate, dao);
				}
			}

			// エラーが一件も存在しない場合更新処理を実行
			if (strMsg == null && errCount == 0) {

				// CSVを分解、画面上と同じように登録用のデータの形にする
				atokbm = new JSONArray();
				atokbmtj = new JSONArray();
				atokbmshn = new JSONArray();
				otokbm = new JSONObject();
				otokbmtj = new JSONObject();
				otokbmshn = new JSONObject();

				String moyskbn	= "";
				String moysstdt	= "";
				String moysrban	= "";

				// データ加工
				for (int i = idxHeader; i < dL.size(); i++) {

					Object[] data = dL.get(i);

					for(FileLayout itm :FileLayout.values()){
						String val = StringUtils.trim((String) data[itm.getNo()-1]);

						// 1.""で囲まれていた場合除去 2.末尾空白除去
						val = StringUtils.stripEnd(val.replaceFirst("^\"(.*)\"$", "$1"), null);

						if(itm.getTbl() == RefTable.TOKBM && i == 0){
							if(otokbm.containsKey(itm.getCol())){
								atokbm.add(otokbm);
								otokbm = new JSONObject();
							}
							if (itm.getCol().equals(TOKBMLayout.MOYSCD.getCol())) {
								if (val.length() > 7) {
									moyskbn		= val.substring(0, 1);
									moysstdt	= val.substring(1, 7);
									moysrban	= val.substring(7);
									val = String.format("%01d", Integer.valueOf(val.substring(0, 1))) +
											String.format("%06d", Integer.valueOf(val.substring(1, 7))) +
											String.format("%03d", Integer.valueOf(val.substring(7)));
								}
								otokbm.put(itm.getCol(), val);
								otokbm.put(TOKBMLayout.MOYSKBN.getCol(), moyskbn);
								otokbm.put(TOKBMLayout.MOYSSTDT.getCol(), moysstdt);
								otokbm.put(TOKBMLayout.MOYSRBAN.getCol(), moysrban);
							} else {
								otokbm.put(itm.getCol(), val);
							}
						} else if(itm.getTbl() == RefTable.TOKBM_SHN){
							if(otokbmshn.containsKey(itm.getCol())){
								atokbmshn.add(otokbmshn);
								otokbmshn = new JSONObject();
							}

							otokbmshn.put(itm.getCol(), val);
						} else if(itm.getTbl() == RefTable.TOKBM_TJTEN  && i == 0){
							if(otokbmtj.containsKey(itm.getCol())){
								atokbmtj.add(otokbmtj);
								otokbmtj = new JSONObject();
							}
							otokbmtj.put(itm.getCol(), val);
						}
					}

					if (i == 0) {
						atokbm.add(otokbm);
						atokbmtj.add(otokbmtj);
					}
					atokbmshn.add(otokbmshn);
					otokbmshn = new JSONObject();
				}

				// 最新の情報取得
				// データ取得先判断
				ReportBM006Dao dao = new ReportBM006Dao(super.JNDIname);

				JSONObject rt = this.createCommandUpdateData(
						dao,
						userInfo,
						this.selectTOKBM(atokbm,false),
						this.selectTOKBMSHN(atokbmshn,false),
						this.selectTOKBMTJ(atokbmtj,false)
				);

				this.sqlList.addAll(dao.sqlList);
				this.prmList.addAll(dao.prmList);
				this.lblList.addAll(dao.lblList);
			}

			if (strMsg == null) {
				JSONObject rtn = this.updateData();

				if (rtn.containsKey(MsgKey.E.getKey())) {
					strMsg = rtn.get(MsgKey.E.getKey()).toString();
				} else {
					strMsg = this.getMessage();
				}
			}

			// 更新成功
			status = "処理";
			cnt = atokbmshn.size();

			if (errCount == 0 && StringUtils.isEmpty(strMsg)) {
				status += "が終了しました";
			} else {
				status += "を中断しました";
				cnt = 0;
			}

			// 実行トラン情報をJSに戻す
			option.put(DefineReport.Text.SEQ.getObj(), seq);
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
		// 実行結果のメッセージを設定
		json.setMessage(strMsg);
		return json;
	}

	/*
	private List<JSONObject> checkData(ReportBM006Dao dao, HashMap<String, String> map, User userInfo, String sysdate1, MessageUtility mu,
			JSONArray dataArray,			// BM催し送信
			JSONArray dataArraySHN,			// BM催し送信_商品
			JSONArray dataArrayTJ,			// BM催し送信_対象除外店
			JSONObject dataOther			// その他情報
			) {
		JSONArray msg = new JSONArray();

		// DB最新情報再取得
		JSONObject seiJsonObject = new JSONObject();

		String ErrTblNm = "CSVファイル";

		JSONObject data = dataArray.optJSONObject(0);

		return msg;
	}
	*/

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
				System.out.println(MessageUtility.getMessage(Msg.S00006.getVal(), new String[]{lblList.get(i), Integer.toString(countList.get(i))}));
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
			ReportBM006Dao dao, User userInfo,
			JSONArray dataArray,	// BM催し送信
			JSONArray dataArrayShn,	// BM催し送信_商品
			JSONArray dataArrayTj	// BM催し送信_対象除外店
		) throws Exception {

		JSONObject option = new JSONObject();

		// パラメータ確認
		JSONObject data = dataArray.optJSONObject(0);
		JSONObject dataTj = dataArrayTj.optJSONObject(0);

		JSONObject result = dao.createSqlBmCsv(data, dataArrayShn, dataTj, userInfo);

		return option;
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
	public JSONObject createSqlCSVTOKHEAD(String userId, String sysdate, String seq, String commentkn) {
		JSONObject result = new JSONObject();

		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();

		LinkedList<String> valList = new LinkedList<String>();
		valList.add("");	//

		String values = "";
		values += " " + seq;
		values += ",'BM'";
		values += ",?";
		prmData.add(userId);
		values += ",current timestamp ";

		if (StringUtils.isEmpty(commentkn)) {
			values += ",null";
		} else {
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

	/**
	 * CSV取込トラン_BM催し送信INSERT処理
	 *
	 * @param userId
	 * @param data
	 * @param tbl
	 *
	 * @throws Exception
	 */
	public JSONObject createSqlCSVTOKBM(String userId, String sysdate, ReportBM006Dao dao) {
		JSONObject result = new JSONObject();
		String dbsysdate = CmnDate.dbDateFormat(sysdate);
		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();

		LinkedList<String> valList = new LinkedList<String>();
		valList.add("");	//

		String values = "";
		for(CSVTOKLayout itm :CSVTOKLayout.values()){
			String val = ((String) dao.csvtok_add_data[itm.getNo()-1]);

			if (StringUtils.isEmpty(val)) {
				values += " null,";
			} else {
				values += " ?,";
				prmData.add(val);
			}
		}
		values += "?,?";
		prmData.add(userId);
		prmData.add(dbsysdate);

		StringBuffer sbSQL;
		sbSQL = new StringBuffer();
		sbSQL.append("insert into INATK.CSVTOK_BM(");
		sbSQL.append("SEQ,");
		sbSQL.append("INPUTNO,");
		sbSQL.append("ERRCD,");
		sbSQL.append("ERRFLD,");
		sbSQL.append("ERRVL,");
		sbSQL.append("ERRTBLNM,");
		sbSQL.append("CSV_UPDKBN,");
		sbSQL.append("MOYSKBN,");
		sbSQL.append("MOYSSTDT,");
		sbSQL.append("MOYSRBAN,");
		sbSQL.append("BMNNO,");
		sbSQL.append("BMNMAN,");
		sbSQL.append("HBSTDT,");
		sbSQL.append("HBEDDT,");
		sbSQL.append("SHNCD,");
		sbSQL.append("OPERATOR,");
		sbSQL.append("ADDDT)");
		sbSQL.append("values("+values+")");
		if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		this.sqlList.add(sbSQL.toString());
		this.prmList.add(prmData);
		this.lblList.add("CSV取込トラン_BM催し送信");
		return result;
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
					prmData.add(val);
					values += ", cast(? as varchar("+MessageUtility.getDefByteLen(val)+"))";
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
					prmData.add(val);
					values += ", cast(? as varchar("+MessageUtility.getDefByteLen(val)+"))";
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
	 * BM催し送信情報取得処理
	 *
	 * @param isSei	正/予約
	 * @param isNew	新規/更新
	 * @param data	CSV抜粋データ
	 *
	 * @throws Exception
	 */
	private JSONArray selectTOKBM(JSONArray dataArray ,boolean checkFlg) {
		ArrayList<String> prmData = new ArrayList<>();
		String sqlCommand = "";
		if (checkFlg) {
			sqlCommand = this.checkSelectCommandMST(dataArray, TOKBMLayout.values(), prmData);
		} else {
			sqlCommand = this.setSelectCommandMST(dataArray, TOKBMLayout.values(), prmData);
		}
		if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sqlCommand);

		ItemList iL = new ItemList();
		@SuppressWarnings("static-access")
		JSONArray rtnArray = iL.selectJSONArray(sqlCommand, prmData, Defines.STR_JNDI_DS);
		return rtnArray;
	}

	/**
	 * BM催し送信_商品情報取得処理
	 *
	 * @param isSei	正/予約
	 * @param isNew	新規/更新
	 * @param data	CSV抜粋データ
	 *
	 * @throws Exception
	 */
	private JSONArray selectTOKBMSHN(JSONArray dataArray, boolean checkFlg) {
		ArrayList<String> prmData = new ArrayList<>();
		String sqlCommand = "";
		if (checkFlg) {
			sqlCommand = this.checkSelectCommandMST(dataArray, TOKBMSHNLayout.values(), prmData);
		} else {
			sqlCommand = this.setSelectCommandMST(dataArray, TOKBMSHNLayout.values(), prmData);
		}

		if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sqlCommand);

		ItemList iL = new ItemList();
		@SuppressWarnings("static-access")
		JSONArray rtnArray = iL.selectJSONArray(sqlCommand, prmData, Defines.STR_JNDI_DS);
		return rtnArray;
	}

	/**
	 * BM催し送信_対象除外店情報取得処理
	 *
	 * @param data	CSV抜粋データ
	 *
	 * @throws Exception
	 */
	private JSONArray selectTOKBMTJ(JSONArray dataArray, boolean checkFlg) {
		ArrayList<String> prmData = new ArrayList<>();
		String sqlCommand = "";
		if (checkFlg) {
			sqlCommand = this.checkSelectCommandMST(dataArray, TOKBMTJTENLayout.values(), prmData);
		} else {
			sqlCommand = this.setSelectCommandMST(dataArray, TOKBMTJTENLayout.values(), prmData);
		}

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

		/** BM催し送信 */
		TOKBM(1,"BM催し送信"),
		/** BM催し送信_商品 */
		TOKBM_SHN(2,"BM催し送信_商品"),
		/** BM催し送信_対象除外店 */
		TOKBM_TJTEN(3,"BM催し送信_対象除外店"),
		/** その他 */
		OTHER(4,"その他");
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
		/** 更新区分 */
		UPDKBN(1,"更新区分",RefTable.OTHER,"UPDKBN"),
		MOYSCD(2,"催しコード",RefTable.TOKBM,"MOYSCD"),
		/*
		MOYSKBN(2,"催し区分",RefTable.TOKBM,"MOYSKBN"),
		MOYSSTDT(3,"催し開始日",RefTable.TOKBM,"MOYSSTDT"),
		MOYSRBAN(4,"催し連番",RefTable.TOKBM,"MOYSRBAN"),
		*/
		MOYKN(3,"催し名称（漢字）",RefTable.TOKBM,"MOYKN"),
		MOYSHBSTDT(4,"催し販売期間_開始日",RefTable.OTHER,"MOYSHBSTDT"),
		MOYSHBEDDT(5,"催し販売期間_終了日",RefTable.OTHER,"MOYSHBEDDT"),
		TENGPNO(6,"店グループ番号",RefTable.OTHER,"TENGPNO"),
		RTENNO(7,"リーダー店番号",RefTable.OTHER,"RTENNO"),
		DELAYPTN(8,"遅れパターンあり",RefTable.OTHER,"DELAYPTN"),
		BMNNO(9,"グループ番号",RefTable.TOKBM,"BMNNO"),
		BMNMAN(10,"BM名称（ｶﾅ）",RefTable.TOKBM,"BMNMAN"),
		BMNMKN(11,"BM名称（漢字）",RefTable.TOKBM,"BMNMKN"),
		BAIKAAM(12,"1個売総売価",RefTable.TOKBM,"BAIKAAM"),
		BMTYP(13,"バンドルタイプ",RefTable.TOKBM,"BMTYP"),
		BD_KOSU1(14,"バンドル1個数",RefTable.TOKBM,"BD_KOSU1"),
		BD_BAIKAAN1(15,"バンドル1総売価",RefTable.TOKBM,"BD_BAIKAAN1"),
		BD_KOSU2(16,"バンドル2個数",RefTable.TOKBM,"BD_KOSU2"),
		BD_BAIKAAN2(17,"バンドル2総売価",RefTable.TOKBM,"BD_BAIKAAN2"),
		HBSTDT(18,"販売期間_開始日",RefTable.TOKBM,"HBSTDT"),
		HBEDDT(19,"販売期間_終了日",RefTable.TOKBM,"HBEDDT"),
		BMNCD_RANK(20,"部門",RefTable.TOKBM,"BMNCD_RANK"),
		RANKNO_ADD(21,"対象店G",RefTable.TOKBM,"RANKNO_ADD"),
		RANKNO_DEL(22,"除外店G",RefTable.TOKBM,"RANKNO_DEL"),
		TENCD_ADD1(23,"対象店1",RefTable.TOKBM_TJTEN,"TENCD_ADD1"),
		TENCD_ADD2(24,"対象店2",RefTable.TOKBM_TJTEN,"TENCD_ADD2"),
		TENCD_ADD3(25,"対象店3",RefTable.TOKBM_TJTEN,"TENCD_ADD3"),
		TENCD_ADD4(26,"対象店4",RefTable.TOKBM_TJTEN,"TENCD_ADD4"),
		TENCD_ADD5(27,"対象店5",RefTable.TOKBM_TJTEN,"TENCD_ADD5"),
		TENCD_ADD6(28,"対象店6",RefTable.TOKBM_TJTEN,"TENCD_ADD6"),
		TENCD_ADD7(29,"対象店7",RefTable.TOKBM_TJTEN,"TENCD_ADD7"),
		TENCD_ADD8(30,"対象店8",RefTable.TOKBM_TJTEN,"TENCD_ADD8"),
		TENCD_ADD9(31,"対象店9",RefTable.TOKBM_TJTEN,"TENCD_ADD9"),
		TENCD_ADD10(32,"対象店10",RefTable.TOKBM_TJTEN,"TENCD_ADD10"),
		TENCD_DEL1(33,"除外店1",RefTable.TOKBM_TJTEN,"TENCD_DEL1"),
		TENCD_DEL2(34,"除外店2",RefTable.TOKBM_TJTEN,"TENCD_DEL2"),
		TENCD_DEL3(35,"除外店3",RefTable.TOKBM_TJTEN,"TENCD_DEL3"),
		TENCD_DEL4(36,"除外店4",RefTable.TOKBM_TJTEN,"TENCD_DEL4"),
		TENCD_DEL5(37,"除外店5",RefTable.TOKBM_TJTEN,"TENCD_DEL5"),
		TENCD_DEL6(38,"除外店6",RefTable.TOKBM_TJTEN,"TENCD_DEL6"),
		TENCD_DEL7(39,"除外店7",RefTable.TOKBM_TJTEN,"TENCD_DEL7"),
		TENCD_DEL8(40,"除外店8",RefTable.TOKBM_TJTEN,"TENCD_DEL8"),
		TENCD_DEL9(41,"除外店9",RefTable.TOKBM_TJTEN,"TENCD_DEL9"),
		TENCD_DEL10(42,"除外店10",RefTable.TOKBM_TJTEN,"TENCD_DEL10"),
		SHNCD(43,"商品コード",RefTable.TOKBM_SHN,"SHNCD"),
		POPKN(44,"POP名称（漢字）",RefTable.OTHER,"POPKN"),
		GENKAAM(45,"原価",RefTable.TOKBM_SHN,"GENKAAM"),
		HIGAWARIKBN(46,"日替区分",RefTable.OTHER,"HIGAWARIKBN"),
		YORIDORIFLG(47,"よりどりフラグ",RefTable.OTHER,"YORIDORIFLG"),
		ABCKBN(48,"ABC売価区分",RefTable.OTHER,"ABCKBN"),
		SANCHIKN(49,"産地",RefTable.OTHER,"SANCHIKN"),
		MAKERKN(50,"メーカー名（漢字）",RefTable.OTHER,"MAKERKN"),
		KIKKN(51,"規格",RefTable.OTHER,"KIKKN"),
		OPERATOR(52,"オペレータ",RefTable.OTHER,"OPERATOR"),
		ADDDT(53,"登録日",RefTable.OTHER,"ADDDT"),
		UPDDT(54,"更新日",RefTable.OTHER,"UPDDT");

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
