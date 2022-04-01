/*
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.server.logs;

import com.qwazr.utils.StringUtils;
import io.undertow.security.api.SecurityContext;
import io.undertow.security.idm.Account;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

public enum LogParam {

    C_IP(0, "c-ip", ctx -> ctx.sourceAddress.getAddress().getHostAddress()),

    CS_HOST(1, "cs-host", ctx -> ctx.sourceAddress.getHostName()),

    CS_METHOD(2, "cs-method", ctx -> ctx.exchange.getRequestMethod().toString()),

    CS_URI_QUERY(3, "cs-uri-query", ctx -> ctx.exchange.getQueryString()),

    CS_URI_STEM(4, "cs-uri-stem", ctx -> ctx.exchange.getRequestPath()),

    CS_USER_AGENT(5, "cs-user-agent", ctx -> ctx.requestHeaders.getFirst("User-Agent")),

    CS_USERNAME(6, "cs-username", ctx -> getUsername(ctx.exchange.getSecurityContext())),

    CS_X_FORWARDED_FOR(7, "cs-x-forwarded-for", ctx -> ctx.requestHeaders.getFirst("X-Forwarded-For")),

    DATE(8, "date", ctx -> getDate(ctx.logDateTime)),

    CS_REFERER(9, "cs-referer", ctx -> ctx.requestHeaders.getFirst("Referer")),

    SC_STATUS(10, "sc-status", ctx -> Integer.toString(ctx.exchange.getStatusCode())),

    S_IP(11, "s-ip", ctx -> ctx.destinationAddress.getAddress().getHostAddress()),

    S_PORT(12, "s-port", ctx -> Integer.toString(ctx.destinationAddress.getPort())),

    TIME(13, "time", ctx -> getTime(ctx.logDateTime)),

    TIME_TAKEN(14, "time-taken", ctx -> Integer.toString(
            ctx.nanoStartTime == -1 ? 0 : (int) ((ctx.nanoEndTime - ctx.nanoStartTime) / 1_000_000))),

    CS_BYTES(15, "cs-bytes", ctx -> Long.toString(ctx.exchange.getRequestContentLength())),

    SC_BYTES(16, "sc-bytes", ctx -> Long.toString(ctx.exchange.getResponseBytesSent()));

    final int pos;

    final String name;

    final Function<LogContext, String> supplier;

    public final static List<LogParam> DEFAULT_PARAMS =
            List.of(DATE, TIME, C_IP, S_IP, CS_METHOD, CS_URI_STEM, CS_URI_QUERY, SC_STATUS, CS_REFERER, CS_USER_AGENT,
                    CS_BYTES, SC_BYTES, TIME_TAKEN, CS_HOST);

    public final static String DEFAULT_MESSAGE =
            "{0}\t{1}\t{2}\t{3}\t{4}\t{5}\t{6}\t{7}\t{8}\t{9}\t{10}\t{11}\t{12}\t{13}";

    LogParam(int pos, String name, Function<LogContext, String> supplier) {
        this.pos = pos;
        this.name = name;
        this.supplier = supplier;
    }

    private static String getUsername(final SecurityContext securityContext) {
        if (!securityContext.isAuthenticated())
            return null;
        final Account account = securityContext.getAuthenticatedAccount();
        if (account == null)
            return null;
        final Principal principal = account.getPrincipal();
        if (principal == null)
            return null;
        return principal.getName();
    }

    private static void span2(final StringBuilder sb, final int value) {
        if (value < 10)
            sb.append('0');
        sb.append(value);
    }

    private static void span3(final StringBuilder sb, final int value) {
        if (value < 10)
            sb.append("00");
        else if (value < 100)
            sb.append('0');
        sb.append(value);
    }

    private static String getDate(final LocalDateTime localDateTime) {
        final StringBuilder sb = new StringBuilder();
        sb.append(localDateTime.getYear());
        sb.append('-');
        span2(sb, localDateTime.getMonthValue());
        sb.append('-');
        span2(sb, localDateTime.getDayOfMonth());
        return sb.toString();
    }

    private static String getTime(final LocalDateTime localDateTime) {
        final StringBuilder sb = new StringBuilder();
        span2(sb, localDateTime.getHour());
        sb.append(':');
        span2(sb, localDateTime.getMinute());
        sb.append(':');
        span2(sb, localDateTime.getSecond());
        sb.append('.');
        span3(sb, localDateTime.getNano() / 1000000);
        return sb.toString();
    }

    /**
     * Extract the log parameters from the logContext
     *
     * @param logContext the source of the logs
     * @param logParams  the parameters to extract
     * @return a new array of translated parameters
     */
    static Object[] translate(final LogContext logContext, final LogParam... logParams) {
        final Object[] parameters = new Object[logParams.length];
        int i = 0;
        for (final LogParam logParam : logParams) {
            final Object param = logParam.supplier.apply(logContext);
            parameters[i++] = param == null ? StringUtils.EMPTY : param;
        }
        return parameters;
    }
}
