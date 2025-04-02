package jp.ikedam.jenkins.plugins.updatesitesmanager.internal;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;

import jakarta.servlet.ServletException;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.StaplerRequest2;
import org.kohsuke.stapler.StaplerResponse2;
import org.kohsuke.stapler.interceptor.Interceptor;
import org.kohsuke.stapler.interceptor.InterceptorAnnotation;

/**
 * Returns 403 if not permitted
 *
 * @author lanwen (Merkushev Kirill)
 */
@Target(ElementType.METHOD)
@Documented
@InterceptorAnnotation(OnlyAdminister.Processor.class)
public @interface OnlyAdminister {
    class Processor extends Interceptor {
        @Override
        public Object invoke(StaplerRequest2 request, StaplerResponse2 response, Object instance, Object[] arguments)
                throws IllegalAccessException, InvocationTargetException, ServletException {

            Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            return target.invoke(request, response, instance, arguments);
        }
    }
}
