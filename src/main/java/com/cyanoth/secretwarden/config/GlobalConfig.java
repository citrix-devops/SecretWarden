package com.cyanoth.secretwarden.config;

import com.atlassian.bitbucket.user.SecurityService;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Scanned
public class GlobalConfig  {
    private static final String PLUGIN_KEY = "com.cyanoth.secretwarden"; // This need to remain hardcoded incase of changes to the plugin name (keep settings)
    private static final Logger log = LoggerFactory.getLogger(GlobalConfig.class);

    private final PluginSettingsFactory pluginSettingsFactory;
    private final SecurityService securityService;
    private final Permiss



    //EVENT PUBLISHER?
    public GlobalConfig(PluginSettingsFactory pluginSettingsFactory,
                                     SecurityService securityService,

                                       PermissionValidationService validationService) {
        this.eventPublisher = eventPublisher;
        this.pullRequestService = pullRequestService;
        this.securityService = securityService;
        this.transactionTemplate = transactionTemplate;
        this.validationService = validationService;

        pluginSettings = pluginSettingsFactory.createSettingsForKey(PLUGIN_KEY);
    }

}
