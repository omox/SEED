/**
 *
 */
package common;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * @author Eatone
 *
 */
public class FileList {



	/**
	 * CSVファイル読込
	 *
	 * @param session
	 * @param filePath
	 * @return
	 * @throws Exception
	 */
	public ArrayList<Object[]> readCsv(HttpSession session, String filePath) throws Exception {

		// ファイル読み込み
		InputStream is = new FileInputStream(filePath);
		InputStreamReader osr  = new InputStreamReader(is, "MS932");		// 文字化け対応
		BufferedReader br = new BufferedReader(osr);

		ArrayList<Object[]> rtn = new ArrayList<Object[]>();

		String line;
		// 1行ずつCSVファイルを読み込む
		while ((line = br.readLine()) != null) {
			line = convertToProhibited(line,",");
			rtn.add(line.split(",", -1));
		}
		br.close();

		if(DefineReport.ID_DEBUG_MODE) {
			// CSVから読み込んだ配列の中身を表示
			for (int r = 0, lRow = rtn.size(); r < lRow; r++) {
				for (int c = 0, lCol = rtn.get(r).length; c < lCol; c++) {
					System.out.println("Row=" + r + ", Column=" + c + ", value=" + rtn.get(r)[c]);
				}
			}
		}
		return rtn;
	}
	/**
	 * TXTファイル読込
	 *
	 * @param session
	 * @param filePath
	 * @return
	 * @throws Exception
	 */
	public ArrayList<Object[]> readTxt(HttpSession session, String filePath) throws Exception {

		// ファイル読み込み
		InputStream is = new FileInputStream(filePath);
		InputStreamReader osr  = new InputStreamReader(is, "MS932");		// 文字化け対応
		BufferedReader br = new BufferedReader(osr);

		ArrayList<Object[]> rtn = new ArrayList<Object[]>();

		String line;
		// 1行ずつCSVファイルを読み込む
		while ((line = br.readLine()) != null) {
			line = convertToProhibited(line,"\t");
			rtn.add(line.split("\t", -1));
		}
		br.close();

		if(DefineReport.ID_DEBUG_MODE) {
			// CSVから読み込んだ配列の中身を表示
			for (int r = 0, lRow = rtn.size(); r < lRow; r++) {
				for (int c = 0, lCol = rtn.get(r).length; c < lCol; c++) {
					System.out.println("Row=" + r + ", Column=" + c + ", value=" + rtn.get(r)[c]);
				}
			}
		}
		return rtn;
	}


	/**
	 * Excel読込
	 *
	 * @param session
	 * @param filePath
	 * @return
	 * @throws Exception
	 */
	public ArrayList<Object[]> readExcel(HttpSession session, String filePath) throws Exception {
		// xlsx形式ファイル時
		if(StringUtils.endsWith(filePath.toLowerCase(), ".xlsx")){
			return readExcel2007(session, filePath);
		}

		// Excel ワークブック読み込み
		InputStream is = new FileInputStream(filePath);
		HSSFWorkbook wb = new HSSFWorkbook(is);
		HSSFSheet sheet = wb.getSheetAt(0);

		ArrayList<Object[]> rtn = new ArrayList<Object[]>();

		Row row = null;
		Cell cell = null;
		for (int r = 0, lRow = sheet.getLastRowNum(); r <= lRow; r++) {
			Object[] vals = new Object[]{};
			row = sheet.getRow(r);
			if(row == null){
				rtn.add(vals);
				if(DefineReport.ID_DEBUG_MODE) System.out.println("Row=" + r + ", Column=0, Type=?, value=null");
				continue;
			}
			for (int c = 0, lCol = row.getLastCellNum(); c < lCol; c++) {
				String value = "";
				cell = row.getCell(c);
				if(cell != null){
					int cellType = cell.getCellType();
					// セルのデータ型が数式の場合、結果をセットしてデータ型を判断する
					if(cellType == HSSFCell.CELL_TYPE_FORMULA){
						try{
							cellType = cell.getCachedFormulaResultType();
						}catch (Exception e) {}
					}

					// セルのデータ型を調べます。
					switch (cellType) {
					case HSSFCell.CELL_TYPE_NUMERIC:
						// 数値型
						value = String.valueOf(cell.getNumericCellValue()).replaceAll("\\.0$", "");
						break;
					case HSSFCell.CELL_TYPE_STRING:
						// 文字列型
						value = cell.getStringCellValue();
						break;
					case HSSFCell.CELL_TYPE_FORMULA:
						// 数式型
						value = cell.getCellFormula();
						break;
					case HSSFCell.CELL_TYPE_BLANK:
						// 空。データなし
						break;
					case HSSFCell.CELL_TYPE_BOOLEAN:
						// 真偽値
						value = String.valueOf(cell.getBooleanCellValue());
						break;
					case HSSFCell.CELL_TYPE_ERROR:
						// エラー値
						break;
					}
					value = StringUtils.replace(value, "\n", "");
					if(DefineReport.ID_DEBUG_MODE) System.out.println("Row=" + r + ", Column=" + c + ", Type=" + cellType + ", value=" + value);
				}
				vals = ArrayUtils.add(vals, value);
			}
			rtn.add(vals);
		}
		return rtn;
	}

