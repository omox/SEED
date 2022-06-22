package dao;

import java.util.ArrayList;
import java.util.List;

import authentication.bean.User;
import common.DefineReport;
import common.JsonArrayData;
import net.sf.json.JSONObject;

/**
 *
 */
public class Reportx021Dao extends ItemDao {

	private static String DECI_DIGITS = ",20,10";

	/**
	 * インスタンスを生成します。
	 * @param source
	 */
	public Reportx021Dao(String JNDIname) {
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

		// タイトル情報(任意)設定
		List<String> titleList = new ArrayList<String>();

		// 一覧表情報
		StringBuffer sbSQL = new StringBuffer();

		sbSQL.append(" select");
		sbSQL.append(" right('0'|| BMN.BMNCD,2)");												// F1	：部門
		sbSQL.append(", BMN.BMNKBN");															// F2	：部門区分
		sbSQL.append(", BMN.BMNKN");															// F4	：部門名称（漢字）
		sbSQL.append(", BMN.BMNAN");															// F3	：部門名称（カナ）
		sbSQL.append(", BMN.ZEIKBN");															// F5	：税区分
		sbSQL.append(", TO_CHAR(TO_DATE(BMN.ZEIRTHENKODT, 'yyyymmdd'), 'yy/mm/dd')");			// F6	：税率変更日
		sbSQL.append(", BMN.ZEIRTKBN_OLD");														// F7	：旧税率区分
		sbSQL.append(", BMN.ZEIRTKBN");															// F8	：税率区分
		sbSQL.append(", BMN.ODBOOKKBN");														// F9	：オーダーブック出力区分
		sbSQL.append(", BMN.HYOKAKBN");															// F10	：評価方法区分
		sbSQL.append(", BMN.GENKART");															// F11	：原価率
		sbSQL.append(", right ('000' ||BMN.CORPBMNCD, 3)");										// F12	：会社部門
		sbSQL.append(", right ('000' ||BMN.URIBMNCD, 3)");										// F13	：売上計上部門
		sbSQL.append(", right ('000' ||BMN.HOGANBMNCD, 3)");									// F14	：包含部門
		sbSQL.append(", BMN.JYOGENAM");															// F15	：上限金額
		sbSQL.append(", BMN.JYOGENSU");															// F16	：上限数量
		sbSQL.append(", BMN.TANAOROTIMKB");														// F17	：棚卸タイミング
		sbSQL.append(", BMN.POSBAIHENKBN");														// F18	：POS売変対象区分
		sbSQL.append(", BMN.KEIHIKBN");															// F19	：経費対象区分
		sbSQL.append(", BMN.TANPINKBN");														// F20	：単品管理区分
		sbSQL.append(", BMN.DELKIJYUNSU");														// F21	：削除基準区分
		sbSQL.append(", BMN.WARIGAIFLG");														// F22	：値引除外フラグ
		sbSQL.append(", BMN.ITEMBETSUCD");														// F23	：商品・非商品識別コード
		sbSQL.append(", BMN.URISEIGENFLG");														// F24	：販売制限フラグ
		sbSQL.append(", BMN.URIKETAKBN");														// F25	：売上金額最大桁数
		sbSQL.append(" from INAMS.MSTBMN BMN");
		sbSQL.append(" where NVL(BMN.UPDKBN, 0) <> 1");
		sbSQL.append(" order by BMN.BMNCD");

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
}
