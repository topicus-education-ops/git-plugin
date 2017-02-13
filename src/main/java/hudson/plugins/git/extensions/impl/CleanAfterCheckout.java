package hudson.plugins.git.extensions.impl;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.jenkinsci.plugins.gitclient.CleanCommand;
import org.jenkinsci.plugins.gitclient.GitClient;
import org.kohsuke.stapler.DataBoundConstructor;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.plugins.git.GitException;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.extensions.GitClientType;
import hudson.plugins.git.extensions.GitSCMExtension;
import hudson.plugins.git.extensions.GitSCMExtensionDescriptor;

/**
 * git-clean after the checkout. Supports exclude patterns (-e option) and an operation specific timeout value.
 *
 */
public class CleanAfterCheckout extends GitSCMExtension {
	private List<ExcludePattern> excludePatterns = Collections.emptyList();
	
	private Integer timeout = -1;
	
    @DataBoundConstructor
    public CleanAfterCheckout(List<ExcludePattern> excludePatterns, Integer timeout) {
    	this.excludePatterns=excludePatterns;
    	this.timeout=timeout;
    }

    public List<ExcludePattern> getExcludePatterns() {
        return excludePatterns;
    }
    
    public Integer getTimeout() {
        return timeout;
    }

	@Override
    public void onCheckoutCompleted(GitSCM scm, Run<?, ?> build, GitClient git, TaskListener listener) throws IOException, InterruptedException, GitException {
        listener.getLogger().println("Cleaning workspace");
        CleanCommand cmd = git.clean_();
        if (excludePatterns != null && !excludePatterns.isEmpty()) {
        	cmd.excludePatterns(Lists.transform(excludePatterns, ExcludePattern.CLEAN_EXCLUDE_PATTERN_TO_PATTERN));
        }
        if (timeout != null && timeout > -1) {
        	cmd.timeout(timeout);
        }
        cmd.execute();
        	
        // TODO: revisit how to hand off to SubmoduleOption
        for (GitSCMExtension ext : scm.getExtensions()) {
            ext.onClean(scm, git);
        }
    }

//    @Override
//    public GitClientType getRequiredClient() {
//        return GitClientType.GITCLI;
//    }
    
    @Extension
    public static class DescriptorImpl extends GitSCMExtensionDescriptor {
        @Override
        public String getDisplayName() {
            return "Clean after checkout (Git CLI)";
        }
    }
    
    public static class ExcludePattern extends AbstractDescribableImpl<ExcludePattern> implements Serializable {

		private static final long serialVersionUID = 3706207528430240699L;

		public static final transient CleanExcludePatternToPattern CLEAN_EXCLUDE_PATTERN_TO_PATTERN = new CleanExcludePatternToPattern();
    	
    	private String pattern;

        @DataBoundConstructor
        public ExcludePattern(String pattern) {
            this.pattern = pattern;
        }

        public String getPattern() {
            return pattern;
        }
        
        public void setPattern(String pattern) {
			this.pattern = pattern;
		}
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ExcludePattern)) return false;

            ExcludePattern that = (ExcludePattern) o;

            return pattern.equals(that.pattern);

        }

        @Override
        public int hashCode() {
            return pattern.hashCode();
        }

        @Override
        public String toString() {
            return pattern;
        }
        
        private static class CleanExcludePatternToPattern implements Function<ExcludePattern, String>, Serializable {
            public String apply(ExcludePattern cleanExcludePattern) {
                return cleanExcludePattern.getPattern();
            }
        }

        @Extension
        public static class DescriptorImpl extends Descriptor<ExcludePattern> {
            @Override
            public String getDisplayName() { return "Exclude pattern"; }
        }
    }
}
