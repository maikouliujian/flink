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
import org.apache.flink.table.data.RowData;

import java.io.Serializable;

/**
 * A {@link SliceAssigner} assigns element into a single slice. Note that we use the slice end
 * timestamp to identify a slice.
 *
 * <p>Note: {@link SliceAssigner} servers as a base interface. Concrete assigners should implement
 * interface {@link SliceSharedAssigner} or {@link SliceUnsharedAssigner}.
 *
 * @see SlicingWindowOperator for more definition of slice.
 */
@Internal
public interface SliceAssigner extends Serializable {

    /**
     * Returns the end timestamp of a slice that the given element should belong.
     *
     * @param element the element to which slice should belong to.
     * @param clock the service to get current processing time.
     */
    //todo 获取元素所属的slice，返回这个slice的结束时间戳
    long assignSliceEnd(RowData element, ClockService clock);

    /**
     * Returns the last window which the slice belongs to. The window and and slices are both
     * identified by the end timestamp.
     */
    //todo 获取slice所属的window，返回这个window的结束时间戳
    // 如果slice属于多个window（共享slice），则返回这个slice所属的最后一个window
    long getLastWindowEnd(long sliceEnd);

    /** Returns the corresponding window start timestamp of the given window end timestamp. */
    //todo 给出window的结束时间，返回这个window的开始时间
    long getWindowStart(long windowEnd);

    /**
     * Returns an iterator of slices to expire when the given window is emitted. The window and
     * slices are both identified by the end timestamp.
     *
     * @param windowEnd the end timestamp of window emitted.
     */
    //todo // 当window数据发送到下游之后，返回需要过期处理的slice的iterator（有些共享的slice再也用不到了）
    //    // window和slice都用结束时间戳来表示
    Iterable<Long> expiredSlices(long windowEnd);

    /**
     * Returns the interval of slice ends, i.e. the step size to advance of the slice end when a new
     * slice assigned.
     */
    //todo // 返回slice之间的时间间隔。比如说分配下一个slice的时候，新的slice时间需要前进多少
    long getSliceEndInterval();

    /**
     * Returns {@code true} if elements are assigned to windows based on event time, {@code false}
     * based on processing time.
     */
    //todo // 返回时候使用 event time。如果返回false说明使用的是processing time
    boolean isEventTime();
}
