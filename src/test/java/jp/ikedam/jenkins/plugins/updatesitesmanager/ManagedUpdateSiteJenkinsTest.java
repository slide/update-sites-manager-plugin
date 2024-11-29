/*
 * The MIT License
 *
 * Copyright (c) 2013 IKEDA Yasuyuki
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package jp.ikedam.jenkins.plugins.updatesitesmanager;

import static hudson.util.FormValidation.Kind.ERROR;
import static hudson.util.FormValidation.Kind.OK;
import static jp.ikedam.jenkins.plugins.updatesitesmanager.testext.WebServerRecipe.RuleRunnerImpl.getResource;
import static jp.ikedam.jenkins.plugins.updatesitesmanager.testext.WebServerRecipe.RuleRunnerImpl.urlFor;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.Is.is;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import jp.ikedam.jenkins.plugins.updatesitesmanager.testext.WebServerRecipe;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.JenkinsRule;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;

/**
 * Tests for ManagedUpdateSite, concerned with Jenkins.
 */
@RunWith(DataProviderRunner.class)
public class ManagedUpdateSiteJenkinsTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void testDescriptorDoCheckCaCertificate() throws IOException, URISyntaxException {
        String caCertificate =
                FileUtils.readFileToString(getResource("caCertificate.crt", getClass()), Charset.defaultCharset());

        assertThat(
                "Always ok if certificate is disabled", getDescriptor().doCheckCaCertificate(false, null).kind, is(OK));

        assertThat("OK for valid certificate", getDescriptor().doCheckCaCertificate(true, caCertificate).kind, is(OK));
    }

    @Test
    public void shouldSuccessfullyCheckUpdatesWithoutAnyUC() throws Exception {
        j.getInstance().getUpdateCenter().getSites().clear();

        HttpResponse rsp = j.getInstance().getPluginManager().doCheckUpdatesServer();

        // this fails with Jenkins < 1.600, Jenkins < 1.596.1
        assertThat(
                "Should be redirect",
                rsp,
                instanceOf(HttpResponses.forwardToPreviousPage().getClass()));
    }

    @Test
    @DataProvider(
            value = {"", " ", "null", "blabla"},
            trimValues = false)
    public void shouldReturnValidationErrOnWrongCert(String cert) {
        assertThat("Bad certificate", getDescriptor().doCheckCaCertificate(true, cert).kind, is(ERROR));
    }

    @Test
    @WebServerRecipe
    public void shouldFailUpdateWithoutCert() throws Exception {
        TestManagedUpdateSite site = forMethod(j.getTestDescription().getMethodName());

        j.getInstance().getUpdateCenter().getSites().clear();
        j.getInstance().getUpdateCenter().getSites().add(site);

        site.setCaCertificate(null);
        HttpResponse rsp = j.getInstance().getPluginManager().doCheckUpdatesServer();
        assertThat("smth went wrong with rsp type", rsp, instanceOf(HttpResponse.class));
        assertThat(
                "Should be redirect",
                rsp,
                instanceOf(HttpResponses.forwardToPreviousPage().getClass()));
    }

    @Test
    @WebServerRecipe
    public void shouldSuccessfullyUpdateWithWorkingUC() throws Exception {
        String caCertificate =
                FileUtils.readFileToString(getResource("caCertificate.crt", getClass()), Charset.defaultCharset());

        TestManagedUpdateSite site = forMethod(j.getTestDescription().getMethodName());

        j.getInstance().getUpdateCenter().getSites().clear();
        j.getInstance().getUpdateCenter().getSites().add(site);

        site.setCaCertificate(caCertificate);
        HttpResponse rsp = j.getInstance().getPluginManager().doCheckUpdatesServer();
        assertThat(
                "Should be redirect",
                rsp,
                instanceOf(HttpResponses.forwardToPreviousPage().getClass()));
    }

    private ManagedUpdateSite.DescriptorImpl getDescriptor() {
        return (ManagedUpdateSite.DescriptorImpl)
                new ManagedUpdateSite(null, null, false, null, null, false).getDescriptor();
    }

    public static TestManagedUpdateSite forMethod(String method) throws MalformedURLException {
        return new TestManagedUpdateSite(
                "test",
                new URL(new URL(urlFor(method)), "update-center.json").toExternalForm(),
                false,
                null,
                "test",
                false);
    }

    public static class TestManagedUpdateSite extends ManagedUpdateSite {
        public TestManagedUpdateSite(
                String id, String url, boolean useCaCertificate, String caCertificate, String note, boolean disabled) {
            super(id, url, useCaCertificate, caCertificate, note, disabled);
        }
    }
}
