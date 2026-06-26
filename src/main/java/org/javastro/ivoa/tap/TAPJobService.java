package org.javastro.ivoa.tap;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.javastro.ivoacore.tap.TAPJob;
import org.javastro.ivoacore.tap.TAPJobSpecification;
import org.javastro.ivoacore.uws.UWSException;

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
