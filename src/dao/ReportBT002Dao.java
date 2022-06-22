package dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import authentication.bean.User;
import authentication.defines.Consts;
import common.DefineReport;
import common.Defines;
import common.ItemList;
import common.JsonArrayData;
import common.MessageUtility;
import common.MessageUtility.Msg;
import common.MessageUtility.MsgKey;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 */
public class ReportBT002Dao extends ItemDao {

	/** SQLリスト保持用変数 */
	ArrayList<String> sqlList = new ArrayList<String>();
	/** SQLのパラメータリスト保持用変数 */
	ArrayList<ArrayList<String>> prmList = new ArrayList<ArrayList<String>>();
	/** SQLログ用のラベルリスト保持用変数 */
	ArrayList<String> lblList = new ArrayList<String>();

	/** 分類割引企画のKEY保持用変数 */
	String kkkno_seq = "";			//

	/**
	 * インスタンスを生成します。
	 * @param source
	 */
	public ReportBT002Dao(String JNDIname) {
		super(JNDIname);
	}

	/**
	 * 検索実行
	 *
	 * @return
	 */
	public boolean selectBy() {

		// 検索コマンド生成
		String command = createCommand();

		// 出力用検索条件生成
		outputQueryList();

		// 検索実行
		return super.selectBySQL(command);
	}

	public JSONObject update(HttpServletRequest request,HttpSession session,  HashMap<String, String> map, User userInfo) {
		String sysdate = (String)request.getSession().getAttribute(Consts.STR_SES_LOGINDT);

		JSONObject option = new JSONObject();
		JSONArray msgList = this.check(map, userInfo, sysdate);

		if(msgList.size() > 0){
			option.put(MsgKey.E.getKey(), msgList);
			return option;
		}

		// 更新処理
		try {
			option = this.updateData(map, userInfo, sysdate);
		} catch (Exception e) {
			e.printStackTrace();
			option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
		}
		return option;
	}

	// 削除処理
	public JSONObject delete(HttpServletRequest request,HttpSession session,  HashMap<String, String> map, User userInfo) {
		String sysdate = (String)request.getSession().getAttribute(Consts.STR_SES_LOGINDT);

		JSONObject option = new JSONObject();
		JSONArray msgList = this.checkDel(map, userInfo, sysdate);

		if(msgList.size() > 0){
			option.put(MsgKey.E.getKey(), msgList);
			return option;
		}

		// 更新処理
		try {
			option = this.deleteData(map, userInfo, sysdate);
		} catch (Exception e) {
			e.printStackTrace();
			option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
		}
		return option;
	}

