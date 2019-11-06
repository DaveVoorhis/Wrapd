import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import static org.junit.platform.engine.discovery.DiscoverySelectors.*;

import java.util.Arrays;
import java.util.stream.Collectors;

public class WrapdTests {
		
	private static String stackTraceToString(StackTraceElement[] trace) {
		return Arrays.stream(trace).map(element -> element.toString()).collect(Collectors.joining("\n\t"));
	}
	
	public static void main(String args[]) {
	    final LauncherDiscoveryRequest request = 
		        LauncherDiscoveryRequestBuilder.request()
		                                   .selectors(
		                                		   selectPackage("org.reldb.wrapd.tests.main")
		                                		   )
		                                   .build();

        final Launcher launcher = LauncherFactory.create();
        final SummaryGeneratingListener listener = new SummaryGeneratingListener();

        launcher.registerTestExecutionListeners(listener);
        launcher.execute(request);

        TestExecutionSummary summary = listener.getSummary();
        long testFoundCount = summary.getTestsFoundCount();
        var failures = summary.getFailures();
        System.out.println("Tests found: " + testFoundCount);
        System.out.println("Tests succeeded: " + summary.getTestsSucceededCount());
        failures.forEach(failure -> System.out.println(
        		"Test failed: " + failure.getException() + 
        		" in " + failure.getTestIdentifier().getDisplayName() + 
        		" at " + stackTraceToString(failure.getException().getStackTrace())));
	}
}
