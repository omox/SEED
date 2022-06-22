/**
 *
 */
package dao;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
import common.DefineReport.DataType;
import common.Defines;
import common.FileList;
import common.InputChecker;
import common.ItemList;
import common.MessageUtility;
import common.MessageUtility.Msg;
import common.MessageUtility.MsgKey;
import dao.ReportTG016Dao.TOKSP_HBLayout;
import dao.ReportTG016Dao.TOKSP_NNDTLayout;
import dao.ReportTG016Dao.TOKSP_SHNLayout;
import dao.ReportTG016Dao.TOKTG_NNDTLayout;
import dao.ReportTG016Dao.TOKTG_SHNLayout;
import dao.ReportTG016Dao.TOK_CMN_SHNNNDTLayout;
import dao.ReportTG016Dao.TOK_CMN_TJTENLayout;
import dto.JQEasyModel;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 */
public class ReportST024Dao extends ItemDao {

	/** 最大処理件数 */
	public static int MAX_ROW = 10000;

	// CSVを分解、画面上と同じように登録用のデータの形にする
	JSONArray annd = new JSONArray(), ashn = new JSONArray(), atjt = new JSONArray(), ahb = new JSONArray(), aoth = new JSONArray(), amoy = new JSONArray(), achk = new JSONArray();
	JSONObject onnd = new JSONObject(), oshn = new JSONObject(), otjt = new JSONObject(), ohb = new JSONObject(), ooth = new JSONObject(), ochk = new JSONObject();
	String oldBin = "";
	String hobokure = "";

	String strMsg = null;
	JQEasyModel json = new JQEasyModel();

	ReportTG016Dao dao = new ReportTG016Dao(super.JNDIname);
	Set<String> moyscdSet	= new TreeSet<String>();
	Set<String> keySet		= new TreeSet<String>();

	String updkbn = "";
	int stdt = 0;
	int eddt = 0;

	/**
	 * インスタンスを生成します。
	 * @param source
	 */
	public ReportST024Dao(String JNDIname) {
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
		JSONObject option = new JSONObject();
		String btnId = map.get("SENDBTNID");

		// メッセージ情報取得
		MessageUtility mu = new MessageUtility();
		JSONArray errMsgList = new JSONArray();
		List<JSONObject> msgList = new ArrayList<JSONObject>(){};

		String status = "";

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
			msgList = this.checkFileLayout(dL, idxHeader, btnId);

			if (msgList.size() != 0) {
				this.setMessage(msgList.toString());
				strMsg = this.getMessage();
			}

			// ファイル内レコード件数
			int cnt = 0;

			int errCount = 0;

			boolean resultCsv1 = false;

			// 全てのデータに対する単純なチェック
			// データ加工

			if(strMsg==null && StringUtils.isEmpty(this.getMessage())){
				String callpage = map.get("report");
				if (btnId.equals(DefineReport.Button.CSV.getObj() + "1")) {
					msgList = setCsvReqShn(dL,idxHeader,dao, callpage, mu);
					if(msgList.size() != 0){
						this.setMessage(msgList.toString());
						strMsg = this.getMessage();
					}else{
						resultCsv1 = true;
					}
				} else if (btnId.equals(DefineReport.Button.CSV.getObj() + "2")) {
					msgList = setCsvReqTen(dL,idxHeader,dao, callpage, mu);
					if (msgList.size() != 0) {
						this.setMessage(msgList.toString());
						strMsg = this.getMessage();
					}
				} else {
					msgList = setCsvReqNndt(dL,idxHeader);
					if (msgList.size() != 0) {
						this.setMessage(msgList.toString());
						strMsg = this.getMessage();
					}
				}
			}

			if(strMsg==null && StringUtils.isEmpty(this.getMessage())){

				// 最新の情報取得
				// データ取得先判断
				if (!btnId.equals(DefineReport.Button.CSV.getObj() + "1") && !btnId.equals(DefineReport.Button.CSV.getObj() + "2")) {
					JSONObject rt = this.createCommandUpdateData(
							dao,
							userInfo,
							this.selectTOKSP_NNDT(annd,false),
							new JSONArray(),
							false
					);

					this.sqlList.addAll(dao.sqlList);
					this.prmList.addAll(dao.prmList);
					this.lblList.addAll(dao.lblList);

				} else if (btnId.equals(DefineReport.Button.CSV.getObj() + "2")) {

					JSONObject rt = this.createCommandUpdateData(
							dao,
							userInfo,
							this.selectTOKSP_NNDT(annd,false),
							this.selectTOKSP_SHNLayout(ashn,false),
							true
					);

					this.sqlList.addAll(dao.sqlList);
					this.prmList.addAll(dao.prmList);
					this.lblList.addAll(dao.lblList);
				}

				if (strMsg == null && StringUtils.isEmpty(this.getMessage())) {
					JSONObject rtn = this.updateData();

					if (rtn.containsKey(MsgKey.E.getKey())) {
						strMsg = rtn.get(MsgKey.E.getKey()).toString();
					} else {
						strMsg = this.getMessage();
					}
				}
			}

			// 更新成功
			status = "処理";
			cnt = dL.size();

			if (resultCsv1 || (errCount == 0 && StringUtils.isEmpty(strMsg))) {
				strMsg = null;
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

	private JSONArray selectTOK_CMN_SHNNNDT(JSONArray dataArray ,boolean checkFlg) {
		ArrayList<String> prmData = new ArrayList<>();
		String sqlCommand = "";
		if (checkFlg) {
			sqlCommand = this.checkSelectCommandMST(dataArray, TOK_CMN_SHNNNDTLayout.values(), prmData);
		} else {
			sqlCommand = this.setSelectCommandMST(dataArray, TOK_CMN_SHNNNDTLayout.values(), prmData);
		}
		if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sqlCommand);

		ItemList iL = new ItemList();
		@SuppressWarnings("static-access")
		JSONArray rtnArray = iL.selectJSONArray(sqlCommand, prmData, Defines.STR_JNDI_DS);
		return rtnArray;
	}

	private JSONArray selectTOKTG_SHNLayout(JSONArray dataArray ,boolean checkFlg) {
		ArrayList<String> prmData = new ArrayList<>();
		String sqlCommand = "";
		if (checkFlg) {
			sqlCommand = this.checkSelectCommandMST(dataArray, TOKTG_SHNLayout.values(), prmData);
		} else {
			sqlCommand = this.setSelectCommandMST(dataArray, TOKTG_SHNLayout.values(), prmData);
		}
		if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sqlCommand);

		ItemList iL = new ItemList();
		@SuppressWarnings("static-access")
		JSONArray rtnArray = iL.selectJSONArray(sqlCommand, prmData, Defines.STR_JNDI_DS);
		return rtnArray;
	}

	private JSONArray selectTOKSP_SHNLayout(JSONArray dataArray ,boolean checkFlg) {
		ArrayList<String> prmData = new ArrayList<>();
		String sqlCommand = "";
		if (checkFlg) {
			sqlCommand = this.checkSelectCommandMST(dataArray, TOKSP_SHNLayout.values(), prmData);
		} else {
			sqlCommand = this.setSelectCommandMST(dataArray, TOKSP_SHNLayout.values(), prmData);
		}
		if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sqlCommand);

