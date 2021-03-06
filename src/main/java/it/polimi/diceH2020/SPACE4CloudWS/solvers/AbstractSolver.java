/*
Copyright 2016-2017 Eugenio Gianniti
Copyright 2016 Michele Ciavotta

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package it.polimi.diceH2020.SPACE4CloudWS.solvers;

import com.jcraft.jsch.JSchException;
import it.polimi.diceH2020.SPACE4Cloud.shared.solution.SolutionPerJob;
import it.polimi.diceH2020.SPACE4Cloud.shared.settings.Technology;
import it.polimi.diceH2020.SPACE4CloudWS.connection.SshConnector;
import it.polimi.diceH2020.SPACE4CloudWS.core.DataProcessor;
import it.polimi.diceH2020.SPACE4CloudWS.fileManagement.FileUtility;
import it.polimi.diceH2020.SPACE4CloudWS.services.DataService;
import it.polimi.diceH2020.SPACE4CloudWS.services.SshConnectorProxy;
import it.polimi.diceH2020.SPACE4CloudWS.solvers.settings.ConnectionSettings;
import it.polimi.diceH2020.SPACE4CloudWS.solvers.settings.SettingsDealer;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.io.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;


public abstract class AbstractSolver implements Solver {

    protected final Integer MAX_ITERATIONS = 3;

    protected Logger logger = Logger.getLogger(getClass());

    @Setter(onMethod = @__(@Autowired))
    protected FileUtility fileUtility;

    @Setter(onMethod = @__(@Autowired))
    protected SshConnectorProxy connector;

    @Setter(onMethod = @__(@Autowired))
    private SettingsDealer settingsDealer;

    @Setter(onMethod = @__(@Autowired))
    protected DataService dataService;

    @Setter(onMethod = @__(@Autowired))
    protected Environment environment;

    protected ConnectionSettings connSettings;

    private Map<SolutionPerJob, String> remoteSubDirectories = new HashMap<> ();

    protected abstract Class<? extends ConnectionSettings> getSettingsClass();

    @PostConstruct
    @Override
    public void restoreDefaults() {
        connSettings = settingsDealer.getConnectionDefaults(getSettingsClass());
        SshConnector sshConnector = new SshConnector(connSettings);
        connector.registerConnector(sshConnector, getClass());
        logger.debug(String.format("<%s> Restored default solver settings",
                getClass().getCanonicalName()));
    }

    public void delete(List<File> pFiles) {
        if (fileUtility.delete(pFiles)) logger.debug("Working files correctly deleted");
    }

    @Override
    public void setMaxDuration(Integer duration){
        connSettings.setMaxDuration(duration);
    }

    @Override
    public void initRemoteEnvironment() throws Exception {
        List<String> lstProfiles = Arrays.asList(environment.getActiveProfiles());
        logger.info("------------------------------------------------");
        logger.info(String.format("Starting %s service initialization phase", this.getClass().getSimpleName()));
        logger.info("------------------------------------------------");
        if (lstProfiles.contains("test") && ! connSettings.isForceClean()) {
            logger.info("Test phase: the remote work directory tree is assumed to be ok.");
        } else {
            logger.info("Clearing remote work directory tree");
            connector.exec("rm -rf " + connSettings.getRemoteWorkDir(), getClass());
            logger.info("Creating new remote work directory tree");
            connector.exec("mkdir -p " + connSettings.getRemoteWorkDir(), getClass());
            logger.info("Done");
        }
    }

    protected void sendFiles(@NotNull String remoteDirectory, List<File> lstFiles) {
        try {
            connector.exec("mkdir -p " + remoteDirectory, getClass());
        } catch (JSchException | IOException e1) {
            logger.error("Cannot create new Simulation Folder!", e1);
        }

        lstFiles.forEach((File file) -> {
            try {
                connector.sendFile(file.getAbsolutePath(),
                        remoteDirectory + File.separator + file.getName(),
                        getClass());
            } catch (JSchException | IOException e) {
                logger.error("Error sending file: " + file.toString(), e);
            }
        });
    }

    protected synchronized void putRemoteSubDirectory (@NotNull SolutionPerJob solutionPerJob) {
        remoteSubDirectories.put (solutionPerJob,
                connSettings.getRemoteWorkDir() + File.separator + UUID.randomUUID());
    }

    protected synchronized void removeRemoteSubDirectory (@NotNull SolutionPerJob solutionPerJob) {
        remoteSubDirectories.remove (solutionPerJob);
    }

    protected String retrieveRemoteSubDirectory (@NotNull SolutionPerJob solutionPerJob) {
        return remoteSubDirectories.get (solutionPerJob);
    }

    protected void cleanRemoteSubDirectory (@NotNull String remoteDirectory) {
        String command = String.format ("rm -rf %s", remoteDirectory);
        try {
            connector.exec (command, getClass ());
        } catch (JSchException|IOException e) {
            logger.error ("Could not purge remote subdirectory", e);
        }
    }

    public Consumer<Double> metricUpdater (SolutionPerJob solutionPerJob, Technology technology) {
        return solutionPerJob::setDuration;
    }

    protected void writeLinesToFile(@NotNull List<String> lines, @NotNull File outputFile) throws IOException {
        try (BufferedWriter writer = new BufferedWriter (new FileWriter (outputFile))) {
            for (String line: lines) {
                writer.write (line);
                writer.newLine ();
            }
        }
    }

    protected  @NotNull List<String>
    processPlaceholders (@NotNull File templateFile,
                         @NotNull Map<String, String> nameValueMap) throws IOException {
        List<String> lines = new LinkedList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(templateFile))) {
            String line;

            while ((line = reader.readLine()) != null) {
                for (Map.Entry<String, String> entry: nameValueMap.entrySet()) {
                    line = line.replace(entry.getKey(), entry.getValue());
                }

                lines.add(line);
            }
        }

        return lines;
    }
}
