package org.cricketmsf.services;

import java.util.HashMap;
import java.util.List;
import org.cricketmsf.RequestObject;
import org.cricketmsf.annotation.EventHook;
import org.cricketmsf.api.ResponseCode;
import org.cricketmsf.api.Result;
import org.cricketmsf.api.ResultIface;
import org.cricketmsf.api.StandardResult;
import org.cricketmsf.event.Event;
import org.cricketmsf.event.HttpEvent;
import org.cricketmsf.event.Procedures;
import org.cricketmsf.in.http.ParameterMapResult;
import org.cricketmsf.microsite.SiteAdministrationModule;
import org.cricketmsf.microsite.event.auth.AuthEvent;
import org.cricketmsf.microsite.event.cms.CmsEvent;
import org.cricketmsf.microsite.event.user.UserEvent;
import org.cricketmsf.microsite.out.auth.Token;
import org.cricketmsf.microsite.out.cms.ContentRequestProcessor;
import org.cricketmsf.microsite.out.user.User;
import org.cricketmsf.out.db.KeyValueDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author greg
 */
public class MicrositeEventRouter {

    private static final Logger logger = LoggerFactory.getLogger(MicrositeEventRouter.class);

    private Microsite service;

    public MicrositeEventRouter(Microsite service) {
        this.service = service;
    }

