package com.siemens.cto.aem.persistence.jpa.domain.resource.config.template;

import com.siemens.cto.aem.persistence.jpa.domain.JpaApplication;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;

import javax.persistence.*;

/**
 * POJO that describes a db table that holds data about a group of application related resource configuration templates.
 */
@Entity
@Table(name = "GRP_APP_CONFIG_TEMPLATE", uniqueConstraints = {@UniqueConstraint(columnNames = {"GRP_ID", "TEMPLATE_NAME"})})
@NamedQueries({
        @NamedQuery(name = JpaGroupAppConfigTemplate.GET_GROUP_APP_TEMPLATE_RESOURCE_NAMES,
                query = "SELECT t.templateName FROM JpaGroupAppConfigTemplate t WHERE t.grp.name = :grpName"),
        @NamedQuery(name = JpaGroupAppConfigTemplate.GET_GROUP_APP_TEMPLATE_CONTENT,
                query = "SELECT t.templateContent FROM JpaGroupAppConfigTemplate t where t.grp.name = :grpName and t.app.name = :appName and t.templateName = :templateName"),
        @NamedQuery(name = JpaGroupAppConfigTemplate.GET_GROUP_APP_TEMPLATE_META_DATA,
                query = "SELECT t.metaData FROM JpaGroupAppConfigTemplate t where t.grp.name = :grpName and t.templateName = :templateName"),
        @NamedQuery(name = JpaGroupAppConfigTemplate.UPDATE_GROUP_APP_TEMPLATE_CONTENT,
                query = "UPDATE JpaGroupAppConfigTemplate t SET t.templateContent = :templateContent WHERE t.grp.name = :grpName AND t.app.name = :appName AND t.templateName = :templateName"),
        @NamedQuery(name = JpaGroupAppConfigTemplate.QUERY_APP_RESOURCE_NAMES, query = "SELECT t.templateName FROM JpaGroupAppConfigTemplate t WHERE t.grp.name = :grpName AND t.app.name = :appName"),
        @NamedQuery(name = JpaGroupAppConfigTemplate.GET_GROUP_APP_TEMPLATE_RESOURCE_NAME,
                query = "SELECT t.templateName FROM JpaGroupAppConfigTemplate t WHERE t.grp.name = :grpName AND t.templateName = :templateName"),
        @NamedQuery(name = JpaGroupAppConfigTemplate.QUERY_DELETE_GROUP_LEVEL_APP_RESOURCE_BY_TEMPLATE_GROUP_NAME, query = "DELETE FROM JpaGroupAppConfigTemplate t WHERE t.templateName = :templateName AND t.grp.name = :grpName")
})
public class JpaGroupAppConfigTemplate extends ConfigTemplate {
    public static final String GET_GROUP_APP_TEMPLATE_RESOURCE_NAMES = "getGroupAppTemplateResourceNames";
    public static final String GET_GROUP_APP_TEMPLATE_CONTENT = "getGroupAppTemplateContent";
    public static final String GET_GROUP_APP_TEMPLATE_META_DATA = "getGroupAppTemplateMetaData";
    public static final String UPDATE_GROUP_APP_TEMPLATE_CONTENT = "updateGroupAppTemplateContent";
    public static final String QUERY_DELETE_GROUP_LEVEL_APP_RESOURCE_BY_TEMPLATE_GROUP_NAME = "deleteGroupLevelAppResourceByTemplateGroupName";

    public static final String QUERY_PARAM_GRP_NAME = "grpName";
    public static final String QUERY_PARAM_APP_NAME = "appName";
    public static final String QUERY_PARAM_TEMPLATE_NAME = "templateName";
    public static final java.lang.String QUERY_APP_RESOURCE_NAMES = "getAppResourceNames";

    public static final String GET_GROUP_APP_TEMPLATE_RESOURCE_NAME = "getGroupAppTemplateResourceName";

    @ManyToOne(fetch = FetchType.EAGER)
    @Column(nullable = true)
    @org.apache.openjpa.persistence.jdbc.ForeignKey(deleteAction = org.apache.openjpa.persistence.jdbc.ForeignKeyAction.CASCADE)
    private JpaApplication app;

    @ManyToOne(fetch = FetchType.EAGER)
    @Column(nullable = true)
    @org.apache.openjpa.persistence.jdbc.ForeignKey(deleteAction = org.apache.openjpa.persistence.jdbc.ForeignKeyAction.CASCADE)
    private JpaGroup grp;

    public JpaApplication getApp() {
        return app;
    }

    public void setApp(JpaApplication app) {
        this.app = app;
    }

    public JpaGroup getJpaGroup() {
        return grp;
    }

    public void setJpaGroup(JpaGroup jpaGroup) {
        this.grp = jpaGroup;
    }
}
