package nl.jpoint.vertxdeployjenkins;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.autoscaling.AmazonAutoScalingClient;
import com.amazonaws.services.autoscaling.model.AutoScalingGroup;
import com.amazonaws.services.autoscaling.model.DescribeAutoScalingGroupsRequest;
import com.amazonaws.services.autoscaling.model.DescribeAutoScalingGroupsResult;
import hudson.Extension;
import hudson.Launcher;
import hudson.maven.MavenModuleSet;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Builder;
import hudson.tasks.Maven;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;
import java.util.List;

/**
 * Sample {@link Builder}.
 * <p/>
 * <p/>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked
 * and a new {@link VertxDeployBuilder} is created. The created
 * instance is persisted to the project configuration XML by using
 * XStream, so this allows you to use instance fields
 * to remember the configuration.
 * <p/>
 * <p/>
 * When a build is performed, the {@link #perform(AbstractBuild, Launcher, BuildListener)}
 * method will be invoked.
 *
 * @author Kohsuke Kawaguchi
 */
public class VertxDeployBuilder extends Builder {

    private final String credentialsId;
    private final String groupName;
    private final String mavenOpts;
    private final String exclusions;
    private final Integer maxSize;
    private final Integer minSize;
    private final String strategy;
    private final boolean useAwsPrivate;
    private final boolean useAwsElb;
    private final boolean decrementCapacity;
    private final boolean ignoreInStandby;
    private final boolean restart;
    private final boolean testScope;
    private final boolean deployConfig;
    private final boolean allowSnapshots;
    private final String pluginVersion;

    private String jdk;
    private final String mavenInstallationName;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public VertxDeployBuilder(String credentialsId, String groupName, String mavenOpts, String exclusions, Integer maxSize, Integer minSize,
                              String strategy, boolean useAwsPrivate, boolean useAwsElb,
                              boolean decrementCapacity, boolean ignoreInStandby, boolean restart,
                              boolean testScope, boolean deployConfig, boolean allowSnapshots, String jdk, String mavenInstallationName,
                              String pluginVersion) {
        this.credentialsId = credentialsId;
        this.groupName = groupName;
        this.mavenOpts = mavenOpts;
        this.exclusions = exclusions;
        this.maxSize = maxSize;
        this.minSize = minSize;
        this.strategy = strategy;
        this.useAwsPrivate = useAwsPrivate;
        this.useAwsElb = useAwsElb;
        this.decrementCapacity = decrementCapacity;
        this.ignoreInStandby = ignoreInStandby;
        this.restart = restart;
        this.testScope = testScope;
        this.deployConfig = deployConfig;
        this.jdk = jdk;
        this.allowSnapshots = allowSnapshots;
        this.mavenInstallationName = mavenInstallationName;
        this.pluginVersion = pluginVersion;
    }

    /**
     * We'll use this from the <tt>config.jelly</tt>.
     */

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        VertxDeployMaven maven = VertxDeployMaven.executeMaven(mavenInstallationName, this.createPropertyString(), mavenOpts, pluginVersion);
        try {
            boolean result = maven.perform(build, launcher, listener);
            build.setResult(result ? Result.SUCCESS : Result.FAILURE);
        } catch (IOException e) {
            listener.getLogger().println(e.getMessage());
            build.setResult(Result.FAILURE);
            return false;
        } catch (InterruptedException e) {
            listener.getLogger().println(e.getMessage());
            build.setResult(Result.FAILURE);
            return false;
        } catch (Exception e) {
            build.setResult(Result.FAILURE);
            listener.getLogger().println("command execution failed : " + build.getResult());
            return false;
        }

