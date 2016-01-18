package nl.jpoint.vertxdeployjenkins;

import hudson.Util;
import hudson.tasks.Maven;
import jenkins.model.Jenkins;
import jenkins.mvn.GlobalSettingsProvider;
import jenkins.mvn.SettingsProvider;
import org.apache.commons.lang.StringUtils;

public class VertxDeployMaven extends Maven {

    private final static String DEFAULT_PLUGIN_VERSION = "1.2.2";

    private  VertxDeployMaven(String targets, String name, String pom, String properties, String jvmOptions, boolean usePrivateRepository, SettingsProvider settings, GlobalSettingsProvider globalSettings) {
        super(targets, name, pom, properties, jvmOptions, usePrivateRepository, settings, globalSettings);
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) Jenkins.getInstance().getDescriptorOrDie(Maven.class);
    }

    public static VertxDeployMaven executeMaven(String mavenName, String properties, String mavenOpts, String pluginVersion) {
        String version = DEFAULT_PLUGIN_VERSION;
        if (StringUtils.isNotBlank(pluginVersion)) {
            version = pluginVersion;
        }
        return new VertxDeployMaven("nl.jpoint.vertx-deploy-tools:vertx-deploy-maven-plugin:"+version+ ":deploy-single-as " + Util.fixNull(mavenOpts), mavenName, "pom.xml", properties, null, false, null, null);

    }
}
