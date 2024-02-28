/**
 *
 */
package dao;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.JDBCType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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
import common.DefineReport.DataType;
import common.DefineReport.InfTrankbn;
import common.Defines;
import common.FileList;
import common.ItemList;
import common.MessageUtility;
import common.MessageUtility.Msg;
import common.MessageUtility.MsgKey;
import common.NumberingUtility;
import dao.Reportx002Dao.CSVCMNLayout;
import dao.Reportx002Dao.CSVSHNLayout;
import dao.Reportx002Dao.MSTBAIKACTLLayout;
import dao.Reportx002Dao.MSTSHINAGPLayout;
import dao.Reportx002Dao.MSTSHNTENBMNLayout;
import dao.Reportx002Dao.MSTSIRGPSHNLayout;
import dao.Reportx002Dao.MSTSRCCDLayout;
import dao.Reportx002Dao.MSTTENKABUTSULayout;
import dao.Reportx002Dao.TblType;
import dto.JQEasyModel;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 */
public class Reportx004Dao extends ItemDao {

	boolean isTest = false;

	/** 最大処理件数 */
	public static int MAX_ROW = 10000;

	/**
	 * インスタンスを生成します。
	 * @param source
	 */
	public Reportx004Dao(String JNDIname) {
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
					String shncd = StringUtils.trim((String) data[FileLayout.SHNCD.getNo()-1]);
					// 頭1ケタ落ち対応
					if(StringUtils.length(shncd)==3){
						shncd = StringUtils.leftPad(shncd, 4, "0");
					}else if(StringUtils.length(shncd)==7){
						shncd = StringUtils.leftPad(shncd, 8, "0");
					}
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
							}
							// ユニットプライス_容量、又はユニットプライス_単位容量
							if(ArrayUtils.contains(new String[]{FileLayout.UP_YORYOSU.getCol(),FileLayout.UP_TYORYOSU.getCol()}, itm.getCol())){
								// 小数点以下を四捨五入する
								try {
									val = String.valueOf(Math.round(Double.parseDouble(val)));
								}
								catch(NumberFormatException e) {}
							}
							// ＰＯＰ名称（漢字）
							if(StringUtils.equals(itm.getCol(), FileLayout.POPKN.getCol()) ){
								// 商品種類が"通常商品"かつＰＯＰ名称（漢字）が空の場合
								if(DefineReport.ValKbn105.VAL0.getVal().equals(oshn.getString(FileLayout.SHNKBN.getCol())) && StringUtils.isEmpty(val)) {
									// 商品名（カナ）を全角変換して設定
									val = MessageUtility.HanToZen(oshn.getString(FileLayout.SHNAN.getCol()));
								}
							}
							if(StringUtils.equals(itm.getCol(), FileLayout.SHNCD.getCol()) ){
								oshn.put(itm.getCol(), shncd);
							}else{
								oshn.put(itm.getCol(), val);
							}
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
								osrc.put(MSTSRCCDLayout.SHNCD.getCol(), shncd);
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
								osir.put(MSTSIRGPSHNLayout.SHNCD.getCol(), shncd);
								osir.put(MSTSIRGPSHNLayout.YOYAKUDT.getCol(), szYoyakudt);
								osir.put(CSVCMNLayout.SEQ.getCol(), seq);						// CSV用SEQ
								osir.put(CSVCMNLayout.INPUTNO.getCol(),inputno);				// CSV用入力番号
								osir.put(CSVCMNLayout.INPUTEDANO.getCol(), asir.size() + 1);	// CSV用入力枝番

