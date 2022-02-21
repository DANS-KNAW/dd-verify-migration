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
package nl.knaw.dans.filemigration.api;

import nl.knaw.dans.filemigration.core.FileRights;
import nl.knaw.dans.filemigration.core.ManifestCsv;
import org.hsqldb.lib.StringUtil;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import javax.persistence.*;
import java.util.Objects;

@Entity
@IdClass(ExpectedFileKey.class)
@Table(name = "expected")
public class ExpectedFile {
    // https://docs.jboss.org/hibernate/orm/5.6/userguide/html_single/Hibernate_User_Guide.html#schema-generation

    public ExpectedFile() {
    }

    public ExpectedFile(String doi, EasyFile easyFile, boolean removeOriginal, String depositor) {
        final String path = removeOriginal
                ? easyFile.getPath().replace("original/", "")
                : easyFile.getPath();
        final String dvPath = dvPath(path);

        setDoi(doi);
        setSha1Checksum(easyFile.getSha1Checksum());
        setEasyFileId(easyFile.getPid());
        setFsRdbPath(easyFile.getPath());
        setExpectedPath(dvPath);
        setAccessibleTo(easyFile.getAccessibleTo());
        setVisibleTo(easyFile.getVisibleTo());
        setAddedDuringMigration(false);
        setRemovedThumbnail(path.toLowerCase().matches(".*thumbnails/.*_small.(png|jpg|tiff)"));
        setRemovedOriginalDirectory(removeOriginal);
        setRemovedDuplicateFileCount(0);
        setDepositor(depositor);
        setTransformedName(!path.equals(dvPath));
    }

    public ExpectedFile(String doi, ManifestCsv manifestCsv, FileRights fileRights, String depositor) {
        final String path = manifestCsv.getPath();
        final String dvPath = dvPath(path);

        setDoi(doi);
        setSha1Checksum(manifestCsv.getSha1());
        setEasyFileId("");
        setFsRdbPath(manifestCsv.getPath());
        setExpectedPath(dvPath);
        setAccessibleTo(fileRights.getAccessibleTo());
        setVisibleTo(fileRights.getVisibleTo());
        setAddedDuringMigration(false);
        setRemovedThumbnail(path.toLowerCase().matches(".*thumbnails/.*_small.(png|jpg|tiff)"));
        setRemovedOriginalDirectory(false);
        setRemovedDuplicateFileCount(0);
        setDepositor(depositor);
        setTransformedName(!path.equals(dvPath));
    }

    private String dvPath(String path) {
        final String file = replaceForbidden(path.replaceAll(".*/", ""), forbiddenInFileName);
        final String folder = replaceForbidden(path.replaceAll("[^/]*$", ""), forbiddenInFolders);
        final String dvPath = folder + file;
        return dvPath;
    }

    private static final String forbidden = ":*?\"<>|;#";
    private static final char[] forbiddenInFileName = ":*?\"<>|;#".toCharArray();
    private static final char[] forbiddenInFolders = (forbidden + "'(),[]&+'").toCharArray();

    private static String replaceForbidden(String s, char[] forbidden) {
        for (char c : forbidden)
            s = s.replace(c, '_');
        return s;
    }

    // most lengths from easy-dtap/provisioning/roles/easy-fs-rdb/templates/create-easy-db-tables.sql
    // doi length as in dd-dtap/shared-code/dataverse/scripts/database/create/create_v*.sql

    @Id
    @Column(length = 255)
    private String doi;

    @Id
    @Column(name="expected_path", length = 1024) // TODO basic_file_meta has only 1000
    private String expectedPath;

    @Id
    @Column()
    private String depositor;

    @Id
    @Column(name="removed_duplicate_file_count")
    private int removedDuplicateFileCount;

    @Column(name="removed_original_directory")
    private boolean removedOriginalDirectory;

    @Column(name="sha1_checksum", length = 40)
    private String sha1Checksum = "";

    @Column(name="easy_file_id", length = 64)
    private String easyFileId = "";

    @Column(name="fs_rdb_path", length = 1024)
    private String fsRdbPath = "";

    @Column(name="added_during_migration")
    private boolean addedDuringMigration;

    @Column(name="removed_thumbnail")
    private boolean removedThumbnail;

    @Column(name="transformed_name")
    private boolean transformedName;

    @Column()
    private String accessibleTo;

    @Column()
    private String visibleTo;

    @Nullable
    @Column(name="embargo_date")
    private String embargoDate;

