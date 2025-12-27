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

import static java.nio.charset.StandardCharsets.UTF_16;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("deprecation")
public class IOUtilTest {

    private static final long INFINITE_LOOP_TIMEOUT = 500;

    @Test
    public void closeReaderWithNull() {
        IOUtil.close((Reader) null);
    }

    @Test
    public void closeWriterWithNull() {
        IOUtil.close((Writer) null);
    }

    @Test
    public void closeInputStreamWithNull() {
        IOUtil.close(nullInputStream());
    }

    @Test
    public void closeOutputStreamWithNull() {
        IOUtil.close(nullOutputStream());
    }

    @Test
    public void closeReaderWithIOE() {
        IOUtil.close(new BufferedReader(new StringReader(emptyString())) {
            @Override
            public void close() throws IOException {
                super.close();
                throw new IOException("don't bomb out");
            }
        });
    }

    @Test
    public void closeWriterWithIOE() {
        IOUtil.close(new BufferedWriter(new StringWriter()) {
            @Override
            public void close() throws IOException {
                super.close();
                throw new IOException("don't bomb out");
            }
        });
    }

    @Test
    public void closeInputStreamWithIOE() {
        IOUtil.close(new BufferedInputStream(emptyInputStream()) {
            @Override
            public void close() throws IOException {
                super.close();
                throw new IOException("don't bomb out");
            }
        });
    }

    @Test
    public void closeOutputStreamWithIOE() {
        IOUtil.close(new BufferedOutputStream(new ByteArrayOutputStream()) {
            @Override
            public void close() throws IOException {
                super.close();
                throw new IOException("don't bomb out");
            }
        });
    }

    @Test
    public void closeReaderCloses() {
        final AtomicBoolean closed = new AtomicBoolean(false);
        IOUtil.close(new BufferedReader(new StringReader(emptyString())) {
            @Override
            public void close() throws IOException {
                closed.set(true);
                super.close();
            }
        });
        assertTrue(closed.get());
    }

    @Test
    public void closeWriterCloses() {
        final AtomicBoolean closed = new AtomicBoolean(false);
        IOUtil.close(new BufferedWriter(new StringWriter()) {
            @Override
            public void close() throws IOException {
                closed.set(true);
                super.close();
            }
        });
        assertTrue(closed.get());
    }

    @Test
    public void closeInputStreamCloses() {
        final AtomicBoolean closed = new AtomicBoolean(false);
        IOUtil.close(new BufferedInputStream(emptyInputStream()) {
            @Override
            public void close() throws IOException {
                closed.set(true);
                super.close();
            }
        });
        assertTrue(closed.get());
    }

    @Test
    public void closeOutputStreamCloses() {
        final AtomicBoolean closed = new AtomicBoolean(false);
        IOUtil.close(new BufferedOutputStream(new ByteArrayOutputStream()) {
            @Override
            public void close() throws IOException {
                closed.set(true);
                super.close();
            }
        });
        assertTrue(closed.get());
    }

    @Test
    public void toByteArrayFromString() throws Exception {
        String probe = "A string ⍅ï";
        assertArrayEquals(probe.getBytes(), IOUtil.toByteArray(probe));
    }

    @Test
    public void toByteArrayFromReader() throws Exception {
        String probe = "A string ⍅ï";
        assertArrayEquals(probe.getBytes(), IOUtil.toByteArray(new StringReader(probe)));
    }

    @Test
    public void toByteArrayFromInputStream() throws Exception {
        String probe = "A string ⍅ï";
        assertArrayEquals(
                probe.getBytes(), IOUtil.toByteArray(new DontCloseByteArrayInputStream(IOUtil.toByteArray(probe))));
    }

    @Test
    public void toByteArrayNullString() {
        //noinspection DataFlowIssue
        assertThrows(NullPointerException.class, () -> IOUtil.toByteArray((String) null));
    }

    @Test
    public void toByteArrayNullReader() {
        //noinspection DataFlowIssue
        assertThrows(NullPointerException.class, () -> IOUtil.toByteArray((Reader) null));
    }

    @Test
    public void toByteArrayNullInputStream() {
        assertThrows(NullPointerException.class, () -> {
            assertNotNull(nullInputStream());
            IOUtil.toByteArray(nullInputStream());
        });
    }

    @Test
    public void contentEqualNullNull() {
        //noinspection DataFlowIssue
        assertThrows(IOException.class, () -> IOUtil.contentEquals(null, null));
    }

    @Test
    public void contentEqualNonNullNull() {
        //noinspection DataFlowIssue
        assertThrows(
                IOException.class,
                () -> IOUtil.contentEquals(new DontCloseByteArrayInputStream(emptyByteArray()), null));
    }

    @Test
    public void contentEqualNullNonNull() {
        //noinspection DataFlowIssue
        assertThrows(
                IOException.class,
                () -> IOUtil.contentEquals(new DontCloseByteArrayInputStream(emptyByteArray()), null));
    }

    @Test
    public void contentEqualEmptyEmpty() throws Exception {
        assertTrue(IOUtil.contentEquals(
                new DontCloseByteArrayInputStream(emptyByteArray()),
                new DontCloseByteArrayInputStream(emptyByteArray())));
    }

    @Test
    public void contentEqualNonEmptyEmpty() throws Exception {
        assertFalse(IOUtil.contentEquals(
                new DontCloseByteArrayInputStream(new byte[1]), new DontCloseByteArrayInputStream(emptyByteArray())));
    }

    @Test
    public void contentEqualEmptyNonEmpty() throws Exception {
        assertFalse(IOUtil.contentEquals(
                new DontCloseByteArrayInputStream(emptyByteArray()), new DontCloseByteArrayInputStream(new byte[1])));
    }

    @Test
    public void contentEqualNonEmptyNonEmpty() throws Exception {
        assertTrue(IOUtil.contentEquals(
                new DontCloseByteArrayInputStream(new byte[1]), new DontCloseByteArrayInputStream(new byte[1])));
    }

    @Test
    public void contentEqualMostlySame() throws Exception {
        assertFalse(IOUtil.contentEquals(
                new DontCloseByteArrayInputStream(new byte[] {1, 2, 3, 4, 5, 6}),
                new DontCloseByteArrayInputStream(new byte[] {1, 2, 3, 4, 5, 7})));
    }

    @Test
    public void contentEqualLargeSame() throws Exception {
        assertTrue(IOUtil.contentEquals(
                new DontCloseByteArrayInputStream(new byte[8192]), new DontCloseByteArrayInputStream(new byte[8192])));
    }

    @Test
    public void contentEqualLargeDifferent() throws Exception {
        byte[] buf = new byte[8192];
        buf[8191] = 1;
        assertFalse(IOUtil.contentEquals(
                new DontCloseByteArrayInputStream(new byte[8192]), new DontCloseByteArrayInputStream(buf)));
    }