								// エリア区分の設定がない場合は0
								String area = (String) data[FileLayout.AREAKBN1.getNo()-1];
								if (StringUtils.isEmpty(area)) area = "0";
								osir.put(MSTSIRGPSHNLayout.AREAKBN.getCol(), area);
							}
							if (!itm.getCol().equals(MSTSIRGPSHNLayout.AREAKBN.getCol())) {
								osir.put(itm.getCol(), val);
							}
						}else if(itm.getTbl() == RefTable.MSTBAIKACTL){
							if(obik.containsKey(itm.getCol())){
								abik.add(obik);
								obik = new JSONObject();
							}
							if(obik.isEmpty()){
								obik.put(MSTBAIKACTLLayout.SHNCD.getCol(), shncd);
								obik.put(MSTBAIKACTLLayout.YOYAKUDT.getCol(), szYoyakudt);
								obik.put(CSVCMNLayout.SEQ.getCol(), seq);						// CSV用SEQ
								obik.put(CSVCMNLayout.INPUTNO.getCol(),inputno);				// CSV用入力番号
								obik.put(CSVCMNLayout.INPUTEDANO.getCol(), abik.size() + 1);	// CSV用入力枝番

								// エリア区分の設定がない場合は1
								String area = (String) data[FileLayout.AREAKBN2.getNo()-1];
								if (StringUtils.isEmpty(area)) area = "1";
								obik.put(MSTBAIKACTLLayout.AREAKBN.getCol(), area);
							}
							if (!itm.getCol().equals(MSTBAIKACTLLayout.AREAKBN.getCol())) {
								obik.put(itm.getCol(), val);
							}
						}else if(itm.getTbl() == RefTable.MSTSHINAGP){
							if(osin.containsKey(itm.getCol())){
								asin.add(osin);
								osin = new JSONObject();
							}
							if(osin.isEmpty()){
								osin.put(MSTSHINAGPLayout.SHNCD.getCol(), shncd);
								osin.put(MSTSHINAGPLayout.YOYAKUDT.getCol(), szYoyakudt);
								osin.put(CSVCMNLayout.SEQ.getCol(), seq);						// CSV用SEQ
								osin.put(CSVCMNLayout.INPUTNO.getCol(),inputno);				// CSV用入力番号
								osin.put(CSVCMNLayout.INPUTEDANO.getCol(), asin.size() + 1);	// CSV用入力枝番

								// エリア区分の設定がない場合は0
								String area = (String) data[FileLayout.AREAKBN3.getNo()-1];
								if (StringUtils.isEmpty(area)) area = "0";
								osin.put(MSTSHINAGPLayout.AREAKBN.getCol(), area);
							}
							if (!itm.getCol().equals(MSTSHINAGPLayout.AREAKBN.getCol())) {
								osin.put(itm.getCol(), val);
							}
						}else if(itm.getTbl() == RefTable.MSTTENKABUTSU){
							if(oten.containsKey(itm.getCol())){
								aten.add(oten);
								oten = new JSONObject();
							}
							if(oten.isEmpty()){
								oten.put(MSTTENKABUTSULayout.SHNCD.getCol(), shncd);
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
								otbmn.put(MSTSHNTENBMNLayout.SHNCD.getCol(), shncd);
								otbmn.put(MSTSHNTENBMNLayout.YOYAKUDT.getCol(), szYoyakudt);
								String areakbn = "0";
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

					// ファイル更新区分="U"、ソース区分="9"、ソースコード!=""、該当ソースコードが存在する場合、ソースコード削除情報を退避
					JSONArray delsrcArray = new JSONArray();
					for(int j=0; j < asrc.size(); j++){
						String srccd = asrc.optJSONObject(j).optString(MSTSRCCDLayout.SRCCD.getCol());
						String srckbn = asrc.optJSONObject(j).optString(MSTSRCCDLayout.SOURCEKBN.getCol());
						if(DefineReport.ValFileUpdkbn.UPD.getVal().equals(fUpdkbn) && DefineReport.Sourcekbn.DEL.getVal().equals(srckbn) && StringUtils.isNotEmpty(srccd)) {
							Reportx002Dao dao = new Reportx002Dao(super.JNDIname);
							if(	dao.checkMstExist(DefineReport.InpText.SRCCD.getObj() + "_UPD", shncd + "," + srccd)) {
								delsrcArray.add(asrc.optJSONObject(j));
							}
						}
					}

					asrc = selectCompData(oshn, asrc,RefTable.MSTSRCCD.getId(),MSTSRCCDLayout.SRCCD.getCol(),MSTSRCCDLayout.SEQNO.getCol(),MSTSRCCDLayout.values());
					asir = selectCompData(oshn, asir,RefTable.MSTSIRGPSHN.getId(),MSTSIRGPSHNLayout.TENGPCD.getCol(),"",MSTSIRGPSHNLayout.values());
					abik = selectCompData(oshn, abik,RefTable.MSTBAIKACTL.getId(),MSTBAIKACTLLayout.TENGPCD.getCol(),"",MSTBAIKACTLLayout.values());
					asin = selectCompData(oshn, asin,RefTable.MSTSHINAGP.getId(),MSTSHINAGPLayout.TENGPCD.getCol(),"",MSTSHINAGPLayout.values());
					aten = selectCompData(oshn, aten,RefTable.MSTTENKABUTSU.getId(),MSTTENKABUTSULayout.TENKABCD.getCol(),"",MSTTENKABUTSULayout.values());
					atbmn = selectCompData(oshn, atbmn,RefTable.MSTSHNTENBMN.getId(),MSTSHNTENBMNLayout.TENSHNCD.getCol(),"",MSTSHNTENBMNLayout.values());

					// ソースコード削除有りの場合
					if(delsrcArray.size() > 0) {
						// 該当ソースコードを削除。SEQNO「1」のソースコードが削除され、かつ他のソースコードが
						// 残るケースは不可とする（画面と仕様統一。当該エラーは後のcheckDataで検知）
						for(int j=0; j < delsrcArray.size(); j++){
							String delseqno = delsrcArray.optJSONObject(j).optString(MSTSRCCDLayout.SEQNO.getCol());
							for(int k=0; k < asrc.size(); k++){
								String seqno = asrc.optJSONObject(k).optString(MSTSRCCDLayout.SEQNO.getCol());
								if(delseqno.equals(seqno)) {
									asrc.remove(k);
									break;
								}
							}
						}
						// ソースコードを全て削除する場合、メーカーコードをデフォルト値にする
						if(asrc.size() == 0) {
							oshn.put(MSTSHNLayout.MAKERCD.getCol(), oshn.get(MSTSHNLayout.BMNCD.getCol()) + "00001");
						}
					}

					// 最新の情報取得
					// データ取得先判断
					boolean isNew = DefineReport.ValFileUpdkbn.NEW.getVal().equals(fUpdkbn);
					boolean isEmptyShn = StringUtils.isEmpty(shncd);

					// TODO:各メソッドの第二引数は現在未使用
					// 1.商品マスタ
					JSONArray mstshnArray = this.selectMSTSHN(isSeiRep, isNew||isEmptyShn, oshn);
					// 2.ソースマスタ
					JSONArray mstsrcArray = this.selectMSTSRC(isSeiRep, isNew||isEmptyShn, asrc);
					// 3.仕入グループ商品マスタ
					JSONArray mstsirArray = this.selectMSTSIRGPSHN(isSeiRep, isNew||isEmptyShn, asir);
					// 4.売価コントロールマスタ
					JSONArray mstbikArray = this.selectMSTBAIKACTL(isSeiRep, isNew||isEmptyShn, abik);
					// 5.品揃グループマスタ
					JSONArray mstsinArray = this.selectMSTSHINAGP(isSeiRep, isNew||isEmptyShn, asin);
					// 6.添加物
					JSONArray msttenArray = this.selectMSTTENKABUTSU(isSeiRep, isNew||isEmptyShn, aten);
					// 7.グループ名
					JSONArray mstgrpArray = new JSONArray(); // TODO:仕様確定後 this.selectMSTTENKABUTSU(isSei, isNew||isEmptyShn, aten);
					// 8.自動発注
					JSONArray mstahsArray = new JSONArray(); // TODO:仕様確定後 this.selectMSTTENKABUTSU(isSei, isNew||isEmptyShn, aten);
					// 9.店別異部門
					JSONArray msttbmnArray = this.selectMSTSHNTENBMN(isSeiRep, isNew||isEmptyShn, atbmn);// TODO:仕様確定後 this.selectMSTTENKABUTSU(isSei, isNew||isEmptyShn, aten);
					// 10.その他
					ooth.put(CSVSHNLayout.SEQ.getCol(), seq);				// CSV用.SEQ
					ooth.put(CSVSHNLayout.INPUTNO.getCol(), inputno);		// CSV用.入力番号

					JSONObject objset = new JSONObject();
					objset.put(MSTSHNLayout.UPDKBN.getCol(), fUpdkbn);	// 更新区分
					objset.put(MSTSHNLayout.SHNCD.getCol(), shncd);		// 商品コード
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

				// 各テーブル登録用のSQL作成
				for (int i = 0; i < dataList.size(); i++) {
					Reportx002Dao dao = new Reportx002Dao(super.JNDIname);

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
					boolean isDel = isSeiRep && DefineReport.ValFileUpdkbn.DEL.getVal().equals(fUpdkbn);

					// 詳細情報チェック
					JSONArray msgList = new JSONArray();
					if(msgListB.size()==0){
						if(!isDel) {
							if(!isChangeY1Del){
								msgList = dao.checkData(
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
						}else {
							List<JSONObject> msgList_ = new ArrayList<JSONObject>();
							msgList_ = dao.checkDataDel(
									isNew, isChange, isYoyaku1, false, false,
									sendmap, userInfo, sysdate, mu,
									mstshnArray,		// 対象情報（主要な更新情報）
									mstsrcArray,		// ソースコード
									mstsinArray,		// 品揃えグループ
									mstbikArray,		// 売価コントロール
									mstsirArray,		// 仕入グループ
									msttenArray			// 添加物
								);
							if(msgList_.size() > 0){
								msgList.add(msgList_.get(0));
							}
						}
					}else{
						msgList.add(msgListB.optJSONObject(0));
					}

					boolean isError = msgList.size()!=0;
					if(isError){
						errCount++;

						// エラー時に商品コード採番時解放処理
						if(StringUtils.isNotEmpty(ooth.optString("SHNCD_RENEW"))){
							// 商品コード情報、もしくはエラー情報が返ってくる
							JSONObject result = NumberingUtility.execReleaseNewSHNCD(userInfo, ooth.optString("SHNCD_RENEW"));
						}
						// 採番実行時にエラーの場合、解除処理
						if(StringUtils.isNotEmpty(ooth.optString("URICD_NEW"))){
							// 販売コード情報、もしくはエラー情報が返ってくる
							JSONObject result = NumberingUtility.execReleaseNewURICD(userInfo, ooth.optString("URICD_NEW"));
						}

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
						}else if(DefineReport.ValFileUpdkbn.DEL.getVal().equals(fUpdkbn)){
							csvUpdkbn = DefineReport.ValCsvUpdkbn.DEL.getVal();
						}
						dao.csvshn_add_data[CSVSHNLayout.CSV_UPDKBN.getNo()-1] = csvUpdkbn;										// CSV_UPDKBN
						dao.csvshn_add_data[CSVSHNLayout.KETAKBN.getNo()-1] = ooth.optString(FileLayout.KETAKBN.getCol());		// KETAKBN
					}else{
						updCount++;
						jnlshn_seq = this.getJNLSHN_SEQ_NOW();
					}

					JSONObject rt = this.createCommandUpdateData(
							// 更新処理コマンド作成
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

					if(fUpdkbn.equals("D") && !isError) {
						JSONObject rt2 = this.createCommandDeleteData(
								// 削除処理コマンド作成
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
								mstgrpArray,		// グループ名
								mstahsArray,		// 自動発注
								ooth				// その他
							);
					}
					this.sqlList.addAll(dao.sqlList);
					this.prmList.addAll(dao.prmList);
					this.lblList.addAll(dao.lblList);
				}

				// 更新・削除処理
				try {
					option = this.updateData();
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

	/** ジャーナル更新のKEY保持用変数 */
	String jnlshn_seq = "";			//
	/** ジャーナル更新のテーブル区分保持用変数 */
	String jnlshn_tablekbn = "";	//
	/** ジャーナル更新の処理区分保持用変数 */
	String jnlshn_trankbn = "";

	/**
	 * 削除処理
	 * @param userInfo
	 *
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws Exception
	 */
	public JSONObject deleteData() throws Exception {

		// 更新情報チェック(基本JS側で制御)
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
				option.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00002.getVal()));
			}
		}else{
			option.put(MsgKey.E.getKey(), getMessage());
		}

		return option;
	}

	/**
	 * CSVエラー情報セット
	 * @throws Exception
	 */
	public void setCsvshnErrinfo(JSONObject o, RefTable errtbl, MSTLayout errfld, String errvl) {
		this.setCsvshnErrinfo(o, "0", errtbl.getTxt(), errfld.getTxt(), errvl);
	}

	/**
	 * CSVエラー情報セット
	 * @throws Exception
	 */
	public void setCsvshnErrinfo(JSONObject o, RefTable errtbl, MSTLayout errfld, JSONObject data) {
		this.setCsvshnErrinfo(o, errtbl, errfld, data.optString(errfld.getId()));
	}

	/**
	 * 正情報取得処理
	 *
	 * @throws Exception
	 */
	public JSONArray getSeiJSONArray(HashMap<String, String> map) {
		String szSelShncd	= map.get("SEL_SHNCD");	// 検索商品コード


		JSONArray array = new JSONArray();
		if(!szSelShncd.isEmpty()){
			ArrayList<String> paramData = new ArrayList<String>();
			paramData.add(szSelShncd.replace("-", "") + "%");

			StringBuffer sbSQL = new StringBuffer();
			sbSQL.append(" select ");
			for(MSTSHNLayout itm :MSTSHNLayout.values()){
				if(itm.getNo() > 1){ sbSQL.append(" ,"); }
				sbSQL.append(itm.getCol() + " as " + itm.getId());
			}
			sbSQL.append(" from INAMS.MSTSHN where SHNCD like ? and COALESCE(UPDKBN, 0) <> 1");

			ItemList iL = new ItemList();
			@SuppressWarnings("static-access")
			JSONArray array0 = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);
			array.addAll(array0);
		}
		return array;
	}

	/**
	 * 予約情報取得処理
	 *
	 * @throws Exception
	 */
	public JSONArray getYoyakuJSONArray(HashMap<String, String> map) {
		String szSelShncd	= map.get("SEL_SHNCD");	// 検索商品コード

		JSONArray array = new JSONArray();
		if(!szSelShncd.isEmpty()){
			ArrayList<String> paramData = new ArrayList<String>();
			paramData.add(szSelShncd.replace("-", "") + "%");

			StringBuffer sbSQL = new StringBuffer();
			sbSQL.append(" select ");
			for(MSTSHNLayout itm :MSTSHNLayout.values()){
				if(itm.getNo() > 1){ sbSQL.append(" ,"); }
				sbSQL.append(itm.getCol() + " as " + itm.getId());
			}
			sbSQL.append(" from INAMS.MSTSHN_Y where SHNCD like ? and COALESCE(UPDKBN, 0) <> 1 order by YOYAKUDT");

			ItemList iL = new ItemList();
			@SuppressWarnings("static-access")
			JSONArray array0 = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);
			array.addAll(array0);
		}

		return array;
	}

	/**
	 * 削除処理実行
	 * @param sysdate
	 * @return
	 *
	 * @throws Exception
	 */
	private JSONObject createCommandDeleteData(Reportx002Dao dao,
			boolean isNewtyp, boolean isSeiRep, boolean isErr, String jnlshn_seq,
			HashMap<String, String> map, User userInfo, String sysdate,
			JSONArray dataArray,				// 対象情報（主要な更新情報）
			JSONArray dataArraySRCCD,		// ソースコード
			JSONArray dataArrayTENGP4,		// 店別異部門
			JSONArray dataArrayTENGP3,		// 品揃えグループ
			JSONArray dataArrayTENGP2,		// 売価コントロール
			JSONArray dataArrayTENGP1,		// 仕入グループ
			JSONArray dataArrayTENKABUTSU,	// 添加物
			JSONArray dataArrayGROUP,		// グループ名
			JSONArray dataArrayAHS,			// 自動発注
			JSONObject dataOther			// その他情報
		) throws Exception {

		// パラメータ確認
		String szSelShncd	= map.get("SEL_SHNCD");	// 検索商品コード
		String szShncd		= map.get("SHNCD");		// 入力商品コード
		String szSeq		= map.get("SEQ");			// CSVエラー.SEQ
		String szInputno	= map.get("INPUTNO");		// CSVエラー.入力番号
		String szCsvUpdkbn	= map.get("CSV_UPDKBN");	// CSVエラー.CSV登録区分
		String szYoyakudt	= map.get("YOYAKUDT");		// CSVエラー用.マスタ変更予定日
		String szTenbaikadt	= map.get("TENBAIKADT");	// CSVエラー用.店売価実施日
		String sendBtnid	= map.get("SENDBTNID");	// 呼出しボタン

		JSONArray dataArray_Del = dataArray;	// 対象情報

		JSONArray dataArraySRCCD_Del = dataArraySRCCD;		// ソースコード
		JSONArray dataArrayTENGP4_Del = dataArrayTENGP4;	// 店別異部門
		JSONArray dataArrayTENGP3_Del = dataArrayTENGP3;	// 品揃えグループ
		JSONArray dataArrayTENGP2_Del = dataArrayTENGP2;	// 売価コントロール
		JSONArray dataArrayTENGP1_Del = dataArrayTENGP1;	// 仕入グループ
		JSONArray dataArrayTENKABUTSU_Del = dataArrayTENKABUTSU;	// 添加物
		JSONArray dataArrayGROUP_Del = dataArrayGROUP;		// グループ分類
		JSONArray dataArrayAHS_Del = dataArrayAHS;			// 自動発注データ

		JSONObject option = new JSONObject();
		JSONArray msg = new JSONArray();

		// パラメータ確認
		// 必須チェック
		if ( dataArray_Del.isEmpty() || dataArray_Del.size() != 1 || dataArray_Del.getJSONObject(0).isEmpty()) {
			option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
			return option;
		}

		// ログインユーザー情報取得
		String userId	= userInfo.getId();								// ログインユーザー
		String szYoyaku		= "0";		// 予約件数

		boolean isChange = DefineReport.Button.SEL_CHANGE.getObj().equals(sendBtnid)|| DefineReport.Button.SEARCH.getObj().equals(sendBtnid)|| DefineReport.Button.SEI.getObj().equals(sendBtnid);
		boolean isYoyaku = DefineReport.Button.YOYAKU1.getObj().equals(sendBtnid)||DefineReport.Button.YOYAKU2.getObj().equals(sendBtnid);
		// EX.CSVエラー修正
		boolean isCsverr = DefineReport.Button.ERR_CHANGE.getObj().equals(sendBtnid);


		// 基本登録情報
		JSONObject data = dataArray_Del.getJSONObject(0);

			// --- 07.商品_予約
			JSONObject result1 = dao.createSqlMSTSHN_DEL(userId, sendBtnid, data, TblType.YYK, SqlType.DEL);

			JSONArray dataArrayDel = new JSONArray();
			JSONArray dataArrayDelTENKABUTSU = new JSONArray();
			// 子テーブルは、一度削除してから追加なので、キー項目に注意
			szYoyakudt = data.optString("F2");
			dataArrayDel.add(this.createJSONObject(new String[]{"F1", "F3"}, new String[]{szShncd, szYoyakudt}));
			dataArrayDelTENKABUTSU.add(this.createJSONObject(new String[]{"F1", "F4"}, new String[]{szShncd, szYoyakudt}));

			// --- 02.仕入グループ
			JSONObject result2D= dao.createSqlMSTSIRGPSHN(userId, sendBtnid, dataArrayDel, TblType.YYK, SqlType.DEL);

			// --- 03.売価コントロール
			JSONObject result3D= dao.createSqlMSTBAIKACTL(userId, sendBtnid, dataArrayDel, TblType.YYK, SqlType.DEL);

			// --- 04.ソースコード管理
			JSONObject result4D= dao.createSqlMSTSRCCD(userId, sendBtnid, dataArrayDel, TblType.YYK, SqlType.DEL);

			// --- 05.添加物
			JSONObject result5D= dao.createSqlMSTTENKABUTSU(userId, sendBtnid, dataArrayDelTENKABUTSU, TblType.YYK, SqlType.DEL);

			// --- 06.品揃グループ
			JSONObject result6D= dao.createSqlMSTSHINAGP(userId, sendBtnid, dataArrayDel, TblType.YYK, SqlType.DEL);


			// ************ 関連テーブル処理 ***********
			// --- 14.商品更新件数
			JSONObject result14 = dao.createSqlSYSSHNCOUNT(userId, sendBtnid, TblType.SEI, SqlType.MRG);


			// ******** ジャーナル処理（正・予共通） ********
			// ジャーナル用の情報を取得
			dao.jnlshn_seq = dao.getJNLSHN_SEQ();
			dao.jnlshn_tablekbn = isYoyaku ? DefineReport.ValTablekbn.YYK.getVal() : DefineReport.ValTablekbn.SEI.getVal();		// 0：正、1：予約
			dao.jnlshn_trankbn = InfTrankbn.DEL.getVal();

			// --- 15.ジャーナル_商品
			JSONObject result15 = dao.createSqlMSTSHN(userId, sendBtnid, data, TblType.JNL, SqlType.INS);

			// --- 16.ジャーナル_仕入グループ商品
			if(dataArrayTENGP1_Del.size() > 0){
				JSONObject result16 = dao.createSqlMSTSIRGPSHN(userId, sendBtnid, dataArrayTENGP1_Del, TblType.JNL, SqlType.INS);
			}

			// --- 17.ジャーナル_売価コントロール
			if(dataArrayTENGP2_Del.size() > 0){
				JSONObject result17 = dao.createSqlMSTBAIKACTL(userId, sendBtnid, dataArrayTENGP2_Del, TblType.JNL, SqlType.INS);
			}

			// --- 18.ジャーナル_ソースコード管理
			if(dataArraySRCCD_Del.size() > 0){
				JSONObject result18 = dao.createSqlMSTSRCCD(userId, sendBtnid, dataArraySRCCD_Del, TblType.JNL, SqlType.INS);
			}

			// --- 19.ジャーナル_添加物
			if(dataArrayTENKABUTSU_Del.size() > 0){
				JSONObject result19 = dao.createSqlMSTTENKABUTSU(userId, sendBtnid, dataArrayTENKABUTSU_Del, TblType.JNL, SqlType.INS);
			}

			// --- 20.ジャーナル_品揃グループ
			if(dataArrayTENGP3_Del.size() > 0){
				JSONObject result20 = dao.createSqlMSTSHINAGP(userId, sendBtnid, dataArrayTENGP3_Del, TblType.JNL, SqlType.INS);
			}

			// --- 21.ジャーナル_グループ分類管理マスタ
			if(dataArrayGROUP_Del.size() > 0){
				JSONObject result21 = dao.createSqlMSTGRP(userId, sendBtnid, dataArrayGROUP, TblType.JNL, SqlType.INS);
			}

			// --- 22.ジャーナル_自動発注
			if(dataArrayAHS_Del.size() > 0){
				JSONObject result22 = dao.createSqlMSTAHS(userId, sendBtnid, dataArrayAHS, TblType.JNL, SqlType.INS);
			}

			// --- 23.ジャーナル_店別異部門
			if(dataArrayTENGP4_Del.size() > 0){
				JSONObject result23 = dao.createSqlMSTSHNTENBMN(userId, sendBtnid, dataArrayTENGP4_Del, TblType.JNL, SqlType.INS);
			}
		//}

		// 排他チェック実行
		String targetTable = null;
		String targetWhere = "nvl(UPDKBN, 0) <> 1";
		ArrayList<String> targetParam = new ArrayList<String>();
		// EX.CSVエラー修正
		/*if(isCsverr){
			targetTable = "INAMS.CSVSHN";
			targetWhere += " and SEQ = ? and INPUTNO = ?";
			targetParam.add(szSeq);
			targetParam.add(szInputno);
		}else{*/
			// ②正 .変更
			/*if(isChange){
				targetTable = "INAMS.MSTSHN";
				targetWhere += " and SHNCD = ?";
				targetParam.add(data.optString(MSTSHNLayout.SHNCD.getId()));*/

			// ⑤予1.変更/⑧予2.変更
			//}else if(isYoyaku){
				targetTable = "INAMS.MSTSHN_Y";
				targetWhere += " and SHNCD = ? and YOYAKUDT = ?";
				targetParam.add(data.optString(MSTSHNLayout.SHNCD.getId()));
				targetParam.add(data.optString(MSTSHNLayout.YOYAKUDT.getId()));
			//}
		//}
 		/*if(!super.checkExclusion(targetTable, targetWhere, targetParam, data.optString("F127"))){
 			msg.add(MessageUtility.getDbMessageExclusion(FieldType.DEFAULT, new String[]{}));
 			option.put(MsgKey.E.getKey(), msg);
			return option;
		}*/



		return option;
	}

	private JSONArray checkData(boolean isSeiRep, Reportx002Dao dao,
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
		if(!isSeiRep && (DefineReport.ValFileUpdkbn.NEW.getVal().equals(fUpdkbn)||DefineReport.ValFileUpdkbn.UPD.getVal().equals(fUpdkbn)
				||DefineReport.ValFileUpdkbn.DEL.getVal().equals(fUpdkbn))){
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
		// ⑧正 .削除 → 正 ：Delete処理、ジャ：Insert処理								※予1がある場合不可
		boolean isNew = isSeiRep && DefineReport.ValFileUpdkbn.NEW.getVal().equals(fUpdkbn);
		boolean isChange = isSeiRep && DefineReport.ValFileUpdkbn.UPD.getVal().equals(fUpdkbn);
		boolean isYoyaku1 = !isSeiRep;
		boolean isChangeY1 = !isSeiRep && DefineReport.ValFileUpdkbn.YYK.getVal().equals(fUpdkbn);
		boolean isChangeY1Del = !isSeiRep && DefineReport.ValFileUpdkbn.YDEL.getVal().equals(fUpdkbn);
		boolean isDel = isSeiRep && DefineReport.ValFileUpdkbn.DEL.getVal().equals(fUpdkbn);

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
		if(isDel && NumberUtils.toInt(szYoyaku, 0) > 0){
			JSONObject o = mu.getDbMessageObj("E11179", new String[]{});
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
			String txt_shncd = StringUtils.strip(data.optString(MSTSHNLayout.SHNCD.getId()));
			String txt_yoyakudt = data.optString(MSTSHNLayout.YOYAKUDT.getId());
			String txt_tenbaikadt = data.optString(MSTSHNLayout.TENBAIKADT.getId());

			String txt_yoyakudt_ = "";		// 検索実行時のﾏｽﾀ変更日
			String txt_tenbaikadt_ = "";	// 検索実行時の店売価実施日
			if(yArray.size() > 0){
				txt_yoyakudt_ = yArray.optJSONObject(0).optString(MSTSHNLayout.YOYAKUDT.getId());			// 検索実行時のﾏｽﾀ変更日
				txt_tenbaikadt_ = yArray.optJSONObject(0).optString(MSTSHNLayout.TENBAIKADT.getId());		// 検索実行時の店売価実施日

			}
			if((NumberUtils.toInt(txt_yoyakudt) != NumberUtils.toInt(txt_yoyakudt_)) && !StringUtils.isEmpty(txt_yoyakudt_)){
				JSONObject o = mu.getDbMessageObj("E11195", new String[]{});
				setCsvshnErrinfo(o, errTbl, MSTSHNLayout.YOYAKUDT, data);
				msg.add(o);
				return msg;
			}
			if((NumberUtils.toInt(txt_tenbaikadt) != NumberUtils.toInt(txt_tenbaikadt_)) && !StringUtils.isEmpty(txt_tenbaikadt_)){
				JSONObject o = mu.getDbMessageObj("E11196", new String[]{});
				setCsvshnErrinfo(o, errTbl, MSTSHNLayout.TENBAIKADT, data);
				msg.add(o);
				return msg;
			}
			if(!this.checkMstExist(DefineReport.InpText.SHNCD.getObj(), txt_shncd)){
				JSONObject o = mu.getDbMessageObj("EX1009", new String[]{});
				setCsvshnErrinfo(o, errTbl, MSTSHNLayout.SHNCD, txt_shncd);
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
					String sqlcommand = "select count(*) as VALUE from INAMS.MSTSRCCD where SRCCD = ?";
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
		// ソースマスタ
		if(isChange && dataArraySRCCD.size() > 0){
			errTbl = RefTable.MSTSRCCD;
			int i;
			JSONObject data = null;
			for (i = 0; i < dataArraySRCCD.size(); i++) {
				data = dataArraySRCCD.optJSONObject(i);
				String seqno = data.optString("F"+MSTSRCCDLayout.SEQNO.getNo());
				if("1".equals(seqno)) {
					break;
				}
			}
			// SEQNO「1」が存在しない場合はエラーとする。
			if(i == dataArraySRCCD.size()) {
				JSONObject o = mu.getDbMessageObj("E11110", new String[]{});
				dao.setCsvshnErrinfo(o, errTbl, MSTSRCCDLayout.SRCCD, data);
				msg.add(o);
				return msg;
			}
		}
		return msg;
	}

	/**
	 * マスタ情報取得処理
	 *
	 * @throws Exception
	 */
	public boolean checkMstExist(String outobj, String value) {
		// 関連情報取得
		ItemList iL = new ItemList();
		// 配列準備
		ArrayList<String> paramData = new ArrayList<String>();
		String sqlcommand = "";
		String dataLength = "";	// 複数データの存在チェックを行う場合に使用

		String tbl = "";
		String col = "";
		String rep = "";
		String whr ="";
		// 商品コード
		if (outobj.equals(DefineReport.InpText.SHNCD.getObj())) {
			tbl="INAMS.MSTSHN_Y";
			col="SHNCD";
			whr = DefineReport.ID_SQL_CMN_WHERE2;
		}

		if(tbl.length()>0&&col.length()>0){
			if(paramData.size() > 0 && rep.length() > 0 ){
				sqlcommand = DefineReport.ID_SQL_CHK_TBL.replace("@T", tbl).replaceAll("@C", col).replace("?", rep) + whr;
			}else{
				paramData.add(value);
				sqlcommand = DefineReport.ID_SQL_CHK_TBL.replace("@T", tbl).replaceAll("@C", col) + whr;
			}

			@SuppressWarnings("static-access")
			JSONArray array = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
			if(array.size() > 0 && NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) > 0){
				if(StringUtils.isNotEmpty(dataLength)){
					if(NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) == Integer.parseInt(dataLength)){
						return true;
					}
				}else{
					return true;
				}

			}
		}
		return false;
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
		if(FileLayout.values().length != eL.get(idxHeader).length && 233 != eL.get(idxHeader).length){
			JSONObject o = MessageUtility.getDbMessageIdObj("E40008", new String[]{});
			msg.add(o);
			return msg;
		}

		// 新規(正) 1.5　商品登録数は商品登録限度数テーブルの登録限度数を超えた場合は、エラー。
		Reportx002Dao dao = new Reportx002Dao(super.JNDIname);
		String sysdate = dao.getSHORIDT();
		JSONArray shnchk_rows = dao.getMstData(DefineReport.ID_SQL_SHN_CHK_UPDATECNT2, new ArrayList<String>(Arrays.asList(sysdate)));
		if(shnchk_rows.size() != 0){
			if (shnchk_rows.getJSONObject(0).containsKey("VALUE")) {
				if (shnchk_rows.getJSONObject(0).getInt("VALUE") < eL.size()) {
					JSONObject o = MessageUtility.getDbMessageIdObj("E11241", new String[]{});
					msg.add(o);
					return msg;
				}
			}
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
			Reportx002Dao dao,
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

		JSONObject option = new JSONObject();


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
		// ⑧正 .削除 → 正 ：Delete処理、ジャ：Insert処理								※予1がある場合不可
		boolean isNew = !isErr && isSeiRep && DefineReport.ValFileUpdkbn.NEW.getVal().equals(fUpdkbn);
		boolean isChange = !isErr && isSeiRep && DefineReport.ValFileUpdkbn.UPD.getVal().equals(fUpdkbn);
		boolean isChangeY1 = !isErr && !isSeiRep && DefineReport.ValFileUpdkbn.YYK.getVal().equals(fUpdkbn);
		boolean isChangeY1Del = !isErr && !isSeiRep && DefineReport.ValFileUpdkbn.YDEL.getVal().equals(fUpdkbn);
		boolean isDelUpd = !isErr && isSeiRep && DefineReport.ValFileUpdkbn.DEL.getVal().equals(fUpdkbn);
		boolean isDel = isSeiRep && DefineReport.ValFileUpdkbn.DEL.getVal().equals(fUpdkbn);

		// ①正 .新規
		if(isNew){
			dao.jnlshn_trankbn = InfTrankbn.INS.getVal();

			// --- 01.商品
			JSONObject result1 = dao.createSqlMSTSHN(userId, sendBtnid, data, TblType.SEI, SqlType.MRG);

			// --- 08.商品コード空き番
			//JSONObject result8 = dao.createSqlSYSSHNCD_AKI(userId, sendBtnid, data, TblType.SEI, SqlType.MRG);

			// --- 09.販売コード付番管理
			//JSONObject result9 = dao.createSqlSYSURICD_FU(userId, sendBtnid, data, TblType.SEI, SqlType.UPD);

			// --- 10.販売コード空き番
			//JSONObject result10 = dao.createSqlSYSURICD_AKI(userId, sendBtnid, data, TblType.SEI, SqlType.UPD);


		// ②正 .変更
		}else if(isChange){
			dao.jnlshn_trankbn = InfTrankbn.UPD.getVal();

			// --- 01.商品
			JSONObject result1 = dao.createSqlMSTSHN(userId, sendBtnid, data, TblType.SEI, SqlType.UPD);

//
//		// ④予1.新規
//		}else if(isNewY1){
//			dao.jnlshn_trankbn = InfTrankbn.INS.getVal();
//			// --- 07.商品_予約
//			JSONObject result1 = dao.createSqlMSTSHN(userId, sendBtnid, data, TblType.YYK, SqlType.MRG);

		// ⑤予1.更新
		}else if(isChangeY1){
			dao.jnlshn_trankbn = InfTrankbn.UPD.getVal();

			// この場合は、予約を削除して追加となる
			// --- 07.商品_予約
			JSONObject result1_ = dao.createSqlMSTSHN_DEL(userId, sendBtnid, data, TblType.YYK, SqlType.DEL);

			// --- 07.商品_予約
			JSONObject result1 = dao.createSqlMSTSHN(userId, sendBtnid, data, TblType.YYK, SqlType.MRG);

		// ⑤予1.削除
		}else if(isChangeY1Del){

			// 更新区分が’D'の場合は削除処理
			dao.jnlshn_trankbn = InfTrankbn.DEL.getVal();

			// --- 07.商品_予約
			JSONObject result1_ = dao.createSqlMSTSHN_DEL(userId, sendBtnid, data, TblType.YYK, SqlType.DEL);

		// ⑧正 .削除
		}else if(isDelUpd){

			dao.jnlshn_trankbn = InfTrankbn.DEL.getVal();

			// --- 01.商品
			JSONObject result1 = dao.createSqlMSTSHN_DEL(userId, sendBtnid, data, TblType.SEI, SqlType.DEL);

		}else if(isErr){

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
			JSONObject result1 = dao.createSqlMSTSHN(userId, sendBtnid, data, TblType.CSV, SqlType.INS);

		}

		// ************ 子テーブル処理 ***********
		// 2004/03/05に、子テーブルの同一項目考慮はなくなった模様
		TblType baseTblType = isNew||isChange ? TblType.SEI :TblType.YYK;

		if(isSeiRep && isErr && !isDel){
			baseTblType = TblType.CSV;

			// --- 02.仕入グループ
			if(dataArrayTENGP1.size() > 0){
				JSONObject result2 = dao.createSqlMSTSIRGPSHN(userId, sendBtnid, dataArrayTENGP1, baseTblType, SqlType.INS);
			}

			// --- 03.売価コントロール
			if(dataArrayTENGP2.size() > 0){
				JSONObject result3 = dao.createSqlMSTBAIKACTL(userId, sendBtnid, dataArrayTENGP2, baseTblType, SqlType.INS);
			}
			// --- 04.ソースコード管理
			if(dataArraySRCCD.size() > 0){
				JSONObject result4 = dao.createSqlMSTSRCCD(userId, sendBtnid, dataArraySRCCD, baseTblType, SqlType.INS);
			}

			// --- 05.添加物
			if(dataArrayTENKABUTSU.size() > 0){
				JSONObject result5 = dao.createSqlMSTTENKABUTSU(userId, sendBtnid, dataArrayTENKABUTSU, baseTblType, SqlType.INS);
			}

			// --- 06.品揃グループ
			if(dataArrayTENGP3.size() > 0){
				JSONObject result6 = dao.createSqlMSTSHINAGP(userId, sendBtnid, dataArrayTENGP3, baseTblType, SqlType.INS);
			}

			// --- 09.店別異部門
			if(dataArrayTENGP4.size() > 0){
				JSONObject result6 = dao.createSqlMSTSHNTENBMN(userId, sendBtnid, dataArrayTENGP4, baseTblType, SqlType.INS);
			}


		}else if(!isErr && !isChangeY1Del && !isDel){
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
				JSONObject result2 = dao.createSqlMSTSIRGPSHN(userId, sendBtnid, dataArrayTENGP1, baseTblType, SqlType.INS);
			}

			// --- 03.売価コントロール
			JSONObject result3D= dao.createSqlMSTBAIKACTL(userId, sendBtnid, dataArrayDel, baseTblType, SqlType.DEL);
			if(dataArrayTENGP2.size() > 0){
				JSONObject result3 = dao.createSqlMSTBAIKACTL(userId, sendBtnid, dataArrayTENGP2, baseTblType, SqlType.INS);
			}
			// --- 04.ソースコード管理
			JSONObject result4D= dao.createSqlMSTSRCCD(userId, sendBtnid, dataArrayDel, baseTblType, SqlType.DEL);
			if(dataArraySRCCD.size() > 0){
				JSONObject result4 = dao.createSqlMSTSRCCD(userId, sendBtnid, dataArraySRCCD, baseTblType, SqlType.INS);
			}

			// --- 05.添加物
			JSONObject result5D= dao.createSqlMSTTENKABUTSU(userId, sendBtnid, dataArrayDelTENKABUTSU, baseTblType, SqlType.DEL);
			if(dataArrayTENKABUTSU.size() > 0){
				JSONObject result5 = dao.createSqlMSTTENKABUTSU(userId, sendBtnid, dataArrayTENKABUTSU, baseTblType, SqlType.INS);
			}

			// --- 06.品揃グループ
			JSONObject result6D= dao.createSqlMSTSHINAGP(userId, sendBtnid, dataArrayDel, baseTblType, SqlType.DEL);
			if(dataArrayTENGP3.size() > 0){
				JSONObject result6 = dao.createSqlMSTSHINAGP(userId, sendBtnid, dataArrayTENGP3, baseTblType, SqlType.INS);
			}

			// --- 09.店別異部門
			JSONObject result9D= dao.createSqlMSTSHNTENBMN(userId, sendBtnid, dataArrayDelMSTSHNTENBMN, baseTblType, SqlType.DEL);
			if(dataArrayTENGP4.size() > 0){
				JSONObject result9 = dao.createSqlMSTSHNTENBMN(userId, sendBtnid, dataArrayTENGP4, baseTblType, SqlType.INS);
			}
		}

		if(!isErr && !isChangeY1Del && !isDel){
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

		if(!isErr && !isChangeY1Del){
			// --- 14.商品更新件数
			JSONObject result14 = dao.createSqlSYSSHNCOUNT(userId, sendBtnid, TblType.SEI, SqlType.MRG);

			// ******** ジャーナル処理（正・予共通） ********
			// ジャーナル用の情報を取得
			dao.jnlshn_seq = jnlshn_seq;
			dao.jnlshn_tablekbn = isNew||isChange ? DefineReport.ValTablekbn.SEI.getVal() : DefineReport.ValTablekbn.YYK.getVal() ;		// 0：正、1：予約

			// --- 15.ジャーナル_商品
			JSONObject result15 = dao.createSqlMSTSHN(userId, sendBtnid, data, TblType.JNL, SqlType.INS);

			// --- 16.ジャーナル_仕入グループ商品
			if(dataArrayTENGP1.size() > 0){
				JSONObject result16 = dao.createSqlMSTSIRGPSHN(userId, sendBtnid, dataArrayTENGP1, TblType.JNL, SqlType.INS);
			}

			// --- 17.ジャーナル_売価コントロール
			if(dataArrayTENGP2.size() > 0){
				JSONObject result17 = dao.createSqlMSTBAIKACTL(userId, sendBtnid, dataArrayTENGP2, TblType.JNL, SqlType.INS);
			}

			// --- 18.ジャーナル_ソースコード管理
			if(dataArraySRCCD.size() > 0){
				JSONObject result18 = dao.createSqlMSTSRCCD(userId, sendBtnid, dataArraySRCCD, TblType.JNL, SqlType.INS);
			}

			// --- 19.ジャーナル_添加物
			if(dataArrayTENKABUTSU.size() > 0){
				JSONObject result19 = dao.createSqlMSTTENKABUTSU(userId, sendBtnid, dataArrayTENKABUTSU, TblType.JNL, SqlType.INS);
			}

			// --- 20.ジャーナル_品揃グループ
			if(dataArrayTENGP3.size() > 0){
				JSONObject result20 = dao.createSqlMSTSHINAGP(userId, sendBtnid, dataArrayTENGP3, TblType.JNL, SqlType.INS);
			}

			// --- 23.ジャーナル_店別異部門
			if(dataArrayTENGP4.size() > 0){
				JSONObject result23 = dao.createSqlMSTSHNTENBMN(userId, sendBtnid, dataArrayTENGP4, TblType.JNL, SqlType.INS);
			}
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
		sbSQL.append("insert into INAMS.CSVSHNHEAD(SEQ, OPERATOR, INPUT_DATE, COMMENTKN)");
		sbSQL.append("values("+values+")");
		if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

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
		for(MSTSHNLayout itm :MSTSHNLayout.values()){
			if(itm.getNo() > 1){ sbSQLIn.append(","); }
			if(data.containsKey(itm.getCol())){
				String value = StringUtils.strip(data.optString(itm.getCol()));
				if(StringUtils.isNotEmpty(value)){
					prmData.add(value);
					sbSQLIn.append("cast(? as varchar("+MessageUtility.getDefByteLen(value)+")) as "+itm.getCol());
				}else{
					sbSQLIn.append("cast(null as " + itm.getTyp() + ") as "+itm.getCol());
				}
			}else{
				sbSQLIn.append("null as "+itm.getCol());
			}
		}
		sbSQLIn.append(" from (SELECT 1 AS DUMMY) DUMMY ");

		// 基本Select文
		StringBuffer sbSQL = new StringBuffer();

		String szTable = "INAMS.MSTSHN";			// 予約の時は予約を削除し、登録を行うので、参照は正となる
		String szWhere = "T.SHNCD = RE.SHNCD";

		sbSQL.append(" select ");
		for(MSTSHNLayout itm :MSTSHNLayout.values()){
			//if(!ArrayUtils.contains(targetCol, itm.getCol())){continue;}
			if(itm.getNo() > 1){ sbSQL.append(","); }
			if(itm.isText()){
				// POP名称の場合
				if (itm.getCol().equals(MSTSHNLayout.POPKN.getCol())) {
					sbSQL.append("CASE WHEN nvl(RE."+MSTSHNLayout.SHNKBN.getCol()+", varchar(T."+MSTSHNLayout.SHNKBN.getCol()+")) = '0' AND ");
					sbSQL.append("trim(COALESCE(RE."+itm.getCol()+", varchar(T."+itm.getCol()+"))) = '' THEN ");
					sbSQL.append("trim(COALESCE(RE."+MSTSHNLayout.SHNKN.getCol()+", varchar(T."+MSTSHNLayout.SHNKN.getCol()+"))) ELSE ");
					sbSQL.append("trim(COALESCE(RE."+itm.getCol()+", varchar(T."+itm.getCol()+"))) END as "+itm.getId());
				} else {
					sbSQL.append("trim(COALESCE(RE."+itm.getCol()+", varchar(T."+itm.getCol()+"))) as "+itm.getId());
				}
			}else{
				sbSQL.append("COALESCE(RE."+itm.getCol()+", varchar(T."+itm.getCol()+")) as "+itm.getId());
			}
		}
		sbSQL.append(" from (" + sbSQLIn.toString() + ") RE");
		sbSQL.append(" left outer join " + szTable  + " T on "+ szWhere);
		if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

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
	private String setSelectCommandMST(boolean isSeiRep, boolean isNew, JSONArray dataArray, dao.Reportx002Dao.MSTLayout[] layouts, ArrayList<String> prmData) {
		String values = "", names = "", rows = "";
		String[] nullSetCol = new String[]{"UPDDT", "ADDDT"};
		for(int j=0; j < dataArray.size(); j++){
			values = "";
			names = "";
			for(dao.Reportx002Dao.MSTLayout itm : layouts){
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
		for(dao.Reportx002Dao.MSTLayout itm :layouts){
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
					sbSQL.append("COALESCE(RE."+itm.getCol()+", varchar(T."+itm.getCol()+")) as "+itm.getId());
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

		String szTable = "INAMS.MSTSRCCD";
		String szWhere = "T.SHNCD = RE.SHNCD";
		szWhere += " and T.SRCCD = RE.SRCCD ";

		ArrayList<String> prmData = new ArrayList<>();
		String sqlCommand = this.setSelectCommandMST(isSeiRep, isNew, dataArray, MSTSRCCDLayout.values(), prmData);
		sqlCommand = sqlCommand.replace("@T", szTable).replace("@W", szWhere);
		if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sqlCommand);

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

		String szTable = "INAMS.MSTSIRGPSHN";
		String szWhere = "T.SHNCD = RE.SHNCD";
		szWhere += " and T.TENGPCD = RE.TENGPCD";

		ArrayList<String> prmData = new ArrayList<>();
		String sqlCommand = this.setSelectCommandMST(isSeiRep, isNew, dataArray, MSTSIRGPSHNLayout.values(), prmData);
		sqlCommand = sqlCommand.replace("@T", szTable).replace("@W", szWhere);
		if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sqlCommand);

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

		String szTable = "INAMS.MSTBAIKACTL";
		String szWhere = "T.SHNCD = RE.SHNCD";
		szWhere += " and T.TENGPCD = RE.TENGPCD";

		ArrayList<String> prmData = new ArrayList<>();
		String sqlCommand = this.setSelectCommandMST(isSeiRep, isNew, dataArray, MSTBAIKACTLLayout.values(), prmData);
		sqlCommand = sqlCommand.replace("@T", szTable).replace("@W", szWhere);
		if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sqlCommand);

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

		String szTable = "INAMS.MSTSHINAGP";
		String szWhere = "T.SHNCD = RE.SHNCD";
		szWhere += " and T.TENGPCD = RE.TENGPCD";

		ArrayList<String> prmData = new ArrayList<>();
		String sqlCommand = this.setSelectCommandMST(isSeiRep, isNew, dataArray, MSTSHINAGPLayout.values(), prmData);
		sqlCommand = sqlCommand.replace("@T", szTable).replace("@W", szWhere);
		if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sqlCommand);

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

		String szTable = "INAMS.MSTTENKABUTSU";
		String szWhere = "T.SHNCD = RE.SHNCD";
		szWhere += " and T.TENKABKBN = RE.TENKABKBN and T.TENKABCD = RE.TENKABCD";

		ArrayList<String> prmData = new ArrayList<>();
		String sqlCommand = this.setSelectCommandMST(isSeiRep, isNew, dataArray, MSTTENKABUTSULayout.values(), prmData);
		sqlCommand = sqlCommand.replace("@T", szTable).replace("@W", szWhere);
		if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sqlCommand);

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
		if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sqlCommand);

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
	public JSONArray selectCompData(JSONObject ShnData, JSONArray dataArray, String tbl, String keyCol, String groupCol, dao.Reportx002Dao.MSTLayout[] layouts) {

		if(dataArray.size()==0){
			return new JSONArray();
		}

		ArrayList<String> prmData = new ArrayList<>();
		String szTable = "INAMS."+tbl;
		String szWhere = "T.SHNCD = RE.SHNCD";
		String szYoyakudt	 = ShnData.optString(MSTSHNLayout.YOYAKUDT.getCol());

		// 更新を行わない子テーブルデータ(CSVにNullで設定されている場合)を追加する。
		dataArray = this.addNotUpdateData(dataArray, tbl, groupCol,layouts,szYoyakudt);

		// 結合キーの追加
		if(StringUtils.equals(RefTable.MSTSRCCD.getId(), tbl)){
			// ソースコードマスタ情報取得処理
			szWhere += " and T.SRCCD = RE.SRCCD ";

		}else if(StringUtils.equals(RefTable.MSTSIRGPSHN.getId(), tbl)){
			// 仕入グループ商品マスタ
			szWhere += " and T.TENGPCD = RE.TENGPCD";

		}else if(StringUtils.equals(RefTable.MSTBAIKACTL.getId(), tbl)){
			// 売価コントロールマスタ情報取得処理
			szWhere += " and T.TENGPCD = RE.TENGPCD";

		}else if(StringUtils.equals(RefTable.MSTSHINAGP.getId(), tbl)){
			// 品揃グループマスタ情報取得処理
			szWhere += " and T.TENGPCD = RE.TENGPCD";

		}else if(StringUtils.equals(RefTable.MSTTENKABUTSU.getId(), tbl)){
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
			for(dao.Reportx002Dao.MSTLayout itm : layouts){
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
		for(dao.Reportx002Dao.MSTLayout itm :layouts){
			if(itm.getNo() > 1){ sbSQL.append(","); }
			if(itm.isText()){
				sbSQL.append("trim(COALESCE(RE."+itm.getCol()+", varchar(T."+itm.getCol()+"))) as "+itm.getCol());
			}else{
				sbSQL.append("COALESCE(RE."+itm.getCol()+", varchar(T."+itm.getCol()+")) as "+itm.getCol());
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
		if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sqlCommand);

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
	public JSONArray addNotUpdateData(JSONArray dataArray, String tbl, String groupCol, dao.Reportx002Dao.MSTLayout[] layouts, String yoyakudt) {

		ArrayList<String> prmData = new ArrayList<>();
		StringBuffer sbSQL = new StringBuffer();
		ArrayList<String> pkeyCol  = new ArrayList<>();
		ArrayList<String> pkeyVal  = new ArrayList<>();

		String szShncd = "";
		String szCol = "SHNCD, ";
		String szTable = "INAMS."+tbl;
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

		if(StringUtils.equals(RefTable.MSTSIRGPSHN.getId(), tbl)){
			// 仕入グループ商品マスタ
			pkeyCol.add("TENGPCD");

		}else if(StringUtils.equals(RefTable.MSTBAIKACTL.getId(), tbl)){
			// 売価コントロールマスタ情報取得処理
			pkeyCol.add("TENGPCD");

		}else if(StringUtils.equals(RefTable.MSTSHINAGP.getId(), tbl)){
			// 品揃グループマスタ情報取得処理
			pkeyCol.add("TENGPCD");

		}else if(StringUtils.equals(RefTable.MSTTENKABUTSU.getId(), tbl)){
			// 添加物マスタ情報取得処理
			pkeyCol.add("TENKABKBN");
			pkeyCol.add("TENKABCD");

		}else if(StringUtils.equals(RefTable.MSTSHNTENBMN.getId(), tbl)){
			// 店別異部門管理情報取得処理
			pkeyCol.add("TENSHNCD");
			pkeyCol.add("TENGPCD");
		}

		if(pkeyCol.size() == 0){
			if(StringUtils.equals(RefTable.MSTSRCCD.getId(), tbl)){
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
		for(dao.Reportx002Dao.MSTLayout itm :layouts){
			String col = itm.getCol();
			String val = "T."+itm.getCol();
			if(StringUtils.equals(itm.getCol(), Reportx002Dao.MSTSHNLayout.YOYAKUDT.getCol())){
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
		if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sqlCommand);

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

	/** マスタレイアウト */
	public interface MSTLayout {
		public Integer getNo();
		public String getCol();
		public String getTyp();
		public String getTxt();
		public String getId();
		public DataType getDataType();
		public boolean isText();
	}

	/**  商品マスタレイアウト() */
	public enum MSTSHNLayout implements MSTLayout{
		/** 商品コード */
		SHNCD(1,"SHNCD","CHARACTER(14)","商品コード"),
		/** マスタ変更予定日 */
		YOYAKUDT(2,"YOYAKUDT","INTEGER","マスタ変更予定日"),
		/** 店売価実施日 */
		TENBAIKADT(3,"TENBAIKADT","INTEGER","店売価実施日"),
		/** 用途分類コード_部門 */
		YOT_BMNCD(4,"YOT_BMNCD","SMALLINT","用途分類コード_部門"),
		/** 用途分類コード_大 */
		YOT_DAICD(5,"YOT_DAICD","SMALLINT","用途分類コード_大"),
		/** 用途分類コード_中 */
		YOT_CHUCD(6,"YOT_CHUCD","SMALLINT","用途分類コード_中"),
		/** 用途分類コード_小 */
		YOT_SHOCD(7,"YOT_SHOCD","SMALLINT","用途分類コード_小"),
		/** 売場分類コード_部門 */
		URI_BMNCD(8,"URI_BMNCD","SMALLINT","売場分類コード_部門"),
		/** 売場分類コード_大 */
		URI_DAICD(9,"URI_DAICD","SMALLINT","売場分類コード_大"),
		/** 売場分類コード_中 */
		URI_CHUCD(10,"URI_CHUCD","SMALLINT","売場分類コード_中"),
		/** 売場分類コード_小 */
		URI_SHOCD(11,"URI_SHOCD","SMALLINT","売場分類コード_小"),
		/** 標準分類コード_部門 */
		BMNCD(12,"BMNCD","SMALLINT","標準分類コード_部門"),
		/** 標準分類コード_大 */
		DAICD(13,"DAICD","SMALLINT","標準分類コード_大"),
		/** 標準分類コード_中 */
		CHUCD(14,"CHUCD","SMALLINT","標準分類コード_中"),
		/** 標準分類コード_小 */
		SHOCD(15,"SHOCD","SMALLINT","標準分類コード_小"),
		/** 標準分類コード_小小 */
		SSHOCD(16,"SSHOCD","SMALLINT","標準分類コード_小小"),
		/** 取扱期間_開始日 */
		ATSUK_STDT(17,"ATSUK_STDT","INTEGER","取扱期間_開始日"),
		/** 取扱期間_終了日 */
		ATSUK_EDDT(18,"ATSUK_EDDT","INTEGER","取扱期間_終了日"),
		/** 取扱停止 */
		TEISHIKBN(19,"TEISHIKBN","SMALLINT","取扱停止"),
		/** 商品名（カナ） */
		SHNAN(20,"SHNAN","VARCHAR(20)","商品名（カナ）"),
		/** 商品名（漢字） */
		SHNKN(21,"SHNKN","VARCHAR(40)","商品名（漢字）"),
		/** プライスカード商品名称（漢字） */
		PCARDKN(22,"PCARDKN","VARCHAR(40)","プライスカード商品名称（漢字）"),
		/** POP名称 */
		POPKN(23,"POPKN","VARCHAR(40)","POP名称"),
		/** レシート名（カナ） */
		RECEIPTAN(24,"RECEIPTAN","VARCHAR(20)","レシート名（カナ）"),
		/** レシート名（漢字） */
		RECEIPTKN(25,"RECEIPTKN","VARCHAR(40)","レシート名（漢字）"),
		/** PC区分 */
		PCKBN(26,"PCKBN","SMALLINT","PC区分"),
		/** 加工区分 */
		KAKOKBN(27,"KAKOKBN","SMALLINT","加工区分"),
		/** 市場区分 */
		ICHIBAKBN(28,"ICHIBAKBN","SMALLINT","市場区分"),
		/** 商品種類 */
		SHNKBN(29,"SHNKBN","SMALLINT","商品種類"),
		/** 産地 */
		SANCHIKN(30,"SANCHIKN","VARCHAR(40)","産地"),
		/** 標準仕入先コード */
		SSIRCD(31,"SSIRCD","INTEGER","標準仕入先コード"),
		/** 配送パターン */
		HSPTN(32,"HSPTN","SMALLINT","配送パターン"),
		/** レギュラー情報_取扱フラグ */
		RG_ATSUKFLG(33,"RG_ATSUKFLG","SMALLINT","レギュラー情報_取扱フラグ"),
		/** レギュラー情報_原価 */
		RG_GENKAAM(34,"RG_GENKAAM","DECIMAL(8,2)","レギュラー情報_原価"),
		/** レギュラー情報_売価 */
		RG_BAIKAAM(35,"RG_BAIKAAM","INTEGER","レギュラー情報_売価"),
		/** レギュラー情報_店入数 */
		RG_IRISU(36,"RG_IRISU","SMALLINT","レギュラー情報_店入数"),
		/** レギュラー情報_一括伝票フラグ */
		RG_IDENFLG(37,"RG_IDENFLG","CHARACTER(1)","レギュラー情報_一括伝票フラグ"),
		/** レギュラー情報_ワッペン */
		RG_WAPNFLG(38,"RG_WAPNFLG","CHARACTER(1)","レギュラー情報_ワッペン"),
		/** 販促情報_取扱フラグ */
		HS_ATSUKFLG(39,"HS_ATSUKFLG","SMALLINT","販促情報_取扱フラグ"),
		/** 販促情報_原価 */
		HS_GENKAAM(40,"HS_GENKAAM","DECIMAL(8,2)","販促情報_原価"),
		/** 販促情報_売価 */
		HS_BAIKAAM(41,"HS_BAIKAAM","INTEGER","販促情報_売価"),
		/** 販促情報_店入数 */
		HS_IRISU(42,"HS_IRISU","SMALLINT","販促情報_店入数"),
		/** 販促情報_ワッペン */
		HS_WAPNFLG(43,"HS_WAPNFLG","CHARACTER(1)","販促情報_ワッペン"),
		/** 販促情報_スポット最低発注数 */
		HS_SPOTMINSU(44,"HS_SPOTMINSU","SMALLINT","販促情報_スポット最低発注数"),
		/** 販促情報_特売ワッペン */
		HP_SWAPNFLG(45,"HP_SWAPNFLG","CHARACTER(1)","販促情報_特売ワッペン"),
		/** 規格名称 */
		KIKKN(46,"KIKKN","VARCHAR(46)","規格名称"),
		/** ユニットプライス_容量 */
		UP_YORYOSU(47,"UP_YORYOSU","INTEGER","ユニットプライス_容量"),
		/** ユニットプライス_単位容量 */
		UP_TYORYOSU(48,"UP_TYORYOSU","SMALLINT","ユニットプライス_単位容量"),
		/** ユニットプライス_ユニット単位 */
		UP_TANIKBN(49,"UP_TANIKBN","SMALLINT","ユニットプライス_ユニット単位"),
		/** 商品サイズ_横 */
		SHNYOKOSZ(50,"SHNYOKOSZ","SMALLINT","商品サイズ_横"),
		/** 商品サイズ_縦 */
		SHNTATESZ(51,"SHNTATESZ","SMALLINT","商品サイズ_縦"),
		/** 商品サイズ_奥行 */
		SHNOKUSZ(52,"SHNOKUSZ","SMALLINT","商品サイズ_奥行"),
		/** 商品サイズ_重量 */
		SHNJRYOSZ(53,"SHNJRYOSZ","DECIMAL(6,1)","商品サイズ_重量"),
		/** PB区分 */
		PBKBN(54,"PBKBN","SMALLINT","PB区分"),
		/** 小物区分 */
		KOMONOKBM(55,"KOMONOKBM","SMALLINT","小物区分"),
		/** 棚卸区分 */
		TANAOROKBN(56,"TANAOROKBN","SMALLINT","棚卸区分"),
		/** 定計区分 */
		TEIKEIKBN(57,"TEIKEIKBN","SMALLINT","定計区分"),
		/** ODS_賞味期限_春 */
		ODS_HARUSU(58,"ODS_HARUSU","SMALLINT","ODS_賞味期限_春"),
		/** ODS_賞味期限_夏 */
		ODS_NATSUSU(59,"ODS_NATSUSU","SMALLINT","ODS_賞味期限_夏"),
		/** ODS_賞味期限_秋 */
		ODS_AKISU(60,"ODS_AKISU","SMALLINT","ODS_賞味期限_秋"),
		/** ODS_賞味期限_冬 */
		ODS_FUYUSU(61,"ODS_FUYUSU","SMALLINT","ODS_賞味期限_冬"),
		/** ODS_入荷期限 */
		ODS_NYUKASU(62,"ODS_NYUKASU","SMALLINT","ODS_入荷期限"),
		/** ODS_値引期限 */
		ODS_NEBIKISU(63,"ODS_NEBIKISU","SMALLINT","ODS_値引期限"),
		/** プライスカード_種類 */
		PCARD_SHUKBN(64,"PCARD_SHUKBN","SMALLINT","プライスカード_種類"),
		/** プライスカード_色 */
		PCARD_IROKBN(65,"PCARD_IROKBN","SMALLINT","プライスカード_色"),
		/** 税区分 */
		ZEIKBN(66,"ZEIKBN","SMALLINT","税区分"),
		/** 税率区分 */
		ZEIRTKBN(67,"ZEIRTKBN","SMALLINT","税率区分"),
		/** 旧税率区分 */
		ZEIRTKBN_OLD(68,"ZEIRTKBN_OLD","SMALLINT","旧税率区分"),
		/** 税率変更日 */
		ZEIRTHENKODT(69,"ZEIRTHENKODT","INTEGER","税率変更日"),
		/** 製造限度日数 */
		SEIZOGENNISU(70,"SEIZOGENNISU","SMALLINT","製造限度日数"),
		/** 定貫不定貫区分 */
		TEIKANKBN(71,"TEIKANKBN","SMALLINT","定貫不定貫区分"),
		/** メーカーコード */
		MAKERCD(72,"MAKERCD","INTEGER","メーカーコード"),
		/** 輸入区分 */
		IMPORTKBN(73,"IMPORTKBN","SMALLINT","輸入区分"),
		/** 仕分区分 */
		SIWAKEKBN(74,"SIWAKEKBN","SMALLINT","仕分区分"),
		/** 返品区分 */
		HENPIN_KBN(75,"HENPIN_KBN","SMALLINT","返品区分"),
		/** 対象年齢 */
		TAISHONENSU(76,"TAISHONENSU","SMALLINT","対象年齢"),
		/** カロリー表示 */
		CALORIESU(77,"CALORIESU","SMALLINT","カロリー表示"),
		/** フラグ情報_ELP */
		ELPFLG(78,"ELPFLG","SMALLINT","フラグ情報_ELP"),
		/** フラグ情報_ベルマーク */
		BELLMARKFLG(79,"BELLMARKFLG","SMALLINT","フラグ情報_ベルマーク"),
		/** フラグ情報_リサイクル */
		RECYCLEFLG(80,"RECYCLEFLG","SMALLINT","フラグ情報_リサイクル"),
		/** フラグ情報_エコマーク */
		ECOFLG(81,"ECOFLG","SMALLINT","フラグ情報_エコマーク"),
		/** 包材用途 */
		HZI_YOTO(82,"HZI_YOTO","SMALLINT","包材用途"),
		/** 包材材質 */
		HZI_ZAISHITU(83,"HZI_ZAISHITU","SMALLINT","包材材質"),
		/** 包材リサイクル対象 */
		HZI_RECYCLE(84,"HZI_RECYCLE","SMALLINT","包材リサイクル対象"),
		/** 期間 */
		KIKANKBN(85,"KIKANKBN","CHARACTER(1)","期間"),
		/** 酒級 */
		SHUKYUKBN(86,"SHUKYUKBN","SMALLINT","酒級"),
		/** 度数 */
		DOSU(87,"DOSU","SMALLINT","度数"),
		/** 陳列形式コード */
		CHINRETUCD(88,"CHINRETUCD","CHARACTER(1)","陳列形式コード"),
		/** 段積み形式コード */
		DANTUMICD(89,"DANTUMICD","CHARACTER(2)","段積み形式コード"),
		/** 重なりコード */
		KASANARICD(90,"KASANARICD","CHARACTER(1)","重なりコード"),
		/** 重なりサイズ */
		KASANARISZ(91,"KASANARISZ","SMALLINT","重なりサイズ"),
		/** 圧縮率 */
		ASSHUKURT(92,"ASSHUKURT","SMALLINT","圧縮率"),
		/** 種別コード */
		SHUBETUCD(93,"SHUBETUCD","CHARACTER(2)","種別コード"),
		/** 販売コード */
		URICD(94,"URICD","INTEGER","販売コード"),
		/** 商品コピー・セールスコメント */
		SALESCOMKN(95,"SALESCOMKN","VARCHAR(60)","商品コピー・セールスコメント"),
		/** 裏貼 */
		URABARIKBN(96,"URABARIKBN","SMALLINT","裏貼"),
		/** プライスカード出力有無 */
		PCARD_OPFLG(97,"PCARD_OPFLG","SMALLINT","プライスカード出力有無"),
		/** 親商品コード */
		PARENTCD(98,"PARENTCD","CHARACTER(14)","親商品コード"),
		/** 便区分 */
		BINKBN(99,"BINKBN","SMALLINT","便区分"),
		/** 発注曜日_月 */
		HAT_MONKBN(100,"HAT_MONKBN","SMALLINT","発注曜日_月"),
		/** 発注曜日_火 */
		HAT_TUEKBN(101,"HAT_TUEKBN","SMALLINT","発注曜日_火"),
		/** 発注曜日_水 */
		HAT_WEDKBN(102,"HAT_WEDKBN","SMALLINT","発注曜日_水"),
		/** 発注曜日_木 */
		HAT_THUKBN(103,"HAT_THUKBN","SMALLINT","発注曜日_木"),
		/** 発注曜日_金 */
		HAT_FRIKBN(104,"HAT_FRIKBN","SMALLINT","発注曜日_金"),
		/** 発注曜日_土 */
		HAT_SATKBN(105,"HAT_SATKBN","SMALLINT","発注曜日_土"),
		/** 発注曜日_日 */
		HAT_SUNKBN(106,"HAT_SUNKBN","SMALLINT","発注曜日_日"),
		/** リードタイムパターン */
		READTMPTN(107,"READTMPTN","SMALLINT","リードタイムパターン"),
		/** 締め回数 */
		SIMEKAISU(108,"SIMEKAISU","SMALLINT","締め回数"),
		/** 衣料使い回しフラグ */
		IRYOREFLG(109,"IRYOREFLG","SMALLINT","衣料使い回しフラグ"),
		/** 登録元 */
		TOROKUMOTO(110,"TOROKUMOTO","SMALLINT","登録元"),
		/** 更新区分 */
		UPDKBN(111,"UPDKBN","SMALLINT","更新区分"),
		/** 送信フラグ */
		SENDFLG(112,"SENDFLG","SMALLINT","送信フラグ"),
		/** オペレータ */
		OPERATOR(113,"OPERATOR","VARCHAR(20)","オペレータ"),
		/** 登録日 */
		ADDDT(114,"ADDDT","TIMESTAMP","登録日"),
		/** 更新日 */
		UPDDT(115,"UPDDT","TIMESTAMP","更新日"),
		/** 保温区分 */
		K_HONKB(116,"K_HONKB","SMALLINT","保温区分"),
		/** デリカワッペン区分_レギュラー */
		K_WAPNFLG_R(117,"K_WAPNFLG_R","SMALLINT","デリカワッペン区分_レギュラー"),
		/** デリカワッペン区分_販促 */
		K_WAPNFLG_H(118,"K_WAPNFLG_H","SMALLINT","デリカワッペン区分_販促"),
		/** 取扱区分 */
		K_TORIKB(119,"K_TORIKB","SMALLINT","取扱区分"),
		/** ITFコード */
		ITFCD(120,"ITFCD","CHARACTER(14)","ITFコード"),
		/** センター入数 */
		CENTER_IRISU(121,"CENTER_IRISU","SMALLINT","センター入数");

		private final Integer no;
		private final String col;
		private final String txt;
		private final String typ;
		/** 初期化 */
		private MSTSHNLayout(Integer no, String col, String typ, String txt) {
			this.no = no;
			this.col = col;
			this.typ = typ;
			this.txt = txt;
		}
		/** @return col 列番号 */
		public Integer getNo() { return no; }
		/** @return tbl 列名 */
		public String getCol() { return col; }
		/** @return typ 列型 */
		public String getTyp() { return typ; }
		/** @return txt 論理名 */
		public String getTxt() { return txt; }
		/** @return col Id */
		public String getId() { return "F" + Integer.toString(no); }

		/** @return datatype データ型のみ */
		public DataType getDataType() {
			if(typ.indexOf("INT") != -1){ return DefineReport.DataType.INTEGER;}
			if(typ.indexOf("DEC") != -1){ return DefineReport.DataType.DECIMAL;}
			if(typ.indexOf("DATE")!= -1||typ.indexOf("TIMESTAMP")!= -1){ return DefineReport.DataType.DATE;}
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

	/**  Fileレイアウト */
	public enum FileLayout {
		/** 更新区分 */
		UPDKBN(1,"更新区分",RefTable.OTHER,"UPDKBN"),
		/** 商品コード */
		SHNCD(2,"商品コード",RefTable.MSTSHN,"SHNCD"),
		/** 商品コード桁数指定 */
		KETAKBN(3,"商品コード桁数指定",RefTable.OTHER,"KETAKBN"),
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
		/** 親商品コード */
		PARENTCD(16,"親商品コード",RefTable.MSTSHN,"PARENTCD"),
		/** 商品名（カナ） */
		SHNAN(17,"商品名（カナ）",RefTable.MSTSHN,"SHNAN"),
		/** 商品名（漢字） */
		SHNKN(18,"商品名（漢字）",RefTable.MSTSHN,"SHNKN"),
		/** レシート名（カナ） */
		RECEIPTAN(19,"レシート名（カナ）",RefTable.MSTSHN,"RECEIPTAN"),
		/** レシート名（漢字） */
		RECEIPTKN(20,"レシート名（漢字）",RefTable.MSTSHN,"RECEIPTKN"),
		/** プライスカード商品名称（漢字） */
		PCARDKN(21,"プライスカード商品名称（漢字）",RefTable.MSTSHN,"PCARDKN"),
		/** 商品コメント・セールスコピー（漢字） */
		SALESCOMKN(22,"商品コメント・セールスコピー（漢字）",RefTable.MSTSHN,"SALESCOMKN"),
		/** ＰＯＰ名称（漢字） */
		POPKN(23,"ＰＯＰ名称（漢字）",RefTable.MSTSHN,"POPKN"),
		/** 規格 */
		KIKKN(24,"規格",RefTable.MSTSHN,"KIKKN"),
		/** レギュラー情報_取扱フラグ */
		RG_ATSUKFLG(25,"レギュラー情報_取扱フラグ",RefTable.MSTSHN,"RG_ATSUKFLG"),
		/** レギュラー情報_原価 */
		RG_GENKAAM(26,"レギュラー情報_原価",RefTable.MSTSHN,"RG_GENKAAM"),
		/** レギュラー情報_売価 */
		RG_BAIKAAM(27,"レギュラー情報_売価",RefTable.MSTSHN,"RG_BAIKAAM"),
		/** レギュラー情報_店入数 */
		RG_IRISU(28,"レギュラー情報_店入数",RefTable.MSTSHN,"RG_IRISU"),
		/** レギュラー情報_一括伝票ﾌﾗｸﾞ */
		RG_IDENFLG(29,"レギュラー情報_一括伝票ﾌﾗｸﾞ",RefTable.MSTSHN,"RG_IDENFLG"),
		/** レギュラー情報_ワッペン */
		RG_WAPNFLG(30,"レギュラー情報_ワッペン",RefTable.MSTSHN,"RG_WAPNFLG"),
		/** 販促情報_取扱フラグ */
		HS_ATSUKFLG(31,"販促情報_取扱フラグ",RefTable.MSTSHN,"HS_ATSUKFLG"),
		/** 販促情報_原価 */
		HS_GENKAAM(32,"販促情報_原価",RefTable.MSTSHN,"HS_GENKAAM"),
		/** 販促情報_売価 */
		HS_BAIKAAM(33,"販促情報_売価",RefTable.MSTSHN,"HS_BAIKAAM"),
		/** 販促情報_店入数 */
		HS_IRISU(34,"販促情報_店入数",RefTable.MSTSHN,"HS_IRISU"),
		/** 販促情報_ワッペン */
		HS_WAPNFLG(35,"販促情報_ワッペン",RefTable.MSTSHN,"HS_WAPNFLG"),
		/** 販促情報_特売ワッペン */
		HP_SWAPNFLG(36,"販促情報_特売ワッペン",RefTable.MSTSHN,"HP_SWAPNFLG"),
		/** 便区分 */
		BINKBN(37,"便区分",RefTable.MSTSHN,"BINKBN"),
		/** 締め回数 */
		SIMEKAISU(38,"締め回数",RefTable.MSTSHN,"SIMEKAISU"),
		/** 小物区分 */
		KOMONOKBM(39,"小物区分",RefTable.MSTSHN,"KOMONOKBM"),
		/** 仕分区分 */
		SIWAKEKBN(40,"仕分区分",RefTable.MSTSHN,"SIWAKEKBN"),
		/** 棚卸区分 */
		TANAOROKBN(41,"棚卸区分",RefTable.MSTSHN,"TANAOROKBN"),
		/** 期間 */
		KIKANKBN(42,"期間",RefTable.MSTSHN,"KIKANKBN"),
		/** ＯＤＳ_賞味期限_春 */
		ODS_HARUSU(43,"ＯＤＳ_賞味期限_春",RefTable.MSTSHN,"ODS_HARUSU"),
		/** ＯＤＳ_賞味期限_夏 */
		ODS_NATSUSU(44,"ＯＤＳ_賞味期限_夏",RefTable.MSTSHN,"ODS_NATSUSU"),
		/** ＯＤＳ_賞味期限_秋 */
		ODS_AKISU(45,"ＯＤＳ_賞味期限_秋",RefTable.MSTSHN,"ODS_AKISU"),
		/** ＯＤＳ_賞味期限_冬 */
		ODS_FUYUSU(46,"ＯＤＳ_賞味期限_冬",RefTable.MSTSHN,"ODS_FUYUSU"),
		/** ＯＤＳ_入荷期限 */
		ODS_NYUKASU(47,"ＯＤＳ_入荷期限",RefTable.MSTSHN,"ODS_NYUKASU"),
		/** ＯＤＳ_値引開始 */
		ODS_NEBIKISU(48,"ＯＤＳ_値引開始",RefTable.MSTSHN,"ODS_NEBIKISU"),
		/** 販促情報_スポット最低発注数 */
		HS_SPOTMINSU(49,"販促情報_スポット最低発注数",RefTable.MSTSHN,"HS_SPOTMINSU"),
		/** 製造限度日数 */
		SEIZOGENNISU(50,"製造限度日数",RefTable.MSTSHN,"SEIZOGENNISU"),
		/** リードタイムパターン */
		READTMPTN(51,"リードタイムパターン",RefTable.MSTSHN,"READTMPTN"),
		/** 発注曜日_月 */
		HAT_MONKBN(52,"発注曜日_月",RefTable.MSTSHN,"HAT_MONKBN"),
		/** 発注曜日_火 */
		HAT_TUEKBN(53,"発注曜日_火",RefTable.MSTSHN,"HAT_TUEKBN"),
		/** 発注曜日_水 */
		HAT_WEDKBN(54,"発注曜日_水",RefTable.MSTSHN,"HAT_WEDKBN"),
		/** 発注曜日_木 */
		HAT_THUKBN(55,"発注曜日_木",RefTable.MSTSHN,"HAT_THUKBN"),
		/** 発注曜日_金 */
		HAT_FRIKBN(56,"発注曜日_金",RefTable.MSTSHN,"HAT_FRIKBN"),
		/** 発注曜日_土 */
		HAT_SATKBN(57,"発注曜日_土",RefTable.MSTSHN,"HAT_SATKBN"),
		/** 発注曜日_日 */
		HAT_SUNKBN(58,"発注曜日_日",RefTable.MSTSHN,"HAT_SUNKBN"),
		/** 配送パターン */
		HSPTN(59,"配送パターン",RefTable.MSTSHN,"HSPTN"),
		/** ユニットプライス_容量 */
		UP_YORYOSU(60,"ユニットプライス_容量",RefTable.MSTSHN,"UP_YORYOSU"),
		/** ユニットプライス_単位容量 */
		UP_TYORYOSU(61,"ユニットプライス_単位容量",RefTable.MSTSHN,"UP_TYORYOSU"),
		/** ユニットプライス_ユニット単位 */
		UP_TANIKBN(62,"ユニットプライス_ユニット単位",RefTable.MSTSHN,"UP_TANIKBN"),
		/** 商品サイズ_縦 */
		SHNTATESZ(63,"商品サイズ_縦",RefTable.MSTSHN,"SHNTATESZ"),
		/** 商品サイズ_横 */
		SHNYOKOSZ(64,"商品サイズ_横",RefTable.MSTSHN,"SHNYOKOSZ"),
		/** 商品サイズ_奥行 */
		SHNOKUSZ(65,"商品サイズ_奥行",RefTable.MSTSHN,"SHNOKUSZ"),
		/** 商品サイズ_重量 */
		SHNJRYOSZ(66,"商品サイズ_重量",RefTable.MSTSHN,"SHNJRYOSZ"),
		/** 取扱期間_開始日 */
		ATSUK_STDT(67,"取扱期間_開始日",RefTable.MSTSHN,"ATSUK_STDT"),
		/** 取扱期間_終了日 */
		ATSUK_EDDT(68,"取扱期間_終了日",RefTable.MSTSHN,"ATSUK_EDDT"),
		/** 陳列形式コード */
		CHINRETUCD(69,"陳列形式コード",RefTable.MSTSHN,"CHINRETUCD"),
		/** 段積み形式コード */
		DANTUMICD(70,"段積み形式コード",RefTable.MSTSHN,"DANTUMICD"),
		/** 重なりコード */
		KASANARICD(71,"重なりコード",RefTable.MSTSHN,"KASANARICD"),
		/** 重なりサイズ */
		KASANARISZ(72,"重なりサイズ",RefTable.MSTSHN,"KASANARISZ"),
		/** 圧縮率 */
		ASSHUKURT(73,"圧縮率",RefTable.MSTSHN,"ASSHUKURT"),
		/** マスタ変更予定日 */
		YOYAKUDT(74,"マスタ変更予定日",RefTable.MSTSHN,"YOYAKUDT"),
		/** 店売価実施日 */
		TENBAIKADT(75,"店売価実施日",RefTable.MSTSHN,"TENBAIKADT"),
		/** 用途分類コード_部門 */
		YOT_BMNCD(76,"用途分類コード_部門",RefTable.MSTSHN,"YOT_BMNCD"),
		/** 用途分類コード_大 */
		YOT_DAICD(77,"用途分類コード_大",RefTable.MSTSHN,"YOT_DAICD"),
		/** 用途分類コード_中 */
		YOT_CHUCD(78,"用途分類コード_中",RefTable.MSTSHN,"YOT_CHUCD"),
		/** 用途分類コード_小 */
		YOT_SHOCD(79,"用途分類コード_小",RefTable.MSTSHN,"YOT_SHOCD"),
		/** 売場分類コード_部門 */
		URI_BMNCD(80,"売場分類コード_部門",RefTable.MSTSHN,"URI_BMNCD"),
		/** 売場分類コード_大 */
		URI_DAICD(81,"売場分類コード_大",RefTable.MSTSHN,"URI_DAICD"),
		/** 売場分類コード_中 */
		URI_CHUCD(82,"売場分類コード_中",RefTable.MSTSHN,"URI_CHUCD"),
		/** 売場分類コード_小 */
		URI_SHOCD(83,"売場分類コード_小",RefTable.MSTSHN,"URI_SHOCD"),

		/** エリア区分（仕入） */
		AREAKBN1(84,"エリア区分（仕入）",RefTable.MSTSIRGPSHN,"AREAKBN"),
		/** 店グループ（エリア）_1（仕入） */
		TENGPCD1_1(85,"店グループ（エリア）_1（仕入）",RefTable.MSTSIRGPSHN,"TENGPCD"),
		/** 仕入先コード_1（仕入） */
		SIRCD1_1(86,"仕入先コード_1（仕入）",RefTable.MSTSIRGPSHN,"SIRCD"),
		/** 配送パターン_1（仕入） */
		HSPTN1_1(87,"配送パターン_1（仕入）",RefTable.MSTSIRGPSHN,"HSPTN"),
		/** 店グループ（エリア）_2（仕入） */
		TENGPCD1_2(88,"店グループ（エリア）_2（仕入）",RefTable.MSTSIRGPSHN,"TENGPCD"),
		/** 仕入先コード_2（仕入） */
		SIRCD1_2(89,"仕入先コード_2（仕入）",RefTable.MSTSIRGPSHN,"SIRCD"),
		/** 配送パターン_2（仕入） */
		HSPTN1_2(90,"配送パターン_2（仕入）",RefTable.MSTSIRGPSHN,"HSPTN"),
		/** 店グループ（エリア）_3（仕入） */
		TENGPCD1_3(91,"店グループ（エリア）_3（仕入）",RefTable.MSTSIRGPSHN,"TENGPCD"),
		/** 仕入先コード_3（仕入） */
		SIRCD1_3(92,"仕入先コード_3（仕入）",RefTable.MSTSIRGPSHN,"SIRCD"),
		/** 配送パターン_3（仕入） */
		HSPTN1_3(93,"配送パターン_3（仕入）",RefTable.MSTSIRGPSHN,"HSPTN"),
		/** 店グループ（エリア）_4（仕入） */
		TENGPCD1_4(94,"店グループ（エリア）_4（仕入）",RefTable.MSTSIRGPSHN,"TENGPCD"),
		/** 仕入先コード_4（仕入） */
		SIRCD1_4(95,"仕入先コード_4（仕入）",RefTable.MSTSIRGPSHN,"SIRCD"),
		/** 配送パターン_4（仕入） */
		HSPTN1_4(96,"配送パターン_4（仕入）",RefTable.MSTSIRGPSHN,"HSPTN"),
		/** 店グループ（エリア）_5（仕入） */
		TENGPCD1_5(97,"店グループ（エリア）_5（仕入）",RefTable.MSTSIRGPSHN,"TENGPCD"),
		/** 仕入先コード_5（仕入） */
		SIRCD1_5(98,"仕入先コード_5（仕入）",RefTable.MSTSIRGPSHN,"SIRCD"),
		/** 配送パターン_5（仕入） */
		HSPTN1_5(99,"配送パターン_5（仕入）",RefTable.MSTSIRGPSHN,"HSPTN"),
		/** 店グループ（エリア）_6（仕入） */
		TENGPCD1_6(100,"店グループ（エリア）_6（仕入）",RefTable.MSTSIRGPSHN,"TENGPCD"),
		/** 仕入先コード_6（仕入） */
		SIRCD1_6(101,"仕入先コード_6（仕入）",RefTable.MSTSIRGPSHN,"SIRCD"),
		/** 配送パターン_6（仕入） */
		HSPTN1_6(102,"配送パターン_6（仕入）",RefTable.MSTSIRGPSHN,"HSPTN"),
		/** 店グループ（エリア）_7（仕入） */
		TENGPCD1_7(103,"店グループ（エリア）_7（仕入）",RefTable.MSTSIRGPSHN,"TENGPCD"),
		/** 仕入先コード_7（仕入） */
		SIRCD1_7(104,"仕入先コード_7（仕入）",RefTable.MSTSIRGPSHN,"SIRCD"),
		/** 配送パターン_7（仕入） */
		HSPTN1_7(105,"配送パターン_7（仕入）",RefTable.MSTSIRGPSHN,"HSPTN"),
		/** 店グループ（エリア）_8（仕入） */
		TENGPCD1_8(106,"店グループ（エリア）_8（仕入）",RefTable.MSTSIRGPSHN,"TENGPCD"),
		/** 仕入先コード_8（仕入） */
		SIRCD1_8(107,"仕入先コード_8（仕入）",RefTable.MSTSIRGPSHN,"SIRCD"),
		/** 配送パターン_8（仕入） */
		HSPTN1_8(108,"配送パターン_8（仕入）",RefTable.MSTSIRGPSHN,"HSPTN"),
		/** 店グループ（エリア）_9（仕入） */
		TENGPCD1_9(109,"店グループ（エリア）_9（仕入）",RefTable.MSTSIRGPSHN,"TENGPCD"),
		/** 仕入先コード_9（仕入） */
		SIRCD1_9(110,"仕入先コード_9（仕入）",RefTable.MSTSIRGPSHN,"SIRCD"),
		/** 配送パターン_9（仕入） */
		HSPTN1_9(111,"配送パターン_9（仕入）",RefTable.MSTSIRGPSHN,"HSPTN"),
		/** 店グループ（エリア）_10（仕入） */
		TENGPCD1_10(112,"店グループ（エリア）_10（仕入）",RefTable.MSTSIRGPSHN,"TENGPCD"),
		/** 仕入先コード_10（仕入） */
		SIRCD1_10(113,"仕入先コード_10（仕入）",RefTable.MSTSIRGPSHN,"SIRCD"),
		/** 配送パターン_10（仕入） */
		HSPTN1_10(114,"配送パターン_10（仕入）",RefTable.MSTSIRGPSHN,"HSPTN"),

		/** エリア区分（売価） */
		AREAKBN2(115,"エリア区分（売価）",RefTable.MSTBAIKACTL,"AREAKBN"),
		/** 店グループ（エリア）_1（売価） */
		TENGPCD2_1(116,"店グループ（エリア）_1（売価）",RefTable.MSTBAIKACTL,"TENGPCD"),
		/** 原価_1（売価） */
		GENKAAM2_1(117,"原価_1（売価）",RefTable.MSTBAIKACTL,"GENKAAM"),
		/** 売価_1（売価） */
		BAIKAAM2_1(118,"売価_1（売価）",RefTable.MSTBAIKACTL,"BAIKAAM"),
		/** 店入数_1（売価） */
		IRISU2_1(119,"店入数_1（売価）",RefTable.MSTBAIKACTL,"IRISU"),
		/** 店グループ（エリア）_2（売価） */
		TENGPCD2_2(120,"店グループ（エリア）_2（売価）",RefTable.MSTBAIKACTL,"TENGPCD"),
		/** 原価_2（売価） */
		GENKAAM2_2(121,"原価_2（売価）",RefTable.MSTBAIKACTL,"GENKAAM"),
		/** 売価_2（売価） */
		BAIKAAM2_2(122,"売価_2（売価）",RefTable.MSTBAIKACTL,"BAIKAAM"),
		/** 店入数_2（売価） */
		IRISU2_2(123,"店入数_2（売価）",RefTable.MSTBAIKACTL,"IRISU"),
		/** 店グループ（エリア）_3（売価） */
		TENGPCD2_3(124,"店グループ（エリア）_3（売価）",RefTable.MSTBAIKACTL,"TENGPCD"),
		/** 原価_3（売価） */
		GENKAAM2_3(125,"原価_3（売価）",RefTable.MSTBAIKACTL,"GENKAAM"),
		/** 売価_3（売価） */
		BAIKAAM2_3(126,"売価_3（売価）",RefTable.MSTBAIKACTL,"BAIKAAM"),
		/** 店入数_3（売価） */
		IRISU2_3(127,"店入数_3（売価）",RefTable.MSTBAIKACTL,"IRISU"),
		/** 店グループ（エリア）_4（売価） */
		TENGPCD2_4(128,"店グループ（エリア）_4（売価）",RefTable.MSTBAIKACTL,"TENGPCD"),
		/** 原価_4（売価） */
		GENKAAM2_4(129,"原価_4（売価）",RefTable.MSTBAIKACTL,"GENKAAM"),
		/** 売価_4（売価） */
		BAIKAAM2_4(130,"売価_4（売価）",RefTable.MSTBAIKACTL,"BAIKAAM"),
		/** 店入数_4（売価） */
		IRISU2_4(131,"店入数_4（売価）",RefTable.MSTBAIKACTL,"IRISU"),
		/** 店グループ（エリア）_5（売価） */
		TENGPCD2_5(132,"店グループ（エリア）_5（売価）",RefTable.MSTBAIKACTL,"TENGPCD"),
		/** 原価_5（売価） */
		GENKAAM2_5(133,"原価_5（売価）",RefTable.MSTBAIKACTL,"GENKAAM"),
		/** 売価_5（売価） */
		BAIKAAM2_5(134,"売価_5（売価）",RefTable.MSTBAIKACTL,"BAIKAAM"),
		/** 店入数_5（売価） */
		IRISU2_5(135,"店入数_5（売価）",RefTable.MSTBAIKACTL,"IRISU"),

		/** エリア区分（品揃） */
		AREAKBN3(136,"エリア区分（品揃）",RefTable.MSTSHINAGP,"AREAKBN"),
		/** 店グループ（エリア）_1（品揃） */
		TENGPCD3_1(137,"店グループ（エリア）_1（品揃）",RefTable.MSTSHINAGP,"TENGPCD"),
		/** 扱い区分_1（品揃） */
		ATSUKKBN3_1(138,"扱い区分_1（品揃）",RefTable.MSTSHINAGP,"ATSUKKBN"),
		/** 店グループ（エリア）_2（品揃） */
		TENGPCD3_2(139,"店グループ（エリア）_2（品揃）",RefTable.MSTSHINAGP,"TENGPCD"),
		/** 扱い区分_2（品揃） */
		ATSUKKBN3_2(140,"扱い区分_2（品揃）",RefTable.MSTSHINAGP,"ATSUKKBN"),
		/** 店グループ（エリア）_3（品揃） */
		TENGPCD3_3(141,"店グループ（エリア）_3（品揃）",RefTable.MSTSHINAGP,"TENGPCD"),
		/** 扱い区分_3（品揃） */
		ATSUKKBN3_3(142,"扱い区分_3（品揃）",RefTable.MSTSHINAGP,"ATSUKKBN"),
		/** 店グループ（エリア）_4（品揃） */
		TENGPCD3_4(143,"店グループ（エリア）_4（品揃）",RefTable.MSTSHINAGP,"TENGPCD"),
		/** 扱い区分_4（品揃） */
		ATSUKKBN3_4(144,"扱い区分_4（品揃）",RefTable.MSTSHINAGP,"ATSUKKBN"),
		/** 店グループ（エリア）_5（品揃） */
		TENGPCD3_5(145,"店グループ（エリア）_5（品揃）",RefTable.MSTSHINAGP,"TENGPCD"),
		/** 扱い区分_5（品揃） */
		ATSUKKBN3_5(146,"扱い区分_5（品揃）",RefTable.MSTSHINAGP,"ATSUKKBN"),
		/** 店グループ（エリア）_6（品揃） */
		TENGPCD3_6(147,"店グループ（エリア）_6（品揃）",RefTable.MSTSHINAGP,"TENGPCD"),
		/** 扱い区分_6（品揃） */
		ATSUKKBN3_6(148,"扱い区分_6（品揃）",RefTable.MSTSHINAGP,"ATSUKKBN"),
		/** 店グループ（エリア）_7（品揃） */
		TENGPCD3_7(149,"店グループ（エリア）_7（品揃）",RefTable.MSTSHINAGP,"TENGPCD"),
		/** 扱い区分_7（品揃） */
		ATSUKKBN3_7(150,"扱い区分_7（品揃）",RefTable.MSTSHINAGP,"ATSUKKBN"),
		/** 店グループ（エリア）_8（品揃） */
		TENGPCD3_8(151,"店グループ（エリア）_8（品揃）",RefTable.MSTSHINAGP,"TENGPCD"),
		/** 扱い区分_8（品揃） */
		ATSUKKBN3_8(152,"扱い区分_8（品揃）",RefTable.MSTSHINAGP,"ATSUKKBN"),
		/** 店グループ（エリア）_9（品揃） */
		TENGPCD3_9(153,"店グループ（エリア）_9（品揃）",RefTable.MSTSHINAGP,"TENGPCD"),
		/** 扱い区分_9（品揃） */
		ATSUKKBN3_9(154,"扱い区分_9（品揃）",RefTable.MSTSHINAGP,"ATSUKKBN"),
		/** 店グループ（エリア）_10（品揃） */
		TENGPCD3_10(155,"店グループ（エリア）_10（品揃）",RefTable.MSTSHINAGP,"TENGPCD"),
		/** 扱い区分_10（品揃） */
		ATSUKKBN3_10(156,"扱い区分_10（品揃）",RefTable.MSTSHINAGP,"ATSUKKBN"),
		/** 平均パック単価 */
		AVGPTANKAAM(157,"平均パック単価",RefTable.MSTAVGPTANKA,"AVGPTANKAAM"),
		/** ソース区分_2 */
		SOURCEKBN2(158,"ソース区分_2",RefTable.MSTSRCCD,"SOURCEKBN"),
		/** ソースコード_2 */
		SRCCD2(159,"ソースコード_2",RefTable.MSTSRCCD,"SRCCD"),
		/** プライスカード出力有無 */
		PCARD_OPFLG(160,"プライスカード出力有無",RefTable.MSTSHN,"PCARD_OPFLG"),
		/** プライスカード_種類 */
		PCARD_SHUKBN(161,"プライスカード_種類",RefTable.MSTSHN,"PCARD_SHUKBN"),
		/** プライスカード_色 */
		PCARD_IROKBN(162,"プライスカード_色",RefTable.MSTSHN,"PCARD_IROKBN"),
		/** 税区分 */
		ZEIKBN(163,"税区分",RefTable.MSTSHN,"ZEIKBN"),
		/** 税率区分 */
		ZEIRTKBN(164,"税率区分",RefTable.MSTSHN,"ZEIRTKBN"),
		/** 旧税率区分 */
		ZEIRTKBN_OLD(165,"旧税率区分",RefTable.MSTSHN,"ZEIRTKBN_OLD"),
		/** 税率変更日 */
		ZEIRTHENKODT(166,"税率変更日",RefTable.MSTSHN,"ZEIRTHENKODT"),
		/** 取扱停止 */
		TEISHIKBN(167,"取扱停止",RefTable.MSTSHN,"TEISHIKBN"),
		/** 市場区分 */
		ICHIBAKBN(168,"市場区分",RefTable.MSTSHN,"ICHIBAKBN"),
		/** ＰＢ区分 */
		PBKBN(169,"ＰＢ区分",RefTable.MSTSHN,"PBKBN"),
		/** 返品区分 */
		HENPIN_KBN(170,"返品区分",RefTable.MSTSHN,"HENPIN_KBN"),
		/** 輸入区分 */
		IMPORTKBN(171,"輸入区分",RefTable.MSTSHN,"IMPORTKBN"),
		/** 裏貼 */
		URABARIKBN(172,"裏貼",RefTable.MSTSHN,"URABARIKBN"),
		/** 対象年齢 */
		TAISHONENSU(173,"対象年齢",RefTable.MSTSHN,"TAISHONENSU"),
		/** カロリー表示 */
		CALORIESU(174,"カロリー表示",RefTable.MSTSHN,"CALORIESU"),
		/** 加工区分 */
		KAKOKBN(175,"加工区分",RefTable.MSTSHN,"KAKOKBN"),
		/** 産地（漢字） */
		SANCHIKN(176,"産地（漢字）",RefTable.MSTSHN,"SANCHIKN"),
		/** 酒級 */
		SHUKYUKBN(177,"酒級",RefTable.MSTSHN,"SHUKYUKBN"),
		/** 度数 */
		DOSU(178,"度数",RefTable.MSTSHN,"DOSU"),
		/** 包材用途 */
		HZI_YOTO(179,"包材用途",RefTable.MSTSHN,"HZI_YOTO"),
		/** 包材材質 */
		HZI_ZAISHITU(180,"包材材質",RefTable.MSTSHN,"HZI_ZAISHITU"),
		/** 包材リサイクル対象 */
		HZI_RECYCLE(181,"包材リサイクル対象",RefTable.MSTSHN,"HZI_RECYCLE"),
		/** フラグ情報_ＥＬＰ */
		ELPFLG(182,"フラグ情報_ＥＬＰ",RefTable.MSTSHN,"ELPFLG"),
		/** フラグ情報_ベルマーク */
		BELLMARKFLG(183,"フラグ情報_ベルマーク",RefTable.MSTSHN,"BELLMARKFLG"),
		/** フラグ情報_リサイクル */
		RECYCLEFLG(184,"フラグ情報_リサイクル",RefTable.MSTSHN,"RECYCLEFLG"),
		/** フラグ情報_エコマーク */
		ECOFLG(185,"フラグ情報_エコマーク",RefTable.MSTSHN,"ECOFLG"),
		/** メーカーコード */
		MAKERCD(186,"メーカーコード",RefTable.MSTSHN,"MAKERCD"),
		/** 販売コード */
		URICD(187,"販売コード",RefTable.MSTSHN,"URICD"),
		/** 添加物_1 */
		TENKABCD2_1(188,"添加物_1",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** 添加物_2 */
		TENKABCD2_2(189,"添加物_2",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** 添加物_3 */
		TENKABCD2_3(190,"添加物_3",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** 添加物_4 */
		TENKABCD2_4(191,"添加物_4",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** 添加物_5 */
		TENKABCD2_5(192,"添加物_5",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** 添加物_6 */
		TENKABCD2_6(193,"添加物_6",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** 添加物_7 */
		TENKABCD2_7(194,"添加物_7",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** 添加物_8 */
		TENKABCD2_8(195,"添加物_8",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** 添加物_9 */
		TENKABCD2_9(196,"添加物_9",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** 添加物_10 */
		TENKABCD2_10(197,"添加物_10",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** アレルギー_1 */
		TENKABCD1_1(198,"アレルギー_1",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** アレルギー_2 */
		TENKABCD1_2(199,"アレルギー_2",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** アレルギー_3 */
		TENKABCD1_3(200,"アレルギー_3",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** アレルギー_4 */
		TENKABCD1_4(201,"アレルギー_4",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** アレルギー_5 */
		TENKABCD1_5(202,"アレルギー_5",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** アレルギー_6 */
		TENKABCD1_6(203,"アレルギー_6",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** アレルギー_7 */
		TENKABCD1_7(204,"アレルギー_7",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** アレルギー_8 */
		TENKABCD1_8(205,"アレルギー_8",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** アレルギー_9 */
		TENKABCD1_9(206,"アレルギー_9",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** アレルギー_10 */
		TENKABCD1_10(207,"アレルギー_10",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** アレルギー_11 */
		TENKABCD1_11(208,"アレルギー_11",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** アレルギー_12 */
		TENKABCD1_12(209,"アレルギー_12",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** アレルギー_13 */
		TENKABCD1_13(210,"アレルギー_13",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** アレルギー_14 */
		TENKABCD1_14(211,"アレルギー_14",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** アレルギー_15 */
		TENKABCD1_15(212,"アレルギー_15",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** アレルギー_16 */
		TENKABCD1_16(213,"アレルギー_16",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** アレルギー_17 */
		TENKABCD1_17(214,"アレルギー_17",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** アレルギー_18 */
		TENKABCD1_18(215,"アレルギー_18",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** アレルギー_19 */
		TENKABCD1_19(216,"アレルギー_19",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** アレルギー_20 */
		TENKABCD1_20(217,"アレルギー_20",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** アレルギー_21 */
		TENKABCD1_21(218,"アレルギー_21",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** アレルギー_22 */
		TENKABCD1_22(219,"アレルギー_22",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** アレルギー_23 */
		TENKABCD1_23(220,"アレルギー_23",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** アレルギー_24 */
		TENKABCD1_24(221,"アレルギー_24",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** アレルギー_25 */
		TENKABCD1_25(222,"アレルギー_25",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** アレルギー_26 */
		TENKABCD1_26(223,"アレルギー_26",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** アレルギー_27 */
		TENKABCD1_27(224,"アレルギー_27",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** アレルギー_28 */
		TENKABCD1_28(225,"アレルギー_28",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** アレルギー_29 */
		TENKABCD1_29(226,"アレルギー_29",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** アレルギー_30 */
		TENKABCD1_30(227,"アレルギー_30",RefTable.MSTTENKABUTSU,"TENKABCD"),
		/** 種別コード */
		SHUBETUCD(228,"種別コード",RefTable.MSTSHN,"SHUBETUCD"),
		/** 衣料使い回しフラグ */
		IRYOREFLG(229,"衣料使い回しフラグ",RefTable.MSTSHN,"IRYOREFLG"),
		/** 登録元 */
		TOROKUMOTO(230,"登録元",RefTable.MSTSHN,"TOROKUMOTO"),
		/** オペレータ */
		OPERATOR(231,"オペレータ",RefTable.MSTSHN,"OPERATOR"),
		/** 登録日 */
		ADDDT(232,"登録日",RefTable.MSTSHN,"ADDDT"),
		/** 変更日 */
		UPDDT(233,"変更日",RefTable.MSTSHN,"UPDDT"),

		/*** デリカ対応追加項目 ***/
		/** 保温区分 */
		K_HONKB(234,"保温区分",RefTable.MSTSHN,"K_HONKB"),
		/** デリカワッペン */
		K_WAPNFLG_R(235,"デリカワッペン",RefTable.MSTSHN,"K_WAPNFLG_R"),
		/** 取引区分 */
		K_TORIKB(236,"取引区分",RefTable.MSTSHN,"K_TORIKB"),
		/** ＩＴＦコード */
		ITFCD(237,"ＩＴＦコード",RefTable.MSTSHN,"ITFCD"),
		/** センター入数 */
		CENTER_IRISU(238,"センター入数",RefTable.MSTSHN,"CENTER_IRISU"),
		/** 店別異部門 */
		TENBMNCD(239,"店別異部門",RefTable.MSTSHNTENBMN,"TENBMNCD"),
		/** 商品ｺｰﾄﾞ_1 */
		TENSHNCD_1(240,"商品ｺｰﾄﾞ_1",RefTable.MSTSHNTENBMN,"TENSHNCD"),
		/** JANｺｰﾄﾞ_1 */
		SRCCD_1(241,"JANｺｰﾄﾞ_1",RefTable.MSTSHNTENBMN,"SRCCD"),
		/** 店ｸﾞﾙｰﾌﾟ_1 */
		TENGPCD_1(242,"店ｸﾞﾙｰﾌﾟ_1",RefTable.MSTSHNTENBMN,"TENGPCD"),
		/** 商品ｺｰﾄﾞ_1 */
		TENSHNCD_2(243,"商品ｺｰﾄﾞ_2",RefTable.MSTSHNTENBMN,"TENSHNCD"),
		/** JANｺｰﾄﾞ_1 */
		SRCCD_2(244,"JANｺｰﾄﾞ_2",RefTable.MSTSHNTENBMN,"SRCCD"),
		/** 店ｸﾞﾙｰﾌﾟ_1 */
		TENGPCD_2(245,"店ｸﾞﾙｰﾌﾟ_2",RefTable.MSTSHNTENBMN,"TENGPCD"),
		/** 商品ｺｰﾄﾞ_1 */
		TENSHNCD_3(246,"商品ｺｰﾄﾞ_3",RefTable.MSTSHNTENBMN,"TENSHNCD"),
		/** JANｺｰﾄﾞ_1 */
		SRCCD_3(247,"JANｺｰﾄﾞ_3",RefTable.MSTSHNTENBMN,"SRCCD"),
		/** 店ｸﾞﾙｰﾌﾟ_1 */
		TENGPCD_3(248,"店ｸﾞﾙｰﾌﾟ_3",RefTable.MSTSHNTENBMN,"TENGPCD"),
		/** 商品ｺｰﾄﾞ_1 */
		TENSHNCD_4(249,"商品ｺｰﾄﾞ_4",RefTable.MSTSHNTENBMN,"TENSHNCD"),
		/** JANｺｰﾄﾞ_1 */
		SRCCD_4(250,"JANｺｰﾄﾞ_4",RefTable.MSTSHNTENBMN,"SRCCD"),
		/** 店ｸﾞﾙｰﾌﾟ_1 */
		TENGPCD_4(251,"店ｸﾞﾙｰﾌﾟ_4",RefTable.MSTSHNTENBMN,"TENGPCD"),
		/** 商品ｺｰﾄﾞ_1 */
		TENSHNCD_5(252,"商品ｺｰﾄﾞ_5",RefTable.MSTSHNTENBMN,"TENSHNCD"),
		/** JANｺｰﾄﾞ_1 */
		SRCCD_5(253,"JANｺｰﾄﾞ_5",RefTable.MSTSHNTENBMN,"SRCCD"),
		/** 店ｸﾞﾙｰﾌﾟ_1 */
		TENGPCD_5(254,"店ｸﾞﾙｰﾌﾟ_5",RefTable.MSTSHNTENBMN,"TENGPCD");

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