    @Override
    public String toString() {
        return "ExpectedFile{" +
                "doi='" + doi + '\'' +
                ", expectedPath='" + expectedPath + '\'' +
                ", depositor='" + depositor + '\'' +
                ", removedDuplicateFileCount=" + removedDuplicateFileCount +
                ", removedOriginalDirectory=" + removedOriginalDirectory +
                ", sha1Checksum='" + sha1Checksum + '\'' +
                ", easyFileId='" + easyFileId + '\'' +
                ", fsRdbPath='" + fsRdbPath + '\'' +
                ", addedDuringMigration=" + addedDuringMigration +
                ", removedThumbnail=" + removedThumbnail +
                ", transformedName=" + transformedName +
                ", accessibleTo='" + accessibleTo + '\'' +
                ", visibleTo='" + visibleTo + '\'' +
                ", embargoDate='" + embargoDate + '\'' +
                '}';
    }

    public boolean isTransformedName() {
        return transformedName;
    }

    public void setTransformedName(boolean transformedName) {
        this.transformedName = transformedName;
    }

    public int getRemovedDuplicateFileCount() {
        return removedDuplicateFileCount;
    }

    public void setRemovedDuplicateFileCount(int removedDuplicateFileCount) {
        this.removedDuplicateFileCount = removedDuplicateFileCount;
    }

    public void incRemoved_duplicate_file_count() {
        this.removedDuplicateFileCount += 1;
    }

    public boolean isRemovedOriginalDirectory() {
        return removedOriginalDirectory;
    }

    public void setRemovedOriginalDirectory(boolean removedOriginalDirectory) {
        this.removedOriginalDirectory = removedOriginalDirectory;
    }

    public boolean isRemovedThumbnail() {
        return removedThumbnail;
    }

    public void setRemovedThumbnail(boolean removedThumbnail) {
        this.removedThumbnail = removedThumbnail;
    }

    public boolean isAddedDuringMigration() {
        return addedDuringMigration;
    }

    public void setAddedDuringMigration(boolean addedDuringMigration) {
        this.addedDuringMigration = addedDuringMigration;
    }

    public String getExpectedPath() {
        return expectedPath;
    }

    public void setExpectedPath(String expectedPath) {
        this.expectedPath = expectedPath;
    }

    public String getFsRdbPath() {
        return fsRdbPath;
    }

    public void setFsRdbPath(String fsRdbPath) {
        this.fsRdbPath = fsRdbPath;
    }

    public String getEasyFileId() {
        return easyFileId;
    }

    public void setEasyFileId(String easyFileId) {
        this.easyFileId = easyFileId;
    }

    public String getSha1Checksum() {
        return sha1Checksum;
    }

    public void setSha1Checksum(String sha1Checksum) {
        this.sha1Checksum = sha1Checksum;
    }

    public String getDoi() {
        return doi;
    }

    public void setDoi(String doi) {
        this.doi = doi;
    }

    public String getDepositor() {
        return depositor;
    }

    public void setDepositor(String depositor) {
        // TODO mapping of easy-convert-bag-to-deposit/src/main/assembly/dist/cfg/account-substitutes.csv
        this.depositor = depositor;
    }

    @Nullable
    public String getEmbargoDate() {
        return embargoDate;
    }

    public void setEmbargoDate(@Nullable String dateAvailable) {
        if (!StringUtil.isEmpty(dateAvailable) && DateTime.now().compareTo(DateTime.parse(dateAvailable)) < 0)
            this.embargoDate = dateAvailable;
    }

    public String getAccessibleTo() {
        return accessibleTo;
    }

    public void setAccessibleTo(String accessibleTo) {
        this.accessibleTo = accessibleTo;
    }

    public String getVisibleTo() {
        return visibleTo;
    }

    public void setVisibleTo(String visibleTo) {
        this.visibleTo = visibleTo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExpectedFile that = (ExpectedFile) o;
        return removedDuplicateFileCount == that.removedDuplicateFileCount && removedOriginalDirectory == that.removedOriginalDirectory && addedDuringMigration == that.addedDuringMigration && removedThumbnail == that.removedThumbnail && transformedName == that.transformedName && Objects.equals(doi, that.doi) && Objects.equals(expectedPath, that.expectedPath) && Objects.equals(depositor, that.depositor) && Objects.equals(sha1Checksum, that.sha1Checksum) && Objects.equals(easyFileId, that.easyFileId) && Objects.equals(fsRdbPath, that.fsRdbPath) && Objects.equals(accessibleTo, that.accessibleTo) && Objects.equals(visibleTo, that.visibleTo) && Objects.equals(embargoDate, that.embargoDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(doi, expectedPath, depositor, removedDuplicateFileCount, removedOriginalDirectory, sha1Checksum, easyFileId, fsRdbPath, addedDuringMigration, removedThumbnail, transformedName, accessibleTo, visibleTo, embargoDate);
    }
}
