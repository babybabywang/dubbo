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
package org.apache.dubbo.remoting;

import org.apache.dubbo.common.URL;

import java.net.InetSocketAddress;

/**
 * Endpoint. (API/SPI, Prototype, ThreadSafe)
 * 端点 原型模式，线程安全
 * 在 dubbo-remoting-api 中，一个 Client 或 Server ，都是一个 Endpoint 。
 * @see org.apache.dubbo.remoting.Channel
 * @see org.apache.dubbo.remoting.Client
 * @see org.apache.dubbo.remoting.Server
 */
public interface Endpoint {

    /**
     * get url.
     * 获得url
     * @return url
     */
    URL getUrl();

    /**
     * get channel handler.
     * 得到管道处理器
     * @return channel handler
     */
    ChannelHandler getChannelHandler();

    /**
     * get local address.
     * 得到本地地址
     * @return local address.
     */
    InetSocketAddress getLocalAddress();

    /**
     * send message.
     * 发送消息
     * @param message
     * @throws RemotingException
     */
    void send(Object message) throws RemotingException;

    /**
     * send message.
     *
     * @param message
     * @param sent    already sent to socket? 已经发送到套接字?
     */
    void send(Object message, boolean sent) throws RemotingException;

    /**
     * close the channel.
     */
    void close();

    /**
     * Graceful close the channel.
     * 优雅地关闭通道。
     */
    void close(int timeout);

    void startClose();

    /**
     * is closed.
     *
     * @return closed
     */
    boolean isClosed();

}