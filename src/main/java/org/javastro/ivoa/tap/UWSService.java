package org.javastro.ivoa.tap;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.javastro.ivoa.entities.uws.ExecutionPhase;
import org.javastro.ivoa.entities.uws.Jobs;
import org.javastro.ivoa.entities.uws.ShortJobDescription;
import org.javastro.ivoacore.uws.*;
import org.javastro.ivoacore.uws.environment.execution.ParameterValue;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

/**
 * CDI service that provides transactional access to JobManager operations.
 * This bridges the library JobManager with CDI transaction management.
 */
@ApplicationScoped
public class UWSService {

    @Inject
    JobManager jobManager;

    @Transactional
    public BaseUWSJob createJob(JobSpecification specification) throws UWSException {
        return jobManager.createJob(specification);
    }

    @Transactional
    public ExecutionPhase setPhase(String jobId, String newPhase) throws UWSException {
        return jobManager.setPhase(jobId, newPhase);
    }

    @Transactional
    public boolean deleteJob(String jobId) throws UWSException {
        return jobManager.deleteJob(jobId);
    }

    @Transactional
    public void runJob(String jobId) throws UWSException {
        jobManager.runJob(jobId);
    }

    @Transactional
    public void abortJob(String jobId) throws UWSException {
        jobManager.abortJob(jobId);
    }

    // Non-transactional read operations
    public Set<String> listJobIDs() throws UWSException {
        return jobManager.listJobIDs();
    }

    public Jobs listJobs(String phase, ZonedDateTime after, Integer last) throws UWSException {
        return jobManager.listJobs(phase, after, last);
    }

    public org.javastro.ivoa.entities.uws.Job jobDetail(String jobId) throws UWSException {
        return jobManager.jobDetail(jobId);
    }

    public List<ParameterValue> getJobResults(String jobId) throws UWSException {
        return jobManager.getJobResults(jobId);
    }

    public String jobErrorDetail(String jobId) {
        return jobManager.jobErrorDetail(jobId);
    }

    public ZonedDateTime setDestruction(String jobId, ZonedDateTime destructionTime) throws UWSException {
        return jobManager.setDestruction(jobId, destructionTime);
    }

    public Long setExecutionDuration(String jobId, Long duration) throws UWSException {
        return jobManager.setExecutionDuration(jobId, duration);
    }
}
