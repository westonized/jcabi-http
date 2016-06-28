/**
 * Copyright (c) 2011-2015, jcabi.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the jcabi.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jcabi.http.wire;

import com.google.common.io.ByteStreams;
import com.jcabi.aspects.Tv;
import com.jcabi.http.Request;
import com.jcabi.http.mock.MkAnswer;
import com.jcabi.http.mock.MkContainer;
import com.jcabi.http.mock.MkGrizzlyContainer;
import com.jcabi.http.mock.MkQuery;
import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.RestResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.Map;
import java.util.zip.GZIPOutputStream;
import javax.ws.rs.core.HttpHeaders;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsAnything;
import org.junit.Test;

/**
 * Test case for {@link LastModifiedCachingWire}.
 * @author Alan Evans (thealanevans@gmail.com)
 * @version $Id$
 */
public final class GzipBodyTest {

    /**
     * JdkRequest can unzip bodies in GET requests that respond with gzip
     *  stream.
     * @throws Exception If fails
     */
    @Test
    public void unzipsGzipBodyStream() throws Exception {
        final String body = "Test body";
        final Map<String, String> headers = Collections.singletonMap(
            HttpHeaders.CONTENT_ENCODING,
            "gzip"
        );
        final MkContainer container = new MkGrizzlyContainer()
            .next(
                new MkAnswer.Simple(
                    HttpURLConnection.HTTP_OK,
                    headers.entrySet(),
                    gzip(body.getBytes())
                )
            )
            .start();
        try {
            final Request req = new JdkRequest(container.home());
            req.fetch().as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_OK)
                .assertBody(
                    Matchers.equalTo(body)
                );
        } finally {
            container.stop();
        }
    }

    /**
     * Creates a gzip byte array of the supplied byte array
     * @param source
     * @return
     * @throws IOException
     */
    private byte[] gzip(final byte[] source) throws IOException {
        final InputStream original = new ByteArrayInputStream(source);
        final ByteArrayOutputStream zipped = new ByteArrayOutputStream();
        final OutputStream compressor = new GZIPOutputStream(zipped);
        ByteStreams.copy(original, compressor);
        compressor.close();
        original.close();
        final byte[] output = zipped.toByteArray();
        zipped.close();
        return output;
    }

    @Test
    public void cachesGetRequest() throws Exception {
        final String body = "Test body";
        final Map<String, String> headers = Collections.singletonMap(
            HttpHeaders.CONTENT_TYPE,
            "Wed, 15 Nov 1995 04:58:08 GMT"
        );
        final MkContainer container = new MkGrizzlyContainer()
            .next(
                new MkAnswer.Simple(
                    HttpURLConnection.HTTP_OK,
                    headers.entrySet(),
                    body.getBytes()
                )
            )
            .next(
                new MkAnswer.Simple(HttpURLConnection.HTTP_NOT_MODIFIED),
                new IsAnything<MkQuery>(),
                Tv.TEN
            ).start();
        try {
            final Request req = new JdkRequest(container.home())
                .through(LastModifiedCachingWire.class);
            for (int idx = 0; idx < Tv.TEN; ++idx) {
                req.fetch().as(RestResponse.class)
                    .assertStatus(HttpURLConnection.HTTP_OK)
                    .assertBody(
                        Matchers.equalTo(body)
                    );
            }
            MatcherAssert.assertThat(
                container.queries(), Matchers.equalTo(Tv.TEN)
            );
        } finally {
            container.stop();
        }
    }
}
