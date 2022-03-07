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
package nl.knaw.dans.filemigration.core;

import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class FileRightsTest {

    @Test
    public void parseEmbargoedDdm() throws IOException {
        FileRights datasetRights = DatasetRightsHandler
                .parseRights(new FileInputStream("src/test/resources/ddm/embargoed.xml"));
        assertEquals("ANONYMOUS", datasetRights.getAccessibleTo());
        assertEquals("ANONYMOUS", datasetRights.getVisibleTo());
        assertEquals("2062-02-14", datasetRights.getEmbargoDate());
    }

    @Test
    public void parseQuotedEmptyEmbargoDdm() throws IOException {
        FileRights datasetRights = DatasetRightsHandler
                .parseRights(new FileInputStream("src/test/resources/ddm/quoted-empty-embargo.xml"));
        assertEquals("ANONYMOUS", datasetRights.getAccessibleTo());
        assertEquals("ANONYMOUS", datasetRights.getVisibleTo());
        assertNull(datasetRights.getEmbargoDate());
    }

    @Test
    public void parseDD_874() throws IOException {
        FileRights datasetRights = DatasetRightsHandler
                .parseRights(new FileInputStream("src/test/resources/ddm/dd-874.xml"));
        assertEquals("ANONYMOUS", datasetRights.getAccessibleTo());
        assertEquals("ANONYMOUS", datasetRights.getVisibleTo());
        assertNull(datasetRights.getEmbargoDate());
    }

    @Test
    public void parseRestrictedDdm() throws IOException {
        FileRights datasetRights = DatasetRightsHandler
                .parseRights(new FileInputStream("src/test/resources/ddm/restricted.xml"));
        assertEquals("RESTRICTED_REQUEST", datasetRights.getAccessibleTo());
        assertEquals("RESTRICTED_REQUEST", datasetRights.getVisibleTo());
        assertNull(datasetRights.getEmbargoDate());
    }

    @Test
    public void applyEmbargoed() throws IOException {
        FileRights fileRights = FileRightsHandler
                .parseRights(new FileInputStream("src/test/resources/files.xml"))
                .get("data/secret.txt")
                .applyDefaults(DatasetRightsHandler
                        .parseRights(new FileInputStream("src/test/resources/ddm/embargoed.xml"))
                );
        assertEquals("NONE", fileRights.getAccessibleTo());
        assertEquals("NONE", fileRights.getVisibleTo());
        assertEquals("2062-02-14", fileRights.getEmbargoDate());
    }

    @Test
    public void applyRestricted() throws IOException {
        FileRights fileRights = FileRightsHandler
                .parseRights(new FileInputStream("src/test/resources/files.xml"))
                .get("data/secret.txt")
                .applyDefaults(DatasetRightsHandler
                        .parseRights(new FileInputStream("src/test/resources/ddm/restricted.xml"))
                );
        // note the priority for individual file rights over dataset rights
        assertEquals("NONE", fileRights.getAccessibleTo());
        assertEquals("NONE", fileRights.getVisibleTo());
        assertNull(fileRights.getEmbargoDate());
    }
}
