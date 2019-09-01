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

package org.apache.dubbo.remoting.transport;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.extension.ExtensionLoader;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.common.serialize.ObjectInput;
import org.apache.dubbo.common.serialize.Serialization;
import org.apache.dubbo.remoting.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.apache.dubbo.common.serialize.Constants.COMPACTED_JAVA_SERIALIZATION_ID;
import static org.apache.dubbo.common.serialize.Constants.JAVA_SERIALIZATION_ID;
import static org.apache.dubbo.common.serialize.Constants.NATIVE_JAVA_SERIALIZATION_ID;

public class CodecSupport {

    private static final Logger logger = LoggerFactory.getLogger(CodecSupport.class);
    /**
     * 序列化对象集合
     * key：序列化类型编号 {@link Serialization#getContentTypeId()}
     */
    private static Map<Byte, Serialization> ID_SERIALIZATION_MAP = new HashMap<Byte, Serialization>();
    /**
     * 序列化名集合
     * key：序列化类型编号 {@link Serialization#getContentTypeId()}
     * value: 序列化拓展名
     */
    private static Map<Byte, String> ID_SERIALIZATIONNAME_MAP = new HashMap<Byte, String>();

    static {
        //通过拓展加载器得到序列化支持的拓展集合
        Set<String> supportedExtensions = ExtensionLoader.getExtensionLoader(Serialization.class).getSupportedExtensions();
        for (String name : supportedExtensions) {
            //得到序列化对象
            Serialization serialization = ExtensionLoader.getExtensionLoader(Serialization.class).getExtension(name);
            //得到内容类型id
            byte idByte = serialization.getContentTypeId();
            //如果包含警告并且跳过
            if (ID_SERIALIZATION_MAP.containsKey(idByte)) {
                logger.error("Serialization extension " + serialization.getClass().getName()
                        + " has duplicate id to Serialization extension "
                        + ID_SERIALIZATION_MAP.get(idByte).getClass().getName()
                        + ", ignore this Serialization extension");
                continue;
            }
            ID_SERIALIZATION_MAP.put(idByte, serialization);
            ID_SERIALIZATIONNAME_MAP.put(idByte, name);
        }
    }

    private CodecSupport() {
    }

    public static Serialization getSerializationById(Byte id) {
        return ID_SERIALIZATION_MAP.get(id);
    }

    public static Serialization getSerialization(URL url) {
        //得到序列化对象
        return ExtensionLoader.getExtensionLoader(Serialization.class).getExtension(
                url.getParameter(Constants.SERIALIZATION_KEY, Constants.DEFAULT_REMOTING_SERIALIZATION));
    }

    public static Serialization getSerialization(URL url, Byte id) throws IOException {
        Serialization serialization = getSerializationById(id);
        String serializationName = url.getParameter(Constants.SERIALIZATION_KEY, Constants.DEFAULT_REMOTING_SERIALIZATION);
        // Check if "serialization id" passed from network matches the id on this side(only take effect for JDK serialization), for security purpose.
        if (serialization == null
                || ((id == JAVA_SERIALIZATION_ID || id == NATIVE_JAVA_SERIALIZATION_ID || id == COMPACTED_JAVA_SERIALIZATION_ID)
                && !(serializationName.equals(ID_SERIALIZATIONNAME_MAP.get(id))))) {
            throw new IOException("Unexpected serialization id:" + id + " received from network, please check if the peer send the right id.");
        }
        return serialization;
    }

    public static ObjectInput deserialize(URL url, InputStream is, byte proto) throws IOException {
        Serialization s = getSerialization(url, proto);
        return s.deserialize(url, is);
    }
}
