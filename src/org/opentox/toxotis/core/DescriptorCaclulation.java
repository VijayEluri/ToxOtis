package org.opentox.toxotis.core;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.opentox.toxotis.ErrorCause;
import org.opentox.toxotis.ToxOtisException;
import org.opentox.toxotis.client.ClientFactory;
import org.opentox.toxotis.client.IPostClient;
import org.opentox.toxotis.client.VRI;
import org.opentox.toxotis.client.collection.Media;
import org.opentox.toxotis.client.collection.Services;
import org.opentox.toxotis.core.component.Task;
import org.opentox.toxotis.util.TaskRunner;
import org.opentox.toxotis.util.aa.AuthenticationToken;
import org.opentox.toxotis.util.spiders.TaskSpider;

/**
 * Abstact class that implements the interface {@link IDescriptorCalculation } offering
 * some facilities for easier calculation of descriptors using a remote DC algorithm.
 * 
 * @author Pantelis Sopasakis
 * @author Charalampos Chomenides
 */
public abstract class DescriptorCaclulation<T extends OTPublishable> extends OTPublishable<T> implements IDescriptorCalculation {

    private org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DescriptorCaclulation.class);

    public DescriptorCaclulation() {
        super();
    }

    public DescriptorCaclulation(VRI uri) {
        super(uri);
    }

    public Task calculateDescriptors(VRI descriptorCalculationAlgorithm, AuthenticationToken token, String... serviceConfiguration) throws ToxOtisException {        

        /** REQUEST */
        IPostClient pc = ClientFactory.createPostClient(descriptorCalculationAlgorithm);
        pc.authorize(token);
        pc.addPostParameter("dataset_uri", getUri().toString()); // dataset_uri={compound_uri}
        if (serviceConfiguration != null) {
            for (int i = 0; i < serviceConfiguration.length; i++) {
                pc.addPostParameter(serviceConfiguration[i], serviceConfiguration[++i]);
            }
        }
        pc.setMediaType(Media.TEXT_URI_LIST);
        pc.post(); //Request is performed...

        /** RESPONSE */
        String taskUri = pc.getResponseText().trim();

        try {
            Thread.sleep(4000);
        } catch (InterruptedException ex) {
            logger.error("Interruption exception while sleeping", ex);
            throw new ToxOtisException(ex);
        }

        try {
            TaskSpider taskSpider = new TaskSpider(new VRI(taskUri));
            return taskSpider.parse();
        } catch (URISyntaxException ex) {
            throw new ToxOtisException("The remote service at " + descriptorCalculationAlgorithm
                    + " returned an invalid task URI : " + taskUri, ex);
        } finally {
            if (pc != null) {
                try {
                    pc.close();
                } catch (IOException ex) {
                    String message = "Client used to perform the POST operation "
                            + "at '" + descriptorCalculationAlgorithm.toString() + "' could not close!";
                    logger.error(message, ex);
                    throw new ToxOtisException(ErrorCause.StreamCouldNotClose, message, ex);
                }
            }
        }
    }

    public Task calculateDescriptors(VRI descriptorCalculationAlgorithm, AuthenticationToken token) throws ToxOtisException {
        return calculateDescriptors(descriptorCalculationAlgorithm, token, "ALL", "true");
    }

    public Future<VRI> futureDescriptors(VRI descriptorCalculationAlgorithm, AuthenticationToken token, String... serviceConfiguration) throws ToxOtisException {
        return futureDescriptors(descriptorCalculationAlgorithm, token, Executors.newSingleThreadExecutor(), serviceConfiguration);
    }

    public Future<VRI> futureDescriptors(final VRI descriptorCalculationAlgorithm,
            final AuthenticationToken token, ExecutorService executor, final String... serviceConfiguration) {
        Future<VRI> future = executor.submit(new Callable<VRI>() {

            public VRI call() throws Exception {
                Task t = calculateDescriptors(descriptorCalculationAlgorithm, token, serviceConfiguration);
                final TaskRunner taskRunner = new TaskRunner(t);
                Task result = taskRunner.call();
                VRI resultUri = null;
                if (result != null) {
                    resultUri = result.getResultUri();
                }
                return resultUri;
            }
        });
        return future;
    }

    public Future<VRI> futureCDKPhysChemDescriptors(AuthenticationToken token, VRI datasetService) throws ToxOtisException {
        return futureDescriptors(Services.DescriptorCalculation.cdkPhysChem(), token, datasetService);
    }

    public Future<VRI> futureJoeLibDescriptors(AuthenticationToken token, VRI datasetService) throws ToxOtisException {
        return futureDescriptors(Services.DescriptorCalculation.joelib(), token, datasetService);
    }

    public Future<VRI> futureDescriptors(VRI descriptorCalculationAlgorithm, AuthenticationToken token, VRI datasetService) throws ToxOtisException {
        ArrayList<String> options = new ArrayList<String>();
        options.add("ALL");
        options.add("true");
        if (datasetService != null) {
            options.add("dataset_service");
            options.add(datasetService.toString());
        }
        String[] ops = new String[]{};
        ops = options.toArray(ops);
        return futureDescriptors(descriptorCalculationAlgorithm, token, ops);
    }
}
