package com.cyanoth.secretwarden.config.REST;

import com.atlassian.bitbucket.AuthorisationException;
import com.atlassian.bitbucket.permission.Permission;
import com.atlassian.bitbucket.permission.PermissionValidationService;
import com.atlassian.bitbucket.rest.RestErrorMessage;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.cyanoth.secretwarden.config.MatchRuleSetCache;
import com.cyanoth.secretwarden.config.MatchRuleSettings;
import com.cyanoth.secretwarden.pullrequest.PullRequestSecretScanResultCache;
import com.cyanoth.secretwarden.structures.MatchRule;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;

/**
 * Exposed REST endpoints to modify global configuration of the plugin (for administrators)
 *
 * [1] https://developer.atlassian.com/server/framework/atlassian-sdk/rest-plugin-module/
 */
@Path("/globalconfig")
@Scanned
public class GlobalConfig  {
    private static final Logger log = LoggerFactory.getLogger(GlobalConfig.class);
    private final PermissionValidationService permissionValidationService;
    private final MatchRuleSettings matchRuleSettings;
    private final MatchRuleSetCache matchRuleSetCache;
    private final PullRequestSecretScanResultCache pullRequestSecretScanResultCache;

    public GlobalConfig(@ComponentImport  PermissionValidationService permissionValidationService,
                        final MatchRuleSettings matchRuleSettings,
                        final MatchRuleSetCache matchRuleSetCache,
                        final PullRequestSecretScanResultCache pullRequestSecretScanResultCache) {

        this.permissionValidationService = permissionValidationService;
        this.matchRuleSetCache = matchRuleSetCache;
        this.matchRuleSettings = matchRuleSettings;
        this.pullRequestSecretScanResultCache = pullRequestSecretScanResultCache;

    }

    @GET
    @Path("/match-secret-rule/{rule_number}")
    public Response getMatchSecretRule(@PathParam("rule_number") int ruleNumber) {
        MatchRule rule = matchRuleSetCache.getRuleSet().getRule(ruleNumber);

        if (rule == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } else {
            return Response.ok(new Gson().toJson(rule)).build();
        }
    }

    @GET
    @Path("/match-secret-rules")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getMatchSecretRules() {
        Collection<MatchRule> rules = matchRuleSetCache.getRuleSet().getAllRules();

        if (rules == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } else {
            return Response.ok(new Gson().toJson(rules)).build();
        }
    }

    @PUT
    @Path("/match-secret-rule/{rule_number}")
    @Consumes({MediaType.APPLICATION_JSON})
    public Response updateMatchSecretRule(@PathParam("rule_number") int ruleNumber) {

        // TODO: updateMatchSecretRule

        log.debug("Updating rule");


        return Response.ok("Updated ").build();
    }

    @POST
    @Path("/match-secret-rule/{rule_number}")
    public Response createMatchSecretRule(@PathParam("rule_number") int ruleNumber) {

        // TODO: createMatchSecretRule

        return Response.ok("Created").build();
    }

    @PUT
    @Path("/clear-result-cache")
    public Response clearResultCache() {
        try {
            this.permissionValidationService.validateForGlobal(Permission.ADMIN);
            try {
                log.debug("Clearing SecretWarden Result Cache...");
                pullRequestSecretScanResultCache.clear();
                return Response.ok("The secret result cache has been cleared!").build();
            }
            catch (Exception e) {
                log.error("Failed to clear SecretWarden result cache! An error occurred.", e);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new RestErrorMessage("The secret result cache has NOT been cleared. " +
                        "An error occurred, see server-logs for more information")).build();
            }
        } catch (AuthorisationException e) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
    }

    @PUT
    @Path("/reload-ruleset")
    public Response reloadRuleSet() {
        try {
            this.permissionValidationService.validateForGlobal(Permission.ADMIN);
            try {
                log.debug("Reloading MatchSecretRuleSet...");
                matchRuleSetCache.reloadRuleSet();
                return Response.ok("SecretWarden MatchSecretRuleSet reloaded!").build();
            }
            catch (Exception e) {
                log.error("Failed to reload SecretWarden ruleset. An error occurred.", e);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new RestErrorMessage("The SecretWarden MatchRuleSet has NOT been reloaded." +
                        "An error occurred, see server-logs for more information")).build();
            }
        } catch (AuthorisationException e) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
    }
}
