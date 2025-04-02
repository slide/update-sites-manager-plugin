package jp.ikedam.jenkins.plugins.updatesitesmanager.internal;

import hudson.util.HttpResponses;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;

import jakarta.servlet.ServletException;
import jp.ikedam.jenkins.plugins.updatesitesmanager.UpdateSitesManager;
import org.kohsuke.stapler.StaplerRequest2;
import org.kohsuke.stapler.StaplerResponse2;
import org.kohsuke.stapler.interceptor.Interceptor;
import org.kohsuke.stapler.interceptor.InterceptorAnnotation;

/**
 * Just redirects to url of sites manager if not POST
 *
 * @author lanwen (Merkushev Kirill)
 */
@Target(ElementType.METHOD)
@Documented
@InterceptorAnnotation(IgnoreNotPOST.Processor.class)
public @interface IgnoreNotPOST {
    class Processor extends Interceptor {
        @Override
        public Object invoke(StaplerRequest2 request, StaplerResponse2 response, Object instance, Object[] arguments)
                throws IllegalAccessException, InvocationTargetException, ServletException {

            if (!request.getMethod().equals("POST")) {
                throw new InvocationTargetException(HttpResponses.redirectViaContextPath(UpdateSitesManager.URL));
            }

            return target.invoke(request, response, instance, arguments);
        }
    }
}
