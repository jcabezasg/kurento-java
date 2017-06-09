/*
 * (C) Copyright 2013 Kurento (http://kurento.org/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.kurento.repository;

public class RepositoryApiConfiguration {

  private static final String DEFAULT_MONGO_URL = "mongodb://localhost";
  private static final String DEFAULT_MONGO_GRIDFS = "fs";
  private static final String DEFAULT_MONGO_DBNAME = "kurento-repository";
  public static final String DEFAULT_FILESYSTEM_LOC = "repository";

  public enum RepoType {
    FILESYSTEM("filesystem"), MONGODB("mongodb");

    private String value;

    private RepoType(String val) {
      this.value = val;
    }

    public String getTypeValue() {
      return this.value;
    }

    public static RepoType parseType(String typeValue) {
      for (RepoType t : RepoType.values()) {
        if (t.getTypeValue().equalsIgnoreCase(typeValue)) {
          return t;
        }
      }
      return FILESYSTEM;
    }

    public boolean isFilesystem() {
      return this.compareTo(FILESYSTEM) == 0;
    }

    public boolean isMongoDB() {
      return this.compareTo(MONGODB) == 0;
    }
  }

  private String webappPublicUrl = "";
  private RepoType repositoryType = RepoType.FILESYSTEM;
  private String fileSystemFolder = DEFAULT_FILESYSTEM_LOC;
  private String mongoDatabaseName = DEFAULT_MONGO_DBNAME;
  private String mongoGridFSCollectionName = DEFAULT_MONGO_GRIDFS;
  private String mongoUrlConnection = DEFAULT_MONGO_URL;

  /**
   * Returns the repository public URL. The default value is "" and can be changed using
   * {@link RepositoryApiConfiguration#setWebappPublicUrl(String)}.
   *
   * @return the repository public URL.
   */
  public String getWebappPublicUrl() {
    return webappPublicUrl;
  }

  /**
   * Sets the public URL for the webapp of the repository.
   *
   * @param webappPublicUrl
   *          URL for the web application
   */
  public void setWebappPublicUrl(String webappPublicUrl) {
    this.webappPublicUrl = webappPublicUrl;
  }

  /**
   * Returns the repository type. The default value is {@link RepoType#FILESYSTEM} and can be
   * changed with a properties file or with the method
   * {@link RepositoryApiConfiguration#setRepositoryType(String)}.
   *
   * @return the repository type.
   */
  public RepoType getRepositoryType() {
    return repositoryType;
  }

  /**
   * Sets the type of the repository. The value can be {@link RepoType#FILESYSTEM} or
   * {@link RepoType#MONGODB}.
   *
   * @param repositoryType
   *          type of the repository
   */
  public void setRepositoryType(RepoType repositoryType) {
    this.repositoryType = repositoryType;
  }

  /**
   * Returns the folder path of the repository. The default value is "repository" and can be changed
   * with a properties file or with the method
   * {@link RepositoryApiConfiguration#setFileSystemFolder(String)}. This property is only used when
   * the repository type is "filesystem".
   *
   * @return the folder path of the repository.
   */
  public String getFileSystemFolder() {
    return fileSystemFolder;
  }

  /**
   * Sets the folder path of the repository. This property is only used when the repository type is
   * "filesystem".
   *
   * @param fileSystemFolder
   *          folder, in the filesystem, that the repository will use to save the files
   */
  public void setFileSystemFolder(String fileSystemFolder) {
    this.fileSystemFolder = fileSystemFolder;
  }

  /**
   * Returns the database name used for the repository. The default value is "kurento-repository"
   * and can be changed with a properties file or with the method
   * {@link RepositoryApiConfiguration#setMongoDatabaseName(String)}. This property is only used
   * when the repository type is "mongodb".
   *
   * @return the database name.
   */
  public String getMongoDatabaseName() {
    return mongoDatabaseName;
  }

  /**
   * Sets the database name used for the repository. This property is only used when the repository
   * type is "mongodb".
   *
   * @param mongoDatabaseName
   *          The database name
   */
  public void setMongoDatabaseName(String mongoDatabaseName) {
    this.mongoDatabaseName = mongoDatabaseName;
  }

  /**
   * Returns the name of the gridfs collection used for the repository. The default value is "fs"
   * and can be changed with a properties file or with the method
   * {@link RepositoryApiConfiguration#setMongoGridFSCollectionName(String)}. This property is only
   * used when the repository type is "mongodb".
   *
   * @return the name of the gridfs collection.
   */
  public String getMongoGridFSCollectionName() {
    return mongoGridFSCollectionName;
  }

  /**
   * Sets the name of the gridfs collection used for the repository. This property is only used when
   * the repository type is "mongodb".
   *
   * @param mongoGridFSCollectionName
   *          the name of the gridfs collection
   */
  public void setMongoGridFSCollectionName(String mongoGridFSCollectionName) {
    this.mongoGridFSCollectionName = mongoGridFSCollectionName;
  }

  /**
   * Returns the connection to mongo database. The default value is "mongodb://localhost" and can be
   * changed with a properties file or with the method
   * {@link RepositoryApiConfiguration#setMongoUrlConnection(String)}. This property is only used
   * when the repository type is "mongodb".
   *
   * @return the connection to mongo database.
   */
  public String getMongoUrlConnection() {
    return mongoUrlConnection;
  }

  /**
   * Sets the connection to mongo database in the format specified in
   * http://docs.mongodb.org/manual/reference/connection-string/. This property is only used when
   * the repository type is "mongodb".
   *
   * @param mongoUrlConnection
   *          connection URL for the mong database
   */
  public void setMongoUrlConnection(String mongoUrlConnection) {
    this.mongoUrlConnection = mongoUrlConnection;
  }
}