/* This file is part of VoltDB.
 * Copyright (C) 2008-2015 VoltDB Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with VoltDB.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.voltcore.utils;

import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.concurrent.Callable;

import org.voltcore.logging.VoltLogger;
import org.voltcore.utils.DBBPool.BBContainer;
import org.voltdb.utils.PosixAdvise;

/**
 * Utility class for accessing a variety of com.sun.misc.Unsafe stuff
 */
public final class Bits {

    public static final sun.misc.Unsafe unsafe;

    private static sun.misc.Unsafe getUnsafe() {
        try {
            return sun.misc.Unsafe.getUnsafe();
        } catch (SecurityException se) {
            try {
                return java.security.AccessController.doPrivileged
                        (new java.security
                                .PrivilegedExceptionAction<sun.misc.Unsafe>() {
                            @Override
                            public sun.misc.Unsafe run() throws Exception {
                                java.lang.reflect.Field f = sun.misc
                                        .Unsafe.class.getDeclaredField("theUnsafe");
                                f.setAccessible(true);
                                return (sun.misc.Unsafe) f.get(null);
                            }});
            } catch (java.security.PrivilegedActionException e) {
                throw new RuntimeException("Could not initialize intrinsics",
                        e.getCause());
            }
        }
    }

    private static int PAGE_SIZE = -1;

    static {
        sun.misc.Unsafe unsafeTemp = null;
        try {
            unsafeTemp = getUnsafe();
        } catch (Exception e) {
            e.printStackTrace();
        }
        unsafe = unsafeTemp;
    }

    public static int pageSize() {
        if (PAGE_SIZE == -1) {
            PAGE_SIZE = unsafe.pageSize();
        }
        return PAGE_SIZE;
    }

    public static int numPages(int size) {
        return (size + pageSize()  - 1) / pageSize();
    }

    //Target for storing the checksum to prevent dead code elimination
    private static byte unused;

    public static void readEveryPage(BBContainer cont) {
        long address = cont.address();
        //Make address page aligned
        final int offset = (int)(address % Bits.pageSize());
        address -= offset;
        final int numPages = Bits.numPages(cont.b().remaining() + offset);
        byte checksum = 0;
        for (int ii = 0; ii < numPages; ii++) {
            checksum ^= Bits.unsafe.getByte(address);
            address += PAGE_SIZE;
        }
        //This store will never actually occur, but the compiler doesn't care
        //for the purposes of dead code elimination
        if (unused != 0) {
            unused = checksum;
        }
    }

    public static long sync_file_range(VoltLogger logger, FileDescriptor fd, FileChannel fc, long syncStart, long positionAtSync) throws IOException {
        //Don't start writeback on the currently appending page to avoid
        //issues with stables pages, hence we move the end back one page
        long syncedBytes = ((positionAtSync / Bits.pageSize()) - 1) * Bits.pageSize();
        if (PosixAdvise.SYNC_FILE_RANGE_SUPPORTED) {
            final long retval = PosixAdvise.sync_file_range(fd,
                                                            syncStart,
                                                            syncedBytes - syncStart,
                                                            PosixAdvise.SYNC_FILE_RANGE_SYNC);
            if (retval != 0) {
                logger.error("Error sync_file_range snapshot data: " + retval);
                logger.error(
                        "Params offset " + syncedBytes +
                        " length " + (syncedBytes - syncStart) +
                        " flags " + PosixAdvise.SYNC_FILE_RANGE_SYNC);
                fc.force(false);
            }
        } else {
            fc.force(false);
        }
        return syncedBytes;
    }

    public static Callable<Boolean> rolling_sync_file_range_with_task(final VoltLogger logger, final FileDescriptor fd, final FileChannel fc, final Callable<Boolean> prevWrite, final long syncStart, final long positionAtSync) throws IOException {
        assert syncStart % Bits.pageSize() == 0;
        final long syncLength = positionAtSync - syncStart;
        if (PosixAdvise.SYNC_FILE_RANGE_SUPPORTED) {
            logger.info("Starting Sync from range " + syncStart + " to " + positionAtSync + " (" + syncLength + " bytes)");
            final long retval = PosixAdvise.sync_file_range(fd,
                                                            syncStart,
                                                            syncLength,
                                                            PosixAdvise.SYNC_FILE_RANGE_WRITE);
            if (retval != 0) {
                logger.error("Error in rolling_sync_file_range_with_wait: " + retval);
                logger.error(
                        "Params offset " + syncStart +
                        " length " + syncLength +
                        " flags " + PosixAdvise.SYNC_FILE_RANGE_WRITE);
                fc.force(false);
            }
            else {
                return new Callable<Boolean>() {
                    @Override
                    public Boolean call() {
                        // should not bother calling for the end of the file but just use close
                        try {
                            if (prevWrite.call()) {
                                assert syncLength % Bits.pageSize() == 0;
                                logger.info("Waiting to complete Sync from range " + syncStart + " to " + positionAtSync + " (" + syncLength + " bytes)");
                                final long retval2 = PosixAdvise.sync_file_range(fd,
                                        syncStart,
                                        syncLength,
                                        PosixAdvise.SYNC_FILE_RANGE_SYNC);
                                if (retval2 != 0) {
                                    logger.error("Error in rolling_sync_file_range_first(2): " + retval2);
                                    logger.error(
                                            "Params offset " + syncStart +
                                            " length " + syncLength +
                                            " flags " + PosixAdvise.SYNC_FILE_RANGE_SYNC);
                                }
                                else {
                                    // Note: POSIX_FADV_DONTNEED of a partial page is ignored by the OS
                                    PosixAdvise.fadvise(fd,
                                            0,
                                            positionAtSync,
                                            PosixAdvise.POSIX_FADV_DONTNEED);
                                }
                                return (retval2 == 0);
                            }
                            else
                                return false;
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                            return false;
                        }
                    }
                };
            }
        } else {
            fc.force(false);
        }
        // Fall through if sync_file_range failed
        PosixAdvise.fadvise(fd,
                0,
                positionAtSync,
                PosixAdvise.POSIX_FADV_DONTNEED);
        return new Callable<Boolean>() {
            @Override
            public Boolean call() {
                // Since the force has been done in this path and the
                return false;
            }
        };
    }
}
