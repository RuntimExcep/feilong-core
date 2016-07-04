/*
 * Copyright (C) 2008 feilong
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.feilong.core.net;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link URL} 工具类.
 * 
 * <h3>URL 的长度上限</h3>
 * 
 * <blockquote>
 * 
 * <p>
 * URL 的最大长度是多少？W3C 的 HTTP 协议 并没有限定; 然而经过试验,不同浏览器和 Web 服务器有不同的约定:
 * </p>
 * 
 * <ul>
 * <li><b>IE</b> 长度上限是 <b>2083</b> 字节,其中纯路径部分不能超过 <b>2048</b> 字节.</li>
 * <li><b>Firefox</b> 地址栏中超过 <b>65536</b> 字符后就不再显示.</li>
 * <li><b>Safari</b> 测试到 <b>80000</b> 字符还工作得好好的.</li>
 * <li><b>Opera</b> 测试到 <b>190000</b> 字符还工作得好好的.</li>
 * </ul>
 * 
 * <p>
 * Web 服务器:
 * </p>
 * 
 * <ul>
 * <li><b>Apache Web</b> 服务器在接收到大约 <b>4000</b> 字符长的 URL 时候产生 <b>"413 Entity Too Large"</b> 错误.</li>
 * <li><b>IIS</b> 默认接收的最大 URL 是 <b>16384</b> 字符.</li>
 * </ul>
 * </blockquote>
 *
 * @author <a href="http://feitianbenyue.iteye.com/">feilong</a>
 * @since 1.2.1
 */
public final class URLUtil{

    /** The Constant log. */
    private static final Logger LOGGER = LoggerFactory.getLogger(URLUtil.class);

    /** Don't let anyone instantiate this class. */
    private URLUtil(){
        //AssertionError不是必须的. 但它可以避免不小心在类的内部调用构造器. 保证该类在任何情况下都不会被实例化.
        //see 《Effective Java》 2nd
        throw new AssertionError("No " + getClass().getName() + " instances for you!");
    }

    /**
     * 将字符串转成url.
     * 
     * <h3>注意:</h3>
     * 
     * <blockquote>
     * 
     * <p>
     * 如果直接调用 {@link URL#URL(String)} 方法,如果是文件路径, 比如
     * </p>
     * 
     * <pre class="code">
     * 
     * String spec = "C:/Users/feilong/feilong/train/新员工/warmReminder/20160704141057.html";
     * 
     * URL newURL = new URL(spec);
     * </pre>
     * 
     * 此时会抛出异常,
     * 
     * <pre class="code">
     * Caused by: java.net.MalformedURLException: unknown protocol: c
     * at java.net.URL.<init>(URL.java:593)
     * at java.net.URL.<init>(URL.java:483)
     * at java.net.URL.<init>(URL.java:432)
     * at com.feilong.core.net.URLUtil.newURL(URLUtil.java:95)
     * ... 24 more
     * </pre>
     * 
     * 此时 ,你需要修改成
     * 
     * <pre class="code">
     * 
     * String spec = "file://C:/Users/feilong/feilong/train/新员工/warmReminder/20160704141057.html";
     * 
     * URL newURL = new URL(spec);
     * </pre>
     * 
     * 也就是说,此方法必须要有协议 支持 (file URI scheme),如果你是文件需要转成 url的话,建议直接调用 {@link #toFileURL(String)}
     * </blockquote>
     * 
     * 
     * <h3>而本方法有以下特点:</h3>
     * <blockquote>
     * <ol>
     * <li>处理 check exception,转成 uncheck exception</li>
     * <li>如果使用new URL 出现异常,会尝试使用 {@link #toFileURL(String)}</li>
     * </ol>
     * </blockquote>
     * 
     * @param spec
     *            the <code>String</code> to parse as a URL.
     * @return 如果 <code>spec</code> 是null,抛出 {@link NullPointerException}<br>
     *         如果 <code>spec</code> 是blank,抛出 {@link IllegalArgumentException}<br>
     * @see java.net.URL#URL(String)
     * @see "org.apache.cxf.common.util.StringUtils#getURL(String)"
     * @see "org.springframework.util.ResourceUtils#getURL(String)"
     * @see <a href="https://en.wikipedia.org/wiki/File_URI_scheme">File_URI_scheme</a>
     * @see "org.apache.xml.resolver.readers.TextCatalogReader#readCatalog(Catalog, String)"
     * @see <a href="https://docs.oracle.com/javase/tutorial/networking/urls/creatingUrls.html">Creating a URL</a>
     * @since 1.3.0
     */
    public static URL newURL(String spec){
        Validate.notBlank(spec, "spec can't be blank!");
        try{
            return new URL(spec);
        }catch (MalformedURLException e){
            // no URL -> treat as file path
            LOGGER.info("[new URL(\"{}\")] exception,cause by :[{}],will try call [toFileURL(\"{}\")]", spec, e.getMessage(), spec);
            return toFileURL(spec);
        }
    }

