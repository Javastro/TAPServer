package org.javastro.ivoa.tap;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.javastro.ivoacore.tap.TAPJob;
import org.javastro.ivoacore.tap.TAPJobSpecification;
import org.javastro.ivoacore.uws.UWSException;

/**
 * TAPJobService is responsible for managing the creation of TAPJob instances
 * based on provided specifications. It relies on TAPHelper to handle the
 * underlying operations associated with job management.
 * <p>
 * This service is application-scoped, ensuring a single instance is used
 * throughout the application context. In a multithreaded environment,
 * the service is designed to be thread-safe.
 */
@ApplicationScoped
public class TAPJobService {

    private final TAPHelper tapHelper;

    public TAPJobService(TAPHelper tapHelper) {
        this.tapHelper = tapHelper;
    }

    @Transactional
    public TAPJob createJob(TAPJobSpecification spec) throws UWSException {
        return (TAPJob) tapHelper.jobmanager.createJob(spec);
    }
}
