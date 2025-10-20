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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings("deprecation")
public class IOUtilTest {

    private static final long INFINITE_LOOP_TIMEOUT = 500;

    @Test
    public void closeReaderWithNull() throws Exception {
        IOUtil.close((Reader) null);
    }

    @Test
    public void closeWriterWithNull() throws Exception {
        IOUtil.close((Writer) null);
    }

    @Test
    public void closeInputStreamWithNull() throws Exception {
        IOUtil.close(nullInputStream());
    }

    @Test
    public void closeOutputStreamWithNull() throws Exception {
        IOUtil.close(nullOutputStream());
    }

    @Test
    public void closeReaderWithIOE() throws Exception {
        IOUtil.close(new BufferedReader(new StringReader(emptyString())) {
            @Override
            public void close() throws IOException {
                super.close();
                throw new IOException("don't bomb out");
            }
        });
    }

    @Test
    public void closeWriterWithIOE() throws Exception {
        IOUtil.close(new BufferedWriter(new StringWriter()) {
            @Override
            public void close() throws IOException {
                super.close();
                throw new IOException("don't bomb out");
            }
        });
    }

    @Test
    public void closeInputStreamWithIOE() throws Exception {
        IOUtil.close(new BufferedInputStream(emptyInputStream()) {
            @Override
            public void close() throws IOException {
                super.close();
                throw new IOException("don't bomb out");
            }
        });
    }

    @Test
    public void closeOutputStreamWithIOE() throws Exception {
        IOUtil.close(new BufferedOutputStream(new ByteArrayOutputStream()) {
            @Override
            public void close() throws IOException {
                super.close();
                throw new IOException("don't bomb out");
            }
        });
    }

    @Test
    public void closeReaderCloses() throws Exception {
        final AtomicBoolean closed = new AtomicBoolean(false);
        IOUtil.close(new BufferedReader(new StringReader(emptyString())) {
            @Override
            public void close() throws IOException {
                closed.set(true);
                super.close();
            }
        });
        assertThat(closed.get(), is(true));
    }

    @Test
    public void closeWriterCloses() throws Exception {
        final AtomicBoolean closed = new AtomicBoolean(false);
        IOUtil.close(new BufferedWriter(new StringWriter()) {
            @Override
            public void close() throws IOException {
                closed.set(true);
                super.close();
            }
        });
        assertThat(closed.get(), is(true));
    }

    @Test
    public void closeInputStreamCloses() throws Exception {
        final AtomicBoolean closed = new AtomicBoolean(false);
        IOUtil.close(new BufferedInputStream(emptyInputStream()) {
            @Override
            public void close() throws IOException {
                closed.set(true);
                super.close();
            }
        });
        assertThat(closed.get(), is(true));
    }

    @Test
    public void closeOutputStreamCloses() throws Exception {
        final AtomicBoolean closed = new AtomicBoolean(false);
        IOUtil.close(new BufferedOutputStream(new ByteArrayOutputStream()) {
            @Override
            public void close() throws IOException {
                closed.set(true);
                super.close();
            }
        });
        assertThat(closed.get(), is(true));
    }

    @Test
    public void toByteArrayFromString() throws Exception {
        String probe = "A string \u2345\u00ef";
        assertThat(IOUtil.toByteArray(probe), is(probe.getBytes()));
    }

    @Test
    public void toByteArrayFromReader() throws Exception {
        String probe = "A string \u2345\u00ef";
        assertThat(IOUtil.toByteArray(new StringReader(probe)), is(probe.getBytes()));
    }

    @Test
    public void toByteArrayFromInputStream() throws Exception {
        String probe = "A string \u2345\u00ef";
        assertThat(
                IOUtil.toByteArray(new DontCloseByteArrayInputStream(IOUtil.toByteArray(probe))), is(probe.getBytes()));
    }