	/**
	 * Excel読込
	 *
	 * @param session
	 * @param filePath
	 * @return
	 * @throws Exception
	 */
	public ArrayList<Object[]> readExcel2007(HttpSession session, String filePath) throws Exception {

		// Excel ワークブック読み込み
		InputStream is = new FileInputStream(filePath);
		XSSFWorkbook wb = new XSSFWorkbook(is);
		XSSFSheet sheet = wb.getSheetAt(0);

		ArrayList<Object[]> rtn = new ArrayList<Object[]>();

		Row row = null;
		Cell cell = null;
		for (int r = 0, lRow = sheet.getLastRowNum(); r <= lRow; r++) {
			Object[] vals = new Object[]{};
			row = sheet.getRow(r);
			if(row == null){
				rtn.add(vals);
				if(DefineReport.ID_DEBUG_MODE) System.out.println("Row=" + r + ", Column=0, Type=?, value=null");
				continue;
			}
			for (int c = 0, lCol = row.getLastCellNum(); c < lCol; c++) {
				String value = "";
				cell = row.getCell(c);
				if(cell != null){
					int cellType = cell.getCellType();
					// セルのデータ型が数式の場合、結果をセットしてデータ型を判断する
					if(cellType == XSSFCell.CELL_TYPE_FORMULA){
						try{
							cellType = cell.getCachedFormulaResultType();
						}catch (Exception e) {}
					}

					// セルのデータ型を調べます。
					switch (cellType) {
					case XSSFCell.CELL_TYPE_NUMERIC:
						// 数値型
						value = String.valueOf(cell.getNumericCellValue()).replaceAll("\\.0$", "");
						break;
					case XSSFCell.CELL_TYPE_STRING:
						// 文字列型
						value = cell.getStringCellValue();
						break;
					case XSSFCell.CELL_TYPE_FORMULA:
						// 数式型
						value = cell.getCellFormula();
						break;
					case XSSFCell.CELL_TYPE_BLANK:
						// 空。データなし
						break;
					case XSSFCell.CELL_TYPE_BOOLEAN:
						// 真偽値
						value = String.valueOf(cell.getBooleanCellValue());
						break;
					case XSSFCell.CELL_TYPE_ERROR:
						// エラー値
						break;
					}
					value = StringUtils.replace(value, "\n", "");
					if(DefineReport.ID_DEBUG_MODE) System.out.println("Row=" + r + ", Column=" + c + ", Type=" + cellType + ", value=" + value);
				}
				vals = ArrayUtils.add(vals, value);
			}
			rtn.add(vals);
		}
		return rtn;
	}

	/** Excelデータ分解
	 * @param json
	 * @return boolean
	 * @throws Exception
	 */
	public String[][] parseExcelData(JSONObject jo){
		String[] rows = StringUtils.splitPreserveAllTokens(jo.optString("data"),"\n");

		String[][] data = new String[rows.length][];
		for (int r=0, lRow = rows.length; r < lRow; r++) {
			data[r] = StringUtils.splitPreserveAllTokens(StringUtils.removeEnd(rows[r],"\t"),"\t");
		}
		return data;
	}

	/** アルファベットの文字数 */
	private static final int ALPHABET_SIZE = 26;