    @Test
    public void toStringNullByteArray() {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullByteArray()));
    }

    @Test
    public void toStringEmptyByteArray() throws Exception {
        assertEquals(emptyString(), IOUtil.toString(emptyByteArray()));
    }

    @Test
    public void toStringByteArray() throws Exception {
        String probe = "A string ⍅ï";
        assertArrayEquals(probe.getBytes(), IOUtil.toString(probe.getBytes()).getBytes());
    }

    @Test
    public void toStringNullByteArrayNegBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullByteArray(), -1));
    }

    @Test
    public void toStringEmptyByteArrayNegBufSz() {
        assertThrows(
                NegativeArraySizeException.class,
                () -> assertEquals(IOUtil.toString(emptyByteArray(), -1), emptyString()));
    }

    @Test
    public void toStringByteArrayNegBufSz() {
        assertThrows(NegativeArraySizeException.class, () -> {
            String probe = "A string ⍅ï";
            assertEquals(probe, IOUtil.toString(probe.getBytes(), -1));
        });
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void toStringNullByteArrayZeroBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullByteArray(), 0));
    }

    @Test
    public void toStringNullByteArrayPosBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullByteArray(), 1));
    }

    @Test
    public void toStringEmptyByteArrayPosBufSz() throws Exception {
        assertEquals(emptyString(), IOUtil.toString(emptyByteArray(), 1));
    }

    @Test
    public void toStringByteArrayPosBufSz() throws Exception {
        String probe = "A string ⍅ï";
        assertArrayEquals(probe.getBytes(), IOUtil.toString(probe.getBytes(), 1).getBytes());
    }

    @Test
    public void toStringNullByteArrayNullEncoding() {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullByteArray(), null));
    }

    @Test
    public void toStringEmptyByteArrayNullEncoding() {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(emptyByteArray(), null));
    }

    @Test
    public void toStringByteArrayNullEncoding() {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string ⍅ï";
            IOUtil.toString(probe.getBytes(), null).getBytes();
        });
    }

    @Test
    public void toStringNullByteArrayJunkEncoding() {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullByteArray(), "junk"));
    }

    @Test
    public void toStringEmptyByteArrayJunkEncoding() {
        assertThrows(
                UnsupportedEncodingException.class,
                () -> assertEquals(IOUtil.toString(emptyByteArray(), "junk"), emptyString()));
    }

    @Test
    public void toStringByteArrayJunkEncoding() {
        assertThrows(UnsupportedEncodingException.class, () -> {
            String probe = "A string ⍅ï";
            assertArrayEquals(IOUtil.toString(probe.getBytes(), "junk").getBytes(), probe.getBytes());
        });
    }

    @Test
    public void toStringNullByteArrayValidEncoding() {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullByteArray(), "utf-16"));
    }

    @Test
    public void toStringEmptyByteArrayValidEncoding() throws Exception {
        assertEquals(IOUtil.toString(emptyByteArray(), "utf-16"), emptyString());
    }

    @Test
    public void toStringByteArrayValidEncoding() throws Exception {
        String probe = "A string ⍅ï";
        assertArrayEquals(
                IOUtil.toString(probe.getBytes("utf-16"), "utf-16").getBytes("utf-8"), probe.getBytes("utf-8"));
    }

    @Test
    public void toStringNullByteArrayNullEncodingNegBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullByteArray(), null, -1));
    }

    @Test
    public void toStringEmptyByteArrayNullEncodingNegBufSz() {
        assertThrows(
                NullPointerException.class,
                () -> assertEquals(IOUtil.toString(emptyByteArray(), null, -1), emptyString()));
    }

    @Test
    public void toStringByteArrayNullEncodingNegBufSz() {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string ⍅ï";
            assertArrayEquals(IOUtil.toString(probe.getBytes(), null, -1).getBytes(), probe.getBytes());
        });
    }

    @Test
    public void toStringNullByteArrayJunkEncodingNegBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullByteArray(), "junk", -1));
    }

    @Test
    public void toStringEmptyByteArrayJunkEncodingNegBufSz() {
        assertThrows(
                UnsupportedEncodingException.class,
                () -> assertEquals(IOUtil.toString(emptyByteArray(), "junk", -1), emptyString()));
    }

    @Test
    public void toStringByteArrayJunkEncodingNegBufSz() {
        assertThrows(UnsupportedEncodingException.class, () -> {
            String probe = "A string ⍅ï";
            assertEquals(IOUtil.toString(probe.getBytes(), "junk", -1).getBytes(), probe.getBytes());
        });
    }

    @Test
    public void toStringNullByteArrayValidEncodingNegBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullByteArray(), "utf-16", -1));
    }

    @Test
    public void toStringEmptyByteArrayValidEncodingNegBufSz() {
        assertThrows(
                NegativeArraySizeException.class,
                () -> assertEquals(IOUtil.toString(emptyByteArray(), "utf-16", -1), emptyString()));
    }

    @Test
    public void toStringByteArrayValidEncodingNegBufSz() {
        assertThrows(NegativeArraySizeException.class, () -> {
            String probe = "A string ⍅ï";
            assertArrayEquals(
                    IOUtil.toString(probe.getBytes("utf-16"), "utf-16", -1).getBytes(UTF_8), probe.getBytes(UTF_8));
        });
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void toStringNullByteArrayNullEncodingZeroBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullByteArray(), null, 0));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void toStringEmptyByteArrayNullEncodingZeroBufSz() {
        assertThrows(
                NullPointerException.class,
                () -> assertEquals(IOUtil.toString(emptyByteArray(), null, 0), emptyString()));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void toStringByteArrayNullEncodingZeroBufSz() {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string ⍅ï";
            assertArrayEquals(IOUtil.toString(probe.getBytes(), null, 0).getBytes(), probe.getBytes());
        });
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void toStringNullByteArrayJunkEncodingZeroBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullByteArray(), "junk", 0));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void toStringEmptyByteArrayJunkEncodingZeroBufSz() {
        assertThrows(
                UnsupportedEncodingException.class,
                () -> assertEquals(IOUtil.toString(emptyByteArray(), "junk", 0), emptyString()));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void toStringByteArrayJunkEncodingZeroBufSz() {
        assertThrows(UnsupportedEncodingException.class, () -> {
            String probe = "A string ⍅ï";
            assertArrayEquals(IOUtil.toString(probe.getBytes(), "junk", 0).getBytes(), probe.getBytes());
        });
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void toStringNullByteArrayValidEncodingZeroBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullByteArray(), "utf-16", 0));
    }

    /*
     * copy(byte[],OutputStream)
     */

    @Test
    public void copyNullByteArrayNullOutputStream() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullByteArray(), nullOutputStream()));
    }

    @Test
    public void copyNullByteArrayValidOutputStream() {
        assertThrows(
                NullPointerException.class, () -> IOUtil.copy(nullByteArray(), new DontCloseByteArrayOutputStream()));
    }

    @Test
    public void copyEmptyByteArrayNullOutputStream() {
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
        assertArrayEquals(input, outputStream.toByteArray());
    }

    /*
     * copy(byte[],OutputStream,int)
     */

    @Test
    public void copyNullByteArrayNullOutputStreamNegBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullByteArray(), nullOutputStream()));
    }

    @Test
    public void copyNullByteArrayValidOutputStreamNegBufSz() {
        assertThrows(
                NullPointerException.class, () -> IOUtil.copy(nullByteArray(), new DontCloseByteArrayOutputStream()));
    }

    @Test
    public void copyEmptyByteArrayNullOutputStreamNegBufSz() {
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
        assertArrayEquals(input, outputStream.toByteArray());
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyNullByteArrayNullOutputStreamZeroBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullByteArray(), nullOutputStream()));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyNullByteArrayValidOutputStreamZeroBufSz() {
        assertThrows(
                NullPointerException.class, () -> IOUtil.copy(nullByteArray(), new DontCloseByteArrayOutputStream()));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyEmptyByteArrayNullOutputStreamZeroBufSz() {
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
        assertArrayEquals(input, outputStream.toByteArray());
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyNullByteArrayNullOutputStreamPosBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullByteArray(), nullOutputStream()));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyNullByteArrayValidOutputStreamPosBufSz() {
        assertThrows(
                NullPointerException.class, () -> IOUtil.copy(nullByteArray(), new DontCloseByteArrayOutputStream()));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyEmptyByteArrayNullOutputStreamPosBufSz() {
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
        assertArrayEquals(input, outputStream.toByteArray());
    }

    @Test
    public void copyNullInputStreamNullOutputStream() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullInputStream(), nullOutputStream()));
    }

    @Test
    public void copyNullInputStreamValidOutputStream() {
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
        assertArrayEquals(input, outputStream.toByteArray());
    }

    @Test
    public void copyNullInputStreamNullOutputStreamNegBufSz() {
        assertThrows(NegativeArraySizeException.class, () -> IOUtil.copy(nullInputStream(), nullOutputStream(), -1));
    }

    @Test
    public void copyNullInputStreamValidOutputStreamNegBufSz() {
        assertThrows(
                NegativeArraySizeException.class,
                () -> IOUtil.copy(nullInputStream(), new DontCloseByteArrayOutputStream(), -1));
    }

    @Test
    public void copyEmptyInputStreamNullOutputStreamNegBufSz() {
        assertThrows(
                NegativeArraySizeException.class,
                () -> IOUtil.copy(new DontCloseByteArrayInputStream(emptyByteArray()), nullOutputStream(), -1));
    }

    @Test
    public void copyEmptyInputStreamValidOutputStreamNegBufSz() {
        assertThrows(
                NegativeArraySizeException.class,
                () -> IOUtil.copy(
                        new DontCloseByteArrayInputStream(emptyByteArray()), new DontCloseByteArrayOutputStream(), -1));
    }

    @Test
    public void copyInputStreamValidOutputStreamNegBufSz() {
        assertThrows(NegativeArraySizeException.class, () -> {
            ByteArrayOutputStream outputStream = new DontCloseByteArrayOutputStream();
            byte[] input = {1, 2, 3, 4, 5, 6};
            IOUtil.copy(new DontCloseByteArrayInputStream(input), outputStream, -1);
            assertEquals(input, outputStream.toByteArray());
        });
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyNullInputStreamNullOutputStreamZeroBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullInputStream(), nullOutputStream(), 0));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyNullInputStreamValidOutputStreamZeroBufSz() {
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
    public void copyNullInputStreamNullOutputStreamPosBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullInputStream(), nullOutputStream(), 1));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyNullInputStreamValidOutputStreamPosBufSz() {
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
        assertArrayEquals(input, outputStream.toByteArray());
    }

    @Test
    public void toStringNullInputStream() {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullInputStream()));
    }

    @Test
    public void toStringEmptyInputStream() throws Exception {
        assertEquals(emptyString(), IOUtil.toString(emptyInputStream()));
    }

    @Test
    public void toStringInputStream() throws Exception {
        String probe = "A string ⍅ï";
        assertArrayEquals(
                probe.getBytes(),
                IOUtil.toString(new ByteArrayInputStream(probe.getBytes())).getBytes());
    }

    @Test
    public void toStringNullInputStreamNegBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullInputStream(), -1));
    }

    @Test
    public void toStringEmptyInputStreamNegBufSz() {
        assertThrows(
                NegativeArraySizeException.class,
                () -> assertEquals(IOUtil.toString(emptyInputStream(), -1), emptyString()));
    }

    @Test
    public void toStringInputStreamNegBufSz() {
        assertThrows(NegativeArraySizeException.class, () -> {
            String probe = "A string ⍅ï";
            assertEquals(probe, IOUtil.toString(new ByteArrayInputStream(probe.getBytes()), -1));
        });
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void toStringNullInputStreamZeroBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullInputStream(), 0));
    }

    @Test
    public void toStringNullInputStreamPosBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullInputStream(), 1));
    }

    @Test
    public void toStringEmptyInputStreamPosBufSz() throws Exception {
        assertEquals(IOUtil.toString(emptyInputStream(), 1), emptyString());
    }

    @Test
    public void toStringInputStreamPosBufSz() throws Exception {
        String probe = "A string ⍅ï";
        assertArrayEquals(
                IOUtil.toString(new ByteArrayInputStream(probe.getBytes()), 1).getBytes(), probe.getBytes());
    }

    @Test
    public void toStringNullInputStreamNullEncoding() {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullInputStream(), null));
    }

    @Test
    public void toStringEmptyInputStreamNullEncoding() {
        assertThrows(
                NullPointerException.class,
                () -> assertEquals(IOUtil.toString(emptyInputStream(), null), emptyString()));
    }

    @Test
    public void toStringInputStreamNullEncoding() {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string ⍅ï";
            assertEquals(
                    IOUtil.toString(new ByteArrayInputStream(probe.getBytes()), null)
                            .getBytes(),
                    probe.getBytes());
        });
    }

    @Test
    public void toStringNullInputStreamJunkEncoding() {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullInputStream(), "junk"));
    }

    @Test
    public void toStringEmptyInputStreamJunkEncoding() {
        assertThrows(
                UnsupportedEncodingException.class,
                () -> assertEquals(IOUtil.toString(emptyInputStream(), "junk"), emptyString()));
    }

    @Test
    public void toStringInputStreamJunkEncoding() {
        assertThrows(UnsupportedEncodingException.class, () -> {
            String probe = "A string ⍅ï";
            assertEquals(
                    IOUtil.toString(new ByteArrayInputStream(probe.getBytes()), "junk")
                            .getBytes(),
                    probe.getBytes());
        });
    }

    @Test
    public void toStringNullInputStreamValidEncoding() {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullInputStream(), "utf-16"));
    }

    @Test
    public void toStringEmptyInputStreamValidEncoding() throws Exception {
        assertEquals(IOUtil.toString(emptyInputStream(), "utf-16"), emptyString());
    }

    @Test
    public void toStringInputStreamValidEncoding() throws Exception {
        String probe = "A string ⍅ï";
        assertArrayEquals(
                IOUtil.toString(new ByteArrayInputStream(probe.getBytes("utf-16")), "utf-16")
                        .getBytes("utf-8"),
                probe.getBytes("utf-8"));
    }

    @Test
    public void toStringNullInputStreamNullEncodingNegBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullInputStream(), null, -1));
    }

    @Test
    public void toStringEmptyInputStreamNullEncodingNegBufSz() {
        assertThrows(
                NullPointerException.class,
                () -> assertEquals(IOUtil.toString(emptyInputStream(), null, -1), emptyString()));
    }

    @Test
    public void toStringInputStreamNullEncodingNegBufSz() {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string ⍅ï";
            assertEquals(
                    IOUtil.toString(new ByteArrayInputStream(probe.getBytes()), null, -1)
                            .getBytes(),
                    probe.getBytes());
        });
    }

    @Test
    public void toStringNullInputStreamJunkEncodingNegBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullInputStream(), "junk", -1));
    }

    @Test
    public void toStringEmptyInputStreamJunkEncodingNegBufSz() {
        assertThrows(
                UnsupportedEncodingException.class,
                () -> assertEquals(IOUtil.toString(emptyInputStream(), "junk", -1), emptyString()));
    }

    @Test
    public void toStringInputStreamJunkEncodingNegBufSz() {
        assertThrows(UnsupportedEncodingException.class, () -> {
            String probe = "A string ⍅ï";
            assertEquals(
                    IOUtil.toString(new ByteArrayInputStream(probe.getBytes()), "junk", -1)
                            .getBytes(),
                    probe.getBytes());
        });
    }

    @Test
    public void toStringNullInputStreamValidEncodingNegBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullInputStream(), "utf-16", -1));
    }

    @Test
    public void toStringEmptyInputStreamValidEncodingNegBufSz() {
        assertThrows(
                NegativeArraySizeException.class,
                () -> assertEquals(IOUtil.toString(emptyInputStream(), "utf-16", -1), emptyString()));
    }

    @Test
    public void toStringInputStreamValidEncodingNegBufSz() {
        assertThrows(NegativeArraySizeException.class, () -> {
            String probe = "A string ⍅ï";
            assertArrayEquals(
                    IOUtil.toString(new ByteArrayInputStream(probe.getBytes(UTF_16)), "utf-16", -1)
                            .getBytes(UTF_8),
                    probe.getBytes(UTF_8));
        });
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void toStringNullInputStreamNullEncodingZeroBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullInputStream(), null, 0));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void toStringEmptyInputStreamNullEncodingZeroBufSz() {
        assertThrows(
                NullPointerException.class,
                () -> assertEquals(IOUtil.toString(emptyInputStream(), null, 0), emptyString()));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void toStringInputStreamNullEncodingZeroBufSz() {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string ⍅ï";
            assertEquals(
                    IOUtil.toString(new ByteArrayInputStream(probe.getBytes()), null, 0)
                            .getBytes(),
                    probe.getBytes());
        });
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void toStringNullInputStreamJunkEncodingZeroBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullInputStream(), "junk", 0));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void toStringEmptyInputStreamJunkEncodingZeroBufSz() {
        assertThrows(
                UnsupportedEncodingException.class,
                () -> assertEquals(IOUtil.toString(emptyInputStream(), "junk", 0), emptyString()));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void toStringInputStreamJunkEncodingZeroBufSz() {
        assertThrows(UnsupportedEncodingException.class, () -> {
            String probe = "A string ⍅ï";
            assertEquals(
                    IOUtil.toString(new ByteArrayInputStream(probe.getBytes()), "junk", 0)
                            .getBytes(),
                    probe.getBytes());
        });
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void toStringNullInputStreamValidEncodingZeroBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullInputStream(), "utf-16", 0));
    }

    /*
     * copy(InputStream,Writer)
     */

    @Test
    public void copyNullInputStreamNullWriter() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullInputStream(), nullWriter()));
    }

    @Test
    public void copyEmptyInputStreamNullWriter() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyInputStream(), nullWriter()));
    }

    @Test
    public void copyEmptyInputStreamValidWriter() throws Exception {
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy(emptyInputStream(), writer);
        assertEquals(emptyString(), writer.toString());
    }

    @Test
    public void copyInputStreamNullWriter() {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string ⍅ï";
            IOUtil.copy(new ByteArrayInputStream(probe.getBytes()), nullWriter());
        });
    }

    @Test
    public void copyInputStreamValidWriter() throws Exception {
        String probe = "A string ⍅ï";
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy(new ByteArrayInputStream(probe.getBytes()), writer);
        assertArrayEquals(probe.getBytes(), writer.toString().getBytes());
    }

    /*
     * copy(InputStream,Writer,int)
     */

    @Test
    public void copyNullInputStreamNullWriterNegBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullInputStream(), nullWriter(), -1));
    }

    @Test
    public void copyEmptyInputStreamNullWriterNegBufSz() {
        assertThrows(NegativeArraySizeException.class, () -> IOUtil.copy(emptyInputStream(), nullWriter(), -1));
    }

    @Test
    public void copyEmptyInputStreamValidWriterNegBufSz() {
        assertThrows(
                NegativeArraySizeException.class,
                () -> IOUtil.copy(emptyInputStream(), new DontCloseStringWriter(), -1));
    }

    @Test
    public void copyInputStreamNullWriterNegBufSz() {
        assertThrows(NegativeArraySizeException.class, () -> {
            String probe = "A string ⍅ï";
            IOUtil.copy(new ByteArrayInputStream(probe.getBytes()), nullWriter(), -1);
        });
    }

    @Test
    public void copyInputStreamValidWriterNegBufSz() {
        assertThrows(NegativeArraySizeException.class, () -> {
            String probe = "A string ⍅ï";
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(new ByteArrayInputStream(probe.getBytes()), writer, -1);
            assertArrayEquals(probe.getBytes(), writer.toString().getBytes());
        });
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyNullInputStreamNullWriterZeroBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullInputStream(), nullWriter(), 0));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyNullInputStreamValidWriterZeroBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullInputStream(), new DontCloseStringWriter(), 0));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyEmptyInputStreamNullWriterZeroBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyInputStream(), nullWriter(), 0));
    }

    @Test
    public void copyNullInputStreamNullWriterPosBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullInputStream(), nullWriter(), 1));
    }

    @Test
    public void copyNullInputStreamValidWriterPosBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullInputStream(), new DontCloseStringWriter(), 1));
    }

    @Test
    public void copyEmptyInputStreamNullWriterPosBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyInputStream(), nullWriter(), 1));
    }

    @Test
    public void copyEmptyInputStreamValidWriterPosBufSz() throws Exception {
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy(emptyInputStream(), writer, 1);
        assertEquals(emptyString(), writer.toString());
    }

    @Test
    public void copyInputStreamValidWriterPosBufSz() throws Exception {
        String probe = "A string ⍅ï";
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy(new ByteArrayInputStream(probe.getBytes()), writer, 1);
        assertArrayEquals(probe.getBytes(), writer.toString().getBytes());
    }

    /*
     * copy(InputStream,Writer,String)
     */

    @Test
    public void copyNullInputStreamNullWriterNullEncoding() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullInputStream(), nullWriter(), null));
    }

    @Test
    public void copyNullInputStreamValidWriterNullEncoding() {
        assertThrows(
                NullPointerException.class, () -> IOUtil.copy(nullInputStream(), new DontCloseStringWriter(), null));
    }

    @Test
    public void copyEmptyInputStreamNullWriterNullEncoding() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyInputStream(), nullWriter(), null));
    }

    @Test
    public void copyEmptyInputStreamValidWriterNullEncoding() {
        assertThrows(
                NullPointerException.class, () -> IOUtil.copy(emptyInputStream(), new DontCloseStringWriter(), null));
    }

    @Test
    public void copyInputStreamNullEncoding() {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string ⍅ï";
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(new ByteArrayInputStream(probe.getBytes()), writer, null);
            assertArrayEquals(probe.getBytes(), writer.toString().getBytes());
        });
    }

    @Test
    public void copyNullInputStreamNullWriterJunkEncoding() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullInputStream(), nullWriter(), "junk"));
    }

    @Test
    public void copyNullInputStreamValidWriterJunkEncoding() {
        assertThrows(
                NullPointerException.class, () -> IOUtil.copy(nullInputStream(), new DontCloseStringWriter(), "junk"));
    }

    @Test
    public void copyEmptyInputStreamNullWriterJunkEncoding() {
        assertThrows(UnsupportedEncodingException.class, () -> IOUtil.copy(emptyInputStream(), nullWriter(), "junk"));
    }

    @Test
    public void copyEmptyInputStreamValidWriterJunkEncoding() {
        assertThrows(
                UnsupportedEncodingException.class,
                () -> IOUtil.copy(emptyInputStream(), new DontCloseStringWriter(), "junk"));
    }

    @Test
    public void copyInputStreamNullWriterJunkEncoding() {
        assertThrows(UnsupportedEncodingException.class, () -> {
            String probe = "A string ⍅ï";
            IOUtil.copy(new ByteArrayInputStream(probe.getBytes()), nullWriter(), "junk");
        });
    }

    @Test
    public void copyInputStreamValidWriterJunkEncoding() {
        assertThrows(UnsupportedEncodingException.class, () -> {
            String probe = "A string ⍅ï";
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(new ByteArrayInputStream(probe.getBytes()), writer, "junk");
            assertArrayEquals(probe.getBytes(), writer.toString().getBytes());
        });
    }

    @Test
    public void copyNullInputStreamNullWriterValidEncoding() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullInputStream(), nullWriter(), "utf-16"));
    }

    @Test
    public void copyEmptyInputStreamNullWriterValidEncoding() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyInputStream(), nullWriter(), "utf-16"));
    }

    @Test
    public void copyNullInputStreamValidWriterValidEncoding() {
        assertThrows(
                NullPointerException.class,
                () -> IOUtil.copy(nullInputStream(), new DontCloseStringWriter(), "utf-16"));
    }

    @Test
    public void copyEmptyInputStreamValidWriterValidEncoding() throws Exception {
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy(emptyInputStream(), writer, "utf-16");
        assertEquals(emptyString(), writer.toString());
    }

    @Test
    public void copyInputStreamNullWriterValidEncoding() {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string ⍅ï";
            IOUtil.copy(new ByteArrayInputStream(probe.getBytes("utf-16")), nullWriter(), "utf-16");
        });
    }

    @Test
    public void copyInputStreamValidWriterValidEncoding() throws Exception {
        String probe = "A string ⍅ï";
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy(new ByteArrayInputStream(probe.getBytes(UTF_16)), writer, "utf-16");
        assertArrayEquals(probe.getBytes(UTF_8), writer.toString().getBytes(UTF_8));
    }

    /*
     * copy(InputStream,Writer,String,int)
     */

    @Test
    public void copyNullInputStreamNullWriterNullEncodingNegBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullInputStream(), nullWriter(), null, -1));
    }

    @Test
    public void copyNullInputStreamValidWriterNullEncodingNegBufSz() {
        assertThrows(
                NullPointerException.class,
                () -> IOUtil.copy(nullInputStream(), new DontCloseStringWriter(), null, -1));
    }

    @Test
    public void copyEmptyInputStreamNullWriterNullEncodingNegBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyInputStream(), nullWriter(), null, -1));
    }

    @Test
    public void copyEmptyInputStreamValidWriterNullEncodingNegBufSz() {
        assertThrows(NullPointerException.class, () -> {
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(emptyInputStream(), writer, null, -1);
            assertEquals(emptyString(), writer.toString());
        });
    }

    @Test
    public void copyInputStreamNullWriterNullEncodingNegBufSz() {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string ⍅ï";
            IOUtil.copy(new ByteArrayInputStream(probe.getBytes()), nullWriter(), null, -1);
        });
    }

    @Test
    public void copyInputStreamValidWriterNullEncodingNegBufSz() {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string ⍅ï";
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(new ByteArrayInputStream(probe.getBytes()), writer, null, -1);
            assertArrayEquals(probe.getBytes(), writer.toString().getBytes());
        });
    }

    @Test
    public void copyNullInputStreamNullWriterJunkEncodingNegBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullInputStream(), nullWriter(), "junk", -1));
    }

    @Test
    public void copyNullInputStreamValidWriterJunkEncodingNegBufSz() {
        assertThrows(
                NullPointerException.class,
                () -> IOUtil.copy(nullInputStream(), new DontCloseStringWriter(), "junk", -1));
    }

    @Test
    public void copyEmptyInputStreamNullWriterJunkEncodingNegBufSz() {
        assertThrows(
                UnsupportedEncodingException.class, () -> IOUtil.copy(emptyInputStream(), nullWriter(), "junk", -1));
    }

    @Test
    public void copyEmptyInputStreamJunkEncodingNegBufSz() {
        assertThrows(UnsupportedEncodingException.class, () -> {
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(emptyInputStream(), writer, "junk", -1);
            assertEquals(emptyString(), writer.toString());
        });
    }

    @Test
    public void copyInputStreamNullWriterJunkEncodingNegBufSz() {
        assertThrows(UnsupportedEncodingException.class, () -> {
            String probe = "A string ⍅ï";
            IOUtil.copy(new ByteArrayInputStream(probe.getBytes()), nullWriter(), "junk", -1);
        });
    }

    @Test
    public void copyInputStreamValidWriterJunkEncodingNegBufSz() {
        assertThrows(UnsupportedEncodingException.class, () -> {
            String probe = "A string ⍅ï";
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(new ByteArrayInputStream(probe.getBytes()), writer, "junk", -1);
            assertArrayEquals(probe.getBytes(), writer.toString().getBytes());
        });
    }

    @Test
    public void copyNullInputStreamNullWriterValidEncodingNegBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullInputStream(), nullWriter(), "utf-16", -1));
    }

    @Test
    public void copyNullInputStreamValidWriterValidEncodingNegBufSz() {
        assertThrows(
                NullPointerException.class,
                () -> IOUtil.copy(nullInputStream(), new DontCloseStringWriter(), "utf-16", -1));
    }

    @Test
    public void copyEmptyInputStreamNullWriterValidEncodingNegBufSz() {
        assertThrows(
                NegativeArraySizeException.class, () -> IOUtil.copy(emptyInputStream(), nullWriter(), "utf-16", -1));
    }

    @Test
    public void copyEmptyInputStreamValidWriterValidEncodingNegBufSz() {
        assertThrows(NegativeArraySizeException.class, () -> {
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(emptyInputStream(), writer, "utf-16", -1);
            assertEquals(emptyString(), writer.toString());
        });
    }

    @Test
    public void copyInputStreamNullWriterValidEncodingNegBufSz() {
        assertThrows(NegativeArraySizeException.class, () -> {
            String probe = "A string ⍅ï";
            IOUtil.copy(new ByteArrayInputStream(probe.getBytes("utf-16")), nullWriter(), -1);
        });
    }

    @Test
    public void copyInputStreamValidEncodingNegBufSz() {
        assertThrows(NegativeArraySizeException.class, () -> {
            String probe = "A string ⍅ï";
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(new ByteArrayInputStream(probe.getBytes(UTF_16)), writer, "utf-16", -1);
            assertEquals(probe.getBytes(UTF_8), writer.toString().getBytes(UTF_8));
        });
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyNullInputStreamNullWriterNullEncodingZeroBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullInputStream(), nullWriter(), null, 0));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyNullInputStreamValidWriterNullEncodingZeroBufSz() {
        assertThrows(
                NullPointerException.class, () -> IOUtil.copy(nullInputStream(), new DontCloseStringWriter(), null, 0));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyEmptyInputStreamNullWriterNullEncodingZeroBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyInputStream(), nullWriter(), null, 0));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyEmptyInputStreamValidWriterNullEncodingZeroBufSz() {
        assertThrows(NullPointerException.class, () -> {
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(emptyInputStream(), writer, null, 0);
            assertEquals(emptyString(), writer.toString());
        });
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyInputStreamNullWriterNullEncodingZeroBufSz() {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string ⍅ï";
            IOUtil.copy(new ByteArrayInputStream(probe.getBytes()), nullWriter(), null, 0);
        });
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyInputStreamValidWriterNullEncodingZeroBufSz() {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string ⍅ï";
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(new ByteArrayInputStream(probe.getBytes()), writer, null, 0);
            assertArrayEquals(probe.getBytes(), writer.toString().getBytes());
        });
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyNullInputStreamNullWriterJunkEncodingZeroBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullInputStream(), nullWriter(), "junk", 0));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyNullInputStreamValidWriterJunkEncodingZeroBufSz() {
        assertThrows(
                NullPointerException.class,
                () -> IOUtil.copy(nullInputStream(), new DontCloseStringWriter(), "junk", 0));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyEmptyInputStreamNullWriterJunkEncodingZeroBufSz() {
        assertThrows(
                UnsupportedEncodingException.class, () -> IOUtil.copy(emptyInputStream(), nullWriter(), "junk", 0));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyEmptyInputStreamValidWriterJunkEncodingZeroBufSz() {
        assertThrows(UnsupportedEncodingException.class, () -> {
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(emptyInputStream(), writer, "junk", 0);
            assertEquals(emptyString(), writer.toString());
        });
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyInputStreamNullWriterJunkEncodingZeroBufSz() {
        assertThrows(UnsupportedEncodingException.class, () -> {
            String probe = "A string ⍅ï";
            IOUtil.copy(new ByteArrayInputStream(probe.getBytes()), nullWriter(), "junk", 0);
        });
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyInputStreamValidWriterJunkEncodingZeroBufSz() {
        assertThrows(UnsupportedEncodingException.class, () -> {
            String probe = "A string ⍅ï";
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(new ByteArrayInputStream(probe.getBytes()), writer, "junk", 0);
            assertArrayEquals(probe.getBytes(), writer.toString().getBytes());
        });
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyNullInputStreamNullWriterValidEncodingZeroBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullInputStream(), nullWriter(), "utf-16", 0));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyNullInputStreamValidWriterValidEncodingZeroBufSz() {
        assertThrows(
                NullPointerException.class,
                () -> IOUtil.copy(nullInputStream(), new DontCloseStringWriter(), "utf-16", 0));
    }

    /*
     * copy(String,Writer)
     */

    @Test
    public void copyNullStringNullWriter() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullString(), nullWriter()));
    }

    @Test
    public void copyEmptyStringNullWriter() {
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
        assertEquals(emptyString(), writer.toString());
    }

    @Test
    public void copyStringNullWriter() {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string ⍅ï";
            IOUtil.copy(probe, nullWriter());
        });
    }

    @Test
    public void copyStringValidWriter() throws Exception {
        String probe = "A string ⍅ï";
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy(probe, writer);
        assertEquals(probe, writer.toString());
    }

    @Test
    public void copyNullStringNullOutputStream() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullString(), nullOutputStream()));
    }

    @Test
    public void copyEmptyStringNullOutputStream() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyString(), nullOutputStream()));
    }

    @Test
    public void copyNullStringValidOutputStream() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullString(), new DontCloseByteArrayOutputStream()));
    }

    @Test
    public void copyEmptyStringValidOutputStream() throws Exception {
        ByteArrayOutputStream os = new DontCloseByteArrayOutputStream();
        IOUtil.copy(emptyString(), os);
        assertArrayEquals(os.toByteArray(), emptyString().getBytes());
    }

    @Test
    public void copyStringNullOutputStream() {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string ⍅ï";
            IOUtil.copy(probe, nullOutputStream());
        });
    }

    @Test
    public void copyStringValidOutputStream() throws Exception {
        String probe = "A string ⍅ï";
        ByteArrayOutputStream os = new DontCloseByteArrayOutputStream();
        IOUtil.copy(probe, os);
        assertArrayEquals(probe.getBytes(), os.toByteArray());
    }

    @Test
    public void copyNullStringNullOutputStreamNegBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullString(), nullOutputStream(), -1));
    }

    @Test
    public void copyEmptyStringNullOutputStreamNegBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyString(), nullOutputStream(), -1));
    }

    @Test
    public void copyNullStringValidOutputStreamNegBufSz() {
        assertThrows(
                NullPointerException.class, () -> IOUtil.copy(nullString(), new DontCloseByteArrayOutputStream(), -1));
    }

    @Test
    public void copyEmptyStringValidOutputStreamNegBufSz() {
        assertThrows(NegativeArraySizeException.class, () -> {
            ByteArrayOutputStream os = new DontCloseByteArrayOutputStream();
            IOUtil.copy(emptyString(), os, -1);
            assertArrayEquals(os.toByteArray(), emptyString().getBytes());
        });
    }

    @Test
    public void copyStringNullOutputStreamNegBufSz() {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string ⍅ï";
            IOUtil.copy(probe, nullOutputStream(), -1);
        });
    }

    @Test
    public void copyStringValidOutputStreamNegBufSz() {
        assertThrows(NegativeArraySizeException.class, () -> {
            String probe = "A string ⍅ï";
            ByteArrayOutputStream os = new DontCloseByteArrayOutputStream();
            IOUtil.copy(probe, os, -1);
            assertEquals(probe.getBytes(), os.toByteArray());
        });
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyNullStringNullOutputStreamZeroBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullString(), nullOutputStream(), 0));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyEmptyStringNullOutputStreamZeroBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyString(), nullOutputStream(), 0));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyNullStringValidOutputStreamZeroBufSz() {
        assertThrows(
                NullPointerException.class, () -> IOUtil.copy(nullString(), new DontCloseByteArrayOutputStream(), 0));
    }

    @Test
    public void copyNullStringNullOutputStreamPosBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullString(), nullOutputStream(), 1));
    }

    @Test
    public void copyEmptyStringNullOutputStreamPosBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyString(), nullOutputStream(), 1));
    }

    @Test
    public void copyNullStringValidOutputStreamPosBufSz() {
        assertThrows(
                NullPointerException.class, () -> IOUtil.copy(nullString(), new DontCloseByteArrayOutputStream(), 1));
    }

    @Test
    public void copyEmptyStringValidOutputStreamPosBufSz() throws Exception {
        ByteArrayOutputStream os = new DontCloseByteArrayOutputStream();
        IOUtil.copy(emptyString(), os, 1);
        assertArrayEquals(os.toByteArray(), emptyString().getBytes());
    }

    @Test
    public void copyStringNullOutputStreamPosBufSz() {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string ⍅ï";
            IOUtil.copy(probe, nullOutputStream(), 1);
        });
    }

    @Test
    public void copyStringValidOutputStreamPosBufSz() throws Exception {
        String probe = "A string ⍅ï";
        ByteArrayOutputStream os = new DontCloseByteArrayOutputStream();
        IOUtil.copy(probe, os, 1);
        assertArrayEquals(probe.getBytes(), os.toByteArray());
    }

    @Test
    public void copyNullReaderNullWriter() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullReader(), nullWriter()));
    }

    @Test
    public void copyEmptyReaderNullWriter() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyReader(), nullWriter()));
    }

    @Test
    public void copyNullReaderValidWriter() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullReader(), new DontCloseStringWriter()));
    }

    @Test
    public void copyEmptyReaderValidWriter() throws Exception {
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy(emptyReader(), writer);
        assertEquals(emptyString(), writer.toString());
    }

    @Test
    public void copyReaderNullWriter() {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string ⍅ï";
            IOUtil.copy(new StringReader(probe), nullWriter());
        });
    }

    @Test
    public void copyReaderValidWriter() throws Exception {
        String probe = "A string ⍅ï";
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy(new StringReader(probe), writer);
        assertEquals(probe, writer.toString());
    }

    /*
     * copy(Reader,Writer,int)
     */

    @Test
    public void copyNullReaderNullWriterNegBufSz() {
        assertThrows(NegativeArraySizeException.class, () -> IOUtil.copy(nullReader(), nullWriter(), -1));
    }

    @Test
    public void copyEmptyReaderNullWriterNegBufSz() {
        assertThrows(NegativeArraySizeException.class, () -> IOUtil.copy(emptyReader(), nullWriter(), -1));
    }

    @Test
    public void copyNullReaderValidWriterNegBufSz() {
        assertThrows(
                NegativeArraySizeException.class, () -> IOUtil.copy(nullReader(), new DontCloseStringWriter(), -1));
    }

    @Test
    public void copyEmptyReaderValidWriterNegBufSz() {
        assertThrows(NegativeArraySizeException.class, () -> {
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(emptyReader(), writer, -1);
            assertEquals(emptyString(), writer.toString());
        });
    }

    @Test
    public void copyReaderNullWriterNegBufSz() {
        assertThrows(NegativeArraySizeException.class, () -> {
            String probe = "A string ⍅ï";
            IOUtil.copy(new StringReader(probe), nullWriter(), -1);
        });
    }

    @Test
    public void copyReaderValidWriterNegBufSz() {
        assertThrows(NegativeArraySizeException.class, () -> {
            String probe = "A string ⍅ï";
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(new StringReader(probe), writer, -1);
            assertEquals(probe, writer.toString());
        });
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyNullReaderNullWriterZeroBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullReader(), nullWriter(), 0));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyEmptyReaderNullWriterZeroBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyReader(), nullWriter(), 0));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyNullReaderValidWriterZeroBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullReader(), new DontCloseStringWriter(), 0));
    }

    @Test
    public void copyNullReaderNullWriterPosBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullReader(), nullWriter(), 1));
    }

    @Test
    public void copyEmptyReaderNullWriterPosBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyReader(), nullWriter(), 1));
    }

    @Test
    public void copyNullReaderValidWriterPosBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullReader(), new DontCloseStringWriter(), 1));
    }

    @Test
    public void copyEmptyReaderValidWriterPosBufSz() throws Exception {
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy(emptyReader(), writer, 1);
        assertEquals(emptyString(), writer.toString());
    }

    @Test
    public void copyReaderNullWriterPosBufSz() {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string ⍅ï";
            IOUtil.copy(new StringReader(probe), nullWriter(), 1);
        });
    }

    @Test
    public void copyReaderValidWriterPosBufSz() throws Exception {
        String probe = "A string ⍅ï";
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy(new StringReader(probe), writer, 1);
        assertEquals(probe, writer.toString());
    }

    /*
     * toByteArray(InputStream,int)
     */

    @Test
    public void toByteArrayFromInputStreamNegBufSz() {
        assertThrows(NegativeArraySizeException.class, () -> {
            String probe = "A string ⍅ï";
            assertEquals(
                    IOUtil.toByteArray(new DontCloseByteArrayInputStream(IOUtil.toByteArray(probe)), -1),
                    probe.getBytes());
        });
    }

    @Test
    public void toByteArrayNullInputStreamNegBufSz() {
        assertThrows(NegativeArraySizeException.class, () -> IOUtil.toByteArray(nullInputStream(), -1));
    }

    @Test
    public void toByteArrayFromInputStreamPosBufSz() throws Exception {
        String probe = "A string ⍅ï";
        assertArrayEquals(
                IOUtil.toByteArray(new DontCloseByteArrayInputStream(IOUtil.toByteArray(probe)), +1), probe.getBytes());
    }

    @Test
    public void toByteArrayNullInputStreamPosBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.toByteArray(nullInputStream(), +1));
    }

    /*
     * toByteArray(Reader,int)
     */

    @Test
    public void toByteArrayFromReaderNegBufSz() {
        assertThrows(NegativeArraySizeException.class, () -> {
            String probe = "A string ⍅ï";
            assertEquals(IOUtil.toByteArray(new DontCloseStringReader(probe), -1), probe.getBytes());
        });
    }

    @Test
    public void toByteArrayNullReaderNegBufSz() {
        assertThrows(NegativeArraySizeException.class, () -> IOUtil.toByteArray(nullReader(), -1));
    }

    @Test
    public void toByteArrayFromReaderPosBufSz() throws Exception {
        String probe = "A string ⍅ï";
        assertArrayEquals(IOUtil.toByteArray(new DontCloseStringReader(probe), +1), probe.getBytes());
    }

    @Test
    public void toByteArrayNullReaderPosBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.toByteArray(nullReader(), +1));
    }

    /*
     * toByteArray(String,int)
     */

    @Test
    public void toByteArrayFromStringNegBufSz() {
        assertThrows(NegativeArraySizeException.class, () -> {
            String probe = "A string ⍅ï";
            assertEquals(IOUtil.toByteArray(probe, -1), probe.getBytes());
        });
    }

    @Test
    public void toByteArrayNullStringNegBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.toByteArray(nullString(), -1));
    }

    @Test
    public void toByteArrayFromStringPosBufSz() throws Exception {
        String probe = "A string ⍅ï";
        assertArrayEquals(IOUtil.toByteArray(probe, +1), probe.getBytes());
    }

    @Test
    public void toByteArrayNullStringPosBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.toByteArray(nullString(), +1));
    }

    /*
     * toString(Reader,int)
     */

    @Test
    public void toStringFromReaderNegBufSz() {
        assertThrows(NegativeArraySizeException.class, () -> {
            String probe = "A string ⍅ï";
            assertEquals(probe, IOUtil.toString(new DontCloseStringReader(probe), -1));
        });
    }

    @Test
    public void toStringNullReaderNegBufSz() {
        assertThrows(NegativeArraySizeException.class, () -> IOUtil.toString(nullReader(), -1));
    }

    @Test
    public void toStringFromReaderPosBufSz() throws Exception {
        String probe = "A string ⍅ï";
        assertEquals(probe, IOUtil.toString(new DontCloseStringReader(probe), +1));
    }

    @Test
    public void toStringNullReaderPosBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.toString(nullReader(), +1));
    }

    /*
     * copy(Reader,OutputStream)
     */

    @Test
    public void copyNullReaderNullOutputStream() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullReader(), nullOutputStream()));
    }

    @Test
    public void copyNullReaderValidOutputStream() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullReader(), new DontCloseByteArrayOutputStream()));
    }

    @Test
    public void copyEmptyReaderNullOutputStream() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyReader(), nullOutputStream()));
    }

    @Test
    public void copyEmptyReaderValidOutputStream() throws Exception {
        IOUtil.copy(emptyReader(), new DontCloseByteArrayOutputStream());
    }

    @Test
    public void copyReaderValidOutputStream() throws Exception {
        ByteArrayOutputStream outputStream = new DontCloseByteArrayOutputStream();
        String probe = "A string ⍅ï";
        IOUtil.copy(new DontCloseStringReader(probe), outputStream);
        assertArrayEquals(probe.getBytes(), outputStream.toByteArray());
    }

    /*
     * copy(Reader,OutputStream,int)
     */

    @Test
    public void copyNullReaderNullOutputStreamNegBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullReader(), nullOutputStream(), -1));
    }

    @Test
    public void copyNullReaderValidOutputStreamNegBufSz() {
        assertThrows(
                NegativeArraySizeException.class,
                () -> IOUtil.copy(nullReader(), new DontCloseByteArrayOutputStream(), -1));
    }

    @Test
    public void copyEmptyReaderNullOutputStreamNegBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyReader(), nullOutputStream(), -1));
    }

    @Test
    public void copyEmptyReaderValidOutputStreamNegBufSz() {
        assertThrows(
                NegativeArraySizeException.class,
                () -> IOUtil.copy(emptyReader(), new DontCloseByteArrayOutputStream(), -1));
    }

    @Test
    public void copyReaderValidOutputStreamNegBufSz() {
        assertThrows(NegativeArraySizeException.class, () -> {
            ByteArrayOutputStream outputStream = new DontCloseByteArrayOutputStream();
            String probe = "A string ⍅ï";
            IOUtil.copy(new DontCloseStringReader(probe), outputStream, -1);
            assertEquals(probe.getBytes(), outputStream.toByteArray());
        });
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyNullReaderNullOutputStreamZeroBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullReader(), nullOutputStream(), 0));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyNullReaderValidOutputStreamZeroBufSz() {
        assertThrows(
                NullPointerException.class, () -> IOUtil.copy(nullReader(), new DontCloseByteArrayOutputStream(), 0));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyEmptyReaderNullOutputStreamZeroBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyReader(), nullOutputStream(), 0));
    }

    @Test
    public void copyNullReaderNullOutputStreamPosBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullReader(), nullOutputStream(), 1));
    }

    @Test
    public void copyNullReaderValidOutputStreamPosBufSz() {
        assertThrows(
                NullPointerException.class, () -> IOUtil.copy(nullReader(), new DontCloseByteArrayOutputStream(), 1));
    }

    @Test
    public void copyEmptyReaderNullOutputStreamPosBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyReader(), nullOutputStream(), 1));
    }

    @Test
    public void copyEmptyReaderValidOutputStreamPosBufSz() throws Exception {
        IOUtil.copy(emptyReader(), new DontCloseByteArrayOutputStream(), 1);
    }

    @Test
    public void copyReaderValidOutputStreamPosBufSz() throws Exception {
        ByteArrayOutputStream outputStream = new DontCloseByteArrayOutputStream();
        String probe = "A string ⍅ï";
        IOUtil.copy(new DontCloseStringReader(probe), outputStream, 1);
        assertArrayEquals(probe.getBytes(), outputStream.toByteArray());
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
    public void copyNullByteArrayNullWriter() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullByteArray(), nullWriter()));
    }

    @Test
    public void copyEmptyByteArrayNullWriter() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyByteArray(), nullWriter()));
    }

    @Test
    public void copyEmptyByteArrayValidWriter() throws Exception {
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy(emptyByteArray(), writer);
        assertEquals(emptyString(), writer.toString());
    }

    @Test
    public void copyByteArrayNullWriter() {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string ⍅ï";
            IOUtil.copy(probe.getBytes(), nullWriter());
        });
    }

    @Test
    public void copyByteArrayValidWriter() throws Exception {
        String probe = "A string ⍅ï";
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy(probe.getBytes(), writer);
        assertArrayEquals(probe.getBytes(), writer.toString().getBytes());
    }

    /*
     * copy(byte[],Writer,int)
     */

    @Test
    public void copyNullByteArrayNullWriterNegBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullByteArray(), nullWriter(), -1));
    }

    @Test
    public void copyEmptyByteArrayNullWriterNegBufSz() {
        assertThrows(NegativeArraySizeException.class, () -> IOUtil.copy(emptyByteArray(), nullWriter(), -1));
    }

    @Test
    public void copyEmptyByteArrayValidWriterNegBufSz() {
        assertThrows(
                NegativeArraySizeException.class, () -> IOUtil.copy(emptyByteArray(), new DontCloseStringWriter(), -1));
    }

    @Test
    public void copyByteArrayNullWriterNegBufSz() {
        assertThrows(NegativeArraySizeException.class, () -> {
            String probe = "A string ⍅ï";
            IOUtil.copy(probe.getBytes(), nullWriter(), -1);
        });
    }

    @Test
    public void copyByteArrayValidWriterNegBufSz() {
        assertThrows(NegativeArraySizeException.class, () -> {
            String probe = "A string ⍅ï";
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(probe.getBytes(), writer, -1);
            assertArrayEquals(probe.getBytes(), writer.toString().getBytes());
        });
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyNullByteArrayNullWriterZeroBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullByteArray(), nullWriter(), 0));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyNullByteArrayValidWriterZeroBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullByteArray(), new DontCloseStringWriter(), 0));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyEmptyByteArrayNullWriterZeroBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyByteArray(), nullWriter(), 0));
    }

    @Test
    public void copyNullByteArrayNullWriterPosBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullByteArray(), nullWriter(), 1));
    }

    @Test
    public void copyNullByteArrayValidWriterPosBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullByteArray(), new DontCloseStringWriter(), 1));
    }

    @Test
    public void copyEmptyByteArrayNullWriterPosBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyByteArray(), nullWriter(), 1));
    }

    @Test
    public void copyEmptyByteArrayValidWriterPosBufSz() throws Exception {
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy(emptyByteArray(), writer, 1);
        assertEquals(emptyString(), writer.toString());
    }

    @Test
    public void copyByteArrayValidWriterPosBufSz() throws Exception {
        String probe = "A string ⍅ï";
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy(probe.getBytes(), writer, 1);
        assertArrayEquals(probe.getBytes(), writer.toString().getBytes());
    }

    /*
     * copy(byte[],Writer,String)
     */

    @Test
    public void copyNullByteArrayNullWriterNullEncoding() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullByteArray(), nullWriter(), null));
    }

    @Test
    public void copyNullByteArrayValidWriterNullEncoding() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullByteArray(), new DontCloseStringWriter(), null));
    }

    @Test
    public void copyEmptyByteArrayNullWriterNullEncoding() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyByteArray(), nullWriter(), null));
    }

    @Test
    public void copyEmptyByteArrayValidWriterNullEncoding() {
        assertThrows(
                NullPointerException.class, () -> IOUtil.copy(emptyByteArray(), new DontCloseStringWriter(), null));
    }

    @Test
    public void copyByteArrayNullEncoding() {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string ⍅ï";
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(probe.getBytes(), writer, null);
            assertArrayEquals(probe.getBytes(), writer.toString().getBytes());
        });
    }

    @Test
    public void copyNullByteArrayNullWriterJunkEncoding() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullByteArray(), nullWriter(), "junk"));
    }

    @Test
    public void copyNullByteArrayValidWriterJunkEncoding() {
        assertThrows(
                NullPointerException.class, () -> IOUtil.copy(nullByteArray(), new DontCloseStringWriter(), "junk"));
    }

    @Test
    public void copyEmptyByteArrayNullWriterJunkEncoding() {
        assertThrows(UnsupportedEncodingException.class, () -> IOUtil.copy(emptyByteArray(), nullWriter(), "junk"));
    }

    @Test
    public void copyEmptyByteArrayValidWriterJunkEncoding() {
        assertThrows(
                UnsupportedEncodingException.class,
                () -> IOUtil.copy(emptyByteArray(), new DontCloseStringWriter(), "junk"));
    }

    @Test
    public void copyByteArrayNullWriterJunkEncoding() {
        assertThrows(UnsupportedEncodingException.class, () -> {
            String probe = "A string ⍅ï";
            IOUtil.copy(probe.getBytes(), nullWriter(), "junk");
        });
    }

    @Test
    public void copyByteArrayValidWriterJunkEncoding() {
        assertThrows(UnsupportedEncodingException.class, () -> {
            String probe = "A string ⍅ï";
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(probe.getBytes(), writer, "junk");
            assertArrayEquals(probe.getBytes(), writer.toString().getBytes());
        });
    }

    @Test
    public void copyNullByteArrayNullWriterValidEncoding() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullByteArray(), nullWriter(), "utf-16"));
    }

    @Test
    public void copyEmptyByteArrayNullWriterValidEncoding() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyByteArray(), nullWriter(), "utf-16"));
    }

    @Test
    public void copyNullByteArrayValidWriterValidEncoding() {
        assertThrows(
                NullPointerException.class, () -> IOUtil.copy(nullByteArray(), new DontCloseStringWriter(), "utf-16"));
    }

    @Test
    public void copyEmptyByteArrayValidWriterValidEncoding() throws Exception {
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy(emptyByteArray(), writer, "utf-16");
        assertEquals(emptyString(), writer.toString());
    }

    @Test
    public void copyByteArrayNullWriterValidEncoding() {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string ⍅ï";
            IOUtil.copy(probe.getBytes("utf-16"), nullWriter(), "utf-16");
        });
    }

    @Test
    public void copyByteArrayValidWriterValidEncoding() throws Exception {
        String probe = "A string ⍅ï";
        StringWriter writer = new DontCloseStringWriter();
        IOUtil.copy(probe.getBytes(UTF_16), writer, "utf-16");
        assertArrayEquals(probe.getBytes(UTF_8), writer.toString().getBytes(UTF_8));
    }

    /*
     * copy(byte[],Writer,String,int)
     */

    @Test
    public void copyNullByteArrayNullWriterNullEncodingNegBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullByteArray(), nullWriter(), null, -1));
    }

    @Test
    public void copyNullByteArrayValidWriterNullEncodingNegBufSz() {
        assertThrows(
                NullPointerException.class, () -> IOUtil.copy(nullByteArray(), new DontCloseStringWriter(), null, -1));
    }

    @Test
    public void copyEmptyByteArrayNullWriterNullEncodingNegBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyByteArray(), nullWriter(), null, -1));
    }

    @Test
    public void copyEmptyByteArrayValidWriterNullEncodingNegBufSz() {
        assertThrows(NullPointerException.class, () -> {
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(emptyByteArray(), writer, null, -1);
            assertEquals(emptyString(), writer.toString());
        });
    }

    @Test
    public void copyByteArrayNullWriterNullEncodingNegBufSz() {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string ⍅ï";
            IOUtil.copy(probe.getBytes(), nullWriter(), null, -1);
        });
    }

    @Test
    public void copyByteArrayValidWriterNullEncodingNegBufSz() {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string ⍅ï";
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(probe.getBytes(), writer, null, -1);
            assertArrayEquals(probe.getBytes(), writer.toString().getBytes());
        });
    }

    @Test
    public void copyNullByteArrayNullWriterJunkEncodingNegBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullByteArray(), nullWriter(), "junk", -1));
    }

    @Test
    public void copyNullByteArrayValidWriterJunkEncodingNegBufSz() {
        assertThrows(
                NullPointerException.class,
                () -> IOUtil.copy(nullByteArray(), new DontCloseStringWriter(), "junk", -1));
    }

    @Test
    public void copyEmptyByteArrayNullWriterJunkEncodingNegBufSz() {
        assertThrows(UnsupportedEncodingException.class, () -> IOUtil.copy(emptyByteArray(), nullWriter(), "junk", -1));
    }

    @Test
    public void copyEmptyByteArrayJunkEncodingNegBufSz() {
        assertThrows(UnsupportedEncodingException.class, () -> {
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(emptyByteArray(), writer, "junk", -1);
            assertEquals(emptyString(), writer.toString());
        });
    }

    @Test
    public void copyByteArrayNullWriterJunkEncodingNegBufSz() {
        assertThrows(UnsupportedEncodingException.class, () -> {
            String probe = "A string ⍅ï";
            IOUtil.copy(probe.getBytes(), nullWriter(), "junk", -1);
        });
    }

    @Test
    public void copyByteArrayValidWriterJunkEncodingNegBufSz() {
        assertThrows(UnsupportedEncodingException.class, () -> {
            String probe = "A string ⍅ï";
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(probe.getBytes(), writer, "junk", -1);
            assertArrayEquals(probe.getBytes(), writer.toString().getBytes());
        });
    }

    @Test
    public void copyNullByteArrayNullWriterValidEncodingNegBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullByteArray(), nullWriter(), "utf-16", -1));
    }

    @Test
    public void copyNullByteArrayValidWriterValidEncodingNegBufSz() {
        assertThrows(
                NullPointerException.class,
                () -> IOUtil.copy(nullByteArray(), new DontCloseStringWriter(), "utf-16", -1));
    }

    @Test
    public void copyEmptyByteArrayNullWriterValidEncodingNegBufSz() {
        assertThrows(NegativeArraySizeException.class, () -> IOUtil.copy(emptyByteArray(), nullWriter(), "utf-16", -1));
    }

    @Test
    public void copyEmptyByteArrayValidWriterValidEncodingNegBufSz() {
        assertThrows(NegativeArraySizeException.class, () -> {
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(emptyByteArray(), writer, "utf-16", -1);
            assertEquals(emptyString(), writer.toString());
        });
    }

    @Test
    public void copyByteArrayNullWriterValidEncodingNegBufSz() {
        assertThrows(NegativeArraySizeException.class, () -> {
            String probe = "A string ⍅ï";
            IOUtil.copy(probe.getBytes("utf-16"), nullWriter(), -1);
        });
    }

    @Test
    public void copyByteArrayValidEncodingNegBufSz() {
        assertThrows(NegativeArraySizeException.class, () -> {
            String probe = "A string ⍅ï";
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(probe.getBytes("utf-16"), writer, "utf-16", -1);
            assertEquals(probe.getBytes("utf-8"), writer.toString().getBytes("utf-8"));
        });
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyNullByteArrayNullWriterNullEncodingZeroBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullByteArray(), nullWriter(), null, 0));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyNullByteArrayValidWriterNullEncodingZeroBufSz() {
        assertThrows(
                NullPointerException.class, () -> IOUtil.copy(nullByteArray(), new DontCloseStringWriter(), null, 0));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyEmptyByteArrayNullWriterNullEncodingZeroBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(emptyByteArray(), nullWriter(), null, 0));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyEmptyByteArrayValidWriterNullEncodingZeroBufSz() {
        assertThrows(NullPointerException.class, () -> {
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(emptyByteArray(), writer, null, 0);
            assertEquals(emptyString(), writer.toString());
        });
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyByteArrayNullWriterNullEncodingZeroBufSz() {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string ⍅ï";
            IOUtil.copy(probe.getBytes(), nullWriter(), null, 0);
        });
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyByteArrayValidWriterNullEncodingZeroBufSz() {
        assertThrows(NullPointerException.class, () -> {
            String probe = "A string ⍅ï";
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(probe.getBytes(), writer, null, 0);
            assertArrayEquals(probe.getBytes(), writer.toString().getBytes());
        });
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyNullByteArrayNullWriterJunkEncodingZeroBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullByteArray(), nullWriter(), "junk", 0));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyNullByteArrayValidWriterJunkEncodingZeroBufSz() {
        assertThrows(
                NullPointerException.class, () -> IOUtil.copy(nullByteArray(), new DontCloseStringWriter(), "junk", 0));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyEmptyByteArrayNullWriterJunkEncodingZeroBufSz() {
        assertThrows(UnsupportedEncodingException.class, () -> IOUtil.copy(emptyByteArray(), nullWriter(), "junk", 0));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyEmptyByteArrayValidWriterJunkEncodingZeroBufSz() {
        assertThrows(UnsupportedEncodingException.class, () -> {
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(emptyByteArray(), writer, "junk", 0);
            assertEquals(emptyString(), writer.toString());
        });
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyByteArrayNullWriterJunkEncodingZeroBufSz() {
        assertThrows(UnsupportedEncodingException.class, () -> {
            String probe = "A string ⍅ï";
            IOUtil.copy(probe.getBytes(), nullWriter(), "junk", 0);
        });
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyByteArrayValidWriterJunkEncodingZeroBufSz() {
        assertThrows(UnsupportedEncodingException.class, () -> {
            String probe = "A string ⍅ï";
            StringWriter writer = new DontCloseStringWriter();
            IOUtil.copy(probe.getBytes(), writer, "junk", 0);
            assertArrayEquals(probe.getBytes(), writer.toString().getBytes());
        });
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyNullByteArrayNullWriterValidEncodingZeroBufSz() {
        assertThrows(NullPointerException.class, () -> IOUtil.copy(nullByteArray(), nullWriter(), "utf-16", 0));
    }

    @Test
    @Timeout(value = INFINITE_LOOP_TIMEOUT, unit = TimeUnit.MILLISECONDS)
    public void copyNullByteArrayValidWriterValidEncodingZeroBufSz() {
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
        public void close() {
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
        public void close() {
            throw new UnsupportedOperationException("should not be called");
        }
    }

    private static class DontCloseByteArrayOutputStream extends ByteArrayOutputStream {
        @Override
        public void close() {
            throw new UnsupportedOperationException("should not be called");
        }
    }
}
