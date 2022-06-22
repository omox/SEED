package common;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import net.sf.json.JSONArray;


public class RowsLengthLimitUtility {

	public static int[] defaultValues = { 1, 0 };

	/** 各レポートの検索結果の上限を取得
	 * @param report
	 * @return int
	 * @throws Exception
	 */
	public int searchResultLimit(String dispID) {

		int limit = 0;			// 検索上限値

		ItemList			iL		 = new ItemList();
		ArrayList<String> paramData	 = new ArrayList<String>();

		if(StringUtils.isEmpty(dispID)){
			return 0;
		}

		StringBuffer sbSQL = new StringBuffer();
		sbSQL.append(" select MAX_SPREAD as value from INAAD.SYSDEFAULT where DISP_ID = ? ");
		paramData.add(dispID);

		@SuppressWarnings("static-access")
		JSONArray array = iL.selectJSONArray(sbSQL.toString(), paramData, Defines.STR_JNDI_DS);
		if(array.size() > 0 && NumberUtils.toInt(array.getJSONObject(0).optString("VALUE")) > 0){
			limit = array.getJSONObject(0).optInt("VALUE");
		}
		return limit;
	}


	/** 各レポートのページ番号からDispIDを取得
	 * @param report
	 * @return int
	 * @throws Exception
	 */
	public String getPageDispID(String report, HashMap<String,String> map) {

		if(StringUtils.isEmpty(report)){
			return "";
		}

		String dispID = "";		// 画面ID

		if(StringUtils.startsWith(StringUtils.replace(report, "Out_Report", ""), "x") ){
			if(StringUtils.equals(DefineReport.ID_PAGE_X001, report)){
				dispID = "IT001";

			}else if(StringUtils.equals(DefineReport.ID_PAGE_X021, report)){
				dispID = "";

			}else if(StringUtils.equals(DefineReport.ID_PAGE_X041, report)){
				dispID = "LT001";

			}else if(StringUtils.equals(DefineReport.ID_PAGE_X111, report)){
				dispID = "TP001";

			}else if(StringUtils.equals(DefineReport.ID_PAGE_X112, report)){
				dispID = "TP005";

			}else if(StringUtils.equals(DefineReport.ID_PAGE_X151, report)){
				dispID = "SI001";

			}else if(StringUtils.equals(DefineReport.ID_PAGE_X161, report)){
				dispID = "SI021";

			}else if(StringUtils.equals(DefineReport.ID_PAGE_X161, report)){
				//dispID = "SI021";
				dispID = "";

			}else if(StringUtils.equals(DefineReport.ID_PAGE_X201, report)){
				dispID = "SI031";

			}else if(StringUtils.equals(DefineReport.ID_PAGE_X203, report)){
				dispID = "SI034";

			}else if(StringUtils.equals(DefineReport.ID_PAGE_X171, report)){
				dispID = "HP010";

			}else if(StringUtils.equals(DefineReport.ID_PAGE_X151, report)){
				dispID = "ME003";

			}else if(StringUtils.equals(DefineReport.ID_PAGE_X001, report)){
				dispID = "IT001";

			}else if(StringUtils.equals(DefineReport.ID_PAGE_X005, report)){
				dispID = "IT025";

			}else if(StringUtils.equals(DefineReport.ID_PAGE_X191, report)){
				dispID = "IT021";

			}else if(StringUtils.equals(DefineReport.ID_PAGE_X212, report)){
				dispID = "PC008";

			}else if(StringUtils.equals(DefineReport.ID_PAGE_X215, report)){
				dispID = "PC004";

			}else if(StringUtils.equals(DefineReport.ID_PAGE_X217, report)){
				dispID = "PC006";

			}else if(StringUtils.equals(DefineReport.ID_PAGE_X218, report)){
				dispID = "PC007";

			}else if(StringUtils.equals(DefineReport.ID_PAGE_X261, report) || StringUtils.equals(DefineReport.ID_PAGE_X244, report)){
				dispID = "IT001";
			}

		}else{
			dispID = StringUtils.replace(report, "Out_Report", "");

		}
		return dispID;
	}
}
