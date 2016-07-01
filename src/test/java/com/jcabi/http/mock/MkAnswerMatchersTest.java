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
package com.jcabi.http.mock;

import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.Map;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

import javax.ws.rs.core.HttpHeaders;

/**
 * Test case for {@link MkAnswerMatchers}.
 * @author Carlos Miranda (miranda.cma@gmail.com)
 * @version $Id$
 */
public final class MkAnswerMatchersTest {

    /**
     * MkAnswerMatchers should be able to match MkAnswer body.
     */
    @Test
    public void canMatchBody() {
        final String body = "Hello \u20ac!";
        final MkAnswer query = Mockito.mock(MkAnswer.class);
        Mockito.doReturn(body).when(query).body();
        MatcherAssert.assertThat(
            query,
            MkAnswerMatchers.hasBody(
                Matchers.is(body)
            )
        );
    }

    /**
     * MkAnswerMatchers should be able to match MkAnswer header.
     */
    @Test
    public void canMatchHeader() {
        final String header = "Content-Type";
        final String value = "application/json";
        final MkAnswer query = Mockito.mock(MkAnswer.class);
        Mockito.doReturn(
            Collections.singletonMap(header, Collections.singletonList(value))
        ).when(query).headers();
        MatcherAssert.assertThat(
            query,
            MkAnswerMatchers.hasHeader(
                header,
                Matchers.contains(value)
            )
        );
    }

    /**
     * MkAnswerMatchers can match MkAnswer binary body.
     */
    @Test
    public void canMatchBinaryBody() {
        final byte[] body = getAllByteValues();
        final Map<String, String> headers = Collections.emptyMap();
        MkAnswer answer = new MkAnswer.Simple(
                HttpURLConnection.HTTP_OK,
                headers.entrySet(),
                body);
        MatcherAssert.assertThat(
                answer,
                MkAnswerMatchers.hasBinaryBody(
                        Matchers.is(body)
                )
        );
    }

    /**
     * Gets all 256 byte values in an array.
     * @return An array containing all bytes values.
     */
    private byte[] getAllByteValues() {
        byte[] bytes = new byte[255];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) i;
        }
        return bytes;
    }
}