	private String createCommand() {
		// ログインユーザー情報取得
		User userInfo = getUserInfo();
		if(userInfo==null){
			return "";
		}

		String kkkno	 = getMap().get("KKKNO"); 					// 企画No
		String sendBtnid	 = getMap().get("SENDBTNID");			// 呼出しボタン

		// パラメータ確認
		// 必須チェック
		if ( kkkno == null) {
			System.out.println(super.getConditionLog());
			return "";
		}

		// タイトル情報(任意)設定
		List<String> titleList = new ArrayList<String>();

		// 一覧表情報
		StringBuffer sbSQL = new StringBuffer();

		if(StringUtils.equals(DefineReport.Button.SEL_CHANGE.getObj(), sendBtnid)
				|| StringUtils.equals(DefineReport.Button.SEL_REFER.getObj(), sendBtnid)){

			sbSQL.append("select");
			sbSQL.append(" KKK.MOYSKBN || KKK.MOYSSTDT || KKK.MOYSRBAN");					// F1	 : 催しコード
			sbSQL.append(", MYCD.MOYKN");													// F2	 : 催し名称（漢字）
			sbSQL.append(", right(MYCD.HBSTDT, 6)");										// F3	 : 販売開始日
			sbSQL.append(", right(MYCD.HBEDDT, 6)");										// F4	 : 販売終了日
			sbSQL.append(", KKK.BTKN");														// F5	 : 分類割引名称
			sbSQL.append(", right(KKK.HBSTDT, 6)");											// F6	 : 販売期間_開始日
			sbSQL.append(", right(KKK.HBEDDT, 6)");											// F7	 : 販売期間_終了日
			sbSQL.append(", KKK.BMNCD");													// F8	 : 部門
			sbSQL.append(", KKK.DAICD");													// F9	 : 大分類
			sbSQL.append(", KKK.CHUCD");													// F10	 : 中分類
			sbSQL.append(", KKK.WARIRT");													// F11	 : 割引率
			sbSQL.append(", KKK.RANKNO_ADD");												// F12	 : 対象店ランク
			sbSQL.append(", KKK.RANKNO_DEL");												// F13	 : 除外店ランク
			sbSQL.append(", KKK.OPERATOR");													// F14	 : オペレータ
			sbSQL.append(", TO_CHAR(KKK.ADDDT, 'yy/mm/dd')");								// F15	 : 登録日
			sbSQL.append(", TO_CHAR(KKK.UPDDT, 'yy/mm/dd')");								// F16	 : 更新日
			sbSQL.append(", TO_CHAR(KKK.UPDDT,'YYYYMMDDHH24MISSNNNNNN') as HDN_UPDDT ");	// F17	 : 排他チェック用：更新日(非表示)
//			sbSQL.append(", LEFT(RIGHT('0' || KKK.WARIGK, 6), 4)");							// F18	 : 割引額(タイムサービス開始時刻)
			sbSQL.append(", CASE WHEN KKK.WARIGK < 1000 THEN LEFT ('000' || RIGHT (KKK.WARIGK, 6), 4)"
						+ " WHEN KKK.WARIGK < 10000 THEN LEFT ('00' || RIGHT (KKK.WARIGK, 6), 4)"
						+ " WHEN KKK.WARIGK < 100000 THEN LEFT ('0' || RIGHT (KKK.WARIGK, 6), 4)"
						+ " WHEN KKK.WARIGK < 1000000 THEN LEFT (RIGHT (KKK.WARIGK, 6), 4) END");
//			sbSQL.append(", LEFT(RIGHT('0' || KKK.ICHIRTGK, 6), 4)");						// F19	 : 一律額(タイムサービス終了時刻)
			sbSQL.append(", CASE WHEN KKK.ICHIRTGK < 1000 THEN LEFT ('000' || RIGHT (KKK.ICHIRTGK, 6), 4)"
						+ " WHEN KKK.ICHIRTGK < 10000 THEN LEFT ('00' || RIGHT (KKK.ICHIRTGK, 6), 4)"
						+ " WHEN KKK.ICHIRTGK < 100000 THEN LEFT ('0' || RIGHT (KKK.ICHIRTGK, 6), 4)"
						+ " WHEN KKK.ICHIRTGK < 1000000 THEN LEFT (RIGHT (KKK.ICHIRTGK, 6), 4) END");
			sbSQL.append(", MYCD.PLUSFLG");													// F20	 : PLU配信済フラグ
			sbSQL.append(" from INATK.TOKBT_KKK KKK");
			sbSQL.append(" left join INATK.TOKMOYCD MYCD on MYCD.MOYSKBN = KKK.MOYSKBN and MYCD.MOYSSTDT = KKK.MOYSSTDT and MYCD.MOYSRBAN = KKK.MOYSRBAN");
			sbSQL.append(" where KKK.UPDKBN = "+DefineReport.ValUpdkbn.NML.getVal());
			sbSQL.append(" and KKK.KKKNO = "+kkkno);
		}

		// オプション情報（タイトル）設定
		JSONObject option = new JSONObject();
		option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
		setOption(option);

		if (DefineReport.ID_DEBUG_MODE) System.out.println(getClass().getSimpleName()+"[sql]"+sbSQL.toString());
		return sbSQL.toString();
	}

	private void outputQueryList() {

		// 検索条件の加工クラス作成
		JsonArrayData jad = new JsonArrayData();
		jad.setJsonString(getJson());

	}

