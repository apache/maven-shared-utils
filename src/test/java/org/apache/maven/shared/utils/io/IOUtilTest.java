/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.maven.shared.utils.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static java.nio.charset.StandardCharsets.UTF_16;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings("deprecation")
class IOUtilTest {

    private static final long INFINITE_LOOP_TIMEOUT = 500;

    @Test
    void closeReaderWithNull() throws Exception {
        IOUtil.close((Reader) null);
    }

    @Test
    void closeWriterWithNull() throws Exception {
        IOUtil.close((Writer) null);
    }

    @Test
    void closeInputStreamWithNull() throws Exception {
        IOUtil.close(nullInputStream());
    }

    @Test
    void closeOutputStreamWithNull() throws Exception {
        IOUtil.close(nullOutputStream());
    }

    @Test
    void closeReaderWithIOE() throws Exception {
        IOUtil.close(new BufferedReader(new StringReader(emptyString())) {
            @Override
            public void close() throws IOException {
                super.close();
                throw new IOException("don't bomb out");
            }
        });
    }

    @Test
    void closeWriterWithIOE() throws Exception {
        IOUtil.close(new BufferedWriter(new StringWriter()) {
            @Override
            public void close() throws IOException {
                super.close();
                throw new IOException("don't bomb out");
            }
        });
    }

    @Test
    void closeInputStreamWithIOE() throws Exception {
        IOUtil.close(new BufferedInputStream(emptyInputStream()) {
            @Override
            public void close() throws IOException {
                super.close();
                throw new IOException("don't bomb out");
            }
        });
    }

    @Test
    void closeOutputStreamWithIOE() throws Exception {
        IOUtil.close(new BufferedOutputStream(new ByteArrayOutputStream()) {
            @Override
            public void close() throws IOException {
                super.close();
                throw new IOException("don't bomb out");
            }
        });
    }

    @Test
    void closeReaderCloses() throws Exception {
        final AtomicBoolean closed = new AtomicBoolean(false);
        IOUtil.close(new BufferedReader(new StringReader(emptyString())) {
            @Override
            public void close() throws IOException {
                closed.set(true);
                super.close();
            }
        });
        assertThat(closed.get()).isEqualTo(true);
    }

    @Test
    void closeWriterCloses() throws Exception {
        final AtomicBoolean closed = new AtomicBoolean(false);
        IOUtil.close(new BufferedWriter(new StringWriter()) {
            @Override
            public void close() throws IOException {
                closed.set(true);
                super.close();
            }
        });
        assertThat(closed.get()).isEqualTo(true);
    }

    @Test
    void closeInputStreamCloses() throws Exception {
        final AtomicBoolean closed = new AtomicBoolean(false);
        IOUtil.close(new BufferedInputStream(emptyInputStream()) {
            @Override
            public void close() throws IOException {
                closed.set(true);
                super.close();
            }
        });
        assertThat(closed.get()).isEqualTo(true);
    }

    @Test
    void closeOutputStreamCloses() throws Exception {
        final AtomicBoolean closed = new AtomicBoolean(false);
        IOUtil.close(new BufferedOutputStream(new ByteArrayOutputStream()) {
            @Override
            public void close() throws IOException {
                closed.set(true);
                super.close();
            }
        });
        assertThat(closed.get()).isEqualTo(true);
    }

    @Test
    void toByteArrayFromString() throws Exception {
        String probe = "A string \u2345\u00ef";
        assertThat(IOUtil.toByteArray(probe)).isEqualTo(probe.getBytes());
    }

    @Test
    void toByteArrayFromReader() throws Exception {
        String probe = "A string \u2345\u00ef";
        assertThat(IOUtil.toByteArray(new StringReader(probe))).isEqualTo(probe.getBytes());
    }

    @Test
    void toByteArrayFromInputStream() throws Exception {
        String probe = "A string \u2345\u00ef";
        assertThat(IOUtil.toByteArray(new DontCloseByteArrayInputStream(IOUtil.toByteArray(probe))))
                .isEqualTo(probe.getBytes());
    }

