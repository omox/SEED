package dao;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import authentication.bean.User;
import common.DefineReport;
import common.JsonArrayData;
import net.sf.json.JSONObject;

/**
 *
 */
public class ReportTR004Dao extends ItemDao {

	/**
	 * インスタンスを生成します。
	 * @param source
	 */
	public ReportTR004Dao(String JNDIname) {
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

	private String createCommand() {
		// ログインユーザー情報取得
		User userInfo = getUserInfo();
		if(userInfo==null){
			return "";
		}

		String szBumon	= getMap().get("BUMON");	// 部門
		String szDaibun	= getMap().get("DAIBUN");	// 大分類
		String szChubun	= getMap().get("CHUBUN");	// 中分類
		String szShncd	= getMap().get("SHNCD");	// 商品コード

		// 配送グループ名称（漢字）に入力があった場合部分一致検索を行う
		// DB検索用パラメータ
		String sqlWith = "";
		ArrayList<String> paramData = new ArrayList<String>();

		sqlWith = "WITH MST AS(SELECT SHNCD,SHNKN FROM INAMS.MSTSHN WHERE ";
		// 商品コードが未入力の場合は部門で検索
		if(StringUtils.isEmpty(szShncd)){
			if(StringUtils.isEmpty(szBumon) || szBumon.equals("-1")){
				sqlWith += "BMNCD=null AND ";
			}else{
				sqlWith += "SUBSTR(SHNCD,1,2)=? AND BMNCD=? AND ";
				paramData.add(szBumon);
				paramData.add(szBumon);
				//paramData.add(String.valueOf(Integer.valueOf(szBumon.substring(0, 2))));
			}

			if(StringUtils.isEmpty(szDaibun) || szDaibun.equals("-1")){
				sqlWith += "DAICD=null ";
			}else{
				sqlWith += "DAICD=? ";
				paramData.add(szDaibun);
				//paramData.add(String.valueOf(Integer.valueOf(szDaibun.substring(2, 4))));
			}

			if(StringUtils.isEmpty(szChubun) || szChubun.equals("-1")){
				sqlWith += "";
			}else{
				sqlWith += " AND CHUCD=? ";
				//paramData.add(String.valueOf(Integer.valueOf(szChubun.substring(4, 6))));
				paramData.add(szChubun);
			}
		}else{

			sqlWith += "SHNCD=? ";
			paramData.add(szShncd);
		}
		sqlWith += ")";

		// タイトル情報(任意)設定
		List<String> titleList = new ArrayList<String>();

		// 一覧表情報
		StringBuffer sbSQL = new StringBuffer();
		sbSQL.append(sqlWith);
		sbSQL.append("SELECT  ");
		sbSQL.append("TK.SHNCD ");
		sbSQL.append(",M1.SHNKN ");
		sbSQL.append(",TK.BINKBN ");
		sbSQL.append("FROM INATK.HATSTR_SHN TK LEFT JOIN MST AS M1 ON TK.SHNCD=M1.SHNCD, MST AS M2 ");
		sbSQL.append("WHERE TK.SHNCD=M2.SHNCD ");
		sbSQL.append(" AND TK.UPDKBN="+DefineReport.ValUpdkbn.NML.getVal());
		sbSQL.append(" ORDER BY TK.SHNCD,TK.BINKBN");

		// オプション情報（タイトル）設定
		JSONObject option = new JSONObject();
		option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
		setOption(option);

		// DB検索用パラメータ設定
		setParamData(paramData);

		if (DefineReport.ID_DEBUG_MODE) System.out.println(getClass().getSimpleName()+"[sql]"+sbSQL.toString());
		return sbSQL.toString();
	}

	private void outputQueryList() {

		// 検索条件の加工クラス作成
		JsonArrayData jad = new JsonArrayData();
		jad.setJsonString(getJson());

	}
}