	/**
	 * チェック処理
	 *
	 * @throws Exception
	 */
	public JSONArray check(HashMap<String, String> map, User userInfo, String sysdate) {

		// パラメータ確認
		JSONArray dataArray = JSONArray.fromObject(map.get("DATA"));	// 更新情報(配送グループ)

		JSONArray		msg	= new JSONArray();
		MessageUtility	mu	= new MessageUtility();

		JSONArray tenCdAdds = new JSONArray();
		JSONArray tenCdDels = new JSONArray();
		ArrayList<JSONObject>	cTenCdAdds	= new ArrayList<JSONObject>();
		HashSet<String>			tenCdAdds_	= new HashSet<String>();
		ArrayList<JSONObject>	cTenCdDels	= new ArrayList<JSONObject>();
		HashSet<String>			tenCdDels_	= new HashSet<String>();

		// チェック処理
		// 対象件数チェック
		if(dataArray.size() == 0 || dataArray.getJSONObject(0).isEmpty()){
			msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
			return msg;
		}

		JSONObject data = dataArray.getJSONObject(0);

		String bmncd		= data.optString("F8");		// 部門コード
		String moyskbn		= map.get("MOYSKBN");		// 催し区分
		String moysstdt	 	= map.get("MOYSSTDT");		// 催し開始日
		String moysrban	 	= map.get("MOYSRBAN");		// 催し連番
		String rankNoAdd	= data.optString("F15");	// 対象ランク
		String rankNoDel	= data.optString("F16");	// 除外ランク

		int addCnt=29;
		int delCnt=19;

		Set<String> checkTencds = new TreeSet<String>();
		ReportBM006Dao checkDao = new ReportBM006Dao(JNDIname);

		for (int i = 0; i < 10; i++) {

			String val = data.optString("F"+addCnt);

			if (checkTencds.contains(val)) {
				msg.add(mu.getDbMessageObj("E20024", new String[]{}));
				return msg;
			} else if (!StringUtils.isEmpty(val)) {
				checkTencds.add(val);
			}

			if(StringUtils.isNotEmpty(val)){
				String msgCd = checkDao.checkTenCd(val);
				if (!StringUtils.isEmpty(msgCd)) {
					msg.add(mu.getDbMessageObj(msgCd, new String[]{}));
					return msg;
				}
				cTenCdAdds.add(data);
				tenCdAdds_.add(val);
			}
			tenCdAdds.add(i, data.optString("F"+addCnt));

			val = data.optString("F"+delCnt);

			if (checkTencds.contains(val)) {
				msg.add(mu.getDbMessageObj("E20024", new String[]{}));
				return msg;
			} else if (!StringUtils.isEmpty(val)) {
				checkTencds.add(val);
			}

			if(StringUtils.isNotEmpty(val)){
				String msgCd = checkDao.checkTenCd(val);
				if (!StringUtils.isEmpty(msgCd)) {
					msg.add(mu.getDbMessageObj(msgCd, new String[]{}));
					return msg;
				}
				cTenCdDels.add(data);
				tenCdDels_.add(val);
			}
			tenCdDels.add(i, data.optString("F"+delCnt));

			addCnt++;
			delCnt++;
		}

		// 追加店重複
		if(cTenCdAdds.size() != tenCdAdds_.size()){
			msg.add(mu.getDbMessageObj("E11040", new String[]{"対象店"}));
			return msg;
		}

		// 除外店重複
		if(cTenCdDels.size() != tenCdDels_.size()){
			msg.add(mu.getDbMessageObj("E11040", new String[]{"除外店"}));
			return msg;
		}

		// 対象店を取得
		String checkTencd = new ReportBM015Dao(JNDIname).checkTenCdAdd(
				 bmncd		// 部門コード
				,moyskbn	// 催し区分
				,moysstdt	// 催し開始日
				,moysrban	// 催し連番
				,rankNoAdd	// 対象ランク№
				,rankNoDel	// 除外ランク№
				,tenCdAdds	// 対象店
				,tenCdDels	// 除外店
		);

		if (!StringUtils.isEmpty(checkTencd)) {
			msg.add(mu.getDbMessageObj(checkTencd, new String[]{}));
			return msg;
		}

		return msg;
	}