        listener.getLogger().println("Vertx deploy completed: " + build.getResult());
        return true;
    }

    private String createPropertyString() {
        return new StringBuilder()
                .append("deploy.credentialsId=").append(credentialsId).append("\n")
                .append("deploy.as.id=").append(groupName).append("\n")
                .append("deploy.as.strategy=").append(strategy).append("\n")
                .append("deploy.as.max=").append(maxSize).append("\n")
                .append("deploy.as.min=").append(minSize).append("\n")
                .append("deploy.as.private=").append(useAwsPrivate).append("\n")
                .append("deploy.as.elb=").append(useAwsElb).append("\n")
                .append("deploy.as.test=").append(testScope).append("\n")
                .append("deploy.as.config=").append(deployConfig).append("\n")
                .append("deploy.as.restart=").append(restart).append("\n")
                .append("deploy.as.decrement=").append(decrementCapacity).append("\n")
                .append("deploy.as.allowSnapshots=").append(allowSnapshots).append("\n")
                .append("deploy.exclusions=").append(exclusions).append("\n")
                .append("deploy.as.ignore=").append(ignoreInStandby).toString();

    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.STEP;
    }


    /**
     * Descriptor for {@link VertxDeployBuilder}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     * <p/>
     * <p/>
     * See <tt>src/main/resources/hudson/plugins/hello_world/HelloWorldBuilder/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
     */

    public String getStrategy() {
        return strategy;
    }

    public Integer getMinSize() {
        return minSize;
    }

    public Integer getMaxSize() {
        return maxSize;
    }

    public String getGroupName() {
        return groupName;
    }

    public boolean isUseAwsElb() {
        return useAwsElb;
    }

    public boolean isRestart() {
        return restart;
    }

    public boolean isUseAwsPrivate() {
        return useAwsPrivate;
    }

    public boolean isDecrementCapacity() {
        return decrementCapacity;
    }

    public boolean isIgnoreInStandby() {
        return ignoreInStandby;
    }

    public boolean isTestScope() {
        return testScope;
    }

    public boolean isDeployConfig() {
        return deployConfig;
    }

    public boolean isAllowSnapshots() {
        return allowSnapshots;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    public String getMavenOpts() {
        return mavenOpts;
    }

    public String getExclusions() {
        return exclusions;
    }

    public String getMavenInstallationName() {
        return mavenInstallationName;
    }

    public String getPluginVersion() { return pluginVersion; }

    public MavenModuleSet getMavenProject(AbstractBuild<?, ?> build) {
        return (build.getProject() instanceof MavenModuleSet) ? (MavenModuleSet) build.getProject() : null;
    }

    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        transient AmazonAutoScalingClient amazonAutoScalingClient;

        private AwsCredentials credentials;

        /**
         * In order to load the persisted global configuration, you have to
         * call load() in the constructor.
         */
        public DescriptorImpl() {
            load();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types 
            return true;
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Execute Vert.X Deploy";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            String accessKey = formData.getString("accessKey");
            String secretAccessKey = formData.getString("secretAccessKey");
            credentials = new AwsCredentials(accessKey, secretAccessKey);
            save();
            return super.configure(req, formData);
        }

        public String[] getAutoScalingGroups() {
            amazonAutoScalingClient = new AmazonAutoScalingClient(new BasicAWSCredentials(credentials.getAccessKey(), credentials.getSecretAccessKey()));
            amazonAutoScalingClient.setRegion(Region.getRegion(Regions.EU_WEST_1));

            DescribeAutoScalingGroupsResult result = amazonAutoScalingClient.describeAutoScalingGroups(new DescribeAutoScalingGroupsRequest());
            List<AutoScalingGroup> groups = result.getAutoScalingGroups();
            while (result.getNextToken() != null && !result.getNextToken().isEmpty()) {
                result = amazonAutoScalingClient.describeAutoScalingGroups(new DescribeAutoScalingGroupsRequest().withNextToken(result.getNextToken()));
                groups.addAll(result.getAutoScalingGroups());
            }

            String[] groupNames = new String[groups.size()];
            for (int i = 0; i < groups.size(); i++) {
                groupNames[i] = groups.get(i).getAutoScalingGroupName();
            }

            return groupNames;
        }

        public Maven.MavenInstallation[] getMavenInstallations() {
            return Jenkins.getInstance().getDescriptorByType(Maven.DescriptorImpl.class).getInstallations();
        }

        public AwsCredentials getCredentials() {
            return credentials;
        }


    }
}