		ItemList iL = new ItemList();
		@SuppressWarnings("static-access")
		JSONArray rtnArray = iL.selectJSONArray(sqlCommand, prmData, Defines.STR_JNDI_DS);
		return rtnArray;
	}

	private JSONArray selectTOKSP_NNDT(JSONArray dataArray ,boolean checkFlg) {
		ArrayList<String> prmData = new ArrayList<>();
		String sqlCommand = "";
		if (checkFlg) {
			sqlCommand = this.checkSelectCommandMST(dataArray, TOKSP_NNDTLayout.values(), prmData);
		} else {
			sqlCommand = this.setSelectCommandMST(dataArray, TOKSP_NNDTLayout.values(), prmData);
		}
		if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sqlCommand);

		ItemList iL = new ItemList();
		@SuppressWarnings("static-access")
		JSONArray rtnArray = iL.selectJSONArray(sqlCommand, prmData, Defines.STR_JNDI_DS);
		return rtnArray;
	}

	private JSONArray selectTOKTG_NNDT(JSONArray dataArray ,boolean checkFlg) {
		ArrayList<String> prmData = new ArrayList<>();
		String sqlCommand = "";
		if (checkFlg) {
			sqlCommand = this.checkSelectCommandMST(dataArray, TOKTG_NNDTLayout.values(), prmData);
		} else {
			sqlCommand = this.setSelectCommandMST(dataArray, TOKTG_NNDTLayout.values(), prmData);
		}
		if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sqlCommand);

		ItemList iL = new ItemList();
		@SuppressWarnings("static-access")
		JSONArray rtnArray = iL.selectJSONArray(sqlCommand, prmData, Defines.STR_JNDI_DS);
		return rtnArray;
	}

	private JSONArray selectTOK_TJTEN(JSONArray dataArray ,boolean checkFlg) {
		ArrayList<String> prmData = new ArrayList<>();
		String sqlCommand = "";
		if (checkFlg) {
			sqlCommand = this.checkSelectCommandMST(dataArray, TOK_CMN_TJTENLayout.values(), prmData);
		} else {
			sqlCommand = this.setSelectCommandMST(dataArray, TOK_CMN_TJTENLayout.values(), prmData);
		}
		if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sqlCommand);

		ItemList iL = new ItemList();
		@SuppressWarnings("static-access")
		JSONArray rtnArray = iL.selectJSONArray(sqlCommand, prmData, Defines.STR_JNDI_DS);
		return rtnArray;
	}

	private JSONArray selectTOK_HB(JSONArray dataArray ,boolean checkFlg) {
		ArrayList<String> prmData = new ArrayList<>();
		String sqlCommand = "";
		if (checkFlg) {
			sqlCommand = this.checkSelectCommandMST(dataArray, TOKSP_HBLayout.values(), prmData);
		} else {
			sqlCommand = this.setSelectCommandMST(dataArray, TOKSP_HBLayout.values(), prmData);
		}
		if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sqlCommand);

		ItemList iL = new ItemList();
		@SuppressWarnings("static-access")
		JSONArray rtnArray = iL.selectJSONArray(sqlCommand, prmData, Defines.STR_JNDI_DS);
		return rtnArray;
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
	private String checkSelectCommandMST(JSONArray dataArray, ReportTG016Dao.MSTLayout[] layouts, ArrayList<String> prmData) {
		String values = "", names = "", rows = "";
		for(int j=0; j < dataArray.size(); j++){
			values = "";
			names = "";
			for(ReportTG016Dao.MSTLayout itm : layouts){
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
		for(ReportTG016Dao.MSTLayout itm :layouts){
			if(itm.getNo() > 1){ sbSQLIn.append(","); }
			sbSQLIn.append(itm.getCol() + " as "+itm.getCol());
		}
		sbSQLIn.append(" from (values"+rows+") as T1("+names+")");

		// 基本Select文
		StringBuffer sbSQL = new StringBuffer();
		sbSQL.append(" select ");
		for(ReportTG016Dao.MSTLayout itm :layouts){
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
	private String setSelectCommandMST(JSONArray dataArray, ReportTG016Dao.MSTLayout[] layouts, ArrayList<String> prmData) {
		String values = "", names = "", rows = "";
		for(int j=0; j < dataArray.size(); j++){
			values = "";
			names = "";
			for(ReportTG016Dao.MSTLayout itm : layouts){
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
		for(ReportTG016Dao.MSTLayout itm :layouts){
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
		for(ReportTG016Dao.MSTLayout itm :layouts){
			if(itm.getNo() > 1){ sbSQL.append(","); }
			sbSQL.append("RE."+itm.getCol() + " as "+itm.getId());
		}
		sbSQL.append(" from (" + sbSQLIn.toString() + ") RE");

		return sbSQL.toString();
	}

	private List<JSONObject> checkData(boolean isNew, boolean isChange, boolean isYoyaku1, Reportx002Dao dao,
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


		// DB最新情報再取得
		String szYoyaku = "0";
		JSONArray yArray = new JSONArray();
		if(!isNew){
			JSONArray array = dao.getYoyakuJSONArray(map);
			szYoyaku = Integer.toString(array.size());
			yArray.addAll(array);
		}
		JSONObject seiJsonObject = new JSONObject();
		if(isChange){
			JSONArray array = dao.getSeiJSONArray(map);
			seiJsonObject = array.optJSONObject(0);
		}


		return msg;
	}

	private JSONArray checkFileLayout(ArrayList<Object[]> eL, int idxHeader, String btnId) {

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
		int length = FileLayout.values().length;
		if (btnId.equals(DefineReport.Button.CSV.getObj() + "1")) {
			length = FileLayout3.values().length;
		} else if (btnId.equals(DefineReport.Button.CSV.getObj() + "2")) {
			length = FileLayout2.values().length;
		}

		if(length != eL.get(idxHeader).length){
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

	public List<JSONObject> dataCreateCheck(Object[] data, MessageUtility mu, String reqNo) {

		List<JSONObject> msgList = new ArrayList<JSONObject>(){};

		StringBuffer		sbSQL	= new StringBuffer();
		ItemList			iL		= new ItemList();
		JSONArray			dbDatas	= new JSONArray();
		ArrayList<String>	paramData = new ArrayList<String>();

		// DB検索用パラメータ
		String sqlWhere	= "";
		String sqlFrom	= "";

		String moyskbn	= "";
		String moysstdt	= "";
		String moysrban	= "";
		String kanrino	= "";
		String bmncd	= "";

		String tenrank_arr = "";
		String addRank = "";
		String delRank = "";
		JSONArray addTenArr = new JSONArray();
		JSONArray addTenRankArr = new JSONArray();
		JSONArray delTenArr = new JSONArray();
		String tenkaiKbn = "";
		String wwmm = "";
		String daicd = "";
		String chucd = "";
		String shncd = "";
		String syuKbn = "";

		String a_baikaam2 = "";
		String a_baikaam1 = "";
		String b_baikaam2 = "";
		String b_baikaam1 = "";
		String c_baikaam2 = "";
		String c_baikaam1 = "";
		String tkanplukbn = "";
		String pcKbn = "";

		for(FileLayout3 itm :FileLayout3.values()){
			String val = StringUtils.trim((String) data[itm.getNo()-1]);
			// 1.""で囲まれていた場合除去 2.末尾空白除去
			val = StringUtils.stripEnd(val.replaceFirst("^\"(.*)\"$", "$1"), null);

			if(itm.getTbl() == RefTable.TOKSHN){
				if(oshn.containsKey(itm.getCol())){
					ashn.add(oshn);
					oshn = new JSONObject();
				}
				if (itm.getCol().equals(FileLayout3.MOYSCD.getCol())) {

					if (!moyscdSet.contains(val)) {
						if (moyscdSet.size() != 0) {
							JSONObject o = mu.getDbMessageObj("E40004", new String[]{reqNo});
							msgList.add(o);
							return msgList;
						} else {
							moyscdSet.add(val);
						}
					}

					if (val.length() >= 8) {
						moyskbn		= val.substring(0,1);
						moysstdt	= val.substring(1,7);
						moysrban	= String.valueOf(Integer.valueOf(val.substring(7)));
					} else if (val.length() >= 7) {
						moyskbn		= val.substring(0,1);
						moysstdt	= val.substring(1,7);
					} else if (val.length() >= 1) {
						moyskbn		= val.substring(0,1);
					}

					DataType dtype = DefineReport.DataType.SUUJI;
					int[] digit = new int[]{2, 0};

					// ①データ型による文字種チェック
					if(!InputChecker.checkDataType(dtype, bmncd)){
						// エラー発生箇所を保存
						JSONObject o = mu.getDbMessageObjDataTypeErr(dtype, new String[]{reqNo+"部門は"});
						msgList.add(o);
						return msgList;
					}
					// ②データ桁チェック
					if(!InputChecker.checkDataLen(dtype, bmncd, digit)){
						// エラー発生箇所を保存
						JSONObject o = mu.getDbMessageObjLen(dtype, new String[]{reqNo+"部門は"});
						msgList.add(o);
						return msgList;
					}

					// 登録種別の取得
					// 催し種類情報設定
					dao.setMoycdInfo(moyskbn, moysstdt, moysrban, bmncd);

					oshn.put(TOKSP_SHNLayout.MOYSKBN.getCol(), moyskbn);
					oshn.put(TOKSP_SHNLayout.MOYSSTDT.getCol(), moysstdt);
					oshn.put(TOKSP_SHNLayout.MOYSRBAN.getCol(), moysrban);

					ohb.put(TOKSP_HBLayout.MOYSKBN.getCol(), moyskbn);
					ohb.put(TOKSP_HBLayout.MOYSSTDT.getCol(), moysstdt);
					ohb.put(TOKSP_HBLayout.MOYSRBAN.getCol(), moysrban);

					ochk.put(TOKSP_SHNLayout.MOYSKBN.getCol(), moyskbn);
					ochk.put(TOKSP_SHNLayout.MOYSSTDT.getCol(), moysstdt);
					ochk.put(TOKSP_SHNLayout.MOYSRBAN.getCol(), moysrban);

				} else if (itm.getCol().equals(FileLayout3.BMNCD.getCol())) {
					bmncd = val;

					oshn.put(TOKSP_SHNLayout.BMNCD.getCol(), bmncd);
					ohb.put(TOKSP_HBLayout.BMNCD.getCol(), bmncd);
					ochk.put(TOKSP_HBLayout.BMNCD.getCol(), bmncd);

				} else if (itm.getCol().equals(FileLayout3.KANRINO.getCol())) {

					if (updkbn.equals("A")) {
						kanrino = "";
					} else {
						kanrino = !StringUtils.isEmpty(val) && NumberUtils.isNumber(val) ? String.valueOf(Integer.valueOf(val)) : val;
					}

					oshn.put(TOKSP_SHNLayout.KANRINO.getCol(), kanrino);
					ohb.put(TOKSP_HBLayout.KANRINO.getCol(), kanrino);
					ochk.put(TOKSP_HBLayout.KANRINO.getCol(), kanrino);

					// 同一のキーはNG
					if (!StringUtils.isEmpty(kanrino)) {
						if (keySet.contains(moyskbn+moysstdt+moysrban+bmncd+val)) {
							if (keySet.size() != 0) {
								JSONObject o = mu.getDbMessageObj("E40005", new String[]{reqNo});
								msgList.add(o);
								return msgList;
							}
						} else {
								keySet.add(moyskbn+moysstdt+moysrban+bmncd+val);
						}
					}

					// 変数を初期化
					sbSQL	= new StringBuffer();
					iL		= new ItemList();
					dbDatas = new JSONArray();
					sqlWhere	= "";
					paramData	= new ArrayList<String>();

					// 登録種別の取得
					if (dao.isToktg) {
						sqlFrom = "INATK.TOKTG_SHN ";
					} else {
						sqlFrom = "INATK.TOKSP_SHN ";
					}

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
						sqlWhere += "MOYSRBAN=null AND ";
					} else {
						sqlWhere += "MOYSRBAN=? AND ";
						paramData.add(moysrban);
					}

					// 部門コード
					if (StringUtils.isEmpty(bmncd)) {
						sqlWhere += "BMNCD=null AND ";
					} else {
						sqlWhere += "BMNCD=? AND ";
						paramData.add(bmncd);
					}

					// 管理番号
					if (StringUtils.isEmpty(kanrino)) {
						sqlWhere += "KANRINO=null ";
					} else {
						sqlWhere += "KANRINO=? ";
						paramData.add(kanrino);
					}

					sbSQL.append("SELECT DISTINCT ");
					sbSQL.append("ADDSHUKBN ");		// 登録種別
					sbSQL.append(",TENRANK_ARR ");	// 店ランク配列
					sbSQL.append(",KANRIENO ");		// 枝番
					sbSQL.append(",BINKBN ");		// 便区分
					sbSQL.append("FROM ");
					sbSQL.append(sqlFrom);
					sbSQL.append("WHERE ");
					sbSQL.append(sqlWhere);		// 入力された催しコード

					dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

					String updkbn = StringUtils.trim((String) data[FileLayout3.UPDKBN.getNo()-1]);

					if (dbDatas.size() != 0){

						if (updkbn.equals("A")) {
							JSONObject o = mu.getDbMessageObj("E40007", new String[]{reqNo});
							msgList.add(o);
							return msgList;
						}

						oshn.put(TOKSP_SHNLayout.ADDSHUKBN.getCol(), dbDatas.getJSONObject(0).optString("ADDSHUKBN"));
						oshn.put(TOKSP_SHNLayout.KANRIENO.getCol(), dbDatas.getJSONObject(0).optString("KANRIENO"));
						ochk.put(TOKSP_SHNLayout.KANRIENO.getCol(), dbDatas.getJSONObject(0).optString("KANRIENO"));
						dao.setFrmInfo(dbDatas.getJSONObject(0).optString("ADDSHUKBN"));
						tenrank_arr = dbDatas.getJSONObject(0).optString("TENRANK_ARR");
						oldBin = dbDatas.getJSONObject(0).optString("BINKBN");
					} else {

						if (updkbn.equals("U")) {
							JSONObject o = mu.getDbMessageObj("E20582", new String[]{reqNo});
							msgList.add(o);
							return msgList;
						}

						if (bmncd.equals("2") || bmncd.equals("9") || bmncd.equals("15")) {
							oshn.put(TOKSP_SHNLayout.ADDSHUKBN.getCol(), DefineReport.ValAddShuKbn.VAL3.getVal());
							dao.setFrmInfo(DefineReport.ValAddShuKbn.VAL3.getVal());
						} else if (bmncd.equals("4") || bmncd.equals("6")) {
							oshn.put(TOKSP_SHNLayout.ADDSHUKBN.getCol(), DefineReport.ValAddShuKbn.VAL4.getVal());
							dao.setFrmInfo(DefineReport.ValAddShuKbn.VAL4.getVal());
						} else if (bmncd.equals("5")) {
							oshn.put(TOKSP_SHNLayout.ADDSHUKBN.getCol(), DefineReport.ValAddShuKbn.VAL5.getVal());
							dao.setFrmInfo(DefineReport.ValAddShuKbn.VAL5.getVal());
						} else {
							oshn.put(TOKSP_SHNLayout.ADDSHUKBN.getCol(), DefineReport.ValAddShuKbn.VAL2.getVal());
							dao.setFrmInfo(DefineReport.ValAddShuKbn.VAL2.getVal());
						}

						ochk.put(TOKSP_SHNLayout.KANRIENO.getCol(), "");
					}
					ochk.put(FileLayout3.UPDKBN.getCol(), updkbn);
				} else if (itm.getCol().equals(FileLayout3.SHNCD.getCol())) {
					val = !StringUtils.isEmpty(val) && val.length() <= 7 && NumberUtils.isNumber(val) ? String.format("%08d", Integer.valueOf(val)) : val;
					shncd = val;
					oshn.put(itm.getCol(), val);
					ochk.put(itm.getCol(), val);

					// 変数を初期化
					sbSQL	= new StringBuffer();
					iL		= new ItemList();
					dbDatas = new JSONArray();
					sqlWhere	= "";
					paramData	= new ArrayList<String>();

					// 登録種別の取得
					sqlFrom = "INAMS.MSTSHN ";

					// 商品コード
					if (StringUtils.isEmpty(val)) {
						sqlWhere += "SHNCD=null ";
					} else {
						sqlWhere += "SHNCD=? ";
						paramData.add(val);
					}

					sbSQL.append("SELECT ");
					sbSQL.append("PCKBN ");			// PC区分
					sbSQL.append(",CASE WHEN TEIKANKBN='0' THEN '2' WHEN TEIKANKBN='1' THEN TEIKANKBN ELSE '1' END AS TEIKANKBN ");	// 定貫区分
					sbSQL.append(",DAICD ");	// 大分類
					sbSQL.append(",CHUCD ");	// 中分類
					sbSQL.append("FROM ");
					sbSQL.append(sqlFrom);
					sbSQL.append("WHERE ");
					sbSQL.append(sqlWhere);		// 入力された催しコード

					dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

					if (dbDatas.size() >= 1) {
						pcKbn = dbDatas.getJSONObject(0).optString("PCKBN");
						daicd = dbDatas.getJSONObject(0).optString("DAICD");
						chucd = dbDatas.getJSONObject(0).optString("CHUCD");

						String teikankbn	= dbDatas.getJSONObject(0).optString("TEIKANKBN");
						String teikankbnCsv	= StringUtils.trim((String) data[FileLayout3.TKANPLUKBN.getNo()-1]);

						if (!teikankbn.equals(teikankbnCsv)) {
							JSONObject o = mu.getDbMessageObj("E20605", new String[]{reqNo});
							msgList.add(o);
							return msgList;
						}
					}
				} else if (itm.getCol().equals(FileLayout3.A_BAIKAAM2.getCol())) {
					a_baikaam2 = val;
				} else if (itm.getCol().equals(FileLayout3.A_BAIKAAM1.getCol())) {
					a_baikaam1 = val;
				} else if (itm.getCol().equals(FileLayout3.B_BAIKAAM2.getCol())) {
					b_baikaam2 = val;
				} else if (itm.getCol().equals(FileLayout3.B_BAIKAAM1.getCol())) {
					b_baikaam1 = val;
				} else if (itm.getCol().equals(FileLayout3.C_BAIKAAM2.getCol())) {
					c_baikaam2 = val;
				} else if (itm.getCol().equals(FileLayout3.C_BAIKAAM1.getCol())) {
					c_baikaam1 = val;
				} else if (itm.getCol().equals(FileLayout3.TKANPLUKBN.getCol())) {
					if(!StringUtils.isEmpty(val) && !val.equals("1") && !val.equals("2")) {
						JSONObject o = mu.getDbMessageObj("E40013", new String[]{reqNo + itm.getTxt() + "は"});
						msgList.add(o);
						return msgList;
					}
					if (!dao.isFrm2 && !dao.isFrm3) {
						tkanplukbn = "";
						oshn.put(itm.getCol(), "");
					} else {
						tkanplukbn = val;
						oshn.put(itm.getCol(), val);
					}
				} else if (itm.getCol().equals(FileLayout3.A_GENKAAM_1KG.getCol())) {
					if (tkanplukbn.equals("2") && StringUtils.isEmpty(val)) {
						// エラー発生箇所を保存
						JSONObject o = mu.getDbMessageObj("EX1047", new String[]{reqNo+"特売Ａ1kg総売価"});
						msgList.add(o);
						return msgList;
					} else {
						oshn.put(itm.getCol(), val);
					}
				} else if (itm.getCol().equals(FileLayout3.HTGENBAIKAFLG.getCol())) {
					if (!StringUtils.isEmpty(val) && !val.equals("0") && !val.equals("1")) {
						JSONObject o = mu.getDbMessageObj("E40013", new String[]{reqNo + itm.getTxt() + "は"});
						msgList.add(o);
						return msgList;
					}
					if (!dao.isFrm2 && !dao.isFrm3) {
						oshn.put(itm.getCol(), "");
					} else {
						if (StringUtils.isEmpty(val)) {
							val = "0";
						}
						oshn.put(itm.getCol(), val);
					}
				} else if (itm.getCol().equals(FileLayout3.HBSLIDEFLG.getCol())) {
					if (!StringUtils.isEmpty(val) && !val.equals("0") && !val.equals("1")) {
						JSONObject o = mu.getDbMessageObj("E40013", new String[]{reqNo + itm.getTxt() + "は"});
						msgList.add(o);
						return msgList;
					}
					if (dao.isToktg) {
						oshn.put(TOKTG_SHNLayout.HBSLIDEFLG.getCol(), val);
					}
				} else if (itm.getCol().equals(FileLayout3.NHSLIDEFLG.getCol())) {
					if (!StringUtils.isEmpty(val) && !val.equals("0") && !val.equals("1")) {
						JSONObject o = mu.getDbMessageObj("E40013", new String[]{reqNo + itm.getTxt() + "は"});
						msgList.add(o);
						return msgList;
					}
					if (dao.isToktg) {
						oshn.put(TOKTG_SHNLayout.NHSLIDEFLG.getCol(), val);
					}
				} else if (itm.getCol().equals(FileLayout3.RANKNO_ADD_A.getCol())) {
					if (dao.isToktg) {
						oshn.put(TOKTG_SHNLayout.RANKNO_ADD.getCol(), val);
					} else {
						oshn.put(TOKSP_SHNLayout.RANKNO_ADD_A.getCol(), val);
					}
					addRank = val;
				} else if (itm.getCol().equals(FileLayout3.RANKNO_ADD_B.getCol())) {
					if (!dao.isToktg) {
						oshn.put(TOKSP_SHNLayout.RANKNO_ADD_B.getCol(), val);
					}
				} else if (itm.getCol().equals(FileLayout3.RANKNO_ADD_C.getCol())) {
					if (!dao.isToktg) {
						oshn.put(TOKSP_SHNLayout.RANKNO_ADD_C.getCol(), val);
					}
				} else if (itm.getCol().equals(FileLayout3.RANKNO_DEL.getCol())) {
					if (!dao.isToktg) {
						oshn.put(TOKSP_SHNLayout.RANKNO_DEL.getCol(), val);
						delRank = val;
					}
				} else if (itm.getCol().equals(FileLayout3.TENKAIKBN.getCol())) {
					tenkaiKbn = StringUtils.isEmpty(val) ? "2":val;
					oshn.put(itm.getCol(), tenkaiKbn);
				} else if (itm.getCol().equals(FileLayout3.JSKPTNZNENWKBN.getCol())) {
					if (!StringUtils.isEmpty(val) && !val.equals("0")) {
						wwmm = "1";
						oshn.put(itm.getCol(), val);
					} else {
						oshn.put(itm.getCol(), "1");
					}
				} else if (itm.getCol().equals(FileLayout3.JSKPTNZNENMKBN.getCol())) {
					if (!StringUtils.isEmpty(val) && !val.equals("0")) {
						if (!StringUtils.isEmpty(wwmm)) {
							JSONObject o = mu.getDbMessageObj("E40013", new String[]{reqNo + itm.getTxt() + "は"});
							msgList.add(o);
							return msgList;
						}
						wwmm = "2";
					}
					oshn.put(itm.getCol(), val);
				} else if (itm.getCol().equals(FileLayout3.JSKPTNSYUKBN.getCol())) {
					syuKbn = StringUtils.isEmpty(val) ? "2":val;
					oshn.put(itm.getCol(), syuKbn);
				} else if (itm.getCol().equals(FileLayout3.HBSTDT.getCol())) {
					oshn.put(itm.getCol(), val);
					ochk.put(itm.getCol(), val);
				} else if (itm.getCol().equals(FileLayout3.HBEDDT.getCol())) {

					if (!StringUtils.isEmpty(val)) {
						eddt = Integer.valueOf(val);
					}
					oshn.put(itm.getCol(), val);
					ochk.put(itm.getCol(), val);
				} else if (itm.getCol().equals(FileLayout3.NNSTDT.getCol())) {
					if (!StringUtils.isEmpty(val)) {
						stdt = Integer.valueOf(val);
					} else {
						stdt = 0;
					}
					oshn.put(itm.getCol(), val);
				} else if (itm.getCol().equals(FileLayout3.NNEDDT.getCol())) {

					if (stdt == 0 && StringUtils.isEmpty(val)) {
						eddt = 0;
					} else if (!StringUtils.isEmpty(val) && Integer.valueOf(val) > eddt) {
						eddt = Integer.valueOf(val);
					}
					oshn.put(itm.getCol(), val);

				} else if (itm.getCol().equals(FileLayout3.SHUDENFLG.getCol())) {
					if (!StringUtils.isEmpty(val) && !val.equals("0") && !val.equals("1")) {
						JSONObject o = mu.getDbMessageObj("E40013", new String[]{reqNo + itm.getTxt() + "は"});
						msgList.add(o);
						return msgList;
					}
					if (StringUtils.isEmpty(val)) {
						if (!StringUtils.isEmpty(pcKbn) && pcKbn.equals("1")) {
							val = "1";
						} else {
							val = "0";
						}
					}
					oshn.put(itm.getCol(), val);
				} else if (itm.getCol().equals(FileLayout3.HIGAWRFLG.getCol()) || itm.getCol().equals(FileLayout3.YORIFLG.getCol()) || itm.getCol().equals(FileLayout3.CUTTENFLG.getCol()) ||
						itm.getCol().equals(FileLayout3.CHIRASFLG.getCol()) || itm.getCol().equals(FileLayout3.PLUSNDFLG.getCol())) {
					if (StringUtils.isEmpty(val)) {
						oshn.put(itm.getCol(), "0");
						ochk.put(itm.getCol(), "0");
					} else {
						if (!val.equals("0") && !val.equals("1")) {
							JSONObject o = mu.getDbMessageObj("E40013", new String[]{reqNo + itm.getTxt() + "は"});
							msgList.add(o);
							return msgList;
						}
						oshn.put(itm.getCol(), val);
						ochk.put(itm.getCol(), val);
					}
				} else if (itm.getCol().equals(FileLayout3.NAMANETUKBN.getCol()) &&
							!StringUtils.isEmpty(val) && !val.equals("1") && !val.equals("2")) {
					JSONObject o = mu.getDbMessageObj("E40013", new String[]{reqNo + itm.getTxt() + "は"});
					msgList.add(o);
					return msgList;
				} else if ((itm.getCol().equals(FileLayout3.KAITOFLG.getCol()) || itm.getCol().equals(FileLayout3.YOSHOKUFLG.getCol())
						) && !StringUtils.isEmpty(val) && !val.equals("0") && !val.equals("1")) {
					JSONObject o = mu.getDbMessageObj("E40013", new String[]{reqNo + itm.getTxt() + "は"});
					msgList.add(o);
					return msgList;
				} else if ((itm.getCol().equals(FileLayout3.A_WRITUKBN.getCol()) || itm.getCol().equals(FileLayout3.B_WRITUKBN.getCol()) || itm.getCol().equals(FileLayout3.C_WRITUKBN.getCol())) &&
							!StringUtils.isEmpty(val) && !val.equals("1") && !val.equals("2") && !val.equals("9")) {
					JSONObject o = mu.getDbMessageObj("E40013", new String[]{reqNo + itm.getTxt() + "は"});
					msgList.add(o);
					return msgList;
				} else if (itm.getCol().equals(FileLayout3.MEDAMAKBN.getCol()) && !StringUtils.isEmpty(val) && !val.equals("1") && !val.equals("2") && !val.equals("3")) {
					JSONObject o = mu.getDbMessageObj("E40013", new String[]{reqNo + itm.getTxt() + "は"});
					msgList.add(o);
					return msgList;
				} else if (itm.getCol().equals(FileLayout3.JUFLG.getCol()) && !StringUtils.isEmpty(val) && !val.equals("0") && !val.equals("2")) {
					JSONObject o = mu.getDbMessageObj("E40013", new String[]{reqNo + itm.getTxt() + "は"});
					msgList.add(o);
					return msgList;
				} else if (itm.getCol().equals(FileLayout3.BINKBN.getCol())) {
					// 便区分=2の場合、PLU未配信フラグは強制的に1
					if (!StringUtils.isEmpty(val) && val.equals("2") ||
							(oshn.containsKey(FileLayout3.HBSTDT.getCol()) && StringUtils.isEmpty(oshn.optString(FileLayout3.HBSTDT.getCol())) &&
									oshn.containsKey(FileLayout3.HBEDDT.getCol()) && StringUtils.isEmpty(oshn.optString(FileLayout3.HBEDDT.getCol())))) {
						if (oshn.containsKey(FileLayout3.PLUSNDFLG.getCol())) {
							oshn.replace(FileLayout3.PLUSNDFLG.getCol(), "1");
							ochk.replace(FileLayout3.PLUSNDFLG.getCol(), "1");
						} else {
							oshn.put(FileLayout3.PLUSNDFLG.getCol(), "1");
							ochk.put(FileLayout3.PLUSNDFLG.getCol(), "1");
						}
					}
					oshn.put(itm.getCol(), val);
					ochk.put(itm.getCol(), val);
				} else if (itm.getCol().equals(FileLayout3.COMMENT_TB.getCol())) {
					if (stdt == 0 && eddt == 0) {
						val = "";
					}
					oshn.put(itm.getCol(), val);
				} else {
					oshn.put(itm.getCol(), val);
				}

				if (itm.getCol().equals(FileLayout3.BYCD.getCol())) {
					break;
				}
			} else if(itm.getTbl() == RefTable.TOKTJTEN) {
				if(otjt.containsKey(itm.getCol())){
					atjt.add(otjt);
					otjt = new JSONObject();
				}

				int addten		= FileLayout3.ADDTEN1.getNo()-1;		// 追加店
				int addtenrank	= FileLayout3.ADDTENRANK1.getNo()-1;	// 追加店ランク
				int delten		= FileLayout3.DELTEN1.getNo()-1;		// 除外店

				for (int j = 0; j < 10; j++) {

					String tjflg = ""; // 対象

					String addtenCd = StringUtils.trim(String.valueOf(data[addten+j]));
					String deltenCd = StringUtils.trim(String.valueOf(data[delten+j]));

					// 対象のみ
					if (!StringUtils.isEmpty(addtenCd) && StringUtils.isEmpty(deltenCd)) {
						tjflg = "1";

					// 除外のみ
					} else if (StringUtils.isEmpty(addtenCd) && !StringUtils.isEmpty(deltenCd)) {
						tjflg = "2";

					// どちらも入力あり
					} else if (!StringUtils.isEmpty(addtenCd) && !StringUtils.isEmpty(deltenCd)) {
						tjflg = "3";
					}

					if (tjflg.equals("1")) {
						otjt.put(TOK_CMN_TJTENLayout.MOYSKBN.getCol(), moyskbn);
						otjt.put(TOK_CMN_TJTENLayout.MOYSSTDT.getCol(), moysstdt);
						otjt.put(TOK_CMN_TJTENLayout.MOYSRBAN.getCol(), moysrban);
						otjt.put(TOK_CMN_TJTENLayout.BMNCD.getCol(), bmncd);
						otjt.put(TOK_CMN_TJTENLayout.KANRINO.getCol(), kanrino);
						otjt.put(TOK_CMN_TJTENLayout.TENCD.getCol(), StringUtils.trim(String.valueOf(data[addten+j])));
						otjt.put(TOK_CMN_TJTENLayout.TENRANK.getCol(), StringUtils.trim(String.valueOf(data[addtenrank+j])));
						otjt.put(TOK_CMN_TJTENLayout.TJFLG.getCol(), tjflg);
						addTenArr.add(StringUtils.trim(String.valueOf(data[addten+j])));
						addTenRankArr.add(StringUtils.trim(String.valueOf(data[addtenrank+j])));
					} else if (tjflg.equals("2")) {
						otjt.put(TOK_CMN_TJTENLayout.MOYSKBN.getCol(), moyskbn);
						otjt.put(TOK_CMN_TJTENLayout.MOYSSTDT.getCol(), moysstdt);
						otjt.put(TOK_CMN_TJTENLayout.MOYSRBAN.getCol(), moysrban);
						otjt.put(TOK_CMN_TJTENLayout.BMNCD.getCol(), bmncd);
						otjt.put(TOK_CMN_TJTENLayout.KANRINO.getCol(), kanrino);
						otjt.put(TOK_CMN_TJTENLayout.TENCD.getCol(), StringUtils.trim(String.valueOf(data[delten+j])));
						otjt.put(TOK_CMN_TJTENLayout.TJFLG.getCol(), tjflg);
						delTenArr.add(StringUtils.trim(String.valueOf(data[delten+j])));
					} else if (tjflg.equals("3")) {
						otjt.put(TOK_CMN_TJTENLayout.MOYSKBN.getCol(), moyskbn);
						otjt.put(TOK_CMN_TJTENLayout.MOYSSTDT.getCol(), moysstdt);
						otjt.put(TOK_CMN_TJTENLayout.MOYSRBAN.getCol(), moysrban);
						otjt.put(TOK_CMN_TJTENLayout.BMNCD.getCol(), bmncd);
						otjt.put(TOK_CMN_TJTENLayout.KANRINO.getCol(), kanrino);
						otjt.put(TOK_CMN_TJTENLayout.TENCD.getCol(), StringUtils.trim(String.valueOf(data[addten+j])));
						otjt.put(TOK_CMN_TJTENLayout.TENRANK.getCol(), StringUtils.trim(String.valueOf(data[addtenrank+j])));
						otjt.put(TOK_CMN_TJTENLayout.TJFLG.getCol(), "1");
						atjt.add(otjt);
						otjt = new JSONObject();
						otjt.put(TOK_CMN_TJTENLayout.MOYSKBN.getCol(), moyskbn);
						otjt.put(TOK_CMN_TJTENLayout.MOYSSTDT.getCol(), moysstdt);
						otjt.put(TOK_CMN_TJTENLayout.MOYSRBAN.getCol(), moysrban);
						otjt.put(TOK_CMN_TJTENLayout.BMNCD.getCol(), bmncd);
						otjt.put(TOK_CMN_TJTENLayout.KANRINO.getCol(), kanrino);
						otjt.put(TOK_CMN_TJTENLayout.TENCD.getCol(), StringUtils.trim(String.valueOf(data[delten+j])));
						otjt.put(TOK_CMN_TJTENLayout.TJFLG.getCol(), "2");
						addTenArr.add(StringUtils.trim(String.valueOf(data[addten+j])));
						addTenRankArr.add(StringUtils.trim(String.valueOf(data[addtenrank+j])));
						delTenArr.add(StringUtils.trim(String.valueOf(data[delten+j])));
					}

					if (otjt.containsKey(TOK_CMN_TJTENLayout.TENCD.getCol())) {
						atjt.add(otjt);
						otjt = new JSONObject();
					}

					ochk.put("addRank",addRank);
					ochk.put("delRank",delRank);
					ochk.put("addTenArr",addTenArr);
					ochk.put("addTenRankArr",addTenRankArr);
					ochk.put("delTenArr",delTenArr);
				}
			} else if (itm.getTbl() == RefTable.OTHER) {
				if (itm.getCol().equals(FileLayout3.UPDKBN.getCol())) {
					updkbn = val;
				} else if (itm.getCol().equals(FileLayout3.ZENWARI.getCol())) {
					if (!StringUtils.isEmpty(val)) {

						// ①データ型による文字種チェック
						if(!InputChecker.checkDataType(DefineReport.DataType.SUUJI, val)){
							// エラー発生箇所を保存
							JSONObject o = mu.getDbMessageObjDataTypeErr(DefineReport.DataType.SUUJI, new String[]{reqNo + FileLayout3.ZENWARI.getTxt() + "は"});
							msgList.add(o);
							return msgList;
						}
						// ②データ桁チェック
						if(!InputChecker.checkDataLen(DefineReport.DataType.SUUJI, val, new int[]{1, 0})){
							// エラー発生箇所を保存
							JSONObject o = mu.getDbMessageObjLen(DefineReport.DataType.SUUJI, new String[]{reqNo + FileLayout3.ZENWARI.getTxt() + "は"});
							msgList.add(o);
							return msgList;
						}

						if (!val.equals("0") && !val.equals("1")) {
							JSONObject o = mu.getDbMessageObj("E40013", new String[]{reqNo + itm.getTxt() + "は"});
							msgList.add(o);
							return msgList;
						}

						if (val.length() == 1 && val.equals("1")) {
							dao.setFrmInfo("");
							oshn.replace(TOKSP_SHNLayout.ADDSHUKBN.getCol(), DefineReport.ValAddShuKbn.VAL1.getVal());
						}
					} else {
						// エラー発生箇所を保存
						JSONObject o = mu.getDbMessageObj("EX1047", new String[]{reqNo+FileLayout3.ZENWARI.getTxt()});
						msgList.add(o);
						return msgList;
					}
				} else if (itm.getCol().equals(FileLayout3.MOYSSLIDEFLG.getCol())) {
					hobokure = val;
				}
			}
		}

		// 納入日情報作成
		int nhtaisho	= FileLayout3.NHTAISHO1.getNo()-1;	// 納品対象
		int hatasu		= FileLayout3.HTASU1.getNo()-1;		// 発注総数
		int ptnno		= FileLayout3.PTNNO1.getNo()-1;		// パタン№
		int tskbn		= FileLayout3.TSKBN1.getNo()-1;		// 訂正区分
		int nndt		= FileLayout3.MYOSDT1.getNo()-1;

		boolean nhtaishoChk = false;

		for (int j = 0; j < 10; j++) {

			String getnhtaisho = StringUtils.trim(String.valueOf(data[nhtaisho+j]));
			String chkMoysDt = !StringUtils.isEmpty(String.valueOf(data[nndt+j])) ? String.valueOf(data[nndt+j]) : "";

			DataType dtype = DefineReport.DataType.DATE;
			int[] digit = new int[]{8, 0};

			// ①データ型による文字種チェック
			if(!InputChecker.checkDataType(dtype, chkMoysDt)){
				// エラー発生箇所を保存
				JSONObject o = mu.getDbMessageObjDataTypeErr(dtype, new String[]{reqNo+"催し日付"+(j+1)+"は"});
				msgList.add(o);
				return msgList;
			}
			// ②データ桁チェック
			if(!InputChecker.checkDataLen(dtype, chkMoysDt, digit)){
				// エラー発生箇所を保存
				JSONObject o = mu.getDbMessageObjLen(dtype, new String[]{reqNo+"催し日付"+(j+1)+"は"});
				msgList.add(o);
				return msgList;
			}

			int moysdt = !StringUtils.isEmpty(String.valueOf(data[nndt+j])) ? Integer.valueOf(String.valueOf(data[nndt+j])) : 0;

			// ドライ、鮮魚、精肉かつ特売スポット(ここではそれ以外あり得ない想定)かつアン有(チラシのみ)orアン有(販売・納入)時は納入対象は一つ以上必須
			if (stdt != 0 && eddt != 0) {
				if (!StringUtils.isEmpty(getnhtaisho) && getnhtaisho.equals("1") &&
						moysdt >= stdt && moysdt <= eddt) {
					nhtaishoChk = true;
				}
			} else if (stdt == 0 && eddt == 0) {
				nhtaishoChk = true;
			}

			if (!StringUtils.isEmpty(getnhtaisho) && getnhtaisho.equals("1") &&
					moysdt >= stdt && moysdt <= eddt) {

				int hat = 0;

				if (!StringUtils.isEmpty((StringUtils.trim(String.valueOf(data[hatasu+j])))) && !tenkaiKbn.equals("2")) {

					// ①データ型による文字種チェック
					if(!InputChecker.checkDataType(DefineReport.DataType.SUUJI, StringUtils.trim(String.valueOf(data[hatasu+j])))){
						// エラー発生箇所を保存
						JSONObject o = mu.getDbMessageObjDataTypeErr(DefineReport.DataType.SUUJI, new String[]{reqNo+"発注総数は"});
						msgList.add(o);
						return msgList;
					}
					// ②データ桁チェック
					if(!InputChecker.checkDataLen(DefineReport.DataType.SUUJI, StringUtils.trim(String.valueOf(data[hatasu+j])), new int[]{5, 0})){
						// エラー発生箇所を保存
						JSONObject o = mu.getDbMessageObjLen(DefineReport.DataType.SUUJI, new String[]{reqNo+"発注総数は"});
						msgList.add(o);
						return msgList;
					}

					hat = Integer.valueOf(StringUtils.trim(String.valueOf(data[hatasu+j])));
				}

				// パターン№未入力
				if (StringUtils.isEmpty(StringUtils.trim(String.valueOf(data[ptnno+j])))) {
					JSONObject o = mu.getDbMessageObj("EX1047", new String[]{reqNo + TOKTG_NNDTLayout.PTNNO.getTxt() + (j+1)});
					msgList.add(o);
					return msgList;
				} else {

					// ①データ型による文字種チェック
					if(!InputChecker.checkDataType(DefineReport.DataType.SUUJI, StringUtils.trim(String.valueOf(data[ptnno+j])))){
						// エラー発生箇所を保存
						JSONObject o = mu.getDbMessageObjDataTypeErr(DefineReport.DataType.SUUJI, new String[]{reqNo+TOKTG_NNDTLayout.PTNNO.getTxt() + (j+1) + "は"});
						msgList.add(o);
						return msgList;
					}

					int length = tenkaiKbn.equals("3")?4:3;

					// ②データ桁チェック
					if(!InputChecker.checkDataLen(DefineReport.DataType.SUUJI, StringUtils.trim(String.valueOf(data[ptnno+j])), new int[]{length, 0})){
						// エラー発生箇所を保存
						JSONObject o = mu.getDbMessageObjLen(DefineReport.DataType.SUUJI, new String[]{reqNo + TOKTG_NNDTLayout.PTNNO.getTxt() + (j+1) + "は"});
						msgList.add(o);
						return msgList;
					}
				}

				// 配列項目作成
				ArrayList<String> list = new ArrayList<String>();
				if (tenkaiKbn.equals("2")) {
					list = new ReportBM015Dao(Defines.STR_JNDI_DS).getSuryoArr(
						bmncd,moyskbn,moysstdt,moysrban,addRank,delRank,addTenArr,addTenRankArr,delTenArr,tenrank_arr,StringUtils.trim(String.valueOf(data[ptnno+j]))
					);
				} else if (tenkaiKbn.equals("3") && StringUtils.isEmpty(wwmm)) {
							JSONObject o = mu.getDbMessageObj("E40013", new String[]{reqNo + FileLayout3.JSKPTNZNENMKBN.getTxt() + "は"});
							msgList.add(o);
							return msgList;
				} else {
					// 対象店を取得
					Set<Integer> tencds = new ReportBM015Dao(Defines.STR_JNDI_DS).getTenCdAdd(bmncd,moyskbn,moysstdt,moysrban,addRank,delRank,addTenArr,delTenArr);

					list = new ReportBM015Dao(Defines.STR_JNDI_DS).getRtPtArr(
						bmncd,StringUtils.trim(String.valueOf(data[ptnno+j])),wwmm,syuKbn,StringUtils.trim(String.valueOf(data[ptnno+j])),daicd,chucd,hat,tencds,tenkaiKbn
					);
				}

				if (dao.isToktg) {
					onnd.put(TOKTG_NNDTLayout.MOYSKBN.getCol(), moyskbn);
					onnd.put(TOKTG_NNDTLayout.MOYSSTDT.getCol(), moysstdt);
					onnd.put(TOKTG_NNDTLayout.MOYSRBAN.getCol(), moysrban);
					onnd.put(TOKTG_NNDTLayout.BMNCD.getCol(), bmncd);
					onnd.put(TOKTG_NNDTLayout.KANRINO.getCol(), kanrino);
					onnd.put(TOKTG_NNDTLayout.NNDT.getCol(), StringUtils.trim(String.valueOf(data[nndt+j])));
					// 通常数パターンの場合、発注総数は不要
					if(tenkaiKbn.equals("2")) {
						onnd.put(TOKTG_NNDTLayout.HTASU.getCol(), "");
					} else {
						onnd.put(TOKTG_NNDTLayout.HTASU.getCol(), StringUtils.trim(String.valueOf(data[hatasu+j])));
					}
					onnd.put(TOKTG_NNDTLayout.PTNNO.getCol(), StringUtils.trim(String.valueOf(data[ptnno+j])));
					onnd.put(TOKTG_NNDTLayout.TSEIKBN.getCol(), StringUtils.trim(String.valueOf(data[tskbn+j])));

					// 発注数配列、展開数、店舗数
					if (list.size() != 0) {
						onnd.put(TOKTG_NNDTLayout.TENHTSU_ARR.getCol(), list.get(0));
						onnd.put(TOKTG_NNDTLayout.TPSU.getCol(), list.get(1));
						onnd.put(TOKTG_NNDTLayout.TENKAISU.getCol(), list.get(2));
						ochk.put(TOKTG_NNDTLayout.TENHTSU_ARR.getCol()+(j+1), list.get(0));
					} else {
						onnd.put(TOKTG_NNDTLayout.TENHTSU_ARR.getCol(), "");
						onnd.put(TOKTG_NNDTLayout.TENKAISU.getCol(), "");
						onnd.put(TOKTG_NNDTLayout.TPSU.getCol(), "");
						ochk.put(TOKTG_NNDTLayout.TENHTSU_ARR.getCol()+(j+1), "");
					}
					onnd.put(TOKTG_NNDTLayout.SENDFLG.getCol(), getnhtaisho);
					ochk.put(TOKTG_NNDTLayout.SENDFLG.getCol()+(j+1), getnhtaisho);
					ochk.put(TOKTG_NNDTLayout.NNDT.getCol()+(j+1), StringUtils.trim(String.valueOf(data[nndt+j])));
				} else {
					onnd.put(TOKSP_NNDTLayout.MOYSKBN.getCol(), moyskbn);
					onnd.put(TOKSP_NNDTLayout.MOYSSTDT.getCol(), moysstdt);
					onnd.put(TOKSP_NNDTLayout.MOYSRBAN.getCol(), moysrban);
					onnd.put(TOKSP_NNDTLayout.BMNCD.getCol(), bmncd);
					onnd.put(TOKSP_NNDTLayout.KANRINO.getCol(), kanrino);
					onnd.put(TOKSP_NNDTLayout.NNDT.getCol(), StringUtils.trim(String.valueOf(data[nndt+j])));
					// 通常数パターンの場合、発注総数は不要
					if(tenkaiKbn.equals("2")) {
						onnd.put(TOKSP_NNDTLayout.HTASU.getCol(), "");
					} else {
						onnd.put(TOKSP_NNDTLayout.HTASU.getCol(), StringUtils.trim(String.valueOf(data[hatasu+j])));
					}
					onnd.put(TOKSP_NNDTLayout.PTNNO.getCol(), StringUtils.trim(String.valueOf(data[ptnno+j])));
					onnd.put(TOKSP_NNDTLayout.TSEIKBN.getCol(), StringUtils.trim(String.valueOf(data[tskbn+j])));

					// 発注数配列、展開数、店舗数
					if (list.size() != 0) {
						onnd.put(TOKSP_NNDTLayout.TENHTSU_ARR.getCol(), list.get(0));
						onnd.put(TOKSP_NNDTLayout.TPSU.getCol(), list.get(1));
						onnd.put(TOKSP_NNDTLayout.TENKAISU.getCol(), list.get(2));
						ochk.put(TOKSP_NNDTLayout.TENHTSU_ARR.getCol()+(j+1), list.get(0));
					} else {
						onnd.put(TOKSP_NNDTLayout.TENHTSU_ARR.getCol(), "");
						onnd.put(TOKSP_NNDTLayout.TENKAISU.getCol(), "");
						onnd.put(TOKSP_NNDTLayout.TPSU.getCol(), "");
						ochk.put(TOKSP_NNDTLayout.TENHTSU_ARR.getCol()+(j+1), "");
					}
					onnd.put(TOKSP_NNDTLayout.SENDFLG.getCol(), getnhtaisho);
					onnd.put(TOKSP_NNDTLayout.OPERATOR.getCol(), oldBin);
					ochk.put(TOKSP_NNDTLayout.SENDFLG.getCol()+(j+1), getnhtaisho);
					ochk.put(TOKSP_NNDTLayout.NNDT.getCol()+(j+1), StringUtils.trim(String.valueOf(data[nndt+j])));
				}

				annd.add(onnd);
				onnd = new JSONObject();
			} else if (!StringUtils.isEmpty(getnhtaisho) && !getnhtaisho.equals("0") && !getnhtaisho.equals("1")) {
				JSONObject o = mu.getDbMessageObj("E40013", new String[]{reqNo + "納品対象" + (j+1) + "は"});
				msgList.add(o);
				return msgList;
			}
		}

		if (!nhtaishoChk) {
			JSONObject o = mu.getDbMessageObj("E20363", new String[]{reqNo});
			msgList.add(o);
			return msgList;
		}

		if (tkanplukbn.equals("2")) {
			oshn.put(TOKTG_SHNLayout.A_BAIKAAM.getCol(), a_baikaam1);
			oshn.put(TOKTG_SHNLayout.B_BAIKAAM.getCol(), b_baikaam1);
			oshn.put(TOKTG_SHNLayout.C_BAIKAAM.getCol(), c_baikaam1);
		} else if (tkanplukbn.equals("1") || (!dao.isFrm2 && !dao.isFrm3)) {
			oshn.put(TOKTG_SHNLayout.A_BAIKAAM.getCol(), a_baikaam2);
			oshn.put(TOKTG_SHNLayout.B_BAIKAAM.getCol(), b_baikaam2);
			oshn.put(TOKTG_SHNLayout.C_BAIKAAM.getCol(), c_baikaam2);
		}

		if (StringUtils.isEmpty(tenrank_arr)) {
			tenrank_arr = " ";
		}

		if (dao.isToktg) {
			oshn.put(TOKTG_SHNLayout.DAICD.getCol(), daicd);
			oshn.put(TOKTG_SHNLayout.CHUCD.getCol(), chucd);
			oshn.put(TOKTG_SHNLayout.TENRANK_ARR.getCol(), tenrank_arr);
		} else {
			oshn.put(TOKSP_SHNLayout.DAICD.getCol(), daicd);
			oshn.put(TOKSP_SHNLayout.CHUCD.getCol(), chucd);
			oshn.put(TOKSP_SHNLayout.TENRANK_ARR.getCol(), tenrank_arr);
		}

		achk.add(ochk);
		ochk = new JSONObject();

		// 変数を初期化
		sbSQL	= new StringBuffer();
		iL		= new ItemList();
		dbDatas = new JSONArray();
		sqlWhere	= "";
		paramData	= new ArrayList<String>();

		String sqlWith = "";
		// 部門コード
		if (StringUtils.isEmpty(bmncd)) {
			sqlWith += "BMNCD=null ";
		} else {
			sqlWith += "BMNCD=? ";
			paramData.add(bmncd);
		}

		// 登録種別の取得
		if (dao.isToktg) {
			sqlFrom = "INATK.TOKTG_SHN ";
		} else {
			sqlFrom = "INATK.TOKSP_SHN ";
		}

		String sqlWhere2 = "";

		// 催し区分
		if (StringUtils.isEmpty(moyskbn)) {
			sqlWhere2 += "MOYSKBN=null AND ";
		} else {
			sqlWhere2 += "MOYSKBN=? AND ";
			paramData.add(moyskbn);
		}

		// 催し開始日
		if (StringUtils.isEmpty(moysstdt)) {
			sqlWhere2 += "MOYSSTDT=null AND ";
		} else {
			sqlWhere2 += "MOYSSTDT=? AND ";
			paramData.add(moysstdt);
		}

		// 催し連番
		if (StringUtils.isEmpty(moysrban)) {
			sqlWhere2 += "MOYSRBAN=null AND ";
		} else {
			sqlWhere2 += "MOYSRBAN=? AND ";
			paramData.add(moysrban);
		}

		// 部門コード
		if (StringUtils.isEmpty(bmncd)) {
			sqlWhere2 += "BMNCD=null AND ";
		} else {
			sqlWhere2 += "BMNCD=? AND ";
			paramData.add(bmncd);
		}

		// 管理番号
		if (StringUtils.isEmpty(kanrino)) {
			sqlWhere2 += "KANRINO=null AND ";
		} else {
			sqlWhere2 += "KANRINO=? AND ";
			paramData.add(kanrino);
		}

		// 商品コード
		if (StringUtils.isEmpty(shncd)) {
			sqlWhere2 += "SHNCD=null AND ";
		} else {
			sqlWhere2 += "SHNCD=? AND ";
			paramData.add(shncd);
		}

		// 催し区分
		if (StringUtils.isEmpty(moyskbn)) {
			sqlWhere += "T1.MOYSKBN=null AND ";
		} else {
			sqlWhere += "T1.MOYSKBN=? AND ";
			paramData.add(moyskbn);
		}

		// 催し開始日
		if (StringUtils.isEmpty(moysstdt)) {
			sqlWhere += "T1.MOYSSTDT=null AND ";
		} else {
			sqlWhere += "T1.MOYSSTDT=? AND ";
			paramData.add(moysstdt);
		}

		// 催し連番
		if (StringUtils.isEmpty(moysrban)) {
			sqlWhere += "T1.MOYSRBAN=null AND ";
		} else {
			sqlWhere += "T1.MOYSRBAN=? AND ";
			paramData.add(moysrban);
		}

		sbSQL.append("WITH DF as (  ");
		sbSQL.append("SELECT ");
		sbSQL.append("DBMNATRKBN ");
		sbSQL.append("FROM ");
		sbSQL.append("INATK.TOKMOYDEF ");
		sbSQL.append("WHERE ");
		sbSQL.append(sqlWith);
		sbSQL.append(") ");
		sbSQL.append("SELECT ");
		sbSQL.append("CASE  ");
		sbSQL.append("WHEN T2.MOYSKBN IS NOT NULL  ");
		sbSQL.append("THEN T2.MYOSNNSTDT  ");
		sbSQL.append("WHEN DF.DBMNATRKBN = 1  ");
		sbSQL.append("THEN T1.NNSTDT_TGF  ");
		sbSQL.append("ELSE T1.NNSTDT  ");
		sbSQL.append("END NNSTDT ");
		sbSQL.append(", CASE  ");
		sbSQL.append("WHEN T2.MOYSKBN IS NOT NULL  ");
		sbSQL.append("THEN T2.MYOSNNEDDT  ");
		sbSQL.append("WHEN DF.DBMNATRKBN = 1  ");
		sbSQL.append("THEN T1.NNEDDT_TGF  ");
		sbSQL.append("ELSE T1.NNEDDT  ");
		sbSQL.append("END NNEDDT ");
		sbSQL.append(", CASE  ");
		sbSQL.append("WHEN T2.MOYSKBN IS NOT NULL  ");
		sbSQL.append("THEN T2.MYOSHBSTDT  ");
		sbSQL.append("ELSE T1.HBSTDT  ");
		sbSQL.append("END HBSTDT ");
		sbSQL.append(", CASE  ");
		sbSQL.append("WHEN T2.MOYSKBN IS NOT NULL  ");
		sbSQL.append("THEN T2.MYOSHBEDDT  ");
		sbSQL.append("ELSE T1.HBEDDT  ");
		sbSQL.append("END HBEDDT  ");
		sbSQL.append("FROM ");
		sbSQL.append("INATK.TOKMOYCD T1  ");
		sbSQL.append("left join (SELECT * FROM "+sqlFrom+" WHERE " + sqlWhere2 + " UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal() + ") T2 ");
		sbSQL.append("ON T1.MOYSKBN = T2.MOYSKBN  ");
		sbSQL.append("and T1.MOYSSTDT = T2.MOYSSTDT  ");
		sbSQL.append("and T1.MOYSRBAN = T2.MOYSRBAN ");
		sbSQL.append(", DF  ");
		sbSQL.append("WHERE ");
		sbSQL.append(sqlWhere);
		sbSQL.append("T1.UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal() + " ");

		dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

		if (dbDatas.size() != 0){
			if (dao.isToktg) {
				oshn.put(TOKTG_SHNLayout.MYOSNNSTDT.getCol(), dbDatas.getJSONObject(0).optString("NNSTDT"));
				oshn.put(TOKTG_SHNLayout.MYOSNNEDDT.getCol(), dbDatas.getJSONObject(0).optString("NNEDDT"));
				oshn.put(TOKTG_SHNLayout.MYOSHBSTDT.getCol(), dbDatas.getJSONObject(0).optString("HBSTDT"));
				oshn.put(TOKTG_SHNLayout.MYOSHBEDDT.getCol(), dbDatas.getJSONObject(0).optString("HBEDDT"));
			} else {
				oshn.put(TOKSP_SHNLayout.MYOSNNSTDT.getCol(), dbDatas.getJSONObject(0).optString("NNSTDT"));
				oshn.put(TOKSP_SHNLayout.MYOSNNEDDT.getCol(), dbDatas.getJSONObject(0).optString("NNEDDT"));
				oshn.put(TOKSP_SHNLayout.MYOSHBSTDT.getCol(), dbDatas.getJSONObject(0).optString("HBSTDT"));
				oshn.put(TOKSP_SHNLayout.MYOSHBEDDT.getCol(), dbDatas.getJSONObject(0).optString("HBEDDT"));
			}
		}

		ashn.add(oshn);
		ahb.add(ohb);
		oshn = new JSONObject();
		otjt = new JSONObject();
		onnd = new JSONObject();
		ohb = new JSONObject();

		return msgList;
	}

	public void dataCreate(Object[] data) {

		StringBuffer		sbSQL	= new StringBuffer();
		ItemList			iL		= new ItemList();
		JSONArray			dbDatas	= new JSONArray();
		ArrayList<String>	paramData = new ArrayList<String>();

		// DB検索用パラメータ
		String sqlWhere	= "";
		String sqlFrom	= "";

		String moyskbn	= "";
		String moysstdt	= "";
		String moysrban	= "";
		String kanrino	= "";
		String bmncd	= "";

		Set<String> moyscdSet	= new TreeSet<String>();

		String tenrank_arr = "";
		String addRank = "";
		String delRank = "";
		JSONArray addTenArr = new JSONArray();
		JSONArray addTenRankArr = new JSONArray();
		JSONArray delTenArr = new JSONArray();
		String tenkaiKbn = "";
		String wwmm = "";
		String daicd = "";
		String chucd = "";
		String shocd = "";
		String shncd = "";
		String syuKbn = "";

		String a_baikaam2 = "";
		String a_baikaam1 = "";
		String b_baikaam2 = "";
		String b_baikaam1 = "";
		String c_baikaam2 = "";
		String c_baikaam1 = "";
		String tkanplukbn = "";
		String pcKbn = "";

		for(FileLayout3 itm :FileLayout3.values()){
			String val = StringUtils.trim((String) data[itm.getNo()-1]);
			// 1.""で囲まれていた場合除去 2.末尾空白除去
			val = StringUtils.stripEnd(val.replaceFirst("^\"(.*)\"$", "$1"), null);

			if(itm.getTbl() == RefTable.TOKSHN){
				if(oshn.containsKey(itm.getCol())){
					ashn.add(oshn);
					oshn = new JSONObject();
				}
				if (itm.getCol().equals(FileLayout3.MOYSCD.getCol())) {

					if (!moyscdSet.contains(val)) {
						if (moyscdSet.size() != 0) {
						} else {
							moyscdSet.add(val);
						}
					}

					if (val.length() >= 8) {
						moyskbn		= val.substring(0,1);
						moysstdt	= val.substring(1,7);
						moysrban	= String.valueOf(Integer.valueOf(val.substring(7)));
					} else if (val.length() >= 7) {
						moyskbn		= val.substring(0,1);
						moysstdt	= val.substring(1,7);
					} else if (val.length() >= 1) {
						moyskbn		= val.substring(0,1);
					}

					// 登録種別の取得
					// 催し種類情報設定
					dao.setMoycdInfo(moyskbn, moysstdt, moysrban, bmncd);

					oshn.put(TOKSP_SHNLayout.MOYSKBN.getCol(), moyskbn);
					oshn.put(TOKSP_SHNLayout.MOYSSTDT.getCol(), moysstdt);
					oshn.put(TOKSP_SHNLayout.MOYSRBAN.getCol(), moysrban);

					ohb.put(TOKSP_HBLayout.MOYSKBN.getCol(), moyskbn);
					ohb.put(TOKSP_HBLayout.MOYSSTDT.getCol(), moysstdt);
					ohb.put(TOKSP_HBLayout.MOYSRBAN.getCol(), moysrban);

				} else if (itm.getCol().equals(FileLayout3.BMNCD.getCol())) {
					bmncd = val;
					oshn.put(TOKSP_SHNLayout.BMNCD.getCol(), bmncd);
					ohb.put(TOKSP_HBLayout.BMNCD.getCol(), bmncd);

				} else if (itm.getCol().equals(FileLayout3.KANRINO.getCol())) {

					if (updkbn.equals("A")) {
						kanrino = "";
					} else {
						kanrino = !StringUtils.isEmpty(val) && NumberUtils.isNumber(val) ? String.valueOf(Integer.valueOf(val)) : val;
					}

					oshn.put(TOKSP_SHNLayout.KANRINO.getCol(), kanrino);
					ohb.put(TOKSP_HBLayout.KANRINO.getCol(), kanrino);

					// 変数を初期化
					sbSQL	= new StringBuffer();
					iL		= new ItemList();
					dbDatas = new JSONArray();
					sqlWhere	= "";
					paramData	= new ArrayList<String>();

					// 登録種別の取得
					if (dao.isToktg) {
						sqlFrom = "INATK.TOKTG_SHN ";
					} else {
						sqlFrom = "INATK.TOKSP_SHN ";
					}

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
						sqlWhere += "MOYSRBAN=null AND ";
					} else {
						sqlWhere += "MOYSRBAN=? AND ";
						paramData.add(moysrban);
					}

					// 部門コード
					if (StringUtils.isEmpty(bmncd)) {
						sqlWhere += "BMNCD=null AND ";
					} else {
						sqlWhere += "BMNCD=? AND ";
						paramData.add(bmncd);
					}

					// 管理番号
					if (StringUtils.isEmpty(kanrino)) {
						sqlWhere += "KANRINO=null ";
					} else {
						sqlWhere += "KANRINO=? ";
						paramData.add(kanrino);
					}

					sbSQL.append("SELECT DISTINCT ");
					sbSQL.append("ADDSHUKBN ");		// 登録種別
					sbSQL.append(",TENRANK_ARR ");	// 店ランク配列
					sbSQL.append(",KANRIENO ");		// 管理枝番
					sbSQL.append("FROM ");
					sbSQL.append(sqlFrom);
					sbSQL.append("WHERE ");
					sbSQL.append(sqlWhere);		// 入力された催しコード

					dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

					if (dbDatas.size() != 0){
						oshn.put(TOKSP_SHNLayout.ADDSHUKBN.getCol(), dbDatas.getJSONObject(0).optString("ADDSHUKBN"));
						oshn.put(TOKSP_SHNLayout.KANRIENO.getCol(), dbDatas.getJSONObject(0).optString("KANRIENO"));
						dao.setFrmInfo(dbDatas.getJSONObject(0).optString("ADDSHUKBN"));
						tenrank_arr = dbDatas.getJSONObject(0).optString("TENRANK_ARR");
					} else {
						if (bmncd.equals("2") || bmncd.equals("9") || bmncd.equals("15")) {
							oshn.put(TOKSP_SHNLayout.ADDSHUKBN.getCol(), DefineReport.ValAddShuKbn.VAL3.getVal());
							dao.setFrmInfo(DefineReport.ValAddShuKbn.VAL3.getVal());
						} else if (bmncd.equals("4") || bmncd.equals("6")) {
							oshn.put(TOKSP_SHNLayout.ADDSHUKBN.getCol(), DefineReport.ValAddShuKbn.VAL4.getVal());
							dao.setFrmInfo(DefineReport.ValAddShuKbn.VAL4.getVal());
						} else if (bmncd.equals("5")) {
							oshn.put(TOKSP_SHNLayout.ADDSHUKBN.getCol(), DefineReport.ValAddShuKbn.VAL5.getVal());
							dao.setFrmInfo(DefineReport.ValAddShuKbn.VAL5.getVal());
						} else {
							oshn.put(TOKSP_SHNLayout.ADDSHUKBN.getCol(), DefineReport.ValAddShuKbn.VAL2.getVal());
							dao.setFrmInfo(DefineReport.ValAddShuKbn.VAL2.getVal());
						}
					}

					// 変数を初期化
					sbSQL	= new StringBuffer();
					iL		= new ItemList();
					dbDatas = new JSONArray();
					sqlWhere	= "";
					paramData	= new ArrayList<String>();

					// 登録種別の取得
					if (!dao.isToktg) {
						sqlFrom = "INATK.TOKSP_HB ";

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
							sqlWhere += "MOYSRBAN=null AND ";
						} else {
							sqlWhere += "MOYSRBAN=? AND ";
							paramData.add(moysrban);
						}

						// 部門コード
						if (StringUtils.isEmpty(bmncd)) {
							sqlWhere += "BMNCD=null AND ";
						} else {
							sqlWhere += "BMNCD=? AND ";
							paramData.add(bmncd);
						}

						// 管理番号
						if (StringUtils.isEmpty(kanrino)) {
							sqlWhere += "KANRINO=null ";
						} else {
							sqlWhere += "KANRINO=? ";
							paramData.add(kanrino);
						}

						sbSQL.append("SELECT DISTINCT ");
						sbSQL.append(" TENATSUK_ARR ");	// 店扱いフラグ
						sbSQL.append("FROM ");
						sbSQL.append(sqlFrom);
						sbSQL.append("WHERE ");
						sbSQL.append(sqlWhere);		// 入力された催しコード

						dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

						if (dbDatas.size() != 0){
							ohb.put(TOKSP_HBLayout.TENATSUK_ARR.getCol(), dbDatas.getJSONObject(0).optString("TENATSUK_ARR"));
						}
					}
				} else if (itm.getCol().equals(FileLayout3.SHNCD.getCol())) {
					val = !StringUtils.isEmpty(val) && val.length() <= 7 && NumberUtils.isNumber(val) ? String.format("%08d", Integer.valueOf(val)) : val;
					shncd = val;
					oshn.put(itm.getCol(), val);

					// 変数を初期化
					sbSQL	= new StringBuffer();
					iL		= new ItemList();
					dbDatas = new JSONArray();
					sqlWhere	= "";
					paramData	= new ArrayList<String>();

					// 登録種別の取得
					sqlFrom = "INAMS.MSTSHN ";

					// 商品コード
					if (StringUtils.isEmpty(val)) {
						sqlWhere += "SHNCD=null ";
					} else {
						sqlWhere += "SHNCD=? ";
						paramData.add(val);
					}

					sbSQL.append("SELECT ");
					sbSQL.append("PCKBN ");	// PC区分
					sbSQL.append(",DAICD ");	// 大分類
					sbSQL.append(",CHUCD ");	// 中分類
					sbSQL.append(",SHOCD ");	// 中分類
					sbSQL.append("FROM ");
					sbSQL.append(sqlFrom);
					sbSQL.append("WHERE ");
					sbSQL.append(sqlWhere);		// 入力された催しコード

					dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

					if (dbDatas.size() >= 1) {
						pcKbn = dbDatas.getJSONObject(0).optString("PCKBN");
						daicd = dbDatas.getJSONObject(0).optString("DAICD");
						chucd = dbDatas.getJSONObject(0).optString("CHUCD");
						shocd = dbDatas.getJSONObject(0).optString("SHOCD");
					}
				} else if (itm.getCol().equals(FileLayout3.A_BAIKAAM2.getCol())) {
					a_baikaam2 = val;
				} else if (itm.getCol().equals(FileLayout3.A_BAIKAAM1.getCol())) {
					a_baikaam1 = val;
				} else if (itm.getCol().equals(FileLayout3.B_BAIKAAM2.getCol())) {
					b_baikaam2 = val;
				} else if (itm.getCol().equals(FileLayout3.B_BAIKAAM1.getCol())) {
					b_baikaam1 = val;
				} else if (itm.getCol().equals(FileLayout3.C_BAIKAAM2.getCol())) {
					c_baikaam2 = val;
				} else if (itm.getCol().equals(FileLayout3.C_BAIKAAM1.getCol())) {
					c_baikaam1 = val;
				} else if (itm.getCol().equals(FileLayout3.TKANPLUKBN.getCol())) {
					if (!dao.isFrm2 && !dao.isFrm3) {
						tkanplukbn = "";
						oshn.put(itm.getCol(), "");
					} else {
						tkanplukbn = val;
						oshn.put(itm.getCol(), val);
					}
				} else if (itm.getCol().equals(FileLayout3.HTGENBAIKAFLG.getCol())) {
					if (!dao.isFrm2 && !dao.isFrm3) {
						oshn.put(itm.getCol(), "");
					} else {
						if (StringUtils.isEmpty(val)) {
							val = "0";
						}
						oshn.put(itm.getCol(), val);
					}
				} else if (itm.getCol().equals(FileLayout3.HBSLIDEFLG.getCol())) {
					if (dao.isToktg) {
						oshn.put(TOKTG_SHNLayout.HBSLIDEFLG.getCol(), val);
					}
				} else if (itm.getCol().equals(FileLayout3.NHSLIDEFLG.getCol())) {
					if (dao.isToktg) {
						oshn.put(TOKTG_SHNLayout.NHSLIDEFLG.getCol(), val);
					}
				} else if (itm.getCol().equals(FileLayout3.RANKNO_ADD_A.getCol())) {
					if (dao.isToktg) {
						oshn.put(TOKTG_SHNLayout.RANKNO_ADD.getCol(), val);
					} else {
						oshn.put(TOKSP_SHNLayout.RANKNO_ADD_A.getCol(), val);
					}
					addRank = val;
				} else if (itm.getCol().equals(FileLayout3.RANKNO_ADD_B.getCol())) {
					if (!dao.isToktg) {
						oshn.put(TOKSP_SHNLayout.RANKNO_ADD_B.getCol(), val);
					}
				} else if (itm.getCol().equals(FileLayout3.RANKNO_ADD_C.getCol())) {
					if (!dao.isToktg) {
						oshn.put(TOKSP_SHNLayout.RANKNO_ADD_C.getCol(), val);
					}
				} else if (itm.getCol().equals(FileLayout3.RANKNO_DEL.getCol())) {
					if (!dao.isToktg) {
						oshn.put(TOKSP_SHNLayout.RANKNO_DEL.getCol(), val);
						delRank = val;
					}
				} else if (itm.getCol().equals(FileLayout3.TENKAIKBN.getCol())) {
					tenkaiKbn = StringUtils.isEmpty(val) ? "2":val;
					oshn.put(itm.getCol(), tenkaiKbn);
				} else if (itm.getCol().equals(FileLayout3.JSKPTNZNENWKBN.getCol())) {
					if (!StringUtils.isEmpty(val)) {
						wwmm = "1";
						oshn.put(itm.getCol(), val);
					} else {
						oshn.put(itm.getCol(), "1");
					}
				} else if (itm.getCol().equals(FileLayout3.JSKPTNZNENMKBN.getCol())) {
					if (!StringUtils.isEmpty(val)) {
						wwmm = "2";
					}
					oshn.put(itm.getCol(), val);
				} else if (itm.getCol().equals(FileLayout3.HBEDDT.getCol())) {

					if (!StringUtils.isEmpty(val)) {
						eddt = Integer.valueOf(val);
					}
					oshn.put(itm.getCol(), val);
				} else if (itm.getCol().equals(FileLayout3.NNSTDT.getCol())) {
					if (!StringUtils.isEmpty(val)) {
						stdt = Integer.valueOf(val);
					} else {
						stdt = 0;
					}
					oshn.put(itm.getCol(), val);
				} else if (itm.getCol().equals(FileLayout3.NNEDDT.getCol())) {
					if (stdt == 0 && StringUtils.isEmpty(val)) {
						eddt = 0;
					} else if (!StringUtils.isEmpty(val) && Integer.valueOf(val) > eddt) {
						eddt = Integer.valueOf(val);
					}
					oshn.put(itm.getCol(), val);
				} else if (itm.getCol().equals(FileLayout3.JSKPTNSYUKBN.getCol())) {
					syuKbn = StringUtils.isEmpty(val) ? "2":val;
					oshn.put(itm.getCol(), syuKbn);
				} else if (itm.getCol().equals(FileLayout3.SHUDENFLG.getCol())) {
					if (StringUtils.isEmpty(val)) {
						if (!StringUtils.isEmpty(pcKbn) && pcKbn.equals("1")) {
							val = "1";
						} else {
							val = "0";
						}
					}
					oshn.put(itm.getCol(), val);
				} else if (itm.getCol().equals(FileLayout3.HIGAWRFLG.getCol()) || itm.getCol().equals(FileLayout3.YORIFLG.getCol()) || itm.getCol().equals(FileLayout3.CUTTENFLG.getCol()) ||
						itm.getCol().equals(FileLayout3.CHIRASFLG.getCol()) || itm.getCol().equals(FileLayout3.PLUSNDFLG.getCol())) {
					if (StringUtils.isEmpty(val)) {
						oshn.put(itm.getCol(), "0");
					} else {
						oshn.put(itm.getCol(), val);
					}
				} else if (itm.getCol().equals(FileLayout3.BINKBN.getCol())) {
					// 便区分=2の場合、PLU未配信フラグは強制的に1
					if (!StringUtils.isEmpty(val) && val.equals("2") ||
							(oshn.containsKey(FileLayout3.HBSTDT.getCol()) && StringUtils.isEmpty(oshn.optString(FileLayout3.HBSTDT.getCol())) &&
									oshn.containsKey(FileLayout3.HBEDDT.getCol()) && StringUtils.isEmpty(oshn.optString(FileLayout3.HBEDDT.getCol())))) {
						if (oshn.containsKey(FileLayout3.PLUSNDFLG.getCol())) {
							oshn.replace(FileLayout3.PLUSNDFLG.getCol(), "1");
						} else {
							oshn.put(FileLayout3.PLUSNDFLG.getCol(), "1");
						}
					}
					oshn.put(itm.getCol(), val);
				} else if (itm.getCol().equals(FileLayout3.COMMENT_TB.getCol())) {
					if (stdt == 0 && eddt == 0) {
						val = "";
					}
					oshn.put(itm.getCol(), val);
				} else {
					oshn.put(itm.getCol(), val);
				}

				if (itm.getCol().equals(FileLayout3.BYCD.getCol())) {
					break;
				}
			} else if(itm.getTbl() == RefTable.TOKTJTEN) {
				if(otjt.containsKey(itm.getCol())){
					atjt.add(otjt);
					otjt = new JSONObject();
				}

				int addten		= FileLayout3.ADDTEN1.getNo()-1;		// 追加店
				int addtenrank	= FileLayout3.ADDTENRANK1.getNo()-1;	// 追加店ランク
				int delten		= FileLayout3.DELTEN1.getNo()-1;		// 除外店

				for (int j = 0; j < 10; j++) {

					String tjflg = ""; // 対象

					String addtenCd = StringUtils.trim(String.valueOf(data[addten+j]));
					String deltenCd = StringUtils.trim(String.valueOf(data[delten+j]));

					// 対象のみ
					if (!StringUtils.isEmpty(addtenCd) && StringUtils.isEmpty(deltenCd)) {
						tjflg = "1";

					// 除外のみ
					} else if (StringUtils.isEmpty(addtenCd) && !StringUtils.isEmpty(deltenCd)) {
						tjflg = "2";

					// どちらも入力あり
					} else if (!StringUtils.isEmpty(addtenCd) && !StringUtils.isEmpty(deltenCd)) {
						tjflg = "3";
					}

					if (tjflg.equals("1")) {
						otjt.put(TOK_CMN_TJTENLayout.MOYSKBN.getCol(), moyskbn);
						otjt.put(TOK_CMN_TJTENLayout.MOYSSTDT.getCol(), moysstdt);
						otjt.put(TOK_CMN_TJTENLayout.MOYSRBAN.getCol(), moysrban);
						otjt.put(TOK_CMN_TJTENLayout.BMNCD.getCol(), bmncd);
						otjt.put(TOK_CMN_TJTENLayout.KANRINO.getCol(), kanrino);
						otjt.put(TOK_CMN_TJTENLayout.TENCD.getCol(), StringUtils.trim(String.valueOf(data[addten+j])));
						otjt.put(TOK_CMN_TJTENLayout.TENRANK.getCol(), StringUtils.trim(String.valueOf(data[addtenrank+j])));
						otjt.put(TOK_CMN_TJTENLayout.TJFLG.getCol(), tjflg);
						otjt.put(TOK_CMN_TJTENLayout.SENDFLG.getCol(), 0);
						addTenArr.add(StringUtils.trim(String.valueOf(data[addten+j])));
						addTenRankArr.add(StringUtils.trim(String.valueOf(data[addtenrank+j])));
					} else if (tjflg.equals("2")) {
						otjt.put(TOK_CMN_TJTENLayout.MOYSKBN.getCol(), moyskbn);
						otjt.put(TOK_CMN_TJTENLayout.MOYSSTDT.getCol(), moysstdt);
						otjt.put(TOK_CMN_TJTENLayout.MOYSRBAN.getCol(), moysrban);
						otjt.put(TOK_CMN_TJTENLayout.BMNCD.getCol(), bmncd);
						otjt.put(TOK_CMN_TJTENLayout.KANRINO.getCol(), kanrino);
						otjt.put(TOK_CMN_TJTENLayout.TENCD.getCol(), StringUtils.trim(String.valueOf(data[delten+j])));
						otjt.put(TOK_CMN_TJTENLayout.TJFLG.getCol(), tjflg);
						otjt.put(TOK_CMN_TJTENLayout.SENDFLG.getCol(), 0);
						delTenArr.add(StringUtils.trim(String.valueOf(data[delten+j])));
					} else if (tjflg.equals("3")) {
						otjt.put(TOK_CMN_TJTENLayout.MOYSKBN.getCol(), moyskbn);
						otjt.put(TOK_CMN_TJTENLayout.MOYSSTDT.getCol(), moysstdt);
						otjt.put(TOK_CMN_TJTENLayout.MOYSRBAN.getCol(), moysrban);
						otjt.put(TOK_CMN_TJTENLayout.BMNCD.getCol(), bmncd);
						otjt.put(TOK_CMN_TJTENLayout.KANRINO.getCol(), kanrino);
						otjt.put(TOK_CMN_TJTENLayout.TENCD.getCol(), StringUtils.trim(String.valueOf(data[addten+j])));
						otjt.put(TOK_CMN_TJTENLayout.TENRANK.getCol(), StringUtils.trim(String.valueOf(data[addtenrank+j])));
						otjt.put(TOK_CMN_TJTENLayout.TJFLG.getCol(), "1");
						otjt.put(TOK_CMN_TJTENLayout.SENDFLG.getCol(), 0);
						atjt.add(otjt);
						otjt = new JSONObject();
						otjt.put(TOK_CMN_TJTENLayout.MOYSKBN.getCol(), moyskbn);
						otjt.put(TOK_CMN_TJTENLayout.MOYSSTDT.getCol(), moysstdt);
						otjt.put(TOK_CMN_TJTENLayout.MOYSRBAN.getCol(), moysrban);
						otjt.put(TOK_CMN_TJTENLayout.BMNCD.getCol(), bmncd);
						otjt.put(TOK_CMN_TJTENLayout.KANRINO.getCol(), kanrino);
						otjt.put(TOK_CMN_TJTENLayout.TENCD.getCol(), StringUtils.trim(String.valueOf(data[delten+j])));
						otjt.put(TOK_CMN_TJTENLayout.TJFLG.getCol(), "2");
						otjt.put(TOK_CMN_TJTENLayout.SENDFLG.getCol(), 0);
						addTenArr.add(StringUtils.trim(String.valueOf(data[addten+j])));
						addTenRankArr.add(StringUtils.trim(String.valueOf(data[addtenrank+j])));
						delTenArr.add(StringUtils.trim(String.valueOf(data[delten+j])));
					}

					if (otjt.containsKey(TOK_CMN_TJTENLayout.TENCD.getCol())) {
						atjt.add(otjt);
						otjt = new JSONObject();
					}
				}
			} else if (itm.getTbl() == RefTable.OTHER) {

				if (itm.getCol().equals(FileLayout3.UPDKBN.getCol())) {
					updkbn = val;
				} else if (itm.getCol().equals(FileLayout3.ZENWARI.getCol())) {
					if (!StringUtils.isEmpty(val)) {
						if (val.length() == 1 && val.equals("1")) {
							dao.setFrmInfo("");
							oshn.replace(TOKSP_SHNLayout.ADDSHUKBN.getCol(), DefineReport.ValAddShuKbn.VAL1.getVal());
						}
					}
				} else if (itm.getCol().equals(FileLayout3.MOYSSLIDEFLG.getCol())) {
					hobokure = val;
				}
			}
		}

		// 対象除外店の物理削除対象となるデータが存在するか確認
		if (!updkbn.equals("A")) {

			// 変数を初期化
			sbSQL	= new StringBuffer();
			iL		= new ItemList();
			dbDatas = new JSONArray();
			sqlWhere	= "";
			paramData	= new ArrayList<String>();
			// 物理削除対象データを取得
			if (dao.isToktg) {
				sqlFrom = "INATK.TOKTG_TJTEN T1 ";
			} else {
				sqlFrom = "INATK.TOKSP_TJTEN T1 ";
			}

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
				sqlWhere += "MOYSRBAN=null AND ";
			} else {
				sqlWhere += "MOYSRBAN=? AND ";
				paramData.add(moysrban);
			}

			// 部門コード
			if (StringUtils.isEmpty(bmncd)) {
				sqlWhere += "BMNCD=null AND ";
			} else {
				sqlWhere += "BMNCD=? AND ";
				paramData.add(bmncd);
			}

			// 管理番号
			if (StringUtils.isEmpty(kanrino)) {
				sqlWhere += "KANRINO=null ";
			} else {
				sqlWhere += "KANRINO=? ";
				paramData.add(kanrino);
			}

			sbSQL.append("SELECT T1.* ");
			sbSQL.append("FROM " + sqlFrom);
			sbSQL.append("WHERE ");
			sbSQL.append(sqlWhere);

			if (atjt.size() != 0) {
				// 今回物理削除対象のデータが存在するか確認
				String values = "", names = "", rows = "";
				String[] notTarget = new String[]{TOK_CMN_TJTENLayout.KANRIENO.getId(),TOK_CMN_TJTENLayout.SENDFLG.getId(),TOK_CMN_TJTENLayout.OPERATOR.getId(), TOK_CMN_TJTENLayout.ADDDT.getId(), TOK_CMN_TJTENLayout.UPDDT.getId()};
				for (int i = 0; i < atjt.size(); i++) {
					JSONObject getData = atjt.getJSONObject(i);
					values = "";
					names = "";
					for (TOK_CMN_TJTENLayout itm :  TOK_CMN_TJTENLayout.values()) {
						if(ArrayUtils.contains(notTarget, itm.getId())){ continue;}	// パラメータ不要

						String col = itm.getCol();
						String val = StringUtils.trim(getData.optString(col));
						if(StringUtils.isEmpty(val)){
							values += ", null";
						}else{
							paramData.add(val);
							values += ", cast(? as varchar("+MessageUtility.getDefByteLen(val)+"))";
						}
						names  += ", "+col;
					}
					rows += ",("+StringUtils.removeStart(values, ",")+")";
				}

				rows = StringUtils.removeStart(rows, ",");
				names  = StringUtils.removeStart(names, ",");

				sbSQL.append(" AND NOT EXISTS(SELECT 1 ");
				sbSQL.append(" from (values"+rows+") as T2("+names+")");
				sbSQL.append(" WHERE ");
				sbSQL.append("T1.MOYSKBN = T2.MOYSKBN ");
				sbSQL.append("and T1.MOYSSTDT = T2.MOYSSTDT ");
				sbSQL.append("and T1.MOYSRBAN = T2.MOYSRBAN ");
				sbSQL.append("and T1.BMNCD = T2.BMNCD ");
				sbSQL.append("and T1.KANRINO = T2.KANRINO ");
				sbSQL.append("and T1.TENCD = T2.TENCD) ");
			}

			dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

			if (dbDatas.size() != 0){
				otjt = new JSONObject();
				otjt.put(TOK_CMN_TJTENLayout.MOYSKBN.getCol(), moyskbn);
				otjt.put(TOK_CMN_TJTENLayout.MOYSSTDT.getCol(), moysstdt);
				otjt.put(TOK_CMN_TJTENLayout.MOYSRBAN.getCol(), moysrban);
				otjt.put(TOK_CMN_TJTENLayout.BMNCD.getCol(), bmncd);
				otjt.put(TOK_CMN_TJTENLayout.KANRINO.getCol(), kanrino);

				JSONObject getData = dbDatas.getJSONObject(0);
				String tencd	= getData.containsKey("TENCD") ? getData.getString("TENCD") : "";
				String tenrank	= getData.containsKey("TENRANK") ? getData.getString("TENRANK") : "";
				String tjflg	= getData.containsKey("TJFLG") ? getData.getString("TJFLG") : "";

				otjt.put(TOK_CMN_TJTENLayout.TENCD.getCol(), tencd);
				otjt.put(TOK_CMN_TJTENLayout.TENRANK.getCol(), tenrank);
				otjt.put(TOK_CMN_TJTENLayout.TJFLG.getCol(), tjflg);
				otjt.put(TOK_CMN_TJTENLayout.SENDFLG.getCol(), 1);
				atjt.add(otjt);
			}
		}

		// 納入日情報作成
		int nhtaisho	= FileLayout3.NHTAISHO1.getNo()-1;	// 納品対象
		int hatasu		= FileLayout3.HTASU1.getNo()-1;		// 発注総数
		int ptnno		= FileLayout3.PTNNO1.getNo()-1;		// パタン№
		int tskbn		= FileLayout3.TSKBN1.getNo()-1;		// 訂正区分
		int nndt		= FileLayout3.MYOSDT1.getNo()-1;

		for (int j = 0; j < 10; j++) {

			String getnhtaisho = StringUtils.trim(String.valueOf(data[nhtaisho+j]));
			getnhtaisho = !StringUtils.isEmpty(getnhtaisho) ? getnhtaisho : "0";
			int moysdt = !StringUtils.isEmpty(String.valueOf(data[nndt+j])) ? Integer.valueOf(String.valueOf(data[nndt+j])) : 0;

			if (moysdt >= stdt && moysdt <= eddt) {

				int hat = 0;
				// 配列項目作成
				ArrayList<String> list = new ArrayList<String>();

				if (!StringUtils.isEmpty((StringUtils.trim(String.valueOf(data[hatasu+j]))))) {
					hat = Integer.valueOf(StringUtils.trim(String.valueOf(data[hatasu+j])));
				}

				if (tenkaiKbn.equals("2")) {
					list = new ReportBM015Dao(Defines.STR_JNDI_DS).getSuryoArr(
						bmncd,moyskbn,moysstdt,moysrban,addRank,delRank,addTenArr,addTenRankArr,delTenArr,tenrank_arr,StringUtils.trim(String.valueOf(data[ptnno+j]))
					);
				} else {

					// 対象店を取得
					Set<Integer> tencds = new ReportBM015Dao(Defines.STR_JNDI_DS).getTenCdAdd(bmncd,moyskbn,moysstdt,moysrban,addRank,delRank,addTenArr,delTenArr);

					list = new ReportBM015Dao(Defines.STR_JNDI_DS).getRtPtArr(
							bmncd,StringUtils.trim(String.valueOf(data[ptnno+j])),wwmm,syuKbn,StringUtils.trim(String.valueOf(data[ptnno+j])),daicd,chucd,hat,tencds,tenkaiKbn
					);
				}

				if (dao.isToktg) {
					onnd.put(TOKTG_NNDTLayout.MOYSKBN.getCol(), moyskbn);
					onnd.put(TOKTG_NNDTLayout.MOYSSTDT.getCol(), moysstdt);
					onnd.put(TOKTG_NNDTLayout.MOYSRBAN.getCol(), moysrban);
					onnd.put(TOKTG_NNDTLayout.BMNCD.getCol(), bmncd);
					onnd.put(TOKTG_NNDTLayout.KANRINO.getCol(), kanrino);
					onnd.put(TOKTG_NNDTLayout.NNDT.getCol(), StringUtils.trim(String.valueOf(data[nndt+j])));
					// 通常数パターンの場合、発注総数は不要
					if(tenkaiKbn.equals("2")) {
						onnd.put(TOKTG_NNDTLayout.HTASU.getCol(), "");
					} else {
						onnd.put(TOKTG_NNDTLayout.HTASU.getCol(), StringUtils.trim(String.valueOf(data[hatasu+j])));
					}
					onnd.put(TOKTG_NNDTLayout.PTNNO.getCol(), StringUtils.trim(String.valueOf(data[ptnno+j])));
					onnd.put(TOKTG_NNDTLayout.TSEIKBN.getCol(), StringUtils.trim(String.valueOf(data[tskbn+j])));

					// 発注数配列、展開数、店舗数
					if (list.size() != 0) {
						onnd.put(TOKTG_NNDTLayout.TENHTSU_ARR.getCol(), list.get(0));
						onnd.put(TOKTG_NNDTLayout.TPSU.getCol(), list.get(1));
						onnd.put(TOKTG_NNDTLayout.TENKAISU.getCol(), list.get(2));
					} else {
						onnd.put(TOKTG_NNDTLayout.TENHTSU_ARR.getCol(), "");
						onnd.put(TOKTG_NNDTLayout.TENKAISU.getCol(), "");
						onnd.put(TOKTG_NNDTLayout.TPSU.getCol(), "");
					}
					onnd.put(TOKTG_NNDTLayout.SENDFLG.getCol(), getnhtaisho);
				} else {
					onnd.put(TOKSP_NNDTLayout.MOYSKBN.getCol(), moyskbn);
					onnd.put(TOKSP_NNDTLayout.MOYSSTDT.getCol(), moysstdt);
					onnd.put(TOKSP_NNDTLayout.MOYSRBAN.getCol(), moysrban);
					onnd.put(TOKSP_NNDTLayout.BMNCD.getCol(), bmncd);
					onnd.put(TOKSP_NNDTLayout.KANRINO.getCol(), kanrino);
					onnd.put(TOKSP_NNDTLayout.NNDT.getCol(), StringUtils.trim(String.valueOf(data[nndt+j])));
					// 通常数パターンの場合、発注総数は不要
					if(tenkaiKbn.equals("2")) {
						onnd.put(TOKSP_NNDTLayout.HTASU.getCol(), "");
					} else {
						onnd.put(TOKSP_NNDTLayout.HTASU.getCol(), StringUtils.trim(String.valueOf(data[hatasu+j])));
					}
					onnd.put(TOKSP_NNDTLayout.PTNNO.getCol(), StringUtils.trim(String.valueOf(data[ptnno+j])));
					onnd.put(TOKSP_NNDTLayout.TSEIKBN.getCol(), StringUtils.trim(String.valueOf(data[tskbn+j])));

					// 発注数配列、展開数、店舗数
					if (list.size() != 0) {
						onnd.put(TOKSP_NNDTLayout.TENHTSU_ARR.getCol(), list.get(0));
						onnd.put(TOKSP_NNDTLayout.TPSU.getCol(), list.get(1));
						onnd.put(TOKSP_NNDTLayout.TENKAISU.getCol(), list.get(2));
					} else {
						onnd.put(TOKSP_NNDTLayout.TENHTSU_ARR.getCol(), "");
						onnd.put(TOKSP_NNDTLayout.TENKAISU.getCol(), "");
						onnd.put(TOKSP_NNDTLayout.TPSU.getCol(), "");
					}
					onnd.put(TOKSP_NNDTLayout.SENDFLG.getCol(), getnhtaisho);
					onnd.put(TOKSP_NNDTLayout.OPERATOR.getCol(), oldBin);
				}

				annd.add(onnd);
				onnd = new JSONObject();
			}
		}

		if (tkanplukbn.equals("2")) {

			if (oshn.containsKey(TOKTG_SHNLayout.A_GENKAAM_1KG.getCol()) && StringUtils.isEmpty(a_baikaam1)) {
				// 小数点以下を四捨五入する
				try {
					a_baikaam1 = String.valueOf(Math.round(oshn.getInt(TOKTG_SHNLayout.A_GENKAAM_1KG.getCol())/10.0));
				}
				catch(NumberFormatException e) {}
			}

			oshn.put(TOKTG_SHNLayout.A_BAIKAAM.getCol(), a_baikaam1);
			oshn.put(TOKTG_SHNLayout.B_BAIKAAM.getCol(), b_baikaam1);
			oshn.put(TOKTG_SHNLayout.C_BAIKAAM.getCol(), c_baikaam1);


		} else if (tkanplukbn.equals("1") || (!dao.isFrm4 && !dao.isFrm5)) {
			oshn.put(TOKTG_SHNLayout.A_BAIKAAM.getCol(), a_baikaam2);
			oshn.put(TOKTG_SHNLayout.B_BAIKAAM.getCol(), b_baikaam2);
			oshn.put(TOKTG_SHNLayout.C_BAIKAAM.getCol(), c_baikaam2);
		}

		if (StringUtils.isEmpty(tenrank_arr)) {
			tenrank_arr = " ";
		}

		if (dao.isToktg) {
			oshn.put(TOKTG_SHNLayout.DAICD.getCol(), daicd);
			oshn.put(TOKTG_SHNLayout.CHUCD.getCol(), chucd);
			oshn.put(TOKTG_SHNLayout.SHOBUNCD.getCol(), shocd);
			oshn.put(TOKTG_SHNLayout.TENRANK_ARR.getCol(), tenrank_arr);
		} else {
			oshn.put(TOKSP_SHNLayout.DAICD.getCol(), daicd);
			oshn.put(TOKSP_SHNLayout.CHUCD.getCol(), chucd);
			oshn.put(TOKSP_SHNLayout.TENRANK_ARR.getCol(), tenrank_arr);
		}

		// 変数を初期化
		sbSQL	= new StringBuffer();
		iL		= new ItemList();
		dbDatas = new JSONArray();
		sqlWhere	= "";
		paramData	= new ArrayList<String>();

		String sqlWith = "";
		// 部門コード
		if (StringUtils.isEmpty(bmncd)) {
			sqlWith += "BMNCD=null ";
		} else {
			sqlWith += "BMNCD=? ";
			paramData.add(bmncd);
		}

		// 登録種別の取得
		if (dao.isToktg) {
			sqlFrom = "INATK.TOKTG_SHN ";
		} else {
			sqlFrom = "INATK.TOKSP_SHN ";
		}

		String sqlWhere2 = "";

		// 催し区分
		if (StringUtils.isEmpty(moyskbn)) {
			sqlWhere2 += "MOYSKBN=null AND ";
		} else {
			sqlWhere2 += "MOYSKBN=? AND ";
			paramData.add(moyskbn);
		}

		// 催し開始日
		if (StringUtils.isEmpty(moysstdt)) {
			sqlWhere2 += "MOYSSTDT=null AND ";
		} else {
			sqlWhere2 += "MOYSSTDT=? AND ";
			paramData.add(moysstdt);
		}

		// 催し連番
		if (StringUtils.isEmpty(moysrban)) {
			sqlWhere2 += "MOYSRBAN=null AND ";
		} else {
			sqlWhere2 += "MOYSRBAN=? AND ";
			paramData.add(moysrban);
		}

		// 部門コード
		if (StringUtils.isEmpty(bmncd)) {
			sqlWhere2 += "BMNCD=null AND ";
		} else {
			sqlWhere2 += "BMNCD=? AND ";
			paramData.add(bmncd);
		}

		// 管理番号
		if (StringUtils.isEmpty(kanrino)) {
			sqlWhere2 += "KANRINO=null AND ";
		} else {
			sqlWhere2 += "KANRINO=? AND ";
			paramData.add(kanrino);
		}

		// 商品コード
		if (StringUtils.isEmpty(shncd)) {
			sqlWhere2 += "SHNCD=null AND ";
		} else {
			sqlWhere2 += "SHNCD=? AND ";
			paramData.add(shncd);
		}

		// 催し区分
		if (StringUtils.isEmpty(moyskbn)) {
			sqlWhere += "T1.MOYSKBN=null AND ";
		} else {
			sqlWhere += "T1.MOYSKBN=? AND ";
			paramData.add(moyskbn);
		}

		// 催し開始日
		if (StringUtils.isEmpty(moysstdt)) {
			sqlWhere += "T1.MOYSSTDT=null AND ";
		} else {
			sqlWhere += "T1.MOYSSTDT=? AND ";
			paramData.add(moysstdt);
		}

		// 催し連番
		if (StringUtils.isEmpty(moysrban)) {
			sqlWhere += "T1.MOYSRBAN=null AND ";
		} else {
			sqlWhere += "T1.MOYSRBAN=? AND ";
			paramData.add(moysrban);
		}

		sbSQL.append("WITH DF as (  ");
		sbSQL.append("SELECT ");
		sbSQL.append("DBMNATRKBN ");
		sbSQL.append("FROM ");
		sbSQL.append("INATK.TOKMOYDEF ");
		sbSQL.append("WHERE ");
		sbSQL.append(sqlWith);
		sbSQL.append(") ");
		sbSQL.append("SELECT ");
		sbSQL.append("CASE  ");
		sbSQL.append("WHEN T2.MOYSKBN IS NOT NULL  ");
		sbSQL.append("THEN T2.MYOSNNSTDT  ");
		sbSQL.append("WHEN DF.DBMNATRKBN = 1  ");
		sbSQL.append("THEN T1.NNSTDT_TGF  ");
		sbSQL.append("ELSE T1.NNSTDT  ");
		sbSQL.append("END NNSTDT ");
		sbSQL.append(", CASE  ");
		sbSQL.append("WHEN T2.MOYSKBN IS NOT NULL  ");
		sbSQL.append("THEN T2.MYOSNNEDDT  ");
		sbSQL.append("WHEN DF.DBMNATRKBN = 1  ");
		sbSQL.append("THEN T1.NNEDDT_TGF  ");
		sbSQL.append("ELSE T1.NNEDDT  ");
		sbSQL.append("END NNEDDT ");
		sbSQL.append(", CASE  ");
		sbSQL.append("WHEN T2.MOYSKBN IS NOT NULL  ");
		sbSQL.append("THEN T2.MYOSHBSTDT  ");
		sbSQL.append("ELSE T1.HBSTDT  ");
		sbSQL.append("END HBSTDT ");
		sbSQL.append(", CASE  ");
		sbSQL.append("WHEN T2.MOYSKBN IS NOT NULL  ");
		sbSQL.append("THEN T2.MYOSHBEDDT  ");
		sbSQL.append("ELSE T1.HBEDDT  ");
		sbSQL.append("END HBEDDT  ");
		sbSQL.append("FROM ");
		sbSQL.append("INATK.TOKMOYCD T1  ");
		sbSQL.append("left join (SELECT * FROM "+sqlFrom+" WHERE " + sqlWhere2 + " UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal() + ") T2 ");
		sbSQL.append("ON T1.MOYSKBN = T2.MOYSKBN  ");
		sbSQL.append("and T1.MOYSSTDT = T2.MOYSSTDT  ");
		sbSQL.append("and T1.MOYSRBAN = T2.MOYSRBAN ");
		sbSQL.append(", DF  ");
		sbSQL.append("WHERE ");
		sbSQL.append(sqlWhere);
		sbSQL.append("T1.UPDKBN=" + DefineReport.ValUpdkbn.NML.getVal() + " ");

		dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

		if (dbDatas.size() != 0){
			if (dao.isToktg) {
				oshn.put(TOKTG_SHNLayout.MYOSNNSTDT.getCol(), dbDatas.getJSONObject(0).optString("NNSTDT"));
				oshn.put(TOKTG_SHNLayout.MYOSNNEDDT.getCol(), dbDatas.getJSONObject(0).optString("NNEDDT"));
				oshn.put(TOKTG_SHNLayout.MYOSHBSTDT.getCol(), dbDatas.getJSONObject(0).optString("HBSTDT"));
				oshn.put(TOKTG_SHNLayout.MYOSHBEDDT.getCol(), dbDatas.getJSONObject(0).optString("HBEDDT"));
			} else {
				oshn.put(TOKSP_SHNLayout.MYOSNNSTDT.getCol(), dbDatas.getJSONObject(0).optString("NNSTDT"));
				oshn.put(TOKSP_SHNLayout.MYOSNNEDDT.getCol(), dbDatas.getJSONObject(0).optString("NNEDDT"));
				oshn.put(TOKSP_SHNLayout.MYOSHBSTDT.getCol(), dbDatas.getJSONObject(0).optString("HBSTDT"));
				oshn.put(TOKSP_SHNLayout.MYOSHBEDDT.getCol(), dbDatas.getJSONObject(0).optString("HBEDDT"));
			}
		}

		ashn.add(oshn);
		ahb.add(ohb);
		oshn = new JSONObject();
		otjt = new JSONObject();
		onnd = new JSONObject();
		ohb = new JSONObject();
	}


	public List<JSONObject> setCsvReqShn(ArrayList<Object[]> dL, int idxHeader, ReportTG016Dao dao, String callpage, MessageUtility mu) {

		List<JSONObject> msgList = new ArrayList<JSONObject>(){};

		int cnt = 1;

		// 店発注数配列作成の為、データの再作成
		for (int i = idxHeader; i < dL.size(); i++) {
			String reqNo = String.valueOf(cnt);

			msgList = dataCreateCheck(dL.get(i),mu,reqNo + "行目：");
			if (msgList.size() != 0) {
				return msgList;
			}

			JSONArray tokShn = new JSONArray();
			JSONArray tokNnd = new JSONArray();
			JSONArray tokTjt = new JSONArray();

			if (ashn.size() != 0) {
				if (dao.isToktg) {
					tokShn = this.selectTOKTG_SHNLayout(ashn,true);
				} else {
					tokShn = this.selectTOKSP_SHNLayout(ashn,true);
				}
			}

			if (annd.size() != 0) {
				if (dao.isToktg) {
					tokNnd = this.selectTOKTG_NNDT(annd,true);
				} else {
					tokNnd = this.selectTOKSP_NNDT(annd,true);
				}
			}

			if (atjt.size() != 0) {
				tokTjt = this.selectTOK_TJTEN(atjt,true);
			}

			msgList = dao.checkCsvShn(
					mu,
					tokShn,
					tokTjt,
					tokNnd,
					hobokure,
					callpage,
					updkbn,
					reqNo
			);

			if (msgList.size() != 0) {
				return msgList;
			}

			// JSONArray初期化
			ashn = new JSONArray();
			atjt = new JSONArray();
			annd = new JSONArray();
			ahb = new JSONArray();
			dao.setFrmInfo("");
			cnt++;
		}

		// ファイル内重複チェック
		for (int i=0; i < achk.size(); i++) {

			JSONObject obj = achk.getJSONObject(i);
			boolean isChange = false;

			if (!obj.getString(FileLayout3.UPDKBN.getCol()).equals("A")) {
				isChange = true;
			}

			String reqNo = String.valueOf(i+1) + "行目：";

			for (int j = i+1; j < achk.size(); j++) {
				JSONObject _obj = achk.getJSONObject(j);

				if (dao.isToktg) {

					// 基本情報
					String shncd		= obj.getString(TOKTG_SHNLayout.SHNCD.getCol());
					String moyskbn		= obj.getString(TOKTG_SHNLayout.MOYSKBN.getCol());
					String moysstdt		= obj.getString(TOKTG_SHNLayout.MOYSSTDT.getCol());
					String moysrban		= obj.getString(TOKTG_SHNLayout.MOYSRBAN.getCol());
					String bmncd		= obj.getString(TOKTG_SHNLayout.BMNCD.getCol());
					String kanrino		= obj.getString(TOKTG_SHNLayout.KANRINO.getCol());
					String kanrieno		= obj.getString(TOKTG_SHNLayout.KANRIENO.getCol());
					String _shncd		= _obj.getString(TOKTG_SHNLayout.SHNCD.getCol());
					String _moyskbn		= _obj.getString(TOKTG_SHNLayout.MOYSKBN.getCol());
					String _moysstdt	= _obj.getString(TOKTG_SHNLayout.MOYSSTDT.getCol());
					String _moysrban	= _obj.getString(TOKTG_SHNLayout.MOYSRBAN.getCol());
					String _bmncd		= _obj.getString(TOKTG_SHNLayout.BMNCD.getCol());
					String _kanrino		= _obj.getString(TOKTG_SHNLayout.KANRINO.getCol());
					String _kanrieno	= _obj.getString(TOKTG_SHNLayout.KANRIENO.getCol());

					// 販売情報
					String chkhbstdt	= obj.getString(TOKTG_SHNLayout.HBSTDT.getCol());
					String chkhbeddt	= obj.getString(TOKTG_SHNLayout.HBEDDT.getCol());
					String _chkhbstdt	= _obj.getString(TOKTG_SHNLayout.HBSTDT.getCol());
					String _chkhbeddt	= _obj.getString(TOKTG_SHNLayout.HBEDDT.getCol());
					String pluSndFlg	= obj.getString(TOKTG_SHNLayout.PLUSNDFLG.getCol());

					if ((!StringUtils.isEmpty(chkhbstdt) && !StringUtils.isEmpty(chkhbeddt)
							&& !StringUtils.isEmpty(_chkhbstdt) && !StringUtils.isEmpty(_chkhbeddt))
							&& (StringUtils.isEmpty(pluSndFlg) || (!StringUtils.isEmpty(pluSndFlg) && !pluSndFlg.equals("1")))) {
						int hbstdt			= Integer.valueOf(chkhbstdt);
						int hbeddt			= Integer.valueOf(chkhbeddt);
						int _hbstdt			= Integer.valueOf(_chkhbstdt);
						int _hbeddt			= Integer.valueOf(_chkhbeddt);
						String _pluSndFlg	= _obj.getString(TOKTG_SHNLayout.PLUSNDFLG.getCol());

						// アン有販売日チェック
						// 同一販売日の重複チェックエラー
						if (shncd.equals(_shncd) && hbstdt <= _hbeddt && hbeddt >= _hbstdt &&
								(StringUtils.isEmpty(_pluSndFlg) || (!StringUtils.isEmpty(_pluSndFlg) && !_pluSndFlg.equals("1")))) {
							if (isChange) {
								if (!moyskbn.equals(_moyskbn) ||
										!moysstdt.equals(_moysstdt) ||
										!moysrban.equals(_moysrban) ||
										!bmncd.equals(_bmncd) ||
										!kanrino.equals(_kanrino) ||
										!kanrieno.equals(_kanrieno)) {
									JSONObject o = mu.getDbMessageObj("E20449", new String[]{reqNo});
									msgList.add(o);
									return msgList;

								}
							} else {
								JSONObject o = mu.getDbMessageObj("E20449", new String[]{reqNo});
								msgList.add(o);
								return msgList;
							}
						}
					}

					// アン有納品日チェック

					if (shncd.equals(_shncd) &&
							moyskbn.equals(_moyskbn) &&
							moysstdt.equals(_moysstdt) &&
							moysrban.equals(_moysrban) &&
							bmncd.equals(_bmncd) &&
							kanrino.equals(_kanrino) &&
							kanrieno.equals(_kanrieno)
					) {
						for (int no = 1; no <= 10; no++) {

							// 納入日+noに設定がない場合チェック対象外
							if (!obj.containsKey(TOKTG_NNDTLayout.NNDT.getCol()+no)) {
								continue;
							}

							String nndt		= obj.getString(TOKTG_NNDTLayout.NNDT.getCol()+no);
							String sendFlg	= obj.getString(TOKTG_SHNLayout.SENDFLG.getCol()+no);

							// 納入対象フラグがない場合チェック対象外
							if (StringUtils.isEmpty(sendFlg) || (!StringUtils.isEmpty(sendFlg) && !sendFlg.equals("1"))) {
								continue;
							}

							for (int _no = 1; _no <= 10; _no++) {

								// 納入日+_noに設定がない場合チェック対象外
								if (!_obj.containsKey(TOKTG_NNDTLayout.NNDT.getCol()+_no)) {
									continue;
								}

								String _nndt		= _obj.getString(TOKTG_NNDTLayout.NNDT.getCol()+_no);
								String _sendFlg		= _obj.getString(TOKTG_SHNLayout.SENDFLG.getCol()+_no);

								// 納入対象フラグがない場合チェック対象外
								if (StringUtils.isEmpty(_sendFlg) || (!StringUtils.isEmpty(_sendFlg) && !_sendFlg.equals("1"))) {
									continue;
								}

								// 納入日が重複した場合エラー
								if (nndt.equals(_nndt)) {
									// 同一納入日の重複チェックエラー
									JSONObject o = mu.getDbMessageObj("E20450", new String[]{reqNo});
									msgList.add(o);
									return msgList;
								}
							}
						}
					}

				} else {
					// 基本情報
					String shncd		= obj.getString(TOKSP_SHNLayout.SHNCD.getCol());
					String moyskbn		= obj.getString(TOKSP_SHNLayout.MOYSKBN.getCol());
					String moysstdt		= obj.getString(TOKSP_SHNLayout.MOYSSTDT.getCol());
					String moysrban		= obj.getString(TOKSP_SHNLayout.MOYSRBAN.getCol());
					String bmncd		= obj.getString(TOKSP_SHNLayout.BMNCD.getCol());
					String kanrino		= obj.getString(TOKSP_SHNLayout.KANRINO.getCol());
					String binkbn		= obj.getString(TOKSP_SHNLayout.BINKBN.getCol());
					String _shncd		= _obj.getString(TOKSP_SHNLayout.SHNCD.getCol());
					String _moyskbn		= _obj.getString(TOKSP_SHNLayout.MOYSKBN.getCol());
					String _kanrino		= _obj.getString(TOKSP_SHNLayout.KANRINO.getCol());
					String _binkbn		= _obj.getString(TOKSP_SHNLayout.BINKBN.getCol());

					// 販売情報
					String chkhbstdt	= obj.getString(TOKSP_SHNLayout.HBSTDT.getCol());
					String chkhbeddt	= obj.getString(TOKSP_SHNLayout.HBEDDT.getCol());
					String _chkhbstdt	= _obj.getString(TOKSP_SHNLayout.HBSTDT.getCol());
					String _chkhbeddt	= _obj.getString(TOKSP_SHNLayout.HBEDDT.getCol());
					String pluSndFlg	= obj.getString(TOKSP_SHNLayout.PLUSNDFLG.getCol());

					if ((!StringUtils.isEmpty(chkhbstdt) && !StringUtils.isEmpty(chkhbeddt)
							&& !StringUtils.isEmpty(_chkhbstdt) && !StringUtils.isEmpty(_chkhbeddt))
							&& (StringUtils.isEmpty(pluSndFlg) || (!StringUtils.isEmpty(pluSndFlg) && !pluSndFlg.equals("1")))) {
						int hbstdt			= Integer.valueOf(chkhbstdt);
						int hbeddt			= Integer.valueOf(chkhbeddt);
						int _hbstdt			= Integer.valueOf(_chkhbstdt);
						int _hbeddt			= Integer.valueOf(_chkhbeddt);
						String _pluSndFlg	= _obj.getString(TOKTG_SHNLayout.PLUSNDFLG.getCol());

						// 商品コード、管理番号にかぶりがあるかつPLU配信しないチェックがされてないことかつ!(チェック対象行.販売開始日 < その他行.販売終了日 || チェック対象行.販売終了日 < その他行.販売開始日)
						if (shncd.equals(_shncd) && kanrino.equals(_kanrino) &&
								((moyskbn.equals("3") && _moyskbn.equals("3")) || (!moyskbn.equals("3") && !_moyskbn.equals("3"))) &&
								(StringUtils.isEmpty(_pluSndFlg) || (!StringUtils.isEmpty(_pluSndFlg) && !_pluSndFlg.equals("1"))) &&
								!(hbstdt > _hbeddt || hbeddt < _hbstdt)) {
							// 重複チェック
							String errMsg = "";
							int amount = 0;
							String stdt = "";
							String eddt = StringUtils.isEmpty(obj.getString(TOKSP_SHNLayout.HBEDDT.getCol())) ? "" : CmnDate.dateFormat(CmnDate.getDayAddedDate(CmnDate.convDate(obj.getString(TOKSP_SHNLayout.HBEDDT.getCol())),amount));

							String moyscd = moyskbn + moysstdt + String.format("%3s", moysrban);
							int tenSu = 0;

							while (!stdt.equals(eddt)) {
								stdt = StringUtils.isEmpty(obj.getString(TOKSP_SHNLayout.HBSTDT.getCol())) ? "" : CmnDate.dateFormat(CmnDate.getDayAddedDate(CmnDate.convDate(obj.getString(TOKSP_SHNLayout.HBSTDT.getCol())),amount));
								amount++;

								// 商品販売日重複チェック
								HashMap<String,String> map = getArrMap(stdt,"1",_obj);

								if (map.size() == 0) {
									continue;
								}

								String addRank = obj.getString("addRank");
								String delRank = obj.getString("delRank");
								JSONArray addTenArr = obj.getJSONArray("addTenArr");
								JSONArray addTenRankArr = obj.getJSONArray("addTenRankArr");
								JSONArray delTenArr = obj.getJSONArray("delTenArr");
								ArrayList<String> tenranks = new ReportBM015Dao(JNDIname).getTenrankArray(bmncd, moyskbn, moysstdt, moysrban, addRank, delRank, addTenArr, addTenRankArr, delTenArr, "");

								int tenCd = 1;
								for(String rank : tenranks){
									if (!StringUtils.isEmpty(rank) && map.containsKey(String.valueOf(tenCd))) {
										if ((isChange && !map.get(String.valueOf(tenCd)).equals(moyscd)) || !isChange) {
											if (tenSu == 0) {
												String moyscdMsg = map.get(String.valueOf(tenCd)).substring(0,1) + "-"
												+ map.get(String.valueOf(tenCd)).substring(1,7) + "-"
												+ String.format("%03d", Integer.valueOf(map.get(String.valueOf(tenCd)).substring(8).trim()));
												errMsg += "販売日 " + stdt.substring(4,6) + "月" + stdt.substring(6,8) + "日 催しコード " + moyscdMsg + "と " + tenCd + "号店以下";
											}
											tenSu++;
										}
									}
									tenCd++;
								}
								if (tenSu != 0) {
									errMsg += tenSu + "店舗<br>";
									msgList.add(mu.getDbMessageObj("E30025", new String[]{reqNo+"商品コード " + shncd + "<br>" + errMsg + "<br>"}));
									return msgList;
								}
							}
						}
					}

					// アン無納品日チェック
					String errMsg = "";
					if (shncd.equals(_shncd) && binkbn.equals(_binkbn) &&
							((moyskbn.equals("3") && _moyskbn.equals("3")) || (!moyskbn.equals("3") && !_moyskbn.equals("3")))) {

						for (int no = 1; no <= 10; no++) {

							// 納入日+noに設定がない場合チェック対象外
							if (!obj.containsKey(TOKTG_NNDTLayout.NNDT.getCol()+no)) {
								continue;
							}

							String nndt		= obj.getString(TOKTG_NNDTLayout.NNDT.getCol()+no);
							String sendFlg	= obj.getString(TOKTG_SHNLayout.SENDFLG.getCol()+no);

							// 納入対象フラグがない場合チェック対象外
							if (StringUtils.isEmpty(sendFlg) || (!StringUtils.isEmpty(sendFlg) && !sendFlg.equals("1"))) {
								continue;
							}

							String moyscd = moyskbn + moysstdt + String.format("%3s", moysrban);
							int tenSu = 0;

							// 商品納入日重複チェック
							HashMap<String,String> map		= getArrMap(nndt,"2",_obj);
							HashMap<String,String> mapKanri	= getArrMap(nndt,"3",_obj);

							if (map.size() == 0) {
								continue;
							}

							String arr = obj.optString(TOKSP_NNDTLayout.TENHTSU_ARR.getCol()+no);
							HashMap<String,String> mapHtsu = new ReportJU012Dao(JNDIname).getDigitMap(arr,5,"1");

							for (HashMap.Entry<String, String> shnnn : map.entrySet()) {
								if (mapHtsu.containsKey(shnnn.getKey()) && Integer.valueOf(mapHtsu.get(shnnn.getKey())) >= 0) {
									// 更新：便区分が変更されている or 既に別の催しが存在している 登録：ここまできたら無条件にエラー
									if ((isChange &&
											(!shnnn.getValue().equals(moyscd)) || (shnnn.getValue().equals(moyscd) && mapKanri.size() != 0 && !mapKanri.get(shnnn.getKey()).equals(kanrino)))
											|| !isChange) {
										if (tenSu == 0) {
											String moyscdMsg = shnnn.getValue().substring(0,1) + "-"
													+ shnnn.getValue().substring(1,7) + "-"
													+ String.format("%03d", Integer.valueOf(shnnn.getValue().substring(8).trim()));
											errMsg += "納入日 " + nndt.substring(4,6) + "月" + nndt.substring(6,8) + "日 催しコード " + moyscdMsg + "と " + shnnn.getKey() + "号店以下";
										}
										tenSu++;
									}
								}
							}

							if (tenSu != 0) {
								errMsg += tenSu + "店舗<br>";
								msgList.add(mu.getDbMessageObj("E30025", new String[]{reqNo+"商品コード " + shncd + " 便区分 " + binkbn + "便<br>" + errMsg + "<br>"}));
								return msgList;
							}
						}
					}
				}
			}
		}

		for (int i = idxHeader; i < dL.size(); i++) {

			JSONArray tokShn = new JSONArray();
			JSONArray tokNnd = new JSONArray();
			JSONArray tokTjt = new JSONArray();
			JSONArray tokHb = new JSONArray();

			dataCreate(dL.get(i));

			if (ashn.size() != 0) {
				if (dao.isToktg) {
					tokShn = this.selectTOKTG_SHNLayout(ashn,false);
				} else {
					tokShn = this.selectTOKSP_SHNLayout(ashn,false);
				}
			}

			if (annd.size() != 0) {
				if (dao.isToktg) {
					tokNnd = this.selectTOKTG_NNDT(annd,false);
				} else {
					tokNnd = this.selectTOKSP_NNDT(annd,false);
				}
			}

			if (atjt.size() != 0) {
				tokTjt = this.selectTOK_TJTEN(atjt,false);

				JSONArray arr = new JSONArray();
				for (int j = 0; j < tokTjt.size(); j++) {
					JSONObject getData = tokTjt.getJSONObject(j);

					if (getData.containsKey(TOK_CMN_TJTENLayout.SENDFLG.getId()) && getData.optString(TOK_CMN_TJTENLayout.SENDFLG.getId()).equals("0")) {
						getData.element(TOK_CMN_TJTENLayout.SENDFLG.getId(), "A");
					} else if (getData.containsKey(TOK_CMN_TJTENLayout.SENDFLG.getId()) && getData.optString(TOK_CMN_TJTENLayout.SENDFLG.getId()).equals("1")) {
						getData.element(TOK_CMN_TJTENLayout.SENDFLG.getId(), "D");
					}
					arr.add(getData);
				}
				tokTjt = arr;
			}

			if (ahb.size() != 0) {
				tokHb = this.selectTOK_HB(ahb,false);
			}

			JSONObject rt = dao.createSqlShn(tokShn,tokTjt,tokNnd,tokHb,getUserInfo(),updkbn);

			// JSONArray初期化
			ashn = new JSONArray();
			atjt = new JSONArray();
			annd = new JSONArray();
			ahb = new JSONArray();

			/** SQLリスト保持用変数 */
			dao.sqlList = new ArrayList<String>();
			/** SQLのパラメータリスト保持用変数 */
			dao.prmList = new ArrayList<ArrayList<String>>();
			/** SQLログ用のラベルリスト保持用変数 */
			dao.lblList = new ArrayList<String>();
			/** SQLリスト保持用変数(付番管理用) */
			dao.sqlList0 = new ArrayList<String>();
			dao.setFrmInfo("");
		}

		return msgList;
	}

	/**
	 * 配列をkye,valueの形で返却(key:店、value:催しコード)
	 * @param dt		比較対象日
	 * @param getTblFlg 1:販売日 2:納入日(催しコード) 3:納入日(管理番号)
	 * @param obj		比較対象obj
	 * @return
	 */
	public HashMap<String,String> getArrMap(String dt, String getTblFlg, JSONObject obj){

		HashMap<String,String> arrMap = new HashMap<String,String>();
		boolean mapCreate = false;

		// 販売日の重複チェック
		if (getTblFlg.equals("1")) {

			String hbstdt	= obj.getString(TOKSP_SHNLayout.HBSTDT.getCol());
			String hbeddt	= obj.getString(TOKSP_SHNLayout.HBEDDT.getCol());

			int amount = 0;
			String stdt = "";
			String eddt = StringUtils.isEmpty(hbeddt) ? "" : CmnDate.dateFormat(CmnDate.getDayAddedDate(CmnDate.convDate(hbeddt),amount));

			while (!stdt.equals(eddt)) {
				stdt = StringUtils.isEmpty(hbstdt) ? "" : CmnDate.dateFormat(CmnDate.getDayAddedDate(CmnDate.convDate(hbstdt),amount));
				amount++;

				if (stdt.equals(dt)) {
					mapCreate = true;
					break;
				}
			}

		// 納入日の重複チェック
		} else {
			for (int no = 1; no <= 10; no++) {

				// 納入日+noに設定がない場合チェック対象外
				if (!obj.containsKey(TOKTG_NNDTLayout.NNDT.getCol()+no)) {
					continue;
				}

				String nndt		= obj.getString(TOKTG_NNDTLayout.NNDT.getCol()+no);
				String sendFlg	= obj.getString(TOKTG_SHNLayout.SENDFLG.getCol()+no);

				// 納入対象フラグがない場合チェック対象外
				if (StringUtils.isEmpty(sendFlg) || (!StringUtils.isEmpty(sendFlg) && !sendFlg.equals("1"))) {
					continue;
				}

				if (nndt.equals(dt)) {
					mapCreate = true;
					break;
				}
			}
		}

		// 配列展開前までの重複が確認できない場合、空のmapを返却
		if (!mapCreate) {
			return arrMap;
		}

		String moyskbn		= obj.getString(TOKSP_SHNLayout.MOYSKBN.getCol());
		String moysstdt		= obj.getString(TOKSP_SHNLayout.MOYSSTDT.getCol());
		String moysrban		= obj.getString(TOKSP_SHNLayout.MOYSRBAN.getCol());
		String kanrino		= obj.getString(TOKSP_SHNLayout.KANRINO.getCol());
		String bmncd		= obj.getString(TOKSP_SHNLayout.BMNCD.getCol());
		String addRank		= obj.getString("addRank");
		String delRank		= obj.getString("delRank");
		JSONArray addTenArr	= obj.getJSONArray("addTenArr");
		JSONArray delTenArr	= obj.getJSONArray("delTenArr");

		String arrVal = getTblFlg.equals("3") ? kanrino : moyskbn + moysstdt + String.format("%3s", moysrban);
		Set<Integer> tencds = new ReportBM015Dao(Defines.STR_JNDI_DS).getTenCdAdd(bmncd,moyskbn,moysstdt,moysrban,addRank,delRank,addTenArr,delTenArr);

		String createArr = "";
		int digit = getTblFlg.equals("3") ? 4:10;

		Iterator<Integer> ten = tencds.iterator();
		int min = 0;
		int max = 0;
		for (int i = 0; i < tencds.size(); i++) {
			min = max;
			max = ten.next();
			for (int j = min; j < max; j++) {
				if (j+1 == max) {
					createArr += arrVal;
				} else {
					createArr += String.format("%"+digit+"s","");
				}
			}
		}

		ReportJU012Dao dao = new ReportJU012Dao(JNDIname);
		createArr	= dao.spaceArr(createArr,digit);
		arrMap		= dao.getDigitMap(createArr,digit,"1");

		return arrMap;
	}

	public List<JSONObject> setCsvReqTen(ArrayList<Object[]> dL, int idxHeader, ReportTG016Dao dao, String callpage, MessageUtility mu) {

		List<JSONObject> msgList = new ArrayList<JSONObject>(){};

		StringBuffer		sbSQL	= new StringBuffer();
		ItemList			iL		= new ItemList();
		JSONArray			dbDatas	= new JSONArray();
		ArrayList<String>	paramData = new ArrayList<String>();

		// DB検索用パラメータ
		String sqlWhere	= "";
		String sqlFrom	= "";

		String moyskbn	= "";
		String moysstdt	= "";
		String moysrban	= "";
		String kanrino	= "";
		String bmncd	= "";
		String shncd	= "";
		String nndt		= "";
		String tencd	= "";
		String arr		= "";

		Set<String> moyscdSet	= new TreeSet<String>();
		Set<String> bmncdSet	= new TreeSet<String>();
		Set<String> keySet		= new TreeSet<String>();

		HashMap<String,String> nndtMap		= new HashMap<String,String>();
		HashMap<String,String> addshuMap	= new HashMap<String,String>();
		HashMap<String,String> shnchkMap	= new HashMap<String,String>();
		HashMap<String,String> nnkeyMap		= new HashMap<String,String>();

		int cnt = 0;

		// 店発注数配列作成の為、データの再作成
		for (int i = idxHeader; i < dL.size(); i++) {
			Object[] data = dL.get(i);

			cnt++;
			String reqNo = String.valueOf(cnt) + "行目：";

			for(FileLayout2 itm :FileLayout2.values()){
				String val = StringUtils.trim((String) data[itm.getNo()-1]);
				// 1.""で囲まれていた場合除去 2.末尾空白除去
				val = StringUtils.stripEnd(val.replaceFirst("^\"(.*)\"$", "$1"), null);

				if(itm.getTbl() == RefTable.TOKSHN){
					if(oshn.containsKey(itm.getCol())){
						ashn.add(oshn);
						oshn = new JSONObject();
					}
					if (itm.getCol().equals(FileLayout2.MOYSCD.getCol())) {

						if (!moyscdSet.contains(val)) {
							if (moyscdSet.size() != 0) {
								JSONObject o = mu.getDbMessageObj("E40004", new String[]{reqNo});
								msgList.add(o);
								return msgList;
							} else {
								moyscdSet.add(val);
							}
						}

						if (val.length() >= 8) {
							moyskbn		= val.substring(0,1);
							moysstdt	= val.substring(1,7);
							moysrban	= val.substring(7);
						} else if (val.length() >= 7) {
							moyskbn		= val.substring(0,1);
							moysstdt	= val.substring(1,7);
						} else if (val.length() >= 1) {
							moyskbn		= val.substring(0,1);
						}

						oshn.put(TOKSP_SHNLayout.MOYSKBN.getCol(), moyskbn);
						oshn.put(TOKSP_SHNLayout.MOYSSTDT.getCol(), moysstdt);
						oshn.put(TOKSP_SHNLayout.MOYSRBAN.getCol(), moysrban);
					} else if (itm.getCol().equals(FileLayout2.BMNCD.getCol())) {
						bmncd = val;

						DataType dtype = DefineReport.DataType.SUUJI;
						int[] digit = new int[]{2, 0};

						// ①データ型による文字種チェック
						if(!InputChecker.checkDataType(dtype, bmncd)){
							// エラー発生箇所を保存
							JSONObject o = mu.getDbMessageObjDataTypeErr(dtype, new String[]{reqNo+"部門は"});
							msgList.add(o);
							return msgList;
						}
						// ②データ桁チェック
						if(!InputChecker.checkDataLen(dtype, bmncd, digit)){
							// エラー発生箇所を保存
							JSONObject o = mu.getDbMessageObjLen(dtype, new String[]{reqNo+"部門は"});
							msgList.add(o);
							return msgList;
						}

						// 登録種別の取得
						// 催し種類情報設定
						dao.setMoycdInfo(moyskbn, moysstdt, moysrban, bmncd);

						if (!bmncdSet.contains(val)) {
							if (bmncdSet.size() != 0) {
								JSONObject o = mu.getDbMessageObj("E40087", new String[]{reqNo});
								msgList.add(o);
								return msgList;
							} else {
								bmncdSet.add(val);
							}
						}

						oshn.put(TOKSP_SHNLayout.BMNCD.getCol(), bmncd);

					} else if (itm.getCol().equals(FileLayout2.KANRINO.getCol())) {
						kanrino = !StringUtils.isEmpty(val) && NumberUtils.isNumber(val) ? String.valueOf(Integer.valueOf(val)) : val;
						oshn.put(TOKSP_SHNLayout.KANRINO.getCol(), kanrino);

						// keyの生成
						String key = moyskbn + "-" + moysstdt + "-" + moysrban + "-" + bmncd + "-" + kanrino;

						// keyが存在している場合
						if (addshuMap.containsKey(key)) {
							String addShuKbn = addshuMap.get(key).split("-")[0];
							String kanriEno = addshuMap.get(key).split("-")[0];

							if (!StringUtils.isEmpty(addShuKbn)) {
								oshn.put(TOKSP_SHNLayout.ADDSHUKBN.getCol(), dbDatas.getJSONObject(0).optString("ADDSHUKBN"));
							}
							if (!StringUtils.isEmpty(kanriEno)) {
								oshn.put(TOKSP_SHNLayout.KANRIENO.getCol(), dbDatas.getJSONObject(0).optString("KANRIENO"));
							}
							continue;
						}

						// 変数を初期化
						sbSQL	= new StringBuffer();
						iL		= new ItemList();
						dbDatas = new JSONArray();
						sqlWhere	= "";
						paramData	= new ArrayList<String>();

						// 登録種別の取得
						if (dao.isToktg) {
							sqlFrom = "INATK.TOKTG_SHN ";
						} else {
							sqlFrom = "INATK.TOKSP_SHN ";
						}

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
							sqlWhere += "MOYSRBAN=null AND ";
						} else {
							sqlWhere += "MOYSRBAN=? AND ";
							paramData.add(moysrban);
						}

						// 部門コード
						if (StringUtils.isEmpty(bmncd)) {
							sqlWhere += "BMNCD=null AND ";
						} else {
							sqlWhere += "BMNCD=? AND ";
							paramData.add(bmncd);
						}

						// 管理番号
						if (StringUtils.isEmpty(kanrino)) {
							sqlWhere += "KANRINO=null ";
						} else {
							sqlWhere += "KANRINO=? ";
							paramData.add(kanrino);
						}

						sbSQL.append("SELECT DISTINCT ");
						sbSQL.append("ADDSHUKBN ");	// 登録種別
						sbSQL.append("KANRIENO ");	// 管理枝番
						sbSQL.append("FROM ");
						sbSQL.append(sqlFrom);
						sbSQL.append("WHERE ");
						sbSQL.append(sqlWhere);		// 入力された催しコード

						dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

						if (dbDatas.size() != 0){
							oshn.put(TOKSP_SHNLayout.ADDSHUKBN.getCol(), dbDatas.getJSONObject(0).optString("ADDSHUKBN"));
							oshn.put(TOKSP_SHNLayout.KANRIENO.getCol(), dbDatas.getJSONObject(0).optString("KANRIENO"));
							addshuMap.put(key, dbDatas.getJSONObject(0).optString("ADDSHUKBN") + "-" + dbDatas.getJSONObject(0).optString("KANRIENO"));
						} else {
							if (bmncd.equals("2") || bmncd.equals("9") || bmncd.equals("15")) {
								oshn.put(TOKSP_SHNLayout.ADDSHUKBN.getCol(), DefineReport.ValAddShuKbn.VAL3.getVal());
							} else if (bmncd.equals("4") || bmncd.equals("6")) {
								oshn.put(TOKSP_SHNLayout.ADDSHUKBN.getCol(), DefineReport.ValAddShuKbn.VAL4.getVal());
							} else if (bmncd.equals("5")) {
								oshn.put(TOKSP_SHNLayout.ADDSHUKBN.getCol(), DefineReport.ValAddShuKbn.VAL5.getVal());
							} else {
								oshn.put(TOKSP_SHNLayout.ADDSHUKBN.getCol(), DefineReport.ValAddShuKbn.VAL2.getVal());
							}
							addshuMap.put(key, oshn.getString(TOKSP_SHNLayout.ADDSHUKBN.getCol()) + "-");
						}
					} else if (itm.getCol().equals(FileLayout2.SHNCD.getCol())) {

						val = !StringUtils.isEmpty(val) && val.length() <= 7 && NumberUtils.isNumber(val) ? String.format("%08d", Integer.valueOf(val)) : val;
						shncd = val;

						if (dao.isToktg) {
							oshn.put(TOKTG_SHNLayout.SHNCD.getCol(), val);
						} else {
							oshn.put(TOKSP_SHNLayout.SHNCD.getCol(), val);
						}
					} else if (itm.getCol().equals(FileLayout2.POPKN.getCol())) {

						if (dao.isToktg) {
							oshn.put(TOKTG_SHNLayout.POPKN.getCol(), val);
						} else {
							oshn.put(TOKSP_SHNLayout.POPKN.getCol(), val);
						}
					} else if (itm.getCol().equals(FileLayout2.SANCHIKN.getCol())) {

						if (dao.isToktg) {
							oshn.put(TOKTG_SHNLayout.SANCHIKN.getCol(), val);
						} else {
							oshn.put(TOKSP_SHNLayout.SANCHIKN.getCol(), val);
						}
					} else if (itm.getCol().equals(FileLayout2.MAKERKN.getCol())) {

						if (dao.isToktg) {
							oshn.put(TOKTG_SHNLayout.MAKERKN.getCol(), val);
						} else {
							oshn.put(TOKSP_SHNLayout.MAKERKN.getCol(), val);
						}
					} else if (itm.getCol().equals(FileLayout2.KIKKN.getCol())) {

						if (dao.isToktg) {
							oshn.put(TOKTG_SHNLayout.KIKKN.getCol(), val);
						} else {
							oshn.put(TOKSP_SHNLayout.KIKKN.getCol(), val);
						}
					} else if (itm.getCol().equals(FileLayout2.DAICD.getCol())) {

						if (dao.isToktg) {
							oshn.put(TOKTG_SHNLayout.DAICD.getCol(), val);
						} else {
							oshn.put(TOKSP_SHNLayout.DAICD.getCol(), val);
						}
					} else if (itm.getCol().equals(FileLayout2.CHUCD.getCol())) {

						if (dao.isToktg) {
							oshn.put(TOKTG_SHNLayout.CHUCD.getCol(), val);
						} else {
							oshn.put(TOKSP_SHNLayout.CHUCD.getCol(), val);
						}
					} else {
						oshn.put(itm.getCol(), val);
					}

					if (itm.getCol().equals(FileLayout2.KIKKN.getCol())) {
						break;
					}
				} else if(itm.getTbl() == RefTable.TOKNNDT) {

					String key = moyskbn+moysstdt+moysrban+bmncd;

					if (itm.getCol().equals(FileLayout2.NNDT.getCol())) {
						nndt = val;
					} else if (itm.getCol().equals(FileLayout2.TENCD.getCol())) {

						if (StringUtils.isEmpty(val)) {
							JSONObject o = mu.getDbMessageObj("EX1047", new String[]{reqNo+"店番"});
							msgList.add(o);
							return msgList;
						}

						DataType dtype = DefineReport.DataType.SUUJI;
						int[] digit = new int[]{3, 0};

						// ①データ型による文字種チェック
						if(!InputChecker.checkDataType(dtype, val)){
							// エラー発生箇所を保存
							JSONObject o = mu.getDbMessageObjDataTypeErr(dtype, new String[]{reqNo+"店番は"});
							msgList.add(o);
							return msgList;
						}
						// ②データ桁チェック
						if(!InputChecker.checkDataLen(dtype, val, digit)){
							// エラー発生箇所を保存
							JSONObject o = mu.getDbMessageObjLen(dtype, new String[]{reqNo+"店番は"});
							msgList.add(o);
							return msgList;
						}

						tencd = val;

						if (StringUtils.isEmpty(kanrino)) {
							key += shncd+nndt+tencd;
						} else {
							key += kanrino+nndt+tencd;
						}

						// 同一のキーはNG
						if (keySet.contains(key)) {
							JSONObject o = mu.getDbMessageObj("E40005", new String[]{reqNo});
							msgList.add(o);
							return msgList;
						} else {
							keySet.add(key);
						}
					} else if (itm.getCol().equals(FileLayout2.SURYO.getCol())) {

						if (StringUtils.isEmpty(val)) {
							JSONObject o = mu.getDbMessageObj("EX1047", new String[]{reqNo+"数量"});
							msgList.add(o);
							return msgList;
						}

						DataType dtype = DefineReport.DataType.SUUJI;
						int[] digit = new int[]{5, 0};

						// ①データ型による文字種チェック
						if(!InputChecker.checkDataType(dtype, val)){
							// エラー発生箇所を保存
							JSONObject o = mu.getDbMessageObjDataTypeErr(dtype, new String[]{reqNo+"数量は"});
							msgList.add(o);
							return msgList;
						}
						// ②データ桁チェック
						if(!InputChecker.checkDataLen(dtype, val, digit)){
							// エラー発生箇所を保存
							JSONObject o = mu.getDbMessageObjLen(dtype, new String[]{reqNo+"数量は"});
							msgList.add(o);
							return msgList;
						}

						// 納入日テーブル更新用の情報作成
						key = moyskbn + "-" +moysstdt + "-" + moysrban + "-" + bmncd + "-" + kanrino + "-" + nndt;

						if (nnkeyMap.containsKey(key)) {
							key += "-" + nnkeyMap.get(key);
						} else {
							// 変数を初期化
							sbSQL	= new StringBuffer();
							iL		= new ItemList();
							dbDatas = new JSONArray();
							sqlWhere	= "";
							paramData	= new ArrayList<String>();

							if (dao.isToktg) {
								sqlFrom = "INATK.TOKTG_NNDT ";
							} else {
								sqlFrom = "INATK.TOKSP_NNDT ";
							}

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
								sqlWhere += "MOYSRBAN=null AND ";
							} else {
								sqlWhere += "MOYSRBAN=? AND ";
								paramData.add(moysrban);
							}

							// 部門コード
							if (StringUtils.isEmpty(bmncd)) {
								sqlWhere += "BMNCD=null AND ";
							} else {
								sqlWhere += "BMNCD=? AND ";
								paramData.add(bmncd);
							}

							// 管理番号
							if (StringUtils.isEmpty(kanrino)) {
								sqlWhere += "KANRINO=null AND ";
							} else {
								sqlWhere += "KANRINO=? AND ";
								paramData.add(kanrino);
							}

							// 納入日
							if (StringUtils.isEmpty(nndt)) {
								sqlWhere += "NNDT=null ";
							} else {
								sqlWhere += "NNDT=? ";
								paramData.add(nndt);
							}

							sbSQL.append("SELECT DISTINCT ");
							sbSQL.append("MOYSKBN ");		// 件数
							sbSQL.append(",TENHTSU_ARR ");	// 件数
							sbSQL.append("FROM ");
							sbSQL.append(sqlFrom);
							sbSQL.append("WHERE ");
							sbSQL.append(sqlWhere);		// 入力された催しコード

							dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

							if (dbDatas.size() == 0 || (dbDatas.size() != 0 && !dbDatas.getJSONObject(0).containsKey("MOYSKBN"))){
								// エラー発生箇所を保存
								JSONObject o = mu.getDbMessageObj("E40076", new String[]{reqNo});
								msgList.add(o);
								return msgList;
							} else {
								arr = dbDatas.getJSONObject(0).containsKey("TENHTSU_ARR") ? dbDatas.getJSONObject(0).getString("TENHTSU_ARR") : "";
								key += "-" + arr;

								nnkeyMap.put(key, arr);
							}
						}

						if (nndtMap.containsKey(key)) {
							nndtMap.replace(key, nndtMap.get(key) + "," + tencd + "-" + val);
						} else {
							nndtMap.put(key, tencd + "-" + val);
						}

						if (dao.isToktg) {
							onnd.put(TOKTG_NNDTLayout.MOYSKBN.getCol(),moyskbn);
							onnd.put(TOKTG_NNDTLayout.MOYSSTDT.getCol(),moysstdt);
							onnd.put(TOKTG_NNDTLayout.MOYSRBAN.getCol(),moysrban);
							onnd.put(TOKTG_NNDTLayout.KANRINO.getCol(),kanrino);
							onnd.put(TOKTG_NNDTLayout.BMNCD.getCol(),bmncd);
							onnd.put(TOKTG_NNDTLayout.NNDT.getCol(),nndt);
							onnd.put(TOKTG_NNDTLayout.TENHTSU_ARR.getCol(),arr+ "-" + tencd + "-" + val);
						} else {
							onnd.put(TOKSP_NNDTLayout.MOYSKBN.getCol(),moyskbn);
							onnd.put(TOKSP_NNDTLayout.MOYSSTDT.getCol(),moysstdt);
							onnd.put(TOKSP_NNDTLayout.MOYSRBAN.getCol(),moysrban);
							onnd.put(TOKSP_NNDTLayout.KANRINO.getCol(),kanrino);
							onnd.put(TOKSP_NNDTLayout.BMNCD.getCol(),bmncd);
							onnd.put(TOKSP_NNDTLayout.NNDT.getCol(),nndt);
							onnd.put(TOKSP_NNDTLayout.TENHTSU_ARR.getCol(),arr+ "-" + tencd + "-" + val);
						}
					}
				} else if (itm.getTbl() == RefTable.OTHER && itm.getCol().equals(FileLayout3.UPDKBN.getCol())) {
					if (val.equals("A")) {
						JSONObject o = mu.getDbMessageObj("E30012", new String[]{reqNo+"更新区分はU"});
						msgList.add(o);
						return msgList;
					}
				}
			}

			JSONArray shnArray	= new JSONArray();
			JSONArray nnArray	= new JSONArray();
			shnArray.add(oshn);
			nnArray.add(onnd);
			if (dao.isToktg) {
				shnArray = this.selectTOKTG_SHNLayout(shnArray,true);
			} else {
				shnArray = this.selectTOKSP_SHNLayout(shnArray,true);
			}

			msgList = dao.checkCsvNndt2(
					mu,
					this.selectTOKSP_NNDT(nnArray,true),
					shnArray,
					reqNo
			);

			if (msgList.size() != 0) {
				return msgList;
			}

			ashn.add(oshn);
			annd.add(onnd);
			oshn = new JSONObject();
			onnd = new JSONObject();
		}

		annd = new JSONArray();

		for(HashMap.Entry<String, String> getMap : nndtMap.entrySet()) {

			String key = getMap.getKey();
			String val = getMap.getValue();

			for (int j = 0; j < key.split("-").length; j++) {

				String getVal = key.split("-")[j];

				if (j == 0) {
					moyskbn = getVal;
				} else if (j == 1) {
					moysstdt = getVal;
				} else if (j == 2) {
					moysrban = getVal;
				} else if (j == 3) {
					bmncd = getVal;
				} else if (j == 4) {
					kanrino = getVal;
				} else if (j == 5) {
					nndt = getVal;
				} else if (j == 6) {
					arr = getVal;
				}
			}

			HashMap<String,String> arrMapNn	= new ReportJU012Dao(JNDIname).getDigitMap(arr,5,"0");
			for (int j = 0; j < val.split(",").length; j++) {

				key = val.split(",")[j].split("-")[0];

				if (arrMapNn.containsKey(key)) {
					arrMapNn.replace(key, val.split(",")[j].split("-")[1]);
				} else {
					arrMapNn.put(key, val.split(",")[j].split("-")[1]);
				}
			}


			arr = "";
			int tennum = 0;

			for (int j = 1; j <= 400; j++) {

				key = String.valueOf(j);
				val = "";

				if (arrMapNn.containsKey(key)) {
					val = arrMapNn.get(key);

					for (int jj = tennum+1; jj < Integer.valueOf(key); jj++) {
						arr += String.format("%5s", "");
					}
					arr += String.format("%05d", Integer.valueOf(val));
					tennum = Integer.valueOf(key);
				}
			}

			if (dao.isToktg) {
				onnd.put(TOKTG_NNDTLayout.MOYSKBN.getCol(),moyskbn);
				onnd.put(TOKTG_NNDTLayout.MOYSSTDT.getCol(),moysstdt);
				onnd.put(TOKTG_NNDTLayout.MOYSRBAN.getCol(),moysrban);
				onnd.put(TOKTG_NNDTLayout.KANRINO.getCol(),kanrino);
				onnd.put(TOKTG_NNDTLayout.BMNCD.getCol(),bmncd);
				onnd.put(TOKTG_NNDTLayout.NNDT.getCol(),nndt);
				onnd.put(TOKTG_NNDTLayout.TENHTSU_ARR.getCol(),arr);
			} else {
				onnd.put(TOKSP_NNDTLayout.MOYSKBN.getCol(),moyskbn);
				onnd.put(TOKSP_NNDTLayout.MOYSSTDT.getCol(),moysstdt);
				onnd.put(TOKSP_NNDTLayout.MOYSRBAN.getCol(),moysrban);
				onnd.put(TOKSP_NNDTLayout.KANRINO.getCol(),kanrino);
				onnd.put(TOKSP_NNDTLayout.BMNCD.getCol(),bmncd);
				onnd.put(TOKSP_NNDTLayout.NNDT.getCol(),nndt);
				onnd.put(TOKSP_NNDTLayout.TENHTSU_ARR.getCol(),arr);
			}
			annd.add(onnd);
			onnd = new JSONObject();
		}
		return msgList;
	}

	public List<JSONObject> setCsvReqNndt(ArrayList<Object[]> dL, int idxHeader) {

		// メッセージ情報取得
		MessageUtility mu = new MessageUtility();
		List<JSONObject> msg = new ArrayList<JSONObject>(){};

		// 重複確認用
		Map<String,String> csvMap		= new HashMap<String,String>();
		Map<String,String> tenBanMap	= new HashMap<String,String>();

		Set<String> moyscdSet	= new TreeSet<String>();
		Set<String> bmncdSet	= new TreeSet<String>();

		List<JSONObject> msgList = new ArrayList<JSONObject>(){};

		// 店発注数配列作成の為、データの再作成
		for (int i = idxHeader; i < dL.size(); i++) {
			Object[] data = dL.get(i);

			String reqNo = String.valueOf(i+1) + "行目：";

			// 商品コード格納
			String shncd = StringUtils.trim(String.valueOf(data[FileLayout.SHNCD.getNo()-1]));
			shncd = !StringUtils.isEmpty(shncd) && shncd.length() <= 7 && NumberUtils.isNumber(shncd) ? String.format("%08d", Integer.valueOf(shncd)) : shncd;
			ooth.put(TOK_CMN_SHNNNDTLayout.SHNCD,shncd);
			ooth.put(TOK_CMN_SHNNNDTLayout.BINKBN,StringUtils.trim(String.valueOf(data[FileLayout.BINKBN.getNo()-1])));
			ooth.put(TOK_CMN_SHNNNDTLayout.NNDT,StringUtils.trim(String.valueOf(data[FileLayout.NNDT.getNo()-1])));

			aoth.add(ooth);
			ooth = new JSONObject();

			String checkStr = StringUtils.trim(String.valueOf(data[FileLayout.CSV.getNo()-1]));
			if (StringUtils.isEmpty(checkStr) || !checkStr.equals("一括")) {

				JSONObject o = mu.getDbMessageObj("E30012", new String[]{reqNo+"店別数量CSV識別は一括"});
				msg.add(o);
				return msg;
			}

			checkStr = StringUtils.trim(String.valueOf(data[FileLayout.UPDKBN.getNo()-1]));
			if (StringUtils.isEmpty(checkStr) || !checkStr.equals("U")) {

				JSONObject o = mu.getDbMessageObj("E30012", new String[]{reqNo+"更新区分はU"});
				msg.add(o);
				return msg;
			}

			// keyとなる項目
			String f1	= StringUtils.trim(String.valueOf(data[FileLayout.MOYSCD.getNo()-1]));
			String f2	= StringUtils.trim(String.valueOf(data[FileLayout.BMNCD.getNo()-1]));
			String f3	= String.valueOf(Integer.valueOf(StringUtils.trim(String.valueOf(data[FileLayout.KANRINO.getNo()-1]))));
			String f4	= StringUtils.trim(String.valueOf(data[FileLayout.NNDT.getNo()-1]));
			String f5	= StringUtils.trim(String.valueOf(data[FileLayout.TENBANFLG.getNo()-1]));

			// 複数催しはNG
			if (!moyscdSet.contains(f1)) {
				if (moyscdSet.size() != 0) {
					JSONObject o = mu.getDbMessageObj("E40004", new String[]{reqNo});
					msg.add(o);
					return msg;
				} else {
					moyscdSet.add(f1);
				}
			}

			// 複数部門はNG
			if (!bmncdSet.contains(f2)) {
				if (bmncdSet.size() != 0) {
					JSONObject o = mu.getDbMessageObj("E40087", new String[]{reqNo});
					msg.add(o);
					return msg;
				} else {
					bmncdSet.add(f2);
				}
			}

			if (!f5.equals("0") && !f5.equals("1")) {
				JSONObject o = mu.getDbMessageObj("E30012", new String[]{reqNo+"店番フラグは0または1"});
				msg.add(o);
				return msg;
			}

			String key = f1 + "-" + f2 + "-" + f3 + "-" + f4 + "-" + f5;
			String val = "";

			int num = FileLayout.SURYO1.getNo()-1;

			for (int j = 0; j < 200; j++) {

				String suryo = StringUtils.trim(String.valueOf(data[num+j]));

				if (!StringUtils.isEmpty(suryo)) {

					if (!StringUtils.isNumeric(suryo)) {
						JSONObject o = mu.getDbMessageObj("E11019", new String[]{reqNo+"数量は"});
						msg.add(o);
						return msg;
					}

					if (suryo.length() > 5 || Integer.valueOf(suryo) > 99999) {
						JSONObject o = mu.getDbMessageObj("E30012", new String[]{reqNo+"数量 ≦ 99999"});
						msg.add(o);
						return msg;
					}
				}

				val += suryo;
				if (j+1 != 200) {
					val += ",";
				}
			}

			if (csvMap.containsKey(key)) {
				JSONObject o = mu.getDbMessageObj("E40005", new String[]{reqNo});
				msg.add(o);
				return msg;
			} else {
				csvMap.put(key, val);	// 20210630 コメント無効→有効化
				// 拡張forによる順不同回避
				onnd = new JSONObject();

				String moyscd = key.split("-")[0];
				String moyskbn = "";
				String moysstdt = "";
				String moysrban = "";
				String flg		= key.split("-")[4];

				if (moyscd.length() >= 8) {
					moyskbn		= moyscd.substring(0,1);
					moysstdt	= moyscd.substring(1,7);
					moysrban	= moyscd.substring(7);
				} else if (moyscd.length() >= 7) {
					moyskbn		= moyscd.substring(0,1);
					moysstdt	= moyscd.substring(1,7);
				} else if (moyscd.length() >= 1) {
					moyskbn		= moyscd.substring(0,1);
				}

				onnd.put(TOKTG_NNDTLayout.MOYSKBN, moyskbn);
				onnd.put(TOKTG_NNDTLayout.MOYSSTDT, moysstdt);
				onnd.put(TOKTG_NNDTLayout.MOYSRBAN, moysrban);
				onnd.put(TOKTG_NNDTLayout.BMNCD, key.split("-")[1]);
				onnd.put(TOKTG_NNDTLayout.KANRINO, key.split("-")[2]);
				onnd.put(TOKTG_NNDTLayout.NNDT, key.split("-")[3]);

				String arr = "";
				String suryo = "";
				int size = val.split(",").length;

				for (int j = 0; j < size; j++) {
					suryo = val.split(",")[j];
					if (StringUtils.isEmpty(suryo)) {
						arr += String.format("%5s", "");
					} else {
						arr += String.format("%05d", Integer.valueOf(suryo));
					}
				}

				int len = (5*200) - arr.length();
				if (len!=0) {
					arr += String.format("%"+len+"s","");
				}
				onnd.put(TOKTG_NNDTLayout.TENHTSU_ARR, arr+"-"+flg);
				annd.add(onnd);
			}
		}

		// 拡張forによる順不同回避のためコメントアウト
//		for(HashMap.Entry<String, String> chk : csvMap.entrySet()) {
//
//			onnd = new JSONObject();
//
//			String key = chk.getKey();
//			String val = chk.getValue();
//
//			String moyscd = key.split("-")[0];
//			String moyskbn = "";
//			String moysstdt = "";
//			String moysrban = "";
//			String flg		= key.split("-")[4];
//
//			if (moyscd.length() >= 8) {
//				moyskbn		= moyscd.substring(0,1);
//				moysstdt	= moyscd.substring(1,7);
//				moysrban	= moyscd.substring(7);
//			} else if (moyscd.length() >= 7) {
//				moyskbn		= moyscd.substring(0,1);
//				moysstdt	= moyscd.substring(1,7);
//			} else if (moyscd.length() >= 1) {
//				moyskbn		= moyscd.substring(0,1);
//			}
//
//			onnd.put(TOKTG_NNDTLayout.MOYSKBN, moyskbn);
//			onnd.put(TOKTG_NNDTLayout.MOYSSTDT, moysstdt);
//			onnd.put(TOKTG_NNDTLayout.MOYSRBAN, moysrban);
//			onnd.put(TOKTG_NNDTLayout.BMNCD, key.split("-")[1]);
//			onnd.put(TOKTG_NNDTLayout.KANRINO, key.split("-")[2]);
//			onnd.put(TOKTG_NNDTLayout.NNDT, key.split("-")[3]);
//
//			String arr = "";
//			String suryo = "";
//			int size = val.split(",").length;
//
//			for (int i = 0; i < size; i++) {
//				suryo = val.split(",")[i];
//				if (StringUtils.isEmpty(suryo)) {
//					arr += String.format("%5s", "");
//				} else {
//					arr += String.format("%05d", Integer.valueOf(suryo));
//				}
//			}
//
//			int len = (5*200) - arr.length();
//			if (len!=0) {
//				arr += String.format("%"+len+"s","");
//			}
//			onnd.put(TOKTG_NNDTLayout.TENHTSU_ARR, arr+"-"+flg);
//			annd.add(onnd);
//		}

		msgList = dao.checkCsvNndt(
				mu,
				this.selectTOKSP_NNDT(annd,true),
				this.selectTOK_CMN_SHNNNDT(aoth,true)
		);

		if (msgList.size() != 0) {
			return msgList;
		}

		// チェック完了後初期化
		annd = new JSONArray();
		onnd = new JSONObject();

		for(HashMap.Entry<String, String> csv : csvMap.entrySet()) {

			String key = csv.getKey().split("-")[0] + "-" + csv.getKey().split("-")[1] + "-" + csv.getKey().split("-")[2] + "-" + csv.getKey().split("-")[3];
			String val = csv.getKey().split("-")[4];

			String tenbanFlg = "";

			if (tenBanMap.containsKey(key)) {

				// 店番フラグ0、1が既にセットされていない
				if (!tenBanMap.get(key).contains(":")) {
					tenbanFlg = tenBanMap.get(key).split("-")[0];
				}

				if (tenbanFlg.equals("0")) {
					if (val.equals("1")) {
						tenBanMap.replace(key, tenBanMap.get(key) + ":" + val + "-" + csv.getValue());
					} else if (val.equals("0")) {
						tenBanMap.replace(key, val + "-" + csv.getValue());
					}
				} else if (tenbanFlg.equals("1")) {

					if (val.equals("0")) {
						tenBanMap.replace(key, val + "-" + csv.getValue() + ":" + tenBanMap.get(key));
					} else if (val.equals("1")) {
						tenBanMap.replace(key, val + "-" + csv.getValue());
					}
				} else {

					if (val.equals("1")) {
						tenBanMap.replace(key, tenBanMap.get(key).split(":")[0] + ":" + val + "-" + csv.getValue());
					} else if (val.equals("0")) {
						tenBanMap.replace(key, val + "-" + csv.getValue() + ":" + tenBanMap.get(key).split(":")[1]);
					}
				}
			} else {
				tenBanMap.put(key, val + "-" + csv.getValue());
			}
		}

		for(HashMap.Entry<String, String> ten : tenBanMap.entrySet()) {

			onnd = new JSONObject();

			String key = ten.getKey();
			String val = ten.getValue();

			String moyscd = key.split("-")[0];
			String moyskbn = "";
			String moysstdt = "";
			String moysrban = "";

			if (moyscd.length() >= 8) {
				moyskbn		= moyscd.substring(0,1);
				moysstdt	= moyscd.substring(1,7);
				moysrban	= moyscd.substring(7);
			} else if (moyscd.length() >= 7) {
				moyskbn		= moyscd.substring(0,1);
				moysstdt	= moyscd.substring(1,7);
			} else if (moyscd.length() >= 1) {
				moyskbn		= moyscd.substring(0,1);
			}

			onnd.put(TOKTG_NNDTLayout.MOYSKBN, moyskbn);
			onnd.put(TOKTG_NNDTLayout.MOYSSTDT, moysstdt);
			onnd.put(TOKTG_NNDTLayout.MOYSRBAN, moysrban);
			onnd.put(TOKTG_NNDTLayout.BMNCD, key.split("-")[1]);
			onnd.put(TOKTG_NNDTLayout.KANRINO, key.split("-")[2]);
			onnd.put(TOKTG_NNDTLayout.NNDT, key.split("-")[3]);

			String arr = "";
			String suryo = "";
			int size = 0;
			if (val.contains(":")) {
				String cVal =  val.split(":")[0].split("-")[1] + "," + val.split(":")[1].split("-")[1];
				size = cVal.split(",").length;
				for (int i = 0; i < size; i++) {
					suryo = cVal.split(",")[i];
					if (StringUtils.isEmpty(suryo)) {
						arr += String.format("%5s", "");
					} else {
						arr += String.format("%05d", Integer.valueOf(suryo));
					}
				}
			} else {
				size = val.split("-")[1].split(",").length;
				for (int i = 0; i < size; i++) {
					suryo = val.split("-")[1].split(",")[i];
					if (StringUtils.isEmpty(suryo)) {
						arr += String.format("%5s", "");
					} else {
						arr += String.format("%05d", Integer.valueOf(suryo));
					}
				}
				if (val.split("-")[0].equals("0")) {
					arr += String.format("%1000s", "");
				} else {
					arr = String.format("%1000s", "") + arr;
				}
			}

			JSONArray beforeArr = new ReportBM015Dao(Defines.STR_JNDI_DS).getTG016NndtChgBefore(moyskbn,moysstdt,moysrban,key.split("-")[1],key.split("-")[2],key.split("-")[3]);
			if (beforeArr.size() != 0) {
				String getArr = beforeArr.getJSONObject(0).getString("TENHTSU_ARR");

				HashMap<String,String> arrMap		= new ReportJU012Dao(Defines.STR_JNDI_DS).getDigitMap(arr,5,"1");
				HashMap<String,String> getArrMap	= new ReportJU012Dao(Defines.STR_JNDI_DS).getDigitMap(getArr,5,"1");

				for(HashMap.Entry<String, String> getEntry : getArrMap.entrySet()) {

					key = getEntry.getKey();
					val = getEntry.getValue();

					if (!arrMap.containsKey(key)) {
						arrMap.put(key, val);
					}
				}

				arr	= "";
				int tennum		= 0;
				for (int j = 1; j <= 400; j++) {

					key = String.valueOf(j);
					val = "";

					if (arrMap.containsKey(key)) {
						val = arrMap.get(key);

						for (int jj = tennum+1; jj < Integer.valueOf(key); jj++) {
							arr += String.format("%5s", "");
						}
						arr += String.format("%05d", Integer.valueOf(val));
						tennum = Integer.valueOf(key);
					}
				}
			}

			onnd.put(TOKTG_NNDTLayout.TENHTSU_ARR, arr);
			annd.add(onnd);
		}
		return msg;
	}


	/**
	 * 更新処理実行
	 * @param updCount
	 * @return
	 *
	 * @throws Exception
	 */
	private JSONObject createCommandUpdateData(
			ReportTG016Dao dao, User userInfo,
			JSONArray dataArray, JSONArray dataArrayShn, boolean insTen
		) throws Exception {

		JSONObject option = new JSONObject();

		// パラメータ確認
		JSONObject result = dao.createSqlNnDtCsv(dataArray, dataArrayShn, insTen, userInfo);

		return option;
	}

	/**  File出力項目の参照テーブル */
	public enum RefTable {
		/** 全店特売（アンケート有/無）_納入日 */
		TOKNNDT(1,"全店特売（アンケート有/無）_納入日"),
		/** 全店特売（アンケート有/無）_所品 */
		TOKSHN(2,"全店特売（アンケート有/無）_商品"),
		/** 全店特売（アンケート有/無）_納入日 */
		TOKTJTEN(3,"全店特売（アンケート有/無）_対象除外店"),
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
	public enum FileLayout2 {
		UPDKBN(1,"更新区分",RefTable.OTHER,"UPDKBN"),
		MOYSCD(2,"催しコード",RefTable.TOKSHN,"MOYSCD"),
		MOYKN(3,"催し名称",RefTable.OTHER,"MOYKN"),
		HBSTDT(4,"催し販売期間_開始日",RefTable.TOKSHN,"HBSTDT"),
		HBEDDT(5,"催し販売期間_終了日",RefTable.TOKSHN,"HBEDDT"),
		BMNCD(6,"部門",RefTable.TOKSHN,"BMNCD"),
		KANRINO(7,"管理番号",RefTable.TOKSHN,"KANRINO"),
		SHNCD(8,"商品コード",RefTable.TOKSHN,"SHNCD"),
		POPKN(9,"POP名称（漢字）",RefTable.TOKSHN,"POPKN"),
		NNDT(10,"納入日",RefTable.TOKNNDT,"NNDT"),
		TENCD(11,"店番",RefTable.TOKNNDT,"TENCD"),
		TENKN(12,"店舗名称",RefTable.OTHER,"TENKN"),
		SURYO(13,"数量",RefTable.TOKNNDT,"SURYO"),
		SANCHIKN(14,"産地",RefTable.TOKSHN,"SANCHIKN"),
		MAKERKN(15,"メーカー名",RefTable.TOKSHN,"MAKERKN"),
		KIKKN(16,"規格",RefTable.TOKSHN,"KIKKN"),
		DAICD(17,"大分類コード",RefTable.OTHER,"DAICD"),
		DAIKN(18,"大分類名称",RefTable.OTHER,"DAIKN"),
		CHUCD(19,"中分類コード",RefTable.OTHER,"CHUCD"),
		CHUKN(20,"中分類名称",RefTable.OTHER,"CHUKN"),
		SHOCD(21,"小分類コード",RefTable.OTHER,"SHOCD"),
		SHOKN(22,"小分類名",RefTable.OTHER,"SHOKN"),
		OPERATOR(23,"オペレーター",RefTable.OTHER,"OPERATOR"),
		ADDDT(24,"登録日",RefTable.OTHER,"ADDDT"),
		UPDDT(25,"更新日",RefTable.OTHER,"UPDDT");

		private final Integer no;
		private final String txt;
		private final RefTable tbl;
		private final String col;
		/** 初期化 */
		private FileLayout2(Integer no, String txt, RefTable tbl, String col) {
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

	/**  Fileレイアウト */
	public enum FileLayout {
		UPDKBN(1,"更新区分",RefTable.OTHER,"UPDKBN"),
		CSV(2,"店別数量CSV識別",RefTable.OTHER,"CSV"),
		MOYSCD(3,"催しコード",RefTable.TOKNNDT,"MOYSCD"),
		BMNCD(4,"部門",RefTable.TOKNNDT,"BMNCD"),
		KANRINO(5,"管理番号",RefTable.TOKNNDT,"KANRINO"),
		NNDT(6,"納入日",RefTable.TOKNNDT,"NNDT"),
		SHNCD(7,"商品コード",RefTable.OTHER,"SHNCD"),
		BINKBN(8,"便区分",RefTable.OTHER,"BINKBN"),
		POPKN(9,"POP名称（漢字）",RefTable.OTHER,"POPKN"),
		TENBANFLG(10,"店番フラグ",RefTable.TOKNNDT,"TENBANFLG"),
		SURYO1(11,"数量_1号店",RefTable.OTHER,"SURYO1"),
		SURYO2(12,"数量_2号店",RefTable.OTHER,"SURYO2"),
		SURYO3(13,"数量_3号店",RefTable.OTHER,"SURYO3"),
		SURYO4(14,"数量_4号店",RefTable.OTHER,"SURYO4"),
		SURYO5(15,"数量_5号店",RefTable.OTHER,"SURYO5"),
		SURYO6(16,"数量_6号店",RefTable.OTHER,"SURYO6"),
		SURYO7(17,"数量_7号店",RefTable.OTHER,"SURYO7"),
		SURYO8(18,"数量_8号店",RefTable.OTHER,"SURYO8"),
		SURYO9(19,"数量_9号店",RefTable.OTHER,"SURYO9"),
		SURYO10(20,"数量_10号店",RefTable.OTHER,"SURYO10"),
		SURYO11(21,"数量_11号店",RefTable.OTHER,"SURYO11"),
		SURYO12(22,"数量_12号店",RefTable.OTHER,"SURYO12"),
		SURYO13(23,"数量_13号店",RefTable.OTHER,"SURYO13"),
		SURYO14(24,"数量_14号店",RefTable.OTHER,"SURYO14"),
		SURYO15(25,"数量_15号店",RefTable.OTHER,"SURYO15"),
		SURYO16(26,"数量_16号店",RefTable.OTHER,"SURYO16"),
		SURYO17(27,"数量_17号店",RefTable.OTHER,"SURYO17"),
		SURYO18(28,"数量_18号店",RefTable.OTHER,"SURYO18"),
		SURYO19(29,"数量_19号店",RefTable.OTHER,"SURYO19"),
		SURYO20(30,"数量_20号店",RefTable.OTHER,"SURYO20"),
		SURYO21(31,"数量_21号店",RefTable.OTHER,"SURYO21"),
		SURYO22(32,"数量_22号店",RefTable.OTHER,"SURYO22"),
		SURYO23(33,"数量_23号店",RefTable.OTHER,"SURYO23"),
		SURYO24(34,"数量_24号店",RefTable.OTHER,"SURYO24"),
		SURYO25(35,"数量_25号店",RefTable.OTHER,"SURYO25"),
		SURYO26(36,"数量_26号店",RefTable.OTHER,"SURYO26"),
		SURYO27(37,"数量_27号店",RefTable.OTHER,"SURYO27"),
		SURYO28(38,"数量_28号店",RefTable.OTHER,"SURYO28"),
		SURYO29(39,"数量_29号店",RefTable.OTHER,"SURYO29"),
		SURYO30(40,"数量_30号店",RefTable.OTHER,"SURYO30"),
		SURYO31(41,"数量_31号店",RefTable.OTHER,"SURYO31"),
		SURYO32(42,"数量_32号店",RefTable.OTHER,"SURYO32"),
		SURYO33(43,"数量_33号店",RefTable.OTHER,"SURYO33"),
		SURYO34(44,"数量_34号店",RefTable.OTHER,"SURYO34"),
		SURYO35(45,"数量_35号店",RefTable.OTHER,"SURYO35"),
		SURYO36(46,"数量_36号店",RefTable.OTHER,"SURYO36"),
		SURYO37(47,"数量_37号店",RefTable.OTHER,"SURYO37"),
		SURYO38(48,"数量_38号店",RefTable.OTHER,"SURYO38"),
		SURYO39(49,"数量_39号店",RefTable.OTHER,"SURYO39"),
		SURYO40(50,"数量_40号店",RefTable.OTHER,"SURYO40"),
		SURYO41(51,"数量_41号店",RefTable.OTHER,"SURYO41"),
		SURYO42(52,"数量_42号店",RefTable.OTHER,"SURYO42"),
		SURYO43(53,"数量_43号店",RefTable.OTHER,"SURYO43"),
		SURYO44(54,"数量_44号店",RefTable.OTHER,"SURYO44"),
		SURYO45(55,"数量_45号店",RefTable.OTHER,"SURYO45"),
		SURYO46(56,"数量_46号店",RefTable.OTHER,"SURYO46"),
		SURYO47(57,"数量_47号店",RefTable.OTHER,"SURYO47"),
		SURYO48(58,"数量_48号店",RefTable.OTHER,"SURYO48"),
		SURYO49(59,"数量_49号店",RefTable.OTHER,"SURYO49"),
		SURYO50(60,"数量_50号店",RefTable.OTHER,"SURYO50"),
		SURYO51(61,"数量_51号店",RefTable.OTHER,"SURYO51"),
		SURYO52(62,"数量_52号店",RefTable.OTHER,"SURYO52"),
		SURYO53(63,"数量_53号店",RefTable.OTHER,"SURYO53"),
		SURYO54(64,"数量_54号店",RefTable.OTHER,"SURYO54"),
		SURYO55(65,"数量_55号店",RefTable.OTHER,"SURYO55"),
		SURYO56(66,"数量_56号店",RefTable.OTHER,"SURYO56"),
		SURYO57(67,"数量_57号店",RefTable.OTHER,"SURYO57"),
		SURYO58(68,"数量_58号店",RefTable.OTHER,"SURYO58"),
		SURYO59(69,"数量_59号店",RefTable.OTHER,"SURYO59"),
		SURYO60(70,"数量_60号店",RefTable.OTHER,"SURYO60"),
		SURYO61(71,"数量_61号店",RefTable.OTHER,"SURYO61"),
		SURYO62(72,"数量_62号店",RefTable.OTHER,"SURYO62"),
		SURYO63(73,"数量_63号店",RefTable.OTHER,"SURYO63"),
		SURYO64(74,"数量_64号店",RefTable.OTHER,"SURYO64"),
		SURYO65(75,"数量_65号店",RefTable.OTHER,"SURYO65"),
		SURYO66(76,"数量_66号店",RefTable.OTHER,"SURYO66"),
		SURYO67(77,"数量_67号店",RefTable.OTHER,"SURYO67"),
		SURYO68(78,"数量_68号店",RefTable.OTHER,"SURYO68"),
		SURYO69(79,"数量_69号店",RefTable.OTHER,"SURYO69"),
		SURYO70(80,"数量_70号店",RefTable.OTHER,"SURYO70"),
		SURYO71(81,"数量_71号店",RefTable.OTHER,"SURYO71"),
		SURYO72(82,"数量_72号店",RefTable.OTHER,"SURYO72"),
		SURYO73(83,"数量_73号店",RefTable.OTHER,"SURYO73"),
		SURYO74(84,"数量_74号店",RefTable.OTHER,"SURYO74"),
		SURYO75(85,"数量_75号店",RefTable.OTHER,"SURYO75"),
		SURYO76(86,"数量_76号店",RefTable.OTHER,"SURYO76"),
		SURYO77(87,"数量_77号店",RefTable.OTHER,"SURYO77"),
		SURYO78(88,"数量_78号店",RefTable.OTHER,"SURYO78"),
		SURYO79(89,"数量_79号店",RefTable.OTHER,"SURYO79"),
		SURYO80(90,"数量_80号店",RefTable.OTHER,"SURYO80"),
		SURYO81(91,"数量_81号店",RefTable.OTHER,"SURYO81"),
		SURYO82(92,"数量_82号店",RefTable.OTHER,"SURYO82"),
		SURYO83(93,"数量_83号店",RefTable.OTHER,"SURYO83"),
		SURYO84(94,"数量_84号店",RefTable.OTHER,"SURYO84"),
		SURYO85(95,"数量_85号店",RefTable.OTHER,"SURYO85"),
		SURYO86(96,"数量_86号店",RefTable.OTHER,"SURYO86"),
		SURYO87(97,"数量_87号店",RefTable.OTHER,"SURYO87"),
		SURYO88(98,"数量_88号店",RefTable.OTHER,"SURYO88"),
		SURYO89(99,"数量_89号店",RefTable.OTHER,"SURYO89"),
		SURYO90(100,"数量_90号店",RefTable.OTHER,"SURYO90"),
		SURYO91(101,"数量_91号店",RefTable.OTHER,"SURYO91"),
		SURYO92(102,"数量_92号店",RefTable.OTHER,"SURYO92"),
		SURYO93(103,"数量_93号店",RefTable.OTHER,"SURYO93"),
		SURYO94(104,"数量_94号店",RefTable.OTHER,"SURYO94"),
		SURYO95(105,"数量_95号店",RefTable.OTHER,"SURYO95"),
		SURYO96(106,"数量_96号店",RefTable.OTHER,"SURYO96"),
		SURYO97(107,"数量_97号店",RefTable.OTHER,"SURYO97"),
		SURYO98(108,"数量_98号店",RefTable.OTHER,"SURYO98"),
		SURYO99(109,"数量_99号店",RefTable.OTHER,"SURYO99"),
		SURYO100(110,"数量_100号店",RefTable.OTHER,"SURYO100"),
		SURYO101(111,"数量_101号店",RefTable.OTHER,"SURYO101"),
		SURYO102(112,"数量_102号店",RefTable.OTHER,"SURYO102"),
		SURYO103(113,"数量_103号店",RefTable.OTHER,"SURYO103"),
		SURYO104(114,"数量_104号店",RefTable.OTHER,"SURYO104"),
		SURYO105(115,"数量_105号店",RefTable.OTHER,"SURYO105"),
		SURYO106(116,"数量_106号店",RefTable.OTHER,"SURYO106"),
		SURYO107(117,"数量_107号店",RefTable.OTHER,"SURYO107"),
		SURYO108(118,"数量_108号店",RefTable.OTHER,"SURYO108"),
		SURYO109(119,"数量_109号店",RefTable.OTHER,"SURYO109"),
		SURYO110(120,"数量_110号店",RefTable.OTHER,"SURYO110"),
		SURYO111(121,"数量_111号店",RefTable.OTHER,"SURYO111"),
		SURYO112(122,"数量_112号店",RefTable.OTHER,"SURYO112"),
		SURYO113(123,"数量_113号店",RefTable.OTHER,"SURYO113"),
		SURYO114(124,"数量_114号店",RefTable.OTHER,"SURYO114"),
		SURYO115(125,"数量_115号店",RefTable.OTHER,"SURYO115"),
		SURYO116(126,"数量_116号店",RefTable.OTHER,"SURYO116"),
		SURYO117(127,"数量_117号店",RefTable.OTHER,"SURYO117"),
		SURYO118(128,"数量_118号店",RefTable.OTHER,"SURYO118"),
		SURYO119(129,"数量_119号店",RefTable.OTHER,"SURYO119"),
		SURYO120(130,"数量_120号店",RefTable.OTHER,"SURYO120"),
		SURYO121(131,"数量_121号店",RefTable.OTHER,"SURYO121"),
		SURYO122(132,"数量_122号店",RefTable.OTHER,"SURYO122"),
		SURYO123(133,"数量_123号店",RefTable.OTHER,"SURYO123"),
		SURYO124(134,"数量_124号店",RefTable.OTHER,"SURYO124"),
		SURYO125(135,"数量_125号店",RefTable.OTHER,"SURYO125"),
		SURYO126(136,"数量_126号店",RefTable.OTHER,"SURYO126"),
		SURYO127(137,"数量_127号店",RefTable.OTHER,"SURYO127"),
		SURYO128(138,"数量_128号店",RefTable.OTHER,"SURYO128"),
		SURYO129(139,"数量_129号店",RefTable.OTHER,"SURYO129"),
		SURYO130(140,"数量_130号店",RefTable.OTHER,"SURYO130"),
		SURYO131(141,"数量_131号店",RefTable.OTHER,"SURYO131"),
		SURYO132(142,"数量_132号店",RefTable.OTHER,"SURYO132"),
		SURYO133(143,"数量_133号店",RefTable.OTHER,"SURYO133"),
		SURYO134(144,"数量_134号店",RefTable.OTHER,"SURYO134"),
		SURYO135(145,"数量_135号店",RefTable.OTHER,"SURYO135"),
		SURYO136(146,"数量_136号店",RefTable.OTHER,"SURYO136"),
		SURYO137(147,"数量_137号店",RefTable.OTHER,"SURYO137"),
		SURYO138(148,"数量_138号店",RefTable.OTHER,"SURYO138"),
		SURYO139(149,"数量_139号店",RefTable.OTHER,"SURYO139"),
		SURYO140(150,"数量_140号店",RefTable.OTHER,"SURYO140"),
		SURYO141(151,"数量_141号店",RefTable.OTHER,"SURYO141"),
		SURYO142(152,"数量_142号店",RefTable.OTHER,"SURYO142"),
		SURYO143(153,"数量_143号店",RefTable.OTHER,"SURYO143"),
		SURYO144(154,"数量_144号店",RefTable.OTHER,"SURYO144"),
		SURYO145(155,"数量_145号店",RefTable.OTHER,"SURYO145"),
		SURYO146(156,"数量_146号店",RefTable.OTHER,"SURYO146"),
		SURYO147(157,"数量_147号店",RefTable.OTHER,"SURYO147"),
		SURYO148(158,"数量_148号店",RefTable.OTHER,"SURYO148"),
		SURYO149(159,"数量_149号店",RefTable.OTHER,"SURYO149"),
		SURYO150(160,"数量_150号店",RefTable.OTHER,"SURYO150"),
		SURYO151(161,"数量_151号店",RefTable.OTHER,"SURYO151"),
		SURYO152(162,"数量_152号店",RefTable.OTHER,"SURYO152"),
		SURYO153(163,"数量_153号店",RefTable.OTHER,"SURYO153"),
		SURYO154(164,"数量_154号店",RefTable.OTHER,"SURYO154"),
		SURYO155(165,"数量_155号店",RefTable.OTHER,"SURYO155"),
		SURYO156(166,"数量_156号店",RefTable.OTHER,"SURYO156"),
		SURYO157(167,"数量_157号店",RefTable.OTHER,"SURYO157"),
		SURYO158(168,"数量_158号店",RefTable.OTHER,"SURYO158"),
		SURYO159(169,"数量_159号店",RefTable.OTHER,"SURYO159"),
		SURYO160(170,"数量_160号店",RefTable.OTHER,"SURYO160"),
		SURYO161(171,"数量_161号店",RefTable.OTHER,"SURYO161"),
		SURYO162(172,"数量_162号店",RefTable.OTHER,"SURYO162"),
		SURYO163(173,"数量_163号店",RefTable.OTHER,"SURYO163"),
		SURYO164(174,"数量_164号店",RefTable.OTHER,"SURYO164"),
		SURYO165(175,"数量_165号店",RefTable.OTHER,"SURYO165"),
		SURYO166(176,"数量_166号店",RefTable.OTHER,"SURYO166"),
		SURYO167(177,"数量_167号店",RefTable.OTHER,"SURYO167"),
		SURYO168(178,"数量_168号店",RefTable.OTHER,"SURYO168"),
		SURYO169(179,"数量_169号店",RefTable.OTHER,"SURYO169"),
		SURYO170(180,"数量_170号店",RefTable.OTHER,"SURYO170"),
		SURYO171(181,"数量_171号店",RefTable.OTHER,"SURYO171"),
		SURYO172(182,"数量_172号店",RefTable.OTHER,"SURYO172"),
		SURYO173(183,"数量_173号店",RefTable.OTHER,"SURYO173"),
		SURYO174(184,"数量_174号店",RefTable.OTHER,"SURYO174"),
		SURYO175(185,"数量_175号店",RefTable.OTHER,"SURYO175"),
		SURYO176(186,"数量_176号店",RefTable.OTHER,"SURYO176"),
		SURYO177(187,"数量_177号店",RefTable.OTHER,"SURYO177"),
		SURYO178(188,"数量_178号店",RefTable.OTHER,"SURYO178"),
		SURYO179(189,"数量_179号店",RefTable.OTHER,"SURYO179"),
		SURYO180(190,"数量_180号店",RefTable.OTHER,"SURYO180"),
		SURYO181(191,"数量_181号店",RefTable.OTHER,"SURYO181"),
		SURYO182(192,"数量_182号店",RefTable.OTHER,"SURYO182"),
		SURYO183(193,"数量_183号店",RefTable.OTHER,"SURYO183"),
		SURYO184(194,"数量_184号店",RefTable.OTHER,"SURYO184"),
		SURYO185(195,"数量_185号店",RefTable.OTHER,"SURYO185"),
		SURYO186(196,"数量_186号店",RefTable.OTHER,"SURYO186"),
		SURYO187(197,"数量_187号店",RefTable.OTHER,"SURYO187"),
		SURYO188(198,"数量_188号店",RefTable.OTHER,"SURYO188"),
		SURYO189(199,"数量_189号店",RefTable.OTHER,"SURYO189"),
		SURYO190(200,"数量_190号店",RefTable.OTHER,"SURYO190"),
		SURYO191(201,"数量_191号店",RefTable.OTHER,"SURYO191"),
		SURYO192(202,"数量_192号店",RefTable.OTHER,"SURYO192"),
		SURYO193(203,"数量_193号店",RefTable.OTHER,"SURYO193"),
		SURYO194(204,"数量_194号店",RefTable.OTHER,"SURYO194"),
		SURYO195(205,"数量_195号店",RefTable.OTHER,"SURYO195"),
		SURYO196(206,"数量_196号店",RefTable.OTHER,"SURYO196"),
		SURYO197(207,"数量_197号店",RefTable.OTHER,"SURYO197"),
		SURYO198(208,"数量_198号店",RefTable.OTHER,"SURYO198"),
		SURYO199(209,"数量_199号店",RefTable.OTHER,"SURYO199"),
		SURYO200(210,"数量_200号店",RefTable.OTHER,"SURYO200");

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

	/**  Fileレイアウト */
	public enum FileLayout3 {
		UPDKBN(1,"更新区分",RefTable.OTHER,"UPDKBN"),
		BMNCD(2,"部門",RefTable.TOKSHN,"BMNCD"),
		MM(3,"週№_年",RefTable.OTHER,"MM"),
		WW(4,"週№_週",RefTable.OTHER,"WW"),
		MOYSCD(5,"催しコード",RefTable.TOKSHN,"MOYSCD"),
		MOYKNN(6,"催し名称",RefTable.OTHER,"MOYKNN"),
		MOYSHBSTDT(7,"催し販売期間_開始日",RefTable.OTHER,"MOYSHBSTDT"),
		MOYSHBEDDT(8,"催し販売期間_終了日",RefTable.OTHER,"MOYSHBEDDT"),
		MOYSNNSTDT(9,"催し納品期間_開始日",RefTable.OTHER,"MOYSNNSTDT"),
		MOYSNNEDDT(10,"催し納品期間_終了日",RefTable.OTHER,"MOYSNNEDDT"),
		MOYSSLIDEFLG(11,"催し１日遅れフラグ",RefTable.OTHER,"MOYSSLIDEFLG"),
		KANRINO(12,"管理番号",RefTable.TOKSHN,"KANRINO"),
		PARNO(13,"グループ№",RefTable.TOKSHN,"PARNO"),
		CHLDNO(14,"子№",RefTable.TOKSHN,"CHLDNO"),
		SHNCD(15,"商品コード",RefTable.TOKSHN,"SHNCD"),
		POPKN(16,"ＰＯＰ名称(漢字)",RefTable.TOKSHN,"POPKN"),
		MAKERKN(17,"メーカー名(漢字)",RefTable.TOKSHN,"MAKERKN"),
		KIKKN(18,"規格",RefTable.TOKSHN,"KIKKN"),
		SANCHIKN(19,"産地",RefTable.TOKSHN,"SANCHIKN"),
		NAMANETUKBN(20,"生食加熱区分",RefTable.TOKSHN,"NAMANETUKBN"),
		KAITOFLG(21,"解凍フラグ",RefTable.TOKSHN,"KAITOFLG"),
		YOSHOKUFLG(22,"養殖フラグ",RefTable.TOKSHN,"YOSHOKUFLG"),
		HIGAWRFLG(23,"日替区分",RefTable.TOKSHN,"HIGAWRFLG"),
		HBSTDT(24,"販売期間_開始日",RefTable.TOKSHN,"HBSTDT"),
		HBEDDT(25,"販売期間_終了日",RefTable.TOKSHN,"HBEDDT"),
		NNSTDT(26,"納入期間_開始日",RefTable.TOKSHN,"NNSTDT"),
		NNEDDT(27,"納入期間_終了日",RefTable.TOKSHN,"NNEDDT"),
		HBSLIDEFLG(28,"販売スライドフラグ",RefTable.TOKSHN,"HBSLIDEFLG"),
		NHSLIDEFLG(29,"納品スライドフラグ",RefTable.TOKSHN,"NHSLIDEFLG"),
		RANKNO_ADD_A(30,"特売Ａ売価店グループ",RefTable.TOKSHN,"RANKNO_ADD_A"),
		RANKNO_ADD_B(31,"特売Ｂ売価店グループ",RefTable.TOKSHN,"RANKNO_ADD_B"),
		RANKNO_ADD_C(32,"特売Ｃ売価店グループ",RefTable.TOKSHN,"RANKNO_ADD_C"),
		RANKNO_DEL(33,"特売除外店グループ",RefTable.TOKSHN,"RANKNO_DEL"),
		GENKAAM_MAE(34,"特売事前原価",RefTable.TOKSHN,"GENKAAM_MAE"),
		GENKAAM_ATO(35,"特売追加原価",RefTable.TOKSHN,"GENKAAM_ATO"),
		IRISU(36,"特売事前入数",RefTable.TOKSHN,"IRISU"),
		A_BAIKAAM2(37,"特売Ａ売価",RefTable.TOKSHN,"A_BAIKAAM2"),
		A_BAIKAAM_C(38,"特売Ａ本体売価_計算",RefTable.OTHER,"A_BAIKAAM_C"),
		B_BAIKAAM2(39,"特売Ｂ売価",RefTable.TOKSHN,"B_BAIKAAM2"),
		B_BAIKAAM_C(40,"特売Ｂ本体売価_計算",RefTable.OTHER,"B_BAIKAAM_C"),
		C_BAIKAAM2(41,"特売Ｃ売価",RefTable.TOKSHN,"C_BAIKAAM2"),
		C_BAIKAAM_C(42,"特売Ｃ本体売価_計算",RefTable.OTHER,"C_BAIKAAM_C"),
		TKANPLUKBN(43,"定貫ＰＬＵ・不定貫区分",RefTable.TOKSHN,"TKANPLUKBN"),
		A_BAIKAAM1(44,"特売Ａ100ｇ売価",RefTable.TOKSHN,"A_BAIKAAM1"),
		A_GENKAAM_1KG(45,"特売Ａ1㎏売価",RefTable.TOKSHN,"A_GENKAAM_1KG"),
		A_BAIKAAM_PACK(46,"特売ＡP売価",RefTable.TOKSHN,"A_BAIKAAM_PACK"),
		B_BAIKAAM1(47,"特売Ｂ100ｇ売価",RefTable.TOKSHN,"B_BAIKAAM1"),
		B_GENKAAM_1KG(48,"特売Ｂ1㎏売価",RefTable.TOKSHN,"B_GENKAAM_1KG"),
		B_BAIKAAM_PACK(49,"特売ＢP売価",RefTable.TOKSHN,"B_BAIKAAM_PACK"),
		C_BAIKAAM1(50,"特売Ｃ100ｇ売価",RefTable.TOKSHN,"C_BAIKAAM1"),
		C_GENKAAM_1KG(51,"特売Ｃ1㎏売価",RefTable.TOKSHN,"C_GENKAAM_1KG"),
		C_BAIKAAM_PACK(52,"特売ＣP売価",RefTable.TOKSHN,"C_BAIKAAM_PACK"),
		A_BAIKAAM_100G(53,"Ａ売価_100ｇ相当",RefTable.TOKSHN,"A_BAIKAAM_100G"),
		B_BAIKAAM_100G(54,"Ｂ売価_100ｇ相当",RefTable.TOKSHN,"B_BAIKAAM_100G"),
		C_BAIKAAM_100G(55,"Ｃ売価_100ｇ相当",RefTable.TOKSHN,"C_BAIKAAM_100G"),
		GENKAAM_1KG(56,"1㎏原価",RefTable.TOKSHN,"GENKAAM_1KG"),
		GENKAAM_PACK(57,"P原価",RefTable.TOKSHN,"GENKAAM_PACK"),
		A_WRITUKBN(58,"Ａ割引率",RefTable.TOKSHN,"A_WRITUKBN"),
		B_WRITUKBN(59,"Ｂ割引率",RefTable.TOKSHN,"B_WRITUKBN"),
		C_WRITUKBN(60,"Ｃ割引率",RefTable.TOKSHN,"C_WRITUKBN"),
		HBYOTEISU(61,"予定数",RefTable.TOKSHN,"HBYOTEISU"),
		YORIFLG(62,"よりどりフラグ",RefTable.TOKSHN,"YORIFLG"),
		KO_A_BAIKAAN(63,"１個売売価Ａ",RefTable.TOKSHN,"KO_A_BAIKAAN"),
		KO_B_BAIKAAN(64,"１個売売価Ｂ",RefTable.TOKSHN,"KO_B_BAIKAAN"),
		KO_C_BAIKAAN(65,"１個売売価Ｃ",RefTable.TOKSHN,"KO_C_BAIKAAN"),
		BD1_TENSU(66,"バンドル１点数",RefTable.TOKSHN,"BD1_TENSU"),
		BD1_A_BAIKAAN(67,"バンドル１売価Ａ",RefTable.TOKSHN,"BD1_A_BAIKAAN"),
		BD1_B_BAIKAAN(68,"バンドル１売価Ｂ",RefTable.TOKSHN,"BD1_B_BAIKAAN"),
		BD1_C_BAIKAAN(69,"バンドル１売価Ｃ",RefTable.TOKSHN,"BD1_C_BAIKAAN"),
		BD2_TENSU(70,"バンドル２点数",RefTable.TOKSHN,"BD2_TENSU"),
		BD2_A_BAIKAAN(71,"バンドル２売価Ａ",RefTable.TOKSHN,"BD2_A_BAIKAAN"),
		BD2_B_BAIKAAN(72,"バンドル２売価Ｂ",RefTable.TOKSHN,"BD2_B_BAIKAAN"),
		BD2_C_BAIKAAN(73,"バンドル２売価Ｃ",RefTable.TOKSHN,"BD2_C_BAIKAAN"),
		MEDAMAKBN(74,"目玉",RefTable.TOKSHN,"MEDAMAKBN"),
		COMMENT_POP(75,"ＰＯＰコメント",RefTable.TOKSHN,"COMMENT_POP"),
		COMMENT_TB(76,"特売コメント",RefTable.TOKSHN,"COMMENT_TB"),
		COMMENT_HGW(77,"その他日替コメント",RefTable.TOKSHN,"COMMENT_HGW"),
		SEGN_NINZU(78,"制限_先着人数",RefTable.TOKSHN,"SEGN_NINZU"),
		SEGN_GENTEI(79,"制限_限定表現",RefTable.TOKSHN,"SEGN_GENTEI"),
		SEGN_1KOSU(80,"制限_一人",RefTable.TOKSHN,"SEGN_1KOSU"),
		SEGN_1KOSUTNI(81,"制限_単位",RefTable.TOKSHN,"SEGN_1KOSUTNI"),
		CUTTENFLG(82,"カット店展開フラグ",RefTable.TOKSHN,"CUTTENFLG"),
		CHIRASFLG(83,"チラシ未掲載フラグ",RefTable.TOKSHN,"CHIRASFLG"),
		PLUSNDFLG(84,"ＰＬＵ未配信フラグ",RefTable.TOKSHN,"PLUSNDFLG"),
		HTGENBAIKAFLG(85,"発注原売価適用フラグ",RefTable.TOKSHN,"HTGENBAIKAFLG"),
		ZENWARI(86,"全品割引区分",RefTable.OTHER,"ZENWARI"),
		MYOSDT1(87,"催し日付1",RefTable.TOKSHN,"MYOSDT1"),
		MYOSDT2(88,"催し日付2",RefTable.TOKSHN,"MYOSDT2"),
		MYOSDT3(89,"催し日付3",RefTable.TOKSHN,"MYOSDT3"),
		MYOSDT4(90,"催し日付4",RefTable.TOKSHN,"MYOSDT4"),
		MYOSDT5(91,"催し日付5",RefTable.TOKSHN,"MYOSDT5"),
		MYOSDT6(92,"催し日付6",RefTable.TOKSHN,"MYOSDT6"),
		MYOSDT7(93,"催し日付7",RefTable.TOKSHN,"MYOSDT7"),
		MYOSDT8(94,"催し日付8",RefTable.TOKSHN,"MYOSDT8"),
		MYOSDT9(95,"催し日付9",RefTable.TOKSHN,"MYOSDT9"),
		MYOSDT10(96,"催し日付10",RefTable.TOKSHN,"MYOSDT10"),
		HBTAISHO1(97,"販売対象1",RefTable.TOKSHN,"HBTAISHO1"),
		HBTAISHO2(98,"販売対象2",RefTable.TOKSHN,"HBTAISHO2"),
		HBTAISHO3(99,"販売対象3",RefTable.TOKSHN,"HBTAISHO3"),
		HBTAISHO4(100,"販売対象4",RefTable.TOKSHN,"HBTAISHO4"),
		HBTAISHO5(101,"販売対象5",RefTable.TOKSHN,"HBTAISHO5"),
		HBTAISHO6(102,"販売対象6",RefTable.TOKSHN,"HBTAISHO6"),
		HBTAISHO7(103,"販売対象7",RefTable.TOKSHN,"HBTAISHO7"),
		HBTAISHO8(104,"販売対象8",RefTable.TOKSHN,"HBTAISHO8"),
		HBTAISHO9(105,"販売対象9",RefTable.TOKSHN,"HBTAISHO9"),
		HBTAISHO10(106,"販売対象10",RefTable.TOKSHN,"HBTAISHO10"),
		NHTAISHO1(107,"納品対象1",RefTable.TOKNNDT,"NHTAISHO1"),
		NHTAISHO2(108,"納品対象2",RefTable.OTHER,"NHTAISHO2"),
		NHTAISHO3(109,"納品対象3",RefTable.OTHER,"NHTAISHO3"),
		NHTAISHO4(110,"納品対象4",RefTable.OTHER,"NHTAISHO4"),
		NHTAISHO5(111,"納品対象5",RefTable.OTHER,"NHTAISHO5"),
		NHTAISHO6(112,"納品対象6",RefTable.OTHER,"NHTAISHO6"),
		NHTAISHO7(113,"納品対象7",RefTable.OTHER,"NHTAISHO7"),
		NHTAISHO8(114,"納品対象8",RefTable.OTHER,"NHTAISHO8"),
		NHTAISHO9(115,"納品対象9",RefTable.OTHER,"NHTAISHO9"),
		NHTAISHO10(116,"納品対象10",RefTable.OTHER,"NHTAISHO10"),
		HTASU1(117,"発注総数1",RefTable.OTHER,"HTASU1"),
		HTASU2(118,"発注総数2",RefTable.OTHER,"HTASU2"),
		HTASU3(119,"発注総数3",RefTable.OTHER,"HTASU3"),
		HTASU4(120,"発注総数4",RefTable.OTHER,"HTASU4"),
		HTASU5(121,"発注総数5",RefTable.OTHER,"HTASU5"),
		HTASU6(122,"発注総数6",RefTable.OTHER,"HTASU6"),
		HTASU7(123,"発注総数7",RefTable.OTHER,"HTASU7"),
		HTASU8(124,"発注総数8",RefTable.OTHER,"HTASU8"),
		HTASU9(125,"発注総数9",RefTable.OTHER,"HTASU9"),
		HTASU10(126,"発注総数10",RefTable.OTHER,"HTASU10"),
		PTNNO1(127,"パタン№1",RefTable.OTHER,"PTNNO1"),
		PTNNO2(128,"パタン№2",RefTable.OTHER,"PTNNO2"),
		PTNNO3(129,"パタン№3",RefTable.OTHER,"PTNNO3"),
		PTNNO4(130,"パタン№4",RefTable.OTHER,"PTNNO4"),
		PTNNO5(131,"パタン№5",RefTable.OTHER,"PTNNO5"),
		PTNNO6(132,"パタン№6",RefTable.OTHER,"PTNNO6"),
		PTNNO7(133,"パタン№7",RefTable.OTHER,"PTNNO7"),
		PTNNO8(134,"パタン№8",RefTable.OTHER,"PTNNO8"),
		PTNNO9(135,"パタン№9",RefTable.OTHER,"PTNNO9"),
		PTNNO10(136,"パタン№10",RefTable.OTHER,"PTNNO10"),
		TSKBN1(137,"訂正区分1",RefTable.OTHER,"TSKBN1"),
		TSKBN2(138,"訂正区分2",RefTable.OTHER,"TSKBN2"),
		TSKBN3(139,"訂正区分3",RefTable.OTHER,"TSKBN3"),
		TSKBN4(140,"訂正区分4",RefTable.OTHER,"TSKBN4"),
		TSKBN5(141,"訂正区分5",RefTable.OTHER,"TSKBN5"),
		TSKBN6(142,"訂正区分6",RefTable.OTHER,"TSKBN6"),
		TSKBN7(143,"訂正区分7",RefTable.OTHER,"TSKBN7"),
		TSKBN8(144,"訂正区分8",RefTable.OTHER,"TSKBN8"),
		TSKBN9(145,"訂正区分9",RefTable.OTHER,"TSKBN9"),
		TSKBN10(146,"訂正区分10",RefTable.OTHER,"TSKBN10"),
		ADDTEN1(147,"追加店1",RefTable.TOKTJTEN,"ADDTEN1"),
		ADDTEN2(148,"追加店2",RefTable.OTHER,"ADDTEN2"),
		ADDTEN3(149,"追加店3",RefTable.OTHER,"ADDTEN3"),
		ADDTEN4(150,"追加店4",RefTable.OTHER,"ADDTEN4"),
		ADDTEN5(151,"追加店5",RefTable.OTHER,"ADDTEN5"),
		ADDTEN6(152,"追加店6",RefTable.OTHER,"ADDTEN6"),
		ADDTEN7(153,"追加店7",RefTable.OTHER,"ADDTEN7"),
		ADDTEN8(154,"追加店8",RefTable.OTHER,"ADDTEN8"),
		ADDTEN9(155,"追加店9",RefTable.OTHER,"ADDTEN9"),
		ADDTEN10(156,"追加店10",RefTable.OTHER,"ADDTEN10"),
		ADDTENRANK1(157,"追加店ランク1",RefTable.OTHER,"ADDTENRANK1"),
		ADDTENRANK2(158,"追加店ランク2",RefTable.OTHER,"ADDTENRANK2"),
		ADDTENRANK3(159,"追加店ランク3",RefTable.OTHER,"ADDTENRANK3"),
		ADDTENRANK4(160,"追加店ランク4",RefTable.OTHER,"ADDTENRANK4"),
		ADDTENRANK5(161,"追加店ランク5",RefTable.OTHER,"ADDTENRANK5"),
		ADDTENRANK6(162,"追加店ランク6",RefTable.OTHER,"ADDTENRANK6"),
		ADDTENRANK7(163,"追加店ランク7",RefTable.OTHER,"ADDTENRANK7"),
		ADDTENRANK8(164,"追加店ランク8",RefTable.OTHER,"ADDTENRANK8"),
		ADDTENRANK9(165,"追加店ランク9",RefTable.OTHER,"ADDTENRANK9"),
		ADDTENRANK10(166,"追加店ランク10",RefTable.OTHER,"ADDTENRANK10"),
		DELTEN1(167,"除外店1",RefTable.OTHER,"DELTEN1"),
		DELTEN2(168,"除外店2",RefTable.OTHER,"DELTEN2"),
		DELTEN3(169,"除外店3",RefTable.OTHER,"DELTEN3"),
		DELTEN4(170,"除外店4",RefTable.OTHER,"DELTEN4"),
		DELTEN5(171,"除外店5",RefTable.OTHER,"DELTEN5"),
		DELTEN6(172,"除外店6",RefTable.OTHER,"DELTEN6"),
		DELTEN7(173,"除外店7",RefTable.OTHER,"DELTEN7"),
		DELTEN8(174,"除外店8",RefTable.OTHER,"DELTEN8"),
		DELTEN9(175,"除外店9",RefTable.OTHER,"DELTEN9"),
		DELTEN10(176,"除外店10",RefTable.OTHER,"DELTEN10"),
		BINKBN(177,"便区分",RefTable.TOKSHN,"BINKBN"),
		BDENKBN(178,"別伝区分",RefTable.TOKSHN,"BDENKBN"),
		WAPPNKBN(179,"ワッペン区分",RefTable.TOKSHN,"WAPPNKBN"),
		JUFLG(180,"事前打出区分",RefTable.TOKSHN,"JUFLG"),
		JUHTDT(181,"事前打出発注日",RefTable.TOKSHN,"JUHTDT"),
		SHUDENFLG(182,"週次伝送区分",RefTable.TOKSHN,"SHUDENFLG"),
		POPCD(183,"ＰＯＰコード",RefTable.TOKSHN,"POPCD"),
		POPSU(184,"ＰＯＰ枚数",RefTable.TOKSHN,"POPSU"),
		POPSZ(185,"ＰＯＰサイズ",RefTable.TOKSHN,"POPSZ"),
		SHNSIZE(186,"商品サイズ",RefTable.TOKSHN,"SHNSIZE"),
		SHNCOLOR(187,"商品色",RefTable.TOKSHN,"SHNCOLOR"),
		TENKAIKBN(188,"ﾊﾟﾀｰﾝ種類",RefTable.TOKSHN,"TENKAIKBN"),
		JSKPTNSYUKBN(189,"実績率PT数値",RefTable.TOKSHN,"JSKPTNSYUKBN"),
		JSKPTNZNENWKBN(190,"実績率PT前年同週",RefTable.TOKSHN,"JSKPTNZNENWKBN"),
		JSKPTNZNENMKBN(191,"実績率PT前年同月",RefTable.TOKSHN,"JSKPTNZNENMKBN"),
		BYCD(192,"バイヤーコード",RefTable.TOKSHN,"BYCD"),
		OPERATOR(193,"オペレーター",RefTable.OTHER,"OPERATOR"),
		ADDDT(194,"登録日",RefTable.OTHER,"ADDDT"),
		UPDDT(195,"更新日",RefTable.OTHER,"UPDDT");

		private final Integer no;
		private final String txt;
		private final RefTable tbl;
		private final String col;
		/** 初期化 */
		private FileLayout3(Integer no, String txt, RefTable tbl, String col) {
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