    /**
     * Process requests from simple web server implementation given by
     * HtmlGenAdapter access web web resources
     *
     * @param event
     * @return ParameterMapResult with the file content as a byte array
     */
    @EventHook(className = "org.cricketmsf.event.HttpEvent"/*, procedure = Procedures.WWW*/)
    public ResultIface handleWwwRequest(HttpEvent event) {
        RequestObject request = (RequestObject) event.getData();
        String language = (String) request.parameters.get("language");
        if (language == null || language.isEmpty()) {
            language = "en";
        }
        ResultIface result = null;
        String cacheName = "webcache_" + language;
        try {
            result = (ParameterMapResult) service.contentManager
                    .getFile(request, service.htmlAdapter.useCache() ? service.database : null, cacheName, language, true);
            //((HashMap) result.getData()).put("serviceurl", getProperties().get("serviceurl"));
            HashMap rd = (HashMap) result.getData();
            rd.put("serviceurl", service.getProperties().get("serviceurl"));
            rd.put("defaultLanguage", service.getProperties().get("default-language"));
            rd.put("token", (String) request.parameters.get("tid"));  // fake tokens doesn't pass SecurityFilter
            rd.put("user", request.headers.getFirst("X-user-id"));
            rd.put("environmentName", service.getName());
            rd.put("serviceversion", service.getServiceVersion());
            rd.put("cricketversion", service.getKernelVersion());
            rd.put("javaversion", System.getProperty("java.version"));
            rd.put("wwwTheme", service.getProperties().getOrDefault("www-theme", "theme0"));
            List<String> roles = request.headers.get("X-user-role");
            if (roles != null && roles.size() > 0) {
                StringBuilder sb = new StringBuilder("[");
                for (String role : roles) {
                    sb.append(role).append(",");
                }
                rd.put("roles", sb.substring(0, sb.length() - 1) + "]");
            } else {
                rd.put("roles", "[]");
            }
            result.setData(rd);
            // TODO: caching policy 
            result.setMaxAge(120);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if ("HEAD".equalsIgnoreCase(request.method)) { //quick hack
            byte[] empty = {};
            result.setPayload(empty);
        }
        return result;
    }

    @EventHook(className = "org.cricketmsf.microsite.event.auth.AuthEvent", procedure = Procedures.AUTH_LOGIN)
    public Result authLogin(AuthEvent event) {
        Token token = service.authAdapter.login(event.getData().get("login"), event.getData().get("password"));
        return new Result(token != null ? token.getToken() : null, Procedures.AUTH_LOGIN);
    }

    @EventHook(className = "org.cricketmsf.microsite.event.auth.AuthEvent", procedure = Procedures.AUTH_LOGOUT)
    public Object authLogout(AuthEvent event) {
        Boolean ok = service.authAdapter.logout(event.getData().get("token"));
        return new Result(ok, Procedures.AUTH_LOGOUT);
    }

    @EventHook(className = "org.cricketmsf.microsite.event.auth.AuthEvent", procedure = Procedures.AUTH_CHECK_TOKEN)
    public Object authCheck(AuthEvent event) {
        boolean ok = service.authAdapter.checkToken(event.getData().get("token"));
        return new Result(ok, Procedures.AUTH_CHECK_TOKEN);
    }

    @EventHook(className = "org.cricketmsf.microsite.event.auth.AuthEvent", procedure = Procedures.AUTH_REFRESH_TOKEN)
    public Object authRefresh(AuthEvent event) {
        boolean ok = service.authAdapter.refreshToken(event.getData().get("token"));
        return new Result(ok, Procedures.AUTH_REFRESH_TOKEN);
    }

    @EventHook(className = "org.cricketmsf.microsite.event.cms.CmsEvent", procedure = Procedures.CS_GET)
    public Object getPublishedContent(CmsEvent event) {
        try {
            return new ContentRequestProcessor().processGetPublished((HashMap) event.getData(), service.contentManager);
        } catch (Exception e) {
            e.printStackTrace();
            StandardResult r = new StandardResult();
            r.setCode(ResponseCode.NOT_FOUND);
            return r;
        }
    }

    @EventHook(className = "org.cricketmsf.microsite.event.cms.CmsEvent", procedure = Procedures.CMS_GET)
    public Object getContent(CmsEvent event) {
        try {
            return new ContentRequestProcessor().processGet((HashMap) event.getData(), service.contentManager);
        } catch (Exception e) {
            e.printStackTrace();
            StandardResult r = new StandardResult();
            r.setCode(ResponseCode.NOT_FOUND);
            return r;
        }
    }

    @EventHook(className = "org.cricketmsf.microsite.event.cms.CmsEvent", procedure = Procedures.CMS_POST)
    public Object setContent(CmsEvent event) {
        try {
            return new ContentRequestProcessor().processPost(event, service.contentManager);
        } catch (Exception e) {
            e.printStackTrace();
            StandardResult r = new StandardResult();
            r.setCode(ResponseCode.NOT_FOUND);
            return r;
        }
    }

    @EventHook(className = "org.cricketmsf.microsite.event.cms.CmsEvent", procedure = Procedures.CMS_PUT)
    public Object updateContent(CmsEvent event) {
        try {
            return new ContentRequestProcessor().processPut(event, service.contentManager);
        } catch (Exception e) {
            e.printStackTrace();
            StandardResult r = new StandardResult();
            r.setCode(ResponseCode.NOT_FOUND);
            return r;
        }
    }

    @EventHook(className = "org.cricketmsf.microsite.event.cms.CmsEvent", procedure = Procedures.CMS_DELETE)
    public Object removeContent(CmsEvent event) {
        try {
            return new ContentRequestProcessor().processDelete(event, service.contentManager);
        } catch (Exception e) {
            e.printStackTrace();
            StandardResult r = new StandardResult();
            r.setCode(ResponseCode.NOT_FOUND);
            return r;
        }
    }

    @EventHook(className = "org.cricketmsf.microsite.event.cms.CmsEvent", procedure = Procedures.CMS_CONTENT_CHANGED)
    public Object clearWebCache(CmsEvent event) {
        try {
            service.database.clear("webcache_pl");
        } catch (KeyValueDBException ex) {
            logger.warn("Problem while clearing web cache - " + ex.getMessage());
        }
        try {
            service.database.clear("webcache_en");
        } catch (KeyValueDBException ex) {
            logger.warn("Problem while clearing web cache - " + ex.getMessage());
        }
        try {
            service.database.clear("webcache_fr");
        } catch (KeyValueDBException ex) {
            logger.warn("Problem while clearing web cache - " + ex.getMessage());
        }
        return null;
    }

    /**
     * Return user data
     *
     * @param event
     * @return
     */
    @EventHook(className = "org.cricketmsf.microsite.event.user.UserEvent", procedure = Procedures.USER_GET)
    public Result userGet(UserEvent event) {
        return service.userAdapter.handleGet((HashMap) event.getData()).procedure(Procedures.USER_GET);
    }

    @EventHook(className = "org.cricketmsf.microsite.event.user.UserEvent", procedure = Procedures.USER_REGISTER)
    public Object userAdd(UserEvent event) {
        return service.userAdapter.handleRegisterUser((User) event.getData()).procedure(Procedures.USER_REGISTER);
    }

    @EventHook(className = "org.cricketmsf.microsite.event.user.UserEvent", procedure = Procedures.USER_UPDATE)
    public Object userUpdate(UserEvent event) {
        return service.userAdapter.handleUpdateUser((HashMap) event.getData()).procedure(Procedures.USER_UPDATE);
    }

    @EventHook(className = "org.cricketmsf.microsite.event.user.UserEvent", procedure = Procedures.USER_UPDATED)
    public Object userUpdated(UserEvent event) {
        service.gdprLogger.print("USER DATA UPDATED FOR " + event.getData());
        return null;
    }

    @EventHook(className = "org.cricketmsf.microsite.event.user.UserEvent", procedure = Procedures.USER_REMOVAL_SCHEDULED)
    public Object userRemoveSheduled(UserEvent event) {
        try {
            String uid = (String) event.getData();
            User user = service.userAdapter.get(uid);
            service.gdprLogger.print("DELETE REQUEST FOR " + user.getNumber());
            service.emailSender.send(
                    user.getEmail(),
                    "Cricket unregistration confirmed",
                    "We received a request to remove your account from Cricket Platform with this email address.<br>"
                    + "Your account is locked now and all data related to your account will be deleted to the end of next work day.<br>"
                    + "If you received this email by mistake, you can contact our support before this date to stop unregistration procedure."
            );
            service.emailSender.send((String) service.getProperties().getOrDefault("admin-notification-email", ""), "Cricket - unregister", uid);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage() + " while sending confirmation emai");
        }
        return null;
    }

