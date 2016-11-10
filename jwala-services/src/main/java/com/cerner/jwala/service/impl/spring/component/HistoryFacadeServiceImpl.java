package com.cerner.jwala.service.impl.spring.component;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.persistence.jpa.domain.JpaHistory;
import com.cerner.jwala.persistence.jpa.type.EventType;
import com.cerner.jwala.service.HistoryFacadeService;
import com.cerner.jwala.service.HistoryService;
import com.cerner.jwala.service.Message;
import com.cerner.jwala.service.MessagingService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Implements {@link HistoryFacadeService}
 *
 * Created by JC043760 on 11/9/2016
 */
@Service
public class HistoryFacadeServiceImpl implements HistoryFacadeService {

    public static final String SUBJECT_HISTORY = "HISTORY";
    private final HistoryService historyService;
    private final MessagingService messagingService;

    public HistoryFacadeServiceImpl(final HistoryService historyService, final MessagingService messagingService) {
        this.historyService = historyService;
        this.messagingService = messagingService;
    }

    @Override
    public void write(final String serverName, final Collection<Group> groups, final String event, final EventType eventType,
                      final String user) {
        final List<JpaHistory> jpaHistoryList = historyService.createHistory(serverName, new ArrayList<>(groups), event, eventType, user);
        for (JpaHistory jpaHistory : jpaHistoryList) {
            messagingService.send(new Message<>(jpaHistory.getGroup().getName(), SUBJECT_HISTORY, jpaHistory));
        }
    }

    @Override
    public void write(final String serverName, final Group group, final String event, final EventType eventType,
                      final String user) {
        final List<Group> groupList = Arrays.asList(new Group[]{group});
        write(serverName, groupList, event, eventType, user);
    }

}
