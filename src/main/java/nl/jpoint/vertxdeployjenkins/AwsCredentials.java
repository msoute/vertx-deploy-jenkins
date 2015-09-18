package nl.jpoint.vertxdeployjenkins;

import hudson.util.Secret;
import org.kohsuke.stapler.DataBoundConstructor;

public class AwsCredentials {
    /**
     * @return all available installations, never <tt>null</tt>
     * @since 1.7
     */
/*    public static final AwsCredentials get() {
        Jenkins jenkins = Jenkins.getInstance();

        SonarPublisher.DescriptorImpl sonarDescriptor = Jenkins.getInstance().getDescriptorByType(SonarPublisher.DescriptorImpl.class);
        return sonarDescriptor.getInstallations();
    }*/

    private final String accessKey;
    private final Secret secretAccessKey;

    @DataBoundConstructor
    public AwsCredentials(String accessKey, String secretAccessKey) {
        this.accessKey = accessKey;
        this.secretAccessKey = Secret.fromString(secretAccessKey);
    }

    public String getAccessKey() {
        return accessKey;
    }

    public String getSecretAccessKey() {
        return Secret.toString(secretAccessKey);
    }
}