    /**
     * 将字符串路径 <code>filePathName</code> 转成{@link URL}.
     * 
     * <h3>示例:</h3>
     * 
     * <blockquote>
     * 
     * <pre class="code">
     * 
     * String spec = "C:\\Users\\feilong\\feilong\\train\\新员工\\warmReminder\\20160704141057.html";
     * 
     * URL newURL = URLUtil.toFileURL(spec);
     * </pre>
     * 
     * </blockquote>
     * 
     * <h3>Using Java 7 你还可以使用:</h3>
     * 
     * <blockquote>
     * Paths#get(string).toUri().toURL();
     * 
     * <p>
     * However, you probably want to get a URI. <br>
     * Eg, a URI begins with file:/// but a URL with file:/ (at least, that's what toString produces).
     * </p>
     * 
     * 参见
     * <a href="http://stackoverflow.com/questions/6098472/pass-a-local-file-in-to-url-in-java#21461738">pass-a-local-file-in-to-url-in-java
     * </a>
     * </blockquote>
     *
     * @param filePath
     *            字符串路径
     * @return 如果 <code>filePathName</code> 是null,抛出 {@link NullPointerException}<br>
     *         如果 <code>filePathName</code> 是blank,抛出 {@link IllegalArgumentException}<br>
     * @see java.io.File#toURI()
     * @see java.net.URI#toURL()
     * @see "org.apache.commons.io.FileUtils#toURLs(File[])"
     * @since 1.6.3
     */
    public static URL toFileURL(String filePath){
        Validate.notBlank(filePath, "filePath can't be null/empty!");
        try{
            return new File(filePath).toURI().toURL();// file.toURL() 已经过时,它不会自动转义 URL 中的非法字符
        }catch (MalformedURLException e){
            throw new URIParseException("filePathName:" + filePath, e);
        }
    }

    //******************************************************************************************************

    /**
     * 将 {@link URL}转成 {@link URI}.
     *
     * @param url
     *            the url
     * @return 如果 <code>url</code> 是null,抛出 {@link NullPointerException}<br>
     * @see "org.springframework.util.ResourceUtils#toURI(URL)"
     * @see java.net.URL#toURI()
     * @since 1.2.2
     */
    public static URI toURI(URL url){
        Validate.notNull(url, "url can't be null!");
        try{
            return url.toURI();
        }catch (URISyntaxException e){
            throw new URIParseException(e);
        }
    }
    //******************************************************************************************************

    /**
     * 获取联合url,通过在指定的上下文中对给定的 spec 进行解析创建 URL,新的 URL 从给定的上下文 URL 和 spec 参数创建.
     * 
     * <p style="color:red">
     * 网站地址拼接,请使用这个method
     * </p>
     * 
     * <h3>示例:</h3>
     * 
     * <blockquote>
     * 
     * <pre class="code">
     * 
     * URL url = new URL("http://www.exiaoshuo.com/jinyiyexing/");
     * URIUtil.getUnionUrl(url, "/jinyiyexing/1173348/")    =  http://www.exiaoshuo.com/jinyiyexing/1173348/
     * </pre>
     * 
     * </blockquote>
     * 
     * @param context
     *            要解析规范的上下文
     * @param spec
     *            the <code>String</code> to parse as a URL.
     * @return 如果 <code>spec</code> 是null,抛出 {@link NullPointerException}<br>
     *         如果 <code>spec</code> 是blank,抛出 {@link IllegalArgumentException}<br>
     * @since 1.4.0
     */
    public static String getUnionUrl(URL context,String spec){
        Validate.notBlank(spec, "spec can't be null!");
        try{
            return new URL(context, spec).toString();
        }catch (MalformedURLException e){
            throw new URIParseException(e);
        }
    }
}
