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
import authentication.defines.Consts;
import common.DefineReport;
import common.JsonArrayData;
import common.MessageUtility;
import common.MessageUtility.Msg;
import common.MessageUtility.MsgKey;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 */
public class ReportTJ014Dao extends ItemDao {

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
	public ReportTJ014Dao(String JNDIname) {
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
		String sysdate = (String)request.getSession().getAttribute(Consts.STR_SES_LOGINDT);
		// 更新情報チェック(基本JS側で制御)
		JSONObject msgObj = new JSONObject();
		JSONArray msg = this.check(map, userInfo, sysdate);

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
		ArrayList<String> paramData = new ArrayList<String>();

		// タイトル情報(任意)設定
		StringBuffer sbSQL = new StringBuffer();

		String szLstno			= getMap().get("LSTNO");				// リスト№
		String szBmncd			= getMap().get("BMNCD");				// 部門コード
		String tenpo 			= userInfo.getTenpo();

		if(StringUtils.equals("-1", szBmncd) || szBmncd == null){
			return "";
		}

		// タイトル情報(任意)設定
		List<String> titleList = new ArrayList<String>();

		sbSQL.append(" select");
		sbSQL.append(" COUNT(T1.LSTNO)");
		sbSQL.append(" from INATK.TOKTJ_WK_MNG T1");
		sbSQL.append(" left join INATK.TOKTJ_SHNDT_WK T2 on T2.LSTNO = T1.LSTNO and T2.TENCD = T1.TENCD and T2.BMNCD = T1.BMNCD");
		sbSQL.append(" where T2.LSTNO = ?");
		sbSQL.append(" and T2.TENCD = ?");
		sbSQL.append(" and T2.BMNCD = ? ");

		paramData.add(szLstno);
		paramData.add(tenpo);
		paramData.add(szBmncd);

		// オプション情報（タイトル）設定
		JSONObject option = new JSONObject();
		option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
		setOption(option);

		// DB検索用パラメータ設定
		setParamData(paramData);

		if (DefineReport.ID_DEBUG_MODE) System.out.println(getClass().getSimpleName()+"[sql]"+sbSQL.toString());
		return sbSQL.toString();
	}

	boolean isTest = true;

	private void outputQueryList() {

		// 検索条件の加工クラス作成
		JsonArrayData jad = new JsonArrayData();
		jad.setJsonString(getJson());

		// 保存用 List (検索情報)作成
		setWhere(new ArrayList<List<String>>());
		List<String> cells = new ArrayList<String>();

		// 期間系
		cells = new ArrayList<String>();
		cells.add( DefineReport.Select.KIKAN.getTxt() );
		cells.add( jad.getJSONText(DefineReport.Select.KIKAN_F.getObj()));
		getWhere().add(cells);

		// 店舗系
		cells = new ArrayList<String>();
		cells.add( DefineReport.Select.KIGYO.getTxt());
		cells.add( DefineReport.Select.TENPO.getTxt());
		cells.add( jad.getJSONText( DefineReport.Select.TENPO.getObj()) );
		getWhere().add(cells);

		// 分類系
		cells = new ArrayList<String>();
		cells.add( DefineReport.Select.BUMON.getTxt());
		cells.add( jad.getJSONText( DefineReport.Select.BUMON.getObj()) );
		getWhere().add(cells);

		// 共通箇所設定
		createCmnOutput(jad);
	}

	/**
	 * 削除処理実行
	 * @return
	 *
	 * @throws Exception
	 */
	private JSONObject deleteData(HashMap<String, String> map, User userInfo) throws Exception {
		JSONObject option = new JSONObject();

		// 更新情報
		ArrayList<String> prmData = new ArrayList<String>();

		String szLstno		= map.get("LSTNO");			// リスト№
		String szBmncd		= map.get("BMNCD");			// 部門コード
		String tenpoCd 		= userInfo.getTenpo();		// 店舗コード

		// 事前発注_発注明細wk管理
		if (StringUtils.isNotEmpty(szLstno)
				|| StringUtils.isNotEmpty(tenpoCd)
				|| StringUtils.isNotEmpty(szBmncd)) {

			prmData = new ArrayList<String>();
			prmData.add(szLstno);
			prmData.add(tenpoCd);
			prmData.add(szBmncd);

			StringBuffer sbSQL;
			sbSQL = new StringBuffer();
			sbSQL.append("delete from INATK.TOKTJ_WK_MNG where LSTNO = ? and TENCD = ? and BMNCD = ?");

			if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

			sqlList.add(sbSQL.toString());
			prmList.add(prmData);
			lblList.add("事前発注_発注明細wk管理");
		}

		// 事前発注_発注明細wk
		if (StringUtils.isNotEmpty(szLstno)
				|| StringUtils.isNotEmpty(tenpoCd)
				|| StringUtils.isNotEmpty(szBmncd)) {

			prmData = new ArrayList<String>();
			prmData.add(szLstno);
			prmData.add(tenpoCd);
			prmData.add(szBmncd);

			StringBuffer sbSQL;
			sbSQL = new StringBuffer();
			sbSQL.append("delete from INATK.TOKTJ_SHNDT_WK where LSTNO = ? and TENCD = ? and BMNCD = ?");

			if (DefineReport.ID_DEBUG_MODE) System.out.println(this.getClass().getName()+ ":" + sbSQL.toString());

			sqlList.add(sbSQL.toString());
			prmList.add(prmData);
			lblList.add("事前発注_発注明細wk");
		}

		// 登録処理
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
	 * チェック処理
	 *
	 * @throws Exception
	 */
	public JSONArray check(HashMap<String, String> map, User userInfo, String sysdate) {
		// パラメータ確認
		JSONArray dataArray = JSONArray.fromObject(map.get("DATA"));	// 更新情報

		String sendBtnid	= map.get("SENDBTNID");	// 呼出しボタン

		MessageUtility mu = new MessageUtility();
		// ①正 .新規
		boolean isNew = false;
		// ②正 .変更
		boolean isChange = true;

		JSONArray msgArray = new JSONArray();

		return msgArray;
	}
}
