package com.siemens.cto.aem.service.webserver.impl;

import com.siemens.cto.toc.files.TocFile;

public enum ConfigurationTemplate implements TocFile {    
    
    SERVER_XML_TEMPLATE("server-xml.tpl"),
    HTTPD_CONF_TEMPLATE("httpd-conf.tpl"),
    HTTPD_SSL_CONF_TEMPLATE("httpd-ssl-conf.tpl"),
    WORKERS_PROPS_TEMPLATE("workers-properties.tpl");

    private String fileName;

    ConfigurationTemplate(String aFileName) {
        fileName = aFileName;
    }
    
    public String getFileName() { 
        return fileName;
    }
}