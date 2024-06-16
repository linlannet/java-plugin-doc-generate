package io.linlan.doc.utils;


import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtilsExt  {
    public static final String EMPTY = "";
    private static final String SERIAL_NO_PATTERN = "yyyyMMddHHmmssSSS";
    private static final char UNDERLINE = '_';
    private static final char HYPHEN_LINE = '-';

    public StringUtilsExt() {
    }

    public static boolean isEmpty(String str) {
        return null == str || "".equals(str.trim()) || "null".equals(str.trim()) || "NaN".equals(str.trim());
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static boolean isSameCharacter(String s) {
        s = s.toUpperCase();
        String character = s.substring(0, 1);
        String replace = "";
        String test = s.replace(character, replace);
        return "".equals(test);
    }

    public static boolean isContinuityCharacter(String s) {
        boolean continuity = true;
        char[] data = s.toCharArray();

        for(int i = 0; i < data.length - 1; ++i) {
            int a = Integer.parseInt(data[i] + "");
            int b = Integer.parseInt(data[i + 1] + "");
            continuity = continuity && (a + 1 == b || a - 1 == b);
        }

        return continuity;
    }

    public static String getCharCode(String str) {
        String temp = "";

        for(int i = 0; i < temp.length(); ++i) {
            temp = temp + Integer.toHexString(str.charAt(i)) + "nbsp;";
        }

        return temp;
    }

    public static String convertTo8859(String str) {
        String strOutPut = "";

        try {
            byte[] tempStrByte = str.getBytes("ISO-8859-1");
            strOutPut = new String(tempStrByte);
        } catch (Exception var3) {
            var3.printStackTrace();
        }

        return strOutPut;
    }

    public static String capitalise(String fieldName) {
        return fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    }

    public static String trim(String sourceStr) {
        return isEmpty(sourceStr) ? null : sourceStr.replaceAll(" ", "");
    }

    public static String filterStr(String str) {
        if (isEmpty(str)) {
            return str;
        } else {
            str = str.replaceAll(";", "");
            str = str.replaceAll("%", "");
            str = str.replaceAll("--", "");
            str = str.replaceAll("/", "");
            str = str.replaceAll("=", "");
            str = str.replaceAll("'", "&#39;");
            str = str.replaceAll("\\(", "&#40;").replace("\\)", "&#41;");
            str = str.replaceAll("<", "&lt");
            str = str.replaceAll(">", "&gt");
            return str;
        }
    }

    public static String cleanSqlWildCharater(String str) {
        if (isEmpty(str)) {
            return str;
        } else {
            str = str.replaceAll("%", "invalid character");
            str = str.replaceAll("_", "invalid character");
            str = str.replaceAll("=", "invalid character");
            return str;
        }
    }

    public static String cleanXSS(String value) {
        if (null == value) {
            return value;
        } else {
            value = value.replaceAll("\\bselect\\b", "invalid character");
            value = value.replaceAll("\\band\\b", "invalid character");
            value = value.replaceAll("\\bor\\b", "invalid character");
            value = value.replaceAll("\\bdelete\\b", "invalid character");
            value = value.replaceAll("\\bjoin\\b", "invalid character");
            value = value.replaceAll("\\bdrop\\b", "invalid character");
            value = value.replaceAll("\\+", "&#43;");
            value = value.replaceAll("&", "&amp;");
            value = value.replaceAll("%", "&#37;");
            value = value.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
            value = value.replaceAll("%3C", "&lt;").replaceAll("%3E", "&gt;");
            value = value.replaceAll("\\(", "&#40;").replaceAll("\\)", "&#41;");
            value = value.replaceAll("%28", "&#40;").replaceAll("%29", "&#41;");
            value = value.replaceAll("'", "&#39;");
            value = value.replaceAll("alert", "invalid character");
            value = value.replaceAll("eval\\((.*)\\)", "invalid character");
            value = value.replaceAll("[\\\"\\'][\\s]*javascript:(.*)[\\\"\\']", "\"\"");
            value = value.replaceAll("<\\s*script", "invalid character");
            value = value.replaceAll("location.href", "invalid character");
            return value;
        }
    }

    public static String camelToUnderline(String param) {
        if (param != null && !"".equals(param.trim())) {
            int length = param.length();
            StringBuilder sb = new StringBuilder(length);

            for(int i = 0; i < length; ++i) {
                char c = param.charAt(i);
                if (Character.isUpperCase(c)) {
                    sb.append('_');
                    sb.append(Character.toLowerCase(c));
                } else {
                    sb.append(c);
                }
            }

            return sb.toString();
        } else {
            return "";
        }
    }

    public static String underlineToCamel(String param) {
        return toCamel(param, '_');
    }

    public static String hyphenLineToCamel(String param) {
        return toCamel(param, '-');
    }

    public static String toCapitalizeCamelCase(String s) {
        if (s == null) {
            return null;
        } else {
            s = underlineToCamel(s);
            return s.substring(0, 1).toUpperCase() + s.substring(1);
        }
    }

    public static String firstToUpperCase(String param) {
        char[] cs = param.toCharArray();
        if (cs[0] > '`' && cs[0] < '{') {
            cs[0] = (char)(cs[0] - 32);
        }

        return String.valueOf(cs);
    }

    public static String firstToLowerCase(String param) {
        char[] cs = param.toCharArray();
        if (cs[0] > '@' && cs[0] < '[') {
            cs[0] = (char)(cs[0] + 32);
        }

        return String.valueOf(cs);
    }

    public static String createSerialNo() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        return format.format(cal.getTime());
    }

    public static String urlDecode(String str) {
        if (isEmpty(str)) {
            return null;
        } else {
            try {
                return URLDecoder.decode(str, "utf-8");
            } catch (UnsupportedEncodingException var2) {
                var2.printStackTrace();
                return null;
            }
        }
    }

    public static String urlEncode(String str) {
        if (isEmpty(str)) {
            return null;
        } else {
            try {
                return URLEncoder.encode(str, "utf-8");
            } catch (UnsupportedEncodingException var2) {
                var2.printStackTrace();
                return null;
            }
        }
    }

    public static String ios8859ToUtf8(String str) {
        if (isEmpty(str)) {
            return null;
        } else {
            try {
                return new String(str.getBytes("iso8859-1"), "utf-8");
            } catch (Exception var2) {
                return null;
            }
        }
    }

    public static String binaryString2hexString(String bString) {
        if (bString != null && !bString.equals("") && bString.length() % 8 == 0) {
            StringBuilder tmp = new StringBuilder();

            for(int i = 0; i < bString.length(); i += 4) {
                int iTmp = 0;

                for(int j = 0; j < 4; ++j) {
                    iTmp += Integer.parseInt(bString.substring(i + j, i + j + 1)) << 4 - j - 1;
                }

                tmp.append(Integer.toHexString(iTmp));
            }

            return tmp.toString();
        } else {
            return null;
        }
    }

    public static String hexString2binaryString(String hexString) {
        if (hexString != null && hexString.length() % 2 == 0) {
            String bString = "";

            for(int i = 0; i < hexString.length(); ++i) {
                String tmp = "0000" + Integer.toBinaryString(Integer.parseInt(hexString.substring(i, i + 1), 16));
                bString = bString + tmp.substring(tmp.length() - 4);
            }

            return bString;
        } else {
            return null;
        }
    }

    private static String fillStringByArgs(String str, String[] arr) {
        for(Matcher m = Pattern.compile("\\{(\\d)\\}").matcher(str); m.find(); str = str.replace(m.group(), arr[Integer.parseInt(m.group(1))])) {
            ;
        }

        return str;
    }

    public static String trimBlank(String str) {
        return isEmpty(str) ? null : str.replaceAll("^[　 ]+|[　 ]+$", "");
    }

    public static int length(String str) {
        return isEmpty(str) ? 0 : str.length();
    }

    public static String createRandom(int length) {
        double a = Math.pow(10.0D, (double)(length - 1));
        int num = (int)((Math.random() * 9.0D + 1.0D) * a);
        return String.valueOf(num);
    }

    public static String removeQuotes(String str) {
        return isNotEmpty(str) ? str.replaceAll("'", "").replaceAll("\"", "") : "";
    }

    public static String removeDoubleQuotes(String str) {
        return isNotEmpty(str) ? str.replaceAll("\"", "") : "";
    }

    public static String replaceHtml(String html) {
        if (isEmpty(html)) {
            return "";
        } else {
            String regEx = "<.+?>";
            Pattern p = Pattern.compile(regEx);
            Matcher m = p.matcher(html);
            String s = m.replaceAll("");
            return s;
        }
    }

    public static String replaceMobileHtml(String html) {
        return html == null ? "" : html.replaceAll("<([a-z]+?)\\s+?.*?>", "<$1>");
    }

    public static String getChinese(String str) {
        String reg = "[^一-龥]";
        str = str.replaceAll(reg, "");
        return str;
    }

    public static String getNotChinese(String str) {
        String reg = "[^A-Za-z0-9_]";
        str = str.replaceAll(reg, "");
        return str;
    }

    public static String removePrefix(String str, String prefix) {
        if (!isEmpty(str) && !isEmpty(prefix)) {
            return str.startsWith(prefix) ? str.substring(prefix.length()) : str;
        } else {
            return str;
        }
    }

    public static String[] split(String str, String regex) {
        return null != str ? str.split(regex) : null;
    }

    public static String seqNumLeftPadZero(long seq, int len) {
        String b = String.valueOf(seq);
        StringBuilder builder = new StringBuilder();
        int rest = len - b.length();

        for(int i = 0; i < rest; ++i) {
            builder.append("0");
        }

        builder.append(b);
        return builder.toString();
    }

    public static String unicode2String(String unicode) {
        if (isEmpty(unicode)) {
            return "";
        } else {
            StringBuilder string = new StringBuilder();
            String[] hex = unicode.split("\\\\u");

            for(int i = 1; i < hex.length; ++i) {
                int data = Integer.parseInt(hex[i], 16);
                string.append((char)data);
            }

            return string.toString();
        }
    }

    private static String toCamel(String param, char s) {
        if (param != null && !"".equals(param.trim())) {
            int length = param.length();
            StringBuilder sb = new StringBuilder(length);

            for(int i = 0; i < length; ++i) {
                char c = param.charAt(i);
                if (c == s) {
                    ++i;
                    if (i < length) {
                        sb.append(Character.toUpperCase(param.charAt(i)));
                    }
                } else {
                    sb.append(c);
                }
            }

            return sb.toString();
        } else {
            return "";
        }
    }

    public static String[] tokenizeToStringArray(String str, String delimiters) {
        return tokenizeToStringArray(str, delimiters, true, true);
    }

    public static String[] tokenizeToStringArray(String str, String delimiters, boolean trimTokens, boolean ignoreEmptyTokens) {
        if (str == null) {
            return null;
        } else {
            StringTokenizer st = new StringTokenizer(str, delimiters);
            ArrayList tokens = new ArrayList();

            while(true) {
                String token;
                do {
                    if (!st.hasMoreTokens()) {
                        return toStringArray(tokens);
                    }

                    token = st.nextToken();
                    if (trimTokens) {
                        token = token.trim();
                    }
                } while(ignoreEmptyTokens && token.length() <= 0);

                tokens.add(token);
            }
        }
    }

    public static String[] toStringArray(Collection collection) {
        return collection == null ? null : (String[])collection.toArray(new String[collection.size()]);
    }

}
