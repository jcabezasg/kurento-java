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

package org.kurento.repository.internal;

import static org.kurento.commons.PropertiesManager.getProperty;

import javax.servlet.MultipartConfigElement;

import org.kurento.commons.ConfigFileManager;
import org.kurento.commons.exception.KurentoException;
import org.kurento.repository.Repository;
import org.kurento.repository.RepositoryApiConfiguration;
import org.kurento.repository.RepositoryApiConfiguration.RepoType;
import org.kurento.repository.internal.repoimpl.filesystem.FileSystemRepository;
import org.kurento.repository.internal.repoimpl.mongo.MongoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class RepositoryApplicationContextConfiguration {

  public static final String KEY_CONFIG_FILENAME = "kurento-repo.conf.json";

  public static final String KEY_REPO_HOST = "repository.hostname";
  public static final String KEY_REPO_PORT = "repository.port";
  public static final String KEY_REPO_TYPE = "repository.type";

  public static final String KEY_FS_FOLDER = "repository.filesystem.folder";

  public static final String KEY_MG_DB = "repository.mongodb.dbName";
  public static final String KEY_MG_GRID = "repository.mongodb.gridName";
  public static final String KEY_MG_URL = "repository.mongodb.urlConn";

  static {
    ConfigFileManager.loadConfigFile(KEY_CONFIG_FILENAME);
  }

  public static int SERVER_PORT = getProperty(KEY_REPO_PORT, 7676);
  public static String SERVER_HOSTNAME = getProperty(KEY_REPO_HOST, "localhost");
  public static String REPO_TYPE = getProperty(KEY_REPO_TYPE, RepoType.MONGODB.getTypeValue());

  private static final Logger log =
      LoggerFactory.getLogger(RepositoryApplicationContextConfiguration.class);

  @Bean
  public MultipartConfigElement multipartConfigElement() {
    return new MultipartConfigElement("");
  }

  @Bean
  public Repository repository() {
    RepositoryApiConfiguration repositoryApiConfiguration = repositoryApiConfiguration();
    RepoType rtype = repositoryApiConfiguration.getRepositoryType();
    log.debug("Repository type: {}", rtype);
    if (rtype.isFilesystem()) {
      return new FileSystemRepository();
    } else if (rtype.isMongoDB()) {
      return new MongoRepository();
    } else {
      throw new KurentoException("Unrecognized repository type. Must be filesystem or mongodb");
    }
  }

  @Bean(destroyMethod = "shutdown")
  public TaskScheduler repositoryTaskScheduler() {
    return new ThreadPoolTaskScheduler();
  }

  @Bean
  public RepositoryApiConfiguration repositoryApiConfiguration() {

    RepositoryApiConfiguration config = new RepositoryApiConfiguration();

    config.setWebappPublicUrl("http://" + SERVER_HOSTNAME + ":" + SERVER_PORT + "/");

    RepoType type = RepoType.parseType(REPO_TYPE);
    config.setRepositoryType(type);
    StringBuilder sb = new StringBuilder(type.getTypeValue());

    if (type.isFilesystem()) {

      String filesFolder = getProperty(KEY_FS_FOLDER, config.getFileSystemFolder());
      config.setFileSystemFolder(filesFolder);
      sb.append("\n\t").append("folder : ").append(filesFolder);

    } else if (type.isMongoDB()) {

      String dbName = getProperty(KEY_MG_DB, config.getMongoDatabaseName());
      config.setMongoDatabaseName(dbName);
      sb.append("\n\t").append("dbName : ").append(dbName);
      String grid = getProperty(KEY_MG_GRID, config.getMongoGridFSCollectionName());
      config.setMongoGridFSCollectionName(grid);
      sb.append("\n\t").append("gridName : ").append(grid);
      String url = getProperty(KEY_MG_URL, config.getMongoUrlConnection());
      config.setMongoUrlConnection(url);
      sb.append("\n\t").append("urlConn : ").append(url);
    }

    log.debug("Repository config: {}", sb.toString());
    return config;
  }
}
