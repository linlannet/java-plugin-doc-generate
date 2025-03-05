/**
 * Copyright 2015-2017 the original author or Linlan authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.linlan.doc.utils;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.*;

/**
 * String class to provide utils for use
 * Filename:StringUtils.java
 * Desc:String utils include isBlank, startsWith, trim methods
 * the utils is for commons group packages to use
 *
 * Createtime 2017/6/30 8:41 PM
 *
 * @version 1.0
 * @since 1.0
 *
 */
public final class StringUtils {

    public static final String EMPTY = "";

    public static final String NULL = "null";

    /**
     * Represents a failed index search.
     */
    public static final int INDEX_NOT_FOUND = -1;
    /**
     * back slash of char
     */
    public static final char C_BACKSLASH = '\\';
    /**
     * the max split limit int, use 16
     */
    public static final int MAX_SPLIT_LIMIT = 16;
    public static final String EMPTY_JSON = "{}";
    public static final char C_DELIM_START = '{';
    public static final char C_DELIM_END = '}';

    /**
     * constructor of self
     */
    private StringUtils() {
    }

    /**
     * if the string is blank, 1:null, 2:blank, 3:""<br>
     *
     * @param source source string
     * @return if blank true, else false
     */
    public static boolean isBlank(final CharSequence source) {
        int length;

        if ((source == null) || ((length = source.length()) == 0)) {
            return true;
        }

        if (source.equals(NULL)){
            return true;
        }
        for (int i = 0; i < length; i++) {
            // if has any char then not null
            if (false == isBlankChar(source.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    /**
     * if the source include space, whitespace, tab, nbsp, entire space<br>
     *
     * @see Character#isWhitespace(int)
     * @see Character#isSpaceChar(int)
     * @param source source char
     * @return true, false
     */
    public static boolean isBlankChar(char source) {
        return isBlankChar((int) source);
    }

    /**
     * if the source include space, whitespace, tab, nbsp, entire space<br>
     *
     * @see Character#isWhitespace(int)
     * @see Character#isSpaceChar(int)
     * @param source source char
     * @return true, false
     */
    public static boolean isBlankChar(int source) {
        return Character.isWhitespace(source) || Character.isSpaceChar(source);
    }


    /**
     * if the string is not blank, 1:null, 2:blank, 3:""<br>
     *
     * @param source source string
     * @return if blank true, else false
     */
    public static boolean isNotBlank(final CharSequence source) {
        return false == isBlank(source);
    }

    /**
     * if the source include blank
     *
     * @param source source string list
     * @return if has true, else false
     */
    public static boolean hasBlank(final CharSequence... source) {
        if (isEmpty(source)) {
            return true;
        }

        for (CharSequence str : source) {
            if (isBlank(str)) {
                return true;
            }
        }
        return false;
    }

    /**
     * if the source is empty
     *
     * @param source source
     * @param <T> the type of source
     * @return if empty true, else false
     */
    public static <T> boolean isEmpty(final T... source) {
        return source == null || source.length == 0;
    }

    /**
     * if the string is empty, 1:null, 2:""<br>
     *
     * @param source source string
     * @return if empty true, else false
     */
    public static boolean isEmpty(final CharSequence source) {
        return null==source || "".equals(source.toString().trim())
                || "null".equalsIgnoreCase(source+"") || source.length() == 0;
    }

    /**
     * if the string is not empty, 1:null, 2:""<br>
     *
     * @param source source string
     * @return if empty true, else false
     */
    public static boolean isNotEmpty(final CharSequence source) {
        return false == isEmpty(source);
    }

    /**
     * if the source is null, trans to Empty
     *
     * @param source source string
     * @return {@link String} the string of source input
     */
    public static String nullToEmpty(final CharSequence source) {
        return nullToDefault(source, EMPTY);
    }

    /**
     * if the source is empty, trans to null<code>null</code>
     *
     * @param source source string
     * @return {@link String}
     */
    public static String emptyToNull(final CharSequence source) {
        return isEmpty(source) ? null : source.toString();
    }

    /**
     * if the source is <code>null</code>, return default string, else return the source string
     *
     * <pre>
     * nullToDefault(null, &quot;default&quot;)  = &quot;default&quot;
     * nullToDefault(&quot;&quot;, &quot;default&quot;)    = &quot;&quot;
     * nullToDefault(&quot;  &quot;, &quot;default&quot;)  = &quot;  &quot;
     * nullToDefault(&quot;bat&quot;, &quot;default&quot;) = &quot;bat&quot;
     * </pre>
     *
     * @param source source string
     * @param defStr default string
     *
     * @return if the source is null, return default string, else return the source string
     */
    public static String nullToDefault(final CharSequence source, final String defStr) {
        return (source == null || source == "") ? defStr : source.toString();
    }



    /**
     * if the source include empty
     *
     * @param source source string list
     * @return if has true, else false
     */
    public static boolean hasEmpty(final CharSequence... source) {
        if (isEmpty(source)) {
            return true;
        }

        for (CharSequence str : source) {
            if (isEmpty(str)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Check that the given {@code CharSequence} is neither {@code null} nor
     * of length 0.
     * <p>Note: this method returns {@code true} for a {@code CharSequence}
     * that purely consists of whitespace.
     * StringUtils.hasLength(null) = false
     * StringUtils.hasLength("") = false
     * StringUtils.hasLength(" ") = true
     * StringUtils.hasLength("Hello") = true
     * @param source the {@code CharSequence} to check (may be {@code null})
     * @return {@code true} if the {@code CharSequence} is not {@code null} and has length
     * @see StringUtils#hasText(String)
     */
    public static boolean hasLength(CharSequence source) {
        return (source != null && source.length() > 0);
    }


    /**
     * Check that the given {@code CharSequence} is neither {@code null} nor
     * of length 0.
     * <p>Note: this method returns {@code true} for a {@code CharSequence}
     * that purely consists of whitespace.
     * StringUtils.hasLength(null) = false
     * StringUtils.hasLength("") = false
     * StringUtils.hasLength(" ") = true
     * StringUtils.hasLength("Hello") = true
     * @param source the {@code CharSequence} to check (may be {@code null})
     * @return {@code true} if the {@code CharSequence} is not {@code null} and has length
     * @see StringUtils#hasText(String)
     */
    public static boolean hasLength(String source) {
        return hasLength((CharSequence) source);
    }

    /**
     * Check whether the given {@code CharSequence} contains actual <em>text</em>.
     * <p>More specifically, this method returns {@code true} if the
     * {@code CharSequence} is not {@code null}, its length is greater than
     * 0, and it contains at least one non-whitespace character.
     * StringUtils.hasText(null) = false
     * StringUtils.hasText("") = false
     * StringUtils.hasText(" ") = false
     * StringUtils.hasText("12345") = true
     * StringUtils.hasText(" 12345 ") = true
     * @param str the {@code CharSequence} to check (may be {@code null})
     * @return {@code true} if the {@code CharSequence} is not {@code null},
     * its length is greater than 0, and it does not contain whitespace only
     * @see Character#isWhitespace
     */
    public static boolean hasText(CharSequence str) {
        if (!hasLength(str)) {
            return false;
        }
        int strLen = str.length();
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check whether the given {@code CharSequence} contains actual <em>text</em>.
     * <p>More specifically, this method returns {@code true} if the
     * {@code CharSequence} is not {@code null}, its length is greater than
     * 0, and it contains at least one non-whitespace character.
     * StringUtils.hasText(null) = false
     * StringUtils.hasText("") = false
     * StringUtils.hasText(" ") = false
     * StringUtils.hasText("12345") = true
     * StringUtils.hasText(" 12345 ") = true
     * @param source the {@code CharSequence} to check (may be {@code null})
     * @return {@code true} if the {@code CharSequence} is not {@code null},
     * its length is greater than 0, and it does not contain whitespace only
     * @see Character#isWhitespace
     */
    public static boolean hasText(String source) {
        return hasText((CharSequence) source);
    }

    /**
     * trim the space of source string in the head or the bottom
     * if the source string is<code>null</code>, return <code>null</code>
     * the trim is not the same with <code>String.trim</code>,
     * in this use <code>isBlankChar</code> to deal with blank,
     * can trim english or chinese char, blank char
     * trim(null)          = null
     * trim(&quot;&quot;)            = &quot;&quot;
     * trim(&quot;     &quot;)       = &quot;&quot;
     * trim(&quot;abc&quot;)         = &quot;abc&quot;
     * trim(&quot;    abc    &quot;) = &quot;abc&quot;
     * @param source source string
     *
     * @return the String trim over
     */
    public static String trim(final CharSequence source) {
        return (null == source) ? null : trim(source, 0);
    }

    /**
     * trim the String[] in the head or the bottom
     *
     * @param source source string list
     */
    public static void trim(final String[] source) {
        if (null == source) {
            return;
        }
        String str;
        for (int i = 0; i < source.length; i++) {
            str = source[i];
            if (null != str) {
                source[i] = str.trim();
            }
        }
    }

    /**
     * trim the space of source string in the head or the bottom
     * if the source string is<code>null</code>, return <code>null</code>
     *
     * the trim is not the same with <code>String.trim</code>,
     * in this use <code>isBlankChar</code> to deal with blank,
     * can trim english or chinese char, blank char
     *
     * @param source source string
     * @param mode <code>-1</code> trim head, <code>0</code> trim all source, <code>1</code>trim bottom
     * @return the String trim over
     */
    public static String trim(final CharSequence source, final int mode) {
        if (source == null) {
            return null;
        }

        int length = source.length();
        int start = 0;
        int end = length;

        // trim the head
        if (mode <= 0) {
            while ((start < end) && (isBlankChar(source.charAt(start)))) {
                start++;
            }
        }

        // trim the bottom
        if (mode >= 0) {
            while ((start < end) && (isBlankChar(source.charAt(end - 1)))) {
                end--;
            }
        }

        if ((start > 0) || (end < length)) {
            return source.toString().substring(start, end);
        }

        return source.toString();
    }

    /**
     * if the source string is start with prefix
     *
     * @param source source string
     * @param prefix prefix
     * @param isIgnoreCase if true, ignore case lower or upper
     * @return if start with true, else false
     */
    public static boolean startsWith(final CharSequence source, final CharSequence prefix, final boolean isIgnoreCase) {
        if (isIgnoreCase) {
            return source.toString().toLowerCase().startsWith(prefix.toString().toLowerCase());
        } else {
            return source.toString().startsWith(prefix.toString());
        }
    }

    /**
     * if the source string is start with prefix, the default is to ignore case
     *
     * @param source source string
     * @param prefix prefix
     * @return if start with true, else false
     */
    public static boolean startsWith(final CharSequence source, final CharSequence prefix) {
        return startsWith(source, prefix, false);
    }

    /**
     * if the source string is start with prefixes, more prefixes input, the default is to ignore case
     *
     * @param source source string
     * @param prefixes prefixes, more prefixes input
     * @return if start with true, else false
     */
    public static boolean startsWith(final CharSequence source, final CharSequence... prefixes) {
        if (isEmpty(source) || isEmpty(prefixes)) {
            return false;
        }

        for (CharSequence suffix : prefixes) {
            if (startsWith(source, suffix, false)) {
                return true;
            }
        }
        return false;
    }

    /**
     * if the source string is end with suffix
     *
     * @param source source string
     * @param suffix suffix
     * @param isIgnoreCase if true, ignore case lower or upper
     * @return if end with true, else false
     */
    public static boolean endsWith(final CharSequence source, final CharSequence suffix, final boolean isIgnoreCase) {
        if (isBlank(source) || isBlank(suffix)) {
            return false;
        }

        if (isIgnoreCase) {
            return source.toString().toLowerCase().endsWith(suffix.toString().toLowerCase());
        } else {
            return source.toString().endsWith(suffix.toString());
        }
    }

    /**
     * if the source string is end with suffix, the default is to ignore case
     *
     * @param source source string
     * @param suffix suffix
     * @return if end with true, else false
     */
    public static boolean endsWith(final CharSequence source, final CharSequence suffix) {
        return endsWith(source, suffix, false);
    }


    /**
     * if the source string is end with suffix, suffixes input, the default is to ignore case
     *
     * @param source source string
     * @param suffixes suffixes, suffixes input
     * @return if end with true, else false
     */
    public static boolean endsWith(final CharSequence source, final CharSequence... suffixes) {
        if (isEmpty(source) || isEmpty(suffixes)) {
            return false;
        }

        for (CharSequence suffix : suffixes) {
            if (endsWith(source, suffix, false)) {
                return true;
            }
        }
        return false;
    }

    /**
     * if the source string is contain with dest string
     *
     * @param source source string
     * @param destStr dest string
     * @param isIgnoreCase if true, ignore case lower or upper
     * @return if contain true, else false
     */
    public static boolean contains(final CharSequence source, final CharSequence destStr, final boolean isIgnoreCase) {
        if (null == source) {
            return null == destStr;
        }
        if (isIgnoreCase) {
            return source.toString().toLowerCase().contains(destStr.toString().toLowerCase());
        }else{
            return source.toString().contains(destStr.toString());
        }
    }

    /**
     * if the source string is contain the dest string, destStrs input, the default is to ignore case
     *
     * @param source source string
     * @param destStrs destStr, destStrs input
     * @return if contain true, else false
     */
    public static boolean contains(final CharSequence source, final CharSequence... destStrs) {
        if (isEmpty(source) || isEmpty(destStrs)) {
            return false;
        }

        for (CharSequence destStr : destStrs) {
            if (contains(source, destStr, false)) {
                return true;
            }
        }
        return false;
    }


    /**
     * remove source string when find from string to default ""<br>
     * such as:remove("ab-cd-ef-gh", "-"):abcdefgh,
     * the default replace to {@link StringUtils#EMPTY}
     *
     * @param source source string
     * @param remove remove string
     * @return {@link String} the string of source input
     */
    public static String remove(final String source, final CharSequence remove) {
        return replace(source, remove, EMPTY);
    }

    /**
     * remove source string when find from char to default ""<br>
     * such as:remove("ab-cd-ef-gh", "-"):abcdefgh,
     * the default replace to {@link StringUtils#EMPTY}
     *
     * @param source source string
     * @param remove remove string
     * @return {@link String} the string of source input
     */
    public static String remove(final String source, final char remove) {
        if (isEmpty(source) || source.indexOf(remove) == INDEX_NOT_FOUND) {
            return source;
        }
        final char[] chars = source.toCharArray();
        int pos = 0;
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] != remove) {
                chars[pos++] = chars[i];
            }
        }
        return new String(chars, 0, pos);
    }

    /**
     * replace source string from from string to to string <br>
     *
     * @param source source string
     * @param from from string
     * @param to to string
     * @return {@link String} the string of source input
     */
    public static String replace(final String source, final CharSequence from, final CharSequence to) {
        return source.replace(from, to);
    }



    /**
     * split source string by sep char<br>
     * such as:a,b,c,d:split(source, ','), return [a,b,c,d] <br>
     * a,b,,d:split(source, ','), return [a,b,'',d] <br>
     *
     * @param source source string
     * @param spt separator
     * @return a list
     */
    public static List<String> split(final CharSequence source, final char spt) {
        return split(source, spt, 0);
    }

    /**
     * split source string by sep char<br>
     * such as:a,b,c,d:split(source, ","), return [a,b,c,d] <br>
     * a,b,,d:split(source, ","), return [a,b,"",d] <br>
     *
     * @param source source string
     * @param spt separator
     * @param limit limit
     * @return a list
     */
    public static List<String> split(final CharSequence source, final char spt, final int limit) {
        if (source == null) {
            return null;
        }
        List<String> list = new ArrayList<>(limit > 0 ? MAX_SPLIT_LIMIT : limit);
        if (limit == 1) {
            list.add(source.toString());
            return list;
        }
        // if split to be end
        boolean isNotEnd = true;
        int strLen = source.length();
        StringBuilder sb = new StringBuilder(strLen);
        char c;
        for (int i = 0; i < strLen; i++) {
            c = source.charAt(i);
            if (isNotEnd && c == spt) {
                list.add(sb.toString());
                // clean StringBuilder
                sb.delete(0, sb.length());

                // if reach limit, leave all the next to be one
                if (limit > 0 && list.size() == limit - 1) {
                    isNotEnd = false;
                }
            } else {
                sb.append(c);
            }
        }
        // add the bottom
        list.add(sb.toString());
        return list;
    }

    /**
     * split source string by separator string<br>
     * such as:a,b,c,d:split(source, ","), return {a,b,c,d} <br>
     * a,b,,d:split(source, ","), return {a,b,"",d} <br>
     *
     * @param source source string
     * @param spt separator
     * @return {@link String} the string of source input[]
     */
    public static String[] split(final CharSequence source, final CharSequence spt) {
        if (source == null) {
            return null;
        }

        final String str = source.toString();
        if (str.trim().length() == 0) {
            return new String[] { str };
        }

        int len = spt.length(); // delete length
        int max = (source.length() / len) + 2; // one more for the last
        int[] positions = new int[max];

        int i, j = 0;
        int count = 0;
        positions[0] = -len;
        final String delimiter2 = spt.toString();
        while ((i = str.indexOf(delimiter2, j)) != -1) {
            count++;
            positions[count] = i;
            j = i + len;
        }
        count++;
        positions[count] = source.length();

        String[] result = new String[count];

        for (i = 0; i < count; i++) {
            result[i] = str.substring(positions[i] + len, positions[i + 1]);
        }
        return result;
    }

    /**
     * split source string by separator string and trim space<br>
     * such as:a,b,c,d:split(source, ","), return {a,b,c,d} <br>
     * a,b,,d:split(source, ","), return {a,b,d} <br>
     *
     * @param source source string
     * @param spt separator
     * @return {@link String} the string of source input[]
     */
    public static String[] splitAndTrim(final CharSequence source, final CharSequence spt) {
        String[] array = split(source, spt);
        for (int i = 0, len = array.length; i < len; i++) {
            array[i] = array[i].trim();
        }
        return array;
    }

    /**
     * repeat char to make up equals length or other
     *
     * @param c the repeat char
     * @param count the count of repeat, if count lt 0, return ""
     * @return {@link String} the string of source input
     */
    public static String repeat(final char c, final int count) {
        if (count <= 0) {
            return EMPTY;
        }

        char[] result = new char[count];
        for (int i = 0; i < count; i++) {
            result[i] = c;
        }
        return new String(result);
    }

    /**
     * repeat string to make up equals length or other
     *
     * @param source the repeat string
     * @param count the count of repeat, if count lt 0, return ""
     * @return {@link String} the string of source input
     */
    public static String repeat(final CharSequence source, final int count) {
        if (null == source) {
            return null;
        }
        if (count <= 0) {
            return EMPTY;
        }
        if (count == 1 || source.length() == 0) {
            return source.toString();
        }

        // check
        final int len = source.length();
        final long longSize = (long) len * (long) count;
        final int size = (int) longSize;
        if (size != longSize) {
            throw new ArrayIndexOutOfBoundsException("Required String length is too large: " + longSize);
        }

        final char[] array = new char[size];
        source.toString().getChars(0, len, array, 0);
        int n;
        // n <<= 1 means n *2
        for (n = len; n < size - n; n <<= 1) {
            System.arraycopy(array, 0, array, n, n);
        }
        System.arraycopy(array, 0, array, n, size - n);
        return new String(array);
    }

    /**
     * the two string if equals, not ignore case
     *
     * <pre>
     * equals(null, null)   = true
     * equals(null, &quot;abcde&quot;)  = false
     * equals(&quot;abcde&quot;, null)  = false
     * equals(&quot;abcde&quot;, &quot;abcde&quot;) = true
     * equals(&quot;abcde&quot;, &quot;ABCDE&quot;) = false
     * </pre>
     *
     * @param str1 string1
     * @param str2 string2
     *
     * @return if equals, then true
     */
    public static boolean equals(final CharSequence str1, final CharSequence str2) {
        if (str1 == null) {
            return str2 == null;
        }

        return str1.equals(str2);
    }

    /**
     * the two string if equals, ignore case
     *
     * <pre>
     * equals(null, null)   = true
     * equals(null, &quot;abcde&quot;)  = false
     * equals(&quot;abcde&quot;, null)  = false
     * equals(&quot;abcde&quot;, &quot;abcde&quot;) = true
     * equals(&quot;abcde&quot;, &quot;ABCDE&quot;) = false
     * </pre>
     *
     * @param str1 string1
     * @param str2 string2
     *
     * @return if equals, then true
     */
    public static boolean equalsIgnoreCase(final CharSequence str1, final CharSequence str2) {
        if (str1 == null) {
            return str2 == null;
        }

        return str1.toString().equalsIgnoreCase(str2.toString());
    }

    /**
     * format the source string with template, {} is placeholders<br>
     * format method can replace {} to parameter in order<br>
     * if need to output {}, can use \\
     * if need to output \, can use \\\\ <br>
     * such as:<br>
     * format("this is {} for {}", "abc", "xyz"), the result is:this is abc for xyz<br>
     * format("this is \\{} for {}", "abc", "xyz"), the result is:this is \{} for abc<br>
     * format("this is \\\\{} for {}", "abc", "xyz"), the result is:this is \abc for xyz<br>
     *
     * @param pattern pattern to use to replace parameter in {}
     * @param params params
     * @return {@link String} the string of source input
     */
    public static String format(final CharSequence pattern, final Object... params) {
        if (null == pattern) {
            return null;
        }
        if (isEmpty(params) || isBlank(pattern)) {
            return pattern.toString();
        }
        return format(pattern.toString(), params);
    }

    /**
     * format the source string with pattern, {} is placeholders<br>
     * format method can replace {} to parameter in order<br>
     * if need to output {}, can use \\
     * if need to output \, can use \\\\ <br>
     * such as:<br>
     * format("this is {} for {}", "abc", "xyz"), the result is:this is abc for xyz<br>
     * format("this is \\{} for {}", "abc", "xyz"), the result is:this is \{} for abc<br>
     * format("this is \\\\{} for {}", "abc", "xyz"), the result is:this is \abc for xyz<br>
     *
     * @param pattern string pattern in {}
     * @param params params
     * @return {@link String} the string of source input
     */
    private static String format(final String pattern, final Object... params) {
        if (isBlank(pattern) || isEmpty(params)) {
            return pattern;
        }
        final int length = pattern.length();

        //to init sb long length to get more performance
        StringBuilder sb = new StringBuilder(length + 50);

        //the handled position
        int handledPosition = 0;
        //the index place of holder
        int holderIndex;
        for (int argIndex = 0; argIndex < params.length; argIndex++) {
            holderIndex = pattern.indexOf(EMPTY_JSON, handledPosition);
            //if the next part have not placeholders
            if (holderIndex == -1) {
                //return pattern without placeholders
                if (handledPosition == 0) {
                    return pattern;
                } else {
                    //if the next part of pattern have not placeholders, return the next part
                    sb.append(pattern, handledPosition, length);
                    return sb.toString();
                }
            } else {
                //deal with the \\
                if (holderIndex > 0 && pattern.charAt(holderIndex - 1) == C_BACKSLASH) {
                    //deal with the \\\\
                    if (holderIndex > 1 && pattern.charAt(holderIndex - 2) == C_BACKSLASH) {
                        //if there is \\ before \\, the placeholders need to be deal
                        sb.append(pattern, handledPosition, holderIndex - 1);
                        sb.append(utf8String(params[argIndex]));
                        handledPosition = holderIndex + 2;
                    } else {
                        //deal with the placeholders
                        argIndex--;
                        sb.append(pattern, handledPosition, holderIndex - 1);
                        sb.append(C_DELIM_START);
                        handledPosition = holderIndex + 1;
                    }
                } else {
                    //deal with the placeholders without any \\ or \\\\
                    sb.append(pattern, handledPosition, holderIndex);
                    sb.append(utf8String(params[argIndex]));
                    handledPosition = holderIndex + 2;
                }
            }
        }
        // append the characters following the last {} pair.
        sb.append(pattern, handledPosition, pattern.length());

        return sb.toString();
    }

    /**
     * through index in {} to format string, use {number} as placeholders<br>
     * such as:<br>
     * format("this is {0} for {1}", "abc", "xyz"), the result is:this is abc for xyz<br>
     *
     * @param pattern pattern of string
     * @param params params
     * @return {@link String} the string of source input
     */
    public static String formatOfIndex(final CharSequence pattern, final Object... params) {
        return MessageFormat.format(pattern.toString(), params);
    }

    /**
     * the source string with pattern use map K and V, {varName} is placeholders<br>
     * map = {abc: "abcValue", xyz: "xyzValue"}
     * format("{abc} and {xyz}", map), the result is:abcValue and xyzValue
     *
     * @param pattern pattern of string {key}
     * @param map the map of value
     * @return {@link String} the string of source input
     */
    public static String format(final CharSequence pattern, final Map<?, ?> map) {
        if (null == pattern) {
            return null;
        }
        if (null == map || map.isEmpty()) {
            return pattern.toString();
        }

        final String result = pattern.toString();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            result.replace("{" + entry.getKey() + "}", utf8String(entry.getValue()));
        }
        return result;
    }

    /**
     * get the byte of source string with UTF-8 Charset, the default charset is UTF-8
     *
     * @param source source string
     * @return byte[]
     */
    public static byte[] utf8Bytes(final CharSequence source) {
        return getBytes(source, Charset.forName("UTF-8"));
    }


    /**
     * get the byte of source sting with input charset
     *
     * @param source source string
     * @param charset Charset, if null use os
     * @return byte[]
     */
    public static byte[] getBytes(final CharSequence source, final Charset charset) {
        if (source == null) {
            return null;
        }

        if (null == charset) {
            return source.toString().getBytes();
        }
        return source.toString().getBytes(charset);
    }

    /**
     * get the string of source object with UTF-8 Charset, the default charset is UTF-8
     *
     * @param source source object
     * @return {@link String} the string of source input
     */
    public static String utf8String(final Object source) {
        return getString(source, Charset.forName("UTF-8"));
    }

    /**
     * get the string of source object with input charset
     * byte[] and ByteBuffer will get the String array
     * Object array will use Arrays.toString
     *
     * @param source source object
     * @param charset input charset
     * @return {@link String} the string of source input
     */
    public static String getString(final Object source, final Charset charset) {
        if (null == source) {
            return null;
        }

        if (source instanceof String) {
            return (String) source;
        } else if (source instanceof byte[]) {
            return getString((byte[]) source, charset);
        } else if (isArray(source)) {
            return toString(source);
        }

        return source.toString();
    }

    /**
     * if the source object is a Array
     *
     * @param source the source object
     * @return if is a array true, else false
     * @throws NullPointerException if the source is null, then<code>null</code>
     */
    public static boolean isArray(Object source) {
        if (null == source) {
            throw new NullPointerException("Object check for isArray is null");
        }
        return source.getClass().isArray();
    }

    /**
     * get the string of source byte[] with input charset
     *
     * @param source source byte[]
     * @param charset Charset, if null use os
     * @return {@link String} the string of source input
     */
    public static String getString(final byte[] source, final Charset charset) {
        if (source == null) {
            return null;
        }

        if (null == charset) {
            return new String(source);
        }
        return new String(source, charset);
    }


    /**
     * get the string of source ByteBuffer with input charset
     *
     * @param source source ByteBuffer
     * @param charset Charset, if null use os
     * @return {@link String} the string of source input
     */
    public static String getString(ByteBuffer source, Charset charset) {
        if (null == charset) {
            charset = Charset.defaultCharset();
        }
        return charset.decode(source).toString();
    }


    /**
     * new builder of StringBuilder
     *
     * @return {@link String} the string of source inputBuilder
     */
    public static StringBuilder builder() {
        return new StringBuilder();
    }

    /**
     * new builder of StringBuilder with capacity
     *
     * @param capacity init capacity
     * @return {@link String} the string of source inputBuilder
     */
    public static StringBuilder builder(int capacity) {
        return new StringBuilder(capacity);
    }

    /**
     * new builder of StringBuilder
     *
     * @param source init builder string
     * @return {@link String} the string of source inputBuilder
     */
    public static StringBuilder builder(CharSequence... source) {
        final StringBuilder sb = new StringBuilder();
        for (CharSequence str : source) {
            sb.append(str);
        }
        return sb;
    }

    /**
     * get reader of StringReader
     *
     * @param source source string
     * @return {@link String} the string of source inputReader
     */
    public static StringReader getReader(CharSequence source) {
        if (null == source) {
            return null;
        }
        return new StringReader(source.toString());
    }

    /**
     * get writer of StringWriter
     *
     * @return {@link String} the string of source inputWriter
     */
    public static StringWriter getWriter() {
        return new StringWriter();
    }

    /**
     * write to Writer provide to direct method<br>
     *
     * @param source a string
     * @param writer {@link Writer}
     * @return {@link Writer}
     * @throws IOException IO exception
     */
    public static Writer writeByWriter(String source, Writer writer)
            throws IOException {
        if (isEmpty(source)) {
            writer.write("\"\"");
            return writer;
        }

        char b;        //back char
        char c = 0; //current char
        String s;
        int i;
        int len = source.length();

        writer.write('"');
        for (i = 0; i < len; i++) {
            b = c;
            c = source.charAt(i);
            switch (c) {
                case '\\':
                case '"':
                    writer.write('\\');
                    writer.write(c);
                    break;
                case '/':
                    if (b == '<') {
                        writer.write('\\');
                    }
                    writer.write(c);
                    break;
                case '\b':
                    writer.write("\\b");
                    break;
                case '\t':
                    writer.write("\\t");
                    break;
                case '\n':
                    writer.write("\\n");
                    break;
                case '\f':
                    writer.write("\\f");
                    break;
                case '\r':
                    writer.write("\\r");
                    break;
                default:
                    if (c < ' ' || (c >= '\u0080' && c < '\u00a0') || (c >= '\u2000' && c < '\u2100')) {
                        writer.write("\\u");
                        s = Integer.toHexString(c);
                        writer.write("0000", 0, 4 - s.length());
                        writer.write(s);
                    } else {
                        writer.write(c);
                    }
            }
        }
        writer.write('"');
        return writer;
    }

    /**
     * count the dest char in source string<br>
     *
     * @param source 内容
     * @param findChar char to be found
     * @return count of found
     */
    public static int count(final CharSequence source, final char findChar) {
        int count = 0;
        if (isEmpty(source)) {
            return 0;
        }
        int contentLength = source.length();
        for (int i = 0; i < contentLength; i++) {
            if (findChar == source.charAt(i)) {
                count++;
            }
        }
        return count;
    }

    /**
     * count the dest string in source string<br>
     * if source is {@code null} or "", return {@code 0}
     *
     * <pre>
     * count(null, *)       = 0
     * count("", *)         = 0
     * count("abccba", null)  = 0
     * count("abccba", "")    = 0
     * count("abccba", "a")   = 2
     * count("abccba", "ab")  = 1
     * count("abccba", "xxx") = 0
     * </pre>
     *
     * @param source source string
     * @param findStr string to be found
     * @return count of found
     */
    public static int count(final CharSequence source, final CharSequence findStr) {
        if (hasEmpty(source, findStr) || findStr.length() > source.length()) {
            return 0;
        }

        int count = 0;
        int idx = 0;
        final String content2 = source.toString();
        final String strForSearch2 = findStr.toString();
        while ((idx = content2.indexOf(strForSearch2, idx)) > -1) {
            count++;
            idx += findStr.length();
        }
        return count;
    }

    /**
     * implicit the source with ... if the too long
     * such as:"xxx...xxx"
     *
     * @param source source string
     * @param length the length of implicit after
     * @return {@link String} the string of source input
     */
    public static String implicit(final CharSequence source, final int length) {
        if (null == source) {
            return null;
        }
        if ((source.length() + 3) <= length) {
            return source.toString();
        }
        int w = length / 2;
        int l = source.length();

        final String str2 = source.toString();
        return format("{}...{}", str2.substring(0, length - w), str2.substring(l - w));
    }


    /**
     * index of dest char of the source string
     *
     * @param source source string
     * @param dest the string to be found
     * @return the first position
     */
    public static int indexOf(final CharSequence source, final char dest) {
        return indexOf(source, dest, 0);
    }

    /**
     * index of dest char of the source string from the pos
     *
     * @param source source string
     * @param dest the string to be found
     * @param begin the position to begin
     * @return the first position
     */
    public static int indexOf(final CharSequence source, final char dest, final int begin) {
        if (source instanceof String) {
            return ((String) source).indexOf(dest, begin);
        } else {
            return indexOf(source, dest, begin, -1);
        }
    }

    /**
     * index of dest char of the source string between the begin to end
     *
     * @param source source string
     * @param dest the string to be found
     * @param begin the position to begin
     * @param end the position to end
     * @return the first position
     */
    public static int indexOf(final CharSequence source, final char dest, int begin, int end) {
        int len = source.length();
        if (begin < 0 || begin > len) {
            begin = 0;
        }
        if (end > len || end < 0) {
            end = len;
        }
        for (int i = begin; i < end; i++) {
            if (source.charAt(i) == dest) {
                return i;
            }
        }
        return -1;
    }

    /**
     * last index of dest char of the source string
     *
     * @param source source string
     * @param dest the string to be found
     * @return the first position
     */
    public static int lastIndexOf(final CharSequence source, final char dest) {
        return ((String) source).lastIndexOf(dest);
    }

    /**
     * get the subString of source string from fromIndex to toIndex<br>
     * index is begin from 0, the last char is -1<br>
     * if the fromIndex == toIndex, return "" <br>
     * if fromIndex or toIndex lt 0, use the length of source string
     * @see StringUtils#substring
     * substring("abcdefghijk", 2, 3): c <br>
     * substring("abcdefghijk", 2, -3): cdefghi <br>
     *
     * @param source source string
     * @param fromIndex from index, include from index
     * @return String the substring of fromIndex to toIndex
     */
    public static String substring(CharSequence source, int fromIndex) {
        int len = source.length();
        return substring(source, fromIndex, len);
    }

    /**
     * get the subString of source string from fromIndex to toIndex<br>
     * index is begin from 0, the last char is -1<br>
     * if the fromIndex == toIndex, return "" <br>
     * if fromIndex or toIndex lt 0, use the length of source string
     * @see StringUtils#substring
     * substring("abcdefghijk", 2, 3): c <br>
     * substring("abcdefghijk", 2, -3): cdefghi <br>
     *
     * @param source source string
     * @param fromIndex from index, include from index
     * @param toIndex to index, exclude to index
     * @return String the substring of fromIndex to toIndex
     */
    public static String substring(CharSequence source, int fromIndex, int toIndex) {
        int len = source.length();
        if (fromIndex < 0) {
            fromIndex = len + fromIndex;
            if (fromIndex < 0) {
                fromIndex = 0;
            }
        } else if (fromIndex > len) {
            fromIndex = len;
        }

        if (toIndex < 0) {
            toIndex = len + toIndex;
            if (toIndex < 0) {
                toIndex = len;
            }
        } else if (toIndex > len) {
            toIndex = len;
        }

        if (toIndex < fromIndex) {
            int tmp = fromIndex;
            fromIndex = toIndex;
            toIndex = tmp;
        }

        if (fromIndex == toIndex) {
            return StringUtils.EMPTY;
        }

        return source.toString().substring(fromIndex, toIndex);
    }

    /** 将空对象输出为""
     * @param source the source object
     * @return if null then ""
     */
    public static String clearNull(Object source) {
        if (source == null)
            return "";
        if (!(source instanceof String))
            return "";
        return (String) source;
    }

    /** 是否为string数组
     * @param source the input source string
     * @param array the array
     * @return  true
     *          false
     */
    public static boolean inStringArray(String source, String[] array) {
        for (String x : array) {
            if (x.equals(source)) {
                return true;
            }
        }
        return false;
    }

    /**Object类型转String类型
     * @param source source
     * @return a string
     */
    public static String toString(Object source) {
        try {
            if (source == null) {
                return "";
            } else {
                return source.toString();
            }
        } catch (Exception e) {
            return "";
        }
    }


    /**
     * 大写首字母<br>
     * 例如：str = name, return Name
     *
     * @param str 字符串
     * @return 字符串
     */
    public static String upperFirst(CharSequence str) {
        if (null == str) {
            return null;
        }
        if (str.length() > 0) {
            char firstChar = str.charAt(0);
            if (Character.isLowerCase(firstChar)) {
                return Character.toUpperCase(firstChar) + substring(str, 1);
            }
        }
        return str.toString();
    }

    /**
     * 小写首字母<br>
     * 例如：str = Name, return name
     *
     * @param str 字符串
     * @return 字符串
     */
    public static String lowerFirst(CharSequence str) {
        if (null == str) {
            return null;
        }
        if (str.length() > 0) {
            char firstChar = str.charAt(0);
            if (Character.isUpperCase(firstChar)) {
                return Character.toLowerCase(firstChar) + substring(str, 1);
            }
        }
        return str.toString();
    }

}