	/** アルファベット変換後の最大桁数 */
	private static final int MAX_SIZE = 10;

	/** アルファベット文字セット */
	private static final char alphabet[] = { 'A', 'B', 'C', 'D', 'E', 'F', 'G','H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T','U', 'V', 'W', 'X', 'Y', 'Z'};

	/** 禁止文字一覧 */
	HashMap<String, JSONObject> dbDbProhibitedList = initDbProhibitedListData();

	/** 列数→Excel列名変換<br>
	 * 例）0=A,26=Z,27=AA
	 * @param number
	 * @return Excel列名
	 * @throws IllegalArgumentException
	 */
	public static String convertToExcelAlphabet(int number) throws IllegalArgumentException{
		// マイナスの数値の場合にはエラー
		if (number < 0) {
			throw new IllegalArgumentException("The parameter is a negative number. you cannot specify a negative number.");
		}

		char convertedAlphabet[] = new char[MAX_SIZE]; //アルファベット変換後の文字列格納用配列
		int startPos = MAX_SIZE - 1; //アルファベットの1桁目にあたる配列のインデックス
		int pos = startPos;

		//以降、1桁目から順に数値をアルファベットに変換する。
		do {
			//1桁目の場合
			if (pos == startPos) {
				convertedAlphabet[pos--] = alphabet[number % ALPHABET_SIZE];

			//2桁目以降の場合
			}else{
				//26の倍数の場合は特殊。その桁には「Z」を設定する。
				if(number % ALPHABET_SIZE == 0 && number != 0){
						convertedAlphabet[pos--] = alphabet[ALPHABET_SIZE - 1];
						number -= ALPHABET_SIZE; //26を引いて、この桁の分の数値を減算する。
				}else{
						//その桁の数値を、アルファベットに変換する。
						convertedAlphabet[pos--] = alphabet[(number - 1) % ALPHABET_SIZE];

				}
			}

			//一番大きな桁だった場合、変換処理を終了する。
			if(number < ALPHABET_SIZE){
					break;
			}

			//数値を26で割り、次の桁の解析に進む。
			number /= ALPHABET_SIZE;
		} while (true);

		//配列からアルファベットが入っている部分だけを抜き出し、Stringにして返却する。
		pos++; //インデックスを最後の文字に合わせる。
		return new String(convertedAlphabet, pos, MAX_SIZE - pos);
	}

	public String convertToProhibited(String str, String split) {
		for(HashMap.Entry<String, JSONObject> prohibited : dbDbProhibitedList.entrySet()) {

			String text = prohibited.getValue().getString("TEXT");
			String text2 = prohibited.getValue().getString("TEXT2");

			for (int i = 0; i < text.split("").length; i++) {
				if (StringUtils.isEmpty(split) || (!StringUtils.isEmpty(split) && !text.split("")[i].equals(split))) {
					str = str.replace(text.split("")[i], text2.split("")[i]);
				}
			}
		}
		return str;
	}

	/** 禁止文字一覧情報取得 */
	@SuppressWarnings("unchecked")
	public static HashMap<String, JSONObject> initDbProhibitedListData() {
		HashMap<String, JSONObject> dbDbProhibitedList = new HashMap<String, JSONObject>();
		ItemList iL = new ItemList();

		ArrayList<String> paramData = new ArrayList<String>();
		String command = DefineReport.ID_SQL_MEISYO_PROHIBITED;;
		@SuppressWarnings("static-access")
		JSONArray rows = iL.selectJSONArray(command, paramData, Defines.STR_JNDI_DS);
		for (int i=0; i<rows.size(); i++){

			JSONObject obj = rows.optJSONObject(i);

			try {
				String val = new String(obj.getString("TEXT").getBytes("Cp943C"), "MS932");
				obj.replace("TEXT", val);
				if (obj.containsKey("TEXT2")) {
					val = new String(obj.getString("TEXT2").getBytes("Cp943C"), "MS932");
					obj.replace("TEXT2", val);
				} else {
					obj.put("TEXT2", "");
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			dbDbProhibitedList.put(rows.optJSONObject(i).optString("MOJI"), obj);
		}
		return dbDbProhibitedList;
	}
}
