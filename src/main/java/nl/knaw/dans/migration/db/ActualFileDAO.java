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
package nl.knaw.dans.migration.db;

import io.dropwizard.hibernate.AbstractDAO;
import nl.knaw.dans.migration.core.tables.ActualFile;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActualFileDAO extends AbstractDAO<ActualFileDAO> {
  private static final Logger log = LoggerFactory.getLogger(ActualFileDAO.class);

  public ActualFileDAO(SessionFactory sessionFactory) {
    super(sessionFactory);
  }

  public void create(ActualFile actual) {
    log.trace(actual.toString());
    currentSession().save(actual);
  }

  public void deleteByDoi(String doi) {
    log.trace("deleting ActualFile {}", doi);
    int r = currentSession()
        .createQuery("DELETE FROM ActualFile WHERE doi = :doi")
        .setParameter("doi", doi)
        .executeUpdate();
    log.trace("deleted {} from ActualFile", r);
  }

  public void deleteAll() {
    log.trace("deleting all from ActualFile");
    int r = currentSession()
        .createQuery("DELETE FROM ActualFile")
        .executeUpdate();
    log.trace("deleted {} from ActualFile", r);
  }
}