    @Test
    public void toByteArrayNullString() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toByteArray((String) null));
    }

    @Test
    public void toByteArrayNullReader() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toByteArray((Reader) null));
    }

    @Test
    public void toByteArrayNullInputStream() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toByteArray(nullInputStream()));
    }

    @Test
    public void contentEqualNullNull() throws Exception {
        assertThrows(IOException.class, () -> IOUtil.contentEquals(null, null));
    }

    @Test
    public void contentEqualNonNullNull() throws Exception {
        assertThrows(
                IOException.class,
                () -> IOUtil.contentEquals(new DontCloseByteArrayInputStream(emptyByteArray()), null));
    }

    @Test
    public void contentEqualNullNonNull() throws Exception {
        assertThrows(
                IOException.class,
                () -> IOUtil.contentEquals(new DontCloseByteArrayInputStream(emptyByteArray()), null));
    }

    @Test
    public void contentEqualEmptyEmpty() throws Exception {
        assertThat(
                IOUtil.contentEquals(
                        new DontCloseByteArrayInputStream(emptyByteArray()),
                        new DontCloseByteArrayInputStream(emptyByteArray())),
                is(true));
    }

    @Test
    public void contentEqualNonEmptyEmpty() throws Exception {
        assertThat(
                IOUtil.contentEquals(
                        new DontCloseByteArrayInputStream(new byte[1]),
                        new DontCloseByteArrayInputStream(emptyByteArray())),
                is(false));
    }

    @Test
    public void contentEqualEmptyNonEmpty() throws Exception {
        assertThat(
                IOUtil.contentEquals(
                        new DontCloseByteArrayInputStream(emptyByteArray()),
                        new DontCloseByteArrayInputStream(new byte[1])),
                is(false));
    }

    @Test
    public void contentEqualNonEmptyNonEmpty() throws Exception {
        assertThat(
                IOUtil.contentEquals(
                        new DontCloseByteArrayInputStream(new byte[1]), new DontCloseByteArrayInputStream(new byte[1])),
                is(true));
    }

    @Test
    public void contentEqualMostlySame() throws Exception {
        assertThat(
                IOUtil.contentEquals(
                        new DontCloseByteArrayInputStream(new byte[] {1, 2, 3, 4, 5, 6}),
                        new DontCloseByteArrayInputStream(new byte[] {1, 2, 3, 4, 5, 7})),
                is(false));
    }

    @Test
    public void contentEqualLargeSame() throws Exception {
        assertThat(
                IOUtil.contentEquals(
                        new DontCloseByteArrayInputStream(new byte[8192]),
                        new DontCloseByteArrayInputStream(new byte[8192])),
                is(true));
    }

    @Test
    public void contentEqualLargeDifferent() throws Exception {
        byte[] buf = new byte[8192];
        buf[8191] = 1;
        assertThat(
                IOUtil.contentEquals(
                        new DontCloseByteArrayInputStream(new byte[8192]), new DontCloseByteArrayInputStream(buf)),
                is(false));
    }

    @Test
    public void toStringNullByteArray() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullByteArray()));
    }

    @Test
    public void toStringEmptyByteArray() throws Exception {
        assertThat(IOUtil.toString(emptyByteArray()), is(emptyString()));
    }

    @Test
    public void toStringByteArray() throws Exception {
        String probe = "A string \u2345\u00ef";
        assertThat(IOUtil.toString(probe.getBytes()).getBytes(), is(probe.getBytes()));
    }

    @Test
    public void toStringNullByteArrayNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullByteArray(), -1));
    }

    @Test
    public void toStringEmptyByteArrayNegBufSz() throws Exception {
        assertThrows(
                NegativeArraySizeException.class,
                () -> assertThat(IOUtil.toString(emptyByteArray(), -1), is(emptyString())));
    }

    @Test
    public void toStringByteArrayNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> {
            String probe = "A string \u2345\u00ef";
            assertThat(IOUtil.toString(probe.getBytes(), -1), is(probe));
        });
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void toStringNullByteArrayZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullByteArray(), 0));
    }

    @Test
    public void toStringNullByteArrayPosBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullByteArray(), 1));
    }

    @Test
    public void toStringEmptyByteArrayPosBufSz() throws Exception {
        assertThat(IOUtil.toString(emptyByteArray(), 1), is(emptyString()));
    }

    @Test
    public void toStringByteArrayPosBufSz() throws Exception {
        String probe = "A string \u2345\u00ef";
        assertThat(IOUtil.toString(probe.getBytes(), 1).getBytes(), is(probe.getBytes()));
    }

    @Test
    public void toStringNullByteArrayNullEncoding() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullByteArray(), null));
    }

    @Test
    public void toStringEmptyByteArrayNullEncoding() throws Exception {
        assertThrows(
                NullPointerException.class,
                () -> assertThat(IOUtil.toString(emptyByteArray(), null), is(emptyString())));
    }

    @Test
    public void toStringByteArrayNullEncoding() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string \u2345\u00ef";
            assertThat(IOUtil.toString(probe.getBytes(), null).getBytes(), is(probe.getBytes()));
        });
    }

    @Test
    public void toStringNullByteArrayJunkEncoding() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullByteArray(), "junk"));
    }

    @Test
    public void toStringEmptyByteArrayJunkEncoding() throws Exception {
        assertThrows(
                UnsupportedEncodingException.class,
                () -> assertThat(IOUtil.toString(emptyByteArray(), "junk"), is(emptyString())));
    }

    @Test
    public void toStringByteArrayJunkEncoding() throws Exception {
        assertThrows(UnsupportedEncodingException.class, () -> {
            String probe = "A string \u2345\u00ef";
            assertThat(IOUtil.toString(probe.getBytes(), "junk").getBytes(), is(probe.getBytes()));
        });
    }

    @Test
    public void toStringNullByteArrayValidEncoding() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullByteArray(), "utf-16"));
    }

    @Test
    public void toStringEmptyByteArrayValidEncoding() throws Exception {
        assertThat(IOUtil.toString(emptyByteArray(), "utf-16"), is(emptyString()));
    }

    @Test
    public void toStringByteArrayValidEncoding() throws Exception {
        String probe = "A string \u2345\u00ef";
        assertThat(IOUtil.toString(probe.getBytes("utf-16"), "utf-16").getBytes("utf-8"), is(probe.getBytes("utf-8")));
    }

    @Test
    public void toStringNullByteArrayNullEncodingNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullByteArray(), null, -1));
    }

    @Test
    public void toStringEmptyByteArrayNullEncodingNegBufSz() throws Exception {
        assertThrows(
                NullPointerException.class,
                () -> assertThat(IOUtil.toString(emptyByteArray(), null, -1), is(emptyString())));
    }

    @Test
    public void toStringByteArrayNullEncodingNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string \u2345\u00ef";
            assertThat(IOUtil.toString(probe.getBytes(), null, -1).getBytes(), is(probe.getBytes()));
        });
    }

    @Test
    public void toStringNullByteArrayJunkEncodingNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullByteArray(), "junk", -1));
    }

    @Test
    public void toStringEmptyByteArrayJunkEncodingNegBufSz() throws Exception {
        assertThrows(
                UnsupportedEncodingException.class,
                () -> assertThat(IOUtil.toString(emptyByteArray(), "junk", -1), is(emptyString())));
    }

    @Test
    public void toStringByteArrayJunkEncodingNegBufSz() throws Exception {
        assertThrows(UnsupportedEncodingException.class, () -> {
            String probe = "A string \u2345\u00ef";
            assertThat(IOUtil.toString(probe.getBytes(), "junk", -1).getBytes(), is(probe.getBytes()));
        });
    }

    @Test
    public void toStringNullByteArrayValidEncodingNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullByteArray(), "utf-16", -1));
    }

    @Test
    public void toStringEmptyByteArrayValidEncodingNegBufSz() throws Exception {
        assertThrows(
                NegativeArraySizeException.class,
                () -> assertThat(IOUtil.toString(emptyByteArray(), "utf-16", -1), is(emptyString())));
    }

    @Test
    public void toStringByteArrayValidEncodingNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> {
            String probe = "A string \u2345\u00ef";
            assertThat(
                    IOUtil.toString(probe.getBytes("utf-16"), "utf-16", -1).getBytes("utf-8"),
                    is(probe.getBytes("utf-8")));
        });
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void toStringNullByteArrayNullEncodingZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullByteArray(), null, 0));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void toStringEmptyByteArrayNullEncodingZeroBufSz() throws Exception {
        assertThrows(
                NullPointerException.class,
                () -> assertThat(IOUtil.toString(emptyByteArray(), null, 0), is(emptyString())));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void toStringByteArrayNullEncodingZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string \u2345\u00ef";
            assertThat(IOUtil.toString(probe.getBytes(), null, 0).getBytes(), is(probe.getBytes()));
        });
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void toStringNullByteArrayJunkEncodingZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullByteArray(), "junk", 0));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void toStringEmptyByteArrayJunkEncodingZeroBufSz() throws Exception {
        assertThrows(
                UnsupportedEncodingException.class,
                () -> assertThat(IOUtil.toString(emptyByteArray(), "junk", 0), is(emptyString())));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void toStringByteArrayJunkEncodingZeroBufSz() throws Exception {
        assertThrows(UnsupportedEncodingException.class, () -> {
            String probe = "A string \u2345\u00ef";
            assertThat(IOUtil.toString(probe.getBytes(), "junk", 0).getBytes(), is(probe.getBytes()));
        });
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void toStringNullByteArrayValidEncodingZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullByteArray(), "utf-16", 0));
    }

    /*
     * copy(byte[],OutputStream)
     */

    @Test
    public void copyNullByteArrayNullOutputStream() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullByteArray(), nullOutputStream()));
    }

    @Test
    public void copyNullByteArrayValidOutputStream() throws Exception {
        assertThrows(
                NullPointerException.class, () -> IOUtil.copy(nullByteArray(), new DontCloseByteArrayOutputStream()));
    }

    @Test
    public void copyEmptyByteArrayNullOutputStream() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyByteArray(), nullOutputStream()));
    }

    @Test
    public void copyEmptyByteArrayValidOutputStream() throws Exception {
        IOUtil.copy(emptyByteArray(), new DontCloseByteArrayOutputStream());
    }

    @Test
    public void copyByteArrayValidOutputStream() throws Exception {
        ByteArrayOutputStream outputStream = new DontCloseByteArrayOutputStream();
        byte[] input = {1, 2, 3, 4, 5, 6};
        IOUtil.copy(input, outputStream);
        assertThat(outputStream.toByteArray(), is(input));
    }

    /*
     * copy(byte[],OutputStream,int)
     */

    @Test
    public void copyNullByteArrayNullOutputStreamNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullByteArray(), nullOutputStream()));
    }

    @Test
    public void copyNullByteArrayValidOutputStreamNegBufSz() throws Exception {
        assertThrows(
                NullPointerException.class, () -> IOUtil.copy(nullByteArray(), new DontCloseByteArrayOutputStream()));
    }

    @Test
    public void copyEmptyByteArrayNullOutputStreamNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyByteArray(), nullOutputStream()));
    }

    @Test
    public void copyEmptyByteArrayValidOutputStreamNegBufSz() throws Exception {
        IOUtil.copy(emptyByteArray(), new DontCloseByteArrayOutputStream());
    }

    @Test
    public void copyByteArrayValidOutputStreamNegBufSz() throws Exception {
        ByteArrayOutputStream outputStream = new DontCloseByteArrayOutputStream();
        byte[] input = {1, 2, 3, 4, 5, 6};
        IOUtil.copy(input, outputStream);
        assertThat(outputStream.toByteArray(), is(input));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyNullByteArrayNullOutputStreamZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullByteArray(), nullOutputStream()));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyNullByteArrayValidOutputStreamZeroBufSz() throws Exception {
        assertThrows(
                NullPointerException.class, () -> IOUtil.copy(nullByteArray(), new DontCloseByteArrayOutputStream()));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyEmptyByteArrayNullOutputStreamZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyByteArray(), nullOutputStream()));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyEmptyByteArrayValidOutputStreamZeroBufSz() throws Exception {
        IOUtil.copy(emptyByteArray(), new DontCloseByteArrayOutputStream());
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyByteArrayValidOutputStreamZeroBufSz() throws Exception {
        ByteArrayOutputStream outputStream = new DontCloseByteArrayOutputStream();
        byte[] input = {1, 2, 3, 4, 5, 6};
        IOUtil.copy(input, outputStream);
        assertThat(outputStream.toByteArray(), is(input));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyNullByteArrayNullOutputStreamPosBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullByteArray(), nullOutputStream()));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyNullByteArrayValidOutputStreamPosBufSz() throws Exception {
        assertThrows(
                NullPointerException.class, () -> IOUtil.copy(nullByteArray(), new DontCloseByteArrayOutputStream()));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyEmptyByteArrayNullOutputStreamPosBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyByteArray(), nullOutputStream()));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyEmptyByteArrayValidOutputStreamPosBufSz() throws Exception {
        IOUtil.copy(emptyByteArray(), new DontCloseByteArrayOutputStream());
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyByteArrayValidOutputStreamPosBufSz() throws Exception {
        ByteArrayOutputStream outputStream = new DontCloseByteArrayOutputStream();
        byte[] input = {1, 2, 3, 4, 5, 6};
        IOUtil.copy(input, outputStream);
        assertThat(outputStream.toByteArray(), is(input));
    }

    @Test
    public void copyNullInputStreamNullOutputStream() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullInputStream(), nullOutputStream()));
    }

    @Test
    public void copyNullInputStreamValidOutputStream() throws Exception {
        assertThrows(
                NullPointerException.class, () -> IOUtil.copy(nullInputStream(), new DontCloseByteArrayOutputStream()));
    }

    @Test
    public void copyEmptyInputStreamNullOutputStream() throws Exception {
        IOUtil.copy(new DontCloseByteArrayInputStream(emptyByteArray()), nullOutputStream());
    }

    @Test
    public void copyEmptyInputStreamValidOutputStream() throws Exception {
        IOUtil.copy(new DontCloseByteArrayInputStream(emptyByteArray()), new DontCloseByteArrayOutputStream());
    }

    @Test
    public void copyInputStreamValidOutputStream() throws Exception {
        ByteArrayOutputStream outputStream = new DontCloseByteArrayOutputStream();
        byte[] input = {1, 2, 3, 4, 5, 6};
        IOUtil.copy(new DontCloseByteArrayInputStream(input), outputStream);
        assertThat(outputStream.toByteArray(), is(input));
    }

    @Test
    public void copyNullInputStreamNullOutputStreamNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> IOUtil.copy(nullInputStream(), nullOutputStream(), -1));
    }

    @Test
    public void copyNullInputStreamValidOutputStreamNegBufSz() throws Exception {
        assertThrows(
                NegativeArraySizeException.class,
                () -> IOUtil.copy(nullInputStream(), new DontCloseByteArrayOutputStream(), -1));
    }

    @Test
    public void copyEmptyInputStreamNullOutputStreamNegBufSz() throws Exception {
        assertThrows(
                NegativeArraySizeException.class,
                () -> IOUtil.copy(new DontCloseByteArrayInputStream(emptyByteArray()), nullOutputStream(), -1));
    }

    @Test
    public void copyEmptyInputStreamValidOutputStreamNegBufSz() throws Exception {
        assertThrows(
                NegativeArraySizeException.class,
                () -> IOUtil.copy(
                        new DontCloseByteArrayInputStream(emptyByteArray()), new DontCloseByteArrayOutputStream(), -1));
    }

    @Test
    public void copyInputStreamValidOutputStreamNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> {
            ByteArrayOutputStream outputStream = new DontCloseByteArrayOutputStream();
            byte[] input = {1, 2, 3, 4, 5, 6};
            IOUtil.copy(new DontCloseByteArrayInputStream(input), outputStream, -1);
            assertThat(outputStream.toByteArray(), is(input));
        });
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyNullInputStreamNullOutputStreamZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullInputStream(), nullOutputStream(), 0));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyNullInputStreamValidOutputStreamZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullInputStream(), new ByteArrayOutputStream(), 0));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyEmptyInputStreamNullOutputStreamZeroBufSz() throws Exception {
        IOUtil.copy(new DontCloseByteArrayInputStream(emptyByteArray()), nullOutputStream(), 0);
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyEmptyInputStreamValidOutputStreamZeroBufSz() throws Exception {
        IOUtil.copy(new DontCloseByteArrayInputStream(emptyByteArray()), new DontCloseByteArrayOutputStream(), 0);
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyNullInputStreamNullOutputStreamPosBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullInputStream(), nullOutputStream(), 1));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyNullInputStreamValidOutputStreamPosBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullInputStream(), new ByteArrayOutputStream(), 1));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyEmptyInputStreamNullOutputStreamPosBufSz() throws Exception {
        IOUtil.copy(new DontCloseByteArrayInputStream(emptyByteArray()), nullOutputStream(), 1);
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyEmptyInputStreamValidOutputStreamPosBufSz() throws Exception {
        IOUtil.copy(new DontCloseByteArrayInputStream(emptyByteArray()), new DontCloseByteArrayOutputStream(), 1);
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyInputStreamValidOutputStreamPosBufSz() throws Exception {
        ByteArrayOutputStream outputStream = new DontCloseByteArrayOutputStream();
        byte[] input = {1, 2, 3, 4, 5, 6};
        IOUtil.copy(new DontCloseByteArrayInputStream(input), outputStream, 1);
        assertThat(outputStream.toByteArray(), is(input));
    }

    @Test
    public void toStringNullInputStream() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullInputStream()));
    }

    @Test
    public void toStringEmptyInputStream() throws Exception {
        assertThat(IOUtil.toString(emptyInputStream()), is(emptyString()));
    }

    @Test
    public void toStringInputStream() throws Exception {
        String probe = "A string \u2345\u00ef";
        assertThat(IOUtil.toString(new ByteArrayInputStream(probe.getBytes())).getBytes(), is(probe.getBytes()));
    }

    @Test
    public void toStringNullInputStreamNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullInputStream(), -1));
    }

    @Test
    public void toStringEmptyInputStreamNegBufSz() throws Exception {
        assertThrows(
                NegativeArraySizeException.class,
                () -> assertThat(IOUtil.toString(emptyInputStream(), -1), is(emptyString())));
    }

    @Test
    public void toStringInputStreamNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> {
            String probe = "A string \u2345\u00ef";
            assertThat(IOUtil.toString(new ByteArrayInputStream(probe.getBytes()), -1), is(probe));
        });
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void toStringNullInputStreamZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullInputStream(), 0));
    }

    @Test
    public void toStringNullInputStreamPosBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullInputStream(), 1));
    }

    @Test
    public void toStringEmptyInputStreamPosBufSz() throws Exception {
        assertThat(IOUtil.toString(emptyInputStream(), 1), is(emptyString()));
    }

    @Test
    public void toStringInputStreamPosBufSz() throws Exception {
        String probe = "A string \u2345\u00ef";
        assertThat(
                IOUtil.toString(new ByteArrayInputStream(probe.getBytes()), 1).getBytes(), is(probe.getBytes()));
    }

    @Test
    public void toStringNullInputStreamNullEncoding() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullInputStream(), null));
    }

    @Test
    public void toStringEmptyInputStreamNullEncoding() throws Exception {
        assertThrows(
                NullPointerException.class,
                () -> assertThat(IOUtil.toString(emptyInputStream(), null), is(emptyString())));
    }

    @Test
    public void toStringInputStreamNullEncoding() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string \u2345\u00ef";
            assertThat(
                    IOUtil.toString(new ByteArrayInputStream(probe.getBytes()), null)
                            .getBytes(),
                    is(probe.getBytes()));
        });
    }

    @Test
    public void toStringNullInputStreamJunkEncoding() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullInputStream(), "junk"));
    }

    @Test
    public void toStringEmptyInputStreamJunkEncoding() throws Exception {
        assertThrows(
                UnsupportedEncodingException.class,
                () -> assertThat(IOUtil.toString(emptyInputStream(), "junk"), is(emptyString())));
    }

    @Test
    public void toStringInputStreamJunkEncoding() throws Exception {
        assertThrows(UnsupportedEncodingException.class, () -> {
            String probe = "A string \u2345\u00ef";
            assertThat(
                    IOUtil.toString(new ByteArrayInputStream(probe.getBytes()), "junk")
                            .getBytes(),
                    is(probe.getBytes()));
        });
    }

    @Test
    public void toStringNullInputStreamValidEncoding() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullInputStream(), "utf-16"));
    }

    @Test
    public void toStringEmptyInputStreamValidEncoding() throws Exception {
        assertThat(IOUtil.toString(emptyInputStream(), "utf-16"), is(emptyString()));
    }

    @Test
    public void toStringInputStreamValidEncoding() throws Exception {
        String probe = "A string \u2345\u00ef";
        assertThat(
                IOUtil.toString(new ByteArrayInputStream(probe.getBytes("utf-16")), "utf-16")
                        .getBytes("utf-8"),
                is(probe.getBytes("utf-8")));
    }

    @Test
    public void toStringNullInputStreamNullEncodingNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullInputStream(), null, -1));
    }

    @Test
    public void toStringEmptyInputStreamNullEncodingNegBufSz() throws Exception {
        assertThrows(
                NullPointerException.class,
                () -> assertThat(IOUtil.toString(emptyInputStream(), null, -1), is(emptyString())));
    }

    @Test
    public void toStringInputStreamNullEncodingNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string \u2345\u00ef";
            assertThat(
                    IOUtil.toString(new ByteArrayInputStream(probe.getBytes()), null, -1)
                            .getBytes(),
                    is(probe.getBytes()));
        });
    }

    @Test
    public void toStringNullInputStreamJunkEncodingNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullInputStream(), "junk", -1));
    }

    @Test
    public void toStringEmptyInputStreamJunkEncodingNegBufSz() throws Exception {
        assertThrows(
                UnsupportedEncodingException.class,
                () -> assertThat(IOUtil.toString(emptyInputStream(), "junk", -1), is(emptyString())));
    }

    @Test
    public void toStringInputStreamJunkEncodingNegBufSz() throws Exception {
        assertThrows(UnsupportedEncodingException.class, () -> {
            String probe = "A string \u2345\u00ef";
            assertThat(
                    IOUtil.toString(new ByteArrayInputStream(probe.getBytes()), "junk", -1)
                            .getBytes(),
                    is(probe.getBytes()));
        });
    }

    @Test
    public void toStringNullInputStreamValidEncodingNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullInputStream(), "utf-16", -1));
    }

    @Test
    public void toStringEmptyInputStreamValidEncodingNegBufSz() throws Exception {
        assertThrows(
                NegativeArraySizeException.class,
                () -> assertThat(IOUtil.toString(emptyInputStream(), "utf-16", -1), is(emptyString())));
    }

    @Test
    public void toStringInputStreamValidEncodingNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> {
            String probe = "A string \u2345\u00ef";
            assertThat(
                    IOUtil.toString(new ByteArrayInputStream(probe.getBytes("utf-16")), "utf-16", -1)
                            .getBytes("utf-8"),
                    is(probe.getBytes("utf-8")));
        });
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void toStringNullInputStreamNullEncodingZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullInputStream(), null, 0));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void toStringEmptyInputStreamNullEncodingZeroBufSz() throws Exception {
        assertThrows(
                NullPointerException.class,
                () -> assertThat(IOUtil.toString(emptyInputStream(), null, 0), is(emptyString())));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void toStringInputStreamNullEncodingZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string \u2345\u00ef";
            assertThat(
                    IOUtil.toString(new ByteArrayInputStream(probe.getBytes()), null, 0)
                            .getBytes(),
                    is(probe.getBytes()));
        });
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void toStringNullInputStreamJunkEncodingZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullInputStream(), "junk", 0));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void toStringEmptyInputStreamJunkEncodingZeroBufSz() throws Exception {
        assertThrows(
                UnsupportedEncodingException.class,
                () -> assertThat(IOUtil.toString(emptyInputStream(), "junk", 0), is(emptyString())));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void toStringInputStreamJunkEncodingZeroBufSz() throws Exception {
        assertThrows(UnsupportedEncodingException.class, () -> {
            String probe = "A string \u2345\u00ef";
            assertThat(
                    IOUtil.toString(new ByteArrayInputStream(probe.getBytes()), "junk", 0)
                            .getBytes(),
                    is(probe.getBytes()));
        });
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void toStringNullInputStreamValidEncodingZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullInputStream(), "utf-16", 0));
    }

    /*
     * copy(InputStream,Writer)
     */

    @Test
    public void copyNullInputStreamNullWriter() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullInputStream(), nullWriter()));
    }

    @Test
    public void copyEmptyInputStreamNullWriter() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyInputStream(), nullWriter()));
    }

    @Test
    public void copyEmptyInputStreamValidWriter() throws Exception {
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy(emptyInputStream(), writer);
        assertThat(writer.toString(), is(emptyString()));
    }

    @Test
    public void copyInputStreamNullWriter() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string \u2345\u00ef";
            IOUtil.copy(new ByteArrayInputStream(probe.getBytes()), nullWriter());
        });
    }

    @Test
    public void copyInputStreamValidWriter() throws Exception {
        String probe = "A string \u2345\u00ef";
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy(new ByteArrayInputStream(probe.getBytes()), writer);
        assertThat(writer.toString().getBytes(), is(probe.getBytes()));
    }

    /*
     * copy(InputStream,Writer,int)
     */

    @Test
    public void copyNullInputStreamNullWriterNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullInputStream(), nullWriter(), -1));
    }

    @Test
    public void copyEmptyInputStreamNullWriterNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> IOUtil.copy(emptyInputStream(), nullWriter(), -1));
    }

    @Test
    public void copyEmptyInputStreamValidWriterNegBufSz() throws Exception {
        assertThrows(
                NegativeArraySizeException.class,
                () -> IOUtil.copy(emptyInputStream(), new DontCloseStringWriter(), -1));
    }

    @Test
    public void copyInputStreamNullWriterNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> {
            String probe = "A string \u2345\u00ef";
            IOUtil.copy(new ByteArrayInputStream(probe.getBytes()), nullWriter(), -1);
        });
    }

    @Test
    public void copyInputStreamValidWriterNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> {
            String probe = "A string \u2345\u00ef";
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(new ByteArrayInputStream(probe.getBytes()), writer, -1);
            assertThat(writer.toString().getBytes(), is(probe.getBytes()));
        });
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyNullInputStreamNullWriterZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullInputStream(), nullWriter(), 0));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyNullInputStreamValidWriterZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullInputStream(), new DontCloseStringWriter(), 0));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyEmptyInputStreamNullWriterZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyInputStream(), nullWriter(), 0));
    }

    @Test
    public void copyNullInputStreamNullWriterPosBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullInputStream(), nullWriter(), 1));
    }

    @Test
    public void copyNullInputStreamValidWriterPosBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullInputStream(), new DontCloseStringWriter(), 1));
    }

    @Test
    public void copyEmptyInputStreamNullWriterPosBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyInputStream(), nullWriter(), 1));
    }

    @Test
    public void copyEmptyInputStreamValidWriterPosBufSz() throws Exception {
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy(emptyInputStream(), writer, 1);
        assertThat(writer.toString(), is(emptyString()));
    }

    @Test
    public void copyInputStreamValidWriterPosBufSz() throws Exception {
        String probe = "A string \u2345\u00ef";
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy(new ByteArrayInputStream(probe.getBytes()), writer, 1);
        assertThat(writer.toString().getBytes(), is(probe.getBytes()));
    }

    /*
     * copy(InputStream,Writer,String)
     */

    @Test
    public void copyNullInputStreamNullWriterNullEncoding() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullInputStream(), nullWriter(), null));
    }

    @Test
    public void copyNullInputStreamValidWriterNullEncoding() throws Exception {
        assertThrows(
                NullPointerException.class, () -> IOUtil.copy(nullInputStream(), new DontCloseStringWriter(), null));
    }

    @Test
    public void copyEmptyInputStreamNullWriterNullEncoding() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyInputStream(), nullWriter(), null));
    }

    @Test
    public void copyEmptyInputStreamValidWriterNullEncoding() throws Exception {
        assertThrows(
                NullPointerException.class, () -> IOUtil.copy(emptyInputStream(), new DontCloseStringWriter(), null));
    }

    @Test
    public void copyInputStreamNullEncoding() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string \u2345\u00ef";
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(new ByteArrayInputStream(probe.getBytes()), writer, null);
            assertThat(writer.toString().getBytes(), is(probe.getBytes()));
        });
    }

    @Test
    public void copyNullInputStreamNullWriterJunkEncoding() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullInputStream(), nullWriter(), "junk"));
    }

    @Test
    public void copyNullInputStreamValidWriterJunkEncoding() throws Exception {
        assertThrows(
                NullPointerException.class, () -> IOUtil.copy(nullInputStream(), new DontCloseStringWriter(), "junk"));
    }

    @Test
    public void copyEmptyInputStreamNullWriterJunkEncoding() throws Exception {
        assertThrows(UnsupportedEncodingException.class, () -> IOUtil.copy(emptyInputStream(), nullWriter(), "junk"));
    }

    @Test
    public void copyEmptyInputStreamValidWriterJunkEncoding() throws Exception {
        assertThrows(
                UnsupportedEncodingException.class,
                () -> IOUtil.copy(emptyInputStream(), new DontCloseStringWriter(), "junk"));
    }

    @Test
    public void copyInputStreamNullWriterJunkEncoding() throws Exception {
        assertThrows(UnsupportedEncodingException.class, () -> {
            String probe = "A string \u2345\u00ef";
            IOUtil.copy(new ByteArrayInputStream(probe.getBytes()), nullWriter(), "junk");
        });
    }

    @Test
    public void copyInputStreamValidWriterJunkEncoding() throws Exception {
        assertThrows(UnsupportedEncodingException.class, () -> {
            String probe = "A string \u2345\u00ef";
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(new ByteArrayInputStream(probe.getBytes()), writer, "junk");
            assertThat(writer.toString().getBytes(), is(probe.getBytes()));
        });
    }

    @Test
    public void copyNullInputStreamNullWriterValidEncoding() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullInputStream(), nullWriter(), "utf-16"));
    }

    @Test
    public void copyEmptyInputStreamNullWriterValidEncoding() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyInputStream(), nullWriter(), "utf-16"));
    }

    @Test
    public void copyNullInputStreamValidWriterValidEncoding() throws Exception {
        assertThrows(
                NullPointerException.class,
                () -> IOUtil.copy(nullInputStream(), new DontCloseStringWriter(), "utf-16"));
    }

    @Test
    public void copyEmptyInputStreamValidWriterValidEncoding() throws Exception {
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy(emptyInputStream(), writer, "utf-16");
        assertThat(writer.toString(), is(emptyString()));
    }

    @Test
    public void copyInputStreamNullWriterValidEncoding() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string \u2345\u00ef";
            IOUtil.copy(new ByteArrayInputStream(probe.getBytes("utf-16")), nullWriter(), "utf-16");
        });
    }

    @Test
    public void copyInputStreamValidWriterValidEncoding() throws Exception {
        String probe = "A string \u2345\u00ef";
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy(new ByteArrayInputStream(probe.getBytes("utf-16")), writer, "utf-16");
        assertThat(writer.toString().getBytes("utf-8"), is(probe.getBytes("utf-8")));
    }

    /*
     * copy(InputStream,Writer,String,int)
     */

    @Test
    public void copyNullInputStreamNullWriterNullEncodingNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullInputStream(), nullWriter(), null, -1));
    }

    @Test
    public void copyNullInputStreamValidWriterNullEncodingNegBufSz() throws Exception {
        assertThrows(
                NullPointerException.class,
                () -> IOUtil.copy(nullInputStream(), new DontCloseStringWriter(), null, -1));
    }

    @Test
    public void copyEmptyInputStreamNullWriterNullEncodingNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyInputStream(), nullWriter(), null, -1));
    }

    @Test
    public void copyEmptyInputStreamValidWriterNullEncodingNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(emptyInputStream(), writer, null, -1);
            assertThat(writer.toString(), is(emptyString()));
        });
    }

    @Test
    public void copyInputStreamNullWriterNullEncodingNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string \u2345\u00ef";
            IOUtil.copy(new ByteArrayInputStream(probe.getBytes()), nullWriter(), null, -1);
        });
    }

    @Test
    public void copyInputStreamValidWriterNullEncodingNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string \u2345\u00ef";
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(new ByteArrayInputStream(probe.getBytes()), writer, null, -1);
            assertThat(writer.toString().getBytes(), is(probe.getBytes()));
        });
    }

    @Test
    public void copyNullInputStreamNullWriterJunkEncodingNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullInputStream(), nullWriter(), "junk", -1));
    }

    @Test
    public void copyNullInputStreamValidWriterJunkEncodingNegBufSz() throws Exception {
        assertThrows(
                NullPointerException.class,
                () -> IOUtil.copy(nullInputStream(), new DontCloseStringWriter(), "junk", -1));
    }

    @Test
    public void copyEmptyInputStreamNullWriterJunkEncodingNegBufSz() throws Exception {
        assertThrows(
                UnsupportedEncodingException.class, () -> IOUtil.copy(emptyInputStream(), nullWriter(), "junk", -1));
    }

    @Test
    public void copyEmptyInputStreamJunkEncodingNegBufSz() throws Exception {
        assertThrows(UnsupportedEncodingException.class, () -> {
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(emptyInputStream(), writer, "junk", -1);
            assertThat(writer.toString(), is(emptyString()));
        });
    }

    @Test
    public void copyInputStreamNullWriterJunkEncodingNegBufSz() throws Exception {
        assertThrows(UnsupportedEncodingException.class, () -> {
            String probe = "A string \u2345\u00ef";
            IOUtil.copy(new ByteArrayInputStream(probe.getBytes()), nullWriter(), "junk", -1);
        });
    }

    @Test
    public void copyInputStreamValidWriterJunkEncodingNegBufSz() throws Exception {
        assertThrows(UnsupportedEncodingException.class, () -> {
            String probe = "A string \u2345\u00ef";
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(new ByteArrayInputStream(probe.getBytes()), writer, "junk", -1);
            assertThat(writer.toString().getBytes(), is(probe.getBytes()));
        });
    }

    @Test
    public void copyNullInputStreamNullWriterValidEncodingNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullInputStream(), nullWriter(), "utf-16", -1));
    }

    @Test
    public void copyNullInputStreamValidWriterValidEncodingNegBufSz() throws Exception {
        assertThrows(
                NullPointerException.class,
                () -> IOUtil.copy(nullInputStream(), new DontCloseStringWriter(), "utf-16", -1));
    }

    @Test
    public void copyEmptyInputStreamNullWriterValidEncodingNegBufSz() throws Exception {
        assertThrows(
                NegativeArraySizeException.class, () -> IOUtil.copy(emptyInputStream(), nullWriter(), "utf-16", -1));
    }

    @Test
    public void copyEmptyInputStreamValidWriterValidEncodingNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> {
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(emptyInputStream(), writer, "utf-16", -1);
            assertThat(writer.toString(), is(emptyString()));
        });
    }

    @Test
    public void copyInputStreamNullWriterValidEncodingNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> {
            String probe = "A string \u2345\u00ef";
            IOUtil.copy(new ByteArrayInputStream(probe.getBytes("utf-16")), nullWriter(), -1);
        });
    }

    @Test
    public void copyInputStreamValidEncodingNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> {
            String probe = "A string \u2345\u00ef";
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(new ByteArrayInputStream(probe.getBytes("utf-16")), writer, "utf-16", -1);
            assertThat(writer.toString().getBytes("utf-8"), is(probe.getBytes("utf-8")));
        });
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyNullInputStreamNullWriterNullEncodingZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullInputStream(), nullWriter(), null, 0));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyNullInputStreamValidWriterNullEncodingZeroBufSz() throws Exception {
        assertThrows(
                NullPointerException.class, () -> IOUtil.copy(nullInputStream(), new DontCloseStringWriter(), null, 0));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyEmptyInputStreamNullWriterNullEncodingZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyInputStream(), nullWriter(), null, 0));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyEmptyInputStreamValidWriterNullEncodingZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(emptyInputStream(), writer, null, 0);
            assertThat(writer.toString(), is(emptyString()));
        });
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyInputStreamNullWriterNullEncodingZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string \u2345\u00ef";
            IOUtil.copy(new ByteArrayInputStream(probe.getBytes()), nullWriter(), null, 0);
        });
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyInputStreamValidWriterNullEncodingZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string \u2345\u00ef";
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(new ByteArrayInputStream(probe.getBytes()), writer, null, 0);
            assertThat(writer.toString().getBytes(), is(probe.getBytes()));
        });
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyNullInputStreamNullWriterJunkEncodingZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullInputStream(), nullWriter(), "junk", 0));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyNullInputStreamValidWriterJunkEncodingZeroBufSz() throws Exception {
        assertThrows(
                NullPointerException.class,
                () -> IOUtil.copy(nullInputStream(), new DontCloseStringWriter(), "junk", 0));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyEmptyInputStreamNullWriterJunkEncodingZeroBufSz() throws Exception {
        assertThrows(
                UnsupportedEncodingException.class, () -> IOUtil.copy(emptyInputStream(), nullWriter(), "junk", 0));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyEmptyInputStreamValidWriterJunkEncodingZeroBufSz() throws Exception {
        assertThrows(UnsupportedEncodingException.class, () -> {
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(emptyInputStream(), writer, "junk", 0);
            assertThat(writer.toString(), is(emptyString()));
        });
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyInputStreamNullWriterJunkEncodingZeroBufSz() throws Exception {
        assertThrows(UnsupportedEncodingException.class, () -> {
            String probe = "A string \u2345\u00ef";
            IOUtil.copy(new ByteArrayInputStream(probe.getBytes()), nullWriter(), "junk", 0);
        });
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyInputStreamValidWriterJunkEncodingZeroBufSz() throws Exception {
        assertThrows(UnsupportedEncodingException.class, () -> {
            String probe = "A string \u2345\u00ef";
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(new ByteArrayInputStream(probe.getBytes()), writer, "junk", 0);
            assertThat(writer.toString().getBytes(), is(probe.getBytes()));
        });
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyNullInputStreamNullWriterValidEncodingZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullInputStream(), nullWriter(), "utf-16", 0));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyNullInputStreamValidWriterValidEncodingZeroBufSz() throws Exception {
        assertThrows(
                NullPointerException.class,
                () -> IOUtil.copy(nullInputStream(), new DontCloseStringWriter(), "utf-16", 0));
    }

    /*
     * copy(String,Writer)
     */

    @Test
    public void copyNullStringNullWriter() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullString(), nullWriter()));
    }

    @Test
    public void copyEmptyStringNullWriter() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyString(), nullWriter()));
    }

    @Test
    public void copyNullStringValidWriter() throws Exception {
        IOUtil.copy(nullString(), new DontCloseStringWriter());
    }

    @Test
    public void copyEmptyStringValidWriter() throws Exception {
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy(emptyString(), writer);
        assertThat(writer.toString(), is(emptyString()));
    }

    @Test
    public void copyStringNullWriter() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string \u2345\u00ef";
            IOUtil.copy(probe, nullWriter());
        });
    }

    @Test
    public void copyStringValidWriter() throws Exception {
        String probe = "A string \u2345\u00ef";
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy(probe, writer);
        assertThat(writer.toString(), is(probe));
    }

    @Test
    public void copyNullStringNullOutputStream() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullString(), nullOutputStream()));
    }

    @Test
    public void copyEmptyStringNullOutputStream() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyString(), nullOutputStream()));
    }

    @Test
    public void copyNullStringValidOutputStream() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullString(), new DontCloseByteArrayOutputStream()));
    }

    @Test
    public void copyEmptyStringValidOutputStream() throws Exception {
        ByteArrayOutputStream os = new DontCloseByteArrayOutputStream();
        IOUtil.copy(emptyString(), os);
        assertThat(os.toByteArray(), is(emptyString().getBytes()));
    }

    @Test
    public void copyStringNullOutputStream() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string \u2345\u00ef";
            IOUtil.copy(probe, nullOutputStream());
        });
    }

    @Test
    public void copyStringValidOutputStream() throws Exception {
        String probe = "A string \u2345\u00ef";
        ByteArrayOutputStream os = new DontCloseByteArrayOutputStream();
        IOUtil.copy(probe, os);
        assertThat(os.toByteArray(), is(probe.getBytes()));
    }

    @Test
    public void copyNullStringNullOutputStreamNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullString(), nullOutputStream(), -1));
    }

    @Test
    public void copyEmptyStringNullOutputStreamNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyString(), nullOutputStream(), -1));
    }

    @Test
    public void copyNullStringValidOutputStreamNegBufSz() throws Exception {
        assertThrows(
                NullPointerException.class, () -> IOUtil.copy(nullString(), new DontCloseByteArrayOutputStream(), -1));
    }

    @Test
    public void copyEmptyStringValidOutputStreamNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> {
            ByteArrayOutputStream os = new DontCloseByteArrayOutputStream();
            IOUtil.copy(emptyString(), os, -1);
            assertThat(os.toByteArray(), is(emptyString().getBytes()));
        });
    }

    @Test
    public void copyStringNullOutputStreamNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string \u2345\u00ef";
            IOUtil.copy(probe, nullOutputStream(), -1);
        });
    }

    @Test
    public void copyStringValidOutputStreamNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> {
            String probe = "A string \u2345\u00ef";
            ByteArrayOutputStream os = new DontCloseByteArrayOutputStream();
            IOUtil.copy(probe, os, -1);
            assertThat(os.toByteArray(), is(probe.getBytes()));
        });
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyNullStringNullOutputStreamZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullString(), nullOutputStream(), 0));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyEmptyStringNullOutputStreamZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyString(), nullOutputStream(), 0));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyNullStringValidOutputStreamZeroBufSz() throws Exception {
        assertThrows(
                NullPointerException.class, () -> IOUtil.copy(nullString(), new DontCloseByteArrayOutputStream(), 0));
    }

    @Test
    public void copyNullStringNullOutputStreamPosBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullString(), nullOutputStream(), 1));
    }

    @Test
    public void copyEmptyStringNullOutputStreamPosBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyString(), nullOutputStream(), 1));
    }

    @Test
    public void copyNullStringValidOutputStreamPosBufSz() throws Exception {
        assertThrows(
                NullPointerException.class, () -> IOUtil.copy(nullString(), new DontCloseByteArrayOutputStream(), 1));
    }

    @Test
    public void copyEmptyStringValidOutputStreamPosBufSz() throws Exception {
        ByteArrayOutputStream os = new DontCloseByteArrayOutputStream();
        IOUtil.copy(emptyString(), os, 1);
        assertThat(os.toByteArray(), is(emptyString().getBytes()));
    }

    @Test
    public void copyStringNullOutputStreamPosBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string \u2345\u00ef";
            IOUtil.copy(probe, nullOutputStream(), 1);
        });
    }

    @Test
    public void copyStringValidOutputStreamPosBufSz() throws Exception {
        String probe = "A string \u2345\u00ef";
        ByteArrayOutputStream os = new DontCloseByteArrayOutputStream();
        IOUtil.copy(probe, os, 1);
        assertThat(os.toByteArray(), is(probe.getBytes()));
    }

    @Test
    public void copyNullReaderNullWriter() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullReader(), nullWriter()));
    }

    @Test
    public void copyEmptyReaderNullWriter() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyReader(), nullWriter()));
    }

    @Test
    public void copyNullReaderValidWriter() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullReader(), new DontCloseStringWriter()));
    }

    @Test
    public void copyEmptyReaderValidWriter() throws Exception {
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy(emptyReader(), writer);
        assertThat(writer.toString(), is(emptyString()));
    }

    @Test
    public void copyReaderNullWriter() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string \u2345\u00ef";
            IOUtil.copy(new StringReader(probe), nullWriter());
        });
    }

    @Test
    public void copyReaderValidWriter() throws Exception {
        String probe = "A string \u2345\u00ef";
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy(new StringReader(probe), writer);
        assertThat(writer.toString(), is(probe));
    }

    /*
     * copy(Reader,Writer,int)
     */

    @Test
    public void copyNullReaderNullWriterNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> IOUtil.copy(nullReader(), nullWriter(), -1));
    }

    @Test
    public void copyEmptyReaderNullWriterNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> IOUtil.copy(emptyReader(), nullWriter(), -1));
    }

    @Test
    public void copyNullReaderValidWriterNegBufSz() throws Exception {
        assertThrows(
                NegativeArraySizeException.class, () -> IOUtil.copy(nullReader(), new DontCloseStringWriter(), -1));
    }

    @Test
    public void copyEmptyReaderValidWriterNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> {
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(emptyReader(), writer, -1);
            assertThat(writer.toString(), is(emptyString()));
        });
    }

    @Test
    public void copyReaderNullWriterNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> {
            String probe = "A string \u2345\u00ef";
            IOUtil.copy(new StringReader(probe), nullWriter(), -1);
        });
    }

    @Test
    public void copyReaderValidWriterNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> {
            String probe = "A string \u2345\u00ef";
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(new StringReader(probe), writer, -1);
            assertThat(writer.toString(), is(probe));
        });
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyNullReaderNullWriterZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullReader(), nullWriter(), 0));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyEmptyReaderNullWriterZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyReader(), nullWriter(), 0));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyNullReaderValidWriterZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullReader(), new DontCloseStringWriter(), 0));
    }

    @Test
    public void copyNullReaderNullWriterPosBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullReader(), nullWriter(), 1));
    }

    @Test
    public void copyEmptyReaderNullWriterPosBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyReader(), nullWriter(), 1));
    }

    @Test
    public void copyNullReaderValidWriterPosBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullReader(), new DontCloseStringWriter(), 1));
    }

    @Test
    public void copyEmptyReaderValidWriterPosBufSz() throws Exception {
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy(emptyReader(), writer, 1);
        assertThat(writer.toString(), is(emptyString()));
    }

    @Test
    public void copyReaderNullWriterPosBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string \u2345\u00ef";
            IOUtil.copy(new StringReader(probe), nullWriter(), 1);
        });
    }

    @Test
    public void copyReaderValidWriterPosBufSz() throws Exception {
        String probe = "A string \u2345\u00ef";
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy(new StringReader(probe), writer, 1);
        assertThat(writer.toString(), is(probe));
    }

    /*
     * toByteArray(InputStream,int)
     */

    @Test
    public void toByteArrayFromInputStreamNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> {
            String probe = "A string \u2345\u00ef";
            assertThat(
                    IOUtil.toByteArray(new DontCloseByteArrayInputStream(IOUtil.toByteArray(probe)), -1),
                    is(probe.getBytes()));
        });
    }

    @Test
    public void toByteArrayNullInputStreamNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> IOUtil.toByteArray(nullInputStream(), -1));
    }

    @Test
    public void toByteArrayFromInputStreamPosBufSz() throws Exception {
        String probe = "A string \u2345\u00ef";
        assertThat(
                IOUtil.toByteArray(new DontCloseByteArrayInputStream(IOUtil.toByteArray(probe)), +1),
                is(probe.getBytes()));
    }

    @Test
    public void toByteArrayNullInputStreamPosBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toByteArray(nullInputStream(), +1));
    }

    /*
     * toByteArray(Reader,int)
     */

    @Test
    public void toByteArrayFromReaderNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> {
            String probe = "A string \u2345\u00ef";
            assertThat(IOUtil.toByteArray(new DontCloseStringReader(probe), -1), is(probe.getBytes()));
        });
    }

    @Test
    public void toByteArrayNullReaderNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> IOUtil.toByteArray(nullReader(), -1));
    }

    @Test
    public void toByteArrayFromReaderPosBufSz() throws Exception {
        String probe = "A string \u2345\u00ef";
        assertThat(IOUtil.toByteArray(new DontCloseStringReader(probe), +1), is(probe.getBytes()));
    }

    @Test
    public void toByteArrayNullReaderPosBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toByteArray(nullReader(), +1));
    }

    /*
     * toByteArray(String,int)
     */

    @Test
    public void toByteArrayFromStringNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> {
            String probe = "A string \u2345\u00ef";
            assertThat(IOUtil.toByteArray(probe, -1), is(probe.getBytes()));
        });
    }

    @Test
    public void toByteArrayNullStringNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toByteArray(nullString(), -1));
    }

    @Test
    public void toByteArrayFromStringPosBufSz() throws Exception {
        String probe = "A string \u2345\u00ef";
        assertThat(IOUtil.toByteArray(probe, +1), is(probe.getBytes()));
    }

    @Test
    public void toByteArrayNullStringPosBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toByteArray(nullString(), +1));
    }

    /*
     * toString(Reader,int)
     */

    @Test
    public void toStringFromReaderNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> {
            String probe = "A string \u2345\u00ef";
            assertThat(IOUtil.toString(new DontCloseStringReader(probe), -1), is(probe));
        });
    }

    @Test
    public void toStringNullReaderNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> IOUtil.toString(nullReader(), -1));
    }

    @Test
    public void toStringFromReaderPosBufSz() throws Exception {
        String probe = "A string \u2345\u00ef";
        assertThat(IOUtil.toString(new DontCloseStringReader(probe), +1), is(probe));
    }

    @Test
    public void toStringNullReaderPosBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullReader(), +1));
    }

    /*
     * copy(Reader,OutputStream)
     */

    @Test
    public void copyNullReaderNullOutputStream() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullReader(), nullOutputStream()));
    }

    @Test
    public void copyNullReaderValidOutputStream() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullReader(), new DontCloseByteArrayOutputStream()));
    }

    @Test
    public void copyEmptyReaderNullOutputStream() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyReader(), nullOutputStream()));
    }

    @Test
    public void copyEmptyReaderValidOutputStream() throws Exception {
        IOUtil.copy(emptyReader(), new DontCloseByteArrayOutputStream());
    }

    @Test
    public void copyReaderValidOutputStream() throws Exception {
        ByteArrayOutputStream outputStream = new DontCloseByteArrayOutputStream();
        String probe = "A string \u2345\u00ef";
        IOUtil.copy(new DontCloseStringReader(probe), outputStream);
        assertThat(outputStream.toByteArray(), is(probe.getBytes()));
    }

    /*
     * copy(Reader,OutputStream,int)
     */

    @Test
    public void copyNullReaderNullOutputStreamNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullReader(), nullOutputStream(), -1));
    }

    @Test
    public void copyNullReaderValidOutputStreamNegBufSz() throws Exception {
        assertThrows(
                NegativeArraySizeException.class,
                () -> IOUtil.copy(nullReader(), new DontCloseByteArrayOutputStream(), -1));
    }

    @Test
    public void copyEmptyReaderNullOutputStreamNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyReader(), nullOutputStream(), -1));
    }

    @Test
    public void copyEmptyReaderValidOutputStreamNegBufSz() throws Exception {
        assertThrows(
                NegativeArraySizeException.class,
                () -> IOUtil.copy(emptyReader(), new DontCloseByteArrayOutputStream(), -1));
    }

    @Test
    public void copyReaderValidOutputStreamNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> {
            ByteArrayOutputStream outputStream = new DontCloseByteArrayOutputStream();
            String probe = "A string \u2345\u00ef";
            IOUtil.copy(new DontCloseStringReader(probe), outputStream, -1);
            assertThat(outputStream.toByteArray(), is(probe.getBytes()));
        });
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyNullReaderNullOutputStreamZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullReader(), nullOutputStream(), 0));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyNullReaderValidOutputStreamZeroBufSz() throws Exception {
        assertThrows(
                NullPointerException.class, () -> IOUtil.copy(nullReader(), new DontCloseByteArrayOutputStream(), 0));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyEmptyReaderNullOutputStreamZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyReader(), nullOutputStream(), 0));
    }

    @Test
    public void copyNullReaderNullOutputStreamPosBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullReader(), nullOutputStream(), 1));
    }

    @Test
    public void copyNullReaderValidOutputStreamPosBufSz() throws Exception {
        assertThrows(
                NullPointerException.class, () -> IOUtil.copy(nullReader(), new DontCloseByteArrayOutputStream(), 1));
    }

    @Test
    public void copyEmptyReaderNullOutputStreamPosBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyReader(), nullOutputStream(), 1));
    }

    @Test
    public void copyEmptyReaderValidOutputStreamPosBufSz() throws Exception {
        IOUtil.copy(emptyReader(), new DontCloseByteArrayOutputStream(), 1);
    }

    @Test
    public void copyReaderValidOutputStreamPosBufSz() throws Exception {
        ByteArrayOutputStream outputStream = new DontCloseByteArrayOutputStream();
        String probe = "A string \u2345\u00ef";
        IOUtil.copy(new DontCloseStringReader(probe), outputStream, 1);
        assertThat(outputStream.toByteArray(), is(probe.getBytes()));
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
    public void copyNullByteArrayNullWriter() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullByteArray(), nullWriter()));
    }

    @Test
    public void copyEmptyByteArrayNullWriter() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyByteArray(), nullWriter()));
    }

    @Test
    public void copyEmptyByteArrayValidWriter() throws Exception {
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy(emptyByteArray(), writer);
        assertThat(writer.toString(), is(emptyString()));
    }

    @Test
    public void copyByteArrayNullWriter() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string \u2345\u00ef";
            IOUtil.copy(probe.getBytes(), nullWriter());
        });
    }

    @Test
    public void copyByteArrayValidWriter() throws Exception {
        String probe = "A string \u2345\u00ef";
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy(probe.getBytes(), writer);
        assertThat(writer.toString().getBytes(), is(probe.getBytes()));
    }

    /*
     * copy(byte[],Writer,int)
     */

    @Test
    public void copyNullByteArrayNullWriterNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullByteArray(), nullWriter(), -1));
    }

    @Test
    public void copyEmptyByteArrayNullWriterNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> IOUtil.copy(emptyByteArray(), nullWriter(), -1));
    }

    @Test
    public void copyEmptyByteArrayValidWriterNegBufSz() throws Exception {
        assertThrows(
                NegativeArraySizeException.class, () -> IOUtil.copy(emptyByteArray(), new DontCloseStringWriter(), -1));
    }

    @Test
    public void copyByteArrayNullWriterNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> {
            String probe = "A string \u2345\u00ef";
            IOUtil.copy(probe.getBytes(), nullWriter(), -1);
        });
    }

    @Test
    public void copyByteArrayValidWriterNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> {
            String probe = "A string \u2345\u00ef";
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(probe.getBytes(), writer, -1);
            assertThat(writer.toString().getBytes(), is(probe.getBytes()));
        });
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyNullByteArrayNullWriterZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullByteArray(), nullWriter(), 0));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyNullByteArrayValidWriterZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullByteArray(), new DontCloseStringWriter(), 0));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyEmptyByteArrayNullWriterZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyByteArray(), nullWriter(), 0));
    }

    @Test
    public void copyNullByteArrayNullWriterPosBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullByteArray(), nullWriter(), 1));
    }

    @Test
    public void copyNullByteArrayValidWriterPosBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullByteArray(), new DontCloseStringWriter(), 1));
    }

    @Test
    public void copyEmptyByteArrayNullWriterPosBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyByteArray(), nullWriter(), 1));
    }

    @Test
    public void copyEmptyByteArrayValidWriterPosBufSz() throws Exception {
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy(emptyByteArray(), writer, 1);
        assertThat(writer.toString(), is(emptyString()));
    }

    @Test
    public void copyByteArrayValidWriterPosBufSz() throws Exception {
        String probe = "A string \u2345\u00ef";
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy(probe.getBytes(), writer, 1);
        assertThat(writer.toString().getBytes(), is(probe.getBytes()));
    }

    /*
     * copy(byte[],Writer,String)
     */

    @Test
    public void copyNullByteArrayNullWriterNullEncoding() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullByteArray(), nullWriter(), null));
    }

    @Test
    public void copyNullByteArrayValidWriterNullEncoding() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullByteArray(), new DontCloseStringWriter(), null));
    }

    @Test
    public void copyEmptyByteArrayNullWriterNullEncoding() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyByteArray(), nullWriter(), null));
    }

    @Test
    public void copyEmptyByteArrayValidWriterNullEncoding() throws Exception {
        assertThrows(
                NullPointerException.class, () -> IOUtil.copy(emptyByteArray(), new DontCloseStringWriter(), null));
    }

    @Test
    public void copyByteArrayNullEncoding() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string \u2345\u00ef";
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(probe.getBytes(), writer, null);
            assertThat(writer.toString().getBytes(), is(probe.getBytes()));
        });
    }

    @Test
    public void copyNullByteArrayNullWriterJunkEncoding() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullByteArray(), nullWriter(), "junk"));
    }

    @Test
    public void copyNullByteArrayValidWriterJunkEncoding() throws Exception {
        assertThrows(
                NullPointerException.class, () -> IOUtil.copy(nullByteArray(), new DontCloseStringWriter(), "junk"));
    }

    @Test
    public void copyEmptyByteArrayNullWriterJunkEncoding() throws Exception {
        assertThrows(UnsupportedEncodingException.class, () -> IOUtil.copy(emptyByteArray(), nullWriter(), "junk"));
    }

    @Test
    public void copyEmptyByteArrayValidWriterJunkEncoding() throws Exception {
        assertThrows(
                UnsupportedEncodingException.class,
                () -> IOUtil.copy(emptyByteArray(), new DontCloseStringWriter(), "junk"));
    }

    @Test
    public void copyByteArrayNullWriterJunkEncoding() throws Exception {
        assertThrows(UnsupportedEncodingException.class, () -> {
            String probe = "A string \u2345\u00ef";
            IOUtil.copy(probe.getBytes(), nullWriter(), "junk");
        });
    }

    @Test
    public void copyByteArrayValidWriterJunkEncoding() throws Exception {
        assertThrows(UnsupportedEncodingException.class, () -> {
            String probe = "A string \u2345\u00ef";
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(probe.getBytes(), writer, "junk");
            assertThat(writer.toString().getBytes(), is(probe.getBytes()));
        });
    }

    @Test
    public void copyNullByteArrayNullWriterValidEncoding() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullByteArray(), nullWriter(), "utf-16"));
    }

    @Test
    public void copyEmptyByteArrayNullWriterValidEncoding() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyByteArray(), nullWriter(), "utf-16"));
    }

    @Test
    public void copyNullByteArrayValidWriterValidEncoding() throws Exception {
        assertThrows(
                NullPointerException.class, () -> IOUtil.copy(nullByteArray(), new DontCloseStringWriter(), "utf-16"));
    }

    @Test
    public void copyEmptyByteArrayValidWriterValidEncoding() throws Exception {
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy(emptyByteArray(), writer, "utf-16");
        assertThat(writer.toString(), is(emptyString()));
    }

    @Test
    public void copyByteArrayNullWriterValidEncoding() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string \u2345\u00ef";
            IOUtil.copy(probe.getBytes("utf-16"), nullWriter(), "utf-16");
        });
    }

    @Test
    public void copyByteArrayValidWriterValidEncoding() throws Exception {
        String probe = "A string \u2345\u00ef";
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy(probe.getBytes("utf-16"), writer, "utf-16");
        assertThat(writer.toString().getBytes("utf-8"), is(probe.getBytes("utf-8")));
    }

    /*
     * copy(byte[],Writer,String,int)
     */

    @Test
    public void copyNullByteArrayNullWriterNullEncodingNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullByteArray(), nullWriter(), null, -1));
    }

    @Test
    public void copyNullByteArrayValidWriterNullEncodingNegBufSz() throws Exception {
        assertThrows(
                NullPointerException.class, () -> IOUtil.copy(nullByteArray(), new DontCloseStringWriter(), null, -1));
    }

    @Test
    public void copyEmptyByteArrayNullWriterNullEncodingNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyByteArray(), nullWriter(), null, -1));
    }

    @Test
    public void copyEmptyByteArrayValidWriterNullEncodingNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(emptyByteArray(), writer, null, -1);
            assertThat(writer.toString(), is(emptyString()));
        });
    }

    @Test
    public void copyByteArrayNullWriterNullEncodingNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string \u2345\u00ef";
            IOUtil.copy(probe.getBytes(), nullWriter(), null, -1);
        });
    }

    @Test
    public void copyByteArrayValidWriterNullEncodingNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string \u2345\u00ef";
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(probe.getBytes(), writer, null, -1);
            assertThat(writer.toString().getBytes(), is(probe.getBytes()));
        });
    }

    @Test
    public void copyNullByteArrayNullWriterJunkEncodingNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullByteArray(), nullWriter(), "junk", -1));
    }

    @Test
    public void copyNullByteArrayValidWriterJunkEncodingNegBufSz() throws Exception {
        assertThrows(
                NullPointerException.class,
                () -> IOUtil.copy(nullByteArray(), new DontCloseStringWriter(), "junk", -1));
    }

    @Test
    public void copyEmptyByteArrayNullWriterJunkEncodingNegBufSz() throws Exception {
        assertThrows(UnsupportedEncodingException.class, () -> IOUtil.copy(emptyByteArray(), nullWriter(), "junk", -1));
    }

    @Test
    public void copyEmptyByteArrayJunkEncodingNegBufSz() throws Exception {
        assertThrows(UnsupportedEncodingException.class, () -> {
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(emptyByteArray(), writer, "junk", -1);
            assertThat(writer.toString(), is(emptyString()));
        });
    }

    @Test
    public void copyByteArrayNullWriterJunkEncodingNegBufSz() throws Exception {
        assertThrows(UnsupportedEncodingException.class, () -> {
            String probe = "A string \u2345\u00ef";
            IOUtil.copy(probe.getBytes(), nullWriter(), "junk", -1);
        });
    }

    @Test
    public void copyByteArrayValidWriterJunkEncodingNegBufSz() throws Exception {
        assertThrows(UnsupportedEncodingException.class, () -> {
            String probe = "A string \u2345\u00ef";
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(probe.getBytes(), writer, "junk", -1);
            assertThat(writer.toString().getBytes(), is(probe.getBytes()));
        });
    }

    @Test
    public void copyNullByteArrayNullWriterValidEncodingNegBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullByteArray(), nullWriter(), "utf-16", -1));
    }

    @Test
    public void copyNullByteArrayValidWriterValidEncodingNegBufSz() throws Exception {
        assertThrows(
                NullPointerException.class,
                () -> IOUtil.copy(nullByteArray(), new DontCloseStringWriter(), "utf-16", -1));
    }

    @Test
    public void copyEmptyByteArrayNullWriterValidEncodingNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> IOUtil.copy(emptyByteArray(), nullWriter(), "utf-16", -1));
    }

    @Test
    public void copyEmptyByteArrayValidWriterValidEncodingNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> {
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(emptyByteArray(), writer, "utf-16", -1);
            assertThat(writer.toString(), is(emptyString()));
        });
    }

    @Test
    public void copyByteArrayNullWriterValidEncodingNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> {
            String probe = "A string \u2345\u00ef";
            IOUtil.copy(probe.getBytes("utf-16"), nullWriter(), -1);
        });
    }

    @Test
    public void copyByteArrayValidEncodingNegBufSz() throws Exception {
        assertThrows(NegativeArraySizeException.class, () -> {
            String probe = "A string \u2345\u00ef";
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(probe.getBytes("utf-16"), writer, "utf-16", -1);
            assertThat(writer.toString().getBytes("utf-8"), is(probe.getBytes("utf-8")));
        });
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyNullByteArrayNullWriterNullEncodingZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullByteArray(), nullWriter(), null, 0));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyNullByteArrayValidWriterNullEncodingZeroBufSz() throws Exception {
        assertThrows(
                NullPointerException.class, () -> IOUtil.copy(nullByteArray(), new DontCloseStringWriter(), null, 0));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyEmptyByteArrayNullWriterNullEncodingZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyByteArray(), nullWriter(), null, 0));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyEmptyByteArrayValidWriterNullEncodingZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(emptyByteArray(), writer, null, 0);
            assertThat(writer.toString(), is(emptyString()));
        });
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyByteArrayNullWriterNullEncodingZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string \u2345\u00ef";
            IOUtil.copy(probe.getBytes(), nullWriter(), null, 0);
        });
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyByteArrayValidWriterNullEncodingZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string \u2345\u00ef";
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(probe.getBytes(), writer, null, 0);
            assertThat(writer.toString().getBytes(), is(probe.getBytes()));
        });
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyNullByteArrayNullWriterJunkEncodingZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullByteArray(), nullWriter(), "junk", 0));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyNullByteArrayValidWriterJunkEncodingZeroBufSz() throws Exception {
        assertThrows(
                NullPointerException.class, () -> IOUtil.copy(nullByteArray(), new DontCloseStringWriter(), "junk", 0));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyEmptyByteArrayNullWriterJunkEncodingZeroBufSz() throws Exception {
        assertThrows(UnsupportedEncodingException.class, () -> IOUtil.copy(emptyByteArray(), nullWriter(), "junk", 0));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyEmptyByteArrayValidWriterJunkEncodingZeroBufSz() throws Exception {
        assertThrows(UnsupportedEncodingException.class, () -> {
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(emptyByteArray(), writer, "junk", 0);
            assertThat(writer.toString(), is(emptyString()));
        });
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyByteArrayNullWriterJunkEncodingZeroBufSz() throws Exception {
        assertThrows(UnsupportedEncodingException.class, () -> {
            String probe = "A string \u2345\u00ef";
            IOUtil.copy(probe.getBytes(), nullWriter(), "junk", 0);
        });
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyByteArrayValidWriterJunkEncodingZeroBufSz() throws Exception {
        assertThrows(UnsupportedEncodingException.class, () -> {
            String probe = "A string \u2345\u00ef";
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(probe.getBytes(), writer, "junk", 0);
            assertThat(writer.toString().getBytes(), is(probe.getBytes()));
        });
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyNullByteArrayNullWriterValidEncodingZeroBufSz() throws Exception {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullByteArray(), nullWriter(), "utf-16", 0));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyNullByteArrayValidWriterValidEncodingZeroBufSz() throws Exception {
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

        DontCloseStringReader(String s) {
            super(s);
        }

        @Override
        public void close() {
            throw new UnsupportedOperationException("should not be called");
        }
    }

    private static class DontCloseByteArrayInputStream extends ByteArrayInputStream {
        DontCloseByteArrayInputStream(byte[] input) {
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
