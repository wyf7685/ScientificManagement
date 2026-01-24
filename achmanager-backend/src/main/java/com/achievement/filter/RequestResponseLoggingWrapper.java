package com.achievement.filter;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StreamUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * 请求响应日志包装器
 * 用于捕获请求体和响应体内容
 *
 * @author system
 * @since 2026-01-24
 */
@Slf4j
public class RequestResponseLoggingWrapper {

    /**
     * 可缓存的请求包装器
     */
    public static class CachingRequestWrapper extends HttpServletRequestWrapper {
        
        private final byte[] cachedBody;

        public CachingRequestWrapper(HttpServletRequest request) throws IOException {
            super(request);
            InputStream requestInputStream = request.getInputStream();
            this.cachedBody = StreamUtils.copyToByteArray(requestInputStream);
        }

        @Override
        public ServletInputStream getInputStream() {
            return new CachingServletInputStream(this.cachedBody);
        }

        @Override
        public BufferedReader getReader() {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(this.cachedBody);
            return new BufferedReader(new InputStreamReader(byteArrayInputStream, StandardCharsets.UTF_8));
        }

        public String getBody() {
            return new String(this.cachedBody, StandardCharsets.UTF_8);
        }
    }

    /**
     * 可缓存的响应包装器
     */
    public static class CachingResponseWrapper extends HttpServletResponseWrapper {
        
        private final ByteArrayOutputStream cachedBody = new ByteArrayOutputStream();
        private final PrintWriter writer;

        public CachingResponseWrapper(HttpServletResponse response) throws IOException {
            super(response);
            this.writer = new PrintWriter(new OutputStreamWriter(cachedBody, StandardCharsets.UTF_8));
        }

        @Override
        public PrintWriter getWriter() {
            return this.writer;
        }

        @Override
        public void flushBuffer() throws IOException {
            this.writer.flush();
            super.flushBuffer();
        }

        public String getBody() {
            this.writer.flush();
            return new String(this.cachedBody.toByteArray(), StandardCharsets.UTF_8);
        }

        public byte[] getBodyBytes() {
            this.writer.flush();
            return this.cachedBody.toByteArray();
        }

        /**
         * 将缓存的响应体写入原始响应
         */
        public void copyBodyToResponse() throws IOException {
            if (cachedBody.size() > 0) {
                getResponse().getOutputStream().write(cachedBody.toByteArray());
            }
        }
    }

    /**
     * 可缓存的ServletInputStream实现
     */
    private static class CachingServletInputStream extends ServletInputStream {
        
        private final ByteArrayInputStream inputStream;

        public CachingServletInputStream(byte[] cachedBody) {
            this.inputStream = new ByteArrayInputStream(cachedBody);
        }

        @Override
        public boolean isFinished() {
            return inputStream.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
            throw new UnsupportedOperationException("ReadListener is not supported");
        }

        @Override
        public int read() {
            return inputStream.read();
        }

        @Override
        public int read(byte[] b) throws IOException {
            return inputStream.read(b);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return inputStream.read(b, off, len);
        }
    }
}