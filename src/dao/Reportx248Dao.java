/**
 *
 */
package dao;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import authentication.bean.User;
import authentication.defines.Consts;
import common.CmnDate;
import common.DefineReport;
import common.DefineReport.InfTrankbn;
import common.Defines;
import common.FileList;
import common.ItemList;
import common.MessageUtility;
import common.MessageUtility.Msg;
import common.MessageUtility.MsgKey;
import dao.Reportx247Dao.CSVCMNLayout;
import dao.Reportx247Dao.CSVSHNLayout;
import dao.Reportx247Dao.MSTBAIKACTLLayout;
import dao.Reportx247Dao.MSTSHINAGPLayout;
import dao.Reportx247Dao.MSTSHNLayout;
import dao.Reportx247Dao.MSTSHNTENBMNLayout;
import dao.Reportx247Dao.MSTSIRGPSHNLayout;
import dao.Reportx247Dao.MSTSRCCDLayout;
import dao.Reportx247Dao.MSTTENKABUTSULayout;
import dao.Reportx247Dao.TblType;
import dto.JQEasyModel;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 */
public class Reportx248Dao extends ItemDao {

	boolean isTest = false;

	/** 最大処理件数 */
	public static int MAX_ROW = 10000;

	/**
	 * インスタンスを生成します。
	 * @param source
	 */
	public Reportx248Dao(String JNDIname) {
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


		String szYoyakudt	= map.get(DefineReport.InpText.YOYAKUDT.getObj());		// マスタ変更予定日
		String szTenbaikadt	= map.get(DefineReport.InpText.TENBAIKADT.getObj());	// 店売価実施日
		String szCommentkn	= map.get(DefineReport.InpText.COMMENTKN.getObj());		// コメント
		String sendBtnid	= map.get("SENDBTNID");		// 呼出しボタン


		// 正・予約画面判断
		boolean isSeiRep = DefineReport.Button.CSV_IMPORT.getObj().equals(sendBtnid);


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

			// CVSトランSEQ情報
			String seq = "-1";

			// データ加工 + 最新情報取得
			ArrayList<JSONObject> dataList = new ArrayList<JSONObject>();

			// 更新区分
			String fUpdkbn = "";
			// 状態
			String status = "";

			// ソースコードファイル内重複チェック用
			HashMap<String,String> srccdMap = new HashMap<String,String>();
			Set<String> srccds = new TreeSet<String>();

			if(errMsgList.size()==0){

				// データ加工
				for (int i = idxHeader; i < dL.size(); i++) {
					Object[] data = dL.get(i);

					// CSVを分解、画面上と同じように登録用のデータの形にする
					JSONArray asrc = new JSONArray(), asir = new JSONArray(), abik = new JSONArray(), asin = new JSONArray(), aten = new JSONArray(), atbmn = new JSONArray();
					JSONObject oshn = new JSONObject(), osrc = new JSONObject(), osir = new JSONObject(), obik = new JSONObject(), osin = new JSONObject(), oten = new JSONObject(), otbmn = new JSONObject(), ooth = new JSONObject();
//					String shncd = StringUtils.trim((String) data[FileLayout.SHNCD.getNo()-1]);
					// 頭1ケタ落ち対応
//					if(StringUtils.length(shncd)==3){
//						shncd = StringUtils.leftPad(shncd, 4, "0");
//					}else if(StringUtils.length(shncd)==7){
//						shncd = StringUtils.leftPad(shncd, 8, "0");
//					}
					String inputno = Integer.toString(i+1);
					for(FileLayout itm :FileLayout.values()){
						String val = "";
						if(data.length >= (itm.getNo())){
							val = (String) data[itm.getNo()-1];
						}

						// 1.""で囲まれていた場合除去 2.末尾空白除去
						val = StringUtils.stripEnd(val.replaceFirst("^\"(.*)\"$", "$1"), null);

						if(itm.getTbl() == RefTable.MSTSHN){
							// 特殊データ加工
							if(ArrayUtils.contains(new String[]{FileLayout.BMNCD.getCol(),FileLayout.URI_BMNCD.getCol(),FileLayout.YOT_BMNCD.getCol()}, itm.getCol())){
								// 特殊データ加工
								// 部門コードは3ケタと2ケタがありうることがわかったので末尾2ケタとする
								val = StringUtils.right(val, DefineReport.InpText.BMNCD.getLen());
							} else if (StringUtils.equals(itm.getCol(), FileLayout.STATUS.getCol())) {
								status = val;
							}
							oshn.put(itm.getCol(), val);
						}else if(itm.getTbl() == RefTable.MSTSRCCD){
							// 特殊データ加工
							if(StringUtils.equals(itm.getCol(), FileLayout.SRCCD1.getCol())
								||StringUtils.equals(itm.getCol(), FileLayout.SRCCD2.getCol())){
								// 先頭に'がある場合除去
								val = StringUtils.removeStart(val, "'");

								if (!StringUtils.isEmpty(val)) {
									if (srccds.contains(val)) {
										if (!srccdMap.containsKey(inputno)) {
											srccdMap.put(inputno, val);
										}
									} else {
										srccds.add(val);
									}
								}
							}
							if(osrc.containsKey(itm.getCol())){
								asrc.add(osrc);
								osrc = new JSONObject();
							}
							if(osrc.isEmpty()){
//								osrc.put(MSTSRCCDLayout.SHNCD.getCol(), shncd);
								osrc.put(MSTSRCCDLayout.YOYAKUDT.getCol(), szYoyakudt);
								osrc.put(MSTSRCCDLayout.SEQNO.getCol(), asrc.size() + 1);
								osrc.put(CSVCMNLayout.SEQ.getCol(), seq);						// CSV用SEQ
								osrc.put(CSVCMNLayout.INPUTNO.getCol(),inputno);				// CSV用入力番号
								osrc.put(CSVCMNLayout.INPUTEDANO.getCol(), asrc.size() + 1);	// CSV用入力枝番
							}
							osrc.put(itm.getCol(), val);
						}else if(itm.getTbl() == RefTable.MSTSIRGPSHN){
							if(osir.containsKey(itm.getCol())){
								asir.add(osir);
								osir = new JSONObject();
							}
							if(osir.isEmpty()){
//								osir.put(MSTSIRGPSHNLayout.SHNCD.getCol(), shncd);
								osir.put(MSTSIRGPSHNLayout.YOYAKUDT.getCol(), szYoyakudt);
								osir.put(MSTSIRGPSHNLayout.AREAKBN.getCol(), (String) data[FileLayout.AREAKBN1.getNo()-1]);
								osir.put(CSVCMNLayout.SEQ.getCol(), seq);						// CSV用SEQ
								osir.put(CSVCMNLayout.INPUTNO.getCol(),inputno);				// CSV用入力番号
								osir.put(CSVCMNLayout.INPUTEDANO.getCol(), asir.size() + 1);	// CSV用入力枝番

							}
							osir.put(itm.getCol(), val);
						}else if(itm.getTbl() == RefTable.MSTBAIKACTL){
							if(obik.containsKey(itm.getCol())){
								abik.add(obik);
								obik = new JSONObject();
							}
							if(obik.isEmpty()){
//								obik.put(MSTBAIKACTLLayout.SHNCD.getCol(), shncd);
								obik.put(MSTBAIKACTLLayout.YOYAKUDT.getCol(), szYoyakudt);
								obik.put(MSTBAIKACTLLayout.AREAKBN.getCol(), (String) data[FileLayout.AREAKBN2.getNo()-1]);
								obik.put(CSVCMNLayout.SEQ.getCol(), seq);						// CSV用SEQ
								obik.put(CSVCMNLayout.INPUTNO.getCol(),inputno);				// CSV用入力番号
								obik.put(CSVCMNLayout.INPUTEDANO.getCol(), abik.size() + 1);	// CSV用入力枝番
							}
							obik.put(itm.getCol(), val);
						}else if(itm.getTbl() == RefTable.MSTSHINAGP){
							if(osin.containsKey(itm.getCol())){
								asin.add(osin);
								osin = new JSONObject();
							}
							if(osin.isEmpty()){
//								osin.put(MSTSHINAGPLayout.SHNCD.getCol(), shncd);
								osin.put(MSTSHINAGPLayout.YOYAKUDT.getCol(), szYoyakudt);
								osin.put(MSTSHINAGPLayout.AREAKBN.getCol(), (String) data[FileLayout.AREAKBN3.getNo()-1]);
								osin.put(CSVCMNLayout.SEQ.getCol(), seq);						// CSV用SEQ
								osin.put(CSVCMNLayout.INPUTNO.getCol(),inputno);				// CSV用入力番号
								osin.put(CSVCMNLayout.INPUTEDANO.getCol(), asin.size() + 1);	// CSV用入力枝番
							}
							osin.put(itm.getCol(), val);
						}else if(itm.getTbl() == RefTable.MSTTENKABUTSU){
							if(oten.containsKey(itm.getCol())){
								aten.add(oten);
								oten = new JSONObject();
							}
							if(oten.isEmpty()){
//								oten.put(MSTTENKABUTSULayout.SHNCD.getCol(), shncd);
								oten.put(MSTTENKABUTSULayout.YOYAKUDT.getCol(), szYoyakudt);
								if(StringUtils.startsWith(itm.getTxt(), DefineReport.ValTenkabkbn.VAL1.getTxt())){
									oten.put(MSTTENKABUTSULayout.TENKABKBN.getCol(), DefineReport.ValTenkabkbn.VAL1.getVal());
								}else{
									oten.put(MSTTENKABUTSULayout.TENKABKBN.getCol(), DefineReport.ValTenkabkbn.VAL2.getVal());
								}
								oten.put(CSVCMNLayout.SEQ.getCol(), seq);						// CSV用SEQ
								oten.put(CSVCMNLayout.INPUTNO.getCol(),inputno);				// CSV用入力番号
								oten.put(CSVCMNLayout.INPUTEDANO.getCol(), aten.size() + 1);	// CSV用入力枝番
							}
							oten.put(itm.getCol(), val);


						}else if(itm.getTbl() == RefTable.MSTSHNTENBMN){
							if(otbmn.containsKey(itm.getCol())){
								atbmn.add(otbmn);
								otbmn = new JSONObject();
							}

							if(otbmn.isEmpty()){
//								otbmn.put(MSTSHNTENBMNLayout.SHNCD.getCol(), shncd);
								otbmn.put(MSTSHNTENBMNLayout.YOYAKUDT.getCol(), szYoyakudt);
								String areakbn = "";
								if((FileLayout.TENBMNCD.getNo()-1) <= data.length){
									areakbn = (String) data[FileLayout.TENBMNCD.getNo()-1];
								}
								otbmn.put(MSTSHNTENBMNLayout.AREAKBN.getCol(), areakbn);
								otbmn.put(CSVCMNLayout.SEQ.getCol(), seq);						// CSV用SEQ
								otbmn.put(CSVCMNLayout.INPUTNO.getCol(),inputno);				// CSV用入力番号
								otbmn.put(CSVCMNLayout.INPUTEDANO.getCol(), aten.size() + 1);	// CSV用入力枝番
							}
							otbmn.put(itm.getCol(), val);

						}else if(itm.getTbl() == RefTable.OTHER){
							if (itm.getCol().equals(FileLayout.UPDKBN.getCol())) {
								fUpdkbn = val;
							}
							ooth.put(itm.getCol(), val);
						}
					}
					oshn.put(MSTSHNLayout.YOYAKUDT.getCol(), szYoyakudt);
					oshn.put(MSTSHNLayout.TENBAIKADT.getCol(), szTenbaikadt);
					oshn.put(MSTSHNLayout.TOROKUMOTO.getCol(), '1');
					asrc.add(osrc);
					asir.add(osir);
					abik.add(obik);
					asin.add(osin);
					aten.add(oten);
					atbmn.add(otbmn);
					asrc = selectCompData(oshn, asrc,RefTable.PIMTISRCCD.getId(),MSTSRCCDLayout.SRCCD.getCol(),MSTSRCCDLayout.SEQNO.getCol(),MSTSRCCDLayout.values());
					asir = selectCompData(oshn, asir,RefTable.PIMTISIRGPSHN.getId(),MSTSIRGPSHNLayout.TENGPCD.getCol(),"",MSTSIRGPSHNLayout.values());
					abik = selectCompData(oshn, abik,RefTable.PIMTIBAIKACTL.getId(),MSTBAIKACTLLayout.TENGPCD.getCol(),"",MSTBAIKACTLLayout.values());
					asin = selectCompData(oshn, asin,RefTable.PIMTISHINAGP.getId(),MSTSHINAGPLayout.TENGPCD.getCol(),"",MSTSHINAGPLayout.values());
					aten = selectCompData(oshn, aten,RefTable.PIMTITENKABUTSU.getId(),MSTTENKABUTSULayout.TENKABCD.getCol(),"",MSTTENKABUTSULayout.values());
					atbmn = selectCompData(oshn, atbmn,RefTable.MSTSHNTENBMN.getId(),MSTSHNTENBMNLayout.TENSHNCD.getCol(),"",MSTSHNTENBMNLayout.values());

					// 最新の情報取得
					// データ取得先判断
					boolean isNew = DefineReport.ValFileUpdkbn.NEW.getVal().equals(fUpdkbn);
//					boolean isEmptyShn = StringUtils.isEmpty(shncd);

					// TODO:各メソッドの第二引数は現在未使用
					// 1.商品マスタ
					//JSONArray mstshnArray = new JSONArray();
					//mstshnArray.add(oshn);
					JSONArray mstshnArray = this.selectMSTSHN(isSeiRep, isNew, oshn);
					// 2.ソースマスタ
					JSONArray mstsrcArray = this.selectMSTSRC(isSeiRep, isNew, asrc);
					// 3.仕入グループ商品マスタ
					JSONArray mstsirArray = this.selectMSTSIRGPSHN(isSeiRep, isNew, asir);
					// 4.売価コントロールマスタ
					JSONArray mstbikArray = this.selectMSTBAIKACTL(isSeiRep, isNew, abik);
					// 5.品揃グループマスタ
					JSONArray mstsinArray = this.selectMSTSHINAGP(isSeiRep, isNew, asin);
					// 6.添加物
					JSONArray msttenArray = this.selectMSTTENKABUTSU(isSeiRep, isNew, aten);
					// 7.グループ名
					JSONArray mstgrpArray = new JSONArray(); // TODO:仕様確定後 this.selectMSTTENKABUTSU(isSei, isNew||isEmptyShn, aten);
					// 8.自動発注
					JSONArray mstahsArray = new JSONArray(); // TODO:仕様確定後 this.selectMSTTENKABUTSU(isSei, isNew||isEmptyShn, aten);
					// 9.店別異部門
					JSONArray msttbmnArray = this.selectMSTSHNTENBMN(isSeiRep, isNew, atbmn);// TODO:仕様確定後 this.selectMSTTENKABUTSU(isSei, isNew||isEmptyShn, aten);
					// 10.その他
					ooth.put(CSVSHNLayout.SEQ.getCol(), seq);				// CSV用.SEQ
					ooth.put(CSVSHNLayout.INPUTNO.getCol(), inputno);		// CSV用.入力番号

					JSONObject objset = new JSONObject();
					objset.put(MSTSHNLayout.UPDKBN.getCol(), fUpdkbn);	// 更新区分
//					objset.put(MSTSHNLayout.SHNCD.getCol(), shncd);		// 商品コード
					objset.put("DATA", mstshnArray);			// 対象情報（主要な更新情報）
					objset.put("DATA_SRCCD", mstsrcArray);		// ソースコード
					objset.put("DATA_TENGP3",mstsinArray);		// 品揃えグループ
					objset.put("DATA_TENGP2",mstbikArray);		// 売価コントロール
					objset.put("DATA_TENGP1",mstsirArray);		// 仕入グループ
					objset.put("DATA_TENKABUTSU",msttenArray);	// 添加物
					objset.put("DATA_GROUP",mstgrpArray);		// グループ名
					objset.put("DATA_AHS",mstahsArray);			// 自動発注
					objset.put("DATA_TENGP4",msttbmnArray);		// 店別異部門
					objset.put("DATA_OTHER", ooth);

					dataList.add(objset);
				}

				int updCount = 0;
				int errCount = 0;

				// *** 詳細情報チェック＋情報登録用SQL作成 ***
				String userId	= userInfo.getId();								// ログインユーザー

				// 現在のジャーナルSEQ情報取得
				String jnlshn_seq = "";
				int isShnInserted = 0;
				// 各テーブル登録用のSQL作成
				for (int i = 0; i < dataList.size(); i++) {
					Reportx247Dao dao = new Reportx247Dao(super.JNDIname);

					// 商品マスタメイン処理Dao呼出し

					JSONObject objset = dataList.get(i);

					String shncd  = objset.optString(MSTSHNLayout.SHNCD.getCol());
					fUpdkbn = objset.optString(MSTSHNLayout.UPDKBN.getCol());
					HashMap<String, String> sendmap = new HashMap<String, String>();
					sendmap.put("SEL_SHNCD", shncd);			// 検索商品コード
					sendmap.put("SHNCD", shncd);				// 商品コード
					sendmap.put("YOYAKUDT", szYoyakudt);		// マスタ変更予定日
					sendmap.put("TENBAIKADT", szTenbaikadt);	// 店売価実施日
					sendmap.put("SEQ", seq);					// SEQ
					sendmap.put("SENDBTNID", sendBtnid);		// 呼出しボタン
					sendmap.put("STATUS", status);				// 状態

					// 1.商品マスタ
					JSONArray mstshnArray = objset.optJSONArray("DATA");
					// 2.ソースマスタ
					JSONArray mstsrcArray = objset.optJSONArray("DATA_SRCCD");
					// 3.仕入グループ商品マスタ
					JSONArray mstsirArray = objset.optJSONArray("DATA_TENGP1");
					// 4.売価コントロールマスタ
					JSONArray mstbikArray = objset.optJSONArray("DATA_TENGP2");
					// 5.品揃グループマスタ
					JSONArray mstsinArray = objset.optJSONArray("DATA_TENGP3");
					// 6.添加物
					JSONArray msttenArray = objset.optJSONArray("DATA_TENKABUTSU");
					// 7.グループ名
					JSONArray mstgrpArray = objset.optJSONArray("DATA_GROUP");
					// 8.自動発注
					JSONArray mstahsArray= objset.optJSONArray("DATA_AHS");
					// 9.店別異部門
					JSONArray msttbmnArray= objset.optJSONArray("DATA_TENGP4");
					// 10.その他
					JSONObject ooth = objset.optJSONObject("DATA_OTHER");

					// 基本情報チェック
					JSONArray msgListB = this.checkData(
							isSeiRep, dao,
							sendmap, userInfo, sysdate, mu,
							mstshnArray,		// 対象情報（主要な更新情報）
							new JSONArray(),
							mstsrcArray,		// ソースコード
							mstsinArray,		// 品揃えグループ
							mstbikArray,		// 売価コントロール
							mstsirArray,		// 仕入グループ
							msttenArray,		// 添加物
							// TODO mstgrpArray,		// グループ名
							ooth				// その他
						);

					// デフォルト値設定
					// 基本エラーがなかった場合、画面上で設定されるデフォルト値設定
					if(msgListB.size()==0){
						CmnDate cd = new CmnDate();
						String def_yuko_stdt = cd.getTomorrow();
						String def_yuko_eddt = "20501231";
						// 2.ソースマスタ
						for (int i1=0; i1<mstsrcArray.size(); i1++){
							if(StringUtils.isEmpty(mstsrcArray.getJSONObject(i1).optString(MSTSRCCDLayout.YUKO_STDT.getId()))){
								mstsrcArray.getJSONObject(i1).element(MSTSRCCDLayout.YUKO_STDT.getId(), def_yuko_stdt);
							}
							if(StringUtils.isEmpty(mstsrcArray.getJSONObject(i1).optString(MSTSRCCDLayout.YUKO_EDDT.getId()))){
								mstsrcArray.getJSONObject(i1).element(MSTSRCCDLayout.YUKO_EDDT.getId(), def_yuko_eddt);
							}
						}
					}

					if (srccdMap.containsKey(ooth.optString(CSVSHNLayout.INPUTNO.getCol()))) {
						JSONObject o = mu.getDbMessageObj("E11109", new String[]{srccdMap.get(ooth.optString(CSVSHNLayout.INPUTNO.getCol())) + ":"});
						this.setCsvshnErrinfo(o, RefTable.CSV, FileLayout.SRCCD1, fUpdkbn);
						msgListB.add(o);
					}

					// SQLパターン
					// ①正 .新規 → 正 ：Insert処理、ジャ：Insert処理
					// ②正 .変更 → 正 ：Update処理、ジャ：Insert処理								※予1がある場合不可
					// ×常に削除後実行するのでDelete/Insert ④予1.新規 → 予1：Insert処理、ジャ：Insert処理
					// ⑤予1.変更 → 予1：Delete/Insert処理、ジャ：Insert処理						※予2がある場合不可
					// ⑥予1.削除 → 予1：Delete処理、ジャ：Insert処理								※予2がある場合不可
					// ⑦CSVエラー.新規 → ERR：Insert処理
					boolean isNew = isSeiRep && DefineReport.ValFileUpdkbn.NEW.getVal().equals(fUpdkbn);
					boolean isChange = isSeiRep && DefineReport.ValFileUpdkbn.UPD.getVal().equals(fUpdkbn);
					boolean isYoyaku1 = !isSeiRep;
					boolean isChangeY1 = !isSeiRep && DefineReport.ValFileUpdkbn.YYK.getVal().equals(fUpdkbn);
					boolean isChangeY1Del = !isSeiRep && DefineReport.ValFileUpdkbn.YDEL.getVal().equals(fUpdkbn);

					// 詳細情報チェック
					JSONArray msgList = new JSONArray();
					if(msgListB.size()==0){
						if(!isChangeY1Del){
							msgList = new Reportx002Dao(super.JNDIname).checkData(
									isNew, isChange, isYoyaku1, false, false, true,
									sendmap, userInfo, sysdate, mu,
									mstshnArray,		// 対象情報（主要な更新情報）
									new JSONArray(),
									mstsrcArray,		// ソースコード
									msttbmnArray,		// 店別異部門
									mstsinArray,		// 品揃えグループ
									mstbikArray,		// 売価コントロール
									mstsirArray,		// 仕入グループ
									msttenArray,		// 添加物
									mstgrpArray,		// グループ名
									mstahsArray,		// 自動発注
									ooth				// その他
								);
						}
					}else{
						msgList.add(msgListB.optJSONObject(0));
					}

					boolean isError = msgList.size()!=0;
					if(isError){
						errCount++;

//						// エラー時に商品コード採番時解放処理
//						if(StringUtils.isNotEmpty(ooth.optString("SHNCD_RENEW"))){
//							// 商品コード情報、もしくはエラー情報が返ってくる
//							JSONObject result = NumberingUtility.execReleaseNewSHNCD(userInfo, ooth.optString("SHNCD_RENEW"));
//						}
//						// 採番実行時にエラーの場合、解除処理
//						if(StringUtils.isNotEmpty(ooth.optString("URICD_NEW"))){
//							// 販売コード情報、もしくはエラー情報が返ってくる
//							JSONObject result = NumberingUtility.execReleaseNewURICD(userInfo, ooth.optString("URICD_NEW"));
//						}

						// CVSトランSEQ情報取得
						if(StringUtils.equals(seq, "-1")){
							seq = this.getCSVSHN_SEQ();
							// ヘッダ登録用SQL作成
							JSONObject hdrtn = this.createSqlCSVSHNHEAD(userId, sendBtnid, seq, szCommentkn);
						}

						// CSV用情報セット
						dao.csvshn_seq = seq;
						dao.csvshn_add_data[CSVSHNLayout.SEQ.getNo()-1] = seq;													// CSV用.SEQ
						dao.csvshn_add_data[CSVSHNLayout.INPUTNO.getNo()-1] = ooth.optString(CSVSHNLayout.INPUTNO.getCol());	// CSV用.入力番号
						dao.csvshn_add_data[CSVSHNLayout.ERRCD.getNo()-1] = msgList.optJSONObject(0).optString(CSVSHNLayout.ERRCD.getCol());		// ERRCD
						dao.csvshn_add_data[CSVSHNLayout.ERRFLD.getNo()-1] = msgList.optJSONObject(0).optString(CSVSHNLayout.ERRFLD.getCol());	// ERRFLD
						dao.csvshn_add_data[CSVSHNLayout.ERRVL.getNo()-1] = msgList.optJSONObject(0).optString(CSVSHNLayout.ERRVL.getCol());		// ERRVL
						dao.csvshn_add_data[CSVSHNLayout.ERRTBLNM.getNo()-1] = msgList.optJSONObject(0).optString(CSVSHNLayout.ERRTBLNM.getCol());	// ERRTBLNM
						String csvUpdkbn = DefineReport.ValCsvUpdkbn.NEW.getVal();
						if(DefineReport.ValFileUpdkbn.UPD.getVal().equals(fUpdkbn)){
							csvUpdkbn = DefineReport.ValCsvUpdkbn.UPD.getVal();
						}else if(DefineReport.ValFileUpdkbn.YYK.getVal().equals(fUpdkbn)){
							csvUpdkbn = DefineReport.ValCsvUpdkbn.YYK.getVal();
						}else if(DefineReport.ValFileUpdkbn.YDEL.getVal().equals(fUpdkbn)){
							csvUpdkbn = DefineReport.ValCsvUpdkbn.YDEL.getVal();
						}
						dao.csvshn_add_data[CSVSHNLayout.CSV_UPDKBN.getNo()-1] = csvUpdkbn;										// CSV_UPDKBN
//						dao.csvshn_add_data[CSVSHNLayout.KETAKBN.getNo()-1] = ooth.optString(FileLayout.KETAKBN.getCol());		// KETAKBN
					}else{
						updCount++;
						jnlshn_seq = this.getJNLSHN_SEQ_NOW();
					}

					isShnInserted = this.createCommandUpdateData(
							dao,
							isNew, isSeiRep, isError,jnlshn_seq,
							sendmap, userInfo, sysdate,
							mstshnArray,		// 対象情報（主要な更新情報）
							mstsrcArray,		// ソースコード
							msttbmnArray,		// 店別異部門
							mstsinArray,		// 品揃えグループ
							mstbikArray,		// 売価コントロール
							mstsirArray,		// 仕入グループ
							msttenArray,		// 添加物
							ooth				// その他
						);

					this.sqlList.addAll(dao.sqlList);
					this.prmList.addAll(dao.prmList);
					this.lblList.addAll(dao.lblList);
				}

				// 更新処理
				try {
					option = this.updateData(isShnInserted);
				} catch (Exception e) {
					e.printStackTrace();
					errMsgList.add(MessageUtility.getDbMessageIdObj("E30007", new String[]{}));
				}

				// 実行結果のメッセージを取得
				strMsg = this.getMessage();
				if(errMsgList.size()==0){
					// 実行トラン情報をJSに戻す
					option.put(DefineReport.Text.SEQ.getObj(), seq);
					option.put(DefineReport.Text.STATUS.getObj(), "完了");
					option.put(DefineReport.Text.UPD_NUMBER.getObj(), dataList.size());
					option.put(DefineReport.Text.ERR_NUMBER.getObj(), errCount);
				}
			}

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

	private JSONArray checkData(boolean isSeiRep, Reportx247Dao dao,
			HashMap<String, String> map, User userInfo, String sysdate1, MessageUtility mu,
			JSONArray dataArray,			// 対象情報（主要な更新情報）
			JSONArray dataArrayAdd,			// 対象情報（追加更新情報）
			JSONArray dataArraySRCCD,		// ソースコード
			JSONArray dataArrayTENGP3,		// 品揃えグループ
			JSONArray dataArrayTENGP2,		// 売価コントロール
			JSONArray dataArrayTENGP1,		// 仕入グループ
			JSONArray dataArrayTENKABUTSU,	// 添加物
			JSONObject dataOther			// その他情報
			) {
		JSONArray msg = new JSONArray();

		String fUpdkbn = dataOther.optString(FileLayout.UPDKBN.getCol());

		RefTable errTbl = RefTable.CSV;

		// 画面とファイル更新区分に誤りがないか
		// 正画面で予約更新
		if(isSeiRep && (DefineReport.ValFileUpdkbn.YYK.getVal().equals(fUpdkbn)||DefineReport.ValFileUpdkbn.YDEL.getVal().equals(fUpdkbn))){
			JSONObject o = mu.getDbMessageObj("E40009", new String[]{});
			this.setCsvshnErrinfo(o, errTbl, FileLayout.UPDKBN, fUpdkbn);
			msg.add(o);
			return msg;
		}
		// 予約画面で正更新
		if(!isSeiRep && (DefineReport.ValFileUpdkbn.NEW.getVal().equals(fUpdkbn)||DefineReport.ValFileUpdkbn.UPD.getVal().equals(fUpdkbn))){
			JSONObject o = mu.getDbMessageObj("E40009", new String[]{});
			this.setCsvshnErrinfo(o, errTbl, FileLayout.UPDKBN, fUpdkbn);
			msg.add(o);
			return msg;
		}

		// SQLパターン
		// ①正 .新規 → 正 ：Insert処理、ジャ：Insert処理
		// ②正 .変更 → 正 ：Update処理、ジャ：Insert処理								※予1がある場合不可
		// ×常に削除後実行するのでDelete/Insert ④予1.新規 → 予1：Insert処理、ジャ：Insert処理
		// ⑤予1.変更 → 予1：Delete/Insert処理、ジャ：Insert処理						※予2がある場合不可
		// ⑥予1.削除 → 予1：Delete処理、ジャ：Insert処理								※予2がある場合不可
		// ⑦CSVエラー.新規 → ERR：Insert処理
		boolean isNew = isSeiRep && DefineReport.ValFileUpdkbn.NEW.getVal().equals(fUpdkbn);
		boolean isChange = isSeiRep && DefineReport.ValFileUpdkbn.UPD.getVal().equals(fUpdkbn);
		boolean isYoyaku1 = !isSeiRep;
		boolean isChangeY1 = !isSeiRep && DefineReport.ValFileUpdkbn.YYK.getVal().equals(fUpdkbn);
		boolean isChangeY1Del = !isSeiRep && DefineReport.ValFileUpdkbn.YDEL.getVal().equals(fUpdkbn);

		// DB最新情報再取得
		String szYoyaku = "0";
		JSONArray yArray = new JSONArray();
		if(!isNew){
			JSONArray array = dao.getYoyakuJSONArray(map);
			szYoyaku = Integer.toString(array.size());
			yArray.addAll(array);
		}
		if(isChange && NumberUtils.toInt(szYoyaku, 0) > 0){
			JSONObject o = mu.getDbMessageObj("EX1010", new String[]{});
			this.setCsvshnErrinfo(o, errTbl, FileLayout.UPDKBN, fUpdkbn);
			msg.add(o);
			return msg;
		}
		if(isYoyaku1 && NumberUtils.toInt(szYoyaku, 0) > 1){
			JSONObject o = mu.getDbMessageObj("EX1011", new String[]{});
			this.setCsvshnErrinfo(o, errTbl, FileLayout.UPDKBN, fUpdkbn);
			msg.add(o);
			return msg;
		}
		// 予約の削除
		if( isChangeY1Del ){
			if(isYoyaku1 && NumberUtils.toInt(szYoyaku, 0) > 1){
				JSONObject o = mu.getDbMessageObj("EX1009", new String[]{});
				this.setCsvshnErrinfo(o, errTbl, FileLayout.UPDKBN, fUpdkbn);
				msg.add(o);
				return msg;
			}
			errTbl = RefTable.MSTSHN;
			JSONObject data = dataArray.optJSONObject(0);
			String txt_yoyakudt = data.optString(MSTSHNLayout.YOYAKUDT.getId());
			String txt_tenbaikadt = data.optString(MSTSHNLayout.TENBAIKADT.getId());

			String txt_yoyakudt_ = "";		// 検索実行時のﾏｽﾀ変更日
			String txt_tenbaikadt_ = "";	// 検索実行時の店売価実施日
			if(yArray.size() > 0){
				txt_yoyakudt_ = yArray.optJSONObject(0).optString(MSTSHNLayout.YOYAKUDT.getId());			// 検索実行時のﾏｽﾀ変更日
				txt_tenbaikadt_ = yArray.optJSONObject(0).optString(MSTSHNLayout.TENBAIKADT.getId());		// 検索実行時の店売価実施日

			}
			if(NumberUtils.toInt(txt_yoyakudt) != NumberUtils.toInt(txt_yoyakudt_)){
				JSONObject o = mu.getDbMessageObj("E11195", new String[]{});
				dao.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.YOYAKUDT, data);
				msg.add(o);
				return msg;
			}
			if(NumberUtils.toInt(txt_tenbaikadt) != NumberUtils.toInt(txt_tenbaikadt_)){
				JSONObject o = mu.getDbMessageObj("E11196", new String[]{});
				dao.setCsvshnErrinfo(o, errTbl, MSTSHNLayout.TENBAIKADT, data);
				msg.add(o);
				return msg;
			}
		}
		// 2.ソースマスタ
		if(isNew){
			errTbl = RefTable.MSTSRCCD;
			for (int i = 0; i < dataArraySRCCD.size(); i++) {
				JSONObject data = dataArraySRCCD.optJSONObject(0);
				String srccd = data.optString("F"+MSTSRCCDLayout.SRCCD.getNo());

				if(StringUtils.isNotEmpty(srccd)){
					ItemList iL = new ItemList();
					// 配列準備
					ArrayList<String> paramData = new ArrayList<String>();
					String sqlcommand = "select count(*) as VALUE from INAWS.PIMTISRCCD where SRCCD = ?";
					paramData.add(srccd);

					@SuppressWarnings("static-access")
					JSONArray array = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
					if(array.size() > 0 && array.getJSONObject(0).optInt("VALUE") > 0){
						// 登録済みソースコードを新規登録した場合はエラーとする。
						JSONObject o = mu.getDbMessageObj("E11139", new String[]{});
						dao.setCsvshnErrinfo(o, errTbl, MSTSRCCDLayout.SRCCD, data);
						msg.add(o);
						return msg;
					}

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
		if(FileLayout.values().length != eL.get(idxHeader).length && 232 != eL.get(idxHeader).length){
			JSONObject o = MessageUtility.getDbMessageIdObj("E40008", new String[]{});
			msg.add(o);
			return msg;
		}
		return msg;
	}

	/** SQL保持用変数 */
	String sqlCmd = "";
	/** SQLのパラメータ保持用変数 */
	ArrayList<String> cmdParams = new ArrayList<String>();
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
	private JSONObject updateData(int isShnInserted) throws Exception {

		JSONObject option = new JSONObject();

		ArrayList<Integer> countList  = new ArrayList<Integer>();
		if(sqlList.size() > 0){
			countList =  super.executeSQLs(sqlList, prmList);
		}

		if(StringUtils.isEmpty(getMessage())){
			int count = isShnInserted;
			for (int i = 0; i < countList.size(); i++) {
				count += countList.get(i);
				if (DefineReport.ID_DEBUG_MODE)	System.out.println(MessageUtility.getMessage(Msg.S00006.getVal(), new String[]{lblList.get(i), Integer.toString(countList.get(i))}));
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
	private int createCommandUpdateData(
			Reportx247Dao dao,
			boolean isNewtyp, boolean isSeiRep, boolean isErr, String jnlshn_seq,
			HashMap<String, String> map, User userInfo, String sysdate,
			JSONArray dataArray,				// 対象情報（主要な更新情報）
			JSONArray dataArraySRCCD,		// ソースコード
			JSONArray dataArrayTENGP4,		// 店別異部門
			JSONArray dataArrayTENGP3,		// 品揃えグループ
			JSONArray dataArrayTENGP2,		// 売価コントロール
			JSONArray dataArrayTENGP1,		// 仕入グループ
			JSONArray dataArrayTENKABUTSU,	// 添加物
			JSONObject dataOther			// その他情報
		) throws Exception {

//		JSONObject option = new JSONObject();


		// パラメータ確認
		String szShncd		= map.get("SHNCD");			// 入力商品コード
		String sendBtnid	= map.get("SENDBTNID");		// 呼出しボタン
		// ログインユーザー情報取得
		String userId	= userInfo.getId();				// ログインユーザー

		JSONObject data = dataArray.optJSONObject(0);


		String fUpdkbn = dataOther.optString(FileLayout.UPDKBN.getCol());

		// SQLパターン
		// ①正 .新規 → 正 ：Insert処理、ジャ：Insert処理
		// ②正 .変更 → 正 ：Update処理、ジャ：Insert処理								※予1がある場合不可
		// ×常に削除後実行するのでDelete/Insert ④予1.新規 → 予1：Insert処理、ジャ：Insert処理
		// ⑤予1.変更 → 予1：Delete/Insert処理、ジャ：Insert処理						※予2がある場合不可
		// ⑥予1.削除 → 予1：Delete処理、ジャ：Insert処理								※予2がある場合不可
		// ⑦CSVエラー.新規 → ERR：Insert処理
		boolean isNew = !isErr && isSeiRep && DefineReport.ValFileUpdkbn.NEW.getVal().equals(fUpdkbn);
		boolean isChange = !isErr && isSeiRep && DefineReport.ValFileUpdkbn.UPD.getVal().equals(fUpdkbn);
		boolean isChangeY1 = !isErr && !isSeiRep && DefineReport.ValFileUpdkbn.YYK.getVal().equals(fUpdkbn);
		boolean isChangeY1Del = !isErr && !isSeiRep && DefineReport.ValFileUpdkbn.YDEL.getVal().equals(fUpdkbn);

		// PIMTIT auto cremental ID
		int targetId = 0;

		// ①正 .新規
		if(isNew){
			dao.jnlshn_trankbn = InfTrankbn.INS.getVal();

			// --- 01.商品
			JSONObject result1 = dao.createSqlMSTSHN(userId, sendBtnid, data, TblType.SEI, SqlType.MRG, data.getString("TITKNNO"), data.getString("TITSTCD"));
			//　実行
			targetId = super.executeSQLReturnId(dao.sqlCmd, dao.cmdParams, "SHNCD", 0, "INAWS.PIMTIT");

			// --- 08.商品コード空き番
			//JSONObject result8 = dao.createSqlSYSSHNCD_AKI(userId, sendBtnid, data, TblType.SEI, SqlType.MRG);

			// --- 09.販売コード付番管理
			//JSONObject result9 = dao.createSqlSYSURICD_FU(userId, sendBtnid, data, TblType.SEI, SqlType.UPD);

			// --- 10.販売コード空き番
			//JSONObject result10 = dao.createSqlSYSURICD_AKI(userId, sendBtnid, data, TblType.SEI, SqlType.UPD);


		} else if(isErr){

			// ************ 関連テーブル処理 ***********
			// --- 07.メーカー
			String makercd = data.optString("F72");
			String txt_bmncd = data.optString("F12");
			if(dataArraySRCCD.size() > 0){
				// メーカーコード存在チェック
				if(StringUtils.isEmpty(makercd) || (StringUtils.isNotEmpty(makercd) && !dao.checkMstExist(DefineReport.InpText.MAKERCD.getObj(), makercd))){
					// ソースコードからメーカーコードの取得
					// 添付資料（MD03112501）のメーカーコードの取得方法
					String value = dataArraySRCCD.optJSONObject(0).optString(MSTSRCCDLayout.SRCCD.getId());
					String kbn = dataArraySRCCD.optJSONObject(0).optString(MSTSRCCDLayout.SOURCEKBN.getId());
					ArrayList<String> paramData = new ArrayList<String>(Arrays.asList(value, kbn, txt_bmncd));
					JSONArray makercd_row = dao.getMstData(DefineReport.ID_SQL_MD03112501, paramData);				// ソースコードからメーカーコード取得
					String txt_makercd_src = "";
					if(makercd_row.size() > 0){
						txt_makercd_src = makercd_row.optJSONObject(0).optString("VALUE");
					}
					// メーカーコードが入力有の場合
					if(makercd.length() > 0){
						// 入力されたメーカーコードがメーカーマスタに存在しない、かつ、ソースコードから取得したメーカーコードとも異なる場合
						if(!(!dao.checkMstExist(DefineReport.InpText.MAKERCD.getObj(), makercd) && !StringUtils.equals(txt_makercd_src, makercd))){
							makercd = txt_makercd_src;
						}
					} else {
						makercd = txt_makercd_src;
					}
				}
			} else {
				// ソースコードの入力がない場合
				if(makercd.length() == 0){
					// メーカーコードが空白の場合はデフォルト値を適用する。
					makercd = txt_bmncd + "00001";
				}
			}

			// メーカーコードの置き換え
			if (data.containsKey("F72")) {
				data.element("F72",makercd);
			} else {
				data.put("F72",makercd);
			}

			// --- 01.商品
			JSONObject result1 = dao.createSqlMSTSHN(userId, sendBtnid, data, TblType.CSV, SqlType.INS, data.getString("TITKNNO"), data.getString("TITSTCD"));
			//　実行
			targetId = super.executeSQLReturnId( dao.sqlCmd, dao.cmdParams, "SEQ", 0, "INAWS.CSVSHN");

		}

		// ************ 子テーブル処理 ***********
		// 2004/03/05に、子テーブルの同一項目考慮はなくなった模様
		TblType baseTblType = isNew||isChange ? TblType.SEI :TblType.YYK;

		if(isSeiRep && isErr){
			baseTblType = TblType.CSV;

			// --- 02.仕入グループ
			if(dataArrayTENGP1.size() > 0){
//				dataArrayTENGP1 = dao.addSHNCD(dataArrayTENGP1, isNew, String.valueOf(targetId));
				JSONObject result2 = dao.createSqlMSTSIRGPSHN(userId, sendBtnid, dataArrayTENGP1, baseTblType, SqlType.INS);
			}

			// --- 03.売価コントロール
			if(dataArrayTENGP2.size() > 0){
//				dataArrayTENGP2 = dao.addSHNCD(dataArrayTENGP2, isNew, String.valueOf(targetId));
				JSONObject result3 = dao.createSqlMSTBAIKACTL(userId, sendBtnid, dataArrayTENGP2, baseTblType, SqlType.INS);
			}
			// --- 04.ソースコード管理
			if(dataArraySRCCD.size() > 0){
//				dataArraySRCCD = dao.addSHNCD(dataArraySRCCD, isNew, String.valueOf(targetId));
				JSONObject result4 = dao.createSqlMSTSRCCD(userId, sendBtnid, dataArraySRCCD, baseTblType, SqlType.INS);
			}

			// --- 05.添加物
			if(dataArrayTENKABUTSU.size() > 0){
//				dataArrayTENKABUTSU = dao.addSHNCD(dataArrayTENKABUTSU, isNew, String.valueOf(targetId));
				JSONObject result5 = dao.createSqlMSTTENKABUTSU(userId, sendBtnid, dataArrayTENKABUTSU, baseTblType, SqlType.INS);
			}

			// --- 06.品揃グループ
			if(dataArrayTENGP3.size() > 0){
//				dataArrayTENGP3 = dao.addSHNCD(dataArrayTENGP3, isNew, String.valueOf(targetId));
				JSONObject result6 = dao.createSqlMSTSHINAGP(userId, sendBtnid, dataArrayTENGP3, baseTblType, SqlType.INS);
			}

			// --- 09.店別異部門
			if(dataArrayTENGP4.size() > 0){
//				dataArrayTENGP4 = dao.addSHNCD(dataArrayTENGP4, isNew, String.valueOf(targetId));
				JSONObject result6 = dao.createSqlMSTSHNTENBMN(userId, sendBtnid, dataArrayTENGP4, baseTblType, SqlType.INS);
			}


		}else if(!isErr && !isChangeY1Del){
			JSONArray dataArrayDel = new JSONArray();
			JSONArray dataArrayDelTENKABUTSU = new JSONArray();
			JSONArray dataArrayDelMSTSHNTENBMN = new JSONArray();
			// 子テーブルは、一度削除してから追加なので、キー項目に注意
			if(isChangeY1){
				String szYoyakudt = data.optString("F2");
				dataArrayDel.add(dao.createJSONObject(new String[]{"F1", "F3"}, new String[]{szShncd, szYoyakudt}));
				dataArrayDelTENKABUTSU.add(this.createJSONObject(new String[]{"F1", "F4"}, new String[]{szShncd, szYoyakudt}));
				dataArrayDelMSTSHNTENBMN.add(this.createJSONObject(new String[]{"F1", "F4"}, new String[]{szShncd, szYoyakudt}));
			}else{
				dataArrayDel.add(dao.createJSONObject(new String[]{"F1"}, new String[]{szShncd}));
				dataArrayDelTENKABUTSU.add(this.createJSONObject(new String[]{"F1"}, new String[]{szShncd}));
				dataArrayDelMSTSHNTENBMN.add(this.createJSONObject(new String[]{"F1"}, new String[]{szShncd}));
			}

			// --- 02.仕入グループ
			JSONObject result2D= dao.createSqlMSTSIRGPSHN(userId, sendBtnid, dataArrayDel, baseTblType, SqlType.DEL);
			if(dataArrayTENGP1.size() > 0){
				dataArrayTENGP1 = dao.addSHNCD(dataArrayTENGP1, isNew, String.format("%08d",targetId));
				JSONObject result2 = dao.createSqlMSTSIRGPSHN(userId, sendBtnid, dataArrayTENGP1, baseTblType, SqlType.INS);
			}

			// --- 03.売価コントロール
			JSONObject result3D= dao.createSqlMSTBAIKACTL(userId, sendBtnid, dataArrayDel, baseTblType, SqlType.DEL);
			if(dataArrayTENGP2.size() > 0){
				dataArrayTENGP2 = dao.addSHNCD(dataArrayTENGP2, isNew, String.format("%08d",targetId));
				JSONObject result3 = dao.createSqlMSTBAIKACTL(userId, sendBtnid, dataArrayTENGP2, baseTblType, SqlType.INS);
			}
			// --- 04.ソースコード管理
			JSONObject result4D= dao.createSqlMSTSRCCD(userId, sendBtnid, dataArrayDel, baseTblType, SqlType.DEL);
			if(dataArraySRCCD.size() > 0){
				dataArraySRCCD = dao.addSHNCD(dataArraySRCCD, isNew, String.format("%08d",targetId));
				JSONObject result4 = dao.createSqlMSTSRCCD(userId, sendBtnid, dataArraySRCCD, baseTblType, SqlType.INS);
			}

			// --- 05.添加物
			JSONObject result5D= dao.createSqlMSTTENKABUTSU(userId, sendBtnid, dataArrayDelTENKABUTSU, baseTblType, SqlType.DEL);
			if(dataArrayTENKABUTSU.size() > 0){
				dataArrayTENKABUTSU = dao.addSHNCD(dataArrayTENKABUTSU, isNew, String.format("%08d",targetId));
				JSONObject result5 = dao.createSqlMSTTENKABUTSU(userId, sendBtnid, dataArrayTENKABUTSU, baseTblType, SqlType.INS);
			}

			// --- 06.品揃グループ
			JSONObject result6D= dao.createSqlMSTSHINAGP(userId, sendBtnid, dataArrayDel, baseTblType, SqlType.DEL);
			if(dataArrayTENGP3.size() > 0){
				dataArrayTENGP3 = dao.addSHNCD(dataArrayTENGP3, isNew, String.format("%08d",targetId));
				JSONObject result6 = dao.createSqlMSTSHINAGP(userId, sendBtnid, dataArrayTENGP3, baseTblType, SqlType.INS);
			}

			// --- 09.店別異部門
			JSONObject result9D= dao.createSqlMSTSHNTENBMN(userId, sendBtnid, dataArrayDelMSTSHNTENBMN, baseTblType, SqlType.DEL);
			if(dataArrayTENGP4.size() > 0){
				dataArrayTENGP4 = dao.addSHNCD(dataArrayTENGP4, isNew, String.format("%08d",targetId));
				JSONObject result9 = dao.createSqlMSTSHNTENBMN(userId, sendBtnid, dataArrayTENGP4, baseTblType, SqlType.INS);
			}
		}

		if(!isErr && !isChangeY1Del){
			// ************ 関連テーブル処理 ***********
			// --- 07.メーカー
			String makercd = data.optString("F72");
			if(StringUtils.isNotEmpty(makercd) && dataArraySRCCD.size() > 0){
				// メーカーコード存在チェック
				if(!dao.checkMstExist(DefineReport.InpText.MAKERCD.getObj(), makercd)){
					String jancd = dataArraySRCCD.optJSONObject(0).optString("F2");
					JSONObject dataMAKER = dao.createJSONObject(new String[]{"F1", "F4"}, new String[]{makercd, jancd});
					JSONObject result7 = dao.createSqlMSTMAKER(userId, sendBtnid, dataMAKER, TblType.SEI, SqlType.INS);
				}
			}

		}

		return targetId;
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
	public JSONObject createSqlCSVSHNHEAD(String userId, String btnId, String seq, String commentkn) {
		JSONObject result = new JSONObject();
		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();

		LinkedList<String> valList = new LinkedList<String>();
		valList.add("");	//

		String values = "";
		values += " " + seq;				// SEQ
		values += ",'"+ userId+ "'";			// OPERATOR
		values += ",current timestamp";		// INPUT_DATE
		values += ",cast(? as varchar("+MessageUtility.getDefByteLen(commentkn)+"))";
		prmData.add(commentkn);

		StringBuffer sbSQL;
		sbSQL = new StringBuffer();
		sbSQL.append("insert into INAWS.CSVSHNHEAD(SEQ, OPERATOR, INPUT_DATE, COMMENTKN)");
		sbSQL.append("values("+values+")");
		if (DefineReport.ID_DEBUG_MODE)	System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		this.sqlList.add(sbSQL.toString());
		this.prmList.add(prmData);
		this.lblList.add("CSV取込トラン_商品ヘッダ");
		return result;
	}


	/**
	 * 商品マスタ情報取得処理
	 *
	 * @param isSei	正/予約
	 * @param isNew	新規/更新
	 * @param data	CSV抜粋データ
	 *
	 * @throws Exception
	 */
	public JSONArray selectMSTSHN(boolean isSeiRep, boolean emptyShn, JSONObject data) {
		if(data.isEmpty()){
			return new JSONArray();
		}
		ArrayList<String> prmData = new ArrayList<String>();

		// 共通SQL
		StringBuffer sbSQLIn = new StringBuffer();
		sbSQLIn.append(" select ");

		String teianVal = StringUtils.strip(data.optString("TITKNNO"));
		if(StringUtils.isNotEmpty(teianVal)){
			prmData.add(teianVal);
			sbSQLIn.append("cast(? as INTEGER) as TITKNNO");
		} else {
			sbSQLIn.append("cast(null as INTEGER) as TITKNNO");
		}

		String statusVal = StringUtils.strip(data.optString("TITSTCD"));
		if(StringUtils.isNotEmpty(statusVal)){
			prmData.add(statusVal);
			sbSQLIn.append(",cast(? as INTEGER) as TITSTCD");
		} else {
			sbSQLIn.append(",cast(null as INTEGER) as TITSTCD");
		}

		for(MSTSHNLayout itm :MSTSHNLayout.values()){
			sbSQLIn.append(",");
			//if(itm.getNo() > 1){ sbSQLIn.append(","); }
			if(data.containsKey(itm.getCol())){
				String value = StringUtils.strip(data.optString(itm.getCol()));
				if(StringUtils.isNotEmpty(value)){
					prmData.add(value);
					sbSQLIn.append("cast(? as varchar("+MessageUtility.getDefByteLen(value)+")) as "+itm.getCol());
				}else{
					sbSQLIn.append("cast(null as " + itm.getTyp() + ") as "+itm.getCol());
				}
			}else{
				if(itm.getCol().equals(MSTSHNLayout.SHNCD.getCol())) {
					sbSQLIn.append("cast(null as varchar) as "+itm.getCol());
				} else {
					sbSQLIn.append("null as "+itm.getCol());
				}
			}
		}
		sbSQLIn.append(" from SYSIBM.SYSDUMMY1");

		// 基本Select文
		StringBuffer sbSQL = new StringBuffer();

		String szTable = "INAWS.PIMTIT";			// 予約の時は予約を削除し、登録を行うので、参照は正となる
		String szWhere = "T.SHNCD = RE.SHNCD";

		sbSQL.append(" select ");
		sbSQL.append("nvl(RE.TITKNNO, INTEGER(T.TITKNNO)) as TITKNNO");
		sbSQL.append(",nvl(RE.TITSTCD, INTEGER(T.TITSTCD)) as TITSTCD");
		for(MSTSHNLayout itm :MSTSHNLayout.values()){
			//if(!ArrayUtils.contains(targetCol, itm.getCol())){continue;}
			sbSQL.append(",");
			//if(itm.getNo() > 1){ sbSQL.append(","); }
			if(itm.isText()){
				sbSQL.append("trim(nvl(RE."+itm.getCol()+", varchar(T."+itm.getCol()+"))) as "+itm.getId());
			}else{
				sbSQL.append("nvl(RE."+itm.getCol()+", varchar(T."+itm.getCol()+")) as "+itm.getId());
			}
		}
		sbSQL.append(" from (" + sbSQLIn.toString() + ") RE");
		sbSQL.append(" left outer join " + szTable  + " T on "+ szWhere);
		if (DefineReport.ID_DEBUG_MODE)	System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		ItemList iL = new ItemList();
		@SuppressWarnings("static-access")
		JSONArray dataArray = iL.selectJSONArray(sbSQL.toString(), prmData, Defines.STR_JNDI_DS);

		return dataArray;
	}

	/**
	 * 子テーブルマスタ共通SQL作成処理
	 *
	 * @param isSeiRep	正/予約
	 * @param isNew	新規/更新
	 * @param dataArray	CSV抜粋データ
	 * @param layout	マスタレイアウト
	 * @return
	 *
	 * @throws Exception
	 */
	private String setSelectCommandMST(boolean isSeiRep, boolean isNew, JSONArray dataArray, dao.Reportx247Dao.MSTLayout[] layouts, ArrayList<String> prmData) {
		String values = "", names = "", rows = "";
		String[] nullSetCol = new String[]{"UPDDT", "ADDDT"};
		for(int j=0; j < dataArray.size(); j++){
			values = "";
			names = "";
			for(dao.Reportx247Dao.MSTLayout itm : layouts){
				String col = itm.getCol();
				String val = StringUtils.strip(dataArray.optJSONObject(j).optString(col));
				if(dataArray.optJSONObject(j).containsKey(itm.getCol())&&!ArrayUtils.contains(nullSetCol, itm.getCol())){
					if(StringUtils.isNotEmpty(val)){
						prmData.add(val);
						values += ", cast(? as varchar("+MessageUtility.getDefByteLen(val)+"))";
					}else{
						values += ",cast(null as " + itm.getTyp() + ")";
					}
				}else{
					values += ", null";
				}
				names  += ", "+col;
			}
			for(CSVCMNLayout itm : CSVCMNLayout.values()){
				String col = itm.getCol();
				String val = dataArray.optJSONObject(j).optString(col);
				values += ", "+val+"";
				names  += ", "+col;
			}
			rows += ",("+StringUtils.removeStart(values, ",")+")";
		}
		rows = StringUtils.removeStart(rows, ",");
		names = StringUtils.removeStart(names, ",");

		// 基本Select文
		StringBuffer sbSQL = new StringBuffer();
		// マスタ情報を参照し、入力値がNULLの場合参照する
		sbSQL.append(" select ");
		for(dao.Reportx247Dao.MSTLayout itm :layouts){
			if(itm.getNo() > 1){ sbSQL.append(","); }

			if(isNew){
				if(itm.isText()){
					sbSQL.append("trim(RE."+itm.getCol()+") as "+itm.getId());
				}else{
					sbSQL.append("RE."+itm.getCol()+" as "+itm.getId());
				}
			}else{
				if(itm.isText()){
					sbSQL.append("trim(nvl(RE."+itm.getCol()+", varchar(T."+itm.getCol()+"))) as "+itm.getId());
				}else{
					sbSQL.append("nvl(RE."+itm.getCol()+", varchar(T."+itm.getCol()+")) as "+itm.getId());
				}
			}
		}
		for(CSVCMNLayout itm : CSVCMNLayout.values()){
			sbSQL.append(",RE."+itm.getCol() + " as "+itm.getId2(layouts.length));
		}
		sbSQL.append(" from (values"+rows+") as RE("+names+")");
		if(!isNew){
			// 新規登録時は既存値が存在しないので、テーブルを参照しない。
			sbSQL.append(" left outer join @T T on @W");
		}

		return sbSQL.toString();
	}

	/**
	 * ソースコードマスタ情報取得処理
	 *
	 * @param isSeiRep	正/予約
	 * @param isNew	新規/更新
	 * @param data	CSV抜粋データ
	 *
	 * @throws Exception
	 */
	private JSONArray selectMSTSRC(boolean isSeiRep, boolean isNew, JSONArray dataArray) {
		if(dataArray.size()==0){
			return new JSONArray();
		}

		String szTable = "INAWS.PIMTISRCCD";
		String szWhere = "T.SHNCD = RE.SHNCD";
		szWhere += " and T.SRCCD = RE.SRCCD ";

		ArrayList<String> prmData = new ArrayList<>();
		String sqlCommand = this.setSelectCommandMST(isSeiRep, isNew, dataArray, MSTSRCCDLayout.values(), prmData);
		sqlCommand = sqlCommand.replace("@T", szTable).replace("@W", szWhere);
		if (DefineReport.ID_DEBUG_MODE)	System.out.println(this.getClass().getName()+ ":" + sqlCommand);

		ItemList iL = new ItemList();
		@SuppressWarnings("static-access")
		JSONArray rtnArray = iL.selectJSONArray(sqlCommand, prmData, Defines.STR_JNDI_DS);
		return rtnArray;
	}


	/**
	 * 仕入グループ商品マスタ情報取得処理
	 *
	 * @param isSeiRep	正/予約
	 * @param isNew	新規/更新
	 * @param data	CSV抜粋データ
	 *
	 * @throws Exception
	 */
	private JSONArray selectMSTSIRGPSHN(boolean isSeiRep, boolean isNew, JSONArray dataArray) {
		if(dataArray.size()==0){
			return new JSONArray();
		}

		String szTable = "INAWS.PIMTISIRGPSHN";
		String szWhere = "T.SHNCD = RE.SHNCD";
		szWhere += " and T.TENGPCD = RE.TENGPCD";

		ArrayList<String> prmData = new ArrayList<>();
		String sqlCommand = this.setSelectCommandMST(isSeiRep, isNew, dataArray, MSTSIRGPSHNLayout.values(), prmData);
		sqlCommand = sqlCommand.replace("@T", szTable).replace("@W", szWhere);
		if (DefineReport.ID_DEBUG_MODE)	System.out.println(this.getClass().getName()+ ":" + sqlCommand);

		ItemList iL = new ItemList();
		@SuppressWarnings("static-access")
		JSONArray rtnArray = iL.selectJSONArray(sqlCommand, prmData, Defines.STR_JNDI_DS);
		return rtnArray;
	}

	/**
	 * 売価コントロールマスタ情報取得処理
	 *
	 * @param isSeiRep	正/予約
	 * @param isNew	新規/更新
	 * @param data	CSV抜粋データ
	 *
	 * @throws Exception
	 */
	private JSONArray selectMSTBAIKACTL(boolean isSeiRep, boolean isNew, JSONArray dataArray) {
		if(dataArray.size()==0){
			return new JSONArray();
		}

		String szTable = "INAWS.PIMTIBAIKACTL";
		String szWhere = "T.SHNCD = RE.SHNCD";
		szWhere += " and T.TENGPCD = RE.TENGPCD";

		ArrayList<String> prmData = new ArrayList<>();
		String sqlCommand = this.setSelectCommandMST(isSeiRep, isNew, dataArray, MSTBAIKACTLLayout.values(), prmData);
		sqlCommand = sqlCommand.replace("@T", szTable).replace("@W", szWhere);
		if (DefineReport.ID_DEBUG_MODE)	System.out.println(this.getClass().getName()+ ":" + sqlCommand);

		ItemList iL = new ItemList();
		@SuppressWarnings("static-access")
		JSONArray rtnArray = iL.selectJSONArray(sqlCommand, prmData, Defines.STR_JNDI_DS);
		return rtnArray;
	}


	/**
	 * 品揃グループマスタ情報取得処理
	 *
	 * @param isSeiRep	正/予約
	 * @param isNew	新規/更新
	 * @param data	CSV抜粋データ
	 *
	 * @throws Exception
	 */
	private JSONArray selectMSTSHINAGP(boolean isSeiRep, boolean isNew, JSONArray dataArray) {
		if(dataArray.size()==0){
			return new JSONArray();
		}

		String szTable = "INAWS.PIMTISHINAGP";
		String szWhere = "T.SHNCD = RE.SHNCD";
		szWhere += " and T.TENGPCD = RE.TENGPCD";

		ArrayList<String> prmData = new ArrayList<>();
		String sqlCommand = this.setSelectCommandMST(isSeiRep, isNew, dataArray, MSTSHINAGPLayout.values(), prmData);
		sqlCommand = sqlCommand.replace("@T", szTable).replace("@W", szWhere);
		if (DefineReport.ID_DEBUG_MODE)	System.out.println(this.getClass().getName()+ ":" + sqlCommand);

		ItemList iL = new ItemList();
		@SuppressWarnings("static-access")
		JSONArray rtnArray = iL.selectJSONArray(sqlCommand, prmData, Defines.STR_JNDI_DS);
		return rtnArray;
	}


	/**
	 * 添加物マスタ情報取得処理
	 *
	 * @param isSeiRep	正/予約
	 * @param isNew	新規/更新
	 * @param data	CSV抜粋データ
	 *
	 * @throws Exception
	 */
	private JSONArray selectMSTTENKABUTSU(boolean isSeiRep, boolean isNew, JSONArray dataArray) {
		if(dataArray.size()==0){
			return new JSONArray();
		}

		String szTable = "INAWS.PIMTITENKABUTSU";
		String szWhere = "T.SHNCD = RE.SHNCD";
		szWhere += " and T.TENKABKBN = RE.TENKABKBN and T.TENKABCD = RE.TENKABCD";

		ArrayList<String> prmData = new ArrayList<>();
		String sqlCommand = this.setSelectCommandMST(isSeiRep, isNew, dataArray, MSTTENKABUTSULayout.values(), prmData);
		sqlCommand = sqlCommand.replace("@T", szTable).replace("@W", szWhere);
		if (DefineReport.ID_DEBUG_MODE)	System.out.println(this.getClass().getName()+ ":" + sqlCommand);

		ItemList iL = new ItemList();
		@SuppressWarnings("static-access")
		JSONArray rtnArray = iL.selectJSONArray(sqlCommand, prmData, Defines.STR_JNDI_DS);
		return rtnArray;
	}

	/**
	 * 店別異部門管理情報取得処理
	 *
	 * @param isSeiRep	正/予約
	 * @param isNew	新規/更新
	 * @param data	CSV抜粋データ
	 *
	 * @throws Exception
	 */
	private JSONArray selectMSTSHNTENBMN(boolean isSeiRep, boolean isNew, JSONArray dataArray) {
		if(dataArray.size()==0){
			return new JSONArray();
		}

		String szTable = "INAMS.MSTSHNTENBMN";
		String szWhere = "T.SHNCD = RE.SHNCD";
		szWhere += " and T.TENSHNCD = RE.TENSHNCD and T.TENGPCD = RE.TENGPCD";

		ArrayList<String> prmData = new ArrayList<>();
		String sqlCommand = this.setSelectCommandMST(isSeiRep, isNew, dataArray, MSTSHNTENBMNLayout.values(), prmData);
		sqlCommand = sqlCommand.replace("@T", szTable).replace("@W", szWhere);
		if (DefineReport.ID_DEBUG_MODE)	System.out.println(this.getClass().getName()+ ":" + sqlCommand);

		ItemList iL = new ItemList();
		@SuppressWarnings("static-access")
		JSONArray rtnArray = iL.selectJSONArray(sqlCommand, prmData, Defines.STR_JNDI_DS);
		return rtnArray;
	}

	/**
	 * 子テーブルマスタ共通SQL作成処理
	 *
	 * @param isNew	新規/更新
	 * @param dataArray	CSV抜粋データ
	 * @param layout	マスタレイアウト
	 * @return
	 *
	 * @throws Exception
	 */
	public JSONArray selectCompData(JSONObject ShnData, JSONArray dataArray, String tbl, String keyCol, String groupCol, dao.Reportx247Dao.MSTLayout[] layouts) {

		if(dataArray.size()==0){
			return new JSONArray();
		}

		ArrayList<String> prmData = new ArrayList<>();
		String szTable = "INAMS."+tbl;
		if(!StringUtils.equals(RefTable.MSTSHNTENBMN.getId(), tbl)){
			szTable = "INAWS."+tbl;
		}

		String szWhere = "T.SHNCD = RE.SHNCD";
		String szYoyakudt	 = ShnData.optString(MSTSHNLayout.YOYAKUDT.getCol());

		// 更新を行わない子テーブルデータ(CSVにNullで設定されている場合)を追加する。
		dataArray = this.addNotUpdateData(dataArray, tbl, groupCol,layouts,szYoyakudt);

		// 結合キーの追加
		if(StringUtils.equals(RefTable.PIMTISRCCD.getId(), tbl)){
			// ソースコードマスタ情報取得処理
			szWhere += " and T.SRCCD = RE.SRCCD ";

		}else if(StringUtils.equals(RefTable.PIMTISIRGPSHN.getId(), tbl)){
			// 仕入グループ商品マスタ
			szWhere += " and T.TENGPCD = RE.TENGPCD";

		}else if(StringUtils.equals(RefTable.PIMTIBAIKACTL.getId(), tbl)){
			// 売価コントロールマスタ情報取得処理
			szWhere += " and T.TENGPCD = RE.TENGPCD";

		}else if(StringUtils.equals(RefTable.PIMTISHINAGP.getId(), tbl)){
			// 品揃グループマスタ情報取得処理
			szWhere += " and T.TENGPCD = RE.TENGPCD";

		}else if(StringUtils.equals(RefTable.PIMTITENKABUTSU.getId(), tbl)){
			// 添加物マスタ情報取得処理
			szWhere += " and T.TENKABKBN = RE.TENKABKBN and T.TENKABCD = RE.TENKABCD";

		}else if(StringUtils.equals(RefTable.MSTSHNTENBMN.getId(), tbl)){
			// 店別異部門管理情報取得処理
			szWhere += " and T.TENSHNCD = RE.TENSHNCD and T.TENGPCD = RE.TENGPCD";
		}

		String values = "", names = "", selNames = "" , groupNames = "", orderNames = "", rows = "";
		String[] nullSetCol = new String[]{"UPDDT", "ADDDT"};
		boolean count = false;

		if (!StringUtils.isEmpty(groupCol)) {
			orderNames += ", MIN("+groupCol+")";
		}
		orderNames += ", MIN("+CSVCMNLayout.INPUTEDANO.getCol()+")";

		for(int j=0; j < dataArray.size(); j++){
			values = "";
			names = "";
			selNames = "";
			groupNames = "";
			for(dao.Reportx247Dao.MSTLayout itm : layouts){
				String col = itm.getCol();
				String val = StringUtils.strip(dataArray.optJSONObject(j).optString(col));
				if(dataArray.optJSONObject(j).containsKey(itm.getCol())&&!ArrayUtils.contains(nullSetCol, itm.getCol())){
					if(StringUtils.isNotEmpty(val)){
						prmData.add(val);
						values += ", cast(? as varchar("+MessageUtility.getDefByteLen(val)+"))";
						if (keyCol.equals(itm.getCol())) {
							count = true;
						}
					}else{

						if (itm.getTyp().equals("INTEGER") || itm.getTyp().equals("SMALLINT")) {
							values += ",cast(cast(null as " + itm.getTyp() + ") as varchar (1))";
						} else {
							values += ",cast(null as " + itm.getTyp() + ")";
						}
					}
				}else{
					values += ", null";
				}
				names  += ", "+col;
				if (!StringUtils.isEmpty(groupCol) && col.equals(groupCol)) {
					selNames += ", varchar (ROW_NUMBER() over (order by @O)) as " + col;
				} else {
					selNames += ", "+col;
					groupNames += ", "+col;
				}

			}
			for(CSVCMNLayout itm : CSVCMNLayout.values()){
				String col = itm.getCol();
				String val = dataArray.optJSONObject(j).optString(col);
				values += ", "+val+"";
				names  += ", "+col;
				if (col.equals(CSVCMNLayout.INPUTEDANO.getCol())) {
					selNames += ", varchar (ROW_NUMBER() over (order by @O)) as " + col;
				} else {
					selNames += ", "+col;
					groupNames += ", "+col;
				}
			}
			rows += ",("+StringUtils.removeStart(values, ",")+")";
		}
		rows = StringUtils.removeStart(rows, ",");
		names = StringUtils.removeStart(names, ",");
		selNames = StringUtils.removeStart(selNames, ",");
		groupNames = StringUtils.removeStart(groupNames, ",");
		orderNames = StringUtils.removeStart(orderNames, ",");

		// 基本Select文
		StringBuffer sbSQL = new StringBuffer();
		// マスタ情報を参照し、入力値がNULLの場合参照する
		sbSQL.append(" select * from (select ");
		for(dao.Reportx247Dao.MSTLayout itm :layouts){
			if(itm.getNo() > 1){ sbSQL.append(","); }
			if(itm.isText()){
				sbSQL.append("trim(nvl(RE."+itm.getCol()+", varchar(T."+itm.getCol()+"))) as "+itm.getCol());
			}else{
				sbSQL.append("nvl(RE."+itm.getCol()+", varchar(T."+itm.getCol()+")) as "+itm.getCol());
			}
		}
		for(CSVCMNLayout itm : CSVCMNLayout.values()){
			sbSQL.append(",RE."+itm.getCol() + " as "+itm.getCol());
		}
		sbSQL.append(" from (SELECT "+selNames+" FROM (values"+rows+") as RE("+names+") ");
		if (count) {
			sbSQL.append(" where @K IS NOT NULL ");
		}
		sbSQL.append(" GROUP BY "+groupNames+") RE left outer join @T T on @W) ");


		String sqlCommand = sbSQL.toString();
		sqlCommand = sqlCommand.replace("@T", szTable).replace("@W", szWhere).replace("@K", keyCol).replace("@O", orderNames);
		if (DefineReport.ID_DEBUG_MODE)	System.out.println(this.getClass().getName()+ ":" + sqlCommand);

		ItemList iL = new ItemList();
		@SuppressWarnings("static-access")
		JSONArray rtnArray = iL.selectJSONArray(sqlCommand, prmData, Defines.STR_JNDI_DS);

		if (!count && rtnArray.size() >= 1 && StringUtils.isEmpty(rtnArray.getJSONObject(0).optString(keyCol))) {
			return new JSONArray();
		}
		return rtnArray;
	}


	/**
	 * 子テーブルマスタ共通SQL作成処理
	 *
	 * @param isNew	新規/更新
	 * @param dataArray	CSV抜粋データ
	 * @param layout	マスタレイアウト
	 * @return
	 *
	 * @throws Exception
	 */
	public JSONArray addNotUpdateData(JSONArray dataArray, String tbl, String groupCol, dao.Reportx247Dao.MSTLayout[] layouts, String yoyakudt) {

		ArrayList<String> prmData = new ArrayList<>();
		StringBuffer sbSQL = new StringBuffer();
		ArrayList<String> pkeyCol  = new ArrayList<>();
		ArrayList<String> pkeyVal  = new ArrayList<>();

		String szShncd = "";
		String szCol = "SHNCD, ";
		String szTable = "INAMS."+tbl;
		if(!StringUtils.equals(RefTable.MSTSHNTENBMN.getId(), tbl)){
			szTable = "INAWS."+tbl;
		}
		String szWhere = "SHNCD = ?";
		String szNotPattarn = "";
		String szOrder = "";

		String SEQ			 = "";
		String INPUTNO		 = "";
		String INPUTEDANO	 = "";
		String SEQNO		 = "";

		JSONObject lastData =  dataArray.optJSONObject((dataArray.size() -1));	// 子データ配列の最終行のデータ
		SEQ			 = lastData.optString(CSVCMNLayout.SEQ.getCol());			// 最終行データのSEQ(データ配列内で同値)
		INPUTNO		 = lastData.optString(CSVCMNLayout.INPUTNO.getCol());		// 最終行データのINPUTNO(データ配列内で同値)
		INPUTEDANO	 = lastData.optString(CSVCMNLayout.INPUTEDANO.getCol());	// 最終行データのINPUTEDANO

		if(StringUtils.equals(RefTable.PIMTISIRGPSHN.getId(), tbl)){
			// 仕入グループ商品マスタ
			pkeyCol.add("TENGPCD");

		}else if(StringUtils.equals(RefTable.PIMTIBAIKACTL.getId(), tbl)){
			// 売価コントロールマスタ情報取得処理
			pkeyCol.add("TENGPCD");

		}else if(StringUtils.equals(RefTable.PIMTISHINAGP.getId(), tbl)){
			// 品揃グループマスタ情報取得処理
			pkeyCol.add("TENGPCD");

		}else if(StringUtils.equals(RefTable.PIMTITENKABUTSU.getId(), tbl)){
			// 添加物マスタ情報取得処理
			pkeyCol.add("TENKABKBN");
			pkeyCol.add("TENKABCD");

		}else if(StringUtils.equals(RefTable.MSTSHNTENBMN.getId(), tbl)){
			// 店別異部門管理情報取得処理
			pkeyCol.add("TENSHNCD");
			pkeyCol.add("TENGPCD");
		}

		if(pkeyCol.size() == 0){
			if(StringUtils.equals(RefTable.PIMTISRCCD.getId(), tbl)){
				// ソースコードマスタ情報取得処理
				pkeyCol.add("SRCCD");
				pkeyCol.add("SEQNO");
				SEQNO = lastData.optString(MSTSRCCDLayout.SEQNO.getCol());	// 入力番号
				szOrder += "order by SEQNO";		// ソースコードは既存データの入力順番に取得して配列に保持しないと、登録時のRowNumber処理で誤った順番に上書きされてしまう為、ソート順を指定する。

				String str = "";
				for(int j=0; j < pkeyCol.size(); j++){
					String col = pkeyCol.get(j);

					if(j != 0){
						szCol += ", ";
						str += " and ";
					}
					szCol += col;
					str += " not("+col+"=?) ";
				}
				szNotPattarn = "and (" +str+ ")";
			} else {
				// 主キー設定が存在しない場合は、既存値検索を行えない為、デフォルト値を返す。
				return dataArray;
			}
		}else{
			String str = "";
			for(int j=0; j < pkeyCol.size(); j++){
				String col = pkeyCol.get(j);

				if(j != 0){
					szCol += ", ";
					str += " and ";
				}
				szCol += col;
				str += col + " = ? ";
			}
			szNotPattarn = "and not (" +str+ ")";
		}

		for(int j=0; j < dataArray.size(); j++){
			JSONObject data =  dataArray.optJSONObject(j);

			// 商品コードの設定
			if(StringUtils.isEmpty(szShncd) && StringUtils.isNotEmpty(MSTSHNLayout.SHNCD.getCol())){
				szShncd = data.optString(MSTSHNLayout.SHNCD.getCol());
			}
		}

		if(StringUtils.isEmpty(szShncd)){
			// 商品コード未入力の場合は、既存値検索を行えない為、デフォルト値を返す。
			return dataArray;

		}else{
			prmData.add(szShncd);
			for(int j=0; j < dataArray.size(); j++){
				JSONObject data =  dataArray.optJSONObject(j);

				int existCol = 0;
				ArrayList<String> prmDataBef = new ArrayList<>();
				for(String col : pkeyCol){
					if(StringUtils.isNotEmpty(data.optString(col))){
						prmDataBef.add(data.optString(col));
						existCol += 1;
					}
				}
				if(existCol == pkeyCol.size() && prmDataBef.size() > 0){
					// キー分のデータを追加出来た場合
					szWhere += szNotPattarn;
					for(String val : prmDataBef){
						prmData.add(val);
					}
				}
			}
		}

		// 基本Select文
		sbSQL = new StringBuffer();
		// マスタ情報を参照し、入力値がNULLの場合参照する
		sbSQL.append(" select ");
		for(dao.Reportx247Dao.MSTLayout itm :layouts){
			String col = itm.getCol();
			String val = "T."+itm.getCol();
			if(StringUtils.equals(itm.getCol(), Reportx247Dao.MSTSHNLayout.YOYAKUDT.getCol())){
				if(StringUtils.isNotEmpty(yoyakudt) && !StringUtils.equals("0", yoyakudt)){
					val = yoyakudt;
				}
			}
			if(itm.getNo() > 1){ sbSQL.append(","); }
			if(itm.isText()){
				sbSQL.append("trim("+val+") as "+col);
			}else{
				sbSQL.append(val+" as "+col);
			}
		}
		sbSQL.append(" from @T as T where @W");
		sbSQL.append(szOrder);

		String sqlCommand = sbSQL.toString();
		sqlCommand = sqlCommand.replace("@C", szCol).replace("@T", szTable).replace("@W", szWhere);
		if (DefineReport.ID_DEBUG_MODE)	System.out.println(this.getClass().getName()+ ":" + sqlCommand);

		ItemList iL = new ItemList();
		@SuppressWarnings("static-access")
		JSONArray rtnArray = iL.selectJSONArray(sqlCommand, prmData, Defines.STR_JNDI_DS);

		if(rtnArray.size() > 0){
			for(int j=0; j < rtnArray.size(); j++){
				JSONObject data =  rtnArray.optJSONObject(j);
				data.put(CSVCMNLayout.SEQ.getCol(), SEQ);
				data.put(CSVCMNLayout.INPUTNO.getCol(), INPUTNO);
				data.put(CSVCMNLayout.INPUTEDANO.getCol(), Integer.parseInt(INPUTEDANO) + (j+1));
				if(StringUtils.isNotEmpty(SEQNO)){
					data.put(MSTSRCCDLayout.SEQNO.getCol(), Integer.parseInt(SEQNO) + (j+1));
				}
				dataArray.add(data);
			}
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
		String sqlColCommand = "VALUES NEXTVAL FOR INAMS.SEQ003";
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
		String sqlColCommand = "VALUES NEXTVAL FOR INAMS.SEQ002";
		@SuppressWarnings("static-access")
		JSONArray array = iL.selectJSONArray(sqlColCommand, null, Defines.STR_JNDI_DS);
		String value = "";
		if(array.size() > 0){
			value = array.optJSONObject(0).optString("1");
		}
		return value;
	}

	/**  Fileレイアウト */
	public enum FileLayout {
		/** 更新区分 */
		UPDKBN(1,"更新区分",RefTable.OTHER,"UPDKBN"),
		/** 提案No */
		TEIAN(2,"提案No",RefTable.MSTSHN,"TITKNNO"),
		/** 状態 */
		STATUS(3,"状態",RefTable.MSTSHN,"TITSTCD"),
		/** 定計区分 */
		TEIKEIKBN(4,"定計区分",RefTable.MSTSHN,"TEIKEIKBN"),
		/** ソース区分_1 */
		SOURCEKBN1(5,"ソース区分_1",RefTable.MSTSRCCD,"SOURCEKBN"),
		/** ソースコード_1 */
		SRCCD1(6,"ソースコード_1",RefTable.MSTSRCCD,"SRCCD"),
		/** 標準仕入先コード */
		SSIRCD(7,"標準仕入先コード",RefTable.MSTSHN,"SSIRCD"),
		/** 定貫不定貫区分 */
		TEIKANKBN(8,"定貫不定貫区分",RefTable.MSTSHN,"TEIKANKBN"),
		/** 標準分類コード_部門 */
		BMNCD(9,"標準分類コード_部門",RefTable.MSTSHN,"BMNCD"),
		/** 標準分類コード_大 */
		DAICD(10,"標準分類コード_大",RefTable.MSTSHN,"DAICD"),
		/** 標準分類コード_中 */
		CHUCD(11,"標準分類コード_中",RefTable.MSTSHN,"CHUCD"),
		/** 標準分類コード_小 */
		SHOCD(12,"標準分類コード_小",RefTable.MSTSHN,"SHOCD"),
		/** 標準分類コード_小小 */
		SSHOCD(13,"標準分類コード_小小",RefTable.MSTSHN,"SSHOCD"),
		/** ＰＣ区分 */
		PCKBN(14,"ＰＣ区分",RefTable.MSTSHN,"PCKBN"),
		/** 商品種類 */
		SHNKBN(15,"商品種類",RefTable.MSTSHN,"SHNKBN"),
		/** 商品名（カナ） */
		SHNAN(16,"商品名（カナ）",RefTable.MSTSHN,"SHNAN"),
		/** 商品名（漢字） */
		SHNKN(17,"商品名（漢字）",RefTable.MSTSHN,"SHNKN"),
		/** レシート名（カナ） */
		RECEIPTAN(18,"レシート名（カナ）",RefTable.MSTSHN,"RECEIPTAN"),
		/** レシート名（漢字） */
		RECEIPTKN(19,"レシート名（漢字）",RefTable.MSTSHN,"RECEIPTKN"),
		/** プライスカード商品名称（漢字） */
		PCARDKN(20,"プライスカード商品名称（漢字）",RefTable.MSTSHN,"PCARDKN"),
		/** 商品コメント・セールスコピー（漢字） */
		SALESCOMKN(21,"商品コメント・セールスコピー（漢字）",RefTable.MSTSHN,"SALESCOMKN"),
		/** ＰＯＰ名称（漢字） */
		POPKN(22,"ＰＯＰ名称（漢字）",RefTable.MSTSHN,"POPKN"),
		/** 規格 */
		KIKKN(23,"規格",RefTable.MSTSHN,"KIKKN"),
		/** レギュラー情報_取扱フラグ */
		RG_ATSUKFLG(24,"レギュラー情報_取扱フラグ",RefTable.MSTSHN,"RG_ATSUKFLG"),
		/** レギュラー情報_原価 */
		RG_GENKAAM(25,"レギュラー情報_原価",RefTable.MSTSHN,"RG_GENKAAM"),
		/** レギュラー情報_売価 */
		RG_BAIKAAM(26,"レギュラー情報_売価",RefTable.MSTSHN,"RG_BAIKAAM"),
		/** レギュラー情報_店入数 */
		RG_IRISU(27,"レギュラー情報_店入数",RefTable.MSTSHN,"RG_IRISU"),
		/** レギュラー情報_一括伝票ﾌﾗｸﾞ */
		RG_IDENFLG(28,"レギュラー情報_一括伝票ﾌﾗｸﾞ",RefTable.MSTSHN,"RG_IDENFLG"),
		/** レギュラー情報_ワッペン */
		RG_WAPNFLG(29,"レギュラー情報_ワッペン",RefTable.MSTSHN,"RG_WAPNFLG"),
		/** 販促情報_取扱フラグ */
		HS_ATSUKFLG(30,"販促情報_取扱フラグ",RefTable.MSTSHN,"HS_ATSUKFLG"),
		/** 販促情報_原価 */
		HS_GENKAAM(31,"販促情報_原価",RefTable.MSTSHN,"HS_GENKAAM"),
		/** 販促情報_売価 */
		HS_BAIKAAM(32,"販促情報_売価",RefTable.MSTSHN,"HS_BAIKAAM"),
		/** 販促情報_店入数 */
		HS_IRISU(33,"販促情報_店入数",RefTable.MSTSHN,"HS_IRISU"),
		/** 販促情報_ワッペン */
		HS_WAPNFLG(34,"販促情報_ワッペン",RefTable.MSTSHN,"HS_WAPNFLG"),
		/** 販促情報_特売ワッペン */
		HP_SWAPNFLG(35,"販促情報_特売ワッペン",RefTable.MSTSHN,"HP_SWAPNFLG"),
		/** 便区分 */
		BINKBN(36,"便区分",RefTable.MSTSHN,"BINKBN"),
		/** 締め回数 */
		SIMEKAISU(37,"締め回数",RefTable.MSTSHN,"SIMEKAISU"),
		/** 小物区分 */
		KOMONOKBM(38,"小物区分",RefTable.MSTSHN,"KOMONOKBM"),
		/** 仕分区分 */
		SIWAKEKBN(39,"仕分区分",RefTable.MSTSHN,"SIWAKEKBN"),
		/** 棚卸区分 */
		TANAOROKBN(40,"棚卸区分",RefTable.MSTSHN,"TANAOROKBN"),
		/** 期間 */
		KIKANKBN(41,"期間",RefTable.MSTSHN,"KIKANKBN"),
		/** ＯＤＳ_賞味期限_春 */
		ODS_HARUSU(42,"ＯＤＳ_賞味期限_春",RefTable.MSTSHN,"ODS_HARUSU"),
		/** ＯＤＳ_賞味期限_夏 */
		ODS_NATSUSU(43,"ＯＤＳ_賞味期限_夏",RefTable.MSTSHN,"ODS_NATSUSU"),
		/** ＯＤＳ_賞味期限_秋 */
		ODS_AKISU(44,"ＯＤＳ_賞味期限_秋",RefTable.MSTSHN,"ODS_AKISU"),
		/** ＯＤＳ_賞味期限_冬 */
		ODS_FUYUSU(45,"ＯＤＳ_賞味期限_冬",RefTable.MSTSHN,"ODS_FUYUSU"),
		/** ＯＤＳ_入荷期限 */
		ODS_NYUKASU(46,"ＯＤＳ_入荷期限",RefTable.MSTSHN,"ODS_NYUKASU"),
		/** ＯＤＳ_値引開始 */
		ODS_NEBIKISU(47,"ＯＤＳ_値引開始",RefTable.MSTSHN,"ODS_NEBIKISU"),
		/** 販促情報_スポット最低発注数 */
		HS_SPOTMINSU(48,"販促情報_スポット最低発注数",RefTable.MSTSHN,"HS_SPOTMINSU"),
		/** 製造限度日数 */
		SEIZOGENNISU(49,"製造限度日数",RefTable.MSTSHN,"SEIZOGENNISU"),
		/** リードタイムパターン */
		READTMPTN(50,"リードタイムパターン",RefTable.MSTSHN,"READTMPTN"),
		/** 発注曜日_月 */
		HAT_MONKBN(51,"発注曜日_月",RefTable.MSTSHN,"HAT_MONKBN"),
		/** 発注曜日_火 */
		HAT_TUEKBN(52,"発注曜日_火",RefTable.MSTSHN,"HAT_TUEKBN"),
		/** 発注曜日_水 */
		HAT_WEDKBN(53,"発注曜日_水",RefTable.MSTSHN,"HAT_WEDKBN"),
		/** 発注曜日_木 */
		HAT_THUKBN(54,"発注曜日_木",RefTable.MSTSHN,"HAT_THUKBN"),
		/** 発注曜日_金 */
		HAT_FRIKBN(55,"発注曜日_金",RefTable.MSTSHN,"HAT_FRIKBN"),
		/** 発注曜日_土 */
		HAT_SATKBN(56,"発注曜日_土",RefTable.MSTSHN,"HAT_SATKBN"),
		/** 発注曜日_日 */
		HAT_SUNKBN(57,"発注曜日_日",RefTable.MSTSHN,"HAT_SUNKBN"),
		/** 配送パターン */
		HSPTN(58,"配送パターン",RefTable.MSTSHN,"HSPTN"),
		/** ユニットプライス_容量 */
		UP_YORYOSU(59,"ユニットプライス_容量",RefTable.MSTSHN,"UP_YORYOSU"),
		/** ユニットプライス_単位容量 */
		UP_TYORYOSU(60,"ユニットプライス_単位容量",RefTable.MSTSHN,"UP_TYORYOSU"),
		/** ユニットプライス_ユニット単位 */
		UP_TANIKBN(61,"ユニットプライス_ユニット単位",RefTable.MSTSHN,"UP_TANIKBN"),
		/** 商品サイズ_縦 */
		SHNTATESZ(62,"商品サイズ_縦",RefTable.MSTSHN,"SHNTATESZ"),
		/** 商品サイズ_横 */
		SHNYOKOSZ(63,"商品サイズ_横",RefTable.MSTSHN,"SHNYOKOSZ"),
		/** 商品サイズ_奥行 */
		SHNOKUSZ(64,"商品サイズ_奥行",RefTable.MSTSHN,"SHNOKUSZ"),
		/** 商品サイズ_重量 */
		SHNJRYOSZ(65,"商品サイズ_重量",RefTable.MSTSHN,"SHNJRYOSZ"),
		/** 取扱期間_開始日 */
		ATSUK_STDT(66,"取扱期間_開始日",RefTable.MSTSHN,"ATSUK_STDT"),
		/** 取扱期間_終了日 */
		ATSUK_EDDT(67,"取扱期間_終了日",RefTable.MSTSHN,"ATSUK_EDDT"),
		/** 陳列形式コード */
		CHINRETUCD(68,"陳列形式コード",RefTable.MSTSHN,"CHINRETUCD"),
		/** 段積み形式コード */
		DANTUMICD(69,"段積み形式コード",RefTable.MSTSHN,"DANTUMICD"),
		/** 重なりコード */
		KASANARICD(70,"重なりコード",RefTable.MSTSHN,"KASANARICD"),
		/** 重なりサイズ */
		KASANARISZ(71,"重なりサイズ",RefTable.MSTSHN,"KASANARISZ"),
		/** 圧縮率 */
		ASSHUKURT(72,"圧縮率",RefTable.MSTSHN,"ASSHUKURT"),
		/** マスタ変更予定日 */
		YOYAKUDT(73,"マスタ変更予定日",RefTable.MSTSHN,"YOYAKUDT"),
		/** 店売価実施日 */
		TENBAIKADT(74,"店売価実施日",RefTable.MSTSHN,"TENBAIKADT"),
		/** 用途分類コード_部門 */
		YOT_BMNCD(75,"用途分類コード_部門",RefTable.MSTSHN,"YOT_BMNCD"),
		/** 用途分類コード_大 */
		YOT_DAICD(76,"用途分類コード_大",RefTable.MSTSHN,"YOT_DAICD"),
		/** 用途分類コード_中 */
		YOT_CHUCD(77,"用途分類コード_中",RefTable.MSTSHN,"YOT_CHUCD"),
		/** 用途分類コード_小 */
		YOT_SHOCD(78,"用途分類コード_小",RefTable.MSTSHN,"YOT_SHOCD"),
		/** 売場分類コード_部門 */
		URI_BMNCD(79,"売場分類コード_部門",RefTable.MSTSHN,"URI_BMNCD"),
		/** 売場分類コード_大 */
		URI_DAICD(80,"売場分類コード_大",RefTable.MSTSHN,"URI_DAICD"),
		/** 売場分類コード_中 */
		URI_CHUCD(81,"売場分類コード_中",RefTable.MSTSHN,"URI_CHUCD"),
		/** 売場分類コード_小 */
		URI_SHOCD(82,"売場分類コード_小",RefTable.MSTSHN,"URI_SHOCD"),

		/** エリア区分（仕入） */
		AREAKBN1(83,"エリア区分（仕入）",RefTable.MSTSIRGPSHN,"AREAKBN"),
		/** 店グループ（エリア）_1（仕入） */
		TENGPCD1_1(84,"店グループ（エリア）_1（仕入）",RefTable.MSTSIRGPSHN,"TENGPCD"),
		/** 仕入先コード_1（仕入） */
		SIRCD1_1(85,"仕入先コード_1（仕入）",RefTable.MSTSIRGPSHN,"SIRCD"),
		/** 配送パターン_1（仕入） */
		HSPTN1_1(86,"配送パターン_1（仕入）",RefTable.MSTSIRGPSHN,"HSPTN"),
		/** 店グループ（エリア）_2（仕入） */
		TENGPCD1_2(87,"店グループ（エリア）_2（仕入）",RefTable.MSTSIRGPSHN,"TENGPCD"),
		/** 仕入先コード_2（仕入） */
		SIRCD1_2(88,"仕入先コード_2（仕入）",RefTable.MSTSIRGPSHN,"SIRCD"),
		/** 配送パターン_2（仕入） */
		HSPTN1_2(89,"配送パターン_2（仕入）",RefTable.MSTSIRGPSHN,"HSPTN"),
		/** 店グループ（エリア）_3（仕入） */
		TENGPCD1_3(90,"店グループ（エリア）_3（仕入）",RefTable.MSTSIRGPSHN,"TENGPCD"),
		/** 仕入先コード_3（仕入） */
		SIRCD1_3(91,"仕入先コード_3（仕入）",RefTable.MSTSIRGPSHN,"SIRCD"),
		/** 配送パターン_3（仕入） */
		HSPTN1_3(92,"配送パターン_3（仕入）",RefTable.MSTSIRGPSHN,"HSPTN"),
		/** 店グループ（エリア）_4（仕入） */
		TENGPCD1_4(93,"店グループ（エリア）_4（仕入）",RefTable.MSTSIRGPSHN,"TENGPCD"),
		/** 仕入先コード_4（仕入） */
		SIRCD1_4(94,"仕入先コード_4（仕入）",RefTable.MSTSIRGPSHN,"SIRCD"),
		/** 配送パターン_4（仕入） */
		HSPTN1_4(95,"配送パターン_4（仕入）",RefTable.MSTSIRGPSHN,"HSPTN"),
		/** 店グループ（エリア）_5（仕入） */
		TENGPCD1_5(96,"店グループ（エリア）_5（仕入）",RefTable.MSTSIRGPSHN,"TENGPCD"),
		/** 仕入先コード_5（仕入） */
		SIRCD1_5(97,"仕入先コード_5（仕入）",RefTable.MSTSIRGPSHN,"SIRCD"),
		/** 配送パターン_5（仕入） */
		HSPTN1_5(98,"配送パターン_5（仕入）",RefTable.MSTSIRGPSHN,"HSPTN"),
		/** 店グループ（エリア）_6（仕入） */
		TENGPCD1_6(99,"店グループ（エリア）_6（仕入）",RefTable.MSTSIRGPSHN,"TENGPCD"),
		/** 仕入先コード_6（仕入） */
		SIRCD1_6(100,"仕入先コード_6（仕入）",RefTable.MSTSIRGPSHN,"SIRCD"),
		/** 配送パターン_6（仕入） */
		HSPTN1_6(101,"配送パターン_6（仕入）",RefTable.MSTSIRGPSHN,"HSPTN"),
		/** 店グループ（エリア）_7（仕入） */
		TENGPCD1_7(102,"店グループ（エリア）_7（仕入）",RefTable.MSTSIRGPSHN,"TENGPCD"),
		/** 仕入先コード_7（仕入） */
		SIRCD1_7(103,"仕入先コード_7（仕入）",RefTable.MSTSIRGPSHN,"SIRCD"),
		/** 配送パターン_7（仕入） */
		HSPTN1_7(104,"配送パターン_7（仕入）",RefTable.MSTSIRGPSHN,"HSPTN"),
		/** 店グループ（エリア）_8（仕入） */
		TENGPCD1_8(105,"店グループ（エリア）_8（仕入）",RefTable.MSTSIRGPSHN,"TENGPCD"),
		/** 仕入先コード_8（仕入） */
		SIRCD1_8(106,"仕入先コード_8（仕入）",RefTable.MSTSIRGPSHN,"SIRCD"),
		/** 配送パターン_8（仕入） */
		HSPTN1_8(107,"配送パターン_8（仕入）",RefTable.MSTSIRGPSHN,"HSPTN"),
		/** 店グループ（エリア）_9（仕入） */
		TENGPCD1_9(108,"店グループ（エリア）_9（仕入）",RefTable.MSTSIRGPSHN,"TENGPCD"),
		/** 仕入先コード_9（仕入） */
		SIRCD1_9(109,"仕入先コード_9（仕入）",RefTable.MSTSIRGPSHN,"SIRCD"),
		/** 配送パターン_9（仕入） */
		HSPTN1_9(110,"配送パターン_9（仕入）",RefTable.MSTSIRGPSHN,"HSPTN"),
		/** 店グループ（エリア）_10（仕入） */
		TENGPCD1_10(111,"店グループ（エリア）_10（仕入）",RefTable.MSTSIRGPSHN,"TENGPCD"),
		/** 仕入先コード_10（仕入） */
		SIRCD1_10(112,"仕入先コード_10（仕入）",RefTable.MSTSIRGPSHN,"SIRCD"),
		/** 配送パターン_10（仕入） */
		HSPTN1_10(113,"配送パターン_10（仕入）",RefTable.MSTSIRGPSHN,"HSPTN"),

		/** エリア区分（売価） */
		AREAKBN2(114,"エリア区分（売価）",RefTable.MSTBAIKACTL,"AREAKBN"),
		/** 店グループ（エリア）_1（売価） */
		TENGPCD2_1(115,"店グループ（エリア）_1（売価）",RefTable.MSTBAIKACTL,"TENGPCD"),
		/** 原価_1（売価） */
		GENKAAM2_1(116,"原価_1（売価）",RefTable.MSTBAIKACTL,"GENKAAM"),
		/** 売価_1（売価） */
		BAIKAAM2_1(117,"売価_1（売価）",RefTable.MSTBAIKACTL,"BAIKAAM"),
		/** 店入数_1（売価） */
		IRISU2_1(118,"店入数_1（売価）",RefTable.MSTBAIKACTL,"IRISU"),
		/** 店グループ（エリア）_2（売価） */
		TENGPCD2_2(119,"店グループ（エリア）_2（売価）",RefTable.MSTBAIKACTL,"TENGPCD"),
		/** 原価_2（売価） */
		GENKAAM2_2(120,"原価_2（売価）",RefTable.MSTBAIKACTL,"GENKAAM"),
		/** 売価_2（売価） */
		BAIKAAM2_2(121,"売価_2（売価）",RefTable.MSTBAIKACTL,"BAIKAAM"),
		/** 店入数_2（売価） */
		IRISU2_2(122,"店入数_2（売価）",RefTable.MSTBAIKACTL,"IRISU"),
		/** 店グループ（エリア）_3（売価） */
		TENGPCD2_3(123,"店グループ（エリア）_3（売価）",RefTable.MSTBAIKACTL,"TENGPCD"),
		/** 原価_3（売価） */
		GENKAAM2_3(124,"原価_3（売価）",RefTable.MSTBAIKACTL,"GENKAAM"),
		/** 売価_3（売価） */
		BAIKAAM2_3(125,"売価_3（売価）",RefTable.MSTBAIKACTL,"BAIKAAM"),
		/** 店入数_3（売価） */
		IRISU2_3(126,"店入数_3（売価）",RefTable.MSTBAIKACTL,"IRISU"),
		/** 店グループ（エリア）_4（売価） */
		TENGPCD2_4(127,"店グループ（エリア）_4（売価）",RefTable.MSTBAIKACTL,"TENGPCD"),
		/** 原価_4（売価） */
		GENKAAM2_4(128,"原価_4（売価）",RefTable.MSTBAIKACTL,"GENKAAM"),
		/** 売価_4（売価） */
		BAIKAAM2_4(129,"売価_4（売価）",RefTable.MSTBAIKACTL,"BAIKAAM"),
		/** 店入数_4（売価） */
		IRISU2_4(130,"店入数_4（売価）",RefTable.MSTBAIKACTL,"IRISU"),
		/** 店グループ（エリア）_5（売価） */
		TENGPCD2_5(131,"店グループ（エリア）_5（売価）",RefTable.MSTBAIKACTL,"TENGPCD"),
		/** 原価_5（売価） */
		GENKAAM2_5(132,"原価_5（売価）",RefTable.MSTBAIKACTL,"GENKAAM"),
		/** 売価_5（売価） */
		BAIKAAM2_5(133,"売価_5（売価）",RefTable.MSTBAIKACTL,"BAIKAAM"),
		/** 店入数_5（売価） */
		IRISU2_5(134,"店入数_5（売価）",RefTable.MSTBAIKACTL,"IRISU"),

		/** エリア区分（品揃） */
		AREAKBN3(135,"エリア区分（品揃）",RefTable.MSTSHINAGP,"AREAKBN"),
		/** 店グループ（エリア）_1（品揃） */
		TENGPCD3_1(136,"店グループ（エリア）_1（品揃）",RefTable.MSTSHINAGP,"TENGPCD"),
		/** 扱い区分_1（品揃） */
		ATSUKKBN3_1(137,"扱い区分_1（品揃）",RefTable.MSTSHINAGP,"ATSUKKBN"),
		/** 店グループ（エリア）_2（品揃） */
		TENGPCD3_2(138,"店グループ（エリア）_2（品揃）",RefTable.MSTSHINAGP,"TENGPCD"),
		/** 扱い区分_2（品揃） */
		ATSUKKBN3_2(139,"扱い区分_2（品揃）",RefTable.MSTSHINAGP,"ATSUKKBN"),
		/** 店グループ（エリア）_3（品揃） */
		TENGPCD3_3(140,"店グループ（エリア）_3（品揃）",RefTable.MSTSHINAGP,"TENGPCD"),
		/** 扱い区分_3（品揃） */
		ATSUKKBN3_3(141,"扱い区分_3（品揃）",RefTable.MSTSHINAGP,"ATSUKKBN"),
		/** 店グループ（エリア）_4（品揃） */
		TENGPCD3_4(142,"店グループ（エリア）_4（品揃）",RefTable.MSTSHINAGP,"TENGPCD"),
		/** 扱い区分_4（品揃） */
		ATSUKKBN3_4(143,"扱い区分_4（品揃）",RefTable.MSTSHINAGP,"ATSUKKBN"),
		/** 店グループ（エリア）_5（品揃） */
		TENGPCD3_5(144,"店グループ（エリア）_5（品揃）",RefTable.MSTSHINAGP,"TENGPCD"),
		/** 扱い区分_5（品揃） */
		ATSUKKBN3_5(145,"扱い区分_5（品揃）",RefTable.MSTSHINAGP,"ATSUKKBN"),
		/** 店グループ（エリア）_6（品揃） */
		TENGPCD3_6(146,"店グループ（エリア）_6（品揃）",RefTable.MSTSHINAGP,"TENGPCD"),
		/** 扱い区分_6（品揃） */
		ATSUKKBN3_6(147,"扱い区分_6（品揃）",RefTable.MSTSHINAGP,"ATSUKKBN"),
		/** 店グループ（エリア）_7（品揃） */
		TENGPCD3_7(148,"店グループ（エリア）_7（品揃）",RefTable.MSTSHINAGP,"TENGPCD"),
		/** 扱い区分_7（品揃） */
		ATSUKKBN3_7(149,"扱い区分_7（品揃）",RefTable.MSTSHINAGP,"ATSUKKBN"),
		/** 店グループ（エリア）_8（品揃） */
		TENGPCD3_8(150,"店グループ（エリア）_8（品揃）",RefTable.MSTSHINAGP,"TENGPCD"),
		/** 扱い区分_8（品揃） */
		ATSUKKBN3_8(151,"扱い区分_8（品揃）",RefTable.MSTSHINAGP,"ATSUKKBN"),
		/** 店グループ（エリア）_9（品揃） */
		TENGPCD3_9(152,"店グループ（エリア）_9（品揃）",RefTable.MSTSHINAGP,"TENGPCD"),
		/** 扱い区分_9（品揃） */
		ATSUKKBN3_9(153,"扱い区分_9（品揃）",RefTable.MSTSHINAGP,"ATSUKKBN"),
		/** 店グループ（エリア）_10（品揃） */
		TENGPCD3_10(154,"店グループ（エリア）_10（品揃）",RefTable.MSTSHINAGP,"TENGPCD"),
		/** 扱い区分_10（品揃） */
		ATSUKKBN3_10(155,"扱い区分_10（品揃）",RefTable.MSTSHINAGP,"ATSUKKBN"),
		/** 平均パック単価 */
		AVGPTANKAAM(156,"平均パック単価",RefTable.MSTAVGPTANKA,"AVGPTANKAAM"),
		/** ソース区分_2 */
		SOURCEKBN2(157,"ソース区分_2",RefTable.MSTSRCCD,"SOURCEKBN"),
		/** ソースコード_2 */
		SRCCD2(158,"ソースコード_2",RefTable.MSTSRCCD,"SRCCD"),
		/** プライスカード出力有無 */
		PCARD_OPFLG(159,"プライスカード出力有無",RefTable.MSTSHN,"PCARD_OPFLG"),
		/** プライスカード_種類 */
		PCARD_SHUKBN(160,"プライスカード_種類",RefTable.MSTSHN,"PCARD_SHUKBN"),
		/** プライスカード_色 */
		PCARD_IROKBN(161,"プライスカード_色",RefTable.MSTSHN,"PCARD_IROKBN"),
		/** 税区分 */
		ZEIKBN(162,"税区分",RefTable.MSTSHN,"ZEIKBN"),
		/** 税率区分 */
		ZEIRTKBN(163,"税率区分",RefTable.MSTSHN,"ZEIRTKBN"),
		/** 旧税率区分 */
		ZEIRTKBN_OLD(164,"旧税率区分",RefTable.MSTSHN,"ZEIRTKBN_OLD"),
		/** 税率変更日 */
		ZEIRTHENKODT(165,"税率変更日",RefTable.MSTSHN,"ZEIRTHENKODT"),
		/** 取扱停止 */
		TEISHIKBN(166,"取扱停止",RefTable.MSTSHN,"TEISHIKBN"),
		/** 市場区分 */
		ICHIBAKBN(167,"市場区分",RefTable.MSTSHN,"ICHIBAKBN"),
		/** ＰＢ区分 */
		PBKBN(168,"ＰＢ区分",RefTable.MSTSHN,"PBKBN"),
		/** 返品区分 */
		HENPIN_KBN(169,"返品区分",RefTable.MSTSHN,"HENPIN_KBN"),
		/** 輸入区分 */
		IMPORTKBN(170,"輸入区分",RefTable.MSTSHN,"IMPORTKBN"),
		/** 裏貼 */
		URABARIKBN(171,"裏貼",RefTable.MSTSHN,"URABARIKBN"),
		/** 対象年齢 */
		TAISHONENSU(172,"対象年齢",RefTable.MSTSHN,"TAISHONENSU"),
		/** カロリー表示 */
		CALORIESU(173,"カロリー表示",RefTable.MSTSHN,"CALORIESU"),
		/** 加工区分 */
		KAKOKBN(174,"加工区分",RefTable.MSTSHN,"KAKOKBN"),
		/** 産地（漢字） */
		SANCHIKN(175,"産地（漢字）",RefTable.MSTSHN,"SANCHIKN"),
		/** 酒級 */
		SHUKYUKBN(176,"酒級",RefTable.MSTSHN,"SHUKYUKBN"),
		/** 度数 */
		DOSU(177,"度数",RefTable.MSTSHN,"DOSU"),
		/** 包材用途 */
		HZI_YOTO(178,"包材用途",RefTable.MSTSHN,"HZI_YOTO"),
		/** 包材材質 */
		HZI_ZAISHITU(179,"包材材質",RefTable.MSTSHN,"HZI_ZAISHITU"),
		/** 包材リサイクル対象 */
		HZI_RECYCLE(180,"包材リサイクル対象",RefTable.MSTSHN,"HZI_RECYCLE"),
		/** フラグ情報_ＥＬＰ */
		ELPFLG(181,"フラグ情報_ＥＬＰ",RefTable.MSTSHN,"ELPFLG"),
		/** フラグ情報_ベルマーク */
		BELLMARKFLG(182,"フラグ情報_ベルマーク",RefTable.MSTSHN,"BELLMARKFLG"),
		/** フラグ情報_リサイクル */
		RECYCLEFLG(183,"フラグ情報_リサイクル",RefTable.MSTSHN,"RECYCLEFLG"),
		/** フラグ情報_エコマーク */
		ECOFLG(184,"フラグ情報_エコマーク",RefTable.MSTSHN,"ECOFLG"),
		/** メーカーコード */
		MAKERCD(185,"メーカーコード",RefTable.MSTSHN,"MAKERCD"),
		/** 販売コード */
		URICD(186,"販売コード",RefTable.MSTSHN,"URICD"),
		/** 添加物_1 */
		TENKABCD2_1(187,"添加物_1",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** 添加物_2 */
		TENKABCD2_2(188,"添加物_2",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** 添加物_3 */
		TENKABCD2_3(189,"添加物_3",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** 添加物_4 */
		TENKABCD2_4(190,"添加物_4",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** 添加物_5 */
		TENKABCD2_5(191,"添加物_5",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** 添加物_6 */
		TENKABCD2_6(192,"添加物_6",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** 添加物_7 */
		TENKABCD2_7(193,"添加物_7",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** 添加物_8 */
		TENKABCD2_8(194,"添加物_8",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** 添加物_9 */
		TENKABCD2_9(195,"添加物_9",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** 添加物_10 */
		TENKABCD2_10(196,"添加物_10",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** アレルギー_1 */
		TENKABCD1_1(197,"アレルギー_1",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** アレルギー_2 */
		TENKABCD1_2(198,"アレルギー_2",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** アレルギー_3 */
		TENKABCD1_3(199,"アレルギー_3",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** アレルギー_4 */
		TENKABCD1_4(200,"アレルギー_4",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** アレルギー_5 */
		TENKABCD1_5(201,"アレルギー_5",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** アレルギー_6 */
		TENKABCD1_6(202,"アレルギー_6",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** アレルギー_7 */
		TENKABCD1_7(203,"アレルギー_7",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** アレルギー_8 */
		TENKABCD1_8(204,"アレルギー_8",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** アレルギー_9 */
		TENKABCD1_9(205,"アレルギー_9",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** アレルギー_10 */
		TENKABCD1_10(206,"アレルギー_10",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** アレルギー_11 */
		TENKABCD1_11(207,"アレルギー_11",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** アレルギー_12 */
		TENKABCD1_12(208,"アレルギー_12",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** アレルギー_13 */
		TENKABCD1_13(209,"アレルギー_13",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** アレルギー_14 */
		TENKABCD1_14(210,"アレルギー_14",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** アレルギー_15 */
		TENKABCD1_15(211,"アレルギー_15",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** アレルギー_16 */
		TENKABCD1_16(212,"アレルギー_16",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** アレルギー_17 */
		TENKABCD1_17(213,"アレルギー_17",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** アレルギー_18 */
		TENKABCD1_18(214,"アレルギー_18",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** アレルギー_19 */
		TENKABCD1_19(215,"アレルギー_19",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** アレルギー_20 */
		TENKABCD1_20(216,"アレルギー_20",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** アレルギー_21 */
		TENKABCD1_21(217,"アレルギー_21",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** アレルギー_22 */
		TENKABCD1_22(218,"アレルギー_22",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** アレルギー_23 */
		TENKABCD1_23(219,"アレルギー_23",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** アレルギー_24 */
		TENKABCD1_24(220,"アレルギー_24",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** アレルギー_25 */
		TENKABCD1_25(221,"アレルギー_25",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** アレルギー_26 */
		TENKABCD1_26(222,"アレルギー_26",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** アレルギー_27 */
		TENKABCD1_27(223,"アレルギー_27",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** アレルギー_28 */
		TENKABCD1_28(224,"アレルギー_28",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** アレルギー_29 */
		TENKABCD1_29(225,"アレルギー_29",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** アレルギー_30 */
		TENKABCD1_30(226,"アレルギー_30",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** 種別コード */
		SHUBETUCD(227,"種別コード",RefTable.MSTSHN,"SHUBETUCD"),
		/** 衣料使い回しフラグ */
		IRYOREFLG(228,"衣料使い回しフラグ",RefTable.MSTSHN,"IRYOREFLG"),
		/** 登録元 */
		TOROKUMOTO(229,"登録元",RefTable.MSTSHN,"TOROKUMOTO"),
		/** オペレータ */
		OPERATOR(230,"オペレータ",RefTable.MSTSHN,"OPERATOR"),
		/** 登録日 */
		ADDDT(231,"登録日",RefTable.MSTSHN,"ADDDT"),
		/** 変更日 */
		UPDDT(232,"変更日",RefTable.MSTSHN,"UPDDT"),

		/*** デリカ対応追加項目 ***/
		/** 保温区分 */
		K_HONKB(233,"保温区分",RefTable.MSTSHN,"K_HONKB"),
		/** デリカワッペン */
		K_WAPNFLG_R(234,"デリカワッペン",RefTable.MSTSHN,"K_WAPNFLG_R"),
		/** 取引区分 */
		K_TORIKB(235,"取引区分",RefTable.MSTSHN,"K_TORIKB"),
		/** ＩＴＦコード */
		ITFCD(236,"ＩＴＦコード",RefTable.MSTSHN,"ITFCD"),
		/** センター入数 */
		CENTER_IRISU(237,"センター入数",RefTable.MSTSHN,"CENTER_IRISU"),
		/** 店別異部門 */
		TENBMNCD(238,"店別異部門",RefTable.MSTSHNTENBMN,"TENBMNCD"),
		/** 商品ｺｰﾄﾞ_1 */
		TENSHNCD_1(239,"商品ｺｰﾄﾞ_1",RefTable.MSTSHNTENBMN,"TENSHNCD"),
		/** JANｺｰﾄﾞ_1 */
		SRCCD_1(240,"JANｺｰﾄﾞ_1",RefTable.MSTSHNTENBMN,"SRCCD"),
		/** 店ｸﾞﾙｰﾌﾟ_1 */
		TENGPCD_1(241,"店ｸﾞﾙｰﾌﾟ_1",RefTable.MSTSHNTENBMN,"TENGPCD"),
		/** 商品ｺｰﾄﾞ_1 */
		TENSHNCD_2(242,"商品ｺｰﾄﾞ_2",RefTable.MSTSHNTENBMN,"TENSHNCD"),
		/** JANｺｰﾄﾞ_1 */
		SRCCD_2(243,"JANｺｰﾄﾞ_2",RefTable.MSTSHNTENBMN,"SRCCD"),
		/** 店ｸﾞﾙｰﾌﾟ_1 */
		TENGPCD_2(244,"店ｸﾞﾙｰﾌﾟ_2",RefTable.MSTSHNTENBMN,"TENGPCD"),
		/** 商品ｺｰﾄﾞ_1 */
		TENSHNCD_3(245,"商品ｺｰﾄﾞ_3",RefTable.MSTSHNTENBMN,"TENSHNCD"),
		/** JANｺｰﾄﾞ_1 */
		SRCCD_3(246,"JANｺｰﾄﾞ_3",RefTable.MSTSHNTENBMN,"SRCCD"),
		/** 店ｸﾞﾙｰﾌﾟ_1 */
		TENGPCD_3(247,"店ｸﾞﾙｰﾌﾟ_3",RefTable.MSTSHNTENBMN,"TENGPCD"),
		/** 商品ｺｰﾄﾞ_1 */
		TENSHNCD_4(248,"商品ｺｰﾄﾞ_4",RefTable.MSTSHNTENBMN,"TENSHNCD"),
		/** JANｺｰﾄﾞ_1 */
		SRCCD_4(249,"JANｺｰﾄﾞ_4",RefTable.MSTSHNTENBMN,"SRCCD"),
		/** 店ｸﾞﾙｰﾌﾟ_1 */
		TENGPCD_4(250,"店ｸﾞﾙｰﾌﾟ_4",RefTable.MSTSHNTENBMN,"TENGPCD"),
		/** 商品ｺｰﾄﾞ_1 */
		TENSHNCD_5(251,"商品ｺｰﾄﾞ_5",RefTable.MSTSHNTENBMN,"TENSHNCD"),
		/** JANｺｰﾄﾞ_1 */
		SRCCD_5(252,"JANｺｰﾄﾞ_5",RefTable.MSTSHNTENBMN,"SRCCD"),
		/** 店ｸﾞﾙｰﾌﾟ_1 */
		TENGPCD_5(253,"店ｸﾞﾙｰﾌﾟ_5",RefTable.MSTSHNTENBMN,"TENGPCD");

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

	/**
	 * CSVエラー情報セット
	 * @throws Exception
	 */
	public void setCsvshnErrinfo(JSONObject o, String errcd, String errtbl, String errfld, String errvl) {
		o.put(CSVSHNLayout.ERRCD.getCol(), errcd);	// TODO
		o.put(CSVSHNLayout.ERRTBLNM.getCol(), errtbl);
		o.put(CSVSHNLayout.ERRFLD.getCol(), errfld);
		o.put(CSVSHNLayout.ERRVL.getCol(), MessageUtility.leftB(StringUtils.trim(errvl + " " + o.optString(MessageUtility.MSG)), 100));
	}

	/**
	 * CSVエラー情報セット
	 * @throws Exception
	 */
	public void setCsvshnErrinfo(JSONObject o, RefTable errtbl, FileLayout errfld, String errvl) {
		this.setCsvshnErrinfo(o, "0", errtbl.getTxt(), errfld.getTxt(), errvl);
	}
}
