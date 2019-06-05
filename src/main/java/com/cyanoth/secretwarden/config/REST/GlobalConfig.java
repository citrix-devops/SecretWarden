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

    /**
     * Retrieve information about a single match secret rule identified by a number
     * @param ruleNumber The unique number of the rule to get information about
     * @return JSON representation of a single MatchSecretRule. 404 if not found.
     */
    @GET
    @Path("/match-secret-rule/{rule_number}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response getMatchSecretRule(@PathParam("rule_number") int ruleNumber) {
        MatchRule rule = matchRuleSetCache.getRuleSet().getRule(ruleNumber);

        if (rule == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } else {
            return Response.ok(new Gson().toJson(rule)).build();
        }
    }

    /**
     * @return JSON representation of all MatchSecretRule currently loaded in the cache
     */
    @GET
    @Path("/match-secret-rule")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response getMatchSecretRules() {
        Collection<MatchRule> rules = matchRuleSetCache.getRuleSet().getAllRules();

        if (rules == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } else {
            return Response.ok(new Gson().toJson(rules)).build();
        }
    }

     /**
     * Creates or updates an existing match secret rule
     * @param incomingRule Required, matching JSON data representation of a new MatchSecretRule.
     * @return Response 200 if the rule has been updated & reloaded successfully. HTTP error otherwise.
     */
    @POST
    @Path("/match-secret-rule")
    @Consumes({ MediaType.APPLICATION_JSON })
    public Response updateMatchRule(MatchRule incomingRule) {
        try {
            this.permissionValidationService.validateForGlobal(Permission.ADMIN);

            try {
                if (incomingRule.getRuleNumber() == 0) { // Creating a new rule
                    log.debug("Creating new rule");
                    boolean createSuccess = matchRuleSetCache.createNewRule(incomingRule.getFriendlyName(),
                            incomingRule.getCompiledRegexPattern().toString(),true);

                    if (createSuccess)
                        return Response.ok("{ \"id\": \"1\" }").build(); //AJS Restful Table id of the new row (which we don't have)
                    else
                        throw new Exception("New rule was not created!");
                }
                else {  // Updating an existing rule
                    int ruleNumber = incomingRule.getRuleNumber();
                    String newFriendlyName = incomingRule.getFriendlyName();
                    String newRegexPattern = incomingRule.getCompiledRegexPattern().toString();
                    Boolean newIsEnabled = incomingRule.getIsEnabled();

                    log.info(String.format("Updating a MatchSecret Rule: Number: %d Name: %s Pattern: %s Enabled: %s",
                        ruleNumber, newFriendlyName, newRegexPattern, newIsEnabled.toString()));

                    boolean ruleEnableSuccess = matchRuleSettings.setRuleEnabled(ruleNumber, newIsEnabled);
                    boolean ruleNameSuccess = matchRuleSettings.setRuleName(ruleNumber, newFriendlyName);
                    boolean rulePatternSuccess = matchRuleSettings.setRulePattern(ruleNumber, newRegexPattern);
                    reloadRuleSet();

                    if (ruleEnableSuccess && ruleNameSuccess && rulePatternSuccess)
                        return Response.ok("{}").build(); //AJS Restful Table response >requires< JSON response as the OK
                    else
                        throw new Exception ("Rule was not updated!");
                }

            }
            catch (Exception e) {
                log.error("Failed to create/update a MatchRule. An error occurred:", e);
                return Response.status(Response.Status.BAD_REQUEST).entity(new RestErrorMessage("Failed to update MatchSecretRule." +
                        "An error occurred, see server-logs for more information")).build();
            }

        } catch (AuthorisationException e) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
    }

    /**
     * @return Response 200 if the secret scan result cache has been cleared. HTTP error otherwise.
     */
    @PUT
    @Path("/clear-result-cache")
    @Produces({ MediaType.TEXT_PLAIN })
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

    /**
     * @return Response 200 if the ruleset has been reloaded. HTTP error otherwise.
     */
    @PUT
    @Path("/reload-ruleset")
    @Produces({ MediaType.TEXT_PLAIN })
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