    @Test
    void toByteArrayNullString() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toByteArray((String) null));
    }

    @Test
    void toByteArrayNullReader() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toByteArray((Reader) null));
    }

    @Test
    void toByteArrayNullInputStream() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toByteArray(nullInputStream()));
    }

    @Test
    void contentEqualNullNull() throws Exception {
        assertThrows(IOException.class, () -> IOUtil.contentEquals(null, null));
    }

    @Test
    void contentEqualNonNullNull() throws Exception {
        assertThrows(
                IOException.class,
                () -> IOUtil.contentEquals(new DontCloseByteArrayInputStream(emptyByteArray()), null));
    }

    @Test
    void contentEqualNullNonNull() throws Exception {
        assertThrows(
                IOException.class,
                () -> IOUtil.contentEquals(new DontCloseByteArrayInputStream(emptyByteArray()), null));
    }

    @Test
    void contentEqualEmptyEmpty() throws Exception {
        assertThat(IOUtil.contentEquals(
                        new DontCloseByteArrayInputStream(emptyByteArray()),
                        new DontCloseByteArrayInputStream(emptyByteArray())))
                .isEqualTo(true);
    }

    @Test
    void contentEqualNonEmptyEmpty() throws Exception {
        assertThat(IOUtil.contentEquals(
                        new DontCloseByteArrayInputStream(new byte[1]),
                        new DontCloseByteArrayInputStream(emptyByteArray())))
                .isEqualTo(false);
    }

    @Test
    void contentEqualEmptyNonEmpty() throws Exception {
        assertThat(IOUtil.contentEquals(
                        new DontCloseByteArrayInputStream(emptyByteArray()),
                        new DontCloseByteArrayInputStream(new byte[1])))
                .isEqualTo(false);
    }

    @Test
    void contentEqualNonEmptyNonEmpty() throws Exception {
        assertThat(IOUtil.contentEquals(
                        new DontCloseByteArrayInputStream(new byte[1]), new DontCloseByteArrayInputStream(new byte[1])))
                .isEqualTo(true);
    }

    @Test
    void contentEqualMostlySame() throws Exception {
        assertThat(IOUtil.contentEquals(
                        new DontCloseByteArrayInputStream(new byte[] {1, 2, 3, 4, 5, 6}),
                        new DontCloseByteArrayInputStream(new byte[] {1, 2, 3, 4, 5, 7})))
                .isEqualTo(false);
    }

    @Test
    void contentEqualLargeSame() throws Exception {
        assertThat(IOUtil.contentEquals(
                        new DontCloseByteArrayInputStream(new byte[8192]),
                        new DontCloseByteArrayInputStream(new byte[8192])))
                .isEqualTo(true);
    }

    @Test
    void contentEqualLargeDifferent() throws Exception {
        byte[] buf = new byte[8192];
        buf[8191] = 1;
        assertThat(IOUtil.contentEquals(
                        new DontCloseByteArrayInputStream(new byte[8192]), new DontCloseByteArrayInputStream(buf)))
                .isEqualTo(false);
    }

    @Test
    void toStringNullByteArray() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullByteArray()));
    }

    @Test
    void toStringEmptyByteArray() throws Exception {
        assertThat(IOUtil.toString(emptyByteArray())).isEqualTo(emptyString());
    }

    @Test
    void toStringByteArray() throws Exception {
        String probe = "A string \u2345\u00ef";
        assertThat(IOUtil.toString(probe.getBytes()).getBytes()).isEqualTo(probe.getBytes());
    }

    @Test
    void toStringNullByteArrayNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullByteArray(), -1));
    }

    @Test
    void toStringEmptyByteArrayNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> assertThat(IOUtil.toString(emptyByteArray(), -1))
                .isEqualTo(emptyString()));
    }

    @Test
    void toStringByteArrayNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> {
            String probe = "A string \u2345\u00ef";
            assertThat(IOUtil.toString(probe.getBytes(), -1)).isEqualTo(probe);
        });
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void toStringNullByteArrayZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullByteArray(), 0));
    }

    @Test
    void toStringNullByteArrayPosBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullByteArray(), 1));
    }

    @Test
    void toStringEmptyByteArrayPosBufSz() throws Exception {
        assertThat(IOUtil.toString(emptyByteArray(), 1)).isEqualTo(emptyString());
    }

    @Test
    void toStringByteArrayPosBufSz() throws Exception {
        String probe = "A string \u2345\u00ef";
        assertThat(IOUtil.toString(probe.getBytes(), 1).getBytes()).isEqualTo(probe.getBytes());
    }

    @Test
    void toStringNullByteArrayNullEncoding() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullByteArray(), null));
    }

    @Test
    void toStringEmptyByteArrayNullEncoding() throws Exception {
        assertThrows(NullPointerException.class, () -> assertThat(IOUtil.toString(emptyByteArray(), null))
                .isEqualTo(emptyString()));
    }

    @Test
    void toStringByteArrayNullEncoding() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string \u2345\u00ef";
            assertThat(IOUtil.toString(probe.getBytes(), null).getBytes()).isEqualTo(probe.getBytes());
        });
    }

    @Test
    void toStringNullByteArrayJunkEncoding() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullByteArray(), "junk"));
    }

    @Test
    void toStringEmptyByteArrayJunkEncoding() throws Exception {
        assertThrows(UnsupportedEncodingException.class, () -> assertThat(IOUtil.toString(emptyByteArray(), "junk"))
                .isEqualTo(emptyString()));
    }

    @Test
    void toStringByteArrayJunkEncoding() throws Exception {
        assertThrows(UnsupportedEncodingException.class, () -> {
            String probe = "A string \u2345\u00ef";
            assertThat(IOUtil.toString(probe.getBytes(), "junk").getBytes()).isEqualTo(probe.getBytes());
        });
    }

    @Test
    void toStringNullByteArrayValidEncoding() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullByteArray(), "utf-16"));
    }

    @Test
    void toStringEmptyByteArrayValidEncoding() throws Exception {
        assertThat(IOUtil.toString(emptyByteArray(), "utf-16")).isEqualTo(emptyString());
    }

    @Test
    void toStringByteArrayValidEncoding() throws Exception {
        String probe = "A string \u2345\u00ef";
        assertThat(IOUtil.toString(probe.getBytes(UTF_16), "utf-16").getBytes(UTF_8))
                .isEqualTo(probe.getBytes(UTF_8));
    }

    @Test
    void toStringNullByteArrayNullEncodingNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullByteArray(), null, -1));
    }

    @Test
    void toStringEmptyByteArrayNullEncodingNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> assertThat(IOUtil.toString(emptyByteArray(), null, -1))
                .isEqualTo(emptyString()));
    }

    @Test
    void toStringByteArrayNullEncodingNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string \u2345\u00ef";
            assertThat(IOUtil.toString(probe.getBytes(), null, -1).getBytes()).isEqualTo(probe.getBytes());
        });
    }

    @Test
    void toStringNullByteArrayJunkEncodingNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullByteArray(), "junk", -1));
    }

    @Test
    void toStringEmptyByteArrayJunkEncodingNegBufSz() throws Exception {
        assertThrows(UnsupportedEncodingException.class, () -> assertThat(IOUtil.toString(emptyByteArray(), "junk", -1))
                .isEqualTo(emptyString()));
    }

    @Test
    void toStringByteArrayJunkEncodingNegBufSz() throws Exception {
        assertThrows(UnsupportedEncodingException.class, () -> {
            String probe = "A string \u2345\u00ef";
            assertThat(IOUtil.toString(probe.getBytes(), "junk", -1).getBytes()).isEqualTo(probe.getBytes());
        });
    }

    @Test
    void toStringNullByteArrayValidEncodingNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullByteArray(), "utf-16", -1));
    }

    @Test
    void toStringEmptyByteArrayValidEncodingNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> assertThat(IOUtil.toString(emptyByteArray(), "utf-16", -1))
                .isEqualTo(emptyString()));
    }

    @Test
    void toStringByteArrayValidEncodingNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> {
            String probe = "A string \u2345\u00ef";
            assertThat(IOUtil.toString(probe.getBytes(UTF_16), "utf-16", -1).getBytes(UTF_8))
                    .isEqualTo(probe.getBytes(UTF_8));
        });
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void toStringNullByteArrayNullEncodingZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullByteArray(), null, 0));
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void toStringEmptyByteArrayNullEncodingZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> assertThat(IOUtil.toString(emptyByteArray(), null, 0))
                .isEqualTo(emptyString()));
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void toStringByteArrayNullEncodingZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string \u2345\u00ef";
            assertThat(IOUtil.toString(probe.getBytes(), null, 0).getBytes()).isEqualTo(probe.getBytes());
        });
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void toStringNullByteArrayJunkEncodingZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullByteArray(), "junk", 0));
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void toStringEmptyByteArrayJunkEncodingZeroBufSz() throws Exception {
        assertThrows(UnsupportedEncodingException.class, () -> assertThat(IOUtil.toString(emptyByteArray(), "junk", 0))
                .isEqualTo(emptyString()));
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void toStringByteArrayJunkEncodingZeroBufSz() throws Exception {
        assertThrows(UnsupportedEncodingException.class, () -> {
            String probe = "A string \u2345\u00ef";
            assertThat(IOUtil.toString(probe.getBytes(), "junk", 0).getBytes()).isEqualTo(probe.getBytes());
        });
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void toStringNullByteArrayValidEncodingZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullByteArray(), "utf-16", 0));
    }

    /*
     * copy(byte[],OutputStream)
     */

    @Test
    void copyNullByteArrayNullOutputStream() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullByteArray(), nullOutputStream()));
    }

    @Test
    void copyNullByteArrayValidOutputStream() throws Exception {
        assertThrows(
                NullPointerException.class, () -> IOUtil.copy(nullByteArray(), new DontCloseByteArrayOutputStream()));
    }

    @Test
    void copyEmptyByteArrayNullOutputStream() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyByteArray(), nullOutputStream()));
    }

    @Test
    void copyEmptyByteArrayValidOutputStream() throws Exception {
        IOUtil.copy(emptyByteArray(), new DontCloseByteArrayOutputStream());
    }

    @Test
    void copyByteArrayValidOutputStream() throws Exception {
        ByteArrayOutputStream outputStream = new DontCloseByteArrayOutputStream();
        byte[] input = {1, 2, 3, 4, 5, 6};
        IOUtil.copy(input, outputStream);
        assertThat(outputStream.toByteArray()).isEqualTo(input);
    }

    /*
     * copy(byte[],OutputStream,int)
     */

    @Test
    void copyNullByteArrayNullOutputStreamNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullByteArray(), nullOutputStream()));
    }

    @Test
    void copyNullByteArrayValidOutputStreamNegBufSz() throws Exception {
        assertThrows(
                NullPointerException.class, () -> IOUtil.copy(nullByteArray(), new DontCloseByteArrayOutputStream()));
    }

    @Test
    void copyEmptyByteArrayNullOutputStreamNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyByteArray(), nullOutputStream()));
    }

    @Test
    void copyEmptyByteArrayValidOutputStreamNegBufSz() throws Exception {
        IOUtil.copy(emptyByteArray(), new DontCloseByteArrayOutputStream());
    }

    @Test
    void copyByteArrayValidOutputStreamNegBufSz() throws Exception {
        ByteArrayOutputStream outputStream = new DontCloseByteArrayOutputStream();
        byte[] input = {1, 2, 3, 4, 5, 6};
        IOUtil.copy(input, outputStream);
        assertThat(outputStream.toByteArray()).isEqualTo(input);
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void copyNullByteArrayNullOutputStreamZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullByteArray(), nullOutputStream()));
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void copyNullByteArrayValidOutputStreamZeroBufSz() throws Exception {
        assertThrows(
                NullPointerException.class, () -> IOUtil.copy(nullByteArray(), new DontCloseByteArrayOutputStream()));
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void copyEmptyByteArrayNullOutputStreamZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyByteArray(), nullOutputStream()));
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void copyEmptyByteArrayValidOutputStreamZeroBufSz() throws Exception {
        IOUtil.copy(emptyByteArray(), new DontCloseByteArrayOutputStream());
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void copyByteArrayValidOutputStreamZeroBufSz() throws Exception {
        ByteArrayOutputStream outputStream = new DontCloseByteArrayOutputStream();
        byte[] input = {1, 2, 3, 4, 5, 6};
        IOUtil.copy(input, outputStream);
        assertThat(outputStream.toByteArray()).isEqualTo(input);
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void copyNullByteArrayNullOutputStreamPosBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullByteArray(), nullOutputStream()));
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void copyNullByteArrayValidOutputStreamPosBufSz() throws Exception {
        assertThrows(
                NullPointerException.class, () -> IOUtil.copy(nullByteArray(), new DontCloseByteArrayOutputStream()));
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void copyEmptyByteArrayNullOutputStreamPosBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyByteArray(), nullOutputStream()));
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void copyEmptyByteArrayValidOutputStreamPosBufSz() throws Exception {
        IOUtil.copy(emptyByteArray(), new DontCloseByteArrayOutputStream());
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void copyByteArrayValidOutputStreamPosBufSz() throws Exception {
        ByteArrayOutputStream outputStream = new DontCloseByteArrayOutputStream();
        byte[] input = {1, 2, 3, 4, 5, 6};
        IOUtil.copy(input, outputStream);
        assertThat(outputStream.toByteArray()).isEqualTo(input);
    }

    @Test
    void copyNullInputStreamNullOutputStream() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullInputStream(), nullOutputStream()));
    }

    @Test
    void copyNullInputStreamValidOutputStream() throws Exception {
        assertThrows(
                NullPointerException.class, () -> IOUtil.copy(nullInputStream(), new DontCloseByteArrayOutputStream()));
    }

    @Test
    void copyEmptyInputStreamNullOutputStream() throws Exception {
        IOUtil.copy(new DontCloseByteArrayInputStream(emptyByteArray()), nullOutputStream());
    }

    @Test
    void copyEmptyInputStreamValidOutputStream() throws Exception {
        IOUtil.copy(new DontCloseByteArrayInputStream(emptyByteArray()), new DontCloseByteArrayOutputStream());
    }

    @Test
    void copyInputStreamValidOutputStream() throws Exception {
        ByteArrayOutputStream outputStream = new DontCloseByteArrayOutputStream();
        byte[] input = {1, 2, 3, 4, 5, 6};
        IOUtil.copy(new DontCloseByteArrayInputStream(input), outputStream);
        assertThat(outputStream.toByteArray()).isEqualTo(input);
    }

    @Test
    void copyNullInputStreamNullOutputStreamNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> IOUtil.copy(nullInputStream(), nullOutputStream(), -1));
    }

    @Test
    void copyNullInputStreamValidOutputStreamNegBufSz() throws Exception {
        assertThrows(
                NegativeArraySizeException.class,
                () -> IOUtil.copy(nullInputStream(), new DontCloseByteArrayOutputStream(), -1));
    }

    @Test
    void copyEmptyInputStreamNullOutputStreamNegBufSz() throws Exception {
        assertThrows(
                NegativeArraySizeException.class,
                () -> IOUtil.copy(new DontCloseByteArrayInputStream(emptyByteArray()), nullOutputStream(), -1));
    }

    @Test
    void copyEmptyInputStreamValidOutputStreamNegBufSz() throws Exception {
        assertThrows(
                NegativeArraySizeException.class,
                () -> IOUtil.copy(
                        new DontCloseByteArrayInputStream(emptyByteArray()), new DontCloseByteArrayOutputStream(), -1));
    }

    @Test
    void copyInputStreamValidOutputStreamNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> {
            ByteArrayOutputStream outputStream = new DontCloseByteArrayOutputStream();
            byte[] input = {1, 2, 3, 4, 5, 6};
            IOUtil.copy(new DontCloseByteArrayInputStream(input), outputStream, -1);
            assertThat(outputStream.toByteArray()).isEqualTo(input);
        });
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void copyNullInputStreamNullOutputStreamZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullInputStream(), nullOutputStream(), 0));
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void copyNullInputStreamValidOutputStreamZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullInputStream(), new ByteArrayOutputStream(), 0));
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void copyEmptyInputStreamNullOutputStreamZeroBufSz() throws Exception {
        IOUtil.copy(new DontCloseByteArrayInputStream(emptyByteArray()), nullOutputStream(), 0);
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void copyEmptyInputStreamValidOutputStreamZeroBufSz() throws Exception {
        IOUtil.copy(new DontCloseByteArrayInputStream(emptyByteArray()), new DontCloseByteArrayOutputStream(), 0);
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void copyNullInputStreamNullOutputStreamPosBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullInputStream(), nullOutputStream(), 1));
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void copyNullInputStreamValidOutputStreamPosBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullInputStream(), new ByteArrayOutputStream(), 1));
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void copyEmptyInputStreamNullOutputStreamPosBufSz() throws Exception {
        IOUtil.copy(new DontCloseByteArrayInputStream(emptyByteArray()), nullOutputStream(), 1);
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void copyEmptyInputStreamValidOutputStreamPosBufSz() throws Exception {
        IOUtil.copy(new DontCloseByteArrayInputStream(emptyByteArray()), new DontCloseByteArrayOutputStream(), 1);
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void copyInputStreamValidOutputStreamPosBufSz() throws Exception {
        ByteArrayOutputStream outputStream = new DontCloseByteArrayOutputStream();
        byte[] input = {1, 2, 3, 4, 5, 6};
        IOUtil.copy(new DontCloseByteArrayInputStream(input), outputStream, 1);
        assertThat(outputStream.toByteArray()).isEqualTo(input);
    }

    @Test
    void toStringNullInputStream() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullInputStream()));
    }

    @Test
    void toStringEmptyInputStream() throws Exception {
        assertThat(IOUtil.toString(emptyInputStream())).isEqualTo(emptyString());
    }

    @Test
    void toStringInputStream() throws Exception {
        String probe = "A string \u2345\u00ef";
        assertThat(IOUtil.toString(new ByteArrayInputStream(probe.getBytes())).getBytes())
                .isEqualTo(probe.getBytes());
    }

    @Test
    void toStringNullInputStreamNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullInputStream(), -1));
    }

    @Test
    void toStringEmptyInputStreamNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> assertThat(IOUtil.toString(emptyInputStream(), -1))
                .isEqualTo(emptyString()));
    }

    @Test
    void toStringInputStreamNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> {
            String probe = "A string \u2345\u00ef";
            assertThat(IOUtil.toString(new ByteArrayInputStream(probe.getBytes()), -1))
                    .isEqualTo(probe);
        });
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void toStringNullInputStreamZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullInputStream(), 0));
    }

    @Test
    void toStringNullInputStreamPosBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullInputStream(), 1));
    }

    @Test
    void toStringEmptyInputStreamPosBufSz() throws Exception {
        assertThat(IOUtil.toString(emptyInputStream(), 1)).isEqualTo(emptyString());
    }

    @Test
    void toStringInputStreamPosBufSz() throws Exception {
        String probe = "A string \u2345\u00ef";
        assertThat(IOUtil.toString(new ByteArrayInputStream(probe.getBytes()), 1)
                        .getBytes())
                .isEqualTo(probe.getBytes());
    }

    @Test
    void toStringNullInputStreamNullEncoding() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullInputStream(), null));
    }

    @Test
    void toStringEmptyInputStreamNullEncoding() throws Exception {
        assertThrows(NullPointerException.class, () -> assertThat(IOUtil.toString(emptyInputStream(), null))
                .isEqualTo(emptyString()));
    }

    @Test
    void toStringInputStreamNullEncoding() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string \u2345\u00ef";
            assertThat(IOUtil.toString(new ByteArrayInputStream(probe.getBytes()), null)
                            .getBytes())
                    .isEqualTo(probe.getBytes());
        });
    }

    @Test
    void toStringNullInputStreamJunkEncoding() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullInputStream(), "junk"));
    }

    @Test
    void toStringEmptyInputStreamJunkEncoding() throws Exception {
        assertThrows(UnsupportedEncodingException.class, () -> assertThat(IOUtil.toString(emptyInputStream(), "junk"))
                .isEqualTo(emptyString()));
    }

    @Test
    void toStringInputStreamJunkEncoding() throws Exception {
        assertThrows(UnsupportedEncodingException.class, () -> {
            String probe = "A string \u2345\u00ef";
            assertThat(IOUtil.toString(new ByteArrayInputStream(probe.getBytes()), "junk")
                            .getBytes())
                    .isEqualTo(probe.getBytes());
        });
    }

    @Test
    void toStringNullInputStreamValidEncoding() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullInputStream(), "utf-16"));
    }

    @Test
    void toStringEmptyInputStreamValidEncoding() throws Exception {
        assertThat(IOUtil.toString(emptyInputStream(), "utf-16")).isEqualTo(emptyString());
    }

    @Test
    void toStringInputStreamValidEncoding() throws Exception {
        String probe = "A string \u2345\u00ef";
        assertThat(IOUtil.toString(new ByteArrayInputStream(probe.getBytes(UTF_16)), "utf-16")
                        .getBytes(UTF_8))
                .isEqualTo(probe.getBytes(UTF_8));
    }

    @Test
    void toStringNullInputStreamNullEncodingNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullInputStream(), null, -1));
    }

    @Test
    void toStringEmptyInputStreamNullEncodingNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> assertThat(IOUtil.toString(emptyInputStream(), null, -1))
                .isEqualTo(emptyString()));
    }

    @Test
    void toStringInputStreamNullEncodingNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string \u2345\u00ef";
            assertThat(IOUtil.toString(new ByteArrayInputStream(probe.getBytes()), null, -1)
                            .getBytes())
                    .isEqualTo(probe.getBytes());
        });
    }

    @Test
    void toStringNullInputStreamJunkEncodingNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullInputStream(), "junk", -1));
    }

    @Test
    void toStringEmptyInputStreamJunkEncodingNegBufSz() throws Exception {
        assertThrows(
                UnsupportedEncodingException.class, () -> assertThat(IOUtil.toString(emptyInputStream(), "junk", -1))
                        .isEqualTo(emptyString()));
    }

    @Test
    void toStringInputStreamJunkEncodingNegBufSz() throws Exception {
        assertThrows(UnsupportedEncodingException.class, () -> {
            String probe = "A string \u2345\u00ef";
            assertThat(IOUtil.toString(new ByteArrayInputStream(probe.getBytes()), "junk", -1)
                            .getBytes())
                    .isEqualTo(probe.getBytes());
        });
    }

    @Test
    void toStringNullInputStreamValidEncodingNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullInputStream(), "utf-16", -1));
    }

    @Test
    void toStringEmptyInputStreamValidEncodingNegBufSz() throws Exception {
        assertThrows(
                NegativeArraySizeException.class, () -> assertThat(IOUtil.toString(emptyInputStream(), "utf-16", -1))
                        .isEqualTo(emptyString()));
    }

    @Test
    void toStringInputStreamValidEncodingNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> {
            String probe = "A string \u2345\u00ef";
            assertThat(IOUtil.toString(new ByteArrayInputStream(probe.getBytes(UTF_16)), "utf-16", -1)
                            .getBytes(UTF_8))
                    .isEqualTo(probe.getBytes(UTF_8));
        });
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void toStringNullInputStreamNullEncodingZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullInputStream(), null, 0));
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void toStringEmptyInputStreamNullEncodingZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> assertThat(IOUtil.toString(emptyInputStream(), null, 0))
                .isEqualTo(emptyString()));
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void toStringInputStreamNullEncodingZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string \u2345\u00ef";
            assertThat(IOUtil.toString(new ByteArrayInputStream(probe.getBytes()), null, 0)
                            .getBytes())
                    .isEqualTo(probe.getBytes());
        });
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void toStringNullInputStreamJunkEncodingZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullInputStream(), "junk", 0));
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void toStringEmptyInputStreamJunkEncodingZeroBufSz() throws Exception {
        assertThrows(
                UnsupportedEncodingException.class,
                () -> assertThat(IOUtil.toString(emptyInputStream(), "junk", 0)).isEqualTo(emptyString()));
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void toStringInputStreamJunkEncodingZeroBufSz() throws Exception {
        assertThrows(UnsupportedEncodingException.class, () -> {
            String probe = "A string \u2345\u00ef";
            assertThat(IOUtil.toString(new ByteArrayInputStream(probe.getBytes()), "junk", 0)
                            .getBytes())
                    .isEqualTo(probe.getBytes());
        });
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void toStringNullInputStreamValidEncodingZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullInputStream(), "utf-16", 0));
    }

    /*
     * copy(InputStream,Writer)
     */

    @Test
    void copyNullInputStreamNullWriter() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullInputStream(), nullWriter()));
    }

    @Test
    void copyEmptyInputStreamNullWriter() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyInputStream(), nullWriter()));
    }

    @Test
    void copyEmptyInputStreamValidWriter() throws Exception {
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy(emptyInputStream(), writer);
        assertThat(writer.toString()).isEqualTo(emptyString());
    }

    @Test
    void copyInputStreamNullWriter() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string \u2345\u00ef";
            IOUtil.copy(new ByteArrayInputStream(probe.getBytes()), nullWriter());
        });
    }

    @Test
    void copyInputStreamValidWriter() throws Exception {
        String probe = "A string \u2345\u00ef";
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy(new ByteArrayInputStream(probe.getBytes()), writer);
        assertThat(writer.toString().getBytes()).isEqualTo(probe.getBytes());
    }

    /*
     * copy(InputStream,Writer,int)
     */

    @Test
    void copyNullInputStreamNullWriterNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullInputStream(), nullWriter(), -1));
    }

    @Test
    void copyEmptyInputStreamNullWriterNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> IOUtil.copy(emptyInputStream(), nullWriter(), -1));
    }

    @Test
    void copyEmptyInputStreamValidWriterNegBufSz() throws Exception {
        assertThrows(
                NegativeArraySizeException.class,
                () -> IOUtil.copy(emptyInputStream(), new DontCloseStringWriter(), -1));
    }

    @Test
    void copyInputStreamNullWriterNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> {
            String probe = "A string \u2345\u00ef";
            IOUtil.copy(new ByteArrayInputStream(probe.getBytes()), nullWriter(), -1);
        });
    }

    @Test
    void copyInputStreamValidWriterNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> {
            String probe = "A string \u2345\u00ef";
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(new ByteArrayInputStream(probe.getBytes()), writer, -1);
            assertThat(writer.toString().getBytes()).isEqualTo(probe.getBytes());
        });
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void copyNullInputStreamNullWriterZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullInputStream(), nullWriter(), 0));
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void copyNullInputStreamValidWriterZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullInputStream(), new DontCloseStringWriter(), 0));
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void copyEmptyInputStreamNullWriterZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyInputStream(), nullWriter(), 0));
    }

    @Test
    void copyNullInputStreamNullWriterPosBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullInputStream(), nullWriter(), 1));
    }

    @Test
    void copyNullInputStreamValidWriterPosBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullInputStream(), new DontCloseStringWriter(), 1));
    }

    @Test
    void copyEmptyInputStreamNullWriterPosBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyInputStream(), nullWriter(), 1));
    }

    @Test
    void copyEmptyInputStreamValidWriterPosBufSz() throws Exception {
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy(emptyInputStream(), writer, 1);
        assertThat(writer.toString()).isEqualTo(emptyString());
    }

    @Test
    void copyInputStreamValidWriterPosBufSz() throws Exception {
        String probe = "A string \u2345\u00ef";
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy(new ByteArrayInputStream(probe.getBytes()), writer, 1);
        assertThat(writer.toString().getBytes()).isEqualTo(probe.getBytes());
    }

    /*
     * copy(InputStream,Writer,String)
     */

    @Test
    void copyNullInputStreamNullWriterNullEncoding() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullInputStream(), nullWriter(), null));
    }

    @Test
    void copyNullInputStreamValidWriterNullEncoding() throws Exception {
        assertThrows(
                NullPointerException.class, () -> IOUtil.copy(nullInputStream(), new DontCloseStringWriter(), null));
    }

    @Test
    void copyEmptyInputStreamNullWriterNullEncoding() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyInputStream(), nullWriter(), null));
    }

    @Test
    void copyEmptyInputStreamValidWriterNullEncoding() throws Exception {
        assertThrows(
                NullPointerException.class, () -> IOUtil.copy(emptyInputStream(), new DontCloseStringWriter(), null));
    }

    @Test
    void copyInputStreamNullEncoding() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string \u2345\u00ef";
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(new ByteArrayInputStream(probe.getBytes()), writer, null);
            assertThat(writer.toString().getBytes()).isEqualTo(probe.getBytes());
        });
    }

    @Test
    void copyNullInputStreamNullWriterJunkEncoding() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullInputStream(), nullWriter(), "junk"));
    }

    @Test
    void copyNullInputStreamValidWriterJunkEncoding() throws Exception {
        assertThrows(
                NullPointerException.class, () -> IOUtil.copy(nullInputStream(), new DontCloseStringWriter(), "junk"));
    }

    @Test
    void copyEmptyInputStreamNullWriterJunkEncoding() throws Exception {
        assertThrows(UnsupportedEncodingException.class, () -> IOUtil.copy(emptyInputStream(), nullWriter(), "junk"));
    }

    @Test
    void copyEmptyInputStreamValidWriterJunkEncoding() throws Exception {
        assertThrows(
                UnsupportedEncodingException.class,
                () -> IOUtil.copy(emptyInputStream(), new DontCloseStringWriter(), "junk"));
    }

    @Test
    void copyInputStreamNullWriterJunkEncoding() throws Exception {
        assertThrows(UnsupportedEncodingException.class, () -> {
            String probe = "A string \u2345\u00ef";
            IOUtil.copy(new ByteArrayInputStream(probe.getBytes()), nullWriter(), "junk");
        });
    }

    @Test
    void copyInputStreamValidWriterJunkEncoding() throws Exception {
        assertThrows(UnsupportedEncodingException.class, () -> {
            String probe = "A string \u2345\u00ef";
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(new ByteArrayInputStream(probe.getBytes()), writer, "junk");
            assertThat(writer.toString().getBytes()).isEqualTo(probe.getBytes());
        });
    }

    @Test
    void copyNullInputStreamNullWriterValidEncoding() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullInputStream(), nullWriter(), "utf-16"));
    }

    @Test
    void copyEmptyInputStreamNullWriterValidEncoding() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyInputStream(), nullWriter(), "utf-16"));
    }

    @Test
    void copyNullInputStreamValidWriterValidEncoding() throws Exception {
        assertThrows(
                NullPointerException.class,
                () -> IOUtil.copy(nullInputStream(), new DontCloseStringWriter(), "utf-16"));
    }

    @Test
    void copyEmptyInputStreamValidWriterValidEncoding() throws Exception {
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy(emptyInputStream(), writer, "utf-16");
        assertThat(writer.toString()).isEqualTo(emptyString());
    }

    @Test
    void copyInputStreamNullWriterValidEncoding() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string \u2345\u00ef";
            IOUtil.copy(new ByteArrayInputStream(probe.getBytes("utf-16")), nullWriter(), "utf-16");
        });
    }

    @Test
    void copyInputStreamValidWriterValidEncoding() throws Exception {
        String probe = "A string \u2345\u00ef";
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy(new ByteArrayInputStream(probe.getBytes(UTF_16)), writer, "utf-16");
        assertThat(writer.toString().getBytes(UTF_8)).isEqualTo(probe.getBytes(UTF_8));
    }

    /*
     * copy(InputStream,Writer,String,int)
     */

    @Test
    void copyNullInputStreamNullWriterNullEncodingNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullInputStream(), nullWriter(), null, -1));
    }

    @Test
    void copyNullInputStreamValidWriterNullEncodingNegBufSz() throws Exception {
        assertThrows(
                NullPointerException.class,
                () -> IOUtil.copy(nullInputStream(), new DontCloseStringWriter(), null, -1));
    }

    @Test
    void copyEmptyInputStreamNullWriterNullEncodingNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyInputStream(), nullWriter(), null, -1));
    }

    @Test
    void copyEmptyInputStreamValidWriterNullEncodingNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(emptyInputStream(), writer, null, -1);
            assertThat(writer.toString()).isEqualTo(emptyString());
        });
    }

    @Test
    void copyInputStreamNullWriterNullEncodingNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string \u2345\u00ef";
            IOUtil.copy(new ByteArrayInputStream(probe.getBytes()), nullWriter(), null, -1);
        });
    }

    @Test
    void copyInputStreamValidWriterNullEncodingNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string \u2345\u00ef";
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(new ByteArrayInputStream(probe.getBytes()), writer, null, -1);
            assertThat(writer.toString().getBytes()).isEqualTo(probe.getBytes());
        });
    }

    @Test
    void copyNullInputStreamNullWriterJunkEncodingNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullInputStream(), nullWriter(), "junk", -1));
    }

    @Test
    void copyNullInputStreamValidWriterJunkEncodingNegBufSz() throws Exception {
        assertThrows(
                NullPointerException.class,
                () -> IOUtil.copy(nullInputStream(), new DontCloseStringWriter(), "junk", -1));
    }

    @Test
    void copyEmptyInputStreamNullWriterJunkEncodingNegBufSz() throws Exception {
        assertThrows(
                UnsupportedEncodingException.class, () -> IOUtil.copy(emptyInputStream(), nullWriter(), "junk", -1));
    }

    @Test
    void copyEmptyInputStreamJunkEncodingNegBufSz() throws Exception {
        assertThrows(UnsupportedEncodingException.class, () -> {
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(emptyInputStream(), writer, "junk", -1);
            assertThat(writer.toString()).isEqualTo(emptyString());
        });
    }

    @Test
    void copyInputStreamNullWriterJunkEncodingNegBufSz() throws Exception {
        assertThrows(UnsupportedEncodingException.class, () -> {
            String probe = "A string \u2345\u00ef";
            IOUtil.copy(new ByteArrayInputStream(probe.getBytes()), nullWriter(), "junk", -1);
        });
    }

    @Test
    void copyInputStreamValidWriterJunkEncodingNegBufSz() throws Exception {
        assertThrows(UnsupportedEncodingException.class, () -> {
            String probe = "A string \u2345\u00ef";
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(new ByteArrayInputStream(probe.getBytes()), writer, "junk", -1);
            assertThat(writer.toString().getBytes()).isEqualTo(probe.getBytes());
        });
    }

    @Test
    void copyNullInputStreamNullWriterValidEncodingNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullInputStream(), nullWriter(), "utf-16", -1));
    }

    @Test
    void copyNullInputStreamValidWriterValidEncodingNegBufSz() throws Exception {
        assertThrows(
                NullPointerException.class,
                () -> IOUtil.copy(nullInputStream(), new DontCloseStringWriter(), "utf-16", -1));
    }

    @Test
    void copyEmptyInputStreamNullWriterValidEncodingNegBufSz() throws Exception {
        assertThrows(
                NegativeArraySizeException.class, () -> IOUtil.copy(emptyInputStream(), nullWriter(), "utf-16", -1));
    }

    @Test
    void copyEmptyInputStreamValidWriterValidEncodingNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> {
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(emptyInputStream(), writer, "utf-16", -1);
            assertThat(writer.toString()).isEqualTo(emptyString());
        });
    }

    @Test
    void copyInputStreamNullWriterValidEncodingNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> {
            String probe = "A string \u2345\u00ef";
            IOUtil.copy(new ByteArrayInputStream(probe.getBytes(UTF_16)), nullWriter(), -1);
        });
    }

    @Test
    void copyInputStreamValidEncodingNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> {
            String probe = "A string \u2345\u00ef";
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(new ByteArrayInputStream(probe.getBytes(UTF_16)), writer, "utf-16", -1);
            assertThat(writer.toString().getBytes(UTF_8)).isEqualTo(probe.getBytes(UTF_8));
        });
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void copyNullInputStreamNullWriterNullEncodingZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullInputStream(), nullWriter(), null, 0));
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void copyNullInputStreamValidWriterNullEncodingZeroBufSz() throws Exception {
        assertThrows(
                NullPointerException.class, () -> IOUtil.copy(nullInputStream(), new DontCloseStringWriter(), null, 0));
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void copyEmptyInputStreamNullWriterNullEncodingZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyInputStream(), nullWriter(), null, 0));
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void copyEmptyInputStreamValidWriterNullEncodingZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(emptyInputStream(), writer, null, 0);
            assertThat(writer.toString()).isEqualTo(emptyString());
        });
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void copyInputStreamNullWriterNullEncodingZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string \u2345\u00ef";
            IOUtil.copy(new ByteArrayInputStream(probe.getBytes()), nullWriter(), null, 0);
        });
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void copyInputStreamValidWriterNullEncodingZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string \u2345\u00ef";
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(new ByteArrayInputStream(probe.getBytes()), writer, null, 0);
            assertThat(writer.toString().getBytes()).isEqualTo(probe.getBytes());
        });
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void copyNullInputStreamNullWriterJunkEncodingZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullInputStream(), nullWriter(), "junk", 0));
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void copyNullInputStreamValidWriterJunkEncodingZeroBufSz() throws Exception {
        assertThrows(
                NullPointerException.class,
                () -> IOUtil.copy(nullInputStream(), new DontCloseStringWriter(), "junk", 0));
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void copyEmptyInputStreamNullWriterJunkEncodingZeroBufSz() throws Exception {
        assertThrows(
                UnsupportedEncodingException.class, () -> IOUtil.copy(emptyInputStream(), nullWriter(), "junk", 0));
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void copyEmptyInputStreamValidWriterJunkEncodingZeroBufSz() throws Exception {
        assertThrows(UnsupportedEncodingException.class, () -> {
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(emptyInputStream(), writer, "junk", 0);
            assertThat(writer.toString()).isEqualTo(emptyString());
        });
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void copyInputStreamNullWriterJunkEncodingZeroBufSz() throws Exception {
        assertThrows(UnsupportedEncodingException.class, () -> {
            String probe = "A string \u2345\u00ef";
            IOUtil.copy(new ByteArrayInputStream(probe.getBytes()), nullWriter(), "junk", 0);
        });
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void copyInputStreamValidWriterJunkEncodingZeroBufSz() throws Exception {
        assertThrows(UnsupportedEncodingException.class, () -> {
            String probe = "A string \u2345\u00ef";
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(new ByteArrayInputStream(probe.getBytes()), writer, "junk", 0);
            assertThat(writer.toString().getBytes()).isEqualTo(probe.getBytes());
        });
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void copyNullInputStreamNullWriterValidEncodingZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullInputStream(), nullWriter(), "utf-16", 0));
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void copyNullInputStreamValidWriterValidEncodingZeroBufSz() throws Exception {
        assertThrows(
                NullPointerException.class,
                () -> IOUtil.copy(nullInputStream(), new DontCloseStringWriter(), "utf-16", 0));
    }

    /*
     * copy(String,Writer)
     */

    @Test
    void copyNullStringNullWriter() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullString(), nullWriter()));
    }

    @Test
    void copyEmptyStringNullWriter() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyString(), nullWriter()));
    }

    @Test
    void copyNullStringValidWriter() throws Exception {
        IOUtil.copy(nullString(), new DontCloseStringWriter());
    }

    @Test
    void copyEmptyStringValidWriter() throws Exception {
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy(emptyString(), writer);
        assertThat(writer.toString()).isEqualTo(emptyString());
    }

    @Test
    void copyStringNullWriter() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string \u2345\u00ef";
            IOUtil.copy(probe, nullWriter());
        });
    }

    @Test
    void copyStringValidWriter() throws Exception {
        String probe = "A string \u2345\u00ef";
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy(probe, writer);
        assertThat(writer.toString()).isEqualTo(probe);
    }

    @Test
    void copyNullStringNullOutputStream() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullString(), nullOutputStream()));
    }

    @Test
    void copyEmptyStringNullOutputStream() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyString(), nullOutputStream()));
    }

    @Test
    void copyNullStringValidOutputStream() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullString(), new DontCloseByteArrayOutputStream()));
    }

    @Test
    void copyEmptyStringValidOutputStream() throws Exception {
        ByteArrayOutputStream OutputStream = new DontCloseByteArrayOutputStream();
        IOUtil.copy(emptyString(), OutputStream);
        assertThat(OutputStream.toByteArray()).isEqualTo(emptyString().getBytes());
    }

    @Test
    void copyStringNullOutputStream() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string \u2345\u00ef";
            IOUtil.copy(probe, nullOutputStream());
        });
    }

    @Test
    void copyStringValidOutputStream() throws Exception {
        String probe = "A string \u2345\u00ef";
        ByteArrayOutputStream OutputStream = new DontCloseByteArrayOutputStream();
        IOUtil.copy(probe, OutputStream);
        assertThat(OutputStream.toByteArray()).isEqualTo(probe.getBytes());
    }

    @Test
    void copyNullStringNullOutputStreamNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullString(), nullOutputStream(), -1));
    }

    @Test
    void copyEmptyStringNullOutputStreamNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyString(), nullOutputStream(), -1));
    }

    @Test
    void copyNullStringValidOutputStreamNegBufSz() throws Exception {
        assertThrows(
                NullPointerException.class, () -> IOUtil.copy(nullString(), new DontCloseByteArrayOutputStream(), -1));
    }

    @Test
    void copyEmptyStringValidOutputStreamNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> {
            ByteArrayOutputStream OutputStream = new DontCloseByteArrayOutputStream();
            IOUtil.copy(emptyString(), OutputStream, -1);
            assertThat(OutputStream.toByteArray()).isEqualTo(emptyString().getBytes());
        });
    }

    @Test
    void copyStringNullOutputStreamNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string \u2345\u00ef";
            IOUtil.copy(probe, nullOutputStream(), -1);
        });
    }

    @Test
    void copyStringValidOutputStreamNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> {
            String probe = "A string \u2345\u00ef";
            ByteArrayOutputStream OutputStream = new DontCloseByteArrayOutputStream();
            IOUtil.copy(probe, OutputStream, -1);
            assertThat(OutputStream.toByteArray()).isEqualTo(probe.getBytes());
        });
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void copyNullStringNullOutputStreamZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullString(), nullOutputStream(), 0));
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void copyEmptyStringNullOutputStreamZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyString(), nullOutputStream(), 0));
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void copyNullStringValidOutputStreamZeroBufSz() throws Exception {
        assertThrows(
                NullPointerException.class, () -> IOUtil.copy(nullString(), new DontCloseByteArrayOutputStream(), 0));
    }

    @Test
    void copyNullStringNullOutputStreamPosBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullString(), nullOutputStream(), 1));
    }

    @Test
    void copyEmptyStringNullOutputStreamPosBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyString(), nullOutputStream(), 1));
    }

    @Test
    void copyNullStringValidOutputStreamPosBufSz() throws Exception {
        assertThrows(
                NullPointerException.class, () -> IOUtil.copy(nullString(), new DontCloseByteArrayOutputStream(), 1));
    }

    @Test
    void copyEmptyStringValidOutputStreamPosBufSz() throws Exception {
        ByteArrayOutputStream OutputStream = new DontCloseByteArrayOutputStream();
        IOUtil.copy(emptyString(), OutputStream, 1);
        assertThat(OutputStream.toByteArray()).isEqualTo(emptyString().getBytes());
    }

    @Test
    void copyStringNullOutputStreamPosBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string \u2345\u00ef";
            IOUtil.copy(probe, nullOutputStream(), 1);
        });
    }

    @Test
    void copyStringValidOutputStreamPosBufSz() throws Exception {
        String probe = "A string \u2345\u00ef";
        ByteArrayOutputStream OutputStream = new DontCloseByteArrayOutputStream();
        IOUtil.copy(probe, OutputStream, 1);
        assertThat(OutputStream.toByteArray()).isEqualTo(probe.getBytes());
    }

    @Test
    void copyNullReaderNullWriter() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullReader(), nullWriter()));
    }

    @Test
    void copyEmptyReaderNullWriter() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyReader(), nullWriter()));
    }

    @Test
    void copyNullReaderValidWriter() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullReader(), new DontCloseStringWriter()));
    }

    @Test
    void copyEmptyReaderValidWriter() throws Exception {
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy(emptyReader(), writer);
        assertThat(writer.toString()).isEqualTo(emptyString());
    }

    @Test
    void copyReaderNullWriter() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string \u2345\u00ef";
            IOUtil.copy(new StringReader(probe), nullWriter());
        });
    }

    @Test
    void copyReaderValidWriter() throws Exception {
        String probe = "A string \u2345\u00ef";
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy(new StringReader(probe), writer);
        assertThat(writer.toString()).isEqualTo(probe);
    }

    /*
     * copy(Reader,Writer,int)
     */

    @Test
    void copyNullReaderNullWriterNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> IOUtil.copy(nullReader(), nullWriter(), -1));
    }

    @Test
    void copyEmptyReaderNullWriterNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> IOUtil.copy(emptyReader(), nullWriter(), -1));
    }

    @Test
    void copyNullReaderValidWriterNegBufSz() throws Exception {
        assertThrows(
                NegativeArraySizeException.class, () -> IOUtil.copy(nullReader(), new DontCloseStringWriter(), -1));
    }

    @Test
    void copyEmptyReaderValidWriterNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> {
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(emptyReader(), writer, -1);
            assertThat(writer.toString()).isEqualTo(emptyString());
        });
    }

    @Test
    void copyReaderNullWriterNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> {
            String probe = "A string \u2345\u00ef";
            IOUtil.copy(new StringReader(probe), nullWriter(), -1);
        });
    }

    @Test
    void copyReaderValidWriterNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> {
            String probe = "A string \u2345\u00ef";
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(new StringReader(probe), writer, -1);
            assertThat(writer.toString()).isEqualTo(probe);
        });
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void copyNullReaderNullWriterZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullReader(), nullWriter(), 0));
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void copyEmptyReaderNullWriterZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyReader(), nullWriter(), 0));
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void copyNullReaderValidWriterZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullReader(), new DontCloseStringWriter(), 0));
    }

    @Test
    void copyNullReaderNullWriterPosBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullReader(), nullWriter(), 1));
    }

    @Test
    void copyEmptyReaderNullWriterPosBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyReader(), nullWriter(), 1));
    }

    @Test
    void copyNullReaderValidWriterPosBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullReader(), new DontCloseStringWriter(), 1));
    }

    @Test
    void copyEmptyReaderValidWriterPosBufSz() throws Exception {
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy(emptyReader(), writer, 1);
        assertThat(writer.toString()).isEqualTo(emptyString());
    }

    @Test
    void copyReaderNullWriterPosBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string \u2345\u00ef";
            IOUtil.copy(new StringReader(probe), nullWriter(), 1);
        });
    }

    @Test
    void copyReaderValidWriterPosBufSz() throws Exception {
        String probe = "A string \u2345\u00ef";
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy(new StringReader(probe), writer, 1);
        assertThat(writer.toString()).isEqualTo(probe);
    }

    /*
     * toByteArray(InputStream,int)
     */

    @Test
    void toByteArrayFromInputStreamNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> {
            String probe = "A string \u2345\u00ef";
            assertThat(IOUtil.toByteArray(new DontCloseByteArrayInputStream(IOUtil.toByteArray(probe)), -1))
                    .isEqualTo(probe.getBytes());
        });
    }

    @Test
    void toByteArrayNullInputStreamNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> IOUtil.toByteArray(nullInputStream(), -1));
    }

    @Test
    void toByteArrayFromInputStreamPosBufSz() throws Exception {
        String probe = "A string \u2345\u00ef";
        assertThat(IOUtil.toByteArray(new DontCloseByteArrayInputStream(IOUtil.toByteArray(probe)), +1))
                .isEqualTo(probe.getBytes());
    }

    @Test
    void toByteArrayNullInputStreamPosBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toByteArray(nullInputStream(), +1));
    }

    /*
     * toByteArray(Reader,int)
     */

    @Test
    void toByteArrayFromReaderNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> {
            String probe = "A string \u2345\u00ef";
            assertThat(IOUtil.toByteArray(new DontCloseStringReader(probe), -1)).isEqualTo(probe.getBytes());
        });
    }

    @Test
    void toByteArrayNullReaderNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> IOUtil.toByteArray(nullReader(), -1));
    }

    @Test
    void toByteArrayFromReaderPosBufSz() throws Exception {
        String probe = "A string \u2345\u00ef";
        assertThat(IOUtil.toByteArray(new DontCloseStringReader(probe), +1)).isEqualTo(probe.getBytes());
    }

    @Test
    void toByteArrayNullReaderPosBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toByteArray(nullReader(), +1));
    }

    /*
     * toByteArray(String,int)
     */

    @Test
    void toByteArrayFromStringNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> {
            String probe = "A string \u2345\u00ef";
            assertThat(IOUtil.toByteArray(probe, -1)).isEqualTo(probe.getBytes());
        });
    }

    @Test
    void toByteArrayNullStringNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toByteArray(nullString(), -1));
    }

    @Test
    void toByteArrayFromStringPosBufSz() throws Exception {
        String probe = "A string \u2345\u00ef";
        assertThat(IOUtil.toByteArray(probe, +1)).isEqualTo(probe.getBytes());
    }

    @Test
    void toByteArrayNullStringPosBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toByteArray(nullString(), +1));
    }

    /*
     * toString(Reader,int)
     */

    @Test
    void toStringFromReaderNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> {
            String probe = "A string \u2345\u00ef";
            assertThat(IOUtil.toString(new DontCloseStringReader(probe), -1)).isEqualTo(probe);
        });
    }

    @Test
    void toStringNullReaderNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> IOUtil.toString(nullReader(), -1));
    }

    @Test
    void toStringFromReaderPosBufSz() throws Exception {
        String probe = "A string \u2345\u00ef";
        assertThat(IOUtil.toString(new DontCloseStringReader(probe), +1)).isEqualTo(probe);
    }

    @Test
    void toStringNullReaderPosBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullReader(), +1));
    }

    /*
     * copy(Reader,OutputStream)
     */

    @Test
    void copyNullReaderNullOutputStream() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullReader(), nullOutputStream()));
    }

    @Test
    void copyNullReaderValidOutputStream() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullReader(), new DontCloseByteArrayOutputStream()));
    }

    @Test
    void copyEmptyReaderNullOutputStream() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyReader(), nullOutputStream()));
    }

    @Test
    void copyEmptyReaderValidOutputStream() throws Exception {
        IOUtil.copy(emptyReader(), new DontCloseByteArrayOutputStream());
    }

    @Test
    void copyReaderValidOutputStream() throws Exception {
        ByteArrayOutputStream outputStream = new DontCloseByteArrayOutputStream();
        String probe = "A string \u2345\u00ef";
        IOUtil.copy(new DontCloseStringReader(probe), outputStream);
        assertThat(outputStream.toByteArray()).isEqualTo(probe.getBytes());
    }

    /*
     * copy(Reader,OutputStream,int)
     */

    @Test
    void copyNullReaderNullOutputStreamNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullReader(), nullOutputStream(), -1));
    }

    @Test
    void copyNullReaderValidOutputStreamNegBufSz() throws Exception {
        assertThrows(
                NegativeArraySizeException.class,
                () -> IOUtil.copy(nullReader(), new DontCloseByteArrayOutputStream(), -1));
    }

    @Test
    void copyEmptyReaderNullOutputStreamNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyReader(), nullOutputStream(), -1));
    }

    @Test
    void copyEmptyReaderValidOutputStreamNegBufSz() throws Exception {
        assertThrows(
                NegativeArraySizeException.class,
                () -> IOUtil.copy(emptyReader(), new DontCloseByteArrayOutputStream(), -1));
    }

    @Test
    void copyReaderValidOutputStreamNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> {
            ByteArrayOutputStream outputStream = new DontCloseByteArrayOutputStream();
            String probe = "A string \u2345\u00ef";
            IOUtil.copy(new DontCloseStringReader(probe), outputStream, -1);
            assertThat(outputStream.toByteArray()).isEqualTo(probe.getBytes());
        });
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void copyNullReaderNullOutputStreamZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullReader(), nullOutputStream(), 0));
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void copyNullReaderValidOutputStreamZeroBufSz() throws Exception {
        assertThrows(
                NullPointerException.class, () -> IOUtil.copy(nullReader(), new DontCloseByteArrayOutputStream(), 0));
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void copyEmptyReaderNullOutputStreamZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyReader(), nullOutputStream(), 0));
    }

    @Test
    void copyNullReaderNullOutputStreamPosBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullReader(), nullOutputStream(), 1));
    }

    @Test
    void copyNullReaderValidOutputStreamPosBufSz() throws Exception {
        assertThrows(
                NullPointerException.class, () -> IOUtil.copy(nullReader(), new DontCloseByteArrayOutputStream(), 1));
    }

    @Test
    void copyEmptyReaderNullOutputStreamPosBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyReader(), nullOutputStream(), 1));
    }

    @Test
    void copyEmptyReaderValidOutputStreamPosBufSz() throws Exception {
        IOUtil.copy(emptyReader(), new DontCloseByteArrayOutputStream(), 1);
    }

    @Test
    void copyReaderValidOutputStreamPosBufSz() throws Exception {
        ByteArrayOutputStream outputStream = new DontCloseByteArrayOutputStream();
        String probe = "A string \u2345\u00ef";
        IOUtil.copy(new DontCloseStringReader(probe), outputStream, 1);
        assertThat(outputStream.toByteArray()).isEqualTo(probe.getBytes());
    }

    /*
     * copy(byte[],Writer)
     */

    /*
     * copy(byte[],Writer,int)
     */

    /*
     * copy(byte[],Writer,String)
     */

    /*
     * copy(byte[],Writer,String,int)
     */
    /*
     * copy(byte[],Writer)
     */

    @Test
    void copyNullByteArrayNullWriter() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullByteArray(), nullWriter()));
    }

    @Test
    void copyEmptyByteArrayNullWriter() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyByteArray(), nullWriter()));
    }

    @Test
    void copyEmptyByteArrayValidWriter() throws Exception {
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy(emptyByteArray(), writer);
        assertThat(writer.toString()).isEqualTo(emptyString());
    }

    @Test
    void copyByteArrayNullWriter() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string \u2345\u00ef";
            IOUtil.copy(probe.getBytes(), nullWriter());
        });
    }

    @Test
    void copyByteArrayValidWriter() throws Exception {
        String probe = "A string \u2345\u00ef";
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy(probe.getBytes(), writer);
        assertThat(writer.toString().getBytes()).isEqualTo(probe.getBytes());
    }

    /*
     * copy(byte[],Writer,int)
     */

    @Test
    void copyNullByteArrayNullWriterNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullByteArray(), nullWriter(), -1));
    }

    @Test
    void copyEmptyByteArrayNullWriterNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> IOUtil.copy(emptyByteArray(), nullWriter(), -1));
    }

    @Test
    void copyEmptyByteArrayValidWriterNegBufSz() throws Exception {
        assertThrows(
                NegativeArraySizeException.class, () -> IOUtil.copy(emptyByteArray(), new DontCloseStringWriter(), -1));
    }

    @Test
    void copyByteArrayNullWriterNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> {
            String probe = "A string \u2345\u00ef";
            IOUtil.copy(probe.getBytes(), nullWriter(), -1);
        });
    }

    @Test
    void copyByteArrayValidWriterNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> {
            String probe = "A string \u2345\u00ef";
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(probe.getBytes(), writer, -1);
            assertThat(writer.toString().getBytes()).isEqualTo(probe.getBytes());
        });
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void copyNullByteArrayNullWriterZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullByteArray(), nullWriter(), 0));
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void copyNullByteArrayValidWriterZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullByteArray(), new DontCloseStringWriter(), 0));
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void copyEmptyByteArrayNullWriterZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyByteArray(), nullWriter(), 0));
    }

    @Test
    void copyNullByteArrayNullWriterPosBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullByteArray(), nullWriter(), 1));
    }

    @Test
    void copyNullByteArrayValidWriterPosBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullByteArray(), new DontCloseStringWriter(), 1));
    }

    @Test
    void copyEmptyByteArrayNullWriterPosBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyByteArray(), nullWriter(), 1));
    }

    @Test
    void copyEmptyByteArrayValidWriterPosBufSz() throws Exception {
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy(emptyByteArray(), writer, 1);
        assertThat(writer.toString()).isEqualTo(emptyString());
    }

    @Test
    void copyByteArrayValidWriterPosBufSz() throws Exception {
        String probe = "A string \u2345\u00ef";
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy(probe.getBytes(), writer, 1);
        assertThat(writer.toString().getBytes()).isEqualTo(probe.getBytes());
    }

    /*
     * copy(byte[],Writer,String)
     */

    @Test
    void copyNullByteArrayNullWriterNullEncoding() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullByteArray(), nullWriter(), null));
    }

    @Test
    void copyNullByteArrayValidWriterNullEncoding() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullByteArray(), new DontCloseStringWriter(), null));
    }

    @Test
    void copyEmptyByteArrayNullWriterNullEncoding() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyByteArray(), nullWriter(), null));
    }

    @Test
    void copyEmptyByteArrayValidWriterNullEncoding() throws Exception {
        assertThrows(
                NullPointerException.class, () -> IOUtil.copy(emptyByteArray(), new DontCloseStringWriter(), null));
    }

    @Test
    void copyByteArrayNullEncoding() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string \u2345\u00ef";
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(probe.getBytes(), writer, null);
            assertThat(writer.toString().getBytes()).isEqualTo(probe.getBytes());
        });
    }

    @Test
    void copyNullByteArrayNullWriterJunkEncoding() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullByteArray(), nullWriter(), "junk"));
    }

    @Test
    void copyNullByteArrayValidWriterJunkEncoding() throws Exception {
        assertThrows(
                NullPointerException.class, () -> IOUtil.copy(nullByteArray(), new DontCloseStringWriter(), "junk"));
    }

    @Test
    void copyEmptyByteArrayNullWriterJunkEncoding() throws Exception {
        assertThrows(UnsupportedEncodingException.class, () -> IOUtil.copy(emptyByteArray(), nullWriter(), "junk"));
    }

    @Test
    void copyEmptyByteArrayValidWriterJunkEncoding() throws Exception {
        assertThrows(
                UnsupportedEncodingException.class,
                () -> IOUtil.copy(emptyByteArray(), new DontCloseStringWriter(), "junk"));
    }

    @Test
    void copyByteArrayNullWriterJunkEncoding() throws Exception {
        assertThrows(UnsupportedEncodingException.class, () -> {
            String probe = "A string \u2345\u00ef";
            IOUtil.copy(probe.getBytes(), nullWriter(), "junk");
        });
    }

    @Test
    void copyByteArrayValidWriterJunkEncoding() throws Exception {
        assertThrows(UnsupportedEncodingException.class, () -> {
            String probe = "A string \u2345\u00ef";
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(probe.getBytes(), writer, "junk");
            assertThat(writer.toString().getBytes()).isEqualTo(probe.getBytes());
        });
    }

    @Test
    void copyNullByteArrayNullWriterValidEncoding() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullByteArray(), nullWriter(), "utf-16"));
    }

    @Test
    void copyEmptyByteArrayNullWriterValidEncoding() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyByteArray(), nullWriter(), "utf-16"));
    }

    @Test
    void copyNullByteArrayValidWriterValidEncoding() throws Exception {
        assertThrows(
                NullPointerException.class, () -> IOUtil.copy(nullByteArray(), new DontCloseStringWriter(), "utf-16"));
    }

    @Test
    void copyEmptyByteArrayValidWriterValidEncoding() throws Exception {
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy(emptyByteArray(), writer, "utf-16");
        assertThat(writer.toString()).isEqualTo(emptyString());
    }

    @Test
    void copyByteArrayNullWriterValidEncoding() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string \u2345\u00ef";
            IOUtil.copy(probe.getBytes(UTF_16), nullWriter(), "utf-16");
        });
    }

    @Test
    void copyByteArrayValidWriterValidEncoding() throws Exception {
        String probe = "A string \u2345\u00ef";
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy(probe.getBytes(UTF_16), writer, "utf-16");
        assertThat(writer.toString().getBytes(UTF_8)).isEqualTo(probe.getBytes(UTF_8));
    }

    /*
     * copy(byte[],Writer,String,int)
     */

    @Test
    void copyNullByteArrayNullWriterNullEncodingNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullByteArray(), nullWriter(), null, -1));
    }

    @Test
    void copyNullByteArrayValidWriterNullEncodingNegBufSz() throws Exception {
        assertThrows(
                NullPointerException.class, () -> IOUtil.copy(nullByteArray(), new DontCloseStringWriter(), null, -1));
    }

    @Test
    void copyEmptyByteArrayNullWriterNullEncodingNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyByteArray(), nullWriter(), null, -1));
    }

    @Test
    void copyEmptyByteArrayValidWriterNullEncodingNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(emptyByteArray(), writer, null, -1);
            assertThat(writer.toString()).isEqualTo(emptyString());
        });
    }

    @Test
    void copyByteArrayNullWriterNullEncodingNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string \u2345\u00ef";
            IOUtil.copy(probe.getBytes(), nullWriter(), null, -1);
        });
    }

    @Test
    void copyByteArrayValidWriterNullEncodingNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string \u2345\u00ef";
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(probe.getBytes(), writer, null, -1);
            assertThat(writer.toString().getBytes()).isEqualTo(probe.getBytes());
        });
    }

    @Test
    void copyNullByteArrayNullWriterJunkEncodingNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullByteArray(), nullWriter(), "junk", -1));
    }

    @Test
    void copyNullByteArrayValidWriterJunkEncodingNegBufSz() throws Exception {
        assertThrows(
                NullPointerException.class,
                () -> IOUtil.copy(nullByteArray(), new DontCloseStringWriter(), "junk", -1));
    }

    @Test
    void copyEmptyByteArrayNullWriterJunkEncodingNegBufSz() throws Exception {
        assertThrows(UnsupportedEncodingException.class, () -> IOUtil.copy(emptyByteArray(), nullWriter(), "junk", -1));
    }

    @Test
    void copyEmptyByteArrayJunkEncodingNegBufSz() throws Exception {
        assertThrows(UnsupportedEncodingException.class, () -> {
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(emptyByteArray(), writer, "junk", -1);
            assertThat(writer.toString()).isEqualTo(emptyString());
        });
    }

    @Test
    void copyByteArrayNullWriterJunkEncodingNegBufSz() throws Exception {
        assertThrows(UnsupportedEncodingException.class, () -> {
            String probe = "A string \u2345\u00ef";
            IOUtil.copy(probe.getBytes(), nullWriter(), "junk", -1);
        });
    }

    @Test
    void copyByteArrayValidWriterJunkEncodingNegBufSz() throws Exception {
        assertThrows(UnsupportedEncodingException.class, () -> {
            String probe = "A string \u2345\u00ef";
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(probe.getBytes(), writer, "junk", -1);
            assertThat(writer.toString().getBytes()).isEqualTo(probe.getBytes());
        });
    }

    @Test
    void copyNullByteArrayNullWriterValidEncodingNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullByteArray(), nullWriter(), "utf-16", -1));
    }

    @Test
    void copyNullByteArrayValidWriterValidEncodingNegBufSz() throws Exception {
        assertThrows(
                NullPointerException.class,
                () -> IOUtil.copy(nullByteArray(), new DontCloseStringWriter(), "utf-16", -1));
    }

    @Test
    void copyEmptyByteArrayNullWriterValidEncodingNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> IOUtil.copy(emptyByteArray(), nullWriter(), "utf-16", -1));
    }

    @Test
    void copyEmptyByteArrayValidWriterValidEncodingNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> {
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(emptyByteArray(), writer, "utf-16", -1);
            assertThat(writer.toString()).isEqualTo(emptyString());
        });
    }

    @Test
    void copyByteArrayNullWriterValidEncodingNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> {
            String probe = "A string \u2345\u00ef";
            IOUtil.copy(probe.getBytes(UTF_16), nullWriter(), -1);
        });
    }

    @Test
    void copyByteArrayValidEncodingNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> {
            String probe = "A string \u2345\u00ef";
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(probe.getBytes(UTF_16), writer, "utf-16", -1);
            assertThat(writer.toString().getBytes(UTF_8)).isEqualTo(probe.getBytes(UTF_8));
        });
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void copyNullByteArrayNullWriterNullEncodingZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullByteArray(), nullWriter(), null, 0));
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void copyNullByteArrayValidWriterNullEncodingZeroBufSz() throws Exception {
        assertThrows(
                NullPointerException.class, () -> IOUtil.copy(nullByteArray(), new DontCloseStringWriter(), null, 0));
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void copyEmptyByteArrayNullWriterNullEncodingZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyByteArray(), nullWriter(), null, 0));
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void copyEmptyByteArrayValidWriterNullEncodingZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(emptyByteArray(), writer, null, 0);
            assertThat(writer.toString()).isEqualTo(emptyString());
        });
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void copyByteArrayNullWriterNullEncodingZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string \u2345\u00ef";
            IOUtil.copy(probe.getBytes(), nullWriter(), null, 0);
        });
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void copyByteArrayValidWriterNullEncodingZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string \u2345\u00ef";
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(probe.getBytes(), writer, null, 0);
            assertThat(writer.toString().getBytes()).isEqualTo(probe.getBytes());
        });
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void copyNullByteArrayNullWriterJunkEncodingZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullByteArray(), nullWriter(), "junk", 0));
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void copyNullByteArrayValidWriterJunkEncodingZeroBufSz() throws Exception {
        assertThrows(
                NullPointerException.class, () -> IOUtil.copy(nullByteArray(), new DontCloseStringWriter(), "junk", 0));
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void copyEmptyByteArrayNullWriterJunkEncodingZeroBufSz() throws Exception {
        assertThrows(UnsupportedEncodingException.class, () -> IOUtil.copy(emptyByteArray(), nullWriter(), "junk", 0));
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void copyEmptyByteArrayValidWriterJunkEncodingZeroBufSz() throws Exception {
        assertThrows(UnsupportedEncodingException.class, () -> {
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(emptyByteArray(), writer, "junk", 0);
            assertThat(writer.toString()).isEqualTo(emptyString());
        });
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void copyByteArrayNullWriterJunkEncodingZeroBufSz() throws Exception {
        assertThrows(UnsupportedEncodingException.class, () -> {
            String probe = "A string \u2345\u00ef";
            IOUtil.copy(probe.getBytes(), nullWriter(), "junk", 0);
        });
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void copyByteArrayValidWriterJunkEncodingZeroBufSz() throws Exception {
        assertThrows(UnsupportedEncodingException.class, () -> {
            String probe = "A string \u2345\u00ef";
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(probe.getBytes(), writer, "junk", 0);
            assertThat(writer.toString().getBytes()).isEqualTo(probe.getBytes());
        });
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void copyNullByteArrayNullWriterValidEncodingZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullByteArray(), nullWriter(), "utf-16", 0));
    }

    @Test
    @Timeout(INFINITE_LOOP_TIMEOUT)
    void copyNullByteArrayValidWriterValidEncodingZeroBufSz() throws Exception {
        assertThrows(
                NullPointerException.class,
                () -> IOUtil.copy(nullByteArray(), new DontCloseStringWriter(), "utf-16", 0));
    }

    /*
     * Utility methods
     */
    private static byte[] nullByteArray() {
        return null;
    }

    private static String nullString() {
        return null;
    }

    private static OutputStream nullOutputStream() {
        return null;
    }

    private static InputStream nullInputStream() {
        return null;
    }

    private static Writer nullWriter() {
        return null;
    }

    private static Reader nullReader() {
        return null;
    }

    private static ByteArrayInputStream emptyInputStream() {
        return new ByteArrayInputStream(emptyByteArray());
    }

    private static Reader emptyReader() {
        return new StringReader(emptyString());
    }

    private static String emptyString() {
        return "";
    }

    private static byte[] emptyByteArray() {
        return new byte[0];
    }

    private static class DontCloseStringWriter extends StringWriter {
        @Override
        public void close() throws IOException {
            throw new UnsupportedOperationException("should not be called");
        }
    }

    private static class DontCloseStringReader extends StringReader {

        public DontCloseStringReader(String s) {
            super(s);
        }

        @Override
        public void close() {
            throw new UnsupportedOperationException("should not be called");
        }
    }

    private static class DontCloseByteArrayInputStream extends ByteArrayInputStream {
        public DontCloseByteArrayInputStream(byte[] input) {
            super(input);
        }

        @Override
        public void close() throws IOException {
            throw new UnsupportedOperationException("should not be called");
        }
    }

    private static class DontCloseByteArrayOutputStream extends ByteArrayOutputStream {
        @Override
        public void close() throws IOException {
            throw new UnsupportedOperationException("should not be called");
        }
    }
}
