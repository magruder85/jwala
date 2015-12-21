package com.siemens.cto.aem.service.app.impl;

import com.siemens.cto.aem.commandprocessor.impl.jsch.JschBuilder;
import com.siemens.cto.aem.common.configuration.TestExecutionProfile;
import com.siemens.cto.aem.common.domain.model.app.Application;
import com.siemens.cto.aem.common.domain.model.app.ApplicationControlOperation;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.ssh.SshConfiguration;
import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.control.command.RemoteCommandExecutor;
import com.siemens.cto.aem.control.configuration.AemSshConfig;
import com.siemens.cto.aem.persistence.dao.ApplicationDao;
import com.siemens.cto.aem.persistence.dao.impl.JpaApplicationDaoImpl;
import com.siemens.cto.aem.persistence.jpa.service.GroupJvmRelationshipService;
import com.siemens.cto.aem.persistence.jpa.service.WebServerCrudService;
import com.siemens.cto.aem.persistence.jpa.service.impl.*;
import com.siemens.cto.aem.persistence.service.ApplicationPersistenceService;
import com.siemens.cto.aem.persistence.service.JvmPersistenceService;
import com.siemens.cto.aem.persistence.service.impl.JpaApplicationPersistenceServiceImpl;
import com.siemens.cto.aem.persistence.service.impl.JpaJvmPersistenceServiceImpl;
import com.siemens.cto.aem.service.app.ApplicationCommandService;
import com.siemens.cto.aem.service.app.ApplicationService;
import com.siemens.cto.aem.service.configuration.TestJpaConfiguration;
import com.siemens.cto.aem.service.group.GroupService;
import com.siemens.cto.aem.service.ssl.hc.HttpClientRequestFactory;
import com.siemens.cto.aem.service.webserver.component.ClientFactoryHelper;
import com.siemens.cto.toc.files.FileManager;
import com.siemens.cto.toc.files.FilesConfiguration;
import com.siemens.cto.toc.files.RepositoryService;
import com.siemens.cto.toc.files.TocPath;
import com.siemens.cto.toc.files.impl.FileManagerImpl;
import com.siemens.cto.toc.files.impl.LocalFileSystemRepositoryServiceImpl;
import com.siemens.cto.toc.files.resources.ResourceTypeDeserializer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {
        ApplicationServiceImplIntegrationTest.CommonConfiguration.class,
        TestJpaConfiguration.class})
@IfProfileValue(name = TestExecutionProfile.RUN_TEST_TYPES, value = TestExecutionProfile.INTEGRATION)
@RunWith(SpringJUnit4ClassRunner.class)
@EnableTransactionManagement
@Transactional
public class ApplicationServiceImplIntegrationTest {

    @Mock
    private AemSshConfig aemSshConfig;

    @Mock
    private GroupService groupService;

    @Configuration
    static class CommonConfiguration {

        @Bean
        public WebServerCrudService getWebServerDao() {
            return new WebServerCrudServiceImpl();
        }

        @Bean
        public ApplicationPersistenceService getApplicationPersistenceService() {
            return new JpaApplicationPersistenceServiceImpl(new ApplicationCrudServiceImpl(), new GroupCrudServiceImpl());
        }

        @Bean
        public ApplicationDao getApplicationDao() {
            return new JpaApplicationDaoImpl();
        }

        @Bean
        @Autowired
        public JvmPersistenceService getJvmPersistenceService(final GroupJvmRelationshipService groupJvmRelationshipService) {
            return new JpaJvmPersistenceServiceImpl(new JvmCrudServiceImpl(), groupJvmRelationshipService);
        }

        @Bean
        public GroupJvmRelationshipService getGroupJvmRelationshipService() {
            return new GroupJvmRelationshipServiceImpl(new GroupCrudServiceImpl(), new JvmCrudServiceImpl());
        }

        @Bean
        public ApplicationCommandService getApplicationCommandService() {
            final SshConfiguration sshConfiguration = new SshConfiguration("z003bpej", 22, "", "", "MrI6SA43vbcIws0pJygEDA==");
            JschBuilder jschBuilder = new JschBuilder();
            return new ApplicationCommandServiceImpl(sshConfiguration, jschBuilder);
        }

        @Bean(name = "webServerHttpRequestFactory")
        public HttpClientRequestFactory getHttpClientRequestFactory() throws Exception {
            return new HttpClientRequestFactory();
        }

        @Bean
        public ClientFactoryHelper getClientFactoryHelper() {
            return new ClientFactoryHelper();
        }

        @Bean
        public FilesConfiguration getFilesConfiguration() {
            return new FilesConfiguration() {
                @Override
                public Path getConfiguredPath(TocPath webArchive) {
                    // Implement this when the need arises...
                    return null;
                }

                @Override
                public void reload() {
                    // Implement this when the need arises...
                }
            };
        }

        @Bean
        public RepositoryService getFileSystemStorage() {
            return new LocalFileSystemRepositoryServiceImpl();
        }

        @Bean
        public ResourceTypeDeserializer getResourceTypeDeserializer() {
            // Implement this when the need arises...
            return null;
        }

        @Bean
        public FileManager getFileManager() {
            return new FileManagerImpl();
        }

    }

    @Autowired
    private ApplicationDao applicationDao;

    @Autowired
    private ApplicationPersistenceService applicationPersistenceService;

    private ApplicationService cut;

    private JvmPersistenceService jvmPersistenceService;

    @Autowired
    private ClientFactoryHelper clientFactoryHelper;

    private RemoteCommandExecutor<ApplicationControlOperation> remoteCommandExecutor;

    @Autowired
    private FileManager fileManager;

    @Before
    public void setup() {
        SshConfiguration mockSshConfig = mock(SshConfiguration.class);
        aemSshConfig = mock(AemSshConfig.class);
        groupService = mock(GroupService.class);
        when(mockSshConfig.getUserName()).thenReturn("mockUser");
        when(aemSshConfig.getSshConfiguration()).thenReturn(mockSshConfig);
        cut = new ApplicationServiceImpl(applicationDao, applicationPersistenceService, jvmPersistenceService,
                remoteCommandExecutor, null,
                groupService, fileManager, null, null);
    }

    /**
     * With this revision there is no capacity to create. Therefore integration
     * testing at the service layer can only return NotFound.
     * We'll mock the rest.
     */
    @Test(expected = NotFoundException.class)
    public void testGetApplication() {
        cut.getApplication(new Identifier<Application>(0L));
    }

    /**
     * Test getting the full list.
     */
    @Test
    public void testGetAllApplications() {
        List<Application> all = cut.getApplications();
        assertEquals(0, all.size());
    }

    /**
     * Test getting the partial list.
     */
    @Test
    public void testGetApplicationsByGroup() {
        List<Application> partial = cut.findApplications(new Identifier<Group>(0L));
        assertEquals(0, partial.size());
    }

}
