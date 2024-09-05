/**
 *
 */
package dao;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.JDBCType;
import java.text.SimpleDateFormat;
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
import dao.Reportx002Dao.CSVSHNLayout;
import dao.Reportx002Dao.MSTSHNLayout;
import dto.JQEasyModel;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 */
public class ReportBW000Dao extends ItemDao {

	boolean isTest = false;
	/** SQLリスト保持用変数 */
	ArrayList<String> sqlList = new ArrayList<String>();
	/** SQLのパラメータリスト保持用変数 */
	ArrayList<ArrayList<String>> prmList = new ArrayList<ArrayList<String>>();
	/** SQLログ用のラベルリスト保持用変数 */
	ArrayList<String> lblList = new ArrayList<String>();

	/** 最大処理件数 */
	public static int MAX_ROW = 500;

	/**
	 * インスタンスを生成します。
	 * @param source
	 */
	public ReportBW000Dao(String JNDIname) {
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
						// 冷凍食品_企画
						if(itm.getTbl() == RefTable.TOKRS_KKK){
							obj_tokso_bmn.put(itm.getCol(), val);
							// 冷凍食品_商品
						}else if(itm.getTbl() == RefTable.TOKRS_SHN || itm.getTbl() == RefTable.CSVTOK_RSSHN){
							obj_tokso_shn.put(itm.getCol(), val);
							// CSV取込トラン_冷凍食品_商品
						}
					}
					array_tokso_bmn.add(obj_tokso_bmn);
					array_tokso_shn.add(obj_tokso_shn);
					//ooth.put(CSVSHNLayout.SEQ.getCol(), seq);				// CSV用.SEQ
					ooth.put(CSVSHNLayout.INPUTNO.getCol(), inputno);		// CSV用.入力番号
					array_ooth.add(ooth);

				}
				// 1.冷凍食品_企画
				JSONArray dataArray		 = this.selectTOKRS_KKK(array_tokso_bmn);
				// 2.冷凍食品_商品
				JSONArray dataArray_shn	 = this.selectTOKRS_SHN(array_tokso_shn, dataArray);

				JSONObject objset = new JSONObject();
				objset.put("DATA", dataArray);						// 対象情報（主要な更新情報）
				objset.put("DATA_SHN", dataArray_shn);				// 対象情報（生活応援＿商品）
				objset.put("DATA_OTHER", array_ooth);				// 対象情報（その他）
				dataList.add(objset);
			}

			int updCount = 0;
			int errCount = 0;

			// *** 詳細情報チェック＋情報登録用SQL作成 ***
			int userId = userInfo.getCD_user();

			// 各テーブル登録用のSQL作成
			for (int i = 0; i < dataList.size(); i++) {

				JSONObject objset = dataList.get(i);

				//String shncd = objset.optString(HATSKLayout.SHNCD.getCol());
				HashMap<String, String> sendmap = new HashMap<String, String>();
				sendmap.put("SENDBTNID", sendBtnid);		// 呼出しボタン

				// 1.冷凍食品＿企画
				JSONArray dataArray		 = objset.optJSONArray("DATA");
				// 2.冷凍食品＿商品
				JSONArray dataArray_shn	 = objset.optJSONArray("DATA_SHN");
				// 3.その他
				JSONArray ooth			 = objset.optJSONArray("DATA_OTHER");

				// 基本情報チェック(ファイル取込前チェック)
				JSONArray msgListB = this.checkData(map, userInfo, mu, dataArray, dataArray_shn,array_tokso_shn);


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
					// 登録前削除処理
					JSONObject dataDel = dataArray.getJSONObject(0);
					JSONObject msg = this.createSqlDelTOKRS(userInfo, dataDel, false);

					for (int j = 0; j < dataArray_shn.size(); j++) {
						checkData_Palent	 = new JSONArray();
						checkData_Child	 = new JSONArray();
						checkData_Palent.add(dataArray.get(j));
						checkData_Child.add(dataArray_shn.get(j));

						// 詳細情報チェック
						msgList = this.errCheckData(map, userInfo, mu, checkData_Palent, checkData_Child,array_tokso_shn);

						isError = msgList.size()!=0;

						if(isError){
							// エラー情報を格納
							errCount++;
							updDataListCSVErr.add(msgList.get(0));
						}else{
							// 登録対象データを格納
							updCount++;
							updDataList_SOBMN.add(dataArray.get(j));
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
						this.createSqlCSVTOKHEAD(userId, sysdate, seq, szCommentkn);
					}

					// エラー内容登録SQLの発行
					// CSV用情報セット
					String csvUpdkbn = DefineReport.ValCsvUpdkbn.NEW.getVal();
					this.csvtokrsshn_add_data[CSVTOK_RSSHNLayout.SEQ.getNo()-1]		 	= seq;																		// CSV用.SEQ
					this.csvtokrsshn_add_data[CSVTOK_RSSHNLayout.INPUTNO.getNo()-1]	 	= "1";
					this.csvtokrsshn_add_data[CSVTOK_RSSHNLayout.INPUTEDANO.getNo()-1]	 = ooth.getJSONObject(j).optString(CSVTOK_RSSHNLayout.INPUTNO.getCol());	// CSV用.入力番号
					this.csvtokrsshn_add_data[CSVTOK_RSSHNLayout.ERRCD.getNo()-1]		 = data.optString(CSVTOK_RSSHNLayout.ERRCD.getCol());						// ERRCD
					this.csvtokrsshn_add_data[CSVTOK_RSSHNLayout.ERRFLD.getNo()-1]		 = data.optString(CSVTOK_RSSHNLayout.ERRFLD.getCol());						// ERRFLD
					this.csvtokrsshn_add_data[CSVTOK_RSSHNLayout.ERRVL.getNo()-1]		 = data.optString(CSVTOK_RSSHNLayout.ERRVL.getCol());						// ERRVL
					this.csvtokrsshn_add_data[CSVTOK_RSSHNLayout.ERRTBLNM.getNo()-1]	 = data.optString(CSVTOK_RSSHNLayout.ERRTBLNM.getCol());					// ERRTBLNM
					this.csvtokrsshn_add_data[CSVTOK_RSSHNLayout.CSV_UPDKBN.getNo()-1]	 = data.optString(CSVTOK_RSSHNLayout.CSV_UPDKBN.getCol()); 					// CSV_UPDKBN
					this.csvtokrsshn_add_data[CSVTOK_RSSHNLayout.HBSTDT.getNo()-1]	 	 = data.optString(CSVTOK_RSSHNLayout.HBSTDT.getCol()); 						// HBSTDT
					this.csvtokrsshn_add_data[CSVTOK_RSSHNLayout.BMNCD.getNo()-1]	 	 = data.optString(CSVTOK_RSSHNLayout.BMNCD.getCol()); 						// BMNCD
					this.csvtokrsshn_add_data[CSVTOK_RSSHNLayout.WRITUKBN.getNo()-1]	 = data.optString(CSVTOK_RSSHNLayout.WRITUKBN.getCol()); 					// WRITUKBN
					this.csvtokrsshn_add_data[CSVTOK_RSSHNLayout.SEICUTKBN.getNo()-1]	 = data.optString(CSVTOK_RSSHNLayout.SEICUTKBN.getCol()); 					// SEICUTKBN
					this.csvtokrsshn_add_data[CSVTOK_RSSHNLayout.DUMMYCD.getNo()-1]	 	 = data.optString(CSVTOK_RSSHNLayout.DUMMYCD.getCol()); 					// DUMMYCD
					this.csvtokrsshn_add_data[CSVTOK_RSSHNLayout.KANRINO.getNo()-1]		 = data.optString(CSVTOK_RSSHNLayout.KANRINO.getCol()); 					// KANRINO
					this.csvtokrsshn_add_data[CSVTOK_RSSHNLayout.SHNCD.getNo()-1]		 = data.optString(CSVTOK_RSSHNLayout.SHNCD.getCol()); 						// SHNCD
					this.csvtokrsshn_add_data[CSVTOK_RSSHNLayout.MAKERKN.getNo()-1]		 = data.optString(CSVTOK_RSSHNLayout.MAKERKN.getCol()); 					// MAKERKN
					this.csvtokrsshn_add_data[CSVTOK_RSSHNLayout.SHNKN.getNo()-1]		 = data.optString(CSVTOK_RSSHNLayout.SHNKN.getCol()); 						// SHNKN
					this.csvtokrsshn_add_data[CSVTOK_RSSHNLayout.KIKKN.getNo()-1]	 	 = data.optString(CSVTOK_RSSHNLayout.KIKKN.getCol()); 						// KIKKN
					this.csvtokrsshn_add_data[CSVTOK_RSSHNLayout.IRISU.getNo()-1]	 	 = data.optString(CSVTOK_RSSHNLayout.IRISU.getCol()); 						// IRISU
					this.csvtokrsshn_add_data[CSVTOK_RSSHNLayout.BAIKAAM.getNo()-1]	 	 = data.optString(CSVTOK_RSSHNLayout.BAIKAAM.getCol()); 					// BAIKAAM
					this.csvtokrsshn_add_data[CSVTOK_RSSHNLayout.GENKAAM.getNo()-1]	 	 = data.optString(CSVTOK_RSSHNLayout.GENKAAM.getCol()); 					// GENKAAM

					// 規格情報
					this.csvtokrskkk_add_data[CSVTOK_RSKKKLayout.MEISHOKN.getNo()-1]	 = data.optString(CSVTOK_RSKKKLayout.MEISHOKN.getCol());					// MEISHOKN
					if(j==0){
						// CSV取込トラン_催し別送信情報更新
						JSONObject kkkrtn = this.createSqlCSVTOKRSSKKK(userId, dataArray,seq);
					}
					JSONObject shnrtn = this.createSqlCSVTOKRSSHN(userId, dataArray_shn,seq);
				}

				// エラー無しの子要素のデータが無い場合は登録を行わない。
				if(updDataList_SOSHN.size() > 0){

					// 主要情報以外のデータを格納
					//sendmap.put("DATA_SHN", updDataList_SOSHN.toString());
					// 親要素の情報を格納
					//updDataList_SOBMN = dataArray;

					// 登録処理
					//this.createCommandUpdateData( sendmap, userInfo, updDataList_SOBMN);
					//					this.sqlList.addAll(this.sqlList);
					//					this.prmList.addAll(this.prmList);
					//					this.lblList.addAll(this.lblList);


					// 取込前チェックにて親コードの重複が制限されていない為、
					// 親コード単位に登録処理を実行する。
					ArrayList<String> PRKEY	 = new ArrayList<String>();		// 主キー保持用変数
					for (int j = 0; j < updDataList_SOBMN.size(); j++) {
						JSONObject dataP = updDataList_SOBMN.getJSONObject(j);
						String ParentPrKey = "";
						JSONArray	updDataList_SOBMN_Af	= new JSONArray();
						JSONArray	updDataList_SOSHN_Af	= new JSONArray();
						ParentPrKey += dataP.optString(TOKRS_KKKLayout.HBSTDT.getId()).trim();
						ParentPrKey += dataP.optString(TOKRS_KKKLayout.BMNCD.getId()).trim();
						ParentPrKey += dataP.optString(TOKRS_KKKLayout.WRITUKBN.getId()).trim();
						ParentPrKey += dataP.optString(TOKRS_KKKLayout.SEICUTKBN.getId()).trim();
						ParentPrKey += dataP.optString(TOKRS_KKKLayout.DUMMYCD.getId()).trim();
						int index = PRKEY.indexOf(ParentPrKey);
						if(index == -1){
							PRKEY.add(ParentPrKey);
							updDataList_SOBMN_Af.add(dataP);

							// 親データと同じ主キーを持つ子データを保持する。
							for (int k = 0; k < updDataList_SOSHN.size(); k++) {
								JSONObject dataC = updDataList_SOSHN.getJSONObject(k);
								String ChildrenPrKey = "";
								ChildrenPrKey += dataC.optString(TOKRS_SHNLayout.HBSTDT.getId()).trim();
								ChildrenPrKey += dataC.optString(TOKRS_SHNLayout.BMNCD.getId()).trim();
								ChildrenPrKey += dataC.optString(TOKRS_SHNLayout.WRITUKBN.getId()).trim();
								ChildrenPrKey += dataC.optString(TOKRS_SHNLayout.SEICUTKBN.getId()).trim();
								ChildrenPrKey += dataC.optString(TOKRS_SHNLayout.DUMMYCD.getId()).trim();

								if(StringUtils.equals(ParentPrKey, ChildrenPrKey)){
									updDataList_SOSHN_Af.add(dataC);
								}
							}

							// 主要情報以外のデータを格納
							sendmap.put("DATA_SHN", updDataList_SOSHN_Af.toString());
							// SQL発行
							this.createCommandUpdateData( sendmap, userInfo, updDataList_SOBMN_Af);
						}
					}
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
				option.put(DefineReport.Text.SEQ.getObj(), seq);
				option.put(DefineReport.Text.STATUS.getObj(), "完了");
				option.put(DefineReport.Text.UPD_NUMBER.getObj(), updCount);
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

	public List<JSONObject> errCheckData(HashMap<String, String> map, User userInfo, MessageUtility mu,
			JSONArray dataArray,			// 対象情報（主要な更新情報）
			JSONArray dataArrayShn,			// 対象情報（追加更新情報）
			JSONArray array_tokso_shn
			) {
		JSONArray msg = new JSONArray();
		JSONObject data	 = new JSONObject();
		ItemList	iL	= new ItemList();

		String sqlcommand		 = "";
		ArrayList<String> paramData	 = new ArrayList<String>();

		// テーブル名定義
		String tokrs_kkk = "TOKRS_KKK";
		String tokrs_shn = "TOKRS_SHN";
		String errCd = "";

		// 1.冷凍食品_企画
		for (int i = 0; i < dataArray.size(); i++) {
			String updkbn = array_tokso_shn.getJSONObject(i).getString(FileLayout.UPDKBN.getCol());
			data = dataArray.optJSONObject(i);
			if(data.isEmpty()){
				continue;
			}

			TOKRS_KKKLayout[] targetCol = null;
			targetCol = new TOKRS_KKKLayout[]{TOKRS_KKKLayout.HBSTDT,TOKRS_KKKLayout.BMNCD,TOKRS_KKKLayout.SEICUTKBN,TOKRS_KKKLayout.DUMMYCD};
			for(TOKRS_KKKLayout colinf: targetCol){
				String val = StringUtils.trim(data.optString(colinf.getId()));
				if(StringUtils.isEmpty(val)){
					// エラー発生箇所を保存
					JSONObject o = mu.getDbMessageObj("E00001", new String[]{});
					o = this.setCsvSoErrinfo(o, errCd, tokrs_kkk, colinf.getText(),String.valueOf(i+1), data, dataArrayShn.optJSONObject(i),val,updkbn);
					msg.add(o);
					return msg;
				}
			}

			// 基本データチェック:入力値がテーブル定義と矛盾してないか確認
			errCd = "002";
			for(TOKRS_KKKLayout colinf: TOKRS_KKKLayout.values()){
				String val = StringUtils.trim(data.optString(colinf.getId()));
				if(StringUtils.isNotEmpty(val)){
					DataType dtype = null;
					int[] digit = null;
					try {
						DefineReport.InpText inpsetting = DefineReport.InpText.valueOf(colinf.getText());
						dtype = inpsetting.getType();
						digit = new int[]{inpsetting.getDigit1(), inpsetting.getDigit2()};
					}catch (IllegalArgumentException e){
						dtype = colinf.getDataType();
						digit = colinf.getDigit();
					}
					// ①データ型による文字種チェック
					if(!InputChecker.checkDataType(dtype, val)){
						//JSONObject o = mu.getDbMessageObjDataTypeErr(dtype, colinf.getText()+"は");
						JSONObject o = mu.getDbMessageObjDataTypeErr(dtype, new String[]{});
						o = this.setCsvSoErrinfo(o, errCd, tokrs_kkk, colinf.getText(),String.valueOf(i+1), data, dataArrayShn.optJSONObject(i),val,updkbn);
						msg.add(o);
						return msg;
					}

					if(!InputChecker.checkDataLen(dtype, val, digit)){
						//JSONObject o = mu.getDbMessageObjLen(dtype, colinf.getText()+"は");
						JSONObject o = mu.getDbMessageObjLen(dtype, new String[]{});
						o = this.setCsvSoErrinfo(o, errCd, tokrs_kkk, colinf.getText(),String.valueOf(i+1), data, dataArrayShn.optJSONObject(i),val,updkbn);
						msg.add(o);
						return msg;
					}
				}
			}


			// 例外チェック
			errCd = "003";

			// 入力チェック：販売開始日
			if(StringUtils.isNotEmpty(data.optString("F1"))){
				paramData  = new ArrayList<String>();
				paramData.add(data.getString("F1"));
				sqlcommand = "select case when SHORIDT > ? then 1 else 0 end as value from INAAD.SYSSHORIDT";

				@SuppressWarnings("static-access")
				JSONArray array = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
				if(NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) == 1){
					// 販売開始日 < 処理日付の場合
					JSONObject o = mu.getDbMessageObj("EX1124", new String[]{});
					o = this.setCsvSoErrinfo(o, errCd, tokrs_kkk, TOKRS_KKKLayout.HBSTDT.getText(),String.valueOf(i+1), data, dataArrayShn.optJSONObject(i),data.getString("F1"),updkbn);
					msg.add(o);
					return msg;
				}

				// 実在する日付か確認する。
				SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
				try {
					format.setLenient(false);
					format.parse(data.getString("F1"));
				} catch (Exception e) {

					JSONObject o = mu.getDbMessageObj("E30019", new String[]{});
					data.put("F1", "");
					o = this.setCsvSoErrinfo(o, errCd, tokrs_kkk, TOKRS_KKKLayout.HBSTDT.getText(),String.valueOf(i+1), data, dataArrayShn.optJSONObject(i),data.getString("F1"),updkbn);
					msg.add(o);
					return msg;
				}
			}

			// 入力チェック：ダミーコード
			if(StringUtils.isNotEmpty(data.optString("F5"))){
				if(StringUtils.equals("00000000", data.optString("F5"))){
					// ダミーコードALL0の場合
					JSONObject o = mu.getDbMessageObj("EX1047", new String[]{"ダミーコードは[00000000]以外の値"});
					o = this.setCsvSoErrinfo(o, errCd, tokrs_kkk, TOKRS_KKKLayout.DUMMYCD.getText(),String.valueOf(i+1), data, dataArrayShn.optJSONObject(i),data.getString("F5"),updkbn);
					msg.add(o);
					return msg;
				}
			}
		}

		// 2.冷凍食品_商品
		for (int i = 0; i < dataArrayShn.size(); i++) {
			String updkbn = array_tokso_shn.getJSONObject(i).getString(FileLayout.UPDKBN.getCol());
			data = dataArrayShn.optJSONObject(i);
			if(data.isEmpty()){
				continue;
			}

			errCd = "003";
			if(dataArrayShn.size() == 0 || dataArrayShn.getJSONObject(0).isEmpty()){
				JSONObject o = mu.getDbMessageObj("E00001", new String[]{});
				o = this.setCsvSoErrinfo(o, errCd, tokrs_shn, "",String.valueOf(i+1), dataArray.optJSONObject(i), data,"",updkbn);
				msg.add(o);
				return msg;
			}

			// 部門コードの存在チェック
			paramData  = new ArrayList<String>();
			paramData.add(data.getString("F2"));
			sqlcommand = "select count(1) as value from INAMS.MSTBMN where BMNCD = ?  and UPDKBN = " +DefineReport.ValUpdkbn.NML.getVal();

			@SuppressWarnings("static-access")
			JSONArray array = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
			if(array.size() != 1 ){
				JSONObject o = mu.getDbMessageObj("E11044");
				o = this.setCsvSoErrinfo(o, errCd, tokrs_shn, TOKRS_SHNLayout.BMNCD.getText(),String.valueOf(i+1), dataArray.optJSONObject(i), data,data.getString("F2"),updkbn);
				msg.add(o);
				return msg;
			}

			// 必須入力チェック
			errCd = "001";
			TOKRS_SHNLayout[] targetCol = null;
			targetCol = new TOKRS_SHNLayout[]{TOKRS_SHNLayout.HBSTDT,TOKRS_SHNLayout.BMNCD,TOKRS_SHNLayout.WRITUKBN,TOKRS_SHNLayout.SEICUTKBN,TOKRS_SHNLayout.DUMMYCD};
			for(TOKRS_SHNLayout colinf: targetCol){

				String val = StringUtils.trim(data.optString(colinf.getId()));
				if(StringUtils.isEmpty(val)){
					// エラー発生箇所を保存
					JSONObject o = mu.getDbMessageObj("E00001", new String[]{});
					o = this.setCsvSoErrinfo(o, errCd, tokrs_shn, colinf.getText(),String.valueOf(i+1), dataArray.optJSONObject(i), data,"",updkbn);
					msg.add(o);
					return msg;
				}
			}
			errCd = "002";
			// 基本データチェック:入力値がテーブル定義と矛盾してないか確認、
			for(TOKRS_SHNLayout colinf: TOKRS_SHNLayout.values()){
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
						data.element(colinf.getId(), "");		// CSVトラン用に空
						JSONObject o = mu.getDbMessageObjDataTypeErr(dtype, new String[]{});
						o = this.setCsvSoErrinfo(o, errCd, tokrs_shn, colinf.getText(),String.valueOf(i+1), dataArray.optJSONObject(i), data,val,updkbn);
						msg.add(o);
						return msg;
					}

					if(!InputChecker.checkDataLen(dtype, val, digit)){
						data.element(colinf.getId(), "");		// CSVトラン用に空
						JSONObject o = mu.getDbMessageObjLen(dtype, new String[]{});
						o = this.setCsvSoErrinfo(o, errCd, tokrs_shn, colinf.getText(),String.valueOf(i+1), dataArray.optJSONObject(i), data,val,updkbn);
						msg.add(o);
						return msg;
					}
				}
			}

			// 例外チェック
			errCd = "003";

			// 存在チェック：商品コード
			if(StringUtils.isNotEmpty(data.optString("F7"))){
				paramData  = new ArrayList<String>();
				paramData.add(data.getString("F7"));
				sqlcommand = "select COUNT(SHNCD) as value from INAMS.MSTSHN where COALESCE(UPDKBN, 0) <> 1 and SHNCD = ? ";

				array = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
				if(NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) == 0){
					JSONObject o = mu.getDbMessageObj("E11046", new String[]{});
					o = this.setCsvSoErrinfo(o, errCd, tokrs_shn, TOKRS_SHNLayout.SHNCD.getText(),String.valueOf(i+1), dataArray.optJSONObject(i), data,data.getString("F7"),updkbn);
					msg.add(o);
					return msg;
				}
			}

			// 必須入力チェック
			String shncd	 = data.getString("F7");
			String shnkn	 = data.getString("F9");
			String irisu	 = data.getString("F11");
			String sougaku	 = data.getString("F12");
			String genka	 = data.getString("F13");

			// 商品コード
			if(StringUtils.isEmpty(shncd)){
				JSONObject o = mu.getDbMessageObj("E30012", new String[]{TOKRS_SHNLayout.SHNCD.getText()});
				o = this.setCsvSoErrinfo(o, errCd, tokrs_shn, TOKRS_SHNLayout.SHNCD.getText(),String.valueOf(i+1), dataArray.optJSONObject(i), data, shncd, updkbn);
				msg.add(o);
				return msg;
			}

			// 商品名称
			if(StringUtils.isEmpty(shnkn)){
				JSONObject o = mu.getDbMessageObj("E30012", new String[]{TOKRS_SHNLayout.SHNKN.getText()});
				o = this.setCsvSoErrinfo(o, errCd, tokrs_shn, TOKRS_SHNLayout.SHNKN.getText(),String.valueOf(i+1), dataArray.optJSONObject(i), data, shnkn, updkbn);
				msg.add(o);
				return msg;
			}

			// 入数
			if(StringUtils.isEmpty(irisu)){
				JSONObject o = mu.getDbMessageObj("E30012", new String[]{TOKRS_SHNLayout.IRISU.getText()});
				o = this.setCsvSoErrinfo(o, errCd, tokrs_shn, TOKRS_SHNLayout.IRISU.getText(),String.valueOf(i+1), dataArray.optJSONObject(i), data, irisu, updkbn);
				msg.add(o);
				return msg;
			}

			// 売価
			if(StringUtils.isEmpty(sougaku)){
				JSONObject o = mu.getDbMessageObj("E30012", new String[]{TOKRS_SHNLayout.BAIKAAM.getText()});
				o = this.setCsvSoErrinfo(o, errCd, tokrs_shn, TOKRS_SHNLayout.BAIKAAM.getText(),String.valueOf(i+1), dataArray.optJSONObject(i), data, sougaku,updkbn);
				msg.add(o);
				return msg;
			}

			// 原価
			if(StringUtils.isEmpty(genka)){
				JSONObject o = mu.getDbMessageObj("E30012", new String[]{TOKRS_SHNLayout.GENKAAM.getText()});
				o = this.setCsvSoErrinfo(o, errCd, tokrs_shn, TOKRS_SHNLayout.GENKAAM.getText(),String.valueOf(i+1), dataArray.optJSONObject(i), data, genka, updkbn);
				msg.add(o);
				return msg;
			}
		}
		return msg;
	}

	public JSONObject setCsvSoErrinfo(JSONObject o, String errCd, String errtblnm, String errfld, String inputno, JSONObject dataKKK, JSONObject dataSHN ,String val,String updkbn ) {

		// SQLエラーとなるデータを取り除く
		dataKKK = this.removeErrData(dataKKK, true);
		dataSHN = this.removeErrData(dataSHN, false);

		// 商品情報
		o.put(CSVTOK_RSSHNLayout.INPUTEDANO.getCol(),	inputno);
		o.put(CSVTOK_RSSHNLayout.ERRCD.getCol(),		errCd);
		o.put(CSVTOK_RSSHNLayout.ERRFLD.getCol(),		errfld);
		o.put(CSVTOK_RSSHNLayout.ERRVL.getCol(),		val);
		o.put(CSVTOK_RSSHNLayout.ERRTBLNM.getCol(),		o.optString(MessageUtility.MSG));
		o.put(CSVTOK_RSSHNLayout.CSV_UPDKBN.getCol(),	updkbn);
		o.put(CSVTOK_RSSHNLayout.HBSTDT.getCol(),		dataSHN.optString(TOKRS_SHNLayout.HBSTDT.getId()));
		o.put(CSVTOK_RSSHNLayout.BMNCD.getCol(),		dataSHN.optString(TOKRS_SHNLayout.BMNCD.getId()));
		o.put(CSVTOK_RSSHNLayout.WRITUKBN.getCol(),		dataSHN.optString(TOKRS_SHNLayout.WRITUKBN.getId()));
		o.put(CSVTOK_RSSHNLayout.SEICUTKBN.getCol(),	dataSHN.optString(TOKRS_SHNLayout.SEICUTKBN.getId()));
		o.put(CSVTOK_RSSHNLayout.DUMMYCD.getCol(),		dataSHN.optString(TOKRS_SHNLayout.DUMMYCD.getId()));
		o.put(CSVTOK_RSSHNLayout.KANRINO.getCol(),		dataSHN.optString(TOKRS_SHNLayout.KANRINO.getId()));
		o.put(CSVTOK_RSSHNLayout.SHNCD.getCol(),		dataSHN.optString(TOKRS_SHNLayout.SHNCD.getId()));
		o.put(CSVTOK_RSSHNLayout.MAKERKN.getCol(),		dataSHN.optString(TOKRS_SHNLayout.MAKERKN.getId()));
		o.put(CSVTOK_RSSHNLayout.SHNKN.getCol(),		dataSHN.optString(TOKRS_SHNLayout.SHNKN.getId()));
		o.put(CSVTOK_RSSHNLayout.KIKKN.getCol(),		dataSHN.optString(TOKRS_SHNLayout.KIKKN.getId()));
		o.put(CSVTOK_RSSHNLayout.IRISU.getCol(),		dataSHN.optString(TOKRS_SHNLayout.IRISU.getId()));
		o.put(CSVTOK_RSSHNLayout.BAIKAAM.getCol(),		dataSHN.optString(TOKRS_SHNLayout.BAIKAAM.getId()));
		o.put(CSVTOK_RSSHNLayout.GENKAAM.getCol(),		dataSHN.optString(TOKRS_SHNLayout.GENKAAM.getId()));

		// 企画情報
		o.put(CSVTOK_RSKKKLayout.MEISHOKN.getCol(),		dataKKK.optString(TOKRS_KKKLayout.MEISHOKN.getId()));

		// エラー項目判定
		/*if (errtblnm.equals("TOKBM_SHN")) {
			o.put(CSVTOKLayout.SHNCD.getCol(), jo.optString(TOKBMSHNLayout.SHNCD.getId()));
		}*/

		return o;
	}

	// CSYエラー登録時に、レイアウトに対してエラーとなる項目を空に置き換える。
	public JSONObject removeErrData (JSONObject row, boolean parent) {
		JSONObject updateRow = new JSONObject();

		if(parent){
			for(TOKRS_KKKLayout colinf: TOKRS_KKKLayout.values()){
				String val = StringUtils.trim(row.optString(colinf.getId()));
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
						val = "";
					}
					// ②データ桁チェック
					if(!InputChecker.checkDataLen(dtype, val, digit)){
						val = "";
					}
				}
				updateRow.put(colinf.getId(), val);
			}

		}else{
			for(TOKRS_SHNLayout colinf: TOKRS_SHNLayout.values()){
				String val = StringUtils.trim(row.optString(colinf.getId()));
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
						val = "";
					}
					// ②データ桁チェック
					if(!InputChecker.checkDataLen(dtype, val, digit)){
						val = "";
					}
				}
				updateRow.put(colinf.getId(), val);
			}

		}
		return updateRow;
	}

	private JSONArray checkData(HashMap<String, String> map, User userInfo, MessageUtility mu,
			JSONArray dataArray,			// 対象情報（主要な更新情報）
			JSONArray dataArrayShn,			// 対象情報（追加更新情報）
			JSONArray array_tokso_shn			// 対象情報（追加更新情報）
			) {

		JSONArray msg	 = new JSONArray();
		ItemList	iL	= new ItemList();

		// 冷凍食品_企画：データチェック
		if(dataArray.size() > 0){
			JSONObject data	 = new JSONObject();
			HashSet<String> checkPramsKEY = new HashSet<String>();

			//更新区分のチェック
			for (int j = 0; j < array_tokso_shn.size(); j++) {
				String updkbn = array_tokso_shn.getJSONObject(j).getString(FileLayout.UPDKBN.getCol());
				if(!("A".equals(updkbn)||"U".equals(updkbn))){
					JSONObject o = mu.getDbMessageObj("E40009", new String[]{});
					msg.add(o);
					return msg;
				}
			}

			for (int j = 0; j < dataArray.size(); j++) {
				data = dataArray.getJSONObject(j);
				if(data.isEmpty()){
					continue;
				}
				checkPramsKEY.add(data.optString("F1")+data.optString("F2")+data.optString("F3")+data.optString("F4")+data.optString("F5"));
			}
			// 種類チェック：販売開始日＋部門＋割引率区分+正規・カット+ダミーコード
			// TODO
			// 取り込み前チェックを行わない。
			/*if(checkPramsKEY.size() != 1){
				// 1種類以上のパラメータが入力されている
				//JSONObject o = MessageUtility.getDbMessageIdObj("E40116", new String[]{});
				JSONObject o = mu.getDbMessageObj("E40116", new String[]{});
				msg.add(o);
				return msg;
			}*/
			ArrayList<String> paramData	 = new ArrayList<String>();

			/*for(TOKRS_KKKLayout colinf: TOKRS_KKKLayout.values()){
				String val = StringUtils.trim(data.optString(colinf.getId()));
				if(StringUtils.isNotEmpty(val)){
					DataType dtype = null;
					int[] digit = null;
					try {
						DefineReport.InpText inpsetting = DefineReport.InpText.valueOf(colinf.getText());
						dtype = inpsetting.getType();
						digit = new int[]{inpsetting.getDigit1(), inpsetting.getDigit2()};
					}catch (IllegalArgumentException e){
						dtype = colinf.getDataType();
						digit = colinf.getDigit();
					}
					// ①データ型による文字種チェック
					if(!InputChecker.checkDataType(dtype, val)){
						//JSONObject o = mu.getDbMessageObjDataTypeErr(dtype, colinf.getText()+"は");
						JSONObject o = mu.getDbMessageObjLen(dtype, new String[]{});
						msg.add(o);
						return msg;
					}

					if(!InputChecker.checkDataLen(dtype, val, digit)){
						//JSONObject o = mu.getDbMessageObjLen(dtype, colinf.getText()+"は");
						JSONObject o = mu.getDbMessageObjLen(dtype, new String[]{});
						msg.add(o);
						return msg;
					}
				}
			}*/

			/*SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
			try {
				format.setLenient(false);
				format.parse(data.getString("F1"));
			} catch (Exception e) {
				JSONObject o = mu.getDbMessageObj("E30019", new String[]{});
				msg.add(o);
				return msg;
			}*/

//			if(!InputChecker.checkDataType(dtype, data.getString("F5"))){
//				//JSONObject o = mu.getDbMessageObjDataTypeErr(dtype, colinf.getText()+"は");
//				JSONObject o = mu.getDbMessageObjLen(dtype, new String[]{});
//				msg.add(o);
//				return msg;
//			}
			//　商品マスタ存在チェック
			String sqlcommand = "";
			/*paramData  = new ArrayList<String>();
			paramData.add(data.getString("F5"));
			sqlcommand = "select count(1) as value from INAMS.MSTSHN where SHNCD = ?  and UPDKBN = " +DefineReport.ValUpdkbn.NML.getVal();
			@SuppressWarnings("static-access")
			JSONArray array1 = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
			if(!"1".equals(array1.getJSONObject(0).optString("VALUE"))  ){
				JSONObject o = mu.getDbMessageObj("E20160");
				msg.add(o);
				return msg;
			}*/
			//ダミーコードの商品種類チェック
			paramData  = new ArrayList<String>();
			paramData.add(data.getString("F5"));
			sqlcommand = "select SHNKBN as VALUE from INAMS.MSTSHN where SHNCD = ? and UPDKBN = " +DefineReport.ValUpdkbn.NML.getVal();

			@SuppressWarnings("static-access")
			JSONArray array2 = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
			if(array2.size() > 0){
				if(!"5".equals(array2.getJSONObject(0).optString("VALUE")) ){
					JSONObject o = mu.getDbMessageObj("E20488");
					msg.add(o);
					return msg;
				}
			}

			//ダミーコードの部門チェック
			if(StringUtils.isNotEmpty(data.getString("F2")) && StringUtils.isNotEmpty(data.getString("F5"))){
				if(!String.format("%02d", Integer.parseInt(data.getString("F2"))).equals(data.getString("F5").substring(0,2)) ){
					JSONObject o = mu.getDbMessageObj("E30017");
					msg.add(o);
					return msg;
				}
			}

			paramData  = new ArrayList<String>();
			paramData.add(data.getString("F2"));
			sqlcommand = "select count(1) as value from INAMS.MSTBMN where BMNCD = ?  and UPDKBN = " +DefineReport.ValUpdkbn.NML.getVal();

			@SuppressWarnings("static-access")
			JSONArray array3 = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
			if(array3.size() != 1 ){
				JSONObject o = mu.getDbMessageObj("E11044");
				msg.add(o);
				return msg;
			}
			return msg;
		}

		// 冷凍食品_商品：データチェック
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
			}

			// 重複チェック(入力値)：商品
			if(checkPramsSHNCD.size() != SHNCD.size()){
				// 重複する店舗コードが含まれている場合。
				JSONObject o = mu.getDbMessageObj("E40116", new String[]{});
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

		// 3.項目数チェック
		if(FileLayout.values().length != eL.get(idxHeader).length){
			JSONObject o = MessageUtility.getDbMessageIdObj("E40008", new String[]{});
			msg.add(o);
			return msg;
		}
		return msg;
	}

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
	private JSONObject createCommandUpdateData( HashMap<String, String> map, User userInfo,JSONArray dataArray) throws Exception {

		JSONObject option = new JSONObject();

		map.put("SENDBTNID", DefineReport.Button.NEW.getObj());		// 呼出しボタンを新規に変更
		//map.put("DATA_SHN", new JSONArray().toString());			// 空データ:生活応援＿商品
		map.put("DATA_SHN_DEL", new JSONArray().toString());		// 空データ:生活応援＿商品削除

		JSONObject data = dataArray.optJSONObject(0);				// 親テーブルの為１件文のデータのみを送る。

		// 冷凍食品_企画　冷凍食品_商品
		// 基本登録情報
		if(data.size() > 0){
			this.createSqlTOKSO(data,map,userInfo);
		}

		return option;
	}

	/**
	 * 冷凍食品_企画、冷凍食品_商品INSERT/UPDATE処理
	 *
	 * @param data
	 * @param map
	 * @param userInfo
	 */
	public String createSqlTOKSO(JSONObject data, HashMap<String, String> map, User userInfo){

		StringBuffer sbSQL		= new StringBuffer();
		JSONArray dataArrayT	= JSONArray.fromObject(map.get("DATA_SHN"));		// 更新情報(予約発注_納品日)
		JSONArray dataArrayDel	= JSONArray.fromObject(map.get("DATA_SHN_DEL"));	// 対象情報（主要な更新情報）


		// ログインユーザー情報取得
		String userId	= userInfo.getId();							// ログインユーザー
		String kanriNo	= "";										// 管理No
		String dispType	= map.get("DISPTYPE");						// 画面状態

		ArrayList<String> prmData = new ArrayList<String>();
		Object[] valueData = new Object[]{};
		String values = "";

		int maxField = 6;		// Fxxの最大値
		for (int k = 1; k <= maxField; k++) {
			String key = "F" + String.valueOf(k);

			String val = data.optString(key);
			if(StringUtils.isEmpty(val)){
				values += "null　,";
			}else{
				values += "? ,";
				prmData.add(val);
			}

			if(k == maxField){
	            values += " " + DefineReport.ValUpdkbn.NML.getVal() + " ";
	            values += ", 0";
	            values += ", '" + userId + "'";
	            values += ", CURRENT_TIMESTAMP";
	            values += ", CURRENT_TIMESTAMP";
	            
				valueData = ArrayUtils.add(valueData, "(" + values + ")");
				values = "";
			}
		}

		// 冷凍食品_企画の登録・更新
		sbSQL = new StringBuffer();
		sbSQL.append(" INSERT into INATK.TOKRS_KKK (");
		sbSQL.append(" HBSTDT");													// 催し開始日
		sbSQL.append(", BMNCD");													// 部門コード
		sbSQL.append(", WRITUKBN");													// 割り付き区分
		sbSQL.append(", SEICUTKBN");												// 正規カット
		sbSQL.append(", DUMMYCD");													// ダミーコード
		sbSQL.append(", MEISHOKN");													// 名称漢字名
		sbSQL.append(", UPDKBN");		                                            // 更新区分：
		sbSQL.append(", SENDFLG");	                                                // 送信フラグ
		sbSQL.append(", OPERATOR ");                  								// オペレーター：
		sbSQL.append(", ADDDT ");	                    							// 登録日：
		sbSQL.append(", UPDDT ");                                                	// 更新日：
        sbSQL.append(") ");
        sbSQL.append("VALUES ");
        sbSQL.append(StringUtils.join(valueData, ",") + "AS NEW ");
        sbSQL.append("ON DUPLICATE KEY UPDATE ");
		sbSQL.append("HBSTDT = NEW.HBSTDT");
		sbSQL.append(", BMNCD = NEW.BMNCD");
		sbSQL.append(", WRITUKBN = NEW.WRITUKBN");
		sbSQL.append(", SEICUTKBN = NEW.SEICUTKBN");
		sbSQL.append(", DUMMYCD = NEW.DUMMYCD");
		sbSQL.append(", MEISHOKN = NEW.MEISHOKN");
	    sbSQL.append(", UPDKBN = NEW.UPDKBN"); 
	    sbSQL.append(", SENDFLG = NEW.SENDFLG"); 
	    sbSQL.append(", OPERATOR = NEW.OPERATOR");                                                          
	    sbSQL.append(", UPDDT = NEW.UPDDT ");   


		if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("冷凍食品_企画");

		// クリア
		prmData = new ArrayList<String>();
		valueData = new Object[]{};
		values = "";

		maxField = 13;		// Fxxの最大値
		int len = dataArrayT.size();
		for (int i = 0; i < len; i++) {
			sbSQL		= new StringBuffer();
			JSONObject dataT = dataArrayT.getJSONObject(i);
			if(! dataT.isEmpty()){
				for (int k = 1; k <= maxField; k++) {
					String key = "F" + String.valueOf(k);

					String val = dataT.optString(key);

					// 新規登録時は管理番号を使用する。
					if(StringUtils.isEmpty(val)){
						values += "null ,";
					}else{
						values += "? ,";
						prmData.add(val);
					}

					if(k == maxField){
						values = StringUtils.removeStart(values, ",");
						valueData = ArrayUtils.add(valueData, "(" + values + ")");
						values = "";
					}
				}
			}
			//	冷凍食品_商品
			sbSQL = new StringBuffer();
			sbSQL.append(" INSERT into INATK.TOKRS_SHN (");
			sbSQL.append(" HBSTDT");													// 催し開始日
			sbSQL.append(", BMNCD");													// 部門コード
			sbSQL.append(", WRITUKBN");													// 割率区分
			sbSQL.append(", SEICUTKBN");												// 正規カット区分
			sbSQL.append(", DUMMYCD");													// ダミーコード
			sbSQL.append(", KANRINO");													// 管理番号
			sbSQL.append(", SHNCD");													// 商品コード
			sbSQL.append(", MAKERKN");													// メーカー漢字名
			sbSQL.append(", SHNKN");													// 商品名漢字
			sbSQL.append(", KIKKN");													// 規格名
			sbSQL.append(", IRISU");													// 入数
			sbSQL.append(", BAIKAAM");													// 売価
			sbSQL.append(", GENKAAM");													// 原価
			sbSQL.append(", UPDKBN");		                                            // 更新区分：
			sbSQL.append(", SENDFLG");	                                                // 送信フラグ
			sbSQL.append(", OPERATOR ");								                // オペレーター：
			sbSQL.append(", ADDDT ");								                    // 登録日：
			sbSQL.append(", UPDDT ");								                    // 更新日：
	        sbSQL.append(") ");
	        sbSQL.append("VALUES ");
	        sbSQL.append(StringUtils.join(valueData, ",") + "AS NEW ");
	        sbSQL.append("ON DUPLICATE KEY UPDATE ");
			sbSQL.append("HBSTDT = NEW.HBSTDT");
			sbSQL.append(", BMNCD = NEW.BMNCD");
			sbSQL.append(", WRITUKBN = NEW.WRITUKBN");
			sbSQL.append(", SEICUTKBN = NEW.SEICUTKBN");
			sbSQL.append(", DUMMYCD = NEW.DUMMYCD");
			sbSQL.append(", KANRINO = NEW.KANRINO");
			sbSQL.append(", SHNCD = NEW.SHNCD");
			sbSQL.append(", MAKERKN = NEW.MAKERKN");
			sbSQL.append(", SHNKN = NEW.SHNKN");
			sbSQL.append(", KIKKN = NEW.KIKKN");
			sbSQL.append(", IRISU = NEW.IRISU");
			sbSQL.append(", BAIKAAM = NEW.BAIKAAM");
			sbSQL.append(", GENKAAM = NEW.GENKAAM");
	        sbSQL.append(", UPDKBN = NEW.UPDKBN");                                             
	        sbSQL.append(", SENDFLG = NEW.SENDFLG");                                                
	        sbSQL.append(", OPERATOR = NEW.OPERATOR");                                                                                      
	        sbSQL.append(", UPDDT = NEW.UPDDT");

			if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

			sqlList.add(sbSQL.toString());
			prmList.add(prmData);
			lblList.add("冷凍食品_商品");

			// クリア
			prmData = new ArrayList<String>();
			valueData = new Object[]{};
			values = "";
		}
		// クリア
		prmData = new ArrayList<String>();
		valueData = new Object[]{};
		values = "";

		maxField = 6;		// Fxxの最大値
		len = dataArrayT.size();
		for (int i = 0; i < len; i++) {
			sbSQL		= new StringBuffer();
			JSONObject dataT = dataArrayT.getJSONObject(i);
			if(! dataT.isEmpty()){
				for (int k = 1; k <= maxField; k++) {
					String key = "F" + String.valueOf(k);

					String val = dataT.optString(key);

					// 新規登録時は管理番号を使用する。
					if(StringUtils.isEmpty(val)){
						values += "null ,";
					}else{
						values += "? ,";
						prmData.add(val);
					}

					if(k == maxField){
			            values += ", '" + userId + "'";
			            values += ", CURRENT_TIMESTAMP";
			            values += ", CURRENT_TIMESTAMP";
			            
						valueData = ArrayUtils.add(valueData, "(" + values + ")");
						values = "";
					}
				}
			}
			//	冷凍食品内部管理
			sbSQL.append(" INSERT into INATK.SYSRS (");
			sbSQL.append(" HBSTDT");													// 催し開始日
			sbSQL.append(", BMNCD");													// 部門コード
			sbSQL.append(", WRITUKBN");													// 割率区分
			sbSQL.append(", SEICUTKBN");												// 正規カット区分
			sbSQL.append(", DUMMYCD");													// ダミーコード
			sbSQL.append(", SUMI_KANRINO");												// 管理番号
			sbSQL.append(", OPERATOR ");								                // オペレーター：
			sbSQL.append(", ADDDT ");								                    // 登録日：
			sbSQL.append(", UPDDT ");								                    // 更新日：
	        sbSQL.append(") ");
	        sbSQL.append("VALUES ");
	        sbSQL.append(StringUtils.join(valueData, ",") + "AS NEW ");
			sbSQL.append("HBSTDT = NEW.HBSTDT");
			sbSQL.append(", BMNCD = NEW.BMNCD");
			sbSQL.append(", WRITUKBN = NEW.WRITUKBN");
			sbSQL.append(", SEICUTKBN = NEW.SEICUTKBN");
			sbSQL.append(", DUMMYCD = NEW.DUMMYCD");
			sbSQL.append(", SUMI_KANRINO = NEW.SUMI_KANRINO");
	        sbSQL.append(", OPERATOR = NEW.OPERATOR");                                                // オペレーター：
	        sbSQL.append(", UPDDT = NEW.UPDDT");


			if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

			sqlList.add(sbSQL.toString());
			prmList.add(prmData);
			lblList.add("冷凍食品内部管理");

			// クリア
			prmData = new ArrayList<String>();
			valueData = new Object[]{};
			values = "";
		}

		return sbSQL.toString();
	}

	/**
	 * 冷凍食品DELETE SQL作成処理
	 *
	 * @param userInfo
	 * @param Sqlprm		 入力No
	 * @throws Exception
	 */
	public JSONObject createSqlDelTOKRS(User userInfo, JSONObject data, boolean fullDelFlg){
		JSONObject result = new JSONObject();

		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();
		JSONObject msgObj = new JSONObject();
		String userId		= userInfo.getId();											// ログインユーザー
		String hbstdt		= data.optString("F1");
		String bmncd		= data.optString("F2");
		String waritukbn	= data.optString("F3");
		String seicutkbn	= data.optString("F4");
		String dummycd		= data.optString("F5");

		StringBuffer sbSQL;

		// 全削除ボタン押下時
		sbSQL = new StringBuffer();
		prmData.add(hbstdt);
		prmData.add(bmncd);
		prmData.add(waritukbn);
		prmData.add(seicutkbn);
		prmData.add(dummycd);
		//	冷凍食品_企画
		sbSQL.append("UPDATE INATK.TOKRS_KKK");
		sbSQL.append(" SET ");
		sbSQL.append(" UPDKBN=" + DefineReport.ValUpdkbn.DEL.getVal());
		sbSQL.append(",OPERATOR='" + userId + "'");
		sbSQL.append(",UPDDT=current_timestamp ");
		sbSQL.append(" WHERE HBSTDT = ?");
		sbSQL.append(" and BMNCD = ?");
		sbSQL.append(" and WRITUKBN = ?");
		sbSQL.append(" and SEICUTKBN = ?");
		sbSQL.append(" and DUMMYCD = ?");

		int count = super.executeSQL(sbSQL.toString(), prmData);
		if(count>0) {
			msgObj.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00002.getVal()));
		}else{
			msgObj.put(MsgKey.E.getKey(), getMessage());
		}

		if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		// 冷凍食品_商品
		sbSQL = new StringBuffer();
		sbSQL.append("UPDATE INATK.TOKRS_SHN");
		sbSQL.append(" SET ");
		sbSQL.append(" UPDKBN=" + DefineReport.ValUpdkbn.DEL.getVal());
		sbSQL.append(",OPERATOR='" + userId + "'");
		sbSQL.append(",UPDDT=current_timestamp ");
		sbSQL.append(" WHERE HBSTDT = ?");
		sbSQL.append(" and BMNCD = ?");
		sbSQL.append(" and WRITUKBN = ?");
		sbSQL.append(" and SEICUTKBN = ?");
		sbSQL.append(" and DUMMYCD = ?");

		count = super.executeSQL(sbSQL.toString(), prmData);
		if(count>0) {
			msgObj.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00002.getVal()));
		}else{
			msgObj.put(MsgKey.E.getKey(), getMessage());
		}

		//CSV取込トラン_冷凍食品_企画
		sbSQL = new StringBuffer();
		sbSQL.append("UPDATE INATK.CSVTOK_RSKKK");
		sbSQL.append(" SET ");
		sbSQL.append(" UPDKBN=" + DefineReport.ValUpdkbn.DEL.getVal());
		sbSQL.append(",OPERATOR='" + userId + "'");
		sbSQL.append(",UPDDT=current_timestamp ");
		sbSQL.append(" WHERE HBSTDT = ?");
		sbSQL.append(" and BMNCD = ?");
		sbSQL.append(" and WRITUKBN = ?");
		sbSQL.append(" and SEICUTKBN = ?");
		sbSQL.append(" and DUMMYCD = ?");

		count = super.executeSQL(sbSQL.toString(), prmData);
		if(count>0) {
			msgObj.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00002.getVal()));
		}else{
			msgObj.put(MsgKey.E.getKey(), getMessage());
		}

		return result;
	}


	/**
	 * SEQ情報取得処理
	 *
	 * @throws Exception
	 */
	public String getCSVTOK_SEQ() {
		// 関連情報取得
		new ItemList();
		String sqlColCommand = "SELECT INAMS.nextval('SEQ010') AS \"1\" ";
		@SuppressWarnings("static-access")
		JSONArray array = ItemList.selectJSONArray(sqlColCommand, null, Defines.STR_JNDI_DS);
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
	public JSONObject createSqlCSVTOKHEAD(int userId, String sysdate, String seq, String commentkn) {
		JSONObject result = new JSONObject();
		String dbsysdate = CmnDate.dbDateFormat(sysdate);
		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();

		LinkedList<String> valList = new LinkedList<String>();
		valList.add("");	//

		String values = "";
		values += " " + seq;
		values += "," + userId;
		values += ",current_timestamp ";
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
	 * 冷凍食品規格情報取得処理
	 *
	 * @param isSei	正/予約
	 * @param isNew	新規/更新
	 * @param data	CSV抜粋データ
	 *
	 * @throws Exception
	 */
	public JSONArray selectTOKRS_KKK(JSONArray array) {
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
			for(TOKRS_KKKLayout itm :TOKRS_KKKLayout.values()){
				String value = "";
				value = dataBf.getString(itm.getCol());
				dataAf.put("F"+ itm.getNo(), value);
			}
			dataArray.add(dataAf);
		}
		return dataArray;
	}


	/**
	 * 冷凍食品_商品情報取得処理
	 *
	 * @param isSei	正/予約
	 * @param isNew	新規/更新
	 * @param data	CSV抜粋データ
	 *
	 * @throws Exception
	 */
	public JSONArray selectTOKRS_SHN(JSONArray childArray, JSONArray parentArray) {
		JSONObject dataAf = new JSONObject();
		JSONObject dataBf = new JSONObject();
		JSONArray	dataArray	= new JSONArray();	// 対象情報（主要な更新情報）
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
			dataAf.put("F5", parentArray.getJSONObject(j).opt("F5"));

			if(dataBf.isEmpty()){
				continue;
			}
			dataAf.put("F6", String.valueOf(j+1));
			for(TOKRS_SHNLayout itm :TOKRS_SHNLayout.values()){
				//if(dataBf.containsKey(itm.getCol()) && StringUtils.isNotEmpty(dataBf.optString(itm.getCol()))){
				if(dataBf.containsKey(itm.getCol())){
					String value = dataBf.getString(itm.getCol());

					// Nullが含まれている場合は置き換える。
					Matcher m = p.matcher(value);
					if (m.find()){
						value = "";
					}

					/*if(StringUtils.equals(itm.getCol(), FileLayout.UPDKBN.getCol())){
						if(StringUtils.equals("U", value)){
							// 変更データの場合
							// DBから管理Noを取得
							kanriNo = this.getKANRINO(dataBf.getString("F1"), dataBf.getString("F2"), dataBf.getString("F3"), dataBf.getString("F4"), dataBf.getString("F6"));
							dataAf.put("F5", kanriNo);
						}
					}*/

					if(! ArrayUtils.contains(new String[]{"F6"}, "F"+ itm.getNo())){
						dataAf.put("F"+ itm.getNo(), value);
					}

				}else{
					//dataAf.put("F"+itm.getCol(), "");
				}
			}
			dataArray.add(dataAf);
		}

		// 重複し商品コードがある場合、後データを優先する
		ArrayList<String> SHNCD	 = new ArrayList<String>();
		JSONArray	dataArray2	= new JSONArray();

		for (int j = 0; j < dataArray.size(); j++) {
			JSONObject data = dataArray.getJSONObject(j);
			String shncd = data.optString("F7").trim();
			int index = SHNCD.indexOf(shncd);
			if(index == -1){
				SHNCD.add(shncd);
				dataArray2.add(data);
			}else{
				for (int k = 0; k < dataArray2.size(); k++) {
					if(StringUtils.equals(shncd, dataArray2.getJSONObject(k).optString("F7").trim())){
						dataArray2.remove(k);
						dataArray2.add(data);
						break;
					}
				}
			}
		}
		//return dataArray;
		return dataArray2;
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
		sbSQL.append(" SUMI_KANRINO ");
		sbSQL.append(" from INATK.SYSRS ");
		sbSQL.append(" where HBSTDT = ? ");
		sbSQL.append(" and BMNCD = ? ");
		sbSQL.append(" and WRITUKBN = ? ");
		sbSQL.append(" and SEICUTKBN = ? ");
		sbSQL.append(" and DUMMYCD = ? ");

		@SuppressWarnings("static-access")
		JSONArray array = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);
		String value = "";
		if(array.size() > 0){
			value = array.optJSONObject(0).optString("SUMI_KANRINO");
		}
		/*else{
			value = "1";
		}*/
		return value;
	}

	/**
	 * CSV取込トラン_冷凍食品企画INSERT処理
	 *
	 * @param userId
	 * @param data
	 * @param tbl
	 *
	 * @throws Exception
	 */
	public JSONObject createSqlCSVTOKRSSKKK(int userId, JSONArray dataArray, String seq) {
		JSONObject result = new JSONObject();
		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();

		LinkedList<String> valList = new LinkedList<String>();
		valList.add("");	//
		int i = 1;
	    Object[] valueData = new Object[] {};
		String values = "";
		for(CSVTOK_RSKKKLayout itm :CSVTOK_RSKKKLayout.values()){
			String val = "";
			if(itm.getNo()==1){
				val = seq;
			}else if(itm.getNo()==2){
				val = "1";

			}else if(itm.getCol()==CSVTOK_RSKKKLayout.HBSTDT.getCol()){
				// 販売開始日
				val = ((String) this.csvtokrsshn_add_data[CSVTOK_RSSHNLayout.HBSTDT.getNo()-1]);

			}else if(itm.getCol()==CSVTOK_RSKKKLayout.BMNCD.getCol()){
				// 部門
				val = ((String) this.csvtokrsshn_add_data[CSVTOK_RSSHNLayout.BMNCD.getNo()-1]);

			}else if(itm.getCol()==CSVTOK_RSKKKLayout.WRITUKBN.getCol()){
				// 割引率区分
				val = ((String) this.csvtokrsshn_add_data[CSVTOK_RSSHNLayout.WRITUKBN.getNo()-1]);

			}else if(itm.getCol()==CSVTOK_RSKKKLayout.SEICUTKBN.getCol()){
				// 正規・カット
				val = ((String) this.csvtokrsshn_add_data[CSVTOK_RSSHNLayout.SEICUTKBN.getNo()-1]);

			}else if(itm.getCol()==CSVTOK_RSKKKLayout.DUMMYCD.getCol()){
				// ダミーコード
				val = ((String) this.csvtokrsshn_add_data[CSVTOK_RSSHNLayout.DUMMYCD.getNo()-1]);

			}else if(itm.getNo()==8){
				// 企画名称
				val = ((String) this.csvtokrskkk_add_data[itm.getNo()-1]);
			}else{
				String key = "F" + String.valueOf(i);
				val = dataArray.getJSONObject(0).optString(key);
				i++;
			}

			if(StringUtils.isEmpty(val)){
				values += "null ,";
			}else{
				values += "? ,";
				prmData.add(val);
			}
		}
		//values = StringUtils.removeEnd(values, ",");
		
        values += " " + DefineReport.ValUpdkbn.NML.getVal() + " ";
        values += ", '" + userId + "'";
        values += ", CURRENT_TIMESTAMP";
        values += ", CURRENT_TIMESTAMP";
        
        valueData = ArrayUtils.add(valueData, "(" + values + ")");
        values = "";
        
		StringBuffer sbSQL;
		sbSQL = new StringBuffer();
		sbSQL.append(" INSERT into INATK.CSVTOK_RSKKK (");
		sbSQL.append(" SEQ");														// SEQ
		sbSQL.append(", INPUTNO");													// 入力番号
		sbSQL.append(", HBSTDT");													// 催し区分
		sbSQL.append(", BMNCD");													// 催し開始日
		sbSQL.append(", WRITUKBN");													// 催し連番
		sbSQL.append(", SEICUTKBN");												// 部門
		sbSQL.append(", DUMMYCD");													// ダミーコード
		sbSQL.append(", MEISHOKN");													// 企画名称
		sbSQL.append(", UPDKBN");		                                            // 更新区分：
		sbSQL.append(", OPERATOR ");								                // オペレーター：
		sbSQL.append(", ADDDT ");								                    // 登録日：
		sbSQL.append(", UPDDT ");								                    // 更新日：
        sbSQL.append(") ");
        sbSQL.append("VALUES ");
        sbSQL.append(StringUtils.join(valueData, ",") + "AS NEW ");
        sbSQL.append("ON DUPLICATE KEY UPDATE ");
		sbSQL.append(" SEQ = NEW.SEQ");												         
		sbSQL.append(", INPUTNO = NEW.INPUTNO");													
		sbSQL.append(", HBSTDT = NEW.HBSTDT");
		sbSQL.append(", BMNCD = NEW.BMNCD");
		sbSQL.append(", WRITUKBN = NEW.WRITUKBN");
		sbSQL.append(", SEICUTKBN = NEW.SEICUTKBN");
		sbSQL.append(", DUMMYCD = NEW.DUMMYCD");
		sbSQL.append(", MEISHOKN  = NEW.MEISHOKN");
	    sbSQL.append(", UPDKBN = NEW.UPDKBN");                                                   
	    sbSQL.append(", OPERATOR = NEW.OPERATOR");                                                
	    sbSQL.append(", ADDDT = NEW.ADDDT  ");                                                   
	    sbSQL.append(", UPDDT = NEW.UPDDT ");


		if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		this.sqlList.add(sbSQL.toString());
		this.prmList.add(prmData);
		this.lblList.add("CSV取込トラン_冷凍食品企画");
		return result;
	}

	/**
	 * CSV取込トラン_冷凍食品企画INSERT処理
	 *
	 * @param userId
	 * @param data
	 * @param tbl
	 *
	 * @throws Exception
	 */
	public JSONObject createSqlCSVTOKRSSHN(int userId, JSONArray dataArray, String seq) {
		JSONObject result = new JSONObject();
		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();

		LinkedList<String> valList = new LinkedList<String>();
		valList.add("");	//
		int i = 1;
	    Object[] valueData = new Object[]{};
		String values = "";
		for(CSVTOK_RSSHNLayout itm :CSVTOK_RSSHNLayout.values()){
			String val = ((String) this.csvtokrsshn_add_data[itm.getNo()-1]);

			if(StringUtils.equals(CSVTOK_SOLayout.ERRVL.getCol(), itm.getCol())){
				// エラーメッセージを字化する。
				val = "'" + val.trim() + "'";
			}

			if (StringUtils.isEmpty(val)) {
				values += "null ,";
			} else {
				values += "? ,";
				prmData.add(val);
			}

		}
        values += " " + DefineReport.ValUpdkbn.NML.getVal() + " ";
        values += ", '" + userId + "'";
        values += ", CURRENT_TIMESTAMP";
        values += ", CURRENT_TIMESTAMP";

        valueData = ArrayUtils.add(valueData, "(" + values + ")");
        values = "";

		StringBuffer sbSQL;
		sbSQL = new StringBuffer();
		// 冷凍食品_商品の登録・更新
		sbSQL.append(" INSERT into INATK.CSVTOK_RSSHN (");
		sbSQL.append(" SEQ");													    // 催し区分
		sbSQL.append(", INPUTNO");													// 催し開始日
		sbSQL.append(", INPUTEDANO");											    // 催し開始日
		sbSQL.append(", ERRCD");													// 催し開始日
		sbSQL.append(", ERRFLD");													// 催し開始日
		sbSQL.append(", ERRVL");													// 催し開始日
		sbSQL.append(", ERRTBLNM");													// 催し開始日
		sbSQL.append(", CSV_UPDKBN");											    // 催し開始日
		sbSQL.append(", HBSTDT");													// 催し開始日
		sbSQL.append(", BMNCD");													// 催し開始日
		sbSQL.append(", WRITUKBN");													// 催し連番
		sbSQL.append(", SEICUTKBN");											    // 部門
		sbSQL.append(", DUMMYCD");													// 部門
		sbSQL.append(", KANRINO");													// 部門
		sbSQL.append(", SHNCD");													// 部門
		sbSQL.append(", MAKERKN");													// 部門
		sbSQL.append(", SHNKN");													// 部門
		sbSQL.append(", KIKKN");													// 部門
		sbSQL.append(", IRISU");													// 部門
		sbSQL.append(", BAIKAAM");													// 部門
		sbSQL.append(", GENKAAM");													// 部門
		sbSQL.append(", UPDKBN");		                                            // 更新区分：
		sbSQL.append(", OPERATOR ");								                // オペレーター：
		sbSQL.append(", ADDDT ");								                    // 登録日：
		sbSQL.append(", UPDDT ");								                    // 更新日：
        sbSQL.append(") ");
        sbSQL.append("VALUES ");
        sbSQL.append(StringUtils.join(valueData, ",") + "AS NEW ");
        sbSQL.append("ON DUPLICATE KEY UPDATE ");
		sbSQL.append(" SEQ = NEW.SEQ");													// 催し区分
		sbSQL.append(", INPUTNO = NEW.INPUTNO");													// 催し開始日
		sbSQL.append(", INPUTEDANO = NEW.INPUTEDANO");													// 催し開始日
		sbSQL.append(", ERRCD = NEW.ERRCD");													// 催し開始日
		sbSQL.append(", ERRFLD = NEW.ERRFLD");													// 催し開始日
		sbSQL.append(", ERRVL = NEW.ERRVL");													// 催し開始日
		sbSQL.append(", ERRTBLNM = NEW.ERRTBLNM");													// 催し開始日
		sbSQL.append(", CSV_UPDKBN = NEW.CSV_UPDKBN");													// 催し開始日
		sbSQL.append(", HBSTDT = NEW.HBSTDT");													// 催し開始日
		sbSQL.append(", BMNCD = NEW.BMNCD");													// 催し開始日
		sbSQL.append(", WRITUKBN = NEW.WRITUKBN");													// 催し連番
		sbSQL.append(", SEICUTKBN = NEW.SEICUTKBN");													// 部門
		sbSQL.append(", DUMMYCD = NEW.DUMMYCD");													// 部門
		sbSQL.append(", KANRINO = NEW.KANRINO");													// 部門
		sbSQL.append(", SHNCD = NEW.SHNCD");													// 部門
		sbSQL.append(", MAKERKN = NEW.MAKERKN");													// 部門
		sbSQL.append(", SHNKN = NEW.SHNKN");													// 部門
		sbSQL.append(", KIKKN = NEW.KIKKN");													// 部門
		sbSQL.append(", IRISU = NEW.IRISU");													// 部門
		sbSQL.append(", BAIKAAM = NEW.BAIKAAM ");													// 部門
		sbSQL.append(", GENKAAM = NEW.GENKAAM");	
	    sbSQL.append(", UPDKBN = NEW.UPDKBN");                                                   // 更新区分：
	    sbSQL.append(", OPERATOR = NEW.OPERATOR ");                                                // オペレーター：                                                  // 登録日：
	    sbSQL.append(", UPDDT = NEW.UPDDT "); 


		if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		this.sqlList.add(sbSQL.toString());
		this.prmList.add(prmData);
		this.lblList.add("CSV取込トラン_冷凍食品_商品");
		return result;
	}
	/** CSV取込トラン特殊情報保持用 */
	String[] csvtokrsshn_add_data = new String[CSVTOK_RSSHNLayout.values().length];
	String[] csvtokrskkk_add_data = new String[CSVTOK_RSKKKLayout.values().length];

	String Param = "";

	/**  File出力項目の参照テーブル */
	public enum RefTable {
		/** 冷凍食品_企画 */
		TOKRS_KKK(1,"冷凍食品_企画"),
		/** 冷凍食品_商品 */
		TOKRS_SHN(2,"冷凍食品_商品"),
		/** CSV取込トラン_冷凍食品_商品 */
		CSVTOK_RSSHN(3,"CSV取込トラン_冷凍食品_商品");
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
	/**  冷凍食品_企画 */
	public enum TOKRS_KKKLayout implements MSTLayout{
		/** 催し販売開始日 */
		HBSTDT(1,"HBSTDT","INTEGER","販売開始日"),
		/** 部門 */
		BMNCD(2,"BMNCD","INTEGER","部門"),
		/** 割引率区分 */
		WRITUKBN(3,"WRITUKBN","SMALLINT","割引率区分"),
		/** 正規.カット */
		SEICUTKBN(4,"SEICUTKBN","SMALLINT","正規.カット"),
		/** ダミーコード（商品コード） */
		DUMMYCD(5,"DUMMYCD","CHARACTER(14)","ダミーコード（商品コード）"),
		/** 名称 */
		MEISHOKN(6,"MEISHOKN","VARCHAR(40)","名称");

		private final Integer no;
		private final String col;
		private final String typ;
		private final String text;
		/** 初期化 */
		private TOKRS_KKKLayout(Integer no, String col, String typ, String text) {
			this.no = no;
			this.col = col;
			this.typ = typ;
			this.text = text;
		}
		/** @return col 列番号 */
		public Integer getNo() { return no; }
		/** @return tbl 列名 */
		public String getCol() { return col; }
		/** @return tbl 列型 */
		public String getTyp() { return typ; }
		/** @return tbl 列名(論理名称) */
		public String getText() { return text; }
		/** @return col Id */
		public String getId() { return "F" + Integer.toString(no); }

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
	/**  冷凍食品_商品 */
	public enum TOKRS_SHNLayout implements MSTLayout{
		/** 販売開始日 */
		HBSTDT(1,"HBSTDT","SMALLINT","販売開始日"),
		/** 部門 */
		BMNCD(2,"BMNCD","INTEGER","部門"),
		/** 割引率区分 */
		WRITUKBN(3,"WRITUKBN","SMALLINT","区分"),
		/** 正規・カット */
		SEICUTKBN(4,"SEICUTKBN","SMALLINT","正規・カット"),
		/** ダミーコード（商品コード） */
		DUMMYCD(5,"DUMMYCD","SMALLINT","ダミーコード（商品コード）"),
		/** 管理番号 */
		KANRINO(6,"KANRINO","CHARACTER(14)","管理番号"),
		/** 商品コード */
		SHNCD(7,"SHNCD","VARCHAR(28)","商品コード"),
		/** メーカー名称 */
		MAKERKN(8,"MAKERKN","VARCHAR(20)","メーカー名称"),
		/** 商品名称 */
		SHNKN(9,"SHNKN","VARCHAR(20)","商品名称"),
		/** 規格名称 */
		KIKKN(10,"KIKKN","SMALLINT","規格名称"),
		/** 入数 */
		IRISU(11,"IRISU","SMALLINT","入数"),
		/** 売価 */
		BAIKAAM(12,"BAIKAAM","DECIMAL","売価"),
		/** 原価 */
		GENKAAM(13,"GENKAAM","INTEGER","原価");

		private final Integer no;
		private final String col;
		private final String typ;
		private final String text;

		/** 初期化 */
		private TOKRS_SHNLayout(Integer no, String col, String typ, String text) {
			this.no = no;
			this.col = col;
			this.typ = typ;
			this.text = text;

		}
		/** @return col 列番号 */
		public Integer getNo() { return no; }
		/** @return tbl 列名 */
		public String getCol() { return col; }
		/** @return tbl 列型 */
		public String getTyp() { return typ; }
		/** @return tbl 列名(論理名称) */
		public String getText() { return text; }
		/** @return col Id */
		public String getId() { return "F" + Integer.toString(no); }

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

	/**  CSV取込トラン_冷凍食品_企画(正マスタとの差分) */
	public enum CSVTOK_RSKKKLayout implements MSTLayout{
		/** SEQ */
		SEQ(1,"SEQ","INTEGER"),
		/** 入力番号 */
		INPUTNO(2,"INPUTNO","INTEGER"),
		/** 販売開始日 */
		HBSTDT(3,"HBSTDT","INTEGER"),
		/** 部門 */
		BMNCD(4,"BMNCD","SMALLINT"),
		/** 割引率区分 */
		WRITUKBN(5,"WRITUKBN","SMALLINT"),
		/** 正規・カット */
		SEICUTKBN(6,"SEICUTKBN","SMALLINT"),
		/** ダミーコード（商品コード） */
		DUMMYCD(7,"DUMMYCD","CHARACTER(14)"),
		/** 名称 */
		MEISHOKN(8,"MEISHOKN","VARCHAR(40)");

		private final Integer no;
		private final String col;
		private final String typ;
		/** 初期化 */
		private CSVTOK_RSKKKLayout(Integer no, String col, String typ) {
			this.no = no;
			this.col = col;
			this.typ = typ;
		}
		/** @return col 列番号 */
		public Integer getNo() { return no; }
		/** @return tbl 列名 */
		public String getCol() { return col; }
		/** @return tbl 列型 */
		public String getTyp() { return typ; }
		/** @return col Id */
		public String getId() { return "F" + Integer.toString(no) ;}
		/** @return col Id */
		public String getId2() { return "F" + Integer.toString(no+MSTSHNLayout.values().length) ;}
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
	}


	/**  CSV取込トラン_冷凍食品_商品(正マスタとの差分) */
	public enum CSVTOK_RSSHNLayout implements MSTLayout{
		/** SEQ */
		SEQ(1,"SEQ","INTEGER"),
		/** 入力番号 */
		INPUTNO(2,"INPUTNO","INTEGER"),
		/** 入力枝番 */
		INPUTEDANO(3,"INPUTEDANO","SMALLINT"),
		/** エラーコード */
		ERRCD(4,"ERRCD","SMALLINT"),
		/** エラー箇所 */
		ERRFLD(5,"ERRFLD","VARCHAR(100)"),
		/** エラー値 */
		ERRVL(6,"ERRVL","VARCHAR(100)"),
		/** エラーテーブル名 */
		ERRTBLNM(7,"ERRTBLNM","VARCHAR(100)"),
		/** CSV登録区分 */
		CSV_UPDKBN(8,"CSV_UPDKBN","CHARACTER(1)"),
		/** 販売開始日 */
		HBSTDT(9,"HBSTDT","INTEGER"),
		/** 部門 */
		BMNCD(10,"BMNCD","SMALLINT"),
		/** 催し連番 */
		WRITUKBN(11,"WRITUKBN","SMALLINT"),
		/** 正規・カット */
		SEICUTKBN(12,"SEICUTKBN","SMALLINT"),
		/** ダミーコード（商品コード） */
		DUMMYCD(13,"DUMMYCD","CHARACTER(14)"),
		/** 管理番号 */
		KANRINO(14,"KANRINO","SMALLINT"),
		/** 商品コード */
		SHNCD(15,"SHNCD","CHARACTER(14)"),
		/** メーカー名称 */
		MAKERKN(16,"MAKERKN","VARCHAR(28)"),
		/** 商品名称 */
		SHNKN(17,"SHNKN","VARCHAR(40)"),
		/** 規格名称 */
		KIKKN(18,"KIKKN","VARCHAR(46)"),
		/** 入数 */
		IRISU(19,"IRISU","SMALLINT"),
		/** 売価 */
		BAIKAAM(20,"BAIKAAM","INTEGER"),
		/** 原価 */
		GENKAAM(21,"GENKAAM","DECIMAL(8, 2)");

		private final Integer no;
		private final String col;
		private final String typ;
		/** 初期化 */
		private CSVTOK_RSSHNLayout(Integer no, String col, String typ) {
			this.no = no;
			this.col = col;
			this.typ = typ;
		}
		/** @return col 列番号 */
		public Integer getNo() { return no; }
		/** @return tbl 列名 */
		public String getCol() { return col; }
		/** @return tbl 列型 */
		public String getTyp() { return typ; }
		/** @return col Id */
		public String getId() { return "F" + Integer.toString(no) ;}
		/** @return col Id */
		public String getId2() { return "F" + Integer.toString(no+MSTSHNLayout.values().length) ;}
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
	}


	/**  Fileレイアウト */
	public enum FileLayout {
		/** 更新区分 */
		UPDKBN(1,"更新区分", RefTable.CSVTOK_RSSHN,"CSV_UPDKBN"),
		/** 企画名称 */
		MEISHOKN(2,"企画名称", RefTable.TOKRS_KKK,"MEISHOKN"),
		/** 販売開始日 */
		HBSTDT(3,"販売開始日", RefTable.TOKRS_KKK,"HBSTDT"),
		/** 部門 */
		BMNCD(4,"部門", RefTable.TOKRS_KKK,"BMNCD"),
		/** 割引率区分 */
		WRITUKBN(5,"割引率区分", RefTable.TOKRS_KKK,"WRITUKBN"),
		/** 正規／カット */
		SEICUTKBN(6,"正規／カット", RefTable.TOKRS_KKK,"SEICUTKBN"),
		/** ダミーコード */
		DUMMYCD(7,"ダミーコード", RefTable.TOKRS_KKK,"DUMMYCD"),
		/** 商品コード */
		SHNCD(8,"商品コード", RefTable.TOKRS_SHN,"SHNCD"),
		/** メーカー名(漢字) */
		MAKERKN(9,"メーカー名(漢字)", RefTable.TOKRS_SHN,"MAKERKN"),
		/** ＰＯＰ名称(漢字) */
		SHNKN(10,"ＰＯＰ名称(漢字)", RefTable.TOKRS_SHN,"SHNKN"),
		/** 規格 */
		KIKKN(11,"規格", RefTable.TOKRS_SHN,"KIKKN"),
		/** 入数（販促） */
		IRISU(12,"入数（販促）", RefTable.TOKRS_SHN,"IRISU"),
		/** 割引総売価 */
		GENKAAM(13,"割引総売価", RefTable.TOKRS_SHN,"BAIKAAM"),
		/** 割引原価 */
		A_BAIKAAM(14,"割引原価", RefTable.TOKRS_SHN,"GENKAAM"),
		/** バイヤーコード */
		BCD(15,"バイヤーコード", RefTable.TOKRS_KKK,"BCD"),
		//POPKN(20,"POPKN", RefTable.TOKSO_SHN,"VARCHAR "),
		/** オペレータ */
		OPERATOR(16,"オペレータ", RefTable.TOKRS_KKK,"OPERATOR"),
		/** 登録日 */
		ADDDT(17,"登録日", RefTable.TOKRS_KKK,"ADDDT"),
		/** 更新日 */
		UPDDT(18,"更新日", RefTable.TOKRS_KKK,"UPDDT");

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
