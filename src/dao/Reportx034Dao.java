package dao;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;

import authentication.bean.User;
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
public class Reportx034Dao extends ItemDao {

	private static String DECI_DIGITS = ",20,10";

	/**
	 * インスタンスを生成します。
	 * @param source
	 */
	public Reportx034Dao(String JNDIname) {
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

	/**
	 * 更新処理
	 * @param userInfo
	 *
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws Exception
	 */
	public JSONObject update(HttpServletRequest request,HttpSession session,  HashMap<String, String> map, User userInfo) {

		// 更新情報チェック(基本JS側で制御)
		JSONObject msgObj = new JSONObject();
		JSONArray msg = this.check(map);

		if(msg.size() > 0){
			msgObj.put(MsgKey.E.getKey(), msg);
			return msgObj;
		}

		// 更新処理
		try {
			msgObj = this.updateData(map, userInfo);
		} catch (Exception e) {
			e.printStackTrace();
			msgObj.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
		}
		return msgObj;
	}

	/**
	 * 削除処理
	 * @param userInfo
	 *
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws Exception
	 */
	public JSONObject delete(HttpServletRequest request,HttpSession session,  HashMap<String, String> map, User userInfo) {

		// 更新情報チェック(基本JS側で制御)
		JSONObject msgObj = new JSONObject();
		JSONArray msg = this.check(map);

		if(msg.size() > 0){
			msgObj.put(MsgKey.E.getKey(), msg);
			return msgObj;
		}

		// 削除処理
		try {
			msgObj = this.deleteData(map, userInfo);
		} catch (Exception e) {
			e.printStackTrace();
			msgObj.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00002.getVal()));
		}
		return msgObj;
	}

	private String createCommand() {
		// ログインユーザー情報取得
		User userInfo = getUserInfo();
		if(userInfo==null){
			return "";
		}

		String Bmoncd = getMap().get("BUMON");								// 部門コード
		String Daicd = getMap().get("DAICD");								// 大分類コード
		String Chucd = getMap().get("CHUCD");								// 中分類コード
		String Shocd = getMap().get("SHOCD");								// 小分類コード

		// パラメータ確認
		// 必須チェック
		if ( Bmoncd == null || Daicd == null || Chucd == null || Shocd == null ) {
			System.out.println(super.getConditionLog());
			return "";
		}

		// タイトル情報(任意)設定
		List<String> titleList = new ArrayList<String>();

		// 一覧表情報
		StringBuffer sbSQL = new StringBuffer();

		sbSQL.append(" select");
		sbSQL.append(" null as SSHOCD");
		sbSQL.append(", null");
		sbSQL.append(", null");
		sbSQL.append(", null");
		sbSQL.append(", null");
		sbSQL.append(", null");
		sbSQL.append(", null");
		sbSQL.append(", null");
		sbSQL.append(", null");
		sbSQL.append(", null");
		sbSQL.append(", null");
		sbSQL.append(", null");
		sbSQL.append(", 1 as SUPDKBN");
		sbSQL.append(", " + DefineReport.ValUpdkbn.NML.getVal());
		sbSQL.append(", null");
		// TODO 暫定
		sbSQL.append(" from (values ('', '')) as X(value, TEXT)");
		sbSQL.append(" union all");
		sbSQL.append(" select");
		sbSQL.append(" SSHO.SSHOCD");															// F1	：小小分類コード
		sbSQL.append(", SSHO.SSHOBRUIAN");														// F2	：小小分類名（カナ）
		sbSQL.append(", SSHO.SSHOBRUIKN");														// F3	：小小分類名（漢字）
		sbSQL.append(", SSHO.ATR1");															// F4	：属性1
		sbSQL.append(", SSHO.ATR2");															// F5	：属性2
		sbSQL.append(", SSHO.ATR3");															// F6	：属性3
		sbSQL.append(", SSHO.ATR4");															// F7	：属性4
		sbSQL.append(", SSHO.ATR5");															// F8	：属性5
		sbSQL.append(", SSHO.ATR6");															// F9	：属性6
		sbSQL.append(", TO_CHAR(SSHO.ADDDT, 'yy/mm/dd')");										// F10	：登録日
		sbSQL.append(", TO_CHAR(SSHO.UPDDT, 'yy/mm/dd')");										// F11	：更新日
		sbSQL.append(", SSHO.OPERATOR");														// F12	：オペレータ
		sbSQL.append(", 0 as SUPDKBN");															// F13	：新規登録区分(非表示)
		sbSQL.append(", SSHO.UPDKBN");															// F14	：更新区分
		sbSQL.append(", TO_CHAR(SSHO.UPDDT, 'YYYYMMDDHH24MISSNNNNNN') as HDN_UPDDT");			// F15	：(非表示)
		sbSQL.append(" from INAMS.MSTSSHOBRUI SSHO");
		sbSQL.append(" where NVL(SSHO.UPDKBN, 0) = 0 and SSHO.BMNCD = "+Bmoncd+" and SSHO.DAICD = "+Daicd+" and SSHO.CHUCD = "+Chucd+" and SSHO.SHOCD = "+Shocd);
		sbSQL.append(" order by SSHOCD, SUPDKBN");

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
	 * 更新処理実行
	 * @return
	 *
	 * @throws Exception
	 */
	@SuppressWarnings("static-access")
	private JSONObject updateData(HashMap<String, String> map, User userInfo) throws Exception {
		// パラメータ確認
		JSONArray dataArray = JSONArray.fromObject(map.get("DATA"));	// 対象情報
		JSONObject msgObj = new JSONObject();
		JSONArray msg = new JSONArray();

		String updateRows = "";											// 更新データ
		//Object[] createRows = new Object[]{};							// 新規登録データ

		// ログインユーザー情報取得
		String userId	= userInfo.getId();								// ログインユーザー

		//String sendBtnid = map.get("SENDBTNID");						// 呼出しボタン
		String bmoncd	 = map.get("BUMON");							// 入力部門コード
		String daicd	 = map.get("DAICD");							// 入力大分類コード
		String chucd	 = map.get("CHUCD");							// 入力中分類コード
		String shocd	 = map.get("SHOCD");;							// 入力小分類コード
		String sshocd	 = "";											// 入力小小分類コード
		String adddt	 = "";											// 登録日
		String updflg	 = "";											// 更新フラグ
		String supdate	 = "";											// 新規登録フラグ
		// 入力部門コード
		// 更新情報
		for (int i = 0; i < dataArray.size(); i++) {
			JSONObject data = dataArray.getJSONObject(i);
			if(data.isEmpty()){
				continue;
			}

			sshocd	 = data.optString("F1");
			supdate	 = data.optString("F13");
			updflg = data.optString("F14");
			String values = "";
			values += bmoncd +",";
			values += daicd +",";
			values += chucd +",";
			values += shocd +",";
			values += StringUtils.defaultIfEmpty(data.optString("F1"), "null")+",";
			values += StringUtils.defaultIfEmpty("'"+data.optString("F2")+"'", "null")+",";
			values += StringUtils.defaultIfEmpty("'"+data.optString("F3")+"'", "null")+",";
			values += StringUtils.defaultIfEmpty(data.optString("F4"), "null")+",";
			values += StringUtils.defaultIfEmpty(data.optString("F5"), "null")+",";
			values += StringUtils.defaultIfEmpty(data.optString("F6"), "null")+",";
			values += StringUtils.defaultIfEmpty(data.optString("F7"), "null")+",";
			values += StringUtils.defaultIfEmpty(data.optString("F8"), "null")+",";
			values += StringUtils.defaultIfEmpty(data.optString("F9"), "null")+",";
			values += (StringUtils.isEmpty(updflg) ? DefineReport.ValUpdkbn.NML.getVal() : DefineReport.ValUpdkbn.DEL.getVal())+",";;
			values += "null,";
			values += "null,";
			values += "null,";
			values += "null";

			// 削除処理
			if(StringUtils.equals(updflg, DefineReport.ValUpdkbn.DEL.getVal())){
				JSONArray msgdl = this.deleteChildData(map, data);
				if(msgdl.size() > 0){
					msg.add(msgdl);
				}
			}
			if(!StringUtils.isEmpty(bmoncd) && !StringUtils.isEmpty(daicd)){
				// 未入力新規登録データを省く
				updateRows += ",("+values+")";
			}
		}
		updateRows = StringUtils.removeStart(updateRows, ",");

		if(updateRows.length()==0){
			msgObj.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
			return msgObj;
		}

		// 基本INSERT/UPDATE文
		StringBuffer sbSQL;
		ItemList iL = new ItemList();
		JSONArray dbDatas = new JSONArray();
		ArrayList<String> prmData = new ArrayList<String>();

		// 重複チェック：小小分類コード
		if(StringUtils.equals(supdate, "1")){
			sbSQL = new StringBuffer();
			sbSQL.append("select * from INAMS.MSTSSHOBRUI SSHO where NVL(SSHO.UPDKBN, 0) = 0 and SSHO.BMNCD = "+bmoncd+" and SSHO.DAICD = "+daicd+" and SSHO.CHUCD = "+chucd+" and SSHO.SHOCD = "+shocd+" and SSHO.SSHOCD = "+sshocd);
			dbDatas = iL.selectJSONArray(sbSQL.toString(), null, Defines.STR_JNDI_DS);
			if(dbDatas.size() > 0 ){
				msg.add(MessageUtility.getMsg("小小分類コードが重複しています。"));
			}

			// 新規登録(論理削除したデータの上書きの場合)
			sbSQL = new StringBuffer();
			sbSQL.append("select * from INAMS.MSTSSHOBRUI SSHO where NVL(SSHO.UPDKBN, 0) = "+DefineReport.ValUpdkbn.DEL.getVal()+" and SSHO.BMNCD = "+bmoncd+" and SSHO.DAICD = "+daicd+" and SSHO.SHOCD = "+shocd+" and SSHO.SSHOCD = "+sshocd);
			dbDatas = iL.selectJSONArray(sbSQL.toString(), null, Defines.STR_JNDI_DS);
			if(dbDatas.size() > 0 ){
				adddt = ", ADDDT = RE.ADDDT";
			}
		}

		// 更新SQL
		sbSQL = new StringBuffer();
		sbSQL.append("merge into INAMS.MSTSSHOBRUI as T using (select");
		sbSQL.append(" T1.BMNCD");
		sbSQL.append(", T1.DAICD");
		sbSQL.append(", T1.CHUCD");
		sbSQL.append(", T1.SHOCD");
		sbSQL.append(", T1.SSHOCD");
		sbSQL.append(", T1.SSHOBRUIAN");
		sbSQL.append(", T1.SSHOBRUIKN");
		sbSQL.append(", T1.ATR1");
		sbSQL.append(", T1.ATR2");
		sbSQL.append(", T1.ATR3");
		sbSQL.append(", T1.ATR4");
		sbSQL.append(", T1.ATR5");
		sbSQL.append(", T1.ATR6");
		sbSQL.append(", T1.UPDKBN");
		sbSQL.append(", T1.SENDFLG");
		// TODO 暫定
		sbSQL.append(", '"+userId+"' as OPERATOR");
		sbSQL.append(", current timestamp as ADDDT");
		sbSQL.append(", current timestamp as UPDDT");
		sbSQL.append(" from (values "+updateRows+") as T1(");
		sbSQL.append(" BMNCD");
		sbSQL.append(", DAICD");
		sbSQL.append(", CHUCD");
		sbSQL.append(", SHOCD");
		sbSQL.append(", SSHOCD");
		sbSQL.append(", SSHOBRUIAN");
		sbSQL.append(", SSHOBRUIKN");
		sbSQL.append(", ATR1");
		sbSQL.append(", ATR2");
		sbSQL.append(", ATR3");
		sbSQL.append(", ATR4");
		sbSQL.append(", ATR5");
		sbSQL.append(", ATR6");
		sbSQL.append(", UPDKBN");
		sbSQL.append(", SENDFLG");
		sbSQL.append(", OPERATOR");
		sbSQL.append(", ADDDT");
		sbSQL.append(", UPDDT)) as RE on T.BMNCD = RE.BMNCD and T.DAICD = RE.DAICD and T.CHUCD = RE.CHUCD and T.SHOCD = RE.SHOCD and T.SSHOCD = RE.SSHOCD");
		sbSQL.append(" when matched then update set");
		sbSQL.append(" BMNCD = RE.BMNCD");
		sbSQL.append(", DAICD = RE.DAICD");
		sbSQL.append(", CHUCD = RE.CHUCD");
		sbSQL.append(", SHOCD = RE.SHOCD");
		sbSQL.append(", SSHOCD = RE.SSHOCD");
		sbSQL.append(", SSHOBRUIAN = RE.SSHOBRUIAN");
		sbSQL.append(", SSHOBRUIKN = RE.SSHOBRUIKN");
		sbSQL.append(", ATR1 = RE.ATR1");
		sbSQL.append(", ATR2 = RE.ATR2");
		sbSQL.append(", ATR3 = RE.ATR3");
		sbSQL.append(", ATR4 = RE.ATR4");
		sbSQL.append(", ATR5 = RE.ATR5");
		sbSQL.append(", ATR6 = RE.ATR6");
		sbSQL.append(", UPDKBN = RE.UPDKBN");
		sbSQL.append(", SENDFLG = RE.SENDFLG");
		sbSQL.append(", OPERATOR = RE.OPERATOR");
		sbSQL.append( 	adddt);
		sbSQL.append(", UPDDT = RE.UPDDT");
		sbSQL.append(" when not matched then insert values (");
		sbSQL.append(" RE.BMNCD");
		sbSQL.append(", RE.DAICD");
		sbSQL.append(", RE.CHUCD");
		sbSQL.append(", RE.SHOCD");
		sbSQL.append(", RE.SSHOCD");
		sbSQL.append(", RE.SSHOBRUIAN");
		sbSQL.append(", RE.SSHOBRUIKN");
		sbSQL.append(", RE.ATR1");
		sbSQL.append(", RE.ATR2");
		sbSQL.append(", RE.ATR3");
		sbSQL.append(", RE.ATR4");
		sbSQL.append(", RE.ATR5");
		sbSQL.append(", RE.ATR6");
		sbSQL.append(", RE.UPDKBN");
		sbSQL.append(", RE.SENDFLG");
		sbSQL.append(", RE.OPERATOR");
		sbSQL.append(", RE.ADDDT");
		sbSQL.append(", RE.UPDDT)");

		if (msg.size() > 0) {
			// 重複チェック_エラー時
			msgObj.put(MsgKey.E.getKey(), msg);

		}else{
			// 更新処理実行
			if (DefineReport.ID_DEBUG_MODE) System.out.println(sbSQL);
			int count = super.executeSQL(sbSQL.toString(), prmData);
			if(StringUtils.isEmpty(getMessage())) {
				if (DefineReport.ID_DEBUG_MODE) System.out.println("小分類を " + MessageUtility.getMessage(Msg.S00003.getVal(),
						new String[] { Integer.toString(dataArray.size()), Integer.toString(count) }));
				msgObj.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00001.getVal()));
			}else{
				msgObj.put(MsgKey.E.getKey(), getMessage());
			}
		}
		return msgObj;
	}

	/**
	 * 削除処理実行
	 * @return
	 *
	 * @throws Exception
	 */
	@SuppressWarnings("static-access")
	private JSONArray deleteChildData(HashMap<String, String> map, JSONObject date) throws Exception {

		String Bmoncd	 = map.get("BUMON");								// 部門コード
		String Daicd	 = map.get("DAICD");								// 大分類コード
		String Chucd	 = map.get("CHUCD");								// 中分類コード
		String Shocd	 = map.get("SHOCD");								// 小分類コード
		String Sshocd 	 = date.optString("F1");								// 小小分類コード

		JSONObject msgObj = new JSONObject();
		JSONArray msg = new JSONArray();

		// 基本INSERT/UPDATE文
		ItemList iL = new ItemList();
		JSONArray dbDatas = new JSONArray();
		StringBuffer sbSQL;
		ArrayList<String> prmData = new ArrayList<String>();

		// 部門紐付チェック:商品マスタ
		sbSQL = new StringBuffer();
		sbSQL.append("select SHN.* from INAMS.MSTSHN SHN where SHN.BMNCD = "+Bmoncd+" and SHN.DAICD = "+Daicd+" and SHN.CHUCD = "+Chucd+" and SHN.SHOCD = "+Shocd+" and SHN.SSHOCD = "+Sshocd+" and NVL(SHN.UPDKBN, 0) <> 1");
		dbDatas = iL.selectJSONArray(sbSQL.toString(), null, Defines.STR_JNDI_DS);
		if(dbDatas.size() > 0 ){
			msg.add(MessageUtility.getMsg("商品マスタにて小小分類コードを使用している為、削除出来ません"));
		}

		/*if(msg.size() > 0){

		}else{
			// 小分類マスタ_他小分類検索
			sbSQL = new StringBuffer();
			sbSQL.append("select SHO.* from INAMS.MSTSHOBRUI SHO where SHO.BMNCD = "+Bmoncd+" and SHO.DAICD = "+Daicd+" and SHO.CHUCD = "+Chucd+" and NVL(SHO.UPDKBN, 0) <> 1");
			dbDatas = iL.selectJSONArray(sbSQL.toString(), null, Defines.STR_JNDI_DS);
			if(dbDatas.size() > 0 ){

			}else{
				// 削除処理：小小分類マスタの更新区分に"1"（削除）を登録
				sbSQL = new StringBuffer();
				sbSQL.append("update INAMS.MSTSSHOBRUI SSHO set SSHO.UPDKBN = "+DefineReport.ValUpdkbn.DEL.getVal()+" where SSHO.BMNCD = "+Bmoncd+" and SSHO.DAICD = "+Daicd+" and SSHO.SSHOCD = "+Chucd+" and SSHO.SHOCD = "+Shocd+" and SSHO.SSHOCD = "+Sshocd);

				int count = super.executeSQL(sbSQL.toString(), prmData);
				if(StringUtils.isEmpty(getMessage())){
					System.out.println("小小分類を "+MessageUtility.getMessage(Msg.S00004.getVal(), new String[]{Integer.toString(count)}));
					msgObj.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00002.getVal()));
				}else{
					msgObj.put(MsgKey.E.getKey(), getMessage());
				}


			}

		}*/
		return msg;
	}

	/**
	 * 削除処理実行
	 * @return
	 *
	 * @throws Exception
	 */
	@SuppressWarnings("static-access")
	private JSONObject deleteData(HashMap<String, String> map, User userInfo) throws Exception {
		// パラメータ確認
		JSONArray dataArray = JSONArray.fromObject(map.get("DATA"));	// 対象情報

		JSONObject msgObj = new JSONObject();
		JSONArray msg = new JSONArray();

		// ログインユーザー情報取得
		String userId	= userInfo.getId();								// ログインユーザー

		// 更新情報
		String values = "";
		String bmoncd = "";												// 入力部門コード
		String daicd = "";												// 入力大分類コード
		for (int i = 0; i < dataArray.size(); i++) {
			JSONObject data = dataArray.getJSONObject(i);
			if(data.isEmpty()){
				continue;
			}

			bmoncd=data.optString("F2");
			daicd=data.optString("F2");
			values += ",'"+data.optString("F2")+"'";						// 大分類コード
		}
		values = StringUtils.removeStart(values, ",");

		if(values.length()==0){
			msgObj.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00002.getVal()));
			return msgObj;
		}

		// 基本INSERT/UPDATE文
		ItemList iL = new ItemList();
		JSONArray dbDatas = new JSONArray();
		StringBuffer sbSQL;
		ArrayList<String> prmData = new ArrayList<String>();

		return msgObj;
	}

	/**
	 * チェック処理
	 *
	 * @throws Exception
	 */
	public JSONArray check(HashMap<String, String> map) {
		// パラメータ確認
		JSONArray dataArray = JSONArray.fromObject(map.get("DATA"));	// 更新情報
		String outobj		= map.get(DefineReport.ID_PARAM_OBJ);		// 実行ボタン

		JSONArray msg = new JSONArray();

		// 入力値を取得
		JSONObject data = dataArray.getJSONObject(0);

		// チェック処理
		// 対象件数チェック
		if(dataArray.size() == 0 || dataArray.getJSONObject(0).isEmpty()){
			msg.add(MessageUtility.getMessageObj(Msg.E10000.getVal()));
			return msg;
		}

		// 各データ行チェック
//		if(!outobj.equals(DefineReport.Button.DELETE.getObj())) {
//			FieldType fieldType = FieldType.GRID;
//			for (int i = 0; i < dataArray.size(); i++) {
//				JSONObject data = dataArray.getJSONObject(i);
//				String[] addParam = {Integer.toString(i+1)};
//				// 比較対象店舗必須チェック
//				if(!InputChecker.isNotNull(data.optString("F2"))){
//					msg.add(MessageUtility.getCheckNullMessage(DefineReport.Select.M_TENPO.getTxt(), fieldType, addParam));
//				}
//			}
//
//		}
		return msg;
	}
}