	/**
	 * チェック処理
	 *
	 * @throws Exception
	 */
	public JSONArray checkDel(HashMap<String, String> map, User userInfo, String sysdate) {

		// 削除データ検索用コード
		JSONArray	dataArray	= JSONArray.fromObject(map.get("DATA"));	// 対象情報（主要な更新情報）
		String		kkkno		= map.get("KKKNO");							// 企画No

		// 格納用変数
		StringBuffer		sbSQL	= new StringBuffer();
		JSONArray			msg		= new JSONArray();
		ItemList			iL		= new ItemList();
		JSONArray			dbDatas = new JSONArray();
		MessageUtility		mu		= new MessageUtility();

		// チェック処理
		// 対象件数チェック
		/*if(dataArray.size() == 0 || dataArray.getJSONObject(0).isEmpty()){
			msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
			return msg;
		}*/

		if(kkkno.isEmpty() || kkkno == null){
			msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
			return msg;
		}

		// 基本登録情報
		JSONObject data = dataArray.getJSONObject(0);

		// エリア別配送パターンテーブルにレコードが存在する場合エラー
		/*sbSQL.append("SELECT ");
		sbSQL.append("	 HSGPCD ");	// レコード件数
		sbSQL.append("FROM ");
		sbSQL.append("	INAMS.MSTAREAHSPTN ");
		sbSQL.append("WHERE ");
		sbSQL.append("	HSGPCD	= '" + data.optString("F1") + "' AND ");	// 入力された配送グループで検索
		sbSQL.append("	TENGPCD	= '" + tengpcd + "' ");	// 入力された配送店グループで検索

		dbDatas = iL.selectJSONArray(sbSQL.toString(), null, Defines.STR_JNDI_DS);*/

		if (dbDatas.size() >= 1){
			// エリア別配送パターンに存在している
			msg.add(mu.getDbMessageObj("E11036", new String[]{}));
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
	private JSONObject updateData(HashMap<String, String> map, User userInfo, String sysdate) throws Exception {
		// パラメータ確認
		JSONObject	option		= new JSONObject();
		JSONArray	dataArray	= JSONArray.fromObject(map.get("DATA"));	// 対象情報（主要な更新情報）

		// 排他チェック用
		JSONArray			msg			= new JSONArray();

		// パラメータ確認
		// 必須チェック
		if ( dataArray.isEmpty() || dataArray.size() != 1 || dataArray.getJSONObject(0).isEmpty()) {
			option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
			return option;
		}

		// 基本登録情報
		JSONObject data = dataArray.getJSONObject(0);

		// 予約発注_商品、予約発注_納品日INSERT/UPDATE処理
		this.createSqlTOKBT_KKK(data,map,userInfo);

		// 排他チェック実行
		/*String targetTable = null;
		String targetWhere = null;
		ArrayList<String> targetParam = new ArrayList<String>();
		if(dataArray.size() > 0){
			targetTable = "INATK.TOKBT_KKK";
			targetWhere = "KKKNO = ?";
			targetParam.add(data.optString("F1"));
			if(! super.checkExclusion(targetTable, targetWhere, targetParam, data.optString("F18"))){
	 			msg.add(MessageUtility.getDbMessageExclusion(FieldType.DEFAULT, new String[]{}));
	 			option.put(MsgKey.E.getKey(), msg);
				return option;
			}
		}*/

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
	 * 削除処理実行
	 * @return
	 *
	 * @throws Exception
	 */
	private JSONObject deleteData(HashMap<String, String> map, User userInfo, String sysdate) throws Exception {
		// パラメータ確認
		JSONObject	option		= new JSONObject();
		JSONArray	dataArray	= JSONArray.fromObject(map.get("DATA"));	// 対象情報（主要な更新情報）

		String	kkkno			= map.get("KKKNO");							// 企画No

		// パラメータ確認
		// 必須チェック
		/*if ( dataArray.isEmpty() || dataArray.size() != 1 || dataArray.getJSONObject(0).isEmpty()) {
			option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
			return option;
		}*/

		// 基本登録情報
		//JSONObject data = dataArray.getJSONObject(0);

		// 分類割引_企画削除処理
		if(StringUtils.isNotEmpty(kkkno)){
			this.createSqlDelTOKBT_KKK(userInfo, kkkno);
		}

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
	 * 分類割引_企画、分類割引_対象除外店INSERT/UPDATE処理
	 *
	 * @param data
	 * @param map
	 * @param userInfo
	 */
	public String createSqlTOKBT_KKK(JSONObject data, HashMap<String, String> map, User userInfo){

		StringBuffer	sbSQL		= new StringBuffer();
		JSONArray		dataArrayT	= JSONArray.fromObject(map.get("DATA_TEN"));	// 更新情報(予約発注_納品日)

		// ログインユーザー情報取得
		String userId	= userInfo.getId();							// ログインユーザー
		String sendBtnid	 = map.get("SENDBTNID");				// 呼出しボタン

		String 	kkkno		 = "";											// 企画No
		String 	moyskbn		 = map.get("MOYSKBN");							// 催し区分
		String 	moysstdt	 = map.get("MOYSSTDT");							// 催し開始日
		String 	moysrban	 = map.get("MOYSRBAN");							// 催し連番
		String 	tenAtuk_Arr	 = "";											// 取扱いフラグ

		JSONArray tenCdAdds = new JSONArray();								// 対象店一覧
		JSONArray tenCdDels = new JSONArray();								// 削除店一覧

		ArrayList<String> prmData = new ArrayList<String>();
		Object[] valueData = new Object[]{};
		String values = "";

		// シーケンス取得
		if(StringUtils.equals(DefineReport.Button.NEW.getObj(), sendBtnid)){
			kkkno_seq = this.getKKKCD_SEQ();
		}

		// 店扱いフラグ配列作成
		int addCnt=29;
		int delCnt=19;

		for (int i = 0; i < 10; i++) {
			tenCdAdds.add(i, data.optString("F" + addCnt));
			tenCdDels.add(i, data.optString("F" + delCnt));
			addCnt++;
			delCnt++;
		}

		// 対象店を取得
		Set<Integer> tencds = new TreeSet<Integer>();

		if(StringUtils.equals(DefineReport.Button.NEW.getObj(), sendBtnid)){
			tencds = new ReportBM015Dao(JNDIname).getTenCdAdd(
					 data.optString("F8")	// 部門コード
					,moyskbn				// 催し区分
					,moysstdt				// 催し開始日
					,moysrban				// 催し連番
					,data.optString("F15")	// 対象ランク№
					,data.optString("F16")	// 除外ランク№
					,tenCdAdds				// 対象店
					,tenCdDels				// 除外店
			);
		}else{
			tencds = this.getTenCdAddUpdate(data.optString("F1"), tenCdAdds, tenCdDels);
		}


		Iterator<Integer> ten = tencds.iterator();
		int min = 0;
		int max = 0;
		for (int i = 0; i < tencds.size(); i++) {
			min = max;
			max = ten.next();
			for (int j = min; j < max; j++) {
				if (j+1 == max) {
					tenAtuk_Arr += "1";
				} else {
					tenAtuk_Arr += " ";
				}
			}
		}

		int maxField = 18;		// Fxxの最大値
		for (int k = 1; k <= maxField; k++) {
			String key = "F" + String.valueOf(k);

			if(k == 1){
				values += String.valueOf(0 + 1);

			}
			if(! ArrayUtils.contains(new String[]{"F11", "F18"}, key)){
				String val = data.optString(key);

				// 新規登録時はシーケンスを使用する。
				if(StringUtils.equals("F1", key)){
					if(StringUtils.isNotEmpty(kkkno_seq)){
						val = kkkno_seq;
					}
					kkkno = val;
				}else if (StringUtils.equals("F17", key)){
					// 店扱いフラグ配列を設定
					val = tenAtuk_Arr;

				}else if (StringUtils.equals("F12", key)){
					if(StringUtils.isNotEmpty(val)){
						val = val + "00";
					}
				}else if (StringUtils.equals("F13", key)){
					if(StringUtils.isNotEmpty(val)){
						val = val + "00";
					}
				}


				if(StringUtils.isEmpty(val)){
					values += ", null";
				}else{
					values += ", ?";
					prmData.add(val);
				}
			}

			if(k == maxField){
				valueData = ArrayUtils.add(valueData, "("+values+")");
				values = "";
			}
		}

		sbSQL.append(" merge into INATK.TOKBT_KKK as T using (select");
		sbSQL.append(" KKKNO");														// 企画No
		sbSQL.append(", BTKN");														// 分類割引名称
		sbSQL.append(", HBSTDT");													// 販売期間_開始日
		sbSQL.append(", HBEDDT");													// 販売期間_終了日
		sbSQL.append(", MOYSKBN");													// 催し区分
		sbSQL.append(", MOYSSTDT");													// 催し開始日
		sbSQL.append(", MOYSRBAN");													// 催し連番
		sbSQL.append(", BMNCD");													// 部門
		sbSQL.append(", DAICD");													// 大分類
		sbSQL.append(", CHUCD");													// 中分類
		sbSQL.append(", WARIRT");													// 割引率
		// TODO
		// タイムサービス項目の登録処理を追記
		// テーブル内に対象項目が存在しない為、登録なしとなった割引額、一律額項目を一時的に使用
		sbSQL.append(", WARIGK");													// 割引額(暫定的にタイムサービス_開始時間項目として使用)
		sbSQL.append(", ICHIRTGK");													// 一律額(暫定的にタイムサービス_終了時間項目として使用)
		sbSQL.append(", RANKNO_ADD");												// 対象店ランク
		sbSQL.append(", RANKNO_DEL");												// 除外店ランク
		sbSQL.append(", TENATSUK_ARR");												// 店扱いフラグ配列
		sbSQL.append(", 0 as SENDFLG");												// 送信区分
		sbSQL.append(", "+DefineReport.ValUpdkbn.NML.getVal()+" AS UPDKBN");		// 更新区分
		sbSQL.append(", '"+userId+"' AS OPERATOR ");								// オペレーター
		sbSQL.append(", current timestamp AS ADDDT ");								// 登録日
		sbSQL.append(", current timestamp AS UPDDT ");								// 更新日
		sbSQL.append(" from (values "+StringUtils.join(valueData, ",")+") as T1(NUM, ");
		sbSQL.append(" KKKNO");
		sbSQL.append(", BTKN");
		sbSQL.append(", HBSTDT");
		sbSQL.append(", HBEDDT");
		sbSQL.append(", MOYSKBN");
		sbSQL.append(", MOYSSTDT");
		sbSQL.append(", MOYSRBAN");
		sbSQL.append(", BMNCD");
		sbSQL.append(", DAICD");
		sbSQL.append(", CHUCD");
		sbSQL.append(", WARIGK");
		sbSQL.append(", ICHIRTGK");
		sbSQL.append(", WARIRT");
		sbSQL.append(", RANKNO_ADD");
		sbSQL.append(", RANKNO_DEL");
		sbSQL.append(", TENATSUK_ARR");
		sbSQL.append(" ))as RE on (T.KKKNO = RE.KKKNO)");
		sbSQL.append(" when matched then update set");
		sbSQL.append(" KKKNO=RE.KKKNO");
		sbSQL.append(", BTKN=RE.BTKN");
		sbSQL.append(", HBSTDT=RE.HBSTDT");
		sbSQL.append(", HBEDDT=RE.HBEDDT");
		sbSQL.append(", MOYSKBN=RE.MOYSKBN");
		sbSQL.append(", MOYSSTDT=RE.MOYSSTDT");
		sbSQL.append(", MOYSRBAN=RE.MOYSRBAN");
		sbSQL.append(", BMNCD=RE.BMNCD");
		sbSQL.append(", DAICD=RE.DAICD");
		sbSQL.append(", CHUCD=RE.CHUCD");
		sbSQL.append(", WARIRT=RE.WARIRT");
		sbSQL.append(", WARIGK=RE.WARIGK");
		sbSQL.append(", ICHIRTGK=RE.ICHIRTGK");
		sbSQL.append(", RANKNO_ADD=RE.RANKNO_ADD");
		sbSQL.append(", RANKNO_DEL=RE.RANKNO_DEL");
		sbSQL.append(", TENATSUK_ARR=RE.TENATSUK_ARR");
		sbSQL.append(", UPDKBN=RE.UPDKBN");
		sbSQL.append(", SENDFLG=RE.SENDFLG");
		sbSQL.append(", OPERATOR=RE.OPERATOR");
		sbSQL.append(", ADDDT = case when NVL(UPDKBN, 0) = 1 then RE.ADDDT else ADDDT end");
		sbSQL.append(", UPDDT=RE.UPDDT");
		sbSQL.append(" when not matched then insert (");
		sbSQL.append(" KKKNO");
		sbSQL.append(", BTKN");
		sbSQL.append(", HBSTDT");
		sbSQL.append(", HBEDDT");
		sbSQL.append(", MOYSKBN");
		sbSQL.append(", MOYSSTDT");
		sbSQL.append(", MOYSRBAN");
		sbSQL.append(", BMNCD");
		sbSQL.append(", DAICD");
		sbSQL.append(", CHUCD");
		sbSQL.append(", WARIRT");
		sbSQL.append(", WARIGK");
		sbSQL.append(", ICHIRTGK");
		sbSQL.append(", RANKNO_ADD");
		sbSQL.append(", RANKNO_DEL");
		sbSQL.append(", TENATSUK_ARR");
		sbSQL.append(", UPDKBN");
		sbSQL.append(", SENDFLG");
		sbSQL.append(", OPERATOR");
		sbSQL.append(", ADDDT");
		sbSQL.append(", UPDDT");
		sbSQL.append(") values (");
		sbSQL.append(" RE.KKKNO");
		sbSQL.append(", RE.BTKN");
		sbSQL.append(", RE.HBSTDT");
		sbSQL.append(", RE.HBEDDT");
		sbSQL.append(", RE.MOYSKBN");
		sbSQL.append(", RE.MOYSSTDT");
		sbSQL.append(", RE.MOYSRBAN");
		sbSQL.append(", RE.BMNCD");
		sbSQL.append(", RE.DAICD");
		sbSQL.append(", RE.CHUCD");
		sbSQL.append(", RE.WARIRT");
		sbSQL.append(", RE.WARIGK");
		sbSQL.append(", RE.ICHIRTGK");
		sbSQL.append(", RE.RANKNO_ADD");
		sbSQL.append(", RE.RANKNO_DEL");
		sbSQL.append(", RE.TENATSUK_ARR");
		sbSQL.append(", RE.UPDKBN");
		sbSQL.append(", RE.SENDFLG");
		sbSQL.append(", RE.OPERATOR");
		sbSQL.append(", RE.ADDDT");
		sbSQL.append(", RE.UPDDT");
		sbSQL.append(")");

		if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("分類割引_企画");

		// クリア
		prmData = new ArrayList<String>();
		valueData = new Object[]{};
		values = "";

		// 分類割引_対象除外店テーブルの削除処理
		if(StringUtils.equals(DefineReport.Button.SEL_CHANGE.getObj(), sendBtnid)){
			sbSQL = new StringBuffer();
			prmData.add(data.optString("F1"));
			sbSQL.append(" delete from INATK.TOKBT_ADTEN where KKKNO = ?");

			sqlList.add(sbSQL.toString());
			prmList.add(prmData);
			lblList.add("分類割引_対象除外店");

			// クリア
			prmData = new ArrayList<String>();
			valueData = new Object[]{};
			values = "";
		}

		// 入力された対象店・除外店のvaluesを作成
		for (int i = 0; i < 10; i++) {
			if (!StringUtils.isEmpty(tenCdAdds.getString(i))) {
				if (!values.equals("")) {
					values += ",";
				}
				values += "(?,?,1,0,'"+userId+"',current timestamp,current timestamp)";
				prmData.add(kkkno);
				prmData.add(tenCdAdds.getString(i));
			}
			if (!StringUtils.isEmpty(tenCdDels.getString(i))) {
				if (!values.equals("")) {
					values += ",";
				}
				values += "(?,?,0,0,'"+userId+"',current timestamp,current timestamp)";
				prmData.add(kkkno);
				prmData.add(tenCdDels.getString(i));

			}
		}

		// 対象店・除外店に入力があった場合Insertを実行
		if (!StringUtils.isEmpty(values)) {
			sbSQL = new StringBuffer();
			sbSQL.append("INSERT INTO INATK.TOKBT_ADTEN ( ");
			sbSQL.append("KKKNO");
			sbSQL.append(",TENCD");
			sbSQL.append(",ADDDELFLG");
			sbSQL.append(",SENDFLG");
			sbSQL.append(",OPERATOR ");
			sbSQL.append(",ADDDT ");
			sbSQL.append(",UPDDT ");
			sbSQL.append(")");
			sbSQL.append("SELECT ");
			sbSQL.append(" KKKNO");								// 企画No
			sbSQL.append(",TENCD");								// 店コード
			sbSQL.append(",ADDDELFLG");							// 対象除外フラグ
			sbSQL.append(",SENDFLG");							// 送信フラグ
			sbSQL.append(",OPERATOR ");							// オペレーター
			sbSQL.append(",ADDDT ");							// 登録日
			sbSQL.append(",UPDDT ");							// 更新日
			sbSQL.append(" FROM (values "+values+") as T1(");
			sbSQL.append(" KKKNO");								// 企画No
			sbSQL.append(",TENCD");								// 店コード
			sbSQL.append(",ADDDELFLG");							// 対象除外フラグ
			sbSQL.append(",SENDFLG");							// 送信フラグ
			sbSQL.append(",OPERATOR ");							// オペレーター
			sbSQL.append(",ADDDT ");							// 登録日
			sbSQL.append(",UPDDT ");							// 更新日
			sbSQL.append(")");

			if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

			sqlList.add(sbSQL.toString());
			prmList.add(prmData);
			lblList.add("分類割引_対象除外店");
		}

		return sbSQL.toString();
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

		String tbl = "";
		String col = "";
		String rep = "";

		// 部門コード
		if (outobj.equals(DefineReport.InpText.BMNCD.getObj())){
			tbl="INAMS.MSTBMN";
			col=" BMNCD";
		}
		// 大分類コード
		if (outobj.equals(DefineReport.InpText.DAICD.getObj())){
			tbl="INAMS.MSTDAIBRUI";
			col=" BMNCD || DAICD";
		}
		if(tbl.length()>0&&col.length()>0){
			if(paramData.size() > 0 && rep.length() > 0 ){
				sqlcommand = DefineReport.ID_SQL_CHK_TBL.replace("@T", tbl).replaceAll("@C", col).replace("?", rep);
			}else{
				paramData.add(value);
				sqlcommand = DefineReport.ID_SQL_CHK_TBL.replace("@T", tbl).replaceAll("@C", col);
			}

			@SuppressWarnings("static-access")
			JSONArray array = iL.selectJSONArray(sqlcommand, paramData, Defines.STR_JNDI_DS);
			if(array.size() > 0 && NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) > 0){
				return true;
			}
		}
		return false;
	}


	// 対象店取得処理(更新処理用)
	public Set<Integer> getTenCdAddUpdate(String kkkno, JSONArray tenCdAdds, JSONArray tenCdDels) {

		ArrayList<String> paramData = new ArrayList<String>();
		String sqlWhere	= "";
		String sqlFrom	= "INATK.TOKRANK ";

		// 格納用変数
		StringBuffer	sbSQL		= new StringBuffer();
		ItemList		iL			= new ItemList();
		JSONArray		dbDatas 	= new JSONArray();
		JSONObject		data		= new JSONObject();
		String[]		taisyoTen	= new String[]{};
		String[]		jyogaiTen	= new String[]{};
		Set<Integer>	tencds		= new TreeSet<Integer>();

		// 保存済み店取扱いフラグ配列の逆展開を実行
		sbSQL.append(" with WK as (");
		sbSQL.append("select");
		sbSQL.append(" TENATSUK_ARR as ARR");
		sbSQL.append(", 1 as LEN");
		sbSQL.append(" from INATK.TOKBT_KKK");
		sbSQL.append(" where UPDKBN = "+DefineReport.ValUpdkbn.NML.getVal());
		sbSQL.append(" and KKKNO = "+kkkno);
		sbSQL.append("), ARRWK(IDX, RNK, S, ARR, LEN) as (");
		sbSQL.append("select 1");
		sbSQL.append(", SUBSTR(ARR, 1, LEN)");
		sbSQL.append(", 1 + LEN");
		sbSQL.append(", ARR, LEN");
		sbSQL.append(" from WK");
		sbSQL.append(" union all");
		sbSQL.append(" select IDX + 1");
		sbSQL.append(", SUBSTR(ARR, S, LEN)");
		sbSQL.append(", S + LEN, ARR");
		sbSQL.append(", LEN from ARRWK");
		sbSQL.append(" where S <= LENGTH(ARR)");
		sbSQL.append("), ADDTEN as (");
		sbSQL.append("select");
		sbSQL.append(" TENCD");
		sbSQL.append(" from INATK.TOKBT_ADTEN");
		sbSQL.append(" where KKKNO = "+kkkno+" and ADDDELFLG = 1");
		sbSQL.append("), DELTEN as (select");
		sbSQL.append(" TENCD");
		sbSQL.append(" from INATK.TOKBT_ADTEN");
		sbSQL.append(" where KKKNO = "+kkkno+" and ADDDELFLG = 0");
		sbSQL.append(") select");
		sbSQL.append(" T1.IDX");
		sbSQL.append(", case");
		sbSQL.append(" when T3.TENCD is not null then ''");
		sbSQL.append(" when T4.TENCD is not null then '1'");
		sbSQL.append(" else T1.RNK end as ARR");
		sbSQL.append(" from ARRWK T1");
		sbSQL.append(" inner join INAMS.MSTTEN T2 on T1.IDX = T2.TENCD");
		sbSQL.append(" left join ADDTEN T3 on T3.TENCD = T1.IDX");
		sbSQL.append(" left join DELTEN T4 on T4.TENCD = T1.IDX");

		dbDatas = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);

		if (dbDatas.size() > 0) {
			data	= dbDatas.getJSONObject(0);
			taisyoTen = data.optString("TENRANK_ARR").split("");
		}

		for (int i = 0; i < dbDatas.size(); i++) {
			data	= dbDatas.getJSONObject(i);
			if (!StringUtils.isEmpty(data.optString("ARR").trim())) {
				if(!tencds.contains(i+1)) {
					tencds.add(i+1);
				}
			}
		}

		// 対象店を追加
		for (int i = 0; i < tenCdAdds.size(); i++) {
			if (!tenCdAdds.getString(i).equals("") && !tencds.contains(tenCdAdds.getInt(i))) {
				tencds.add(tenCdAdds.getInt(i));
			}
		}

		// 除外店を削除
		for (int i = 0; i < tenCdDels.size(); i++) {
			if (!tenCdDels.getString(i).equals("")) {
				tencds.remove(tenCdDels.getInt(i));
			}
		}

		return tencds;
	}

	/**
	 * 分類割引_企画DELETE SQL作成処理
	 *
	 * @param userInfo
	 * @param Sqlprm		 入力No
	 * @throws Exception
	 */
	public JSONObject createSqlDelTOKBT_KKK(User userInfo, String Sqlprm){
		JSONObject result = new JSONObject();

		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();

		String userId	= userInfo.getId();											// ログインユーザー

		StringBuffer sbSQL;
		sbSQL = new StringBuffer();
		prmData.add(Sqlprm);

		//sbSQL.append("delete from INATK.HATSK where INPUTNO = ?");
		sbSQL.append("UPDATE INATK.TOKBT_KKK ");
		sbSQL.append("SET ");
		sbSQL.append("UPDKBN=" + DefineReport.ValUpdkbn.DEL.getVal());
		sbSQL.append(",OPERATOR='" + userId + "'");
		sbSQL.append(",UPDDT=current timestamp ");
		sbSQL.append(" WHERE KKKNO = ? ");

		if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("分類割引_企画");

		// 子要素の削除
		sbSQL = new StringBuffer();
		sbSQL.append(" delete from INATK.TOKBT_ADTEN where KKKNO = ?");

		if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("分類割引_対象除外店");

		return result;
	}

	/**
	 * SEQ情報取得処理(分類割引)
	 *
	 * @throws Exception
	 */
	public String getKKKCD_SEQ() {
		// 関連情報取得
		ItemList iL = new ItemList();
		String sqlColCommand = "VALUES NEXTVAL FOR INAMS.SEQ005";
		@SuppressWarnings("static-access")
		JSONArray array = iL.selectJSONArray(sqlColCommand, null, Defines.STR_JNDI_DS);
		String value = "";
		if(array.size() > 0){
			value = array.optJSONObject(0).optString("1");
		}
		return value;
	}

}
