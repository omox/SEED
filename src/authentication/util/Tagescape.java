/*******************************************************************************
 * Copyright (c) 2001, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * 作成日: 2006/10/26
 */
package authentication.util;

/**
 * タグ文字を別の文字に変換するクラス
 * システムに影響が出る可能性を持つタグを強制的に置換・又は変換する
 */
public class Tagescape {
/**
	 * HTMLタグを無効化する <br />
	 * ４つの特殊文字 & > < " をHTMLで表現する際のコードに変換する。 <br />
	 *
	 * @param strVal HTMLを含む文字 <br />
	 * @return 無効化後の文字 <br />
	 */
	public String htmlEscape(String strVal) {
		StringBuffer strResult = new StringBuffer();
		for (int i = 0; i < strVal.length(); i++) {
			switch (strVal.charAt(i)) {
			case '&':
				strResult.append("&amp;");
				break;
			case '<':
				strResult.append("&lt;");
				break;
			case '>':
				strResult.append("&gt;");
				break;
			case '"':
				strResult.append("&quot;");
				break;
			default:
				strResult.append(strVal.charAt(i));
				break;
			}
		}
		return strResult.toString();
	}
}
