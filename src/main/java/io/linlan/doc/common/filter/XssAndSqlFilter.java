package io.linlan.doc.common.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * xss and sql injection filter
 */
public class XssAndSqlFilter extends AbstractUrlMatcher implements Filter {

    public static final String IGNORES = "ignores";
    FilterConfig filterConfig = null;

    private Set<String> excluded = null;

    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
        String excludedString = filterConfig.getInitParameter(IGNORES);
        if (io.linlan.doc.common.util.StringUtil.isNotEmpty(excludedString)) {
            excluded = Collections.unmodifiableSet(
                    new HashSet<>(Arrays.asList(excludedString.split(";", 0))));
        } else {
            excluded = Collections.emptySet();
        }
    }

    public void destroy() {
        this.filterConfig = null;
    }

    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletResponse res = (HttpServletResponse) response;
        HttpServletRequest req = (HttpServletRequest) request;
        //res.setHeader("Content-Security-Policy", "frame-ancestors 'self'");
        res.addHeader("X-XSS-Protection", "1; mode=block");
      /*  res.addHeader( "Cache-Control", "no-store" );
        res.addHeader( "Cache-Control", "no-cache" );*/
        // Disabling browsers to perform risky mime sniffing
        res.addHeader("X-Content-Type-Options", "nosniff");
//        res.setHeader("X-Frame-Options","sameorigin");
        if (isExcluded(req)) {
            chain.doFilter(request, response);
        } else {
            chain.doFilter(new XssHttpServletRequestWrapper(
                    (HttpServletRequest) request), response);
        }
    }

    private boolean isExcluded(HttpServletRequest request) {
        String url0 = request.getRequestURI();
        return this.isMatches(excluded, url0);
    }
}