    @EventHook(className = "org.cricketmsf.microsite.event.user.UserEvent", procedure = Procedures.USER_REMOVE)
    public Object userRemove(UserEvent event) {
        return service.userAdapter.handleDeleteUser((HashMap) event.getData()).procedure(Procedures.USER_REMOVE);
    }

    @EventHook(className = "org.cricketmsf.microsite.event.user.UserEvent", procedure = Procedures.USER_CONFIRM_REGISTRATION)
    public Object userConfirmationRequired(UserEvent event) {
        try {
            String uid = (String) event.getData();
            User user = service.userAdapter.get(uid);
            service.gdprLogger.print("REGISTERED USER " + user.getNumber() + " " + user.getUid());
            long timeout = 1800 * 1000; //30 minut
            service.authAdapter.createConfirmationToken(user, user.getConfirmString(), timeout);
            service.emailSender.send(
                    user.getEmail(),
                    "Micrisite registration confirmation",
                    "We received a request to sign up to Microsite with this email address.<br>"
                    + "<a href='" + service.getProperties().get("serviceurl") + "/api/confirm?key=" + user.getConfirmString() + "'>Click here to confirm your registration</a><br>"
                    + "If you received this email by mistake, simply delete it. You won't be registered if you don't click the confirmation link above."
            );
            service.emailSender.send((String) service.getProperties().getOrDefault("admin-notification-email", ""), "Cricket - registration", uid);

        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage() + " while sending confirmation emai");
        }
        return null;
    }

    @EventHook(className = "org.cricketmsf.microsite.event.user.UserEvent", procedure = Procedures.USER_REGISTRATION_CONFIRMED)
    public Object registrationConfirmed(UserEvent event) {
        service.gdprLogger.print("REGISTRATION CONFIRMED FOR " + event.getData());
        return null;
    }

    @EventHook(className = "org.cricketmsf.microsite.event.user.UserEvent", procedure = Procedures.USER_AFTER_REMOVAL)
    public Object userRemoved(UserEvent event) {
        service.gdprLogger.print("DELETED USER " + event.getData());
        return null;
    }

    @EventHook(className = "org.cricketmsf.microsite.event.user.UserEvent", procedure = Procedures.USER_RESET_PASSWORD)
    public Object userResetPassword(UserEvent event) {
        String payload = null;
        try {
            payload = (String) event.getData();
        } catch (ClassCastException e) {
        }
        if (payload != null && !payload.isEmpty()) {
            String[] params = payload.split(":");
            if (params.length == 2) {
                //TODO: email templates from CMS
                String passResetLink = service.properties.getOrDefault("serviceurl", "") + "?tid=" + params[0] + "#account";
                service.emailSender.send(params[1], "Password Reset Request", "Click here to change password: <a href=\"" + passResetLink + "\">" + passResetLink + "</a>");
            } else {
                logger.warn("Malformed payload->{}", payload);
            }
        } else {
            logger.warn("Malformed payload->{}", payload);
        }
        service.gdprLogger.print("RESET PASSWORD REQUESTED FOR " + event.getData());
        return null;
    }

    @EventHook(className = "org.cricketmsf.event.Event", procedure = Procedures.SA_ANY)
    public Object systemServiceHandle(Event event) {
        return new SiteAdministrationModule().handleRestEvent(event);
    }

    @EventHook(className = "org.cricketmsf.event.Event", procedure = Procedures.SYSTEM_SHUTDOWN)
    public Object handleShutdownRequest(Event event) {
        service.shutdown();
        return null;
    }

    @EventHook(className = "org.cricketmsf.event.Event", procedure = Procedures.SYSTEM_STATUS)
    public Object handleStatusRequest(Event event) {
        //System.out.println(printStatus());
        return null;
    }

    @EventHook(className = "org.cricketmsf.event.Event", procedure = Procedures.SYSTEM_BACKUP)
    public Object handleBackupRequest(Event event) {
        SiteAdministrationModule.getInstance().backupDatabases(
                service.database,
                service.userDB,
                service.authDB,
                service.cmsDatabase,
                (String) event.getData()
        );
        return null;
    }

    @EventHook(className = "org.cricketmsf.event.Event")
    public Object logEventsNotHandled(Event event) {
        logger.warn("org.cricketmsf.event.Event procedure {} not handled", service.getProceduresDictionary().getName(Procedures.DEFAULT));
        return null;
    }

    /*
    @EventHook(className = "org.cricketmsf.event.Event", procedure = Procedures.SYSTEM_STATUS)
    public Object getStatusInfo(Event event) {
        System.out.println(siteAdmin.getServiceInfo().getData());
        return null;
    }
     */
}
