package com.siemens.cto.aem.service.jvm.jms.listener.message;

import javax.jms.JMSException;
import javax.jms.MapMessage;

import com.siemens.cto.aem.domain.model.jvm.message.JvmStateMessage;
import com.siemens.cto.infrastructure.report.runnable.jms.impl.ReportingJmsMessageKey;

public class JvmStateMapMessageConverterImpl implements JvmStateMapMessageConverter {

    @Override
    public JvmStateMessage convert(final MapMessage aMapMessage) throws JMSException {
        return new JvmStateMessage(get(aMapMessage, ReportingJmsMessageKey.ID),
                                   get(aMapMessage, ReportingJmsMessageKey.INSTANCE_ID),
                                   get(aMapMessage, ReportingJmsMessageKey.TYPE),
                                   get(aMapMessage, ReportingJmsMessageKey.STATE),
                                   get(aMapMessage, ReportingJmsMessageKey.AS_OF));
    }

    protected String get(final MapMessage aMapMessage,
                         final ReportingJmsMessageKey aKey) throws JMSException {
        return aMapMessage.getString(aKey.getKey());
    }
}
