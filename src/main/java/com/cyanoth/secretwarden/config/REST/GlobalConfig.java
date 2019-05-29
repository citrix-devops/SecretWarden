package com.cyanoth.secretwarden.config.REST;

import com.atlassian.bitbucket.AuthorisationException;
import com.atlassian.bitbucket.permission.Permission;
import com.atlassian.bitbucket.permission.PermissionValidationService;
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
    public Response updateMatchSecretRule(@PathParam("identifier") String identifier) {

        // TODO: updateMatchSecretRule


        return Response.ok("Updated ").build();
    }

    @POST
    @Path("/match-secret-rule/{identifier}")
    public Response createMatchSecretRule(@PathParam("identifier") String identifier) {

        // TODO: createMatchSecretRule

        return Response.ok("Created").build();
    }


    @DELETE
    @Path("/match-secret-rule/{identifier}")
    public Response deleteMatchSecretRule(@PathParam("identifier") String identifier) {
        return Response.status(400).build();
    }

    @PUT
    @Path("/clear-result-cache")
    public Response clearResultCache() {

        try {
            this.permissionValidationService.validateForGlobal(Permission.ADMIN);
            pullRequestSecretScanResultCache.clear();
            return Response.ok("The secret result cache has been cleared!").build();
        } catch (AuthorisationException e) {
            return responseForNonAdmin();
        }
    }

    public void checkAdminPermission() throws AuthorisationException {
        this.permissionValidationService.validateForGlobal(Permission.ADMIN);
    }

    public Response responseForNonAdmin() {
        return Response.status(Response.Status.FORBIDDEN).build();
    }

}
