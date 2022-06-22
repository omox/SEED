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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import authentication.bean.User;
import authentication.defines.Consts;
import common.CmnDate;
import common.DefineReport;
import common.DefineReport.DataType;
import common.Defines;
import common.FileList;
import common.InputChecker;
import common.ItemList;
import common.MessageUtility;
import common.MessageUtility.Msg;
import common.MessageUtility.MsgKey;
import dao.ReportSO003Dao.CSVTOK_SOLayout;
import dao.ReportSO003Dao.TOKSO_BMNLayout;
import dao.ReportSO003Dao.TOKSO_SHNLayout;
import dao.Reportx002Dao.CSVSHNLayout;
import dto.JQEasyModel;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 */
public class ReportSO004Dao extends ItemDao {

	boolean isTest = false;


	/** 最大処理件数 */
	public static int MAX_ROW = 10000;

	/**
	 * インスタンスを生成します。
	 * @param source
	 */
	public ReportSO004Dao(String JNDIname) {
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

		String sendBtnid	= map.get("SENDBTNID");		// 呼出しボタン
		int countdata		= 0;						// 登録件数

		// 正・予約判断
		boolean isSei = DefineReport.Button.CSV_IMPORT.getObj().equals(sendBtnid);

		// メッセージ情報取得
		MessageUtility mu = new MessageUtility();
		JSONArray errMsgList = new JSONArray();

		// CVSトランSEQ情報
		String seq = "-1";

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

			ReportSO003Dao dao = new ReportSO003Dao(super.JNDIname);

			// CVSトランSEQ情報
			//String seq = this.getCSVSHN_SEQ();

			// データ加工 + 最新情報取得
			ArrayList<JSONObject> dataList	 = new ArrayList<JSONObject>();
			JSONArray array_tokso_bmn		 = new JSONArray()
					,array_tokso_shn		 = new JSONArray()
					,array_ooth				 = new JSONArray();

			if(errMsgList.size()==0){
				// データ加工
				for (int i = idxHeader; i < dL.size(); i++) {
					Object[] data = dL.get(i);
					String inputno = Integer.toString(i+1);			// 入力番号
					JSONObject obj_tokso_bmn	 = new JSONObject()
							, obj_tokso_shn		 = new JSONObject()
							, ooth				 = new JSONObject();

					for(FileLayout itm :FileLayout.values()){
						String val = StringUtils.trim((String) data[itm.getNo()-1]);
						// TODO
						// 値にダブルコートが入ってしまう。
						val = val.replace("\"", "");
						// 基本情報
						if(itm.getTbl() == RefTable.TOKSO_BMN){
							obj_tokso_bmn.put(itm.getCol(), val);

						// 生活応援＿商品
						}else if(itm.getTbl() == RefTable.TOKSO_SHN || itm.getTbl() == RefTable.OTHER){
							obj_tokso_shn.put(itm.getCol(), val);
						}
					}
					array_tokso_bmn.add(obj_tokso_bmn);
					array_tokso_shn.add(obj_tokso_shn);
					//ooth.put(CSVSHNLayout.SEQ.getCol(), seq);				// CSV用.SEQ
					ooth.put(CSVSHNLayout.INPUTNO.getCol(), inputno);		// CSV用.入力番号
					array_ooth.add(ooth);


					// データ取得先判断
					String fUpdkbn = ooth.optString(FileLayout.UPDKBN.getCol());
					boolean isNew = DefineReport.ValFileUpdkbn.NEW.getVal().equals(fUpdkbn);
				}
				// 1.生活応援_部門
				JSONArray dataArray		 = this.selectTOKSO_BMN(array_tokso_bmn);
				// 2.生活応援_商品
				JSONArray dataArray_shn	 = this.selectTOKSO_SHN(array_tokso_shn, dataArray);

				JSONObject objset = new JSONObject();
				objset.put("DATA", dataArray);						// 対象情報（主要な更新情報）
				objset.put("DATA_SHN", dataArray_shn);				// 対象情報（生活応援＿商品）
				objset.put("DATA_OTHER", array_ooth);				// 対象情報（その他）
				dataList.add(objset);
			}

			int updCount = 0;
			int errCount = 0;

			// *** 詳細情報チェック＋情報登録用SQL作成 ***
			String userId = userInfo.getId();


			// 各テーブル登録用のSQL作成
			for (int i = 0; i < dataList.size(); i++) {

				JSONObject objset = dataList.get(i);

				//String shncd = objset.optString(HATSKLayout.SHNCD.getCol());
				HashMap<String, String> sendmap = new HashMap<String, String>();
				sendmap.put("SENDBTNID", sendBtnid);		// 呼出しボタン

				// 1.生活応援＿部門
				JSONArray dataArray		 = objset.optJSONArray("DATA");
				// 2.生活応援＿商品
				JSONArray dataArray_shn	 = objset.optJSONArray("DATA_SHN");
				// 3.その他
				JSONArray ooth			 = objset.optJSONArray("DATA_OTHER");

				// 基本情報チェック(ファイル取込前チェック)
				JSONArray msgListB = this.checkData(dao, map, userInfo, mu, dataArray, dataArray_shn);

				// 詳細情報チェック
				List<JSONObject> msgList	 = new ArrayList<JSONObject>(){};
				List<JSONObject> updDataListCSVErr	 = new ArrayList<JSONObject>(){};	// 登録用データリスト：CSVエラー
				JSONArray updDataList_SOBMN			 = new JSONArray();					// 登録用データリスト：生活応援_部門
				JSONArray updDataList_SOSHN			 = new JSONArray();					// 登録用データリスト：生活応援_商品

				boolean isError = false;
				JSONArray checkData_Child	 = new JSONArray();							// チェック用データリスト：生活応援_部門
				JSONArray checkData_Palent	 = new JSONArray();							// チェック用データリスト：生活応援_商品(中身を空で送信する)

				// 1行ずつチェックを行う
				if(msgListB.size()==0){
					for (int j = 0; j < dataArray_shn.size(); j++) {
						checkData_Child	 = new JSONArray();
						checkData_Child.add(dataArray_shn.get(j));

						// 詳細情報チェック
						msgList = dao.checkData(map, userInfo, mu, checkData_Palent, checkData_Child);

						isError = msgList.size()!=0;

						if(isError){
							// エラー情報を格納
							errCount++;
							updDataListCSVErr.add(msgList.get(0));
						}else{
							// 登録対象データを格納
							updCount++;
							updDataList_SOSHN.add(dataArray_shn.get(j));
						}
					}
				}else{
					// ファイル取り込み前チェックのエラーメッセージを格納
					msgList.add(msgListB.optJSONObject(0));
					//strMsg = msgList.toString();
					errMsgList.add(msgListB.optJSONObject(0));
				}


				// CSVエラーを登録する。
				for (int j = 0; j < updDataListCSVErr.size(); j++) {
					JSONObject data = updDataListCSVErr.get(j);

					if(StringUtils.equals(seq, "-1")){
						// CVSトランSEQ情報
						seq = this.getCSVTOK_SEQ();

						// ヘッダ登録用SQL作成
						JSONObject hdrtn = this.createSqlCSVTOKHEAD(userId, sysdate, seq, szCommentkn);
					}

					// エラー内容登録SQLの発行
					// CSV用情報セット
					dao.csvtokso_add_data[CSVTOK_SOLayout.SEQ.getNo()-1]		 = seq;																		// CSV用.SEQ
					dao.csvtokso_add_data[CSVTOK_SOLayout.INPUTNO.getNo()-1]	 = ooth.getJSONObject(j).optString(CSVTOK_SOLayout.INPUTNO.getCol());		// CSV用.入力番号
					dao.csvtokso_add_data[CSVTOK_SOLayout.CSV_UPDKBN.getNo()-1]	 = data.optString(CSVTOK_SOLayout.CSV_UPDKBN.getCol());						// CSV_UPDKBN
					dao.csvtokso_add_data[CSVTOK_SOLayout.ERRCD.getNo()-1]		 = data.optString(CSVTOK_SOLayout.ERRCD.getCol());							// ERRCD
					dao.csvtokso_add_data[CSVTOK_SOLayout.ERRFLD.getNo()-1]		 = data.optString(CSVTOK_SOLayout.ERRFLD.getCol());							// ERRFLD
					dao.csvtokso_add_data[CSVTOK_SOLayout.ERRVL.getNo()-1]		 = data.optString(CSVTOK_SOLayout.ERRVL.getCol());							// ERRVL
					dao.csvtokso_add_data[CSVTOK_SOLayout.ERRTBLNM.getNo()-1]	 = data.optString(CSVTOK_SOLayout.ERRTBLNM.getCol());						// ERRTBLNM
					dao.csvtokso_add_data[CSVTOK_SOLayout.MOYSKBN.getNo()-1]	 = data.optString(CSVTOK_SOLayout.MOYSKBN.getCol()); 						// MOYSKBN
					dao.csvtokso_add_data[CSVTOK_SOLayout.MOYSSTDT.getNo()-1]	 = data.optString(CSVTOK_SOLayout.MOYSSTDT.getCol()); 						// MOYSSTDT
					dao.csvtokso_add_data[CSVTOK_SOLayout.MOYSRBAN.getNo()-1]	 = data.optString(CSVTOK_SOLayout.MOYSRBAN.getCol()); 						// MOYSRBAN
					dao.csvtokso_add_data[CSVTOK_SOLayout.SHNCD.getNo()-1]		 = data.optString(CSVTOK_SOLayout.SHNCD.getCol()); 							// SHNCD
					dao.csvtokso_add_data[CSVTOK_SOLayout.BMNCD.getNo()-1]		 = data.optString(CSVTOK_SOLayout.BMNCD.getCol()); 							// BMNCD
					dao.csvtokso_add_data[CSVTOK_SOLayout.MAKERKN.getNo()-1]	 = data.optString(CSVTOK_SOLayout.MAKERKN.getCol()); 						// MAKERKN
					dao.csvtokso_add_data[CSVTOK_SOLayout.SHNKN.getNo()-1]		 = data.optString(CSVTOK_SOLayout.SHNKN.getCol()); 							// SHNKN
					dao.csvtokso_add_data[CSVTOK_SOLayout.KIKKN.getNo()-1]		 = data.optString(CSVTOK_SOLayout.KIKKN.getCol()); 							// KIKKN
					dao.csvtokso_add_data[CSVTOK_SOLayout.IRISU.getNo()-1]		 = data.optString(CSVTOK_SOLayout.IRISU.getCol()); 							// IRISU
					dao.csvtokso_add_data[CSVTOK_SOLayout.MINSU.getNo()-1]		 = data.optString(CSVTOK_SOLayout.MINSU.getCol()); 							// MINSU
					dao.csvtokso_add_data[CSVTOK_SOLayout.GENKAAM.getNo()-1]	 = data.optString(CSVTOK_SOLayout.GENKAAM.getCol());						// GENKAAM
					dao.csvtokso_add_data[CSVTOK_SOLayout.A_BAIKAAM.getNo()-1]	 = data.optString(CSVTOK_SOLayout.A_BAIKAAM.getCol());						// A_BAIKAAM
					dao.csvtokso_add_data[CSVTOK_SOLayout.B_BAIKAAM.getNo()-1]	 = data.optString(CSVTOK_SOLayout.B_BAIKAAM.getCol());						// B_BAIKAAM
					dao.csvtokso_add_data[CSVTOK_SOLayout.C_BAIKAAM.getNo()-1]	 = data.optString(CSVTOK_SOLayout.C_BAIKAAM.getCol());						// C_BAIKAAM
					dao.csvtokso_add_data[CSVTOK_SOLayout.A_RANKNO.getNo()-1]	 = data.optString(CSVTOK_SOLayout.A_RANKNO.getCol());						// A_RANKNO
					dao.csvtokso_add_data[CSVTOK_SOLayout.B_RANKNO.getNo()-1]	 = data.optString(CSVTOK_SOLayout.B_RANKNO.getCol());						// B_RANKNO
					dao.csvtokso_add_data[CSVTOK_SOLayout.C_RANKNO.getNo()-1]	 = data.optString(CSVTOK_SOLayout.C_RANKNO.getCol());						// C_RANKNO
					dao.csvtokso_add_data[CSVTOK_SOLayout.POPCD.getNo()-1]		 = data.optString(CSVTOK_SOLayout.POPCD.getCol());							// POPCD
					dao.csvtokso_add_data[CSVTOK_SOLayout.POPSZ.getNo()-1]		 = data.optString(CSVTOK_SOLayout.POPSZ.getCol());							// POPSZ
					dao.csvtokso_add_data[CSVTOK_SOLayout.POPSU.getNo()-1]		 = data.optString(CSVTOK_SOLayout.POPSU.getCol());							// POPSU
					dao.csvtokso_add_data[CSVTOK_SOLayout.TENATSUK_ARR.getNo()-1] = data.optString(CSVTOK_SOLayout.TENATSUK_ARR.getCol());					// TENATSUK_ARR

					// CSV取込トラン_催し別送信情報更新
					JSONObject bmrtn = this.createSqlCSVTOKSO(userId, sysdate, dao);
				}

				// エラー無しの子要素のデータが無い場合は登録を行わない。
				if(updDataList_SOSHN.size() > 0){

					// 主要情報以外のデータを格納
					sendmap.put("DATA_SHN", updDataList_SOSHN.toString());
					// 親要素の情報を格納
					updDataList_SOBMN = dataArray;

					// 登録処理
					this.createCommandUpdateData(dao, sendmap, userInfo, updDataList_SOBMN);
					this.sqlList.addAll(dao.sqlList);
					this.prmList.addAll(dao.prmList);
					this.lblList.addAll(dao.lblList);
				}
			}

			// 更新処理
			try {
				option = this.updateData();
			} catch (Exception e) {
				e.printStackTrace();
				errMsgList.add(MessageUtility.getDbMessageIdObj("E00009", new String[]{}));
			}

			// 実行結果のメッセージを取得
			strMsg = this.getMessage();
			if(errMsgList.size()==0){
				// 実行トラン情報をJSに戻す

				String status = "完了";
				if(errCount != 0){
					status = "CSVデータのエラー";
				}

				option.put(DefineReport.Text.SEQ.getObj(), seq);
				option.put(DefineReport.Text.STATUS.getObj(), status);
				option.put(DefineReport.Text.UPD_NUMBER.getObj(), dL.size());
				//option.put(DefineReport.Text.UPD_NUMBER.getObj(), updCount);
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
	private JSONArray checkData(ReportSO003Dao dao,HashMap<String, String> map, User userInfo, MessageUtility mu,
			JSONArray dataArray,			// 対象情報（主要な更新情報）
			JSONArray dataArrayShn			// 対象情報（追加更新情報）
			) {

		ItemList			iL		= new ItemList();
		String sqlcommand		 = "";
		ArrayList<String> paramData	 = new ArrayList<String>();

		JSONArray msg	 = new JSONArray();

		// 生活応援_部門：データチェック
		if(dataArray.size() > 0){
			JSONObject data	 = new JSONObject();
			HashSet<String> checkPramsMOYSCD = new HashSet<String>();
			HashSet<String> checkPramsBMNCD	 = new HashSet<String>();

			for (int j = 0; j < dataArray.size(); j++) {
				data = dataArray.getJSONObject(j);
				if(data.isEmpty()){
					continue;
				}
				checkPramsBMNCD.add(data.optString("F4"));
				checkPramsMOYSCD.add(data.optString("F1") + data.optString("F2") + data.optString("F3"));
			}
			// 種類チェック：催しコード
			if(checkPramsMOYSCD.size() != 1){
				// 1種類以上のパラメータが入力されている
				//JSONObject o = MessageUtility.getDbMessageIdObj("E40116", new String[]{});
				JSONObject o = mu.getDbMessageObj("E40004", new String[]{});
				msg.add(o);
				return msg;
			}

			// 種類チェック：部門
			if(checkPramsBMNCD.size() != 1){
				// 1種類以上のパラメータが入力されている
				JSONObject o = mu.getDbMessageObj("E40005", new String[]{});
				msg.add(o);
				return msg;
			}

			// 存在チェック：催しコード
			paramData  = new ArrayList<String>();

			if(StringUtils.isNumeric(data.getString("F1"))
					&& StringUtils.isNumeric(data.getString("F2"))
					&& StringUtils.isNumeric(data.getString("F3"))){
				paramData.add(data.getString("F1"));
				paramData.add(data.getString("F2"));
				paramData.add(data.getString("F3"));
				sqlcommand = "select COUNT(MOYSKBN) as value from INATK.TOKMOYCD where NVL(UPDKBN, 0) <> 1 and MOYSKBN = ? and MOYSSTDT = ? and MOYSRBAN = ?   ";

				@SuppressWarnings("static-access")
				JSONArray array = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
				if(NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) == 0){
					JSONObject o = mu.getDbMessageObj("E20005", new String[]{});
					msg.add(o);
					return msg;
				}
			}

			// 存在チェック：部門コード
			if(StringUtils.isNumeric(data.getString("F4"))){
				paramData  = new ArrayList<String>();
				paramData.add(data.getString("F4"));
				sqlcommand = "select COUNT(BMNCD) as value from INAMS.MSTBMN where NVL(UPDKBN, 0) <> 1 and BMNCD = ? ";

				@SuppressWarnings("static-access")
				JSONArray array = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
				if(NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) == 0){
					JSONObject o = mu.getDbMessageObj("E11097", new String[]{});
					msg.add(o);
					return msg;
				}
			}

			// 必須入力チェック
			/*if(data.optString(FileLayout.TENNO.getCol())){

			}*/
			// 基本データチェック:入力値がテーブル定義と矛盾してないか確認
			TOKSO_BMNLayout[] targetCol = null;
			targetCol = new TOKSO_BMNLayout[]{
					TOKSO_BMNLayout.MOYSKBN			// 催し区分
					,TOKSO_BMNLayout.MOYSSTDT		// 催し開始日
					,TOKSO_BMNLayout.MOYSRBAN		// 催し連番
					,TOKSO_BMNLayout.BMNCD			// 部門コード
			};
			for(TOKSO_BMNLayout colinf: targetCol){
				String val = StringUtils.trim(data.optString(colinf.getId()));
				if(StringUtils.isNotEmpty(val)){
					DataType dtype = null;
					int[] digit = null;
					try {
						DefineReport.InpText inpsetting = DefineReport.InpText.valueOf(colinf.getCol());
						dtype = inpsetting.getType();
						digit = new int[]{inpsetting.getDigit1(), inpsetting.getDigit2()};
					}catch (IllegalArgumentException e){
						dtype = colinf.getDataType();
						digit = colinf.getDigit();
					}
					// ①データ型による文字種チェック
					if(!InputChecker.checkDataType(dtype, val)){
						//JSONObject o = mu.getDbMessageObjDataTypeErr(dtype, colinf.getText()+"は");
						JSONObject o = mu.getDbMessageObjDataTypeErr(dtype, new String[]{colinf.getText() + "は"});
						msg.add(o);
						return msg;
					}
					// ②データ桁チェック
					if(!InputChecker.checkDataLen(dtype, val, digit)){
						//JSONObject o = mu.getDbMessageObjLen(dtype, colinf.getText()+"は");
						JSONObject o = mu.getDbMessageObjLen(dtype, new String[]{colinf.getText() + "は"});
						msg.add(o);
						return msg;
					}
				}
			}
		}

		// 生活応援_商品：データチェック
		if(dataArrayShn.size() > 0){
			JSONObject data	 = new JSONObject();
			HashSet<String> checkPramsSHNCD	 = new HashSet<String>();
			ArrayList<String> SHNCD			 = new ArrayList<>();

			for (int j = 0; j < dataArrayShn.size(); j++) {
				data = dataArrayShn.getJSONObject(j);
				if(data.isEmpty()){
					continue;
				}
				checkPramsSHNCD.add(data.optString("F6"));
				SHNCD.add(data.optString("F6"));

				// 基本データチェック:入力値がテーブル定義と矛盾してないか確認、
				/*for(TOKSO_SHNLayout colinf: TOKSO_SHNLayout.values()){
					String val = StringUtils.trim(data.optString(colinf.getId()));
					if(StringUtils.isNotEmpty(val)){
						DataType dtype = null;
						int[] digit = null;
						try {
							DefineReport.InpText inpsetting = DefineReport.InpText.valueOf(colinf.getCol());
							dtype = inpsetting.getType();
							digit = new int[]{inpsetting.getDigit1(), inpsetting.getDigit2()};
							if(StringUtils.equals("POPSZ", colinf.getCol())){
								dtype = colinf.getDataType();
								digit = colinf.getDigit();
							}
						}catch (IllegalArgumentException e){
							dtype = colinf.getDataType();
							digit = colinf.getDigit();
						}

						// ①データ型による文字種チェック
						if(!InputChecker.checkDataType(dtype, val)){
							//JSONObject o = mu.getDbMessageObjDataTypeErr(dtype, colinf.getText()+"は");
							JSONObject o = mu.getDbMessageObjDataTypeErr(dtype, new String[]{});
							msg.add(o);
							return msg;
						}
						// ②データ桁チェック
						if(!InputChecker.checkDataLen(dtype, val, digit)){
							//JSONObject o = mu.getDbMessageObjLen(dtype, colinf.getText()+"は");
							JSONObject o = mu.getDbMessageObjLen(dtype, new String[]{});
							msg.add(o);
							return msg;
							//data.element(colinf.getId(), "");		// CSVトラン用に空
						}
					}
				}*/
			}

			// 重複チェック(入力値)：商品
			if(checkPramsSHNCD.size() != SHNCD.size()){
				// 重複する店舗コードが含まれている場合。
				JSONObject o = mu.getDbMessageObj("EX1022", new String[]{});
				msg.add(o);
				return msg;
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
	private JSONObject createCommandUpdateData(ReportSO003Dao dao, HashMap<String, String> map, User userInfo,JSONArray	 dataArray) throws Exception {

		JSONObject option = new JSONObject();

		map.put("SENDBTNID", DefineReport.Button.NEW.getObj());		// 呼出しボタンを新規に変更
		//map.put("DATA_SHN", new JSONArray().toString());			// 空データ:生活応援＿商品
		map.put("DATA_SHN_DEL", new JSONArray().toString());		// 空データ:生活応援＿商品削除

		JSONObject data = dataArray.optJSONObject(0);				// 親テーブルの為１件文のデータのみを送る。

		// --- 01.生活応援＿部門、生活応援＿商品
		// 基本登録情報
		if(data.size() > 0){
			dao.createSqlTOKSO(data,map,userInfo);
		}

		return option;
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
		String dbsysdate = CmnDate.dbDateFormat(sysdate);
		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();

		LinkedList<String> valList = new LinkedList<String>();
		valList.add("");	//

		String values = "";
		values += " " + seq;
		values += ",?";
		prmData.add(userId);
		values += ",current timestamp ";

		if(isTest){
			values += ",'" + commentkn+"'";
		}else{
			values += ",?";
			prmData.add(commentkn);
		}

		StringBuffer sbSQL;
		sbSQL = new StringBuffer();
		sbSQL.append("insert into INATK.CSVTOKHEAD(SEQ, OPERATOR, INPUT_DATE, COMMENTKN)");
		sbSQL.append("values("+values+")");
		if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		this.sqlList.add(sbSQL.toString());
		this.prmList.add(prmData);
		this.lblList.add("CSV取込トラン_特売ヘッダ");
		return result;
	}


	/**
	 * 生活応援＿部門情報取得処理
	 *
	 * @param isSei	正/予約
	 * @param isNew	新規/更新
	 * @param data	CSV抜粋データ
	 *
	 * @throws Exception
	 */
	public JSONArray selectTOKSO_BMN(JSONArray array) {
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
			for(TOKSO_BMNLayout itm :TOKSO_BMNLayout.values()){
				String value = "";
				if(StringUtils.equals(itm.getCol(), "MOYSKBN")){
					value = StringUtils.substring(dataBf.optString("MOYSCD"), 0, 1);

				}else if(StringUtils.equals(itm.getCol(), "MOYSSTDT")){
					value = StringUtils.substring(dataBf.optString("MOYSCD"), 1, 7);

				}else if(StringUtils.equals(itm.getCol(), "MOYSRBAN")){
					value = StringUtils.substring(dataBf.optString("MOYSCD"), 7, 10);

				}else{
					value = dataBf.getString(itm.getCol());
				}
				dataAf.put("F"+ itm.getNo(), value);
			}
			dataArray.add(dataAf);
		}
		return dataArray;
	}


	/**
	 * 生活応援＿部門情報取得処理
	 *
	 * @param isSei	正/予約
	 * @param isNew	新規/更新
	 * @param data	CSV抜粋データ
	 *
	 * @throws Exception
	 */
	public JSONArray selectTOKSO_SHN(JSONArray childArray, JSONArray parentArray) {
		JSONObject dataAf		 = new JSONObject();
		JSONObject dataBf		 = new JSONObject();
		JSONObject dataDefoult	 = new JSONObject();
		JSONArray	dataArray	 = new JSONArray();	// 対象情報（主要な更新情報）

		//String key = "";
		String regex = "(?i)null";
		String kanriNo = "";
		Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

		for (int j = 0; j < childArray.size(); j++) {
			dataBf = childArray.getJSONObject(j);

			dataAf = new JSONObject();
			dataAf.put("F1", parentArray.getJSONObject(j).opt("F1"));
			dataAf.put("F2", parentArray.getJSONObject(j).opt("F2"));
			dataAf.put("F3", parentArray.getJSONObject(j).opt("F3"));
			dataAf.put("F4", parentArray.getJSONObject(j).opt("F4"));

			if(dataBf.isEmpty()){
				continue;
			}

			// 更新区分="U"の時は、管理Noを取得する。
			if(StringUtils.equals(DefineReport.ValFileUpdkbn.UPD.getVal(), dataBf.optString(FileLayout.UPDKBN.getCol()))){

				if(StringUtils.isNumeric(dataAf.getString("F1"))
						&& StringUtils.isNumeric(dataAf.getString("F2"))
						&& StringUtils.isNumeric(dataAf.getString("F3"))
						&& StringUtils.isNumeric(dataAf.getString("F4"))
						&& StringUtils.isNumeric(dataBf.getString(TOKSO_SHNLayout.SHNCD.getCol()))
						){
					kanriNo = this.getKANRINO(dataAf.getString("F1")
							, dataAf.getString("F2")
							, dataAf.getString("F3")
							, dataAf.getString("F4")
							, dataBf.getString(TOKSO_SHNLayout.SHNCD.getCol()));
					dataAf.put("F5", kanriNo);
				}
			}

			// CSV更新区分を保持する
			dataAf.put("F24", dataBf.optString(FileLayout.UPDKBN.getCol()));

			// 商品コードよりデフォルト値を取得する。
			dataDefoult = this.getDefoultValues(dataBf.getString(TOKSO_SHNLayout.SHNCD.getCol()), dataAf.getString("F2"));

			for(TOKSO_SHNLayout itm :TOKSO_SHNLayout.values()){
				//if(dataBf.containsKey(itm.getCol()) && StringUtils.isNotEmpty(dataBf.optString(itm.getCol()))){
				if(dataBf.containsKey(itm.getCol())){
					String value = dataBf.getString(itm.getCol());

					// Nullが含まれている場合は置き換える。
					Matcher m = p.matcher(value);
					if (m.find()){
						value = "";
					}
					if(StringUtils.isEmpty(value)){
						// 入力値が空の場合、デフォルト値を設定する。
						if(StringUtils.equals(itm.getCol(), FileLayout.MAKERKN.getCol())){
							value = dataDefoult.containsKey(FileLayout.MAKERKN.getCol()) ? dataDefoult.getString(FileLayout.MAKERKN.getCol()) : value;

						}else if(StringUtils.equals(itm.getCol(), FileLayout.SHNKN.getCol())){
							value = dataDefoult.containsKey(FileLayout.SHNKN.getCol()) ? dataDefoult.getString(FileLayout.SHNKN.getCol()) : value;

						}else if(StringUtils.equals(itm.getCol(), FileLayout.KIKKN.getCol())){
							value = dataDefoult.containsKey(FileLayout.KIKKN.getCol()) ? dataDefoult.getString(FileLayout.KIKKN.getCol()) : value;

						}else if(StringUtils.equals(itm.getCol(), FileLayout.IRISU.getCol())){
							value = dataDefoult.containsKey(FileLayout.IRISU.getCol()) ? dataDefoult.getString(FileLayout.IRISU.getCol()) : value;

						}else if(StringUtils.equals(itm.getCol(), FileLayout.MINSU.getCol())){
							value = dataDefoult.containsKey(FileLayout.MINSU.getCol()) ? dataDefoult.getString(FileLayout.MINSU.getCol()) : value;

						}else if(StringUtils.equals(itm.getCol(), FileLayout.GENKAAM.getCol())){
							value = dataDefoult.containsKey(FileLayout.GENKAAM.getCol()) ? dataDefoult.getString(FileLayout.GENKAAM.getCol()) : value;

						}else if(StringUtils.equals(itm.getCol(), FileLayout.A_BAIKAAM.getCol())){
							value = dataDefoult.containsKey(FileLayout.A_BAIKAAM.getCol()) ? dataDefoult.getString(FileLayout.A_BAIKAAM.getCol()) : value;
						}
					}


					/*if(StringUtils.equals(itm.getCol(), FileLayout.UPDKBN.getCol())){
						if(StringUtils.equals("U", value)){
							// 変更データの場合
							// DBから管理Noを取得
							kanriNo = this.getKANRINO(dataBf.getString("F1"), dataBf.getString("F2"), dataBf.getString("F3"), dataBf.getString("F4"), dataBf.getString("F6"));
							dataAf.put("F5", kanriNo);
						}
					}*/

					if(! ArrayUtils.contains(new String[]{"F22"}, "F"+ itm.getNo())){
						dataAf.put("F"+ itm.getNo(), value);
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
	 * 管理番号取得処理(生活応援_商品)
	 *
	 * @throws Exception
	 */
	public String getKANRINO(String MOYSKBN, String MOYSSTDT, String MOYSRBAN, String BMNCD, String SHNCD) {
		// 関連情報取得
		ItemList iL = new ItemList();
		StringBuffer sbSQL = new StringBuffer();
		ArrayList<String> paramData	 = new ArrayList<String>();

		paramData.add(MOYSKBN);
		paramData.add(MOYSSTDT);
		paramData.add(MOYSRBAN);
		paramData.add(BMNCD);
		paramData.add(SHNCD);

		sbSQL = new StringBuffer();
		sbSQL.append("select");
		sbSQL.append(" SOS.KANRINO as KANRINO");
		sbSQL.append(" from INATK.TOKMOYCD MYCD");
		sbSQL.append(" inner join INATK.TOKSO_BMN SOB on SOB.MOYSKBN = MYCD.MOYSKBN and SOB.MOYSSTDT = MYCD.MOYSSTDT and SOB.MOYSRBAN = MYCD.MOYSRBAN and MYCD.UPDKBN = 0 and SOB.UPDKBN = 0");
		sbSQL.append(" inner join INATK.TOKSO_SHN SOS on SOS.MOYSKBN = SOB.MOYSKBN and SOS.MOYSSTDT = SOB.MOYSSTDT and SOS.MOYSRBAN = SOB.MOYSRBAN and SOS.BMNCD = SOB.BMNCD and SOS.UPDKBN = 0");
		sbSQL.append(" where SOS.MOYSKBN = ?");
		sbSQL.append(" and SOS.MOYSSTDT = ?");
		sbSQL.append(" and SOS.MOYSRBAN = ?");
		sbSQL.append(" and SOS.BMNCD = ?");
		sbSQL.append(" and SOS.SHNCD = ?");

		@SuppressWarnings("static-access")
		JSONArray array = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);
		String value = "";
		if(array.size() > 0){
			value = array.optJSONObject(0).optString("KANRINO");
		}
		/*else{
			value = "1";
		}*/
		return value;
	}

	/**
	 * 商品コードよりデフォルト値を取得
	 *
	 * @throws Exception
	 */
	public JSONObject getDefoultValues(String SHNCD, String MYOSSTDT) {
		// 関連情報取得
		ItemList iL = new ItemList();
		StringBuffer sbSQL = new StringBuffer();
		ArrayList<String> paramData	 = new ArrayList<String>();

		JSONObject defoultValues = new JSONObject();

		String baika	 = "";
		String soBaika	 = "";

		paramData.add(SHNCD);

		// 商品コード
		sbSQL = new StringBuffer();
		sbSQL.append(ReportSO003Dao.ID_SQL_SHNKN_SO003);
		@SuppressWarnings("static-access")
		JSONArray array = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);
		int size = 9;
		if(array.size() > 0){
			for(int i = 0; i < size; i++){
				String key = "F" + (i + 1);
				String val =array.optJSONObject(0).optString(key);

				if(StringUtils.equals(key, "F6")){
					defoultValues.put(TOKSO_SHNLayout.MAKERKN.getCol(), val);
				}else if(StringUtils.equals(key, "F1")){
					defoultValues.put(TOKSO_SHNLayout.SHNKN.getCol(), val);
				}else if(StringUtils.equals(key, "F2")){
					defoultValues.put(TOKSO_SHNLayout.KIKKN.getCol(), val);
				}else if(StringUtils.equals(key, "F3")){
					defoultValues.put(TOKSO_SHNLayout.IRISU.getCol(), val);
				}else if(StringUtils.equals(key, "F4")){
					defoultValues.put(TOKSO_SHNLayout.MINSU.getCol(), val);
				}else if(StringUtils.equals(key, "F9")){
					defoultValues.put(TOKSO_SHNLayout.GENKAAM.getCol(), val);
				}else if(StringUtils.equals(key, "F7")){
					baika = val;
				}
			}
		}

		// 総売価検索
		if(StringUtils.isNotEmpty(baika) && StringUtils.isNotEmpty(MYOSSTDT)){
			sbSQL		 = new StringBuffer();
			paramData	 = new ArrayList<String>();

			String kijundt = common.CmnDate.dateFormat(common.CmnDate.convYYMMDD(MYOSSTDT));
			if(NumberUtils.isNumber(baika) && NumberUtils.isNumber(SHNCD)&& NumberUtils.isNumber(kijundt)){
				paramData.add(SHNCD);
				paramData.add(baika);
				paramData.add(StringUtils.defaultIfEmpty(kijundt, "0"));
				sbSQL.append(DefineReport.ID_SQL_TOKBAIKA_SOU.replaceAll("@SHNCD", "T1.SHNCD").replaceAll("@", ""));
				array = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

				soBaika =array.optJSONObject(0).optString("VALUE");
				if(StringUtils.isNotEmpty(soBaika)){
					defoultValues.put(TOKSO_SHNLayout.A_BAIKAAM.getCol(), soBaika);
				}
			}
		}
		return defoultValues;
	}

	/**
	 * CSV取込トラン_生活応援INSERT処理
	 *
	 * @param userId
	 * @param data
	 * @param tbl
	 *
	 * @throws Exception
	 */
	public JSONObject createSqlCSVTOKSO(String userId, String sysdate, ReportSO003Dao dao) {
		JSONObject result = new JSONObject();
		String dbsysdate = CmnDate.dbDateFormat(sysdate);
		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();

		LinkedList<String> valList = new LinkedList<String>();
		valList.add("");	//

		String values = "";
		for(CSVTOK_SOLayout itm :CSVTOK_SOLayout.values()){
			String val = ((String) dao.csvtokso_add_data[itm.getNo()-1]);

			if(StringUtils.equals(CSVTOK_SOLayout.ERRVL.getCol(), itm.getCol())){
				// エラーメッセージを字化する。
				val = "'" + val.trim() + "'";
			}

			if (StringUtils.isEmpty(val)) {
				values += " null,";
			} else {
				values += " ?,";
				prmData.add(val);
			}
		}
		values = StringUtils.removeEnd(values, ",");
		//values += "?,?";
		//prmData.add(String.valueOf(userId));
		//prmData.add(dbsysdate);
		//prmData.add(dbsysdate);

		StringBuffer sbSQL;
		sbSQL = new StringBuffer();
		sbSQL.append("insert into INATK.CSVTOK_SO(");
		sbSQL.append(" SEQ");
		sbSQL.append(", INPUTNO");
		sbSQL.append(", ERRCD");
		sbSQL.append(", ERRFLD");
		sbSQL.append(", ERRVL");
		sbSQL.append(", ERRTBLNM");
		sbSQL.append(", CSV_UPDKBN");
		sbSQL.append(", MOYSKBN");
		sbSQL.append(", MOYSSTDT");
		sbSQL.append(", MOYSRBAN");
		sbSQL.append(", SHNCD");
		sbSQL.append(", BMNCD");
		sbSQL.append(", MAKERKN");
		sbSQL.append(", SHNKN");
		sbSQL.append(", KIKKN");
		sbSQL.append(", IRISU");
		sbSQL.append(", MINSU");
		sbSQL.append(", GENKAAM");
		sbSQL.append(", A_BAIKAAM");
		sbSQL.append(", B_BAIKAAM");
		sbSQL.append(", C_BAIKAAM");
		sbSQL.append(", A_RANKNO");
		sbSQL.append(", B_RANKNO");
		sbSQL.append(", C_RANKNO");
		sbSQL.append(", POPCD");
		sbSQL.append(", POPSZ");
		sbSQL.append(", POPSU");
		sbSQL.append(", TENATSUK_ARR");
		sbSQL.append(", UPDKBN");
		sbSQL.append(", OPERATOR");
		sbSQL.append(", ADDDT");
		sbSQL.append(", UPDDT");
		sbSQL.append(" )");
		sbSQL.append(" select");
		sbSQL.append(" SEQ");
		sbSQL.append(", INPUTNO");
		sbSQL.append(", ERRCD");
		sbSQL.append(", ERRFLD");
		sbSQL.append(", ERRVL");
		sbSQL.append(", ERRTBLNM");
		sbSQL.append(", CSV_UPDKBN");
		sbSQL.append(", MOYSKBN");
		sbSQL.append(", MOYSSTDT");
		sbSQL.append(", MOYSRBAN");
		sbSQL.append(", SHNCD");
		sbSQL.append(", BMNCD");
		sbSQL.append(", MAKERKN");
		sbSQL.append(", SHNKN");
		sbSQL.append(", KIKKN");
		sbSQL.append(", IRISU");
		sbSQL.append(", MINSU");
		sbSQL.append(", GENKAAM");
		sbSQL.append(", A_BAIKAAM");
		sbSQL.append(", B_BAIKAAM");
		sbSQL.append(", C_BAIKAAM");
		sbSQL.append(", A_RANKNO");
		sbSQL.append(", B_RANKNO");
		sbSQL.append(", C_RANKNO");
		sbSQL.append(", POPCD");
		sbSQL.append(", POPSZ");
		sbSQL.append(", POPSU");
		sbSQL.append(", TENATSUK_ARR");
		sbSQL.append(", "+DefineReport.ValUpdkbn.NML.getVal()+" AS UPDKBN");
		sbSQL.append(", '"+userId+"' AS OPERATOR ");
		sbSQL.append(", current timestamp as ADDDT");
		sbSQL.append(", current timestamp as UPDDT");
		sbSQL.append("  from");
		sbSQL.append(" (values("+values+")) as T1( ");
		sbSQL.append(" SEQ");
		sbSQL.append(", INPUTNO");
		sbSQL.append(", ERRCD");
		sbSQL.append(", ERRFLD");
		sbSQL.append(", ERRVL");
		sbSQL.append(", ERRTBLNM");
		sbSQL.append(", CSV_UPDKBN");
		sbSQL.append(", MOYSKBN");
		sbSQL.append(", MOYSSTDT");
		sbSQL.append(", MOYSRBAN");
		sbSQL.append(", SHNCD");
		sbSQL.append(", BMNCD");
		sbSQL.append(", MAKERKN");
		sbSQL.append(", SHNKN");
		sbSQL.append(", KIKKN");
		sbSQL.append(", IRISU");
		sbSQL.append(", MINSU");
		sbSQL.append(", GENKAAM");
		sbSQL.append(", A_BAIKAAM");
		sbSQL.append(", B_BAIKAAM");
		sbSQL.append(", C_BAIKAAM");
		sbSQL.append(", A_RANKNO");
		sbSQL.append(", B_RANKNO");
		sbSQL.append(", C_RANKNO");
		sbSQL.append(", POPCD");
		sbSQL.append(", POPSZ");
		sbSQL.append(", POPSU");
		sbSQL.append(", TENATSUK_ARR)");

		if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		this.sqlList.add(sbSQL.toString());
		this.prmList.add(prmData);
		this.lblList.add("CSV取込トラン_生活応援");
		return result;
	}


	/**  File出力項目の参照テーブル */
	public enum RefTable {
		/** 商品マスタ */
		TOKSO_BMN(1,"生活応援_部門"),
		/** ソースコード管理マスタ */
		TOKSO_SHN(2,"生活応援_商品"),
		/** 仕入グループ商品 */
		SYSSK(3,"生活応援＿部門＿内部管理テーブル"),
		/** その他 */
		OTHER(9,"その他");
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
		/** 生活応援＿部門 */
		/** 入力№ */
		UPDKBN(1,"更新区分", RefTable.OTHER,"UPDKBN"),
		/** 部門 */
		BMNCD(2,"部門", RefTable.TOKSO_BMN,"BMNCD"),
		/** 催しコード */
		MOYSCD(3,"催しコード", RefTable.TOKSO_BMN,"MOYSCD"),
		/** Aランク */
		A_RANKNO(4,"Aランク", RefTable.TOKSO_SHN,"A_RANKNO"),
		/** Bランク */
		B_RANKNO(5,"Bランク", RefTable.TOKSO_SHN,"B_RANKNO"),
		/** Cランク */
		C_RANKNO(6,"Cランク", RefTable.TOKSO_SHN,"C_RANKNO"),
		/** 商品コード */
		SHNCD(7,"商品コード", RefTable.TOKSO_SHN,"SHNCD"),
		/** メーカー名称 */
		MAKERKN(8,"メーカー名称", RefTable.TOKSO_SHN,"MAKERKN"),
		/** POP名称 */
		SHNKN(9,"POP名称", RefTable.TOKSO_SHN,"SHNKN"),
		/** 規格名称 */
		KIKKN(10,"規格名称", RefTable.TOKSO_SHN,"KIKKN"),
		/** 入数 */
		IRISU(11,"入数", RefTable.TOKSO_SHN,"IRISU"),
		/** 最低発注数 */
		MINSU(12,"最低発注数", RefTable.TOKSO_SHN,"MINSU"),
		/** 原価 */
		GENKAAM(13,"原価", RefTable.TOKSO_SHN,"GENKAAM"),
		/** A売価 */
		A_BAIKAAM(14,"A売価", RefTable.TOKSO_SHN,"A_BAIKAAM"),
		/** B売価 */
		B_BAIKAAM(15,"B売価", RefTable.TOKSO_SHN,"B_BAIKAAM"),
		/** C売価 */
		C_BAIKAAM(16,"C売価", RefTable.TOKSO_SHN,"C_BAIKAAM"),
		/** POPコード */
		POPCD(17,"POPコード", RefTable.TOKSO_SHN,"POPCD"),
		/** 枚数 */
		POPSU(18,"枚数", RefTable.TOKSO_SHN,"POPSU"),
		/** POPサイズ */
		POPSZ(19,"POPサイズ", RefTable.TOKSO_SHN,"POPSZ"),
		// TODO
		// 該当項目がマスタにない。
		/** バイヤーコード */
		BCD(20,"バイヤーコード", RefTable.TOKSO_SHN,"POPKN"),
		//POPKN(20,"POPKN", RefTable.TOKSO_SHN,"VARCHAR "),
		/** オペレータ */
		OPERATOR(21,"オペレータ", RefTable.TOKSO_BMN,"OPERATOR"),
		/** 登録日 */
		ADDDT(22,"登録日", RefTable.TOKSO_BMN,"ADDDT"),
		/** 更新日 */
		UPDDT(23,"更新日", RefTable.TOKSO_BMN,"UPDDT");

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
