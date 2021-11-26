/*
 * Copyright (C) 2021 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.knaw.dans.filemigration.cli;

import io.dropwizard.Application;
import io.dropwizard.cli.EnvironmentCommand;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.hibernate.UnitOfWorkAwareProxyFactory;
import io.dropwizard.setup.Environment;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import nl.knaw.dans.filemigration.DdVerifyFileMigrationConfiguration;
import nl.knaw.dans.filemigration.core.FedoraToBagCsv;
import nl.knaw.dans.filemigration.core.EasyFileLoader;
import nl.knaw.dans.filemigration.db.EasyFileDAO;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static nl.knaw.dans.filemigration.core.FedoraToBagCsv.*;

public class LoadFromEasyCommand  extends EnvironmentCommand<DdVerifyFileMigrationConfiguration> {

    private static final Logger log = LoggerFactory.getLogger(LoadFromEasyCommand.class);
    private final HibernateBundle<DdVerifyFileMigrationConfiguration> hibernate;

    /**
     * Creates a new environment command.
     *
     * @param application the application providing this command
     */
    public LoadFromEasyCommand(Application<DdVerifyFileMigrationConfiguration> application, HibernateBundle<DdVerifyFileMigrationConfiguration> hibernate) {
        super(application, "load-from-easy", "Load expected table with info from easy_files in fs-rdb and transformation rules");
        this.hibernate = hibernate;
    }

    @Override
    public void configure(Subparser subparser) {
        super.configure(subparser);
        subparser.addArgument("csv")
            .type(File.class)
            .nargs("*")
            .help("CSV file produced by easy-fedora-to-bag");
    }

    @Override
    protected void run(Environment environment, Namespace namespace, DdVerifyFileMigrationConfiguration configuration) throws Exception {
        EasyFileDAO easyFileDAO = new EasyFileDAO(hibernate.getSessionFactory());
        // https://stackoverflow.com/questions/42384671/dropwizard-hibernate-no-session-currently-bound-to-execution-context
        EasyFileLoader proxy = new UnitOfWorkAwareProxyFactory(hibernate)
            .create(EasyFileLoader.class, EasyFileDAO.class, easyFileDAO);
        for (File file : namespace.<File>getList("csv")) {
            log.info(file.toString());
            for(CSVRecord r: FedoraToBagCsv.parse(file)) {
                proxy.loadFromCsv(new FedoraToBagCsv(r));
            }
        }
    }
}