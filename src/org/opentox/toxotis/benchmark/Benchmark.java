package org.opentox.toxotis.benchmark;

import org.opentox.toxotis.benchmark.job.Job;
import org.opentox.toxotis.benchmark.gauge.Gauge;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.StatisticalBarRenderer;
import org.jfree.chart.renderer.category.StatisticalLineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.statistics.DefaultStatisticalCategoryDataset;
import org.jfree.data.statistics.StatisticalCategoryDataset;
import org.jfree.data.statistics.Statistics;
import org.opentox.toxotis.ToxOtisException;

/**
 *
 * @author Charalampos Chomenides
 * @author Pantelis Sopasakis
 */
public class Benchmark {

    private String title;
    private ExecutorService executor;
    private Status status;
    private List<Job> jobs;

    public enum Status {

        RUNNING,
        IDLE,
        COMPLETED
    }

    public Benchmark(String title) {
        executor = Executors.newSingleThreadExecutor();
        jobs = new ArrayList<Job>();
        status = Status.IDLE;
        this.title = title;
    }

    public void addJob(Job job) throws ToxOtisException {
        if (!status.equals(Status.IDLE)) {
            throw new ToxOtisException("Benchmark:" + title + " is not in IDLE state and cannot accept more Jobs");
        }
        jobs.add(job);
    }

    public void setJobs(List<Job> jobs) {
        this.jobs = jobs;
    }

    

    public void start() throws ToxOtisException {
        if (!status.equals(Status.IDLE)) {
            throw new ToxOtisException("Benchmark:" + title + " is not in IDLE state and cannot be executed");
        }
        status = Status.RUNNING;
        for (Job job : jobs) {
            executor.submit(job);
        }
        try {
            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException ex) {
            throw new ToxOtisException(ex);
        }
        status = Status.COMPLETED;
    }

    public Status getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }

    public JFreeChart getBarChart(Class<? extends Gauge>... counters) {
        return getChart(new StatisticalBarRenderer(), counters);
    }

    public JFreeChart getLineChart(Class<? extends Gauge>... counters) {
        return getChart(new StatisticalLineAndShapeRenderer(true, true), counters);
    }

    private JFreeChart getChart( CategoryItemRenderer renderer,Class<? extends Gauge>... counters) {
        List<Class<? extends Gauge>> gaugesToInclude = Arrays.asList(counters);
        DefaultStatisticalCategoryDataset dataset = new DefaultStatisticalCategoryDataset();
        for (Job job : jobs) {
            for (Gauge g : job.getCounters()) {
                if (gaugesToInclude.contains(g.getClass())) {
                    dataset.add(g.getMeasurement(), g.getStdev(), g.getTitle(), job.getParameter());
                }
            }
        }       
        CategoryAxis categoryAxis = new CategoryAxis("ambit datasets");
        ValueAxis valueAxis = new NumberAxis("time");
        CategoryPlot plot = new CategoryPlot(dataset, categoryAxis, valueAxis, renderer);
        plot.setOrientation(PlotOrientation.VERTICAL);
        JFreeChart c = new JFreeChart(title, plot);
        return c;
    }
}
