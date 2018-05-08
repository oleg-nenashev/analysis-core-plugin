package io.jenkins.plugins.analysis.core.history;

import java.sql.Ref;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Issues;
import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import static io.jenkins.plugins.analysis.core.testutil.Assertions.assertThat;
import io.jenkins.plugins.analysis.core.views.ResultAction;
import static org.mockito.Mockito.*;

import hudson.model.Result;
import hudson.model.Run;

/**
 * Tests the abstract class {@link ReferenceFinderTest} using the abstract test pattern.
 *
 * @author Stephan Pl�derl
 */
public abstract class ReferenceFinderTest {

    /**
     * Should return an instance of {@link ReferenceFinder} with overallResultSuccessMustBe set to true.
     *
     * @return instance of a childclass of {@link ReferenceFinder} which shall be tested.
     */
    protected abstract ReferenceFinder getReferenceFinder(Run baseline, ResultSelector resultSelector);

    /** Verifies that {@link ReferenceFinder#create(Run, ResultSelector, boolean, boolean)} creates the instances of the correct ReferenceProviders. */
    @Test
    void createsRightInstance() {
        assertThat(ReferenceFinder.create(null, null, false, false)).isInstanceOf(StablePluginReference.class);
        assertThat(ReferenceFinder.create(null, null, true, false)).isInstanceOf(PreviousRunReference.class);
    }

    /** Verifies that {@link ReferenceFinder#getAnalysisRun()} returns the right owner. */
    @Test
    void shouldReturnRightOwner() {
        Run baseline = mock(Run.class);
        Run prevBuild = mock(Run.class);
        when(baseline.getPreviousBuild()).thenReturn(prevBuild);
        when(prevBuild.getResult()).thenReturn(Result.SUCCESS);

        ResultAction resultAction = mock(ResultAction.class);
        when(prevBuild.getActions(ResultAction.class)).thenReturn(Collections.singletonList(resultAction));
        //noinspection unchecked
        when(resultAction.getOwner()).thenReturn(baseline, (Run) null);
        when(resultAction.isSuccessful()).thenReturn(true);

        ResultSelector resultSelector = mock(ResultSelector.class);
        when(resultSelector.get(prevBuild)).thenReturn(Optional.of(resultAction));

        ReferenceFinder referenceFinder = getReferenceFinder(baseline, resultSelector);

        assertThat(referenceFinder.getAnalysisRun()).contains(baseline);
        assertThat(referenceFinder.getAnalysisRun()).isEmpty();
    }

    /** Verifies that {@link ReferenceFinder#getIssues()} returns the issues of the reference-job. */
    @Test
    void getIssuesOfReferenceJob() {
        Run baseline = mock(Run.class);
        Run prevBuild = mock(Run.class);
        when(baseline.getPreviousBuild()).thenReturn(prevBuild);
        when(prevBuild.getResult()).thenReturn(Result.SUCCESS);

        AnalysisResult analysisResult = mock(AnalysisResult.class);

        ResultAction resultAction = mock(ResultAction.class);
        when(resultAction.isSuccessful()).thenReturn(true);
        when(resultAction.getResult()).thenReturn(analysisResult);

        ResultSelector resultSelector = mock(ResultSelector.class);
        when(resultSelector.get(prevBuild)).thenReturn(Optional.of(resultAction));

        List<ResultAction> actions = Collections.singletonList(resultAction);
        when(prevBuild.getActions(ResultAction.class)).thenReturn(actions);

        IssueBuilder builder = new IssueBuilder();
        Issues issues = new Issues<>(
                Collections.singletonList(builder.setCategory("testCompany").setLineEnd(1).build()));
        //noinspection unchecked
        when(analysisResult.getIssues()).thenReturn(issues, (Issues) null);

        ReferenceFinder referenceFinder = getReferenceFinder(baseline, resultSelector);

        assertThat(referenceFinder.getIssues()).isEqualTo(issues);
        assertThat(referenceFinder.getIssues()).isEqualTo(new Issues<>());
    }

}