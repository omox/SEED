package common;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import dao.DBConnection;
import dto.JQEasyModel;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

public class JsonData {

	/** 検索＆JSON生成
	 * @param sqlCommand
	 * @param paramData
	 * @return JSON文字列
	 * @throws Exception
	 */
	public String selectJSON(String sqlCommand, ArrayList<String> paramData, String JNDIname) throws Exception {

		//System.out.println("JsonData.selectJSON:" + sqlCommand);
		String jsonString = "";

		// 検索
		Connection con = null; // コネクション
		try {
			// コネクションの取得
			con = DBConnection.getConnection(JNDIname);

			// 実行SQL設定
			PreparedStatement stmt = con.prepareStatement(sqlCommand);

			// 分類コードパラメータ判断
			for (int i=0; i < paramData.size(); i++) {
				stmt.setString((i+1), (String)paramData.get(i));
			}
			// SQL実行
			ResultSet rs = stmt.executeQuery();

			int total = 0;

			// カラム数
			ResultSetMetaData rsmd = rs.getMetaData();
			int sizeColumn = rsmd.getColumnCount();
			//System.out.println(sizeColumn);

			JSONArray json = new JSONArray();

			// レコード情報の格納先(JSONObject)作成
			JSONObject jsonOB = new JSONObject();

			// 項目単位の情報格納
			List<JSONObject> lineData = new ArrayList<JSONObject>();

			// 結果の取得
			while (rs.next()) {
				total++;

				JSONObject obj = new JSONObject();

				for ( int i=1; i <= sizeColumn; i++ ) {
					obj.put ( rsmd.getColumnLabel(i), rs.getString(i) );
				}

				// 行データ格納
				json.add(obj);

				// 行情報へセル情報を追加
				lineData.add(obj);

			}
			// jqEasy 用 JSON モデル作成
			JQEasyModel jsonModel = new JQEasyModel();
			jsonModel.setRows(lineData);
			jsonModel.setTotal(total);
			jsonOB = JSONObject.fromObject(JSONSerializer.toJSON(jsonModel));

			jsonString = jsonOB.toString();

			con.commit();
			con.close();

		} catch (Exception e) {
			/* 接続解除 */
			if (con != null){
				try {
					con.rollback();
					con.close();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}
			e.printStackTrace();
		}

		return jsonString;
	}

}
