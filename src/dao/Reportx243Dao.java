package dao;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import authentication.bean.User;
import common.CmnDate;
import common.DefineReport;
import common.JsonArrayData;
import net.sf.json.JSONObject;

/**
 *
 */
public class Reportx243Dao extends ItemDao {

	/** SQLリスト保持用変数 */
	ArrayList<String> sqlList = new ArrayList<String>();
	/** SQLのパラメータリスト保持用変数 */
	ArrayList<ArrayList<String>> prmList = new ArrayList<ArrayList<String>>();
	/** SQLログ用のラベルリスト保持用変数 */
	ArrayList<String> lblList = new ArrayList<String>();

	/**
	 * インスタンスを生成します。
	 *
	 * @param source
	 */
	public Reportx243Dao(String JNDIname) {
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
		if (userInfo == null) {
			return "";
		}

		String szId = getMap().get("ID"); // ID
		String szName = getMap().get("NAME"); // NAME
		String szTen = getMap().get("TEN"); // TEN
		String szFromDate = getMap().get("FROM_DATE"); // 開始日
		String szToDate = getMap().get("TO_DATE"); // 終了日
		String szSelDisp = getMap().get("SELDISP");

		// 配送グループ名称（漢字）に入力があった場合部分一致検索を行う
		// DB検索用パラメータ
		String sqlWhere = "";
		ArrayList<String> paramData = new ArrayList<String>();

		// タイトル情報(任意)設定
		List<String> titleList = new ArrayList<String>();
		// 一覧表情報
		StringBuffer sbSQL = new StringBuffer();

		sbSQL.append(" with WKUSER as ( ");
		sbSQL.append(" SELECT T1.* ");
		sbSQL.append(" , T2.TENCD ");
		sbSQL.append(" , T2.TENKN ");
		sbSQL.append(" FROM ( ");
		sbSQL.append(" select ");
		sbSQL.append(" T1.USER_ID ");
		sbSQL.append(" , LPAD('',LENGTH(T1.PASSWORDS),'*') AS PASSWORDS ");
		sbSQL.append(" , T1.NM_FAMILY || T1.NM_NAME AS USERNM ");
		sbSQL.append(" , DATE_FORMAT( ");
		sbSQL.append(" DATE_FORMAT(T1.DT_PW_TERM, '%Y%m%d') ");
		sbSQL.append(" , '%Y/%m/%d' ");
		sbSQL.append(" ) as DT_PW_TERM ");
		sbSQL.append(" , case ");
		sbSQL.append(" when T1.YOBI_2 is null ");
		sbSQL.append(" or T1.YOBI_2 = '' ");
		sbSQL.append(" then null ");
		sbSQL.append(" else cast(T1.YOBI_2 as SIGNED) ");
		sbSQL.append(" end YOBI_2 ");
		sbSQL.append(" , T1.YOBI_6 ");
		sbSQL.append(" , T1.YOBI_7 ");
		sbSQL.append(" , T1.YOBI_8 ");
		sbSQL.append(" , T1.NM_UPDATE || '　' || T2.NM_FAMILY || T2.NM_NAME AS UPDUSER ");
		sbSQL.append(" , DATE_FORMAT(T1.DT_UPDATE ,'%Y/%m/%d %d:%i') AS DT_UPDATE ");
		sbSQL.append(" , DATE_FORMAT(T1.DT_UPDATE ,'%Y%m%d') AS DT_UPDATE_CK ");
		sbSQL.append(" , T1.CD_USER ");
		sbSQL.append(" , T1.INF_TABLEKBN ");
		sbSQL.append(" from KEYSYS.SYS_USERS_JNL T1 ");
		sbSQL.append(" left join KEYSYS.SYS_USERS T2 ");
		sbSQL.append(" on T1.NM_UPDATE = T2.USER_ID ) T1 ");
		sbSQL.append(" left join INAMS.MSTTEN T2 ");
		sbSQL.append(" on T1.YOBI_2 = T2.TENCD ");

		// ユーザーID
		if (!StringUtils.isEmpty(szId)) {
			sqlWhere = " where T1.USER_ID like ? ";
			paramData.add("%" + szId + "%");
		}

		// 姓名
		if (!StringUtils.isEmpty(szName)) {
			if (!StringUtils.isEmpty(sqlWhere)) {
				sqlWhere += " and T1.USERNM like ? ";
			} else {
				sqlWhere += " where T1.USERNM like ? ";
			}
			paramData.add("%" + szName + "%");
		}

		// 店コード
		if (!StringUtils.isEmpty(szTen) && !szTen.equals("-1")) {
			if (!StringUtils.isEmpty(sqlWhere)) {
				sqlWhere += " and T2.TENCD =? ";
			} else {
				sqlWhere += " where T2.TENCD =? ";
			}
			paramData.add(szTen);
		}

		// 期間
		if (!StringUtils.isEmpty(szFromDate)) {
			String convdt = CmnDate.getConvInpDate(szFromDate);
			if (!StringUtils.isEmpty(sqlWhere)) {
				sqlWhere += " and T1.DT_UPDATE_CK >= '" + convdt + "'";
			} else {
				sqlWhere += " where T1.DT_UPDATE_CK >= '" + convdt + "'";
			}
		}
		if (!StringUtils.isEmpty(szToDate)) {
			String convdt = CmnDate.getConvInpDate(szToDate);
			if (!StringUtils.isEmpty(sqlWhere)) {
				sqlWhere += " and T1.DT_UPDATE_CK <= '" + convdt + "'";
			} else {
				sqlWhere += " where T1.DT_UPDATE_CK <= '" + convdt + "'";
			}
		}

		// 表示する・しない
		if (!(!StringUtils.isEmpty(szSelDisp) && szSelDisp.equals("1"))) {
			// 表示しない
			if (StringUtils.isEmpty(sqlWhere)) {
				sqlWhere += " where T1.INF_TABLEKBN > ?";
			} else {
				sqlWhere += " and T1.INF_TABLEKBN > ?";
			}
			paramData.add("0");
		}

		sbSQL.append(sqlWhere + " ) ");
		sbSQL.append(" , WKMS as ( ");
		sbSQL.append(" SELECT ");
		sbSQL.append(" CD_USER AS CD_USER_WK ");
		sbSQL.append(" FROM ");
		sbSQL.append(" KEYSYS.SYS_USER_POS_JNL ");
		sbSQL.append(" WHERE ");
		sbSQL.append(" CD_POSITION = 24321) ");
		sbSQL.append(" , WKTK as ( ");
		sbSQL.append(" SELECT ");
		sbSQL.append(" CD_USER AS CD_USER_WK ");
		sbSQL.append(" FROM ");
		sbSQL.append(" KEYSYS.SYS_USER_POS_JNL ");
		sbSQL.append(" WHERE");
		sbSQL.append(" CD_POSITION = 24320) ");
		sbSQL.append(" , WKTN as ( ");
		sbSQL.append(" SELECT ");
		sbSQL.append(" CD_USER AS CD_USER_WK ");
		sbSQL.append(" FROM ");
		sbSQL.append(" KEYSYS.SYS_USER_POS_JNL ");
		sbSQL.append(" WHERE ");
		sbSQL.append(" CD_POSITION = 24322) ");
		sbSQL.append(" SELECT ");
		sbSQL.append(" USER_ID "); // F1:ユーザーID
		sbSQL.append(" , PASSWORDS "); // F2:パスワード
		sbSQL.append(" , USERNM "); // F3:姓名
		sbSQL.append(" , case "); // F4:所属
		sbSQL.append(" when TENCD is null ");
		sbSQL.append(" then '本部' ");
		sbSQL.append(" else YOBI_2 || '　' || TENKN ");
		sbSQL.append(" end ");
		sbSQL.append(" , DT_PW_TERM "); // F5:有効期限
		sbSQL.append(" , case "); // F6:本部マスタ
		sbSQL.append(" when NOT EXISTS ( ");
		sbSQL.append(" SELECT ");
		sbSQL.append(" 1 ");
		sbSQL.append(" FROM ");
		sbSQL.append(" WKMS T1 ");
		sbSQL.append(" WHERE ");
		sbSQL.append(" CD_USER = T1.CD_USER_WK) ");
		sbSQL.append(" then '参照不可' ");
		sbSQL.append(" when YOBI_6 = '1' ");
		sbSQL.append(" and YOBI_7 = '1' ");
		sbSQL.append(" then '登録・削除可' ");
		sbSQL.append(" when YOBI_6 = '1' ");
		sbSQL.append(" then '登録可' ");
		sbSQL.append(" else '参照可' end ");
		sbSQL.append(" , case "); // F7:本部特売
		sbSQL.append(" when NOT EXISTS ( ");
		sbSQL.append(" SELECT ");
		sbSQL.append(" 1 ");
		sbSQL.append(" FROM ");
		sbSQL.append(" WKTK T1 ");
		sbSQL.append(" WHERE ");
		sbSQL.append(" CD_USER = T1.CD_USER_WK) ");
		sbSQL.append(" then '参照不可' ");
		sbSQL.append(" when YOBI_8 = '1' ");
		sbSQL.append(" then '登録・削除可' ");
		sbSQL.append(" else '参照可' end ");
		sbSQL.append(" , case "); // F8:店舗画面
		sbSQL.append(" when NOT EXISTS ( ");
		sbSQL.append(" SELECT ");
		sbSQL.append(" 1 ");
		sbSQL.append(" FROM ");
		sbSQL.append(" WKTN T1 ");
		sbSQL.append(" WHERE ");
		sbSQL.append(" CD_USER = T1.CD_USER_WK) ");
		sbSQL.append(" then '参照不可' ");
		sbSQL.append(" else '登録・削除可' end ");
		sbSQL.append(" , UPDUSER "); // F9:更新ユーザー
		sbSQL.append(" , DT_UPDATE "); // F10:更新日
		sbSQL.append(" , CD_USER "); // F11:ユーザーコード(hidden)
		sbSQL.append(" FROM WKUSER ORDER BY USER_ID ASC, DT_UPDATE DESC");

		// オプション情報（タイトル）設定
		JSONObject option = new JSONObject();
		option.put(DefineReport.ID_PARAM_OPT_TITLE, titleList.toArray(new String[titleList.size()]));
		setOption(option);

		// DB検索用パラメータ設定
		setParamData(paramData);

		if (DefineReport.ID_DEBUG_MODE)	System.out.println(getClass().getSimpleName() + "[sql]" + sbSQL.toString());
		return sbSQL.toString();
	}

	private void outputQueryList() {

		// 検索条件の加工クラス作成
		JsonArrayData jad = new JsonArrayData();
		jad.setJsonString(getJson());

		// 保存用 List (検索情報)作成
		setWhere(new ArrayList<List<String>>());

		// 共通箇所設定
		createCmnOutput(jad);
	}
}
