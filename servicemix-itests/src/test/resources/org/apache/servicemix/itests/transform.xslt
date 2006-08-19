<?xml version="1.0" encoding="UTF-8"?>
<!--

    Licensed to the Apache Software Foundation (ASF) under one or more
    contributor license agreements.  See the NOTICE file distributed with
    this work for additional information regarding copyright ownership.
    The ASF licenses this file to You under the Apache License, Version 2.0
    (the "License"); you may not use this file except in compliance with
    the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:jbi="xalan://org.servicemix.components.xslt.XalanExtension"
    extension-element-prefixes="jbi"
    xmlns:my="http://servicemix.org/demo/" version="1.0">
    <xsl:template match="/">
        <ser:simpleMethod xmlns:ser="http://server.simpleexample/">
            <arg0>20</arg0>
            <arg1>2</arg1>
        </ser:simpleMethod>
    </xsl:template>
</xsl:stylesheet>
