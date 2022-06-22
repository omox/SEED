package dao;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import authentication.bean.User;
import common.DefineReport;
import common.Defines;
import common.ItemList;
import common.JsonArrayData;
import common.MessageUtility;
import common.MessageUtility.FieldType;
import common.MessageUtility.Msg;
import common.MessageUtility.MsgKey;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 */
public class ReportSK002Dao extends ItemDao {

	/** SQLリスト保持用変数 */
	ArrayList<String> sqlList = new ArrayList<String>();
	/** SQLのパラメータリスト保持用変数 */
	ArrayList<ArrayList<String>> prmList = new ArrayList<ArrayList<String>>();
	/** SQLログ用のラベルリスト保持用変数 */
	ArrayList<String> lblList = new ArrayList<String>();

	/**
	 * インスタンスを生成します。
	 * @param source
	 */
	public ReportSK002Dao(String JNDIname) {
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
		JSONArray msg = this.checkDel(map);

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

		String	yobiInfo	 = getMap().get("YOBIINFO");				// 呼出しボタン

		// タイトル情報(任意)設定
		List<String> titleList = new ArrayList<String>();

		// 一覧表情報
		StringBuffer sbSQL = new StringBuffer();

		sbSQL.append(DefineReport.ID_SQL_CMN_WEEK);
		sbSQL.append(" select");
		sbSQL.append(" right ('00000' || T1.INPUTNO, 5)");							// F1 : 入力NO
		sbSQL.append(", right ('000' || T1.TENNO, 3)");								// F2 : 店番
		sbSQL.append(", T1.TENKN");													// F3 : 店舗名
		sbSQL.append(", left (right ('00000000' || T1.KSPAGE,8), 4) || '-' ||");
		sbSQL.append(" SUBSTR(right ('00000000' || T1.KSPAGE,8), 5)");				// F4 : 構成ページ
		//sbSQL.append(", left (T1.KSPAGE, 4) || '-' || SUBSTR(T1.KSPAGE, 5)");		// F4 : 構成ページ
		sbSQL.append(", T1.HTDT || W1.JWEEK");										// F5 : 発注日
		sbSQL.append(", T1.NNDT || W2.JWEEK");										// F6 : 納入日
		sbSQL.append(", T1.SHNKBN");												// F7 : 商品区分
		sbSQL.append(" from (select");
		sbSQL.append(" HSK.INPUTNO");
		sbSQL.append(", HSK.TENNO");
		sbSQL.append(", TEN.TENKN");
		sbSQL.append(", HSK.KSPAGE");
		sbSQL.append(", TO_CHAR(TO_DATE('20' || right ('0' || HSK.HTDT, 6), 'YYYYMMDD'), 'YY/MM/DD') as HTDT");
		sbSQL.append(", DAYOFWEEK(TO_DATE('20' || right ('0' || HSK.HTDT, 6), 'YYYYMMDD')) as HTDT_WNUM");
		sbSQL.append(", TO_CHAR(TO_DATE('20' || right ('0' || HSK.NNDT, 6), 'YYYYMMDD'), 'YY/MM/DD') as NNDT");
		sbSQL.append(", DAYOFWEEK(TO_DATE('20' || right ('0' || HSK.NNDT, 6), 'YYYYMMDD')) as NNDT_WNUM");
		sbSQL.append(", HSK.SHNKBN from INATK.HATSK HSK");
		sbSQL.append(" left join INAMS.MSTTEN TEN on TEN.TENCD = HSK.TENNO where HSK.UPDKBN = 0) T1");
		sbSQL.append(" left outer join WEEK W1 on T1.HTDT_WNUM = W1.CWEEK");
		sbSQL.append(" left outer join WEEK W2 on T1.NNDT_WNUM = W2.CWEEK");

		if(StringUtils.equals("0", yobiInfo)){
			sbSQL.append(" where T1.KSPAGE is null");
			sbSQL.append(" order by T1.INPUTNO, T1.TENNO");

		}else if(StringUtils.equals("2", yobiInfo)){
			sbSQL.append(" where T1.KSPAGE is not null");
			sbSQL.append(" order by T1.TENNO, T1.KSPAGE");

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
	 * 予約発注_企画削除処理
	 *
	 * @param data
	 * @param map
	 * @param userInfo
	 */
	public String createDelSqlHtk(JSONObject data, HashMap<String, String> map, User userInfo){

		// 削除データ検索用コード

		// 格納用変数
		StringBuffer	sbSQL	= new StringBuffer();

		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();

		// ログインユーザー情報取得
		String userId	= userInfo.getId(); // ログインユーザー

		// 予約発注_企画
		sbSQL = new StringBuffer();
		prmData = new ArrayList<String>();
		sbSQL.append("update INATK.HATYH_KKK set");
		sbSQL.append(" UPDKBN = "+DefineReport.ValUpdkbn.DEL.getVal());
		sbSQL.append(", OPERATOR = '" + userId + "' ");
		sbSQL.append(", UPDDT = current timestamp ");
		sbSQL.append(" where KKKCD = ? ");
		prmData.add(data.optString("F1"));
		if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("予約発注_企画");

		// 予約発注_商品
		sbSQL = new StringBuffer();
		prmData = new ArrayList<String>();
		sbSQL.append("update INATK.HATYH_SHN set");
		sbSQL.append(" UPDKBN = "+DefineReport.ValUpdkbn.DEL.getVal());
		sbSQL.append(", OPERATOR = '" + userId + "' ");
		sbSQL.append(", UPDDT = current timestamp ");
		sbSQL.append(" where KKKCD = ? ");
		prmData.add(data.optString("F1"));
		if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("予約発注_商品");

		// 予約発注_納品日
		sbSQL = new StringBuffer();
		prmData = new ArrayList<String>();
		sbSQL.append("delete from INATK.HATYH_NNDT where KKKCD = ?");
		prmData.add(data.optString("F1"));
		if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("予約発注_納品日");

		// 予約発注_店舗
		sbSQL = new StringBuffer();
		prmData = new ArrayList<String>();
		sbSQL.append("delete from INATK.HATYH_TEN where KKKCD = ?");
		prmData.add(data.optString("F1"));
		if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

		sqlList.add(sbSQL.toString());
		prmList.add(prmData);
		lblList.add("予約発注_店舗");

		return sbSQL.toString();
	}

	/**
	 * 削除処理実行
	 * @return
	 *
	 * @throws Exception
	 */
	private JSONObject deleteData(HashMap<String, String> map, User userInfo) throws Exception {
		// パラメータ確認
		JSONObject option = new JSONObject();
		JSONArray	dataArray	= JSONArray.fromObject(map.get("DATA"));	// 対象情報（主要な更新情報）

		// 排他チェック用
		String				targetTable	= null;
		String				targetWhere	= null;
		ArrayList<String>	targetParam	= new ArrayList<String>();
		JSONArray			msg			= new JSONArray();

		// パラメータ確認
		// 必須チェック
		if ( dataArray.isEmpty() || dataArray.size() != 1 || dataArray.getJSONObject(0).isEmpty()) {
			option.put(MsgKey.E.getKey(), MessageUtility.getMessage(Msg.E00001.getVal()));
			return option;
		}

		// 基本登録情報
		JSONObject data = dataArray.getJSONObject(0);


		// 予約発注_企画DELETE処理
		this.createDelSqlHtk(data,map,userInfo);

		// 排他チェック実行
		targetTable = "INATK.HATYH_KKK";
		targetWhere = " KKKCD = ?";
		targetParam.add(data.optString("F1"));

		if(!super.checkExclusion(targetTable, targetWhere, targetParam, data.optString("F5"))){
 			msg.add(MessageUtility.getDbMessageExclusion(FieldType.DEFAULT, new String[]{}));
 			option.put(MsgKey.E.getKey(), msg);
			return option;
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
				option.put(MsgKey.S.getKey(), MessageUtility.getMessage(Msg.S00001.getVal()));
			}
		}else{
			option.put(MsgKey.E.getKey(), getMessage());
		}
		return option;
	}

	/**
	 * チェック処理(削除時)
	 *
	 * @throws Exception
	 */
	@SuppressWarnings("static-access")
	public JSONArray checkDel(HashMap<String, String> map) {
		// パラメータ確認

		MessageUtility mu	 = new MessageUtility();

		JSONArray	dataArray	 = JSONArray.fromObject(map.get("DATA"));		// 対象情報（主要な更新情報）

		JSONArray msg				 = new JSONArray();
		ItemList iL					 = new ItemList();
		JSONArray dbDatas			 = new JSONArray();
		StringBuffer sbSQL			 = new StringBuffer();
		ArrayList<String> prmData	 = new ArrayList<String>();

		// 基本登録情報
		JSONObject data = dataArray.getJSONObject(0);

		sbSQL = new StringBuffer();
		sbSQL.append("select SHNCD from INATK.HATYH_SHN where KKKCD = "+data.optString("F1")+" fetch first 1 rows only");
		dbDatas = iL.selectJSONArray(sbSQL.toString(), null, Defines.STR_JNDI_DS);

		if(dbDatas.size() > 0 ){
			msg.add(mu.getDbMessageObj("E00006", new String[]{}));

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

		String tbl = "";
		String col = "";
		String rep = "";
		// 商品コード
		if (outobj.equals(DefineReport.Select.SHUNO.getObj())) {
			tbl="INAMS.MSTSHN";
			col="SHUNO";
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
}
