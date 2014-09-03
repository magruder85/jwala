package com.siemens.cto.aem.service.state.jms.sender.message;

import javax.jms.JMSException;
import javax.jms.MapMessage;

import org.joda.time.format.ISODateTimeFormat;
import org.mockito.Mock;

import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.state.ExternalizableState;
import com.siemens.cto.aem.domain.model.state.message.CommonStateKey;
import com.siemens.cto.aem.domain.model.state.message.StateKey;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

public class AbstractCurrentStateMessageExtractorTest {

    @Mock
    protected MapMessage message;

    protected <S, T extends ExternalizableState> void setupMockMapMessage(final CurrentState<S, T> aState) throws JMSException {
        mockMapString(CommonStateKey.AS_OF, ISODateTimeFormat.dateTime().print(aState.getAsOf()));
        mockMapString(CommonStateKey.ID, aState.getId().getId().toString());
        mockMapString(CommonStateKey.STATE, aState.getState().toStateString());
        mockMapString(CommonStateKey.TYPE, aState.getType().name());
        mockMapString(CommonStateKey.MESSAGE, aState.getMessage());
    }

    private void mockMapString(final StateKey aKey,
                               final String aValue) throws JMSException {
        when(message.getString(eq(aKey.getKey()))).thenReturn(aValue);
    }
}
