/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.table.runtime.operators.window.slicing;

import org.apache.flink.annotation.Internal;
import org.apache.flink.table.runtime.operators.aggregate.window.processors.SliceSharedWindowAggProcessor;

import javax.annotation.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * A {@link SliceAssigner} which shares slices for windows, which means a window is divided into
 * multiple slices and need to merge the slices into windows when emitting windows.
 *
 * <p>Classical window of {@link SliceSharedAssigner} is hopping window.
 */
@Internal
public interface SliceSharedAssigner extends SliceAssigner {

    /**
     * Determines which slices (if any) should be merged.
     *
     * @param sliceEnd the triggered slice, identified by end timestamp
     * @param callback a callback that can be invoked to signal which slices should be merged.
     */
    //todo // 合并slice。sliceEnd为触发合并操作的slice，callback为合并slice的操作
    void mergeSlices(long sliceEnd, MergeCallback callback) throws Exception;

    /**
     * Returns the optional end timestamp of next window which should be triggered. Empty if no
     * following window to trigger for now.
     *
     * <p>The purpose of this method is avoid register too many timers for each hopping and
     * cumulative slice, e.g. HOP(1day, 10s) needs register 8640 timers for every slice. In order to
     * improve this, we only register one timer for the next window. For hopping windows we don't
     * register next window if current window is empty (i.e. no records in current window). That
     * means we will have one more unnecessary window triggered for hopping windows if no elements
     * arrives for a key for a long time. We will skip to emit window result for the triggered empty
     * window, see {@link SliceSharedWindowAggProcessor#fireWindow(Long)}.
     *
     * @param windowEnd the current triggered window, identified by end timestamp
     * @param isWindowEmpty a supplier that can be invoked to get whether the triggered window is
     *     empty (i.e. no records in the window).
     */
    //todo // 返回下一个需要触发计算的window
    //    // windowEnd为当前触发计算的window的结束时间戳
    //    // isWindowEmpty为触发计算的window内是否有数据，是一个supplier类型，在需要计算的时候再返回结果
    Optional<Long> nextTriggerWindow(long windowEnd, Supplier<Boolean> isWindowEmpty);

    // ------------------------------------------------------------------------

    /**
     * Callback to be used in {@link #mergeSlices(long, MergeCallback)} for specifying which slices
     * should be merged.
     */
    //todo // 合并slice的回调函数，确定哪些slice需要合并，执行合并操作
    interface MergeCallback {

        /**
         * Specifies that states of the given slices should be merged into the result slice.
         *
         * @param mergeResult The resulting merged slice, {@code null} if it represents a non-state
         *     namespace.
         * @param toBeMerged The list of slices that should be merged into one slice.
         */
        //todo         // mergeResult 合并后的slice（结束时间戳表示）
        //        // toBeMerged 需要合并的slice（同样是结束时间戳表示）
        void merge(@Nullable Long mergeResult, Iterable<Long> toBeMerged) throws Exception;
    }
}
