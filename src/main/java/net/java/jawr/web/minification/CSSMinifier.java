/**
 * Copyright 2007 Jordi Hernández Sellés
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package net.java.jawr.web.minification;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Minifies CSS files by removing expendable whitespace and comments. 
 * 
 * @author Jordi Hernández Sellés
 *
 */
public class CSSMinifier {
	
	// This regex captures comments, plus newlines and tabs
	private static final String commentRegex ="(/\\*[^*]*\\*+([^/][^*]*\\*+)*/)|\\r|\\n|\\t|\\f";
	
	// This regex captures, in order: single or double quote enclosed content, 
	// brackets, parentheses,colons and semicolons and any spaces around them, and occurrences of one or more spaces. 
	private static final String spacesRegex = "('.*')|(\".*\")|(\\s*\\{\\s*)|(\\s*\\}\\s*)|(\\s*\\(\\s*)|(\\s*\\)\\s*)|(\\s*;\\s*)|(\\s*:\\s*)|( +)";
	
	private static final Pattern cssPattern = Pattern.compile(commentRegex, Pattern.DOTALL);
	private static final Pattern spacesPattern = Pattern.compile(spacesRegex, Pattern.DOTALL);
	
	private static final String SPACE = " ";
	private static final String QUOTE = "'";
	private static final String DQUOTE = "\"";
	private static final String BRACKET_OPEN = "{";
	private static final String BRACKET_CLOSE = "}";
	private static final String PAREN_OPEN = "(";
	private static final String PAREN_CLOSE = ")";

	private static final String COLON = ":";
	private static final String SEMICOLON = ";";

	/**
	 * @param data CSS to minify
	 * @return StringBuffer Minified CSS. 
	 */
	public StringBuffer minifyCSS(StringBuffer data) {
		// Remove comments and carriage returns
		String noComments = cssPattern.matcher(data.toString()).replaceAll("");
		
		// Match all empty space we can minify 
		Matcher matcher = spacesPattern.matcher(noComments);
		StringBuffer sb = new StringBuffer();
		while(matcher.find()) {
			String replacement = SPACE;
			String match = matcher.group();
			
			// No replacement for strings
			if(match.indexOf(QUOTE) != -1 || match.indexOf(DQUOTE) != -1)
				replacement = match;
			else if(match.indexOf(BRACKET_OPEN) != -1)
				replacement = BRACKET_OPEN;
			else if(match.indexOf(BRACKET_CLOSE) != -1)
				replacement = BRACKET_CLOSE;
			else if(match.indexOf(PAREN_OPEN) != -1)
				replacement = PAREN_OPEN;
			else if(match.indexOf(PAREN_CLOSE) != -1)
				replacement = PAREN_CLOSE;
			else if(match.indexOf(COLON) != -1)
				replacement = COLON;
			else if(match.indexOf(SEMICOLON) != -1)
				replacement = SEMICOLON;
			
			matcher.appendReplacement(sb, replacement);
		}
		matcher.appendTail(sb);
		return sb;
	}

}
