/**
 *
 */
package dao;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import authentication.bean.User;
import common.CmnDate;
import common.DefineReport;
import common.Defines;
import common.FileList;
import common.ItemList;
import common.MessageUtility;
import common.MessageUtility.Msg;
import common.MessageUtility.MsgKey;
import dao.ReportBM006Dao.CSVTOKLayout;
import dao.ReportJU012Dao.TOKJULayout;
import dao.ReportJU012Dao.TOKJU_CKLayout;
import dto.JQEasyModel;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 */
public class ReportJU037Dao extends ItemDao {

	boolean isTest = true;


	/** 最大処理件数 */
	public static int MAX_ROW = 10000;

	/**
	 * インスタンスを生成します。
	 * @param source
	 */
	public ReportJU037Dao(String JNDIname) {
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
		String report	= map.get("report");
		String shoridt	= map.get("txt_shoridt");
		String callpage	= map.get("report");

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

			// データ加工 + 最新情報取得
			// CSVを分解、画面上と同じように登録用のデータの形にする
			JSONArray amer = new JSONArray(),aall = new JSONArray();
			JSONObject omer = new JSONObject(), oall = new JSONObject();

			// ファイル内レコード件数
			int cnt = 0;

			// 重複確認用
			Map<String,String> tenHtsuArr		= new HashMap<String,String>();
			Map<String,String> shnNndtTenArr	= new HashMap<String,String>();
			Map<Integer,String> kanrinoMap		= new HashMap<Integer,String>();
			HashMap<String,String> kanrinoArr	= new HashMap<String,String>();
			String kanrino	= "";
			String moyskbn	= "";
			String moysstdt	= "";
			String moysrban	= "";
			String moyscd	= "";
			int keyCnt = 0;

			int errCount = 0;
			// ヘッダ登録用SQL作成
			ReportJU012Dao dao = new ReportJU012Dao(super.JNDIname);

			if(StringUtils.isEmpty(strMsg)){
				// 全てのデータに対する単純なチェック

				// データ加工
				for (int i = idxHeader; i < dL.size(); i++) {
					Object[] data = dL.get(i);

					for(FileLayout itm :FileLayout.values()){
						String val = StringUtils.trim((String) data[itm.getNo()-1]);

						if(itm.getTbl() == RefTable.TOKJU){

							if(oall.containsKey(itm.getCol())){
								aall.add(oall);
								oall = new JSONObject();
							}
							if (itm.getCol().equals(FileLayout.MOYSCD.getCol())) {
								if (val.length() >= 8) {

									moyskbn		= val.substring(0,1);
									moysstdt	= val.substring(1,7);
									moysrban	= val.substring(7);
									moyscd		= moyskbn + moysstdt + moysrban;
								} else {
									if (val.length() >= 1) {
										moyskbn = val.substring(0,1);
									}
									if (val.length() >= 2) {
										moysstdt = val.substring(1);
									}
								}

								// 催し区分が不正だった場合
								if (callpage.equals(DefineReport.ID_PAGE_JU037) && moyskbn.equals("8") ||
										callpage.equals(DefineReport.ID_PAGE_JU038) && moyskbn.equals("9")) {
									msgList.add(mu.getDbMessageObj("E20609", new String[]{String.valueOf(i+1) + "行目："}));
									break;
								} else {
									oall.put(TOKJU_CKLayout.MOYSKBN.getCol(), moyskbn);
									oall.put(TOKJU_CKLayout.MOYSSTDT.getCol(), moysstdt);
									oall.put(TOKJU_CKLayout.MOYSRBAN.getCol(), moysrban);
								}
							} else if (itm.getCol().equals(FileLayout.TENHTSU_ARR.getCol())) {
								val = !StringUtils.isEmpty(val) && NumberUtils.isNumber(val) ? String.valueOf(Integer.valueOf(val)) : val;
								oall.put(itm.getCol(), val);
							} else if (itm.getCol().equals(FileLayout.WAPPNKBN.getCol())) {
								if (StringUtils.isEmpty(val)) {
									val = "0";
								}
								oall.put(itm.getCol(), val);
							} else {
								oall.put(itm.getCol(), val);
							}

							if (itm.getCol().equals(FileLayout.SURYO.getCol())) {
								break;
							}
						}
					}

					if (msgList.size() != 0) {
						this.setMessage(msgList.toString());
						strMsg = this.getMessage();
						break;
					}

					aall.add(oall);
					oall = new JSONObject();
				}
			}

			if(StringUtils.isEmpty(strMsg) && StringUtils.isEmpty(this.getMessage())){
				// 詳細情報チェック
				msgList = dao.checkData(
						mu,
						this.selectTOKJU_CK(aall),
						shoridt,
						report
				);

				if (msgList.size() != 0) {
					this.setMessage(msgList.toString());
					strMsg = this.getMessage();
				}
			}

			if(StringUtils.isEmpty(strMsg) && StringUtils.isEmpty(this.getMessage())){

				// 店発注数配列作成の為、データの再作成
				HashMap<String,String> shnMap	= new HashMap<String,String>();
				for (int i = idxHeader; i < dL.size(); i++) {
					Object[] data = dL.get(i);

					// keyとなる項目
					String f1	= StringUtils.trim(String.valueOf(data[FileLayout.MOYSCD.getNo()-1]));
					String f2	= StringUtils.trim(String.valueOf(data[FileLayout.SHNCD.getNo()-1]));
					String f3	= StringUtils.trim(String.valueOf(data[FileLayout.SHNKBN.getNo()-1]));
					String f4	= StringUtils.trim(String.valueOf(data[FileLayout.TSEIKBN.getNo()-1]));
					String f5	= "1";
					String f6	= StringUtils.trim(String.valueOf(data[FileLayout.BDENKBN.getNo()-1]));
					if (StringUtils.isEmpty(f6)) {
						f6 = "0";
					}
					String f7	= StringUtils.trim(String.valueOf(data[FileLayout.WAPPNKBN.getNo()-1]));
					if (StringUtils.isEmpty(f7)) {
						f7 = "0";
					}
					String f8	= StringUtils.trim(String.valueOf(data[FileLayout.HTDT.getNo()-1]));
					String f9	= StringUtils.trim(String.valueOf(data[FileLayout.NNDT.getNo()-1]));
					String f10	= StringUtils.trim(String.valueOf(data[FileLayout.IRISU.getNo()-1]));
					String f11	= StringUtils.trim(String.valueOf(data[FileLayout.GENKAAM.getNo()-1]));
					String f12	= StringUtils.trim(String.valueOf(data[FileLayout.BAIKAAM.getNo()-1]));

					if (f3.equals("0")) {

						String key = StringUtils.isEmpty(f1) ? "" : f1;
						key += StringUtils.isEmpty(f2) ? "-" : "-" + f2;
						if (!shnMap.containsKey(key)) {
							// 変数を初期化
							sbSQL	= new StringBuffer();
							iL		= new ItemList();
							dbDatas = new JSONArray();
							sqlWhere	= "";
							paramData	= new ArrayList<String>();

							sbSQL.append("SELECT ");
							sbSQL.append("T1.RG_IRISU AS F10");			// 入数
							sbSQL.append(",T1.RG_GENKAAM AS F11");		// 原価
							sbSQL.append(",CASE WHEN T2.ZEIRT IS NULL THEN T1.RG_BAIKAAM ");												// 売価
							sbSQL.append("ELSE TRUNC(DOUBLE (T1.RG_BAIKAAM) + (DOUBLE (T1.RG_BAIKAAM) * (T2.ZEIRT / 100)), 0) END F12 ");	// 売価
							sbSQL.append("FROM ");
							sbSQL.append("INAMS.MSTSHN T1 ");
							sbSQL.append(",(SELECT T1.SHNCD,T2.ZEIRT FROM( ");
							sbSQL.append("SELECT ");
							sbSQL.append("T1.SHNCD ");
							sbSQL.append(",CASE ");
							sbSQL.append("WHEN T1.ZEIKBN <> '3' THEN ");
							sbSQL.append("CASE ");
							sbSQL.append("WHEN T1.ZEIKBN <> '0' THEN NULL ");
							sbSQL.append("WHEN T1.ZEIKBN = '0' AND SUBSTR(T1.ZEIRTHENKODT,3,6) <= ? THEN T1.ZEIRTKBN ");
							sbSQL.append("WHEN T1.ZEIKBN = '0' AND SUBSTR(T1.ZEIRTHENKODT,3,6) > ? THEN T1.ZEIRTKBN_OLD END ");
							sbSQL.append("ELSE ");
							sbSQL.append("CASE ");
							sbSQL.append("WHEN T2.ZEIKBN <> '0' THEN NULL ");
							sbSQL.append("WHEN T2.ZEIKBN = '0' AND SUBSTR(T2.ZEIRTHENKODT,3,6) <= ? THEN T2.ZEIRTKBN ");
							sbSQL.append("WHEN T2.ZEIKBN = '0' AND SUBSTR(T2.ZEIRTHENKODT,3,6) > ? THEN T2.ZEIRTKBN_OLD END END ZEIRTKBN ");
							paramData.add(f1.substring(1,7));
							paramData.add(f1.substring(1,7));
							paramData.add(f1.substring(1,7));
							paramData.add(f1.substring(1,7));
							sbSQL.append("FROM ");
							sbSQL.append("INAMS.MSTSHN T1 LEFT JOIN INAMS.MSTBMN T2 ON SUBSTR(T1.SHNCD,1,2) = T2.BMNCD ");
							sbSQL.append(") T1 LEFT JOIN INAMS.MSTZEIRT T2 ON T1.ZEIRTKBN = T2.ZEIRTKBN) T2 ");

							// 商品存在チェック
							if (StringUtils.isEmpty(f2)) {
								sqlWhere += "T1.SHNCD=null AND ";
							} else {
								sqlWhere += "T1.SHNCD=? AND ";
								paramData.add(f2);
							}
							sbSQL.append("WHERE ");
							sbSQL.append(sqlWhere); // 入力された商品コードで検索
							sbSQL.append("T1.SHNCD=T2.SHNCD AND T1.UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal() + " ");

							dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

							if (dbDatas.size() != 0){
								f10 = dbDatas.getJSONObject(0).getString("F10");
								f11 = dbDatas.getJSONObject(0).getString("F11");
								f12 = dbDatas.getJSONObject(0).getString("F12");
							}
							shnMap.put(key, f10 + "-" + f11 + "-" + f12);
						} else {
							String val = shnMap.get(key);
							f10 = val.split("-")[0];
							f11 = val.split("-")[1];
							f12 = val.split("-")[2];
						}
					}

					// valとなる項目
					String tenCd	= StringUtils.trim(String.valueOf(data[FileLayout.TENHTSU_ARR.getNo()-1]));
					tenCd = !StringUtils.isEmpty(tenCd) && NumberUtils.isNumber(tenCd) ? String.valueOf(Integer.valueOf(tenCd)) : tenCd;
					String suryo	= StringUtils.trim(String.valueOf(data[FileLayout.SURYO.getNo()-1]));

					// 項目ごとに-で連結し格納
					String key = "UPDKBN-"+f1+"-BMNCD-"+f2+"-"+f3+"-"+f4+"-"+f5+"-"+f6+"-"+f7+"-"+f8+"-"+f9+"-"+f10+"-"+f11+"-"+f12+"-"+"TENHTSU_ARR";
					String val = tenCd+'-'+suryo;

					if (tenHtsuArr.containsKey(key)) {
						// 既に存在するキーだった場合、,区切りで後ろに店・発注数を連結
						tenHtsuArr.replace(key, tenHtsuArr.get(key)+','+val);
					} else {
						tenHtsuArr.put(key,val);
						kanrinoMap.put(keyCnt, key);
						keyCnt++;
					}

					// 商品、発注日、納入日、店毎で異なる催しコードはエラー
					key = f2 + f8 + f9 + tenCd;
					val = f1;

					if (shnNndtTenArr.containsKey(key)) {
						// 既に存在するキーだった場合、valを比較
						if (!val.equals(shnNndtTenArr.get(key))) {
							// 二重登録エラー
							msgList.add(mu.getDbMessageObj("E20136", new String[]{String.valueOf(i+1) + "行目："}));
						} else {
							// 店舗重複エラー
							msgList.add(mu.getDbMessageObj("E11040", new String[]{String.valueOf(i+1) + "行目：催しコード、商品コード、発注日、納入日、対象店"}));
						}
					} else {
						shnNndtTenArr.put(key,val);
					}

					if (msgList.size() != 0) {
						this.setMessage(msgList.toString());
						strMsg = this.getMessage();
						break;
					}
				}

				if (StringUtils.isEmpty(strMsg) && StringUtils.isEmpty(this.getMessage())) {

					int gyo = 1;

					for (int i = 0; i < keyCnt; i++) {

					    String[] key		= kanrinoMap.get(i).split("-");
					    String[] tenSuryo	= tenHtsuArr.get(kanrinoMap.get(i)).split(",");

					    // 店舗のソート
					    HashMap<String,String> tenSuMap	= new HashMap<String,String>();
					    Set<Integer> tencds = new TreeSet<Integer>();
					    for (int j = 0; j < tenSuryo.length; j++) {

					    	int tenCd = Integer.valueOf(tenSuryo[j].split("-")[0]);

					    	if (!tencds.contains(tenCd)) {
					    		tencds.add(tenCd);
					    		tenSuMap.put(String.valueOf(tenCd),tenSuryo[j].split("-")[1]);
					    	}
					    }

					    String shncd	= "";
					    String htdt		= "";
					    String nndt		= "";

					    for(FileLayout itm :FileLayout.values()){
						    if(itm.getTbl() == RefTable.TOKJU){
								String val = StringUtils.trim((String) key[itm.getNo()-1]);

								if (itm.getCol().equals(FileLayout.MOYSCD.getCol())) {
									moyscd = val;
								} else if (itm.getCol().equals(FileLayout.SHNCD.getCol())) {
									shncd = val;
								} else if (itm.getCol().equals(FileLayout.HTDT.getCol())) {
									htdt = val;
								} else if (itm.getCol().equals(FileLayout.NNDT.getCol())) {
									nndt = val;
									break;
								}
						    }
					    }

					    // 商品、納入日、店重複チェック
					    strMsg = dao.checkCsvArr(moyscd,shncd,htdt,nndt,tencds,callpage);
					    if (!StringUtils.isEmpty(strMsg)) {
					    	msgList.add(mu.getDbMessageObj("EX1047", new String[]{strMsg+"商品、発注日、納入日、店番で重複しない値"}));
					    	if (msgList.size() != 0) {
								this.setMessage(msgList.toString());
								strMsg = this.getMessage();
								break;
							}
					    }

					    String tenHtSu_Arr = "";
					    int lastTenCd = 1;
					    Iterator<Integer> iTen = tencds.iterator();
					    HashMap<String,String> tenSuryoMap = new HashMap<String,String>();

					    for (int j = 0; j < tencds.size(); j++) {

					    	int tencd = iTen.next();
					    	int suryo = Integer.valueOf(tenSuMap.get(String.valueOf(tencd)));

							if ((tencd-lastTenCd) != 0) {
								tenHtSu_Arr += String.format("%"+(tencd-lastTenCd)*5+"s","");
							}

							if (suryo != 0) {
								tenHtSu_Arr += String.format("%05d", suryo);
							} else {
								tenHtSu_Arr += String.format("%05d",0);
								suryo = 0;
							}

							if (tenSuryoMap.containsKey(String.valueOf(tencd))) {
								tenSuryoMap.replace(String.valueOf(tencd), String.valueOf(suryo));
							} else {
								tenSuryoMap.put(String.valueOf(tencd), String.valueOf(suryo));
							}
							lastTenCd = (tencd+1);
					    }

					    tenHtSu_Arr = new ReportJU012Dao(JNDIname).spaceArr(tenHtSu_Arr,5);

					    for(FileLayout itm :FileLayout.values()){

							if(itm.getTbl() == RefTable.TOKJU){
								String val = StringUtils.trim((String) key[itm.getNo()-1]);

								if(omer.containsKey(itm.getCol())){
									amer.add(omer);
									omer = new JSONObject();
								}
								if (itm.getCol().equals(FileLayout.MOYSCD.getCol())) {
									if (val.length() >= 8) {

										moyskbn		= val.substring(0,1);
										moysstdt	= val.substring(1,7);
										moysrban	= val.substring(7);
										moyscd		= moyskbn + moysstdt + moysrban;

										omer.put(TOKJULayout.MOYSKBN.getCol(), moyskbn);
										omer.put(TOKJULayout.MOYSSTDT.getCol(), moysstdt);
										omer.put(TOKJULayout.MOYSRBAN.getCol(), moysrban);

										// 管理番号の取得
										if (kanrinoArr.containsKey(moyscd)) {
											kanrino = String.valueOf(Integer.valueOf(kanrinoArr.get(moyscd)) + 1);
											kanrinoArr.replace(moyscd, kanrino);
										}else{
											sbSQL	= new StringBuffer();
											iL		= new ItemList();
											dbDatas = new JSONArray();
											sqlWhere	= "";
											paramData	= new ArrayList<String>();
											getData = new JSONObject();

											// 催し区分
											if (StringUtils.isEmpty(moyskbn)) {
												sqlWhere += "MOYSKBN=null AND ";
											} else {
												sqlWhere += "MOYSKBN=? AND ";
												paramData.add(moyskbn);
											}

											// 催し開始日
											if (StringUtils.isEmpty(moysstdt)) {
												sqlWhere += "MOYSSTDT=null AND ";
											} else {
												sqlWhere += "MOYSSTDT=? AND ";
												paramData.add(moysstdt);
											}

											// 催し連番
											if (StringUtils.isEmpty(moysrban)) {
												sqlWhere += "MOYSRBAN=null ";
											} else {
												sqlWhere += "MOYSRBAN=? ";
												paramData.add(moysrban);
											}

											// 一覧表情報
											sbSQL.append("SELECT ");
											sbSQL.append("MAX(SUMI_KANRINO) AS SUMI_KANRINO ");
											sbSQL.append("FROM INATK.SYSMOYCD ");
											sbSQL.append("WHERE ");
											sbSQL.append(sqlWhere);

											dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

											if (dbDatas.size() > 0) {
												getData	= dbDatas.getJSONObject(0);
												if (!StringUtils.isEmpty(getData.optString("SUMI_KANRINO"))) {
													kanrino = String.valueOf(getData.optInt("SUMI_KANRINO") + 1);
												} else {
													kanrino = "1";
												}
											} else {
												kanrino = "1";
											}
											kanrinoArr.put(moyscd, kanrino);
										}
									}
								} else if (itm.getCol().equals(TOKJULayout.TENHTSU_ARR.getCol())) {
									omer.put(itm.getCol(), tenHtSu_Arr);
									omer.put(TOKJULayout.GYONO.getCol(), gyo);
									omer.put(TOKJULayout.KANRINO.getCol(), kanrino);
									break;
								} else {
									omer.put(itm.getCol(), val);
								}
							}
					    }
					    amer.add(omer);
					    omer = new JSONObject();
						gyo++;

						// エラーが一件も存在しない場合更新処理を実行
						if (StringUtils.isEmpty(strMsg) && errCount == 0) {

							JSONArray dataArray = this.selectTOKJU(amer);

							// 登録件数取得
							cnt += this.countShncd(dataArray);

							// 最新の情報取得
							// データ取得先判断
							JSONObject rt = this.createCommandUpdateData(
									dao,
									userInfo,
									dataArray,
									tencds,
									tenSuryoMap,
									report
							);
							amer = new JSONArray();
						}
					}
					this.sqlList.addAll(dao.sqlList);
					this.prmList.addAll(dao.prmList);
					this.lblList.addAll(dao.lblList);
				}
			}

			if (errCount == 0 && StringUtils.isEmpty(strMsg)) {
				// ログインユーザー情報取得
				String	userId = userInfo.getId(); // ログインユーザー
				int len = kanrinoArr.size();
				int i = 0;

				for(HashMap.Entry<String, String> no : kanrinoArr.entrySet()) {

					moyscd = no.getKey();
					if (moyscd.length() >= 8) {
						moyskbn		= moyscd.substring(0,1);
						moysstdt	= moyscd.substring(1,7);
						moysrban	= moyscd.substring(7);
					}

				    kanrino = no.getValue();

				    // 催しコード内部管理更新
					values = "1,";
					prmData = new ArrayList<String>();
					valueData = new Object[]{};

					// 催し区分
					if (StringUtils.isEmpty(moyskbn)) {
						values += "null,";
					} else {
						values += "?,";
						prmData.add(moyskbn);
					}

					// 催し開始日
					if (StringUtils.isEmpty(moysstdt)) {
						values += "null,";
					} else {
						values += "?,";
						prmData.add(moysstdt);
					}

					// 催し連番
					if (StringUtils.isEmpty(moysrban)) {
						values += "null,";
					} else {
						values += "?,";
						prmData.add(moysrban);
					}

					// 管理番号
					if (StringUtils.isEmpty(kanrino)) {
						values += "null";
					} else {
						values += "?";
						prmData.add(kanrino);
					}

					valueData = ArrayUtils.add(valueData, "("+values+")");
					values = "";

					if(valueData.length >= 100 || (i+1 == len && valueData.length > 0)){
						sbSQL = new StringBuffer();
						sbSQL.append("MERGE INTO INATK.SYSMOYCD AS T USING (SELECT ");
						sbSQL.append(" MOYSKBN");												// 催し区分
						sbSQL.append(",MOYSSTDT");												// 催し開始日
						sbSQL.append(",MOYSRBAN");												// 催し連番
						sbSQL.append(",SUMI_KANRINO");											// 管理番号
						sbSQL.append(", '"+userId+"' AS OPERATOR ");							// オペレーター：
						sbSQL.append(", current timestamp AS ADDDT ");							// 登録日：
						sbSQL.append(", current timestamp AS UPDDT ");							// 更新日：
						sbSQL.append(" FROM (values "+StringUtils.join(valueData, ",")+") as T1(NUM");
						sbSQL.append(",MOYSKBN");												// 催し区分
						sbSQL.append(",MOYSSTDT");												// 催し開始日
						sbSQL.append(",MOYSRBAN");												// 催し連番
						sbSQL.append(",SUMI_KANRINO");											// 管理番号
						sbSQL.append(")) as RE on (");
						sbSQL.append("T.MOYSKBN=RE.MOYSKBN AND ");
						sbSQL.append("T.MOYSSTDT=RE.MOYSSTDT AND ");
						sbSQL.append("T.MOYSRBAN=RE.MOYSRBAN ");
						sbSQL.append(") WHEN MATCHED THEN UPDATE SET ");
						sbSQL.append("SUMI_KANRINO=RE.SUMI_KANRINO");
						sbSQL.append(",OPERATOR=RE.OPERATOR ");
						sbSQL.append(",UPDDT=RE.UPDDT");
						sbSQL.append(" WHEN NOT MATCHED THEN INSERT (");
						sbSQL.append(" MOYSKBN");												// 催し区分
						sbSQL.append(",MOYSSTDT");												// 催し開始日
						sbSQL.append(",MOYSRBAN");												// 催し連番
						sbSQL.append(",SUMI_KANRINO");											// 管理番号
						sbSQL.append(",OPERATOR");
						sbSQL.append(",ADDDT");
						sbSQL.append(",UPDDT");
						sbSQL.append(") VALUES (");
						sbSQL.append(" RE.MOYSKBN");											// 催し区分
						sbSQL.append(",RE.MOYSSTDT");											// 催し開始日
						sbSQL.append(",RE.MOYSRBAN");											// 催し連番
						sbSQL.append(",RE.SUMI_KANRINO");										// 管理番号
						sbSQL.append(",RE.OPERATOR");
						sbSQL.append(",RE.ADDDT");
						sbSQL.append(",RE.UPDDT");
						sbSQL.append(")");

						if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

						sqlList.add(sbSQL.toString());
						prmList.add(prmData);
						lblList.add("催しコード内部管理更新");

						// クリア
						prmData = new ArrayList<String>();
						valueData = new Object[]{};
						values = "";
					}
					i++;
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
			}

			// 更新成功
			String status = "処理";

			if (errCount == 0 && StringUtils.isEmpty(strMsg)) {
				status += "が終了しました";

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
			ReportJU012Dao dao, User userInfo,
			JSONArray dataArray, Set<Integer> tencds, HashMap<String,String> tenSuryoMap, String report
		) throws Exception {

		JSONObject option = new JSONObject();

		// パラメータ確認
		JSONObject result = dao.createSqlJuCsv(dataArray, tencds, tenSuryoMap, report, userInfo);

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
	 * CSV取込トラン_BM催し送信INSERT処理
	 *
	 * @param userId
	 * @param data
	 * @param tbl
	 *
	 * @throws Exception
	 */
	public JSONObject createSqlCSVTOKBM(int userId, String sysdate, ReportBM006Dao dao) {
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
		prmData.add(String.valueOf(userId));
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
	 * 登録件数取得
	 *
	 * @param dataArray	更新データ
	 *
	 * @throws Exception
	 */
	private int countShncd (JSONArray dataArray) {
		int count = 0;
		HashSet<String> checkPramsSHNCD	 = new HashSet<String>();

		if(dataArray.size() == 0){
			return count;
		}

		for (int j = 0; j < dataArray.size(); j++) {
			JSONObject data  = dataArray.getJSONObject(j);
			checkPramsSHNCD.add(data.optString("F"+TOKJULayout.SHNCD.getNo()));
		}
		return checkPramsSHNCD.size();

	}

	/**
	 * 事前打出し商品(アン付含む)
	 *
	 * @param isSei	正/予約
	 * @param isNew	新規/更新
	 * @param data	CSV抜粋データ
	 *
	 * @throws Exception
	 */
	private JSONArray selectTOKJU_CK(JSONArray dataArray) {
		ArrayList<String> prmData = new ArrayList<>();
		String sqlCommand = this.checkSelectCommandMST(dataArray, TOKJU_CKLayout.values(), prmData);
		if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sqlCommand);

		ItemList iL = new ItemList();
		@SuppressWarnings("static-access")
		JSONArray rtnArray = iL.selectJSONArray(sqlCommand, prmData, Defines.STR_JNDI_DS);
		return rtnArray;
	}

	/**
	 * 事前打出し商品(アン付含む)
	 *
	 * @param isSei	正/予約
	 * @param isNew	新規/更新
	 * @param data	CSV抜粋データ
	 *
	 * @throws Exception
	 */
	private JSONArray selectTOKJU(JSONArray dataArray) {
		ArrayList<String> prmData = new ArrayList<>();
		String sqlCommand = this.setSelectCommandMST(dataArray, TOKJULayout.values(), prmData);
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
		String sqlColCommand = "VALUES NEXTVAL FOR INAMS.SEQ003";
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

		/** 事前打出し商品(アン付含む) */
		TOKJU(1,"事前打出し商品"),
		/** その他 */
		OTHER(2,"その他");
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
		MOYSCD(2,"催しコード",RefTable.TOKJU,"MOYSCD"),
		BMNCD(3,"部門",RefTable.OTHER,"BMNCD"),
		SHNCD(4,"商品コード",RefTable.TOKJU,"SHNCD"),
		SHNKBN(5,"商品区分",RefTable.TOKJU,"SHNKBN"),
		TSEIKBN(6,"訂正区分",RefTable.TOKJU,"TSEIKBN"),
		JUKBN(7,"事前区分",RefTable.TOKJU,"JUKBN"),
		BDENKBN(8,"別伝区分",RefTable.TOKJU,"BDENKBN"),
		WAPPNKBN(9,"ワッペン区分",RefTable.TOKJU,"WAPPNKBN"),
		HTDT(10,"発注日",RefTable.TOKJU,"HTDT"),
		NNDT(11,"納入日",RefTable.TOKJU,"NNDT"),
		IRISU(12,"入数",RefTable.TOKJU,"IRISU"),
		GENKAAM(13,"原価",RefTable.TOKJU,"GENKAAM"),
		BAIKAAM(14,"売価",RefTable.TOKJU,"BAIKAAM"),
		TENHTSU_ARR(15,"店発注数配列",RefTable.TOKJU,"TENHTSU_ARR"),
		SURYO(16,"数量",RefTable.TOKJU,"SURYO"),
		OPERATOR(17,"オペレータ",RefTable.OTHER,"OPERATOR"),
		ADDDT(18,"登録日",RefTable.OTHER,"ADDDT"),
		UPDDT(19,"更新日",RefTable.OTHER,"UPDDT"),
		JAN(20,"JAN",RefTable.OTHER,"JAN")
		;

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
