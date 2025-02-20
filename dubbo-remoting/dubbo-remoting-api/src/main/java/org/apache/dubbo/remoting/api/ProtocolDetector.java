/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.dubbo.remoting.api;


import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * Determine incoming bytes belong to the specific protocol.
 *
 * @author guohaoice@gmail.com
 */

/**
 * 根据传输的数据来判断使用了哪个协议
 */
public interface ProtocolDetector {

    Result detect(final ChannelHandlerContext ctx, final ByteBuf in);

    enum Result {
        RECOGNIZED, UNRECOGNIZED, NEED_MORE_DATA
    }
}
